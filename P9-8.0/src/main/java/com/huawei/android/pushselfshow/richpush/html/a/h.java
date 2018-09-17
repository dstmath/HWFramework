package com.huawei.android.pushselfshow.richpush.html.a;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.huawei.android.pushagent.a.a.a.f;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushagent.a.a.d;
import com.huawei.android.pushselfshow.richpush.html.api.NativeToJsMessageQueue;
import com.huawei.android.pushselfshow.utils.a;
import org.json.JSONObject;

public class h implements g {
    private Activity a;
    private NativeToJsMessageQueue b;
    private String c;
    private boolean d = false;
    private String e = "";

    public h(Activity activity, boolean z, String str) {
        c.e("PushSelfShowLog", "init App");
        this.a = activity;
        this.d = z;
        this.e = str;
    }

    private String e() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("manufacturer", Build.MANUFACTURER);
            jSONObject.put("model", Build.MODEL);
            jSONObject.put("version", Build.DISPLAY);
            jSONObject.put("os", "Android");
            jSONObject.put("osVersion", VERSION.RELEASE);
            jSONObject.put("uuid", a());
            jSONObject.put("sdkVersion", "2907");
            if (this.d) {
                jSONObject.put("imei", a.a(this.e));
            }
        } catch (Throwable e) {
            c.e("PushSelfShowLog", "onError error", e);
        }
        return jSONObject.toString();
    }

    public String a() {
        try {
            String a = d.a(this.a, "push_client_self_info", "token_info");
            if (TextUtils.isEmpty(a)) {
                a = this.e;
            }
            return f.a(a);
        } catch (Exception e) {
            c.d("PushSelfShowLog", e.toString());
            return "";
        }
    }

    public String a(String str, JSONObject jSONObject) {
        return !"getDeviceInfo".equals(str) ? com.huawei.android.pushselfshow.richpush.html.api.d.a(com.huawei.android.pushselfshow.richpush.html.api.d.a.ERROR).toString() : e();
    }

    public void a(int i, int i2, Intent intent) {
    }

    public void a(NativeToJsMessageQueue nativeToJsMessageQueue, String str, String str2, JSONObject jSONObject) {
        if (nativeToJsMessageQueue != null) {
            this.b = nativeToJsMessageQueue;
            if (str2 == null) {
                c.a("PushSelfShowLog", "get DeviceInfo exec callback is null ");
            } else {
                this.c = str2;
            }
            this.b.a(this.c, com.huawei.android.pushselfshow.richpush.html.api.d.a.METHOD_NOT_FOUND_EXCEPTION, "error", null);
            return;
        }
        c.a("PushSelfShowLog", "jsMessageQueue is null while run into App exec");
    }

    public void b() {
    }

    public void c() {
    }

    public void d() {
    }
}
