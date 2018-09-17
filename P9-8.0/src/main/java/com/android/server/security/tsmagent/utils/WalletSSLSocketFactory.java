package com.android.server.security.tsmagent.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class WalletSSLSocketFactory extends SSLSocketFactory {
    private static final String CLIENT_AGREEMENT = "TLS";
    private static String[] safeEnableCiphers = null;
    private SSLContext sslContext = null;

    public WalletSSLSocketFactory(X509TrustManager trustManager) {
        try {
            this.sslContext = SSLContext.getInstance(CLIENT_AGREEMENT);
            this.sslContext.init(null, new X509TrustManager[]{trustManager}, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            HwLog.e("SSLContext init exception");
        } catch (KeyManagementException e2) {
            HwLog.e("SSLContext init exception");
        }
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        Socket mySocket = this.sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        if (mySocket == null || !(mySocket instanceof SSLSocket)) {
            return mySocket;
        }
        SSLSocket mySSLSocket = (SSLSocket) mySocket;
        setEnableSafeCipherSuites(mySSLSocket);
        return mySSLSocket;
    }

    public static void setEnableSafeCipherSuites(SSLSocket sslsock) {
        String[] enabledCiphers = sslsock.getEnabledCipherSuites();
        List<String> enabledCiphersList = new ArrayList();
        String upperCaseStr = "";
        for (String cipherString : enabledCiphers) {
            upperCaseStr = cipherString.toUpperCase(Locale.ENGLISH);
            if (!(upperCaseStr.contains("RC4") || upperCaseStr.contains("DES") || upperCaseStr.contains("MD5") || upperCaseStr.contains("ANON") || upperCaseStr.contains("NULL") || upperCaseStr.contains("TLS_EMPTY_RENEGOTIATION_INFO_SCSV"))) {
                enabledCiphersList.add(cipherString);
            }
        }
        safeEnableCiphers = (String[]) enabledCiphersList.toArray(new String[enabledCiphersList.size()]);
        sslsock.setEnabledCipherSuites(safeEnableCiphers);
    }

    public String[] getDefaultCipherSuites() {
        if (safeEnableCiphers != null) {
            return (String[]) safeEnableCiphers.clone();
        }
        return new String[0];
    }

    public String[] getSupportedCipherSuites() {
        return new String[0];
    }

    public Socket createSocket(String arg0, int arg1) throws IOException {
        Socket mySocket = this.sslContext.getSocketFactory().createSocket(arg0, arg1);
        if (mySocket == null || !(mySocket instanceof SSLSocket)) {
            return mySocket;
        }
        SSLSocket mySSLSocket = (SSLSocket) mySocket;
        setEnableSafeCipherSuites(mySSLSocket);
        return mySSLSocket;
    }

    public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
        Socket mySocket = this.sslContext.getSocketFactory().createSocket(arg0, arg1);
        if (mySocket == null || !(mySocket instanceof SSLSocket)) {
            return mySocket;
        }
        SSLSocket mySSLSocket = (SSLSocket) mySocket;
        setEnableSafeCipherSuites(mySSLSocket);
        return mySSLSocket;
    }

    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
        Socket mySocket = this.sslContext.getSocketFactory().createSocket(arg0, arg1, arg2, arg3);
        if (mySocket == null || !(mySocket instanceof SSLSocket)) {
            return mySocket;
        }
        SSLSocket mySSLSocket = (SSLSocket) mySocket;
        setEnableSafeCipherSuites(mySSLSocket);
        return mySSLSocket;
    }

    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
        Socket mySocket = this.sslContext.getSocketFactory().createSocket(arg0, arg1, arg2, arg3);
        if (mySocket == null || !(mySocket instanceof SSLSocket)) {
            return mySocket;
        }
        SSLSocket mySSLSocket = (SSLSocket) mySocket;
        setEnableSafeCipherSuites(mySSLSocket);
        return mySSLSocket;
    }
}
