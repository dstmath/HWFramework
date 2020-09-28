package com.huawei.dmsdpsdk2.pc;

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

public final class PcAdapter {
    private static final Object PC_LOCK = new Object();
    private static final String TAG = "PcAdapter";
    private static PcAdapter pcAdapter;
    private static PcAdapterCallback pcAdapterCallback;
    private DMSDPAdapter mDMSDPAdapter;

    public interface PcAdapterCallback {
        void onAdapterGet(PcAdapter pcAdapter);

        void onBinderDied();
    }

    private PcAdapter(DMSDPAdapter adapter) {
        synchronized (PC_LOCK) {
            this.mDMSDPAdapter = adapter;
            IDMSDPAdapter dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdpsdk2.pc.PcAdapter.AnonymousClass1 */

                        public void binderDied() {
                            HwLog.i(PcAdapter.TAG, "PcAdapter onBinderDied");
                            if (PcAdapter.pcAdapterCallback != null) {
                                PcAdapter.pcAdapterCallback.onBinderDied();
                            }
                            PcAdapter.releaseInstance();
                            DMSDPAdapter.disableVirtualAudio();
                        }
                    }, 0);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "Call service linkToDeath RemoteException");
                }
            }
        }
    }

    public static void createInstance(Context context, final PcAdapterCallback callback) {
        synchronized (PC_LOCK) {
            HwLog.i(TAG, "PcAdapter createInstance");
            if (callback != null) {
                pcAdapterCallback = callback;
                if (pcAdapter != null) {
                    HwLog.d(TAG, "createInstance callback has been exist");
                    callback.onAdapterGet(pcAdapter);
                    return;
                }
                DMSDPAdapter.createInstance(context, new DMSDPAdapterCallback() {
                    /* class com.huawei.dmsdpsdk2.pc.PcAdapter.AnonymousClass2 */

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onAdapterGet(DMSDPAdapter adapter) {
                        synchronized (PcAdapter.PC_LOCK) {
                            HwLog.w(PcAdapter.TAG, "PcAdapter onAdapterGet " + adapter);
                            if (adapter == null) {
                                callback.onBinderDied();
                                PcAdapter.releaseInstance();
                                return;
                            }
                            PcAdapter unused = PcAdapter.pcAdapter = new PcAdapter(adapter);
                            callback.onAdapterGet(PcAdapter.pcAdapter);
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
        synchronized (PC_LOCK) {
            HwLog.w(TAG, "PcAdapter releaseInstance");
            pcAdapter = null;
            pcAdapterCallback = null;
            DMSDPAdapter.releaseInstance();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        HwLog.w(TAG, "PcAdapter finalize");
        super.finalize();
    }

    public int registerDMSDPListener(DMSDPListener listener) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.registerDMSDPListener(2, listener);
        }
    }

    public int unRegisterDMSDPListener(DMSDPListener listener) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.unRegisterDMSDPListener(2, listener);
        }
    }

    public int registerDataListener(DMSDPDevice device, int dataType, DataListener listener) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.registerDataListener(2, device, dataType, listener);
        }
    }

    public int unRegisterDataListener(DMSDPDevice device, int dataType) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.unRegisterDataListener(2, device, dataType);
        }
    }

    public int sendData(DMSDPDevice device, int dataType, byte[] data) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.sendData(2, device, dataType, data);
        }
    }

    public int connectDevice(DMSDPDevice device) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.connectDevice(2, 2, device, null);
        }
    }

    public int disconnectDevice(DMSDPDevice device) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.disconnectDevice(2, 2, device);
        }
    }

    public int requestDeviceService(DMSDPDevice device, int type) {
        synchronized (PC_LOCK) {
            HwLog.i(TAG, "requestDeviceService start");
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.requestDeviceService(2, device, type);
        }
    }

    public int startDeviceService(DMSDPDeviceService service) {
        synchronized (PC_LOCK) {
            HwLog.i(TAG, "startDeviceService start");
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            } else if (service == null) {
                HwLog.e(TAG, "service is null");
                return -2;
            } else if (service.getServiceType() == 1) {
                return this.mDMSDPAdapter.startDeviceService(2, service, 1, null);
            } else {
                return this.mDMSDPAdapter.startDeviceService(2, service, 0, null);
            }
        }
    }

    public int stopDeviceService(DMSDPDeviceService service) {
        synchronized (PC_LOCK) {
            HwLog.i(TAG, "stopDeviceService start");
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            } else if (service == null) {
                HwLog.e(TAG, "service is null");
                return -2;
            } else if (service.getServiceType() == 1) {
                return this.mDMSDPAdapter.stopDeviceService(2, service, 1);
            } else {
                return this.mDMSDPAdapter.stopDeviceService(2, service, 0);
            }
        }
    }

    public int updateDeviceService(DMSDPDeviceService service, int action) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.updateDeviceService(2, service, action, null);
        }
    }

    public int updateDeviceService(DMSDPDeviceService service, int action, Map<String, Object> params) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.updateDeviceService(2, service, action, params);
        }
    }

    public int setVirtualDevicePolicy(int module, int policy) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.setVirtualDevicePolicy(2, module, policy);
        }
    }

    public int switchModem(String deviceId, int mode, String varStr, int varInt) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.switchModem(deviceId, mode, varStr, varInt);
        }
    }

    public int getModemStatus(List<DMSDPVirtualDevice> virDeviceList) {
        synchronized (PC_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.getModemStatus(virDeviceList);
        }
    }
}
