package com.huawei.dmsdpsdk2.hicar;

import android.content.Context;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPAdapterAgent;
import com.huawei.dmsdpsdk2.DMSDPAdapterCallback;
import com.huawei.dmsdpsdk2.DMSDPAdapterProxy;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.DataListener;
import com.huawei.dmsdpsdk2.DeviceInfo;
import com.huawei.dmsdpsdk2.HwLog;
import java.util.List;
import java.util.Map;

public class CarAdapter {
    protected static final Object CAR_LOCK = new Object();
    private static final String TAG = "CarAdapter";
    private static CarAdapter sCarAdapter;
    private static CarAdapter sCarAdapterAgent;
    private static CarAdapterCallback sCarAdapterCallback;
    private static CarAdapterCallback sCarAdapterCallbackAgent;
    private DMSDPAdapter mDMSDPAdapter;

    public interface CarAdapterCallback {
        void onAdapterGet(CarAdapter carAdapter);

        void onBinderDied();
    }

    public static void createInstance(Context context, final CarAdapterCallback callback) {
        synchronized (CAR_LOCK) {
            HwLog.i(TAG, "CarAdapter createInstance");
            if (callback != null) {
                sCarAdapterCallback = callback;
                if (sCarAdapter != null) {
                    HwLog.d(TAG, "createInstance callback has been exist");
                    callback.onAdapterGet(sCarAdapter);
                    return;
                }
                DMSDPAdapter.createInstance(context, new DMSDPAdapterCallback() {
                    /* class com.huawei.dmsdpsdk2.hicar.CarAdapter.AnonymousClass1 */

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onAdapterGet(DMSDPAdapter adapter) {
                        synchronized (CarAdapter.CAR_LOCK) {
                            HwLog.w(CarAdapter.TAG, "CarAdapter onAdapterGet " + adapter);
                            if (adapter != null) {
                                if (adapter instanceof DMSDPAdapterProxy) {
                                    CarAdapter unused = CarAdapter.sCarAdapter = new CarAdapterProxy(adapter);
                                    callback.onAdapterGet(CarAdapter.sCarAdapter);
                                    return;
                                }
                            }
                            callback.onBinderDied();
                            CarAdapter.releaseInstance();
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

    public static void createInstanceAgent(Context context, final CarAdapterCallback callback) {
        synchronized (CAR_LOCK) {
            HwLog.i(TAG, "CarAdapter createInstanceAgent");
            if (callback != null) {
                sCarAdapterCallbackAgent = callback;
                if (sCarAdapterAgent != null) {
                    HwLog.d(TAG, "createInstanceAgent carAdapter has been exist");
                    callback.onAdapterGet(sCarAdapterAgent);
                    return;
                }
                DMSDPAdapterAgent.createInstance(context, new DMSDPAdapterCallback() {
                    /* class com.huawei.dmsdpsdk2.hicar.CarAdapter.AnonymousClass2 */

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onAdapterGet(DMSDPAdapter adapter) {
                        synchronized (CarAdapter.CAR_LOCK) {
                            HwLog.w(CarAdapter.TAG, "CarAdapter createInstanceAgent onAdapterGet " + adapter);
                            if (adapter != null) {
                                if (adapter instanceof DMSDPAdapterAgent) {
                                    CarAdapter unused = CarAdapter.sCarAdapterAgent = new CarAdapterAgent(adapter);
                                    callback.onAdapterGet(CarAdapter.sCarAdapterAgent);
                                    return;
                                }
                            }
                            callback.onBinderDied();
                            CarAdapter.releaseInstanceAgent();
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
        synchronized (CAR_LOCK) {
            HwLog.w(TAG, "CarAdapter releaseInstance");
            sCarAdapter = null;
            sCarAdapterCallback = null;
            DMSDPAdapter.releaseInstance();
        }
    }

    public static void releaseInstanceAgent() {
        synchronized (CAR_LOCK) {
            HwLog.w(TAG, "CarAdapter releaseInstanceAgent");
            sCarAdapterAgent = null;
            sCarAdapterCallbackAgent = null;
            DMSDPAdapterAgent.releaseInstance();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        HwLog.w(TAG, "CarAdapter finalize");
        super.finalize();
    }

    /* access modifiers changed from: protected */
    public CarAdapterCallback getCarAdapterCallback() {
        return sCarAdapterCallback;
    }

    /* access modifiers changed from: protected */
    public CarAdapterCallback getCarAdapterCallbackAgent() {
        return sCarAdapterCallbackAgent;
    }

    /* access modifiers changed from: protected */
    public void setDmsdpAdapter(DMSDPAdapter adapter) {
        this.mDMSDPAdapter = adapter;
    }

    public int registerDMSDPListener(DMSDPListener listener) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.registerDMSDPListener(1, listener);
        }
    }

    public int unRegisterDMSDPListener(DMSDPListener listener) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.unRegisterDMSDPListener(1, listener);
        }
    }

    public int registerDataListener(DMSDPDevice device, int dataType, DataListener listener) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.registerDataListener(1, device, dataType, listener);
        }
    }

    public int unRegisterDataListener(DMSDPDevice device, int dataType) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.unRegisterDataListener(1, device, dataType);
        }
    }

    public int sendData(DMSDPDevice device, int dataType, byte[] data) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.sendData(1, device, dataType, data);
        }
    }

    public int connectDevice(int channelType, DMSDPDevice device) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.connectDevice(1, channelType, device, null);
        }
    }

    public int disconnectDevice(int channelType, DMSDPDevice device) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.disconnectDevice(1, channelType, device);
        }
    }

