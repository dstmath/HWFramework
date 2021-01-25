package com.android.server.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.Ringtone;
import android.os.Handler;
import android.os.Parcel;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import com.android.server.SystemService;
import java.util.HashMap;

public abstract class AbsNotificationManager extends SystemService {
    protected static final int REASON_USER_FORCED = 101;

    public AbsNotificationManager(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void hwEnqueueNotificationWithTag(String pkg, int uid, NotificationRecord nr) {
    }

    /* access modifiers changed from: protected */
    public void detectNotifyBySM(int uid, String pkg, Notification n) {
    }

    /* access modifiers changed from: protected */
    public boolean inNonDisturbMode(String pkg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public int modifyScoreBySM(String pkg, int callingUid, int origScore) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void hwCancelNotification(String pkg, String tag, int id, int userId) {
    }

    /* access modifiers changed from: protected */
    public boolean isImportantNotification(String pkg, Notification notification) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isMmsNotificationEnable(String pkg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleGetNotifications(Parcel data, Parcel reply) {
    }

    /* access modifiers changed from: protected */
    public void updateLight(boolean enable, int ledOnMS, int ledOffMS) {
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitchEvents(int userId) {
    }

    /* access modifiers changed from: protected */
    public void stopPlaySound() {
    }

    /* access modifiers changed from: protected */
    public boolean isAFWUserId(int userId) {
        return false;
    }

    /* access modifiers changed from: protected */
    public String getNCTargetAppPkg(String opPkg, String defaultPkg, Notification notification) {
        return defaultPkg;
    }

    /* access modifiers changed from: protected */
    public boolean isHwSoundAllow(String pkg, String channelId, int userId) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isHwVibrateAllow(String pkg, String channelId, int userId) {
        return true;
    }

    /* access modifiers changed from: protected */
    public String getHwVibratorType(Ringtone ringtone, String pkg, String channelId, int userId) {
        return "";
    }

    /* access modifiers changed from: protected */
    public void playHwVibrate(NotificationRecord record, String type, boolean delayVibForSound) {
    }

    /* access modifiers changed from: protected */
    public void addHwExtraForNotification(Notification notification, String pkg, int pid) {
    }

    /* access modifiers changed from: protected */
    public boolean isBlockRideModeNotification(String pkg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isNotInTvWhiteListNotification(String pkg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void reportToIAware(String pkg, int uid, int nid, boolean added) {
    }

    /* access modifiers changed from: protected */
    public boolean isFromPinNotification(Notification notification, String pkg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isGameRunningForeground() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isGameDndSwitchOn() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isPackageRequestNarrowNotification() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void recognize(String tag, int id, Notification notification, UserHandle user, String pkg, int uid, int pid) {
    }

    /* access modifiers changed from: protected */
    public void bindRecSys() {
    }

    /* access modifiers changed from: protected */
    public boolean isNotificationDisable() {
        return false;
    }

    public boolean isAllowToShow(String pkg, ActivityInfo topActivity) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean doForUpdateNotification(String key, Handler handler) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void removeNotificationInUpdateQueue(String key) {
    }

    /* access modifiers changed from: protected */
    public String getPackageNameByPid(int pid) {
        return "";
    }

    /* access modifiers changed from: protected */
    public void addNotificationFlag(StatusBarNotification n) {
    }

    /* access modifiers changed from: protected */
    public boolean isNoitficationWhiteApp(String pkg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public NotificationChannel getHwNotificationChannel(NotificationChannel channel, String pkg, int uid) {
        return channel;
    }

    /* access modifiers changed from: protected */
    public void checkCallerIsSystemOrSystemApp() {
    }

    /* access modifiers changed from: protected */
    public String readDefaultApprovedFromWhiteList(String defaultApproved) {
        return defaultApproved;
    }

    /* access modifiers changed from: protected */
    public boolean isNeedForbidAppNotification(String pkgName, String className, HashMap<String, String> hashMap) {
        return false;
    }
}
