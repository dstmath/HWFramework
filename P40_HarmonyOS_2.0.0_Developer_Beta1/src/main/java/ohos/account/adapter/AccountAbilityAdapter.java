package ohos.account.adapter;

import android.app.IActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.os.RemoteException;
import android.os.ServiceManager;
import ohos.account.IOsAccountSubscriber;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AccountAbilityAdapter {
    private static final String APP_SERVICE_NAME = "activity";
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final int LOG_DOMAIN = 218110720;
    private static final String TAG = "AccountAbilityAdapter";
    private volatile IActivityManager appService = null;
    private final Object remoteLock = new Object();

    private IActivityManager getApplicationService() {
        if (this.appService != null) {
            return this.appService;
        }
        synchronized (this.remoteLock) {
            if (this.appService == null) {
                try {
                    this.appService = IActivityManager.Stub.asInterface(ServiceManager.getServiceOrThrow(APP_SERVICE_NAME));
                    if (this.appService == null) {
                        HiLog.error(LABEL, "ServiceManager get failed", new Object[0]);
                        return this.appService;
                    }
                } catch (ServiceManager.ServiceNotFoundException unused) {
                    HiLog.error(LABEL, "get app service not found exception", new Object[0]);
                    return this.appService;
                }
            }
            return this.appService;
        }
    }

    public void subscribeOsAccountEvent(final IOsAccountSubscriber iOsAccountSubscriber, String str) {
        HiLog.debug(LABEL, "subscribe account event", new Object[0]);
        if (iOsAccountSubscriber == null || str == null) {
            HiLog.error(LABEL, "subscriber or name error", new Object[0]);
            return;
        }
        IActivityManager applicationService = getApplicationService();
        if (applicationService == null) {
            HiLog.error(LABEL, "getApplicationService failed", new Object[0]);
            return;
        }
        try {
            applicationService.registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                /* class ohos.account.adapter.AccountAbilityAdapter.AnonymousClass1 */

                public void onUserSwitching(int i) throws RemoteException {
                    iOsAccountSubscriber.onAccountActivating(i);
                }

                public void onUserSwitchComplete(int i) throws RemoteException {
                    iOsAccountSubscriber.onAccountActivated(i);
                }
            }, str);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "register user switch observer RemoteException", new Object[0]);
        }
    }
}
