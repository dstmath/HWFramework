package android.icu.util;

import java.util.Date;

public class InitialTimeZoneRule extends TimeZoneRule {
    private static final long serialVersionUID = 1876594993064051206L;

    public InitialTimeZoneRule(String name, int rawOffset, int dstSavings) {
        super(name, rawOffset, dstSavings);
    }

    public boolean isEquivalentTo(TimeZoneRule other) {
        if (other instanceof InitialTimeZoneRule) {
            return super.isEquivalentTo(other);
        }
        return false;
    }

    public Date getFinalStart(int prevRawOffset, int prevDSTSavings) {
        return null;
    }

    public Date getFirstStart(int prevRawOffset, int prevDSTSavings) {
        return null;
    }

    public Date getNextStart(long base, int prevRawOffset, int prevDSTSavings, boolean inclusive) {
        return null;
    }

    public Date getPreviousStart(long base, int prevRawOffset, int prevDSTSavings, boolean inclusive) {
        return null;
    }

    public boolean isTransitionRule() {
        return false;
    }
}
