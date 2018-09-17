package com.huawei.okhttp3.internal.http;

import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.Response.Builder;
import com.huawei.okhttp3.ResponseBody;
import com.huawei.okio.Sink;
import java.io.IOException;

public interface HttpCodec {
    public static final int DISCARD_STREAM_TIMEOUT_MILLIS = 100;

    void cancel();

    Sink createRequestBody(Request request, long j);

    void finishRequest() throws IOException;

    ResponseBody openResponseBody(Response response) throws IOException;

    Builder readResponseHeaders() throws IOException;

    void writeRequestHeaders(Request request) throws IOException;
}
