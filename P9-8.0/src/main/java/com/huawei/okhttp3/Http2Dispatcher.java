package com.huawei.okhttp3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import okhttp3.RealCall.AsyncCall;

public final class Http2Dispatcher extends Dispatcher {
    private static final int HTTP1_REQUEST_PER_HOST_DEFAULT = 5;
    private static final int HTTP2_CONNECTION_PER_HOST_DEFAULT = 2;
    private static final int HTTP2_REQUEST_PER_HOST_DEFAULT = 32;
    private int http2ConnectionOnDemandFactor = 2;
    private final List<Http2HostInfo> http2Hosts = new ArrayList();
    private final Object lock = new Object();
    private int maxHttp1RequestsPerHost = 5;
    private int maxHttp2ConnectionPerHost = 2;
    private int maxHttp2RequestsPerHost = 32;

    private final class Http2HostInfo {
        static final /* synthetic */ boolean -assertionsDisabled = (Http2HostInfo.class.desiredAssertionStatus() ^ 1);
        final /* synthetic */ boolean $assertionsDisabled;
        public int callAmount;
        public int connectionAmount = 1;
        public String hostName;
        public int port;
        public int runningCallAmount;
        public String scheme;

        public Http2HostInfo(String hostName, int port, String scheme) {
            this.hostName = hostName;
            this.port = port;
            this.scheme = scheme;
            updateCallAmountForHost(hostName, port, scheme);
        }

        private void updateCallAmountForHost(String hostName, int port, String scheme) {
            if (-assertionsDisabled || Thread.holdsLock(Http2Dispatcher.this.lock)) {
                HttpUrl url;
                int result = 0;
                for (AsyncCall c : Http2Dispatcher.this.runningAsyncCalls) {
                    url = c.request().url();
                    if (url.host().equals(hostName) && url.port() == port && url.scheme().equals(scheme)) {
                        result++;
                    }
                }
                this.runningCallAmount = result;
                for (AsyncCall c2 : Http2Dispatcher.this.readyAsyncCalls) {
                    url = c2.request().url();
                    if (url.host().equals(hostName) && url.port() == port && url.scheme().equals(scheme)) {
                        result++;
                    }
                }
                this.callAmount = result;
                return;
            }
            throw new AssertionError();
        }
    }

    public Http2Dispatcher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setMaxHttp1RequestsPerHost(int maxHttp1RequestsPerHost) {
        synchronized (this.lock) {
            if (maxHttp1RequestsPerHost < 1) {
                throw new IllegalArgumentException("max < 1: " + maxHttp1RequestsPerHost);
            }
            this.maxHttp1RequestsPerHost = maxHttp1RequestsPerHost;
            promoteCalls();
        }
    }

    public int getMaxHttp1RequestsPerHost() {
        int i;
        synchronized (this.lock) {
            i = this.maxHttp1RequestsPerHost;
        }
        return i;
    }

    public void setMaxHttp2RequestsPerHost(int maxHttp2RequestsPerHost) {
        synchronized (this.lock) {
            if (maxHttp2RequestsPerHost < 1) {
                throw new IllegalArgumentException("max < 1: " + maxHttp2RequestsPerHost);
            }
            this.maxHttp2RequestsPerHost = maxHttp2RequestsPerHost;
            promoteCalls();
        }
    }

    public int getMaxHttp2RequestsPerHost() {
        int i;
        synchronized (this.lock) {
            i = this.maxHttp2RequestsPerHost;
        }
        return i;
    }

    public void setMaxHttp2ConnectionPerHost(int maxHttp2ConnectionPerHost) {
        if (maxHttp2ConnectionPerHost < 1) {
            throw new IllegalArgumentException("max < 1: " + maxHttp2ConnectionPerHost);
        }
        this.maxHttp2ConnectionPerHost = maxHttp2ConnectionPerHost;
    }

    public int getMaxHttp2ConnectionPerHost() {
        return this.maxHttp2ConnectionPerHost;
    }

