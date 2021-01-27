package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IAwareCMSManager;
import android.rms.iaware.StatisticsData;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.memory.action.Action;
import com.android.server.rms.iaware.memory.action.GpuCompressAction;
import com.android.server.rms.iaware.memory.action.KillAction;
import com.android.server.rms.iaware.memory.action.QuickKillAction;
import com.android.server.rms.iaware.memory.action.ReclaimAction;
import com.android.server.rms.iaware.memory.action.SystemTrimAction;
import com.android.server.rms.iaware.memory.data.dispatch.DataDispatch;
import com.android.server.rms.iaware.memory.policy.DmeServer;
import com.android.server.rms.iaware.memory.policy.MemoryExecutorServer;
import com.android.server.rms.iaware.memory.policy.MemoryScenePolicy;
import com.android.server.rms.iaware.memory.policy.MemoryScenePolicyList;
import com.android.server.rms.iaware.memory.utils.BigDataStore;
import com.android.server.rms.iaware.memory.utils.BigMemoryConstant;
import com.android.server.rms.iaware.memory.utils.BigMemoryInfo;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageInfoCollector;
import com.huawei.android.internal.util.MemInfoReaderExt;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryFeature extends RFeature {
    private static final FeatureHandler FEATURE_HANDLER = new FeatureHandler();
    private static final int INTERVAL_SAVE_DATA_IN_MSEC = 7200000;
    private static final String TAG = "MemFeature";
    private BigDataStore mBigDataStore = BigDataStore.getInstance();
    private final AtomicBoolean mRunning = new AtomicBoolean(false);

    public MemoryFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        MemoryConstant.init(this.mContext);
        MemoryConstant.setCameraPowerUpMem();
        loadMemConfig();
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        subscribleEvents();
        DmeServer.getInstance().enable();
        DataDispatch.getInstance().start();
        MemoryUtils.writeSwappiness(MemoryConstant.getConfigSwappiness());
        MemoryUtils.writeDirectSwappiness(MemoryConstant.getConfigDirectSwappiness());
        FEATURE_HANDLER.enableProtectLru();
        this.mRunning.set(true);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        MemoryUtils.writeExtraFreeKbytes(MemoryConstant.DEFAULT_EXTRA_FREE_KBYTES);
        MemoryUtils.writeSwappiness(60);
        MemoryUtils.writeDirectSwappiness(60);
        FEATURE_HANDLER.disableProtectLru();
        DataDispatch.getInstance().stop();
        DmeServer.getInstance().disable();
        unSubscribeEvents();
        this.mRunning.set(false);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        if (!this.mRunning.get()) {
            return false;
        }
        DataDispatch.getInstance().reportData(data);
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public ArrayList<DumpData> getDumpData(int time) {
        if (!this.mRunning.get()) {
            return null;
        }
        return EventTracker.getInstance().getDumpData(time);
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public String saveBigData(boolean clear) {
        AwareLog.d(TAG, "enter saveBigData");
        if (!this.mRunning.get()) {
            return null;
        }
        this.mBigDataStore.totalTimeEnd = SystemClock.elapsedRealtime();
        this.mBigDataStore.getMeminfoAllocCount();
        this.mBigDataStore.getLmkOccurCount();
        String rtStatisJsonStr = this.mBigDataStore.creatJsonStr();
        if (clear) {
            this.mBigDataStore.clearCache();
        }
        return rtStatisJsonStr;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public ArrayList<StatisticsData> getStatisticsData() {
        if (!this.mRunning.get()) {
            return null;
        }
        return EventTracker.getInstance().getStatisticsData();
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean configUpdate() {
        loadMemConfig();
        MemoryUtils.onProtectLruConfigUpdate();
        DmeServer.getInstance().notifyProtectLruState(0);
        return true;
    }

    private void loadMemConfig() {
        loadMemConstantConfig(false);
        loadBigMemAppPolicyConfig(getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_BIGMEMAPP, true));
        MemoryExecutorServer.getInstance().setMemoryScenePolicyList(loadMemPolicyListConfig());
        loadFileCacheConfig(false);
        loadMemConstantConfig(true);
        loadFileCacheConfig(true);
    }

    private static void saveBigMemActivityConfig(AwareConfig.Item item) {
        BigMemoryInfo bigMemInfo = BigMemoryInfo.getInstance();
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            if (subItem == null) {
                AwareLog.w(TAG, "saveBigMemActivityConfig subItem is null");
            } else {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (!(itemName == null || itemValue == null)) {
                    if (BigMemoryConstant.MEM_POLICY_CAMERA_PROCESS_NAME.equals(itemName)) {
                        bigMemInfo.addCameraServerProcs(itemValue);
                    } else {
                        int value = parseInt(itemValue);
                        if (value == -1 || !saveBigMemActivityConfigItem(itemName, value)) {
                            AwareLog.d(TAG, itemName + " para error");
                            bigMemInfo.setBigMemFeatureOn(false);
                            return;
                        }
                    }
                }
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static boolean saveBigMemActivityConfigItem(String itemName, int value) {
        char c;
        switch (itemName.hashCode()) {
            case -2037365942:
                if (itemName.equals(BigMemoryConstant.MEM_POLICY_ACTIVITY_MAX_MEM)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1754937184:
                if (itemName.equals(BigMemoryConstant.MEM_POLICY_CAMERA_MEM_CHANGE_THRESHOLD)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -889473228:
                if (itemName.equals("switch")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -106123102:
                if (itemName.equals(BigMemoryConstant.MEM_POLICY_ACTIVITIES_MAX_COUNT)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1303340624:
                if (itemName.equals(BigMemoryConstant.MEM_POLICY_CAMERA_MEM_CHANGE_THRESHOLD_SD)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 1814640644:
                if (itemName.equals(BigMemoryConstant.MEM_POLICY_CAMERA_OPEN_DELAY_TIME)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1926208864:
                if (itemName.equals(BigMemoryConstant.MEM_POLICY_CAMERA_MAX_MEM)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 2146006862:
                if (itemName.equals(BigMemoryConstant.MEM_POLICY_CAMERA_MIN_MEM)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return saveBigMemSwitch(itemName, value);
            case 1:
                return saveBigMemActivityMaxMem(itemName, value);
            case 2:
                return saveBigMemRecordsMaxSize(itemName, value);
            case 3:
                return saveCameraMaxMem(itemName, value);
            case 4:
                return saveCameraMinMem(itemName, value);
            case 5:
                return saveCameraOpenDelayTime(itemName, value);
            case 6:
                return saveCameraMemChangeThresholdSd(itemName, value);
            case 7:
                return saveCameraMemChangeThreshold(itemName, value);
            default:
                return true;
        }
    }

    private static boolean saveCameraMemChangeThreshold(String itemName, int value) {
        if (value < 1) {
            AwareLog.d(TAG, itemName + " para error");
            return false;
        }
        BigMemoryInfo.getInstance().setCameraMemChangeThreshold(value * 1024);
        return true;
    }

    private static boolean saveCameraMemChangeThresholdSd(String itemName, int value) {
        if (value < 1) {
            AwareLog.d(TAG, itemName + " para error");
            return false;
        }
        BigMemoryInfo.getInstance().setCameraMemChangeThresholdSd(value * 1024);
        return true;
    }

    private static boolean saveCameraOpenDelayTime(String itemName, int value) {
        if (value < 2 || value > 6) {
            AwareLog.d(TAG, itemName + " value is too small or too large");
            return false;
        }
        BigMemoryInfo.getInstance().setOpenCameraDelayTime(value * 1000);
        return true;
    }

    private static boolean saveCameraMinMem(String itemName, int value) {
        if (value < 100 || ((long) value) >= getTotalMem()) {
            AwareLog.d(TAG, itemName + " value is too small or too large");
            return false;
        }
        BigMemoryInfo.getInstance().setCameraMinMem(value * 1024);
        return true;
    }

    private static boolean saveCameraMaxMem(String itemName, int value) {
        if (value < 100 || ((long) value) > getTotalMem()) {
            AwareLog.d(TAG, itemName + " value is too small or too large");
            return false;
        }
        BigMemoryInfo.getInstance().setCameraMaxMem(value * 1024);
        return true;
    }

    private static boolean saveBigMemRecordsMaxSize(String itemName, int value) {
        if (value <= 0 || value > 100) {
            AwareLog.d(TAG, itemName + " value is too small or too large");
            return false;
        }
        BigMemoryInfo.getInstance().setActivitiesSize(value);
        return true;
    }

    private static boolean saveBigMemActivityMaxMem(String itemName, int value) {
        if (value <= 0 || ((long) value) > getTotalMem()) {
            AwareLog.d(TAG, itemName + " value is too small or too large");
            return false;
        }
        BigMemoryInfo.getInstance().setActivityMaxMem(value * 1024);
        return true;
    }

    private static boolean saveBigMemSwitch(String itemName, int value) {
        boolean z = false;
        if (value == 0 || value == 1) {
            BigMemoryInfo bigMemInfo = BigMemoryInfo.getInstance();
            if (value == 1) {
                z = true;
            }
            bigMemInfo.setBigMemFeatureOn(z);
            return true;
        }
        AwareLog.d(TAG, itemName + "switch value should only be 0 or 1");
        return false;
    }

    private static int parseInt(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse int error");
            return -1;
        }
    }

    private static long getTotalMem() {
        MemInfoReaderExt memInfoReader = new MemInfoReaderExt();
        memInfoReader.readMemInfo();
        return memInfoReader.getTotalSize() / MemoryConstant.MB_SIZE;
    }

    private void loadMemConstantConfig(boolean isCustConfig) {
        AwareConfig.Item curMemItem = MemoryUtils.getCurrentMemItem(getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_CONSTANT_CONFIGNAME, isCustConfig), isCustConfig);
        if (curMemItem != null) {
            AwareLog.i(TAG, "loadMemConstantConfig sucess, iscust:" + isCustConfig);
            saveMemConstantItem(curMemItem);
            return;
        }
        AwareLog.i(TAG, "loadMemConstantConfig empty, iscust:" + isCustConfig);
    }

    public static void loadBigMemAppPolicyConfig(AwareConfig custConfigList) {
        if (custConfigList == null) {
            AwareLog.w(TAG, "loadBigMemAppConfig failure cause null configList");
            return;
        }
        MemoryUtils.initialRamSizeLowerBound();
        for (AwareConfig.Item item : custConfigList.getConfigList()) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadBigMemAppPolicyConfig continue cause null item");
            } else {
                Map<String, String> configPropertries = item.getProperties();
                String appName = configPropertries.get(MemoryConstant.MEM_POLICY_BIGAPPNAME);
                if (appName != null) {
                    AwareLog.d(TAG, "big memory app is " + appName);
                    saveBigMemoryAppConfig(appName, item);
                } else {
                    String ramSize = configPropertries.get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME);
                    if (!MemoryUtils.checkRamSize(ramSize)) {
                        AwareLog.d(TAG, "checkRamSize failure cause ramSize: " + ramSize);
                    } else {
                        AwareLog.d(TAG, "loadBigMemAppPolicyConfig success:ramsize: " + ramSize);
                        saveCameraIonConfig(item);
                    }
                }
            }
        }
        if (!BigMemoryInfo.getInstance().isMemoryRequestMapEmpty()) {
            BigMemoryInfo.getInstance().setDynamicBigMemory(false);
            AwareLog.d(TAG, "loadBigMemAppPolicyConfig dynamic is false");
        }
    }

    private static void saveBigMemoryAppConfig(String appName, AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                try {
                    if (itemName.hashCode() == -1109843021 && itemName.equals(MemoryConstant.MEM_SCENE_LAUNCH)) {
                        c = 0;
                    }
                    if (c != 0) {
                        AwareLog.w(TAG, "no such configuration!");
                    } else {
                        long launchRequestMem = Long.parseLong(itemValue.trim());
                        AwareLog.d(TAG, "saveBigMemoryAppConfig " + appName + " request memory is " + launchRequestMem);
                        BigMemoryInfo.getInstance().setRequestMemForLaunch(appName, launchRequestMem);
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "parse app mem error: " + e);
                }
            }
        }
    }

    public static void loadBigMemActivityPolicyConfig(AwareConfig custConfigList) {
        if (custConfigList == null) {
            AwareLog.w(TAG, "loadBigMemActivityPolicyConfig failure cause null configList");
            return;
        }
        BigMemoryInfo bigMemInfo = BigMemoryInfo.getInstance();
        if (!bigMemInfo.isDynamicBigMemory()) {
            AwareLog.w(TAG, "loadBigMemActivityPolicyConfig failure because of white list ");
            return;
        }
        for (AwareConfig.Item item : custConfigList.getConfigList()) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadBigMemActivityPolicyConfig continue cause null item");
            } else if (item.getProperties().get(BigMemoryConstant.BIG_MEM_INFO_ITEM_TAG) != null) {
                saveBigMemActivityConfig(item);
            }
        }
        if (bigMemInfo.getCameraMaxMem() <= bigMemInfo.getCameraMinMem() || !bigMemInfo.checkConfigParas()) {
            bigMemInfo.setBigMemFeatureOn(false);
            AwareLog.w(TAG, "loadBigMemActivityPolicyConfig para error camera max <= camera min");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0049 A[Catch:{ NumberFormatException -> 0x00a5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0084 A[Catch:{ NumberFormatException -> 0x00a5 }] */
    private static void saveCameraIonConfig(AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                try {
                    int hashCode = itemName.hashCode();
                    if (hashCode != -190523471) {
                        if (hashCode == 2089932130 && itemName.equals(MemoryConstant.MEM_CONSTANT_IONSPEEDUPSWITCH)) {
                            c = 0;
                            if (c == 0) {
                                int ionSpeedupSwitch = Integer.parseInt(itemValue.trim());
                                AwareLog.i(TAG, "saveCameraIonConfigs camera ion memory speedup switch: " + ionSpeedupSwitch);
                                MemoryConstant.setConfigIonSpeedupSwitch(ionSpeedupSwitch);
                            } else if (c != 1) {
                                AwareLog.w(TAG, "saveCameraIonConfigs no such configuration. " + itemName);
                            } else {
                                int cameraPowerupIonMemory = Integer.parseInt(itemValue.trim());
                                AwareLog.i(TAG, "saveCameraIonConfigs camera powerup memory: " + (cameraPowerupIonMemory * 1024));
                                MemoryConstant.setCameraPowerUpMemoryDefault(cameraPowerupIonMemory * 1024);
                            }
                        }
                    } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_CAMERAPOWERUPNAME)) {
                        c = 1;
                        if (c == 0) {
                        }
                    }
                    if (c == 0) {
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "saveCameraIonConfigs parse memory xml error");
                }
            }
        }
    }

    private ArraySet<String> loadFileCacheItem(AwareConfig.Item item) {
        ArraySet<String> xmlSet = new ArraySet<>();
        if (item == null || item.getProperties() == null) {
            AwareLog.w(TAG, "loadFileCacheConfig continue cause null item");
            return xmlSet;
        }
        Map<String, String> configPropertries = item.getProperties();
        String strName = configPropertries.get("name");
        if (TextUtils.isEmpty(strName)) {
            AwareLog.w(TAG, "loadFileCacheConfig failure cause name: " + strName);
            return xmlSet;
        }
        String strLevel = configPropertries.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
        if (checkFileCacheLevel(strLevel) == -1) {
            AwareLog.w(TAG, "checkFileCacheLevel failure cause level: " + strLevel);
            return xmlSet;
        }
        AwareLog.d(TAG, "loadFileCacheConfig success. level: " + strLevel);
        if (item.getSubItemList() == null) {
            AwareLog.w(TAG, "loadFileCacheConfig continue cause null item");
            return xmlSet;
        }
        for (AwareConfig.SubItem subitem : item.getSubItemList()) {
            if (subitem != null && !TextUtils.isEmpty(subitem.getValue())) {
                xmlSet.add(subitem.getValue());
            }
        }
        return xmlSet;
    }

    private void setFileCacheItem(String strName, int fileCacheLevel, boolean isCustConfig, ArrayMap<Integer, ArraySet<String>> fileCacheMap, ArraySet<String> xmlSet) {
        if ("file".equals(strName)) {
            fileCacheMap.put(Integer.valueOf(fileCacheLevel), xmlSet);
        } else if (!"package".equals(strName) || isCustConfig) {
            AwareLog.w(TAG, "setFileCacheItem unexpected name");
        } else {
            ArraySet<String> pkgSet = PackageInfoCollector.getLibFilesFromPackage(this.mContext, xmlSet);
            if (pkgSet != null && pkgSet.size() > 0) {
                fileCacheMap.put(Integer.valueOf(fileCacheLevel + 50), pkgSet);
            }
        }
    }

    private void loadFileCacheConfig(boolean isCustConfig) {
        ArrayMap<Integer, ArraySet<String>> fileCacheMap;
        AwareLog.d(TAG, "loadFileCacheConfig begin");
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_FILECACHE, isCustConfig);
        if (configList == null) {
            AwareLog.w(TAG, "loadFileCacheConfig failure cause null configList");
            return;
        }
        ArrayMap<Integer, ArraySet<String>> fileCacheMap2 = new ArrayMap<>();
        if (isCustConfig) {
            ArrayMap<Integer, ArraySet<String>> fileCacheMap3 = MemoryConstant.getFileCacheMap();
            if (fileCacheMap3 == null) {
                AwareLog.w(TAG, "loadFileCacheConfig cust fileCacheMap is null");
                return;
            }
            fileCacheMap = fileCacheMap3;
        } else {
            fileCacheMap = fileCacheMap2;
        }
        for (AwareConfig.Item item : configList.getConfigList()) {
            ArraySet<String> xmlSet = loadFileCacheItem(item);
            if (xmlSet.isEmpty()) {
                AwareLog.i(TAG, "loadFileCacheConfig xmlSet is empty");
            } else {
                AwareLog.i(TAG, "loadFileCacheConfig xmlSet=" + xmlSet.toString());
                Map<String, String> configPropertries = item.getProperties();
                setFileCacheItem(configPropertries.get("name"), checkFileCacheLevel(configPropertries.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL)), isCustConfig, fileCacheMap, xmlSet);
            }
        }
        if (!isCustConfig) {
            MemoryConstant.setFileCacheMap(fileCacheMap);
        }
        AwareLog.d(TAG, "loadFileCacheConfig end");
    }

    private int checkFileCacheLevel(String fileCacheLevel) {
        if (TextUtils.isEmpty(fileCacheLevel)) {
            return -1;
        }
        try {
            int level = Integer.parseInt(fileCacheLevel.trim());
            if (level < 1 || level > 3) {
                return -1;
            }
            return level;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse filecache index error: " + e);
            return -1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x003e  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x008e  */
    private boolean saveKernelParamItem(String itemName, String itemValue, StringBuffer logBuf) {
        char c = 65535;
        try {
            int hashCode = itemName.hashCode();
            if (hashCode != -286045405) {
                if (hashCode != 175422052) {
                    if (hashCode == 274770233 && itemName.equals(MemoryConstant.MEM_CONSTANT_DIRECTSWAPPINESSNAME)) {
                        c = 1;
                        if (c != 0) {
                            int swappiness = Integer.parseInt(itemValue.trim());
                            logBuf.append(itemName + ":" + itemValue + " ");
                            MemoryConstant.setConfigSwappiness(swappiness);
                            return true;
                        } else if (c == 1) {
                            int directswappiness = Integer.parseInt(itemValue.trim());
                            logBuf.append(itemName + ":" + itemValue + " ");
                            MemoryConstant.setConfigDirectSwappiness(directswappiness);
                            return true;
                        } else if (c != 2) {
                            return false;
                        } else {
                            int extraFreeKbytes = Integer.parseInt(itemValue.trim());
                            logBuf.append(itemName + ":" + itemValue + " ");
                            MemoryConstant.setConfigExtraFreeKbytes(extraFreeKbytes);
                            return true;
                        }
                    }
                } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_EXTRAFREEKBYTESNAME)) {
                    c = 2;
                    if (c != 0) {
                    }
                }
            } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_SWAPPINESSNAME)) {
                c = 0;
                if (c != 0) {
                }
            }
            if (c != 0) {
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error_saveKernelParamItem");
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00a5  */
    private boolean saveKernelParamItem2(String itemName, String itemValue, StringBuffer logBuf) {
        char c = 65535;
        try {
            int hashCode = itemName.hashCode();
            if (hashCode != -1111598932) {
                if (hashCode != 90016637) {
                    if (hashCode == 219089332 && itemName.equals(MemoryConstant.MEM_CONSTANT_PROTECTLRU_SWITCH)) {
                        c = 2;
                        if (c != 0) {
                            long reservedZram = Long.parseLong(itemValue.trim());
                            logBuf.append(itemName + ":" + itemValue + " ");
                            MemoryConstant.setReservedZramSpace(1024 * reservedZram);
                            return true;
                        } else if (c == 1) {
                            int reclaimFileCache = Integer.parseInt(itemValue.trim());
                            logBuf.append(itemName + ":" + itemValue + " ");
                            StringBuilder sb = new StringBuilder();
                            sb.append("loadMemConfig reclaimFileCache: ");
                            sb.append(reclaimFileCache);
                            AwareLog.d(TAG, sb.toString());
                            MemoryConstant.setConfigReclaimFileCache(reclaimFileCache);
                            return true;
                        } else if (c != 2) {
                            return false;
                        } else {
                            int protectLruSwitch = Integer.parseInt(itemValue.trim());
                            logBuf.append(itemName + ":" + itemValue + " ");
                            MemoryConstant.setConfigProtectLruSwitch(protectLruSwitch);
                            return true;
                        }
                    }
                } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_RECLAIMFILECACHE)) {
                    c = 1;
                    if (c != 0) {
                    }
                }
            } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_RESERVEDZRAMNAME)) {
                c = 0;
                if (c != 0) {
                }
            }
            if (c != 0) {
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error_saveKernelParamItem2");
            return false;
        }
    }

    private void saveMemConstantItem(AwareConfig.Item item) {
        StringBuffer logBuf = new StringBuffer();
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (itemName != null && itemValue != null && !saveKernelParamItem(itemName, itemValue, logBuf) && !saveKernelParamItem2(itemName, itemValue, logBuf) && !saveMemConstantKillItems(itemName, itemValue, logBuf)) {
                setMemConfigValue(itemName, itemValue, logBuf);
            }
        }
        AwareLog.d(TAG, logBuf.toString());
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void setMemConfigValue(String itemName, String itemValue, StringBuffer logBuf) {
        char c = 65535;
        try {
            switch (itemName.hashCode()) {
                case -1919496163:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_KILL_GAP)) {
                        c = 3;
                        break;
                    }
                    break;
                case -1053226004:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_CLEAN_ALL)) {
                        c = 2;
                        break;
                    }
                    break;
                case -489919045:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROTECTLRULIMIT)) {
                        c = 0;
                        break;
                    }
                    break;
                case -484609525:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROTECTRATIO)) {
                        c = 1;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                String protectLruLimit = itemValue.trim();
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setConfigProtectLruLimit(protectLruLimit);
            } else if (c == 1) {
                int ratio = Integer.parseInt(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setConfigProtectLruRatio(ratio);
            } else if (c == 2) {
                int cleanAllSwitch = Integer.parseInt(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setCleanAllSwitch(cleanAllSwitch);
            } else if (c == 3) {
                long killMemGap = Long.parseLong(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setKillGapMemory(killMemGap);
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean saveMemConstantKillItems(String itemName, String itemValue, StringBuffer logBuf) {
        char c = 65535;
        try {
            switch (itemName.hashCode()) {
                case -633324046:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_EMERGEMCYMEMORYNAME)) {
                        c = 2;
                        break;
                    }
                    break;
                case -34546488:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_NORMALMEMORYNAME)) {
                        c = 5;
                        break;
                    }
                    break;
                case 671098540:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_HIGHCPULOADNAME)) {
                        c = 1;
                        break;
                    }
                    break;
                case 745458209:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_DEFAULTCRITICALMEMORYNAME)) {
                        c = 4;
                        break;
                    }
                    break;
                case 1078523066:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_LOWCPULOADNAME)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1403791765:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_BIGMEMCRITICALMEMORYNAME)) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                long lowCpuLoad = Long.parseLong(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setIdleThresHold(lowCpuLoad);
                return true;
            } else if (c == 1) {
                long highCpuLoad = Long.parseLong(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setNormalThresHold(highCpuLoad);
                return true;
            } else if (c == 2) {
                long emergemcyMemory = Long.parseLong(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setEmergencyMemory(1024 * emergemcyMemory);
                return true;
            } else if (c == 3) {
                long bigMemCriticalMemory = Long.parseLong(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setBigMemoryAppCriticalMemory(1024 * bigMemCriticalMemory);
                return true;
            } else if (c == 4) {
                long criticalMemory = Long.parseLong(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setDefaultCriticalMemory(1024 * criticalMemory);
                return true;
            } else if (c != 5) {
                return false;
            } else {
                long normalMemory = Long.parseLong(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setIdleMemory(1024 * normalMemory);
                return true;
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
            return false;
        }
    }

    private MemoryScenePolicyList loadMemPolicyListConfig() {
        String scene;
        Map<String, MemoryScenePolicy> memoryScenePolicies = new ArrayMap<>();
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_CONFIGNAME, false);
        if (configList == null) {
            return new MemoryScenePolicyList(createDefaultMemorPolicyList());
        }
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (!(item.getProperties() == null || (scene = item.getProperties().get("scene")) == null)) {
                AwareLog.d(TAG, "add scene: " + scene);
                ArrayList<Action> memActions = getActionList(item);
                if (memActions.size() > 0) {
                    memoryScenePolicies.put(scene, new MemoryScenePolicy(scene, memActions));
                }
            }
        }
        return new MemoryScenePolicyList(memoryScenePolicies);
    }

    private Map<String, MemoryScenePolicy> createDefaultMemorPolicyList() {
        Map<String, MemoryScenePolicy> memoryScenePolicies = new ArrayMap<>();
        ArrayList<Action> memActions = new ArrayList<>();
        memActions.add(new KillAction(this.mContext));
        memoryScenePolicies.put(MemoryConstant.MEM_SCENE_DEFAULT, new MemoryScenePolicy(MemoryConstant.MEM_SCENE_DEFAULT, memActions));
        ArrayList<Action> bigMemActions = new ArrayList<>();
        bigMemActions.add(new QuickKillAction(this.mContext));
        memoryScenePolicies.put(MemoryConstant.MEM_SCENE_BIGMEM, new MemoryScenePolicy(MemoryConstant.MEM_SCENE_BIGMEM, bigMemActions));
        ArrayList<Action> idleActions = new ArrayList<>();
        idleActions.add(new ReclaimAction(this.mContext));
        idleActions.add(new GpuCompressAction(this.mContext));
        idleActions.add(new SystemTrimAction(this.mContext));
        memoryScenePolicies.put(MemoryConstant.MEM_SCENE_IDLE, new MemoryScenePolicy(MemoryConstant.MEM_SCENE_IDLE, idleActions));
        AwareLog.i(TAG, "use default memory policy cause reading xml config failure");
        return memoryScenePolicies;
    }

    private ArrayList<Action> getActionList(AwareConfig.Item item) {
        Action action;
        ArrayList<Action> actions = new ArrayList<>();
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            if (subItem.getProperties() != null) {
                String actionName = subItem.getProperties().get("name");
                char c = 65535;
                switch (actionName.hashCode()) {
                    case 102461:
                        if (actionName.equals(MemoryConstant.MEM_POLICY_GMCACTION)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 3291998:
                        if (actionName.equals(MemoryConstant.MEM_POLICY_KILLACTION)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 643839697:
                        if (actionName.equals(MemoryConstant.MEM_POLICY_SYSTEMTRIMACTION)) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1082491369:
                        if (actionName.equals(MemoryConstant.MEM_POLICY_RECLAIM)) {
                            c = 0;
                            break;
                        }
                        break;
                    case 1301455563:
                        if (actionName.equals(MemoryConstant.MEM_POLICY_QUICKKILLACTION)) {
                            c = 2;
                            break;
                        }
                        break;
                }
                if (c == 0) {
                    action = new ReclaimAction(this.mContext);
                } else if (c == 1) {
                    action = new KillAction(this.mContext);
                } else if (c == 2) {
                    action = new QuickKillAction(this.mContext);
                } else if (c == 3) {
                    action = new GpuCompressAction(this.mContext);
                } else if (c != 4) {
                    AwareLog.e(TAG, "no such action!");
                    action = null;
                } else {
                    action = new SystemTrimAction(this.mContext);
                }
                if (action != null) {
                    AwareLog.d(TAG, "add action: " + actionName);
                    actions.add(action);
                }
            }
        }
        AwareLog.d(TAG, "getActionList return: " + actions);
        return actions;
    }

    private AwareConfig getConfig(String featureName, String configName, boolean isCustConfig) {
        if (featureName == null || featureName.isEmpty() || configName == null || configName.isEmpty()) {
            AwareLog.i(TAG, "featureName or configName is null");
            return null;
        }
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService == null) {
                AwareLog.i(TAG, "can not find service awareService.");
                return null;
            } else if (isCustConfig) {
                return IAwareCMSManager.getCustConfig(awareService, featureName, configName);
            } else {
                return IAwareCMSManager.getConfig(awareService, featureName, configName);
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "MemoryFeature getConfig RemoteException");
            return null;
        }
    }

    private void subscribleEvents() {
        if (this.mDataRegister != null) {
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RES_DEV_STATUS, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_USERHABIT, this.mFeatureType);
            this.mDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_FACE_RECOGNIZE, this.mFeatureType);
        }
    }

    private void unSubscribeEvents() {
        if (this.mDataRegister != null) {
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_DEV_STATUS, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
            this.mDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_FACE_RECOGNIZE, this.mFeatureType);
        }
    }

    private static final class FeatureHandler extends Handler {
        private static final int MSG_CONFIG_PROTECTLRU = 102;
        private static final int MSG_DISABLE_PROTECTLRU = 101;
        private boolean mFirstAccess;

        private FeatureHandler() {
            this.mFirstAccess = true;
        }

        public void removeProtectLruMsg() {
            removeMessages(101);
        }

        public void enableProtectLru() {
            removeProtectLruMsg();
            if (this.mFirstAccess) {
                sendEmptyMessageDelayed(102, 20000);
            }
            DmeServer.getInstance().notifyProtectLruState(0);
            this.mFirstAccess = false;
        }

        public void disableProtectLru() {
            removeProtectLruMsg();
            sendEmptyMessageDelayed(101, 1000);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i == 101) {
                MemoryUtils.disableProtectLru();
            } else if (i != 102) {
                AwareLog.e(MemoryFeature.TAG, "error msg what = " + msg.what);
            } else {
                MemoryUtils.onProtectLruConfigUpdate();
            }
        }
    }
}
