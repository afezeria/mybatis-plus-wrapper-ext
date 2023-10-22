package io.github.afezeria.mybatispluswrapperext.processor.mapper

/**
 *
 * @author afezeria
 */
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

const val NAME = "Name"

@TableName("person")
class Person {
    /**
     * primary key
     */
    @TableId("id", type = IdType.AUTO)
    var id: Int? = null

    /**
     * person name %
     */
    var name: String? = null

    @TableField("`age`")
    var age: Int? = null
    val imgs: Array<Byte>? = null
    var createTime: LocalDateTime? = null
    var updateTime: LocalDateTime? = null
    override fun toString(): String {
        return "Person(id=$id, name=$name, age=$age, createTime=$createTime, updateTime=$updateTime)"
    }

}


@Retention()
@Target(AnnotationTarget.CLASS)
annotation class IgnoreTag

interface MyBaseMapper<T> : com.baomidou.mybatisplus.core.mapper.BaseMapper<T>

@IgnoreTag
interface PersonMapper : MyBaseMapper<Person> {

}
