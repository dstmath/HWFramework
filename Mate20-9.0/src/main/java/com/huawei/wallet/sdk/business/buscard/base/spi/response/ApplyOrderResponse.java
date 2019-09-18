package com.huawei.wallet.sdk.business.buscard.base.spi.response;

import com.huawei.wallet.sdk.business.buscard.base.spi.model.ApplyOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.TransferOrder;
import com.huawei.wallet.sdk.common.http.response.BaseResponse;
import java.util.List;

public class ApplyOrderResponse extends BaseResponse {
    private String appCode;
    private TransferOrder mTransferOrder = null;
    private List<ApplyOrder> orderList = null;
    private String wxOrderListJsonString;

    public TransferOrder getTransferOrder() {
        return this.mTransferOrder;
    }

    public void setTransferOrder(TransferOrder mTransferOrder2) {
        this.mTransferOrder = mTransferOrder2;
    }

    public List<ApplyOrder> getOrderList() {
        return this.orderList;
    }

    public void setOrderList(List<ApplyOrder> orderList2) {
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
