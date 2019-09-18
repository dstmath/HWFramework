package com.huawei.wallet.sdk.common.apdu.response;

import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import java.util.List;

public class ServerAccessBaseResponse extends CardServerBaseResponse {
    private List<ServerAccessAPDU> apduList = null;
    private ErrorInfo errorInfo = null;
    private String resultDesc = null;
    private String srcTranID = null;
    private String transactionId = null;

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId2) {
        this.transactionId = transactionId2;
    }

    public List<ServerAccessAPDU> getApduList() {
        return this.apduList;
    }

    public void setApduList(List<ServerAccessAPDU> apduList2) {
        this.apduList = apduList2;
    }

    public String getResultDesc() {
        return this.resultDesc;
    }

    public void setResultDesc(String resultDesc2) {
        this.resultDesc = resultDesc2;
    }

    public ErrorInfo getErrorInfo() {
        return this.errorInfo;
    }

    public void setErrorInfo(ErrorInfo errorInfo2) {
        this.errorInfo = errorInfo2;
    }

    public String getSrcTranID() {
        return this.srcTranID;
    }

    public void setSrcTranID(String srcTranID2) {
        this.srcTranID = srcTranID2;
    }
}
