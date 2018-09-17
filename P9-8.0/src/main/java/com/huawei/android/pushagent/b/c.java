package com.huawei.android.pushagent.b;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.a.a;
import com.huawei.android.pushagent.model.a.b;
import com.huawei.android.pushagent.model.a.e;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.a.h;
import com.huawei.android.pushagent.model.a.i;
import com.huawei.android.pushagent.model.a.j;
import java.util.Map.Entry;

public class c {
    private Context appCtx;
    private h ig = h.dp(this.appCtx);

    public c(Context context) {
        this.appCtx = context.getApplicationContext();
    }

    public void yj() {
        if (!yo()) {
            yt();
        } else if (yq()) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "The GDPR config is exist and valid, no need update again.");
        } else {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "The GDPR config is exist but invalid, need recorrect and delete wrong files.");
            a.xv(13);
            yn();
            yt();
        }
    }

    private void yn() {
        b l = b.l(this.appCtx);
        for (String str : l.p()) {
            if (!TextUtils.isEmpty(str)) {
                j.ev(this.appCtx).ey(str);
                com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "pkgName: " + str + " need register again");
            }
        }
        l.j();
        this.ig.dt();
        g.aq(this.appCtx).setValue("connId", "");
    }

    private void yt() {
        yv();
        ys();
        yw();
    }

    private boolean yo() {
        if (this.ig.getDeviceIdType() == -1 || this.ig.getDeviceId().length() <= 0) {
            return false;
        }
        return true;
    }

    private boolean yq() {
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "check GDPR config is valid, DeviceType:" + this.ig.dq() + ", DeviceIdType:" + this.ig.getDeviceIdType() + ", DeviceId length:" + this.ig.getDeviceId().length());
        return 1 == this.ig.dq() ? (this.ig.getDeviceIdType() == 0 || 6 == this.ig.getDeviceIdType()) && 16 == this.ig.getDeviceId().length() : (9 == this.ig.getDeviceIdType() || 6 == this.ig.getDeviceIdType()) && this.ig.getDeviceId().length() > 16;
    }

    public void yw() {
        Iterable<Entry> ai = e.ae(this.appCtx).ai();
        if (ai.size() == 0) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "no notify_switch need to update");
            return;
        }
        for (Entry entry : ai) {
            String str = (String) entry.getKey();
            boolean booleanValue = ((Boolean) entry.getValue()).booleanValue();
            if (TextUtils.isEmpty(str)) {
                com.huawei.android.pushagent.utils.d.c.sj("PushLog2951", "pkgNameWithUid is empty");
            } else {
                e.ae(this.appCtx).ah(com.huawei.android.pushagent.utils.b.ty(str), booleanValue);
                e.ae(this.appCtx).ag(str);
            }
        }
    }

    private void ys() {
        g aq = g.aq(this.appCtx);
        aq.setValue("serverIp", "");
        aq.setValue("serverPort", Integer.valueOf(-1));
        aq.setValue("result", Integer.valueOf(-1));
        i.ea(this.appCtx).ed(0);
        i.ea(this.appCtx).eg(0);
    }

    private boolean yr() {
        if (TextUtils.isEmpty(com.huawei.android.pushagent.utils.c.e.qx(this.appCtx, 2).getDeviceId())) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "not support get UDID.");
            return false;
        }
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "support get UDID.");
        return true;
    }

    private boolean yp() {
        if (b.l(this.appCtx).o() || (TextUtils.isEmpty(this.ig.du()) ^ 1) != 0) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "exsit old IMEI config.");
            return true;
        }
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "It is a new device.");
        return false;
    }

    private void yu(int i) {
        Object du = this.ig.du();
        if (1 != i || (TextUtils.isEmpty(du) ^ 1) == 0) {
            this.ig.dr("");
            com.huawei.android.pushagent.utils.b.ua(this.appCtx);
        } else {
            this.ig.ds(du);
            this.ig.setDeviceIdType(0);
        }
        this.ig.dx();
        this.ig.dy();
    }

    private void yv() {
        if (!yr() || yp()) {
            this.ig.dz(1);
            yu(1);
            return;
        }
        this.ig.dz(2);
        yu(2);
    }
}
