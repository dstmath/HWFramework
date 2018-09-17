package com.huawei.g11n.tmr;

import android.util.HwSecureWaterMark;
import com.huawei.g11n.tmr.datetime.utils.DatePeriod;

public class Match implements Comparable<Object> {
    static final /* synthetic */ boolean $assertionsDisabled;
    int begin;
    DatePeriod dp;
    int end;
    boolean isTimePeriod = false;
    String regex;
    int type;

    static {
        boolean z = false;
        if (!Match.class.desiredAssertionStatus()) {
            z = true;
        }
        $assertionsDisabled = z;
    }

    public void setIsTimePeriod(boolean is) {
        this.isTimePeriod = is;
    }

    public boolean isTimePeriod() {
        if (this.isTimePeriod) {
            return this.isTimePeriod;
        }
        if (this.regex == null || this.regex.trim().isEmpty()) {
            return false;
        }
        int n = Integer.parseInt(this.regex);
        if (n > 49999 && n < HwSecureWaterMark.MAX_NUMER) {
            return true;
        }
        return false;
    }

    public Match(int begin, int end, String regex) {
        this.begin = begin;
        this.end = end;
        this.regex = regex;
    }

    public String getRegex() {
        return this.regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public DatePeriod getDp() {
        return this.dp;
    }

    public void setDp(DatePeriod dp) {
        this.dp = dp;
    }

    public int getBegin() {
        return this.begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return this.end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String toString() {
        return "[" + this.regex + "][" + this.begin + "-" + this.end + "]";
    }

    public int compareTo(Object o) {
        int result = 0;
        if (!(o instanceof Match)) {
            return 0;
        }
        Match om = (Match) o;
        if (this.begin < om.begin) {
            result = -1;
        }
        if (this.begin > om.begin) {
            return 1;
        }
        return result;
    }

    public boolean equals(Object arg0) {
        return super.equals(arg0);
    }

    public int hashCode() {
        if ($assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }
}
