package com.huawei.systemmanager.notificationmanager;

import android.app.ActivityManager;
import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import java.util.List;

public class HwNotificationManagerImpl implements IHwNotificationManager {
    private static volatile IHwNotificationManager mInstance = null;
    private INotificationManager mImpl;

    public static synchronized IHwNotificationManager getInstance() {
        synchronized (HwNotificationManagerImpl.class) {
            HwNotificationManagerImpl tmp = new HwNotificationManagerImpl();
            if (tmp.mImpl == null) {
                return null;
            }
            return tmp;
        }
    }

    private HwNotificationManagerImpl() {
        this.mImpl = null;
        this.mImpl = NotificationManager.getService();
    }

    public boolean canShowBadge(String pkg, int uid) throws RemoteException {
        if (this.mImpl != null) {
            return this.mImpl.canShowBadge(pkg, uid);
        }
        return false;
    }

    public boolean areNotificationsEnabledForPackage(String pkg, int uid) throws RemoteException {
        if (this.mImpl != null) {
            return this.mImpl.areNotificationsEnabledForPackage(pkg, uid);
        }
        return false;
    }

    public void setShowBadge(String pkg, int uid, boolean showBadge) throws RemoteException {
        if (this.mImpl != null) {
            this.mImpl.setShowBadge(pkg, uid, showBadge);
        }
    }

    public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) throws RemoteException {
        if (this.mImpl != null) {
            this.mImpl.setNotificationsEnabledForPackage(pkg, uid, enabled);
        }
    }

    public List<NotificationChannel> getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) throws RemoteException {
        if (this.mImpl == null) {
            return null;
        }
        ParceledListSlice parceledListSlice = this.mImpl.getNotificationChannelsForPackage(pkg, uid, includeDeleted);
        if (parceledListSlice != null) {
            return parceledListSlice.getList();
        }
        return null;
    }

    public void updateNotificationChannelForPackage(String pkg, int uid, NotificationChannel channel) throws RemoteException {
        if (this.mImpl != null) {
            this.mImpl.updateNotificationChannelForPackage(pkg, uid, channel);
        }
    }

    public NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted) throws RemoteException {
        if (this.mImpl != null) {
            return this.mImpl.getNotificationChannelForPackage(pkg, uid, channelId, includeDeleted);
        }
        return null;
    }

    public boolean matchesCallFilter(Bundle extras) throws RemoteException {
        if (this.mImpl == null) {
            return false;
        }
        if (extras != null) {
            extras.putInt("userId", getCurrentUserId());
        }
        return this.mImpl.matchesCallFilter(extras);
    }

    private static int getCurrentUserId() {
        int userId;
        long origin = Binder.clearCallingIdentity();
        try {
            userId = ActivityManager.getCurrentUser();
        } catch (Exception e) {
            userId = -10000;
            Log.e("HwNotificationManagerImpl", "getCurrentUserId failed");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origin);
            throw th;
        }
        Binder.restoreCallingIdentity(origin);
        return userId;
    }

    private StatusBarNotification[] getActiveNotifications(String callingPkg) throws RemoteException {
        if (this.mImpl != null) {
            return this.mImpl.getActiveNotifications(callingPkg);
        }
        return null;
    }

    public int getClearableNotificationsNum(String callingPkg) throws RemoteException {
        StatusBarNotification[] statusBarNotifications = getActiveNotifications(callingPkg);
        if (statusBarNotifications == null) {
            return 0;
        }
        int num = statusBarNotifications.length;
        for (StatusBarNotification notification : statusBarNotifications) {
            if (!notification.isClearable()) {
                num--;
            }
        }
        return num;
    }

    public void cancelClearableNotifications(String callingPkg) throws RemoteException {
        if (Binder.getCallingUid() == 1000) {
            StatusBarNotification[] statusBarNotifications = getActiveNotifications(callingPkg);
            if (statusBarNotifications != null) {
                for (StatusBarNotification notification : statusBarNotifications) {
                    if (notification.isClearable()) {
                        this.mImpl.cancelNotificationWithTag(notification.getPackageName(), notification.getTag(), notification.getId(), notification.getUser().hashCode());
                    }
                }
            }
        }
    }
}
