package ohos.global.icu.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RangeDateRule implements DateRule {
    List<Range> ranges = new ArrayList(2);

    public void add(DateRule dateRule) {
        add(new Date(Long.MIN_VALUE), dateRule);
    }

    public void add(Date date, DateRule dateRule) {
        this.ranges.add(new Range(date, dateRule));
    }

    @Override // ohos.global.icu.util.DateRule
    public Date firstAfter(Date date) {
        int startIndex = startIndex(date);
        if (startIndex == this.ranges.size()) {
            startIndex = 0;
        }
        Range rangeAt = rangeAt(startIndex);
        Range rangeAt2 = rangeAt(startIndex + 1);
        if (rangeAt == null || rangeAt.rule == null) {
            return null;
        }
        if (rangeAt2 != null) {
            return rangeAt.rule.firstBetween(date, rangeAt2.start);
        }
        return rangeAt.rule.firstAfter(date);
    }

    @Override // ohos.global.icu.util.DateRule
    public Date firstBetween(Date date, Date date2) {
        if (date2 == null) {
            return firstAfter(date);
        }
        int startIndex = startIndex(date);
        Date date3 = null;
        Range rangeAt = rangeAt(startIndex);
        while (date3 == null && rangeAt != null && !rangeAt.start.after(date2)) {
            Range rangeAt2 = rangeAt(startIndex + 1);
            if (rangeAt.rule != null) {
                date3 = rangeAt.rule.firstBetween(date, (rangeAt2 == null || rangeAt2.start.after(date2)) ? date2 : rangeAt2.start);
            }
            rangeAt = rangeAt2;
        }
        return date3;
    }

    @Override // ohos.global.icu.util.DateRule
    public boolean isOn(Date date) {
        Range rangeAt = rangeAt(startIndex(date));
        return (rangeAt == null || rangeAt.rule == null || !rangeAt.rule.isOn(date)) ? false : true;
    }

    @Override // ohos.global.icu.util.DateRule
    public boolean isBetween(Date date, Date date2) {
        return firstBetween(date, date2) == null;
    }

    private int startIndex(Date date) {
        int size = this.ranges.size();
        int i = 0;
        while (true) {
            size = i;
            if (size >= this.ranges.size() || date.before(this.ranges.get(size).start)) {
                break;
            }
            i = size + 1;
        }
        return size;
    }

    private Range rangeAt(int i) {
        if (i < this.ranges.size()) {
            return this.ranges.get(i);
        }
        return null;
    }
}
