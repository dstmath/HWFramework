package android.security.keystore;

import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterDefs;
import android.security.keystore.KeyProperties.Digest;
import android.security.keystore.KeyProperties.EncryptionPadding;
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
import javax.crypto.spec.PSource.PSpecified;

abstract class AndroidKeyStoreRSACipherSpi extends AndroidKeyStoreCipherSpiBase {
    private final int mKeymasterPadding;
    private int mKeymasterPaddingOverride;
    private int mModulusSizeBytes = -1;

    public static final class NoPadding extends AndroidKeyStoreRSACipherSpi {
        public NoPadding() {
            super(1);
        }

        protected boolean adjustConfigForEncryptingWithPrivateKey() {
            setKeymasterPurposeOverride(2);
            return true;
        }

        protected void initAlgorithmSpecificParameters() throws InvalidKeyException {
        }

        protected void initAlgorithmSpecificParameters(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                throw new InvalidAlgorithmParameterException("Unexpected parameters: " + params + ". No parameters supported");
            }
        }

        protected void initAlgorithmSpecificParameters(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                throw new InvalidAlgorithmParameterException("Unexpected parameters: " + params + ". No parameters supported");
            }
        }

        protected AlgorithmParameters engineGetParameters() {
            return null;
        }

        protected final int getAdditionalEntropyAmountForBegin() {
            return 0;
        }

        protected final int getAdditionalEntropyAmountForFinish() {
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

        protected final void initAlgorithmSpecificParameters() throws InvalidKeyException {
        }

        protected final void initAlgorithmSpecificParameters(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                if (params instanceof OAEPParameterSpec) {
                    OAEPParameterSpec spec = (OAEPParameterSpec) params;
                    if (MGF_ALGORITGM_MGF1.equalsIgnoreCase(spec.getMGFAlgorithm())) {
                        String jcaDigest = spec.getDigestAlgorithm();
                        try {
                            int keymasterDigest = Digest.toKeymaster(jcaDigest);
                            switch (keymasterDigest) {
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 6:
                                    AlgorithmParameterSpec mgfParams = spec.getMGFParameters();
                                    if (mgfParams == null) {
                                        throw new InvalidAlgorithmParameterException("MGF parameters must be provided");
                                    } else if (mgfParams instanceof MGF1ParameterSpec) {
                                        String mgf1JcaDigest = ((MGF1ParameterSpec) mgfParams).getDigestAlgorithm();
                                        if (KeyProperties.DIGEST_SHA1.equalsIgnoreCase(mgf1JcaDigest)) {
                                            PSource pSource = spec.getPSource();
                                            if (pSource instanceof PSpecified) {
                                                byte[] pSourceValue = ((PSpecified) pSource).getValue();
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
                                default:
                                    throw new InvalidAlgorithmParameterException("Unsupported digest: " + jcaDigest);
                            }
                        } catch (IllegalArgumentException e) {
                            throw new InvalidAlgorithmParameterException("Unsupported digest: " + jcaDigest, e);
                        }
                    }
                    throw new InvalidAlgorithmParameterException("Unsupported MGF: " + spec.getMGFAlgorithm() + ". Only " + MGF_ALGORITGM_MGF1 + " supported");
                }
                throw new InvalidAlgorithmParameterException("Unsupported parameter spec: " + params + ". Only OAEPParameterSpec supported");
            }
        }

        protected final void initAlgorithmSpecificParameters(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                try {
                    AlgorithmParameterSpec spec = (OAEPParameterSpec) params.getParameterSpec(OAEPParameterSpec.class);
                    if (spec == null) {
                        throw new InvalidAlgorithmParameterException("OAEP parameters required, but not provided in parameters: " + params);
                    }
                    initAlgorithmSpecificParameters(spec);
                } catch (InvalidParameterSpecException e) {
                    throw new InvalidAlgorithmParameterException("OAEP parameters required, but not found in parameters: " + params, e);
                }
            }
        }

        protected final AlgorithmParameters engineGetParameters() {
            OAEPParameterSpec spec = new OAEPParameterSpec(Digest.fromKeymaster(this.mKeymasterDigest), MGF_ALGORITGM_MGF1, MGF1ParameterSpec.SHA1, PSpecified.DEFAULT);
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

        protected final void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArgs) {
            super.addAlgorithmSpecificParametersToBegin(keymasterArgs);
            keymasterArgs.addEnum(KeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigest);
        }

        protected final void loadAlgorithmSpecificParametersFromBeginResult(KeymasterArguments keymasterArgs) {
            super.loadAlgorithmSpecificParametersFromBeginResult(keymasterArgs);
        }

        protected final int getAdditionalEntropyAmountForBegin() {
            return 0;
        }

        protected final int getAdditionalEntropyAmountForFinish() {
            return isEncrypting() ? this.mDigestOutputSizeBytes : 0;
        }
    }

    public static class OAEPWithSHA1AndMGF1Padding extends OAEPWithMGF1Padding {
        public OAEPWithSHA1AndMGF1Padding() {
            super(2);
        }
    }

    public static class OAEPWithSHA224AndMGF1Padding extends OAEPWithMGF1Padding {
        public OAEPWithSHA224AndMGF1Padding() {
            super(3);
        }
    }

    public static class OAEPWithSHA256AndMGF1Padding extends OAEPWithMGF1Padding {
        public OAEPWithSHA256AndMGF1Padding() {
            super(4);
        }
    }

    public static class OAEPWithSHA384AndMGF1Padding extends OAEPWithMGF1Padding {
        public OAEPWithSHA384AndMGF1Padding() {
            super(5);
        }
    }

    public static class OAEPWithSHA512AndMGF1Padding extends OAEPWithMGF1Padding {
        public OAEPWithSHA512AndMGF1Padding() {
            super(6);
        }
    }

    public static final class PKCS1Padding extends AndroidKeyStoreRSACipherSpi {
        public PKCS1Padding() {
            super(4);
        }

        protected boolean adjustConfigForEncryptingWithPrivateKey() {
            setKeymasterPurposeOverride(2);
            setKeymasterPaddingOverride(5);
            return true;
        }

        protected void initAlgorithmSpecificParameters() throws InvalidKeyException {
        }

        protected void initAlgorithmSpecificParameters(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                throw new InvalidAlgorithmParameterException("Unexpected parameters: " + params + ". No parameters supported");
            }
        }

        protected void initAlgorithmSpecificParameters(AlgorithmParameters params) throws InvalidAlgorithmParameterException {
            if (params != null) {
                throw new InvalidAlgorithmParameterException("Unexpected parameters: " + params + ". No parameters supported");
            }
        }

        protected AlgorithmParameters engineGetParameters() {
            return null;
        }

        protected final int getAdditionalEntropyAmountForBegin() {
            return 0;
        }

        protected final int getAdditionalEntropyAmountForFinish() {
            return isEncrypting() ? getModulusSizeBytes() : 0;
        }
    }

    AndroidKeyStoreRSACipherSpi(int keymasterPadding) {
        this.mKeymasterPadding = keymasterPadding;
    }

    protected final void initKey(int opmode, Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Unsupported key: null");
        } else if (KeyProperties.KEY_ALGORITHM_RSA.equalsIgnoreCase(key.getAlgorithm())) {
            AndroidKeyStoreKey keystoreKey;
            if (key instanceof AndroidKeyStorePrivateKey) {
                keystoreKey = (AndroidKeyStoreKey) key;
            } else if (key instanceof AndroidKeyStorePublicKey) {
                keystoreKey = (AndroidKeyStoreKey) key;
            } else {
                throw new InvalidKeyException("Unsupported key type: " + key);
            }
            if (keystoreKey instanceof PrivateKey) {
                switch (opmode) {
                    case 1:
                    case 3:
                        if (!adjustConfigForEncryptingWithPrivateKey()) {
                            throw new InvalidKeyException("RSA private keys cannot be used with " + AndroidKeyStoreCipherSpiBase.opmodeToString(opmode) + " and padding " + EncryptionPadding.fromKeymaster(this.mKeymasterPadding) + ". Only RSA public keys supported for this mode");
                        }
                        break;
                    case 2:
                    case 4:
                        break;
                    default:
                        throw new InvalidKeyException("RSA private keys cannot be used with opmode: " + opmode);
                }
            }
            switch (opmode) {
                case 1:
                case 3:
                    break;
                case 2:
                case 4:
                    throw new InvalidKeyException("RSA public keys cannot be used with " + AndroidKeyStoreCipherSpiBase.opmodeToString(opmode) + " and padding " + EncryptionPadding.fromKeymaster(this.mKeymasterPadding) + ". Only RSA private keys supported for this opmode.");
                default:
                    throw new InvalidKeyException("RSA public keys cannot be used with " + AndroidKeyStoreCipherSpiBase.opmodeToString(opmode));
            }
            KeyCharacteristics keyCharacteristics = new KeyCharacteristics();
            int errorCode = getKeyStore().getKeyCharacteristics(keystoreKey.getAlias(), null, null, keystoreKey.getUid(), keyCharacteristics);
            if (errorCode != 1) {
                throw getKeyStore().getInvalidKeyException(keystoreKey.getAlias(), keystoreKey.getUid(), errorCode);
            }
            long keySizeBits = keyCharacteristics.getUnsignedInt(KeymasterDefs.KM_TAG_KEY_SIZE, -1);
            if (keySizeBits == -1) {
                throw new InvalidKeyException("Size of key not known");
            } else if (keySizeBits > 2147483647L) {
                throw new InvalidKeyException("Key too large: " + keySizeBits + " bits");
            } else {
                this.mModulusSizeBytes = (int) ((7 + keySizeBits) / 8);
                setKey(keystoreKey);
            }
        } else {
            throw new InvalidKeyException("Unsupported key algorithm: " + key.getAlgorithm() + ". Only " + KeyProperties.KEY_ALGORITHM_RSA + " supported");
        }
    }

    protected boolean adjustConfigForEncryptingWithPrivateKey() {
        return false;
    }

    protected final void resetAll() {
        this.mModulusSizeBytes = -1;
        this.mKeymasterPaddingOverride = -1;
        super.resetAll();
    }

    protected final void resetWhilePreservingInitState() {
        super.resetWhilePreservingInitState();
    }

    protected void addAlgorithmSpecificParametersToBegin(KeymasterArguments keymasterArgs) {
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

    protected void loadAlgorithmSpecificParametersFromBeginResult(KeymasterArguments keymasterArgs) {
    }

    protected final int engineGetBlockSize() {
        return 0;
    }

    protected final byte[] engineGetIV() {
        return null;
    }

    protected final int engineGetOutputSize(int inputLen) {
        return getModulusSizeBytes();
    }

    protected final int getModulusSizeBytes() {
        if (this.mModulusSizeBytes != -1) {
            return this.mModulusSizeBytes;
        }
        throw new IllegalStateException("Not initialized");
    }

    protected final void setKeymasterPaddingOverride(int keymasterPadding) {
        this.mKeymasterPaddingOverride = keymasterPadding;
    }

    protected final int getKeymasterPaddingOverride() {
        return this.mKeymasterPaddingOverride;
    }
}
