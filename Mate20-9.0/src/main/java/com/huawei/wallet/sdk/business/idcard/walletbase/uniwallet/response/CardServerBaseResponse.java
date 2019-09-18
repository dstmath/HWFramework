package com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.response;

public class CardServerBaseResponse {
    public static final int RESPONSE_CODE_OTHER_ERRORS = -99;
    public static final int RESPONSE_CODE_SUCCESS = 0;
    public int returnCode = -99;

    public int getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(int returnCode2) {
        this.returnCode = returnCode2;
    }
}
