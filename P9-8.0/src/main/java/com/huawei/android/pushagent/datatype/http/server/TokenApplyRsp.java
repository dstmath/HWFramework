package com.huawei.android.pushagent.datatype.http.server;

import com.huawei.android.pushagent.datatype.http.metadata.TokenApplyRspMeta;
import java.util.List;

public class TokenApplyRsp {
    private List<TokenApplyRspMeta> rets;

    public List<TokenApplyRspMeta> getRets() {
        return this.rets;
    }

    public void setRets(List<TokenApplyRspMeta> list) {
        this.rets = list;
    }
}
