package com.huawei.okhttp3.internal.http;

import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.Connection;
import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.Exchange;
import com.huawei.okhttp3.internal.connection.Transmitter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

@Deprecated
public final class RealInterceptorChain implements Interceptor.Chain {
    private final Call call;
    private int calls;
    private final int connectTimeout;
    @Nullable
    private final Exchange exchange;
    private final int index;
    private final List<Interceptor> interceptors;
    private final int readTimeout;
    private final Request request;
    private final Transmitter transmitter;
    private final int writeTimeout;

    public RealInterceptorChain(List<Interceptor> interceptors2, Transmitter transmitter2, @Nullable Exchange exchange2, int index2, Request request2, Call call2, int connectTimeout2, int readTimeout2, int writeTimeout2) {
        this.interceptors = interceptors2;
        this.transmitter = transmitter2;
        this.exchange = exchange2;
        this.index = index2;
        this.request = request2;
        this.call = call2;
        this.connectTimeout = connectTimeout2;
        this.readTimeout = readTimeout2;
        this.writeTimeout = writeTimeout2;
    }

    @Override // com.huawei.okhttp3.Interceptor.Chain
    @Nullable
    public Connection connection() {
        Exchange exchange2 = this.exchange;
        if (exchange2 != null) {
            return exchange2.connection();
        }
        return null;
    }

    @Override // com.huawei.okhttp3.Interceptor.Chain
    public int connectTimeoutMillis() {
        return this.connectTimeout;
    }

    @Override // com.huawei.okhttp3.Interceptor.Chain
    public Interceptor.Chain withConnectTimeout(int timeout, TimeUnit unit) {
        return new RealInterceptorChain(this.interceptors, this.transmitter, this.exchange, this.index, this.request, this.call, Util.checkDuration("timeout", (long) timeout, unit), this.readTimeout, this.writeTimeout);
    }

    @Override // com.huawei.okhttp3.Interceptor.Chain
    public int readTimeoutMillis() {
        return this.readTimeout;
    }

    @Override // com.huawei.okhttp3.Interceptor.Chain
    public Interceptor.Chain withReadTimeout(int timeout, TimeUnit unit) {
        return new RealInterceptorChain(this.interceptors, this.transmitter, this.exchange, this.index, this.request, this.call, this.connectTimeout, Util.checkDuration("timeout", (long) timeout, unit), this.writeTimeout);
    }

    @Override // com.huawei.okhttp3.Interceptor.Chain
    public int writeTimeoutMillis() {
        return this.writeTimeout;
    }

    @Override // com.huawei.okhttp3.Interceptor.Chain
    public Interceptor.Chain withWriteTimeout(int timeout, TimeUnit unit) {
        return new RealInterceptorChain(this.interceptors, this.transmitter, this.exchange, this.index, this.request, this.call, this.connectTimeout, this.readTimeout, Util.checkDuration("timeout", (long) timeout, unit));
    }

    public Transmitter transmitter() {
        return this.transmitter;
    }

    public Exchange exchange() {
        Exchange exchange2 = this.exchange;
        if (exchange2 != null) {
            return exchange2;
        }
        throw new IllegalStateException();
    }

    @Override // com.huawei.okhttp3.Interceptor.Chain
    public Call call() {
        return this.call;
    }

    @Override // com.huawei.okhttp3.Interceptor.Chain
    public Request request() {
        return this.request;
    }

    @Override // com.huawei.okhttp3.Interceptor.Chain
    public Response proceed(Request request2) throws IOException {
        return proceed(request2, this.transmitter, this.exchange);
    }

    public Response proceed(Request request2, Transmitter transmitter2, @Nullable Exchange exchange2) throws IOException {
        if (this.index < this.interceptors.size()) {
            this.calls++;
            Exchange exchange3 = this.exchange;
            if (exchange3 != null && !exchange3.connection().supportsUrl(request2.url())) {
                throw new IllegalStateException("network interceptor " + this.interceptors.get(this.index - 1) + " must retain the same host and port");
            } else if (this.exchange == null || this.calls <= 1) {
                RealInterceptorChain next = new RealInterceptorChain(this.interceptors, transmitter2, exchange2, this.index + 1, request2, this.call, this.connectTimeout, this.readTimeout, this.writeTimeout);
                Interceptor interceptor = this.interceptors.get(this.index);
                Response response = interceptor.intercept(next);
                if (exchange2 != null && this.index + 1 < this.interceptors.size() && next.calls != 1) {
                    throw new IllegalStateException("network interceptor " + interceptor + " must call proceed() exactly once");
                } else if (response == null) {
                    throw new NullPointerException("interceptor " + interceptor + " returned null");
                } else if (response.body() != null || request2.isCreateConnectionRequest()) {
                    return response;
                } else {
                    throw new IllegalStateException("interceptor " + interceptor + " returned a response with no body");
                }
            } else {
                throw new IllegalStateException("network interceptor " + this.interceptors.get(this.index - 1) + " must call proceed() exactly once");
            }
        } else {
            throw new AssertionError();
        }
    }
}
