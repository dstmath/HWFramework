package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.HttpUrl;
import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.http.HttpCodec;
import com.huawei.okhttp3.internal.http.RealInterceptorChain;
import java.io.IOException;

public final class ConnectInterceptor implements Interceptor {
    public final OkHttpClient client;

    public ConnectInterceptor(OkHttpClient client2) {
        this.client = client2;
    }

    @Override // com.huawei.okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        StreamAllocation streamAllocation = realChain.streamAllocation();
        HttpCodec httpCodec = streamAllocation.newStream(this.client, chain, !request.method().equals("GET"));
        RealConnection connection = streamAllocation.connection();
        Response response = realChain.proceed(request, streamAllocation, httpCodec, connection);
        if (connection.isMultiplexed() && connection.successCount == 0) {
            HttpUrl url = request.url();
            this.client.addHttp2Host(url.host(), url.port(), url.scheme());
        }
        return response;
    }
}
