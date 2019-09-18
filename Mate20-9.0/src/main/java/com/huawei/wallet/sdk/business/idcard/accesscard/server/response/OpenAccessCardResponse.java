package com.huawei.wallet.sdk.business.idcard.accesscard.server.response;

import com.huawei.wallet.sdk.common.apdu.response.ServerAccessBaseResponse;

public class OpenAccessCardResponse extends ServerAccessBaseResponse {
    private int apduCount;
    private String nextStep;
    private int returnCode;

    public int getApduCount() {
        return this.apduCount;
    }

    public void setApduCount(int apduCount2) {
        this.apduCount = apduCount2;
    }

    public String getNextStep() {
        return this.nextStep;
    }

    public void setNextStep(String nextStep2) {
        this.nextStep = nextStep2;
    }

    public int getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(int returnCode2) {
        this.returnCode = returnCode2;
    }
}
