package com.huawei.wallet.sdk.common.apdu.tsm.requester.response;

public class TSMOperateParam {
    private String cplc;
    private String funCallId;
    private String serverId;

    public static TSMOperateParam build(String cplc2, String funcallID, String serverID) {
        TSMOperateParam request = new TSMOperateParam();
        request.cplc = cplc2;
        request.funCallId = funcallID;
        request.serverId = serverID;
        return request;
    }

    public String getCplc() {
        return this.cplc;
    }

    public String getFunCallId() {
        return this.funCallId;
    }

    public String getServerId() {
        return this.serverId;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public void setFunCallId(String funCallId2) {
        this.funCallId = funCallId2;
    }

    public void setServerId(String serverId2) {
        this.serverId = serverId2;
    }
}
