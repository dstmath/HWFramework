package com.android.server.notification;

import android.app.Notification;
import android.content.Context;
import android.os.Parcel;
import com.android.server.SystemService;

public abstract class AbsNotificationManager extends SystemService {
    public AbsNotificationManager(Context context) {
        super(context);
    }

    protected void hwEnqueueNotificationWithTag(String pkg, int uid, NotificationRecord nr) {
    }

    protected void detectNotifyBySM(int uid, String pkg, Notification n) {
    }

    protected boolean inNonDisturbMode(String pkg) {
        return false;
    }

    protected int modifyScoreBySM(String pkg, int callingUid, int origScore) {
        return 0;
    }

    protected void hwCancelNotification(String pkg, String tag, int id, int userId) {
    }

    protected boolean isImportantNotification(String pkg, Notification notification) {
        return false;
    }

    protected boolean isMmsNotificationEnable(String pkg) {
        return false;
    }

    protected void handleGetNotifications(Parcel data, Parcel reply) {
    }

    protected void updateLight(boolean enable, int ledOnMS, int ledOffMS) {
    }

    protected void handleUserSwitchEvents(int userId) {
    }

    protected void stopPlaySound() {
    }

    protected boolean isAFWUserId(int userId) {
        return false;
    }

    protected boolean isClonedAppDeleted(int reason, String tag) {
        return false;
    }

    protected String convertNotificationTag(String tag, int pid) {
        return tag;
    }

    protected void handleNotificationForClone(Notification notification, int pid) {
    }

    protected int getNCTargetAppUid(String opPkg, String pkg, int defaultUid, Notification notification) {
        return defaultUid;
    }

    protected String getNCTargetAppPkg(String opPkg, String defaultPkg, Notification notification) {
        return defaultPkg;
    }

    protected boolean isHwSoundAllow(String pkg, int userId) {
        return true;
    }

    protected boolean isHwVibrateAllow(String pkg, int userId) {
        return true;
    }

    protected void addHwExtraForNotification(Notification notification, String pkg, int pid) {
    }
}
