package com.huawei.wallet.sdk.common.utils;

import java.util.ArrayList;

public class QueryDicsResponse extends CardServerBaseResponse {
    public ArrayList<DicItem> dicItems = new ArrayList<>();

    public ArrayList<DicItem> getDicItems() {
        return this.dicItems;
    }

    public void setDicItems(ArrayList<DicItem> dicItems2) {
        this.dicItems = dicItems2;
    }
}
