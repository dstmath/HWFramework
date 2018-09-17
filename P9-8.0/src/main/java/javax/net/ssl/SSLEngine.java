package javax.net.ssl;

import java.nio.ByteBuffer;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

public abstract class SSLEngine {
    private String peerHost = null;
    private int peerPort = -1;

    public abstract void beginHandshake() throws SSLException;

    public abstract void closeInbound() throws SSLException;

    public abstract void closeOutbound();

    public abstract Runnable getDelegatedTask();

    public abstract boolean getEnableSessionCreation();

    public abstract String[] getEnabledCipherSuites();

    public abstract String[] getEnabledProtocols();

    public abstract HandshakeStatus getHandshakeStatus();

    public abstract boolean getNeedClientAuth();

    public abstract SSLSession getSession();

    public abstract String[] getSupportedCipherSuites();

    public abstract String[] getSupportedProtocols();

    public abstract boolean getUseClientMode();

    public abstract boolean getWantClientAuth();

    public abstract boolean isInboundDone();

    public abstract boolean isOutboundDone();

    public abstract void setEnableSessionCreation(boolean z);

    public abstract void setEnabledCipherSuites(String[] strArr);

    public abstract void setEnabledProtocols(String[] strArr);

    public abstract void setNeedClientAuth(boolean z);

    public abstract void setUseClientMode(boolean z);

    public abstract void setWantClientAuth(boolean z);

    public abstract SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBufferArr, int i, int i2) throws SSLException;

    public abstract SSLEngineResult wrap(ByteBuffer[] byteBufferArr, int i, int i2, ByteBuffer byteBuffer) throws SSLException;

    protected SSLEngine() {
    }

    protected SSLEngine(String peerHost, int peerPort) {
        this.peerHost = peerHost;
        this.peerPort = peerPort;
    }

    public String getPeerHost() {
        return this.peerHost;
    }

    public int getPeerPort() {
        return this.peerPort;
    }

    public SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        return wrap(new ByteBuffer[]{src}, 0, 1, dst);
    }

    public SSLEngineResult wrap(ByteBuffer[] srcs, ByteBuffer dst) throws SSLException {
        if (srcs != null) {
            return wrap(srcs, 0, srcs.length, dst);
        }
        throw new IllegalArgumentException("src == null");
    }

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        return unwrap(src, new ByteBuffer[]{dst}, 0, 1);
    }

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts) throws SSLException {
        if (dsts != null) {
            return unwrap(src, dsts, 0, dsts.length);
        }
        throw new IllegalArgumentException("dsts == null");
    }

    public SSLSession getHandshakeSession() {
        throw new UnsupportedOperationException();
    }

    public SSLParameters getSSLParameters() {
        SSLParameters params = new SSLParameters();
        params.setCipherSuites(getEnabledCipherSuites());
        params.setProtocols(getEnabledProtocols());
        if (getNeedClientAuth()) {
            params.setNeedClientAuth(true);
        } else if (getWantClientAuth()) {
            params.setWantClientAuth(true);
        }
        return params;
    }

    public void setSSLParameters(SSLParameters params) {
        String[] s = params.getCipherSuites();
        if (s != null) {
            setEnabledCipherSuites(s);
        }
        s = params.getProtocols();
        if (s != null) {
            setEnabledProtocols(s);
        }
        if (params.getNeedClientAuth()) {
            setNeedClientAuth(true);
        } else if (params.getWantClientAuth()) {
            setWantClientAuth(true);
        } else {
            setWantClientAuth(false);
        }
    }
}
