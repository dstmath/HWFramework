package com.huawei.android.pushagent.model.token;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.http.metadata.TokenApplyReqMeta;
import com.huawei.android.pushagent.datatype.http.metadata.TokenApplyRspMeta;
import com.huawei.android.pushagent.datatype.http.server.TokenApplyRsp;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.a.j;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;
import com.huawei.android.pushagent.utils.f;
import com.huawei.android.pushagent.utils.threadpool.a;
import com.huawei.android.pushagent.utils.tools.d;
import java.util.ArrayList;
import java.util.List;

public class TokenApply {
    private static final String TAG = "PushLog2951";
    private Context appCtx;

    public TokenApply(Context context) {
        this.appCtx = context.getApplicationContext();
    }

    public static void execute(Context context) {
        if (g.aq(context).isValid()) {
            new TokenApply(context).apply();
            return;
        }
        c.sh(TAG, "apply token: TRS is invalid, so need to query TRS");
        com.huawei.android.pushagent.model.d.c.jz(context).ka(false);
    }

    public void apply() {
        a.os(new b(this));
    }

    public void responseToken(TokenApplyRsp tokenApplyRsp) {
        b.us(this.appCtx, 100);
        Iterable<TokenApplyRspMeta> rets = tokenApplyRsp.getRets();
        if (rets != null && rets.size() != 0) {
            for (TokenApplyRspMeta tokenApplyRspMeta : rets) {
                if (tokenApplyRspMeta != null) {
                    if (tokenApplyRspMeta.isValid()) {
                        String pkgName = tokenApplyRspMeta.getPkgName();
                        String token = tokenApplyRspMeta.getToken();
                        String userId = tokenApplyRspMeta.getUserId();
                        com.huawei.android.pushagent.a.a.xx(65, pkgName);
                        delToRegApp(pkgName, userId);
                        c.sg(TAG, "pushSrv response register token to " + pkgName);
                        if (d.qo()) {
                            d.qs(pkgName);
                        }
                        com.huawei.android.pushagent.model.a.b.l(this.appCtx).t(b.ue(pkgName, userId), token);
                        b.up(this.appCtx, pkgName, userId, token);
                    } else if (tokenApplyRspMeta.isRemoveAble()) {
                        com.huawei.android.pushagent.a.a.xx(66, tokenApplyRspMeta.getPkgName());
                        c.sh(TAG, "PKG name is " + tokenApplyRspMeta.getPkgName() + ", ErrCode is" + tokenApplyRspMeta.getRet());
                        c.sf(TAG, "apply token error, remove info is complete");
                        delToRegApp(tokenApplyRspMeta.getPkgName(), tokenApplyRspMeta.getUserId());
                    } else {
                        com.huawei.android.pushagent.a.a.xx(66, tokenApplyRspMeta.getPkgName());
                        c.sf(TAG, "apply token error, errorCode is: " + tokenApplyRspMeta.getRet());
                    }
                }
            }
        }
    }

    private void delToRegApp(String str, String str2) {
        if (!TextUtils.isEmpty(str)) {
            String ue = b.ue(str, str2);
            if (TextUtils.isEmpty(j.ev(this.appCtx).ew(ue))) {
                c.sh(TAG, "not found record in pclient_request_info after token response, so remove all similar packagenames.");
                for (String ue2 : j.ev(this.appCtx).ex()) {
                    if (ue2.startsWith(str)) {
                        j.ev(this.appCtx).remove(ue2);
                    }
                }
            } else {
                j.ev(this.appCtx).remove(ue2);
            }
        }
    }

    private List<TokenApplyReqMeta> getTokenReqs() {
        List<TokenApplyReqMeta> arrayList = new ArrayList();
        for (String str : j.ev(this.appCtx).ex()) {
            String uf = b.uf(str);
            int uh = b.uh(str);
            TokenApplyReqMeta tokenApplyReqMeta = new TokenApplyReqMeta();
            tokenApplyReqMeta.setPkgName(uf);
            tokenApplyReqMeta.setUserId(f.vd(uh));
            arrayList.add(tokenApplyReqMeta);
            com.huawei.android.pushagent.a.a.xx(62, uf);
        }
        TokenApplyReqMeta ncTokenReq = getNcTokenReq();
        if (ncTokenReq != null) {
            arrayList.add(ncTokenReq);
        }
        return arrayList;
    }

    private TokenApplyReqMeta getNcTokenReq() {
        String vd = f.vd(f.vb());
        if (!TextUtils.isEmpty(com.huawei.android.pushagent.model.a.b.l(this.appCtx).k(b.ue("com.huawei.android.pushagent", vd)))) {
            return null;
        }
        TokenApplyReqMeta tokenApplyReqMeta = new TokenApplyReqMeta();
        tokenApplyReqMeta.setPkgName("com.huawei.android.pushagent");
        tokenApplyReqMeta.setUserId(vd);
        com.huawei.android.pushagent.a.a.xx(62, "com.huawei.android.pushagent");
        return tokenApplyReqMeta;
    }
}
