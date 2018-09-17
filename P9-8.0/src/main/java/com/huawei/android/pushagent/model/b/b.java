package com.huawei.android.pushagent.model.b;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.datatype.tcp.PushDataReqMessage;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.d.a;
import com.huawei.android.pushagent.model.d.f;
import com.huawei.android.pushagent.utils.a.e;
import com.huawei.android.pushagent.utils.d.c;
import com.huawei.android.pushagent.utils.tools.d;

public class b extends c {
    public b(Context context, PushDataReqMessage pushDataReqMessage) {
        super(context, pushDataReqMessage);
    }

    protected boolean ic(Context context) {
        if (1 != g.aq(context).ba() || (if(context) ^ 1) == 0 || (f.kv(context, this.pkgName, String.valueOf(this.cb)) ^ 1) == 0) {
            return true;
        }
        c.sh("PushLog2951", this.pkgName + " is not registed,user id is " + this.cb);
        return false;
    }

    protected boolean ib(Context context) {
        if (1 != g.aq(context).ba() || (if(context) ^ 1) == 0 || (f.kw(context, this.pkgName, String.valueOf(this.cb), this.tokenBytes) ^ 1) == 0) {
            return true;
        }
        c.sh("PushLog2951", this.pkgName + " token is not equal,user id is " + this.cb);
        return false;
    }

    protected boolean hz(Context context) {
        return true;
    }

    protected boolean ia(Context context) {
        return true;
    }

    protected boolean hy(Context context) {
        return true;
    }

    protected void id(Context context) {
        if (a.jp().jr(context, this.pkgName)) {
            a.jp().jv(context, this.cc, this.pkgName, this.tokenBytes, this.cd, this.cb);
            return;
        }
        ie(context, this.pkgName, this.tokenBytes, this.cc, this.cb, this.cd);
    }

    private void ie(Context context, String str, byte[] bArr, byte[] bArr2, int i, String str2) {
        if (d.qo()) {
            d.qp(2, 180);
        } else {
            com.huawei.android.pushagent.utils.b.tl(2, 180);
        }
        Intent intent = new Intent("com.huawei.android.push.intent.RECEIVE");
        intent.setPackage(str).putExtra("msg_data", bArr2).putExtra("device_token", bArr).putExtra("msgIdStr", e.nv(str2)).setFlags(32);
        com.huawei.android.pushagent.model.d.e.kp().kq(str, str2);
        com.huawei.android.pushagent.utils.b.tr(context, intent, i);
        com.huawei.android.pushagent.utils.tools.a.qf(context, new Intent("com.huawei.android.push.intent.MSG_RSP_TIMEOUT").setPackage(context.getPackageName()), g.aq(context).az());
    }
}
