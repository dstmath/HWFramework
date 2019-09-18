package android.icu.util;

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

    public TimeZoneRule(String name2, int rawOffset2, int dstSavings2) {
        this.name = name2;
        this.rawOffset = rawOffset2;
        this.dstSavings = dstSavings2;
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

    public boolean isEquivalentTo(TimeZoneRule other) {
        if (this.rawOffset == other.rawOffset && this.dstSavings == other.dstSavings) {
            return true;
        }
        return false;
    }

    public String toString() {
        return ("name=" + this.name) + (", stdOffset=" + this.rawOffset) + (", dstSaving=" + this.dstSavings);
    }
}
