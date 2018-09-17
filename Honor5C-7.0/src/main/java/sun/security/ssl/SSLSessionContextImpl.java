package sun.security.ssl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import sun.security.util.Cache;
import sun.security.util.Cache.CacheVisitor;

final class SSLSessionContextImpl implements SSLSessionContext {
    private int cacheLimit;
    private Cache<SessionId, SSLSessionImpl> sessionCache;
    private Cache<String, SSLSessionImpl> sessionHostPortCache;
    private int timeout;

    final class SessionCacheVisitor implements CacheVisitor<SessionId, SSLSessionImpl> {
        Vector<byte[]> ids;

        SessionCacheVisitor() {
            this.ids = null;
        }

        public void visit(Map<SessionId, SSLSessionImpl> map) {
            this.ids = new Vector(map.size());
            for (SessionId key : map.keySet()) {
                if (!SSLSessionContextImpl.this.isTimedout((SSLSessionImpl) map.get(key))) {
                    this.ids.addElement(key.getId());
                }
            }
        }

        public Enumeration<byte[]> getSessionIds() {
            if (this.ids != null) {
                return this.ids.elements();
            }
            return new Vector().elements();
        }
    }

    SSLSessionContextImpl() {
        this.cacheLimit = getDefaultCacheLimit();
        this.timeout = 86400;
        this.sessionCache = Cache.newSoftMemoryCache(this.cacheLimit, this.timeout);
        this.sessionHostPortCache = Cache.newSoftMemoryCache(this.cacheLimit, this.timeout);
    }

    public SSLSession getSession(byte[] sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("session id cannot be null");
        }
        SSLSessionImpl sess = (SSLSessionImpl) this.sessionCache.get(new SessionId(sessionId));
        if (isTimedout(sess)) {
            return null;
        }
        return sess;
    }

    public Enumeration<byte[]> getIds() {
        SessionCacheVisitor scVisitor = new SessionCacheVisitor();
        this.sessionCache.accept(scVisitor);
        return scVisitor.getSessionIds();
    }

    public void setSessionTimeout(int seconds) throws IllegalArgumentException {
        if (seconds < 0) {
            throw new IllegalArgumentException();
        } else if (this.timeout != seconds) {
            this.sessionCache.setTimeout(seconds);
            this.sessionHostPortCache.setTimeout(seconds);
            this.timeout = seconds;
        }
    }

    public int getSessionTimeout() {
        return this.timeout;
    }

    public void setSessionCacheSize(int size) throws IllegalArgumentException {
        if (size < 0) {
            throw new IllegalArgumentException();
        } else if (this.cacheLimit != size) {
            this.sessionCache.setCapacity(size);
            this.sessionHostPortCache.setCapacity(size);
            this.cacheLimit = size;
        }
    }

    public int getSessionCacheSize() {
        return this.cacheLimit;
    }

    SSLSessionImpl get(byte[] id) {
        return (SSLSessionImpl) getSession(id);
    }

    SSLSessionImpl get(String hostname, int port) {
        if (hostname == null && port == -1) {
            return null;
        }
        SSLSessionImpl sess = (SSLSessionImpl) this.sessionHostPortCache.get(getKey(hostname, port));
        if (isTimedout(sess)) {
            return null;
        }
        return sess;
    }

    private String getKey(String hostname, int port) {
        return (hostname + ":" + String.valueOf(port)).toLowerCase(Locale.ENGLISH);
    }

    void put(SSLSessionImpl s) {
        this.sessionCache.put(s.getSessionId(), s);
        if (!(s.getPeerHost() == null || s.getPeerPort() == -1)) {
            this.sessionHostPortCache.put(getKey(s.getPeerHost(), s.getPeerPort()), s);
        }
        s.setContext(this);
    }

    void remove(SessionId key) {
        SSLSessionImpl s = (SSLSessionImpl) this.sessionCache.get(key);
        if (s != null) {
            this.sessionCache.remove(key);
            this.sessionHostPortCache.remove(getKey(s.getPeerHost(), s.getPeerPort()));
        }
    }

    private int getDefaultCacheLimit() {
        int cacheLimit = 0;
        try {
            String s = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("javax.net.ssl.sessionCacheSize");
                }
            });
            if (s != null) {
                cacheLimit = Integer.parseInt(s);
            } else {
                cacheLimit = 0;
            }
        } catch (Exception e) {
        }
        if (cacheLimit > 0) {
            return cacheLimit;
        }
        return 0;
    }

    boolean isTimedout(SSLSession sess) {
        if (this.timeout == 0 || sess == null || sess.getCreationTime() + (((long) this.timeout) * 1000) > System.currentTimeMillis()) {
            return false;
        }
        sess.invalidate();
        return true;
    }
}
