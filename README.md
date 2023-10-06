# mybatis-plus-wrapper-ext

使用kotlin实现的针对mybatis-plus的条件构造器的扩展库。主要目的是提供类型安全的api和更好的自动补全体验。

## 快速开始

在包含mapper的模块中添加依赖：

```
plugins {
  id("com.google.devtools.ksp") version "1.9.10-1.0.13"
}

dependencies {
    implementation("io.github.afezeria:mybatis-plus-wrapper-ext-runtime:1.2.0")
    ksp("io.github.afezeria:mybatis-plus-wrapper-ext-processor:1.2.0")
}

```

执行构建命令生成代码：

```shell
./gradlew build
```

## 配置

```groovy
ksp {
    arg("dbNamingConvention", "FIELD_NAME")
    arg("ignoreMarkerAnnotation", "com.example.IgnoreTag")
}
```

参数说明：

- dbNamingConvention 表字段命名规范，在没有显示指定时会使用该配置根据实体属性名生成表字段名
    - 当字段名为handlerName时，不同规则生产成的表字段名
        - CAMEL_CASE handlerName
        - SNAKE_CASE handler_name
        - PASCAL_CASE Handler_Name
        - CONSTANT_CASE HANDLER_NAME
        - UPPER_CASE HANDLERNAME
        - FIELD_NAME handlerName (默认值，表字段名和属性名一致)
- ignoreMarkerAnnotation 指定用于注释要忽略的mapper类的注解全类名。默认情况下会扫描当前项目中所有的文件，为每一个继承BaseMapper的接口生成对应的扩展函数。

## 使用说明

实体及mapper：

```kotlin
@TableName("person")
class Person {
    @TableId("id", type = IdType.AUTO)
    var id: Int? = null
    var name: String? = null
    var age: Int? = null
}

interface PersonMapper : BaseMapper<Person>
```

生成的扩展函数签名：

```kotlin
fun PersonMapper.query(): PersonMapperQueryWrapper
fun PersonMapper.queryList(fn: PersonMapperQueryWrapper.() -> Unit): List<Person>
fun <P : IPage<Person>> PersonMapper.queryPage(page: P, fn: PersonMapperQueryWrapper.() -> Unit): P
fun PersonMapper.queryOne(fn: PersonMapperQueryWrapper.() -> Unit): Person?
fun PersonMapper.queryCount(fn: PersonMapperQueryWrapper.() -> Unit): Long
fun PersonMapper.delete(): PersonMapperQueryWrapper
fun PersonMapper.delete(fn: PersonMapperQueryWrapper.() -> Unit): Int
fun PersonMapper.update(): PersonMapperUpdateWrapper
fun PersonMapper.update(fn: PersonMapperUpdateWrapper.() -> Unit): Int
fun PersonMapper.updateById(id: Int, fn: PersonMapperUpdateWrapper.() -> Unit): Int

```

使用示例：

```kotlin
//等价于：mapper.selectList(Wrappers.query<Person?>().like("name", "a"))
mapper.query()
    .NAME.like("a")
    .toList()

//等价于：mapper.selectList(Wrappers.query<Person?>().gt("age", 1))
mapper.queryList {
    AGE.gt(1)
}

//等价于：mapper.selectPage(Page(1, 2), Wrappers.query<Person?>().gt("age", 1))
mapper.queryPage(Page(1, 2)) {
    AGE.gt(1)
}

//等价于：mapper.selectOne(Wrappers.query<Person?>().gt("id", 1))
mapper.queryOne {
    ID.eq(1)
}

//等价于：mapper.selectCount(Wrappers.query<Person?>().eq("name", "a"))
mapper.queryCount {
    NAME.eq("a")
}

//等价于：mapper.delete(Wrappers.query<Person?>().eq("id", 1))
mapper.delete()
    .ID.eq(1)
    .delete()

//等价于：mapper.delete(Wrappers.query<Person?>().eq("id", 1))
mapper.delete {
    ID.eq(1)
}

//等价于：mapper.update(null, Wrappers.update<Person?>().set("name", "abc").eq("age", 1))
mapper.update()
    .NAME.set("abc")
    .AGE.eq(1)
    .update()

//等价于：mapper.update(null, Wrappers.update<Person?>().set("name", "abc").eq("age", 1))
mapper.update {
    NAME.set("abc")
    AGE.eq(1)
}

//等价于：mapper.update(null, Wrappers.update<Person?>().set("name", "abc").eq("id", 1))
mapper.updateById(1) {
    NAME.set("abc")
}



```

对于wrapper的包装有两种：

- QueryWrapper
    - 表达式函数：between, eq, ge, gt, in, isNotNull, isNull, le, like, likeLeft, likeRight, lt, ne, notBetween, notIn,
      notLike, notLikeLeft, notLikeRight, and, or
    - 终止方法：toList(), toOne(), toCount(), delete()
- UpdateWrapper
    - 表达式函数：在QueryWrapper的基础上增加了set
    - 终止方法：update()

表达式函数基本都存在三个重载，以eq为例：

```kotlin
//T为字段类型，ME为扩展类型
infix fun eq(value: T): ME

fun eq(condition: Boolean, value: T): ME

fun eq(condition: Boolean, value: () -> T): ME
```

## Q&A

### 为什么需要指定属性名和字段名的转换规则？

因为包装的是QueryWrapper，字段名的确认是编译期行为。

### 为什么包装QueryWrapper而不是KtQueryWrapper?

因为KtQueryWrapper限制了字段只能通过KMutableProperty指定，在当前库已经提供了类型安全和自动补全的情况下这个限制有点多余。

## License

This project is licensed under the Apache 2.0 license - see the LICENSE file for details