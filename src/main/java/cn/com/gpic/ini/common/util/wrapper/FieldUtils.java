package cn.com.gpic.ini.common.util.wrapper;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;

public class FieldUtils {

    public static <T> String propertyToField(Func1<T, ?> func) {
        return StrUtil.toUnderlineCase(LambdaUtil.getFieldName(func));
    }
}
