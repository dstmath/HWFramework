package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.params.DESParameters;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.util.Pack;
import java.lang.reflect.Array;

public class AESFastEngine implements BlockCipher {
    private static final int BLOCK_SIZE = 16;
    private static final byte[] S = null;
    private static final byte[] Si = null;
    private static final int[] T = null;
    private static final int[] Tinv = null;
    private static final int m1 = -2139062144;
    private static final int m2 = 2139062143;
    private static final int m3 = 27;
    private static final int m4 = -1061109568;
    private static final int m5 = 1061109567;
    private static final int[] rcon = null;
    private int C0;
    private int C1;
    private int C2;
    private int C3;
    private int ROUNDS;
    private int[][] WorkingKey;
    private boolean forEncryption;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.engines.AESFastEngine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.engines.AESFastEngine.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.engines.AESFastEngine.<clinit>():void");
    }

    private static int shift(int r, int shift) {
        return (r >>> shift) | (r << (-shift));
    }

    private static int FFmulX(int x) {
        return ((m2 & x) << 1) ^ (((m1 & x) >>> 7) * m3);
    }

    private static int FFmulX2(int x) {
        int t1 = x & m4;
        t1 ^= t1 >>> 1;
        return ((t1 >>> 2) ^ ((m5 & x) << 2)) ^ (t1 >>> 5);
    }

    private static int inv_mcol(int x) {
        int t0 = x;
        int t1 = x ^ shift(x, 8);
        t0 = x ^ FFmulX(t1);
        t1 ^= FFmulX2(t0);
        return t0 ^ (shift(t1, BLOCK_SIZE) ^ t1);
    }

    private static int subWord(int x) {
        int i0 = x;
        return ((((S[(x >>> 8) & 255] & 255) << 8) | (S[x & 255] & 255)) | ((S[(x >>> BLOCK_SIZE) & 255] & 255) << BLOCK_SIZE)) | ((S[(x >>> 24) & 255] & 255) << 24);
    }

    private int[][] generateWorkingKey(byte[] key, boolean forEncryption) {
        int keyLen = key.length;
        if (keyLen < BLOCK_SIZE || keyLen > 32 || (keyLen & 7) != 0) {
            throw new IllegalArgumentException("Key length not 128/192/256 bits.");
        }
        int i;
        int KC = keyLen >>> 2;
        this.ROUNDS = KC + 6;
        Class cls = Integer.TYPE;
        r18 = new int[2];
        r18[0] = this.ROUNDS + 1;
        r18[1] = 4;
        int[][] W = (int[][]) Array.newInstance(cls, r18);
        int t0;
        int t1;
        int t2;
        int t3;
        int t4;
        int t5;
        int rcon;
        int u;
        switch (KC) {
            case ECCurve.COORD_JACOBIAN_MODIFIED /*4*/:
                t0 = Pack.littleEndianToInt(key, 0);
                W[0][0] = t0;
                t1 = Pack.littleEndianToInt(key, 4);
                W[0][1] = t1;
                t2 = Pack.littleEndianToInt(key, 8);
                W[0][2] = t2;
                t3 = Pack.littleEndianToInt(key, 12);
                W[0][3] = t3;
                for (i = 1; i <= 10; i++) {
                    t0 ^= subWord(shift(t3, 8)) ^ rcon[i - 1];
                    W[i][0] = t0;
                    t1 ^= t0;
                    W[i][1] = t1;
                    t2 ^= t1;
                    W[i][2] = t2;
                    t3 ^= t2;
                    W[i][3] = t3;
                }
                break;
            case ECCurve.COORD_LAMBDA_PROJECTIVE /*6*/:
                t0 = Pack.littleEndianToInt(key, 0);
                W[0][0] = t0;
                t1 = Pack.littleEndianToInt(key, 4);
                W[0][1] = t1;
                t2 = Pack.littleEndianToInt(key, 8);
                W[0][2] = t2;
                t3 = Pack.littleEndianToInt(key, 12);
                W[0][3] = t3;
                t4 = Pack.littleEndianToInt(key, BLOCK_SIZE);
                W[1][0] = t4;
                t5 = Pack.littleEndianToInt(key, 20);
                W[1][1] = t5;
                rcon = 2;
                t0 ^= subWord(shift(t5, 8)) ^ 1;
                W[1][2] = t0;
                t1 ^= t0;
                W[1][3] = t1;
                t2 ^= t1;
                W[2][0] = t2;
                t3 ^= t2;
                W[2][1] = t3;
                t4 ^= t3;
                W[2][2] = t4;
                t5 ^= t4;
                W[2][3] = t5;
                for (i = 3; i < 12; i += 3) {
                    u = subWord(shift(t5, 8)) ^ rcon;
                    rcon <<= 1;
                    t0 ^= u;
                    W[i][0] = t0;
                    t1 ^= t0;
                    W[i][1] = t1;
                    t2 ^= t1;
                    W[i][2] = t2;
                    t3 ^= t2;
                    W[i][3] = t3;
                    t4 ^= t3;
                    W[i + 1][0] = t4;
                    t5 ^= t4;
                    W[i + 1][1] = t5;
                    u = subWord(shift(t5, 8)) ^ rcon;
                    rcon <<= 1;
                    t0 ^= u;
                    W[i + 1][2] = t0;
                    t1 ^= t0;
                    W[i + 1][3] = t1;
                    t2 ^= t1;
                    W[i + 2][0] = t2;
                    t3 ^= t2;
                    W[i + 2][1] = t3;
                    t4 ^= t3;
                    W[i + 2][2] = t4;
                    t5 ^= t4;
                    W[i + 2][3] = t5;
                }
                t0 ^= subWord(shift(t5, 8)) ^ rcon;
                W[12][0] = t0;
                t1 ^= t0;
                W[12][1] = t1;
                t2 ^= t1;
                W[12][2] = t2;
                W[12][3] = t3 ^ t2;
                break;
            case DESParameters.DES_KEY_LENGTH /*8*/:
                t0 = Pack.littleEndianToInt(key, 0);
                W[0][0] = t0;
                t1 = Pack.littleEndianToInt(key, 4);
                W[0][1] = t1;
                t2 = Pack.littleEndianToInt(key, 8);
                W[0][2] = t2;
                t3 = Pack.littleEndianToInt(key, 12);
                W[0][3] = t3;
                t4 = Pack.littleEndianToInt(key, BLOCK_SIZE);
                W[1][0] = t4;
                t5 = Pack.littleEndianToInt(key, 20);
                W[1][1] = t5;
                int t6 = Pack.littleEndianToInt(key, 24);
                W[1][2] = t6;
                int t7 = Pack.littleEndianToInt(key, 28);
                W[1][3] = t7;
                rcon = 1;
                for (i = 2; i < 14; i += 2) {
                    u = subWord(shift(t7, 8)) ^ rcon;
                    rcon <<= 1;
                    t0 ^= u;
                    W[i][0] = t0;
                    t1 ^= t0;
                    W[i][1] = t1;
                    t2 ^= t1;
                    W[i][2] = t2;
                    t3 ^= t2;
                    W[i][3] = t3;
                    t4 ^= subWord(t3);
                    W[i + 1][0] = t4;
                    t5 ^= t4;
                    W[i + 1][1] = t5;
                    t6 ^= t5;
                    W[i + 1][2] = t6;
                    t7 ^= t6;
                    W[i + 1][3] = t7;
                }
                t0 ^= subWord(shift(t7, 8)) ^ rcon;
                W[14][0] = t0;
                t1 ^= t0;
                W[14][1] = t1;
                t2 ^= t1;
                W[14][2] = t2;
                W[14][3] = t3 ^ t2;
                break;
            default:
                throw new IllegalStateException("Should never get here");
        }
        if (!forEncryption) {
            int j = 1;
            while (true) {
                int i2 = this.ROUNDS;
                if (j < r0) {
                    for (i = 0; i < 4; i++) {
                        W[j][i] = inv_mcol(W[j][i]);
                    }
                    j++;
                }
            }
        }
        return W;
    }

    public AESFastEngine() {
        this.WorkingKey = null;
    }

    public void init(boolean forEncryption, CipherParameters params) {
        if (params instanceof KeyParameter) {
            this.WorkingKey = generateWorkingKey(((KeyParameter) params).getKey(), forEncryption);
            this.forEncryption = forEncryption;
            return;
        }
        throw new IllegalArgumentException("invalid parameter passed to AES init - " + params.getClass().getName());
    }

    public String getAlgorithmName() {
        return "AES";
    }

    public int getBlockSize() {
        return BLOCK_SIZE;
    }

    public int processBlock(byte[] in, int inOff, byte[] out, int outOff) {
        if (this.WorkingKey == null) {
            throw new IllegalStateException("AES engine not initialised");
        } else if (inOff + BLOCK_SIZE > in.length) {
            throw new DataLengthException("input buffer too short");
        } else if (outOff + BLOCK_SIZE > out.length) {
            throw new OutputLengthException("output buffer too short");
        } else {
            unpackBlock(in, inOff);
            if (this.forEncryption) {
                encryptBlock(this.WorkingKey);
            } else {
                decryptBlock(this.WorkingKey);
            }
            packBlock(out, outOff);
            return BLOCK_SIZE;
        }
    }

    public void reset() {
    }

    private void unpackBlock(byte[] bytes, int off) {
        this.C0 = Pack.littleEndianToInt(bytes, off);
        this.C1 = Pack.littleEndianToInt(bytes, off + 4);
        this.C2 = Pack.littleEndianToInt(bytes, off + 8);
        this.C3 = Pack.littleEndianToInt(bytes, off + 12);
    }

    private void packBlock(byte[] bytes, int off) {
        Pack.intToLittleEndian(this.C0, bytes, off);
        Pack.intToLittleEndian(this.C1, bytes, off + 4);
        Pack.intToLittleEndian(this.C2, bytes, off + 8);
        Pack.intToLittleEndian(this.C3, bytes, off + 12);
    }

    private void encryptBlock(int[][] KW) {
        int i0;
        int r0;
        int r1;
        int r2;
        int r;
        int t0 = this.C0 ^ KW[0][0];
        int t1 = this.C1 ^ KW[0][1];
        int t2 = this.C2 ^ KW[0][2];
        int r3 = 1;
        int r32 = this.C3 ^ KW[0][3];
        while (r3 < this.ROUNDS - 1) {
            i0 = t0;
            r0 = (((T[t0 & 255] ^ T[((t1 >>> 8) & 255) + 256]) ^ T[((t2 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((r32 >>> 24) & 255) + 768]) ^ KW[r3][0];
            i0 = t1;
            r1 = (((T[t1 & 255] ^ T[((t2 >>> 8) & 255) + 256]) ^ T[((r32 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((t0 >>> 24) & 255) + 768]) ^ KW[r3][1];
            i0 = t2;
            r2 = (((T[t2 & 255] ^ T[((r32 >>> 8) & 255) + 256]) ^ T[((t0 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((t1 >>> 24) & 255) + 768]) ^ KW[r3][2];
            i0 = r32;
            r = r3 + 1;
            r32 = (((T[r32 & 255] ^ T[((t0 >>> 8) & 255) + 256]) ^ T[((t1 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((t2 >>> 24) & 255) + 768]) ^ KW[r3][3];
            i0 = r0;
            t0 = (((T[r0 & 255] ^ T[((r1 >>> 8) & 255) + 256]) ^ T[((r2 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((r32 >>> 24) & 255) + 768]) ^ KW[r][0];
            i0 = r1;
            t1 = (((T[r1 & 255] ^ T[((r2 >>> 8) & 255) + 256]) ^ T[((r32 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((r0 >>> 24) & 255) + 768]) ^ KW[r][1];
            i0 = r2;
            t2 = (((T[r2 & 255] ^ T[((r32 >>> 8) & 255) + 256]) ^ T[((r0 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((r1 >>> 24) & 255) + 768]) ^ KW[r][2];
            i0 = r32;
            r3 = r + 1;
            r32 = (((T[r32 & 255] ^ T[((r0 >>> 8) & 255) + 256]) ^ T[((r1 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((r2 >>> 24) & 255) + 768]) ^ KW[r][3];
        }
        i0 = t0;
        r0 = (((T[t0 & 255] ^ T[((t1 >>> 8) & 255) + 256]) ^ T[((t2 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((r32 >>> 24) & 255) + 768]) ^ KW[r3][0];
        i0 = t1;
        r1 = (((T[t1 & 255] ^ T[((t2 >>> 8) & 255) + 256]) ^ T[((r32 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((t0 >>> 24) & 255) + 768]) ^ KW[r3][1];
        i0 = t2;
        r2 = (((T[t2 & 255] ^ T[((r32 >>> 8) & 255) + 256]) ^ T[((t0 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((t1 >>> 24) & 255) + 768]) ^ KW[r3][2];
        i0 = r32;
        r = r3 + 1;
        r32 = (((T[r32 & 255] ^ T[((t0 >>> 8) & 255) + 256]) ^ T[((t1 >>> BLOCK_SIZE) & 255) + 512]) ^ T[((t2 >>> 24) & 255) + 768]) ^ KW[r3][3];
        i0 = r0;
        this.C0 = (((((S[(r1 >>> 8) & 255] & 255) << 8) ^ (S[r0 & 255] & 255)) ^ ((S[(r2 >>> BLOCK_SIZE) & 255] & 255) << BLOCK_SIZE)) ^ ((S[(r32 >>> 24) & 255] & 255) << 24)) ^ KW[r][0];
        i0 = r1;
        this.C1 = (((((S[(r2 >>> 8) & 255] & 255) << 8) ^ (S[r1 & 255] & 255)) ^ ((S[(r32 >>> BLOCK_SIZE) & 255] & 255) << BLOCK_SIZE)) ^ ((S[(r0 >>> 24) & 255] & 255) << 24)) ^ KW[r][1];
        i0 = r2;
        this.C2 = (((((S[(r32 >>> 8) & 255] & 255) << 8) ^ (S[r2 & 255] & 255)) ^ ((S[(r0 >>> BLOCK_SIZE) & 255] & 255) << BLOCK_SIZE)) ^ ((S[(r1 >>> 24) & 255] & 255) << 24)) ^ KW[r][2];
        i0 = r32;
        this.C3 = (((((S[(r0 >>> 8) & 255] & 255) << 8) ^ (S[r32 & 255] & 255)) ^ ((S[(r1 >>> BLOCK_SIZE) & 255] & 255) << BLOCK_SIZE)) ^ ((S[(r2 >>> 24) & 255] & 255) << 24)) ^ KW[r][3];
    }

    private void decryptBlock(int[][] KW) {
        int i0;
        int r0;
        int r1;
        int r2;
        int t0 = this.C0 ^ KW[this.ROUNDS][0];
        int t1 = this.C1 ^ KW[this.ROUNDS][1];
        int t2 = this.C2 ^ KW[this.ROUNDS][2];
        int r3 = this.C3 ^ KW[this.ROUNDS][3];
        int r = this.ROUNDS - 1;
        while (r > 1) {
            i0 = t0;
            r0 = (((Tinv[t0 & 255] ^ Tinv[((r3 >>> 8) & 255) + 256]) ^ Tinv[((t2 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((t1 >>> 24) & 255) + 768]) ^ KW[r][0];
            i0 = t1;
            r1 = (((Tinv[t1 & 255] ^ Tinv[((t0 >>> 8) & 255) + 256]) ^ Tinv[((r3 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((t2 >>> 24) & 255) + 768]) ^ KW[r][1];
            i0 = t2;
            r2 = (((Tinv[t2 & 255] ^ Tinv[((t1 >>> 8) & 255) + 256]) ^ Tinv[((t0 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((r3 >>> 24) & 255) + 768]) ^ KW[r][2];
            i0 = r3;
            int r4 = r - 1;
            r3 = (((Tinv[r3 & 255] ^ Tinv[((t2 >>> 8) & 255) + 256]) ^ Tinv[((t1 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((t0 >>> 24) & 255) + 768]) ^ KW[r][3];
            i0 = r0;
            t0 = (((Tinv[r0 & 255] ^ Tinv[((r3 >>> 8) & 255) + 256]) ^ Tinv[((r2 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((r1 >>> 24) & 255) + 768]) ^ KW[r4][0];
            i0 = r1;
            t1 = (((Tinv[r1 & 255] ^ Tinv[((r0 >>> 8) & 255) + 256]) ^ Tinv[((r3 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((r2 >>> 24) & 255) + 768]) ^ KW[r4][1];
            i0 = r2;
            t2 = (((Tinv[r2 & 255] ^ Tinv[((r1 >>> 8) & 255) + 256]) ^ Tinv[((r0 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((r3 >>> 24) & 255) + 768]) ^ KW[r4][2];
            i0 = r3;
            r = r4 - 1;
            r3 = (((Tinv[r3 & 255] ^ Tinv[((r2 >>> 8) & 255) + 256]) ^ Tinv[((r1 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((r0 >>> 24) & 255) + 768]) ^ KW[r4][3];
        }
        i0 = t0;
        r0 = (((Tinv[t0 & 255] ^ Tinv[((r3 >>> 8) & 255) + 256]) ^ Tinv[((t2 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((t1 >>> 24) & 255) + 768]) ^ KW[1][0];
        i0 = t1;
        r1 = (((Tinv[t1 & 255] ^ Tinv[((t0 >>> 8) & 255) + 256]) ^ Tinv[((r3 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((t2 >>> 24) & 255) + 768]) ^ KW[1][1];
        i0 = t2;
        r2 = (((Tinv[t2 & 255] ^ Tinv[((t1 >>> 8) & 255) + 256]) ^ Tinv[((t0 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((r3 >>> 24) & 255) + 768]) ^ KW[1][2];
        i0 = r3;
        r3 = (((Tinv[r3 & 255] ^ Tinv[((t2 >>> 8) & 255) + 256]) ^ Tinv[((t1 >>> BLOCK_SIZE) & 255) + 512]) ^ Tinv[((t0 >>> 24) & 255) + 768]) ^ KW[1][3];
        i0 = r0;
        this.C0 = (((((Si[(r3 >>> 8) & 255] & 255) << 8) ^ (Si[r0 & 255] & 255)) ^ ((Si[(r2 >>> BLOCK_SIZE) & 255] & 255) << BLOCK_SIZE)) ^ ((Si[(r1 >>> 24) & 255] & 255) << 24)) ^ KW[0][0];
        i0 = r1;
        this.C1 = (((((Si[(r0 >>> 8) & 255] & 255) << 8) ^ (Si[r1 & 255] & 255)) ^ ((Si[(r3 >>> BLOCK_SIZE) & 255] & 255) << BLOCK_SIZE)) ^ ((Si[(r2 >>> 24) & 255] & 255) << 24)) ^ KW[0][1];
        i0 = r2;
        this.C2 = (((((Si[(r1 >>> 8) & 255] & 255) << 8) ^ (Si[r2 & 255] & 255)) ^ ((Si[(r0 >>> BLOCK_SIZE) & 255] & 255) << BLOCK_SIZE)) ^ ((Si[(r3 >>> 24) & 255] & 255) << 24)) ^ KW[0][2];
        i0 = r3;
        this.C3 = (((((Si[(r2 >>> 8) & 255] & 255) << 8) ^ (Si[r3 & 255] & 255)) ^ ((Si[(r1 >>> BLOCK_SIZE) & 255] & 255) << BLOCK_SIZE)) ^ ((Si[(r0 >>> 24) & 255] & 255) << 24)) ^ KW[0][3];
    }
}
