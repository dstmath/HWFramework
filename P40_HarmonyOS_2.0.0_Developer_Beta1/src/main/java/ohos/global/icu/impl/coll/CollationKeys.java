package ohos.global.icu.impl.coll;

public final class CollationKeys {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int CASE_LOWER_FIRST_COMMON_HIGH = 13;
    private static final int CASE_LOWER_FIRST_COMMON_LOW = 1;
    private static final int CASE_LOWER_FIRST_COMMON_MAX_COUNT = 7;
    private static final int CASE_LOWER_FIRST_COMMON_MIDDLE = 7;
    private static final int CASE_UPPER_FIRST_COMMON_HIGH = 15;
    private static final int CASE_UPPER_FIRST_COMMON_LOW = 3;
    private static final int CASE_UPPER_FIRST_COMMON_MAX_COUNT = 13;
    private static final int QUAT_COMMON_HIGH = 252;
    private static final int QUAT_COMMON_LOW = 28;
    private static final int QUAT_COMMON_MAX_COUNT = 113;
    private static final int QUAT_COMMON_MIDDLE = 140;
    private static final int QUAT_SHIFTED_LIMIT_BYTE = 27;
    static final int SEC_COMMON_HIGH = 69;
    private static final int SEC_COMMON_LOW = 5;
    private static final int SEC_COMMON_MAX_COUNT = 33;
    private static final int SEC_COMMON_MIDDLE = 37;
    public static final LevelCallback SIMPLE_LEVEL_FALLBACK = new LevelCallback();
    private static final int TER_LOWER_FIRST_COMMON_HIGH = 69;
    private static final int TER_LOWER_FIRST_COMMON_LOW = 5;
    private static final int TER_LOWER_FIRST_COMMON_MAX_COUNT = 33;
    private static final int TER_LOWER_FIRST_COMMON_MIDDLE = 37;
    private static final int TER_ONLY_COMMON_HIGH = 197;
    private static final int TER_ONLY_COMMON_LOW = 5;
    private static final int TER_ONLY_COMMON_MAX_COUNT = 97;
    private static final int TER_ONLY_COMMON_MIDDLE = 101;
    private static final int TER_UPPER_FIRST_COMMON_HIGH = 197;
    private static final int TER_UPPER_FIRST_COMMON_LOW = 133;
    private static final int TER_UPPER_FIRST_COMMON_MAX_COUNT = 33;
    private static final int TER_UPPER_FIRST_COMMON_MIDDLE = 165;
    private static final int[] levelMasks = {2, 6, 22, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 54};

    public static class LevelCallback {
        /* access modifiers changed from: package-private */
        public boolean needToWrite(int i) {
            return true;
        }
    }

    public static abstract class SortKeyByteSink {
        private int appended_ = 0;
        protected byte[] buffer_;

        /* access modifiers changed from: protected */
        public abstract void AppendBeyondCapacity(byte[] bArr, int i, int i2, int i3);

        /* access modifiers changed from: protected */
        public abstract boolean Resize(int i, int i2);

        public SortKeyByteSink(byte[] bArr) {
            this.buffer_ = bArr;
        }

        public void setBufferAndAppended(byte[] bArr, int i) {
            this.buffer_ = bArr;
            this.appended_ = i;
        }

        public void Append(byte[] bArr, int i) {
            if (i > 0 && bArr != null) {
                int i2 = this.appended_;
                this.appended_ = i2 + i;
                byte[] bArr2 = this.buffer_;
                if (i <= bArr2.length - i2) {
                    System.arraycopy(bArr, 0, bArr2, i2, i);
                } else {
                    AppendBeyondCapacity(bArr, 0, i, i2);
                }
            }
        }

        public void Append(int i) {
            int i2 = this.appended_;
            if (i2 < this.buffer_.length || Resize(1, i2)) {
                this.buffer_[this.appended_] = (byte) i;
            }
            this.appended_++;
        }

        public int NumberOfBytesAppended() {
            return this.appended_;
        }

        public int GetRemainingCapacity() {
            return this.buffer_.length - this.appended_;
        }

