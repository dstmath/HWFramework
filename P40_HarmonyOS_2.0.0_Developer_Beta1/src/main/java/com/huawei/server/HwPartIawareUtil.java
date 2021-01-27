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
import android.rms.iaware.IDeviceSettingCallback;
import android.rms.iaware.ISceneCallback;
import android.rms.iaware.NetLocationStrategy;
import android.rms.iaware.RPolicyData;
import android.rms.iaware.memrepair.MemRepairPkgInfo;
import android.view.View;
import android.view.WindowManager;
import com.android.server.AlarmManagerServiceExt;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.BaseAppStartupInfo;
import com.android.server.rms.collector.DefaultMemInfoReader;
import com.android.server.rms.config.DefaultHwConfigReader;
import com.android.server.rms.iaware.DefaultRdaService;
import com.android.server.rms.iaware.appmng.DefaultContinuePowerDevMng;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.annotation.HwSystemApi;
import com.huawei.server.rme.hyperhold.AdvancedKillerAwareUtils;
import com.huawei.server.wm.WindowProcessControllerEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@HwSystemApi
public class HwPartIawareUtil {
    private static final String IAWARE_SERVICE_FACTORY_EX_IMPL_NAME = "com.huawei.server.HwPartIawareServiceFactoryExImpl";

    public static void reportCloudUpdate(Bundle bundle) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").reportCloudUpdate(bundle);
    }

    public static boolean isEvilWindow(int window, int code, int type) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").isEvilWindow(window, code, type);
    }

    public static boolean shouldPreventRestartService(ServiceInfo serviceInfo, boolean isRealStart) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").shouldPreventRestartService(serviceInfo, isRealStart);
    }

    public static boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid, WindowProcessControllerEx callerApp) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").shouldPreventStartProvider(cpi, callerPid, callerUid, callerApp);
    }

    public static boolean shouldPreventStartService(BaseAppStartupInfo baseAppStartupInfo, ServiceInfo servInfo, int servFlag, boolean servExist) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").shouldPreventStartService(baseAppStartupInfo, servInfo, servFlag, servExist);
    }

    public static boolean shouldPreventSendReceiver(ResolveInfo resolveInfo, BaseAppStartupInfo baseAppStartupInfo) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").shouldPreventSendReceiver(resolveInfo, baseAppStartupInfo);
    }

    public static void setProtectedListFromMdm(Context context, List<String> protectedList) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").setProtectedListFromMdm(context, protectedList);
    }

    public static void removeProtectedListFromMdm(Context context) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").removeProtectedListFromMdm(context);
    }

    public static boolean shouldPreventStartActivity(Intent intent, ActivityInfo activityInfo, int callerPid, int callerUid, WindowProcessControllerEx callerApp) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").shouldPreventStartActivity(intent, activityInfo, callerPid, callerUid, callerApp);
    }

    public static void reportTopActData(Bundle bdl) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").reportTopActData(bdl);
    }

    public static List<String> getForceProtectApps(int num) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getForceProtectApps(num);
    }

    public static List<String> recognizeLongTimeRunningApps() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").recognizeLongTimeRunningApps();
    }

    public static List<String> getMostFrequentUsedApp(int num, int minCount) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getMostFrequentUsedApp(num, minCount);
    }

    public static void reportHabitData(Bundle bdl) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").reportHabitData(bdl);
    }

    public static List<String> getTopN(int num) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getTopN(num);
    }

    public static long getProcessPssByPid(int pid) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getProcessPssByPid(pid);
    }

    public static int getPidForProcName(String[] procName) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getPidForProcName(procName);
    }

    public static boolean dumpIaware(Context context, PrintWriter pw, String[] args) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").dumpIaware(context, pw, args);
    }

    public static void registerProcessObserver(IProcessObserverEx observer) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").registerProcessObserver(observer);
    }

    public static void dispatchPolicy(RPolicyData policy) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").dispatchPolicy(policy);
    }

    public static void setHwStartWindowCacheHandler(Handler handler) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").setHwStartWindowCacheHandler(handler);
    }

    public static View tryAddViewFromCache(String packageName, IBinder appToken, Configuration config) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").tryAddViewFromCache(packageName, appToken, config);
    }

    public static void putViewToCache(String packageName, View startView, WindowManager.LayoutParams params) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").putViewToCache(packageName, startView, params);
    }

    public static void reportSceneInfos(Bundle bdl) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").reportSceneInfos(bdl);
    }

    public static boolean isVisibleWindows(int userId, String pkg) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").isVisibleWindows(userId, pkg);
    }

    public static boolean isWidgetVisible(Bundle options) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").isWidgetVisible(options);
    }

    public static boolean shouldPreventStartActivity(ActivityInfo activityInfo, int callerPid, int callerUid) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").shouldPreventStartActivity(activityInfo, callerPid, callerUid);
    }

    public static void processNativeEventNotify(int eventType, int eventValue, int keyAction, int pid, int uid) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").processNativeEventNotify(eventType, eventValue, keyAction, pid, uid);
    }

    public static void setFingerprintWakeup(boolean isWakeup) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").setFingerprintWakeup(isWakeup);
    }

    public static void notifyWakeupResult(boolean isWakenupThisTime) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").notifyWakeupResult(isWakenupThisTime);
    }

    public static void notifyPowerkeyInteractive(boolean isInteractive) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").notifyPowerkeyInteractive(isInteractive);
    }

    public static void setImsForAwareFakeActivityRecg(InputManagerServiceEx inputManagerService) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").setImsForAwareFakeActivityRecg(inputManagerService);
    }

    public static void setImsForAwareGameModeRecg(InputManagerServiceEx inputManagerService) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").setImsForAwareGameModeRecg(inputManagerService);
    }

    public static void setImsForSysLoadManager(InputManagerServiceEx inputManagerService) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").setImsForSysLoadManager(inputManagerService);
    }

    public static void registerCallback(ISceneCallback callback, int scene) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").registerCallback(callback, scene);
    }

    public static boolean isToastWindows(int userId, String pkg) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").isToastWindows(userId, pkg);
    }

    public static Bundle getTypeTopN(int[] appTypes) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getTypeTopN(appTypes);
    }

    public static int getAppMngSpecType(String pkg) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getAppMngSpecType(pkg);
    }

    public static String getActTopImcn() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getActTopImcn();
    }

    public static boolean isAwareSceneRecognizeEnable() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").isAwareSceneRecognizeEnable();
    }

    public static void modifyAlarmIfOverload(AlarmManagerServiceExt.AlarmEx alarm) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").modifyAlarmIfOverload(alarm);
    }

    public static void reportWakeupAlarms(ArrayList<AlarmManagerServiceExt.AlarmEx> alarms) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").reportWakeupAlarms(alarms);
    }

    public static void reportWakeupSystem(String reason) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").reportWakeupSystem(reason);
    }

    public static void initAwareWakeUpManager(Handler handler, Context context) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").initAwareWakeUpManager(handler, context);
    }

    public static DefaultContinuePowerDevMng getContinuePowerDevMng() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getContinuePowerDevMng();
    }

    public static List<String> getFrequentIm(int count) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getFrequentIm(count);
    }

    public static void writeBigdataFile(String featureData, String newFileName, String filePath) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").writeBigdataFile(featureData, newFileName, filePath);
    }

    public static boolean isAwareAlarmManagerEnabled() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").isAwareAlarmManagerEnabled();
    }

    public static NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getNetLocationStrategy(pkgName, uid, type);
    }

    public static void registerDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").registerDevModeMethod(deviceId, callback, args);
    }

    public static void unregisterDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").unregisterDevModeMethod(deviceId, callback, args);
    }

    public static boolean getConcurrentSwitch() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getConcurrentSwitch();
    }

    public static boolean isStartWindowEnable() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").isStartWindowEnable();
    }

    public static boolean isNotificatinSwitchEnable() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").isNotificatinSwitchEnable();
    }

    public static long getNotificationInterval() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getNotificationInterval();
    }

    public static long getMemAvailable() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getMemAvailable();
    }

    public static void reclaimProcessAll(int pid, boolean suspend) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").reclaimProcessAll(pid, suspend);
    }

    public static void setRtgThreadForAnimation(boolean flag) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").setRtgThreadForAnimation(flag);
    }

    public static void applyRtgPolicy(int tid, int enable) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").applyRtgPolicy(tid, enable);
    }

    public static boolean isPreloadEnable() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").isPreloadEnable();
    }

    public static boolean isFeatureEnabled(int feature) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").isFeatureEnabled(feature);
    }

    public static boolean getIawareResourceFeature(int type) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getIawareResourceFeature(type);
    }

    public static void updateStatisticsData(int subTypeCode) {
        HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").updateStatisticsData(subTypeCode);
    }

    public static List<MemRepairPkgInfo> getMemRepairPolicy(int sceneType) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getMemRepairPolicy(sceneType);
    }

    public static DefaultRdaService getRdaService(Context context, HandlerThread handlerThread) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getRdaService(context, handlerThread);
    }

    public static DefaultHwConfigReader getHwConfigReader() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getHwConfigReader();
    }

    public static DefaultMemInfoReader getMemInfoReader() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getMemInfoReader();
    }

    public static AdvancedKillerAwareUtils getAdvancedKillerAwareUtils(Context context) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getAdvancedKillerAwareUtils(context);
    }

    public static List<String> getCtsPkgs() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getCtsPkgs();
    }

    public static String dumpHwStartWindowCache() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").dumpHwStartWindowCache();
    }

    public static int getAppSwapIndex(String pkgName) {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getAppSwapIndex(pkgName);
    }

    public static Map<String, Integer> getAllAppSwapIndex() {
        return HwPartIawareServiceFactoryEx.loadFactory("com.huawei.server.HwPartIawareServiceFactoryExImpl").getAllAppSwapIndex();
    }
}
