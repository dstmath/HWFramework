package com.android.server.mtm.iaware.appmng;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.mtm.iaware.appmng.appclean.SmartClean;
import com.android.server.mtm.iaware.appmng.rule.ListItem;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.HwStartWindowCache;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppUseDataManager;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.cpu.NetManager;
import com.android.server.rms.iaware.feature.AppSceneMngFeature;
import com.android.server.rms.iaware.memory.policy.CachedMemoryCleanPolicy;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.content.pm.UserInfoExAdapter;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import com.huawei.util.LogEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AwareAppMngSort {
    public static final String ACTIVITY_RECENT_TASK = "com.android.systemui/.recents.RecentsActivity";
    public static final int ACTIVITY_TASK_IMPORT_CNT = 2;
    public static final String ADJTYPE_SERVICE = "service";
    public static final int APPMNG_MEM_ALLOWSTOP_GROUP = 2;
    public static final int APPMNG_MEM_ALL_GROUP = 3;
    public static final int APPMNG_MEM_FORBIDSTOP_GROUP = 0;
    public static final int APPMNG_MEM_SHORTAGESTOP_GROUP = 1;
    public static final int APPSORT_FORCOMPACT = 1;
    public static final int APPSORT_FORMEM = 0;
    public static final int APPSORT_FORMEMCLEAN = 2;
    private static final long BETA_LOG_PRINT_INTERVEL = 60000;
    private static final long BETA_LOG_PRINT_LEVEL_INTERVEL = 1000;
    private static final Comparator<AwareProcessBlockInfo> BLOCK_BY_ADJ = new Comparator<AwareProcessBlockInfo>() {
        /* class com.android.server.mtm.iaware.appmng.AwareAppMngSort.AnonymousClass4 */

        public int compare(AwareProcessBlockInfo infoLeft, AwareProcessBlockInfo infoRight) {
            if (infoLeft == null) {
                return infoRight == null ? 0 : -1;
            }
            if (infoRight == null) {
                return 1;
            }
            int adjLeft = infoLeft.getAdj();
            int adjRight = infoRight.getAdj();
            if (adjLeft < adjRight) {
                return -1;
            }
            if (adjLeft > adjRight) {
                return 1;
            }
            String pkgLeft = infoLeft.procPackageName;
            String pkgRight = infoRight.procPackageName;
            if (pkgLeft == null) {
                return pkgRight == null ? 0 : -1;
            }
            if (pkgRight == null) {
                return 1;
            }
            if (!pkgRight.equals(pkgLeft)) {
                return pkgLeft.compareTo(pkgRight);
            }
            if (infoLeft.procUid == infoRight.procUid) {
                return 0;
            }
            if (infoLeft.procUid < infoRight.procUid) {
                return -1;
            }
            return 1;
        }
    };
    private static final Comparator<AwareProcessBlockInfo> BLOCK_BY_IMPORTANCE = new Comparator<AwareProcessBlockInfo>() {
        /* class com.android.server.mtm.iaware.appmng.AwareAppMngSort.AnonymousClass1 */

        public int compare(AwareProcessBlockInfo arg0, AwareProcessBlockInfo arg1) {
            return arg1.procImportance - arg0.procImportance;
        }
    };
    private static final Comparator<AwareProcessBlockInfo> BLOCK_BY_USER_HABIT = new Comparator<AwareProcessBlockInfo>() {
        /* class com.android.server.mtm.iaware.appmng.AwareAppMngSort.AnonymousClass2 */

        public int compare(AwareProcessBlockInfo arg0, AwareProcessBlockInfo arg1) {
            if (arg0 == null) {
                if (arg1 == null) {
                    return 0;
                }
                return -1;
            } else if (arg1 == null) {
                return 1;
            } else {
                return arg0.procImportance - arg1.procImportance;
            }
        }
    };
    private static final Comparator<AwareProcessBlockInfo> BLOCK_BY_WEIGHT = new Comparator<AwareProcessBlockInfo>() {
        /* class com.android.server.mtm.iaware.appmng.AwareAppMngSort.AnonymousClass3 */

        public int compare(AwareProcessBlockInfo arg1, AwareProcessBlockInfo arg0) {
            if (arg0 == null) {
                if (arg1 == null) {
                    return 0;
                }
                return -1;
            } else if (arg1 == null) {
                return 1;
            } else {
                return arg0.procWeight - arg1.procWeight;
            }
        }
    };
    private static final String CSP_APPS = "csp_apps";
    private static final int DEFAULT_VALUE = -1;
    private static final int EQUAL_TO = 0;
    public static final String EXEC_SERVICES = "exec-service";
    public static final String FG_SERVICE = "fg-service";
    public static final long FOREVER_DECAYTIME = -1;
    public static final int HABITMAX_IMPORT = 10000;
    public static final int HABITMIN_IMPORT = 0;
    private static final int INVALID_VALUE = -1;
    private static final int LESS_THAN = -1;
    private static final String LIST_FILTER_LEVEL2 = "list-filter-lv2";
    private static final int MAX_IMPORTANCE_VAL = 10000;
    private static final int MAX_TOP_N_NUM = 8;
    public static final int MEM_LEVEL0 = 0;
    public static final int MEM_LEVEL1 = 1;
    public static final int MEM_LEVEL2 = 2;
    public static final int MEM_LEVEL3 = 3;
    public static final int MEM_LEVEL4 = 4;
    public static final int MEM_LEVEL4_WEIGHT = 14;
    public static final int MEM_LEVEL5 = 5;
    public static final int MEM_LEVEL_DEFAULT = 0;
    public static final int MEM_LEVEL_KILL_MORE = 1;
    private static final int MORE_THAN = 1;
    private static final int MSG_PRINT_BETA_LOG = 1;
    private static final int PKG_INDEX = 2;
    public static final long PREVIOUS_APP_DIRCACTIVITY_DECAYTIME = 600000;
    private static final int SEC_PER_MIN = 60;
    private static final String TAG = "AwareAppMngSort";
    private static final int TAG_INDEX = 3;
    private static final String TAG_POLICY = "policy";
    private static final int TOP_N_IMPORT_RATE = -100;
    private static final int UID_INDEX = 4;
    private static ArrayList<String> filteredApps = null;
    private static boolean sDebug = false;
    private static boolean sEnabled = false;
    private static AwareAppMngSort sInstance = null;
    private boolean mAssocEnable = true;
    private final Context mContext;
    private Handler mHandler = null;
    private HwActivityManagerService mHwAMS = null;
    private long mLastLevelPrintTime = 0;
    private ArrayMap<Integer, Long> mLastPrintTimeByLevel = new ArrayMap<>();
    private ArraySet<String> mListFilterLevel2 = null;

    public enum ClassRate {
        NONE("none"),
        PERSIST("persist"),
        FOREGROUND(MemoryConstant.MEM_REPAIR_CONSTANT_FG),
        KEYBACKGROUND("keybackground"),
        HOME("home"),
        KEYSERVICES("keyservices"),
        NORMAL("normal"),
        UNKNOWN("unknown");
        
        private String mDescription;

        private ClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    public enum ForbidSubClassRate {
        NONE("none"),
        PREVIOUS("previous"),
        AWARE_PROTECTED("awareProtected");
        
        private String mDescription;

        private ForbidSubClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    public enum ShortageSubClassRate {
        NONE("none"),
        PREV_ONECLEAN("prevOneclean"),
        MUSIC_PLAY("musicPlay"),
        SOUND_RECORD("soundRecord"),
        FGSERVICES_TOPN("fgservice_topN"),
        SERVICE_ADJ_TOPN("service_adj_topN"),
        HW_SYSTEM("hw_system"),
        KEY_IM("key_im"),
        GUIDE("guide"),
        DOWN_UP_LOAD("downupLoad"),
        HEALTH("health"),
        PREVIOUS("previous"),
        AWARE_PROTECTED("awareProtected"),
        KEY_SYS_SERVICE("keySysService"),
        ASSOC_WITH_FG("assocWithFg"),
        VISIBLEWIN("visibleWin"),
        FREQN("user_freqN"),
        TOPN("user_topN"),
        WIDGET("widget"),
        UNKNOWN("unknown");
        
        private String mDescription;
        private int subClass = -1;

        private ShortageSubClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }

        public int getSubClassRate() {
            return this.subClass;
        }
    }

    public enum AllowStopSubClassRate {
        NONE("none"),
        PREVIOUS("previous"),
        TOPN("user_topN"),
        KEY_SYS_SERVICE("keySysService"),
        HEALTH("health"),
        FG_SERVICES("fg_services"),
        OTHER("other"),
        NONCURUSER("nonCurUser"),
        UNKNOWN("unknown");
        
        private String mDescription;

        private AllowStopSubClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    private AwareAppMngSort(Context context) {
        this.mContext = context;
        initHandler();
        init();
    }

    public static synchronized AwareAppMngSort getInstance(Context context) {
        AwareAppMngSort awareAppMngSort;
        synchronized (AwareAppMngSort.class) {
            if (sInstance == null) {
                sInstance = new AwareAppMngSort(context);
            }
            awareAppMngSort = sInstance;
        }
        return awareAppMngSort;
    }

    public static synchronized AwareAppMngSort getInstance() {
        AwareAppMngSort awareAppMngSort;
        synchronized (AwareAppMngSort.class) {
            awareAppMngSort = sInstance;
        }
        return awareAppMngSort;
    }

    private void init() {
        this.mHwAMS = HwActivityManagerService.self();
    }

    public static void enable() {
        sEnabled = true;
    }

    public static void disable() {
        sEnabled = false;
    }

    /* access modifiers changed from: private */
    public static class MemSortGroup {
        public List<AwareProcessBlockInfo> procAllowStopList = null;
        public List<AwareProcessBlockInfo> procForbidStopList = null;
        public List<AwareProcessBlockInfo> procShortageStopList = null;

        public MemSortGroup(List<AwareProcessBlockInfo> forbidStopList, List<AwareProcessBlockInfo> shortageStopList, List<AwareProcessBlockInfo> allowStopList) {
            this.procForbidStopList = forbidStopList;
            this.procShortageStopList = shortageStopList;
            this.procAllowStopList = allowStopList;
        }
    }

    private boolean isCurUserProc(int checkUid, int curUserUid) {
        UserManager usm;
        int userId = UserHandleEx.getUserId(checkUid);
        boolean isCloned = false;
        Context context = this.mContext;
        if (!(context == null || (usm = UserManagerExt.get(context)) == null)) {
            UserInfoExAdapter info = UserInfoExAdapter.getUserInfo(usm, userId);
            isCloned = info != null ? info.isClonedProfile() : false;
        }
        if (userId == curUserUid || isCloned) {
            return true;
        }
        return false;
    }

    private static boolean isPkgIncludeForTgt(List<String> tgtPkg, List<String> dstPkg) {
        if (tgtPkg == null || tgtPkg.isEmpty() || dstPkg == null) {
            return false;
        }
        for (String pkg : dstPkg) {
            if (pkg != null && tgtPkg.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFgServicesImportantByAdjtype(String adjType) {
        return FG_SERVICE.equals(adjType) || ADJTYPE_SERVICE.equals(adjType);
    }

    public boolean needCheckAlarm(AwareProcessBlockInfo info) {
        if (info == null) {
            return false;
        }
        if (AppMngConfig.getAlarmCheckFlag()) {
            return true;
        }
        return info.procAlarmChk;
    }

    private Map<Integer, AwareProcessBlockInfo> groupByUid(Map<Integer, AwareProcessInfo> allProcNeedSort) {
        if (allProcNeedSort == null) {
            return null;
        }
        Map<Integer, AwareProcessBlockInfo> uids = new ArrayMap<>();
        Map<Integer, Integer> userHabitMap = null;
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null) {
            userHabitMap = habit.getTopList(allProcNeedSort);
        }
        for (Map.Entry<Integer, AwareProcessInfo> m : allProcNeedSort.entrySet()) {
            AwareProcessInfo info = m.getValue();
            if (info != null) {
                Integer importance = null;
                if (userHabitMap != null) {
                    importance = userHabitMap.get(Integer.valueOf(info.procPid));
                }
                if (importance != null) {
                    info.procImportance = importance.intValue();
                } else {
                    info.procImportance = 10000;
                }
                AwareProcessBlockInfo block = uids.get(Integer.valueOf(info.procProcInfo.mUid));
                if (block == null) {
                    AwareProcessBlockInfo block2 = new AwareProcessBlockInfo(info.procProcInfo.mUid, false, info.procClassRate);
                    block2.procProcessList.add(info);
                    uids.put(Integer.valueOf(info.procProcInfo.mUid), block2);
                    block2.procImportance = info.procImportance;
                } else {
                    block.procProcessList.add(info);
                    if (block.procImportance > info.procImportance) {
                        block.procImportance = info.procImportance;
                    }
                }
            }
        }
        return uids;
    }

    private MemSortGroup getAppMemCompactSortGroup(int subType) {
        List<AwareProcessBlockInfo> procForbidList = new ArrayList<>();
        List<AwareProcessBlockInfo> procCompactibleList = new ArrayList<>();
        List<AwareProcessBlockInfo> procFrozenList = new ArrayList<>();
        List<AwareProcessInfo> allProcNeedSortList = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (allProcNeedSortList == null) {
            AwareLog.e(TAG, "getAllProcNeedSort failed for memCompact!");
            return null;
        }
        List<AwareProcessBlockInfo> resultInfo = DecisionMaker.getInstance().decideAllWithoutList(allProcNeedSortList, subType, AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.COMPACT);
        if (resultInfo == null) {
            AwareLog.e(TAG, "decideAll get null for memCompact!");
            return null;
        }
        Map<Integer, AwareProcessInfo> allProcNeedSort = new ArrayMap<>();
        updateProcListInfo(resultInfo, procForbidList, procFrozenList, procCompactibleList, allProcNeedSort);
        Map<Integer, AwareProcessBlockInfo> allUids = groupByUid(allProcNeedSort);
        if (filteredApps == null) {
            updateFilteredApps();
        }
        for (AwareProcessBlockInfo awareBlkProc : procCompactibleList) {
            AwareProcessInfo printItem = awareBlkProc.procProcessList.get(0);
            if (!filteredApps.contains(awareBlkProc.procPackageName)) {
                int blkByUidImportance = allUids.get(Integer.valueOf(awareBlkProc.procUid)).procImportance;
                if (blkByUidImportance >= printItem.procImportance || AwareAppAssociate.isDealAsPkgUid(awareBlkProc.procUid)) {
                    awareBlkProc.procImportance = printItem.procImportance;
                } else {
                    awareBlkProc.procImportance = blkByUidImportance;
                }
            } else {
                awareBlkProc.procImportance = 0;
            }
        }
        Collections.sort(procCompactibleList, BLOCK_BY_IMPORTANCE);
        return new MemSortGroup(procForbidList, procCompactibleList, procFrozenList);
    }

    private void updateProcListInfo(List<AwareProcessBlockInfo> resultInfo, List<AwareProcessBlockInfo> procForbidList, List<AwareProcessBlockInfo> procFrozenList, List<AwareProcessBlockInfo> procCompactibleList, Map<Integer, AwareProcessInfo> allProcNeedSort) {
        AppStatusUtils appStatus = AppStatusUtils.getInstance();
        for (AwareProcessBlockInfo awareProcBlkInfo : resultInfo) {
            if (isBlockInfoValid(awareProcBlkInfo)) {
                AwareProcessInfo item = awareProcBlkInfo.procProcessList.get(0);
                if (awareProcBlkInfo.procCleanType == ProcessCleaner.CleanType.NONE) {
                    procForbidList.add(awareProcBlkInfo);
                } else if (appStatus.checkAppStatus(AppStatusUtils.Status.FROZEN, item)) {
                    procFrozenList.add(awareProcBlkInfo);
                } else {
                    procCompactibleList.add(awareProcBlkInfo);
                    allProcNeedSort.put(Integer.valueOf(item.procPid), item);
                }
            }
        }
    }

    private boolean isBlockInfoValid(AwareProcessBlockInfo awareProcBlkInfo) {
        if (awareProcBlkInfo == null || awareProcBlkInfo.procProcessList == null || awareProcBlkInfo.procProcessList.isEmpty() || awareProcBlkInfo.procProcessList.get(0) == null) {
            return false;
        }
        return true;
    }

    private static void updateFilteredApps() {
        filteredApps = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_CLEAN.getDesc(), CSP_APPS);
        if (filteredApps == null) {
            AwareLog.e(TAG, "get csp_apps failed!");
            filteredApps = new ArrayList<>(0);
        }
    }

    public static String getClassRateStr(int classRate) {
        ClassRate[] allRate = ClassRate.values();
        for (ClassRate rate : allRate) {
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ClassRate.UNKNOWN.description();
    }

    public boolean isGroupBeHigher(int pid, int uid, String processName, ArrayList<String> arrayList, int groupId) {
        AwareAppAssociate awareAssoc;
        if (!sEnabled || !this.mAssocEnable || (awareAssoc = AwareAppAssociate.getInstance()) == null) {
            return false;
        }
        SparseSet forePid = new SparseSet();
        awareAssoc.getForeGroundApp(forePid);
        if (forePid.contains(pid)) {
            return true;
        }
        HwActivityManagerService hwActivityManagerService = this.mHwAMS;
        AwareProcessBaseInfo info = hwActivityManagerService != null ? hwActivityManagerService.getProcessBaseInfo(pid) : null;
        if (info == null) {
            return false;
        }
        if (info.curAdj < HwActivityManagerService.PERCEPTIBLE_APP_ADJ) {
            return true;
        }
        if (groupId == 2) {
            if (info.curAdj == HwActivityManagerService.BACKUP_APP_ADJ || info.curAdj == HwActivityManagerService.HEAVY_WEIGHT_APP_ADJ) {
                return true;
            }
            if (info.curAdj == HwActivityManagerService.PERCEPTIBLE_APP_ADJ && !isFgServicesImportantByAdjtype(info.adjType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkAppMngEnable() {
        return sEnabled;
    }

    private boolean needReplaceAllowStopList(int resourceType) {
        return 2 == resourceType;
    }

    public AwareAppMngSortPolicy getAppMngSortPolicy(int resourceType, int subType, int groupId) {
        if (!sEnabled) {
            return null;
        }
        long startTime = 0;
        if (sDebug) {
            startTime = System.currentTimeMillis();
        }
        ArrayMap<Integer, List<AwareProcessBlockInfo>> appGroup = new ArrayMap<>();
        if (needReplaceAllowStopList(resourceType)) {
            appGroup = getAppMngSortGroupForMemCleanChina(groupId, subType);
        } else {
            MemSortGroup sortGroup = null;
            if (1 == resourceType) {
                sortGroup = getAppMemCompactSortGroup(subType);
            }
            if (sortGroup == null) {
                return null;
            }
            if (groupId == 0) {
                appGroup.put(Integer.valueOf(groupId), sortGroup.procForbidStopList);
            } else if (groupId == 1) {
                appGroup.put(Integer.valueOf(groupId), sortGroup.procShortageStopList);
            } else if (groupId == 2) {
                appGroup.put(2, sortGroup.procAllowStopList);
            } else if (groupId == 3) {
                appGroup.put(0, sortGroup.procForbidStopList);
                appGroup.put(1, sortGroup.procShortageStopList);
                appGroup.put(2, sortGroup.procAllowStopList);
            }
        }
        AwareAppMngSortPolicy sortPolicy = new AwareAppMngSortPolicy(this.mContext, appGroup);
        if (sDebug) {
            AwareLog.i(TAG, "getAppMngSortPolicy eclipse time:" + (System.currentTimeMillis() - startTime));
            long availableRam = MemoryReader.getInstance().getMemAvailable();
            AwareLog.i(TAG, "MemAvailable(KB): " + availableRam);
            dumpPolicy(sortPolicy, null, false, subType);
        }
        if (LogEx.getLogHWInfo() && 2 == resourceType) {
            printBetaLog(sortPolicy, subType);
        }
        return sortPolicy;
    }

    private void printBetaLog(AwareAppMngSortPolicy sortPolicy, int level) {
        long curTime = SystemClock.elapsedRealtime();
        synchronized (this.mLastPrintTimeByLevel) {
            Long lastTime = this.mLastPrintTimeByLevel.get(Integer.valueOf(level));
            if (lastTime == null) {
                lastTime = 0L;
            }
            if (curTime - lastTime.longValue() >= 60000) {
                if (this.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = new BetaLog(sortPolicy, level);
                    long delay = 0;
                    if (curTime - this.mLastLevelPrintTime < 1000) {
                        delay = (this.mLastLevelPrintTime + 1000) - curTime;
                        AwareLog.i(TAG, "level = " + level + " policy print delay " + delay + " ms.");
                    }
                    this.mHandler.sendMessageDelayed(msg, delay);
                    this.mLastPrintTimeByLevel.put(Integer.valueOf(level), Long.valueOf(curTime));
                    this.mLastLevelPrintTime = curTime + delay;
                }
            }
        }
    }

    private ArrayMap<Integer, List<AwareProcessBlockInfo>> getAppMngSortGroupForMemCleanChina(int groupId, int memLevel) {
        List<AwareProcessInfo> allAwareProcNeedProcess = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (allAwareProcNeedProcess == null || allAwareProcNeedProcess.isEmpty()) {
            AwareLog.e(TAG, "getAllProcNeedSort failed!");
            return null;
        }
        ArraySet<String> listFilter = new ArraySet<>();
        boolean consiterPartialList = false;
        if (memLevel == 2) {
            if (this.mListFilterLevel2 == null) {
                updateLevel2ListFilter();
            }
            ArraySet<String> arraySet = this.mListFilterLevel2;
            if (arraySet != null) {
                listFilter.addAll((ArraySet<? extends String>) arraySet);
            }
        } else if (memLevel == 4 || memLevel == 5) {
            consiterPartialList = true;
        }
        List<AwareProcessBlockInfo> rawInfo = DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, new DecisionMaker.DecideConfigInfo(memLevel, AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.MEMORY), listFilter, consiterPartialList);
        List<AwareProcessBlockInfo> needClean = CleanSource.mergeBlockForMemory(rawInfo, DecisionMaker.getInstance().getProcessList(AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.MEMORY));
        if (memLevel == 4 || memLevel == 5) {
            DecisionMaker.getInstance().checkListForMemLowEnd(needClean, rawInfo, memLevel);
            setWeightForLowEnd(needClean);
        }
        AppCleanupDumpRadar.getInstance().reportMemoryData(rawInfo, -1);
        checkIfNothingCanKill(needClean);
        sortByLevelOrHabit(needClean, memLevel);
        return getAppGroupById(groupId, rawInfo, needClean);
    }

    private void sortByLevelOrHabit(List<AwareProcessBlockInfo> needClean, int memLevel) {
        if (!(needClean == null || needClean.isEmpty())) {
            if (isLowLevel(memLevel)) {
                Collections.sort(needClean, BLOCK_BY_WEIGHT);
                return;
            }
            List<String> pkgTopN = null;
            Map<String, Integer> allTopList = null;
            AwareUserHabit habit = AwareUserHabit.getInstance();
            if (habit != null) {
                allTopList = habit.getAllTopList();
                pkgTopN = habit.getTopN(AppMngConfig.getTopN());
            }
            for (AwareProcessBlockInfo block : needClean) {
                setPropImportance(block, allTopList, pkgTopN);
            }
            Collections.sort(needClean, BLOCK_BY_USER_HABIT);
        }
    }

    private ArrayMap<Integer, List<AwareProcessBlockInfo>> getAppGroupById(int groupId, List<AwareProcessBlockInfo> rawInfo, List<AwareProcessBlockInfo> needClean) {
        ArrayMap<Integer, List<AwareProcessBlockInfo>> appGroup = new ArrayMap<>();
        if (groupId == 0) {
            appGroup.put(0, rawInfo);
        } else if (groupId == 2) {
            appGroup.put(2, needClean);
        } else if (groupId != 3) {
            return null;
        } else {
            appGroup.put(0, rawInfo);
            appGroup.put(2, needClean);
        }
        return appGroup;
    }

    public AwareAppMngSortPolicy getAppMngSortPolicyForMemRepairVss(int sceneType, AwareAppMngSortPolicy pssPolicy) {
        List<AwareProcessBlockInfo> presetData = getPresetDataFromMemRepairVss(pssPolicy);
        AwareAppMngSortPolicy vssPolicy = getAppMngSortPolicyForMemRepair(sceneType, AppMngConstant.AppCleanSource.MEMORY_REPAIR_VSS);
        ArrayMap<Integer, List<AwareProcessBlockInfo>> appGroup = new ArrayMap<>();
        if (vssPolicy == null) {
            return null;
        }
        List<AwareProcessBlockInfo> allowStop = differenceList(vssPolicy.getAllowStopProcBlockList(), presetData);
        List<AwareProcessBlockInfo> forbidStop = vssPolicy.getForbidStopProcBlockList();
        forbidStop.addAll(presetData);
        appGroup.put(2, allowStop);
        appGroup.put(0, forbidStop);
        return new AwareAppMngSortPolicy(this.mContext, appGroup);
    }

    private List<AwareProcessBlockInfo> getPresetDataFromMemRepairVss(AwareAppMngSortPolicy pssWithList) {
        AwareIntelligentRecg aiRecg = AwareIntelligentRecg.getInstance();
        List<AwareProcessBlockInfo> pssForbidStop = pssWithList.getForbidStopProcBlockList();
        List<AwareProcessBlockInfo> presetData = new ArrayList<>();
        for (AwareProcessBlockInfo pss : pssForbidStop) {
            String pssPackageName = pss.procPackageName;
            if (AppMngConstant.CleanReason.LIST.getCode().equals(pss.procReason) && !aiRecg.isTopImAppBase(pssPackageName) && !aiRecg.getActTopIMCN().equals(pssPackageName)) {
                presetData.add(pss);
            }
        }
        return presetData;
    }

    private List<AwareProcessBlockInfo> differenceList(List<AwareProcessBlockInfo> pSet1, List<AwareProcessBlockInfo> pSet2) {
        List<AwareProcessBlockInfo> diff = new ArrayList<>();
        for (AwareProcessBlockInfo p1 : pSet1) {
            boolean found = false;
            Iterator<AwareProcessBlockInfo> it = pSet2.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (isTheSamePackage(p1, it.next())) {
                        found = true;
                        break;
                    }
                } else {
                    break;
                }
            }
            if (!found) {
                diff.add(p1);
            }
        }
        return diff;
    }

    private boolean isTheSamePackage(AwareProcessBlockInfo b1, AwareProcessBlockInfo b2) {
        return (b1.procPackageName == null || b2.procPackageName == null || !b1.procPackageName.equals(b2.procPackageName)) ? false : true;
    }

    public AwareAppMngSortPolicy getAppMngSortPolicyForMemRepair(int sceneType) {
        return getAppMngSortPolicyForMemRepair(sceneType, AppMngConstant.AppCleanSource.MEMORY_REPAIR);
    }

    private AwareAppMngSortPolicy getAppMngSortPolicyForMemRepair(int sceneType, AppMngConstant.AppCleanSource flag) {
        if (!sEnabled) {
            return null;
        }
        List<AwareProcessInfo> allAwareProcNeedProcess = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (allAwareProcNeedProcess == null || allAwareProcNeedProcess.isEmpty()) {
            AwareLog.e(TAG, "getAllProcNeedSort failed!");
            return null;
        }
        List<AwareProcessBlockInfo> rawInfo = DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, sceneType, AppMngConstant.AppMngFeature.APP_CLEAN, (AppMngConstant.EnumWithDesc) flag);
        if (rawInfo == null || rawInfo.isEmpty()) {
            AwareLog.e(TAG, "decideAll get null for memRepair!");
            return null;
        }
        List<AwareProcessBlockInfo> resultInfo = CleanSource.mergeBlock(rawInfo);
        if (resultInfo == null) {
            AwareLog.e(TAG, "mergeBlock get null for memRepair!");
            return null;
        }
        List<AwareProcessBlockInfo> arrayList = new ArrayList<>();
        List<AwareProcessBlockInfo> arrayList2 = new ArrayList<>();
        for (AwareProcessBlockInfo blockInfo : resultInfo) {
            if (!(blockInfo == null || blockInfo.procProcessList == null || blockInfo.procProcessList.isEmpty())) {
                if (blockInfo.procCleanType == ProcessCleaner.CleanType.NONE) {
                    arrayList.add(blockInfo);
                } else {
                    arrayList2.add(blockInfo);
                }
            }
        }
        ArrayMap<Integer, List<AwareProcessBlockInfo>> appGroup = new ArrayMap<>();
        appGroup.put(0, arrayList);
        appGroup.put(2, arrayList2);
        return new AwareAppMngSortPolicy(this.mContext, appGroup);
    }

    public List<AwareProcessInfo> getAppMngSortPolicyForSystemTrim() {
        if (!sEnabled) {
            return null;
        }
        return AppStatusUtils.getInstance().getAllProcNeedSort();
    }

    public boolean checkNonSystemUser(AwareProcessInfo awareProcInfo) {
        int curUserUid;
        if (awareProcInfo == null || awareProcInfo.procProcInfo == null || (curUserUid = AwareAppAssociate.getInstance().getCurUserId()) != 0 || isCurUserProc(awareProcInfo.procProcInfo.mUid, curUserUid)) {
            return false;
        }
        return true;
    }

    private boolean isNonSystemUser(int checkUid) {
        return AwareAppAssociate.getInstance().getCurUserId() == 0 && !isCurUserProc(checkUid, AwareAppAssociate.getInstance().getCurUserId());
    }

    private void setPropImportance(AwareProcessBlockInfo block, Map<String, Integer> allTopList, List<String> pkgTopN) {
        int topIdx;
        if (block != null) {
            if (isNonSystemUser(block.procUid)) {
                block.procImportance = 10000;
                return;
            }
            Integer importance = null;
            if (allTopList != null) {
                importance = allTopList.get(block.procPackageName);
            }
            if (pkgTopN != null && (topIdx = pkgTopN.indexOf(block.procPackageName)) >= 0 && topIdx < 8) {
                importance = Integer.valueOf((8 - topIdx) * -100);
            }
            block.procImportance = importance != null ? importance.intValue() : 0;
        }
    }

    public static void enableDebug() {
        sDebug = true;
    }

    public static void disableDebug() {
        sDebug = false;
    }

    public void enableAssocDebug() {
        this.mAssocEnable = true;
    }

    public void disableAssocDebug() {
        this.mAssocEnable = false;
    }

    public boolean getAssocDebug() {
        return this.mAssocEnable;
    }

    private static String getForbidSubClassRateStr(int classRate) {
        ForbidSubClassRate[] allRate = ForbidSubClassRate.values();
        for (ForbidSubClassRate rate : allRate) {
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ForbidSubClassRate.NONE.description();
    }

    private static String getShortageSubClassRateStr(int classRate) {
        ShortageSubClassRate[] allRate = ShortageSubClassRate.values();
        for (ShortageSubClassRate rate : allRate) {
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ShortageSubClassRate.NONE.description();
    }

    private static String getAllowSubClassRateStr(int classRate) {
        AllowStopSubClassRate[] allRate = AllowStopSubClassRate.values();
        for (AllowStopSubClassRate rate : allRate) {
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return AllowStopSubClassRate.NONE.description();
    }

    private String getClassStr(int classRate, int subClassRate) {
        if (classRate == ClassRate.FOREGROUND.ordinal()) {
            return getForbidSubClassRateStr(subClassRate);
        }
        if (classRate == ClassRate.KEYSERVICES.ordinal()) {
            return getShortageSubClassRateStr(subClassRate);
        }
        return getAllowSubClassRateStr(subClassRate);
    }

    private void updateProcessInfo() {
        ProcessInfoCollector processInfoCollector = ProcessInfoCollector.getInstance();
        if (processInfoCollector != null) {
            ArrayList<ProcessInfo> procs = processInfoCollector.getProcessInfoList();
            if (!procs.isEmpty()) {
                int size = procs.size();
                for (int i = 0; i < size; i++) {
                    ProcessInfo procInfo = procs.get(i);
                    if (procInfo != null) {
                        processInfoCollector.recordProcessInfo(procInfo.mPid, procInfo.mUid);
                    }
                }
            }
        }
    }

    public boolean isProcessBlockPidChanged(AwareProcessBlockInfo procGroup) {
        if (!sEnabled || procGroup == null) {
            return false;
        }
        ArrayList<ProcessInfo> procs = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procs.isEmpty()) {
            return false;
        }
        int uid = procGroup.procUid;
        int size = procs.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo procInfo = procs.get(i);
            if (procInfo != null && procInfo.mUid == uid && procInfo.mCreatedTime - procGroup.procUpdateTime > 0) {
                return true;
            }
        }
        return false;
    }

    private void dumpBlockList(PrintWriter pw, List<AwareProcessBlockInfo> list, boolean toPrint) {
        if (list == null) {
            return;
        }
        if (pw != null || !toPrint) {
            for (AwareProcessBlockInfo pinfo : list) {
                if (pinfo != null) {
                    boolean allow = pinfo.procResCleanAllow;
                    print(pw, "AppProc:uid:" + pinfo.procUid + ",import:" + pinfo.procImportance + ",classRates:" + pinfo.procClassRate + ",classStr:" + getClassRateStr(pinfo.procClassRate) + ",subStr:" + getClassStr(pinfo.procClassRate, pinfo.procSubClassRate) + ",subTypeStr:" + pinfo.procSubTypeStr + ",appType:" + pinfo.procAppType + ",policy:" + pinfo.procDetailedReason.get("policy") + ",reason:" + pinfo.procReason + ",weight:" + pinfo.procWeight);
                    if (pinfo.procProcessList != null) {
                        for (AwareProcessInfo info : pinfo.procProcessList) {
                            print(pw, "     name:" + info.procProcInfo.mProcessName + ",pid:" + info.procProcInfo.mPid + ",uid:" + info.procProcInfo.mUid + ",group:" + info.procMemGroup + ",import:" + info.procImportance + ",classRate:" + info.procClassRate + ",adj:" + info.procProcInfo.mCurAdj + "," + info.procProcInfo.mAdjType + ",classStr:" + getClassRateStr(info.procClassRate) + ",subStr:" + getClassStr(info.procClassRate, info.procSubClassRate) + ",mResCleanAllow:" + allow + ",mRestartFlag:" + info.getRestartFlag() + ",ui:" + info.procHasShownUi);
                        }
                    }
                }
            }
        }
    }

    private void dumpStringList(PrintWriter pw, List<String> list) {
        if (list != null) {
            for (String pinfo : list) {
                if (pinfo != null) {
                    print(pw, pinfo);
                }
            }
        }
    }

    private void dumpBlock(PrintWriter pw, int memLevel) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppMngSort disabled!");
                return;
            }
            AwareAppMngSortPolicy policy = getAppMngSortPolicy(2, memLevel, 3);
            if (policy == null) {
                pw.println("getAppMngSortPolicy return null!");
            } else {
                dumpPolicy(policy, pw, true, memLevel);
            }
        }
    }

    private void dumpPolicy(AwareAppMngSortPolicy policy, PrintWriter pw, boolean toPrint, int level) {
        if (policy == null) {
            return;
        }
        if (pw != null || !toPrint) {
            print(pw, "------------------start dump Group  forbidstop ------------------");
            dumpBlockList(pw, policy.getForbidStopProcBlockList(), toPrint);
            print(pw, "------------------start dump Group  shortagestop ------------------");
            dumpBlockList(pw, policy.getShortageStopProcBlockList(), toPrint);
            print(pw, "------------------start dump Group  allowstop ------------------");
            List<AwareProcessBlockInfo> list = policy.getAllowStopProcBlockList();
            sortByTimeIfNeed(list, level);
            dumpBlockList(pw, list, toPrint);
        }
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mHandler = new BetaLogHandler(looper);
        } else {
            this.mHandler = new BetaLogHandler(BackgroundThreadEx.getLooper());
        }
    }

    /* access modifiers changed from: private */
    public static final class BetaLogHandler extends Handler {
        public BetaLogHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1 && msg.obj != null && (msg.obj instanceof BetaLog)) {
                ((BetaLog) msg.obj).print();
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class BetaLog {
        private static final char FLAG_ITEM_INNER_SPLIT = ',';
        private static final char FLAG_ITEM_SPLIT = ';';
        private static final char FLAG_NEW_LINE = '\n';
        private static final int INDEX_POLICY = 9;
        private static final int INDEX_STATUS = 3;
        private static final int ITEMS_ONE_LINE = 10;
        private static final int PROCESS_INFO_CNT = 2;
        private List<String> mData = new ArrayList();

        BetaLog(AwareAppMngSortPolicy policy, int level) {
            if (policy.getForbidStopProcBlockList() != null) {
                inflat(policy.getForbidStopProcBlockList(), level);
                inflat(policy.getShortageStopProcBlockList(), level);
                inflat(policy.getAllowStopProcBlockList(), level);
            }
        }

        private void inflat(List<AwareProcessBlockInfo> list, int level) {
            if (list != null) {
                for (AwareProcessBlockInfo pinfo : list) {
                    if (!(pinfo == null || pinfo.procProcessList == null)) {
                        this.mData.add(pinfo.procPackageName);
                        addDetailedReason(pinfo, level);
                    }
                }
            }
        }

        private void addDetailedReason(AwareProcessBlockInfo info, int level) {
            if (!(info == null || info.procDetailedReason == null)) {
                Integer specialReason = info.procDetailedReason.get("spec");
                if (specialReason != null) {
                    int reasonLength = AppMngConstant.CleanReason.values().length;
                    if (specialReason.intValue() >= 0 && specialReason.intValue() < reasonLength) {
                        this.mData.add(AppMngConstant.CleanReason.values()[specialReason.intValue()].getAbbr());
                        return;
                    }
                    return;
                }
                int tagLength = RuleParserUtil.AppMngTag.values().length;
                StringBuilder ruleValue = new StringBuilder();
                for (int i = 0; i < tagLength; i++) {
                    Integer value = info.procDetailedReason.get(RuleParserUtil.AppMngTag.values()[i].getDesc());
                    if (value == null) {
                        ruleValue.append(Integer.toString(-1));
                    } else {
                        ruleValue.append(value.toString());
                        ruleValue.append(getRuleDesc(i, value.intValue()));
                    }
                    if (i != tagLength - 1) {
                        ruleValue.append(FLAG_ITEM_INNER_SPLIT);
                    }
                }
                ruleValue.append(FLAG_ITEM_INNER_SPLIT);
                if (level == 4 || level == 5) {
                    ruleValue.append(info.procWeight);
                } else {
                    ruleValue.append(Integer.toString(-1));
                }
                this.mData.add(ruleValue.toString());
            }
        }

        private String getRuleDesc(int valueIndex, int descIndex) {
            if (descIndex == -1) {
                return "";
            }
            if (valueIndex != 3) {
                if (valueIndex == 9 && descIndex >= 0 && descIndex < ProcessCleaner.CleanType.values().length) {
                    return ProcessCleaner.CleanType.values()[descIndex].description();
                }
                return "";
            } else if (descIndex < 0 || descIndex >= AppStatusUtils.Status.values().length) {
                return "";
            } else {
                return AppStatusUtils.Status.values()[descIndex].description();
            }
        }

        public void print() {
            int size = this.mData.size();
            if (size != 0 && size % 2 == 0) {
                StringBuilder outStr = new StringBuilder();
                int cnt = 0;
                for (String cur : this.mData) {
                    outStr.append(cur);
                    cnt++;
                    if (cnt % 2 != 0) {
                        outStr.append(FLAG_ITEM_INNER_SPLIT);
                    } else if (cnt % 20 == 0) {
                        outStr.append(FLAG_NEW_LINE);
                    } else {
                        outStr.append(FLAG_ITEM_SPLIT);
                    }
                }
                AwareLog.i(AwareAppMngSort.TAG, outStr.toString());
                if (MemoryConstant.isPrintEnhanceSwitch()) {
                    String topnProcMem = MemoryReader.getTopnProcMem();
                    if (!topnProcMem.isEmpty()) {
                        AwareLog.i(AwareAppMngSort.TAG, topnProcMem);
                    }
                }
            }
        }
    }

    private void print(PrintWriter pw, String info) {
        if (pw != null) {
            pw.println(info);
        } else if (sDebug) {
            AwareLog.i(TAG, info);
        }
    }

    public void dump(PrintWriter pw, String type) {
        if (pw != null && type != null) {
            pw.println("  App Group Manager Information dump :");
            if (!dumpForResourceType(pw, type)) {
                if (type.equals("enable")) {
                    enable();
                } else if (type.equals("disable")) {
                    disable();
                } else if (type.equals("checkEnabled")) {
                    boolean status = checkAppMngEnable();
                    pw.println("AwareAppMngSort is " + status);
                } else if (!type.equals("procinfo")) {
                    pw.println("  dump parameter error!");
                } else if (!sEnabled) {
                    pw.println("AwareAppMngSort disabled!");
                } else {
                    ProcessInfoCollector mProcInfo = ProcessInfoCollector.getInstance();
                    if (mProcInfo != null) {
                        updateProcessInfo();
                        mProcInfo.dump(pw);
                    }
                }
            }
        }
    }

    public void dumpRemoveAlarm(PrintWriter pw, String[] args) {
        String pkg = null;
        if (args.length > 2) {
            pkg = args[2];
        }
        if (pkg == null) {
            pw.println("dumpRemoveAlarm package can not be null!");
            return;
        }
        String tag = null;
        if (args.length > 3) {
            tag = args[3];
        }
        int uid = 0;
        if (args.length > 4) {
            try {
                uid = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                pw.println("  please check input uid!");
                return;
            }
        }
        List<String> tags = new ArrayList<>();
        tags.add(tag);
        HwActivityManager.removePackageAlarm(pkg, tags, uid);
        pw.println("dumpRemoveAlarm sucessfull tag:" + tag);
    }

    public void dumpRemoveInvalidAlarm(PrintWriter pw, String[] args) {
        String pkg = null;
        if (args.length > 2) {
            pkg = args[2];
        }
        if (pkg == null) {
            pw.println("dumpRemoveAlarm package can not be null!");
            return;
        }
        int uid = 0;
        if (args.length > 3) {
            try {
                uid = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                pw.println("  please check input uid!");
                return;
            }
        }
        List<String> tags = AwareIntelligentRecg.getInstance().getAllInvalidAlarmTags(uid, pkg);
        if (tags != null && !tags.isEmpty()) {
            HwActivityManager.removePackageAlarm(pkg, tags, uid);
            pw.println("dumpRemoveAlarm sucessfull tags:");
            Iterator<String> it = tags.iterator();
            while (it.hasNext()) {
                pw.println("tag: " + it.next());
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean dumpForResourceType(PrintWriter pw, String type) {
        char c;
        switch (type.hashCode()) {
            case -1831967232:
                if (type.equals("smartClean")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1389206271:
                if (type.equals("bigApp")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -971181661:
                if (type.equals("getCachedCleanPolicy")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -677872268:
                if (type.equals("memClean")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -232627586:
                if (type.equals("memRepairVss2")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 443555725:
                if (type.equals("memCleanAll")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 460796222:
                if (type.equals("memClean2")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 460796223:
                if (type.equals("memClean3")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 865325637:
                if (type.equals("memCleanAll2")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 884096450:
                if (type.equals(MemoryConstant.MEM_POLICY_REPAIR)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1377969204:
                if (type.equals("memRepairVss")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1493492622:
                if (type.equals("memCompact")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1637186224:
                if (type.equals("memRepair2")) {
                    c = '\t';
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
                dumpBlock(pw, 0);
                break;
            case 1:
                dumpBlock(pw, 2);
                break;
            case 2:
                dumpBlock(pw, 3);
                break;
            case 3:
                dumpBlock(pw, 4);
                break;
            case 4:
                dumpBlock(pw, 5);
                break;
            case 5:
                dumpSmartClean(pw);
                break;
            case 6:
                dumpBlock(pw, 1);
                break;
            case 7:
                dumpMemCompactGroup(pw);
                break;
            case '\b':
                dumpMemRepair(pw, 0);
                break;
            case AwareProcessState.STATE_FOREGROUND /* 9 */:
                dumpMemRepair(pw, 1);
                break;
            case '\n':
                dumpMemRepairVss(pw, 0);
                break;
            case 11:
                dumpMemRepairVss(pw, 1);
                break;
            case NetManager.MSG_NET_GAME_ENABLE /* 12 */:
                dumpCachedCleanPolicy(pw);
                break;
            default:
                return false;
        }
        return true;
    }

    private void dumpSmartClean(PrintWriter pw) {
        if (pw != null) {
            print(pw, "------------------start dump Group  small sample list ------------------");
            dumpStringList(pw, AwareIntelligentRecg.getInstance().getSmallSampleList());
            print(pw, "------------------start dump Group  clean Group ------------------");
            dumpBlockList(pw, new SmartClean(this.mContext).getSmartCleanList(null), true);
        }
    }

    private void dumpMemCompactGroup(PrintWriter pw) {
        AwareAppMngSortPolicy policy = getAppMngSortPolicy(1, 0, 3);
        if (policy != null) {
            print(pw, "------------------start dump Group forbid group ------------------");
            dumpBlockList(pw, policy.getForbidStopProcBlockList(), true);
            print(pw, "------------------start dump Group compactible group ------------------");
            dumpBlockList(pw, policy.getShortageStopProcBlockList(), true);
            print(pw, "------------------start dump Group frozen group ------------------");
            dumpBlockList(pw, policy.getAllowStopProcBlockList(), true);
        }
    }

    private void dumpMemRepair(PrintWriter pw, int sceneType) {
        AwareAppMngSortPolicy policy = getAppMngSortPolicyForMemRepair(sceneType);
        if (policy != null) {
            print(pw, "------------------start dump Group forbid group ------------------");
            dumpBlockList(pw, policy.getForbidStopProcBlockList(), true);
            print(pw, "------------------start dump Group allowstop group ------------------");
            dumpBlockList(pw, policy.getAllowStopProcBlockList(), true);
        }
    }

    private void dumpMemRepairVss(PrintWriter pw, int sceneType) {
        AwareAppMngSortPolicy vssPolicy;
        AwareAppMngSortPolicy policy = getAppMngSortPolicyForMemRepair(sceneType);
        if (policy != null && (vssPolicy = getAppMngSortPolicyForMemRepairVss(sceneType, policy)) != null) {
            print(pw, "------------------start dump Group forbid group ------------------");
            dumpBlockList(pw, vssPolicy.getForbidStopProcBlockList(), true);
            print(pw, "------------------start dump Group allowstop group ------------------");
            dumpBlockList(pw, vssPolicy.getAllowStopProcBlockList(), true);
        }
    }

    private void dumpShortageSubClassRate(PrintWriter pw) {
        if (pw != null) {
            ShortageSubClassRate[] allRate = ShortageSubClassRate.values();
            for (ShortageSubClassRate rate : allRate) {
                pw.println("    sub" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
            }
        }
    }

    private void dumpForbidSubClassRate(PrintWriter pw) {
        if (pw != null) {
            ForbidSubClassRate[] allRate = ForbidSubClassRate.values();
            for (ForbidSubClassRate rate : allRate) {
                pw.println("    sub" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
            }
        }
    }

    private void dumpAllowStopSubClassRate(PrintWriter pw) {
        if (pw != null) {
            AllowStopSubClassRate[] allRate = AllowStopSubClassRate.values();
            for (AllowStopSubClassRate rate : allRate) {
                pw.println("    sub" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
            }
        }
    }

    public void dumpClassInfo(PrintWriter pw) {
        if (!sEnabled) {
            pw.println("AwareAppMngSort disabled!");
        } else if (pw != null) {
            ClassRate[] allRate = ClassRate.values();
            for (ClassRate rate : allRate) {
                pw.println("Class" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
                String subClass = ShortageSubClassRate.NONE.description();
                if (rate == ClassRate.FOREGROUND) {
                    dumpForbidSubClassRate(pw);
                } else if (rate == ClassRate.KEYSERVICES) {
                    dumpShortageSubClassRate(pw);
                } else if (rate == ClassRate.NORMAL) {
                    dumpAllowStopSubClassRate(pw);
                } else {
                    pw.println("sub" + ShortageSubClassRate.NONE.ordinal() + ": value=" + ShortageSubClassRate.NONE.ordinal() + "," + subClass);
                }
            }
        }
    }

    public void dumpAlarm(PrintWriter pw, String[] args) {
        String pkg = null;
        if (args.length > 2) {
            pkg = args[2];
        }
        if (pkg == null) {
            pw.println("dumpAlarm package can not be null!");
            return;
        }
        int uid = 0;
        if (args.length > 3) {
            try {
                uid = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                pw.println("  please check input uid!");
                return;
            }
        }
        List<String> tags = AwareIntelligentRecg.getInstance().getAllInvalidAlarmTags(uid, pkg);
        if (tags == null || tags.isEmpty()) {
            pw.println("getAllInvalidAlarmTags is null or empty.");
        } else {
            pw.println("getAllInvalidAlarmTags:" + tags);
        }
        List<String> packageList = new ArrayList<>();
        packageList.add(pkg);
        boolean has = AwareIntelligentRecg.getInstance().hasPerceptAlarm(uid, packageList);
        pw.println("hasPerceptAlarm: " + has);
    }

    private void updateLevel2ListFilter() {
        ArrayList<String> rawFilter = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_CLEAN.getDesc(), LIST_FILTER_LEVEL2);
        ArraySet<String> listFilter = new ArraySet<>();
        if (rawFilter != null) {
            listFilter.addAll(rawFilter);
            this.mListFilterLevel2 = listFilter;
        }
    }

    public void updateAppMngConfig() {
        updateFilteredApps();
        updateLevel2ListFilter();
    }

    private void setWeightForLowEnd(List<AwareProcessBlockInfo> needClean) {
        if (!(needClean == null || needClean.isEmpty())) {
            List<AwareProcessInfo> allAwareProcNeedProcess = AppStatusUtils.getInstance().getAllProcNeedSort();
            if (allAwareProcNeedProcess == null || allAwareProcNeedProcess.isEmpty()) {
                AwareLog.e(TAG, "getAllProcNeedSort failed!");
                return;
            }
            List<AwareProcessBlockInfo> cleanWeight = CleanSource.mergeBlockForMemory(DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, new DecisionMaker.DecideConfigInfo(14, AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.MEMORY), (ArraySet<String>) null, true), null);
            if (cleanWeight != null) {
                ArrayMap<Integer, ArrayMap<String, AwareProcessBlockInfo>> weightWithIndex = new ArrayMap<>();
                int size = cleanWeight.size();
                for (int i = 0; i < size; i++) {
                    AwareProcessBlockInfo info = cleanWeight.get(i);
                    int userId = UserHandleEx.getUserId(info.procUid);
                    String pkgName = info.procPackageName;
                    ArrayMap<String, AwareProcessBlockInfo> weightMap = weightWithIndex.get(Integer.valueOf(userId));
                    if (weightMap == null) {
                        weightMap = new ArrayMap<>();
                    }
                    weightMap.put(pkgName, info);
                    weightWithIndex.put(Integer.valueOf(userId), weightMap);
                }
                ArrayMap<String, Integer> recentAppsWeight = AwareAppUseDataManager.getInstance().getRecentAppsWeight();
                boolean needCheckRecentUse = true;
                if (recentAppsWeight == null || recentAppsWeight.isEmpty()) {
                    needCheckRecentUse = false;
                }
                int needCleanSize = needClean.size();
                for (int i2 = 0; i2 < needCleanSize; i2++) {
                    AwareProcessBlockInfo info2 = needClean.get(i2);
                    setWeightForDecided(info2, weightWithIndex.get(Integer.valueOf(UserHandleEx.getUserId(info2.procUid))), info2.procPackageName);
                    if (needCheckRecentUse) {
                        setWeightForRecentUse(info2, info2.procPackageName, getRecentUseWeight(recentAppsWeight, info2.procPackageName, info2.procUid));
                    }
                }
            }
        }
    }

    private void setWeightForRecentUse(AwareProcessBlockInfo info, String pkg, int recentUseWeight) {
        if (recentUseWeight != -1 && recentUseWeight > info.procWeight) {
            info.procWeight = recentUseWeight;
            info.procReason += AwareAppUseDataManager.getInstance().getWeightSetReason();
        }
    }

    private void setWeightForDecided(AwareProcessBlockInfo info, ArrayMap<String, AwareProcessBlockInfo> weightMap, String pkgName) {
        AwareProcessBlockInfo weight;
        if (weightMap != null && (weight = weightMap.get(pkgName)) != null && weight.procDetailedReason != null && info.procDetailedReason != null && weight.procWeight > info.procWeight) {
            info.procWeight = weight.procWeight;
            info.procReason = weight.procReason;
        }
    }

    private void checkIfNothingCanKill(List<AwareProcessBlockInfo> needClean) {
        if (needClean != null && needClean.isEmpty()) {
            HwStartWindowCache.getInstance().notifyMemCritical();
        }
    }

    public void sortByTimeIfNeed(List<AwareProcessBlockInfo> procs, int level) {
        ArrayList<String> recentApp;
        if (!(procs == null || isLowLevel(level) || (recentApp = AwareAppUseDataManager.getInstance().getRecentApp()) == null || recentApp.isEmpty())) {
            List<AwareProcessBlockInfo> recentUseApp = new ArrayList<>();
            Iterator<String> it = recentApp.iterator();
            while (it.hasNext()) {
                String app = it.next();
                if (app != null) {
                    Iterator<AwareProcessBlockInfo> iter = procs.iterator();
                    while (iter.hasNext()) {
                        AwareProcessBlockInfo info = iter.next();
                        if (info != null && isCurUser(info.procUid) && app.equals(info.procPackageName)) {
                            recentUseApp.add(info);
                            iter.remove();
                        }
                    }
                }
            }
            for (AwareProcessBlockInfo app2 : recentUseApp) {
                procs.add(0, app2);
            }
        }
    }

    private boolean isLowLevel(int memLevel) {
        return memLevel == 3 || memLevel == 4 || memLevel == 5;
    }

    private boolean isCurUser(int uid) {
        return AwareIntelligentRecg.getInstance().isCurrentUser(uid, AwareAppAssociate.getInstance().getCurUserId());
    }

    private int getRecentUseWeight(ArrayMap<String, Integer> recentAppsWeight, String pkg, int uid) {
        Integer weight;
        if (pkg == null || !isCurUser(uid) || (weight = recentAppsWeight.get(pkg)) == null) {
            return -1;
        }
        return weight.intValue();
    }

    public AwareAppMngSortPolicy getCachedCleanPolicy() {
        if (!sEnabled) {
            return new AwareAppMngSortPolicy();
        }
        long startTime = 0;
        if (sDebug) {
            startTime = System.currentTimeMillis();
        }
        AwareAppMngSortPolicy cacheGroup = getCachedCleanPolicyInner();
        if (sDebug) {
            AwareLog.i(TAG, "getCachedCleanPolicy eclipse time:" + (System.currentTimeMillis() - startTime));
        }
        return cacheGroup;
    }

    private AwareAppMngSortPolicy getCachedCleanPolicyInner() {
        List<AwareProcessInfo> allProcs = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (allProcs == null || allProcs.isEmpty()) {
            AwareLog.e(TAG, "getCachedCleanPolicyInner failed!");
            return new AwareAppMngSortPolicy();
        }
        List<AwareProcessBlockInfo> rawInfo = DecisionMaker.getInstance().decideForCachedMem(allProcs);
        if (rawInfo == null) {
            return new AwareAppMngSortPolicy();
        }
        List<AwareProcessBlockInfo> canClean = new ArrayList<>();
        Iterator<AwareProcessBlockInfo> iterator = rawInfo.iterator();
        while (iterator.hasNext()) {
            AwareProcessBlockInfo curBlock = iterator.next();
            if (curBlock == null) {
                iterator.remove();
            } else if (!ProcessCleaner.CleanType.NONE.equals(curBlock.procCleanType) && !checkForProcessList(curBlock)) {
                iterator.remove();
                canClean.add(curBlock);
            }
        }
        Collections.sort(canClean, BLOCK_BY_ADJ);
        return new AwareAppMngSortPolicy(this.mContext, getAppGroupById(3, rawInfo, canClean));
    }

    private boolean checkForProcessList(AwareProcessBlockInfo curBlock) {
        AwareProcessInfo processInfo;
        String processName;
        ArrayMap<String, ListItem> processList;
        ListItem item;
        if (curBlock == null || curBlock.procProcessList == null || curBlock.procProcessList.isEmpty() || (processInfo = curBlock.procProcessList.get(0)) == null || processInfo.procProcInfo == null) {
            return false;
        }
        if ((AppSceneMngFeature.isEnable() && processInfo.procListPolicy == 1) || (processName = processInfo.procProcInfo.mProcessName) == null || (processList = DecisionMaker.getInstance().getProcessList(AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.CACHED_MEMORY)) == null || (item = processList.get(processName)) == null || item.getPolicy() != ProcessCleaner.CleanType.NONE.ordinal()) {
            return false;
        }
        curBlock.procCleanType = ProcessCleaner.CleanType.NONE;
        setReasonForProcessList(curBlock, item.getPolicy());
        return true;
    }

    private void setReasonForProcessList(AwareProcessBlockInfo curBlock, int policy) {
        ArrayMap<String, Integer> detailedReason = new ArrayMap<>();
        detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(policy));
        detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.PROCESS_LIST.ordinal()));
        curBlock.procReason = AppMngConstant.CleanReason.PROCESS_LIST.getCode();
        curBlock.procDetailedReason = detailedReason;
    }

    private void dumpCachedCleanPolicy(PrintWriter pw) {
        if (pw != null) {
            if (!sEnabled) {
                pw.println("AwareAppMngSort disabled!");
            } else if (!CachedMemoryCleanPolicy.getInstance().isCachedMemoryEnable()) {
                pw.println("Cached memory clean is disabled!");
            } else {
                AwareAppMngSortPolicy policy = getCachedCleanPolicy();
                if (policy == null) {
                    pw.println("dumpCachedCleanPolicy return null!");
                    return;
                }
                print(pw, "------------------start dump Group  forbidstop ------------------");
                for (AwareProcessBlockInfo info : policy.getForbidStopProcBlockList()) {
                    if (info != null) {
                        pw.println(info.toStringWithProcName());
                    }
                }
                print(pw, "------------------start dump Group  allowstop ------------------");
                for (AwareProcessBlockInfo info2 : policy.getAllowStopProcBlockList()) {
                    if (info2 != null) {
                        pw.println(info2.toStringWithProcName());
                    }
                }
            }
        }
    }
}
