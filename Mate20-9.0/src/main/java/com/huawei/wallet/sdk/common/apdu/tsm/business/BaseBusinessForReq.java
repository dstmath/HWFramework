package com.huawei.wallet.sdk.common.apdu.tsm.business;

public class BaseBusinessForReq extends Business {
    private int taskIndex;

    public int getTaskIndex() {
        return this.taskIndex;
    }

    public void setTaskIndex(int taskIndex2) {
        this.taskIndex = taskIndex2;
    }

    public static BaseBusinessForReq build(int businessType, int taskIndex2) {
        BaseBusinessForReq business = new BaseBusinessForReq();
        business.setType(businessType);
        business.setTaskIndex(taskIndex2);
        return business;
    }
}
