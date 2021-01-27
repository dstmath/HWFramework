package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwareCMSManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.appswap.AwareAppSwapMng;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppUseDataManager;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.policy.CachedMemoryCleanPolicy;
import com.android.server.rms.iaware.memory.policy.SystemTrimPolicy;
import com.android.server.rms.iaware.memory.utils.BigDataStore;
import com.android.server.rms.iaware.memory.utils.BigMemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryLockFile;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.android.server.rms.iaware.memory.utils.PrereadUtils;
import com.android.server.rms.memrepair.MemRepairPolicy;
import com.android.server.rms.memrepair.NativeAppMemRepair;
import com.android.server.rms.memrepair.ProcStateStatisData;
import com.android.server.rms.memrepair.SysMemMngBigData;
import com.android.server.rms.memrepair.SystemAppMemRepairMng;
import com.huawei.android.internal.util.MemInfoReaderExt;
import com.huawei.android.os.SystemPropertiesEx;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryFeatureEx extends RFeature {
    private static final int BASE_VERSION = 2;
    public static final AtomicBoolean IS_UP_MEMORY_FEATURE = new AtomicBoolean(false);
    private static final int MAX_CACHED_APPS = SystemPropertiesEx.getInt("ro.sys.fw.bg_apps_limit", 32);
    private static final int MAX_CACHED_APP_SIZE = 512;
    private static final int MIN_CACHED_APP_SIZE = 4;
    private static final int POLICY_ARRAY_LENGTH_MAX = 2;
    private static final int SYS_MEM_MNG_THRESHOLD_KB_SIZE = 1024;
    private static final String TAG = "MemFeature2.0";
    private int mEmptyProcessPercent;
    private HwActivityManagerService mHwAms;
    private MemoryLockFile mLockFile;
    private int mNumProcessLimit;
    private PrereadUtils mPrereadUtils;
    private long timeStamp;

    public MemoryFeatureEx(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        this.mEmptyProcessPercent = SystemPropertiesEx.getInt("ro.sys.fw.empty_app_percent", 50);
        this.timeStamp = 0;
        this.mLockFile = new MemoryLockFile();
        this.mHwAms = null;
        this.mHwAms = HwActivityManagerService.self();
        this.mNumProcessLimit = MAX_CACHED_APPS;
        this.mPrereadUtils = PrereadUtils.getInstance();
        PrereadUtils.setContext(context);
        loadMemConfig();
        ProcStateStatisData.getInstance().setContext(context);
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        AwareLog.d(TAG, "enableFeatureEx realVersion=" + realVersion);
        if (realVersion < 2) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", baseVersion: 2");
            return false;
        }
        enableMemoryFeatureTwo();
        if (realVersion >= 3) {
            enableMemoryFeatureThree();
        }
        if (realVersion >= 5) {
            enableMemoryFeatureFive();
        }
        if (realVersion >= 6) {
            CachedMemoryCleanPolicy.getInstance().initialize();
        }
        if (realVersion < 6) {
            return true;
        }
        AwareAppSwapMng.getInstance();
        AwareAppSwapMng.enable();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        IS_UP_MEMORY_FEATURE.set(false);
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            MemoryUtils.writeMemMonitorSwitch(0);
        }
        this.mLockFile.clearPinFile();
        MemoryUtils.destroySocket();
        PrereadUtils prereadUtils = this.mPrereadUtils;
        PrereadUtils.stop();
        ProcStateStatisData.getInstance().setEnable(false);
        setEmptyProcessPercent(-1);
        setProcessLimit(MAX_CACHED_APPS);
        SystemTrimPolicy.getInstance().disable();
        MemoryUtils.setQosMemorySwitch(0);
        MemoryUtils.setEglInitOptSwitch(0);
        SystemAppMemRepairMng.getInstance().disable();
        NativeAppMemRepair.getInstance().disable();
        MemoryConstant.setIawareThirdSwitch(0);
        MemoryConstant.setIawareFifthSwitch(0);
        MemoryUtils.rccIdleCompressEnable(false);
        CachedMemoryCleanPolicy.getInstance().deInitialize();
        AwareAppSwapMng.getInstance();
        AwareAppSwapMng.disable();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean configUpdate() {
        loadMemConfig();
        setProcessLimit(this.mNumProcessLimit);
        this.mLockFile.clearPinFile();
        this.mLockFile.addPinFile();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        return null;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public String getDftDataByVersion(int iawareVer, boolean forBeta, boolean clearData, boolean betaEncode) {
        AwareLog.i(TAG, "MemoryfeatureEx getDftDataByVersion. iawareVer: " + iawareVer + ", beta: " + forBeta + ", clear: " + clearData + ";betaEncode=" + betaEncode);
        if (!IS_UP_MEMORY_FEATURE.get() || iawareVer < 2 || AwareConstant.CURRENT_USER_TYPE != 3) {
            return null;
        }
        if (betaEncode) {
            String str = BigDataStore.getInstance().makeMemBigData(clearData, System.currentTimeMillis());
            AwareLog.d(TAG, "MemoryfeatureEx getDftDataByVersion composed: " + str);
            return str;
        } else if (!forBeta) {
            return null;
        } else {
            String jsonStr = SysMemMngBigData.getInstance().savaBigData(true);
            AwareLog.d(TAG, "MemoryfeatureEx getDftDataByVersion SysMemMngBigData: " + jsonStr);
            return jsonStr;
        }
    }

    private void enableMemoryFeatureTwo() {
        IS_UP_MEMORY_FEATURE.set(true);
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            MemoryUtils.writeMemMonitorSwitch(1);
        }
        this.mLockFile.addPinFile();
        readMemoryApiWhiteListUid();
        PrereadUtils prereadUtils = this.mPrereadUtils;
        PrereadUtils.start();
        ProcStateStatisData.getInstance().setEnable(true);
        setEmptyProcessPercent(this.mEmptyProcessPercent);
        setProcessLimit(this.mNumProcessLimit);
    }

    private void enableMemoryFeatureThree() {
        MemoryConstant.setIawareThirdSwitch(1);
        loadKillFreqConfig();
    }

    private void enableMemoryFeatureFive() {
        MemoryUtils.setQosMemorySwitch(1);
        MemoryUtils.setEglInitOptSwitch(1);
        loadSysTrimConfig(false);
        loadSysTrimConfig(true);
        loadNativeAppThresholdConfig(false);
        loadNativeAppThresholdConfig(true);
        MemoryFeature.loadBigMemActivityPolicyConfig(getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, BigMemoryConstant.MEM_POLICY_ACTIVITY_MEM_APP, true));
        MemoryConstant.setIawareFifthSwitch(1);
        if (MemoryConstant.isKernCompressEnable()) {
            MemoryUtils.rccIdleCompressEnable(true);
            MemoryUtils.rccSetIdleThreshold(MemoryConstant.getKernCompressIdleThreshold());
            MemoryUtils.rccSetAvailTarget((int) (MemoryConstant.getIdleMemory() / 1024));
            MemoryConstant.setRccHighAvailTarget();
            MemoryUtils.rccSetAnonTarget(MemoryConstant.getKernCompressAnonTarget());
            MemoryUtils.rccSetSwapPercent(MemoryConstant.getKernCompressSwapPercent());
        }
        loadMemRepairIonConfig();
        AwareAppUseDataManager.getInstance().reportStart();
        if (MemoryConstant.getPressureReclaimSwitch() == 1) {
            MemoryUtils.enablePsi();
        }
    }

    private void loadMemConfig() {
        loadMemConstantConfig(false);
        loadMemConstantConfig(true);
        loadPrereadConfig();
        loadCameraPreloadReclaimConfig();
        loadMemRepairConfig();
        MemoryConstant.clearPinnedFilesStr();
        loadPinFileConfig();
        MemoryFeature.loadBigMemAppPolicyConfig(getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_BIGMEMAPP, true));
        MemoryFeature.loadBigMemAppPolicyConfig(getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_IONPROPERTYS, true));
        loadNotificationConfig();
        loadMemRepirSumPssSwitchConfig();
        loadScanModeOptSwitchConfig();
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

    public void loadPrereadConfig() {
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_PREREAD_CONFIGNAME, true);
        if (configList == null) {
            AwareLog.w(TAG, "loadPrereadConfig failure, configList is empty");
            return;
        }
        Map<String, ArrayList<String>> filePathMap = new ArrayMap<>();
        parsePreloadConfig(filePathMap, configList);
        MemoryConstant.setPrereadFileMap(filePathMap);
        this.mPrereadUtils.sendPrereadDataUpdateMsg();
    }

    private void parsePreloadConfig(Map<String, ArrayList<String>> filePathMap, AwareConfig configList) {
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (item == null) {
                AwareLog.w(TAG, "loadPrereadConfig failure, item is empty");
            } else {
                Map<String, String> configProperties = item.getProperties();
                if (configProperties == null) {
                    AwareLog.w(TAG, "loadPrereadConfig failure, configProperties is empty");
                } else {
                    String pkgName = configProperties.get(MemoryConstant.MEM_PREREAD_ITEM_NAME);
                    if (pkgName != null) {
                        if (!pkgName.equals(MemoryConstant.FACE_RECOGNIZE_CONFIGNAME)) {
                            try {
                                if (Integer.parseInt(configProperties.get(MemoryConstant.MEM_PREREAD_SWITCH)) == 0) {
                                    AwareLog.w(TAG, "prereadSwitch off");
                                }
                            } catch (NumberFormatException e) {
                                AwareLog.e(TAG, "Number Format Error !");
                            }
                        }
                        ArrayList<String> filePath = new ArrayList<>();
                        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
                        if (subItemList != null) {
                            for (AwareConfig.SubItem subItem : subItemList) {
                                String itemName = subItem.getName();
                                String itemValue = subItem.getValue();
                                if ("file".equals(itemName)) {
                                    filePath.add(itemValue);
                                }
                            }
                            filePathMap.put(pkgName, filePath);
                        }
                    }
                }
            }
        }
    }

    private void loadCameraPreloadReclaimConfig() {
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_CAMERAPRELOADRECLAIM_CONFIGNAME, true);
        if (configList == null) {
            AwareLog.w(TAG, "CameraPreloadReclaimConfig failure, configList is empty");
            return;
        }
        List<AwareConfig.Item> items = configList.getConfigList();
        if (items == null) {
            AwareLog.w(TAG, "CameraPreloadReclaimConfig failure, configList is null");
        } else if (items.size() != 1) {
            AwareLog.w(TAG, "CameraPreloadReclaimConfig failure, configList size is not 1");
        } else {
            AwareConfig.Item item = items.get(0);
            if (item == null) {
                AwareLog.w(TAG, "CameraPreloadReclaimConfig failure, item is empty");
                return;
            }
            Map<String, String> configProperties = item.getProperties();
            if (configProperties == null) {
                AwareLog.w(TAG, "CameraPreloadReclaimConfig failure, configProperties is empty");
                return;
            }
            try {
                int preloadSwitch = Integer.parseInt(configProperties.get(MemoryConstant.MEM_CAMERAPRELOADRECLAIM_CONFIGNAME_SWITCH));
                int reclaimDelay = Integer.parseInt(configProperties.get(MemoryConstant.MEM_CAMERAPRELOADRECLAIM_CONFIGNAME_RECLAIMDELAY));
                int killUss = Integer.parseInt(configProperties.get(MemoryConstant.MEM_CAMERAPRELOADRECLAIM_CONFIGNAME_KILLUSS));
                AwareLog.d(TAG, "CameraPreloadReclaimConfig preloadSwitch " + preloadSwitch + " reclaimDelay" + reclaimDelay + " killUss" + killUss);
                setCameraPreloadReclaimConfig(preloadSwitch, reclaimDelay, killUss);
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "loadCameraPreloadReclaimConfig Number Format Error !");
            }
        }
    }

    private void setCameraPreloadReclaimConfig(int sw, int delay, int uss) {
        if (sw == 1 && delay < 10000 && delay > 0 && uss < 102400 && uss > 1024) {
            MemoryConstant.setCameraPreloadSwitch(1);
            MemoryConstant.setCameraPreloadReclaimDelay(delay);
            MemoryConstant.setCameraPreloadKillUss(uss);
        }
    }

    private void loadNotificationConfig() {
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_NOTIFICATION_CONFIGNAME, true);
        if (configList == null || configList.getConfigList() == null) {
            AwareLog.w(TAG, "loadNotificationConfig failure, configList is empty");
            return;
        }
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (item == null) {
                AwareLog.w(TAG, "loadNotificationConfig failure, item is empty");
            } else {
                Map<String, String> configProperties = item.getProperties();
                if (configProperties == null) {
                    AwareLog.w(TAG, "loadNotificationConfig failure, configProperties is empty");
                } else {
                    try {
                        MemoryConstant.setConfigNotificatinSwitch(Integer.parseInt(configProperties.get(MemoryConstant.MEM_NOTIFICATION_SWITCH)));
                        MemoryConstant.setNotificationInterval(Long.parseLong(configProperties.get(MemoryConstant.MEM_NOTIFICATION_INTERVAL)));
                        return;
                    } catch (NumberFormatException e) {
                        AwareLog.e(TAG, "Number Format Error !");
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public AwareConfig getConfig(String featureName, String configName, boolean isCustConfig) {
        if (featureName == null || featureName.isEmpty() || configName == null || configName.isEmpty()) {
            AwareLog.i(TAG, "featureName or configName is null");
            return null;
        }
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice == null) {
                AwareLog.i(TAG, "can not find service awareservice.");
                return null;
            } else if (isCustConfig) {
                return IAwareCMSManager.getCustConfig(awareservice, featureName, configName);
            } else {
                return IAwareCMSManager.getConfig(awareservice, featureName, configName);
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "MemoryFeature getConfig RemoteException");
            return null;
        }
    }

    private void saveQosMemoryConfig(String itemValue) {
        String[] sets = itemValue.split(",");
        if (sets.length != 5) {
            AwareLog.w(TAG, "saveQosMemoryConfig val size error!");
            return;
        }
        long[] qosConfigVal = new long[5];
        int index = 0;
        try {
            int length = sets.length;
            int i = 0;
            while (i < length) {
                qosConfigVal[index] = Long.parseLong(sets[i].trim());
                i++;
                index++;
            }
            MemoryConstant.setConfigQosMemoryWatermark(qosConfigVal);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse qos mem from xml error");
        }
    }

    private void saveQosMemorySwitch(String itemValue) {
        try {
            MemoryConstant.setConfigQosMemorySwitch(Long.parseLong(itemValue.trim()));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse qos mem switch from xml error");
        }
    }

    private void saveEglInitOptSwitch(String itemValue) {
        try {
            MemoryConstant.setEglInitOptConfigSwitch(Long.parseLong(itemValue.trim()));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse egl pre init switch from xml error");
        }
    }

    private void savePressureReclaimSwitch(String itemValue) {
        try {
            MemoryConstant.setPressureReclaimSwitch(Integer.parseInt(itemValue.trim()));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse pressure reclaim switch from xml error");
        }
    }

    private void saveBigMemEnableDmeSwitch(String itemValue) {
        try {
            MemoryConstant.setBigMemEnableDmeSwitch(Integer.parseInt(itemValue.trim()));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse big memory activity enable DmeServer switch from xml error");
        }
    }

    private void saveExtraDependOnPsiSwitch(String itemValue) {
        try {
            MemoryConstant.setExtraDependOnPsiSwitch(Integer.parseInt(itemValue.trim()));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse extra depend on memory psi switch from xml error");
        }
    }

    private void saveMemConstantItem(AwareConfig.Item item) {
        StringBuffer logbuf = new StringBuffer();
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (itemName == null || itemValue == null || saveMemConstantItemPart1(itemName, itemValue, logbuf) || saveMemConstantItemPart2(itemName, itemValue, logbuf) || setMemConfigValue(itemName, itemValue, logbuf)) {
            }
        }
        AwareLog.d(TAG, logbuf.toString());
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean saveMemConstantItemPart1(String itemName, String itemValue, StringBuffer logbuf) {
        char c = 65535;
        try {
            switch (itemName.hashCode()) {
                case -1470603166:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_SYSTEMTRIMWITCH)) {
                        c = 0;
                        break;
                    }
                    break;
                case -798160691:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_GPUNAME)) {
                        c = 3;
                        break;
                    }
                    break;
                case -533820387:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROCESSPERCENT)) {
                        c = 4;
                        break;
                    }
                    break;
                case 53466486:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_GMCSWITCH)) {
                        c = 1;
                        break;
                    }
                    break;
                case 63393072:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_APPTRIMSWITCH)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                int systemTrimSwitch = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setConfigSystemTrimSwitch(systemTrimSwitch);
                return true;
            } else if (c == 1) {
                int gmcSwitch = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setConfigGmcSwitch(gmcSwitch);
                return true;
            } else if (c == 2) {
                int appTrimSwitch = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setConfigAppTrimSwitch(appTrimSwitch);
                return true;
            } else if (c == 3) {
                long gpuMemoryLimit = Long.parseLong(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setGpuMemoryLimit(1024 * gpuMemoryLimit);
                return true;
            } else if (c != 4) {
                return false;
            } else {
                int numProcessPercent = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                if (numProcessPercent < 0 || numProcessPercent > 100) {
                    return true;
                }
                this.mEmptyProcessPercent = SystemPropertiesEx.getInt("ro.sys.fw.empty_app_percent", numProcessPercent);
                return true;
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml NumberFormatException");
            return false;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean saveMemConstantItemPart2(String itemName, String itemValue, StringBuffer logbuf) {
        char c = 65535;
        try {
            switch (itemName.hashCode()) {
                case -1919496163:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_KILL_GAP)) {
                        c = 4;
                        break;
                    }
                    break;
                case -1669721547:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_PREREAD_ODEX)) {
                        c = 1;
                        break;
                    }
                    break;
                case -1053226004:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_CLEAN_ALL)) {
                        c = 3;
                        break;
                    }
                    break;
                case -822986700:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_API_MAX_REQUEST_MEM)) {
                        c = 2;
                        break;
                    }
                    break;
                case -470167246:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROCESSLIMIT)) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                int numProcessLimit = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                this.mNumProcessLimit = SystemPropertiesEx.getInt("ro.sys.fw.bg_apps_limit", numProcessLimit);
                return true;
            } else if (c == 1) {
                int prereadOdexSwitch = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                PrereadUtils.getInstance();
                PrereadUtils.setPrereadOdexSwitch(prereadOdexSwitch);
                return true;
            } else if (c == 2) {
                long maxApiRequestMem = Long.parseLong(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setMaxRequestMemory(maxApiRequestMem);
                return true;
            } else if (c == 3) {
                int cleanAllSwitch = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setCleanAllSwitch(cleanAllSwitch);
                return true;
            } else if (c != 4) {
                return false;
            } else {
                long killMemGap = Long.parseLong(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setKillGapMemory(killMemGap);
                return true;
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml NumberFormatException");
            return false;
        }
    }

    private boolean setMemConfigValue(String itemName, String itemValue, StringBuffer logbuf) {
        if (!setMemConfigValueInternel1(itemName, itemValue, logbuf) && !setMemConfigValueInternel2(itemName, itemValue, logbuf) && !setMemConfigValueInternel3(itemName, itemValue, logbuf) && !setMemConfigValueInternel4(itemName, itemValue, logbuf)) {
            return false;
        }
        return true;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean setMemConfigValueInternel1(String itemName, String itemValue, StringBuffer logbuf) {
        char c = 65535;
        try {
            switch (itemName.hashCode()) {
                case -2061925320:
                    if (itemName.equals(MemoryConstant.MEM_PRESSURE_RECLAIM_SWITCH)) {
                        c = 7;
                        break;
                    }
                    break;
                case -1948547035:
                    if (itemName.equals(MemoryConstant.MEM_EGL_INITOPT_SWITCH_CONFIGNAME)) {
                        c = 6;
                        break;
                    }
                    break;
                case -1418184189:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_FAST_QUICKKILL)) {
                        c = 2;
                        break;
                    }
                    break;
                case -714132116:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_MORE_PREVIOUS)) {
                        c = 3;
                        break;
                    }
                    break;
                case -51235512:
                    if (itemName.equals(MemoryConstant.MEM_QOS_SWITCH_CONFIGNAME)) {
                        c = 5;
                        break;
                    }
                    break;
                case 786514384:
                    if (itemName.equals(MemoryConstant.MEM_QOS_WATERMARK_CONFIGNAME)) {
                        c = 4;
                        break;
                    }
                    break;
                case 1050088593:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_EXACT_KILL)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1686655406:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_FAST_KILL)) {
                        c = 1;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    int exactKillSwitch = Integer.parseInt(itemValue.trim());
                    logbuf.append(itemName + ":" + itemValue + " ");
                    MemoryConstant.setExactKillSwitch(exactKillSwitch);
                    return true;
                case 1:
                    int fastKillSwitch = Integer.parseInt(itemValue.trim());
                    logbuf.append(itemName + ":" + itemValue + " ");
                    MemoryConstant.setFastKillSwitch(fastKillSwitch);
                    return true;
                case 2:
                    int fastQuickKillSwitch = Integer.parseInt(itemValue.trim());
                    logbuf.append(itemName + ":" + itemValue + " ");
                    MemoryConstant.setFastQuickKillSwitch(fastQuickKillSwitch);
                    return true;
                case 3:
                    int morePreviousLevel = Integer.parseInt(itemValue.trim());
                    logbuf.append(itemName + ":" + itemValue + " ");
                    AwareAppAssociate.getInstance().setMorePreviousLevel(morePreviousLevel);
                    return true;
                case 4:
                    saveQosMemoryConfig(itemValue);
                    return true;
                case 5:
                    saveQosMemorySwitch(itemValue);
                    return true;
                case 6:
                    saveEglInitOptSwitch(itemValue);
                    return true;
                case 7:
                    savePressureReclaimSwitch(itemValue);
                    return true;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            AwareLog.d(TAG, "parse interval failure cause :NumberFormatException");
            return false;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean setMemConfigValueInternel2(String itemName, String itemValue, StringBuffer logBuf) {
        char c = 65535;
        try {
            switch (itemName.hashCode()) {
                case -2084263182:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_TOPN_PROCESS_MEM)) {
                        c = 5;
                        break;
                    }
                    break;
                case -1695261643:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_KERNEL_ANON_TARGET)) {
                        c = 2;
                        break;
                    }
                    break;
                case -1463174335:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_PRINT_ENHANCE)) {
                        c = 4;
                        break;
                    }
                    break;
                case -1182948998:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_KERNEL_SWAP_PERCENT)) {
                        c = 3;
                        break;
                    }
                    break;
                case -522521793:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_KERNEL_IDLE_THRESHOLD)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1198544876:
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_KERNEL_COMPRESS_SWITCH)) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                int rccSwitch = Integer.parseInt(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setConfigKernCompressSwitch(rccSwitch);
                return true;
            } else if (c == 1) {
                int cpuPercent = Integer.parseInt(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setKernCompressIdleThreshold(cpuPercent);
                return true;
            } else if (c == 2) {
                int target = Integer.parseInt(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setKernCompressAnonTarget(target);
                return true;
            } else if (c == 3) {
                int percent = Integer.parseInt(itemValue.trim());
                logBuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setKernCompressSwapPercent(percent);
                return true;
            } else if (c == 4) {
                int killPrintSwitch = Integer.parseInt(itemValue.trim());
                logBuf.append(itemName);
                logBuf.append(":");
                logBuf.append(itemValue);
                logBuf.append(" ");
                MemoryConstant.setPrintEnhanceSwitch(killPrintSwitch);
                return true;
            } else if (c != 5) {
                return false;
            } else {
                int topnMemProc = Integer.parseInt(itemValue.trim());
                logBuf.append(itemName);
                logBuf.append(":");
                logBuf.append(itemValue);
                logBuf.append(" ");
                MemoryConstant.setTopnMemProcNum(topnMemProc);
                return true;
            }
        } catch (NumberFormatException e) {
            AwareLog.d(TAG, "parse interval failure cause :NumberFormatException");
            return false;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean setMemConfigValueInternel3(String itemName, String itemValue, StringBuffer logBuf) {
        char c = 65535;
        try {
            switch (itemName.hashCode()) {
                case -2025354689:
                    if (itemName.equals(MemoryConstant.EXTRA_DEPEND_ON_PSI_SWITCH)) {
                        c = 0;
                        break;
                    }
                    break;
                case -1992326998:
                    if (itemName.equals(MemoryConstant.MEM_SWAP_LOW_THRESHOLD)) {
                        c = 6;
                        break;
                    }
                    break;
                case -1523452317:
                    if (itemName.equals(MemoryConstant.MEM_SWAP_MEDIUM_THRESHOLD)) {
                        c = 5;
                        break;
                    }
                    break;
                case 190631337:
                    if (itemName.equals(MemoryConstant.PSI_CPU_THRESHOLD)) {
                        c = 3;
                        break;
                    }
                    break;
                case 582082399:
                    if (itemName.equals(MemoryConstant.PSI_IO_THRESHOLD)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1345195036:
                    if (itemName.equals(MemoryConstant.PSI_MEM_THRESHOLD)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1915544086:
                    if (itemName.equals(MemoryConstant.MEM_SWAP_HIGH_THRESHOLD)) {
                        c = 4;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    saveExtraDependOnPsiSwitch(itemValue);
                    return true;
                case 1:
                    String psiIoThreshold = itemValue.trim();
                    logBuf.append(itemName);
                    logBuf.append(":");
                    logBuf.append(itemValue);
                    logBuf.append(" ");
                    MemoryConstant.setConfigPsiThreshold(0, psiIoThreshold);
                    return true;
                case 2:
                    String psiMemThreshold = itemValue.trim();
                    logBuf.append(itemName);
                    logBuf.append(":");
                    logBuf.append(itemValue);
                    logBuf.append(" ");
                    MemoryConstant.setConfigPsiThreshold(1, psiMemThreshold);
                    return true;
                case 3:
                    String psiCpuThreshold = itemValue.trim();
                    logBuf.append(itemName);
                    logBuf.append(":");
                    logBuf.append(itemValue);
                    logBuf.append(" ");
                    MemoryConstant.setConfigPsiThreshold(2, psiCpuThreshold);
                    return true;
                case 4:
                    int swapHighThreshold = Integer.parseInt(itemValue.trim());
                    logBuf.append(itemName);
                    logBuf.append(":");
                    logBuf.append(itemValue);
                    logBuf.append(" ");
                    MemoryConstant.setSwapHighThreshold(swapHighThreshold);
                    return true;
                case 5:
                    int swapMediumThreshold = Integer.parseInt(itemValue.trim());
                    logBuf.append(itemName);
                    logBuf.append(":");
                    logBuf.append(itemValue);
                    logBuf.append(" ");
                    MemoryConstant.setSwapMediumThreshold(swapMediumThreshold);
                    return true;
                case 6:
                    int swapLowThreshold = Integer.parseInt(itemValue.trim());
                    logBuf.append(itemName);
                    logBuf.append(":");
                    logBuf.append(itemValue);
                    logBuf.append(" ");
                    MemoryConstant.setSwapLowThreshold(swapLowThreshold);
                    return true;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            AwareLog.d(TAG, "parse interval failure cause :NumberFormatException");
            return false;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean setMemConfigValueInternel4(String itemName, String itemValue, StringBuffer logBuf) {
        char c = 65535;
        try {
            switch (itemName.hashCode()) {
                case -1395754022:
                    if (itemName.equals(MemoryConstant.MEM_SWAPPINESS_RANGE)) {
                        c = 0;
                        break;
                    }
                    break;
                case -891694571:
                    if (itemName.equals(MemoryConstant.MEM_ENHANCED_KILL_SIZE)) {
                        c = 1;
                        break;
                    }
                    break;
                case -376299229:
                    if (itemName.equals(MemoryConstant.MEM_BIGMEM_ENABLE_DMESERVER_SWITCH)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1985079069:
                    if (itemName.equals(MemoryConstant.MEM_ENHANCED_RCC_SIZE)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                String swappinessRange = itemValue.trim();
                logBuf.append(itemName);
                logBuf.append(":");
                logBuf.append(itemValue);
                logBuf.append(" ");
                MemoryConstant.setConfigSwappinessRange(swappinessRange);
                return true;
            } else if (c == 1) {
                long enhancedKillSize = Long.parseLong(itemValue.trim());
                logBuf.append(itemName);
                logBuf.append(":");
                logBuf.append(itemValue);
                logBuf.append(" ");
                MemoryConstant.setConfigEnhancedKillSize(enhancedKillSize);
                return true;
            } else if (c == 2) {
                long enhancedRccSize = Long.parseLong(itemValue.trim());
                logBuf.append(itemName);
                logBuf.append(":");
                logBuf.append(itemValue);
                logBuf.append(" ");
                MemoryConstant.setConfigEnhancedRccSize(enhancedRccSize);
                return true;
            } else if (c != 3) {
                return false;
            } else {
                saveBigMemEnableDmeSwitch(itemValue);
                return true;
            }
        } catch (NumberFormatException e) {
            AwareLog.d(TAG, "parse interval failure cause :NumberFormatException");
            return false;
        }
    }

    private void setProcessLimit(int max) {
        HwActivityManagerService hwActivityManagerService;
        if (max >= 4 && max <= 512 && (hwActivityManagerService = this.mHwAms) != null) {
            hwActivityManagerService.setProcessLimit(max);
        }
    }

    private void setEmptyProcessPercent(int percent) {
        try {
            SystemPropertiesEx.set("sys.iaware.empty_app_percent", Integer.toString(percent));
        } catch (IllegalArgumentException e) {
            AwareLog.i(TAG, "setEmptyProcessPercent IllegalArgumentException! ");
        }
    }

    private void loadPinFilesItem(AwareConfig.Item item, int size) {
        if (size > 20) {
            AwareLog.i(TAG, "loadPinFilesItem too long size=" + size);
            return;
        }
        int curIndex = 0;
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            if (curIndex >= size) {
                int curIndex2 = size + 1;
                return;
            } else if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemValue)) {
                    AwareLog.i(TAG, "loadPinFilesItem null item");
                    return;
                }
                char c = 65535;
                if (itemName.hashCode() == 3143036 && itemName.equals("file")) {
                    c = 0;
                }
                if (c != 0) {
                    AwareLog.w(TAG, "loadPinFilesItem no such configuration:" + itemName);
                    curIndex = size + 1;
                } else {
                    MemoryConstant.addPinnedFilesStr(itemValue.trim());
                    curIndex++;
                }
            }
        }
    }

    private void loadSysTrimConfig(boolean isCust) {
        AwareLog.d(TAG, "loadSysMemConfig begin, isCust=" + isCust);
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_SYSTRIM, isCust);
        if (configList == null) {
            AwareLog.w(TAG, "loadSysMemConfig failure cause null configList");
            return;
        }
        List<AwareConfig.Item> itemList = configList.getConfigList();
        if (itemList == null) {
            AwareLog.w(TAG, "loadSysMemConfig failure cause null itemList");
            return;
        }
        MemoryUtils.initialRamSizeLowerBound();
        Iterator<AwareConfig.Item> it = itemList.iterator();
        while (true) {
            if (it.hasNext()) {
                AwareConfig.Item item = it.next();
                if (item != null && item.getProperties() != null) {
                    Map<String, String> configProperties = item.getProperties();
                    String ramSize = configProperties.get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME);
                    if (MemoryUtils.checkRamSize(ramSize)) {
                        AwareLog.i(TAG, "loadSysMemConfig success. ramSize: " + ramSize);
                        String interval = configProperties.get(MemoryConstant.MEM_SYSAPPREPAIR_TRIGGER_INTERVAL);
                        if (configSystemAppMemRepairInterval(interval)) {
                            parseSysMemItem(item);
                            break;
                        }
                        AwareLog.d(TAG, "config SystemAppMemRepair interval failure! interval = " + interval);
                    } else {
                        AwareLog.d(TAG, "checkRamSize failure cause ramSize: " + ramSize);
                    }
                } else {
                    AwareLog.w(TAG, "loadSysMemConfig continue cause null item");
                }
            } else {
                break;
            }
        }
        AwareLog.d(TAG, "loadSysMemConfig end");
    }

    private boolean configSystemAppMemRepairInterval(String interval) {
        if (interval == null) {
            return false;
        }
        try {
            SystemAppMemRepairMng.getInstance().setTriggerInterval(Long.parseLong(interval.trim()));
            return true;
        } catch (NumberFormatException e) {
            AwareLog.d(TAG, "parse interval failure cause :NumberFormatException");
            return false;
        }
    }

    private void parseSysMemItem(AwareConfig.Item item) {
        if (!isConfigEnable(item)) {
            SystemAppMemRepairMng.getInstance().disable();
            AwareLog.w(TAG, "loadSysMemConfig switch is off");
            return;
        }
        SystemAppMemRepairMng.getInstance().enable();
        List<AwareConfig.SubItem> subItems = item.getSubItemList();
        if (subItems == null) {
            AwareLog.w(TAG, "loadSysMemConfig continue cause null subitem");
            return;
        }
        for (AwareConfig.SubItem subItem : subItems) {
            if (subItem == null || subItem.getProperties() == null) {
                AwareLog.d(TAG, "loadSysMemConfig properties is null");
            } else {
                Map<String, String> properties = subItem.getProperties();
                String packageName = properties.get("packageName");
                String threshold = properties.get("threshold");
                String policy = properties.get(MemoryConstant.MEM_SYSTRIM_POLICY);
                String scene = properties.get("scene");
                if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(threshold)) {
                    updateSystemMemConfig(packageName, threshold, policy, scene);
                }
            }
        }
    }

    private boolean isConfigEnable(AwareConfig.Item item) {
        int switchValue;
        String value = item.getProperties().get("switch");
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        try {
            switchValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            switchValue = 0;
            AwareLog.i(TAG, "switch config is wrong");
        }
        if (switchValue == 1) {
            return true;
        }
        return false;
    }

    private void updateSystemMemConfig(String pkgName, String threshold, String policy, String scene) {
        try {
            long thres = Long.parseLong(threshold) * 1024;
            if (thres <= 0) {
                AwareLog.w(TAG, "loadSysMemConfig continue cause, the threshhold is less than 0");
            } else if (TextUtils.isEmpty(policy)) {
                SystemAppMemRepairMng.getInstance().configSystemAppThreshold(pkgName, thres, SystemAppMemRepairMng.Policy.TRIM.ordinal(), 0);
            } else if (!isValid(policy)) {
                AwareLog.i(TAG, "loadSysMemConfig continue cause subitem policy is invaild");
            } else {
                if (policy.contains("0")) {
                    SystemAppMemRepairMng.getInstance().configSystemAppThreshold(pkgName, thres, SystemAppMemRepairMng.Policy.TRIM.ordinal(), 0);
                }
                if (policy.contains("1")) {
                    try {
                        SystemAppMemRepairMng.getInstance().configSystemAppThreshold(pkgName, thres, SystemAppMemRepairMng.Policy.KILL.ordinal(), Integer.parseInt(scene));
                    } catch (NumberFormatException e) {
                        AwareLog.i(TAG, "loadSysMemConfig continue cause subitem scene is not long");
                    }
                }
            }
        } catch (NumberFormatException e2) {
            AwareLog.w(TAG, "loadSysMemConfig continue cause subitem threshhold is not long");
        }
    }

    private boolean isValid(String policy) {
        String[] policys = policy.split(",");
        if (policys.length > 2 || policys.length <= 0) {
            AwareLog.i(TAG, "loadSysMemConfig continue cause subitem policy is invaild. policy=" + policy);
            return false;
        }
        for (String p : policys) {
            if (TextUtils.isEmpty(p)) {
                AwareLog.i(TAG, "loadSysMemConfig continue cause subitem policy is invaild.. policy=" + policy);
                return false;
            }
            try {
                int configPolicy = Integer.parseInt(p);
                if (configPolicy != 0 && configPolicy != 1) {
                    AwareLog.i(TAG, "loadSysMemConfig continue cause subitem policy is out of scope");
                    return false;
                }
            } catch (NumberFormatException e) {
                AwareLog.i(TAG, "loadSysMemConfig continue cause subitem policy is not int");
                return false;
            }
        }
        return true;
    }

    private void loadNativeAppThresholdConfig(boolean isCust) {
        AwareLog.d(TAG, "loadNativeAppThresholdConfig begin");
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_NATIVE_MNG, isCust);
        if (configList == null) {
            AwareLog.w(TAG, "loadNativeAppThresholdConfig failure cause null configList");
            return;
        }
        List<AwareConfig.Item> itemList = configList.getConfigList();
        if (itemList == null) {
            AwareLog.w(TAG, "loadNativeAppThresholdConfig failure cause null itemList");
            return;
        }
        MemoryUtils.initialRamSizeLowerBound();
        Iterator<AwareConfig.Item> it = itemList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            AwareConfig.Item item = it.next();
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadNativeAppThresholdConfig continue cause null item");
            } else {
                String ramSize = item.getProperties().get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME);
                if (!MemoryUtils.checkRamSize(ramSize)) {
                    AwareLog.d(TAG, "checkRamSize failure cause ramSize: " + ramSize);
                } else {
                    AwareLog.i(TAG, "loadNativeAppThresholdConfig success. ramSize: " + ramSize);
                    if (!isConfigEnable(item)) {
                        AwareLog.w(TAG, "loadNativeAppThresholdConfig switch off");
                        NativeAppMemRepair.getInstance().disable();
                        return;
                    }
                    NativeAppMemRepair.getInstance().enable();
                    parseNativeMemItem(item);
                }
            }
        }
        AwareLog.d(TAG, "loadNativeAppThresholdConfig end");
    }

    private void parseNativeMemItem(AwareConfig.Item item) {
        List<AwareConfig.SubItem> subItems = item.getSubItemList();
        if (subItems == null) {
            AwareLog.w(TAG, "loadNativeAppThresholdConfig continue cause null subitem");
            return;
        }
        for (AwareConfig.SubItem subItem : subItems) {
            if (subItem == null || subItem.getProperties() == null) {
                AwareLog.w(TAG, "loadNativeAppThresholdConfig continue cause subitem is null");
            } else {
                Map<String, String> properties = subItem.getProperties();
                String procesName = properties.get(MemoryConstant.MEM_NATIVE_ITEM_PROCESSNAME);
                String threshold = properties.get("threshold");
                if (!TextUtils.isEmpty(procesName) && !TextUtils.isEmpty(threshold)) {
                    try {
                        long thres = Long.parseLong(threshold) * 1024;
                        if (thres <= 0) {
                            AwareLog.w(TAG, "loadNativeAppThresholdConfig continue cause, the threshhold is less than 0");
                        } else {
                            NativeAppMemRepair.getInstance().updateConfigNativeThreshold(procesName, thres);
                        }
                    } catch (NumberFormatException e) {
                        AwareLog.w(TAG, "loadNativeAppThresholdConfig continue cause subitem threshhold is not long");
                    }
                }
            }
        }
    }

    private void loadPinFileConfig() {
        AwareLog.d(TAG, "loadPinFileConfig begin");
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_PIN_FILE, false);
        if (configList == null) {
            AwareLog.w(TAG, "loadPinFileConfig failure cause null configList");
            return;
        }
        List<AwareConfig.Item> itemList = configList.getConfigList();
        if (itemList == null) {
            AwareLog.w(TAG, "loadPinFileConfig failure cause null itemList");
            return;
        }
        MemoryUtils.initialRamSizeLowerBound();
        for (AwareConfig.Item item : itemList) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadPinFileConfig continue cause null item");
            } else {
                Map<String, String> configProperties = item.getProperties();
                String strName = configProperties.get("name");
                if (TextUtils.isEmpty(strName)) {
                    AwareLog.w(TAG, "loadPinFileConfig failure null item name");
                } else {
                    String ramSize = configProperties.get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME);
                    if (!MemoryUtils.checkRamSize(ramSize)) {
                        AwareLog.d(TAG, "checkRamSize failure ramSize");
                    } else {
                        AwareLog.d(TAG, "loadPinFile success: ramsize: " + ramSize);
                        int size = getSize(configProperties.get("size"));
                        if (size < 1) {
                            AwareLog.w(TAG, "loadPinFileConfig continue failure size: " + size);
                        } else if (item.getSubItemList() == null) {
                            AwareLog.w(TAG, "loadPinFileConfig continue cause null subitem");
                        } else if (strName.equals(MemoryConstant.MEM_PIN_FILE_NAME)) {
                            loadPinFilesItem(item, size);
                        }
                    }
                }
            }
        }
        AwareLog.d(TAG, "loadPinFileConfig end");
    }

    private void loadMemRepairConfig() {
        AwareLog.d(TAG, "loadMemRepairConfig begin");
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_REPAIR, true);
        if (configList == null) {
            AwareLog.w(TAG, "loadMemRepairConfig failure cause null configList");
            return;
        }
        List<AwareConfig.Item> itemList = configList.getConfigList();
        if (itemList == null) {
            AwareLog.w(TAG, "loadMemRepairConfig failure cause null itemList");
            return;
        }
        MemInfoReaderExt info = new MemInfoReaderExt();
        info.readMemInfo();
        Long totalMemMb = Long.valueOf(info.getTotalSize() / MemoryConstant.MB_SIZE);
        MemoryUtils.initialRamSizeLowerBound();
        for (AwareConfig.Item item : itemList) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadMemRepairConfig continue cause null item");
            } else {
                Map<String, String> configProperties = item.getProperties();
                String strName = configProperties.get("name");
                if (TextUtils.isEmpty(strName)) {
                    AwareLog.w(TAG, "loadMemRepairConfig failure null item name");
                } else {
                    int size = getSize(configProperties.get("size"));
                    if (size < 1) {
                        AwareLog.w(TAG, "loadMemRepairConfig continue failure size: " + size);
                    } else if (item.getSubItemList() == null) {
                        AwareLog.w(TAG, "loadMemRepairConfig continue cause null subitem");
                    } else {
                        loadMemRepairVss(strName, item);
                        if (strName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BASE)) {
                            loadMemRepairConstantItem(item, size);
                        } else {
                            loadMemRepairEmergThres(configProperties, totalMemMb, item, size);
                        }
                    }
                }
            }
        }
        AwareLog.d(TAG, "loadMemRepairConfig end");
    }

    private void loadMemRepairEmergThres(Map<String, String> configProperties, Long totalMemMb, AwareConfig.Item item, int size) {
        String ramSize = configProperties.get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME);
        String strName = configProperties.get("name");
        if (!MemoryUtils.checkRamSize(ramSize)) {
            AwareLog.d(TAG, "checkRamSize failure ramSize: " + ramSize);
            return;
        }
        AwareLog.d(TAG, "loadMemRepairEmergThres success: ramsize: " + ramSize);
        if (strName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_MIN_MAX_THRES)) {
            loadMemRepairMinMaxThresItem(item, size);
        } else if (MemoryConstant.MEM_REPAIR_CONSTANT_PROC_EMERG_THRES.equals(strName)) {
            loadMemRepairProcThresItem(totalMemMb.longValue(), item);
        }
    }

    private void loadMemRepairIonConfig() {
        AwareLog.d(TAG, "loadMemRepairIonConfig begin");
        AwareConfig memRepairIonConfig = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_REPAIR_ION, true);
        if (memRepairIonConfig == null) {
            AwareLog.w(TAG, "loadMemRepairIonConfig failure cause null configList");
            return;
        }
        List<AwareConfig.Item> itemList = memRepairIonConfig.getConfigList();
        if (itemList == null) {
            AwareLog.w(TAG, "loadMemRepairIonConfig failure cause null itemList");
            return;
        }
        MemoryUtils.initialRamSizeLowerBound();
        for (AwareConfig.Item item : itemList) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadMemRepairIonConfig continue cause null item");
            } else {
                String strName = item.getProperties().get("name");
                if (TextUtils.isEmpty(strName)) {
                    AwareLog.w(TAG, "loadMemRepairIonConfig memInfoReader failure null item name");
                } else {
                    loadMemRepairIonItem(strName, item);
                }
            }
        }
        if (!MemRepairPolicy.getInstance().checkIonConfigParas()) {
            MemRepairPolicy.getInstance().updateIonSwitch(0);
        }
    }

    private void loadMemRepairIonItem(String strName, AwareConfig.Item item) {
        List<AwareConfig.SubItem> subItems = item.getSubItemList();
        if (subItems != null) {
            String ramSize = (String) item.getProperties().get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME);
            if (!strName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_ION_PROC_EMERG_THRES) || MemoryUtils.checkRamSize(ramSize)) {
                AwareLog.d(TAG, "loadMemRepairIonItem success: ramSize: " + ramSize);
                char c = 65535;
                int hashCode = strName.hashCode();
                if (hashCode != -410283503) {
                    if (hashCode == 1836671275 && strName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_ION_PROC_EMERG_THRES)) {
                        c = 0;
                    }
                } else if (strName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_ION_BASE)) {
                    c = 1;
                }
                if (c == 0) {
                    loadMemRepairIonThreshold(subItems);
                } else if (c == 1) {
                    loadMemRepairIonBaseConfig(subItems);
                }
            } else {
                AwareLog.d(TAG, "checkRamSize failure ramSize: " + ramSize);
            }
        }
    }

    private void loadMemRepairIonBaseConfig(List<AwareConfig.SubItem> subItems) {
        AwareConfig.SubItem subItem;
        Iterator<AwareConfig.SubItem> it = subItems.iterator();
        while (it.hasNext() && (subItem = it.next()) != null) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemValue)) {
                AwareLog.w(TAG, "loadMemRepairIonBaseConfig null item");
                return;
            }
            try {
                int itemValueTemp = Integer.parseInt(itemValue);
                char c = 65535;
                if (itemName.hashCode() == -889473228 && itemName.equals("switch")) {
                    c = 0;
                }
                if (c == 0) {
                    MemRepairPolicy.getInstance().updateIonSwitch(itemValueTemp);
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "loadMemRepairIonBaseConfig param error");
                return;
            }
        }
    }

    private void loadMemRepairIonThreshold(List<AwareConfig.SubItem> subItems) {
        AwareConfig.SubItem subItem;
        Iterator<AwareConfig.SubItem> it = subItems.iterator();
        while (it.hasNext() && (subItem = it.next()) != null) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemValue)) {
                AwareLog.i(TAG, "loadMemRepairProcThresItem null item");
                return;
            } else if (itemName.equals("threshold")) {
                try {
                    MemRepairPolicy.getInstance().updateIonThreshold(Integer.parseInt(itemValue));
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "loadMemRepairIonProcThresItem param error");
                    return;
                }
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void loadMemRepairConstantItem(AwareConfig.Item item, int size) {
        char c;
        int index = 0;
        int[] constValues = {0, 0, 0, 0, 0};
        boolean loadSucc = true;
        StringBuffer logbuf = new StringBuffer();
        Iterator it = item.getSubItemList().iterator();
        while (true) {
            if (it.hasNext()) {
                AwareConfig.SubItem subItem = (AwareConfig.SubItem) it.next();
                if (index >= size) {
                    index = size + 1;
                } else if (subItem != null) {
                    String itemName = subItem.getName();
                    String itemValue = subItem.getValue();
                    if (!TextUtils.isEmpty(itemName)) {
                        if (!TextUtils.isEmpty(itemValue)) {
                            switch (itemName.hashCode()) {
                                case -1602877080:
                                    if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_MIN_COUNT)) {
                                        c = 1;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -1575767891:
                                    if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_DV_NEGA_PERCENT)) {
                                        c = 4;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -1086546204:
                                    if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG_MIN_COUNT)) {
                                        c = 0;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case -1005530493:
                                    if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG_INTERVAL)) {
                                        c = 2;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                case 1887307647:
                                    if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_INTERVAL)) {
                                        c = 3;
                                        break;
                                    }
                                    c = 65535;
                                    break;
                                default:
                                    c = 65535;
                                    break;
                            }
                            if (c == 0 || c == 1 || c == 2 || c == 3 || c == 4) {
                                logbuf.append(itemName + ":" + itemValue + " ");
                                loadSucc = saveMemRepairConstantItem(itemName, itemValue.trim(), constValues);
                                index = loadSucc ? index + 1 : size + 1;
                            } else {
                                index = size + 1;
                            }
                        }
                    }
                }
            }
        }
        AwareLog.d(TAG, logbuf.toString());
        if (index == size && loadSucc) {
            ProcStateStatisData.getInstance().updateConfig(constValues[0], constValues[1], ((long) constValues[2]) * AppHibernateCst.DELAY_ONE_MINS, ((long) constValues[3]) * AppHibernateCst.DELAY_ONE_MINS);
            MemRepairPolicy.getInstance().updateCollectCount(constValues[0], constValues[1]);
            MemRepairPolicy.getInstance().updateDvalueFloatPercent(constValues[4]);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003c, code lost:
        if (r17.equals(com.android.server.rms.iaware.memory.utils.MemoryConstant.MEM_REPAIR_CONSTANT_FG_MIN_COUNT) != false) goto L_0x0054;
     */
    private boolean saveMemRepairConstantItem(String itemName, String itemValue, int[] constValues) {
        boolean z = false;
        if (constValues == null || constValues.length != 5) {
            return false;
        }
        switch (itemName.hashCode()) {
            case -1602877080:
                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_MIN_COUNT)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -1575767891:
                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_DV_NEGA_PERCENT)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -1086546204:
                break;
            case -1005530493:
                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG_INTERVAL)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 1887307647:
                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_INTERVAL)) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            return parseConstMinCount(itemValue.trim(), constValues, 0);
        }
        if (z) {
            return parseConstMinCount(itemValue.trim(), constValues, 1);
        }
        if (z) {
            return parseConstInterval(itemValue.trim(), constValues, 2);
        }
        if (z) {
            return parseConstInterval(itemValue.trim(), constValues, 3);
        }
        if (!z) {
            return false;
        }
        return parseConstDvPercent(itemValue.trim(), constValues, 4);
    }

    private boolean parseConstMinCount(String itemValue, int[] constValues, int index) {
        try {
            int minCount = Integer.parseInt(itemValue.trim());
            if (minCount >= 6) {
                if (minCount <= 50) {
                    constValues[index] = minCount;
                    return true;
                }
            }
            AwareLog.i(TAG, "error minCount:" + minCount);
            return false;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
            return false;
        }
    }

    private boolean parseConstInterval(String itemValue, int[] constValues, int index) {
        try {
            int interval = Integer.parseInt(itemValue.trim());
            if (interval >= 2) {
                if (interval <= 30) {
                    constValues[index] = interval;
                    return true;
                }
            }
            AwareLog.i(TAG, "error interval:" + interval);
            return false;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
            return false;
        }
    }

    private boolean parseConstDvPercent(String itemValue, int[] constValues, int index) {
        try {
            int percent = Integer.parseInt(itemValue.trim());
            if (percent >= 1) {
                if (percent <= 30) {
                    constValues[index] = percent;
                    return true;
                }
            }
            AwareLog.i(TAG, "error percent:" + percent);
            return false;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x009a, code lost:
        android.rms.iaware.AwareLog.i(com.android.server.rms.iaware.feature.MemoryFeatureEx.TAG, "loadMemRepairMinMaxThresItem null item");
     */
    private void loadMemRepairMinMaxThresItem(AwareConfig.Item item, int size) {
        if (size > 20) {
            AwareLog.i(TAG, "loadMemRepairMinMaxThresItem too long size=" + size);
            return;
        }
        long[][] memThresHolds = (long[][]) Array.newInstance(long.class, size, 3);
        int index = 0;
        Iterator it = item.getSubItemList().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            AwareConfig.SubItem subItem = (AwareConfig.SubItem) it.next();
            if (index >= size) {
                index = size + 1;
                break;
            } else if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemValue)) {
                    break;
                }
                char c = 65535;
                if (itemName.hashCode() == -1545477013 && itemName.equals("threshold")) {
                    c = 0;
                }
                if (c != 0) {
                    AwareLog.w(TAG, "loadMemRepairMinMaxThresItem no such configuration:" + itemName);
                    index = size + 1;
                } else if (!parseMinMaxThres(itemValue.trim(), memThresHolds[index])) {
                    index = size + 1;
                } else {
                    index++;
                }
            }
        }
        if (index == size) {
            MemRepairPolicy.getInstance().updateFloatThresHold(memThresHolds);
        }
    }

    private boolean parseMinMaxThres(String itemValue, long[] memThresHolds) {
        try {
            String[] sets = itemValue.split(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            if (sets.length != 3) {
                AwareLog.i(TAG, "error split item");
                return false;
            }
            long minFloatThres = Long.parseLong(sets[0].trim());
            long maxFloatThres = Long.parseLong(sets[1].trim());
            long percentage = Long.parseLong(sets[2].trim());
            if (minFloatThres < 0 || maxFloatThres < 0 || minFloatThres >= maxFloatThres || percentage < 1) {
                AwareLog.i(TAG, "error minthres:" + minFloatThres + ",maxthres:" + maxFloatThres + ",percent:" + percentage);
                return false;
            }
            memThresHolds[0] = minFloatThres * 1024;
            memThresHolds[1] = 1024 * maxFloatThres;
            memThresHolds[2] = percentage;
            AwareLog.i(TAG, "mem minthres:" + minFloatThres + ",maxthres:" + maxFloatThres + ",percent:" + percentage);
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
            return false;
        }
    }

    private void loadMemRepairVss(String strName, AwareConfig.Item item) {
        if (MemoryConstant.MEM_REPAIR_CONSTANT_VSS_PARAMETER.equals(strName)) {
            loadMemRepairVssParameterItem(item);
        } else if (MemoryConstant.MEM_REPAIR_CONSTANT_VSS_EMERG_THRES.equals(strName)) {
            loadMemRepairVssThresItem(item);
        } else {
            AwareLog.w(TAG, "loadMemRepairVss item name invalid!");
        }
    }

    private void loadMemRepairVssParameterItem(AwareConfig.Item item) {
        if (!isItemValid(item, MemoryConstant.MEM_REPAIR_CONSTANT_PARAS)) {
            AwareLog.w(TAG, "loadMemRepairMinMaxThresItem item invalid!");
            return;
        }
        String[] blocks = ((AwareConfig.SubItem) item.getSubItemList().get(0)).getValue().split(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        if (blocks.length == 5) {
            long[] values = parseLongArray(blocks);
            AwareLog.i(TAG, "loadMemRepairVssParameterItem: " + Arrays.toString(values));
            MemRepairPolicy.getInstance().updateVssParameters(values);
        }
    }

    private void loadMemRepairVssThresItem(AwareConfig.Item item) {
        if (!isItemValid(item, "threshold")) {
            AwareLog.w(TAG, "loadMemRepairVssThresItem item invalid!");
            return;
        }
        long threshold = parseLong(((AwareConfig.SubItem) item.getSubItemList().get(0)).getValue());
        AwareLog.i(TAG, "loadMemRepairVssThresItem: " + threshold);
        MemRepairPolicy.getInstance().updateVssThreshold(threshold);
    }

    private boolean isItemValid(AwareConfig.Item item, String exceptTag) {
        List<AwareConfig.SubItem> subItems = item.getSubItemList();
        if (subItems.size() != 1) {
            return false;
        }
        AwareConfig.SubItem subItem = subItems.get(0);
        return subItem != null && !TextUtils.isEmpty(subItem.getName()) && !TextUtils.isEmpty(subItem.getValue()) && subItem.getName().equals(exceptTag);
    }

    private long[] parseLongArray(String[] blocks) {
        long[] values = new long[blocks.length];
        for (int i = 0; i < blocks.length; i++) {
            values[i] = parseLong(blocks[i]);
        }
        return values;
    }

    private long parseLong(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse long error");
            return -1;
        }
    }

    private void loadMemRepairProcThresItem(long totalMemMb, AwareConfig.Item item) {
        int itemSize = item.getSubItemList().size();
        if (itemSize == 2 || itemSize == 4) {
            long[][] thresholds = (long[][]) Array.newInstance(long.class, 1, 2);
            long[][] lowMemThresholds = (long[][]) Array.newInstance(long.class, 1, 2);
            for (AwareConfig.SubItem subItem : item.getSubItemList()) {
                if (subItem != null) {
                    String itemName = subItem.getName();
                    String itemValue = subItem.getValue();
                    if (!checkSubItem(itemName, itemValue)) {
                        MemRepairPolicy.getInstance().updateProcThresHold(thresholds, lowMemThresholds);
                        return;
                    }
                    char c = 65535;
                    switch (itemName.hashCode()) {
                        case -1332194002:
                            if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG)) {
                                c = 2;
                                break;
                            }
                            break;
                        case 918485508:
                            if (itemName.equals(MemoryConstant.MEM_REPAIR_LOWMEM_FOREGROUND)) {
                                c = 1;
                                break;
                            }
                            break;
                        case 1896801775:
                            if (itemName.equals(MemoryConstant.MEM_REPAIR_LOWMEM_BACKGROUND)) {
                                c = 3;
                                break;
                            }
                            break;
                        case 1984457027:
                            if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG)) {
                                c = 0;
                                break;
                            }
                            break;
                    }
                    if (c == 0) {
                        parseProcThres(itemName, itemValue.trim(), thresholds[0], 0, totalMemMb);
                    } else if (c == 1) {
                        parseProcThres(itemName, itemValue.trim(), lowMemThresholds[0], 0, totalMemMb);
                    } else if (c == 2) {
                        parseProcThres(itemName, itemValue.trim(), thresholds[0], 1, totalMemMb);
                    } else if (c != 3) {
                        AwareLog.w(TAG, "loadMemRepairProcThresItem no such configuration:" + itemName);
                    } else {
                        parseProcThres(itemName, itemValue.trim(), lowMemThresholds[0], 1, totalMemMb);
                    }
                }
            }
            MemRepairPolicy.getInstance().updateProcThresHold(thresholds, lowMemThresholds);
            return;
        }
        AwareLog.w(TAG, "MemRepairProcThresItem size error!");
    }

    private boolean checkSubItem(String itemName, String itemValue) {
        if (!TextUtils.isEmpty(itemName) && !TextUtils.isEmpty(itemValue)) {
            return true;
        }
        AwareLog.i(TAG, "loadMemRepairProcThresItem null item");
        return false;
    }

    private void loadMemRepirSumPssSwitchConfig() {
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_REPAIR_SUMPSS_CONFIGNAME, true);
        if (configList == null) {
            AwareLog.w(TAG, "loadMemRepirSumPssSwitchConfig failure, configList is empty");
            return;
        }
        List<AwareConfig.Item> items = configList.getConfigList();
        if (items == null) {
            AwareLog.w(TAG, "loadMemRepirSumPssSwitchConfig failure, configList is null");
        } else if (items.size() != 1) {
            AwareLog.w(TAG, "loadMemRepirSumPssSwitchConfig failure, configList size is not 1");
        } else {
            AwareConfig.Item item = items.get(0);
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadMemRepirSumPssSwitchConfig failure, item or configProperties is empty");
                return;
            }
            try {
                MemoryConstant.setConfigMemRepairBySumPssSwitch(Integer.parseInt(item.getProperties().get(MemoryConstant.MEM_REPAIR_SUMPSS_CONFIG_SWITCH)));
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "loadMemRepirSumPssSwitchConfig Number Format Error !");
            }
        }
    }

    private void parseProcThres(String itemName, String itemValue, long[] thresholds, int index, long totalMemMb) {
        try {
            long thres = Long.parseLong(itemValue.trim());
            if (thres >= 1) {
                if (thres < totalMemMb) {
                    thresholds[index] = 1024 * thres;
                    AwareLog.i(TAG, "MemRepairConfig: " + itemName + ":" + thres);
                    return;
                }
            }
            AwareLog.i(TAG, "error process threshold:" + thres);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
        }
    }

    private int getSize(String strSize) {
        if (strSize == null) {
            return 0;
        }
        try {
            int size = Integer.parseInt(strSize.trim());
            if (size >= 1) {
                return size;
            }
            AwareLog.w(TAG, "loadMemRepairMinMaxThresItem error size:" + size);
            return 0;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse size error");
            return 0;
        }
    }

    private void readMemoryApiWhiteListUid() {
        Context mcontext;
        PackageManager pm;
        MultiTaskManagerService mMtmService = MultiTaskManagerService.self();
        if (mMtmService != null && (mcontext = mMtmService.context()) != null && (pm = mcontext.getPackageManager()) != null) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo("com.huawei.camera", 1);
                if (appInfo != null && appInfo.uid > 0) {
                    MemoryConstant.setSysCameraUid(appInfo.uid);
                }
            } catch (PackageManager.NameNotFoundException e) {
                AwareLog.e(TAG, "can not get uid");
            }
        }
    }

    private void loadKillFreqConfig() {
        boolean z = true;
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_FREQKILL_CONFIGNAME, true);
        if (configList == null) {
            AwareLog.w(TAG, "loadKillFreqConfig failure, configList is empty");
            return;
        }
        List<AwareConfig.Item> items = configList.getConfigList();
        if (items == null) {
            AwareLog.w(TAG, "loadKillFreqConfig failure, configList is null");
        } else if (items.size() != 1) {
            AwareLog.w(TAG, "loadKillFreqConfig failure, configList size is not 1");
        } else {
            AwareConfig.Item item = items.get(0);
            if (item == null) {
                AwareLog.w(TAG, "loadKillFreqConfig failure, item is empty");
                return;
            }
            Map<String, String> configProperties = item.getProperties();
            if (configProperties == null) {
                AwareLog.w(TAG, "loadKillFreqConfig failure, configProperties is empty");
                return;
            }
            try {
                int pkgFreqSwitch = Integer.parseInt(configProperties.get(MemoryConstant.MEM_FREQKILL_PKG));
                int killFreqLimit = Integer.parseInt(configProperties.get(MemoryConstant.MEM_FREQKILL_LIMIT));
                int scaleTimes = Integer.parseInt(configProperties.get(MemoryConstant.MEM_FREQKILL_SCALETIMES));
                PackageTracker instance = PackageTracker.getInstance();
                if (pkgFreqSwitch != 1) {
                    z = false;
                }
                if (!instance.updateKillThresHold(z, killFreqLimit, scaleTimes)) {
                    AwareLog.i(TAG, "loadKillFreqConfig error limit:" + killFreqLimit + ",scaleTimes:" + scaleTimes);
                    return;
                }
                AwareLog.d(TAG, "loadKillFreqConfig limit:" + killFreqLimit + ", scaleTimes:" + scaleTimes);
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "loadKillFreqConfig Number Format Error !");
            }
        }
    }

    private void loadScanModeOptSwitchConfig() {
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_SCAN_MODE_OPT_CONFIGNAME, true);
        if (configList == null) {
            AwareLog.i(TAG, "loadScanModeOptSwitchConfig failure, configList is empty");
            return;
        }
        List<AwareConfig.Item> items = configList.getConfigList();
        if (items == null) {
            AwareLog.w(TAG, "loadScanModeOptSwitchConfig failure, configList is null");
        } else if (items.size() != 1) {
            AwareLog.w(TAG, "loadScanModeOptSwitchConfig failure, configList size is not 1");
        } else {
            AwareConfig.Item item = items.get(0);
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadScanModeOptSwitchConfig failure, item or configPropertries is empty");
                return;
            }
            try {
                MemoryConstant.setConfigScanModeOptSwitch(Integer.parseInt(item.getProperties().get(MemoryConstant.MEM_SCAN_MODE_OPT_SWITCH_CONFIGNAME)));
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "loadScanModeOptSwitchConfig Number Format Error !");
            }
        }
    }
}
