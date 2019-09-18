package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwareCMSManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.internal.util.MemInfoReader;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.policy.SystemTrimPolicy;
import com.android.server.rms.iaware.memory.utils.BigDataStore;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryLockFile;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PrereadUtils;
import com.android.server.rms.memrepair.MemRepairPolicy;
import com.android.server.rms.memrepair.ProcStateStatisData;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryFeature2 extends RFeature {
    private static final int BASE_VERSION = 2;
    private static final int MAX_CACHED_APPS = SystemProperties.getInt("ro.sys.fw.bg_apps_limit", 32);
    private static final int MAX_CACHED_APP_SIZE = 512;
    private static final String MEMORY_BG_PROCS = "memory_bg_procs";
    private static final String MEMORY_COLD_WARM_LAUNCH = "memory_cold_warm_launch";
    private static final String MEMORY_MMONITOR = "memory_mmonitor";
    private static final int MIN_CACHED_APP_SIZE = 4;
    private static final String TAG = "MemFeature2.0";
    public static final AtomicBoolean isUpMemoryFeature = new AtomicBoolean(false);
    private int mEmptyProcessPercent;
    private HwActivityManagerService mHwAMS;
    private MemoryLockFile mLockFile;
    private int mNumProcessLimit;
    private PrereadUtils mPrereadUtils;
    private long timeStamp;

    public MemoryFeature2(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        this.mHwAMS = null;
        this.mEmptyProcessPercent = SystemProperties.getInt("ro.sys.fw.empty_app_percent", 50);
        this.mLockFile = new MemoryLockFile();
        this.timeStamp = 0;
        this.mHwAMS = HwActivityManagerService.self();
        this.mNumProcessLimit = MAX_CACHED_APPS;
        this.mPrereadUtils = PrereadUtils.getInstance();
        PrereadUtils.setContext(context);
        loadMemConfig();
        ProcStateStatisData.getInstance().setContext(context);
    }

    public boolean enable() {
        return false;
    }

    public boolean enableFeatureEx(int realVersion) {
        AwareLog.d(TAG, "enableFeatureEx realVersion=" + realVersion);
        if (realVersion < 2) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", baseVersion: " + 2);
            return false;
        }
        enableMemoryFeature2();
        if (realVersion >= 3) {
            enableMemoryFeature3();
        }
        if (realVersion >= 5) {
            enableMemoryFeature5();
        }
        return true;
    }

    public boolean disable() {
        isUpMemoryFeature.set(false);
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            MemoryUtils.writeMMonitorSwitch(0);
        }
        this.mLockFile.clearPinFile();
        MemoryUtils.destroySocket();
        PrereadUtils prereadUtils = this.mPrereadUtils;
        PrereadUtils.stop();
        ProcStateStatisData.getInstance().setEnable(false);
        setEmptyProcessPercent(-1);
        setProcessLimit(MAX_CACHED_APPS);
        SystemTrimPolicy.getInstance().disable();
        MemoryConstant.setIawareThirdSwitch(0);
        MemoryConstant.setIawareFifthSwitch(0);
        MemoryUtils.rccIdleCompressEnable(false);
        return true;
    }

    public boolean reportData(CollectData data) {
        return false;
    }

    public boolean configUpdate() {
        loadMemConfig();
        setProcessLimit(this.mNumProcessLimit);
        this.mLockFile.clearPinFile();
        this.mLockFile.iAwareAddPinFile();
        return true;
    }

    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        return null;
    }

    public String getDFTDataByVersion(int iawareVer, boolean forBeta, boolean clearData, boolean betaEncode) {
        AwareLog.i(TAG, "Memoryfeature2 getDFTDataByVersion. iawareVer: " + iawareVer + ", beta: " + forBeta + ", clear: " + clearData + ";betaEncode=" + betaEncode);
        if (!betaEncode || !isUpMemoryFeature.get() || iawareVer < 2 || AwareConstant.CURRENT_USER_TYPE != 3) {
            return null;
        }
        long timeMillis = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        String result = MemoryReader.getMmonitorData();
        AwareLog.d(TAG, "result=" + result);
        if (result != null) {
            builder.append("{");
            builder.append(createHeadMsg(MEMORY_MMONITOR, timeMillis));
            builder.append("\"data\":");
            builder.append(result);
            builder.append("}\n");
        }
        String result2 = BigDataStore.getInstance().getColdWarmLaunchData(clearData);
        if (result2 != null) {
            builder.append("{");
            builder.append(createHeadMsg(MEMORY_COLD_WARM_LAUNCH, timeMillis));
            builder.append(result2);
            builder.append("}\n");
        }
        builder.append("{");
        builder.append(createHeadMsg(MEMORY_BG_PROCS, timeMillis));
        builder.append(BigDataStore.getInstance().getBgAppData(clearData));
        builder.append("}");
        AwareLog.d(TAG, "Memoryfeature2 getBigDataByVersion. result: " + builder.toString());
        this.timeStamp = timeMillis;
        return builder.toString();
    }

    private void enableMemoryFeature2() {
        isUpMemoryFeature.set(true);
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            MemoryUtils.writeMMonitorSwitch(1);
        }
        this.mLockFile.iAwareAddPinFile();
        readMemoryAPIWhiteListUid();
        PrereadUtils prereadUtils = this.mPrereadUtils;
        PrereadUtils.start();
        ProcStateStatisData.getInstance().setEnable(true);
        setEmptyProcessPercent(this.mEmptyProcessPercent);
        setProcessLimit(this.mNumProcessLimit);
        SystemTrimPolicy.getInstance().enable();
    }

    private void enableMemoryFeature3() {
        MemoryConstant.setIawareThirdSwitch(1);
    }

    private void enableMemoryFeature5() {
        MemoryConstant.setIawareFifthSwitch(1);
        if (MemoryConstant.isKernCompressEnable()) {
            MemoryUtils.rccIdleCompressEnable(true);
            MemoryUtils.rccSetIdleThreshold(MemoryConstant.getKernCompressIdleThreshold());
            MemoryUtils.rccSetAvailTarget((int) (MemoryConstant.getIdleMemory() / 1024));
            MemoryUtils.rccSetAnonTarget(MemoryConstant.getKernCompressAnonTarget());
            MemoryUtils.rccSetSwapPercent(MemoryConstant.getKernCompressSwapPercent());
        }
    }

    private String createHeadMsg(String featureName, long endTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"feature\":\"");
        sb.append(featureName);
        sb.append("\",");
        sb.append("\"start\":");
        sb.append(this.timeStamp > 0 ? this.timeStamp : endTime);
        sb.append(",");
        sb.append("\"end\":");
        sb.append(endTime);
        sb.append(",");
        return sb.toString();
    }

    private void loadMemConfig() {
        loadMemConstantConfig(false);
        loadMemConstantConfig(true);
        loadPrereadConfig();
        loadCameraPreloadReclaimConfig();
        loadMemRepairConfig(false);
        loadMemRepairConfig(true);
        MemoryConstant.clearPinnedFilesStr();
        loadPinFileConfig();
        MemoryFeature.loadBigMemAppPolicyConfig(getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_BIGMEMAPP, true));
        MemoryFeature.loadBigMemAppPolicyConfig(getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_IONPROPERTYS, true));
        loadSysTrimConfig();
        loadNotificationConfig();
    }

    private void loadMemConstantConfig(boolean isCustConfig) {
        AwareLog.i(TAG, "isCustConfig : " + isCustConfig);
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_CONSTANT_CONFIGNAME, isCustConfig);
        if (configList == null) {
            AwareLog.w(TAG, "loadMemConstantConfig failure cause null configList");
            return;
        }
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        Long totalMemMb = Long.valueOf(minfo.getTotalSize() / MemoryConstant.MB_SIZE);
        Iterator it = configList.getConfigList().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            AwareConfig.Item item = (AwareConfig.Item) it.next();
            if (item != null && item.getProperties() != null) {
                String ramSize = item.getProperties().get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME);
                if (MemoryUtils.checkRamSize(ramSize, totalMemMb)) {
                    AwareLog.i(TAG, "loadMemConstantConfig success. ramSize: " + ramSize + " totalMemMb: " + totalMemMb);
                    saveMemConstantItem(item);
                    break;
                }
                AwareLog.d(TAG, "checkRamSize failure cause ramSize: " + ramSize + " totalMemMb: " + totalMemMb);
            } else {
                AwareLog.d(TAG, "loadMemConstantConfig continue cause null item");
            }
        }
    }

    public void loadPrereadConfig() {
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_PREREAD_CONFIGNAME, true);
        if (configList == null) {
            AwareLog.w(TAG, "loadPrereadConfig failure, configList is empty");
            return;
        }
        Map<String, ArrayList<String>> filePathMap = new ArrayMap<>();
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (item == null) {
                AwareLog.w(TAG, "loadPrereadConfig failure, item is empty");
            } else {
                Map<String, String> configPropertries = item.getProperties();
                if (configPropertries == null) {
                    AwareLog.w(TAG, "loadPrereadConfig failure, configPropertries is empty");
                } else {
                    try {
                        if (Integer.parseInt(configPropertries.get(MemoryConstant.MEM_PREREAD_SWITCH)) == 0) {
                            AwareLog.w(TAG, "prereadSwitch off");
                        } else {
                            String pkgName = configPropertries.get(MemoryConstant.MEM_PREREAD_ITEM_NAME);
                            if (pkgName != null) {
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
                    } catch (NumberFormatException e) {
                        AwareLog.e(TAG, "Number Format Error !");
                    }
                }
            }
        }
        MemoryConstant.setPrereadFileMap(filePathMap);
        this.mPrereadUtils.sendPrereadDataUpdateMsg();
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
                AwareLog.w(TAG, "CameraPreloadReclaimConfig failure, configPropertries is empty");
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
        if (1 == sw && delay < 10000 && delay > 0 && uss < 102400 && uss > 1024) {
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
                Map<String, String> configPropertries = item.getProperties();
                if (configPropertries == null) {
                    AwareLog.w(TAG, "loadNotificationConfig failure, configPropertries is empty");
                } else {
                    try {
                        MemoryConstant.setConfigNotificatinSwitch(Integer.parseInt(configPropertries.get(MemoryConstant.MEM_NOTIFICATION_SWITCH)));
                        MemoryConstant.setNotificationInterval(Long.parseLong(configPropertries.get("interval")));
                        return;
                    } catch (NumberFormatException e) {
                        AwareLog.e(TAG, "Number Format Error !");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public AwareConfig getConfig(String featureName, String configName, boolean isCustConfig) {
        AwareConfig configList = null;
        if (featureName == null || featureName.equals("") || configName == null || configName.equals("")) {
            AwareLog.i(TAG, "featureName or configName is null");
            return null;
        }
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                configList = isCustConfig ? IAwareCMSManager.getCustConfig(awareservice, featureName, configName) : IAwareCMSManager.getConfig(awareservice, featureName, configName);
            } else {
                AwareLog.i(TAG, "can not find service awareservice.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "MemoryFeature getConfig RemoteException");
        }
        return configList;
    }

    private void saveMemConstantItem(AwareConfig.Item item) {
        StringBuffer logbuf = new StringBuffer();
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                try {
                    switch (itemName.hashCode()) {
                        case -1919496163:
                            if (itemName.equals(MemoryConstant.MEM_CONSTANT_KILL_GAP)) {
                                c = 8;
                                break;
                            }
                            break;
                        case -1669721547:
                            if (itemName.equals(MemoryConstant.MEM_CONSTANT_PREREAD_ODEX)) {
                                c = 5;
                                break;
                            }
                            break;
                        case -1470603166:
                            if (itemName.equals(MemoryConstant.MEM_CONSTANT_SYSTEMTRIMWITCH)) {
                                c = 0;
                                break;
                            }
                            break;
                        case -1053226004:
                            if (itemName.equals(MemoryConstant.MEM_CONSTANT_CLEAN_ALL)) {
                                c = 7;
                                break;
                            }
                            break;
                        case -822986700:
                            if (itemName.equals(MemoryConstant.MEM_CONSTANT_API_MAX_REQUEST_MEM)) {
                                c = 6;
                                break;
                            }
                            break;
                        case -798160691:
                            if (itemName.equals(MemoryConstant.MEM_CONSTANT_GPUNAME)) {
                                c = 2;
                                break;
                            }
                            break;
                        case -533820387:
                            if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROCESSPERCENT)) {
                                c = 4;
                                break;
                            }
                            break;
                        case -470167246:
                            if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROCESSLIMIT)) {
                                c = 3;
                                break;
                            }
                            break;
                        case 53466486:
                            if (itemName.equals(MemoryConstant.MEM_CONSTANT_GMCSWITCH)) {
                                c = 1;
                                break;
                            }
                            break;
                    }
                    switch (c) {
                        case 0:
                            int systemTrimSwitch = Integer.parseInt(itemValue.trim());
                            logbuf.append(itemName + ":" + itemValue + " ");
                            MemoryConstant.setConfigSystemTrimSwitch(systemTrimSwitch);
                            break;
                        case 1:
                            int gmcSwitch = Integer.parseInt(itemValue.trim());
                            logbuf.append(itemName + ":" + itemValue + " ");
                            MemoryConstant.setConfigGmcSwitch(gmcSwitch);
                            break;
                        case 2:
                            long gpuMemoryLimit = Long.parseLong(itemValue.trim());
                            logbuf.append(itemName + ":" + itemValue + " ");
                            MemoryConstant.setGpuMemoryLimit(1024 * gpuMemoryLimit);
                            break;
                        case 3:
                            int numProcessLimit = Integer.parseInt(itemValue.trim());
                            logbuf.append(itemName + ":" + itemValue + " ");
                            this.mNumProcessLimit = SystemProperties.getInt("ro.sys.fw.bg_apps_limit", numProcessLimit);
                            break;
                        case 4:
                            int numProcessPercent = Integer.parseInt(itemValue.trim());
                            logbuf.append(itemName + ":" + itemValue + " ");
                            if (numProcessPercent >= 0 && numProcessPercent <= 100) {
                                this.mEmptyProcessPercent = SystemProperties.getInt("ro.sys.fw.empty_app_percent", numProcessPercent);
                                break;
                            }
                        case 5:
                            int preread_odex_switch = Integer.parseInt(itemValue.trim());
                            logbuf.append(itemName + ":" + itemValue + " ");
                            PrereadUtils.getInstance();
                            PrereadUtils.setPrereadOdexSwitch(preread_odex_switch);
                            break;
                        case 6:
                            long maxApiRequestMem = Long.parseLong(itemValue.trim());
                            logbuf.append(itemName + ":" + itemValue + " ");
                            MemoryConstant.setMaxAPIRequestMemory(maxApiRequestMem);
                            break;
                        case 7:
                            int cleanAllSwitch = Integer.parseInt(itemValue.trim());
                            logbuf.append(itemName + ":" + itemValue + " ");
                            MemoryConstant.setCleanAllSwitch(cleanAllSwitch);
                            break;
                        case 8:
                            long killMemGap = Long.parseLong(itemValue.trim());
                            logbuf.append(itemName + ":" + itemValue + " ");
                            MemoryConstant.setKillGapMemory(killMemGap);
                            break;
                        default:
                            setMemConfigValue(itemName, itemValue, logbuf);
                            break;
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "parse memory xml error");
                }
            }
        }
        AwareLog.d(TAG, logbuf.toString());
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private void setMemConfigValue(String itemName, String itemValue, StringBuffer logbuf) {
        char c;
        switch (itemName.hashCode()) {
            case -1695261643:
                if (itemName.equals(MemoryConstant.MEM_CONSTANT_KERNEL_ANON_TARGET)) {
                    c = 6;
                    break;
                }
            case -1418184189:
                if (itemName.equals(MemoryConstant.MEM_CONSTANT_FAST_QUICKKILL)) {
                    c = 2;
                    break;
                }
            case -1182948998:
                if (itemName.equals(MemoryConstant.MEM_CONSTANT_KERNEL_SWAP_PERCENT)) {
                    c = 7;
                    break;
                }
            case -714132116:
                if (itemName.equals(MemoryConstant.MEM_CONSTANT_MORE_PREVIOUS)) {
                    c = 3;
                    break;
                }
            case -522521793:
                if (itemName.equals(MemoryConstant.MEM_CONSTANT_KERNEL_IDLE_THRESHOLD)) {
                    c = 5;
                    break;
                }
            case 1050088593:
                if (itemName.equals(MemoryConstant.MEM_CONSTANT_EXACT_KILL)) {
                    c = 0;
                    break;
                }
            case 1198544876:
                if (itemName.equals(MemoryConstant.MEM_CONSTANT_KERNEL_COMPRESS_SWITCH)) {
                    c = 4;
                    break;
                }
            case 1686655406:
                if (itemName.equals(MemoryConstant.MEM_CONSTANT_FAST_KILL)) {
                    c = 1;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                int exactKillSwitch = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setExactKillSwitch(exactKillSwitch);
                return;
            case 1:
                int fastKillSwitch = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setFastKillSwitch(fastKillSwitch);
                return;
            case 2:
                int fastQuickKillSwitch = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setFastQuickKillSwitch(fastQuickKillSwitch);
                return;
            case 3:
                int morePreviousLevel = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                AwareAppAssociate.getInstance().setMorePreviousLevel(morePreviousLevel);
                return;
            case 4:
                int rccSwitch = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setConfigKernCompressSwitch(rccSwitch);
                return;
            case 5:
                int cpuPercent = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setKernCompressIdleThreshold(cpuPercent);
                return;
            case 6:
                int target = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setKernCompressAnonTarget(target);
                return;
            case 7:
                int percent = Integer.parseInt(itemValue.trim());
                logbuf.append(itemName + ":" + itemValue + " ");
                MemoryConstant.setKernCompressSwapPercent(percent);
                return;
            default:
                return;
        }
    }

    private void setProcessLimit(int max) {
        if (max >= 4 && max <= 512 && this.mHwAMS != null) {
            this.mHwAMS.setProcessLimit(max);
        }
    }

    private void setEmptyProcessPercent(int percent) {
        try {
            SystemProperties.set("sys.iaware.empty_app_percent", Integer.toString(percent));
        } catch (IllegalArgumentException e) {
            AwareLog.i(TAG, "setEmptyProcessPercent IllegalArgumentException! ");
        }
    }

    private void loadPinFilesItem(AwareConfig.Item item, int mrSize) {
        if (mrSize > 20) {
            AwareLog.i(TAG, "loadPinFilesItem too long mrSize=" + mrSize);
            return;
        }
        int curIndex = 0;
        Iterator it = item.getSubItemList().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            AwareConfig.SubItem subItem = (AwareConfig.SubItem) it.next();
            if (curIndex >= mrSize) {
                int curIndex2 = mrSize + 1;
                break;
            } else if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemValue)) {
                    AwareLog.i(TAG, "loadPinFilesItem null item");
                } else {
                    char c = 65535;
                    if (itemName.hashCode() == 3143036 && itemName.equals("file")) {
                        c = 0;
                    }
                    if (c != 0) {
                        AwareLog.w(TAG, "loadPinFilesItem no such configuration:" + itemName);
                        curIndex = mrSize + 1;
                    } else {
                        MemoryConstant.addPinnedFilesStr(itemValue.trim());
                        curIndex++;
                    }
                }
            }
        }
    }

    private void loadSysTrimConfig() {
        AwareLog.d(TAG, "loadSysTrimConfig begin");
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_SYSTRIM, false);
        if (configList == null) {
            AwareLog.w(TAG, "loadSysTrimConfig failure cause null configList");
            return;
        }
        List<AwareConfig.Item> itemList = configList.getConfigList();
        if (itemList == null) {
            AwareLog.w(TAG, "loadSysTrimConfig failure cause null itemList");
            return;
        }
        for (AwareConfig.Item item : itemList) {
            if (item == null) {
                AwareLog.w(TAG, "loadSysTrimConfig continue cause null item");
            } else {
                List<AwareConfig.SubItem> subItems = item.getSubItemList();
                if (subItems == null) {
                    AwareLog.w(TAG, "loadSysTrimConfig continue cause null subitem");
                } else {
                    for (AwareConfig.SubItem subItem : subItems) {
                        Map<String, String> properties = subItem.getProperties();
                        if (properties != null) {
                            String packageName = properties.get("packageName");
                            String threshold = properties.get("threshold");
                            if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(threshold)) {
                                try {
                                    long thres = Long.parseLong(threshold) * 1024;
                                    if (thres <= 0) {
                                        AwareLog.w(TAG, "loadSysTrimConfig continue cause, the threshhold is less than 0");
                                    } else {
                                        SystemTrimPolicy.getInstance().updateProcThreshold(packageName, thres);
                                    }
                                } catch (NumberFormatException e) {
                                    AwareLog.w(TAG, "loadSysTrimConfig continue cause subitem threshhold is not long");
                                }
                            }
                        }
                    }
                }
            }
        }
        AwareLog.d(TAG, "loadSysTrimConfig end");
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
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        Long totalMemMb = Long.valueOf(minfo.getTotalSize() / MemoryConstant.MB_SIZE);
        for (AwareConfig.Item item : itemList) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadPinFileConfig continue cause null item");
            } else {
                Map<String, String> configPropertries = item.getProperties();
                String strName = configPropertries.get("name");
                if (TextUtils.isEmpty(strName)) {
                    AwareLog.w(TAG, "loadPinFileConfig failure null item name");
                } else if (!MemoryUtils.checkRamSize(configPropertries.get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME), totalMemMb)) {
                    AwareLog.d(TAG, "checkRamSize failure ramSize, totalMemMb: " + totalMemMb);
                } else {
                    int size = getSize(configPropertries.get("size"));
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
        AwareLog.d(TAG, "loadPinFileConfig end");
    }

    private void loadMemRepairConfig(boolean isCustConfig) {
        AwareLog.d(TAG, "loadMemRepairConfig begin");
        boolean z = isCustConfig;
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_REPAIR, z);
        if (configList == null) {
            AwareLog.w(TAG, "loadMemRepairConfig failure cause null configList");
            return;
        }
        List<AwareConfig.Item> itemList = configList.getConfigList();
        if (itemList == null) {
            AwareLog.w(TAG, "loadMemRepairConfig failure cause null itemList");
            return;
        }
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        Long totalMemMb = Long.valueOf(minfo.getTotalSize() / MemoryConstant.MB_SIZE);
        Iterator<AwareConfig.Item> it = itemList.iterator();
        while (it.hasNext()) {
            String item = (AwareConfig.Item) it.next();
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadMemRepairConfig continue cause null item");
            } else {
                Map<String, String> configPropertries = item.getProperties();
                String strName = configPropertries.get("name");
                if (TextUtils.isEmpty(strName)) {
                    AwareLog.w(TAG, "loadMemRepairConfig failure null item name");
                } else {
                    String strSize = configPropertries.get("size");
                    int size = getSize(strSize);
                    if (size < 1) {
                        AwareLog.w(TAG, "loadMemRepairConfig continue failure size: " + size);
                    } else if (item.getSubItemList() == null) {
                        AwareLog.w(TAG, "loadMemRepairConfig continue cause null subitem");
                    } else {
                        loadMemRepairVss(strName, item);
                        if (strName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BASE)) {
                            loadMemRepairConstantItem(item, size);
                        } else {
                            String ramSize = configPropertries.get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME);
                            if (!MemoryUtils.checkRamSize(ramSize, totalMemMb)) {
                                AwareLog.d(TAG, "checkRamSize failure ramSize, totalMemMb: " + totalMemMb);
                            } else if (strName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_MIN_MAX_THRES)) {
                                loadMemRepairMinMaxThresItem(item, size);
                            } else if (strName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_PROC_EMERG_THRES)) {
                                String str = ramSize;
                                int i = size;
                                String str2 = strSize;
                                loadMemRepairProcThresItem(totalMemMb.longValue(), item, size, z);
                            }
                        }
                    }
                }
            }
        }
        AwareLog.d(TAG, "loadMemRepairConfig end");
    }

    private void loadMemRepairConstantItem(AwareConfig.Item item, int mrSize) {
        Iterator it;
        int i = mrSize;
        int index = 0;
        int[] constValues = {0, 0, 0, 0, 0};
        boolean loadSucc = true;
        StringBuffer logbuf = new StringBuffer();
        Iterator it2 = item.getSubItemList().iterator();
        while (true) {
            if (it2.hasNext()) {
                AwareConfig.SubItem subItem = (AwareConfig.SubItem) it2.next();
                if (index >= i) {
                    index = i + 1;
                } else if (subItem != null) {
                    String itemName = subItem.getName();
                    String itemValue = subItem.getValue();
                    if (!TextUtils.isEmpty(itemName) && !TextUtils.isEmpty(itemValue)) {
                        char c = 65535;
                        switch (itemName.hashCode()) {
                            case -1602877080:
                                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_MIN_COUNT)) {
                                    c = 1;
                                    break;
                                }
                                break;
                            case -1575767891:
                                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_DV_NEGA_PERCENT)) {
                                    c = 4;
                                    break;
                                }
                                break;
                            case -1086546204:
                                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG_MIN_COUNT)) {
                                    c = 0;
                                    break;
                                }
                                break;
                            case -1005530493:
                                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG_INTERVAL)) {
                                    c = 2;
                                    break;
                                }
                                break;
                            case 1887307647:
                                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_INTERVAL)) {
                                    c = 3;
                                    break;
                                }
                                break;
                        }
                        switch (c) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                                logbuf.append(itemName + ":" + itemValue + " ");
                                it = it2;
                                loadSucc = saveMemRepairConstantItem(itemName, itemValue.trim(), constValues);
                                index = loadSucc ? index + 1 : i + 1;
                                break;
                            default:
                                it = it2;
                                index = i + 1;
                                break;
                        }
                        it2 = it;
                    }
                }
            }
        }
        AwareLog.d(TAG, logbuf.toString());
        if (index != i) {
        } else if (!loadSucc) {
            int i2 = index;
        } else {
            int i3 = index;
            ProcStateStatisData.getInstance().updateConfig(constValues[0], constValues[1], ((long) constValues[2]) * AppHibernateCst.DELAY_ONE_MINS, ((long) constValues[3]) * AppHibernateCst.DELAY_ONE_MINS);
            MemRepairPolicy.getInstance().updateCollectCount(constValues[0], constValues[1]);
            MemRepairPolicy.getInstance().updateDValueFloatPercent(constValues[4]);
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0032, code lost:
        if (r10.equals(com.android.server.rms.iaware.memory.utils.MemoryConstant.MEM_REPAIR_CONSTANT_FG_MIN_COUNT) != false) goto L_0x004a;
     */
    private boolean saveMemRepairConstantItem(String itemName, String itemValue, int[] constValues) {
        boolean loadSucc;
        char c = 0;
        if (constValues == null || constValues.length != 5) {
            return false;
        }
        switch (itemName.hashCode()) {
            case -1602877080:
                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_MIN_COUNT)) {
                    c = 1;
                    break;
                }
            case -1575767891:
                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_DV_NEGA_PERCENT)) {
                    c = 4;
                    break;
                }
            case -1086546204:
                break;
            case -1005530493:
                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG_INTERVAL)) {
                    c = 2;
                    break;
                }
            case 1887307647:
                if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_INTERVAL)) {
                    c = 3;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                loadSucc = parseConstMinCount(itemValue.trim(), constValues, 0);
                break;
            case 1:
                loadSucc = parseConstMinCount(itemValue.trim(), constValues, 1);
                break;
            case 2:
                loadSucc = parseConstInterval(itemValue.trim(), constValues, 2);
                break;
            case 3:
                loadSucc = parseConstInterval(itemValue.trim(), constValues, 3);
                break;
            case 4:
                loadSucc = parseConstDValuePercent(itemValue.trim(), constValues, 4);
                break;
            default:
                loadSucc = false;
                break;
        }
        return loadSucc;
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

    private boolean parseConstDValuePercent(String itemValue, int[] constValues, int index) {
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

    private void loadMemRepairMinMaxThresItem(AwareConfig.Item item, int mrSize) {
        if (mrSize > 20) {
            AwareLog.i(TAG, "loadMemRepairMinMaxThresItem too long mrSize=" + mrSize);
            return;
        }
        long[][] memThresHolds = (long[][]) Array.newInstance(long.class, new int[]{mrSize, 3});
        int index = 0;
        Iterator it = item.getSubItemList().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            AwareConfig.SubItem subItem = (AwareConfig.SubItem) it.next();
            if (index >= mrSize) {
                index = mrSize + 1;
                break;
            } else if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemValue)) {
                    AwareLog.i(TAG, "loadMemRepairMinMaxThresItem null item");
                } else {
                    char c = 65535;
                    if (itemName.hashCode() == -1545477013 && itemName.equals("threshold")) {
                        c = 0;
                    }
                    if (c != 0) {
                        AwareLog.w(TAG, "loadMemRepairMinMaxThresItem no such configuration:" + itemName);
                        index = mrSize + 1;
                    } else if (!parseMinMaxThres(itemValue.trim(), memThresHolds[index])) {
                        index = mrSize + 1;
                    } else {
                        index++;
                    }
                }
            }
        }
        if (index == mrSize) {
            MemRepairPolicy.getInstance().updateFloatThresHold(memThresHolds);
        }
    }

    private boolean parseMinMaxThres(String itemValue, long[] memThresHolds) {
        try {
            String[] sets = itemValue.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            if (sets.length != 3) {
                AwareLog.i(TAG, "error split item");
                return false;
            }
            long minFloatThres = Long.parseLong(sets[0].trim());
            long maxFloatThres = Long.parseLong(sets[1].trim());
            long percentage = Long.parseLong(sets[2].trim());
            if (minFloatThres >= 0 && maxFloatThres >= 0 && minFloatThres < maxFloatThres) {
                if (percentage >= 1) {
                    memThresHolds[0] = minFloatThres * 1024;
                    memThresHolds[1] = 1024 * maxFloatThres;
                    memThresHolds[2] = percentage;
                    AwareLog.i(TAG, "mem minthres:" + minFloatThres + ",maxthres:" + maxFloatThres + ",percent:" + percentage);
                    return true;
                }
            }
            AwareLog.i(TAG, "error minthres:" + minFloatThres + ",maxthres:" + maxFloatThres + ",percent:" + percentage);
            return false;
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
        }
    }

    private void loadMemRepairVssParameterItem(AwareConfig.Item item) {
        if (!isItemValid(item, MemoryConstant.MEM_REPAIR_CONSTANT_PARAS)) {
            AwareLog.w(TAG, "loadMemRepairMinMaxThresItem item invalid!");
            return;
        }
        String[] blocks = ((AwareConfig.SubItem) item.getSubItemList().get(0)).getValue().split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
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
        boolean z = true;
        if (subItems.size() != 1) {
            return false;
        }
        AwareConfig.SubItem subItem = subItems.get(0);
        if (subItem == null || TextUtils.isEmpty(subItem.getName()) || TextUtils.isEmpty(subItem.getValue()) || !subItem.getName().equals(exceptTag)) {
            z = false;
        }
        return z;
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

    private void loadMemRepairProcThresItem(long totalMemMb, AwareConfig.Item item, int itemSize, boolean isCustConfig) {
        int i = itemSize;
        if (i == 2) {
            long[][] thresHolds = (long[][]) Array.newInstance(long.class, new int[]{1, 2});
            int index = 0;
            Iterator it = item.getSubItemList().iterator();
            while (true) {
                if (it.hasNext()) {
                    AwareConfig.SubItem subItem = (AwareConfig.SubItem) it.next();
                    if (index >= i) {
                        index = i + 1;
                    } else if (subItem != null) {
                        String itemName = subItem.getName();
                        String itemValue = subItem.getValue();
                        if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemValue)) {
                            AwareLog.i(TAG, "loadMemRepairProcThresItem null item");
                        } else {
                            boolean finded = true;
                            int findIndex = 0;
                            char c = 65535;
                            int hashCode = itemName.hashCode();
                            if (hashCode != -1332194002) {
                                if (hashCode != 918485508) {
                                    if (hashCode != 1896801775) {
                                        if (hashCode == 1984457027 && itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG)) {
                                            c = 0;
                                        }
                                    } else if (itemName.equals(MemoryConstant.MEM_REPAIR_LOWMEM_BACKGROUND)) {
                                        c = 3;
                                    }
                                } else if (itemName.equals(MemoryConstant.MEM_REPAIR_LOWMEM_FOREGROUND)) {
                                    c = 1;
                                }
                            } else if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG)) {
                                c = 2;
                            }
                            switch (c) {
                                case 0:
                                case 1:
                                    findIndex = 0;
                                    break;
                                case 2:
                                case 3:
                                    findIndex = 1;
                                    break;
                                default:
                                    finded = false;
                                    AwareLog.w(TAG, "loadMemRepairProcThresItem no such configuration:" + itemName);
                                    index = i + 1;
                                    break;
                            }
                            int index2 = index;
                            int findIndex2 = findIndex;
                            if (finded) {
                                if (!parseProcThres(itemValue.trim(), thresHolds[0], findIndex2, totalMemMb)) {
                                    index = i + 1;
                                } else {
                                    index2++;
                                }
                            }
                            index = index2;
                        }
                    }
                }
            }
            AwareLog.i(TAG, "loadMemRepairProcThresItem null item");
            if (index != i || thresHolds[0][0] <= thresHolds[0][1]) {
                boolean z = isCustConfig;
            } else {
                MemRepairPolicy.getInstance().updateProcThresHold(thresHolds, isCustConfig);
            }
        }
    }

    private boolean parseProcThres(String itemValue, long[] thresHolds, int index, long totalMemMb) {
        try {
            long thres = Long.parseLong(itemValue.trim());
            if (thres >= 1) {
                if (thres < totalMemMb) {
                    thresHolds[index] = 1024 * thres;
                    AwareLog.i(TAG, "process threshold:" + thres);
                    return true;
                }
            }
            AwareLog.i(TAG, "error process threshold:" + thres);
            return false;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
            return false;
        }
    }

    private int getSize(String strSize) {
        if (strSize == null) {
            return 0;
        }
        try {
            int iSize = Integer.parseInt(strSize.trim());
            if (iSize >= 1) {
                return iSize;
            }
            AwareLog.w(TAG, "loadMemRepairMinMaxThresItem error size:" + iSize);
            return 0;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse size error");
            return 0;
        }
    }

    private void readMemoryAPIWhiteListUid() {
        MultiTaskManagerService mMtmService = MultiTaskManagerService.self();
        if (mMtmService != null) {
            Context mcontext = mMtmService.context();
            if (mcontext != null) {
                PackageManager pm = mcontext.getPackageManager();
                if (pm != null) {
                    try {
                        ApplicationInfo appInfo = pm.getApplicationInfo(MemoryConstant.CAMERA_PACKAGE_NAME, 1);
                        if (appInfo != null && appInfo.uid > 0) {
                            MemoryConstant.setSysCameraUid(appInfo.uid);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        AwareLog.e(TAG, "can not get uid");
                    }
                }
            }
        }
    }
}
