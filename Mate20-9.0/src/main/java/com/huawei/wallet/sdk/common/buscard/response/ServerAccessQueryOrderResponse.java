package com.huawei.wallet.sdk.common.buscard.response;

import com.huawei.wallet.sdk.common.apdu.model.ServerAccessQueryOrder;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessBaseResponse;
import java.util.List;

public class ServerAccessQueryOrderResponse extends ServerAccessBaseResponse {
    private int balance;
    private List<ServerAccessQueryOrder> orderList = null;

    public List<ServerAccessQueryOrder> getOrderList() {
        return this.orderList;
    }

    public void setOrderList(List<ServerAccessQueryOrder> orderList2) {
        this.orderList = orderList2;
    }

    public void setBalance(int balance2) {
        this.balance = balance2;
    }

    public int getBalance() {
        return this.balance;
    }
}
