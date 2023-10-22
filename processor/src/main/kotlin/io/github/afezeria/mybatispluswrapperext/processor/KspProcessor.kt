package io.github.afezeria.mybatispluswrapperext.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo


/**
 *
 * @date 2021/7/17
 */
lateinit var logger: KSPLogger
lateinit var globalResolver: Resolver
lateinit var globalEnvironment: SymbolProcessorEnvironment

@OptIn(KspExperimental::class)
class KspProcessor(val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    /**
     * 用户自定义的用于标记要忽略的mapper的注解
     */
    val ignoreMarkerAnnotation: String?

    /**
     * 数据库字段命名风格，当类属性未显示指定数据表字段名时会使用该配置根据字段名推断数据库表字段名
     */
    val dbNamingConvention: NamingConvention

    init {
        logger = environment.logger
        globalEnvironment = environment
        ignoreMarkerAnnotation = environment.options[::ignoreMarkerAnnotation.name]
        dbNamingConvention = NamingConvention.valueOf(environment.options[::dbNamingConvention.name] ?: "FIELD_NAME")
    }

    lateinit var BASE_MAPPER_CLASS: KSClassDeclaration
    lateinit var BASE_MAPPER_TYPE: KSType

    val processedMapperSet = mutableSetOf<String>()

    val packageLevelApiGenerateHandler = PackageLevelApiGenerateHandler()

    fun init(resolver: Resolver) {
        globalResolver = resolver
        BASE_MAPPER_CLASS = resolver.getClassDeclarationByName(BASE_MAPPER_CLASS_NAME.canonicalName)!!
        BASE_MAPPER_TYPE = BASE_MAPPER_CLASS.asStarProjectedType()
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        init(resolver)
        resolver.getAllFiles()
            .map { it.declarations.filterIsInstance<KSClassDeclaration>() }
            .flatten()
            .filter {
                BASE_MAPPER_TYPE.isAssignableFrom(it.asStarProjectedType())
                        && it.isAbstract()
                        && (ignoreMarkerAnnotation == null || it.annotations.none {
                    it.annotationType.resolve().declaration.qualifiedName!!.asString() == ignoreMarkerAnnotation
                })
            }
            .forEach {
                handleMapperInterface(it)
            }

        return emptyList()
    }

    private fun handleMapperInterface(mapper: KSClassDeclaration) {
        if (processedMapperSet.contains(mapper.qualifiedName!!.asString())) {
            return
        }
        processedMapperSet.add(mapper.qualifiedName!!.asString())
        if (mapper.typeParameters.isNotEmpty()) {
            logger.warn(
                "The interface should not have generic parameters. ${mapper.qualifiedName!!.asString()}",
                mapper
            )
            return
        }
        val entityClass = findEntityType(mapper)
            ?: run {
                logger.warn(
                    "The entity class type cannot be determined from the definition of '${mapper.qualifiedName!!.asString()}'.",
                    mapper
                )
                return
            }

        packageLevelApiGenerateHandler.generate(mapper.packageName.asString())


        val mapperClassName = mapper.toClassName()
        val entityClassName = entityClass.toClassName()
        val fileSpecBuilder =
            FileSpec.builder(mapper.packageName.asString(), mapper.simpleName.asString() + "Extensions")
        val tableDefInterface = addTableDefInterface(fileSpecBuilder, mapperClassName, entityClassName, entityClass)
        val tableDefClass =
            addTableDefClass(fileSpecBuilder, mapperClassName, entityClassName, entityClass, tableDefInterface)
        val whereScopeClass =
            addWhereScopeClass(fileSpecBuilder, mapperClassName, entityClassName, tableDefInterface, tableDefClass)
        val updateScopeClass = addUpdateScopeClass(
            fileSpecBuilder,
            mapperClassName,
            entityClassName,
            tableDefInterface,
            tableDefClass,
            whereScopeClass
        )

        addExtensionMethod(
            fileSpecBuilder,
            mapperClassName,
            entityClassName,
            entityClass,
            tableDefInterface,
            whereScopeClass,
            updateScopeClass
        )

        val fileSpec = fileSpecBuilder.build()
        fileSpec.writeTo(
            environment.codeGenerator,
            Dependencies(false, mapper.containingFile!!, entityClass.containingFile!!)
        )
    }

    /**
     * ```
     * interface PersonTableDef {
     *     val ID: FieldDef<Int, Int?>
     *     val NAME: FieldDef<String, String?>
     *     val AGE: FieldDef<Int, Int?>
     * }
     * ```
     * @param fileSpecBuilder Builder
     * @param mapperClassName ClassName
     * @param entityClassName ClassName
     * @param entityClass KSClassDeclaration
     * @return ClassName
     */
    private fun addTableDefInterface(
        fileSpecBuilder: FileSpec.Builder,
        mapperClassName: ClassName,
        entityClassName: ClassName,
        entityClass: KSClassDeclaration,
    ): ClassName {
        val tableDefInterfaceName = ClassName(mapperClassName.packageName, "${entityClassName.simpleName}TableDef")

        val interfaceBuilder = TypeSpec.interfaceBuilder(tableDefInterfaceName)

        val props = entityClass.getAllProperties()
            .filter { it.hasBackingField && !it.isDelegated() }
            .toList()

        val fieldDefClassName = packageLevelApiGenerateHandler.currentFieldDefClassName

        for (property in props) {
            val tableField = property.findAnnotation(TABLE_FIELD_QUALIFIED_NAME)
            if (tableField?.getValue<Boolean>("exist") == false) {
                continue
            }

            val doc = if (property.docString.isNullOrBlank()) {
                ""
            } else {
                property.docString!!.trim() + "\n\n"
            } + "@see ${property.qualifiedName?.asString()}"
            interfaceBuilder.addProperty(
                PropertySpec.builder(
                    NamingConvention.CONSTANT_CASE.convert(property.simpleName.asString()),
                    fieldDefClassName.parameterizedBy(
                        property.type.resolve().makeNotNullable().toTypeName(),
                        property.type.resolve().toTypeName()
                    )
                ).addKdoc("%L", doc)
                    .build()
            )
        }
        fileSpecBuilder.addType(interfaceBuilder.build())
        return tableDefInterfaceName
    }

    /**
     * ```
     * object PersonTableDefImpl : PersonTableDef {
     *     override val ID: FieldDef<Int, Int?> = FieldDef<Int, Int?>("id")
     *     override val NAME: FieldDef<String, String?> = FieldDef<String, String?>("name")
     *     override val AGE: FieldDef<Int, Int?> = FieldDef<Int, Int?>("age")
     * }
     * ```
     */
    private fun addTableDefClass(
        fileSpecBuilder: FileSpec.Builder,
        mapperClassName: ClassName,
        entityClassName: ClassName,
        entityClass: KSClassDeclaration,
        tableDefInterfaceClassName: ClassName,
    ): ClassName {
        val tableDefObjectName = ClassName(mapperClassName.packageName, "${entityClassName.simpleName}TableDefImpl")

        val classBuilder = TypeSpec.objectBuilder(tableDefObjectName)
            .addSuperinterface(tableDefInterfaceClassName)

        val props = entityClass.getAllProperties()
            .filter { it.hasBackingField && !it.isDelegated() }
            .toList()

        val fieldDefClassName = packageLevelApiGenerateHandler.currentFieldDefClassName


        for (property in props) {
            val tableField = property.findAnnotation(TABLE_FIELD_QUALIFIED_NAME)
            val tableId = property.findAnnotation(TABLE_ID_QUALIFIED_NAME)
            if (tableField?.getValue<Boolean>("exist") == false) {
                continue
            }
            val dbFieldName: String = tableId?.getValue("value")
                ?: tableField?.getValue("value")
                ?: dbNamingConvention.convert(property.simpleName.asString())

            classBuilder.addProperty(
                PropertySpec.builder(
                    NamingConvention.CONSTANT_CASE.convert(property.simpleName.asString()),
                    fieldDefClassName.parameterizedBy(
                        property.type.resolve().makeNotNullable().toTypeName(),
                        property.type.resolve().toTypeName()
                    )
                ).initializer(
                    "%T(%S)",
                    fieldDefClassName,
                    dbFieldName
                ).addModifiers(KModifier.OVERRIDE)
                    .build()
            )
        }
        fileSpecBuilder.addType(classBuilder.build())
        return tableDefObjectName
    }

    /**
     * ```
     * class PersonWhereScope(mapper: BaseMapper<Person>) :
     * WhereScope<PersonWhereScope, PersonTableDef, Person>(mapper, PersonTableDefImpl),
     * PersonTableDef by PersonTableDefImpl
     * ```
     */
    private fun addWhereScopeClass(
        fileSpecBuilder: FileSpec.Builder,
        mapperClassName: ClassName,
        entityClassName: ClassName,
        tableDefInterfaceClassName: ClassName,
        tableDefObjectClassName: ClassName,
    ): ClassName {
        val whereScopeClassName = packageLevelApiGenerateHandler.currentWhereScopeClassName

        val entityWhereScopeName = ClassName(mapperClassName.packageName, "${entityClassName.simpleName}WhereScope")
        val entityWhereScopeClass = TypeSpec.classBuilder(entityWhereScopeName)
            .superclass(
                whereScopeClassName.parameterizedBy(
                    entityWhereScopeName,
                    tableDefInterfaceClassName,
                    entityClassName
                )
            )
            .addSuperclassConstructorParameter("mapper, %T", tableDefObjectClassName)
            .addSuperinterface(tableDefInterfaceClassName, delegate = CodeBlock.of("%T", tableDefObjectClassName))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("mapper", BASE_MAPPER_CLASS_NAME.parameterizedBy(entityClassName))
                    .build()
            ).build()
        fileSpecBuilder.addType(entityWhereScopeClass)
        return entityWhereScopeName
    }

    /**
     * ```
     * class PersonUpdateScope(finalWhereScope: FinalWhereScope<PersonWhereScope, PersonTableDef, Person>) :
     * UpdateScope<PersonUpdateScope, PersonTableDef, Person>(
     *     finalWhereScope.getUpdateWrapper(),
     *     PersonTableDefImpl,
     * ),
     * PersonTableDef by PersonTableDefImpl
     * ```
     */
    private fun addUpdateScopeClass(
        fileSpecBuilder: FileSpec.Builder,
        mapperClassName: ClassName,
        entityClassName: ClassName,
        tableDefInterfaceClassName: ClassName,
        tableDefObjectClassName: ClassName,
        tableWhereScopeClassName: ClassName,
    ): ClassName {
        val finalWhereScopeClassName = packageLevelApiGenerateHandler.currentFinalWhereScopeClassName
        val updateScopeClassName = packageLevelApiGenerateHandler.currentUpdateScopeClassName

        val tableUpdateScopeName = ClassName(mapperClassName.packageName, "${entityClassName.simpleName}UpdateScope")
        val tableUpdateScopeClass = TypeSpec.classBuilder(tableUpdateScopeName)
            .superclass(
                updateScopeClassName.parameterizedBy(
                    tableUpdateScopeName,
                    tableDefInterfaceClassName,
                    entityClassName
                )
            )
            .addSuperclassConstructorParameter(
                "scope.getUpdateWrapper(), %T",
                tableDefObjectClassName,
            )
            .addSuperinterface(tableDefInterfaceClassName, delegate = CodeBlock.of("%T", tableDefObjectClassName))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        "scope",
                        finalWhereScopeClassName.parameterizedBy(
                            tableWhereScopeClassName,
                            tableDefInterfaceClassName,
                            entityClassName
                        )
                    )
                    .build()
            ).build()
        fileSpecBuilder.addType(tableUpdateScopeClass)
        return tableUpdateScopeName
    }

    fun addExtensionMethod(
        fileSpecBuilder: FileSpec.Builder,
        mapperClassName: ClassName,
        entityClassName: ClassName,
        entityClass: KSClassDeclaration,
        tableDefInterfaceClassName: ClassName,
        tableWhereScopeClassName: ClassName,
        tableUpdateScopeClassName: ClassName,
    ) {
        val finalWhereScopeClassName = packageLevelApiGenerateHandler.currentFinalWhereScopeClassName
        val fieldDefClassName = packageLevelApiGenerateHandler.currentFieldDefClassName

        //query==========
        /**
         * ```
         * fun PersonMapper.where(
         *     fn: PersonWhereScope.() -> Unit
         * ): FinalWhereScope<PersonWhereScope, PersonTableDef, Person> {
         *     val scope = PersonWhereScope(this)
         *     scope.invoke(fn)
         *     return FinalWhereScope(scope)
         * }
         * ```
         */
        fileSpecBuilder.addFunction(
            FunSpec.builder("where")
                .receiver(mapperClassName)
                .addParameter("fn", LambdaTypeName.get(tableWhereScopeClassName, emptyList(), UNIT_CLASS_NAME))
                .returns(
                    finalWhereScopeClassName.parameterizedBy(
                        tableWhereScopeClassName,
                        tableDefInterfaceClassName,
                        entityClassName
                    )
                )
                .addCode(
                    """
                    val s = %T(this)
                    return s(fn)
                """.trimIndent(), tableWhereScopeClassName
                )
                .build()
        )

        /**
         * ```
         * fun PersonMapper.queryList(
         *     fn: PersonWhereScope.() -> Unit
         * ): List<Person> {
         *     val scope = PersonWhereScope(this)
         *     scope.invoke(fn)
         *     return FinalWhereScope(scope).toList()
         * }
         * ```
         */
        fileSpecBuilder.addFunction(
            FunSpec.builder("queryList")
                .receiver(mapperClassName)
                .returns(LIST_CLASS_NAME.parameterizedBy(entityClassName))
                .addParameter("fn", LambdaTypeName.get(tableWhereScopeClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val s = %T(this)
                    s(fn)
                    return %T(s).toList()
                """.trimIndent(), tableWhereScopeClassName, finalWhereScopeClassName
                )
                .build()
        )

        /**
         * ```
         * fun PersonMapper.queryOne(
         *     fn: PersonWhereScope.() -> Unit
         * ): Person? {
         *     val scope = PersonWhereScope(this)
         *     scope.invoke(fn)
         *     return FinalWhereScope(scope).toOne()
         * }
         * ```
         */
        fileSpecBuilder.addFunction(
            FunSpec.builder("queryOne")
                .receiver(mapperClassName)
                .returns(entityClassName.copy(nullable = true))
                .addParameter("fn", LambdaTypeName.get(tableWhereScopeClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val s = %T(this)
                    s(fn)
                    return %T(s).toOne()
                """.trimIndent(), tableWhereScopeClassName, finalWhereScopeClassName
                )
                .build()
        )

        /**
         * fun <P : IPage<Person>> PersonMapper.queryPage(
         *     page: P,
         *     fn: PersonWhereScope.() -> Unit
         * ): P {
         *     val scope = PersonWhereScope(this)
         *     scope(fn)
         *     return FinalWhereScope(scope).toPage(page)
         * }
         */
        val pageType = TypeVariableName("P", I_PAGE_CLASS_NAME.parameterizedBy(entityClassName))
        fileSpecBuilder.addFunction(
            FunSpec.builder("queryPage")
                .addTypeVariable(pageType)
                .receiver(mapperClassName)
                .returns(pageType)
                .addParameter("page", pageType)
                .addParameter("fn", LambdaTypeName.get(tableWhereScopeClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val s = %T(this)
                    s(fn)
                    return %T(s).toPage(page)
                """.trimIndent(), tableWhereScopeClassName, finalWhereScopeClassName
                )
                .build()
        )

        /**
         * fun PersonMapper.queryCount(
         *     fn: PersonWhereScope.() -> Unit
         * ): Long {
         *     val scope = PersonWhereScope(this)
         *     scope(fn)
         *     return FinalWhereScope(scope).toCount()
         * }
         */
        fileSpecBuilder.addFunction(
            FunSpec.builder("queryCount")
                .receiver(mapperClassName)
                .returns(LONG_CLASS_NAME)
                .addParameter("fn", LambdaTypeName.get(tableWhereScopeClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val s = %T(this)
                    s(fn)
                    return %T(s).toCount()
                """.trimIndent(), tableWhereScopeClassName, finalWhereScopeClassName
                )
                .build()
        )

        /**
         * fun <F1> PersonMapper.querySingleFieldList(
         *     columnsFn: PersonTableDef.() -> FieldDef<*, F1>,
         *     fn: PersonWhereScope.() -> Unit
         * ): List<F1> {
         *     val scope = PersonWhereScope(this)
         *     scope.invoke(fn)
         *     return FinalWhereScope(scope).toSingleFieldList(columnsFn)
         * }
         *
         */
        fileSpecBuilder.addFunction(
            FunSpec.builder("querySingleFieldList")
                .addTypeVariable(TYPE_VAR_F1)
                .receiver(mapperClassName)
                .returns(List::class.asClassName().parameterizedBy(TYPE_VAR_F1))
                .addParameter(
                    "columnsFn",
                    LambdaTypeName.get(
                        tableDefInterfaceClassName,
                        emptyList(),
                        fieldDefClassName.parameterizedBy(STAR, TYPE_VAR_F1)
                    )
                )
                .addParameter("fn", LambdaTypeName.get(tableWhereScopeClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val s = %T(this)
                    s(fn)
                    return %T(s).toSingleFieldList(columnsFn)
                """.trimIndent(), tableWhereScopeClassName, finalWhereScopeClassName
                )
                .build()
        )

        /**
         * fun <F1, F2> PersonMapper.queryPairList(
         *     columnsFn: PersonTableDef.() -> Pair<FieldDef<*, F1>, FieldDef<*, F2>>,
         *     fn: PersonWhereScope.() -> Unit
         * ): List<Pair<F1, F2>> {
         *     val scope = PersonWhereScope(this)
         *     scope(fn)
         *     return FinalWhereScope(scope).toPairList(columnsFn)
         * }
         *
         *
         */
        fileSpecBuilder.addFunction(
            FunSpec.builder("queryPairList")
                .addTypeVariable(TYPE_VAR_F1)
                .addTypeVariable(TYPE_VAR_F2)
                .receiver(mapperClassName)
                .returns(LIST_CLASS_NAME.parameterizedBy(PAIR_CLASS_NAME.parameterizedBy(TYPE_VAR_F1, TYPE_VAR_F2)))
                .addParameter(
                    "columnsFn",
                    LambdaTypeName.get(
                        tableDefInterfaceClassName,
                        emptyList(),
                        PAIR_CLASS_NAME.parameterizedBy(
                            fieldDefClassName.parameterizedBy(STAR, TYPE_VAR_F1),
                            fieldDefClassName.parameterizedBy(STAR, TYPE_VAR_F2),
                        )
                    )
                )
                .addParameter("fn", LambdaTypeName.get(tableWhereScopeClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val s = %T(this)
                    s(fn)
                    return %T(s).toPairList(columnsFn)
                """.trimIndent(), tableWhereScopeClassName, finalWhereScopeClassName
                )
                .build()
        )

        /**
         * fun <F1, F2, F3> PersonMapper.queryTripleList(
         *     columnsFn: PersonTableDef.() -> Triple<FieldDef<*, F1>, FieldDef<*, F2>, FieldDef<*, F3>>,
         *     fn: PersonWhereScope.() -> Unit
         * ): List<Triple<F1, F2, F3>> {
         *     val scope = PersonWhereScope(this)
         *     scope(fn)
         *     return FinalWhereScope(scope).toTripleList(columnsFn)
         * }
         */
        fileSpecBuilder.addFunction(
            FunSpec.builder("queryTripleList")
                .addTypeVariable(TYPE_VAR_F1)
                .addTypeVariable(TYPE_VAR_F2)
                .addTypeVariable(TYPE_VAR_F3)
                .receiver(mapperClassName)
                .returns(
                    LIST_CLASS_NAME.parameterizedBy(
                        TRIPLE_CLASS_NAME.parameterizedBy(
                            TYPE_VAR_F1,
                            TYPE_VAR_F2,
                            TYPE_VAR_F3
                        )
                    )
                )
                .addParameter(
                    "columnsFn",
                    LambdaTypeName.get(
                        tableDefInterfaceClassName,
                        emptyList(),
                        TRIPLE_CLASS_NAME.parameterizedBy(
                            fieldDefClassName.parameterizedBy(STAR, TYPE_VAR_F1),
                            fieldDefClassName.parameterizedBy(STAR, TYPE_VAR_F2),
                            fieldDefClassName.parameterizedBy(STAR, TYPE_VAR_F3),
                        )
                    )
                )
                .addParameter("fn", LambdaTypeName.get(tableWhereScopeClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val s = %T(this)
                    s(fn)
                    return %T(s).toTripleList(columnsFn)
                """.trimIndent(), tableWhereScopeClassName, finalWhereScopeClassName

                )
                .build()
        )


        //delete==========
        /**
         * fun PersonMapper.delete(
         *     fn: PersonWhereScope.() -> Unit
         * ): Int {
         *     val scope = PersonWhereScope(this)
         *     scope.invoke(fn)
         *     return FinalWhereScope(scope).toDelete()
         * }
         *
         */
        fileSpecBuilder.addFunction(
            FunSpec.builder("delete")
                .receiver(mapperClassName)
                .returns(INT_CLASS_NAME)
                .addParameter("fn", LambdaTypeName.get(tableWhereScopeClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val s = %T(this)
                    s(fn)
                    return %T(s).toDelete()
                """.trimIndent(), tableWhereScopeClassName, finalWhereScopeClassName
                )
                .build()
        )

        //update==========
        /**
         * fun FinalWhereScope<PersonWhereScope, PersonTableDef, Person>.update(fn: PersonUpdateScope.() -> Unit): Int {
         *     val w = PersonUpdateScope(this)
         *     w.invoke(fn)
         *     return whereScope.mapper.update(null, w.wrapper)
         * }
         *
         */
        fileSpecBuilder.addFunction(
            FunSpec.builder("update")
                .receiver(
                    finalWhereScopeClassName.parameterizedBy(
                        tableWhereScopeClassName,
                        tableDefInterfaceClassName,
                        entityClassName
                    )
                )
                .returns(INT_CLASS_NAME)
                .addParameter("fn", LambdaTypeName.get(tableUpdateScopeClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val s = %T(this)
                    s(fn)
                    return whereScope.mapper.update(null, s.wrapper)
                """.trimIndent(), tableUpdateScopeClassName
                )
                .build()
        )

        entityClass.getAllProperties()
            .firstOrNull {
                it.hasBackingField
                        && !it.isDelegated()
                        && it.annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == TABLE_ID_QUALIFIED_NAME }
            }?.let { idProp ->
                /**
                 * fun PersonMapper.updateById(
                 *     id: Int,
                 *     fn: PersonUpdateScope.() -> Unit
                 * ): Int {
                 *     val scope = PersonWhereScope(this)
                 *     return scope {
                 *         ID.eq(id)
                 *     }.update(fn)
                 * }
                 *
                 */
                val idSimpleName = idProp.simpleName.asString()
                fileSpecBuilder.addFunction(
                    FunSpec.builder("updateById")
                        .receiver(mapperClassName)
                        .returns(INT_CLASS_NAME)
                        .addParameter(idSimpleName, idProp.type.resolve().makeNotNullable().toClassName())
                        .addParameter("fn", LambdaTypeName.get(tableUpdateScopeClassName, emptyList(), UNIT_CLASS_NAME))
                        .addCode(
                            """
                            val s = %T(this)
                            return s {
                                %N.eq(%N)
                            }.update(fn)
                        """.trimIndent(),
                            tableWhereScopeClassName,
                            NamingConvention.CONSTANT_CASE.convert(idSimpleName),
                            idSimpleName,
                        )
                        .build()
                )
            }
    }

    private fun findEntityType(
        mapperDeclaration: KSClassDeclaration,
        typeArgMap: Map<String, KSType> = emptyMap()
    ): KSClassDeclaration? {
        if (!BASE_MAPPER_TYPE.isAssignableFrom(mapperDeclaration.asStarProjectedType())) {
            return null
        }
        return mapperDeclaration.superTypes.firstNotNullOf {
            val type = it.resolve()
            val newTypeArgumentArr = type.arguments.map { arg ->
                if (arg.type!!.resolve().declaration is KSClassDeclaration) {
                    arg
                } else {
                    globalResolver.getTypeArgument(
                        globalResolver.createKSTypeReferenceFromKSType(
                            typeArgMap[arg.type!!.resolve().declaration.simpleName.asString()]!!
                        ), Variance.INVARIANT
                    )
                }
            }
            findEntityType(type.replace(newTypeArgumentArr))
        }
    }

    private fun KSAnnotated.findAnnotation(name: String): KSAnnotation? {
        return annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == name
        }
    }

    private inline fun <reified T> KSAnnotation.getValue(name: String): T {
        return arguments.first { it.name?.asString() == name }.value as T
    }

    private fun findEntityType(ksType: KSType): KSClassDeclaration? {
        if (!BASE_MAPPER_TYPE.isAssignableFrom(ksType)) {
            return null
        }
        val classDeclaration = ksType.declaration as KSClassDeclaration
        if (classDeclaration == BASE_MAPPER_CLASS) {
            return ksType.arguments.first().type!!.resolve().declaration as KSClassDeclaration
        }
        return findEntityType(
            classDeclaration,
            ksType.arguments.mapIndexed { index, arg ->
                classDeclaration.typeParameters[index].name.asString() to arg.type!!.resolve()
            }.toMap()
        )
    }

    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return KspProcessor(environment)
        }
    }
}
