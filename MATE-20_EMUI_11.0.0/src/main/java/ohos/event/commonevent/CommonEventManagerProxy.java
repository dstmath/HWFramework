package ohos.event.commonevent;

import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.tools.Bytrace;

/* access modifiers changed from: package-private */
public class CommonEventManagerProxy {
    private static final CommonEventManagerProxy INSTANCE = new CommonEventManagerProxy();
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    private static final String TAG = "CommonEventManagerProxy";

    private CommonEventManagerProxy() {
    }

    static CommonEventManagerProxy getInstance() {
        return INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public void subscribeCommonEvent(CommonEventSubscriber commonEventSubscriber) throws IllegalArgumentException, RemoteException {
        if (commonEventSubscriber == null) {
            throw new IllegalArgumentException("The subscriber param is illegal.");
        } else if (commonEventSubscriber.nativeSubscriber == 0) {
            ICommonEventManager service = CommonEventManagerHelper.getService();
            if (service != null) {
                service.subscribeCommonEvent(commonEventSubscriber.getSubscriber(), commonEventSubscriber.getSubscribeInfo());
            }
        } else {
            Bytrace.startTrace(2, "javacalljni");
            commonEventSubscriber.handleRemoteSubscribe();
            Bytrace.finishTrace(2, "javacalljni");
        }
    }

    /* access modifiers changed from: package-private */
    public void unsubscribeCommonEvent(CommonEventSubscriber commonEventSubscriber) throws RemoteException {
        if (commonEventSubscriber == null) {
            throw new IllegalArgumentException("The subscriber can not be null.");
        } else if (commonEventSubscriber.nativeSubscriber == 0) {
            ICommonEventManager service = CommonEventManagerHelper.getService();
            if (service != null) {
                service.unsubscribeCommonEvent(commonEventSubscriber.getSubscriber());
            }
        } else {
            Bytrace.startTrace(2, "UnsubCallJniMethod");
            commonEventSubscriber.handleRemoteUnsubscribe();
            Bytrace.finishTrace(2, "UnsubCallJniMethod");
        }
    }

    /* access modifiers changed from: package-private */
    public void publishCommonEvent(CommonEventData commonEventData, CommonEventPublishInfo commonEventPublishInfo, CommonEventSubscriber commonEventSubscriber) throws RemoteException {
        if (commonEventData != null) {
            ICommonEventManager service = CommonEventManagerHelper.getService();
            if (service != null) {
                ICommonEventSubscriber iCommonEventSubscriber = null;
                if (commonEventSubscriber != null) {
                    iCommonEventSubscriber = commonEventSubscriber.getSubscriber();
                }
                service.publishCommonEvent(commonEventData, new CommonEventPublishInfo(commonEventPublishInfo), iCommonEventSubscriber);
                return;
            }
            HiLog.warn(LABEL, "CommonEventManger service is not available for publishCommonEvent.", new Object[0]);
            return;
        }
        throw new IllegalArgumentException("data can not be null.");
    }
}
