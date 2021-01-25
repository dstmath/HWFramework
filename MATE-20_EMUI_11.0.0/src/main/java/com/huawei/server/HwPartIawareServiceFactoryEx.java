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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.android.server.AlarmManagerServiceExt;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.mtm.DefaultMultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.BaseAppStartupInfo;
import com.android.server.rms.collector.DefaultMemInfoReader;
import com.android.server.rms.config.DefaultHwConfigReader;
import com.android.server.rms.iaware.DefaultRdaService;
import com.android.server.rms.iaware.appmng.DefaultContinuePowerDevMng;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.annotation.HwSystemApi;
import com.huawei.server.rme.hyperhold.DefaultAdvancedKiller;
import com.huawei.server.wm.WindowProcessControllerEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@HwSystemApi
public class HwPartIawareServiceFactoryEx {
    public static final String IAWARE_SERVICE_FACTORY_EX_IMPL_NAME = "com.huawei.server.HwPartIawareServiceFactoryExImpl";
    private static final String TAG = "HwPartIawareServiceFactoryEx";
    private static HwPartIawareServiceFactoryEx sFactory;

    public static HwPartIawareServiceFactoryEx loadFactory(String factoryName) {
        HwPartIawareServiceFactoryEx hwPartIawareServiceFactoryEx = sFactory;
        if (hwPartIawareServiceFactoryEx != null) {
            return hwPartIawareServiceFactoryEx;
        }
        Object object = FactoryLoader.loadFactory(factoryName);
        if (object == null || !(object instanceof HwPartIawareServiceFactoryEx)) {
            sFactory = new HwPartIawareServiceFactoryEx();
        } else {
            sFactory = (HwPartIawareServiceFactoryEx) object;
        }
        if (sFactory != null) {
            Log.i(TAG, "add " + factoryName + " to memory.");
            return sFactory;
        }
        throw new RuntimeException("can't load any iaware service ex factory");
    }

    public DefaultMultiTaskManagerService getMultiTaskManagerService(Context context) {
        return new DefaultMultiTaskManagerService(context);
    }

    public boolean shouldPreventRestartService(ServiceInfo serviceInfo, boolean isRealStart) {
        return false;
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid, WindowProcessControllerEx callerApp) {
        return false;
    }

    public boolean shouldPreventStartService(BaseAppStartupInfo baseAppStartupInfo, ServiceInfo servInfo, int servFlag, boolean servExist) {
        return false;
    }

    public boolean shouldPreventSendReceiver(ResolveInfo resolveInfo, BaseAppStartupInfo baseAppStartupInfo) {
        return false;
    }

    public boolean isEvilWindow(int window, int code, int type) {
        return false;
    }

    public void reportCloudUpdate(Bundle bundle) {
    }

    public void setProtectedListFromMdm(Context context, List<String> list) {
    }

    public void removeProtectedListFromMdm(Context context) {
    }

    public boolean shouldPreventStartActivity(Intent intent, ActivityInfo activityInfo, int callerPid, int callerUid, WindowProcessControllerEx callerApp) {
        return false;
    }

    public void reportTopActData(Bundle bdl) {
    }

    public List<String> getForceProtectApps(int num) {
        return null;
    }

    public List<String> recognizeLongTimeRunningApps() {
        return null;
    }

    public List<String> getMostFrequentUsedApp(int num, int minCount) {
        return null;
    }

    public void reportHabitData(Bundle bdl) {
    }

    public List<String> getTopN(int num) {
        return null;
    }

    public long getProcessPssByPid(int pid) {
        return 0;
    }

    public int getPidForProcName(String[] procName) {
        return 0;
    }

    public boolean dumpIaware(Context context, PrintWriter pw, String[] args) {
        return false;
    }

    public void registerProcessObserver(IProcessObserverEx observer) {
    }

    public void dispatchPolicy(RPolicyData policy) {
    }

    public void setHwStartWindowCacheHandler(Handler handler) {
    }

