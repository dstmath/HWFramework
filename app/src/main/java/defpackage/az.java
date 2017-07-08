package defpackage;

import android.content.Context;
import android.text.TextUtils;
import java.util.Map.Entry;

/* renamed from: az */
public class az {
    static boolean a(Context context, String str, String str2, String str3) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return false;
        }
        return new bt(context, str).f(str2 + "_v2", bj.encrypter(str3));
    }

    public static String k(Context context, String str, String str2) {
        String str3 = "";
        if (!(TextUtils.isEmpty(str) || TextUtils.isEmpty(str2))) {
            try {
                str3 = bj.decrypter(new bt(context, str).getString(str2 + "_v2"));
            } catch (Throwable e) {
                aw.d("PushLog2828", e.toString(), e);
            }
            if (TextUtils.isEmpty(str3)) {
                aw.d("PushLog2828", "not exist for:" + str2);
            }
        }
        return str3;
    }

    public static boolean l(Context context, String str, String str2) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return new bt(context, "pclient_info_v2").f(str, bj.encrypter(str2));
    }

    public static boolean m(Context context, String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return false;
        }
        return new bt(context, "pclient_unRegist_info_v2").f(bj.encrypter(str), str2);
    }

    public static String s(Context context, String str) {
        String str2 = "";
        if (!TextUtils.isEmpty(str)) {
            try {
                str2 = bj.decrypter(new bt(context, "pclient_info_v2").getString(str));
            } catch (Throwable e) {
                aw.d("PushLog2828", e.toString(), e);
            }
            if (TextUtils.isEmpty(str2)) {
                aw.d("PushLog2828", "not exist for:" + str);
            }
        }
        return str2;
    }

    public static void t(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            aw.e("PushLog2828", "removeUnRegisterToken token is empty");
            return;
        }
        try {
            bt btVar = new bt(context, "pclient_unRegist_info_v2");
            for (Entry key : btVar.getAll().entrySet()) {
                String str2 = (String) key.getKey();
                if (str.equals(bj.decrypter(str2))) {
                    btVar.z(str2);
                    return;
                }
            }
        } catch (Throwable e) {
            aw.d("PushLog2828", e.toString(), e);
        }
    }
}
