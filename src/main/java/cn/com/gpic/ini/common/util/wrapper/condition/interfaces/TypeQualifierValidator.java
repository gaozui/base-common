package cn.com.gpic.ini.common.util.wrapper.condition.interfaces;

import cn.com.gpic.ini.common.util.wrapper.condition.annotations.Nonnull;
import cn.com.gpic.ini.common.util.wrapper.condition.domain.When;

import java.lang.annotation.Annotation;

public interface TypeQualifierValidator<A extends Annotation> {
    @Nonnull
    When forConstantValue(@Nonnull A var1, Object var2);
}
