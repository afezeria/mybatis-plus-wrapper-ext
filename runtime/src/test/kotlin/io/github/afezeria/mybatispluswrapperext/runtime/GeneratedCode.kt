package io.github.afezeria.mybatispluswrapperext.runtime

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.metadata.IPage
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

class PersonMapperQueryWrapper(
    mapper: PersonMapper,
) : AbstractQueryWrapper<PersonMapperQueryWrapper, PersonMapper, Person>(mapper) {


    /**
     *  person name
     *
     * @see io.github.afezeria.mybatispluswrapperext.processor.mapper.Person.name
     */
    val ID = FieldDefinition<PersonMapperQueryWrapper, Int, Int?>("id", this)

    val NAME: FieldDefinition<PersonMapperQueryWrapper, String, String?>
        get() = FieldDefinition("name", this)

    val AGE: FieldDefinition<PersonMapperQueryWrapper, Int, Int?>
        get() = FieldDefinition("age", this)
}


fun PersonMapper.query(): PersonMapperQueryWrapper {
    return PersonMapperQueryWrapper(this)
}


fun PersonMapper.queryList(fn: PersonMapperQueryWrapper.() -> Unit): List<Person> {
    return PersonMapperQueryWrapper(this).apply {
        fn(this)
    }.toList()
}

fun <F> PersonMapper.querySingleField(
    columnFn: PersonMapperQueryWrapper.() -> FieldDefinition<PersonMapperQueryWrapper, *, F>,
    fn: PersonMapperQueryWrapper.() -> Unit
): List<F> {
    val w = PersonMapperQueryWrapper(this)
    fn(w)
    return w.toSingleFieldList(columnFn)

}

fun <F1, F2> PersonMapper.queryPair(
    columnsFn: PersonMapperQueryWrapper.() -> Pair<FieldDefinition<PersonMapperQueryWrapper, *, F1>, FieldDefinition<PersonMapperQueryWrapper, *, F2>>,
    fn: PersonMapperQueryWrapper.() -> Unit
): List<Pair<F1, F2>> {
    val w = PersonMapperQueryWrapper(this)
    fn(w)
    return w.toPairList(columnsFn)
}

fun <F1, F2, F3> PersonMapper.queryTriple(
    columnsFn: PersonMapperQueryWrapper.() -> Triple<FieldDefinition<PersonMapperQueryWrapper, *, F1>, FieldDefinition<PersonMapperQueryWrapper, *, F2>, FieldDefinition<PersonMapperQueryWrapper, *, F3>>,
    fn: PersonMapperQueryWrapper.() -> Unit
): List<Triple<F1, F2, F3>> {
    val w = PersonMapperQueryWrapper(this)
    fn(w)
    return w.toTripleList(columnsFn)
}

fun <P : IPage<Person>> PersonMapper.queryPage(
    page: P,
    fn: PersonMapperQueryWrapper.() -> Unit
): P {
    return PersonMapperQueryWrapper(this).apply {
        fn(this)
    }.toPage(page)
}

fun PersonMapper.queryOne(fn: PersonMapperQueryWrapper.() -> Unit): Person? {
    return PersonMapperQueryWrapper(this).apply {
        fn(this)
    }.toOne()
}

fun PersonMapper.queryCount(fn: PersonMapperQueryWrapper.() -> Unit): Long {
    return PersonMapperQueryWrapper(this).apply {
        fn(this)
    }.toCount()
}

fun PersonMapper.delete(): PersonMapperQueryWrapper {
    return PersonMapperQueryWrapper(this)
}

fun PersonMapper.delete(fn: PersonMapperQueryWrapper.() -> Unit): Int {
    return PersonMapperQueryWrapper(this).apply {
        fn(this)
    }.delete()
}

class PersonMapperUpdateWrapper(
    mapper: PersonMapper,
) : AbstractUpdateWrapper<PersonMapperUpdateWrapper, PersonMapper, Person>(mapper) {

    val ID = UpdateFieldDefinition<PersonMapperUpdateWrapper, Int, Int?>("id", this)

    val NAME = UpdateFieldDefinition<PersonMapperUpdateWrapper, String, String?>("name", this)

    val AGE = UpdateFieldDefinition<PersonMapperUpdateWrapper, Int, Int?>("age", this)
}

fun PersonMapper.update(): PersonMapperUpdateWrapper {
    return PersonMapperUpdateWrapper(this)
}

fun PersonMapper.update(fn: PersonMapperUpdateWrapper.() -> Unit): Int {
    return PersonMapperUpdateWrapper(this).apply {
        fn(this)
    }.update()
}

fun PersonMapper.updateById(id: Int, fn: PersonMapperUpdateWrapper.() -> Unit): Int {
    val wrapper = PersonMapperUpdateWrapper(this).apply(fn)
        .ID.eq(id)
        .wrapper
    return this.update(null, wrapper)
}

fun main() {
    generateAssert()
    val map = mapper.selectMaps(
        Wrappers.query<Person?>()
            .select("`name`")
            .isNotNull("`name`")
    ).map { it?.get("name") as String }
    val querySingleField = mapper.querySingleField({ NAME.markNotNull }) {
        NAME.isNotNull()
    }

    val b = mapper.selectMaps(
        Wrappers.query<Person?>()
            .select("`name`", "age")
            .gt("id", 1)
    ).map {
        Pair(it?.get("name") as String?, it?.get("age") as Int?)
    }
    mapper.queryPair({ NAME to AGE }) {
        ID.gt(1)
    }
    val c = mapper.selectMaps(
        Wrappers.query<Person?>()
            .select("id", "`name`", "age")
    ).map {
        Triple(it?.get("id") as Int?, it?.get("name") as String?, it?.get("age") as Int?)
    }
    mapper.queryTriple({ Triple(ID, NAME, AGE) }) {}

    mapper.delete {
        ID.eq(1)
    }
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

fun extSql(fn: PersonMapperQueryWrapper.() -> Unit): String {
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
