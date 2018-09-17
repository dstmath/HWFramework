package com.android.org.conscrypt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

public class OpenSSLSocketImplWrapper extends OpenSSLSocketImpl {
    private Socket socket;

    protected OpenSSLSocketImplWrapper(Socket socket, String hostname, int port, boolean autoClose, SSLParametersImpl sslParameters) throws IOException {
        super(socket, hostname, port, autoClose, sslParameters);
        if (socket.isConnected()) {
            this.socket = socket;
            return;
        }
        throw new SocketException("Socket is not connected.");
    }

    public void connect(SocketAddress sockaddr, int timeout) throws IOException {
        throw new IOException("Underlying socket is already connected.");
    }

    public void connect(SocketAddress sockaddr) throws IOException {
        throw new IOException("Underlying socket is already connected.");
    }

    public void bind(SocketAddress sockaddr) throws IOException {
        throw new IOException("Underlying socket is already connected.");
    }

    public SocketAddress getRemoteSocketAddress() {
        return this.socket.getRemoteSocketAddress();
    }

    public SocketAddress getLocalSocketAddress() {
        return this.socket.getLocalSocketAddress();
    }

    public InetAddress getLocalAddress() {
        return this.socket.getLocalAddress();
    }

    public InetAddress getInetAddress() {
        return this.socket.getInetAddress();
    }

    public String toString() {
        return "SSL socket over " + this.socket.toString();
    }

    public void setSoLinger(boolean on, int linger) throws SocketException {
        this.socket.setSoLinger(on, linger);
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        this.socket.setTcpNoDelay(on);
    }

    public void setReuseAddress(boolean on) throws SocketException {
        this.socket.setReuseAddress(on);
    }

    public void setKeepAlive(boolean on) throws SocketException {
        this.socket.setKeepAlive(on);
    }

    public void setTrafficClass(int tos) throws SocketException {
        this.socket.setTrafficClass(tos);
    }

    public void setSendBufferSize(int size) throws SocketException {
        this.socket.setSendBufferSize(size);
    }

    public void setReceiveBufferSize(int size) throws SocketException {
        this.socket.setReceiveBufferSize(size);
    }

    public boolean getTcpNoDelay() throws SocketException {
        return this.socket.getTcpNoDelay();
    }

    public boolean getReuseAddress() throws SocketException {
        return this.socket.getReuseAddress();
    }

    public boolean getOOBInline() throws SocketException {
        return this.socket.getOOBInline();
    }

    public boolean getKeepAlive() throws SocketException {
        return this.socket.getKeepAlive();
    }

    public int getTrafficClass() throws SocketException {
        return this.socket.getTrafficClass();
    }

    public int getSoTimeout() throws SocketException {
        return this.socket.getSoTimeout();
    }

    public int getSoLinger() throws SocketException {
        return this.socket.getSoLinger();
    }

    public int getSendBufferSize() throws SocketException {
        return this.socket.getSendBufferSize();
    }

    public int getReceiveBufferSize() throws SocketException {
        return this.socket.getReceiveBufferSize();
    }

    public boolean isConnected() {
        return this.socket.isConnected();
    }

    public boolean isClosed() {
        return this.socket.isClosed();
    }

    public boolean isBound() {
        return this.socket.isBound();
    }

    public boolean isOutputShutdown() {
        return this.socket.isOutputShutdown();
    }

    public boolean isInputShutdown() {
        return this.socket.isInputShutdown();
    }

    public int getPort() {
        return this.socket.getPort();
    }

    public int getLocalPort() {
        return this.socket.getLocalPort();
    }
}
