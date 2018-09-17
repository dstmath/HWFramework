package com.android.okhttp.internal.huc;

import com.android.okhttp.Handshake;
import com.android.okhttp.Headers;
import com.android.okhttp.MediaType;
import com.android.okhttp.Request;
import com.android.okhttp.RequestBody;
import com.android.okhttp.Response;
import com.android.okhttp.Response.Builder;
import com.android.okhttp.ResponseBody;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.http.HttpMethod;
import com.android.okhttp.internal.http.OkHeaders;
import com.android.okhttp.internal.http.StatusLine;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.Okio;
import com.android.okhttp.okio.Sink;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SecureCacheResponse;
import java.net.URI;
import java.net.URLConnection;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;

public final class JavaApiConverter {
    private static final RequestBody EMPTY_REQUEST_BODY = null;

    /* renamed from: com.android.okhttp.internal.huc.JavaApiConverter.1 */
    static class AnonymousClass1 extends SecureCacheResponse {
        final /* synthetic */ ResponseBody val$body;
        final /* synthetic */ Handshake val$handshake;
        final /* synthetic */ Headers val$headers;
        final /* synthetic */ Response val$response;

        AnonymousClass1(Handshake val$handshake, Headers val$headers, Response val$response, ResponseBody val$body) {
            this.val$handshake = val$handshake;
            this.val$headers = val$headers;
            this.val$response = val$response;
            this.val$body = val$body;
        }

        public String getCipherSuite() {
            return this.val$handshake != null ? this.val$handshake.cipherSuite() : null;
        }

        public List<Certificate> getLocalCertificateChain() {
            if (this.val$handshake == null) {
                return null;
            }
            List<Certificate> certificates = this.val$handshake.localCertificates();
            if (certificates.size() <= 0) {
                certificates = null;
            }
            return certificates;
        }

        public List<Certificate> getServerCertificateChain() throws SSLPeerUnverifiedException {
            if (this.val$handshake == null) {
                return null;
            }
            List<Certificate> certificates = this.val$handshake.peerCertificates();
            if (certificates.size() <= 0) {
                certificates = null;
            }
            return certificates;
        }

        public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
            if (this.val$handshake == null) {
                return null;
            }
            return this.val$handshake.peerPrincipal();
        }

        public Principal getLocalPrincipal() {
            if (this.val$handshake == null) {
                return null;
            }
            return this.val$handshake.localPrincipal();
        }

        public Map<String, List<String>> getHeaders() throws IOException {
            return OkHeaders.toMultimap(this.val$headers, StatusLine.get(this.val$response).toString());
        }

