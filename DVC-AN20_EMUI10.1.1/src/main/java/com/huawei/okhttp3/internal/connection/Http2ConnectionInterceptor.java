package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Protocol;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.http.RealInterceptorChain;
import java.io.IOException;

public final class Http2ConnectionInterceptor implements Interceptor {
    public final OkHttpClient client;

    public Http2ConnectionInterceptor(OkHttpClient client2) {
        this.client = client2;
    }

    @Override // com.huawei.okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        realChain.streamAllocation().newHttp2Connection(this.client, request);
        return new Response.Builder().protocol(Protocol.HTTP_2).code(200).request(request).build();
    }
}
