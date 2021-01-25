package com.huawei.nb.notification;

import android.os.RemoteException;
import com.huawei.nb.notification.IKvObserver;
import com.huawei.nb.notification.KvLocalObservable;
import com.huawei.nb.service.IKvServiceCall;
import com.huawei.nb.utils.logger.DSLog;
import java.util.Iterator;
import java.util.List;

public class KvLocalObservable extends LocalObservable<KeyObserverInfo, KeyObserver, IKvServiceCall> {
    private static final int SUCCESS = 0;
    private static final String TAG = "KvLocalObservable";

    /* access modifiers changed from: protected */
    public boolean registerModelRemoteObserver(KeyObserverInfo keyObserverInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to register remote observer, error: not connected to data service.", new Object[0]);
            return false;
        }
        try {
            if (((IKvServiceCall) getRemoteService()).registerObserver(keyObserverInfo, new RemoteKvObserver(keyObserverInfo), null) == 0) {
                DSLog.d(TAG, "Register observer for %s.", keyObserverInfo.getKey());
                return true;
            }
            DSLog.e(TAG, "Failed to register observer for %s.", keyObserverInfo.getKey());
            return false;
        } catch (RemoteException | RuntimeException e) {
            DSLog.et(TAG, "Failed to register observer for %s, error: %s.", keyObserverInfo, e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean unregisterModelRemoteObserver(KeyObserverInfo keyObserverInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to unregister remote observer, error: not connected to data service.", new Object[0]);
            return false;
        }
        try {
            if (((IKvServiceCall) getRemoteService()).unRegisterObserver(keyObserverInfo, new RemoteKvObserver(keyObserverInfo), null) == 0) {
                DSLog.d(TAG, "Unregister observer for %s.", keyObserverInfo.getKey());
                return true;
            }
            DSLog.e(TAG, "Failed to unregister observer for %s.", keyObserverInfo.getKey());
            return false;
        } catch (RemoteException | RuntimeException e) {
            DSLog.et(TAG, "Failed to unregister model observer for %s, error: %s.", keyObserverInfo, e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public class RemoteKvObserver extends IKvObserver.Stub {
        private KeyObserverInfo info;

        RemoteKvObserver(KeyObserverInfo keyObserverInfo) {
            this.info = keyObserverInfo;
        }

        @Override // com.huawei.nb.notification.IKvObserver
        public void notify(ChangeNotification changeNotification) throws RemoteException {
            List observers;
            if (changeNotification != null && changeNotification.getType() != null && (observers = KvLocalObservable.this.getObservers(this.info)) != null) {
                new Thread(new Runnable(observers, changeNotification) {
                    /* class com.huawei.nb.notification.$$Lambda$KvLocalObservable$RemoteKvObserver$5ESoiueOrl5hl2qElASU6nBeW58 */
                    private final /* synthetic */ List f$0;
                    private final /* synthetic */ ChangeNotification f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        KvLocalObservable.RemoteKvObserver.lambda$notify$0(this.f$0, this.f$1);
                    }
                }).start();
            }
        }

        static /* synthetic */ void lambda$notify$0(List list, ChangeNotification changeNotification) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                ((KeyObserver) it.next()).onKeyChanged(changeNotification);
            }
        }
    }
}
