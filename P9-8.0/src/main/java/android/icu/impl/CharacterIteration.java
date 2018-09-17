package android.icu.impl;

import android.icu.text.DateTimePatternGenerator;
import android.icu.text.UTF16;
import java.text.CharacterIterator;

public final class CharacterIteration {
    public static final int DONE32 = Integer.MAX_VALUE;

    private CharacterIteration() {
    }

    public static int next32(CharacterIterator ci) {
        int c = ci.current();
        if (c >= 55296 && c <= UTF16.LEAD_SURROGATE_MAX_VALUE) {
            c = ci.next();
            if (c < UTF16.TRAIL_SURROGATE_MIN_VALUE || c > 57343) {
                ci.previous();
            }
        }
        c = ci.next();
        if (c >= 55296) {
            c = nextTrail32(ci, c);
        }
        if (c >= 65536 && c != Integer.MAX_VALUE) {
            ci.previous();
        }
        return c;
    }

    public static int nextTrail32(CharacterIterator ci, int lead) {
        if (lead == DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH && ci.getIndex() >= ci.getEndIndex()) {
            return Integer.MAX_VALUE;
        }
        int retVal = lead;
        if (lead <= UTF16.LEAD_SURROGATE_MAX_VALUE) {
            char cTrail = ci.next();
            if (UTF16.isTrailSurrogate(cTrail)) {
                retVal = (((lead - 55296) << 10) + (cTrail - UTF16.TRAIL_SURROGATE_MIN_VALUE)) + 65536;
            } else {
                ci.previous();
            }
        }
        return retVal;
    }

    public static int previous32(CharacterIterator ci) {
        if (ci.getIndex() <= ci.getBeginIndex()) {
            return Integer.MAX_VALUE;
        }
        char trail = ci.previous();
        int retVal = trail;
        if (UTF16.isTrailSurrogate(trail) && ci.getIndex() > ci.getBeginIndex()) {
            char lead = ci.previous();
            if (UTF16.isLeadSurrogate(lead)) {
                retVal = (((lead - 55296) << 10) + (trail - UTF16.TRAIL_SURROGATE_MIN_VALUE)) + 65536;
            } else {
                ci.next();
            }
        }
        return retVal;
    }

    public static int current32(CharacterIterator ci) {
        char lead = ci.current();
        int retVal = lead;
        if (retVal < 55296) {
            return retVal;
        }
        if (UTF16.isLeadSurrogate(lead)) {
            int trail = ci.next();
            ci.previous();
            if (UTF16.isTrailSurrogate((char) trail)) {
                retVal = (((lead - 55296) << 10) + (trail - UTF16.TRAIL_SURROGATE_MIN_VALUE)) + 65536;
            }
        } else if (lead == 65535 && ci.getIndex() >= ci.getEndIndex()) {
            retVal = Integer.MAX_VALUE;
        }
        return retVal;
    }
}
