package ohos.miscservices.area;

import android.location.Country;
import android.location.ICountryDetector;
import android.location.ICountryListener;
import android.os.ServiceManager;
import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.area.AreaProxyImpl;
import ohos.rpc.RemoteException;

public class AreaProxyImpl {
    private static final String COUNTRY_SERVICE = "country_detector";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "AreaProxyImpl");
    private ICountryListener.Stub mAdapterListener = new ICountryListener.Stub() {
        /* class ohos.miscservices.area.AreaProxyImpl.AnonymousClass1 */

        public void onCountryDetected(Country country) {
            if (AreaProxyImpl.this.mContext == null) {
                HiLog.error(AreaProxyImpl.TAG, "Context is not ready!", new Object[0]);
            } else if (country == null) {
                HiLog.error(AreaProxyImpl.TAG, "Callback area is not ready!", new Object[0]);
            } else {
                TaskDispatcher mainTaskDispatcher = AreaProxyImpl.this.mContext.getMainTaskDispatcher();
                if (mainTaskDispatcher == null) {
                    HiLog.error(AreaProxyImpl.TAG, "Context dispatcher is not ready!", new Object[0]);
                } else {
                    mainTaskDispatcher.asyncDispatch(new Runnable(country) {
                        /* class ohos.miscservices.area.$$Lambda$AreaProxyImpl$1$MtlIkL_vt4vryf3XtlPGzNBQovs */
                        private final /* synthetic */ Country f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            AreaProxyImpl.AnonymousClass1.this.lambda$onCountryDetected$0$AreaProxyImpl$1(this.f$1);
                        }
                    });
                }
            }
        }

        public /* synthetic */ void lambda$onCountryDetected$0$AreaProxyImpl$1(Country country) {
            synchronized (this) {
                if (AreaProxyImpl.this.mIAreacb != null) {
                    AreaProxyImpl.this.mIAreacb.notifyChanged(country.getCountryIso());
                }
            }
        }
    };
    private Context mContext;
    private OnChangedCallback mIAreacb = null;
    private ICountryDetector mService;

    /* access modifiers changed from: protected */
    public interface OnChangedCallback {
        void notifyChanged(String str);
    }

    AreaProxyImpl(Context context) {
        this.mContext = context;
        tryInit();
    }

    private boolean tryInit() {
        this.mService = ICountryDetector.Stub.asInterface(ServiceManager.getService(COUNTRY_SERVICE));
        return this.mService != null;
    }

    public String getISOAlpha2Code() throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                Country detectCountry = this.mService.detectCountry();
                if (detectCountry == null) {
                    return "";
                }
                HiLog.info(TAG, "Get area from service success!", new Object[0]);
                return detectCountry.getCountryIso();
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "Connect area service IPC failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "Get area service failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void addAreaListener() throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                this.mService.addCountryListener(this.mAdapterListener);
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "Connect area service IPC failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "Get area service failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void removeAreaListener() throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                this.mService.removeCountryListener(this.mAdapterListener);
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "Connect area service IPC failed!", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "Get area service failed!", new Object[0]);
            throw new RemoteException();
        }
    }

    /* access modifiers changed from: protected */
    public void setAreaChangedCb(OnChangedCallback onChangedCallback) {
        synchronized (this) {
            if (this.mIAreacb == null) {
                HiLog.info(TAG, "SystemArea set listener callback", new Object[0]);
                this.mIAreacb = onChangedCallback;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeAreaChangedCb() {
        synchronized (this) {
            this.mIAreacb = null;
        }
    }
}
