package defpackage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.huawei.android.feature.BuildConfig;
import com.huawei.android.pushagent.PushService;

/* renamed from: as  reason: default package */
public final class as extends BroadcastReceiver {
    final /* synthetic */ PushService ag;

    private as(PushService pushService) {
        this.ag = pushService;
    }

    public /* synthetic */ as(PushService pushService, byte b) {
        this(pushService);
    }

    public final void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Log.e("PushLogSys", "PushInnerReceiver context is null or intent is null");
            return;
        }
        try {
            intent.getStringExtra("TestIntent");
            String action = intent.getAction();
            Uri data = intent.getData();
            String str = BuildConfig.FLAVOR;
            if (data != null) {
                str = data.getSchemeSpecificPart();
            }
            Log.i("PushLogSys", "sys push system receiver get action is " + action + ". pkgName is " + str);
            if (!"android.intent.action.PACKAGE_ADDED".equals(action)) {
                return;
            }
            if ("com.huawei.hwid".equals(str) || "com.huawei.android.pushagent".equals(str)) {
                aq b = this.ag.c();
                if (aq.LOCAL_VERSION != b) {
                    Log.i("PushLogSys", "HMS or NC update pushcore version, need install push again");
                    this.ag.a(b);
                }
            }
        } catch (Exception e) {
            Log.e("PushLogSys", "intent has some error");
        }
    }
}
