package com.android.server.mtm.iaware.appmng.appstart.datamgr;

import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.HwAppStartupSettingFilter;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessState;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.comm.AppStartupUtil;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.srms.AppStartupFeature;
import com.huawei.android.os.UserHandleEx;
import com.huawei.server.wm.WindowProcessControllerEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AppStartupDataMgr {
    private static final int ARGS_CMD = 2;
    private static final int ARGS_PARAM = 3;
    private static final String BAD_COMMAND_INFO = "Bad command";
    private static final String HWPUSH_FORCESTART_EXTRA = "force_start";
    public static final String HWPUSH_PKGNAME = "android";
    private static final String HWPUSH_PROCESSNAME = "com.huawei.android.pushagent.PushService";
    private static final String LOG_OFF_PATAM = "0";
    private static final String LOG_ON_PATAM = "1";
    private static final int MIN_CMD_LEN = 3;
    private static final String TAG = "AppStartupDataMgr";
    private final Set<String> mAllowedReceiverSet = new HashSet();
    private final Set<String> mAutoAppPkgSet = new HashSet();
    private int[] mBigDataThreshold = new int[ThresholdType.values().length];
    private HsmDataCache mCacheMgr = new HsmDataCache();
    private boolean mDebugDataMgr = false;
    private final Set<String> mPgCleanPkgSet = new HashSet();
    private final Set<String> mPgForbidRestartSet = new HashSet();
    private Set<String> mSpExtCallerPkgSet = new ArraySet();
    private SystemUnremoveUidCache mSpecialUidSet = null;

    /* access modifiers changed from: private */
    public enum ThresholdType {
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
        loadPgForbidPkgs();
        loadAutoMngPkgs();
        loadSpExtCallerPkgs();
        synchronized (this.mPgCleanPkgSet) {
            this.mPgCleanPkgSet.clear();
        }
    }

    public void initSystemUidCache(Context context) {
        SystemUnremoveUidCache systemUnremoveUidCache = this.mSpecialUidSet;
        if (systemUnremoveUidCache != null) {
            systemUnremoveUidCache.loadSystemUid(context);
            if (this.mDebugDataMgr) {
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

    public void updateAppMngConfig() {
        loadAllowedReceivers();
        loadBigDataThreshold();
        loadPgForbidPkgs();
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
        if (applicationInfo == null) {
            return false;
        }
        if (UserHandleEx.getAppId(applicationInfo.uid) < 10000 || AppStartupUtil.isSystemUnRemovablePkg(applicationInfo)) {
            return true;
        }
        return false;
    }

    private boolean isAppStartupValid(AppStartupInfo appStartupInfo) {
        if (appStartupInfo == null || appStartupInfo.applicationInfo == null || appStartupInfo.statusCacheExt == null) {
            return false;
        }
        return true;
    }

    public int getDefaultAllowedRequestType(AppStartupInfo appStartupInfo) {
        if (!isAppStartupValid(appStartupInfo)) {
            return 4;
        }
        boolean needCheckSp = false;
        boolean needCheckSameApp = false;
        boolean gmsNeedCtrl = appStartupInfo.statusCacheExt.gmsNeedCtrl;
        switch (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[appStartupInfo.requestSource.ordinal()]) {
            case 1:
            case 2:
                if (!isAllowedReceiver(appStartupInfo.action)) {
                    needCheckSameApp = true;
                    break;
                } else {
                    return 2;
                }
            case 3:
            case 4:
            case 5:
                break;
            case 6:
                if (!gmsNeedCtrl && appStartupInfo.unPercetibleAlarm != 1) {
                    needCheckSp = true;
                }
                if (appStartupInfo.unPercetibleAlarm == 2) {
                    return 1;
                }
                break;
            case 7:
                needCheckSp = needCheckSpOfBindService(appStartupInfo, false);
                needCheckSameApp = true;
                break;
            case 8:
                if (!appStartupInfo.fromRecent) {
                    needCheckSameApp = true;
                    needCheckSp = true;
                    break;
                } else {
                    return 2;
                }
            case AwareProcessState.STATE_FOREGROUND /* 9 */:
                needCheckSameApp = true;
                needCheckSp = checkHwPushCall(appStartupInfo);
                break;
            default:
                needCheckSameApp = true;
                needCheckSp = true;
                break;
        }
        return getAllowedRequestType(appStartupInfo, needCheckSameApp, needCheckSp);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.mtm.iaware.appmng.appstart.datamgr.AppStartupDataMgr$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
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
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppStartSource[AppMngConstant.AppStartSource.START_SERVICE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
        }
    }

    private int getAllowedRequestType(AppStartupInfo appStartupInfo, boolean needCheckSameApp, boolean needCheckSp) {
        if (!needCheckSameApp || !isTargetAndCallerSameApp(appStartupInfo)) {
            if ((!needCheckSp || (!isSpecialCaller(appStartupInfo.callerUid) && !isSpExtCaller(appStartupInfo.callerApp))) && !AppStartupUtil.isCtsPackage(appStartupInfo.applicationInfo.packageName) && !isCtsCaller(appStartupInfo.callerApp)) {
                return 0;
            }
            return 2;
        } else if (!this.mDebugDataMgr) {
            return 1;
        } else {
            AwareLog.i(TAG, "same app " + appStartupInfo.applicationInfo.packageName + ",caller:" + appStartupInfo.callerUid + "," + appStartupInfo.callerApp);
            return 1;
        }
    }

    private boolean needCheckSpOfBindService(AppStartupInfo appStartupInfo, boolean needCheckSpDefault) {
        appStartupInfo.isCallerBtMediaBrowser = AwareIntelligentRecg.getInstance().isBtMediaBrowserCaller(appStartupInfo.callerUid, appStartupInfo.action);
        appStartupInfo.isCallerNotifyListener = AwareIntelligentRecg.getInstance().isNotifyListenerCaller(appStartupInfo.callerUid, appStartupInfo.action, appStartupInfo.callerApp);
        if (appStartupInfo.isCallerBtMediaBrowser || appStartupInfo.isCallerNotifyListener) {
            return needCheckSpDefault;
        }
        return checkHwPushCall(appStartupInfo);
    }

    private boolean isCtsCaller(WindowProcessControllerEx callerApp) {
        if (callerApp == null || callerApp.isWindowProcessControllerNull() || callerApp.getPkgList() == null || callerApp.getPkgList().isEmpty()) {
            return false;
        }
        return AppStartupUtil.isCtsPackage((String) callerApp.getPkgList().valueAt(0));
    }

    private boolean isSpExtCaller(WindowProcessControllerEx callerApp) {
        if (callerApp == null || callerApp.isWindowProcessControllerNull() || callerApp.getPkgList() == null || callerApp.getPkgList().isEmpty()) {
            return false;
        }
        return this.mSpExtCallerPkgSet.contains(callerApp.getPkgList().valueAt(0));
    }

    public boolean isHwpushCaller(int callerUid, WindowProcessControllerEx callerApp) {
        if (callerUid != 1000 || callerApp == null || callerApp.isWindowProcessControllerNull() || callerApp.getPkgList() == null || callerApp.getPkgList().isEmpty() || !HWPUSH_PKGNAME.equals(callerApp.getPkgList().valueAt(0)) || !HWPUSH_PROCESSNAME.equals(callerApp.getName())) {
            return false;
        }
        return true;
    }

    private boolean checkHwPushCall(AppStartupInfo appStartupInfo) {
        if (!appStartupInfo.statusCacheExt.hwPush) {
            return true;
        }
        Intent intent = appStartupInfo.intent;
        if (appStartupInfo.policyType != 0 || intent == null || !intent.getBooleanExtra(HWPUSH_FORCESTART_EXTRA, false)) {
            return false;
        }
        return true;
    }

    public boolean isWidgetExistPkg(String pkg) {
        return this.mCacheMgr.isWidgetExistPkg(pkg);
    }

    public boolean isBlindAssistPkg(String pkg) {
        return this.mCacheMgr.isBlindAssistPkg(pkg);
    }

    private boolean isTargetAndCallerSameApp(AppStartupInfo appStartupInfo) {
        if (appStartupInfo.applicationInfo.uid != appStartupInfo.callerUid) {
            return false;
        }
        WindowProcessControllerEx callerApp = appStartupInfo.callerApp;
        if (AwareAppAssociate.isDealAsPkgUid(appStartupInfo.callerUid)) {
            if (callerApp == null || callerApp.isWindowProcessControllerNull()) {
                HwActivityManagerService hwAms = HwActivityManagerService.self();
                if (hwAms != null) {
                    callerApp = hwAms.getWindowProcessControllerEx(appStartupInfo.callerPid);
                }
                if (this.mDebugDataMgr) {
                    AwareLog.i(TAG, "isTargetAndCallerSameApp req=" + appStartupInfo.requestSource.getDesc() + ", target=" + appStartupInfo.applicationInfo.packageName + ", callerPid=" + appStartupInfo.callerPid + ", " + callerApp);
                }
            }
            if (callerApp == null || callerApp.isWindowProcessControllerNull() || callerApp.getPkgList() == null) {
                return true;
            }
            return callerApp.getPkgList().contains(appStartupInfo.applicationInfo.packageName);
        } else if (appStartupInfo.isSysApp || !AppMngConstant.AppStartSource.START_SERVICE.equals(appStartupInfo.requestSource)) {
            return true;
        } else {
            return !appStartupInfo.isAppStop;
        }
    }

    public boolean isSpecialCaller(int callerUid) {
        int callerUid2 = UserHandleEx.getAppId(callerUid);
        if (callerUid2 < 10000) {
            if (this.mDebugDataMgr) {
                AwareLog.d(TAG, "isSpecialCaller uid=" + callerUid2);
            }
            return true;
        }
        SystemUnremoveUidCache systemUnremoveUidCache = this.mSpecialUidSet;
        if (systemUnremoveUidCache == null || !systemUnremoveUidCache.checkUidExist(callerUid2)) {
            return false;
        }
        if (this.mDebugDataMgr) {
            AwareLog.d(TAG, "isSpecialCaller un-removable uid=" + callerUid2);
        }
        return true;
    }

    private ArrayList<String> loadAppStartRawConfig(String configName) {
        return DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), configName);
    }

    private void loadAllowedReceivers() {
        ArrayList<String> whiteReceiverList = loadAppStartRawConfig("receiver");
        if (whiteReceiverList != null) {
            synchronized (this.mAllowedReceiverSet) {
                this.mAllowedReceiverSet.clear();
                this.mAllowedReceiverSet.addAll(whiteReceiverList);
            }
        }
    }

    private void loadPgForbidPkgs() {
        ArrayList<String> pgForbidRestart = loadAppStartRawConfig("PGForbidRestart");
        if (pgForbidRestart != null) {
            synchronized (this.mPgForbidRestartSet) {
                this.mPgForbidRestartSet.clear();
                this.mPgForbidRestartSet.addAll(pgForbidRestart);
            }
        }
    }

    private void loadAutoMngPkgs() {
        ArrayList<String> autoMng = loadAppStartRawConfig("automanage");
        if (autoMng != null) {
            synchronized (this.mAutoAppPkgSet) {
                this.mAutoAppPkgSet.clear();
                this.mAutoAppPkgSet.addAll(autoMng);
            }
        }
    }

    private void loadSpExtCallerPkgs() {
        Collection<? extends String> spExtCaller = loadAppStartRawConfig("SpExtCaller");
        if (spExtCaller != null) {
            Set<String> spExtCache = new ArraySet<>();
            spExtCache.addAll(spExtCaller);
            this.mSpExtCallerPkgSet = spExtCache;
        }
    }

    private void loadBigDataThreshold() {
        ArrayList<String> thresholdList = loadAppStartRawConfig("bigdata_threshold");
        if (thresholdList != null) {
            int thresholdListSize = thresholdList.size();
            for (int i = 0; i < thresholdListSize; i++) {
                String thr = thresholdList.get(i);
                if (thr != null) {
                    String[] thrList = thr.split(":");
                    if (thrList.length == 2) {
                        int num = 0;
                        try {
                            num = Integer.parseInt(thrList[1]);
                        } catch (NumberFormatException e) {
                            AwareLog.e(TAG, "loadBigDataThreshold catch NumberFormatException");
                        }
                        String key = thrList[0];
                        if ("beta".equals(key)) {
                            this.mBigDataThreshold[ThresholdType.BETA.ordinal()] = num;
                        } else if ("commercial".equals(key)) {
                            this.mBigDataThreshold[ThresholdType.COMMERCIAL.ordinal()] = num;
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
        if (this.mDebugDataMgr) {
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
        synchronized (this.mPgForbidRestartSet) {
            contains = this.mPgForbidRestartSet.contains(pkg);
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
        if (!isInvalidDump(pw, args)) {
            if (args.length == 3) {
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
                        synchronized (this.mPgForbidRestartSet) {
                            pw.println("PGForbidRestart(" + this.mPgForbidRestartSet.size() + "):");
                            Iterator<String> it2 = this.mPgForbidRestartSet.iterator();
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
                    pw.println(BAD_COMMAND_INFO);
                }
            } else {
                dumpEx(pw, args);
            }
        }
    }

    private boolean isInvalidDump(PrintWriter pw, String[] args) {
        if (args == null || pw == null) {
            return true;
        }
        if (args.length >= 3) {
            return false;
        }
        pw.println(BAD_COMMAND_INFO);
        return true;
    }

    private void dumpEx(PrintWriter pw, String[] args) {
        String cmd = args[2];
        String param = args[3];
        if (!"log".equals(cmd)) {
            return;
        }
        if (LOG_OFF_PATAM.equals(param)) {
            this.mDebugDataMgr = false;
        } else if (LOG_ON_PATAM.equals(param)) {
            this.mDebugDataMgr = true;
        } else {
            pw.println(BAD_COMMAND_INFO);
        }
    }
}
