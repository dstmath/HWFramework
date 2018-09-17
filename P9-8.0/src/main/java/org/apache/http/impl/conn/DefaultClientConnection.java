package org.apache.http.impl.conn;

import java.io.IOException;
import java.net.Socket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.impl.SocketHttpClientConnection;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.params.HttpParams;

@Deprecated
public class DefaultClientConnection extends SocketHttpClientConnection implements OperatedClientConnection {
    private boolean connSecure;
    private final Log headerLog = LogFactory.getLog("org.apache.http.headers");
    private final Log log = LogFactory.getLog(getClass());
    private volatile boolean shutdown;
    private volatile Socket socket;
    private HttpHost targetHost;
    private final Log wireLog = LogFactory.getLog("org.apache.http.wire");

    public final HttpHost getTargetHost() {
        return this.targetHost;
    }

    public final boolean isSecure() {
        return this.connSecure;
    }

    public final Socket getSocket() {
        return this.socket;
    }

    public void opening(Socket sock, HttpHost target) throws IOException {
        assertNotOpen();
        this.socket = sock;
        this.targetHost = target;
        if (this.shutdown) {
            sock.close();
            throw new IOException("Connection already shutdown");
        }
    }

    public void openCompleted(boolean secure, HttpParams params) throws IOException {
        assertNotOpen();
        if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        this.connSecure = secure;
        bind(this.socket, params);
    }

    public void shutdown() throws IOException {
        this.log.debug("Connection shut down");
        this.shutdown = true;
        super.shutdown();
        Socket sock = this.socket;
        if (sock != null) {
            sock.close();
        }
    }

    public void close() throws IOException {
        this.log.debug("Connection closed");
        super.close();
    }

    protected SessionInputBuffer createSessionInputBuffer(Socket socket, int buffersize, HttpParams params) throws IOException {
        SessionInputBuffer inbuffer = super.createSessionInputBuffer(socket, buffersize, params);
        if (this.wireLog.isDebugEnabled()) {
            return new LoggingSessionInputBuffer(inbuffer, new Wire(this.wireLog));
        }
        return inbuffer;
    }

    protected SessionOutputBuffer createSessionOutputBuffer(Socket socket, int buffersize, HttpParams params) throws IOException {
        SessionOutputBuffer outbuffer = super.createSessionOutputBuffer(socket, buffersize, params);
        if (this.wireLog.isDebugEnabled()) {
            return new LoggingSessionOutputBuffer(outbuffer, new Wire(this.wireLog));
        }
        return outbuffer;
    }

    protected HttpMessageParser createResponseParser(SessionInputBuffer buffer, HttpResponseFactory responseFactory, HttpParams params) {
        return new DefaultResponseParser(buffer, null, responseFactory, params);
    }

    public void update(Socket sock, HttpHost target, boolean secure, HttpParams params) throws IOException {
        assertOpen();
        if (target == null) {
            throw new IllegalArgumentException("Target host must not be null.");
        } else if (params == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        } else {
            if (sock != null) {
                this.socket = sock;
                bind(sock, params);
            }
            this.targetHost = target;
            this.connSecure = secure;
        }
    }

    public HttpResponse receiveResponseHeader() throws HttpException, IOException {
        HttpResponse response = super.receiveResponseHeader();
        if (this.headerLog.isDebugEnabled()) {
            this.headerLog.debug("<< " + response.getStatusLine().toString());
            for (Header header : response.getAllHeaders()) {
                this.headerLog.debug("<< " + header.toString());
            }
        }
        return response;
    }

    public void sendRequestHeader(HttpRequest request) throws HttpException, IOException {
        super.sendRequestHeader(request);
        if (this.headerLog.isDebugEnabled()) {
            this.headerLog.debug(">> " + request.getRequestLine().toString());
            for (Header header : request.getAllHeaders()) {
                this.headerLog.debug(">> " + header.toString());
            }
        }
    }
}
