package com.android.org.conscrypt;

import android.system.ErrnoException;
import android.system.OsConstants;
import android.system.StructTimeval;
import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;
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
import libcore.io.Libcore;
import libcore.net.NetworkSecurityPolicy;
import sun.security.x509.AlgorithmId;

final class Platform {

    private static class NoPreloadHolder {
        public static final Platform MAPPER = new Platform();

        private NoPreloadHolder() {
        }
    }

    public static void setup() {
        NoPreloadHolder.MAPPER.ping();
    }

    private void ping() {
    }

    private Platform() {
    }

    static String getDefaultProviderName() {
        return "AndroidOpenSSL";
    }

    static FileDescriptor getFileDescriptor(Socket s) {
        return s.getFileDescriptor$();
    }

    static FileDescriptor getFileDescriptorFromSSLSocket(AbstractConscryptSocket socket) {
        try {
            Field f_impl = Socket.class.getDeclaredField("impl");
            f_impl.setAccessible(true);
            Object socketImpl = f_impl.get(socket);
            Field f_fd = SocketImpl.class.getDeclaredField("fd");
            f_fd.setAccessible(true);
            return (FileDescriptor) f_fd.get(socketImpl);
        } catch (Exception e) {
            throw new RuntimeException("Can't get FileDescriptor from socket", e);
        }
    }

    static String getCurveName(ECParameterSpec spec) {
        return spec.getCurveName();
    }

    static void setCurveName(ECParameterSpec spec, String curveName) {
        spec.setCurveName(curveName);
    }

