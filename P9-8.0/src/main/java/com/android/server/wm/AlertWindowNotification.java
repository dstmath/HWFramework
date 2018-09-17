package com.android.server.wm;

import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Slog;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.policy.IconUtilities;
import com.android.server.wm.-$Lambda$KU7DZdSRHCLy3_sRshBk0_qT5v0.AnonymousClass1;

class AlertWindowNotification {
    private static final String CHANNEL_PREFIX = "com.android.server.wm.AlertWindowNotification - ";
    private static final int NOTIFICATION_ID = 0;
    private static final String TAG = "AlertWindowNotification";
    private static NotificationChannelGroup sChannelGroup;
    private static int sNextRequestCode = 0;
    private IconUtilities mIconUtilities;
    private final NotificationManager mNotificationManager = ((NotificationManager) this.mService.mContext.getSystemService("notification"));
    private String mNotificationTag = (CHANNEL_PREFIX + this.mPackageName);
    private final String mPackageName;
    private boolean mPosted;
    private final int mRequestCode;
    private final WindowManagerService mService;

    AlertWindowNotification(WindowManagerService service, String packageName) {
        this.mService = service;
        this.mPackageName = packageName;
        int i = sNextRequestCode;
        sNextRequestCode = i + 1;
        this.mRequestCode = i;
        this.mIconUtilities = new IconUtilities(this.mService.mContext);
    }

    void post() {
        this.mService.mH.post(new AnonymousClass1(this));
    }

    void cancel() {
        this.mService.mH.post(new -$Lambda$KU7DZdSRHCLy3_sRshBk0_qT5v0(this));
    }

    private void onCancelNotification() {
        if (this.mPosted) {
            this.mPosted = false;
            this.mNotificationManager.cancel(this.mNotificationTag, 0);
        }
    }

    private void onPostNotification() {
        if (!this.mPosted) {
            this.mPosted = true;
            Context context = this.mService.mContext;
            PackageManager pm = context.getPackageManager();
            ApplicationInfo aInfo = getApplicationInfo(pm, this.mPackageName);
            createNotificationChannel(context, aInfo != null ? pm.getApplicationLabel(aInfo).toString() : this.mPackageName);
            String message = context.getString(17039565, new Object[]{appName});
            Builder builder = new Builder(context, this.mNotificationTag).setOngoing(true).setContentTitle(context.getString(17039566, new Object[]{appName})).setContentText(message).setSmallIcon(17301708).setColor(context.getColor(17170769)).setStyle(new BigTextStyle().bigText(message)).setLocalOnly(true).setContentIntent(getContentIntent(context, this.mPackageName));
            if (aInfo != null) {
                Drawable drawable = pm.getApplicationIcon(aInfo);
                if (drawable != null) {
                    builder.setLargeIcon(this.mIconUtilities.createIconBitmap(drawable));
                }
            }
            this.mNotificationManager.notify(this.mNotificationTag, 0, builder.build());
        }
    }

    private PendingIntent getContentIntent(Context context, String packageName) {
        Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.fromParts(HwBroadcastRadarUtil.KEY_PACKAGE, packageName, null));
        intent.setFlags(268468224);
        return PendingIntent.getActivity(context, this.mRequestCode, intent, 268435456);
    }

    private void createNotificationChannel(Context context, String appName) {
        if (sChannelGroup == null) {
            sChannelGroup = new NotificationChannelGroup(CHANNEL_PREFIX, this.mService.mContext.getString(17039563));
            this.mNotificationManager.createNotificationChannelGroup(sChannelGroup);
        }
        String nameChannel = context.getString(17039564, new Object[]{appName});
        Slog.w(TAG, "createNotificationChannel for appName:" + appName + ", PackageName is:" + this.mPackageName + ",nameChannel is:" + nameChannel);
        NotificationChannel channel = new NotificationChannel(this.mNotificationTag, nameChannel, 1);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setBlockableSystem(true);
        channel.setGroup(sChannelGroup.getId());
        this.mNotificationManager.createNotificationChannel(channel);
    }

    private ApplicationInfo getApplicationInfo(PackageManager pm, String packageName) {
        try {
            return pm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }
}
