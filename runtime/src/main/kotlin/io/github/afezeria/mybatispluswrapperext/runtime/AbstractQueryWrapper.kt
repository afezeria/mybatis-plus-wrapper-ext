package io.github.afezeria.mybatispluswrapperext.runtime

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.metadata.IPage
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
    protected val self: S = this as S

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

    fun <F> toSingleFieldList(columnFn: S.() -> FieldDefinition<S, *, F>): List<F> {
        val column = columnFn(self)
        wrapper.select(column.name)
        return mapper.selectMaps(wrapper).map {
            @Suppress("UNCHECKED_CAST")
            it?.get(column.trimName) as F
        }
    }

    fun <F1, F2> toPairList(columnsFn: S.() -> Pair<FieldDefinition<S, *, F1>, FieldDefinition<S, *, F2>>): List<Pair<F1, F2>> {
        val columns = columnsFn(self)
        wrapper.select(columns.first.name, columns.second.name)
        return mapper.selectMaps(wrapper).map {
            @Suppress("UNCHECKED_CAST")
            Pair(
                it?.get(columns.first.trimName) as F1,
                it?.get(columns.second.trimName) as F2,
            )
        }
    }

    fun <F1, F2, F3> toTripleList(
        columnsFn: S.() -> Triple<FieldDefinition<S, *, F1>, FieldDefinition<S, *, F2>, FieldDefinition<S, *, F3>>
    ): List<Triple<F1, F2, F3>> {
        val columns = columnsFn(self)
        wrapper.select(columns.first.name, columns.second.name, columns.third.name)
        @Suppress("UNCHECKED_CAST")
        return mapper.selectMaps(wrapper).map {
            Triple(
                it?.get(columns.first.trimName) as F1,
                it?.get(columns.second.trimName) as F2,
                it?.get(columns.third.trimName) as F3
            )
        }
    }

    fun <P : IPage<T>> toPage(page: P): P {
        return mapper.selectPage(page, wrapper)
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
