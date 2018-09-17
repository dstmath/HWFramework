package java.security;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.Provider.Service;
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
import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;
import sun.security.jca.ServiceId;

public abstract class Signature extends SignatureSpi {
    private static final String RSA_CIPHER = "RSA/ECB/PKCS1Padding";
    private static final String RSA_SIGNATURE = "NONEwithRSA";
    protected static final int SIGN = 2;
    protected static final int UNINITIALIZED = 0;
    protected static final int VERIFY = 3;
    private static final List<ServiceId> rsaIds = Arrays.asList(new ServiceId("Signature", RSA_SIGNATURE), new ServiceId("Cipher", RSA_CIPHER), new ServiceId("Cipher", "RSA/ECB"), new ServiceId("Cipher", "RSA//PKCS1Padding"), new ServiceId("Cipher", "RSA"));
    private static final Map<String, Boolean> signatureInfo = new ConcurrentHashMap();
    private String algorithm;
    Provider provider;
    protected int state = 0;

    private static class CipherAdapter extends SignatureSpi {
        private final Cipher cipher;
        private ByteArrayOutputStream data;

        CipherAdapter(Cipher cipher) {
            this.cipher = cipher;
        }

        protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
            this.cipher.init(2, (Key) publicKey);
            if (this.data == null) {
                this.data = new ByteArrayOutputStream(128);
            } else {
                this.data.reset();
            }
        }

        protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
            this.cipher.init(1, (Key) privateKey);
            this.data = null;
        }

        protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
            this.cipher.init(1, (Key) privateKey, random);
            this.data = null;
        }

        protected void engineUpdate(byte b) throws SignatureException {
            engineUpdate(new byte[]{b}, 0, 1);
        }

        protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
            if (this.data != null) {
                this.data.write(b, off, len);
                return;
            }
            byte[] out = this.cipher.update(b, off, len);
            if (out != null && out.length != 0) {
                throw new SignatureException("Cipher unexpectedly returned data");
            }
        }

        protected byte[] engineSign() throws SignatureException {
            try {
                return this.cipher.doFinal();
            } catch (IllegalBlockSizeException e) {
                throw new SignatureException("doFinal() failed", e);
            } catch (BadPaddingException e2) {
                throw new SignatureException("doFinal() failed", e2);
            }
        }

        protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
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

        protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
            throw new InvalidParameterException("Parameters not supported");
        }

        protected Object engineGetParameter(String param) throws InvalidParameterException {
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

        Delegate(SignatureSpi sigSpi, String algorithm) {
            super(algorithm);
            this.sigSpi = sigSpi;
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

        private static SignatureSpi newInstance(Service s) throws NoSuchAlgorithmException {
            if (s.getType().equals("Cipher")) {
                try {
                    return new CipherAdapter(Cipher.getInstance(Signature.RSA_CIPHER, s.getProvider()));
                } catch (Throwable e) {
                    throw new NoSuchAlgorithmException(e);
                }
            }
            Object o = s.newInstance(null);
            if (o instanceof SignatureSpi) {
                return (SignatureSpi) o;
            }
            throw new NoSuchAlgorithmException("Not a SignatureSpi: " + o.getClass().getName());
        }

        void chooseFirstProvider() {
            if (this.sigSpi == null) {
                synchronized (this.lock) {
                    if (this.sigSpi != null) {
                        return;
                    }
                    Throwable lastException = null;
                    List<Service> list;
                    if (this.algorithm.equalsIgnoreCase(Signature.RSA_SIGNATURE)) {
                        list = GetInstance.getServices(Signature.rsaIds);
                    } else {
                        list = GetInstance.getServices("Signature", this.algorithm);
                    }
                    for (Service s : list) {
                        if (Signature.isSpi(s)) {
                            try {
                                this.sigSpi = newInstance(s);
                                this.provider = s.getProvider();
                                return;
                            } catch (Throwable e) {
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

        private void chooseProvider(int type, Key key, SecureRandom random) throws InvalidKeyException {
            synchronized (this.lock) {
                if (this.sigSpi == null || key != null) {
                    Throwable lastException = null;
                    List<Service> list;
                    if (this.algorithm.equalsIgnoreCase(Signature.RSA_SIGNATURE)) {
                        list = GetInstance.getServices(Signature.rsaIds);
                    } else {
                        list = GetInstance.getServices("Signature", this.algorithm);
                    }
                    for (Service s : list) {
                        if (s.supportsParameter(key) && Signature.isSpi(s)) {
                            try {
                                SignatureSpi spi = newInstance(s);
                                init(spi, type, key, random);
                                this.provider = s.getProvider();
                                this.sigSpi = spi;
                                return;
                            } catch (Throwable e) {
                                if (lastException == null) {
                                    lastException = e;
                                }
                                if (lastException instanceof InvalidKeyException) {
                                    throw ((InvalidKeyException) lastException);
                                }
                            }
                        }
                    }
                    if (lastException instanceof InvalidKeyException) {
                        throw ((InvalidKeyException) lastException);
                    } else if (lastException instanceof RuntimeException) {
                        throw ((RuntimeException) lastException);
                    } else {
                        throw new InvalidKeyException("No installed provider supports this key: " + (key != null ? key.getClass().getName() : "(null)"), lastException);
                    }
                }
                init(this.sigSpi, type, key, random);
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
                    throw new AssertionError("Internal error: " + type);
            }
        }

        protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
            if (this.sigSpi == null || !(this.lock == null || publicKey == null)) {
                chooseProvider(1, publicKey, null);
            } else {
                this.sigSpi.engineInitVerify(publicKey);
            }
        }

        protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
            if (this.sigSpi == null || !(this.lock == null || privateKey == null)) {
                chooseProvider(2, privateKey, null);
            } else {
                this.sigSpi.engineInitSign(privateKey);
            }
        }

        protected void engineInitSign(PrivateKey privateKey, SecureRandom sr) throws InvalidKeyException {
            if (this.sigSpi == null || !(this.lock == null || privateKey == null)) {
                chooseProvider(3, privateKey, sr);
            } else {
                this.sigSpi.engineInitSign(privateKey, sr);
            }
        }

        protected void engineUpdate(byte b) throws SignatureException {
            chooseFirstProvider();
            this.sigSpi.engineUpdate(b);
        }

        protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
            chooseFirstProvider();
            this.sigSpi.engineUpdate(b, off, len);
        }

        protected void engineUpdate(ByteBuffer data) {
            chooseFirstProvider();
            this.sigSpi.engineUpdate(data);
        }

        protected byte[] engineSign() throws SignatureException {
            chooseFirstProvider();
            return this.sigSpi.engineSign();
        }

        protected int engineSign(byte[] outbuf, int offset, int len) throws SignatureException {
            chooseFirstProvider();
            return this.sigSpi.engineSign(outbuf, offset, len);
        }

        protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
            chooseFirstProvider();
            return this.sigSpi.engineVerify(sigBytes);
        }

        protected boolean engineVerify(byte[] sigBytes, int offset, int length) throws SignatureException {
            chooseFirstProvider();
            return this.sigSpi.engineVerify(sigBytes, offset, length);
        }

        protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
            chooseFirstProvider();
            this.sigSpi.engineSetParameter(param, value);
        }

        protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            chooseFirstProvider();
            this.sigSpi.engineSetParameter(params);
        }

        protected Object engineGetParameter(String param) throws InvalidParameterException {
            chooseFirstProvider();
            return this.sigSpi.engineGetParameter(param);
        }

        protected AlgorithmParameters engineGetParameters() {
            chooseFirstProvider();
            return this.sigSpi.engineGetParameters();
        }

        public SignatureSpi getCurrentSpi() {
            if (this.lock == null) {
                return this.sigSpi;
            }
            SignatureSpi signatureSpi;
            synchronized (this.lock) {
                signatureSpi = this.sigSpi;
            }
            return signatureSpi;
        }
    }

    protected Signature(String algorithm) {
        this.algorithm = algorithm;
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

    public static Signature getInstance(String algorithm) throws NoSuchAlgorithmException {
        List<Service> list;
        if (algorithm.equalsIgnoreCase(RSA_SIGNATURE)) {
            list = GetInstance.getServices(rsaIds);
        } else {
            list = GetInstance.getServices("Signature", algorithm);
        }
        Iterator<Service> t = list.iterator();
        if (t.hasNext()) {
            do {
                Service s = (Service) t.next();
                if (isSpi(s)) {
                    return new Delegate(algorithm);
                }
                try {
                    return getInstance(GetInstance.getInstance(s, SignatureSpi.class), algorithm);
                } catch (NoSuchAlgorithmException e) {
                    NoSuchAlgorithmException failure = e;
                    if (!t.hasNext()) {
                        throw e;
                    }
                }
            } while (t.hasNext());
            throw e;
        }
        throw new NoSuchAlgorithmException(algorithm + " Signature not available");
    }

    private static Signature getInstance(Instance instance, String algorithm) {
        Signature sig;
        if (instance.impl instanceof Signature) {
            sig = instance.impl;
            sig.algorithm = algorithm;
        } else {
            sig = new Delegate(instance.impl, algorithm);
        }
        sig.provider = instance.provider;
        return sig;
    }

    private static boolean isSpi(Service s) {
        if (s.getType().equals("Cipher")) {
            return true;
        }
        String className = s.getClassName();
        Boolean result = (Boolean) signatureInfo.get(className);
        if (result == null) {
            try {
                Object instance = s.newInstance(null);
                boolean r = instance instanceof SignatureSpi ? !(instance instanceof Signature) : false;
                result = Boolean.valueOf(r);
                signatureInfo.put(className, result);
            } catch (Exception e) {
                return false;
            }
        }
        return result.booleanValue();
    }

    public static Signature getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (!algorithm.equalsIgnoreCase(RSA_SIGNATURE)) {
            return getInstance(GetInstance.getInstance("Signature", SignatureSpi.class, algorithm, provider), algorithm);
        }
        if (provider == null || provider.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        }
        Provider p = Security.getProvider(provider);
        if (p != null) {
            return getInstanceRSA(p);
        }
        throw new NoSuchProviderException("no such provider: " + provider);
    }

    public static Signature getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        if (!algorithm.equalsIgnoreCase(RSA_SIGNATURE)) {
            return getInstance(GetInstance.getInstance("Signature", SignatureSpi.class, algorithm, provider), algorithm);
        }
        if (provider != null) {
            return getInstanceRSA(provider);
        }
        throw new IllegalArgumentException("missing provider");
    }

    private static Signature getInstanceRSA(Provider p) throws NoSuchAlgorithmException {
        Service s = p.getService("Signature", RSA_SIGNATURE);
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

    void chooseFirstProvider() {
    }

    public final void initVerify(PublicKey publicKey) throws InvalidKeyException {
        engineInitVerify(publicKey);
        this.state = 3;
    }

    public final void initVerify(Certificate certificate) throws InvalidKeyException {
        if (certificate instanceof X509Certificate) {
            X509Certificate cert = (X509Certificate) certificate;
            Set<String> critSet = cert.getCriticalExtensionOIDs();
            if (!(critSet == null || (critSet.isEmpty() ^ 1) == 0 || !critSet.contains("2.5.29.15"))) {
                boolean[] keyUsageInfo = cert.getKeyUsage();
                if (!(keyUsageInfo == null || keyUsageInfo[0])) {
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
        } else if (data.length - off < len) {
            throw new IllegalArgumentException("data too small for specified offset and length");
        } else {
            engineUpdate(data, off, len);
        }
    }

    public final void update(ByteBuffer data) throws SignatureException {
        if (this.state != 2 && this.state != 3) {
            throw new SignatureException("object not initialized for signature or verification");
        } else if (data == null) {
            throw new NullPointerException();
        } else {
            engineUpdate(data);
        }
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public String toString() {
        String initState = "";
        switch (this.state) {
            case 0:
                initState = "<not initialized>";
                break;
            case 2:
                initState = "<initialized for signing>";
                break;
            case 3:
                initState = "<initialized for verifying>";
                break;
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
