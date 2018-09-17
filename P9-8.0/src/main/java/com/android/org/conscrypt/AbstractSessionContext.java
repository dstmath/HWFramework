package com.android.org.conscrypt;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;

abstract class AbstractSessionContext implements SSLSessionContext {
    private static final int DEFAULT_SESSION_TIMEOUT_SECONDS = 28800;
    static final int OPEN_SSL = 1;
    static final int OPEN_SSL_WITH_OCSP = 2;
    static final int OPEN_SSL_WITH_TLS_SCT = 3;
    volatile int maximumSize;
    private final Map<ByteArray, SSLSession> sessions = new LinkedHashMap<ByteArray, SSLSession>() {
        protected boolean removeEldestEntry(Entry<ByteArray, SSLSession> eldest) {
            boolean remove = AbstractSessionContext.this.maximumSize > 0 && size() > AbstractSessionContext.this.maximumSize;
            if (remove) {
                remove(eldest.getKey());
                AbstractSessionContext.this.sessionRemoved((SSLSession) eldest.getValue());
            }
            return false;
        }
    };
    final long sslCtxNativePointer = NativeCrypto.SSL_CTX_new();
    volatile int timeout = DEFAULT_SESSION_TIMEOUT_SECONDS;

    protected abstract void sessionRemoved(SSLSession sSLSession);

    AbstractSessionContext(int maximumSize) {
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
        final Iterator<SSLSession> i = sessionIterator();
        return new Enumeration<byte[]>() {
            private SSLSession next;

            public boolean hasMoreElements() {
                if (this.next != null) {
                    return true;
                }
                while (i.hasNext()) {
                    SSLSession session = (SSLSession) i.next();
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
        };
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

    public byte[] toBytes(SSLSession session) {
        if (!(session instanceof OpenSSLSessionImpl)) {
            return null;
        }
        OpenSSLSessionImpl sslSession = (OpenSSLSessionImpl) session;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeInt(3);
            byte[] data = sslSession.getEncoded();
            daos.writeInt(data.length);
            daos.write(data);
            Certificate[] certs = session.getPeerCertificates();
            daos.writeInt(certs.length);
            for (Certificate cert : certs) {
                data = cert.getEncoded();
                daos.writeInt(data.length);
                daos.write(data);
            }
            List<byte[]> ocspResponses = sslSession.getStatusResponses();
            daos.writeInt(ocspResponses.size());
            for (byte[] ocspResponse : ocspResponses) {
                daos.writeInt(ocspResponse.length);
                daos.write(ocspResponse);
            }
            byte[] tlsSctData = sslSession.getTlsSctData();
            if (tlsSctData != null) {
                daos.writeInt(tlsSctData.length);
                daos.write(tlsSctData);
            } else {
                daos.writeInt(0);
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

    private static void checkRemaining(ByteBuffer buf, int length) throws IOException {
        if (length < 0) {
            throw new IOException("Length is negative: " + length);
        } else if (length > buf.remaining()) {
            throw new IOException("Length of blob is longer than available: " + length + " > " + buf.remaining());
        }
    }

    public OpenSSLSessionImpl toSession(byte[] data, String host, int port) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        int count;
        int i;
        try {
            int type = buf.getInt();
            if (type == 1 || type == 2 || type == 3) {
                int length = buf.getInt();
                checkRemaining(buf, length);
                byte[] sessionData = new byte[length];
                buf.get(sessionData);
                count = buf.getInt();
                checkRemaining(buf, count);
                X509Certificate[] certs = new X509Certificate[count];
                i = 0;
                while (i < count) {
                    length = buf.getInt();
                    checkRemaining(buf, length);
                    byte[] certData = new byte[length];
                    buf.get(certData);
                    certs[i] = OpenSSLX509Certificate.fromX509Der(certData);
                    i++;
                }
                byte[] ocspData = null;
                if (type >= 2) {
                    int countOcspResponses = buf.getInt();
                    checkRemaining(buf, countOcspResponses);
                    if (countOcspResponses >= 1) {
                        int ocspLength = buf.getInt();
                        checkRemaining(buf, ocspLength);
                        ocspData = new byte[ocspLength];
                        buf.get(ocspData);
                        for (i = 1; i < countOcspResponses; i++) {
                            ocspLength = buf.getInt();
                            checkRemaining(buf, ocspLength);
                            buf.position(buf.position() + ocspLength);
                        }
                    }
                }
                byte[] tlsSctData = null;
                if (type == 3) {
                    int tlsSctDataLength = buf.getInt();
                    checkRemaining(buf, tlsSctDataLength);
                    if (tlsSctDataLength > 0) {
                        tlsSctData = new byte[tlsSctDataLength];
                        buf.get(tlsSctData);
                    }
                }
                if (buf.remaining() == 0) {
                    return new OpenSSLSessionImpl(sessionData, host, port, certs, ocspData, tlsSctData, this);
                }
                log(new AssertionError("Read entire session, but data still remains; rejecting"));
                return null;
            }
            throw new IOException("Unexpected type ID: " + type);
        } catch (Exception e) {
            throw new IOException("Can not read certificate " + i + "/" + count);
        } catch (IOException e2) {
            log(e2);
            return null;
        } catch (BufferUnderflowException e3) {
            log(e3);
            return null;
        }
    }

    protected SSLSession wrapSSLSessionIfNeeded(SSLSession session) {
        if (session instanceof AbstractOpenSSLSession) {
            return Platform.wrapSSLSession((AbstractOpenSSLSession) session);
        }
        return session;
    }

    public SSLSession getSession(byte[] sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("sessionId == null");
        }
        SSLSession session;
        ByteArray key = new ByteArray(sessionId);
        synchronized (this.sessions) {
            session = (SSLSession) this.sessions.get(key);
        }
        if (session == null || !session.isValid()) {
            return null;
        }
        return wrapSSLSessionIfNeeded(session);
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
        System.out.println("Error inflating SSL session: " + (t.getMessage() != null ? t.getMessage() : t.getClass().getName()));
    }

    protected void finalize() throws Throwable {
        try {
            NativeCrypto.SSL_CTX_free(this.sslCtxNativePointer);
        } finally {
            super.finalize();
        }
    }
}
