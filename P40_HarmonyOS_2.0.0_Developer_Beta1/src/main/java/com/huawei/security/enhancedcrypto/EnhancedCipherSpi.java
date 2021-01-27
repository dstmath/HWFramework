package com.huawei.security.enhancedcrypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;

public abstract class EnhancedCipherSpi {
    /* access modifiers changed from: protected */
    public abstract int engineDoFinal(byte[] bArr, int i, int i2, byte[] bArr2, int i3);

    /* access modifiers changed from: protected */
    public abstract void engineInit(int i, byte[] bArr, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException;

    /* access modifiers changed from: protected */
    public abstract void engineUpdateAad(byte[] bArr, int i, int i2);
}
