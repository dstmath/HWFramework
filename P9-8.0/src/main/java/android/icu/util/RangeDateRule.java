package android.icu.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RangeDateRule implements DateRule {
    List<Range> ranges = new ArrayList(2);

    public void add(DateRule rule) {
        add(new Date(Long.MIN_VALUE), rule);
    }

    public void add(Date start, DateRule rule) {
        this.ranges.add(new Range(start, rule));
    }

    public Date firstAfter(Date start) {
        int index = startIndex(start);
        if (index == this.ranges.size()) {
            index = 0;
        }
        Range r = rangeAt(index);
        Range e = rangeAt(index + 1);
        if (r == null || r.rule == null) {
            return null;
        }
        if (e != null) {
            return r.rule.firstBetween(start, e.start);
        }
        return r.rule.firstAfter(start);
    }

    public Date firstBetween(Date start, Date end) {
        if (end == null) {
            return firstAfter(start);
        }
        int index = startIndex(start);
        Date result = null;
        Range next = rangeAt(index);
        while (result == null && next != null && (next.start.after(end) ^ 1) != 0) {
            Range r = next;
            next = rangeAt(index + 1);
            if (r.rule != null) {
                Date e;
                if (next == null || (next.start.after(end) ^ 1) == 0) {
                    e = end;
                } else {
                    e = next.start;
                }
                result = r.rule.firstBetween(start, e);
            }
        }
        return result;
    }

    public boolean isOn(Date date) {
        Range r = rangeAt(startIndex(date));
        return (r == null || r.rule == null) ? false : r.rule.isOn(date);
    }

    public boolean isBetween(Date start, Date end) {
        return firstBetween(start, end) == null;
    }

    private int startIndex(Date start) {
        int lastIndex = this.ranges.size();
        int i = 0;
        while (i < this.ranges.size() && !start.before(((Range) this.ranges.get(i)).start)) {
            lastIndex = i;
            i++;
        }
        return lastIndex;
    }

    private Range rangeAt(int index) {
        if (index < this.ranges.size()) {
            return (Range) this.ranges.get(index);
        }
        return null;
    }
}
