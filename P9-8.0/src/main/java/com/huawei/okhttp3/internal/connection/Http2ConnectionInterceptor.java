package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.Interceptor.Chain;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Protocol;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.Response.Builder;
import com.huawei.okhttp3.internal.http.RealInterceptorChain;
import java.io.IOException;

public final class Http2ConnectionInterceptor implements Interceptor {
    public final OkHttpClient client;

    public Http2ConnectionInterceptor(OkHttpClient client) {
        this.client = client;
    }

    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        realChain.streamAllocation().newHttp2Connection(this.client, request);
        return new Builder().protocol(Protocol.HTTP_2).code(200).request(request).build();
    }
}
