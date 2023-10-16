package com.example.demo

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
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
        mapper.query()
            .ID.eq(1)
            .toList().size shouldBe 1
        mapper.query()
            .ID.eq(1)
            .toOne()!!.id shouldBe 1
        mapper.query()
            .ID.eq(1)
            .toCount() shouldBe 1
        mapper.query()
            .toPage(Page(1, 2, true)).let { p ->
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
        mapper.querySingleField({ AGE }) {} shouldContainExactly listOf(1, 2, null)
        mapper.querySingleField({ ID.markNotNull }) {} shouldContainExactly listOf(1, 2, 3)
    }

    @Test
    fun queryPair() {
        mapper.queryPair({ NAME to AGE }) {} shouldContainExactly listOf("aba" to 1, "abb" to 2, "abc" to null)
    }

    @Test
    fun queryTriple() {
        mapper.queryTriple({ Triple(ID, NAME, AGE) }) {} shouldContainExactly listOf(
            Triple(1, "aba", 1),
            Triple(2, "abb", 2),
            Triple(3, "abc", null)
        )
    }

    @Test
    fun delete() {
        mapper.query().ID.eq(1).toCount() shouldBe 1
        mapper.delete()
            .ID.eq(1)
            .delete() shouldBe 1
        mapper.query().ID.eq(1).toCount() shouldBe 0

        mapper.query().ID.eq(2).toCount() shouldBe 1
        mapper.delete {
            ID.eq(2)
        }
        mapper.query().ID.eq(2).toCount() shouldBe 0
    }

    @Test
    fun update() {
        mapper.query().ID.eq(1).toOne()!!.name shouldBe "aba"
        mapper.updateById(1) {
            NAME.set("bcd")
        }
        mapper.query().ID.eq(1).toOne()!!.name shouldBe "bcd"

        mapper.query().ID.eq(2).toOne()!!.name shouldBe "abb"
        mapper.update()
            .NAME.set("bcd")
            .ID.eq(2)
            .update()
        mapper.query().ID.eq(2).toOne()!!.name shouldBe "bcd"

        mapper.query().ID.eq(3).toOne()!!.name shouldBe "abc"
        mapper.update {
            NAME.set("bcd")
            ID.eq(3)
        }
        mapper.query().ID.eq(3).toOne()!!.name shouldBe "bcd"
    }

    @Test
    fun updateWithWhere() {
        mapper.query().ID.eq(1).toOne()!!.name shouldBe "aba"
        mapper.where {
            ID.eq(1)
        }.update {
            NAME.set("bcd")
        }
        mapper.query().ID.eq(1).toOne()!!.name shouldBe "bcd"
    }


}