package com.huawei.android.pushagent.model.e;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.http.server.TokenApplyReq;
import com.huawei.android.pushagent.datatype.http.server.TokenApplyRsp;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.d.c;
import org.json.JSONException;

public class b extends d<TokenApplyRsp> {
    private TokenApplyReq eh;

    public b(Context context, TokenApplyReq tokenApplyReq) {
        super(context);
        this.eh = tokenApplyReq;
    }

    protected String nb() {
        String nf = d.nf("pushtrs.push.hicloud.com", g.aq(ne()).getBelongId());
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("https://").append(nf).append("/PushTokenServer/v4/pushtoken/apply");
        c.sg("PushLog2951", "url:" + stringBuffer.toString());
        return stringBuffer.toString();
    }

    protected int mz() {
        return 5222;
    }

    protected String na() {
        try {
            return com.huawei.android.pushagent.utils.b.b.oz(this.eh);
        } catch (JSONException e) {
            c.sf("PushLog2951", "fail to get reqContent");
            return null;
        }
    }

    protected TokenApplyRsp nc(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return (TokenApplyRsp) com.huawei.android.pushagent.utils.b.b.oy(str, TokenApplyRsp.class, new Class[0]);
    }
}
