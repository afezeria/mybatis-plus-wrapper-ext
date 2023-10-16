package com.example.demo

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDateTime

@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = ["classpath:data.sql"])
class ConditionFunctionTests {
    @Autowired
    lateinit var mapper: PersonMapper

    @Test
    fun between() {
        mapper.queryList {
            ID.between(1, 2)
        }.size shouldBe 2
        mapper.queryList {
            ID.between(true, 1, 2)
        }.size shouldBe 2
        mapper.queryList {
            ID.between(false, 1, 2)
        }.size shouldBe 3
        mapper.queryList {
            ID.between(true) { 1 to 2 }
        }.size shouldBe 2
        mapper.queryList {
            ID.between(false) { 1 to 2 }
        }.size shouldBe 3
    }

    @Test
    fun eq() {
        mapper.queryList {
            ID.eq(1)
        }.size shouldBe 1
        mapper.queryList {
            ID.eq(true, 1)
        }.size shouldBe 1
        mapper.queryList {
            ID.eq(false, 1)
        }.size shouldBe 3
        mapper.queryList {
            ID.eq(true) { 1 }
        }.size shouldBe 1
        mapper.queryList {
            ID.eq(false) { 1 }
        }.size shouldBe 3
    }

    @Test
    fun ge() {
        mapper.queryList {
            ID.ge(2)
        }.size shouldBe 2
        mapper.queryList {
            ID.ge(true, 2)
        }.size shouldBe 2
        mapper.queryList {
            ID.ge(false, 2)
        }.size shouldBe 3
        mapper.queryList {
            ID.ge(true) { 2 }
        }.size shouldBe 2
        mapper.queryList {
            ID.ge(false) { 2 }
        }.size shouldBe 3
    }

    @Test
    fun gt() {
        mapper.queryList {
            ID.gt(2)
        }.size shouldBe 1
        mapper.queryList {
            ID.gt(true, 2)
        }.size shouldBe 1
        mapper.queryList {
            ID.gt(false, 2)
        }.size shouldBe 3
        mapper.queryList {
            ID.gt(true) { 2 }
        }.size shouldBe 1
        mapper.queryList {
            ID.gt(false) { 2 }
        }.size shouldBe 3
    }

    @Test
    fun `in`() {
        mapper.queryList {
            ID.`in`(2, 3, 4)
        }.size shouldBe 2
        mapper.queryList {
            ID.`in`(listOf(2, 3, 4))
        }.size shouldBe 2
        mapper.queryList {
            ID.`in`(listOf(null, 3, 4))
        }.size shouldBe 1
        mapper.queryList {
            ID.`in`(true, 2)
        }.size shouldBe 1
        mapper.queryList {
            ID.`in`(false, 2)
        }.size shouldBe 3
        mapper.queryList {
            ID.`in`(true) { listOf(2) }
        }.size shouldBe 1
        mapper.queryList {
            ID.`in`(false) { listOf(2) }
        }.size shouldBe 3
    }

    @Test
    fun isNotNull() {
        mapper.queryList {
            AGE.isNotNull()
        }.size shouldBe 2
        mapper.queryList {
            AGE.isNotNull(true)
        }.size shouldBe 2
        mapper.queryList {
            AGE.isNotNull(false)
        }.size shouldBe 3
    }

    @Test
    fun isNull() {
        mapper.queryList {
            AGE.isNull()
        }.size shouldBe 1
        mapper.queryList {
            AGE.isNull(true)
        }.size shouldBe 1
        mapper.queryList {
            AGE.isNull(false)
        }.size shouldBe 3
    }

    @Test
    fun le() {
        mapper.queryList {
            ID.le(2)
        }.size shouldBe 2
        mapper.queryList {
            ID.le(true, 2)
        }.size shouldBe 2
        mapper.queryList {
            ID.le(false, 2)
        }.size shouldBe 3
        mapper.queryList {
            ID.le(true) { 2 }
        }.size shouldBe 2
        mapper.queryList {
            ID.le(false) { 2 }
        }.size shouldBe 3
    }

