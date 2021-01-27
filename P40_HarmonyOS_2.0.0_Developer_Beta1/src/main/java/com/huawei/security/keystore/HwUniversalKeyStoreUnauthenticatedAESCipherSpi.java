package com.huawei.security.keystore;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterDefs;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import javax.crypto.spec.IvParameterSpec;

public class HwUniversalKeyStoreUnauthenticatedAESCipherSpi extends HwUniversalKeyStoreCipherSpiBase {
    private static final int BLOCK_BYTES_SIZE = 16;
    private static final int BLOCK_BYTES_SIZE_CNT = 3;
    private static final String TAG = "HwUniversalKeyStoreUnauthenticatedAESCipherSpi";
    private byte[] mIv;
    private boolean mIvHasBeenUsed;
    private final boolean mIvRequired;
    private final int mKeymasterBlockMode;
    private final int mKeymasterPadding;

    HwUniversalKeyStoreUnauthenticatedAESCipherSpi(int keymasterBlockMode, int keymasterPadding, boolean ivRequired) {
        this.mIvRequired = ivRequired;
        this.mKeymasterPadding = keymasterPadding;
        this.mKeymasterBlockMode = keymasterBlockMode;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public final void resetWhilePreservingInitState() {
        super.resetWhilePreservingInitState();
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public final void resetAll() {
        super.resetAll();
        this.mIv = null;
        this.mIvHasBeenUsed = false;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public final void initKey(int mode, Key key) throws InvalidKeyException {
        if (!(key instanceof HwUniversalKeyStoreSecretKey)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Not support key: ");
            sb.append(key != null ? key.getClass().getName() : "null");
            throw new InvalidKeyException(sb.toString());
        } else if (HwKeyProperties.KEY_ALGORITHM_AES.equalsIgnoreCase(key.getAlgorithm())) {
            setKey((HwUniversalKeyStoreSecretKey) key);
        } else {
            throw new InvalidKeyException("Not support key algorithm: " + key.getAlgorithm() + ". Only " + HwKeyProperties.KEY_ALGORITHM_AES + " supported");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public final void initAlgorithmSpecificParameters(AlgorithmParameterSpec algParams) throws InvalidAlgorithmParameterException {
        if (!this.mIvRequired) {
            if (algParams != null) {
                throw new InvalidAlgorithmParameterException("Unsupported parameters not null!");
            }
        } else if (algParams == null) {
            if (!isEncrypting()) {
                throw new InvalidAlgorithmParameterException("Need IvParameterSpec when decrypting");
            }
        } else if (algParams instanceof IvParameterSpec) {
            this.mIv = ((IvParameterSpec) algParams).getIV();
            if (this.mIv == null) {
                throw new InvalidAlgorithmParameterException("Null IV from IvParameterSpec");
            }
        } else {
            throw new InvalidAlgorithmParameterException("Only support IvParameterSpec");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public final void initAlgorithmSpecificParameters() throws InvalidKeyException {
        if (this.mIvRequired && !isEncrypting()) {
            throw new InvalidKeyException("IV required when decrypting.Provide it with IvParameterSpec or AlgorithmParameters.");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public final void initAlgorithmSpecificParameters(AlgorithmParameters algParams) throws InvalidAlgorithmParameterException {
        if (!this.mIvRequired) {
            if (algParams != null) {
                throw new InvalidAlgorithmParameterException("Unsupported parameters not null!");
            }
        } else if (algParams == null) {
            if (!isEncrypting()) {
                throw new InvalidAlgorithmParameterException("IV required when decrypting.Provide IvParameterSpec or AlgorithmParameters.");
            }
        } else if (HwKeyProperties.KEY_ALGORITHM_AES.equalsIgnoreCase(algParams.getAlgorithm())) {
            try {
                this.mIv = ((IvParameterSpec) algParams.getParameterSpec(IvParameterSpec.class)).getIV();
                if (this.mIv == null) {
                    throw new InvalidAlgorithmParameterException("Null IV from AlgorithmParameters");
                }
            } catch (InvalidParameterSpecException ex) {
                if (isEncrypting()) {
                    this.mIv = null;
                    return;
                }
                throw new InvalidAlgorithmParameterException("IV required when decrypting,but not found in parameters: " + algParams, ex);
            }
        } else {
            throw new InvalidAlgorithmParameterException("Only support AES AlgorithmParameters algorithm");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public final int getAdditionalEntropyAmountForBegin() {
        if (!this.mIvRequired || this.mIv != null || !isEncrypting()) {
            return 0;
        }
        return 16;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public final int getAdditionalEntropyAmountForFinish() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public void addAlgorithmSpecificParametersToBegin(@NonNull HwKeymasterArguments keymasterArgs) {
        byte[] bArr;
        if (!isEncrypting() || !this.mIvRequired || !this.mIvHasBeenUsed) {
            keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_ALGORITHM, 32);
            keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_BLOCK_MODE, this.mKeymasterBlockMode);
            keymasterArgs.addEnum(HwKeymasterDefs.KM_TAG_PADDING, this.mKeymasterPadding);
            if (this.mIvRequired && (bArr = this.mIv) != null) {
                keymasterArgs.addBytes(HwKeymasterDefs.KM_TAG_NONCE, bArr);
                Log.e(TAG, "addBytes mIv, length is " + this.mIv.length);
                return;
            }
            return;
        }
        throw new IllegalStateException("IV has already been used. Reusing IV in encryption mode violates security best practices.");
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase
    public final void loadAlgorithmSpecificParametersFromBeginResult(@NonNull HwKeymasterArguments keymasterArgs) {
        this.mIvHasBeenUsed = true;
        byte[] returnedIv = keymasterArgs.getBytes(HwKeymasterDefs.KM_TAG_NONCE, null);
        if (returnedIv != null && returnedIv.length == 0) {
            returnedIv = null;
        }
        if (this.mIvRequired) {
            byte[] bArr = this.mIv;
            if (bArr == null) {
                this.mIv = returnedIv;
                Log.i(TAG, "Iv saved from outParams!");
            } else if (returnedIv != null && !Arrays.equals(returnedIv, bArr)) {
                throw new ProviderException("IV in use differs from provided IV");
            }
        } else if (returnedIv != null) {
            throw new ProviderException("IV in use despite IV not being used by this transformation");
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineGetIV() {
        return ArrayUtils.cloneIfNotEmpty(this.mIv);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetOutputSize(int inputLen) {
        return inputLen + 48;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetBlockSize() {
        return 16;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.keystore.HwUniversalKeyStoreCipherSpiBase, javax.crypto.CipherSpi
    @Nullable
    public final AlgorithmParameters engineGetParameters() {
        byte[] bArr;
        if (!this.mIvRequired || (bArr = this.mIv) == null || bArr.length <= 0) {
            return null;
        }
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance(HwKeyProperties.KEY_ALGORITHM_AES);
            params.init(new IvParameterSpec(this.mIv));
            return params;
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException("Failed to get the AES AlgorithmParameters.", e);
        } catch (InvalidParameterSpecException e2) {
            throw new ProviderException("Failed to initialize the AES algorithm parameters by using the IV.", e2);
        }
    }
}
