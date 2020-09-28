package com.huawei.displayengine;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DisplayEngineManager {
    private static final String REMOTE_ERROR = ") has remote exception:";
    public static final String SERVICE_NAME = "DisplayEngineExService";
    private static final String TAG = "DE J DisplayEngineManager";
    private final Context mContext;
    private volatile DisplayEngineLibraries mLibraries;
    private final Object mLockLibraries;
    private final Object mLockService;
    private final Object mLockUptoXnit;
    private volatile IDisplayEngineServiceEx mService;
    private volatile HwMinLuminanceUptoXnit mUpToXNit;

    /* access modifiers changed from: private */
    public static class Holder {
        static HbmSceneFilter sHbmFilter = new HbmSceneFilter();

        private Holder() {
        }
    }

    public DisplayEngineManager() {
        this(null);
    }

    public DisplayEngineManager(Context context) {
        this.mService = null;
        this.mLibraries = null;
        this.mUpToXNit = null;
        this.mLockService = new Object();
        this.mLockLibraries = new Object();
        this.mLockUptoXnit = new Object();
        this.mService = null;
        this.mLibraries = null;
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public IDisplayEngineServiceEx getService() {
        if (this.mService == null) {
            synchronized (this.mLockService) {
                if (this.mService == null) {
                    bindService();
                }
            }
        }
        return this.mService;
    }

    private void bindService() {
        IBinder binder = ServiceManager.getService(SERVICE_NAME);
        if (binder != null) {
            this.mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
            if (this.mService == null) {
                DeLog.w(TAG, "service is null!");
                return;
            }
            return;
        }
        this.mService = null;
        DeLog.w(TAG, "binder is null!");
    }

    private HwMinLuminanceUptoXnit getUpToXNit() {
        if (this.mUpToXNit == null) {
            synchronized (this.mLockUptoXnit) {
                if (this.mUpToXNit == null) {
                    this.mUpToXNit = new HwMinLuminanceUptoXnit(this);
                }
            }
        }
        return this.mUpToXNit;
    }

    public DisplayEngineLibraries getLibraries() {
        if (this.mLibraries == null) {
            synchronized (this.mLockLibraries) {
                if (this.mLibraries == null) {
                    this.mLibraries = new DisplayEngineLibraries(this);
                }
            }
        }
        return this.mLibraries;
    }

    public static HbmSceneFilter getHbmFilter() {
        return Holder.sHbmFilter;
    }

    public int getSupported(int feature) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.getSupported(feature);
            }
            return 0;
        } catch (RemoteException e) {
            DeLog.e(TAG, "getSupported(" + feature + REMOTE_ERROR + e.getMessage());
            return 0;
        }
    }

    public int setScene(int scene, int action) {
        int ret = 0;
        if (getHbmFilter().check(scene, action)) {
            return 0;
        }
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                ret = service.setScene(scene, action);
            }
            getLibraries().setScene(scene, action);
        } catch (RemoteException e) {
            DeLog.e(TAG, "setScene(" + scene + ", " + action + REMOTE_ERROR + e.getMessage());
        }
        return ret;
    }

    public void registerCallback(IDisplayEngineCallback cb) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                service.registerCallback(cb);
            }
        } catch (RemoteException e) {
            DeLog.e(TAG, "registerCallback(cb) has remote exception:" + e.getMessage());
        }
    }

    public void unregisterCallback(IDisplayEngineCallback cb) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                service.registerCallback(cb);
            }
        } catch (RemoteException e) {
            DeLog.e(TAG, "registerCallback(cb) has remote exception:" + e.getMessage());
        }
    }

    public void updateLightSensorState(boolean isSensorEnable) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                service.updateLightSensorState(isSensorEnable);
            }
        } catch (RemoteException e) {
            DeLog.e(TAG, "updateLightSensorState has remote exception:" + e.getMessage());
        }
    }

    public int setData(int type, PersistableBundle data) {
        if (type == 6) {
            return getUpToXNit().setXnit(data.getInt("MinBrightness"), data.getInt("MaxBrightness"), data.getInt("brightnesslevel"));
        }
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.setData(type, data);
            }
            return 0;
        } catch (RemoteException e) {
            DeLog.e(TAG, "setData(" + type + ", " + data + REMOTE_ERROR + e.getMessage());
            return 0;
        }
    }

    public int sendMessage(int messageId, Bundle data) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.sendMessage(messageId, data);
            }
            return 0;
        } catch (RemoteException e) {
            DeLog.e(TAG, "sendMessage(" + messageId + ", " + data + REMOTE_ERROR + e.getMessage());
            return 0;
        }
    }

    public int getEffect(int feature, int type, Bundle data) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.getEffectEx(feature, type, data);
            }
            return 0;
        } catch (RemoteException e) {
            DeLog.e(TAG, "getEffect(" + feature + "," + type + "," + data + REMOTE_ERROR + e.getMessage());
            return 0;
        }
    }

    public int getEffect(int feature, int type, byte[] status, int length) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.getEffect(feature, type, status, length);
            }
            return 0;
        } catch (RemoteException e) {
            DeLog.e(TAG, "getEffect(" + feature + ", " + type + ", " + Arrays.toString(status) + ", " + length + REMOTE_ERROR + e.getMessage());
            return 0;
        }
    }

    public int setEffect(int feature, int mode, PersistableBundle data) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.setEffect(feature, mode, data);
            }
            return 0;
        } catch (RemoteException e) {
            DeLog.e(TAG, "setEffect(" + feature + ", " + mode + ", " + data + REMOTE_ERROR + e.getMessage());
            return 0;
        }
    }

    public List<Bundle> getAllRecords(String name) {
        return getAllRecords(name, null);
    }

    public List<Bundle> getAllRecords(String name, Bundle info) {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                return service.getAllRecords(name, info);
            }
            return null;
        } catch (RemoteException e) {
            DeLog.e(TAG, "getAllRecords(" + name + ", " + info + REMOTE_ERROR + e.getMessage());
            return null;
        }
    }

    public Object imageProcess(String command, Map<String, Object> param) {
        return getLibraries().imageProcess(command, param);
    }

    public int brightnessTrainingProcess() {
        DeLog.i(TAG, "brightnessTrainingProcess ");
        return getLibraries().brightnessTrainingProcess(null, null);
    }

    public int brightnessTrainingAbort() {
        DeLog.i(TAG, "brightnessTrainingAbort ");
        return getLibraries().brightnessTrainingAbort();
    }

    public int setDataToFilter(String filterName, Bundle data) {
        if ("HBM".equals(filterName)) {
            return getHbmFilter().setData(data);
        }
        return 0;
    }
}
