package com.hianalytics.android.v1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;
import com.hianalytics.android.a.a.a;
import com.hianalytics.android.a.a.c;

/* compiled from: Unknown */
public class SessionContext {
    private static String createSessionID(Context context) {
        return String.valueOf(System.currentTimeMillis() + a.b(((TelephonyManager) context.getSystemService("phone")).getDeviceId()));
    }

    public static void destroy(Context context) {
        Editor edit = c.a(context, "sessioncontext").edit();
        edit.remove("session_id");
        edit.remove("refer_id");
        edit.commit();
    }

    public static void destroyReferID(Context context) {
        Editor edit = c.a(context, "sessioncontext").edit();
        edit.remove("refer_id");
        edit.commit();
    }

    public static String getReferID(Context context) {
        return c.a(context, "sessioncontext").getString("refer_id", "");
    }

    public static String getSessionID(Context context) {
        return c.a(context, "sessioncontext").getString("session_id", "");
    }

    public static void init(Context context, String str) {
        SharedPreferences a = c.a(context, "sessioncontext");
        String createSessionID = createSessionID(context);
        Editor edit = a.edit();
        edit.remove("session_id");
        edit.remove("refer_id");
        edit.putString("session_id", createSessionID);
        edit.putString("refer_id", str);
        edit.commit();
    }

    public static void setReferID(Context context, String str) {
        Editor edit = c.a(context, "sessioncontext").edit();
        edit.putString("refer_id", str);
        edit.commit();
    }

    public static void setSessionID(Context context, String str) {
        Editor edit = c.a(context, "sessioncontext").edit();
        edit.putString("session_id", str);
        edit.commit();
    }
}
