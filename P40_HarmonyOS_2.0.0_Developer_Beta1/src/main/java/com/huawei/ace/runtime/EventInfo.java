package com.huawei.ace.runtime;

public class EventInfo {
    private int errType;
    private int eventType;

    public EventInfo() {
        this(0);
    }

    public EventInfo(int i) {
        this.eventType = i;
    }

    public void setErrType(int i) {
        this.errType = i;
    }

    public int getEventType() {
        return this.eventType;
    }

    public int getErrType() {
        return this.errType;
    }
}
