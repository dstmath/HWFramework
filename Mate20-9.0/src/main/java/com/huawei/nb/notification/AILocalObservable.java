package com.huawei.nb.notification;

import android.os.RemoteException;
import com.huawei.nb.client.callback.AISubscribeCallback;
import com.huawei.nb.client.callback.CallbackManager;
import com.huawei.nb.notification.IAIObserver;
import com.huawei.nb.service.IAIServiceCall;
import com.huawei.nb.utils.logger.DSLog;
import java.util.List;

public class AILocalObservable extends LocalObservable<ModelObserverInfo, ModelObserver, IAIServiceCall> {
    private static final long TIMEOUT_MILLISECONDS = 1500;
    private final CallbackManager mCallbackManager;

    private final class RemoteModelObserver extends IAIObserver.Stub {
        private ModelObserverInfo mInfo;

        private RemoteModelObserver(ModelObserverInfo info) {
            this.mInfo = info;
        }

        public void notify(final ChangeNotification changeNotification) throws RemoteException {
            if (changeNotification != null && changeNotification.getType() != null) {
                final List<ModelObserver> observers = AILocalObservable.this.getObservers(this.mInfo);
                if (observers != null) {
                    new Thread(new Runnable() {
                        public void run() {
                            for (ModelObserver observer : observers) {
                                observer.onModelChanged(changeNotification);
                            }
                        }
                    }).start();
                }
            }
        }
    }

    public AILocalObservable(CallbackManager callbackManager) {
        this.mCallbackManager = callbackManager;
    }

    /* access modifiers changed from: protected */
    public boolean registerModelRemoteObserver(ModelObserverInfo observerInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to register remote observer, error: not connected to ai service.", new Object[0]);
            return false;
        }
        DSLog.d("Register observer for AiModel to ai service.", new Object[0]);
        RemoteModelObserver remoteModelObserver = new RemoteModelObserver(observerInfo);
        AISubscribeCallback subscribeCallback = (AISubscribeCallback) this.mCallbackManager.createCallBack(AILocalObservable$$Lambda$0.$instance);
        try {
            return subscribeCallback.await(((IAIServiceCall) getRemoteService()).registerObserver(observerInfo, remoteModelObserver, subscribeCallback), (long) TIMEOUT_MILLISECONDS).booleanValue();
        } catch (RemoteException | RuntimeException e) {
            DSLog.e("Failed to register observer for %s, error: %s.", observerInfo, e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean unregisterModelRemoteObserver(ModelObserverInfo observerInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to unregister remote observer, error: not connected to ai service.", new Object[0]);
            return false;
        }
        DSLog.d("Unregister observer for AiModel to ai service.", new Object[0]);
        RemoteModelObserver remoteModelObserver = new RemoteModelObserver(observerInfo);
        AISubscribeCallback subscribeCallback = (AISubscribeCallback) this.mCallbackManager.createCallBack(AILocalObservable$$Lambda$1.$instance);
        try {
            return subscribeCallback.await(((IAIServiceCall) getRemoteService()).unregisterObserver(observerInfo, remoteModelObserver, subscribeCallback), (long) TIMEOUT_MILLISECONDS).booleanValue();
        } catch (RemoteException | RuntimeException e) {
            DSLog.e("Failed to unregister model observer for %s, error: %s.", observerInfo, e.getMessage());
            return false;
        }
    }
}
