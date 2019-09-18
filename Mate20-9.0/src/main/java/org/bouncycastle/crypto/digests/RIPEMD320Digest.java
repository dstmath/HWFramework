package org.bouncycastle.crypto.digests;

import org.bouncycastle.util.Memoable;

public class RIPEMD320Digest extends GeneralDigest {
    private static final int DIGEST_LENGTH = 40;
    private int H0;
    private int H1;
    private int H2;
    private int H3;
    private int H4;
    private int H5;
    private int H6;
    private int H7;
    private int H8;
    private int H9;
    private int[] X = new int[16];
    private int xOff;

    public RIPEMD320Digest() {
        reset();
    }

    public RIPEMD320Digest(RIPEMD320Digest rIPEMD320Digest) {
        super((GeneralDigest) rIPEMD320Digest);
        doCopy(rIPEMD320Digest);
    }

    private int RL(int i, int i2) {
        return (i >>> (32 - i2)) | (i << i2);
    }

    private void doCopy(RIPEMD320Digest rIPEMD320Digest) {
        super.copyIn(rIPEMD320Digest);
        this.H0 = rIPEMD320Digest.H0;
        this.H1 = rIPEMD320Digest.H1;
        this.H2 = rIPEMD320Digest.H2;
        this.H3 = rIPEMD320Digest.H3;
        this.H4 = rIPEMD320Digest.H4;
        this.H5 = rIPEMD320Digest.H5;
        this.H6 = rIPEMD320Digest.H6;
        this.H7 = rIPEMD320Digest.H7;
        this.H8 = rIPEMD320Digest.H8;
        this.H9 = rIPEMD320Digest.H9;
        System.arraycopy(rIPEMD320Digest.X, 0, this.X, 0, rIPEMD320Digest.X.length);
        this.xOff = rIPEMD320Digest.xOff;
    }

    private int f1(int i, int i2, int i3) {
        return (i ^ i2) ^ i3;
    }

    private int f2(int i, int i2, int i3) {
        return ((~i) & i3) | (i2 & i);
    }

    private int f3(int i, int i2, int i3) {
        return (i | (~i2)) ^ i3;
    }

    private int f4(int i, int i2, int i3) {
        return (i & i3) | (i2 & (~i3));
    }

    private int f5(int i, int i2, int i3) {
        return i ^ (i2 | (~i3));
    }

