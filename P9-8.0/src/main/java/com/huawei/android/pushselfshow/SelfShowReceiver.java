package com.huawei.android.pushselfshow;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushReceiver.ACTION;
import com.huawei.android.pushagent.PushReceiver.KEY_TYPE;
import com.huawei.android.pushagent.a.a.a.d;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushagent.a.a.e;
import com.huawei.android.pushselfshow.d.g;
import com.huawei.android.pushselfshow.permission.RequestPermissionsActivity;
import com.huawei.android.pushselfshow.utils.b.b;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.json.JSONArray;

public class SelfShowReceiver {

    static class a extends Thread {
        Context a;
        String b;

        public a(Context context, String str) {
            this.a = context;
            this.b = str;
        }

        public void run() {
            ArrayList a = com.huawei.android.pushselfshow.utils.a.a.a(this.a, this.b);
            int size = a.size();
            c.e("PushSelfShowLog", "receive package add ,arrSize " + size);
            for (int i = 0; i < size; i++) {
                com.huawei.android.pushselfshow.c.a aVar = new com.huawei.android.pushselfshow.c.a();
                aVar.g((String) a.get(i));
                aVar.b("app");
                aVar.a(this.b);
                com.huawei.android.pushselfshow.utils.a.a(this.a, "16", aVar, -1);
            }
            if (size > 0) {
                com.huawei.android.pushselfshow.utils.a.a.b(this.a, this.b);
            }
            com.huawei.android.pushselfshow.utils.a.b(new File(b.a(this.a)));
        }
    }

    private boolean a(Context context, Intent intent) {
        if (com.huawei.android.pushagent.a.a.a.a() < 12) {
            return true;
        }
        String str = "";
        if ("com.huawei.android.pushagent".equals(context.getPackageName())) {
            str = com.huawei.android.pushselfshow.utils.a.e();
        } else {
            str = !"com.huawei.hwid".equals(context.getPackageName()) ? d.b(context, new e(context, "push_client_self_info").b("push_notify_key")) : com.huawei.android.pushselfshow.utils.a.f();
        }
        Object stringExtra = intent.getStringExtra("extra_encrypt_data");
        if (!TextUtils.isEmpty(stringExtra)) {
            try {
                if (context.getPackageName().equals(d.b(context, stringExtra, str.getBytes("UTF-8")))) {
                    c.a("PushSelfShowLog", "parse msg success!");
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                c.d("PushSelfShowLog", e.toString());
            }
        }
        return false;
    }

    public void a(Context context, Intent intent, com.huawei.android.pushselfshow.c.a aVar) {
        c.a("PushSelfShowLog", "receive a selfshow message ,the type is" + aVar.m());
        if (com.huawei.android.pushselfshow.b.a.a(aVar.m())) {
            long b = com.huawei.android.pushselfshow.utils.a.b(aVar.j());
            if (b == 0) {
                new g(context, aVar).start();
            } else {
                c.a("PushSelfShowLog", "waiting ……");
                intent.setPackage(context.getPackageName());
                com.huawei.android.pushselfshow.utils.a.a(context, intent, b);
            }
            return;
        }
        com.huawei.android.pushselfshow.utils.a.a(context, "3", aVar, -1);
    }

    public void a(Context context, Intent intent, String str, com.huawei.android.pushselfshow.c.a aVar, int i) {
        c.a("PushSelfShowLog", "receive a selfshow userhandle message");
        if ("-1".equals(str)) {
            com.huawei.android.pushselfshow.utils.a.a(context, i);
        } else {
            com.huawei.android.pushselfshow.utils.a.b(context, intent);
        }
        if ("1".equals(str)) {
            new com.huawei.android.pushselfshow.b.a(context, aVar).a();
            if (aVar.l() != null) {
                try {
                    JSONArray jSONArray = new JSONArray(aVar.l());
                    Intent intent2 = new Intent(ACTION.ACTION_NOTIFICATION_MSG_CLICK);
                    intent2.putExtra(KEY_TYPE.PUSH_KEY_CLICK, jSONArray.toString()).setPackage(aVar.k()).setFlags(32);
                    context.sendBroadcast(intent2);
                } catch (Exception e) {
                    c.d("PushSelfShowLog", "message.extras is not a json format,err info " + e.toString());
                }
            }
        }
        if (!TextUtils.isEmpty(aVar.e())) {
            String str2 = aVar.k() + aVar.e();
            c.a("PushSelfShowLog", "groupMap key is " + str2);
            com.huawei.android.pushselfshow.d.d.a(str2);
        }
        com.huawei.android.pushselfshow.utils.a.a(context, str, aVar, i);
    }

    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            try {
                c.a("PushSelfShowLog", "enter SelfShowReceiver receiver, context or intent is null");
            } catch (Throwable e) {
                c.a("PushSelfShowLog", e.toString(), e);
            }
        } else {
            c.a(context);
            String action = intent.getAction();
            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                Uri data = intent.getData();
                if (data != null) {
                    Object schemeSpecificPart = data.getSchemeSpecificPart();
                    c.e("PushSelfShowLog", "receive package add ,the pkgName is " + schemeSpecificPart);
                    if (!TextUtils.isEmpty(schemeSpecificPart)) {
                        new a(context, schemeSpecificPart).start();
                    }
                }
            } else if ("com.huawei.intent.action.PUSH".equals(action)) {
                if (!"com.huawei.android.pushagent".equals(context.getPackageName()) && RequestPermissionsActivity.a(context)) {
                    c.b("PushSelfShowLog", "needStartPermissionActivity");
                    RequestPermissionsActivity.a(context, intent);
                    return;
                }
                String str = null;
                if (intent.hasExtra("selfshow_info")) {
                    byte[] byteArrayExtra = intent.getByteArrayExtra("selfshow_info");
                    if (intent.hasExtra("selfshow_token")) {
                        byte[] byteArrayExtra2 = intent.getByteArrayExtra("selfshow_token");
                        if (intent.hasExtra("selfshow_event_id")) {
                            str = intent.getStringExtra("selfshow_event_id");
                        }
                        int i = 0;
                        if (intent.hasExtra("selfshow_notify_id")) {
                            i = intent.getIntExtra("selfshow_notify_id", 0);
                            c.b("PushSelfShowLog", "get notifyId:" + i);
                        }
                        if (a(context, intent)) {
                            com.huawei.android.pushselfshow.c.a aVar = new com.huawei.android.pushselfshow.c.a(byteArrayExtra, byteArrayExtra2);
                            if (aVar.b()) {
                                c.a("PushSelfShowLog", " onReceive the msg id = " + aVar.a() + ",and cmd is" + aVar.m() + ",and the eventId is " + str);
                                if (str != null) {
                                    a(context, intent, str, aVar, i);
                                } else {
                                    a(context, intent, aVar);
                                }
                                com.huawei.android.pushselfshow.utils.a.b(new File(b.a(context)));
                            } else {
                                c.a("PushSelfShowLog", "parseMessage failed");
                                return;
                            }
                        }
                        c.b("PushSelfShowLog", "msg is invalid!");
                    }
                }
            }
        }
    }
}
