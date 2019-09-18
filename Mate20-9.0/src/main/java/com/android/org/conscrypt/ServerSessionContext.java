package com.android.org.conscrypt;

public final class ServerSessionContext extends AbstractSessionContext {
    private SSLServerSessionCache persistentCache;

    ServerSessionContext() {
        super(100);
        NativeCrypto.SSL_CTX_set_session_id_context(this.sslCtxNativePointer, this, new byte[]{32});
    }

    public void setPersistentCache(SSLServerSessionCache persistentCache2) {
        this.persistentCache = persistentCache2;
    }

    /* access modifiers changed from: package-private */
    public NativeSslSession getSessionFromPersistentCache(byte[] sessionId) {
        if (this.persistentCache != null) {
            byte[] data = this.persistentCache.getSessionData(sessionId);
            if (data != null) {
                NativeSslSession session = NativeSslSession.newInstance(this, data, null, -1);
                if (session != null && session.isValid()) {
                    cacheSession(session);
                    return session;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void onBeforeAddSession(NativeSslSession session) {
        if (this.persistentCache != null) {
            byte[] data = session.toBytes();
            if (data != null) {
                this.persistentCache.putSessionData(session.toSSLSession(), data);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onBeforeRemoveSession(NativeSslSession session) {
    }
}
