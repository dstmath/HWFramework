package ohos.event.notification;

import java.util.List;
import java.util.Set;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IAdvancedNotificationManager extends IRemoteBroker {
    public static final int ANS_ABORT_ALL = 2;
    public static final int ANS_ABORT_ONE = 3;
    public static final int ANS_ADD_SLOTS = 23;
    public static final int ANS_CAN_PUBLISH_NOTIFICATION_AS_BUNDLE = 33;
    public static final int ANS_CREATE_SLOT = 6;
    public static final int ANS_CREATE_SLOTGROUP = 19;
    public static final int ANS_CREATE_SLOTGROUPS = 25;
    public static final int ANS_DELETE_SLOT = 7;
    public static final int ANS_DELETE_SLOTGROUP = 20;
    public static final int ANS_GET_ACTIVE_NOTIFICATION = 9;
    public static final int ANS_GET_ACTIVE_NOTIFICATION_NUM = 11;
    public static final int ANS_GET_ALL_ACTIVE_NOTIFICATION = 10;
    public static final int ANS_GET_CURRENT_DISTURB_MODE = 39;
    public static final int ANS_GET_NOTIFICATION_AGENT = 32;
    public static final int ANS_GET_SLOT = 8;
    public static final int ANS_GET_SLOTGROUP = 21;
    public static final int ANS_GET_SLOTGROUPS = 26;
    public static final int ANS_GET_SLOTS = 24;
    public static final int ANS_GET_SLOTS_FOR_BUNDLE = 37;
    public static final int ANS_GET_SORTING = 18;
    public static final int ANS_INQUIRY_ALLOWED_NOTIFY = 14;
    public static final int ANS_INQUIRY_BUNDLE_IMPORTANCE = 29;
    public static final int ANS_INQUIRY_IS_SUSPENDED = 28;
    public static final int ANS_NOTIFICATIONS_ALLOWED = 27;
    public static final int ANS_NOTIFICATION_POLICY_ACCESS_PERMISSION = 22;
    public static final int ANS_PUBLISH_NOTIFICATION_AS_BUNDLE = 30;
    public static final int ANS_REMOVE_ALL_NOTIFICATIONS = 36;
    public static final int ANS_REMOVE_A_BUNDLE_NOTIFICATIONS = 35;
    public static final int ANS_REMOVE_A_NOTIFICATION = 34;
    public static final int ANS_SCHEDULE_NOTIFICATION = 1;
    public static final int ANS_SET_ALL_ENABLE_NOTIFICATION2 = 17;
    public static final int ANS_SET_DEFAULT_ENABLE_NOTIFICATION1 = 15;
    public static final int ANS_SET_DISTURB_MODE = 38;
    public static final int ANS_SET_NOTIFICATION_AGENT = 31;
    public static final int ANS_SET_NOTIFICATION_BADGE_NUM1 = 12;
    public static final int ANS_SET_NOTIFICATION_BADGE_NUM2 = 13;
    public static final int ANS_SET_SPECIFICAL_ENABLE_NOTIFICATION2 = 16;
    public static final int ANS_SUBSCRIBE_NOTIFICATION = 4;
    public static final int ANS_UNSUBSCRIBE_NOTIFICATION = 5;
    public static final String DESCRIPTOR = "ohos.event.notification.IAdvancedNotificationManager";

    void addNotificationSlot(String str, NotificationSlot notificationSlot) throws RemoteException;

    void addNotificationSlotGroup(String str, NotificationSlotGroup notificationSlotGroup) throws RemoteException;

    void addNotificationSlotGroups(String str, List<NotificationSlotGroup> list) throws RemoteException;

    void addNotificationSlots(String str, List<NotificationSlot> list) throws RemoteException;

    boolean areNotificationsSuspended(String str) throws RemoteException;

    boolean canPublishNotificationAsBundle(String str, String str2) throws RemoteException;

    void cancelAllNotifications(String str, int i) throws RemoteException;

    void cancelNotification(String str, String str2) throws RemoteException;

    int getActiveNotificationNums(String str) throws RemoteException;

    Set<NotificationRequest> getActiveNotifications(String str) throws RemoteException;

    Set<NotificationRequest> getAllActiveNotifications(String[] strArr) throws RemoteException;

    int getBundleImportance(String str) throws RemoteException;

    NotificationSortingMap getCurrentAppSorting(String str) throws RemoteException;

    int getDisturbMode() throws RemoteException;

    String getNotificationAgent(String str) throws RemoteException;

    NotificationSlot getNotificationSlot(String str, String str2) throws RemoteException;

    NotificationSlotGroup getNotificationSlotGroup(String str, String str2) throws RemoteException;

    List<NotificationSlotGroup> getNotificationSlotGroups(String str) throws RemoteException;

    List<NotificationSlot> getNotificationSlots(String str) throws RemoteException;

    List<NotificationSlot> getNotificationSlotsForBundle(String str, String str2) throws RemoteException;

    boolean hasNotificationPolicyAccessPermission(String str) throws RemoteException;

    boolean isAllowedNotify() throws RemoteException;

    boolean isAllowedNotify(String str) throws RemoteException;

    void publishNotificationAsBundle(String str, String str2, NotificationRequest notificationRequest) throws RemoteException;

    void removeNotification(String str, String str2) throws RemoteException;

    void removeNotificationSlot(String str, String str2) throws RemoteException;

    void removeNotificationSlotGroup(String str, String str2) throws RemoteException;

    void removeNotifications(String str) throws RemoteException;

    void removeNotifications(String str, String str2) throws RemoteException;

    void scheduleNotification(String str, String str2, NotificationRequest notificationRequest) throws RemoteException;

    void setDisturbMode(int i) throws RemoteException;

    void setNotificationAgent(String str, String str2) throws RemoteException;

    void setNotificationBadgeNum(String str) throws RemoteException;

    void setNotificationBadgeNum(String str, int i) throws RemoteException;

    void setNotificationsEnabledForAllBundles(String str, boolean z) throws RemoteException;

    void setNotificationsEnabledForDefaultBundle(String str, boolean z) throws RemoteException;

    void setNotificationsEnabledForSpecifiedBundle(String str, String str2, boolean z) throws RemoteException;

    void subscribeNotification(INotificationSubscriber iNotificationSubscriber, NotificationSubscribeInfo notificationSubscribeInfo) throws RemoteException;

    void unsubscribeNotification(INotificationSubscriber iNotificationSubscriber, NotificationSubscribeInfo notificationSubscribeInfo) throws RemoteException;
}
