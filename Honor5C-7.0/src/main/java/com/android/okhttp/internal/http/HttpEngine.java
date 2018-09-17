package com.android.okhttp.internal.http;

import com.android.okhttp.Address;
import com.android.okhttp.CertificatePinner;
import com.android.okhttp.Connection;
import com.android.okhttp.ConnectionPool;
import com.android.okhttp.Headers;
import com.android.okhttp.HttpUrl;
import com.android.okhttp.Interceptor;
import com.android.okhttp.Interceptor.Chain;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.Protocol;
import com.android.okhttp.Request;
import com.android.okhttp.Response;
import com.android.okhttp.Response.Builder;
import com.android.okhttp.ResponseBody;
import com.android.okhttp.Route;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.InternalCache;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.Version;
import com.android.okhttp.internal.http.CacheStrategy.Factory;
import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSink;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.GzipSource;
import com.android.okhttp.okio.Okio;
import com.android.okhttp.okio.Sink;
import com.android.okhttp.okio.Source;
import com.android.okhttp.okio.Timeout;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.CookieHandler;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;

public final class HttpEngine {
    private static final ResponseBody EMPTY_BODY = null;
    public static final int MAX_FOLLOW_UPS = 20;
    private Address address;
    public final boolean bufferRequestBody;
    private BufferedSink bufferedRequestBody;
    private Response cacheResponse;
    private CacheStrategy cacheStrategy;
    private final boolean callerWritesRequestBody;
    final OkHttpClient client;
    private Connection connection;
    private final boolean forWebSocket;
    private Request networkRequest;
    private final Response priorResponse;
    private Sink requestBodyOut;
    private Route route;
    private RouteSelector routeSelector;
    long sentRequestMillis;
    private CacheRequest storeRequest;
    private boolean transparentGzip;
    private Transport transport;
    private final Request userRequest;
    private Response userResponse;

    /* renamed from: com.android.okhttp.internal.http.HttpEngine.2 */
    class AnonymousClass2 implements Source {
        boolean cacheRequestClosed;
        final /* synthetic */ BufferedSink val$cacheBody;
        final /* synthetic */ CacheRequest val$cacheRequest;
        final /* synthetic */ BufferedSource val$source;

        AnonymousClass2(BufferedSource val$source, CacheRequest val$cacheRequest, BufferedSink val$cacheBody) {
            this.val$source = val$source;
            this.val$cacheRequest = val$cacheRequest;
            this.val$cacheBody = val$cacheBody;
        }

        public long read(Buffer sink, long byteCount) throws IOException {
            try {
                long bytesRead = this.val$source.read(sink, byteCount);
                if (bytesRead == -1) {
                    if (!this.cacheRequestClosed) {
                        this.cacheRequestClosed = true;
                        this.val$cacheBody.close();
                    }
                    return -1;
                }
                sink.copyTo(this.val$cacheBody.buffer(), sink.size() - bytesRead, bytesRead);
                this.val$cacheBody.emitCompleteSegments();
                return bytesRead;
            } catch (IOException e) {
                if (!this.cacheRequestClosed) {
                    this.cacheRequestClosed = true;
                    this.val$cacheRequest.abort();
                }
                throw e;
            }
        }

        public Timeout timeout() {
            return this.val$source.timeout();
        }

        public void close() throws IOException {
            if (!(this.cacheRequestClosed || Util.discard(this, 100, TimeUnit.MILLISECONDS))) {
                this.cacheRequestClosed = true;
                this.val$cacheRequest.abort();
            }
            this.val$source.close();
        }
    }

    class NetworkInterceptorChain implements Chain {
        private int calls;
        private final int index;
        private final Request request;

        NetworkInterceptorChain(int index, Request request) {
            this.index = index;
            this.request = request;
        }

        public Connection connection() {
            return HttpEngine.this.connection;
        }

        public Request request() {
            return this.request;
        }

