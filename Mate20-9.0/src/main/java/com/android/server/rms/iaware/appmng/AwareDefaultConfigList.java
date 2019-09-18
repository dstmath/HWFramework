package com.android.server.rms.iaware.appmng;

import android.content.Context;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.rms.HwSysResManager;
import android.rms.IUpdateWhiteListCallback;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.algorithm.AwareUserHabitAlgorithm;
import com.huawei.android.app.HwActivityManager;
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
    private static final int APPMNG_BADAPP_TYPE = 7;
    private static final int APPMNG_CFG_ADJ_2G_TYPE = 4;
    private static final int APPMNG_CFG_ADJ_3G_TYPE = 3;
    private static final int APPMNG_CFG_ADJ_TYPE = 2;
    private static final int APPMNG_CFG_WHITE_TYPE = 1;
    private static final int APPMNG_LOWEND_PROTECTED_ID = 5;
    private static final int APPMNG_RESTARTAPP_TYPE = 6;
    private static final int BG_DECAY_TWO_HOUR_MASK = 4;
    private static final String BG_DELAY_KEY = "BGDCY";
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    public static final int GROUP_ID_FORBIDSTOP = 1;
    public static final int GROUP_ID_NOSPEC = 0;
    public static final int GROUP_ID_SHORTAGESTOP = 2;
    private static final int HABIT_PROT_MAX_CNT = 10000;
    public static final int HW_PERCEPTIBLE_APP_ADJ = 260;
    private static final String IM_CNT_KEY = "IM_CNT";
    private static final String KEYPROC_DECAY_KEY = "KEYPROC_DECAY";
    private static final String KILL_MORE_KEY = "KM";
    private static final int KILL_MORE_MASK = 2;
    private static final String LOWEND_KEY = "LOW";
    private static final int LOW_END_MASK = 1;
    private static final int MASK_CLEANRES = 1;
    private static final int MASK_FREQUENTLYUSED = 4;
    private static final int MASK_GROUP = 3840;
    private static final int MASK_RESTART = 2;
    private static final int MEM_OPT_FOR_3G = SystemProperties.getInt("sys.iaware.mem_opt", 0);
    private static final String MEM_THRD_KEY = "MEM_THRD";
    private static final String PG_PROTECT_KEY = "PROT";
    private static final String RESTART_KEY = "RESTART";
    private static final String SCREEN_CHANGED_KEY = "screenChanged";
    private static final String SEPARATOR = "#";
    private static final String SMART_CLEAN_INTERVAL_KEY = "smartCleanInterval";
    private static final String SYSPROC_DECAY_KEY = "SYSPROC_DECAY";
    private static final String TAG = "AwareDefaultConfigList";
    private static final String TOPN_CNT_KEY = "TOPN_CNT";
    private static AwareDefaultConfigList sInstance = null;
    private final ArraySet<String> mAdjustAdjList = new ArraySet<>();
    private final ArrayList<String> mAllHabitAppList = new ArrayList<>();
    /* access modifiers changed from: private */
    public final ArraySet<String> mAwareProtectList = new ArraySet<>();
    private final ArrayMap<String, PackageConfigItem> mAwareProtectMap = new ArrayMap<>();
    private final ArraySet<String> mBadAppList = new ArraySet<>();
    private int mCfgAdjTypeId = 4;
    private Context mContext;
    private boolean mEnabled = false;
    private final ArraySet<String> mHabitFrequentUsed = new ArraySet<>();
    private AwareUserHabitAlgorithm.HabitProtectListChangeListener mHabitListener = new AwareUserHabitAlgorithm.HabitProtectListChangeListener() {
        public void onListChanged() {
            AwareDefaultConfigList.this.setHabitWhiteList();
        }
    };
    /* access modifiers changed from: private */
    public boolean mHasReadXml = false;
    private final ArrayList<String> mKeyHabitAppList = new ArrayList<>();
    private boolean mLowEnd = false;
    private final ArraySet<String> mRestartAppList = new ArraySet<>();
    private IUpdateWhiteListCallback mUpdateWhiteListCallback = new IUpdateWhiteListCallback.Stub() {
        public void update() throws RemoteException {
            if (AwareDefaultConfigList.DEBUG) {
                AwareLog.d(AwareDefaultConfigList.TAG, "IUpdateWhiteListCallback update whiteList.");
            }
            synchronized (AwareDefaultConfigList.this) {
                boolean unused = AwareDefaultConfigList.this.mHasReadXml = false;
                AwareDefaultConfigList.this.mAwareProtectList.clear();
            }
            AwareDefaultConfigList.this.setStaticXmlWhiteList();
        }
    };

    static class AppMngCfgXml {
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
        int mTopNCnt = 0;

        AppMngCfgXml() {
        }
    }

    public static class PackageConfigItem extends ProcessConfigItem {
        ArrayMap<String, ProcessConfigItem> mProcessMap = new ArrayMap<>();

        public PackageConfigItem(String name, int value) {
            super(name, value);
        }

        public void add(ProcessConfigItem item) {
            if (item != null) {
                this.mProcessMap.put(item.mName, item);
            }
        }

        public boolean isEmpty() {
            return this.mProcessMap.isEmpty();
        }

        public ProcessConfigItem getItem(String processName) {
            return this.mProcessMap.get(processName);
        }
    }

    public static class ProcessConfigItem {
        public boolean mFrequentlyUsed;
        public int mGroupId;
        public String mName;
        public boolean mResCleanAllow;
        public boolean mRestartFlag;

        private ProcessConfigItem() {
        }

        public ProcessConfigItem(String name, int value) {
            this.mName = name;
            boolean z = false;
            this.mResCleanAllow = (value & 1) != 0;
            this.mRestartFlag = (value & 2) != 0;
            this.mFrequentlyUsed = (value & 4) != 0 ? true : z;
            this.mGroupId = ((value & 3840) >> 8) & 15;
        }

        public ProcessConfigItem copy() {
            ProcessConfigItem dst = new ProcessConfigItem();
            dst.mName = this.mName;
            dst.mResCleanAllow = this.mResCleanAllow;
            dst.mRestartFlag = this.mRestartFlag;
            dst.mFrequentlyUsed = this.mFrequentlyUsed;
            dst.mGroupId = this.mGroupId;
            return dst;
        }
    }

    private AwareDefaultConfigList() {
    }

    private void initialize(Context context) {
        this.mContext = context;
        this.mEnabled = true;
        setAllWhiteList();
        startObserver();
        if (this.mLowEnd) {
            AwareUserHabit usrhabit = AwareUserHabit.getInstance();
            if (usrhabit != null) {
                usrhabit.setLowEndFlag(true);
            }
        }
    }

    private void deInitialize() {
        AwareUserHabit habitInstance = AwareUserHabit.getInstance();
        if (habitInstance != null) {
            habitInstance.unregistHabitProtectListChangeListener(this.mHabitListener);
        }
        this.mEnabled = false;
        synchronized (this) {
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
        if (DEBUG) {
            AwareLog.d(TAG, "WhiteList Feature enable!!!");
        }
        getInstance().initialize(context);
    }

    public static void disable() {
        if (DEBUG) {
            AwareLog.d(TAG, "WhiteList Feature disable!!!");
        }
        getInstance().deInitialize();
    }

    private void startObserver() {
        if (DEBUG) {
            AwareLog.d(TAG, "WhiteList Feature startObserver!!!");
        }
        startXmlObserver();
        startHabitObserver();
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }

    public static AwareDefaultConfigList getInstance() {
        AwareDefaultConfigList awareDefaultConfigList;
        synchronized (AwareDefaultConfigList.class) {
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
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001d, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r2.mAwareProtectList.addAll(r0);
        parseAwareProtectList();
        r2.mHasReadXml = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0029, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0008, code lost:
        updateAppMngCfgFromRMS(28);
        updateAdjWhiteListFromRMS(28);
        updateRestartAppListFromRMS(28);
        updateBadAppListFromRMS(28);
        r0 = getWhiteListFromRMS(28);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001a, code lost:
        if (r0 != null) goto L_0x001d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
        return;
     */
    public void setStaticXmlWhiteList() {
        synchronized (this) {
            if (this.mHasReadXml) {
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
        boolean z = true;
        if (appType != 255) {
            switch (appType) {
                case -1:
                case 0:
                case 1:
                    break;
                default:
                    if (appType <= 255) {
                        z = false;
                    }
                    return z;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void setHabitWhiteList() {
        List<String> habitProtectList;
        AwareUserHabit habitInstance = AwareUserHabit.getInstance();
        if (habitInstance != null) {
            List<String> gcmList = getGcmAppList(habitInstance);
            List<String> habitProtectListAll = habitInstance.getHabitProtectListAll(10000, 10000);
            if (DEBUG) {
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
            if (DEBUG) {
                AwareLog.i(TAG, "HabitListChangeListener onListChanged list:" + keyImList);
            }
            updateKeyImCache(keyImList);
        }
    }

    private ArraySet<String> getWhiteListFromRMS(int rmsGroupId) {
        String str;
        ArraySet<String> whiteList = new ArraySet<>();
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (this.mLowEnd) {
            str = resManager.getWhiteList(rmsGroupId, 5);
        } else {
            str = resManager.getWhiteList(rmsGroupId, 0);
        }
        if (str == null) {
            if (DEBUG) {
                AwareLog.e(TAG, "getWhiteListFromRMS failed because null whiteList!");
            }
            return null;
        }
        for (String content : str.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
            String content2 = content.trim();
            if (!content2.isEmpty()) {
                whiteList.add(content2);
            }
        }
        return whiteList;
    }

    private void updateAppMngCfgFromRMS(int rmsGroupId) {
        HwSysResManager resManager;
        HwSysResManager resManager2 = HwSysResManager.getInstance();
        int i = 1;
        String str = resManager2.getWhiteList(rmsGroupId, 1);
        if (str == null || !str.contains("{")) {
        } else if (!str.contains("}")) {
            HwSysResManager hwSysResManager = resManager2;
        } else {
            AppMngCfgXml cfg2G = new AppMngCfgXml();
            AppMngCfgXml cfg3G = new AppMngCfgXml();
            AppMngCfgXml cfg4G = new AppMngCfgXml();
            AppMngCfgXml cfgCur = null;
            String[] contentArray = str.split("\\}");
            int length = contentArray.length;
            char c = 0;
            int i2 = 0;
            while (i2 < length) {
                String content = contentArray[i2];
                if (content != null) {
                    String[] contentArraySplit = content.split("\\{");
                    if (contentArraySplit.length > i) {
                        String keyString = contentArraySplit[c];
                        String valueString = contentArraySplit[i];
                        if (keyString == null) {
                            resManager = resManager2;
                        } else if (valueString != null) {
                            String keyString2 = keyString.trim();
                            String valueString2 = valueString.trim();
                            if (keyString2.contains(APPMNG_2G_CFG_KEY)) {
                                resManager = resManager2;
                                if (cfg2G.mMemThrd == 0) {
                                    setAppMngCfg(valueString2, cfg2G);
                                }
                            } else {
                                resManager = resManager2;
                            }
                            if (keyString2.contains(APPMNG_3G_CFG_KEY) && cfg3G.mMemThrd == 0) {
                                setAppMngCfg(valueString2, cfg3G);
                            } else if (keyString2.contains(APPMNG_4G_CFG_KEY) && cfg4G.mMemThrd == 0) {
                                setAppMngCfg(valueString2, cfg4G);
                            }
                        }
                        i2++;
                        resManager2 = resManager;
                        i = 1;
                        c = 0;
                    }
                }
                resManager = resManager2;
                i2++;
                resManager2 = resManager;
                i = 1;
                c = 0;
            }
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
                this.mLowEnd = cfgCur.mLowEnd;
                boolean restartFlag = cfgCur.mRestart;
                AppMngConfig.setAbroadFlag(isAbroadArea());
                AppMngConfig.setRestartFlag(restartFlag);
                AppMngConfig.setTopN(cfgCur.mTopNCnt);
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
        }
    }

    public boolean isLowEnd() {
        return this.mLowEnd;
    }

    private void updateAdjWhiteListFromRMS(int rmsGroupId) {
        Set<String> adjList = updateListFromRMS(rmsGroupId, this.mCfgAdjTypeId);
        if (adjList != null) {
            synchronized (this.mAdjustAdjList) {
                this.mAdjustAdjList.clear();
                this.mAdjustAdjList.addAll(adjList);
            }
        }
    }

    private void updateRestartAppListFromRMS(int rmsGroupId) {
        Set<String> adjList = updateListFromRMS(rmsGroupId, 6);
        if (adjList != null) {
            synchronized (this.mRestartAppList) {
                this.mRestartAppList.clear();
                this.mRestartAppList.addAll(adjList);
            }
        }
    }

    private void updateBadAppListFromRMS(int rmsGroupId) {
        Set<String> adjList = updateListFromRMS(rmsGroupId, 7);
        if (adjList != null) {
            synchronized (this.mBadAppList) {
                this.mBadAppList.clear();
                this.mBadAppList.addAll(adjList);
            }
        }
    }

    private Set<String> updateListFromRMS(int rmsGroupId, int whiteListType) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, whiteListType);
        if (str == null) {
            if (DEBUG) {
                AwareLog.e(TAG, "updateAdjWhiteListFromRMS failed because null whiteList!");
            }
            return null;
        }
        ArraySet<String> adjList = new ArraySet<>();
        for (String content : str.split("#")[0].split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
            String content2 = content.trim();
            if (!content2.isEmpty()) {
                adjList.add(content2);
            }
        }
        return adjList;
    }

    private void setCfgParam(AppMngCfgXml cfg2G, String cfgType, int value) {
        if (MEM_THRD_KEY.equals(cfgType)) {
            cfg2G.mMemThrd = (long) value;
        } else if (TOPN_CNT_KEY.equals(cfgType)) {
            cfg2G.mTopNCnt = value;
        } else if (IM_CNT_KEY.equals(cfgType)) {
            cfg2G.mImCnt = value;
        } else if (SYSPROC_DECAY_KEY.equals(cfgType)) {
            cfg2G.mSysDecay = (long) value;
        } else if (KEYPROC_DECAY_KEY.equals(cfgType)) {
            cfg2G.mKeyDecay = (long) value;
        } else if (ADJCUSTTOP_CNT_KEY.equals(cfgType)) {
            cfg2G.mAdjCustTopN = value;
        } else {
            boolean z = false;
            if (RESTART_KEY.equals(cfgType)) {
                if (value != 0) {
                    z = true;
                }
                cfg2G.mRestart = z;
            } else if (BG_DELAY_KEY.equals(cfgType)) {
                cfg2G.mBgDecay = (long) value;
            } else if (PG_PROTECT_KEY.equals(cfgType)) {
                if (value != 0) {
                    z = true;
                }
                cfg2G.mPgProtect = z;
            } else if (ALARM_CHK_KEY.equals(cfgType)) {
                if (value != 0) {
                    z = true;
                }
                cfg2G.mAlarmChk = z;
            } else if (KILL_MORE_KEY.equals(cfgType)) {
                if (value != 0) {
                    z = true;
                }
                cfg2G.mKillMore = z;
            } else if (LOWEND_KEY.equals(cfgType)) {
                if (value != 0) {
                    z = true;
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
        if (str != null && cfg2G != null) {
            for (String content : str.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
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

    private void parseAwareProtectList() {
        synchronized (this) {
            if (this.mAwareProtectList != null) {
                this.mAwareProtectMap.clear();
                int value = 0;
                Iterator<String> it = this.mAwareProtectList.iterator();
                while (it.hasNext()) {
                    String str = it.next();
                    if (str != null && str.contains("{")) {
                        if (str.contains("}")) {
                            int startIdx = str.indexOf("{");
                            int endIdx = str.indexOf("}");
                            if (startIdx + 1 >= endIdx) {
                                continue;
                            } else if (startIdx + 1 < str.length()) {
                                String pkgName = str.substring(0, startIdx);
                                try {
                                    value = Integer.parseInt(str.substring(startIdx + 1, endIdx), 16);
                                } catch (NumberFormatException e) {
                                    AwareLog.e(TAG, "parseInt error");
                                }
                                String[] names = pkgName.split("#");
                                String pkgName2 = names[0];
                                String processName = names.length > 1 ? names[1] : null;
                                if (!pkgName2.isEmpty()) {
                                    PackageConfigItem item = this.mAwareProtectMap.get(pkgName2);
                                    if (item == null) {
                                        item = new PackageConfigItem(pkgName2, value);
                                        this.mAwareProtectMap.put(pkgName2, item);
                                        if (DEBUG) {
                                            AwareLog.i(TAG, "pkgName:" + pkgName2 + " mGroupId:" + item.mGroupId + " restart:" + item.mRestartFlag + " clean:" + item.mResCleanAllow + " frequently used:" + item.mFrequentlyUsed);
                                        }
                                    }
                                    if (processName != null) {
                                        ProcessConfigItem dItem = new ProcessConfigItem(processName, value);
                                        item.add(dItem);
                                        if (DEBUG) {
                                            AwareLog.i(TAG, "processName:" + processName + " mGroupId:" + dItem.mGroupId + " restart:" + dItem.mRestartFlag + " clean:" + dItem.mResCleanAllow + " frequently used:" + item.mFrequentlyUsed);
                                        }
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
        synchronized (this) {
            map.putAll(this.mAwareProtectMap);
        }
        return map;
    }

    public List<String> getKeyHabitAppList() {
        List<String> list = new ArrayList<>();
        if (!this.mEnabled) {
            return list;
        }
        synchronized (this) {
            list.addAll(this.mKeyHabitAppList);
        }
        return list;
    }

    public List<String> getAllHabitAppList() {
        List<String> list = new ArrayList<>();
        if (!this.mEnabled) {
            return list;
        }
        synchronized (this) {
            list.addAll(this.mAllHabitAppList);
        }
        return list;
    }

    public void updateKeyImCache(List<String> list) {
        if (list != null) {
            synchronized (this) {
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
            synchronized (this) {
                this.mAllHabitAppList.clear();
                for (String pkgName : list) {
                    if (pkgName != null) {
                        this.mAllHabitAppList.add(pkgName);
                    }
                }
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw) {
        if (pw != null) {
            if (!this.mEnabled) {
                pw.println("WhiteList feature not enabled.");
                return;
            }
            pw.println("dump iAware Protect WhiteList Apps start --------");
            synchronized (this) {
                Iterator<String> it = this.mAwareProtectList.iterator();
                while (it.hasNext()) {
                    pw.println(it.next());
                }
            }
            pw.println("dump iAware Protect WhiteList Apps end-----------");
            pw.println("dump iAware Adjust Adj Apps start --------");
            synchronized (this.mAdjustAdjList) {
                Iterator<String> it2 = this.mAdjustAdjList.iterator();
                while (it2.hasNext()) {
                    pw.println(it2.next());
                }
            }
            pw.println("dump iAware Adjust Adj Apps end-----------");
            pw.println("dump User Habit Frequent Used start-----------");
            synchronized (this.mHabitFrequentUsed) {
                Iterator<String> it3 = this.mHabitFrequentUsed.iterator();
                while (it3.hasNext()) {
                    pw.println(it3.next());
                }
            }
            pw.println("dump User Habit Frequent Used end-----------");
            pw.println("dump User Habit WhiteList Apps start ------------");
            synchronized (this) {
                int size = this.mKeyHabitAppList.size();
                for (int i = 0; i < size; i++) {
                    pw.println(this.mKeyHabitAppList.get(i));
                }
            }
            pw.println("dump User Habit WhiteList Apps end --------------");
            pw.println("dump User All Habit WhiteList Apps start ------------");
            synchronized (this) {
                int size2 = this.mAllHabitAppList.size();
                for (int i2 = 0; i2 < size2; i2++) {
                    pw.println(this.mAllHabitAppList.get(i2));
                }
            }
            pw.println("dump User All Habit WhiteList Apps end --------------");
            pw.println("dump iAware Restart Apps start --------");
            synchronized (this.mRestartAppList) {
                Iterator<String> it4 = this.mRestartAppList.iterator();
                while (it4.hasNext()) {
                    pw.println(it4.next());
                }
            }
            pw.println("dump iAware Restart Apps end-----------");
            pw.println("dump iAware Bad Apps start --------");
            synchronized (this.mBadAppList) {
                Iterator<String> it5 = this.mBadAppList.iterator();
                while (it5.hasNext()) {
                    pw.println(it5.next());
                }
            }
            pw.println("dump iAware Bad Apps end-----------");
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
            List<String> listHabit = new ArrayList<>();
            if (list != null) {
                listHabit.addAll(list);
            }
            List<String> setAdjPkg = new ArrayList<>();
            setAdjPkg.addAll(listHabit);
            synchronized (this.mAdjustAdjList) {
                setAdjPkg.addAll(this.mAdjustAdjList);
            }
            HwActivityManager.setAndRestoreMaxAdjIfNeed(setAdjPkg);
            synchronized (this.mHabitFrequentUsed) {
                this.mHabitFrequentUsed.clear();
                this.mHabitFrequentUsed.addAll(listHabit);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0018, code lost:
        r2 = r4.mHabitFrequentUsed;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001a, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0021, code lost:
        if (r4.mHabitFrequentUsed.contains(r5) == false) goto L_0x0025;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0023, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0024, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0025, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0026, code lost:
        return false;
     */
    public boolean isAppMngOomAdjCustomized(String pkg) {
        if (!this.mEnabled || pkg == null) {
            return false;
        }
        synchronized (this.mAdjustAdjList) {
            if (this.mAdjustAdjList.contains(pkg)) {
                return true;
            }
        }
    }

    public Set<String> getRestartAppList() {
        ArraySet arraySet;
        synchronized (this.mRestartAppList) {
            arraySet = new ArraySet(this.mRestartAppList);
        }
        return arraySet;
    }

    public Set<String> getBadAppList() {
        ArraySet arraySet;
        synchronized (this.mBadAppList) {
            arraySet = new ArraySet(this.mBadAppList);
        }
        return arraySet;
    }

    public static boolean isAbroadArea() {
        return !SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    }
}
