package com.android.server.rms.iaware;

import android.os.Binder;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DataContract.Apps;
import android.rms.iaware.DataContract.Apps.Builder;
import android.rms.iaware.LogIAware;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.cpu.CPUResourceConfigControl;
import com.huawei.android.app.IHwDAMonitorCallback.Stub;

class AwareAmsMonitorCallback extends Stub {
    private static final String ACTIVITY_DESTROYED = "DESTROYED";
    private static final String ACTIVITY_RESUME = "RESUMED";
    private static final int ACTIVITY_STATE_INFO_LENGTH = 5;
    private static final String ACTIVITY_STOPPED = "STOPPED";
    private static final String TAG = "AwareDAMonitorCallback";

    AwareAmsMonitorCallback() {
    }

    public int getActivityImportCount() {
        return 2;
    }

    public String getRecentTask() {
        return AwareAppMngSort.ACTIVITY_RECENT_TASK;
    }

    public int isCPUConfigWhiteList(String processName) {
        return CPUResourceConfigControl.getInstance().isWhiteList(processName);
    }

    public int getCPUConfigGroupBG() {
        return 1;
    }

    public boolean isCpusetEnable() {
        return CPUFeature.isCpusetEnable();
    }

    public int getFirstDevSchedEventId() {
        return 2100;
    }

    public void notifyActivityState(String activityInfo) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null) {
            if (!resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RES_APP))) {
                return;
            }
            if (isInvalidStr(activityInfo)) {
                AwareLog.e(TAG, "invalid str. activityInfo : " + activityInfo);
                return;
            }
            String[] info = activityInfo.split(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
            if (5 != info.length) {
                AwareLog.e(TAG, "info error. activityInfo : " + activityInfo);
                return;
            }
            try {
                String packageName = info[0];
                String activityName = info[1];
                int uid = Integer.parseInt(info[2]);
                int pid = Integer.parseInt(info[3]);
                String state = info[4];
                if (isInvalidActivityInfo(packageName, activityName, state, uid, pid)) {
                    AwareLog.e(TAG, "invalid activity info, activityInfo : " + activityInfo);
                    return;
                }
                int event;
                if (ACTIVITY_RESUME.equals(state)) {
                    event = 15019;
                } else if ("STOPPED".equals(state) || ACTIVITY_DESTROYED.equals(state)) {
                    event = 85019;
                } else {
                    AwareLog.e(TAG, "state out of control, state : " + state);
                    return;
                }
                Builder builder = Apps.builder();
                builder.addEvent(event);
                builder.addCalledApp(packageName, null, activityName, pid, uid);
                CollectData appsData = builder.build();
                long id = Binder.clearCallingIdentity();
                resManager.reportData(appsData);
                Binder.restoreCallingIdentity(id);
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "NumberFormatException, noteActivityInfo : " + activityInfo);
            } catch (ArrayIndexOutOfBoundsException e2) {
                AwareLog.e(TAG, "ArrayIndexOutOfBoundsException, noteActivityInfo : " + activityInfo);
            }
        }
    }

    private boolean isInvalidStr(String str) {
        return str != null ? str.trim().isEmpty() : true;
    }

    private boolean isInvalidActivityInfo(String packageName, String activityName, String state, int uid, int pid) {
        if (packageName == null || activityName == null || state == null || uid <= 1000 || pid < 0) {
            return true;
        }
        return false;
    }

    public int DAMonitorReport(int tag, String msg) {
        return LogIAware.report(tag, msg);
    }
}
