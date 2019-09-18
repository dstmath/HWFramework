package com.android.server.mtm.iaware.appmng.appstart.datamgr;

import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.HwAppStartupSettingFilter;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.ProcessRecord;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.comm.AppStartupUtil;
import com.android.server.pfw.autostartup.comm.XmlConst;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppStartStatusCacheExt;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.srms.AppStartupFeature;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AppStartupDataMgr {
    private static final String TAG = "AppStartupDataMgr";
    private boolean DEBUG_DATAMGR = false;
    private Set<String> mAllowedReceiverSet = new HashSet();
    private Set<String> mAutoAppPkgSet = new HashSet();
    private int[] mBigDataThreshold = new int[ThresholdType.values().length];
    private HsmDataCache mCacheMgr = new HsmDataCache();
    private Set<String> mPGForbidRestartSet = new HashSet();
    private Set<String> mPgCleanPkgSet = new HashSet();
    private Set<String> mSpExtCallerPkgSet = new ArraySet();
    private SystemUnremoveUidCache mSpecialUidSet = null;

    /* renamed from: com.android.server.mtm.iaware.appmng.appstart.datamgr.AppStartupDataMgr$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource = new int[AppMngConstant.AppStartSource.values().length];

        static {
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.THIRD_BROADCAST.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.SYSTEM_BROADCAST.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.JOB_SCHEDULE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.ACCOUNT_SYNC.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.SCHEDULE_RESTART.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.ALARM.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.BIND_SERVICE.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.THIRD_ACTIVITY.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    private enum ThresholdType {
        BETA,
        COMMERCIAL
    }

    public void initData(Context context) {
        AppStartupUtil.initCtsPkgList();
        this.mSpecialUidSet = SystemUnremoveUidCache.getInstance(context);
        this.mCacheMgr.loadStartupSettingCache();
        this.mCacheMgr.loadBootCache();
        loadAllowedReceivers();
        loadBigDataThreshold();
        loadPGForbidPkgs();
        loadAutoMngPkgs();
        loadSpExtCallerPkgs();
        synchronized (this.mPgCleanPkgSet) {
            this.mPgCleanPkgSet.clear();
        }
    }

    public void initSystemUidCache(Context context) {
        if (this.mSpecialUidSet != null) {
            this.mSpecialUidSet.loadSystemUid(context);
            if (this.DEBUG_DATAMGR) {
                AwareLog.i(TAG, "initSystemUidCache called");
            }
        }
    }

    public void initStartupSetting() {
        this.mCacheMgr.loadStartupSettingCache();
    }

    public void unInitData() {
        synchronized (this.mAllowedReceiverSet) {
            this.mAllowedReceiverSet.clear();
        }
    }

    public void updateCloudData() {
        loadAllowedReceivers();
        loadBigDataThreshold();
        loadPGForbidPkgs();
        loadAutoMngPkgs();
        loadSpExtCallerPkgs();
    }

    public int getBigdataThreshold(boolean beta) {
        return this.mBigDataThreshold[(beta ? ThresholdType.BETA : ThresholdType.COMMERCIAL).ordinal()];
    }

    private boolean isAllowedReceiver(String action) {
        boolean result;
        synchronized (this.mAllowedReceiverSet) {
            result = this.mAllowedReceiverSet.contains(action);
        }
        return result;
    }

    public boolean isSystemBaseApp(ApplicationInfo applicationInfo) {
        return UserHandle.getAppId(applicationInfo.uid) < 10000 || AppStartupUtil.isSystemUnRemovablePkg(applicationInfo);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0068, code lost:
        r18 = r0;
        r17 = r1;
        r4 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x009b, code lost:
        if (r17 == false) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009d, code lost:
        r19 = r4;
        r21 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00b3, code lost:
        if (isTargetAndCallerSameApp(r9, r25, r11, r12, r28, r29, r30) == false) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0027, code lost:
        r18 = r0;
        r17 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00b7, code lost:
        if (r8.DEBUG_DATAMGR == false) goto L_0x00e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b9, code lost:
        android.rms.iaware.AwareLog.i(TAG, "same app " + r9.packageName + ",caller:" + r11 + "," + r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00e2, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00e3, code lost:
        r19 = r4;
        r21 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00e7, code lost:
        if (r18 == false) goto L_0x00f6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00ed, code lost:
        if (isSpecialCaller(r11) != false) goto L_0x00f5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00f3, code lost:
        if (isSpExtCaller(r12) == false) goto L_0x00f6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00f5, code lost:
        return r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00fc, code lost:
        if (com.android.server.mtm.iaware.appmng.appstart.comm.AppStartupUtil.isCtsPackage(r9.packageName) != false) goto L_0x0106;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0102, code lost:
        if (isCtsCaller(r12) == false) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0105, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0106, code lost:
        return r19;
     */
    public int getDefaultAllowedRequestType(ApplicationInfo applicationInfo, String action, int callerPid, int callerUid, ProcessRecord callerApp, AppMngConstant.AppStartSource requestSource, boolean isAppStop, boolean isSysApp, boolean[] isBtMediaBrowserCaller, boolean[] notifyListenerCaller, int unPercetibleAlarm, boolean fromRecent, AwareAppStartStatusCacheExt statusCacheExt) {
        boolean needCheckSameApp;
        ApplicationInfo applicationInfo2 = applicationInfo;
        String str = action;
        int i = callerUid;
        ProcessRecord processRecord = callerApp;
        boolean[] zArr = isBtMediaBrowserCaller;
        boolean[] zArr2 = notifyListenerCaller;
        int i2 = unPercetibleAlarm;
        boolean needCheckSp = false;
        boolean needCheckSameApp2 = false;
        boolean gmsNeedCtrl = statusCacheExt.mGmsNeedCtrl;
        int i3 = 2;
        switch (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[requestSource.ordinal()]) {
            case 1:
            case 2:
                if (!isAllowedReceiver(str)) {
                    i3 = 2;
                    needCheckSameApp = true;
                    break;
                } else {
                    if (this.DEBUG_DATAMGR) {
                        AwareLog.i(TAG, "isAllowReceiver action=" + str + ", callerUid=" + i);
                    }
                    return 2;
                }
            case 3:
            case 4:
            case 5:
                break;
            case 6:
                if (!gmsNeedCtrl && i2 != 1) {
                    needCheckSp = true;
                }
                if (i2 != 2) {
                    boolean needCheckSp2 = needCheckSp;
                    boolean needCheckSameApp3 = false;
                    i3 = 2;
                    break;
                } else {
                    return 1;
                }
            case 7:
                boolean btCaller = AwareIntelligentRecg.getInstance().isBtMediaBrowserCaller(i, str);
                if (zArr != null && zArr.length > 0) {
                    zArr[0] = btCaller;
                }
                boolean notifyCaller = AwareIntelligentRecg.getInstance().isNotifyListenerCaller(i, str, processRecord);
                if (zArr2 != null && zArr2.length > 0) {
                    zArr2[0] = notifyCaller;
                }
                if (!btCaller && !notifyCaller) {
                    needCheckSp = true;
                }
                needCheckSameApp2 = true;
                break;
            case 8:
                if (!fromRecent) {
                    needCheckSameApp = true;
                    needCheckSp = true;
                    break;
                } else {
                    return 2;
                }
            default:
                needCheckSameApp = true;
                needCheckSp = true;
                break;
        }
    }

    private boolean isCtsCaller(ProcessRecord callerApp) {
        if (callerApp == null || callerApp.pkgList.isEmpty()) {
            return false;
        }
        return AppStartupUtil.isCtsPackage((String) callerApp.pkgList.keyAt(0));
    }

    private boolean isSpExtCaller(ProcessRecord callerApp) {
        if (callerApp == null || callerApp.pkgList == null || callerApp.pkgList.isEmpty()) {
            return false;
        }
        String pkg = (String) callerApp.pkgList.keyAt(0);
        if (pkg != null && this.mSpExtCallerPkgSet.contains(pkg)) {
            return true;
        }
        return false;
    }

    public boolean isWidgetExistPkg(String pkg) {
        return this.mCacheMgr.isWidgetExistPkg(pkg);
    }

    public boolean isBlindAssistPkg(String pkg) {
        return this.mCacheMgr.isBlindAssistPkg(pkg);
    }

    private boolean isTargetAndCallerSameApp(ApplicationInfo applicationInfo, int callerPid, int callerUid, ProcessRecord callerApp, AppMngConstant.AppStartSource requestSource, boolean isAppStop, boolean isSysApp) {
        if (applicationInfo.uid != callerUid) {
            return false;
        }
        boolean z = true;
        if (AwareAppAssociate.isDealAsPkgUid(callerUid)) {
            if (callerApp == null) {
                HwActivityManagerService hwAMS = HwActivityManagerService.self();
                if (hwAMS != null) {
                    callerApp = hwAMS.getProcessRecordLocked(callerPid);
                }
                if (this.DEBUG_DATAMGR) {
                    AwareLog.i(TAG, "isTargetAndCallerSameApp req=" + requestSource.getDesc() + ", target=" + applicationInfo.packageName + ", callerPid=" + callerPid + ", " + callerApp);
                }
            }
            if (callerApp != null) {
                z = callerApp.pkgList.containsKey(applicationInfo.packageName);
            }
            return z;
        } else if (isSysApp || !AppMngConstant.AppStartSource.START_SERVICE.equals(requestSource)) {
            return true;
        } else {
            return !isAppStop;
        }
    }

    public boolean isSpecialCaller(int callerUid) {
        int callerUid2 = UserHandle.getAppId(callerUid);
        if (callerUid2 < 10000) {
            if (this.DEBUG_DATAMGR) {
                AwareLog.d(TAG, "isSpecialCaller uid=" + callerUid2);
            }
            return true;
        } else if (this.mSpecialUidSet == null || !this.mSpecialUidSet.checkUidExist(callerUid2)) {
            return false;
        } else {
            if (this.DEBUG_DATAMGR) {
                AwareLog.d(TAG, "isSpecialCaller un-removable uid=" + callerUid2);
            }
            return true;
        }
    }

    private void loadAllowedReceivers() {
        ArrayList<String> whiteReceiverList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), XmlConst.PreciseIgnore.RECEIVER_ACTION_RECEIVER_ELEMENT_KEY);
        if (whiteReceiverList != null) {
            synchronized (this.mAllowedReceiverSet) {
                this.mAllowedReceiverSet.clear();
                this.mAllowedReceiverSet.addAll(whiteReceiverList);
            }
        }
    }

    private void loadPGForbidPkgs() {
        ArrayList<String> pgForbidRestart = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "PGForbidRestart");
        if (pgForbidRestart != null) {
            synchronized (this.mPGForbidRestartSet) {
                this.mPGForbidRestartSet.clear();
                this.mPGForbidRestartSet.addAll(pgForbidRestart);
            }
        }
    }

    private void loadAutoMngPkgs() {
        ArrayList<String> autoMng = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "automanage");
        if (autoMng != null) {
            synchronized (this.mAutoAppPkgSet) {
                this.mAutoAppPkgSet.clear();
                this.mAutoAppPkgSet.addAll(autoMng);
            }
        }
    }

    private void loadSpExtCallerPkgs() {
        ArrayList<String> spExtCaller = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "SpExtCaller");
        if (spExtCaller != null) {
            Set<String> spExtCache = new ArraySet<>();
            spExtCache.addAll(spExtCaller);
            this.mSpExtCallerPkgSet = spExtCache;
        }
    }

    private void loadBigDataThreshold() {
        ArrayList<String> thresholdList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), "bigdata_threshold");
        if (thresholdList != null) {
            int size = thresholdList.size();
            for (int i = 0; i < size; i++) {
                String thr = thresholdList.get(i);
                if (thr != null) {
                    String[] thrList = thr.split(":");
                    if (thrList.length == 2) {
                        int v = 0;
                        try {
                            v = Integer.parseInt(thrList[1]);
                        } catch (NumberFormatException e) {
                            AwareLog.e(TAG, "loadBigDataThreshold catch NumberFormatException");
                        }
                        String key = thrList[0];
                        if ("beta".equals(key)) {
                            this.mBigDataThreshold[ThresholdType.BETA.ordinal()] = v;
                        } else if ("commercial".equals(key)) {
                            this.mBigDataThreshold[ThresholdType.COMMERCIAL.ordinal()] = v;
                        }
                    }
                }
            }
        }
    }

    public boolean updateWidgetList(Set<String> pkgList) {
        if (pkgList == null) {
            return false;
        }
        if (this.DEBUG_DATAMGR) {
            AwareLog.i(TAG, "updateWidgetList pkgList=" + pkgList);
        }
        this.mCacheMgr.updateWidgetPkgList(pkgList);
        return true;
    }

    public void updateWidgetUpdateTime(String pkgName) {
        if (pkgName != null) {
            this.mCacheMgr.updateWidgetUpdateTime(pkgName);
        }
    }

    public int getWidgetCnt() {
        return this.mCacheMgr.getWidgetExistPkgCnt();
    }

    public long getWidgetExistPkgUpdateTime(String pkgName) {
        return this.mCacheMgr.getWidgetExistPkgUpdateTime(pkgName);
    }

    public void updateBlind(Set<String> pkgSet) {
        this.mCacheMgr.updateBlindPkg(pkgSet);
    }

    public void flushStartupSettingToDisk() {
        this.mCacheMgr.flushStartupSettingToDisk();
    }

    public void flushBootCacheDataToDisk() {
        this.mCacheMgr.flushBootCacheDataToDisk();
    }

    public HwAppStartupSetting getAppStartupSetting(String pkgName) {
        return this.mCacheMgr.getAppStartupSetting(pkgName);
    }

    public List<HwAppStartupSetting> retrieveAppStartupSettings(List<String> pkgList, HwAppStartupSettingFilter filter) {
        return this.mCacheMgr.retrieveStartupSettings(pkgList, filter);
    }

    public boolean updateAppStartupSettings(List<HwAppStartupSetting> settingList, boolean clearFirst) {
        return this.mCacheMgr.updateStartupSettings(settingList, clearFirst);
    }

    public boolean removeAppStartupSetting(String pkgName) {
        return this.mCacheMgr.removeStartupSetting(pkgName);
    }

    public boolean isPGForbidRestart(String pkg) {
        boolean contains;
        synchronized (this.mPGForbidRestartSet) {
            contains = this.mPGForbidRestartSet.contains(pkg);
        }
        return contains;
    }

    public void addPgCleanApp(String pkg) {
        synchronized (this.mPgCleanPkgSet) {
            this.mPgCleanPkgSet.add(pkg);
        }
    }

    public void removePgCleanApp(String pkg) {
        synchronized (this.mPgCleanPkgSet) {
            if (this.mPgCleanPkgSet.contains(pkg)) {
                this.mPgCleanPkgSet.remove(pkg);
            }
        }
    }

    public boolean isPgCleanApp(String pkg) {
        boolean contains;
        synchronized (this.mPgCleanPkgSet) {
            contains = this.mPgCleanPkgSet.contains(pkg);
        }
        return contains;
    }

    public boolean isAutoMngPkg(String pkg) {
        boolean contains;
        synchronized (this.mAutoAppPkgSet) {
            contains = this.mAutoAppPkgSet.contains(pkg);
        }
        return contains;
    }

    public void dump(PrintWriter pw, String[] args) {
        if (args != null && pw != null) {
            if (args.length < 3) {
                pw.println("Bad command");
            } else if (args.length == 3) {
                String cmd = args[2];
                if ("cache".equals(cmd)) {
                    if (AppStartupFeature.isAppStartupEnabled()) {
                        synchronized (this.mAllowedReceiverSet) {
                            pw.println("Receiver(" + this.mAllowedReceiverSet.size() + "):");
                            Iterator<String> it = this.mAllowedReceiverSet.iterator();
                            while (it.hasNext()) {
                                pw.println("  " + it.next());
                            }
                        }
                        synchronized (this.mPGForbidRestartSet) {
                            pw.println("PGForbidRestart(" + this.mPGForbidRestartSet.size() + "):");
                            Iterator<String> it2 = this.mPGForbidRestartSet.iterator();
                            while (it2.hasNext()) {
                                pw.println("  " + it2.next());
                            }
                        }
                        if (this.mSpecialUidSet != null) {
                            pw.println("SystemUid:");
                            pw.println("  " + this.mSpecialUidSet.toString());
                        }
                        synchronized (this.mAutoAppPkgSet) {
                            pw.println("AutoAppPkgSet(" + this.mAutoAppPkgSet.size() + "):");
                            Iterator<String> it3 = this.mAutoAppPkgSet.iterator();
                            while (it3.hasNext()) {
                                pw.println("  " + it3.next());
                            }
                        }
                        Iterator<String> it4 = this.mSpExtCallerPkgSet.iterator();
                        while (it4.hasNext()) {
                            pw.println("  " + it4.next());
                        }
                    }
                    pw.println(AppStartupUtil.getDumpCtsPackages());
                    pw.println(this.mCacheMgr.toString());
                } else if ("info".equals(cmd)) {
                    pw.println("BigdataThreshold: beta=" + this.mBigDataThreshold[ThresholdType.BETA.ordinal()] + ", commercial=" + this.mBigDataThreshold[ThresholdType.COMMERCIAL.ordinal()]);
                } else {
                    pw.println("Bad command");
                }
            } else {
                String cmd2 = args[2];
                String param = args[3];
                if ("log".equals(cmd2)) {
                    if ("0".equals(param)) {
                        this.DEBUG_DATAMGR = false;
                    } else if ("1".equals(param)) {
                        this.DEBUG_DATAMGR = true;
                    } else {
                        pw.println("Bad command");
                    }
                }
            }
        }
    }
}
