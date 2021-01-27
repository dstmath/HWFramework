package android.rms;

import android.app.mtm.MultiTaskPolicy;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.IHwSysResManager;
import android.rms.config.ResourceConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IDeviceSettingCallback;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.NetLocationStrategy;
import android.rms.iaware.RPolicyData;
import android.rms.iaware.StatisticsData;
import android.rms.iaware.memrepair.MemRepairPkgInfo;
import android.util.Log;
import java.util.List;
import java.util.Map;

public final class HwSysResManager {
    private static final String TAG = "RMS.HwSysResManager";
    private static HwSysResManager resourceManager;
    private IHwSysResManager mService;

    private HwSysResManager() {
    }

    public static synchronized HwSysResManager getInstance() {
        HwSysResManager hwSysResManager;
        synchronized (HwSysResManager.class) {
            if (resourceManager == null) {
                resourceManager = new HwSysResManager();
            }
            hwSysResManager = resourceManager;
        }
        return hwSysResManager;
    }

    private IHwSysResManager getService() {
        IHwSysResManager iHwSysResManager = this.mService;
        if (iHwSysResManager != null) {
            return iHwSysResManager;
        }
        this.mService = IHwSysResManager.Stub.asInterface(ServiceManager.getService("hwsysresmanager"));
        return this.mService;
    }

