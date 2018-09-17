package com.huawei.okhttp3.internal.platform;

import com.huawei.okhttp3.Protocol;
import java.lang.reflect.Method;
import java.util.List;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

final class Jdk9Platform extends Platform {
    final Method getProtocolMethod;
    final Method setProtocolMethod;

    public Jdk9Platform(Method setProtocolMethod, Method getProtocolMethod) {
        this.setProtocolMethod = setProtocolMethod;
        this.getProtocolMethod = getProtocolMethod;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0021 A:{ExcHandler: java.lang.IllegalAccessException (e java.lang.IllegalAccessException), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:5:0x0027, code:
            throw new java.lang.AssertionError();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
        try {
            SSLParameters sslParameters = sslSocket.getSSLParameters();
            List<String> names = Platform.alpnProtocolNames(protocols);
            this.setProtocolMethod.invoke(sslParameters, new Object[]{names.toArray(new String[names.size()])});
            sslSocket.setSSLParameters(sslParameters);
        } catch (IllegalAccessException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0019 A:{ExcHandler: java.lang.IllegalAccessException (e java.lang.IllegalAccessException), Splitter: B:1:0x0001} */
    /* JADX WARNING: Missing block: B:10:0x001f, code:
            throw new java.lang.AssertionError();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getSelectedProtocol(SSLSocket socket) {
        try {
            String protocol = (String) this.getProtocolMethod.invoke(socket, new Object[0]);
            if (protocol == null || protocol.equals("")) {
                return null;
            }
            return protocol;
        } catch (IllegalAccessException e) {
        }
    }

    public X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
        throw new UnsupportedOperationException("clientBuilder.sslSocketFactory(SSLSocketFactory) not supported on JDK 9+");
    }

    public static Jdk9Platform buildIfSupported() {
        try {
            return new Jdk9Platform(SSLParameters.class.getMethod("setApplicationProtocols", new Class[]{String[].class}), SSLSocket.class.getMethod("getApplicationProtocol", new Class[0]));
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
