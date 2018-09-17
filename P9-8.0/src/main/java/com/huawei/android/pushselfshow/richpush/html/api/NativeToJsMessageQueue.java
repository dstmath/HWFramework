package com.huawei.android.pushselfshow.richpush.html.api;

import android.app.Activity;
import android.webkit.WebView;
import com.huawei.android.pushagent.a.a.c;
import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NativeToJsMessageQueue {
    public WebView a;
    private final LinkedList b = new LinkedList();
    private final a c;
    private final Activity d;
    private String e;

    private interface a {
        void onNativeToJsMessageAvailable();
    }

    private class OnlineEventsBridgeMode implements a {
        boolean a = true;
        final Runnable b = new a(this);

        OnlineEventsBridgeMode() {
            c.a("PushSelfShowLog", "OnlineEventsBridgeMode() the webview is " + NativeToJsMessageQueue.this.a);
            NativeToJsMessageQueue.this.a.setNetworkAvailable(true);
        }

        public void onNativeToJsMessageAvailable() {
            NativeToJsMessageQueue.this.d.runOnUiThread(this.b);
        }
    }

    private static class b {
        final String a;
        final d b;

        b(d dVar, String str) {
            this.a = str;
            this.b = dVar;
        }

        JSONObject a() {
            if (this.b == null) {
                return null;
            }
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", this.b.a());
                if (this.b.b() != null) {
                    jSONObject.put("message", this.b.b());
                }
                jSONObject.put("callbackId", this.a);
                return jSONObject;
            } catch (JSONException e) {
                return null;
            }
        }
    }

    public NativeToJsMessageQueue(Activity activity, WebView webView, String str) {
        c.a("PushSelfShowLog", "activity is " + activity);
        c.a("PushSelfShowLog", "webView is " + webView);
        c.a("PushSelfShowLog", "localPath is " + str);
        this.d = activity;
        this.a = webView;
        this.e = str;
        this.c = new OnlineEventsBridgeMode();
        b();
    }

    private boolean d() {
        boolean isEmpty;
        synchronized (this) {
            isEmpty = this.b.isEmpty();
        }
        return isEmpty;
    }

    public String a() {
        return this.e;
    }

    public void a(String str, com.huawei.android.pushselfshow.richpush.html.api.d.a aVar, String str2, JSONObject jSONObject) {
        try {
            c.a("PushSelfShowLog", "addPluginResult status is " + d.c()[aVar.ordinal()]);
            if (str != null) {
                b bVar = new b(jSONObject != null ? new d(str2, aVar, jSONObject) : new d(str2, aVar), str);
                synchronized (this) {
                    this.b.add(bVar);
                    if (this.c != null) {
                        this.c.onNativeToJsMessageAvailable();
                    }
                }
                return;
            }
            c.e("JsMessageQueue", "Got plugin result with no callbackId");
        } catch (Throwable e) {
            c.e("PushSelfShowLog", "addPluginResult failed", e);
        }
    }

    public final void b() {
        synchronized (this) {
            this.b.clear();
        }
    }

    public String c() {
        synchronized (this) {
            if (this.b.isEmpty()) {
                return null;
            }
            JSONArray jSONArray = new JSONArray();
            int size = this.b.size();
            for (int i = 0; i < size; i++) {
                JSONObject a = ((b) this.b.removeFirst()).a();
                if (a != null) {
                    jSONArray.put(a);
                }
            }
            String jSONArray2 = jSONArray.toString();
            return jSONArray2;
        }
    }
}
