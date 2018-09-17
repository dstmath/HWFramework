package com.android.org.conscrypt;

import java.util.HashMap;
import javax.net.ssl.SSLSession;

public class ClientSessionContext extends AbstractSessionContext {
    private SSLClientSessionCache persistentCache;
    private final HashMap<HostAndPort, SSLSession> sessionsByHostAndPort;

    static class HostAndPort {
        final String host;
        final int port;

        HostAndPort(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public int hashCode() {
            return (this.host.hashCode() * 31) + this.port;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof HostAndPort)) {
                return false;
            }
            HostAndPort lhs = (HostAndPort) o;
            if (this.host.equals(lhs.host) && this.port == lhs.port) {
                z = true;
            }
            return z;
        }
    }

    public /* bridge */ /* synthetic */ SSLSession getSession(byte[] sessionId) {
        return super.getSession(sessionId);
    }

    public /* bridge */ /* synthetic */ void setSessionTimeout(int seconds) {
        super.setSessionTimeout(seconds);
    }

    public ClientSessionContext() {
        super(10);
        this.sessionsByHostAndPort = new HashMap();
    }

    public int size() {
        return this.sessionsByHostAndPort.size();
    }

    public void setPersistentCache(SSLClientSessionCache persistentCache) {
        this.persistentCache = persistentCache;
    }

    protected void sessionRemoved(SSLSession session) {
        String host = session.getPeerHost();
        int port = session.getPeerPort();
        if (host != null) {
            HostAndPort hostAndPortKey = new HostAndPort(host, port);
            synchronized (this.sessionsByHostAndPort) {
                this.sessionsByHostAndPort.remove(hostAndPortKey);
            }
        }
    }

    public SSLSession getSession(String host, int port) {
        if (host == null) {
            return null;
        }
        HostAndPort hostAndPortKey = new HostAndPort(host, port);
        synchronized (this.sessionsByHostAndPort) {
            SSLSession session = (SSLSession) this.sessionsByHostAndPort.get(hostAndPortKey);
        }
        if (session != null && session.isValid()) {
            return wrapSSLSessionIfNeeded(session);
        }
        if (this.persistentCache != null) {
            byte[] data = this.persistentCache.getSessionData(host, port);
            if (data != null) {
                session = toSession(data, host, port);
                if (session != null && session.isValid()) {
                    super.putSession(session);
                    synchronized (this.sessionsByHostAndPort) {
                        this.sessionsByHostAndPort.put(hostAndPortKey, session);
                    }
                    return wrapSSLSessionIfNeeded(session);
                }
            }
        }
        return null;
    }

    public void putSession(SSLSession session) {
        super.putSession(session);
        String host = session.getPeerHost();
        int port = session.getPeerPort();
        if (host != null) {
            HostAndPort hostAndPortKey = new HostAndPort(host, port);
            synchronized (this.sessionsByHostAndPort) {
                this.sessionsByHostAndPort.put(hostAndPortKey, session);
            }
            if (this.persistentCache != null) {
                byte[] data = toBytes(session);
                if (data != null) {
                    this.persistentCache.putSessionData(session, data);
                }
            }
        }
    }
}
