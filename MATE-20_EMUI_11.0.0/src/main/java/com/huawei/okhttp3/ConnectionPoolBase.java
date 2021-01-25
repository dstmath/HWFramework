package com.huawei.okhttp3;

import com.huawei.okhttp3.ConnectionPool;
import com.huawei.okhttp3.internal.connection.RealConnection;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class ConnectionPoolBase {
    protected final Deque<RealConnection> connections = new ArrayDeque();
    private final Deque<WeakReference<ConnectionPool.Http2ConnectionEventListener>> listenerWrList = new ArrayDeque();

    public synchronized int http2ConnectionCount(Address address) {
        int count = 0;
        if (address == null) {
            return 0;
        }
        for (RealConnection connection : this.connections) {
            if (connection != null && address.equals(connection.route().address) && !connection.getNoNewExchanges() && connection.isMultiplexed()) {
                if (connection.successCount() == 0 || connection.isHealthy(true)) {
                    count++;
                }
            }
        }
        return count;
    }

    public synchronized int http2ConnectionCount(String hostName, int port, String scheme) {
        int count = 0;
        if (hostName == null || scheme == null) {
            return 0;
        }
        for (RealConnection connection : this.connections) {
            if (connection != null && !connection.getNoNewExchanges()) {
                if (connection.isMultiplexed()) {
                    if (hostName.equals(connection.route().address.url().host()) && port == connection.route().address.url().port()) {
                        if (scheme.equals(connection.route().address.url().scheme())) {
                            if (connection.successCount() == 0 || connection.isHealthy(true)) {
                                count++;
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    public synchronized boolean keepHttp2ConnectionAlive(String hostName, int port, String scheme) {
        if (hostName == null || scheme == null) {
            return false;
        }
        for (RealConnection connection : this.connections) {
            if (connection != null && !connection.getNoNewExchanges()) {
                if (connection.isMultiplexed()) {
                    if (hostName.equals(connection.route().address.url().host()) && port == connection.route().address.url().port()) {
                        if (scheme.equals(connection.route().address.url().scheme())) {
                            if (connection.isHealthy(true)) {
                                connection.keepaliveTimestampNs = System.nanoTime();
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public synchronized void addHttp2Listener(ConnectionPool.Http2ConnectionEventListener listener) {
        if (listener != null) {
            this.listenerWrList.add(new WeakReference<>(listener));
        }
    }

    public synchronized void removeHttp2Listener(ConnectionPool.Http2ConnectionEventListener listener) {
        if (listener != null) {
            Iterator<WeakReference<ConnectionPool.Http2ConnectionEventListener>> itWr = this.listenerWrList.iterator();
            while (itWr.hasNext()) {
                ConnectionPool.Http2ConnectionEventListener listenerInList = itWr.next().get();
                if (listenerInList == null || listenerInList == listener) {
                    itWr.remove();
                }
            }
        }
    }
}