    public int sendKeyEvent(int keyCode, int action) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.sendKeyEvent(1, keyCode, action);
        }
    }

    public int sendHotWord(String hotWord) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.sendHotWord(1, hotWord);
        }
    }

    public int requestDeviceService(DMSDPDevice device, int type) {
        synchronized (CAR_LOCK) {
            HwLog.i(TAG, "requestDeviceService start");
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.requestDeviceService(1, device, type);
        }
    }

    public int startDeviceService(DMSDPDeviceService service) {
        synchronized (CAR_LOCK) {
            HwLog.i(TAG, "startDeviceService start");
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            } else if (service.getServiceType() == 1) {
                return this.mDMSDPAdapter.startDeviceService(1, service, 2, null);
            } else {
                return this.mDMSDPAdapter.startDeviceService(1, service, 0, null);
            }
        }
    }

    public int startDeviceService(DMSDPDeviceService service, int type, Map<String, Object> params) {
        synchronized (CAR_LOCK) {
            HwLog.i(TAG, "startDeviceService start");
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            } else if (service.getServiceType() == 1) {
                return this.mDMSDPAdapter.startDeviceService(1, service, type, params);
            } else {
                return this.mDMSDPAdapter.startDeviceService(1, service, 0, params);
            }
        }
    }

    public int stopDeviceService(DMSDPDeviceService service) {
        synchronized (CAR_LOCK) {
            HwLog.i(TAG, "stopDeviceService start");
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            } else if (service.getServiceType() == 1) {
                return this.mDMSDPAdapter.stopDeviceService(1, service, 2);
            } else {
                return this.mDMSDPAdapter.stopDeviceService(1, service, 0);
            }
        }
    }

    public int updateDeviceService(DMSDPDeviceService service, int action) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.updateDeviceService(1, service, action, null);
        }
    }

    public int updateDeviceService(DMSDPDeviceService service, int action, Map<String, Object> params) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.updateDeviceService(1, service, action, params);
        }
    }

    public int getVirtualCameraList(int businessId, List<String> cameraIdList) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.getVirtualCameraList(businessId, cameraIdList);
        }
    }

    public int setVirtualDevicePolicy(int module, int policy) {
        synchronized (CAR_LOCK) {
            HwLog.i(TAG, "setDefaultModemPolicy: policy:" + policy);
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.setVirtualDevicePolicy(1, module, policy);
        }
    }

    public void reportData(Map params) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
            } else {
                this.mDMSDPAdapter.reportData(params);
            }
        }
    }

    public int setDeviceInfo(DeviceInfo deviceInfo) {
        synchronized (CAR_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            } else if (deviceInfo == null) {
                HwLog.e(TAG, "device info is null");
                return -2;
            } else {
                return this.mDMSDPAdapter.setDeviceInfo(1, deviceInfo);
            }
        }
    }
}
