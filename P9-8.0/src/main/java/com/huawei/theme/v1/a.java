package com.huawei.theme.v1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.huawei.theme.a.a.c;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class a implements Runnable {
    private Context a;
    private String b;
    private String c;
    private long d;

    public a(Context context, String str, String str2, long j) {
        this.a = context;
        this.b = str.replace(",", "^");
        this.c = str2.replace(",", "^");
        this.d = j;
    }

    public final void run() {
        try {
            SharedPreferences a = c.a(this.a, "state");
            if (a == null) {
                com.huawei.theme.a.a.a.h();
                return;
            }
            Object string = a.getString("events", "");
            if (!"".equals(string)) {
                string = new StringBuilder(String.valueOf(string)).append(";").toString();
            }
            String stringBuilder = new StringBuilder(String.valueOf(string)).append(this.b).append(",").append(this.c).append(",").append(new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(new Date(this.d))).toString();
            int length = stringBuilder.split(";").length;
            if (length <= com.huawei.theme.a.a.a.d()) {
                Editor edit = a.edit();
                edit.remove("events");
                edit.putString("events", stringBuilder);
                edit.commit();
                " current event record number is：" + length;
                com.huawei.theme.a.a.a.h();
            }
            if (!com.huawei.theme.a.a.a.d(this.a)) {
                return;
            }
            if (com.huawei.theme.a.a.a.e()) {
                com.huawei.theme.a.a.a.h();
                HiStat.r(this.a);
                return;
            }
            a.edit().remove("events").commit();
        } catch (Exception e) {
            e.getMessage();
            com.huawei.theme.a.a.a.h();
            e.printStackTrace();
        }
    }
}
