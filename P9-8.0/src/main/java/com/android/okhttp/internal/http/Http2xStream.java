package com.android.okhttp.internal.http;

import com.android.okhttp.Headers;
import com.android.okhttp.Protocol;
import com.android.okhttp.Request;
import com.android.okhttp.Response;
import com.android.okhttp.Response.Builder;
import com.android.okhttp.ResponseBody;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.framed.ErrorCode;
import com.android.okhttp.internal.framed.FramedConnection;
import com.android.okhttp.internal.framed.FramedStream;
import com.android.okhttp.internal.framed.Header;
import com.android.okhttp.okio.ByteString;
import com.android.okhttp.okio.ForwardingSource;
import com.android.okhttp.okio.Okio;
import com.android.okhttp.okio.Sink;
import com.android.okhttp.okio.Source;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class Http2xStream implements HttpStream {
    private static final ByteString CONNECTION = ByteString.encodeUtf8("connection");
    private static final ByteString ENCODING = ByteString.encodeUtf8("encoding");
    private static final ByteString HOST = ByteString.encodeUtf8("host");
    private static final List<ByteString> HTTP_2_SKIPPED_REQUEST_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TE, TRANSFER_ENCODING, ENCODING, UPGRADE, Header.TARGET_METHOD, Header.TARGET_PATH, Header.TARGET_SCHEME, Header.TARGET_AUTHORITY, Header.TARGET_HOST, Header.VERSION);
    private static final List<ByteString> HTTP_2_SKIPPED_RESPONSE_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TE, TRANSFER_ENCODING, ENCODING, UPGRADE);
    private static final ByteString KEEP_ALIVE = ByteString.encodeUtf8("keep-alive");
    private static final ByteString PROXY_CONNECTION = ByteString.encodeUtf8("proxy-connection");
    private static final List<ByteString> SPDY_3_SKIPPED_REQUEST_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TRANSFER_ENCODING, Header.TARGET_METHOD, Header.TARGET_PATH, Header.TARGET_SCHEME, Header.TARGET_AUTHORITY, Header.TARGET_HOST, Header.VERSION);
    private static final List<ByteString> SPDY_3_SKIPPED_RESPONSE_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TRANSFER_ENCODING);
    private static final ByteString TE = ByteString.encodeUtf8("te");
    private static final ByteString TRANSFER_ENCODING = ByteString.encodeUtf8("transfer-encoding");
    private static final ByteString UPGRADE = ByteString.encodeUtf8("upgrade");
    private final FramedConnection framedConnection;
    private HttpEngine httpEngine;
    private FramedStream stream;
    private final StreamAllocation streamAllocation;

    class StreamFinishingSource extends ForwardingSource {
        public StreamFinishingSource(Source delegate) {
            super(delegate);
        }

        public void close() throws IOException {
            Http2xStream.this.streamAllocation.streamFinished(Http2xStream.this);
            super.close();
        }
    }

    public Http2xStream(StreamAllocation streamAllocation, FramedConnection framedConnection) {
        this.streamAllocation = streamAllocation;
        this.framedConnection = framedConnection;
    }

    public void setHttpEngine(HttpEngine httpEngine) {
        this.httpEngine = httpEngine;
    }

    public Sink createRequestBody(Request request, long contentLength) throws IOException {
        return this.stream.getSink();
    }

    public void writeRequestHeaders(Request request) throws IOException {
        if (this.stream == null) {
            List<com.squareup.okhttp.internal.framed.Header> requestHeaders;
            this.httpEngine.writingRequestHeaders();
            boolean permitsRequestBody = this.httpEngine.permitsRequestBody(request);
            if (this.framedConnection.getProtocol() == Protocol.HTTP_2) {
                requestHeaders = http2HeadersList(request);
            } else {
                requestHeaders = spdy3HeadersList(request);
            }
            this.stream = this.framedConnection.newStream(requestHeaders, permitsRequestBody, true);
            this.stream.readTimeout().timeout((long) this.httpEngine.client.getReadTimeout(), TimeUnit.MILLISECONDS);
            this.stream.writeTimeout().timeout((long) this.httpEngine.client.getWriteTimeout(), TimeUnit.MILLISECONDS);
        }
    }

    public void writeRequestBody(RetryableSink requestBody) throws IOException {
        requestBody.writeToSocket(this.stream.getSink());
    }

    public void finishRequest() throws IOException {
        this.stream.getSink().close();
    }

    public Builder readResponseHeaders() throws IOException {
        if (this.framedConnection.getProtocol() == Protocol.HTTP_2) {
            return readHttp2HeadersList(this.stream.getResponseHeaders());
        }
        return readSpdy3HeadersList(this.stream.getResponseHeaders());
    }

    public static List<Header> spdy3HeadersList(Request request) {
        Headers headers = request.headers();
        List<com.squareup.okhttp.internal.framed.Header> result = new ArrayList(headers.size() + 5);
        result.add(new Header(Header.TARGET_METHOD, request.method()));
        result.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.httpUrl())));
        result.add(new Header(Header.VERSION, "HTTP/1.1"));
        result.add(new Header(Header.TARGET_HOST, Util.hostHeader(request.httpUrl(), false)));
        result.add(new Header(Header.TARGET_SCHEME, request.httpUrl().scheme()));
        Set<okio.ByteString> names = new LinkedHashSet();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            ByteString name = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            if (!SPDY_3_SKIPPED_REQUEST_HEADERS.contains(name)) {
                String value = headers.value(i);
                if (names.add(name)) {
                    result.add(new Header(name, value));
                } else {
                    for (int j = 0; j < result.size(); j++) {
                        if (((Header) result.get(j)).name.equals(name)) {
                            result.set(j, new Header(name, joinOnNull(((Header) result.get(j)).value.utf8(), value)));
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    private static String joinOnNull(String first, String second) {
        return 0 + second;
    }

    public static List<Header> http2HeadersList(Request request) {
        Headers headers = request.headers();
        List<com.squareup.okhttp.internal.framed.Header> result = new ArrayList(headers.size() + 4);
        result.add(new Header(Header.TARGET_METHOD, request.method()));
        result.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.httpUrl())));
        result.add(new Header(Header.TARGET_AUTHORITY, Util.hostHeader(request.httpUrl(), false)));
        result.add(new Header(Header.TARGET_SCHEME, request.httpUrl().scheme()));
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            ByteString name = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            if (!HTTP_2_SKIPPED_REQUEST_HEADERS.contains(name)) {
                result.add(new Header(name, headers.value(i)));
            }
        }
        return result;
    }

    public static Builder readSpdy3HeadersList(List<Header> headerBlock) throws IOException {
        String status = null;
        String version = "HTTP/1.1";
        Headers.Builder headersBuilder = new Headers.Builder();
        int size = headerBlock.size();
        for (int i = 0; i < size; i++) {
            ByteString name = ((Header) headerBlock.get(i)).name;
            String values = ((Header) headerBlock.get(i)).value.utf8();
            int start = 0;
            while (start < values.length()) {
                int end = values.indexOf(0, start);
                if (end == -1) {
                    end = values.length();
                }
                String value = values.substring(start, end);
                if (name.equals(Header.RESPONSE_STATUS)) {
                    status = value;
                } else if (name.equals(Header.VERSION)) {
                    version = value;
                } else if (!SPDY_3_SKIPPED_RESPONSE_HEADERS.contains(name)) {
                    headersBuilder.add(name.utf8(), value);
                }
                start = end + 1;
            }
        }
        if (status == null) {
            throw new ProtocolException("Expected ':status' header not present");
        }
        StatusLine statusLine = StatusLine.parse(version + " " + status);
        return new Builder().protocol(Protocol.SPDY_3).code(statusLine.code).message(statusLine.message).headers(headersBuilder.build());
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
                headersBuilder.add(name.utf8(), value);
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
