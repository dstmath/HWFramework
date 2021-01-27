package com.huawei.server;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IDeviceSettingCallback;
import android.rms.iaware.ISceneCallback;
import android.rms.iaware.NetLocationStrategy;
import android.rms.iaware.RPolicyData;
import android.rms.iaware.memrepair.MemRepairPkgInfo;
import android.view.View;
import android.view.WindowManager;
import com.android.server.AlarmManagerServiceExt;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.mtm.DefaultMultiTaskManagerService;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.CloudPushManager;
import com.android.server.mtm.iaware.appmng.appfreeze.AwareAppFreezeMng;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.iaware.appmng.appstart.comm.AppStartupUtil;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.AppStartupInfo;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.BaseAppStartupInfo;
import com.android.server.mtm.iaware.appmng.appswap.AwareAppSwapMng;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.algorithm.ActivityTopManagerRt;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.collector.DefaultMemInfoReader;
import com.android.server.rms.collector.MemInfoReader;
import com.android.server.rms.collector.ProcMemInfoReader;
import com.android.server.rms.config.DefaultHwConfigReader;
import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.dump.DumpCaseIaware;
import com.android.server.rms.iaware.AwareCallback;
import com.android.server.rms.iaware.DefaultRdaService;
import com.android.server.rms.iaware.DmeDispatcher;
import com.android.server.rms.iaware.HwStartWindowCache;
import com.android.server.rms.iaware.RdaService;
import com.android.server.rms.iaware.appmng.ActivityEventManager;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.appmng.AwareGameModeRecg;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.android.server.rms.iaware.appmng.AwareWakeUpManager;
import com.android.server.rms.iaware.appmng.ContinuePowerDevMng;
import com.android.server.rms.iaware.appmng.DefaultContinuePowerDevMng;
import com.android.server.rms.iaware.appmng.FreezeDataManager;
import com.android.server.rms.iaware.bigdata.AwareBigDataFile;
import com.android.server.rms.iaware.feature.AlarmManagerFeature;
import com.android.server.rms.iaware.feature.DevSchedFeatureRt;
import com.android.server.rms.iaware.feature.StartWindowFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.qos.AwareBinderSchedManager;
import com.android.server.rms.iaware.resource.StartResParallelManager;
import com.android.server.rms.iaware.srms.BroadcastFeature;
import com.android.server.rms.iaware.srms.ResourceFeature;
import com.android.server.rms.iaware.srms.SrmsDumpRadar;
import com.android.server.rms.iaware.sysload.SysLoadManager;
import com.android.server.rms.memrepair.MemRepairPolicy;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.server.rme.hyperhold.AdvancedKillerAwareUtils;
import com.huawei.server.rme.hyperhold.AdvancedKillerAwareUtilsImpl;
import com.huawei.server.wm.WindowProcessControllerEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HwPartIawareServiceFactoryExImpl extends HwPartIawareServiceFactoryEx {
    private static final String TAG = "HwPartIawareServiceFactoryExImpl";
    private final ProcMemInfoReader mProcMemInfoReader = new ProcMemInfoReader();

    public DefaultMultiTaskManagerService getMultiTaskManagerService(Context context) {
        return new MultiTaskManagerService(context);
    }

    public void reportCloudUpdate(Bundle bundle) {
        CloudPushManager.getInstance().reportCloudUpdate(bundle);
    }

    public boolean isEvilWindow(int window, int code, int type) {
        return AwareAppFreezeMng.getInstance().isEvilWindow(window, code, type);
    }

    public boolean shouldPreventRestartService(ServiceInfo serviceInfo, boolean isRealStart) {
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy != null) {
            return appStartupPolicy.shouldPreventRestartService(serviceInfo, isRealStart);
        }
        return false;
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid, WindowProcessControllerEx callerApp) {
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy != null) {
            return appStartupPolicy.shouldPreventStartProvider(cpi, callerPid, callerUid, callerApp);
        }
        return false;
    }

    public boolean shouldPreventStartService(BaseAppStartupInfo baseAppStartupInfo, ServiceInfo servInfo, int servFlag, boolean servExist) {
        AwareAppStartupPolicy appStartupPolicy;
        if (baseAppStartupInfo == null || (appStartupPolicy = AwareAppStartupPolicy.self()) == null) {
            return false;
        }
        AppStartupInfo appStartupInfo = new AppStartupInfo();
        appStartupInfo.setCallerPid(baseAppStartupInfo.callerPid).setCallerUid(baseAppStartupInfo.callerUid).setCallerApp(baseAppStartupInfo.callerApp).setIntent(baseAppStartupInfo.intent);
        return appStartupPolicy.shouldPreventStartService(appStartupInfo, servInfo, servFlag, servExist);
    }

    public boolean shouldPreventSendReceiver(ResolveInfo resolveInfo, BaseAppStartupInfo baseAppStartupInfo) {
        AwareAppStartupPolicy appStartupPolicy;
        if (baseAppStartupInfo == null || (appStartupPolicy = AwareAppStartupPolicy.self()) == null) {
            return false;
        }
        AppStartupInfo appStartupInfo = new AppStartupInfo();
        appStartupInfo.setIntent(baseAppStartupInfo.intent).setCallerPid(baseAppStartupInfo.callerPid).setCallerUid(baseAppStartupInfo.callerUid).setTargetApp(baseAppStartupInfo.targetApp).setCallerApp(baseAppStartupInfo.callerApp);
        return appStartupPolicy.shouldPreventSendReceiver(appStartupInfo, resolveInfo);
    }

    public void setProtectedListFromMdm(Context context, List<String> protectedList) {
        ProcessCleaner.getInstance(context).setProtectedListFromMdm(protectedList);
    }

    public void removeProtectedListFromMdm(Context context) {
        ProcessCleaner.getInstance(context).removeProtectedListFromMdm();
    }

    public boolean shouldPreventStartActivity(Intent intent, ActivityInfo activityInfo, int callerPid, int callerUid, WindowProcessControllerEx callerApp) {
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy == null) {
            return false;
        }
        return appStartupPolicy.shouldPreventStartActivity(intent, activityInfo, callerPid, callerUid, callerApp);
    }

    public void reportTopActData(Bundle bdl) {
        ActivityTopManagerRt habit = ActivityTopManagerRt.obtainExistInstance();
        if (habit != null) {
            habit.reportTopActData(bdl);
        }
    }

    public List<String> getForceProtectApps(int num) {
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null && habit.isEnable()) {
            return habit.getForceProtectApps(num);
        }
        AwareLog.d(TAG, "habit is null or habit is disable");
        return new ArrayList(0);
    }

    public List<String> recognizeLongTimeRunningApps() {
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit == null || !habit.isEnable()) {
            return new ArrayList(0);
        }
        return habit.recognizeLongTimeRunningApps();
    }

    public List<String> getMostFrequentUsedApp(int num, int minCount) {
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit == null || !habit.isEnable()) {
            return null;
        }
        return habit.getMostFrequentUsedApp(num, minCount);
    }

    public void reportHabitData(Bundle bdl) {
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null && habit.isEnable()) {
            habit.reportHabitData(bdl);
        }
    }

    public List<String> getTopN(int num) {
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit == null || !habit.isEnable()) {
            return null;
        }
        return habit.getTopN(num);
    }

    public long getProcessPssByPid(int pid) {
        return this.mProcMemInfoReader.getProcessPssByPid(pid);
    }

    public int getPidForProcName(String[] procName) {
        return this.mProcMemInfoReader.getPidForProcName(procName);
    }

    public boolean dumpIaware(Context context, PrintWriter pw, String[] args) {
        return DumpCaseIaware.dump(context, pw, args);
    }

    public void registerProcessObserver(IProcessObserverEx observer) {
        AwareCallback.getInstance().registerProcessObserver(observer);
    }

    public void dispatchPolicy(RPolicyData policy) {
        DmeDispatcher.dispatchPolicy(policy);
    }

    public void setHwStartWindowCacheHandler(Handler handler) {
        HwStartWindowCache.getInstance().setHandler(handler);
    }

    public View tryAddViewFromCache(String packageName, IBinder appToken, Configuration config) {
        return HwStartWindowCache.getInstance().tryAddViewFromCache(packageName, appToken, config);
    }

    public void putViewToCache(String packageName, View startView, WindowManager.LayoutParams params) {
        HwStartWindowCache.getInstance().putViewToCache(packageName, startView, params);
    }

    public void reportSceneInfos(Bundle bdl) {
        ActivityEventManager.getInstance().reportSceneInfos(bdl);
    }

    public boolean isVisibleWindows(int userId, String pkg) {
        return AwareAppAssociate.getInstance().isVisibleWindows(userId, pkg);
    }

    public boolean isWidgetVisible(Bundle options) {
        return AwareAppAssociate.getInstance().isWidgetVisible(options);
    }

    public boolean shouldPreventStartActivity(ActivityInfo activityInfo, int callerPid, int callerUid) {
        return AwareFakeActivityRecg.self().shouldPreventStartActivity(activityInfo, callerPid, callerUid);
    }

    public void processNativeEventNotify(int eventType, int eventValue, int keyAction, int pid, int uid) {
        AwareFakeActivityRecg.self().processNativeEventNotify(eventType, eventValue, keyAction, pid, uid);
    }

    public void setFingerprintWakeup(boolean isWakeup) {
        AwareFakeActivityRecg.self().setFingerprintWakeup(isWakeup);
    }

    public void notifyWakeupResult(boolean isWakenupThisTime) {
        AwareFakeActivityRecg.self().notifyWakeupResult(isWakenupThisTime);
    }

    public void notifyPowerkeyInteractive(boolean isInteractive) {
        AwareFakeActivityRecg.self().notifyPowerkeyInteractive(isInteractive);
    }

    public void setImsForAwareFakeActivityRecg(InputManagerServiceEx inputManagerService) {
        AwareFakeActivityRecg.self().setInputManagerService(inputManagerService);
    }

    public void setImsForAwareGameModeRecg(InputManagerServiceEx inputManagerService) {
        AwareGameModeRecg.getInstance().setInputManagerService(inputManagerService);
    }

    public void setImsForSysLoadManager(InputManagerServiceEx inputManagerService) {
        SysLoadManager.getInstance().setInputManagerService(inputManagerService);
    }

    public void registerCallback(ISceneCallback callback, int scene) {
        SysLoadManager.getInstance().registerCallback(callback, scene);
    }

    public boolean isToastWindows(int userId, String pkg) {
        return AwareIntelligentRecg.getInstance().isToastWindows(userId, pkg);
    }

    public Bundle getTypeTopN(int[] appTypes) {
        return AwareIntelligentRecg.getInstance().getTypeTopN(appTypes);
    }

    public int getAppMngSpecType(String pkg) {
        return AwareIntelligentRecg.getInstance().getAppMngSpecType(pkg);
    }

    public String getActTopImcn() {
        return AwareIntelligentRecg.getInstance().getActTopIMCN();
    }

    public boolean isAwareSceneRecognizeEnable() {
        return AwareSceneRecognize.isEnable();
    }

    public void modifyAlarmIfOverload(AlarmManagerServiceExt.AlarmEx alarm) {
        AwareWakeUpManager.getInstance().modifyAlarmIfOverload(alarm);
    }

    public void reportWakeupAlarms(ArrayList<AlarmManagerServiceExt.AlarmEx> alarms) {
        AwareWakeUpManager.getInstance().reportWakeupAlarms(alarms);
    }

    public void reportWakeupSystem(String reason) {
        AwareWakeUpManager.getInstance().reportWakeupSystem(reason);
    }

    public void initAwareWakeUpManager(Handler handler, Context context) {
        AwareWakeUpManager.getInstance().init(handler, context);
    }

    public DefaultContinuePowerDevMng getContinuePowerDevMng() {
        return ContinuePowerDevMng.getInstance();
    }

    public List<String> getFrequentIm(int count) {
        return FreezeDataManager.getInstance().getFrequentIm(count);
    }

    public void writeBigdataFile(String featureData, String newFileName, String filePath) {
        if (featureData != null && newFileName != null) {
            new AwareBigDataFile(filePath).saveData(featureData, newFileName, 0);
        }
    }

    public boolean isAwareAlarmManagerEnabled() {
        return AlarmManagerFeature.isEnable();
    }

    public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
        return DevSchedFeatureRt.getNetLocationStrategy(pkgName, uid, type);
    }

    public void registerDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        DevSchedFeatureRt.registerDevModeMethod(deviceId, callback, args);
    }

    public void unregisterDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        DevSchedFeatureRt.unregisterDevModeMethod(deviceId, callback, args);
    }

    public boolean getConcurrentSwitch() {
        return StartWindowFeature.getConcurrentSwitch();
    }

    public boolean isStartWindowEnable() {
        return StartWindowFeature.isStartWindowEnable();
    }

    public boolean isNotificatinSwitchEnable() {
        return MemoryConstant.isNotificatinSwitchEnable();
    }

    public long getNotificationInterval() {
        return MemoryConstant.getNotificationInterval();
    }

    public long getMemAvailable() {
        MemoryReader memoryReader = MemoryReader.getInstance();
        if (memoryReader != null) {
            return memoryReader.getMemAvailable();
        }
        return 0;
    }

    public void reclaimProcessAll(int pid, boolean suspend) {
        MemoryUtils.reclaimProcessAll(pid, suspend);
    }

    public void setRtgThreadForAnimation(boolean flag) {
        AwareBinderSchedManager.getInstance().setRtgThreadForAnimation(flag);
    }

    public void applyRtgPolicy(int tid, int enable) {
        StartResParallelManager.getInstance().applyRtgPolicy(tid, enable);
    }

    public boolean isPreloadEnable() {
        return StartResParallelManager.getInstance().isPreloadEnable();
    }

    public boolean isFeatureEnabled(int feature) {
        return BroadcastFeature.isFeatureEnabled(feature);
    }

    public boolean getIawareResourceFeature(int type) {
        return ResourceFeature.getIawareResourceFeature(type);
    }

    public void updateStatisticsData(int subTypeCode) {
        SrmsDumpRadar.getInstance().updateStatisticsData(subTypeCode);
    }

    public List<MemRepairPkgInfo> getMemRepairPolicy(int sceneType) {
        return MemRepairPolicy.getInstance().getMemRepairPolicy(sceneType);
    }

    public DefaultRdaService getRdaService(Context context, HandlerThread handlerThread) {
        return new RdaService(context, handlerThread);
    }

    public DefaultHwConfigReader getHwConfigReader() {
        return new HwConfigReader();
    }

    public DefaultMemInfoReader getMemInfoReader() {
        return new MemInfoReader();
    }

    public AdvancedKillerAwareUtils getAdvancedKillerAwareUtils(Context context) {
        return new AdvancedKillerAwareUtilsImpl(context);
    }

    public List<String> getCtsPkgs() {
        return AppStartupUtil.getCtsPkgs();
    }

    public String dumpHwStartWindowCache() {
        return HwStartWindowCache.getInstance().dump();
    }

    public int getAppSwapIndex(String pkgName) {
        return AwareAppSwapMng.getInstance().getAppSwapIndex(pkgName);
    }

    public Map<String, Integer> getAllAppSwapIndex() {
        return AwareAppSwapMng.getInstance().getAllAppSwapIndex();
    }
}
