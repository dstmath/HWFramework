package com.huawei.wallet.sdk.common.apdu.response;

import com.huawei.wallet.sdk.common.apdu.model.CardProductInfoServerItem;
import java.util.ArrayList;
import java.util.List;

public class QueryCardProductInfoResponse extends CardServerBaseResponse {
    public List<CardProductInfoServerItem> items = new ArrayList();

    public List<CardProductInfoServerItem> getItems() {
        return this.items;
    }

    public void setItems(List<CardProductInfoServerItem> items2) {
        this.items = items2;
    }
}
