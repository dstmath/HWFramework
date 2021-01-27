package ohos.event.notification;

public abstract class NotificationSubscriber {
    private final NotificationSubscriberHost innerSubscriber = new NotificationSubscriberHost() {
        /* class ohos.event.notification.NotificationSubscriber.AnonymousClass1 */

        @Override // ohos.event.notification.INotificationSubscriber
        public void onNotificationPosted(NotificationRequest notificationRequest) {
            NotificationSubscriber.this.onConsumed(notificationRequest);
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onNotificationPosted(NotificationRequest notificationRequest, NotificationSortingMap notificationSortingMap) {
            NotificationSubscriber.this.onConsumed(notificationRequest, notificationSortingMap);
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onNotificationRemoved(NotificationRequest notificationRequest) {
            NotificationSubscriber.this.onCanceled(notificationRequest);
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onNotificationRemoved(NotificationRequest notificationRequest, NotificationSortingMap notificationSortingMap, int i) {
            NotificationSubscriber.this.onCanceled(notificationRequest, notificationSortingMap, i);
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onNotificationRankingUpdate(NotificationSortingMap notificationSortingMap) {
            NotificationSubscriber.this.onUpdate(notificationSortingMap);
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onSubscribeConnected() {
            NotificationSubscriber.this.onConnected();
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onSubscribeDisConnected() {
            NotificationSubscriber.this.onDisConnect();
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onDisturbModeChange(int i) {
            NotificationSubscriber.this.onDisturbModeChanged(i);
        }
    };

    public abstract void onCanceled(NotificationRequest notificationRequest);

    public abstract void onCanceled(NotificationRequest notificationRequest, NotificationSortingMap notificationSortingMap, int i);

    public abstract void onConnected();

    public abstract void onConsumed(NotificationRequest notificationRequest);

    public abstract void onConsumed(NotificationRequest notificationRequest, NotificationSortingMap notificationSortingMap);

    public abstract void onDied();

    public abstract void onDisConnect();

    public abstract void onDisturbModeChanged(int i);

    public abstract void onUpdate(NotificationSortingMap notificationSortingMap);

    /* access modifiers changed from: package-private */
    public INotificationSubscriber getSubscriber() {
        return this.innerSubscriber;
    }
}
