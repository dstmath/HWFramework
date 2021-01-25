package com.huawei.nb.container;

public interface Container<E> {
    boolean add(E e);

    void clear();

    boolean delete(E e);

    boolean remove(E e);
}
