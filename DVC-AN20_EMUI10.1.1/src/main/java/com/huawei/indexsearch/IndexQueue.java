package com.huawei.indexsearch;

public interface IndexQueue<T> {
    boolean add(T t);

    int getQueueSize();

    T take();
}
