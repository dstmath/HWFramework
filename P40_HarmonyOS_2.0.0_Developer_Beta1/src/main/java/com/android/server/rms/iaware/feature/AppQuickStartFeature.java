package com.android.server.rms.iaware.feature;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwareCMSManager;
import android.rms.iaware.IAwaredConnection;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.rms.algorithm.ActivityTopManagerRt;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.ContinuePowerDevMng;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.resource.StartResParallelManager;
import com.huawei.android.content.ComponentNameEx;
import com.huawei.android.content.pm.IPackageManagerExt;
import com.huawei.android.internal.util.MemInfoReaderExt;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.storage.StorageManagerExt;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppQuickStartFeature extends RFeature {
    private static final String AQS_CONTINUE_POWER_DEV = "ContinuePowerDev";
    private static final int AQS_CONTINUE_POWER_ERROR_PARA = -1;
    private static final String AQS_CONTINUE_POWER_TYPE = "type";
    private static final String AQS_EXCTPRD_APPNAME = "AppName";
    private static final String AQS_EXCTPRD_COLLECT_TIMEOUT = "CollectTimeout";
    private static final String AQS_EXCTPRD_EXCLUDED_APP = "ExcludedApp";
    private static final String AQS_EXCTPRD_HOT_BOOT_SWITCH = "HotBootSwitch";
    private static final String AQS_EXCTPRD_LAUNCH_TIMEOUT = "LaunchTimeout";
    private static final String AQS_EXCTPRD_NAME = "ExactPreread";
    private static final String AQS_EXCTPRD_PAUSE_DELAY = "PauseDelay";
    private static final String AQS_EXCTPRD_PAUSE_TIMEOUT = "PauseTimeout";
    private static final String AQS_EXCTPRD_RAMSIZE = "ramsize";
    private static final String AQS_EXCTPRD_ROMSIZE = "romsize";
    private static final String AQS_EXCTPRD_SWITCH = "Switch";
    private static final String AQS_EXCTPRD_TOPN = "topN";
    private static final String AQS_EXCTPRD_TYPE = "type";
    private static final String AQS_FEATURE_NAME = "AppQuickStart";
    private static final String AQS_KEY_BOOST = "KeyBoost";
    private static final String AQS_LAUNCHER_CHECK_PID = "LauncherCheckPid";
    private static final String AQS_PERMANENT_PRELOAD_PKG = "PermanentPkg";
    private static final String AQS_PRELOAD_OPT_SWITCH = "preloadOptSwitch";
    private static final String AQS_PRELOAD_OPT_SYSPROP = "persist.sys.iaware.preloadoptenable";
    private static final int AQS_TOUCH_DOWN_DEFAULT_KILLTIME = 3000;
    private static final int AQS_TOUCH_DOWN_DEFAULT_TOPN = 7;
    private static final String AQS_TOUCH_DOWN_EXCLUDE_PKG = "PreloadExcludePkg";
    private static final String AQS_TOUCH_DOWN_KILLTIME = "Killtime";
    private static final String AQS_TOUCH_DOWN_LAUNCHER = "LauncherPkg";
    private static final String AQS_TOUCH_DOWN_PRELOAD = "PreloadApplication";
    private static final String AQS_TOUCH_DOWN_SWITCH = "Switch";
    private static final String AQS_TOUCH_DOWN_SYSPROP = "persist.sys.iaware.touchdownpreloadenable";
    private static final String AQS_TOUCH_DOWN_TOPN = "TopN";
    private static final int BYTE_BUFFER_LIMIT = 256;
    private static final int CHILL_MSG_ACTIVITY_NAME_INDEX = 0;
    private static final int CHILL_MSG_CHILLED = -1;
    private static final int CHILL_MSG_LEVEL_INDEX = 1;
    private static final int CHILL_MSG_MAX_ITEMS = 3;
    private static final int CLASS_NAME_INDEX = 1;
    private static final int COMPONENT_LEN = 2;
    private static final String DEFAULT_ACTIVITY_PREFIX = "..";
    private static final int DEFAULT_ROM_SIZE_GB = 16;
    private static final int DEFAULT_TOPN = 100;
    private static final ArraySet<String> EXCTPRD_EXC_APPS = new ArraySet<>();
    private static final int FEATURE_MIN_VERSION = 5;
    private static final int KB_SIZE = 1024;
    private static final String LAUNCHER_MODE_ACTVITY = "activity";
    private static final int PACKAGE_NAME_INDEX = 0;
    private static final AtomicBoolean STATE = new AtomicBoolean(false);
    private static final int SYSTEM_BOOT_CHECK_TIMEOUT = 5000;
    private static final String TAG = "AppQuickStart";
    private static final Set<String> TOUCH_DOWN_EXCLUDE_PKGS = new ArraySet();
    private static final int TWICE = 2;
    private static boolean sExactPrereadFeature = false;
    private static boolean sIsExactHotBootPrereadFeature = false;
    private static boolean sKeyBoostEnable = false;
    private static boolean sLauncherCheckPid = false;
    private static boolean sPreloadOptEnable = false;
    private static int sTopNum = 7;
    private static int sTopNumActs = 100;
    private static int sTouchDownKillTime = AQS_TOUCH_DOWN_DEFAULT_KILLTIME;
    private static boolean sTouchDownPreloadEnable = false;
    private static String sTouchLauncherPkg = null;
    private ExactPrereadWorkHandler mExactPrereadWorkHandler;
    private int mLastColdBootAppId = -1;
    private String mLastColdBootPkgName = null;
    private long mLastDisplayBeginTime = -1;
    private long mLastInputTime = 0;
    private SystemBootCheckRunnable mSystemBootCheckRunnable;

    public AppQuickStartFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        MemInfoReaderExt minfo = new MemInfoReaderExt();
        minfo.readMemInfo();
        Long totalMemMb = Long.valueOf(minfo.getTotalSize() / MemoryConstant.MB_SIZE);
        this.mExactPrereadWorkHandler = new ExactPrereadWorkHandler(totalMemMb);
        this.mSystemBootCheckRunnable = new SystemBootCheckRunnable(this, null);
        long totalRomGb = 16;
        if (context != null) {
            Object obj = context.getSystemService("storage");
            if (obj instanceof StorageManager) {
                totalRomGb = StorageManagerExt.getPrimaryStorageSize((StorageManager) obj) / MemoryConstant.GB_SIZE;
            }
        }
        loadExctprdConfig(false, totalMemMb.longValue(), totalRomGb);
        loadExctprdConfig(true, totalMemMb.longValue(), totalRomGb);
        loadContinuePowerConfig();
        checkSystemBoot();
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        AwareLog.i("AppQuickStart", "AppQuickStartFeature is a iaware3.0 feature, don't allow enable!");
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 5) {
            AwareLog.i("AppQuickStart", "the min version of AppQuickStartFeature is 5, but current version is " + realVersion + ", don't allow enable!");
            return false;
        }
        AwareLog.i("AppQuickStart", "AppQuickStartFeature enabled");
        subscribleEvents();
        enableAppQuickStart();
        StartResParallelManager.getInstance().enable();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        AwareLog.i("AppQuickStart", "AppQuickStartFeature disabled");
        unSubscribeEvents();
        disableAppQuickStart();
        StartResParallelManager.getInstance().disable();
        return true;
    }

    private static void enableAppQuickStart() {
        STATE.set(true);
    }

    private static void disableAppQuickStart() {
        STATE.set(false);
    }

    private boolean isFirstBootOrUpgrade() {
        try {
            return IPackageManagerExt.isFirstBoot() || IPackageManagerExt.isDeviceUpgrading();
        } catch (RemoteException e) {
            AwareLog.e("AppQuickStart", "packagemanager remote error");
            return false;
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        if (!checkParams(data)) {
            return false;
        }
        long timeStamp = data.getTimeStamp();
        int i = AnonymousClass1.$SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.getResourceType(data.getResId()).ordinal()];
        if (i == 1) {
            AttrSegments attrSegments = parseCollectData(data);
            if (attrSegments.isValid()) {
                return appDataHandle(attrSegments.getEvent().intValue(), attrSegments);
            }
            return false;
        } else if (i == 2) {
            AttrSegments attrSegments2 = parseCollectData(data);
            if (attrSegments2.isValid()) {
                return inputDataHandle(timeStamp, attrSegments2.getEvent().intValue(), attrSegments2);
            }
            return false;
        } else if (i == 3) {
            return processUserHabitMsg(data.getBundle());
        } else {
            if (i == 4) {
                exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_BACKUP, 0, 0, 0, "All");
                return true;
            } else if (i != 5) {
                AwareLog.e("AppQuickStart", "Invalid ResourceType");
                return false;
            } else {
                processTopActivityMsg(data.getBundle());
                return false;
            }
        }
    }

    /* renamed from: com.android.server.rms.iaware.feature.AppQuickStartFeature$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$rms$iaware$AwareConstant$ResourceType = new int[AwareConstant.ResourceType.values().length];

        static {
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RES_APP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RES_INPUT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_USERHABIT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_SHUTDOWN.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$rms$iaware$AwareConstant$ResourceType[AwareConstant.ResourceType.RESOURCE_TOP_ACTIVITY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private boolean checkParams(CollectData data) {
        if (!STATE.get() || data == null) {
            AwareLog.e("AppQuickStart", "DataDispatch not start");
            return false;
        } else if (sExactPrereadFeature || sIsExactHotBootPrereadFeature) {
            return true;
        } else {
            return false;
        }
    }

    private boolean processUserHabitMsg(Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        int appInstallStatus = bundle.getInt(AwareUserHabit.USER_HABIT_INSTALL_APP_UPDATE);
        if (appInstallStatus != 1 && appInstallStatus != 2) {
            return false;
        }
        String pkgName = bundle.getString(AwareUserHabit.USER_HABIT_PACKAGE_NAME);
        if (pkgName != null) {
            exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_CLEAN, 0, UserHandleEx.getAppId(bundle.getInt("uid")), 0, pkgName);
        }
        return true;
    }

    private void processTopActivityMsg(Bundle bundle) {
        if (bundle != null) {
            try {
                processRecordsOfChill(bundle.getStringArrayList("topActivityChillMsg"));
                AwareLog.d("AppQuickStart", "recive broadcast form NRT, top level decrease");
            } catch (ArrayIndexOutOfBoundsException e) {
                AwareLog.e("AppQuickStart", "ArrayIndexOutOfBoundsException in reportData");
            }
        }
    }

    private boolean appDataHandle(int event, AttrSegments attrSegments) {
        ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
        if (appInfo == null) {
            AwareLog.w("AppQuickStart", "appInfo is NULL");
            return false;
        } else if (event == 15001) {
            return handleProcessLaunchBegin(appInfo);
        } else {
            if (event == 15005) {
                return handleActivityBegin(appInfo);
            }
            if (event == 15013) {
                return handleDisplayedBegin(appInfo);
            }
            if (event == 85001) {
                return handleProcessLaunchFinish(appInfo);
            }
            if (event != 85013) {
                return false;
            }
            return handleDisplayedFinish(appInfo);
        }
    }

    private AwareConfig getConfig(String featureName, String configName, boolean isCustConfig) {
        if (featureName == null || configName == null || featureName.isEmpty() || configName.isEmpty()) {
            AwareLog.w("AppQuickStart", "featureName or configName is null");
            return null;
        }
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService == null) {
                AwareLog.w("AppQuickStart", "can not find service awareService.");
                return null;
            } else if (isCustConfig) {
                return IAwareCMSManager.getCustConfig(awareService, featureName, configName);
            } else {
                return IAwareCMSManager.getConfig(awareService, featureName, configName);
            }
        } catch (RemoteException e) {
            AwareLog.e("AppQuickStart", "getConfig RemoteException!");
            return null;
        }
    }

    private boolean checkMemSize(String memSize, long totalMemMb, boolean isRomGb) {
        if (memSize == null) {
            return false;
        }
        try {
            long ramSize = Long.parseLong(memSize.trim());
            if (!isRomGb) {
                if (totalMemMb <= ramSize && totalMemMb > ramSize - 1024) {
                    return true;
                }
            } else if (totalMemMb <= ramSize && totalMemMb > ramSize / 2) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            AwareLog.e("AppQuickStart", "parse ramSize error!");
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x002e  */
    private void applyExctprdTypeConfig(String typeName, AwareConfig.Item item) {
        char c;
        int hashCode = typeName.hashCode();
        if (hashCode != -1805606060) {
            if (hashCode == -1423802409 && typeName.equals(AQS_EXCTPRD_EXCLUDED_APP)) {
                c = 1;
                if (c == 0) {
                    applyExctprdSwitchConfig(item);
                    return;
                } else if (c == 1) {
                    applyExctprdExcludedAppsConfig(item);
                    return;
                } else {
                    return;
                }
            }
        } else if (typeName.equals("Switch")) {
            c = 0;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }

    private boolean applyExctprdMemSizeConfig(String memSize, long totalMemSize, AwareConfig.Item item, boolean isRomGb) {
        if (!checkMemSize(memSize, totalMemSize, isRomGb)) {
            AwareLog.i("AppQuickStart", "checkMemSize failure! memSize: " + memSize + " totalMemSize: " + totalMemSize);
            return false;
        } else if (isRomGb) {
            applyExctprdTopNumConfig(item);
            return true;
        } else {
            applyExctprdCollectTimeConfig(item);
            return true;
        }
    }

    private void loadDefaultExctprdMemSizeConfig(boolean isMatched, AwareConfig.Item defaultItem, boolean isRomGb, long totalMemSize) {
        String memSizeStr;
        if (!isMatched && defaultItem != null) {
            Map<String, String> configPropertries = defaultItem.getProperties();
            if (isRomGb) {
                memSizeStr = configPropertries.get(AQS_EXCTPRD_ROMSIZE);
            } else {
                memSizeStr = configPropertries.get("ramsize");
            }
            if (memSizeStr != null) {
                try {
                    if (totalMemSize > Long.parseLong(memSizeStr.trim())) {
                        if (isRomGb) {
                            applyExctprdTopNumConfig(defaultItem);
                        } else {
                            applyExctprdCollectTimeConfig(defaultItem);
                        }
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e("AppQuickStart", "parse ramsize error!");
                }
            }
        }
    }

    private void loadExctprdConfig(boolean isCustConfig, long totalMemMb, long totalRomGb) {
        AppQuickStartFeature appQuickStartFeature = this;
        AwareLog.i("AppQuickStart", "loadExctprdConfig isCustConfig = " + isCustConfig);
        AwareConfig configList = appQuickStartFeature.getConfig("AppQuickStart", AQS_EXCTPRD_NAME, isCustConfig);
        if (configList == null) {
            AwareLog.w("AppQuickStart", "loadExctprdConfig failure, configList is null!");
            return;
        }
        boolean matchRam = false;
        boolean matchRom = false;
        AwareConfig.Item lastRamItem = null;
        AwareConfig.Item lastRomItem = null;
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w("AppQuickStart", "loadExctprdConfig skip a item because it is null!");
                appQuickStartFeature = this;
            } else {
                Map<String, String> configPropertries = item.getProperties();
                String typeName = configPropertries.get("type");
                if (typeName != null) {
                    appQuickStartFeature.applyExctprdTypeConfig(typeName, item);
                } else if (configPropertries.get("ramsize") != null) {
                    matchRam = applyExctprdMemSizeConfig(configPropertries.get("ramsize"), totalMemMb, item, false);
                    lastRamItem = item;
                } else if (configPropertries.get(AQS_EXCTPRD_ROMSIZE) != null) {
                    matchRom = applyExctprdMemSizeConfig(configPropertries.get(AQS_EXCTPRD_ROMSIZE), totalRomGb, item, true);
                    lastRomItem = item;
                } else {
                    AwareLog.w("AppQuickStart", "unexpected config name");
                }
                appQuickStartFeature = this;
            }
        }
        loadDefaultExctprdMemSizeConfig(matchRam, lastRamItem, false, totalMemMb);
        loadDefaultExctprdMemSizeConfig(matchRom, lastRomItem, true, totalRomGb);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x004a A[Catch:{ NumberFormatException -> 0x00a6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0083 A[Catch:{ NumberFormatException -> 0x00a6 }] */
    private static void applyExctprdSwitchConfig(AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                try {
                    int hashCode = itemName.hashCode();
                    boolean z = false;
                    if (hashCode != -1805606060) {
                        if (hashCode == -1099774765 && itemName.equals(AQS_EXCTPRD_HOT_BOOT_SWITCH)) {
                            c = 1;
                            if (c == 0) {
                                if (Integer.parseInt(itemValue.trim()) == 1) {
                                    z = true;
                                }
                                sExactPrereadFeature = z;
                                AwareLog.i("AppQuickStart", "applyExctprdSwitchConfig Switch = " + itemValue);
                            } else if (c != 1) {
                                AwareLog.w("AppQuickStart", "applyExctprdSwitchConfig no such configuration. " + itemName);
                            } else {
                                if (Integer.parseInt(itemValue.trim()) == 1) {
                                    z = true;
                                }
                                sIsExactHotBootPrereadFeature = z;
                                AwareLog.i("AppQuickStart", "applyExctprdSwitchConfig HotBootSwitch = " + itemValue);
                            }
                        }
                    } else if (itemName.equals("Switch")) {
                        c = 0;
                        if (c == 0) {
                        }
                    }
                    if (c == 0) {
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e("AppQuickStart", "parse applyExctprdSwitchConfig error!");
                }
            }
        }
    }

    private static void applyExctprdExcludedAppsConfig(AwareConfig.Item item) {
        EXCTPRD_EXC_APPS.clear();
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                if (itemName.hashCode() == 870516780 && itemName.equals(AQS_EXCTPRD_APPNAME)) {
                    c = 0;
                }
                if (c != 0) {
                    AwareLog.w("AppQuickStart", "applyExctprdExcludedAppsConfig no such configuration." + itemName);
                } else {
                    EXCTPRD_EXC_APPS.add(itemValue.trim());
                    AwareLog.i("AppQuickStart", "applyExctprdExcludedAppsConfig AppName = " + itemValue);
                }
            }
        }
    }

    private void applyExctprdCollectTimeConfig(AwareConfig.Item item) {
        int delayPause = -1;
        int pauseTimeout = -1;
        int collectTimeout = -1;
        int launchTimeout = -1;
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (itemName != null && itemValue != null) {
                char c = 65535;
                try {
                    switch (itemName.hashCode()) {
                        case 590910123:
                            if (itemName.equals(AQS_EXCTPRD_PAUSE_TIMEOUT)) {
                                c = 1;
                                break;
                            }
                            break;
                        case 1545794062:
                            if (itemName.equals(AQS_EXCTPRD_LAUNCH_TIMEOUT)) {
                                c = 3;
                                break;
                            }
                            break;
                        case 1920911693:
                            if (itemName.equals(AQS_EXCTPRD_PAUSE_DELAY)) {
                                c = 0;
                                break;
                            }
                            break;
                        case 1974045943:
                            if (itemName.equals(AQS_EXCTPRD_COLLECT_TIMEOUT)) {
                                c = 2;
                                break;
                            }
                            break;
                    }
                    if (c == 0) {
                        delayPause = Integer.parseInt(itemValue.trim());
                    } else if (c == 1) {
                        pauseTimeout = Integer.parseInt(itemValue.trim());
                        if (delayPause > pauseTimeout) {
                            AwareLog.w("AppQuickStart", "applyExctprdCollectTimeConfig invalid parameters: PauseDelay=" + delayPause + ", " + itemName + "=" + pauseTimeout);
                            return;
                        }
                    } else if (c == 2) {
                        collectTimeout = Integer.parseInt(itemValue.trim());
                        if (pauseTimeout > collectTimeout || delayPause > collectTimeout) {
                            AwareLog.w("AppQuickStart", "applyExctprdCollectTimeConfig invalid parameters: PauseDelay=" + delayPause + ", " + AQS_EXCTPRD_PAUSE_TIMEOUT + "=" + pauseTimeout + ", " + itemName + "=" + collectTimeout);
                            return;
                        }
                    } else if (c != 3) {
                        AwareLog.w("AppQuickStart", "applyExctprdCollectTimeConfig no such configuration. " + itemName);
                    } else {
                        launchTimeout = Integer.parseInt(itemValue.trim());
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e("AppQuickStart", "parse applyExctprdCollectTimeConfig error!");
                }
            } else {
                return;
            }
        }
        this.mExactPrereadWorkHandler.setWorkCollectTime(launchTimeout, collectTimeout, pauseTimeout, delayPause);
    }

    private void applyExctprdTopNumConfig(AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (!(itemName == null || itemValue == null)) {
                    char c = 65535;
                    try {
                        if (itemName.hashCode() == 3565977 && itemName.equals(AQS_EXCTPRD_TOPN)) {
                            c = 0;
                        }
                        if (c != 0) {
                            AwareLog.w("AppQuickStart", "applyExctprdTopNumConfig no such configuration. " + itemName);
                        } else {
                            sTopNumActs = Integer.parseInt(itemValue.trim());
                            AwareLog.i("AppQuickStart", "applyExctprdTopNumConfig topN = " + itemValue);
                        }
                    } catch (NumberFormatException e) {
                        AwareLog.e("AppQuickStart", "parse applyExctprdTopNumConfig error!");
                    }
                }
            }
        }
    }

    private void processRecordsOfChill(List<String> chillList) {
        if (chillList != null) {
            for (String record : chillList) {
                if (record != null && !record.trim().isEmpty()) {
                    String[] items = record.trim().split(CpuCustBaseConfig.CPUCONFIG_INVALID_STR);
                    if (items.length == 3) {
                        try {
                            int level = Integer.parseInt(items[1].trim());
                            if (level == -1 || level > sTopNumActs) {
                                exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_CLEAN, 0, 0, 0, items[0].trim());
                            }
                        } catch (NumberFormatException e) {
                            AwareLog.e("AppQuickStart", "parse topActivityChillMsg error!");
                        }
                    }
                }
            }
        }
    }

    private String checkActivityName(String appName, String activityName, String className) {
        if (!appName.equals(className)) {
            return activityName;
        }
        String tmpClassName = DEFAULT_ACTIVITY_PREFIX + className;
        StringBuilder sb = new StringBuilder(appName.length() + tmpClassName.length());
        ComponentNameEx.appendShortString(sb, appName, tmpClassName);
        return sb.toString();
    }

    private boolean doHotActivityBegin(String appName, String activityName, String origActivityName, int pid, int appId) {
        if (!sIsExactHotBootPrereadFeature || pid <= 0) {
            return false;
        }
        this.mExactPrereadWorkHandler.stopCurrentWork();
        ActivityTopManagerRt activityTopManagerRt = ActivityTopManagerRt.obtainExistInstance();
        if (activityTopManagerRt == null || !activityTopManagerRt.isTopActivity(activityName, sTopNumActs)) {
            return false;
        }
        if (this.mExactPrereadWorkHandler.hasWorkExisted(appId, appName) || this.mExactPrereadWorkHandler.hasWorkExisted(appId, pid, origActivityName)) {
            return true;
        }
        AwareLog.d("AppQuickStart", "workingset: doHotActivityBegin, appId: " + appId + ", pid: " + pid + ", pkgName: " + appName + ", className: " + activityName);
        exactPrereadSendCmd(350, 0, appId, pid, origActivityName);
        this.mExactPrereadWorkHandler.startCollectWork(appId, pid, origActivityName);
        this.mLastColdBootAppId = appId;
        this.mLastColdBootPkgName = appName;
        this.mLastDisplayBeginTime = SystemClock.uptimeMillis();
        return true;
    }

    private void doColdActivityBegin(String appName, String activityName, int appId) {
        if (sExactPrereadFeature && !EXCTPRD_EXC_APPS.contains(appName)) {
            AwareLog.d("AppQuickStart", "workingset: doColdActivityBegin, appId: " + appId + ", pkgName: " + appName + ", className: " + activityName);
            exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_PREREAD, 1, appId, 0, appName);
            this.mLastColdBootAppId = appId;
            this.mLastColdBootPkgName = appName;
        }
    }

    private boolean handleActivityBegin(ArrayMap<String, String> appInfo) {
        String appName = appInfo.get("packageName");
        String activityName = appInfo.get("activityName");
        try {
            int uid = Integer.parseInt(appInfo.get("uid"));
            int pid = Integer.parseInt(appInfo.get(SceneRecogFeature.DATA_PID));
            int appId = UserHandleEx.getAppId(uid);
            doTouchDownPreloadActivityBegin(appName, UserHandleEx.getUserId(uid), uid);
            tryStopPreviousOwnerWorks(appId, appName);
            if (appName == null || activityName == null) {
                return false;
            }
            if (pid > 0) {
                return true;
            }
            doColdActivityBegin(appName, activityName, appId);
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e("AppQuickStart", "uid is not right");
            return false;
        }
    }

    private boolean handleProcessLaunchBegin(ArrayMap<String, String> appInfo) {
        if (!sExactPrereadFeature) {
            return false;
        }
        if (!"activity".equals(appInfo.get("launchMode"))) {
            return true;
        }
        try {
            String packageName = appInfo.get("packageName");
            int appId = UserHandleEx.getAppId(Integer.parseInt(appInfo.get("uid")));
            tryStopPreviousOwnerWorks(appId, packageName);
            if (packageName == null || EXCTPRD_EXC_APPS.contains(packageName)) {
                return true;
            }
            AwareLog.d("AppQuickStart", "workingset: handleProcessLaunchBegin, appId: " + appId + ", packageName: " + packageName);
            exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_PREREAD, 1, appId, 0, packageName);
            this.mLastColdBootAppId = appId;
            this.mLastColdBootPkgName = packageName;
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e("AppQuickStart", "handleProcessLaunchBegin get info failed!");
            return false;
        }
    }

    private boolean handleProcessLaunchFinish(ArrayMap<String, String> appInfo) {
        if (!sExactPrereadFeature) {
            return false;
        }
        if (!"activity".equals(appInfo.get("launchMode"))) {
            return true;
        }
        try {
            String packageName = appInfo.get("packageName");
            int uid = Integer.parseInt(appInfo.get("uid"));
            int pid = Integer.parseInt(appInfo.get(SceneRecogFeature.DATA_PID));
            int appId = UserHandleEx.getAppId(uid);
            tryStopPreviousOwnerWorks(appId, packageName);
            if (packageName == null || EXCTPRD_EXC_APPS.contains(packageName)) {
                return true;
            }
            AwareLog.d("AppQuickStart", "workingset: handleProcessLaunchFinish, appId: " + appId + ", pid: " + pid + ", packageName: " + packageName);
            this.mExactPrereadWorkHandler.startCollectWork(appId, pid, packageName);
            exactPrereadSendCmd(350, 1, appId, pid, packageName);
            this.mLastColdBootAppId = appId;
            this.mLastColdBootPkgName = packageName;
            this.mLastDisplayBeginTime = SystemClock.uptimeMillis();
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e("AppQuickStart", "handleProcessLaunchFinish get info failed!");
            return false;
        }
    }

    private void tryStopPreviousOwnerWorks(int appId, String appName) {
        String str;
        int i = this.mLastColdBootAppId;
        if (i != -1 && (str = this.mLastColdBootPkgName) != null) {
            if (this.mLastInputTime > this.mLastDisplayBeginTime || appId != i || !str.equals(appName)) {
                this.mExactPrereadWorkHandler.stopCurrentWork();
                this.mLastColdBootAppId = -1;
                this.mLastColdBootPkgName = null;
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0016: APUT  
      (r0v1 'out' java.lang.String[] A[D('out' java.lang.String[])])
      (0 ??[int, short, byte, char])
      (r1v1 'packageName' java.lang.String A[D('packageName' java.lang.String)])
     */
    private String[] getPackageAndClassName(String activityName) {
        ComponentName cpName;
        String[] out = new String[2];
        String packageName = null;
        String className = null;
        if (!(activityName == null || (cpName = ComponentName.unflattenFromString(activityName)) == null)) {
            packageName = cpName.getPackageName();
            className = cpName.getClassName();
        }
        out[0] = packageName;
        out[1] = className;
        return out;
    }

    private boolean handleDisplayedBegin(ArrayMap<String, String> appInfo) {
        try {
            String activityName = appInfo.get("activityName");
            String[] names = getPackageAndClassName(activityName);
            String packageName = names[0];
            String className = names[1];
            int uid = Integer.parseInt(appInfo.get("uid"));
            int pid = Integer.parseInt(appInfo.get(SceneRecogFeature.DATA_PID));
            int appId = UserHandleEx.getAppId(uid);
            if (activityName == null || packageName == null) {
                return false;
            }
            AwareLog.d("AppQuickStart", "workingset: handleDisplayedBegin, appId: " + appId + ", pid: " + pid + ", activityName: " + activityName);
            if (!sExactPrereadFeature || !this.mExactPrereadWorkHandler.hasWorkExisted(appId, packageName)) {
                return doHotActivityBegin(packageName, activityName, checkActivityName(packageName, activityName, className), pid, appId);
            }
            if (this.mExactPrereadWorkHandler.hasPauseWorkExisted()) {
                exactPrereadSendCmd(350, 1, appId, pid, packageName);
            }
            this.mExactPrereadWorkHandler.startCollectWork(appId, pid, packageName);
            this.mLastDisplayBeginTime = SystemClock.uptimeMillis();
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e("AppQuickStart", "handleDisplayedBegin get pid or time failed");
            return false;
        }
    }

    private boolean handleDisplayedFinish(ArrayMap<String, String> appInfo) {
        try {
            String activityName = appInfo.get("activityName");
            String[] names = getPackageAndClassName(activityName);
            String packageName = names[0];
            String className = names[1];
            int uid = Integer.parseInt(appInfo.get("uid"));
            int pid = Integer.parseInt(appInfo.get(SceneRecogFeature.DATA_PID));
            int appId = UserHandleEx.getAppId(uid);
            if (activityName == null || packageName == null) {
                return false;
            }
            AwareLog.d("AppQuickStart", "workingset: handleDisplayedFinish, appId: " + appId + ", pid: " + pid + ", activityName: " + activityName);
            if (sExactPrereadFeature && this.mExactPrereadWorkHandler.hasWorkExisted(appId, packageName)) {
                this.mExactPrereadWorkHandler.sendDelayPauseMsg();
            } else if (sIsExactHotBootPrereadFeature) {
                if (this.mExactPrereadWorkHandler.hasWorkExisted(appId, checkActivityName(packageName, activityName, className))) {
                    this.mExactPrereadWorkHandler.sendDelayPauseMsg();
                }
            } else {
                AwareLog.d("AppQuickStart", "workingset: handleDisplayedFinish work not exist");
            }
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e("AppQuickStart", "handleDisplayedFinish get pid or time failed");
            return false;
        }
    }

    private boolean inputDataHandle(long timeStamp, int event, AttrSegments attrSegments) {
        if (event == 10001 || event == 80001) {
            this.mLastInputTime = timeStamp;
            return true;
        }
        AwareLog.w("AppQuickStart", "Input event invalid");
        return false;
    }

    private AttrSegments parseCollectData(CollectData data) {
        String eventData = data.getData();
        AttrSegments.Builder builder = new AttrSegments.Builder();
        builder.addCollectData(eventData);
        return builder.build();
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean configUpdate() {
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkSystemBoot() {
        if (!"1".equals(SystemPropertiesEx.get("sys.boot_completed"))) {
            this.mExactPrereadWorkHandler.postDelayed(this.mSystemBootCheckRunnable, 5000);
        } else if (isFirstBootOrUpgrade()) {
            AwareLog.d("AppQuickStart", "FirstBootOrUpgrade when init");
            exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_CLEAN, 0, 0, 0, "All");
        }
    }

    private void subscribleEvents() {
        if (this.mDataRegister != null) {
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RES_DEV_STATUS, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_USERHABIT, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SHUTDOWN, this.mFeatureType);
            if (sIsExactHotBootPrereadFeature) {
                this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_TOP_ACTIVITY, this.mFeatureType);
            }
        }
    }

    private void unSubscribeEvents() {
        if (this.mDataRegister != null) {
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_DEV_STATUS, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_USERHABIT, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SHUTDOWN, this.mFeatureType);
            if (sIsExactHotBootPrereadFeature) {
                this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_TOP_ACTIVITY, this.mFeatureType);
            }
        }
    }

    public static boolean isExactPrereadFeatureEnable() {
        return sExactPrereadFeature && STATE.get();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void exactPrereadSendCmd(int action, int coldboot, int appId, int pid, String ownerName) {
        AwareLog.d("AppQuickStart", "workingset, action: " + action + ", coldboot:" + coldboot + ", appId: " + appId + ", pid: " + pid + ", " + ownerName);
        try {
            byte[] nameBytes = ownerName.getBytes("UTF-8");
            if (nameBytes.length <= 0 || nameBytes.length > 256) {
                AwareLog.w("AppQuickStart", "ComponentName is invalid!");
                return;
            }
            ByteBuffer buffer = ByteBuffer.allocate(nameBytes.length + 16);
            buffer.putInt(action);
            buffer.putInt(coldboot);
            buffer.putInt(appId);
            buffer.putInt(pid);
            buffer.put(nameBytes);
            IAwaredConnection.getInstance().sendPacket(buffer.array());
        } catch (UnsupportedEncodingException e) {
            AwareLog.w("AppQuickStart", "UnsupportedEncodingException: transform ComponentName failed!");
        }
    }

    /* access modifiers changed from: private */
    public final class ExactPrereadWorkHandler extends Handler {
        private static final int INVALID_ID = -1;
        private static final String INVALID_NAME = "";
        private static final int INVALID_TYPE = -1;
        private static final int MAX_LAUNCH_TIMEOUT = 20000;
        private static final int MAX_PAUSECOLLECT_TIMEOUT = 20000;
        private static final int MAX_PAUSEDELAY_TIMEOUT = 3000;
        private static final int MAX_PAUSE_TIMEOUT = 10000;
        private static final int MIN_LAUNCH_TIMEOUT = 5000;
        private static final int MSG_DELAY_PAUSE_TIMEOUT = 300;
        private static final int MSG_LAUNCH_TIMEOUT = 100;
        private static final int MSG_PAUSE_COLLECT_TIMEOUT = 400;
        private static final int MSG_PAUSE_TIMEOUT = 200;
        private int mDelayPauseTimeout = 0;
        private int mLaunchTimeout = 10000;
        private int mPauseCollectTimeout = 8000;
        private int mPauseTimeout = 1000;
        private ExactPrereadWork mWork = new ExactPrereadWork(-1, -1, "", -1);

        public ExactPrereadWorkHandler(Long totalMemMb) {
            if (totalMemMb.longValue() > 4096) {
                this.mDelayPauseTimeout = 200;
                this.mPauseTimeout = MAX_PAUSEDELAY_TIMEOUT;
            } else if (totalMemMb.longValue() > 2048) {
                this.mDelayPauseTimeout = 0;
                this.mPauseTimeout = 2000;
            } else if (totalMemMb.longValue() > 1024) {
                this.mDelayPauseTimeout = 0;
                this.mPauseTimeout = 1500;
            } else {
                this.mDelayPauseTimeout = 0;
                this.mPauseTimeout = 1000;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setWorkCollectTime(int launchTimeout, int collectTimeout, int pauseTimeout, int pauseDelay) {
            if (launchTimeout >= 0) {
                if (launchTimeout < MIN_LAUNCH_TIMEOUT) {
                    this.mLaunchTimeout = MIN_LAUNCH_TIMEOUT;
                } else if (launchTimeout > 20000) {
                    this.mLaunchTimeout = 20000;
                } else {
                    this.mLaunchTimeout = launchTimeout;
                }
            }
            if (collectTimeout >= 0) {
                if (collectTimeout > 20000) {
                    this.mPauseCollectTimeout = 20000;
                } else {
                    this.mPauseCollectTimeout = collectTimeout;
                }
            }
            if (pauseTimeout >= 0) {
                if (pauseTimeout > 10000) {
                    this.mPauseTimeout = 10000;
                } else {
                    this.mPauseTimeout = pauseTimeout;
                }
            }
            int i = this.mPauseTimeout;
            int i2 = this.mPauseCollectTimeout;
            if (i > i2) {
                this.mPauseTimeout = i2;
            }
            if (pauseDelay >= 0) {
                if (pauseDelay > MAX_PAUSEDELAY_TIMEOUT) {
                    this.mDelayPauseTimeout = MAX_PAUSEDELAY_TIMEOUT;
                } else {
                    this.mDelayPauseTimeout = pauseDelay;
                }
            }
            int i3 = this.mDelayPauseTimeout;
            int i4 = this.mPauseTimeout;
            if (i3 > i4) {
                this.mDelayPauseTimeout = i4;
            }
            AwareLog.i("AppQuickStart", "setWorkCollectTime, launchTimeout: " + this.mLaunchTimeout + ", collectTimeout: " + this.mPauseCollectTimeout + ", pauseTimeout: " + this.mPauseTimeout + ", pauseDelay: " + this.mDelayPauseTimeout);
        }

        /* access modifiers changed from: private */
        public class ExactPrereadWork {
            public int myAppId;
            public String myOwnerName;
            public int myPid;
            public int myType;

            public ExactPrereadWork(int appId, int pid, String ownerName, int type) {
                this.myAppId = appId;
                this.myPid = pid;
                this.myOwnerName = ownerName;
                this.myType = type;
            }

            public void workReset() {
                this.myAppId = -1;
                this.myPid = -1;
                this.myOwnerName = "";
                this.myType = -1;
            }
        }

        private boolean hasValidWorkExisted() {
            if (this.mWork.myAppId == -1 || this.mWork.myPid == -1 || this.mWork.myType == -1 || this.mWork.myOwnerName.isEmpty()) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean hasWorkExisted(int appId, int pid, String ownerName) {
            if (this.mWork.myAppId == appId && this.mWork.myPid == pid) {
                return this.mWork.myOwnerName.equals(ownerName);
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean hasWorkExisted(int appId, String ownerName) {
            return hasWorkExisted(appId, this.mWork.myPid, ownerName);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean hasPauseWorkExisted() {
            if (this.mWork.myType == 200 || this.mWork.myType == 400) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startCollectWork(int appId, int pid, String ownerName) {
            if (ownerName != null) {
                removeMessages(100);
                removeMessages(200);
                removeMessages(400);
                removeMessages(300);
                if (!hasWorkExisted(appId, ownerName)) {
                    stopCurrentWork();
                    ExactPrereadWork exactPrereadWork = this.mWork;
                    exactPrereadWork.myAppId = appId;
                    exactPrereadWork.myOwnerName = ownerName;
                } else if (this.mWork.myType == 100 && this.mWork.myPid != pid) {
                    AwareLog.d("AppQuickStart", "workingset: startCollectWork, curPid: " + pid + ", oldPid: " + this.mWork.myPid);
                    AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_PAUSE_COLLECT, 0, this.mWork.myAppId, this.mWork.myPid, this.mWork.myOwnerName);
                }
                ExactPrereadWork exactPrereadWork2 = this.mWork;
                exactPrereadWork2.myPid = pid;
                exactPrereadWork2.myType = 100;
                Message message = Message.obtain();
                message.obj = this.mWork;
                message.what = 100;
                sendMessageDelayed(message, (long) this.mLaunchTimeout);
            }
        }

        private void sendPauseMsg() {
            this.mWork.myType = 200;
            Message message = Message.obtain();
            message.what = 200;
            sendMessageDelayed(message, (long) (this.mPauseTimeout - this.mDelayPauseTimeout));
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendDelayPauseMsg() {
            removeMessages(100);
            this.mWork.myType = 300;
            Message message = Message.obtain();
            message.what = 300;
            sendMessageDelayed(message, (long) this.mDelayPauseTimeout);
        }

        private void sendPauseCollectMsg() {
            this.mWork.myType = 400;
            Message message = Message.obtain();
            message.what = 400;
            sendMessageDelayed(message, (long) (this.mPauseCollectTimeout - this.mPauseTimeout));
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopCurrentWork() {
            if (hasValidWorkExisted()) {
                AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_STOP, 0, this.mWork.myAppId, this.mWork.myPid, this.mWork.myOwnerName);
                this.mWork.workReset();
                removeMessages(100);
                removeMessages(200);
                removeMessages(400);
                removeMessages(300);
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i == 100) {
                AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_ABORT, 0, this.mWork.myAppId, this.mWork.myPid, this.mWork.myOwnerName);
                this.mWork.workReset();
            } else if (i == 200) {
                sendPauseCollectMsg();
                AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_PAUSE_COLLECT, 0, this.mWork.myAppId, this.mWork.myPid, this.mWork.myOwnerName);
            } else if (i == 300) {
                sendPauseMsg();
                AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_PAUSE, 0, this.mWork.myAppId, this.mWork.myPid, this.mWork.myOwnerName);
            } else if (i == 400) {
                AppQuickStartFeature.this.exactPrereadSendCmd(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_STOP, 0, this.mWork.myAppId, this.mWork.myPid, this.mWork.myOwnerName);
                this.mWork.workReset();
            }
        }
    }

    /* access modifiers changed from: private */
    public class SystemBootCheckRunnable implements Runnable {
        private SystemBootCheckRunnable() {
        }

        /* synthetic */ SystemBootCheckRunnable(AppQuickStartFeature x0, AnonymousClass1 x1) {
            this();
        }

        @Override // java.lang.Runnable
        public void run() {
            AppQuickStartFeature.this.checkSystemBoot();
        }
    }

    public static boolean isTouchDownPreloadEnable() {
        return sTouchDownPreloadEnable;
    }

    public static boolean isPreloadOptEnable() {
        return sPreloadOptEnable && STATE.get();
    }

    public static boolean isKeyBoostEnable() {
        return sKeyBoostEnable;
    }

    public static boolean isLauncherCheckPid() {
        return sLauncherCheckPid;
    }

    public static int getTouchDownKillTime() {
        return sTouchDownKillTime;
    }

    public static int getTopN() {
        return sTopNum;
    }

    public static String getTouchDownLauncher() {
        return sTouchLauncherPkg;
    }

    public static boolean isTouchDownExcludePkg(String pkg) {
        boolean contains;
        if (pkg == null || pkg.isEmpty()) {
            return false;
        }
        synchronized (TOUCH_DOWN_EXCLUDE_PKGS) {
            contains = TOUCH_DOWN_EXCLUDE_PKGS.contains(pkg);
        }
        return contains;
    }

    private void loadContinuePowerConfig() {
        AwareConfig configList = null;
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                configList = IAwareCMSManager.getCustConfig(awareService, "AppQuickStart", AQS_CONTINUE_POWER_DEV);
                if (configList == null) {
                    configList = IAwareCMSManager.getConfig(awareService, "AppQuickStart", AQS_CONTINUE_POWER_DEV);
                }
                parseContinuePowerConfig(configList);
                return;
            }
            AwareLog.w("AppQuickStart", "getAwareConfig can not find service awareService.");
        } catch (RemoteException e) {
            AwareLog.e("AppQuickStart", "getConfig RemoteException!");
        }
    }

    private void parseContinuePowerConfig(AwareConfig configList) {
        if (configList == null) {
            AwareLog.i("AppQuickStart", "loadContinuePowerConfig failure, configList is null!");
            return;
        }
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (item == null || item.getProperties() == null) {
                AwareLog.i("AppQuickStart", "loadContinuePowerConfig skip a item because it is null!");
            } else {
                String typeName = item.getProperties().get("type");
                if (AQS_TOUCH_DOWN_PRELOAD.equals(typeName)) {
                    applyTouchDownPreloadConfig(item);
                } else if (AQS_TOUCH_DOWN_EXCLUDE_PKG.equals(typeName)) {
                    applyTouchDownPreloadExcludeApp(item);
                } else if (AQS_PERMANENT_PRELOAD_PKG.equals(typeName)) {
                    applyPermanentPreloadPkgs(item);
                } else {
                    boolean z = false;
                    if (AQS_KEY_BOOST.equals(typeName)) {
                        if (applyCommSwitch(item) == 1) {
                            z = true;
                        }
                        sKeyBoostEnable = z;
                    } else if (AQS_LAUNCHER_CHECK_PID.equals(typeName)) {
                        if (applyCommSwitch(item) == 1) {
                            z = true;
                        }
                        sLauncherCheckPid = z;
                    } else {
                        AwareLog.i("AppQuickStart", "loadContinuePowerConfig unknown typeName:" + typeName);
                    }
                }
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static void applyTouchDownPreloadConfig(AwareConfig.Item item) {
        setTouchDownPreloadEnable(0);
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                try {
                    switch (itemName.hashCode()) {
                        case -1805606060:
                            if (itemName.equals("Switch")) {
                                c = 0;
                                break;
                            }
                            break;
                        case -539356341:
                            if (itemName.equals(AQS_TOUCH_DOWN_KILLTIME)) {
                                c = 2;
                                break;
                            }
                            break;
                        case 2612665:
                            if (itemName.equals(AQS_TOUCH_DOWN_TOPN)) {
                                c = 3;
                                break;
                            }
                            break;
                        case 1443245548:
                            if (itemName.equals(AQS_TOUCH_DOWN_LAUNCHER)) {
                                c = 4;
                                break;
                            }
                            break;
                        case 1560390590:
                            if (itemName.equals(AQS_PRELOAD_OPT_SWITCH)) {
                                c = 1;
                                break;
                            }
                            break;
                    }
                    if (c == 0) {
                        setTouchDownPreloadEnable(Integer.parseInt(itemValue.trim()));
                        AwareLog.d("AppQuickStart", "TouchDownPreloadApp applyTouchDownPreloadSwitchConfig Switch = " + itemValue);
                    } else if (c == 1) {
                        setPreloadOptEnable(Integer.parseInt(itemValue.trim()));
                        AwareLog.d("AppQuickStart", "TouchDownPreloadApp applyTouchDownPreloadSwitchConfig preloadOptSwitch = " + itemValue);
                    } else if (c == 2) {
                        setTouchDownPreloadKillTime(Integer.parseInt(itemValue.trim()));
                    } else if (c == 3) {
                        setTouchDownPreloadTopN(Integer.parseInt(itemValue.trim()));
                    } else if (c != 4) {
                        AwareLog.w("AppQuickStart", "TouchDownPreloadApp applyTouchDown no such configuration. " + itemName);
                    } else {
                        setTouchDownPreloadLauncher(itemValue.trim());
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e("AppQuickStart", "parse applyTouchDownPreloadSwitchConfig error!");
                }
            }
        }
        initContinuePowerDevMng();
    }

    private static void applyTouchDownPreloadExcludeApp(AwareConfig.Item item) {
        synchronized (TOUCH_DOWN_EXCLUDE_PKGS) {
            TOUCH_DOWN_EXCLUDE_PKGS.clear();
        }
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                if (itemName.hashCode() == 870516780 && itemName.equals(AQS_EXCTPRD_APPNAME)) {
                    c = 0;
                }
                if (c != 0) {
                    AwareLog.i("AppQuickStart", "applyExctprdExcludedAppsConfig no such configuration." + itemName);
                } else {
                    String value = itemValue.trim();
                    if (value != null && !value.isEmpty()) {
                        synchronized (TOUCH_DOWN_EXCLUDE_PKGS) {
                            TOUCH_DOWN_EXCLUDE_PKGS.add(value);
                        }
                        AwareLog.i("AppQuickStart", "applyTouchDownPreloadExcludeApp = " + value);
                    } else {
                        return;
                    }
                }
            }
        }
    }

    private static void applyPermanentPreloadPkgs(AwareConfig.Item item) {
        Set<String> permanentPkgs = new ArraySet<>();
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                if (itemName.hashCode() == 870516780 && itemName.equals(AQS_EXCTPRD_APPNAME)) {
                    c = 0;
                }
                if (c != 0) {
                    AwareLog.i("AppQuickStart", "applyPermanentPreloadPkgsConfig no such configuration." + itemName);
                } else {
                    String value = itemValue.trim();
                    if (value != null && !value.isEmpty()) {
                        permanentPkgs.add(value);
                        AwareLog.i("AppQuickStart", "applyPermanentPreloadPkg = " + value);
                    } else {
                        return;
                    }
                }
            }
        }
        ContinuePowerDevMng.getInstance().setPermanentPreloadPkgs(permanentPkgs);
    }

    private static int applyCommSwitch(AwareConfig.Item item) {
        int value = -1;
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                try {
                    if (itemName.hashCode() == -1805606060 && itemName.equals("Switch")) {
                        c = 0;
                    }
                    if (c != 0) {
                        AwareLog.w("AppQuickStart", "applyKeyBoostSwitch no such configuration. " + itemName);
                    } else {
                        value = Integer.parseInt(itemValue.trim());
                        AwareLog.d("AppQuickStart", "applyCommSwitch Switch = " + itemValue);
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e("AppQuickStart", "parse applyKeyBoostSwitch error!");
                }
            }
        }
        return value;
    }

    private static void setTouchDownPreloadEnable(int value) {
        int i = 0;
        sTouchDownPreloadEnable = value == 1;
        if (value == 1) {
            i = 1;
        }
        SystemPropertiesEx.set(AQS_TOUCH_DOWN_SYSPROP, String.valueOf(i));
    }

    private static void initContinuePowerDevMng() {
        if (sTouchDownPreloadEnable || sPreloadOptEnable) {
            ContinuePowerDevMng.getInstance().initHandler();
        }
    }

    private static void setPreloadOptEnable(int value) {
        int i = 0;
        sPreloadOptEnable = value == 1;
        if (value == 1) {
            i = 1;
        }
        SystemPropertiesEx.set(AQS_PRELOAD_OPT_SYSPROP, String.valueOf(i));
    }

    private static void setTouchDownPreloadKillTime(int value) {
        sTouchDownKillTime = value < 0 ? AQS_TOUCH_DOWN_DEFAULT_KILLTIME : value;
    }

    private static void setTouchDownPreloadTopN(int value) {
        sTopNum = value < 0 ? 7 : value;
    }

    private static void setTouchDownPreloadLauncher(String value) {
        if (value != null && !value.isEmpty()) {
            sTouchLauncherPkg = value;
        }
    }

    private void doTouchDownPreloadActivityBegin(String pkg, int userId, int uid) {
        ContinuePowerDevMng.getInstance().finishPreloadApplicationForLauncher(pkg, userId, uid);
    }
}
