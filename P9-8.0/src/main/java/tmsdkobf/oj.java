package tmsdkobf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import tmsdk.common.TMSDKContext;

public class oj {
    public static PendingIntent a(Context context, String str, long j) {
        mb.n("AlarmerUtil", "添加闹钟 : " + str + " " + (j / 1000) + "s");
        PendingIntent pendingIntent = null;
        try {
            Intent intent = new Intent(str);
            intent.setPackage(TMSDKContext.getApplicaionContext().getPackageName());
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            ((AlarmManager) context.getSystemService("alarm")).set(0, System.currentTimeMillis() + j, pendingIntent);
            return pendingIntent;
        } catch (Exception e) {
            mb.o("AlarmerUtil", "addAlarm: " + e);
            return pendingIntent;
        }
    }

    public static void h(Context context, String str) {
        mb.n("AlarmerUtil", "删除闹钟 : " + str);
        try {
            Intent intent = new Intent(str);
            intent.setPackage(TMSDKContext.getApplicaionContext().getPackageName());
            ((AlarmManager) context.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(context, 0, intent, 0));
        } catch (Exception e) {
            mb.o("AlarmerUtil", "delAlarm exception: " + e);
        }
    }
}
