package com.android.contacts.hap.numbermark.hwtoms.model.request;

public class TomsRequestTelForHW extends TomsRequestBase {
    private String subjectNum;
    private String timestamp;

    public String getSubjectNum() {
        return this.subjectNum;
    }

    public void setSubjectNum(String subjectNum) {
        this.subjectNum = subjectNum;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
