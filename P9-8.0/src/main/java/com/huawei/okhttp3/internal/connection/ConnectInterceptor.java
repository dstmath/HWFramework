package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.HttpUrl;
import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.Interceptor.Chain;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.http.HttpCodec;
import com.huawei.okhttp3.internal.http.RealInterceptorChain;
import java.io.IOException;

public final class ConnectInterceptor implements Interceptor {
    public final OkHttpClient client;

    public ConnectInterceptor(OkHttpClient client) {
        this.client = client;
    }

    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        StreamAllocation streamAllocation = realChain.streamAllocation();
        HttpCodec httpCodec = streamAllocation.newStream(this.client, request.method().equals("GET") ^ 1);
        RealConnection connection = streamAllocation.connection();
        Response response = realChain.proceed(request, streamAllocation, httpCodec, connection);
        if (connection.http2Connection != null && connection.successCount == 0) {
            HttpUrl url = request.url();
            this.client.addHttp2Host(url.host(), url.port(), url.scheme());
        }
        return response;
    }
}
