package com.huawei.security.keystore;

import android.security.keystore.KeyGenParameterSpec;
import android.support.annotation.NonNull;
import com.huawei.security.HwCredentials;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeyCharacteristics;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keymaster.HwKeymasterUtils;
import com.huawei.security.keystore.ArrayUtils;
import com.huawei.security.keystore.HwKeyGenParameterSpec;
import com.huawei.security.keystore.HwKeyProperties;
import java.security.InvalidAlgorithmParameterException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;

public class HwUniversalKeyStoreKeyGeneratorSpi extends KeyGeneratorSpi {
    private static final String TAG = "HwUniversalKeyStoreKeyGeneratorSpi";
    private final int mDefaultKeySizeBits;
    protected int mKeySizeBits;
    private final HwKeystoreManager mKeyStore;
    private final int mKeymasterAlgorithm;
    private int[] mKeymasterBlockModes;
    private final int mKeymasterDigest;
    private int[] mKeymasterDigests;
    private int[] mKeymasterPaddings;
    private int[] mKeymasterPurposes;
    private SecureRandom mRng;
    private HwKeyGenParameterSpec mSpec;

    protected HwUniversalKeyStoreKeyGeneratorSpi(int keymasterAlgorithm, int defaultKeySizeBits) {
        this(keymasterAlgorithm, -1, defaultKeySizeBits);
    }

    protected HwUniversalKeyStoreKeyGeneratorSpi(int keymasterAlgorithm, int keymasterDigest, int defaultKeySizeBits) {
        this.mKeyStore = HwKeystoreManager.getInstance();
        this.mKeymasterDigest = keymasterDigest;
        this.mDefaultKeySizeBits = defaultKeySizeBits;
        this.mKeymasterAlgorithm = keymasterAlgorithm;
        if (this.mDefaultKeySizeBits <= 0) {
            throw new IllegalArgumentException("Default key size must set first");
        } else if (this.mKeymasterAlgorithm == 128 && this.mKeymasterDigest == -1) {
            throw new IllegalArgumentException("Digest algorithm must specify to HMAC key");
        }
    }

