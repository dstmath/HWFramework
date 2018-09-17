package android.icu.impl;

import android.icu.text.UTF16;
import android.icu.text.UnicodeMatcher;
import com.android.dex.DexFormat;
import java.text.CharacterIterator;
import libcore.icu.DateUtilsBridge;

public final class CharacterIteration {
    public static final int DONE32 = Integer.MAX_VALUE;

    private CharacterIteration() {
    }

    public static int next32(CharacterIterator ci) {
        int c = ci.current();
        if (c >= UTF16.SURROGATE_MIN_VALUE && c <= UTF16.LEAD_SURROGATE_MAX_VALUE) {
            c = ci.next();
            if (c < UTF16.TRAIL_SURROGATE_MIN_VALUE || c > UTF16.TRAIL_SURROGATE_MAX_VALUE) {
                c = ci.previous();
            }
        }
        c = ci.next();
        if (c >= UTF16.SURROGATE_MIN_VALUE) {
            c = nextTrail32(ci, c);
        }
        if (c >= DateUtilsBridge.FORMAT_ABBREV_MONTH && c != DONE32) {
            ci.previous();
        }
        return c;
    }

    public static int nextTrail32(CharacterIterator ci, int lead) {
        if (lead == DexFormat.MAX_TYPE_IDX && ci.getIndex() >= ci.getEndIndex()) {
            return DONE32;
        }
        int retVal = lead;
        if (lead <= UTF16.LEAD_SURROGATE_MAX_VALUE) {
            char cTrail = ci.next();
            if (UTF16.isTrailSurrogate(cTrail)) {
                retVal = (((lead - UTF16.SURROGATE_MIN_VALUE) << 10) + (cTrail - UTF16.TRAIL_SURROGATE_MIN_VALUE)) + DateUtilsBridge.FORMAT_ABBREV_MONTH;
            } else {
                ci.previous();
            }
        }
        return retVal;
    }

    public static int previous32(CharacterIterator ci) {
        if (ci.getIndex() <= ci.getBeginIndex()) {
            return DONE32;
        }
        char trail = ci.previous();
        int retVal = trail;
        if (UTF16.isTrailSurrogate(trail) && ci.getIndex() > ci.getBeginIndex()) {
            char lead = ci.previous();
            if (UTF16.isLeadSurrogate(lead)) {
                retVal = (((lead - UTF16.SURROGATE_MIN_VALUE) << 10) + (trail - UTF16.TRAIL_SURROGATE_MIN_VALUE)) + DateUtilsBridge.FORMAT_ABBREV_MONTH;
            } else {
                ci.next();
            }
        }
        return retVal;
    }

    public static int current32(CharacterIterator ci) {
        char lead = ci.current();
        int retVal = lead;
        if (retVal < UTF16.SURROGATE_MIN_VALUE) {
            return retVal;
        }
        if (UTF16.isLeadSurrogate(lead)) {
            int trail = ci.next();
            ci.previous();
            if (UTF16.isTrailSurrogate((char) trail)) {
                retVal = (((lead - UTF16.SURROGATE_MIN_VALUE) << 10) + (trail - UTF16.TRAIL_SURROGATE_MIN_VALUE)) + DateUtilsBridge.FORMAT_ABBREV_MONTH;
            }
        } else if (lead == UnicodeMatcher.ETHER && ci.getIndex() >= ci.getEndIndex()) {
            retVal = DONE32;
        }
        return retVal;
    }
}
