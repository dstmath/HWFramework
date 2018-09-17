package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.params.KeyParameter;

public class DESedeEngine extends DESEngine {
    protected static final int BLOCK_SIZE = 8;
    private boolean forEncryption;
    private int[] workingKey1 = null;
    private int[] workingKey2 = null;
    private int[] workingKey3 = null;

    public void init(boolean encrypting, CipherParameters params) {
        if (params instanceof KeyParameter) {
            byte[] keyMaster = ((KeyParameter) params).getKey();
            if (keyMaster.length == 24 || keyMaster.length == 16) {
                this.forEncryption = encrypting;
                byte[] key1 = new byte[8];
                System.arraycopy(keyMaster, 0, key1, 0, key1.length);
                this.workingKey1 = generateWorkingKey(encrypting, key1);
                byte[] key2 = new byte[8];
                System.arraycopy(keyMaster, 8, key2, 0, key2.length);
                this.workingKey2 = generateWorkingKey(encrypting ^ 1, key2);
                if (keyMaster.length == 24) {
                    byte[] key3 = new byte[8];
                    System.arraycopy(keyMaster, 16, key3, 0, key3.length);
                    this.workingKey3 = generateWorkingKey(encrypting, key3);
                    return;
                }
                this.workingKey3 = this.workingKey1;
                return;
            }
            throw new IllegalArgumentException("key size must be 16 or 24 bytes.");
        }
        throw new IllegalArgumentException("invalid parameter passed to DESede init - " + params.getClass().getName());
    }

    public String getAlgorithmName() {
        return "DESede";
    }

    public int getBlockSize() {
        return 8;
    }

    public int processBlock(byte[] in, int inOff, byte[] out, int outOff) {
        if (this.workingKey1 == null) {
            throw new IllegalStateException("DESede engine not initialised");
        } else if (inOff + 8 > in.length) {
            throw new DataLengthException("input buffer too short");
        } else if (outOff + 8 > out.length) {
            throw new OutputLengthException("output buffer too short");
        } else {
            byte[] temp = new byte[8];
            if (this.forEncryption) {
                desFunc(this.workingKey1, in, inOff, temp, 0);
                desFunc(this.workingKey2, temp, 0, temp, 0);
                desFunc(this.workingKey3, temp, 0, out, outOff);
            } else {
                desFunc(this.workingKey3, in, inOff, temp, 0);
                desFunc(this.workingKey2, temp, 0, temp, 0);
                desFunc(this.workingKey1, temp, 0, out, outOff);
            }
            return 8;
        }
    }

    public void reset() {
    }
}
