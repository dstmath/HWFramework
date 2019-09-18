package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.params.KeyParameter;

public final class TwofishEngine implements BlockCipher {
    private static final int BLOCK_SIZE = 16;
    private static final int GF256_FDBK = 361;
    private static final int GF256_FDBK_2 = 180;
    private static final int GF256_FDBK_4 = 90;
    private static final int INPUT_WHITEN = 0;
    private static final int MAX_KEY_BITS = 256;
    private static final int MAX_ROUNDS = 16;
    private static final int OUTPUT_WHITEN = 4;
    private static final byte[][] P;
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
    private boolean encrypting = false;
    private int[] gMDS0 = new int[MAX_KEY_BITS];
    private int[] gMDS1 = new int[MAX_KEY_BITS];
    private int[] gMDS2 = new int[MAX_KEY_BITS];
    private int[] gMDS3 = new int[MAX_KEY_BITS];
    private int[] gSBox;
    private int[] gSubKeys;
    private int k64Cnt = 0;
    private byte[] workingKey = null;

    static {
        byte[] bArr = new byte[MAX_KEY_BITS];
        // fill-array-data instruction
        bArr[0] = -87;
        bArr[1] = 103;
        bArr[2] = -77;
        bArr[3] = -24;
        bArr[4] = 4;
        bArr[5] = -3;
        bArr[6] = -93;
        bArr[7] = 118;
        bArr[8] = -102;
        bArr[9] = -110;
        bArr[10] = -128;
        bArr[11] = 120;
        bArr[12] = -28;
        bArr[13] = -35;
        bArr[14] = -47;
        bArr[15] = 56;
        bArr[16] = 13;
        bArr[17] = -58;
        bArr[18] = 53;
        bArr[19] = -104;
        bArr[20] = 24;
        bArr[21] = -9;
        bArr[22] = -20;
        bArr[23] = 108;
        bArr[24] = 67;
        bArr[25] = 117;
        bArr[26] = 55;
        bArr[27] = 38;
        bArr[28] = -6;
        bArr[29] = 19;
        bArr[30] = -108;
        bArr[31] = 72;
        bArr[32] = -14;
        bArr[33] = -48;
        bArr[34] = -117;
        bArr[35] = 48;
        bArr[36] = -124;
        bArr[37] = 84;
        bArr[38] = -33;
        bArr[39] = 35;
        bArr[40] = 25;
        bArr[41] = 91;
        bArr[42] = 61;
        bArr[43] = 89;
        bArr[44] = -13;
        bArr[45] = -82;
        bArr[46] = -94;
        bArr[47] = -126;
        bArr[48] = 99;
        bArr[49] = 1;
        bArr[50] = -125;
        bArr[51] = 46;
        bArr[52] = -39;
        bArr[53] = 81;
        bArr[54] = -101;
        bArr[55] = 124;
        bArr[56] = -90;
        bArr[57] = -21;
        bArr[58] = -91;
        bArr[59] = -66;
        bArr[60] = 22;
        bArr[61] = 12;
        bArr[62] = -29;
        bArr[63] = 97;
        bArr[64] = -64;
        bArr[65] = -116;
        bArr[66] = 58;
        bArr[67] = -11;
        bArr[68] = 115;
        bArr[69] = 44;
        bArr[70] = 37;
        bArr[71] = 11;
        bArr[72] = -69;
        bArr[73] = 78;
        bArr[74] = -119;
        bArr[75] = 107;
        bArr[76] = 83;
        bArr[77] = 106;
        bArr[78] = -76;
        bArr[79] = -15;
        bArr[80] = -31;
        bArr[81] = -26;
        bArr[82] = -67;
        bArr[83] = 69;
        bArr[84] = -30;
        bArr[85] = -12;
        bArr[86] = -74;
        bArr[87] = 102;
        bArr[88] = -52;
        bArr[89] = -107;
        bArr[90] = 3;
        bArr[91] = 86;
        bArr[92] = -44;
        bArr[93] = 28;
        bArr[94] = 30;
        bArr[95] = -41;
        bArr[96] = -5;
        bArr[97] = -61;
        bArr[98] = -114;
        bArr[99] = -75;
        bArr[100] = -23;
        bArr[101] = -49;
        bArr[102] = -65;
        bArr[103] = -70;
        bArr[104] = -22;
        bArr[105] = 119;
        bArr[106] = 57;
        bArr[107] = -81;
        bArr[108] = 51;
        bArr[109] = -55;
        bArr[110] = 98;
        bArr[111] = 113;
        bArr[112] = -127;
        bArr[113] = 121;
        bArr[114] = 9;
        bArr[115] = -83;
        bArr[116] = 36;
        bArr[117] = -51;
        bArr[118] = -7;
        bArr[119] = -40;
        bArr[120] = -27;
        bArr[121] = -59;
        bArr[122] = -71;
        bArr[123] = 77;
        bArr[124] = 68;
        bArr[125] = 8;
        bArr[126] = -122;
        bArr[127] = -25;
        bArr[128] = -95;
        bArr[129] = 29;
        bArr[130] = -86;
        bArr[131] = -19;
        bArr[132] = 6;
        bArr[133] = 112;
        bArr[134] = -78;
        bArr[135] = -46;
        bArr[136] = 65;
        bArr[137] = 123;
        bArr[138] = -96;
        bArr[139] = 17;
        bArr[140] = 49;
        bArr[141] = -62;
        bArr[142] = 39;
        bArr[143] = -112;
        bArr[144] = 32;
        bArr[145] = -10;
        bArr[146] = 96;
        bArr[147] = -1;
        bArr[148] = -106;
        bArr[149] = 92;
        bArr[150] = -79;
        bArr[151] = -85;
        bArr[152] = -98;
        bArr[153] = -100;
        bArr[154] = 82;
        bArr[155] = 27;
        bArr[156] = 95;
        bArr[157] = -109;
        bArr[158] = 10;
        bArr[159] = -17;
        bArr[160] = -111;
        bArr[161] = -123;
        bArr[162] = 73;
        bArr[163] = -18;
        bArr[164] = 45;
        bArr[165] = 79;
        bArr[166] = -113;
        bArr[167] = 59;
        bArr[168] = 71;
        bArr[169] = -121;
        bArr[170] = 109;
        bArr[171] = 70;
        bArr[172] = -42;
        bArr[173] = 62;
        bArr[174] = 105;
        bArr[175] = 100;
        bArr[176] = 42;
        bArr[177] = -50;
        bArr[178] = -53;
        bArr[179] = 47;
        bArr[180] = -4;
        bArr[181] = -105;
        bArr[182] = 5;
        bArr[183] = 122;
        bArr[184] = -84;
        bArr[185] = 127;
        bArr[186] = -43;
        bArr[187] = 26;
        bArr[188] = 75;
        bArr[189] = 14;
        bArr[190] = -89;
        bArr[191] = 90;
        bArr[192] = 40;
        bArr[193] = 20;
        bArr[194] = 63;
        bArr[195] = 41;
        bArr[196] = -120;
        bArr[197] = 60;
        bArr[198] = 76;
        bArr[199] = 2;
        bArr[200] = -72;
        bArr[201] = -38;
        bArr[202] = -80;
        bArr[203] = 23;
        bArr[204] = 85;
        bArr[205] = 31;
        bArr[206] = -118;
        bArr[207] = 125;
        bArr[208] = 87;
        bArr[209] = -57;
        bArr[210] = -115;
        bArr[211] = 116;
        bArr[212] = -73;
        bArr[213] = -60;
        bArr[214] = -97;
        bArr[215] = 114;
        bArr[216] = 126;
        bArr[217] = 21;
        bArr[218] = 34;
        bArr[219] = 18;
        bArr[220] = 88;
        bArr[221] = 7;
        bArr[222] = -103;
        bArr[223] = 52;
        bArr[224] = 110;
        bArr[225] = 80;
        bArr[226] = -34;
        bArr[227] = 104;
        bArr[228] = 101;
        bArr[229] = -68;
        bArr[230] = -37;
        bArr[231] = -8;
        bArr[232] = -56;
        bArr[233] = -88;
        bArr[234] = 43;
        bArr[235] = 64;
        bArr[236] = -36;
        bArr[237] = -2;
        bArr[238] = 50;
        bArr[239] = -92;
        bArr[240] = -54;
        bArr[241] = 16;
        bArr[242] = 33;
        bArr[243] = -16;
        bArr[244] = -45;
        bArr[245] = 93;
        bArr[246] = 15;
        bArr[247] = 0;
        bArr[248] = 111;
        bArr[249] = -99;
        bArr[250] = 54;
        bArr[251] = 66;
        bArr[252] = 74;
        bArr[253] = 94;
        bArr[254] = -63;
        bArr[255] = -32;
        byte[] bArr2 = new byte[MAX_KEY_BITS];
        // fill-array-data instruction
        bArr2[0] = 117;
        bArr2[1] = -13;
        bArr2[2] = -58;
        bArr2[3] = -12;
        bArr2[4] = -37;
        bArr2[5] = 123;
        bArr2[6] = -5;
        bArr2[7] = -56;
        bArr2[8] = 74;
        bArr2[9] = -45;
        bArr2[10] = -26;
        bArr2[11] = 107;
        bArr2[12] = 69;
        bArr2[13] = 125;
        bArr2[14] = -24;
        bArr2[15] = 75;
        bArr2[16] = -42;
        bArr2[17] = 50;
        bArr2[18] = -40;
        bArr2[19] = -3;
        bArr2[20] = 55;
        bArr2[21] = 113;
        bArr2[22] = -15;
        bArr2[23] = -31;
        bArr2[24] = 48;
        bArr2[25] = 15;
        bArr2[26] = -8;
        bArr2[27] = 27;
        bArr2[28] = -121;
        bArr2[29] = -6;
        bArr2[30] = 6;
        bArr2[31] = 63;
        bArr2[32] = 94;
        bArr2[33] = -70;
        bArr2[34] = -82;
        bArr2[35] = 91;
        bArr2[36] = -118;
        bArr2[37] = 0;
        bArr2[38] = -68;
        bArr2[39] = -99;
        bArr2[40] = 109;
        bArr2[41] = -63;
        bArr2[42] = -79;
        bArr2[43] = 14;
        bArr2[44] = -128;
        bArr2[45] = 93;
        bArr2[46] = -46;
        bArr2[47] = -43;
        bArr2[48] = -96;
        bArr2[49] = -124;
        bArr2[50] = 7;
        bArr2[51] = 20;
        bArr2[52] = -75;
        bArr2[53] = -112;
        bArr2[54] = 44;
        bArr2[55] = -93;
        bArr2[56] = -78;
        bArr2[57] = 115;
        bArr2[58] = 76;
        bArr2[59] = 84;
        bArr2[60] = -110;
        bArr2[61] = 116;
        bArr2[62] = 54;
        bArr2[63] = 81;
        bArr2[64] = 56;
        bArr2[65] = -80;
        bArr2[66] = -67;
        bArr2[67] = 90;
        bArr2[68] = -4;
        bArr2[69] = 96;
        bArr2[70] = 98;
        bArr2[71] = -106;
        bArr2[72] = 108;
        bArr2[73] = 66;
        bArr2[74] = -9;
        bArr2[75] = 16;
        bArr2[76] = 124;
        bArr2[77] = 40;
        bArr2[78] = 39;
        bArr2[79] = -116;
        bArr2[80] = 19;
        bArr2[81] = -107;
        bArr2[82] = -100;
        bArr2[83] = -57;
        bArr2[84] = 36;
        bArr2[85] = 70;
        bArr2[86] = 59;
        bArr2[87] = 112;
        bArr2[88] = -54;
        bArr2[89] = -29;
        bArr2[90] = -123;
        bArr2[91] = -53;
        bArr2[92] = 17;
        bArr2[93] = -48;
        bArr2[94] = -109;
        bArr2[95] = -72;
        bArr2[96] = -90;
        bArr2[97] = -125;
        bArr2[98] = 32;
        bArr2[99] = -1;
        bArr2[100] = -97;
        bArr2[101] = 119;
        bArr2[102] = -61;
        bArr2[103] = -52;
        bArr2[104] = 3;
        bArr2[105] = 111;
        bArr2[106] = 8;
        bArr2[107] = -65;
        bArr2[108] = 64;
        bArr2[109] = -25;
        bArr2[110] = 43;
        bArr2[111] = -30;
        bArr2[112] = 121;
        bArr2[113] = 12;
        bArr2[114] = -86;
        bArr2[115] = -126;
        bArr2[116] = 65;
        bArr2[117] = 58;
        bArr2[118] = -22;
        bArr2[119] = -71;
        bArr2[120] = -28;
        bArr2[121] = -102;
        bArr2[122] = -92;
        bArr2[123] = -105;
        bArr2[124] = 126;
        bArr2[125] = -38;
        bArr2[126] = 122;
        bArr2[127] = 23;
        bArr2[128] = 102;
        bArr2[129] = -108;
        bArr2[130] = -95;
        bArr2[131] = 29;
        bArr2[132] = 61;
        bArr2[133] = -16;
        bArr2[134] = -34;
        bArr2[135] = -77;
        bArr2[136] = 11;
        bArr2[137] = 114;
        bArr2[138] = -89;
        bArr2[139] = 28;
        bArr2[140] = -17;
        bArr2[141] = -47;
        bArr2[142] = 83;
        bArr2[143] = 62;
        bArr2[144] = -113;
        bArr2[145] = 51;
        bArr2[146] = 38;
        bArr2[147] = 95;
        bArr2[148] = -20;
        bArr2[149] = 118;
        bArr2[150] = 42;
        bArr2[151] = 73;
        bArr2[152] = -127;
        bArr2[153] = -120;
        bArr2[154] = -18;
        bArr2[155] = 33;
        bArr2[156] = -60;
        bArr2[157] = 26;
        bArr2[158] = -21;
        bArr2[159] = -39;
        bArr2[160] = -59;
        bArr2[161] = 57;
        bArr2[162] = -103;
        bArr2[163] = -51;
        bArr2[164] = -83;
        bArr2[165] = 49;
        bArr2[166] = -117;
        bArr2[167] = 1;
        bArr2[168] = 24;
        bArr2[169] = 35;
        bArr2[170] = -35;
        bArr2[171] = 31;
        bArr2[172] = 78;
        bArr2[173] = 45;
        bArr2[174] = -7;
        bArr2[175] = 72;
        bArr2[176] = 79;
        bArr2[177] = -14;
        bArr2[178] = 101;
        bArr2[179] = -114;
        bArr2[180] = 120;
        bArr2[181] = 92;
        bArr2[182] = 88;
        bArr2[183] = 25;
        bArr2[184] = -115;
        bArr2[185] = -27;
        bArr2[186] = -104;
        bArr2[187] = 87;
        bArr2[188] = 103;
        bArr2[189] = 127;
        bArr2[190] = 5;
        bArr2[191] = 100;
        bArr2[192] = -81;
        bArr2[193] = 99;
        bArr2[194] = -74;
        bArr2[195] = -2;
        bArr2[196] = -11;
        bArr2[197] = -73;
        bArr2[198] = 60;
        bArr2[199] = -91;
        bArr2[200] = -50;
        bArr2[201] = -23;
        bArr2[202] = 104;
        bArr2[203] = 68;
        bArr2[204] = -32;
        bArr2[205] = 77;
        bArr2[206] = 67;
        bArr2[207] = 105;
        bArr2[208] = 41;
        bArr2[209] = 46;
        bArr2[210] = -84;
        bArr2[211] = 21;
        bArr2[212] = 89;
        bArr2[213] = -88;
        bArr2[214] = 10;
        bArr2[215] = -98;
        bArr2[216] = 110;
        bArr2[217] = 71;
        bArr2[218] = -33;
        bArr2[219] = 52;
        bArr2[220] = 53;
        bArr2[221] = 106;
        bArr2[222] = -49;
        bArr2[223] = -36;
        bArr2[224] = 34;
        bArr2[225] = -55;
        bArr2[226] = -64;
        bArr2[227] = -101;
        bArr2[228] = -119;
        bArr2[229] = -44;
        bArr2[230] = -19;
        bArr2[231] = -85;
        bArr2[232] = 18;
        bArr2[233] = -94;
        bArr2[234] = 13;
        bArr2[235] = 82;
        bArr2[236] = -69;
        bArr2[237] = 2;
        bArr2[238] = 47;
        bArr2[239] = -87;
        bArr2[240] = -41;
        bArr2[241] = 97;
        bArr2[242] = 30;
        bArr2[243] = -76;
        bArr2[244] = 80;
        bArr2[245] = 4;
        bArr2[246] = -10;
        bArr2[247] = -62;
        bArr2[248] = 22;
        bArr2[249] = 37;
        bArr2[250] = -122;
        bArr2[251] = 86;
        bArr2[252] = 85;
        bArr2[253] = 9;
        bArr2[254] = -66;
        bArr2[255] = -111;
        P = new byte[][]{bArr, bArr2};
    }

