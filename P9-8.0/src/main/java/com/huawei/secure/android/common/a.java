package com.huawei.secure.android.common;

import android.os.Build.VERSION;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;

public class a extends SSLSocketFactory {
    public static final X509HostnameVerifier jd = new BrowserCompatHostnameVerifier();
    public static final X509HostnameVerifier je = new StrictHostnameVerifier();
    private static final String[] jf = new String[]{"TEA", "SHA0", "MD2", "MD4", "RIPEMD", "RC4", "DES", "GCM", "DESX", "DES40", "RC2", "MD5", "ANON", "NULL", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV"};
    private static String[] jh = null;
    private SSLContext jg;

    public a(InputStream inputStream, String str) {
        this.jg = null;
        this.jg = SSLContext.getInstance("TLS");
        this.jg.init(null, new X509TrustManager[]{new b(inputStream, str)}, new SecureRandom());
    }

    private static void zv(SSLSocket sSLSocket) {
        if (sSLSocket != null) {
            String[] enabledCipherSuites = sSLSocket.getEnabledCipherSuites();
            List arrayList = new ArrayList();
            String str = "";
            for (String str2 : enabledCipherSuites) {
                Object obj;
                String toUpperCase = str2.toUpperCase(Locale.US);
                for (CharSequence contains : jf) {
                    if (toUpperCase.contains(contains)) {
                        obj = 1;
                        break;
                    }
                }
                obj = null;
                if (obj == null) {
                    arrayList.add(str2);
                }
            }
            jh = (String[]) arrayList.toArray(new String[arrayList.size()]);
            sSLSocket.setEnabledCipherSuites(jh);
        }
    }

    private void zw(Socket socket) {
        if (socket != null && (socket instanceof SSLSocket)) {
            zu((SSLSocket) socket);
            zv((SSLSocket) socket);
        }
    }

    private void zu(SSLSocket sSLSocket) {
        if (sSLSocket != null && VERSION.SDK_INT >= 16) {
            sSLSocket.setEnabledProtocols(new String[]{"TLSv1.1", "TLSv1.2"});
        }
    }

    public String[] getDefaultCipherSuites() {
        if (jh == null) {
            return new String[0];
        }
        return (String[]) jh.clone();
    }

    public String[] getSupportedCipherSuites() {
        return new String[0];
    }

    public Socket createSocket(String str, int i) {
        Socket createSocket = this.jg.getSocketFactory().createSocket(str, i);
        zw(createSocket);
        return createSocket;
    }

    public Socket createSocket(InetAddress inetAddress, int i) {
        return createSocket(inetAddress.getHostAddress(), i);
    }

    public Socket createSocket(String str, int i, InetAddress inetAddress, int i2) {
        return createSocket(str, i);
    }

    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) {
        return createSocket(inetAddress.getHostAddress(), i);
    }

    public Socket createSocket(Socket socket, String str, int i, boolean z) {
        Socket createSocket = this.jg.getSocketFactory().createSocket(socket, str, i, z);
        zw(createSocket);
        return createSocket;
    }
}
