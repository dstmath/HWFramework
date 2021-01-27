package com.huawei.i18n.tmr.datetime;

import com.huawei.i18n.tmr.datetime.utils.DatePeriod;

public class Match implements Comparable<Match> {
    int begin;
    private DatePeriod dp;
    int end;
    private boolean isTimePeriod = false;
    String regex;
    private int type;

    Match(int begin2, int end2, String regex2) {
        this.begin = begin2;
        this.end = end2;
        this.regex = regex2;
    }

    public void setIsTimePeriod(boolean is) {
        this.isTimePeriod = is;
    }

    public boolean isTimePeriod() {
        int key;
        boolean z = this.isTimePeriod;
        if (z) {
            return z;
        }
        String str = this.regex;
        if (str == null || str.trim().isEmpty() || (key = Integer.parseInt(this.regex)) <= 49999 || key >= 60000) {
            return false;
        }
        return true;
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

    @Override // java.lang.Object
    public String toString() {
        return "[" + this.regex + "][" + this.begin + "-" + this.end + "]";
    }

    public int compareTo(Match match) {
        int result = 0;
        if (this.begin < match.begin) {
            result = -1;
        }
        if (this.begin > match.begin) {
            return 1;
        }
        return result;
    }
}
