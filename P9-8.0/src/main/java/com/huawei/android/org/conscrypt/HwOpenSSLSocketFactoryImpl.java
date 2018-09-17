package com.huawei.android.org.conscrypt;

import com.android.org.conscrypt.OpenSSLSocketFactoryImpl;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;

public class HwOpenSSLSocketFactoryImpl {
    private SSLSocketFactory mSSLSocketFactory;

    private class OpenSSLSocketFactoryImplInner extends OpenSSLSocketFactoryImpl {
        private SSLSocketFactory mSslSocketFactory;

        public OpenSSLSocketFactoryImplInner(SSLSocketFactory factory) {
            this.mSslSocketFactory = factory;
        }

        public Socket createSocket() throws IOException {
            Socket mySocket = this.mSslSocketFactory.createSocket();
            HwOpenSSLSocketFactoryImpl.this.onCreateSocket(mySocket);
            return mySocket;
        }

        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            Socket mySocket = this.mSslSocketFactory.createSocket(address, port, localAddress, localPort);
            HwOpenSSLSocketFactoryImpl.this.onCreateSocket(mySocket);
            return mySocket;
        }

        public Socket createSocket(InetAddress host, int port) throws IOException {
            Socket mySocket = this.mSslSocketFactory.createSocket(host, port);
            HwOpenSSLSocketFactoryImpl.this.onCreateSocket(mySocket);
            return mySocket;
        }

        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            Socket mySocket = this.mSslSocketFactory.createSocket(s, host, port, autoClose);
            HwOpenSSLSocketFactoryImpl.this.onCreateSocket(mySocket);
            return mySocket;
        }

        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
            Socket mySocket = this.mSslSocketFactory.createSocket(host, port, localHost, localPort);
            HwOpenSSLSocketFactoryImpl.this.onCreateSocket(mySocket);
            return mySocket;
        }

        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            Socket mySocket = this.mSslSocketFactory.createSocket(host, port);
            HwOpenSSLSocketFactoryImpl.this.onCreateSocket(mySocket);
            return mySocket;
        }
    }

    public SSLSocketFactory getOpenSSLSocketFactory() {
        return this.mSSLSocketFactory;
    }

    protected void onCreateSocket(Socket socket) {
    }

    public HwOpenSSLSocketFactoryImpl(SSLSocketFactory factory) {
        this.mSSLSocketFactory = new OpenSSLSocketFactoryImplInner(factory);
    }
}
