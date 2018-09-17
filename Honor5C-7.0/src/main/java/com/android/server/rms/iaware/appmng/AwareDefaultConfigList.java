package com.android.server.rms.iaware.appmng;

import android.content.Context;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.rms.HwSysResManager;
import android.rms.IUpdateWhiteListCallback;
import android.rms.IUpdateWhiteListCallback.Stub;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.PPPOEStateMachine;
import com.android.server.am.HwActivityManagerService;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.algorithm.AwareUserHabitAlgorithm.HabitProtectListChangeListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AwareDefaultConfigList {
    private static final String ADJCUSTTOP_CNT_KEY = "ADJCUSTTOP_CNT";
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
    private static boolean DEBUG = false;
    private static final int DELAY_DAYS = 2;
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
    private static final int MEM_OPT_FOR_3G = 0;
    private static final String MEM_THRD_KEY = "MEM_THRD";
    private static final String RESTART_KEY = "RESTART";
    private static final String SEPARATOR = "#";
    private static final String SYSPROC_DECAY_KEY = "SYSPROC_DECAY";
    private static final String TAG = "AwareDefaultConfigList";
    private static final String TOPN_CNT_KEY = "TOPN_CNT";
    private static AwareDefaultConfigList sInstance;
    private final ArraySet<String> mAdjustAdjList;
    private final ArrayList<String> mAllHabitAppList;
    private final ArraySet<String> mAwareProtectList;
    private final ArrayMap<String, PackageConfigItem> mAwareProtectMap;
    private final ArraySet<String> mBadAppList;
    private int mCfgAdjTypeId;
    private Context mContext;
    private boolean mEnabled;
    private final ArraySet<String> mHabitFrequentUsed;
    private HabitProtectListChangeListener mHabitListener;
    private boolean mHasReadXml;
    private final ArrayList<String> mKeyHabitAppList;
    private boolean mLowEnd;
    private final ArraySet<String> mRestartAppList;
    private IUpdateWhiteListCallback mUpdateWhiteListCallback;

    static class AppMngCfgXml {
        int mAdjCustTopN;
        long mBgDecay;
        int mImCnt;
        long mKeyDecay;
        boolean mKillMore;
        boolean mLowEnd;
        long mMemThrd;
        boolean mRestart;
        long mSysDecay;
        int mTopNCnt;

        AppMngCfgXml() {
            this.mMemThrd = 0;
            this.mTopNCnt = AwareDefaultConfigList.MEM_OPT_FOR_3G;
            this.mImCnt = AwareDefaultConfigList.MEM_OPT_FOR_3G;
            this.mSysDecay = 0;
            this.mKeyDecay = 0;
            this.mAdjCustTopN = AwareDefaultConfigList.MEM_OPT_FOR_3G;
            this.mRestart = true;
            this.mBgDecay = 7200;
            this.mKillMore = false;
            this.mLowEnd = false;
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
            boolean z;
            boolean z2 = true;
            this.mName = name;
            if ((value & AwareDefaultConfigList.MASK_CLEANRES) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mResCleanAllow = z;
            if ((value & AwareDefaultConfigList.MASK_RESTART) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mRestartFlag = z;
            if ((value & AwareDefaultConfigList.MASK_FREQUENTLYUSED) == 0) {
                z2 = false;
            }
            this.mFrequentlyUsed = z2;
            this.mGroupId = ((value & AwareDefaultConfigList.MASK_GROUP) >> 8) & 15;
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

    public static class PackageConfigItem extends ProcessConfigItem {
        ArrayMap<String, ProcessConfigItem> mProcessMap;

        public PackageConfigItem(String name, int value) {
            super(name, value);
            this.mProcessMap = new ArrayMap();
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
            if (processName == null) {
                return null;
            }
            return (ProcessConfigItem) this.mProcessMap.get(processName);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.appmng.AwareDefaultConfigList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.appmng.AwareDefaultConfigList.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.appmng.AwareDefaultConfigList.<clinit>():void");
    }

    private AwareDefaultConfigList() {
        this.mEnabled = false;
        this.mHasReadXml = false;
        this.mCfgAdjTypeId = MASK_FREQUENTLYUSED;
        this.mAwareProtectList = new ArraySet();
        this.mAwareProtectMap = new ArrayMap();
        this.mKeyHabitAppList = new ArrayList();
        this.mAllHabitAppList = new ArrayList();
        this.mHabitFrequentUsed = new ArraySet();
        this.mAdjustAdjList = new ArraySet();
        this.mRestartAppList = new ArraySet();
        this.mBadAppList = new ArraySet();
        this.mLowEnd = false;
        this.mHabitListener = new HabitProtectListChangeListener() {
            public void onListChanged() {
                AwareDefaultConfigList.this.setHabitWhiteList();
            }
        };
        this.mUpdateWhiteListCallback = new Stub() {
            public void update() throws RemoteException {
                if (AwareDefaultConfigList.DEBUG) {
                    AwareLog.d(AwareDefaultConfigList.TAG, "IUpdateWhiteListCallback update whiteList.");
                }
                synchronized (AwareDefaultConfigList.this) {
                    AwareDefaultConfigList.this.mHasReadXml = false;
                    AwareDefaultConfigList.this.mAwareProtectList.clear();
                }
                AwareDefaultConfigList.this.setStaticXmlWhiteList();
            }
        };
    }

    private void initialize(Context context) {
        this.mContext = context;
        this.mEnabled = true;
        setAllWhiteList();
        startObserver();
        if (this.mLowEnd) {
            AwareUserHabit usrhabit = AwareUserHabit.getInstance();
            if (usrhabit != null) {
                usrhabit.setStayInBackgroudTime(2);
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

    private void setStaticXmlWhiteList() {
        synchronized (this) {
            if (this.mHasReadXml) {
                return;
            }
            updateAppMngCfgFromRMS(34);
            updateAdjWhiteListFromRMS(34);
            updateRestartAppListFromRMS(34);
            updateBadAppListFromRMS(34);
            ArraySet<String> awareList = getWhiteListFromRMS(34);
            if (awareList != null) {
                synchronized (this) {
                    this.mAwareProtectList.addAll(awareList);
                    parseAwareProtectList();
                    this.mHasReadXml = true;
                }
            }
        }
    }

    private void setHabitWhiteList() {
        AwareUserHabit habitInstance = AwareUserHabit.getInstance();
        if (habitInstance != null) {
            List<String> habitProtectList;
            List<String> gcmList = habitInstance.getGCMAppList();
            List<String> habitProtectListAll = habitInstance.getHabitProtectListAll(HABIT_PROT_MAX_CNT, HABIT_PROT_MAX_CNT);
            if (DEBUG) {
                AwareLog.i(TAG, "AllHabitListChangeListener onListChanged list:" + habitProtectListAll);
            }
            if (habitProtectListAll != null) {
                if (gcmList != null) {
                    habitProtectListAll.addAll(gcmList);
                }
                updateAllImCache(habitProtectListAll);
            }
            if (this.mLowEnd) {
                int emailCnt = AppMngConfig.getImCnt() / MASK_RESTART;
                habitProtectList = habitInstance.queryHabitProtectAppList(AppMngConfig.getImCnt() - emailCnt, emailCnt);
            } else {
                habitProtectList = habitInstance.getHabitProtectList(HABIT_PROT_MAX_CNT, HABIT_PROT_MAX_CNT);
            }
            List<String> keyImList = new ArrayList();
            if (habitProtectList != null) {
                keyImList.addAll(habitProtectList);
            }
            if (!(gcmList == null || this.mLowEnd)) {
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
        int i = MEM_OPT_FOR_3G;
        ArraySet<String> whiteList = new ArraySet();
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (this.mLowEnd) {
            str = resManager.getWhiteList(rmsGroupId, APPMNG_LOWEND_PROTECTED_ID);
        } else {
            str = resManager.getWhiteList(rmsGroupId, MEM_OPT_FOR_3G);
        }
        if (str == null) {
            if (DEBUG) {
                AwareLog.e(TAG, "getWhiteListFromRMS failed because null whiteList!");
            }
            return null;
        }
        String[] contentArray = str.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        int length = contentArray.length;
        while (i < length) {
            String content = contentArray[i].trim();
            if (!content.isEmpty()) {
                whiteList.add(content);
            }
            i += MASK_CLEANRES;
        }
        return whiteList;
    }

    private void updateAppMngCfgFromRMS(int rmsGroupId) {
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, MASK_CLEANRES);
        if (str != null) {
            if (str.contains("{")) {
                if (str.contains("}")) {
                    AppMngCfgXml cfg2G = new AppMngCfgXml();
                    AppMngCfgXml cfg3G = new AppMngCfgXml();
                    AppMngCfgXml cfg4G = new AppMngCfgXml();
                    AppMngCfgXml appMngCfgXml = null;
                    String[] contentArray = str.split("\\}");
                    int length = contentArray.length;
                    for (int i = MEM_OPT_FOR_3G; i < length; i += MASK_CLEANRES) {
                        String content = contentArray[i];
                        if (content != null) {
                            String[] contentArraySplit = content.split("\\{");
                            int length2 = contentArraySplit.length;
                            if (r0 > MASK_CLEANRES) {
                                String keyString = contentArraySplit[MEM_OPT_FOR_3G];
                                String valueString = contentArraySplit[MASK_CLEANRES];
                                if (!(keyString == null || valueString == null)) {
                                    keyString = keyString.trim();
                                    valueString = valueString.trim();
                                    if (keyString.contains(APPMNG_2G_CFG_KEY)) {
                                        if (cfg2G.mMemThrd == 0) {
                                            setAppMngCfg(valueString, cfg2G);
                                        }
                                    }
                                    if (keyString.contains(APPMNG_3G_CFG_KEY)) {
                                        if (cfg3G.mMemThrd == 0) {
                                            setAppMngCfg(valueString, cfg3G);
                                        }
                                    }
                                    if (keyString.contains(APPMNG_4G_CFG_KEY)) {
                                        if (cfg4G.mMemThrd == 0) {
                                            setAppMngCfg(valueString, cfg4G);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    long memMb = AppMngConfig.getMemorySize();
                    if (cfg2G.mMemThrd != 0) {
                        if (memMb < cfg2G.mMemThrd) {
                            appMngCfgXml = cfg2G;
                            this.mCfgAdjTypeId = MASK_FREQUENTLYUSED;
                            if (appMngCfgXml == null) {
                                AppMngConfig.setRestartFlag(appMngCfgXml.mRestart);
                                this.mLowEnd = appMngCfgXml.mLowEnd;
                                AppMngConfig.setAbroadFlag(isAbroadArea());
                                AppMngConfig.setTopN(appMngCfgXml.mTopNCnt);
                                AppMngConfig.setImCnt(appMngCfgXml.mImCnt);
                                AppMngConfig.setSysDecay(appMngCfgXml.mSysDecay);
                                AppMngConfig.setKeySysDecay(appMngCfgXml.mKeyDecay);
                                AppMngConfig.setAdjCustTopN(appMngCfgXml.mAdjCustTopN);
                                AppMngConfig.setBgDecay(appMngCfgXml.mBgDecay);
                                AppMngConfig.setKillMoreFlag(appMngCfgXml.mKillMore);
                            }
                        }
                    }
                    if (cfg3G.mMemThrd != 0) {
                        if (memMb < cfg3G.mMemThrd) {
                            appMngCfgXml = cfg3G;
                            if ((MEM_OPT_FOR_3G & MASK_CLEANRES) != 0) {
                                cfg3G.mLowEnd = true;
                            }
                            if ((MEM_OPT_FOR_3G & MASK_RESTART) != 0) {
                                cfg3G.mKillMore = true;
                            }
                            if ((MEM_OPT_FOR_3G & MASK_FREQUENTLYUSED) != 0) {
                                cfg3G.mBgDecay = 120;
                            }
                            this.mCfgAdjTypeId = APPMNG_CFG_ADJ_3G_TYPE;
                            if (appMngCfgXml == null) {
                                AppMngConfig.setRestartFlag(appMngCfgXml.mRestart);
                                this.mLowEnd = appMngCfgXml.mLowEnd;
                                AppMngConfig.setAbroadFlag(isAbroadArea());
                                AppMngConfig.setTopN(appMngCfgXml.mTopNCnt);
                                AppMngConfig.setImCnt(appMngCfgXml.mImCnt);
                                AppMngConfig.setSysDecay(appMngCfgXml.mSysDecay);
                                AppMngConfig.setKeySysDecay(appMngCfgXml.mKeyDecay);
                                AppMngConfig.setAdjCustTopN(appMngCfgXml.mAdjCustTopN);
                                AppMngConfig.setBgDecay(appMngCfgXml.mBgDecay);
                                AppMngConfig.setKillMoreFlag(appMngCfgXml.mKillMore);
                            }
                        }
                    }
                    if (cfg4G.mMemThrd != 0) {
                        appMngCfgXml = cfg4G;
                        if ((MEM_OPT_FOR_3G & MASK_CLEANRES) != 0) {
                            cfg4G.mLowEnd = true;
                        }
                        if ((MEM_OPT_FOR_3G & MASK_RESTART) != 0) {
                            cfg4G.mKillMore = true;
                        }
                        if ((MEM_OPT_FOR_3G & MASK_FREQUENTLYUSED) != 0) {
                            cfg4G.mBgDecay = 120;
                        }
                        this.mCfgAdjTypeId = MASK_RESTART;
                    }
                    if (appMngCfgXml == null) {
                        AppMngConfig.setRestartFlag(appMngCfgXml.mRestart);
                        this.mLowEnd = appMngCfgXml.mLowEnd;
                        AppMngConfig.setAbroadFlag(isAbroadArea());
                        AppMngConfig.setTopN(appMngCfgXml.mTopNCnt);
                        AppMngConfig.setImCnt(appMngCfgXml.mImCnt);
                        AppMngConfig.setSysDecay(appMngCfgXml.mSysDecay);
                        AppMngConfig.setKeySysDecay(appMngCfgXml.mKeyDecay);
                        AppMngConfig.setAdjCustTopN(appMngCfgXml.mAdjCustTopN);
                        AppMngConfig.setBgDecay(appMngCfgXml.mBgDecay);
                        AppMngConfig.setKillMoreFlag(appMngCfgXml.mKillMore);
                    }
                }
            }
        }
    }

    public boolean isLowEnd() {
        return this.mLowEnd;
    }

    private void updateAdjWhiteListFromRMS(int rmsGroupId) {
        int i = MEM_OPT_FOR_3G;
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, this.mCfgAdjTypeId);
        if (str == null) {
            if (DEBUG) {
                AwareLog.e(TAG, "updateAdjWhiteListFromRMS failed because null whiteList!");
            }
            return;
        }
        ArraySet<String> adjList = new ArraySet();
        String[] contentArray = str.split(SEPARATOR)[MEM_OPT_FOR_3G].split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        int length = contentArray.length;
        while (i < length) {
            String content = contentArray[i].trim();
            if (!content.isEmpty()) {
                adjList.add(content);
            }
            i += MASK_CLEANRES;
        }
        synchronized (this.mAdjustAdjList) {
            this.mAdjustAdjList.clear();
            this.mAdjustAdjList.addAll(adjList);
        }
    }

    private void updateRestartAppListFromRMS(int rmsGroupId) {
        int i = MEM_OPT_FOR_3G;
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, APPMNG_RESTARTAPP_TYPE);
        if (str == null) {
            if (DEBUG) {
                AwareLog.e(TAG, "updateAdjWhiteListFromRMS failed because null whiteList!");
            }
            return;
        }
        ArraySet<String> adjList = new ArraySet();
        String[] contentArray = str.split(SEPARATOR)[MEM_OPT_FOR_3G].split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        int length = contentArray.length;
        while (i < length) {
            String content = contentArray[i].trim();
            if (!content.isEmpty()) {
                adjList.add(content);
            }
            i += MASK_CLEANRES;
        }
        synchronized (this.mRestartAppList) {
            this.mRestartAppList.clear();
            this.mRestartAppList.addAll(adjList);
        }
    }

    private void updateBadAppListFromRMS(int rmsGroupId) {
        int i = MEM_OPT_FOR_3G;
        String str = HwSysResManager.getInstance().getWhiteList(rmsGroupId, APPMNG_BADAPP_TYPE);
        if (str == null) {
            if (DEBUG) {
                AwareLog.e(TAG, "updateAdjWhiteListFromRMS failed because null whiteList!");
            }
            return;
        }
        ArraySet<String> adjList = new ArraySet();
        String[] contentArray = str.split(SEPARATOR)[MEM_OPT_FOR_3G].split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        int length = contentArray.length;
        while (i < length) {
            String content = contentArray[i].trim();
            if (!content.isEmpty()) {
                adjList.add(content);
            }
            i += MASK_CLEANRES;
        }
        synchronized (this.mBadAppList) {
            this.mBadAppList.clear();
            this.mBadAppList.addAll(adjList);
        }
    }

    private void setAppMngCfg(String str, AppMngCfgXml cfg2G) {
        if (str != null && cfg2G != null) {
            String[] contentArray = str.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            int length = contentArray.length;
            for (int i = MEM_OPT_FOR_3G; i < length; i += MASK_CLEANRES) {
                String content = contentArray[i];
                if (content != null) {
                    String[] names = content.trim().split(":");
                    if (names.length > MASK_CLEANRES) {
                        String cfgType = names[MEM_OPT_FOR_3G];
                        String cfgValue = names[MASK_CLEANRES];
                        if (!(cfgType == null || cfgValue == null)) {
                            cfgType = cfgType.trim();
                            cfgValue = cfgValue.trim();
                            int value = MEM_OPT_FOR_3G;
                            try {
                                value = Integer.parseInt(cfgValue, 10);
                            } catch (NumberFormatException e) {
                                AwareLog.e(TAG, "parseInt error");
                            }
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
                            } else if (RESTART_KEY.equals(cfgType)) {
                                cfg2G.mRestart = value != 0;
                            } else if (BG_DELAY_KEY.equals(cfgType)) {
                                cfg2G.mBgDecay = (long) value;
                            } else if (KILL_MORE_KEY.equals(cfgType)) {
                                cfg2G.mKillMore = value != 0;
                            } else if (LOWEND_KEY.equals(cfgType)) {
                                cfg2G.mLowEnd = value != 0;
                            }
                        }
                    }
                }
            }
        }
    }

    private void parseAwareProtectList() {
        synchronized (this) {
            if (this.mAwareProtectList == null) {
                return;
            }
            this.mAwareProtectMap.clear();
            int value = MEM_OPT_FOR_3G;
            for (String str : this.mAwareProtectList) {
                if (str != null && str.contains("{") && str.contains("}")) {
                    int startIdx = str.indexOf("{");
                    int endIdx = str.indexOf("}");
                    if (startIdx + MASK_CLEANRES < endIdx && startIdx + MASK_CLEANRES < str.length()) {
                        String pkgName = str.substring(MEM_OPT_FOR_3G, startIdx);
                        try {
                            value = Integer.parseInt(str.substring(startIdx + MASK_CLEANRES, endIdx), 16);
                        } catch (NumberFormatException e) {
                            AwareLog.e(TAG, "parseInt error");
                        }
                        String[] names = pkgName.split(SEPARATOR);
                        pkgName = names[MEM_OPT_FOR_3G];
                        String str2 = names.length > MASK_CLEANRES ? names[MASK_CLEANRES] : null;
                        if (!pkgName.isEmpty()) {
                            PackageConfigItem item = (PackageConfigItem) this.mAwareProtectMap.get(pkgName);
                            if (item == null) {
                                item = new PackageConfigItem(pkgName, value);
                                this.mAwareProtectMap.put(pkgName, item);
                                if (DEBUG) {
                                    AwareLog.i(TAG, "pkgName:" + pkgName + " mGroupId:" + item.mGroupId + " restart:" + item.mRestartFlag + " clean:" + item.mResCleanAllow + " frequently used:" + item.mFrequentlyUsed);
                                }
                            }
                            if (!(str2 == null || str2.isEmpty())) {
                                ProcessConfigItem dItem = new ProcessConfigItem(str2, value);
                                item.add(dItem);
                                if (DEBUG) {
                                    AwareLog.i(TAG, "processName:" + str2 + " mGroupId:" + dItem.mGroupId + " restart:" + dItem.mRestartFlag + " clean:" + dItem.mResCleanAllow + " frequently used:" + item.mFrequentlyUsed);
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
        ArrayMap<String, PackageConfigItem> map = new ArrayMap();
        setStaticXmlWhiteList();
        synchronized (this) {
            map.putAll(this.mAwareProtectMap);
        }
        return map;
    }

    public List<String> getKeyHabitAppList() {
        List<String> list = new ArrayList();
        if (!this.mEnabled) {
            return list;
        }
        synchronized (this) {
            list.addAll(this.mKeyHabitAppList);
        }
        return list;
    }

    public List<String> getAllHabitAppList() {
        List<String> list = new ArrayList();
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
                int maxCnt = MEM_OPT_FOR_3G;
                for (String pkgName : list) {
                    if (pkgName != null) {
                        this.mKeyHabitAppList.add(pkgName);
                        maxCnt += MASK_CLEANRES;
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
            if (this.mEnabled) {
                pw.println("dump iAware Protect WhiteList Apps start --------");
                synchronized (this) {
                    for (String item : this.mAwareProtectList) {
                        pw.println(item);
                    }
                }
                pw.println("dump iAware Protect WhiteList Apps end-----------");
                pw.println("dump iAware Adjust Adj Apps start --------");
                synchronized (this.mAdjustAdjList) {
                    for (String item2 : this.mAdjustAdjList) {
                        pw.println(item2);
                    }
                }
                pw.println("dump iAware Adjust Adj Apps end-----------");
                pw.println("dump User Habit Frequent Used start-----------");
                synchronized (this.mHabitFrequentUsed) {
                    for (String item22 : this.mHabitFrequentUsed) {
                        pw.println(item22);
                    }
                }
                pw.println("dump User Habit Frequent Used end-----------");
                pw.println("dump User Habit WhiteList Apps start ------------");
                synchronized (this) {
                    for (String item222 : this.mKeyHabitAppList) {
                        pw.println(item222);
                    }
                }
                pw.println("dump User Habit WhiteList Apps end --------------");
                pw.println("dump User All Habit WhiteList Apps start ------------");
                synchronized (this) {
                    for (String item2222 : this.mAllHabitAppList) {
                        pw.println(item2222);
                    }
                }
                pw.println("dump User All Habit WhiteList Apps end --------------");
                pw.println("dump iAware Restart Apps start --------");
                synchronized (this.mRestartAppList) {
                    for (String item22222 : this.mRestartAppList) {
                        pw.println(item22222);
                    }
                }
                pw.println("dump iAware Restart Apps end-----------");
                pw.println("dump iAware Bad Apps start --------");
                synchronized (this.mBadAppList) {
                    for (String item222222 : this.mBadAppList) {
                        pw.println(item222222);
                    }
                }
                pw.println("dump iAware Bad Apps end-----------");
                pw.println("dump AppMng Config start ------------");
                dumpCfg(pw);
                pw.println("dump AppMng Configs end ------------");
                return;
            }
            pw.println("WhiteList feature not enabled.");
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
        pw.println("killMore:" + AppMngConfig.getKillMoreFlag());
        pw.println("lowEnd:" + this.mLowEnd);
    }

    public void fillMostFrequentUsedApp(List<String> list) {
        if (this.mEnabled) {
            List<String> listHabit = new ArrayList();
            if (list != null) {
                listHabit.addAll(list);
            }
            Set<String> setAdjPkg = new ArraySet();
            setAdjPkg.addAll(listHabit);
            synchronized (this.mAdjustAdjList) {
                setAdjPkg.addAll(this.mAdjustAdjList);
            }
            HwActivityManagerService ams = HwActivityManagerService.self();
            if (ams != null) {
                ams.setAndRestoreMaxAdjIfNeed(setAdjPkg);
            }
            synchronized (this.mHabitFrequentUsed) {
                this.mHabitFrequentUsed.clear();
                this.mHabitFrequentUsed.addAll(listHabit);
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
            synchronized (this.mHabitFrequentUsed) {
                if (this.mHabitFrequentUsed.contains(pkg)) {
                    return true;
                }
                return false;
            }
        }
    }

    public Set<String> getRestartAppList() {
        Set arraySet;
        synchronized (this.mRestartAppList) {
            arraySet = new ArraySet(this.mRestartAppList);
        }
        return arraySet;
    }

    public Set<String> getBadAppList() {
        Set arraySet;
        synchronized (this.mBadAppList) {
            arraySet = new ArraySet(this.mBadAppList);
        }
        return arraySet;
    }

    public static boolean isAbroadArea() {
        return !SystemProperties.get("ro.config.hw_optb", PPPOEStateMachine.PHASE_DEAD).equals("156");
    }
}
