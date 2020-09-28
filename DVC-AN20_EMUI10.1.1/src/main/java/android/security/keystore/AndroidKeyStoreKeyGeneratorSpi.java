package android.security.keystore;

import android.security.Credentials;
import android.security.KeyStore;
import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keystore.KeyProperties;
import java.security.InvalidAlgorithmParameterException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import libcore.util.EmptyArray;

public abstract class AndroidKeyStoreKeyGeneratorSpi extends KeyGeneratorSpi {
    private final int mDefaultKeySizeBits;
    protected int mKeySizeBits;
    private final KeyStore mKeyStore;
    private final int mKeymasterAlgorithm;
    private int[] mKeymasterBlockModes;
    private final int mKeymasterDigest;
    private int[] mKeymasterDigests;
    private int[] mKeymasterPaddings;
    private int[] mKeymasterPurposes;
    private SecureRandom mRng;
    private KeyGenParameterSpec mSpec;

    public static class AES extends AndroidKeyStoreKeyGeneratorSpi {
        public AES() {
            super(32, 128);
        }

        /* access modifiers changed from: protected */
        @Override // javax.crypto.KeyGeneratorSpi, android.security.keystore.AndroidKeyStoreKeyGeneratorSpi
        public void engineInit(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
            AndroidKeyStoreKeyGeneratorSpi.super.engineInit(params, random);
            if (this.mKeySizeBits != 128 && this.mKeySizeBits != 192 && this.mKeySizeBits != 256) {
                throw new InvalidAlgorithmParameterException("Unsupported key size: " + this.mKeySizeBits + ". Supported: 128, 192, 256.");
            }
        }
    }

    public static class DESede extends AndroidKeyStoreKeyGeneratorSpi {
        public DESede() {
            super(33, 168);
        }
    }

    protected static abstract class HmacBase extends AndroidKeyStoreKeyGeneratorSpi {
        protected HmacBase(int keymasterDigest) {
            super(128, keymasterDigest, KeymasterUtils.getDigestOutputSizeBits(keymasterDigest));
        }
    }

    public static class HmacSHA1 extends HmacBase {
        public HmacSHA1() {
            super(2);
        }
    }

    public static class HmacSHA224 extends HmacBase {
        public HmacSHA224() {
            super(3);
        }
    }

    public static class HmacSHA256 extends HmacBase {
        public HmacSHA256() {
            super(4);
        }
    }

    public static class HmacSHA384 extends HmacBase {
        public HmacSHA384() {
            super(5);
        }
    }

    public static class HmacSHA512 extends HmacBase {
        public HmacSHA512() {
            super(6);
        }
    }

    protected AndroidKeyStoreKeyGeneratorSpi(int keymasterAlgorithm, int defaultKeySizeBits) {
        this(keymasterAlgorithm, -1, defaultKeySizeBits);
    }

