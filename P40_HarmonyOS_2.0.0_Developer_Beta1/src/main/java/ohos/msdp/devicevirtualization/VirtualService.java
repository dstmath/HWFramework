package ohos.msdp.devicevirtualization;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import ohos.dmsdp.sdk.DMSDPAdapter;
import ohos.dmsdp.sdk.DMSDPAdapterCallback;
import ohos.dmsdp.sdk.HwLog;

/* access modifiers changed from: package-private */
public class VirtualService {
    private static final Object SERVICE_LOCK = new Object();
    private static final String TAG = "VirtualService";
    private static IDmsdpServiceCallback sCallback;
    private static Context sContext;
    private static IDmsdpServiceCallback sDeviceCallback;
    private static VirtualService sDmsdpDeviceService;
    private static VirtualService sDmsdpService;
    private DMSDPAdapter mDMSDPAdapter;

    private VirtualService(DMSDPAdapter dMSDPAdapter) {
        synchronized (SERVICE_LOCK) {
            this.mDMSDPAdapter = dMSDPAdapter;
            IInterface dMSDPService = dMSDPAdapter.getDMSDPService();
            if (dMSDPService != null) {
                try {
                    dMSDPService.asBinder().linkToDeath(new Recipient(), 0);
                } catch (RemoteException unused) {
                    HwLog.e(TAG, "Call service linkToDeath RemoteException");
                }
            }
        }
    }

    public static void createInstance(Context context, final IDmsdpServiceCallback iDmsdpServiceCallback) {
        synchronized (SERVICE_LOCK) {
            HwLog.i(TAG, "Dmsdp service createInstance");
            if (iDmsdpServiceCallback == null) {
                HwLog.e(TAG, "createInstance callback null");
                throw new IllegalArgumentException("createInstance callback null");
            } else if (sCallback != null) {
                HwLog.e(TAG, "call back already register");
            } else {
                sContext = context;
                sCallback = iDmsdpServiceCallback;
                if (sDmsdpService != null) {
                    HwLog.d(TAG, "createInstance callback has been exist");
                    iDmsdpServiceCallback.onAdapterGet(sDmsdpService);
                    return;
                }
                DMSDPAdapter.createInstance(context, new DMSDPAdapterCallback() {
                    /* class ohos.msdp.devicevirtualization.VirtualService.AnonymousClass1 */

                    public void onAdapterGet(DMSDPAdapter dMSDPAdapter) {
                        synchronized (VirtualService.SERVICE_LOCK) {
                            HwLog.i(VirtualService.TAG, "Dmsdp service onAdapterGet " + dMSDPAdapter);
                            if (dMSDPAdapter != null) {
                                VirtualService unused = VirtualService.sDmsdpService = new VirtualService(dMSDPAdapter);
                                VirtualService.sCallback.onAdapterGet(VirtualService.sDmsdpService);
                            }
                        }
                    }

                    public void onBinderDied() {
                        HwLog.i(VirtualService.TAG, "onBinderDied");
                        IDmsdpServiceCallback.this.onBinderDied();
                        VirtualService.releaseInstance();
                    }
                });
            }
        }
    }

    public static void createDeviceInstance(Context context, final IDmsdpServiceCallback iDmsdpServiceCallback, boolean z) {
        synchronized (SERVICE_LOCK) {
            HwLog.i(TAG, "Dmsdp service createInstance");
            if (iDmsdpServiceCallback != null) {
                sContext = context;
                if (z) {
                    HwLog.i(TAG, "enter create dmsdp device instance");
                    if (sDeviceCallback != null) {
                        HwLog.e(TAG, "dmsdp device service call back already register");
                        return;
                    }
                    sDeviceCallback = iDmsdpServiceCallback;
                    sContext = context;
                    if (sDmsdpDeviceService != null) {
                        HwLog.d(TAG, "createInstance callback has been exist");
                        iDmsdpServiceCallback.onAdapterGet(sDmsdpDeviceService);
                        return;
                    }
                    DMSDPAdapter.createAgentInstance(context, new DMSDPAdapterCallback() {
                        /* class ohos.msdp.devicevirtualization.VirtualService.AnonymousClass2 */

                        public void onAdapterGet(DMSDPAdapter dMSDPAdapter) {
                            synchronized (VirtualService.SERVICE_LOCK) {
                                HwLog.i(VirtualService.TAG, "Dmsdp service onAdapterGet " + dMSDPAdapter);
                                if (dMSDPAdapter != null) {
                                    VirtualService unused = VirtualService.sDmsdpDeviceService = new VirtualService(dMSDPAdapter);
                                    VirtualService.sDeviceCallback.onAdapterGet(VirtualService.sDmsdpDeviceService);
                                }
                            }
                        }

                        public void onBinderDied() {
                            HwLog.i(VirtualService.TAG, "onBinderDied");
                            IDmsdpServiceCallback.this.onBinderDied();
                            VirtualService.releaseInstance();
                        }
                    });
                } else if (sCallback != null) {
                    HwLog.e(TAG, "dmsdpservice call back already register");
                    return;
                } else {
                    sCallback = iDmsdpServiceCallback;
                    if (sDmsdpService != null) {
                        HwLog.d(TAG, "createInstance callback has been exist");
                        iDmsdpServiceCallback.onAdapterGet(sDmsdpService);
                        return;
                    }
                    DMSDPAdapter.createInstance(context, new DMSDPAdapterCallback() {
                        /* class ohos.msdp.devicevirtualization.VirtualService.AnonymousClass3 */

                        public void onAdapterGet(DMSDPAdapter dMSDPAdapter) {
                            synchronized (VirtualService.SERVICE_LOCK) {
                                HwLog.i(VirtualService.TAG, "Dmsdp service onAdapterGet " + dMSDPAdapter);
                                if (dMSDPAdapter != null) {
                                    VirtualService unused = VirtualService.sDmsdpService = new VirtualService(dMSDPAdapter);
                                    VirtualService.sCallback.onAdapterGet(VirtualService.sDmsdpService);
                                }
                            }
                        }

                        public void onBinderDied() {
                            HwLog.i(VirtualService.TAG, "onBinderDied");
                            IDmsdpServiceCallback.this.onBinderDied();
                            VirtualService.releaseInstance();
                        }
                    });
                }
                return;
            }
            HwLog.e(TAG, "createInstance callback null");
            throw new IllegalArgumentException("createInstance callback null");
        }
    }

    public static void releaseInstance() {
        synchronized (SERVICE_LOCK) {
            HwLog.i(TAG, "Dmsdp service releaseInstance");
            sDmsdpService = null;
            sDmsdpDeviceService = null;
            sCallback = null;
            sDeviceCallback = null;
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

    private static class Recipient implements IBinder.DeathRecipient {
        Recipient() {
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            HwLog.i(VirtualService.TAG, "CarAdapter onBinderDied");
            if (VirtualService.sCallback != null) {
                VirtualService.sCallback.onBinderDied();
            }
            if (VirtualService.sDeviceCallback != null) {
                VirtualService.sDeviceCallback.onBinderDied();
            }
            VirtualService.releaseInstance();
        }
    }
}
