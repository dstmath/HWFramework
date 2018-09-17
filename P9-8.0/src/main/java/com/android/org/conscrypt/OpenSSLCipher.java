package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef.EVP_CIPHER_CTX;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Locale;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public abstract class OpenSSLCipher extends CipherSpi {
    private int blockSize;
    protected byte[] encodedKey;
    private boolean encrypting;
    protected byte[] iv;
    protected Mode mode = Mode.ECB;
    private Padding padding = Padding.PKCS5PADDING;

    public static abstract class EVP_AEAD extends OpenSSLCipher {
        private static final int DEFAULT_TAG_SIZE_BITS = 128;
        private static int lastGlobalMessageSize = 32;
        private byte[] aad;
        protected byte[] buf;
        protected int bufCount;
        protected long evpAead;
        private int tagLengthInBytes;

        public static abstract class AES extends EVP_AEAD {
            private static final int AES_BLOCK_SIZE = 16;

            public static class GCM extends AES {
                private boolean mustInitialize;
                private byte[] previousIv;
                private byte[] previousKey;

                public static class AES_128 extends GCM {
                    protected void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                        if (keyLength != 16) {
                            throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes (must be 16)");
                        }
                    }
                }

                public static class AES_256 extends GCM {
                    protected void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                        if (keyLength != 32) {
                            throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes (must be 32)");
                        }
                    }
                }

                public GCM() {
                    super(Mode.GCM);
                }

                private void checkInitialization() {
                    if (this.mustInitialize) {
                        throw new IllegalStateException("Cannot re-use same key and IV for multiple encryptions");
                    }
                }

                private boolean arraysAreEqual(byte[] a, byte[] b) {
                    boolean z = false;
                    if (a.length != b.length) {
                        return false;
                    }
                    int diff = 0;
                    for (int i = 0; i < a.length; i++) {
                        diff |= a[i] ^ b[i];
                    }
                    if (diff == 0) {
                        z = true;
                    }
                    return z;
                }

                protected void engineInitInternal(byte[] encodedKey, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
                    super.engineInitInternal(encodedKey, params, random);
                    if (isEncrypting() && this.iv != null) {
                        if (this.previousKey == null || this.previousIv == null || !arraysAreEqual(this.previousKey, encodedKey) || !arraysAreEqual(this.previousIv, this.iv)) {
                            this.previousKey = encodedKey;
                            this.previousIv = this.iv;
                        } else {
                            this.mustInitialize = true;
                            throw new InvalidAlgorithmParameterException("In GCM mode key and IV must not be re-used");
                        }
                    }
                    this.mustInitialize = false;
                }

                protected int updateInternal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset, int maximumLen) throws ShortBufferException {
                    checkInitialization();
                    return super.updateInternal(input, inputOffset, inputLen, output, outputOffset, maximumLen);
                }

                protected int doFinalInternal(byte[] output, int outputOffset, int maximumLen) throws IllegalBlockSizeException, BadPaddingException {
                    checkInitialization();
                    int retVal = super.doFinalInternal(output, outputOffset, maximumLen);
                    if (isEncrypting()) {
                        this.mustInitialize = true;
                    }
                    return retVal;
                }

                protected void engineUpdateAAD(byte[] input, int inputOffset, int inputLen) {
                    checkInitialization();
                    super.engineUpdateAAD(input, inputOffset, inputLen);
                }

                protected void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
                    if (mode != Mode.GCM) {
                        throw new NoSuchAlgorithmException("Mode must be GCM");
                    }
                }

                protected long getEVP_AEAD(int keyLength) throws InvalidKeyException {
                    if (keyLength == 16) {
                        return NativeCrypto.EVP_aead_aes_128_gcm();
                    }
                    if (keyLength == 32) {
                        return NativeCrypto.EVP_aead_aes_256_gcm();
                    }
                    throw new RuntimeException("Unexpected key length: " + keyLength);
                }
            }

            protected AES(Mode mode) {
                super(mode);
            }

            protected void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                switch (keyLength) {
                    case 16:
                    case 32:
                        return;
                    default:
                        throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes (must be 16 or 32)");
                }
            }

            protected String getBaseCipherName() {
                return "AES";
            }

            protected int getCipherBlockSize() {
                return 16;
            }

            protected int getOutputSizeForUpdate(int inputLen) {
                return 0;
            }
        }

        protected abstract long getEVP_AEAD(int i) throws InvalidKeyException;

        public EVP_AEAD(Mode mode) {
            super(mode, Padding.NOPADDING);
        }

        private void expand(int i) {
            if (this.bufCount + i > this.buf.length) {
                byte[] newbuf = new byte[((this.bufCount + i) * 2)];
                System.arraycopy(this.buf, 0, newbuf, 0, this.bufCount);
                this.buf = newbuf;
            }
        }

        private void reset() {
            this.aad = null;
            int lastBufSize = lastGlobalMessageSize;
            if (this.buf == null) {
                this.buf = new byte[lastBufSize];
            } else if (this.bufCount > 0 && this.bufCount != lastBufSize) {
                lastGlobalMessageSize = this.bufCount;
                if (this.buf.length != this.bufCount) {
                    this.buf = new byte[this.bufCount];
                }
            }
            this.bufCount = 0;
        }

        protected void engineInitInternal(byte[] encodedKey, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            byte[] iv;
            int tagLenBits;
            if (params == null) {
                iv = null;
                tagLenBits = 128;
            } else {
                GCMParameters gcmParams = Platform.fromGCMParameterSpec(params);
                if (gcmParams != null) {
                    iv = gcmParams.getIV();
                    tagLenBits = gcmParams.getTLen();
                } else if (params instanceof IvParameterSpec) {
                    iv = ((IvParameterSpec) params).getIV();
                    tagLenBits = 128;
                } else {
                    iv = null;
                    tagLenBits = 128;
                }
            }
            if (tagLenBits % 8 != 0) {
                throw new InvalidAlgorithmParameterException("Tag length must be a multiple of 8; was " + this.tagLengthInBytes);
            }
            this.tagLengthInBytes = tagLenBits / 8;
            boolean encrypting = isEncrypting();
            this.evpAead = getEVP_AEAD(encodedKey.length);
            int expectedIvLength = NativeCrypto.EVP_AEAD_nonce_length(this.evpAead);
            if (iv != null || expectedIvLength == 0) {
                if (expectedIvLength == 0 && iv != null) {
                    throw new InvalidAlgorithmParameterException("IV not used in " + this.mode + " mode");
                } else if (!(iv == null || iv.length == expectedIvLength)) {
                    throw new InvalidAlgorithmParameterException("Expected IV length of " + expectedIvLength + " but was " + iv.length);
                }
            } else if (encrypting) {
                iv = new byte[expectedIvLength];
                if (random != null) {
                    random.nextBytes(iv);
                } else {
                    NativeCrypto.RAND_bytes(iv);
                }
            } else {
                throw new InvalidAlgorithmParameterException("IV must be specified in " + this.mode + " mode");
            }
            this.iv = iv;
            reset();
        }

        protected int updateInternal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset, int maximumLen) throws ShortBufferException {
            if (this.buf == null) {
                throw new IllegalStateException("Cipher not initialized");
            }
            ArrayUtils.checkOffsetAndCount(input.length, inputOffset, inputLen);
            if (inputLen > 0) {
                expand(inputLen);
                System.arraycopy(input, inputOffset, this.buf, this.bufCount, inputLen);
                this.bufCount += inputLen;
            }
            return 0;
        }

        private void throwAEADBadTagExceptionIfAvailable(String message, Throwable cause) throws BadPaddingException {
            try {
                BadPaddingException badPaddingException = null;
                try {
                    badPaddingException = (BadPaddingException) Class.forName("javax.crypto.AEADBadTagException").getConstructor(new Class[]{String.class}).newInstance(new Object[]{message});
                    badPaddingException.initCause(cause);
                } catch (IllegalAccessException e) {
                } catch (InstantiationException e2) {
                } catch (InvocationTargetException e22) {
                    throw ((BadPaddingException) new BadPaddingException().initCause(e22.getTargetException()));
                }
                if (badPaddingException != null) {
                    throw badPaddingException;
                }
            } catch (Exception e3) {
            }
        }

        protected int doFinalInternal(byte[] output, int outputOffset, int maximumLen) throws IllegalBlockSizeException, BadPaddingException {
            try {
                int bytesWritten;
                if (isEncrypting()) {
                    bytesWritten = NativeCrypto.EVP_AEAD_CTX_seal(this.evpAead, this.encodedKey, this.tagLengthInBytes, output, outputOffset, this.iv, this.buf, 0, this.bufCount, this.aad);
                } else {
                    bytesWritten = NativeCrypto.EVP_AEAD_CTX_open(this.evpAead, this.encodedKey, this.tagLengthInBytes, output, outputOffset, this.iv, this.buf, 0, this.bufCount, this.aad);
                }
                reset();
                return bytesWritten;
            } catch (BadPaddingException e) {
                throwAEADBadTagExceptionIfAvailable(e.getMessage(), e.getCause());
                throw e;
            }
        }

        protected void checkSupportedPadding(Padding padding) throws NoSuchPaddingException {
            if (padding != Padding.NOPADDING) {
                throw new NoSuchPaddingException("Must be NoPadding for AEAD ciphers");
            }
        }

        protected int getOutputSizeForFinal(int inputLen) {
            return (isEncrypting() ? NativeCrypto.EVP_AEAD_max_overhead(this.evpAead) : 0) + (this.bufCount + inputLen);
        }

        protected void engineUpdateAAD(byte[] input, int inputOffset, int inputLen) {
            if (this.aad == null) {
                this.aad = Arrays.copyOfRange(input, inputOffset, inputOffset + inputLen);
                return;
            }
            byte[] newaad = new byte[(this.aad.length + inputLen)];
            System.arraycopy(this.aad, 0, newaad, 0, this.aad.length);
            System.arraycopy(input, inputOffset, newaad, this.aad.length, inputLen);
            this.aad = newaad;
        }

        /* JADX WARNING: Removed duplicated region for block: B:10:0x0022 A:{ExcHandler: java.security.NoSuchAlgorithmException (e java.security.NoSuchAlgorithmException), Splitter: B:7:0x0017} */
        /* JADX WARNING: Missing block: B:11:0x0023, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected AlgorithmParameters engineGetParameters() {
            if (this.iv == null) {
                return null;
            }
            AlgorithmParameterSpec spec = Platform.toGCMParameterSpec(this.tagLengthInBytes * 8, this.iv);
            if (spec == null) {
                return super.engineGetParameters();
            }
            try {
                AlgorithmParameters params = AlgorithmParameters.getInstance("GCM");
                params.init(spec);
                return params;
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }

    public static abstract class EVP_CIPHER extends OpenSSLCipher {
        protected boolean calledUpdate;
        private final EVP_CIPHER_CTX cipherCtx = new EVP_CIPHER_CTX(NativeCrypto.EVP_CIPHER_CTX_new());
        private int modeBlockSize;

        protected static abstract class AES_BASE extends EVP_CIPHER {
            private static final /* synthetic */ int[] -com-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues = null;
            private static final /* synthetic */ int[] -com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues = null;
            private static final int AES_BLOCK_SIZE = 16;

            private static /* synthetic */ int[] -getcom-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues() {
                if (-com-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues != null) {
                    return -com-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues;
                }
                int[] iArr = new int[Mode.values().length];
                try {
                    iArr[Mode.CBC.ordinal()] = 1;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[Mode.CTR.ordinal()] = 2;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[Mode.ECB.ordinal()] = 3;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[Mode.GCM.ordinal()] = 6;
                } catch (NoSuchFieldError e4) {
                }
                -com-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues = iArr;
                return iArr;
            }

            private static /* synthetic */ int[] -getcom-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues() {
                if (-com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues != null) {
                    return -com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues;
                }
                int[] iArr = new int[Padding.values().length];
                try {
                    iArr[Padding.ISO10126PADDING.ordinal()] = 6;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[Padding.NOPADDING.ordinal()] = 1;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[Padding.PKCS5PADDING.ordinal()] = 2;
                } catch (NoSuchFieldError e3) {
                }
                -com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues = iArr;
                return iArr;
            }

            protected AES_BASE(Mode mode, Padding padding) {
                super(mode, padding);
            }

            protected void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
                switch (-getcom-android-org-conscrypt-OpenSSLCipher$ModeSwitchesValues()[mode.ordinal()]) {
                    case 1:
                    case 2:
                    case 3:
                        return;
                    default:
                        throw new NoSuchAlgorithmException("Unsupported mode " + mode.toString());
                }
            }

            protected void checkSupportedPadding(Padding padding) throws NoSuchPaddingException {
                switch (-getcom-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues()[padding.ordinal()]) {
                    case 1:
                    case 2:
                        return;
                    default:
                        throw new NoSuchPaddingException("Unsupported padding " + padding.toString());
                }
            }

            protected String getBaseCipherName() {
                return "AES";
            }

            protected String getCipherName(int keyLength, Mode mode) {
                return "aes-" + (keyLength * 8) + "-" + mode.toString().toLowerCase(Locale.US);
            }

            protected int getCipherBlockSize() {
                return 16;
            }
        }

        public static class AES extends AES_BASE {

            public static class CBC extends AES {

                public static class NoPadding extends CBC {
                    public NoPadding() {
                        super(Padding.NOPADDING);
                    }
                }

                public static class PKCS5Padding extends CBC {
                    public PKCS5Padding() {
                        super(Padding.PKCS5PADDING);
                    }
                }

                public CBC(Padding padding) {
                    super(Mode.CBC, padding);
                }
            }

            public static class CTR extends AES {
                public CTR() {
                    super(Mode.CTR, Padding.NOPADDING);
                }
            }

            public static class ECB extends AES {

                public static class NoPadding extends ECB {
                    public NoPadding() {
                        super(Padding.NOPADDING);
                    }
                }

                public static class PKCS5Padding extends ECB {
                    public PKCS5Padding() {
                        super(Padding.PKCS5PADDING);
                    }
                }

                public ECB(Padding padding) {
                    super(Mode.ECB, padding);
                }
            }

            protected AES(Mode mode, Padding padding) {
                super(mode, padding);
            }

            protected void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                switch (keyLength) {
                    case 16:
                    case 24:
                    case 32:
                        return;
                    default:
                        throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes");
                }
            }
        }

        public static class AES_128 extends AES_BASE {

            public static class CBC extends AES_128 {

                public static class NoPadding extends CBC {
                    public NoPadding() {
                        super(Padding.NOPADDING);
                    }
                }

                public static class PKCS5Padding extends CBC {
                    public PKCS5Padding() {
                        super(Padding.PKCS5PADDING);
                    }
                }

                public CBC(Padding padding) {
                    super(Mode.CBC, padding);
                }
            }

            public static class CTR extends AES_128 {
                public CTR() {
                    super(Mode.CTR, Padding.NOPADDING);
                }
            }

            public static class ECB extends AES_128 {

                public static class NoPadding extends ECB {
                    public NoPadding() {
                        super(Padding.NOPADDING);
                    }
                }

                public static class PKCS5Padding extends ECB {
                    public PKCS5Padding() {
                        super(Padding.PKCS5PADDING);
                    }
                }

                public ECB(Padding padding) {
                    super(Mode.ECB, padding);
                }
            }

            protected AES_128(Mode mode, Padding padding) {
                super(mode, padding);
            }

            protected void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                if (keyLength != 16) {
                    throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes");
                }
            }
        }

        public static class AES_256 extends AES_BASE {

            public static class CBC extends AES_256 {

                public static class NoPadding extends CBC {
                    public NoPadding() {
                        super(Padding.NOPADDING);
                    }
                }

                public static class PKCS5Padding extends CBC {
                    public PKCS5Padding() {
                        super(Padding.PKCS5PADDING);
                    }
                }

                public CBC(Padding padding) {
                    super(Mode.CBC, padding);
                }
            }

            public static class CTR extends AES_256 {
                public CTR() {
                    super(Mode.CTR, Padding.NOPADDING);
                }
            }

            public static class ECB extends AES_256 {

                public static class NoPadding extends ECB {
                    public NoPadding() {
                        super(Padding.NOPADDING);
                    }
                }

                public static class PKCS5Padding extends ECB {
                    public PKCS5Padding() {
                        super(Padding.PKCS5PADDING);
                    }
                }

                public ECB(Padding padding) {
                    super(Mode.ECB, padding);
                }
            }

            protected AES_256(Mode mode, Padding padding) {
                super(mode, padding);
            }

            protected void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                if (keyLength != 32) {
                    throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes");
                }
            }
        }

        public static class ARC4 extends EVP_CIPHER {
            public ARC4() {
                super(Mode.ECB, Padding.NOPADDING);
            }

            protected String getBaseCipherName() {
                return "ARCFOUR";
            }

            protected String getCipherName(int keySize, Mode mode) {
                return "rc4";
            }

            protected void checkSupportedKeySize(int keySize) throws InvalidKeyException {
            }

            protected void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
                throw new NoSuchAlgorithmException("ARC4 does not support modes");
            }

            protected void checkSupportedPadding(Padding padding) throws NoSuchPaddingException {
                throw new NoSuchPaddingException("ARC4 does not support padding");
            }

            protected int getCipherBlockSize() {
                return 0;
            }

            protected boolean supportsVariableSizeKey() {
                return true;
            }
        }

        public static class DESEDE extends EVP_CIPHER {
            private static final /* synthetic */ int[] -com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues = null;
            private static final int DES_BLOCK_SIZE = 8;

            public static class CBC extends DESEDE {

                public static class NoPadding extends CBC {
                    public NoPadding() {
                        super(Padding.NOPADDING);
                    }
                }

                public static class PKCS5Padding extends CBC {
                    public PKCS5Padding() {
                        super(Padding.PKCS5PADDING);
                    }
                }

                public CBC(Padding padding) {
                    super(Mode.CBC, padding);
                }
            }

            private static /* synthetic */ int[] -getcom-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues() {
                if (-com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues != null) {
                    return -com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues;
                }
                int[] iArr = new int[Padding.values().length];
                try {
                    iArr[Padding.ISO10126PADDING.ordinal()] = 3;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[Padding.NOPADDING.ordinal()] = 1;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[Padding.PKCS5PADDING.ordinal()] = 2;
                } catch (NoSuchFieldError e3) {
                }
                -com-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues = iArr;
                return iArr;
            }

            public DESEDE(Mode mode, Padding padding) {
                super(mode, padding);
            }

            protected String getBaseCipherName() {
                return "DESede";
            }

            protected String getCipherName(int keySize, Mode mode) {
                String baseCipherName;
                if (keySize == 16) {
                    baseCipherName = "des-ede";
                } else {
                    baseCipherName = "des-ede3";
                }
                return baseCipherName + "-" + mode.toString().toLowerCase(Locale.US);
            }

            protected void checkSupportedKeySize(int keySize) throws InvalidKeyException {
                if (keySize != 16 && keySize != 24) {
                    throw new InvalidKeyException("key size must be 128 or 192 bits");
                }
            }

            protected void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
                if (mode != Mode.CBC) {
                    throw new NoSuchAlgorithmException("Unsupported mode " + mode.toString());
                }
            }

            protected void checkSupportedPadding(Padding padding) throws NoSuchPaddingException {
                switch (-getcom-android-org-conscrypt-OpenSSLCipher$PaddingSwitchesValues()[padding.ordinal()]) {
                    case 1:
                    case 2:
                        return;
                    default:
                        throw new NoSuchPaddingException("Unsupported padding " + padding.toString());
                }
            }

            protected int getCipherBlockSize() {
                return 8;
            }
        }

        protected abstract String getCipherName(int i, Mode mode);

        public EVP_CIPHER(Mode mode, Padding padding) {
            super(mode, padding);
        }

        protected void engineInitInternal(byte[] encodedKey, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            byte[] iv;
            if (params instanceof IvParameterSpec) {
                iv = ((IvParameterSpec) params).getIV();
            } else {
                iv = null;
            }
            long cipherType = NativeCrypto.EVP_get_cipherbyname(getCipherName(encodedKey.length, this.mode));
            if (cipherType == 0) {
                throw new InvalidAlgorithmParameterException("Cannot find name for key length = " + (encodedKey.length * 8) + " and mode = " + this.mode);
            }
            boolean encrypting = isEncrypting();
            int expectedIvLength = NativeCrypto.EVP_CIPHER_iv_length(cipherType);
            if (iv != null || expectedIvLength == 0) {
                if (expectedIvLength == 0 && iv != null) {
                    throw new InvalidAlgorithmParameterException("IV not used in " + this.mode + " mode");
                } else if (!(iv == null || iv.length == expectedIvLength)) {
                    throw new InvalidAlgorithmParameterException("expected IV length of " + expectedIvLength + " but was " + iv.length);
                }
            } else if (encrypting) {
                iv = new byte[expectedIvLength];
                if (random != null) {
                    random.nextBytes(iv);
                } else {
                    NativeCrypto.RAND_bytes(iv);
                }
            } else {
                throw new InvalidAlgorithmParameterException("IV must be specified in " + this.mode + " mode");
            }
            this.iv = iv;
            if (supportsVariableSizeKey()) {
                NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, cipherType, null, null, encrypting);
                NativeCrypto.EVP_CIPHER_CTX_set_key_length(this.cipherCtx, encodedKey.length);
                NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, 0, encodedKey, iv, isEncrypting());
            } else {
                NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, cipherType, encodedKey, iv, encrypting);
            }
            NativeCrypto.EVP_CIPHER_CTX_set_padding(this.cipherCtx, getPadding() == Padding.PKCS5PADDING);
            this.modeBlockSize = NativeCrypto.EVP_CIPHER_CTX_block_size(this.cipherCtx);
            this.calledUpdate = false;
        }

        protected int updateInternal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset, int maximumLen) throws ShortBufferException {
            int intialOutputOffset = outputOffset;
            int bytesLeft = output.length - outputOffset;
            if (bytesLeft < maximumLen) {
                throw new ShortBufferException("output buffer too small during update: " + bytesLeft + " < " + maximumLen);
            }
            outputOffset += NativeCrypto.EVP_CipherUpdate(this.cipherCtx, output, outputOffset, input, inputOffset, inputLen);
            this.calledUpdate = true;
            return outputOffset - intialOutputOffset;
        }

        protected int doFinalInternal(byte[] output, int outputOffset, int maximumLen) throws IllegalBlockSizeException, BadPaddingException, ShortBufferException {
            int initialOutputOffset = outputOffset;
            if (!isEncrypting() && (this.calledUpdate ^ 1) != 0) {
                return 0;
            }
            int writtenBytes;
            int bytesLeft = output.length - outputOffset;
            if (bytesLeft >= maximumLen) {
                writtenBytes = NativeCrypto.EVP_CipherFinal_ex(this.cipherCtx, output, outputOffset);
            } else {
                byte[] lastBlock = new byte[maximumLen];
                writtenBytes = NativeCrypto.EVP_CipherFinal_ex(this.cipherCtx, lastBlock, 0);
                if (writtenBytes > bytesLeft) {
                    throw new ShortBufferException("buffer is too short: " + writtenBytes + " > " + bytesLeft);
                } else if (writtenBytes > 0) {
                    System.arraycopy(lastBlock, 0, output, outputOffset, writtenBytes);
                }
            }
            outputOffset += writtenBytes;
            reset();
            return outputOffset - initialOutputOffset;
        }

        protected int getOutputSizeForFinal(int inputLen) {
            int i = 0;
            if (this.modeBlockSize == 1) {
                return inputLen;
            }
            int buffered = NativeCrypto.get_EVP_CIPHER_CTX_buf_len(this.cipherCtx);
            if (getPadding() == Padding.NOPADDING) {
                return buffered + inputLen;
            }
            int i2;
            int i3 = inputLen + buffered;
            if (NativeCrypto.get_EVP_CIPHER_CTX_final_used(this.cipherCtx)) {
                i2 = this.modeBlockSize;
            } else {
                i2 = 0;
            }
            int totalLen = i3 + i2;
            if (totalLen % this.modeBlockSize != 0 || isEncrypting()) {
                i = this.modeBlockSize;
            }
            totalLen += i;
            return totalLen - (totalLen % this.modeBlockSize);
        }

        protected int getOutputSizeForUpdate(int inputLen) {
            return getOutputSizeForFinal(inputLen);
        }

        private void reset() {
            NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, 0, this.encodedKey, this.iv, isEncrypting());
            this.calledUpdate = false;
        }
    }

    protected enum Mode {
        CBC,
        CTR,
        ECB,
        GCM
    }

    protected enum Padding {
        NOPADDING,
        PKCS5PADDING,
        ISO10126PADDING
    }

    protected abstract void checkSupportedKeySize(int i) throws InvalidKeyException;

    protected abstract void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException;

    protected abstract void checkSupportedPadding(Padding padding) throws NoSuchPaddingException;

    protected abstract int doFinalInternal(byte[] bArr, int i, int i2) throws IllegalBlockSizeException, BadPaddingException, ShortBufferException;

    protected abstract void engineInitInternal(byte[] bArr, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException;

    protected abstract String getBaseCipherName();

    protected abstract int getCipherBlockSize();

    protected abstract int getOutputSizeForFinal(int i);

    protected abstract int getOutputSizeForUpdate(int i);

    protected abstract int updateInternal(byte[] bArr, int i, int i2, byte[] bArr2, int i3, int i4) throws ShortBufferException;

    protected OpenSSLCipher() {
    }

    protected OpenSSLCipher(Mode mode, Padding padding) {
        this.mode = mode;
        this.padding = padding;
        this.blockSize = getCipherBlockSize();
    }

    protected boolean supportsVariableSizeKey() {
        return false;
    }

    protected boolean supportsVariableSizeIv() {
        return false;
    }

    protected void engineSetMode(String modeStr) throws NoSuchAlgorithmException {
        try {
            Mode mode = Mode.valueOf(modeStr.toUpperCase(Locale.US));
            checkSupportedMode(mode);
            this.mode = mode;
        } catch (IllegalArgumentException e) {
            NoSuchAlgorithmException newE = new NoSuchAlgorithmException("No such mode: " + modeStr);
            newE.initCause(e);
            throw newE;
        }
    }

    protected void engineSetPadding(String paddingStr) throws NoSuchPaddingException {
        try {
            Padding padding = Padding.valueOf(paddingStr.toUpperCase(Locale.US));
            checkSupportedPadding(padding);
            this.padding = padding;
        } catch (IllegalArgumentException e) {
            NoSuchPaddingException newE = new NoSuchPaddingException("No such padding: " + paddingStr);
            newE.initCause(e);
            throw newE;
        }
    }

    protected Padding getPadding() {
        return this.padding;
    }

    protected int engineGetBlockSize() {
        return this.blockSize;
    }

    protected int engineGetOutputSize(int inputLen) {
        return getOutputSizeForFinal(inputLen);
    }

    protected byte[] engineGetIV() {
        return this.iv;
    }

    protected AlgorithmParameters engineGetParameters() {
        if (this.iv == null || this.iv.length <= 0) {
            return null;
        }
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance(getBaseCipherName());
            params.init(this.iv);
            return params;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (IOException e2) {
            return null;
        }
    }

    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        checkAndSetEncodedKey(opmode, key);
        try {
            engineInitInternal(this.encodedKey, null, random);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        checkAndSetEncodedKey(opmode, key);
        engineInitInternal(this.encodedKey, params, random);
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        AlgorithmParameterSpec spec;
        if (params != null) {
            try {
                spec = params.getParameterSpec(IvParameterSpec.class);
            } catch (InvalidParameterSpecException e) {
                throw new InvalidAlgorithmParameterException("Params must be convertible to IvParameterSpec", e);
            }
        }
        spec = null;
        engineInit(opmode, key, spec, random);
    }

    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        byte[] output;
        int maximumLen = getOutputSizeForUpdate(inputLen);
        if (maximumLen > 0) {
            output = new byte[maximumLen];
        } else {
            output = EmptyArray.BYTE;
        }
        try {
            int bytesWritten = updateInternal(input, inputOffset, inputLen, output, 0, maximumLen);
            if (output.length == bytesWritten) {
                return output;
            }
            if (bytesWritten == 0) {
                return EmptyArray.BYTE;
            }
            return Arrays.copyOfRange(output, 0, bytesWritten);
        } catch (ShortBufferException e) {
            throw new RuntimeException("calculated buffer size was wrong: " + maximumLen);
        }
    }

    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        return updateInternal(input, inputOffset, inputLen, output, outputOffset, getOutputSizeForUpdate(inputLen));
    }

    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        int bytesWritten;
        int maximumLen = getOutputSizeForFinal(inputLen);
        byte[] output = new byte[maximumLen];
        if (inputLen > 0) {
            try {
                bytesWritten = updateInternal(input, inputOffset, inputLen, output, 0, maximumLen);
            } catch (ShortBufferException e) {
                throw new RuntimeException("our calculated buffer was too small", e);
            }
        }
        bytesWritten = 0;
        try {
            bytesWritten += doFinalInternal(output, bytesWritten, maximumLen - bytesWritten);
            if (bytesWritten == output.length) {
                return output;
            }
            if (bytesWritten == 0) {
                return EmptyArray.BYTE;
            }
            return Arrays.copyOfRange(output, 0, bytesWritten);
        } catch (ShortBufferException e2) {
            throw new RuntimeException("our calculated buffer was too small", e2);
        }
    }

    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        if (output == null) {
            throw new NullPointerException("output == null");
        }
        int bytesWritten;
        int maximumLen = getOutputSizeForFinal(inputLen);
        if (inputLen > 0) {
            bytesWritten = updateInternal(input, inputOffset, inputLen, output, outputOffset, maximumLen);
            outputOffset += bytesWritten;
            maximumLen -= bytesWritten;
        } else {
            bytesWritten = 0;
        }
        return doFinalInternal(output, outputOffset, maximumLen) + bytesWritten;
    }

    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        try {
            byte[] encoded = key.getEncoded();
            return engineDoFinal(encoded, 0, encoded.length);
        } catch (BadPaddingException e) {
            IllegalBlockSizeException newE = new IllegalBlockSizeException();
            newE.initCause(e);
            throw newE;
        }
    }

    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        try {
            byte[] encoded = engineDoFinal(wrappedKey, 0, wrappedKey.length);
            if (wrappedKeyType == 1) {
                return KeyFactory.getInstance(wrappedKeyAlgorithm).generatePublic(new X509EncodedKeySpec(encoded));
            }
            if (wrappedKeyType == 2) {
                return KeyFactory.getInstance(wrappedKeyAlgorithm).generatePrivate(new PKCS8EncodedKeySpec(encoded));
            }
            if (wrappedKeyType == 3) {
                return new SecretKeySpec(encoded, wrappedKeyAlgorithm);
            }
            throw new UnsupportedOperationException("wrappedKeyType == " + wrappedKeyType);
        } catch (IllegalBlockSizeException e) {
            throw new InvalidKeyException(e);
        } catch (BadPaddingException e2) {
            throw new InvalidKeyException(e2);
        } catch (InvalidKeySpecException e3) {
            throw new InvalidKeyException(e3);
        }
    }

    private byte[] checkAndSetEncodedKey(int opmode, Key key) throws InvalidKeyException {
        if (opmode == 1 || opmode == 3) {
            this.encrypting = true;
        } else if (opmode == 2 || opmode == 4) {
            this.encrypting = false;
        } else {
            throw new InvalidParameterException("Unsupported opmode " + opmode);
        }
        if (key instanceof SecretKey) {
            byte[] encodedKey = key.getEncoded();
            if (encodedKey == null) {
                throw new InvalidKeyException("key.getEncoded() == null");
            }
            checkSupportedKeySize(encodedKey.length);
            this.encodedKey = encodedKey;
            return encodedKey;
        }
        throw new InvalidKeyException("Only SecretKey is supported");
    }

    protected boolean isEncrypting() {
        return this.encrypting;
    }
}
