package com.huawei.okhttp3.internal.http2;

import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.Interceptor;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Protocol;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.RealConnection;
import com.huawei.okhttp3.internal.http.ExchangeCodec;
import com.huawei.okhttp3.internal.http.HttpHeaders;
import com.huawei.okhttp3.internal.http.RequestLine;
import com.huawei.okhttp3.internal.http.StatusLine;
import com.huawei.okio.Sink;
import com.huawei.okio.Source;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class Http2ExchangeCodec implements ExchangeCodec {
    private static final String CONNECTION = "connection";
    private static final String ENCODING = "encoding";
    private static final String HOST = "host";
    private static final List<String> HTTP_2_SKIPPED_REQUEST_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TE, TRANSFER_ENCODING, ENCODING, UPGRADE, Header.TARGET_METHOD_UTF8, Header.TARGET_PATH_UTF8, Header.TARGET_SCHEME_UTF8, Header.TARGET_AUTHORITY_UTF8);
    private static final List<String> HTTP_2_SKIPPED_RESPONSE_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TE, TRANSFER_ENCODING, ENCODING, UPGRADE);
    private static final String KEEP_ALIVE = "keep-alive";
    private static final String PROXY_CONNECTION = "proxy-connection";
    private static final String TE = "te";
    private static final String TRANSFER_ENCODING = "transfer-encoding";
    private static final String UPGRADE = "upgrade";
    private volatile boolean canceled;
    private final Interceptor.Chain chain;
    private final Http2Connection connection;
    private final Protocol protocol;
    private final RealConnection realConnection;
    private volatile Http2Stream stream;

    public Http2ExchangeCodec(OkHttpClient client, RealConnection realConnection2, Interceptor.Chain chain2, Http2Connection connection2) {
        Protocol protocol2;
        this.realConnection = realConnection2;
        this.chain = chain2;
        this.connection = connection2;
        if (client.protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)) {
            protocol2 = Protocol.H2_PRIOR_KNOWLEDGE;
        } else {
            protocol2 = Protocol.HTTP_2;
        }
        this.protocol = protocol2;
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public RealConnection connection() {
        return this.realConnection;
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public Sink createRequestBody(Request request, long contentLength) {
        return this.stream.getSink();
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public void writeRequestHeaders(Request request) throws IOException {
        if (this.stream == null) {
            this.stream = this.connection.newStream(http2HeadersList(request), request.body() != null);
            if (!this.canceled) {
                this.stream.readTimeout().timeout((long) this.chain.readTimeoutMillis(), TimeUnit.MILLISECONDS);
                this.stream.writeTimeout().timeout((long) this.chain.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
                return;
            }
            this.stream.closeLater(ErrorCode.CANCEL);
            throw new IOException("Canceled");
        }
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public void flushRequest() throws IOException {
        this.connection.flush();
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public void finishRequest() throws IOException {
        this.stream.getSink().close();
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
        Response.Builder responseBuilder = readHttp2HeadersList(this.stream.takeHeaders(), this.protocol);
        if (!expectContinue || Internal.instance.code(responseBuilder) != 100) {
            return responseBuilder;
        }
        return null;
    }

    public static List<Header> http2HeadersList(Request request) {
        Headers headers = request.headers();
        List<Header> result = new ArrayList<>(headers.size() + 4);
        result.add(new Header(Header.TARGET_METHOD, request.method()));
        result.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.url())));
        String host = request.header("Host");
        if (host != null) {
            result.add(new Header(Header.TARGET_AUTHORITY, host));
        }
        result.add(new Header(Header.TARGET_SCHEME, request.url().scheme()));
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            String name = headers.name(i).toLowerCase(Locale.US);
            if (!HTTP_2_SKIPPED_REQUEST_HEADERS.contains(name) || (name.equals(TE) && headers.value(i).equals("trailers"))) {
                result.add(new Header(name, headers.value(i)));
            }
        }
        return result;
    }

    public static Response.Builder readHttp2HeadersList(Headers headerBlock, Protocol protocol2) throws IOException {
        StatusLine statusLine = null;
        Headers.Builder headersBuilder = new Headers.Builder();
        int size = headerBlock.size();
        for (int i = 0; i < size; i++) {
            String name = headerBlock.name(i);
            String value = headerBlock.value(i);
            if (name.equals(Header.RESPONSE_STATUS_UTF8)) {
                statusLine = StatusLine.parse("HTTP/1.1 " + value);
            } else if (!HTTP_2_SKIPPED_RESPONSE_HEADERS.contains(name)) {
                Internal.instance.addLenient(headersBuilder, name, value);
            }
        }
        if (statusLine != null) {
            return new Response.Builder().protocol(protocol2).code(statusLine.code).message(statusLine.message).headers(headersBuilder.build());
        }
        throw new ProtocolException("Expected ':status' header not present");
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public long reportedContentLength(Response response) {
        return HttpHeaders.contentLength(response);
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public Source openResponseBodySource(Response response) {
        return this.stream.getSource();
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public Headers trailers() throws IOException {
        return this.stream.trailers();
    }

    @Override // com.huawei.okhttp3.internal.http.ExchangeCodec
    public void cancel() {
        this.canceled = true;
        if (this.stream != null) {
            this.stream.closeLater(ErrorCode.CANCEL);
        }
    }
}
