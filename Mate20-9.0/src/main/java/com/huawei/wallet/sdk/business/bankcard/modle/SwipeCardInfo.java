package com.huawei.wallet.sdk.business.bankcard.modle;

public class SwipeCardInfo {
    private String aid;
    private String issuerId;
    private String mStatus;
    private String mTokenID;

    public String getTokenID() {
        return this.mTokenID;
    }

    public void setTokenID(String tokenID) {
        this.mTokenID = tokenID;
    }

    public String getStatus() {
        return this.mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid2) {
        this.aid = aid2;
    }
}
