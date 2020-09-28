package com.huawei.okhttp3;

import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.okhttp3.RealCall;
import com.huawei.okhttp3.internal.Util;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

public class Dispatcher extends AbsDispatcher {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int MAX_HTTP2_CONNECTION_PER_HOST = 1;
    @Nullable
    protected ExecutorService executorService;
    @Nullable
    protected Runnable idleCallback;
    protected int maxRequests = 64;
    private int maxRequestsPerHost = 5;
    protected final Deque<RealCall.AsyncCall> readyAsyncCalls = new ArrayDeque();
    protected final Deque<RealCall.AsyncCall> runningAsyncCalls = new ArrayDeque();
    protected final Deque<RealCall> runningSyncCalls = new ArrayDeque();

    public Dispatcher(ExecutorService executorService2) {
        this.executorService = executorService2;
    }

    public Dispatcher() {
    }

    public synchronized ExecutorService executorService() {
        if (this.executorService == null) {
            this.executorService = new ThreadPoolExecutor(0, (int) SignalStrengthEx.INVALID, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp Dispatcher", false));
        }
        return this.executorService;
    }

    @Override // com.huawei.okhttp3.iDispatcher
    public void setMaxRequests(int maxRequests2) {
        if (maxRequests2 >= 1) {
            synchronized (this) {
                this.maxRequests = maxRequests2;
            }
            promoteAndExecute();
            return;
        }
        throw new IllegalArgumentException("max < 1: " + maxRequests2);
    }

    @Override // com.huawei.okhttp3.iDispatcher
    public synchronized int getMaxRequests() {
        return this.maxRequests;
    }

    @Override // com.huawei.okhttp3.iDispatcher
    public void setMaxRequestsPerHost(int maxRequestsPerHost2) {
        if (maxRequestsPerHost2 >= 1) {
            synchronized (this) {
                this.maxRequestsPerHost = maxRequestsPerHost2;
            }
            promoteAndExecute();
            return;
        }
        throw new IllegalArgumentException("max < 1: " + maxRequestsPerHost2);
    }

    @Override // com.huawei.okhttp3.iDispatcher
    public synchronized int getMaxRequestsPerHost() {
        return this.maxRequestsPerHost;
    }

    @Override // com.huawei.okhttp3.iDispatcher
    public synchronized void setIdleCallback(@Nullable Runnable idleCallback2) {
        this.idleCallback = idleCallback2;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.okhttp3.AbsDispatcher
    public void enqueue(RealCall.AsyncCall call) {
        synchronized (this) {
            this.readyAsyncCalls.add(call);
        }
        promoteAndExecute();
    }

    @Override // com.huawei.okhttp3.iDispatcher
    public synchronized void cancelAll() {
        for (RealCall.AsyncCall call : this.readyAsyncCalls) {
            call.get().cancel();
        }
        for (RealCall.AsyncCall call2 : this.runningAsyncCalls) {
            call2.get().cancel();
        }
        for (RealCall call3 : this.runningSyncCalls) {
            call3.cancel();
        }
    }

    private boolean promoteAndExecute() {
        boolean isRunning;
        List<RealCall.AsyncCall> executableCalls = new ArrayList<>();
        synchronized (this) {
            Iterator<RealCall.AsyncCall> i = this.readyAsyncCalls.iterator();
            while (true) {
                if (!i.hasNext()) {
                    break;
                }
                RealCall.AsyncCall asyncCall = i.next();
                if (this.runningAsyncCalls.size() >= this.maxRequests) {
                    break;
                } else if (runningCallsForHost(asyncCall) < this.maxRequestsPerHost) {
                    i.remove();
                    executableCalls.add(asyncCall);
                    this.runningAsyncCalls.add(asyncCall);
                }
            }
            isRunning = runningCallsCount() > 0;
        }
        int size = executableCalls.size();
        for (int i2 = 0; i2 < size; i2++) {
            executableCalls.get(i2).executeOn(executorService());
        }
        return isRunning;
    }

    /* access modifiers changed from: protected */
    public int runningCallsForHost(RealCall.AsyncCall call) {
        int result = 0;
        for (RealCall.AsyncCall c : this.runningAsyncCalls) {
            if (!c.get().forWebSocket && c.host().equals(call.host())) {
                result++;
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.okhttp3.AbsDispatcher
    public synchronized void executed(RealCall call) {
        this.runningSyncCalls.add(call);
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.okhttp3.AbsDispatcher
    public void finished(RealCall.AsyncCall call) {
        finished(this.runningAsyncCalls, call);
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.okhttp3.AbsDispatcher
    public void finished(RealCall call) {
        finished(this.runningSyncCalls, call);
    }

    private <T> void finished(Deque<T> calls, T call) {
        Runnable idleCallback2;
        synchronized (this) {
            if (calls.remove(call)) {
                idleCallback2 = this.idleCallback;
            } else {
                throw new AssertionError("Call wasn't in-flight!");
            }
        }
        if (!promoteAndExecute() && idleCallback2 != null) {
            idleCallback2.run();
        }
    }

    @Override // com.huawei.okhttp3.iDispatcher
    public synchronized List<Call> queuedCalls() {
        List<Call> result;
        result = new ArrayList<>();
        for (RealCall.AsyncCall asyncCall : this.readyAsyncCalls) {
            result.add(asyncCall.get());
        }
        return Collections.unmodifiableList(result);
    }

    @Override // com.huawei.okhttp3.iDispatcher
    public synchronized List<Call> runningCalls() {
        List<Call> result;
        result = new ArrayList<>();
        result.addAll(this.runningSyncCalls);
        for (RealCall.AsyncCall asyncCall : this.runningAsyncCalls) {
            result.add(asyncCall.get());
        }
        return Collections.unmodifiableList(result);
    }

    @Override // com.huawei.okhttp3.iDispatcher
    public synchronized int queuedCallsCount() {
        return this.readyAsyncCalls.size();
    }

    @Override // com.huawei.okhttp3.iDispatcher
    public synchronized int runningCallsCount() {
        return this.runningAsyncCalls.size() + this.runningSyncCalls.size();
    }

    @Override // com.huawei.okhttp3.AbsDispatcher
    public int getMaxHttp2ConnectionPerHost() {
        return 1;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.okhttp3.AbsDispatcher
    public void addHttp2Host(String hostName, int port, String scheme) {
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.okhttp3.AbsDispatcher
    public void removeHttp2Host(String hostName, int port, String scheme) {
    }
}
