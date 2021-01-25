package com.huawei.dmsdpsdk2.huaweitv;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.view.Surface;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.DMSDPAdapterAgent;
import com.huawei.dmsdpsdk2.DMSDPAdapterCallback;
import com.huawei.dmsdpsdk2.DMSDPDevice;
import com.huawei.dmsdpsdk2.DMSDPDeviceService;
import com.huawei.dmsdpsdk2.DMSDPListener;
import com.huawei.dmsdpsdk2.DataListener;
import com.huawei.dmsdpsdk2.DeviceInfo;
import com.huawei.dmsdpsdk2.DeviceParameterConst;
import com.huawei.dmsdpsdk2.HwLog;
import java.util.List;

public final class HuaweiTvAdapter {
    private static final Object HUAWEI_TV_LOCK = new Object();
    private static final String TAG = "HuaweiTvAdapter";
    private static HuaweiTvAdapter sHuaweiTvAdapter;
    private static HuaweiTvAdapterCallback sHuaweiTvAdapterCallback;
    private DMSDPAdapter mDMSDPAdapter;

    public interface HuaweiTvAdapterCallback {
        void onAdapterGet(HuaweiTvAdapter huaweiTvAdapter);

        void onBinderDied();
    }

    private HuaweiTvAdapter(DMSDPAdapter adapter) {
        synchronized (HUAWEI_TV_LOCK) {
            this.mDMSDPAdapter = adapter;
            IInterface dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdpsdk2.huaweitv.HuaweiTvAdapter.AnonymousClass1 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            synchronized (HuaweiTvAdapter.HUAWEI_TV_LOCK) {
                                HwLog.i(HuaweiTvAdapter.TAG, "HuaweiTvAdapter onBinderDied");
                                if (HuaweiTvAdapter.sHuaweiTvAdapterCallback != null) {
                                    HuaweiTvAdapter.sHuaweiTvAdapterCallback.onBinderDied();
                                }
                                HuaweiTvAdapter.releaseInstance();
                            }
                        }
                    }, 0);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "HuaweiTvAdapter service linkToDeath RemoteException");
                }
            }
        }
    }

    public static void createInstance(Context context, HuaweiTvAdapterCallback callback) {
        synchronized (HUAWEI_TV_LOCK) {
            HwLog.e(TAG, "HuaweiTvAdapter createInstance");
            if (callback != null) {
                sHuaweiTvAdapterCallback = callback;
                if (sHuaweiTvAdapter != null) {
                    HwLog.e(TAG, "createInstance callback has been exist");
                    callback.onAdapterGet(sHuaweiTvAdapter);
                    return;
                }
                HwLog.e(TAG, "call DMSDPAdapter createInstance");
                DMSDPAdapterAgent.createInstance(context, new DMSDPAdapterCallback() {
                    /* class com.huawei.dmsdpsdk2.huaweitv.HuaweiTvAdapter.AnonymousClass2 */

                    @Override // com.huawei.dmsdpsdk2.DMSDPAdapterCallback
                    public void onAdapterGet(DMSDPAdapter adapter) {
                        synchronized (HuaweiTvAdapter.HUAWEI_TV_LOCK) {
                            HwLog.e(HuaweiTvAdapter.TAG, "HuaweiTvAdapter onAdapterGet");
                            if (adapter != null) {
                                if (adapter instanceof DMSDPAdapterAgent) {
                                    HuaweiTvAdapter.sHuaweiTvAdapterCallback.onAdapterGet(new HuaweiTvAdapter(adapter));
                                    return;
                                }
                            }
                            HuaweiTvAdapter.sHuaweiTvAdapterCallback.onBinderDied();
                            HuaweiTvAdapter.releaseInstance();
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
        synchronized (HUAWEI_TV_LOCK) {
            HwLog.w(TAG, "HuaweiTvAdapter releaseInstance");
            sHuaweiTvAdapter = null;
            sHuaweiTvAdapterCallback = null;
            DMSDPAdapterAgent.releaseInstance();
        }
    }

    public int registerDMSDPListener(DMSDPListener listener) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "registerDMSDPListener mDMSDPAdapter is null");
                return -2;
            }
            HwLog.e(TAG, "registerDMSDPListener mDMSDPAdapter");
            return this.mDMSDPAdapter.registerDMSDPListener(4, listener);
        }
    }

    public int unRegisterDMSDPListener(DMSDPListener listener) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "unRegisterDMSDPListener mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.unRegisterDMSDPListener(4, listener);
        }
    }

    public int registerDataListener(DMSDPDevice device, int dataType, DataListener listener) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.registerDataListener(4, device, dataType, listener);
        }
    }

    public int unRegisterDataListener(DMSDPDevice device, int dataType) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.unRegisterDataListener(4, device, dataType);
        }
    }

    public int connectDevice(DMSDPDevice device) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            HwLog.e(TAG, "mDMSDPAdapter connectDevice=" + device.toString());
            return this.mDMSDPAdapter.connectDevice(4, 10, device, null);
        }
    }

    public int connectDevice(int channelType, DMSDPDevice device) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            HwLog.e(TAG, "mDMSDPAdapter connectDevice=" + device.toString());
            return this.mDMSDPAdapter.connectDevice(4, channelType, device, null);
        }
    }

    public int disconnectDevice(DMSDPDevice device) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "disconnectDevice mDMSDPAdapter is null and return");
                return -2;
            }
            return this.mDMSDPAdapter.disconnectDevice(4, 10, device);
        }
    }

    public int disconnectDevice(int channelType, DMSDPDevice device) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "disconnectDevice mDMSDPAdapter is null and return");
                return -2;
            }
            return this.mDMSDPAdapter.disconnectDevice(4, channelType, device);
        }
    }

    public int sendData(DMSDPDevice device, int dataType, byte[] data) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.sendData(4, device, dataType, data);
        }
    }

    public int updateDeviceService(DMSDPDeviceService service, int action) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.updateDeviceService(4, service, action, null);
        }
    }

    public int setDeviceInfo(DeviceInfo deviceInfo) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "setDeviceInfo mDMSDPAdapter is null");
                return -2;
            }
            HwLog.i(TAG, "come in setDeviceInfo...");
            return this.mDMSDPAdapter.setDeviceInfo(4, deviceInfo);
        }
    }

    public int deleteTrustDevice(String clientIdentify) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "deleteTrustDevice mDMSDPAdapter is null");
                return -2;
            }
            HwLog.e(TAG, "come in deleteTrustDevice clientIdentify=" + clientIdentify);
            return this.mDMSDPAdapter.deleteTrustDevice(4, clientIdentify);
        }
    }

    public int getTrustDeviceList(List<DMSDPDevice> devices) {
        synchronized (HUAWEI_TV_LOCK) {
            if (this.mDMSDPAdapter == null) {
                HwLog.e(TAG, "deleteTrustDevice mDMSDPAdapter is null");
                return -2;
            }
            return this.mDMSDPAdapter.getTrustDeviceList(4, devices);
        }
    }

    public int acceptConnection(boolean isPermanent) {
        DMSDPDevice currentDevice = new DMSDPDevice(BuildConfig.FLAVOR, 3);
        if (isPermanent) {
            currentDevice.addProperties(DeviceParameterConst.DEVICE_PERMISSION_TYPE, 1);
        } else {
            currentDevice.addProperties(DeviceParameterConst.DEVICE_PERMISSION_TYPE, 0);
        }
        return connectDevice(currentDevice);
    }

    public int startProjection(DMSDPDeviceService service, Surface surface, int width, int height) {
        HwLog.i(TAG, "start projection");
        if (surface == null || !surface.isValid()) {
            HwLog.e(TAG, "surface is not valid");
            return -2;
        }
        DeviceInfo info = new DeviceInfo(BuildConfig.FLAVOR);
        info.setSurface(surface);
        info.addProperties(DeviceParameterConst.DISPLAY_VITUALWIDTH_INT, Integer.valueOf(width));
        info.addProperties(DeviceParameterConst.DISPLAY_VITUALHEIGHT_INT, Integer.valueOf(height));
        int result = setDeviceInfo(info);
        if (result == 0) {
            return updateDeviceService(service, 204);
        }
        HwLog.e(TAG, "startProjection set device info failed,result:" + result);
        return result;
    }

    public int stopProjection(DMSDPDeviceService service) {
        return updateDeviceService(service, 205);
    }
}
