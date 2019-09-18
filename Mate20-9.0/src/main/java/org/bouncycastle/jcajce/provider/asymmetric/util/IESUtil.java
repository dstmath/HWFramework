package org.bouncycastle.jcajce.provider.asymmetric.util;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.jce.spec.IESParameterSpec;

public class IESUtil {
    public static IESParameterSpec guessParameterSpec(BufferedBlockCipher bufferedBlockCipher, byte[] bArr) {
        if (bufferedBlockCipher == null) {
            return new IESParameterSpec(null, null, 128);
        }
        BlockCipher underlyingCipher = bufferedBlockCipher.getUnderlyingCipher();
        if (underlyingCipher.getAlgorithmName().equals("DES") || underlyingCipher.getAlgorithmName().equals("RC2") || underlyingCipher.getAlgorithmName().equals("RC5-32") || underlyingCipher.getAlgorithmName().equals("RC5-64")) {
            IESParameterSpec iESParameterSpec = new IESParameterSpec(null, null, 64, 64, bArr);
            return iESParameterSpec;
        } else if (underlyingCipher.getAlgorithmName().equals("SKIPJACK")) {
            IESParameterSpec iESParameterSpec2 = new IESParameterSpec(null, null, 80, 80, bArr);
            return iESParameterSpec2;
        } else if (underlyingCipher.getAlgorithmName().equals("GOST28147")) {
            IESParameterSpec iESParameterSpec3 = new IESParameterSpec(null, null, 256, 256, bArr);
            return iESParameterSpec3;
        } else {
            IESParameterSpec iESParameterSpec4 = new IESParameterSpec(null, null, 128, 128, bArr);
            return iESParameterSpec4;
        }
    }
}
