package ohos.app;

import java.util.List;
import ohos.aafwk.ability.AbilityMissionInfo;
import ohos.aafwk.ability.AbilityStackInfo;
import ohos.aafwk.ability.DeviceConfigInfo;
import ohos.aafwk.ability.IApplicationMission;
import ohos.aafwk.ability.MemoryInfo;
import ohos.aafwk.ability.MissionInformation;
import ohos.aafwk.ability.ProcessErrorInfo;
import ohos.aafwk.ability.RunningProcessInfo;
import ohos.aafwk.ability.SystemMemoryInfo;
import ohos.aafwk.content.Intent;
import ohos.annotation.SystemApi;
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
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getProcessesErrorInfo();
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public List<RunningProcessInfo> getAllRunningProcesses() {
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
    @SystemApi
    public int createApplicationMission(Context context, Intent intent, MissionInformation missionInformation, PixelMap pixelMap) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.createApplicationMission(context, intent, missionInformation, pixelMap);
        }
        return -1;
    }

    @Override // ohos.app.IAbilityManager
    @SystemApi
    public List<IApplicationMission> getApplicationMissions() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getApplicationMissions();
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    @SystemApi
    public void moveMissionToTop(int i, int i2) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.moveMissionToTop(i, i2);
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
    public boolean canAbilityStartOnDisplay(Context context, int i, Intent intent) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.canAbilityStartOnDisplay(context, i, intent);
        }
        return false;
    }

    @Override // ohos.app.IAbilityManager
    public void clearHeapLimitMonitor() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.clearHeapLimitMonitor();
        }
    }

    @Override // ohos.app.IAbilityManager
    public int getMissionLockModeState() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getMissionLockModeState();
        }
        return -1;
    }

    @Override // ohos.app.IAbilityManager
    public void updateDeviceLocale(LocaleProfile localeProfile) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.updateDeviceLocale(localeProfile);
        }
    }

    @Override // ohos.app.IAbilityManager
    @SystemApi
    public ElementName getTopAbility() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getTopAbility();
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public List<AbilityMissionInfo> queryRunningAbilityMissionInfo(int i) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.queryRunningAbilityMissionInfo(i);
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public List<AbilityMissionInfo> queryRecentAbilityMissionInfo(int i, int i2) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.queryRecentAbilityMissionInfo(i, i2);
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public void resizeStack(int i, int i2, int i3, int i4, int i5) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.resizeStack(i, i2, i3, i4, i5);
        }
    }

    @Override // ohos.app.IAbilityManager
    public void moveMissionToStack(int i, int i2, boolean z) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.moveMissionToStack(i, i2, z);
        }
    }

    @Override // ohos.app.IAbilityManager
    public List<AbilityStackInfo> getAllStackInfo() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getAllStackInfo();
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public List<AbilityMissionInfo> getVisibleMissions() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getVisibleMissions();
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public List<String> getVisiblePackages() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.getVisiblePackages();
        }
        return null;
    }

    @Override // ohos.app.IAbilityManager
    public void removeMissions(int[] iArr) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.removeMissions(iArr);
        }
    }

    @Override // ohos.app.IAbilityManager
    public boolean isBackgroundRunningRestricted() {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            return iAbilityManager.isBackgroundRunningRestricted();
        }
        return false;
    }

    @Override // ohos.app.IAbilityManager
    public void setHeapLimitMonitor(long j) {
        IAbilityManager iAbilityManager = this.abilityManagerImpl;
        if (iAbilityManager != null) {
            iAbilityManager.setHeapLimitMonitor(j);
        }
    }
}
