package cn.com.gpic.ini.common.util.wrapper.condition.util;

import cn.com.gpic.ini.common.util.wrapper.condition.util.items.EnumerationUtils;
import cn.com.gpic.ini.common.util.wrapper.condition.util.items.IterableUtils;
import cn.com.gpic.ini.common.util.wrapper.condition.util.items.IteratorUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;


public class CollectionUtils {

    public static Object get(final Object object, final int index) {
        int i = index;
        if (i < 0) {
            throw new IndexOutOfBoundsException("Index cannot be negative: " + i);
        }
        if (object instanceof Map<?,?>) {
            final Map<?, ?> map = (Map<?, ?>) object;
            final Iterator<?> iterator = map.entrySet().iterator();
            return IteratorUtils.get(iterator, i);
        } else if (object instanceof Object[]) {
            return ((Object[]) object)[i];
        } else if (object instanceof Iterator<?>) {
            final Iterator<?> it = (Iterator<?>) object;
            return IteratorUtils.get(it, i);
        } else if (object instanceof Iterable<?>) {
            final Iterable<?> iterable = (Iterable<?>) object;
            return IterableUtils.get(iterable, i);
        } else if (object instanceof Collection<?>) {
            final Iterator<?> iterator = ((Collection<?>) object).iterator();
            return IteratorUtils.get(iterator, i);
        } else if (object instanceof Enumeration<?>) {
            final Enumeration<?> it = (Enumeration<?>) object;
            return EnumerationUtils.get(it, i);
        } else if (object == null) {
            throw new IllegalArgumentException("Unsupported object type: null");
        } else {
            try {
                return Array.get(object, i);
            } catch (final IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
            }
        }
    }

    public static int size(final Object object) {
        if (object == null) {
            return 0;
        }
        int total = 0;
        if (object instanceof Map<?,?>) {
            total = ((Map<?, ?>) object).size();
        } else if (object instanceof Collection<?>) {
            total = ((Collection<?>) object).size();
        } else if (object instanceof Iterable<?>) {
            total = IterableUtils.size((Iterable<?>) object);
        } else if (object instanceof Object[]) {
            total = ((Object[]) object).length;
        } else if (object instanceof Iterator<?>) {
            total = IteratorUtils.size((Iterator<?>) object);
        } else if (object instanceof Enumeration<?>) {
            final Enumeration<?> it = (Enumeration<?>) object;
            while (it.hasMoreElements()) {
                total++;
                it.nextElement();
            }
        } else {
            try {
                total = Array.getLength(object);
            } catch (final IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
            }
        }
        return total;
    }

    public static void checkIndexBounds(final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index cannot be negative: " + index);
        }
    }
}
