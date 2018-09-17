package android.icu.text;

import android.icu.impl.Normalizer2Impl;

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

    /* JADX WARNING: Missing block: B:22:0x007c, code:
            r18 = r17;
            r12 = r11;
     */
    /* JADX WARNING: Missing block: B:23:0x007e, code:
            if (r12 >= r22) goto L_0x0095;
     */
    /* JADX WARNING: Missing block: B:25:0x0084, code:
            if (r18 >= r26) goto L_0x0095;
     */
    /* JADX WARNING: Missing block: B:26:0x0086, code:
            r11 = r12 + 1;
            r10 = r20[r12] & 255;
     */
    /* JADX WARNING: Missing block: B:27:0x008c, code:
            switch(r10) {
                case 0: goto L_0x0112;
                case 1: goto L_0x0156;
                case 2: goto L_0x0156;
                case 3: goto L_0x0156;
                case 4: goto L_0x0156;
                case 5: goto L_0x0156;
                case 6: goto L_0x0156;
                case 7: goto L_0x0156;
                case 8: goto L_0x0156;
                case 9: goto L_0x0112;
                case 10: goto L_0x0112;
                case 11: goto L_0x01e7;
                case 12: goto L_0x0236;
                case 13: goto L_0x0112;
                case 14: goto L_0x0119;
                case 15: goto L_0x014f;
                case 16: goto L_0x019d;
                case 17: goto L_0x019d;
                case 18: goto L_0x019d;
                case 19: goto L_0x019d;
                case 20: goto L_0x019d;
                case 21: goto L_0x019d;
                case 22: goto L_0x019d;
                case 23: goto L_0x019d;
                case 24: goto L_0x01a7;
                case 25: goto L_0x01a7;
                case 26: goto L_0x01a7;
                case 27: goto L_0x01a7;
                case 28: goto L_0x01a7;
                case 29: goto L_0x01a7;
                case 30: goto L_0x01a7;
                case 31: goto L_0x01a7;
                case 32: goto L_0x0112;
                case 33: goto L_0x0112;
                case 34: goto L_0x0112;
                case 35: goto L_0x0112;
                case 36: goto L_0x0112;
                case 37: goto L_0x0112;
                case 38: goto L_0x0112;
                case 39: goto L_0x0112;
                case 40: goto L_0x0112;
                case 41: goto L_0x0112;
                case 42: goto L_0x0112;
                case 43: goto L_0x0112;
                case 44: goto L_0x0112;
                case 45: goto L_0x0112;
                case 46: goto L_0x0112;
                case 47: goto L_0x0112;
                case 48: goto L_0x0112;
                case 49: goto L_0x0112;
                case 50: goto L_0x0112;
                case 51: goto L_0x0112;
                case 52: goto L_0x0112;
                case 53: goto L_0x0112;
                case 54: goto L_0x0112;
                case 55: goto L_0x0112;
                case 56: goto L_0x0112;
                case 57: goto L_0x0112;
                case 58: goto L_0x0112;
                case 59: goto L_0x0112;
                case 60: goto L_0x0112;
                case 61: goto L_0x0112;
                case 62: goto L_0x0112;
                case 63: goto L_0x0112;
                case 64: goto L_0x0112;
                case 65: goto L_0x0112;
                case 66: goto L_0x0112;
                case 67: goto L_0x0112;
                case 68: goto L_0x0112;
                case 69: goto L_0x0112;
                case 70: goto L_0x0112;
                case 71: goto L_0x0112;
                case 72: goto L_0x0112;
                case 73: goto L_0x0112;
                case 74: goto L_0x0112;
                case 75: goto L_0x0112;
                case 76: goto L_0x0112;
                case 77: goto L_0x0112;
                case 78: goto L_0x0112;
                case 79: goto L_0x0112;
                case 80: goto L_0x0112;
                case 81: goto L_0x0112;
                case 82: goto L_0x0112;
                case 83: goto L_0x0112;
                case 84: goto L_0x0112;
                case 85: goto L_0x0112;
                case 86: goto L_0x0112;
                case 87: goto L_0x0112;
                case 88: goto L_0x0112;
                case 89: goto L_0x0112;
                case 90: goto L_0x0112;
                case 91: goto L_0x0112;
                case 92: goto L_0x0112;
                case 93: goto L_0x0112;
                case 94: goto L_0x0112;
                case 95: goto L_0x0112;
                case 96: goto L_0x0112;
                case 97: goto L_0x0112;
                case 98: goto L_0x0112;
                case 99: goto L_0x0112;
                case 100: goto L_0x0112;
                case 101: goto L_0x0112;
                case 102: goto L_0x0112;
                case 103: goto L_0x0112;
                case 104: goto L_0x0112;
                case 105: goto L_0x0112;
                case 106: goto L_0x0112;
                case 107: goto L_0x0112;
                case 108: goto L_0x0112;
                case 109: goto L_0x0112;
                case 110: goto L_0x0112;
                case 111: goto L_0x0112;
                case 112: goto L_0x0112;
                case 113: goto L_0x0112;
                case 114: goto L_0x0112;
                case 115: goto L_0x0112;
                case 116: goto L_0x0112;
                case 117: goto L_0x0112;
                case 118: goto L_0x0112;
                case 119: goto L_0x0112;
                case 120: goto L_0x0112;
                case 121: goto L_0x0112;
                case 122: goto L_0x0112;
                case 123: goto L_0x0112;
                case 124: goto L_0x0112;
                case 125: goto L_0x0112;
                case 126: goto L_0x0112;
                case 127: goto L_0x0112;
                case 128: goto L_0x0099;
                case 129: goto L_0x0099;
                case 130: goto L_0x0099;
                case 131: goto L_0x0099;
                case 132: goto L_0x0099;
                case 133: goto L_0x0099;
                case 134: goto L_0x0099;
                case 135: goto L_0x0099;
                case 136: goto L_0x0099;
                case 137: goto L_0x0099;
                case 138: goto L_0x0099;
                case 139: goto L_0x0099;
                case 140: goto L_0x0099;
                case 141: goto L_0x0099;
                case 142: goto L_0x0099;
                case 143: goto L_0x0099;
                case 144: goto L_0x0099;
                case 145: goto L_0x0099;
                case 146: goto L_0x0099;
                case 147: goto L_0x0099;
                case 148: goto L_0x0099;
                case 149: goto L_0x0099;
                case 150: goto L_0x0099;
                case 151: goto L_0x0099;
                case 152: goto L_0x0099;
                case 153: goto L_0x0099;
                case 154: goto L_0x0099;
                case 155: goto L_0x0099;
                case 156: goto L_0x0099;
                case 157: goto L_0x0099;
                case 158: goto L_0x0099;
                case 159: goto L_0x0099;
                case 160: goto L_0x0099;
                case 161: goto L_0x0099;
                case 162: goto L_0x0099;
                case 163: goto L_0x0099;
                case 164: goto L_0x0099;
                case 165: goto L_0x0099;
                case 166: goto L_0x0099;
                case 167: goto L_0x0099;
                case 168: goto L_0x0099;
                case 169: goto L_0x0099;
                case 170: goto L_0x0099;
                case 171: goto L_0x0099;
                case 172: goto L_0x0099;
                case 173: goto L_0x0099;
                case 174: goto L_0x0099;
                case 175: goto L_0x0099;
                case 176: goto L_0x0099;
                case 177: goto L_0x0099;
                case 178: goto L_0x0099;
                case 179: goto L_0x0099;
                case 180: goto L_0x0099;
                case 181: goto L_0x0099;
                case 182: goto L_0x0099;
                case 183: goto L_0x0099;
                case 184: goto L_0x0099;
                case 185: goto L_0x0099;
                case 186: goto L_0x0099;
                case 187: goto L_0x0099;
                case 188: goto L_0x0099;
                case 189: goto L_0x0099;
                case 190: goto L_0x0099;
                case 191: goto L_0x0099;
                case 192: goto L_0x0099;
                case 193: goto L_0x0099;
                case 194: goto L_0x0099;
                case 195: goto L_0x0099;
                case 196: goto L_0x0099;
                case 197: goto L_0x0099;
                case 198: goto L_0x0099;
                case 199: goto L_0x0099;
                case 200: goto L_0x0099;
                case 201: goto L_0x0099;
                case 202: goto L_0x0099;
                case 203: goto L_0x0099;
                case 204: goto L_0x0099;
                case 205: goto L_0x0099;
                case 206: goto L_0x0099;
                case 207: goto L_0x0099;
                case 208: goto L_0x0099;
                case 209: goto L_0x0099;
                case 210: goto L_0x0099;
                case 211: goto L_0x0099;
                case 212: goto L_0x0099;
                case 213: goto L_0x0099;
                case 214: goto L_0x0099;
                case 215: goto L_0x0099;
                case 216: goto L_0x0099;
                case 217: goto L_0x0099;
                case 218: goto L_0x0099;
                case 219: goto L_0x0099;
                case 220: goto L_0x0099;
                case 221: goto L_0x0099;
                case 222: goto L_0x0099;
                case 223: goto L_0x0099;
                case 224: goto L_0x0099;
                case 225: goto L_0x0099;
                case 226: goto L_0x0099;
                case 227: goto L_0x0099;
                case 228: goto L_0x0099;
                case 229: goto L_0x0099;
                case 230: goto L_0x0099;
                case 231: goto L_0x0099;
                case 232: goto L_0x0099;
                case 233: goto L_0x0099;
                case 234: goto L_0x0099;
                case 235: goto L_0x0099;
                case 236: goto L_0x0099;
                case 237: goto L_0x0099;
                case 238: goto L_0x0099;
                case 239: goto L_0x0099;
                case 240: goto L_0x0099;
                case 241: goto L_0x0099;
                case 242: goto L_0x0099;
                case 243: goto L_0x0099;
                case 244: goto L_0x0099;
                case 245: goto L_0x0099;
                case 246: goto L_0x0099;
                case 247: goto L_0x0099;
                case 248: goto L_0x0099;
                case 249: goto L_0x0099;
                case 250: goto L_0x0099;
                case 251: goto L_0x0099;
                case 252: goto L_0x0099;
                case 253: goto L_0x0099;
                case 254: goto L_0x0099;
                case 255: goto L_0x0099;
                default: goto L_0x008f;
            };
     */
    /* JADX WARNING: Missing block: B:28:0x008f, code:
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:30:0x0095, code:
            r11 = r12;
     */
    /* JADX WARNING: Missing block: B:31:0x0096, code:
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:33:0x00a6, code:
            if (r19.fOffsets[r19.fCurrentWindow] > 65535) goto L_0x00bb;
     */
    /* JADX WARNING: Missing block: B:34:0x00a8, code:
            r17 = r18 + 1;
            r24[r18] = (char) ((r19.fOffsets[r19.fCurrentWindow] + r10) - 128);
     */
    /* JADX WARNING: Missing block: B:36:0x00bf, code:
            if ((r18 + 1) < r26) goto L_0x00e6;
     */
    /* JADX WARNING: Missing block: B:37:0x00c1, code:
            r11 = r11 - 1;
            java.lang.System.arraycopy(r20, r11, r19.fBuffer, 0, r22 - r11);
            r19.fBufferLength = r22 - r11;
            r11 = r11 + r19.fBufferLength;
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:42:0x00e6, code:
            r16 = r19.fOffsets[r19.fCurrentWindow] - 65536;
            r17 = r18 + 1;
            r24[r18] = (char) ((r16 >> 10) + 55296);
            r18 = r17 + 1;
            r24[r17] = (char) (((r16 & dalvik.bytecode.Opcodes.OP_NEW_INSTANCE_JUMBO) + android.icu.text.UTF16.TRAIL_SURROGATE_MIN_VALUE) + (r10 & 127));
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:43:0x0112, code:
            r17 = r18 + 1;
            r24[r18] = (char) r10;
     */
    /* JADX WARNING: Missing block: B:45:0x011d, code:
            if ((r11 + 1) < r22) goto L_0x013b;
     */
    /* JADX WARNING: Missing block: B:46:0x011f, code:
            r11 = r11 - 1;
            java.lang.System.arraycopy(r20, r11, r19.fBuffer, 0, r22 - r11);
            r19.fBufferLength = r22 - r11;
            r11 = r11 + r19.fBufferLength;
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:47:0x013b, code:
            r12 = r11 + 1;
            r17 = r18 + 1;
            r11 = r12 + 1;
            r24[r18] = (char) ((r20[r11] << 8) | (r20[r12] & 255));
     */
    /* JADX WARNING: Missing block: B:48:0x014f, code:
            r19.fMode = 1;
     */
    /* JADX WARNING: Missing block: B:50:0x0158, code:
            if (r11 < r22) goto L_0x0177;
     */
    /* JADX WARNING: Missing block: B:51:0x015a, code:
            r11 = r11 - 1;
            java.lang.System.arraycopy(r20, r11, r19.fBuffer, 0, r22 - r11);
            r19.fBufferLength = r22 - r11;
            r11 = r11 + r19.fBufferLength;
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:52:0x0177, code:
            r12 = r11 + 1;
            r14 = r20[r11] & 255;
            r17 = r18 + 1;
     */
    /* JADX WARNING: Missing block: B:53:0x017f, code:
            if (r14 < 0) goto L_0x0192;
     */
    /* JADX WARNING: Missing block: B:55:0x0183, code:
            if (r14 >= 128) goto L_0x0192;
     */
    /* JADX WARNING: Missing block: B:56:0x0185, code:
            r2 = sOffsets[r10 - 1];
     */
    /* JADX WARNING: Missing block: B:57:0x018b, code:
            r24[r18] = (char) (r2 + r14);
            r11 = r12;
     */
    /* JADX WARNING: Missing block: B:58:0x0192, code:
            r2 = r19.fOffsets[r10 - 1] - 128;
     */
    /* JADX WARNING: Missing block: B:59:0x019d, code:
            r19.fCurrentWindow = r10 - 16;
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:61:0x01a9, code:
            if (r11 < r22) goto L_0x01c8;
     */
    /* JADX WARNING: Missing block: B:62:0x01ab, code:
            r11 = r11 - 1;
            java.lang.System.arraycopy(r20, r11, r19.fBuffer, 0, r22 - r11);
            r19.fBufferLength = r22 - r11;
            r11 = r11 + r19.fBufferLength;
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:63:0x01c8, code:
            r19.fCurrentWindow = r10 - 24;
            r12 = r11 + 1;
            r19.fOffsets[r19.fCurrentWindow] = sOffsetTable[r20[r11] & 255];
            r17 = r18;
            r11 = r12;
     */
    /* JADX WARNING: Missing block: B:65:0x01eb, code:
            if ((r11 + 1) < r22) goto L_0x020a;
     */
    /* JADX WARNING: Missing block: B:66:0x01ed, code:
            r11 = r11 - 1;
            java.lang.System.arraycopy(r20, r11, r19.fBuffer, 0, r22 - r11);
            r19.fBufferLength = r22 - r11;
            r11 = r11 + r19.fBufferLength;
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:67:0x020a, code:
            r12 = r11 + 1;
            r10 = r20[r11] & 255;
            r19.fCurrentWindow = (r10 & 224) >> 5;
            r11 = r12 + 1;
            r19.fOffsets[r19.fCurrentWindow] = ((((r10 & 31) << 8) | (r20[r12] & 255)) * 128) + 65536;
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:68:0x0236, code:
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:69:0x023d, code:
            r18 = r17;
            r12 = r11;
     */
    /* JADX WARNING: Missing block: B:70:0x023f, code:
            if (r12 >= r22) goto L_0x0271;
     */
    /* JADX WARNING: Missing block: B:72:0x0245, code:
            if (r18 >= r26) goto L_0x0271;
     */
    /* JADX WARNING: Missing block: B:73:0x0247, code:
            r11 = r12 + 1;
            r10 = r20[r12] & 255;
     */
    /* JADX WARNING: Missing block: B:74:0x024d, code:
            switch(r10) {
                case 224: goto L_0x030a;
                case 225: goto L_0x030a;
                case 226: goto L_0x030a;
                case 227: goto L_0x030a;
                case 228: goto L_0x030a;
                case 229: goto L_0x030a;
                case 230: goto L_0x030a;
                case 231: goto L_0x030a;
                case 232: goto L_0x0276;
                case 233: goto L_0x0276;
                case 234: goto L_0x0276;
                case 235: goto L_0x0276;
                case 236: goto L_0x0276;
                case 237: goto L_0x0276;
                case 238: goto L_0x0276;
                case 239: goto L_0x0276;
                case 240: goto L_0x0317;
                case 241: goto L_0x02b8;
                default: goto L_0x0250;
            };
     */
    /* JADX WARNING: Missing block: B:76:0x0252, code:
            if (r11 < r22) goto L_0x034f;
     */
    /* JADX WARNING: Missing block: B:77:0x0254, code:
            r11 = r11 - 1;
            java.lang.System.arraycopy(r20, r11, r19.fBuffer, 0, r22 - r11);
            r19.fBufferLength = r22 - r11;
            r11 = r11 + r19.fBufferLength;
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:78:0x0271, code:
            r11 = r12;
     */
    /* JADX WARNING: Missing block: B:79:0x0272, code:
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:81:0x0278, code:
            if (r11 < r22) goto L_0x0297;
     */
    /* JADX WARNING: Missing block: B:82:0x027a, code:
            r11 = r11 - 1;
            java.lang.System.arraycopy(r20, r11, r19.fBuffer, 0, r22 - r11);
            r19.fBufferLength = r22 - r11;
            r11 = r11 + r19.fBufferLength;
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:83:0x0297, code:
            r19.fCurrentWindow = r10 - 232;
            r12 = r11 + 1;
            r19.fOffsets[r19.fCurrentWindow] = sOffsetTable[r20[r11] & 255];
            r19.fMode = 0;
            r11 = r12;
     */
    /* JADX WARNING: Missing block: B:85:0x02bc, code:
            if ((r11 + 1) < r22) goto L_0x02db;
     */
    /* JADX WARNING: Missing block: B:86:0x02be, code:
            r11 = r11 - 1;
            java.lang.System.arraycopy(r20, r11, r19.fBuffer, 0, r22 - r11);
            r19.fBufferLength = r22 - r11;
            r11 = r11 + r19.fBufferLength;
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:87:0x02db, code:
            r12 = r11 + 1;
            r10 = r20[r11] & 255;
            r19.fCurrentWindow = (r10 & 224) >> 5;
            r11 = r12 + 1;
            r19.fOffsets[r19.fCurrentWindow] = ((((r10 & 31) << 8) | (r20[r12] & 255)) * 128) + 65536;
            r19.fMode = 0;
     */
    /* JADX WARNING: Missing block: B:88:0x030a, code:
            r19.fCurrentWindow = r10 - 224;
            r19.fMode = 0;
     */
    /* JADX WARNING: Missing block: B:90:0x0319, code:
            if (r11 < (r22 - 1)) goto L_0x0338;
     */
    /* JADX WARNING: Missing block: B:91:0x031b, code:
            r11 = r11 - 1;
            java.lang.System.arraycopy(r20, r11, r19.fBuffer, 0, r22 - r11);
            r19.fBufferLength = r22 - r11;
            r11 = r11 + r19.fBufferLength;
            r17 = r18;
     */
    /* JADX WARNING: Missing block: B:92:0x0338, code:
            r12 = r11 + 1;
            r17 = r18 + 1;
            r11 = r12 + 1;
            r24[r18] = (char) ((r20[r11] << 8) | (r20[r12] & 255));
     */
    /* JADX WARNING: Missing block: B:94:0x034f, code:
            r17 = r18 + 1;
            r12 = r11 + 1;
            r24[r18] = (char) ((r10 << 8) | (r20[r11] & 255));
            r11 = r12;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int decompress(byte[] byteBuffer, int byteBufferStart, int byteBufferLimit, int[] bytesRead, char[] charBuffer, int charBufferStart, int charBufferLimit) {
        int bytePos = byteBufferStart;
        int ucPos = charBufferStart;
        if (charBuffer.length < 2 || charBufferLimit - charBufferStart < 2) {
            throw new IllegalArgumentException("charBuffer.length < 2");
        }
        if (this.fBufferLength > 0) {
            int newBytes = 0;
            if (this.fBufferLength != 3) {
                newBytes = this.fBuffer.length - this.fBufferLength;
                if (byteBufferLimit - byteBufferStart < newBytes) {
                    newBytes = byteBufferLimit - byteBufferStart;
                }
                System.arraycopy(byteBuffer, byteBufferStart, this.fBuffer, this.fBufferLength, newBytes);
            }
            this.fBufferLength = 0;
            ucPos = charBufferStart + decompress(this.fBuffer, 0, this.fBuffer.length, null, charBuffer, charBufferStart, charBufferLimit);
            bytePos = byteBufferStart + newBytes;
        }
        while (bytePos < byteBufferLimit && ucPos < charBufferLimit) {
            switch (this.fMode) {
                case 0:
                    break;
                case 1:
                    break;
                default:
                    break;
            }
        }
        if (bytesRead != null) {
            bytesRead[0] = bytePos - byteBufferStart;
        }
        return ucPos - charBufferStart;
    }

    public void reset() {
        this.fOffsets[0] = 128;
        this.fOffsets[1] = 192;
        this.fOffsets[2] = 1024;
        this.fOffsets[3] = 1536;
        this.fOffsets[4] = 2304;
        this.fOffsets[5] = 12352;
        this.fOffsets[6] = 12448;
        this.fOffsets[7] = Normalizer2Impl.JAMO_VT;
        this.fCurrentWindow = 0;
        this.fMode = 0;
        this.fBufferLength = 0;
    }
}
