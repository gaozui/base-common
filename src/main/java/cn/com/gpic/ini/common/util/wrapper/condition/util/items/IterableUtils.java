package cn.com.gpic.ini.common.util.wrapper.condition.util.items;

import cn.com.gpic.ini.common.util.wrapper.condition.util.CollectionUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class IterableUtils {

    public static <T> T get(final Iterable<T> iterable, final int index) {
        CollectionUtils.checkIndexBounds(index);
        if (iterable instanceof List<?>) {
            return ((List<T>) iterable).get(index);
        }
        return IteratorUtils.get(emptyIteratorIfNull(iterable), index);
    }

    public static int size(final Iterable<?> iterable) {
        if (iterable instanceof Collection<?>) {
            return ((Collection<?>) iterable).size();
        } else {
            return IteratorUtils.size(emptyIteratorIfNull(iterable));
        }
    }

    private static <E> Iterator<E> emptyIteratorIfNull(final Iterable<E> iterable) {
        return iterable != null ? iterable.iterator() : IteratorUtils.emptyIterator();
    }
}
