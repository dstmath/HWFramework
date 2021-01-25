package android.net.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import org.apache.http.HttpConnection;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpInetConnection;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.HttpConnectionMetricsImpl;
import org.apache.http.impl.entity.EntitySerializer;
import org.apache.http.impl.entity.StrictContentLengthStrategy;
import org.apache.http.impl.io.ChunkedInputStream;
import org.apache.http.impl.io.ContentLengthInputStream;
import org.apache.http.impl.io.HttpRequestWriter;
import org.apache.http.impl.io.IdentityInputStream;
import org.apache.http.impl.io.SocketInputBuffer;
import org.apache.http.impl.io.SocketOutputBuffer;
import org.apache.http.io.HttpMessageWriter;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.CharArrayBuffer;

public class AndroidHttpClientConnection implements HttpInetConnection, HttpConnection {
    private final EntitySerializer entityserializer = new EntitySerializer(new StrictContentLengthStrategy());
    private SessionInputBuffer inbuffer = null;
    private int maxHeaderCount;
    private int maxLineLength;
    private HttpConnectionMetricsImpl metrics = null;
    private volatile boolean open;
    private SessionOutputBuffer outbuffer = null;
    private HttpMessageWriter requestWriter = null;
    private Socket socket = null;

    public void bind(Socket socket2, HttpParams params) throws IOException {
        if (socket2 == null) {
            throw new IllegalArgumentException("Socket may not be null");
        } else if (params != null) {
            assertNotOpen();
            socket2.setTcpNoDelay(HttpConnectionParams.getTcpNoDelay(params));
            socket2.setSoTimeout(HttpConnectionParams.getSoTimeout(params));
            int linger = HttpConnectionParams.getLinger(params);
            if (linger >= 0) {
                socket2.setSoLinger(linger > 0, linger);
            }
            this.socket = socket2;
            int buffersize = HttpConnectionParams.getSocketBufferSize(params);
            this.inbuffer = new SocketInputBuffer(socket2, buffersize, params);
            this.outbuffer = new SocketOutputBuffer(socket2, buffersize, params);
            this.maxHeaderCount = params.getIntParameter("http.connection.max-header-count", -1);
            this.maxLineLength = params.getIntParameter("http.connection.max-line-length", -1);
            this.requestWriter = new HttpRequestWriter(this.outbuffer, null, params);
            this.metrics = new HttpConnectionMetricsImpl(this.inbuffer.getMetrics(), this.outbuffer.getMetrics());
            this.open = true;
        } else {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getSimpleName());
        buffer.append("[");
        if (isOpen()) {
            buffer.append(getRemotePort());
        } else {
            buffer.append("closed");
        }
        buffer.append("]");
        return buffer.toString();
    }

    private void assertNotOpen() {
        if (this.open) {
            throw new IllegalStateException("Connection is already open");
        }
    }

    private void assertOpen() {
        if (!this.open) {
            throw new IllegalStateException("Connection is not open");
        }
    }

    @Override // org.apache.http.HttpConnection
    public boolean isOpen() {
        Socket socket2;
        return this.open && (socket2 = this.socket) != null && socket2.isConnected();
    }

    @Override // org.apache.http.HttpInetConnection
    public InetAddress getLocalAddress() {
        Socket socket2 = this.socket;
        if (socket2 != null) {
            return socket2.getLocalAddress();
        }
        return null;
    }

    @Override // org.apache.http.HttpInetConnection
    public int getLocalPort() {
        Socket socket2 = this.socket;
        if (socket2 != null) {
            return socket2.getLocalPort();
        }
        return -1;
    }

    @Override // org.apache.http.HttpInetConnection
    public InetAddress getRemoteAddress() {
        Socket socket2 = this.socket;
        if (socket2 != null) {
            return socket2.getInetAddress();
        }
        return null;
    }

    @Override // org.apache.http.HttpInetConnection
    public int getRemotePort() {
        Socket socket2 = this.socket;
        if (socket2 != null) {
            return socket2.getPort();
        }
        return -1;
    }

    @Override // org.apache.http.HttpConnection
    public void setSocketTimeout(int timeout) {
        assertOpen();
        Socket socket2 = this.socket;
        if (socket2 != null) {
            try {
                socket2.setSoTimeout(timeout);
            } catch (SocketException e) {
            }
        }
    }

