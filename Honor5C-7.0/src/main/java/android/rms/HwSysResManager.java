package android.rms;

import android.app.mtm.MultiTaskPolicy;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.IHwSysResManager.Stub;
import android.rms.config.ResourceConfig;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.RPolicyData;
import android.rms.iaware.StatisticsData;
import android.util.Log;
import java.util.List;

public final class HwSysResManager {
    private static final String TAG = "RMS.HwSysResManager";
    private static HwSysResManager mResourceManager;
    private IHwSysResManager sService;

    public static synchronized HwSysResManager getInstance() {
        HwSysResManager hwSysResManager;
        synchronized (HwSysResManager.class) {
            if (mResourceManager == null) {
                mResourceManager = new HwSysResManager();
            }
            hwSysResManager = mResourceManager;
        }
        return hwSysResManager;
    }

    private IHwSysResManager getService() {
        if (this.sService != null) {
            return this.sService;
        }
        this.sService = Stub.asInterface(ServiceManager.getService("hwsysresmanager"));
        return this.sService;
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
            ResourceConfig[] config = service.getResourceConfig(resourceType);
            if (config != null) {
                return config;
            }
            return null;
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.e(TAG, "Should never happen!");
            }
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
            String whiteList = service.getWhiteList(resourceType, type);
            if (whiteList != null) {
                return whiteList;
            }
            return null;
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "getWhiteList catch RemoteException");
            }
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

    public boolean registerResourceCallback(IUpdateWhiteListCallback hwu) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "HwSysResManager_registerResourceCallback service is null ");
            }
            return false;
        }
        try {
            if (service.registerResourceUpdateCallback(hwu)) {
                return true;
            }
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "registerResourceCallback catch RemoteException");
            }
        }
        return false;
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

    public void recordResourceOverloadStatus(int uid, String pkg, int resourceType, int speedOverloadNum, int speedOverLoadPeroid, int countOverLoadNum) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "recordResourceOverloadStatus  service is null ");
            }
            return;
        }
        try {
            service.recordResourceOverloadStatus(uid, pkg, resourceType, speedOverloadNum, speedOverLoadPeroid, countOverLoadNum);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "reportResourceSpeedStatus RemoteException");
            }
        }
    }

    public void clearResourceStatus(int uid, int resourceType) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "clearResourceStatus service is null ");
            }
            return;
        }
        try {
            service.clearResourceStatus(uid, resourceType);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "clearResourceStatus RemoteException");
            }
        }
    }

    public int acquireSysRes(int resourceType, Uri uri, IContentObserver observer, Bundle args) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "acquireSysRes service is null ");
            }
            return 2;
        }
        try {
            return service.acquireSysRes(resourceType, uri, observer, args);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "acquireSysRes RemoteException");
            }
            return 1;
        }
    }

    public void notifyResourceStatus(int resourceType, String resourceName, int resourceStatus, Bundle bd) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "notifyResourceStatus service is null ");
            }
            return;
        }
        try {
            service.notifyResourceStatus(resourceType, resourceName, resourceStatus, bd);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "notifyResourceStatus RemoteException");
            }
        }
    }

    public void dispatch(int resourceType, MultiTaskPolicy policy) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "dispatch service is null ");
            }
            return;
        }
        try {
            service.dispatch(resourceType, policy);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "dispatch RemoteException");
            }
        }
    }

    public void reportData(CollectData data) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "reportData service is null ");
            }
            return;
        }
        try {
            service.reportData(data);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "reportData RemoteException");
            }
        }
    }

    public void reportDataWithCallback(CollectData data, IReportDataCallback callback) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "reportDataWithCallback service is null ");
            }
            return;
        }
        try {
            service.reportDataWithCallback(data, callback);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "reportDataWithCallback RemoteException");
            }
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
            if (Log.HWINFO) {
                Log.i(TAG, "getIAwareProtectList RemoteException");
            }
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
            if (Log.HWINFO) {
                Log.i(TAG, "getLongTimeRunningApps RemoteException");
            }
            return null;
        }
    }

    public List<String> getMostFrequentUsedApps(int n) {
        return getMostFrequentUsedApps(n, -1);
    }

    public List<String> getMostFrequentUsedApps(int n, int minCount) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getMostFrequentUsedApps service is null ");
            }
            return null;
        }
        try {
            return service.getMostFrequentUsedApps(n, minCount);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "getMostFrequentUsedApps RemoteException");
            }
            return null;
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
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "enableFeature service is null ");
            }
            return;
        }
        try {
            service.enableFeature(type);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "enableFeature RemoteException");
            }
        }
    }

    public void disableFeature(int type) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "disableFeature service is null ");
            }
            return;
        }
        try {
            service.disableFeature(type);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "disableFeature RemoteException");
            }
        }
    }

    public void dispatchRPolicy(RPolicyData policy) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "dispatchRPolicy service is null ");
            }
            return;
        }
        try {
            service.dispatchRPolicy(policy);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "dispatchRPolicy RemoteException");
            }
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

    public void init(Bundle bundle) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "init service is null ");
            }
            return;
        }
        try {
            service.init(bundle);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "init RemoteException");
            }
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
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "registerProcessStateChangeObserver catch RemoteException");
            }
        }
        return false;
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
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "unRegisterProcessStateChangeObserver catch RemoteException");
            }
        }
        return false;
    }

    public void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "getStatisticsData service is null ");
            }
            return;
        }
        try {
            service.noteProcessStart(packageName, processName, pid, uid, started, launcherMode, reason);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "getStatisticsData RemoteException");
            }
        }
    }

    public String saveBigData(int featureId, boolean clear) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "saveBigData service is null ");
            }
            return null;
        }
        try {
            return service.saveBigData(featureId, clear);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "saveBigData RemoteException");
            }
            return null;
        }
    }

    public void updateFakeForegroundList(List<String> processList) {
        IHwSysResManager service = getService();
        if (service == null) {
            if (Log.HWINFO) {
                Log.e(TAG, "updateFakeForegroundList service is null ");
            }
            return;
        }
        try {
            service.updateFakeForegroundList(processList);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.e(TAG, "updateFakeForegroundList RemoteException");
            }
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
}