        public boolean Overflowed() {
            return this.appended_ > this.buffer_.length;
        }
    }

    /* access modifiers changed from: private */
    public static final class SortKeyLevel {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final int INITIAL_CAPACITY = 40;
        byte[] buffer = new byte[40];
        int len = 0;

        SortKeyLevel() {
        }

        /* access modifiers changed from: package-private */
        public boolean isEmpty() {
            return this.len == 0;
        }

        /* access modifiers changed from: package-private */
        public int length() {
            return this.len;
        }

        /* access modifiers changed from: package-private */
        public byte getAt(int i) {
            return this.buffer[i];
        }

        /* access modifiers changed from: package-private */
        public byte[] data() {
            return this.buffer;
        }

        /* access modifiers changed from: package-private */
        public void appendByte(int i) {
            if (this.len < this.buffer.length || ensureCapacity(1)) {
                byte[] bArr = this.buffer;
                int i2 = this.len;
                this.len = i2 + 1;
                bArr[i2] = (byte) i;
            }
        }

        /* access modifiers changed from: package-private */
        public void appendWeight16(int i) {
            byte b = (byte) (i >>> 8);
            byte b2 = (byte) i;
            int i2 = b2 == 0 ? 1 : 2;
            if (this.len + i2 <= this.buffer.length || ensureCapacity(i2)) {
                byte[] bArr = this.buffer;
                int i3 = this.len;
                this.len = i3 + 1;
                bArr[i3] = b;
                if (b2 != 0) {
                    int i4 = this.len;
                    this.len = i4 + 1;
                    bArr[i4] = b2;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void appendWeight32(long j) {
            int i = 4;
            byte[] bArr = {(byte) ((int) (j >>> 24)), (byte) ((int) (j >>> 16)), (byte) ((int) (j >>> 8)), (byte) ((int) j)};
            if (bArr[1] == 0) {
                i = 1;
            } else if (bArr[2] == 0) {
                i = 2;
            } else if (bArr[3] == 0) {
                i = 3;
            }
            if (this.len + i <= this.buffer.length || ensureCapacity(i)) {
                byte[] bArr2 = this.buffer;
                int i2 = this.len;
                this.len = i2 + 1;
                bArr2[i2] = bArr[0];
                if (bArr[1] != 0) {
                    int i3 = this.len;
                    this.len = i3 + 1;
                    bArr2[i3] = bArr[1];
                    if (bArr[2] != 0) {
                        int i4 = this.len;
                        this.len = i4 + 1;
                        bArr2[i4] = bArr[2];
                        if (bArr[3] != 0) {
                            int i5 = this.len;
                            this.len = i5 + 1;
                            bArr2[i5] = bArr[3];
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void appendReverseWeight16(int i) {
            byte b = (byte) (i >>> 8);
            byte b2 = (byte) i;
            int i2 = b2 == 0 ? 1 : 2;
            if (this.len + i2 > this.buffer.length && !ensureCapacity(i2)) {
                return;
            }
            if (b2 == 0) {
                byte[] bArr = this.buffer;
                int i3 = this.len;
                this.len = i3 + 1;
                bArr[i3] = b;
                return;
            }
            byte[] bArr2 = this.buffer;
            int i4 = this.len;
            bArr2[i4] = b2;
            bArr2[i4 + 1] = b;
            this.len = i4 + 2;
        }

        /* access modifiers changed from: package-private */
        public void appendTo(SortKeyByteSink sortKeyByteSink) {
            sortKeyByteSink.Append(this.buffer, this.len - 1);
        }

        private boolean ensureCapacity(int i) {
            int length = this.buffer.length * 2;
            int i2 = (i * 2) + this.len;
            if (length >= i2) {
                i2 = length;
            }
            if (i2 < 200) {
                i2 = 200;
            }
            byte[] bArr = new byte[i2];
            System.arraycopy(this.buffer, 0, bArr, 0, this.len);
            this.buffer = bArr;
            return true;
        }
    }

    private static SortKeyLevel getSortKeyLevel(int i, int i2) {
        if ((i & i2) != 0) {
            return new SortKeyLevel();
        }
        return null;
    }

    private CollationKeys() {
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0136: APUT  (r13v0 byte[] A[IMMUTABLE_TYPE]), (0 ??[int, short, byte, char]), (r7v61 byte A[IMMUTABLE_TYPE]) */
    /* JADX WARNING: Removed duplicated region for block: B:233:0x0377  */
    /* JADX WARNING: Removed duplicated region for block: B:293:0x0445  */
    /* JADX WARNING: Removed duplicated region for block: B:296:0x03e0 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0135  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0156  */
    public static void writeSortKeyUpToQuaternary(CollationIterator collationIterator, boolean[] zArr, CollationSettings collationSettings, SortKeyByteSink sortKeyByteSink, int i, LevelCallback levelCallback, boolean z) {
        int i2;
        int i3;
        long j;
        long j2;
        SortKeyLevel sortKeyLevel;
        int i4;
        SortKeyLevel sortKeyLevel2;
        long j3;
        SortKeyLevel sortKeyLevel3;
        int i5;
        int i6;
        int i7;
        SortKeyLevel sortKeyLevel4;
        int i8;
        int i9;
        int i10;
        int i11;
        int i12;
        int i13;
        int i14;
        int i15;
        int i16;
        int i17;
        long j4;
        char c;
        byte b;
        int i18;
        int i19;
        long nextCE;
        CollationSettings collationSettings2 = collationSettings;
        int i20 = collationSettings2.options;
        int i21 = levelMasks[CollationSettings.getStrength(i20)];
        if ((i20 & 1024) != 0) {
            i21 |= 8;
        }
        int i22 = i21 & (~((1 << i) - 1));
        if (i22 != 0) {
            int i23 = i20 & 12;
            long j5 = i23 == 0 ? 0 : collationSettings2.variableTop + 1;
            int tertiaryMask = CollationSettings.getTertiaryMask(i20);
            byte[] bArr = new byte[3];
            SortKeyLevel sortKeyLevel5 = getSortKeyLevel(i22, 8);
            SortKeyLevel sortKeyLevel6 = getSortKeyLevel(i22, 4);
            SortKeyLevel sortKeyLevel7 = getSortKeyLevel(i22, 16);
            char c2 = ' ';
            SortKeyLevel sortKeyLevel8 = getSortKeyLevel(i22, 32);
            int i24 = 0;
            long j6 = 0;
            int i25 = 0;
            int i26 = 0;
            int i27 = 0;
            int i28 = 0;
            int i29 = 0;
            while (true) {
                collationIterator.clearCEsIfNoneRemaining();
                long nextCE2 = collationIterator.nextCE();
                long j7 = nextCE2 >>> c2;
                if (j7 >= j5 || j7 <= Collation.MERGE_SEPARATOR_PRIMARY) {
                    i2 = i23;
                    i3 = i20;
                    j2 = nextCE2;
                    j = j7;
                } else {
                    if (i24 != 0) {
                        int i30 = i24 - 1;
                        while (i30 >= 113) {
                            sortKeyLevel8.appendByte(140);
                            i30 -= 113;
                        }
                        sortKeyLevel8.appendByte(i30 + 28);
                        i19 = i23;
                        j = j7;
                        i18 = 0;
                    } else {
                        i19 = i23;
                        i18 = i24;
                        j = j7;
                    }
                    while (true) {
                        if ((i22 & 32) != 0) {
                            if (collationSettings.hasReordering()) {
                                j = collationSettings2.reorder(j);
                            }
                            i2 = i19;
                            if ((((int) j) >>> 24) >= 27) {
                                sortKeyLevel8.appendByte(27);
                            }
                            sortKeyLevel8.appendWeight32(j);
                        } else {
                            i2 = i19;
                        }
                        do {
                            nextCE = collationIterator.nextCE();
                            j = nextCE >>> 32;
                        } while (j == 0);
                        if (j >= j5 || j <= Collation.MERGE_SEPARATOR_PRIMARY) {
                            break;
                        }
                        i19 = i2;
                    }
                    i3 = i20;
                    j2 = nextCE;
                    i24 = i18;
                }
                if (j <= 1 || (i22 & 2) == 0) {
                    i4 = tertiaryMask;
                    sortKeyLevel = sortKeyLevel7;
                    j3 = j6;
                    sortKeyLevel2 = sortKeyLevel8;
                } else {
                    boolean z2 = zArr[((int) j) >>> 24];
                    if (collationSettings.hasReordering()) {
                        j = collationSettings2.reorder(j);
                    }
                    int i31 = (int) j;
                    int i32 = i31 >>> 24;
                    i4 = tertiaryMask;
                    sortKeyLevel = sortKeyLevel7;
                    sortKeyLevel2 = sortKeyLevel8;
                    if (!z2 || i32 != (((int) j6) >>> 24)) {
                        if (j6 != 0) {
                            if (j >= j6) {
                                sortKeyByteSink.Append(255);
                            } else if (i32 > 2) {
                                sortKeyByteSink.Append(3);
                            }
                        }
                        sortKeyByteSink.Append(i32);
                        if (z2) {
                            j4 = j;
                        } else {
                            c = 16;
                            j4 = 0;
                            b = (byte) ((int) (j >>> c));
                            if (b == 0) {
                                bArr[0] = b;
                                bArr[1] = (byte) ((int) (j >>> 8));
                                bArr[2] = (byte) i31;
                                sortKeyByteSink.Append(bArr, bArr[1] == 0 ? 1 : bArr[2] == 0 ? 2 : 3);
                            }
                            if (!z || !sortKeyByteSink.Overflowed()) {
                                j3 = j4;
                            } else {
                                return;
                            }
                        }
                    } else {
                        j4 = j6;
                    }
                    c = 16;
                    b = (byte) ((int) (j >>> c));
                    if (b == 0) {
                    }
                    if (!z) {
                    }
                    j3 = j4;
                }
                int i33 = (int) j2;
                if (i33 == 0) {
                    collationSettings2 = collationSettings;
                    i20 = i3;
                    sortKeyLevel8 = sortKeyLevel2;
                    j5 = j5;
                    i23 = i2;
                    c2 = ' ';
                    j6 = j3;
                    tertiaryMask = i4;
                    sortKeyLevel7 = sortKeyLevel;
                } else {
                    int i34 = i22 & 4;
                    int i35 = 1280;
                    if (i34 == 0 || (i17 = i33 >>> 16) == 0) {
                        i26 = i26;
                    } else if (i17 == 1280 && ((i3 & 2048) == 0 || j != Collation.MERGE_SEPARATOR_PRIMARY)) {
                        i25++;
                    } else if ((i3 & 2048) == 0) {
                        if (i25 != 0) {
                            int i36 = i25 - 1;
                            while (i36 >= 33) {
                                sortKeyLevel6.appendByte(37);
                                i36 -= 33;
                            }
                            sortKeyLevel6.appendByte(i17 < 1280 ? i36 + 5 : 69 - i36);
                            i25 = 0;
                        }
                        sortKeyLevel6.appendWeight16(i17);
                    } else {
                        if (i25 != 0) {
                            int i37 = i25 - 1;
                            int i38 = i37 % 33;
                            sortKeyLevel6.appendByte(i26 < 1280 ? i38 + 5 : 69 - i38);
                            i25 = i37 - i38;
                            while (i25 > 0) {
                                sortKeyLevel6.appendByte(37);
                                i25 -= 33;
                            }
                        }
                        if (0 >= j || j > Collation.MERGE_SEPARATOR_PRIMARY) {
                            sortKeyLevel6.appendReverseWeight16(i17);
                            i26 = i17;
                        } else {
                            byte[] data = sortKeyLevel6.data();
                            int length = sortKeyLevel6.length() - 1;
                            for (int i39 = i27; i39 < length; i39++) {
                                byte b2 = data[i39];
                                data[i39] = data[length];
                                data[length] = b2;
                                length--;
                            }
                            sortKeyLevel6.appendByte(j == 1 ? 1 : 2);
                            i27 = sortKeyLevel6.length();
                            i26 = 0;
                        }
                    }
                    int i40 = i22 & 8;
                    if (i40 != 0) {
                        if (CollationSettings.getStrength(i3) != 0 ? (i33 >>> 16) != 0 : j != 0) {
                            int i41 = (i33 >>> 8) & 255;
                            if ((i41 & 192) == 0) {
                                i12 = 1;
                                if (i41 > 1) {
                                    i28++;
                                }
                            } else {
                                i12 = 1;
                            }
                            if ((i3 & 256) == 0) {
                                if (i28 == 0 || (i41 <= i12 && sortKeyLevel5.isEmpty())) {
                                    i13 = 4;
                                    i16 = 1;
                                } else {
                                    int i42 = i28 - 1;
                                    while (i42 >= 7) {
                                        sortKeyLevel5.appendByte(112);
                                        i42 -= 7;
                                    }
                                    i13 = 4;
                                    sortKeyLevel5.appendByte((i41 <= 1 ? i42 + 1 : 13 - i42) << 4);
                                    i16 = 1;
                                    i28 = 0;
                                }
                                if (i41 > i16) {
                                    i14 = (i41 >>> 6) + 13;
                                }
                                sortKeyLevel5.appendByte(i41);
                            } else {
                                if (i28 != 0) {
                                    int i43 = i28 - 1;
                                    while (i43 >= 13) {
                                        sortKeyLevel5.appendByte(48);
                                        i43 -= 13;
                                    }
                                    i13 = 4;
                                    sortKeyLevel5.appendByte((i43 + 3) << 4);
                                    i15 = 1;
                                    i28 = 0;
                                } else {
                                    i13 = 4;
                                    i15 = 1;
                                }
                                if (i41 > i15) {
                                    i14 = 3 - (i41 >>> 6);
                                }
                                sortKeyLevel5.appendByte(i41);
                            }
                            i41 = i14 << i13;
                            sortKeyLevel5.appendByte(i41);
                        }
                    }
                    int i44 = i22 & 16;
                    if (i44 != 0) {
                        int i45 = i33 & i4;
                        if (i45 == 1280) {
                            i29++;
                        } else {
                            if ((i4 & 32768) == 0) {
                                if (i29 != 0) {
                                    int i46 = i29 - 1;
                                    while (i46 >= 97) {
                                        sortKeyLevel.appendByte(101);
                                        i46 -= 97;
                                        i35 = 1280;
                                    }
                                    i11 = i35;
                                    sortKeyLevel3 = sortKeyLevel;
                                    sortKeyLevel3.appendByte(i45 < i11 ? i46 + 5 : 197 - i46);
                                    i29 = 0;
                                } else {
                                    i11 = 1280;
                                    sortKeyLevel3 = sortKeyLevel;
                                }
                                if (i45 > i11) {
                                    i45 += Collation.CASE_MASK;
                                }
                                sortKeyLevel3.appendWeight16(i45);
                            } else {
                                sortKeyLevel3 = sortKeyLevel;
                                if ((i3 & 256) == 0) {
                                    if (i29 != 0) {
                                        int i47 = i29 - 1;
                                        while (i47 >= 33) {
                                            sortKeyLevel3.appendByte(37);
                                            i47 -= 33;
                                        }
                                        i10 = 1280;
                                        sortKeyLevel3.appendByte(i45 < 1280 ? i47 + 5 : 69 - i47);
                                        i29 = 0;
                                    } else {
                                        i10 = 1280;
                                    }
                                    if (i45 > i10) {
                                        i45 += 16384;
                                    }
                                    sortKeyLevel3.appendWeight16(i45);
                                } else {
                                    if (i45 > 256) {
                                        if ((i33 >>> 16) != 0) {
                                            i45 ^= Collation.CASE_MASK;
                                            if (i45 < 50432) {
                                                i45 -= 16384;
                                            }
                                        } else {
                                            i45 += 16384;
                                        }
                                    }
                                    if (i29 != 0) {
                                        int i48 = i29 - 1;
                                        while (i48 >= 33) {
                                            sortKeyLevel3.appendByte(165);
                                            i48 -= 33;
                                        }
                                        sortKeyLevel3.appendByte(i45 < 34048 ? i48 + 133 : 197 - i48);
                                        i29 = 0;
                                    }
                                    sortKeyLevel3.appendWeight16(i45);
                                }
                            }
                            i5 = i22 & 32;
                            if (i5 != 0) {
                                int i49 = 65535 & i33;
                                if ((i49 & 192) == 0) {
                                    i9 = 256;
                                    if (i49 > 256) {
                                        i24++;
                                    }
                                } else {
                                    i9 = 256;
                                }
                                if (i49 == i9 && i2 == 0 && sortKeyLevel2.isEmpty()) {
                                    sortKeyLevel4 = sortKeyLevel2;
                                    i7 = i22;
                                    sortKeyLevel4.appendByte(1);
                                    i6 = i3;
                                    if ((i33 >>> 24) != 1) {
                                    }
                                } else {
                                    sortKeyLevel4 = sortKeyLevel2;
                                    i7 = i22;
                                    int i50 = i49 == 256 ? 1 : ((i49 >>> 6) & 3) + 252;
                                    if (i24 != 0) {
                                        i6 = i3;
                                        int i51 = i24 - 1;
                                        while (i51 >= 113) {
                                            sortKeyLevel4.appendByte(140);
                                            i51 -= 113;
                                        }
                                        sortKeyLevel4.appendByte(i50 < 28 ? i51 + 28 : 252 - i51);
                                        i24 = 0;
                                    } else {
                                        i6 = i3;
                                    }
                                    sortKeyLevel4.appendByte(i50);
                                    if ((i33 >>> 24) != 1) {
                                        if (i34 != 0) {
                                            if (levelCallback.needToWrite(2)) {
                                                sortKeyByteSink.Append(1);
                                                sortKeyLevel6.appendTo(sortKeyByteSink);
                                            } else {
                                                return;
                                            }
                                        }
                                        if (i40 != 0) {
                                            if (levelCallback.needToWrite(3)) {
                                                sortKeyByteSink.Append(1);
                                                int length2 = sortKeyLevel5.length() - 1;
                                                byte b3 = 0;
                                                for (int i52 = 0; i52 < length2; i52++) {
                                                    byte at = sortKeyLevel5.getAt(i52);
                                                    if (b3 == 0) {
                                                        b3 = at;
                                                    } else {
                                                        sortKeyByteSink.Append(b3 | ((at >> 4) & 15));
                                                        b3 = 0;
                                                    }
                                                }
                                                if (b3 != 0) {
                                                    sortKeyByteSink.Append(b3);
                                                }
                                            } else {
                                                return;
                                            }
                                        }
                                        if (i44 == 0) {
                                            i8 = 1;
                                        } else if (levelCallback.needToWrite(4)) {
                                            i8 = 1;
                                            sortKeyByteSink.Append(1);
                                            sortKeyLevel3.appendTo(sortKeyByteSink);
                                        } else {
                                            return;
                                        }
                                        if (i5 != 0 && levelCallback.needToWrite(5)) {
                                            sortKeyByteSink.Append(i8);
                                            sortKeyLevel4.appendTo(sortKeyByteSink);
                                            return;
                                        }
                                        return;
                                    }
                                    collationSettings2 = collationSettings;
                                    sortKeyLevel8 = sortKeyLevel4;
                                    i22 = i7;
                                    i20 = i6;
                                    i23 = i2;
                                    c2 = ' ';
                                    j6 = j3;
                                    tertiaryMask = i4;
                                    sortKeyLevel7 = sortKeyLevel3;
                                    j5 = j5;
                                }
                            }
                            i6 = i3;
                            sortKeyLevel4 = sortKeyLevel2;
                            i7 = i22;
                            if ((i33 >>> 24) != 1) {
                            }
                        }
                    }
                    sortKeyLevel3 = sortKeyLevel;
                    i5 = i22 & 32;
                    if (i5 != 0) {
                    }
                    i6 = i3;
                    sortKeyLevel4 = sortKeyLevel2;
                    i7 = i22;
                    if ((i33 >>> 24) != 1) {
                    }
                }
            }
        }
    }
}
