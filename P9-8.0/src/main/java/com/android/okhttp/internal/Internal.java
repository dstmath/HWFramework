package com.android.okhttp.internal;

import com.android.okhttp.Address;
import com.android.okhttp.Call;
import com.android.okhttp.Callback;
import com.android.okhttp.ConnectionPool;
import com.android.okhttp.ConnectionSpec;
import com.android.okhttp.Headers.Builder;
import com.android.okhttp.HttpUrl;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.internal.http.StreamAllocation;
import com.android.okhttp.internal.io.RealConnection;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

public abstract class Internal {
    public static Internal instance;
    public static final Logger logger = Logger.getLogger(OkHttpClient.class.getName());

    public abstract void addLenient(Builder builder, String str);

    public abstract void addLenient(Builder builder, String str, String str2);

    public abstract void apply(ConnectionSpec connectionSpec, SSLSocket sSLSocket, boolean z);

    public abstract StreamAllocation callEngineGetStreamAllocation(Call call);

    public abstract void callEnqueue(Call call, Callback callback, boolean z);

    public abstract boolean connectionBecameIdle(ConnectionPool connectionPool, RealConnection realConnection);

    public abstract RealConnection get(ConnectionPool connectionPool, Address address, StreamAllocation streamAllocation);

    public abstract HttpUrl getHttpUrlChecked(String str) throws MalformedURLException, UnknownHostException;

    public abstract InternalCache internalCache(OkHttpClient okHttpClient);

    public abstract void put(ConnectionPool connectionPool, RealConnection realConnection);

    public abstract RouteDatabase routeDatabase(ConnectionPool connectionPool);

    public abstract void setCache(OkHttpClient okHttpClient, InternalCache internalCache);

    public static void initializeInstanceForTests() {
        OkHttpClient okHttpClient = new OkHttpClient();
    }
}
