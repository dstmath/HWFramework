package com.huawei.wallet.sdk.common.apdu.tsm.business;

public class BaseBusinessForReqNext extends BaseBusinessForReq {
    private ApduResBean rapduList;
    private int result;

    public int getResult() {
        return this.result;
    }

    public void setResult(int result2) {
        this.result = result2;
    }

    public ApduResBean getRapduList() {
        return this.rapduList;
    }

    public void setRapduList(ApduResBean rapduList2) {
        this.rapduList = rapduList2;
    }

    public static BaseBusinessForReqNext build(int businessType, int taskIndex, int result2, ApduResBean rapduList2) {
        BaseBusinessForReqNext business = new BaseBusinessForReqNext();
        business.setType(businessType);
        business.setRapduList(rapduList2);
        business.setResult(result2);
        business.setTaskIndex(taskIndex);
        return business;
    }
}
