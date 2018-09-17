package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.server.am.HwActivityManagerService;
import com.android.server.input.HwCircleAnimation;
import com.android.server.location.HwGnssLogHandlerMsgID;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo.XmlConfig;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppLruBase;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList.PackageConfigItem;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList.ProcessConfigItem;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsView;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AwareAppMngSort {
    public static final String ACTIVITY_RECENT_TASK = "com.android.systemui/.recents.RecentsActivity";
    public static final int ACTIVITY_TASK_IMPORT_CNT = 1;
    public static final String ADJTYPE_SERVICE = "service";
    public static final int APPMNG_MEM_ALLOWSTOP_GROUP = 2;
    public static final int APPMNG_MEM_ALL_GROUP = 3;
    public static final int APPMNG_MEM_FORBIDSTOP_GROUP = 0;
    public static final int APPMNG_MEM_SHORTAGESTOP_GROUP = 1;
    public static final int APPSORT_FORMEM = 0;
    private static final int BETA_LOG_PRINT_INTERVEL = 60000;
    private static final int CLASSRATE_KEY_OFFSET = 8;
    private static boolean DEBUG = false;
    public static final String EXEC_SERVICES = "exec-service";
    public static final String FG_SERVICE = "fg-service";
    public static final long FOREVER_DECAYTIME = -1;
    public static final int HABITMAX_IMPORT = 10000;
    private static final int INVALID_VALUE = -1;
    public static final int MEM_LEVEL0 = 0;
    public static final int MEM_LEVEL1 = 1;
    private static final int MSG_PRINT_BETA_LOG = 1;
    public static final long PREVIOUS_APP_DIRCACTIVITY_DECAYTIME = 600000;
    private static final int SEC_PER_MIN = 60;
    private static final String SUBTYPE_ASSOCIATION = "assoc";
    private static final String TAG = "AwareAppMngSort";
    private static boolean mEnabled;
    private static AwareAppMngSort sInstance;
    private boolean mAssocEnable;
    private final Context mContext;
    private Handler mHandler;
    private HwActivityManagerService mHwAMS;
    private long mLastBetaLogOutTime;

    enum AllowStopSubClassRate {
        ;
        
        String mDescription;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.iaware.appmng.AwareAppMngSort.AllowStopSubClassRate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.iaware.appmng.AwareAppMngSort.AllowStopSubClassRate.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.iaware.appmng.AwareAppMngSort.AllowStopSubClassRate.<clinit>():void");
        }

        private AllowStopSubClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    static class AppBlockKeyBase {
        public int mPid;
        public int mUid;

        public AppBlockKeyBase(int pid, int uid) {
            this.mPid = AwareAppMngSort.MEM_LEVEL0;
            this.mUid = AwareAppMngSort.MEM_LEVEL0;
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
        private static final int PROCESS_INFO_CNT = 5;
        private List<Integer> mData;

        BetaLog(AwareAppMngSortPolicy policy) {
            this.mData = new ArrayList();
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
                        for (AwareProcessInfo info : pinfo.mProcessList) {
                            this.mData.add(Integer.valueOf(info.mProcInfo.mPid));
                            this.mData.add(Integer.valueOf(info.mProcInfo.mUid));
                            this.mData.add(Integer.valueOf(info.mClassRate));
                            this.mData.add(Integer.valueOf(info.mSubClassRate));
                            this.mData.add(Integer.valueOf(info.mProcInfo.mCurAdj));
                        }
                    }
                }
            }
        }

        public void print() {
            int size = this.mData.size();
            if (size != 0 && size % PROCESS_INFO_CNT == 0) {
                StringBuilder outStr = new StringBuilder();
                int cnt = AwareAppMngSort.MEM_LEVEL0;
                for (Integer cur : this.mData) {
                    outStr.append(cur);
                    cnt += AwareAppMngSort.MSG_PRINT_BETA_LOG;
                    if (cnt % PROCESS_INFO_CNT != 0) {
                        outStr.append(FLAG_ITEM_INNER_SPLIT);
                    } else if (cnt % 50 == 0) {
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
            switch (msg.what) {
                case AwareAppMngSort.MSG_PRINT_BETA_LOG /*1*/:
                    BetaLog betaLog = msg.obj;
                    if (betaLog != null) {
                        betaLog.print();
                    }
                default:
            }
        }
    }

    static class CachedWhiteList {
        private final ArraySet<String> mAllHabitCacheList;
        final Set<String> mAllProtectApp;
        final Set<String> mAllUnProtectApp;
        private Map<String, PackageConfigItem> mAwareProtectCacheMap;
        final ArraySet<String> mBadAppList;
        final ArraySet<String> mBgNonDecayPkg;
        private final ArraySet<String> mKeyHabitCacheList;
        private boolean mLowEnd;
        final ArraySet<String> mRestartAppList;

        public CachedWhiteList() {
            this.mKeyHabitCacheList = new ArraySet();
            this.mAllHabitCacheList = new ArraySet();
            this.mBgNonDecayPkg = new ArraySet();
            this.mAwareProtectCacheMap = new ArrayMap();
            this.mLowEnd = false;
            this.mRestartAppList = new ArraySet();
            this.mBadAppList = new ArraySet();
            this.mAllProtectApp = new ArraySet();
            this.mAllUnProtectApp = new ArraySet();
        }

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
                    if (AppMngConfig.getAbroadFlag()) {
                        Set<String> unprotectApp = habit.getAllUnProtectApps();
                        if (unprotectApp != null) {
                            this.mAllUnProtectApp.addAll(unprotectApp);
                        }
                    }
                    Set<String> protectApp = habit.getAllProtectApps();
                    if (protectApp != null) {
                        this.mAllProtectApp.addAll(protectApp);
                    }
                    if (isLowEnd()) {
                        Set<String> bgNonDcyApp = habit.getBackgroundApps(AppMngConfig.getBgDecay() * 60);
                        if (bgNonDcyApp != null) {
                            this.mBgNonDecayPkg.addAll(bgNonDcyApp);
                        }
                    }
                }
                this.mAwareProtectCacheMap = whiteListInstance.getAwareProtectMap();
            }
        }

        private boolean isLowEnd() {
            return this.mLowEnd;
        }

        public boolean isInKeyHabitList(ArrayList<String> packageNames) {
            if (packageNames == null || packageNames.isEmpty()) {
                return false;
            }
            for (String pkgName : packageNames) {
                if (this.mKeyHabitCacheList.contains(pkgName)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isInAllHabitList(ArrayList<String> packageNames) {
            if (packageNames == null || packageNames.isEmpty()) {
                return false;
            }
            for (String pkgName : packageNames) {
                if (this.mAllHabitCacheList.contains(pkgName)) {
                    return true;
                }
            }
            return false;
        }

        private ProcessConfigItem getAwareWhiteListItem(ArrayList<String> packageNames, String processName) {
            if (packageNames == null || packageNames.isEmpty() || this.mAwareProtectCacheMap == null) {
                return null;
            }
            for (String pkgName : packageNames) {
                if (this.mAwareProtectCacheMap.containsKey(pkgName)) {
                    PackageConfigItem pkgItem = (PackageConfigItem) this.mAwareProtectCacheMap.get(pkgName);
                    if (pkgItem == null) {
                        return null;
                    }
                    if (pkgItem.isEmpty()) {
                        return pkgItem.copy();
                    }
                    ProcessConfigItem procItem = pkgItem.getItem(processName);
                    if (procItem == null) {
                        return null;
                    }
                    return procItem.copy();
                }
            }
            return null;
        }

        private int getGroupId(ProcessConfigItem item) {
            if (item == null) {
                return AwareAppMngSort.APPMNG_MEM_ALLOWSTOP_GROUP;
            }
            int group = AwareAppMngSort.APPMNG_MEM_ALLOWSTOP_GROUP;
            switch (item.mGroupId) {
                case AwareAppMngSort.MSG_PRINT_BETA_LOG /*1*/:
                    group = AwareAppMngSort.MEM_LEVEL0;
                    break;
                case AwareAppMngSort.APPMNG_MEM_ALLOWSTOP_GROUP /*2*/:
                    group = AwareAppMngSort.MSG_PRINT_BETA_LOG;
                    break;
            }
            return group;
        }

        private void updateProcessInfoByConfig(AwareProcessInfo processInfo) {
            if (processInfo != null && processInfo.mProcInfo != null) {
                ProcessConfigItem item = getAwareWhiteListItem(processInfo.mProcInfo.mPackageName, processInfo.mProcInfo.mProcessName);
                if (item != null) {
                    processInfo.mXmlConfig = new XmlConfig(getGroupId(item), item.mFrequentlyUsed, item.mResCleanAllow, item.mRestartFlag);
                }
            }
        }
    }

    enum ClassRate {
        ;
        
        String mDescription;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.iaware.appmng.AwareAppMngSort.ClassRate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.iaware.appmng.AwareAppMngSort.ClassRate.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.iaware.appmng.AwareAppMngSort.ClassRate.<clinit>():void");
        }

        private ClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    enum ForbidSubClassRate {
        ;
        
        String mDescription;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.iaware.appmng.AwareAppMngSort.ForbidSubClassRate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.iaware.appmng.AwareAppMngSort.ForbidSubClassRate.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.iaware.appmng.AwareAppMngSort.ForbidSubClassRate.<clinit>():void");
        }

        private ForbidSubClassRate(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    private static class MemSortGroup {
        public List<AwareProcessBlockInfo> mProcAllowStopList;
        public List<AwareProcessBlockInfo> mProcForbidStopList;
        public List<AwareProcessBlockInfo> mProcShortageStopList;

        public MemSortGroup(List<AwareProcessBlockInfo> procForbidStopList, List<AwareProcessBlockInfo> procShortageStopList, List<AwareProcessBlockInfo> procAllowStopList) {
            this.mProcForbidStopList = null;
            this.mProcShortageStopList = null;
            this.mProcAllowStopList = null;
            this.mProcForbidStopList = procForbidStopList;
            this.mProcShortageStopList = procShortageStopList;
            this.mProcAllowStopList = procAllowStopList;
        }
    }

    private static class ProcessHabitCompare implements Comparator<AwareProcessInfo>, Serializable {
        private static final long serialVersionUID = 1;

        /* synthetic */ ProcessHabitCompare(ProcessHabitCompare processHabitCompare) {
            this();
        }

        private ProcessHabitCompare() {
        }

        public /* bridge */ /* synthetic */ int compare(Object arg0, Object arg1) {
            return compare((AwareProcessInfo) arg0, (AwareProcessInfo) arg1);
        }

        public int compare(AwareProcessInfo arg0, AwareProcessInfo arg1) {
            if (arg0 == null || arg1 == null) {
                return AwareAppMngSort.MEM_LEVEL0;
            }
            return arg0.mImportance - arg1.mImportance;
        }
    }

    static class ShortageProcessInfo {
        public Map<Integer, AwareProcessInfo> mAllProcNeedSort;
        final ArrayMap<Integer, AwareProcessInfo> mAudioIn;
        final ArrayMap<Integer, AwareProcessInfo> mAudioOut;
        private final ArraySet<Integer> mForeGroundServiceUid;
        private final ArrayMap<Integer, ArrayList<String>> mForeGroundUid;
        List<String> mHabitFreqN;
        Set<Integer> mHabitTopN;
        private int mHomeProcessPid;
        private final Set<Integer> mHomeStrong;
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
            return AwareAppMngSort.isPkgIncludeForTgt(procInfo.mPackageName, (ArrayList) this.mForeGroundUid.get(Integer.valueOf(procInfo.mUid)));
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

        private boolean isAudioSubClass(ProcessInfo procInfo, Map<Integer, AwareProcessInfo> audioInfo) {
            if (procInfo == null) {
                return false;
            }
            for (Entry<Integer, AwareProcessInfo> m : audioInfo.entrySet()) {
                AwareProcessInfo info = (AwareProcessInfo) m.getValue();
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
            this.mHabitTopN = getHabitAppTopN(allProcNeedSort, AppMngConfig.getTopN());
            this.mHabitFreqN = getHabitAppFreq();
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
            boolean z = true;
            this.mAudioIn = new ArrayMap();
            this.mAudioOut = new ArrayMap();
            this.mForeGroundUid = new ArrayMap();
            this.mForeGroundServiceUid = new ArraySet();
            this.mHomeStrong = new ArraySet();
            this.mRecentTaskShow = false;
            this.mAllProcNeedSort = null;
            this.mPrevAmsBase = null;
            this.mPrevAwareBase = null;
            this.mRecentTaskAppBase = null;
            this.mNonCurUserProc = new ArrayMap();
            this.mVisibleWinPkg = new ArraySet();
            this.mWidgetPkg = new ArraySet();
            this.mKillMore = false;
            if (!(AppMngConfig.getKillMoreFlag() && memLevel == AwareAppMngSort.MSG_PRINT_BETA_LOG)) {
                z = false;
            }
            this.mKillMore = z;
        }

        private boolean isHomeAssocStrong(AwareProcessInfo awareProcInfo) {
            if (awareProcInfo == null || awareProcInfo.mProcInfo == null) {
                return false;
            }
            if (awareProcInfo.mProcInfo.mCurAdj == WifiProCommonUtils.RESP_CODE_TIMEOUT && (awareProcInfo.mProcInfo.mType == AwareAppMngSort.APPMNG_MEM_ALLOWSTOP_GROUP || awareProcInfo.mProcInfo.mType == AwareAppMngSort.APPMNG_MEM_ALL_GROUP)) {
                return true;
            }
            return this.mHomeStrong.contains(Integer.valueOf(awareProcInfo.mPid));
        }

        private void loadHomeAssoc(int homePid, Map<Integer, AwareProcessInfo> allProc) {
            Set<Integer> homeStrong = new ArraySet();
            AwareAppAssociate.getInstance().getAssocListForPid(homePid, homeStrong);
            for (Integer pid : homeStrong) {
                AwareProcessInfo awareProcInfo = (AwareProcessInfo) allProc.get(pid);
                if (!(awareProcInfo == null || awareProcInfo.mProcInfo == null || awareProcInfo.mHasShownUi || awareProcInfo.mProcInfo.mType != AwareAppMngSort.APPMNG_MEM_ALLOWSTOP_GROUP)) {
                    this.mHomeStrong.add(pid);
                }
            }
        }

        private boolean isHabitTopN(int pid) {
            return this.mHabitTopN != null ? this.mHabitTopN.contains(Integer.valueOf(pid)) : false;
        }

        private boolean isHabitFreqN(List<String> packageList) {
            if (packageList == null || this.mHabitFreqN == null) {
                return false;
            }
            for (String pkg : packageList) {
                if (this.mHabitFreqN.contains(pkg)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isHomeProcess(int pid) {
            return this.mHomeProcessPid == pid;
        }

        public int getKeyBackgroupTypeInternal(ProcessInfo procInfo) {
            if (procInfo == null) {
                return AwareAppMngSort.INVALID_VALUE;
            }
            return AwareAppKeyBackgroup.getInstance().getKeyBackgroupTypeInternal(procInfo.mPid, procInfo.mUid, procInfo.mPackageName);
        }

        public int getKeyBackgroupTypeInternalByPid(ProcessInfo procInfo) {
            if (procInfo == null) {
                return AwareAppMngSort.INVALID_VALUE;
            }
            return AwareAppKeyBackgroup.getInstance().getKeyBackgroupTypeInternal(procInfo.mPid, AwareAppMngSort.MEM_LEVEL0, null);
        }

        private Set<Integer> getHabitAppTopN(Map<Integer, AwareProcessInfo> proc, int topN) {
            if (proc == null) {
                return null;
            }
            List<AwareProcessInfo> procList = new ArrayList();
            for (Entry<Integer, AwareProcessInfo> pm : proc.entrySet()) {
                AwareProcessInfo info = (AwareProcessInfo) pm.getValue();
                if (info.mImportance != AwareAppMngSort.HABITMAX_IMPORT) {
                    procList.add(info);
                }
            }
            Collections.sort(procList, new ProcessHabitCompare());
            ArraySet<Integer> uidTopN = new ArraySet();
            int foundUidNum = AwareAppMngSort.MEM_LEVEL0;
            ArraySet<Integer> procTopN = new ArraySet();
            for (AwareProcessInfo info2 : procList) {
                if (info2 != null) {
                    boolean need = uidTopN.contains(Integer.valueOf(info2.mProcInfo.mUid));
                    if (foundUidNum < topN && !need) {
                        uidTopN.add(Integer.valueOf(info2.mProcInfo.mUid));
                        foundUidNum += AwareAppMngSort.MSG_PRINT_BETA_LOG;
                        need = true;
                    }
                    if (need) {
                        procTopN.add(Integer.valueOf(info2.mProcInfo.mPid));
                    }
                }
            }
            return procTopN;
        }

        private List<String> getHabitAppFreq() {
            return null;
        }

        private void updateVisibleWin() {
            if (this.mAllProcNeedSort != null) {
                Set<Integer> visibleWindows = new ArraySet();
                AwareAppAssociate.getInstance().getVisibleWindows(visibleWindows);
                for (Integer pid : visibleWindows) {
                    AwareProcessInfo procInfo = (AwareProcessInfo) this.mAllProcNeedSort.get(pid);
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

    enum ShortageSubClassRate {
        ;
        
        String mDescription;
        int subClass;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.iaware.appmng.AwareAppMngSort.ShortageSubClassRate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.iaware.appmng.AwareAppMngSort.ShortageSubClassRate.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.iaware.appmng.AwareAppMngSort.ShortageSubClassRate.<clinit>():void");
        }

        private ShortageSubClassRate(String description) {
            this.mDescription = description;
            this.subClass = AwareAppMngSort.INVALID_VALUE;
        }

        public String description() {
            return this.mDescription;
        }

        public int getSubClassRate() {
            return this.subClass;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.iaware.appmng.AwareAppMngSort.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.iaware.appmng.AwareAppMngSort.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.iaware.appmng.AwareAppMngSort.<clinit>():void");
    }

    private AwareAppMngSort(Context context) {
        this.mAssocEnable = true;
        this.mLastBetaLogOutTime = 0;
        this.mHwAMS = null;
        this.mHandler = null;
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
            Set<Integer> strong = new ArraySet();
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
        if (!app.mHasShownUi || app.mPid == getCurHomeProcessPid() || client.mProcInfo.mCurAdj <= WifiProCommonUtils.HTTP_REACHALBE_HOME) {
            return true;
        }
        return false;
    }

    private void loadAssocListForPid(int pid, Map<Integer, AwareProcessInfo> pidsClass, Set<Integer> strong, Map<Integer, AwareProcessInfo> strongAssocProc) {
        if (pidsClass != null && strongAssocProc != null && strong != null) {
            AwareAppAssociate.getInstance().getAssocListForPid(pid, strong);
            for (Integer sPid : strong) {
                AwareProcessInfo procInfo = (AwareProcessInfo) pidsClass.get(sPid);
                if (procInfo != null && isAssocRelation((AwareProcessInfo) pidsClass.get(Integer.valueOf(pid)), procInfo)) {
                    strongAssocProc.put(sPid, procInfo);
                }
            }
        }
    }

    private ArrayMap<Integer, AwareProcessInfo> getNeedSortedProcesses(Map<Integer, AwareProcessInfo> foreGrdProc, Set<Integer> keyPercepServicePid, CachedWhiteList cachedWhitelist, ShortageProcessInfo shortageProc) {
        ArrayList<ProcessInfo> procs = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procs.isEmpty()) {
            return null;
        }
        Map<Integer, AwareProcessBaseInfo> baseInfos = this.mHwAMS != null ? this.mHwAMS.getAllProcessBaseInfo() : null;
        if (baseInfos == null || baseInfos.isEmpty()) {
            return null;
        }
        int curUserUid = AwareAppAssociate.getInstance().getCurUserId();
        Set<Integer> fgServiceUid = new ArraySet();
        ArraySet<Integer> importUid = new ArraySet();
        ArrayMap<Integer, Integer> percepServicePid = new ArrayMap();
        ArrayMap<Integer, AwareProcessInfo> allProcNeedSort = new ArrayMap();
        for (ProcessInfo procInfo : procs) {
            AwareProcessInfo awareProcInfo;
            if (procInfo != null) {
                AwareProcessBaseInfo updateInfo = (AwareProcessBaseInfo) baseInfos.get(Integer.valueOf(procInfo.mPid));
                if (updateInfo != null) {
                    procInfo.mCurAdj = updateInfo.mCurAdj;
                    procInfo.mForegroundActivities = updateInfo.mForegroundActivities;
                    procInfo.mAdjType = updateInfo.mAdjType;
                    awareProcInfo = new AwareProcessInfo(procInfo.mPid, MEM_LEVEL0, MEM_LEVEL0, ClassRate.NORMAL.ordinal(), procInfo);
                    awareProcInfo.mHasShownUi = updateInfo.mHasShownUi;
                    cachedWhitelist.updateProcessInfoByConfig(awareProcInfo);
                    if (curUserUid != 0 || isCurUserProc(procInfo, curUserUid)) {
                        allProcNeedSort.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                        if (procInfo.mForegroundActivities) {
                            foreGrdProc.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                        }
                        if (procInfo.mCurAdj >= WifiProCommonUtils.HTTP_REACHALBE_HOME) {
                            if (shortageProc.getKeyBackgroupTypeInternalByPid(procInfo) == APPMNG_MEM_ALLOWSTOP_GROUP) {
                                shortageProc.mAudioOut.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                            } else {
                                if (shortageProc.getKeyBackgroupTypeInternalByPid(procInfo) == MSG_PRINT_BETA_LOG) {
                                    shortageProc.mAudioIn.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                                }
                            }
                        }
                        if (procInfo.mCurAdj < WifiProCommonUtils.HTTP_REACHALBE_HOME) {
                            importUid.add(Integer.valueOf(procInfo.mUid));
                        } else if (procInfo.mCurAdj == WifiProCommonUtils.HTTP_REACHALBE_HOME && FG_SERVICE.equals(procInfo.mAdjType)) {
                            fgServiceUid.add(Integer.valueOf(procInfo.mUid));
                        } else if (procInfo.mCurAdj == WifiProCommonUtils.HTTP_REACHALBE_HOME && ADJTYPE_SERVICE.equals(procInfo.mAdjType)) {
                            percepServicePid.put(Integer.valueOf(procInfo.mPid), Integer.valueOf(procInfo.mUid));
                        } else if (procInfo.mCurAdj == WifiProCommonUtils.HTTP_REACHALBE_HOME) {
                            importUid.add(Integer.valueOf(procInfo.mUid));
                        }
                    } else {
                        shortageProc.mNonCurUserProc.put(Integer.valueOf(procInfo.mPid), awareProcInfo);
                    }
                }
            }
        }
        int myPid = Process.myPid();
        for (Entry<Integer, Integer> m : percepServicePid.entrySet()) {
            int pid = ((Integer) m.getKey()).intValue();
            int uid = ((Integer) m.getValue()).intValue();
            if (!importUid.contains(Integer.valueOf(uid))) {
                if (fgServiceUid.contains(Integer.valueOf(uid))) {
                    Set<Integer> strong = new ArraySet();
                    AwareAppAssociate.getInstance().getAssocClientListForPid(pid, strong);
                    for (Integer clientPid : strong) {
                        if (clientPid.intValue() != myPid) {
                            awareProcInfo = (AwareProcessInfo) allProcNeedSort.get(clientPid);
                            if (awareProcInfo != null && awareProcInfo.mProcInfo != null && awareProcInfo.mProcInfo.mCurAdj <= WifiProCommonUtils.HTTP_REACHALBE_HOME) {
                                keyPercepServicePid.add(Integer.valueOf(pid));
                                break;
                            }
                        } else {
                            keyPercepServicePid.add(Integer.valueOf(pid));
                            break;
                        }
                    }
                }
                keyPercepServicePid.add(Integer.valueOf(pid));
            } else {
                keyPercepServicePid.add(Integer.valueOf(pid));
            }
        }
        return allProcNeedSort;
    }

    private boolean isCurUserProc(ProcessInfo procInfo, int curUserUid) {
        boolean z = false;
        if (procInfo == null) {
            return false;
        }
        if (UserHandle.getUserId(procInfo.mUid) == curUserUid) {
            z = true;
        }
        return z;
    }

    private void groupNonCurUserProc(ArrayMap<Integer, AwareProcessBlockInfo> classNormal, ArrayMap<Integer, AwareProcessInfo> nonCurUserProc) {
        if (classNormal != null && nonCurUserProc != null) {
            for (Entry<Integer, AwareProcessInfo> m : nonCurUserProc.entrySet()) {
                AwareProcessInfo awareProcInfo = (AwareProcessInfo) m.getValue();
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
        if (procInfo.mType == APPMNG_MEM_ALLOWSTOP_GROUP) {
            z = true;
        }
        return z;
    }

    private boolean groupIntoForbidstop(ShortageProcessInfo shortageProc, AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        int curAdj = procInfo.mCurAdj;
        int classType = ClassRate.UNKNOWN.ordinal();
        int subClassType = ForbidSubClassRate.NONE.ordinal();
        boolean isGroup = true;
        if (procInfo.mForegroundActivities) {
            shortageProc.recordForegroundUid(procInfo.mUid, procInfo.mPackageName);
        }
        if (curAdj < 0) {
            classType = ClassRate.PERSIST.ordinal();
        } else if (curAdj < WifiProCommonUtils.HTTP_REACHALBE_HOME) {
            classType = ClassRate.FOREGROUND.ordinal();
        } else if (awareProcInfo.mXmlConfig == null || !isCfgDefaultGroup(awareProcInfo, MEM_LEVEL0)) {
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

    private static boolean isPkgIncludeForTgt(ArrayList<String> tgtPkg, ArrayList<String> dstPkg) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isLastRecentlyUsedBase(ProcessInfo procInfo, Map<Integer, AwareProcessInfo> allProcNeedSort, AwareAppLruBase appLruBase, long decayTime) {
        if (procInfo == null || allProcNeedSort == null || appLruBase == null || procInfo.mUid != appLruBase.mUid) {
            return false;
        }
        if (decayTime != FOREVER_DECAYTIME && SystemClock.elapsedRealtime() - appLruBase.mTime > decayTime) {
            return false;
        }
        if (!AwareAppAssociate.isDealAsPkgUid(procInfo.mUid)) {
            return true;
        }
        AwareProcessInfo prevProcInfo = (AwareProcessInfo) allProcNeedSort.get(Integer.valueOf(appLruBase.mPid));
        if (prevProcInfo == null) {
            return false;
        }
        return isPkgIncludeForTgt(procInfo.mPackageName, prevProcInfo.mProcInfo.mPackageName);
    }

    private boolean isPerceptable(ProcessInfo procInfo) {
        if (procInfo == null || procInfo.mCurAdj != WifiProCommonUtils.HTTP_REACHALBE_HOME || FG_SERVICE.equals(procInfo.mAdjType) || ADJTYPE_SERVICE.equals(procInfo.mAdjType)) {
            return false;
        }
        return true;
    }

    private boolean isFgServices(ProcessInfo procInfo, ShortageProcessInfo shortageProc) {
        if (procInfo == null || procInfo.mCurAdj != WifiProCommonUtils.HTTP_REACHALBE_HOME) {
            return false;
        }
        if (FG_SERVICE.equals(procInfo.mAdjType) && (isSystemProcess(procInfo) || shortageProc.isForegroundUid(procInfo))) {
            return true;
        }
        return ADJTYPE_SERVICE.equals(procInfo.mAdjType) && (isSystemProcess(procInfo) || shortageProc.isForegroundUid(procInfo));
    }

    public boolean isFgServicesImportantByAdjtype(String adjType) {
        if (FG_SERVICE.equals(adjType) || ADJTYPE_SERVICE.equals(adjType)) {
            return true;
        }
        return false;
    }

    private boolean isFgServicesImportant(ProcessInfo procInfo) {
        if (procInfo != null && procInfo.mCurAdj == WifiProCommonUtils.HTTP_REACHALBE_HOME) {
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
        } else if (curAdj == HwGlobalActionsView.VIBRATE_DELAY) {
            classType = ClassRate.KEYBACKGROUND.ordinal();
            subClass = ShortageSubClassRate.NONE.ordinal();
        } else if (curAdj == HwCircleAnimation.LUNCH_DURATION) {
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
        } else if (shortageProc.getKeyBackgroupTypeInternal(procInfo) == APPMNG_MEM_ALL_GROUP) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.GUIDE.ordinal();
        } else if (shortageProc.getKeyBackgroupTypeInternal(procInfo) == 5) {
            classType = ClassRate.KEYSERVICES.ordinal();
            subClass = ShortageSubClassRate.DOWN_UP_LOAD.ordinal();
        } else if (cachedWhitelist.isLowEnd() || shortageProc.getKeyBackgroupTypeInternal(procInfo) != 4) {
            if (isLastRecentlyUsed(procInfo, shortageProc, shortageProc.mKillMore ? PREVIOUS_APP_DIRCACTIVITY_DECAYTIME : FOREVER_DECAYTIME)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.PREVIOUS.ordinal();
            } else if (awareProcInfo.mXmlConfig != null && isCfgDefaultGroup(awareProcInfo, MSG_PRINT_BETA_LOG) && (!awareProcInfo.mXmlConfig.mFrequentlyUsed || shortageProc.isHabitTopN(procInfo.mPid))) {
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
            } else if (shortageProc.isHabitFreqN(procInfo.mPackageName)) {
                classType = ClassRate.KEYSERVICES.ordinal();
                subClass = ShortageSubClassRate.FREQN.ordinal();
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
        if (decayTime == FOREVER_DECAYTIME && procInfo.mCurAdj == HwActivityManagerService.PREVIOUS_APP_ADJ) {
            return true;
        }
        boolean z;
        if (isLastRecentlyUsedBase(procInfo, shortageProc.mAllProcNeedSort, shortageProc.mPrevAmsBase, decayTime)) {
            z = true;
        } else {
            z = isLastRecentlyUsedBase(procInfo, shortageProc.mAllProcNeedSort, shortageProc.mPrevAwareBase, decayTime);
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
        if (!(clocks == null || clocks.isEmpty() || packageNames == null || packageNames.isEmpty())) {
            for (String pkg : packageNames) {
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
        return isLastRecentlyUsedBase(procInfo, shortageProc.mAllProcNeedSort, shortageProc.mRecentTaskAppBase, FOREVER_DECAYTIME);
    }

    private boolean isKeySysProc(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (!isSystemProcess(procInfo)) {
            return false;
        }
        if (procInfo.mCurAdj == HwActivityManagerService.SERVICE_ADJ) {
            return true;
        }
        if (procInfo.mUid < HABITMAX_IMPORT && procInfo.mCurAdj == HwActivityManagerService.SERVICE_B_ADJ) {
            return true;
        }
        if (procInfo.mUid >= HABITMAX_IMPORT || awareProcInfo.mHasShownUi || procInfo.mCreatedTime == FOREVER_DECAYTIME || SystemClock.elapsedRealtime() - procInfo.mCreatedTime >= AppMngConfig.getKeySysDecay()) {
            return procInfo.mUid >= HABITMAX_IMPORT && !awareProcInfo.mHasShownUi && procInfo.mCreatedTime != FOREVER_DECAYTIME && SystemClock.elapsedRealtime() - procInfo.mCreatedTime < AppMngConfig.getSysDecay();
        } else {
            return true;
        }
    }

    private boolean groupIntoAllowStop(AwareProcessInfo awareProcInfo, ShortageProcessInfo shortageProc, CachedWhiteList cachedWhitelist) {
        if (awareProcInfo == null) {
            return false;
        }
        int subClassType;
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (shortageProc.mKillMore && isLastRecentlyUsed(procInfo, shortageProc, FOREVER_DECAYTIME)) {
            subClassType = AllowStopSubClassRate.PREVIOUS.ordinal();
        } else if (shortageProc.mKillMore && shortageProc.isHabitTopN(procInfo.mPid)) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isBgDecayApp(String pkgName, CachedWhiteList cachedWhitelist) {
        if (pkgName == null || cachedWhitelist == null || cachedWhitelist.mBgNonDecayPkg.contains(pkgName)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                } else if (!cachedWhitelist.isLowEnd() || !isBgDecayApp(pkgName, cachedWhitelist)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean getAllowCleanResByProc(int classRate, int subRate, CachedWhiteList cachedWhitelist, ArrayList<String> pkgList, boolean isRestartByAppType) {
        boolean z = true;
        if (cachedWhitelist == null) {
            return false;
        }
        if (!cachedWhitelist.isLowEnd()) {
            return true;
        }
        if (subRate != AllowStopSubClassRate.FG_SERVICES.ordinal()) {
            if (subRate != AllowStopSubClassRate.OTHER.ordinal()) {
                z = false;
            }
            return z;
        } else if (!isRestartByAppType) {
            return true;
        } else {
            if (!AppMngConfig.getAbroadFlag() || pkgList == null || pkgList.isEmpty()) {
                return false;
            }
            for (String pkgName : pkgList) {
                if (!cachedWhitelist.mAllUnProtectApp.contains(pkgName)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean getAllowCleanResByAppType(int appType) {
        switch (appType) {
            case IAwareHabitUtils.HABIT_PROTECT_MAX_TRAIN_COUNTS /*14*/:
                return false;
            default:
                return true;
        }
    }

    private void updateAppType(com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo r9, com.android.server.mtm.iaware.appmng.AwareAppMngSort.CachedWhiteList r10) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r8 = this;
        if (r9 != 0) goto L_0x0003;
    L_0x0002:
        return;
    L_0x0003:
        r5 = r9.mProcessList;
        r3 = r5.iterator();
    L_0x0009:
        r5 = r3.hasNext();
        if (r5 == 0) goto L_0x006c;
    L_0x000f:
        r2 = r3.next();
        r2 = (com.android.server.mtm.iaware.appmng.AwareProcessInfo) r2;
        if (r2 == 0) goto L_0x0009;
    L_0x0017:
        r5 = r2.mProcInfo;
        if (r5 == 0) goto L_0x0009;
    L_0x001b:
        r5 = r2.mProcInfo;
        r5 = r5.mPackageName;
        if (r5 == 0) goto L_0x0009;
    L_0x0021:
        r5 = r2.mProcInfo;
        r5 = r5.mPackageName;
        r1 = r5.iterator();
    L_0x0029:
        r5 = r1.hasNext();
        if (r5 == 0) goto L_0x0009;
    L_0x002f:
        r0 = r1.next();
        r0 = (java.lang.String) r0;
        r4 = com.android.server.rms.algorithm.utils.IAwareHabitUtils.getAppTypeForAppMng(r0);
        r5 = DEBUG;
        if (r5 == 0) goto L_0x0062;
    L_0x003d:
        r5 = "AwareAppMngSort";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "getAppType packagename ";
        r6 = r6.append(r7);
        r6 = r6.append(r0);
        r7 = " type : ";
        r6 = r6.append(r7);
        r6 = r6.append(r4);
        r6 = r6.toString();
        android.rms.iaware.AwareLog.i(r5, r6);
    L_0x0062:
        switch(r4) {
            case 3: goto L_0x0069;
            case 4: goto L_0x0065;
            case 5: goto L_0x0065;
            case 6: goto L_0x0069;
            case 7: goto L_0x0069;
            case 8: goto L_0x0069;
            case 9: goto L_0x0069;
            case 10: goto L_0x0069;
            case 11: goto L_0x0065;
            case 12: goto L_0x0069;
            case 13: goto L_0x0069;
            case 14: goto L_0x0069;
            case 15: goto L_0x0069;
            case 16: goto L_0x0065;
            case 17: goto L_0x0069;
            case 18: goto L_0x0069;
            default: goto L_0x0065;
        };
    L_0x0065:
        r5 = -1;
        r9.mAppType = r5;
        return;
    L_0x0069:
        r9.mAppType = r4;
        goto L_0x0029;
    L_0x006c:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.iaware.appmng.AwareAppMngSort.updateAppType(com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo, com.android.server.mtm.iaware.appmng.AwareAppMngSort$CachedWhiteList):void");
    }

    private boolean getRestartFlagByAppType(int appType) {
        switch (appType) {
            case APPMNG_MEM_ALL_GROUP /*3*/:
            case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
            case LifeCycleStateMachine.TIME_OUT /*7*/:
            case CLASSRATE_KEY_OFFSET /*8*/:
            case HwGnssLogHandlerMsgID.UPDATESVSTATUS /*9*/:
            case AwareAppMngDFX.APPLICATION_STARTTYPE_COLD /*10*/:
            case HwGnssLogHandlerMsgID.UPDATESETPOSMODE /*12*/:
            case HwGnssLogHandlerMsgID.PERMISSIONERR /*13*/:
            case IAwareHabitUtils.HABIT_PROTECT_MAX_TRAIN_COUNTS /*14*/:
            case HwGnssLogHandlerMsgID.ADDGEOFENCESTATUS /*15*/:
            case HwGnssLogHandlerMsgID.UPDATELOSTPOSITION /*17*/:
            case HwGnssLogHandlerMsgID.UPDATEAPKNAME /*18*/:
                return false;
            default:
                return true;
        }
    }

    public boolean needCheckAlarm(AwareProcessBlockInfo info) {
        if (info == null) {
            return false;
        }
        AwareDefaultConfigList whiteListInstance = AwareDefaultConfigList.getInstance();
        if (whiteListInstance == null || !whiteListInstance.isLowEnd()) {
            return info.mAlarmChk;
        }
        return true;
    }

    private void addClassToAllClass(Map<Integer, AwareProcessBlockInfo> appAllClass, Map<Integer, AwareProcessBlockInfo> blocks, ShortageProcessInfo shortageProc, Map<Integer, AwareProcessBlockInfo> allUids, CachedWhiteList cachedWhitelist, Set<String> clocks, boolean chkRestartFlag) {
        for (Entry<Integer, AwareProcessBlockInfo> m : blocks.entrySet()) {
            AwareProcessBlockInfo value = (AwareProcessBlockInfo) m.getValue();
            Map<Integer, AwareProcessBlockInfo> map = appAllClass;
            map.put(Integer.valueOf(((Integer) m.getKey()).intValue()), value);
            if (chkRestartFlag) {
                boolean isWidgetApp = false;
                boolean isClockApp = false;
                boolean isImApp = false;
                boolean isAllowCleanRes = true;
                boolean isRestart = false;
                boolean isRestartByAppType = false;
                updateAppType(value, cachedWhitelist);
                if (getRestartFlagByAppType(value.mAppType)) {
                    isRestartByAppType = true;
                } else if (!(cachedWhitelist.isLowEnd() || getAllowCleanResByAppType(value.mAppType))) {
                    isAllowCleanRes = false;
                }
                for (AwareProcessInfo procInfo : value.mProcessList) {
                    if (!AppMngConfig.getRestartFlag()) {
                        if (!getRestartFlagByProc(value.mClassRate, value.mSubClassRate, procInfo.mProcInfo.mPackageName, cachedWhitelist, isRestartByAppType, procInfo.mProcInfo, value.mAppType, value)) {
                            if (isAllowCleanRes) {
                                if (!isWidgetApp) {
                                    isWidgetApp = isWidget(shortageProc.mWidgetPkg, procInfo.mProcInfo.mPackageName);
                                }
                                if (!isClockApp) {
                                    isClockApp = isClock(clocks, procInfo.mProcInfo.mPackageName);
                                }
                                if (!isImApp) {
                                    isImApp = cachedWhitelist.isInAllHabitList(procInfo.mProcInfo.mPackageName);
                                }
                                if (procInfo.mXmlConfig != null) {
                                    isAllowCleanRes = procInfo.mXmlConfig.mResCleanAllow;
                                }
                                if (isAllowCleanRes) {
                                    if (!getAllowCleanResByProc(value.mClassRate, value.mSubClassRate, cachedWhitelist, procInfo.mProcInfo.mPackageName, isRestartByAppType)) {
                                        isAllowCleanRes = false;
                                    }
                                }
                            }
                        }
                    }
                    isRestart = true;
                    break;
                }
                if (!(isRestart || isWidgetApp || isClockApp || isImApp || !isAllowCleanRes)) {
                    if (inSameUids(allUids, value.mProcessList)) {
                        value.mResCleanAllow = true;
                        value.mCleanAlarm = true;
                        if (!isRestart || isImApp) {
                            for (AwareProcessInfo awareProcessInfo : value.mProcessList) {
                                awareProcessInfo.mRestartFlag = true;
                            }
                        }
                    }
                }
                value.mResCleanAllow = false;
                value.mCleanAlarm = false;
                if (isRestart) {
                }
                while (procInfo$iterator.hasNext()) {
                    awareProcessInfo.mRestartFlag = true;
                }
            }
        }
    }

    private void addProcessInfoToGroupBlock(Map<Integer, AwareProcessBlockInfo> appAllClass, AwareProcessInfo pinfo, int key, Map<Integer, Map<Integer, AwareProcessBlockInfo>> groupBlock) {
        Integer groupKey = Integer.valueOf((pinfo.mClassRate << CLASSRATE_KEY_OFFSET) + pinfo.mSubClassRate);
        Map<Integer, AwareProcessBlockInfo> groupUid = (Map) groupBlock.get(groupKey);
        if (groupUid == null) {
            groupUid = new ArrayMap();
            groupBlock.put(groupKey, groupUid);
        }
        AwareProcessBlockInfo block = (AwareProcessBlockInfo) groupUid.get(Integer.valueOf(pinfo.mProcInfo.mUid));
        if (block == null) {
            block = new AwareProcessBlockInfo(pinfo.mProcInfo.mUid, false, pinfo.mClassRate);
            block.mProcessList.add(pinfo);
            block.mSubClassRate = pinfo.mSubClassRate;
            block.mImportance = pinfo.mImportance;
            block.mMinAdj = pinfo.mProcInfo.mCurAdj;
            groupUid.put(Integer.valueOf(pinfo.mProcInfo.mUid), block);
            appAllClass.put(Integer.valueOf(key), block);
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
        ArraySet<Integer> keyPercepServicePid = new ArraySet();
        ArrayMap<Integer, AwareProcessInfo> foreGrdProc = new ArrayMap();
        ShortageProcessInfo shortageProc = new ShortageProcessInfo(subType);
        CachedWhiteList cachedWhitelist = new CachedWhiteList();
        cachedWhitelist.updateCachedList();
        Set clocks = null;
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null) {
            clocks = habit.getAppListByType(5);
        }
        ArrayMap<Integer, AwareProcessInfo> allProcNeedSort = getNeedSortedProcesses(foreGrdProc, keyPercepServicePid, cachedWhitelist, shortageProc);
        if (allProcNeedSort == null) {
            return null;
        }
        ArrayMap<Integer, AwareProcessBlockInfo> appAllClass = new ArrayMap();
        ArrayMap<Integer, AwareProcessBlockInfo> classShort = new ArrayMap();
        ArrayMap<Integer, AwareProcessBlockInfo> classNormal = new ArrayMap();
        Map<Integer, Map<Integer, AwareProcessBlockInfo>> groupBlock = new ArrayMap();
        Map<Integer, AwareProcessBlockInfo> allUids = groupByUid(allProcNeedSort);
        shortageProc.updateBaseInfo(allProcNeedSort, getCurHomeProcessPid(), isRecentTaskShow(foreGrdProc), keyPercepServicePid);
        for (Entry<Integer, AwareProcessInfo> m : allProcNeedSort.entrySet()) {
            AwareProcessInfo awareProcInfo = (AwareProcessInfo) m.getValue();
            boolean isGroup = groupIntoForbidstop(shortageProc, awareProcInfo);
            if (!isGroup) {
                isGroup = groupIntoShortageStop(awareProcInfo, shortageProc, cachedWhitelist);
            }
            if (!isGroup) {
                groupIntoAllowStop(awareProcInfo, shortageProc, cachedWhitelist);
            }
            if (awareProcInfo.mClassRate < ClassRate.KEYBACKGROUND.ordinal()) {
                addProcessInfoToBlock(appAllClass, awareProcInfo, awareProcInfo.mPid);
            } else if (awareProcInfo.mClassRate < ClassRate.NORMAL.ordinal()) {
                addProcessInfoToGroupBlock(classShort, awareProcInfo, awareProcInfo.mPid, groupBlock);
            } else {
                addProcessInfoToBlock(classNormal, awareProcInfo, awareProcInfo.mPid);
            }
        }
        groupNonCurUserProc(classNormal, shortageProc.mNonCurUserProc);
        adjustClassRate(allProcNeedSort, classShort, classNormal, appAllClass, shortageProc, allUids, cachedWhitelist, clocks);
        addClassToAllClass(appAllClass, classShort, shortageProc, allUids, cachedWhitelist, clocks, false);
        return appAllClass;
    }

    private Map<AppBlockKeyBase, AwareProcessBlockInfo> convertToUidBlock(Map<Integer, AwareProcessBlockInfo> pidsBlock, ArrayMap<Integer, AppBlockKeyBase> pidsAppBlock, ArrayMap<Integer, AppBlockKeyBase> uidAppBlock) {
        if (pidsBlock == null) {
            return null;
        }
        Map<AppBlockKeyBase, AwareProcessBlockInfo> uids = new ArrayMap();
        for (Entry<Integer, AwareProcessBlockInfo> m : pidsBlock.entrySet()) {
            AwareProcessBlockInfo blockInfo = (AwareProcessBlockInfo) m.getValue();
            if (blockInfo.mProcessList != null) {
                for (AwareProcessInfo awareProcInfo : blockInfo.mProcessList) {
                    AppBlockKeyBase blockKeyValue;
                    AwareProcessBlockInfo info;
                    if (AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
                        blockKeyValue = (AppBlockKeyBase) pidsAppBlock.get(Integer.valueOf(awareProcInfo.mProcInfo.mPid));
                    } else {
                        blockKeyValue = (AppBlockKeyBase) uidAppBlock.get(Integer.valueOf(awareProcInfo.mProcInfo.mUid));
                    }
                    if (blockKeyValue == null) {
                        info = null;
                    } else {
                        info = (AwareProcessBlockInfo) uids.get(blockKeyValue);
                    }
                    if (info == null) {
                        info = new AwareProcessBlockInfo(awareProcInfo.mProcInfo.mUid, false, awareProcInfo.mClassRate);
                        info.mProcessList.add(awareProcInfo);
                        info.mClassRate = blockInfo.mClassRate;
                        info.mSubClassRate = blockInfo.mSubClassRate;
                        AppBlockKeyBase keyBase = new AppBlockKeyBase(awareProcInfo.mProcInfo.mPid, awareProcInfo.mProcInfo.mUid);
                        if (AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
                            pidsAppBlock.put(Integer.valueOf(awareProcInfo.mProcInfo.mPid), keyBase);
                        } else {
                            uidAppBlock.put(Integer.valueOf(awareProcInfo.mProcInfo.mUid), keyBase);
                        }
                        uids.put(keyBase, info);
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
        Map<Integer, AwareProcessBlockInfo> uids = new ArrayMap();
        Map userHabitMap = null;
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null) {
            userHabitMap = habit.getTopList(allProcNeedSort);
        }
        for (Entry<Integer, AwareProcessInfo> m : allProcNeedSort.entrySet()) {
            AwareProcessInfo info = (AwareProcessInfo) m.getValue();
            if (info != null) {
                Integer num = null;
                if (userHabitMap != null) {
                    num = (Integer) userHabitMap.get(Integer.valueOf(info.mPid));
                }
                if (num != null) {
                    info.mImportance = num.intValue();
                } else {
                    info.mImportance = HABITMAX_IMPORT;
                }
                AwareProcessBlockInfo block = (AwareProcessBlockInfo) uids.get(Integer.valueOf(info.mProcInfo.mUid));
                if (block == null) {
                    block = new AwareProcessBlockInfo(info.mProcInfo.mUid, false, info.mClassRate);
                    block.mProcessList.add(info);
                    uids.put(Integer.valueOf(info.mProcInfo.mUid), block);
                } else {
                    block.mProcessList.add(info);
                }
            }
        }
        return uids;
    }

    private void adjustClassByStrongAssoc(AwareProcessBlockInfo blockInfo, Map<Integer, AwareProcessInfo> allProcNeedSort, Map<AppBlockKeyBase, AwareProcessBlockInfo> uids, ArrayMap<Integer, AppBlockKeyBase> pidsAppBlock, ArrayMap<Integer, AppBlockKeyBase> uidAppBlock, Map<AppBlockKeyBase, AwareProcessBlockInfo> assocNormalUids) {
        ArrayMap<Integer, AwareProcessInfo> strong = new ArrayMap();
        loadAppAssoc(blockInfo.mProcessList, allProcNeedSort, strong);
        for (Entry<Integer, AwareProcessInfo> sm : strong.entrySet()) {
            AwareProcessInfo procInfo = (AwareProcessInfo) allProcNeedSort.get(sm.getKey());
            if (!(procInfo == null || procInfo.mProcInfo == null)) {
                AppBlockKeyBase blockKeyValue;
                AwareProcessBlockInfo blockInfoAssoc;
                if (AwareAppAssociate.isDealAsPkgUid(procInfo.mProcInfo.mUid)) {
                    blockKeyValue = (AppBlockKeyBase) pidsAppBlock.get(Integer.valueOf(procInfo.mProcInfo.mPid));
                } else {
                    blockKeyValue = (AppBlockKeyBase) uidAppBlock.get(Integer.valueOf(procInfo.mProcInfo.mUid));
                }
                if (blockKeyValue == null) {
                    blockInfoAssoc = null;
                } else {
                    blockInfoAssoc = (AwareProcessBlockInfo) uids.get(blockKeyValue);
                }
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
        if (allProcNeedSort != null && classNormal != null) {
            ArrayMap<Integer, AppBlockKeyBase> pidsAppBlock = new ArrayMap();
            ArrayMap<Integer, AppBlockKeyBase> uidAppBlock = new ArrayMap();
            Map<AppBlockKeyBase, AwareProcessBlockInfo> uids = convertToUidBlock(classNormal, pidsAppBlock, uidAppBlock);
            if (uids != null) {
                AwareProcessBlockInfo blockInfo;
                Map<Integer, AwareProcessBlockInfo> assocNormalClass = new ArrayMap();
                for (Entry<Integer, AwareProcessBlockInfo> m : classShort.entrySet()) {
                    blockInfo = (AwareProcessBlockInfo) m.getValue();
                    if (blockInfo.mClassRate == ClassRate.KEYSERVICES.ordinal()) {
                        Map<AppBlockKeyBase, AwareProcessBlockInfo> assocNormalUids = new ArrayMap();
                        adjustClassByStrongAssoc(blockInfo, allProcNeedSort, uids, pidsAppBlock, uidAppBlock, assocNormalUids);
                        for (Entry<AppBlockKeyBase, AwareProcessBlockInfo> assocBlock : assocNormalUids.entrySet()) {
                            AppBlockKeyBase blockKeyValue = (AppBlockKeyBase) assocBlock.getKey();
                            AwareProcessBlockInfo blockInfoAssoc = (AwareProcessBlockInfo) assocBlock.getValue();
                            if (AwareAppAssociate.isDealAsPkgUid(blockKeyValue.mUid)) {
                                pidsAppBlock.remove(Integer.valueOf(blockKeyValue.mPid));
                                uids.remove(blockKeyValue);
                            } else {
                                uidAppBlock.remove(Integer.valueOf(blockKeyValue.mUid));
                                uids.remove(blockKeyValue);
                            }
                            assocNormalClass.put(Integer.valueOf(blockKeyValue.mPid), blockInfoAssoc);
                        }
                    }
                }
                classShort.putAll(assocNormalClass);
                Map<Integer, AwareProcessBlockInfo> classNormalBlock = new ArrayMap();
                for (Entry<AppBlockKeyBase, AwareProcessBlockInfo> m2 : uids.entrySet()) {
                    blockInfo = (AwareProcessBlockInfo) m2.getValue();
                    adjustClassByStrongAssoc(blockInfo, allProcNeedSort, uids, pidsAppBlock, uidAppBlock, null);
                    boolean addToAll = false;
                    int subClass = AllowStopSubClassRate.UNKNOWN.ordinal();
                    for (AwareProcessInfo procInfo : blockInfo.mProcessList) {
                        if (addToAll) {
                            if (blockInfo.mSubClassRate > procInfo.mSubClassRate) {
                                blockInfo.mSubClassRate = procInfo.mSubClassRate;
                            }
                            if (blockInfo.mImportance > procInfo.mImportance) {
                                blockInfo.mImportance = procInfo.mImportance;
                            }
                            if (blockInfo.mMinAdj > procInfo.mProcInfo.mCurAdj) {
                                blockInfo.mMinAdj = procInfo.mProcInfo.mCurAdj;
                            }
                        } else {
                            blockInfo.mImportance = procInfo.mImportance;
                            blockInfo.mMinAdj = procInfo.mProcInfo.mCurAdj;
                            blockInfo.mSubClassRate = procInfo.mSubClassRate;
                            classNormalBlock.put(Integer.valueOf(procInfo.mProcInfo.mPid), blockInfo);
                            if (shortageProc.isFgServicesUid(blockInfo.mUid)) {
                                subClass = AllowStopSubClassRate.FG_SERVICES.ordinal();
                            }
                            if (blockInfo.mSubClassRate > subClass) {
                                blockInfo.mSubClassRate = subClass;
                            }
                            addToAll = true;
                        }
                    }
                }
                addClassToAllClass(allClass, classNormalBlock, shortageProc, allUids, cachedWhitelist, clocks, true);
            }
        }
    }

    private boolean inSameUids(Map<Integer, AwareProcessBlockInfo> allUids, List<AwareProcessInfo> lists) {
        if (lists == null || lists.isEmpty()) {
            return false;
        }
        AwareProcessBlockInfo info = (AwareProcessBlockInfo) allUids.get(Integer.valueOf(((AwareProcessInfo) lists.get(MEM_LEVEL0)).mProcInfo.mUid));
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
        List<AwareProcessBlockInfo> procForbidStopList = new ArrayList();
        List<AwareProcessBlockInfo> procShortageStopList = new ArrayList();
        List<AwareProcessBlockInfo> procAllowStopList = new ArrayList();
        for (Entry<Integer, AwareProcessBlockInfo> m : pidsClass.entrySet()) {
            AwareProcessBlockInfo awareProcInfo = (AwareProcessBlockInfo) m.getValue();
            awareProcInfo.mUpdateTime = SystemClock.elapsedRealtime();
            int groupId = MEM_LEVEL0;
            if (awareProcInfo.mClassRate <= ClassRate.FOREGROUND.ordinal()) {
                procForbidStopList.add(awareProcInfo);
            } else if (awareProcInfo.mClassRate < ClassRate.NORMAL.ordinal()) {
                procShortageStopList.add(awareProcInfo);
                groupId = MSG_PRINT_BETA_LOG;
            } else {
                procAllowStopList.add(awareProcInfo);
                groupId = APPMNG_MEM_ALLOWSTOP_GROUP;
            }
            awareProcInfo.setMemGroup(groupId);
        }
        Collections.sort(procShortageStopList);
        Collections.sort(procAllowStopList);
        return new MemSortGroup(procForbidStopList, procShortageStopList, procAllowStopList);
    }

    public static String getClassRateStr(int classRate) {
        ClassRate[] allRate = ClassRate.values();
        int length = allRate.length;
        for (int i = MEM_LEVEL0; i < length; i += MSG_PRINT_BETA_LOG) {
            ClassRate rate = allRate[i];
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ClassRate.UNKNOWN.description();
    }

    public boolean isGroupBeHigher(int pid, int uid, String processName, ArrayList<String> arrayList, int groupId) {
        AwareProcessBaseInfo info = null;
        if (!mEnabled || !this.mAssocEnable) {
            return false;
        }
        AwareAppAssociate awareAssoc = AwareAppAssociate.getInstance();
        if (awareAssoc == null) {
            return false;
        }
        Set<Integer> forePid = new ArraySet();
        awareAssoc.getForeGroundApp(forePid);
        if (forePid.contains(Integer.valueOf(pid))) {
            return true;
        }
        if (this.mHwAMS != null) {
            info = this.mHwAMS.getProcessBaseInfo(pid);
        }
        if (info == null) {
            return false;
        }
        if (info.mCurAdj < WifiProCommonUtils.HTTP_REACHALBE_HOME) {
            return true;
        }
        if (groupId == APPMNG_MEM_ALLOWSTOP_GROUP) {
            if (info.mCurAdj == HwGlobalActionsView.VIBRATE_DELAY || info.mCurAdj == HwCircleAnimation.LUNCH_DURATION) {
                return true;
            }
            return info.mCurAdj == WifiProCommonUtils.HTTP_REACHALBE_HOME && !isFgServicesImportantByAdjtype(info.mAdjType);
        }
    }

    public static boolean checkAppMngEnable() {
        return mEnabled;
    }

    public AwareAppMngSortPolicy getAppMngSortPolicy(int resourceType, int subType, int groupId) {
        if (!mEnabled) {
            return null;
        }
        long startTime = 0;
        if (DEBUG) {
            startTime = System.currentTimeMillis();
        }
        ArrayMap<Integer, List<AwareProcessBlockInfo>> appGroup = new ArrayMap();
        MemSortGroup sortGroup = getAppMemSortGroup(subType);
        if (sortGroup == null) {
            return null;
        }
        if (groupId == 0) {
            appGroup.put(Integer.valueOf(groupId), sortGroup.mProcForbidStopList);
        } else if (groupId == MSG_PRINT_BETA_LOG) {
            appGroup.put(Integer.valueOf(groupId), sortGroup.mProcShortageStopList);
        } else if (groupId == APPMNG_MEM_ALLOWSTOP_GROUP) {
            appGroup.put(Integer.valueOf(groupId), sortGroup.mProcAllowStopList);
        } else if (groupId == APPMNG_MEM_ALL_GROUP) {
            appGroup.put(Integer.valueOf(MEM_LEVEL0), sortGroup.mProcForbidStopList);
            appGroup.put(Integer.valueOf(MSG_PRINT_BETA_LOG), sortGroup.mProcShortageStopList);
            appGroup.put(Integer.valueOf(APPMNG_MEM_ALLOWSTOP_GROUP), sortGroup.mProcAllowStopList);
        }
        AwareAppMngSortPolicy sortPolicy = new AwareAppMngSortPolicy(this.mContext, appGroup);
        if (DEBUG) {
            AwareLog.i(TAG, "        getAppMngSortPolicy eclipse time     :" + (System.currentTimeMillis() - startTime));
            AwareLog.i(TAG, "MemAvailable(KB): " + MemoryReader.getInstance().getMemAvailable());
            dumpPolicy(sortPolicy, null, false);
        }
        if (Log.HWINFO) {
            long curTime = System.currentTimeMillis();
            if (curTime - this.mLastBetaLogOutTime > AppHibernateCst.DELAY_ONE_MINS && this.mHandler != null) {
                BetaLog betaLog = new BetaLog(sortPolicy);
                Message msg = Message.obtain();
                msg.what = MSG_PRINT_BETA_LOG;
                msg.obj = betaLog;
                this.mHandler.sendMessage(msg);
                this.mLastBetaLogOutTime = curTime;
            }
        }
        return sortPolicy;
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
        ForbidSubClassRate[] allRate = ForbidSubClassRate.values();
        int length = allRate.length;
        for (int i = MEM_LEVEL0; i < length; i += MSG_PRINT_BETA_LOG) {
            ForbidSubClassRate rate = allRate[i];
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ForbidSubClassRate.NONE.description();
    }

    private static String getShortageSubClassRateStr(int classRate) {
        ShortageSubClassRate[] allRate = ShortageSubClassRate.values();
        int length = allRate.length;
        for (int i = MEM_LEVEL0; i < length; i += MSG_PRINT_BETA_LOG) {
            ShortageSubClassRate rate = allRate[i];
            if (rate.ordinal() == classRate) {
                return rate.description();
            }
        }
        return ShortageSubClassRate.NONE.description();
    }

    private static String getAllowSubClassRateStr(int classRate) {
        AllowStopSubClassRate[] allRate = AllowStopSubClassRate.values();
        int length = allRate.length;
        for (int i = MEM_LEVEL0; i < length; i += MSG_PRINT_BETA_LOG) {
            AllowStopSubClassRate rate = allRate[i];
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
                for (ProcessInfo procInfo : procs) {
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
        for (ProcessInfo procInfo : procs) {
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
                    print(pw, "AppProc:uid:" + pinfo.mUid + ",import:" + pinfo.mImportance + ",classRates:" + pinfo.mClassRate + ",classStr:" + getClassRateStr(pinfo.mClassRate) + ",subStr:" + getClassStr(pinfo.mClassRate, pinfo.mSubClassRate) + ",subTypeStr:" + pinfo.mSubTypeStr + ",appType:" + pinfo.mAppType);
                    if (pinfo.mProcessList != null) {
                        for (AwareProcessInfo info : pinfo.mProcessList) {
                            print(pw, "     name:" + info.mProcInfo.mProcessName + ",pid:" + info.mProcInfo.mPid + ",uid:" + info.mProcInfo.mUid + ",group:" + info.mMemGroup + ",import:" + info.mImportance + ",classRate:" + info.mClassRate + ",adj:" + info.mProcInfo.mCurAdj + "," + info.mProcInfo.mAdjType + ",classStr:" + getClassRateStr(info.mClassRate) + ",subStr:" + getClassStr(info.mClassRate, info.mSubClassRate) + ",mResCleanAllow:" + allow + ",mRestartFlag:" + info.getRestartFlag() + ",ui:" + info.mHasShownUi);
                        }
                    }
                }
            }
        }
    }

    private void dumpBlock(PrintWriter pw, int memLevel) {
        if (pw != null) {
            if (mEnabled) {
                AwareAppMngSortPolicy policy = getAppMngSortPolicy(MEM_LEVEL0, memLevel, APPMNG_MEM_ALL_GROUP);
                if (policy == null) {
                    pw.println("getAppMngSortPolicy return null!");
                    return;
                } else {
                    dumpPolicy(policy, pw, true);
                    return;
                }
            }
            pw.println("AwareAppMngSort disabled!");
        }
    }

    private void dumpGroupBlock(PrintWriter pw, int group) {
        if (pw != null) {
            if (mEnabled) {
                AwareAppMngSortPolicy policy = getAppMngSortPolicy(MEM_LEVEL0, MEM_LEVEL0, group);
                if (policy == null) {
                    pw.println("getAppMngSortPolicy return null!");
                    return;
                } else {
                    dumpPolicy(policy, pw, true);
                    return;
                }
            }
            pw.println("AwareAppMngSort disabled!");
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
            if (type.equals("mem")) {
                dumpBlock(pw, MEM_LEVEL0);
            } else if (type.equals("mem2")) {
                dumpBlock(pw, MSG_PRINT_BETA_LOG);
            } else if (type.equals("memForbid")) {
                dumpGroupBlock(pw, MEM_LEVEL0);
            } else if (type.equals("memShortage")) {
                dumpGroupBlock(pw, MSG_PRINT_BETA_LOG);
            } else if (type.equals("memAllow")) {
                dumpGroupBlock(pw, APPMNG_MEM_ALLOWSTOP_GROUP);
            } else if (type.equals("enable")) {
                enable();
            } else if (type.equals("disable")) {
                disable();
            } else if (type.equals("checkEnabled")) {
                pw.println("AwareAppMngSort is " + checkAppMngEnable());
            } else if (!type.equals("procinfo")) {
                pw.println("  dump parameter error!");
            } else if (mEnabled) {
                ProcessInfoCollector mProcInfo = ProcessInfoCollector.getInstance();
                if (mProcInfo != null) {
                    updateProcessInfo();
                    mProcInfo.dump(pw);
                }
            } else {
                pw.println("AwareAppMngSort disabled!");
            }
        }
    }

    private void killAllProcess(PrintWriter pw) {
        if (pw != null) {
            MemSortGroup sortGroup = getAppMemSortGroup(MEM_LEVEL0);
            if (sortGroup != null && sortGroup.mProcAllowStopList != null && this.mContext != null) {
                for (AwareProcessBlockInfo infoBlock : sortGroup.mProcAllowStopList) {
                    if (!(infoBlock == null || infoBlock.mProcessList == null)) {
                        ProcessCleaner.getInstance(this.mContext).killProcessesSameUidExt(infoBlock, null, false, false);
                    }
                }
            }
        }
    }

    public void debugKillAllProcess(PrintWriter pw) {
        if (mEnabled) {
            killAllProcess(pw);
        } else {
            pw.println("AwareAppMngSort disabled!");
        }
    }

    private void forceStopAllPackage(PrintWriter pw) {
        MemSortGroup sortGroup = getAppMemSortGroup(MEM_LEVEL0);
        if (sortGroup != null && sortGroup.mProcAllowStopList != null && this.mContext != null && pw != null) {
            for (AwareProcessBlockInfo infoBlock : sortGroup.mProcAllowStopList) {
                if (!(infoBlock == null || infoBlock.mProcessList == null)) {
                    for (AwareProcessInfo info : infoBlock.mProcessList) {
                        pw.println("forceStopAllPackage forceStopAllPackage pid:" + info.mPid + (ProcessCleaner.getInstance(this.mContext).forcestopApps(info.mPid) ? " success!" : " failed!"));
                    }
                }
            }
        }
    }

    public void debugForstopPackageAllProcess(PrintWriter pw) {
        if (mEnabled) {
            forceStopAllPackage(pw);
        } else {
            pw.println("AwareAppMngSort disabled!");
        }
    }

    public void dumpKillProcess(PrintWriter pw, int killPid, int groupId, boolean restart) {
        if (!mEnabled) {
            pw.println("AwareAppMngSort disabled!");
        } else if (this.mContext == null) {
            pw.println("  dumpKillProcess : mContext is null!");
        } else if (killPid <= 0) {
            pw.println("  dumpKillProcess : killPid is not a valid process id!");
        } else {
            ProcessCleaner cleaner = ProcessCleaner.getInstance(this.mContext);
            ProcessInfo info = ProcessInfoCollector.getInstance().getProcessInfo(killPid);
            if (info == null) {
                pw.println("  dumpKillProcess : can not find process:" + killPid + " in running processes!");
                return;
            }
            if (isGroupBeHigher(info.mPid, info.mUid, info.mProcessName, info.mPackageName, groupId)) {
                pw.println("  dumpKillProcess : can not kill process in higher group!");
            } else if (cleaner.killProcess(info.mPid, restart)) {
                pw.println("  dumpKillProcess : kill process:" + killPid + " successful!");
            } else {
                pw.println("  dumpKillProcess : kill process:" + killPid + " failed!");
            }
        }
    }

    private void dumpShortageSubClassRate(PrintWriter pw) {
        if (pw != null) {
            ShortageSubClassRate[] allRate = ShortageSubClassRate.values();
            int length = allRate.length;
            for (int i = MEM_LEVEL0; i < length; i += MSG_PRINT_BETA_LOG) {
                ShortageSubClassRate rate = allRate[i];
                pw.println("    sub" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
            }
        }
    }

    private void dumpForbidSubClassRate(PrintWriter pw) {
        if (pw != null) {
            ForbidSubClassRate[] allRate = ForbidSubClassRate.values();
            int length = allRate.length;
            for (int i = MEM_LEVEL0; i < length; i += MSG_PRINT_BETA_LOG) {
                ForbidSubClassRate rate = allRate[i];
                pw.println("    sub" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
            }
        }
    }

    private void dumpAllowStopSubClassRate(PrintWriter pw) {
        if (pw != null) {
            AllowStopSubClassRate[] allRate = AllowStopSubClassRate.values();
            int length = allRate.length;
            for (int i = MEM_LEVEL0; i < length; i += MSG_PRINT_BETA_LOG) {
                AllowStopSubClassRate rate = allRate[i];
                pw.println("    sub" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
            }
        }
    }

    public void dumpClassInfo(PrintWriter pw) {
        if (!mEnabled) {
            pw.println("AwareAppMngSort disabled!");
        } else if (pw != null) {
            ClassRate[] allRate = ClassRate.values();
            int length = allRate.length;
            for (int i = MEM_LEVEL0; i < length; i += MSG_PRINT_BETA_LOG) {
                ClassRate rate = allRate[i];
                pw.println("Class" + rate.ordinal() + ": value=" + rate.ordinal() + "," + rate.description());
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
}
