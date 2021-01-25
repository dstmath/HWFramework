package ohos.event.commonevent;

import java.util.concurrent.atomic.AtomicReference;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class CommonEventSubscriberHost extends RemoteObject implements ICommonEventSubscriber {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    private static final String TAG = "CommonEventSubscriberHost";
    private AtomicReference<Object> objectAtomicRef;
    private final CommonEventSubscriber owner;

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

    public CommonEventSubscriberHost(String str) {
        super(str);
        this.objectAtomicRef = new AtomicReference<>();
        this.owner = null;
    }

    public CommonEventSubscriberHost() {
        this(ICommonEventSubscriber.DESCRIPTOR);
    }

    CommonEventSubscriberHost(CommonEventSubscriber commonEventSubscriber) {
        super(ICommonEventSubscriber.DESCRIPTOR);
        this.objectAtomicRef = new AtomicReference<>();
        this.owner = commonEventSubscriber;
    }

    /* access modifiers changed from: package-private */
    public Object writeObject(Object obj) {
        this.objectAtomicRef.compareAndSet(null, obj);
        return this.objectAtomicRef.get();
    }

    /* access modifiers changed from: package-private */
    public Object readObject() {
        return this.objectAtomicRef.get();
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null) {
            return false;
        }
        if (i != 1) {
            return CommonEventSubscriberHost.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        CommonEventData commonEventData = new CommonEventData();
        if (messageParcel.readSequenceable(commonEventData)) {
            onReceive(commonEventData);
            return true;
        }
        HiLog.warn(LABEL, "onRemoteRequest read commonEventData failed.", new Object[0]);
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setAsyncCommonEventResult(AsyncCommonEventResult asyncCommonEventResult) {
        CommonEventSubscriber commonEventSubscriber = this.owner;
        if (commonEventSubscriber != null) {
            commonEventSubscriber.setAsyncCommonEventResult(asyncCommonEventResult);
        }
    }
}
