package com.huawei.okhttp3.internal;

import com.huawei.okhttp3.Address;
import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.ConnectionPool;
import com.huawei.okhttp3.ConnectionSpec;
import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.connection.Exchange;
import com.huawei.okhttp3.internal.connection.RealConnectionPool;
import javax.annotation.Nullable;
import javax.net.ssl.SSLSocket;

public abstract class Internal {
    public static Internal instance;

    public abstract void addLenient(Headers.Builder builder, String str);

    public abstract void addLenient(Headers.Builder builder, String str, String str2);

    public abstract void apply(ConnectionSpec connectionSpec, SSLSocket sSLSocket, boolean z);

    public abstract int code(Response.Builder builder);

    public abstract boolean equalsNonHost(Address address, Address address2);

    @Nullable
    public abstract Exchange exchange(Response response);

    public abstract void initExchange(Response.Builder builder, Exchange exchange);

    public abstract Call newWebSocketCall(OkHttpClient okHttpClient, Request request);

    public abstract RealConnectionPool realConnectionPool(ConnectionPool connectionPool);

    public static void initializeInstanceForTests() {
        new OkHttpClient();
    }
}
