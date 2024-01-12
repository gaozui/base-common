package cn.com.gpic.ini.common.util.wrapper.condition.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SqlOrderBy {

    @AliasFor("isAsc")
    boolean value() default false;

    @AliasFor("value")
    boolean isAsc() default false;

    String column() default "";

    short sortNum() default 1;
}
