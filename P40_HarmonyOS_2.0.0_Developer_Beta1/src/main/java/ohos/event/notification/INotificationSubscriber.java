package ohos.event.notification;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface INotificationSubscriber extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.event.notification.INotificationSubscriber";
    public static final int NOTIFICATION_CONNECTED = 3;
    public static final int NOTIFICATION_DISCONNECT = 4;
    public static final int NOTIFICATION_DISTURB_MODE_CHANGE = 6;
    public static final int NOTIFICATION_POSTED = 1;
    public static final int NOTIFICATION_REMOVED = 2;
    public static final int NOTIFICATION_UPDATE = 5;

    void onDisturbModeChange(int i) throws RemoteException;

    void onNotificationPosted(NotificationRequest notificationRequest) throws RemoteException;

    void onNotificationPosted(NotificationRequest notificationRequest, NotificationSortingMap notificationSortingMap) throws RemoteException;

    void onNotificationRankingUpdate(NotificationSortingMap notificationSortingMap) throws RemoteException;

    void onNotificationRemoved(NotificationRequest notificationRequest) throws RemoteException;

    void onNotificationRemoved(NotificationRequest notificationRequest, NotificationSortingMap notificationSortingMap, int i) throws RemoteException;

    void onSubscribeConnected() throws RemoteException;

    void onSubscribeDisConnected() throws RemoteException;
}
