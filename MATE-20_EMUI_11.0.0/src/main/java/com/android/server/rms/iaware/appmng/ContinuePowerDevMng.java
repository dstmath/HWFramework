package com.android.server.rms.iaware.appmng;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.iawareperf.UniPerf;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.feature.AppQuickStartFeature;
import com.android.server.rms.iaware.memory.utils.BigMemoryConstant;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.content.pm.IPackageManagerEx;
import com.huawei.android.content.pm.IPackageManagerExt;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ContinuePowerDevMng extends DefaultContinuePowerDevMng {
    private static final long BOOT_COMPLETED_DELAY_TIME = 5000;
    private static final int DEFAULT_PRELOAD_TYPE = 0;
    private static final int DYNAMIC_APP_SIZE = 3;
    private static final int DYNAMIC_PRELOAD = 1;
    private static final boolean IS_TV_PERSIST = SystemPropertiesEx.getBoolean("ro.config.preload_persist", false);
    private static final int MSG_BOOT_PRELOAD = 4;
    private static final int MSG_KEY_DOWN_BOOST = 3;
    private static final int MSG_KILL_PRE_PROCESS = 2;
    private static final int MSG_PRELOAD_APPLICATION = 1;
    private static final int MSG_PRELOAD_APPLICATION_FOR_LAUNCHER = 5;
    private static final int PERMANENT_PRELOAD = 2;
    private static final int PERSISTENT_MASK = 9;
    private static final String TAG = "ContinuePowerDevMng";
    private static final Object sInstLocker = new Object();
    private static ContinuePowerDevMng sInstance = null;
    private final List<String> mAppClearedList = new ArrayList();
    private Context mContext = null;
    private final LinkedList<PreloadAppInfo> mDynamicAppList = new LinkedList<>();
    private boolean mGetLauncherUid = false;
    private HandlerThread mHandlerThread = null;
    private String mLastPackageName = null;
    private int mLastUid = 0;
    private int mLastUserId = 0;
    private int mLauncherUid = -1;
    private final Object mLocker = new Object();
    private final Set<String> mPermanentPreloadPkgs = new ArraySet();
    private final List<HwProcessInfo> mPkgList = new ArrayList();
    private Handler mThreadhandler = null;

    public static ContinuePowerDevMng getInstance() {
        ContinuePowerDevMng continuePowerDevMng;
        synchronized (sInstLocker) {
            if (sInstance == null) {
                sInstance = new ContinuePowerDevMng();
            }
            continuePowerDevMng = sInstance;
        }
        return continuePowerDevMng;
    }

    private ContinuePowerDevMng() {
    }

    /* access modifiers changed from: private */
    public static class PreloadAppInfo {
        public String packageName;
        public int uid = -1;
        public int userId = -1;

        public PreloadAppInfo(String pkg, int userId2, int uid2) {
            this.packageName = pkg;
            this.userId = userId2;
            this.uid = uid2;
        }

        public int hashCode() {
            String str = this.packageName;
            return (str == null ? 0 : str.hashCode()) + (this.userId * 100000) + this.uid;
        }

        public boolean equals(Object obj) {
            String str;
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof PreloadAppInfo)) {
                return false;
            }
            PreloadAppInfo other = (PreloadAppInfo) obj;
            String str2 = this.packageName;
            if (str2 == null || (str = other.packageName) == null) {
                return false;
            }
            if (this.userId == other.userId && this.uid == other.uid && str2.equals(str)) {
                return true;
            }
            return false;
        }
    }

    public void init(HandlerThread handlerThread, Context context) {
        this.mHandlerThread = handlerThread;
        this.mContext = context;
    }

    public void initHandler() {
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null) {
            initThreadhandler(handlerThread);
        }
    }

    private void initThreadhandler(HandlerThread thread) {
        if (thread != null && this.mThreadhandler == null) {
            this.mThreadhandler = new Handler(thread.getLooper()) {
                /* class com.android.server.rms.iaware.appmng.ContinuePowerDevMng.AnonymousClass1 */

                /* JADX INFO: Multiple debug info for r0v1 java.lang.Object: [D('msgObj' java.lang.Object), D('obj' java.lang.Object)] */
                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    if (msg != null) {
                        int i = msg.what;
                        if (i == 1) {
                            Object obj = msg.obj;
                            AwareLog.d(ContinuePowerDevMng.TAG, "TouchDownPreloadApp start");
                            if (obj instanceof HwProcessInfo) {
                                HwProcessInfo pi = (HwProcessInfo) obj;
                                boolean ret = ContinuePowerDevMng.this.handlePreloadAppForLauncher(pi);
                                AwareLog.d(ContinuePowerDevMng.TAG, "TouchDownPreloadApp " + pi.packageName + " preloadType:" + pi.preloadType + " result:" + ret);
                            }
                        } else if (i != 5) {
                            ContinuePowerDevMng.this.handleMessageEx(msg);
                        } else {
                            Object msgObj = msg.obj;
                            AwareLog.d(ContinuePowerDevMng.TAG, "TouchDownPreloadApp start");
                            if (msgObj instanceof HwProcessInfo) {
                                HwProcessInfo pi2 = (HwProcessInfo) msgObj;
                                if (!ContinuePowerDevMng.this.checkPermission(Binder.getCallingUid(), pi2.packageName, pi2.userId)) {
                                    AwareLog.d(ContinuePowerDevMng.TAG, "TouchDownPreloadApp CallingUid not Launcher, packageName: " + pi2.packageName + ", userid: " + pi2.userId);
                                    return;
                                }
                                boolean ret2 = ContinuePowerDevMng.this.handlePreloadAppForLauncher(pi2);
                                AwareLog.d(ContinuePowerDevMng.TAG, "TouchDownPreloadApp " + pi2.packageName + " preloadType:" + pi2.preloadType + " result:" + ret2);
                            }
                        }
                    }
                }
            };
            if (AppQuickStartFeature.isTouchDownPreloadEnable()) {
                this.mThreadhandler.sendMessageDelayed(this.mThreadhandler.obtainMessage(4), BOOT_COMPLETED_DELAY_TIME);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessageEx(Message msg) {
        if (msg != null) {
            int i = msg.what;
            if (i == 2) {
                Object objMsg = msg.obj;
                if (objMsg instanceof HwProcessInfo) {
                    handleKillpreApplication((HwProcessInfo) objMsg);
                }
            } else if (i == 3) {
                AwareLog.d(TAG, "MSG_KEY_DOWN_BOOST.");
                UniPerf.getInstance().uniPerfEvent(13200, "", new int[0]);
            } else if (i == 4) {
                AwareLog.d(TAG, "MSG_BOOT_PRELOAD.");
                tryPreLoadPermanentApplication();
            }
        }
    }

    private boolean checkPreloadType(int preloadType) {
        if (preloadType == 0 || preloadType == 1) {
            if (AppQuickStartFeature.isTouchDownPreloadEnable()) {
                return true;
            }
            return false;
        } else if (preloadType != 101) {
            AwareLog.w(TAG, "unsupport preloadType:" + preloadType);
            return false;
        } else if (AppQuickStartFeature.isPreloadOptEnable()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean startPreLoadApplication(String pkg, int userId, int preloadType) {
        if (pkg == null || pkg.isEmpty() || userId < 0 || !checkPreloadType(preloadType) || this.mThreadhandler == null) {
            return false;
        }
        this.mThreadhandler.sendMessage(this.mThreadhandler.obtainMessage(5, new HwProcessInfo(pkg, "", userId, -1, preloadType)));
        return true;
    }

    public void setPermanentPreloadPkgs(Set<String> permanentPkgs) {
        if (permanentPkgs != null && !permanentPkgs.isEmpty()) {
            synchronized (this.mPermanentPreloadPkgs) {
                this.mPermanentPreloadPkgs.clear();
                this.mPermanentPreloadPkgs.addAll(permanentPkgs);
            }
        }
    }

    public void tryPreLoadPermanentApplication() {
        if (AppQuickStartFeature.isTouchDownPreloadEnable() && this.mThreadhandler != null) {
            synchronized (this.mPermanentPreloadPkgs) {
                for (String pkg : this.mPermanentPreloadPkgs) {
                    if (pkg != null) {
                        this.mThreadhandler.sendMessage(this.mThreadhandler.obtainMessage(1, new HwProcessInfo(pkg, "", 0, -1, 2)));
                    }
                }
            }
        }
    }

    private boolean checkAppIsTopN(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        List<String> list = null;
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null && habit.isEnable()) {
            int topN = AppQuickStartFeature.getTopN();
            list = habit.getTopN(topN);
            AwareLog.d(TAG, "TouchDownPreloadApp getTopN topN " + topN + " list " + list);
        }
        if (list != null && list.contains(packageName)) {
            return true;
        }
        AwareLog.d(TAG, "TouchDownPreloadApp checkAppCanLoad, not in topN apps");
        return false;
    }

    private boolean handlePreloadAppOpt(String packageName, int userId, int preloadType) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        if (preloadType == 101 && !checkAppIsTopN(packageName)) {
            AwareLog.d(TAG, "handlePreloadAppOpt packageName is not top");
            return false;
        } else if (AppQuickStartFeature.isTouchDownExcludePkg(packageName)) {
            return false;
        } else {
            HwActivityManager.preloadAppForLauncher(packageName, userId, preloadType);
            return true;
        }
    }

    private boolean checkAppInfo(ApplicationInfo appInfo, int userId) {
        if (appInfo == null) {
            return false;
        }
        if ((appInfo.flags & 9) == 9) {
            AwareLog.d(TAG, "TouchDownPreloadApp preloadAppForLauncher, application is persistent, return");
            return false;
        } else if (checkHasAnyProcessExist(appInfo, userId)) {
            return false;
        } else {
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean handlePreloadAppForLauncher(HwProcessInfo pi) {
        int ret;
        String packageName = pi.packageName;
        int preloadType = pi.preloadType;
        int userId = pi.userId;
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        if (preloadType == 101) {
            return handlePreloadAppOpt(packageName, userId, preloadType);
        }
        if (!checkAppCanLoad(packageName, userId)) {
            AwareLog.d(TAG, "TouchDownPreloadApp preloadAppForLauncher App Can't Load, packageName: " + packageName + ", userid: " + userId);
            return false;
        }
        ApplicationInfo appInfo = getAppInfo(packageName, userId).orElse(null);
        if (!checkAppInfo(appInfo, userId) || (ret = HwActivityManager.preloadAppForLauncher(packageName, userId, preloadType)) == -1) {
            return false;
        }
        if (ret == -2) {
            handleExistedProcess(appInfo.packageName, appInfo.processName, userId, appInfo.uid);
            return false;
        } else if (IS_TV_PERSIST) {
            AwareLog.d(TAG, "TouchDownPreloadApp persist preload process, no need to reclaim");
            return true;
        } else {
            if (preloadType == 0) {
                AwareLog.d(TAG, "TouchDownPreloadApp handleDefaultPreloadType");
                handleDefaultPreloadType(appInfo, appInfo.processName, userId);
            } else if (preloadType == 1) {
                AwareLog.d(TAG, "TouchDownPreloadApp handleDynamicPreloadType");
                handleDynamicPreloadType(appInfo, appInfo.processName, userId);
            } else if (preloadType == 2) {
                AwareLog.d(TAG, "TouchDownPreloadApp handlePermanentPreloadType");
            } else {
                AwareLog.d(TAG, "TouchDownPreloadApp unsupport preloadType sendKillMessageDelay, pkg: " + appInfo.packageName + ", procName: " + appInfo.processName + ",userId: " + userId + ", uid: " + appInfo.uid);
                sendKillMessageDelay(appInfo.packageName, appInfo.processName, userId, appInfo.uid);
            }
            return true;
        }
    }

    private Optional<ApplicationInfo> getAppInfo(String packageName, int userId) {
        if (packageName == null || packageName.isEmpty()) {
            return Optional.empty();
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = IPackageManagerEx.getApplicationInfo(packageName, 1152, userId);
        } catch (RemoteException e) {
            AwareLog.e(TAG, "TouchDownPreloadApp RemoteException Failed trying to get application info: " + packageName);
        }
        if (appInfo == null) {
            AwareLog.d(TAG, "TouchDownPreloadApp preloadAppForLauncher, no such application info, pkg = " + packageName);
        }
        return Optional.ofNullable(appInfo);
    }

    private boolean checkAppCanLoad(String packageName, int userId) {
        if (userId < 0 || packageName == null || packageName.isEmpty()) {
            return false;
        }
        synchronized (this.mAppClearedList) {
            if (this.mAppClearedList.contains(packageName)) {
                AwareLog.d(TAG, "TouchDownPreloadApp checkAppCanLoad, app data cleared");
                return false;
            }
        }
        if (AppQuickStartFeature.isTouchDownExcludePkg(packageName)) {
            return false;
        }
        return true;
    }

    private boolean checkHasAnyProcessExist(ApplicationInfo appInfo, int userId) {
        HwActivityManagerService hams = HwActivityManagerService.self();
        if (hams == null) {
            return true;
        }
        int num = hams.iawareGetUidProcNum(appInfo.uid);
        if (num <= 0 || !checkProcRealExist(this.mContext, appInfo.packageName)) {
            return false;
        }
        AwareLog.d(TAG, "TouchDownPreloadApp preloadAppForLauncher, this uid " + appInfo.uid + " really has more than 1 process (" + num + ") exist.");
        handleExistedProcess(appInfo.packageName, appInfo.processName, userId, appInfo.uid);
        return true;
    }

    private static boolean checkProcRealExist(Context context, String packageName) {
        List<ActivityManager.RunningAppProcessInfo> runningApps;
        if (context == null || packageName == null || packageName.isEmpty()) {
            return false;
        }
        Object obj = context.getSystemService(BigMemoryConstant.BIG_MEM_INFO_ITEM_TAG);
        if (!(obj instanceof ActivityManager) || (runningApps = ((ActivityManager) obj).getRunningAppProcesses()) == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void handleDefaultPreloadType(ApplicationInfo appInfo, String processName, int userId) {
        if (processName != null && !processName.isEmpty()) {
            String str = this.mLastPackageName;
            if (str != null && !processName.equals(str)) {
                AwareLog.d(TAG, "TouchDownPreloadApp new app to preload, packageName: " + processName + ", old packageName: " + this.mLastPackageName);
                killOldPreloadApp(this.mLastPackageName, this.mLastUserId, this.mLastUid);
            }
            this.mLastPackageName = appInfo.packageName;
            this.mLastUserId = userId;
            this.mLastUid = appInfo.uid;
        }
    }

    private void handleDynamicPreloadType(ApplicationInfo appInfo, String processName, int userId) {
        if (processName != null && !processName.isEmpty()) {
            PreloadAppInfo papp = new PreloadAppInfo(appInfo.packageName, userId, appInfo.uid);
            AwareLog.d(TAG, "TouchDownPreloadApp new app to preload, packageName: " + processName);
            if (this.mDynamicAppList.size() < 3) {
                int index = this.mDynamicAppList.indexOf(papp);
                if (index >= 0) {
                    AwareLog.d(TAG, "TouchDownPreloadApp app already in preload app list.");
                    this.mDynamicAppList.remove(index);
                    this.mDynamicAppList.add(this.mDynamicAppList.get(index));
                    return;
                }
                AwareLog.d(TAG, "TouchDownPreloadApp new app to add");
                this.mDynamicAppList.add(papp);
                return;
            }
            AwareLog.d(TAG, "TouchDownPreloadApp new app to update");
            int index2 = this.mDynamicAppList.indexOf(papp);
            if (index2 >= 0) {
                AwareLog.d(TAG, "TouchDownPreloadApp app already in preload app list");
                this.mDynamicAppList.remove(index2);
                this.mDynamicAppList.add(this.mDynamicAppList.get(index2));
                return;
            }
            AwareLog.d(TAG, "TouchDownPreloadApp app not in preload app list");
            PreloadAppInfo oldPapp = this.mDynamicAppList.poll();
            this.mDynamicAppList.add(papp);
            if (oldPapp != null) {
                ProcessCleaner pcleaner = ProcessCleaner.getInstance();
                if (pcleaner == null || !pcleaner.checkPkgInProtectedListFromMdm(oldPapp.packageName)) {
                    killOldPreloadApp(oldPapp.packageName, oldPapp.userId, oldPapp.uid);
                    return;
                }
                AwareLog.d(TAG, "TouchDownPreloadApp app " + oldPapp.packageName + " protected by MDM");
            }
        }
    }

    private void killOldPreloadApp(String packageName, int userId, int uid) {
        ApplicationInfo oldAppInfo;
        if (packageName != null && !packageName.isEmpty() && (oldAppInfo = getAppInfo(packageName, userId).orElse(null)) != null && HwActivityManager.isProcessExistLocked(oldAppInfo.processName, uid)) {
            AwareLog.d(TAG, "TouchDownPreloadApp sendKillMessageDelay to kill old preload app, pkg: " + oldAppInfo.packageName + ", procName: " + oldAppInfo.processName + ",userId: " + userId + ", uid: " + uid);
            sendKillMessageDelay(oldAppInfo.packageName, oldAppInfo.processName, userId, uid);
        }
    }

    private void handleExistedProcess(String pkg, String processName, int userId, int uid) {
        if (pkg != null && processName != null && !pkg.isEmpty() && !processName.isEmpty() && this.mThreadhandler != null) {
            HwProcessInfo pi = new HwProcessInfo(pkg, processName, userId, uid, 0);
            synchronized (this.mLocker) {
                int index = this.mPkgList.indexOf(pi);
                if (index >= 0) {
                    AwareLog.d(TAG, "TouchDownPreloadApp delayKillMessage, process has existed, " + pkg + ", processName: " + processName + ", userId: " + userId + ", uid: " + uid);
                    HwProcessInfo pi2 = this.mPkgList.get(index);
                    this.mThreadhandler.removeMessages(2, pi2);
                    this.mThreadhandler.sendMessageDelayed(this.mThreadhandler.obtainMessage(2, pi2), (long) AppQuickStartFeature.getTouchDownKillTime());
                }
            }
        }
    }

    private void sendKillMessageDelay(String pkg, String processName, int userId, int uid) {
        if (pkg != null && processName != null && !pkg.isEmpty() && !processName.isEmpty() && this.mThreadhandler != null) {
            HwProcessInfo pi = new HwProcessInfo(pkg, processName, userId, uid, 0);
            synchronized (this.mLocker) {
                int index = this.mPkgList.indexOf(pi);
                if (index >= 0) {
                    pi = this.mPkgList.get(index);
                    this.mThreadhandler.removeMessages(2, pi);
                } else {
                    this.mPkgList.add(pi);
                    AwareLog.d(TAG, "TouchDownPreloadApp sendKillMessageDelay, packageName: " + pkg + ", processName: " + processName + ", userId: " + userId + ", uid: " + uid);
                }
            }
            this.mThreadhandler.sendMessageDelayed(this.mThreadhandler.obtainMessage(2, pi), (long) AppQuickStartFeature.getTouchDownKillTime());
        }
    }

    public void finishPreloadApplicationForLauncher(String pkg, int userId, int uid) {
        HwProcessInfo pi0;
        if (pkg != null && uid >= 0 && userId >= 0 && !pkg.isEmpty() && AppQuickStartFeature.isTouchDownPreloadEnable() && !AppQuickStartFeature.isTouchDownExcludePkg(pkg)) {
            removeFromClearedList(pkg);
            synchronized (this.mLocker) {
                for (int i = this.mPkgList.size() - 1; i >= 0; i--) {
                    HwProcessInfo hpi = this.mPkgList.get(i);
                    if (hpi != null && hpi.userId == userId && pkg.equals(hpi.packageName) && (pi0 = this.mPkgList.remove(i)) != null) {
                        AwareLog.d(TAG, "TouchDownPreloadApp removeKillMessage, packageName: " + pkg + ", userId: " + userId + ", uid: " + uid);
                        if (this.mThreadhandler != null) {
                            this.mThreadhandler.removeMessages(2, pi0);
                        }
                    }
                }
            }
        }
    }

    private void handleKillpreApplication(HwProcessInfo pi) {
        if (pi != null && pi.packageName != null && !pi.packageName.isEmpty() && pi.userId >= 0) {
            synchronized (this.mLocker) {
                if (this.mPkgList.indexOf(pi) >= 0) {
                    this.mPkgList.remove(pi);
                }
            }
            if (getPackageStoppedState(pi.packageName, pi.userId)) {
                AwareLog.d(TAG, "TouchDownPreloadApp killpreApplication, forceStopApp packageName: " + pi.packageName);
                forceStopApp(pi.packageName, pi.userId);
                return;
            }
            AwareLog.d(TAG, "TouchDownPreloadApp killpreApplication, cleanApp packageName: " + pi.packageName + " userid: " + pi.userId);
            cleanApp(pi.packageName, pi.userId, "touchdown preload");
        }
    }

    private boolean getPackageStoppedState(String packageName, int userId) {
        ApplicationInfo appInfo = getAppInfo(packageName, userId).orElse(null);
        if (appInfo == null || (appInfo.flags & 2097152) == 0) {
            return false;
        }
        return true;
    }

    private void forceStopApp(String packageName, int userId) {
        Context context;
        if (packageName != null && userId >= 0 && (context = this.mContext) != null) {
            Object obj = context.getSystemService(BigMemoryConstant.BIG_MEM_INFO_ITEM_TAG);
            if (obj instanceof ActivityManager) {
                ActivityManagerEx.forceStopPackageAsUser((ActivityManager) obj, packageName, userId);
            }
        }
    }

    private void cleanApp(String packageName, int userId, String reason) {
        ArrayList<AwareProcessInfo> procInfo = AwareProcessInfo.getAwareProcInfosFromPackage(packageName, userId);
        AwareProcessBlockInfo procGroup = null;
        int iProcInfoSize = procInfo.size();
        for (int i = 0; i < iProcInfoSize; i++) {
            AwareProcessInfo apinfo = procInfo.get(i);
            if (apinfo != null) {
                if (!(apinfo.procProcInfo == null || apinfo.procProcInfo.mProcessName == null)) {
                    AwareLog.d(TAG, "ContinuePowerDevMng HwTouchDownPreloadManager , cleanApp process: " + apinfo.procProcInfo.mProcessName);
                }
                if (procGroup == null) {
                    procGroup = new AwareProcessBlockInfo(reason, userId, apinfo, ProcessCleaner.CleanType.KILL_ALLOW_START.ordinal(), null);
                } else {
                    procGroup.add(apinfo);
                }
            }
        }
        ProcessCleaner pcleaner = ProcessCleaner.getInstance(null);
        if (pcleaner != null) {
            pcleaner.uniformClean(procGroup, null, reason);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkPermission(int callingUid, String packageName, int userId) {
        if (callingUid == 0 || callingUid == 1000 || checkCallingPackage(callingUid)) {
            return true;
        }
        return false;
    }

    private boolean checkCallingPackage(int callingUid) {
        String name = "";
        String launcher = AppQuickStartFeature.getTouchDownLauncher();
        if (launcher == null || launcher.isEmpty()) {
            return false;
        }
        try {
            name = IPackageManagerExt.getNameForUid(callingUid);
        } catch (RemoteException e) {
            AwareLog.e(TAG, "TouchDownPreloadApp Failed trying to get lancher info.");
        }
        AwareLog.d(TAG, "callingUid: " + callingUid + " name:" + name);
        return launcher.equals(name);
    }

    public void addToClearedList(String packageName) {
        if (packageName != null && !packageName.isEmpty() && AppQuickStartFeature.isTouchDownPreloadEnable() && !AppQuickStartFeature.isTouchDownExcludePkg(packageName)) {
            synchronized (this.mAppClearedList) {
                this.mAppClearedList.add(packageName);
            }
            AwareLog.d(TAG, "TouchDownPreloadApp add " + packageName + " to ClearedList");
        }
    }

    private void removeFromClearedList(String packageName) {
        if (packageName != null && !packageName.isEmpty()) {
            synchronized (this.mAppClearedList) {
                if (this.mAppClearedList.contains(packageName)) {
                    this.mAppClearedList.remove(packageName);
                }
            }
        }
    }

    public void keyDownEvent(int keyCode, boolean down) {
        Handler handler;
        if (AppQuickStartFeature.isKeyBoostEnable() && (handler = this.mThreadhandler) != null) {
            this.mThreadhandler.sendMessage(handler.obtainMessage(3));
        }
    }

    public void dumpPreloadAppPkgs(PrintWriter pw, String pkg, int userId, int preloadType) {
        if (pw != null && pkg != null) {
            pw.println("== preloadAppPkgs Start ==");
            boolean ret = HwSysResManager.getInstance().preloadAppForLauncher(pkg, userId, preloadType);
            pw.println("== preloadAppPkgs End ==, result:" + ret);
        }
    }

    /* access modifiers changed from: private */
    public static class HwProcessInfo {
        public String packageName;
        public int preloadType = 0;
        public String processName;
        public int uid = -1;
        public int userId = -1;

        public HwProcessInfo(String pkg, String prcn, int userId2, int uid2, int preloadType2) {
            this.packageName = pkg;
            this.processName = prcn;
            this.userId = userId2;
            this.uid = uid2;
            this.preloadType = preloadType2;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof HwProcessInfo)) {
                return false;
            }
            HwProcessInfo other = (HwProcessInfo) obj;
            if (this.userId != other.userId || this.uid != other.uid || !equalEx(this.packageName, other.packageName) || !equalEx(this.processName, other.processName)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            String str = this.packageName;
            int i = 0;
            int hashCode = str == null ? 0 : str.hashCode();
            String str2 = this.processName;
            if (str2 != null) {
                i = str2.hashCode();
            }
            return hashCode + i + (this.userId * 100000) + this.uid;
        }

        private boolean equalEx(String str1, String str2) {
            if (str1 == null || str2 == null || !str1.equals(str2)) {
                return false;
            }
            return true;
        }
    }
}
