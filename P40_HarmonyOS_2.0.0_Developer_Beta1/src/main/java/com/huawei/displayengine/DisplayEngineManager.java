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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DisplayEngineManager {
    private static final int DEFAULT_RETUREN_VALUE = -1;
    public static final String NAME_BOARD_VERSION = "boardVersion";
    public static final String NAME_DISPLAY_FULL = "Full";
    public static final String NAME_DISPLAY_MAIN = "Main";
    public static final String NAME_FACTORY_RUN_MODE = "FacRunMode";
    public static final String NAME_HEIGHT = "Height";
    public static final String NAME_IC_TYPE = "icType";
    public static final String NAME_LCD_PANEL_VERSION = "lcdPanelVersion";
    public static final String NAME_MAX_BACKLIGHT = "MaxBacklight";
    public static final String NAME_MAX_LUMINANCE = "MaxLuminance";
    public static final String NAME_MIN_BACKLIGHT = "MinBacklight";
    public static final String NAME_MIN_LUMINANCE = "MinLuminance";
    public static final String NAME_PANEL_NAME = "panelName";
    public static final String NAME_SN_CODE = "snCode";
    public static final String NAME_WIDTH = "Width";
    private static final String REMOTE_ERROR = ") has remote exception:";
    public static final String SERVICE_NAME = "DisplayEngineExService";
    private static final String TAG = "DE J DisplayEngineManager";
    private static final int TIME_OUT_MILLISECONDS = 2000;
    private static ExecutorService sPool = Executors.newFixedThreadPool(10);
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

    private <T> T excuteBinderTask(Callable<T> task, String taskName, T defaultValue) {
        if (task == null || sPool.isShutdown()) {
            DeLog.e(TAG, "task == null || sPool.isShutdown(, taskName = " + taskName);
            return defaultValue;
        }
        FutureTask<T> futureTask = new FutureTask<>(task);
        try {
            sPool.execute(futureTask);
            return futureTask.get(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            DeLog.e(TAG, taskName + " thread interrupt exception");
            return defaultValue;
        } catch (ExecutionException e2) {
            DeLog.e(TAG, taskName + " thread execution exception");
            return defaultValue;
        } catch (TimeoutException e3) {
            DeLog.e(TAG, taskName + " timeout exception");
            if (!futureTask.cancel(true)) {
                DeLog.e(TAG, taskName + " futureTask cancel failed!");
            }
            return defaultValue;
        } catch (RejectedExecutionException e4) {
            DeLog.e(TAG, taskName + " RejectedExecutionException");
            return defaultValue;
        }
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
        $$Lambda$DisplayEngineManager$LlYpjvMfEYdgvuw2utrsYfeY0U0 r0 = new Callable(feature) {
            /* class com.huawei.displayengine.$$Lambda$DisplayEngineManager$LlYpjvMfEYdgvuw2utrsYfeY0U0 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DisplayEngineManager.this.lambda$getSupported$0$DisplayEngineManager(this.f$1);
            }
        };
        return ((Integer) excuteBinderTask(r0, "getSupported " + feature, 0)).intValue();
    }

    public /* synthetic */ Integer lambda$getSupported$0$DisplayEngineManager(int feature) throws Exception {
        int ret = 0;
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                ret = service.getSupported(feature);
            }
        } catch (RemoteException e) {
            DeLog.e(TAG, "getSupported(" + feature + REMOTE_ERROR + e.getMessage());
        }
        return Integer.valueOf(ret);
    }

    public int setScene(int scene, int action) {
        $$Lambda$DisplayEngineManager$KdDs4NlK2iIdHs5t_hnii0a2OBY r0 = new Callable(scene, action) {
            /* class com.huawei.displayengine.$$Lambda$DisplayEngineManager$KdDs4NlK2iIdHs5t_hnii0a2OBY */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DisplayEngineManager.this.lambda$setScene$1$DisplayEngineManager(this.f$1, this.f$2);
            }
        };
        return ((Integer) excuteBinderTask(r0, "setScene " + scene + ", action " + action, -1)).intValue();
    }

    public /* synthetic */ Integer lambda$setScene$1$DisplayEngineManager(int scene, int action) throws Exception {
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
        return Integer.valueOf(ret);
    }

    public void registerCallback(IDisplayEngineCallback cb) {
        excuteBinderTask(new Callable(cb) {
            /* class com.huawei.displayengine.$$Lambda$DisplayEngineManager$DoKyr56oO3Ejq6stL9uDzcW6vIQ */
            private final /* synthetic */ IDisplayEngineCallback f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DisplayEngineManager.this.lambda$registerCallback$2$DisplayEngineManager(this.f$1);
            }
        }, "registerCallback", null);
    }

    public /* synthetic */ Object lambda$registerCallback$2$DisplayEngineManager(IDisplayEngineCallback cb) throws Exception {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service == null) {
                return null;
            }
            service.registerCallback(cb);
            return null;
        } catch (RemoteException e) {
            DeLog.e(TAG, "registerCallback(cb) has remote exception:" + e.getMessage());
            return null;
        }
    }

    public void unregisterCallback(IDisplayEngineCallback cb) {
        excuteBinderTask(new Callable(cb) {
            /* class com.huawei.displayengine.$$Lambda$DisplayEngineManager$aLwnZRk4eCGChTxCil6n5LfZSqg */
            private final /* synthetic */ IDisplayEngineCallback f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DisplayEngineManager.this.lambda$unregisterCallback$3$DisplayEngineManager(this.f$1);
            }
        }, "unregisterCallback", null);
    }

    public /* synthetic */ Object lambda$unregisterCallback$3$DisplayEngineManager(IDisplayEngineCallback cb) throws Exception {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service == null) {
                return null;
            }
            service.unregisterCallback(cb);
            return null;
        } catch (RemoteException e) {
            DeLog.e(TAG, "unregisterCallback(cb) has remote exception:" + e.getMessage());
            return null;
        }
    }

    public void updateLightSensorState(boolean isSensorEnable) {
        $$Lambda$DisplayEngineManager$75zbwaIUZcveWArfjJ3AmkKXs7s r0 = new Callable(isSensorEnable) {
            /* class com.huawei.displayengine.$$Lambda$DisplayEngineManager$75zbwaIUZcveWArfjJ3AmkKXs7s */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DisplayEngineManager.this.lambda$updateLightSensorState$4$DisplayEngineManager(this.f$1);
            }
        };
        excuteBinderTask(r0, "updateLightSensorState " + isSensorEnable, null);
    }

    public /* synthetic */ Object lambda$updateLightSensorState$4$DisplayEngineManager(boolean isSensorEnable) throws Exception {
        try {
            IDisplayEngineServiceEx service = getService();
            if (service == null) {
                return null;
            }
            service.updateLightSensorState(isSensorEnable);
            return null;
        } catch (RemoteException e) {
            DeLog.e(TAG, "updateLightSensorState has remote exception:" + e.getMessage());
            return null;
        }
    }

    public int setData(int type, PersistableBundle data) {
        $$Lambda$DisplayEngineManager$DdA_nyGESPxjAgzokBCLp73MauQ r0 = new Callable(type, data) {
            /* class com.huawei.displayengine.$$Lambda$DisplayEngineManager$DdA_nyGESPxjAgzokBCLp73MauQ */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ PersistableBundle f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DisplayEngineManager.this.lambda$setData$5$DisplayEngineManager(this.f$1, this.f$2);
            }
        };
        return ((Integer) excuteBinderTask(r0, "setData, type " + type + ", data " + data, -1)).intValue();
    }

    public /* synthetic */ Integer lambda$setData$5$DisplayEngineManager(int type, PersistableBundle data) throws Exception {
        int ret = 0;
        if (type == 6) {
            ret = getUpToXNit().setXnit(data.getInt("MinBrightness"), data.getInt("MaxBrightness"), data.getInt("brightnesslevel"));
        } else {
            try {
                IDisplayEngineServiceEx service = getService();
                if (service != null) {
                    ret = service.setData(type, data);
                }
            } catch (RemoteException e) {
                DeLog.e(TAG, "setData(" + type + ", " + data + REMOTE_ERROR + e.getMessage());
            }
        }
        return Integer.valueOf(ret);
    }

    public int sendMessage(int messageId, Bundle data) {
        $$Lambda$DisplayEngineManager$BcGjUZFE8SkM06cPhFP1JrzxblM r0 = new Callable(messageId, data) {
            /* class com.huawei.displayengine.$$Lambda$DisplayEngineManager$BcGjUZFE8SkM06cPhFP1JrzxblM */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ Bundle f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DisplayEngineManager.this.lambda$sendMessage$6$DisplayEngineManager(this.f$1, this.f$2);
            }
        };
        return ((Integer) excuteBinderTask(r0, "sendMessage, messageId " + messageId + ", data " + data, -1)).intValue();
    }

    public /* synthetic */ Integer lambda$sendMessage$6$DisplayEngineManager(int messageId, Bundle data) throws Exception {
        int ret = 0;
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                ret = service.sendMessage(messageId, data);
            }
        } catch (RemoteException e) {
            DeLog.e(TAG, "sendMessage(" + messageId + ", " + data + REMOTE_ERROR + e.getMessage());
        }
        return Integer.valueOf(ret);
    }

    public int getEffect(int feature, int type, Bundle data) {
        GetEffectTask getEffectTask = new GetEffectTask(feature, type, data);
        return ((Integer) excuteBinderTask(getEffectTask, "getEffect " + feature, -1)).intValue();
    }

    private class GetEffectTask implements Callable<Integer> {
        private Bundle mData;
        private int mFeature;
        private int mType;

        GetEffectTask(int feature, int type, Bundle data) {
            this.mFeature = feature;
            this.mType = type;
            this.mData = data;
        }

        @Override // java.util.concurrent.Callable
        public Integer call() throws Exception {
            int ret = 0;
            try {
                IDisplayEngineServiceEx service = DisplayEngineManager.this.getService();
                if (service != null) {
                    ret = service.getEffectEx(this.mFeature, this.mType, this.mData);
                }
            } catch (RemoteException e) {
                DeLog.e(DisplayEngineManager.TAG, "getEffect(" + this.mFeature + "," + this.mType + "," + this.mData + DisplayEngineManager.REMOTE_ERROR + e.getMessage());
            }
            return Integer.valueOf(ret);
        }
    }

    public int getEffect(int feature, int type, byte[] status, int length) {
        GetEffectByteTask getEffectByteTask = new GetEffectByteTask(feature, type, status, length);
        return ((Integer) excuteBinderTask(getEffectByteTask, "getEffectByte " + feature, -1)).intValue();
    }

    private class GetEffectByteTask implements Callable<Integer> {
        private int mFeature;
        private int mLength;
        private byte[] mStatus;
        private int mType;

        GetEffectByteTask(int feature, int type, byte[] status, int length) {
            this.mFeature = feature;
            this.mType = type;
            this.mStatus = status;
            this.mLength = length;
        }

        @Override // java.util.concurrent.Callable
        public Integer call() throws Exception {
            int ret = 0;
            try {
                IDisplayEngineServiceEx service = DisplayEngineManager.this.getService();
                if (service != null) {
                    ret = service.getEffect(this.mFeature, this.mType, this.mStatus, this.mLength);
                }
            } catch (RemoteException e) {
                DeLog.e(DisplayEngineManager.TAG, "getEffect(" + this.mFeature + ", " + this.mType + ", " + Arrays.toString(this.mStatus) + ", " + this.mLength + DisplayEngineManager.REMOTE_ERROR + e.getMessage());
            }
            return Integer.valueOf(ret);
        }
    }

    public int setEffect(int feature, int mode, PersistableBundle data) {
        $$Lambda$DisplayEngineManager$5zJtCPXKlNtIZYfdzQv9yh9tw r0 = new Callable(feature, mode, data) {
            /* class com.huawei.displayengine.$$Lambda$DisplayEngineManager$5zJtCPXKlNtIZYfdzQv9yh9tw */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ PersistableBundle f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DisplayEngineManager.this.lambda$setEffect$7$DisplayEngineManager(this.f$1, this.f$2, this.f$3);
            }
        };
        return ((Integer) excuteBinderTask(r0, "setEffect, feature " + feature + ", mode " + mode + ", data " + data, -1)).intValue();
    }

    public /* synthetic */ Integer lambda$setEffect$7$DisplayEngineManager(int feature, int mode, PersistableBundle data) throws Exception {
        int ret = 0;
        try {
            IDisplayEngineServiceEx service = getService();
            if (service != null) {
                ret = service.setEffect(feature, mode, data);
            }
        } catch (RemoteException e) {
            DeLog.e(TAG, "setEffect(" + feature + ", " + mode + ", " + data + REMOTE_ERROR + e.getMessage());
        }
        return Integer.valueOf(ret);
    }

    public List<Bundle> getAllRecords(String name) {
        return getAllRecords(name, null);
    }

    public List<Bundle> getAllRecords(String name, Bundle info) {
        GetAllRecordsTask getAllRecordsTask = new GetAllRecordsTask(name, info);
        return (List) excuteBinderTask(getAllRecordsTask, "getAllRecords " + name, null);
    }

    /* access modifiers changed from: private */
    public class GetAllRecordsTask implements Callable<List<Bundle>> {
        private Bundle mInfo;
        private String mName;

        GetAllRecordsTask(String name, Bundle info) {
            this.mName = name;
            this.mInfo = info;
        }

        @Override // java.util.concurrent.Callable
        public List<Bundle> call() throws Exception {
            try {
                IDisplayEngineServiceEx service = DisplayEngineManager.this.getService();
                if (service != null) {
                    return service.getAllRecords(this.mName, this.mInfo);
                }
                return null;
            } catch (RemoteException e) {
                DeLog.e(DisplayEngineManager.TAG, "getAllRecords(" + this.mName + ", " + this.mInfo + DisplayEngineManager.REMOTE_ERROR + e.getMessage());
                return null;
            }
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
        return ((Integer) excuteBinderTask(new Callable(filterName, data) {
            /* class com.huawei.displayengine.$$Lambda$DisplayEngineManager$omTboPlVkfhmo6S5cIrR8W7Y15I */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ Bundle f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DisplayEngineManager.lambda$setDataToFilter$8(this.f$0, this.f$1);
            }
        }, "setDataToFilter", -1)).intValue();
    }

    static /* synthetic */ Integer lambda$setDataToFilter$8(String filterName, Bundle data) throws Exception {
        int ret = 0;
        if ("HBM".equals(filterName)) {
            ret = getHbmFilter().setData(data);
        }
        return Integer.valueOf(ret);
    }
}
