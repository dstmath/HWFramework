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

    public TimeZoneRule(String name, int rawOffset, int dstSavings) {
        this.name = name;
        this.rawOffset = rawOffset;
        this.dstSavings = dstSavings;
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
        StringBuilder buf = new StringBuilder();
        buf.append("name=").append(this.name);
        buf.append(", stdOffset=").append(this.rawOffset);
        buf.append(", dstSaving=").append(this.dstSavings);
        return buf.toString();
    }
}
