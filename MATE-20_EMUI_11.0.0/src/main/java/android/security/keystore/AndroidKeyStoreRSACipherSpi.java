package android.security.keystore;

import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keystore.KeyProperties;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

abstract class AndroidKeyStoreRSACipherSpi extends AndroidKeyStoreCipherSpiBase {
    private final int mKeymasterPadding;
    private int mKeymasterPaddingOverride;
    private int mModulusSizeBytes = -1;

    public static final class NoPadding extends AndroidKeyStoreRSACipherSpi {
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
        public /* bridge */ /* synthetic */ void finalize() throws Throwable {
            AndroidKeyStoreRSACipherSpi.super.finalize();
        }

        public NoPadding() {
            super(1);
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreRSACipherSpi
        public boolean adjustConfigForEncryptingWithPrivateKey() {
            setKeymasterPurposeOverride(2);
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public void initAlgorithmSpecificParameters() throws InvalidKeyException {
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public void initAlgorithmSpecificParameters(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                throw new InvalidAlgorithmParameterException("Unexpected parameters: " + params + ". No parameters supported");
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public void initAlgorithmSpecificParameters(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                throw new InvalidAlgorithmParameterException("Unexpected parameters: " + params + ". No parameters supported");
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, javax.crypto.CipherSpi
        public AlgorithmParameters engineGetParameters() {
            return null;
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final int getAdditionalEntropyAmountForBegin() {
            return 0;
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final int getAdditionalEntropyAmountForFinish() {
            return 0;
        }
    }

    public static final class PKCS1Padding extends AndroidKeyStoreRSACipherSpi {
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
        public /* bridge */ /* synthetic */ void finalize() throws Throwable {
            AndroidKeyStoreRSACipherSpi.super.finalize();
        }

        public PKCS1Padding() {
            super(4);
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreRSACipherSpi
        public boolean adjustConfigForEncryptingWithPrivateKey() {
            setKeymasterPurposeOverride(2);
            setKeymasterPaddingOverride(5);
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public void initAlgorithmSpecificParameters() throws InvalidKeyException {
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public void initAlgorithmSpecificParameters(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                throw new InvalidAlgorithmParameterException("Unexpected parameters: " + params + ". No parameters supported");
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public void initAlgorithmSpecificParameters(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                throw new InvalidAlgorithmParameterException("Unexpected parameters: " + params + ". No parameters supported");
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, javax.crypto.CipherSpi
        public AlgorithmParameters engineGetParameters() {
            return null;
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final int getAdditionalEntropyAmountForBegin() {
            return 0;
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final int getAdditionalEntropyAmountForFinish() {
            if (isEncrypting()) {
                return getModulusSizeBytes();
            }
            return 0;
        }
    }

    static abstract class OAEPWithMGF1Padding extends AndroidKeyStoreRSACipherSpi {
        private static final String MGF_ALGORITGM_MGF1 = "MGF1";
        private int mDigestOutputSizeBytes;
        private int mKeymasterDigest = -1;

        OAEPWithMGF1Padding(int keymasterDigest) {
            super(2);
            this.mKeymasterDigest = keymasterDigest;
            this.mDigestOutputSizeBytes = (KeymasterUtils.getDigestOutputSizeBits(keymasterDigest) + 7) / 8;
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final void initAlgorithmSpecificParameters() throws InvalidKeyException {
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final void initAlgorithmSpecificParameters(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                if (params instanceof OAEPParameterSpec) {
                    OAEPParameterSpec spec = (OAEPParameterSpec) params;
                    if (MGF_ALGORITGM_MGF1.equalsIgnoreCase(spec.getMGFAlgorithm())) {
                        String jcaDigest = spec.getDigestAlgorithm();
                        try {
                            int keymasterDigest = KeyProperties.Digest.toKeymaster(jcaDigest);
                            if (keymasterDigest == 2 || keymasterDigest == 3 || keymasterDigest == 4 || keymasterDigest == 5 || keymasterDigest == 6) {
                                AlgorithmParameterSpec mgfParams = spec.getMGFParameters();
                                if (mgfParams == null) {
                                    throw new InvalidAlgorithmParameterException("MGF parameters must be provided");
                                } else if (mgfParams instanceof MGF1ParameterSpec) {
                                    String mgf1JcaDigest = ((MGF1ParameterSpec) mgfParams).getDigestAlgorithm();
                                    if (KeyProperties.DIGEST_SHA1.equalsIgnoreCase(mgf1JcaDigest)) {
                                        PSource pSource = spec.getPSource();
                                        if (pSource instanceof PSource.PSpecified) {
                                            byte[] pSourceValue = ((PSource.PSpecified) pSource).getValue();
                                            if (pSourceValue == null || pSourceValue.length <= 0) {
                                                this.mKeymasterDigest = keymasterDigest;
                                                this.mDigestOutputSizeBytes = (KeymasterUtils.getDigestOutputSizeBits(keymasterDigest) + 7) / 8;
                                                return;
                                            }
                                            throw new InvalidAlgorithmParameterException("Unsupported source of encoding input P: " + pSource + ". Only pSpecifiedEmpty (PSource.PSpecified.DEFAULT) supported");
                                        }
                                        throw new InvalidAlgorithmParameterException("Unsupported source of encoding input P: " + pSource + ". Only pSpecifiedEmpty (PSource.PSpecified.DEFAULT) supported");
                                    }
                                    throw new InvalidAlgorithmParameterException("Unsupported MGF1 digest: " + mgf1JcaDigest + ". Only " + KeyProperties.DIGEST_SHA1 + " supported");
                                } else {
                                    throw new InvalidAlgorithmParameterException("Unsupported MGF parameters: " + mgfParams + ". Only MGF1ParameterSpec supported");
                                }
                            } else {
                                throw new InvalidAlgorithmParameterException("Unsupported digest: " + jcaDigest);
                            }
                        } catch (IllegalArgumentException e) {
                            throw new InvalidAlgorithmParameterException("Unsupported digest: " + jcaDigest, e);
                        }
                    } else {
                        throw new InvalidAlgorithmParameterException("Unsupported MGF: " + spec.getMGFAlgorithm() + ". Only " + MGF_ALGORITGM_MGF1 + " supported");
                    }
                } else {
                    throw new InvalidAlgorithmParameterException("Unsupported parameter spec: " + params + ". Only OAEPParameterSpec supported");
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final void initAlgorithmSpecificParameters(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                try {
                    OAEPParameterSpec spec = (OAEPParameterSpec) params.getParameterSpec(OAEPParameterSpec.class);
                    if (spec != null) {
                        initAlgorithmSpecificParameters(spec);
                        return;
                    }
                    throw new InvalidAlgorithmParameterException("OAEP parameters required, but not provided in parameters: " + params);
                } catch (InvalidParameterSpecException e) {
                    throw new InvalidAlgorithmParameterException("OAEP parameters required, but not found in parameters: " + params, e);
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, javax.crypto.CipherSpi
        public final AlgorithmParameters engineGetParameters() {
            OAEPParameterSpec spec = new OAEPParameterSpec(KeyProperties.Digest.fromKeymaster(this.mKeymasterDigest), MGF_ALGORITGM_MGF1, MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
            try {
                AlgorithmParameters params = AlgorithmParameters.getInstance("OAEP");
                params.init(spec);
                return params;
            } catch (NoSuchAlgorithmException e) {
                throw new ProviderException("Failed to obtain OAEP AlgorithmParameters", e);
            } catch (InvalidParameterSpecException e2) {
                throw new ProviderException("Failed to initialize OAEP AlgorithmParameters with an IV", e2);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreRSACipherSpi, android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArgs) {
            AndroidKeyStoreRSACipherSpi.super.addAlgorithmSpecificParametersToBegin(keymasterArgs);
            keymasterArgs.addEnum(KeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigest);
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreRSACipherSpi, android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final void loadAlgorithmSpecificParametersFromBeginResult(KeymasterArguments keymasterArgs) {
            AndroidKeyStoreRSACipherSpi.super.loadAlgorithmSpecificParametersFromBeginResult(keymasterArgs);
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final int getAdditionalEntropyAmountForBegin() {
            return 0;
        }

        /* access modifiers changed from: protected */
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
        public final int getAdditionalEntropyAmountForFinish() {
            if (isEncrypting()) {
                return this.mDigestOutputSizeBytes;
            }
            return 0;
        }
    }

    public static class OAEPWithSHA1AndMGF1Padding extends OAEPWithMGF1Padding {
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
        public /* bridge */ /* synthetic */ void finalize() throws Throwable {
            super.finalize();
        }

        public OAEPWithSHA1AndMGF1Padding() {
            super(2);
        }
    }

    public static class OAEPWithSHA224AndMGF1Padding extends OAEPWithMGF1Padding {
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
        public /* bridge */ /* synthetic */ void finalize() throws Throwable {
            super.finalize();
        }

        public OAEPWithSHA224AndMGF1Padding() {
            super(3);
        }
    }

    public static class OAEPWithSHA256AndMGF1Padding extends OAEPWithMGF1Padding {
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
        public /* bridge */ /* synthetic */ void finalize() throws Throwable {
            super.finalize();
        }

        public OAEPWithSHA256AndMGF1Padding() {
            super(4);
        }
    }

    public static class OAEPWithSHA384AndMGF1Padding extends OAEPWithMGF1Padding {
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
        public /* bridge */ /* synthetic */ void finalize() throws Throwable {
            super.finalize();
        }

        public OAEPWithSHA384AndMGF1Padding() {
            super(5);
        }
    }

    public static class OAEPWithSHA512AndMGF1Padding extends OAEPWithMGF1Padding {
        @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase, java.lang.Object
        public /* bridge */ /* synthetic */ void finalize() throws Throwable {
            super.finalize();
        }

        public OAEPWithSHA512AndMGF1Padding() {
            super(6);
        }
    }

    AndroidKeyStoreRSACipherSpi(int keymasterPadding) {
        this.mKeymasterPadding = keymasterPadding;
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public final void initKey(int opmode, Key key) throws InvalidKeyException {
        AndroidKeyStoreKey keystoreKey;
        if (key == null) {
            throw new InvalidKeyException("Unsupported key: null");
        } else if (KeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(key.getAlgorithm())) {
            if (key instanceof AndroidKeyStorePrivateKey) {
                keystoreKey = (AndroidKeyStoreKey) key;
            } else if (key instanceof AndroidKeyStorePublicKey) {
                keystoreKey = (AndroidKeyStoreKey) key;
            } else {
                throw new InvalidKeyException("Unsupported key type: " + key);
            }
            if (keystoreKey instanceof PrivateKey) {
                if (opmode != 1) {
                    if (opmode != 2) {
                        if (opmode != 3) {
                            if (opmode != 4) {
                                throw new InvalidKeyException("RSA private keys cannot be used with opmode: " + opmode);
                            }
                        }
                    }
                }
                if (!adjustConfigForEncryptingWithPrivateKey()) {
                    throw new InvalidKeyException("RSA private keys cannot be used with " + opmodeToString(opmode) + " and padding " + KeyProperties.EncryptionPadding.fromKeymaster(this.mKeymasterPadding) + ". Only RSA public keys supported for this mode");
                }
            } else if (opmode != 1) {
                if (opmode != 2) {
                    if (opmode != 3) {
                        if (opmode != 4) {
                            throw new InvalidKeyException("RSA public keys cannot be used with " + opmodeToString(opmode));
                        }
                    }
                }
                throw new InvalidKeyException("RSA public keys cannot be used with " + opmodeToString(opmode) + " and padding " + KeyProperties.EncryptionPadding.fromKeymaster(this.mKeymasterPadding) + ". Only RSA private keys supported for this opmode.");
            }
            KeyCharacteristics keyCharacteristics = new KeyCharacteristics();
            int errorCode = getKeyStore().getKeyCharacteristics(keystoreKey.getAlias(), null, null, keystoreKey.getUid(), keyCharacteristics);
            if (errorCode == 1) {
                long keySizeBits = keyCharacteristics.getUnsignedInt(KeymasterDefs.KM_TAG_KEY_SIZE, -1);
                if (keySizeBits == -1) {
                    throw new InvalidKeyException("Size of key not known");
                } else if (keySizeBits <= 2147483647L) {
                    this.mModulusSizeBytes = (int) ((7 + keySizeBits) / 8);
                    setKey(keystoreKey);
                } else {
                    throw new InvalidKeyException("Key too large: " + keySizeBits + " bits");
                }
            } else {
                throw getKeyStore().getInvalidKeyException(keystoreKey.getAlias(), keystoreKey.getUid(), errorCode);
            }
        } else {
            throw new InvalidKeyException("Unsupported key algorithm: " + key.getAlgorithm() + ". Only " + KeyProperties.KEY_ALGORITHM_RSA + " supported");
        }
    }

    /* access modifiers changed from: protected */
    public boolean adjustConfigForEncryptingWithPrivateKey() {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public final void resetAll() {
        this.mModulusSizeBytes = -1;
        this.mKeymasterPaddingOverride = -1;
        super.resetAll();
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public final void resetWhilePreservingInitState() {
        super.resetWhilePreservingInitState();
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArgs) {
        keymasterArgs.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, 1);
        int keymasterPadding = getKeymasterPaddingOverride();
        if (keymasterPadding == -1) {
            keymasterPadding = this.mKeymasterPadding;
        }
        keymasterArgs.addEnum(KeymasterDefs.KM_TAG_PADDING, keymasterPadding);
        int purposeOverride = getKeymasterPurposeOverride();
        if (purposeOverride == -1) {
            return;
        }
        if (purposeOverride == 2 || purposeOverride == 3) {
            keymasterArgs.addEnum(KeymasterDefs.KM_TAG_DIGEST, 0);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.security.keystore.AndroidKeyStoreCipherSpiBase
    public void loadAlgorithmSpecificParametersFromBeginResult(KeymasterArguments keymasterArgs) {
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetBlockSize() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineGetIV() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetOutputSize(int inputLen) {
        return getModulusSizeBytes();
    }

    /* access modifiers changed from: protected */
    public final int getModulusSizeBytes() {
        int i = this.mModulusSizeBytes;
        if (i != -1) {
            return i;
        }
        throw new IllegalStateException("Not initialized");
    }

    /* access modifiers changed from: protected */
    public final void setKeymasterPaddingOverride(int keymasterPadding) {
        this.mKeymasterPaddingOverride = keymasterPadding;
    }

    /* access modifiers changed from: protected */
    public final int getKeymasterPaddingOverride() {
        return this.mKeymasterPaddingOverride;
    }
}
