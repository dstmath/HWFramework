package com.huawei.wallet.sdk.common.event;

import java.util.List;

public class EventBase {
    private int eventID;
    private List<?> list;
    private String[] result;

    public EventBase() {
    }

    public EventBase(int eventID2, String[] result2) {
        this.eventID = eventID2;
        if (result2 == null) {
            this.result = null;
        } else {
            this.result = (String[]) result2.clone();
        }
    }

    public EventBase(int eventID2, List<?> result2) {
        this.eventID = eventID2;
        this.list = result2;
    }

    public EventBase(int eventID2, String[] result1, List<?> result2) {
        this.eventID = eventID2;
        if (result1 == null) {
            this.result = null;
        } else {
            this.result = (String[]) result1.clone();
        }
        this.list = result2;
    }

    public int getEventID() {
        return this.eventID;
    }

    public void setEventID(int eventID2) {
        this.eventID = eventID2;
    }

    public String[] getResult() {
        if (this.result == null) {
            return new String[]{""};
        }
        return (String[]) this.result.clone();
    }

    public void setResult(String[] result2) {
        if (result2 == null) {
            this.result = null;
        } else {
            this.result = (String[]) result2.clone();
        }
    }

    public List<?> getList() {
        return this.list;
    }

    public void setList(List<?> list2) {
        this.list = list2;
    }
}
