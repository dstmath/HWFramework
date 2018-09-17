package com.huawei.okhttp3;

public interface iDispatcherFactory {
    AbsDispatcher createDispatcher(Protocol protocol);
}
