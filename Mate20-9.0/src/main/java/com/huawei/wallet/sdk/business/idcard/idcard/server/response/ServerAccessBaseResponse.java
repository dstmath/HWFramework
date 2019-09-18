package com.huawei.wallet.sdk.business.idcard.idcard.server.response;

import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import java.util.List;

public class ServerAccessBaseResponse extends CardServerBaseResponse {
    private List<ServerAccessAPDU> apduList = null;
    private ErrorInfo errorCodeInfo;
    private String nextStep = null;
    private String returnDesc = null;
    private String transactionId = null;

    public ErrorInfo getErrorCodeInfo() {
        return this.errorCodeInfo;
    }

    public void setErrorCodeInfo(ErrorInfo errorCodeInfo2) {
        this.errorCodeInfo = errorCodeInfo2;
    }

    public String getReturnDesc() {
        return this.returnDesc;
    }

    public void setReturnDesc(String returnDesc2) {
        this.returnDesc = returnDesc2;
    }

    public List<ServerAccessAPDU> getApduList() {
        return this.apduList;
    }

    public void setApduList(List<ServerAccessAPDU> apduList2) {
        this.apduList = apduList2;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId2) {
        this.transactionId = transactionId2;
    }

    public String getNextStep() {
        return this.nextStep;
    }

    public void setNextStep(String nextStep2) {
        this.nextStep = nextStep2;
    }
}
