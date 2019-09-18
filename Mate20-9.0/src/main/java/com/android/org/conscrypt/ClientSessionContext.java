package com.android.org.conscrypt;

import java.util.HashMap;
import java.util.Map;

public final class ClientSessionContext extends AbstractSessionContext {
    private SSLClientSessionCache persistentCache;
    private final Map<HostAndPort, NativeSslSession> sessionsByHostAndPort = new HashMap();

    private static final class HostAndPort {
        final String host;
        final int port;

        HostAndPort(String host2, int port2) {
            this.host = host2;
            this.port = port2;
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

    ClientSessionContext() {
        super(10);
    }

    public void setPersistentCache(SSLClientSessionCache persistentCache2) {
        this.persistentCache = persistentCache2;
    }

    /* access modifiers changed from: package-private */
    public NativeSslSession getCachedSession(String hostName, int port, SSLParametersImpl sslParameters) {
        if (hostName == null) {
            return null;
        }
        NativeSslSession session = getSession(hostName, port);
        if (session == null) {
            return null;
        }
        String protocol = session.getProtocol();
        boolean protocolFound = false;
        String[] strArr = sslParameters.enabledProtocols;
        int length = strArr.length;
        int i = 0;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            } else if (protocol.equals(strArr[i2])) {
                protocolFound = true;
                break;
            } else {
                i2++;
            }
        }
        if (!protocolFound) {
            return null;
        }
        String cipherSuite = session.getCipherSuite();
        boolean cipherSuiteFound = false;
        String[] strArr2 = sslParameters.enabledCipherSuites;
        int length2 = strArr2.length;
        while (true) {
            if (i >= length2) {
                break;
            } else if (cipherSuite.equals(strArr2[i])) {
                cipherSuiteFound = true;
                break;
            } else {
                i++;
            }
        }
        if (!cipherSuiteFound) {
            return null;
        }
        return session;
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return this.sessionsByHostAndPort.size();
    }

    private NativeSslSession getSession(String host, int port) {
        NativeSslSession session;
        if (host == null) {
            return null;
        }
        HostAndPort key = new HostAndPort(host, port);
        synchronized (this.sessionsByHostAndPort) {
            session = this.sessionsByHostAndPort.get(key);
        }
        if (session != null && session.isValid()) {
            return session;
        }
        if (this.persistentCache != null) {
            byte[] data = this.persistentCache.getSessionData(host, port);
            if (data != null) {
                NativeSslSession session2 = NativeSslSession.newInstance(this, data, host, port);
                if (session2 != null && session2.isValid()) {
                    synchronized (this.sessionsByHostAndPort) {
                        this.sessionsByHostAndPort.put(key, session2);
                    }
                    return session2;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void onBeforeAddSession(NativeSslSession session) {
        String host = session.getPeerHost();
        int port = session.getPeerPort();
        if (host != null) {
            HostAndPort key = new HostAndPort(host, port);
            synchronized (this.sessionsByHostAndPort) {
                this.sessionsByHostAndPort.put(key, session);
            }
            if (this.persistentCache != null) {
                byte[] data = session.toBytes();
                if (data != null) {
                    this.persistentCache.putSessionData(session.toSSLSession(), data);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onBeforeRemoveSession(NativeSslSession session) {
        String host = session.getPeerHost();
        if (host != null) {
            HostAndPort hostAndPortKey = new HostAndPort(host, session.getPeerPort());
            synchronized (this.sessionsByHostAndPort) {
                this.sessionsByHostAndPort.remove(hostAndPortKey);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public NativeSslSession getSessionFromPersistentCache(byte[] sessionId) {
        return null;
    }
}
