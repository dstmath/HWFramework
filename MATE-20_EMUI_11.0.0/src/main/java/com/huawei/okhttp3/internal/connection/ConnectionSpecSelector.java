package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.ConnectionSpec;
import com.huawei.okhttp3.internal.Internal;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.UnknownServiceException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

/* access modifiers changed from: package-private */
public final class ConnectionSpecSelector {
    private final List<ConnectionSpec> connectionSpecs;
    private boolean isFallback;
    private boolean isFallbackPossible;
    private int nextModeIndex = 0;

    ConnectionSpecSelector(List<ConnectionSpec> connectionSpecs2) {
        this.connectionSpecs = connectionSpecs2;
    }

    /* access modifiers changed from: package-private */
    public ConnectionSpec configureSecureSocket(SSLSocket sslSocket) throws IOException {
        ConnectionSpec tlsConfiguration = null;
        int i = this.nextModeIndex;
        int size = this.connectionSpecs.size();
        while (true) {
            if (i >= size) {
                break;
            }
            ConnectionSpec connectionSpec = this.connectionSpecs.get(i);
            if (connectionSpec.isCompatible(sslSocket)) {
                tlsConfiguration = connectionSpec;
                this.nextModeIndex = i + 1;
                break;
            }
            i++;
        }
        if (tlsConfiguration != null) {
            this.isFallbackPossible = isFallbackPossible(sslSocket);
            Internal.instance.apply(tlsConfiguration, sslSocket, this.isFallback);
            return tlsConfiguration;
        }
        throw new UnknownServiceException("Unable to find acceptable protocols. isFallback=" + this.isFallback + ", modes=" + this.connectionSpecs + ", supported protocols=" + Arrays.toString(sslSocket.getEnabledProtocols()));
    }

    /* access modifiers changed from: package-private */
    public boolean connectionFailed(IOException e) {
        this.isFallback = true;
        if (!this.isFallbackPossible || (e instanceof ProtocolException) || (e instanceof InterruptedIOException)) {
            return false;
        }
        if ((!(e instanceof SSLHandshakeException) || !(e.getCause() instanceof CertificateException)) && !(e instanceof SSLPeerUnverifiedException)) {
            return e instanceof SSLException;
        }
        return false;
    }

    private boolean isFallbackPossible(SSLSocket socket) {
        for (int i = this.nextModeIndex; i < this.connectionSpecs.size(); i++) {
            if (this.connectionSpecs.get(i).isCompatible(socket)) {
                return true;
            }
        }
        return false;
    }
}
