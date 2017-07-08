package com.android.okhttp.internal;

import com.android.okhttp.ConnectionSpec;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.UnknownServiceException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLSocket;

public final class ConnectionSpecSelector {
    private final List<ConnectionSpec> connectionSpecs;
    private boolean isFallback;
    private boolean isFallbackPossible;
    private int nextModeIndex;

    public ConnectionSpecSelector(List<ConnectionSpec> connectionSpecs) {
        this.nextModeIndex = 0;
        this.connectionSpecs = connectionSpecs;
    }

    public ConnectionSpec configureSecureSocket(SSLSocket sslSocket) throws IOException {
        ConnectionSpec connectionSpec = null;
        int size = this.connectionSpecs.size();
        for (int i = this.nextModeIndex; i < size; i++) {
            ConnectionSpec connectionSpec2 = (ConnectionSpec) this.connectionSpecs.get(i);
            if (connectionSpec2.isCompatible(sslSocket)) {
                connectionSpec = connectionSpec2;
                this.nextModeIndex = i + 1;
                break;
            }
        }
        if (connectionSpec == null) {
            throw new UnknownServiceException("Unable to find acceptable protocols. isFallback=" + this.isFallback + ", modes=" + this.connectionSpecs + ", supported protocols=" + Arrays.toString(sslSocket.getEnabledProtocols()));
        }
        this.isFallbackPossible = isFallbackPossible(sslSocket);
        Internal.instance.apply(connectionSpec, sslSocket, this.isFallback);
        return connectionSpec;
    }

    public boolean connectionFailed(IOException e) {
        boolean z = true;
        this.isFallback = true;
        if (!this.isFallbackPossible || (e instanceof ProtocolException) || (e instanceof InterruptedIOException)) {
            return false;
        }
        if (((e instanceof SSLHandshakeException) && (e.getCause() instanceof CertificateException)) || (e instanceof SSLPeerUnverifiedException)) {
            return false;
        }
        if (!(e instanceof SSLHandshakeException)) {
            z = e instanceof SSLProtocolException;
        }
        return z;
    }

    private boolean isFallbackPossible(SSLSocket socket) {
        for (int i = this.nextModeIndex; i < this.connectionSpecs.size(); i++) {
            if (((ConnectionSpec) this.connectionSpecs.get(i)).isCompatible(socket)) {
                return true;
            }
        }
        return false;
    }
}
