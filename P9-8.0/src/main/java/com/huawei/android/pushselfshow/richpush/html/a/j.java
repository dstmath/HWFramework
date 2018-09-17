package com.huawei.android.pushselfshow.richpush.html.a;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.richpush.html.api.NativeToJsMessageQueue;
import com.huawei.android.pushselfshow.richpush.html.api.b;
import com.huawei.android.pushselfshow.richpush.html.api.d.a;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import org.json.JSONException;
import org.json.JSONObject;

public class j implements g {
    private NativeToJsMessageQueue a;
    private String b;
    private Context c;
    private String d = null;

    public j(Context context) {
        c.e("PushSelfShowLog", "init VideoPlayer");
        this.c = context;
    }

    private void a(JSONObject jSONObject) {
        if (this.a != null) {
            if (jSONObject != null && jSONObject.has(CheckVersionField.CHECK_VERSION_SERVER_URL)) {
                try {
                    String string = jSONObject.getString(CheckVersionField.CHECK_VERSION_SERVER_URL);
                    String a = b.a(this.a.a(), string);
                    if (a != null) {
                        if (a.length() > 0) {
                            this.d = a;
                            String str = "video/*";
                            if (jSONObject.has("mime-type")) {
                                try {
                                    String string2 = jSONObject.getString("mime-type");
                                    c.e("PushSelfShowLog", "the custom mimetype is " + string2);
                                    if (string2.startsWith("video/")) {
                                        str = string2;
                                    }
                                } catch (JSONException e) {
                                    c.e("PushSelfShowLog", "get mime-type error");
                                } catch (Exception e2) {
                                    c.e("PushSelfShowLog", "get mime-type error");
                                }
                            }
                            Intent intent = new Intent("android.intent.action.VIEW");
                            intent.setDataAndType(Uri.parse(this.d), str);
                            if (jSONObject.has("package-name")) {
                                try {
                                    String string3 = jSONObject.getString("package-name");
                                    c.e("PushSelfShowLog", "the custom packageName is " + string3);
                                    if (b.a(this.c, intent).contains(string3)) {
                                        intent.setPackage(string3);
                                    }
                                } catch (JSONException e3) {
                                    c.e("PushSelfShowLog", "get packageName error");
                                }
                            }
                            this.c.startActivity(intent);
                            this.a.a(this.b, a.OK, "success", null);
                        }
                    }
                    c.e("PushSelfShowLog", string + "File not exist");
                    this.a.a(this.b, a.AUDIO_ONLY_SUPPORT_HTTP, "error", null);
                } catch (Throwable e4) {
                    c.e("PushSelfShowLog", "startPlaying failed ", e4);
                    this.a.a(this.b, a.JSON_EXCEPTION, "error", null);
                } catch (Throwable e42) {
                    c.e("PushSelfShowLog", "startPlaying failed ", e42);
                    this.a.a(this.b, a.JSON_EXCEPTION, "error", null);
                }
            } else {
                this.a.a(this.b, a.JSON_EXCEPTION, "error", null);
            }
            return;
        }
        c.a("PushSelfShowLog", "jsMessageQueue is null while run into Video Player exec");
    }

    public String a(String str, JSONObject jSONObject) {
        return null;
    }

    public void a(int i, int i2, Intent intent) {
    }

    public void a(NativeToJsMessageQueue nativeToJsMessageQueue, String str, String str2, JSONObject jSONObject) {
        if (nativeToJsMessageQueue != null) {
            this.a = nativeToJsMessageQueue;
            if ("playVideo".equals(str)) {
                d();
                if (str2 == null) {
                    c.a("PushSelfShowLog", "Audio exec callback is null ");
                } else {
                    this.b = str2;
                    a(jSONObject);
                }
            } else {
                nativeToJsMessageQueue.a(str2, a.METHOD_NOT_FOUND_EXCEPTION, "error", null);
            }
            return;
        }
        c.a("PushSelfShowLog", "jsMessageQueue is null while run into Video Player exec");
    }

    public void b() {
    }

    public void c() {
        d();
    }

    public void d() {
        this.b = null;
        this.d = null;
    }
}
