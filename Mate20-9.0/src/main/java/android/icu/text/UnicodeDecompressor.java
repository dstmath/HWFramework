package android.icu.text;

public final class UnicodeDecompressor implements SCSU {
    private static final int BUFSIZE = 3;
    private byte[] fBuffer = new byte[3];
    private int fBufferLength = 0;
    private int fCurrentWindow = 0;
    private int fMode = 0;
    private int[] fOffsets = new int[8];

    public UnicodeDecompressor() {
        reset();
    }

    public static String decompress(byte[] buffer) {
        return new String(decompress(buffer, 0, buffer.length));
    }

    public static char[] decompress(byte[] buffer, int start, int limit) {
        UnicodeDecompressor comp = new UnicodeDecompressor();
        int len = Math.max(2, (limit - start) * 2);
        char[] temp = new char[len];
        int charCount = comp.decompress(buffer, start, limit, null, temp, 0, len);
        char[] result = new char[charCount];
        System.arraycopy(temp, 0, result, 0, charCount);
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x0057, code lost:
        continue;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0057, code lost:
        continue;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x0057, code lost:
        continue;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0066, code lost:
        if (r0 >= r11) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0068, code lost:
        if (r1 >= r14) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006a, code lost:
        r2 = r0 + 1;
        r0 = r9[r0] & 255;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0070, code lost:
        switch(r0) {
            case 224: goto L_0x0121;
            case 225: goto L_0x0121;
            case 226: goto L_0x0121;
            case 227: goto L_0x0121;
            case 228: goto L_0x0121;
            case 229: goto L_0x0121;
            case 230: goto L_0x0121;
            case 231: goto L_0x0121;
            case 232: goto L_0x00f1;
            case 233: goto L_0x00f1;
            case 234: goto L_0x00f1;
            case 235: goto L_0x00f1;
            case 236: goto L_0x00f1;
            case 237: goto L_0x00f1;
            case 238: goto L_0x00f1;
            case 239: goto L_0x00f1;
            case 240: goto L_0x00c4;
            case 241: goto L_0x0087;
            default: goto L_0x0073;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0073, code lost:
        if (r2 < r11) goto L_0x0129;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0075, code lost:
        r2 = r2 - 1;
        java.lang.System.arraycopy(r9, r2, r8.fBuffer, r13, r11 - r2);
        r8.fBufferLength = r11 - r2;
        r2 = r2 + r8.fBufferLength;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0089, code lost:
        if ((r2 + 1) < r11) goto L_0x009d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x008b, code lost:
        r2 = r2 - 1;
        java.lang.System.arraycopy(r9, r2, r8.fBuffer, r13, r11 - r2);
        r8.fBufferLength = r11 - r2;
        r2 = r2 + r8.fBufferLength;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x009d, code lost:
        r5 = r2 + 1;
        r0 = r9[r2] & 255;
        r8.fCurrentWindow = (r0 & 224) >> 5;
        r8.fOffsets[r8.fCurrentWindow] = 65536 + (128 * ((r9[r5] & 255) | ((r0 & 31) << 8)));
        r8.fMode = r13;
        r17 = r0;
        r0 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00c6, code lost:
        if (r2 < (r11 - 1)) goto L_0x00da;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00c8, code lost:
        r2 = r2 - 1;
        java.lang.System.arraycopy(r9, r2, r8.fBuffer, r13, r11 - r2);
        r8.fBufferLength = r11 - r2;
        r2 = r2 + r8.fBufferLength;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00da, code lost:
        r5 = r2 + 1;
        r17 = r9[r2];
        r12[r1] = (char) ((r17 << 8) | (r9[r5] & 255));
        r1 = r1 + 1;
        r0 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00f1, code lost:
        if (r2 < r11) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00f3, code lost:
        r2 = r2 - 1;
        java.lang.System.arraycopy(r9, r2, r8.fBuffer, r13, r11 - r2);
        r8.fBufferLength = r11 - r2;
        r2 = r2 + r8.fBufferLength;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0105, code lost:
        r8.fCurrentWindow = r0 - 232;
        r8.fOffsets[r8.fCurrentWindow] = sOffsetTable[r9[r2] & 255];
        r8.fMode = r13;
        r17 = r0;
        r0 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0121, code lost:
        r8.fCurrentWindow = r0 - 224;
        r8.fMode = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0129, code lost:
        r12[r1] = (char) ((r9[r2] & 255) | (r0 << 8));
        r17 = r0;
        r1 = r1 + 1;
        r0 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x013e, code lost:
        if (r0 >= r11) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0140, code lost:
        if (r1 >= r14) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0142, code lost:
        r2 = r0 + 1;
        r0 = r9[r0] & 255;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0148, code lost:
        switch(r0) {
            case 0: goto L_0x028d;
            case 1: goto L_0x0255;
            case 2: goto L_0x0255;
            case 3: goto L_0x0255;
            case 4: goto L_0x0255;
            case 5: goto L_0x0255;
            case 6: goto L_0x0255;
            case 7: goto L_0x0255;
            case 8: goto L_0x0255;
            case 9: goto L_0x028d;
            case 10: goto L_0x028d;
            case 11: goto L_0x0219;
            case 12: goto L_0x014b;
            case 13: goto L_0x028d;
            case 14: goto L_0x01e8;
            case 15: goto L_0x01df;
            case 16: goto L_0x01d6;
            case 17: goto L_0x01d6;
            case 18: goto L_0x01d6;
            case 19: goto L_0x01d6;
            case 20: goto L_0x01d6;
            case 21: goto L_0x01d6;
            case 22: goto L_0x01d6;
            case 23: goto L_0x01d6;
            case 24: goto L_0x01a9;
            case 25: goto L_0x01a9;
            case 26: goto L_0x01a9;
            case 27: goto L_0x01a9;
            case 28: goto L_0x01a9;
            case 29: goto L_0x01a9;
            case 30: goto L_0x01a9;
            case 31: goto L_0x01a9;
            case 32: goto L_0x028d;
            case 33: goto L_0x028d;
            case 34: goto L_0x028d;
            case 35: goto L_0x028d;
            case 36: goto L_0x028d;
            case 37: goto L_0x028d;
            case 38: goto L_0x028d;
            case 39: goto L_0x028d;
            case 40: goto L_0x028d;
            case 41: goto L_0x028d;
            case 42: goto L_0x028d;
            case 43: goto L_0x028d;
            case 44: goto L_0x028d;
            case 45: goto L_0x028d;
            case 46: goto L_0x028d;
            case 47: goto L_0x028d;
            case 48: goto L_0x028d;
            case 49: goto L_0x028d;
            case 50: goto L_0x028d;
            case 51: goto L_0x028d;
            case 52: goto L_0x028d;
            case 53: goto L_0x028d;
            case 54: goto L_0x028d;
            case 55: goto L_0x028d;
            case 56: goto L_0x028d;
            case 57: goto L_0x028d;
            case 58: goto L_0x028d;
            case 59: goto L_0x028d;
            case 60: goto L_0x028d;
            case 61: goto L_0x028d;
            case 62: goto L_0x028d;
            case 63: goto L_0x028d;
            case 64: goto L_0x028d;
            case 65: goto L_0x028d;
            case 66: goto L_0x028d;
            case 67: goto L_0x028d;
            case 68: goto L_0x028d;
            case 69: goto L_0x028d;
            case 70: goto L_0x028d;
            case 71: goto L_0x028d;
            case 72: goto L_0x028d;
            case 73: goto L_0x028d;
            case 74: goto L_0x028d;
            case 75: goto L_0x028d;
            case 76: goto L_0x028d;
            case 77: goto L_0x028d;
            case 78: goto L_0x028d;
            case 79: goto L_0x028d;
            case 80: goto L_0x028d;
            case 81: goto L_0x028d;
            case 82: goto L_0x028d;
            case 83: goto L_0x028d;
            case 84: goto L_0x028d;
            case 85: goto L_0x028d;
            case 86: goto L_0x028d;
            case 87: goto L_0x028d;
            case 88: goto L_0x028d;
            case 89: goto L_0x028d;
            case 90: goto L_0x028d;
            case 91: goto L_0x028d;
            case 92: goto L_0x028d;
            case 93: goto L_0x028d;
            case 94: goto L_0x028d;
            case 95: goto L_0x028d;
            case 96: goto L_0x028d;
            case 97: goto L_0x028d;
            case 98: goto L_0x028d;
            case 99: goto L_0x028d;
            case 100: goto L_0x028d;
            case 101: goto L_0x028d;
            case 102: goto L_0x028d;
            case 103: goto L_0x028d;
            case 104: goto L_0x028d;
            case 105: goto L_0x028d;
            case 106: goto L_0x028d;
            case 107: goto L_0x028d;
            case 108: goto L_0x028d;
            case 109: goto L_0x028d;
            case 110: goto L_0x028d;
            case 111: goto L_0x028d;
            case 112: goto L_0x028d;
            case 113: goto L_0x028d;
            case 114: goto L_0x028d;
            case 115: goto L_0x028d;
            case 116: goto L_0x028d;
            case 117: goto L_0x028d;
            case 118: goto L_0x028d;
            case 119: goto L_0x028d;
            case 120: goto L_0x028d;
            case 121: goto L_0x028d;
            case 122: goto L_0x028d;
            case 123: goto L_0x028d;
            case 124: goto L_0x028d;
            case 125: goto L_0x028d;
            case 126: goto L_0x028d;
            case 127: goto L_0x028d;
            case 128: goto L_0x014e;
            case 129: goto L_0x014e;
            case 130: goto L_0x014e;
            case 131: goto L_0x014e;
            case 132: goto L_0x014e;
            case 133: goto L_0x014e;
            case 134: goto L_0x014e;
            case 135: goto L_0x014e;
            case 136: goto L_0x014e;
            case 137: goto L_0x014e;
            case 138: goto L_0x014e;
            case 139: goto L_0x014e;
            case 140: goto L_0x014e;
            case 141: goto L_0x014e;
            case 142: goto L_0x014e;
            case 143: goto L_0x014e;
            case 144: goto L_0x014e;
            case 145: goto L_0x014e;
            case 146: goto L_0x014e;
            case 147: goto L_0x014e;
            case 148: goto L_0x014e;
            case 149: goto L_0x014e;
            case 150: goto L_0x014e;
            case 151: goto L_0x014e;
            case 152: goto L_0x014e;
            case 153: goto L_0x014e;
            case 154: goto L_0x014e;
            case 155: goto L_0x014e;
            case 156: goto L_0x014e;
            case 157: goto L_0x014e;
            case 158: goto L_0x014e;
            case 159: goto L_0x014e;
            case 160: goto L_0x014e;
            case 161: goto L_0x014e;
            case 162: goto L_0x014e;
            case 163: goto L_0x014e;
            case 164: goto L_0x014e;
            case 165: goto L_0x014e;
            case 166: goto L_0x014e;
            case 167: goto L_0x014e;
            case 168: goto L_0x014e;
            case 169: goto L_0x014e;
            case 170: goto L_0x014e;
            case 171: goto L_0x014e;
            case 172: goto L_0x014e;
            case 173: goto L_0x014e;
            case 174: goto L_0x014e;
            case 175: goto L_0x014e;
            case 176: goto L_0x014e;
            case 177: goto L_0x014e;
            case 178: goto L_0x014e;
            case 179: goto L_0x014e;
            case 180: goto L_0x014e;
            case 181: goto L_0x014e;
            case 182: goto L_0x014e;
            case 183: goto L_0x014e;
            case 184: goto L_0x014e;
            case 185: goto L_0x014e;
            case 186: goto L_0x014e;
            case 187: goto L_0x014e;
            case 188: goto L_0x014e;
            case 189: goto L_0x014e;
            case 190: goto L_0x014e;
            case 191: goto L_0x014e;
            case 192: goto L_0x014e;
            case 193: goto L_0x014e;
            case 194: goto L_0x014e;
            case 195: goto L_0x014e;
            case 196: goto L_0x014e;
            case 197: goto L_0x014e;
            case 198: goto L_0x014e;
            case 199: goto L_0x014e;
            case 200: goto L_0x014e;
            case 201: goto L_0x014e;
            case 202: goto L_0x014e;
            case 203: goto L_0x014e;
            case 204: goto L_0x014e;
            case 205: goto L_0x014e;
            case 206: goto L_0x014e;
            case 207: goto L_0x014e;
            case 208: goto L_0x014e;
            case 209: goto L_0x014e;
            case 210: goto L_0x014e;
            case 211: goto L_0x014e;
            case 212: goto L_0x014e;
            case 213: goto L_0x014e;
            case 214: goto L_0x014e;
            case 215: goto L_0x014e;
            case 216: goto L_0x014e;
            case 217: goto L_0x014e;
            case 218: goto L_0x014e;
            case 219: goto L_0x014e;
            case 220: goto L_0x014e;
            case 221: goto L_0x014e;
            case 222: goto L_0x014e;
            case 223: goto L_0x014e;
            case 224: goto L_0x014e;
            case 225: goto L_0x014e;
            case 226: goto L_0x014e;
            case 227: goto L_0x014e;
            case 228: goto L_0x014e;
            case 229: goto L_0x014e;
            case 230: goto L_0x014e;
            case 231: goto L_0x014e;
            case 232: goto L_0x014e;
            case 233: goto L_0x014e;
            case 234: goto L_0x014e;
            case 235: goto L_0x014e;
            case 236: goto L_0x014e;
            case 237: goto L_0x014e;
            case 238: goto L_0x014e;
            case 239: goto L_0x014e;
            case 240: goto L_0x014e;
            case 241: goto L_0x014e;
            case 242: goto L_0x014e;
            case 243: goto L_0x014e;
            case 244: goto L_0x014e;
            case 245: goto L_0x014e;
            case 246: goto L_0x014e;
            case 247: goto L_0x014e;
            case 248: goto L_0x014e;
            case 249: goto L_0x014e;
            case 250: goto L_0x014e;
            case 251: goto L_0x014e;
            case 252: goto L_0x014e;
            case 253: goto L_0x014e;
            case 254: goto L_0x014e;
            case 255: goto L_0x014e;
            default: goto L_0x014b;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x014b, code lost:
        r6 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0157, code lost:
        if (r8.fOffsets[r8.fCurrentWindow] > 65535) goto L_0x016d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0159, code lost:
        r5 = r1 + 1;
        r12[r1] = (char) ((r8.fOffsets[r8.fCurrentWindow] + r0) - 128);
        r17 = r0;
        r0 = r2;
        r6 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x016a, code lost:
        r1 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x016f, code lost:
        if ((r1 + 1) < r14) goto L_0x0183;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0171, code lost:
        r2 = r2 - 1;
        java.lang.System.arraycopy(r9, r2, r8.fBuffer, r13, r11 - r2);
        r8.fBufferLength = r11 - r2;
        r2 = r2 + r8.fBufferLength;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0183, code lost:
        r5 = r8.fOffsets[r8.fCurrentWindow] - r3;
        r6 = r1 + 1;
        r12[r1] = (char) (55296 + (r5 >> 10));
        r1 = r6 + 1;
        r12[r6] = (char) ((android.icu.text.UTF16.TRAIL_SURROGATE_MIN_VALUE + (r5 & dalvik.bytecode.Opcodes.OP_NEW_INSTANCE_JUMBO)) + (r0 & 127));
        r17 = r0;
        r0 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x01a9, code lost:
        if (r2 < r11) goto L_0x01bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x01ab, code lost:
        r2 = r2 - 1;
        java.lang.System.arraycopy(r9, r2, r8.fBuffer, r13, r11 - r2);
        r8.fBufferLength = r11 - r2;
        r2 = r2 + r8.fBufferLength;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x01bd, code lost:
        r8.fCurrentWindow = r0 - 24;
        r8.fOffsets[r8.fCurrentWindow] = sOffsetTable[r9[r2] & 255];
        r17 = r0;
        r0 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x01d6, code lost:
        r8.fCurrentWindow = r0 - 16;
        r6 = 65536;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x01df, code lost:
        r8.fMode = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x01e3, code lost:
        r17 = r0;
        r0 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x01ea, code lost:
        if ((r2 + 1) < r11) goto L_0x01fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x01ec, code lost:
        r2 = r2 - 1;
        java.lang.System.arraycopy(r9, r2, r8.fBuffer, r13, r11 - r2);
        r8.fBufferLength = r11 - r2;
        r2 = r2 + r8.fBufferLength;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x01fe, code lost:
        r3 = r2 + 1;
        r0 = r9[r2];
        r12[r1] = (char) ((r9[r3] & 255) | (r0 << 8));
        r17 = r0;
        r1 = r1 + 1;
        r0 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0215, code lost:
        r6 = 65536;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x021b, code lost:
        if ((r2 + 1) < r11) goto L_0x022f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x021d, code lost:
        r2 = r2 - 1;
        java.lang.System.arraycopy(r9, r2, r8.fBuffer, r13, r11 - r2);
        r8.fBufferLength = r11 - r2;
        r2 = r2 + r8.fBufferLength;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x022f, code lost:
        r3 = r2 + 1;
        r0 = r9[r2] & 255;
        r8.fCurrentWindow = (r0 & 224) >> 5;
        r6 = 65536;
        r8.fOffsets[r8.fCurrentWindow] = (((r9[r3] & 255) | ((r0 & 31) << 8)) * 128) + 65536;
        r17 = r0;
        r0 = r3 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0255, code lost:
        r6 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0256, code lost:
        if (r2 < r11) goto L_0x0269;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0258, code lost:
        r2 = r2 - 1;
        java.lang.System.arraycopy(r9, r2, r8.fBuffer, r13, r11 - r2);
        r8.fBufferLength = r11 - r2;
        r2 = r2 + r8.fBufferLength;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0269, code lost:
        r3 = r2 + 1;
        r2 = r9[r2] & 255;
        r5 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0271, code lost:
        if (r2 < 0) goto L_0x027c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0273, code lost:
        if (r2 >= 128) goto L_0x027c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0275, code lost:
        r7 = sOffsets[r0 - 1];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x027c, code lost:
        r7 = r8.fOffsets[r0 - 1] - 128;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0283, code lost:
        r12[r1] = (char) (r7 + r2);
        r17 = r0;
        r0 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x028d, code lost:
        r6 = r3;
        r12[r1] = (char) r0;
        r17 = r0;
        r0 = r2;
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0299, code lost:
        r17 = r0;
        r0 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x029c, code lost:
        r3 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x0057, code lost:
        continue;
     */
    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r17v20, types: [byte, int] */
    public int decompress(byte[] byteBuffer, int byteBufferStart, int byteBufferLimit, int[] bytesRead, char[] charBuffer, int charBufferStart, int charBufferLimit) {
        int i;
        int bytePos;
        byte[] bArr = byteBuffer;
        int i2 = byteBufferStart;
        int i3 = byteBufferLimit;
        char[] cArr = charBuffer;
        int i4 = charBufferLimit;
        int bytePos2 = i2;
        int ucPos = charBufferStart;
        int aByte = 0;
        if (cArr.length < 2 || i4 - charBufferStart < 2) {
            throw new IllegalArgumentException("charBuffer.length < 2");
        }
        if (this.fBufferLength > 0) {
            int newBytes = 0;
            if (this.fBufferLength != 3) {
                int newBytes2 = this.fBuffer.length - this.fBufferLength;
                if (i3 - i2 < newBytes2) {
                    newBytes = i3 - i2;
                } else {
                    newBytes = newBytes2;
                }
                System.arraycopy(bArr, i2, this.fBuffer, this.fBufferLength, newBytes);
            }
            this.fBufferLength = 0;
            i = 0;
            ucPos += decompress(this.fBuffer, 0, this.fBuffer.length, null, cArr, charBufferStart, i4);
            bytePos2 += newBytes;
        } else {
            i = 0;
        }
        int aByte2 = bytePos2;
        int ucPos2 = ucPos;
        while (true) {
            if (aByte2 < i3 && ucPos2 < i4) {
                int bytePos3 = 65536;
                switch (this.fMode) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            } else {
                bytePos = aByte2;
                int bytePos4 = aByte;
            }
        }
        if (bytesRead != null) {
            bytesRead[i] = bytePos - i2;
        }
        return ucPos2 - charBufferStart;
    }

    public void reset() {
        this.fOffsets[0] = 128;
        this.fOffsets[1] = 192;
        this.fOffsets[2] = 1024;
        this.fOffsets[3] = 1536;
        this.fOffsets[4] = 2304;
        this.fOffsets[5] = 12352;
        this.fOffsets[6] = 12448;
        this.fOffsets[7] = 65280;
        this.fCurrentWindow = 0;
        this.fMode = 0;
        this.fBufferLength = 0;
    }
}
