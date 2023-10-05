package com.example.demo

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.LocalDateTime

@SpringBootApplication
class Demo5Application

fun main(args: Array<String>) {
    runApplication<Demo5Application>(*args)
}

@TableName("person")
class Person {
    @TableId("id", type = IdType.AUTO)
    var id: Int? = null

    @TableField(value = "Name")
    var name: String? = null
    val age: Int? = null

    val now: LocalDateTime
        get() = LocalDateTime.now()
    val d1 by lazy {
        ""
    }
}

interface PersonMapper : BaseMapper<Person>
