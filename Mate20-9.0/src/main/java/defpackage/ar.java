package defpackage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.huawei.android.pushagent.PushService;

/* renamed from: ar  reason: default package */
public final class ar extends BroadcastReceiver {
    final /* synthetic */ PushService ag;

    private ar(PushService pushService) {
        this.ag = pushService;
    }

    public /* synthetic */ ar(PushService pushService, byte b) {
        this(pushService);
    }

    public final void onReceive(Context context, Intent intent) {
        long versionCode;
        if (context == null || intent == null) {
            Log.e("PushLogSys", "PushInnerReceiver context is null or intent is null");
            return;
        }
        try {
            intent.getStringExtra("TestIntent");
            String action = intent.getAction();
            Log.i("PushLogSys", "sys push inner receiver get action is " + action);
            if ("com.huawei.android.push.intent.CHECK_HWPUSH_VERSION".equals(action) && context.getPackageName().equals(intent.getStringExtra("Remote_Package_Name"))) {
                Object b = new bf(context).b("latestVersion", -1L);
                long intValue = b instanceof Integer ? (long) ((Integer) b).intValue() : b instanceof Long ? ((Long) b).longValue() : -1;
                synchronized (PushService.Z) {
                    versionCode = ax.b(context, this.ag.W).getVersionCode();
                }
                Log.i("PushLogSys", "check pushcore version trs version is " + intValue + ". localVersion is " + versionCode);
                if (versionCode <= intValue) {
                    aq b2 = this.ag.c();
                    if (aq.LOCAL_VERSION != b2) {
                        Log.i("PushLogSys", "TRS update pushcore version , need install push again");
                        this.ag.a(b2);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("PushLogSys", "intent has some error");
        }
    }
}
