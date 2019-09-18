package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.TweakableBlockCipherParameters;

public class ThreefishEngine implements BlockCipher {
    public static final int BLOCKSIZE_1024 = 1024;
    public static final int BLOCKSIZE_256 = 256;
    public static final int BLOCKSIZE_512 = 512;
    private static final long C_240 = 2004413935125273122L;
    private static final int MAX_ROUNDS = 80;
    /* access modifiers changed from: private */
    public static int[] MOD17 = new int[MOD9.length];
    /* access modifiers changed from: private */
    public static int[] MOD3 = new int[MOD9.length];
    /* access modifiers changed from: private */
    public static int[] MOD5 = new int[MOD9.length];
    /* access modifiers changed from: private */
    public static int[] MOD9 = new int[80];
    private static final int ROUNDS_1024 = 80;
    private static final int ROUNDS_256 = 72;
    private static final int ROUNDS_512 = 72;
    private static final int TWEAK_SIZE_BYTES = 16;
    private static final int TWEAK_SIZE_WORDS = 2;
    private int blocksizeBytes;
    private int blocksizeWords;
    private ThreefishCipher cipher;
    private long[] currentBlock;
    private boolean forEncryption;
    private long[] kw;
    private long[] t = new long[5];

    private static final class Threefish1024Cipher extends ThreefishCipher {
        private static final int ROTATION_0_0 = 24;
        private static final int ROTATION_0_1 = 13;
        private static final int ROTATION_0_2 = 8;
        private static final int ROTATION_0_3 = 47;
        private static final int ROTATION_0_4 = 8;
        private static final int ROTATION_0_5 = 17;
        private static final int ROTATION_0_6 = 22;
        private static final int ROTATION_0_7 = 37;
        private static final int ROTATION_1_0 = 38;
        private static final int ROTATION_1_1 = 19;
        private static final int ROTATION_1_2 = 10;
        private static final int ROTATION_1_3 = 55;
        private static final int ROTATION_1_4 = 49;
        private static final int ROTATION_1_5 = 18;
        private static final int ROTATION_1_6 = 23;
        private static final int ROTATION_1_7 = 52;
        private static final int ROTATION_2_0 = 33;
        private static final int ROTATION_2_1 = 4;
        private static final int ROTATION_2_2 = 51;
        private static final int ROTATION_2_3 = 13;
        private static final int ROTATION_2_4 = 34;
        private static final int ROTATION_2_5 = 41;
        private static final int ROTATION_2_6 = 59;
        private static final int ROTATION_2_7 = 17;
        private static final int ROTATION_3_0 = 5;
        private static final int ROTATION_3_1 = 20;
        private static final int ROTATION_3_2 = 48;
        private static final int ROTATION_3_3 = 41;
        private static final int ROTATION_3_4 = 47;
        private static final int ROTATION_3_5 = 28;
        private static final int ROTATION_3_6 = 16;
        private static final int ROTATION_3_7 = 25;
        private static final int ROTATION_4_0 = 41;
        private static final int ROTATION_4_1 = 9;
        private static final int ROTATION_4_2 = 37;
        private static final int ROTATION_4_3 = 31;
        private static final int ROTATION_4_4 = 12;
        private static final int ROTATION_4_5 = 47;
        private static final int ROTATION_4_6 = 44;
        private static final int ROTATION_4_7 = 30;
        private static final int ROTATION_5_0 = 16;
        private static final int ROTATION_5_1 = 34;
        private static final int ROTATION_5_2 = 56;
        private static final int ROTATION_5_3 = 51;
        private static final int ROTATION_5_4 = 4;
        private static final int ROTATION_5_5 = 53;
        private static final int ROTATION_5_6 = 42;
        private static final int ROTATION_5_7 = 41;
        private static final int ROTATION_6_0 = 31;
        private static final int ROTATION_6_1 = 44;
        private static final int ROTATION_6_2 = 47;
        private static final int ROTATION_6_3 = 46;
        private static final int ROTATION_6_4 = 19;
        private static final int ROTATION_6_5 = 42;
        private static final int ROTATION_6_6 = 44;
        private static final int ROTATION_6_7 = 25;
        private static final int ROTATION_7_0 = 9;
        private static final int ROTATION_7_1 = 48;
        private static final int ROTATION_7_2 = 35;
        private static final int ROTATION_7_3 = 52;
        private static final int ROTATION_7_4 = 23;
        private static final int ROTATION_7_5 = 31;
        private static final int ROTATION_7_6 = 37;
        private static final int ROTATION_7_7 = 20;

        public Threefish1024Cipher(long[] jArr, long[] jArr2) {
            super(jArr, jArr2);
        }

