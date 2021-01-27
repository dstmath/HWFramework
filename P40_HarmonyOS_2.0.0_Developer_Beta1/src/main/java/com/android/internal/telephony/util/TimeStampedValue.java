package com.android.internal.telephony.util;

public final class TimeStampedValue<T> {
    public final long mElapsedRealtime;
    public final T mValue;

    public TimeStampedValue(T value, long elapsedRealtime) {
        this.mValue = value;
        this.mElapsedRealtime = elapsedRealtime;
    }

    public boolean equals(Object o) {
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
        T t = this.mValue;
        if (t != null) {
            return t.equals(that.mValue);
        }
        if (that.mValue == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        T t = this.mValue;
        int result = t != null ? t.hashCode() : 0;
        long j = this.mElapsedRealtime;
        return (result * 31) + ((int) (j ^ (j >>> 32)));
    }

    public String toString() {
        return "TimeStampedValue{mValue=" + ((Object) this.mValue) + ", elapsedRealtime=" + this.mElapsedRealtime + '}';
    }
}
