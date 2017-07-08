package sun.security.ssl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.crypto.SecretKey;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLPermission;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;
import javax.net.ssl.SSLSessionContext;
import javax.security.cert.CertificateException;

final class SSLSessionImpl extends ExtendedSSLSession {
    private static final byte compression_null = (byte) 0;
    private static volatile int counter;
    private static final Debug debug = null;
    private static boolean defaultRejoinable;
    static final SSLSessionImpl nullSession = null;
    private boolean acceptLargeFragments;
    private CipherSuite cipherSuite;
    private byte compressionMethod;
    private SSLSessionContextImpl context;
    private final long creationTime;
    private final String host;
    private boolean invalidated;
    private long lastUsedTime;
    private X509Certificate[] localCerts;
    private Principal localPrincipal;
    private PrivateKey localPrivateKey;
    private String[] localSupportedSignAlgs;
    private SecretKey masterSecret;
    private X509Certificate[] peerCerts;
    private Principal peerPrincipal;
    private String[] peerSupportedSignAlgs;
    private final int port;
    private final ProtocolVersion protocolVersion;
    private int sessionCount;
    private final SessionId sessionId;
    private Hashtable<SecureKey, Object> table;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SSLSessionImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SSLSessionImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SSLSessionImpl.<clinit>():void");
    }

    private SSLSessionImpl() {
        this(ProtocolVersion.NONE, CipherSuite.C_NULL, null, new SessionId(false, null), null, -1);
    }

    SSLSessionImpl(ProtocolVersion protocolVersion, CipherSuite cipherSuite, Collection<SignatureAndHashAlgorithm> algorithms, SecureRandom generator, String host, int port) {
        this(protocolVersion, cipherSuite, (Collection) algorithms, new SessionId(defaultRejoinable, generator), host, port);
    }

    SSLSessionImpl(ProtocolVersion protocolVersion, CipherSuite cipherSuite, Collection<SignatureAndHashAlgorithm> algorithms, SessionId id, String host, int port) {
        this.creationTime = System.currentTimeMillis();
        this.lastUsedTime = 0;
        this.table = new Hashtable();
        this.acceptLargeFragments = Debug.getBooleanProperty("jsse.SSLEngine.acceptLargeFragments", false);
        this.protocolVersion = protocolVersion;
        this.sessionId = id;
        this.peerCerts = null;
        this.compressionMethod = (byte) 0;
        this.cipherSuite = cipherSuite;
        this.masterSecret = null;
        this.host = host;
        this.port = port;
        int i = counter + 1;
        counter = i;
        this.sessionCount = i;
        this.localSupportedSignAlgs = SignatureAndHashAlgorithm.getAlgorithmNames(algorithms);
        if (debug != null && Debug.isOn("session")) {
            System.out.println("%% Initialized:  " + this);
        }
    }

    void setMasterSecret(SecretKey secret) {
        if (this.masterSecret == null) {
            this.masterSecret = secret;
            return;
        }
        throw new RuntimeException("setMasterSecret() error");
    }

    SecretKey getMasterSecret() {
        return this.masterSecret;
    }

    void setPeerCertificates(X509Certificate[] peer) {
        if (this.peerCerts == null) {
            this.peerCerts = peer;
        }
    }

    void setLocalCertificates(X509Certificate[] local) {
        this.localCerts = local;
    }

    void setLocalPrivateKey(PrivateKey privateKey) {
        this.localPrivateKey = privateKey;
    }

    void setPeerSupportedSignatureAlgorithms(Collection<SignatureAndHashAlgorithm> algorithms) {
        this.peerSupportedSignAlgs = SignatureAndHashAlgorithm.getAlgorithmNames(algorithms);
    }

    void setPeerPrincipal(Principal principal) {
        if (this.peerPrincipal == null) {
            this.peerPrincipal = principal;
        }
    }

    void setLocalPrincipal(Principal principal) {
        this.localPrincipal = principal;
    }

    boolean isRejoinable() {
        if (this.sessionId == null || this.sessionId.length() == 0 || this.invalidated) {
            return false;
        }
        return isLocalAuthenticationValid();
    }

    public synchronized boolean isValid() {
        return isRejoinable();
    }

    boolean isLocalAuthenticationValid() {
        if (this.localPrivateKey != null) {
            try {
                this.localPrivateKey.getAlgorithm();
            } catch (Exception e) {
                invalidate();
                return false;
            }
        }
        return true;
    }

    public byte[] getId() {
        return this.sessionId.getId();
    }

    public SSLSessionContext getSessionContext() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SSLPermission("getSSLSessionContext"));
        }
        return this.context;
    }

    SessionId getSessionId() {
        return this.sessionId;
    }

    CipherSuite getSuite() {
        return this.cipherSuite;
    }

    void setSuite(CipherSuite suite) {
        this.cipherSuite = suite;
        if (debug != null && Debug.isOn("session")) {
            System.out.println("%% Negotiating:  " + this);
        }
    }

    public String getCipherSuite() {
        return getSuite().name;
    }

    ProtocolVersion getProtocolVersion() {
        return this.protocolVersion;
    }

    public String getProtocol() {
        return getProtocolVersion().name;
    }

    byte getCompression() {
        return this.compressionMethod;
    }

    public int hashCode() {
        return this.sessionId.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SSLSessionImpl)) {
            return false;
        }
        SSLSessionImpl sess = (SSLSessionImpl) obj;
        if (this.sessionId != null) {
            z = this.sessionId.equals(sess.getSessionId());
        }
        return z;
    }

    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        if (this.cipherSuite.keyExchange == KeyExchange.K_KRB5 || this.cipherSuite.keyExchange == KeyExchange.K_KRB5_EXPORT) {
            throw new SSLPeerUnverifiedException("no certificates expected for Kerberos cipher suites");
        } else if (this.peerCerts != null) {
            return (Certificate[]) this.peerCerts.clone();
        } else {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        }
    }

    public Certificate[] getLocalCertificates() {
        if (this.localCerts == null) {
            return null;
        }
        return (Certificate[]) this.localCerts.clone();
    }

    public javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        if (this.cipherSuite.keyExchange == KeyExchange.K_KRB5 || this.cipherSuite.keyExchange == KeyExchange.K_KRB5_EXPORT) {
            throw new SSLPeerUnverifiedException("no certificates expected for Kerberos cipher suites");
        } else if (this.peerCerts == null) {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        } else {
            javax.security.cert.X509Certificate[] certs = new javax.security.cert.X509Certificate[this.peerCerts.length];
            int i = 0;
            while (i < this.peerCerts.length) {
                try {
                    certs[i] = javax.security.cert.X509Certificate.getInstance(this.peerCerts[i].getEncoded());
                    i++;
                } catch (CertificateEncodingException e) {
                    throw new SSLPeerUnverifiedException(e.getMessage());
                } catch (CertificateException e2) {
                    throw new SSLPeerUnverifiedException(e2.getMessage());
                }
            }
            return certs;
        }
    }

    public X509Certificate[] getCertificateChain() throws SSLPeerUnverifiedException {
        if (this.cipherSuite.keyExchange == KeyExchange.K_KRB5 || this.cipherSuite.keyExchange == KeyExchange.K_KRB5_EXPORT) {
            throw new SSLPeerUnverifiedException("no certificates expected for Kerberos cipher suites");
        } else if (this.peerCerts != null) {
            return (X509Certificate[]) this.peerCerts.clone();
        } else {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        }
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        if (this.cipherSuite.keyExchange == KeyExchange.K_KRB5 || this.cipherSuite.keyExchange == KeyExchange.K_KRB5_EXPORT) {
            if (this.peerPrincipal != null) {
                return this.peerPrincipal;
            }
            throw new SSLPeerUnverifiedException("peer not authenticated");
        } else if (this.peerCerts != null) {
            return this.peerCerts[0].getSubjectX500Principal();
        } else {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        }
    }

    public Principal getLocalPrincipal() {
        Principal principal = null;
        if (this.cipherSuite.keyExchange == KeyExchange.K_KRB5 || this.cipherSuite.keyExchange == KeyExchange.K_KRB5_EXPORT) {
            if (this.localPrincipal != null) {
                principal = this.localPrincipal;
            }
            return principal;
        }
        if (this.localCerts != null) {
            principal = this.localCerts[0].getSubjectX500Principal();
        }
        return principal;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public long getLastAccessedTime() {
        return this.lastUsedTime != 0 ? this.lastUsedTime : this.creationTime;
    }

    void setLastAccessedTime(long time) {
        this.lastUsedTime = time;
    }

    public InetAddress getPeerAddress() {
        try {
            return InetAddress.getByName(this.host);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public String getPeerHost() {
        return this.host;
    }

    public int getPeerPort() {
        return this.port;
    }

    void setContext(SSLSessionContextImpl ctx) {
        if (this.context == null) {
            this.context = ctx;
        }
    }

    public synchronized void invalidate() {
        if (this != nullSession) {
            this.invalidated = true;
            if (debug != null && Debug.isOn("session")) {
                System.out.println("%% Invalidated:  " + this);
            }
            if (this.context != null) {
                this.context.remove(this.sessionId);
                this.context = null;
            }
        }
    }

    public void putValue(String key, Object value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("arguments can not be null");
        }
        Object oldValue = this.table.put(new SecureKey(key), value);
        if (oldValue instanceof SSLSessionBindingListener) {
            ((SSLSessionBindingListener) oldValue).valueUnbound(new SSLSessionBindingEvent(this, key));
        }
        if (value instanceof SSLSessionBindingListener) {
            ((SSLSessionBindingListener) value).valueBound(new SSLSessionBindingEvent(this, key));
        }
    }

    public Object getValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("argument can not be null");
        }
        return this.table.get(new SecureKey(key));
    }

    public void removeValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("argument can not be null");
        }
        Object value = this.table.remove(new SecureKey(key));
        if (value instanceof SSLSessionBindingListener) {
            ((SSLSessionBindingListener) value).valueUnbound(new SSLSessionBindingEvent(this, key));
        }
    }

    public String[] getValueNames() {
        Vector<Object> v = new Vector();
        Object securityCtx = SecureKey.getCurrentSecurityContext();
        Enumeration<SecureKey> e = this.table.keys();
        while (e.hasMoreElements()) {
            SecureKey key = (SecureKey) e.nextElement();
            if (securityCtx.equals(key.getSecurityContext())) {
                v.addElement(key.getAppKey());
            }
        }
        String[] names = new String[v.size()];
        v.copyInto(names);
        return names;
    }

    protected synchronized void expandBufferSizes() {
        this.acceptLargeFragments = true;
    }

    public synchronized int getPacketBufferSize() {
        return this.acceptLargeFragments ? Record.maxLargeRecordSize : Record.maxRecordSize;
    }

    public synchronized int getApplicationBufferSize() {
        return getPacketBufferSize() - 5;
    }

    public String[] getLocalSupportedSignatureAlgorithms() {
        if (this.localSupportedSignAlgs != null) {
            return (String[]) this.localSupportedSignAlgs.clone();
        }
        return new String[0];
    }

    public String[] getPeerSupportedSignatureAlgorithms() {
        if (this.peerSupportedSignAlgs != null) {
            return (String[]) this.peerSupportedSignAlgs.clone();
        }
        return new String[0];
    }

    public String toString() {
        return "[Session-" + this.sessionCount + ", " + getCipherSuite() + "]";
    }

    public void finalize() {
        String[] names = getValueNames();
        for (String removeValue : names) {
            removeValue(removeValue);
        }
    }
}
