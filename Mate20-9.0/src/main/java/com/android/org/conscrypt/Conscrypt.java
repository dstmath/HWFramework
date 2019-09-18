package com.android.org.conscrypt;

import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.PrivateKey;
import java.security.Provider;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public final class Conscrypt {
    private Conscrypt() {
    }

    public static boolean isAvailable() {
        try {
            checkAvailability();
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    public static void checkAvailability() {
        NativeCrypto.checkAvailability();
    }

    public static boolean isConscrypt(Provider provider) {
        return provider instanceof OpenSSLProvider;
    }

    public static Provider newProvider() {
        checkAvailability();
        return new OpenSSLProvider();
    }

    public static Provider newProvider(String providerName) {
        checkAvailability();
        return new OpenSSLProvider(providerName);
    }

    public static int maxEncryptedPacketLength() {
        return 16709;
    }

    public static X509TrustManager getDefaultX509TrustManager() throws KeyManagementException {
        checkAvailability();
        return SSLParametersImpl.getDefaultX509TrustManager();
    }

    public static boolean isConscrypt(SSLContext context) {
        return context.getProvider() instanceof OpenSSLProvider;
    }

    public static SSLContextSpi newPreferredSSLContextSpi() {
        checkAvailability();
        return OpenSSLContextImpl.getPreferred();
    }

    public static void setClientSessionCache(SSLContext context, SSLClientSessionCache cache) {
        SSLSessionContext clientContext = context.getClientSessionContext();
        if (clientContext instanceof ClientSessionContext) {
            ((ClientSessionContext) clientContext).setPersistentCache(cache);
            return;
        }
        throw new IllegalArgumentException("Not a conscrypt client context: " + clientContext.getClass().getName());
    }

    public static void setServerSessionCache(SSLContext context, SSLServerSessionCache cache) {
        SSLSessionContext serverContext = context.getServerSessionContext();
        if (serverContext instanceof ServerSessionContext) {
            ((ServerSessionContext) serverContext).setPersistentCache(cache);
            return;
        }
        throw new IllegalArgumentException("Not a conscrypt client context: " + serverContext.getClass().getName());
    }

    public static boolean isConscrypt(SSLSocketFactory factory) {
        return factory instanceof OpenSSLSocketFactoryImpl;
    }

    private static OpenSSLSocketFactoryImpl toConscrypt(SSLSocketFactory factory) {
        if (isConscrypt(factory)) {
            return (OpenSSLSocketFactoryImpl) factory;
        }
        throw new IllegalArgumentException("Not a conscrypt socket factory: " + factory.getClass().getName());
    }

    public static void setUseEngineSocketByDefault(boolean useEngineSocket) {
        OpenSSLSocketFactoryImpl.setUseEngineSocketByDefault(useEngineSocket);
        OpenSSLServerSocketFactoryImpl.setUseEngineSocketByDefault(useEngineSocket);
    }

    public static void setUseEngineSocket(SSLSocketFactory factory, boolean useEngineSocket) {
        toConscrypt(factory).setUseEngineSocket(useEngineSocket);
    }

    public static boolean isConscrypt(SSLServerSocketFactory factory) {
        return factory instanceof OpenSSLServerSocketFactoryImpl;
    }

    private static OpenSSLServerSocketFactoryImpl toConscrypt(SSLServerSocketFactory factory) {
        if (isConscrypt(factory)) {
            return (OpenSSLServerSocketFactoryImpl) factory;
        }
        throw new IllegalArgumentException("Not a conscrypt server socket factory: " + factory.getClass().getName());
    }

    public static void setUseEngineSocket(SSLServerSocketFactory factory, boolean useEngineSocket) {
        toConscrypt(factory).setUseEngineSocket(useEngineSocket);
    }

    public static boolean isConscrypt(SSLSocket socket) {
        return socket instanceof AbstractConscryptSocket;
    }

    private static AbstractConscryptSocket toConscrypt(SSLSocket socket) {
        if (isConscrypt(socket)) {
            return (AbstractConscryptSocket) socket;
        }
        throw new IllegalArgumentException("Not a conscrypt socket: " + socket.getClass().getName());
    }

    public static void setHostname(SSLSocket socket, String hostname) {
        toConscrypt(socket).setHostname(hostname);
    }

    public static String getHostname(SSLSocket socket) {
        return toConscrypt(socket).getHostname();
    }

    public static String getHostnameOrIP(SSLSocket socket) {
        return toConscrypt(socket).getHostnameOrIP();
    }

    public static void setUseSessionTickets(SSLSocket socket, boolean useSessionTickets) {
        toConscrypt(socket).setUseSessionTickets(useSessionTickets);
    }

    public static void setChannelIdEnabled(SSLSocket socket, boolean enabled) {
        toConscrypt(socket).setChannelIdEnabled(enabled);
    }

    public static byte[] getChannelId(SSLSocket socket) throws SSLException {
        return toConscrypt(socket).getChannelId();
    }

    public static void setChannelIdPrivateKey(SSLSocket socket, PrivateKey privateKey) {
        toConscrypt(socket).setChannelIdPrivateKey(privateKey);
    }

    public static String getApplicationProtocol(SSLSocket socket) {
        return toConscrypt(socket).getApplicationProtocol();
    }

    public static void setApplicationProtocolSelector(SSLSocket socket, ApplicationProtocolSelector selector) {
        toConscrypt(socket).setApplicationProtocolSelector(selector);
    }

    public static void setApplicationProtocols(SSLSocket socket, String[] protocols) {
        toConscrypt(socket).setApplicationProtocols(protocols);
    }

    public static String[] getApplicationProtocols(SSLSocket socket) {
        return toConscrypt(socket).getApplicationProtocols();
    }

    public static byte[] getTlsUnique(SSLSocket socket) {
        return toConscrypt(socket).getTlsUnique();
    }

    public static boolean isConscrypt(SSLEngine engine) {
        return engine instanceof AbstractConscryptEngine;
    }

    private static AbstractConscryptEngine toConscrypt(SSLEngine engine) {
        if (isConscrypt(engine)) {
            return (AbstractConscryptEngine) engine;
        }
        throw new IllegalArgumentException("Not a conscrypt engine: " + engine.getClass().getName());
    }

    public static void setBufferAllocator(SSLEngine engine, BufferAllocator bufferAllocator) {
        toConscrypt(engine).setBufferAllocator(bufferAllocator);
    }

    public static void setHostname(SSLEngine engine, String hostname) {
        toConscrypt(engine).setHostname(hostname);
    }

    public static String getHostname(SSLEngine engine) {
        return toConscrypt(engine).getHostname();
    }

    public static int maxSealOverhead(SSLEngine engine) {
        return toConscrypt(engine).maxSealOverhead();
    }

    public static void setHandshakeListener(SSLEngine engine, HandshakeListener handshakeListener) {
        toConscrypt(engine).setHandshakeListener(handshakeListener);
    }

    public static void setChannelIdEnabled(SSLEngine engine, boolean enabled) {
        toConscrypt(engine).setChannelIdEnabled(enabled);
    }

    public static byte[] getChannelId(SSLEngine engine) throws SSLException {
        return toConscrypt(engine).getChannelId();
    }

    public static void setChannelIdPrivateKey(SSLEngine engine, PrivateKey privateKey) {
        toConscrypt(engine).setChannelIdPrivateKey(privateKey);
    }

    public static SSLEngineResult unwrap(SSLEngine engine, ByteBuffer[] srcs, ByteBuffer[] dsts) throws SSLException {
        return toConscrypt(engine).unwrap(srcs, dsts);
    }

    public static SSLEngineResult unwrap(SSLEngine engine, ByteBuffer[] srcs, int srcsOffset, int srcsLength, ByteBuffer[] dsts, int dstsOffset, int dstsLength) throws SSLException {
        return toConscrypt(engine).unwrap(srcs, srcsOffset, srcsLength, dsts, dstsOffset, dstsLength);
    }

    public static void setUseSessionTickets(SSLEngine engine, boolean useSessionTickets) {
        toConscrypt(engine).setUseSessionTickets(useSessionTickets);
    }

    public static void setApplicationProtocols(SSLEngine engine, String[] protocols) {
        toConscrypt(engine).setApplicationProtocols(protocols);
    }

    public static String[] getApplicationProtocols(SSLEngine engine) {
        return toConscrypt(engine).getApplicationProtocols();
    }

    public static void setApplicationProtocolSelector(SSLEngine engine, ApplicationProtocolSelector selector) {
        toConscrypt(engine).setApplicationProtocolSelector(selector);
    }

    public static String getApplicationProtocol(SSLEngine engine) {
        return toConscrypt(engine).getApplicationProtocol();
    }

    public static byte[] getTlsUnique(SSLEngine engine) {
        return toConscrypt(engine).getTlsUnique();
    }
}
