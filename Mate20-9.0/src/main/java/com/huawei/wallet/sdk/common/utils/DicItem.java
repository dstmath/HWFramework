package com.huawei.wallet.sdk.common.utils;

public class DicItem {
    private String bankCard;
    private String name;
    private String parent;
    private String value;

    public String toString() {
        return "DicItem{parent='" + this.parent + '\'' + ", name='" + this.name + '\'' + ", value='" + this.value + '\'' + ", bankCard='" + this.bankCard + '\'' + '}';
    }

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

    public String getBankCard() {
        return this.bankCard;
    }

    public void setBankCard(String bankCard2) {
        this.bankCard = bankCard2;
    }
}
