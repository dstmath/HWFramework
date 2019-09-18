package com.huawei.okhttp3.internal.platform;

import android.os.Build;
import android.util.Log;
import com.huawei.okhttp3.Protocol;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.tls.CertificateChainCleaner;
import com.huawei.okhttp3.internal.tls.TrustRootIndex;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

class AndroidPlatform extends Platform {
    private static final int MAX_LOG_LENGTH = 4000;
    private final CloseGuard closeGuard = CloseGuard.get();
    private final OptionalMethod<Socket> getAlpnSelectedProtocol;
    private final OptionalMethod<Socket> setAlpnProtocols;
    private final OptionalMethod<Socket> setHostname;
    private final OptionalMethod<Socket> setUseSessionTickets;
    private final Class<?> sslParametersClass;

    static final class AndroidCertificateChainCleaner extends CertificateChainCleaner {
        private final Method checkServerTrusted;
        private final Object x509TrustManagerExtensions;

        AndroidCertificateChainCleaner(Object x509TrustManagerExtensions2, Method checkServerTrusted2) {
            this.x509TrustManagerExtensions = x509TrustManagerExtensions2;
            this.checkServerTrusted = checkServerTrusted2;
        }

        public List<Certificate> clean(List<Certificate> chain, String hostname) throws SSLPeerUnverifiedException {
            try {
                return (List) this.checkServerTrusted.invoke(this.x509TrustManagerExtensions, new Object[]{(X509Certificate[]) chain.toArray(new X509Certificate[chain.size()]), "RSA", hostname});
            } catch (InvocationTargetException e) {
                SSLPeerUnverifiedException exception = new SSLPeerUnverifiedException(e.getMessage());
                exception.initCause(e);
                throw exception;
            } catch (IllegalAccessException e2) {
                throw new AssertionError(e2);
            }
        }

        public boolean equals(Object other) {
            return other instanceof AndroidCertificateChainCleaner;
        }

        public int hashCode() {
            return 0;
        }
    }

    static final class AndroidTrustRootIndex implements TrustRootIndex {
        private final Method findByIssuerAndSignatureMethod;
        private final X509TrustManager trustManager;

        AndroidTrustRootIndex(X509TrustManager trustManager2, Method findByIssuerAndSignatureMethod2) {
            this.findByIssuerAndSignatureMethod = findByIssuerAndSignatureMethod2;
            this.trustManager = trustManager2;
        }

