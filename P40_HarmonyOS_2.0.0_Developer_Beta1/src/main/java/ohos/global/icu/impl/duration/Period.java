package ohos.global.icu.impl.duration;

public final class Period {
    final int[] counts;
    final boolean inFuture;
    final byte timeLimit;

    public static Period at(float f, TimeUnit timeUnit) {
        checkCount(f);
        return new Period(0, false, f, timeUnit);
    }

    public static Period moreThan(float f, TimeUnit timeUnit) {
        checkCount(f);
        return new Period(2, false, f, timeUnit);
    }

    public static Period lessThan(float f, TimeUnit timeUnit) {
        checkCount(f);
        return new Period(1, false, f, timeUnit);
    }

    public Period and(float f, TimeUnit timeUnit) {
        checkCount(f);
        return setTimeUnitValue(timeUnit, f);
    }

    public Period omit(TimeUnit timeUnit) {
        return setTimeUnitInternalValue(timeUnit, 0);
    }

    public Period at() {
        return setTimeLimit((byte) 0);
    }

    public Period moreThan() {
        return setTimeLimit((byte) 2);
    }

    public Period lessThan() {
        return setTimeLimit((byte) 1);
    }

    public Period inFuture() {
        return setFuture(true);
    }

    public Period inPast() {
        return setFuture(false);
    }

    public Period inFuture(boolean z) {
        return setFuture(z);
    }

    public Period inPast(boolean z) {
        return setFuture(!z);
    }

    public boolean isSet() {
        int i = 0;
        while (true) {
            int[] iArr = this.counts;
            if (i >= iArr.length) {
                return false;
            }
            if (iArr[i] != 0) {
                return true;
            }
            i++;
        }
    }

    public boolean isSet(TimeUnit timeUnit) {
        return this.counts[timeUnit.ordinal] > 0;
    }

    public float getCount(TimeUnit timeUnit) {
        byte b = timeUnit.ordinal;
        int[] iArr = this.counts;
        if (iArr[b] == 0) {
            return 0.0f;
        }
        return ((float) (iArr[b] - 1)) / 1000.0f;
    }

    public boolean isInFuture() {
        return this.inFuture;
    }

    public boolean isInPast() {
        return !this.inFuture;
    }

    public boolean isMoreThan() {
        return this.timeLimit == 2;
    }

    public boolean isLessThan() {
        return this.timeLimit == 1;
    }

    public boolean equals(Object obj) {
        try {
            return equals((Period) obj);
        } catch (ClassCastException unused) {
            return false;
        }
    }

    public boolean equals(Period period) {
        if (period == null || this.timeLimit != period.timeLimit || this.inFuture != period.inFuture) {
            return false;
        }
        int i = 0;
        while (true) {
            int[] iArr = this.counts;
            if (i >= iArr.length) {
                return true;
            }
            if (iArr[i] != period.counts[i]) {
                return false;
            }
            i++;
        }
    }

    public int hashCode() {
        int i = (this.timeLimit << 1) | (this.inFuture ? 1 : 0);
        int i2 = 0;
        while (true) {
            int[] iArr = this.counts;
            if (i2 >= iArr.length) {
                return i;
            }
            i = (i << 2) ^ iArr[i2];
            i2++;
        }
    }

    private Period(int i, boolean z, float f, TimeUnit timeUnit) {
        this.timeLimit = (byte) i;
        this.inFuture = z;
        this.counts = new int[TimeUnit.units.length];
        this.counts[timeUnit.ordinal] = ((int) (f * 1000.0f)) + 1;
    }

    Period(int i, boolean z, int[] iArr) {
        this.timeLimit = (byte) i;
        this.inFuture = z;
        this.counts = iArr;
    }

    private Period setTimeUnitValue(TimeUnit timeUnit, float f) {
        if (f >= 0.0f) {
            return setTimeUnitInternalValue(timeUnit, ((int) (f * 1000.0f)) + 1);
        }
        throw new IllegalArgumentException("value: " + f);
    }

    private Period setTimeUnitInternalValue(TimeUnit timeUnit, int i) {
        byte b = timeUnit.ordinal;
        int[] iArr = this.counts;
        if (iArr[b] == i) {
            return this;
        }
        int[] iArr2 = new int[iArr.length];
        int i2 = 0;
        while (true) {
            int[] iArr3 = this.counts;
            if (i2 < iArr3.length) {
                iArr2[i2] = iArr3[i2];
                i2++;
            } else {
                iArr2[b] = i;
                return new Period(this.timeLimit, this.inFuture, iArr2);
            }
        }
    }

    private Period setFuture(boolean z) {
        return this.inFuture != z ? new Period(this.timeLimit, z, this.counts) : this;
    }

    private Period setTimeLimit(byte b) {
        return this.timeLimit != b ? new Period(b, this.inFuture, this.counts) : this;
    }

    private static void checkCount(float f) {
        if (f < 0.0f) {
            throw new IllegalArgumentException("count (" + f + ") cannot be negative");
        }
    }
}
