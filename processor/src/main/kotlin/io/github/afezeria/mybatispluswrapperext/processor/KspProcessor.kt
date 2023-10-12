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
import io.github.afezeria.mybatispluswrapperext.runtime.AbstractQueryWrapper
import io.github.afezeria.mybatispluswrapperext.runtime.AbstractUpdateWrapper
import io.github.afezeria.mybatispluswrapperext.runtime.FieldDefinition
import io.github.afezeria.mybatispluswrapperext.runtime.UpdateFieldDefinition


/**
 *
 * @date 2021/7/17
 */
lateinit var logger: KSPLogger

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
        ignoreMarkerAnnotation = environment.options[::ignoreMarkerAnnotation.name]
        dbNamingConvention = NamingConvention.valueOf(environment.options[::dbNamingConvention.name] ?: "FIELD_NAME")
    }

    lateinit var BASE_MAPPER_CLASS: KSClassDeclaration
    lateinit var BASE_MAPPER_TYPE: KSType

    lateinit var resolver: Resolver
    val processedMapperSet = mutableSetOf<String>()

    fun init(resolver: Resolver) {
        this.resolver = resolver
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

        val mapperClassName = mapper.toClassName()
        val entityClassName = entityClass.toClassName()
        val fileSpecBuilder =
            FileSpec.builder(mapper.packageName.asString(), mapper.simpleName.asString() + "Extensions")
        val queryExtensionClassName =
            addMapperWrapperClass(fileSpecBuilder, mapperClassName, entityClassName, entityClass, false)
        val updateExtensionClassName =
            addMapperWrapperClass(fileSpecBuilder, mapperClassName, entityClassName, entityClass, true)

        addExtensionMethod(
            fileSpecBuilder,
            mapperClassName,
            queryExtensionClassName,
            updateExtensionClassName,
            entityClassName,
            entityClass
        )

        val fileSpec = fileSpecBuilder.build()
        fileSpec.writeTo(
            environment.codeGenerator,
            Dependencies(false, mapper.containingFile!!, entityClass.containingFile!!)
        )
    }

    fun addExtensionMethod(
        fileSpecBuilder: FileSpec.Builder,
        mapperClassName: ClassName,
        queryExtensionClassName: ClassName,
        updateExtensionClassName: ClassName,
        entityClassName: ClassName,
        entityClass: KSClassDeclaration,
    ) {

        //query==========
        fileSpecBuilder.addFunction(
            FunSpec.builder("query")
                .receiver(mapperClassName)
                .returns(queryExtensionClassName)
                .addCode(
                    """
                    val w = %T(this)
                    return w
                """.trimIndent(), queryExtensionClassName
                )
                .build(),
        )

        fileSpecBuilder.addFunction(
            FunSpec.builder("queryList")
                .receiver(mapperClassName)
                .returns(List::class.asClassName().parameterizedBy(entityClassName))
                .addParameter("fn", LambdaTypeName.get(queryExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val w = %T(this)
                    fn(w)
                    return w.toList()
                """.trimIndent(), queryExtensionClassName
                )
                .build()
        )

        fileSpecBuilder.addFunction(
            FunSpec.builder("querySingleField")
                .addTypeVariable(TYPE_VAR_F1)
                .receiver(mapperClassName)
                .returns(List::class.asClassName().parameterizedBy(TYPE_VAR_F1))
                .addParameter(
                    "columnFn",
                    LambdaTypeName.get(
                        queryExtensionClassName,
                        emptyList(),
                        FieldDefinition::class.asTypeName().parameterizedBy(queryExtensionClassName, STAR, TYPE_VAR_F1)
                    )
                )
                .addParameter("fn", LambdaTypeName.get(queryExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val w = %T(this)
                    fn(w)
                    return w.toSingleFieldList(columnFn)
                """.trimIndent(), queryExtensionClassName
                )
                .build()
        )

        fileSpecBuilder.addFunction(
            FunSpec.builder("queryPair")
                .addTypeVariable(TYPE_VAR_F1)
                .addTypeVariable(TYPE_VAR_F2)
                .receiver(mapperClassName)
                .returns(LIST_CLASS_NAME.parameterizedBy(PAIR_CLASS_NAME.parameterizedBy(TYPE_VAR_F1, TYPE_VAR_F2)))
                .addParameter(
                    "columnsFn",
                    LambdaTypeName.get(
                        queryExtensionClassName,
                        emptyList(),
                        PAIR_CLASS_NAME.parameterizedBy(
                            FIELD_DEFINITION_CLASS_NAME.parameterizedBy(queryExtensionClassName, STAR, TYPE_VAR_F1),
                            FIELD_DEFINITION_CLASS_NAME.parameterizedBy(queryExtensionClassName, STAR, TYPE_VAR_F2),
                        )
                    )
                )
                .addParameter("fn", LambdaTypeName.get(queryExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val w = %T(this)
                    fn(w)
                    return w.toPairList(columnsFn)
                """.trimIndent(), queryExtensionClassName
                )
                .build()
        )

        fileSpecBuilder.addFunction(
            FunSpec.builder("queryTriple")
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
                        queryExtensionClassName,
                        emptyList(),
                        TRIPLE_CLASS_NAME.parameterizedBy(
                            FIELD_DEFINITION_CLASS_NAME.parameterizedBy(queryExtensionClassName, STAR, TYPE_VAR_F1),
                            FIELD_DEFINITION_CLASS_NAME.parameterizedBy(queryExtensionClassName, STAR, TYPE_VAR_F2),
                            FIELD_DEFINITION_CLASS_NAME.parameterizedBy(queryExtensionClassName, STAR, TYPE_VAR_F3),
                        )
                    )
                )
                .addParameter("fn", LambdaTypeName.get(queryExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val w = %T(this)
                    fn(w)
                    return w.toTripleList(columnsFn)
                """.trimIndent(), queryExtensionClassName
                )
                .build()
        )

        val pageType = TypeVariableName("P", I_PAGE_CLASS_NAME.parameterizedBy(entityClassName))
        fileSpecBuilder.addFunction(
            FunSpec.builder("queryPage")
                .addTypeVariable(pageType)
                .receiver(mapperClassName)
                .returns(pageType)
                .addParameter("page", pageType)
                .addParameter("fn", LambdaTypeName.get(queryExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val w = %T(this)
                    fn(w)
                    return w.toPage(page)
                """.trimIndent(), queryExtensionClassName
                )
                .build()
        )
        fileSpecBuilder.addFunction(
            FunSpec.builder("queryOne")
                .receiver(mapperClassName)
                .returns(entityClassName.copy(nullable = true))
                .addParameter("fn", LambdaTypeName.get(queryExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val w = %T(this)
                    fn(w)
                    return w.toOne()
                """.trimIndent(), queryExtensionClassName
                )
                .build()
        )
        fileSpecBuilder.addFunction(
            FunSpec.builder("queryCount")
                .receiver(mapperClassName)
                .returns(LONG_CLASS_NAME)
                .addParameter("fn", LambdaTypeName.get(queryExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val w = %T(this)
                    fn(w)
                    return w.toCount()
                """.trimIndent(), queryExtensionClassName
                )
                .build()
        )

        //delete==========
        fileSpecBuilder.addFunction(
            FunSpec.builder("delete")
                .receiver(mapperClassName)
                .returns(queryExtensionClassName)
                .addCode(
                    """
                    val w = %T(this)
                    return w
                """.trimIndent(), queryExtensionClassName
                )
                .build(),
        )

        fileSpecBuilder.addFunction(
            FunSpec.builder("delete")
                .receiver(mapperClassName)
                .returns(INT_CLASS_NAME)
                .addParameter("fn", LambdaTypeName.get(queryExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val w = %T(this)
                    fn(w)
                    return w.delete()
                """.trimIndent(), queryExtensionClassName
                )
                .build()
        )

        //update==========
        fileSpecBuilder.addFunction(
            FunSpec.builder("update")
                .receiver(mapperClassName)
                .returns(updateExtensionClassName)
                .addCode(
                    """
                    val w = %T(this)
                    return w
                """.trimIndent(), updateExtensionClassName
                )
                .build(),
        )
        fileSpecBuilder.addFunction(
            FunSpec.builder("update")
                .receiver(mapperClassName)
                .returns(INT_CLASS_NAME)
                .addParameter("fn", LambdaTypeName.get(updateExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    val w = %T(this)
                    fn(w)
                    return w.update()
                """.trimIndent(), updateExtensionClassName
                )
                .build()
        )
        entityClass.getAllProperties()
            .firstOrNull {
                it.hasBackingField
                        && !it.isDelegated()
                        && it.annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == TABLE_ID_QUALIFIED_NAME }
            }?.let { idProp ->
                val idSimpleName = idProp.simpleName.asString()
                fileSpecBuilder.addFunction(
                    FunSpec.builder("updateById")
                        .receiver(mapperClassName)
                        .returns(INT_CLASS_NAME)
                        .addParameter(idSimpleName, idProp.type.resolve().makeNotNullable().toClassName())
                        .addParameter("fn", LambdaTypeName.get(updateExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                        .addCode(
                            """
                            val wrapper = %T(this).apply(fn)
                                .%N.eq(%N)
                                .wrapper
                            return this.update(null, wrapper)
                        """.trimIndent(),
                            updateExtensionClassName,
                            NamingConvention.CONSTANT_CASE.convert(idSimpleName),
                            idSimpleName,
                        )
                        .build()
                )
            }
    }


    private fun addMapperWrapperClass(
        fileSpecBuilder: FileSpec.Builder,
        mapperClassName: ClassName,
        entityClassName: ClassName,
        entityClass: KSClassDeclaration,
        isUpdateExtension: Boolean,
    ): ClassName {
        val extensionClassName: ClassName
        val abstractMapperExtension: ClassName
        val fieldDefinitionClassName: ClassName
        if (isUpdateExtension) {
            extensionClassName = ClassName(
                mapperClassName.packageName,
                "${mapperClassName.simpleName}UpdateWrapper"
            )
            abstractMapperExtension = ABSTRACT_UPDATE_WRAPPER_CLASS_NAME
            fieldDefinitionClassName = UPDATE_FIELD_DEFINITION_CLASS_NAME
        } else {
            extensionClassName = ClassName(
                mapperClassName.packageName,
                "${mapperClassName.simpleName}QueryWrapper"
            )
            abstractMapperExtension = ABSTRACT_QUERY_WRAPPER_CLASS_NAME
            fieldDefinitionClassName = FIELD_DEFINITION_CLASS_NAME
        }

        val classBuilder = TypeSpec.classBuilder(extensionClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(EXTENSION_CONSTRUCTOR_PARAMETER_NAME, mapperClassName)
                    .build()
            )
            .superclass(
                abstractMapperExtension.parameterizedBy(
                    extensionClassName,
                    mapperClassName,
                    entityClassName,
                )
            )
            .addSuperclassConstructorParameter(EXTENSION_CONSTRUCTOR_PARAMETER_NAME)

        val props = entityClass.getAllProperties()
            .filter { it.hasBackingField && !it.isDelegated() }
            .toList()

        for (property in props) {
            val tableField = property.findAnnotation(TABLE_FIELD_QUALIFIED_NAME)
            val tableId = property.findAnnotation(TABLE_ID_QUALIFIED_NAME)
            if (tableField?.getValue<Boolean>("exist") == false) {
                continue
            }
            val dbFieldName: String = tableId?.getValue("value")
                ?: tableField?.getValue("value")
                ?: dbNamingConvention.convert(property.simpleName.asString())

            val doc = if (property.docString.isNullOrBlank()) {
                ""
            } else {
                property.docString!!.trim() + "\n\n"
            } + "@see ${property.qualifiedName?.asString()}"
            classBuilder.addProperty(
                PropertySpec.builder(
                    NamingConvention.CONSTANT_CASE.convert(property.simpleName.asString()),
                    fieldDefinitionClassName.parameterizedBy(
                        extensionClassName,
                        property.type.resolve().makeNotNullable().toTypeName(),
                        property.type.resolve().toTypeName()
                    )
                ).addKdoc("%L", doc)
                    .initializer(
                        "%T(%S, this)",
                        fieldDefinitionClassName,
                        dbFieldName
                    ).build()
            )
        }
        fileSpecBuilder.addType(classBuilder.build())
        return extensionClassName
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
                    resolver.getTypeArgument(
                        resolver.createKSTypeReferenceFromKSType(
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

    companion object {
        val UNIT_CLASS_NAME = Unit::class.asClassName()
        val INT_CLASS_NAME = Int::class.asClassName()
        val LONG_CLASS_NAME = Long::class.asClassName()
        val LIST_CLASS_NAME = List::class.asClassName()
        val PAIR_CLASS_NAME = Pair::class.asClassName()
        val TRIPLE_CLASS_NAME = Triple::class.asClassName()

        val FIELD_DEFINITION_CLASS_NAME = FieldDefinition::class.asClassName()
        val UPDATE_FIELD_DEFINITION_CLASS_NAME = UpdateFieldDefinition::class.asClassName()
        val ABSTRACT_QUERY_WRAPPER_CLASS_NAME = AbstractQueryWrapper::class.asClassName()
        val ABSTRACT_UPDATE_WRAPPER_CLASS_NAME = AbstractUpdateWrapper::class.asClassName()

        val TYPE_VAR_F1 = TypeVariableName("F1")
        val TYPE_VAR_F2 = TypeVariableName("F2")
        val TYPE_VAR_F3 = TypeVariableName("F3")

        const val TABLE_FIELD_QUALIFIED_NAME = "com.baomidou.mybatisplus.annotation.TableField"
        const val TABLE_ID_QUALIFIED_NAME = "com.baomidou.mybatisplus.annotation.TableId"
        val BASE_MAPPER_CLASS_NAME = ClassName("com.baomidou.mybatisplus.core.mapper", "BaseMapper")
        val I_PAGE_CLASS_NAME = ClassName("com.baomidou.mybatisplus.core.metadata", "IPage")
        const val EXTENSION_CONSTRUCTOR_PARAMETER_NAME = "mapper"

    }
}

fun main() {
    val a = List::class.asClassName()
    val b = a.parameterizedBy(Int::class.asTypeName())
    println(a)
    println(b)
}