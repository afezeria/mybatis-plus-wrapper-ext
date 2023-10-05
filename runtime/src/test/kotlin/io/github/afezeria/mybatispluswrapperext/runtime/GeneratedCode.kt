package io.github.afezeria.mybatispluswrapperext.runtime

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import java.lang.reflect.Proxy

/**
 *
 * @author afezeria
 */

val mapper = Proxy.newProxyInstance(
    PersonMapper::class.java.classLoader, arrayOf(PersonMapper::class.java)
) { _, _, _ ->
    null
} as PersonMapper

class PersonMapperQueryExtension(
    mapper: PersonMapper,
) : AbstractMapperQueryExtension<PersonMapperQueryExtension, PersonMapper, Person>(mapper) {

    val ID = FieldDefinition<PersonMapperQueryExtension, Int>("id", this)

    val NAME: FieldDefinition<PersonMapperQueryExtension, String>
        get() = FieldDefinition("name", this)

    val AGE: FieldDefinition<PersonMapperQueryExtension, Int>
        get() = FieldDefinition("age", this)
}


fun PersonMapper.query(): PersonMapperQueryExtension {
    return PersonMapperQueryExtension(this)
}


fun PersonMapper.queryList(fn: PersonMapperQueryExtension.() -> Unit): List<Person> {
    return PersonMapperQueryExtension(this).apply {
        fn(this)
    }.toList()
}

fun PersonMapper.queryOne(fn: PersonMapperQueryExtension.() -> Unit): Person? {
    return PersonMapperQueryExtension(this).apply {
        fn(this)
    }.toOne()
}

fun PersonMapper.queryCount(fn: PersonMapperQueryExtension.() -> Unit): Long {
    return PersonMapperQueryExtension(this).apply {
        fn(this)
    }.toCount()
}

class PersonMapperUpdateExtension(
    mapper: PersonMapper,
) : AbstractMapperUpdateExtension<PersonMapperUpdateExtension, PersonMapper, Person>(mapper) {

    val ID = UpdateFieldDefinition<PersonMapperUpdateExtension, Int>("id", this)

    val NAME = UpdateFieldDefinition<PersonMapperUpdateExtension, String>("name", this)

    val AGE = UpdateFieldDefinition<PersonMapperUpdateExtension, Int>("age", this)
}

fun PersonMapper.update(): PersonMapperUpdateExtension {
    return PersonMapperUpdateExtension(this)
}

fun PersonMapper.updateById(id: Int, fn: PersonMapperUpdateExtension.() -> Unit): Int {
    val wrapper = PersonMapperUpdateExtension(this).apply(fn)
        .ID.eq(id)
        .wrapper
    return this.update(null, wrapper)
}

fun main() {
    generateAssert()
//    println(
//        mapper.update()
//            .NAME.set("abc")
//            .AGE.set(2)
//            .ID.eq(2)
//            .wrapper.sqlSet
//    )
//    println(
//        Wrappers.update<Person>()
//            .set("name", "abc")
//            .set("age",2)
//            .eq("id", 1)
//            .targetSql
//    )

}

fun generateAssert() {
    FieldDefinition::class.java
        .declaredMethods
        .filter { !it.name.startsWith("get") }
        .forEach {
            val name = if (it.name == "in") "`in`" else it.name
            if (it.parameterCount == 1) {
                println("extSql { ID.${name}(1) } shouldBeEqual oriSql { $name(Person::id.name, 1) }")
            } else if (it.parameterCount == 2) {
                if (it.parameters.first().type == Boolean::class.java) {
                    if (it.parameters.last().type == Function0::class.java) {
                        println("extSql { ID.$name(true) { 1 } } shouldBeEqual oriSql { if (true) { $name(Person::id.name, 1) } }")
                        println("extSql { ID.$name(false) { 1 } } shouldBeEqual oriSql { if (false) { $name(Person::id.name, 1) } }")
                    } else {
                        println("extSql { ID.$name(true, 1) } shouldBeEqual oriSql { if (true) { $name(Person::id.name, 1) } }")
                        println("extSql { ID.$name(false, 1) } shouldBeEqual oriSql { if (false) { $name(Person::id.name, 1) } }")
                    }
                } else {
                    println("extSql { ID.${name}(1, 1) } shouldBeEqual oriSql { $name(Person::id.name, 1, 1) }")
                }
            } else {
                println("extSql { ID.$name(true, 1, 1) } shouldBeEqual oriSql { if (true) { $name(Person::id.name, 1, 1) } }")
            }
        }
}

fun extSql(fn: PersonMapperQueryExtension.() -> Unit): String {
    return mapper.query().apply {
        fn(this)
    }.wrapper.customSqlSegment.also {
        println("ext sql:$it")
    }
}

fun oriSql(fn: QueryWrapper<Person>.() -> Unit): String {
    return Wrappers.query<Person>().apply {
        fn(this)
    }.customSqlSegment.also {
        println("ori sql:$it")
    }
}
