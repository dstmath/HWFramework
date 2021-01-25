package ohos.event.notification;

import java.util.List;
import java.util.Set;
import ohos.annotation.SystemApi;
import ohos.rpc.RemoteException;

public class NotificationHelper {
    public static void delayShowNotification(NotificationRequest notificationRequest, long j) {
    }

    private NotificationHelper() {
    }

    @SystemApi
    public static boolean isAllowedNotify(String str) throws RemoteException {
        return NotificationManagerProxy.getInstance().isAllowedNotify(str);
    }

    public static void publishNotification(NotificationRequest notificationRequest) throws RemoteException {
        publishNotification((String) null, notificationRequest);
    }

    public static void publishNotification(String str, NotificationRequest notificationRequest) throws RemoteException {
        NotificationManagerProxy.getInstance().publishNotification(str, notificationRequest);
    }

    public static void publishNotification(NotificationRequest notificationRequest, String str) throws RemoteException {
        NotificationManagerProxy.getInstance().publishNotification(notificationRequest, str);
    }

    public static void cancelNotification(int i) throws RemoteException {
        NotificationManagerProxy.getInstance().cancelNotification(i);
    }

    public static void cancelAllNotifications() throws RemoteException {
        NotificationManagerProxy.getInstance().cancelAllNotifications();
    }

    @SystemApi
    public static void subscribeNotification(NotificationSubscriber notificationSubscriber) throws RemoteException {
        subscribeNotification(notificationSubscriber, null);
    }

    @SystemApi
    public static void subscribeNotification(NotificationSubscriber notificationSubscriber, NotificationSubscribeInfo notificationSubscribeInfo) throws RemoteException {
        NotificationManagerProxy.getInstance().subscribeNotification(notificationSubscriber, notificationSubscribeInfo);
    }

    @SystemApi
    public static void unsubscribeNotification(NotificationSubscriber notificationSubscriber) throws RemoteException {
        unsubscribeNotification(notificationSubscriber, null);
    }

    @SystemApi
    public static void unsubscribeNotification(NotificationSubscriber notificationSubscriber, NotificationSubscribeInfo notificationSubscribeInfo) throws RemoteException {
        NotificationManagerProxy.getInstance().unsubscribeNotification(notificationSubscriber, notificationSubscribeInfo);
    }

    public static void addNotificationSlot(NotificationSlot notificationSlot) throws RemoteException {
        NotificationManagerProxy.getInstance().addNotificationSlot(notificationSlot);
    }

    public static void removeNotificationSlot(String str) throws RemoteException {
        NotificationManagerProxy.getInstance().removeNotificationSlot(str);
    }

    public static NotificationSlot getNotificationSlot(String str) throws RemoteException {
        return NotificationManagerProxy.getInstance().getNotificationSlot(str);
    }

    public static Set<NotificationRequest> getActiveNotifications() throws RemoteException {
        return NotificationManagerProxy.getInstance().getActiveNotifications();
    }

    @SystemApi
    public static Set<NotificationRequest> getAllActiveNotifications() throws RemoteException {
        return NotificationManagerProxy.getInstance().getAllActiveNotifications();
    }

    public static int getActiveNotificationNums() throws RemoteException {
        return NotificationManagerProxy.getInstance().getActiveNotificationNums();
    }

    public static void setNotificationBadgeNum(int i) throws RemoteException {
        NotificationManagerProxy.getInstance().setNotificationBadgeNum(i);
    }

    public static void setNotificationBadgeNum() throws RemoteException {
        NotificationManagerProxy.getInstance().setNotificationBadgeNum();
    }

    @SystemApi
    public static void setNotificationsEnabledForDefaultBundle(String str, boolean z) throws RemoteException {
        NotificationManagerProxy.getInstance().setNotificationsEnabledForDefaultBundle(str, z);
    }

