# base_common

## 📚系统简介

系统模块·通用模块

## 💡依赖引用

        <dependency>
            <groupId>cn.com.gpic.lzk-yjj</groupId>
            <artifactId>common</artifactId>
            <version>1.0.0</version>
        </dependency>

## 📝内置功能

1.  通用模块：WrapperUtils、SqlCondition、ExcelUtils相关工具类集合。

## 🍊目录结构

        main                                            # 主目录
        ├── java                                        # Java代码
        │   └── cn
        │       └── com
        │           └── gpic
        │               └── ini
        │                   └── common                  # 通用模块目类
        └── resources                                   # 资源文件目录
            └── mapper                                  # mapper-xml文件

## 📐主要jar包说明
| 包名                | 内容                  |
|----------------------------|--------------------------|
| stream-plugin-mybatis-plus | 对mybatis-plus进行封装        |
| stream-core                | 对Optional的优化和对Stream流的封装 |
| mybatis-plus-join          | 支持mybatis-plus连表查询       |
| easyexcel                  | alibaba解析Excel工具         |
| mapstruct                  | 实现Java Bean之间转换的扩展映射     |
| hutool                     | 小而全的Java工具类库             |

## 🐾更新日志

    2023-06-06 项目初始化