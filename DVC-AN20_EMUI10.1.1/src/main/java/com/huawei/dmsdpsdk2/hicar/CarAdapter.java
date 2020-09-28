package com.huawei.dmsdpsdk2.hicar;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPAdapterCallback;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.DataListener;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.IDMSDPAdapter;
import java.util.List;
import java.util.Map;

public final class CarAdapter {
    private static final Object CAR_LOCK = new Object();
    private static final String TAG = "CarAdapter";
    private static CarAdapter carAdapter;
    private static CarAdapterCallback carAdapterCallback;
    private DMSDPAdapter mDMSDPAdapter;

    public interface CarAdapterCallback {
        void onAdapterGet(CarAdapter carAdapter);

        void onBinderDied();
    }

    private CarAdapter(DMSDPAdapter adapter) {
        synchronized (CAR_LOCK) {
            this.mDMSDPAdapter = adapter;
            IDMSDPAdapter dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdpsdk2.hicar.CarAdapter.AnonymousClass1 */

                        public void binderDied() {
                            HwLog.i(CarAdapter.TAG, "CarAdapter onBinderDied");
                            CarAdapter.carAdapterCallback.onBinderDied();
                            CarAdapter.releaseInstance();
                            DMSDPAdapter.disableVirtualAudio();
                        }
                    }, 0);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "Call service linkToDeath RemoteException");
                }
            }
        }
    }

    public static void createInstance(Context context, final CarAdapterCallback callback) {
        synchronized (CAR_LOCK) {
            HwLog.i(TAG, "CarAdapter createInstance");
            if (callback != null) {
                carAdapterCallback = callback;
                if (carAdapter != null) {
                    HwLog.d(TAG, "createInstance callback has been exist");
                    callback.onAdapterGet(carAdapter);
                    return;
                }
                DMSDPAdapter.createInstance(context, new DMSDPAdapterCallback() {
                    /* class com.huawei.dmsdpsdk2.hicar.CarAdapter.AnonymousClass2 */

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onAdapterGet(DMSDPAdapter adapter) {
                        synchronized (CarAdapter.CAR_LOCK) {
                            HwLog.w(CarAdapter.TAG, "CarAdapter onAdapterGet " + adapter);
                            if (adapter == null) {
                                callback.onBinderDied();
                                CarAdapter.releaseInstance();
                                return;
                            }
                            CarAdapter unused = CarAdapter.carAdapter = new CarAdapter(adapter);
                            callback.onAdapterGet(CarAdapter.carAdapter);
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
        synchronized (CAR_LOCK) {
            HwLog.w(TAG, "CarAdapter releaseInstance");
            carAdapter = null;
            carAdapterCallback = null;
            DMSDPAdapter.releaseInstance();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        HwLog.w(TAG, "CarAdapter finalize");
        super.finalize();
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
}
