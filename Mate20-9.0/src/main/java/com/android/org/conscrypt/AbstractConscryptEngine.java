package com.android.org.conscrypt;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

abstract class AbstractConscryptEngine extends SSLEngine {
    public abstract String getApplicationProtocol();

    /* access modifiers changed from: package-private */
    public abstract String[] getApplicationProtocols();

    /* access modifiers changed from: package-private */
    public abstract byte[] getChannelId() throws SSLException;

    public abstract String getHandshakeApplicationProtocol();

    /* access modifiers changed from: package-private */
    public abstract String getHostname();

    public abstract String getPeerHost();

    public abstract int getPeerPort();

    /* access modifiers changed from: package-private */
    public abstract byte[] getTlsUnique();

    /* access modifiers changed from: package-private */
    public abstract SSLSession handshakeSession();

    /* access modifiers changed from: package-private */
    public abstract int maxSealOverhead();

    /* access modifiers changed from: package-private */
    public abstract void setApplicationProtocolSelector(ApplicationProtocolSelector applicationProtocolSelector);

    /* access modifiers changed from: package-private */
    public abstract void setApplicationProtocols(String[] strArr);

    /* access modifiers changed from: package-private */
    public abstract void setBufferAllocator(BufferAllocator bufferAllocator);

    /* access modifiers changed from: package-private */
    public abstract void setChannelIdEnabled(boolean z);

    /* access modifiers changed from: package-private */
    public abstract void setChannelIdPrivateKey(PrivateKey privateKey);

    /* access modifiers changed from: package-private */
    public abstract void setHandshakeListener(HandshakeListener handshakeListener);

    /* access modifiers changed from: package-private */
    public abstract void setHostname(String str);

    /* access modifiers changed from: package-private */
    public abstract void setUseSessionTickets(boolean z);

    public abstract SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer byteBuffer2) throws SSLException;

    public abstract SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBufferArr) throws SSLException;

    public abstract SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBufferArr, int i, int i2) throws SSLException;

    /* access modifiers changed from: package-private */
    public abstract SSLEngineResult unwrap(ByteBuffer[] byteBufferArr, int i, int i2, ByteBuffer[] byteBufferArr2, int i3, int i4) throws SSLException;

    /* access modifiers changed from: package-private */
    public abstract SSLEngineResult unwrap(ByteBuffer[] byteBufferArr, ByteBuffer[] byteBufferArr2) throws SSLException;

    public abstract SSLEngineResult wrap(ByteBuffer byteBuffer, ByteBuffer byteBuffer2) throws SSLException;

    public abstract SSLEngineResult wrap(ByteBuffer[] byteBufferArr, int i, int i2, ByteBuffer byteBuffer) throws SSLException;

    AbstractConscryptEngine() {
    }

    public final SSLSession getHandshakeSession() {
        return handshakeSession();
    }
}
