package cn.com.gpic.ini.common.util.wrapper.condition.interfaces;

import cn.com.gpic.ini.common.util.wrapper.condition.annotations.Nonnull;
import cn.com.gpic.ini.common.util.wrapper.condition.annotations.Nullable;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;

import java.io.Serializable;

@FunctionalInterface
public interface SqlSymbolAbstractWrapperConsumer extends Serializable {
    void accept(@Nonnull AbstractWrapper wrapper, @Nonnull String column, @Nullable Object value);
}
