package com.huawei.secure.android.common;

import android.content.Context;
import android.os.Build;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;

@Deprecated
public class SecureApacheSSLSocketFactory extends SSLSocketFactory {
    public static final X509HostnameVerifier BROWSER_COMPATIBLE_HOSTNAME_VERIFIER = new BrowserCompatHostnameVerifier();
    private static final String CLIENT_AGREEMENT = "TLS";
    public static final X509HostnameVerifier STRICT_HOSTNAME_VERIFIER = new StrictHostnameVerifier();
    private static final String[] UNSAFEALGORITHMS = {"TEA", "SHA0", "MD2", "MD4", "RIPEMD", "NULL", "RC4", "DES", "DESX", "DES40", "RC2", "MD5", "ANON", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", "TLS_RSA"};
    private static volatile SecureApacheSSLSocketFactory sasf = null;
    private Context mContext;
    private SSLContext sslContext;

    private SecureApacheSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);
    }

    private SecureApacheSSLSocketFactory(KeyStore trustStore, Context context) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException, IllegalAccessException {
        super(trustStore);
        this.mContext = context;
        this.sslContext = SSLContext.getInstance(CLIENT_AGREEMENT);
        this.sslContext.init(null, new X509TrustManager[]{new SecureX509TrustManager(this.mContext)}, new SecureRandom());
    }

    @Deprecated
    public SecureApacheSSLSocketFactory(KeyStore trustStore, InputStream trustStream, String trustPwd) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        super(trustStore);
        this.sslContext = SSLContext.getInstance(CLIENT_AGREEMENT);
        this.sslContext.init(null, new X509TrustManager[]{new HiCloudX509TrustManager(trustStream, trustPwd)}, new SecureRandom());
    }

    @Deprecated
    public static SecureApacheSSLSocketFactory getInstance(KeyStore truststore, Context context) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException, IllegalAccessException {
        if (sasf == null) {
            synchronized (SecureApacheSSLSocketFactory.class) {
                if (sasf == null) {
                    sasf = new SecureApacheSSLSocketFactory(truststore, context);
                }
            }
        }
        return sasf;
    }

    private static void setEnableSafeCipherSuites(SSLSocket sslsock) {
        String[] enableCiphers;
        if (!(sslsock == null || (enableCiphers = sslsock.getEnabledCipherSuites()) == null || enableCiphers.length == 0)) {
            List<String> enableCiphersList = new ArrayList<>();
            for (String cipherString : enableCiphers) {
                boolean isUnSafeAlgorithm = false;
                String upperCaseStr = cipherString.toUpperCase(Locale.ENGLISH);
                String[] strArr = UNSAFEALGORITHMS;
                int length = strArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (upperCaseStr.contains(strArr[i].toUpperCase(Locale.ENGLISH))) {
                        isUnSafeAlgorithm = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (!isUnSafeAlgorithm) {
                    enableCiphersList.add(cipherString);
                }
            }
            sslsock.setEnabledCipherSuites((String[]) enableCiphersList.toArray(new String[enableCiphersList.size()]));
        }
    }

    @Override // org.apache.http.conn.ssl.SSLSocketFactory, org.apache.http.conn.scheme.LayeredSocketFactory
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        Socket s = this.sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        setSocketOptions(s);
        return s;
    }

    @Override // org.apache.http.conn.ssl.SSLSocketFactory, org.apache.http.conn.scheme.SocketFactory
    public Socket createSocket() throws IOException {
        Socket s = this.sslContext.getSocketFactory().createSocket();
        setSocketOptions(s);
        return s;
    }

    private void setSocketOptions(Socket s) {
        if (s != null && (s instanceof SSLSocket)) {
            setEnabledProtocols((SSLSocket) s);
            setEnableSafeCipherSuites((SSLSocket) s);
        }
    }

    private void setEnabledProtocols(SSLSocket s) {
        if (s != null && Build.VERSION.SDK_INT >= 16) {
            s.setEnabledProtocols(new String[]{"TLSv1.1", "TLSv1.2"});
        }
    }
}
