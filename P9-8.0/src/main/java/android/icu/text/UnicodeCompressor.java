package android.icu.text;

import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Normalizer2Impl.Hangul;

public final class UnicodeCompressor implements SCSU {
    private static boolean[] sSingleTagTable = new boolean[]{false, true, true, true, true, true, true, true, true, false, false, true, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
    private static boolean[] sUnicodeTagTable = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false};
    private int fCurrentWindow = 0;
    private int[] fIndexCount = new int[256];
    private int fMode = 0;
    private int[] fOffsets = new int[8];
    private int fTimeStamp = 0;
    private int[] fTimeStamps = new int[8];

    public UnicodeCompressor() {
        reset();
    }

    public static byte[] compress(String buffer) {
        return compress(buffer.toCharArray(), 0, buffer.length());
    }

    public static byte[] compress(char[] buffer, int start, int limit) {
        UnicodeCompressor comp = new UnicodeCompressor();
        int len = Math.max(4, ((limit - start) * 3) + 1);
        byte[] temp = new byte[len];
        int byteCount = comp.compress(buffer, start, limit, null, temp, 0, len);
        byte[] result = new byte[byteCount];
        System.arraycopy(temp, 0, result, 0, byteCount);
        return result;
    }

    /* JADX WARNING: Missing block: B:13:0x002f, code:
            r10 = r9;
            r2 = r1;
     */
    /* JADX WARNING: Missing block: B:14:0x0031, code:
            if (r10 >= r17) goto L_0x0060;
     */
    /* JADX WARNING: Missing block: B:16:0x0035, code:
            if (r2 >= r21) goto L_0x0060;
     */
    /* JADX WARNING: Missing block: B:17:0x0037, code:
            r9 = r10 + 1;
            r4 = r15[r10];
     */
    /* JADX WARNING: Missing block: B:18:0x003d, code:
            if (r9 >= r17) goto L_0x0063;
     */
    /* JADX WARNING: Missing block: B:19:0x003f, code:
            r8 = r15[r9];
     */
    /* JADX WARNING: Missing block: B:21:0x0043, code:
            if (r4 >= 128) goto L_0x0073;
     */
    /* JADX WARNING: Missing block: B:22:0x0045, code:
            r7 = r4 & 255;
     */
    /* JADX WARNING: Missing block: B:23:0x004b, code:
            if (sSingleTagTable[r7] == false) goto L_0x03c9;
     */
    /* JADX WARNING: Missing block: B:25:0x0051, code:
            if ((r2 + 1) < r21) goto L_0x0065;
     */
    /* JADX WARNING: Missing block: B:26:0x0053, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:31:0x0060, code:
            r9 = r10;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:32:0x0063, code:
            r8 = -1;
     */
    /* JADX WARNING: Missing block: B:33:0x0065, code:
            r1 = r2 + 1;
            r19[r2] = (byte) 1;
     */
    /* JADX WARNING: Missing block: B:34:0x006a, code:
            r2 = r1 + 1;
            r19[r1] = (byte) r7;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:37:0x0079, code:
            if (inDynamicWindow(r4, r14.fCurrentWindow) == false) goto L_0x008b;
     */
    /* JADX WARNING: Missing block: B:38:0x007b, code:
            r1 = r2 + 1;
            r19[r2] = (byte) ((r4 - r14.fOffsets[r14.fCurrentWindow]) + 128);
     */
    /* JADX WARNING: Missing block: B:40:0x008f, code:
            if (isCompressible(r4) != false) goto L_0x00e9;
     */
    /* JADX WARNING: Missing block: B:42:0x0092, code:
            if (r8 == -1) goto L_0x00b9;
     */
    /* JADX WARNING: Missing block: B:44:0x0098, code:
            if (isCompressible(r8) == false) goto L_0x00b9;
     */
    /* JADX WARNING: Missing block: B:46:0x009e, code:
            if ((r2 + 2) < r21) goto L_0x00a4;
     */
    /* JADX WARNING: Missing block: B:47:0x00a0, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:48:0x00a4, code:
            r1 = r2 + 1;
            r19[r2] = (byte) 14;
            r2 = r1 + 1;
            r19[r1] = (byte) (r4 >>> 8);
            r1 = r2 + 1;
            r19[r2] = (byte) (r4 & 255);
     */
    /* JADX WARNING: Missing block: B:50:0x00bd, code:
            if ((r2 + 3) < r21) goto L_0x00c3;
     */
    /* JADX WARNING: Missing block: B:51:0x00bf, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:52:0x00c3, code:
            r1 = r2 + 1;
            r19[r2] = (byte) 15;
            r6 = r4 >>> 8;
            r7 = r4 & 255;
     */
    /* JADX WARNING: Missing block: B:53:0x00d1, code:
            if (sUnicodeTagTable[r6] == false) goto L_0x00da;
     */
    /* JADX WARNING: Missing block: B:54:0x00d3, code:
            r2 = r1 + 1;
            r19[r1] = (byte) -16;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:55:0x00da, code:
            r2 = r1 + 1;
            r19[r1] = (byte) r6;
            r1 = r2 + 1;
            r19[r2] = (byte) r7;
            r14.fMode = 1;
     */
    /* JADX WARNING: Missing block: B:56:0x00e9, code:
            r11 = findDynamicWindow(r4);
     */
    /* JADX WARNING: Missing block: B:57:0x00ee, code:
            if (r11 == -1) goto L_0x0158;
     */
    /* JADX WARNING: Missing block: B:59:0x00f4, code:
            if ((r9 + 1) >= r17) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:60:0x00f6, code:
            r5 = r15[r9 + 1];
     */
    /* JADX WARNING: Missing block: B:62:0x00fe, code:
            if (inDynamicWindow(r8, r11) == false) goto L_0x0136;
     */
    /* JADX WARNING: Missing block: B:64:0x0104, code:
            if (inDynamicWindow(r5, r11) == false) goto L_0x0136;
     */
    /* JADX WARNING: Missing block: B:66:0x010a, code:
            if ((r2 + 1) < r21) goto L_0x0113;
     */
    /* JADX WARNING: Missing block: B:67:0x010c, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:68:0x0111, code:
            r5 = -1;
     */
    /* JADX WARNING: Missing block: B:69:0x0113, code:
            r1 = r2 + 1;
            r19[r2] = (byte) (r11 + 16);
            r2 = r1 + 1;
            r19[r1] = (byte) ((r4 - r14.fOffsets[r11]) + 128);
            r12 = r14.fTimeStamps;
            r13 = r14.fTimeStamp + 1;
            r14.fTimeStamp = r13;
            r12[r11] = r13;
            r14.fCurrentWindow = r11;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:71:0x013a, code:
            if ((r2 + 1) < r21) goto L_0x0141;
     */
    /* JADX WARNING: Missing block: B:72:0x013c, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:73:0x0141, code:
            r1 = r2 + 1;
            r19[r2] = (byte) (r11 + 1);
            r2 = r1 + 1;
            r19[r1] = (byte) ((r4 - r14.fOffsets[r11]) + 128);
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:74:0x0158, code:
            r11 = findStaticWindow(r4);
     */
    /* JADX WARNING: Missing block: B:75:0x015d, code:
            if (r11 == -1) goto L_0x0187;
     */
    /* JADX WARNING: Missing block: B:77:0x0165, code:
            if ((inStaticWindow(r8, r11) ^ 1) == 0) goto L_0x0187;
     */
    /* JADX WARNING: Missing block: B:79:0x016b, code:
            if ((r2 + 1) < r21) goto L_0x0172;
     */
    /* JADX WARNING: Missing block: B:80:0x016d, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:81:0x0172, code:
            r1 = r2 + 1;
            r19[r2] = (byte) (r11 + 1);
            r2 = r1 + 1;
            r19[r1] = (byte) (r4 - sOffsets[r11]);
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:82:0x0187, code:
            r3 = makeIndex(r4);
            r12 = r14.fIndexCount;
            r12[r3] = r12[r3] + 1;
     */
    /* JADX WARNING: Missing block: B:83:0x0197, code:
            if ((r9 + 1) >= r17) goto L_0x01bb;
     */
    /* JADX WARNING: Missing block: B:84:0x0199, code:
            r5 = r15[r9 + 1];
     */
    /* JADX WARNING: Missing block: B:86:0x01a2, code:
            if (r14.fIndexCount[r3] > 1) goto L_0x01b0;
     */
    /* JADX WARNING: Missing block: B:88:0x01a8, code:
            if (r3 != makeIndex(r8)) goto L_0x01f0;
     */
    /* JADX WARNING: Missing block: B:90:0x01ae, code:
            if (r3 != makeIndex(r5)) goto L_0x01f0;
     */
    /* JADX WARNING: Missing block: B:92:0x01b4, code:
            if ((r2 + 2) < r21) goto L_0x01bd;
     */
    /* JADX WARNING: Missing block: B:93:0x01b6, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:94:0x01bb, code:
            r5 = -1;
     */
    /* JADX WARNING: Missing block: B:95:0x01bd, code:
            r11 = getLRDefinedWindow();
            r1 = r2 + 1;
            r19[r2] = (byte) (r11 + 24);
            r2 = r1 + 1;
            r19[r1] = (byte) r3;
            r1 = r2 + 1;
            r19[r2] = (byte) ((r4 - sOffsetTable[r3]) + 128);
            r14.fOffsets[r11] = sOffsetTable[r3];
            r14.fCurrentWindow = r11;
            r12 = r14.fTimeStamps;
            r13 = r14.fTimeStamp + 1;
            r14.fTimeStamp = r13;
            r12[r11] = r13;
     */
    /* JADX WARNING: Missing block: B:97:0x01f4, code:
            if ((r2 + 3) < r21) goto L_0x01fb;
     */
    /* JADX WARNING: Missing block: B:98:0x01f6, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:99:0x01fb, code:
            r1 = r2 + 1;
            r19[r2] = (byte) 15;
            r6 = r4 >>> 8;
            r7 = r4 & 255;
     */
    /* JADX WARNING: Missing block: B:100:0x0209, code:
            if (sUnicodeTagTable[r6] == false) goto L_0x0212;
     */
    /* JADX WARNING: Missing block: B:101:0x020b, code:
            r2 = r1 + 1;
            r19[r1] = (byte) -16;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:102:0x0212, code:
            r2 = r1 + 1;
            r19[r1] = (byte) r6;
            r1 = r2 + 1;
            r19[r2] = (byte) r7;
            r14.fMode = 1;
     */
    /* JADX WARNING: Missing block: B:103:0x0223, code:
            r10 = r9;
            r2 = r1;
     */
    /* JADX WARNING: Missing block: B:104:0x0225, code:
            if (r10 >= r17) goto L_0x0251;
     */
    /* JADX WARNING: Missing block: B:106:0x0229, code:
            if (r2 >= r21) goto L_0x0251;
     */
    /* JADX WARNING: Missing block: B:107:0x022b, code:
            r9 = r10 + 1;
            r4 = r15[r10];
     */
    /* JADX WARNING: Missing block: B:108:0x0231, code:
            if (r9 >= r17) goto L_0x0255;
     */
    /* JADX WARNING: Missing block: B:109:0x0233, code:
            r8 = r15[r9];
     */
    /* JADX WARNING: Missing block: B:111:0x0239, code:
            if (isCompressible(r4) == false) goto L_0x0246;
     */
    /* JADX WARNING: Missing block: B:113:0x023c, code:
            if (r8 == -1) goto L_0x0274;
     */
    /* JADX WARNING: Missing block: B:115:0x0244, code:
            if ((isCompressible(r8) ^ 1) == 0) goto L_0x0274;
     */
    /* JADX WARNING: Missing block: B:117:0x024a, code:
            if ((r2 + 2) < r21) goto L_0x0257;
     */
    /* JADX WARNING: Missing block: B:118:0x024c, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:119:0x0251, code:
            r9 = r10;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:120:0x0255, code:
            r8 = -1;
     */
    /* JADX WARNING: Missing block: B:121:0x0257, code:
            r6 = r4 >>> 8;
            r7 = r4 & 255;
     */
    /* JADX WARNING: Missing block: B:122:0x025f, code:
            if (sUnicodeTagTable[r6] == false) goto L_0x03c6;
     */
    /* JADX WARNING: Missing block: B:123:0x0261, code:
            r1 = r2 + 1;
            r19[r2] = (byte) -16;
     */
    /* JADX WARNING: Missing block: B:124:0x0267, code:
            r2 = r1 + 1;
            r19[r1] = (byte) r6;
            r1 = r2 + 1;
            r19[r2] = (byte) r7;
     */
    /* JADX WARNING: Missing block: B:127:0x0276, code:
            if (r4 >= 128) goto L_0x02c9;
     */
    /* JADX WARNING: Missing block: B:128:0x0278, code:
            r7 = r4 & 255;
     */
    /* JADX WARNING: Missing block: B:129:0x027b, code:
            if (r8 == -1) goto L_0x02b2;
     */
    /* JADX WARNING: Missing block: B:131:0x027f, code:
            if (r8 >= 128) goto L_0x02b2;
     */
    /* JADX WARNING: Missing block: B:133:0x0287, code:
            if ((sSingleTagTable[r7] ^ 1) == 0) goto L_0x02b2;
     */
    /* JADX WARNING: Missing block: B:135:0x028d, code:
            if ((r2 + 1) < r21) goto L_0x0294;
     */
    /* JADX WARNING: Missing block: B:136:0x028f, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:137:0x0294, code:
            r11 = r14.fCurrentWindow;
            r1 = r2 + 1;
            r19[r2] = (byte) (r11 + 224);
            r2 = r1 + 1;
            r19[r1] = (byte) r7;
            r12 = r14.fTimeStamps;
            r13 = r14.fTimeStamp + 1;
            r14.fTimeStamp = r13;
            r12[r11] = r13;
            r14.fMode = 0;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:139:0x02b6, code:
            if ((r2 + 1) < r21) goto L_0x02bd;
     */
    /* JADX WARNING: Missing block: B:140:0x02b8, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:141:0x02bd, code:
            r1 = r2 + 1;
            r19[r2] = (byte) 0;
            r2 = r1 + 1;
            r19[r1] = (byte) r7;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:142:0x02c9, code:
            r11 = findDynamicWindow(r4);
     */
    /* JADX WARNING: Missing block: B:143:0x02ce, code:
            if (r11 == -1) goto L_0x032e;
     */
    /* JADX WARNING: Missing block: B:145:0x02d4, code:
            if (inDynamicWindow(r8, r11) == false) goto L_0x0307;
     */
    /* JADX WARNING: Missing block: B:147:0x02da, code:
            if ((r2 + 1) < r21) goto L_0x02e1;
     */
    /* JADX WARNING: Missing block: B:148:0x02dc, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:149:0x02e1, code:
            r1 = r2 + 1;
            r19[r2] = (byte) (r11 + 224);
            r2 = r1 + 1;
            r19[r1] = (byte) ((r4 - r14.fOffsets[r11]) + 128);
            r12 = r14.fTimeStamps;
            r13 = r14.fTimeStamp + 1;
            r14.fTimeStamp = r13;
            r12[r11] = r13;
            r14.fCurrentWindow = r11;
            r14.fMode = 0;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:151:0x030b, code:
            if ((r2 + 2) < r21) goto L_0x0312;
     */
    /* JADX WARNING: Missing block: B:152:0x030d, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:153:0x0312, code:
            r6 = r4 >>> 8;
            r7 = r4 & 255;
     */
    /* JADX WARNING: Missing block: B:154:0x031a, code:
            if (sUnicodeTagTable[r6] == false) goto L_0x03c3;
     */
    /* JADX WARNING: Missing block: B:155:0x031c, code:
            r1 = r2 + 1;
            r19[r2] = (byte) -16;
     */
    /* JADX WARNING: Missing block: B:156:0x0322, code:
            r2 = r1 + 1;
            r19[r1] = (byte) r6;
            r1 = r2 + 1;
            r19[r2] = (byte) r7;
     */
    /* JADX WARNING: Missing block: B:157:0x032e, code:
            r3 = makeIndex(r4);
            r12 = r14.fIndexCount;
            r12[r3] = r12[r3] + 1;
     */
    /* JADX WARNING: Missing block: B:158:0x033e, code:
            if ((r9 + 1) >= r17) goto L_0x0362;
     */
    /* JADX WARNING: Missing block: B:159:0x0340, code:
            r5 = r15[r9 + 1];
     */
    /* JADX WARNING: Missing block: B:161:0x0349, code:
            if (r14.fIndexCount[r3] > 1) goto L_0x0357;
     */
    /* JADX WARNING: Missing block: B:163:0x034f, code:
            if (r3 != makeIndex(r8)) goto L_0x039a;
     */
    /* JADX WARNING: Missing block: B:165:0x0355, code:
            if (r3 != makeIndex(r5)) goto L_0x039a;
     */
    /* JADX WARNING: Missing block: B:167:0x035b, code:
            if ((r2 + 2) < r21) goto L_0x0364;
     */
    /* JADX WARNING: Missing block: B:168:0x035d, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:169:0x0362, code:
            r5 = -1;
     */
    /* JADX WARNING: Missing block: B:170:0x0364, code:
            r11 = getLRDefinedWindow();
            r1 = r2 + 1;
            r19[r2] = (byte) (r11 + 232);
            r2 = r1 + 1;
            r19[r1] = (byte) r3;
            r1 = r2 + 1;
            r19[r2] = (byte) ((r4 - sOffsetTable[r3]) + 128);
            r14.fOffsets[r11] = sOffsetTable[r3];
            r14.fCurrentWindow = r11;
            r12 = r14.fTimeStamps;
            r13 = r14.fTimeStamp + 1;
            r14.fTimeStamp = r13;
            r12[r11] = r13;
            r14.fMode = 0;
     */
    /* JADX WARNING: Missing block: B:172:0x039e, code:
            if ((r2 + 2) < r21) goto L_0x03a5;
     */
    /* JADX WARNING: Missing block: B:173:0x03a0, code:
            r9 = r9 - 1;
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:174:0x03a5, code:
            r6 = r4 >>> 8;
            r7 = r4 & 255;
     */
    /* JADX WARNING: Missing block: B:175:0x03ad, code:
            if (sUnicodeTagTable[r6] == false) goto L_0x03c1;
     */
    /* JADX WARNING: Missing block: B:176:0x03af, code:
            r1 = r2 + 1;
            r19[r2] = (byte) -16;
     */
    /* JADX WARNING: Missing block: B:177:0x03b5, code:
            r2 = r1 + 1;
            r19[r1] = (byte) r6;
            r1 = r2 + 1;
            r19[r2] = (byte) r7;
     */
    /* JADX WARNING: Missing block: B:178:0x03c1, code:
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:179:0x03c3, code:
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:180:0x03c6, code:
            r1 = r2;
     */
    /* JADX WARNING: Missing block: B:181:0x03c9, code:
            r1 = r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int compress(char[] charBuffer, int charBufferStart, int charBufferLimit, int[] charsRead, byte[] byteBuffer, int byteBufferStart, int byteBufferLimit) {
        int bytePos = byteBufferStart;
        int ucPos = charBufferStart;
        if (byteBuffer.length < 4 || byteBufferLimit - byteBufferStart < 4) {
            throw new IllegalArgumentException("byteBuffer.length < 4");
        }
        while (ucPos < charBufferLimit && bytePos < byteBufferLimit) {
            switch (this.fMode) {
                case 0:
                    break;
                case 1:
                    break;
                default:
                    break;
            }
        }
        if (charsRead != null) {
            charsRead[0] = ucPos - charBufferStart;
        }
        return bytePos - byteBufferStart;
    }

    public void reset() {
        int i;
        this.fOffsets[0] = 128;
        this.fOffsets[1] = 192;
        this.fOffsets[2] = 1024;
        this.fOffsets[3] = 1536;
        this.fOffsets[4] = 2304;
        this.fOffsets[5] = 12352;
        this.fOffsets[6] = 12448;
        this.fOffsets[7] = Normalizer2Impl.JAMO_VT;
        for (i = 0; i < 8; i++) {
            this.fTimeStamps[i] = 0;
        }
        for (i = 0; i <= 255; i++) {
            this.fIndexCount[i] = 0;
        }
        this.fTimeStamp = 0;
        this.fCurrentWindow = 0;
        this.fMode = 0;
    }

    private static int makeIndex(int c) {
        if (c >= 192 && c < 320) {
            return 249;
        }
        if (c >= 592 && c < 720) {
            return 250;
        }
        if (c >= 880 && c < 1008) {
            return 251;
        }
        if (c >= 1328 && c < 1424) {
            return 252;
        }
        if (c >= 12352 && c < 12448) {
            return 253;
        }
        if (c >= 12448 && c < 12576) {
            return 254;
        }
        if (c >= 65376 && c < 65439) {
            return 255;
        }
        if (c >= 128 && c < Normalizer2Impl.COMP_1_TRAIL_LIMIT) {
            return (c / 128) & 255;
        }
        if (c < 57344 || c > DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
            return 0;
        }
        return ((c - Hangul.HANGUL_BASE) / 128) & 255;
    }

    private boolean inDynamicWindow(int c, int whichWindow) {
        if (c < this.fOffsets[whichWindow] || c >= this.fOffsets[whichWindow] + 128) {
            return false;
        }
        return true;
    }

    private static boolean inStaticWindow(int c, int whichWindow) {
        if (c < sOffsets[whichWindow] || c >= sOffsets[whichWindow] + 128) {
            return false;
        }
        return true;
    }

    private static boolean isCompressible(int c) {
        return c < Normalizer2Impl.COMP_1_TRAIL_LIMIT || c >= 57344;
    }

    private int findDynamicWindow(int c) {
        for (int i = 7; i >= 0; i--) {
            if (inDynamicWindow(c, i)) {
                int[] iArr = this.fTimeStamps;
                iArr[i] = iArr[i] + 1;
                return i;
            }
        }
        return -1;
    }

    private static int findStaticWindow(int c) {
        for (int i = 7; i >= 0; i--) {
            if (inStaticWindow(c, i)) {
                return i;
            }
        }
        return -1;
    }

    private int getLRDefinedWindow() {
        int leastRU = Integer.MAX_VALUE;
        int whichWindow = -1;
        for (int i = 7; i >= 0; i--) {
            if (this.fTimeStamps[i] < leastRU) {
                leastRU = this.fTimeStamps[i];
                whichWindow = i;
            }
        }
        return whichWindow;
    }
}
