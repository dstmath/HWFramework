package com.android.server.rms.iaware.appmng;

import android.content.Context;
import android.rms.HwSysResManager;
import android.rms.IUpdateWhiteListCallback;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.algorithm.AwareUserHabitAlgorithm;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AwareDefaultConfigList {
    private static final String ADJCUSTTOP_CNT_KEY = "ADJCUSTTOP_CNT";
    private static final String ALARM_CHK_KEY = "ALM";
    public static final String APPMNG_2G_CFG_KEY = "APPMNG_2G_CFG";
    public static final String APPMNG_3G_CFG_KEY = "APPMNG_3G_CFG";
    public static final String APPMNG_4G_CFG_KEY = "APPMNG_4G_CFG";
    private static final int APPMNG_BAD_APP_TYPE = 7;
    private static final int APPMNG_CFG_ADJ_2G_TYPE = 4;
    private static final int APPMNG_CFG_ADJ_3G_TYPE = 3;
    private static final int APPMNG_CFG_ADJ_TYPE = 2;
    private static final int APPMNG_CFG_WHITE_TYPE = 1;
    private static final int APPMNG_LOWEND_PROTECTED_ID = 5;
    private static final int APPMNG_RESTART_APP_TYPE = 6;
    private static final int BG_DECAY_TWO_HOUR_MASK = 4;
    private static final String BG_DELAY_KEY = "BGDCY";
    public static final int GROUP_ID_FORBIDSTOP = 1;
    public static final int GROUP_ID_NOSPEC = 0;
    public static final int GROUP_ID_SHORTAGESTOP = 2;
    private static final int HABIT_PROT_MAX_CNT = 10000;
    public static final int HW_PERCEPTIBLE_APP_ADJ = 260;
    private static final String IM_CNT_KEY = "IM_CNT";
    private static final String KEYPROC_DECAY_KEY = "KEYPROC_DECAY";
    private static final String KILL_MORE_KEY = "KM";
    private static final int KILL_MORE_MASK = 2;
    private static final Object LOCK = new Object();
    private static final String LOWEND_KEY = "LOW";
    private static final int LOW_END_MASK = 1;
    private static final int MASK_CLEANRES = 1;
    private static final int MASK_FREQUENTLYUSED = 4;
    private static final int MASK_GROUP = 3840;
    private static final int MASK_RESTART = 2;
    private static final int MEM_OPT_FOR_3G = SystemPropertiesEx.getInt("sys.iaware.mem_opt", 0);
    private static final String MEM_THRD_KEY = "MEM_THRD";
    private static final String OPTB_CHINA = "156";
    private static final String PG_PROTECT_KEY = "PROT";
    private static final String RESTART_KEY = "RESTART";
    private static final String SCREEN_CHANGED_KEY = "screenChanged";
    private static final String SEPARATOR = "#";
    private static final String SMART_CLEAN_INTERVAL_KEY = "smartCleanInterval";
    private static final String SYSPROC_DECAY_KEY = "SYSPROC_DECAY";
    private static final String TAG = "AwareDefaultConfigList";
    private static final String TOPN_CNT_KEY = "TOPN_CNT";
    private static boolean sDebug = false;
    private static AwareDefaultConfigList sInstance = null;
    private final ArraySet<String> mAdjustAdjList = new ArraySet<>();
    private final ArrayList<String> mAllHabitAppList = new ArrayList<>();
    private final ArraySet<String> mAwareProtectList = new ArraySet<>();
    private final ArrayMap<String, PackageConfigItem> mAwareProtectMap = new ArrayMap<>();
    private final ArraySet<String> mBadAppList = new ArraySet<>();
    private int mCfgAdjTypeId = 4;
    private Context mContext;
    private boolean mEnabled = false;
    private final ArraySet<String> mHabitFrequentUsed = new ArraySet<>();
    private AwareUserHabitAlgorithm.HabitProtectListChangeListener mHabitListener = new AwareUserHabitAlgorithm.HabitProtectListChangeListener() {
        /* class com.android.server.rms.iaware.appmng.AwareDefaultConfigList.AnonymousClass1 */

        @Override // com.android.server.rms.algorithm.AwareUserHabitAlgorithm.HabitProtectListChangeListener
        public void onListChanged() {
            AwareDefaultConfigList.this.setHabitWhiteList();
        }
    };
    private boolean mHasReadXml = false;
    private final ArrayList<String> mKeyHabitAppList = new ArrayList<>();
    private final Object mLock = new Object();
    private boolean mLowEnd = false;
    private final ArraySet<String> mRestartAppList = new ArraySet<>();
    private IUpdateWhiteListCallback mUpdateWhiteListCallback = new IUpdateWhiteListCallback.Stub() {
        /* class com.android.server.rms.iaware.appmng.AwareDefaultConfigList.AnonymousClass2 */

        public void update() {
            if (AwareDefaultConfigList.sDebug) {
                AwareLog.d(AwareDefaultConfigList.TAG, "IUpdateWhiteListCallback update whiteList.");
            }
            synchronized (AwareDefaultConfigList.this.mLock) {
                AwareDefaultConfigList.this.mHasReadXml = false;
                AwareDefaultConfigList.this.mAwareProtectList.clear();
            }
            AwareDefaultConfigList.this.setStaticXmlWhiteList();
        }
    };

    /* access modifiers changed from: package-private */
    public static class AppMngCfgXml {
        int mAdjCustTopN = 0;
        boolean mAlarmChk = true;
        long mBgDecay = 7200;
        int mImCnt = 0;
        long mKeyDecay = 0;
        boolean mKillMore = false;
        boolean mLowEnd = false;
        long mMemThrd = 0;
        boolean mPgProtect = false;
        boolean mRestart = true;
        int mScreenChanged = 30;
        int mSmartCleanInterval = 600;
        long mSysDecay = 0;
        int mTopNum = 0;

        AppMngCfgXml() {
        }
    }

    public static class ProcessConfigItem {
        public boolean frequentlyUsed;
        public int groupId;
        public String name;
        public boolean resCleanAllow;
        public boolean restartFlag;

        private ProcessConfigItem() {
        }

        public ProcessConfigItem(String procName, int value) {
            this.name = procName;
            boolean z = false;
            this.resCleanAllow = (value & 1) != 0;
            this.restartFlag = (value & 2) != 0;
            this.frequentlyUsed = (value & 4) != 0 ? true : z;
            this.groupId = ((value & AwareDefaultConfigList.MASK_GROUP) >> 8) & 15;
        }

        public ProcessConfigItem copy() {
            ProcessConfigItem dst = new ProcessConfigItem();
            dst.name = this.name;
            dst.resCleanAllow = this.resCleanAllow;
            dst.restartFlag = this.restartFlag;
            dst.frequentlyUsed = this.frequentlyUsed;
            dst.groupId = this.groupId;
            return dst;
        }
    }

    public static class PackageConfigItem extends ProcessConfigItem {
        ArrayMap<String, ProcessConfigItem> mProcessMap = new ArrayMap<>();

        public PackageConfigItem(String name, int value) {
            super(name, value);
        }

        public void add(ProcessConfigItem item) {
            if (item != null) {
                this.mProcessMap.put(item.name, item);
            }
        }

        public boolean isEmpty() {
            return this.mProcessMap.isEmpty();
        }

        public ProcessConfigItem getItem(String processName) {
            return this.mProcessMap.get(processName);
        }
    }

    private AwareDefaultConfigList() {
    }

    private void initialize(Context context) {
        AwareUserHabit usrhabit;
        this.mContext = context;
        this.mEnabled = true;
        setAllWhiteList();
        startObserver();
        if (this.mLowEnd && (usrhabit = AwareUserHabit.getInstance()) != null) {
            usrhabit.setLowEndFlag(true);
        }
    }

    private void deInitialize() {
        AwareUserHabit habitInstance = AwareUserHabit.getInstance();
        if (habitInstance != null) {
            habitInstance.unregistHabitProtectListChangeListener(this.mHabitListener);
        }
        this.mEnabled = false;
        synchronized (this.mLock) {
            this.mHasReadXml = false;
            this.mAwareProtectList.clear();
            this.mAwareProtectMap.clear();
            this.mKeyHabitAppList.clear();
            this.mAllHabitAppList.clear();
        }
        synchronized (this.mAdjustAdjList) {
            this.mAdjustAdjList.clear();
        }
        synchronized (this.mRestartAppList) {
            this.mRestartAppList.clear();
        }
        synchronized (this.mBadAppList) {
            this.mBadAppList.clear();
        }
        synchronized (this.mHabitFrequentUsed) {
            this.mHabitFrequentUsed.clear();
        }
    }

    public static void enable(Context context) {
        if (sDebug) {
            AwareLog.d(TAG, "WhiteList Feature enable!!!");
        }
        getInstance().initialize(context);
    }

    public static void disable() {
        if (sDebug) {
            AwareLog.d(TAG, "WhiteList Feature disable!!!");
        }
        getInstance().deInitialize();
    }

    private void startObserver() {
        if (sDebug) {
            AwareLog.d(TAG, "WhiteList Feature startObserver!!!");
        }
        startXmlObserver();
        startHabitObserver();
    }

    public static void enableDebug() {
        sDebug = true;
    }

    public static void disableDebug() {
        sDebug = false;
    }

    public static AwareDefaultConfigList getInstance() {
        AwareDefaultConfigList awareDefaultConfigList;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new AwareDefaultConfigList();
            }
            awareDefaultConfigList = sInstance;
        }
        return awareDefaultConfigList;
    }

    private void setAllWhiteList() {
        setStaticXmlWhiteList();
        setHabitWhiteList();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStaticXmlWhiteList() {
        synchronized (this.mLock) {
            if (this.mHasReadXml) {
                return;
            }
        }
        updateAppMngCfgFromRms(28);
        updateAdjWhiteListFromRms(28);
        updateRestartAppListFromRms(28);
        updateBadAppListFromRms(28);
        ArraySet<String> awareList = getWhiteListFromRms(28);
        if (awareList != null) {
            synchronized (this.mLock) {
                this.mAwareProtectList.addAll((ArraySet<? extends String>) awareList);
                parseAwareProtectList();
                this.mHasReadXml = true;
            }
        }
    }

    private List<String> getGcmAppList(AwareUserHabit habitInstance) {
        List<String> gcmList = habitInstance.getGCMAppList();
        if (gcmList == null || AppMngConfig.getAbroadFlag()) {
            return gcmList;
        }
        List<String> gcmListFilter = new ArrayList<>();
        for (String pkg : gcmList) {
            if (isNeedGcmByAppType(AppTypeRecoManager.getInstance().getAppType(pkg))) {
                gcmListFilter.add(pkg);
            }
        }
        return gcmListFilter;
    }

    private boolean isNeedGcmByAppType(int appType) {
        if (appType == -1 || appType == 0 || appType == 1 || appType == 255 || appType > 255) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setHabitWhiteList() {
        List<String> habitProtectList;
        AwareUserHabit habitInstance = AwareUserHabit.getInstance();
        if (habitInstance != null) {
            List<String> gcmList = getGcmAppList(habitInstance);
            List<String> habitProtectListAll = habitInstance.getHabitProtectListAll(10000, 10000);
            if (sDebug) {
                AwareLog.i(TAG, "AllHabitListChangeListener onListChanged list:" + habitProtectListAll);
            }
            if (habitProtectListAll != null) {
                if (gcmList != null) {
                    habitProtectListAll.addAll(gcmList);
                }
                updateAllImCache(habitProtectListAll);
            }
            if (!this.mLowEnd) {
                habitProtectList = habitInstance.getHabitProtectList(10000, 10000);
            } else {
                int emailCnt = AppMngConfig.getImCnt() / 2;
                habitProtectList = habitInstance.queryHabitProtectAppList(AppMngConfig.getImCnt() - emailCnt, emailCnt);
            }
            List<String> keyImList = new ArrayList<>();
            if (habitProtectList != null) {
                keyImList.addAll(habitProtectList);
            }
            if (gcmList != null && !this.mLowEnd) {
                keyImList.addAll(gcmList);
            }
            if (sDebug) {
                AwareLog.i(TAG, "HabitListChangeListener onListChanged list:" + keyImList);
            }
            updateKeyImCache(keyImList);
        }
    }

    private ArraySet<String> getWhiteListFromRms(int rmsGroupId) {
        String str;
        ArraySet<String> whiteList = new ArraySet<>();
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (this.mLowEnd) {
            str = resManager.getWhiteList(rmsGroupId, 5);
        } else {
            str = resManager.getWhiteList(rmsGroupId, 0);
        }
        if (str != null) {
            for (String content : str.split(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                String content2 = content.trim();
                if (!content2.isEmpty()) {
                    whiteList.add(content2);
                }
            }
            return whiteList;
        } else if (!sDebug) {
            return null;
        } else {
            AwareLog.e(TAG, "getWhiteListFromRms failed because null whiteList!");
            return null;
        }
    }

    private void setMngCfg(String[] contentArray, AppMngCfgXml cfg2G, AppMngCfgXml cfg3G, AppMngCfgXml cfg4G) {
        int i;
        int length = contentArray.length;
        char c = 0;
        int i2 = 0;
        while (i2 < length) {
            String content = contentArray[i2];
            if (content == null) {
                i = i2;
            } else {
                String[] contentArraySplit = content.split("\\{");
                if (contentArraySplit.length <= 1) {
                    i = i2;
                } else {
                    String keyString = contentArraySplit[c];
                    String valueString = contentArraySplit[1];
                    if (keyString == null) {
                        i = i2;
                    } else if (valueString == null) {
                        i = i2;
                    } else {
                        String keyString2 = keyString.trim();
                        String valueString2 = valueString.trim();
                        if (keyString2.contains(APPMNG_2G_CFG_KEY)) {
                            i = i2;
                            if (cfg2G.mMemThrd == 0) {
                                setAppMngCfg(valueString2, cfg2G);
                            }
                        } else {
                            i = i2;
                        }
                        if (keyString2.contains(APPMNG_3G_CFG_KEY) && cfg3G.mMemThrd == 0) {
                            setAppMngCfg(valueString2, cfg3G);
                        } else if (keyString2.contains(APPMNG_4G_CFG_KEY) && cfg4G.mMemThrd == 0) {
                            setAppMngCfg(valueString2, cfg4G);
                        }
                    }
                }
            }
            i2 = i + 1;
            c = 0;
        }
    }

    private void updateAppMngCfgFromRms(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, 1);
        if (str != null && str.contains("{") && str.contains("}")) {
            AppMngCfgXml cfg2G = new AppMngCfgXml();
            AppMngCfgXml cfg3G = new AppMngCfgXml();
            AppMngCfgXml cfg4G = new AppMngCfgXml();
            AppMngCfgXml cfgCur = null;
            setMngCfg(str.split("\\}"), cfg2G, cfg3G, cfg4G);
            long memMb = AppMngConfig.getMemorySize();
            if (cfg2G.mMemThrd != 0 && memMb < cfg2G.mMemThrd) {
                cfgCur = cfg2G;
                this.mCfgAdjTypeId = 4;
            } else if (cfg3G.mMemThrd != 0 && memMb < cfg3G.mMemThrd) {
                cfgCur = cfg3G;
                if ((MEM_OPT_FOR_3G & 1) != 0) {
                    cfgCur.mLowEnd = true;
                }
                if ((MEM_OPT_FOR_3G & 2) != 0) {
                    cfgCur.mKillMore = true;
                }
                if ((MEM_OPT_FOR_3G & 4) != 0) {
                    cfgCur.mBgDecay = 120;
                }
                this.mCfgAdjTypeId = 3;
            } else if (cfg4G.mMemThrd != 0) {
                cfgCur = cfg4G;
                this.mCfgAdjTypeId = 2;
            }
            if (cfgCur != null) {
                setConfig(cfgCur);
            }
        }
    }

    private void setConfig(AppMngCfgXml cfgCur) {
        this.mLowEnd = cfgCur.mLowEnd;
        boolean restartFlag = cfgCur.mRestart;
        AppMngConfig.setAbroadFlag(isAbroadArea());
        AppMngConfig.setRestartFlag(restartFlag);
        AppMngConfig.setTopN(cfgCur.mTopNum);
        AppMngConfig.setImCnt(cfgCur.mImCnt);
        AppMngConfig.setSysDecay(cfgCur.mSysDecay);
        AppMngConfig.setKeySysDecay(cfgCur.mKeyDecay);
        AppMngConfig.setAdjCustTopN(cfgCur.mAdjCustTopN);
        AppMngConfig.setBgDecay(cfgCur.mBgDecay);
        AppMngConfig.setPgProtectFlag(cfgCur.mPgProtect);
        AppMngConfig.setAlarmChkFlag(cfgCur.mAlarmChk);
        AppMngConfig.setKillMoreFlag(cfgCur.mKillMore);
        AppMngConfig.setScreenChangedThreshold(cfgCur.mScreenChanged);
        AppMngConfig.setSmartCleanInterval(cfgCur.mSmartCleanInterval);
    }

    public boolean isLowEnd() {
        return this.mLowEnd;
    }

    private void updateAdjWhiteListFromRms(int rmsGroupId) {
        Set<String> adjList = updateListFromRms(rmsGroupId, this.mCfgAdjTypeId);
        if (adjList != null) {
            synchronized (this.mAdjustAdjList) {
                this.mAdjustAdjList.clear();
                this.mAdjustAdjList.addAll(adjList);
            }
        }
    }

    private void updateRestartAppListFromRms(int rmsGroupId) {
        Set<String> adjList = updateListFromRms(rmsGroupId, 6);
        if (adjList != null) {
            synchronized (this.mRestartAppList) {
                this.mRestartAppList.clear();
                this.mRestartAppList.addAll(adjList);
            }
        }
    }

    private void updateBadAppListFromRms(int rmsGroupId) {
        Set<String> adjList = updateListFromRms(rmsGroupId, 7);
        if (adjList != null) {
            synchronized (this.mBadAppList) {
                this.mBadAppList.clear();
                this.mBadAppList.addAll(adjList);
            }
        }
    }

    private Set<String> updateListFromRms(int rmsGroupId, int whiteListType) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, whiteListType);
        if (str != null) {
            ArraySet<String> adjList = new ArraySet<>();
            for (String content : str.split("#")[0].split(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
                String content2 = content.trim();
                if (!content2.isEmpty()) {
                    adjList.add(content2);
                }
            }
            return adjList;
        } else if (!sDebug) {
            return null;
        } else {
            AwareLog.e(TAG, "updateAdjWhiteListFromRms failed because null whiteList!");
            return null;
        }
    }

    private void setCfgParam(AppMngCfgXml cfg2G, String cfgType, int value) {
        if (MEM_THRD_KEY.equals(cfgType)) {
            cfg2G.mMemThrd = (long) value;
        } else if (TOPN_CNT_KEY.equals(cfgType)) {
            cfg2G.mTopNum = value;
        } else if (IM_CNT_KEY.equals(cfgType)) {
            cfg2G.mImCnt = value;
        } else if (SYSPROC_DECAY_KEY.equals(cfgType)) {
            cfg2G.mSysDecay = (long) value;
        } else if (KEYPROC_DECAY_KEY.equals(cfgType)) {
            cfg2G.mKeyDecay = (long) value;
        } else if (ADJCUSTTOP_CNT_KEY.equals(cfgType)) {
            cfg2G.mAdjCustTopN = value;
        } else {
            boolean z = true;
            if (RESTART_KEY.equals(cfgType)) {
                if (value == 0) {
                    z = false;
                }
                cfg2G.mRestart = z;
            } else if (BG_DELAY_KEY.equals(cfgType)) {
                cfg2G.mBgDecay = (long) value;
            } else if (PG_PROTECT_KEY.equals(cfgType)) {
                if (value == 0) {
                    z = false;
                }
                cfg2G.mPgProtect = z;
            } else if (ALARM_CHK_KEY.equals(cfgType)) {
                if (value == 0) {
                    z = false;
                }
                cfg2G.mAlarmChk = z;
            } else if (KILL_MORE_KEY.equals(cfgType)) {
                if (value == 0) {
                    z = false;
                }
                cfg2G.mKillMore = z;
            } else if (LOWEND_KEY.equals(cfgType)) {
                if (value == 0) {
                    z = false;
                }
                cfg2G.mLowEnd = z;
            } else if (SCREEN_CHANGED_KEY.equals(cfgType)) {
                cfg2G.mScreenChanged = value;
            } else if (SMART_CLEAN_INTERVAL_KEY.equals(cfgType)) {
                cfg2G.mSmartCleanInterval = value;
            }
        }
    }

    private void setAppMngCfg(String str, AppMngCfgXml cfg2G) {
        if (!(str == null || cfg2G == null)) {
            String[] contentArray = str.split(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            for (String content : contentArray) {
                if (content != null) {
                    String[] names = content.trim().split(":");
                    if (names.length > 1) {
                        String cfgType = names[0];
                        String cfgValue = names[1];
                        if (!(cfgType == null || cfgValue == null)) {
                            String cfgType2 = cfgType.trim();
                            int value = 0;
                            try {
                                value = Integer.parseInt(cfgValue.trim(), 10);
                            } catch (NumberFormatException e) {
                                AwareLog.e(TAG, "parseInt error");
                            }
                            setCfgParam(cfg2G, cfgType2, value);
                        }
                    }
                }
            }
        }
    }

    private void addPkg(PackageConfigItem item, String pkgName, int value, String processName) {
        if (item == null) {
            item = new PackageConfigItem(pkgName, value);
            this.mAwareProtectMap.put(pkgName, item);
            if (sDebug) {
                AwareLog.i(TAG, "pkgName:" + pkgName + " mGroupId:" + item.groupId + " restart:" + item.restartFlag + " clean:" + item.resCleanAllow + " frequently used:" + item.frequentlyUsed);
            }
        }
        if (processName != null) {
            ProcessConfigItem processItem = new ProcessConfigItem(processName, value);
            item.add(processItem);
            if (sDebug) {
                AwareLog.i(TAG, "processName:" + processName + " mGroupId:" + processItem.groupId + " restart:" + processItem.restartFlag + " clean:" + processItem.resCleanAllow + " frequently used:" + item.frequentlyUsed);
            }
        }
    }

    private void parseAwareProtectList() {
        synchronized (this.mLock) {
            if (this.mAwareProtectList != null) {
                this.mAwareProtectMap.clear();
                Iterator<String> it = this.mAwareProtectList.iterator();
                while (it.hasNext()) {
                    String str = it.next();
                    if (str != null && str.contains("{")) {
                        if (str.contains("}")) {
                            int startIdx = str.indexOf("{");
                            int endIdx = str.indexOf("}");
                            if (startIdx + 1 < endIdx) {
                                if (startIdx + 1 < str.length()) {
                                    String pkgName = str.substring(0, startIdx);
                                    int value = 0;
                                    try {
                                        value = Integer.parseInt(str.substring(startIdx + 1, endIdx), 16);
                                    } catch (NumberFormatException e) {
                                        AwareLog.e(TAG, "parseInt error");
                                    }
                                    String[] names = pkgName.split("#");
                                    String pkgName2 = names[0];
                                    String processName = names.length > 1 ? names[1] : null;
                                    if (!pkgName2.isEmpty()) {
                                        addPkg(this.mAwareProtectMap.get(pkgName2), pkgName2, value, processName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void startHabitObserver() {
        setHabitWhiteList();
        AwareUserHabit habitInstance = AwareUserHabit.getInstance();
        if (habitInstance != null) {
            habitInstance.registHabitProtectListChangeListener(this.mHabitListener);
        }
    }

    private void startXmlObserver() {
        HwSysResManager.getInstance().registerResourceCallback(this.mUpdateWhiteListCallback);
    }

    public Map<String, PackageConfigItem> getAwareProtectMap() {
        if (!this.mEnabled) {
            return null;
        }
        ArrayMap<String, PackageConfigItem> map = new ArrayMap<>();
        setStaticXmlWhiteList();
        synchronized (this.mLock) {
            map.putAll(this.mAwareProtectMap);
        }
        return map;
    }

    public List<String> getKeyHabitAppList() {
        List<String> list = new ArrayList<>();
        if (!this.mEnabled) {
            return list;
        }
        synchronized (this.mLock) {
            list.addAll(this.mKeyHabitAppList);
        }
        return list;
    }

    public List<String> getAllHabitAppList() {
        List<String> list = new ArrayList<>();
        if (!this.mEnabled) {
            return list;
        }
        synchronized (this.mLock) {
            list.addAll(this.mAllHabitAppList);
        }
        return list;
    }

    public void updateKeyImCache(List<String> list) {
        if (list != null) {
            synchronized (this.mLock) {
                this.mKeyHabitAppList.clear();
                int maxCnt = 0;
                Iterator<String> it = list.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    String pkgName = it.next();
                    if (pkgName != null) {
                        this.mKeyHabitAppList.add(pkgName);
                        maxCnt++;
                        if (maxCnt >= AppMngConfig.getImCnt()) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void updateAllImCache(List<String> list) {
        if (list != null) {
            synchronized (this.mLock) {
                this.mAllHabitAppList.clear();
                for (String pkgName : list) {
                    if (pkgName != null) {
                        this.mAllHabitAppList.add(pkgName);
                    }
                }
            }
        }
    }

    private void dumpAwareProtectList(PrintWriter pw) {
        pw.println("dump iAware Protect WhiteList Apps start --------");
        synchronized (this.mLock) {
            Iterator<String> it = this.mAwareProtectList.iterator();
            while (it.hasNext()) {
                pw.println(it.next());
            }
        }
        pw.println("dump iAware Protect WhiteList Apps end-----------");
    }

    private void dumpAdjustAdjList(PrintWriter pw) {
        pw.println("dump iAware Adjust Adj Apps start --------");
        synchronized (this.mAdjustAdjList) {
            Iterator<String> it = this.mAdjustAdjList.iterator();
            while (it.hasNext()) {
                pw.println(it.next());
            }
        }
        pw.println("dump iAware Adjust Adj Apps end-----------");
    }

    private void dumpHabitFrequentUsed(PrintWriter pw) {
        pw.println("dump User Habit Frequent Used start-----------");
        synchronized (this.mHabitFrequentUsed) {
            Iterator<String> it = this.mHabitFrequentUsed.iterator();
            while (it.hasNext()) {
                pw.println(it.next());
            }
        }
        pw.println("dump User Habit Frequent Used end-----------");
    }

    private void dumpKeyHabitAppList(PrintWriter pw) {
        pw.println("dump User Habit WhiteList Apps start ------------");
        synchronized (this.mLock) {
            int size = this.mKeyHabitAppList.size();
            for (int i = 0; i < size; i++) {
                pw.println(this.mKeyHabitAppList.get(i));
            }
        }
        pw.println("dump User Habit WhiteList Apps end --------------");
    }

    private void dumpAllHabitAppList(PrintWriter pw) {
        pw.println("dump User All Habit WhiteList Apps start ------------");
        synchronized (this.mLock) {
            int size = this.mAllHabitAppList.size();
            for (int i = 0; i < size; i++) {
                pw.println(this.mAllHabitAppList.get(i));
            }
        }
        pw.println("dump User All Habit WhiteList Apps end --------------");
    }

    private void dumpRestartAppList(PrintWriter pw) {
        pw.println("dump iAware Restart Apps start --------");
        synchronized (this.mRestartAppList) {
            Iterator<String> it = this.mRestartAppList.iterator();
            while (it.hasNext()) {
                pw.println(it.next());
            }
        }
        pw.println("dump iAware Restart Apps end-----------");
    }

    private void dumpBadAppList(PrintWriter pw) {
        pw.println("dump iAware Bad Apps start --------");
        synchronized (this.mBadAppList) {
            Iterator<String> it = this.mBadAppList.iterator();
            while (it.hasNext()) {
                pw.println(it.next());
            }
        }
        pw.println("dump iAware Bad Apps end-----------");
    }

    public void dump(FileDescriptor fd, PrintWriter pw) {
        if (pw != null) {
            if (!this.mEnabled) {
                pw.println("WhiteList feature not enabled.");
                return;
            }
            dumpAwareProtectList(pw);
            dumpAdjustAdjList(pw);
            dumpHabitFrequentUsed(pw);
            dumpKeyHabitAppList(pw);
            dumpAllHabitAppList(pw);
            dumpRestartAppList(pw);
            dumpBadAppList(pw);
            pw.println("dump AppMng Config start ------------");
            dumpCfg(pw);
            pw.println("dump AppMng Configs end ------------");
        }
    }

    private void dumpCfg(PrintWriter pw) {
        pw.println("memMB:" + AppMngConfig.getMemorySize());
        pw.println("topN:" + AppMngConfig.getTopN());
        pw.println("imCnt:" + AppMngConfig.getImCnt());
        pw.println("sysDecay:" + AppMngConfig.getSysDecay());
        pw.println("keySysDecay:" + AppMngConfig.getKeySysDecay());
        pw.println("adjCustTopN:" + AppMngConfig.getAdjCustTopN());
        pw.println("restart:" + AppMngConfig.getRestartFlag());
        pw.println("abroad:" + AppMngConfig.getAbroadFlag());
        pw.println("bgDecayMinute:" + AppMngConfig.getBgDecay());
        pw.println("pgProtectEn:" + AppMngConfig.getPgProtectFlag());
        pw.println("alarmChk:" + AppMngConfig.getAlarmCheckFlag());
        pw.println("killMore:" + AppMngConfig.getKillMoreFlag());
        pw.println("lowEnd:" + this.mLowEnd);
    }

    public void fillMostFrequentUsedApp(List<String> list) {
        if (this.mEnabled) {
            ArrayList arrayList = new ArrayList();
            if (list != null) {
                arrayList.addAll(list);
            }
            List<String> setAdjPkg = new ArrayList<>();
            setAdjPkg.addAll(arrayList);
            synchronized (this.mAdjustAdjList) {
                setAdjPkg.addAll(this.mAdjustAdjList);
            }
            HwActivityManager.setAndRestoreMaxAdjIfNeed(setAdjPkg);
            synchronized (this.mHabitFrequentUsed) {
                this.mHabitFrequentUsed.clear();
                this.mHabitFrequentUsed.addAll(arrayList);
            }
        }
    }

    public boolean isAppMngOomAdjCustomized(String pkg) {
        if (!this.mEnabled || pkg == null) {
            return false;
        }
        synchronized (this.mAdjustAdjList) {
            if (this.mAdjustAdjList.contains(pkg)) {
                return true;
            }
        }
        synchronized (this.mHabitFrequentUsed) {
            if (this.mHabitFrequentUsed.contains(pkg)) {
                return true;
            }
            return false;
        }
    }

    public Set<String> getRestartAppList() {
        ArraySet arraySet;
        synchronized (this.mRestartAppList) {
            arraySet = new ArraySet((ArraySet) this.mRestartAppList);
        }
        return arraySet;
    }

    public Set<String> getBadAppList() {
        ArraySet arraySet;
        synchronized (this.mBadAppList) {
            arraySet = new ArraySet((ArraySet) this.mBadAppList);
        }
        return arraySet;
    }

    public static boolean isAbroadArea() {
        return !SystemPropertiesEx.get("ro.config.hw_optb", "0").equals(OPTB_CHINA);
    }
}
