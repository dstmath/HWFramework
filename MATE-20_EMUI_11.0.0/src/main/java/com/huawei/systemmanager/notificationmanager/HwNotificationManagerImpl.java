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
import com.huawei.android.os.UserHandleEx;
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

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
    public boolean canShowBadge(String pkg, int uid) throws RemoteException {
        INotificationManager iNotificationManager = this.mImpl;
        if (iNotificationManager != null) {
            return iNotificationManager.canShowBadge(pkg, uid);
        }
        return false;
    }

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
    public boolean areNotificationsEnabledForPackage(String pkg, int uid) throws RemoteException {
        INotificationManager iNotificationManager = this.mImpl;
        if (iNotificationManager != null) {
            return iNotificationManager.areNotificationsEnabledForPackage(pkg, uid);
        }
        return false;
    }

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
    public void setShowBadge(String pkg, int uid, boolean showBadge) throws RemoteException {
        INotificationManager iNotificationManager = this.mImpl;
        if (iNotificationManager != null) {
            iNotificationManager.setShowBadge(pkg, uid, showBadge);
        }
    }

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
    public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) throws RemoteException {
        INotificationManager iNotificationManager = this.mImpl;
        if (iNotificationManager != null) {
            iNotificationManager.setNotificationsEnabledForPackage(pkg, uid, enabled);
        }
    }

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
    public void setBubblesAllowed(String pkg, int uid, boolean allowed) throws RemoteException {
        INotificationManager iNotificationManager = this.mImpl;
        if (iNotificationManager != null) {
            iNotificationManager.setBubblesAllowed(pkg, uid, allowed);
        }
    }

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
    public boolean areBubblesAllowedForPackage(String pkg, int uid) throws RemoteException {
        INotificationManager iNotificationManager = this.mImpl;
        if (iNotificationManager != null) {
            return iNotificationManager.areBubblesAllowedForPackage(pkg, uid);
        }
        return false;
    }

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
    public List<NotificationChannel> getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted) throws RemoteException {
        ParceledListSlice parceledListSlice;
        INotificationManager iNotificationManager = this.mImpl;
        if (iNotificationManager == null || (parceledListSlice = iNotificationManager.getNotificationChannelsForPackage(pkg, uid, includeDeleted)) == null) {
            return null;
        }
        return parceledListSlice.getList();
    }

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
    public void updateNotificationChannelForPackage(String pkg, int uid, NotificationChannel channel) throws RemoteException {
        INotificationManager iNotificationManager = this.mImpl;
        if (iNotificationManager != null) {
            iNotificationManager.updateNotificationChannelForPackage(pkg, uid, channel);
        }
    }

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
    public NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted) throws RemoteException {
        INotificationManager iNotificationManager = this.mImpl;
        if (iNotificationManager != null) {
            return iNotificationManager.getNotificationChannelForPackage(pkg, uid, channelId, includeDeleted);
        }
        return null;
    }

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
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
            userId = UserHandleEx.USER_NULL;
            Log.e("HwNotificationManagerImpl", "getCurrentUserId failed");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origin);
            throw th;
        }
        Binder.restoreCallingIdentity(origin);
        return userId;
    }

    private StatusBarNotification[] getActiveNotifications(String callingPkg) throws RemoteException {
        INotificationManager iNotificationManager = this.mImpl;
        if (iNotificationManager != null) {
            return iNotificationManager.getActiveNotifications(callingPkg);
        }
        return null;
    }

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
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

    @Override // com.huawei.systemmanager.notificationmanager.IHwNotificationManager
    public void cancelClearableNotifications(String callingPkg) throws RemoteException {
        StatusBarNotification[] statusBarNotifications;
        if (Binder.getCallingUid() == 1000 && (statusBarNotifications = getActiveNotifications(callingPkg)) != null) {
            for (StatusBarNotification notification : statusBarNotifications) {
                if (notification.isClearable()) {
                    this.mImpl.cancelNotificationWithTag(notification.getPackageName(), notification.getTag(), notification.getId(), notification.getUser().hashCode());
                }
            }
        }
    }
}
