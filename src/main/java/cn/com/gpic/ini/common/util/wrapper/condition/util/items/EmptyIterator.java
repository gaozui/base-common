package cn.com.gpic.ini.common.util.wrapper.condition.util.items;

import java.util.Iterator;

public class EmptyIterator<E> extends AbstractEmptyIterator<E> implements ResettableIterator<E> {

    @SuppressWarnings("rawtypes")
    public static final ResettableIterator RESETTABLE_INSTANCE = new EmptyIterator<>();

    @SuppressWarnings("rawtypes")
    public static final Iterator INSTANCE = RESETTABLE_INSTANCE;

    @SuppressWarnings("unchecked")
    public static <E> ResettableIterator<E> resettableEmptyIterator() {
        return (ResettableIterator<E>) RESETTABLE_INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <E> Iterator<E> emptyIterator() {
        return (Iterator<E>) INSTANCE;
    }

    /**
     * Constructor.
     */
    protected EmptyIterator() {
        super();
    }

}
