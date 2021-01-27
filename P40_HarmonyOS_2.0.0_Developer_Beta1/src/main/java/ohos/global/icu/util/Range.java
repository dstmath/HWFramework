package ohos.global.icu.util;

import java.util.Date;

/* compiled from: RangeDateRule */
class Range {
    public DateRule rule;
    public Date start;

    public Range(Date date, DateRule dateRule) {
        this.start = date;
        this.rule = dateRule;
    }
}