        public InputStream getBody() throws IOException {
            if (this.val$body == null) {
                return null;
            }
            return this.val$body.byteStream();
        }
    }

    /* renamed from: com.android.okhttp.internal.huc.JavaApiConverter.2 */
    static class AnonymousClass2 extends CacheResponse {
        final /* synthetic */ ResponseBody val$body;
        final /* synthetic */ Headers val$headers;
        final /* synthetic */ Response val$response;

        AnonymousClass2(Headers val$headers, Response val$response, ResponseBody val$body) {
            this.val$headers = val$headers;
            this.val$response = val$response;
            this.val$body = val$body;
        }

        public Map<String, List<String>> getHeaders() throws IOException {
            return OkHeaders.toMultimap(this.val$headers, StatusLine.get(this.val$response).toString());
        }

        public InputStream getBody() throws IOException {
            if (this.val$body == null) {
                return null;
            }
            return this.val$body.byteStream();
        }
    }

    /* renamed from: com.android.okhttp.internal.huc.JavaApiConverter.3 */
    static class AnonymousClass3 extends CacheRequest {
        final /* synthetic */ com.android.okhttp.internal.http.CacheRequest val$okCacheRequest;

        AnonymousClass3(com.android.okhttp.internal.http.CacheRequest val$okCacheRequest) {
            this.val$okCacheRequest = val$okCacheRequest;
        }

        public void abort() {
            this.val$okCacheRequest.abort();
        }

        public OutputStream getBody() throws IOException {
            Sink body = this.val$okCacheRequest.body();
            if (body == null) {
                return null;
            }
            return Okio.buffer(body).outputStream();
        }
    }

    /* renamed from: com.android.okhttp.internal.huc.JavaApiConverter.4 */
    static class AnonymousClass4 extends ResponseBody {
        private BufferedSource body;
        final /* synthetic */ CacheResponse val$cacheResponse;
        final /* synthetic */ Headers val$okHeaders;

        AnonymousClass4(Headers val$okHeaders, CacheResponse val$cacheResponse) {
            this.val$okHeaders = val$okHeaders;
            this.val$cacheResponse = val$cacheResponse;
        }

        public MediaType contentType() {
            String contentTypeHeader = this.val$okHeaders.get("Content-Type");
            if (contentTypeHeader == null) {
                return null;
            }
            return MediaType.parse(contentTypeHeader);
        }

        public long contentLength() {
            return OkHeaders.contentLength(this.val$okHeaders);
        }

        public BufferedSource source() throws IOException {
            if (this.body == null) {
                this.body = Okio.buffer(Okio.source(this.val$cacheResponse.getBody()));
            }
            return this.body;
        }
    }

    /* renamed from: com.android.okhttp.internal.huc.JavaApiConverter.5 */
    static class AnonymousClass5 extends ResponseBody {
        private BufferedSource body;
        final /* synthetic */ URLConnection val$urlConnection;

        AnonymousClass5(URLConnection val$urlConnection) {
            this.val$urlConnection = val$urlConnection;
        }

        public MediaType contentType() {
            String contentTypeHeader = this.val$urlConnection.getContentType();
            if (contentTypeHeader == null) {
                return null;
            }
            return MediaType.parse(contentTypeHeader);
        }

        public long contentLength() {
            return JavaApiConverter.stringToLong(this.val$urlConnection.getHeaderField("Content-Length"));
        }

        public BufferedSource source() throws IOException {
            if (this.body == null) {
                this.body = Okio.buffer(Okio.source(this.val$urlConnection.getInputStream()));
            }
            return this.body;
        }
    }

    private static final class CacheHttpURLConnection extends HttpURLConnection {
        private final Request request;
        private final Response response;

        public CacheHttpURLConnection(Response response) {
            super(response.request().url());
            this.request = response.request();
            this.response = response;
            this.connected = true;
            this.doOutput = this.request.body() != null;
            this.doInput = true;
            this.useCaches = true;
            this.method = this.request.method();
        }

        public void connect() throws IOException {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public void disconnect() {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public void setRequestProperty(String key, String value) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public void addRequestProperty(String key, String value) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public String getRequestProperty(String key) {
            return this.request.header(key);
        }

        public Map<String, List<String>> getRequestProperties() {
            return OkHeaders.toMultimap(this.request.headers(), null);
        }

        public void setFixedLengthStreamingMode(int contentLength) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public void setFixedLengthStreamingMode(long contentLength) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public void setChunkedStreamingMode(int chunklen) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public void setInstanceFollowRedirects(boolean followRedirects) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public boolean getInstanceFollowRedirects() {
            return super.getInstanceFollowRedirects();
        }

        public void setRequestMethod(String method) throws ProtocolException {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public String getRequestMethod() {
            return this.request.method();
        }

        public String getHeaderFieldKey(int position) {
            if (position < 0) {
                throw new IllegalArgumentException("Invalid header index: " + position);
            } else if (position == 0) {
                return null;
            } else {
                return this.response.headers().name(position - 1);
            }
        }

        public String getHeaderField(int position) {
            if (position < 0) {
                throw new IllegalArgumentException("Invalid header index: " + position);
            } else if (position == 0) {
                return StatusLine.get(this.response).toString();
            } else {
                return this.response.headers().value(position - 1);
            }
        }

        public String getHeaderField(String fieldName) {
            if (fieldName == null) {
                return StatusLine.get(this.response).toString();
            }
            return this.response.headers().get(fieldName);
        }

        public Map<String, List<String>> getHeaderFields() {
            return OkHeaders.toMultimap(this.response.headers(), StatusLine.get(this.response).toString());
        }

        public int getResponseCode() throws IOException {
            return this.response.code();
        }

        public String getResponseMessage() throws IOException {
            return this.response.message();
        }

        public InputStream getErrorStream() {
            return null;
        }

        public boolean usingProxy() {
            return false;
        }

        public void setConnectTimeout(int timeout) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public int getConnectTimeout() {
            return 0;
        }

        public void setReadTimeout(int timeout) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public int getReadTimeout() {
            return 0;
        }

        public Object getContent() throws IOException {
            throw JavaApiConverter.throwResponseBodyAccessException();
        }

        public Object getContent(Class[] classes) throws IOException {
            throw JavaApiConverter.throwResponseBodyAccessException();
        }

        public InputStream getInputStream() throws IOException {
            throw JavaApiConverter.throwResponseBodyAccessException();
        }

        public OutputStream getOutputStream() throws IOException {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public void setDoInput(boolean doInput) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public boolean getDoInput() {
            return this.doInput;
        }

        public void setDoOutput(boolean doOutput) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public boolean getDoOutput() {
            return this.doOutput;
        }

        public void setAllowUserInteraction(boolean allowUserInteraction) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public boolean getAllowUserInteraction() {
            return false;
        }

        public void setUseCaches(boolean useCaches) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public boolean getUseCaches() {
            return super.getUseCaches();
        }

        public void setIfModifiedSince(long ifModifiedSince) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public long getIfModifiedSince() {
            return JavaApiConverter.stringToLong(this.request.headers().get("If-Modified-Since"));
        }

        public boolean getDefaultUseCaches() {
            return super.getDefaultUseCaches();
        }

        public void setDefaultUseCaches(boolean defaultUseCaches) {
            super.setDefaultUseCaches(defaultUseCaches);
        }
    }

    private static final class CacheHttpsURLConnection extends DelegatingHttpsURLConnection {
        private final CacheHttpURLConnection delegate;

        public CacheHttpsURLConnection(CacheHttpURLConnection delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        protected Handshake handshake() {
            return this.delegate.response.handshake();
        }

        public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public HostnameVerifier getHostnameVerifier() {
            throw JavaApiConverter.throwRequestSslAccessException();
        }

        public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
            throw JavaApiConverter.throwRequestModificationException();
        }

        public SSLSocketFactory getSSLSocketFactory() {
            throw JavaApiConverter.throwRequestSslAccessException();
        }

        public void setFixedLengthStreamingMode(long contentLength) {
            this.delegate.setFixedLengthStreamingMode(contentLength);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.huc.JavaApiConverter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.huc.JavaApiConverter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.huc.JavaApiConverter.<clinit>():void");
    }

    private JavaApiConverter() {
    }

    public static Response createOkResponseForCachePut(URI uri, URLConnection urlConnection) throws IOException {
        HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;
        Builder okResponseBuilder = new Builder();
        Headers varyHeaders = varyHeaders(urlConnection, createHeaders(urlConnection.getHeaderFields()));
        if (varyHeaders == null) {
            return null;
        }
        RequestBody requestBody;
        String requestMethod = httpUrlConnection.getRequestMethod();
        if (HttpMethod.requiresRequestBody(requestMethod)) {
            requestBody = EMPTY_REQUEST_BODY;
        } else {
            requestBody = null;
        }
        okResponseBuilder.request(new Request.Builder().url(uri.toString()).method(requestMethod, requestBody).headers(varyHeaders).build());
        StatusLine statusLine = StatusLine.parse(extractStatusLine(httpUrlConnection));
        okResponseBuilder.protocol(statusLine.protocol);
        okResponseBuilder.code(statusLine.code);
        okResponseBuilder.message(statusLine.message);
        okResponseBuilder.networkResponse(okResponseBuilder.build());
        okResponseBuilder.headers(extractOkResponseHeaders(httpUrlConnection));
        okResponseBuilder.body(createOkBody(urlConnection));
        if (httpUrlConnection instanceof HttpsURLConnection) {
            Object[] serverCertificates;
            HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) httpUrlConnection;
            try {
                serverCertificates = httpsUrlConnection.getServerCertificates();
            } catch (SSLPeerUnverifiedException e) {
                serverCertificates = null;
            }
            okResponseBuilder.handshake(Handshake.get(httpsUrlConnection.getCipherSuite(), nullSafeImmutableList(serverCertificates), nullSafeImmutableList(httpsUrlConnection.getLocalCertificates())));
        }
        return okResponseBuilder.build();
    }

    private static Headers createHeaders(Map<String, List<String>> headers) {
        Headers.Builder builder = new Headers.Builder();
        for (Entry<String, List<String>> header : headers.entrySet()) {
            if (!(header.getKey() == null || header.getValue() == null)) {
                String name = ((String) header.getKey()).trim();
                for (String value : (List) header.getValue()) {
                    Internal.instance.addLenient(builder, name, value.trim());
                }
            }
        }
        return builder.build();
    }

    private static Headers varyHeaders(URLConnection urlConnection, Headers responseHeaders) {
        if (OkHeaders.hasVaryAll(responseHeaders)) {
            return null;
        }
        Set<String> varyFields = OkHeaders.varyFields(responseHeaders);
        if (varyFields.isEmpty()) {
            return new Headers.Builder().build();
        }
        boolean z;
        if (urlConnection instanceof CacheHttpURLConnection) {
            z = true;
        } else {
            z = urlConnection instanceof CacheHttpsURLConnection;
        }
        if (!z) {
            return null;
        }
        Map<String, List<String>> requestProperties = urlConnection.getRequestProperties();
        Headers.Builder result = new Headers.Builder();
        for (String fieldName : varyFields) {
            List<String> fieldValues = (List) requestProperties.get(fieldName);
            if (fieldValues != null) {
                for (String fieldValue : fieldValues) {
                    Internal.instance.addLenient(result, fieldName, fieldValue);
                }
            } else if (fieldName.equals("Accept-Encoding")) {
                result.add("Accept-Encoding", "gzip");
            }
        }
        return result.build();
    }

    static Response createOkResponseForCacheGet(Request request, CacheResponse javaResponse) throws IOException {
        Headers varyHeaders;
        Headers responseHeaders = createHeaders(javaResponse.getHeaders());
        if (OkHeaders.hasVaryAll(responseHeaders)) {
            varyHeaders = new Headers.Builder().build();
        } else {
            varyHeaders = OkHeaders.varyHeaders(request.headers(), responseHeaders);
        }
        Request cacheRequest = new Request.Builder().url(request.httpUrl()).method(request.method(), null).headers(varyHeaders).build();
        Builder okResponseBuilder = new Builder();
        okResponseBuilder.request(cacheRequest);
        StatusLine statusLine = StatusLine.parse(extractStatusLine(javaResponse));
        okResponseBuilder.protocol(statusLine.protocol);
        okResponseBuilder.code(statusLine.code);
        okResponseBuilder.message(statusLine.message);
        Headers okHeaders = extractOkHeaders(javaResponse);
        okResponseBuilder.headers(okHeaders);
        okResponseBuilder.body(createOkBody(okHeaders, javaResponse));
        if (javaResponse instanceof SecureCacheResponse) {
            List<Certificate> peerCertificates;
            SecureCacheResponse javaSecureCacheResponse = (SecureCacheResponse) javaResponse;
            try {
                peerCertificates = javaSecureCacheResponse.getServerCertificateChain();
            } catch (SSLPeerUnverifiedException e) {
                peerCertificates = Collections.emptyList();
            }
            List<Certificate> localCertificates = javaSecureCacheResponse.getLocalCertificateChain();
            if (localCertificates == null) {
                localCertificates = Collections.emptyList();
            }
            okResponseBuilder.handshake(Handshake.get(javaSecureCacheResponse.getCipherSuite(), peerCertificates, localCertificates));
        }
        return okResponseBuilder.build();
    }

    public static Request createOkRequest(URI uri, String requestMethod, Map<String, List<String>> requestHeaders) {
        RequestBody requestBody;
        if (HttpMethod.requiresRequestBody(requestMethod)) {
            requestBody = EMPTY_REQUEST_BODY;
        } else {
            requestBody = null;
        }
        Request.Builder builder = new Request.Builder().url(uri.toString()).method(requestMethod, requestBody);
        if (requestHeaders != null) {
            builder.headers(extractOkHeaders((Map) requestHeaders));
        }
        return builder.build();
    }

    public static CacheResponse createJavaCacheResponse(Response response) {
        Headers headers = response.headers();
        ResponseBody body = response.body();
        return response.request().isHttps() ? new AnonymousClass1(response.handshake(), headers, response, body) : new AnonymousClass2(headers, response, body);
    }

    public static CacheRequest createJavaCacheRequest(com.android.okhttp.internal.http.CacheRequest okCacheRequest) {
        return new AnonymousClass3(okCacheRequest);
    }

    static HttpURLConnection createJavaUrlConnectionForCachePut(Response okResponse) {
        if (okResponse.request().isHttps()) {
            return new CacheHttpsURLConnection(new CacheHttpURLConnection(okResponse));
        }
        return new CacheHttpURLConnection(okResponse);
    }

    static Map<String, List<String>> extractJavaHeaders(Request request) {
        return OkHeaders.toMultimap(request.headers(), null);
    }

    private static Headers extractOkHeaders(CacheResponse javaResponse) throws IOException {
        return extractOkHeaders(javaResponse.getHeaders());
    }

    private static Headers extractOkResponseHeaders(HttpURLConnection httpUrlConnection) {
        return extractOkHeaders(httpUrlConnection.getHeaderFields());
    }

    static Headers extractOkHeaders(Map<String, List<String>> javaHeaders) {
        Headers.Builder okHeadersBuilder = new Headers.Builder();
        for (Entry<String, List<String>> javaHeader : javaHeaders.entrySet()) {
            String name = (String) javaHeader.getKey();
            if (name != null) {
                for (String value : (List) javaHeader.getValue()) {
                    Internal.instance.addLenient(okHeadersBuilder, name, value);
                }
            }
        }
        return okHeadersBuilder.build();
    }

    private static String extractStatusLine(HttpURLConnection httpUrlConnection) {
        return httpUrlConnection.getHeaderField(null);
    }

    private static String extractStatusLine(CacheResponse javaResponse) throws IOException {
        return extractStatusLine(javaResponse.getHeaders());
    }

    static String extractStatusLine(Map<String, List<String>> javaResponseHeaders) throws ProtocolException {
        List<String> values = (List) javaResponseHeaders.get(null);
        if (values != null && values.size() != 0) {
            return (String) values.get(0);
        }
        throw new ProtocolException("CacheResponse is missing a 'null' header containing the status line. Headers=" + javaResponseHeaders);
    }

    private static ResponseBody createOkBody(Headers okHeaders, CacheResponse cacheResponse) {
        return new AnonymousClass4(okHeaders, cacheResponse);
    }

    private static ResponseBody createOkBody(URLConnection urlConnection) {
        if (urlConnection.getDoInput()) {
            return new AnonymousClass5(urlConnection);
        }
        return null;
    }

    private static RuntimeException throwRequestModificationException() {
        throw new UnsupportedOperationException("ResponseCache cannot modify the request.");
    }

    private static RuntimeException throwRequestHeaderAccessException() {
        throw new UnsupportedOperationException("ResponseCache cannot access request headers");
    }

    private static RuntimeException throwRequestSslAccessException() {
        throw new UnsupportedOperationException("ResponseCache cannot access SSL internals");
    }

    private static RuntimeException throwResponseBodyAccessException() {
        throw new UnsupportedOperationException("ResponseCache cannot access the response body.");
    }

    private static <T> List<T> nullSafeImmutableList(T[] elements) {
        return elements == null ? Collections.emptyList() : Util.immutableList((Object[]) elements);
    }

    private static long stringToLong(String s) {
        long j = -1;
        if (s == null) {
            return j;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return j;
        }
    }
}
