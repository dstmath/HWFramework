package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.Mac;
import com.android.org.bouncycastle.crypto.PBEParametersGenerator;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.crypto.macs.HMac;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import com.android.org.bouncycastle.util.Arrays;

public class PKCS5S2ParametersGenerator extends PBEParametersGenerator {
    private Mac hMac;
    private byte[] state;

    public PKCS5S2ParametersGenerator() {
        this(AndroidDigestFactory.getSHA1());
    }

    public PKCS5S2ParametersGenerator(Digest digest) {
        this.hMac = new HMac(digest);
        this.state = new byte[this.hMac.getMacSize()];
    }

    private void F(byte[] S, int c, byte[] iBuf, byte[] out, int outOff) {
        if (c == 0) {
            throw new IllegalArgumentException("iteration count must be at least 1.");
        }
        if (S != null) {
            this.hMac.update(S, 0, S.length);
        }
        this.hMac.update(iBuf, 0, iBuf.length);
        this.hMac.doFinal(this.state, 0);
        System.arraycopy(this.state, 0, out, outOff, this.state.length);
        for (int count = 1; count < c; count++) {
            this.hMac.update(this.state, 0, this.state.length);
            this.hMac.doFinal(this.state, 0);
            for (int j = 0; j != this.state.length; j++) {
                int i = outOff + j;
                out[i] = (byte) (out[i] ^ this.state[j]);
            }
        }
    }

    private byte[] generateDerivedKey(int dkLen) {
        int hLen = this.hMac.getMacSize();
        int l = ((dkLen + hLen) - 1) / hLen;
        byte[] iBuf = new byte[4];
        byte[] outBytes = new byte[(l * hLen)];
        int outPos = 0;
        this.hMac.init(new KeyParameter(this.password));
        for (int i = 1; i <= l; i++) {
            int pos = 3;
            while (true) {
                byte b = (byte) (iBuf[pos] + 1);
                iBuf[pos] = b;
                if (b != (byte) 0) {
                    break;
                }
                pos--;
            }
            F(this.salt, this.iterationCount, iBuf, outBytes, outPos);
            outPos += hLen;
        }
        return outBytes;
    }

    public CipherParameters generateDerivedParameters(int keySize) {
        keySize /= 8;
        return new KeyParameter(Arrays.copyOfRange(generateDerivedKey(keySize), 0, keySize), 0, keySize);
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
