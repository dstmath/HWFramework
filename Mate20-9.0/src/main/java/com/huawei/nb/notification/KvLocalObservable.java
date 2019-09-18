package com.huawei.nb.notification;

import android.os.RemoteException;
import com.huawei.nb.client.callback.CallbackManager;
import com.huawei.nb.client.callback.KvSubscribeCallback;
import com.huawei.nb.notification.IKvObserver;
import com.huawei.nb.service.IKvServiceCall;
import com.huawei.nb.utils.logger.DSLog;
import java.util.List;

public class KvLocalObservable extends LocalObservable<KeyObserverInfo, KeyObserver, IKvServiceCall> {
    private static final String TAG = "KvLocalObservable";
    private static final long TIMEOUT_MILLISECONDS = 1500;
    private final CallbackManager callbackManager;

    protected class RemoteKvObserver extends IKvObserver.Stub {
        private KeyObserverInfo info;

        RemoteKvObserver(KeyObserverInfo info2) {
            this.info = info2;
        }

        public void notify(final ChangeNotification changeNotification) throws RemoteException {
            if (changeNotification != null && changeNotification.getType() != null) {
                final List<KeyObserver> observers = KvLocalObservable.this.getObservers(this.info);
                if (observers != null) {
                    new Thread(new Runnable() {
                        public void run() {
                            for (KeyObserver observer : observers) {
                                observer.onKeyChanged(changeNotification);
                            }
                        }
                    }).start();
                }
            }
        }
    }

    public KvLocalObservable(CallbackManager callbackManager2) {
        this.callbackManager = callbackManager2;
    }

    /* access modifiers changed from: protected */
    public boolean registerModelRemoteObserver(KeyObserverInfo observerInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to register remote observer, error: not connected to data service.", new Object[0]);
            return false;
        }
        RemoteKvObserver remoteModelObserver = new RemoteKvObserver(observerInfo);
        KvSubscribeCallback subscribeCallback = (KvSubscribeCallback) this.callbackManager.createCallBack(KvLocalObservable$$Lambda$0.$instance);
        try {
            return subscribeCallback.await(((IKvServiceCall) getRemoteService()).registerObserver(observerInfo, remoteModelObserver, subscribeCallback), (long) TIMEOUT_MILLISECONDS).booleanValue();
        } catch (RemoteException | RuntimeException e) {
            DSLog.et(TAG, "Failed to register observer for %s, error: %s.", observerInfo, e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean unregisterModelRemoteObserver(KeyObserverInfo observerInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to unregister remote observer, error: not connected to data service.", new Object[0]);
            return false;
        }
        RemoteKvObserver remoteModelObserver = new RemoteKvObserver(observerInfo);
        KvSubscribeCallback subscribeCallback = (KvSubscribeCallback) this.callbackManager.createCallBack(KvLocalObservable$$Lambda$1.$instance);
        try {
            return subscribeCallback.await(((IKvServiceCall) getRemoteService()).unRegisterObserver(observerInfo, remoteModelObserver, subscribeCallback), (long) TIMEOUT_MILLISECONDS).booleanValue();
        } catch (RemoteException | RuntimeException e) {
            DSLog.et(TAG, "Failed to unregister model observer for %s, error: %s.", observerInfo, e.getMessage());
            return false;
        }
    }
}
