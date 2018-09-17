package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.asn1.x509.ReasonFlags;
import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.math.ec.ECFieldElement.F2m;

public final class TwofishEngine implements BlockCipher {
    private static final int BLOCK_SIZE = 16;
    private static final int GF256_FDBK = 361;
    private static final int GF256_FDBK_2 = 180;
    private static final int GF256_FDBK_4 = 90;
    private static final int INPUT_WHITEN = 0;
    private static final int MAX_KEY_BITS = 256;
    private static final int MAX_ROUNDS = 16;
    private static final int OUTPUT_WHITEN = 4;
    private static final byte[][] P = null;
    private static final int P_00 = 1;
    private static final int P_01 = 0;
    private static final int P_02 = 0;
    private static final int P_03 = 1;
    private static final int P_04 = 1;
    private static final int P_10 = 0;
    private static final int P_11 = 0;
    private static final int P_12 = 1;
    private static final int P_13 = 1;
    private static final int P_14 = 0;
    private static final int P_20 = 1;
    private static final int P_21 = 1;
    private static final int P_22 = 0;
    private static final int P_23 = 0;
    private static final int P_24 = 0;
    private static final int P_30 = 0;
    private static final int P_31 = 1;
    private static final int P_32 = 1;
    private static final int P_33 = 0;
    private static final int P_34 = 1;
    private static final int ROUNDS = 16;
    private static final int ROUND_SUBKEYS = 8;
    private static final int RS_GF_FDBK = 333;
    private static final int SK_BUMP = 16843009;
    private static final int SK_ROTL = 9;
    private static final int SK_STEP = 33686018;
    private static final int TOTAL_SUBKEYS = 40;
    private boolean encrypting;
    private int[] gMDS0;
    private int[] gMDS1;
    private int[] gMDS2;
    private int[] gMDS3;
    private int[] gSBox;
    private int[] gSubKeys;
    private int k64Cnt;
    private byte[] workingKey;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.engines.TwofishEngine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.crypto.engines.TwofishEngine.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.engines.TwofishEngine.<clinit>():void");
    }

    public TwofishEngine() {
        this.encrypting = false;
        this.gMDS0 = new int[MAX_KEY_BITS];
        this.gMDS1 = new int[MAX_KEY_BITS];
        this.gMDS2 = new int[MAX_KEY_BITS];
        this.gMDS3 = new int[MAX_KEY_BITS];
        this.k64Cnt = P_33;
        this.workingKey = null;
        int[] m1 = new int[2];
        int[] mX = new int[2];
        int[] mY = new int[2];
        for (int i = P_33; i < MAX_KEY_BITS; i += P_34) {
            int j = P[P_33][i] & 255;
            m1[P_33] = j;
            mX[P_33] = Mx_X(j) & 255;
            mY[P_33] = Mx_Y(j) & 255;
            j = P[P_34][i] & 255;
            m1[P_34] = j;
            mX[P_34] = Mx_X(j) & 255;
            mY[P_34] = Mx_Y(j) & 255;
            this.gMDS0[i] = ((m1[P_34] | (mX[P_34] << ROUND_SUBKEYS)) | (mY[P_34] << ROUNDS)) | (mY[P_34] << 24);
            this.gMDS1[i] = ((mY[P_33] | (mY[P_33] << ROUND_SUBKEYS)) | (mX[P_33] << ROUNDS)) | (m1[P_33] << 24);
            this.gMDS2[i] = ((mX[P_34] | (mY[P_34] << ROUND_SUBKEYS)) | (m1[P_34] << ROUNDS)) | (mY[P_34] << 24);
            this.gMDS3[i] = ((mX[P_33] | (m1[P_33] << ROUND_SUBKEYS)) | (mY[P_33] << ROUNDS)) | (mX[P_33] << 24);
        }
    }

    public void init(boolean encrypting, CipherParameters params) {
        if (params instanceof KeyParameter) {
            this.encrypting = encrypting;
            this.workingKey = ((KeyParameter) params).getKey();
            this.k64Cnt = this.workingKey.length / ROUND_SUBKEYS;
            setKey(this.workingKey);
            return;
        }
        throw new IllegalArgumentException("invalid parameter passed to Twofish init - " + params.getClass().getName());
    }

    public String getAlgorithmName() {
        return "Twofish";
    }

    public int processBlock(byte[] in, int inOff, byte[] out, int outOff) {
        if (this.workingKey == null) {
            throw new IllegalStateException("Twofish not initialised");
        } else if (inOff + ROUNDS > in.length) {
            throw new DataLengthException("input buffer too short");
        } else if (outOff + ROUNDS > out.length) {
            throw new OutputLengthException("output buffer too short");
        } else {
            if (this.encrypting) {
                encryptBlock(in, inOff, out, outOff);
            } else {
                decryptBlock(in, inOff, out, outOff);
            }
            return ROUNDS;
        }
    }

    public void reset() {
        if (this.workingKey != null) {
            setKey(this.workingKey);
        }
    }

    public int getBlockSize() {
        return ROUNDS;
    }

    private void setKey(byte[] key) {
        int[] k32e = new int[OUTPUT_WHITEN];
        int[] k32o = new int[OUTPUT_WHITEN];
        int[] sBoxKeys = new int[OUTPUT_WHITEN];
        this.gSubKeys = new int[TOTAL_SUBKEYS];
        int i = this.k64Cnt;
        if (r0 < P_34) {
            throw new IllegalArgumentException("Key size less than 64 bits");
        }
        i = this.k64Cnt;
        if (r0 > OUTPUT_WHITEN) {
            throw new IllegalArgumentException("Key size larger than 256 bits");
        }
        int i2 = P_33;
        while (true) {
            i = this.k64Cnt;
            if (i2 >= r0) {
                break;
            }
            int p = i2 * ROUND_SUBKEYS;
            k32e[i2] = BytesTo32Bits(key, p);
            k32o[i2] = BytesTo32Bits(key, p + OUTPUT_WHITEN);
            sBoxKeys[(this.k64Cnt - 1) - i2] = RS_MDS_Encode(k32e[i2], k32o[i2]);
            i2 += P_34;
        }
        for (i2 = P_33; i2 < 20; i2 += P_34) {
            int q = i2 * SK_STEP;
            int A = F32(q, k32e);
            int B = F32(SK_BUMP + q, k32o);
            B = (B << ROUND_SUBKEYS) | (B >>> 24);
            A += B;
            this.gSubKeys[i2 * 2] = A;
            A += B;
            this.gSubKeys[(i2 * 2) + P_34] = (A << SK_ROTL) | (A >>> 23);
        }
        int k0 = sBoxKeys[P_33];
        int k1 = sBoxKeys[P_34];
        int k2 = sBoxKeys[2];
        int k3 = sBoxKeys[3];
        this.gSBox = new int[1024];
        for (i2 = P_33; i2 < MAX_KEY_BITS; i2 += P_34) {
            int b3 = i2;
            int b2 = i2;
            int b1 = i2;
            int b0 = i2;
            switch (this.k64Cnt & 3) {
                case P_33 /*0*/:
                    b0 = (P[P_34][b0] & 255) ^ b0(k3);
                    b1 = (P[P_33][b1] & 255) ^ b1(k3);
                    b2 = (P[P_33][b2] & 255) ^ b2(k3);
                    b3 = (P[P_34][b3] & 255) ^ b3(k3);
                    break;
                case P_34 /*1*/:
                    this.gSBox[i2 * 2] = this.gMDS0[(P[P_33][b0] & 255) ^ b0(k0)];
                    this.gSBox[(i2 * 2) + P_34] = this.gMDS1[(P[P_33][b1] & 255) ^ b1(k0)];
                    this.gSBox[(i2 * 2) + 512] = this.gMDS2[(P[P_34][b2] & 255) ^ b2(k0)];
                    this.gSBox[(i2 * 2) + 513] = this.gMDS3[(P[P_34][b3] & 255) ^ b3(k0)];
                    continue;
                case F2m.TPB /*2*/:
                    break;
                case F2m.PPB /*3*/:
                    break;
                default:
                    break;
            }
            b0 = (P[P_34][b0] & 255) ^ b0(k2);
            b1 = (P[P_34][b1] & 255) ^ b1(k2);
            b2 = (P[P_33][b2] & 255) ^ b2(k2);
            b3 = (P[P_33][b3] & 255) ^ b3(k2);
            this.gSBox[i2 * 2] = this.gMDS0[(P[P_33][(P[P_33][b0] & 255) ^ b0(k1)] & 255) ^ b0(k0)];
            this.gSBox[(i2 * 2) + P_34] = this.gMDS1[(P[P_33][(P[P_34][b1] & 255) ^ b1(k1)] & 255) ^ b1(k0)];
            this.gSBox[(i2 * 2) + 512] = this.gMDS2[(P[P_34][(P[P_33][b2] & 255) ^ b2(k1)] & 255) ^ b2(k0)];
            this.gSBox[(i2 * 2) + 513] = this.gMDS3[(P[P_34][(P[P_34][b3] & 255) ^ b3(k1)] & 255) ^ b3(k0)];
            continue;
        }
    }

    private void encryptBlock(byte[] src, int srcIndex, byte[] dst, int dstIndex) {
        int x0 = BytesTo32Bits(src, srcIndex) ^ this.gSubKeys[P_33];
        int x1 = BytesTo32Bits(src, srcIndex + OUTPUT_WHITEN) ^ this.gSubKeys[P_34];
        int x2 = BytesTo32Bits(src, srcIndex + ROUND_SUBKEYS) ^ this.gSubKeys[2];
        int x3 = BytesTo32Bits(src, srcIndex + 12) ^ this.gSubKeys[3];
        int k = ROUND_SUBKEYS;
        for (int r = P_33; r < ROUNDS; r += 2) {
            int t0 = Fe32_0(x0);
            int t1 = Fe32_3(x1);
            int k2 = k + P_34;
            x2 ^= (t0 + t1) + this.gSubKeys[k];
            x2 = (x2 >>> P_34) | (x2 << 31);
            k = k2 + P_34;
            x3 = ((x3 << P_34) | (x3 >>> 31)) ^ (((t1 * 2) + t0) + this.gSubKeys[k2]);
            t0 = Fe32_0(x2);
            t1 = Fe32_3(x3);
            k2 = k + P_34;
            x0 ^= (t0 + t1) + this.gSubKeys[k];
            x0 = (x0 >>> P_34) | (x0 << 31);
            k = k2 + P_34;
            x1 = ((x1 << P_34) | (x1 >>> 31)) ^ (((t1 * 2) + t0) + this.gSubKeys[k2]);
        }
        Bits32ToBytes(this.gSubKeys[OUTPUT_WHITEN] ^ x2, dst, dstIndex);
        Bits32ToBytes(this.gSubKeys[5] ^ x3, dst, dstIndex + OUTPUT_WHITEN);
        Bits32ToBytes(this.gSubKeys[6] ^ x0, dst, dstIndex + ROUND_SUBKEYS);
        Bits32ToBytes(this.gSubKeys[7] ^ x1, dst, dstIndex + 12);
    }

    private void decryptBlock(byte[] src, int srcIndex, byte[] dst, int dstIndex) {
        int x2 = BytesTo32Bits(src, srcIndex) ^ this.gSubKeys[OUTPUT_WHITEN];
        int x3 = BytesTo32Bits(src, srcIndex + OUTPUT_WHITEN) ^ this.gSubKeys[5];
        int x0 = BytesTo32Bits(src, srcIndex + ROUND_SUBKEYS) ^ this.gSubKeys[6];
        int x1 = BytesTo32Bits(src, srcIndex + 12) ^ this.gSubKeys[7];
        int k = 39;
        for (int r = P_33; r < ROUNDS; r += 2) {
            int t0 = Fe32_0(x2);
            int t1 = Fe32_3(x3);
            int k2 = k - 1;
            x1 ^= ((t1 * 2) + t0) + this.gSubKeys[k];
            k = k2 - 1;
            x0 = ((x0 << P_34) | (x0 >>> 31)) ^ ((t0 + t1) + this.gSubKeys[k2]);
            x1 = (x1 >>> P_34) | (x1 << 31);
            t0 = Fe32_0(x0);
            t1 = Fe32_3(x1);
            k2 = k - 1;
            x3 ^= ((t1 * 2) + t0) + this.gSubKeys[k];
            k = k2 - 1;
            x2 = ((x2 << P_34) | (x2 >>> 31)) ^ ((t0 + t1) + this.gSubKeys[k2]);
            x3 = (x3 >>> P_34) | (x3 << 31);
        }
        Bits32ToBytes(this.gSubKeys[P_33] ^ x0, dst, dstIndex);
        Bits32ToBytes(this.gSubKeys[P_34] ^ x1, dst, dstIndex + OUTPUT_WHITEN);
        Bits32ToBytes(this.gSubKeys[2] ^ x2, dst, dstIndex + ROUND_SUBKEYS);
        Bits32ToBytes(this.gSubKeys[3] ^ x3, dst, dstIndex + 12);
    }

    private int F32(int x, int[] k32) {
        int b0 = b0(x);
        int b1 = b1(x);
        int b2 = b2(x);
        int b3 = b3(x);
        int k0 = k32[P_33];
        int k1 = k32[P_34];
        int k2 = k32[2];
        int k3 = k32[3];
        switch (this.k64Cnt & 3) {
            case P_33 /*0*/:
                b0 = (P[P_34][b0] & 255) ^ b0(k3);
                b1 = (P[P_33][b1] & 255) ^ b1(k3);
                b2 = (P[P_33][b2] & 255) ^ b2(k3);
                b3 = (P[P_34][b3] & 255) ^ b3(k3);
                break;
            case P_34 /*1*/:
                return ((this.gMDS0[(P[P_33][b0] & 255) ^ b0(k0)] ^ this.gMDS1[(P[P_33][b1] & 255) ^ b1(k0)]) ^ this.gMDS2[(P[P_34][b2] & 255) ^ b2(k0)]) ^ this.gMDS3[(P[P_34][b3] & 255) ^ b3(k0)];
            case F2m.TPB /*2*/:
                break;
            case F2m.PPB /*3*/:
                break;
            default:
                return P_33;
        }
        b0 = (P[P_34][b0] & 255) ^ b0(k2);
        b1 = (P[P_34][b1] & 255) ^ b1(k2);
        b2 = (P[P_33][b2] & 255) ^ b2(k2);
        b3 = (P[P_33][b3] & 255) ^ b3(k2);
        return ((this.gMDS0[(P[P_33][(P[P_33][b0] & 255) ^ b0(k1)] & 255) ^ b0(k0)] ^ this.gMDS1[(P[P_33][(P[P_34][b1] & 255) ^ b1(k1)] & 255) ^ b1(k0)]) ^ this.gMDS2[(P[P_34][(P[P_33][b2] & 255) ^ b2(k1)] & 255) ^ b2(k0)]) ^ this.gMDS3[(P[P_34][(P[P_34][b3] & 255) ^ b3(k1)] & 255) ^ b3(k0)];
    }

    private int RS_MDS_Encode(int k0, int k1) {
        int i;
        int r = k1;
        for (i = P_33; i < OUTPUT_WHITEN; i += P_34) {
            r = RS_rem(r);
        }
        r ^= k0;
        for (i = P_33; i < OUTPUT_WHITEN; i += P_34) {
            r = RS_rem(r);
        }
        return r;
    }

    private int RS_rem(int x) {
        int i;
        int i2 = P_33;
        int b = (x >>> 24) & 255;
        int i3 = b << P_34;
        if ((b & ReasonFlags.unused) != 0) {
            i = RS_GF_FDBK;
        } else {
            i = P_33;
        }
        int g2 = (i ^ i3) & 255;
        i = b >>> P_34;
        if ((b & P_34) != 0) {
            i2 = 166;
        }
        int g3 = (i2 ^ i) ^ g2;
        return ((((x << ROUND_SUBKEYS) ^ (g3 << 24)) ^ (g2 << ROUNDS)) ^ (g3 << ROUND_SUBKEYS)) ^ b;
    }

    private int LFSR1(int x) {
        int i = P_33;
        int i2 = x >> P_34;
        if ((x & P_34) != 0) {
            i = GF256_FDBK_2;
        }
        return i ^ i2;
    }

    private int LFSR2(int x) {
        int i;
        int i2 = P_33;
        int i3 = x >> 2;
        if ((x & 2) != 0) {
            i = GF256_FDBK_2;
        } else {
            i = P_33;
        }
        i ^= i3;
        if ((x & P_34) != 0) {
            i2 = GF256_FDBK_4;
        }
        return i2 ^ i;
    }

    private int Mx_X(int x) {
        return LFSR2(x) ^ x;
    }

    private int Mx_Y(int x) {
        return (LFSR1(x) ^ x) ^ LFSR2(x);
    }

    private int b0(int x) {
        return x & 255;
    }

    private int b1(int x) {
        return (x >>> ROUND_SUBKEYS) & 255;
    }

    private int b2(int x) {
        return (x >>> ROUNDS) & 255;
    }

    private int b3(int x) {
        return (x >>> 24) & 255;
    }

    private int Fe32_0(int x) {
        return ((this.gSBox[((x & 255) * 2) + P_33] ^ this.gSBox[(((x >>> ROUND_SUBKEYS) & 255) * 2) + P_34]) ^ this.gSBox[(((x >>> ROUNDS) & 255) * 2) + 512]) ^ this.gSBox[(((x >>> 24) & 255) * 2) + 513];
    }

    private int Fe32_3(int x) {
        return ((this.gSBox[(((x >>> 24) & 255) * 2) + P_33] ^ this.gSBox[((x & 255) * 2) + P_34]) ^ this.gSBox[(((x >>> ROUND_SUBKEYS) & 255) * 2) + 512]) ^ this.gSBox[(((x >>> ROUNDS) & 255) * 2) + 513];
    }

    private int BytesTo32Bits(byte[] b, int p) {
        return (((b[p] & 255) | ((b[p + P_34] & 255) << ROUND_SUBKEYS)) | ((b[p + 2] & 255) << ROUNDS)) | ((b[p + 3] & 255) << 24);
    }

    private void Bits32ToBytes(int in, byte[] b, int offset) {
        b[offset] = (byte) in;
        b[offset + P_34] = (byte) (in >> ROUND_SUBKEYS);
        b[offset + 2] = (byte) (in >> ROUNDS);
        b[offset + 3] = (byte) (in >> 24);
    }
}
