package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef;
import com.android.org.conscrypt.ct.CTConstants;
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
    byte[] encodedKey;
    private boolean encrypting;
    byte[] iv;
    Mode mode = Mode.ECB;
    private Padding padding = Padding.PKCS5PADDING;

    /* renamed from: com.android.org.conscrypt.OpenSSLCipher$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$org$conscrypt$OpenSSLCipher$Mode = new int[Mode.values().length];

        static {
            $SwitchMap$org$conscrypt$OpenSSLCipher$Padding = new int[Padding.values().length];
            try {
                $SwitchMap$org$conscrypt$OpenSSLCipher$Padding[Padding.NOPADDING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$conscrypt$OpenSSLCipher$Padding[Padding.PKCS5PADDING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$conscrypt$OpenSSLCipher$Mode[Mode.CBC.ordinal()] = 1;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$org$conscrypt$OpenSSLCipher$Mode[Mode.CTR.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$org$conscrypt$OpenSSLCipher$Mode[Mode.ECB.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public static abstract class EVP_AEAD extends OpenSSLCipher {
        private static final int DEFAULT_TAG_SIZE_BITS = 128;
        private static int lastGlobalMessageSize = 32;
        private byte[] aad;
        byte[] buf;
        int bufCount;
        long evpAead;
        private boolean mustInitialize;
        private byte[] previousIv;
        private byte[] previousKey;
        int tagLengthInBytes;

        public static abstract class AES extends EVP_AEAD {
            private static final int AES_BLOCK_SIZE = 16;

            public static class GCM extends AES {

                public static class AES_128 extends GCM {
                    /* access modifiers changed from: package-private */
                    public void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                        if (keyLength != AES.AES_BLOCK_SIZE) {
                            throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes (must be 16)");
                        }
                    }
                }

                public static class AES_256 extends GCM {
                    /* access modifiers changed from: package-private */
                    public void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                        if (keyLength != 32) {
                            throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes (must be 32)");
                        }
                    }
                }

                public GCM() {
                    super(Mode.GCM);
                }

                /* access modifiers changed from: package-private */
                public void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
                    if (mode != Mode.GCM) {
                        throw new NoSuchAlgorithmException("Mode must be GCM");
                    }
                }

                /* access modifiers changed from: protected */
                public AlgorithmParameters engineGetParameters() {
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
                        throw ((Error) new AssertionError("GCM not supported").initCause(e));
                    } catch (InvalidParameterSpecException e2) {
                        return null;
                    }
                }

                /* access modifiers changed from: protected */
                public AlgorithmParameterSpec getParameterSpec(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
                    if (params == null) {
                        return null;
                    }
                    AlgorithmParameterSpec spec = Platform.fromGCMParameters(params);
                    if (spec != null) {
                        return spec;
                    }
                    return super.getParameterSpec(params);
                }

                /* access modifiers changed from: package-private */
                public long getEVP_AEAD(int keyLength) throws InvalidKeyException {
                    if (keyLength == AES.AES_BLOCK_SIZE) {
                        return NativeCrypto.EVP_aead_aes_128_gcm();
                    }
                    if (keyLength == 32) {
                        return NativeCrypto.EVP_aead_aes_256_gcm();
                    }
                    throw new RuntimeException("Unexpected key length: " + keyLength);
                }
            }

            AES(Mode mode) {
                super(mode);
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                if (keyLength != AES_BLOCK_SIZE && keyLength != 32) {
                    throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes (must be 16 or 32)");
                }
            }

            /* access modifiers changed from: package-private */
            public String getBaseCipherName() {
                return "AES";
            }

            /* access modifiers changed from: package-private */
            public int getCipherBlockSize() {
                return AES_BLOCK_SIZE;
            }
        }

        public static class ChaCha20 extends EVP_AEAD {
            public ChaCha20() {
                super(Mode.POLY1305);
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                if (keyLength != 32) {
                    throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes (must be 32)");
                }
            }

            /* access modifiers changed from: package-private */
            public String getBaseCipherName() {
                return "ChaCha20";
            }

            /* access modifiers changed from: package-private */
            public int getCipherBlockSize() {
                return 0;
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
                if (mode != Mode.POLY1305) {
                    throw new NoSuchAlgorithmException("Mode must be Poly1305");
                }
            }

            /* access modifiers changed from: package-private */
            public long getEVP_AEAD(int keyLength) throws InvalidKeyException {
                if (keyLength == 32) {
                    return NativeCrypto.EVP_aead_chacha20_poly1305();
                }
                throw new RuntimeException("Unexpected key length: " + keyLength);
            }
        }

        /* access modifiers changed from: package-private */
        public abstract long getEVP_AEAD(int i) throws InvalidKeyException;

        public EVP_AEAD(Mode mode) {
            super(mode, Padding.NOPADDING);
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

        /* access modifiers changed from: package-private */
        public void engineInitInternal(byte[] encodedKey, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            int tagLenBits;
            byte[] iv;
            if (params == null) {
                iv = null;
                tagLenBits = 128;
            } else {
                GCMParameters gcmParams = Platform.fromGCMParameterSpec(params);
                if (gcmParams != null) {
                    byte[] iv2 = gcmParams.getIV();
                    int tagLenBits2 = gcmParams.getTLen();
                    iv = iv2;
                    tagLenBits = tagLenBits2;
                } else if (params instanceof IvParameterSpec) {
                    tagLenBits = 128;
                    iv = ((IvParameterSpec) params).getIV();
                } else {
                    tagLenBits = 128;
                    iv = null;
                }
            }
            if (tagLenBits % 8 == 0) {
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
                if (isEncrypting() && iv != null) {
                    if (this.previousKey == null || this.previousIv == null || !arraysAreEqual(this.previousKey, encodedKey) || !arraysAreEqual(this.previousIv, iv)) {
                        this.previousKey = encodedKey;
                        this.previousIv = iv;
                    } else {
                        this.mustInitialize = true;
                        throw new InvalidAlgorithmParameterException("When using AEAD key and IV must not be re-used");
                    }
                }
                this.mustInitialize = false;
                this.iv = iv;
                reset();
                return;
            }
            throw new InvalidAlgorithmParameterException("Tag length must be a multiple of 8; was " + this.tagLengthInBytes);
        }

        /* access modifiers changed from: package-private */
        public int updateInternal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset, int maximumLen) throws ShortBufferException {
            checkInitialization();
            if (this.buf != null) {
                ArrayUtils.checkOffsetAndCount(input.length, inputOffset, inputLen);
                if (inputLen > 0) {
                    expand(inputLen);
                    System.arraycopy(input, inputOffset, this.buf, this.bufCount, inputLen);
                    this.bufCount += inputLen;
                }
                return 0;
            }
            throw new IllegalStateException("Cipher not initialized");
        }

        private void throwAEADBadTagExceptionIfAvailable(String message, Throwable cause) throws BadPaddingException {
            try {
                BadPaddingException badTagException = null;
                try {
                    badTagException = (BadPaddingException) Class.forName("javax.crypto.AEADBadTagException").getConstructor(new Class[]{String.class}).newInstance(new Object[]{message});
                    badTagException.initCause(cause);
                } catch (IllegalAccessException | InstantiationException e) {
                } catch (InvocationTargetException e2) {
                    throw ((BadPaddingException) new BadPaddingException().initCause(e2.getTargetException()));
                }
                if (badTagException != null) {
                    throw badTagException;
                }
            } catch (Exception e3) {
            }
        }

        /* access modifiers changed from: package-private */
        public int doFinalInternal(byte[] output, int outputOffset, int maximumLen) throws IllegalBlockSizeException, BadPaddingException {
            int bytesWritten;
            checkInitialization();
            try {
                if (isEncrypting()) {
                    bytesWritten = NativeCrypto.EVP_AEAD_CTX_seal(this.evpAead, this.encodedKey, this.tagLengthInBytes, output, outputOffset, this.iv, this.buf, 0, this.bufCount, this.aad);
                } else {
                    bytesWritten = NativeCrypto.EVP_AEAD_CTX_open(this.evpAead, this.encodedKey, this.tagLengthInBytes, output, outputOffset, this.iv, this.buf, 0, this.bufCount, this.aad);
                }
                if (isEncrypting()) {
                    this.mustInitialize = true;
                }
                reset();
                return bytesWritten;
            } catch (BadPaddingException e) {
                throwAEADBadTagExceptionIfAvailable(e.getMessage(), e.getCause());
                throw e;
            }
        }

        /* access modifiers changed from: package-private */
        public void checkSupportedPadding(Padding padding) throws NoSuchPaddingException {
            if (padding != Padding.NOPADDING) {
                throw new NoSuchPaddingException("Must be NoPadding for AEAD ciphers");
            }
        }

        /* access modifiers changed from: package-private */
        public int getOutputSizeForUpdate(int inputLen) {
            return 0;
        }

        /* access modifiers changed from: package-private */
        public int getOutputSizeForFinal(int inputLen) {
            return this.bufCount + inputLen + (isEncrypting() ? NativeCrypto.EVP_AEAD_max_overhead(this.evpAead) : 0);
        }

        /* access modifiers changed from: protected */
        public void engineUpdateAAD(byte[] input, int inputOffset, int inputLen) {
            checkInitialization();
            if (this.aad == null) {
                this.aad = Arrays.copyOfRange(input, inputOffset, inputOffset + inputLen);
                return;
            }
            byte[] newaad = new byte[(this.aad.length + inputLen)];
            System.arraycopy(this.aad, 0, newaad, 0, this.aad.length);
            System.arraycopy(input, inputOffset, newaad, this.aad.length, inputLen);
            this.aad = newaad;
        }
    }

    public static abstract class EVP_CIPHER extends OpenSSLCipher {
        boolean calledUpdate;
        private final NativeRef.EVP_CIPHER_CTX cipherCtx = new NativeRef.EVP_CIPHER_CTX(NativeCrypto.EVP_CIPHER_CTX_new());
        private int modeBlockSize;

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

            AES(Mode mode, Padding padding) {
                super(mode, padding);
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                if (keyLength != 16 && keyLength != 24 && keyLength != 32) {
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

            AES_128(Mode mode, Padding padding) {
                super(mode, padding);
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
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

            AES_256(Mode mode, Padding padding) {
                super(mode, padding);
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedKeySize(int keyLength) throws InvalidKeyException {
                if (keyLength != 32) {
                    throw new InvalidKeyException("Unsupported key size: " + keyLength + " bytes");
                }
            }
        }

        static abstract class AES_BASE extends EVP_CIPHER {
            private static final int AES_BLOCK_SIZE = 16;

            AES_BASE(Mode mode, Padding padding) {
                super(mode, padding);
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
                switch (AnonymousClass1.$SwitchMap$org$conscrypt$OpenSSLCipher$Mode[mode.ordinal()]) {
                    case 1:
                    case 2:
                    case CTConstants.CERTIFICATE_LENGTH_BYTES /*3*/:
                        return;
                    default:
                        throw new NoSuchAlgorithmException("Unsupported mode " + mode.toString());
                }
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedPadding(Padding padding) throws NoSuchPaddingException {
                switch (padding) {
                    case NOPADDING:
                    case PKCS5PADDING:
                        return;
                    default:
                        throw new NoSuchPaddingException("Unsupported padding " + padding.toString());
                }
            }

            /* access modifiers changed from: package-private */
            public String getBaseCipherName() {
                return "AES";
            }

            /* access modifiers changed from: package-private */
            public String getCipherName(int keyLength, Mode mode) {
                return "aes-" + (keyLength * 8) + "-" + mode.toString().toLowerCase(Locale.US);
            }

            /* access modifiers changed from: package-private */
            public int getCipherBlockSize() {
                return AES_BLOCK_SIZE;
            }
        }

        public static class ARC4 extends EVP_CIPHER {
            public ARC4() {
                super(Mode.ECB, Padding.NOPADDING);
            }

            /* access modifiers changed from: package-private */
            public String getBaseCipherName() {
                return "ARCFOUR";
            }

            /* access modifiers changed from: package-private */
            public String getCipherName(int keySize, Mode mode) {
                return "rc4";
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedKeySize(int keySize) throws InvalidKeyException {
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
                if (mode != Mode.NONE && mode != Mode.ECB) {
                    throw new NoSuchAlgorithmException("Unsupported mode " + mode.toString());
                }
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedPadding(Padding padding) throws NoSuchPaddingException {
                if (padding != Padding.NOPADDING) {
                    throw new NoSuchPaddingException("Unsupported padding " + padding.toString());
                }
            }

            /* access modifiers changed from: package-private */
            public int getCipherBlockSize() {
                return 0;
            }

            /* access modifiers changed from: package-private */
            public boolean supportsVariableSizeKey() {
                return true;
            }
        }

        public static class DESEDE extends EVP_CIPHER {
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

            public DESEDE(Mode mode, Padding padding) {
                super(mode, padding);
            }

            /* access modifiers changed from: package-private */
            public String getBaseCipherName() {
                return "DESede";
            }

            /* access modifiers changed from: package-private */
            public String getCipherName(int keySize, Mode mode) {
                String baseCipherName;
                if (keySize == 16) {
                    baseCipherName = "des-ede";
                } else {
                    baseCipherName = "des-ede3";
                }
                return baseCipherName + "-" + mode.toString().toLowerCase(Locale.US);
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedKeySize(int keySize) throws InvalidKeyException {
                if (keySize != 16 && keySize != 24) {
                    throw new InvalidKeyException("key size must be 128 or 192 bits");
                }
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedMode(Mode mode) throws NoSuchAlgorithmException {
                if (mode != Mode.CBC) {
                    throw new NoSuchAlgorithmException("Unsupported mode " + mode.toString());
                }
            }

            /* access modifiers changed from: package-private */
            public void checkSupportedPadding(Padding padding) throws NoSuchPaddingException {
                switch (padding) {
                    case NOPADDING:
                    case PKCS5PADDING:
                        return;
                    default:
                        throw new NoSuchPaddingException("Unsupported padding " + padding.toString());
                }
            }

            /* access modifiers changed from: package-private */
            public int getCipherBlockSize() {
                return 8;
            }
        }

        /* access modifiers changed from: package-private */
        public abstract String getCipherName(int i, Mode mode);

        public EVP_CIPHER(Mode mode, Padding padding) {
            super(mode, padding);
        }

        /* access modifiers changed from: package-private */
        public void engineInitInternal(byte[] encodedKey, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            byte[] iv;
            byte[] bArr = encodedKey;
            AlgorithmParameterSpec algorithmParameterSpec = params;
            SecureRandom secureRandom = random;
            if (algorithmParameterSpec instanceof IvParameterSpec) {
                iv = ((IvParameterSpec) algorithmParameterSpec).getIV();
            } else {
                iv = null;
            }
            long cipherType = NativeCrypto.EVP_get_cipherbyname(getCipherName(bArr.length, this.mode));
            if (cipherType != 0) {
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
                    if (secureRandom != null) {
                        secureRandom.nextBytes(iv);
                    } else {
                        NativeCrypto.RAND_bytes(iv);
                    }
                } else {
                    throw new InvalidAlgorithmParameterException("IV must be specified in " + this.mode + " mode");
                }
                byte[] iv2 = iv;
                this.iv = iv2;
                if (supportsVariableSizeKey()) {
                    NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, cipherType, null, null, encrypting);
                    NativeCrypto.EVP_CIPHER_CTX_set_key_length(this.cipherCtx, bArr.length);
                    int i = expectedIvLength;
                    long j = cipherType;
                    NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, 0, bArr, iv2, isEncrypting());
                } else {
                    int i2 = expectedIvLength;
                    NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, cipherType, bArr, iv2, encrypting);
                }
                NativeCrypto.EVP_CIPHER_CTX_set_padding(this.cipherCtx, getPadding() == Padding.PKCS5PADDING);
                this.modeBlockSize = NativeCrypto.EVP_CIPHER_CTX_block_size(this.cipherCtx);
                this.calledUpdate = false;
                return;
            }
            throw new InvalidAlgorithmParameterException("Cannot find name for key length = " + (bArr.length * 8) + " and mode = " + this.mode);
        }

        /* access modifiers changed from: package-private */
        public int updateInternal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset, int maximumLen) throws ShortBufferException {
            int intialOutputOffset = outputOffset;
            int bytesLeft = output.length - outputOffset;
            if (bytesLeft >= maximumLen) {
                this.calledUpdate = true;
                return (outputOffset + NativeCrypto.EVP_CipherUpdate(this.cipherCtx, output, outputOffset, input, inputOffset, inputLen)) - intialOutputOffset;
            }
            throw new ShortBufferException("output buffer too small during update: " + bytesLeft + " < " + maximumLen);
        }

        /* access modifiers changed from: package-private */
        public int doFinalInternal(byte[] output, int outputOffset, int maximumLen) throws IllegalBlockSizeException, BadPaddingException, ShortBufferException {
            int writtenBytes;
            int initialOutputOffset = outputOffset;
            if (!isEncrypting() && !this.calledUpdate) {
                return 0;
            }
            int bytesLeft = output.length - outputOffset;
            if (bytesLeft >= maximumLen) {
                writtenBytes = NativeCrypto.EVP_CipherFinal_ex(this.cipherCtx, output, outputOffset);
            } else {
                byte[] lastBlock = new byte[maximumLen];
                int writtenBytes2 = NativeCrypto.EVP_CipherFinal_ex(this.cipherCtx, lastBlock, 0);
                if (writtenBytes2 <= bytesLeft) {
                    if (writtenBytes2 > 0) {
                        System.arraycopy(lastBlock, 0, output, outputOffset, writtenBytes2);
                    }
                    writtenBytes = writtenBytes2;
                } else {
                    throw new ShortBufferException("buffer is too short: " + writtenBytes2 + " > " + bytesLeft);
                }
            }
            reset();
            return (outputOffset + writtenBytes) - initialOutputOffset;
        }

        /* access modifiers changed from: package-private */
        public int getOutputSizeForFinal(int inputLen) {
            if (this.modeBlockSize == 1) {
                return inputLen;
            }
            int buffered = NativeCrypto.get_EVP_CIPHER_CTX_buf_len(this.cipherCtx);
            if (getPadding() == Padding.NOPADDING) {
                return buffered + inputLen;
            }
            int i = 0;
            int totalLen = inputLen + buffered + (NativeCrypto.get_EVP_CIPHER_CTX_final_used(this.cipherCtx) ? this.modeBlockSize : 0);
            if (totalLen % this.modeBlockSize != 0 || isEncrypting()) {
                i = this.modeBlockSize;
            }
            int totalLen2 = totalLen + i;
            return totalLen2 - (totalLen2 % this.modeBlockSize);
        }

        /* access modifiers changed from: package-private */
        public int getOutputSizeForUpdate(int inputLen) {
            return getOutputSizeForFinal(inputLen);
        }

        private void reset() {
            NativeCrypto.EVP_CipherInit_ex(this.cipherCtx, 0, this.encodedKey, this.iv, isEncrypting());
            this.calledUpdate = false;
        }
    }

    enum Mode {
        NONE,
        CBC,
        CTR,
        ECB,
        GCM,
        POLY1305
    }

    enum Padding {
        NOPADDING,
        PKCS5PADDING,
        PKCS7PADDING;

        public static Padding getNormalized(String value) {
            Padding p = valueOf(value);
            if (p == PKCS7PADDING) {
                return PKCS5PADDING;
            }
            return p;
        }
    }

    /* access modifiers changed from: package-private */
    public abstract void checkSupportedKeySize(int i) throws InvalidKeyException;

    /* access modifiers changed from: package-private */
    public abstract void checkSupportedMode(Mode mode2) throws NoSuchAlgorithmException;

    /* access modifiers changed from: package-private */
    public abstract void checkSupportedPadding(Padding padding2) throws NoSuchPaddingException;

    /* access modifiers changed from: package-private */
    public abstract int doFinalInternal(byte[] bArr, int i, int i2) throws IllegalBlockSizeException, BadPaddingException, ShortBufferException;

    /* access modifiers changed from: package-private */
    public abstract void engineInitInternal(byte[] bArr, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException;

    /* access modifiers changed from: package-private */
    public abstract String getBaseCipherName();

    /* access modifiers changed from: package-private */
    public abstract int getCipherBlockSize();

    /* access modifiers changed from: package-private */
    public abstract int getOutputSizeForFinal(int i);

    /* access modifiers changed from: package-private */
    public abstract int getOutputSizeForUpdate(int i);

    /* access modifiers changed from: package-private */
    public abstract int updateInternal(byte[] bArr, int i, int i2, byte[] bArr2, int i3, int i4) throws ShortBufferException;

    OpenSSLCipher() {
    }

    OpenSSLCipher(Mode mode2, Padding padding2) {
        this.mode = mode2;
        this.padding = padding2;
        this.blockSize = getCipherBlockSize();
    }

    /* access modifiers changed from: package-private */
    public boolean supportsVariableSizeKey() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean supportsVariableSizeIv() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void engineSetMode(String modeStr) throws NoSuchAlgorithmException {
        try {
            Mode mode2 = Mode.valueOf(modeStr.toUpperCase(Locale.US));
            checkSupportedMode(mode2);
            this.mode = mode2;
        } catch (IllegalArgumentException e) {
            NoSuchAlgorithmException newE = new NoSuchAlgorithmException("No such mode: " + modeStr);
            newE.initCause(e);
            throw newE;
        }
    }

    /* access modifiers changed from: protected */
    public void engineSetPadding(String paddingStr) throws NoSuchPaddingException {
        try {
            Padding padding2 = Padding.getNormalized(paddingStr.toUpperCase(Locale.US));
            checkSupportedPadding(padding2);
            this.padding = padding2;
        } catch (IllegalArgumentException e) {
            NoSuchPaddingException newE = new NoSuchPaddingException("No such padding: " + paddingStr);
            newE.initCause(e);
            throw newE;
        }
    }

    /* access modifiers changed from: package-private */
    public Padding getPadding() {
        return this.padding;
    }

    /* access modifiers changed from: protected */
    public int engineGetBlockSize() {
        return this.blockSize;
    }

    /* access modifiers changed from: protected */
    public int engineGetOutputSize(int inputLen) {
        return Math.max(getOutputSizeForUpdate(inputLen), getOutputSizeForFinal(inputLen));
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetIV() {
        return this.iv;
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameters engineGetParameters() {
        if (this.iv == null || this.iv.length <= 0) {
            return null;
        }
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance(getBaseCipherName());
            params.init(new IvParameterSpec(this.iv));
            return params;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (InvalidParameterSpecException e2) {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameterSpec getParameterSpec(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
        if (params == null) {
            return null;
        }
        try {
            return params.getParameterSpec(IvParameterSpec.class);
        } catch (InvalidParameterSpecException e) {
            throw new InvalidAlgorithmParameterException("Params must be convertible to IvParameterSpec", e);
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        checkAndSetEncodedKey(opmode, key);
        try {
            engineInitInternal(this.encodedKey, null, random);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        checkAndSetEncodedKey(opmode, key);
        engineInitInternal(this.encodedKey, params, random);
    }

    /* access modifiers changed from: protected */
    public void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        engineInit(opmode, key, getParameterSpec(params), random);
    }

    /* access modifiers changed from: protected */
    public byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        byte[] output;
        int maximumLen = getOutputSizeForUpdate(inputLen);
        if (maximumLen > 0) {
            output = new byte[maximumLen];
        } else {
            output = EmptyArray.BYTE;
        }
        byte[] output2 = output;
        try {
            int bytesWritten = updateInternal(input, inputOffset, inputLen, output2, 0, maximumLen);
            if (output2.length == bytesWritten) {
                return output2;
            }
            if (bytesWritten == 0) {
                return EmptyArray.BYTE;
            }
            return Arrays.copyOfRange(output2, 0, bytesWritten);
        } catch (ShortBufferException e) {
            throw new RuntimeException("calculated buffer size was wrong: " + maximumLen);
        }
    }

    /* access modifiers changed from: protected */
    public int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        return updateInternal(input, inputOffset, inputLen, output, outputOffset, getOutputSizeForUpdate(inputLen));
    }

    /* access modifiers changed from: protected */
    public byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        int bytesWritten;
        int maximumLen = getOutputSizeForFinal(inputLen);
        byte[] output = new byte[maximumLen];
        if (inputLen > 0) {
            try {
                bytesWritten = updateInternal(input, inputOffset, inputLen, output, 0, maximumLen);
            } catch (ShortBufferException e) {
                throw new RuntimeException("our calculated buffer was too small", e);
            }
        } else {
            bytesWritten = 0;
        }
        try {
            int bytesWritten2 = bytesWritten + doFinalInternal(output, bytesWritten, maximumLen - bytesWritten);
            if (bytesWritten2 == output.length) {
                return output;
            }
            if (bytesWritten2 == 0) {
                return EmptyArray.BYTE;
            }
            return Arrays.copyOfRange(output, 0, bytesWritten2);
        } catch (ShortBufferException e2) {
            throw new RuntimeException("our calculated buffer was too small", e2);
        }
    }

    /* access modifiers changed from: protected */
    public int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        int bytesWritten;
        if (output != null) {
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
        throw new NullPointerException("output == null");
    }

    /* access modifiers changed from: protected */
    public byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        try {
            byte[] encoded = key.getEncoded();
            return engineDoFinal(encoded, 0, encoded.length);
        } catch (BadPaddingException e) {
            IllegalBlockSizeException newE = new IllegalBlockSizeException();
            newE.initCause(e);
            throw newE;
        }
    }

    /* access modifiers changed from: protected */
    public Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
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

    /* access modifiers changed from: protected */
    public int engineGetKeySize(Key key) throws InvalidKeyException {
        if (key instanceof SecretKey) {
            byte[] encodedKey2 = key.getEncoded();
            if (encodedKey2 != null) {
                checkSupportedKeySize(encodedKey2.length);
                return encodedKey2.length * 8;
            }
            throw new InvalidKeyException("key.getEncoded() == null");
        }
        throw new InvalidKeyException("Only SecretKey is supported");
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
            byte[] encodedKey2 = key.getEncoded();
            if (encodedKey2 != null) {
                checkSupportedKeySize(encodedKey2.length);
                this.encodedKey = encodedKey2;
                return encodedKey2;
            }
            throw new InvalidKeyException("key.getEncoded() == null");
        }
        throw new InvalidKeyException("Only SecretKey is supported");
    }

    /* access modifiers changed from: package-private */
    public boolean isEncrypting() {
        return this.encrypting;
    }
}
