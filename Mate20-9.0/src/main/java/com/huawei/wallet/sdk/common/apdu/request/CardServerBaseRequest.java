package com.huawei.wallet.sdk.common.apdu.request;

import com.huawei.wallet.sdk.common.AppConfig;
import com.huawei.wallet.sdk.common.utils.NfcUtil;

public class CardServerBaseRequest extends BaseLibCardServerBaseRequest {
    public CardServerBaseRequest() {
        this(AppConfig.MERCHANT_ID, -1, NfcUtil.generateSrcId());
    }

    public CardServerBaseRequest(String merchantId, int index, String transId) {
        super(merchantId, index, transId);
    }
}
