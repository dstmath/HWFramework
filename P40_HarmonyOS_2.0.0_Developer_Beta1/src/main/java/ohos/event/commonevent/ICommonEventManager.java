package ohos.event.commonevent;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface ICommonEventManager extends IRemoteBroker {
    public static final int CES_PUBLISH_COMMON_EVENT = 3;
    public static final int CES_REGISTER_SUBSCRIBER = 1;
    public static final int CES_UNREGISTER_SUBSCRIBER = 2;
    public static final String DESCRIPTOR = "ohos.event.commonevent.ICommonEventManager";

    void publishCommonEvent(CommonEventData commonEventData, CommonEventPublishInfo commonEventPublishInfo, ICommonEventSubscriber iCommonEventSubscriber) throws RemoteException;

    void subscribeCommonEvent(ICommonEventSubscriber iCommonEventSubscriber, CommonEventSubscribeInfo commonEventSubscribeInfo) throws RemoteException;

    void unsubscribeCommonEvent(ICommonEventSubscriber iCommonEventSubscriber) throws RemoteException;
}
