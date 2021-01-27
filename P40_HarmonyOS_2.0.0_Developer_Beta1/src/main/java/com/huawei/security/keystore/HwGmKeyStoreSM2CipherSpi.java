package com.huawei.security.keystore;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.huawei.security.keymaster.HwKeyCharacteristics;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterDefs;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

public class HwGmKeyStoreSM2CipherSpi extends HwUniversalKeyStoreCipherSpiBase {
    private static final int BYTE_SIZES = 8;
    private static final String TAG = "HwGmKeyStoreSM2CipherSpi";
    private int mKeymasterPadding;
    private int mKeymasterPaddingOverride = -1;
    private int mModulusSizeBytes = -1;

    HwGmKeyStoreSM2CipherSpi(int keymasterPadding) {
        this.mKeymasterPadding = keymasterPadding;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetBlockSize() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetOutputSize(int inputLen) {
        return getModulusSizeBytes();
    }

    private final int getModulusSizeBytes() {
        int i = this.mModulusSizeBytes;
        if (i != -1) {
            return i;
        }
        throw new IllegalStateException("Not initialized");
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineGetIV() {
        return new byte[0];
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase, javax.crypto.CipherSpi
    public AlgorithmParameters engineGetParameters() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public void resetAll() {
        super.resetAll();
        this.mModulusSizeBytes = -1;
        this.mKeymasterPaddingOverride = -1;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public void addAlgorithmSpecificParametersToBegin(HwKeymasterArguments keymasterArgs) {
        keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_ALGORITHM, 3);
        keymasterArgs.addBoolean(HwKeymasterDefs.KM_TAG_IS_FROM_GM);
        int keymasterPadding = getKeymasterPaddingOverride();
        if (keymasterPadding == -1) {
            keymasterPadding = this.mKeymasterPadding;
        }
        keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_PADDING, keymasterPadding);
        keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_DIGEST, 7);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public int getAdditionalEntropyAmountForBegin() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public void loadAlgorithmSpecificParametersFromBeginResult(@NonNull HwKeymasterArguments keymasterArgs) {
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public final void initKey(int opMode, Key key) throws InvalidKeyException {
        HwUniversalKeyStoreKey keystoreKey;
        if (key == null) {
            throw new InvalidKeyException("Unsupported key: null");
        } else if (HwKeyProperties.KEY_ALGORITHM_EC.equalsIgnoreCase(key.getAlgorithm()) || HwKeyProperties.KEY_ALGORITHM_SM2.equalsIgnoreCase(key.getAlgorithm())) {
            if (key instanceof HwUniversalKeyStorePrivateKey) {
                keystoreKey = (HwUniversalKeyStoreKey) key;
                checkPrivateKeyOpMode(opMode);
            } else if (key instanceof HwUniversalKeyStorePublicKey) {
                keystoreKey = (HwUniversalKeyStoreKey) key;
                checkPublicKeyOpMode(opMode);
            } else {
                throw new InvalidKeyException("Unsupported key type: " + key.getClass().getName());
            }
            HwKeyCharacteristics keyCharacteristics = new HwKeyCharacteristics();
            int errorCode = getKeyStore().getKeyCharacteristics(keystoreKey.getAlias(), null, null, keystoreKey.getUid(), keyCharacteristics);
            if (errorCode == 1) {
                long keySizeBits = keyCharacteristics.getUnsignedInt(HwKeymasterDefs.KM_TAG_KEY_SIZE, -1);
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
            throw new InvalidKeyException("Unsupported key algorithm: " + key.getAlgorithm() + ". Only " + HwKeyProperties.KEY_ALGORITHM_EC + " or " + HwKeyProperties.KEY_ALGORITHM_SM2 + " supported");
        }
    }

    private void checkPublicKeyOpMode(int opMode) throws InvalidKeyException {
        if (opMode != 1) {
            if (opMode != 2) {
                if (opMode == 3) {
                    return;
                }
                if (opMode != 4) {
                    throw new InvalidKeyException("SM2 public keys cannot be used with " + opModeToString(opMode));
                }
            }
            throw new InvalidKeyException("SM2 public keys cannot be used with " + opModeToString(opMode) + ". Only SM2 private keys supported for this opMode.");
        }
    }

    private void checkPrivateKeyOpMode(int opMode) throws InvalidKeyException {
        if (opMode != 1) {
            if (opMode == 2) {
                return;
            }
            if (opMode != 3) {
                if (opMode != 4) {
                    throw new InvalidKeyException("SM2 private keys cannot be used with opMode: " + opMode);
                }
                return;
            }
        }
        if (!adjustConfigForEncryptingWithPrivateKey()) {
            throw new InvalidKeyException("SM2 private keys cannot be used with " + opModeToString(opMode) + ". Only SM2 public keys supported for this mode");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public void initAlgorithmSpecificParameters() throws InvalidKeyException {
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public void initAlgorithmSpecificParameters(@Nullable AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public void initAlgorithmSpecificParameters(@Nullable AlgorithmParameters params) throws InvalidAlgorithmParameterException {
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public int getAdditionalEntropyAmountForFinish() {
        return 0;
    }

    public final int getKeymasterPaddingOverride() {
        return this.mKeymasterPaddingOverride;
    }

    public final void setKeymasterPaddingOverride(int KeymasterPaddingOverride) {
        this.mKeymasterPaddingOverride = KeymasterPaddingOverride;
    }

    public static final class NoPadding extends HwGmKeyStoreSM2CipherSpi {
        public NoPadding() {
            super(1);
        }
    }
}
