package com.huawei.android.pushagent.datatype.http.server;

import com.huawei.android.pushagent.datatype.http.metadata.TokenApplyReqMeta;
import java.util.List;

public class TokenApplyReq {
    private List<TokenApplyReqMeta> apps;
    private int chanMode;
    private String connId;
    private int deviceIdType;

    public TokenApplyReq(String str, int i, List<TokenApplyReqMeta> list, int i2) {
        this.connId = str;
        this.chanMode = i;
        this.apps = list;
        this.deviceIdType = i2;
    }

    public String getConnId() {
        return this.connId;
    }

    public void setConnId(String str) {
        this.connId = str;
    }

    public int getChanMode() {
        return this.chanMode;
    }

    public void setChanMode(int i) {
        this.chanMode = i;
    }

    public List<TokenApplyReqMeta> geTokenReqs() {
        return this.apps;
    }

    public void setTokenReqs(List<TokenApplyReqMeta> list) {
        this.apps = list;
    }

    public int getDeviceIdType() {
        return this.deviceIdType;
    }

    public void setDeviceIdType(int i) {
        this.deviceIdType = i;
    }
}
