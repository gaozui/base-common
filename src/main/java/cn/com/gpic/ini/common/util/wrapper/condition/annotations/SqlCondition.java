package cn.com.gpic.ini.common.util.wrapper.condition.annotations;

import cn.com.gpic.ini.common.util.wrapper.condition.domain.SqlLogical;
import cn.com.gpic.ini.common.util.wrapper.condition.domain.SqlSymbol;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SqlCondition {
    @AliasFor("symbol")
    SqlSymbol value() default SqlSymbol.EQ;

    @AliasFor("value")
    SqlSymbol symbol() default SqlSymbol.EQ;

    String[] columns() default {};

    SqlLogical columnLogical() default SqlLogical.OR;
}
