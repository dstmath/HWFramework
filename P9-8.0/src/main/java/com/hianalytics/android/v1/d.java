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

public final class d implements Runnable {
    private Context a;
    private int b;
    private long c;

    public d(Context context, int i, long j) {
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

    private void b(SharedPreferences -l_3_R) {
        Object stringBuffer;
        boolean z = true;
        JSONObject jSONObject = new JSONObject();
        Context context = this.a;
        StringBuffer stringBuffer2 = new StringBuffer("");
        SharedPreferences a = c.a(context, "sessioncontext");
        String string = a.getString("session_id", "");
        if ("".equals(string)) {
            String b = a.b(((TelephonyManager) context.getSystemService("phone")).getDeviceId());
            long currentTimeMillis = System.currentTimeMillis();
            string = String.valueOf(new StringBuilder(String.valueOf(currentTimeMillis)).append(b).toString());
            Editor edit = a.edit();
            edit.putString("session_id", string);
            edit.putLong("end_millis", currentTimeMillis);
            edit.commit();
        }
        String string2 = a.getString("refer_id", "");
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager != null) {
            stringBuffer2.append(a.c(context)[0]).append(",").append(telephonyManager.getNetworkOperatorName().replace(',', '&')).append(",").append(string).append(",").append(string2);
            stringBuffer = stringBuffer2.toString();
        } else {
            a.h();
            stringBuffer = null;
        }
        if (stringBuffer != null) {
            try {
                String[] split;
                JSONArray jSONArray;
                if (-l_3_R.getString("activities", "").trim().length() > 0) {
                    split = -l_3_R.getString("activities", "").split(";");
                    jSONArray = new JSONArray();
                    for (String put : split) {
                        jSONArray.put(put);
                    }
                    jSONObject.put("b", jSONArray);
                    z = false;
                }
                if (-l_3_R.getString("events", "").trim().length() > 0) {
                    split = -l_3_R.getString("events", "").split(";");
                    jSONArray = new JSONArray();
                    for (String put2 : split) {
                        jSONArray.put(put2);
                    }
                    jSONObject.put("e", jSONArray);
                    z = false;
                }
                jSONObject.put("h", stringBuffer);
                jSONObject.put("type", "termination");
                Handler f = a.f();
                if (f != null) {
                    f.post(new c(this.a, jSONObject, z));
                }
                a.h();
            } catch (Throwable e) {
                Log.e("HiAnalytics", "onTerminate: JSONException.", e);
                e.printStackTrace();
                Throwable th = e;
            }
        }
        Editor edit2 = -l_3_R.edit();
        edit2.putString("activities", "");
        edit2.remove("events");
        edit2.commit();
    }

    private boolean c(SharedPreferences sharedPreferences) {
        return !(((this.c - sharedPreferences.getLong("last_millis", -1)) > (a.a().longValue() * 1000) ? 1 : ((this.c - sharedPreferences.getLong("last_millis", -1)) == (a.a().longValue() * 1000) ? 0 : -1)) <= 0);
    }

    public final void run() {
        try {
            Context context = this.a;
            long j = this.c;
            SharedPreferences a = c.a(context, "sessioncontext");
            if ("".equals(a.getString("session_id", ""))) {
                a(context, a, j);
            } else {
                if ((j - a.getLong("end_millis", 0) <= a.c().longValue() * 1000 ? 1 : null) == null) {
                    a(context, a, j);
                } else {
                    Editor edit = a.edit();
                    edit.putLong("end_millis", j);
                    edit.commit();
                }
            }
            SharedPreferences a2;
            if (this.b == 0) {
                context = this.a;
                if (this.a == context) {
                    this.a = context;
                    a2 = c.a(context, "state");
                    if (a2 != null) {
                        long j2 = a2.getLong("last_millis", -1);
                        if (j2 == -1) {
                            a.h();
                        } else {
                            long j3 = this.c - j2;
                            long j4 = a2.getLong("duration", 0);
                            Editor edit2 = a2.edit();
                            Object string = a2.getString("activities", "");
                            String name = context.getClass().getName();
                            if (!"".equals(string)) {
                                string = new StringBuilder(String.valueOf(string)).append(";").toString();
                            }
                            long j5 = j2;
                            String stringBuilder = new StringBuilder(String.valueOf(string)).append(name).append(",").append(new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(new Date(j2))).append(",").append(j3 / 1000).toString();
                            edit2.remove("activities");
                            edit2.putString("activities", stringBuilder);
                            edit2.putLong("duration", j4 + j3);
                            edit2.commit();
                        }
                        if (c(a2)) {
                            b(a2);
                            a(a2);
                            return;
                        } else if (a.d(context)) {
                            b(a2);
                            a(a2);
                        }
                    }
                    return;
                }
                a.h();
            } else if (this.b != 1) {
                if (this.b == 2) {
                    a2 = c.a(this.a, "state");
                    if (a2 != null) {
                        b(a2);
                    }
                }
            } else {
                context = this.a;
                this.a = context;
                a2 = c.a(context, "state");
                if (a2 != null && c(a2)) {
                    b(a2);
                    a(a2);
                }
            }
        } catch (Exception e) {
            "SessionThread.run() throw exception:" + e.getMessage();
            a.h();
            e.printStackTrace();
        }
    }
}
