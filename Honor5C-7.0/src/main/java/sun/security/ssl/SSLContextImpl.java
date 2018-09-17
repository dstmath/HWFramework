package sun.security.ssl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.AlgorithmConstraints;
import java.security.CryptoPrimitive;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.PrivilegedExceptionAction;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import sun.util.logging.PlatformLogger;

public abstract class SSLContextImpl extends SSLContextSpi {
    private static final Debug debug = null;
    private final SSLSessionContextImpl clientCache;
    private AlgorithmConstraints defaultAlgorithmConstraints;
    private CipherSuiteList defaultClientCipherSuiteList;
    private ProtocolList defaultClientProtocolList;
    private CipherSuiteList defaultServerCipherSuiteList;
    private ProtocolList defaultServerProtocolList;
    private final EphemeralKeyManager ephemeralKeyManager;
    private boolean isInitialized;
    private X509ExtendedKeyManager keyManager;
    private SecureRandom secureRandom;
    private final SSLSessionContextImpl serverCache;
    private CipherSuiteList supportedCipherSuiteList;
    private ProtocolList supportedProtocolList;
    private X509TrustManager trustManager;

    private static class ConservativeSSLContext extends SSLContextImpl {
        private static SSLParameters defaultClientSSLParams;
        private static SSLParameters defaultServerSSLParams;
        private static SSLParameters supportedSSLParams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SSLContextImpl.ConservativeSSLContext.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SSLContextImpl.ConservativeSSLContext.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SSLContextImpl.ConservativeSSLContext.<clinit>():void");
        }

        private ConservativeSSLContext() {
        }

        SSLParameters getDefaultServerSSLParams() {
            return defaultServerSSLParams;
        }

        SSLParameters getDefaultClientSSLParams() {
            return defaultClientSSLParams;
        }

