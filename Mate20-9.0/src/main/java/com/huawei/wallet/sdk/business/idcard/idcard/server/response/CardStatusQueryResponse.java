package com.huawei.wallet.sdk.business.idcard.idcard.server.response;

import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;
import java.util.List;

public class CardStatusQueryResponse extends CardServerBaseResponse {
    private long count;
    private List<IdCardStatusItem> data;

    public long getCount() {
        return this.count;
    }

    public void setCount(long count2) {
        this.count = count2;
    }

    public List<IdCardStatusItem> getData() {
        return this.data;
    }

    public void setData(List<IdCardStatusItem> data2) {
        this.data = data2;
    }
}
