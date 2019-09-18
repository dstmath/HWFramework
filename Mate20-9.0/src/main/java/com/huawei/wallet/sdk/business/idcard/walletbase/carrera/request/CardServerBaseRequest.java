package com.huawei.wallet.sdk.business.idcard.walletbase.carrera.request;

import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.constant.ServiceConfig;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.request.BaseLibCardServerBaseRequest;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.util.Generator;

public class CardServerBaseRequest extends BaseLibCardServerBaseRequest {
    public CardServerBaseRequest() {
        this(ServiceConfig.getWalletMerchantId(), -1, Generator.generateSrcId());
    }

    public CardServerBaseRequest(String merchantId, int index, String transId) {
        super(merchantId, index, transId);
    }
}
