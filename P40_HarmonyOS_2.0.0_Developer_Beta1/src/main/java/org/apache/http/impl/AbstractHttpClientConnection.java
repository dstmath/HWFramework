package org.apache.http.impl;

import java.io.IOException;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.impl.entity.EntityDeserializer;
import org.apache.http.impl.entity.EntitySerializer;
import org.apache.http.impl.entity.LaxContentLengthStrategy;
import org.apache.http.impl.entity.StrictContentLengthStrategy;
import org.apache.http.impl.io.HttpRequestWriter;
import org.apache.http.impl.io.HttpResponseParser;
import org.apache.http.impl.io.SocketInputBuffer;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageWriter;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.params.HttpParams;

@Deprecated
public abstract class AbstractHttpClientConnection implements HttpClientConnection {
    private final EntityDeserializer entitydeserializer = createEntityDeserializer();
    private final EntitySerializer entityserializer = createEntitySerializer();
    private SessionInputBuffer inbuffer = null;
    private HttpConnectionMetricsImpl metrics = null;
    private SessionOutputBuffer outbuffer = null;
    private HttpMessageWriter requestWriter = null;
    private HttpMessageParser responseParser = null;

    /* access modifiers changed from: protected */
    public abstract void assertOpen() throws IllegalStateException;

    /* access modifiers changed from: protected */
    public EntityDeserializer createEntityDeserializer() {
        return new EntityDeserializer(new LaxContentLengthStrategy());
    }

    /* access modifiers changed from: protected */
    public EntitySerializer createEntitySerializer() {
        return new EntitySerializer(new StrictContentLengthStrategy());
    }

    /* access modifiers changed from: protected */
    public HttpResponseFactory createHttpResponseFactory() {
        return new DefaultHttpResponseFactory();
    }

    /* access modifiers changed from: protected */
    public HttpMessageParser createResponseParser(SessionInputBuffer buffer, HttpResponseFactory responseFactory, HttpParams params) {
        return new HttpResponseParser(buffer, null, responseFactory, params);
    }

    /* access modifiers changed from: protected */
    public HttpMessageWriter createRequestWriter(SessionOutputBuffer buffer, HttpParams params) {
        return new HttpRequestWriter(buffer, null, params);
    }

    /* access modifiers changed from: protected */
    public void init(SessionInputBuffer inbuffer2, SessionOutputBuffer outbuffer2, HttpParams params) {
        if (inbuffer2 == null) {
            throw new IllegalArgumentException("Input session buffer may not be null");
        } else if (outbuffer2 != null) {
            this.inbuffer = inbuffer2;
            this.outbuffer = outbuffer2;
            this.responseParser = createResponseParser(inbuffer2, createHttpResponseFactory(), params);
            this.requestWriter = createRequestWriter(outbuffer2, params);
            this.metrics = new HttpConnectionMetricsImpl(inbuffer2.getMetrics(), outbuffer2.getMetrics());
        } else {
            throw new IllegalArgumentException("Output session buffer may not be null");
        }
    }

    @Override // org.apache.http.HttpClientConnection
    public boolean isResponseAvailable(int timeout) throws IOException {
        assertOpen();
        return this.inbuffer.isDataAvailable(timeout);
    }

    @Override // org.apache.http.HttpClientConnection
    public void sendRequestHeader(HttpRequest request) throws HttpException, IOException {
        if (request != null) {
            assertOpen();
            this.requestWriter.write(request);
            this.metrics.incrementRequestCount();
            return;
        }
        throw new IllegalArgumentException("HTTP request may not be null");
    }

    @Override // org.apache.http.HttpClientConnection
    public void sendRequestEntity(HttpEntityEnclosingRequest request) throws HttpException, IOException {
        if (request != null) {
            assertOpen();
            if (request.getEntity() != null) {
                this.entityserializer.serialize(this.outbuffer, request, request.getEntity());
                return;
            }
            return;
        }
        throw new IllegalArgumentException("HTTP request may not be null");
    }

    /* access modifiers changed from: protected */
    public void doFlush() throws IOException {
        this.outbuffer.flush();
    }

    @Override // org.apache.http.HttpClientConnection
    public void flush() throws IOException {
        assertOpen();
        doFlush();
    }

    @Override // org.apache.http.HttpClientConnection
    public HttpResponse receiveResponseHeader() throws HttpException, IOException {
        assertOpen();
        HttpResponse response = (HttpResponse) this.responseParser.parse();
        if (response.getStatusLine().getStatusCode() >= 200) {
            this.metrics.incrementResponseCount();
        }
        return response;
    }

    @Override // org.apache.http.HttpClientConnection
    public void receiveResponseEntity(HttpResponse response) throws HttpException, IOException {
        if (response != null) {
            assertOpen();
            response.setEntity(this.entitydeserializer.deserialize(this.inbuffer, response));
            return;
        }
        throw new IllegalArgumentException("HTTP response may not be null");
    }

    @Override // org.apache.http.HttpConnection
    public boolean isStale() {
        if (!isOpen()) {
            return true;
        }
        try {
            if (this.inbuffer instanceof SocketInputBuffer) {
                return ((SocketInputBuffer) this.inbuffer).isStale();
            }
            this.inbuffer.isDataAvailable(1);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    @Override // org.apache.http.HttpConnection
    public HttpConnectionMetrics getMetrics() {
        return this.metrics;
    }
}
