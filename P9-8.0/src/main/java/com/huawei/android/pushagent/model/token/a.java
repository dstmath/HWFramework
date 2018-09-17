package com.huawei.android.pushagent.model.token;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.http.metadata.TokenDelReqMeta;
import com.huawei.android.pushagent.datatype.http.metadata.TokenDelRspMeta;
import com.huawei.android.pushagent.datatype.http.server.TokenDelRsp;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.a.e;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class a {
    private Context appCtx;

    public a(Context context) {
        this.appCtx = context.getApplicationContext();
    }

    public static void execute(Context context) {
        if (g.aq(context).isValid()) {
            new a(context).kz();
            return;
        }
        c.sh("PushLog2951", "delete token: TRS is invalid, so need to query TRS");
        com.huawei.android.pushagent.model.d.c.jz(context).ka(false);
    }

    public void kz() {
        com.huawei.android.pushagent.utils.threadpool.a.os(new c(this));
    }

    private void lb(TokenDelRsp tokenDelRsp) {
        b.us(this.appCtx, 100);
        Iterable<TokenDelRspMeta> rets = tokenDelRsp.getRets();
        if (rets != null && rets.size() != 0) {
            for (TokenDelRspMeta tokenDelRspMeta : rets) {
                if (tokenDelRspMeta != null) {
                    String token;
                    String d;
                    if (tokenDelRspMeta.isValid()) {
                        token = tokenDelRspMeta.getToken();
                        d = com.huawei.android.pushagent.model.a.a.c(this.appCtx).d(token);
                        com.huawei.android.pushagent.model.a.a.c(this.appCtx).g(token);
                        new com.huawei.android.pushagent.utils.d.a(this.appCtx, "push_notify_key").rr(d);
                    } else if (tokenDelRspMeta.isRemoveAble()) {
                        c.sh("PushLog2951", "PKG name is " + tokenDelRspMeta.getPkgName() + ", ErrCode is" + tokenDelRspMeta.getRet());
                        c.sf("PushLog2951", "delete token error, errorCode is: 1, not reApply in future");
                        token = tokenDelRspMeta.getToken();
                        d = com.huawei.android.pushagent.model.a.a.c(this.appCtx).d(token);
                        com.huawei.android.pushagent.model.a.a.c(this.appCtx).g(token);
                        new com.huawei.android.pushagent.utils.d.a(this.appCtx, "push_notify_key").rr(d);
                    } else {
                        c.sf("PushLog2951", "del token error, errorCode is: " + tokenDelRspMeta.getRet());
                    }
                }
            }
        }
    }

    private List<TokenDelReqMeta> la() {
        Iterable<Entry> a = com.huawei.android.pushagent.model.a.a.c(this.appCtx).a();
        List<TokenDelReqMeta> arrayList = new ArrayList();
        for (Entry entry : a) {
            Object nu = e.nu((String) entry.getKey());
            String str = (String) entry.getValue();
            if (!(TextUtils.isEmpty(nu) || TextUtils.isEmpty(str))) {
                arrayList.add(new TokenDelReqMeta(b.uf(str), nu));
            }
        }
        return arrayList;
    }
}
