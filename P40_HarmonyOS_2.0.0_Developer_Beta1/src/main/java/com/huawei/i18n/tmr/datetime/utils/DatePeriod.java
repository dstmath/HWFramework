package com.huawei.i18n.tmr.datetime.utils;

import com.huawei.android.os.storage.StorageManagerExt;

public class DatePeriod {
    private DateTime begin;
    private DateTime end;

    public enum DatePeriodType {
        TYPE_DATETIME(0),
        TYPE_DATE(1),
        TYPE_TIME(2),
        TYPE_DATETIME_DUR(3),
        TYPE_DATE_DUR(4),
        TYPE_TIME_DUR(5),
        TYPE_OTHER_DUR(6),
        TYPE_NULL(-1);
        
        private int type;

        private DatePeriodType(int type2) {
            this.type = type2;
        }

        public int getValue() {
            return this.type;
        }
    }

    public DatePeriod(DateTime begin2) {
        this(begin2, null);
    }

    public DatePeriod(DateTime begin2, DateTime end2) {
        this.begin = begin2;
        this.end = end2;
    }

    public boolean isEmpty() {
        DateTime dateTime = this.begin;
        return dateTime == null || dateTime.isEmpty();
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
        int type;
        int type2 = DatePeriodType.TYPE_NULL.getValue();
        if (isEmpty()) {
            return type2;
        }
        if (this.begin.getDate() != null && this.begin.getTime() == null) {
            type = DatePeriodType.TYPE_DATE.getValue();
        } else if (this.begin.getDate() != null || this.begin.getTime() == null) {
            type = DatePeriodType.TYPE_DATETIME.getValue();
        } else {
            type = DatePeriodType.TYPE_TIME.getValue();
        }
        DateTime dateTime = this.end;
        if (dateTime == null || dateTime.isEmpty()) {
            return type;
        }
        DatePeriodType beginStatus = this.begin.getType();
        DatePeriodType endStatus = this.end.getType();
        if (beginStatus == DatePeriodType.TYPE_DATETIME && endStatus == DatePeriodType.TYPE_DATETIME) {
            return DatePeriodType.TYPE_DATETIME_DUR.getValue();
        }
        if (beginStatus == DatePeriodType.TYPE_DATE && endStatus == DatePeriodType.TYPE_DATE) {
            return DatePeriodType.TYPE_DATE_DUR.getValue();
        }
        if (beginStatus == DatePeriodType.TYPE_TIME && endStatus == DatePeriodType.TYPE_TIME) {
            return DatePeriodType.TYPE_TIME_DUR.getValue();
        }
        return DatePeriodType.TYPE_OTHER_DUR.getValue();
    }

    public String toString() {
        if (isEmpty()) {
            return StorageManagerExt.INVALID_KEY_DESC;
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
