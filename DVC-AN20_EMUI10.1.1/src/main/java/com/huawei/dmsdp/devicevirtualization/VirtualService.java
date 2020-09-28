package com.huawei.dmsdp.devicevirtualization;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPAdapterCallback;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.IDMSDPAdapter;

/* access modifiers changed from: package-private */
public class VirtualService {
    private static final Object SERVICE_LOCK = new Object();
    private static final String TAG = "VirtualService";
    private static IDmsdpServiceCallback sCallback;
    private static Context sContext;
    private static VirtualService sDmsdpService;
    private DMSDPAdapter mDMSDPAdapter;

    private VirtualService(DMSDPAdapter adapter) {
        synchronized (SERVICE_LOCK) {
            this.mDMSDPAdapter = adapter;
            IDMSDPAdapter dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdp.devicevirtualization.VirtualService.AnonymousClass1 */

                        public void binderDied() {
                            HwLog.i(VirtualService.TAG, "CarAdapter onBinderDied");
                            if (VirtualService.sCallback != null) {
                                VirtualService.sCallback.onBinderDied();
                            }
                            VirtualService.releaseInstance();
                            DMSDPAdapter.disableVirtualAudio();
                        }
                    }, 0);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "Call service linkToDeath RemoteException");
                }
            }
        }
    }

    public static void createInstance(Context context, IDmsdpServiceCallback callback) {
        synchronized (SERVICE_LOCK) {
            HwLog.i(TAG, "Dmsdp service createInstance");
            if (callback == null) {
                HwLog.e(TAG, "createInstance callback null");
                throw new IllegalArgumentException("createInstance callback null");
            } else if (sCallback != null) {
                HwLog.e(TAG, "call back already register");
            } else {
                sContext = context;
                sCallback = callback;
                if (sDmsdpService != null) {
                    HwLog.d(TAG, "createInstance callback has been exist");
                    callback.onAdapterGet(sDmsdpService);
                    return;
                }
                DMSDPAdapter.createInstance(context, new DMSDPAdapterCallback() {
                    /* class com.huawei.dmsdp.devicevirtualization.VirtualService.AnonymousClass2 */

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onAdapterGet(DMSDPAdapter adapter) {
                        synchronized (VirtualService.SERVICE_LOCK) {
                            HwLog.i(VirtualService.TAG, "Dmsdp service onAdapterGet " + adapter);
                            if (adapter != null) {
                                if (VirtualService.sCallback != null) {
                                    VirtualService unused = VirtualService.sDmsdpService = new VirtualService(adapter);
                                    VirtualService.sCallback.onAdapterGet(VirtualService.sDmsdpService);
                                } else {
                                    HwLog.e(VirtualService.TAG, "sCallback is null when onAdapterGet");
                                }
                            }
                        }
                    }

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onBinderDied() {
                        synchronized (VirtualService.SERVICE_LOCK) {
                            HwLog.i(VirtualService.TAG, "onBinderDied");
                            if (VirtualService.sCallback != null) {
                                VirtualService.sCallback.onBinderDied();
                            }
                            VirtualService.releaseInstance();
                        }
                    }
                });
            }
        }
    }

    public static void releaseInstance() {
        synchronized (SERVICE_LOCK) {
            HwLog.i(TAG, "Dmsdp service releaseInstance");
            sDmsdpService = null;
            sCallback = null;
            DMSDPAdapter.releaseInstance();
            HwLog.i(TAG, "Dmsdp service releaseInstance end");
        }
    }

    public DMSDPAdapter getDMSDPAdapter() {
        return this.mDMSDPAdapter;
    }

    public Context getContext() {
        return sContext;
    }
}