        /* JADX WARNING: type inference failed for: r230v0, types: [long[]] */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Unknown variable types count: 1 */
        public void decryptBlock(long[] jArr, long[] r230) {
            long[] jArr2 = this.kw;
            long[] jArr3 = this.t;
            int[] access$300 = ThreefishEngine.MOD17;
            int[] access$100 = ThreefishEngine.MOD3;
            if (jArr2.length != 33) {
                throw new IllegalArgumentException();
            } else if (jArr3.length == 5) {
                long j = jArr[0];
                long j2 = jArr[1];
                long j3 = jArr[2];
                long j4 = jArr[3];
                long j5 = jArr[4];
                long j6 = jArr[5];
                long j7 = jArr[6];
                long j8 = jArr[7];
                long j9 = jArr[8];
                long j10 = jArr[9];
                long j11 = jArr[10];
                long j12 = jArr[11];
                long j13 = jArr[12];
                long j14 = jArr[13];
                long j15 = jArr[14];
                long j16 = jArr[15];
                int i = 19;
                for (int i2 = 1; i >= i2; i2 = 1) {
                    int i3 = access$300[i];
                    int i4 = access$100[i];
                    int i5 = i3 + 1;
                    long j17 = j - jArr2[i5];
                    int i6 = i3 + 2;
                    long j18 = j2 - jArr2[i6];
                    int i7 = i3 + 3;
                    long j19 = j3 - jArr2[i7];
                    int i8 = i3 + 4;
                    long j20 = j4 - jArr2[i8];
                    int i9 = i3 + 5;
                    long j21 = j19;
                    long j22 = j5 - jArr2[i9];
                    int i10 = i3 + 6;
                    long j23 = j6 - jArr2[i10];
                    int i11 = i3 + 7;
                    int[] iArr = access$300;
                    int[] iArr2 = access$100;
                    long j24 = j7 - jArr2[i11];
                    int i12 = i3 + 8;
                    long j25 = j20;
                    int i13 = i3 + 9;
                    long j26 = j8 - jArr2[i12];
                    long j27 = j9 - jArr2[i13];
                    int i14 = i3 + 10;
                    long j28 = j23;
                    long j29 = j10 - jArr2[i14];
                    int i15 = i3 + 11;
                    long j30 = j27;
                    int i16 = i3 + 12;
                    long j31 = j11 - jArr2[i15];
                    long j32 = j12 - jArr2[i16];
                    int i17 = i3 + 13;
                    long j33 = j29;
                    int i18 = i3 + 14;
                    int i19 = i4 + 1;
                    long j34 = j13 - jArr2[i17];
                    long j35 = j14 - (jArr2[i18] + jArr3[i19]);
                    int i20 = i3 + 15;
                    int i21 = i9;
                    long j36 = j15 - (jArr2[i20] + jArr3[i4 + 2]);
                    long j37 = (long) i;
                    int i22 = i;
                    long j38 = j37;
                    long xorRotr = ThreefishEngine.xorRotr(j16 - ((jArr2[i3 + 16] + j37) + 1), 9, j17);
                    long j39 = j17 - xorRotr;
                    long[] jArr4 = jArr3;
                    long j40 = j21;
                    long xorRotr2 = ThreefishEngine.xorRotr(j32, 48, j40);
                    long j41 = j40 - xorRotr2;
                    long xorRotr3 = ThreefishEngine.xorRotr(j35, 35, j24);
                    long j42 = j24 - xorRotr3;
                    long j43 = xorRotr2;
                    long j44 = j22;
                    long xorRotr4 = ThreefishEngine.xorRotr(j33, 52, j44);
                    long j45 = j44 - xorRotr4;
                    long j46 = xorRotr4;
                    long xorRotr5 = ThreefishEngine.xorRotr(j18, 23, j36);
                    long j47 = xorRotr3;
                    long j48 = j36 - xorRotr5;
                    long j49 = j30;
                    long xorRotr6 = ThreefishEngine.xorRotr(j28, 31, j49);
                    long[] jArr5 = jArr2;
                    long j50 = j42;
                    long j51 = j49 - xorRotr6;
                    long j52 = j31;
                    long xorRotr7 = ThreefishEngine.xorRotr(j25, 37, j52);
                    long j53 = xorRotr5;
                    long j54 = j34;
                    long xorRotr8 = ThreefishEngine.xorRotr(j26, 20, j54);
                    long j55 = j54 - xorRotr8;
                    long xorRotr9 = ThreefishEngine.xorRotr(xorRotr8, 31, j39);
                    long j56 = j39 - xorRotr9;
                    long xorRotr10 = ThreefishEngine.xorRotr(xorRotr6, 44, j41);
                    long j57 = j41 - xorRotr10;
                    long xorRotr11 = ThreefishEngine.xorRotr(xorRotr7, 47, j45);
                    long j58 = j45 - xorRotr11;
                    long j59 = xorRotr10;
                    long j60 = xorRotr11;
                    long j61 = j50;
                    long xorRotr12 = ThreefishEngine.xorRotr(j53, 46, j61);
                    long j62 = j61 - xorRotr12;
                    long j63 = xorRotr12;
                    long xorRotr13 = ThreefishEngine.xorRotr(xorRotr, 19, j55);
                    long j64 = xorRotr9;
                    long j65 = j55 - xorRotr13;
                    long j66 = j48;
                    long xorRotr14 = ThreefishEngine.xorRotr(j47, 42, j66);
                    long j67 = j58;
                    long j68 = j66 - xorRotr14;
                    long j69 = j51;
                    long xorRotr15 = ThreefishEngine.xorRotr(j43, 44, j69);
                    long j70 = xorRotr13;
                    long j71 = j69 - xorRotr15;
                    long j72 = j52 - xorRotr7;
                    long xorRotr16 = ThreefishEngine.xorRotr(j46, 25, j72);
                    long j73 = j72 - xorRotr16;
                    long xorRotr17 = ThreefishEngine.xorRotr(xorRotr16, 16, j56);
                    long j74 = j56 - xorRotr17;
                    long xorRotr18 = ThreefishEngine.xorRotr(xorRotr14, 34, j57);
                    long j75 = j57 - xorRotr18;
                    long xorRotr19 = ThreefishEngine.xorRotr(xorRotr15, 56, j62);
                    long j76 = j62 - xorRotr19;
                    long j77 = xorRotr19;
                    long j78 = xorRotr18;
                    long j79 = j67;
                    long xorRotr20 = ThreefishEngine.xorRotr(j70, 51, j79);
                    long j80 = j79 - xorRotr20;
                    long j81 = xorRotr20;
                    long xorRotr21 = ThreefishEngine.xorRotr(j64, 4, j73);
                    long j82 = xorRotr17;
                    long j83 = j73 - xorRotr21;
                    long j84 = j65;
                    long xorRotr22 = ThreefishEngine.xorRotr(j60, 53, j84);
                    long j85 = j76;
                    long j86 = j84 - xorRotr22;
                    long j87 = j68;
                    long xorRotr23 = ThreefishEngine.xorRotr(j59, 42, j87);
                    long j88 = j87 - xorRotr23;
                    long j89 = j71;
                    long xorRotr24 = ThreefishEngine.xorRotr(j63, 41, j89);
                    long j90 = j89 - xorRotr24;
                    long xorRotr25 = ThreefishEngine.xorRotr(xorRotr24, 41, j74);
                    long xorRotr26 = ThreefishEngine.xorRotr(xorRotr22, 9, j75);
                    long xorRotr27 = ThreefishEngine.xorRotr(xorRotr23, 37, j80);
                    long j91 = j80 - xorRotr27;
                    long j92 = xorRotr27;
                    long j93 = j85;
                    long xorRotr28 = ThreefishEngine.xorRotr(xorRotr21, 31, j93);
                    long j94 = j93 - xorRotr28;
                    long j95 = xorRotr28;
                    long xorRotr29 = ThreefishEngine.xorRotr(j82, 12, j90);
                    long j96 = j90 - xorRotr29;
                    long j97 = xorRotr29;
                    long j98 = j83;
                    long xorRotr30 = ThreefishEngine.xorRotr(j77, 47, j98);
                    long j99 = xorRotr30;
                    long j100 = j98 - xorRotr30;
                    long j101 = j86;
                    long xorRotr31 = ThreefishEngine.xorRotr(j78, 44, j101);
                    long j102 = xorRotr31;
                    long j103 = j101 - xorRotr31;
                    long j104 = j88;
                    long xorRotr32 = ThreefishEngine.xorRotr(j81, 30, j104);
                    long j105 = (j74 - xorRotr25) - jArr5[i3];
                    long j106 = xorRotr25 - jArr5[i5];
                    long j107 = (j75 - xorRotr26) - jArr5[i6];
                    long j108 = xorRotr26 - jArr5[i7];
                    long j109 = j91 - jArr5[i8];
                    long j110 = j108;
                    long j111 = j92 - jArr5[i21];
                    long j112 = j94 - jArr5[i10];
                    long j113 = j111;
                    long j114 = j95 - jArr5[i11];
                    long j115 = j96 - jArr5[i12];
                    long j116 = j97 - jArr5[i13];
                    long j117 = j106;
                    long j118 = j100 - jArr5[i14];
                    long j119 = j99 - jArr5[i15];
                    long j120 = j116;
                    long j121 = j103 - jArr5[i16];
                    long j122 = j102 - (jArr5[i17] + jArr4[i4]);
                    long j123 = (j104 - xorRotr32) - (jArr5[i18] + jArr4[i19]);
                    long xorRotr33 = ThreefishEngine.xorRotr(xorRotr32 - (jArr5[i20] + j38), 5, j105);
                    long j124 = j105 - xorRotr33;
                    long xorRotr34 = ThreefishEngine.xorRotr(j119, 20, j107);
                    long j125 = j107 - xorRotr34;
                    long xorRotr35 = ThreefishEngine.xorRotr(j122, 48, j112);
                    long j126 = j112 - xorRotr35;
                    long j127 = xorRotr34;
                    long xorRotr36 = ThreefishEngine.xorRotr(j120, 41, j109);
                    long j128 = j109 - xorRotr36;
                    long j129 = xorRotr36;
                    long j130 = xorRotr35;
                    long j131 = j123;
                    long xorRotr37 = ThreefishEngine.xorRotr(j117, 47, j131);
                    long j132 = xorRotr33;
                    long j133 = j131 - xorRotr37;
                    long j134 = j115;
                    long xorRotr38 = ThreefishEngine.xorRotr(j113, 28, j134);
                    long j135 = xorRotr37;
                    long j136 = j134 - xorRotr38;
                    long j137 = j118;
                    long xorRotr39 = ThreefishEngine.xorRotr(j110, 16, j137);
                    long j138 = j137 - xorRotr39;
                    long j139 = j121;
                    long xorRotr40 = ThreefishEngine.xorRotr(j114, 25, j139);
                    long j140 = j139 - xorRotr40;
                    long xorRotr41 = ThreefishEngine.xorRotr(xorRotr40, 33, j124);
                    long j141 = j124 - xorRotr41;
                    long xorRotr42 = ThreefishEngine.xorRotr(xorRotr38, 4, j125);
                    long j142 = j125 - xorRotr42;
                    long xorRotr43 = ThreefishEngine.xorRotr(xorRotr39, 51, j128);
                    long j143 = j128 - xorRotr43;
                    long j144 = xorRotr43;
                    long j145 = xorRotr42;
                    long j146 = j126;
                    long xorRotr44 = ThreefishEngine.xorRotr(j135, 13, j146);
                    long j147 = j146 - xorRotr44;
                    long j148 = xorRotr44;
                    long xorRotr45 = ThreefishEngine.xorRotr(j132, 34, j140);
                    long j149 = xorRotr41;
                    long j150 = j140 - xorRotr45;
                    long j151 = j133;
                    long xorRotr46 = ThreefishEngine.xorRotr(j130, 41, j151);
                    long j152 = xorRotr45;
                    long j153 = j151 - xorRotr46;
                    long j154 = j136;
                    long xorRotr47 = ThreefishEngine.xorRotr(j127, 59, j154);
                    long j155 = j154 - xorRotr47;
                    long j156 = j143;
                    long j157 = j138;
                    long xorRotr48 = ThreefishEngine.xorRotr(j129, 17, j157);
                    long j158 = j157 - xorRotr48;
                    long xorRotr49 = ThreefishEngine.xorRotr(xorRotr48, 38, j141);
                    long j159 = j141 - xorRotr49;
                    long xorRotr50 = ThreefishEngine.xorRotr(xorRotr46, 19, j142);
                    long j160 = j142 - xorRotr50;
                    long xorRotr51 = ThreefishEngine.xorRotr(xorRotr47, 10, j147);
                    long j161 = j147 - xorRotr51;
                    long j162 = xorRotr50;
                    long j163 = xorRotr51;
                    long j164 = j156;
                    long xorRotr52 = ThreefishEngine.xorRotr(j152, 55, j164);
                    long j165 = j164 - xorRotr52;
                    long j166 = xorRotr52;
                    long xorRotr53 = ThreefishEngine.xorRotr(j149, 49, j158);
                    long j167 = xorRotr49;
                    long j168 = j158 - xorRotr53;
                    long j169 = j150;
                    long xorRotr54 = ThreefishEngine.xorRotr(j144, 18, j169);
                    long j170 = j169 - xorRotr54;
                    long j171 = xorRotr53;
                    long j172 = j153;
                    long xorRotr55 = ThreefishEngine.xorRotr(j145, 23, j172);
                    long j173 = j161;
                    long j174 = j172 - xorRotr55;
                    long j175 = j155;
                    long xorRotr56 = ThreefishEngine.xorRotr(j148, 52, j175);
                    long j176 = j175 - xorRotr56;
                    long xorRotr57 = ThreefishEngine.xorRotr(xorRotr56, 24, j159);
                    j = j159 - xorRotr57;
                    j4 = ThreefishEngine.xorRotr(xorRotr54, 13, j160);
                    j3 = j160 - j4;
                    j6 = ThreefishEngine.xorRotr(xorRotr55, 8, j165);
                    j5 = j165 - j6;
                    long j177 = j173;
                    j8 = ThreefishEngine.xorRotr(j171, 47, j177);
                    j7 = j177 - j8;
                    j10 = ThreefishEngine.xorRotr(j167, 8, j176);
                    j9 = j176 - j10;
                    long j178 = j168;
                    j12 = ThreefishEngine.xorRotr(j163, 17, j178);
                    j11 = j178 - j12;
                    long j179 = j170;
                    j14 = ThreefishEngine.xorRotr(j162, 22, j179);
                    j13 = j179 - j14;
                    long j180 = j174;
                    j16 = ThreefishEngine.xorRotr(j166, 37, j180);
                    j15 = j180 - j16;
                    i = i22 - 2;
                    j2 = xorRotr57;
                    access$300 = iArr;
                    access$100 = iArr2;
                    jArr3 = jArr4;
                    jArr2 = jArr5;
                }
                long[] jArr6 = jArr3;
                long[] jArr7 = jArr2;
                long j181 = j - jArr7[0];
                long j182 = j2 - jArr7[1];
                long j183 = j3 - jArr7[2];
                long j184 = j4 - jArr7[3];
                long j185 = j5 - jArr7[4];
                long j186 = j6 - jArr7[5];
                long j187 = j7 - jArr7[6];
                long j188 = j8 - jArr7[7];
                long j189 = j9 - jArr7[8];
                long j190 = j10 - jArr7[9];
                long j191 = j11 - jArr7[10];
                long j192 = j13 - jArr7[12];
                long j193 = j14 - (jArr7[13] + jArr6[0]);
                long j194 = j15 - (jArr7[14] + jArr6[1]);
                r230[0] = j181;
                r230[1] = j182;
                r230[2] = j183;
                r230[3] = j184;
                r230[4] = j185;
                r230[5] = j186;
                r230[6] = j187;
                r230[7] = j188;
                r230[8] = j189;
                r230[9] = j190;
                r230[10] = j191;
                r230[11] = j12 - jArr7[11];
                r230[12] = j192;
                r230[13] = j193;
                r230[14] = j194;
                r230[15] = j16 - jArr7[15];
            } else {
                throw new IllegalArgumentException();
            }
        }

