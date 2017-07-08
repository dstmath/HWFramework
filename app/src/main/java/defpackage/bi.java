package defpackage;

import android.text.TextUtils;
import android.util.Log;
import java.util.Iterator;
import org.json.JSONObject;

/* renamed from: bi */
public class bi {
    private static String a(char c, int i) {
        StringBuffer stringBuffer = new StringBuffer(i);
        for (int i2 = 0; i2 < i; i2++) {
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }

    public static String b(JSONObject jSONObject) {
        if (jSONObject == null || jSONObject.length() == 0) {
            return "";
        }
        try {
            StringBuffer stringBuffer = new StringBuffer();
            Iterator keys = jSONObject.keys();
            while (keys.hasNext()) {
                String str = (String) keys.next();
                stringBuffer.append(str).append("=").append(bi.w(String.valueOf(jSONObject.get(str)))).append(" ");
            }
            return stringBuffer.toString();
        } catch (Throwable e) {
            Log.e("PushLog2828", "getProguard jsonobj error:" + jSONObject.toString(), e);
            return "";
        }
    }

    public static String w(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (str.length() < 2) {
            return str;
        }
        try {
            int ceil = (int) Math.ceil(((double) (str.length() * 25)) / 100.0d);
            int ceil2 = (int) Math.ceil(((double) (str.length() * 50)) / 100.0d);
            return str.substring(0, ceil) + bi.a('*', ceil2) + str.substring(ceil + ceil2);
        } catch (Exception e) {
            return "";
        }
    }
}
