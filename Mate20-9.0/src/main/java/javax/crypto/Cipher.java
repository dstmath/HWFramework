package javax.crypto;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.RC5ParameterSpec;
import sun.security.jca.Providers;

public class Cipher {
    private static final String ATTRIBUTE_MODES = "SupportedModes";
    private static final String ATTRIBUTE_PADDINGS = "SupportedPaddings";
    public static final int DECRYPT_MODE = 2;
    public static final int ENCRYPT_MODE = 1;
    private static final String KEY_USAGE_EXTENSION_OID = "2.5.29.15";
    public static final int PRIVATE_KEY = 2;
    public static final int PUBLIC_KEY = 1;
    public static final int SECRET_KEY = 3;
    public static final int UNWRAP_MODE = 4;
    public static final int WRAP_MODE = 3;
    private ExemptionMechanism exmech;
    private boolean initialized = false;
    private int opmode = 0;
    /* access modifiers changed from: private */
    public Provider provider;
    /* access modifiers changed from: private */
    public CipherSpi spi;
    private final SpiAndProviderUpdater spiAndProviderUpdater;
    /* access modifiers changed from: private */
    public final String[] tokenizedTransformation;
    private final String transformation;

    static class CipherSpiAndProvider {
        CipherSpi cipherSpi;
        Provider provider;

        CipherSpiAndProvider(CipherSpi cipherSpi2, Provider provider2) {
            this.cipherSpi = cipherSpi2;
            this.provider = provider2;
        }
    }

    static class InitParams {
        final InitType initType;
        final Key key;
        final int opmode;
        final AlgorithmParameters params;
        final SecureRandom random;
        final AlgorithmParameterSpec spec;

        InitParams(InitType initType2, int opmode2, Key key2, SecureRandom random2, AlgorithmParameterSpec spec2, AlgorithmParameters params2) {
            this.initType = initType2;
            this.opmode = opmode2;
            this.key = key2;
            this.random = random2;
            this.spec = spec2;
            this.params = params2;
        }
    }

    enum InitType {
        KEY,
        ALGORITHM_PARAMS,
        ALGORITHM_PARAM_SPEC
    }

    enum NeedToSet {
        NONE,
        MODE,
        PADDING,
        BOTH
    }

    class SpiAndProviderUpdater {
        private final Object initSpiLock = new Object();
        private final Provider specifiedProvider;
        private final CipherSpi specifiedSpi;

        SpiAndProviderUpdater(Provider specifiedProvider2, CipherSpi specifiedSpi2) {
            this.specifiedProvider = specifiedProvider2;
            this.specifiedSpi = specifiedSpi2;
        }

        /* access modifiers changed from: package-private */
        public void setCipherSpiImplAndProvider(CipherSpi cipherSpi, Provider provider) {
            CipherSpi unused = Cipher.this.spi = cipherSpi;
            Provider unused2 = Cipher.this.provider = provider;
        }

        /* access modifiers changed from: package-private */
        public CipherSpiAndProvider updateAndGetSpiAndProvider(InitParams initParams, CipherSpi spiImpl, Provider provider) throws InvalidKeyException, InvalidAlgorithmParameterException {
            if (this.specifiedSpi != null) {
                return new CipherSpiAndProvider(this.specifiedSpi, provider);
            }
            synchronized (this.initSpiLock) {
                if (spiImpl == null || initParams != null) {
                    CipherSpiAndProvider sap = Cipher.tryCombinations(initParams, this.specifiedProvider, Cipher.this.tokenizedTransformation);
                    if (sap != null) {
                        setCipherSpiImplAndProvider(sap.cipherSpi, sap.provider);
                        CipherSpiAndProvider cipherSpiAndProvider = new CipherSpiAndProvider(sap.cipherSpi, sap.provider);
                        return cipherSpiAndProvider;
                    }
                    throw new ProviderException("No provider found for " + Arrays.toString((Object[]) Cipher.this.tokenizedTransformation));
                }
                CipherSpiAndProvider cipherSpiAndProvider2 = new CipherSpiAndProvider(spiImpl, provider);
                return cipherSpiAndProvider2;
            }
        }

        /* access modifiers changed from: package-private */
        public CipherSpiAndProvider updateAndGetSpiAndProvider(CipherSpi spiImpl, Provider provider) {
            try {
                return updateAndGetSpiAndProvider(null, spiImpl, provider);
            } catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
                throw new ProviderException("Exception thrown when params == null", e);
            }
        }

