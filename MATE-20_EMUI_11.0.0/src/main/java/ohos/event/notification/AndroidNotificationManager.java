package ohos.event.notification;

import android.app.ActivityThread;
import android.app.Application;
import android.app.INotificationManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.notification.StatusBarNotification;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.tools.Bytrace;

public class AndroidNotificationManager {
    private static final int DEFAULT_IMPORTANCE = -1000;
    private static final int DEFAULT_NATIVE_PID = -1;
    private static final int DEFAULT_UID = -1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final String LOCAL_DEVICE_ID = "0";
    protected static final String NATIVE_FOUNDATION_EXTEND_INFO = "com.huawei.ohos.foundation.extends";
    protected static final String NATIVE_FOUNDATION_PROCESS_ID = "com.huawei.ohos.foundation.pid";
    private static final String TAG = "AndroidNotificationManager";
    private Context aospContext = null;
    private PackageManager aospPackageManager = null;
    private AdapterListenerService listenerService = new AdapterListenerService();
    private NotificationManager notificationManager = null;
    private INotificationManager serviceManager = null;

    public void publishNotification(String str, NotificationRequest notificationRequest) {
        publishNotification(str, notificationRequest, null);
    }

    public void publishNotification(String str, NotificationRequest notificationRequest, Bundle bundle) {
        if (notificationRequest == null) {
            HiLog.debug(LABEL, "AndroidNotificationManager::publishNotification request is invalid", new Object[0]);
            return;
        }
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "AndroidNotificationManager::publishNotification get NotificationManager failed", new Object[0]);
            return;
        }
        Optional<Notification> transform = NotificationTransformer.getInstance().transform(getAospContext(), notificationManager2, notificationRequest, bundle);
        if (!transform.isPresent()) {
            HiLog.error(LABEL, "AndroidNotificationManager::publishNotification transform Notification failed", new Object[0]);
            return;
        }
        Bytrace.startTrace(2, "EmuiPublishNotification");
        notificationManager2.notify(str, notificationRequest.getNotificationId(), transform.get());
        Bytrace.finishTrace(2, "EmuiPublishNotification");
    }

    public void cancelNotification(String str, String str2) {
        try {
            int notificationId = NotificationTransformer.getInstance().getNotificationId(str2);
            NotificationManager notificationManager2 = getNotificationManager();
            if (notificationManager2 == null) {
                HiLog.debug(LABEL, "AndroidNotificationManager::cancelNotification get getNotificationId failed", new Object[0]);
            } else {
                notificationManager2.cancel(str, notificationId);
            }
        } catch (IllegalArgumentException unused) {
            HiLog.error(LABEL, "AndroidNotificationManager::cancelNotification getNotificationId failed", new Object[0]);
        }
    }

    public void cancelAllNotifications() {
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "AndroidNotificationManager::cancelAllNotifications get NotificationManager failed", new Object[0]);
        } else {
            notificationManager2.cancelAll();
        }
    }

    public void addNotificationSlot(NotificationSlot notificationSlot) {
        if (notificationSlot != null) {
            addNotificationSlots(Collections.singletonList(notificationSlot));
            return;
        }
        throw new IllegalArgumentException("slot can not be null.");
    }

    public void removeNotificationSlot(String str) {
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "AndroidNotificationManager::removeNotificationSlot get NotificationManager failed", new Object[0]);
        } else {
            notificationManager2.deleteNotificationChannel(str);
        }
    }

    public NotificationSlot getNotificationSlot(String str) {
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "AndroidNotificationManager::getNotificationSlot get NotificationManager failed", new Object[0]);
            return null;
        }
        NotificationChannel notificationChannel = notificationManager2.getNotificationChannel(str);
        if (notificationChannel == null) {
            return null;
        }
        Optional<NotificationSlot> notificationSlot = NotificationTransformer.getInstance().getNotificationSlot(notificationChannel);
        if (notificationSlot.isPresent()) {
            return notificationSlot.get();
        }
        HiLog.debug(LABEL, "AndroidNotificationManager::createNotificationSlot getNotificationSlot failed", new Object[0]);
        return null;
    }

    public void subscribeNotification(NotificationSubscriberHost notificationSubscriberHost, NotificationSubscribeInfo notificationSubscribeInfo) {
        this.listenerService.subscribeNotification(notificationSubscriberHost, notificationSubscribeInfo);
    }

    public void unsubscribeNotification(NotificationSubscriberHost notificationSubscriberHost, NotificationSubscribeInfo notificationSubscribeInfo) {
        this.listenerService.unsubscribeNotification(notificationSubscriberHost, notificationSubscribeInfo);
    }

    public void subscribeNotification(NotificationSubscriberHost notificationSubscriberHost, Set<String> set) {
        this.listenerService.subscribeNotification(notificationSubscriberHost, set);
    }

    public void unsubscribeNotification(NotificationSubscriberHost notificationSubscriberHost, Set<String> set) {
        this.listenerService.unsubscribeNotification(notificationSubscriberHost, set);
    }

    public Set<NotificationRequest> getActiveNotifications() {
        return getActiveNotifications(false, 0, "");
    }

    public Set<NotificationRequest> getActiveNotifications(boolean z, int i, String str) {
        Optional<NotificationRequest> optional;
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "AndroidNotificationManager::getActiveNotifications get NotificationManager failed", new Object[0]);
            return new HashSet();
        }
        HashSet hashSet = new HashSet();
        StatusBarNotification[] activeNotifications = notificationManager2.getActiveNotifications();
        if (activeNotifications == null) {
            return hashSet;
        }
        for (StatusBarNotification statusBarNotification : activeNotifications) {
            if (!(statusBarNotification == null || statusBarNotification.getId() == Integer.MAX_VALUE)) {
                if (z) {
                    optional = NotificationTransformer.getInstance().transformToNotificationRequest(statusBarNotification, z, i, str);
                } else {
                    optional = NotificationTransformer.getInstance().transformToNotificationRequest(statusBarNotification);
                }
                optional.ifPresent(new Consumer(hashSet) {
                    /* class ohos.event.notification.$$Lambda$8c0Xx10owZXzBOgLwoJ1fJwjs9Q */
                    private final /* synthetic */ Set f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        this.f$0.add((NotificationRequest) obj);
                    }
                });
            }
        }
        return hashSet;
    }

    public Set<NotificationRequest> getAllActiveNotifications() {
        HashSet hashSet = new HashSet();
        StatusBarNotification[] activeNotifications = this.listenerService.getActiveNotifications();
        if (activeNotifications == null) {
            return hashSet;
        }
        for (StatusBarNotification statusBarNotification : activeNotifications) {
            if (!(statusBarNotification == null || statusBarNotification.getId() == Integer.MAX_VALUE)) {
                NotificationTransformer.getInstance().transformToNotificationRequest(statusBarNotification).ifPresent(new Consumer(hashSet) {
                    /* class ohos.event.notification.$$Lambda$8c0Xx10owZXzBOgLwoJ1fJwjs9Q */
                    private final /* synthetic */ Set f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        this.f$0.add((NotificationRequest) obj);
                    }
                });
            }
        }
        return hashSet;
    }

    public int getActiveNotificationNums() {
        return getActiveNotificationNums(false, 0, "");
    }

    public int getActiveNotificationNums(boolean z, int i, String str) {
        Notification notification;
        Bundle bundle;
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "AndroidNotificationManager::getActiveNotificationNums get NotificationManager failed", new Object[0]);
            return 0;
        }
        StatusBarNotification[] activeNotifications = notificationManager2.getActiveNotifications();
        if (activeNotifications == null) {
            return 0;
        }
        int i2 = 0;
        for (StatusBarNotification statusBarNotification : activeNotifications) {
            if (!(statusBarNotification == null || statusBarNotification.getId() == Integer.MAX_VALUE || (notification = statusBarNotification.getNotification()) == null || (bundle = notification.extras) == null || (z && !(bundle.getInt(NATIVE_FOUNDATION_PROCESS_ID, -1) == i && (str == null || str.isEmpty() || str.equals(bundle.getString(NATIVE_FOUNDATION_EXTEND_INFO, ""))))))) {
                i2++;
            }
        }
        return i2;
    }

    public void setNotificationBadgeNum(int i) {
        Intent launchIntentForPackage;
        ComponentName component;
        String className;
        Context aospContext2 = getAospContext();
        if (aospContext2 != null) {
            String packageName = aospContext2.getPackageName();
            PackageManager packageManager = getPackageManager(aospContext2);
            if (packageManager != null && (launchIntentForPackage = packageManager.getLaunchIntentForPackage(packageName)) != null && (component = launchIntentForPackage.getComponent()) != null && (className = component.getClassName()) != null && !className.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("package", packageName);
                bundle.putString(Constants.ATTRNAME_CLASS, className);
                bundle.putInt("badgenumber", i);
                try {
                    aospContext2.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", (String) null, bundle);
                } catch (IllegalArgumentException | NullPointerException unused) {
                    HiLog.error(LABEL, "AndroidNotificationManager::setNotificationBadgeNum badge function is not supported", new Object[0]);
                }
            }
        }
    }

    public void setNotificationBadgeNum() {
        setNotificationBadgeNum(getActiveNotificationNums());
    }

    public boolean isAllowedNotify(String str) {
        return isAllowedNotify(str, "0");
    }

    /* access modifiers changed from: package-private */
    public boolean isAllowedNotify(String str, String str2) {
        if ("0".equals(str2)) {
            INotificationManager service = getService();
            if (service == null) {
                HiLog.info(LABEL, "isAllowedNotify get service failed", new Object[0]);
                return false;
            }
            int uid = getUid(str);
            if (uid == -1) {
                HiLog.info(LABEL, "isAllowedNotify get uid failed", new Object[0]);
                return false;
            }
            try {
                return service.areNotificationsEnabledForPackage(str, uid);
            } catch (RemoteException unused) {
                HiLog.info(LABEL, "isAllowedNotify calling areNotificationsEnabledForPackage failed", new Object[0]);
            }
        }
        return false;
    }

    public boolean isAllowedNotify() {
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 != null) {
            return notificationManager2.areNotificationsEnabled();
        }
        HiLog.debug(LABEL, "isAllowedNotify::get NotificationManager failed", new Object[0]);
        return false;
    }

    public void setNotificationsEnabledForDefaultPackage(String str, boolean z) {
        String packageName;
        Context aospContext2 = getAospContext();
        if (aospContext2 != null && (packageName = aospContext2.getPackageName()) != null && !packageName.isEmpty()) {
            setNotificationsEnabledForSpecifiedPackage(packageName, str, z);
        }
    }

    public void setNotificationsEnabledForSpecifiedPackage(String str, String str2, boolean z) {
        if (str2 == null || str2.isEmpty()) {
            INotificationManager service = getService();
            if (service == null) {
                HiLog.info(LABEL, "setNotificationsEnabledForSpecifiedPackage get service failed", new Object[0]);
                return;
            }
            int uid = getUid(str);
            if (uid == -1) {
                HiLog.info(LABEL, "setNotificationsEnabledForSpecifiedPackage get uid failed.", new Object[0]);
                return;
            }
            try {
                service.setNotificationsEnabledForPackage(str, uid, z);
            } catch (RemoteException unused) {
                HiLog.info(LABEL, "setNotificationsEnabledForPackage calling failed", new Object[0]);
            }
        }
    }

    public void setNotificationsEnabledForAllPackages(String str, boolean z) {
        Context aospContext2;
        if ((str == null || str.isEmpty()) && (aospContext2 = getAospContext()) != null) {
            PackageManager packageManager = getPackageManager(aospContext2);
            if (packageManager == null) {
                HiLog.info(LABEL, "setNotificationsEnabledForAllPackages get packageManager failed", new Object[0]);
                return;
            }
            List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
            if (installedPackages == null) {
                HiLog.info(LABEL, "setNotificationsEnabledForAllPackages get InstalledPackages failed", new Object[0]);
                return;
            }
            INotificationManager service = getService();
            if (service == null) {
                HiLog.info(LABEL, "setNotificationsEnabledForAllPackages get service failed", new Object[0]);
                return;
            }
            for (PackageInfo packageInfo : installedPackages) {
                if (packageInfo != null) {
                    String str2 = packageInfo.packageName;
                    int uid = getUid(str2);
                    if (uid == -1) {
                        HiLog.info(LABEL, "setNotificationsEnabledForAllPackages get uid of %{public}s failed", new Object[]{str2});
                    } else {
                        try {
                            service.setNotificationsEnabledForPackage(str2, uid, z);
                        } catch (RemoteException unused) {
                            HiLog.info(LABEL, "setNotificationsEnabledForAllPackages calling %{public}s failed", new Object[]{str2});
                        }
                    }
                }
            }
        }
    }

    public NotificationSortingMap getCurrentAppSorting() {
        return this.listenerService.getCurrentAppSorting();
    }

    public void addNotificationSlotGroup(NotificationSlotGroup notificationSlotGroup) {
        if (notificationSlotGroup != null) {
            addNotificationSlotGroups(Collections.singletonList(notificationSlotGroup));
        }
    }

    public void addNotificationSlotGroups(List<NotificationSlotGroup> list) {
        if (list != null) {
            NotificationManager notificationManager2 = getNotificationManager();
            ArrayList arrayList = new ArrayList();
            if (notificationManager2 == null) {
                HiLog.debug(LABEL, "addNotificationSlotGroups::get NotificationManager failed", new Object[0]);
                return;
            }
            for (NotificationSlotGroup notificationSlotGroup : list) {
                NotificationTransformer.getInstance().getNotificationChannelGroup(notificationSlotGroup).ifPresent(new Consumer(arrayList) {
                    /* class ohos.event.notification.$$Lambda$u3lyoQLeOt911yYQFVABHog1g */
                    private final /* synthetic */ List f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        this.f$0.add((NotificationChannelGroup) obj);
                    }
                });
            }
            notificationManager2.createNotificationChannelGroups(arrayList);
        }
    }

    public void removeNotificationSlotGroup(String str) {
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "removeNotificationSlotGroup::get NotificationManager failed", new Object[0]);
        } else {
            notificationManager2.deleteNotificationChannelGroup(str);
        }
    }

    public NotificationSlotGroup getNotificationSlotGroup(String str) {
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "getNotificationSlotGroup::get NotificationManager failed", new Object[0]);
            return null;
        }
        NotificationChannelGroup notificationChannelGroup = notificationManager2.getNotificationChannelGroup(str);
        if (notificationChannelGroup == null) {
            return null;
        }
        Optional<NotificationSlotGroup> notificationSlotGroup = NotificationTransformer.getInstance().getNotificationSlotGroup(notificationChannelGroup);
        if (notificationSlotGroup.isPresent()) {
            return notificationSlotGroup.get();
        }
        HiLog.debug(LABEL, "getNotificationSlotGroup::get NotificationSlotGroup failed", new Object[0]);
        return null;
    }

    public boolean hasNotificationPolicyAccessPermission() {
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 != null) {
            return notificationManager2.isNotificationPolicyAccessGranted();
        }
        HiLog.debug(LABEL, "hasNotificationPolicyAccessPermission gets NotificationManager failed", new Object[0]);
        return false;
    }

    public void addNotificationSlots(List<NotificationSlot> list) {
        if (list != null) {
            NotificationManager notificationManager2 = getNotificationManager();
            if (notificationManager2 == null) {
                HiLog.debug(LABEL, "addNotificationSlots gets NotificationManager failed", new Object[0]);
                return;
            }
            ArrayList arrayList = new ArrayList();
            for (NotificationSlot notificationSlot : list) {
                NotificationTransformer.getInstance().getNotificationChannel(notificationSlot).ifPresent(new Consumer(arrayList) {
                    /* class ohos.event.notification.$$Lambda$pWMIBoPrQzCqegrs52pGRO91Hw */
                    private final /* synthetic */ List f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        this.f$0.add((NotificationChannel) obj);
                    }
                });
            }
            notificationManager2.createNotificationChannels(arrayList);
            return;
        }
        throw new IllegalArgumentException("slots can not be null.");
    }

    public List<NotificationSlot> getNotificationSlots() {
        ArrayList arrayList = new ArrayList();
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "getNotificationSlots gets NotificationManager failed", new Object[0]);
            return arrayList;
        }
        List<NotificationChannel> notificationChannels = notificationManager2.getNotificationChannels();
        if (notificationChannels == null) {
            return arrayList;
        }
        for (NotificationChannel notificationChannel : notificationChannels) {
            NotificationTransformer.getInstance().getNotificationSlot(notificationChannel).ifPresent(new Consumer(arrayList) {
                /* class ohos.event.notification.$$Lambda$LUDFAXRqkVHgeBTVTUtW48YqbBk */
                private final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.add((NotificationSlot) obj);
                }
            });
        }
        return arrayList;
    }

    public void publishNotificationAsPackage(String str, NotificationRequest notificationRequest) {
        if (notificationRequest == null || str == null || str.isEmpty()) {
            throw new IllegalArgumentException("representativePackage and request can not be null.");
        }
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "publishNotificationAsPackage gets NotificationManager failed", new Object[0]);
            return;
        }
        Optional<Notification> transform = NotificationTransformer.getInstance().transform(getAospContext(), notificationManager2, notificationRequest, null);
        if (!transform.isPresent()) {
            HiLog.error(LABEL, "publishNotificationAsPackage transforms Notification failed", new Object[0]);
            return;
        }
        try {
            notificationManager2.notifyAsPackage(str, "", notificationRequest.getNotificationId(), transform.get());
        } catch (SecurityException unused) {
            throw new SecurityException("permission denied publish notification as package.");
        }
    }

    public void setNotificationAgent(String str) {
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "setNotificationAgent gets NotificationManager failed", new Object[0]);
        } else {
            notificationManager2.setNotificationDelegate(str);
        }
    }

    public String getNotificationAgent() {
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 != null) {
            return notificationManager2.getNotificationDelegate();
        }
        HiLog.debug(LABEL, "getNotificationAgent gets NotificationManager failed", new Object[0]);
        return null;
    }

    public boolean canPublishNotificationAsPackage(String str) {
        if (str != null) {
            NotificationManager notificationManager2 = getNotificationManager();
            if (notificationManager2 != null) {
                return notificationManager2.canNotifyAsPackage(str);
            }
            HiLog.debug(LABEL, "canPublishNotificationAsPackage gets NotificationManager failed", new Object[0]);
            return false;
        }
        throw new IllegalArgumentException("representativePackage can not be null.");
    }

    public List<NotificationSlotGroup> getNotificationSlotGroups() {
        ArrayList arrayList = new ArrayList();
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 == null) {
            HiLog.debug(LABEL, "getNotificationSlotGroups::get NotificationManager failed", new Object[0]);
            return arrayList;
        }
        List<NotificationChannelGroup> notificationChannelGroups = notificationManager2.getNotificationChannelGroups();
        if (notificationChannelGroups == null) {
            return arrayList;
        }
        for (NotificationChannelGroup notificationChannelGroup : notificationChannelGroups) {
            NotificationTransformer.getInstance().getNotificationSlotGroup(notificationChannelGroup).ifPresent(new Consumer(arrayList) {
                /* class ohos.event.notification.$$Lambda$NUEKXLgki07PVfBs_anCa2iaDyM */
                private final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.add((NotificationSlotGroup) obj);
                }
            });
        }
        return arrayList;
    }

    public boolean areNotificationsSuspended() {
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 != null) {
            return notificationManager2.areNotificationsPaused();
        }
        HiLog.debug(LABEL, "areNotificationsSuspended::get NotificationManager failed", new Object[0]);
        return false;
    }

    public int getPackageImportance() {
        NotificationManager notificationManager2 = getNotificationManager();
        if (notificationManager2 != null) {
            return notificationManager2.getImportance();
        }
        HiLog.debug(LABEL, "getPackageImportance::get NotificationManager failed", new Object[0]);
        return -1000;
    }

    public void removeNotification(String str) {
        if (str != null) {
            try {
                this.listenerService.removeNotification(str);
            } catch (SecurityException unused) {
                throw new SecurityException("not allowed to call removeNotification.");
            }
        } else {
            throw new IllegalArgumentException("hashCode is null");
        }
    }

    public void removeNotifications(String str) {
        if (str != null) {
            try {
                this.listenerService.removeNotifications(str);
            } catch (SecurityException unused) {
                throw new SecurityException("not allowed to call removeNotifications.");
            }
        } else {
            throw new IllegalArgumentException("bundleName is null");
        }
    }

    public void removeNotifications() {
        try {
            this.listenerService.removeNotifications();
        } catch (SecurityException unused) {
            throw new SecurityException("not allowed to call removeNotifications.");
        }
    }

    public List<NotificationSlot> getNotificationSlotsForBundle(String str) throws ohos.rpc.RemoteException {
        List list;
        if (str == null || str.isEmpty()) {
            return null;
        }
        INotificationManager service = getService();
        if (service == null) {
            HiLog.info(LABEL, "getNotificationSlotsForBundle get service failed.", new Object[0]);
            return null;
        }
        int uid = getUid(str);
        if (uid == -1) {
            HiLog.info(LABEL, "getNotificationSlotsForBundle get uid failed.", new Object[0]);
            return null;
        }
        try {
            ParceledListSlice notificationChannelsForPackage = service.getNotificationChannelsForPackage(str, uid, false);
            if (notificationChannelsForPackage == null || (list = notificationChannelsForPackage.getList()) == null || list.isEmpty()) {
                return null;
            }
            ArrayList arrayList = new ArrayList();
            for (Object obj : list) {
                if (obj instanceof NotificationChannel) {
                    NotificationTransformer.getInstance().getNotificationSlot((NotificationChannel) obj).ifPresent(new Consumer(arrayList) {
                        /* class ohos.event.notification.$$Lambda$LUDFAXRqkVHgeBTVTUtW48YqbBk */
                        private final /* synthetic */ List f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            this.f$0.add((NotificationSlot) obj);
                        }
                    });
                }
            }
            return arrayList;
        } catch (RemoteException | SecurityException unused) {
            throw new ohos.rpc.RemoteException("Caller is not system application.");
        }
    }

    private int getUid(String str) {
        Context aospContext2 = getAospContext();
        if (aospContext2 == null) {
            return -1;
        }
        PackageManager packageManager = getPackageManager(aospContext2);
        if (packageManager == null) {
            HiLog.info(LABEL, "getUid get packageManager failed", new Object[0]);
            return -1;
        }
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(str, 1);
            if (applicationInfo != null) {
                return applicationInfo.uid;
            }
            HiLog.info(LABEL, "getUid getApplicationInfo failed", new Object[0]);
            return -1;
        } catch (PackageManager.NameNotFoundException unused) {
            HiLog.info(LABEL, "getUid calling failed", new Object[0]);
            return -1;
        }
    }

    private INotificationManager getService() {
        if (this.serviceManager == null) {
            this.serviceManager = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        }
        return this.serviceManager;
    }

    private Context getAospContext() {
        Application currentApplication;
        if (this.aospContext == null && (currentApplication = ActivityThread.currentApplication()) != null) {
            this.aospContext = currentApplication.getApplicationContext();
        }
        return this.aospContext;
    }

    private NotificationManager getNotificationManager() {
        Context aospContext2;
        if (this.notificationManager == null && (aospContext2 = getAospContext()) != null) {
            Object systemService = aospContext2.getSystemService("notification");
            if (systemService instanceof NotificationManager) {
                this.notificationManager = (NotificationManager) systemService;
            }
        }
        return this.notificationManager;
    }

    private PackageManager getPackageManager(Context context) {
        if (this.aospPackageManager == null) {
            this.aospPackageManager = context.getPackageManager();
        }
        return this.aospPackageManager;
    }
}
