package org.apache.http.impl.io;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.apache.http.params.HttpParams;

@Deprecated
public class SocketInputBuffer extends AbstractSessionInputBuffer {
    private final Socket socket;

    public SocketInputBuffer(Socket socket, int buffersize, HttpParams params) throws IOException {
        if (socket == null) {
            throw new IllegalArgumentException("Socket may not be null");
        }
        this.socket = socket;
        init(socket.getInputStream(), 8192, params);
    }

    public boolean isDataAvailable(int timeout) throws IOException {
        boolean result = hasBufferedData();
        if (!result) {
            int oldtimeout = this.socket.getSoTimeout();
            try {
                this.socket.setSoTimeout(timeout);
                fillBuffer();
                result = hasBufferedData();
            } catch (InterruptedIOException e) {
                if (!(e instanceof SocketTimeoutException)) {
                    throw e;
                }
            } finally {
                this.socket.setSoTimeout(oldtimeout);
            }
        }
        return result;
    }

    public boolean isStale() throws IOException {
        boolean z = true;
        if (hasBufferedData()) {
            return false;
        }
        int oldTimeout = this.socket.getSoTimeout();
        try {
            this.socket.setSoTimeout(1);
            if (fillBuffer() != -1) {
                z = false;
            }
            this.socket.setSoTimeout(oldTimeout);
            return z;
        } catch (SocketTimeoutException e) {
            this.socket.setSoTimeout(oldTimeout);
            return false;
        } catch (IOException e2) {
            this.socket.setSoTimeout(oldTimeout);
            return true;
        } catch (Throwable th) {
            this.socket.setSoTimeout(oldTimeout);
            throw th;
        }
    }
}