        public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
            X509Certificate x509Certificate = null;
            try {
                TrustAnchor trustAnchor = (TrustAnchor) this.findByIssuerAndSignatureMethod.invoke(this.trustManager, new Object[]{cert});
                if (trustAnchor != null) {
                    x509Certificate = trustAnchor.getTrustedCert();
                }
                return x509Certificate;
            } catch (IllegalAccessException e) {
                throw Util.assertionError("unable to get issues and signature", e);
            } catch (InvocationTargetException e2) {
                return null;
            }
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AndroidTrustRootIndex)) {
                return false;
            }
            AndroidTrustRootIndex that = (AndroidTrustRootIndex) obj;
            if (!this.trustManager.equals(that.trustManager) || !this.findByIssuerAndSignatureMethod.equals(that.findByIssuerAndSignatureMethod)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return this.trustManager.hashCode() + (31 * this.findByIssuerAndSignatureMethod.hashCode());
        }
    }

    static final class CloseGuard {
        private final Method getMethod;
        private final Method openMethod;
        private final Method warnIfOpenMethod;

        CloseGuard(Method getMethod2, Method openMethod2, Method warnIfOpenMethod2) {
            this.getMethod = getMethod2;
            this.openMethod = openMethod2;
            this.warnIfOpenMethod = warnIfOpenMethod2;
        }

        /* access modifiers changed from: package-private */
        public Object createAndOpen(String closer) {
            if (this.getMethod != null) {
                try {
                    Object closeGuardInstance = this.getMethod.invoke(null, new Object[0]);
                    this.openMethod.invoke(closeGuardInstance, new Object[]{closer});
                    return closeGuardInstance;
                } catch (Exception e) {
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public boolean warnIfOpen(Object closeGuardInstance) {
            if (closeGuardInstance == null) {
                return false;
            }
            try {
                this.warnIfOpenMethod.invoke(closeGuardInstance, new Object[0]);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        static CloseGuard get() {
            Method openMethod2;
            Method getMethod2;
            Method warnIfOpenMethod2;
            try {
                Class<?> closeGuardClass = Class.forName("dalvik.system.CloseGuard");
                getMethod2 = closeGuardClass.getMethod("get", new Class[0]);
                openMethod2 = closeGuardClass.getMethod("open", new Class[]{String.class});
                warnIfOpenMethod2 = closeGuardClass.getMethod("warnIfOpen", new Class[0]);
            } catch (Exception e) {
                getMethod2 = null;
                openMethod2 = null;
                warnIfOpenMethod2 = null;
            }
            return new CloseGuard(getMethod2, openMethod2, warnIfOpenMethod2);
        }
    }

    AndroidPlatform(Class<?> sslParametersClass2, OptionalMethod<Socket> setUseSessionTickets2, OptionalMethod<Socket> setHostname2, OptionalMethod<Socket> getAlpnSelectedProtocol2, OptionalMethod<Socket> setAlpnProtocols2) {
        this.sslParametersClass = sslParametersClass2;
        this.setUseSessionTickets = setUseSessionTickets2;
        this.setHostname = setHostname2;
        this.getAlpnSelectedProtocol = getAlpnSelectedProtocol2;
        this.setAlpnProtocols = setAlpnProtocols2;
    }

    public void connectSocket(Socket socket, InetSocketAddress address, int connectTimeout) throws IOException {
        try {
            socket.connect(address, connectTimeout);
        } catch (AssertionError e) {
            if (Util.isAndroidGetsocknameError(e)) {
                throw new IOException(e);
            }
            throw e;
        } catch (ClassCastException | SecurityException e2) {
            IOException ioException = new IOException("Exception in connect");
            ioException.initCause(e2);
            throw ioException;
        }
    }

    /* access modifiers changed from: protected */
    @Nullable
    public X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
        Object context = readFieldOrNull(sslSocketFactory, this.sslParametersClass, "sslParameters");
        if (context == null) {
            try {
                context = readFieldOrNull(sslSocketFactory, Class.forName("com.google.android.gms.org.conscrypt.SSLParametersImpl", false, sslSocketFactory.getClass().getClassLoader()), "sslParameters");
            } catch (ClassNotFoundException e) {
                return super.trustManager(sslSocketFactory);
            }
        }
        X509TrustManager x509TrustManager = (X509TrustManager) readFieldOrNull(context, X509TrustManager.class, "x509TrustManager");
        if (x509TrustManager != null) {
            return x509TrustManager;
        }
        return (X509TrustManager) readFieldOrNull(context, X509TrustManager.class, "trustManager");
    }

    public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
        if (hostname != null) {
            this.setUseSessionTickets.invokeOptionalWithoutCheckedException(sslSocket, true);
            this.setHostname.invokeOptionalWithoutCheckedException(sslSocket, hostname);
        }
        if (this.setAlpnProtocols != null && this.setAlpnProtocols.isSupported(sslSocket)) {
            this.setAlpnProtocols.invokeWithoutCheckedException(sslSocket, concatLengthPrefixed(protocols));
        }
    }

    @Nullable
    public String getSelectedProtocol(SSLSocket socket) {
        String str = null;
        if (this.getAlpnSelectedProtocol == null || !this.getAlpnSelectedProtocol.isSupported(socket)) {
            return null;
        }
        byte[] alpnResult = (byte[]) this.getAlpnSelectedProtocol.invokeWithoutCheckedException(socket, new Object[0]);
        if (alpnResult != null) {
            str = new String(alpnResult, Util.UTF_8);
        }
        return str;
    }

    public void log(int level, String message, Throwable t) {
        int logLevel = 5;
        if (level != 5) {
            logLevel = 3;
        }
        if (t != null) {
            message = message + 10 + Log.getStackTraceString(t);
        }
        int i = 0;
        int length = message.length();
        while (i < length) {
            int newline = message.indexOf(10, i);
            int newline2 = newline != -1 ? newline : length;
            do {
                int end = Math.min(newline2, i + MAX_LOG_LENGTH);
                Log.println(logLevel, "OkHttp", message.substring(i, end));
                i = end;
            } while (i < newline2);
            i++;
        }
    }

    public Object getStackTraceForCloseable(String closer) {
        return this.closeGuard.createAndOpen(closer);
    }

    public void logCloseableLeak(String message, Object stackTrace) {
        if (!this.closeGuard.warnIfOpen(stackTrace)) {
            log(5, message, null);
        }
    }

    public boolean isCleartextTrafficPermitted(String hostname) {
        try {
            Class<?> networkPolicyClass = Class.forName("android.security.NetworkSecurityPolicy");
            return api24IsCleartextTrafficPermitted(hostname, networkPolicyClass, networkPolicyClass.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return super.isCleartextTrafficPermitted(hostname);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e2) {
            throw Util.assertionError("unable to determine cleartext support", e2);
        }
    }

    private boolean api24IsCleartextTrafficPermitted(String hostname, Class<?> networkPolicyClass, Object networkSecurityPolicy) throws InvocationTargetException, IllegalAccessException {
        try {
            return ((Boolean) networkPolicyClass.getMethod("isCleartextTrafficPermitted", new Class[]{String.class}).invoke(networkSecurityPolicy, new Object[]{hostname})).booleanValue();
        } catch (NoSuchMethodException e) {
            return api23IsCleartextTrafficPermitted(hostname, networkPolicyClass, networkSecurityPolicy);
        }
    }

    private boolean api23IsCleartextTrafficPermitted(String hostname, Class<?> networkPolicyClass, Object networkSecurityPolicy) throws InvocationTargetException, IllegalAccessException {
        try {
            return ((Boolean) networkPolicyClass.getMethod("isCleartextTrafficPermitted", new Class[0]).invoke(networkSecurityPolicy, new Object[0])).booleanValue();
        } catch (NoSuchMethodException e) {
            return super.isCleartextTrafficPermitted(hostname);
        }
    }

    private static boolean supportsAlpn() {
        if (Security.getProvider("GMSCore_OpenSSL") != null) {
            return true;
        }
        try {
            Class.forName("android.net.Network");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public CertificateChainCleaner buildCertificateChainCleaner(X509TrustManager trustManager) {
        try {
            Class<?> extensionsClass = Class.forName("android.net.http.X509TrustManagerExtensions");
            return new AndroidCertificateChainCleaner(extensionsClass.getConstructor(new Class[]{X509TrustManager.class}).newInstance(new Object[]{trustManager}), extensionsClass.getMethod("checkServerTrusted", new Class[]{X509Certificate[].class, String.class, String.class}));
        } catch (Exception e) {
            return super.buildCertificateChainCleaner(trustManager);
        }
    }

    public static Platform buildIfSupported() {
        Class<?> sslParametersClass2;
        OptionalMethod<Socket> setAlpnProtocols2;
        try {
            sslParametersClass2 = Class.forName("com.android.org.conscrypt.SSLParametersImpl");
        } catch (ClassNotFoundException e) {
            try {
                sslParametersClass2 = Class.forName("org.apache.harmony.xnet.provider.jsse.SSLParametersImpl");
            } catch (ClassNotFoundException e2) {
                return null;
            }
        }
        OptionalMethod<Socket> setUseSessionTickets2 = new OptionalMethod<>(null, "setUseSessionTickets", Boolean.TYPE);
        OptionalMethod<Socket> setHostname2 = new OptionalMethod<>(null, "setHostname", String.class);
        OptionalMethod<Socket> getAlpnSelectedProtocol2 = null;
        if (supportsAlpn()) {
            getAlpnSelectedProtocol2 = new OptionalMethod<>(byte[].class, "getAlpnSelectedProtocol", new Class[0]);
            setAlpnProtocols2 = new OptionalMethod<>(null, "setAlpnProtocols", byte[].class);
        } else {
            setAlpnProtocols2 = null;
        }
        AndroidPlatform androidPlatform = new AndroidPlatform(sslParametersClass2, setUseSessionTickets2, setHostname2, getAlpnSelectedProtocol2, setAlpnProtocols2);
        return androidPlatform;
    }

    public TrustRootIndex buildTrustRootIndex(X509TrustManager trustManager) {
        try {
            Method method = trustManager.getClass().getDeclaredMethod("findTrustAnchorByIssuerAndSignature", new Class[]{X509Certificate.class});
            method.setAccessible(true);
            return new AndroidTrustRootIndex(trustManager, method);
        } catch (NoSuchMethodException e) {
            return super.buildTrustRootIndex(trustManager);
        }
    }

    public SSLContext getSSLContext() {
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
            try {
                return SSLContext.getInstance("TLSv1.2");
            } catch (NoSuchAlgorithmException e) {
            }
        }
        try {
            return SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e2) {
            throw new IllegalStateException("No TLS provider", e2);
        }
    }
}