    @Override // org.apache.http.HttpConnection
    public int getSocketTimeout() {
        Socket socket2 = this.socket;
        if (socket2 == null) {
            return -1;
        }
        try {
            return socket2.getSoTimeout();
        } catch (SocketException e) {
            return -1;
        }
    }

    @Override // org.apache.http.HttpConnection
    public void shutdown() throws IOException {
        this.open = false;
        Socket tmpsocket = this.socket;
        if (tmpsocket != null) {
            tmpsocket.close();
        }
    }

    /* JADX WARNING: Failed to process nested try/catch */
    @Override // org.apache.http.HttpConnection
    public void close() throws IOException {
        if (this.open) {
            this.open = false;
            doFlush();
            try {
                this.socket.shutdownOutput();
            } catch (IOException e) {
            } catch (IOException | UnsupportedOperationException e2) {
            }
            this.socket.shutdownInput();
            this.socket.close();
        }
    }

    public void sendRequestHeader(HttpRequest request) throws HttpException, IOException {
        if (request != null) {
            assertOpen();
            this.requestWriter.write(request);
            this.metrics.incrementRequestCount();
            return;
        }
        throw new IllegalArgumentException("HTTP request may not be null");
    }

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

    public void flush() throws IOException {
        assertOpen();
        doFlush();
    }

    public StatusLine parseResponseHeader(Headers headers) throws IOException, ParseException {
        assertOpen();
        int i = 64;
        CharArrayBuffer current = new CharArrayBuffer(64);
        int i2 = -1;
        if (this.inbuffer.readLine(current) != -1) {
            StatusLine statusline = BasicLineParser.DEFAULT.parseStatusLine(current, new ParserCursor(0, current.length()));
            int statusCode = statusline.getStatusCode();
            CharArrayBuffer previous = null;
            int headerNumber = 0;
            while (true) {
                if (current == null) {
                    current = new CharArrayBuffer(i);
                } else {
                    current.clear();
                }
                if (this.inbuffer.readLine(current) == i2 || current.length() < 1) {
                    break;
                }
                char first = current.charAt(0);
                if ((first == ' ' || first == '\t') && previous != null) {
                    int start = 0;
                    int length = current.length();
                    while (start < length) {
                        char ch = current.charAt(start);
                        if (ch != ' ' && ch != '\t') {
                            break;
                        }
                        start++;
                    }
                    if (this.maxLineLength <= 0 || ((previous.length() + 1) + current.length()) - start <= this.maxLineLength) {
                        previous.append(' ');
                        previous.append(current, start, current.length() - start);
                    } else {
                        throw new IOException("Maximum line length limit exceeded");
                    }
                } else {
                    if (previous != null) {
                        headers.parseHeader(previous);
                    }
                    headerNumber++;
                    current = null;
                    previous = current;
                }
                int i3 = this.maxHeaderCount;
                if (i3 <= 0 || headerNumber < i3) {
                    i = 64;
                    i2 = -1;
                } else {
                    throw new IOException("Maximum header count exceeded");
                }
            }
            if (previous != null) {
                headers.parseHeader(previous);
            }
            if (statusCode >= 200) {
                this.metrics.incrementResponseCount();
            }
            return statusline;
        }
        throw new NoHttpResponseException("The target server failed to respond");
    }

    public HttpEntity receiveResponseEntity(Headers headers) {
        assertOpen();
        BasicHttpEntity entity = new BasicHttpEntity();
        long len = determineLength(headers);
        if (len == -2) {
            entity.setChunked(true);
            entity.setContentLength(-1);
            entity.setContent(new ChunkedInputStream(this.inbuffer));
        } else if (len == -1) {
            entity.setChunked(false);
            entity.setContentLength(-1);
            entity.setContent(new IdentityInputStream(this.inbuffer));
        } else {
            entity.setChunked(false);
            entity.setContentLength(len);
            entity.setContent(new ContentLengthInputStream(this.inbuffer, len));
        }
        String contentTypeHeader = headers.getContentType();
        if (contentTypeHeader != null) {
            entity.setContentType(contentTypeHeader);
        }
        String contentEncodingHeader = headers.getContentEncoding();
        if (contentEncodingHeader != null) {
            entity.setContentEncoding(contentEncodingHeader);
        }
        return entity;
    }

    private long determineLength(Headers headers) {
        long transferEncoding = headers.getTransferEncoding();
        if (transferEncoding < 0) {
            return transferEncoding;
        }
        long contentlen = headers.getContentLength();
        if (contentlen > -1) {
            return contentlen;
        }
        return -1;
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
