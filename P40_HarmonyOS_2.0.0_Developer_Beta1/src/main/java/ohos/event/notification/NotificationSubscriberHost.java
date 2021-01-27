package ohos.event.notification;

import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
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

    /* access modifiers changed from: package-private */
    public void setDebug(boolean z) {
        this.isDebug = z;
    }

    /* access modifiers changed from: package-private */
    public boolean isDebug() {
        return this.isDebug;
    }
}
