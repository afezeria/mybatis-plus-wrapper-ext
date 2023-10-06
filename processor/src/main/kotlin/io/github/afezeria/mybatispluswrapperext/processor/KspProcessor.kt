package io.github.afezeria.mybatispluswrapperext.processor

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.afezeria.mybatispluswrapperext.runtime.AbstractMapperQueryExtension
import io.github.afezeria.mybatispluswrapperext.runtime.AbstractMapperUpdateExtension
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
        BASE_MAPPER_CLASS = resolver.getClassDeclarationByName(BaseMapper::class.qualifiedName!!)!!
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
            addMapperExtensionClass(fileSpecBuilder, mapperClassName, entityClassName, entityClass, false)
        val updateExtensionClassName =
            addMapperExtensionClass(fileSpecBuilder, mapperClassName, entityClassName, entityClass, true)

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
                .addStatement("return %T(this)", queryExtensionClassName)
                .build(),
        )

        fileSpecBuilder.addFunction(
            FunSpec.builder("queryList")
                .receiver(mapperClassName)
                .returns(List::class.asClassName().parameterizedBy(entityClassName))
                .addParameter("fn", LambdaTypeName.get(queryExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    return %T(this).apply {
                        fn(this)
                    }.toList()
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
                    return %T(this).apply {
                        fn(this)
                    }.toOne()
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
                    return %T(this).apply {
                        fn(this)
                    }.toCount()
                """.trimIndent(), queryExtensionClassName
                )
                .build()
        )
        fileSpecBuilder.addFunction(
            FunSpec.builder("delete")
                .receiver(mapperClassName)
                .returns(queryExtensionClassName)
                .addStatement("return %T(this)", queryExtensionClassName)
                .build(),
        )

        fileSpecBuilder.addFunction(
            FunSpec.builder("delete")
                .receiver(mapperClassName)
                .returns(INT_CLASS_NAME)
                .addParameter("fn", LambdaTypeName.get(queryExtensionClassName, emptyList(), UNIT_CLASS_NAME))
                .addCode(
                    """
                    return %T(this).apply {
                        fn(this)
                    }.delete()
                """.trimIndent(), queryExtensionClassName
                )
                .build()
        )

        //update==========
        fileSpecBuilder.addFunction(
            FunSpec.builder("update")
                .receiver(mapperClassName)
                .returns(updateExtensionClassName)
                .addStatement("return %T(this)", updateExtensionClassName)
                .build(),
        )
        entityClass.getAllProperties()
            .firstOrNull {
                it.hasBackingField
                        && !it.isDelegated()
                        && it.isAnnotationPresent(TableId::class)
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


    private fun addMapperExtensionClass(
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
                "${mapperClassName.simpleName}UpdateExtension"
            )
            abstractMapperExtension = AbstractMapperUpdateExtension::class.asClassName()
            fieldDefinitionClassName = UpdateFieldDefinition::class.asClassName()
        } else {
            extensionClassName = ClassName(
                mapperClassName.packageName,
                "${mapperClassName.simpleName}QueryExtension"
            )
            abstractMapperExtension = AbstractMapperQueryExtension::class.asClassName()
            fieldDefinitionClassName = FieldDefinition::class.asClassName()
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
            val tableField = property.getAnnotationsByType(TableField::class).firstOrNull()
            val tableId = property.getAnnotationsByType(TableId::class).firstOrNull()
            if (tableField?.exist == false) {
                continue
            }
            val dbFieldName = tableId?.value
                ?: tableField?.value
                ?: dbNamingConvention.convert(property.simpleName.asString())
            classBuilder.addProperty(
                PropertySpec.builder(
                    NamingConvention.CONSTANT_CASE.convert(property.simpleName.asString()),
                    fieldDefinitionClassName.parameterizedBy(
                        extensionClassName,
                        property.type.resolve().toClassName(),
                    )
                ).initializer(
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
        const val EXTENSION_CONSTRUCTOR_PARAMETER_NAME = "mapper"

    }
}

fun main() {
    println(
        """
                        return %T(this).apply {
                            fn(this)
                        }.toCount()
                    """.trimIndent()
    )
}