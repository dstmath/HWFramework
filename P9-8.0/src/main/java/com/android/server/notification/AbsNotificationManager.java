package com.android.server.notification;

import android.app.Notification;
import android.content.Context;
import android.os.Parcel;
import android.os.UserHandle;
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

    protected int getNCTargetAppUid(String opPkg, String pkg, int defaultUid, Notification notification) {
        return defaultUid;
    }

    protected String getNCTargetAppPkg(String opPkg, String defaultPkg, Notification notification) {
        return defaultPkg;
    }

    protected boolean isHwSoundAllow(String pkg, String channelId, int userId) {
        return true;
    }

    protected boolean isHwVibrateAllow(String pkg, String channelId, int userId) {
        return true;
    }

    protected void addHwExtraForNotification(Notification notification, String pkg, int pid) {
    }

    protected boolean isBlockRideModeNotification(String pkg) {
        return false;
    }

    protected void reportToIAware(String pkg, int uid, int nid, boolean added) {
    }

    protected boolean isFromPinNotification(Notification notification, String pkg) {
        return false;
    }

    protected boolean isGameRunningForeground() {
        return false;
    }

    protected boolean isGameDndSwitchOn() {
        return false;
    }

    protected void recognize(String tag, int id, Notification notification, UserHandle user, String pkg, int uid, int pid) {
    }

    protected void bindRecSys() {
    }

    protected boolean isNotificationDisable() {
        return false;
    }
}
