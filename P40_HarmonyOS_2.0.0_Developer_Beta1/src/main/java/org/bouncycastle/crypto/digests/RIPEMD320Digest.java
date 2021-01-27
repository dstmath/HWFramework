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
    private int[] X;
    private int xOff;

    public RIPEMD320Digest() {
        this.X = new int[16];
        reset();
    }

    public RIPEMD320Digest(RIPEMD320Digest rIPEMD320Digest) {
        super(rIPEMD320Digest);
        this.X = new int[16];
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
        int[] iArr = rIPEMD320Digest.X;
        System.arraycopy(iArr, 0, this.X, 0, iArr.length);
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

    @Override // org.bouncycastle.util.Memoable
    public Memoable copy() {
        return new RIPEMD320Digest(this);
    }

    @Override // org.bouncycastle.crypto.Digest
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

    @Override // org.bouncycastle.crypto.Digest
    public String getAlgorithmName() {
        return "RIPEMD320";
    }

    @Override // org.bouncycastle.crypto.Digest
    public int getDigestSize() {
        return 40;
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.crypto.digests.GeneralDigest
    public void processBlock() {
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
        int RL = RL(i + f1(i2, i3, i4) + this.X[0], 11) + i5;
        int RL2 = RL(i3, 10);
        int RL3 = RL(i5 + f1(RL, i2, RL2) + this.X[1], 14) + i4;
        int RL4 = RL(i2, 10);
        int RL5 = RL(i4 + f1(RL3, RL, RL4) + this.X[2], 15) + RL2;
        int RL6 = RL(RL, 10);
        int RL7 = RL(RL2 + f1(RL5, RL3, RL6) + this.X[3], 12) + RL4;
        int RL8 = RL(RL3, 10);
        int RL9 = RL(RL4 + f1(RL7, RL5, RL8) + this.X[4], 5) + RL6;
        int RL10 = RL(RL5, 10);
        int RL11 = RL(RL6 + f1(RL9, RL7, RL10) + this.X[5], 8) + RL8;
        int RL12 = RL(RL7, 10);
        int RL13 = RL(RL8 + f1(RL11, RL9, RL12) + this.X[6], 7) + RL10;
        int RL14 = RL(RL9, 10);
        int RL15 = RL(RL10 + f1(RL13, RL11, RL14) + this.X[7], 9) + RL12;
        int RL16 = RL(RL11, 10);
        int RL17 = RL(RL12 + f1(RL15, RL13, RL16) + this.X[8], 11) + RL14;
        int RL18 = RL(RL13, 10);
        int RL19 = RL(RL14 + f1(RL17, RL15, RL18) + this.X[9], 13) + RL16;
        int RL20 = RL(RL15, 10);
        int RL21 = RL(RL16 + f1(RL19, RL17, RL20) + this.X[10], 14) + RL18;
        int RL22 = RL(RL17, 10);
        int RL23 = RL(RL18 + f1(RL21, RL19, RL22) + this.X[11], 15) + RL20;
        int RL24 = RL(RL19, 10);
        int RL25 = RL(RL20 + f1(RL23, RL21, RL24) + this.X[12], 6) + RL22;
        int RL26 = RL(RL21, 10);
        int RL27 = RL(RL22 + f1(RL25, RL23, RL26) + this.X[13], 7) + RL24;
        int RL28 = RL(RL23, 10);
        int RL29 = RL(RL24 + f1(RL27, RL25, RL28) + this.X[14], 9) + RL26;
        int RL30 = RL(RL25, 10);
        int RL31 = RL(RL26 + f1(RL29, RL27, RL30) + this.X[15], 8) + RL28;
        int RL32 = RL(RL27, 10);
        int RL33 = RL(i6 + f5(i7, i8, i9) + this.X[5] + 1352829926, 8) + i10;
        int RL34 = RL(i8, 10);
        int RL35 = RL(i10 + f5(RL33, i7, RL34) + this.X[14] + 1352829926, 9) + i9;
        int RL36 = RL(i7, 10);
        int RL37 = RL(i9 + f5(RL35, RL33, RL36) + this.X[7] + 1352829926, 9) + RL34;
        int RL38 = RL(RL33, 10);
        int RL39 = RL(RL34 + f5(RL37, RL35, RL38) + this.X[0] + 1352829926, 11) + RL36;
        int RL40 = RL(RL35, 10);
        int RL41 = RL(RL36 + f5(RL39, RL37, RL40) + this.X[9] + 1352829926, 13) + RL38;
        int RL42 = RL(RL37, 10);
        int RL43 = RL(RL38 + f5(RL41, RL39, RL42) + this.X[2] + 1352829926, 15) + RL40;
        int RL44 = RL(RL39, 10);
        int RL45 = RL(RL40 + f5(RL43, RL41, RL44) + this.X[11] + 1352829926, 15) + RL42;
        int RL46 = RL(RL41, 10);
        int RL47 = RL(RL42 + f5(RL45, RL43, RL46) + this.X[4] + 1352829926, 5) + RL44;
        int RL48 = RL(RL43, 10);
        int RL49 = RL(RL44 + f5(RL47, RL45, RL48) + this.X[13] + 1352829926, 7) + RL46;
        int RL50 = RL(RL45, 10);
        int RL51 = RL(RL46 + f5(RL49, RL47, RL50) + this.X[6] + 1352829926, 7) + RL48;
        int RL52 = RL(RL47, 10);
        int RL53 = RL(RL48 + f5(RL51, RL49, RL52) + this.X[15] + 1352829926, 8) + RL50;
        int RL54 = RL(RL49, 10);
        int RL55 = RL(RL50 + f5(RL53, RL51, RL54) + this.X[8] + 1352829926, 11) + RL52;
        int RL56 = RL(RL51, 10);
        int RL57 = RL(RL52 + f5(RL55, RL53, RL56) + this.X[1] + 1352829926, 14) + RL54;
        int RL58 = RL(RL53, 10);
        int RL59 = RL(RL54 + f5(RL57, RL55, RL58) + this.X[10] + 1352829926, 14) + RL56;
        int RL60 = RL(RL55, 10);
        int RL61 = RL(RL56 + f5(RL59, RL57, RL60) + this.X[3] + 1352829926, 12) + RL58;
        int RL62 = RL(RL57, 10);
        int RL63 = RL(RL58 + f5(RL61, RL59, RL62) + this.X[12] + 1352829926, 6) + RL60;
        int RL64 = RL(RL59, 10);
        int RL65 = RL(RL28 + f2(RL63, RL29, RL32) + this.X[7] + 1518500249, 7) + RL30;
        int RL66 = RL(RL29, 10);
        int RL67 = RL(RL30 + f2(RL65, RL63, RL66) + this.X[4] + 1518500249, 6) + RL32;
        int RL68 = RL(RL63, 10);
        int RL69 = RL(RL32 + f2(RL67, RL65, RL68) + this.X[13] + 1518500249, 8) + RL66;
        int RL70 = RL(RL65, 10);
        int RL71 = RL(RL66 + f2(RL69, RL67, RL70) + this.X[1] + 1518500249, 13) + RL68;
        int RL72 = RL(RL67, 10);
        int RL73 = RL(RL68 + f2(RL71, RL69, RL72) + this.X[10] + 1518500249, 11) + RL70;
        int RL74 = RL(RL69, 10);
        int RL75 = RL(RL70 + f2(RL73, RL71, RL74) + this.X[6] + 1518500249, 9) + RL72;
        int RL76 = RL(RL71, 10);
        int RL77 = RL(RL72 + f2(RL75, RL73, RL76) + this.X[15] + 1518500249, 7) + RL74;
        int RL78 = RL(RL73, 10);
        int RL79 = RL(RL74 + f2(RL77, RL75, RL78) + this.X[3] + 1518500249, 15) + RL76;
        int RL80 = RL(RL75, 10);
        int RL81 = RL(RL76 + f2(RL79, RL77, RL80) + this.X[12] + 1518500249, 7) + RL78;
        int RL82 = RL(RL77, 10);
        int RL83 = RL(RL78 + f2(RL81, RL79, RL82) + this.X[0] + 1518500249, 12) + RL80;
        int RL84 = RL(RL79, 10);
        int RL85 = RL(RL80 + f2(RL83, RL81, RL84) + this.X[9] + 1518500249, 15) + RL82;
        int RL86 = RL(RL81, 10);
        int RL87 = RL(RL82 + f2(RL85, RL83, RL86) + this.X[5] + 1518500249, 9) + RL84;
        int RL88 = RL(RL83, 10);
        int RL89 = RL(RL84 + f2(RL87, RL85, RL88) + this.X[2] + 1518500249, 11) + RL86;
        int RL90 = RL(RL85, 10);
        int RL91 = RL(RL86 + f2(RL89, RL87, RL90) + this.X[14] + 1518500249, 7) + RL88;
        int RL92 = RL(RL87, 10);
        int RL93 = RL(RL88 + f2(RL91, RL89, RL92) + this.X[11] + 1518500249, 13) + RL90;
        int RL94 = RL(RL89, 10);
        int RL95 = RL(RL90 + f2(RL93, RL91, RL94) + this.X[8] + 1518500249, 12) + RL92;
        int RL96 = RL(RL91, 10);
        int RL97 = RL(RL60 + f4(RL31, RL61, RL64) + this.X[6] + 1548603684, 9) + RL62;
        int RL98 = RL(RL61, 10);
        int RL99 = RL(RL62 + f4(RL97, RL31, RL98) + this.X[11] + 1548603684, 13) + RL64;
        int RL100 = RL(RL31, 10);
        int RL101 = RL(RL64 + f4(RL99, RL97, RL100) + this.X[3] + 1548603684, 15) + RL98;
        int RL102 = RL(RL97, 10);
        int RL103 = RL(RL98 + f4(RL101, RL99, RL102) + this.X[7] + 1548603684, 7) + RL100;
        int RL104 = RL(RL99, 10);
        int RL105 = RL(RL100 + f4(RL103, RL101, RL104) + this.X[0] + 1548603684, 12) + RL102;
        int RL106 = RL(RL101, 10);
        int RL107 = RL(RL102 + f4(RL105, RL103, RL106) + this.X[13] + 1548603684, 8) + RL104;
        int RL108 = RL(RL103, 10);
        int RL109 = RL(RL104 + f4(RL107, RL105, RL108) + this.X[5] + 1548603684, 9) + RL106;
        int RL110 = RL(RL105, 10);
        int RL111 = RL(RL106 + f4(RL109, RL107, RL110) + this.X[10] + 1548603684, 11) + RL108;
        int RL112 = RL(RL107, 10);
        int RL113 = RL(RL108 + f4(RL111, RL109, RL112) + this.X[14] + 1548603684, 7) + RL110;
        int RL114 = RL(RL109, 10);
        int RL115 = RL(RL110 + f4(RL113, RL111, RL114) + this.X[15] + 1548603684, 7) + RL112;
        int RL116 = RL(RL111, 10);
        int RL117 = RL(RL112 + f4(RL115, RL113, RL116) + this.X[8] + 1548603684, 12) + RL114;
        int RL118 = RL(RL113, 10);
        int RL119 = RL(RL114 + f4(RL117, RL115, RL118) + this.X[12] + 1548603684, 7) + RL116;
        int RL120 = RL(RL115, 10);
        int RL121 = RL(RL116 + f4(RL119, RL117, RL120) + this.X[4] + 1548603684, 6) + RL118;
        int RL122 = RL(RL117, 10);
        int RL123 = RL(RL118 + f4(RL121, RL119, RL122) + this.X[9] + 1548603684, 15) + RL120;
        int RL124 = RL(RL119, 10);
        int RL125 = RL(RL120 + f4(RL123, RL121, RL124) + this.X[1] + 1548603684, 13) + RL122;
        int RL126 = RL(RL121, 10);
        int RL127 = RL(RL122 + f4(RL125, RL123, RL126) + this.X[2] + 1548603684, 11) + RL124;
        int RL128 = RL(RL123, 10);
        int RL129 = RL(RL92 + f3(RL95, RL93, RL128) + this.X[3] + 1859775393, 11) + RL94;
        int RL130 = RL(RL93, 10);
        int RL131 = RL(RL94 + f3(RL129, RL95, RL130) + this.X[10] + 1859775393, 13) + RL128;
        int RL132 = RL(RL95, 10);
        int RL133 = RL(RL128 + f3(RL131, RL129, RL132) + this.X[14] + 1859775393, 6) + RL130;
        int RL134 = RL(RL129, 10);
        int RL135 = RL(RL130 + f3(RL133, RL131, RL134) + this.X[4] + 1859775393, 7) + RL132;
        int RL136 = RL(RL131, 10);
        int RL137 = RL(RL132 + f3(RL135, RL133, RL136) + this.X[9] + 1859775393, 14) + RL134;
        int RL138 = RL(RL133, 10);
        int RL139 = RL(RL134 + f3(RL137, RL135, RL138) + this.X[15] + 1859775393, 9) + RL136;
        int RL140 = RL(RL135, 10);
        int RL141 = RL(RL136 + f3(RL139, RL137, RL140) + this.X[8] + 1859775393, 13) + RL138;
        int RL142 = RL(RL137, 10);
        int RL143 = RL(RL138 + f3(RL141, RL139, RL142) + this.X[1] + 1859775393, 15) + RL140;
        int RL144 = RL(RL139, 10);
        int RL145 = RL(RL140 + f3(RL143, RL141, RL144) + this.X[2] + 1859775393, 14) + RL142;
        int RL146 = RL(RL141, 10);
        int RL147 = RL(RL142 + f3(RL145, RL143, RL146) + this.X[7] + 1859775393, 8) + RL144;
        int RL148 = RL(RL143, 10);
        int RL149 = RL(RL144 + f3(RL147, RL145, RL148) + this.X[0] + 1859775393, 13) + RL146;
        int RL150 = RL(RL145, 10);
        int RL151 = RL(RL146 + f3(RL149, RL147, RL150) + this.X[6] + 1859775393, 6) + RL148;
        int RL152 = RL(RL147, 10);
        int RL153 = RL(RL148 + f3(RL151, RL149, RL152) + this.X[13] + 1859775393, 5) + RL150;
        int RL154 = RL(RL149, 10);
        int RL155 = RL(RL150 + f3(RL153, RL151, RL154) + this.X[11] + 1859775393, 12) + RL152;
        int RL156 = RL(RL151, 10);
        int RL157 = RL(RL152 + f3(RL155, RL153, RL156) + this.X[5] + 1859775393, 7) + RL154;
        int RL158 = RL(RL153, 10);
        int RL159 = RL(RL154 + f3(RL157, RL155, RL158) + this.X[12] + 1859775393, 5) + RL156;
        int RL160 = RL(RL155, 10);
        int RL161 = RL(RL124 + f3(RL127, RL125, RL96) + this.X[15] + 1836072691, 9) + RL126;
        int RL162 = RL(RL125, 10);
        int RL163 = RL(RL126 + f3(RL161, RL127, RL162) + this.X[5] + 1836072691, 7) + RL96;
        int RL164 = RL(RL127, 10);
        int RL165 = RL(RL96 + f3(RL163, RL161, RL164) + this.X[1] + 1836072691, 15) + RL162;
        int RL166 = RL(RL161, 10);
        int RL167 = RL(RL162 + f3(RL165, RL163, RL166) + this.X[3] + 1836072691, 11) + RL164;
        int RL168 = RL(RL163, 10);
        int RL169 = RL(RL164 + f3(RL167, RL165, RL168) + this.X[7] + 1836072691, 8) + RL166;
        int RL170 = RL(RL165, 10);
        int RL171 = RL(RL166 + f3(RL169, RL167, RL170) + this.X[14] + 1836072691, 6) + RL168;
        int RL172 = RL(RL167, 10);
        int RL173 = RL(RL168 + f3(RL171, RL169, RL172) + this.X[6] + 1836072691, 6) + RL170;
        int RL174 = RL(RL169, 10);
        int RL175 = RL(RL170 + f3(RL173, RL171, RL174) + this.X[9] + 1836072691, 14) + RL172;
        int RL176 = RL(RL171, 10);
        int RL177 = RL(RL172 + f3(RL175, RL173, RL176) + this.X[11] + 1836072691, 12) + RL174;
        int RL178 = RL(RL173, 10);
        int RL179 = RL(RL174 + f3(RL177, RL175, RL178) + this.X[8] + 1836072691, 13) + RL176;
        int RL180 = RL(RL175, 10);
        int RL181 = RL(RL176 + f3(RL179, RL177, RL180) + this.X[12] + 1836072691, 5) + RL178;
        int RL182 = RL(RL177, 10);
        int RL183 = RL(RL178 + f3(RL181, RL179, RL182) + this.X[2] + 1836072691, 14) + RL180;
        int RL184 = RL(RL179, 10);
        int RL185 = RL(RL180 + f3(RL183, RL181, RL184) + this.X[10] + 1836072691, 13) + RL182;
        int RL186 = RL(RL181, 10);
        int RL187 = RL(RL182 + f3(RL185, RL183, RL186) + this.X[0] + 1836072691, 13) + RL184;
        int RL188 = RL(RL183, 10);
        int RL189 = RL(RL184 + f3(RL187, RL185, RL188) + this.X[4] + 1836072691, 7) + RL186;
        int RL190 = RL(RL185, 10);
        int RL191 = RL(RL186 + f3(RL189, RL187, RL190) + this.X[13] + 1836072691, 5) + RL188;
        int RL192 = RL(RL187, 10);
        int RL193 = RL(((RL188 + f4(RL159, RL157, RL160)) + this.X[1]) - 1894007588, 11) + RL158;
        int RL194 = RL(RL157, 10);
        int RL195 = RL(((RL158 + f4(RL193, RL159, RL194)) + this.X[9]) - 1894007588, 12) + RL160;
        int RL196 = RL(RL159, 10);
        int RL197 = RL(((RL160 + f4(RL195, RL193, RL196)) + this.X[11]) - 1894007588, 14) + RL194;
        int RL198 = RL(RL193, 10);
        int RL199 = RL(((RL194 + f4(RL197, RL195, RL198)) + this.X[10]) - 1894007588, 15) + RL196;
        int RL200 = RL(RL195, 10);
        int RL201 = RL(((RL196 + f4(RL199, RL197, RL200)) + this.X[0]) - 1894007588, 14) + RL198;
        int RL202 = RL(RL197, 10);
        int RL203 = RL(((RL198 + f4(RL201, RL199, RL202)) + this.X[8]) - 1894007588, 15) + RL200;
        int RL204 = RL(RL199, 10);
        int RL205 = RL(((RL200 + f4(RL203, RL201, RL204)) + this.X[12]) - 1894007588, 9) + RL202;
        int RL206 = RL(RL201, 10);
        int RL207 = RL(((RL202 + f4(RL205, RL203, RL206)) + this.X[4]) - 1894007588, 8) + RL204;
        int RL208 = RL(RL203, 10);
        int RL209 = RL(((RL204 + f4(RL207, RL205, RL208)) + this.X[13]) - 1894007588, 9) + RL206;
        int RL210 = RL(RL205, 10);
        int RL211 = RL(((RL206 + f4(RL209, RL207, RL210)) + this.X[3]) - 1894007588, 14) + RL208;
        int RL212 = RL(RL207, 10);
        int RL213 = RL(((RL208 + f4(RL211, RL209, RL212)) + this.X[7]) - 1894007588, 5) + RL210;
        int RL214 = RL(RL209, 10);
        int RL215 = RL(((RL210 + f4(RL213, RL211, RL214)) + this.X[15]) - 1894007588, 6) + RL212;
        int RL216 = RL(RL211, 10);
        int RL217 = RL(((RL212 + f4(RL215, RL213, RL216)) + this.X[14]) - 1894007588, 8) + RL214;
        int RL218 = RL(RL213, 10);
        int RL219 = RL(((RL214 + f4(RL217, RL215, RL218)) + this.X[5]) - 1894007588, 6) + RL216;
        int RL220 = RL(RL215, 10);
        int RL221 = RL(((RL216 + f4(RL219, RL217, RL220)) + this.X[6]) - 1894007588, 5) + RL218;
        int RL222 = RL(RL217, 10);
        int RL223 = RL(((RL218 + f4(RL221, RL219, RL222)) + this.X[2]) - 1894007588, 12) + RL220;
        int RL224 = RL(RL219, 10);
        int RL225 = RL(RL156 + f2(RL191, RL189, RL192) + this.X[8] + 2053994217, 15) + RL190;
        int RL226 = RL(RL189, 10);
        int RL227 = RL(RL190 + f2(RL225, RL191, RL226) + this.X[6] + 2053994217, 5) + RL192;
        int RL228 = RL(RL191, 10);
        int RL229 = RL(RL192 + f2(RL227, RL225, RL228) + this.X[4] + 2053994217, 8) + RL226;
        int RL230 = RL(RL225, 10);
        int RL231 = RL(RL226 + f2(RL229, RL227, RL230) + this.X[1] + 2053994217, 11) + RL228;
        int RL232 = RL(RL227, 10);
        int RL233 = RL(RL228 + f2(RL231, RL229, RL232) + this.X[3] + 2053994217, 14) + RL230;
        int RL234 = RL(RL229, 10);
        int RL235 = RL(RL230 + f2(RL233, RL231, RL234) + this.X[11] + 2053994217, 14) + RL232;
        int RL236 = RL(RL231, 10);
        int RL237 = RL(RL232 + f2(RL235, RL233, RL236) + this.X[15] + 2053994217, 6) + RL234;
        int RL238 = RL(RL233, 10);
        int RL239 = RL(RL234 + f2(RL237, RL235, RL238) + this.X[0] + 2053994217, 14) + RL236;
        int RL240 = RL(RL235, 10);
        int RL241 = RL(RL236 + f2(RL239, RL237, RL240) + this.X[5] + 2053994217, 6) + RL238;
        int RL242 = RL(RL237, 10);
        int RL243 = RL(RL238 + f2(RL241, RL239, RL242) + this.X[12] + 2053994217, 9) + RL240;
        int RL244 = RL(RL239, 10);
        int RL245 = RL(RL240 + f2(RL243, RL241, RL244) + this.X[2] + 2053994217, 12) + RL242;
        int RL246 = RL(RL241, 10);
        int RL247 = RL(RL242 + f2(RL245, RL243, RL246) + this.X[13] + 2053994217, 9) + RL244;
        int RL248 = RL(RL243, 10);
        int RL249 = RL(RL244 + f2(RL247, RL245, RL248) + this.X[9] + 2053994217, 12) + RL246;
        int RL250 = RL(RL245, 10);
        int RL251 = RL(RL246 + f2(RL249, RL247, RL250) + this.X[7] + 2053994217, 5) + RL248;
        int RL252 = RL(RL247, 10);
        int RL253 = RL(RL248 + f2(RL251, RL249, RL252) + this.X[10] + 2053994217, 15) + RL250;
        int RL254 = RL(RL249, 10);
        int RL255 = RL(RL250 + f2(RL253, RL251, RL254) + this.X[14] + 2053994217, 8) + RL252;
        int RL256 = RL(RL251, 10);
        int RL257 = RL(((RL220 + f5(RL223, RL253, RL224)) + this.X[4]) - 1454113458, 9) + RL222;
        int RL258 = RL(RL253, 10);
        int RL259 = RL(((RL222 + f5(RL257, RL223, RL258)) + this.X[0]) - 1454113458, 15) + RL224;
        int RL260 = RL(RL223, 10);
        int RL261 = RL(((RL224 + f5(RL259, RL257, RL260)) + this.X[5]) - 1454113458, 5) + RL258;
        int RL262 = RL(RL257, 10);
        int RL263 = RL(((RL258 + f5(RL261, RL259, RL262)) + this.X[9]) - 1454113458, 11) + RL260;
        int RL264 = RL(RL259, 10);
        int RL265 = RL(((RL260 + f5(RL263, RL261, RL264)) + this.X[7]) - 1454113458, 6) + RL262;
        int RL266 = RL(RL261, 10);
        int RL267 = RL(((RL262 + f5(RL265, RL263, RL266)) + this.X[12]) - 1454113458, 8) + RL264;
        int RL268 = RL(RL263, 10);
        int RL269 = RL(((RL264 + f5(RL267, RL265, RL268)) + this.X[2]) - 1454113458, 13) + RL266;
        int RL270 = RL(RL265, 10);
        int RL271 = RL(((RL266 + f5(RL269, RL267, RL270)) + this.X[10]) - 1454113458, 12) + RL268;
        int RL272 = RL(RL267, 10);
        int RL273 = RL(((RL268 + f5(RL271, RL269, RL272)) + this.X[14]) - 1454113458, 5) + RL270;
        int RL274 = RL(RL269, 10);
        int RL275 = RL(((RL270 + f5(RL273, RL271, RL274)) + this.X[1]) - 1454113458, 12) + RL272;
        int RL276 = RL(RL271, 10);
        int RL277 = RL(((RL272 + f5(RL275, RL273, RL276)) + this.X[3]) - 1454113458, 13) + RL274;
        int RL278 = RL(RL273, 10);
        int RL279 = RL(((RL274 + f5(RL277, RL275, RL278)) + this.X[8]) - 1454113458, 14) + RL276;
        int RL280 = RL(RL275, 10);
        int RL281 = RL(((RL276 + f5(RL279, RL277, RL280)) + this.X[11]) - 1454113458, 11) + RL278;
        int RL282 = RL(RL277, 10);
        int RL283 = RL(((RL278 + f5(RL281, RL279, RL282)) + this.X[6]) - 1454113458, 8) + RL280;
        int RL284 = RL(RL279, 10);
        int RL285 = RL(((RL280 + f5(RL283, RL281, RL284)) + this.X[15]) - 1454113458, 5) + RL282;
        int RL286 = RL(RL281, 10);
        int RL287 = RL(((RL282 + f5(RL285, RL283, RL286)) + this.X[13]) - 1454113458, 6) + RL284;
        int RL288 = RL(RL283, 10);
        int RL289 = RL(RL252 + f1(RL255, RL221, RL256) + this.X[12], 8) + RL254;
        int RL290 = RL(RL221, 10);
        int RL291 = RL(RL254 + f1(RL289, RL255, RL290) + this.X[15], 5) + RL256;
        int RL292 = RL(RL255, 10);
        int RL293 = RL(RL256 + f1(RL291, RL289, RL292) + this.X[10], 12) + RL290;
        int RL294 = RL(RL289, 10);
        int RL295 = RL(RL290 + f1(RL293, RL291, RL294) + this.X[4], 9) + RL292;
        int RL296 = RL(RL291, 10);
        int RL297 = RL(RL292 + f1(RL295, RL293, RL296) + this.X[1], 12) + RL294;
        int RL298 = RL(RL293, 10);
        int RL299 = RL(RL294 + f1(RL297, RL295, RL298) + this.X[5], 5) + RL296;
        int RL300 = RL(RL295, 10);
        int RL301 = RL(RL296 + f1(RL299, RL297, RL300) + this.X[8], 14) + RL298;
        int RL302 = RL(RL297, 10);
        int RL303 = RL(RL298 + f1(RL301, RL299, RL302) + this.X[7], 6) + RL300;
        int RL304 = RL(RL299, 10);
        int RL305 = RL(RL300 + f1(RL303, RL301, RL304) + this.X[6], 8) + RL302;
        int RL306 = RL(RL301, 10);
        int RL307 = RL(RL302 + f1(RL305, RL303, RL306) + this.X[2], 13) + RL304;
        int RL308 = RL(RL303, 10);
        int RL309 = RL(RL304 + f1(RL307, RL305, RL308) + this.X[13], 6) + RL306;
        int RL310 = RL(RL305, 10);
        int RL311 = RL(RL306 + f1(RL309, RL307, RL310) + this.X[14], 5) + RL308;
        int RL312 = RL(RL307, 10);
        int RL313 = RL(RL308 + f1(RL311, RL309, RL312) + this.X[0], 15) + RL310;
        int RL314 = RL(RL309, 10);
        int RL315 = RL(RL310 + f1(RL313, RL311, RL314) + this.X[3], 13) + RL312;
        int RL316 = RL(RL311, 10);
        int RL317 = RL(RL312 + f1(RL315, RL313, RL316) + this.X[9], 11) + RL314;
        int RL318 = RL(RL313, 10);
        int RL319 = RL(RL314 + f1(RL317, RL315, RL318) + this.X[11], 11) + RL316;
        int RL320 = RL(RL315, 10);
        this.H0 += RL284;
        this.H1 += RL287;
        this.H2 += RL285;
        this.H3 += RL288;
        this.H4 += RL318;
        this.H5 += RL316;
        this.H6 += RL319;
        this.H7 += RL317;
        this.H8 += RL320;
        this.H9 += RL286;
        int i11 = 0;
        this.xOff = 0;
        while (true) {
            int[] iArr = this.X;
            if (i11 != iArr.length) {
                iArr[i11] = 0;
                i11++;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.crypto.digests.GeneralDigest
    public void processLength(long j) {
        if (this.xOff > 14) {
            processBlock();
        }
        int[] iArr = this.X;
        iArr[14] = (int) (-1 & j);
        iArr[15] = (int) (j >>> 32);
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.crypto.digests.GeneralDigest
    public void processWord(byte[] bArr, int i) {
        int[] iArr = this.X;
        int i2 = this.xOff;
        this.xOff = i2 + 1;
        iArr[i2] = ((bArr[i + 3] & 255) << 24) | (bArr[i] & 255) | ((bArr[i + 1] & 255) << 8) | ((bArr[i + 2] & 255) << 16);
        if (this.xOff == 16) {
            processBlock();
        }
    }

    @Override // org.bouncycastle.crypto.digests.GeneralDigest, org.bouncycastle.crypto.Digest
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
        int i = 0;
        while (true) {
            int[] iArr = this.X;
            if (i != iArr.length) {
                iArr[i] = 0;
                i++;
            } else {
                return;
            }
        }
    }

    @Override // org.bouncycastle.util.Memoable
    public void reset(Memoable memoable) {
        doCopy((RIPEMD320Digest) memoable);
    }
}
