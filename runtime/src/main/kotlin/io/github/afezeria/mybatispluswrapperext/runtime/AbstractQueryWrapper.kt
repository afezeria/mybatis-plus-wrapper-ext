package io.github.afezeria.mybatispluswrapperext.runtime

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.toolkit.Wrappers

/**
 *
 * @author afezeria
 */
abstract class AbstractWrapperWrapper<
        S : AbstractWrapperWrapper<S, M, AW, T>,
        M : BaseMapper<T>,
        AW : AbstractWrapper<T, String, AW>,
        T>(
    val mapper: M,
    wrapper: AW,
) {
    private val stack = mutableListOf(wrapper)
    val wrapper get() = stack.last()

    @Suppress("UNCHECKED_CAST")
    private val self: S = this as S

    fun and(fn: S.() -> Unit): S {
        wrapper.and {
            stack.add(it)
            fn(self)
            stack.removeAt(stack.size - 1)
        }
        return self
    }

    fun and(condition: Boolean, fn: S.() -> Unit): S {
        if (condition) {
            wrapper.and {
                stack.add(it)
                fn(self)
                stack.removeAt(stack.size - 1)
            }
        }
        return self
    }

    fun or(): S {
        wrapper.or()
        return self
    }

    fun or(fn: S.() -> Unit): S {
        wrapper.or {
            stack.add(it)
            fn(self)
            stack.removeAt(stack.size - 1)
        }
        return self
    }

    fun or(condition: Boolean, fn: S.() -> Unit): S {
        if (condition) {
            wrapper.or {
                stack.add(it)
                fn(self)
                stack.removeAt(stack.size - 1)
            }
        }
        return self
    }

}

abstract class AbstractQueryWrapper<
        S : AbstractQueryWrapper<S, M, T>,
        M : BaseMapper<T>,
        T>(
    mapper: M,
) : AbstractWrapperWrapper<S, M, QueryWrapper<T>, T>(mapper, Wrappers.query()) {

    fun toList(): List<T> {
        return mapper.selectList(wrapper)
    }

    fun toOne(): T? {
        return mapper.selectOne(wrapper)
    }

    fun toCount(): Long {
        return mapper.selectCount(wrapper)
    }

    fun delete(): Int {
        return mapper.delete(wrapper)
    }

}

abstract class AbstractUpdateWrapper<
        S : AbstractUpdateWrapper<S, M, T>,
        M : BaseMapper<T>,
        T>(
    mapper: M,
) : AbstractWrapperWrapper<S, M, UpdateWrapper<T>, T>(mapper, Wrappers.update()) {

    fun update(): Int {
        return mapper.update(null, wrapper)
    }
}