    /* JADX INFO: Multiple debug info for r0v4 com.huawei.security.keystore.HwKeyGenParameterSpec: [D('spec' com.huawei.security.keystore.HwKeyGenParameterSpec), D('tmpBuilder' com.huawei.security.keystore.HwKeyGenParameterSpec$Builder)] */
    @NonNull
    private HwKeyGenParameterSpec convertToHwKeyGenParamSpec(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params instanceof HwKeyGenParameterSpec) {
            return (HwKeyGenParameterSpec) params;
        }
        if (params instanceof KeyGenParameterSpec) {
            return HwKeyGenParameterSpec.getInstance(new HwKeyGenParameterSpec.Builder((KeyGenParameterSpec) params));
        }
        throw new InvalidAlgorithmParameterException("Unsupported params class: " + params.getClass().getName() + ". Supported: " + HwKeyGenParameterSpec.class.getName());
    }

    private void engineInitEx(HwKeyGenParameterSpec spec) throws InvalidAlgorithmParameterException {
        this.mKeymasterPurposes = HwKeyProperties.Purpose.allToKeymaster(spec.getPurposes());
        this.mKeymasterPaddings = HwKeyProperties.EncryptionPadding.allToKeymaster(spec.getEncryptionPaddings());
        if (spec.getSignaturePaddings().length <= 0) {
            this.mKeymasterBlockModes = HwKeyProperties.BlockMode.allToKeymaster(spec.getBlockModes());
            if ((spec.getPurposes() & 1) != 0 && spec.isRandomizedEncryptionRequired()) {
                int[] iArr = this.mKeymasterBlockModes;
                for (int keymasterBlockMode : iArr) {
                    if (!HwKeymasterUtils.isKeymasterBlockModeValidCompatibleWithSymmetricCrypto(keymasterBlockMode)) {
                        throw new InvalidAlgorithmParameterException("Randomized encryption (IND-CPA) required but may be violated by block mode: " + HwKeyProperties.BlockMode.fromKeymaster(keymasterBlockMode) + ". See " + HwKeyGenParameterSpec.class.getName() + " documentation.");
                    }
                }
            }
            int i = this.mKeymasterAlgorithm;
            if (i == 32 || i == 128) {
                if (this.mKeymasterAlgorithm == 128) {
                    int i2 = this.mKeySizeBits;
                    if (i2 < 64 || i2 > 512) {
                        throw new InvalidAlgorithmParameterException("HMAC key sizes must be within 64-512 bits, inclusive.");
                    }
                    this.mKeymasterDigests = new int[]{this.mKeymasterDigest};
                    if (spec.isDigestsSpecified()) {
                        int[] keymasterDigestsFromSpec = HwKeyProperties.Digest.allToKeymaster(spec.getDigests());
                        if (!(keymasterDigestsFromSpec.length == 1 && keymasterDigestsFromSpec[0] == this.mKeymasterDigest)) {
                            throw new InvalidAlgorithmParameterException("Unsupported digests specification: " + Arrays.asList(spec.getDigests()) + ". Only " + HwKeyProperties.Digest.fromKeymaster(this.mKeymasterDigest) + " supported for this HMAC key algorithm");
                        }
                    }
                } else if (spec.isDigestsSpecified()) {
                    this.mKeymasterDigests = HwKeyProperties.Digest.allToKeymaster(spec.getDigests());
                } else {
                    this.mKeymasterDigests = ArrayUtils.EmptyArray.INT;
                }
                HwKeymasterUtils.addUserAuthArgs(new HwKeymasterArguments(), spec, 0);
                return;
            }
            throw new InvalidAlgorithmParameterException("the Keymaster Algorithm must be AES or HMAC.");
        }
        throw new InvalidAlgorithmParameterException("Signature paddings not supported for symmetric key algorithms");
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.KeyGeneratorSpi
    public void engineInit(SecureRandom random) {
        throw new UnsupportedOperationException("Cannot initialize without a " + HwKeyGenParameterSpec.class.getName() + " parameter");
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.KeyGeneratorSpi
    public void engineInit(int keySize, SecureRandom random) {
        throw new UnsupportedOperationException("Cannot initialize without a " + HwKeyGenParameterSpec.class.getName() + " parameter");
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.KeyGeneratorSpi
    public void engineInit(AlgorithmParameterSpec algParams, SecureRandom secRandom) throws InvalidAlgorithmParameterException {
        resetAll();
        boolean flag = false;
        if (algParams != null) {
            try {
                HwKeyGenParameterSpec spec = convertToHwKeyGenParamSpec(algParams);
                if (spec.getKeystoreAlias() != null) {
                    this.mRng = secRandom;
                    this.mSpec = spec;
                    this.mKeySizeBits = spec.getKeySize() != -1 ? spec.getKeySize() : this.mDefaultKeySizeBits;
                    if (this.mKeySizeBits <= 0) {
                        throw new InvalidAlgorithmParameterException("Key size must be positive: " + this.mKeySizeBits);
                    } else if (this.mKeySizeBits % 8 == 0) {
                        try {
                            engineInitEx(spec);
                            flag = true;
                        } catch (IllegalArgumentException | IllegalStateException ex) {
                            throw new InvalidAlgorithmParameterException(ex);
                        }
                    } else {
                        throw new InvalidAlgorithmParameterException("Key size must be a multiple of 8: " + this.mKeySizeBits);
                    }
                } else {
                    throw new InvalidAlgorithmParameterException("KeyStore entry alias not provided");
                }
            } finally {
                if (!flag) {
                    resetAll();
                }
            }
        } else {
            throw new InvalidAlgorithmParameterException("Must supply params of type " + HwKeyGenParameterSpec.class.getName());
        }
    }

    private void resetAll() {
        this.mKeymasterPurposes = null;
        this.mKeymasterPaddings = null;
        this.mKeymasterBlockModes = null;
        this.mSpec = null;
        this.mRng = null;
        this.mKeySizeBits = -1;
    }

    /* access modifiers changed from: protected */
    public void addAlgorithmSpecificParameters(HwKeymasterArguments keymasterArgs) {
        keymasterArgs.addUnsignedInt(HwKeymasterDefs.KM_TAG_KEY_SIZE, (long) this.mKeySizeBits);
        keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_ALGORITHM, this.mKeymasterAlgorithm);
        keymasterArgs.addEnums(HwKeymasterDefs.KM_TAG_PURPOSE, this.mKeymasterPurposes);
        keymasterArgs.addEnums(HwKeymasterDefs.KM_TAG_BLOCK_MODE, this.mKeymasterBlockModes);
        keymasterArgs.addEnums(HwKeymasterDefs.KM_TAG_PADDING, this.mKeymasterPaddings);
        keymasterArgs.addEnums(HwKeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigests);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.KeyGeneratorSpi
    public SecretKey engineGenerateKey() {
        HwKeyGenParameterSpec spec = this.mSpec;
        if (spec != null) {
            HwKeymasterArguments args = new HwKeymasterArguments();
            addAlgorithmSpecificParameters(args);
            HwKeymasterUtils.addUserAuthArgs(args, spec, 0);
            HwKeymasterUtils.addMinMacLengthAuthorizationIfNecessary(args, this.mKeymasterAlgorithm, this.mKeymasterBlockModes, this.mKeymasterDigests);
            args.addDateIfNotNull(HwKeymasterDefs.KM_TAG_ACTIVE_DATETIME, spec.getKeyValidityStart());
            args.addDateIfNotNull(HwKeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, spec.getKeyValidityForOriginationEnd());
            args.addDateIfNotNull(HwKeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, spec.getKeyValidityForConsumptionEnd());
            if ((spec.getPurposes() & 1) != 0 && !spec.isRandomizedEncryptionRequired()) {
                args.addBoolean(HwKeymasterDefs.KM_TAG_CALLER_NONCE);
            }
            byte[] additionalEntropy = HwUniversalKeyStoreCryptoOperationUtils.getRandomBytesToMixIntoKeystoreRng(this.mRng, (this.mKeySizeBits + 7) / 8);
            String keyAliasInKeystore = HwCredentials.USER_SECRET_KEY + spec.getKeystoreAlias();
            HwKeyCharacteristics resultingKeyCharacteristics = new HwKeyCharacteristics();
            boolean success = false;
            try {
                HwCredentials.deleteAllTypesForAlias(this.mKeyStore, spec.getKeystoreAlias(), spec.getUid());
                int errorCode = this.mKeyStore.generateKey(keyAliasInKeystore, args, additionalEntropy, spec.getUid(), 0, resultingKeyCharacteristics);
                if (errorCode == 1) {
                    try {
                        success = true;
                        return new HwUniversalKeyStoreSecretKey(keyAliasInKeystore, spec.getUid(), HwKeyProperties.KeyAlgorithm.fromKeymasterSecretKeyAlgorithm(this.mKeymasterAlgorithm, this.mKeymasterDigest));
                    } catch (IllegalArgumentException e) {
                        throw new ProviderException("Failed to obtain JCA secret key algorithm name", e);
                    }
                } else {
                    throw new ProviderException("Keystore operation failed", HwKeystoreManager.getKeyStoreException(errorCode));
                }
            } finally {
                if (!success) {
                    HwCredentials.deleteAllTypesForAlias(this.mKeyStore, spec.getKeystoreAlias(), spec.getUid());
                }
            }
        } else {
            throw new IllegalStateException("Not initialized");
        }
    }

    protected static abstract class Hmac extends HwUniversalKeyStoreKeyGeneratorSpi {
        protected Hmac(int keymasterDigest) {
            super(128, keymasterDigest, HwKeymasterUtils.getDigestOutputSizeBits(keymasterDigest));
        }
    }

    public static class HmacSHA256 extends Hmac {
        public HmacSHA256() {
            super(4);
        }
    }

    public static class HmacSHA384 extends Hmac {
        public HmacSHA384() {
            super(5);
        }
    }

    public static class HmacSHA512 extends Hmac {
        public HmacSHA512() {
            super(6);
        }
    }

    public static class HmacSM3 extends Hmac {
        public HmacSM3() {
            super(7);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.security.keystore.HwUniversalKeyStoreKeyGeneratorSpi
        public void addAlgorithmSpecificParameters(HwKeymasterArguments keymasterArgs) {
            super.addAlgorithmSpecificParameters(keymasterArgs);
            keymasterArgs.addBoolean(HwKeymasterDefs.KM_TAG_IS_FROM_GM);
        }
    }

    public static class AES extends HwUniversalKeyStoreKeyGeneratorSpi {
        private static final int AES_KEY_SIZE_128 = 128;
        private static final int AES_KEY_SIZE_192 = 192;
        private static final int AES_KEY_SIZE_256 = 256;

        public AES() {
            super(32, 128);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.security.keystore.HwUniversalKeyStoreKeyGeneratorSpi, javax.crypto.KeyGeneratorSpi
        public void engineInit(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
            HwUniversalKeyStoreKeyGeneratorSpi.super.engineInit(params, random);
            if (this.mKeySizeBits != 128 && this.mKeySizeBits != AES_KEY_SIZE_192 && this.mKeySizeBits != 256) {
                throw new InvalidAlgorithmParameterException("Unsupported key size: " + this.mKeySizeBits + ". Supported: 128, 192, 256.");
            }
        }
    }

    public static class SM4 extends HwUniversalKeyStoreKeyGeneratorSpi {
        private static final int SM4_KEY_SIZE_128 = 128;

        public SM4() {
            super(32, 128);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.security.keystore.HwUniversalKeyStoreKeyGeneratorSpi, javax.crypto.KeyGeneratorSpi
        public void engineInit(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
            HwUniversalKeyStoreKeyGeneratorSpi.super.engineInit(params, random);
            if (this.mKeySizeBits != 128) {
                throw new InvalidAlgorithmParameterException("Unsupported key size: " + this.mKeySizeBits + ". SM4 Supported: 128.");
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.security.keystore.HwUniversalKeyStoreKeyGeneratorSpi
        public void addAlgorithmSpecificParameters(HwKeymasterArguments keymasterArgs) {
            HwUniversalKeyStoreKeyGeneratorSpi.super.addAlgorithmSpecificParameters(keymasterArgs);
            keymasterArgs.addBoolean(HwKeymasterDefs.KM_TAG_IS_FROM_GM);
        }
    }
}
