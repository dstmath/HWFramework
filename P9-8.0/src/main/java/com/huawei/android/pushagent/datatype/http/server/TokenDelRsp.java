package com.huawei.android.pushagent.datatype.http.server;

import com.huawei.android.pushagent.datatype.http.metadata.TokenDelRspMeta;
import java.util.List;

public class TokenDelRsp {
    private List<TokenDelRspMeta> rets;

    public List<TokenDelRspMeta> getRets() {
        return this.rets;
    }

    public void setRets(List<TokenDelRspMeta> list) {
        this.rets = list;
    }
}
