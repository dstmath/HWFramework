package com.android.server.mtm.iaware.appmng.appstart.datamgr;

import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.HwAppStartupSettingFilter;
import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.app.mtm.iaware.appmng.AppMngConstant.AppStartSource;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.ProcessRecord;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.comm.AppStartupUtil;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.srms.AppStartupFeature;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppStartupDataMgr {
    private static final /* synthetic */ int[] -android-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues = null;
    private static final String TAG = "AppStartupDataMgr";
    private boolean DEBUG_DATAMGR = false;
    private Set<String> mAllowedReceiverSet = new HashSet();
    private Set<String> mAutoAppPkgSet = new HashSet();
    private int[] mBigDataThreshold = new int[ThresholdType.values().length];
    private HsmDataCache mCacheMgr = new HsmDataCache();
    private Set<String> mPGForbidRestartSet = new HashSet();
    private Set<String> mPgCleanPkgSet = new HashSet();
    private SystemUnremoveUidCache mSpecialUidSet = null;

    private enum ThresholdType {
        BETA,
        COMMERCIAL
    }

    private static /* synthetic */ int[] -getandroid-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues() {
        if (-android-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues != null) {
            return -android-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues;
        }
        int[] iArr = new int[AppStartSource.values().length];
        try {
            iArr[AppStartSource.ACCOUNT_SYNC.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppStartSource.ALARM.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppStartSource.BIND_SERVICE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppStartSource.JOB_SCHEDULE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppStartSource.PROVIDER.ordinal()] = 9;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppStartSource.SCHEDULE_RESTART.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[AppStartSource.START_SERVICE.ordinal()] = 10;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[AppStartSource.SYSTEM_BROADCAST.ordinal()] = 6;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[AppStartSource.THIRD_ACTIVITY.ordinal()] = 7;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[AppStartSource.THIRD_BROADCAST.ordinal()] = 8;
        } catch (NoSuchFieldError e10) {
        }
        -android-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues = iArr;
        return iArr;
    }

    public void initData(Context context) {
        this.mSpecialUidSet = SystemUnremoveUidCache.getInstance(context);
        this.mCacheMgr.loadStartupSettingCache();
        this.mCacheMgr.loadBootCache();
        loadAllowedReceivers();
        loadBigDataThreshold();
        loadPGForbidPkgs();
        loadAutoMngPkgs();
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
        if (UserHandle.getAppId(applicationInfo.uid) >= 10000) {
            return AppStartupUtil.isSystemUnRemovablePkg(applicationInfo);
        }
        return true;
    }

    public int getDefaultAllowedRequestType(ApplicationInfo applicationInfo, String action, int callerPid, int callerUid, ProcessRecord callerApp, AppStartSource requestSource, boolean isAppStop, boolean isSysApp, boolean[] isBtMediaBrowserCaller, boolean[] notifyListenerCaller, int unPercetibleAlarm, boolean fromRecent) {
        boolean needCheckSp = false;
        boolean needCheckSameApp = false;
        switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppStartSourceSwitchesValues()[requestSource.ordinal()]) {
            case 1:
            case 4:
            case 5:
                break;
            case 2:
                needCheckSp = true;
                if (unPercetibleAlarm == 2) {
                    return 1;
                }
                break;
            case 3:
                boolean btCaller = AwareIntelligentRecg.getInstance().isBtMediaBrowserCaller(callerUid, action);
                if (isBtMediaBrowserCaller != null && isBtMediaBrowserCaller.length > 0) {
                    isBtMediaBrowserCaller[0] = btCaller;
                }
                boolean notifyCaller = AwareIntelligentRecg.getInstance().isNotifyListenerCaller(callerUid, action, callerApp);
                if (notifyListenerCaller != null && notifyListenerCaller.length > 0) {
                    notifyListenerCaller[0] = notifyCaller;
                }
                if (!(btCaller || (notifyCaller ^ 1) == 0)) {
                    needCheckSp = true;
                }
                needCheckSameApp = true;
                break;
            case 6:
            case 8:
                if (!isAllowedReceiver(action)) {
                    needCheckSameApp = true;
                    break;
                }
                if (this.DEBUG_DATAMGR) {
                    AwareLog.i(TAG, "isAllowReceiver action=" + action + ", callerUid=" + callerUid);
                }
                return 2;
            case 7:
                if (!fromRecent) {
                    needCheckSameApp = true;
                    needCheckSp = true;
                    break;
                }
                return 2;
            default:
                needCheckSameApp = true;
                needCheckSp = true;
                break;
        }
        if (needCheckSameApp && isTargetAndCallerSameApp(applicationInfo, callerPid, callerUid, callerApp, requestSource, isAppStop, isSysApp)) {
            if (this.DEBUG_DATAMGR) {
                AwareLog.i(TAG, "same app " + applicationInfo.packageName + ",caller:" + callerUid + "," + callerApp);
            }
            return 1;
        } else if (needCheckSp && isSpecialCaller(callerUid)) {
            return 2;
        } else {
            if (AppStartupUtil.isCtsPackage(applicationInfo.packageName) || isCtsCaller(callerApp)) {
                return 2;
            }
            return 0;
        }
    }

    private boolean isCtsCaller(ProcessRecord callerApp) {
        if (callerApp == null || callerApp.pkgList.isEmpty()) {
            return false;
        }
        return AppStartupUtil.isCtsPackage((String) callerApp.pkgList.keyAt(0));
    }

    public boolean isWidgetExistPkg(String pkg) {
        return this.mCacheMgr.isWidgetExistPkg(pkg);
    }

    public boolean isBlindAssistPkg(String pkg) {
        return this.mCacheMgr.isBlindAssistPkg(pkg);
    }

    private boolean isTargetAndCallerSameApp(ApplicationInfo applicationInfo, int callerPid, int callerUid, ProcessRecord callerApp, AppStartSource requestSource, boolean isAppStop, boolean isSysApp) {
        boolean z = true;
        if (applicationInfo.uid != callerUid) {
            return false;
        }
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
        } else if (isSysApp || !AppStartSource.START_SERVICE.equals(requestSource)) {
            return true;
        } else {
            return isAppStop ^ 1;
        }
    }

    public boolean isSpecialCaller(int callerUid) {
        callerUid = UserHandle.getAppId(callerUid);
        if (callerUid < 10000) {
            if (this.DEBUG_DATAMGR) {
                AwareLog.d(TAG, "isSpecialCaller uid=" + callerUid);
            }
            return true;
        } else if (this.mSpecialUidSet == null || !this.mSpecialUidSet.checkUidExist(callerUid)) {
            return false;
        } else {
            if (this.DEBUG_DATAMGR) {
                AwareLog.d(TAG, "isSpecialCaller un-removable uid=" + callerUid);
            }
            return true;
        }
    }

    private void loadAllowedReceivers() {
        ArrayList<String> whiteReceiverList = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), PreciseIgnore.RECEIVER_ACTION_RECEIVER_ELEMENT_KEY);
        if (whiteReceiverList != null) {
            synchronized (this.mAllowedReceiverSet) {
                this.mAllowedReceiverSet.clear();
                this.mAllowedReceiverSet.addAll(whiteReceiverList);
            }
        }
    }

    private void loadPGForbidPkgs() {
        ArrayList<String> pgForbidRestart = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), "PGForbidRestart");
        if (pgForbidRestart != null) {
            synchronized (this.mPGForbidRestartSet) {
                this.mPGForbidRestartSet.clear();
                this.mPGForbidRestartSet.addAll(pgForbidRestart);
            }
        }
    }

    private void loadAutoMngPkgs() {
        ArrayList<String> autoMng = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), "automanage");
        if (autoMng != null) {
            synchronized (this.mAutoAppPkgSet) {
                this.mAutoAppPkgSet.clear();
                this.mAutoAppPkgSet.addAll(autoMng);
            }
        }
    }

    private void loadBigDataThreshold() {
        ArrayList<String> thresholdList = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_START.getDesc(), "bigdata_threshold");
        if (thresholdList != null) {
            int size = thresholdList.size();
            for (int i = 0; i < size; i++) {
                String thr = (String) thresholdList.get(i);
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
            String strBadCmd = "Bad command";
            String strOn = "1";
            String strOff = "0";
            String cmd;
            if (args.length < 3) {
                pw.println("Bad command");
            } else if (args.length == 3) {
                cmd = args[2];
                if ("cache".equals(cmd)) {
                    if (AppStartupFeature.isAppStartupEnabled()) {
                        PrintWriter printWriter;
                        synchronized (this.mAllowedReceiverSet) {
                            pw.println("Receiver(" + this.mAllowedReceiverSet.size() + "):");
                            for (String receiver : this.mAllowedReceiverSet) {
                                printWriter = pw;
                                printWriter.println("  " + receiver);
                            }
                        }
                        synchronized (this.mPGForbidRestartSet) {
                            pw.println("PGForbidRestart(" + this.mPGForbidRestartSet.size() + "):");
                            for (String pgForbidPkg : this.mPGForbidRestartSet) {
                                printWriter = pw;
                                printWriter.println("  " + pgForbidPkg);
                            }
                        }
                        if (this.mSpecialUidSet != null) {
                            pw.println("SystemUid:");
                            pw.println("  " + this.mSpecialUidSet.toString());
                        }
                        synchronized (this.mAutoAppPkgSet) {
                            pw.println("AutoAppPkgSet(" + this.mAutoAppPkgSet.size() + "):");
                            for (String pkg : this.mAutoAppPkgSet) {
                                printWriter = pw;
                                printWriter.println("  " + pkg);
                            }
                        }
                    }
                    pw.println(this.mCacheMgr.toString());
                } else if ("info".equals(cmd)) {
                    pw.println("BigdataThreshold: beta=" + this.mBigDataThreshold[ThresholdType.BETA.ordinal()] + ", commercial=" + this.mBigDataThreshold[ThresholdType.COMMERCIAL.ordinal()]);
                } else {
                    pw.println("Bad command");
                }
            } else {
                cmd = args[2];
                String param = args[3];
                if ("log".equals(cmd)) {
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
