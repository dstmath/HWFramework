package com.huawei.opcollect.strategy;

public abstract class AbsActionParam {
    private boolean checkMaxRecordOneDay;
    private boolean checkMinInterval;

    public boolean isCheckMinInterval() {
        return this.checkMinInterval;
    }

    public boolean isCheckMaxRecordOneDay() {
        return this.checkMaxRecordOneDay;
    }

    public AbsActionParam(boolean checkMinInterval2, boolean checkMaxRecordOneDay2) {
        this.checkMinInterval = checkMinInterval2;
        this.checkMaxRecordOneDay = checkMaxRecordOneDay2;
    }

    public AbsActionParam(boolean checkMinInterval2) {
        this.checkMinInterval = checkMinInterval2;
        this.checkMaxRecordOneDay = true;
    }

    protected AbsActionParam() {
        this.checkMinInterval = true;
        this.checkMaxRecordOneDay = true;
    }

    public String toString() {
        return "AbsActionParam{checkMinInterval=" + this.checkMinInterval + ", checkMaxRecordOneDay=" + this.checkMaxRecordOneDay + '}';
    }
}