        public Response proceed(Request request) throws IOException {
            this.calls++;
            if (this.index > 0) {
                Interceptor caller = (Interceptor) HttpEngine.this.client.networkInterceptors().get(this.index - 1);
                Address address = connection().getRoute().getAddress();
                if (!request.httpUrl().rfc2732host().equals(address.getRfc2732Host()) || request.httpUrl().port() != address.getUriPort()) {
                    throw new IllegalStateException("network interceptor " + caller + " must retain the same host and port");
                } else if (this.calls > 1) {
                    throw new IllegalStateException("network interceptor " + caller + " must call proceed() exactly once");
                }
            }
            if (this.index < HttpEngine.this.client.networkInterceptors().size()) {
                NetworkInterceptorChain chain = new NetworkInterceptorChain(this.index + 1, request);
                Interceptor interceptor = (Interceptor) HttpEngine.this.client.networkInterceptors().get(this.index);
                Response interceptedResponse = interceptor.intercept(chain);
                if (chain.calls == 1) {
                    return interceptedResponse;
                }
                throw new IllegalStateException("network interceptor " + interceptor + " must call proceed() exactly once");
            }
            HttpEngine.this.transport.writeRequestHeaders(request);
            HttpEngine.this.networkRequest = request;
            if (HttpEngine.this.permitsRequestBody() && request.body() != null) {
                BufferedSink bufferedRequestBody = Okio.buffer(HttpEngine.this.transport.createRequestBody(request, request.body().contentLength()));
                request.body().writeTo(bufferedRequestBody);
                bufferedRequestBody.close();
            }
            Response response = HttpEngine.this.readNetworkResponse();
            int code = response.code();
            if ((code != 204 && code != 205) || response.body().contentLength() <= 0) {
                return response;
            }
            throw new ProtocolException("HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.http.HttpEngine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.http.HttpEngine.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.<clinit>():void");
    }

    public HttpEngine(OkHttpClient client, Request request, boolean bufferRequestBody, boolean callerWritesRequestBody, boolean forWebSocket, Connection connection, RouteSelector routeSelector, RetryableSink requestBodyOut, Response priorResponse) {
        this.sentRequestMillis = -1;
        this.client = client;
        this.userRequest = request;
        this.bufferRequestBody = bufferRequestBody;
        this.callerWritesRequestBody = callerWritesRequestBody;
        this.forWebSocket = forWebSocket;
        this.connection = connection;
        this.routeSelector = routeSelector;
        this.requestBodyOut = requestBodyOut;
        this.priorResponse = priorResponse;
        if (connection != null) {
            Internal.instance.setOwner(connection, this);
            this.route = connection.getRoute();
            return;
        }
        this.route = null;
    }

