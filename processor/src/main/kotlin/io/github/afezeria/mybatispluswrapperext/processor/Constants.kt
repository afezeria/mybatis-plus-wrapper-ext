package io.github.afezeria.mybatispluswrapperext.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName

/**
 *
 * @author afezeria
 */
val UNIT_CLASS_NAME = Unit::class.asClassName()
val INT_CLASS_NAME = Int::class.asClassName()
val LONG_CLASS_NAME = Long::class.asClassName()
val LIST_CLASS_NAME = List::class.asClassName()
val PAIR_CLASS_NAME = Pair::class.asClassName()
val TRIPLE_CLASS_NAME = Triple::class.asClassName()

val TYPE_VAR_F1 = TypeVariableName("F1")
val TYPE_VAR_F2 = TypeVariableName("F2")
val TYPE_VAR_F3 = TypeVariableName("F3")

const val TABLE_FIELD_QUALIFIED_NAME = "com.baomidou.mybatisplus.annotation.TableField"
const val TABLE_ID_QUALIFIED_NAME = "com.baomidou.mybatisplus.annotation.TableId"
val BASE_MAPPER_CLASS_NAME = ClassName("com.baomidou.mybatisplus.core.mapper", "BaseMapper")
val I_PAGE_CLASS_NAME = ClassName("com.baomidou.mybatisplus.core.metadata", "IPage")