    public ResourceConfig[] getResourceConfig(int resourceType) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getResourceConfig service is null ");
            }
            return null;
        }
        try {
            return service.getResourceConfig(resourceType);
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.e(TAG, "Should never happen!");
            return null;
        }
    }

    public String getWhiteList(int resourceType, int type) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getWhiteList service is null ");
            }
            return null;
        }
        try {
            return service.getWhiteList(resourceType, type);
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.i(TAG, "getWhiteList catch RemoteException");
            return null;
        }
    }

    public int getPid(String procName) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getPId service is null ");
            }
            return 0;
        }
        try {
            return service.getPid(procName);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "getPid catch RemoteException");
            }
            return 0;
        }
    }

    public long getPss(int pid) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getPId service is null ");
            }
            return 0;
        }
        try {
            return service.getPss(pid);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "getPss catch RemoteException");
            }
            return 0;
        }
    }

    public boolean registerResourceCallback(IUpdateWhiteListCallback registerCallback) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "HwSysResManager_registerResourceCallback service is null ");
            }
            return false;
        }
        try {
            return service.registerResourceUpdateCallback(registerCallback);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "registerResourceCallback catch RemoteException");
            }
            return false;
        }
    }

    public boolean registerSceneCallback(IBinder callback, int scenes) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "HwSysResManager_registerSceneCallback service is null ");
            }
            return false;
        }
        try {
            return service.registerSceneCallback(callback, scenes);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "registerSceneCallback catch RemoteException");
            }
            Log.i(TAG, "HwSysResManager_registerSceneCallback failed");
            return false;
        }
    }

    public void triggerUpdateWhiteList() {
        IHwSysResManager service = getService();
        if (service == null) {
            Log.e(TAG, "HwSysResManager_triggerUpdateWhiteList service is null ");
            return;
        }
        try {
            service.triggerUpdateWhiteList();
        } catch (RemoteException e) {
            Log.e(TAG, "triggerUpdateWhiteList catch RemoteException");
        }
    }

    public void recordResourceOverloadStatus(Map<String, Object> overloadStatus) {
        if (overloadStatus != null) {
            IHwSysResManager service = getService();
            if (service != null) {
                try {
                    service.recordResourceOverloadStatus(getInt(overloadStatus, "uid"), getString(overloadStatus, "pkg"), getInt(overloadStatus, "resourceType"), getInt(overloadStatus, "overloadNum"), getInt(overloadStatus, "speedOverLoadPeriod"), getInt(overloadStatus, "totalNum"), getBundle(overloadStatus, "bundleArgs"));
                } catch (RemoteException e) {
                    if (Log.HWINFO) {
                        Log.i(TAG, "reportResourceSpeedStatus RemoteException");
                    }
                }
            } else if (Log.HWINFO) {
                Log.i(TAG, "recordResourceOverloadStatus  service is null ");
            }
        }
    }

    public void clearResourceStatus(int uid, int resourceType) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.clearResourceStatus(uid, resourceType);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "clearResourceStatus RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "clearResourceStatus service is null ");
        }
    }

    public int acquireSysRes(int resourceType, Uri uri, IContentObserver observer, Bundle args) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                return service.acquireSysRes(resourceType, uri, observer, args);
            } catch (RemoteException e) {
                if (!Log.HWINFO) {
                    return 1;
                }
                Log.i(TAG, "acquireSysRes RemoteException");
                return 1;
            }
        } else if (!Log.HWINFO) {
            return 2;
        } else {
            Log.i(TAG, "acquireSysRes service is null ");
            return 2;
        }
    }

    public void notifyResourceStatus(int resourceType, String resourceName, int resourceStatus, Bundle bd) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.notifyResourceStatus(resourceType, resourceName, resourceStatus, bd);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "notifyResourceStatus RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "notifyResourceStatus service is null ");
        }
    }

    public void dispatch(int resourceType, MultiTaskPolicy policy) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.dispatch(resourceType, policy);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "dispatch RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "dispatch service is null ");
        }
    }

    public void reportData(CollectData data) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.reportData(data);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "reportData RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "reportData service is null ");
        }
    }

    public void reportDataWithCallback(CollectData data, IReportDataCallback callback) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.reportDataWithCallback(data, callback);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "reportDataWithCallback RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "reportDataWithCallback service is null ");
        }
    }

    public List<String> getIAwareProtectList(int num) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getIAwareProtectList service is null ");
            }
            return null;
        }
        try {
            return service.getIAwareProtectList(num);
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.i(TAG, "getIAwareProtectList RemoteException");
            return null;
        }
    }

    public List<String> getLongTimeRunningApps() {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getLongTimeRunningApps service is null ");
            }
            return null;
        }
        try {
            return service.getLongTimeRunningApps();
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.i(TAG, "getLongTimeRunningApps RemoteException");
            return null;
        }
    }

    public List<String> getMostFrequentUsedApps(int pkgNums) {
        return getMostFrequentUsedApps(pkgNums, -1);
    }

    public List<String> getMostFrequentUsedApps(int pkgNums, int minCount) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getMostFrequentUsedApps service is null ");
            }
            return null;
        }
        try {
            return service.getMostFrequentUsedApps(pkgNums, minCount);
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.i(TAG, "getMostFrequentUsedApps RemoteException");
            return null;
        }
    }

    public void reportAppType(String pkgName, int appType, boolean isInstalled, int attr) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.reportAppType(pkgName, appType, isInstalled, attr);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "reportAppType RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "reportAppType error HwSysResManagerService is null ");
        }
    }

    public void reportHabitData(Bundle habitData) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.reportHabitData(habitData);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "reportTopList RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "reportTopList error HwSysResManagerService is null ");
        }
    }

    public long getMemAvaliable() {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getMemAvaliable service is null ");
            }
            return 0;
        }
        try {
            return service.getMemAvaliable();
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "getMemAvaliable RemoteException");
            }
            return 0;
        }
    }

    public void enableFeature(int type) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.enableFeature(type);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "enableFeature RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "enableFeature service is null ");
        }
    }

    public void disableFeature(int type) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.disableFeature(type);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "disableFeature RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "disableFeature service is null ");
        }
    }

    public void dispatchRPolicy(RPolicyData policy) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.dispatchRPolicy(policy);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "dispatchRPolicy RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "dispatchRPolicy service is null ");
        }
    }

    public boolean configUpdate() {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "configUpdate service is null ");
            }
            return false;
        }
        try {
            return service.configUpdate();
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "configUpdate RemoteException");
            }
            return false;
        }
    }

    public boolean custConfigUpdate() {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "csutConfigUpdate service is null ");
            }
            return false;
        }
        try {
            return service.custConfigUpdate();
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "custConfigUpdate RemoteException");
            }
            return false;
        }
    }

    public void requestAppClean(List<String> pkgNameList, int[] userIdArray, int level, String reason, int source) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.requestAppClean(pkgNameList, userIdArray, level, reason, source);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "requestAppClean RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "requestAppClean service is null");
        }
    }

    public void init(Bundle bundle) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.init(bundle);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "init RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "init service is null ");
        }
    }

    public boolean isResourceNeeded(int resourceid) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "isResourceNeeded service is null ");
            }
            return false;
        }
        try {
            return service.isResourceNeeded(resourceid);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "isResourceNeeded RemoteException");
            }
            return false;
        }
    }

    public int getDumpData(int time, List<DumpData> dumpData) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getDumpData service is null ");
            }
            return 0;
        }
        try {
            return service.getDumpData(time, dumpData);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "getDumpData RemoteException");
            }
            return 0;
        }
    }

    public int getStatisticsData(List<StatisticsData> statisticsData) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getStatisticsData service is null ");
            }
            return 0;
        }
        try {
            return service.getStatisticsData(statisticsData);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "getStatisticsData RemoteException");
            }
            return 0;
        }
    }

    public boolean registerProcessStateChangeObserver(IProcessStateChangeObserver observer) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "HwSysResManager_registerProcessStateChangeObserver service is null ");
            }
            return false;
        }
        try {
            if (service.registerProcessStateChangeObserver(observer)) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "registerProcessStateChangeObserver catch RemoteException");
            }
        }
    }

    public boolean unRegisterProcessStateChangeObserver(IProcessStateChangeObserver observer) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "HwSysResManager_unRegisterProcessStateChangeObserver service is null ");
            }
            return false;
        }
        try {
            if (service.unRegisterProcessStateChangeObserver(observer)) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "unRegisterProcessStateChangeObserver catch RemoteException");
            }
        }
    }

    public void noteProcessStart(Map<String, Object> processParams) {
        if (processParams != null) {
            IHwSysResManager service = getService();
            if (service != null) {
                try {
                    service.noteProcessStart(getString(processParams, "packageName"), getString(processParams, FreezeScreenScene.PROCESS_NAME), getInt(processParams, FreezeScreenScene.PID_PARAM), getInt(processParams, "uid"), getBoolean(processParams, "started"), getString(processParams, "launcherMode"), getString(processParams, "reason"));
                } catch (RemoteException e) {
                    if (Log.HWINFO) {
                        Log.i(TAG, "noteProcessStart RemoteException");
                    }
                }
            } else if (Log.HWINFO) {
                Log.i(TAG, "getStatisticsData service is null ");
            }
        }
    }

    public String saveBigData(int featureId, boolean isClear) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "saveBigData service is null ");
            }
            return null;
        }
        try {
            return service.saveBigData(featureId, isClear);
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.i(TAG, "saveBigData RemoteException");
            return null;
        }
    }

    public String fetchBigDataByVersion(int awareVersion, int featureId, boolean beta, boolean clear) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "iaware2.0 fetch data service is null");
            }
            return null;
        }
        try {
            return service.fetchBigDataByVersion(awareVersion, featureId, beta, clear);
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.i(TAG, "iaware2.0 fetch data RemoteException");
            return null;
        }
    }

    public void fetchDftDataByVersion(Bundle args) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.fetchDftDataByVersion(args);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "iaware3.0 fetch dft data RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "iaware3.0 fetch dft data service is null");
        }
    }

    public void updateFakeForegroundList(List<String> processList) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.updateFakeForegroundList(processList);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.e(TAG, "updateFakeForegroundList RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.e(TAG, "updateFakeForegroundList service is null ");
        }
    }

    public boolean isFakeForegroundProcess(String process) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.e(TAG, "isFakeForegroundProcess service is null ");
            }
            return false;
        }
        try {
            return service.isFakeForegroundProcess(process);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.e(TAG, "isFakeForegroundProcess RemoteException");
            }
            return false;
        }
    }

    public boolean isEnableFakeForegroundControl() {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.e(TAG, "isEnableFakeForegroundControl service is null ");
            }
            return false;
        }
        try {
            return service.isEnableFakeForegroundControl();
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.e(TAG, "isEnableFakeForegroundControl RemoteException");
            }
            return false;
        }
    }

    public void reportVibratorToIAware(int uid) {
        if (isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt("tgtUid", uid);
            bundleArgs.putInt("relationType", 19);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long id = Binder.clearCallingIdentity();
            reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    public void reportProximitySensorEventToIAware(boolean positive) {
        if (isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_SCENE_REC))) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt("positive", positive ? 1 : 0);
            bundleArgs.putInt("relationType", 23);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_SCENE_REC), System.currentTimeMillis(), bundleArgs);
            long id = Binder.clearCallingIdentity();
            reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    public List<MemRepairPkgInfo> getMemRepairProcGroup(int sceneType) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.e(TAG, "getMemRepairProcGroup service is null ");
            }
            return null;
        }
        try {
            return service.getMemRepairProcGroup(sceneType);
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.e(TAG, "getMemRepairProcGroup RemoteException");
            return null;
        }
    }

    public List<String> getFrequentIM(int count) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.e(TAG, "getFrequentIM service is null ");
            }
            return null;
        }
        try {
            return service.getFrequentIM(count);
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.e(TAG, "getFrequentIM RemoteException");
            return null;
        }
    }

    public void reportMediaKeyToIAware(int uid) {
        if (isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_MEDIA_BTN))) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt("eventid", 20017);
            bundleArgs.putInt("callUid", uid);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_MEDIA_BTN), System.currentTimeMillis(), bundleArgs);
            long id = Binder.clearCallingIdentity();
            reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    public void reportToastHiddenToIAware(int pid, int hcode) {
        if (isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt(FreezeScreenScene.WINDOW_PARAM, pid);
            bundleArgs.putInt("hashcode", hcode);
            bundleArgs.putInt("relationType", 28);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long id = Binder.clearCallingIdentity();
            reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    public void reportGoogleConn(boolean isGmsConn) {
        if (isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putBoolean("gms_conn", isGmsConn);
            bundleArgs.putInt("relationType", 33);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long origId = Binder.clearCallingIdentity();
            getInstance().reportData(data);
            Binder.restoreCallingIdentity(origId);
        }
    }

    public boolean isVisibleWindow(int userid, String pkg, int type) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "isVisibleWindow service is null ");
            }
            return true;
        }
        try {
            return service.isVisibleWindow(userid, pkg, type);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "isVisibleWindow RemoteException");
            }
            return true;
        }
    }

    public void reportSysWakeUp(String reason) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.reportSysWakeUp(reason);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.e(TAG, "reportSysWakeUp RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.e(TAG, "reportSysWakeUp service is null ");
        }
    }

    public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.e(TAG, "getNetLocationStrategy service is null ");
            }
            return null;
        }
        try {
            return service.getNetLocationStrategy(pkgName, uid, type);
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.e(TAG, "getNetLocationStrategy RemoteException");
            return null;
        }
    }

    public List<String> getHabitTopN(int pkgNums) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getHabitTopN service is null ");
            }
            return null;
        }
        try {
            return service.getHabitTopN(pkgNums);
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.i(TAG, "getHabitTopN RemoteException");
            return null;
        }
    }

    public Bundle getTypeTopN(int[] appTypes) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getTypeTopN service is null ");
            }
            return null;
        }
        try {
            return service.getTypeTopN(appTypes);
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.i(TAG, "getTypeTopN RemoteException");
            return null;
        }
    }

    public void registerDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.registerDevModeMethod(deviceId, callback, args);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.e(TAG, "registerDevModeMethod RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.e(TAG, "registerDevModeMethod service is null ");
        }
    }

    public void unregisterDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.unregisterDevModeMethod(deviceId, callback, args);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.e(TAG, "unregisterDevModeMethod RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.e(TAG, "unregisterDevModeMethod service is null ");
        }
    }

    public void reportAwareVpnConnect(boolean isEnable) {
        int resid = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_VPN_CONN);
        if (isResourceNeeded(resid)) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("vpn_state", isEnable);
            CollectData data = new CollectData(resid, System.currentTimeMillis(), bundle);
            long id = Binder.clearCallingIdentity();
            reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    public void reportTopAData(Bundle bdl) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.reportTopActData(bdl);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "reportTopAData RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "reportTopAData service is null");
        }
    }

    public void reportCloudUpdate(Bundle bundle) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.reportCloudUpdate(bundle);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.e(TAG, "reportCloudUpdate RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.e(TAG, "reportCloudUpdate service is null");
        }
    }

    public void reportSceneInfos(Bundle bdl) {
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                service.reportSceneInfos(bdl);
            } catch (RemoteException e) {
                if (Log.HWINFO) {
                    Log.i(TAG, "reportSceneInfos RemoteException");
                }
            }
        } else if (Log.HWINFO) {
            Log.i(TAG, "reportSceneInfos service is null");
        }
    }

    public List<String> getCtsPkgs() {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getCtsPkgs service is null");
            }
            return null;
        }
        try {
            return service.getCtsPkgs();
        } catch (RemoteException e) {
            if (!Log.HWINFO) {
                return null;
            }
            Log.i(TAG, "getCtsPkgs RemoteException");
            return null;
        }
    }

    public boolean preloadAppForLauncher(String packageName, int userId, int preloadType) {
        Log.i(TAG, "preloadAppForLauncher packageName " + packageName + " preloadType " + preloadType);
        IHwSysResManager service = getService();
        if (service != null) {
            try {
                return service.preloadAppForLauncher(packageName, userId, preloadType);
            } catch (RemoteException e) {
                if (!Log.HWINFO) {
                    return false;
                }
                Log.e(TAG, "reportCloudUpdate RemoteException");
                return false;
            }
        } else if (!Log.HWINFO) {
            return false;
        } else {
            Log.e(TAG, "reportCloudUpdate service is null");
            return false;
        }
    }

    public boolean isZApp(String pkg, int userId) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.e(TAG, "isZApp service is null");
            }
            return false;
        }
        try {
            return service.isZApp(pkg, userId);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.e(TAG, "isZApp RemoteException");
            }
            return false;
        }
    }

    private String getString(Map<String, Object> paramsMap, String key) {
        Object object = paramsMap.get(key);
        if (object != null && (object instanceof String)) {
            return (String) object;
        }
        return null;
    }

    private int getInt(Map<String, Object> paramsMap, String key) {
        Object object = paramsMap.get(key);
        if (object != null && (object instanceof Integer)) {
            return ((Integer) object).intValue();
        }
        return -1;
    }

    private boolean getBoolean(Map<String, Object> paramsMap, String key) {
        Object object = paramsMap.get(key);
        if (object != null && (object instanceof Boolean)) {
            return ((Boolean) object).booleanValue();
        }
        return false;
    }

    private Bundle getBundle(Map<String, Object> paramsMap, String key) {
        Object object = paramsMap.get(key);
        if (object != null && (object instanceof Bundle)) {
            return (Bundle) object;
        }
        return null;
    }
}
