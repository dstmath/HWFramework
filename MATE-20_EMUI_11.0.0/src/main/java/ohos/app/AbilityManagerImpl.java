package ohos.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.os.Debug;
import android.os.LocaleList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import ohos.aafwk.ability.DeviceConfigInfo;
import ohos.aafwk.ability.IApplicationTask;
import ohos.aafwk.ability.MemoryInfo;
import ohos.aafwk.ability.ProcessErrorInfo;
import ohos.aafwk.ability.RunningProcessInfo;
import ohos.aafwk.ability.SystemMemoryInfo;
import ohos.aafwk.ability.TaskInformation;
import ohos.aafwk.content.Intent;
import ohos.abilityshell.utils.AbilityShellConverterUtils;
import ohos.abilityshell.utils.IntentConverter;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.BundleInfo;
import ohos.bundle.BundleManager;
import ohos.bundle.ElementName;
import ohos.bundle.ShellInfo;
import ohos.global.configuration.LocaleProfile;
import ohos.media.image.PixelMap;
import ohos.media.image.inner.ImageDoubleFwConverter;
import ohos.rpc.IPCSkeleton;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class AbilityManagerImpl implements IAbilityManager {
    private static final int BUNDLE_FLAG = 0;
    private static final int ERROR_ARGUMENT = -1;
    private static final int GET_HARMONYOS_BUNDLEINFO = 1;
    private static final String PAGE_SHELL_SUFFIX = "ShellActivity";
    private static final int PRIVILEGED_PERMISSION_UID = 1000;
    private static final int PRIVILEGED_PERMISSION_UID_MAX = 100000;
    private ActivityManager activityManager;
    private BundleManager bundleManager;
    private Context context;

    private int convertToImportanceLevel(int i) {
        if (i == 125) {
            return 125;
        }
        if (i == 200) {
            return 200;
        }
        if (i == 230) {
            return 230;
        }
        if (i == 300) {
            return 300;
        }
        if (i == 325) {
            return 325;
        }
        if (i == 350) {
            return 350;
        }
        if (i != 400) {
            return i != 1000 ? 100 : 1000;
        }
        return 400;
    }

    private int convertToImportanceReason(int i) {
        if (i == 1) {
            return 1;
        }
        return i == 2 ? 2 : 0;
    }

    public AbilityManagerImpl(Context context2) {
        this.context = context2;
    }

    @Override // ohos.app.IAbilityManager
    public MemoryInfo[] getProcessMemoryInfo(int[] iArr) {
        if (iArr == null) {
            return new MemoryInfo[0];
        }
        Debug.MemoryInfo[] memoryInfoArr = null;
        if (getActivityManager(this.context)) {
            AppLog.i("AbilityManagerImpl:getProcessMemoryInfo get ActivityManager info", new Object[0]);
            memoryInfoArr = this.activityManager.getProcessMemoryInfo(iArr);
        }
        MemoryInfo[] memoryInfoArr2 = new MemoryInfo[0];
        return (memoryInfoArr == null || memoryInfoArr.length <= 0) ? memoryInfoArr2 : convertToMemoryInfo(memoryInfoArr);
    }

    @Override // ohos.app.IAbilityManager
    public List<RunningProcessInfo> getAllRunningProcesses() {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = getActivityManager(this.context) ? this.activityManager.getRunningAppProcesses() : null;
        ArrayList arrayList = new ArrayList();
        if (runningAppProcesses == null || runningAppProcesses.size() <= 0) {
            return arrayList;
        }
        if (IPCSkeleton.getCallingUid() % PRIVILEGED_PERMISSION_UID_MAX == 1000) {
            return checkPkgList(runningAppProcesses);
        }
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
            RunningProcessInfo runningProcessInfo = new RunningProcessInfo();
            convertToRunningAppProcessInfo(runningAppProcessInfo, runningProcessInfo);
            arrayList.add(runningProcessInfo);
        }
        return arrayList;
    }

    @Override // ohos.app.IAbilityManager
    public List<ProcessErrorInfo> getProcessesErrorInfo() {
        ArrayList arrayList = null;
        List<ActivityManager.ProcessErrorStateInfo> processesInErrorState = getActivityManager(this.context) ? this.activityManager.getProcessesInErrorState() : null;
        if (processesInErrorState != null) {
            arrayList = new ArrayList(processesInErrorState.size());
            for (ActivityManager.ProcessErrorStateInfo processErrorStateInfo : processesInErrorState) {
                arrayList.add(covertToProcessErrorInfo(processErrorStateInfo));
            }
        }
        return arrayList;
    }

    @Override // ohos.app.IAbilityManager
    public int getAppLargeMemory() {
        if (getActivityManager(this.context)) {
            return this.activityManager.getLargeMemoryClass();
        }
        return 16;
    }

    @Override // ohos.app.IAbilityManager
    public int getAppMemory() {
        if (getActivityManager(this.context)) {
            return this.activityManager.getMemoryClass();
        }
        return 16;
    }

    @Override // ohos.app.IAbilityManager
    public void getSystemMemoryInfo(SystemMemoryInfo systemMemoryInfo) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        if (getActivityManager(this.context)) {
            this.activityManager.getMemoryInfo(memoryInfo);
        }
        convertToSystemMemoryInfo(memoryInfo, systemMemoryInfo);
    }

    @Override // ohos.app.IAbilityManager
    public void getMyProcessMemoryInfo(RunningProcessInfo runningProcessInfo) {
        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = new ActivityManager.RunningAppProcessInfo();
        if (getActivityManager(this.context)) {
            ActivityManager.getMyMemoryState(runningAppProcessInfo);
            convertToRunningAppProcessInfo(runningAppProcessInfo, runningProcessInfo);
        }
    }

    @Override // ohos.app.IAbilityManager
    public boolean isLowRamDevice() {
        if (getActivityManager(this.context)) {
            return this.activityManager.isLowRamDevice();
        }
        return false;
    }

    @Override // ohos.app.IAbilityManager
    public boolean clearUpApplicationData() {
        if (getActivityManager(this.context)) {
            return this.activityManager.clearApplicationUserData();
        }
        return false;
    }

    @Override // ohos.app.IAbilityManager
    public void killProcessesByBundleName(String str) {
        if (str == null) {
            AppLog.i("AbilityManagerImpl:parameter bundleName is null", new Object[0]);
        } else if (getActivityManager(this.context)) {
            this.activityManager.killBackgroundProcesses(str);
        }
    }

    @Override // ohos.app.IAbilityManager
    public DeviceConfigInfo getDeviceConfigInfo() {
        ConfigurationInfo deviceConfigurationInfo = getActivityManager(this.context) ? this.activityManager.getDeviceConfigurationInfo() : null;
        if (deviceConfigurationInfo != null) {
            return convertToDeviceConfigInfo(deviceConfigurationInfo);
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public int getHomeScreenIconDensity() {
        if (getActivityManager(this.context)) {
            return this.activityManager.getLauncherLargeIconDensity();
        }
        return 160;
    }

    @Override // ohos.app.IAbilityManager
    public int getHomeScreenIconSize() {
        if (getActivityManager(this.context)) {
            return this.activityManager.getLauncherLargeIconSize();
        }
        return 48;
    }

    @Override // ohos.app.IAbilityManager
    public int createApplicationTask(Context context2, Intent intent, TaskInformation taskInformation, PixelMap pixelMap) {
        if (context2 == null || intent == null || pixelMap == null) {
            AppLog.i("AbilityManagerImpl:addAppTask parameter is illegal", new Object[0]);
            return -1;
        } else if (!(context2.getHostContext() instanceof Activity)) {
            AppLog.i("AbilityManagerImpl:addAppTask ability is false type", new Object[0]);
            return -1;
        } else {
            Activity activity = (Activity) context2.getHostContext();
            ShellInfo shellInfo = new ShellInfo();
            shellInfo.setPackageName(intent.getElement().getBundleName());
            shellInfo.setType(ShellInfo.ShellType.ACTIVITY);
            shellInfo.setName(intent.getElement().getAbilityName() + "ShellActivity");
            Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, shellInfo);
            if (!createAndroidIntent.isPresent()) {
                return -1;
            }
            ActivityManager.TaskDescription taskDescription = null;
            if (!(taskInformation == null || taskInformation.getIcon() == null)) {
                taskDescription = new ActivityManager.TaskDescription(taskInformation.getLabel(), ImageDoubleFwConverter.createShadowBitmap(taskInformation.getIcon()), taskInformation.getColorPrimary());
            }
            Bitmap createShadowBitmap = ImageDoubleFwConverter.createShadowBitmap(pixelMap);
            if (!(!getActivityManager(context2) || createShadowBitmap == null || activity == null)) {
                try {
                    return this.activityManager.addAppTask(activity, createAndroidIntent.get(), taskDescription, createShadowBitmap);
                } catch (NullPointerException unused) {
                }
            }
            return -1;
        }
    }

    @Override // ohos.app.IAbilityManager
    public List<IApplicationTask> getApplicationTasks() {
        if (!getActivityManager(this.context)) {
            return null;
        }
        List<ActivityManager.AppTask> appTasks = this.activityManager.getAppTasks();
        ArrayList arrayList = new ArrayList();
        int size = appTasks.size();
        for (int i = 0; i < size; i++) {
            arrayList.add(new AppTaskImpl(appTasks.get(i)));
        }
        return arrayList;
    }

    @Override // ohos.app.IAbilityManager
    public void moveTaskToTop(int i, int i2) {
        if (i < 0 || !(i2 == 1 || i2 == 0)) {
            AppLog.i("AbilityManagerImpl:addAppTask parameter is illegal", new Object[0]);
        } else if (getActivityManager(this.context)) {
            this.activityManager.moveTaskToFront(i, i2);
        }
    }

    @Override // ohos.app.IAbilityManager
    public boolean isUserKingKong() {
        return ActivityManager.isUserAMonkey();
    }

    @Override // ohos.app.IAbilityManager
    public void updateDeviceLocale(LocaleProfile localeProfile) {
        if (localeProfile == null) {
            AppLog.i("AbilityManagerImpl:updateDeviceLocale param is illegal.", new Object[0]);
            return;
        }
        Locale[] locales = localeProfile.getLocales();
        if (locales == null) {
            AppLog.i("AbilityManagerImpl:updateDeviceLocale param is illegal.", new Object[0]);
            return;
        }
        LocaleList localeList = new LocaleList(locales);
        if (getActivityManager(this.context)) {
            this.activityManager.setDeviceLocales(localeList);
        }
    }

    @Override // ohos.app.IAbilityManager
    public ElementName getTopAbility() {
        String str;
        String str2;
        BundleInfo bundleInfo = null;
        if (!getActivityManager(this.context)) {
            return null;
        }
        List<ActivityManager.RunningTaskInfo> runningTasks = this.activityManager.getRunningTasks(1);
        if (runningTasks == null || runningTasks.isEmpty()) {
            AppLog.w("get app running task fail, or running task list is null", new Object[0]);
            return null;
        }
        ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
        if (runningTaskInfo == null) {
            AppLog.w("top task is null", new Object[0]);
            return null;
        } else if (runningTaskInfo.topActivity == null) {
            AppLog.w("top activity which in top task is null", new Object[0]);
            return null;
        } else {
            if (this.bundleManager == null) {
                this.bundleManager = getBundleManager();
                if (this.bundleManager == null) {
                    AppLog.w("get bundleManager fail", new Object[0]);
                    return null;
                }
            }
            try {
                bundleInfo = this.bundleManager.getBundleInfo(runningTaskInfo.topActivity.getPackageName(), 0);
            } catch (RemoteException unused) {
            }
            String convertToHarmonyClassName = AbilityShellConverterUtils.convertToHarmonyClassName(runningTaskInfo.topActivity.getShortClassName());
            if (bundleInfo == null || convertToHarmonyClassName == null) {
                str = runningTaskInfo.topActivity.getPackageName();
                str2 = runningTaskInfo.topActivity.getClassName();
            } else {
                String name = bundleInfo.getName();
                str2 = name + convertToHarmonyClassName;
                str = name;
            }
            return new ElementName("", str, str2);
        }
    }

    private boolean getActivityManager(Context context2) {
        if (this.activityManager != null) {
            return true;
        }
        Context context3 = null;
        if (context2.getHostContext() instanceof Context) {
            context3 = (Context) context2.getHostContext();
        }
        if (context3 == null) {
            return false;
        }
        if (context3.getSystemService("activity") instanceof ActivityManager) {
            this.activityManager = (ActivityManager) context3.getSystemService("activity");
        }
        if (this.activityManager != null) {
            return true;
        }
        return false;
    }

    private BundleManager getBundleManager() {
        IRemoteObject sysAbility = SysAbilityManager.getSysAbility(401);
        if (sysAbility != null) {
            return new BundleManager(sysAbility);
        }
        AppLog.e("AbilityManagerImpl::BundleManager getService failed", new Object[0]);
        return null;
    }

    private MemoryInfo[] convertToMemoryInfo(Debug.MemoryInfo[] memoryInfoArr) {
        MemoryInfo[] memoryInfoArr2 = new MemoryInfo[memoryInfoArr.length];
        for (int i = 0; i < memoryInfoArr.length; i++) {
            MemoryInfo memoryInfo = new MemoryInfo();
            memoryInfo.setArkPrivateDirty(memoryInfoArr[i].dalvikPrivateDirty);
            memoryInfo.setArkPss(memoryInfoArr[i].dalvikPss);
            memoryInfo.setArkSharedDirty(memoryInfoArr[i].dalvikSharedDirty);
            memoryInfo.setNativePrivateDirty(memoryInfoArr[i].nativePrivateDirty);
            memoryInfo.setNativePss(memoryInfoArr[i].nativePss);
            memoryInfo.setNativeSharedDirty(memoryInfoArr[i].nativeSharedDirty);
            memoryInfo.setOtherPrivateDirty(memoryInfoArr[i].otherPrivateDirty);
            memoryInfo.setOtherPss(memoryInfoArr[i].otherPss);
            memoryInfo.setOtherSharedDirty(memoryInfoArr[i].otherSharedDirty);
            memoryInfoArr2[i] = memoryInfo;
        }
        return memoryInfoArr2;
    }

    private RunningProcessInfo convertToRunningAppProcessInfo(ActivityManager.RunningAppProcessInfo runningAppProcessInfo, RunningProcessInfo runningProcessInfo) {
        runningProcessInfo.setPid(runningAppProcessInfo.pid);
        runningProcessInfo.setPkgList(runningAppProcessInfo.pkgList);
        runningProcessInfo.setProcessName(runningAppProcessInfo.processName);
        runningProcessInfo.setUid(runningAppProcessInfo.uid);
        runningProcessInfo.setLastMemoryLevel(runningAppProcessInfo.lastTrimLevel);
        runningProcessInfo.setWeight(runningAppProcessInfo.importance);
        runningProcessInfo.setWeightReasonCode(runningAppProcessInfo.importanceReasonCode);
        return runningProcessInfo;
    }

    private List<RunningProcessInfo> checkPkgList(List<ActivityManager.RunningAppProcessInfo> list) {
        if (this.bundleManager == null) {
            this.bundleManager = getBundleManager();
        }
        ArrayList arrayList = new ArrayList();
        if (!(list == null || this.bundleManager == null)) {
            new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            try {
                for (BundleInfo bundleInfo : this.bundleManager.getBundleInfos(1)) {
                    arrayList2.add(bundleInfo.name);
                }
                for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : list) {
                    RunningProcessInfo runningProcessInfo = new RunningProcessInfo();
                    ArrayList arrayList3 = new ArrayList(Arrays.asList(runningAppProcessInfo.pkgList));
                    Iterator it = arrayList3.iterator();
                    while (it.hasNext()) {
                        if (!arrayList2.contains((String) it.next())) {
                            it.remove();
                        }
                    }
                    if (!arrayList3.isEmpty()) {
                        arrayList3.toArray(runningAppProcessInfo.pkgList);
                        convertToRunningAppProcessInfo(runningAppProcessInfo, runningProcessInfo);
                        arrayList.add(runningProcessInfo);
                    }
                }
            } catch (RemoteException unused) {
            }
        }
        return arrayList;
    }

    private void convertToSystemMemoryInfo(ActivityManager.MemoryInfo memoryInfo, SystemMemoryInfo systemMemoryInfo) {
        systemMemoryInfo.setAvailSysMem(memoryInfo.availMem);
        systemMemoryInfo.setTotalSysMem(memoryInfo.totalMem);
        systemMemoryInfo.setThreshold(memoryInfo.threshold);
        systemMemoryInfo.setLowSysMemory(memoryInfo.lowMemory);
    }

    private DeviceConfigInfo convertToDeviceConfigInfo(ConfigurationInfo configurationInfo) {
        DeviceConfigInfo deviceConfigInfo = new DeviceConfigInfo();
        deviceConfigInfo.setTouchScreenType(configurationInfo.reqTouchScreen);
        deviceConfigInfo.setKeyBoardType(configurationInfo.reqKeyboardType);
        deviceConfigInfo.setNavigationType(configurationInfo.reqNavigation);
        deviceConfigInfo.setExternalInputDevices(configurationInfo.reqInputFeatures);
        deviceConfigInfo.setDeviceGLESVersion(configurationInfo.reqGlEsVersion);
        return deviceConfigInfo;
    }

    private ProcessErrorInfo covertToProcessErrorInfo(ActivityManager.ProcessErrorStateInfo processErrorStateInfo) {
        int lastIndexOf;
        ProcessErrorInfo processErrorInfo = new ProcessErrorInfo();
        processErrorInfo.setCondition(processErrorStateInfo.condition);
        processErrorInfo.setProcessName(processErrorStateInfo.processName);
        processErrorInfo.setPid(processErrorStateInfo.pid);
        processErrorInfo.setUid(processErrorStateInfo.uid);
        if (processErrorStateInfo.tag != null) {
            String str = processErrorStateInfo.tag;
            if (str.endsWith("ShellActivity") && (lastIndexOf = str.lastIndexOf("ShellActivity")) != -1) {
                str = str.substring(0, lastIndexOf);
            }
            processErrorInfo.setTag(str);
        }
        processErrorInfo.setShortMsg(processErrorStateInfo.shortMsg);
        processErrorInfo.setLongMsg(processErrorStateInfo.longMsg);
        processErrorInfo.setStackTrace(processErrorStateInfo.stackTrace);
        return processErrorInfo;
    }
}
