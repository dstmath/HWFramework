package com.huawei.nb.notification;

public interface Variable<T> {
    T getChangedData();

    ObserverType getObserverType();
}
