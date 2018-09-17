package com.android.server.devicepolicy;

import android.annotation.IntDef;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class RemoteBugreportUtils {
    static final String BUGREPORT_MIMETYPE = "application/vnd.android.bugreport";
    static final String CTL_STOP = "ctl.stop";
    static final int NOTIFICATION_ID = 678432343;
    static final String REMOTE_BUGREPORT_SERVICE = "bugreportremote";
    static final long REMOTE_BUGREPORT_TIMEOUT_MILLIS = 600000;

    @IntDef({1, 2, 3})
    @Retention(RetentionPolicy.SOURCE)
    @interface RemoteBugreportNotificationType {
    }

    RemoteBugreportUtils() {
    }

    static Notification buildNotification(Context context, int type) {
        Intent dialogIntent = new Intent("android.settings.SHOW_REMOTE_BUGREPORT_DIALOG");
        dialogIntent.addFlags(268468224);
        dialogIntent.putExtra("android.app.extra.bugreport_notification_type", type);
        Builder builder = new Builder(context).setSmallIcon(17303219).setOngoing(true).setLocalOnly(true).setPriority(1).setContentIntent(PendingIntent.getActivityAsUser(context, type, dialogIntent, 0, null, UserHandle.CURRENT)).setColor(context.getColor(17170519));
        if (type == 2) {
            builder.setContentTitle(context.getString(17040394)).setProgress(0, 0, true);
        } else if (type == 1) {
            builder.setContentTitle(context.getString(17040392)).setProgress(0, 0, true);
        } else if (type == 3) {
            PendingIntent pendingIntentAccept = PendingIntent.getBroadcast(context, NOTIFICATION_ID, new Intent("com.android.server.action.BUGREPORT_SHARING_ACCEPTED"), 268435456);
            builder.addAction(new Action.Builder(null, context.getString(17040397), PendingIntent.getBroadcast(context, NOTIFICATION_ID, new Intent("com.android.server.action.BUGREPORT_SHARING_DECLINED"), 268435456)).build()).addAction(new Action.Builder(null, context.getString(17040396), pendingIntentAccept).build()).setContentTitle(context.getString(17040393)).setContentText(context.getString(17040395)).setStyle(new BigTextStyle().bigText(context.getString(17040395)));
        }
        return builder.build();
    }
}
