package ohos.event.notification;

import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.tools.Bytrace;
import ohos.utils.Parcel;

public class ZidaneNotificationManager {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final String TAG = "ZidaneNotificationManager";

    private static native void nativePublishRemoteNotification(Parcel parcel, String str);

    static {
        try {
            HiLog.info(LABEL, "inner Load libans_client_jni.z.so", new Object[0]);
            System.loadLibrary("ans_client_jni.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LABEL, "ERROR: Could not load libans_client_jni.z.so ", new Object[0]);
        }
    }

    public void publishNotification(String str, NotificationRequest notificationRequest) {
        HiLog.debug(LABEL, "ZidaneNotificationManager::publishNotification ans not support yet", new Object[0]);
    }

    public void cancelNotification(String str) {
        HiLog.debug(LABEL, "ZidaneNotificationManager::cancelNotification ans not support yet", new Object[0]);
    }

    public void cancelAllNotifications(String str, int i) {
        HiLog.debug(LABEL, "ZidaneNotificationManager::cancelAllNotifications ans not support yet", new Object[0]);
    }

    /* access modifiers changed from: package-private */
    public void publishRemoteNotification(NotificationRequest notificationRequest, String str) {
        Parcel create = Parcel.create();
        if (!notificationRequest.marshalling(create)) {
            HiLog.error(LABEL, "ZidaneNotificationManager:publishRemoteNotification invalid remote notification", new Object[0]);
            create.reclaim();
            return;
        }
        Bytrace.startTrace(2, "publishnotificationjni");
        nativePublishRemoteNotification(create, str);
        Bytrace.finishTrace(2, "publishnotificationjni");
        create.reclaim();
    }
}
