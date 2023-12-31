package com.example.demo

import com.baomidou.mybatisplus.annotation.*
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.LocalDateTime


@SpringBootApplication
@MapperScan("com.example.demo")
class Demo5Application {
    @Bean
    fun mybatisPlusInterceptor(): MybatisPlusInterceptor {
        val interceptor = MybatisPlusInterceptor()
        interceptor.addInnerInterceptor(PaginationInnerInterceptor(DbType.H2))
        return interceptor
    }
}

fun main(args: Array<String>) {
    runApplication<Demo5Application>(*args)
}

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

interface PersonMapper : BaseMapper<Person>
