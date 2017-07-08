package defpackage;

import android.content.Context;
import java.util.Map;

/* renamed from: bb */
public class bb {
    public static synchronized void aa(Context context) {
        synchronized (bb.class) {
            bt btVar = new bt(context, "pushConfig");
            int i = btVar.getInt("version_config");
            if (i != 2) {
                aw.i("PushLog2828", "update xml data, old version is " + i + ",new version is " + 2);
                if (i < 2) {
                    bb.ab(context);
                }
                btVar.a("version_config", Integer.valueOf(2));
            }
        }
    }

    private static void ab(Context context) {
        new bt(context, "device_info").clear();
        new bt(context, "PushRouteInfo").z("PushID");
        new bt(context, "pushConfig").z("selftoken");
        new bt(context, "push_client_self_info").z("token_info");
        new bt(context, "PushRouteInfo").z("PushID_encrypt");
        new bt(context, "pushConfig").z("selftoken_encrypt");
        new bt(context, "push_client_self_info").z("token_info_encrypt");
        au.n(context, "pclient_unRegist_info");
        bb.u(context, "pclient_info_encrypt");
        bb.u(context, "pclient_info");
    }

    private static void u(Context context, String str) {
        Map all = new bt(context, str).getAll();
        if (all != null && all.size() > 0) {
            bt btVar = new bt(context, "pclient_request_info");
            for (String str2 : all.keySet()) {
                btVar.f(str2, "true");
                aw.d("PushLog2828", str2 + " need to register again");
            }
        }
        au.n(context, str);
    }
}
