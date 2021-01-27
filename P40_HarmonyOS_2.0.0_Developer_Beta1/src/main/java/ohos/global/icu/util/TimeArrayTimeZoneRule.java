package ohos.global.icu.util;

import java.util.Arrays;
import java.util.Date;

public class TimeArrayTimeZoneRule extends TimeZoneRule {
    private static final long serialVersionUID = -1117109130077415245L;
    private final long[] startTimes;
    private final int timeType;

    @Override // ohos.global.icu.util.TimeZoneRule
    public boolean isTransitionRule() {
        return true;
    }

    public TimeArrayTimeZoneRule(String str, int i, int i2, long[] jArr, int i3) {
        super(str, i, i2);
        if (jArr == null || jArr.length == 0) {
            throw new IllegalArgumentException("No start times are specified.");
        }
        this.startTimes = (long[]) jArr.clone();
        Arrays.sort(this.startTimes);
        this.timeType = i3;
    }

    public long[] getStartTimes() {
        return (long[]) this.startTimes.clone();
    }

    public int getTimeType() {
        return this.timeType;
    }

    @Override // ohos.global.icu.util.TimeZoneRule
    public Date getFirstStart(int i, int i2) {
        return new Date(getUTC(this.startTimes[0], i, i2));
    }

    @Override // ohos.global.icu.util.TimeZoneRule
    public Date getFinalStart(int i, int i2) {
        long[] jArr = this.startTimes;
        return new Date(getUTC(jArr[jArr.length - 1], i, i2));
    }

    @Override // ohos.global.icu.util.TimeZoneRule
    public Date getNextStart(long j, int i, int i2, boolean z) {
        int length = this.startTimes.length - 1;
        while (length >= 0) {
            int i3 = (getUTC(this.startTimes[length], i, i2) > j ? 1 : (getUTC(this.startTimes[length], i, i2) == j ? 0 : -1));
            if (i3 < 0 || (!z && i3 == 0)) {
                break;
            }
            length--;
        }
        long[] jArr = this.startTimes;
        if (length == jArr.length - 1) {
            return null;
        }
        return new Date(getUTC(jArr[length + 1], i, i2));
    }

    @Override // ohos.global.icu.util.TimeZoneRule
    public Date getPreviousStart(long j, int i, int i2, boolean z) {
        for (int length = this.startTimes.length - 1; length >= 0; length--) {
            long utc = getUTC(this.startTimes[length], i, i2);
            int i3 = (utc > j ? 1 : (utc == j ? 0 : -1));
            if (i3 < 0 || (z && i3 == 0)) {
                return new Date(utc);
            }
        }
        return null;
    }

    @Override // ohos.global.icu.util.TimeZoneRule
    public boolean isEquivalentTo(TimeZoneRule timeZoneRule) {
        if (!(timeZoneRule instanceof TimeArrayTimeZoneRule)) {
            return false;
        }
        TimeArrayTimeZoneRule timeArrayTimeZoneRule = (TimeArrayTimeZoneRule) timeZoneRule;
        if (this.timeType != timeArrayTimeZoneRule.timeType || !Arrays.equals(this.startTimes, timeArrayTimeZoneRule.startTimes)) {
            return false;
        }
        return super.isEquivalentTo(timeZoneRule);
    }

    private long getUTC(long j, int i, int i2) {
        if (this.timeType != 2) {
            j -= (long) i;
        }
        return this.timeType == 0 ? j - ((long) i2) : j;
    }

    @Override // ohos.global.icu.util.TimeZoneRule, java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", timeType=");
        sb.append(this.timeType);
        sb.append(", startTimes=[");
        for (int i = 0; i < this.startTimes.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(Long.toString(this.startTimes[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