    /* JADX WARNING: Missing block: B:10:0x001f, code:
            if (r2 != 0) goto L_0x0026;
     */
    /* JADX WARNING: Missing block: B:11:0x0021, code:
            if (r0 == null) goto L_0x0026;
     */
    /* JADX WARNING: Missing block: B:12:0x0023, code:
            r0.run();
     */
    /* JADX WARNING: Missing block: B:13:0x0026, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addHttp2Host(String hostName, int port, String scheme) {
        synchronized (this.lock) {
            if (getHttp2HostInfo(hostName, port, scheme) != null) {
                return;
            }
            this.http2Hosts.add(new Http2HostInfo(hostName, port, scheme));
            promoteCalls();
            int runningCallsCount = runningCallsCount();
            Runnable idleCallback = this.idleCallback;
        }
    }

    public void removeHttp2Host(String hostName, int port, String scheme) {
        synchronized (this.lock) {
            Iterator<okhttp3.Http2Dispatcher.Http2HostInfo> iter = this.http2Hosts.iterator();
            while (iter.hasNext()) {
                Http2HostInfo h = (Http2HostInfo) iter.next();
                if (h.hostName.equals(hostName) && h.port == port && h.scheme.equals(scheme)) {
                    iter.remove();
                    return;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0015, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void enqueue(AsyncCall call) {
        synchronized (this.lock) {
            if (call == null) {
                return;
            }
            Http2HostInfo http2HostInfo = getHttp2HostInfo(call.request());
            if (http2HostInfo != null) {
                enqueueHttp2Calls(call, http2HostInfo);
            } else {
                enqueueHttp1Calls(call);
            }
        }
    }

    private void enqueueHttp2Calls(AsyncCall call, Http2HostInfo http2HostInfo) {
        if (http2HostInfo.callAmount == 0) {
            HttpUrl url = call.request().url();
            if (!call.client().keepHttp2ConnectionAlive(url.host(), url.port(), url.scheme())) {
                removeHttp2Host(url.host(), url.port(), url.scheme());
                enqueueHttp1Calls(call);
                return;
            }
        }
        http2HostInfo.callAmount++;
        call.request().setHttp2Indicator();
        if (this.runningAsyncCalls.size() >= getMaxRequests() || runningHttp2CallsForHost(call) >= this.maxHttp2RequestsPerHost) {
            this.readyAsyncCalls.add(call);
            http2ConnectionOnDemand(call, http2HostInfo);
            return;
        }
        this.runningAsyncCalls.add(call);
        http2HostInfo.runningCallAmount++;
        executorService().execute(call);
    }

    private void enqueueHttp1Calls(AsyncCall call) {
        if (this.runningAsyncCalls.size() >= getMaxRequests() || runningHttp1CallsForHost(call) >= this.maxHttp1RequestsPerHost) {
            this.readyAsyncCalls.add(call);
            return;
        }
        this.runningAsyncCalls.add(call);
        executorService().execute(call);
    }

    private void promoteCalls() {
        if (this.runningAsyncCalls.size() < getMaxRequests() && !this.readyAsyncCalls.isEmpty()) {
            Iterator<AsyncCall> i = this.readyAsyncCalls.iterator();
            while (i.hasNext()) {
                int maxHttpRequestsPerHost;
                int runningCalls;
                AsyncCall call = (AsyncCall) i.next();
                Http2HostInfo hostInfo = getHttp2HostInfo(call.request());
                if (hostInfo != null) {
                    maxHttpRequestsPerHost = this.maxHttp2RequestsPerHost;
                    runningCalls = runningHttp2CallsForHost(call);
                } else {
                    maxHttpRequestsPerHost = this.maxHttp1RequestsPerHost;
                    runningCalls = runningHttp1CallsForHost(call);
                }
                if (runningCalls < maxHttpRequestsPerHost) {
                    i.remove();
                    this.runningAsyncCalls.add(call);
                    if (hostInfo != null) {
                        hostInfo.runningCallAmount++;
                    }
                    executorService().execute(call);
                }
                if (this.runningAsyncCalls.size() >= getMaxRequests()) {
                    return;
                }
            }
        }
    }

    void finished(AsyncCall call) {
        synchronized (this.lock) {
            Http2HostInfo http2HostInfo = getHttp2HostInfo(call.request());
            if (http2HostInfo != null) {
                http2HostInfo.callAmount--;
                http2HostInfo.runningCallAmount--;
                if (call.forCreateConnectionOnly()) {
                    HttpUrl url = call.request().url();
                    http2HostInfo.connectionAmount = call.client().http2ConnectionCount(url.host(), url.port(), url.scheme());
                }
            }
        }
        finished(this.runningAsyncCalls, call, true);
    }

    void finished(RealCall call) {
        finished(this.runningSyncCalls, call, false);
    }

    private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {
        int runningCallsCount;
        Runnable idleCallback;
        synchronized (this.lock) {
            if (calls.remove(call)) {
                if (promoteCalls) {
                    promoteCalls();
                }
                runningCallsCount = runningCallsCount();
                idleCallback = this.idleCallback;
            } else {
                throw new AssertionError("Call wasn't in-flight!");
            }
        }
        if (runningCallsCount == 0 && idleCallback != null) {
            idleCallback.run();
        }
    }

    private int runningHttp2CallsForHost(AsyncCall call) {
        int result = 0;
        for (Http2HostInfo hostInfo : this.http2Hosts) {
            if (hostInfo.hostName.equals(call.host())) {
                result += hostInfo.runningCallAmount;
            }
        }
        return result;
    }

    private int runningHttp1CallsForHost(AsyncCall call) {
        return runningCallsForHost(call) - runningHttp2CallsForHost(call);
    }

    private Http2HostInfo getHttp2HostInfo(Request request) {
        return getHttp2HostInfo(request.url().host(), request.url().port(), request.url().scheme());
    }

    private Http2HostInfo getHttp2HostInfo(String hostName, int port, String scheme) {
        for (Http2HostInfo h : this.http2Hosts) {
            if (h.hostName.equals(hostName) && h.port == port && h.scheme.equals(scheme)) {
                return h;
            }
        }
        return null;
    }

    private void http2ConnectionOnDemand(AsyncCall call, Http2HostInfo http2HostInfo) {
        if (http2HostInfo.callAmount == this.maxHttp2RequestsPerHost + 1) {
            HttpUrl url = call.request().url();
            http2HostInfo.connectionAmount = call.client().http2ConnectionCount(url.host(), url.port(), url.scheme());
        }
        int connectionNum = http2HostInfo.connectionAmount;
        if ((this.maxHttp2RequestsPerHost * connectionNum) * this.http2ConnectionOnDemandFactor < http2HostInfo.callAmount && connectionNum < this.maxHttp2ConnectionPerHost) {
            Call tempCall = call.client().newCall(call.request().newBuilder().header("Http2ConnectionIndex", Integer.toString(connectionNum + 1)).build());
            if (tempCall instanceof RealCall) {
                RealCall connectCall = (RealCall) tempCall;
                http2HostInfo.connectionAmount++;
                http2HostInfo.runningCallAmount++;
                Callback callback = new Callback() {
                    public void onFailure(Call call, IOException e) {
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                    }
                };
                connectCall.getClass();
                AsyncCall asyncConnectCall = new AsyncCall(callback);
                asyncConnectCall.setForCreateConnectionOnly();
                this.runningAsyncCalls.add(asyncConnectCall);
                executorService().execute(asyncConnectCall);
            }
        }
    }
}
