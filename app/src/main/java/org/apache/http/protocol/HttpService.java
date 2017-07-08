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
    private ConnectionReuseStrategy connStrategy;
    private HttpExpectationVerifier expectationVerifier;
    private HttpRequestHandlerResolver handlerResolver;
    private HttpParams params;
    private HttpProcessor processor;
    private HttpResponseFactory responseFactory;

    public HttpService(HttpProcessor proc, ConnectionReuseStrategy connStrategy, HttpResponseFactory responseFactory) {
        this.params = null;
        this.processor = null;
        this.handlerResolver = null;
        this.connStrategy = null;
        this.responseFactory = null;
        this.expectationVerifier = null;
        setHttpProcessor(proc);
        setConnReuseStrategy(connStrategy);
        setResponseFactory(responseFactory);
    }

    public void setHttpProcessor(HttpProcessor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("HTTP processor may not be null.");
        }
        this.processor = processor;
    }

    public void setConnReuseStrategy(ConnectionReuseStrategy connStrategy) {
        if (connStrategy == null) {
            throw new IllegalArgumentException("Connection reuse strategy may not be null");
        }
        this.connStrategy = connStrategy;
    }

    public void setResponseFactory(HttpResponseFactory responseFactory) {
        if (responseFactory == null) {
            throw new IllegalArgumentException("Response factory may not be null");
        }
        this.responseFactory = responseFactory;
    }

    public void setHandlerResolver(HttpRequestHandlerResolver handlerResolver) {
        this.handlerResolver = handlerResolver;
    }

    public void setExpectationVerifier(HttpExpectationVerifier expectationVerifier) {
        this.expectationVerifier = expectationVerifier;
    }

    public HttpParams getParams() {
        return this.params;
    }

    public void setParams(HttpParams params) {
        this.params = params;
    }

    public void handleRequest(HttpServerConnection conn, HttpContext context) throws IOException, HttpException {
        context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
        HttpResponse httpResponse = null;
        try {
            HttpRequest request = conn.receiveRequestHeader();
            request.setParams(new DefaultedHttpParams(request.getParams(), this.params));
            ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
            if (!ver.lessEquals(HttpVersion.HTTP_1_1)) {
                ver = HttpVersion.HTTP_1_1;
            }
            if (request instanceof HttpEntityEnclosingRequest) {
                if (((HttpEntityEnclosingRequest) request).expectContinue()) {
                    httpResponse = this.responseFactory.newHttpResponse(ver, 100, context);
                    httpResponse.setParams(new DefaultedHttpParams(httpResponse.getParams(), this.params));
                    if (this.expectationVerifier != null) {
                        try {
                            this.expectationVerifier.verify(request, httpResponse, context);
                        } catch (HttpException ex) {
                            httpResponse = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_0, HttpStatus.SC_INTERNAL_SERVER_ERROR, context);
                            httpResponse.setParams(new DefaultedHttpParams(httpResponse.getParams(), this.params));
                            handleException(ex, httpResponse);
                        }
                    }
                    if (httpResponse.getStatusLine().getStatusCode() < HttpStatus.SC_OK) {
                        conn.sendResponseHeader(httpResponse);
                        conn.flush();
                        httpResponse = null;
                        conn.receiveRequestEntity((HttpEntityEnclosingRequest) request);
                    }
                } else {
                    conn.receiveRequestEntity((HttpEntityEnclosingRequest) request);
                }
            }
            if (httpResponse == null) {
                httpResponse = this.responseFactory.newHttpResponse(ver, HttpStatus.SC_OK, context);
                httpResponse.setParams(new DefaultedHttpParams(httpResponse.getParams(), this.params));
                context.setAttribute(ExecutionContext.HTTP_REQUEST, request);
                context.setAttribute(ExecutionContext.HTTP_RESPONSE, httpResponse);
                this.processor.process(request, context);
                doService(request, httpResponse, context);
            }
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                if (entity != null) {
                    entity.consumeContent();
                }
            }
        } catch (HttpException ex2) {
            httpResponse = this.responseFactory.newHttpResponse(HttpVersion.HTTP_1_0, HttpStatus.SC_INTERNAL_SERVER_ERROR, context);
            httpResponse.setParams(new DefaultedHttpParams(httpResponse.getParams(), this.params));
            handleException(ex2, httpResponse);
        }
        this.processor.process(httpResponse, context);
        conn.sendResponseHeader(httpResponse);
        conn.sendResponseEntity(httpResponse);
        conn.flush();
        if (!this.connStrategy.keepAlive(httpResponse, context)) {
            conn.close();
        }
    }

    protected void handleException(HttpException ex, HttpResponse response) {
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

    protected void doService(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
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
