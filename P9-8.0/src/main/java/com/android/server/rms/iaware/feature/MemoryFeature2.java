package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.internal.util.MemInfoReader;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.BigDataStore;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PrereadUtils;
import com.android.server.rms.memrepair.MemRepairPolicy;
import com.android.server.rms.memrepair.ProcStateStatisData;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryFeature2 extends RFeature {
    private static final int BASE_VERSION = 2;
    private static final int MAX_CACHED_APPS = SystemProperties.getInt("ro.sys.fw.bg_apps_limit", 32);
    private static final int MAX_CACHED_APP_SIZE = 512;
    private static final int MIN_CACHED_APP_SIZE = 4;
    private static final String TAG = "AwareMem_MemFeature2.0";
    public static final AtomicBoolean isUpMemoryFeature = new AtomicBoolean(false);
    private int mEmptyProcessPercent;
    private HwActivityManagerService mHwAMS;
    private int mNumProcessLimit;
    private PrereadUtils mPrereadUtils;

    public MemoryFeature2(Context context, FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        this.mHwAMS = null;
        this.mEmptyProcessPercent = SystemProperties.getInt("ro.sys.fw.empty_app_percent", 50);
        this.mHwAMS = HwActivityManagerService.self();
        this.mNumProcessLimit = MAX_CACHED_APPS;
        this.mPrereadUtils = PrereadUtils.getInstance();
        PrereadUtils.setContext(context);
        loadMemConfig();
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
        isUpMemoryFeature.set(true);
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            MemoryUtils.writeMMonitorSwitch(1);
        }
        readCameraUid();
        PrereadUtils prereadUtils = this.mPrereadUtils;
        PrereadUtils.start();
        ProcStateStatisData.getInstance().setEnable(true);
        setEmptyProcessPercent(this.mEmptyProcessPercent);
        setProcessLimit(this.mNumProcessLimit);
        return true;
    }

    public boolean disable() {
        isUpMemoryFeature.set(false);
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            MemoryUtils.writeMMonitorSwitch(0);
        }
        MemoryUtils.destroySocket();
        PrereadUtils prereadUtils = this.mPrereadUtils;
        PrereadUtils.stop();
        ProcStateStatisData.getInstance().setEnable(false);
        setEmptyProcessPercent(-1);
        setProcessLimit(MAX_CACHED_APPS);
        return true;
    }

    public boolean reportData(CollectData data) {
        return false;
    }

    public boolean configUpdate() {
        loadMemConfig();
        setProcessLimit(this.mNumProcessLimit);
        return true;
    }

    public String getBigDataByVersion(int iawareVer, boolean forBeta, boolean clearData) {
        AwareLog.i(TAG, "Memoryfeature2 getBigDataByVersion. iawareVer: " + iawareVer + ", beta: " + forBeta + ", clear: " + clearData);
        if (!isUpMemoryFeature.get() || iawareVer < 2 || AwareConstant.CURRENT_USER_TYPE != 3) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        String result = MemoryReader.getMmonitorData();
        if (result != null) {
            builder.append(result).append("\n");
        }
        result = BigDataStore.getInstance().getColdWarmLaunchData(clearData);
        if (result != null) {
            builder.append(result).append("\n");
        }
        builder.append(BigDataStore.getInstance().getBgAppData(clearData));
        AwareLog.d(TAG, "Memoryfeature2 getBigDataByVersion. result: " + builder.toString());
        return builder.toString();
    }

    private void loadMemConfig() {
        loadMemConstantConfig(false);
        loadMemConstantConfig(true);
        loadPrereadConfig();
        loadMemRepairConfig();
        MemoryFeature.loadBigMemAppPolicyConfig(getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_BIGMEMAPP, true));
        MemoryFeature.loadBigMemAppPolicyConfig(getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_IONPROPERTYS, true));
    }

    private void loadMemConstantConfig(boolean isCustConfig) {
        AwareLog.i(TAG, "loadMemConstantConfig, isCustConfig : " + isCustConfig);
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_CONSTANT_CONFIGNAME, isCustConfig);
        if (configList == null) {
            AwareLog.w(TAG, "loadMemConstantConfig failure cause null configList");
            return;
        }
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        Long totalMemMb = Long.valueOf(minfo.getTotalSize() / MemoryConstant.MB_SIZE);
        for (Item item : configList.getConfigList()) {
            if (item != null && item.getProperties() != null) {
                String ramSize = (String) item.getProperties().get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME);
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
        Map<String, ArrayList<String>> filePathMap = new ArrayMap();
        for (Item item : configList.getConfigList()) {
            if (item == null) {
                AwareLog.w(TAG, "loadPrereadConfig failure, item is empty");
            } else {
                Map<String, String> configPropertries = item.getProperties();
                if (configPropertries == null) {
                    AwareLog.w(TAG, "loadPrereadConfig failure, configPropertries is empty");
                } else {
                    try {
                        if (Integer.parseInt((String) configPropertries.get(MemoryConstant.MEM_PREREAD_SWITCH)) == 0) {
                            AwareLog.w(TAG, "prereadSwitch off");
                        } else {
                            String pkgName = (String) configPropertries.get(MemoryConstant.MEM_PREREAD_ITEM_NAME);
                            if (pkgName != null) {
                                ArrayList<String> filePath = new ArrayList();
                                List<SubItem> subItemList = item.getSubItemList();
                                if (subItemList != null) {
                                    for (SubItem subItem : subItemList) {
                                        String itemName = subItem.getName();
                                        String itemValue = subItem.getValue();
                                        if (MemoryConstant.MEM_PREREAD_FILE.equals(itemName)) {
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

    protected AwareConfig getConfig(String featureName, String configName, boolean isCustConfig) {
        if (featureName == null || featureName.equals("") || configName == null || configName.equals("")) {
            AwareLog.i(TAG, "featureName or configName is null");
            return null;
        }
        AwareConfig configList = null;
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                configList = isCustConfig ? awareservice.getCustConfig(featureName, configName) : awareservice.getConfig(featureName, configName);
            } else {
                AwareLog.i(TAG, "can not find service awareservice.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "MemoryFeature getConfig RemoteException");
        }
        return configList;
    }

    private void saveMemConstantItem(Item item) {
        for (SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                try {
                    if (itemName.equals(MemoryConstant.MEM_CONSTANT_GMCSWITCH)) {
                        int gmcSwitch = Integer.parseInt(itemValue.trim());
                        AwareLog.i(TAG, "loadMemConfig gmc Switch: " + gmcSwitch);
                        MemoryConstant.setConfigGmcSwitch(gmcSwitch);
                    } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_GPUNAME)) {
                        long gpuMemoryLimit = Long.parseLong(itemValue.trim());
                        AwareLog.i(TAG, "loadMemConfig gpuMemoryLimit: " + (1024 * gpuMemoryLimit));
                        MemoryConstant.setGpuMemoryLimit(1024 * gpuMemoryLimit);
                    } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROCESSLIMIT)) {
                        int numProcessLimit = Integer.parseInt(itemValue.trim());
                        AwareLog.i(TAG, "loadMemConfig numProcessLimit: " + numProcessLimit);
                        this.mNumProcessLimit = SystemProperties.getInt("ro.sys.fw.bg_apps_limit", numProcessLimit);
                    } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROCESSPERCENT)) {
                        int numProcessPercent = Integer.parseInt(itemValue.trim());
                        AwareLog.i(TAG, "loadMemConfig numProcessPercent: " + numProcessPercent);
                        if (numProcessPercent >= 0 && numProcessPercent <= 100) {
                            this.mEmptyProcessPercent = SystemProperties.getInt("ro.sys.fw.empty_app_percent", numProcessPercent);
                        }
                    } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_PREREAD_ODEX)) {
                        int preread_odex_switch = Integer.parseInt(itemValue.trim());
                        AwareLog.i(TAG, "loadMemConfig preread_odex_switch: " + preread_odex_switch);
                        PrereadUtils.getInstance();
                        PrereadUtils.setPrereadOdexSwitch(preread_odex_switch);
                    } else {
                        AwareLog.w(TAG, "loadMemConfig no such configuration. " + itemName);
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "parse memory xml error");
                }
            }
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

    private void loadMemRepairConfig() {
        AwareLog.d(TAG, "loadMemRepairConfig begin");
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_REPAIR, false);
        if (configList == null) {
            AwareLog.w(TAG, "loadMemRepairConfig failure cause null configList");
            return;
        }
        List<Item> itemList = configList.getConfigList();
        if (itemList == null) {
            AwareLog.w(TAG, "loadMemRepairConfig failure cause null itemList");
            return;
        }
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        Long totalMemMb = Long.valueOf(minfo.getTotalSize() / MemoryConstant.MB_SIZE);
        for (Item item : itemList) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadMemRepairConfig continue cause null item");
            } else {
                Map<String, String> configPropertries = item.getProperties();
                String strName = (String) configPropertries.get("name");
                if (TextUtils.isEmpty(strName)) {
                    AwareLog.w(TAG, "loadMemRepairConfig failure null item name");
                } else {
                    int size = getSize((String) configPropertries.get(MemoryConstant.MEM_REPAIR_CONSTANT_SIZE));
                    if (size < 1) {
                        AwareLog.w(TAG, "loadMemRepairConfig continue failure size: " + size);
                    } else if (item.getSubItemList() == null) {
                        AwareLog.w(TAG, "loadMemRepairConfig continue cause null subitem");
                    } else if (strName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BASE)) {
                        loadMemRepairConstantItem(item, size);
                    } else if (!MemoryUtils.checkRamSize((String) configPropertries.get(MemoryConstant.MEM_CONSTANT_RAMSIZENAME), totalMemMb)) {
                        AwareLog.d(TAG, "checkRamSize failure ramSize, totalMemMb: " + totalMemMb);
                    } else if (strName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_MIN_MAX_THRES)) {
                        loadMemRepairMinMaxThresItem(item, size);
                    } else if (strName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_PROC_EMERG_THRES)) {
                        loadMemRepairProcThresItem(totalMemMb.longValue(), item, size);
                    }
                }
            }
        }
        AwareLog.d(TAG, "loadMemRepairConfig end");
    }

    private void loadMemRepairConstantItem(Item item, int mrSize) {
        int index = 0;
        int[] constValues = new int[]{0, 0, 0, 0, 0};
        int loadSucc = 1;
        for (SubItem subItem : item.getSubItemList()) {
            if (index >= mrSize) {
                index = mrSize + 1;
                break;
            } else if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemValue)) {
                    break;
                } else if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG_MIN_COUNT) || itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_MIN_COUNT) || itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG_INTERVAL) || itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_INTERVAL) || itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_DV_NEGA_PERCENT)) {
                    loadSucc = saveMemRepairConstantItem(itemName, itemValue.trim(), constValues);
                    if (loadSucc != 0) {
                        index++;
                    } else {
                        index = mrSize + 1;
                    }
                } else {
                    AwareLog.w(TAG, "loadMemRepairConstantItem no such configuration:" + itemName);
                    index = mrSize + 1;
                }
            }
        }
        if (index == mrSize && (loadSucc ^ 1) == 0) {
            ProcStateStatisData.getInstance().updateConfig(constValues[0], constValues[1], ((long) constValues[2]) * AppHibernateCst.DELAY_ONE_MINS, ((long) constValues[3]) * AppHibernateCst.DELAY_ONE_MINS);
            MemRepairPolicy.getInstance().updateCollectCount(constValues[0], constValues[1]);
            MemRepairPolicy.getInstance().updateDValueFloatPercent(constValues[4]);
        }
    }

    private boolean saveMemRepairConstantItem(String itemName, String itemValue, int[] constValues) {
        if (constValues == null || constValues.length != 5) {
            return false;
        }
        boolean loadSucc;
        if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG_MIN_COUNT)) {
            loadSucc = parseConstMinCount(itemValue.trim(), constValues, 0);
            AwareLog.i(TAG, "saveMemRepairConstantItem minCount:" + constValues[0]);
        } else if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_MIN_COUNT)) {
            loadSucc = parseConstMinCount(itemValue.trim(), constValues, 1);
            AwareLog.i(TAG, "saveMemRepairConstantItem minCount:" + constValues[1]);
        } else if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG_INTERVAL)) {
            loadSucc = parseConstInterval(itemValue.trim(), constValues, 2);
            AwareLog.i(TAG, "saveMemRepairConstantItem fg interval:" + constValues[2]);
        } else if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG_INTERVAL)) {
            loadSucc = parseConstInterval(itemValue.trim(), constValues, 3);
            AwareLog.i(TAG, "saveMemRepairConstantItem bg interval:" + constValues[3]);
        } else if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_DV_NEGA_PERCENT)) {
            loadSucc = parseConstDValuePercent(itemValue.trim(), constValues, 4);
            AwareLog.i(TAG, "saveMemRepairConstantItem percent:" + constValues[4]);
        } else {
            loadSucc = false;
            AwareLog.w(TAG, "saveMemRepairConstantItem no such configuration:" + itemName);
        }
        return loadSucc;
    }

    private boolean parseConstMinCount(String itemValue, int[] constValues, int index) {
        try {
            int minCount = Integer.parseInt(itemValue.trim());
            if (minCount < 6 || minCount > 50) {
                AwareLog.i(TAG, "error minCount:" + minCount);
                return false;
            }
            constValues[index] = minCount;
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
            return false;
        }
    }

    private boolean parseConstInterval(String itemValue, int[] constValues, int index) {
        try {
            int interval = Integer.parseInt(itemValue.trim());
            if (interval < 2 || interval > 30) {
                AwareLog.i(TAG, "error interval:" + interval);
                return false;
            }
            constValues[index] = interval;
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
            return false;
        }
    }

    private boolean parseConstDValuePercent(String itemValue, int[] constValues, int index) {
        try {
            int percent = Integer.parseInt(itemValue.trim());
            if (percent < 1 || percent > 30) {
                AwareLog.i(TAG, "error percent:" + percent);
                return false;
            }
            constValues[index] = percent;
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
            return false;
        }
    }

    private void loadMemRepairMinMaxThresItem(Item item, int mrSize) {
        if (mrSize > 20) {
            AwareLog.i(TAG, "loadMemRepairMinMaxThresItem too long mrSize=" + mrSize);
            return;
        }
        long[][] memThresHolds = (long[][]) Array.newInstance(Long.TYPE, new int[]{mrSize, 3});
        int index = 0;
        for (SubItem subItem : item.getSubItemList()) {
            if (index >= mrSize) {
                index = mrSize + 1;
                break;
            } else if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemValue)) {
                    AwareLog.i(TAG, "loadMemRepairMinMaxThresItem null item");
                    break;
                } else if (!itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_THRES)) {
                    AwareLog.w(TAG, "loadMemRepairMinMaxThresItem no such configuration:" + itemName);
                    index = mrSize + 1;
                } else if (parseMinMaxThres(itemValue.trim(), memThresHolds[index])) {
                    index++;
                } else {
                    index = mrSize + 1;
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
            if (minFloatThres < 0 || maxFloatThres < 0 || minFloatThres >= maxFloatThres || percentage < 1) {
                AwareLog.i(TAG, "error minthres:" + minFloatThres + ",maxthres:" + maxFloatThres + ",percent:" + percentage);
                return false;
            }
            memThresHolds[0] = 1024 * minFloatThres;
            memThresHolds[1] = 1024 * maxFloatThres;
            memThresHolds[2] = percentage;
            AwareLog.i(TAG, "mem minthres:" + minFloatThres + ",maxthres:" + maxFloatThres + ",percent:" + percentage);
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
            return false;
        }
    }

    private void loadMemRepairProcThresItem(long totalMemMb, Item item, int itemSize) {
        if (itemSize == 2) {
            long[][] thresHolds = (long[][]) Array.newInstance(Long.TYPE, new int[]{1, 2});
            int index = 0;
            for (SubItem subItem : item.getSubItemList()) {
                if (index >= itemSize) {
                    index = itemSize + 1;
                    break;
                } else if (subItem != null) {
                    String itemName = subItem.getName();
                    String itemValue = subItem.getValue();
                    if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemValue)) {
                        AwareLog.i(TAG, "loadMemRepairProcThresItem null item");
                        break;
                    }
                    boolean finded = true;
                    int findIndex = 0;
                    if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_FG)) {
                        findIndex = 0;
                    } else if (itemName.equals(MemoryConstant.MEM_REPAIR_CONSTANT_BG)) {
                        findIndex = 1;
                    } else {
                        finded = false;
                        AwareLog.w(TAG, "loadMemRepairProcThresItem no such configuration:" + itemName);
                        index = itemSize + 1;
                    }
                    if (finded) {
                        if (!parseProcThres(itemValue.trim(), thresHolds[0], findIndex, totalMemMb)) {
                            index = itemSize + 1;
                            break;
                        }
                        index++;
                    } else {
                        continue;
                    }
                }
            }
            if (index == itemSize && thresHolds[0][0] > thresHolds[0][1]) {
                MemRepairPolicy.getInstance().updateProcThresHold(thresHolds);
            }
        }
    }

    private boolean parseProcThres(String itemValue, long[] thresHolds, int index, long totalMemMb) {
        try {
            long thres = Long.parseLong(itemValue.trim());
            if (thres < 1 || thres >= totalMemMb) {
                AwareLog.i(TAG, "error process threshold:" + thres);
                return false;
            }
            thresHolds[index] = 1024 * thres;
            AwareLog.i(TAG, "process threshold:" + thres);
            return true;
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

    private void readCameraUid() {
        String packagename = MemoryConstant.CAMERA_PACKAGE_NAME;
        MultiTaskManagerService mMtmService = MultiTaskManagerService.self();
        if (mMtmService != null) {
            Context mcontext = mMtmService.context();
            if (mcontext != null) {
                PackageManager pm = mcontext.getPackageManager();
                if (pm != null) {
                    try {
                        ApplicationInfo appInfo = pm.getApplicationInfo(packagename, 1);
                        if (appInfo != null && appInfo.uid > 0) {
                            MemoryConstant.setSysCameraUid(appInfo.uid);
                        }
                    } catch (NameNotFoundException e) {
                        AwareLog.e(TAG, "can not get uid");
                    }
                }
            }
        }
    }
}
