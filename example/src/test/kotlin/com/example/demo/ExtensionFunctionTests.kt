package com.example.demo

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql

@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = ["classpath:data.sql"])
class ExtensionFunctionTests {
    @Autowired
    lateinit var mapper: PersonMapper

    @Test
    fun query() {
        mapper.where {
            ID.eq(1)
        }.toList().size shouldBe 1
        mapper.where {
            ID.eq(1)
        }.toOne()!!.id shouldBe 1
        mapper.where {
            ID.eq(1)
        }.toCount() shouldBe 1
        mapper.where {
        }.toPage(Page(1, 2, true)).let { p ->
            p.size shouldBe 2
            p.total shouldBe 3
        }
        mapper.queryCount {
            NAME.likeRight("ab")
        } shouldBe 3
        mapper.queryOne {
            ID.eq(1)
        }!!.id shouldBe 1
        mapper.queryList {
            AGE.isNotNull()
        }.size shouldBe 2

        mapper.queryPage(Page(1, 1, true)) {}.let { p ->
            p.size shouldBe 1
            p.total shouldBe 3
        }
    }

    @Test
    fun querySingleField() {
        mapper.querySingleFieldList({ AGE }) {} shouldContainExactly listOf(1, 2, null)
        mapper.querySingleFieldList({ ID.markNotNull }) {} shouldContainExactly listOf(1, 2, 3)
    }

    @Test
    fun queryPair() {
        mapper.queryPairList({ NAME to AGE }) {} shouldContainExactly listOf("aba" to 1, "abb" to 2, "abc" to null)
    }

    @Test
    fun queryTriple() {
        mapper.queryTripleList({ Triple(ID, NAME, AGE) }) {} shouldContainExactly listOf(
            Triple(1, "aba", 1),
            Triple(2, "abb", 2),
            Triple(3, "abc", null)
        )
    }

    @Test
    fun delete() {
        mapper.queryCount { ID.eq(2) } shouldBe 1
        mapper.delete {
            ID.eq(2)
        }
        mapper.queryCount { ID.eq(2) } shouldBe 0
        shouldThrow<IllegalArgumentException> {
            mapper.delete {}
        }
    }

    @Test
    fun update() {
        mapper.queryOne { ID.eq(1) }!!.name shouldBe "aba"
        mapper.updateById(1) {
            NAME.set("bcd")
        }
        mapper.queryOne { ID.eq(1) }!!.name shouldBe "bcd"

        mapper.queryOne { ID.eq(3) }!!.name shouldBe "abc"
        mapper.where {
            ID.eq(3)
        }.update {
            NAME.set("bcd")
        }
        mapper.queryOne { ID.eq(3) }!!.name shouldBe "bcd"
        shouldThrow<IllegalArgumentException> {
            mapper.where { }
                .update {
                    NAME.set("abc")
                }
        }
    }

    @Test
    fun insertOrUpdate() {
        mapper.queryCount { NAME.eq("aba") } shouldBe 1
        mapper.insertOrUpdate(Person().apply {
            name = "aba"
        })
        mapper.queryCount { NAME.eq("aba") } shouldBe 2
        mapper.insertOrUpdate(Person().apply {
            id = 1
            name = "efg"
        })
        mapper.queryCount { NAME.eq("aba") } shouldBe 1
    }
}
