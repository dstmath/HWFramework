package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.PBEParametersGenerator;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;

public class PKCS5S1ParametersGenerator extends PBEParametersGenerator {
    private Digest digest;

    public PKCS5S1ParametersGenerator(Digest digest2) {
        this.digest = digest2;
    }

    private byte[] generateDerivedKey() {
        byte[] digestBytes = new byte[this.digest.getDigestSize()];
        this.digest.update(this.password, 0, this.password.length);
        this.digest.update(this.salt, 0, this.salt.length);
        this.digest.doFinal(digestBytes, 0);
        for (int i = 1; i < this.iterationCount; i++) {
            this.digest.update(digestBytes, 0, digestBytes.length);
            this.digest.doFinal(digestBytes, 0);
        }
        return digestBytes;
    }

    public CipherParameters generateDerivedParameters(int keySize) {
        int keySize2 = keySize / 8;
        if (keySize2 <= this.digest.getDigestSize()) {
            return new KeyParameter(generateDerivedKey(), 0, keySize2);
        }
        throw new IllegalArgumentException("Can't generate a derived key " + keySize2 + " bytes long.");
    }

    public CipherParameters generateDerivedParameters(int keySize, int ivSize) {
        int keySize2 = keySize / 8;
        int ivSize2 = ivSize / 8;
        if (keySize2 + ivSize2 <= this.digest.getDigestSize()) {
            byte[] dKey = generateDerivedKey();
            return new ParametersWithIV(new KeyParameter(dKey, 0, keySize2), dKey, keySize2, ivSize2);
        }
        throw new IllegalArgumentException("Can't generate a derived key " + (keySize2 + ivSize2) + " bytes long.");
    }

    public CipherParameters generateDerivedMacParameters(int keySize) {
        return generateDerivedParameters(keySize);
    }
}
