package com.huawei.android.pushagent.model.e;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.http.server.TrsReq;
import com.huawei.android.pushagent.datatype.http.server.TrsRsp;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.b.b;
import org.json.JSONException;

public class c extends d<TrsRsp> {
    private TrsReq ei;

    public c(Context context, TrsReq trsReq) {
        super(context);
        this.ei = trsReq;
    }

    protected String nb() {
        String nf = d.nf("pushtrs.push.hicloud.com", g.aq(ne()).getBelongId());
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("https://").append(nf).append("/TRSServer/v4/TRSRequest");
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "url:" + stringBuffer.toString());
        return stringBuffer.toString();
    }

    protected int mz() {
        return 5222;
    }

    protected String na() {
        try {
            return b.oz(this.ei);
        } catch (JSONException e) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "fail to get reqContent");
            return null;
        }
    }

    protected TrsRsp nc(String str) {
        if (TextUtils.isEmpty(str)) {
            return new TrsRsp(null);
        }
        return new TrsRsp(str);
    }
}
