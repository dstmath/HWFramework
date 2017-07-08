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
    private final EntitySerializer entityserializer;
    private SessionInputBuffer inbuffer;
    private int maxHeaderCount;
    private int maxLineLength;
    private HttpConnectionMetricsImpl metrics;
    private volatile boolean open;
    private SessionOutputBuffer outbuffer;
    private HttpMessageWriter requestWriter;
    private Socket socket;

    public AndroidHttpClientConnection() {
        this.inbuffer = null;
        this.outbuffer = null;
        this.requestWriter = null;
        this.metrics = null;
        this.socket = null;
        this.entityserializer = new EntitySerializer(new StrictContentLengthStrategy());
    }

    public void bind(Socket socket, HttpParams params) throws IOException {
        boolean z = false;
        if (socket == null) {
            throw new IllegalArgumentException("Socket may not be null");
        } else if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        } else {
            assertNotOpen();
            socket.setTcpNoDelay(HttpConnectionParams.getTcpNoDelay(params));
            socket.setSoTimeout(HttpConnectionParams.getSoTimeout(params));
            int linger = HttpConnectionParams.getLinger(params);
            if (linger >= 0) {
                if (linger > 0) {
                    z = true;
                }
                socket.setSoLinger(z, linger);
            }
            this.socket = socket;
            int buffersize = HttpConnectionParams.getSocketBufferSize(params);
            this.inbuffer = new SocketInputBuffer(socket, buffersize, params);
            this.outbuffer = new SocketOutputBuffer(socket, buffersize, params);
            this.maxHeaderCount = params.getIntParameter("http.connection.max-header-count", -1);
            this.maxLineLength = params.getIntParameter("http.connection.max-line-length", -1);
            this.requestWriter = new HttpRequestWriter(this.outbuffer, null, params);
            this.metrics = new HttpConnectionMetricsImpl(this.inbuffer.getMetrics(), this.outbuffer.getMetrics());
            this.open = true;
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getSimpleName()).append("[");
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

    public boolean isOpen() {
        return (!this.open || this.socket == null) ? false : this.socket.isConnected();
    }

    public InetAddress getLocalAddress() {
        if (this.socket != null) {
            return this.socket.getLocalAddress();
        }
        return null;
    }

    public int getLocalPort() {
        if (this.socket != null) {
            return this.socket.getLocalPort();
        }
        return -1;
    }

    public InetAddress getRemoteAddress() {
        if (this.socket != null) {
            return this.socket.getInetAddress();
        }
        return null;
    }

    public int getRemotePort() {
        if (this.socket != null) {
            return this.socket.getPort();
        }
        return -1;
    }

    public void setSocketTimeout(int timeout) {
        assertOpen();
        if (this.socket != null) {
            try {
                this.socket.setSoTimeout(timeout);
            } catch (SocketException e) {
            }
        }
    }

    public int getSocketTimeout() {
        if (this.socket == null) {
            return -1;
        }
        try {
            return this.socket.getSoTimeout();
        } catch (SocketException e) {
            return -1;
        }
    }

    public void shutdown() throws IOException {
        this.open = false;
        Socket tmpsocket = this.socket;
        if (tmpsocket != null) {
            tmpsocket.close();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() throws IOException {
        if (this.open) {
            this.open = false;
            doFlush();
            try {
                this.socket.shutdownOutput();
                this.socket.shutdownInput();
            } catch (IOException e) {
            } catch (UnsupportedOperationException e2) {
            }
            this.socket.close();
        }
    }

    public void sendRequestHeader(HttpRequest request) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        assertOpen();
        this.requestWriter.write(request);
        this.metrics.incrementRequestCount();
    }

    public void sendRequestEntity(HttpEntityEnclosingRequest request) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        assertOpen();
        if (request.getEntity() != null) {
            this.entityserializer.serialize(this.outbuffer, request, request.getEntity());
        }
    }

    protected void doFlush() throws IOException {
        this.outbuffer.flush();
    }

    public void flush() throws IOException {
        assertOpen();
        doFlush();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public StatusLine parseResponseHeader(Headers headers) throws IOException, ParseException {
        assertOpen();
        CharArrayBuffer current = new CharArrayBuffer(64);
        if (this.inbuffer.readLine(current) == -1) {
            throw new NoHttpResponseException("The target server failed to respond");
        }
        StatusLine statusline = BasicLineParser.DEFAULT.parseStatusLine(current, new ParserCursor(0, current.length()));
        int statusCode = statusline.getStatusCode();
        CharArrayBuffer previous = null;
        int headerNumber = 0;
        while (true) {
            if (current == null) {
                current = new CharArrayBuffer(64);
            } else {
                current.clear();
            }
            if (this.inbuffer.readLine(current) != -1 && current.length() >= 1) {
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
                    if (this.maxLineLength > 0 && ((previous.length() + 1) + current.length()) - start > this.maxLineLength) {
                        break;
                    }
                    previous.append(' ');
                    previous.append(current, start, current.length() - start);
                } else {
                    if (previous != null) {
                        headers.parseHeader(previous);
                    }
                    headerNumber++;
                    previous = current;
                    current = null;
                }
                if (this.maxHeaderCount > 0 && headerNumber >= this.maxHeaderCount) {
                    break;
                }
            } else if (previous != null) {
                headers.parseHeader(previous);
            }
        }
        throw new IOException("Maximum header count exceeded");
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

    public boolean isStale() {
        assertOpen();
        try {
            this.inbuffer.isDataAvailable(1);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    public HttpConnectionMetrics getMetrics() {
        return this.metrics;
    }
}
