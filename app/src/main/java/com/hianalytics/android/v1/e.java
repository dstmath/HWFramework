package com.hianalytics.android.v1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.hianalytics.android.a.a.a;
import com.hianalytics.android.a.a.c;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class e implements Runnable {
    private Context a;
    private int b;
    private long c;

    public e(Context context, int i, long j) {
        this.a = context;
        this.b = i;
        this.c = j;
    }

    private static void a(Context context, SharedPreferences sharedPreferences, long j) {
        Editor edit = sharedPreferences.edit();
        String valueOf = String.valueOf(new StringBuilder(String.valueOf(j)).append(a.b(((TelephonyManager) context.getSystemService("phone")).getDeviceId())).toString());
        edit.remove("session_id");
        edit.remove("refer_id");
        edit.putString("session_id", valueOf);
        edit.putString("refer_id", "");
        edit.putLong("end_millis", j);
        edit.commit();
    }

    private void a(SharedPreferences sharedPreferences) {
        Editor edit = sharedPreferences.edit();
        edit.putLong("last_millis", this.c);
        edit.commit();
    }

    private void b(SharedPreferences sharedPreferences) {
        Object stringBuffer;
        JSONObject jSONObject = new JSONObject();
        Context context = this.a;
        StringBuffer stringBuffer2 = new StringBuffer("");
        SharedPreferences a = c.a(context, "sessioncontext");
        String string = a.getString("session_id", "");
        if ("".equals(string)) {
            string = a.b(((TelephonyManager) context.getSystemService("phone")).getDeviceId());
            long currentTimeMillis = System.currentTimeMillis();
            string = String.valueOf(new StringBuilder(String.valueOf(currentTimeMillis)).append(string).toString());
            Editor edit = a.edit();
            edit.putString("session_id", string);
            edit.putLong("end_millis", currentTimeMillis);
            edit.commit();
        }
        String str = string;
        String string2 = a.getString("refer_id", "");
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager != null) {
            stringBuffer2.append(a.c(context)[0]).append(",").append(telephonyManager.getNetworkOperatorName().replace(',', '&')).append(",").append(str).append(",").append(string2);
            stringBuffer = stringBuffer2.toString();
        } else {
            a.h();
            stringBuffer = null;
        }
        if (stringBuffer != null) {
            try {
                boolean z;
                String[] split;
                JSONArray jSONArray;
                if (sharedPreferences.getString("activities", "").trim().length() <= 0) {
                    z = true;
                } else {
                    split = sharedPreferences.getString("activities", "").split(";");
                    jSONArray = new JSONArray();
                    for (Object put : split) {
                        jSONArray.put(put);
                    }
                    jSONObject.put("b", jSONArray);
                    z = false;
                }
                if (sharedPreferences.getString("events", "").trim().length() > 0) {
                    split = sharedPreferences.getString("events", "").split(";");
                    jSONArray = new JSONArray();
                    for (Object put2 : split) {
                        jSONArray.put(put2);
                    }
                    jSONObject.put("e", jSONArray);
                    z = false;
                }
                jSONObject.put("h", stringBuffer);
                jSONObject.put("type", "termination");
                Handler f = a.f();
                if (f != null) {
                    f.post(new d(this.a, jSONObject, z));
                }
                a.h();
            } catch (Throwable e) {
                Log.e("HiAnalytics", "onTerminate: JSONException.", e);
                e.printStackTrace();
            }
        }
        Editor edit2 = sharedPreferences.edit();
        edit2.putString("activities", "");
        edit2.remove("events");
        edit2.commit();
    }

    private boolean c(SharedPreferences sharedPreferences) {
        return !(((this.c - sharedPreferences.getLong("last_millis", -1)) > (a.a().longValue() * 1000) ? 1 : ((this.c - sharedPreferences.getLong("last_millis", -1)) == (a.a().longValue() * 1000) ? 0 : -1)) <= 0);
    }

    public final void run() {
        Object obj = null;
        try {
            Context context = this.a;
            long j = this.c;
            SharedPreferences a = c.a(context, "sessioncontext");
            if ("".equals(a.getString("session_id", ""))) {
                a(context, a, j);
            } else {
                if (j - a.getLong("end_millis", 0) <= a.c().longValue() * 1000) {
                    obj = 1;
                }
                if (obj == null) {
                    a(context, a, j);
                } else {
                    Editor edit = a.edit();
                    edit.putLong("end_millis", j);
                    edit.commit();
                }
            }
            if (this.b == 0) {
                Context context2 = this.a;
                if (this.a == context2) {
                    this.a = context2;
                    SharedPreferences a2 = c.a(context2, "state");
                    if (a2 != null) {
                        j = a2.getLong("last_millis", -1);
                        if (j == -1) {
                            a.h();
                        } else {
                            long j2 = this.c - j;
                            long j3 = a2.getLong("duration", 0);
                            Editor edit2 = a2.edit();
                            obj = a2.getString("activities", "");
                            String name = context2.getClass().getName();
                            if (!"".equals(obj)) {
                                obj = new StringBuilder(String.valueOf(obj)).append(";").toString();
                            }
                            String stringBuilder = new StringBuilder(String.valueOf(obj)).append(name).append(",").append(new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(new Date(j))).append(",").append(j2 / 1000).toString();
                            edit2.remove("activities");
                            edit2.putString("activities", stringBuilder);
                            edit2.putLong("duration", j3 + j2);
                            edit2.commit();
                        }
                        if (c(a2)) {
                            b(a2);
                            a(a2);
                            return;
                        } else if (a.d(context2)) {
                            b(a2);
                            a(a2);
                        }
                    }
                    return;
                }
                a.h();
            } else if (this.b != 1) {
                if (this.b == 2) {
                    r0 = c.a(this.a, "state");
                    if (r0 != null) {
                        b(r0);
                    }
                }
            } else {
                Context context3 = this.a;
                this.a = context3;
                r0 = c.a(context3, "state");
                if (r0 != null && c(r0)) {
                    b(r0);
                    a(r0);
                }
            }
        } catch (Exception e) {
            "SessionThread.run() throw exception:" + e.getMessage();
            a.h();
            e.printStackTrace();
        }
    }
}
