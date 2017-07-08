package com.android.okhttp;

import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.http.HttpEngine;
import com.squareup.okhttp.Call.AsyncCall;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class Dispatcher {
    private final Deque<Call> executedCalls;
    private ExecutorService executorService;
    private int maxRequests;
    private int maxRequestsPerHost;
    private final Deque<AsyncCall> readyCalls;
    private final Deque<AsyncCall> runningCalls;

    public Dispatcher(ExecutorService executorService) {
        this.maxRequests = 64;
        this.maxRequestsPerHost = 5;
        this.readyCalls = new ArrayDeque();
        this.runningCalls = new ArrayDeque();
        this.executedCalls = new ArrayDeque();
        this.executorService = executorService;
    }

    public Dispatcher() {
        this.maxRequests = 64;
        this.maxRequestsPerHost = 5;
        this.readyCalls = new ArrayDeque();
        this.runningCalls = new ArrayDeque();
        this.executedCalls = new ArrayDeque();
    }

    public synchronized ExecutorService getExecutorService() {
        if (this.executorService == null) {
            this.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp Dispatcher", false));
        }
        return this.executorService;
    }

    public synchronized void setMaxRequests(int maxRequests) {
        if (maxRequests < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequests);
        }
        this.maxRequests = maxRequests;
        promoteCalls();
    }

    public synchronized int getMaxRequests() {
        return this.maxRequests;
    }

    public synchronized void setMaxRequestsPerHost(int maxRequestsPerHost) {
        if (maxRequestsPerHost < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequestsPerHost);
        }
        this.maxRequestsPerHost = maxRequestsPerHost;
        promoteCalls();
    }

    public synchronized int getMaxRequestsPerHost() {
        return this.maxRequestsPerHost;
    }

    synchronized void enqueue(AsyncCall call) {
        if (this.runningCalls.size() >= this.maxRequests || runningCallsForHost(call) >= this.maxRequestsPerHost) {
            this.readyCalls.add(call);
        } else {
            this.runningCalls.add(call);
            getExecutorService().execute(call);
        }
    }

    public synchronized void cancel(Object tag) {
        for (AsyncCall call : this.readyCalls) {
            if (Util.equal(tag, call.tag())) {
                call.cancel();
            }
        }
        for (AsyncCall call2 : this.runningCalls) {
            if (Util.equal(tag, call2.tag())) {
                call2.get().canceled = true;
                HttpEngine engine = call2.get().engine;
                if (engine != null) {
                    engine.disconnect();
                }
            }
        }
        for (Call call3 : this.executedCalls) {
            if (Util.equal(tag, call3.tag())) {
                call3.cancel();
            }
        }
    }

    synchronized void finished(AsyncCall call) {
        if (this.runningCalls.remove(call)) {
            promoteCalls();
        } else {
            throw new AssertionError("AsyncCall wasn't running!");
        }
    }

    private void promoteCalls() {
        if (this.runningCalls.size() < this.maxRequests && !this.readyCalls.isEmpty()) {
            Iterator<AsyncCall> i = this.readyCalls.iterator();
            while (i.hasNext()) {
                AsyncCall call = (AsyncCall) i.next();
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

    private int runningCallsForHost(AsyncCall call) {
        int result = 0;
        for (AsyncCall c : this.runningCalls) {
            if (c.host().equals(call.host())) {
                result++;
            }
        }
        return result;
    }

    synchronized void executed(Call call) {
        this.executedCalls.add(call);
    }

    synchronized void finished(Call call) {
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
