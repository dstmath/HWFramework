package com.huawei.wallet.sdk.common.apdu.response;

public class ServerAccessDeleteAppletResponse extends ServerAccessBaseResponse {
    private String nextStep;

    public String getNextStep() {
        return this.nextStep;
    }

    public void setNextStep(String nextStep2) {
        this.nextStep = nextStep2;
    }
}
