package com.android.org.conscrypt;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.PrivateKey;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

abstract class AbstractConscryptSocket extends SSLSocket {
    /* access modifiers changed from: package-private */
    @Deprecated
    public abstract byte[] getAlpnSelectedProtocol();

    public abstract String getApplicationProtocol();

    /* access modifiers changed from: package-private */
    public abstract String[] getApplicationProtocols();

    /* access modifiers changed from: package-private */
    public abstract byte[] getChannelId() throws SSLException;

    public abstract FileDescriptor getFileDescriptor$();

    public abstract String getHandshakeApplicationProtocol();

    public abstract SSLSession getHandshakeSession();

    /* access modifiers changed from: package-private */
    public abstract String getHostname();

    /* access modifiers changed from: package-private */
    public abstract String getHostnameOrIP();

    /* access modifiers changed from: package-private */
    public abstract int getSoWriteTimeout() throws SocketException;

    /* access modifiers changed from: package-private */
    public abstract byte[] getTlsUnique();

    /* access modifiers changed from: package-private */
    public abstract PeerInfoProvider peerInfoProvider();

    /* access modifiers changed from: package-private */
    @Deprecated
    public abstract void setAlpnProtocols(byte[] bArr);

    /* access modifiers changed from: package-private */
    @Deprecated
    public abstract void setAlpnProtocols(String[] strArr);

    /* access modifiers changed from: package-private */
    public abstract void setApplicationProtocolSelector(ApplicationProtocolSelector applicationProtocolSelector);

    /* access modifiers changed from: package-private */
    public abstract void setApplicationProtocols(String[] strArr);

    /* access modifiers changed from: package-private */
    public abstract void setChannelIdEnabled(boolean z);

    /* access modifiers changed from: package-private */
    public abstract void setChannelIdPrivateKey(PrivateKey privateKey);

    /* access modifiers changed from: package-private */
    public abstract void setHandshakeTimeout(int i) throws SocketException;

    /* access modifiers changed from: package-private */
    public abstract void setHostname(String str);

    /* access modifiers changed from: package-private */
    public abstract void setSoWriteTimeout(int i) throws SocketException;

    /* access modifiers changed from: package-private */
    public abstract void setUseSessionTickets(boolean z);

    AbstractConscryptSocket() {
    }

    AbstractConscryptSocket(String hostname, int port) throws IOException {
        super(hostname, port);
    }

    AbstractConscryptSocket(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    AbstractConscryptSocket(String hostname, int port, InetAddress clientAddress, int clientPort) throws IOException {
        super(hostname, port, clientAddress, clientPort);
    }

    AbstractConscryptSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        super(address, port, clientAddress, clientPort);
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public byte[] getNpnSelectedProtocol() {
        return null;
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public void setNpnProtocols(byte[] npnProtocols) {
    }
}
