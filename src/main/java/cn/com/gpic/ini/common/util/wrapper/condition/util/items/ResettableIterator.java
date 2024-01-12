package cn.com.gpic.ini.common.util.wrapper.condition.util.items;

import java.util.Iterator;

public interface ResettableIterator<E> extends Iterator<E> {

    void reset();
}
