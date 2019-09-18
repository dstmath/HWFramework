package com.android.server.security.tsmagent.server.card.request;

import com.android.server.security.tsmagent.server.CardServerBaseRequest;

public class TsmParamQueryRequest extends CardServerBaseRequest {
    private boolean SignCommandEx = false;
    private String aid;
    private String bankRsaIndex;
    private String bankSignResult;
    private String bankSignTime;
    private String cplc;
    private boolean deleteRelatedObjects = false;
    private String signType;
    private String terminal;
    private String tsmParamIMEI;

    public TsmParamQueryRequest(String cplc2, String merchantID, int rsaKeyIndex, String srcTransactionID, String terminal2, String imei) {
        super(merchantID, rsaKeyIndex, srcTransactionID);
        this.cplc = cplc2;
        this.terminal = terminal2;
        this.tsmParamIMEI = imei;
    }

    public boolean isSignCommandEx() {
        return this.SignCommandEx;
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

    public String getTsmParamIMEI() {
        return this.tsmParamIMEI;
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

    public void setSignCommandEx(boolean signCommandEx) {
        this.SignCommandEx = signCommandEx;
    }

    public void setTsmParamIMEI(String iMEI) {
        this.tsmParamIMEI = iMEI;
    }
}
