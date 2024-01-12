package cn.com.gpic.ini.common.util.wrapper.condition.util.items;

import java.util.Enumeration;

import static cn.com.gpic.ini.common.util.wrapper.condition.util.CollectionUtils.checkIndexBounds;

public class EnumerationUtils {

    /**
     * EnumerationUtils is not normally instantiated.
     */
    private EnumerationUtils() {}

    public static <T> T get(final Enumeration<T> e, final int index) {
        int i = index;
        checkIndexBounds(i);
        while (e.hasMoreElements()) {
            i--;
            if (i == -1) {
                return e.nextElement();
            } else {
                e.nextElement();
            }
        }
        throw new IndexOutOfBoundsException("Entry does not exist: " + i);
    }
}
