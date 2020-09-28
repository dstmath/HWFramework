package com.huawei.dmsdpsdk2.pad;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPAdapterCallback;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.DMSDPVirtualDevice;
import com.huawei.dmsdpsdk2.DataListener;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.IDMSDPAdapter;
import java.util.List;
import java.util.Map;

public final class PadAdapter {
    private static final Object PAD_LOCK = new Object();
    private static final String TAG = "PadAdapter";
    private static PadAdapter padAdapter;
    private static PadAdapterCallback padAdapterCallback;
    private DMSDPAdapter mDMSDPAdapter;

    public interface PadAdapterCallback {
        void onAdapterGet(PadAdapter padAdapter);

        void onBinderDied();
    }

    private PadAdapter(DMSDPAdapter adapter) {
        synchronized (PAD_LOCK) {
            this.mDMSDPAdapter = adapter;
            IDMSDPAdapter dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdpsdk2.pad.PadAdapter.AnonymousClass1 */

                        public void binderDied() {
                            HwLog.i(PadAdapter.TAG, "PadAdapter onBinderDied");
                            if (PadAdapter.padAdapterCallback != null) {
                                PadAdapter.padAdapterCallback.onBinderDied();
                            }
                            PadAdapter.releaseInstance();
                            DMSDPAdapter.disableVirtualAudio();
                        }
                    }, 0);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "Call service linkToDeath RemoteException");
                }
            }
        }
    }

    public static void createInstance(Context context, final PadAdapterCallback callback) {
        synchronized (PAD_LOCK) {
            HwLog.i(TAG, "PadAdapter createInstance");
            if (callback != null) {
                padAdapterCallback = callback;
                if (padAdapter != null) {
                    HwLog.d(TAG, "createInstance callback has been exist");
                    callback.onAdapterGet(padAdapter);
                    return;
                }
                DMSDPAdapter.createInstance(context, new DMSDPAdapterCallback() {
                    /* class com.huawei.dmsdpsdk2.pad.PadAdapter.AnonymousClass2 */

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onAdapterGet(DMSDPAdapter adapter) {
                        synchronized (PadAdapter.PAD_LOCK) {
                            HwLog.w(PadAdapter.TAG, "PadAdapter onAdapterGet " + adapter);
                            if (adapter == null) {
                                callback.onBinderDied();
                                PadAdapter.releaseInstance();
                                return;
                            }
                            PadAdapter unused = PadAdapter.padAdapter = new PadAdapter(adapter);
                            callback.onAdapterGet(PadAdapter.padAdapter);
                        }
                    }

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onBinderDied() {
                    }
                });
                return;
            }
            HwLog.e(TAG, "createInstance callback null");
            throw new IllegalArgumentException("createInstance callback null");
        }
    }

    public static void releaseInstance() {
        synchronized (PAD_LOCK) {
            HwLog.w(TAG, "PadAdapter releaseInstance");
            padAdapter = null;
            padAdapterCallback = null;
            DMSDPAdapter.releaseInstance();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        HwLog.w(TAG, "PadAdapter finalize");
        super.finalize();
    }

    public int registerDMSDPListener(DMSDPListener listener) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.registerDMSDPListener(3, listener);
        }
    }

    public int unRegisterDMSDPListener(DMSDPListener listener) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.unRegisterDMSDPListener(3, listener);
        }
    }

    public int registerDataListener(DMSDPDevice device, int dataType, DataListener listener) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.registerDataListener(3, device, dataType, listener);
        }
    }

    public int unRegisterDataListener(DMSDPDevice device, int dataType) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.unRegisterDataListener(3, device, dataType);
        }
    }

    public int sendData(DMSDPDevice device, int dataType, byte[] data) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.sendData(3, device, dataType, data);
        }
    }

    public int connectDevice(DMSDPDevice device) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.connectDevice(3, 2, device, null);
        }
    }

    public int disconnectDevice(DMSDPDevice device) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.disconnectDevice(3, 2, device);
        }
    }

    public int requestDeviceService(DMSDPDevice device, int type) {
        synchronized (PAD_LOCK) {
            HwLog.i(TAG, "requestDeviceService start");
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.requestDeviceService(3, device, type);
        }
    }

    public int startDeviceService(DMSDPDeviceService service) {
        synchronized (PAD_LOCK) {
            HwLog.i(TAG, "startDeviceService start");
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            } else if (service == null) {
                HwLog.e(TAG, "service is null");
                return -2;
            } else if (service.getServiceType() == 1) {
                return this.mDMSDPAdapter.startDeviceService(3, service, 1, null);
            } else {
                return this.mDMSDPAdapter.startDeviceService(3, service, 0, null);
            }
        }
    }

    public int stopDeviceService(DMSDPDeviceService service) {
        synchronized (PAD_LOCK) {
            HwLog.i(TAG, "stopDeviceService start");
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            } else if (service == null) {
                HwLog.e(TAG, "service is null");
                return -2;
            } else if (service.getServiceType() == 1) {
                return this.mDMSDPAdapter.stopDeviceService(3, service, 1);
            } else {
                return this.mDMSDPAdapter.stopDeviceService(3, service, 0);
            }
        }
    }

    public int updateDeviceService(DMSDPDeviceService service, int action) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.updateDeviceService(3, service, action, null);
        }
    }

    public int updateDeviceService(DMSDPDeviceService service, int action, Map<String, Object> params) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.updateDeviceService(3, service, action, params);
        }
    }

    public int setVirtualDevicePolicy(int module, int policy) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.setVirtualDevicePolicy(3, module, policy);
        }
    }

    public int switchModem(String deviceId, int mode, String varStr, int varInt) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.switchModem(deviceId, mode, varStr, varInt);
        }
    }

    public int getModemStatus(List<DMSDPVirtualDevice> virDeviceList) {
        synchronized (PAD_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.getModemStatus(virDeviceList);
        }
    }
}
