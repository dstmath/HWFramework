package com.huawei.android.pushagent.utils.e;

import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;

public class a extends SSLSocketFactory {
    private SSLSocketFactory fr;

    public a(SSLSocketFactory sSLSocketFactory) {
        this.fr = sSLSocketFactory;
    }

    public Socket createSocket() {
        return this.fr.createSocket();
    }

    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) {
        return this.fr.createSocket(inetAddress, i, inetAddress2, i2);
    }

    public Socket createSocket(InetAddress inetAddress, int i) {
        return this.fr.createSocket(inetAddress, i);
    }

    public Socket createSocket(Socket socket, String str, int i, boolean z) {
        return this.fr.createSocket(socket, str, i, z);
    }

    public Socket createSocket(String str, int i, InetAddress inetAddress, int i2) {
        return this.fr.createSocket(str, i, inetAddress, i2);
    }

    public Socket createSocket(String str, int i) {
        return this.fr.createSocket(str, i);
    }

    public String[] getDefaultCipherSuites() {
        return this.fr.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return this.fr.getSupportedCipherSuites();
    }
}
