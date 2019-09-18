package com.huawei.wallet.sdk.common.apdu.response;

public class ServerAccessPersonalizeAppletResponse extends ServerAccessBaseResponse {
    private String cardId;
    private String nextStep;

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String cardId2) {
        this.cardId = cardId2;
    }

    public void setNextStep(String nextStep2) {
        this.nextStep = nextStep2;
    }

    public String getNextStep() {
        return this.nextStep;
    }
}
