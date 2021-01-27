package com.huawei.okhttp3.internal.http;

import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.connection.RealConnection;
import com.huawei.okio.Sink;
import com.huawei.okio.Source;
import java.io.IOException;
import javax.annotation.Nullable;

@Deprecated
public interface ExchangeCodec {
    public static final int DISCARD_STREAM_TIMEOUT_MILLIS = 100;

    void cancel();

    RealConnection connection();

    Sink createRequestBody(Request request, long j) throws IOException;

    void finishRequest() throws IOException;

    void flushRequest() throws IOException;

    Source openResponseBodySource(Response response) throws IOException;

    @Nullable
    Response.Builder readResponseHeaders(boolean z) throws IOException;

    long reportedContentLength(Response response) throws IOException;

    Headers trailers() throws IOException;

    void writeRequestHeaders(Request request) throws IOException;
}
