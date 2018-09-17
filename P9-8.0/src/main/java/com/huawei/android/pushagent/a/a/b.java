package com.huawei.android.pushagent.a.a;

import android.content.Context;
import com.huawei.android.pushagent.utils.d.c;

public abstract class b implements c {
    protected Context appCtx;
    protected c hu;

    protected abstract void xk(Context context);

    protected abstract void xn(String str, String str2);

    protected abstract void xo(long j, String str, String str2, String str3);

    public void xr(Context context) {
        this.appCtx = context.getApplicationContext();
        xk(this.appCtx);
        if (this.hu != null) {
            this.hu.xr(this.appCtx);
        }
    }

    public void xs(String str, String str2) {
        if (this.appCtx == null) {
            c.sj("PushLog2951", "Please init log first");
            return;
        }
        xn(str, str2);
        if (this.hu != null) {
            this.hu.xs(str, str2);
        }
    }

    public void xt(long j, String str, String str2, String str3) {
        if (this.appCtx == null) {
            c.sj("PushLog2951", "Please init log first");
            return;
        }
        xo(j, str, str2, str3);
        if (this.hu != null) {
            this.hu.xt(j, str, str2, str3);
        }
    }

    protected void xm(boolean z) {
    }

    public void xu(boolean z) {
        if (this.appCtx == null) {
            c.sj("PushLog2951", "Please init log first");
            return;
        }
        xm(z);
        if (this.hu != null) {
            this.hu.xu(z);
        }
    }
}