    public TwofishEngine() {
        int[] m1 = new int[2];
        int[] mX = new int[2];
        int[] mY = new int[2];
        for (int i = 0; i < MAX_KEY_BITS; i++) {
            int j = P[0][i] & 255;
            m1[0] = j;
            mX[0] = Mx_X(j) & 255;
            mY[0] = Mx_Y(j) & 255;
            int j2 = P[1][i] & 255;
            m1[1] = j2;
            mX[1] = Mx_X(j2) & 255;
            mY[1] = Mx_Y(j2) & 255;
            this.gMDS0[i] = m1[1] | (mX[1] << 8) | (mY[1] << 16) | (mY[1] << 24);
            this.gMDS1[i] = mY[0] | (mY[0] << 8) | (mX[0] << 16) | (m1[0] << 24);
            this.gMDS2[i] = (mY[1] << 24) | mX[1] | (mY[1] << 8) | (m1[1] << 16);
            this.gMDS3[i] = mX[0] | (m1[0] << 8) | (mY[0] << 16) | (mX[0] << 24);
        }
    }

    public void init(boolean encrypting2, CipherParameters params) {
        if (params instanceof KeyParameter) {
            this.encrypting = encrypting2;
            this.workingKey = ((KeyParameter) params).getKey();
            this.k64Cnt = this.workingKey.length / 8;
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
        } else if (inOff + 16 > in.length) {
            throw new DataLengthException("input buffer too short");
        } else if (outOff + 16 <= out.length) {
            if (this.encrypting) {
                encryptBlock(in, inOff, out, outOff);
            } else {
                decryptBlock(in, inOff, out, outOff);
            }
            return 16;
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    public void reset() {
        if (this.workingKey != null) {
            setKey(this.workingKey);
        }
    }

    public int getBlockSize() {
        return 16;
    }

    private void setKey(byte[] key) {
        int[] k32e;
        char c;
        char c2;
        byte[] bArr = key;
        int[] k32e2 = new int[4];
        int[] k32o = new int[4];
        int[] sBoxKeys = new int[4];
        this.gSubKeys = new int[TOTAL_SUBKEYS];
        if (this.k64Cnt < 1) {
            throw new IllegalArgumentException("Key size less than 64 bits");
        } else if (this.k64Cnt <= 4) {
            char c3 = 0;
            for (int i = 0; i < this.k64Cnt; i++) {
                int p = i * 8;
                k32e2[i] = BytesTo32Bits(bArr, p);
                k32o[i] = BytesTo32Bits(bArr, p + 4);
                sBoxKeys[(this.k64Cnt - 1) - i] = RS_MDS_Encode(k32e2[i], k32o[i]);
            }
            for (int i2 = 0; i2 < 20; i2++) {
                int q = SK_STEP * i2;
                int A = F32(q, k32e2);
                int B = F32(SK_BUMP + q, k32o);
                int B2 = (B << 8) | (B >>> 24);
                int A2 = A + B2;
                this.gSubKeys[i2 * 2] = A2;
                int A3 = A2 + B2;
                this.gSubKeys[(i2 * 2) + 1] = (A3 << 9) | (A3 >>> 23);
            }
            int k0 = sBoxKeys[0];
            int k1 = sBoxKeys[1];
            int k2 = sBoxKeys[2];
            int i3 = 3;
            int k3 = sBoxKeys[3];
            this.gSBox = new int[1024];
            int i4 = 0;
            while (i4 < MAX_KEY_BITS) {
                int b3 = i4;
                int b2 = i4;
                int b1 = i4;
                int b0 = i4;
                switch (this.k64Cnt & i3) {
                    case 0:
                        b0 = (P[1][b0] & 255) ^ b0(k3);
                        b1 = (P[0][b1] & 255) ^ b1(k3);
                        b2 = (P[0][b2] & 255) ^ b2(k3);
                        c2 = 1;
                        b3 = (P[1][b3] & 255) ^ b3(k3);
                        break;
                    case 1:
                        this.gSBox[i4 * 2] = this.gMDS0[(P[c3][b0] & 255) ^ b0(k0)];
                        this.gSBox[(i4 * 2) + 1] = this.gMDS1[(P[0][b1] & 255) ^ b1(k0)];
                        this.gSBox[(i4 * 2) + 512] = this.gMDS2[(P[1][b2] & 255) ^ b2(k0)];
                        this.gSBox[(i4 * 2) + 513] = this.gMDS3[(P[1][b3] & 255) ^ b3(k0)];
                        k32e = k32e2;
                        c = 0;
                        continue;
                    case 2:
                        k32e = k32e2;
                        this.gSBox[i4 * 2] = this.gMDS0[(P[0][(P[0][b0] & 255) ^ b0(k1)] & 255) ^ b0(k0)];
                        this.gSBox[(i4 * 2) + 1] = this.gMDS1[(P[0][(P[1][b1] & 255) ^ b1(k1)] & 255) ^ b1(k0)];
                        c = 0;
                        this.gSBox[(i4 * 2) + 512] = this.gMDS2[(P[1][(P[0][b2] & 255) ^ b2(k1)] & 255) ^ b2(k0)];
                        this.gSBox[(i4 * 2) + 513] = this.gMDS3[(P[1][(P[1][b3] & 255) ^ b3(k1)] & 255) ^ b3(k0)];
                        continue;
                    case 3:
                        c2 = 1;
                        break;
                    default:
                        c = c3;
                        k32e = k32e2;
                        continue;
                }
                int b02 = (P[c2][b0] & 255) ^ b0(k2);
                int b12 = (P[c2][b1] & 255) ^ b1(k2);
                b0 = b02;
                b1 = b12;
                b2 = (P[0][b2] & 255) ^ b2(k2);
                b3 = (P[0][b3] & 255) ^ b3(k2);
                k32e = k32e2;
                this.gSBox[i4 * 2] = this.gMDS0[(P[0][(P[0][b0] & 255) ^ b0(k1)] & 255) ^ b0(k0)];
                this.gSBox[(i4 * 2) + 1] = this.gMDS1[(P[0][(P[1][b1] & 255) ^ b1(k1)] & 255) ^ b1(k0)];
                c = 0;
                this.gSBox[(i4 * 2) + 512] = this.gMDS2[(P[1][(P[0][b2] & 255) ^ b2(k1)] & 255) ^ b2(k0)];
                this.gSBox[(i4 * 2) + 513] = this.gMDS3[(P[1][(P[1][b3] & 255) ^ b3(k1)] & 255) ^ b3(k0)];
                continue;
                i4++;
                c3 = c;
                k32e2 = k32e;
                byte[] bArr2 = key;
                i3 = 3;
            }
        } else {
            throw new IllegalArgumentException("Key size larger than 256 bits");
        }
    }

    private void encryptBlock(byte[] src, int srcIndex, byte[] dst, int dstIndex) {
        byte[] bArr = src;
        byte[] bArr2 = dst;
        int i = dstIndex;
        int r = 0;
        int x0 = BytesTo32Bits(src, srcIndex) ^ this.gSubKeys[0];
        int x1 = BytesTo32Bits(bArr, srcIndex + 4) ^ this.gSubKeys[1];
        int i2 = 2;
        int x2 = BytesTo32Bits(bArr, srcIndex + 8) ^ this.gSubKeys[2];
        int x3 = BytesTo32Bits(bArr, srcIndex + 12) ^ this.gSubKeys[3];
        int t0 = 8;
        while (r < 16) {
            int t02 = Fe32_0(x0);
            int t1 = Fe32_3(x1);
            int k = t0 + 1;
            int x22 = x2 ^ ((t02 + t1) + this.gSubKeys[t0]);
            x2 = (x22 >>> 1) | (x22 << 31);
            int k2 = k + 1;
            x3 = ((x3 << 1) | (x3 >>> 31)) ^ (((i2 * t1) + t02) + this.gSubKeys[k]);
            int t03 = Fe32_0(x2);
            int t12 = Fe32_3(x3);
            int k3 = k2 + 1;
            int x02 = x0 ^ ((t03 + t12) + this.gSubKeys[k2]);
            x0 = (x02 >>> 1) | (x02 << 31);
            x1 = ((x1 << 1) | (x1 >>> 31)) ^ (((i2 * t12) + t03) + this.gSubKeys[k3]);
            r += 2;
            t0 = k3 + 1;
            i2 = 2;
        }
        Bits32ToBytes(this.gSubKeys[4] ^ x2, bArr2, i);
        Bits32ToBytes(this.gSubKeys[5] ^ x3, bArr2, i + 4);
        Bits32ToBytes(this.gSubKeys[6] ^ x0, bArr2, i + 8);
        Bits32ToBytes(this.gSubKeys[7] ^ x1, bArr2, i + 12);
    }

    private void decryptBlock(byte[] src, int srcIndex, byte[] dst, int dstIndex) {
        byte[] bArr = src;
        byte[] bArr2 = dst;
        int i = dstIndex;
        int x2 = BytesTo32Bits(src, srcIndex) ^ this.gSubKeys[4];
        int x3 = BytesTo32Bits(bArr, srcIndex + 4) ^ this.gSubKeys[5];
        int x0 = BytesTo32Bits(bArr, srcIndex + 8) ^ this.gSubKeys[6];
        int x1 = BytesTo32Bits(bArr, srcIndex + 12) ^ this.gSubKeys[7];
        int t0 = 39;
        int x02 = x0;
        int x32 = x3;
        int x22 = x2;
        int r = 0;
        while (r < 16) {
            int t02 = Fe32_0(x22);
            int t1 = Fe32_3(x32);
            int k = t0 - 1;
            int x12 = x1 ^ (((2 * t1) + t02) + this.gSubKeys[t0]);
            int k2 = k - 1;
            x02 = ((x02 << 1) | (x02 >>> 31)) ^ ((t02 + t1) + this.gSubKeys[k]);
            x1 = (x12 >>> 1) | (x12 << 31);
            int t03 = Fe32_0(x02);
            int t12 = Fe32_3(x1);
            int k3 = k2 - 1;
            int x33 = x32 ^ (((2 * t12) + t03) + this.gSubKeys[k2]);
            x22 = ((x22 << 1) | (x22 >>> 31)) ^ ((t03 + t12) + this.gSubKeys[k3]);
            x32 = (x33 >>> 1) | (x33 << 31);
            r += 2;
            t0 = k3 - 1;
        }
        Bits32ToBytes(this.gSubKeys[0] ^ x02, bArr2, i);
        Bits32ToBytes(this.gSubKeys[1] ^ x1, bArr2, i + 4);
        Bits32ToBytes(this.gSubKeys[2] ^ x22, bArr2, i + 8);
        Bits32ToBytes(this.gSubKeys[3] ^ x32, bArr2, i + 12);
    }

    private int F32(int x, int[] k32) {
        int b0 = b0(x);
        int b1 = b1(x);
        int b2 = b2(x);
        int b3 = b3(x);
        int k0 = k32[0];
        int k1 = k32[1];
        int k2 = k32[2];
        int k3 = k32[3];
        switch (3 & this.k64Cnt) {
            case 0:
                b0 = (P[1][b0] & 255) ^ b0(k3);
                b1 = (P[0][b1] & 255) ^ b1(k3);
                b2 = (P[0][b2] & 255) ^ b2(k3);
                b3 = (P[1][b3] & 255) ^ b3(k3);
                break;
            case 1:
                return ((this.gMDS1[(P[0][b1] & 255) ^ b1(k0)] ^ this.gMDS0[(P[0][b0] & 255) ^ b0(k0)]) ^ this.gMDS2[(P[1][b2] & 255) ^ b2(k0)]) ^ this.gMDS3[(P[1][b3] & 255) ^ b3(k0)];
            case 2:
                break;
            case 3:
                break;
            default:
                return 0;
        }
        b0 = (P[1][b0] & 255) ^ b0(k2);
        b1 = (P[1][b1] & 255) ^ b1(k2);
        b2 = (P[0][b2] & 255) ^ b2(k2);
        b3 = (P[0][b3] & 255) ^ b3(k2);
        return ((this.gMDS1[(P[0][(P[1][b1] & 255) ^ b1(k1)] & 255) ^ b1(k0)] ^ this.gMDS0[(P[0][(P[0][b0] & 255) ^ b0(k1)] & 255) ^ b0(k0)]) ^ this.gMDS2[(P[1][(P[0][b2] & 255) ^ b2(k1)] & 255) ^ b2(k0)]) ^ this.gMDS3[(P[1][(P[1][b3] & 255) ^ b3(k1)] & 255) ^ b3(k0)];
    }

    private int RS_MDS_Encode(int k0, int k1) {
        int r = k1;
        for (int i = 0; i < 4; i++) {
            r = RS_rem(r);
        }
        int r2 = r ^ k0;
        for (int i2 = 0; i2 < 4; i2++) {
            r2 = RS_rem(r2);
        }
        return r2;
    }

    private int RS_rem(int x) {
        int b = (x >>> 24) & 255;
        int i = 0;
        int g2 = ((b << 1) ^ ((b & 128) != 0 ? RS_GF_FDBK : 0)) & 255;
        int i2 = b >>> 1;
        if ((b & 1) != 0) {
            i = 166;
        }
        int g3 = (i2 ^ i) ^ g2;
        return ((((x << 8) ^ (g3 << 24)) ^ (g2 << 16)) ^ (g3 << 8)) ^ b;
    }

    private int LFSR1(int x) {
        return (x >> 1) ^ ((x & 1) != 0 ? GF256_FDBK_2 : 0);
    }

    private int LFSR2(int x) {
        int i = 0;
        int i2 = (x >> 2) ^ ((x & 2) != 0 ? GF256_FDBK_2 : 0);
        if ((x & 1) != 0) {
            i = GF256_FDBK_4;
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
        return (x >>> 8) & 255;
    }

    private int b2(int x) {
        return (x >>> 16) & 255;
    }

    private int b3(int x) {
        return (x >>> 24) & 255;
    }

    private int Fe32_0(int x) {
        return ((this.gSBox[0 + ((x & 255) * 2)] ^ this.gSBox[1 + (((x >>> 8) & 255) * 2)]) ^ this.gSBox[512 + (((x >>> 16) & 255) * 2)]) ^ this.gSBox[513 + (2 * ((x >>> 24) & 255))];
    }

    private int Fe32_3(int x) {
        return ((this.gSBox[0 + (((x >>> 24) & 255) * 2)] ^ this.gSBox[1 + ((x & 255) * 2)]) ^ this.gSBox[512 + (((x >>> 8) & 255) * 2)]) ^ this.gSBox[513 + (2 * ((x >>> 16) & 255))];
    }

    private int BytesTo32Bits(byte[] b, int p) {
        return (b[p] & 255) | ((b[p + 1] & 255) << 8) | ((b[p + 2] & 255) << Tnaf.POW_2_WIDTH) | ((b[p + 3] & 255) << 24);
    }

    private void Bits32ToBytes(int in, byte[] b, int offset) {
        b[offset] = (byte) in;
        b[offset + 1] = (byte) (in >> 8);
        b[offset + 2] = (byte) (in >> 16);
        b[offset + 3] = (byte) (in >> 24);
    }
}
