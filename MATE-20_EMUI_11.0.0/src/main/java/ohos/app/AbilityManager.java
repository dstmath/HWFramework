package ohos.app;

import java.util.List;
import ohos.aafwk.ability.DeviceConfigInfo;
import ohos.aafwk.ability.IApplicationTask;
import ohos.aafwk.ability.MemoryInfo;
import ohos.aafwk.ability.ProcessErrorInfo;
import ohos.aafwk.ability.RunningProcessInfo;
import ohos.aafwk.ability.SystemMemoryInfo;
import ohos.aafwk.ability.TaskInformation;
import ohos.aafwk.content.Intent;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.ElementName;
import ohos.global.configuration.LocaleProfile;
import ohos.media.image.PixelMap;

public class AbilityManager implements IAbilityManager {
    private static final int ERROR_ARGUMENT = -1;
    private IAbilityManager abilityManagerImpl;

    public AbilityManager(IAbilityManager iAbilityManager) {
        this.abilityManagerImpl = iAbilityManager;
    }

    @Override // ohos.app.IAbilityManager
    public MemoryInfo[] getProcessMemoryInfo(int[] iArr) {
        AppLog.d("AbilityManage getProcessMemoryInfo call", new Object[0]);
        if (iArr == null || iArr.length == 0) {
            AppLog.i("AbilityManage parameter pids is null", new Object[0]);
            return null;
        }
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getProcessMemoryInfo(iArr);
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public List<ProcessErrorInfo> getProcessesErrorInfo() {
        AppLog.d("AbilityManage getProcessesErrorInfo call", new Object[0]);
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getProcessesErrorInfo();
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public List<RunningProcessInfo> getAllRunningProcesses() {
        AppLog.d("AbilityManage getAllRunningProcesses call", new Object[0]);
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getAllRunningProcesses();
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public int getAppLargeMemory() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getAppLargeMemory();
        }
        return 16;
    }

    @Override // ohos.app.IAbilityManager
    public int getAppMemory() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getAppMemory();
        }
        return 16;
    }

    @Override // ohos.app.IAbilityManager
    public void getSystemMemoryInfo(SystemMemoryInfo systemMemoryInfo) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.getSystemMemoryInfo(systemMemoryInfo);
        }
    }

    @Override // ohos.app.IAbilityManager
    public void getMyProcessMemoryInfo(RunningProcessInfo runningProcessInfo) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.getMyProcessMemoryInfo(runningProcessInfo);
        }
    }

    @Override // ohos.app.IAbilityManager
    public boolean isLowRamDevice() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.isLowRamDevice();
        }
        return false;
    }

    @Override // ohos.app.IAbilityManager
    public boolean clearUpApplicationData() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.clearUpApplicationData();
        }
        return false;
    }

    @Override // ohos.app.IAbilityManager
    public void killProcessesByBundleName(String str) {
        if (str == null) {
            AppLog.i("AbilityManage parameter packageName is null", new Object[0]);
            return;
        }
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.killProcessesByBundleName(str);
        }
    }

    @Override // ohos.app.IAbilityManager
    public DeviceConfigInfo getDeviceConfigInfo() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getDeviceConfigInfo();
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public int getHomeScreenIconDensity() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getHomeScreenIconDensity();
        }
        return 160;
    }

    @Override // ohos.app.IAbilityManager
    public int getHomeScreenIconSize() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getHomeScreenIconSize();
        }
        return 48;
    }

    @Override // ohos.app.IAbilityManager
    public int createApplicationTask(Context context, Intent intent, TaskInformation taskInformation, PixelMap pixelMap) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.createApplicationTask(context, intent, taskInformation, pixelMap);
        }
        return -1;
    }

    @Override // ohos.app.IAbilityManager
    public List<IApplicationTask> getApplicationTasks() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getApplicationTasks();
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public void moveTaskToTop(int i, int i2) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.moveTaskToTop(i, i2);
        }
    }

    @Override // ohos.app.IAbilityManager
    public boolean isUserKingKong() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.isUserKingKong();
        }
        return false;
    }

    @Override // ohos.app.IAbilityManager
    public void updateDeviceLocale(LocaleProfile localeProfile) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.updateDeviceLocale(localeProfile);
        }
    }

    @Override // ohos.app.IAbilityManager
    public ElementName getTopAbility() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getTopAbility();
        }
        return null;
    }
}
