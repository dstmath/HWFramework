package com.huawei.wallet.sdk.business.idcard.walletbase.tcis.request;

import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.request.BaseLibCardServerBaseRequest;

public class TcisRequest extends BaseLibCardServerBaseRequest {
    private short TA_VERSION;
    private String additionAuthData;
    private String aid;
    private String deviceModel;
    private String tcisID;

    public String getAdditionAuthData() {
        return this.additionAuthData;
    }

    public void setAdditionAuthData(String additionAuthData2) {
        this.additionAuthData = additionAuthData2;
    }

    public String getDeviceModel() {
        return this.deviceModel;
    }

    public void setDeviceModel(String deviceModel2) {
        this.deviceModel = deviceModel2;
    }

    public String getTcisID() {
        return this.tcisID;
    }

    public void setTcisID(String tcisID2) {
        this.tcisID = tcisID2;
    }

    public short getTA_VERSION() {
        return this.TA_VERSION;
    }

    public void setTA_VERSION(short TA_VERSION2) {
        this.TA_VERSION = TA_VERSION2;
    }

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid2) {
        this.aid = aid2;
    }
}
