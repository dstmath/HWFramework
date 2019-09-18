package com.huawei.wallet.sdk.business.buscard.base.model;

import com.huawei.wallet.sdk.business.buscard.base.spi.model.ApplyOrder;

public class UnionPayInfo {
    private String unionRequestId = null;
    private String unionTn = null;

    public String getUnionRequestId() {
        return this.unionRequestId;
    }

    public void setUnionRequestId(String unionRequestId2) {
        this.unionRequestId = unionRequestId2;
    }

    public String getUnionTn() {
        return this.unionTn;
    }

    public void setUnionTn(String unionTn2) {
        this.unionTn = unionTn2;
    }

    public static UnionPayInfo buildUnion(ApplyOrder applyOrder) {
        if (applyOrder == null) {
            return null;
        }
        UnionPayInfo payInfo = new UnionPayInfo();
        payInfo.setUnionRequestId(applyOrder.getOrderId());
        payInfo.setUnionTn(applyOrder.getApplyOrderTn());
        return payInfo;
    }
}
