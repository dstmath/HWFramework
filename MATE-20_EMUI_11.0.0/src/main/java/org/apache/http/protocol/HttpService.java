package org.apache.http.protocol;

import java.io.IOException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;
import org.apache.http.UnsupportedHttpVersionException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EncodingUtils;

@Deprecated
public class HttpService {
    private ConnectionReuseStrategy connStrategy = null;
    private HttpExpectationVerifier expectationVerifier = null;
    private HttpRequestHandlerResolver handlerResolver = null;
    private HttpParams params = null;
    private HttpProcessor processor = null;
    private HttpResponseFactory responseFactory = null;

    public HttpService(HttpProcessor proc, ConnectionReuseStrategy connStrategy2, HttpResponseFactory responseFactory2) {
        setHttpProcessor(proc);
        setConnReuseStrategy(connStrategy2);
        setResponseFactory(responseFactory2);
    }

    public void setHttpProcessor(HttpProcessor processor2) {
        if (processor2 != null) {
            this.processor = processor2;
            return;
        }
        throw new IllegalArgumentException("HTTP processor may not be null.");
    }

    public void setConnReuseStrategy(ConnectionReuseStrategy connStrategy2) {
        if (connStrategy2 != null) {
            this.connStrategy = connStrategy2;
            return;
        }
        throw new IllegalArgumentException("Connection reuse strategy may not be null");
    }

    public void setResponseFactory(HttpResponseFactory responseFactory2) {
        if (responseFactory2 != null) {
            this.responseFactory = responseFactory2;
            return;
        }
        throw new IllegalArgumentException("Response factory may not be null");
    }

    public void setHandlerResolver(HttpRequestHandlerResolver handlerResolver2) {
        this.handlerResolver = handlerResolver2;
    }

    public void setExpectationVerifier(HttpExpectationVerifier expectationVerifier2) {
        this.expectationVerifier = expectationVerifier2;
    }

    public HttpParams getParams() {
        return this.params;
    }

    public void setParams(HttpParams params2) {
        this.params = params2;
    }

    public void handleRequest(HttpServerConnection conn, HttpContext context) throws IOException, HttpException {
        HttpEntity entity;
        context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
        HttpResponse response = null;
        try {
            HttpRequest request = conn.receiveRequestHeader();
            request.setParams(new DefaultedHttpParams(request.getParams(), this.params));
            ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
            if (!ver.lessEquals(HttpVersion.HTTP_1_1)) {
                ver = HttpVersion.HTTP_1_1;
            }
            if (request instanceof HttpEntityEnclosingRequest) {
                if (((HttpEntityEnclosingRequest) request).expectContinue()) {
                    response = this.responseFactory.newHttpResponse(ver, 100, context);
                    response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                    if (this.expectationVerifier != null) {
                        try {
                            this.expectationVerifier.verify(request, response, context);
                        } catch (HttpException ex) {
                            response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_0, HttpStatus.SC_INTERNAL_SERVER_ERROR, context);
                            response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                            handleException(ex, response);
                        }
                    }
                    if (response.getStatusLine().getStatusCode() < 200) {
                        conn.sendResponseHeader(response);
                        conn.flush();
                        response = null;
                        conn.receiveRequestEntity((HttpEntityEnclosingRequest) request);
                    }
                } else {
                    conn.receiveRequestEntity((HttpEntityEnclosingRequest) request);
                }
            }
            if (response == null) {
                response = this.responseFactory.newHttpResponse(ver, HttpStatus.SC_OK, context);
                response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
                context.setAttribute(ExecutionContext.HTTP_REQUEST, request);
                context.setAttribute(ExecutionContext.HTTP_RESPONSE, response);
                this.processor.process(request, context);
                doService(request, response, context);
            }
            if ((request instanceof HttpEntityEnclosingRequest) && (entity = ((HttpEntityEnclosingRequest) request).getEntity()) != null) {
                entity.consumeContent();
            }
        } catch (HttpException ex2) {
            response = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_0, HttpStatus.SC_INTERNAL_SERVER_ERROR, context);
            response.setParams(new DefaultedHttpParams(response.getParams(), this.params));
            handleException(ex2, response);
        }
        this.processor.process(response, context);
        conn.sendResponseHeader(response);
        conn.sendResponseEntity(response);
        conn.flush();
        if (!this.connStrategy.keepAlive(response, context)) {
            conn.close();
        }
    }

    /* access modifiers changed from: protected */
    public void handleException(HttpException ex, HttpResponse response) {
        if (ex instanceof MethodNotSupportedException) {
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        } else if (ex instanceof UnsupportedHttpVersionException) {
            response.setStatusCode(HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED);
        } else if (ex instanceof ProtocolException) {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        } else {
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        ByteArrayEntity entity = new ByteArrayEntity(EncodingUtils.getAsciiBytes(ex.getMessage()));
        entity.setContentType("text/plain; charset=US-ASCII");
        response.setEntity(entity);
    }

    /* access modifiers changed from: protected */
    public void doService(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        HttpRequestHandler handler = null;
        if (this.handlerResolver != null) {
            handler = this.handlerResolver.lookup(request.getRequestLine().getUri());
        }
        if (handler != null) {
            handler.handle(request, response, context);
        } else {
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
        }
    }
}
