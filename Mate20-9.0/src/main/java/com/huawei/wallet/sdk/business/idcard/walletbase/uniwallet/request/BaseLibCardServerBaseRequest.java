package com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.request;

import com.huawei.wallet.sdk.business.idcard.walletbase.constant.BaseConfigrations;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.util.Generator;

public class BaseLibCardServerBaseRequest {
    private String aesKey;
    private String huaweirsaIndex;
    private boolean isNeedServiceTokenAuth;
    private String merchantID;
    private int rsaKeyIndex;
    private String srcTransactionID;

    public BaseLibCardServerBaseRequest() {
        this.isNeedServiceTokenAuth = true;
        this.merchantID = BaseConfigrations.getWalletMerchantId();
        this.rsaKeyIndex = -1;
        this.srcTransactionID = Generator.generateSrcId();
    }

    public BaseLibCardServerBaseRequest(String merchantId, int index, String transId) {
        this.isNeedServiceTokenAuth = true;
        this.merchantID = merchantId;
        this.rsaKeyIndex = index;
        this.srcTransactionID = transId;
    }

    public String getMerchantID() {
        return this.merchantID;
    }

    public void setMerchantID(String merchantID2) {
        this.merchantID = merchantID2;
    }

    public int getRsaKeyIndex() {
        return this.rsaKeyIndex;
    }

    public void setRsaKeyIndex(int rsaKeyIndex2) {
        this.rsaKeyIndex = rsaKeyIndex2;
    }

    public String getSrcTransactionID() {
        return this.srcTransactionID;
    }

    public void setSrcTransactionID(String srcTransactionID2) {
        this.srcTransactionID = srcTransactionID2;
    }

    public boolean getIsNeedServiceTokenAuth() {
        return this.isNeedServiceTokenAuth;
    }

    public void setIsNeedServiceTokenAuth(boolean isNeedServiceTokenAuth2) {
        this.isNeedServiceTokenAuth = isNeedServiceTokenAuth2;
    }
}
