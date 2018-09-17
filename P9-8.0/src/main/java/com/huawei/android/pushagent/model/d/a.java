package com.huawei.android.pushagent.model.d;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.a.e;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;
import com.huawei.android.pushagent.utils.tools.d;
import java.util.ArrayList;
import java.util.List;

public class a {
    private static final a ch = new a();
    private List<com.huawei.android.pushagent.datatype.b.a> cg = new ArrayList();
    private boolean ci = false;
    private String[] cj;

    private a() {
    }

    public static a jp() {
        return ch;
    }

    public void jq() {
        if (d.qo()) {
            if (1 == d.qq()) {
                c.sg("PushLog2951", "push is in socket ctrl model, only white packages app can use push");
                this.ci = true;
                this.cj = d.qr();
            } else {
                c.sg("PushLog2951", "all apps can use push");
                this.ci = false;
                this.cj = new String[0];
            }
            return;
        }
        c.sg("PushLog2951", "not support ctrlsocket v2 ");
    }

    public boolean jr(Context context, String str) {
        if (!this.ci) {
            return false;
        }
        if (this.cj == null || this.cj.length <= 0) {
            c.sh("PushLog2951", "whitePackages is empty, push message's owner is not white app, send it when screen on");
            return true;
        }
        boolean z;
        for (String equals : this.cj) {
            if (equals.equals(str)) {
                z = true;
                break;
            }
        }
        z = false;
        if (z) {
            return false;
        }
        c.sh("PushLog2951", "push message's owner is not white app, send it when screen on");
        return true;
    }

    public void js(Intent intent) {
        boolean booleanExtra = intent.getBooleanExtra("ctrl_socket_status", false);
        this.ci = booleanExtra;
        if (booleanExtra) {
            Object stringExtra = intent.getStringExtra("ctrl_socket_list");
            c.sh("PushLog2951", "only whitepackages can use push:" + stringExtra);
            if (!TextUtils.isEmpty(stringExtra)) {
                this.cj = stringExtra.split("\t");
                return;
            }
            return;
        }
        this.cj = new String[0];
        c.sh("PushLog2951", "not support push in sleep model");
    }

    public void jt(Context context) {
        this.ci = false;
        this.cj = new String[0];
        c.sh("PushLog2951", "all packages allow to use push, send cached messages to apps");
        for (com.huawei.android.pushagent.datatype.b.a aVar : this.cg) {
            if (!(aVar.getToken() == null || aVar.ws() == null)) {
                ju(context, aVar.wt(), aVar.getToken(), aVar.ws(), aVar.wu(), aVar.wv());
            }
        }
        this.cg.clear();
    }

    public void jv(Context context, byte[] bArr, String str, byte[] bArr2, String str2, int i) {
        if (1000 <= this.cg.size()) {
            this.cg.remove(0);
        }
        this.cg.add(new com.huawei.android.pushagent.datatype.b.a(str, bArr2, bArr, i, str2));
    }

    private void ju(Context context, String str, byte[] bArr, byte[] bArr2, int i, String str2) {
        if (d.qo()) {
            d.qp(2, 180);
        } else {
            b.tl(2, 180);
        }
        Intent intent = new Intent("com.huawei.android.push.intent.RECEIVE");
        intent.setPackage(str).putExtra("msg_data", bArr2).putExtra("device_token", bArr).putExtra("msgIdStr", e.nv(str2)).setFlags(32);
        e.kp().kq(str, str2);
        b.tr(context, intent, i);
        com.huawei.android.pushagent.utils.tools.a.qf(context, new Intent("com.huawei.android.push.intent.MSG_RSP_TIMEOUT").setPackage(context.getPackageName()), g.aq(context).az());
    }
}