    @Test
    fun like() {
        mapper.queryList {
            NAME.like("ab")
        }.size shouldBe 3
        mapper.queryList {
            NAME.like(true, "bc")
        }.size shouldBe 1
        mapper.queryList {
            NAME.like(false, "")
        }.size shouldBe 3
        mapper.queryList {
            NAME.like(true) { "aba" }
        }.size shouldBe 1
        mapper.queryList {
            NAME.like(false) { "" }
        }.size shouldBe 3
    }

    @Test
    fun likeLeft() {
        mapper.queryList {
            NAME.likeLeft("ab")
        }.size shouldBe 0
        mapper.queryList {
            NAME.likeLeft(true, "bc")
        }.size shouldBe 1
        mapper.queryList {
            NAME.likeLeft(false, "")
        }.size shouldBe 3
        mapper.queryList {
            NAME.likeLeft(true) { "c" }
        }.size shouldBe 1
        mapper.queryList {
            NAME.likeLeft(false) { "" }
        }.size shouldBe 3
    }

    @Test
    fun likeRight() {
        mapper.queryList {
            NAME.likeRight("ab")
        }.size shouldBe 3
        mapper.queryList {
            NAME.likeRight(true, "bc")
        }.size shouldBe 0
        mapper.queryList {
            NAME.likeRight(false, "")
        }.size shouldBe 3
        mapper.queryList {
            NAME.likeRight(true) { "c" }
        }.size shouldBe 0
        mapper.queryList {
            NAME.likeRight(false) { "" }
        }.size shouldBe 3
    }

    @Test
    fun lt() {
        mapper.queryList {
            ID.lt(2)
        }.size shouldBe 1
        mapper.queryList {
            ID.lt(true, 2)
        }.size shouldBe 1
        mapper.queryList {
            ID.lt(false, 2)
        }.size shouldBe 3
        mapper.queryList {
            ID.lt(true) { 3 }
        }.size shouldBe 2
        mapper.queryList {
            ID.lt(false) { 2 }
        }.size shouldBe 3
    }

    @Test
    fun ne() {
        mapper.queryList {
            ID.ne(2)
        }.size shouldBe 2
        mapper.queryList {
            ID.ne(true, 2)
        }.size shouldBe 2
        mapper.queryList {
            ID.ne(false, 2)
        }.size shouldBe 3
        mapper.queryList {
            ID.ne(true) { 3 }
        }.size shouldBe 2
        mapper.queryList {
            ID.ne(false) { 2 }
        }.size shouldBe 3
    }

    @Test
    fun notBetween() {
        mapper.queryList {
            ID.notBetween(1, 2)
        }.size shouldBe 1
        mapper.queryList {
            ID.notBetween(true, 1, 3)
        }.size shouldBe 0
        mapper.queryList {
            ID.notBetween(false, 1, 2)
        }.size shouldBe 3
        mapper.queryList {
            ID.notBetween(true) { 1 to 2 }
        }.size shouldBe 1
        mapper.queryList {
            ID.notBetween(false) { 1 to 2 }
        }.size shouldBe 3
    }

    @Test
    fun notIn() {
        mapper.queryList {
            ID.notIn(2, 3, 4)
        }.size shouldBe 1
        mapper.queryList {
            ID.notIn(listOf(2, 3, 4))
        }.size shouldBe 1
        mapper.queryList {
            ID.notIn(listOf(2, null, 4))
        }.size shouldBe 0
        mapper.queryList {
            ID.notIn(true, 2)
        }.size shouldBe 2
        mapper.queryList {
            ID.notIn(false, 2)
        }.size shouldBe 3
        mapper.queryList {
            ID.notIn(true) { listOf(4) }
        }.size shouldBe 3
        mapper.queryList {
            ID.notIn(false) { listOf(2) }
        }.size shouldBe 3
    }

