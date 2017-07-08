package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.params.KeyParameter;

public final class BlowfishEngine implements BlockCipher {
    private static final int BLOCK_SIZE = 8;
    private static final int[] KP = null;
    private static final int[] KS0 = null;
    private static final int[] KS1 = null;
    private static final int[] KS2 = null;
    private static final int[] KS3 = null;
    private static final int P_SZ = 18;
    private static final int ROUNDS = 16;
    private static final int SBOX_SK = 256;
    private final int[] P;
    private final int[] S0;
    private final int[] S1;
    private final int[] S2;
    private final int[] S3;
    private boolean encrypting;
    private byte[] workingKey;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.engines.BlowfishEngine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.engines.BlowfishEngine.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.engines.BlowfishEngine.<clinit>():void");
    }

    public BlowfishEngine() {
        this.encrypting = false;
        this.workingKey = null;
        this.S0 = new int[SBOX_SK];
        this.S1 = new int[SBOX_SK];
        this.S2 = new int[SBOX_SK];
        this.S3 = new int[SBOX_SK];
        this.P = new int[P_SZ];
    }

    public void init(boolean encrypting, CipherParameters params) {
        if (params instanceof KeyParameter) {
            this.encrypting = encrypting;
            this.workingKey = ((KeyParameter) params).getKey();
            setKey(this.workingKey);
            return;
        }
        throw new IllegalArgumentException("invalid parameter passed to Blowfish init - " + params.getClass().getName());
    }

    public String getAlgorithmName() {
        return "Blowfish";
    }

    public final int processBlock(byte[] in, int inOff, byte[] out, int outOff) {
        if (this.workingKey == null) {
            throw new IllegalStateException("Blowfish not initialised");
        } else if (inOff + BLOCK_SIZE > in.length) {
            throw new DataLengthException("input buffer too short");
        } else if (outOff + BLOCK_SIZE > out.length) {
            throw new OutputLengthException("output buffer too short");
        } else {
            if (this.encrypting) {
                encryptBlock(in, inOff, out, outOff);
            } else {
                decryptBlock(in, inOff, out, outOff);
            }
            return BLOCK_SIZE;
        }
    }

    public void reset() {
    }

    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    private int F(int x) {
        return ((this.S0[x >>> 24] + this.S1[(x >>> ROUNDS) & 255]) ^ this.S2[(x >>> BLOCK_SIZE) & 255]) + this.S3[x & 255];
    }

    private void processTable(int xl, int xr, int[] table) {
        int size = table.length;
        for (int s = 0; s < size; s += 2) {
            xl ^= this.P[0];
            for (int i = 1; i < ROUNDS; i += 2) {
                xr ^= F(xl) ^ this.P[i];
                xl ^= F(xr) ^ this.P[i + 1];
            }
            table[s] = xr ^ this.P[17];
            table[s + 1] = xl;
            xr = xl;
            xl = table[s];
        }
    }

    private void setKey(byte[] key) {
        System.arraycopy(KS0, 0, this.S0, 0, SBOX_SK);
        System.arraycopy(KS1, 0, this.S1, 0, SBOX_SK);
        System.arraycopy(KS2, 0, this.S2, 0, SBOX_SK);
        System.arraycopy(KS3, 0, this.S3, 0, SBOX_SK);
        System.arraycopy(KP, 0, this.P, 0, P_SZ);
        int keyLength = key.length;
        int keyIndex = 0;
        int i = 0;
        while (i < P_SZ) {
            int data = 0;
            int j = 0;
            int keyIndex2 = keyIndex;
            while (j < 4) {
                keyIndex = keyIndex2 + 1;
                data = (data << BLOCK_SIZE) | (key[keyIndex2] & 255);
                if (keyIndex >= keyLength) {
                    keyIndex = 0;
                }
                j++;
                keyIndex2 = keyIndex;
            }
            int[] iArr = this.P;
            iArr[i] = iArr[i] ^ data;
            i++;
            keyIndex = keyIndex2;
        }
        processTable(0, 0, this.P);
        processTable(this.P[ROUNDS], this.P[17], this.S0);
        processTable(this.S0[254], this.S0[255], this.S1);
        processTable(this.S1[254], this.S1[255], this.S2);
        processTable(this.S2[254], this.S2[255], this.S3);
    }

    private void encryptBlock(byte[] src, int srcIndex, byte[] dst, int dstIndex) {
        int xl = BytesTo32bits(src, srcIndex);
        int xr = BytesTo32bits(src, srcIndex + 4);
        xl ^= this.P[0];
        for (int i = 1; i < ROUNDS; i += 2) {
            xr ^= F(xl) ^ this.P[i];
            xl ^= F(xr) ^ this.P[i + 1];
        }
        Bits32ToBytes(xr ^ this.P[17], dst, dstIndex);
        Bits32ToBytes(xl, dst, dstIndex + 4);
    }

    private void decryptBlock(byte[] src, int srcIndex, byte[] dst, int dstIndex) {
        int xl = BytesTo32bits(src, srcIndex);
        int xr = BytesTo32bits(src, srcIndex + 4);
        xl ^= this.P[17];
        for (int i = ROUNDS; i > 0; i -= 2) {
            xr ^= F(xl) ^ this.P[i];
            xl ^= F(xr) ^ this.P[i - 1];
        }
        Bits32ToBytes(xr ^ this.P[0], dst, dstIndex);
        Bits32ToBytes(xl, dst, dstIndex + 4);
    }

    private int BytesTo32bits(byte[] b, int i) {
        return ((((b[i] & 255) << 24) | ((b[i + 1] & 255) << ROUNDS)) | ((b[i + 2] & 255) << BLOCK_SIZE)) | (b[i + 3] & 255);
    }

    private void Bits32ToBytes(int in, byte[] b, int offset) {
        b[offset + 3] = (byte) in;
        b[offset + 2] = (byte) (in >> BLOCK_SIZE);
        b[offset + 1] = (byte) (in >> ROUNDS);
        b[offset] = (byte) (in >> 24);
    }
}
