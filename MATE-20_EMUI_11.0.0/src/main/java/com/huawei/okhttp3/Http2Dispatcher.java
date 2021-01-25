package com.huawei.okhttp3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

public final class Http2Dispatcher extends Dispatcher {
    private static final int HTTP1_REQUEST_PER_HOST_DEFAULT = 5;
    private static final int HTTP2_REQUEST_PER_HOST_DEFAULT = 32;
    private final List<Http2HostInfo> http2Hosts = new ArrayList();
    private int maxHttp1RequestsPerHost = 5;
    private int maxHttp2RequestsPerHost = 32;
    private final String tooLowerValueInfo = "max < 1: ";

    public Http2Dispatcher(ExecutorService executorService) {
        super(executorService);
    }

    public Http2Dispatcher() {
    }

    public void setMaxHttp1RequestsPerHost(int maxHttp1RequestsPerHost2) {
        if (maxHttp1RequestsPerHost2 >= 1) {
            synchronized (this) {
                this.maxHttp1RequestsPerHost = maxHttp1RequestsPerHost2;
            }
            return;
        }
        throw new IllegalArgumentException("max < 1: " + maxHttp1RequestsPerHost2);
    }

    public int getMaxHttp1RequestsPerHost() {
        int i;
        synchronized (this) {
            i = this.maxHttp1RequestsPerHost;
        }
        return i;
    }

    public void setMaxHttp2RequestsPerHost(int maxHttp2RequestsPerHost2) {
        if (maxHttp2RequestsPerHost2 >= 1) {
            synchronized (this) {
                this.maxHttp2RequestsPerHost = maxHttp2RequestsPerHost2;
            }
            return;
        }
        throw new IllegalArgumentException("max < 1: " + maxHttp2RequestsPerHost2);
    }

    public int getMaxHttp2RequestsPerHost() {
        int i;
        synchronized (this) {
            i = this.maxHttp2RequestsPerHost;
        }
        return i;
    }

    public void setMaxHttp2ConnectionPerHost(int maxHttp2ConnectionPerHost) {
        if (maxHttp2ConnectionPerHost < 1) {
            throw new IllegalArgumentException("max < 1: " + maxHttp2ConnectionPerHost);
        }
    }

    @Override // com.huawei.okhttp3.Dispatcher, com.huawei.okhttp3.AbsDispatcher
    public int getMaxHttp2ConnectionPerHost() {
        return 1;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.okhttp3.AbsDispatcher
    public void addHttp2Host(String hostName, int port, String scheme) {
        if (hostName != null && scheme != null) {
            synchronized (this) {
                if (getHttp2HostInfo(hostName, port, scheme) == null) {
                    this.http2Hosts.add(new Http2HostInfo(hostName, port, scheme));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.okhttp3.AbsDispatcher
    public void removeHttp2Host(String hostName, int port, String scheme) {
        if (hostName != null && scheme != null) {
            synchronized (this) {
                Iterator<Http2HostInfo> iter = this.http2Hosts.iterator();
                while (iter.hasNext()) {
                    Http2HostInfo h2Host = iter.next();
                    if (h2Host != null && h2Host.hostName.equals(hostName) && h2Host.port == port && h2Host.scheme.equals(scheme)) {
                        iter.remove();
                        return;
                    }
                }
            }
        }
    }

    private Http2HostInfo getHttp2HostInfo(String hostName, int port, String scheme) {
        if (hostName == null || scheme == null) {
            return null;
        }
        for (Http2HostInfo h2Host : this.http2Hosts) {
            if (h2Host != null && h2Host.hostName.equals(hostName) && h2Host.port == port && h2Host.scheme.equals(scheme)) {
                return h2Host;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public final class Http2HostInfo {
        public String hostName;
        public int port;
        public String scheme;

        Http2HostInfo(String hostName2, int port2, String scheme2) {
            this.hostName = hostName2;
            this.port = port2;
            this.scheme = scheme2;
        }
    }
}
