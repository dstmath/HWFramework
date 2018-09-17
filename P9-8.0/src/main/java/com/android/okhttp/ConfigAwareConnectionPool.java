package com.android.okhttp;

import libcore.net.event.NetworkEventDispatcher;
import libcore.net.event.NetworkEventListener;

public class ConfigAwareConnectionPool {
    private static final long CONNECTION_POOL_DEFAULT_KEEP_ALIVE_DURATION_MS = 300000;
    private static final long CONNECTION_POOL_KEEP_ALIVE_DURATION_MS;
    private static final int CONNECTION_POOL_MAX_IDLE_CONNECTIONS;
    private static final ConfigAwareConnectionPool instance = new ConfigAwareConnectionPool();
    private ConnectionPool connectionPool;
    private final NetworkEventDispatcher networkEventDispatcher;
    private boolean networkEventListenerRegistered;

    static {
        long parseLong;
        String keepAliveProperty = System.getProperty("http.keepAlive");
        String keepAliveDurationProperty = System.getProperty("http.keepAliveDuration");
        String maxIdleConnectionsProperty = System.getProperty("http.maxConnections");
        if (keepAliveDurationProperty != null) {
            parseLong = Long.parseLong(keepAliveDurationProperty);
        } else {
            parseLong = CONNECTION_POOL_DEFAULT_KEEP_ALIVE_DURATION_MS;
        }
        CONNECTION_POOL_KEEP_ALIVE_DURATION_MS = parseLong;
        if (keepAliveProperty != null && (Boolean.parseBoolean(keepAliveProperty) ^ 1) != 0) {
            CONNECTION_POOL_MAX_IDLE_CONNECTIONS = 0;
        } else if (maxIdleConnectionsProperty != null) {
            CONNECTION_POOL_MAX_IDLE_CONNECTIONS = Integer.parseInt(maxIdleConnectionsProperty);
        } else {
            CONNECTION_POOL_MAX_IDLE_CONNECTIONS = 5;
        }
    }

    protected ConfigAwareConnectionPool(NetworkEventDispatcher networkEventDispatcher) {
        this.networkEventDispatcher = networkEventDispatcher;
    }

    private ConfigAwareConnectionPool() {
        this.networkEventDispatcher = NetworkEventDispatcher.getInstance();
    }

    public static ConfigAwareConnectionPool getInstance() {
        return instance;
    }

    public synchronized ConnectionPool get() {
        if (this.connectionPool == null) {
            if (!this.networkEventListenerRegistered) {
                this.networkEventDispatcher.addListener(new NetworkEventListener() {
                    public void onNetworkConfigurationChanged() {
                        synchronized (ConfigAwareConnectionPool.this) {
                            ConfigAwareConnectionPool.this.connectionPool = null;
                        }
                    }
                });
                this.networkEventListenerRegistered = true;
            }
            this.connectionPool = new ConnectionPool(CONNECTION_POOL_MAX_IDLE_CONNECTIONS, CONNECTION_POOL_KEEP_ALIVE_DURATION_MS);
        }
        return this.connectionPool;
    }
}
