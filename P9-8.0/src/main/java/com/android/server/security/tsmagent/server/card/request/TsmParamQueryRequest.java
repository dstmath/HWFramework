package com.android.server.security.tsmagent.server.card.request;

import com.android.server.security.tsmagent.server.CardServerBaseRequest;

public class TsmParamQueryRequest extends CardServerBaseRequest {
    private String aid;
    private String bankRsaIndex;
    private String bankSignResult;
    private String bankSignTime;
    private String cplc;
    private boolean deleteRelatedObjects = false;
    private String signType;
    private String terminal;
    private String tsmParamIMEI;

    public TsmParamQueryRequest(String cplc, String merchantID, int rsaKeyIndex, String srcTransactionID, String terminal, String imei) {
        super(merchantID, rsaKeyIndex, srcTransactionID);
        this.cplc = cplc;
        this.terminal = terminal;
        this.tsmParamIMEI = imei;
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

    public void setCplc(String cplc) {
        this.cplc = cplc;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public void setBankSignResult(String bankSignResult) {
        this.bankSignResult = bankSignResult;
    }

    public void setBankSignTime(String bankSignTime) {
        this.bankSignTime = bankSignTime;
    }

    public void setBankRsaIndex(String bankRsaIndex) {
        this.bankRsaIndex = bankRsaIndex;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public void setDeleteRelatedObjects(boolean deleteRelatedObjects) {
        this.deleteRelatedObjects = deleteRelatedObjects;
    }

    public void setTsmParamIMEI(String iMEI) {
        this.tsmParamIMEI = iMEI;
    }
}
