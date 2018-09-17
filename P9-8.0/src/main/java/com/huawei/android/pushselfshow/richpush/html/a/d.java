package com.huawei.android.pushselfshow.richpush.html.a;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.richpush.html.api.NativeToJsMessageQueue;
import com.huawei.android.pushselfshow.richpush.html.api.d.a;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.BasicCloudField;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.MessageSafeConfigJsonField;
import org.json.JSONException;
import org.json.JSONObject;

public class d implements g {
    public boolean a = false;
    public int b;
    public int c;
    private NativeToJsMessageQueue d;
    private String e;
    private Activity f;

    public d(Activity activity) {
        c.e("PushSelfShowLog", "init App");
        this.f = activity;
    }

    private void a(String str, String str2, boolean z) {
        try {
            c.a("PushSelfShowLog", "enter launchApp , appPackageName =" + str + ",and msg.intentUri is " + str2 + " boolean appmarket is " + z);
            if (str == null || str.trim().length() == 0) {
                this.d.a(this.e, a.JSON_EXCEPTION, "error", null);
                return;
            }
            Intent b = com.huawei.android.pushselfshow.utils.a.b(this.f, str);
            if (b != null) {
                if (str2 != null) {
                    try {
                        Intent parseUri = Intent.parseUri(str2, 0);
                        c.e("PushSelfShowLog", "Intent.parseUri(intentUri, 0)，" + b.toURI());
                        if (com.huawei.android.pushselfshow.utils.a.a(this.f, str, parseUri).booleanValue()) {
                            b = parseUri;
                        }
                    } catch (Throwable e) {
                        c.a("PushSelfShowLog", "intentUri error ", e);
                    }
                }
                if (com.huawei.android.pushselfshow.utils.a.a(this.f, b)) {
                    if (this.a) {
                        c.e("PushSelfShowLog", " APP_OPEN startActivityForResult " + b.toURI());
                        this.f.startActivityForResult(b, this.b);
                    } else {
                        c.e("PushSelfShowLog", " APP_OPEN start " + b.toURI());
                        this.f.startActivity(b);
                    }
                    this.d.a(this.e, a.OK, "success", null);
                }
                c.c("PushSelfShowLog", "no permission to start Activity");
                this.d.a(this.e, a.ILLEGAL_ACCESS_EXCEPTION, "error", null);
                return;
            }
            if (z) {
                a(str);
            } else {
                c.e("PushSelfShowLog", "APP_NOT_EXIST and appmaeket is false");
                this.d.a(this.e, a.APP_NOT_EXIST, "error", null);
            }
        } catch (Throwable e2) {
            c.d("PushSelfShowLog", e2.toString(), e2);
        }
    }

    private void a(JSONObject jSONObject) {
        String str = null;
        String str2 = null;
        boolean z = false;
        if (jSONObject != null && jSONObject.has("package-name")) {
            try {
                str = jSONObject.getString("package-name");
                if (jSONObject.has("intent-uri")) {
                    str2 = jSONObject.getString("intent-uri");
                }
                if (jSONObject.has("appmarket")) {
                    z = jSONObject.getBoolean("appmarket");
                }
                if (jSONObject.has("requestCode") && jSONObject.has(BasicCloudField.CONFIG_SERVRE_RESULT_CODE)) {
                    this.b = jSONObject.getInt("requestCode");
                    this.c = jSONObject.getInt(BasicCloudField.CONFIG_SERVRE_RESULT_CODE);
                    this.a = true;
                }
            } catch (Throwable e) {
                c.e("PushSelfShowLog", "openApp param failed ", e);
            }
            c.b("PushSelfShowLog", "packageName is %s , appmarket is %s ,bResult is %s ", str, Boolean.valueOf(z), Boolean.valueOf(this.a));
            a(str, str2, z);
            return;
        }
        this.d.a(this.e, a.JSON_EXCEPTION, "error", null);
    }

    private void b(JSONObject jSONObject) {
        if (jSONObject != null && jSONObject.has("package-name")) {
            try {
                String string = jSONObject.getString("package-name");
                JSONObject jSONObject2 = new JSONObject();
                PackageManager packageManager = this.f.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(string, 0);
                String charSequence = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                String str = packageInfo.versionName;
                int i = packageInfo.versionCode;
                jSONObject2.put("appName", charSequence);
                jSONObject2.put("versionCode", i);
                jSONObject2.put("versionName", str);
                this.d.a(this.e, a.OK, "success", jSONObject2);
                return;
            } catch (Throwable e) {
                c.e("PushSelfShowLog", "getAppInfo param failed ", e);
                this.d.a(this.e, a.APP_NOT_EXIST, "error", null);
                return;
            }
        }
        this.d.a(this.e, a.JSON_EXCEPTION, "error", null);
    }

