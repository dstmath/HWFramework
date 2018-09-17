package com.huawei.android.pushselfshow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushReceiver.KEY_TYPE;
import com.huawei.android.pushagent.a.a.a.d;
import com.huawei.android.pushagent.a.a.c;

public class NotificationReceiver extends BroadcastReceiver {
    private void a(Context context, Intent intent) {
        if (context != null && intent != null) {
            String stringExtra = intent.getStringExtra(KEY_TYPE.PKGNAME);
            c.b("PushSelfShowLog", "ACTION_CLEAR_GROUP_NUM, pkg " + stringExtra);
            Object stringExtra2 = intent.getStringExtra("auth");
            if (TextUtils.isEmpty(stringExtra) || TextUtils.isEmpty(stringExtra2)) {
                c.d("PushSelfShowLog", "pkgName is null");
                return;
            }
            CharSequence b = d.b(context, stringExtra2);
            if (TextUtils.isEmpty(b)) {
                c.b("PushSelfShowLog", "pkg is empty");
            } else if (stringExtra.equals(b)) {
                com.huawei.android.pushselfshow.d.d.b(stringExtra);
            } else {
                c.b("PushSelfShowLog", "verify failed!");
            }
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            try {
                c.a(context);
                if ("com.huawei.intent.action.CLEAR_GROUP_NUM".equals(intent.getAction())) {
                    a(context, intent);
                }
            } catch (Exception e) {
                c.d("PushSelfShowLog", e.toString());
            }
        }
    }
}
