package com.huawei.okhttp3.internal;

import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.ConnectionPool;
import com.huawei.okhttp3.ConnectionSpec;
import com.huawei.okhttp3.Headers.Builder;
import com.huawei.okhttp3.HttpUrl;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.internal.cache.InternalCache;
import com.huawei.okhttp3.internal.connection.RealConnection;
import com.huawei.okhttp3.internal.connection.RouteDatabase;
import com.huawei.okhttp3.internal.connection.StreamAllocation;
import java.io.Closeable;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocket;

public abstract class Internal {
    public static Internal instance;

    public abstract void addLenient(Builder builder, String str);

    public abstract void addLenient(Builder builder, String str, String str2);

    public abstract void apply(ConnectionSpec connectionSpec, SSLSocket sSLSocket, boolean z);

    public abstract boolean connectionBecameIdle(ConnectionPool connectionPool, RealConnection realConnection);

    public abstract Closeable deduplicate(OkHttpClient okHttpClient, ConnectionPool connectionPool, Address address, StreamAllocation streamAllocation);

    public abstract RealConnection get(ConnectionPool connectionPool, Address address, StreamAllocation streamAllocation);

    public abstract HttpUrl getHttpUrlChecked(String str) throws MalformedURLException, UnknownHostException;

    public abstract Call newWebSocketCall(OkHttpClient okHttpClient, Request request);

    public abstract void put(ConnectionPool connectionPool, RealConnection realConnection);

    public abstract RouteDatabase routeDatabase(ConnectionPool connectionPool);

    public abstract void setCache(OkHttpClient.Builder builder, InternalCache internalCache);

    public abstract StreamAllocation streamAllocation(Call call);

    public static void initializeInstanceForTests() {
        OkHttpClient okHttpClient = new OkHttpClient();
    }
}
