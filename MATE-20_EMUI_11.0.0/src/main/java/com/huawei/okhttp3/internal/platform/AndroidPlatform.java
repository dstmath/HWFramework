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
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
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

/* access modifiers changed from: package-private */
public class AndroidPlatform extends Platform {
    private static final int MAX_LOG_LENGTH = 4000;
    private final CloseGuard closeGuard = CloseGuard.get();
    private final Method getAlpnSelectedProtocol;
    private final Method setAlpnProtocols;
    private final Method setHostname;
    private final Method setUseSessionTickets;
    private final Class<?> sslParametersClass;
    private final Class<?> sslSocketClass;

    AndroidPlatform(Class<?> sslParametersClass2, Class<?> sslSocketClass2, Method setUseSessionTickets2, Method setHostname2, Method getAlpnSelectedProtocol2, Method setAlpnProtocols2) {
        this.sslParametersClass = sslParametersClass2;
        this.sslSocketClass = sslSocketClass2;
        this.setUseSessionTickets = setUseSessionTickets2;
        this.setHostname = setHostname2;
        this.getAlpnSelectedProtocol = getAlpnSelectedProtocol2;
        this.setAlpnProtocols = setAlpnProtocols2;
    }

    @Override // com.huawei.okhttp3.internal.platform.Platform
    public void connectSocket(Socket socket, InetSocketAddress address, int connectTimeout) throws IOException {
        try {
            socket.connect(address, connectTimeout);
        } catch (AssertionError e) {
            if (Util.isAndroidGetsocknameError(e)) {
                throw new IOException(e);
            }
            throw e;
        } catch (ClassCastException e2) {
            if (Build.VERSION.SDK_INT == 26) {
                throw new IOException("Exception in connect", e2);
            }
            throw e2;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.okhttp3.internal.platform.Platform
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

    @Override // com.huawei.okhttp3.internal.platform.Platform
    public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
        if (this.sslSocketClass.isInstance(sslSocket)) {
            if (hostname != null) {
                try {
                    this.setUseSessionTickets.invoke(sslSocket, true);
                    this.setHostname.invoke(sslSocket, hostname);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new AssertionError(e);
                }
            }
            this.setAlpnProtocols.invoke(sslSocket, concatLengthPrefixed(protocols));
        }
    }

    @Override // com.huawei.okhttp3.internal.platform.Platform
    @Nullable
    public String getSelectedProtocol(SSLSocket socket) {
        if (!this.sslSocketClass.isInstance(socket)) {
            return null;
        }
        try {
            byte[] alpnResult = (byte[]) this.getAlpnSelectedProtocol.invoke(socket, new Object[0]);
            if (alpnResult != null) {
                return new String(alpnResult, StandardCharsets.UTF_8);
            }
            return null;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }

    @Override // com.huawei.okhttp3.internal.platform.Platform
    public void log(int level, String message, @Nullable Throwable t) {
        int logLevel = 5;
        if (level != 5) {
            logLevel = 3;
        }
        if (t != null) {
            message = message + '\n' + Log.getStackTraceString(t);
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

    @Override // com.huawei.okhttp3.internal.platform.Platform
    @Nullable
    public Object getStackTraceForCloseable(String closer) {
        return this.closeGuard.createAndOpen(closer);
    }

    @Override // com.huawei.okhttp3.internal.platform.Platform
    public void logCloseableLeak(String message, Object stackTrace) {
        if (!this.closeGuard.warnIfOpen(stackTrace)) {
            log(5, message, null);
        }
    }

    @Override // com.huawei.okhttp3.internal.platform.Platform
    public boolean isCleartextTrafficPermitted(String hostname) {
        try {
            Class<?> networkPolicyClass = Class.forName("android.security.NetworkSecurityPolicy");
            return api24IsCleartextTrafficPermitted(hostname, networkPolicyClass, networkPolicyClass.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return super.isCleartextTrafficPermitted(hostname);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e2) {
            throw new AssertionError("unable to determine cleartext support", e2);
        }
    }

    private boolean api24IsCleartextTrafficPermitted(String hostname, Class<?> networkPolicyClass, Object networkSecurityPolicy) throws InvocationTargetException, IllegalAccessException {
        try {
            return ((Boolean) networkPolicyClass.getMethod("isCleartextTrafficPermitted", String.class).invoke(networkSecurityPolicy, hostname)).booleanValue();
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

    @Override // com.huawei.okhttp3.internal.platform.Platform
    public CertificateChainCleaner buildCertificateChainCleaner(X509TrustManager trustManager) {
        try {
            Class<?> extensionsClass = Class.forName("android.net.http.X509TrustManagerExtensions");
            return new AndroidCertificateChainCleaner(extensionsClass.getConstructor(X509TrustManager.class).newInstance(trustManager), extensionsClass.getMethod("checkServerTrusted", X509Certificate[].class, String.class, String.class));
        } catch (Exception e) {
            return super.buildCertificateChainCleaner(trustManager);
        }
    }

    @Nullable
    public static Platform buildIfSupported() {
        try {
            Class<?> sslParametersClass2 = Class.forName("com.android.org.conscrypt.SSLParametersImpl");
            Class<?> sslSocketClass2 = Class.forName("com.android.org.conscrypt.OpenSSLSocketImpl");
            if (Build.VERSION.SDK_INT >= 21) {
                try {
                    return new AndroidPlatform(sslParametersClass2, sslSocketClass2, sslSocketClass2.getDeclaredMethod("setUseSessionTickets", Boolean.TYPE), sslSocketClass2.getMethod("setHostname", String.class), sslSocketClass2.getMethod("getAlpnSelectedProtocol", new Class[0]), sslSocketClass2.getMethod("setAlpnProtocols", byte[].class));
                } catch (NoSuchMethodException e) {
                }
            }
            throw new IllegalStateException("Expected Android API level 21+ but was " + Build.VERSION.SDK_INT);
        } catch (ClassNotFoundException e2) {
            return null;
        }
    }

    @Override // com.huawei.okhttp3.internal.platform.Platform
    public TrustRootIndex buildTrustRootIndex(X509TrustManager trustManager) {
        try {
            Method method = trustManager.getClass().getDeclaredMethod("findTrustAnchorByIssuerAndSignature", X509Certificate.class);
            method.setAccessible(true);
            return new CustomTrustRootIndex(trustManager, method);
        } catch (NoSuchMethodException e) {
            return super.buildTrustRootIndex(trustManager);
        }
    }

    static final class AndroidCertificateChainCleaner extends CertificateChainCleaner {
        private final Method checkServerTrusted;
        private final Object x509TrustManagerExtensions;

        AndroidCertificateChainCleaner(Object x509TrustManagerExtensions2, Method checkServerTrusted2) {
            this.x509TrustManagerExtensions = x509TrustManagerExtensions2;
            this.checkServerTrusted = checkServerTrusted2;
        }

        @Override // com.huawei.okhttp3.internal.tls.CertificateChainCleaner
        public List<Certificate> clean(List<Certificate> chain, String hostname) throws SSLPeerUnverifiedException {
            try {
                return (List) this.checkServerTrusted.invoke(this.x509TrustManagerExtensions, (X509Certificate[]) chain.toArray(new X509Certificate[chain.size()]), "RSA", hostname);
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
            Method method = this.getMethod;
            if (method != null) {
                try {
                    Object closeGuardInstance = method.invoke(null, new Object[0]);
                    this.openMethod.invoke(closeGuardInstance, closer);
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
                openMethod2 = closeGuardClass.getMethod("open", String.class);
                warnIfOpenMethod2 = closeGuardClass.getMethod("warnIfOpen", new Class[0]);
            } catch (Exception e) {
                getMethod2 = null;
                openMethod2 = null;
                warnIfOpenMethod2 = null;
            }
            return new CloseGuard(getMethod2, openMethod2, warnIfOpenMethod2);
        }
    }

    static final class CustomTrustRootIndex implements TrustRootIndex {
        private final Method findByIssuerAndSignatureMethod;
        private final X509TrustManager trustManager;

        CustomTrustRootIndex(X509TrustManager trustManager2, Method findByIssuerAndSignatureMethod2) {
            this.findByIssuerAndSignatureMethod = findByIssuerAndSignatureMethod2;
            this.trustManager = trustManager2;
        }

        @Override // com.huawei.okhttp3.internal.tls.TrustRootIndex
        public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
            try {
                TrustAnchor trustAnchor = (TrustAnchor) this.findByIssuerAndSignatureMethod.invoke(this.trustManager, cert);
                if (trustAnchor != null) {
                    return trustAnchor.getTrustedCert();
                }
                return null;
            } catch (IllegalAccessException e) {
                throw new AssertionError("unable to get issues and signature", e);
            } catch (InvocationTargetException e2) {
                return null;
            }
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CustomTrustRootIndex)) {
                return false;
            }
            CustomTrustRootIndex that = (CustomTrustRootIndex) obj;
            if (!this.trustManager.equals(that.trustManager) || !this.findByIssuerAndSignatureMethod.equals(that.findByIssuerAndSignatureMethod)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return this.trustManager.hashCode() + (this.findByIssuerAndSignatureMethod.hashCode() * 31);
        }
    }

    @Override // com.huawei.okhttp3.internal.platform.Platform
    public SSLContext getSSLContext() {
        boolean tryTls12;
        try {
            tryTls12 = Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22;
        } catch (NoClassDefFoundError e) {
            tryTls12 = true;
        }
        if (tryTls12) {
            try {
                return SSLContext.getInstance("TLSv1.2");
            } catch (NoSuchAlgorithmException e2) {
            }
        }
        try {
            return SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e3) {
            throw new IllegalStateException("No TLS provider", e3);
        }
    }
}