    private String c(JSONObject jSONObject) throws JSONException {
        JSONObject a;
        if (jSONObject != null && jSONObject.has("package-name")) {
            try {
                String string = jSONObject.getString("package-name");
                PackageManager packageManager = this.f.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(string, 0);
                String charSequence = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                String str = packageInfo.versionName;
                int i = packageInfo.versionCode;
                a = com.huawei.android.pushselfshow.richpush.html.api.d.a(a.OK);
                a.put("appName", charSequence);
                a.put("versionCode", i);
                a.put("versionName", str);
            } catch (Throwable e) {
                c.e("PushSelfShowLog", "getAppInfo param failed ", e);
                a = com.huawei.android.pushselfshow.richpush.html.api.d.a(a.APP_NOT_EXIST);
            }
        } else {
            a = com.huawei.android.pushselfshow.richpush.html.api.d.a(a.JSON_EXCEPTION);
        }
        return a.toString();
    }

    public String a(String str, JSONObject jSONObject) {
        try {
            return !"getAppInfo".equals(str) ? com.huawei.android.pushselfshow.richpush.html.api.d.a(a.METHOD_NOT_FOUND_EXCEPTION).toString() : c(jSONObject);
        } catch (JSONException e) {
            c.d("PushSelfShowLog", e.toString());
            return null;
        }
    }

    public void a(int i, int i2, Intent intent) {
        c.e("PushSelfShowLog", "onActivityResult and requestCode is " + i + " resultCode is " + i2 + " intent data is " + intent);
        try {
            if (this.a && i2 == this.c && intent != null) {
                JSONObject jSONObject = new JSONObject();
                JSONObject jSONObject2 = new JSONObject();
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    for (String str : extras.keySet()) {
                        jSONObject2.put(str, extras.get(str));
                    }
                    if (jSONObject2.length() > 0) {
                        jSONObject.put("extra", jSONObject2);
                        this.d.a(this.e, a.OK, MessageSafeConfigJsonField.UPDATE_STATUS, jSONObject);
                    }
                }
            }
        } catch (JSONException e) {
            c.e("PushSelfShowLog", "onActivityResult error");
        } catch (Exception e2) {
            c.e("PushSelfShowLog", "onActivityResult error");
        }
    }

    public void a(NativeToJsMessageQueue nativeToJsMessageQueue, String str, String str2, JSONObject jSONObject) {
        if (nativeToJsMessageQueue != null) {
            this.d = nativeToJsMessageQueue;
            if ("openApp".equals(str)) {
                d();
                if (str2 == null) {
                    c.a("PushSelfShowLog", "Audio exec callback is null ");
                } else {
                    this.e = str2;
                    a(jSONObject);
                }
            } else if (!"getAppInfo".equals(str)) {
                nativeToJsMessageQueue.a(str2, a.METHOD_NOT_FOUND_EXCEPTION, "error", null);
            } else if (str2 == null) {
                c.a("PushSelfShowLog", "Audio exec callback is null ");
            } else {
                this.e = str2;
                b(jSONObject);
            }
            return;
        }
        c.a("PushSelfShowLog", "jsMessageQueue is null while run into App exec");
    }

    public void a(String str) {
        Intent intent = null;
        if (com.huawei.android.pushselfshow.utils.a.a(this.f, "com.huawei.appmarket", new Intent("com.huawei.appmarket.intent.action.AppDetail")).booleanValue()) {
            c.a("PushSelfShowLog", "app not exist && appmarkt exist ,so open appmarket");
            intent = new Intent("com.huawei.appmarket.intent.action.AppDetail");
            intent.putExtra("APP_PACKAGENAME", str);
            intent.setPackage("com.huawei.appmarket");
            intent.setFlags(402653184);
            c.a("PushSelfShowLog", "hwAppmarket only support com.huawei.appmarket.intent.action.AppDetail!");
        } else if (com.huawei.android.pushselfshow.utils.a.c(this.f).size() > 0) {
            c.a("PushSelfShowLog", "app not exist && other appmarkt exist ,so open appmarket");
            intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse("market://details?id=" + str));
            intent.setFlags(402653184);
        }
        if (intent == null) {
            c.a("PushSelfShowLog", "intent is null ");
            c.e("PushSelfShowLog", "APP_OPEN_APPMARKET and not find any  appmaeket");
            this.d.a(this.e, a.APP_NOT_APPMARKET, "error", null);
            return;
        }
        c.e("PushSelfShowLog", "intent is not null " + intent.toURI());
        this.f.startActivity(intent);
        c.e("PushSelfShowLog", "APP_OPEN_APPMARKET and open with appmaeket");
        this.d.a(this.e, a.APP_OPEN_APPMARKET, "success", null);
    }

    public void b() {
    }

    public void c() {
        d();
    }

    public void d() {
        this.e = null;
        this.a = false;
        this.b = 0;
        this.c = 0;
    }
}
