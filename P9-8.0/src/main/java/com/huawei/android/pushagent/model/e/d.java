package com.huawei.android.pushagent.model.e;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.constant.HttpMethod;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;

public abstract class d<T> {
    private Context appCtx;

    protected abstract int mz();

    protected abstract String na();

    protected abstract String nb();

    protected abstract T nc(String str);

    public d(Context context) {
        this.appCtx = context.getApplicationContext();
    }

    protected Context ne() {
        return this.appCtx;
    }

    public T nd() {
        if (!b.ul(this.appCtx)) {
            return nc(null);
        }
        String nb = nb();
        c.sg("PushLog2951", "http query start");
        com.huawei.android.pushagent.utils.e.b bVar = new com.huawei.android.pushagent.utils.e.b(this.appCtx, nb, null);
        bVar.sq(((int) g.aq(this.appCtx).as()) * 1000);
        bVar.sr(((int) g.aq(this.appCtx).at()) * 1000);
        nb = na();
        Object ng = ng(bVar, nb, false, false);
        if (!TextUtils.isEmpty(ng)) {
            return nc(ng);
        }
        ng = ng(bVar, nb, true, false);
        if (!TextUtils.isEmpty(ng)) {
            return nc(ng);
        }
        ng = ng(bVar, nb, false, true);
        if (!TextUtils.isEmpty(ng)) {
            return nc(ng);
        }
        Object ng2 = ng(bVar, nb, true, true);
        if (!TextUtils.isEmpty(ng2)) {
            return nc(ng2);
        }
        c.sg("PushLog2951", "query https failed");
        return nc(null);
    }

    private String ng(com.huawei.android.pushagent.utils.e.b bVar, String str, boolean z, boolean z2) {
        bVar.ss(z);
        if (z2) {
            bVar.st(mz());
        } else {
            bVar.st(0);
        }
        return bVar.su(str, HttpMethod.POST);
    }

    public static String nf(String str, String str2) {
        if (TextUtils.isEmpty(str2) || TextUtils.isEmpty(str)) {
            c.sj("PushLog2951", "belongId is null or trsAddress is null");
            return str;
        }
        try {
            int parseInt = Integer.parseInt(str2.trim());
            if (parseInt <= 0) {
                c.sj("PushLog2951", "belongId is invalid:" + parseInt);
                return str;
            }
            int indexOf = str.indexOf(".");
            if (indexOf > -1) {
                return new StringBuffer().append(str.substring(0, indexOf)).append(parseInt).append(str.substring(indexOf)).toString();
            }
            return str;
        } catch (Throwable e) {
            c.se("PushLog2951", "belongId parseInt error " + str2, e);
            return str;
        } catch (Throwable e2) {
            c.se("PushLog2951", e2.getMessage(), e2);
            return str;
        }
    }
}
