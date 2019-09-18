package com.android.okhttp;

import com.android.okhttp.Call;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.http.HttpEngine;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class Dispatcher {
    private final Deque<Call> executedCalls = new ArrayDeque();
    private ExecutorService executorService;
    private int maxRequests = 64;
    private int maxRequestsPerHost = 5;
    private final Deque<Call.AsyncCall> readyCalls = new ArrayDeque();
    private final Deque<Call.AsyncCall> runningCalls = new ArrayDeque();

    public Dispatcher(ExecutorService executorService2) {
        this.executorService = executorService2;
    }

    public Dispatcher() {
    }

    public synchronized ExecutorService getExecutorService() {
        if (this.executorService == null) {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp Dispatcher", false));
            this.executorService = threadPoolExecutor;
        }
        return this.executorService;
    }

    public synchronized void setMaxRequests(int maxRequests2) {
        if (maxRequests2 >= 1) {
            this.maxRequests = maxRequests2;
            promoteCalls();
        } else {
            throw new IllegalArgumentException("max < 1: " + maxRequests2);
        }
    }

    public synchronized int getMaxRequests() {
        return this.maxRequests;
    }

    public synchronized void setMaxRequestsPerHost(int maxRequestsPerHost2) {
        if (maxRequestsPerHost2 >= 1) {
            this.maxRequestsPerHost = maxRequestsPerHost2;
            promoteCalls();
        } else {
            throw new IllegalArgumentException("max < 1: " + maxRequestsPerHost2);
        }
    }

    public synchronized int getMaxRequestsPerHost() {
        return this.maxRequestsPerHost;
    }

    /* access modifiers changed from: package-private */
    public synchronized void enqueue(Call.AsyncCall call) {
        if (this.runningCalls.size() >= this.maxRequests || runningCallsForHost(call) >= this.maxRequestsPerHost) {
            this.readyCalls.add(call);
        } else {
            this.runningCalls.add(call);
            getExecutorService().execute(call);
        }
    }

    public synchronized void cancel(Object tag) {
        for (Call.AsyncCall call : this.readyCalls) {
            if (Util.equal(tag, call.tag())) {
                call.cancel();
            }
        }
        for (Call.AsyncCall call2 : this.runningCalls) {
            if (Util.equal(tag, call2.tag())) {
                call2.get().canceled = true;
                HttpEngine engine = call2.get().engine;
                if (engine != null) {
                    engine.cancel();
                }
            }
        }
        for (Call call3 : this.executedCalls) {
            if (Util.equal(tag, call3.tag())) {
                call3.cancel();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void finished(Call.AsyncCall call) {
        if (this.runningCalls.remove(call)) {
            promoteCalls();
        } else {
            throw new AssertionError("AsyncCall wasn't running!");
        }
    }

    private void promoteCalls() {
        if (this.runningCalls.size() < this.maxRequests && !this.readyCalls.isEmpty()) {
            Iterator<Call.AsyncCall> i = this.readyCalls.iterator();
            while (i.hasNext()) {
                Call.AsyncCall call = i.next();
                if (runningCallsForHost(call) < this.maxRequestsPerHost) {
                    i.remove();
                    this.runningCalls.add(call);
                    getExecutorService().execute(call);
                }
                if (this.runningCalls.size() >= this.maxRequests) {
                    return;
                }
            }
        }
    }

    private int runningCallsForHost(Call.AsyncCall call) {
        int result = 0;
        for (Call.AsyncCall c : this.runningCalls) {
            if (c.host().equals(call.host())) {
                result++;
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public synchronized void executed(Call call) {
        this.executedCalls.add(call);
    }

    /* access modifiers changed from: package-private */
    public synchronized void finished(Call call) {
        if (!this.executedCalls.remove(call)) {
            throw new AssertionError("Call wasn't in-flight!");
        }
    }

    public synchronized int getRunningCallCount() {
        return this.runningCalls.size();
    }

    public synchronized int getQueuedCallCount() {
        return this.readyCalls.size();
    }
}
