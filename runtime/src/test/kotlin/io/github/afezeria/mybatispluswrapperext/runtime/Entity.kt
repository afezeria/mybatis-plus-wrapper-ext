package io.github.afezeria.mybatispluswrapperext.runtime

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.core.mapper.BaseMapper

/**
 *
 * @author afezeria
 */
@TableName("person")
class Person {
    @TableId("id", type = IdType.AUTO)
    var id: Int? = null

    @TableField(value = "")
    var name: String? = null
    var age: Int? = null

}

interface PersonMapper : BaseMapper<Person>
