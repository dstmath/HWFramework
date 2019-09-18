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

    public void setOperationResult(int operationResult2) {
        this.operationResult = operationResult2;
    }

    public String getOperationDes() {
        return this.operationDes;
    }

    public void setOperationDes(String operationDes2) {
        this.operationDes = operationDes2;
    }

    public int getFinishFlag() {
        return this.finishFlag;
    }

    public void setFinishFlag(int finishFlag2) {
        this.finishFlag = finishFlag2;
    }

    public List<ApduBean> getCapduList() {
        return this.capduList;
    }

    public void setCapduList(List<ApduBean> capduList2) {
        this.capduList = capduList2;
    }
}
