package com.android.org.conscrypt;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;
import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.Collections;
import java.util.List;
import javax.crypto.spec.GCMParameterSpec;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import sun.security.x509.AlgorithmId;

class Platform {

    private static class NoPreloadHolder {
        public static final Platform MAPPER = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.Platform.NoPreloadHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.Platform.NoPreloadHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.Platform.NoPreloadHolder.<clinit>():void");
        }

        private NoPreloadHolder() {
        }
    }

    /* synthetic */ Platform(Platform platform) {
        this();
    }

    public static void setup() {
        NoPreloadHolder.MAPPER.ping();
    }

    private void ping() {
    }

    private Platform() {
    }

    public static FileDescriptor getFileDescriptor(Socket s) {
        return s.getFileDescriptor$();
    }

    public static FileDescriptor getFileDescriptorFromSSLSocket(OpenSSLSocketImpl openSSLSocketImpl) {
        try {
            Field f_impl = Socket.class.getDeclaredField("impl");
            f_impl.setAccessible(true);
            Object socketImpl = f_impl.get(openSSLSocketImpl);
            Field f_fd = SocketImpl.class.getDeclaredField("fd");
            f_fd.setAccessible(true);
            return (FileDescriptor) f_fd.get(socketImpl);
        } catch (Exception e) {
            throw new RuntimeException("Can't get FileDescriptor from socket", e);
        }
    }

    public static String getCurveName(ECParameterSpec spec) {
        return spec.getCurveName();
    }

    public static void setCurveName(ECParameterSpec spec, String curveName) {
        spec.setCurveName(curveName);
    }

    public static void setSocketWriteTimeout(Socket s, long timeoutMillis) throws SocketException {
        try {
            Os.setsockoptTimeval(s.getFileDescriptor$(), OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(timeoutMillis));
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }

    public static void setSSLParameters(SSLParameters params, SSLParametersImpl impl, OpenSSLSocketImpl socket) {
        impl.setEndpointIdentificationAlgorithm(params.getEndpointIdentificationAlgorithm());
        impl.setUseCipherSuitesOrder(params.getUseCipherSuitesOrder());
        List<SNIServerName> serverNames = params.getServerNames();
        if (serverNames != null) {
            for (SNIServerName serverName : serverNames) {
                if (serverName.getType() == 0) {
                    socket.setHostname(((SNIHostName) serverName).getAsciiName());
                    return;
                }
            }
        }
    }

    public static void getSSLParameters(SSLParameters params, SSLParametersImpl impl, OpenSSLSocketImpl socket) {
        params.setEndpointIdentificationAlgorithm(impl.getEndpointIdentificationAlgorithm());
        params.setUseCipherSuitesOrder(impl.getUseCipherSuitesOrder());
        if (impl.getUseSni() && AddressUtils.isValidSniHostname(socket.getHostname())) {
            params.setServerNames(Collections.singletonList(new SNIHostName(socket.getHostname())));
        }
    }

    private static boolean checkTrusted(String methodName, X509TrustManager tm, X509Certificate[] chain, String authType, Class<?> argumentClass, Object argumentInstance) throws CertificateException {
        try {
            tm.getClass().getMethod(methodName, new Class[]{X509Certificate[].class, String.class, argumentClass}).invoke(tm, new Object[]{chain, authType, argumentInstance});
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (InvocationTargetException e2) {
            if (e2.getCause() instanceof CertificateException) {
                throw ((CertificateException) e2.getCause());
            }
            throw new RuntimeException(e2.getCause());
        }
    }

    public static void checkClientTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, OpenSSLSocketImpl socket) throws CertificateException {
        if (tm instanceof X509ExtendedTrustManager) {
            ((X509ExtendedTrustManager) tm).checkClientTrusted(chain, authType, socket);
            return;
        }
        if (!checkTrusted("checkClientTrusted", tm, chain, authType, Socket.class, socket)) {
            if (!checkTrusted("checkClientTrusted", tm, chain, authType, String.class, socket.getHandshakeSession().getPeerHost())) {
                tm.checkClientTrusted(chain, authType);
            }
        }
    }

    public static void checkServerTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, OpenSSLSocketImpl socket) throws CertificateException {
        if (tm instanceof X509ExtendedTrustManager) {
            ((X509ExtendedTrustManager) tm).checkServerTrusted(chain, authType, socket);
            return;
        }
        if (!checkTrusted("checkServerTrusted", tm, chain, authType, Socket.class, socket)) {
            if (!checkTrusted("checkServerTrusted", tm, chain, authType, String.class, socket.getHandshakeSession().getPeerHost())) {
                tm.checkServerTrusted(chain, authType);
            }
        }
    }

    public static void checkClientTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, OpenSSLEngineImpl engine) throws CertificateException {
        if (tm instanceof X509ExtendedTrustManager) {
            ((X509ExtendedTrustManager) tm).checkClientTrusted(chain, authType, engine);
            return;
        }
        if (!checkTrusted("checkClientTrusted", tm, chain, authType, SSLEngine.class, engine)) {
            if (!checkTrusted("checkClientTrusted", tm, chain, authType, String.class, engine.getHandshakeSession().getPeerHost())) {
                tm.checkClientTrusted(chain, authType);
            }
        }
    }

    public static void checkServerTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, OpenSSLEngineImpl engine) throws CertificateException {
        if (tm instanceof X509ExtendedTrustManager) {
            ((X509ExtendedTrustManager) tm).checkServerTrusted(chain, authType, engine);
            return;
        }
        if (!checkTrusted("checkServerTrusted", tm, chain, authType, SSLEngine.class, engine)) {
            if (!checkTrusted("checkServerTrusted", tm, chain, authType, String.class, engine.getHandshakeSession().getPeerHost())) {
                tm.checkServerTrusted(chain, authType);
            }
        }
    }

    public static OpenSSLKey wrapRsaKey(PrivateKey key) {
        return null;
    }

    public static void logEvent(String message) {
        try {
            Class processClass = Class.forName("android.os.Process");
            int uid = ((Integer) processClass.getMethod("myUid", (Class[]) null).invoke(processClass.newInstance(), new Object[0])).intValue();
            Class eventLogClass = Class.forName("android.util.EventLog");
            Object eventLogInstance = eventLogClass.newInstance();
            Method writeEventMethod = eventLogClass.getMethod("writeEvent", new Class[]{Integer.TYPE, Object[].class});
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(1397638484);
            objArr[1] = new Object[]{"conscrypt", Integer.valueOf(uid), message};
            writeEventMethod.invoke(eventLogInstance, objArr);
        } catch (Exception e) {
        }
    }

    public static boolean isLiteralIpAddress(String hostname) {
        return InetAddress.isNumeric(hostname);
    }

    public static SSLSocketFactory wrapSocketFactoryIfNeeded(OpenSSLSocketFactoryImpl factory) {
        return factory;
    }

    public static GCMParameters fromGCMParameterSpec(AlgorithmParameterSpec params) {
        if (!(params instanceof GCMParameterSpec)) {
            return null;
        }
        GCMParameterSpec gcmParams = (GCMParameterSpec) params;
        return new GCMParameters(gcmParams.getTLen(), gcmParams.getIV());
    }

    public static AlgorithmParameterSpec toGCMParameterSpec(int tagLenInBits, byte[] iv) {
        return new GCMParameterSpec(tagLenInBits, iv);
    }

    public static CloseGuard closeGuardGet() {
        return CloseGuard.get();
    }

    public static void closeGuardOpen(Object guardObj, String message) {
        ((CloseGuard) guardObj).open(message);
    }

    public static void closeGuardClose(Object guardObj) {
        ((CloseGuard) guardObj).close();
    }

    public static void closeGuardWarnIfOpen(Object guardObj) {
        ((CloseGuard) guardObj).warnIfOpen();
    }

    public static void blockGuardOnNetwork() {
        BlockGuard.getThreadPolicy().onNetwork();
    }

    public static String oidToAlgorithmName(String oid) {
        try {
            return AlgorithmId.get(oid).getName();
        } catch (NoSuchAlgorithmException e) {
            return oid;
        }
    }

    public static SSLSession wrapSSLSession(OpenSSLSessionImpl sslSession) {
        return new OpenSSLExtendedSessionImpl(sslSession);
    }

    public static String getHostStringFromInetSocketAddress(InetSocketAddress addr) {
        return addr.getHostString();
    }
}
