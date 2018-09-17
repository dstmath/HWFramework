package com.android.org.conscrypt;

import java.io.FileDescriptor;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.Provider;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public final class Conscrypt {

    public static final class Engines {
        private Engines() {
        }

        public static boolean isConscrypt(SSLEngine engine) {
            return engine instanceof OpenSSLEngineImpl;
        }

        private static OpenSSLEngineImpl toConscrypt(SSLEngine engine) {
            if (isConscrypt(engine)) {
                return (OpenSSLEngineImpl) engine;
            }
            throw new IllegalArgumentException("Not a conscrypt engine: " + engine.getClass().getName());
        }

        public static void setHostname(SSLEngine engine, String hostname) {
            toConscrypt(engine).setSniHostname(hostname);
        }

        public static String getHostname(SSLEngine engine) {
            return toConscrypt(engine).getSniHostname();
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

        public static void setAlpnProtocols(SSLEngine engine, String[] alpnProtocols) {
            toConscrypt(engine).setAlpnProtocols(alpnProtocols);
        }

        public static String getAlpnSelectedProtocol(SSLEngine engine) {
            return Conscrypt.toProtocolString(toConscrypt(engine).getAlpnSelectedProtocol());
        }
    }

    public static final class ServerSocketFactories {
        private ServerSocketFactories() {
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

        public static void setUseEngineSocketByDefault(boolean useEngineSocket) {
            OpenSSLServerSocketFactoryImpl.setUseEngineSocketByDefault(useEngineSocket);
        }

        public static void setUseEngineSocket(SSLServerSocketFactory factory, boolean useEngineSocket) {
            toConscrypt(factory).setUseEngineSocket(useEngineSocket);
        }
    }

    public static final class SocketFactories {
        private SocketFactories() {
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
        }

        public static void setUseEngineSocket(SSLSocketFactory factory, boolean useEngineSocket) {
            toConscrypt(factory).setUseEngineSocket(useEngineSocket);
        }
    }

    public static final class Sockets {
        private Sockets() {
        }

        public static boolean isConscrypt(SSLSocket socket) {
            return socket instanceof OpenSSLSocketImpl;
        }

        private static OpenSSLSocketImpl toConscrypt(SSLSocket socket) {
            if (isConscrypt(socket)) {
                return (OpenSSLSocketImpl) socket;
            }
            throw new IllegalArgumentException("Not a conscrypt socket: " + socket.getClass().getName());
        }

        public static void setUseSessionTickets(SSLSocket socket, boolean useSessionTickets) {
            toConscrypt(socket).setUseSessionTickets(useSessionTickets);
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

        public static void setSoWriteTimeout(SSLSocket socket, int writeTimeoutMilliseconds) throws SocketException {
            toConscrypt(socket).setSoWriteTimeout(writeTimeoutMilliseconds);
        }

        public static int getSoWriteTimeout(SSLSocket socket) throws SocketException {
            return toConscrypt(socket).getSoWriteTimeout();
        }

        public static void setHandshakeTimeout(SSLSocket socket, int handshakeTimeoutMilliseconds) throws SocketException {
            toConscrypt(socket).setHandshakeTimeout(handshakeTimeoutMilliseconds);
        }

        public static FileDescriptor getFileDescriptor(SSLSocket socket) {
            return toConscrypt(socket).getFileDescriptor$();
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

        public static String getAlpnSelectedProtocol(SSLSocket socket) {
            return Conscrypt.toProtocolString(toConscrypt(socket).getAlpnSelectedProtocol());
        }

        public static void setAlpnProtocols(SSLSocket socket, String[] alpnProtocols) {
            toConscrypt(socket).setAlpnProtocols(alpnProtocols);
        }
    }

    private Conscrypt() {
    }

    public static Provider newProvider() {
        return new OpenSSLProvider();
    }

    public static Provider newProvider(String providerName) {
        return new OpenSSLProvider(providerName);
    }

    private static String toProtocolString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
