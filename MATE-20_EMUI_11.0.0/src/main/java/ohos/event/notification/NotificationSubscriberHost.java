package ohos.event.notification;

import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class NotificationSubscriberHost extends RemoteObject implements INotificationSubscriber {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final String TAG = "NotificationSubscriberHost";
    private boolean isDebug;

    public IRemoteObject asObject() {
        return this;
    }

    static {
        try {
            HiLog.info(LABEL, "inner Load libipc_core.z.so", new Object[0]);
            System.loadLibrary("ipc_core.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LABEL, "ERROR: Could not load ipc_core.z ", new Object[0]);
        }
    }

    public NotificationSubscriberHost(String str) {
        super(str);
        this.isDebug = false;
    }

    public NotificationSubscriberHost() {
        this(INotificationSubscriber.DESCRIPTOR);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (!(messageParcel == null || messageParcel2 == null)) {
            if (i == 1) {
                NotificationRequest notificationRequest = new NotificationRequest();
                if (messageParcel.readSequenceable(notificationRequest)) {
                    onNotificationPosted(notificationRequest);
                    return true;
                }
                HiLog.warn(LABEL, "onRemoteRequest onNotificationPosted read data failed.", new Object[0]);
            } else if (i == 2) {
                NotificationRequest notificationRequest2 = new NotificationRequest();
                if (messageParcel.readSequenceable(notificationRequest2)) {
                    onNotificationRemoved(notificationRequest2);
                    return true;
                }
                HiLog.warn(LABEL, "onRemoteRequest onNotificationRemoved read data failed.", new Object[0]);
                return false;
            } else if (i == 3) {
                onSubscribeConnected();
                return true;
            } else if (i == 4) {
                onSubscribeDisConnected();
                return true;
            } else if (i != 5) {
                return NotificationSubscriberHost.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                NotificationSortingMap notificationSortingMap = new NotificationSortingMap();
                if (messageParcel.readSequenceable(notificationSortingMap)) {
                    onNotificationRankingUpdate(notificationSortingMap);
                    return true;
                }
                HiLog.warn(LABEL, "onRemoteRequest onNotificationRankingUpdate read data failed.", new Object[0]);
                return false;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setIsDebug(boolean z) {
        this.isDebug = z;
    }

    /* access modifiers changed from: package-private */
    public boolean getIsDebug() {
        return this.isDebug;
    }
}
