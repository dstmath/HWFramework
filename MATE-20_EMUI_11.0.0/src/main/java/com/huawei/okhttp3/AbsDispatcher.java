package com.huawei.okhttp3;

import com.huawei.okhttp3.RealCall;

public abstract class AbsDispatcher implements iDispatcher {
    static final int MAX_HTTP2_CONNECTION_PER_HOST = 1;

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
    public void addHttp2Host(String hostName, int port, String scheme) {
    }

    /* access modifiers changed from: package-private */
    public void removeHttp2Host(String hostName, int port, String scheme) {
    }

    /* access modifiers changed from: protected */
    public int runningCallsForHost(RealCall.AsyncCall call) {
        if (call != null) {
            return call.callsPerHost().get();
        }
        return 0;
    }
}
