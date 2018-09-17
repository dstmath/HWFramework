package com.leisen.wallet.sdk.business;

import java.util.List;

public class BaseBusinessForResp extends Business {
    private List<ApduBean> capduList;
    private int finishFlag;
    private String operationDes;
    private int operationResult;

    public int getOperationResult() {
        return this.operationResult;
    }

    public void setOperationResult(int operationResult) {
        this.operationResult = operationResult;
    }

    public String getOperationDes() {
        return this.operationDes;
    }

    public void setOperationDes(String operationDes) {
        this.operationDes = operationDes;
    }

    public int getFinishFlag() {
        return this.finishFlag;
    }

    public void setFinishFlag(int finishFlag) {
        this.finishFlag = finishFlag;
    }

    public List<ApduBean> getCapduList() {
        return this.capduList;
    }

    public void setCapduList(List<ApduBean> capduList) {
        this.capduList = capduList;
    }
}
