package com.huawei.g11n.tmr.datetime.utils;

public class DatePeriod {
    private DateTime begin;
    private DateTime end;

    public DatePeriod(DateTime dateTime) {
        this.begin = dateTime;
    }

    public DatePeriod(DateTime dateTime, DateTime dateTime2) {
        this.begin = dateTime;
        this.end = dateTime2;
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

    public void setBegin(DateTime dateTime) {
        this.begin = dateTime;
    }

    public DateTime getEnd() {
        return this.end;
    }

    public void setEnd(DateTime dateTime) {
        this.end = dateTime;
    }

    public int getType() {
        int i = 0;
        if (isEmpty()) {
            return -1;
        }
        if (this.begin.getDate() != null && this.begin.getTime() == null) {
            i = 1;
        } else if (this.begin.getDate() == null && this.begin.getTime() != null) {
            i = 2;
        }
        if (!(this.end == null || this.end.isEmpty())) {
            i = this.begin.getSatuts();
            int satuts = this.end.getSatuts();
            i = (i == 0 && satuts == 0) ? 3 : (i == 1 && satuts == 1) ? 4 : (i == 2 && satuts == 2) ? 5 : 6;
        }
        return i;
    }

    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.begin.toString()).append(" - ");
        if (!(this.end == null || this.end.isEmpty())) {
            stringBuffer.append(this.end.toString());
        }
        return stringBuffer.toString();
    }
}
