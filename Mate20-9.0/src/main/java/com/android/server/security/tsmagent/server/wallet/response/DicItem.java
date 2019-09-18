package com.android.server.security.tsmagent.server.wallet.response;

public class DicItem {
    private String name;
    private String parent;
    private String value;

    public String getParent() {
        return this.parent;
    }

    public void setParent(String parent2) {
        this.parent = parent2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
    }
}
