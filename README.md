# mybatis-plus-wrapper-ext

使用kotlin实现的针对mybatis-plus的条件构造器的扩展库。主要目的是提供类型安全的api和更好的自动补全体验。

## 快速开始

在包含mapper的模块中添加依赖：

```
plugins {
  id("com.google.devtools.ksp") version "1.9.10-1.0.13"
}

dependencies {
    implementation("io.github.afezeria:mybatis-plus-wrapper-ext-runtime:1.0.0")
    ksp("io.github.afezeria:mybatis-plus-wrapper-ext-processor:1.0.0")
}

```

添加配置：

```groovy
ksp{
  arg("dbNamingConvention","FIELD_NAME")
  arg("ignoreMarkerAnnotation","com.example.IgnoreTag")
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

执行构建命令生成代码：

```shell
./gradlew build
```