    @SystemApi
    public static void setNotificationsEnabledForSpecifiedBundle(String str, String str2, boolean z) throws RemoteException {
        NotificationManagerProxy.getInstance().setNotificationsEnabledForSpecifiedBundle(str, str2, z);
    }

    @SystemApi
    public static void setNotificationsEnabledForAllBundles(String str, boolean z) throws RemoteException {
        NotificationManagerProxy.getInstance().setNotificationsEnabledForAllBundles(str, z);
    }

    public static NotificationSortingMap getCurrentAppSorting() throws RemoteException {
        return NotificationManagerProxy.getInstance().getCurrentAppSorting();
    }

    public static void addNotificationSlotGroup(NotificationSlotGroup notificationSlotGroup) throws RemoteException {
        NotificationManagerProxy.getInstance().addNotificationSlotGroup(notificationSlotGroup);
    }

    public static void addNotificationSlotGroups(List<NotificationSlotGroup> list) throws RemoteException {
        NotificationManagerProxy.getInstance().addNotificationSlotGroups(list);
    }

    public static void removeNotificationSlotGroup(String str) throws RemoteException {
        NotificationManagerProxy.getInstance().removeNotificationSlotGroup(str);
    }

    public static NotificationSlotGroup getNotificationSlotGroup(String str) throws RemoteException {
        return NotificationManagerProxy.getInstance().getNotificationSlotGroup(str);
    }

    public static boolean hasNotificationPolicyAccessPermission() throws RemoteException {
        return NotificationManagerProxy.getInstance().hasNotificationPolicyAccessPermission();
    }

    public static void addNotificationSlots(List<NotificationSlot> list) throws RemoteException {
        NotificationManagerProxy.getInstance().addNotificationSlots(list);
    }

    public static List<NotificationSlot> getNotificationSlots() throws RemoteException {
        return NotificationManagerProxy.getInstance().getNotificationSlots();
    }

    public static void publishNotificationAsBundle(String str, NotificationRequest notificationRequest) throws RemoteException {
        NotificationManagerProxy.getInstance().publishNotificationAsBundle(str, notificationRequest);
    }

    public static void setNotificationAgent(String str) throws RemoteException {
        NotificationManagerProxy.getInstance().setNotificationAgent(str);
    }

    public static String getNotificationAgent() throws RemoteException {
        return NotificationManagerProxy.getInstance().getNotificationAgent();
    }

    public static boolean canPublishNotificationAsBundle(String str) throws RemoteException {
        return NotificationManagerProxy.getInstance().canPublishNotificationAsBundle(str);
    }

    public static List<NotificationSlotGroup> getNotificationSlotGroups() throws RemoteException {
        return NotificationManagerProxy.getInstance().getNotificationSlotGroups();
    }

    public static boolean isAllowedNotify() throws RemoteException {
        return NotificationManagerProxy.getInstance().isAllowedNotify();
    }

    public static boolean areNotificationsSuspended() throws RemoteException {
        return NotificationManagerProxy.getInstance().areNotificationsSuspended();
    }

    public static int getBundleImportance() throws RemoteException {
        return NotificationManagerProxy.getInstance().getBundleImportance();
    }

    @SystemApi
    public static void removeNotification(String str) throws RemoteException {
        NotificationManagerProxy.getInstance().removeNotification(str);
    }

    @SystemApi
    public static void removeNotifications(String str) throws RemoteException {
        NotificationManagerProxy.getInstance().removeNotifications(str);
    }

    @SystemApi
    public static void removeNotifications() throws RemoteException {
        NotificationManagerProxy.getInstance().removeNotifications();
    }

    @SystemApi
    public static List<NotificationSlot> getNotificationSlotsForBundle(String str) throws RemoteException {
        return NotificationManagerProxy.getInstance().getNotificationSlotsForBundle(str);
    }

    public static void cancelNotification(String str, int i) throws RemoteException {
        NotificationManagerProxy.getInstance().cancelNotification(str, i);
    }
}
