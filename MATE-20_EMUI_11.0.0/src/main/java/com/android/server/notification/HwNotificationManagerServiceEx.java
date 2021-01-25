package com.android.server.notification;

import android.app.Notification;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

public class HwNotificationManagerServiceEx implements IHwNotificationManagerServiceEx {
    private static final String DIALER_PKGNAME = "com.android.dialer";
    private static final String GMS_PACKAGE = "com.google.android.gms";
    private static final String GMS_SCREEN_LOCK_CMP = "com.google.android.gms/.trustagent";
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final boolean IS_CUST_DIALER_ENABLE = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private static final String NEW_PACKAGE_NAME_DESKCLOCK = "com.huawei.deskclock";
    private static final String NOTIFICATION_CENTER_PKG = "com.huawei.android.pushagent";
    private static final String OLD_PACKAGE_NAME_DESKCLOCK = "com.android.deskclock";
    private static final String PACKAGE_NAME_SYSTEM = "android";
    private static final String SPECIAL_PATTERN = "##.*~~.*##";
    private static final String SPECIAL_PREFIX_SUFFIX_PATTERN = "##";
    private static final int SPECIAL_SPLIT_ARRAY_LENGTH = 2;
    private static final String SPECIAL_SPLIT_PATTERN = "~~";
    private static final String TAG = "HwNotificationManagerServiceEx";

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public String getHwOpPkg(StatusBarNotification sbn) {
        String pkg = sbn.getOpPkg();
        if (NOTIFICATION_CENTER_PKG.equals(pkg)) {
            return sbn.getPackageName();
        }
        return pkg;
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public boolean isPushSpecialRequest(String pkg, String token) {
        return NOTIFICATION_CENTER_PKG.equals(pkg) && (!TextUtils.isEmpty(token) && token.matches(SPECIAL_PATTERN));
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public String getPushSpecialRequestPkg(String pkg, String tag) {
        String[] array = tag.split(SPECIAL_SPLIT_PATTERN);
        if (!isPushTokenInvailid(array)) {
            return array[0].replaceAll(SPECIAL_PREFIX_SUFFIX_PATTERN, "");
        }
        Log.w(TAG, "getPushSpecialRequestPkg is invallid: " + tag);
        return null;
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public String getPushSpecialRequestTag(String tag) {
        String[] array = tag.split(SPECIAL_SPLIT_PATTERN);
        if (isPushTokenInvailid(array)) {
            Log.w(TAG, "getPushSpecialRequestTag is invallid: " + tag);
            return null;
        }
        String tagFromPush = array[1].replaceAll(SPECIAL_PREFIX_SUFFIX_PATTERN, "");
        if (TextUtils.isEmpty(tagFromPush)) {
            return null;
        }
        return tagFromPush;
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public String getPushSpecialRequestChannel(String channelId) {
        String[] array = channelId.split(SPECIAL_SPLIT_PATTERN);
        if (!isPushTokenInvailid(array)) {
            return array[1].replaceAll(SPECIAL_PREFIX_SUFFIX_PATTERN, "");
        }
        Log.w(TAG, "getPushSpecialRequestChannel is invallid: " + channelId);
        return null;
    }

    private boolean isPushTokenInvailid(String[] array) {
        return array == null || array.length != 2 || TextUtils.isEmpty(array[0]) || TextUtils.isEmpty(array[1]);
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public boolean isBanNotification(String pkg, Notification notification) {
        boolean isBanGMSInCN = pkg.equals(GMS_PACKAGE) && IS_CHINA_AREA;
        boolean isIntentForTrustAgent = false;
        long identity = Binder.clearCallingIdentity();
        if (isBanGMSInCN) {
            try {
                if (!(notification.contentIntent == null || notification.contentIntent.getIntent() == null || notification.contentIntent.getIntent().getComponent() == null)) {
                    isIntentForTrustAgent = notification.contentIntent.getIntent().getComponent().flattenToShortString().contains(GMS_SCREEN_LOCK_CMP);
                    Log.w(TAG, "isBanNotification intent = " + notification.contentIntent.getIntent());
                }
            } catch (Exception e) {
                Log.e(TAG, "method isBanNotification has Exception");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
        Binder.restoreCallingIdentity(identity);
        return isBanGMSInCN && isIntentForTrustAgent;
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public void adjustNotificationGroupIfNeeded(Notification notification, int id) {
        if (notification != null && notification.getGroup() == null && notification.getSortKey() == null) {
            if (((notification.flags & 2) != 0) || notification.isForegroundService() || notification.isMediaNotification()) {
                notification.setGroup("ranker_group" + id);
            }
        }
    }

    @Override // com.android.server.notification.IHwNotificationManagerServiceEx
    public boolean isSendNotificationDisable(int callingUid, String pkg, Notification notification) {
        return !(isUidSystemOrPhone(callingUid) || "android".equals(pkg) || isCustDialer(pkg)) && !isAlarmNotification(pkg, notification) && HwDeviceManager.disallowOp(33);
    }

    private boolean isUidSystemOrPhone(int uid) {
        int appid = UserHandle.getAppId(uid);
        return appid == 1000 || appid == 1001 || uid == 0;
    }

    private boolean isCustDialer(String packageName) {
        return IS_CUST_DIALER_ENABLE && DIALER_PKGNAME.equals(packageName);
    }

    private boolean isAlarmNotification(String pkg, Notification notification) {
        if (notification == null) {
            return false;
        }
        if ((NEW_PACKAGE_NAME_DESKCLOCK.equals(pkg) || OLD_PACKAGE_NAME_DESKCLOCK.equals(pkg)) && "alarm".equals(notification.category)) {
            return true;
        }
        return false;
    }
}
