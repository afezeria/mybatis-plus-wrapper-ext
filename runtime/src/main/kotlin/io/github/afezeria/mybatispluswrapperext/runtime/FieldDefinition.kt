package io.github.afezeria.mybatispluswrapperext.runtime

open class FieldDefinition<ME : AbstractWrapperWrapper<*, *, *, *>, T, VT>(
    val name: String,
    protected val owner: ME,
) {
    val trimName = name.trim('`', '"', '\'')

    @Suppress("UNCHECKED_CAST")
    val markNotNull: FieldDefinition<ME, T, VT & Any> = this as FieldDefinition<ME, T, VT & Any>

    fun between(value1: T, value2: T): ME {
        owner.wrapper.between(name, value1, value2)
        return owner
    }

    fun between(condition: Boolean, value1: T, value2: T): ME {
        if (condition) {
            owner.wrapper.between(name, value1, value2)
        }
        return owner
    }

    fun between(condition: Boolean, pair: () -> Pair<T, T>): ME {
        if (condition) {
            val (v1, v2) = pair()
            owner.wrapper.between(name, v1, v2)
        }
        return owner
    }

    infix fun eq(value: T): ME {
        owner.wrapper.eq(name, value)
        return owner
    }

    fun eq(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.eq(name, value)
        }
        return owner
    }

    fun eq(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.eq(name, value())
        }
        return owner
    }

    infix fun ge(value: T): ME {
        owner.wrapper.ge(name, value)
        return owner
    }

    fun ge(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.ge(name, value)
        }
        return owner
    }

    fun ge(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.ge(name, value())
        }
        return owner
    }

    infix fun gt(value: T): ME {
        owner.wrapper.gt(name, value)
        return owner
    }

    fun gt(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.gt(name, value)
        }
        return owner
    }

    fun gt(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.gt(name, value())
        }
        return owner
    }

    fun `in`(values: Collection<T?>): ME {
        owner.wrapper.`in`(name, values)
        return owner
    }

    fun `in`(vararg values: T): ME {
        owner.wrapper.`in`(name, *values)
        return owner
    }

    fun `in`(condition: Boolean, values: Collection<T?>): ME {
        if (condition) {
            owner.wrapper.`in`(name, values)
        }
        return owner
    }

    fun `in`(condition: Boolean, vararg values: T): ME {
        if (condition) {
            owner.wrapper.`in`(name, *values)
        }
        return owner
    }

    fun `in`(condition: Boolean, values: () -> Collection<T?>): ME {
        if (condition) {
            owner.wrapper.`in`(name, values())
        }
        return owner
    }

    fun notIn(values: Collection<T?>): ME {
        owner.wrapper.notIn(name, values)
        return owner
    }

    fun notIn(vararg values: T): ME {
        owner.wrapper.notIn(name, *values)
        return owner
    }

    fun notIn(condition: Boolean, values: Collection<T?>): ME {
        if (condition) {
            owner.wrapper.notIn(name, values)
        }
        return owner
    }

    fun notIn(condition: Boolean, vararg values: T): ME {
        if (condition) {
            owner.wrapper.notIn(name, *values)
        }
        return owner
    }

    fun notIn(condition: Boolean, values: () -> Collection<T?>): ME {
        if (condition) {
            owner.wrapper.notIn(name, values())
        }
        return owner
    }

    fun isNotNull(): ME {
        owner.wrapper.isNotNull(name)
        return owner
    }

    fun isNotNull(condition: Boolean): ME {
        if (condition) {
            owner.wrapper.isNotNull(name)
        }
        return owner
    }

    fun isNull(): ME {
        owner.wrapper.isNull(name)
        return owner
    }

    fun isNull(condition: Boolean): ME {
        if (condition) {
            owner.wrapper.isNull(name)
        }
        return owner
    }

    infix fun le(value: T): ME {
        owner.wrapper.le(name, value)
        return owner
    }

    fun le(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.le(name, value)
        }
        return owner
    }

    fun le(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.le(name, value())
        }
        return owner
    }

    infix fun like(value: T): ME {
        owner.wrapper.like(name, value)
        return owner
    }

    fun like(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.like(name, value)
        }
        return owner
    }

    fun like(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.like(name, value())
        }
        return owner
    }

    infix fun likeLeft(value: T): ME {
        owner.wrapper.likeLeft(name, value)
        return owner
    }

    fun likeLeft(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.likeLeft(name, value)
        }
        return owner
    }

    fun likeLeft(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.likeLeft(name, value())
        }
        return owner
    }

    infix fun likeRight(value: T): ME {
        owner.wrapper.likeRight(name, value)
        return owner
    }

    fun likeRight(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.likeRight(name, value)
        }
        return owner
    }

    fun likeRight(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.likeRight(name, value())
        }
        return owner
    }

    infix fun lt(value: T): ME {
        owner.wrapper.lt(name, value)
        return owner
    }

    fun lt(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.lt(name, value)
        }
        return owner
    }

    fun lt(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.lt(name, value())
        }
        return owner
    }

    infix fun ne(value: T): ME {
        owner.wrapper.ne(name, value)
        return owner
    }

    fun ne(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.ne(name, value)
        }
        return owner
    }

    fun ne(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.ne(name, value())
        }
        return owner
    }

    fun notBetween(value1: T, value2: T): ME {
        owner.wrapper.notBetween(name, value1, value2)
        return owner
    }

    fun notBetween(condition: Boolean, value1: T, value2: T): ME {
        if (condition) {
            owner.wrapper.notBetween(name, value1, value2)
        }
        return owner
    }

    fun notBetween(condition: Boolean, pair: () -> Pair<T, T>): ME {
        if (condition) {
            val (v1, v2) = pair()
            owner.wrapper.notBetween(name, v1, v2)
        }
        return owner
    }

    infix fun notLike(value: T): ME {
        owner.wrapper.notLike(name, value)
        return owner
    }

    fun notLike(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.notLike(name, value)
        }
        return owner
    }

    fun notLike(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.notLike(name, value())
        }
        return owner
    }

    infix fun notLikeLeft(value: T): ME {
        owner.wrapper.notLikeLeft(name, value)
        return owner
    }

    fun notLikeLeft(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.notLikeLeft(name, value)
        }
        return owner
    }

    fun notLikeLeft(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.notLikeLeft(name, value())
        }
        return owner
    }

    infix fun notLikeRight(value: T): ME {
        owner.wrapper.notLikeRight(name, value)
        return owner
    }

    fun notLikeRight(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.notLikeRight(name, value)
        }
        return owner
    }

    fun notLikeRight(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.notLikeRight(name, value())
        }
        return owner
    }
}

class UpdateFieldDefinition<ME : AbstractUpdateWrapper<*, *, *>, T, VT>(
    name: String,
    owner: ME,
) : FieldDefinition<ME, T, VT>(name, owner) {
    fun set(value: T): ME {
        owner.wrapper.set(name, value)
        return owner
    }

    fun set(condition: Boolean, value: T): ME {
        if (condition) {
            owner.wrapper.set(name, value)
        }
        return owner
    }

    fun set(condition: Boolean, value: () -> T): ME {
        if (condition) {
            owner.wrapper.set(name, value())
        }
        return owner
    }
}
