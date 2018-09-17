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
    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(null, new byte[0]);

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

        public long getContentLengthLong() {
            return this.delegate.getContentLengthLong();
        }

        public void setFixedLengthStreamingMode(long contentLength) {
            this.delegate.setFixedLengthStreamingMode(contentLength);
        }

        public long getHeaderFieldLong(String field, long defaultValue) {
            return this.delegate.getHeaderFieldLong(field, defaultValue);
        }
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
        RequestBody placeholderBody;
        String requestMethod = httpUrlConnection.getRequestMethod();
        if (HttpMethod.requiresRequestBody(requestMethod)) {
            placeholderBody = EMPTY_REQUEST_BODY;
        } else {
            placeholderBody = null;
        }
        okResponseBuilder.request(new Request.Builder().url(uri.toString()).method(requestMethod, placeholderBody).headers(varyHeaders).build());
        StatusLine statusLine = StatusLine.parse(extractStatusLine(httpUrlConnection));
        okResponseBuilder.protocol(statusLine.protocol);
        okResponseBuilder.code(statusLine.code);
        okResponseBuilder.message(statusLine.message);
        okResponseBuilder.networkResponse(okResponseBuilder.build());
        okResponseBuilder.headers(extractOkResponseHeaders(httpUrlConnection));
        okResponseBuilder.body(createOkBody(urlConnection));
        if (httpUrlConnection instanceof HttpsURLConnection) {
            Object[] peerCertificates;
            HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) httpUrlConnection;
            try {
                peerCertificates = httpsUrlConnection.getServerCertificates();
            } catch (SSLPeerUnverifiedException e) {
                peerCertificates = null;
            }
            okResponseBuilder.handshake(Handshake.get(httpsUrlConnection.getCipherSuite(), nullSafeImmutableList(peerCertificates), nullSafeImmutableList(httpsUrlConnection.getLocalCertificates())));
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
        RequestBody placeholderBody;
        if (HttpMethod.requiresRequestBody(requestMethod)) {
            placeholderBody = EMPTY_REQUEST_BODY;
        } else {
            placeholderBody = null;
        }
        Request.Builder builder = new Request.Builder().url(uri.toString()).method(requestMethod, placeholderBody);
        if (requestHeaders != null) {
            builder.headers(extractOkHeaders((Map) requestHeaders));
        }
        return builder.build();
    }

    public static CacheResponse createJavaCacheResponse(final Response response) {
        final Headers headers = response.headers();
        final ResponseBody body = response.body();
        if (!response.request().isHttps()) {
            return new CacheResponse() {
                public Map<String, List<String>> getHeaders() throws IOException {
                    return OkHeaders.toMultimap(headers, StatusLine.get(response).toString());
                }

                public InputStream getBody() throws IOException {
                    if (body == null) {
                        return null;
                    }
                    return body.byteStream();
                }
            };
        }
        final Handshake handshake = response.handshake();
        return new SecureCacheResponse() {
            public String getCipherSuite() {
                return handshake != null ? handshake.cipherSuite() : null;
            }

            public List<Certificate> getLocalCertificateChain() {
                if (handshake == null) {
                    return null;
                }
                List<Certificate> certificates = handshake.localCertificates();
                if (certificates.size() <= 0) {
                    certificates = null;
                }
                return certificates;
            }

            public List<Certificate> getServerCertificateChain() throws SSLPeerUnverifiedException {
                if (handshake == null) {
                    return null;
                }
                List<Certificate> certificates = handshake.peerCertificates();
                if (certificates.size() <= 0) {
                    certificates = null;
                }
                return certificates;
            }

            public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
                if (handshake == null) {
                    return null;
                }
                return handshake.peerPrincipal();
            }

            public Principal getLocalPrincipal() {
                if (handshake == null) {
                    return null;
                }
                return handshake.localPrincipal();
            }

            public Map<String, List<String>> getHeaders() throws IOException {
                return OkHeaders.toMultimap(headers, StatusLine.get(response).toString());
            }

            public InputStream getBody() throws IOException {
                if (body == null) {
                    return null;
                }
                return body.byteStream();
            }
        };
    }

    public static CacheRequest createJavaCacheRequest(final com.android.okhttp.internal.http.CacheRequest okCacheRequest) {
        return new CacheRequest() {
            public void abort() {
                okCacheRequest.abort();
            }

            public OutputStream getBody() throws IOException {
                Sink body = okCacheRequest.body();
                if (body == null) {
                    return null;
                }
                return Okio.buffer(body).outputStream();
            }
        };
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

    private static ResponseBody createOkBody(final Headers okHeaders, final CacheResponse cacheResponse) {
        return new ResponseBody() {
            private BufferedSource body;

            public MediaType contentType() {
                String contentTypeHeader = okHeaders.get("Content-Type");
                if (contentTypeHeader == null) {
                    return null;
                }
                return MediaType.parse(contentTypeHeader);
            }

            public long contentLength() {
                return OkHeaders.contentLength(okHeaders);
            }

            public BufferedSource source() throws IOException {
                if (this.body == null) {
                    this.body = Okio.buffer(Okio.source(cacheResponse.getBody()));
                }
                return this.body;
            }
        };
    }

    private static ResponseBody createOkBody(final URLConnection urlConnection) {
        if (urlConnection.getDoInput()) {
            return new ResponseBody() {
                private BufferedSource body;

                public MediaType contentType() {
                    String contentTypeHeader = urlConnection.getContentType();
                    if (contentTypeHeader == null) {
                        return null;
                    }
                    return MediaType.parse(contentTypeHeader);
                }

                public long contentLength() {
                    return JavaApiConverter.stringToLong(urlConnection.getHeaderField("Content-Length"));
                }

                public BufferedSource source() throws IOException {
                    if (this.body == null) {
                        this.body = Okio.buffer(Okio.source(urlConnection.getInputStream()));
                    }
                    return this.body;
                }
            };
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
