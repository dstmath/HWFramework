package org.bouncycastle.crypto.prng.drbg;

import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.prng.EntropySource;
import org.bouncycastle.util.Arrays;

public class HMacSP800DRBG implements SP80090DRBG {
    private static final int MAX_BITS_REQUEST = 262144;
    private static final long RESEED_MAX = 140737488355328L;
    private byte[] _K;
    private byte[] _V;
    private EntropySource _entropySource;
    private Mac _hMac;
    private long _reseedCounter;
    private int _securityStrength;

    public HMacSP800DRBG(Mac mac, int i, EntropySource entropySource, byte[] bArr, byte[] bArr2) {
        if (i > Utils.getMaxSecurityStrength(mac)) {
            throw new IllegalArgumentException("Requested security strength is not supported by the derivation function");
        } else if (entropySource.entropySize() >= i) {
            this._securityStrength = i;
            this._entropySource = entropySource;
            this._hMac = mac;
            byte[] concatenate = Arrays.concatenate(getEntropy(), bArr2, bArr);
            this._K = new byte[mac.getMacSize()];
            this._V = new byte[this._K.length];
            Arrays.fill(this._V, (byte) 1);
            hmac_DRBG_Update(concatenate);
            this._reseedCounter = 1;
        } else {
            throw new IllegalArgumentException("Not enough entropy for security strength required");
        }
    }

    private byte[] getEntropy() {
        byte[] entropy = this._entropySource.getEntropy();
        if (entropy.length >= (this._securityStrength + 7) / 8) {
            return entropy;
        }
        throw new IllegalStateException("Insufficient entropy provided by entropy source");
    }

    private void hmac_DRBG_Update(byte[] bArr) {
        hmac_DRBG_Update_Func(bArr, (byte) 0);
        if (bArr != null) {
            hmac_DRBG_Update_Func(bArr, (byte) 1);
        }
    }

    private void hmac_DRBG_Update_Func(byte[] bArr, byte b) {
        this._hMac.init(new KeyParameter(this._K));
        Mac mac = this._hMac;
        byte[] bArr2 = this._V;
        mac.update(bArr2, 0, bArr2.length);
        this._hMac.update(b);
        if (bArr != null) {
            this._hMac.update(bArr, 0, bArr.length);
        }
        this._hMac.doFinal(this._K, 0);
        this._hMac.init(new KeyParameter(this._K));
        Mac mac2 = this._hMac;
        byte[] bArr3 = this._V;
        mac2.update(bArr3, 0, bArr3.length);
        this._hMac.doFinal(this._V, 0);
    }

    @Override // org.bouncycastle.crypto.prng.drbg.SP80090DRBG
    public int generate(byte[] bArr, byte[] bArr2, boolean z) {
        int length = bArr.length * 8;
        if (length > 262144) {
            throw new IllegalArgumentException("Number of bits per request limited to 262144");
        } else if (this._reseedCounter > RESEED_MAX) {
            return -1;
        } else {
            if (z) {
                reseed(bArr2);
                bArr2 = null;
            }
            if (bArr2 != null) {
                hmac_DRBG_Update(bArr2);
            }
            byte[] bArr3 = new byte[bArr.length];
            int length2 = bArr.length / this._V.length;
            this._hMac.init(new KeyParameter(this._K));
            for (int i = 0; i < length2; i++) {
                Mac mac = this._hMac;
                byte[] bArr4 = this._V;
                mac.update(bArr4, 0, bArr4.length);
                this._hMac.doFinal(this._V, 0);
                byte[] bArr5 = this._V;
                System.arraycopy(bArr5, 0, bArr3, bArr5.length * i, bArr5.length);
            }
            byte[] bArr6 = this._V;
            if (bArr6.length * length2 < bArr3.length) {
                this._hMac.update(bArr6, 0, bArr6.length);
                this._hMac.doFinal(this._V, 0);
                byte[] bArr7 = this._V;
                System.arraycopy(bArr7, 0, bArr3, bArr7.length * length2, bArr3.length - (length2 * bArr7.length));
            }
            hmac_DRBG_Update(bArr2);
            this._reseedCounter++;
            System.arraycopy(bArr3, 0, bArr, 0, bArr.length);
            return length;
        }
    }

    @Override // org.bouncycastle.crypto.prng.drbg.SP80090DRBG
    public int getBlockSize() {
        return this._V.length * 8;
    }

    @Override // org.bouncycastle.crypto.prng.drbg.SP80090DRBG
    public void reseed(byte[] bArr) {
        hmac_DRBG_Update(Arrays.concatenate(getEntropy(), bArr));
        this._reseedCounter = 1;
    }
}