    @Test
    fun notLike() {
        mapper.queryList {
            NAME.notLike("ab")
        }.size shouldBe 0
        mapper.queryList {
            NAME.notLike(true, "ef")
        }.size shouldBe 3
        mapper.queryList {
            NAME.notLike(false, "")
        }.size shouldBe 3
        mapper.queryList {
            NAME.notLike(true) { "aba" }
        }.size shouldBe 2
        mapper.queryList {
            NAME.notLike(false) { "" }
        }.size shouldBe 3
    }

    @Test
    fun notLikeLeft() {
        mapper.queryList {
            NAME.notLikeLeft("ab")
        }.size shouldBe 3
        mapper.queryList {
            NAME.notLikeLeft(true, "bc")
        }.size shouldBe 2
        mapper.queryList {
            NAME.notLikeLeft(false, "")
        }.size shouldBe 3
        mapper.queryList {
            NAME.notLikeLeft(true) { "c" }
        }.size shouldBe 2
        mapper.queryList {
            NAME.notLikeLeft(false) { "" }
        }.size shouldBe 3
    }

    @Test
    fun notLikeRight() {
        mapper.queryList {
            NAME.notLikeRight("ab")
        }.size shouldBe 0
        mapper.queryList {
            NAME.notLikeRight(true, "bc")
        }.size shouldBe 3
        mapper.queryList {
            NAME.notLikeRight(false, "")
        }.size shouldBe 3
        mapper.queryList {
            NAME.notLikeRight(true) { "c" }
        }.size shouldBe 3
        mapper.queryList {
            NAME.notLikeRight(false) { "" }
        }.size shouldBe 3
    }

    @Test
    fun and() {
        mapper.queryList {
            NAME.likeRight("ab")
                .and {
                    AGE.gt(1)
                }
        }.size shouldBe 1
        mapper.queryList {
            NAME.likeRight("ab")
                .and(true) {
                    AGE.gt(1)
                }
        }.size shouldBe 1
        mapper.queryList {
            NAME.likeRight("ab")
                .and(false) {
                    AGE.gt(1)
                }
        }.size shouldBe 3
    }

    @Test
    fun or() {
        mapper.queryCount {
            ID.eq(1)
                .or()
                .AGE.eq(2)
        } shouldBe 2
        mapper.queryCount {
            ID.eq(1)
            or()
            AGE.eq(2)
        } shouldBe 2
        mapper.queryCount {
            ID.eq(1)
                .or {
                    AGE.eq(2)
                }
        } shouldBe 2
        mapper.queryCount {
            ID.eq(1)
                .or(true) {
                    AGE.eq(2)
                }
        } shouldBe 2
        mapper.queryCount {
            ID.eq(1)
                .or(true) {
                    AGE.eq(2)
                }
        } shouldBe 2
        mapper.queryCount {
            ID.eq(1)
                .or(false) {
                    AGE.eq(2)
                }
        } shouldBe 1
    }

    @Test
    fun set() {
        mapper.selectById(1).apply {
            name shouldBe "aba"
            age shouldBe 1
            createTime shouldNotBe LocalDateTime.MIN
        }
        mapper.updateById(1) {
            NAME.set("a")
            AGE.set(false, 2)
            CREATE_TIME.set(true) { LocalDateTime.MIN }
        }
        mapper.selectById(1).apply {
            name shouldBe "a"
            age shouldBe 1
            createTime shouldBe LocalDateTime.MIN
        }
    }

    @Test
    fun setNull() {
        mapper.selectById(1).apply {
            name shouldBe "aba"
        }
        mapper.updateById(1) {
            NAME.setNull()
        }
        mapper.selectById(1).apply {
            name shouldBe null
        }
    }

    @Test
    fun setNullWithCondition() {
        mapper.selectById(1).apply {
            name shouldBe "aba"
            age shouldBe 1
        }
        mapper.updateById(1) {
            NAME.setNull(false)
            AGE.setNull(true)
        }
        mapper.selectById(1).apply {
            name shouldBe "aba"
            age shouldBe null
        }

    }
}
//between, eq, ge, gt, in, isNotNull, isNull, le, like, likeLeft, likeRight, lt, ne, notBetween, notIn,notLike, notLikeLeft, notLikeRight, and, or