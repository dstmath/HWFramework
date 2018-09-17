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
        int offset = 0;
        while (true) {
            this.digest.update(this.password, 0, this.password.length);
            this.digest.update(this.salt, 0, this.salt.length);
            this.digest.doFinal(buf, 0);
            int len = bytesNeeded > buf.length ? buf.length : bytesNeeded;
            System.arraycopy(buf, 0, key, offset, len);
            offset += len;
            bytesNeeded -= len;
            if (bytesNeeded == 0) {
                return key;
            }
            this.digest.reset();
            this.digest.update(buf, 0, buf.length);
        }
    }

    public CipherParameters generateDerivedParameters(int keySize) {
        keySize /= 8;
        return new KeyParameter(generateDerivedKey(keySize), 0, keySize);
    }

    public CipherParameters generateDerivedParameters(int keySize, int ivSize) {
        keySize /= 8;
        ivSize /= 8;
        byte[] dKey = generateDerivedKey(keySize + ivSize);
        return new ParametersWithIV(new KeyParameter(dKey, 0, keySize), dKey, keySize, ivSize);
    }

    public CipherParameters generateDerivedMacParameters(int keySize) {
        return generateDerivedParameters(keySize);
    }
}
