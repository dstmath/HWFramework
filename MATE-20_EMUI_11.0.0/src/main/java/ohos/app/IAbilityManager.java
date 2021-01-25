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
import ohos.bundle.ElementName;
import ohos.global.configuration.LocaleProfile;
import ohos.media.image.PixelMap;

public interface IAbilityManager {
    public static final int DEFAULT_DENSITY = 160;
    public static final int DEFAULT_HEAPSIZE = 16;
    public static final int DEFAULT_ICONSIZE = 48;
    public static final int MOVE_TO_TOP_WITH_HOME = 1;

    boolean clearUpApplicationData();

    int createApplicationTask(Context context, Intent intent, TaskInformation taskInformation, PixelMap pixelMap);

    List<RunningProcessInfo> getAllRunningProcesses();

    int getAppLargeMemory();

    int getAppMemory();

    List<IApplicationTask> getApplicationTasks();

    DeviceConfigInfo getDeviceConfigInfo();

    int getHomeScreenIconDensity();

    int getHomeScreenIconSize();

    void getMyProcessMemoryInfo(RunningProcessInfo runningProcessInfo);

    MemoryInfo[] getProcessMemoryInfo(int[] iArr);

    List<ProcessErrorInfo> getProcessesErrorInfo();

    void getSystemMemoryInfo(SystemMemoryInfo systemMemoryInfo);

    ElementName getTopAbility();

    boolean isLowRamDevice();

    boolean isUserKingKong();

    void killProcessesByBundleName(String str);

    void moveTaskToTop(int i, int i2);

    void updateDeviceLocale(LocaleProfile localeProfile);
}
