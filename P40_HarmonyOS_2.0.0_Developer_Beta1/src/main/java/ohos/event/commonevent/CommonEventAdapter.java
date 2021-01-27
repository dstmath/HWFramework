package ohos.event.commonevent;

import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

class CommonEventAdapter extends RemoteObject implements ICommonEventManager {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    private static final String TAG = "CommonEventAdapter";

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

    public CommonEventAdapter() {
        super(ICommonEventSubscriber.DESCRIPTOR);
    }

    @Override // ohos.event.commonevent.ICommonEventManager
    public void subscribeCommonEvent(ICommonEventSubscriber iCommonEventSubscriber, CommonEventSubscribeInfo commonEventSubscribeInfo) throws RemoteException {
        if (iCommonEventSubscriber == null || commonEventSubscribeInfo == null) {
            throw new RemoteException("Subscriber or subscribe info is null.");
        } else if (!(iCommonEventSubscriber instanceof CommonEventSubscriberHost)) {
            HiLog.warn(LABEL, "CommonEventAdapter::subscribeCommonEvent invalid ICommonEventSubscriber", new Object[0]);
        } else {
            AndroidCommonEventManager.getInstance().subscribeCommonEvent((CommonEventSubscriberHost) iCommonEventSubscriber, commonEventSubscribeInfo);
        }
    }

    @Override // ohos.event.commonevent.ICommonEventManager
    public void unsubscribeCommonEvent(ICommonEventSubscriber iCommonEventSubscriber) throws RemoteException {
        if (iCommonEventSubscriber == null) {
            throw new RemoteException("Subscriber is null.");
        } else if (!(iCommonEventSubscriber instanceof CommonEventSubscriberHost)) {
            HiLog.warn(LABEL, "CommonEventAdapter::unsubscribeCommonEvent invalid ICommonEventSubscriber", new Object[0]);
        } else {
            AndroidCommonEventManager.getInstance().unsubscribeCommonEvent((CommonEventSubscriberHost) iCommonEventSubscriber);
        }
    }

    @Override // ohos.event.commonevent.ICommonEventManager
    public void publishCommonEvent(CommonEventData commonEventData, CommonEventPublishInfo commonEventPublishInfo, ICommonEventSubscriber iCommonEventSubscriber) throws RemoteException {
        if (commonEventData != null) {
            AndroidCommonEventManager.getInstance().publishCommonEvent(commonEventData, commonEventPublishInfo, iCommonEventSubscriber);
            return;
        }
        throw new RemoteException("Common event data is null.");
    }
}
