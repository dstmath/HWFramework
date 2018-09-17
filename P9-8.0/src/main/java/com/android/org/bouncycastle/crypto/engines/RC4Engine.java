package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.StreamCipher;
import com.android.org.bouncycastle.crypto.params.KeyParameter;

public class RC4Engine implements StreamCipher {
    private static final int STATE_LENGTH = 256;
    private byte[] engineState = null;
    private byte[] workingKey = null;
    private int x = 0;
    private int y = 0;

    public void init(boolean forEncryption, CipherParameters params) {
        if (params instanceof KeyParameter) {
            this.workingKey = ((KeyParameter) params).getKey();
            setKey(this.workingKey);
            return;
        }
        throw new IllegalArgumentException("invalid parameter passed to RC4 init - " + params.getClass().getName());
    }

    public String getAlgorithmName() {
        return "RC4";
    }

    public byte returnByte(byte in) {
        this.x = (this.x + 1) & 255;
        this.y = (this.engineState[this.x] + this.y) & 255;
        byte tmp = this.engineState[this.x];
        this.engineState[this.x] = this.engineState[this.y];
        this.engineState[this.y] = tmp;
        return (byte) (this.engineState[(this.engineState[this.x] + this.engineState[this.y]) & 255] ^ in);
    }

    public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff) {
        if (inOff + len > in.length) {
            throw new DataLengthException("input buffer too short");
        } else if (outOff + len > out.length) {
            throw new OutputLengthException("output buffer too short");
        } else {
            for (int i = 0; i < len; i++) {
                this.x = (this.x + 1) & 255;
                this.y = (this.engineState[this.x] + this.y) & 255;
                byte tmp = this.engineState[this.x];
                this.engineState[this.x] = this.engineState[this.y];
                this.engineState[this.y] = tmp;
                out[i + outOff] = (byte) (in[i + inOff] ^ this.engineState[(this.engineState[this.x] + this.engineState[this.y]) & 255]);
            }
            return len;
        }
    }

    public void reset() {
        setKey(this.workingKey);
    }

    private void setKey(byte[] keyBytes) {
        int i;
        this.workingKey = keyBytes;
        this.x = 0;
        this.y = 0;
        if (this.engineState == null) {
            this.engineState = new byte[STATE_LENGTH];
        }
        for (i = 0; i < STATE_LENGTH; i++) {
            this.engineState[i] = (byte) i;
        }
        int i1 = 0;
        int i2 = 0;
        for (i = 0; i < STATE_LENGTH; i++) {
            i2 = (((keyBytes[i1] & 255) + this.engineState[i]) + i2) & 255;
            byte tmp = this.engineState[i];
            this.engineState[i] = this.engineState[i2];
            this.engineState[i2] = tmp;
            i1 = (i1 + 1) % keyBytes.length;
        }
    }
}