    private void unpackWord(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) i;
        bArr[i2 + 1] = (byte) (i >>> 8);
        bArr[i2 + 2] = (byte) (i >>> 16);
        bArr[i2 + 3] = (byte) (i >>> 24);
    }

    public Memoable copy() {
        return new RIPEMD320Digest(this);
    }

    public int doFinal(byte[] bArr, int i) {
        finish();
        unpackWord(this.H0, bArr, i);
        unpackWord(this.H1, bArr, i + 4);
        unpackWord(this.H2, bArr, i + 8);
        unpackWord(this.H3, bArr, i + 12);
        unpackWord(this.H4, bArr, i + 16);
        unpackWord(this.H5, bArr, i + 20);
        unpackWord(this.H6, bArr, i + 24);
        unpackWord(this.H7, bArr, i + 28);
        unpackWord(this.H8, bArr, i + 32);
        unpackWord(this.H9, bArr, i + 36);
        reset();
        return 40;
    }

    public String getAlgorithmName() {
        return "RIPEMD320";
    }

    public int getDigestSize() {
        return 40;
    }

    /* access modifiers changed from: protected */
    public void processBlock() {
        int RL;
        int RL2;
        int RL3;
        int RL4;
        int RL5;
        int RL6;
        int RL7;
        int RL8;
        int RL9;
        int RL10;
        int RL11;
        int RL12;
        int RL13;
        int RL14;
        int RL15;
        int RL16;
        int RL17;
        int RL18;
        int RL19;
        int RL20;
        int RL21;
        int RL22;
        int RL23;
        int RL24;
        int RL25;
        int RL26;
        int RL27;
        int RL28;
        int RL29;
        int RL30;
        int RL31;
        int RL32;
        int RL33;
        int RL34;
        int RL35;
        int RL36;
        int RL37;
        int RL38;
        int RL39;
        int RL40;
        int RL41;
        int RL42;
        int RL43;
        int RL44;
        int RL45;
        int RL46;
        int RL47;
        int RL48;
        int RL49;
        int RL50;
        int RL51;
        int RL52;
        int RL53;
        int RL54;
        int RL55;
        int i = this.H0;
        int i2 = this.H1;
        int i3 = this.H2;
        int i4 = this.H3;
        int i5 = this.H4;
        int i6 = this.H5;
        int i7 = this.H6;
        int i8 = this.H7;
        int i9 = this.H8;
        int i10 = this.H9;
        int RL56 = RL(i + f1(i2, i3, i4) + this.X[0], 11) + i5;
        int RL57 = RL(i3, 10);
        int RL58 = RL(i5 + f1(RL56, i2, RL57) + this.X[1], 14) + i4;
        int RL59 = RL(i2, 10);
        int RL60 = RL(i4 + f1(RL58, RL56, RL59) + this.X[2], 15) + RL57;
        int RL61 = RL(RL56, 10);
        int RL62 = RL(RL57 + f1(RL60, RL58, RL61) + this.X[3], 12) + RL59;
        int RL63 = RL(RL58, 10);
        int RL64 = RL(RL59 + f1(RL62, RL60, RL63) + this.X[4], 5) + RL61;
        int RL65 = RL(RL60, 10);
        int RL66 = RL(RL61 + f1(RL64, RL62, RL65) + this.X[5], 8) + RL63;
        int RL67 = RL(RL62, 10);
        int RL68 = RL(RL63 + f1(RL66, RL64, RL67) + this.X[6], 7) + RL65;
        int RL69 = RL(RL64, 10);
        int RL70 = RL(RL65 + f1(RL68, RL66, RL69) + this.X[7], 9) + RL67;
        int RL71 = RL(RL66, 10);
        int RL72 = RL(RL67 + f1(RL70, RL68, RL71) + this.X[8], 11) + RL69;
        int RL73 = RL(RL68, 10);
        int RL74 = RL(RL69 + f1(RL72, RL70, RL73) + this.X[9], 13) + RL71;
        int RL75 = RL(RL70, 10);
        int RL76 = RL(RL71 + f1(RL74, RL72, RL75) + this.X[10], 14) + RL73;
        int RL77 = RL(RL72, 10);
        int RL78 = RL(RL73 + f1(RL76, RL74, RL77) + this.X[11], 15) + RL75;
        int RL79 = RL(RL74, 10);
        int RL80 = RL(RL75 + f1(RL78, RL76, RL79) + this.X[12], 6) + RL77;
        int RL81 = RL(RL76, 10);
        int RL82 = RL(RL77 + f1(RL80, RL78, RL81) + this.X[13], 7) + RL79;
        int RL83 = RL(RL78, 10);
        int RL84 = RL(RL79 + f1(RL82, RL80, RL83) + this.X[14], 9) + RL81;
        int RL85 = RL(RL80, 10);
        int RL86 = RL(RL81 + f1(RL84, RL82, RL85) + this.X[15], 8) + RL83;
        int RL87 = RL(RL82, 10);
        int RL88 = RL(i6 + f5(i7, i8, i9) + this.X[5] + 1352829926, 8) + i10;
        int RL89 = RL(i8, 10);
        int RL90 = RL(i10 + f5(RL88, i7, RL89) + this.X[14] + 1352829926, 9) + i9;
        int RL91 = RL(i7, 10);
        int RL92 = RL(i9 + f5(RL90, RL88, RL91) + this.X[7] + 1352829926, 9) + RL89;
        int RL93 = RL(RL88, 10);
        int RL94 = RL(RL89 + f5(RL92, RL90, RL93) + this.X[0] + 1352829926, 11) + RL91;
        int RL95 = RL(RL90, 10);
        int RL96 = RL(RL91 + f5(RL94, RL92, RL95) + this.X[9] + 1352829926, 13) + RL93;
        int RL97 = RL(RL92, 10);
        int RL98 = RL(RL93 + f5(RL96, RL94, RL97) + this.X[2] + 1352829926, 15) + RL95;
        int RL99 = RL(RL94, 10);
        int RL100 = RL(RL95 + f5(RL98, RL96, RL99) + this.X[11] + 1352829926, 15) + RL97;
        int RL101 = RL(RL96, 10);
        int RL102 = RL(RL97 + f5(RL100, RL98, RL101) + this.X[4] + 1352829926, 5) + RL99;
        int RL103 = RL(RL98, 10);
        int RL104 = RL(RL99 + f5(RL102, RL100, RL103) + this.X[13] + 1352829926, 7) + RL101;
        int RL105 = RL(RL100, 10);
        int RL106 = RL(RL101 + f5(RL104, RL102, RL105) + this.X[6] + 1352829926, 7) + RL103;
        int RL107 = RL(RL102, 10);
        int RL108 = RL(RL103 + f5(RL106, RL104, RL107) + this.X[15] + 1352829926, 8) + RL105;
        int RL109 = RL(RL104, 10);
        int RL110 = RL(RL105 + f5(RL108, RL106, RL109) + this.X[8] + 1352829926, 11) + RL107;
        int RL111 = RL(RL106, 10);
        int RL112 = RL(RL107 + f5(RL110, RL108, RL111) + this.X[1] + 1352829926, 14) + RL109;
        int RL113 = RL(RL108, 10);
        int RL114 = RL(RL109 + f5(RL112, RL110, RL113) + this.X[10] + 1352829926, 14) + RL111;
        int RL115 = RL(RL110, 10);
        int RL116 = RL(RL111 + f5(RL114, RL112, RL115) + this.X[3] + 1352829926, 12) + RL113;
        int RL117 = RL(RL112, 10);
        int RL118 = RL(RL113 + f5(RL116, RL114, RL117) + this.X[12] + 1352829926, 6) + RL115;
        int RL119 = RL(RL114, 10);
        int RL120 = RL(RL83 + f2(RL118, RL84, RL87) + this.X[7] + 1518500249, 7) + RL85;
        int RL121 = RL(RL84, 10);
        int RL122 = RL(RL85 + f2(RL120, RL118, RL121) + this.X[4] + 1518500249, 6) + RL87;
        int RL123 = RL(RL118, 10);
        int RL124 = RL(RL87 + f2(RL122, RL120, RL123) + this.X[13] + 1518500249, 8) + RL121;
        int RL125 = RL(RL120, 10);
        int RL126 = RL(RL121 + f2(RL124, RL122, RL125) + this.X[1] + 1518500249, 13) + RL123;
        int RL127 = RL(RL122, 10);
        int RL128 = RL(RL123 + f2(RL126, RL124, RL127) + this.X[10] + 1518500249, 11) + RL125;
        int RL129 = RL(RL124, 10);
        int RL130 = RL(RL125 + f2(RL128, RL126, RL129) + this.X[6] + 1518500249, 9) + RL127;
        int RL131 = RL(RL126, 10);
        int RL132 = RL(RL127 + f2(RL130, RL128, RL131) + this.X[15] + 1518500249, 7) + RL129;
        int RL133 = RL(RL128, 10);
        int RL134 = RL(RL129 + f2(RL132, RL130, RL133) + this.X[3] + 1518500249, 15) + RL131;
        int RL135 = RL(RL130, 10);
        int RL136 = RL(RL131 + f2(RL134, RL132, RL135) + this.X[12] + 1518500249, 7) + RL133;
        int RL137 = RL(RL132, 10);
        int RL138 = RL(RL133 + f2(RL136, RL134, RL137) + this.X[0] + 1518500249, 12) + RL135;
        int RL139 = RL(RL134, 10);
        int RL140 = RL(RL135 + f2(RL138, RL136, RL139) + this.X[9] + 1518500249, 15) + RL137;
        int RL141 = RL(RL136, 10);
        int RL142 = RL(RL137 + f2(RL140, RL138, RL141) + this.X[5] + 1518500249, 9) + RL139;
        int RL143 = RL(RL138, 10);
        int RL144 = RL(RL139 + f2(RL142, RL140, RL143) + this.X[2] + 1518500249, 11) + RL141;
        int RL145 = RL(RL140, 10);
        int RL146 = RL(RL141 + f2(RL144, RL142, RL145) + this.X[14] + 1518500249, 7) + RL143;
        int RL147 = RL(RL142, 10);
        int RL148 = RL(RL143 + f2(RL146, RL144, RL147) + this.X[11] + 1518500249, 13) + RL145;
        int RL149 = RL(RL144, 10);
        int RL150 = RL(RL145 + f2(RL148, RL146, RL149) + this.X[8] + 1518500249, 12) + RL147;
        int RL151 = RL(RL146, 10);
        int RL152 = RL(RL115 + f4(RL86, RL116, RL119) + this.X[6] + 1548603684, 9) + RL117;
        int RL153 = RL(RL116, 10);
        int RL154 = RL(RL117 + f4(RL152, RL86, RL153) + this.X[11] + 1548603684, 13) + RL119;
        int RL155 = RL(RL86, 10);
        int RL156 = RL(RL119 + f4(RL154, RL152, RL155) + this.X[3] + 1548603684, 15) + RL153;
        int RL157 = RL(RL152, 10);
        int RL158 = RL(RL153 + f4(RL156, RL154, RL157) + this.X[7] + 1548603684, 7) + RL155;
        int RL159 = RL(RL154, 10);
        int RL160 = RL(RL155 + f4(RL158, RL156, RL159) + this.X[0] + 1548603684, 12) + RL157;
        int RL161 = RL(RL156, 10);
        int RL162 = RL(RL157 + f4(RL160, RL158, RL161) + this.X[13] + 1548603684, 8) + RL159;
        int RL163 = RL(RL158, 10);
        int RL164 = RL(RL159 + f4(RL162, RL160, RL163) + this.X[5] + 1548603684, 9) + RL161;
        int RL165 = RL(RL160, 10);
        int RL166 = RL(RL161 + f4(RL164, RL162, RL165) + this.X[10] + 1548603684, 11) + RL163;
        int RL167 = RL(RL162, 10);
        int RL168 = RL(RL163 + f4(RL166, RL164, RL167) + this.X[14] + 1548603684, 7) + RL165;
        int RL169 = RL(RL164, 10);
        int RL170 = RL(RL165 + f4(RL168, RL166, RL169) + this.X[15] + 1548603684, 7) + RL167;
        int RL171 = RL(RL166, 10);
        int RL172 = RL(RL167 + f4(RL170, RL168, RL171) + this.X[8] + 1548603684, 12) + RL169;
        int RL173 = RL(RL168, 10);
        int RL174 = RL(RL169 + f4(RL172, RL170, RL173) + this.X[12] + 1548603684, 7) + RL171;
        int RL175 = RL(RL170, 10);
        int RL176 = RL(RL171 + f4(RL174, RL172, RL175) + this.X[4] + 1548603684, 6) + RL173;
        int RL177 = RL(RL172, 10);
        int RL178 = RL(RL173 + f4(RL176, RL174, RL177) + this.X[9] + 1548603684, 15) + RL175;
        int RL179 = RL(RL174, 10);
        int RL180 = RL(RL175 + f4(RL178, RL176, RL179) + this.X[1] + 1548603684, 13) + RL177;
        int RL181 = RL(RL176, 10);
        int RL182 = RL(RL177 + f4(RL180, RL178, RL181) + this.X[2] + 1548603684, 11) + RL179;
        int RL183 = RL(RL178, 10);
        int RL184 = RL(RL147 + f3(RL150, RL148, RL183) + this.X[3] + 1859775393, 11) + RL149;
        int RL185 = RL(RL148, 10);
        int RL186 = RL(RL149 + f3(RL184, RL150, RL185) + this.X[10] + 1859775393, 13) + RL183;
        int RL187 = RL(RL150, 10);
        int RL188 = RL(RL183 + f3(RL186, RL184, RL187) + this.X[14] + 1859775393, 6) + RL185;
        int RL189 = RL(RL184, 10);
        int RL190 = RL(RL185 + f3(RL188, RL186, RL189) + this.X[4] + 1859775393, 7) + RL187;
        int RL191 = RL(RL186, 10);
        int RL192 = RL(RL187 + f3(RL190, RL188, RL191) + this.X[9] + 1859775393, 14) + RL189;
        int RL193 = RL(RL188, 10);
        int RL194 = RL(RL189 + f3(RL192, RL190, RL193) + this.X[15] + 1859775393, 9) + RL191;
        int RL195 = RL(RL190, 10);
        int RL196 = RL(RL191 + f3(RL194, RL192, RL195) + this.X[8] + 1859775393, 13) + RL193;
        int RL197 = RL(RL192, 10);
        int RL198 = RL(RL193 + f3(RL196, RL194, RL197) + this.X[1] + 1859775393, 15) + RL195;
        int RL199 = RL(RL194, 10);
        int RL200 = RL(RL195 + f3(RL198, RL196, RL199) + this.X[2] + 1859775393, 14) + RL197;
        int RL201 = RL(RL196, 10);
        int RL202 = RL(RL197 + f3(RL200, RL198, RL201) + this.X[7] + 1859775393, 8) + RL199;
        int RL203 = RL(RL198, 10);
        int RL204 = RL(RL199 + f3(RL202, RL200, RL203) + this.X[0] + 1859775393, 13) + RL201;
        int RL205 = RL(RL200, 10);
        int RL206 = RL(RL201 + f3(RL204, RL202, RL205) + this.X[6] + 1859775393, 6) + RL203;
        int RL207 = RL(RL202, 10);
        int RL208 = RL(RL203 + f3(RL206, RL204, RL207) + this.X[13] + 1859775393, 5) + RL205;
        int RL209 = RL(RL204, 10);
        int RL210 = RL(RL205 + f3(RL208, RL206, RL209) + this.X[11] + 1859775393, 12) + RL207;
        int RL211 = RL(RL206, 10);
        int RL212 = RL(RL207 + f3(RL210, RL208, RL211) + this.X[5] + 1859775393, 7) + RL209;
        int RL213 = RL(RL208, 10);
        int RL214 = RL(RL209 + f3(RL212, RL210, RL213) + this.X[12] + 1859775393, 5) + RL211;
        int RL215 = RL(RL210, 10);
        int RL216 = RL(RL179 + f3(RL182, RL180, RL151) + this.X[15] + 1836072691, 9) + RL181;
        int RL217 = RL(RL180, 10);
        int RL218 = RL(RL181 + f3(RL216, RL182, RL217) + this.X[5] + 1836072691, 7) + RL151;
        int RL219 = RL(RL182, 10);
        int RL220 = RL(RL151 + f3(RL218, RL216, RL219) + this.X[1] + 1836072691, 15) + RL217;
        int RL221 = RL(RL216, 10);
        int RL222 = RL(RL217 + f3(RL220, RL218, RL221) + this.X[3] + 1836072691, 11) + RL219;
        int RL223 = RL(RL218, 10);
        int RL224 = RL(RL219 + f3(RL222, RL220, RL223) + this.X[7] + 1836072691, 8) + RL221;
        int RL225 = RL(RL220, 10);
        int RL226 = RL(RL221 + f3(RL224, RL222, RL225) + this.X[14] + 1836072691, 6) + RL223;
        int RL227 = RL(RL222, 10);
        int RL228 = RL(RL223 + f3(RL226, RL224, RL227) + this.X[6] + 1836072691, 6) + RL225;
        int RL229 = RL(RL224, 10);
        int RL230 = RL(RL225 + f3(RL228, RL226, RL229) + this.X[9] + 1836072691, 14) + RL227;
        int RL231 = RL(RL226, 10);
        int RL232 = RL(RL227 + f3(RL230, RL228, RL231) + this.X[11] + 1836072691, 12) + RL229;
        int RL233 = RL(RL228, 10);
        int RL234 = RL(RL229 + f3(RL232, RL230, RL233) + this.X[8] + 1836072691, 13) + RL231;
        int RL235 = RL(RL230, 10);
        int RL236 = RL(RL231 + f3(RL234, RL232, RL235) + this.X[12] + 1836072691, 5) + RL233;
        int RL237 = RL(RL232, 10);
        int RL238 = RL(RL233 + f3(RL236, RL234, RL237) + this.X[2] + 1836072691, 14) + RL235;
        int RL239 = RL(RL234, 10);
        int RL240 = RL(RL235 + f3(RL238, RL236, RL239) + this.X[10] + 1836072691, 13) + RL237;
        int RL241 = RL(RL236, 10);
        int RL242 = RL(RL237 + f3(RL240, RL238, RL241) + this.X[0] + 1836072691, 13) + RL239;
        int RL243 = RL(RL238, 10);
        int RL244 = RL(RL239 + f3(RL242, RL240, RL243) + this.X[4] + 1836072691, 7) + RL241;
        int RL245 = RL(RL240, 10);
        int RL246 = RL(RL241 + f3(RL244, RL242, RL245) + this.X[13] + 1836072691, 5) + RL243;
        int RL247 = RL(RL242, 10);
        int RL248 = RL(((RL(RL18, 10) + f4(RL(((RL(RL16, 10) + f4(RL(((RL(RL14, 10) + f4(RL(((RL(RL12, 10) + f4(RL(((RL(RL10, 10) + f4(RL(((RL(RL8, 10) + f4(RL(((RL(RL6, 10) + f4(RL(((RL(RL4, 10) + f4(RL(((RL(RL2, 10) + f4(RL(((RL(RL, 10) + f4(RL(((RL(RL214, 10) + f4(RL(((RL(RL212, 10) + f4(RL(((RL215 + f4(RL(((RL213 + f4(RL(((RL243 + f4(RL214, RL212, RL215)) + this.X[1]) - 1894007588, 11) + RL213, RL214, RL(RL212, 10))) + this.X[9]) - 1894007588, 12) + RL215, RL, RL(RL214, 10))) + this.X[11]) - 1894007588, 14) + RL(RL212, 10), RL2, RL(RL, 10))) + this.X[10]) - 1894007588, 15) + RL3, RL4, RL(RL2, 10))) + this.X[0]) - 1894007588, 14) + RL5, RL6, RL(RL4, 10))) + this.X[8]) - 1894007588, 15) + RL7, RL8, RL(RL6, 10))) + this.X[12]) - 1894007588, 9) + RL9, RL10, RL(RL8, 10))) + this.X[4]) - 1894007588, 8) + RL11, RL12, RL(RL10, 10))) + this.X[13]) - 1894007588, 9) + RL13, RL14, RL(RL12, 10))) + this.X[3]) - 1894007588, 14) + RL15, RL16, RL(RL14, 10))) + this.X[7]) - 1894007588, 5) + RL17, RL18, RL(RL16, 10))) + this.X[15]) - 1894007588, 6) + RL19, RL20, RL(RL18, 10))) + this.X[14]) - 1894007588, 8) + RL21, RL22, RL(RL20, 10))) + this.X[5]) - 1894007588, 6) + RL23, RL24, RL(RL22, 10))) + this.X[6]) - 1894007588, 5) + RL25;
        int RL249 = RL(((RL25 + f4(RL248, RL26, RL(RL24, 10))) + this.X[2]) - 1894007588, 12) + RL27;
        int RL250 = RL(RL26, 10);
        int RL251 = RL(RL211 + f2(RL246, RL244, RL247) + this.X[8] + 2053994217, 15) + RL245;
        int RL252 = RL(RL244, 10);
        int RL253 = RL(RL245 + f2(RL251, RL246, RL252) + this.X[6] + 2053994217, 5) + RL247;
        int RL254 = RL(RL246, 10);
        int RL255 = RL(RL247 + f2(RL253, RL251, RL254) + this.X[4] + 2053994217, 8) + RL252;
        int RL256 = RL(RL251, 10);
        int RL257 = RL(RL252 + f2(RL255, RL253, RL256) + this.X[1] + 2053994217, 11) + RL254;
        int RL258 = RL(RL253, 10);
        int RL259 = RL(RL254 + f2(RL257, RL255, RL258) + this.X[3] + 2053994217, 14) + RL256;
        int RL260 = RL(RL255, 10);
        int RL261 = RL(RL256 + f2(RL259, RL257, RL260) + this.X[11] + 2053994217, 14) + RL258;
        int RL262 = RL(RL257, 10);
        int RL263 = RL(RL258 + f2(RL261, RL259, RL262) + this.X[15] + 2053994217, 6) + RL260;
        int RL264 = RL(RL259, 10);
        int RL265 = RL(RL260 + f2(RL263, RL261, RL264) + this.X[0] + 2053994217, 14) + RL262;
        int RL266 = RL(RL261, 10);
        int RL267 = RL(RL262 + f2(RL265, RL263, RL266) + this.X[5] + 2053994217, 6) + RL264;
        int RL268 = RL(RL263, 10);
        int RL269 = RL(RL264 + f2(RL267, RL265, RL268) + this.X[12] + 2053994217, 9) + RL266;
        int RL270 = RL(RL265, 10);
        int RL271 = RL(RL266 + f2(RL269, RL267, RL270) + this.X[2] + 2053994217, 12) + RL268;
        int RL272 = RL(RL267, 10);
        int RL273 = RL(RL268 + f2(RL271, RL269, RL272) + this.X[13] + 2053994217, 9) + RL270;
        int RL274 = RL(RL269, 10);
        int RL275 = RL(RL270 + f2(RL273, RL271, RL274) + this.X[9] + 2053994217, 12) + RL272;
        int RL276 = RL(RL271, 10);
        int RL277 = RL(RL272 + f2(RL275, RL273, RL276) + this.X[7] + 2053994217, 5) + RL274;
        int RL278 = RL(RL273, 10);
        int RL279 = RL(RL274 + f2(RL277, RL275, RL278) + this.X[10] + 2053994217, 15) + RL276;
        int RL280 = RL(RL275, 10);
        int RL281 = RL(RL276 + f2(RL279, RL277, RL280) + this.X[14] + 2053994217, 8) + RL278;
        int RL282 = RL(RL277, 10);
        int RL283 = RL(((RL(RL44, 10) + f5(RL(((RL(RL42, 10) + f5(RL(((RL(RL40, 10) + f5(RL(((RL(RL38, 10) + f5(RL(((RL(RL36, 10) + f5(RL(((RL(RL34, 10) + f5(RL(((RL(RL32, 10) + f5(RL(((RL(RL30, 10) + f5(RL(((RL(RL29, 10) + f5(RL(((RL(RL249, 10) + f5(RL(((RL(RL279, 10) + f5(RL(((RL250 + f5(RL(((RL28 + f5(RL(((RL27 + f5(RL249, RL279, RL250)) + this.X[4]) - 1454113458, 9) + RL28, RL249, RL(RL279, 10))) + this.X[0]) - 1454113458, 15) + RL250, RL29, RL(RL249, 10))) + this.X[5]) - 1454113458, 5) + RL(RL279, 10), RL30, RL(RL29, 10))) + this.X[9]) - 1454113458, 11) + RL31, RL32, RL(RL30, 10))) + this.X[7]) - 1454113458, 6) + RL33, RL34, RL(RL32, 10))) + this.X[12]) - 1454113458, 8) + RL35, RL36, RL(RL34, 10))) + this.X[2]) - 1454113458, 13) + RL37, RL38, RL(RL36, 10))) + this.X[10]) - 1454113458, 12) + RL39, RL40, RL(RL38, 10))) + this.X[14]) - 1454113458, 5) + RL41, RL42, RL(RL40, 10))) + this.X[1]) - 1454113458, 12) + RL43, RL44, RL(RL42, 10))) + this.X[3]) - 1454113458, 13) + RL45, RL46, RL(RL44, 10))) + this.X[8]) - 1454113458, 14) + RL47, RL48, RL(RL46, 10))) + this.X[11]) - 1454113458, 11) + RL49, RL50, RL(RL48, 10))) + this.X[6]) - 1454113458, 8) + RL51;
        int RL284 = RL(RL50, 10);
        int RL285 = RL(((RL53 + f5(RL(((RL51 + f5(RL283, RL52, RL284)) + this.X[15]) - 1454113458, 5) + RL53, RL283, RL(RL52, 10))) + this.X[13]) - 1454113458, 6) + RL284;
        int RL286 = RL(RL283, 10);
        int RL287 = RL(RL278 + f1(RL281, RL248, RL282) + this.X[12], 8) + RL280;
        int RL288 = RL(RL248, 10);
        int RL289 = RL(RL280 + f1(RL287, RL281, RL288) + this.X[15], 5) + RL282;
        int RL290 = RL(RL281, 10);
        int RL291 = RL(RL282 + f1(RL289, RL287, RL290) + this.X[10], 12) + RL288;
        int RL292 = RL(RL287, 10);
        int RL293 = RL(RL288 + f1(RL291, RL289, RL292) + this.X[4], 9) + RL290;
        int RL294 = RL(RL289, 10);
        int RL295 = RL(RL290 + f1(RL293, RL291, RL294) + this.X[1], 12) + RL292;
        int RL296 = RL(RL291, 10);
        int RL297 = RL(RL292 + f1(RL295, RL293, RL296) + this.X[5], 5) + RL294;
        int RL298 = RL(RL293, 10);
        int RL299 = RL(RL294 + f1(RL297, RL295, RL298) + this.X[8], 14) + RL296;
        int RL300 = RL(RL295, 10);
        int RL301 = RL(RL296 + f1(RL299, RL297, RL300) + this.X[7], 6) + RL298;
        int RL302 = RL(RL297, 10);
        int RL303 = RL(RL298 + f1(RL301, RL299, RL302) + this.X[6], 8) + RL300;
        int RL304 = RL(RL299, 10);
        int RL305 = RL(RL300 + f1(RL303, RL301, RL304) + this.X[2], 13) + RL302;
        int RL306 = RL(RL301, 10);
        int RL307 = RL(RL302 + f1(RL305, RL303, RL306) + this.X[13], 6) + RL304;
        int RL308 = RL(RL303, 10);
        int RL309 = RL(RL304 + f1(RL307, RL305, RL308) + this.X[14], 5) + RL306;
        int RL310 = RL(RL305, 10);
        int RL311 = RL(RL306 + f1(RL309, RL307, RL310) + this.X[0], 15) + RL308;
        int RL312 = RL(RL307, 10);
        int RL313 = RL(RL308 + f1(RL311, RL309, RL312) + this.X[3], 13) + RL310;
        int RL314 = RL(RL309, 10);
        int RL315 = RL(RL310 + f1(RL313, RL311, RL314) + this.X[9], 11) + RL312;
        int RL316 = RL(RL311, 10);
        int RL317 = RL(RL312 + f1(RL315, RL313, RL316) + this.X[11], 11) + RL314;
        int RL318 = RL(RL313, 10);
        this.H0 += RL284;
        this.H1 += RL285;
        this.H2 += RL54;
        this.H3 += RL286;
        this.H4 += RL316;
        this.H5 += RL314;
        this.H6 += RL317;
        this.H7 += RL315;
        this.H8 += RL318;
        this.H9 += RL55;
        this.xOff = 0;
        for (int i11 = 0; i11 != this.X.length; i11++) {
            this.X[i11] = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void processLength(long j) {
        if (this.xOff > 14) {
            processBlock();
        }
        this.X[14] = (int) (-1 & j);
        this.X[15] = (int) (j >>> 32);
    }

    /* access modifiers changed from: protected */
    public void processWord(byte[] bArr, int i) {
        int[] iArr = this.X;
        int i2 = this.xOff;
        this.xOff = i2 + 1;
        iArr[i2] = ((bArr[i + 3] & 255) << 24) | (bArr[i] & 255) | ((bArr[i + 1] & 255) << 8) | ((bArr[i + 2] & 255) << Tnaf.POW_2_WIDTH);
        if (this.xOff == 16) {
            processBlock();
        }
    }

    public void reset() {
        super.reset();
        this.H0 = 1732584193;
        this.H1 = -271733879;
        this.H2 = -1732584194;
        this.H3 = 271733878;
        this.H4 = -1009589776;
        this.H5 = 1985229328;
        this.H6 = -19088744;
        this.H7 = -1985229329;
        this.H8 = 19088743;
        this.H9 = 1009589775;
        this.xOff = 0;
        for (int i = 0; i != this.X.length; i++) {
            this.X[i] = 0;
        }
    }

    public void reset(Memoable memoable) {
        doCopy((RIPEMD320Digest) memoable);
    }
}
