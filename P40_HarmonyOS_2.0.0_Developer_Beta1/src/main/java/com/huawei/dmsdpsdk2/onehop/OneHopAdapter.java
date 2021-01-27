package com.huawei.dmsdpsdk2.onehop;

import android.content.Context;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPAdapterAgent;
import com.huawei.dmsdpsdk2.DMSDPAdapterCallback;
import com.huawei.dmsdpsdk2.DMSDPAdapterProxy;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.DMSDPVirtualDevice;
import com.huawei.dmsdpsdk2.DataListener;
import com.huawei.dmsdpsdk2.DeviceInfo;
import com.huawei.dmsdpsdk2.HwLog;
import java.util.List;
import java.util.Map;

public class OneHopAdapter {
    protected static final Object ONEHOP_LOCK = new Object();
    private static final String TAG = "OneHopAdapter";
    private static OneHopAdapter sOneHopAdapter;
    private static OneHopAdapter sOneHopAdapterAgent;
    private static OneHopAdapterCallback sOneHopAdapterCallback;
    private static OneHopAdapterCallback sOneHopAdapterCallbackAgent;
    private DMSDPAdapter mDMSDPAdapter;

    public interface OneHopAdapterCallback {
        void onAdapterGet(OneHopAdapter oneHopAdapter);

        void onBinderDied();
    }