        /* access modifiers changed from: package-private */
        public CipherSpi getCurrentSpi(CipherSpi spiImpl) {
            if (this.specifiedSpi != null) {
                return this.specifiedSpi;
            }
            synchronized (this.initSpiLock) {
            }
            return spiImpl;
        }
    }

    static class Transform {
        /* access modifiers changed from: private */
        public final String name;
        /* access modifiers changed from: private */
        public final NeedToSet needToSet;

        public Transform(String name2, NeedToSet needToSet2) {
            this.name = name2;
            this.needToSet = needToSet2;
        }
    }

    protected Cipher(CipherSpi cipherSpi, Provider provider2, String transformation2) {
        if (cipherSpi == null) {
            throw new NullPointerException("cipherSpi == null");
        } else if ((cipherSpi instanceof NullCipherSpi) || provider2 != null) {
            this.spi = cipherSpi;
            this.provider = provider2;
            this.transformation = transformation2;
            this.tokenizedTransformation = null;
            this.spiAndProviderUpdater = new SpiAndProviderUpdater(provider2, cipherSpi);
        } else {
            throw new NullPointerException("provider == null");
        }
    }

    private Cipher(CipherSpi cipherSpi, Provider provider2, String transformation2, String[] tokenizedTransformation2) {
        this.spi = cipherSpi;
        this.provider = provider2;
        this.transformation = transformation2;
        this.tokenizedTransformation = tokenizedTransformation2;
        this.spiAndProviderUpdater = new SpiAndProviderUpdater(provider2, cipherSpi);
    }

    private static String[] tokenizeTransformation(String transformation2) throws NoSuchAlgorithmException {
        if (transformation2 == null || transformation2.isEmpty()) {
            throw new NoSuchAlgorithmException("No transformation given");
        }
        String[] parts = new String[3];
        int count = 0;
        StringTokenizer parser = new StringTokenizer(transformation2, "/");
        while (parser.hasMoreTokens() && count < 3) {
            try {
                int count2 = count + 1;
                try {
                    parts[count] = parser.nextToken().trim();
                    count = count2;
                } catch (NoSuchElementException e) {
                    int i = count2;
                    throw new NoSuchAlgorithmException("Invalid transformation format:" + transformation2);
                }
            } catch (NoSuchElementException e2) {
                throw new NoSuchAlgorithmException("Invalid transformation format:" + transformation2);
            }
        }
        if (!(count == 0 || count == 2)) {
            if (!parser.hasMoreTokens()) {
                if (parts[0] != null && parts[0].length() != 0) {
                    return parts;
                }
                throw new NoSuchAlgorithmException("Invalid transformation:algorithm not specified-" + transformation2);
            }
        }
        throw new NoSuchAlgorithmException("Invalid transformation format:" + transformation2);
    }

    public static final Cipher getInstance(String transformation2) throws NoSuchAlgorithmException, NoSuchPaddingException {
        return createCipher(transformation2, null);
    }

    public static final Cipher getInstance(String transformation2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        if (provider2 == null || provider2.length() == 0) {
            throw new IllegalArgumentException("Missing provider");
        }
        Provider p = Security.getProvider(provider2);
        if (p != null) {
            return getInstance(transformation2, p);
        }
        throw new NoSuchProviderException("No such provider: " + provider2);
    }

    public static final Cipher getInstance(String transformation2, Provider provider2) throws NoSuchAlgorithmException, NoSuchPaddingException {
        if (provider2 != null) {
            return createCipher(transformation2, provider2);
        }
        throw new IllegalArgumentException("Missing provider");
    }

    static final Cipher createCipher(String transformation2, Provider provider2) throws NoSuchAlgorithmException, NoSuchPaddingException {
        Providers.checkBouncyCastleDeprecation(provider2, "Cipher", transformation2);
        String[] tokenizedTransformation2 = tokenizeTransformation(transformation2);
        try {
            if (tryCombinations(null, provider2, tokenizedTransformation2) != null) {
                return new Cipher(null, provider2, transformation2, tokenizedTransformation2);
            }
            if (provider2 == null) {
                throw new NoSuchAlgorithmException("No provider found for " + transformation2);
            }
            throw new NoSuchAlgorithmException("Provider " + provider2.getName() + " does not provide " + transformation2);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new IllegalStateException("Key/Algorithm excepton despite not passing one", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateProviderIfNeeded() {
        try {
            this.spiAndProviderUpdater.updateAndGetSpiAndProvider(null, this.spi, this.provider);
        } catch (Exception lastException) {
            ProviderException e = new ProviderException("Could not construct CipherSpi instance");
            if (lastException != null) {
                e.initCause(lastException);
            }
            throw e;
        }
    }

    private void chooseProvider(InitType initType, int opmode2, Key key, AlgorithmParameterSpec paramSpec, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        try {
            InitParams initParams = new InitParams(initType, opmode2, key, random, paramSpec, params);
            this.spiAndProviderUpdater.updateAndGetSpiAndProvider(initParams, this.spi, this.provider);
        } catch (Exception lastException) {
            if (lastException instanceof InvalidKeyException) {
                throw ((InvalidKeyException) lastException);
            } else if (lastException instanceof InvalidAlgorithmParameterException) {
                throw ((InvalidAlgorithmParameterException) lastException);
            } else if (!(lastException instanceof RuntimeException)) {
                String kName = key != null ? key.getClass().getName() : "(null)";
                throw new InvalidKeyException("No installed provider supports this key: " + kName, lastException);
            } else {
                throw ((RuntimeException) lastException);
            }
        }
    }

    public final Provider getProvider() {
        updateProviderIfNeeded();
        return this.provider;
    }

    public final String getAlgorithm() {
        return this.transformation;
    }

    public final int getBlockSize() {
        updateProviderIfNeeded();
        return this.spi.engineGetBlockSize();
    }

    public final int getOutputSize(int inputLen) {
        if (!this.initialized && !(this instanceof NullCipher)) {
            throw new IllegalStateException("Cipher not initialized");
        } else if (inputLen >= 0) {
            updateProviderIfNeeded();
            return this.spi.engineGetOutputSize(inputLen);
        } else {
            throw new IllegalArgumentException("Input size must be equal to or greater than zero");
        }
    }

    public final byte[] getIV() {
        updateProviderIfNeeded();
        return this.spi.engineGetIV();
    }

    public final AlgorithmParameters getParameters() {
        updateProviderIfNeeded();
        return this.spi.engineGetParameters();
    }

    public final ExemptionMechanism getExemptionMechanism() {
        updateProviderIfNeeded();
        return this.exmech;
    }

    private static void checkOpmode(int opmode2) {
        if (opmode2 < 1 || opmode2 > 4) {
            throw new InvalidParameterException("Invalid operation mode");
        }
    }

    private static String getOpmodeString(int opmode2) {
        switch (opmode2) {
            case 1:
                return "encryption";
            case 2:
                return "decryption";
            case 3:
                return "key wrapping";
            case 4:
                return "key unwrapping";
            default:
                return "";
        }
    }

    public final void init(int opmode2, Key key) throws InvalidKeyException {
        init(opmode2, key, JceSecurity.RANDOM);
    }

    public final void init(int opmode2, Key key, SecureRandom random) throws InvalidKeyException {
        this.initialized = false;
        checkOpmode(opmode2);
        try {
            chooseProvider(InitType.KEY, opmode2, key, null, null, random);
            this.initialized = true;
            this.opmode = opmode2;
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException((Throwable) e);
        }
    }

    public final void init(int opmode2, Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        init(opmode2, key, params, JceSecurity.RANDOM);
    }

    public final void init(int opmode2, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.initialized = false;
        checkOpmode(opmode2);
        chooseProvider(InitType.ALGORITHM_PARAM_SPEC, opmode2, key, params, null, random);
        this.initialized = true;
        this.opmode = opmode2;
    }

    public final void init(int opmode2, Key key, AlgorithmParameters params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        init(opmode2, key, params, JceSecurity.RANDOM);
    }

    public final void init(int opmode2, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.initialized = false;
        checkOpmode(opmode2);
        chooseProvider(InitType.ALGORITHM_PARAMS, opmode2, key, null, params, random);
        this.initialized = true;
        this.opmode = opmode2;
    }

    public final void init(int opmode2, Certificate certificate) throws InvalidKeyException {
        init(opmode2, certificate, JceSecurity.RANDOM);
    }

    public final void init(int opmode2, Certificate certificate, SecureRandom random) throws InvalidKeyException {
        this.initialized = false;
        checkOpmode(opmode2);
        if (certificate instanceof X509Certificate) {
            X509Certificate cert = (X509Certificate) certificate;
            Set<String> critSet = cert.getCriticalExtensionOIDs();
            if (critSet != null && !critSet.isEmpty() && critSet.contains(KEY_USAGE_EXTENSION_OID)) {
                boolean[] keyUsageInfo = cert.getKeyUsage();
                if (keyUsageInfo != null && ((opmode2 == 1 && keyUsageInfo.length > 3 && !keyUsageInfo[3]) || (opmode2 == 3 && keyUsageInfo.length > 2 && !keyUsageInfo[2]))) {
                    throw new InvalidKeyException("Wrong key usage");
                }
            }
        }
        try {
            chooseProvider(InitType.KEY, opmode2, certificate == null ? null : certificate.getPublicKey(), null, null, random);
            this.initialized = true;
            this.opmode = opmode2;
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException((Throwable) e);
        }
    }

    private void checkCipherState() {
        if (this instanceof NullCipher) {
            return;
        }
        if (!this.initialized) {
            throw new IllegalStateException("Cipher not initialized");
        } else if (this.opmode != 1 && this.opmode != 2) {
            throw new IllegalStateException("Cipher not initialized for encryption/decryption");
        }
    }

    public final byte[] update(byte[] input) {
        checkCipherState();
        if (input != null) {
            updateProviderIfNeeded();
            if (input.length == 0) {
                return null;
            }
            return this.spi.engineUpdate(input, 0, input.length);
        }
        throw new IllegalArgumentException("Null input buffer");
    }

    public final byte[] update(byte[] input, int inputOffset, int inputLen) {
        checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        updateProviderIfNeeded();
        if (inputLen == 0) {
            return null;
        }
        return this.spi.engineUpdate(input, inputOffset, inputLen);
    }

    public final int update(byte[] input, int inputOffset, int inputLen, byte[] output) throws ShortBufferException {
        checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        updateProviderIfNeeded();
        if (inputLen == 0) {
            return 0;
        }
        return this.spi.engineUpdate(input, inputOffset, inputLen, output, 0);
    }

    public final int update(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0 || outputOffset < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        updateProviderIfNeeded();
        if (inputLen == 0) {
            return 0;
        }
        return this.spi.engineUpdate(input, inputOffset, inputLen, output, outputOffset);
    }

    public final int update(ByteBuffer input, ByteBuffer output) throws ShortBufferException {
        checkCipherState();
        if (input == null || output == null) {
            throw new IllegalArgumentException("Buffers must not be null");
        } else if (input == output) {
            throw new IllegalArgumentException("Input and output buffers must not be the same object, consider using buffer.duplicate()");
        } else if (!output.isReadOnly()) {
            updateProviderIfNeeded();
            return this.spi.engineUpdate(input, output);
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final byte[] doFinal() throws IllegalBlockSizeException, BadPaddingException {
        checkCipherState();
        updateProviderIfNeeded();
        return this.spi.engineDoFinal(null, 0, 0);
    }

    public final int doFinal(byte[] output, int outputOffset) throws IllegalBlockSizeException, ShortBufferException, BadPaddingException {
        checkCipherState();
        if (output == null || outputOffset < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        updateProviderIfNeeded();
        return this.spi.engineDoFinal(null, 0, 0, output, outputOffset);
    }

    public final byte[] doFinal(byte[] input) throws IllegalBlockSizeException, BadPaddingException {
        checkCipherState();
        if (input != null) {
            updateProviderIfNeeded();
            return this.spi.engineDoFinal(input, 0, input.length);
        }
        throw new IllegalArgumentException("Null input buffer");
    }

    public final byte[] doFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        updateProviderIfNeeded();
        return this.spi.engineDoFinal(input, inputOffset, inputLen);
    }

    public final int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        updateProviderIfNeeded();
        return this.spi.engineDoFinal(input, inputOffset, inputLen, output, 0);
    }

    public final int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        checkCipherState();
        if (input == null || inputOffset < 0 || inputLen > input.length - inputOffset || inputLen < 0 || outputOffset < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        updateProviderIfNeeded();
        return this.spi.engineDoFinal(input, inputOffset, inputLen, output, outputOffset);
    }

    public final int doFinal(ByteBuffer input, ByteBuffer output) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        checkCipherState();
        if (input == null || output == null) {
            throw new IllegalArgumentException("Buffers must not be null");
        } else if (input == output) {
            throw new IllegalArgumentException("Input and output buffers must not be the same object, consider using buffer.duplicate()");
        } else if (!output.isReadOnly()) {
            updateProviderIfNeeded();
            return this.spi.engineDoFinal(input, output);
        } else {
            throw new ReadOnlyBufferException();
        }
    }

    public final byte[] wrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        if (!(this instanceof NullCipher)) {
            if (!this.initialized) {
                throw new IllegalStateException("Cipher not initialized");
            } else if (this.opmode != 3) {
                throw new IllegalStateException("Cipher not initialized for wrapping keys");
            }
        }
        updateProviderIfNeeded();
        return this.spi.engineWrap(key);
    }

    public final Key unwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        if (!(this instanceof NullCipher)) {
            if (!this.initialized) {
                throw new IllegalStateException("Cipher not initialized");
            } else if (this.opmode != 4) {
                throw new IllegalStateException("Cipher not initialized for unwrapping keys");
            }
        }
        if (wrappedKeyType == 3 || wrappedKeyType == 2 || wrappedKeyType == 1) {
            updateProviderIfNeeded();
            return this.spi.engineUnwrap(wrappedKey, wrappedKeyAlgorithm, wrappedKeyType);
        }
        throw new InvalidParameterException("Invalid key type");
    }

    private AlgorithmParameterSpec getAlgorithmParameterSpec(AlgorithmParameters params) throws InvalidParameterSpecException {
        if (params == null) {
            return null;
        }
        String alg = params.getAlgorithm().toUpperCase(Locale.ENGLISH);
        if (alg.equalsIgnoreCase("RC2")) {
            return params.getParameterSpec(RC2ParameterSpec.class);
        }
        if (alg.equalsIgnoreCase("RC5")) {
            return params.getParameterSpec(RC5ParameterSpec.class);
        }
        if (alg.startsWith("PBE")) {
            return params.getParameterSpec(PBEParameterSpec.class);
        }
        if (alg.startsWith("DES")) {
            return params.getParameterSpec(IvParameterSpec.class);
        }
        return null;
    }

    public static final int getMaxAllowedKeyLength(String transformation2) throws NoSuchAlgorithmException {
        if (transformation2 != null) {
            tokenizeTransformation(transformation2);
            return Integer.MAX_VALUE;
        }
        throw new NullPointerException("transformation == null");
    }

    public static final AlgorithmParameterSpec getMaxAllowedParameterSpec(String transformation2) throws NoSuchAlgorithmException {
        if (transformation2 != null) {
            tokenizeTransformation(transformation2);
            return null;
        }
        throw new NullPointerException("transformation == null");
    }

    public final void updateAAD(byte[] src) {
        if (src != null) {
            updateAAD(src, 0, src.length);
            return;
        }
        throw new IllegalArgumentException("src buffer is null");
    }

    public final void updateAAD(byte[] src, int offset, int len) {
        checkCipherState();
        if (src == null || offset < 0 || len < 0 || len + offset > src.length) {
            throw new IllegalArgumentException("Bad arguments");
        }
        updateProviderIfNeeded();
        if (len != 0) {
            this.spi.engineUpdateAAD(src, offset, len);
        }
    }

    public final void updateAAD(ByteBuffer src) {
        checkCipherState();
        if (src != null) {
            updateProviderIfNeeded();
            if (src.remaining() != 0) {
                this.spi.engineUpdateAAD(src);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("src ByteBuffer is null");
    }

    public CipherSpi getCurrentSpi() {
        return this.spi;
    }

    static boolean matchAttribute(Provider.Service service, String attr, String value) {
        if (value == null) {
            return true;
        }
        String pattern = service.getAttribute(attr);
        if (pattern == null) {
            return true;
        }
        return value.toUpperCase(Locale.US).matches(pattern.toUpperCase(Locale.US));
    }

    static CipherSpiAndProvider tryCombinations(InitParams initParams, Provider provider2, String[] tokenizedTransformation2) throws InvalidKeyException, InvalidAlgorithmParameterException {
        ArrayList<Transform> transforms = new ArrayList<>();
        if (!(tokenizedTransformation2[1] == null || tokenizedTransformation2[2] == null)) {
            transforms.add(new Transform(tokenizedTransformation2[0] + "/" + tokenizedTransformation2[1] + "/" + tokenizedTransformation2[2], NeedToSet.NONE));
        }
        if (tokenizedTransformation2[1] != null) {
            transforms.add(new Transform(tokenizedTransformation2[0] + "/" + tokenizedTransformation2[1], NeedToSet.PADDING));
        }
        if (tokenizedTransformation2[2] != null) {
            transforms.add(new Transform(tokenizedTransformation2[0] + "//" + tokenizedTransformation2[2], NeedToSet.MODE));
        }
        transforms.add(new Transform(tokenizedTransformation2[0], NeedToSet.BOTH));
        Exception cause = null;
        if (provider2 != null) {
            Iterator<Transform> it = transforms.iterator();
            while (it.hasNext()) {
                Transform transform = it.next();
                Provider.Service service = provider2.getService("Cipher", transform.name);
                if (service != null) {
                    return tryTransformWithProvider(initParams, tokenizedTransformation2, transform.needToSet, service);
                }
            }
        } else {
            for (Provider prov : Security.getProviders()) {
                Iterator<Transform> it2 = transforms.iterator();
                while (it2.hasNext()) {
                    Transform transform2 = it2.next();
                    Provider.Service service2 = prov.getService("Cipher", transform2.name);
                    if (service2 != null && (initParams == null || initParams.key == null || service2.supportsParameter(initParams.key))) {
                        try {
                            CipherSpiAndProvider sap = tryTransformWithProvider(initParams, tokenizedTransformation2, transform2.needToSet, service2);
                            if (sap != null) {
                                return sap;
                            }
                        } catch (Exception e) {
                            if (cause == null) {
                                cause = e;
                            }
                        }
                    }
                }
            }
        }
        if (cause instanceof InvalidKeyException) {
            throw cause;
        } else if (cause instanceof InvalidAlgorithmParameterException) {
            throw cause;
        } else if (cause instanceof RuntimeException) {
            throw cause;
        } else if (cause != null) {
            throw new InvalidKeyException("No provider can be initialized with given key", cause);
        } else if (initParams == null || initParams.key == null) {
            return null;
        } else {
            throw new InvalidKeyException("No provider offers " + Arrays.toString((Object[]) tokenizedTransformation2) + " for " + initParams.key.getAlgorithm() + " key of class " + initParams.key.getClass().getName() + " and export format " + initParams.key.getFormat());
        }
    }

    static CipherSpiAndProvider tryTransformWithProvider(InitParams initParams, String[] tokenizedTransformation2, NeedToSet type, Provider.Service service) throws InvalidKeyException, InvalidAlgorithmParameterException {
        try {
            if (matchAttribute(service, ATTRIBUTE_MODES, tokenizedTransformation2[1])) {
                if (matchAttribute(service, ATTRIBUTE_PADDINGS, tokenizedTransformation2[2])) {
                    CipherSpiAndProvider sap = new CipherSpiAndProvider((CipherSpi) service.newInstance(null), service.getProvider());
                    if (sap.cipherSpi != null) {
                        if (sap.provider != null) {
                            CipherSpi spi2 = sap.cipherSpi;
                            if ((type == NeedToSet.MODE || type == NeedToSet.BOTH) && tokenizedTransformation2[1] != null) {
                                spi2.engineSetMode(tokenizedTransformation2[1]);
                            }
                            if ((type == NeedToSet.PADDING || type == NeedToSet.BOTH) && tokenizedTransformation2[2] != null) {
                                spi2.engineSetPadding(tokenizedTransformation2[2]);
                            }
                            if (initParams != null) {
                                switch (initParams.initType) {
                                    case ALGORITHM_PARAMS:
                                        spi2.engineInit(initParams.opmode, initParams.key, initParams.params, initParams.random);
                                        break;
                                    case ALGORITHM_PARAM_SPEC:
                                        spi2.engineInit(initParams.opmode, initParams.key, initParams.spec, initParams.random);
                                        break;
                                    case KEY:
                                        spi2.engineInit(initParams.opmode, initParams.key, initParams.random);
                                        break;
                                    default:
                                        throw new AssertionError((Object) "This should never be reached");
                                }
                            }
                            return new CipherSpiAndProvider(spi2, sap.provider);
                        }
                    }
                    return null;
                }
            }
            return null;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            return null;
        }
    }
}
