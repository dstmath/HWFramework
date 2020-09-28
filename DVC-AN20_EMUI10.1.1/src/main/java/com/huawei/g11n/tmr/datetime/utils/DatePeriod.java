package com.huawei.g11n.tmr.datetime.utils;

import com.huawei.uikit.effect.BuildConfig;

public class DatePeriod {
    private DateTime begin;
    private DateTime end;

    public DatePeriod(DateTime b) {
        this.begin = b;
    }

    public DatePeriod(DateTime b, DateTime e) {
        this.begin = b;
        this.end = e;
    }

    public boolean isEmpty() {
        DateTime dateTime = this.begin;
        if (dateTime == null || dateTime.isEmpty()) {
            return true;
        }
        return false;
    }

    public DateTime getBegin() {
        return this.begin;
    }

    public void setBegin(DateTime begin2) {
        this.begin = begin2;
    }

    public DateTime getEnd() {
        return this.end;
    }

    public void setEnd(DateTime end2) {
        this.end = end2;
    }

    public int getType() {
        int result;
        if (isEmpty()) {
            return -1;
        }
        if (this.begin.getDate() != null && this.begin.getTime() == null) {
            result = 1;
        } else if (this.begin.getDate() != null || this.begin.getTime() == null) {
            result = 0;
        } else {
            result = 2;
        }
        DateTime dateTime = this.end;
        if (dateTime == null || dateTime.isEmpty()) {
            return result;
        }
        int bSt = this.begin.getSatuts();
        int endSt = this.end.getSatuts();
        if (bSt == 0 && endSt == 0) {
            return 3;
        }
        if (bSt == 1 && endSt == 1) {
            return 4;
        }
        if (bSt == 2 && endSt == 2) {
            return 5;
        }
        return 6;
    }

    public String toString() {
        if (isEmpty()) {
            return BuildConfig.FLAVOR;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(this.begin.toString());
        sb.append(" - ");
        DateTime dateTime = this.end;
        if (dateTime != null && !dateTime.isEmpty()) {
            sb.append(this.end.toString());
        }
        return sb.toString();
    }
}
