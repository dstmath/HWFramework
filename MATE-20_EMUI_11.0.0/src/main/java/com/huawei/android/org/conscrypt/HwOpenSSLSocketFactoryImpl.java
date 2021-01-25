package com.huawei.android.org.conscrypt;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;

public class HwOpenSSLSocketFactoryImpl {
    private SSLSocketFactory mSSLSocketFactory;

    public SSLSocketFactory getOpenSSLSocketFactory() {
        return this.mSSLSocketFactory;
    }

    /* access modifiers changed from: protected */
    public void onCreateSocket(Socket socket) {
    }

    public HwOpenSSLSocketFactoryImpl(SSLSocketFactory factory) {
        this.mSSLSocketFactory = factory;
    }

    public Socket createSocket() throws IOException {
        Socket mySocket = this.mSSLSocketFactory.createSocket();
        onCreateSocket(mySocket);
        return mySocket;
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket mySocket = this.mSSLSocketFactory.createSocket(address, port, localAddress, localPort);
        onCreateSocket(mySocket);
        return mySocket;
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket mySocket = this.mSSLSocketFactory.createSocket(host, port);
        onCreateSocket(mySocket);
        return mySocket;
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        Socket mySocket = this.mSSLSocketFactory.createSocket(s, host, port, autoClose);
        onCreateSocket(mySocket);
        return mySocket;
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        Socket mySocket = this.mSSLSocketFactory.createSocket(host, port, localHost, localPort);
        onCreateSocket(mySocket);
        return mySocket;
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket mySocket = this.mSSLSocketFactory.createSocket(host, port);
        onCreateSocket(mySocket);
        return mySocket;
    }
}
