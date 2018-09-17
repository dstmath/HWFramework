package com.huawei.android.pushagent.datatype.http.server;

import com.huawei.android.pushagent.datatype.http.metadata.TokenDelReqMeta;
import java.util.List;

public class TokenDelReq {
    private int chanMode;
    private int deviceIdType;
    private List<TokenDelReqMeta> tokens;

    public TokenDelReq(int i, List<TokenDelReqMeta> list, int i2) {
        this.chanMode = i;
        this.tokens = list;
        this.deviceIdType = i2;
    }

    public int getChanMode() {
        return this.chanMode;
    }

    public void setChanMode(int i) {
        this.chanMode = i;
    }

    public List<TokenDelReqMeta> getTokens() {
        return this.tokens;
    }

    public void setTokens(List<TokenDelReqMeta> list) {
        this.tokens = list;
    }

    public int getDeviceIdType() {
        return this.deviceIdType;
    }

    public void setDeviceIdType(int i) {
        this.deviceIdType = i;
    }
}