    public void sendRequest() throws RequestException, RouteException, IOException {
        if (this.cacheStrategy == null) {
            if (this.transport != null) {
                throw new IllegalStateException();
            }
            Response response;
            Request request = networkRequest(this.userRequest);
            InternalCache responseCache = Internal.instance.internalCache(this.client);
            if (responseCache != null) {
                response = responseCache.get(request);
            } else {
                response = null;
            }
            this.cacheStrategy = new Factory(System.currentTimeMillis(), request, response).get();
            this.networkRequest = this.cacheStrategy.networkRequest;
            this.cacheResponse = this.cacheStrategy.cacheResponse;
            if (responseCache != null) {
                responseCache.trackResponse(this.cacheStrategy);
            }
            if (response != null && this.cacheResponse == null) {
                Util.closeQuietly(response.body());
            }
            if (this.networkRequest != null) {
                if (this.connection == null) {
                    connect();
                }
                this.transport = Internal.instance.newTransport(this.connection, this);
                if (this.callerWritesRequestBody && permitsRequestBody() && this.requestBodyOut == null) {
                    long contentLength = OkHeaders.contentLength(request);
                    if (!this.bufferRequestBody) {
                        this.transport.writeRequestHeaders(this.networkRequest);
                        this.requestBodyOut = this.transport.createRequestBody(this.networkRequest, contentLength);
                    } else if (contentLength > 2147483647L) {
                        throw new IllegalStateException("Use setFixedLengthStreamingMode() or setChunkedStreamingMode() for requests larger than 2 GiB.");
                    } else if (contentLength != -1) {
                        this.transport.writeRequestHeaders(this.networkRequest);
                        this.requestBodyOut = new RetryableSink((int) contentLength);
                    } else {
                        this.requestBodyOut = new RetryableSink();
                    }
                }
            } else {
                if (this.connection != null) {
                    Internal.instance.recycle(this.client.getConnectionPool(), this.connection);
                    this.connection = null;
                }
                if (this.cacheResponse != null) {
                    this.userResponse = this.cacheResponse.newBuilder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).cacheResponse(stripBody(this.cacheResponse)).build();
                } else {
                    this.userResponse = new Builder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).protocol(Protocol.HTTP_1_1).code(504).message("Unsatisfiable Request (only-if-cached)").body(EMPTY_BODY).build();
                }
                this.userResponse = unzip(this.userResponse);
            }
        }
    }

    private static Response stripBody(Response response) {
        if (response == null || response.body() == null) {
            return response;
        }
        return response.newBuilder().body(null).build();
    }

    private void connect() throws RequestException, RouteException {
        if (this.connection != null) {
            throw new IllegalStateException();
        }
        if (this.routeSelector == null) {
            this.address = createAddress(this.client, this.networkRequest);
            try {
                this.routeSelector = RouteSelector.get(this.address, this.networkRequest, this.client);
            } catch (IOException e) {
                throw new RequestException(e);
            }
        }
        this.connection = createNextConnection();
        Internal.instance.connectAndSetOwner(this.client, this.connection, this, this.networkRequest);
        this.route = this.connection.getRoute();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Connection createNextConnection() throws RouteException {
        Connection pooled;
        ConnectionPool pool = this.client.getConnectionPool();
        while (true) {
            pooled = pool.get(this.address);
            if (pooled == null) {
                try {
                    return new Connection(pool, this.routeSelector.next());
                } catch (IOException e) {
                    throw new RouteException(e);
                }
            } else if (this.networkRequest.method().equals("GET") || Internal.instance.isReadable(pooled)) {
                return pooled;
            } else {
                Util.closeQuietly(pooled.getSocket());
            }
        }
        return pooled;
    }

    public void writingRequestHeaders() {
        if (this.sentRequestMillis != -1) {
            throw new IllegalStateException();
        }
        this.sentRequestMillis = System.currentTimeMillis();
    }

    boolean permitsRequestBody() {
        return HttpMethod.permitsRequestBody(this.userRequest.method());
    }

    public Sink getRequestBody() {
        if (this.cacheStrategy != null) {
            return this.requestBodyOut;
        }
        throw new IllegalStateException();
    }

    public BufferedSink getBufferedRequestBody() {
        BufferedSink bufferedSink = null;
        BufferedSink result = this.bufferedRequestBody;
        if (result != null) {
            return result;
        }
        Sink requestBody = getRequestBody();
        if (requestBody != null) {
            bufferedSink = Okio.buffer(requestBody);
            this.bufferedRequestBody = bufferedSink;
        }
        return bufferedSink;
    }

    public boolean hasResponse() {
        return this.userResponse != null;
    }

    public Request getRequest() {
        return this.userRequest;
    }

    public Response getResponse() {
        if (this.userResponse != null) {
            return this.userResponse;
        }
        throw new IllegalStateException();
    }

    public Connection getConnection() {
        return this.connection;
    }

    public HttpEngine recover(RouteException e) {
        if (!(this.routeSelector == null || this.connection == null)) {
            connectFailed(this.routeSelector, e.getLastConnectException());
        }
        if ((this.routeSelector == null && this.connection == null) || ((this.routeSelector != null && !this.routeSelector.hasNext()) || !isRecoverable(e))) {
            return null;
        }
        return new HttpEngine(this.client, this.userRequest, this.bufferRequestBody, this.callerWritesRequestBody, this.forWebSocket, close(), this.routeSelector, (RetryableSink) this.requestBodyOut, this.priorResponse);
    }

    private boolean isRecoverable(RouteException e) {
        if (!this.client.getRetryOnConnectionFailure()) {
            return false;
        }
        IOException ioe = e.getLastConnectException();
        if (ioe instanceof ProtocolException) {
            return false;
        }
        if (ioe instanceof InterruptedIOException) {
            return ioe instanceof SocketTimeoutException;
        }
        if (((ioe instanceof SSLHandshakeException) && (ioe.getCause() instanceof CertificateException)) || (ioe instanceof SSLPeerUnverifiedException)) {
            return false;
        }
        return true;
    }

    public HttpEngine recover(IOException e, Sink requestBodyOut) {
        if (!(this.routeSelector == null || this.connection == null)) {
            connectFailed(this.routeSelector, e);
        }
        boolean z = requestBodyOut != null ? requestBodyOut instanceof RetryableSink : true;
        if ((this.routeSelector == null && this.connection == null) || ((this.routeSelector != null && !this.routeSelector.hasNext()) || !isRecoverable(e) || !z)) {
            return null;
        }
        return new HttpEngine(this.client, this.userRequest, this.bufferRequestBody, this.callerWritesRequestBody, this.forWebSocket, close(), this.routeSelector, (RetryableSink) requestBodyOut, this.priorResponse);
    }

    private void connectFailed(RouteSelector routeSelector, IOException e) {
        if (Internal.instance.recycleCount(this.connection) <= 0) {
            routeSelector.connectFailed(this.connection.getRoute(), e);
        }
    }

    public HttpEngine recover(IOException e) {
        return recover(e, this.requestBodyOut);
    }

    private boolean isRecoverable(IOException e) {
        if (!this.client.getRetryOnConnectionFailure() || (e instanceof ProtocolException) || (e instanceof InterruptedIOException)) {
            return false;
        }
        return true;
    }

    public Route getRoute() {
        return this.route;
    }

    private void maybeCache() throws IOException {
        InternalCache responseCache = Internal.instance.internalCache(this.client);
        if (responseCache != null) {
            if (CacheStrategy.isCacheable(this.userResponse, this.networkRequest)) {
                this.storeRequest = responseCache.put(stripBody(this.userResponse));
                return;
            }
            if (HttpMethod.invalidatesCache(this.networkRequest.method())) {
                try {
                    responseCache.remove(this.networkRequest);
                } catch (IOException e) {
                }
            }
        }
    }

    public void releaseConnection() throws IOException {
        if (!(this.transport == null || this.connection == null)) {
            this.transport.releaseConnectionOnIdle();
        }
        this.connection = null;
    }

    public void disconnect() {
        try {
            if (this.transport != null) {
                this.transport.disconnect(this);
                return;
            }
            Connection connection = this.connection;
            if (connection != null) {
                Internal.instance.closeIfOwnedBy(connection, this);
            }
        } catch (IOException e) {
        }
    }

    public Connection close() {
        if (this.bufferedRequestBody != null) {
            Util.closeQuietly(this.bufferedRequestBody);
        } else if (this.requestBodyOut != null) {
            Util.closeQuietly(this.requestBodyOut);
        }
        if (this.userResponse == null) {
            if (this.connection != null) {
                Util.closeQuietly(this.connection.getSocket());
            }
            this.connection = null;
            return null;
        }
        Util.closeQuietly(this.userResponse.body());
        if (this.transport == null || this.connection == null || this.transport.canReuseConnection()) {
            if (!(this.connection == null || Internal.instance.clearOwner(this.connection))) {
                this.connection = null;
            }
            Connection result = this.connection;
            this.connection = null;
            return result;
        }
        Util.closeQuietly(this.connection.getSocket());
        this.connection = null;
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Response unzip(Response response) throws IOException {
        if (!this.transparentGzip || !"gzip".equalsIgnoreCase(this.userResponse.header("Content-Encoding")) || response.body() == null) {
            return response;
        }
        Source responseBody = new GzipSource(response.body().source());
        Headers strippedHeaders = response.headers().newBuilder().removeAll("Content-Encoding").removeAll("Content-Length").build();
        return response.newBuilder().headers(strippedHeaders).body(new RealResponseBody(strippedHeaders, Okio.buffer(responseBody))).build();
    }

    public static boolean hasBody(Response response) {
        if (response.request().method().equals("HEAD")) {
            return false;
        }
        int responseCode = response.code();
        return (((responseCode >= 100 && responseCode < 200) || responseCode == 204 || responseCode == 304) && OkHeaders.contentLength(response) == -1 && !"chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) ? false : true;
    }

    private Request networkRequest(Request request) throws IOException {
        Request.Builder result = request.newBuilder();
        if (request.header("Host") == null) {
            result.header("Host", Util.hostHeader(request.httpUrl()));
        }
        if ((this.connection == null || this.connection.getProtocol() != Protocol.HTTP_1_0) && request.header("Connection") == null) {
            result.header("Connection", "Keep-Alive");
        }
        if (request.header("Accept-Encoding") == null) {
            this.transparentGzip = true;
            result.header("Accept-Encoding", "gzip");
        }
        CookieHandler cookieHandler = this.client.getCookieHandler();
        if (cookieHandler != null) {
            OkHeaders.addCookies(result, cookieHandler.get(request.uri(), OkHeaders.toMultimap(result.build().headers(), null)));
        }
        if (request.header("User-Agent") == null) {
            result.header("User-Agent", Version.userAgent());
        }
        return result.build();
    }

    public void readResponse() throws IOException {
        if (this.userResponse == null) {
            if (this.networkRequest == null && this.cacheResponse == null) {
                throw new IllegalStateException("call sendRequest() first!");
            } else if (this.networkRequest != null) {
                Response networkResponse;
                if (this.forWebSocket) {
                    this.transport.writeRequestHeaders(this.networkRequest);
                    networkResponse = readNetworkResponse();
                } else if (this.callerWritesRequestBody) {
                    if (this.bufferedRequestBody != null && this.bufferedRequestBody.buffer().size() > 0) {
                        this.bufferedRequestBody.emit();
                    }
                    if (this.sentRequestMillis == -1) {
                        if (OkHeaders.contentLength(this.networkRequest) == -1 && (this.requestBodyOut instanceof RetryableSink)) {
                            this.networkRequest = this.networkRequest.newBuilder().header("Content-Length", Long.toString(((RetryableSink) this.requestBodyOut).contentLength())).build();
                        }
                        this.transport.writeRequestHeaders(this.networkRequest);
                    }
                    if (this.requestBodyOut != null) {
                        if (this.bufferedRequestBody != null) {
                            this.bufferedRequestBody.close();
                        } else {
                            this.requestBodyOut.close();
                        }
                        if (this.requestBodyOut instanceof RetryableSink) {
                            this.transport.writeRequestBody((RetryableSink) this.requestBodyOut);
                        }
                    }
                    networkResponse = readNetworkResponse();
                } else {
                    networkResponse = new NetworkInterceptorChain(0, this.networkRequest).proceed(this.networkRequest);
                }
                receiveHeaders(networkResponse.headers());
                if (this.cacheResponse != null) {
                    if (validate(this.cacheResponse, networkResponse)) {
                        this.userResponse = this.cacheResponse.newBuilder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).headers(combine(this.cacheResponse.headers(), networkResponse.headers())).cacheResponse(stripBody(this.cacheResponse)).networkResponse(stripBody(networkResponse)).build();
                        networkResponse.body().close();
                        releaseConnection();
                        InternalCache responseCache = Internal.instance.internalCache(this.client);
                        responseCache.trackConditionalCacheHit();
                        responseCache.update(this.cacheResponse, stripBody(this.userResponse));
                        this.userResponse = unzip(this.userResponse);
                        return;
                    }
                    Util.closeQuietly(this.cacheResponse.body());
                }
                this.userResponse = networkResponse.newBuilder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).cacheResponse(stripBody(this.cacheResponse)).networkResponse(stripBody(networkResponse)).build();
                if (hasBody(this.userResponse)) {
                    maybeCache();
                    this.userResponse = unzip(cacheWritingResponse(this.storeRequest, this.userResponse));
                }
            }
        }
    }

    private Response readNetworkResponse() throws IOException {
        this.transport.finishRequest();
        Response networkResponse = this.transport.readResponseHeaders().request(this.networkRequest).handshake(this.connection.getHandshake()).header(OkHeaders.SENT_MILLIS, Long.toString(this.sentRequestMillis)).header(OkHeaders.RECEIVED_MILLIS, Long.toString(System.currentTimeMillis())).build();
        if (!this.forWebSocket) {
            networkResponse = networkResponse.newBuilder().body(this.transport.openResponseBody(networkResponse)).build();
        }
        Internal.instance.setProtocol(this.connection, networkResponse.protocol());
        return networkResponse;
    }

    private Response cacheWritingResponse(CacheRequest cacheRequest, Response response) throws IOException {
        if (cacheRequest == null) {
            return response;
        }
        Sink cacheBodyUnbuffered = cacheRequest.body();
        if (cacheBodyUnbuffered == null) {
            return response;
        }
        return response.newBuilder().body(new RealResponseBody(response.headers(), Okio.buffer(new AnonymousClass2(response.body().source(), cacheRequest, Okio.buffer(cacheBodyUnbuffered))))).build();
    }

    private static boolean validate(Response cached, Response network) {
        if (network.code() == 304) {
            return true;
        }
        Date lastModified = cached.headers().getDate("Last-Modified");
        if (lastModified != null) {
            Date networkLastModified = network.headers().getDate("Last-Modified");
            if (networkLastModified != null && networkLastModified.getTime() < lastModified.getTime()) {
                return true;
            }
        }
        return false;
    }

    private static Headers combine(Headers cachedHeaders, Headers networkHeaders) throws IOException {
        int i;
        Headers.Builder result = new Headers.Builder();
        int size = cachedHeaders.size();
        for (i = 0; i < size; i++) {
            String fieldName = cachedHeaders.name(i);
            String value = cachedHeaders.value(i);
            if (!("Warning".equalsIgnoreCase(fieldName) && value.startsWith("1")) && (!OkHeaders.isEndToEnd(fieldName) || networkHeaders.get(fieldName) == null)) {
                result.add(fieldName, value);
            }
        }
        size = networkHeaders.size();
        for (i = 0; i < size; i++) {
            fieldName = networkHeaders.name(i);
            if (!"Content-Length".equalsIgnoreCase(fieldName) && OkHeaders.isEndToEnd(fieldName)) {
                result.add(fieldName, networkHeaders.value(i));
            }
        }
        return result.build();
    }

    public void receiveHeaders(Headers headers) throws IOException {
        CookieHandler cookieHandler = this.client.getCookieHandler();
        if (cookieHandler != null) {
            cookieHandler.put(this.userRequest.uri(), OkHeaders.toMultimap(headers, null));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Request followUpRequest() throws IOException {
        if (this.userResponse == null) {
            throw new IllegalStateException();
        }
        Proxy selectedProxy;
        if (getRoute() != null) {
            selectedProxy = getRoute().getProxy();
        } else {
            selectedProxy = this.client.getProxy();
        }
        switch (this.userResponse.code()) {
            case 300:
            case 301:
            case 302:
            case 303:
                break;
            case StatusLine.HTTP_TEMP_REDIRECT /*307*/:
            case StatusLine.HTTP_PERM_REDIRECT /*308*/:
                if (!(this.userRequest.method().equals("GET") || this.userRequest.method().equals("HEAD"))) {
                    return null;
                }
            case 401:
                break;
            case 407:
                if (selectedProxy.type() != Type.HTTP) {
                    throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
                }
                break;
            default:
                return null;
        }
    }

    public boolean sameConnection(HttpUrl followUp) {
        HttpUrl url = this.userRequest.httpUrl();
        if (url.host().equals(followUp.host()) && url.port() == followUp.port()) {
            return url.scheme().equals(followUp.scheme());
        }
        return false;
    }

    private static Address createAddress(OkHttpClient client, Request request) {
        SSLSocketFactory sSLSocketFactory = null;
        HostnameVerifier hostnameVerifier = null;
        CertificatePinner certificatePinner = null;
        if (request.isHttps()) {
            sSLSocketFactory = client.getSslSocketFactory();
            hostnameVerifier = client.getHostnameVerifier();
            certificatePinner = client.getCertificatePinner();
        }
        return new Address(request.httpUrl().rfc2732host(), request.httpUrl().port(), client.getSocketFactory(), sSLSocketFactory, hostnameVerifier, certificatePinner, client.getAuthenticator(), client.getProxy(), client.getProtocols(), client.getConnectionSpecs(), client.getProxySelector());
    }
}