    protected AndroidKeyStoreKeyGeneratorSpi(int keymasterAlgorithm, int keymasterDigest, int defaultKeySizeBits) {
        this.mKeyStore = KeyStore.getInstance();
        this.mKeymasterAlgorithm = keymasterAlgorithm;
        this.mKeymasterDigest = keymasterDigest;
        this.mDefaultKeySizeBits = defaultKeySizeBits;
        if (this.mDefaultKeySizeBits <= 0) {
            throw new IllegalArgumentException("Default key size must be positive");
        } else if (this.mKeymasterAlgorithm == 128 && this.mKeymasterDigest == -1) {
            throw new IllegalArgumentException("Digest algorithm must be specified for HMAC key");
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(SecureRandom random) {
        throw new UnsupportedOperationException("Cannot initialize without a " + KeyGenParameterSpec.class.getName() + " parameter");
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.KeyGeneratorSpi
    public void engineInit(int keySize, SecureRandom random) {
        throw new UnsupportedOperationException("Cannot initialize without a " + KeyGenParameterSpec.class.getName() + " parameter");
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.KeyGeneratorSpi
    public void engineInit(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        resetAll();
        boolean success = false;
        if (params != null) {
            try {
                if (params instanceof KeyGenParameterSpec) {
                    KeyGenParameterSpec spec = (KeyGenParameterSpec) params;
                    if (spec.getKeystoreAlias() != null) {
                        this.mRng = random;
                        this.mSpec = spec;
                        this.mKeySizeBits = spec.getKeySize() != -1 ? spec.getKeySize() : this.mDefaultKeySizeBits;
                        if (this.mKeySizeBits <= 0) {
                            throw new InvalidAlgorithmParameterException("Key size must be positive: " + this.mKeySizeBits);
                        } else if (this.mKeySizeBits % 8 == 0) {
                            this.mKeymasterPurposes = KeyProperties.Purpose.allToKeymaster(spec.getPurposes());
                            this.mKeymasterPaddings = KeyProperties.EncryptionPadding.allToKeymaster(spec.getEncryptionPaddings());
                            if (spec.getSignaturePaddings().length <= 0) {
                                this.mKeymasterBlockModes = KeyProperties.BlockMode.allToKeymaster(spec.getBlockModes());
                                if ((spec.getPurposes() & 1) != 0 && spec.isRandomizedEncryptionRequired()) {
                                    int[] iArr = this.mKeymasterBlockModes;
                                    for (int keymasterBlockMode : iArr) {
                                        if (!KeymasterUtils.isKeymasterBlockModeIndCpaCompatibleWithSymmetricCrypto(keymasterBlockMode)) {
                                            throw new InvalidAlgorithmParameterException("Randomized encryption (IND-CPA) required but may be violated by block mode: " + KeyProperties.BlockMode.fromKeymaster(keymasterBlockMode) + ". See " + KeyGenParameterSpec.class.getName() + " documentation.");
                                        }
                                    }
                                }
                                if (this.mKeymasterAlgorithm != 33 || this.mKeySizeBits == 168) {
                                    if (this.mKeymasterAlgorithm == 128) {
                                        if (this.mKeySizeBits < 64) {
                                            throw new InvalidAlgorithmParameterException("HMAC key size must be at least 64 bits.");
                                        } else if (this.mKeySizeBits <= 512 || !spec.isStrongBoxBacked()) {
                                            this.mKeymasterDigests = new int[]{this.mKeymasterDigest};
                                            if (spec.isDigestsSpecified()) {
                                                int[] keymasterDigestsFromSpec = KeyProperties.Digest.allToKeymaster(spec.getDigests());
                                                if (!(keymasterDigestsFromSpec.length == 1 && keymasterDigestsFromSpec[0] == this.mKeymasterDigest)) {
                                                    throw new InvalidAlgorithmParameterException("Unsupported digests specification: " + Arrays.asList(spec.getDigests()) + ". Only " + KeyProperties.Digest.fromKeymaster(this.mKeymasterDigest) + " supported for this HMAC key algorithm");
                                                }
                                            }
                                        } else {
                                            throw new InvalidAlgorithmParameterException("StrongBox HMAC key size must be smaller than 512 bits.");
                                        }
                                    } else if (spec.isDigestsSpecified()) {
                                        this.mKeymasterDigests = KeyProperties.Digest.allToKeymaster(spec.getDigests());
                                    } else {
                                        this.mKeymasterDigests = EmptyArray.INT;
                                    }
                                    KeymasterUtils.addUserAuthArgs(new KeymasterArguments(), spec);
                                    success = true;
                                    if (success) {
                                        return;
                                    }
                                    return;
                                }
                                throw new InvalidAlgorithmParameterException("3DES key size must be 168 bits.");
                            }
                            try {
                                throw new InvalidAlgorithmParameterException("Signature paddings not supported for symmetric key algorithms");
                            } catch (IllegalArgumentException | IllegalStateException e) {
                                throw new InvalidAlgorithmParameterException(e);
                            }
                        } else {
                            throw new InvalidAlgorithmParameterException("Key size must be a multiple of 8: " + this.mKeySizeBits);
                        }
                    } else {
                        throw new InvalidAlgorithmParameterException("KeyStore entry alias not provided");
                    }
                }
            } finally {
                if (!success) {
                    resetAll();
                }
            }
        }
        throw new InvalidAlgorithmParameterException("Cannot initialize without a " + KeyGenParameterSpec.class.getName() + " parameter");
    }

    private void resetAll() {
        this.mSpec = null;
        this.mRng = null;
        this.mKeySizeBits = -1;
        this.mKeymasterPurposes = null;
        this.mKeymasterPaddings = null;
        this.mKeymasterBlockModes = null;
    }

    /* access modifiers changed from: protected */
    public SecretKey engineGenerateKey() {
        int flags;
        KeyGenParameterSpec spec = this.mSpec;
        if (spec != null) {
            KeymasterArguments args = new KeymasterArguments();
            args.addUnsignedInt(KeymasterDefs.KM_TAG_KEY_SIZE, (long) this.mKeySizeBits);
            args.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, this.mKeymasterAlgorithm);
            args.addEnums(KeymasterDefs.KM_TAG_PURPOSE, this.mKeymasterPurposes);
            args.addEnums(KeymasterDefs.KM_TAG_BLOCK_MODE, this.mKeymasterBlockModes);
            args.addEnums(KeymasterDefs.KM_TAG_PADDING, this.mKeymasterPaddings);
            args.addEnums(KeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigests);
            KeymasterUtils.addUserAuthArgs(args, spec);
            KeymasterUtils.addMinMacLengthAuthorizationIfNecessary(args, this.mKeymasterAlgorithm, this.mKeymasterBlockModes, this.mKeymasterDigests);
            args.addDateIfNotNull(KeymasterDefs.KM_TAG_ACTIVE_DATETIME, spec.getKeyValidityStart());
            args.addDateIfNotNull(KeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, spec.getKeyValidityForOriginationEnd());
            args.addDateIfNotNull(KeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, spec.getKeyValidityForConsumptionEnd());
            if ((spec.getPurposes() & 1) != 0 && !spec.isRandomizedEncryptionRequired()) {
                args.addBoolean(KeymasterDefs.KM_TAG_CALLER_NONCE);
            }
            byte[] additionalEntropy = KeyStoreCryptoOperationUtils.getRandomBytesToMixIntoKeystoreRng(this.mRng, (this.mKeySizeBits + 7) / 8);
            if (spec.isStrongBoxBacked()) {
                flags = 0 | 16;
            } else {
                flags = 0;
            }
            String keyAliasInKeystore = Credentials.USER_PRIVATE_KEY + spec.getKeystoreAlias();
            KeyCharacteristics resultingKeyCharacteristics = new KeyCharacteristics();
            boolean success = false;
            try {
                Credentials.deleteAllTypesForAlias(this.mKeyStore, spec.getKeystoreAlias(), spec.getUid());
                int errorCode = this.mKeyStore.generateKey(keyAliasInKeystore, args, additionalEntropy, spec.getUid(), flags, resultingKeyCharacteristics);
                if (errorCode == 1) {
                    try {
                        success = true;
                        return new AndroidKeyStoreSecretKey(keyAliasInKeystore, spec.getUid(), KeyProperties.KeyAlgorithm.fromKeymasterSecretKeyAlgorithm(this.mKeymasterAlgorithm, this.mKeymasterDigest));
                    } catch (IllegalArgumentException e) {
                        throw new ProviderException("Failed to obtain JCA secret key algorithm name", e);
                    }
                } else if (errorCode == -68) {
                    throw new StrongBoxUnavailableException("Failed to generate key");
                } else {
                    throw new ProviderException("Keystore operation failed", KeyStore.getKeyStoreException(errorCode));
                }
            } finally {
                if (!success) {
                    Credentials.deleteAllTypesForAlias(this.mKeyStore, spec.getKeystoreAlias(), spec.getUid());
                }
            }
        } else {
            throw new IllegalStateException("Not initialized");
        }
    }
}
