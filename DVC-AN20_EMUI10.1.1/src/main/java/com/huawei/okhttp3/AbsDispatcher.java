package com.huawei.okhttp3;

import com.huawei.okhttp3.RealCall;

public abstract class AbsDispatcher implements iDispatcher {
    /* access modifiers changed from: package-private */
    public abstract void addHttp2Host(String str, int i, String str2);

    /* access modifiers changed from: package-private */
    public abstract void enqueue(RealCall.AsyncCall asyncCall);

    /* access modifiers changed from: package-private */
    public abstract void executed(RealCall realCall);

    /* access modifiers changed from: package-private */
    public abstract void finished(RealCall.AsyncCall asyncCall);

    /* access modifiers changed from: package-private */
    public abstract void finished(RealCall realCall);

    /* access modifiers changed from: package-private */
    public abstract int getMaxHttp2ConnectionPerHost();

    /* access modifiers changed from: package-private */
    public abstract void removeHttp2Host(String str, int i, String str2);
}