    public static void createInstance(Context context, final OneHopAdapterCallback callback) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "OneHopAdapter createInstance");
            if (callback != null) {
                sOneHopAdapterCallback = callback;
                if (sOneHopAdapter != null) {
                    HwLog.d(TAG, "createInstance callback has been exist");
                    callback.onAdapterGet(sOneHopAdapter);
                    return;
                }
                DMSDPAdapter.createInstance(context, new DMSDPAdapterCallback() {
                    /* class com.huawei.dmsdpsdk2.onehop.OneHopAdapter.AnonymousClass1 */

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onAdapterGet(DMSDPAdapter adapter) {
                        synchronized (OneHopAdapter.ONEHOP_LOCK) {
                            HwLog.w(OneHopAdapter.TAG, "OneHopAdapter onAdapterGet " + adapter);
                            if (adapter != null) {
                                if (adapter instanceof DMSDPAdapterProxy) {
                                    OneHopAdapter unused = OneHopAdapter.sOneHopAdapter = new OneHopAdapterProxy(adapter);
                                    callback.onAdapterGet(OneHopAdapter.sOneHopAdapter);
                                    return;
                                }
                            }
                            callback.onBinderDied();
                            OneHopAdapter.releaseInstance();
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

    public static void createInstanceAgent(Context context, final OneHopAdapterCallback callback) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "OneHopAdapter createInstanceAgent");
            if (callback != null) {
                sOneHopAdapterCallbackAgent = callback;
                if (sOneHopAdapterAgent != null) {
                    HwLog.d(TAG, "createInstanceAgent OneHopAdapter has been exist");
                    callback.onAdapterGet(sOneHopAdapterAgent);
                    return;
                }
                DMSDPAdapterAgent.createInstance(context, new DMSDPAdapterCallback() {
                    /* class com.huawei.dmsdpsdk2.onehop.OneHopAdapter.AnonymousClass2 */

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onAdapterGet(DMSDPAdapter adapter) {
                        synchronized (OneHopAdapter.ONEHOP_LOCK) {
                            HwLog.w(OneHopAdapter.TAG, "OneHopAdapter createInstanceAgent onAdapterGet " + adapter);
                            if (adapter != null) {
                                if (adapter instanceof DMSDPAdapterAgent) {
                                    OneHopAdapter unused = OneHopAdapter.sOneHopAdapterAgent = new OneHopAdapterAgent(adapter);
                                    callback.onAdapterGet(OneHopAdapter.sOneHopAdapterAgent);
                                    return;
                                }
                            }
                            callback.onBinderDied();
                            OneHopAdapter.releaseInstanceAgent();
                        }
                    }

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onBinderDied() {
                    }
                });
                return;
            }
            HwLog.e(TAG, "createInstanceAgent callback null");
            throw new IllegalArgumentException("createInstanceAgent callback null");
        }
    }

    public static void releaseInstance() {
        synchronized (ONEHOP_LOCK) {
            HwLog.w(TAG, "OneHopAdapter releaseInstance");
            sOneHopAdapter = null;
            sOneHopAdapterCallback = null;
            DMSDPAdapter.releaseInstance();
        }
    }

    public static void releaseInstanceAgent() {
        synchronized (ONEHOP_LOCK) {
            HwLog.w(TAG, "OneHopAdapter releaseInstanceAgent");
            sOneHopAdapterAgent = null;
            sOneHopAdapterCallbackAgent = null;
            DMSDPAdapterAgent.releaseInstance();
        }
    }

    /* access modifiers changed from: protected */
    public OneHopAdapterCallback getOneHopAdapterCallback() {
        return sOneHopAdapterCallback;
    }

    /* access modifiers changed from: protected */
    public OneHopAdapterCallback getOneHopAdapterCallbackAgent() {
        return sOneHopAdapterCallbackAgent;
    }

    /* access modifiers changed from: protected */
    public void setDmsdpAdapter(DMSDPAdapter adapter) {
        this.mDMSDPAdapter = adapter;
    }

    /* access modifiers changed from: protected */
    public boolean validateInit() {
        if (this.mDMSDPAdapter != null) {
            return true;
        }
        HwLog.e(TAG, "mDMSDPAdapter is null");
        return false;
    }

    public int registerDMSDPListener(int businessId, DMSDPListener listener) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "registerDMSDPListener start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.registerDMSDPListener(businessId, listener);
        }
    }

    public int unRegisterDMSDPListener(int businessId, DMSDPListener listener) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "unRegisterDMSDPListener start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.unRegisterDMSDPListener(businessId, listener);
        }
    }

    public int registerDataListener(int businessId, DMSDPDevice device, int dataType, DataListener listener) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "registerDataListener start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.registerDataListener(businessId, device, dataType, listener);
        }
    }

    public int unRegisterDataListener(int businessId, DMSDPDevice device, int dataType) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "unRegisterDataListener start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.unRegisterDataListener(businessId, device, dataType);
        }
    }

    public int sendData(int businessId, DMSDPDevice device, int dataType, byte[] data) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "sendData start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.sendData(businessId, device, dataType, data);
        }
    }

    public int connectDevice(int businessId, DMSDPDevice device) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "connectDevice start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.connectDevice(businessId, 2, device, null);
        }
    }

    public int disconnectDevice(int businessId, DMSDPDevice device) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "disconnectDevice start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.disconnectDevice(businessId, 2, device);
        }
    }

    public int requestDeviceService(int businessId, DMSDPDevice device, int type) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "requestDeviceService start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.requestDeviceService(businessId, device, type);
        }
    }

    public int startDeviceService(int businessId, DMSDPDeviceService service) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "startDeviceService start");
            if (!validateInit()) {
                return -2;
            }
            if (service == null) {
                HwLog.e(TAG, "service is null");
                return -2;
            } else if (service.getServiceType() == 1) {
                return this.mDMSDPAdapter.startDeviceService(businessId, service, 1, null);
            } else {
                return this.mDMSDPAdapter.startDeviceService(businessId, service, 0, null);
            }
        }
    }

    public int stopDeviceService(int businessId, DMSDPDeviceService service) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "stopDeviceService start");
            if (!validateInit()) {
                return -2;
            }
            if (service == null) {
                HwLog.e(TAG, "service is null");
                return -2;
            } else if (service.getServiceType() == 1) {
                return this.mDMSDPAdapter.stopDeviceService(businessId, service, 1);
            } else {
                return this.mDMSDPAdapter.stopDeviceService(businessId, service, 0);
            }
        }
    }

    public int updateDeviceService(int businessId, DMSDPDeviceService service, int action, Map<String, Object> params) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "updateDeviceService start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.updateDeviceService(businessId, service, action, params);
        }
    }

    public int setVirtualDevicePolicy(int businessId, int module, int policy) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "setVirtualDevicePolicy start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.setVirtualDevicePolicy(businessId, module, policy);
        }
    }

    public int switchModem(String deviceId, int mode, String varStr, int varInt) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "switchModem start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.switchModem(deviceId, mode, varStr, varInt);
        }
    }

    public int getModemStatus(List<DMSDPVirtualDevice> virDeviceList) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "getModemStatus start");
            if (!validateInit()) {
                return -2;
            }
            return this.mDMSDPAdapter.getModemStatus(virDeviceList);
        }
    }

    public int setDeviceInfo(int businessId, DeviceInfo deviceInfo) {
        synchronized (ONEHOP_LOCK) {
            HwLog.i(TAG, "setDeviceInfo start");
            if (!validateInit()) {
                return -2;
            }
            if (deviceInfo == null) {
                HwLog.e(TAG, "device info is null");
                return -2;
            }
            return this.mDMSDPAdapter.setDeviceInfo(businessId, deviceInfo);
        }
    }
}
