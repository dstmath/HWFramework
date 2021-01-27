package ohos.global.icu.util;

import java.io.Serializable;
import java.util.Date;

public abstract class TimeZoneRule implements Serializable {
    private static final long serialVersionUID = 6374143828553768100L;
    private final int dstSavings;
    private final String name;
    private final int rawOffset;

    public abstract Date getFinalStart(int i, int i2);

    public abstract Date getFirstStart(int i, int i2);

    public abstract Date getNextStart(long j, int i, int i2, boolean z);

    public abstract Date getPreviousStart(long j, int i, int i2, boolean z);

    public abstract boolean isTransitionRule();

    public TimeZoneRule(String str, int i, int i2) {
        this.name = str;
        this.rawOffset = i;
        this.dstSavings = i2;
    }

    public String getName() {
        return this.name;
    }

    public int getRawOffset() {
        return this.rawOffset;
    }

    public int getDSTSavings() {
        return this.dstSavings;
    }

    public boolean isEquivalentTo(TimeZoneRule timeZoneRule) {
        return this.rawOffset == timeZoneRule.rawOffset && this.dstSavings == timeZoneRule.dstSavings;
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=" + this.name);
        sb.append(", stdOffset=" + this.rawOffset);
        sb.append(", dstSaving=" + this.dstSavings);
        return sb.toString();
    }
}
