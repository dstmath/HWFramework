package com.android.internal.telephony.util;

public final class TimeStampedValue<T> {
    public final long mElapsedRealtime;
    public final T mValue;

    public TimeStampedValue(T value, long elapsedRealtime) {
        this.mValue = value;
        this.mElapsedRealtime = elapsedRealtime;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeStampedValue<?> that = (TimeStampedValue) o;
        if (this.mElapsedRealtime != that.mElapsedRealtime) {
            return false;
        }
        if (this.mValue != null) {
            z = this.mValue.equals(that.mValue);
        } else if (that.mValue != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * (this.mValue != null ? this.mValue.hashCode() : 0)) + ((int) (this.mElapsedRealtime ^ (this.mElapsedRealtime >>> 32)));
    }

    public String toString() {
        return "TimeStampedValue{mValue=" + this.mValue + ", elapsedRealtime=" + this.mElapsedRealtime + '}';
    }
}
