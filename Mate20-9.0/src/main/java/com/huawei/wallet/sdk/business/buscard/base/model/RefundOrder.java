package com.huawei.wallet.sdk.business.buscard.base.model;

import com.huawei.wallet.sdk.business.buscard.base.spi.model.QueryOrder;

public class RefundOrder {
    private String cardName;
    private boolean isLocal;
    private QueryOrder refundOrder;

    public RefundOrder() {
    }

    public RefundOrder(QueryOrder refundOrder2, String cardName2, boolean isLocal2) {
        this.refundOrder = refundOrder2;
        this.cardName = cardName2;
        this.isLocal = isLocal2;
    }

    public QueryOrder getRefundOrder() {
        return this.refundOrder;
    }

    public void setRefundOrder(QueryOrder refundOrder2) {
        this.refundOrder = refundOrder2;
    }

    public String getCardName() {
        return this.cardName;
    }

    public void setCardName(String cardName2) {
        this.cardName = cardName2;
    }

    public boolean isLocal() {
        return this.isLocal;
    }

    public void setLocal(boolean local) {
        this.isLocal = local;
    }
}
