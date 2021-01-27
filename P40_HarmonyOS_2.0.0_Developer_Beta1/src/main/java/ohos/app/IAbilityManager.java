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
import ohos.bundle.ElementName;
import ohos.global.configuration.LocaleProfile;
import ohos.media.image.PixelMap;

public interface IAbilityManager {
    public static final int DEFAULT_DENSITY = 160;
    public static final int DEFAULT_HEAPSIZE = 16;
    public static final int DEFAULT_ICONSIZE = 48;
    public static final int LOCK_MISSION_MODE_LOCKED = 1;
    public static final int LOCK_MISSION_MODE_NULL = 0;
    public static final int LOCK_MISSION_MODE_PINNED = 2;
    @SystemApi
    public static final int MOVE_TO_TOP_WITH_HOME = 1;

    boolean canAbilityStartOnDisplay(Context context, int i, Intent intent);

    void clearHeapLimitMonitor();

    boolean clearUpApplicationData();

    @SystemApi
    int createApplicationMission(Context context, Intent intent, MissionInformation missionInformation, PixelMap pixelMap);

    List<RunningProcessInfo> getAllRunningProcesses();

    @SystemApi
    List<AbilityStackInfo> getAllStackInfo();

    int getAppLargeMemory();

    int getAppMemory();

    @SystemApi
    List<IApplicationMission> getApplicationMissions();

    DeviceConfigInfo getDeviceConfigInfo();

    int getHomeScreenIconDensity();

    int getHomeScreenIconSize();

    int getMissionLockModeState();

    void getMyProcessMemoryInfo(RunningProcessInfo runningProcessInfo);

    MemoryInfo[] getProcessMemoryInfo(int[] iArr);

    List<ProcessErrorInfo> getProcessesErrorInfo();

    void getSystemMemoryInfo(SystemMemoryInfo systemMemoryInfo);

    ElementName getTopAbility();

    @SystemApi
    List<AbilityMissionInfo> getVisibleMissions();

    @SystemApi
    List<String> getVisiblePackages();

    boolean isBackgroundRunningRestricted();

    boolean isLowRamDevice();

    boolean isUserKingKong();

    void killProcessesByBundleName(String str);

    @SystemApi
    void moveMissionToStack(int i, int i2, boolean z);

    @SystemApi
    void moveMissionToTop(int i, int i2);

    List<AbilityMissionInfo> queryRecentAbilityMissionInfo(int i, int i2);

    List<AbilityMissionInfo> queryRunningAbilityMissionInfo(int i);

    @SystemApi
    void removeMissions(int[] iArr);

    @SystemApi
    void resizeStack(int i, int i2, int i3, int i4, int i5);

    void setHeapLimitMonitor(long j);

    void updateDeviceLocale(LocaleProfile localeProfile);
}
