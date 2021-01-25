package org.apache.http.impl;

import java.io.IOException;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.entity.EntityDeserializer;
import org.apache.http.impl.entity.EntitySerializer;
import org.apache.http.impl.entity.LaxContentLengthStrategy;
import org.apache.http.impl.entity.StrictContentLengthStrategy;
import org.apache.http.impl.io.HttpRequestParser;
import org.apache.http.impl.io.HttpResponseWriter;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageWriter;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.params.HttpParams;

@Deprecated
public abstract class AbstractHttpServerConnection implements HttpServerConnection {
    private final EntityDeserializer entitydeserializer = createEntityDeserializer();
    private final EntitySerializer entityserializer = createEntitySerializer();
    private SessionInputBuffer inbuffer = null;
    private HttpConnectionMetricsImpl metrics = null;
    private SessionOutputBuffer outbuffer = null;
    private HttpMessageParser requestParser = null;
    private HttpMessageWriter responseWriter = null;

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
    public HttpRequestFactory createHttpRequestFactory() {
        return new DefaultHttpRequestFactory();
    }

    /* access modifiers changed from: protected */
    public HttpMessageParser createRequestParser(SessionInputBuffer buffer, HttpRequestFactory requestFactory, HttpParams params) {
        return new HttpRequestParser(buffer, null, requestFactory, params);
    }

    /* access modifiers changed from: protected */
    public HttpMessageWriter createResponseWriter(SessionOutputBuffer buffer, HttpParams params) {
        return new HttpResponseWriter(buffer, null, params);
    }

    /* access modifiers changed from: protected */
    public void init(SessionInputBuffer inbuffer2, SessionOutputBuffer outbuffer2, HttpParams params) {
        if (inbuffer2 == null) {
            throw new IllegalArgumentException("Input session buffer may not be null");
        } else if (outbuffer2 != null) {
            this.inbuffer = inbuffer2;
            this.outbuffer = outbuffer2;
            this.requestParser = createRequestParser(inbuffer2, createHttpRequestFactory(), params);
            this.responseWriter = createResponseWriter(outbuffer2, params);
            this.metrics = new HttpConnectionMetricsImpl(inbuffer2.getMetrics(), outbuffer2.getMetrics());
        } else {
            throw new IllegalArgumentException("Output session buffer may not be null");
        }
    }

    @Override // org.apache.http.HttpServerConnection
    public HttpRequest receiveRequestHeader() throws HttpException, IOException {
        assertOpen();
        HttpRequest request = (HttpRequest) this.requestParser.parse();
        this.metrics.incrementRequestCount();
        return request;
    }

    @Override // org.apache.http.HttpServerConnection
    public void receiveRequestEntity(HttpEntityEnclosingRequest request) throws HttpException, IOException {
        if (request != null) {
            assertOpen();
            request.setEntity(this.entitydeserializer.deserialize(this.inbuffer, request));
            return;
        }
        throw new IllegalArgumentException("HTTP request may not be null");
    }

    /* access modifiers changed from: protected */
    public void doFlush() throws IOException {
        this.outbuffer.flush();
    }

    @Override // org.apache.http.HttpServerConnection
    public void flush() throws IOException {
        assertOpen();
        doFlush();
    }

    @Override // org.apache.http.HttpServerConnection
    public void sendResponseHeader(HttpResponse response) throws HttpException, IOException {
        if (response != null) {
            assertOpen();
            this.responseWriter.write(response);
            if (response.getStatusLine().getStatusCode() >= 200) {
                this.metrics.incrementResponseCount();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("HTTP response may not be null");
    }

    @Override // org.apache.http.HttpServerConnection
    public void sendResponseEntity(HttpResponse response) throws HttpException, IOException {
        if (response.getEntity() != null) {
            this.entityserializer.serialize(this.outbuffer, response, response.getEntity());
        }
    }

    @Override // org.apache.http.HttpConnection
    public boolean isStale() {
        assertOpen();
        try {
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
