package com.huawei.okhttp3;

public abstract class AbsDispatcher implements iDispatcher {
    abstract void addHttp2Host(String str, int i, String str2);

    abstract void enqueue(AsyncCall asyncCall);

    abstract void executed(RealCall realCall);

    abstract void finished(AsyncCall asyncCall);

    abstract void finished(RealCall realCall);

    abstract int getMaxHttp2ConnectionPerHost();

    abstract void removeHttp2Host(String str, int i, String str2);
}
