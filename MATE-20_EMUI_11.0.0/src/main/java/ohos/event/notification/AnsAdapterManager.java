package ohos.event.notification;

import android.os.Bundle;
import java.util.HashSet;
import java.util.Set;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.tools.Bytrace;
import ohos.utils.Parcel;

public class AnsAdapterManager {
    private static final int LOG_DOMAIN = 218108548;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final String TAG = "AnsAdapterManager";
    private static AndroidNotificationManager androidNotificationManager = new AndroidNotificationManager();
    private static NotificationSubscriberHost subscriberHost = new NativeNotificationSubscriberHost();

    static void delayShowNotification(long j, long j2) {
    }

    private static native void nativeInitAns();

    /* access modifiers changed from: private */
    public static native void nativeOnProxyCallbackFunc(int i, Parcel parcel);

    private AnsAdapterManager() {
    }

    public static void initAnsNative() {
        try {
            HiLog.info(LOG_LABEL, "AnsAdapterManager load libans_jni.z.so", new Object[0]);
            System.loadLibrary("ans_jni.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LOG_LABEL, "AnsAdapterManager Could not load libans_jni.z.so", new Object[0]);
        }
        nativeInitAns();
    }

    static void subscribe(long j) {
        handleSubscribeOrUnsubscribeNotification(j, true);
    }

    static void publish(long j, long j2, int i) {
        Bytrace.startTrace(2, "AnsAdapterPublish");
        Parcel create = Parcel.create(j);
        NotificationRequest notificationRequest = new NotificationRequest();
        if (!notificationRequest.unmarshalling(create)) {
            HiLog.error(LOG_LABEL, "handlePublishNotification, unmarshalling request failed!", new Object[0]);
            create.reclaim();
            return;
        }
        create.reclaim();
        Bundle bundle = new Bundle();
        bundle.putInt("com.huawei.ohos.foundation.pid", i);
        androidNotificationManager.publishNotification(null, notificationRequest, bundle);
        Bytrace.finishTrace(2, "AnsAdapterPublish");
    }

    static void unsubscribe(long j) {
        handleSubscribeOrUnsubscribeNotification(j, false);
    }

    static void cancelAllNotifications() {
        androidNotificationManager.cancelAllNotifications();
    }

    static void cancelNotification(int i) {
        androidNotificationManager.cancelNotification(null, String.valueOf(i));
    }

    static boolean getActiveNotification(int i, long j) {
        Set<NotificationRequest> activeNotifications = androidNotificationManager.getActiveNotifications(true, i, "");
        Parcel create = Parcel.create(j);
        if (!create.writeInt(activeNotifications.size())) {
            HiLog.error(LOG_LABEL, "getActiveNotification, write count failed!", new Object[0]);
            create.reclaim();
            return false;
        }
        for (NotificationRequest notificationRequest : activeNotifications) {
            if (!notificationRequest.marshalling(create)) {
                HiLog.error(LOG_LABEL, "getActiveNotification, request marshalling failed!", new Object[0]);
                create.reclaim();
                return false;
            }
        }
        return true;
    }

    static int getActiveNotificationCount(int i) {
        return androidNotificationManager.getActiveNotificationNums(true, i, "");
    }

    private static void handleSubscribeOrUnsubscribeNotification(long j, boolean z) {
        HashSet hashSet;
        Parcel create = Parcel.create(j);
        int readInt = create.readInt();
        if (readInt > 0) {
            hashSet = new HashSet();
            for (int i = 0; i < readInt; i++) {
                hashSet.add(create.readString());
            }
        } else {
            hashSet = null;
        }
        create.reclaim();
        if (z) {
            androidNotificationManager.subscribeNotification(subscriberHost, hashSet);
        } else {
            androidNotificationManager.unsubscribeNotification(subscriberHost, hashSet);
        }
    }

    private static class NativeNotificationSubscriberHost extends NotificationSubscriberHost {
        @Override // ohos.event.notification.INotificationSubscriber
        public void onNotificationPosted(NotificationRequest notificationRequest, NotificationSortingMap notificationSortingMap) {
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onNotificationRemoved(NotificationRequest notificationRequest, NotificationSortingMap notificationSortingMap, int i) {
        }

        private NativeNotificationSubscriberHost() {
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onNotificationPosted(NotificationRequest notificationRequest) {
            if (notificationRequest != null) {
                Parcel create = Parcel.create();
                if (!notificationRequest.marshalling(create)) {
                    HiLog.error(AnsAdapterManager.LOG_LABEL, "onNotificationPosted: NotificationRequest marshalling failed!", new Object[0]);
                    create.reclaim();
                    return;
                }
                AnsAdapterManager.nativeOnProxyCallbackFunc(1, create);
                create.reclaim();
            }
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onNotificationRemoved(NotificationRequest notificationRequest) {
            if (notificationRequest != null) {
                Parcel create = Parcel.create();
                if (!notificationRequest.marshalling(create)) {
                    HiLog.error(AnsAdapterManager.LOG_LABEL, "onNotificationRemoved: NotificationRequest marshalling failed!", new Object[0]);
                    create.reclaim();
                    return;
                }
                AnsAdapterManager.nativeOnProxyCallbackFunc(2, create);
                create.reclaim();
            }
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onNotificationRankingUpdate(NotificationSortingMap notificationSortingMap) {
            if (notificationSortingMap != null) {
                AnsAdapterManager.nativeOnProxyCallbackFunc(5, null);
            }
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onSubscribeConnected() {
            AnsAdapterManager.nativeOnProxyCallbackFunc(3, null);
        }

        @Override // ohos.event.notification.INotificationSubscriber
        public void onSubscribeDisConnected() {
            AnsAdapterManager.nativeOnProxyCallbackFunc(4, null);
        }
    }
}