        SSLParameters getSupportedSSLParams() {
            return supportedSSLParams;
        }
    }

    public static final class DefaultSSLContext extends ConservativeSSLContext {
        private static final String NONE = "NONE";
        private static final String P11KEYSTORE = "PKCS11";
        private static volatile SSLContextImpl defaultImpl;
        private static KeyManager[] defaultKeyManagers;
        private static TrustManager[] defaultTrustManagers;

        /* renamed from: sun.security.ssl.SSLContextImpl.DefaultSSLContext.1 */
        static class AnonymousClass1 implements PrivilegedExceptionAction<Object> {
            final /* synthetic */ Map val$props;

            AnonymousClass1(Map val$props) {
                this.val$props = val$props;
            }

            public Object run() throws Exception {
                this.val$props.put("keyStore", System.getProperty("javax.net.ssl.keyStore", ""));
                this.val$props.put("keyStoreType", System.getProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType()));
                this.val$props.put("keyStoreProvider", System.getProperty("javax.net.ssl.keyStoreProvider", ""));
                this.val$props.put("keyStorePasswd", System.getProperty("javax.net.ssl.keyStorePassword", ""));
                return null;
            }
        }

        /* renamed from: sun.security.ssl.SSLContextImpl.DefaultSSLContext.2 */
        static class AnonymousClass2 implements PrivilegedExceptionAction<FileInputStream> {
            final /* synthetic */ String val$defaultKeyStore;

            AnonymousClass2(String val$defaultKeyStore) {
                this.val$defaultKeyStore = val$defaultKeyStore;
            }

            public /* bridge */ /* synthetic */ Object m107run() throws Exception {
                return run();
            }

            public FileInputStream run() throws Exception {
                return new FileInputStream(this.val$defaultKeyStore);
            }
        }

        public DefaultSSLContext() throws Exception {
            super();
            try {
                super.engineInit(getDefaultKeyManager(), getDefaultTrustManager(), null);
                if (defaultImpl == null) {
                    defaultImpl = this;
                }
            } catch (Object e) {
                if (SSLContextImpl.debug != null && Debug.isOn("defaultctx")) {
                    System.out.println("default context init failed: " + e);
                }
                throw e;
            }
        }

        protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr) throws KeyManagementException {
            throw new KeyManagementException("Default SSLContext is initialized automatically");
        }

        static synchronized SSLContextImpl getDefaultImpl() throws Exception {
            SSLContextImpl sSLContextImpl;
            synchronized (DefaultSSLContext.class) {
                if (defaultImpl == null) {
                    DefaultSSLContext defaultSSLContext = new DefaultSSLContext();
                }
                sSLContextImpl = defaultImpl;
            }
            return sSLContextImpl;
        }

        private static synchronized TrustManager[] getDefaultTrustManager() throws Exception {
            synchronized (DefaultSSLContext.class) {
                if (defaultTrustManagers != null) {
                    TrustManager[] trustManagerArr = defaultTrustManagers;
                    return trustManagerArr;
                }
                KeyStore ks = TrustManagerFactoryImpl.getCacertsKeyStore("defaultctx");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);
                defaultTrustManagers = tmf.getTrustManagers();
                trustManagerArr = defaultTrustManagers;
                return trustManagerArr;
            }
        }

        private static synchronized KeyManager[] getDefaultKeyManager() throws Exception {
            synchronized (DefaultSSLContext.class) {
                if (defaultKeyManagers != null) {
                    KeyManager[] keyManagerArr = defaultKeyManagers;
                    return keyManagerArr;
                }
                Map<String, String> props = new HashMap();
                AccessController.doPrivileged(new AnonymousClass1(props));
                String defaultKeyStore = (String) props.get("keyStore");
                String defaultKeyStoreType = (String) props.get("keyStoreType");
                String defaultKeyStoreProvider = (String) props.get("keyStoreProvider");
                if (SSLContextImpl.debug != null && Debug.isOn("defaultctx")) {
                    System.out.println("keyStore is : " + defaultKeyStore);
                    System.out.println("keyStore type is : " + defaultKeyStoreType);
                    System.out.println("keyStore provider is : " + defaultKeyStoreProvider);
                }
                if (!P11KEYSTORE.equals(defaultKeyStoreType) || NONE.equals(defaultKeyStore)) {
                    InputStream fs = null;
                    if (!(defaultKeyStore.length() == 0 || NONE.equals(defaultKeyStore))) {
                        FileInputStream fs2 = (FileInputStream) AccessController.doPrivileged(new AnonymousClass2(defaultKeyStore));
                    }
                    String defaultKeyStorePassword = (String) props.get("keyStorePasswd");
                    char[] passwd = null;
                    if (defaultKeyStorePassword.length() != 0) {
                        passwd = defaultKeyStorePassword.toCharArray();
                    }
                    KeyStore ks = null;
                    if (defaultKeyStoreType.length() != 0) {
                        if (SSLContextImpl.debug != null && Debug.isOn("defaultctx")) {
                            System.out.println("init keystore");
                        }
                        if (defaultKeyStoreProvider.length() == 0) {
                            ks = KeyStore.getInstance(defaultKeyStoreType);
                        } else {
                            ks = KeyStore.getInstance(defaultKeyStoreType, defaultKeyStoreProvider);
                        }
                        ks.load(fs, passwd);
                    }
                    if (fs != null) {
                        fs.close();
                    }
                    if (SSLContextImpl.debug != null && Debug.isOn("defaultctx")) {
                        System.out.println("init keymanager of type " + KeyManagerFactory.getDefaultAlgorithm());
                    }
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    if (P11KEYSTORE.equals(defaultKeyStoreType)) {
                        kmf.init(ks, null);
                    } else {
                        kmf.init(ks, passwd);
                    }
                    defaultKeyManagers = kmf.getKeyManagers();
                    keyManagerArr = defaultKeyManagers;
                    return keyManagerArr;
                }
                throw new IllegalArgumentException("if keyStoreType is PKCS11, then keyStore must be NONE");
            }
        }
    }

    public static final class TLS10Context extends ConservativeSSLContext {
        public TLS10Context() {
            super();
        }
    }

    public static final class TLS11Context extends SSLContextImpl {
        private static SSLParameters defaultClientSSLParams;
        private static SSLParameters defaultServerSSLParams;
        private static SSLParameters supportedSSLParams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SSLContextImpl.TLS11Context.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SSLContextImpl.TLS11Context.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SSLContextImpl.TLS11Context.<clinit>():void");
        }

        public TLS11Context() {
        }

        SSLParameters getDefaultServerSSLParams() {
            return defaultServerSSLParams;
        }

        SSLParameters getDefaultClientSSLParams() {
            return defaultClientSSLParams;
        }

        SSLParameters getSupportedSSLParams() {
            return supportedSSLParams;
        }
    }

    public static final class TLS12Context extends SSLContextImpl {
        private static SSLParameters defaultClientSSLParams;
        private static SSLParameters defaultServerSSLParams;
        private static SSLParameters supportedSSLParams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SSLContextImpl.TLS12Context.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SSLContextImpl.TLS12Context.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SSLContextImpl.TLS12Context.<clinit>():void");
        }

        public TLS12Context() {
        }

        SSLParameters getDefaultServerSSLParams() {
            return defaultServerSSLParams;
        }

        SSLParameters getDefaultClientSSLParams() {
            return defaultClientSSLParams;
        }

        SSLParameters getSupportedSSLParams() {
            return supportedSSLParams;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SSLContextImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SSLContextImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SSLContextImpl.<clinit>():void");
    }

    abstract SSLParameters getDefaultClientSSLParams();

    abstract SSLParameters getDefaultServerSSLParams();

    abstract SSLParameters getSupportedSSLParams();

    SSLContextImpl() {
        this.defaultAlgorithmConstraints = new SSLAlgorithmConstraints(null);
        this.ephemeralKeyManager = new EphemeralKeyManager();
        this.clientCache = new SSLSessionContextImpl();
        this.serverCache = new SSLSessionContextImpl();
    }

    protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr) throws KeyManagementException {
        this.isInitialized = false;
        this.keyManager = chooseKeyManager(km);
        if (tm == null) {
            try {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init((KeyStore) null);
                tm = tmf.getTrustManagers();
            } catch (Exception e) {
            }
        }
        this.trustManager = chooseTrustManager(tm);
        if (sr == null) {
            this.secureRandom = JsseJce.getSecureRandom();
        } else if (!SunJSSE.isFIPS() || sr.getProvider() == SunJSSE.cryptoProvider) {
            this.secureRandom = sr;
        } else {
            throw new KeyManagementException("FIPS mode: SecureRandom must be from provider " + SunJSSE.cryptoProvider.getName());
        }
        if (debug != null && Debug.isOn("sslctx")) {
            System.out.println("trigger seeding of SecureRandom");
        }
        this.secureRandom.nextInt();
        if (debug != null && Debug.isOn("sslctx")) {
            System.out.println("done seeding SecureRandom");
        }
        this.isInitialized = true;
    }

    private X509TrustManager chooseTrustManager(TrustManager[] tm) throws KeyManagementException {
        int i = 0;
        while (tm != null && i < tm.length) {
            if (!(tm[i] instanceof X509TrustManager)) {
                i++;
            } else if (SunJSSE.isFIPS() && !(tm[i] instanceof X509TrustManagerImpl)) {
                throw new KeyManagementException("FIPS mode: only SunJSSE TrustManagers may be used");
            } else if (tm[i] instanceof X509ExtendedTrustManager) {
                return (X509TrustManager) tm[i];
            } else {
                return new AbstractTrustManagerWrapper((X509TrustManager) tm[i]);
            }
        }
        return DummyX509TrustManager.INSTANCE;
    }

    private X509ExtendedKeyManager chooseKeyManager(KeyManager[] kms) throws KeyManagementException {
        int i = 0;
        while (kms != null && i < kms.length) {
            KeyManager km = kms[i];
            if (!(km instanceof X509KeyManager)) {
                i++;
            } else if (SunJSSE.isFIPS()) {
                if ((km instanceof X509KeyManagerImpl) || (km instanceof SunX509KeyManagerImpl)) {
                    return (X509ExtendedKeyManager) km;
                }
                throw new KeyManagementException("FIPS mode: only SunJSSE KeyManagers may be used");
            } else if (km instanceof X509ExtendedKeyManager) {
                return (X509ExtendedKeyManager) km;
            } else {
                if (debug != null && Debug.isOn("sslctx")) {
                    System.out.println("X509KeyManager passed to SSLContext.init():  need an X509ExtendedKeyManager for SSLEngine use");
                }
                return new AbstractKeyManagerWrapper((X509KeyManager) km);
            }
        }
        return DummyX509KeyManager.INSTANCE;
    }

    protected SSLSocketFactory engineGetSocketFactory() {
        if (this.isInitialized) {
            return new SSLSocketFactoryImpl(this);
        }
        throw new IllegalStateException("SSLContextImpl is not initialized");
    }

    protected SSLServerSocketFactory engineGetServerSocketFactory() {
        if (this.isInitialized) {
            return new SSLServerSocketFactoryImpl(this);
        }
        throw new IllegalStateException("SSLContext is not initialized");
    }

    protected SSLEngine engineCreateSSLEngine() {
        if (this.isInitialized) {
            return new SSLEngineImpl(this);
        }
        throw new IllegalStateException("SSLContextImpl is not initialized");
    }

    protected SSLEngine engineCreateSSLEngine(String host, int port) {
        if (this.isInitialized) {
            return new SSLEngineImpl(this, host, port);
        }
        throw new IllegalStateException("SSLContextImpl is not initialized");
    }

    protected SSLSessionContext engineGetClientSessionContext() {
        return this.clientCache;
    }

    protected SSLSessionContext engineGetServerSessionContext() {
        return this.serverCache;
    }

    SecureRandom getSecureRandom() {
        return this.secureRandom;
    }

    X509ExtendedKeyManager getX509KeyManager() {
        return this.keyManager;
    }

    X509TrustManager getX509TrustManager() {
        return this.trustManager;
    }

    EphemeralKeyManager getEphemeralKeyManager() {
        return this.ephemeralKeyManager;
    }

    ProtocolList getSuportedProtocolList() {
        if (this.supportedProtocolList == null) {
            this.supportedProtocolList = new ProtocolList(getSupportedSSLParams().getProtocols());
        }
        return this.supportedProtocolList;
    }

    ProtocolList getDefaultProtocolList(boolean roleIsServer) {
        if (roleIsServer) {
            if (this.defaultServerProtocolList == null) {
                this.defaultServerProtocolList = new ProtocolList(getDefaultServerSSLParams().getProtocols());
            }
            return this.defaultServerProtocolList;
        }
        if (this.defaultClientProtocolList == null) {
            this.defaultClientProtocolList = new ProtocolList(getDefaultClientSSLParams().getProtocols());
        }
        return this.defaultClientProtocolList;
    }

    CipherSuiteList getSupportedCipherSuiteList() {
        CipherSuiteList cipherSuiteList;
        synchronized (this) {
            clearAvailableCache();
            if (this.supportedCipherSuiteList == null) {
                this.supportedCipherSuiteList = getApplicableCipherSuiteList(getSuportedProtocolList(), false);
            }
            cipherSuiteList = this.supportedCipherSuiteList;
        }
        return cipherSuiteList;
    }

    CipherSuiteList getDefaultCipherSuiteList(boolean roleIsServer) {
        synchronized (this) {
            clearAvailableCache();
            if (roleIsServer) {
                if (this.defaultServerCipherSuiteList == null) {
                    this.defaultServerCipherSuiteList = getApplicableCipherSuiteList(getDefaultProtocolList(true), true);
                }
                CipherSuiteList cipherSuiteList = this.defaultServerCipherSuiteList;
                return cipherSuiteList;
            }
            if (this.defaultClientCipherSuiteList == null) {
                this.defaultClientCipherSuiteList = getApplicableCipherSuiteList(getDefaultProtocolList(false), true);
            }
            cipherSuiteList = this.defaultClientCipherSuiteList;
            return cipherSuiteList;
        }
    }

    boolean isDefaultProtocolList(ProtocolList protocols) {
        if (protocols == this.defaultServerProtocolList || protocols == this.defaultClientProtocolList) {
            return true;
        }
        return false;
    }

    private CipherSuiteList getApplicableCipherSuiteList(ProtocolList protocols, boolean onlyEnabled) {
        int minPriority = 1;
        if (onlyEnabled) {
            minPriority = PlatformLogger.FINEST;
        }
        Collection<CipherSuite> allowedCipherSuites = CipherSuite.allowedCipherSuites();
        Collection suites = new TreeSet();
        if (!(protocols.collection().isEmpty() || protocols.min.v == ProtocolVersion.NONE.v)) {
            for (Object suite : allowedCipherSuites) {
                if (suite.allowed && suite.priority >= minPriority) {
                    if (!suite.isAvailable() || suite.obsoleted <= protocols.min.v || suite.supported > protocols.max.v) {
                        if (debug != null && Debug.isOn("sslctx") && Debug.isOn("verbose")) {
                            if (suite.obsoleted <= protocols.min.v) {
                                System.out.println("Ignoring obsoleted cipher suite: " + suite);
                            } else if (suite.supported > protocols.max.v) {
                                System.out.println("Ignoring unsupported cipher suite: " + suite);
                            } else {
                                System.out.println("Ignoring unavailable cipher suite: " + suite);
                            }
                        }
                    } else if (this.defaultAlgorithmConstraints.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), suite.name, null)) {
                        suites.add(suite);
                    }
                }
            }
        }
        return new CipherSuiteList(suites);
    }

    private void clearAvailableCache() {
        this.supportedCipherSuiteList = null;
        this.defaultServerCipherSuiteList = null;
        this.defaultClientCipherSuiteList = null;
        BulkCipher.clearAvailableCache();
        JsseJce.clearEcAvailable();
    }
}
