package com.android.okhttp.internal.http;

import com.android.okhttp.Request;
import com.android.okhttp.Response;
import com.android.okhttp.Response.Builder;
import com.android.okhttp.ResponseBody;
import com.android.okhttp.okio.Okio;
import com.android.okhttp.okio.Sink;
import com.android.okhttp.okio.Source;
import java.io.IOException;

public final class HttpTransport implements Transport {
    private final HttpConnection httpConnection;
    private final HttpEngine httpEngine;

    public HttpTransport(HttpEngine httpEngine, HttpConnection httpConnection) {
        this.httpEngine = httpEngine;
        this.httpConnection = httpConnection;
    }

    public Sink createRequestBody(Request request, long contentLength) throws IOException {
        if ("chunked".equalsIgnoreCase(request.header("Transfer-Encoding"))) {
            return this.httpConnection.newChunkedSink();
        }
        if (contentLength != -1) {
            return this.httpConnection.newFixedLengthSink(contentLength);
        }
        throw new IllegalStateException("Cannot stream a request body without chunked encoding or a known content length!");
    }

    public void finishRequest() throws IOException {
        this.httpConnection.flush();
    }

    public void writeRequestBody(RetryableSink requestBody) throws IOException {
        this.httpConnection.writeRequestBody(requestBody);
    }

    public void writeRequestHeaders(Request request) throws IOException {
        this.httpEngine.writingRequestHeaders();
        this.httpConnection.writeRequest(request.headers(), RequestLine.get(request, this.httpEngine.getConnection().getRoute().getProxy().type(), this.httpEngine.getConnection().getProtocol()));
    }

    public Builder readResponseHeaders() throws IOException {
        return this.httpConnection.readResponse();
    }

    public void releaseConnectionOnIdle() throws IOException {
        if (canReuseConnection()) {
            this.httpConnection.poolOnIdle();
        } else {
            this.httpConnection.closeOnIdle();
        }
    }

    public boolean canReuseConnection() {
        if ("close".equalsIgnoreCase(this.httpEngine.getRequest().header("Connection")) || "close".equalsIgnoreCase(this.httpEngine.getResponse().header("Connection")) || this.httpConnection.isClosed()) {
            return false;
        }
        return true;
    }

    public ResponseBody openResponseBody(Response response) throws IOException {
        return new RealResponseBody(response.headers(), Okio.buffer(getTransferStream(response)));
    }

    private Source getTransferStream(Response response) throws IOException {
        if (!HttpEngine.hasBody(response)) {
            return this.httpConnection.newFixedLengthSource(0);
        }
        if ("chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return this.httpConnection.newChunkedSource(this.httpEngine);
        }
        long contentLength = OkHeaders.contentLength(response);
        if (contentLength != -1) {
            return this.httpConnection.newFixedLengthSource(contentLength);
        }
        return this.httpConnection.newUnknownLengthSource();
    }

    public void disconnect(HttpEngine engine) throws IOException {
        this.httpConnection.closeIfOwnedBy(engine);
    }
}
