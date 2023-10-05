package io.github.afezeria.mybatispluswrapperext.runtime

import com.baomidou.mybatisplus.core.toolkit.Wrappers
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

/**
 *
 * @author afezeria
 */

class ExtensionTest : StringSpec({
    "single condition" {

        extSql { ID.`in`(1) } shouldBeEqual oriSql { `in`(Person::id.name, 1) }
        extSql { ID.`in`(true, 1) } shouldBeEqual oriSql {
            if (true) {
                `in`(Person::id.name, 1)
            }
        }
        extSql { ID.`in`(false, 1) } shouldBeEqual oriSql {
            if (false) {
                `in`(Person::id.name, 1)
            }
        }
        extSql { ID.`in`(1) } shouldBeEqual oriSql { `in`(Person::id.name, 1) }
        extSql { ID.`in`(true, 1) } shouldBeEqual oriSql {
            if (true) {
                `in`(Person::id.name, 1)
            }
        }
        extSql { ID.`in`(false, 1) } shouldBeEqual oriSql {
            if (false) {
                `in`(Person::id.name, 1)
            }
        }
        extSql { ID.`in`(true) { listOf(1) } } shouldBeEqual oriSql {
            if (true) {
                `in`(Person::id.name, 1)
            }
        }
        extSql { ID.`in`(false) { listOf(1) } } shouldBeEqual oriSql {
            if (false) {
                `in`(Person::id.name, 1)
            }
        }
        extSql { ID.le(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                le(Person::id.name, 1)
            }
        }
        extSql { ID.le(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                le(Person::id.name, 1)
            }
        }
        extSql { ID.le(1) } shouldBeEqual oriSql { le(Person::id.name, 1) }
        extSql { ID.le(true, 1) } shouldBeEqual oriSql {
            if (true) {
                le(Person::id.name, 1)
            }
        }
        extSql { ID.le(false, 1) } shouldBeEqual oriSql {
            if (false) {
                le(Person::id.name, 1)
            }
        }
        extSql { ID.eq(true, 1) } shouldBeEqual oriSql {
            if (true) {
                eq(Person::id.name, 1)
            }
        }
        extSql { ID.eq(false, 1) } shouldBeEqual oriSql {
            if (false) {
                eq(Person::id.name, 1)
            }
        }
        extSql { ID.eq(1) } shouldBeEqual oriSql { eq(Person::id.name, 1) }
        extSql { ID.eq(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                eq(Person::id.name, 1)
            }
        }
        extSql { ID.eq(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                eq(Person::id.name, 1)
            }
        }
        extSql { ID.isNull(true) } shouldBeEqual oriSql {
            if (true) {
                isNull(Person::id.name)
            }
        }
        extSql { ID.isNull(false) } shouldBeEqual oriSql {
            if (false) {
                isNull(Person::id.name)
            }
        }
        extSql { ID.isNull() } shouldBeEqual oriSql { isNull(Person::id.name) }
        extSql { ID.lt(1) } shouldBeEqual oriSql { lt(Person::id.name, 1) }
        extSql { ID.lt(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                lt(Person::id.name, 1)
            }
        }
        extSql { ID.lt(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                lt(Person::id.name, 1)
            }
        }
        extSql { ID.lt(true, 1) } shouldBeEqual oriSql {
            if (true) {
                lt(Person::id.name, 1)
            }
        }
        extSql { ID.lt(false, 1) } shouldBeEqual oriSql {
            if (false) {
                lt(Person::id.name, 1)
            }
        }
        extSql { ID.between(true) { 1 to 1 } } shouldBeEqual oriSql {
            if (true) {
                between(Person::id.name, 1, 1)
            }
        }
        extSql { ID.between(false) { 1 to 1 } } shouldBeEqual oriSql {
            if (false) {
                between(Person::id.name, 1, 1)
            }
        }
        extSql { ID.between(true, 1, 1) } shouldBeEqual oriSql {
            if (true) {
                between(Person::id.name, 1, 1)
            }
        }
        extSql { ID.between(1, 1) } shouldBeEqual oriSql { between(Person::id.name, 1, 1) }
        extSql { ID.ne(1) } shouldBeEqual oriSql { ne(Person::id.name, 1) }
        extSql { ID.ne(true, 1) } shouldBeEqual oriSql {
            if (true) {
                ne(Person::id.name, 1)
            }
        }
        extSql { ID.ne(false, 1) } shouldBeEqual oriSql {
            if (false) {
                ne(Person::id.name, 1)
            }
        }
        extSql { ID.ne(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                ne(Person::id.name, 1)
            }
        }
        extSql { ID.ne(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                ne(Person::id.name, 1)
            }
        }
        extSql { ID.ge(true, 1) } shouldBeEqual oriSql {
            if (true) {
                ge(Person::id.name, 1)
            }
        }
        extSql { ID.ge(false, 1) } shouldBeEqual oriSql {
            if (false) {
                ge(Person::id.name, 1)
            }
        }
        extSql { ID.ge(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                ge(Person::id.name, 1)
            }
        }
        extSql { ID.ge(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                ge(Person::id.name, 1)
            }
        }
        extSql { ID.ge(1) } shouldBeEqual oriSql { ge(Person::id.name, 1) }
        extSql { ID.gt(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                gt(Person::id.name, 1)
            }
        }
        extSql { ID.gt(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                gt(Person::id.name, 1)
            }
        }
        extSql { ID.gt(true, 1) } shouldBeEqual oriSql {
            if (true) {
                gt(Person::id.name, 1)
            }
        }
        extSql { ID.gt(false, 1) } shouldBeEqual oriSql {
            if (false) {
                gt(Person::id.name, 1)
            }
        }
        extSql { ID.gt(1) } shouldBeEqual oriSql { gt(Person::id.name, 1) }
        extSql { ID.isNotNull(true) } shouldBeEqual oriSql {
            if (true) {
                isNotNull(Person::id.name)
            }
        }
        extSql { ID.isNotNull(false) } shouldBeEqual oriSql {
            if (false) {
                isNotNull(Person::id.name)
            }
        }
        extSql { ID.isNotNull() } shouldBeEqual oriSql { isNotNull(Person::id.name) }
        extSql { ID.like(true, 1) } shouldBeEqual oriSql {
            if (true) {
                like(Person::id.name, 1)
            }
        }
        extSql { ID.like(false, 1) } shouldBeEqual oriSql {
            if (false) {
                like(Person::id.name, 1)
            }
        }
        extSql { ID.like(1) } shouldBeEqual oriSql { like(Person::id.name, 1) }
        extSql { ID.like(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                like(Person::id.name, 1)
            }
        }
        extSql { ID.like(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                like(Person::id.name, 1)
            }
        }
        extSql { ID.likeLeft(1) } shouldBeEqual oriSql { likeLeft(Person::id.name, 1) }
        extSql { ID.likeLeft(true, 1) } shouldBeEqual oriSql {
            if (true) {
                likeLeft(Person::id.name, 1)
            }
        }
        extSql { ID.likeLeft(false, 1) } shouldBeEqual oriSql {
            if (false) {
                likeLeft(Person::id.name, 1)
            }
        }
        extSql { ID.likeLeft(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                likeLeft(Person::id.name, 1)
            }
        }
        extSql { ID.likeLeft(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                likeLeft(Person::id.name, 1)
            }
        }
        extSql { ID.likeRight(true, 1) } shouldBeEqual oriSql {
            if (true) {
                likeRight(Person::id.name, 1)
            }
        }
        extSql { ID.likeRight(false, 1) } shouldBeEqual oriSql {
            if (false) {
                likeRight(Person::id.name, 1)
            }
        }
        extSql { ID.likeRight(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                likeRight(Person::id.name, 1)
            }
        }
        extSql { ID.likeRight(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                likeRight(Person::id.name, 1)
            }
        }
        extSql { ID.likeRight(1) } shouldBeEqual oriSql { likeRight(Person::id.name, 1) }
        extSql { ID.notBetween(true) { 1 to 1 } } shouldBeEqual oriSql {
            if (true) {
                notBetween(Person::id.name, 1, 1)
            }
        }
        extSql { ID.notBetween(false) { 1 to 1 } } shouldBeEqual oriSql {
            if (false) {
                notBetween(Person::id.name, 1, 1)
            }
        }
        extSql { ID.notBetween(true, 1, 1) } shouldBeEqual oriSql {
            if (true) {
                notBetween(Person::id.name, 1, 1)
            }
        }
        extSql { ID.notBetween(1, 1) } shouldBeEqual oriSql { notBetween(Person::id.name, 1, 1) }
        extSql { ID.notLike(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                notLike(Person::id.name, 1)
            }
        }
        extSql { ID.notLike(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                notLike(Person::id.name, 1)
            }
        }
        extSql { ID.notLike(true, 1) } shouldBeEqual oriSql {
            if (true) {
                notLike(Person::id.name, 1)
            }
        }
        extSql { ID.notLike(false, 1) } shouldBeEqual oriSql {
            if (false) {
                notLike(Person::id.name, 1)
            }
        }
        extSql { ID.notLike(1) } shouldBeEqual oriSql { notLike(Person::id.name, 1) }
        extSql { ID.notLikeLeft(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                notLikeLeft(Person::id.name, 1)
            }
        }
        extSql { ID.notLikeLeft(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                notLikeLeft(Person::id.name, 1)
            }
        }
        extSql { ID.notLikeLeft(true, 1) } shouldBeEqual oriSql {
            if (true) {
                notLikeLeft(Person::id.name, 1)
            }
        }
        extSql { ID.notLikeLeft(false, 1) } shouldBeEqual oriSql {
            if (false) {
                notLikeLeft(Person::id.name, 1)
            }
        }
        extSql { ID.notLikeLeft(1) } shouldBeEqual oriSql { notLikeLeft(Person::id.name, 1) }
        extSql { ID.notLikeRight(true) { 1 } } shouldBeEqual oriSql {
            if (true) {
                notLikeRight(Person::id.name, 1)
            }
        }
        extSql { ID.notLikeRight(false) { 1 } } shouldBeEqual oriSql {
            if (false) {
                notLikeRight(Person::id.name, 1)
            }
        }
        extSql { ID.notLikeRight(true, 1) } shouldBeEqual oriSql {
            if (true) {
                notLikeRight(Person::id.name, 1)
            }
        }
        extSql { ID.notLikeRight(false, 1) } shouldBeEqual oriSql {
            if (false) {
                notLikeRight(Person::id.name, 1)
            }
        }
        extSql { ID.notLikeRight(1) } shouldBeEqual oriSql { notLikeRight(Person::id.name, 1) }


    }

    "multiple condition" {
        extSql {
            ID.eq(1)
            AGE.eq(1)
        } shouldBeEqual oriSql {
            eq("id", 1)
            eq("age", 1)
        }

        extSql {
            ID.eq(1)
                .and {
                    AGE.eq(1)
                }
        } shouldBeEqual oriSql {
            eq("id", 1)
                .and {
                    it.eq("age", 1)
                }
        }
        extSql {
            ID.eq(1)
                .and(false) {
                    AGE.eq(1)
                }
        } shouldBeEqual oriSql {
            eq("id", 1)
                .and(false) {
                    it.eq("age", 1)
                }
        }

        extSql {
            ID.eq(1)
                .or()
                .AGE.eq(1)
                .NAME.eq("a")
        } shouldBeEqual oriSql {
            eq("id", 1)
                .or()
                .eq("age", 1)
                .eq("name", "a")
        }

        extSql {
            ID.eq(1)
                .or {
                    AGE.eq(1)
                    NAME.eq("a")
                }
        } shouldBeEqual oriSql {
            eq("id", 1)
                .or {
                    it.eq("age", 1)
                        .eq("name", "a")
                }
        }
        extSql {
            ID.eq(1)
                .or(false) {
                    AGE.eq(1)
                    NAME.eq("a")
                }
        } shouldBeEqual oriSql {
            eq("id", 1)
                .or(false) {
                    it.eq("age", 1)
                        .eq("name", "a")
                }
        }
    }
    "update" {
        val extWrapper = mapper.update()
            .AGE.set(1)
            .NAME.set(true) { "b" }
            .ID.eq(1)
            .wrapper
        val oriWrapper = Wrappers.update<Person>()
            .set("age", 1)
            .set(true, "name", "b")
            .eq("id", 1)
        extWrapper.sqlSet shouldBeEqual oriWrapper.sqlSet
        extWrapper.targetSql shouldBeEqual oriWrapper.targetSql

    }
})
