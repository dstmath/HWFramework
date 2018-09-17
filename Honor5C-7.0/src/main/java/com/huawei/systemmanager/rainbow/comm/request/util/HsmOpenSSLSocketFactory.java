package com.huawei.systemmanager.rainbow.comm.request.util;

import android.util.Log;
import com.android.org.conscrypt.OpenSSLSocketFactoryImpl;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class HsmOpenSSLSocketFactory extends OpenSSLSocketFactoryImpl {
    private static final String ANON_ALGORITHMS = "ANON";
    private static final String DES_ALGORITHMS = "DES";
    private static final String LOG_TAG = "HsmOpenSSLSocketFactory";
    private static final String MD5_ALGORITHMS = "MD5";
    private static final String NULL_ALGORITHMS = "NULL";
    private static final String RC4_ALGORITHMS = "RC4";
    private OpenSSLSocketFactoryImpl mSslSocketFactory;

    public HsmOpenSSLSocketFactory(SSLSocketFactory ssl) {
        this.mSslSocketFactory = (OpenSSLSocketFactoryImpl) ssl;
    }

    public Socket createSocket() throws IOException {
        Socket mySocket = this.mSslSocketFactory.createSocket();
        setEnableSafeCipherSuites(mySocket);
        return mySocket;
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket mySocket = this.mSslSocketFactory.createSocket(address, port, localAddress, localPort);
        setEnableSafeCipherSuites(mySocket);
        return mySocket;
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket mySocket = this.mSslSocketFactory.createSocket(host, port);
        setEnableSafeCipherSuites(mySocket);
        return mySocket;
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        Socket mySocket = this.mSslSocketFactory.createSocket(s, host, port, autoClose);
        setEnableSafeCipherSuites(mySocket);
        return mySocket;
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        Socket mySocket = this.mSslSocketFactory.createSocket(host, port, localHost, localPort);
        setEnableSafeCipherSuites(mySocket);
        return mySocket;
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket mySocket = this.mSslSocketFactory.createSocket(host, port);
        setEnableSafeCipherSuites(mySocket);
        return mySocket;
    }

    private void setEnableSafeCipherSuites(Socket socket) {
        Log.d(LOG_TAG, "enter setEnableSafeCipherSuites");
        if (socket instanceof SSLSocket) {
            SSLSocket sslsock = (SSLSocket) socket;
            String[] cipherSuites = sslsock.getEnabledCipherSuites();
            if (cipherSuites == null || cipherSuites.length == 0) {
                Log.w(LOG_TAG, "Current enabled cipherSuites is invalid!");
                return;
            }
            List<String> cipherSuiteList = new ArrayList();
            for (String cipherSuite : cipherSuites) {
                String cipherSuiteTemp = cipherSuite.toUpperCase(Locale.US);
                if (!(cipherSuiteTemp.contains(RC4_ALGORITHMS) || cipherSuiteTemp.contains(DES_ALGORITHMS) || cipherSuiteTemp.contains(MD5_ALGORITHMS) || cipherSuiteTemp.contains(NULL_ALGORITHMS) || cipherSuiteTemp.contains(ANON_ALGORITHMS))) {
                    cipherSuiteList.add(cipherSuite);
                }
            }
            sslsock.setEnabledCipherSuites((String[]) cipherSuiteList.toArray(new String[cipherSuiteList.size()]));
            return;
        }
        Log.e(LOG_TAG, "socket is not instanceof SSLSocket");
    }
}
