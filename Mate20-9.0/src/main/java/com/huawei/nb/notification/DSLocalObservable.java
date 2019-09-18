package com.huawei.nb.notification;

import android.os.RemoteException;
import com.huawei.nb.client.callback.CallbackManager;
import com.huawei.nb.client.callback.SubscribeCallback;
import com.huawei.nb.coordinator.common.CoordinatorJsonAnalyzer;
import com.huawei.nb.notification.IModelObserver;
import com.huawei.nb.service.IDataServiceCall;
import com.huawei.nb.utils.logger.DSLog;
import java.util.List;

public class DSLocalObservable extends LocalObservable<ModelObserverInfo, ModelObserver, IDataServiceCall> {
    private static final long TIMEOUT_MILLISECONDS = 1500;
    private final CallbackManager callbackManager;

    private class RemoteModelObserver extends IModelObserver.Stub {
        private ModelObserverInfo info;

        private RemoteModelObserver(ModelObserverInfo info2) {
            this.info = info2;
        }

        public void notify(final ChangeNotification changeNotification) throws RemoteException {
            if (changeNotification != null && changeNotification.getType() != null) {
                final List<ModelObserver> observers = DSLocalObservable.this.getObservers(this.info);
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

    public DSLocalObservable(CallbackManager callbackManager2) {
        this.callbackManager = callbackManager2;
    }

    /* access modifiers changed from: protected */
    public boolean registerModelRemoteObserver(ModelObserverInfo observerInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to register remote observer, error: not connected to data service.", new Object[0]);
            return false;
        }
        DSLog.d("Register observer for %s to remote service.", observerInfo.getModelClazz().getSimpleName());
        RemoteModelObserver remoteModelObserver = new RemoteModelObserver(observerInfo);
        SubscribeCallback subscribeCallback = (SubscribeCallback) this.callbackManager.createCallBack(DSLocalObservable$$Lambda$0.$instance);
        try {
            return subscribeCallback.await(((IDataServiceCall) getRemoteService()).registerModelObserver(observerInfo, remoteModelObserver, subscribeCallback), (long) TIMEOUT_MILLISECONDS).booleanValue();
        } catch (RemoteException | RuntimeException e) {
            DSLog.e("Failed to register observer for %s, error: %s.", observerInfo, e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean unregisterModelRemoteObserver(ModelObserverInfo observerInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to unregister remote observer, error: not connected to data service.", new Object[0]);
            return false;
        }
        Object[] objArr = new Object[2];
        objArr[0] = observerInfo.getType() == ObserverType.OBSERVER_MODEL ? "model" : CoordinatorJsonAnalyzer.MSG_TYPE;
        objArr[1] = observerInfo.getModelClazz().getSimpleName();
        DSLog.d("Unregister %s observer for %s to remote service.", objArr);
        RemoteModelObserver remoteModelObserver = new RemoteModelObserver(observerInfo);
        SubscribeCallback subscribeCallback = (SubscribeCallback) this.callbackManager.createCallBack(DSLocalObservable$$Lambda$1.$instance);
        try {
            return subscribeCallback.await(((IDataServiceCall) getRemoteService()).unregisterModelObserver(observerInfo, remoteModelObserver, subscribeCallback), (long) TIMEOUT_MILLISECONDS).booleanValue();
        } catch (RemoteException | RuntimeException e) {
            DSLog.e("Failed to unregister model observer for %s, error: %s.", observerInfo, e.getMessage());
            return false;
        }
    }
}
