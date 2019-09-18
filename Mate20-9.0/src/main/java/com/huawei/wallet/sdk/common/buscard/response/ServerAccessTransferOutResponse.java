package com.huawei.wallet.sdk.common.buscard.response;

import com.huawei.wallet.sdk.common.apdu.response.ServerAccessBaseResponse;

public class ServerAccessTransferOutResponse extends ServerAccessBaseResponse {
    private String nextStep;

    public void setNextStep(String nextStep2) {
        this.nextStep = nextStep2;
    }

    public String getNextStep() {
        return this.nextStep;
    }
}
