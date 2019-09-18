package com.huawei.wallet.sdk.business.buscard.base.spi.response;

import com.huawei.wallet.sdk.business.buscard.base.spi.model.QueryOrder;
import com.huawei.wallet.sdk.common.http.response.BaseResponse;
import java.util.List;

public class QueryOrderResponse extends BaseResponse {
    private int balance;
    private List<QueryOrder> orderList = null;

    public List<QueryOrder> getOrderList() {
        return this.orderList;
    }

    public void setOrderList(List<QueryOrder> orderList2) {
        this.orderList = orderList2;
    }

    public void setBalance(int balance2) {
        this.balance = balance2;
    }

    public int getBalance() {
        return this.balance;
    }
}
