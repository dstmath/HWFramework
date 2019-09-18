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
import android.rms.iaware.IAwaredConnection;
import android.rms.iaware.StatisticsData;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.util.MemInfoReader;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.memory.action.Action;
import com.android.server.rms.iaware.memory.action.GpuCompressAction;
import com.android.server.rms.iaware.memory.action.KillAction;
import com.android.server.rms.iaware.memory.action.QuickKillAction;
import com.android.server.rms.iaware.memory.action.ReclaimAction;
import com.android.server.rms.iaware.memory.action.SystemTrimAction;
import com.android.server.rms.iaware.memory.data.dispatch.DataDispatch;
import com.android.server.rms.iaware.memory.policy.DMEServer;
import com.android.server.rms.iaware.memory.policy.MemoryExecutorServer;
import com.android.server.rms.iaware.memory.policy.MemoryScenePolicy;
import com.android.server.rms.iaware.memory.policy.MemoryScenePolicyList;
import com.android.server.rms.iaware.memory.utils.BigDataStore;
import com.android.server.rms.iaware.memory.utils.BigMemoryInfo;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageInfoCollector;
import com.android.systemui.shared.recents.hwutil.HwRecentsTaskUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

public class MemoryFeature extends RFeature {
    private static final int INTERVAL_SAVE_DATA_IN_MSEC = 7200000;
    private static final String TAG = "MemFeature";
    private static final FeatureHandler mFeatureHandler = new FeatureHandler();
    private BigDataStore mBigDataStore = BigDataStore.getInstance();
    private final AtomicBoolean mRunning = new AtomicBoolean(false);

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
                sendEmptyMessageDelayed(102, HwRecentsTaskUtils.MAX_REMOVE_TASK_TIME);
            }
            DMEServer.getInstance().notifyProtectLruState(0);
            this.mFirstAccess = false;
        }

        public void disableProtectLru() {
            removeProtectLruMsg();
            sendEmptyMessageDelayed(101, 1000);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 101:
                    MemoryUtils.disableProtectLru();
                    return;
                case 102:
                    MemoryUtils.onProtectLruConfigUpdate();
                    return;
                default:
                    AwareLog.e(MemoryFeature.TAG, "error msg what = " + msg.what);
                    return;
            }
        }
    }

    public MemoryFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        MemoryConstant.init(this.mContext);
        MemoryConstant.setCameraPowerUPMem();
        loadMemConfig();
    }

    public boolean enable() {
        subscribleEvents();
        DMEServer.getInstance().enable();
        DataDispatch.getInstance().start();
        setBoostKillSwitch(true);
        MemoryUtils.writeSwappiness(MemoryConstant.getConfigSwappiness());
        MemoryUtils.writeDirectSwappiness(MemoryConstant.getConfigDirectSwappiness());
        mFeatureHandler.enableProtectLru();
        this.mRunning.set(true);
        return true;
    }

    public boolean disable() {
        MemoryUtils.writeExtraFreeKbytes(MemoryConstant.DEFAULT_EXTRA_FREE_KBYTES);
        MemoryUtils.writeSwappiness(60);
        MemoryUtils.writeDirectSwappiness(60);
        setBoostKillSwitch(false);
        mFeatureHandler.disableProtectLru();
        DataDispatch.getInstance().stop();
        DMEServer.getInstance().disable();
        unSubscribeEvents();
        this.mRunning.set(false);
        return true;
    }

    public boolean reportData(CollectData data) {
        if (!this.mRunning.get()) {
            return false;
        }
        DataDispatch.getInstance().reportData(data);
        return true;
    }

    public ArrayList<DumpData> getDumpData(int time) {
        if (!this.mRunning.get()) {
            return null;
        }
        return EventTracker.getInstance().getDumpData(time);
    }

    public String saveBigData(boolean clear) {
        AwareLog.d(TAG, "enter saveBigData");
        if (!this.mRunning.get()) {
            return null;
        }
        this.mBigDataStore.totalTimeEnd = SystemClock.elapsedRealtime();
        this.mBigDataStore.getMeminfoAllocCount();
        this.mBigDataStore.getLmkOccurCount();
        String rtStatisJsonStr = creatJsonStr();
        if (clear) {
            clearCache();
        }
        return rtStatisJsonStr;
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        if (!this.mRunning.get()) {
            return null;
        }
        return EventTracker.getInstance().getStatisticsData();
    }

    public boolean configUpdate() {
        loadMemConfig();
        MemoryUtils.onProtectLruConfigUpdate();
        DMEServer.getInstance().notifyProtectLruState(0);
        return true;
    }

    private void loadMemConfig() {
        loadMemConstantConfig(false);
        loadBigMemAppPolicyConfig(getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_BIGMEMAPP, true));
        MemoryExecutorServer.getInstance().setMemoryScenePolicyList(loadMemPolicyListConfig());
        loadFileCacheConfig();
        loadMemConstantConfig(true);
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

    public static void loadBigMemAppPolicyConfig(AwareConfig custConfigList) {
        if (custConfigList == null) {
            AwareLog.w(TAG, "loadBigMemAppConfig failure cause null configList");
            return;
        }
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
                    Long totalMemMb = Long.valueOf(MemoryReader.getInstance().getTotalRam() / 1024);
                    if (!MemoryUtils.checkRamSize(ramSize, totalMemMb)) {
                        AwareLog.d(TAG, "checkRamSize failure cause ramSize: " + ramSize + " totalMemMb: " + totalMemMb);
                    } else {
                        saveCameraIonConfig(item);
                    }
                }
            }
        }
    }

    private static void saveBigMemoryAppConfig(String appName, AwareConfig.Item item) {
        String appNameTemp = appName;
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                try {
                    if (itemName.hashCode() == -1109843021) {
                        if (itemName.equals(MemoryConstant.MEM_SCENE_LAUNCH)) {
                            c = 0;
                        }
                    }
                    if (c != 0) {
                        AwareLog.w(TAG, "no such configuration!");
                    } else {
                        long launchRequestMem = Long.parseLong(itemValue.trim());
                        AwareLog.d(TAG, "saveBigMemoryAppConfig " + appNameTemp + " request memory is " + launchRequestMem);
                        BigMemoryInfo.getInstance().setRequestMemForLaunch(appNameTemp, launchRequestMem);
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "parse app mem error: " + e);
                }
            }
        }
    }

    private static void saveCameraIonConfig(AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                char c = 65535;
                try {
                    int hashCode = itemName.hashCode();
                    if (hashCode != -190523471) {
                        if (hashCode == 2089932130) {
                            if (itemName.equals(MemoryConstant.MEM_CONSTANT_IONSPEEDUPSWITCH)) {
                                c = 0;
                            }
                        }
                    } else if (itemName.equals(MemoryConstant.MEM_CONSTANT_CAMERAPOWERUPNAME)) {
                        c = 1;
                    }
                    switch (c) {
                        case 0:
                            int ionSpeedupSwitch = Integer.parseInt(itemValue.trim());
                            AwareLog.i(TAG, "saveCameraIonConfigs camera ion memory speedup switch: " + ionSpeedupSwitch);
                            MemoryConstant.setConfigIonSpeedupSwitch(ionSpeedupSwitch);
                            break;
                        case 1:
                            int cameraPowerupIonMemory = Integer.parseInt(itemValue.trim());
                            AwareLog.i(TAG, "saveCameraIonConfigs camera powerup memory: " + (cameraPowerupIonMemory * 1024));
                            MemoryConstant.setCameraPowerUPMemoryDefault(cameraPowerupIonMemory * 1024);
                            break;
                        default:
                            AwareLog.w(TAG, "saveCameraIonConfigs no such configuration. " + itemName);
                            break;
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "saveCameraIonConfigs parse memory xml error");
                }
            }
        }
    }

    private void loadFileCacheConfig() {
        AwareLog.d(TAG, "loadFileCacheConfig begin");
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_FILECACHE, false);
        if (configList == null) {
            AwareLog.w(TAG, "loadFileCacheConfig failure cause null configList");
            return;
        }
        ArrayMap<Integer, ArraySet<String>> fileCacheMap = new ArrayMap<>();
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadFileCacheConfig continue cause null item");
            } else {
                Map<String, String> configPropertries = item.getProperties();
                String strName = configPropertries.get("name");
                if (TextUtils.isEmpty(strName)) {
                    AwareLog.w(TAG, "loadFileCacheConfig failure cause name: " + strName);
                } else {
                    String strLevel = configPropertries.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
                    int fileCacheLevel = checkFileCacheLevel(strLevel);
                    if (fileCacheLevel == -1) {
                        AwareLog.w(TAG, "checkFileCacheLevel failure cause level: " + strLevel);
                    } else {
                        AwareLog.d(TAG, "loadFileCacheConfig success. level: " + strLevel);
                        if (item.getSubItemList() == null) {
                            AwareLog.w(TAG, "loadFileCacheConfig continue cause null item");
                        } else {
                            ArraySet<String> xmlSet = new ArraySet<>();
                            for (AwareConfig.SubItem subitem : item.getSubItemList()) {
                                if (subitem != null && !TextUtils.isEmpty(subitem.getValue())) {
                                    xmlSet.add(subitem.getValue());
                                }
                            }
                            AwareLog.i(TAG, "loadFileCacheConfig xmlSet=" + xmlSet.toString());
                            if ("file".equals(strName)) {
                                fileCacheMap.put(Integer.valueOf(fileCacheLevel), xmlSet);
                            } else if ("package".equals(strName)) {
                                ArraySet<String> pkgSet = PackageInfoCollector.getLibFilesFromPackage(this.mContext, xmlSet);
                                if (pkgSet != null && pkgSet.size() > 0) {
                                    fileCacheMap.put(Integer.valueOf(fileCacheLevel + 50), pkgSet);
                                }
                            }
                        }
                    }
                }
            }
        }
        MemoryConstant.setFileCacheMap(fileCacheMap);
        AwareLog.d(TAG, "loadFileCacheConfig end");
    }

    private int checkFileCacheLevel(String fileCacheLevel) {
        int i = -1;
        if (TextUtils.isEmpty(fileCacheLevel)) {
            return -1;
        }
        try {
            int level = Integer.parseInt(fileCacheLevel.trim());
            if (1 <= level && level <= 3) {
                i = level;
            }
            return i;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse filecache index error: " + e);
            return -1;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x008d  */
    private boolean saveKernelParamItem(String itemName, String itemValue, StringBuffer logbuf) throws NumberFormatException {
        char c;
        int hashCode = itemName.hashCode();
        if (hashCode == -286045405) {
            if (itemName.equals(MemoryConstant.MEM_CONSTANT_SWAPPINESSNAME)) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 175422052) {
            if (itemName.equals(MemoryConstant.MEM_CONSTANT_EXTRAFREEKBYTESNAME)) {
                c = 2;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 274770233 && itemName.equals(MemoryConstant.MEM_CONSTANT_DIRECTSWAPPINESSNAME)) {
            c = 1;
            switch (c) {
                case 0:
                    int swappiness = Integer.parseInt(itemValue.trim());
                    logbuf.append(itemName + ":" + itemValue + " ");
                    MemoryConstant.setConfigSwappiness(swappiness);
                    return true;
                case 1:
                    int directswappiness = Integer.parseInt(itemValue.trim());
                    logbuf.append(itemName + ":" + itemValue + " ");
                    MemoryConstant.setConfigDirectSwappiness(directswappiness);
                    return true;
                case 2:
                    int extraFreeKbytes = Integer.parseInt(itemValue.trim());
                    logbuf.append(itemName + ":" + itemValue + " ");
                    MemoryConstant.setConfigExtraFreeKbytes(extraFreeKbytes);
                    return true;
                default:
                    return false;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private void saveMemConstantItem(AwareConfig.Item item) {
        StringBuffer logbuf = new StringBuffer();
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (!(itemName == null || itemValue == null)) {
                try {
                    char c = 1;
                    if (true != saveKernelParamItem(itemName, itemValue, logbuf)) {
                        switch (itemName.hashCode()) {
                            case -1919496163:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_KILL_GAP)) {
                                    c = 10;
                                    break;
                                }
                            case -1111598932:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_RESERVEDZRAMNAME)) {
                                    c = 6;
                                    break;
                                }
                            case -1053226004:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_CLEAN_ALL)) {
                                    c = 9;
                                    break;
                                }
                            case -836436967:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_RECLAIMENHANCE)) {
                                    c = 12;
                                    break;
                                }
                            case -633324046:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_EMERGEMCYMEMORYNAME)) {
                                    c = 2;
                                    break;
                                }
                            case -489919045:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROTECTLRULIMIT)) {
                                    c = 7;
                                    break;
                                }
                            case -484609525:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_PROTECTRATIO)) {
                                    c = 8;
                                    break;
                                }
                            case -34546488:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_NORMALMEMORYNAME)) {
                                    c = 5;
                                    break;
                                }
                            case 90016637:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_RECLAIMFILECACHE)) {
                                    c = 11;
                                    break;
                                }
                            case 671098540:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_HIGHCPULOADNAME)) {
                                    break;
                                }
                            case 745458209:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_DEFAULTCRITICALMEMORYNAME)) {
                                    c = 4;
                                    break;
                                }
                            case 1078523066:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_LOWCPULOADNAME)) {
                                    c = 0;
                                    break;
                                }
                            case 1403791765:
                                if (itemName.equals(MemoryConstant.MEM_CONSTANT_BIGMEMCRITICALMEMORYNAME)) {
                                    c = 3;
                                    break;
                                }
                        }
                        c = 65535;
                        switch (c) {
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                                logbuf.append(itemName + ":" + itemValue + " ");
                                saveMemConstantKillItems(itemName, itemValue);
                                break;
                            case 6:
                                long reservedZram = Long.parseLong(itemValue.trim());
                                logbuf.append(itemName + ":" + itemValue + " ");
                                MemoryConstant.setReservedZramSpace(1024 * reservedZram);
                                break;
                            case 7:
                                String protectLruLimit = itemValue.trim();
                                logbuf.append(itemName + ":" + itemValue + " ");
                                MemoryConstant.setConfigProtectLruLimit(protectLruLimit);
                                break;
                            case 8:
                                int ratio = Integer.parseInt(itemValue.trim());
                                logbuf.append(itemName + ":" + itemValue + " ");
                                MemoryConstant.setConfigProtectLruRatio(ratio);
                                break;
                            case 9:
                                int cleanAllSwitch = Integer.parseInt(itemValue.trim());
                                logbuf.append(itemName + ":" + itemValue + " ");
                                MemoryConstant.setCleanAllSwitch(cleanAllSwitch);
                                break;
                            case 10:
                                long killMemGap = Long.parseLong(itemValue.trim());
                                logbuf.append(itemName + ":" + itemValue + " ");
                                MemoryConstant.setKillGapMemory(killMemGap);
                                break;
                            case 11:
                                int reclaimFileCache = Integer.parseInt(itemValue.trim());
                                logbuf.append(itemName + ":" + itemValue + " ");
                                MemoryConstant.setConfigReclaimFileCache(reclaimFileCache);
                                break;
                            case 12:
                                int reclaimEnhanceSwitch = Integer.parseInt(itemValue.trim());
                                logbuf.append(itemName + ":" + itemValue + " ");
                                MemoryConstant.setReclaimEnhanceSwitch(reclaimEnhanceSwitch);
                                break;
                        }
                    }
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "parse memory xml error");
                }
            }
        }
        AwareLog.d(TAG, logbuf.toString());
    }

    private void saveMemConstantKillItems(String itemName, String itemValue) {
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
            switch (c) {
                case 0:
                    MemoryConstant.setIdleThresHold(Long.parseLong(itemValue.trim()));
                    return;
                case 1:
                    MemoryConstant.setNormalThresHold(Long.parseLong(itemValue.trim()));
                    return;
                case 2:
                    MemoryConstant.setEmergencyMemory(1024 * Long.parseLong(itemValue.trim()));
                    return;
                case 3:
                    MemoryConstant.setBigMemoryAppCriticalMemory(1024 * Long.parseLong(itemValue.trim()));
                    return;
                case 4:
                    MemoryConstant.setDefaultCriticalMemory(1024 * Long.parseLong(itemValue.trim()));
                    return;
                case 5:
                    MemoryConstant.setIdleMemory(1024 * Long.parseLong(itemValue.trim()));
                    return;
                default:
                    return;
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse memory xml error");
        }
    }

    private MemoryScenePolicyList loadMemPolicyListConfig() {
        Map<String, MemoryScenePolicy> memoryScenePolicies = new ArrayMap<>();
        AwareConfig configList = getConfig(MemoryConstant.MEM_POLICY_FEATURENAME, MemoryConstant.MEM_POLICY_CONFIGNAME, false);
        if (configList == null) {
            return new MemoryScenePolicyList(createDefaultMemorPolicyList());
        }
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (item.getProperties() != null) {
                String scene = item.getProperties().get(MemoryConstant.MEM_POLICY_SCENE);
                if (scene != null) {
                    AwareLog.d(TAG, "add scene: " + scene);
                    ArrayList<Action> memActions = getActionList(item);
                    if (memActions.size() > 0) {
                        memoryScenePolicies.put(scene, new MemoryScenePolicy(scene, memActions));
                    }
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
                switch (c) {
                    case 0:
                        action = new ReclaimAction(this.mContext);
                        break;
                    case 1:
                        action = new KillAction(this.mContext);
                        break;
                    case 2:
                        action = new QuickKillAction(this.mContext);
                        break;
                    case 3:
                        action = new GpuCompressAction(this.mContext);
                        break;
                    case 4:
                        action = new SystemTrimAction(this.mContext);
                        break;
                    default:
                        AwareLog.e(TAG, "no such action!");
                        action = null;
                        break;
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

    private void subscribleEvents() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RES_DEV_STATUS, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_USERHABIT, this.mFeatureType);
            this.mIRDataRegister.subscribeData(AwareConstant.ResourceType.RESOURCE_FACE_RECOGNIZE, this.mFeatureType);
        }
    }

    private void unSubscribeEvents() {
        if (this.mIRDataRegister != null) {
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_APP, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_DEV_STATUS, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_ON, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_SCREEN_OFF, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RES_INPUT, this.mFeatureType);
            this.mIRDataRegister.unSubscribeData(AwareConstant.ResourceType.RESOURCE_FACE_RECOGNIZE, this.mFeatureType);
        }
    }

    private void setBoostKillSwitch(boolean isEnable) {
        AwareLog.i(TAG, "setBoostSigKill switch = " + isEnable);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(301);
        buffer.putInt(isEnable);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    private String creatJsonStr() {
        JSONObject memoryAllocateJson = new JSONObject();
        JSONObject memoryControlAndLMKJson = new JSONObject();
        JSONObject availMemoryTimeJson = new JSONObject();
        try {
            memoryAllocateJson.put("memoryAllocCount", String.valueOf(this.mBigDataStore.meminfoAllocCount - this.mBigDataStore.meminfoAllocCountStash));
            memoryAllocateJson.put("slowPathAllocCount", String.valueOf(this.mBigDataStore.slowPathAllocCount - this.mBigDataStore.slowPathAllocCountStash));
            memoryControlAndLMKJson.put("LMKCount", String.valueOf(this.mBigDataStore.lmkOccurCount - this.mBigDataStore.lmkOccurCountStash));
            memoryControlAndLMKJson.put("memoryControlCount", String.valueOf(this.mBigDataStore.lowMemoryManageCount));
            availMemoryTimeJson.put("belowThresholdTime", String.valueOf(this.mBigDataStore.belowThresholdTime));
            this.mBigDataStore.aboveThresholdTime = (this.mBigDataStore.totalTimeEnd - this.mBigDataStore.totalTimeBegin) - this.mBigDataStore.belowThresholdTime;
            availMemoryTimeJson.put("aboveThresholdTime", String.valueOf(this.mBigDataStore.aboveThresholdTime));
        } catch (JSONException e) {
            AwareLog.e(TAG, "JSONException...");
        }
        return "[iAwareMemoryRTStatis_Start]\n" + "{\n" + "\"" + "memoryAllocateCount" + "\"" + ":" + memoryAllocateJson.toString() + "," + "\n" + "\"" + "memoryControlAndLMKCount" + "\"" + ":" + memoryControlAndLMKJson.toString() + "," + "\n" + "\"" + "availMemoryTimeCount" + "\"" + ":" + availMemoryTimeJson.toString() + "," + "\n" + "}\n" + "[iAwareMemoryRTStatis_End]";
    }

    private void clearCache() {
        AwareLog.e(TAG, "enter clearCache...");
        this.mBigDataStore.lowMemoryManageCount = 0;
        this.mBigDataStore.belowThresholdTime = 0;
        this.mBigDataStore.meminfoAllocCountStash = this.mBigDataStore.meminfoAllocCount;
        this.mBigDataStore.slowPathAllocCountStash = this.mBigDataStore.slowPathAllocCount;
        this.mBigDataStore.lmkOccurCountStash = this.mBigDataStore.lmkOccurCount;
        this.mBigDataStore.aboveThresholdTime = 0;
        this.mBigDataStore.totalTimeBegin = SystemClock.elapsedRealtime();
        this.mBigDataStore.totalTimeEnd = SystemClock.elapsedRealtime();
    }
}
