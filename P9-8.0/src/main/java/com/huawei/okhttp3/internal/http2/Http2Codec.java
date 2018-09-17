package com.huawei.okhttp3.internal.http2;

import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Protocol;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.Response.Builder;
import com.huawei.okhttp3.ResponseBody;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.StreamAllocation;
import com.huawei.okhttp3.internal.http.HttpCodec;
import com.huawei.okhttp3.internal.http.RealResponseBody;
import com.huawei.okhttp3.internal.http.RequestLine;
import com.huawei.okhttp3.internal.http.StatusLine;
import com.huawei.okio.ByteString;
import com.huawei.okio.ForwardingSource;
import com.huawei.okio.Okio;
import com.huawei.okio.Sink;
import com.huawei.okio.Source;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import okhttp3.internal.http2.Header;

public final class Http2Codec implements HttpCodec {
    private static final ByteString CONNECTION = ByteString.encodeUtf8("connection");
    private static final ByteString ENCODING = ByteString.encodeUtf8("encoding");
    private static final ByteString HOST = ByteString.encodeUtf8("host");
    private static final List<ByteString> HTTP_2_SKIPPED_REQUEST_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TE, TRANSFER_ENCODING, ENCODING, UPGRADE, Header.TARGET_METHOD, Header.TARGET_PATH, Header.TARGET_SCHEME, Header.TARGET_AUTHORITY);
    private static final List<ByteString> HTTP_2_SKIPPED_RESPONSE_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TE, TRANSFER_ENCODING, ENCODING, UPGRADE);
    private static final ByteString KEEP_ALIVE = ByteString.encodeUtf8("keep-alive");
    private static final ByteString PROXY_CONNECTION = ByteString.encodeUtf8("proxy-connection");
    private static final ByteString TE = ByteString.encodeUtf8("te");
    private static final ByteString TRANSFER_ENCODING = ByteString.encodeUtf8("transfer-encoding");
    private static final ByteString UPGRADE = ByteString.encodeUtf8("upgrade");
    private final OkHttpClient client;
    private final Http2Connection connection;
    private Http2Stream stream;
    final StreamAllocation streamAllocation;

    class StreamFinishingSource extends ForwardingSource {
        public StreamFinishingSource(Source delegate) {
            super(delegate);
        }

        public void close() throws IOException {
            Http2Codec.this.streamAllocation.streamFinished(false, Http2Codec.this);
            super.close();
        }
    }

    public Http2Codec(OkHttpClient client, StreamAllocation streamAllocation, Http2Connection connection) {
        this.client = client;
        this.streamAllocation = streamAllocation;
        this.connection = connection;
    }

    public Sink createRequestBody(Request request, long contentLength) {
        return this.stream.getSink();
    }

    public void writeRequestHeaders(Request request) throws IOException {
        if (this.stream == null) {
            try {
                this.stream = this.connection.newStream(http2HeadersList(request), request.body() != null);
                this.stream.readTimeout().timeout((long) this.client.readTimeoutMillis(), TimeUnit.MILLISECONDS);
                this.stream.writeTimeout().timeout((long) this.client.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
            } catch (IOException e) {
                this.streamAllocation.noNewStreams();
                throw e;
            }
        }
    }

    public void finishRequest() throws IOException {
        this.stream.getSink().close();
    }

    public Builder readResponseHeaders() throws IOException {
        return readHttp2HeadersList(this.stream.getResponseHeaders());
    }

    public static List<Header> http2HeadersList(Request request) {
        Headers headers = request.headers();
        String headerHost = request.header("host");
        if (headerHost == null || headerHost.length() == 0) {
            headerHost = Util.hostHeader(request.url(), false);
        }
        List<Header> result = new ArrayList(headers.size() + 4);
        result.add(new Header(Header.TARGET_METHOD, request.method()));
        result.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.url())));
        result.add(new Header(Header.TARGET_AUTHORITY, headerHost));
        result.add(new Header(Header.TARGET_SCHEME, request.url().scheme()));
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            ByteString name = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            if (!HTTP_2_SKIPPED_REQUEST_HEADERS.contains(name)) {
                result.add(new Header(name, headers.value(i)));
            }
        }
        return result;
    }

    public static Builder readHttp2HeadersList(List<Header> headerBlock) throws IOException {
        String status = null;
        Headers.Builder headersBuilder = new Headers.Builder();
        int size = headerBlock.size();
        for (int i = 0; i < size; i++) {
            ByteString name = ((Header) headerBlock.get(i)).name;
            String value = ((Header) headerBlock.get(i)).value.utf8();
            if (name.equals(Header.RESPONSE_STATUS)) {
                status = value;
            } else if (!HTTP_2_SKIPPED_RESPONSE_HEADERS.contains(name)) {
                Internal.instance.addLenient(headersBuilder, name.utf8(), value);
            }
        }
        if (status == null) {
            throw new ProtocolException("Expected ':status' header not present");
        }
        StatusLine statusLine = StatusLine.parse("HTTP/1.1 " + status);
        return new Builder().protocol(Protocol.HTTP_2).code(statusLine.code).message(statusLine.message).headers(headersBuilder.build());
    }

    public ResponseBody openResponseBody(Response response) throws IOException {
        return new RealResponseBody(response.headers(), Okio.buffer(new StreamFinishingSource(this.stream.getSource())));
    }

    public void cancel() {
        if (this.stream != null) {
            this.stream.closeLater(ErrorCode.CANCEL);
        }
    }
}
