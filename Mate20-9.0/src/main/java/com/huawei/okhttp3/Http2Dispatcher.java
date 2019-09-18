package com.huawei.okhttp3;

import com.huawei.okhttp3.RealCall;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public final class Http2Dispatcher extends Dispatcher {
    private static final int HTTP1_REQUEST_PER_HOST_DEFAULT = 5;
    private static final int HTTP2_CONNECTION_PER_HOST_DEFAULT = 2;
    private static final int HTTP2_REQUEST_PER_HOST_DEFAULT = 32;
    private int http2ConnectionOnDemandFactor = 2;
    private final List<Http2HostInfo> http2Hosts = new ArrayList();
    private int maxHttp1RequestsPerHost = 5;
    private int maxHttp2ConnectionPerHost = 2;
    private int maxHttp2RequestsPerHost = 32;

    private final class Http2HostInfo {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        public int callAmount;
        public int connectionAmount = 1;
        public String hostName;
        public int port;
        public int runningCallAmount;
        public String scheme;

        static {
            Class<Http2Dispatcher> cls = Http2Dispatcher.class;
        }

        public Http2HostInfo(String hostName2, int port2, String scheme2) {
            this.hostName = hostName2;
            this.port = port2;
            this.scheme = scheme2;
            updateCallAmountForHost(hostName2, port2, scheme2);
        }

        private void updateCallAmountForHost(String hostName2, int port2, String scheme2) {
            int result = 0;
            for (RealCall.AsyncCall c : Http2Dispatcher.this.runningAsyncCalls) {
                HttpUrl url = c.request().url();
                if (url.host().equals(hostName2) && url.port() == port2 && url.scheme().equals(scheme2)) {
                    result++;
                }
            }
            this.runningCallAmount = result;
            for (RealCall.AsyncCall c2 : Http2Dispatcher.this.readyAsyncCalls) {
                HttpUrl url2 = c2.request().url();
                if (url2.host().equals(hostName2) && url2.port() == port2 && url2.scheme().equals(scheme2)) {
                    result++;
                }
            }
            this.callAmount = result;
        }
    }

    public Http2Dispatcher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Http2Dispatcher() {
    }

    public void setMaxHttp1RequestsPerHost(int maxHttp1RequestsPerHost2) {
        synchronized (this) {
            if (maxHttp1RequestsPerHost2 >= 1) {
                this.maxHttp1RequestsPerHost = maxHttp1RequestsPerHost2;
                promoteCalls();
            } else {
                throw new IllegalArgumentException("max < 1: " + maxHttp1RequestsPerHost2);
            }
        }
    }

    public int getMaxHttp1RequestsPerHost() {
        int i;
        synchronized (this) {
            i = this.maxHttp1RequestsPerHost;
        }
        return i;
    }

    public void setMaxHttp2RequestsPerHost(int maxHttp2RequestsPerHost2) {
        synchronized (this) {
            if (maxHttp2RequestsPerHost2 >= 1) {
                this.maxHttp2RequestsPerHost = maxHttp2RequestsPerHost2;
                promoteCalls();
            } else {
                throw new IllegalArgumentException("max < 1: " + maxHttp2RequestsPerHost2);
            }
        }
    }

    public int getMaxHttp2RequestsPerHost() {
        int i;
        synchronized (this) {
            i = this.maxHttp2RequestsPerHost;
        }
        return i;
    }

    public void setMaxHttp2ConnectionPerHost(int maxHttp2ConnectionPerHost2) {
        if (maxHttp2ConnectionPerHost2 >= 1) {
            this.maxHttp2ConnectionPerHost = maxHttp2ConnectionPerHost2;
            return;
        }
        throw new IllegalArgumentException("max < 1: " + maxHttp2ConnectionPerHost2);
    }

    public int getMaxHttp2ConnectionPerHost() {
        return this.maxHttp2ConnectionPerHost;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0022, code lost:
        r0.run();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0025, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001e, code lost:
        if (r1 != 0) goto L_0x0025;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0020, code lost:
        if (r0 == null) goto L_0x0025;
     */
    public void addHttp2Host(String hostName, int port, String scheme) {
        synchronized (this) {
            if (getHttp2HostInfo(hostName, port, scheme) == null) {
                this.http2Hosts.add(new Http2HostInfo(hostName, port, scheme));
                promoteCalls();
                int runningCallsCount = runningCallsCount();
                Runnable idleCallback = this.idleCallback;
            }
        }
    }

    public void removeHttp2Host(String hostName, int port, String scheme) {
        synchronized (this) {
            Iterator<Http2HostInfo> iter = this.http2Hosts.iterator();
            while (iter.hasNext()) {
                Http2HostInfo h = iter.next();
                if (h.hostName.equals(hostName) && h.port == port && h.scheme.equals(scheme)) {
                    iter.remove();
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0019, code lost:
        return;
     */
    public void enqueue(RealCall.AsyncCall call) {
        synchronized (this) {
            if (call == null) {
                try {
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                Http2HostInfo http2HostInfo = getHttp2HostInfo(call.request());
                if (http2HostInfo != null) {
                    enqueueHttp2Calls(call, http2HostInfo);
                } else {
                    enqueueHttp1Calls(call);
                }
            }
        }
    }

    private void enqueueHttp2Calls(RealCall.AsyncCall call, Http2HostInfo http2HostInfo) {
        if (http2HostInfo.callAmount == 0) {
            HttpUrl url = call.request().url();
            if (!call.client().keepHttp2ConnectionAlive(url.host(), url.port(), url.scheme())) {
                removeHttp2Host(url.host(), url.port(), url.scheme());
                enqueueHttp1Calls(call);
                return;
            }
        }
        http2HostInfo.callAmount++;
        if (this.runningAsyncCalls.size() >= getMaxRequests() || runningHttp2CallsForHost(call) >= this.maxHttp2RequestsPerHost) {
            this.readyAsyncCalls.add(call);
            http2ConnectionOnDemand(call, http2HostInfo);
            return;
        }
        this.runningAsyncCalls.add(call);
        http2HostInfo.runningCallAmount++;
        executorService().execute(call);
    }

    private void enqueueHttp1Calls(RealCall.AsyncCall call) {
        if (this.runningAsyncCalls.size() >= getMaxRequests() || runningHttp1CallsForHost(call) >= this.maxHttp1RequestsPerHost) {
            this.readyAsyncCalls.add(call);
            return;
        }
        this.runningAsyncCalls.add(call);
        executorService().execute(call);
    }

    private void promoteCalls() {
        int runningCalls;
        int maxHttpRequestsPerHost;
        if (this.runningAsyncCalls.size() < getMaxRequests() && !this.readyAsyncCalls.isEmpty()) {
            Iterator<RealCall.AsyncCall> i = this.readyAsyncCalls.iterator();
            while (i.hasNext()) {
                RealCall.AsyncCall call = i.next();
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

    /* access modifiers changed from: package-private */
    public void finished(RealCall.AsyncCall call) {
        synchronized (this) {
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

    /* access modifiers changed from: package-private */
    public void finished(RealCall call) {
        finished(this.runningSyncCalls, call, false);
    }

    private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {
        int runningCallsCount;
        Runnable idleCallback;
        synchronized (this) {
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

    private int runningHttp2CallsForHost(RealCall.AsyncCall call) {
        int result = 0;
        for (Http2HostInfo hostInfo : this.http2Hosts) {
            if (hostInfo.hostName.equals(call.host())) {
                result += hostInfo.runningCallAmount;
            }
        }
        return result;
    }

    private int runningHttp1CallsForHost(RealCall.AsyncCall call) {
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

    private void http2ConnectionOnDemand(RealCall.AsyncCall call, Http2HostInfo http2HostInfo) {
        if (http2HostInfo.callAmount == this.maxHttp2RequestsPerHost + 1) {
            HttpUrl url = call.request().url();
            http2HostInfo.connectionAmount = call.client().http2ConnectionCount(url.host(), url.port(), url.scheme());
        }
        int connectionNum = http2HostInfo.connectionAmount;
        if (this.maxHttp2RequestsPerHost * connectionNum * this.http2ConnectionOnDemandFactor < http2HostInfo.callAmount && connectionNum < this.maxHttp2ConnectionPerHost) {
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
                Objects.requireNonNull(connectCall);
                RealCall.AsyncCall asyncConnectCall = new RealCall.AsyncCall(callback);
                asyncConnectCall.setForCreateConnectionOnly();
                this.runningAsyncCalls.add(asyncConnectCall);
                executorService().execute(asyncConnectCall);
            }
        }
    }
}
