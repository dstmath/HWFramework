package com.huawei.wallet.sdk.common.apdu.response;

import com.huawei.wallet.sdk.common.apdu.model.ServerAccessCutoverInfo;

public class ServerAccessApplyAPDUResponse extends ServerAccessBaseResponse {
    public static final int RESPONSE_CODE_ABNORMAL_APDU_RESULT = 6002;
    private String nextStep;
    private ServerAccessCutoverInfo serverAccesscutoverInfo;

    public void setNextStep(String nextStep2) {
        this.nextStep = nextStep2;
    }

    public String getNextStep() {
        return this.nextStep;
    }

    public void setServerAccessCutoverInfo(ServerAccessCutoverInfo cutoverInfo) {
        this.serverAccesscutoverInfo = cutoverInfo;
    }

    public ServerAccessCutoverInfo getServerAccessCutoverInfo() {
        return this.serverAccesscutoverInfo;
    }
}
