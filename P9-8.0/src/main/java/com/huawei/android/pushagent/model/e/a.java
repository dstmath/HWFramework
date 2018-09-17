package com.huawei.android.pushagent.model.e;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.http.server.TokenDelReq;
import com.huawei.android.pushagent.datatype.http.server.TokenDelRsp;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.b.b;
import com.huawei.android.pushagent.utils.d.c;
import org.json.JSONException;

public class a extends d<TokenDelRsp> {
    private TokenDelReq eg;

    public a(Context context, TokenDelReq tokenDelReq) {
        super(context);
        this.eg = tokenDelReq;
    }

    protected String nb() {
        String nf = d.nf("pushtrs.push.hicloud.com", g.aq(ne()).getBelongId());
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("https://").append(nf).append("/PushTokenServer/v4/pushtoken/cancel");
        c.sg("PushLog2951", "url:" + stringBuffer.toString());
        return stringBuffer.toString();
    }

    protected int mz() {
        return 5222;
    }

    protected String na() {
        try {
            return b.oz(this.eg);
        } catch (JSONException e) {
            c.sf("PushLog2951", "fail to get reqContent");
            return null;
        }
    }

    protected TokenDelRsp nc(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return (TokenDelRsp) b.oy(str, TokenDelRsp.class, new Class[0]);
    }
}
