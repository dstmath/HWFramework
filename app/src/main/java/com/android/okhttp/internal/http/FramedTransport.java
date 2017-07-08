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
import com.android.okhttp.okio.Okio;
import com.android.okhttp.okio.Sink;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class FramedTransport implements Transport {
    private static final List<ByteString> HTTP_2_PROHIBITED_HEADERS = null;
    private static final List<ByteString> SPDY_3_PROHIBITED_HEADERS = null;
    private final FramedConnection framedConnection;
    private final HttpEngine httpEngine;
    private FramedStream stream;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.http.FramedTransport.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.http.FramedTransport.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.FramedTransport.<clinit>():void");
    }

    public FramedTransport(HttpEngine httpEngine, FramedConnection framedConnection) {
        this.httpEngine = httpEngine;
        this.framedConnection = framedConnection;
    }

    public Sink createRequestBody(Request request, long contentLength) throws IOException {
        return this.stream.getSink();
    }

    public void writeRequestHeaders(Request request) throws IOException {
        if (this.stream == null) {
            this.httpEngine.writingRequestHeaders();
            this.stream = this.framedConnection.newStream(writeNameValueBlock(request, this.framedConnection.getProtocol(), RequestLine.version(this.httpEngine.getConnection().getProtocol())), this.httpEngine.permitsRequestBody(), true);
            this.stream.readTimeout().timeout((long) this.httpEngine.client.getReadTimeout(), TimeUnit.MILLISECONDS);
        }
    }

    public void writeRequestBody(RetryableSink requestBody) throws IOException {
        requestBody.writeToSocket(this.stream.getSink());
    }

    public void finishRequest() throws IOException {
        this.stream.getSink().close();
    }

    public Builder readResponseHeaders() throws IOException {
        return readNameValueBlock(this.stream.getResponseHeaders(), this.framedConnection.getProtocol());
    }

    public static List<Header> writeNameValueBlock(Request request, Protocol protocol, String version) {
        Headers headers = request.headers();
        List<com.squareup.okhttp.internal.framed.Header> result = new ArrayList(headers.size() + 10);
        result.add(new Header(Header.TARGET_METHOD, request.method()));
        result.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.httpUrl())));
        String host = Util.hostHeader(request.httpUrl());
        if (Protocol.SPDY_3 == protocol) {
            result.add(new Header(Header.VERSION, version));
            result.add(new Header(Header.TARGET_HOST, host));
        } else if (Protocol.HTTP_2 == protocol) {
            result.add(new Header(Header.TARGET_AUTHORITY, host));
        } else {
            throw new AssertionError();
        }
        result.add(new Header(Header.TARGET_SCHEME, request.httpUrl().scheme()));
        Set<okio.ByteString> names = new LinkedHashSet();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            ByteString name = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            String value = headers.value(i);
            if (!(isProhibitedHeader(protocol, name) || name.equals(Header.TARGET_METHOD) || name.equals(Header.TARGET_PATH) || name.equals(Header.TARGET_SCHEME) || name.equals(Header.TARGET_AUTHORITY) || name.equals(Header.TARGET_HOST) || name.equals(Header.VERSION))) {
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
        return '\u0000' + second;
    }

    public static Builder readNameValueBlock(List<Header> headerBlock, Protocol protocol) throws IOException {
        String status = null;
        String version = "HTTP/1.1";
        Headers.Builder headersBuilder = new Headers.Builder();
        headersBuilder.set(OkHeaders.SELECTED_PROTOCOL, protocol.toString());
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
                } else if (!isProhibitedHeader(protocol, name)) {
                    headersBuilder.add(name.utf8(), value);
                }
                start = end + 1;
            }
        }
        if (status == null) {
            throw new ProtocolException("Expected ':status' header not present");
        }
        StatusLine statusLine = StatusLine.parse(version + " " + status);
        return new Builder().protocol(protocol).code(statusLine.code).message(statusLine.message).headers(headersBuilder.build());
    }

    public ResponseBody openResponseBody(Response response) throws IOException {
        return new RealResponseBody(response.headers(), Okio.buffer(this.stream.getSource()));
    }

    public void releaseConnectionOnIdle() {
    }

    public void disconnect(HttpEngine engine) throws IOException {
        if (this.stream != null) {
            this.stream.close(ErrorCode.CANCEL);
        }
    }

    public boolean canReuseConnection() {
        return true;
    }

    private static boolean isProhibitedHeader(Protocol protocol, ByteString name) {
        if (protocol == Protocol.SPDY_3) {
            return SPDY_3_PROHIBITED_HEADERS.contains(name);
        }
        if (protocol == Protocol.HTTP_2) {
            return HTTP_2_PROHIBITED_HEADERS.contains(name);
        }
        throw new AssertionError(protocol);
    }
}
