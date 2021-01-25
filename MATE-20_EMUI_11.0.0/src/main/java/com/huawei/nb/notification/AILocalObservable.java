package com.huawei.nb.notification;

import android.os.RemoteException;
import com.huawei.nb.notification.IAIObserver;
import com.huawei.nb.service.IAIServiceCall;
import com.huawei.nb.utils.logger.DSLog;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AILocalObservable extends LocalObservable<ModelObserverInfo, ModelObserver, IAIServiceCall> {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    /* access modifiers changed from: protected */
    public boolean registerModelRemoteObserver(ModelObserverInfo modelObserverInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to register remote observer, error: not connected to ai service.", new Object[0]);
            return false;
        }
        DSLog.d("Register observer for Model to ai service.", new Object[0]);
        try {
            if (((IAIServiceCall) getRemoteService()).registerObserver(modelObserverInfo, new RemoteModelObserver(modelObserverInfo), null) == 0) {
                return true;
            }
            return false;
        } catch (RemoteException | RuntimeException e) {
            DSLog.e("Failed to register observer for %s, error: %s.", modelObserverInfo, e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean unregisterModelRemoteObserver(ModelObserverInfo modelObserverInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to unregister remote observer, error: not connected to ai service.", new Object[0]);
            return false;
        }
        DSLog.d("Unregister observer for Model to ai service.", new Object[0]);
        try {
            if (((IAIServiceCall) getRemoteService()).unregisterObserver(modelObserverInfo, new RemoteModelObserver(modelObserverInfo), null) == 0) {
                return true;
            }
            return false;
        } catch (RemoteException | RuntimeException e) {
            DSLog.e("Failed to unregister model observer for %s, error: %s.", modelObserverInfo, e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: private */
    public final class RemoteModelObserver extends IAIObserver.Stub {
        private ModelObserverInfo mInfo;

        private RemoteModelObserver(ModelObserverInfo modelObserverInfo) {
            this.mInfo = modelObserverInfo;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:3:0x0009, code lost:
            r0 = r3.this$0.getObservers(r3.mInfo);
         */
        @Override // com.huawei.nb.notification.IAIObserver
        public void notify(final ChangeNotification changeNotification) throws RemoteException {
            final List observers;
            if (changeNotification != null && changeNotification.getType() != null && observers != null) {
                AILocalObservable.EXECUTOR_SERVICE.execute(new Runnable() {
                    /* class com.huawei.nb.notification.AILocalObservable.RemoteModelObserver.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        for (ModelObserver modelObserver : observers) {
                            modelObserver.onModelChanged(changeNotification);
                        }
                    }
                });
            }
        }
    }
}
