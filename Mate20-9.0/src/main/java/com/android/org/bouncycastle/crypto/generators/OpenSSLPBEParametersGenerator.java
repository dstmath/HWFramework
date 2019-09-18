package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.PBEParametersGenerator;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;

public class OpenSSLPBEParametersGenerator extends PBEParametersGenerator {
    private Digest digest = AndroidDigestFactory.getMD5();

    public void init(byte[] password, byte[] salt) {
        super.init(password, salt, 1);
    }

    private byte[] generateDerivedKey(int bytesNeeded) {
        byte[] buf = new byte[this.digest.getDigestSize()];
        byte[] key = new byte[bytesNeeded];
        int bytesNeeded2 = bytesNeeded;
        int offset = 0;
        while (true) {
            this.digest.update(this.password, 0, this.password.length);
            this.digest.update(this.salt, 0, this.salt.length);
            this.digest.doFinal(buf, 0);
            int len = bytesNeeded2 > buf.length ? buf.length : bytesNeeded2;
            System.arraycopy(buf, 0, key, offset, len);
            offset += len;
            bytesNeeded2 -= len;
            if (bytesNeeded2 == 0) {
                return key;
            }
            this.digest.reset();
            this.digest.update(buf, 0, buf.length);
        }
    }

    public CipherParameters generateDerivedParameters(int keySize) {
        int keySize2 = keySize / 8;
        return new KeyParameter(generateDerivedKey(keySize2), 0, keySize2);
    }

    public CipherParameters generateDerivedParameters(int keySize, int ivSize) {
        int keySize2 = keySize / 8;
        int ivSize2 = ivSize / 8;
        byte[] dKey = generateDerivedKey(keySize2 + ivSize2);
        return new ParametersWithIV(new KeyParameter(dKey, 0, keySize2), dKey, keySize2, ivSize2);
    }

    public CipherParameters generateDerivedMacParameters(int keySize) {
        return generateDerivedParameters(keySize);
    }
}
