package tmsdkobf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class pm {
    public static PendingIntent a(Context context, String str, long j) {
        d.d("AlarmerUtil", "\u6dfb\u52a0\u95f9\u949f : " + (j / 1000) + "s");
        Intent intent = new Intent(str);
        intent.setPackage(TMSDKContext.getApplicaionContext().getPackageName());
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, 0);
        ((AlarmManager) context.getSystemService("alarm")).set(0, System.currentTimeMillis() + j, broadcast);
        return broadcast;
    }

    public static void f(Context context, String str) {
        d.d("AlarmerUtil", "\u5220\u9664\u95f9\u949f : " + str);
        ((AlarmManager) context.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(context, 0, new Intent(str), 0));
    }
}
