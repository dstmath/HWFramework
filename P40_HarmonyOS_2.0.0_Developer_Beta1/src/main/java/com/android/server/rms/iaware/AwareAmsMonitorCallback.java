package com.android.server.rms.iaware;

import android.app.mtm.MultiTaskManager;
import android.os.Binder;
import android.os.Bundle;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.AwareNRTConstant;
import android.rms.iaware.CollectData;
import android.rms.iaware.DataContract;
import android.rms.iaware.LogIAware;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.android.server.rms.iaware.cpu.CpuKeyBackground;
import com.android.server.rms.iaware.cpu.CpuResourceConfigControl;
import com.android.server.rms.iaware.cpu.CpuVipThread;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.qos.AwareBinderSchedManager;
import com.android.server.rms.iaware.qos.AwareQosFeatureManager;
import com.android.server.rms.memrepair.ProcStateStatisData;
import com.huawei.android.app.IHwDAMonitorCallback;
import java.util.ArrayList;

class AwareAmsMonitorCallback extends IHwDAMonitorCallback.Stub {
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

    public int isCpuConfigWhiteList(String processName) {
        return CpuResourceConfigControl.getInstance().isWhiteList(processName);
    }

    public int getCpuConfigGroupBg() {
        return 1;
    }

    public int getFirstDevSchedEventId() {
        return AwareNRTConstant.FIRST_DEV_SCHED_EVENT_ID;
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

    private boolean isInvalidStr(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isInvalidActivityInfo(String packageName, String activityName, String state, int uid, int pid) {
        return packageName == null || activityName == null || state == null || uid <= 1000 || pid < 0;
    }

    public void reportScreenRecord(int uid, int pid, int status) {
        if (HwSysResManager.getInstance().isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            int reportStatus = status == 0 ? 26 : 25;
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt("callUid", uid);
            bundleArgs.putInt("callPid", pid);
            bundleArgs.putInt("relationType", reportStatus);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long origId = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(data);
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void reportCamera(int uid, int status) {
        if (uid > 0 && uid != 1000 && HwSysResManager.getInstance().isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            int reportStatus = status == 0 ? 31 : 30;
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt("callUid", uid);
            bundleArgs.putInt("relationType", reportStatus);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long origId = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(data);
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void notifyProcessGroupChangeCpu(int pid, int uid, int renderThreadTid, int grp) {
        CpuKeyBackground.getInstance().notifyProcessGroupChange(pid, uid, grp);
        CpuVipThread.getInstance().notifyProcessGroupChange(pid, renderThreadTid, grp);
    }

    public void setVipThread(int uid, int pid, int renderThreadTid, boolean isSet, boolean isSetGroup) {
        ArrayList<Integer> tidStrs = new ArrayList<>();
        tidStrs.add(Integer.valueOf(pid));
        tidStrs.add(Integer.valueOf(renderThreadTid));
        CpuVipThread.getInstance().setAppVipThread(pid, tidStrs, isSet, isSetGroup);
        if (isSet) {
            AwareBinderSchedManager.getInstance().reportFgChanged(pid, uid, true);
            AwareQosFeatureManager.getInstance().setAppVipThreadForQos(pid, tidStrs);
        }
    }

    public void onPointerEvent(int action) {
        if ((action == 0 || action == 1) && HwSysResManager.getInstance().isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RES_INPUT))) {
            DataContract.Input.Builder builder = DataContract.Input.builder();
            builder.addEvent(action == 0 ? 10001 : 80001);
            CollectData appsData = builder.build();
            long id = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(appsData);
            Binder.restoreCallingIdentity(id);
        }
    }

    public void addPssToMap(String[] procInfos, int[] procIds, long[] pssValues, boolean test) {
        if (procInfos != null && procInfos.length == 2 && procIds != null && procIds.length == 3 && pssValues != null && pssValues.length == 3) {
            ProcStateStatisData.getInstance().addPssToMap(new ProcStateStatisData.ProcAttribute(procInfos[0], procInfos[1], procIds[0], procIds[1], procIds[2]), pssValues[0], pssValues[1], pssValues[2], test);
        }
    }

    public void reportAppDiedMsg(int userId, String processName, String reason) {
        if (processName != null && !processName.contains(":") && reason != null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(processName);
            stringBuffer.append(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
            stringBuffer.append(String.valueOf(userId));
            stringBuffer.append(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
            stringBuffer.append(reason);
            LogIAware.report(AwareNRTConstant.APP_KILLED_EVENT_ID, stringBuffer.toString());
        }
    }

    public int killProcessGroupForQuickKill(int uid, int pid) {
        return MemoryUtils.killProcessGroupForQuickKill(uid, pid);
    }

    public void noteProcessStart(String[] procInfos, int pid, int uid, boolean started) {
        if (procInfos != null && procInfos.length == 4 && HwSysResManager.getInstance().isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RES_APP))) {
            DataContract.Apps.Builder builder = DataContract.Apps.builder();
            builder.addEvent(started ? 15001 : 85001);
            builder.addLaunchCalledApp(procInfos[0], procInfos[1], procInfos[2], procInfos[3], new int[]{pid, uid});
            CollectData appsData = builder.build();
            long id = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(appsData);
            Binder.restoreCallingIdentity(id);
        }
    }

    public void onWakefulnessChanged(int wakefulness) {
        AwareFakeActivityRecg.self().onWakefulnessChanged(wakefulness);
    }

    public void notifyProcessGroupChange(int pid, int uid) {
        MultiTaskManager handler = MultiTaskManager.getInstance();
        if (handler != null) {
            handler.notifyProcessGroupChange(pid, uid);
        }
    }

    public void notifyProcessStatusChange(String pkg, String process, String hostingType, int pid, int uid) {
        MultiTaskManager handler = MultiTaskManager.getInstance();
        if (handler != null) {
            handler.notifyProcessStatusChange(pkg, process, hostingType, pid, uid);
        }
        CpuVipThread.getInstance().setThreadVipAndQos(pid, hostingType);
    }

    public void notifyProcessWillDie(boolean[] dieReasons, String packageName, int pid, int uid) {
        if (dieReasons != null && dieReasons.length == 3) {
            AwareFakeActivityRecg.self().notifyProcessWillDie(dieReasons, packageName, pid, uid);
        }
    }

    public void notifyProcessDied(int pid, int uid) {
        MultiTaskManager handler = MultiTaskManager.getInstance();
        if (handler != null) {
            handler.notifyProcessDiedChange(pid, uid);
        }
    }

    public int resetAppMngOomAdj(int maxAdj, String packageName) {
        if (maxAdj <= 260 || !AwareAppMngSort.checkAppMngEnable() || !AwareDefaultConfigList.getInstance().isAppMngOomAdjCustomized(packageName)) {
            return maxAdj;
        }
        return AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ;
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
        if ("RESOURCE_WINSTATE".equals(resourceId)) {
            return AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_WINSTATE);
        }
        if ("RESOURCE_SET_HM_THREAD_RTG".equals(resourceId)) {
            return AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_SET_HM_THREAD_RTG);
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

    public boolean isExcludedInBgCheck(String pkg, String action) {
        return AwareIntelligentRecg.getInstance().isExcludedInBgCheck(pkg, action);
    }

    public void noteActivityDisplayedStart(String componentName, int uid, int pid) {
        if (componentName != null && pid > 0 && HwSysResManager.getInstance().isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RES_APP))) {
            DataContract.Apps.Builder builder = DataContract.Apps.builder();
            builder.addEvent(15013);
            builder.addActivityDisplayedInfoWithUid(componentName, uid, pid, 0);
            CollectData appsData = builder.build();
            long id = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(appsData);
            Binder.restoreCallingIdentity(id);
        }
    }

    public boolean isFastKillSwitch(String processName, int uid) {
        ProcessCleaner cleaner;
        if ((MemoryConstant.isFastKillSwitch() || MemoryConstant.isFastQuickKillSwitch()) && (cleaner = ProcessCleaner.getInstance()) != null) {
            return cleaner.isProcessFastKillLocked(processName, uid);
        }
        return false;
    }
}
