package com.huawei.android.pushselfshow.richpush.html.api;

import android.app.Activity;
import android.content.Intent;
import com.huawei.android.pushselfshow.richpush.html.a.a;
import com.huawei.android.pushselfshow.richpush.html.a.d;
import com.huawei.android.pushselfshow.richpush.html.a.e;
import com.huawei.android.pushselfshow.richpush.html.a.g;
import com.huawei.android.pushselfshow.richpush.html.a.h;
import com.huawei.android.pushselfshow.richpush.html.a.i;
import com.huawei.android.pushselfshow.richpush.html.a.j;
import java.util.HashMap;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;

public class c {
    public HashMap a = new HashMap();

    public c(Activity activity, boolean z, String str) {
        try {
            this.a.clear();
            this.a.put("Audio", new e(activity));
            this.a.put("Video", new j(activity));
            this.a.put("App", new d(activity));
            this.a.put("Geo", new i(activity));
            this.a.put("Accelerometer", new a(activity));
            this.a.put("Device", new h(activity, z, str));
        } catch (Throwable e) {
            com.huawei.android.pushagent.a.a.c.d("PluginManager", e.toString(), e);
        }
    }

    public String a(String str, String str2) throws JSONException {
        JSONObject jSONObject = new JSONObject();
        try {
            JSONObject jSONObject2 = new JSONObject(str2);
            if (jSONObject2.has("method")) {
                String string = jSONObject2.getString("method");
                com.huawei.android.pushagent.a.a.c.a("PluginManager", "method is " + string);
                if (jSONObject2.has("options")) {
                    jSONObject = jSONObject2.getJSONObject("options");
                }
                if (!this.a.containsKey(str)) {
                    return d.a(d.a.SERVICE_NOT_FOUND_EXCEPTION).toString();
                }
                com.huawei.android.pushagent.a.a.c.a("PluginManager", "plugins.containsKey(" + str + ") ");
                return ((g) this.a.get(str)).a(string, jSONObject);
            }
            com.huawei.android.pushagent.a.a.c.a("PluginManager", "method is null");
            return d.a(d.a.METHOD_NOT_FOUND_EXCEPTION).toString();
        } catch (JSONException e) {
            return d.a(d.a.JSON_EXCEPTION).toString();
        }
    }

    public void a() {
        for (Entry entry : this.a.entrySet()) {
            g gVar = (g) entry.getValue();
            String str = "PluginManager";
            com.huawei.android.pushagent.a.a.c.e(str, "call plugin: " + ((String) entry.getKey()) + " reset");
            gVar.d();
        }
    }

    public void a(int i, int i2, Intent intent) {
        for (Entry entry : this.a.entrySet()) {
            g gVar = (g) entry.getValue();
            String str = "PluginManager";
            com.huawei.android.pushagent.a.a.c.e(str, "call plugin: " + ((String) entry.getKey()) + " reset");
            gVar.a(i, i2, intent);
        }
    }

    public void a(String str, String str2, NativeToJsMessageQueue nativeToJsMessageQueue) {
        if (nativeToJsMessageQueue != null) {
            String str3 = null;
            JSONObject jSONObject = new JSONObject();
            try {
                JSONObject jSONObject2 = new JSONObject(str2);
                if (jSONObject2.has("callbackId")) {
                    str3 = jSONObject2.getString("callbackId");
                    com.huawei.android.pushagent.a.a.c.a("PluginManager", "callbackId is " + str3);
                }
                if (jSONObject2.has("method")) {
                    String string = jSONObject2.getString("method");
                    com.huawei.android.pushagent.a.a.c.a("PluginManager", "method is " + string);
                    if (jSONObject2.has("options")) {
                        jSONObject = jSONObject2.getJSONObject("options");
                    }
                    if (this.a.containsKey(str)) {
                        com.huawei.android.pushagent.a.a.c.a("PluginManager", "plugins.containsKey(" + str + ") ");
                        ((g) this.a.get(str)).a(nativeToJsMessageQueue, string, str3, jSONObject);
                    } else {
                        nativeToJsMessageQueue.a(str3, d.a.SERVICE_NOT_FOUND_EXCEPTION, "error", null);
                    }
                    return;
                }
                com.huawei.android.pushagent.a.a.c.a("PluginManager", "method is null");
                nativeToJsMessageQueue.a(str3, d.a.METHOD_NOT_FOUND_EXCEPTION, "error", null);
                return;
            } catch (JSONException e) {
                nativeToJsMessageQueue.a(str3, d.a.JSON_EXCEPTION, "error", null);
                return;
            }
        }
        com.huawei.android.pushagent.a.a.c.a("PluginManager", "plugin.exec,jsMessageQueue is null");
    }

    public void b() {
        for (Entry entry : this.a.entrySet()) {
            g gVar = (g) entry.getValue();
            String str = "PluginManager";
            com.huawei.android.pushagent.a.a.c.e(str, "call plugin: " + ((String) entry.getKey()) + " reset");
            gVar.b();
        }
    }

    public void c() {
        for (Entry entry : this.a.entrySet()) {
            g gVar = (g) entry.getValue();
            String str = "PluginManager";
            com.huawei.android.pushagent.a.a.c.e(str, "call plugin: " + ((String) entry.getKey()) + " reset");
            gVar.c();
        }
    }
}
