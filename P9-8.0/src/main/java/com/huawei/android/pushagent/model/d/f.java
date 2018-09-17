package com.huawei.android.pushagent.model.d;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.model.a.a;
import com.huawei.android.pushagent.model.a.b;
import com.huawei.android.pushagent.model.a.h;
import com.huawei.android.pushagent.model.a.j;
import com.huawei.android.pushagent.utils.d.c;
import java.io.UnsupportedEncodingException;

public class f {
    private static final byte[] cv = new byte[0];
    private static f cw = null;

    public static void ky(Context context) {
        Iterable<String> p = b.l(context).p();
        if (p.size() > 0) {
            for (String str : p) {
                if (!com.huawei.android.pushagent.utils.b.um(context, com.huawei.android.pushagent.utils.b.uf(str), com.huawei.android.pushagent.utils.b.uh(str))) {
                    a.c(context).h(b.l(context).k(str), str);
                    b.l(context).r(str);
                }
            }
        }
    }

    public static boolean kx(Context context, String str, String str2) {
        if (TextUtils.isEmpty(b.l(context).k(com.huawei.android.pushagent.utils.b.ue(str, str2)))) {
            return false;
        }
        return true;
    }

    public static boolean kv(Context context, String str, String str2) {
        int dq = h.dp(context).dq();
        String ue = com.huawei.android.pushagent.utils.b.ue(str, str2);
        if (1 == dq) {
            if (com.huawei.android.pushagent.model.a.f.ak(context).an(ue) || (TextUtils.isEmpty(b.l(context).k(ue)) ^ 1) != 0) {
                return true;
            }
        } else if (!TextUtils.isEmpty(b.l(context).n(ue))) {
            return true;
        }
        return false;
    }

    public static boolean kw(Context context, String str, String str2, byte[] bArr) {
        int dq = h.dp(context).dq();
        String ue = com.huawei.android.pushagent.utils.b.ue(str, str2);
        if (1 == dq) {
            return true;
        }
        if (bArr != null) {
            try {
                if (new String(bArr, "UTF-8").equals(b.l(context).n(ue))) {
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                c.sf("PushLog2951", "server token parse string failed");
            }
        }
        return false;
    }

    public static boolean ku(Context context) {
        if (b.l(context).o() || (a.c(context).e() ^ 1) == 0 || (j.ev(context).ez() ^ 1) == 0 || (com.huawei.android.pushagent.model.a.f.ak(context).ao() ^ 1) == 0) {
            return true;
        }
        return false;
    }
}
