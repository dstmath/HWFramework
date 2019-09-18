package com.huawei.wallet.sdk.business.bankcard.response;

import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;

public class QueryUnionPayPushResponse extends CardServerBaseResponse {
    private String pushMsg;
    private String pushTime;
    private String systemCurrentTime;

    public String getPushMsg() {
        return this.pushMsg;
    }

    public void setPushMsg(String pushMsg2) {
        this.pushMsg = pushMsg2;
    }

    public String getPushTime() {
        return this.pushTime;
    }

    public void setPushTime(String pushTime2) {
        this.pushTime = pushTime2;
    }

    public String getSystemCurrentTime() {
        return this.systemCurrentTime;
    }

    public void setSystemCurrentTime(String systemCurrentTime2) {
        this.systemCurrentTime = systemCurrentTime2;
    }
}
