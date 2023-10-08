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
    @TableId("id", type = IdType.AUTO)
    var id: Int? = null

    /**
     * person name %
     */
    @TableField(value = NAME)
    var name: String? = null
    val age: Int? = null

    val now: LocalDateTime
        get() = LocalDateTime.now()
    val d1 by lazy {
        ""
    }
}

@Retention()
@Target(AnnotationTarget.CLASS)
annotation class IgnoreTag

interface MyBaseMapper<T> : com.baomidou.mybatisplus.core.mapper.BaseMapper<T>

@IgnoreTag
interface PersonMapper : MyBaseMapper<Person> {

}
