package cn.com.gpic.ini.common.util.wrapper.condition.util;

import cn.com.gpic.ini.common.util.wrapper.condition.annotations.Nullable;
import cn.hutool.core.util.ClassUtil;

import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.*;

public class ClassUtils extends ClassUtil {
    private ClassUtils() {
        throw new AssertionError();
    }

    public static boolean isCollectionType(@Nullable Class<?> type) {
        if (type == null) {
            return false;
        } else {
            return type.isArray() || Map.class.isAssignableFrom(type) || Iterable.class.isAssignableFrom(type) || Iterator.class.isAssignableFrom(type) || Enumeration.class.isAssignableFrom(type);
        }
    }

    public static boolean isSimpleProperty(@Nullable Class<?> type) {
        if (type == null) {
            return false;
        } else {
            return isSimpleValueType(type) || type.isArray() && isSimpleValueType(type.getComponentType());
        }
    }

    public static boolean isSimpleValueType(@Nullable Class<?> type) {
        if (type == null) {
            return false;
        } else {
            return Void.class != type && Void.TYPE != type && (isBasicType(type) || Enum.class.isAssignableFrom(type) || CharSequence.class.isAssignableFrom(type) || Number.class.isAssignableFrom(type) || Date.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type) || URI.class == type || URL.class == type || Locale.class == type || Class.class == type);
        }
    }
}
