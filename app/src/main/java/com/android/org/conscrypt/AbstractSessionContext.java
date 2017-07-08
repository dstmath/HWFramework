package com.android.org.conscrypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;

abstract class AbstractSessionContext implements SSLSessionContext {
    private static final int DEFAULT_SESSION_TIMEOUT_SECONDS = 28800;
    static final int OPEN_SSL = 1;
    volatile int maximumSize;
    private final Map<ByteArray, SSLSession> sessions;
    final long sslCtxNativePointer;
    volatile int timeout;

    /* renamed from: com.android.org.conscrypt.AbstractSessionContext.2 */
    class AnonymousClass2 implements Enumeration<byte[]> {
        private SSLSession next;
        final /* synthetic */ Iterator val$i;

        AnonymousClass2(Iterator val$i) {
            this.val$i = val$i;
        }

        public boolean hasMoreElements() {
            if (this.next != null) {
                return true;
            }
            while (this.val$i.hasNext()) {
                SSLSession session = (SSLSession) this.val$i.next();
                if (session.isValid()) {
                    this.next = session;
                    return true;
                }
            }
            this.next = null;
            return false;
        }

        public byte[] nextElement() {
            if (hasMoreElements()) {
                byte[] id = this.next.getId();
                this.next = null;
                return id;
            }
            throw new NoSuchElementException();
        }
    }

    protected abstract void sessionRemoved(SSLSession sSLSession);

    AbstractSessionContext(int maximumSize) {
        this.timeout = DEFAULT_SESSION_TIMEOUT_SECONDS;
        this.sslCtxNativePointer = NativeCrypto.SSL_CTX_new();
        this.sessions = new LinkedHashMap<ByteArray, SSLSession>() {
            protected boolean removeEldestEntry(Entry<ByteArray, SSLSession> eldest) {
                boolean remove;
                if (AbstractSessionContext.this.maximumSize <= 0 || size() <= AbstractSessionContext.this.maximumSize) {
                    remove = false;
                } else {
                    remove = true;
                }
                if (remove) {
                    remove(eldest.getKey());
                    AbstractSessionContext.this.sessionRemoved((SSLSession) eldest.getValue());
                }
                return false;
            }
        };
        this.maximumSize = maximumSize;
    }

    private Iterator<SSLSession> sessionIterator() {
        Iterator<SSLSession> it;
        synchronized (this.sessions) {
            it = Arrays.asList((SSLSession[]) this.sessions.values().toArray(new SSLSession[this.sessions.size()])).iterator();
        }
        return it;
    }

    public final Enumeration<byte[]> getIds() {
        return new AnonymousClass2(sessionIterator());
    }

    public final int getSessionCacheSize() {
        return this.maximumSize;
    }

    public final int getSessionTimeout() {
        return this.timeout;
    }

    protected void trimToSize() {
        synchronized (this.sessions) {
            int size = this.sessions.size();
            if (size > this.maximumSize) {
                int removals = size - this.maximumSize;
                Iterator<SSLSession> i = this.sessions.values().iterator();
                do {
                    SSLSession session = (SSLSession) i.next();
                    i.remove();
                    sessionRemoved(session);
                    removals--;
                } while (removals > 0);
            }
        }
    }

    public void setSessionTimeout(int seconds) throws IllegalArgumentException {
        if (seconds < 0) {
            throw new IllegalArgumentException("seconds < 0");
        }
        this.timeout = seconds;
        synchronized (this.sessions) {
            Iterator<SSLSession> i = this.sessions.values().iterator();
            while (i.hasNext()) {
                SSLSession session = (SSLSession) i.next();
                if (!session.isValid()) {
                    i.remove();
                    sessionRemoved(session);
                }
            }
        }
    }

    public final void setSessionCacheSize(int size) throws IllegalArgumentException {
        if (size < 0) {
            throw new IllegalArgumentException("size < 0");
        }
        int oldMaximum = this.maximumSize;
        this.maximumSize = size;
        if (size < oldMaximum) {
            trimToSize();
        }
    }

    byte[] toBytes(SSLSession session) {
        if (!(session instanceof OpenSSLSessionImpl)) {
            return null;
        }
        OpenSSLSessionImpl sslSession = (OpenSSLSessionImpl) session;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeInt(OPEN_SSL);
            byte[] data = sslSession.getEncoded();
            daos.writeInt(data.length);
            daos.write(data);
            Certificate[] certs = session.getPeerCertificates();
            daos.writeInt(certs.length);
            int length = certs.length;
            for (int i = 0; i < length; i += OPEN_SSL) {
                data = certs[i].getEncoded();
                daos.writeInt(data.length);
                daos.write(data);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println("Failed to convert saved SSL Session: " + e.getMessage());
            return null;
        } catch (CertificateEncodingException e2) {
            log(e2);
            return null;
        }
    }

    OpenSSLSessionImpl toSession(byte[] data, String host, int port) {
        DataInputStream dais = new DataInputStream(new ByteArrayInputStream(data));
        try {
            int type = dais.readInt();
            if (type != OPEN_SSL) {
                log(new AssertionError("Unexpected type ID: " + type));
                return null;
            }
            byte[] sessionData = new byte[dais.readInt()];
            dais.readFully(sessionData);
            int count = dais.readInt();
            X509Certificate[] certs = new X509Certificate[count];
            for (int i = 0; i < count; i += OPEN_SSL) {
                byte[] certData = new byte[dais.readInt()];
                dais.readFully(certData);
                certs[i] = OpenSSLX509Certificate.fromX509Der(certData);
            }
            return new OpenSSLSessionImpl(sessionData, host, port, certs, this);
        } catch (IOException e) {
            log(e);
            return null;
        }
    }

    protected SSLSession wrapSSLSessionIfNeeded(SSLSession session) {
        if (session instanceof OpenSSLSessionImpl) {
            return Platform.wrapSSLSession((OpenSSLSessionImpl) session);
        }
        return session;
    }

    public SSLSession getSession(byte[] sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("sessionId == null");
        }
        ByteArray key = new ByteArray(sessionId);
        synchronized (this.sessions) {
            SSLSession session = (SSLSession) this.sessions.get(key);
        }
        if (session == null || !session.isValid()) {
            return null;
        }
        if (session instanceof OpenSSLSessionImpl) {
            return Platform.wrapSSLSession((OpenSSLSessionImpl) session);
        }
        return session;
    }

    void putSession(SSLSession session) {
        byte[] id = session.getId();
        if (id.length != 0) {
            ByteArray key = new ByteArray(id);
            synchronized (this.sessions) {
                this.sessions.put(key, session);
            }
        }
    }

    static void log(Throwable t) {
        new Exception("Error converting session", t).printStackTrace();
    }

    protected void finalize() throws Throwable {
        try {
            NativeCrypto.SSL_CTX_free(this.sslCtxNativePointer);
        } finally {
            super.finalize();
        }
    }
}
