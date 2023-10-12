# mybatis-plus-wrapper-ext

使用kotlin实现的针对mybatis-plus的条件构造器的扩展库。主要目的是提供类型安全的api和更好的自动补全体验。

## 快速开始

### 安装

#### gradle

```
plugins {
  id("com.google.devtools.ksp") version "1.9.10-1.0.13"
}

dependencies {
    implementation("io.github.afezeria:mybatis-plus-wrapper-ext-runtime:1.5.0")
    ksp("io.github.afezeria:mybatis-plus-wrapper-ext-processor:1.5.0")
}

```

### maven

```xml

<dependency>
    <groupId>io.github.afezeria</groupId>
    <artifactId>mybatis-plus-wrapper-ext-runtime</artifactId>
    <version>1.5.0</version>
</dependency>
```

```xml

<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>compile</id>
            <phase>compile</phase>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <compilerPlugins>
            <compilerPlugin>ksp</compilerPlugin>
        </compilerPlugins>
        <pluginOptions>
            <option>ksp:kotlinOutputDir=${project.basedir}/target/generated-sources/annotations</option>
        </pluginOptions>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>com.dyescape</groupId>
            <artifactId>kotlin-maven-symbol-processing</artifactId>
            <version>1.6</version>
        </dependency>
        <dependency>
            <groupId>io.github.afezeria</groupId>
            <artifactId>mybatis-plus-wrapper-ext-processor</artifactId>
            <version>1.5.0</version>
        </dependency>
    </dependencies>
</plugin>
```

### 配置

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

定义实体及mapper：

```kotlin
@TableName("person")
class Person {
    @TableId("id", type = IdType.AUTO)
    var id: Int? = null
    var name: String? = null
    var age: Int? = null
    var createTime: LocalDateTime? = null
    var updateTime: LocalDateTime? = null
}

interface PersonMapper : BaseMapper<Person>
```

### 扩展函数

以上定义在编译时会生成具有下述签名的扩展函数：

```kotlin
fun PersonMapper.query(): PersonMapperQueryWrapper
fun PersonMapper.queryList(fn: PersonMapperQueryWrapper.() -> Unit): List<Person>
fun <F> PersonMapper.querySingleField(
    columnFn: PersonMapperQueryWrapper.() -> FieldDefinition<PersonMapperQueryWrapper, *, F>,
    fn: PersonMapperQueryWrapper.() -> Unit
): List<F>
fun <F1, F2> PersonMapper.queryPair(
    columnsFn: PersonMapperQueryWrapper.() -> Pair<FieldDefinition<PersonMapperQueryWrapper, *, F1>, FieldDefinition<PersonMapperQueryWrapper, *, F2>>,
    fn: PersonMapperQueryWrapper.() -> Unit
): List<Pair<F1, F2>>
fun <F1, F2, F3> PersonMapper.queryTriple(
    columnsFn: PersonMapperQueryWrapper.() -> Triple<FieldDefinition<PersonMapperQueryWrapper, *, F1>, FieldDefinition<PersonMapperQueryWrapper, *, F2>, FieldDefinition<PersonMapperQueryWrapper, *, F3>>,
    fn: PersonMapperQueryWrapper.() -> Unit
): List<Triple<F1, F2, F3>>
fun <P : IPage<Person>> PersonMapper.queryPage(page: P, fn: PersonMapperQueryWrapper.() -> Unit): P
fun PersonMapper.queryOne(fn: PersonMapperQueryWrapper.() -> Unit): Person?
fun PersonMapper.queryCount(fn: PersonMapperQueryWrapper.() -> Unit): Long
fun PersonMapper.delete(): PersonMapperQueryWrapper
fun PersonMapper.delete(fn: PersonMapperQueryWrapper.() -> Unit): Int
fun PersonMapper.update(): PersonMapperUpdateWrapper
fun PersonMapper.update(fn: PersonMapperUpdateWrapper.() -> Unit): Int
fun PersonMapper.updateById(id: Int, fn: PersonMapperUpdateWrapper.() -> Unit): Int
```

