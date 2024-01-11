
package cn.com.gpic.ini.common.util.wrapper.condition.annotations;

import cn.com.gpic.ini.common.util.wrapper.condition.domain.When;
import cn.com.gpic.ini.common.util.wrapper.condition.interfaces.TypeQualifierValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@TypeQualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Nonnull {
    When when() default When.ALWAYS;

    public static class Checker implements TypeQualifierValidator<Nonnull> {
        public Checker() {
        }

        public When forConstantValue(Nonnull qualifierArgument, Object value) {
            return value == null ? When.NEVER : When.ALWAYS;
        }
    }
}
