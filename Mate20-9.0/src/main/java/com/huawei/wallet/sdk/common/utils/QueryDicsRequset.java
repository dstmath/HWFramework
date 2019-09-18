package com.huawei.wallet.sdk.common.utils;

public class QueryDicsRequset extends BaseLibCardServerBaseRequest {
    private String dicName;
    private String itemName;

    public String getDicName() {
        return this.dicName;
    }

    public void setDicName(String dicName2) {
        this.dicName = dicName2;
    }

    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String itemName2) {
        this.itemName = itemName2;
    }
}
