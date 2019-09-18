package com.huawei.wallet.sdk.common.apdu.request;

public class TsmParamQueryRequest extends CardServerBaseRequest {
    private String aid;
    private String bankRsaIndex;
    private String bankSignResult;
    private String bankSignTime;
    private String cplc;
    private boolean deleteRelatedObjects = false;
    private String deviceId;
    private String issuerId;
    private String randomStr;
    private String signType;
    private String terminal;

    public TsmParamQueryRequest() {
    }

    public TsmParamQueryRequest(String cplc2, String merchantID, int rsaKeyIndex, String srcTransactionID, String terminal2) {
        super(merchantID, rsaKeyIndex, srcTransactionID);
        this.cplc = cplc2;
        this.terminal = terminal2;
    }

    public TsmParamQueryRequest(String cplc2, String aid2, String terminal2, String bankSignResult2, String bankSignTime2, String bankRsaIndex2, boolean deleteRelatedObjects2, String issuerId2) {
        this.cplc = cplc2;
        this.aid = aid2;
        this.terminal = terminal2;
        this.bankSignResult = bankSignResult2;
        this.bankSignTime = bankSignTime2;
        this.bankRsaIndex = bankRsaIndex2;
        this.deleteRelatedObjects = deleteRelatedObjects2;
        this.issuerId = issuerId2;
    }

    public String getRandomStr() {
        return this.randomStr;
    }

    public void setRandomStr(String randomStr2) {
        this.randomStr = randomStr2;
    }

    public String getCplc() {
        return this.cplc;
    }

    public String getAid() {
        return this.aid;
    }

    public String getTerminal() {
        return this.terminal;
    }

    public String getBankSignResult() {
        return this.bankSignResult;
    }

    public String getBankSignTime() {
        return this.bankSignTime;
    }

    public String getBankRsaIndex() {
        return this.bankRsaIndex;
    }

    public String getSignType() {
        return this.signType;
    }

    public boolean isDeleteRelatedObjects() {
        return this.deleteRelatedObjects;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public void setAid(String aid2) {
        this.aid = aid2;
    }

    public void setTerminal(String terminal2) {
        this.terminal = terminal2;
    }

    public void setBankSignResult(String bankSignResult2) {
        this.bankSignResult = bankSignResult2;
    }

    public void setBankSignTime(String bankSignTime2) {
        this.bankSignTime = bankSignTime2;
    }

    public void setBankRsaIndex(String bankRsaIndex2) {
        this.bankRsaIndex = bankRsaIndex2;
    }

    public void setSignType(String signType2) {
        this.signType = signType2;
    }

    public void setDeleteRelatedObjects(boolean deleteRelatedObjects2) {
        this.deleteRelatedObjects = deleteRelatedObjects2;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }
}
