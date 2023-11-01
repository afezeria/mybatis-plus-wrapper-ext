package io.github.afezeria.mybatispluswrapperext.processor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.ClassName
import org.intellij.lang.annotations.Language

/**
 *
 * @author afezeria
 */
class PackageLevelApiGenerateHandler {
    val generatedPackageLevelApiList = mutableListOf<String>()

    lateinit var currentPackageName: String
    val currentFieldDefClassName get() = ClassName(currentPackageName, "FieldDef")
    val currentWhereScopeClassName get() = ClassName(currentPackageName, "WhereScope")
    val currentFinalWhereScopeClassName get() = ClassName(currentPackageName, "FinalWhereScope")
    val currentUpdateScopeClassName get() = ClassName(currentPackageName, "UpdateScope")

    fun generate(packageName: String) {
        currentPackageName = packageName
        if (packageName in generatedPackageLevelApiList) {
            return
        }
        val code = """
package $packageName

$imports

$fieldDefCode

${generateWhereScopeCode()}

${generateFinalWhereScopeCode()}

$updateScopeCode
        """.trimIndent()
            .toByteArray()
        globalEnvironment.codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = packageName,
            fileName = "MybatisPlusPackageExtensionApi",
            extensionName = "kt"
        ).use {
            it.write(code)
        }
    }

    val imports = """
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.core.toolkit.Wrappers
    """.trimIndent()

    fun generateWhereScopeCode(): String {
        val booleanDeclaration = globalResolver.getClassDeclarationByName("kotlin.Boolean")!!
        val anyDeclaration = globalResolver.getClassDeclarationByName("kotlin.Any")!!

        val compareClassDeclaration =
            globalResolver.getClassDeclarationByName("com.baomidou.mybatisplus.core.conditions.interfaces.Compare")!!
        val compareTypeVarR = compareClassDeclaration.typeParameters.last()
        val compareOperators = compareClassDeclaration.getDeclaredFunctions()
            .filter {
                it.parameters[0].type.resolve().declaration == booleanDeclaration
                        && it.parameters[1].type.resolve().declaration == compareTypeVarR
            }
            .mapNotNull {
                val name = it.simpleName.asString()
                val count = it.parameters.count { it.type.resolve().declaration == anyDeclaration }
                when (count) {
                    1 -> {
                        """
    fun <T> FieldDef<T, *>.$name(value: T) {
        wrapper.$name(true, name, value)
    }

    fun <T> FieldDef<T, *>.${name}IfNotNull(value: T?) {
        if (value != null) {
            wrapper.$name(true, name, value)
        }
    }

    fun <T> FieldDef<T, *>.$name(condition: Boolean, valueFn: () -> T) {
        if (condition) {
            wrapper.$name(true, name, valueFn())
        }
    }
"""
                    }

                    2 -> {
                        """
    fun <T> FieldDef<T, *>.$name(value1: T, value2: T) {
        wrapper.$name(true, name, value1, value2)
    }

    fun <T> FieldDef<T, *>.$name(condition: Boolean, valueFn: () -> Pair<T, T>) {
        if (condition) {
            val (v1, v2) = valueFn()
            wrapper.$name(true, name, v1, v2)
        }
    }
"""
                    }

                    else -> {
                        null
                    }
                }
            }.joinToString(separator = "")
        val nestedClassDeclaration =
            globalResolver.getClassDeclarationByName("com.baomidou.mybatisplus.core.conditions.interfaces.Nested")!!
        val nestedTypeVarParam = nestedClassDeclaration.typeParameters.first()
        val consumerDeclaration = globalResolver.getClassDeclarationByName("java.util.function.Consumer")!!
        val nestedOperators = nestedClassDeclaration.getDeclaredFunctions()
            .filter {
                if (it.parameters.size != 2) {
                    false
                } else {
                    val secondParamType = it.parameters[1].type.resolve()
                    it.parameters.first().type.resolve().declaration == booleanDeclaration
                            && secondParamType.declaration == consumerDeclaration
                            && secondParamType.arguments.first().type!!.resolve().declaration == nestedTypeVarParam
                }
            }.map {
                val name = it.simpleName.asString()

                """
    fun $name(fn: S.() -> Unit) {
        wrapper.$name(true) {
            val current = wrapper
            @Suppress("UNCHECKED_CAST")
            wrapper = it as AbstractWrapper<*, String, *>
            fn(self)
            wrapper = current
        }
    }

    fun $name(condition: Boolean, fn: S.() -> Unit) {
        wrapper.$name(condition) {
            val current = wrapper
            @Suppress("UNCHECKED_CAST")
            wrapper = it as AbstractWrapper<*, String, *>
            fn(self)
            wrapper = current
        }
    }
"""
            }.joinToString(separator = "")


        @Language("kotlin")
        val whereScopeCode = """
abstract class WhereScope<S : WhereScope<S, TD, T>, TD, T>(
    val mapper: BaseMapper<T>,
    val tableDef: TD
) {

    val expressions: MutableList<S.() -> Unit> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    val self: S = this as S

    lateinit var wrapper: AbstractWrapper<*, String, *>
    
$compareOperators
$nestedOperators

    fun or() {
        wrapper.or()
    }

    fun <T> FieldDef<T, *>.isNull() {
        wrapper.isNull(true, name)
    }

    fun <T> FieldDef<T, *>.isNull(condition: Boolean) {
        wrapper.isNull(condition, name)
    }

    fun <T> FieldDef<T, *>.isNotNull() {
        wrapper.isNotNull(true, name)
    }

    fun <T> FieldDef<T, *>.isNotNull(condition: Boolean) {
        wrapper.isNotNull(condition, name)
    }

    fun <T> FieldDef<T, *>.`in`(value: Collection<T?>) {
        wrapper.`in`(true, name, value)
    }

    fun <T> FieldDef<T, *>.`in`(condition: Boolean, value: Collection<T?>) {
        wrapper.`in`(condition, name, value)
    }

    fun <T> FieldDef<T, *>.`in`(condition: Boolean, valueFn: () -> Collection<T?>) {
        if (condition) {
            wrapper.`in`(true, name, valueFn())
        }
    }

    fun <T> FieldDef<T, *>.`in`(vararg value: T?) {
        wrapper.`in`(true, name, *value)
    }

    fun <T> FieldDef<T, *>.`in`(condition: Boolean, vararg value: T?) {
        wrapper.`in`(condition, name, *value)
    }

    fun <T> FieldDef<T, *>.notIn(value: Collection<T?>) {
        wrapper.notIn(true, name, value)
    }

    fun <T> FieldDef<T, *>.notIn(condition: Boolean, value: Collection<T?>) {
        wrapper.notIn(condition, name, value)
    }

    fun <T> FieldDef<T, *>.notIn(condition: Boolean, valueFn: () -> Collection<T?>) {
        if (condition) {
            wrapper.notIn(true, name, valueFn())
        }
    }

    fun <T> FieldDef<T, *>.notIn(vararg value: T?) {
        wrapper.notIn(true, name, *value)
    }

    fun <T> FieldDef<T, *>.notIn(condition: Boolean, vararg value: T?) {
        wrapper.notIn(condition, name, *value)
    }

    operator fun invoke(fn: S.() -> Unit): FinalWhereScope<S, TD, T> {
        expressions.add(fn)
        return FinalWhereScope(self)
    }
}
    """.trimIndent()
        return whereScopeCode
    }


    @Language("kotlin")
    val fieldDefCode = """
class FieldDef<T, VT>(
    val name: String,
) {
    val trimName = name.trim('`', '"', '\'')

    @Suppress("UNCHECKED_CAST")
    val markNotNull: FieldDef<T, VT & Any> = this as FieldDef<T, VT & Any>
}
""".trimIndent()

    @Language("kotlin")
    val updateScopeCode = """
abstract class UpdateScope<S : UpdateScope<S, TD, T>, TD, T>(
    val wrapper: UpdateWrapper<T>,
    val tableDef: TD,
) {
    @Suppress("UNCHECKED_CAST")
    val self = this as S

    fun <T> FieldDef<T, *>.set(value: T) {
        wrapper.set(name, value)
    }

    fun <T> FieldDef<T, *>.setNull() {
        wrapper.set(name, null)
    }

    fun <T> FieldDef<T, *>.setNull(condition: Boolean) {
        if (condition) {
            wrapper.set(name, null)
        }
    }

    fun <T> FieldDef<T, *>.set(condition: Boolean, value: () -> T) {
        if (condition) {
            wrapper.set(name, value())
        }
    }

    operator fun invoke(fn: S.() -> Unit): S {
        fn(self)
        return self
    }
}
    """.trimIndent()

    var hasExistsMethod = false

    fun generateFinalWhereScopeCode(): String {
        val baseMapperDeclaration =
            globalResolver.getClassDeclarationByName("com.baomidou.mybatisplus.core.mapper.BaseMapper")!!
        val longDeclaration = globalResolver.getClassDeclarationByName("kotlin.Long")!!
        val selectCount = baseMapperDeclaration.getDeclaredFunctions()
            .first { it.simpleName.asString() == "selectCount" }
        val toCountMethodBody = if (selectCount.returnType!!.resolve().declaration == longDeclaration) {
            "whereScope.mapper.selectCount(getQueryWrapper())"
        } else {
            "whereScope.mapper.selectCount(getQueryWrapper()).toLong()"
        }
        val existsMethod = baseMapperDeclaration.getDeclaredFunctions()
            .firstOrNull { it.simpleName.asString() == "exists" }
        val existsMethodBody = if (existsMethod != null) {
            hasExistsMethod = true
            ""
        } else {
            """
    fun exists(): Boolean {
        return whereScope.mapper.exists(getQueryWrapper())
    }
""".trimIndent()
        }

        @Language("kotlin")
        val finalWhereScopeCode = """
class FinalWhereScope<S : WhereScope<S, TD, T>, TD, T>(
    val whereScope: WhereScope<S, TD, T>
) {

    fun toList(): List<T> {
        return whereScope.mapper.selectList(getQueryWrapper())
    }

    fun toOne(): T? {
        return whereScope.mapper.selectOne(getQueryWrapper())
    }

    fun toCount(): Long {
        return $toCountMethodBody
    }

$existsMethodBody

    fun <P : IPage<T>> toPage(page: P): P {
        return whereScope.mapper.selectPage(page, getQueryWrapper())
    }

    fun <F> toSingleFieldList(columnFn: TD.() -> FieldDef<*, F>): List<F> {
        val column = columnFn(whereScope.tableDef)
        val w = getQueryWrapper()
        w.select(column.name)
        return whereScope.mapper.selectMaps(w).map {
            @Suppress("UNCHECKED_CAST")
            it?.get(column.trimName) as F
        }
    }

    fun <F1, F2> toPairList(columnsFn: TD.() -> Pair<FieldDef<*, F1>, FieldDef<*, F2>>): List<Pair<F1, F2>> {
        val columns = columnsFn(whereScope.tableDef)
        val w = getQueryWrapper()
        w.select(columns.first.name, columns.second.name)
        return whereScope.mapper.selectMaps(w).map {
            @Suppress("UNCHECKED_CAST")
            Pair(
                it?.get(columns.first.trimName) as F1,
                it?.get(columns.second.trimName) as F2,
            )
        }
    }

    fun <F1, F2, F3> toTripleList(
        columnsFn: TD.() -> Triple<FieldDef<*, F1>, FieldDef<*, F2>, FieldDef<*, F3>>
    ): List<Triple<F1, F2, F3>> {
        val columns = columnsFn(whereScope.tableDef)
        val w = getQueryWrapper()
        w.select(columns.first.name, columns.second.name, columns.third.name)
        @Suppress("UNCHECKED_CAST")
        return whereScope.mapper.selectMaps(w).map {
            Triple(
                it?.get(columns.first.trimName) as F1,
                it?.get(columns.second.trimName) as F2,
                it?.get(columns.third.trimName) as F3
            )
        }
    }

    fun toDelete(): Int {
        return whereScope.mapper.delete(getQueryWrapper())
    }

    fun getQueryWrapper(): QueryWrapper<T> {
        val w = Wrappers.query<T>()
        whereScope.wrapper = w
        whereScope.expressions.forEach {
            it.invoke(whereScope.self)
        }
        return w
    }

    fun getUpdateWrapper(): UpdateWrapper<T> {
        val w = Wrappers.update<T>()
        whereScope.wrapper = w
        whereScope.expressions.forEach {
            it.invoke(whereScope.self)
        }
        return w
    }
}
    """.trimIndent()
        return finalWhereScopeCode
    }

}
