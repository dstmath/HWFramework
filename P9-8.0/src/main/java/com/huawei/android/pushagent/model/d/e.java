package com.huawei.android.pushagent.model.d;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.a.a;
import com.huawei.android.pushagent.datatype.b.c;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.b;
import java.util.ArrayList;
import java.util.Iterator;

public class e {
    private static e cu = new e();
    private ArrayList<c> ct = new ArrayList();

    private e() {
    }

    public static e kp() {
        return cu;
    }

    public void kq(String str, String str2) {
        c cVar = new c();
        cVar.setPkgName(str);
        cVar.wy(str2);
        cVar.wz(System.currentTimeMillis());
        kt(cVar);
    }

    private void kt(c cVar) {
        if (this.ct.size() >= 50) {
            this.ct.remove(0);
        }
        this.ct.add(cVar);
    }

    public void kr(Context context, String str) {
        Iterator it = this.ct.iterator();
        while (it.hasNext()) {
            if (str.equals(((c) it.next()).xa())) {
                a.xx(90, str);
                it.remove();
                break;
            }
        }
        if (this.ct.isEmpty()) {
            com.huawei.android.pushagent.utils.tools.a.qg(context, "com.huawei.android.push.intent.MSG_RSP_TIMEOUT");
        }
    }

    public void ks(Context context) {
        long currentTimeMillis = System.currentTimeMillis();
        long az = g.aq(context).az();
        Iterator it = this.ct.iterator();
        while (it.hasNext()) {
            c cVar = (c) it.next();
            if (currentTimeMillis - cVar.xb() > az) {
                String xa = cVar.xa();
                if (b.uk(context, cVar.xc())) {
                    a.xx(92, xa);
                } else {
                    a.xx(91, xa);
                }
                it.remove();
            }
        }
        if (!this.ct.isEmpty()) {
            com.huawei.android.pushagent.utils.tools.a.qf(context, new Intent("com.huawei.android.push.intent.MSG_RSP_TIMEOUT").setPackage(context.getPackageName()), g.aq(context).az());
        }
    }
}
