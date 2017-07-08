package sun.security.ssl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import sun.security.validator.Validator;

public abstract class SunJSSE extends Provider {
    static Provider cryptoProvider = null;
    private static Boolean fips = null;
    private static String fipsInfo = null;
    private static String info = null;
    private static final long serialVersionUID = 3231825739635378733L;

    /* renamed from: sun.security.ssl.SunJSSE.1 */
    class AnonymousClass1 implements PrivilegedAction<Object> {
        final /* synthetic */ boolean val$isfips;

        AnonymousClass1(boolean val$isfips) {
            this.val$isfips = val$isfips;
        }

        public Object run() {
            SunJSSE.this.doRegister(this.val$isfips);
            return null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SunJSSE.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SunJSSE.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SunJSSE.<clinit>():void");
    }

    protected static synchronized boolean isFIPS() {
        boolean booleanValue;
        synchronized (SunJSSE.class) {
            if (fips == null) {
                fips = Boolean.valueOf(false);
            }
            booleanValue = fips.booleanValue();
        }
        return booleanValue;
    }

    private static synchronized void ensureFIPS(Provider p) {
        synchronized (SunJSSE.class) {
            if (fips == null) {
                fips = Boolean.valueOf(true);
                cryptoProvider = p;
            } else if (!fips.booleanValue()) {
                throw new ProviderException("SunJSSE already initialized in non-FIPS mode");
            } else if (cryptoProvider != p) {
                throw new ProviderException("SunJSSE already initialized with FIPS crypto provider " + cryptoProvider);
            }
        }
    }

    protected SunJSSE() {
        super("SunJSSE", 1.7d, info);
        subclassCheck();
        if (Boolean.TRUE.equals(fips)) {
            throw new ProviderException("SunJSSE is already initialized in FIPS mode");
        }
        registerAlgorithms(false);
    }

    protected SunJSSE(Provider cryptoProvider) {
        this((Provider) checkNull(cryptoProvider), cryptoProvider.getName());
    }

    protected SunJSSE(String cryptoProvider) {
        this(null, (String) checkNull(cryptoProvider));
    }

    private static <T> T checkNull(T t) {
        if (t != null) {
            return t;
        }
        throw new ProviderException("cryptoProvider must not be null");
    }

    private SunJSSE(Provider cryptoProvider, String providerName) {
        super("SunJSSE", 1.6d, fipsInfo + providerName + ")");
        subclassCheck();
        if (cryptoProvider == null) {
            cryptoProvider = Security.getProvider(providerName);
            if (cryptoProvider == null) {
                throw new ProviderException("Crypto provider not installed: " + providerName);
            }
        }
        ensureFIPS(cryptoProvider);
        registerAlgorithms(true);
    }

    private void registerAlgorithms(boolean isfips) {
        AccessController.doPrivileged(new AnonymousClass1(isfips));
    }

    private void doRegister(boolean isfips) {
        if (!isfips) {
            put("KeyFactory.RSA", "sun.security.rsa.RSAKeyFactory");
            put("Alg.Alias.KeyFactory.1.2.840.113549.1.1", "RSA");
            put("Alg.Alias.KeyFactory.OID.1.2.840.113549.1.1", "RSA");
            put("KeyPairGenerator.RSA", "sun.security.rsa.RSAKeyPairGenerator");
            put("Alg.Alias.KeyPairGenerator.1.2.840.113549.1.1", "RSA");
            put("Alg.Alias.KeyPairGenerator.OID.1.2.840.113549.1.1", "RSA");
            put("Signature.MD2withRSA", "sun.security.rsa.RSASignature$MD2withRSA");
            put("Alg.Alias.Signature.1.2.840.113549.1.1.2", "MD2withRSA");
            put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.2", "MD2withRSA");
            put("Signature.MD5withRSA", "sun.security.rsa.RSASignature$MD5withRSA");
            put("Alg.Alias.Signature.1.2.840.113549.1.1.4", "MD5withRSA");
            put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.4", "MD5withRSA");
            put("Signature.SHA1withRSA", "sun.security.rsa.RSASignature$SHA1withRSA");
            put("Alg.Alias.Signature.1.2.840.113549.1.1.5", "SHA1withRSA");
            put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.5", "SHA1withRSA");
            put("Alg.Alias.Signature.1.3.14.3.2.29", "SHA1withRSA");
            put("Alg.Alias.Signature.OID.1.3.14.3.2.29", "SHA1withRSA");
        }
        put("Signature.MD5andSHA1withRSA", "sun.security.ssl.RSASignature");
        put("KeyManagerFactory.SunX509", "sun.security.ssl.KeyManagerFactoryImpl$SunX509");
        put("KeyManagerFactory.NewSunX509", "sun.security.ssl.KeyManagerFactoryImpl$X509");
        put("Alg.Alias.KeyManagerFactory.PKIX", "NewSunX509");
        put("TrustManagerFactory.SunX509", "sun.security.ssl.TrustManagerFactoryImpl$SimpleFactory");
        put("TrustManagerFactory.PKIX", "sun.security.ssl.TrustManagerFactoryImpl$PKIXFactory");
        put("Alg.Alias.TrustManagerFactory.SunPKIX", Validator.TYPE_PKIX);
        put("Alg.Alias.TrustManagerFactory.X509", Validator.TYPE_PKIX);
        put("Alg.Alias.TrustManagerFactory.X.509", Validator.TYPE_PKIX);
        put("SSLContext.TLSv1", "sun.security.ssl.SSLContextImpl$TLS10Context");
        put("Alg.Alias.SSLContext.TLS", "TLSv1");
        if (!isfips) {
            put("Alg.Alias.SSLContext.SSL", "TLSv1");
            put("Alg.Alias.SSLContext.SSLv3", "TLSv1");
        }
        put("SSLContext.TLSv1.1", "sun.security.ssl.SSLContextImpl$TLS11Context");
        put("SSLContext.TLSv1.2", "sun.security.ssl.SSLContextImpl$TLS12Context");
        put("SSLContext.Default", "sun.security.ssl.SSLContextImpl$DefaultSSLContext");
        put("KeyStore.PKCS12", "sun.security.pkcs12.PKCS12KeyStore");
    }

    private void subclassCheck() {
        if (getClass() != com.sun.net.ssl.internal.ssl.Provider.class) {
            throw new AssertionError("Illegal subclass: " + getClass());
        }
    }

    protected final void finalize() throws Throwable {
        super.finalize();
    }
}