        /* JADX WARNING: type inference failed for: r234v0, types: [long[]] */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Unknown variable types count: 1 */
        public void encryptBlock(long[] jArr, long[] r234) {
            long[] jArr2 = this.kw;
            long[] jArr3 = this.t;
            int[] access$300 = ThreefishEngine.MOD17;
            int[] access$100 = ThreefishEngine.MOD3;
            if (jArr2.length != 33) {
                throw new IllegalArgumentException();
            } else if (jArr3.length == 5) {
                boolean z = false;
                long j = jArr[0];
                long j2 = jArr[1];
                long j3 = jArr[2];
                long j4 = jArr[3];
                long j5 = jArr[4];
                long j6 = jArr[5];
                long j7 = jArr[6];
                long j8 = jArr[7];
                long j9 = jArr[8];
                long j10 = jArr[9];
                long j11 = jArr[10];
                long j12 = jArr[11];
                long j13 = jArr[12];
                int i = 13;
                long j14 = jArr[13];
                long j15 = jArr[14];
                long j16 = jArr[15];
                long j17 = j + jArr2[0];
                long j18 = j2 + jArr2[1];
                long j19 = j3 + jArr2[2];
                long j20 = j4 + jArr2[3];
                long j21 = j5 + jArr2[4];
                long j22 = j6 + jArr2[5];
                long j23 = j7 + jArr2[6];
                long j24 = j8 + jArr2[7];
                long j25 = j9 + jArr2[8];
                long j26 = j10 + jArr2[9];
                long j27 = j11 + jArr2[10];
                long j28 = j12 + jArr2[11];
                long j29 = j13 + jArr2[12];
                long j30 = j14 + jArr2[13] + jArr3[0];
                long j31 = j15 + jArr2[14] + jArr3[1];
                int i2 = 1;
                long j32 = j22;
                long j33 = j24;
                long j34 = j26;
                long j35 = j28;
                long j36 = j30;
                long j37 = j16 + jArr2[15];
                long j38 = j19;
                long j39 = j20;
                long j40 = j38;
                while (i2 < 20) {
                    int i3 = access$300[i2];
                    int i4 = access$100[i2];
                    long j41 = j17 + j18;
                    long rotlXor = ThreefishEngine.rotlXor(j18, 24, j41);
                    int[] iArr = access$300;
                    long j42 = j40 + j39;
                    long j43 = j41;
                    long rotlXor2 = ThreefishEngine.rotlXor(j39, i, j42);
                    long j44 = j32;
                    long j45 = j21 + j44;
                    long rotlXor3 = ThreefishEngine.rotlXor(j44, 8, j45);
                    int i5 = i2;
                    long j46 = j33;
                    long j47 = j23 + j46;
                    long rotlXor4 = ThreefishEngine.rotlXor(j46, 47, j47);
                    int i6 = i3;
                    long j48 = rotlXor;
                    long j49 = j34;
                    long j50 = j25 + j49;
                    long rotlXor5 = ThreefishEngine.rotlXor(j49, 8, j50);
                    long j51 = j50;
                    long j52 = rotlXor3;
                    long j53 = j35;
                    long j54 = j27 + j53;
                    long rotlXor6 = ThreefishEngine.rotlXor(j53, 17, j54);
                    long j55 = rotlXor4;
                    long j56 = j54;
                    long j57 = j36;
                    long j58 = j29 + j57;
                    long rotlXor7 = ThreefishEngine.rotlXor(j57, 22, j58);
                    long[] jArr4 = jArr3;
                    long j59 = j58;
                    long j60 = j37;
                    long j61 = j31 + j60;
                    long rotlXor8 = ThreefishEngine.rotlXor(j60, 37, j61);
                    long j62 = j43 + rotlXor5;
                    long rotlXor9 = ThreefishEngine.rotlXor(rotlXor5, 38, j62);
                    long j63 = j42 + rotlXor7;
                    long rotlXor10 = ThreefishEngine.rotlXor(rotlXor7, 19, j63);
                    long[] jArr5 = jArr2;
                    long j64 = j47 + rotlXor6;
                    long j65 = rotlXor9;
                    long rotlXor11 = ThreefishEngine.rotlXor(rotlXor6, 10, j64);
                    long j66 = j45 + rotlXor8;
                    long j67 = rotlXor10;
                    long j68 = j56 + j55;
                    long rotlXor12 = ThreefishEngine.rotlXor(rotlXor8, 55, j66);
                    long rotlXor13 = ThreefishEngine.rotlXor(j55, 49, j68);
                    long j69 = j68;
                    long j70 = j59 + rotlXor2;
                    long j71 = j64;
                    long rotlXor14 = ThreefishEngine.rotlXor(rotlXor2, 18, j70);
                    long j72 = j61 + j52;
                    long j73 = j70;
                    long rotlXor15 = ThreefishEngine.rotlXor(j52, 23, j72);
                    long j74 = j72;
                    long j75 = j51 + j48;
                    long j76 = rotlXor14;
                    long rotlXor16 = ThreefishEngine.rotlXor(j48, 52, j75);
                    long j77 = j62 + rotlXor13;
                    long rotlXor17 = ThreefishEngine.rotlXor(rotlXor13, 33, j77);
                    long j78 = j63 + rotlXor15;
                    long rotlXor18 = ThreefishEngine.rotlXor(rotlXor15, 4, j78);
                    long j79 = j66 + j76;
                    long j80 = rotlXor17;
                    long rotlXor19 = ThreefishEngine.rotlXor(j76, 51, j79);
                    long j81 = j71 + rotlXor16;
                    long j82 = rotlXor18;
                    long j83 = j73 + rotlXor12;
                    long rotlXor20 = ThreefishEngine.rotlXor(rotlXor16, 13, j81);
                    long rotlXor21 = ThreefishEngine.rotlXor(rotlXor12, 34, j83);
                    long j84 = j83;
                    long j85 = j74 + j67;
                    long rotlXor22 = ThreefishEngine.rotlXor(j67, 41, j85);
                    long j86 = j75 + rotlXor11;
                    long j87 = j85;
                    long rotlXor23 = ThreefishEngine.rotlXor(rotlXor11, 59, j86);
                    long j88 = j86;
                    long j89 = j69 + j65;
                    long j90 = rotlXor22;
                    long rotlXor24 = ThreefishEngine.rotlXor(j65, 17, j89);
                    long j91 = j77 + rotlXor21;
                    long rotlXor25 = ThreefishEngine.rotlXor(rotlXor21, 5, j91);
                    long j92 = j78 + rotlXor23;
                    long rotlXor26 = ThreefishEngine.rotlXor(rotlXor23, 20, j92);
                    long j93 = j81 + j90;
                    long j94 = rotlXor25;
                    long rotlXor27 = ThreefishEngine.rotlXor(j90, 48, j93);
                    long j95 = j79 + rotlXor24;
                    long j96 = rotlXor26;
                    long j97 = j87 + rotlXor20;
                    long rotlXor28 = ThreefishEngine.rotlXor(rotlXor24, 41, j95);
                    long rotlXor29 = ThreefishEngine.rotlXor(rotlXor20, 47, j97);
                    long j98 = j97;
                    long j99 = j88 + j82;
                    long rotlXor30 = ThreefishEngine.rotlXor(j82, 28, j99);
                    long j100 = j89 + rotlXor19;
                    long j101 = j99;
                    long rotlXor31 = ThreefishEngine.rotlXor(rotlXor19, 16, j100);
                    long j102 = j100;
                    long j103 = j84 + j80;
                    long j104 = rotlXor30;
                    long rotlXor32 = ThreefishEngine.rotlXor(j80, 25, j103);
                    long j105 = j91 + jArr5[i6];
                    int i7 = i6 + 1;
                    long j106 = rotlXor29 + jArr5[i7];
                    int i8 = i6 + 2;
                    long j107 = j92 + jArr5[i8];
                    int i9 = i6 + 3;
                    long j108 = rotlXor31 + jArr5[i9];
                    int i10 = i6 + 4;
                    long j109 = j95 + jArr5[i10];
                    int i11 = i6 + 5;
                    int i12 = i7;
                    int i13 = i8;
                    long j110 = j104 + jArr5[i11];
                    int i14 = i6 + 6;
                    int i15 = i6 + 7;
                    int i16 = i6 + 8;
                    int i17 = i6 + 9;
                    long j111 = rotlXor32 + jArr5[i15];
                    int i18 = i6 + 10;
                    int i19 = i6 + 11;
                    long j112 = rotlXor28 + jArr5[i17];
                    long j113 = j96 + jArr5[i19];
                    int i20 = i6 + 12;
                    int i21 = i6 + 13;
                    long j114 = j103 + jArr5[i20];
                    int i22 = i6 + 14;
                    int i23 = i4 + 1;
                    int i24 = i6 + 15;
                    long j115 = rotlXor27 + jArr5[i21] + jArr4[i4];
                    long j116 = j113;
                    int i25 = i5;
                    long j117 = (long) i25;
                    int i26 = i25;
                    long j118 = j94 + jArr5[i24] + j117;
                    long j119 = j105 + j106;
                    long rotlXor33 = ThreefishEngine.rotlXor(j106, 41, j119);
                    long j120 = j107 + j108;
                    long rotlXor34 = ThreefishEngine.rotlXor(j108, 9, j120);
                    long j121 = j109 + j110;
                    long j122 = rotlXor33;
                    long j123 = j93 + jArr5[i14] + j111;
                    long rotlXor35 = ThreefishEngine.rotlXor(j110, 37, j121);
                    long j124 = j101 + jArr5[i16] + j112;
                    long j125 = rotlXor34;
                    long rotlXor36 = ThreefishEngine.rotlXor(j111, 31, j123);
                    long rotlXor37 = ThreefishEngine.rotlXor(j112, 12, j124);
                    long j126 = j102 + jArr5[i18] + j116;
                    long j127 = j121;
                    long rotlXor38 = ThreefishEngine.rotlXor(j116, 47, j126);
                    long j128 = j126;
                    long j129 = j114 + j115;
                    long j130 = rotlXor38;
                    long rotlXor39 = ThreefishEngine.rotlXor(j115, 44, j129);
                    long j131 = j129;
                    long j132 = j98 + jArr5[i22] + jArr4[i23] + j118;
                    long rotlXor40 = ThreefishEngine.rotlXor(j118, 30, j132);
                    long j133 = j119 + rotlXor37;
                    long rotlXor41 = ThreefishEngine.rotlXor(rotlXor37, 16, j133);
                    long j134 = j120 + rotlXor39;
                    long rotlXor42 = ThreefishEngine.rotlXor(rotlXor39, 34, j134);
                    long j135 = j123 + j130;
                    long j136 = rotlXor41;
                    long rotlXor43 = ThreefishEngine.rotlXor(j130, 56, j135);
                    long j137 = j127 + rotlXor40;
                    long j138 = rotlXor42;
                    long j139 = j128 + rotlXor36;
                    long rotlXor44 = ThreefishEngine.rotlXor(rotlXor40, 51, j137);
                    long rotlXor45 = ThreefishEngine.rotlXor(rotlXor36, 4, j139);
                    long j140 = j139;
                    long j141 = j131 + j125;
                    long j142 = j135;
                    long rotlXor46 = ThreefishEngine.rotlXor(j125, 53, j141);
                    long j143 = j132 + rotlXor35;
                    long j144 = j141;
                    long rotlXor47 = ThreefishEngine.rotlXor(rotlXor35, 42, j143);
                    long j145 = j143;
                    long j146 = j124 + j122;
                    long j147 = j137;
                    long rotlXor48 = ThreefishEngine.rotlXor(j122, 41, j146);
                    long j148 = j133 + rotlXor45;
                    long rotlXor49 = ThreefishEngine.rotlXor(rotlXor45, 31, j148);
                    long j149 = j134 + rotlXor47;
                    long rotlXor50 = ThreefishEngine.rotlXor(rotlXor47, 44, j149);
                    long j150 = rotlXor49;
                    long j151 = j147 + rotlXor46;
                    long rotlXor51 = ThreefishEngine.rotlXor(rotlXor46, 47, j151);
                    long j152 = j142 + rotlXor48;
                    long j153 = rotlXor50;
                    long j154 = j144 + rotlXor44;
                    long rotlXor52 = ThreefishEngine.rotlXor(rotlXor48, 46, j152);
                    long rotlXor53 = ThreefishEngine.rotlXor(rotlXor44, 19, j154);
                    long j155 = j154;
                    long j156 = j145 + j138;
                    long j157 = j151;
                    long rotlXor54 = ThreefishEngine.rotlXor(j138, 42, j156);
                    long j158 = j146 + rotlXor43;
                    long j159 = j156;
                    long rotlXor55 = ThreefishEngine.rotlXor(rotlXor43, 44, j158);
                    long j160 = j158;
                    long j161 = j140 + j136;
                    long j162 = rotlXor54;
                    long rotlXor56 = ThreefishEngine.rotlXor(j136, 25, j161);
                    long j163 = j148 + rotlXor53;
                    long rotlXor57 = ThreefishEngine.rotlXor(rotlXor53, 9, j163);
                    long j164 = j149 + rotlXor55;
                    long rotlXor58 = ThreefishEngine.rotlXor(rotlXor55, 48, j164);
                    long j165 = j152 + j162;
                    long j166 = rotlXor57;
                    long rotlXor59 = ThreefishEngine.rotlXor(j162, 35, j165);
                    long j167 = j157 + rotlXor56;
                    long j168 = rotlXor58;
                    long j169 = j159 + rotlXor52;
                    long rotlXor60 = ThreefishEngine.rotlXor(rotlXor56, 52, j167);
                    long rotlXor61 = ThreefishEngine.rotlXor(rotlXor52, 23, j169);
                    long j170 = j169;
                    long j171 = j160 + j153;
                    long j172 = j165;
                    long rotlXor62 = ThreefishEngine.rotlXor(j153, 31, j171);
                    long j173 = j161 + rotlXor51;
                    long j174 = j171;
                    long rotlXor63 = ThreefishEngine.rotlXor(rotlXor51, 37, j173);
                    long j175 = j173;
                    long j176 = j155 + j150;
                    long j177 = rotlXor62;
                    long rotlXor64 = ThreefishEngine.rotlXor(j150, 20, j176);
                    long j178 = j163 + jArr5[i12];
                    long j179 = rotlXor61 + jArr5[i13];
                    long j180 = j164 + jArr5[i9];
                    long j181 = rotlXor63 + jArr5[i10];
                    long j182 = j167 + jArr5[i11];
                    j32 = j177 + jArr5[i14];
                    j23 = j172 + jArr5[i15];
                    j33 = rotlXor64 + jArr5[i16];
                    j25 = j174 + jArr5[i17];
                    j34 = rotlXor60 + jArr5[i18];
                    j27 = j175 + jArr5[i19];
                    j35 = j168 + jArr5[i20];
                    long j183 = jArr5[i21] + j176;
                    j36 = rotlXor59 + jArr5[i22] + jArr4[i23];
                    j31 = j170 + jArr5[i24] + jArr4[i4 + 2];
                    j37 = j166 + jArr5[i6 + 16] + j117 + 1;
                    j18 = j179;
                    j40 = j180;
                    j21 = j182;
                    j29 = j183;
                    i2 = i26 + 2;
                    j17 = j178;
                    access$300 = iArr;
                    access$100 = access$100;
                    jArr3 = jArr4;
                    z = false;
                    i = 13;
                    j39 = j181;
                    jArr2 = jArr5;
                }
                r234[z] = j17;
                r234[1] = j18;
                r234[2] = j40;
                r234[3] = j39;
                r234[4] = j21;
                r234[5] = j32;
                r234[6] = j23;
                r234[7] = j33;
                r234[8] = j25;
                r234[9] = j34;
                r234[10] = j27;
                r234[11] = j35;
                r234[12] = j29;
                r234[13] = j36;
                r234[14] = j31;
                r234[15] = j37;
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private static final class Threefish256Cipher extends ThreefishCipher {
        private static final int ROTATION_0_0 = 14;
        private static final int ROTATION_0_1 = 16;
        private static final int ROTATION_1_0 = 52;
        private static final int ROTATION_1_1 = 57;
        private static final int ROTATION_2_0 = 23;
        private static final int ROTATION_2_1 = 40;
        private static final int ROTATION_3_0 = 5;
        private static final int ROTATION_3_1 = 37;
        private static final int ROTATION_4_0 = 25;
        private static final int ROTATION_4_1 = 33;
        private static final int ROTATION_5_0 = 46;
        private static final int ROTATION_5_1 = 12;
        private static final int ROTATION_6_0 = 58;
        private static final int ROTATION_6_1 = 22;
        private static final int ROTATION_7_0 = 32;
        private static final int ROTATION_7_1 = 32;

        public Threefish256Cipher(long[] jArr, long[] jArr2) {
            super(jArr, jArr2);
        }

        /* access modifiers changed from: package-private */
        public void decryptBlock(long[] jArr, long[] jArr2) {
            long[] jArr3 = this.kw;
            long[] jArr4 = this.t;
            int[] access$000 = ThreefishEngine.MOD5;
            int[] access$100 = ThreefishEngine.MOD3;
            if (jArr3.length != 9) {
                throw new IllegalArgumentException();
            } else if (jArr4.length == 5) {
                boolean z = false;
                long j = jArr[0];
                long j2 = jArr[1];
                long j3 = jArr[2];
                long j4 = jArr[3];
                int i = 17;
                for (int i2 = 1; i >= i2; i2 = 1) {
                    int i3 = access$000[i];
                    int i4 = access$100[i];
                    int i5 = i3 + 1;
                    long j5 = j - jArr3[i5];
                    int i6 = i3 + 2;
                    int i7 = i4 + 1;
                    long j6 = j2 - (jArr3[i6] + jArr4[i7]);
                    int i8 = i3 + 3;
                    long j7 = j3 - (jArr3[i8] + jArr4[i4 + 2]);
                    long j8 = (long) i;
                    long xorRotr = ThreefishEngine.xorRotr(j4 - ((jArr3[i3 + 4] + j8) + 1), 32, j5);
                    long j9 = j5 - xorRotr;
                    int[] iArr = access$000;
                    long xorRotr2 = ThreefishEngine.xorRotr(j6, 32, j7);
                    long j10 = j7 - xorRotr2;
                    long xorRotr3 = ThreefishEngine.xorRotr(xorRotr2, 58, j9);
                    long j11 = j9 - xorRotr3;
                    long xorRotr4 = ThreefishEngine.xorRotr(xorRotr, 22, j10);
                    long j12 = j10 - xorRotr4;
                    long xorRotr5 = ThreefishEngine.xorRotr(xorRotr4, 46, j11);
                    long j13 = j11 - xorRotr5;
                    long xorRotr6 = ThreefishEngine.xorRotr(xorRotr3, 12, j12);
                    long j14 = j12 - xorRotr6;
                    long xorRotr7 = ThreefishEngine.xorRotr(xorRotr6, 25, j13);
                    long xorRotr8 = ThreefishEngine.xorRotr(xorRotr5, 33, j14);
                    long j15 = (j13 - xorRotr7) - jArr3[i3];
                    long j16 = xorRotr7 - (jArr3[i5] + jArr4[i4]);
                    long j17 = (j14 - xorRotr8) - (jArr3[i6] + jArr4[i7]);
                    long xorRotr9 = ThreefishEngine.xorRotr(xorRotr8 - (jArr3[i8] + j8), 5, j15);
                    long j18 = j15 - xorRotr9;
                    long xorRotr10 = ThreefishEngine.xorRotr(j16, 37, j17);
                    long j19 = j17 - xorRotr10;
                    long xorRotr11 = ThreefishEngine.xorRotr(xorRotr10, 23, j18);
                    long j20 = j18 - xorRotr11;
                    long xorRotr12 = ThreefishEngine.xorRotr(xorRotr9, 40, j19);
                    long j21 = j19 - xorRotr12;
                    long xorRotr13 = ThreefishEngine.xorRotr(xorRotr12, 52, j20);
                    long j22 = j20 - xorRotr13;
                    long xorRotr14 = ThreefishEngine.xorRotr(xorRotr11, 57, j21);
                    long j23 = j21 - xorRotr14;
                    long xorRotr15 = ThreefishEngine.xorRotr(xorRotr14, 14, j22);
                    j = j22 - xorRotr15;
                    j4 = ThreefishEngine.xorRotr(xorRotr13, 16, j23);
                    j3 = j23 - j4;
                    i -= 2;
                    j2 = xorRotr15;
                    access$000 = iArr;
                    access$100 = access$100;
                    z = false;
                }
                char c = z;
                long j24 = j2 - (jArr3[1] + jArr4[c]);
                long j25 = j3 - (jArr3[2] + jArr4[1]);
                jArr2[c] = j - jArr3[c];
                jArr2[1] = j24;
                jArr2[2] = j25;
                jArr2[3] = j4 - jArr3[3];
            } else {
                throw new IllegalArgumentException();
            }
        }

        /* access modifiers changed from: package-private */
        public void encryptBlock(long[] jArr, long[] jArr2) {
            long[] jArr3 = this.kw;
            long[] jArr4 = this.t;
            int[] access$000 = ThreefishEngine.MOD5;
            int[] access$100 = ThreefishEngine.MOD3;
            if (jArr3.length != 9) {
                throw new IllegalArgumentException();
            } else if (jArr4.length == 5) {
                boolean z = false;
                long j = jArr[0];
                long j2 = jArr[1];
                long j3 = jArr[2];
                long j4 = jArr[3];
                long j5 = j + jArr3[0];
                long j6 = j2 + jArr3[1] + jArr4[0];
                int i = 1;
                long j7 = j3 + jArr3[2] + jArr4[1];
                long j8 = j4 + jArr3[3];
                long j9 = j7;
                while (i < 18) {
                    int i2 = access$000[i];
                    int i3 = access$100[i];
                    long j10 = j5 + j6;
                    long rotlXor = ThreefishEngine.rotlXor(j6, 14, j10);
                    long j11 = j9 + j8;
                    long rotlXor2 = ThreefishEngine.rotlXor(j8, 16, j11);
                    int[] iArr = access$000;
                    long j12 = j10 + rotlXor2;
                    long rotlXor3 = ThreefishEngine.rotlXor(rotlXor2, 52, j12);
                    long j13 = j11 + rotlXor;
                    long rotlXor4 = ThreefishEngine.rotlXor(rotlXor, 57, j13);
                    long j14 = j12 + rotlXor4;
                    long rotlXor5 = ThreefishEngine.rotlXor(rotlXor4, 23, j14);
                    long j15 = j13 + rotlXor3;
                    long rotlXor6 = ThreefishEngine.rotlXor(rotlXor3, 40, j15);
                    long j16 = j14 + rotlXor6;
                    long rotlXor7 = ThreefishEngine.rotlXor(rotlXor6, 5, j16);
                    long j17 = j15 + rotlXor5;
                    long rotlXor8 = ThreefishEngine.rotlXor(rotlXor5, 37, j17);
                    long j18 = j16 + jArr3[i2];
                    int i4 = i2 + 1;
                    long j19 = rotlXor8 + jArr3[i4] + jArr4[i3];
                    int i5 = i2 + 2;
                    int i6 = i3 + 1;
                    long j20 = j17 + jArr3[i5] + jArr4[i6];
                    int i7 = i2 + 3;
                    long j21 = (long) i;
                    long j22 = rotlXor7 + jArr3[i7] + j21;
                    long j23 = j18 + j19;
                    long rotlXor9 = ThreefishEngine.rotlXor(j19, 25, j23);
                    int i8 = i;
                    long j24 = j20 + j22;
                    long rotlXor10 = ThreefishEngine.rotlXor(j22, 33, j24);
                    long j25 = j23 + rotlXor10;
                    long rotlXor11 = ThreefishEngine.rotlXor(rotlXor10, 46, j25);
                    long j26 = j24 + rotlXor9;
                    long rotlXor12 = ThreefishEngine.rotlXor(rotlXor9, 12, j26);
                    long j27 = j25 + rotlXor12;
                    long rotlXor13 = ThreefishEngine.rotlXor(rotlXor12, 58, j27);
                    long j28 = j26 + rotlXor11;
                    long rotlXor14 = ThreefishEngine.rotlXor(rotlXor11, 22, j28);
                    long j29 = j27 + rotlXor14;
                    long rotlXor15 = ThreefishEngine.rotlXor(rotlXor14, 32, j29);
                    long j30 = j28 + rotlXor13;
                    long rotlXor16 = ThreefishEngine.rotlXor(rotlXor13, 32, j30);
                    j5 = jArr3[i4] + j29;
                    j6 = rotlXor16 + jArr3[i5] + jArr4[i6];
                    j9 = j30 + jArr3[i7] + jArr4[i3 + 2];
                    j8 = rotlXor15 + jArr3[i2 + 4] + j21 + 1;
                    i = i8 + 2;
                    access$000 = iArr;
                    access$100 = access$100;
                    z = false;
                }
                jArr2[z] = j5;
                jArr2[1] = j6;
                jArr2[2] = j9;
                jArr2[3] = j8;
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private static final class Threefish512Cipher extends ThreefishCipher {
        private static final int ROTATION_0_0 = 46;
        private static final int ROTATION_0_1 = 36;
        private static final int ROTATION_0_2 = 19;
        private static final int ROTATION_0_3 = 37;
        private static final int ROTATION_1_0 = 33;
        private static final int ROTATION_1_1 = 27;
        private static final int ROTATION_1_2 = 14;
        private static final int ROTATION_1_3 = 42;
        private static final int ROTATION_2_0 = 17;
        private static final int ROTATION_2_1 = 49;
        private static final int ROTATION_2_2 = 36;
        private static final int ROTATION_2_3 = 39;
        private static final int ROTATION_3_0 = 44;
        private static final int ROTATION_3_1 = 9;
        private static final int ROTATION_3_2 = 54;
        private static final int ROTATION_3_3 = 56;
        private static final int ROTATION_4_0 = 39;
        private static final int ROTATION_4_1 = 30;
        private static final int ROTATION_4_2 = 34;
        private static final int ROTATION_4_3 = 24;
        private static final int ROTATION_5_0 = 13;
        private static final int ROTATION_5_1 = 50;
        private static final int ROTATION_5_2 = 10;
        private static final int ROTATION_5_3 = 17;
        private static final int ROTATION_6_0 = 25;
        private static final int ROTATION_6_1 = 29;
        private static final int ROTATION_6_2 = 39;
        private static final int ROTATION_6_3 = 43;
        private static final int ROTATION_7_0 = 8;
        private static final int ROTATION_7_1 = 35;
        private static final int ROTATION_7_2 = 56;
        private static final int ROTATION_7_3 = 22;

        protected Threefish512Cipher(long[] jArr, long[] jArr2) {
            super(jArr, jArr2);
        }

        public void decryptBlock(long[] jArr, long[] jArr2) {
            long[] jArr3 = this.kw;
            long[] jArr4 = this.t;
            int[] access$200 = ThreefishEngine.MOD9;
            int[] access$100 = ThreefishEngine.MOD3;
            if (jArr3.length != 17) {
                throw new IllegalArgumentException();
            } else if (jArr4.length == 5) {
                boolean z = false;
                long j = jArr[0];
                long j2 = jArr[1];
                long j3 = jArr[2];
                long j4 = jArr[3];
                long j5 = jArr[4];
                long j6 = jArr[5];
                long j7 = jArr[6];
                long j8 = jArr[7];
                int i = 17;
                for (int i2 = 1; i >= i2; i2 = 1) {
                    int i3 = access$200[i];
                    int i4 = access$100[i];
                    int i5 = i3 + 1;
                    long j9 = j - jArr3[i5];
                    int i6 = i3 + 2;
                    long j10 = j2 - jArr3[i6];
                    int i7 = i3 + 3;
                    long j11 = j3 - jArr3[i7];
                    int i8 = i3 + 4;
                    long j12 = j4 - jArr3[i8];
                    int i9 = i3 + 5;
                    long j13 = j5 - jArr3[i9];
                    int i10 = i3 + 6;
                    int i11 = i4 + 1;
                    long j14 = j6 - (jArr3[i10] + jArr4[i11]);
                    int i12 = i3 + 7;
                    int[] iArr = access$200;
                    int[] iArr2 = access$100;
                    long j15 = j7 - (jArr3[i12] + jArr4[i4 + 2]);
                    long[] jArr5 = jArr3;
                    long j16 = (long) i;
                    int i13 = i;
                    long j17 = j16;
                    long j18 = j12;
                    long xorRotr = ThreefishEngine.xorRotr(j10, 8, j15);
                    long j19 = j15 - xorRotr;
                    long j20 = j9;
                    long xorRotr2 = ThreefishEngine.xorRotr(j8 - ((jArr3[i3 + 8] + j16) + 1), 35, j20);
                    long j21 = j20 - xorRotr2;
                    long xorRotr3 = ThreefishEngine.xorRotr(j14, 56, j11);
                    long j22 = j11 - xorRotr3;
                    long[] jArr6 = jArr4;
                    long xorRotr4 = ThreefishEngine.xorRotr(j18, 22, j13);
                    long j23 = j13 - xorRotr4;
                    long xorRotr5 = ThreefishEngine.xorRotr(xorRotr, 25, j23);
                    long j24 = j19;
                    long xorRotr6 = ThreefishEngine.xorRotr(xorRotr4, 29, j24);
                    long j25 = j24 - xorRotr6;
                    long xorRotr7 = ThreefishEngine.xorRotr(xorRotr3, 39, j21);
                    long xorRotr8 = ThreefishEngine.xorRotr(xorRotr2, 43, j22);
                    long j26 = j22 - xorRotr8;
                    long xorRotr9 = ThreefishEngine.xorRotr(xorRotr5, 13, j26);
                    long j27 = j26 - xorRotr9;
                    long j28 = j23 - xorRotr5;
                    long xorRotr10 = ThreefishEngine.xorRotr(xorRotr8, 50, j28);
                    long j29 = j28 - xorRotr10;
                    long xorRotr11 = ThreefishEngine.xorRotr(xorRotr7, 10, j25);
                    long j30 = j25 - xorRotr11;
                    long j31 = xorRotr10;
                    long j32 = j21 - xorRotr7;
                    long xorRotr12 = ThreefishEngine.xorRotr(xorRotr6, 17, j32);
                    long j33 = j32 - xorRotr12;
                    long xorRotr13 = ThreefishEngine.xorRotr(xorRotr9, 39, j33);
                    long xorRotr14 = ThreefishEngine.xorRotr(xorRotr12, 30, j27);
                    long xorRotr15 = ThreefishEngine.xorRotr(xorRotr11, 34, j29);
                    long j34 = j29 - xorRotr15;
                    long j35 = xorRotr15;
                    long xorRotr16 = ThreefishEngine.xorRotr(j31, 24, j30);
                    long j36 = (j33 - xorRotr13) - jArr5[i3];
                    long j37 = xorRotr13 - jArr5[i5];
                    long j38 = (j27 - xorRotr14) - jArr5[i6];
                    long j39 = xorRotr14 - jArr5[i7];
                    long j40 = j34 - jArr5[i8];
                    long j41 = j39;
                    long j42 = j35 - (jArr5[i9] + jArr6[i4]);
                    long j43 = (j30 - xorRotr16) - (jArr5[i10] + jArr6[i11]);
                    long xorRotr17 = ThreefishEngine.xorRotr(j37, 44, j43);
                    long j44 = j43 - xorRotr17;
                    long xorRotr18 = ThreefishEngine.xorRotr(xorRotr16 - (jArr5[i12] + j17), 9, j36);
                    long j45 = j36 - xorRotr18;
                    long xorRotr19 = ThreefishEngine.xorRotr(j42, 54, j38);
                    long j46 = j38 - xorRotr19;
                    long j47 = xorRotr18;
                    long xorRotr20 = ThreefishEngine.xorRotr(j41, 56, j40);
                    long j48 = j40 - xorRotr20;
                    long xorRotr21 = ThreefishEngine.xorRotr(xorRotr17, 17, j48);
                    long j49 = j48 - xorRotr21;
                    long xorRotr22 = ThreefishEngine.xorRotr(xorRotr20, 49, j44);
                    long xorRotr23 = ThreefishEngine.xorRotr(xorRotr19, 36, j45);
                    long j50 = j45 - xorRotr23;
                    long xorRotr24 = ThreefishEngine.xorRotr(j47, 39, j46);
                    long j51 = j46 - xorRotr24;
                    long xorRotr25 = ThreefishEngine.xorRotr(xorRotr21, 33, j51);
                    long xorRotr26 = ThreefishEngine.xorRotr(xorRotr24, 27, j49);
                    long j52 = j49 - xorRotr26;
                    long j53 = j44 - xorRotr22;
                    long xorRotr27 = ThreefishEngine.xorRotr(xorRotr23, 14, j53);
                    long j54 = j53 - xorRotr27;
                    long xorRotr28 = ThreefishEngine.xorRotr(xorRotr22, 42, j50);
                    long j55 = j50 - xorRotr28;
                    long xorRotr29 = ThreefishEngine.xorRotr(xorRotr25, 46, j55);
                    long j56 = j55 - xorRotr29;
                    long j57 = j51 - xorRotr25;
                    j4 = ThreefishEngine.xorRotr(xorRotr28, 36, j57);
                    j3 = j57 - j4;
                    j6 = ThreefishEngine.xorRotr(xorRotr27, 19, j52);
                    j5 = j52 - j6;
                    j8 = ThreefishEngine.xorRotr(xorRotr26, 37, j54);
                    j7 = j54 - j8;
                    i = i13 - 2;
                    j2 = xorRotr29;
                    access$200 = iArr;
                    access$100 = iArr2;
                    jArr3 = jArr5;
                    jArr4 = jArr6;
                    j = j56;
                    z = false;
                }
                long[] jArr7 = jArr4;
                long[] jArr8 = jArr3;
                char c = z;
                long j58 = j - jArr8[c];
                long j59 = j2 - jArr8[1];
                long j60 = j3 - jArr8[2];
                long j61 = j4 - jArr8[3];
                long j62 = j5 - jArr8[4];
                long j63 = j7 - (jArr8[6] + jArr7[1]);
                jArr2[c] = j58;
                jArr2[1] = j59;
                jArr2[2] = j60;
                jArr2[3] = j61;
                jArr2[4] = j62;
                jArr2[5] = j6 - (jArr8[5] + jArr7[c]);
                jArr2[6] = j63;
                jArr2[7] = j8 - jArr8[7];
            } else {
                throw new IllegalArgumentException();
            }
        }

        public void encryptBlock(long[] jArr, long[] jArr2) {
            long[] jArr3 = this.kw;
            long[] jArr4 = this.t;
            int[] access$200 = ThreefishEngine.MOD9;
            int[] access$100 = ThreefishEngine.MOD3;
            if (jArr3.length != 17) {
                throw new IllegalArgumentException();
            } else if (jArr4.length == 5) {
                long j = jArr[0];
                long j2 = jArr[1];
                long j3 = jArr[2];
                long j4 = jArr[3];
                long j5 = jArr[4];
                long j6 = jArr[5];
                long j7 = jArr[6];
                long j8 = jArr[7];
                long j9 = j + jArr3[0];
                long j10 = j2 + jArr3[1];
                long j11 = j3 + jArr3[2];
                long j12 = j4 + jArr3[3];
                long j13 = j5 + jArr3[4];
                long j14 = j6 + jArr3[5] + jArr4[0];
                long j15 = j7 + jArr3[6] + jArr4[1];
                int i = 1;
                long j16 = j14;
                long j17 = j8 + jArr3[7];
                long j18 = j12;
                long j19 = j11;
                while (i < 18) {
                    int i2 = access$200[i];
                    int i3 = access$100[i];
                    long j20 = j9 + j10;
                    long rotlXor = ThreefishEngine.rotlXor(j10, 46, j20);
                    int[] iArr = access$200;
                    int[] iArr2 = access$100;
                    long j21 = j19 + j18;
                    long rotlXor2 = ThreefishEngine.rotlXor(j18, 36, j21);
                    int i4 = i;
                    long j22 = j13 + j16;
                    long rotlXor3 = ThreefishEngine.rotlXor(j16, 19, j22);
                    int i5 = i2;
                    long j23 = rotlXor2;
                    long j24 = j17;
                    long j25 = j15 + j24;
                    long rotlXor4 = ThreefishEngine.rotlXor(j24, 37, j25);
                    long j26 = j21 + rotlXor;
                    long rotlXor5 = ThreefishEngine.rotlXor(rotlXor, 33, j26);
                    long j27 = j22 + rotlXor4;
                    long rotlXor6 = ThreefishEngine.rotlXor(rotlXor4, 27, j27);
                    long j28 = j25 + rotlXor3;
                    long rotlXor7 = ThreefishEngine.rotlXor(rotlXor3, 14, j28);
                    long[] jArr5 = jArr3;
                    long j29 = j20 + j23;
                    long j30 = rotlXor6;
                    long rotlXor8 = ThreefishEngine.rotlXor(j23, 42, j29);
                    long j31 = j27 + rotlXor5;
                    long rotlXor9 = ThreefishEngine.rotlXor(rotlXor5, 17, j31);
                    long j32 = j28 + rotlXor8;
                    long rotlXor10 = ThreefishEngine.rotlXor(rotlXor8, 49, j32);
                    long j33 = j29 + rotlXor7;
                    long rotlXor11 = ThreefishEngine.rotlXor(rotlXor7, 36, j33);
                    long j34 = j26 + j30;
                    long[] jArr6 = jArr4;
                    long rotlXor12 = ThreefishEngine.rotlXor(j30, 39, j34);
                    long j35 = j32 + rotlXor9;
                    long rotlXor13 = ThreefishEngine.rotlXor(rotlXor9, 44, j35);
                    long j36 = j33 + rotlXor12;
                    long rotlXor14 = ThreefishEngine.rotlXor(rotlXor12, 9, j36);
                    long j37 = j34 + rotlXor11;
                    long rotlXor15 = ThreefishEngine.rotlXor(rotlXor11, 54, j37);
                    long j38 = rotlXor14;
                    long j39 = j31 + rotlXor10;
                    long rotlXor16 = ThreefishEngine.rotlXor(rotlXor10, 56, j39);
                    long j40 = j36 + jArr5[i5];
                    int i6 = i5 + 1;
                    long j41 = rotlXor13 + jArr5[i6];
                    int i7 = i5 + 2;
                    long j42 = j37 + jArr5[i7];
                    int i8 = i5 + 3;
                    long j43 = rotlXor16 + jArr5[i8];
                    int i9 = i5 + 4;
                    long j44 = j39 + jArr5[i9];
                    int i10 = i5 + 5;
                    long j45 = rotlXor15 + jArr5[i10] + jArr6[i3];
                    int i11 = i5 + 6;
                    int i12 = i3 + 1;
                    long j46 = j35 + jArr5[i11] + jArr6[i12];
                    int i13 = i5 + 7;
                    long j47 = j43;
                    int i14 = i4;
                    long j48 = (long) i14;
                    long j49 = j48;
                    long j50 = j38 + jArr5[i13] + j48;
                    long j51 = j40 + j41;
                    long rotlXor17 = ThreefishEngine.rotlXor(j41, 39, j51);
                    long j52 = j42 + j47;
                    int i15 = i14;
                    long rotlXor18 = ThreefishEngine.rotlXor(j47, 30, j52);
                    long j53 = j44 + j45;
                    long rotlXor19 = ThreefishEngine.rotlXor(j45, 34, j53);
                    long j54 = j46 + j50;
                    long rotlXor20 = ThreefishEngine.rotlXor(j50, 24, j54);
                    long j55 = j52 + rotlXor17;
                    long rotlXor21 = ThreefishEngine.rotlXor(rotlXor17, 13, j55);
                    long j56 = j53 + rotlXor20;
                    long rotlXor22 = ThreefishEngine.rotlXor(rotlXor20, 50, j56);
                    long j57 = j54 + rotlXor19;
                    long rotlXor23 = ThreefishEngine.rotlXor(rotlXor19, 10, j57);
                    long j58 = j51 + rotlXor18;
                    long j59 = rotlXor22;
                    long rotlXor24 = ThreefishEngine.rotlXor(rotlXor18, 17, j58);
                    long j60 = j56 + rotlXor21;
                    long rotlXor25 = ThreefishEngine.rotlXor(rotlXor21, 25, j60);
                    long j61 = j57 + rotlXor24;
                    long rotlXor26 = ThreefishEngine.rotlXor(rotlXor24, 29, j61);
                    long j62 = j58 + rotlXor23;
                    long rotlXor27 = ThreefishEngine.rotlXor(rotlXor23, 39, j62);
                    long j63 = j55 + j59;
                    long j64 = j60;
                    long rotlXor28 = ThreefishEngine.rotlXor(j59, 43, j63);
                    long j65 = j61 + rotlXor25;
                    long rotlXor29 = ThreefishEngine.rotlXor(rotlXor25, 8, j65);
                    long j66 = j62 + rotlXor28;
                    long rotlXor30 = ThreefishEngine.rotlXor(rotlXor28, 35, j66);
                    long j67 = j63 + rotlXor27;
                    long rotlXor31 = ThreefishEngine.rotlXor(rotlXor27, 56, j67);
                    long j68 = j64 + rotlXor26;
                    long rotlXor32 = ThreefishEngine.rotlXor(rotlXor26, 22, j68);
                    long j69 = rotlXor29 + jArr5[i7];
                    j19 = j67 + jArr5[i8];
                    long j70 = rotlXor32 + jArr5[i9];
                    j13 = j68 + jArr5[i10];
                    long j71 = rotlXor31 + jArr5[i11] + jArr6[i12];
                    j15 = j65 + jArr5[i13] + jArr6[i3 + 2];
                    j17 = rotlXor30 + jArr5[i5 + 8] + j49 + 1;
                    i = i15 + 2;
                    j18 = j70;
                    j10 = j69;
                    j9 = j66 + jArr5[i6];
                    jArr3 = jArr5;
                    jArr4 = jArr6;
                    j16 = j71;
                    access$200 = iArr;
                    access$100 = iArr2;
                }
                jArr2[0] = j9;
                jArr2[1] = j10;
                jArr2[2] = j19;
                jArr2[3] = j18;
                jArr2[4] = j13;
                jArr2[5] = j16;
                jArr2[6] = j15;
                jArr2[7] = j17;
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private static abstract class ThreefishCipher {
        protected final long[] kw;
        protected final long[] t;

        protected ThreefishCipher(long[] jArr, long[] jArr2) {
            this.kw = jArr;
            this.t = jArr2;
        }

        /* access modifiers changed from: package-private */
        public abstract void decryptBlock(long[] jArr, long[] jArr2);

        /* access modifiers changed from: package-private */
        public abstract void encryptBlock(long[] jArr, long[] jArr2);
    }

    static {
        for (int i = 0; i < MOD9.length; i++) {
            MOD17[i] = i % 17;
            MOD9[i] = i % 9;
            MOD5[i] = i % 5;
            MOD3[i] = i % 3;
        }
    }

    public ThreefishEngine(int i) {
        ThreefishCipher threefishCipher;
        this.blocksizeBytes = i / 8;
        this.blocksizeWords = this.blocksizeBytes / 8;
        this.currentBlock = new long[this.blocksizeWords];
        this.kw = new long[((2 * this.blocksizeWords) + 1)];
        if (i == 256) {
            threefishCipher = new Threefish256Cipher(this.kw, this.t);
        } else if (i == 512) {
            threefishCipher = new Threefish512Cipher(this.kw, this.t);
        } else if (i == 1024) {
            threefishCipher = new Threefish1024Cipher(this.kw, this.t);
        } else {
            throw new IllegalArgumentException("Invalid blocksize - Threefish is defined with block size of 256, 512, or 1024 bits");
        }
        this.cipher = threefishCipher;
    }

    public static long bytesToWord(byte[] bArr, int i) {
        if (i + 8 <= bArr.length) {
            int i2 = i + 1;
            int i3 = i2 + 1;
            int i4 = i3 + 1;
            int i5 = i4 + 1;
            int i6 = i5 + 1;
            int i7 = i6 + 1;
            return ((((long) bArr[i7 + 1]) & 255) << 56) | (((long) bArr[i]) & 255) | ((((long) bArr[i2]) & 255) << 8) | ((((long) bArr[i3]) & 255) << 16) | ((((long) bArr[i4]) & 255) << 24) | ((((long) bArr[i5]) & 255) << 32) | ((((long) bArr[i6]) & 255) << 40) | ((((long) bArr[i7]) & 255) << 48);
        }
        throw new IllegalArgumentException();
    }

    static long rotlXor(long j, int i, long j2) {
        return ((j >>> (-i)) | (j << i)) ^ j2;
    }

    private void setKey(long[] jArr) {
        if (jArr.length == this.blocksizeWords) {
            long j = 2004413935125273122L;
            for (int i = 0; i < this.blocksizeWords; i++) {
                this.kw[i] = jArr[i];
                j ^= this.kw[i];
            }
            this.kw[this.blocksizeWords] = j;
            System.arraycopy(this.kw, 0, this.kw, this.blocksizeWords + 1, this.blocksizeWords);
            return;
        }
        throw new IllegalArgumentException("Threefish key must be same size as block (" + this.blocksizeWords + " words)");
    }

    private void setTweak(long[] jArr) {
        if (jArr.length == 2) {
            this.t[0] = jArr[0];
            this.t[1] = jArr[1];
            this.t[2] = this.t[0] ^ this.t[1];
            this.t[3] = this.t[0];
            this.t[4] = this.t[1];
            return;
        }
        throw new IllegalArgumentException("Tweak must be 2 words.");
    }

    public static void wordToBytes(long j, byte[] bArr, int i) {
        if (i + 8 <= bArr.length) {
            int i2 = i + 1;
            bArr[i] = (byte) ((int) j);
            int i3 = i2 + 1;
            bArr[i2] = (byte) ((int) (j >> 8));
            int i4 = i3 + 1;
            bArr[i3] = (byte) ((int) (j >> 16));
            int i5 = i4 + 1;
            bArr[i4] = (byte) ((int) (j >> 24));
            int i6 = i5 + 1;
            bArr[i5] = (byte) ((int) (j >> 32));
            int i7 = i6 + 1;
            bArr[i6] = (byte) ((int) (j >> 40));
            bArr[i7] = (byte) ((int) (j >> 48));
            bArr[i7 + 1] = (byte) ((int) (j >> 56));
            return;
        }
        throw new IllegalArgumentException();
    }

    static long xorRotr(long j, int i, long j2) {
        long j3 = j ^ j2;
        return (j3 << (-i)) | (j3 >>> i);
    }

    public String getAlgorithmName() {
        return "Threefish-" + (this.blocksizeBytes * 8);
    }

    public int getBlockSize() {
        return this.blocksizeBytes;
    }

    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        byte[] bArr;
        byte[] bArr2;
        long[] jArr;
        long[] jArr2 = null;
        if (cipherParameters instanceof TweakableBlockCipherParameters) {
            TweakableBlockCipherParameters tweakableBlockCipherParameters = (TweakableBlockCipherParameters) cipherParameters;
            bArr2 = tweakableBlockCipherParameters.getKey().getKey();
            bArr = tweakableBlockCipherParameters.getTweak();
        } else if (cipherParameters instanceof KeyParameter) {
            bArr2 = ((KeyParameter) cipherParameters).getKey();
            bArr = null;
        } else {
            throw new IllegalArgumentException("Invalid parameter passed to Threefish init - " + cipherParameters.getClass().getName());
        }
        if (bArr2 == null) {
            jArr = null;
        } else if (bArr2.length == this.blocksizeBytes) {
            jArr = new long[this.blocksizeWords];
            for (int i = 0; i < jArr.length; i++) {
                jArr[i] = bytesToWord(bArr2, i * 8);
            }
        } else {
            throw new IllegalArgumentException("Threefish key must be same size as block (" + this.blocksizeBytes + " bytes)");
        }
        if (bArr != null) {
            if (bArr.length == 16) {
                jArr2 = new long[]{bytesToWord(bArr, 0), bytesToWord(bArr, 8)};
            } else {
                throw new IllegalArgumentException("Threefish tweak must be 16 bytes");
            }
        }
        init(z, jArr, jArr2);
    }

    public void init(boolean z, long[] jArr, long[] jArr2) {
        this.forEncryption = z;
        if (jArr != null) {
            setKey(jArr);
        }
        if (jArr2 != null) {
            setTweak(jArr2);
        }
    }

    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        if (this.blocksizeBytes + i > bArr.length) {
            throw new DataLengthException("Input buffer too short");
        } else if (this.blocksizeBytes + i2 <= bArr2.length) {
            for (int i3 = 0; i3 < this.blocksizeBytes; i3 += 8) {
                this.currentBlock[i3 >> 3] = bytesToWord(bArr, i + i3);
            }
            processBlock(this.currentBlock, this.currentBlock);
            for (int i4 = 0; i4 < this.blocksizeBytes; i4 += 8) {
                wordToBytes(this.currentBlock[i4 >> 3], bArr2, i2 + i4);
            }
            return this.blocksizeBytes;
        } else {
            throw new OutputLengthException("Output buffer too short");
        }
    }

    public int processBlock(long[] jArr, long[] jArr2) throws DataLengthException, IllegalStateException {
        if (this.kw[this.blocksizeWords] == 0) {
            throw new IllegalStateException("Threefish engine not initialised");
        } else if (jArr.length != this.blocksizeWords) {
            throw new DataLengthException("Input buffer too short");
        } else if (jArr2.length == this.blocksizeWords) {
            if (this.forEncryption) {
                this.cipher.encryptBlock(jArr, jArr2);
            } else {
                this.cipher.decryptBlock(jArr, jArr2);
            }
            return this.blocksizeWords;
        } else {
            throw new OutputLengthException("Output buffer too short");
        }
    }

    public void reset() {
    }
}
