package cn.com.gpic.ini.common.util.wrapper.condition.util.items;

import java.util.NoSuchElementException;

abstract class AbstractEmptyIterator<E> {

    protected AbstractEmptyIterator() {
        super();
    }

    public boolean hasNext() {
        return false;
    }

    public E next() {
        throw new NoSuchElementException("Iterator contains no elements");
    }

    public boolean hasPrevious() {
        return false;
    }

    public E previous() {
        throw new NoSuchElementException("Iterator contains no elements");
    }

    public int nextIndex() {
        return 0;
    }

    public int previousIndex() {
        return -1;
    }

    public void add(final E obj) {
        throw new UnsupportedOperationException("add() not supported for empty Iterator");
    }

    public void set(final E obj) {
        throw new IllegalStateException("Iterator contains no elements");
    }

    public void remove() {
        throw new IllegalStateException("Iterator contains no elements");
    }

    public void reset() {
        // do nothing
    }
}