    static void setSocketWriteTimeout(Socket s, long timeoutMillis) throws SocketException {
        try {
            Libcore.os.setsockoptTimeval(s.getFileDescriptor$(), OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(timeoutMillis));
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }

    static void setSSLParameters(SSLParameters params, SSLParametersImpl impl, AbstractConscryptSocket socket) {
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

    static void getSSLParameters(SSLParameters params, SSLParametersImpl impl, AbstractConscryptSocket socket) {
        params.setEndpointIdentificationAlgorithm(impl.getEndpointIdentificationAlgorithm());
        params.setUseCipherSuitesOrder(impl.getUseCipherSuitesOrder());
        if (impl.getUseSni() && AddressUtils.isValidSniHostname(socket.getHostname())) {
            params.setServerNames(Collections.singletonList(new SNIHostName(socket.getHostname())));
        }
    }

    static void setSSLParameters(SSLParameters params, SSLParametersImpl impl, ConscryptEngine engine) {
        impl.setEndpointIdentificationAlgorithm(params.getEndpointIdentificationAlgorithm());
        impl.setUseCipherSuitesOrder(params.getUseCipherSuitesOrder());
        List<SNIServerName> serverNames = params.getServerNames();
        if (serverNames != null) {
            for (SNIServerName serverName : serverNames) {
                if (serverName.getType() == 0) {
                    engine.setHostname(((SNIHostName) serverName).getAsciiName());
                    return;
                }
            }
        }
    }

    static void getSSLParameters(SSLParameters params, SSLParametersImpl impl, ConscryptEngine engine) {
        params.setEndpointIdentificationAlgorithm(impl.getEndpointIdentificationAlgorithm());
        params.setUseCipherSuitesOrder(impl.getUseCipherSuitesOrder());
        if (impl.getUseSni() && AddressUtils.isValidSniHostname(engine.getHostname())) {
            params.setServerNames(Collections.singletonList(new SNIHostName(engine.getHostname())));
        }
    }

    private static boolean checkTrusted(String methodName, X509TrustManager tm, X509Certificate[] chain, String authType, Class<?> argumentClass, Object argumentInstance) throws CertificateException {
        try {
            tm.getClass().getMethod(methodName, new Class[]{X509Certificate[].class, String.class, argumentClass}).invoke(tm, new Object[]{chain, authType, argumentInstance});
            return true;
        } catch (IllegalAccessException | NoSuchMethodException e) {
            return false;
        } catch (InvocationTargetException e2) {
            if (e2.getCause() instanceof CertificateException) {
                throw ((CertificateException) e2.getCause());
            }
            throw new RuntimeException(e2.getCause());
        }
    }

    static void checkClientTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, AbstractConscryptSocket socket) throws CertificateException {
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

    static void checkServerTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, AbstractConscryptSocket socket) throws CertificateException {
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

    static void checkClientTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, ConscryptEngine engine) throws CertificateException {
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

    static void checkServerTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, ConscryptEngine engine) throws CertificateException {
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

    static OpenSSLKey wrapRsaKey(PrivateKey key) {
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v1, resolved type: java.lang.Object[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    static void logEvent(String message) {
        try {
            Class processClass = Class.forName("android.os.Process");
            int uid = ((Integer) processClass.getMethod("myUid", null).invoke(processClass.newInstance(), new Object[0])).intValue();
            Class eventLogClass = Class.forName("android.util.EventLog");
            Object eventLogInstance = eventLogClass.newInstance();
            eventLogClass.getMethod("writeEvent", new Class[]{Integer.TYPE, Object[].class}).invoke(eventLogInstance, new Object[]{1397638484, new Object[]{"conscrypt", Integer.valueOf(uid), message}});
        } catch (Exception e) {
        }
    }

    static boolean isLiteralIpAddress(String hostname) {
        return InetAddress.isNumeric(hostname);
    }

    static SSLEngine wrapEngine(ConscryptEngine engine) {
        return new Java8EngineWrapper(engine);
    }

    static SSLEngine unwrapEngine(SSLEngine engine) {
        return Java8EngineWrapper.getDelegate(engine);
    }

    static ConscryptEngineSocket createEngineSocket(SSLParametersImpl sslParameters) throws IOException {
        return new Java8EngineSocket(sslParameters);
    }

    static ConscryptEngineSocket createEngineSocket(String hostname, int port, SSLParametersImpl sslParameters) throws IOException {
        return new Java8EngineSocket(hostname, port, sslParameters);
    }

    static ConscryptEngineSocket createEngineSocket(InetAddress address, int port, SSLParametersImpl sslParameters) throws IOException {
        return new Java8EngineSocket(address, port, sslParameters);
    }

    static ConscryptEngineSocket createEngineSocket(String hostname, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        Java8EngineSocket java8EngineSocket = new Java8EngineSocket(hostname, port, clientAddress, clientPort, sslParameters);
        return java8EngineSocket;
    }

    static ConscryptEngineSocket createEngineSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        Java8EngineSocket java8EngineSocket = new Java8EngineSocket(address, port, clientAddress, clientPort, sslParameters);
        return java8EngineSocket;
    }

    static ConscryptEngineSocket createEngineSocket(Socket socket, String hostname, int port, boolean autoClose, SSLParametersImpl sslParameters) throws IOException {
        Java8EngineSocket java8EngineSocket = new Java8EngineSocket(socket, hostname, port, autoClose, sslParameters);
        return java8EngineSocket;
    }

    static ConscryptFileDescriptorSocket createFileDescriptorSocket(SSLParametersImpl sslParameters) throws IOException {
        return new Java8FileDescriptorSocket(sslParameters);
    }

    static ConscryptFileDescriptorSocket createFileDescriptorSocket(String hostname, int port, SSLParametersImpl sslParameters) throws IOException {
        return new Java8FileDescriptorSocket(hostname, port, sslParameters);
    }

    static ConscryptFileDescriptorSocket createFileDescriptorSocket(InetAddress address, int port, SSLParametersImpl sslParameters) throws IOException {
        return new Java8FileDescriptorSocket(address, port, sslParameters);
    }

    static ConscryptFileDescriptorSocket createFileDescriptorSocket(String hostname, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        Java8FileDescriptorSocket java8FileDescriptorSocket = new Java8FileDescriptorSocket(hostname, port, clientAddress, clientPort, sslParameters);
        return java8FileDescriptorSocket;
    }

    static ConscryptFileDescriptorSocket createFileDescriptorSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort, SSLParametersImpl sslParameters) throws IOException {
        Java8FileDescriptorSocket java8FileDescriptorSocket = new Java8FileDescriptorSocket(address, port, clientAddress, clientPort, sslParameters);
        return java8FileDescriptorSocket;
    }

    static ConscryptFileDescriptorSocket createFileDescriptorSocket(Socket socket, String hostname, int port, boolean autoClose, SSLParametersImpl sslParameters) throws IOException {
        Java8FileDescriptorSocket java8FileDescriptorSocket = new Java8FileDescriptorSocket(socket, hostname, port, autoClose, sslParameters);
        return java8FileDescriptorSocket;
    }

    static SSLSocketFactory wrapSocketFactoryIfNeeded(OpenSSLSocketFactoryImpl factory) {
        return factory;
    }

    static GCMParameters fromGCMParameterSpec(AlgorithmParameterSpec params) {
        if (!(params instanceof GCMParameterSpec)) {
            return null;
        }
        GCMParameterSpec gcmParams = (GCMParameterSpec) params;
        return new GCMParameters(gcmParams.getTLen(), gcmParams.getIV());
    }

    static AlgorithmParameterSpec fromGCMParameters(AlgorithmParameters params) {
        try {
            return params.getParameterSpec(GCMParameterSpec.class);
        } catch (InvalidParameterSpecException e) {
            return null;
        }
    }

    static AlgorithmParameterSpec toGCMParameterSpec(int tagLenInBits, byte[] iv) {
        return new GCMParameterSpec(tagLenInBits, iv);
    }

    static CloseGuard closeGuardGet() {
        return CloseGuard.get();
    }

    static void closeGuardOpen(Object guardObj, String message) {
        ((CloseGuard) guardObj).open(message);
    }

    static void closeGuardClose(Object guardObj) {
        ((CloseGuard) guardObj).close();
    }

    static void closeGuardWarnIfOpen(Object guardObj) {
        ((CloseGuard) guardObj).warnIfOpen();
    }

    static void blockGuardOnNetwork() {
        BlockGuard.getThreadPolicy().onNetwork();
    }

    static String oidToAlgorithmName(String oid) {
        try {
            return AlgorithmId.get(oid).getName();
        } catch (NoSuchAlgorithmException e) {
            return oid;
        }
    }

    static SSLSession wrapSSLSession(ConscryptSession sslSession) {
        return new Java8ExtendedSSLSession(sslSession);
    }

    public static String getOriginalHostNameFromInetAddress(InetAddress addr) {
        try {
            Method getHolder = InetAddress.class.getDeclaredMethod("holder", new Class[0]);
            getHolder.setAccessible(true);
            Method getOriginalHostName = Class.forName("java.net.InetAddress$InetAddressHolder").getDeclaredMethod("getOriginalHostName", new Class[0]);
            getOriginalHostName.setAccessible(true);
            String originalHostName = (String) getOriginalHostName.invoke(getHolder.invoke(addr, new Object[0]), new Object[0]);
            if (originalHostName == null) {
                return addr.getHostAddress();
            }
            return originalHostName;
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to get originalHostName", e);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException e2) {
            return addr.getHostAddress();
        }
    }

    static String getHostStringFromInetSocketAddress(InetSocketAddress addr) {
        return addr.getHostString();
    }

    static boolean isCTVerificationRequired(String hostname) {
        return NetworkSecurityPolicy.getInstance().isCertificateTransparencyVerificationRequired(hostname);
    }
}
