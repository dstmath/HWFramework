package com.android.server.rms.iaware;

import android.content.pm.ApplicationInfo;
import android.hwrme.HwPrommEventManager;
import android.hwrme.HwResMngEngine;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DataContract;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.android.server.rms.iaware.cpu.CpuFeatureAmsCommunicator;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.android.app.IHwAtmDAMonitorCallback;
import com.huawei.android.content.pm.IPackageManagerEx;

class AwareAtmsMonitorCallback extends IHwAtmDAMonitorCallback.Stub {
    private static final String ACTIVITY_DESTROYED = "DESTROYED";
    private static final String ACTIVITY_RESUME = "RESUMED";
    private static final int ACTIVITY_STATE_INFO_LENGTH = 5;
    private static final String ACTIVITY_STOPPED = "STOPPED";
    private static final String EMPTY_STRING = "";
    private static final String TAG = "AwareAtmDAMonitorCallback";
    private boolean isStartedActivity = false;

    AwareAtmsMonitorCallback() {
    }

    public void noteActivityStart(String[] procInfos, int pid, int uid, boolean started) {
        if (procInfos != null && procInfos.length == 3 && HwSysResManager.getInstance().isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RES_APP))) {
            DataContract.Apps.Builder builder = DataContract.Apps.builder();
            builder.addEvent(started ? 15005 : 85005);
            builder.addCalledApp(procInfos[0], procInfos[1], procInfos[2], pid, uid);
            CollectData appsData = builder.build();
            long id = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(appsData);
            Binder.restoreCallingIdentity(id);
        }
    }

    public void notifyAppEventToIaware(int type, String packageName) {
        CpuFeatureAmsCommunicator.getInstance().setTopAppToBoost(type, packageName);
        if (type == 3) {
            try {
                ApplicationInfo ai = IPackageManagerEx.getApplicationInfo(packageName, 0, 0);
                if (ai != null) {
                    HwResMngEngine.getInstance().sendMmEvent(0, ai.uid);
                }
            } catch (RemoteException e) {
                AwareLog.e(TAG, "promm package not found!");
            }
        }
    }

    public boolean isResourceNeeded(String resourceId) {
        return HwSysResManager.getInstance().isResourceNeeded(getResourceId(resourceId));
    }

    private int getResourceId(String resourceId) {
        if (resourceId == null) {
            return AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_INVALIDE_TYPE);
        }
        if ("RESOURCE_APPASSOC".equals(resourceId)) {
            return AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC);
        }
        return AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_INVALIDE_TYPE);
    }

    public void reportData(String resourceId, long timeStamp, Bundle args) {
        if (args != null) {
            CollectData data = new CollectData(getResourceId(resourceId), timeStamp, args);
            long id = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    public void recognizeFakeActivity(String compName, int pid, int uid) {
        AwareFakeActivityRecg.self().recognizeFakeActivity(compName, pid, uid);
    }

    private boolean isInvalidActivityInfo(String packageName, String activityName, String state, int uid, int pid) {
        if (packageName == null || activityName == null || state == null || uid <= 1000 || pid < 0) {
            return true;
        }
        return false;
    }

    private boolean isInvalidStr(String str) {
        return str == null || str.trim().isEmpty();
    }

    public void notifyActivityState(String activityInfo) {
        int event;
        if (HwSysResManager.getInstance().isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RES_APP))) {
            if (isInvalidStr(activityInfo)) {
                AwareLog.e(TAG, "invalid str. activityInfo : " + activityInfo);
                return;
            }
            String[] info = activityInfo.split(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
            if (info.length != 5) {
                AwareLog.e(TAG, "info error. activityInfo : " + activityInfo);
                return;
            }
            String packageName = info[0];
            String activityName = info[1];
            try {
                int uid = Integer.parseInt(info[2]);
                try {
                    int pid = Integer.parseInt(info[3]);
                    String state = info[4];
                    if (isInvalidActivityInfo(packageName, activityName, state, uid, pid)) {
                        AwareLog.e(TAG, "invalid activity info, activityInfo : " + activityInfo);
                        return;
                    }
                    if (ACTIVITY_RESUME.equals(state)) {
                        event = 15019;
                    } else if ("STOPPED".equals(state) || ACTIVITY_DESTROYED.equals(state)) {
                        event = 85019;
                    } else {
                        AwareLog.e(TAG, "state out of control, state : " + state);
                        return;
                    }
                    DataContract.Apps.Builder builder = DataContract.Apps.builder();
                    builder.addEvent(event);
                    builder.addCalledApp(packageName, (String) null, activityName, pid, uid);
                    CollectData appsData = builder.build();
                    long id = Binder.clearCallingIdentity();
                    HwSysResManager.getInstance().reportData(appsData);
                    Binder.restoreCallingIdentity(id);
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "NumberFormatException, noteActivityInfo : " + activityInfo);
                }
            } catch (NumberFormatException e2) {
                AwareLog.e(TAG, "NumberFormatException, noteActivityInfo : " + activityInfo);
            }
        }
    }

    public void noteActivityDisplayed(String componentName, int uid, int pid, boolean isStart) {
        if (componentName != null && pid > 0) {
            if (isStart || this.isStartedActivity) {
                this.isStartedActivity = isStart;
                if (HwSysResManager.getInstance().isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RES_APP))) {
                    DataContract.Apps.Builder builder = DataContract.Apps.builder();
                    if (!isStart) {
                        MemoryConstant.setDisplayStartedActivityName("");
                        builder.addEvent(85013);
                    } else if (!componentName.equals(MemoryConstant.getDisplayStartedActivityName())) {
                        MemoryConstant.setDisplayStartedActivityName(componentName);
                        builder.addEvent(15013);
                    } else {
                        return;
                    }
                    builder.addActivityDisplayedInfoWithUid(componentName, uid, pid, 0);
                    CollectData appsData = builder.build();
                    long id = Binder.clearCallingIdentity();
                    HwSysResManager.getInstance().reportData(appsData);
                    Binder.restoreCallingIdentity(id);
                    HwPrommEventManager prommEventMng = HwPrommEventManager.getInstance();
                    if (prommEventMng != null) {
                        prommEventMng.getActivityDisplayed(uid);
                    }
                }
            }
        }
    }
}
