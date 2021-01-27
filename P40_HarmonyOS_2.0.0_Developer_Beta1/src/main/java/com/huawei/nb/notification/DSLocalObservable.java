package com.huawei.nb.notification;

import android.os.RemoteException;
import com.huawei.nb.notification.DSLocalObservable;
import com.huawei.nb.notification.IModelObserver;
import com.huawei.nb.service.IDataServiceCall;
import com.huawei.nb.utils.logger.DSLog;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DSLocalObservable extends LocalObservable<ModelObserverInfo, ModelObserver, IDataServiceCall> {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final int SUCCESS = 0;

    /* access modifiers changed from: protected */
    public boolean registerModelRemoteObserver(ModelObserverInfo modelObserverInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to register observer, error: not connected to data service.", new Object[0]);
            return false;
        }
        try {
            if (((IDataServiceCall) getRemoteService()).registerModelObserver(modelObserverInfo, new RemoteModelObserver(modelObserverInfo), null) == 0) {
                DSLog.d("Register observer for %s.", modelObserverInfo.getModelClazz().getSimpleName());
                return true;
            }
            DSLog.e("Failed to register observer for %s.", modelObserverInfo.getModelClazz().getSimpleName());
            return false;
        } catch (RemoteException | RuntimeException e) {
            DSLog.e("Failed to register observer for %s, error: %s.", modelObserverInfo, e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean unregisterModelRemoteObserver(ModelObserverInfo modelObserverInfo) {
        if (getRemoteService() == null) {
            DSLog.e("Failed to unregister observer, error: not connected to data service.", new Object[0]);
            return false;
        }
        try {
            if (((IDataServiceCall) getRemoteService()).unregisterModelObserver(modelObserverInfo, new RemoteModelObserver(modelObserverInfo), null) == 0) {
                DSLog.d("Unregister observer for %s.", modelObserverInfo.getModelClazz().getSimpleName());
                return true;
            }
            DSLog.e("Failed to unregister observer for %s.", modelObserverInfo.getModelClazz().getSimpleName());
            return false;
        } catch (RemoteException | RuntimeException e) {
            DSLog.e("Failed to unregister observer for %s, error: %s.", modelObserverInfo, e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class RemoteModelObserver extends IModelObserver.Stub {
        private ModelObserverInfo info;

        private RemoteModelObserver(ModelObserverInfo modelObserverInfo) {
            this.info = modelObserverInfo;
        }

        @Override // com.huawei.nb.notification.IModelObserver
        public void notify(ChangeNotification changeNotification) {
            List observers;
            if (changeNotification != null && changeNotification.getType() != null && (observers = DSLocalObservable.this.getObservers(this.info)) != null) {
                DSLocalObservable.EXECUTOR_SERVICE.execute(new Runnable(observers, changeNotification) {
                    /* class com.huawei.nb.notification.$$Lambda$DSLocalObservable$RemoteModelObserver$GbUmMkdPkT1K4bGk_Wxk4esv1Q */
                    private final /* synthetic */ List f$0;
                    private final /* synthetic */ ChangeNotification f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        DSLocalObservable.RemoteModelObserver.lambda$notify$0(this.f$0, this.f$1);
                    }
                });
            }
        }

        static /* synthetic */ void lambda$notify$0(List list, ChangeNotification changeNotification) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                ((ModelObserver) it.next()).onModelChanged(changeNotification);
            }
        }
    }
}
