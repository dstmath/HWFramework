package defpackage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.PushService;

/* renamed from: c */
public class c extends BroadcastReceiver {
    final /* synthetic */ PushService k;

    private c(PushService pushService) {
        this.k = pushService;
    }

    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            aw.i(PushService.TAG, "action is " + action);
            if ("com.huawei.intent.action.PUSH".equals(action) && "delay_start_push".equals(intent.getStringExtra("EXTRA_INTENT_TYPE"))) {
                this.k.a();
                PushService.i = false;
            } else if (PushService.i) {
                aw.i(PushService.TAG, "delay to start push, wait 30 seconds");
            } else if (!"android".equals(context.getPackageName()) || PushService.a(context, action)) {
                if ("android".equals(context.getPackageName()) && PushService.a(context)) {
                    aw.d(PushService.TAG, "enter checkBackUp()");
                    PushService.e();
                    au.O(context);
                }
                PushService.a(intent);
            } else {
                aw.d(PushService.TAG, "frameworkPush not provide service");
            }
        } catch (Throwable e) {
            aw.d(PushService.TAG, "call PushInnerReceiver:onReceive cause " + e.toString(), e);
        }
    }
}
