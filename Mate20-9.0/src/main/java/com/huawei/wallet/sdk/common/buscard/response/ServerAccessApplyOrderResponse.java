package com.huawei.wallet.sdk.common.buscard.response;

import com.huawei.wallet.sdk.business.buscard.base.spi.model.TransferOrder;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessApplyOrder;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessBaseResponse;
import java.util.List;

public class ServerAccessApplyOrderResponse extends ServerAccessBaseResponse {
    private String appCode;
    private TransferOrder mTransferOrder = null;
    private List<ServerAccessApplyOrder> orderList = null;
    private String wxOrderListJsonString;

    public TransferOrder getTransferOrder() {
        return this.mTransferOrder;
    }

    public void setTransferOrder(TransferOrder mTransferOrder2) {
        this.mTransferOrder = mTransferOrder2;
    }

    public List<ServerAccessApplyOrder> getOrderList() {
        return this.orderList;
    }

    public void setOrderList(List<ServerAccessApplyOrder> orderList2) {
        this.orderList = orderList2;
    }

    public void setAppCode(String appCode2) {
        this.appCode = appCode2;
    }

    public String getAppCode() {
        return this.appCode;
    }

    public void setWxOrderListJsonString(String wxOrderListJsonString2) {
        this.wxOrderListJsonString = wxOrderListJsonString2;
    }

    public String getWxOrderListJsonString() {
        return this.wxOrderListJsonString;
    }
}
