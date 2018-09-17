package com.huawei.android.pushagent.model.b;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.tcp.PushDataReqMessage;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.tools.d;
import java.util.Arrays;

public abstract class c implements e {
    private Context appCtx;
    protected int cb;
    protected byte[] cc;
    protected String cd;
    protected PushDataReqMessage ce;
    protected String pkgName;
    protected byte[] tokenBytes;

    protected abstract boolean hy(Context context);

    protected abstract boolean hz(Context context);

    protected abstract boolean ia(Context context);

    protected abstract boolean ib(Context context);

    protected abstract boolean ic(Context context);

    protected abstract void id(Context context);

    public c(Context context, PushDataReqMessage pushDataReqMessage) {
        this.appCtx = context.getApplicationContext();
        this.ce = pushDataReqMessage;
    }

    private boolean isValid() {
        if (this.ce.isValid()) {
            return true;
        }
        return false;
    }

    private void ij() {
        if (d.qo()) {
            d.qp(2, 180);
        } else {
            b.tl(2, 180);
        }
    }

    protected void ik() {
        this.pkgName = this.ce.getPkgName();
        this.cb = this.ce.getUserId();
        this.tokenBytes = this.ce.vy();
        this.cc = this.ce.vz();
        this.cd = com.huawei.android.pushagent.utils.d.b.sb(this.ce.wa());
    }

    protected void ii() {
    }

    private boolean il() {
        if (b.um(this.appCtx, this.pkgName, this.cb)) {
            return true;
        }
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", this.pkgName + " is not installed in " + this.cb + " user");
        return false;
    }

    private boolean ig(Context context) {
        boolean bg = g.aq(context).bg();
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "needCheckAgreement:" + bg);
        if (bg) {
            bg = com.huawei.android.pushagent.model.a.d.aa(context).ab(this.pkgName);
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "disAgree:" + bg);
            if (bg) {
                return false;
            }
        }
        return true;
    }

    protected boolean if(Context context) {
        Object trim = g.aq(context).bh().trim();
        if (TextUtils.isEmpty(trim) || !Arrays.asList(trim.split("#")).contains(this.pkgName)) {
            return false;
        }
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", this.pkgName + " is in pass white list, not valid token.");
        return true;
    }

    protected boolean im(Context context) {
        Object trim = g.aq(context).bi().trim();
        if (TextUtils.isEmpty(trim) || !Arrays.asList(trim.split("#")).contains(this.pkgName)) {
            return false;
        }
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", this.pkgName + " is in notice white list, not valid token.");
        return true;
    }

    private boolean ih(Context context) {
        return b.tu(context, this.pkgName, this.cb);
    }

    public byte in() {
        byte b = (byte) 0;
        if (!isValid()) {
            return (byte) 0;
        }
        ij();
        ik();
        ii();
        if (!il()) {
            return (byte) 2;
        }
        if (!ig(this.appCtx)) {
            return (byte) 17;
        }
        if (!ic(this.appCtx)) {
            return (byte) 5;
        }
        if (!ib(this.appCtx)) {
            return (byte) 19;
        }
        if (!hz(this.appCtx)) {
            return (byte) 6;
        }
        if (!ia(this.appCtx)) {
            return (byte) 9;
        }
        if (!hy(this.appCtx)) {
            b = (byte) 7;
        }
        if (!ih(this.appCtx)) {
            b = (byte) 18;
        }
        id(this.appCtx);
        return b;
    }
}
