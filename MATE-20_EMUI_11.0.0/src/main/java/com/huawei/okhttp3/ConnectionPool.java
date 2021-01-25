package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.connection.RealConnectionPool;
import java.util.concurrent.TimeUnit;

public final class ConnectionPool {
    final RealConnectionPool delegate;

    public interface Http2ConnectionEventListener {
        void onEvicted(String str, int i, String str2);
    }

    public ConnectionPool() {
        this(5, 5, TimeUnit.MINUTES);
    }

    public ConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
        this.delegate = new RealConnectionPool(maxIdleConnections, keepAliveDuration, timeUnit);
    }

    public int idleConnectionCount() {
        return this.delegate.idleConnectionCount();
    }

    public int connectionCount() {
        return this.delegate.connectionCount();
    }

    public void evictAll() {
        this.delegate.evictAll();
    }

    public synchronized int http2ConnectionCount(Address address) {
        return this.delegate.http2ConnectionCount(address);
    }

    public synchronized int http2ConnectionCount(String hostName, int port, String scheme) {
        return this.delegate.http2ConnectionCount(hostName, port, scheme);
    }

    public synchronized boolean keepHttp2ConnectionAlive(String hostName, int port, String scheme) {
        return this.delegate.keepHttp2ConnectionAlive(hostName, port, scheme);
    }

    public synchronized void addHttp2Listener(Http2ConnectionEventListener listener) {
        this.delegate.addHttp2Listener(listener);
    }

    public synchronized void removeHttp2Listener(Http2ConnectionEventListener listener) {
        this.delegate.removeHttp2Listener(listener);
    }
}
