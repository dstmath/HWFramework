package com.android.contacts.hap.numbermark.hwtoms.model.request;

public class TomsRequestDetailForHW extends TomsRequestBase {
    private String id;
    private String type;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
