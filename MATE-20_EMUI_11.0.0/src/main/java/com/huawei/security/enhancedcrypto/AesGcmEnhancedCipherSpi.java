package com.huawei.security.enhancedcrypto;

import android.support.annotation.NonNull;
import android.util.Log;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

public class AesGcmEnhancedCipherSpi extends EnhancedCipherSpi {
    private static final int AES_128_KEY_LENGTH = 16;
    private static final int AES_256_KEY_LENGTH = 32;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final int IV_LENGTH_BYTES = 12;
    private static final String TAG = "AesGcmEnhancedCipherSpi";
    private static final int TAG_LENGTH_BYTES = 16;
    @NonNull
    private byte[] mAad = EMPTY_BYTE_ARRAY;
    private boolean mInitialized;
    private boolean mIsEncrypMode;
    private byte[] mIv;
    private byte[] mKey;
    private int mTagLenInBytes;

    private native int aesGcmDecrypt(byte[][] bArr, int[] iArr, int[] iArr2);

    private native int aesGcmEncrypt(byte[][] bArr, int[] iArr, int[] iArr2);

    static {
        try {
            System.loadLibrary("EnhancedCrypto");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "EnhancedCrypto library could not be loaded!");
        } catch (SecurityException e2) {
            Log.e(TAG, "EnhancedCrypto library not allow be loaded!");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.enhancedcrypto.EnhancedCipherSpi
    public void engineInit(int opmode, @NonNull byte[] key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        checkAndSetEncodedKey(opmode, key);
        initAlgorithmSpecificParameters(algorithmParameterSpec);
        this.mInitialized = true;
    }

    private void checkAndSetEncodedKey(int opmode, byte[] key) throws InvalidKeyException {
        if (opmode == 1) {
            this.mIsEncrypMode = true;
        } else if (opmode == 2) {
            this.mIsEncrypMode = false;
        } else {
            throw new InvalidParameterException("Unsupported operation mode!");
        }
        if (key == null || !(key.length == 16 || key.length == 32)) {
            throw new InvalidKeyException("Unsupported key size or key is null!");
        }
        this.mKey = (byte[]) key.clone();
    }

    private void initAlgorithmSpecificParameters(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidAlgorithmParameterException {
        if (algorithmParameterSpec instanceof GCMParameterSpec) {
            GCMParameterSpec spec = (GCMParameterSpec) algorithmParameterSpec;
            if (spec.getTLen() % 8 == 0) {
                this.mIv = spec.getIV();
                this.mTagLenInBytes = spec.getTLen() / 8;
            } else {
                throw new InvalidAlgorithmParameterException("Tag length must be a multiple of 8!");
            }
        } else if (algorithmParameterSpec instanceof IvParameterSpec) {
            this.mIv = ((IvParameterSpec) algorithmParameterSpec).getIV();
            this.mTagLenInBytes = 16;
        } else {
            this.mIv = null;
            this.mTagLenInBytes = 16;
        }
        byte[] bArr = this.mIv;
        if (bArr == null || bArr.length != 12 || this.mTagLenInBytes != 16) {
            throw new InvalidAlgorithmParameterException("Unsupported algorithm parameters!");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.enhancedcrypto.EnhancedCipherSpi
    public void engineUpdateAad(@NonNull byte[] input, int inputOffset, int inputLen) {
        preCheck(input, inputOffset, inputLen);
        if (this.mInitialized) {
            byte[] bArr = this.mAad;
            if (bArr.length == 0) {
                this.mAad = Arrays.copyOfRange(input, inputOffset, inputOffset + inputLen);
                return;
            }
            byte[] newAad = new byte[(bArr.length + inputLen)];
            System.arraycopy(bArr, 0, newAad, 0, bArr.length);
            System.arraycopy(input, inputOffset, newAad, this.mAad.length, inputLen);
            this.mAad = newAad;
            return;
        }
        throw new IllegalStateException("EnhancedCipher not initialized!");
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.security.enhancedcrypto.EnhancedCipherSpi
    public int engineDoFinal(@NonNull byte[] input, int inputOffset, int inputLen, @NonNull byte[] output, int outputOffset) {
        int res;
        preCheck(input, inputOffset, inputLen);
        int outputLen = this.mIsEncrypMode ? this.mTagLenInBytes + inputLen : inputLen - this.mTagLenInBytes;
        preCheck(output, outputOffset, outputLen);
        if (this.mInitialized) {
            byte[][] sources = {input, output, this.mKey, this.mIv, this.mAad};
            int[] offsets = {inputOffset, outputOffset};
            int[] lengths = {inputLen, outputLen};
            if (this.mIsEncrypMode) {
                res = aesGcmEncrypt(sources, offsets, lengths);
            } else {
                res = aesGcmDecrypt(sources, offsets, lengths);
            }
            reset();
            return res;
        }
        throw new IllegalStateException("EnhancedCipher not initialized!");
    }

    private void preCheck(byte[] input, int inputOffset, int inputLen) {
        if (input == null || inputOffset < 0 || inputLen <= 0 || inputOffset > input.length || input.length - inputOffset < inputLen) {
            throw new IllegalArgumentException("Invalid buffer arguments");
        }
    }

    private void reset() {
        this.mAad = EMPTY_BYTE_ARRAY;
        Arrays.fill(this.mKey, (byte) 0);
        this.mInitialized = false;
    }
}
