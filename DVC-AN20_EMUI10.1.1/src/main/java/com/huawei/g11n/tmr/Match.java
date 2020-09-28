package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.utils.DatePeriod;

public class Match implements Comparable<Object> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    int begin;
    DatePeriod dp;
    int end;
    boolean isTimePeriod = false;
    String regex;
    int type;

    public void setIsTimePeriod(boolean is) {
        this.isTimePeriod = is;
    }

    public boolean isTimePeriod() {
        int n;
        boolean z = this.isTimePeriod;
        if (z) {
            return z;
        }
        String str = this.regex;
        if (str == null || str.trim().isEmpty() || (n = Integer.parseInt(this.regex)) <= 49999 || n >= 60000) {
            return false;
        }
        return true;
    }

    public Match(int begin2, int end2, String regex2) {
        this.begin = begin2;
        this.end = end2;
        this.regex = regex2;
    }

    public String getRegex() {
        return this.regex;
    }

    public void setRegex(String regex2) {
        this.regex = regex2;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public DatePeriod getDp() {
        return this.dp;
    }

    public void setDp(DatePeriod dp2) {
        this.dp = dp2;
    }

    public int getBegin() {
        return this.begin;
    }

    public void setBegin(int begin2) {
        this.begin = begin2;
    }

    public int getEnd() {
        return this.end;
    }

    public void setEnd(int end2) {
        this.end = end2;
    }

    public String toString() {
        return "[" + this.regex + "][" + this.begin + "-" + this.end + "]";
    }

    @Override // java.lang.Comparable
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
        return 42;
    }
}
