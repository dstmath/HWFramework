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

    public void setBegin(DateTime begin) {
        this.begin = begin;
    }

    public DateTime getEnd() {
        return this.end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public int getType() {
        if (isEmpty()) {
            return -1;
        }
        int result;
        if (this.begin.getDate() != null && this.begin.getTime() == null) {
            result = 1;
        } else if (this.begin.getDate() == null && this.begin.getTime() != null) {
            result = 2;
        } else {
            result = 0;
        }
        if (!(this.end == null || this.end.isEmpty())) {
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
        sb.append(this.begin.toString()).append(" - ");
        if (!(this.end == null || this.end.isEmpty())) {
            sb.append(this.end.toString());
        }
        return sb.toString();
    }
}
