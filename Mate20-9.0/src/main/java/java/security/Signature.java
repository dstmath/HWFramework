package java.security;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import sun.security.jca.GetInstance;
import sun.security.jca.Providers;
import sun.security.jca.ServiceId;

public abstract class Signature extends SignatureSpi {
    private static final String RSA_CIPHER = "RSA/ECB/PKCS1Padding";
    private static final String RSA_SIGNATURE = "NONEwithRSA";
    protected static final int SIGN = 2;
    protected static final int UNINITIALIZED = 0;
    protected static final int VERIFY = 3;
    /* access modifiers changed from: private */
    public static final List<ServiceId> rsaIds = Arrays.asList(new ServiceId("Signature", RSA_SIGNATURE), new ServiceId("Cipher", RSA_CIPHER), new ServiceId("Cipher", "RSA/ECB"), new ServiceId("Cipher", "RSA//PKCS1Padding"), new ServiceId("Cipher", "RSA"));
    private static final Map<String, Boolean> signatureInfo = new ConcurrentHashMap();
    /* access modifiers changed from: private */
    public String algorithm;
    Provider provider;
    protected int state = 0;

    private static class CipherAdapter extends SignatureSpi {
        private final Cipher cipher;
        private ByteArrayOutputStream data;

        CipherAdapter(Cipher cipher2) {
            this.cipher = cipher2;
        }

        /* access modifiers changed from: protected */
        public void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
            this.cipher.init(2, (Key) publicKey);
            if (this.data == null) {
                this.data = new ByteArrayOutputStream(128);
            } else {
                this.data.reset();
            }
        }

