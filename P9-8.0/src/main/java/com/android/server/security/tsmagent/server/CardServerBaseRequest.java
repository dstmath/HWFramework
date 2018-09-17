package com.android.server.security.tsmagent.server;

import com.android.server.security.tsmagent.constant.ServiceConfig;

public class CardServerBaseRequest {
    private String merchantID;
    private String srcTransactionID;

    public String getMerchantID() {
        return this.merchantID;
    }

    public String getSrcTransactionID() {
        return this.srcTransactionID;
    }

    public CardServerBaseRequest() {
        this.merchantID = ServiceConfig.WALLET_MERCHANT_ID;
        this.srcTransactionID = ServiceConfig.WALLET_MERCHANT_ID;
    }

    public CardServerBaseRequest(String merchantId, int index, String transId) {
        this.merchantID = merchantId;
        this.srcTransactionID = transId;
    }
}
