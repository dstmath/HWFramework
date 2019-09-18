package android.icu.util;

import java.util.Date;

/* compiled from: RangeDateRule */
class Range {
    public DateRule rule;
    public Date start;

    public Range(Date start2, DateRule rule2) {
        this.start = start2;
        this.rule = rule2;
    }
}