    public View tryAddViewFromCache(String packageName, IBinder appToken, Configuration config) {
        return null;
    }

    public void putViewToCache(String packageName, View startView, WindowManager.LayoutParams params) {
    }

    public void reportSceneInfos(Bundle bdl) {
    }

    public boolean isVisibleWindows(int userId, String pkg) {
        return false;
    }

    public boolean isWidgetVisible(Bundle options) {
        return false;
    }

    public boolean shouldPreventStartActivity(ActivityInfo activityInfo, int callerPid, int callerUid) {
        return false;
    }

    public void processNativeEventNotify(int eventType, int eventValue, int keyAction, int pid, int uid) {
    }

    public void setFingerprintWakeup(boolean isWakeup) {
    }

    public void notifyWakeupResult(boolean isWakenupThisTime) {
    }

    public void notifyPowerkeyInteractive(boolean isInteractive) {
    }

    public void setImsForAwareFakeActivityRecg(InputManagerServiceEx inputManagerService) {
    }

    public void setImsForAwareGameModeRecg(InputManagerServiceEx inputManagerService) {
    }

    public void setImsForSysLoadManager(InputManagerServiceEx inputManagerService) {
    }

    public void registerCallback(ISceneCallback callback, int scene) {
    }

    public boolean isToastWindows(int userId, String pkg) {
        return false;
    }

    public Bundle getTypeTopN(int[] appTypes) {
        return null;
    }

    public int getAppMngSpecType(String pkg) {
        return -1;
    }

    public String getActTopImcn() {
        return "unknownpkg";
    }

    public boolean isAwareSceneRecognizeEnable() {
        return false;
    }

    public void modifyAlarmIfOverload(AlarmManagerServiceExt.AlarmEx alarm) {
    }

    public void reportWakeupAlarms(ArrayList<AlarmManagerServiceExt.AlarmEx> arrayList) {
    }

    public void reportWakeupSystem(String reason) {
    }

    public void initAwareWakeUpManager(Handler handler, Context context) {
    }

    public DefaultContinuePowerDevMng getContinuePowerDevMng() {
        return DefaultContinuePowerDevMng.getInstance();
    }

    public List<String> getFrequentIm(int count) {
        return null;
    }

    public void writeBigdataFile(String featureData, String newFileName, String filePath) {
    }

    public boolean isAwareAlarmManagerEnabled() {
        return false;
    }

    public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
        return null;
    }

    public void registerDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
    }

    public void unregisterDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle args) {
    }

    public boolean getConcurrentSwitch() {
        return false;
    }

    public boolean isStartWindowEnable() {
        return false;
    }

    public boolean isNotificatinSwitchEnable() {
        return false;
    }

    public long getNotificationInterval() {
        return 1000;
    }

    public long getMemAvailable() {
        return 0;
    }

    public void reclaimProcessAll(int pid, boolean suspend) {
    }

    public void setRtgThreadForAnimation(boolean flag) {
    }

    public void applyRtgPolicy(int tid, int enable) {
    }

    public boolean isPreloadEnable() {
        return false;
    }

    public boolean isFeatureEnabled(int feature) {
        return false;
    }

    public boolean getIawareResourceFeature(int type) {
        return false;
    }

    public void updateStatisticsData(int subTypeCode) {
    }

    public List<MemRepairPkgInfo> getMemRepairPolicy(int sceneType) {
        return null;
    }

    public DefaultRdaService getRdaService(Context context, HandlerThread handlerThread) {
        return new DefaultRdaService(context, handlerThread);
    }

    public DefaultHwConfigReader getHwConfigReader() {
        return new DefaultHwConfigReader();
    }

    public DefaultMemInfoReader getMemInfoReader() {
        return new DefaultMemInfoReader();
    }

    public DefaultAdvancedKiller getAdvancedKiller(Context context) {
        return new DefaultAdvancedKiller(context);
    }

    public List<String> getCtsPkgs() {
        return null;
    }

    public String dumpHwStartWindowCache() {
        return null;
    }
}
