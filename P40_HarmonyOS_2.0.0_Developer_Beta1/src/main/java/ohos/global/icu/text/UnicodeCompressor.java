package ohos.global.icu.text;

public final class UnicodeCompressor implements SCSU {
    private static boolean[] sSingleTagTable = {false, true, true, true, true, true, true, true, true, false, false, true, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
    private static boolean[] sUnicodeTagTable = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false};
    private int fCurrentWindow = 0;
    private int[] fIndexCount = new int[256];
    private int fMode = 0;
    private int[] fOffsets = new int[8];
    private int fTimeStamp = 0;
    private int[] fTimeStamps = new int[8];

    private static boolean isCompressible(int i) {
        return i < 13312 || i >= 57344;
    }

    public UnicodeCompressor() {
        reset();
    }

    public static byte[] compress(String str) {
        return compress(str.toCharArray(), 0, str.length());
    }

    public static byte[] compress(char[] cArr, int i, int i2) {
        UnicodeCompressor unicodeCompressor = new UnicodeCompressor();
        int max = Math.max(4, ((i2 - i) * 3) + 1);
        byte[] bArr = new byte[max];
        int compress = unicodeCompressor.compress(cArr, i, i2, null, bArr, 0, max);
        byte[] bArr2 = new byte[compress];
        System.arraycopy(bArr, 0, bArr2, 0, compress);
        return bArr2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:105:0x01e4, code lost:
        if ((r5 + 3) < r24) goto L_0x01e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x01e8, code lost:
        r6 = r5 + 1;
        r22[r5] = 15;
        r5 = r4 >>> '\b';
        r4 = r4 & 255;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x01f4, code lost:
        if (ohos.global.icu.text.UnicodeCompressor.sUnicodeTagTable[r5] == false) goto L_0x01fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x01f6, code lost:
        r9 = r6 + 1;
        r22[r6] = -16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x01fb, code lost:
        r9 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x01fc, code lost:
        r6 = r9 + 1;
        r22[r9] = (byte) r5;
        r5 = r6 + 1;
        r22[r6] = (byte) r4;
        r17.fMode = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0208, code lost:
        r4 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x02ac, code lost:
        if ((r5 + 3) < r24) goto L_0x02b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x02b0, code lost:
        r6 = r5 + 1;
        r22[r5] = 15;
        r5 = r4 >>> '\b';
        r4 = r4 & 255;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x02bc, code lost:
        if (ohos.global.icu.text.UnicodeCompressor.sUnicodeTagTable[r5] == false) goto L_0x02c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x02be, code lost:
        r9 = r6 + 1;
        r22[r6] = -16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x02c3, code lost:
        r9 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x02c4, code lost:
        r6 = r9 + 1;
        r22[r9] = (byte) r5;
        r5 = r6 + 1;
        r22[r6] = (byte) r4;
        r17.fMode = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x011b, code lost:
        if ((r5 + 2) < r24) goto L_0x011e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x011e, code lost:
        r8 = getLRDefinedWindow();
        r10 = r5 + 1;
        r22[r5] = (byte) (r8 + ohos.global.icu.text.SCSU.UDEFINE0);
        r5 = r10 + 1;
        r22[r10] = (byte) r13;
        r10 = r5 + 1;
        r22[r5] = (byte) ((r4 - ohos.global.icu.text.UnicodeCompressor.sOffsetTable[r13]) + 128);
        r17.fOffsets[r8] = ohos.global.icu.text.UnicodeCompressor.sOffsetTable[r13];
        r17.fCurrentWindow = r8;
        r4 = r17.fTimeStamps;
        r5 = r17.fTimeStamp + 1;
        r17.fTimeStamp = r5;
        r4[r8] = r5;
        r17.fMode = 0;
        r4 = r7;
        r5 = r10;
     */
    public int compress(char[] cArr, int i, int i2, int[] iArr, byte[] bArr, int i3, int i4) {
        int i5;
        char c;
        int i6;
        int i7;
        int i8;
        int i9;
        char c2;
        int i10;
        int i11;
        if (bArr.length < 4 || i4 - i3 < 4) {
            throw new IllegalArgumentException("byteBuffer.length < 4");
        }
        int i12 = i;
        int i13 = i3;
        loop0:
        while (true) {
            if (i12 >= i2 || i13 >= i4) {
                break;
            }
            int i14 = this.fMode;
            if (i14 != 0) {
                if (i14 == 1) {
                    while (true) {
                        if (i12 >= i2 || i13 >= i4) {
                            break;
                        }
                        i5 = i12 + 1;
                        char c3 = cArr[i12];
                        char c4 = i5 < i2 ? cArr[i5] : 65535;
                        if (!isCompressible(c3) || (c4 != 65535 && !isCompressible(c4))) {
                            if (i13 + 2 >= i4) {
                                break loop0;
                            }
                            int i15 = c3 >>> '\b';
                            int i16 = c3 & 255;
                            if (sUnicodeTagTable[i15]) {
                                i9 = i13 + 1;
                                bArr[i13] = -16;
                            } else {
                                i9 = i13;
                            }
                            int i17 = i9 + 1;
                            bArr[i9] = (byte) i15;
                            i8 = i17 + 1;
                            bArr[i17] = (byte) i16;
                        } else if (c3 < 128) {
                            int i18 = c3 & 255;
                            if (c4 == 65535 || c4 >= 128 || sSingleTagTable[i18]) {
                                int i19 = i13 + 1;
                                if (i19 >= i4) {
                                    break loop0;
                                }
                                bArr[i13] = 0;
                                i13 = i19 + 1;
                                bArr[i19] = (byte) i18;
                                i12 = i5;
                            } else {
                                int i20 = i13 + 1;
                                if (i20 >= i4) {
                                    break;
                                }
                                int i21 = this.fCurrentWindow;
                                bArr[i13] = (byte) (i21 + 224);
                                i13 = i20 + 1;
                                bArr[i20] = (byte) i18;
                                int[] iArr2 = this.fTimeStamps;
                                int i22 = this.fTimeStamp + 1;
                                this.fTimeStamp = i22;
                                iArr2[i21] = i22;
                                this.fMode = 0;
                            }
                        } else {
                            int findDynamicWindow = findDynamicWindow(c3);
                            if (findDynamicWindow == -1) {
                                int makeIndex = makeIndex(c3);
                                int[] iArr3 = this.fIndexCount;
                                iArr3[makeIndex] = iArr3[makeIndex] + 1;
                                int i23 = i5 + 1;
                                if (i23 < i2) {
                                    c2 = cArr[i23];
                                } else {
                                    c2 = 65535;
                                }
                                if (this.fIndexCount[makeIndex] > 1 || (makeIndex == makeIndex(c4) && makeIndex == makeIndex(c2))) {
                                    break;
                                } else if (i13 + 2 >= i4) {
                                    break loop0;
                                } else {
                                    int i24 = c3 >>> '\b';
                                    int i25 = c3 & 255;
                                    if (sUnicodeTagTable[i24]) {
                                        i10 = i13 + 1;
                                        bArr[i13] = -16;
                                    } else {
                                        i10 = i13;
                                    }
                                    int i26 = i10 + 1;
                                    bArr[i10] = (byte) i24;
                                    i8 = i26 + 1;
                                    bArr[i26] = (byte) i25;
                                }
                            } else if (inDynamicWindow(c4, findDynamicWindow)) {
                                int i27 = i13 + 1;
                                if (i27 >= i4) {
                                    break;
                                }
                                bArr[i13] = (byte) (findDynamicWindow + 224);
                                i13 = i27 + 1;
                                bArr[i27] = (byte) ((c3 - this.fOffsets[findDynamicWindow]) + 128);
                                int[] iArr4 = this.fTimeStamps;
                                int i28 = this.fTimeStamp + 1;
                                this.fTimeStamp = i28;
                                iArr4[findDynamicWindow] = i28;
                                this.fCurrentWindow = findDynamicWindow;
                                this.fMode = 0;
                            } else if (i13 + 2 >= i4) {
                                break loop0;
                            } else {
                                int i29 = c3 >>> '\b';
                                int i30 = c3 & 255;
                                if (sUnicodeTagTable[i29]) {
                                    i11 = i13 + 1;
                                    bArr[i13] = -16;
                                } else {
                                    i11 = i13;
                                }
                                int i31 = i11 + 1;
                                bArr[i11] = (byte) i29;
                                i8 = i31 + 1;
                                bArr[i31] = (byte) i30;
                            }
                        }
                        i12 = i5;
                        i13 = i8;
                    }
                } else {
                    continue;
                }
            } else {
                while (true) {
                    if (i12 >= i2 || i13 >= i4) {
                        break;
                    }
                    i5 = i12 + 1;
                    char c5 = cArr[i12];
                    if (i5 < i2) {
                        c = cArr[i5];
                    } else {
                        c = 65535;
                    }
                    if (c5 < 128) {
                        int i32 = c5 & 255;
                        if (sSingleTagTable[i32]) {
                            i7 = i13 + 1;
                            if (i7 >= i4) {
                                break loop0;
                            }
                            bArr[i13] = 1;
                        } else {
                            i7 = i13;
                        }
                        i13 = i7 + 1;
                        bArr[i7] = (byte) i32;
                    } else {
                        if (inDynamicWindow(c5, this.fCurrentWindow)) {
                            i6 = i13 + 1;
                            bArr[i13] = (byte) ((c5 - this.fOffsets[this.fCurrentWindow]) + 128);
                        } else if (isCompressible(c5)) {
                            int findDynamicWindow2 = findDynamicWindow(c5);
                            if (findDynamicWindow2 != -1) {
                                int i33 = i5 + 1;
                                char c6 = i33 < i2 ? cArr[i33] : 65535;
                                if (!inDynamicWindow(c, findDynamicWindow2) || !inDynamicWindow(c6, findDynamicWindow2)) {
                                    int i34 = i13 + 1;
                                    if (i34 >= i4) {
                                        break loop0;
                                    }
                                    bArr[i13] = (byte) (findDynamicWindow2 + 1);
                                    i13 = i34 + 1;
                                    bArr[i34] = (byte) ((c5 - this.fOffsets[findDynamicWindow2]) + 128);
                                } else {
                                    int i35 = i13 + 1;
                                    if (i35 >= i4) {
                                        break loop0;
                                    }
                                    bArr[i13] = (byte) (findDynamicWindow2 + 16);
                                    i13 = i35 + 1;
                                    bArr[i35] = (byte) ((c5 - this.fOffsets[findDynamicWindow2]) + 128);
                                    int[] iArr5 = this.fTimeStamps;
                                    int i36 = this.fTimeStamp + 1;
                                    this.fTimeStamp = i36;
                                    iArr5[findDynamicWindow2] = i36;
                                    this.fCurrentWindow = findDynamicWindow2;
                                }
                            } else {
                                int findStaticWindow = findStaticWindow(c5);
                                if (findStaticWindow == -1 || inStaticWindow(c, findStaticWindow)) {
                                    int makeIndex2 = makeIndex(c5);
                                    int[] iArr6 = this.fIndexCount;
                                    iArr6[makeIndex2] = iArr6[makeIndex2] + 1;
                                    int i37 = i5 + 1;
                                    char c7 = i37 < i2 ? cArr[i37] : 65535;
                                    if (this.fIndexCount[makeIndex2] > 1 || (makeIndex2 == makeIndex(c) && makeIndex2 == makeIndex(c7))) {
                                        if (i13 + 2 >= i4) {
                                            break loop0;
                                        }
                                        int lRDefinedWindow = getLRDefinedWindow();
                                        int i38 = i13 + 1;
                                        bArr[i13] = (byte) (lRDefinedWindow + 24);
                                        int i39 = i38 + 1;
                                        bArr[i38] = (byte) makeIndex2;
                                        int i40 = i39 + 1;
                                        bArr[i39] = (byte) ((c5 - sOffsetTable[makeIndex2]) + 128);
                                        this.fOffsets[lRDefinedWindow] = sOffsetTable[makeIndex2];
                                        this.fCurrentWindow = lRDefinedWindow;
                                        int[] iArr7 = this.fTimeStamps;
                                        int i41 = this.fTimeStamp + 1;
                                        this.fTimeStamp = i41;
                                        iArr7[lRDefinedWindow] = i41;
                                        i12 = i5;
                                        i13 = i40;
                                    }
                                } else {
                                    int i42 = i13 + 1;
                                    if (i42 >= i4) {
                                        break loop0;
                                    }
                                    bArr[i13] = (byte) (findStaticWindow + 1);
                                    i13 = i42 + 1;
                                    bArr[i42] = (byte) (c5 - sOffsets[findStaticWindow]);
                                }
                            }
                        } else if (c == 65535 || !isCompressible(c)) {
                            break;
                        } else if (i13 + 2 >= i4) {
                            break loop0;
                        } else {
                            int i43 = i13 + 1;
                            bArr[i13] = 14;
                            int i44 = i43 + 1;
                            bArr[i43] = (byte) (c5 >>> '\b');
                            i6 = i44 + 1;
                            bArr[i44] = (byte) (c5 & 255);
                        }
                        i12 = i5;
                        i13 = i6;
                    }
                    i12 = i5;
                }
            }
        }
        i12 = i5 - 1;
        if (iArr != null) {
            iArr[0] = i12 - i;
        }
        return i13 - i3;
    }

    public void reset() {
        int[] iArr = this.fOffsets;
        iArr[0] = 128;
        iArr[1] = 192;
        iArr[2] = 1024;
        iArr[3] = 1536;
        iArr[4] = 2304;
        iArr[5] = 12352;
        iArr[6] = 12448;
        iArr[7] = 65280;
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

    private static int makeIndex(int i) {
        int i2;
        if (i >= 192 && i < 320) {
            return SCSU.LATININDEX;
        }
        if (i >= 592 && i < 720) {
            return SCSU.IPAEXTENSIONINDEX;
        }
        if (i >= 880 && i < 1008) {
            return SCSU.GREEKINDEX;
        }
        if (i >= 1328 && i < 1424) {
            return SCSU.ARMENIANINDEX;
        }
        if (i >= 12352 && i < 12448) {
            return SCSU.HIRAGANAINDEX;
        }
        if (i >= 12448 && i < 12576) {
            return SCSU.KATAKANAINDEX;
        }
        if (i >= 65376 && i < 65439) {
            return 255;
        }
        if (i >= 128 && i < 13312) {
            i2 = i / 128;
        } else if (i < 57344 || i > 65535) {
            return 0;
        } else {
            i2 = (i - 44032) / 128;
        }
        return i2 & 255;
    }

    private boolean inDynamicWindow(int i, int i2) {
        int[] iArr = this.fOffsets;
        return i >= iArr[i2] && i < iArr[i2] + 128;
    }

    private static boolean inStaticWindow(int i, int i2) {
        return i >= sOffsets[i2] && i < sOffsets[i2] + 128;
    }

    private int findDynamicWindow(int i) {
        for (int i2 = 7; i2 >= 0; i2--) {
            if (inDynamicWindow(i, i2)) {
                int[] iArr = this.fTimeStamps;
                iArr[i2] = iArr[i2] + 1;
                return i2;
            }
        }
        return -1;
    }

    private static int findStaticWindow(int i) {
        for (int i2 = 7; i2 >= 0; i2--) {
            if (inStaticWindow(i, i2)) {
                return i2;
            }
        }
        return -1;
    }

    private int getLRDefinedWindow() {
        int i = -1;
        int i2 = Integer.MAX_VALUE;
        for (int i3 = 7; i3 >= 0; i3--) {
            int[] iArr = this.fTimeStamps;
            if (iArr[i3] < i2) {
                i2 = iArr[i3];
                i = i3;
            }
        }
        return i;
    }
}