### 包装器

扩展函数签名中的`PersonMapperQueryWrapper`和`PersonMapperUpdateWrapper`是对mybatis-plus原生`QueryWrapper`
和`UpdateWrapper`类的包装，
包含了字段名和类型等信息，两种类的具体差别如下：

- QueryWrapper
    - 表达式函数：between, eq, ge, gt, in, isNotNull, isNull, le, like, likeLeft, likeRight, lt, ne, notBetween, notIn,
      notLike, notLikeLeft, notLikeRight, and, or
    - 终止方法：toList(), toOne(), toCount(), delete()(不建议在`mapper.query()`后调用delete方法，请使用`mapper.delete()`)
    - 获取`QueryWrapper`对象：wrapper
- UpdateWrapper
    - 表达式函数：在QueryWrapper的基础上增加了set
    - 终止方法：update()
    - 获取`UpdateWrapper`对象：wrapper

### 表达式函数

表达式函数基本都存在三个重载，以eq为例：

```kotlin
infix fun eq(value: T): ME

fun eq(condition: Boolean, value: T): ME

fun eq(condition: Boolean, value: () -> T): ME
```

T为字段类型范型参数，ME为包装类型范型参数。除了参数类型为集合的in函数外，其他表达式函数的表达字段类型的范型参数都是非空类型

### 示例

```kotlin
import java.time.LocalDateTime

//等价于：mapper.selectList(Wrappers.query<Person?>().like("name", "a"))
mapper.query()
    .NAME.like("a")
    .toList()

//等价于：mapper.selectList(Wrappers.query<Person?>().gt("age", 1))
mapper.queryList {
    AGE.gt(1)
}

//等价于：
//mapper.selectList(
//    Wrappers.query<Person?>().apply {
//        gt("age", 1)
//        if (createTimeStr != null) {
//            LocalDateTime.parse(createTimeStr!!)
//        }
//    }
//)
mapper.queryList {
    AGE.gt(1)
    CREATE_TIME.gt(createTimeStr != null) {
        LocalDateTime.parse(createTimeStr!!)
    }
}

//等价于：mapper.selectList(Wrappers.query<Person?>().gt("id", 1).and { it.gt("age", 1) })
mapper.queryList {
    ID.gt(1)
    and {
        AGE gt 1
    }
}

//等价于：mapper.selectList(Wrappers.query<Person?>().gt("id", 1).or { it.gt("age", 1) })
mapper.queryList {
    ID.gt(1)
    or()
    AGE.gt(1)
}
mapper.queryList {
    ID.gt(1)
    or {
        AGE.gt(1)
    }
}


//等价于：
//mapper.selectMaps(
//    Wrappers.query<Person?>()
//        .select("`name`")
//        .isNotNull("`name`")
//).map { it?.get("name") as String }
mapper.querySingleField({ NAME.markNotNull }) {
    NAME.isNotNull()
}

//等价于：
//mapper.selectMaps(
//    Wrappers.query<Person?>()
//        .select("`name`", "age")
//        .gt("id", 1)
//).map {
//    Pair(it?.get("name") as String?, it?.get("age") as Int?)
//}
mapper.queryPair({ NAME to AGE }) {
    ID.gt(1)
}

//等价于：
//mapper.selectMaps(
//    Wrappers.query<Person?>()
//        .select("id", "`name`", "age")
//).map {
//    Triple(it?.get("id") as Int?, it?.get("name") as String?, it?.get("age") as Int?)
//}
mapper.queryTriple({ Triple(ID, NAME, AGE) }) {}

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

## Q&A

### 为什么需要指定属性名和字段名的转换规则？

因为包装的是QueryWrapper，字段名的确认是编译期行为。

### 为什么包装QueryWrapper而不是KtQueryWrapper?

因为KtQueryWrapper限制了字段只能通过KMutableProperty指定，在已经提供了类型安全和自动补全的情况下不再需要这个限制。

## License

This project is licensed under the Apache 2.0 license - see the LICENSE file for details
