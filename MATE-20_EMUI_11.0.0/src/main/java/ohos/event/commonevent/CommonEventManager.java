package ohos.event.commonevent;

import ohos.rpc.RemoteException;
import ohos.tools.Bytrace;

public final class CommonEventManager {
    public static void publishCommonEvent(CommonEventData commonEventData) throws RemoteException {
        Bytrace.startTrace(2, "publishCommonEVentOne");
        CommonEventManagerProxy.getInstance().publishCommonEvent(commonEventData, null, null);
        Bytrace.finishTrace(2, "publishCommonEVentOne");
    }

    public static void publishCommonEvent(CommonEventData commonEventData, CommonEventPublishInfo commonEventPublishInfo) throws RemoteException {
        Bytrace.startTrace(2, "publishCommonEVentTwo");
        CommonEventManagerProxy.getInstance().publishCommonEvent(commonEventData, commonEventPublishInfo, null);
        Bytrace.finishTrace(2, "publishCommonEVentTwo");
    }

    public static void publishCommonEvent(CommonEventData commonEventData, CommonEventPublishInfo commonEventPublishInfo, CommonEventSubscriber commonEventSubscriber) throws RemoteException {
        Bytrace.startTrace(2, "publishCommonEVentThree");
        CommonEventManagerProxy.getInstance().publishCommonEvent(commonEventData, commonEventPublishInfo, commonEventSubscriber);
        Bytrace.finishTrace(2, "publishCommonEVentThree");
    }

    public static void subscribeCommonEvent(CommonEventSubscriber commonEventSubscriber) throws RemoteException {
        Bytrace.startTrace(2, "subscribeCommonEvent");
        CommonEventManagerProxy.getInstance().subscribeCommonEvent(commonEventSubscriber);
        Bytrace.finishTrace(2, "subscribeCommonEvent");
    }

    public static void unsubscribeCommonEvent(CommonEventSubscriber commonEventSubscriber) throws RemoteException {
        CommonEventManagerProxy.getInstance().unsubscribeCommonEvent(commonEventSubscriber);
    }

    private CommonEventManager() {
    }
}
