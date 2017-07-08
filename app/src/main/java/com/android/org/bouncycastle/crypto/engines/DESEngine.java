package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.params.KeyParameter;

public class DESEngine implements BlockCipher {
    protected static final int BLOCK_SIZE = 8;
    private static final int[] SP1 = null;
    private static final int[] SP2 = null;
    private static final int[] SP3 = null;
    private static final int[] SP4 = null;
    private static final int[] SP5 = null;
    private static final int[] SP6 = null;
    private static final int[] SP7 = null;
    private static final int[] SP8 = null;
    private static final int[] bigbyte = null;
    private static final short[] bytebit = null;
    private static final byte[] pc1 = null;
    private static final byte[] pc2 = null;
    private static final byte[] totrot = null;
    private int[] workingKey;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.engines.DESEngine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.engines.DESEngine.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.engines.DESEngine.<clinit>():void");
    }

    public DESEngine() {
        this.workingKey = null;
    }

    public void init(boolean encrypting, CipherParameters params) {
        if (!(params instanceof KeyParameter)) {
            throw new IllegalArgumentException("invalid parameter passed to DES init - " + params.getClass().getName());
        } else if (((KeyParameter) params).getKey().length > BLOCK_SIZE) {
            throw new IllegalArgumentException("DES key too long - should be 8 bytes");
        } else {
            this.workingKey = generateWorkingKey(encrypting, ((KeyParameter) params).getKey());
        }
    }

    public String getAlgorithmName() {
        return "DES";
    }

    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    public int processBlock(byte[] in, int inOff, byte[] out, int outOff) {
        if (this.workingKey == null) {
            throw new IllegalStateException("DES engine not initialised");
        } else if (inOff + BLOCK_SIZE > in.length) {
            throw new DataLengthException("input buffer too short");
        } else if (outOff + BLOCK_SIZE > out.length) {
            throw new OutputLengthException("output buffer too short");
        } else {
            desFunc(this.workingKey, in, inOff, out, outOff);
            return BLOCK_SIZE;
        }
    }

    public void reset() {
    }

    protected int[] generateWorkingKey(boolean encrypting, byte[] key) {
        int j;
        int i;
        int[] newKey = new int[32];
        boolean[] pc1m = new boolean[56];
        boolean[] pcr = new boolean[56];
        for (j = 0; j < 56; j++) {
            int l = pc1[j];
            pc1m[j] = (key[l >>> 3] & bytebit[l & 7]) != 0;
        }
        for (i = 0; i < 16; i++) {
            int m;
            if (encrypting) {
                m = i << 1;
            } else {
                m = (15 - i) << 1;
            }
            int n = m + 1;
            newKey[n] = 0;
            newKey[m] = 0;
            for (j = 0; j < 28; j++) {
                l = j + totrot[i];
                if (l < 28) {
                    pcr[j] = pc1m[l];
                } else {
                    pcr[j] = pc1m[l - 28];
                }
            }
            for (j = 28; j < 56; j++) {
                l = j + totrot[i];
                if (l < 56) {
                    pcr[j] = pc1m[l];
                } else {
                    pcr[j] = pc1m[l - 28];
                }
            }
            for (j = 0; j < 24; j++) {
                if (pcr[pc2[j]]) {
                    newKey[m] = newKey[m] | bigbyte[j];
                }
                if (pcr[pc2[j + 24]]) {
                    newKey[n] = newKey[n] | bigbyte[j];
                }
            }
        }
        for (i = 0; i != 32; i += 2) {
            int i1 = newKey[i];
            int i2 = newKey[i + 1];
            newKey[i] = ((((16515072 & i1) << 6) | ((i1 & 4032) << 10)) | ((16515072 & i2) >>> 10)) | ((i2 & 4032) >>> 6);
            newKey[i + 1] = ((((258048 & i1) << 12) | ((i1 & 63) << 16)) | ((258048 & i2) >>> 4)) | (i2 & 63);
        }
        return newKey;
    }

    protected void desFunc(int[] wKey, byte[] in, int inOff, byte[] out, int outOff) {
        int left = ((((in[inOff + 0] & 255) << 24) | ((in[inOff + 1] & 255) << 16)) | ((in[inOff + 2] & 255) << BLOCK_SIZE)) | (in[inOff + 3] & 255);
        int right = ((((in[inOff + 4] & 255) << 24) | ((in[inOff + 5] & 255) << 16)) | ((in[inOff + 6] & 255) << BLOCK_SIZE)) | (in[inOff + 7] & 255);
        int work = ((left >>> 4) ^ right) & 252645135;
        right ^= work;
        left ^= work << 4;
        work = ((left >>> 16) ^ right) & 65535;
        right ^= work;
        left ^= work << 16;
        work = ((right >>> 2) ^ left) & 858993459;
        left ^= work;
        right ^= work << 2;
        work = ((right >>> BLOCK_SIZE) ^ left) & 16711935;
        left ^= work;
        right ^= work << BLOCK_SIZE;
        right = ((right << 1) | ((right >>> 31) & 1)) & -1;
        work = (left ^ right) & -1431655766;
        left ^= work;
        right ^= work;
        left = ((left << 1) | ((left >>> 31) & 1)) & -1;
        for (int round = 0; round < BLOCK_SIZE; round++) {
            work = ((right << 28) | (right >>> 4)) ^ wKey[(round * 4) + 0];
            int fval = ((SP7[work & 63] | SP5[(work >>> BLOCK_SIZE) & 63]) | SP3[(work >>> 16) & 63]) | SP1[(work >>> 24) & 63];
            work = right ^ wKey[(round * 4) + 1];
            left ^= (((fval | SP8[work & 63]) | SP6[(work >>> BLOCK_SIZE) & 63]) | SP4[(work >>> 16) & 63]) | SP2[(work >>> 24) & 63];
            work = ((left << 28) | (left >>> 4)) ^ wKey[(round * 4) + 2];
            fval = ((SP7[work & 63] | SP5[(work >>> BLOCK_SIZE) & 63]) | SP3[(work >>> 16) & 63]) | SP1[(work >>> 24) & 63];
            work = left ^ wKey[(round * 4) + 3];
            right ^= (((fval | SP8[work & 63]) | SP6[(work >>> BLOCK_SIZE) & 63]) | SP4[(work >>> 16) & 63]) | SP2[(work >>> 24) & 63];
        }
        right = (right << 31) | (right >>> 1);
        work = (left ^ right) & -1431655766;
        left ^= work;
        right ^= work;
        left = (left << 31) | (left >>> 1);
        work = ((left >>> BLOCK_SIZE) ^ right) & 16711935;
        right ^= work;
        left ^= work << BLOCK_SIZE;
        work = ((left >>> 2) ^ right) & 858993459;
        right ^= work;
        left ^= work << 2;
        work = ((right >>> 16) ^ left) & 65535;
        left ^= work;
        right ^= work << 16;
        work = ((right >>> 4) ^ left) & 252645135;
        left ^= work;
        right ^= work << 4;
        out[outOff + 0] = (byte) ((right >>> 24) & 255);
        out[outOff + 1] = (byte) ((right >>> 16) & 255);
        out[outOff + 2] = (byte) ((right >>> BLOCK_SIZE) & 255);
        out[outOff + 3] = (byte) (right & 255);
        out[outOff + 4] = (byte) ((left >>> 24) & 255);
        out[outOff + 5] = (byte) ((left >>> 16) & 255);
        out[outOff + 6] = (byte) ((left >>> BLOCK_SIZE) & 255);
        out[outOff + 7] = (byte) (left & 255);
    }
}
