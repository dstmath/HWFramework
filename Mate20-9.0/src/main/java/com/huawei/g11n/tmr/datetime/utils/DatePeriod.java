package com.huawei.g11n.tmr.datetime.utils;

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
        if (this.begin == null || this.begin.isEmpty()) {
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
        if (this.end != null && !this.end.isEmpty()) {
            int bSt = this.begin.getSatuts();
            int endSt = this.end.getSatuts();
            if (bSt == 0 && endSt == 0) {
                result = 3;
            } else if (bSt == 1 && endSt == 1) {
                result = 4;
            } else if (bSt == 2 && endSt == 2) {
                result = 5;
            } else {
                result = 6;
            }
        }
        return result;
    }

    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(this.begin.toString());
        sb.append(" - ");
        if (this.end != null && !this.end.isEmpty()) {
            sb.append(this.end.toString());
        }
        return sb.toString();
    }
}
