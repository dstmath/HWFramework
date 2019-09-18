package android.icu.impl.coll;

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
        public boolean needToWrite(int level) {
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

        public SortKeyByteSink(byte[] dest) {
            this.buffer_ = dest;
        }

        public void setBufferAndAppended(byte[] dest, int app) {
            this.buffer_ = dest;
            this.appended_ = app;
        }

        public void Append(byte[] bytes, int n) {
            if (n > 0 && bytes != null) {
                int length = this.appended_;
                this.appended_ += n;
                if (n <= this.buffer_.length - length) {
                    System.arraycopy(bytes, 0, this.buffer_, length, n);
                } else {
                    AppendBeyondCapacity(bytes, 0, n, length);
                }
            }
        }

        public void Append(int b) {
            if (this.appended_ < this.buffer_.length || Resize(1, this.appended_)) {
                this.buffer_[this.appended_] = (byte) b;
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

    private static final class SortKeyLevel {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final int INITIAL_CAPACITY = 40;
        byte[] buffer = new byte[40];
        int len = 0;

        static {
            Class<CollationKeys> cls = CollationKeys.class;
        }

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
        public byte getAt(int index) {
            return this.buffer[index];
        }

        /* access modifiers changed from: package-private */
        public byte[] data() {
            return this.buffer;
        }

        /* access modifiers changed from: package-private */
        public void appendByte(int b) {
            if (this.len < this.buffer.length || ensureCapacity(1)) {
                byte[] bArr = this.buffer;
                int i = this.len;
                this.len = i + 1;
                bArr[i] = (byte) b;
            }
        }

        /* access modifiers changed from: package-private */
        public void appendWeight16(int w) {
            byte b0 = (byte) (w >>> 8);
            byte b1 = (byte) w;
            int appendLength = b1 == 0 ? 1 : 2;
            if (this.len + appendLength <= this.buffer.length || ensureCapacity(appendLength)) {
                byte[] bArr = this.buffer;
                int i = this.len;
                this.len = i + 1;
                bArr[i] = b0;
                if (b1 != 0) {
                    byte[] bArr2 = this.buffer;
                    int i2 = this.len;
                    this.len = i2 + 1;
                    bArr2[i2] = b1;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void appendWeight32(long w) {
            int appendLength = 4;
            byte[] bytes = {(byte) ((int) (w >>> 24)), (byte) ((int) (w >>> 16)), (byte) ((int) (w >>> 8)), (byte) ((int) w)};
            if (bytes[1] == 0) {
                appendLength = 1;
            } else if (bytes[2] == 0) {
                appendLength = 2;
            } else if (bytes[3] == 0) {
                appendLength = 3;
            }
            if (this.len + appendLength <= this.buffer.length || ensureCapacity(appendLength)) {
                byte[] bArr = this.buffer;
                int i = this.len;
                this.len = i + 1;
                bArr[i] = bytes[0];
                if (bytes[1] != 0) {
                    byte[] bArr2 = this.buffer;
                    int i2 = this.len;
                    this.len = i2 + 1;
                    bArr2[i2] = bytes[1];
                    if (bytes[2] != 0) {
                        byte[] bArr3 = this.buffer;
                        int i3 = this.len;
                        this.len = i3 + 1;
                        bArr3[i3] = bytes[2];
                        if (bytes[3] != 0) {
                            byte[] bArr4 = this.buffer;
                            int i4 = this.len;
                            this.len = i4 + 1;
                            bArr4[i4] = bytes[3];
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void appendReverseWeight16(int w) {
            byte b0 = (byte) (w >>> 8);
            byte b1 = (byte) w;
            int appendLength = b1 == 0 ? 1 : 2;
            if (this.len + appendLength > this.buffer.length && !ensureCapacity(appendLength)) {
                return;
            }
            if (b1 == 0) {
                byte[] bArr = this.buffer;
                int i = this.len;
                this.len = i + 1;
                bArr[i] = b0;
                return;
            }
            this.buffer[this.len] = b1;
            this.buffer[this.len + 1] = b0;
            this.len += 2;
        }

        /* access modifiers changed from: package-private */
        public void appendTo(SortKeyByteSink sink) {
            sink.Append(this.buffer, this.len - 1);
        }

        private boolean ensureCapacity(int appendCapacity) {
            int newCapacity = this.buffer.length * 2;
            int altCapacity = this.len + (2 * appendCapacity);
            if (newCapacity < altCapacity) {
                newCapacity = altCapacity;
            }
            if (newCapacity < 200) {
                newCapacity = 200;
            }
            byte[] newbuf = new byte[newCapacity];
            System.arraycopy(this.buffer, 0, newbuf, 0, this.len);
            this.buffer = newbuf;
            return true;
        }
    }

    private static SortKeyLevel getSortKeyLevel(int levels, int level) {
        if ((levels & level) != 0) {
            return new SortKeyLevel();
        }
        return null;
    }

    private CollationKeys() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:111:0x01d6  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0276  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x0280 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:168:0x02b6  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x02e6  */
    /* JADX WARNING: Removed duplicated region for block: B:234:0x039f  */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x03a5  */
    /* JADX WARNING: Removed duplicated region for block: B:297:0x046b  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x03f8 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x01a8  */
    public static void writeSortKeyUpToQuaternary(CollationIterator iter, boolean[] compressibleBytes, CollationSettings settings, SortKeyByteSink sink, int minLevel, LevelCallback callback, boolean preflight) {
        long variableTop;
        int options;
        SortKeyLevel tertiaries;
        long p;
        long ce;
        byte[] p234;
        long prevReorderedPrimary;
        byte[] p2342;
        int options2;
        SortKeyLevel tertiaries2;
        LevelCallback levelCallback;
        long variableTop2;
        int q;
        int b;
        int b2;
        int b3;
        int b4;
        int c;
        int b5;
        int prevSecondary;
        byte[] p2343;
        long prevReorderedPrimary2;
        int i;
        int b6;
        int b7;
        int commonQuaternaries;
        long ce2;
        CollationSettings collationSettings = settings;
        SortKeyByteSink sortKeyByteSink = sink;
        LevelCallback levelCallback2 = callback;
        int options3 = collationSettings.options;
        int levels = levelMasks[CollationSettings.getStrength(options3)];
        if ((options3 & 1024) != 0) {
            levels |= 8;
        }
        byte levels2 = levels & (~((1 << minLevel) - 1));
        if (levels2 != 0) {
            if ((options3 & 12) == 0) {
                variableTop = 0;
            } else {
                variableTop = collationSettings.variableTop + 1;
            }
            int tertiaryMask = CollationSettings.getTertiaryMask(options3);
            SortKeyLevel cases = getSortKeyLevel(levels2, 8);
            SortKeyLevel secondaries = getSortKeyLevel(levels2, 4);
            SortKeyLevel tertiaries3 = getSortKeyLevel(levels2, 16);
            char c2 = ' ';
            SortKeyLevel quaternaries = getSortKeyLevel(levels2, 32);
            int commonCases = 0;
            int commonSecondaries = 0;
            int commonTertiaries = 0;
            int commonQuaternaries2 = 0;
            int secSegmentStart = 0;
            byte[] p2344 = new byte[3];
            long prevReorderedPrimary3 = 0;
            int prevSecondary2 = 0;
            while (true) {
                iter.clearCEsIfNoneRemaining();
                long ce3 = iter.nextCE();
                long p2 = ce3 >>> c2;
                if (p2 >= variableTop || p2 <= Collation.MERGE_SEPARATOR_PRIMARY) {
                    tertiaries = tertiaries3;
                    char c3 = c2;
                    options = options3;
                    ce = ce3;
                    p = p2;
                } else {
                    if (commonQuaternaries2 != 0) {
                        int commonQuaternaries3 = commonQuaternaries2 - 1;
                        while (commonQuaternaries3 >= 113) {
                            quaternaries.appendByte(140);
                            commonQuaternaries3 -= 113;
                        }
                        quaternaries.appendByte(28 + commonQuaternaries3);
                        commonQuaternaries2 = 0;
                    }
                    tertiaries = tertiaries3;
                    int commonQuaternaries4 = commonQuaternaries2;
                    p = p2;
                    while (true) {
                        if ((levels2 & 32) != 0) {
                            if (settings.hasReordering()) {
                                p = collationSettings.reorder(p);
                            }
                            commonQuaternaries = commonQuaternaries4;
                            if ((((int) p) >>> 24) >= 27) {
                                quaternaries.appendByte(27);
                            }
                            quaternaries.appendWeight32(p);
                        } else {
                            commonQuaternaries = commonQuaternaries4;
                        }
                        do {
                            ce2 = iter.nextCE();
                            p = ce2 >>> 32;
                        } while (p == 0);
                        if (p >= variableTop || p <= Collation.MERGE_SEPARATOR_PRIMARY) {
                            options = options3;
                            ce = ce2;
                            commonQuaternaries2 = commonQuaternaries;
                        } else {
                            commonQuaternaries4 = commonQuaternaries;
                            LevelCallback levelCallback3 = callback;
                        }
                    }
                    options = options3;
                    ce = ce2;
                    commonQuaternaries2 = commonQuaternaries;
                }
                long variableTop3 = variableTop;
                if (p <= 1 || (levels2 & 2) == 0) {
                    p234 = p2344;
                } else {
                    boolean isCompressible = compressibleBytes[((int) p) >>> 24];
                    if (settings.hasReordering()) {
                        p = collationSettings.reorder(p);
                    }
                    int p1 = ((int) p) >>> 24;
                    if (!isCompressible || p1 != (((int) prevReorderedPrimary3) >>> 24)) {
                        if (prevReorderedPrimary3 != 0) {
                            if (p >= prevReorderedPrimary3) {
                                sortKeyByteSink.Append(255);
                            } else if (p1 > 2) {
                                sortKeyByteSink.Append(3);
                            }
                        }
                        sortKeyByteSink.Append(p1);
                        if (isCompressible) {
                            prevReorderedPrimary3 = p;
                        } else {
                            prevReorderedPrimary3 = 0;
                        }
                    }
                    int i2 = p1;
                    boolean z = isCompressible;
                    byte p22 = (byte) ((int) (p >>> 16));
                    if (p22 != 0) {
                        p2344[0] = p22;
                        p2344[1] = (byte) ((int) (p >>> 8));
                        p2344[2] = (byte) ((int) p);
                        p234 = p2344;
                        sortKeyByteSink = sink;
                        sortKeyByteSink.Append(p234, p2344[1] == 0 ? 1 : p2344[2] == 0 ? 2 : 3);
                    } else {
                        p234 = p2344;
                    }
                    if (!preflight && sink.Overflowed()) {
                        return;
                    }
                }
                int lower32 = (int) ce;
                if (lower32 == 0) {
                    p2344 = p234;
                    tertiaries3 = tertiaries;
                    options3 = options;
                    variableTop2 = variableTop3;
                    collationSettings = settings;
                    LevelCallback levelCallback4 = callback;
                    c2 = ' ';
                } else {
                    if ((levels2 & 4) != 0) {
                        int prevSecondary3 = lower32 >>> 16;
                        if (prevSecondary3 == 0) {
                            long j = ce;
                            p2343 = p234;
                            prevReorderedPrimary2 = prevReorderedPrimary3;
                            prevSecondary = prevSecondary2;
                            options2 = options;
                        } else {
                            long j2 = ce;
                            if (prevSecondary3 == 1280) {
                                options2 = options;
                                if ((options2 & 2048) == 0 || p != Collation.MERGE_SEPARATOR_PRIMARY) {
                                    commonSecondaries++;
                                    p2342 = p234;
                                }
                                if ((options2 & 2048) != 0) {
                                    if (commonSecondaries != 0) {
                                        int commonSecondaries2 = commonSecondaries - 1;
                                        while (true) {
                                            p2342 = p234;
                                            if (commonSecondaries2 < 33) {
                                                break;
                                            }
                                            secondaries.appendByte(37);
                                            commonSecondaries2 -= 33;
                                            p234 = p2342;
                                        }
                                        if (prevSecondary3 < 1280) {
                                            b7 = 5 + commonSecondaries2;
                                        } else {
                                            b7 = 69 - commonSecondaries2;
                                        }
                                        secondaries.appendByte(b7);
                                        commonSecondaries = 0;
                                    } else {
                                        p2342 = p234;
                                    }
                                    secondaries.appendWeight16(prevSecondary3);
                                } else {
                                    p2342 = p234;
                                    if (commonSecondaries != 0) {
                                        int commonSecondaries3 = commonSecondaries - 1;
                                        int remainder = commonSecondaries3 % 33;
                                        prevReorderedPrimary = prevReorderedPrimary3;
                                        if (prevSecondary2 < 1280) {
                                            b6 = 5 + remainder;
                                        } else {
                                            b6 = 69 - remainder;
                                        }
                                        secondaries.appendByte(b6);
                                        commonSecondaries = commonSecondaries3 - remainder;
                                        while (commonSecondaries > 0) {
                                            secondaries.appendByte(37);
                                            commonSecondaries -= 33;
                                        }
                                    } else {
                                        prevReorderedPrimary = prevReorderedPrimary3;
                                        int i3 = prevSecondary2;
                                    }
                                    if (0 >= p || p > Collation.MERGE_SEPARATOR_PRIMARY) {
                                        secondaries.appendReverseWeight16(prevSecondary3);
                                        prevSecondary2 = prevSecondary3;
                                        if ((levels2 & 8) != 0 && (CollationSettings.getStrength(options2) != 0 ? (lower32 >>> 16) != 0 : p != 0)) {
                                            c = (lower32 >>> 8) & 255;
                                            if ((c & 192) == 0 || c <= 1) {
                                                if ((options2 & 256) != 0) {
                                                    if (commonCases != 0 && (c > 1 || !cases.isEmpty())) {
                                                        int commonCases2 = commonCases - 1;
                                                        while (commonCases2 >= 7) {
                                                            cases.appendByte(112);
                                                            commonCases2 -= 7;
                                                        }
                                                        if (c <= 1) {
                                                            b5 = 1 + commonCases2;
                                                        } else {
                                                            b5 = 13 - commonCases2;
                                                        }
                                                        cases.appendByte(b5 << 4);
                                                        commonCases = 0;
                                                    }
                                                    if (c > 1) {
                                                        c = (13 + (c >>> 6)) << 4;
                                                    }
                                                } else {
                                                    if (commonCases != 0) {
                                                        int commonCases3 = commonCases - 1;
                                                        while (commonCases3 >= 13) {
                                                            cases.appendByte(48);
                                                            commonCases3 -= 13;
                                                        }
                                                        cases.appendByte((3 + commonCases3) << 4);
                                                        commonCases = 0;
                                                    }
                                                    if (c > 1) {
                                                        c = (3 - (c >>> 6)) << 4;
                                                    }
                                                }
                                                cases.appendByte(c);
                                            } else {
                                                commonCases++;
                                            }
                                        }
                                        if ((levels2 & 16) == 0) {
                                            int t = lower32 & tertiaryMask;
                                            if (t == 1280) {
                                                commonTertiaries++;
                                                tertiaries2 = tertiaries;
                                            } else if ((32768 & tertiaryMask) == 0) {
                                                if (commonTertiaries != 0) {
                                                    int commonTertiaries2 = commonTertiaries - 1;
                                                    while (commonTertiaries2 >= 97) {
                                                        tertiaries.appendByte(101);
                                                        commonTertiaries2 -= 97;
                                                    }
                                                    tertiaries2 = tertiaries;
                                                    if (t < 1280) {
                                                        b4 = 5 + commonTertiaries2;
                                                    } else {
                                                        b4 = 197 - commonTertiaries2;
                                                    }
                                                    tertiaries2.appendByte(b4);
                                                    commonTertiaries = 0;
                                                } else {
                                                    tertiaries2 = tertiaries;
                                                }
                                                if (t > 1280) {
                                                    t += Collation.CASE_MASK;
                                                }
                                                tertiaries2.appendWeight16(t);
                                            } else {
                                                tertiaries2 = tertiaries;
                                                if ((options2 & 256) == 0) {
                                                    if (commonTertiaries != 0) {
                                                        int commonTertiaries3 = commonTertiaries - 1;
                                                        while (commonTertiaries3 >= 33) {
                                                            tertiaries2.appendByte(37);
                                                            commonTertiaries3 -= 33;
                                                        }
                                                        if (t < 1280) {
                                                            b3 = 5 + commonTertiaries3;
                                                        } else {
                                                            b3 = 69 - commonTertiaries3;
                                                        }
                                                        tertiaries2.appendByte(b3);
                                                        commonTertiaries = 0;
                                                    }
                                                    if (t > 1280) {
                                                        t += 16384;
                                                    }
                                                    tertiaries2.appendWeight16(t);
                                                } else {
                                                    if (t > 256) {
                                                        if ((lower32 >>> 16) != 0) {
                                                            t ^= Collation.CASE_MASK;
                                                            if (t < 50432) {
                                                                t -= 16384;
                                                            }
                                                        } else {
                                                            t += 16384;
                                                        }
                                                    }
                                                    if (commonTertiaries != 0) {
                                                        int commonTertiaries4 = commonTertiaries - 1;
                                                        while (commonTertiaries4 >= 33) {
                                                            tertiaries2.appendByte(165);
                                                            commonTertiaries4 -= 33;
                                                        }
                                                        if (t < 34048) {
                                                            b2 = 133 + commonTertiaries4;
                                                        } else {
                                                            b2 = 197 - commonTertiaries4;
                                                        }
                                                        tertiaries2.appendByte(b2);
                                                        commonTertiaries = 0;
                                                    }
                                                    tertiaries2.appendWeight16(t);
                                                }
                                            }
                                        } else {
                                            tertiaries2 = tertiaries;
                                        }
                                        if ((levels2 & 32) != 0) {
                                            int q2 = 65535 & lower32;
                                            if ((q2 & 192) == 0 && q2 > 256) {
                                                commonQuaternaries2++;
                                            } else if (q2 == 256 && (options2 & 12) == 0 && quaternaries.isEmpty()) {
                                                quaternaries.appendByte(1);
                                            } else {
                                                if (q2 == 256) {
                                                    q = 1;
                                                } else {
                                                    q = 252 + ((q2 >>> 6) & 3);
                                                }
                                                if (commonQuaternaries2 != 0) {
                                                    int commonQuaternaries5 = commonQuaternaries2 - 1;
                                                    while (commonQuaternaries5 >= 113) {
                                                        quaternaries.appendByte(140);
                                                        commonQuaternaries5 -= 113;
                                                    }
                                                    if (q < 28) {
                                                        b = 28 + commonQuaternaries5;
                                                    } else {
                                                        b = 252 - commonQuaternaries5;
                                                    }
                                                    quaternaries.appendByte(b);
                                                    commonQuaternaries2 = 0;
                                                }
                                                quaternaries.appendByte(q);
                                            }
                                        }
                                        if ((lower32 >>> 24) != 1) {
                                            if ((levels2 & 4) != 0) {
                                                levelCallback = callback;
                                                if (levelCallback.needToWrite(2)) {
                                                    sortKeyByteSink.Append(1);
                                                    secondaries.appendTo(sortKeyByteSink);
                                                } else {
                                                    return;
                                                }
                                            } else {
                                                levelCallback = callback;
                                            }
                                            if ((levels2 & 8) != 0) {
                                                if (levelCallback.needToWrite(3)) {
                                                    sortKeyByteSink.Append(1);
                                                    int length = cases.length() - 1;
                                                    byte b8 = 0;
                                                    for (int i4 = 0; i4 < length; i4++) {
                                                        byte c4 = cases.getAt(i4);
                                                        if (b8 == 0) {
                                                            b8 = c4;
                                                        } else {
                                                            sortKeyByteSink.Append(((c4 >> 4) & 15) | b8);
                                                            b8 = 0;
                                                        }
                                                    }
                                                    if (b8 != 0) {
                                                        sortKeyByteSink.Append(b8);
                                                    }
                                                } else {
                                                    return;
                                                }
                                            }
                                            if ((levels2 & 16) != 0) {
                                                if (levelCallback.needToWrite(4)) {
                                                    sortKeyByteSink.Append(1);
                                                    tertiaries2.appendTo(sortKeyByteSink);
                                                } else {
                                                    return;
                                                }
                                            }
                                            if ((levels2 & 32) != 0 && levelCallback.needToWrite(5)) {
                                                sortKeyByteSink.Append(1);
                                                quaternaries.appendTo(sortKeyByteSink);
                                                return;
                                            }
                                            return;
                                        }
                                        options3 = options2;
                                        tertiaries3 = tertiaries2;
                                        variableTop2 = variableTop3;
                                        p2344 = p2342;
                                        prevReorderedPrimary3 = prevReorderedPrimary;
                                        c2 = ' ';
                                        LevelCallback levelCallback5 = callback;
                                        collationSettings = settings;
                                    } else {
                                        byte[] secs = secondaries.data();
                                        int last = secondaries.length() - 1;
                                        for (int last2 = secSegmentStart; last2 < last; last2++) {
                                            byte b9 = secs[last2];
                                            secs[last2] = secs[last];
                                            secs[last] = b9;
                                            last--;
                                        }
                                        if (p == 1) {
                                            byte[] bArr = secs;
                                            i = 1;
                                        } else {
                                            byte[] bArr2 = secs;
                                            i = 2;
                                        }
                                        secondaries.appendByte(i);
                                        prevSecondary2 = 0;
                                        secSegmentStart = secondaries.length();
                                        c = (lower32 >>> 8) & 255;
                                        if ((c & 192) == 0) {
                                        }
                                        if ((options2 & 256) != 0) {
                                        }
                                        cases.appendByte(c);
                                        if ((levels2 & 16) == 0) {
                                        }
                                        if ((levels2 & 32) != 0) {
                                        }
                                        if ((lower32 >>> 24) != 1) {
                                        }
                                    }
                                }
                            } else {
                                options2 = options;
                                if ((options2 & 2048) != 0) {
                                }
                            }
                            prevReorderedPrimary = prevReorderedPrimary3;
                            c = (lower32 >>> 8) & 255;
                            if ((c & 192) == 0) {
                            }
                            if ((options2 & 256) != 0) {
                            }
                            cases.appendByte(c);
                            if ((levels2 & 16) == 0) {
                            }
                            if ((levels2 & 32) != 0) {
                            }
                            if ((lower32 >>> 24) != 1) {
                            }
                        }
                    } else {
                        p2343 = p234;
                        prevReorderedPrimary2 = prevReorderedPrimary3;
                        prevSecondary = prevSecondary2;
                        options2 = options;
                    }
                    prevSecondary2 = prevSecondary;
                    c = (lower32 >>> 8) & 255;
                    if ((c & 192) == 0) {
                    }
                    if ((options2 & 256) != 0) {
                    }
                    cases.appendByte(c);
                    if ((levels2 & 16) == 0) {
                    }
                    if ((levels2 & 32) != 0) {
                    }
                    if ((lower32 >>> 24) != 1) {
                    }
                }
            }
        }
    }
}
