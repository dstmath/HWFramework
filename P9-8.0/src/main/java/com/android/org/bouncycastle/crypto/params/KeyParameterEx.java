package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.PBEParametersGenerator;
import com.android.org.bouncycastle.crypto.generators.OpenSSLPBEParametersGenerator;

public class KeyParameterEx {
    private static final int BYTE_LENTH_IN_BITS = 8;

    public static byte[] getKey(char[] password, int keyLength, byte[] salt) {
        OpenSSLPBEParametersGenerator localOpenSSLPBEParametersGenerator = new OpenSSLPBEParametersGenerator();
        localOpenSSLPBEParametersGenerator.init(PBEParametersGenerator.PKCS5PasswordToBytes(password), salt);
        return ((KeyParameter) localOpenSSLPBEParametersGenerator.generateDerivedParameters(keyLength * 8)).getKey();
    }
}
