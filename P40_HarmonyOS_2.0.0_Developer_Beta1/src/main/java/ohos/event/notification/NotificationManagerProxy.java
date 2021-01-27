package ohos.event.notification;

import java.util.List;
import java.util.Set;
import ohos.rpc.RemoteException;

public class NotificationManagerProxy {
    private static final NotificationManagerProxy INSTANCE = new NotificationManagerProxy();
    private NotificationAdapter adapter = new NotificationAdapter();

    private NotificationManagerProxy() {
    }

    static NotificationManagerProxy getInstance() {
        return INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public void publishNotification(String str, NotificationRequest notificationRequest) throws RemoteException {
        if (notificationRequest != null) {
            this.adapter.scheduleNotification(str, notificationRequest.getNotificationHashCode(), notificationRequest);
            return;
        }
        throw new IllegalArgumentException("notificationRequest can not be null.");
    }

    /* access modifiers changed from: package-private */
    public void publishNotification(NotificationRequest notificationRequest, String str) throws RemoteException {
        if (notificationRequest != null) {
            this.adapter.scheduleRemoteNotification(notificationRequest, str);
            return;
        }
        throw new IllegalArgumentException("notificationRequest must be non-null.");
    }

    /* access modifiers changed from: package-private */
    public void cancelNotification(int i) throws RemoteException {
        this.adapter.cancelNotification(null, String.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public void cancelNotification(String str, int i) throws RemoteException {
        this.adapter.cancelNotification(str, String.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public void cancelAllNotifications() throws RemoteException {
        this.adapter.cancelAllNotifications("", 0);
    }

    /* access modifiers changed from: package-private */
    public void subscribeNotification(NotificationSubscriber notificationSubscriber, NotificationSubscribeInfo notificationSubscribeInfo) throws RemoteException {
        if (notificationSubscriber != null) {
            this.adapter.subscribeNotification(notificationSubscriber.getSubscriber(), notificationSubscribeInfo);
            return;
        }
        throw new IllegalArgumentException("subscriber can not be null.");
    }

    /* access modifiers changed from: package-private */
    public void unsubscribeNotification(NotificationSubscriber notificationSubscriber, NotificationSubscribeInfo notificationSubscribeInfo) throws RemoteException {
        if (notificationSubscriber != null) {
            this.adapter.unsubscribeNotification(notificationSubscriber.getSubscriber(), notificationSubscribeInfo);
            return;
        }
        throw new IllegalArgumentException("subscriber can not be null.");
    }

    /* access modifiers changed from: package-private */
    public void addNotificationSlot(NotificationSlot notificationSlot) throws RemoteException {
        this.adapter.addNotificationSlot("", notificationSlot);
    }

    /* access modifiers changed from: package-private */
    public void removeNotificationSlot(String str) throws RemoteException {
        this.adapter.removeNotificationSlot("", str);
    }

    /* access modifiers changed from: package-private */
    public NotificationSlot getNotificationSlot(String str) throws RemoteException {
        return this.adapter.getNotificationSlot("", str);
    }

    /* access modifiers changed from: package-private */
    public Set<NotificationRequest> getActiveNotifications() throws RemoteException {
        return this.adapter.getActiveNotifications("");
    }

    /* access modifiers changed from: package-private */
    public Set<NotificationRequest> getAllActiveNotifications(String[] strArr) throws RemoteException {
        return this.adapter.getAllActiveNotifications(strArr);
    }

    /* access modifiers changed from: package-private */
    public int getActiveNotificationNums() throws RemoteException {
        return this.adapter.getActiveNotificationNums("");
    }

    /* access modifiers changed from: package-private */
    public void setNotificationBadgeNum(int i) throws RemoteException {
        this.adapter.setNotificationBadgeNum("", i);
    }

    /* access modifiers changed from: package-private */
    public void setNotificationBadgeNum() throws RemoteException {
        this.adapter.setNotificationBadgeNum("");
    }

    /* access modifiers changed from: package-private */
    public boolean isAllowedNotify(String str) throws RemoteException {
        if (str != null && !str.isEmpty()) {
            return this.adapter.isAllowedNotify(str);
        }
        throw new IllegalArgumentException("bundle can not be null or empty.");
    }

    /* access modifiers changed from: package-private */
    public void setNotificationsEnabledForDefaultBundle(String str, boolean z) throws RemoteException {
        this.adapter.setNotificationsEnabledForDefaultBundle(str, z);
    }

    /* access modifiers changed from: package-private */
    public void setNotificationsEnabledForSpecifiedBundle(String str, String str2, boolean z) throws RemoteException {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.adapter.setNotificationsEnabledForSpecifiedBundle(str, str2, z);
    }

    /* access modifiers changed from: package-private */
    public void setNotificationsEnabledForAllBundles(String str, boolean z) throws RemoteException {
        this.adapter.setNotificationsEnabledForAllBundles(str, z);
    }

    /* access modifiers changed from: package-private */
    public NotificationSortingMap getCurrentAppSorting() throws RemoteException {
        return this.adapter.getCurrentAppSorting("");
    }

    /* access modifiers changed from: package-private */
    public void addNotificationSlotGroup(NotificationSlotGroup notificationSlotGroup) throws RemoteException {
        if (notificationSlotGroup != null) {
            this.adapter.addNotificationSlotGroup("", notificationSlotGroup);
            return;
        }
        throw new IllegalArgumentException("slotGroup can not be null.");
    }

    /* access modifiers changed from: package-private */
    public void addNotificationSlotGroups(List<NotificationSlotGroup> list) throws RemoteException {
        if (list != null) {
            this.adapter.addNotificationSlotGroups("", list);
            return;
        }
        throw new IllegalArgumentException();
    }

    /* access modifiers changed from: package-private */
    public void removeNotificationSlotGroup(String str) throws RemoteException {
        this.adapter.removeNotificationSlotGroup("", str);
    }

    /* access modifiers changed from: package-private */
    public NotificationSlotGroup getNotificationSlotGroup(String str) throws RemoteException {
        return this.adapter.getNotificationSlotGroup("", str);
    }

    /* access modifiers changed from: package-private */
    public boolean hasNotificationPolicyAccessPermission() throws RemoteException {
        return this.adapter.hasNotificationPolicyAccessPermission("");
    }

    /* access modifiers changed from: package-private */
    public void addNotificationSlots(List<NotificationSlot> list) throws RemoteException {
        this.adapter.addNotificationSlots("", list);
    }

    /* access modifiers changed from: package-private */
    public List<NotificationSlot> getNotificationSlots() throws RemoteException {
        return this.adapter.getNotificationSlots("");
    }

    /* access modifiers changed from: package-private */
    public void publishNotificationAsBundle(String str, NotificationRequest notificationRequest) throws RemoteException {
        this.adapter.publishNotificationAsBundle("", str, notificationRequest);
    }

    /* access modifiers changed from: package-private */
    public void setNotificationAgent(String str) throws RemoteException {
        this.adapter.setNotificationAgent("", str);
    }

    /* access modifiers changed from: package-private */
    public String getNotificationAgent() throws RemoteException {
        return this.adapter.getNotificationAgent("");
    }

    /* access modifiers changed from: package-private */
    public boolean canPublishNotificationAsBundle(String str) throws RemoteException {
        return this.adapter.canPublishNotificationAsBundle("", str);
    }

    /* access modifiers changed from: package-private */
    public List<NotificationSlotGroup> getNotificationSlotGroups() throws RemoteException {
        return this.adapter.getNotificationSlotGroups("");
    }

    /* access modifiers changed from: package-private */
    public boolean isAllowedNotify() throws RemoteException {
        return this.adapter.isAllowedNotify();
    }

    /* access modifiers changed from: package-private */
    public boolean areNotificationsSuspended() throws RemoteException {
        return this.adapter.areNotificationsSuspended("");
    }

    /* access modifiers changed from: package-private */
    public int getBundleImportance() throws RemoteException {
        return this.adapter.getBundleImportance("");
    }

    /* access modifiers changed from: package-private */
    public void removeNotification(String str) throws RemoteException {
        this.adapter.removeNotification("", str);
    }

    /* access modifiers changed from: package-private */
    public void removeNotifications(String str) throws RemoteException {
        this.adapter.removeNotifications("", str);
    }

    /* access modifiers changed from: package-private */
    public void removeNotifications() throws RemoteException {
        this.adapter.removeNotifications("");
    }

    /* access modifiers changed from: package-private */
    public List<NotificationSlot> getNotificationSlotsForBundle(String str) throws RemoteException {
        return this.adapter.getNotificationSlotsForBundle("", str);
    }

    /* access modifiers changed from: package-private */
    public void setDisturbMode(int i) throws RemoteException {
        this.adapter.setDisturbMode(i);
    }

    /* access modifiers changed from: package-private */
    public int getDisturbMode() throws RemoteException {
        return this.adapter.getDisturbMode();
    }
}
