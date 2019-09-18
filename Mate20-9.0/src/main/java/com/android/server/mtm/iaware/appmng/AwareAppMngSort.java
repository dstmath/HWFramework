package com.android.server.mtm.iaware.appmng;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.mtm.iaware.appmng.appclean.SmartClean;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppLruBase;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import com.huawei.android.app.HwActivityManager;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        public int compare(AwareProcessBlockInfo arg0, AwareProcessBlockInfo arg1) {
            if (arg0 == null) {
                if (arg1 == null) {
                    return 0;
                }
                return -1;
            } else if (arg1 == null) {
                return 1;
            } else {
                return arg0.mMinAdj - arg1.mMinAdj;
            }
        }
    };
    private static final Comparator<AwareProcessBlockInfo> BLOCK_BY_IMPORTANCE = new Comparator<AwareProcessBlockInfo>() {
        public int compare(AwareProcessBlockInfo arg0, AwareProcessBlockInfo arg1) {
            return arg1.mImportance - arg0.mImportance;
        }
    };
    private static final Comparator<AwareProcessBlockInfo> BLOCK_BY_USER_HABIT = new Comparator<AwareProcessBlockInfo>() {
        public int compare(AwareProcessBlockInfo arg0, AwareProcessBlockInfo arg1) {
            if (arg0 == null) {
                if (arg1 == null) {
                    return 0;
                }
                return -1;
            } else if (arg1 == null) {
                return 1;
            } else {
                return arg0.mImportance - arg1.mImportance;
            }
        }
    };
    private static final Comparator<AwareProcessBlockInfo> BLOCK_BY_WEIGHT = new Comparator<AwareProcessBlockInfo>() {
        public int compare(AwareProcessBlockInfo arg1, AwareProcessBlockInfo arg0) {
            if (arg0 == null) {
                if (arg1 == null) {
                    return 0;
                }
                return -1;
            } else if (arg1 == null) {
                return 1;
            } else {
                return arg0.mWeight - arg1.mWeight;
            }
        }
    };
    private static final int CLASSRATE_KEY_OFFSET = 8;
    private static final String CSP_APPS = "csp_apps";
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    public static final String EXEC_SERVICES = "exec-service";
    public static final String FG_SERVICE = "fg-service";
    public static final long FOREVER_DECAYTIME = -1;
    public static final int HABITMAX_IMPORT = 10000;
    public static final int HABITMIN_IMPORT = 0;
    private static final int INVALID_VALUE = -1;
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
    private static final int MSG_PRINT_BETA_LOG = 1;
    public static final long PREVIOUS_APP_DIRCACTIVITY_DECAYTIME = 600000;
    private static final int SEC_PER_MIN = 60;
    private static final String SUBTYPE_ASSOCIATION = "assoc";
    private static final String TAG = "AwareAppMngSort";
    private static final String TAG_POLICY = "policy";
    private static final int TOP_N_IMPORT_RATE = -100;
    private static ArrayList<String> filteredApps = null;
    private static boolean mEnabled = false;
    private static AwareAppMngSort sInstance = null;
    private boolean mAssocEnable = true;
    private final Context mContext;
    private Handler mHandler = null;
    private HwActivityManagerService mHwAMS = null;
    private long mLastLevelPrintTime = 0;
    private ArrayMap<Integer, Long> mLastPrintTimeByLevel = new ArrayMap<>();
    private ArraySet<String> mListFilterLevel2 = null;

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
        
        String mDescription;

        private AllowStopSubClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    static class AppBlockKeyBase {
        public int mPid = 0;
        public int mUid = 0;

        public AppBlockKeyBase(int pid, int uid) {
            this.mPid = pid;
            this.mUid = uid;
            if (AwareAppMngSort.DEBUG) {
                AwareLog.d(AwareAppMngSort.TAG, "AppBlockKeyBase constructor pid:" + this.mPid + ",uid:" + this.mUid);
            }
        }
    }

    private static final class BetaLog {
        private static final char FLAG_ITEM_INNER_SPLIT = ',';
        private static final char FLAG_ITEM_SPLIT = ';';
        private static final char FLAG_NEW_LINE = '\n';
        private static final int ITEMS_ONE_LINE = 10;
        private static final int PROCESS_INFO_CNT = 2;
        private List<String> mData = new ArrayList();

        BetaLog(AwareAppMngSortPolicy policy) {
            if (policy.getForbidStopProcBlockList() != null) {
                inflat(policy.getForbidStopProcBlockList());
                inflat(policy.getShortageStopProcBlockList());
                inflat(policy.getAllowStopProcBlockList());
            }
        }

        private void inflat(List<AwareProcessBlockInfo> list) {
            if (list != null) {
                for (AwareProcessBlockInfo pinfo : list) {
                    if (!(pinfo == null || pinfo.mProcessList == null)) {
                        this.mData.add(pinfo.mPackageName);
                        addDetailedReason(pinfo);
                    }
                }
            }
        }

        private void addDetailedReason(AwareProcessBlockInfo info) {
            if (info != null && info.mDetailedReason != null) {
                Integer specialReason = info.mDetailedReason.get("spec");
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
                    Integer value = info.mDetailedReason.get(RuleParserUtil.AppMngTag.values()[i].getDesc());
                    if (value == null) {
                        ruleValue.append(Integer.toString(-1));
                    } else {
                        ruleValue.append(value.toString());
                    }
                    if (i != tagLength - 1) {
                        ruleValue.append(FLAG_ITEM_INNER_SPLIT);
                    }
                }
                this.mData.add(ruleValue.toString());
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
            }
        }
    }

    private static final class BetaLogHandler extends Handler {
        public BetaLogHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                BetaLog betaLog = (BetaLog) msg.obj;
                if (betaLog != null) {
                    betaLog.print();
                }
            }
        }
    }

    static class CachedWhiteList {
        private final ArraySet<String> mAllHabitCacheList = new ArraySet<>();
        final Set<String> mAllProtectApp = new ArraySet();
        final Set<String> mAllUnProtectApp = new ArraySet();
        private Map<String, AwareDefaultConfigList.PackageConfigItem> mAwareProtectCacheMap = new ArrayMap();
        final ArraySet<String> mBadAppList = new ArraySet<>();
        final ArraySet<String> mBgNonDecayPkg = new ArraySet<>();
        private final ArraySet<String> mKeyHabitCacheList = new ArraySet<>();
        private boolean mLowEnd = false;
        final ArraySet<String> mRestartAppList = new ArraySet<>();

        public void updateCachedList() {
            AwareDefaultConfigList whiteListInstance = AwareDefaultConfigList.getInstance();
            if (whiteListInstance != null) {
                this.mLowEnd = whiteListInstance.isLowEnd();
                this.mKeyHabitCacheList.addAll(whiteListInstance.getKeyHabitAppList());
                this.mAllHabitCacheList.addAll(whiteListInstance.getAllHabitAppList());
                this.mRestartAppList.addAll(whiteListInstance.getRestartAppList());
                this.mBadAppList.addAll(whiteListInstance.getBadAppList());
                AwareUserHabit habit = AwareUserHabit.getInstance();
                if (habit != null) {
                    Set<String> bgNonDcyApp = habit.getBackgroundApps(AppMngConfig.getBgDecay() * 60);
                    if (bgNonDcyApp != null) {
                        this.mBgNonDecayPkg.addAll(bgNonDcyApp);
                    }
                }
                this.mAwareProtectCacheMap = whiteListInstance.getAwareProtectMap();
            }
        }

        /* access modifiers changed from: private */
        public boolean isLowEnd() {
            return this.mLowEnd;
        }

        public boolean isInKeyHabitList(ArrayList<String> packageNames) {
            if (packageNames == null || packageNames.isEmpty()) {
                return false;
            }
            int size = packageNames.size();
            for (int i = 0; i < size; i++) {
                if (this.mKeyHabitCacheList.contains(packageNames.get(i))) {
                    return true;
                }
            }
            return false;
        }

        public boolean isInAllHabitList(ArrayList<String> packageNames) {
            if (packageNames == null || packageNames.isEmpty()) {
                return false;
            }
            int size = packageNames.size();
            for (int i = 0; i < size; i++) {
                if (this.mAllHabitCacheList.contains(packageNames.get(i))) {
                    return true;
                }
            }
            return false;
        }

        private AwareDefaultConfigList.ProcessConfigItem getAwareWhiteListItem(ArrayList<String> packageNames, String processName) {
            if (packageNames == null || packageNames.isEmpty() || this.mAwareProtectCacheMap == null) {
                return null;
            }
            int size = packageNames.size();
            for (int i = 0; i < size; i++) {
                String pkgName = packageNames.get(i);
                if (this.mAwareProtectCacheMap.containsKey(pkgName)) {
                    AwareDefaultConfigList.PackageConfigItem pkgItem = this.mAwareProtectCacheMap.get(pkgName);
                    if (pkgItem == null) {
                        return null;
                    }
                    if (pkgItem.isEmpty()) {
                        return pkgItem.copy();
                    }
                    AwareDefaultConfigList.ProcessConfigItem procItem = pkgItem.getItem(processName);
                    if (procItem == null) {
                        return null;
                    }
                    return procItem.copy();
                }
            }
            return null;
        }

        private int getGroupId(AwareDefaultConfigList.ProcessConfigItem item) {
            if (item == null) {
                return 2;
            }
            int group = 2;
            switch (item.mGroupId) {
                case 1:
                    group = 0;
                    break;
                case 2:
                    group = 1;
                    break;
            }
            return group;
        }

        /* access modifiers changed from: private */
        public void updateProcessInfoByConfig(AwareProcessInfo processInfo) {
            if (processInfo != null && processInfo.mProcInfo != null) {
                AwareDefaultConfigList.ProcessConfigItem item = getAwareWhiteListItem(processInfo.mProcInfo.mPackageName, processInfo.mProcInfo.mProcessName);
                if (item != null) {
                    processInfo.mXmlConfig = new AwareProcessInfo.XmlConfig(getGroupId(item), item.mFrequentlyUsed, item.mResCleanAllow, item.mRestartFlag);
                }
            }
        }
    }

    public enum ClassRate {
        NONE("none"),
        PERSIST("persist"),
        FOREGROUND(MemoryConstant.MEM_REPAIR_CONSTANT_FG),
        KEYBACKGROUND("keybackground"),
        HOME("home"),
        KEYSERVICES("keyservices"),
        NORMAL("normal"),
        UNKNOWN("unknown");
        
        String mDescription;

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
        
        String mDescription;

        private ForbidSubClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    private static class MemSortGroup {
        public List<AwareProcessBlockInfo> mProcAllowStopList = null;
        public List<AwareProcessBlockInfo> mProcForbidStopList = null;
        public List<AwareProcessBlockInfo> mProcShortageStopList = null;

        public MemSortGroup(List<AwareProcessBlockInfo> procForbidStopList, List<AwareProcessBlockInfo> procShortageStopList, List<AwareProcessBlockInfo> procAllowStopList) {
            this.mProcForbidStopList = procForbidStopList;
            this.mProcShortageStopList = procShortageStopList;
            this.mProcAllowStopList = procAllowStopList;
        }
    }

    private static class ProcessHabitCompare implements Comparator<AwareProcessInfo>, Serializable {
        private static final long serialVersionUID = 1;

        private ProcessHabitCompare() {
        }

        public int compare(AwareProcessInfo arg0, AwareProcessInfo arg1) {
            if (arg0 == null || arg1 == null) {
                return 0;
            }
            return arg0.mImportance - arg1.mImportance;
        }
    }

    static class ShortageProcessInfo {
        public Map<Integer, AwareProcessInfo> mAllProcNeedSort;
        final ArrayMap<Integer, AwareProcessInfo> mAudioIn = new ArrayMap<>();
        final ArrayMap<Integer, AwareProcessInfo> mAudioOut = new ArrayMap<>();
        private final ArraySet<Integer> mForeGroundServiceUid = new ArraySet<>();
        private final ArrayMap<Integer, ArrayList<String>> mForeGroundUid = new ArrayMap<>();
        Set<Integer> mHabitTopN;
        private int mHomeProcessPid;
        private final Set<Integer> mHomeStrong = new ArraySet();
        private Set<Integer> mKeyPercepServicePid;
        public boolean mKillMore;
        final ArrayMap<Integer, AwareProcessInfo> mNonCurUserProc;
        public AwareAppLruBase mPrevAmsBase;
        public AwareAppLruBase mPrevAwareBase;
        public AwareAppLruBase mRecentTaskAppBase;
        private boolean mRecentTaskShow;
        final Set<String> mVisibleWinPkg;
        final Set<String> mWidgetPkg;

        public boolean isRecentTaskShow() {
            return this.mRecentTaskShow;
        }

        public boolean isFgServicesUid(int uid) {
            return this.mForeGroundServiceUid.contains(Integer.valueOf(uid));
        }

        public void recordFgServicesUid(int uid) {
            this.mForeGroundServiceUid.add(Integer.valueOf(uid));
        }

        public boolean isForegroundUid(ProcessInfo procInfo) {
            if (procInfo == null || !this.mForeGroundUid.containsKey(Integer.valueOf(procInfo.mUid))) {
                return false;
            }
            if (!AwareAppAssociate.isDealAsPkgUid(procInfo.mUid)) {
                return true;
            }
            return AwareAppMngSort.isPkgIncludeForTgt(procInfo.mPackageName, this.mForeGroundUid.get(Integer.valueOf(procInfo.mUid)));
        }

        public boolean isKeyPercepService(int pid) {
            if (this.mKeyPercepServicePid == null) {
                return false;
            }
            return this.mKeyPercepServicePid.contains(Integer.valueOf(pid));
        }

        public void recordForegroundUid(int uid, ArrayList<String> packageList) {
            if (AwareAppAssociate.isDealAsPkgUid(uid)) {
                this.mForeGroundUid.put(Integer.valueOf(uid), packageList);
            } else {
                this.mForeGroundUid.put(Integer.valueOf(uid), null);
            }
        }

        /* access modifiers changed from: private */
        public boolean isAudioSubClass(ProcessInfo procInfo, Map<Integer, AwareProcessInfo> audioInfo) {
            if (procInfo == null) {
                return false;
            }
            for (Map.Entry<Integer, AwareProcessInfo> m : audioInfo.entrySet()) {
                AwareProcessInfo info = m.getValue();
                if (procInfo.mPid == info.mProcInfo.mPid) {
                    return true;
                }
                if (procInfo.mUid == info.mProcInfo.mUid && (!AwareAppAssociate.isDealAsPkgUid(procInfo.mUid) || AwareAppMngSort.isPkgIncludeForTgt(procInfo.mPackageName, info.mProcInfo.mPackageName))) {
                    return true;
                }
            }
            return false;
        }

        public void updateBaseInfo(Map<Integer, AwareProcessInfo> allProcNeedSort, int homePid, boolean recentTaskShow, Set<Integer> keyPercepServicePid) {
            this.mAllProcNeedSort = allProcNeedSort;
            this.mHabitTopN = getHabitAppTopNVer2(allProcNeedSort, AppMngConfig.getTopN());
            updateVisibleWin();
            updateWidget();
            this.mHomeProcessPid = homePid;
            this.mRecentTaskShow = recentTaskShow;
            this.mKeyPercepServicePid = keyPercepServicePid;
            loadHomeAssoc(this.mHomeProcessPid, allProcNeedSort);
            this.mPrevAmsBase = AwareAppAssociate.getInstance().getPreviousByAmsInfo();
            this.mPrevAwareBase = AwareAppAssociate.getInstance().getPreviousAppInfo();
            this.mRecentTaskAppBase = AwareAppAssociate.getInstance().getRecentTaskPrevInfo();
        }

        public ShortageProcessInfo(int memLevel) {
            boolean z = false;
            this.mRecentTaskShow = false;
            this.mAllProcNeedSort = null;
            this.mPrevAmsBase = null;
            this.mPrevAwareBase = null;
            this.mRecentTaskAppBase = null;
            this.mNonCurUserProc = new ArrayMap<>();
            this.mVisibleWinPkg = new ArraySet();
            this.mWidgetPkg = new ArraySet();
            this.mKillMore = false;
            if (AppMngConfig.getKillMoreFlag() && memLevel == 1) {
                z = true;
            }
            this.mKillMore = z;
        }

        /* access modifiers changed from: private */
        public boolean isHomeAssocStrong(AwareProcessInfo awareProcInfo) {
            if (awareProcInfo == null || awareProcInfo.mProcInfo == null) {
                return false;
            }
            if (awareProcInfo.mProcInfo.mCurAdj == 600 && (awareProcInfo.mProcInfo.mType == 2 || awareProcInfo.mProcInfo.mType == 3)) {
                return true;
            }
            return this.mHomeStrong.contains(Integer.valueOf(awareProcInfo.mPid));
        }

        private void loadHomeAssoc(int homePid, Map<Integer, AwareProcessInfo> allProc) {
            Set<Integer> homeStrong = new ArraySet<>();
            AwareAppAssociate.getInstance().getAssocListForPid(homePid, homeStrong);
            for (Integer pid : homeStrong) {
                AwareProcessInfo awareProcInfo = allProc.get(pid);
                if (awareProcInfo != null && awareProcInfo.mProcInfo != null && !awareProcInfo.mHasShownUi && awareProcInfo.mProcInfo.mType == 2) {
                    this.mHomeStrong.add(pid);
                }
            }
        }

        /* access modifiers changed from: private */
        public boolean isHabitTopN(int pid) {
            return this.mHabitTopN != null && this.mHabitTopN.contains(Integer.valueOf(pid));
        }

        public boolean isHomeProcess(int pid) {
            return this.mHomeProcessPid == pid;
        }

        public int getKeyBackgroupTypeInternal(ProcessInfo procInfo) {
            if (procInfo == null) {
                return -1;
            }
            return AwareAppKeyBackgroup.getInstance().getKeyBackgroupTypeInternal(procInfo.mPid, procInfo.mUid, procInfo.mPackageName);
        }

        public int getKeyBackgroupTypeInternalByPid(ProcessInfo procInfo) {
            if (procInfo == null) {
                return -1;
            }
            return AwareAppKeyBackgroup.getInstance().getKeyBackgroupTypeInternal(procInfo.mPid, procInfo.mUid, null);
        }

        private Set<Integer> getHabitAppTopNVer2(Map<Integer, AwareProcessInfo> proc, int topN) {
            Set<Integer> procTopN = new ArraySet<>();
            if (proc == null) {
                AwareLog.e(AwareAppMngSort.TAG, "proc = null!");
                return procTopN;
            }
            AwareUserHabit habit = AwareUserHabit.getInstance();
            if (habit == null) {
                AwareLog.e(AwareAppMngSort.TAG, "AwareUserHabit is null");
                return procTopN;
            }
            List<String> pkgTopN = habit.getTopN(topN);
            if (pkgTopN == null) {
                AwareLog.e(AwareAppMngSort.TAG, "pkgTopN = null!");
                return procTopN;
            }
            for (Map.Entry<Integer, AwareProcessInfo> pm : proc.entrySet()) {
                AwareProcessInfo info = pm.getValue();
                if (!(info == null || info.mImportance == 10000 || info.mProcInfo == null || !AwareAppMngSort.isPkgIncludeForTgt(pkgTopN, info.mProcInfo.mPackageName))) {
                    procTopN.add(Integer.valueOf(info.mPid));
                }
            }
            return procTopN;
        }

        private void updateVisibleWin() {
            if (this.mAllProcNeedSort != null) {
                Set<Integer> visibleWindows = new ArraySet<>();
                AwareAppAssociate.getInstance().getVisibleWindows(visibleWindows, null);
                for (Integer pid : visibleWindows) {
                    AwareProcessInfo procInfo = this.mAllProcNeedSort.get(pid);
                    if (!(procInfo == null || procInfo.mProcInfo.mPackageName == null)) {
                        this.mVisibleWinPkg.addAll(procInfo.mProcInfo.mPackageName);
                    }
                }
            }
        }

        private void updateWidget() {
            if (this.mAllProcNeedSort != null) {
                Set<String> widgets = AwareAppAssociate.getInstance().getWidgetsPkg();
                if (widgets != null) {
                    this.mWidgetPkg.addAll(widgets);
                }
            }
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
        
        String mDescription;
        int subClass;

        private ShortageSubClassRate(String description) {
            this.mDescription = description;
            this.subClass = -1;
        }

        public String description() {
            return this.mDescription;
        }

        public int getSubClassRate() {
            return this.subClass;
        }
    }

    private AwareAppMngSort(Context context) {
        this.mContext = context;
        this.mHandler = new BetaLogHandler(BackgroundThread.get().getLooper());
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
        mEnabled = true;
    }

    public static void disable() {
        mEnabled = false;
    }

    private boolean containsVisibleWindow(Set<String> visibleWindowList, List<String> pkgList) {
        if (visibleWindowList == null || pkgList == null || visibleWindowList.isEmpty()) {
            return false;
        }
        for (String pkg : pkgList) {
            if (visibleWindowList.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    private void loadAppAssoc(List<AwareProcessInfo> procs, Map<Integer, AwareProcessInfo> pidsClass, Map<Integer, AwareProcessInfo> strongAssocProc) {
        if (procs != null && !procs.isEmpty() && pidsClass != null && strongAssocProc != null) {
            Set<Integer> strong = new ArraySet<>();
            for (AwareProcessInfo procInfoBase : procs) {
                int pid = procInfoBase.mPid;
                strong.clear();
                loadAssocListForPid(pid, pidsClass, strong, strongAssocProc);
            }
        }
    }

    private boolean isAssocRelation(AwareProcessInfo client, AwareProcessInfo app) {
        if (app == null || client == null || client.mProcInfo == null) {
            return false;
        }
        if (!app.mHasShownUi || app.mPid == getCurHomeProcessPid() || client.mProcInfo.mCurAdj <= 200) {
            return true;
        }
        return false;
    }

    private void loadAssocListForPid(int pid, Map<Integer, AwareProcessInfo> pidsClass, Set<Integer> strong, Map<Integer, AwareProcessInfo> strongAssocProc) {
        if (pidsClass != null && strongAssocProc != null && strong != null) {
            AwareAppAssociate.getInstance().getAssocListForPid(pid, strong);
            for (Integer sPid : strong) {
                AwareProcessInfo procInfo = pidsClass.get(sPid);
                if (procInfo != null && isAssocRelation(pidsClass.get(Integer.valueOf(pid)), procInfo)) {
                    strongAssocProc.put(sPid, procInfo);
                }
            }
        }
    }

    private ArrayMap<Integer, AwareProcessInfo> getNeedSortedProcesses(Map<Integer, AwareProcessInfo> foreGrdProc, Set<Integer> keyPercepServicePid, CachedWhiteList cachedWhitelist, ShortageProcessInfo shortageProc) {
        int myPid;
        int uid;
        Map<Integer, AwareProcessBaseInfo> baseInfos;
        ArrayList<ProcessInfo> procs;
        AwareAppMngSort awareAppMngSort = this;
        Set<Integer> set = keyPercepServicePid;
        ShortageProcessInfo shortageProcessInfo = shortageProc;
        ArrayList<ProcessInfo> procs2 = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procs2.isEmpty()) {
            return null;
        }
        Map<Integer, AwareProcessBaseInfo> baseInfos2 = awareAppMngSort.mHwAMS != null ? awareAppMngSort.mHwAMS.getAllProcessBaseInfo() : null;
        if (baseInfos2 == null) {
            Map<Integer, AwareProcessInfo> map = foreGrdProc;
            CachedWhiteList cachedWhiteList = cachedWhitelist;
            ArrayList<ProcessInfo> arrayList = procs2;
            Map<Integer, AwareProcessBaseInfo> map2 = baseInfos2;
        } else if (baseInfos2.isEmpty()) {
            Map<Integer, AwareProcessInfo> map3 = foreGrdProc;
            CachedWhiteList cachedWhiteList2 = cachedWhitelist;
            ArrayList<ProcessInfo> arrayList2 = procs2;
            Map<Integer, AwareProcessBaseInfo> map4 = baseInfos2;
        } else {
            int curUserUid = AwareAppAssociate.getInstance().getCurUserId();
            Set<Integer> fgServiceUid = new ArraySet<>();
            ArraySet<Integer> importUid = new ArraySet<>();
            ArrayMap<Integer, Integer> percepServicePid = new ArrayMap<>();
            ArrayMap<Integer, AwareProcessInfo> allProcNeedSort = new ArrayMap<>();
            int i = 0;
            int size = procs2.size();
            while (i < size) {
                ProcessInfo procInfo = procs2.get(i);
                if (procInfo != null) {
                    AwareProcessBaseInfo updateInfo = baseInfos2.get(Integer.valueOf(procInfo.mPid));
                    if (updateInfo != null) {
                        procInfo.mCurAdj = updateInfo.mCurAdj;
                        procInfo.mForegroundActivities = updateInfo.mForegroundActivities;
                        procInfo.mAdjType = updateInfo.mAdjType;
                        procs = procs2;
                        AwareProcessBaseInfo updateInfo2 = updateInfo;
                        AwareProcessInfo awareProcessInfo = new AwareProcessInfo(procInfo.mPid, 0, 0, ClassRate.NORMAL.ordinal(), procInfo);
                        AwareProcessInfo awareProcInfo = awareProcessInfo;
                        awareProcInfo.mHasShownUi = updateInfo2.mHasShownUi;
                        cachedWhitelist.updateProcessInfoByConfig(awareProcInfo);
                        if (curUserUid != 0 || awareAppMngSort.isCurUserProc(procInfo.mUid, curUserUid)) {
                            allProcNeedSort.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                            if (procInfo.mForegroundActivities) {
                                foreGrdProc.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                                AwareProcessBaseInfo awareProcessBaseInfo = updateInfo2;
                                shortageProcessInfo.recordForegroundUid(procInfo.mUid, procInfo.mPackageName);
                            } else {
                                Map<Integer, AwareProcessInfo> map5 = foreGrdProc;
                                AwareProcessBaseInfo awareProcessBaseInfo2 = updateInfo2;
                            }
                            if (procInfo.mCurAdj >= 200) {
                                boolean audioIn = false;
                                baseInfos = baseInfos2;
                                boolean audioOut = shortageProcessInfo.getKeyBackgroupTypeInternalByPid(procInfo) == 2;
                                if (audioOut) {
                                    shortageProcessInfo.mAudioOut.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                                } else {
                                    if (shortageProcessInfo.getKeyBackgroupTypeInternalByPid(procInfo) == 1) {
                                        audioIn = true;
                                    }
                                    if (audioIn) {
                                        boolean z = audioOut;
                                        shortageProcessInfo.mAudioIn.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                                    }
                                }
                            } else {
                                baseInfos = baseInfos2;
                            }
                            if (procInfo.mCurAdj < 200) {
                                importUid.add(Integer.valueOf(procInfo.mUid));
                            } else if (procInfo.mCurAdj == 200 && FG_SERVICE.equals(procInfo.mAdjType)) {
                                fgServiceUid.add(Integer.valueOf(procInfo.mUid));
                            } else if (procInfo.mCurAdj == 200 && ADJTYPE_SERVICE.equals(procInfo.mAdjType)) {
                                percepServicePid.put(Integer.valueOf(procInfo.mPid), Integer.valueOf(procInfo.mUid));
                            } else if (procInfo.mCurAdj == 200) {
                                importUid.add(Integer.valueOf(procInfo.mUid));
                            }
                            i++;
                            procs2 = procs;
                            baseInfos2 = baseInfos;
                            awareAppMngSort = this;
                        } else {
                            shortageProcessInfo.mNonCurUserProc.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                            Map<Integer, AwareProcessInfo> map6 = foreGrdProc;
                            baseInfos = baseInfos2;
                            i++;
                            procs2 = procs;
                            baseInfos2 = baseInfos;
                            awareAppMngSort = this;
                        }
                    }
                }
                Map<Integer, AwareProcessInfo> map7 = foreGrdProc;
                CachedWhiteList cachedWhiteList3 = cachedWhitelist;
                procs = procs2;
                baseInfos = baseInfos2;
                i++;
                procs2 = procs;
                baseInfos2 = baseInfos;
                awareAppMngSort = this;
            }
            Map<Integer, AwareProcessInfo> map8 = foreGrdProc;
            CachedWhiteList cachedWhiteList4 = cachedWhitelist;
            ArrayList<ProcessInfo> arrayList3 = procs2;
            Map<Integer, AwareProcessBaseInfo> map9 = baseInfos2;
            int myPid2 = Process.myPid();
            for (Map.Entry<Integer, Integer> m : percepServicePid.entrySet()) {
                int pid = m.getKey().intValue();
                int uid2 = m.getValue().intValue();
                if (importUid.contains(Integer.valueOf(uid2))) {
                    set.add(Integer.valueOf(pid));
                } else if (!fgServiceUid.contains(Integer.valueOf(uid2))) {
                    set.add(Integer.valueOf(pid));
                } else {
                    Set<Integer> strong = new ArraySet<>();
                    AwareAppAssociate.getInstance().getAssocClientListForPid(pid, strong);
                    Iterator<Integer> it = strong.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            myPid = myPid2;
                            uid = uid2;
                            break;
                        }
                        Integer clientPid = it.next();
                        uid = uid2;
                        if (clientPid.intValue() == myPid2) {
                            set.add(Integer.valueOf(pid));
                            myPid = myPid2;
                            break;
                        }
                        AwareProcessInfo awareProcInfo2 = allProcNeedSort.get(clientPid);
                        if (awareProcInfo2 != null) {
                            myPid = myPid2;
                            if (awareProcInfo2.mProcInfo != null) {
                                Integer num = clientPid;
                                if (awareProcInfo2.mProcInfo.mCurAdj <= 200) {
                                    set.add(Integer.valueOf(pid));
                                    break;
                                }
                            }
                            uid2 = uid;
                            myPid2 = myPid;
                            ShortageProcessInfo shortageProcessInfo2 = shortageProc;
                        } else {
                            uid2 = uid;
                            ShortageProcessInfo shortageProcessInfo3 = shortageProc;
                        }
                    }
                    myPid2 = myPid;
                    ShortageProcessInfo shortageProcessInfo4 = shortageProc;
                }
                myPid = myPid2;
                uid = uid2;
                myPid2 = myPid;
                ShortageProcessInfo shortageProcessInfo42 = shortageProc;
            }
            return allProcNeedSort;
        }
        return null;
    }

    private boolean isCurUserProc(int checkUid, int curUserUid) {
        int userId = UserHandle.getUserId(checkUid);
        boolean isCloned = false;
        if (this.mContext != null) {
            UserManager usm = UserManager.get(this.mContext);
            if (usm != null) {
                UserInfo info = usm.getUserInfo(userId);
                isCloned = info != null ? info.isClonedProfile() : false;
            }
        }
        if (userId == curUserUid || isCloned) {
            return true;
        }
        return false;
    }

    private void groupNonCurUserProc(ArrayMap<Integer, AwareProcessBlockInfo> classNormal, ArrayMap<Integer, AwareProcessInfo> nonCurUserProc) {
        if (classNormal != null && nonCurUserProc != null) {
            for (Map.Entry<Integer, AwareProcessInfo> m : nonCurUserProc.entrySet()) {
                AwareProcessInfo awareProcInfo = m.getValue();
                if (awareProcInfo != null) {
                    awareProcInfo.mClassRate = ClassRate.NORMAL.ordinal();
                    awareProcInfo.mSubClassRate = AllowStopSubClassRate.NONCURUSER.ordinal();
                    addProcessInfoToBlock(classNormal, awareProcInfo, awareProcInfo.mPid);
                }
            }
        }
    }

    private boolean isSystemProcess(ProcessInfo procInfo) {
        boolean z = false;
        if (procInfo == null) {
            return false;
        }
        if (procInfo.mType == 2) {
            z = true;
        }
        return z;
    }

    private boolean groupIntoForbidstop(ShortageProcessInfo shortageProc, AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        int curAdj = awareProcInfo.mProcInfo.mCurAdj;
        int classType = ClassRate.UNKNOWN.ordinal();
        int subClassType = ForbidSubClassRate.NONE.ordinal();
        boolean isGroup = true;
        if (curAdj < 0) {
            classType = ClassRate.PERSIST.ordinal();
        } else if (curAdj < 200) {
            classType = ClassRate.FOREGROUND.ordinal();
        } else if (awareProcInfo.mXmlConfig == null || !isCfgDefaultGroup(awareProcInfo, 0)) {
            isGroup = false;
        } else {
            classType = ClassRate.FOREGROUND.ordinal();
            subClassType = ForbidSubClassRate.AWARE_PROTECTED.ordinal();
        }
        if (isGroup) {
            awareProcInfo.mClassRate = classType;
            awareProcInfo.mSubClassRate = subClassType;
        }
        return isGroup;
    }

    /* access modifiers changed from: private */
    public static boolean isPkgIncludeForTgt(List<String> tgtPkg, List<String> dstPkg) {
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

    private boolean isLastRecentlyUsedBase(ProcessInfo procInfo, Map<Integer, AwareProcessInfo> allProcNeedSort, AwareAppLruBase appLruBase, long decayTime) {
        if (procInfo == null || allProcNeedSort == null || appLruBase == null || procInfo.mUid != appLruBase.mUid) {
            return false;
        }
        if (decayTime != -1 && SystemClock.elapsedRealtime() - appLruBase.mTime > decayTime) {
            return false;
        }
        if (!AwareAppAssociate.isDealAsPkgUid(procInfo.mUid)) {
            return true;
        }
        AwareProcessInfo prevProcInfo = allProcNeedSort.get(Integer.valueOf(appLruBase.mPid));
        if (prevProcInfo == null) {
            return false;
        }
        return isPkgIncludeForTgt(procInfo.mPackageName, prevProcInfo.mProcInfo.mPackageName);
    }

    private boolean isPerceptable(ProcessInfo procInfo) {
        if (procInfo != null && procInfo.mCurAdj == 200 && !FG_SERVICE.equals(procInfo.mAdjType) && !ADJTYPE_SERVICE.equals(procInfo.mAdjType)) {
            return true;
        }
        return false;
    }

    private boolean isFgServices(ProcessInfo procInfo, ShortageProcessInfo shortageProc) {
        if (procInfo == null || procInfo.mCurAdj != 200) {
            return false;
        }
        if (FG_SERVICE.equals(procInfo.mAdjType) && (isSystemProcess(procInfo) || shortageProc.isForegroundUid(procInfo))) {
            return true;
        }
        if (!ADJTYPE_SERVICE.equals(procInfo.mAdjType) || (!isSystemProcess(procInfo) && !shortageProc.isForegroundUid(procInfo))) {
            return false;
        }
        return true;
    }

    public boolean isFgServicesImportantByAdjtype(String adjType) {
        if (FG_SERVICE.equals(adjType) || ADJTYPE_SERVICE.equals(adjType)) {
            return true;
        }
        return false;
    }

    private boolean isFgServicesImportant(ProcessInfo procInfo) {
        if (procInfo != null && procInfo.mCurAdj == 200) {
            return isFgServicesImportantByAdjtype(procInfo.mAdjType);
        }
        return false;
    }

    private boolean groupIntoShortageStop(AwareProcessInfo awareProcInfo, ShortageProcessInfo shortageProc, CachedWhiteList cachedWhitelist) {
        if (awareProcInfo == null) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        int curAdj = procInfo.mCurAdj;
        int classType = ClassRate.UNKNOWN.ordinal();
        int subClass = ShortageSubClassRate.HW_SYSTEM.ordinal();
        boolean isGroup = true;
        if (isPerceptable(procInfo) || shortageProc.isKeyPercepService(procInfo.mPid)) {
            classType = ClassRate.KEYBACKGROUND.ordinal();
            subClass = ShortageSubClassRate.NONE.ordinal();
        } else if (curAdj == 300) {
            classType = ClassRate.KEYBACKGROUND.ordinal();
            subClass = ShortageSubClassRate.NONE.ordinal();
        } else if (curAdj == 400) {
            classType = ClassRate.KEYBACKGROUND.ordinal();
            subClass = ShortageSubClassRate.NONE.ordinal();
        } else if (shortageProc.isHomeProcess(procInfo.mPid) || shortageProc.isHomeAssocStrong(awareProcInfo)) {
            classType = ClassRate.HOME.ordinal();
            subClass = ShortageSubClassRate.NONE.ordinal();
        } else if (shortageProc.isRecentTaskShow() && isRecentTaskShowApp(procInfo, shortageProc)) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.PREV_ONECLEAN.ordinal();
        } else if (shortageProc.isAudioSubClass(procInfo, shortageProc.mAudioOut)) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.MUSIC_PLAY.ordinal();
        } else if (shortageProc.isAudioSubClass(procInfo, shortageProc.mAudioIn)) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.SOUND_RECORD.ordinal();
        } else if (isFgServices(procInfo, shortageProc)) {
            classType = ClassRate.KEYSERVICES.ordinal();
            shortageProc.recordFgServicesUid(procInfo.mUid);
            subClass = ShortageSubClassRate.FGSERVICES_TOPN.ordinal();
        } else if (cachedWhitelist.isInKeyHabitList(procInfo.mPackageName)) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.KEY_IM.ordinal();
        } else if (shortageProc.getKeyBackgroupTypeInternal(procInfo) == 3) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.GUIDE.ordinal();
        } else if (shortageProc.getKeyBackgroupTypeInternal(procInfo) == 5) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.DOWN_UP_LOAD.ordinal();
        } else if (cachedWhitelist.isLowEnd() || shortageProc.getKeyBackgroupTypeInternal(procInfo) != 4) {
            if (isLastRecentlyUsed(procInfo, shortageProc, shortageProc.mKillMore ? PREVIOUS_APP_DIRCACTIVITY_DECAYTIME : -1)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.PREVIOUS.ordinal();
            } else if (awareProcInfo.mXmlConfig != null && isCfgDefaultGroup(awareProcInfo, 1) && (!awareProcInfo.mXmlConfig.mFrequentlyUsed || shortageProc.isHabitTopN(procInfo.mPid))) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.AWARE_PROTECTED.ordinal();
            } else if (!cachedWhitelist.isLowEnd() && isKeySysProc(awareProcInfo)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.KEY_SYS_SERVICE.ordinal();
            } else if (shortageProc.isForegroundUid(awareProcInfo.mProcInfo)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.ASSOC_WITH_FG.ordinal();
            } else if (containsVisibleWindow(shortageProc.mVisibleWinPkg, procInfo.mPackageName)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.VISIBLEWIN.ordinal();
            } else if (!shortageProc.mKillMore && shortageProc.isHabitTopN(procInfo.mPid)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.TOPN.ordinal();
            } else if (isWidget(shortageProc.mWidgetPkg, procInfo.mPackageName)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.WIDGET.ordinal();
            } else {
                isGroup = false;
            }
        } else {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.HEALTH.ordinal();
        }
        if (isGroup) {
            awareProcInfo.mClassRate = classType;
            awareProcInfo.mSubClassRate = subClass;
        }
        return isGroup;
    }

    private boolean isLastRecentlyUsed(ProcessInfo procInfo, ShortageProcessInfo shortageProc, long decayTime) {
        boolean z = true;
        if (decayTime == -1 && procInfo.mCurAdj == 700) {
            return true;
        }
        if (!isLastRecentlyUsedBase(procInfo, shortageProc.mAllProcNeedSort, shortageProc.mPrevAmsBase, decayTime)) {
            if (!isLastRecentlyUsedBase(procInfo, shortageProc.mAllProcNeedSort, shortageProc.mPrevAwareBase, decayTime)) {
                z = false;
            }
        }
        return z;
    }

    private boolean isWidget(Set<String> widgets, List<String> pkgList) {
        if (widgets == null || pkgList == null || widgets.isEmpty()) {
            return false;
        }
        for (String pkg : pkgList) {
            if (widgets.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    private boolean isClock(Set<String> clocks, ArrayList<String> packageNames) {
        if (clocks != null && !clocks.isEmpty() && packageNames != null && !packageNames.isEmpty()) {
            int size = packageNames.size();
            for (int i = 0; i < size; i++) {
                String pkg = packageNames.get(i);
                if (pkg != null && clocks.contains(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRecentTaskShow(ArrayMap<Integer, AwareProcessInfo> foreGrdProc) {
        if (foreGrdProc == null || foreGrdProc.size() > 0) {
            return false;
        }
        return true;
    }

    private boolean isRecentTaskShowApp(ProcessInfo procInfo, ShortageProcessInfo shortageProc) {
        return isLastRecentlyUsedBase(procInfo, shortageProc.mAllProcNeedSort, shortageProc.mRecentTaskAppBase, -1);
    }

    private boolean isKeySysProc(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (!isSystemProcess(procInfo)) {
            return false;
        }
        if (procInfo.mCurAdj == 500) {
            return true;
        }
        if (procInfo.mUid < 10000 && procInfo.mCurAdj == 800) {
            return true;
        }
        if (procInfo.mUid < 10000 && !awareProcInfo.mHasShownUi && procInfo.mCreatedTime != -1 && SystemClock.elapsedRealtime() - procInfo.mCreatedTime < AppMngConfig.getKeySysDecay()) {
            return true;
        }
        if (procInfo.mUid < 10000 || awareProcInfo.mHasShownUi || procInfo.mCreatedTime == -1 || SystemClock.elapsedRealtime() - procInfo.mCreatedTime >= AppMngConfig.getSysDecay()) {
            return false;
        }
        return true;
    }

    private boolean groupIntoAllowStop(AwareProcessInfo awareProcInfo, ShortageProcessInfo shortageProc, CachedWhiteList cachedWhitelist) {
        int subClassType;
        if (awareProcInfo == null) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (shortageProc.mKillMore && isLastRecentlyUsed(procInfo, shortageProc, -1)) {
            subClassType = AllowStopSubClassRate.PREVIOUS.ordinal();
        } else if (shortageProc.mKillMore != 0 && shortageProc.isHabitTopN(procInfo.mPid)) {
            subClassType = AllowStopSubClassRate.TOPN.ordinal();
        } else if (cachedWhitelist.isLowEnd() && isKeySysProc(awareProcInfo)) {
            subClassType = AllowStopSubClassRate.KEY_SYS_SERVICE.ordinal();
        } else if (cachedWhitelist.isLowEnd() && shortageProc.getKeyBackgroupTypeInternal(procInfo) == 4) {
            subClassType = AllowStopSubClassRate.HEALTH.ordinal();
        } else if (isFgServicesImportant(procInfo)) {
            subClassType = AllowStopSubClassRate.FG_SERVICES.ordinal();
        } else {
            subClassType = AllowStopSubClassRate.OTHER.ordinal();
        }
        awareProcInfo.mClassRate = ClassRate.NORMAL.ordinal();
        awareProcInfo.mSubClassRate = subClassType;
        return true;
    }

    private void addProcessInfoToBlock(Map<Integer, AwareProcessBlockInfo> appAllClass, AwareProcessInfo pinfo, int key) {
        if (appAllClass != null && pinfo != null) {
            AwareProcessBlockInfo info = new AwareProcessBlockInfo(pinfo.mProcInfo.mUid, false, pinfo.mClassRate);
            info.mProcessList.add(pinfo);
            info.mSubClassRate = pinfo.mSubClassRate;
            info.mImportance = pinfo.mImportance;
            info.mMinAdj = pinfo.mProcInfo.mCurAdj;
            appAllClass.put(Integer.valueOf(key), info);
        }
    }

    private boolean isBgDecayApp(String pkgName, CachedWhiteList cachedWhitelist) {
        if (pkgName == null || cachedWhitelist == null || cachedWhitelist.mBgNonDecayPkg.contains(pkgName)) {
            return false;
        }
        return true;
    }

    private boolean getRestartFlagByProc(int classRate, int subRate, List<String> pkg, CachedWhiteList cachedWhitelist, boolean isRestartByAppType, ProcessInfo procInfo, int appType, AwareProcessBlockInfo value) {
        if (pkg == null || cachedWhitelist == null || subRate == AllowStopSubClassRate.NONE.ordinal() || subRate == AllowStopSubClassRate.PREVIOUS.ordinal() || subRate == AllowStopSubClassRate.TOPN.ordinal() || subRate == AllowStopSubClassRate.HEALTH.ordinal() || subRate == AllowStopSubClassRate.NONCURUSER.ordinal() || subRate == AllowStopSubClassRate.UNKNOWN.ordinal()) {
            return true;
        }
        if (cachedWhitelist.isLowEnd() && subRate == AllowStopSubClassRate.KEY_SYS_SERVICE.ordinal()) {
            return false;
        }
        for (String pkgName : pkg) {
            if (cachedWhitelist.mRestartAppList.contains(pkgName)) {
                return true;
            }
            if (cachedWhitelist.mAllProtectApp.contains(pkgName) && (!cachedWhitelist.isLowEnd() || !isBgDecayApp(pkgName, cachedWhitelist))) {
                return true;
            }
            if (!cachedWhitelist.mBadAppList.contains(pkgName) && isRestartByAppType) {
                if (AppMngConfig.getAbroadFlag() && cachedWhitelist.mAllUnProtectApp.contains(pkgName)) {
                    value.mAlarmChk = true;
                } else if (cachedWhitelist.isLowEnd()) {
                    if (!isBgDecayApp(pkgName, cachedWhitelist)) {
                        return true;
                    }
                    if (!isDecayAppByAppType(appType)) {
                        value.mAlarmChk = true;
                    }
                } else if (!isDecayAppByAppType(appType) || !isBgDecayApp(pkgName, cachedWhitelist)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean getAllowCleanResByProc(int classRate, int subRate, CachedWhiteList cachedWhitelist, ArrayList<String> pkgList, boolean isRestartByAppType) {
        boolean z = false;
        if (cachedWhitelist == null) {
            return false;
        }
        if (!cachedWhitelist.isLowEnd()) {
            return true;
        }
        if (subRate != AllowStopSubClassRate.FG_SERVICES.ordinal()) {
            if (subRate == AllowStopSubClassRate.OTHER.ordinal()) {
                z = true;
            }
            return z;
        } else if (!isRestartByAppType) {
            return true;
        } else {
            if (!AppMngConfig.getAbroadFlag() || pkgList == null || pkgList.isEmpty()) {
                return false;
            }
            int size = pkgList.size();
            for (int i = 0; i < size; i++) {
                if (!cachedWhitelist.mAllUnProtectApp.contains(pkgList.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean getAllowCleanResByAppType(int appType) {
        if (appType != 14) {
            return true;
        }
        return false;
    }

    private void updateAppType(AwareProcessBlockInfo info, CachedWhiteList cachedWhitelist) {
        if (info != null) {
            for (AwareProcessInfo procInfo : info.mProcessList) {
                if (!(procInfo == null || procInfo.mProcInfo == null || procInfo.mProcInfo.mPackageName == null)) {
                    int size = procInfo.mProcInfo.mPackageName.size();
                    for (int i = 0; i < size; i++) {
                        int type = AppTypeRecoManager.getInstance().getAppType((String) procInfo.mProcInfo.mPackageName.get(i));
                        if (!getRestartFlagByAppType(type)) {
                            info.mAppType = type;
                        } else if (isDecayAppByAppType(type)) {
                            info.mAppType = type;
                        } else {
                            info.mAppType = -1;
                            return;
                        }
                    }
                    continue;
                }
            }
        }
    }

    private boolean getRestartFlagByAppType(int appType) {
        switch (appType) {
            case 3:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 12:
            case 13:
            case 14:
            case 15:
            case 17:
            case 18:
            case 19:
                return false;
            default:
                return true;
        }
    }

    private boolean isDecayAppByAppType(int appType) {
        switch (appType) {
            case 20:
            case 21:
                return true;
            default:
                return false;
        }
    }

    public boolean needCheckAlarm(AwareProcessBlockInfo info) {
        if (info == null) {
            return false;
        }
        if (AppMngConfig.getAlarmCheckFlag()) {
            return true;
        }
        return info.mAlarmChk;
    }

    /* JADX WARNING: Removed duplicated region for block: B:62:0x014d A[LOOP:2: B:60:0x0147->B:62:0x014d, LOOP_END] */
    private void addClassToAllClass(Map<Integer, AwareProcessBlockInfo> appAllClass, Map<Integer, AwareProcessBlockInfo> blocks, ShortageProcessInfo shortageProc, Map<Integer, AwareProcessBlockInfo> allUids, CachedWhiteList cachedWhitelist, Set<String> clocks, boolean chkRestartFlag) {
        Iterator<Map.Entry<Integer, AwareProcessBlockInfo>> it;
        CachedWhiteList cachedWhiteList = cachedWhitelist;
        Iterator<Map.Entry<Integer, AwareProcessBlockInfo>> it2 = blocks.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry<Integer, AwareProcessBlockInfo> m = it2.next();
            int pidValue = m.getKey().intValue();
            AwareProcessBlockInfo value = m.getValue();
            appAllClass.put(Integer.valueOf(pidValue), value);
            if (chkRestartFlag) {
                boolean isAllowCleanRes = true;
                boolean isRestart = false;
                boolean isRestartByAppType = false;
                if (AppMngConfig.getAbroadFlag() || AppMngConfig.getRestartFlag()) {
                    isRestartByAppType = true;
                } else {
                    updateAppType(value, cachedWhiteList);
                    if (getRestartFlagByAppType(value.mAppType)) {
                        isRestartByAppType = true;
                    } else if (!cachedWhitelist.isLowEnd() && !getAllowCleanResByAppType(value.mAppType)) {
                        isAllowCleanRes = false;
                    }
                }
                boolean isRestartByAppType2 = isRestartByAppType;
                Iterator<AwareProcessInfo> it3 = value.mProcessList.iterator();
                boolean isWidgetApp = false;
                boolean isClockApp = false;
                boolean isImApp = false;
                boolean isAllowCleanRes2 = isAllowCleanRes;
                while (it3.hasNext()) {
                    AwareProcessInfo procInfo = it3.next();
                    if (!AppMngConfig.getRestartFlag()) {
                        it = it2;
                        AwareProcessInfo procInfo2 = procInfo;
                        Iterator<AwareProcessInfo> it4 = it3;
                        if (getRestartFlagByProc(value.mClassRate, value.mSubClassRate, procInfo.mProcInfo.mPackageName, cachedWhiteList, isRestartByAppType2, procInfo.mProcInfo, value.mAppType, value)) {
                            ShortageProcessInfo shortageProcessInfo = shortageProc;
                            Set<String> set = clocks;
                        } else {
                            if (isAllowCleanRes2) {
                                if (!isWidgetApp) {
                                    isWidgetApp = isWidget(shortageProc.mWidgetPkg, procInfo2.mProcInfo.mPackageName);
                                } else {
                                    ShortageProcessInfo shortageProcessInfo2 = shortageProc;
                                }
                                if (!isClockApp) {
                                    isClockApp = isClock(clocks, procInfo2.mProcInfo.mPackageName);
                                } else {
                                    Set<String> set2 = clocks;
                                }
                                if (!isImApp) {
                                    isImApp = cachedWhiteList.isInAllHabitList(procInfo2.mProcInfo.mPackageName);
                                }
                                if (procInfo2.mXmlConfig != null) {
                                    isAllowCleanRes2 = procInfo2.mXmlConfig.mResCleanAllow;
                                }
                                if (isAllowCleanRes2) {
                                    if (!getAllowCleanResByProc(value.mClassRate, value.mSubClassRate, cachedWhiteList, procInfo2.mProcInfo.mPackageName, isRestartByAppType2)) {
                                        isAllowCleanRes2 = false;
                                    }
                                }
                            }
                            it3 = it4;
                            it2 = it;
                        }
                    } else {
                        ShortageProcessInfo shortageProcessInfo3 = shortageProc;
                        it = it2;
                        AwareProcessInfo awareProcessInfo = procInfo;
                        Set<String> set3 = clocks;
                    }
                    isRestart = true;
                }
                ShortageProcessInfo shortageProcessInfo4 = shortageProc;
                Set<String> set4 = clocks;
                it = it2;
                if (isRestart || isWidgetApp || isClockApp || isImApp || !isAllowCleanRes2) {
                    Map<Integer, AwareProcessBlockInfo> map = allUids;
                } else {
                    if (inSameUids(allUids, value.mProcessList)) {
                        value.mResCleanAllow = true;
                        value.mCleanAlarm = true;
                        if (isRestart || isImApp) {
                            for (AwareProcessInfo procInfo3 : value.mProcessList) {
                                procInfo3.mRestartFlag = true;
                            }
                        }
                        it2 = it;
                    }
                }
                value.mResCleanAllow = false;
                value.mCleanAlarm = false;
                while (r1.hasNext()) {
                }
                it2 = it;
            }
        }
        Map<Integer, AwareProcessBlockInfo> map2 = appAllClass;
        ShortageProcessInfo shortageProcessInfo5 = shortageProc;
        Map<Integer, AwareProcessBlockInfo> map3 = allUids;
        Set<String> set5 = clocks;
    }

    private void addProcessInfoToGroupBlock(Map<Integer, AwareProcessBlockInfo> appAllClass, AwareProcessInfo pinfo, int key, Map<Integer, Map<Integer, AwareProcessBlockInfo>> groupBlock) {
        Integer groupKey = Integer.valueOf((pinfo.mClassRate << 8) + pinfo.mSubClassRate);
        Map<Integer, AwareProcessBlockInfo> groupUid = groupBlock.get(groupKey);
        if (groupUid == null) {
            groupUid = new ArrayMap<>();
            groupBlock.put(groupKey, groupUid);
        }
        AwareProcessBlockInfo block = groupUid.get(Integer.valueOf(pinfo.mProcInfo.mUid));
        if (block == null) {
            AwareProcessBlockInfo block2 = new AwareProcessBlockInfo(pinfo.mProcInfo.mUid, false, pinfo.mClassRate);
            block2.mProcessList.add(pinfo);
            block2.mSubClassRate = pinfo.mSubClassRate;
            block2.mImportance = pinfo.mImportance;
            block2.mMinAdj = pinfo.mProcInfo.mCurAdj;
            groupUid.put(Integer.valueOf(pinfo.mProcInfo.mUid), block2);
            appAllClass.put(Integer.valueOf(key), block2);
            return;
        }
        if (block.mImportance > pinfo.mImportance) {
            block.mImportance = pinfo.mImportance;
        }
        if (block.mMinAdj > pinfo.mProcInfo.mCurAdj) {
            block.mMinAdj = pinfo.mProcInfo.mCurAdj;
        }
        block.mProcessList.add(pinfo);
    }

    private boolean isCfgDefaultGroup(AwareProcessInfo procInfo, int groupId) {
        boolean z = false;
        if (procInfo == null || procInfo.mXmlConfig == null) {
            return false;
        }
        if (procInfo.mXmlConfig.mCfgDefaultGroup == groupId) {
            z = true;
        }
        return z;
    }

    private ArrayMap<Integer, AwareProcessBlockInfo> getAppMemSortClassGroup(int subType) {
        ArraySet arraySet = new ArraySet();
        ArrayMap arrayMap = new ArrayMap();
        ShortageProcessInfo shortageProc = new ShortageProcessInfo(subType);
        CachedWhiteList cachedWhitelist = new CachedWhiteList();
        cachedWhitelist.updateCachedList();
        Set<String> clocks = AppTypeRecoManager.getInstance().getAlarmApps();
        ArrayMap<Integer, AwareProcessInfo> allProcNeedSort = getNeedSortedProcesses(arrayMap, arraySet, cachedWhitelist, shortageProc);
        if (allProcNeedSort == null) {
            return null;
        }
        Map<Integer, Map<Integer, AwareProcessBlockInfo>> appAllClass = new ArrayMap<>();
        ArrayMap<Integer, AwareProcessBlockInfo> classShort = new ArrayMap<>();
        ArrayMap<Integer, AwareProcessBlockInfo> classNormal = new ArrayMap<>();
        ArrayMap arrayMap2 = new ArrayMap();
        Map<Integer, AwareProcessBlockInfo> allUids = groupByUid(allProcNeedSort);
        shortageProc.updateBaseInfo(allProcNeedSort, getCurHomeProcessPid(), isRecentTaskShow(arrayMap), arraySet);
        Iterator<Map.Entry<Integer, AwareProcessInfo>> it = allProcNeedSort.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, AwareProcessInfo> m = it.next();
            AwareProcessInfo awareProcInfo = m.getValue();
            boolean isGroup = groupIntoForbidstop(shortageProc, awareProcInfo);
            if (!isGroup) {
                isGroup = groupIntoShortageStop(awareProcInfo, shortageProc, cachedWhitelist);
            }
            if (!isGroup) {
                groupIntoAllowStop(awareProcInfo, shortageProc, cachedWhitelist);
            }
            Iterator<Map.Entry<Integer, AwareProcessInfo>> it2 = it;
            Map.Entry<Integer, AwareProcessInfo> entry = m;
            if (awareProcInfo.mClassRate < ClassRate.KEYBACKGROUND.ordinal()) {
                addProcessInfoToBlock(appAllClass, awareProcInfo, awareProcInfo.mPid);
            } else if (awareProcInfo.mClassRate < ClassRate.NORMAL.ordinal()) {
                addProcessInfoToGroupBlock(classShort, awareProcInfo, awareProcInfo.mPid, arrayMap2);
            } else {
                addProcessInfoToBlock(classNormal, awareProcInfo, awareProcInfo.mPid);
            }
            it = it2;
        }
        groupNonCurUserProc(classNormal, shortageProc.mNonCurUserProc);
        ArrayMap arrayMap3 = arrayMap2;
        ArrayMap<Integer, AwareProcessBlockInfo> arrayMap4 = classNormal;
        ArrayMap<Integer, AwareProcessBlockInfo> appAllClass2 = appAllClass;
        ArrayMap<Integer, AwareProcessInfo> arrayMap5 = allProcNeedSort;
        adjustClassRate(allProcNeedSort, classShort, classNormal, appAllClass, shortageProc, allUids, cachedWhitelist, clocks);
        addClassToAllClass(appAllClass2, classShort, shortageProc, allUids, cachedWhitelist, clocks, false);
        return appAllClass2;
    }

    private Map<AppBlockKeyBase, AwareProcessBlockInfo> convertToUidBlock(Map<Integer, AwareProcessBlockInfo> pidsBlock, ArrayMap<Integer, AppBlockKeyBase> pidsAppBlock, ArrayMap<Integer, AppBlockKeyBase> uidAppBlock) {
        AppBlockKeyBase blockKeyValue;
        ArrayMap<Integer, AppBlockKeyBase> arrayMap = pidsAppBlock;
        ArrayMap<Integer, AppBlockKeyBase> arrayMap2 = uidAppBlock;
        if (pidsBlock == null) {
            return null;
        }
        Map<AppBlockKeyBase, AwareProcessBlockInfo> uids = new ArrayMap<>();
        for (Map.Entry<Integer, AwareProcessBlockInfo> m : pidsBlock.entrySet()) {
            AwareProcessBlockInfo blockInfo = m.getValue();
            if (blockInfo.mProcessList != null) {
                for (AwareProcessInfo awareProcInfo : blockInfo.mProcessList) {
                    if (AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
                        blockKeyValue = arrayMap.get(Integer.valueOf(awareProcInfo.mProcInfo.mPid));
                    } else {
                        blockKeyValue = arrayMap2.get(Integer.valueOf(awareProcInfo.mProcInfo.mUid));
                    }
                    AwareProcessBlockInfo info = blockKeyValue == null ? null : uids.get(blockKeyValue);
                    if (info == null) {
                        AwareProcessBlockInfo info2 = new AwareProcessBlockInfo(awareProcInfo.mProcInfo.mUid, false, awareProcInfo.mClassRate);
                        info2.mProcessList.add(awareProcInfo);
                        info2.mClassRate = blockInfo.mClassRate;
                        info2.mSubClassRate = blockInfo.mSubClassRate;
                        AppBlockKeyBase keyBase = new AppBlockKeyBase(awareProcInfo.mProcInfo.mPid, awareProcInfo.mProcInfo.mUid);
                        if (AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
                            arrayMap.put(Integer.valueOf(awareProcInfo.mProcInfo.mPid), keyBase);
                        } else {
                            arrayMap2.put(Integer.valueOf(awareProcInfo.mProcInfo.mUid), keyBase);
                        }
                        uids.put(keyBase, info2);
                    } else {
                        if (info.mSubClassRate > awareProcInfo.mSubClassRate) {
                            info.mSubClassRate = awareProcInfo.mSubClassRate;
                        }
                        info.mProcessList.add(awareProcInfo);
                    }
                }
            }
        }
        return uids;
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
                    importance = userHabitMap.get(Integer.valueOf(info.mPid));
                }
                if (importance != null) {
                    info.mImportance = importance.intValue();
                } else {
                    info.mImportance = 10000;
                }
                AwareProcessBlockInfo block = uids.get(Integer.valueOf(info.mProcInfo.mUid));
                if (block == null) {
                    AwareProcessBlockInfo block2 = new AwareProcessBlockInfo(info.mProcInfo.mUid, false, info.mClassRate);
                    block2.mProcessList.add(info);
                    uids.put(Integer.valueOf(info.mProcInfo.mUid), block2);
                    block2.mImportance = info.mImportance;
                } else {
                    block.mProcessList.add(info);
                    if (block.mImportance > info.mImportance) {
                        block.mImportance = info.mImportance;
                    }
                }
            }
        }
        return uids;
    }

    private void adjustClassByStrongAssoc(AwareProcessBlockInfo blockInfo, Map<Integer, AwareProcessInfo> allProcNeedSort, Map<AppBlockKeyBase, AwareProcessBlockInfo> uids, ArrayMap<Integer, AppBlockKeyBase> pidsAppBlock, ArrayMap<Integer, AppBlockKeyBase> uidAppBlock, Map<AppBlockKeyBase, AwareProcessBlockInfo> assocNormalUids) {
        AppBlockKeyBase blockKeyValue;
        ArrayMap<Integer, AwareProcessInfo> strong = new ArrayMap<>();
        loadAppAssoc(blockInfo.mProcessList, allProcNeedSort, strong);
        for (Map.Entry<Integer, AwareProcessInfo> sm : strong.entrySet()) {
            AwareProcessInfo procInfo = allProcNeedSort.get(sm.getKey());
            if (!(procInfo == null || procInfo.mProcInfo == null)) {
                if (AwareAppAssociate.isDealAsPkgUid(procInfo.mProcInfo.mUid)) {
                    blockKeyValue = pidsAppBlock.get(Integer.valueOf(procInfo.mProcInfo.mPid));
                } else {
                    blockKeyValue = uidAppBlock.get(Integer.valueOf(procInfo.mProcInfo.mUid));
                }
                AwareProcessBlockInfo blockInfoAssoc = blockKeyValue == null ? null : uids.get(blockKeyValue);
                if (!(blockInfoAssoc == null || blockInfoAssoc.mProcessList == null || blockInfoAssoc.mProcessList.size() <= 0)) {
                    if (blockInfoAssoc.mClassRate > blockInfo.mClassRate) {
                        blockInfoAssoc.mClassRate = blockInfo.mClassRate;
                        blockInfoAssoc.mSubClassRate = blockInfo.mSubClassRate;
                        blockInfoAssoc.mSubTypeStr = SUBTYPE_ASSOCIATION;
                        if (assocNormalUids != null) {
                            assocNormalUids.put(blockKeyValue, blockInfoAssoc);
                        }
                    } else if (blockInfoAssoc.mClassRate == blockInfo.mClassRate && blockInfoAssoc.mSubClassRate > blockInfo.mSubClassRate) {
                        blockInfoAssoc.mSubClassRate = blockInfo.mSubClassRate;
                        blockInfoAssoc.mSubTypeStr = SUBTYPE_ASSOCIATION;
                    }
                }
            }
        }
    }

    private void adjustClassRate(Map<Integer, AwareProcessInfo> allProcNeedSort, Map<Integer, AwareProcessBlockInfo> classShort, Map<Integer, AwareProcessBlockInfo> classNormal, Map<Integer, AwareProcessBlockInfo> allClass, ShortageProcessInfo shortageProc, Map<Integer, AwareProcessBlockInfo> allUids, CachedWhiteList cachedWhitelist, Set<String> clocks) {
        Map<Integer, AwareProcessBlockInfo> map = classNormal;
        if (allProcNeedSort == null || map == null) {
            ShortageProcessInfo shortageProcessInfo = shortageProc;
            return;
        }
        ArrayMap<Integer, AppBlockKeyBase> pidsAppBlock = new ArrayMap<>();
        ArrayMap<Integer, AppBlockKeyBase> uidAppBlock = new ArrayMap<>();
        Map<AppBlockKeyBase, AwareProcessBlockInfo> uids = convertToUidBlock(map, pidsAppBlock, uidAppBlock);
        if (uids != null) {
            ArrayMap arrayMap = new ArrayMap();
            for (Map.Entry<Integer, AwareProcessBlockInfo> m : classShort.entrySet()) {
                AwareProcessBlockInfo blockInfo = m.getValue();
                if (blockInfo.mClassRate == ClassRate.KEYSERVICES.ordinal()) {
                    Map<AppBlockKeyBase, AwareProcessBlockInfo> assocNormalUids = new ArrayMap<>();
                    AwareProcessBlockInfo awareProcessBlockInfo = blockInfo;
                    adjustClassByStrongAssoc(blockInfo, allProcNeedSort, uids, pidsAppBlock, uidAppBlock, assocNormalUids);
                    for (Map.Entry<AppBlockKeyBase, AwareProcessBlockInfo> assocBlock : assocNormalUids.entrySet()) {
                        AppBlockKeyBase blockKeyValue = assocBlock.getKey();
                        AwareProcessBlockInfo blockInfoAssoc = assocBlock.getValue();
                        if (AwareAppAssociate.isDealAsPkgUid(blockKeyValue.mUid)) {
                            pidsAppBlock.remove(Integer.valueOf(blockKeyValue.mPid));
                            uids.remove(blockKeyValue);
                        } else {
                            uidAppBlock.remove(Integer.valueOf(blockKeyValue.mUid));
                            uids.remove(blockKeyValue);
                        }
                        arrayMap.put(Integer.valueOf(blockKeyValue.mPid), blockInfoAssoc);
                    }
                }
            }
            classShort.putAll(arrayMap);
            Map<Integer, AwareProcessBlockInfo> classNormalBlock = new ArrayMap<>();
            Iterator<Map.Entry<AppBlockKeyBase, AwareProcessBlockInfo>> it = uids.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<AppBlockKeyBase, AwareProcessBlockInfo> m2 = it.next();
                AwareProcessBlockInfo blockInfo2 = m2.getValue();
                Map.Entry<AppBlockKeyBase, AwareProcessBlockInfo> entry = m2;
                Iterator<Map.Entry<AppBlockKeyBase, AwareProcessBlockInfo>> it2 = it;
                adjustClassByStrongAssoc(blockInfo2, allProcNeedSort, uids, pidsAppBlock, uidAppBlock, null);
                boolean addToAll = false;
                int subClass = AllowStopSubClassRate.UNKNOWN.ordinal();
                AwareProcessBlockInfo blockInfo3 = blockInfo2;
                for (AwareProcessInfo procInfo : blockInfo3.mProcessList) {
                    if (!addToAll) {
                        blockInfo3.mImportance = procInfo.mImportance;
                        blockInfo3.mMinAdj = procInfo.mProcInfo.mCurAdj;
                        blockInfo3.mSubClassRate = procInfo.mSubClassRate;
                        classNormalBlock.put(Integer.valueOf(procInfo.mProcInfo.mPid), blockInfo3);
                        if (shortageProc.isFgServicesUid(blockInfo3.mUid)) {
                            subClass = AllowStopSubClassRate.FG_SERVICES.ordinal();
                        }
                        if (blockInfo3.mSubClassRate > subClass) {
                            blockInfo3.mSubClassRate = subClass;
                        }
                        addToAll = true;
                    } else {
                        ShortageProcessInfo shortageProcessInfo2 = shortageProc;
                        if (blockInfo3.mSubClassRate > procInfo.mSubClassRate) {
                            blockInfo3.mSubClassRate = procInfo.mSubClassRate;
                        }
                        if (blockInfo3.mImportance > procInfo.mImportance) {
                            blockInfo3.mImportance = procInfo.mImportance;
                        }
                        if (blockInfo3.mMinAdj > procInfo.mProcInfo.mCurAdj) {
                            blockInfo3.mMinAdj = procInfo.mProcInfo.mCurAdj;
                        }
                    }
                    Map<Integer, AwareProcessBlockInfo> map2 = classNormal;
                }
                ShortageProcessInfo shortageProcessInfo3 = shortageProc;
                it = it2;
                Map<Integer, AwareProcessBlockInfo> map3 = classNormal;
            }
            ArrayMap arrayMap2 = arrayMap;
            Map<AppBlockKeyBase, AwareProcessBlockInfo> map4 = uids;
            addClassToAllClass(allClass, classNormalBlock, shortageProc, allUids, cachedWhitelist, clocks, true);
        }
    }

    private boolean inSameUids(Map<Integer, AwareProcessBlockInfo> allUids, List<AwareProcessInfo> lists) {
        if (lists == null || lists.isEmpty()) {
            return false;
        }
        AwareProcessBlockInfo info = allUids.get(Integer.valueOf(lists.get(0).mProcInfo.mUid));
        if (info == null || info.mProcessList == null) {
            return false;
        }
        return info.mProcessList.equals(lists);
    }

    private MemSortGroup getAppMemSortGroup(int subType) {
        ArrayMap<Integer, AwareProcessBlockInfo> pidsClass = getAppMemSortClassGroup(subType);
        if (pidsClass == null) {
            return null;
        }
        List<AwareProcessBlockInfo> procForbidStopList = new ArrayList<>();
        List<AwareProcessBlockInfo> procShortageStopList = new ArrayList<>();
        List<AwareProcessBlockInfo> procAllowStopList = new ArrayList<>();
        for (Map.Entry<Integer, AwareProcessBlockInfo> m : pidsClass.entrySet()) {
            AwareProcessBlockInfo awareProcInfo = m.getValue();
            awareProcInfo.mUpdateTime = SystemClock.elapsedRealtime();
            int groupId = 0;
            if (awareProcInfo.mClassRate <= ClassRate.FOREGROUND.ordinal()) {
                procForbidStopList.add(awareProcInfo);
            } else if (awareProcInfo.mClassRate < ClassRate.NORMAL.ordinal()) {
                procShortageStopList.add(awareProcInfo);
                groupId = 1;
            } else {
                procAllowStopList.add(awareProcInfo);
                groupId = 2;
            }
            awareProcInfo.setMemGroup(groupId);
        }
        Collections.sort(procShortageStopList);
        Collections.sort(procAllowStopList);
        return new MemSortGroup(procForbidStopList, procShortageStopList, procAllowStopList);
    }

    private MemSortGroup getAppMemCompactSortGroup(int subType) {
        int i;
        List<AwareProcessBlockInfo> procForbidList = new ArrayList<>();
        List<AwareProcessBlockInfo> procCompactibleList = new ArrayList<>();
        List<AwareProcessBlockInfo> procFrozenList = new ArrayList<>();
        ArrayList<AwareProcessInfo> allProcNeedSortList = AppStatusUtils.getInstance().getAllProcNeedSort();
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
        AppStatusUtils appStatus = AppStatusUtils.getInstance();
        Iterator<AwareProcessBlockInfo> it = resultInfo.iterator();
        while (true) {
            i = 0;
            if (!it.hasNext()) {
                break;
            }
            AwareProcessBlockInfo awareProcBlkInfo = it.next();
            if (isBlockInfoValid(awareProcBlkInfo)) {
                AwareProcessInfo item = awareProcBlkInfo.mProcessList.get(0);
                if (awareProcBlkInfo.mCleanType == ProcessCleaner.CleanType.NONE) {
                    procForbidList.add(awareProcBlkInfo);
                } else if (appStatus.checkAppStatus(AppStatusUtils.Status.FROZEN, item)) {
                    procFrozenList.add(awareProcBlkInfo);
                } else {
                    procCompactibleList.add(awareProcBlkInfo);
                    allProcNeedSort.put(Integer.valueOf(item.mPid), item);
                }
            }
        }
        Map<Integer, AwareProcessBlockInfo> allUids = groupByUid(allProcNeedSort);
        if (filteredApps == null) {
            updateFilteredApps();
        }
        for (AwareProcessBlockInfo awareBlkProc : procCompactibleList) {
            AwareProcessInfo printItem = awareBlkProc.mProcessList.get(i);
            if (!filteredApps.contains(awareBlkProc.mPackageName)) {
                int blkByUidImportance = allUids.get(Integer.valueOf(awareBlkProc.mUid)).mImportance;
                if (blkByUidImportance >= printItem.mImportance || AwareAppAssociate.isDealAsPkgUid(awareBlkProc.mUid)) {
                    awareBlkProc.mImportance = printItem.mImportance;
                } else {
                    awareBlkProc.mImportance = blkByUidImportance;
                }
                i = 0;
            } else {
                i = 0;
                awareBlkProc.mImportance = 0;
            }
        }
        Collections.sort(procCompactibleList, BLOCK_BY_IMPORTANCE);
        return new MemSortGroup(procForbidList, procCompactibleList, procFrozenList);
    }

    private boolean isBlockInfoValid(AwareProcessBlockInfo awareProcBlkInfo) {
        if (awareProcBlkInfo == null || awareProcBlkInfo.mProcessList == null || awareProcBlkInfo.mProcessList.isEmpty() || awareProcBlkInfo.mProcessList.get(0) == null) {
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
        for (ClassRate rate : ClassRate.values()) {
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ClassRate.UNKNOWN.description();
    }

    public boolean isGroupBeHigher(int pid, int uid, String processName, ArrayList<String> arrayList, int groupId) {
        if (!mEnabled || !this.mAssocEnable) {
            return false;
        }
        AwareAppAssociate awareAssoc = AwareAppAssociate.getInstance();
        if (awareAssoc == null) {
            return false;
        }
        Set<Integer> forePid = new ArraySet<>();
        awareAssoc.getForeGroundApp(forePid);
        if (forePid.contains(Integer.valueOf(pid))) {
            return true;
        }
        AwareProcessBaseInfo info = this.mHwAMS != null ? this.mHwAMS.getProcessBaseInfo(pid) : null;
        if (info == null) {
            return false;
        }
        if (info.mCurAdj < 200) {
            return true;
        }
        if (groupId == 2) {
            if (info.mCurAdj == 300 || info.mCurAdj == 400) {
                return true;
            }
            if (info.mCurAdj != 200 || isFgServicesImportantByAdjtype(info.mAdjType)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean checkAppMngEnable() {
        return mEnabled;
    }

    private boolean needReplaceAllowStopList(int resourceType) {
        return 2 == resourceType;
    }

    public AwareAppMngSortPolicy getAppMngSortPolicy(int resourceType, int subType, int groupId) {
        MemSortGroup sortGroup;
        if (!mEnabled) {
            return null;
        }
        long startTime = 0;
        if (DEBUG) {
            startTime = System.currentTimeMillis();
        }
        ArrayMap<Integer, List<AwareProcessBlockInfo>> appGroup = new ArrayMap<>();
        if (needReplaceAllowStopList(resourceType)) {
            appGroup = getAppMngSortGroupForMemCleanChina(groupId, subType);
        } else {
            if (1 == resourceType) {
                sortGroup = getAppMemCompactSortGroup(subType);
            } else {
                sortGroup = getAppMemSortGroup(subType);
            }
            if (sortGroup == null) {
                return null;
            }
            if (groupId == 0) {
                appGroup.put(Integer.valueOf(groupId), sortGroup.mProcForbidStopList);
            } else if (groupId == 1) {
                appGroup.put(Integer.valueOf(groupId), sortGroup.mProcShortageStopList);
            } else if (groupId == 2) {
                appGroup.put(2, sortGroup.mProcAllowStopList);
            } else if (groupId == 3) {
                appGroup.put(0, sortGroup.mProcForbidStopList);
                appGroup.put(1, sortGroup.mProcShortageStopList);
                appGroup.put(2, sortGroup.mProcAllowStopList);
            }
        }
        AwareAppMngSortPolicy sortPolicy = new AwareAppMngSortPolicy(this.mContext, appGroup);
        if (DEBUG) {
            AwareLog.i(TAG, "        getAppMngSortPolicy eclipse time     :" + (System.currentTimeMillis() - startTime));
            long availableRam = MemoryReader.getInstance().getMemAvailable();
            AwareLog.i(TAG, "MemAvailable(KB): " + availableRam);
            dumpPolicy(sortPolicy, null, false);
        }
        if (Log.HWINFO && 2 == resourceType) {
            printBetaLog(sortPolicy, subType);
        }
        return sortPolicy;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0089, code lost:
        return;
     */
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
                    msg.obj = new BetaLog(sortPolicy);
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
        int i = memLevel;
        ArrayMap<Integer, List<AwareProcessBlockInfo>> appGroup = new ArrayMap<>();
        List<AwareProcessInfo> allAwareProcNeedProcess = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (allAwareProcNeedProcess == null || allAwareProcNeedProcess.isEmpty()) {
            AwareLog.e(TAG, "getAllProcNeedSort failed!");
            return null;
        }
        ArraySet<String> listFilter = new ArraySet<>();
        boolean noList = false;
        if (i == 2) {
            if (this.mListFilterLevel2 == null) {
                updateLevel2ListFilter();
            }
            if (this.mListFilterLevel2 != null) {
                listFilter.addAll(this.mListFilterLevel2);
            }
        } else if (i == 4 || i == 5) {
            noList = true;
        }
        List<AwareProcessBlockInfo> rawInfo = DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, i, AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.MEMORY, listFilter, noList);
        List<AwareProcessBlockInfo> needClean = CleanSource.mergeBlockForMemory(rawInfo, DecisionMaker.getInstance().getProcessList(AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.MEMORY));
        if (i == 4 || i == 5) {
            DecisionMaker.getInstance().checkListForMemLowEnd(needClean, rawInfo);
            setWeightForLowEnd(needClean);
        }
        AppCleanupDumpRadar.getInstance().reportMemoryData(rawInfo, -1);
        if (needClean != null && !needClean.isEmpty()) {
            if (i == 3 || i == 4 || i == 5) {
                Collections.sort(needClean, BLOCK_BY_WEIGHT);
            } else {
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
        if (groupId != 0) {
            switch (groupId) {
                case 2:
                    appGroup.put(2, needClean);
                    break;
                case 3:
                    appGroup.put(0, rawInfo);
                    appGroup.put(2, needClean);
                    break;
                default:
                    return null;
            }
        } else {
            appGroup.put(0, rawInfo);
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
            String pssPackageName = pss.mPackageName;
            if (AppMngConstant.CleanReason.LIST.getCode().equals(pss.mReason) && !aiRecg.isTopImAppBase(pssPackageName) && !aiRecg.getActTopIMCN().equals(pssPackageName)) {
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
        return (b1.mPackageName == null || b2.mPackageName == null || !b1.mPackageName.equals(b2.mPackageName)) ? false : true;
    }

    public AwareAppMngSortPolicy getAppMngSortPolicyForMemRepair(int sceneType) {
        return getAppMngSortPolicyForMemRepair(sceneType, AppMngConstant.AppCleanSource.MEMORY_REPAIR);
    }

    private AwareAppMngSortPolicy getAppMngSortPolicyForMemRepair(int sceneType, AppMngConstant.AppCleanSource flag) {
        if (!mEnabled) {
            return null;
        }
        ArrayMap<Integer, List<AwareProcessBlockInfo>> appGroup = new ArrayMap<>();
        List<AwareProcessInfo> allAwareProcNeedProcess = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (allAwareProcNeedProcess == null || allAwareProcNeedProcess.isEmpty()) {
            AwareLog.e(TAG, "getAllProcNeedSort failed!");
            return null;
        }
        List<AwareProcessBlockInfo> rawInfo = DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, sceneType, AppMngConstant.AppMngFeature.APP_CLEAN, flag);
        if (rawInfo == null || rawInfo.isEmpty()) {
            AwareLog.e(TAG, "decideAll get null for memRepair!");
            return null;
        }
        List<AwareProcessBlockInfo> resultInfo = CleanSource.mergeBlock(rawInfo);
        if (resultInfo == null) {
            AwareLog.e(TAG, "mergeBlock get null for memRepair!");
            return null;
        }
        List<AwareProcessBlockInfo> procForbidList = new ArrayList<>();
        List<AwareProcessBlockInfo> procAllowStopList = new ArrayList<>();
        for (AwareProcessBlockInfo blockInfo : resultInfo) {
            if (!(blockInfo == null || blockInfo.mProcessList == null || blockInfo.mProcessList.isEmpty())) {
                if (blockInfo.mCleanType == ProcessCleaner.CleanType.NONE) {
                    procForbidList.add(blockInfo);
                } else {
                    procAllowStopList.add(blockInfo);
                }
            }
        }
        appGroup.put(0, procForbidList);
        appGroup.put(2, procAllowStopList);
        return new AwareAppMngSortPolicy(this.mContext, appGroup);
    }

    public List<AwareProcessInfo> getAppMngSortPolicyForSystemTrim() {
        if (!mEnabled) {
            return null;
        }
        return AppStatusUtils.getInstance().getAllProcNeedSort();
    }

    public boolean checkNonSystemUser(AwareProcessInfo awareProcInfo) {
        boolean z = false;
        if (awareProcInfo == null || awareProcInfo.mProcInfo == null) {
            return false;
        }
        int curUserUid = AwareAppAssociate.getInstance().getCurUserId();
        if (curUserUid == 0 && !isCurUserProc(awareProcInfo.mProcInfo.mUid, curUserUid)) {
            z = true;
        }
        return z;
    }

    private boolean isNonSystemUser(int checkUid) {
        return AwareAppAssociate.getInstance().getCurUserId() == 0 && !isCurUserProc(checkUid, AwareAppAssociate.getInstance().getCurUserId());
    }

    private void setPropImportance(AwareProcessBlockInfo block, Map<String, Integer> allTopList, List<String> pkgTopN) {
        if (block != null) {
            if (isNonSystemUser(block.mUid)) {
                block.mImportance = 10000;
                return;
            }
            Integer importance = null;
            if (allTopList != null) {
                importance = allTopList.get(block.mPackageName);
            }
            if (pkgTopN != null) {
                int topIdx = pkgTopN.indexOf(block.mPackageName);
                if (topIdx >= 0 && topIdx < 8) {
                    importance = Integer.valueOf(-100 * (8 - topIdx));
                }
            }
            block.mImportance = importance != null ? importance.intValue() : 0;
        }
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
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
        for (ForbidSubClassRate rate : ForbidSubClassRate.values()) {
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ForbidSubClassRate.NONE.description();
    }

    private static String getShortageSubClassRateStr(int classRate) {
        for (ShortageSubClassRate rate : ShortageSubClassRate.values()) {
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ShortageSubClassRate.NONE.description();
    }

    private static String getAllowSubClassRateStr(int classRate) {
        for (AllowStopSubClassRate rate : AllowStopSubClassRate.values()) {
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

    private int getCurHomeProcessPid() {
        return AwareAppAssociate.getInstance().getCurHomeProcessPid();
    }

    public boolean isProcessBlockPidChanged(AwareProcessBlockInfo procGroup) {
        if (!mEnabled || procGroup == null) {
            return false;
        }
        ArrayList<ProcessInfo> procs = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procs.isEmpty()) {
            return false;
        }
        int uid = procGroup.mUid;
        int size = procs.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo procInfo = procs.get(i);
            if (procInfo != null && procInfo.mUid == uid && procInfo.mCreatedTime - procGroup.mUpdateTime > 0) {
                return true;
            }
        }
        return false;
    }

    private void dumpBlockList(PrintWriter pw, List<AwareProcessBlockInfo> list, boolean toPrint) {
        if (list != null && (pw != null || !toPrint)) {
            for (AwareProcessBlockInfo pinfo : list) {
                if (pinfo != null) {
                    boolean allow = pinfo.mResCleanAllow;
                    print(pw, "AppProc:uid:" + pinfo.mUid + ",import:" + pinfo.mImportance + ",classRates:" + pinfo.mClassRate + ",classStr:" + getClassRateStr(pinfo.mClassRate) + ",subStr:" + getClassStr(pinfo.mClassRate, pinfo.mSubClassRate) + ",subTypeStr:" + pinfo.mSubTypeStr + ",appType:" + pinfo.mAppType + ",policy:" + pinfo.mDetailedReason.get(TAG_POLICY) + ",reason:" + pinfo.mReason + ",weight:" + pinfo.mWeight);
                    if (pinfo.mProcessList != null) {
                        for (AwareProcessInfo info : pinfo.mProcessList) {
                            print(pw, "     name:" + info.mProcInfo.mProcessName + ",pid:" + info.mProcInfo.mPid + ",uid:" + info.mProcInfo.mUid + ",group:" + info.mMemGroup + ",import:" + info.mImportance + ",classRate:" + info.mClassRate + ",adj:" + info.mProcInfo.mCurAdj + "," + info.mProcInfo.mAdjType + ",classStr:" + getClassRateStr(info.mClassRate) + ",subStr:" + getClassStr(info.mClassRate, info.mSubClassRate) + ",mResCleanAllow:" + allow + ",mRestartFlag:" + info.getRestartFlag() + ",ui:" + info.mHasShownUi);
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

    private void dumpBlock(PrintWriter pw, int memLevel, boolean isMemClean) {
        AwareAppMngSortPolicy policy;
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareAppMngSort disabled!");
                return;
            }
            if (isMemClean) {
                policy = getAppMngSortPolicy(2, memLevel, 3);
            } else {
                policy = getAppMngSortPolicy(0, memLevel, 3);
            }
            if (policy == null) {
                pw.println("getAppMngSortPolicy return null!");
            } else {
                dumpPolicy(policy, pw, true);
            }
        }
    }

    private void dumpGroupBlock(PrintWriter pw, int group) {
        if (pw != null) {
            if (!mEnabled) {
                pw.println("AwareAppMngSort disabled!");
                return;
            }
            AwareAppMngSortPolicy policy = getAppMngSortPolicy(0, 0, group);
            if (policy == null) {
                pw.println("getAppMngSortPolicy return null!");
            } else {
                dumpPolicy(policy, pw, true);
            }
        }
    }

    private void dumpPolicy(AwareAppMngSortPolicy policy, PrintWriter pw, boolean toPrint) {
        if (policy != null && (pw != null || !toPrint)) {
            print(pw, "------------------start dump Group  forbidstop ------------------");
            dumpBlockList(pw, policy.getForbidStopProcBlockList(), toPrint);
            print(pw, "------------------start dump Group  shortagestop ------------------");
            dumpBlockList(pw, policy.getShortageStopProcBlockList(), toPrint);
            print(pw, "------------------start dump Group  allowstop ------------------");
            dumpBlockList(pw, policy.getAllowStopProcBlockList(), toPrint);
        }
    }

    private void print(PrintWriter pw, String info) {
        if (pw != null) {
            pw.println(info);
        } else if (DEBUG) {
            AwareLog.i(TAG, info);
        }
    }

    public void dump(PrintWriter pw, String type) {
        if (pw != null && type != null) {
            pw.println("  App Group Manager Information dump :");
            if (!dumpForResourceType(pw, type)) {
                if (type.equals("memForbid")) {
                    dumpGroupBlock(pw, 0);
                } else if (type.equals("memShortage")) {
                    dumpGroupBlock(pw, 1);
                } else if (type.equals("memAllow")) {
                    dumpGroupBlock(pw, 2);
                } else if (type.equals("enable")) {
                    enable();
                } else if (type.equals("disable")) {
                    disable();
                } else if (type.equals("checkEnabled")) {
                    boolean status = checkAppMngEnable();
                    pw.println("AwareAppMngSort is " + status);
                } else if (!type.equals("procinfo")) {
                    pw.println("  dump parameter error!");
                } else if (!mEnabled) {
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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private boolean dumpForResourceType(PrintWriter pw, String type) {
        char c;
        switch (type.hashCode()) {
            case -1831967232:
                if (type.equals("smartClean")) {
                    c = 7;
                    break;
                }
            case -1389206271:
                if (type.equals("bigApp")) {
                    c = 4;
                    break;
                }
            case -677872268:
                if (type.equals("memClean")) {
                    c = 2;
                    break;
                }
            case -232627586:
                if (type.equals("memRepairVss2")) {
                    c = 13;
                    break;
                }
            case 107989:
                if (type.equals("mem")) {
                    c = 0;
                    break;
                }
            case 3347709:
                if (type.equals("mem2")) {
                    c = 1;
                    break;
                }
            case 443555725:
                if (type.equals("memCleanAll")) {
                    c = 5;
                    break;
                }
            case 460796222:
                if (type.equals("memClean2")) {
                    c = 8;
                    break;
                }
            case 460796223:
                if (type.equals("memClean3")) {
                    c = 3;
                    break;
                }
            case 865325637:
                if (type.equals("memCleanAll2")) {
                    c = 6;
                    break;
                }
            case 884096450:
                if (type.equals(MemoryConstant.MEM_POLICY_REPAIR)) {
                    c = 10;
                    break;
                }
            case 1377969204:
                if (type.equals("memRepairVss")) {
                    c = 12;
                    break;
                }
            case 1493492622:
                if (type.equals("memCompact")) {
                    c = 9;
                    break;
                }
            case 1637186224:
                if (type.equals("memRepair2")) {
                    c = 11;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                dumpBlock(pw, 0, false);
                break;
            case 1:
                dumpBlock(pw, 1, false);
                break;
            case 2:
                dumpBlock(pw, 0, true);
                break;
            case 3:
                dumpBlock(pw, 2, true);
                break;
            case 4:
                dumpBlock(pw, 3, true);
                break;
            case 5:
                dumpBlock(pw, 4, true);
                break;
            case 6:
                dumpBlock(pw, 5, true);
                break;
            case 7:
                dumpSmartClean(pw);
                break;
            case 8:
                dumpBlock(pw, 1, true);
                break;
            case 9:
                dumpMemCompactGroup(pw);
                break;
            case 10:
                dumpMemRepair(pw, 0);
                break;
            case 11:
                dumpMemRepair(pw, 1);
                break;
            case 12:
                dumpMemRepairVss(pw, 0);
                break;
            case 13:
                dumpMemRepairVss(pw, 1);
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
        AwareAppMngSortPolicy policy = getAppMngSortPolicyForMemRepair(sceneType);
        if (policy != null) {
            AwareAppMngSortPolicy vssPolicy = getAppMngSortPolicyForMemRepairVss(sceneType, policy);
            if (vssPolicy != null) {
                print(pw, "------------------start dump Group forbid group ------------------");
                dumpBlockList(pw, vssPolicy.getForbidStopProcBlockList(), true);
                print(pw, "------------------start dump Group allowstop group ------------------");
                dumpBlockList(pw, vssPolicy.getAllowStopProcBlockList(), true);
            }
        }
    }

    private void dumpShortageSubClassRate(PrintWriter pw) {
        if (pw != null) {
            for (ShortageSubClassRate rate : ShortageSubClassRate.values()) {
                String classRate = rate.description();
                pw.println("    sub" + rate.ordinal() + ": value=" + allRate[r2].ordinal() + "," + classRate);
            }
        }
    }

    private void dumpForbidSubClassRate(PrintWriter pw) {
        if (pw != null) {
            for (ForbidSubClassRate rate : ForbidSubClassRate.values()) {
                String classRate = rate.description();
                pw.println("    sub" + rate.ordinal() + ": value=" + allRate[r2].ordinal() + "," + classRate);
            }
        }
    }

    private void dumpAllowStopSubClassRate(PrintWriter pw) {
        if (pw != null) {
            for (AllowStopSubClassRate rate : AllowStopSubClassRate.values()) {
                String classRate = rate.description();
                pw.println("    sub" + rate.ordinal() + ": value=" + allRate[r2].ordinal() + "," + classRate);
            }
        }
    }

    public void dumpClassInfo(PrintWriter pw) {
        if (!mEnabled) {
            pw.println("AwareAppMngSort disabled!");
        } else if (pw != null) {
            for (ClassRate rate : ClassRate.values()) {
                String classRate = rate.description();
                pw.println("Class" + rate.ordinal() + ": value=" + rate.ordinal() + "," + classRate);
                String subClass = ShortageSubClassRate.NONE.description();
                if (rate == ClassRate.FOREGROUND) {
                    dumpForbidSubClassRate(pw);
                } else if (rate == ClassRate.KEYSERVICES) {
                    dumpShortageSubClassRate(pw);
                } else if (rate == ClassRate.NORMAL) {
                    dumpAllowStopSubClassRate(pw);
                } else {
                    pw.println("    sub" + ShortageSubClassRate.NONE.ordinal() + ": value=" + ShortageSubClassRate.NONE.ordinal() + "," + subClass);
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

    public void updateCloudData() {
        updateFilteredApps();
        updateLevel2ListFilter();
    }

    private void setWeightForLowEnd(List<AwareProcessBlockInfo> needClean) {
        if (needClean != null && !needClean.isEmpty()) {
            List<AwareProcessInfo> allAwareProcNeedProcess = AppStatusUtils.getInstance().getAllProcNeedSort();
            if (allAwareProcNeedProcess == null || allAwareProcNeedProcess.isEmpty()) {
                AwareLog.e(TAG, "getAllProcNeedSort failed!");
                return;
            }
            List<AwareProcessBlockInfo> cleanWeight = CleanSource.mergeBlockForMemory(DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, 14, AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.MEMORY, null, true), null);
            ArrayMap<Integer, ArrayMap<String, AwareProcessBlockInfo>> weightWithIndex = new ArrayMap<>();
            int size = cleanWeight.size();
            for (int i = 0; i < size; i++) {
                AwareProcessBlockInfo info = cleanWeight.get(i);
                int userId = UserHandle.getUserId(info.mUid);
                String pkgName = info.mPackageName;
                ArrayMap<String, AwareProcessBlockInfo> weightMap = weightWithIndex.get(Integer.valueOf(userId));
                if (weightMap == null) {
                    weightMap = new ArrayMap<>();
                }
                weightMap.put(pkgName, info);
                weightWithIndex.put(Integer.valueOf(userId), weightMap);
            }
            int size2 = needClean.size();
            for (int i2 = 0; i2 < size2; i2++) {
                AwareProcessBlockInfo info2 = needClean.get(i2);
                int userId2 = UserHandle.getUserId(info2.mUid);
                String pkgName2 = info2.mPackageName;
                ArrayMap<String, AwareProcessBlockInfo> weightMap2 = weightWithIndex.get(Integer.valueOf(userId2));
                if (weightMap2 != null) {
                    AwareProcessBlockInfo weight = weightMap2.get(pkgName2);
                    if (!(weight == null || weight.mDetailedReason == null || info2.mDetailedReason == null || weight.mWeight <= info2.mWeight)) {
                        info2.mWeight = weight.mWeight;
                        info2.mReason = weight.mReason;
                    }
                }
            }
        }
    }
}