        /* access modifiers changed from: protected */
        public void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
            this.cipher.init(1, (Key) privateKey);
            this.data = null;
        }

        /* access modifiers changed from: protected */
        public void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
            this.cipher.init(1, (Key) privateKey, random);
            this.data = null;
        }

        /* access modifiers changed from: protected */
        public void engineUpdate(byte b) throws SignatureException {
            engineUpdate(new byte[]{b}, 0, 1);
        }

        /* access modifiers changed from: protected */
        public void engineUpdate(byte[] b, int off, int len) throws SignatureException {
            if (this.data != null) {
                this.data.write(b, off, len);
                return;
            }
            byte[] out = this.cipher.update(b, off, len);
            if (out != null && out.length != 0) {
                throw new SignatureException("Cipher unexpectedly returned data");
            }
        }

        /* access modifiers changed from: protected */
        public byte[] engineSign() throws SignatureException {
            try {
                return this.cipher.doFinal();
            } catch (IllegalBlockSizeException e) {
                throw new SignatureException("doFinal() failed", e);
            } catch (BadPaddingException e2) {
                throw new SignatureException("doFinal() failed", e2);
            }
        }

        /* access modifiers changed from: protected */
        public boolean engineVerify(byte[] sigBytes) throws SignatureException {
            try {
                byte[] out = this.cipher.doFinal(sigBytes);
                byte[] dataBytes = this.data.toByteArray();
                this.data.reset();
                return MessageDigest.isEqual(out, dataBytes);
            } catch (BadPaddingException e) {
                return false;
            } catch (IllegalBlockSizeException e2) {
                throw new SignatureException("doFinal() failed", e2);
            }
        }

        /* access modifiers changed from: protected */
        public void engineSetParameter(String param, Object value) throws InvalidParameterException {
            throw new InvalidParameterException("Parameters not supported");
        }

        /* access modifiers changed from: protected */
        public Object engineGetParameter(String param) throws InvalidParameterException {
            throw new InvalidParameterException("Parameters not supported");
        }
    }

    private static class Delegate extends Signature {
        private static final int I_PRIV = 2;
        private static final int I_PRIV_SR = 3;
        private static final int I_PUB = 1;
        private static int warnCount = 10;
        private final Object lock;
        private SignatureSpi sigSpi;

        Delegate(SignatureSpi sigSpi2, String algorithm) {
            super(algorithm);
            this.sigSpi = sigSpi2;
            this.lock = null;
        }

        Delegate(String algorithm) {
            super(algorithm);
            this.lock = new Object();
        }

        public Object clone() throws CloneNotSupportedException {
            chooseFirstProvider();
            if (this.sigSpi instanceof Cloneable) {
                Signature that = new Delegate((SignatureSpi) this.sigSpi.clone(), this.algorithm);
                that.provider = this.provider;
                return that;
            }
            throw new CloneNotSupportedException();
        }

        private static SignatureSpi newInstance(Provider.Service s) throws NoSuchAlgorithmException {
            if (s.getType().equals("Cipher")) {
                try {
                    return new CipherAdapter(Cipher.getInstance(Signature.RSA_CIPHER, s.getProvider()));
                } catch (NoSuchPaddingException e) {
                    throw new NoSuchAlgorithmException((Throwable) e);
                }
            } else {
                Object o = s.newInstance(null);
                if (o instanceof SignatureSpi) {
                    return (SignatureSpi) o;
                }
                throw new NoSuchAlgorithmException("Not a SignatureSpi: " + o.getClass().getName());
            }
        }

        /* access modifiers changed from: package-private */
        public void chooseFirstProvider() {
            List<Provider.Service> list;
            if (this.sigSpi == null) {
                synchronized (this.lock) {
                    if (this.sigSpi == null) {
                        Exception lastException = null;
                        if (this.algorithm.equalsIgnoreCase(Signature.RSA_SIGNATURE)) {
                            list = GetInstance.getServices(Signature.rsaIds);
                        } else {
                            list = GetInstance.getServices("Signature", this.algorithm);
                        }
                        for (Provider.Service s : list) {
                            if (Signature.isSpi(s)) {
                                try {
                                    this.sigSpi = newInstance(s);
                                    this.provider = s.getProvider();
                                    return;
                                } catch (NoSuchAlgorithmException e) {
                                    lastException = e;
                                }
                            }
                        }
                        ProviderException e2 = new ProviderException("Could not construct SignatureSpi instance");
                        if (lastException != null) {
                            e2.initCause(lastException);
                        }
                        throw e2;
                    }
                }
            }
        }

        private void chooseProvider(int type, Key key, SecureRandom random) throws InvalidKeyException {
            List<Provider.Service> list;
            synchronized (this.lock) {
                if (this.sigSpi == null || key != null) {
                    InvalidKeyException lastException = null;
                    if (this.algorithm.equalsIgnoreCase(Signature.RSA_SIGNATURE)) {
                        list = GetInstance.getServices(Signature.rsaIds);
                    } else {
                        list = GetInstance.getServices("Signature", this.algorithm);
                    }
                    for (Provider.Service s : list) {
                        if (s.supportsParameter(key)) {
                            if (!Signature.isSpi(s)) {
                                continue;
                            } else {
                                try {
                                    SignatureSpi spi = newInstance(s);
                                    init(spi, type, key, random);
                                    this.provider = s.getProvider();
                                    this.sigSpi = spi;
                                    return;
                                } catch (Exception e) {
                                    if (lastException == null) {
                                        lastException = e;
                                    }
                                    if (lastException instanceof InvalidKeyException) {
                                        throw lastException;
                                    }
                                }
                            }
                        }
                    }
                    if (lastException instanceof InvalidKeyException) {
                        throw lastException;
                    } else if (!(lastException instanceof RuntimeException)) {
                        String k = key != null ? key.getClass().getName() : "(null)";
                        throw new InvalidKeyException("No installed provider supports this key: " + k, lastException);
                    } else {
                        throw ((RuntimeException) lastException);
                    }
                } else {
                    init(this.sigSpi, type, key, random);
                }
            }
        }

        private void init(SignatureSpi spi, int type, Key key, SecureRandom random) throws InvalidKeyException {
            switch (type) {
                case 1:
                    spi.engineInitVerify((PublicKey) key);
                    return;
                case 2:
                    spi.engineInitSign((PrivateKey) key);
                    return;
                case 3:
                    spi.engineInitSign((PrivateKey) key, random);
                    return;
                default:
                    throw new AssertionError((Object) "Internal error: " + type);
            }
        }

        /* access modifiers changed from: protected */
        public void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
            if (this.sigSpi == null || !(this.lock == null || publicKey == null)) {
                chooseProvider(1, publicKey, null);
            } else {
                this.sigSpi.engineInitVerify(publicKey);
            }
        }

        /* access modifiers changed from: protected */
        public void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
            if (this.sigSpi == null || !(this.lock == null || privateKey == null)) {
                chooseProvider(2, privateKey, null);
            } else {
                this.sigSpi.engineInitSign(privateKey);
            }
        }

        /* access modifiers changed from: protected */
        public void engineInitSign(PrivateKey privateKey, SecureRandom sr) throws InvalidKeyException {
            if (this.sigSpi == null || !(this.lock == null || privateKey == null)) {
                chooseProvider(3, privateKey, sr);
            } else {
                this.sigSpi.engineInitSign(privateKey, sr);
            }
        }

        /* access modifiers changed from: protected */
        public void engineUpdate(byte b) throws SignatureException {
            chooseFirstProvider();
            this.sigSpi.engineUpdate(b);
        }

        /* access modifiers changed from: protected */
        public void engineUpdate(byte[] b, int off, int len) throws SignatureException {
            chooseFirstProvider();
            this.sigSpi.engineUpdate(b, off, len);
        }

        /* access modifiers changed from: protected */
        public void engineUpdate(ByteBuffer data) {
            chooseFirstProvider();
            this.sigSpi.engineUpdate(data);
        }

        /* access modifiers changed from: protected */
        public byte[] engineSign() throws SignatureException {
            chooseFirstProvider();
            return this.sigSpi.engineSign();
        }

        /* access modifiers changed from: protected */
        public int engineSign(byte[] outbuf, int offset, int len) throws SignatureException {
            chooseFirstProvider();
            return this.sigSpi.engineSign(outbuf, offset, len);
        }

        /* access modifiers changed from: protected */
        public boolean engineVerify(byte[] sigBytes) throws SignatureException {
            chooseFirstProvider();
            return this.sigSpi.engineVerify(sigBytes);
        }

        /* access modifiers changed from: protected */
        public boolean engineVerify(byte[] sigBytes, int offset, int length) throws SignatureException {
            chooseFirstProvider();
            return this.sigSpi.engineVerify(sigBytes, offset, length);
        }

        /* access modifiers changed from: protected */
        public void engineSetParameter(String param, Object value) throws InvalidParameterException {
            chooseFirstProvider();
            this.sigSpi.engineSetParameter(param, value);
        }

        /* access modifiers changed from: protected */
        public void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            chooseFirstProvider();
            this.sigSpi.engineSetParameter(params);
        }

        /* access modifiers changed from: protected */
        public Object engineGetParameter(String param) throws InvalidParameterException {
            chooseFirstProvider();
            return this.sigSpi.engineGetParameter(param);
        }

        /* access modifiers changed from: protected */
        public AlgorithmParameters engineGetParameters() {
            chooseFirstProvider();
            return this.sigSpi.engineGetParameters();
        }

        public SignatureSpi getCurrentSpi() {
            SignatureSpi signatureSpi;
            if (this.lock == null) {
                return this.sigSpi;
            }
            synchronized (this.lock) {
                signatureSpi = this.sigSpi;
            }
            return signatureSpi;
        }
    }

    protected Signature(String algorithm2) {
        this.algorithm = algorithm2;
    }

    static {
        Boolean TRUE = Boolean.TRUE;
        signatureInfo.put("sun.security.provider.DSA$RawDSA", TRUE);
        signatureInfo.put("sun.security.provider.DSA$SHA1withDSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$MD2withRSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$MD5withRSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA1withRSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA256withRSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA384withRSA", TRUE);
        signatureInfo.put("sun.security.rsa.RSASignature$SHA512withRSA", TRUE);
        signatureInfo.put("com.sun.net.ssl.internal.ssl.RSASignature", TRUE);
        signatureInfo.put("sun.security.pkcs11.P11Signature", TRUE);
    }

    public static Signature getInstance(String algorithm2) throws NoSuchAlgorithmException {
        List<Provider.Service> list;
        NoSuchAlgorithmException failure;
        if (algorithm2.equalsIgnoreCase(RSA_SIGNATURE)) {
            list = GetInstance.getServices(rsaIds);
        } else {
            list = GetInstance.getServices("Signature", algorithm2);
        }
        Iterator<Provider.Service> t = list.iterator();
        if (t.hasNext()) {
            do {
                Provider.Service s = t.next();
                if (isSpi(s)) {
                    return new Delegate(algorithm2);
                }
                try {
                    return getInstance(GetInstance.getInstance(s, SignatureSpi.class), algorithm2);
                } catch (NoSuchAlgorithmException e) {
                    failure = e;
                    if (!t.hasNext()) {
                        throw failure;
                    }
                }
            } while (!t.hasNext());
            throw failure;
        }
        throw new NoSuchAlgorithmException(algorithm2 + " Signature not available");
    }

    private static Signature getInstance(GetInstance.Instance instance, String algorithm2) {
        Signature sig;
        if (instance.impl instanceof Signature) {
            sig = (Signature) instance.impl;
            sig.algorithm = algorithm2;
        } else {
            sig = new Delegate((SignatureSpi) instance.impl, algorithm2);
        }
        sig.provider = instance.provider;
        return sig;
    }

    /* access modifiers changed from: private */
    public static boolean isSpi(Provider.Service s) {
        boolean r = true;
        if (s.getType().equals("Cipher")) {
            return true;
        }
        String className = s.getClassName();
        Boolean result = signatureInfo.get(className);
        if (result == null) {
            try {
                Object instance = s.newInstance(null);
                if (!(instance instanceof SignatureSpi) || (instance instanceof Signature)) {
                    r = false;
                }
                result = Boolean.valueOf(r);
                signatureInfo.put(className, result);
            } catch (Exception e) {
                return false;
            }
        }
        return result.booleanValue();
    }

    public static Signature getInstance(String algorithm2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (!algorithm2.equalsIgnoreCase(RSA_SIGNATURE)) {
            Providers.checkBouncyCastleDeprecation(provider2, "Signature", algorithm2);
            return getInstance(GetInstance.getInstance("Signature", (Class<?>) SignatureSpi.class, algorithm2, provider2), algorithm2);
        } else if (provider2 == null || provider2.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        } else {
            Provider p = Security.getProvider(provider2);
            if (p != null) {
                return getInstanceRSA(p);
            }
            throw new NoSuchProviderException("no such provider: " + provider2);
        }
    }

    public static Signature getInstance(String algorithm2, Provider provider2) throws NoSuchAlgorithmException {
        if (!algorithm2.equalsIgnoreCase(RSA_SIGNATURE)) {
            Providers.checkBouncyCastleDeprecation(provider2, "Signature", algorithm2);
            return getInstance(GetInstance.getInstance("Signature", (Class<?>) SignatureSpi.class, algorithm2, provider2), algorithm2);
        } else if (provider2 != null) {
            return getInstanceRSA(provider2);
        } else {
            throw new IllegalArgumentException("missing provider");
        }
    }

    private static Signature getInstanceRSA(Provider p) throws NoSuchAlgorithmException {
        Provider.Service s = p.getService("Signature", RSA_SIGNATURE);
        if (s != null) {
            return getInstance(GetInstance.getInstance(s, SignatureSpi.class), RSA_SIGNATURE);
        }
        try {
            return new Delegate(new CipherAdapter(Cipher.getInstance(RSA_CIPHER, p)), RSA_SIGNATURE);
        } catch (GeneralSecurityException e) {
            throw new NoSuchAlgorithmException("no such algorithm: NONEwithRSA for provider " + p.getName(), e);
        }
    }

    public final Provider getProvider() {
        chooseFirstProvider();
        return this.provider;
    }

    /* access modifiers changed from: package-private */
    public void chooseFirstProvider() {
    }

    public final void initVerify(PublicKey publicKey) throws InvalidKeyException {
        engineInitVerify(publicKey);
        this.state = 3;
    }

    public final void initVerify(Certificate certificate) throws InvalidKeyException {
        if (certificate instanceof X509Certificate) {
            X509Certificate cert = (X509Certificate) certificate;
            Set<String> critSet = cert.getCriticalExtensionOIDs();
            if (critSet != null && !critSet.isEmpty() && critSet.contains("2.5.29.15")) {
                boolean[] keyUsageInfo = cert.getKeyUsage();
                if (keyUsageInfo != null && !keyUsageInfo[0]) {
                    throw new InvalidKeyException("Wrong key usage");
                }
            }
        }
        engineInitVerify(certificate.getPublicKey());
        this.state = 3;
    }

    public final void initSign(PrivateKey privateKey) throws InvalidKeyException {
        engineInitSign(privateKey);
        this.state = 2;
    }

    public final void initSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        engineInitSign(privateKey, random);
        this.state = 2;
    }

    public final byte[] sign() throws SignatureException {
        if (this.state == 2) {
            return engineSign();
        }
        throw new SignatureException("object not initialized for signing");
    }

    public final int sign(byte[] outbuf, int offset, int len) throws SignatureException {
        if (outbuf == null) {
            throw new IllegalArgumentException("No output buffer given");
        } else if (offset < 0 || len < 0) {
            throw new IllegalArgumentException("offset or len is less than 0");
        } else if (outbuf.length - offset < len) {
            throw new IllegalArgumentException("Output buffer too small for specified offset and length");
        } else if (this.state == 2) {
            return engineSign(outbuf, offset, len);
        } else {
            throw new SignatureException("object not initialized for signing");
        }
    }

    public final boolean verify(byte[] signature) throws SignatureException {
        if (this.state == 3) {
            return engineVerify(signature);
        }
        throw new SignatureException("object not initialized for verification");
    }

    public final boolean verify(byte[] signature, int offset, int length) throws SignatureException {
        if (this.state != 3) {
            throw new SignatureException("object not initialized for verification");
        } else if (signature == null) {
            throw new IllegalArgumentException("signature is null");
        } else if (offset < 0 || length < 0) {
            throw new IllegalArgumentException("offset or length is less than 0");
        } else if (signature.length - offset >= length) {
            return engineVerify(signature, offset, length);
        } else {
            throw new IllegalArgumentException("signature too small for specified offset and length");
        }
    }

    public final void update(byte b) throws SignatureException {
        if (this.state == 3 || this.state == 2) {
            engineUpdate(b);
            return;
        }
        throw new SignatureException("object not initialized for signature or verification");
    }

    public final void update(byte[] data) throws SignatureException {
        update(data, 0, data.length);
    }

    public final void update(byte[] data, int off, int len) throws SignatureException {
        if (this.state != 2 && this.state != 3) {
            throw new SignatureException("object not initialized for signature or verification");
        } else if (data == null) {
            throw new IllegalArgumentException("data is null");
        } else if (off < 0 || len < 0) {
            throw new IllegalArgumentException("off or len is less than 0");
        } else if (data.length - off >= len) {
            engineUpdate(data, off, len);
        } else {
            throw new IllegalArgumentException("data too small for specified offset and length");
        }
    }

    public final void update(ByteBuffer data) throws SignatureException {
        if (this.state != 2 && this.state != 3) {
            throw new SignatureException("object not initialized for signature or verification");
        } else if (data != null) {
            engineUpdate(data);
        } else {
            throw new NullPointerException();
        }
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public String toString() {
        String initState = "";
        int i = this.state;
        if (i != 0) {
            switch (i) {
                case 2:
                    initState = "<initialized for signing>";
                    break;
                case 3:
                    initState = "<initialized for verifying>";
                    break;
            }
        } else {
            initState = "<not initialized>";
        }
        return "Signature object: " + getAlgorithm() + initState;
    }

    @Deprecated
    public final void setParameter(String param, Object value) throws InvalidParameterException {
        engineSetParameter(param, value);
    }

    public final void setParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        engineSetParameter(params);
    }

    public final AlgorithmParameters getParameters() {
        return engineGetParameters();
    }

    @Deprecated
    public final Object getParameter(String param) throws InvalidParameterException {
        return engineGetParameter(param);
    }

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }

    public SignatureSpi getCurrentSpi() {
        return null;
    }
}
