package com.huawei.wallet.sdk.business.bankcard.response;

import com.huawei.wallet.sdk.common.apdu.response.ServerAccessBaseResponse;

public class NullifyCardResponse extends ServerAccessBaseResponse {
    public static final int ERR_QUERY_CPLC_ERRO = -10;
    private int apduCount;
    private String appletaid;
    private String commandId;
    private String nextStep;
    private String noNeedCommandResp;

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

    public String getNoNeedCommandResp() {
        return this.noNeedCommandResp;
    }

    public void setNoNeedCommandResp(String noNeedCommandResp2) {
        this.noNeedCommandResp = noNeedCommandResp2;
    }

    public String getNullifyAid() {
        return this.appletaid;
    }

    public void setNullifyAid(String appletaid2) {
        this.appletaid = appletaid2;
    }

    public String getCommandId() {
        return this.commandId;
    }

    public void setCommandId(String commandId2) {
        this.commandId = commandId2;
    }
}
