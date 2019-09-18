package android.icu.text;

import android.icu.impl.Normalizer2Impl;

public final class UnicodeCompressor implements SCSU {
    private static boolean[] sSingleTagTable = {false, true, true, true, true, true, true, true, true, false, false, true, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
    private static boolean[] sUnicodeTagTable = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false};
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
        int len = Math.max(4, (3 * (limit - start)) + 1);
        byte[] temp = new byte[len];
        int byteCount = comp.compress(buffer, start, limit, null, temp, 0, len);
        byte[] result = new byte[byteCount];
        System.arraycopy(temp, 0, result, 0, byteCount);
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0265, code lost:
        r16 = r5;
        r5 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x02df, code lost:
        r16 = r5;
        r5 = r7;
        r10 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x0439, code lost:
        r16 = r5;
        r25 = r8;
        r12 = r31;
        r5 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:0x0499, code lost:
        r12 = r31;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b4, code lost:
        r16 = r6;
        r6 = r7;
        r25 = r18;
        r10 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01a0, code lost:
        if ((r7 + 2) < r4) goto L_0x01ac;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01a2, code lost:
        r5 = r5 - 1;
        r16 = r6;
        r6 = r7;
        r25 = r8;
        r11 = r21;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01ac, code lost:
        r9 = getLRDefinedWindow();
        r11 = r7 + 1;
        r3[r7] = (byte) (232 + r9);
        r7 = r11 + 1;
        r3[r11] = (byte) r8;
        r11 = r7 + 1;
        r24 = r10;
        r3[r7] = (byte) ((r6 - sOffsetTable[r8]) + 128);
        r0.fOffsets[r9] = sOffsetTable[r8];
        r0.fCurrentWindow = r9;
        r7 = r0.fTimeStamps;
        r10 = r0.fTimeStamp + 1;
        r0.fTimeStamp = r10;
        r7[r9] = r10;
        r0.fMode = 0;
        r7 = r6;
        r6 = r11;
        r12 = r22;
        r10 = r24;
        r11 = r9;
        r9 = r23;
     */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r10v15, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r10v74, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r9v16, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r9v3, types: [char] */
    public int compress(char[] charBuffer, int charBufferStart, int charBufferLimit, int[] charsRead, byte[] byteBuffer, int byteBufferStart, int byteBufferLimit) {
        int ucPos;
        int hiByte;
        int ucPos2;
        int nextUC;
        int forwardUC;
        int curUC;
        int bytePos;
        int ucPos3;
        int bytePos2;
        int hiByte2;
        int bytePos3;
        int forwardUC2;
        int bytePos4;
        int curUC2;
        int ucPos4;
        int curIndex;
        int forwardUC3;
        char forwardUC4;
        int ucPos5;
        int ucPos6;
        int nextUC2;
        int nextUC3;
        int hiByte3;
        int forwardUC5;
        int curIndex2;
        int bytePos5;
        int nextUC4;
        int forwardUC6;
        int bytePos6 = charBufferLimit;
        byte[] bArr = byteBuffer;
        int i = byteBufferLimit;
        int curUC3 = -1;
        int curIndex3 = -1;
        int forwardUC7 = 65535;
        int hiByte4 = 0;
        int bytePos7 = byteBufferStart;
        int ucPos7 = charBufferStart;
        if (bArr.length < 4 || i - byteBufferStart < 4) {
            throw new IllegalArgumentException("byteBuffer.length < 4");
        }
        int bytePos8 = bytePos7;
        int ucPos8 = ucPos7;
        while (true) {
            int curUC4 = curUC3;
            if (ucPos8 >= bytePos6 || bytePos8 >= i) {
                hiByte = hiByte4;
                int i2 = curIndex3;
                ucPos2 = ucPos8;
            } else {
                int ucPos9 = ucPos8;
                switch (this.fMode) {
                    case 0:
                        ucPos8 = ucPos9;
                        while (ucPos8 < bytePos6 && bytePos8 < i) {
                            int ucPos10 = ucPos8 + 1;
                            char ucPos11 = charBuffer[ucPos8];
                            if (ucPos10 < bytePos6) {
                                nextUC = charBuffer[ucPos10];
                            } else {
                                nextUC = -1;
                            }
                            int curIndex4 = curIndex3;
                            if (ucPos11 < 128) {
                                int loByte = ucPos11 & 255;
                                if (sSingleTagTable[loByte]) {
                                    if (bytePos8 + 1 >= i) {
                                        ucPos5 = ucPos10 - 1;
                                        break;
                                    } else {
                                        bArr[bytePos8] = 1;
                                        bytePos8++;
                                    }
                                }
                                bArr[bytePos8] = (byte) loByte;
                                curUC = ucPos11;
                                ucPos8 = ucPos10;
                                curIndex3 = curIndex4;
                                bytePos = bytePos8 + 1;
                            } else {
                                if (!inDynamicWindow(ucPos11, this.fCurrentWindow)) {
                                    forwardUC = forwardUC7;
                                    if (isCompressible(ucPos11)) {
                                        int curIndex5 = findDynamicWindow(ucPos11);
                                        int whichWindow = curIndex5;
                                        if (curIndex5 != -1) {
                                            if (ucPos10 + 1 < bytePos6) {
                                                forwardUC4 = charBuffer[ucPos10 + 1];
                                            } else {
                                                forwardUC4 = 65535;
                                            }
                                            int i3 = forwardUC4;
                                            if (!inDynamicWindow(nextUC, whichWindow) || !inDynamicWindow(i3, whichWindow)) {
                                                int i4 = i3;
                                                if (bytePos8 + 1 >= i) {
                                                    char c = ucPos11;
                                                    ucPos = ucPos10 - 1;
                                                    int i5 = i4;
                                                    break;
                                                } else {
                                                    int bytePos9 = bytePos8 + 1;
                                                    hiByte = hiByte4;
                                                    bArr[bytePos8] = (byte) (1 + whichWindow);
                                                    bytePos4 = bytePos9 + 1;
                                                    bArr[bytePos9] = (byte) ((ucPos11 - this.fOffsets[whichWindow]) + 128);
                                                    curUC2 = ucPos11;
                                                    ucPos4 = ucPos10;
                                                    curIndex = curIndex4;
                                                    forwardUC3 = i4;
                                                }
                                            } else if (bytePos8 + 1 >= i) {
                                                ucPos5 = ucPos10 - 1;
                                                break;
                                            } else {
                                                int bytePos10 = bytePos8 + 1;
                                                bArr[bytePos8] = (byte) (16 + whichWindow);
                                                int bytePos11 = bytePos10 + 1;
                                                bArr[bytePos10] = (byte) ((ucPos11 - this.fOffsets[whichWindow]) + 128);
                                                int[] iArr = this.fTimeStamps;
                                                int i6 = this.fTimeStamp + 1;
                                                this.fTimeStamp = i6;
                                                iArr[whichWindow] = i6;
                                                this.fCurrentWindow = whichWindow;
                                                curUC = ucPos11;
                                                ucPos8 = ucPos10;
                                                curIndex3 = curIndex4;
                                                forwardUC7 = i3;
                                                bytePos = bytePos11;
                                            }
                                        } else {
                                            hiByte = hiByte4;
                                            int findStaticWindow = findStaticWindow(ucPos11);
                                            int whichWindow2 = findStaticWindow;
                                            if (findStaticWindow == -1 || inStaticWindow(nextUC, whichWindow2)) {
                                                curIndex3 = makeIndex(ucPos11);
                                                int[] iArr2 = this.fIndexCount;
                                                iArr2[curIndex3] = iArr2[curIndex3] + 1;
                                                if (ucPos10 + 1 < bytePos6) {
                                                    forwardUC2 = charBuffer[ucPos10 + 1];
                                                } else {
                                                    forwardUC2 = -1;
                                                }
                                                if (this.fIndexCount[curIndex3] > 1 || (curIndex3 == makeIndex(nextUC) && curIndex3 == makeIndex(forwardUC7))) {
                                                    if (bytePos8 + 2 >= i) {
                                                        ucPos3 = -1 + ucPos10;
                                                        break;
                                                    } else {
                                                        int whichWindow3 = getLRDefinedWindow();
                                                        int bytePos12 = bytePos8 + 1;
                                                        bArr[bytePos8] = (byte) (24 + whichWindow3);
                                                        int bytePos13 = bytePos12 + 1;
                                                        bArr[bytePos12] = (byte) curIndex3;
                                                        int bytePos14 = bytePos13 + 1;
                                                        bArr[bytePos13] = (byte) ((ucPos11 - sOffsetTable[curIndex3]) + 128);
                                                        this.fOffsets[whichWindow3] = sOffsetTable[curIndex3];
                                                        this.fCurrentWindow = whichWindow3;
                                                        int[] iArr3 = this.fTimeStamps;
                                                        int i7 = this.fTimeStamp + 1;
                                                        this.fTimeStamp = i7;
                                                        iArr3[whichWindow3] = i7;
                                                        bytePos = bytePos14;
                                                        curUC = ucPos11;
                                                        ucPos8 = ucPos10;
                                                        hiByte4 = hiByte;
                                                        bytePos6 = charBufferLimit;
                                                    }
                                                } else if (bytePos8 + 3 >= i) {
                                                    ucPos3 = -1 + ucPos10;
                                                    break;
                                                } else {
                                                    int bytePos15 = bytePos8 + 1;
                                                    bArr[bytePos8] = 15;
                                                    int hiByte5 = ucPos11 >>> 8;
                                                    char c2 = ucPos11 & 255;
                                                    if (sUnicodeTagTable[hiByte5]) {
                                                        bytePos2 = bytePos15 + 1;
                                                        bArr[bytePos15] = -16;
                                                    } else {
                                                        bytePos2 = bytePos15;
                                                    }
                                                    int bytePos16 = bytePos2 + 1;
                                                    bArr[bytePos2] = (byte) hiByte5;
                                                    bArr[bytePos16] = (byte) c2;
                                                    this.fMode = 1;
                                                    char c3 = c2;
                                                    hiByte2 = hiByte5;
                                                    bytePos3 = bytePos16 + 1;
                                                }
                                            } else if (bytePos8 + 1 >= i) {
                                                char c4 = ucPos11;
                                                ucPos2 = ucPos10 - 1;
                                                int i8 = forwardUC;
                                                break;
                                            } else {
                                                int bytePos17 = bytePos8 + 1;
                                                bArr[bytePos8] = (byte) (1 + whichWindow2);
                                                bytePos4 = bytePos17 + 1;
                                                bArr[bytePos17] = (byte) (ucPos11 - sOffsets[whichWindow2]);
                                                curUC2 = ucPos11;
                                                ucPos4 = ucPos10;
                                                curIndex = curIndex4;
                                                forwardUC3 = forwardUC;
                                            }
                                        }
                                        hiByte4 = hiByte;
                                    } else if (nextUC == -1 || !isCompressible(nextUC)) {
                                        if (bytePos8 + 3 >= i) {
                                            ucPos6 = ucPos10 - 1;
                                            break;
                                        } else {
                                            int bytePos18 = bytePos8 + 1;
                                            bArr[bytePos8] = 15;
                                            int hiByte6 = ucPos11 >>> 8;
                                            char c5 = ucPos11 & 255;
                                            if (sUnicodeTagTable[hiByte6]) {
                                                bArr[bytePos18] = -16;
                                                bytePos18++;
                                            }
                                            int bytePos19 = bytePos18 + 1;
                                            bArr[bytePos18] = (byte) hiByte6;
                                            int bytePos20 = bytePos19 + 1;
                                            bArr[bytePos19] = (byte) c5;
                                            this.fMode = 1;
                                            hiByte2 = hiByte6;
                                            bytePos3 = bytePos20;
                                            char c6 = c5;
                                            curIndex3 = curIndex4;
                                            forwardUC7 = forwardUC;
                                        }
                                    } else if (bytePos8 + 2 >= i) {
                                        ucPos6 = ucPos10 - 1;
                                        break;
                                    } else {
                                        int bytePos21 = bytePos8 + 1;
                                        bArr[bytePos8] = 14;
                                        int bytePos22 = bytePos21 + 1;
                                        bArr[bytePos21] = (byte) (ucPos11 >>> 8);
                                        bArr[bytePos22] = (byte) (ucPos11 & 255);
                                        curUC = ucPos11;
                                        ucPos8 = ucPos10;
                                        bytePos = bytePos22 + 1;
                                        curIndex3 = curIndex4;
                                    }
                                    int i9 = ucPos10;
                                    curUC3 = ucPos11;
                                    ucPos8 = i9;
                                    break;
                                } else {
                                    forwardUC = forwardUC7;
                                    bArr[bytePos8] = (byte) ((ucPos11 - this.fOffsets[this.fCurrentWindow]) + 128);
                                    curUC = ucPos11;
                                    ucPos8 = ucPos10;
                                    curIndex3 = curIndex4;
                                    bytePos = bytePos8 + 1;
                                }
                                forwardUC7 = forwardUC;
                            }
                        }
                        curUC3 = curUC4;
                        curIndex3 = curIndex3;
                        forwardUC7 = forwardUC7;
                        hiByte4 = hiByte4;
                        continue;
                    case 1:
                        int bytePos23 = bytePos8;
                        int ucPos12 = ucPos9;
                        while (true) {
                            if (ucPos12 >= bytePos6 || bytePos23 >= i) {
                                ucPos8 = ucPos12;
                                bytePos8 = bytePos23;
                                curUC3 = curUC4;
                                curIndex3 = curIndex3;
                                forwardUC7 = forwardUC7;
                                hiByte4 = hiByte4;
                                break;
                            } else {
                                ucPos8 = ucPos12 + 1;
                                char ucPos13 = charBuffer[ucPos12];
                                if (ucPos8 < bytePos6) {
                                    nextUC2 = charBuffer[ucPos8];
                                } else {
                                    nextUC2 = -1;
                                }
                                if (isCompressible(ucPos13)) {
                                    curIndex2 = curIndex3;
                                    if (nextUC2 == -1 || isCompressible(nextUC2) != 0) {
                                        if (ucPos13 >= 128) {
                                            forwardUC6 = forwardUC7;
                                            int findDynamicWindow = findDynamicWindow(ucPos13);
                                            int whichWindow4 = findDynamicWindow;
                                            if (findDynamicWindow == -1) {
                                                curIndex3 = makeIndex(ucPos13);
                                                int[] iArr4 = this.fIndexCount;
                                                iArr4[curIndex3] = iArr4[curIndex3] + 1;
                                                if (ucPos8 + 1 < bytePos6) {
                                                    forwardUC7 = charBuffer[ucPos8 + 1];
                                                } else {
                                                    forwardUC7 = -1;
                                                }
                                                int whichWindow5 = whichWindow4;
                                                hiByte3 = hiByte4;
                                                if (this.fIndexCount[curIndex3] <= 1) {
                                                    if (curIndex3 == makeIndex(nextUC2) && curIndex3 == makeIndex(forwardUC7)) {
                                                        nextUC3 = nextUC2;
                                                        break;
                                                    } else if (bytePos23 + 2 >= i) {
                                                        ucPos = ucPos8 - 1;
                                                        char c7 = ucPos13;
                                                        bytePos8 = bytePos23;
                                                        int i10 = curIndex3;
                                                        int i11 = whichWindow5;
                                                        int i12 = hiByte3;
                                                        break;
                                                    } else {
                                                        hiByte4 = ucPos13 >>> 8;
                                                        int loByte2 = ucPos13 & 255;
                                                        if (sUnicodeTagTable[hiByte4]) {
                                                            bArr[bytePos23] = -16;
                                                            bytePos23++;
                                                        }
                                                        int bytePos24 = bytePos23 + 1;
                                                        nextUC3 = nextUC2;
                                                        bArr[bytePos23] = (byte) hiByte4;
                                                        bytePos5 = bytePos24 + 1;
                                                        bArr[bytePos24] = (byte) loByte2;
                                                        curUC4 = ucPos13;
                                                        int i13 = whichWindow5;
                                                        ucPos12 = ucPos8;
                                                    }
                                                } else {
                                                    nextUC3 = nextUC2;
                                                    break;
                                                }
                                            } else if (inDynamicWindow(nextUC2, whichWindow4)) {
                                                if (bytePos23 + 1 < i) {
                                                    int bytePos25 = bytePos23 + 1;
                                                    bArr[bytePos23] = (byte) (224 + whichWindow4);
                                                    int bytePos26 = bytePos25 + 1;
                                                    bArr[bytePos25] = (byte) ((ucPos13 - this.fOffsets[whichWindow4]) + 128);
                                                    int[] iArr5 = this.fTimeStamps;
                                                    int i14 = this.fTimeStamp + 1;
                                                    this.fTimeStamp = i14;
                                                    iArr5[whichWindow4] = i14;
                                                    this.fCurrentWindow = whichWindow4;
                                                    this.fMode = 0;
                                                    curUC3 = ucPos13;
                                                    curIndex3 = curIndex2;
                                                    forwardUC7 = forwardUC6;
                                                    bytePos8 = bytePos26;
                                                    break;
                                                } else {
                                                    ucPos = ucPos8 - 1;
                                                    break;
                                                }
                                            } else if (bytePos23 + 2 >= i) {
                                                ucPos = ucPos8 - 1;
                                                break;
                                            } else {
                                                hiByte4 = ucPos13 >>> 8;
                                                int loByte3 = ucPos13 & 255;
                                                if (sUnicodeTagTable[hiByte4]) {
                                                    bArr[bytePos23] = -16;
                                                    bytePos23++;
                                                }
                                                int bytePos27 = bytePos23 + 1;
                                                bArr[bytePos23] = (byte) hiByte4;
                                                bytePos23 = bytePos27 + 1;
                                                bArr[bytePos27] = (byte) loByte3;
                                                curUC4 = ucPos13;
                                                curIndex3 = curIndex2;
                                                forwardUC7 = forwardUC6;
                                                ucPos12 = ucPos8;
                                            }
                                        } else {
                                            int loByte4 = ucPos13 & 255;
                                            if (nextUC2 == -1 || nextUC2 >= 128 || sSingleTagTable[loByte4]) {
                                                forwardUC6 = forwardUC7;
                                                if (bytePos23 + 1 >= i) {
                                                    ucPos = ucPos8 - 1;
                                                    break;
                                                } else {
                                                    int bytePos28 = bytePos23 + 1;
                                                    bArr[bytePos23] = 0;
                                                    bytePos23 = bytePos28 + 1;
                                                    bArr[bytePos28] = (byte) loByte4;
                                                    curUC4 = ucPos13;
                                                    curIndex3 = curIndex2;
                                                    forwardUC7 = forwardUC6;
                                                    ucPos12 = ucPos8;
                                                }
                                            } else if (bytePos23 + 1 >= i) {
                                                ucPos = ucPos8 - 1;
                                                char c8 = ucPos13;
                                                bytePos8 = bytePos23;
                                                int i15 = curIndex2;
                                                break;
                                            } else {
                                                int whichWindow6 = this.fCurrentWindow;
                                                int whichWindow7 = bytePos23 + 1;
                                                int forwardUC8 = forwardUC7;
                                                bArr[bytePos23] = (byte) (224 + whichWindow6);
                                                int bytePos29 = whichWindow7 + 1;
                                                bArr[whichWindow7] = (byte) loByte4;
                                                int[] iArr6 = this.fTimeStamps;
                                                int i16 = this.fTimeStamp + 1;
                                                this.fTimeStamp = i16;
                                                iArr6[whichWindow6] = i16;
                                                this.fMode = 0;
                                                int i17 = whichWindow6;
                                                curIndex3 = curIndex2;
                                                forwardUC7 = forwardUC8;
                                                int i18 = bytePos29;
                                                curUC3 = ucPos13;
                                                bytePos8 = i18;
                                                continue;
                                            }
                                        }
                                    } else {
                                        nextUC4 = nextUC2;
                                        forwardUC5 = forwardUC7;
                                        hiByte3 = hiByte4;
                                    }
                                } else {
                                    curIndex2 = curIndex3;
                                    nextUC4 = nextUC2;
                                    forwardUC5 = forwardUC7;
                                    hiByte3 = hiByte4;
                                }
                                if (bytePos23 + 2 >= i) {
                                    int ucPos14 = ucPos8 - 1;
                                    char c9 = ucPos13;
                                    bytePos8 = bytePos23;
                                    int i19 = curIndex2;
                                    int i20 = forwardUC5;
                                    break;
                                } else {
                                    hiByte4 = ucPos13 >>> 8;
                                    int loByte5 = ucPos13 & 255;
                                    if (sUnicodeTagTable[hiByte4]) {
                                        bArr[bytePos23] = -16;
                                        bytePos23++;
                                    }
                                    int bytePos30 = bytePos23 + 1;
                                    bArr[bytePos23] = (byte) hiByte4;
                                    bytePos5 = bytePos30 + 1;
                                    bArr[bytePos30] = (byte) loByte5;
                                    curUC4 = ucPos13;
                                    curIndex3 = curIndex2;
                                    forwardUC7 = forwardUC5;
                                    ucPos12 = ucPos8;
                                }
                            }
                        }
                        break;
                    default:
                        int i21 = hiByte4;
                        curUC3 = curUC4;
                        ucPos8 = ucPos9;
                        continue;
                }
            }
            bytePos6 = charBufferLimit;
        }
        if (charsRead != null) {
            charsRead[0] = ucPos - charBufferStart;
        }
        return bytePos8 - byteBufferStart;
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
        for (int i = 0; i < 8; i++) {
            this.fTimeStamps[i] = 0;
        }
        for (int i2 = 0; i2 <= 255; i2++) {
            this.fIndexCount[i2] = 0;
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
        if (c >= 128 && c < 13312) {
            return (c / 128) & 255;
        }
        if (c < 57344 || c > 65535) {
            return 0;
        }
        return ((c - Normalizer2Impl.Hangul.HANGUL_BASE) / 128) & 255;
    }

    private boolean inDynamicWindow(int c, int whichWindow) {
        return c >= this.fOffsets[whichWindow] && c < this.fOffsets[whichWindow] + 128;
    }

    private static boolean inStaticWindow(int c, int whichWindow) {
        return c >= sOffsets[whichWindow] && c < sOffsets[whichWindow] + 128;
    }

    private static boolean isCompressible(int c) {
        return c < 13312 || c >= 57344;
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
