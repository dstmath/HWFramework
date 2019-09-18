package com.android.server.mtm.utils;

import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAppLruBase;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMService;
import huawei.com.android.server.fingerprint.FingerViewController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class AppStatusUtils {
    private static final String TAG = "AppStatusUtils";
    private static volatile AppStatusUtils instance;
    private final String ADJTYPE_SERVICE = AwareAppMngSort.ADJTYPE_SERVICE;
    private final long BLUETOOTH_PROTECT_TIME = 120000;
    private final long EXPIRE_TIME_NANOS = 10000000;
    private final String FG_SERVICE = AwareAppMngSort.FG_SERVICE;
    private final String RECENT_TASK_ADJ_TYPE = "pers-top-activity";
    private final String SYSTEMUI_PACKAGE_NAME = FingerViewController.PKGNAME_OF_KEYGUARD;
    private final String TOP_ACTIVITY_ADJ_TYPE = "top-activity";
    private volatile ArrayMap<Integer, AwareProcessInfo> mAllProcNeedSort = null;
    private volatile ArrayMap<Integer, ProcessInfo> mAudioIn = null;
    private volatile ArrayMap<Integer, ProcessInfo> mAudioOut = null;
    private volatile Set<String> mBlindPkg = new ArraySet();
    private int mCurrentUserId = 0;
    private volatile ArraySet<Integer> mForeGroundAssocPid = null;
    private volatile ArrayMap<Integer, ArrayList<String>> mForeGroundUid = null;
    private volatile List<String> mGcmAppList = null;
    private final HwActivityManagerService mHwAMS = HwActivityManagerService.self();
    private final AwareAppKeyBackgroup mKeyBackgroupInstance = AwareAppKeyBackgroup.getInstance();
    private volatile Set<Integer> mKeyPercepServicePid = null;
    private volatile AwareAppLruBase mPrevAmsBase = null;
    private volatile Set<AwareAppLruBase> mPrevAwareBase = null;
    private volatile Set<String> mRestrainedVisWinList = null;
    private volatile ProcessInfo mSystemuiProcInfo = null;
    private volatile long mUpdateTime = -1;
    private volatile Set<String> mVisibleWindowList = null;
    private volatile Set<String> mWidgetList = null;
    private volatile Map<Status, Predicate<AwareProcessInfo>> predicates = new HashMap();

    public enum Status {
        NOT_IMPORTANT("not_important"),
        PERSIST("Persist"),
        TOP_ACTIVITY("TopActivity"),
        VISIBLE_APP("VisApp"),
        FOREGROUND("Fground"),
        VISIBLEWIN("VisWin"),
        WIDGET("Widget"),
        ASSOC_WITH_FG("FgAssoc"),
        KEYBACKGROUND("KeyBground"),
        BACKUP_APP("BackApp"),
        HEAVY_WEIGHT_APP("HeaWeiApp"),
        PREV_ONECLEAN("RecTask"),
        MUSIC_PLAY("Music"),
        SOUND_RECORD("Record"),
        GUIDE("Guide"),
        DOWN_UP_LOAD("Download"),
        KEY_SYS_SERVICE("KeySys"),
        HEALTH("Health"),
        PREVIOUS("LastUse"),
        SYSTEM_APP("SysApp"),
        NON_CURRENT_USER("non_cur_user"),
        LAUNCHER("Launcher"),
        INPUT_METHOD("InputMethed"),
        WALLPAPER("WallPaper"),
        FROZEN("Frozen"),
        BLUETOOTH("Btooth"),
        TOAST_WINDOW("ToastWin"),
        FOREGROUND_APP("FgApp"),
        BLIND("Blind"),
        MUSIC_INSTANT("MusicINS"),
        NONSYSTEMUSER("NonSystemUser"),
        GCM("gcm"),
        INSMALLSAMPLELIST("InSmallSampleList"),
        ACHSCRCHANGEDNUM("AchScrChangedNum"),
        SCREENRECORD("ScreenRecord"),
        CAMERARECORD("CameraRecord"),
        RESTRAINED_VIS_WIN("RestrainedVisWin"),
        OVERSEA_APP("OverseaApp"),
        DOWNLOAD_FREEZE("DownloadFreeze"),
        PROC_STATE_CACHED("PSCached"),
        REG_KEEP_ALIVER_APP("RegKeepAlive"),
        TOP_IM_APP_BASE("TopImBase"),
        BLUETOOTH_CONNECT("BtoothConnect"),
        BLUETOOTH_LAST("BtoothLast");
        
        private String mDescription;

        private Status(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    private AppStatusUtils() {
        this.predicates.put(Status.PERSIST, $$Lambda$AppStatusUtils$pzX_iOvm521ZtH93faodknz7Hwg.INSTANCE);
        this.predicates.put(Status.TOP_ACTIVITY, $$Lambda$AppStatusUtils$gYXvacvzzxawxrbmBjc_KJ5lztE.INSTANCE);
        this.predicates.put(Status.VISIBLE_APP, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.lambda$new$2(AppStatusUtils.this, (AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.FOREGROUND, $$Lambda$AppStatusUtils$6urJlXXNXnVV2wx0LEBFOLgcMzM.INSTANCE);
        this.predicates.put(Status.VISIBLEWIN, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkVisibleWindow((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.WIDGET, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkWidget((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.ASSOC_WITH_FG, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkAssocWithFg((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.KEYBACKGROUND, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkKeyBackGround((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.BACKUP_APP, $$Lambda$AppStatusUtils$evEKtntBZDMPoH3rjBl76lj0yc4.INSTANCE);
        this.predicates.put(Status.HEAVY_WEIGHT_APP, $$Lambda$AppStatusUtils$iUWENTOSCiKEf90R5ewWLgDRytY.INSTANCE);
        this.predicates.put(Status.PREV_ONECLEAN, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkPrevOneClean((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.MUSIC_PLAY, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkMusicPlay((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.SOUND_RECORD, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkSoundRecord((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.GUIDE, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkKeyBackgroupByState(3, (AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.DOWN_UP_LOAD, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkKeyBackgroupByState(5, (AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.KEY_SYS_SERVICE, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkKeySysProc((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.HEALTH, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkKeyBackgroupByState(4, (AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.PREVIOUS, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkPrevious((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.SYSTEM_APP, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkSystemApp((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.NON_CURRENT_USER, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkNonCurrentUser((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.LAUNCHER, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkLauncher((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.INPUT_METHOD, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkInputMethod((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.WALLPAPER, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkWallPaper((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.FROZEN, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkFrozen((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.BLUETOOTH, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkBluetooth((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.TOAST_WINDOW, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkToastWindow((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.FOREGROUND_APP, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkForeground((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.BLIND, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkBlind((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.MUSIC_INSTANT, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkMusicInstant((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.NONSYSTEMUSER, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkNonSystemUser((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.GCM, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkGcm((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.INSMALLSAMPLELIST, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkInSmallSampleList((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.ACHSCRCHANGEDNUM, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkScreenChangedAcheive((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.SCREENRECORD, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkScreenRecord((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.CAMERARECORD, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkCameraRecord((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.RESTRAINED_VIS_WIN, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkRestrainedVisWin((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.OVERSEA_APP, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkOverseaApp((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.DOWNLOAD_FREEZE, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkDownloadFrozen((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.PROC_STATE_CACHED, $$Lambda$AppStatusUtils$zeTic1YZ3MP4rMTOdes0tYmSXc.INSTANCE);
        this.predicates.put(Status.REG_KEEP_ALIVER_APP, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkIsRegKeepALive((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.TOP_IM_APP_BASE, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkTopImAppBase((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.BLUETOOTH_CONNECT, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkBluetoothConnect((AwareProcessInfo) obj);
            }
        });
        this.predicates.put(Status.BLUETOOTH_LAST, new Predicate() {
            public final boolean test(Object obj) {
                return AppStatusUtils.this.checkBluetoothLast((AwareProcessInfo) obj);
            }
        });
    }

    static /* synthetic */ boolean lambda$new$0(AwareProcessInfo param) {
        return param.mProcInfo.mCurAdj < 0;
    }

    static /* synthetic */ boolean lambda$new$1(AwareProcessInfo param) {
        return param.mProcInfo.mCurAdj == 0;
    }

    public static /* synthetic */ boolean lambda$new$2(AppStatusUtils appStatusUtils, AwareProcessInfo param) {
        return param.mProcInfo.mCurAdj >= 100 && param.mProcInfo.mCurAdj < 200 && !appStatusUtils.checkForeground(param);
    }

    static /* synthetic */ boolean lambda$new$3(AwareProcessInfo param) {
        return param.mProcInfo.mCurAdj < 200;
    }

    static /* synthetic */ boolean lambda$new$8(AwareProcessInfo param) {
        return param.mProcInfo.mCurAdj == 300;
    }

    static /* synthetic */ boolean lambda$new$9(AwareProcessInfo param) {
        return param.mProcInfo.mCurAdj == 400;
    }

    static /* synthetic */ boolean lambda$new$38(AwareProcessInfo param) {
        return param.mProcInfo.mSetProcState > 13;
    }

    public static AppStatusUtils getInstance() {
        if (instance == null) {
            synchronized (AppStatusUtils.class) {
                if (instance == null) {
                    instance = new AppStatusUtils();
                }
            }
        }
        return instance;
    }

    public boolean checkAppStatus(Status statusType, AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            AwareLog.e(TAG, "null AwareProcessInfo input!");
            return false;
        } else if (awareProcInfo.mProcInfo == null) {
            AwareLog.e(TAG, "null ProcessInfo input!");
            return false;
        } else if (-1 == awareProcInfo.mProcInfo.mCurAdj) {
            AwareLog.e(TAG, "mCurAdj of ProcessInfo not set!");
            return false;
        } else {
            Predicate<AwareProcessInfo> func = this.predicates.get(statusType);
            if (func == null) {
                AwareLog.e(TAG, "error status type input!");
                return false;
            } else if (!getNewProcesses()) {
                AwareLog.e(TAG, "update processes status failed!");
                return false;
            } else {
                ArrayMap<Integer, AwareProcessInfo> tmpAllProcNeedSort = this.mAllProcNeedSort;
                if (tmpAllProcNeedSort == null || tmpAllProcNeedSort.isEmpty()) {
                    AwareLog.e(TAG, "mAllProcNeedSort is null");
                    return false;
                }
                AwareProcessInfo newAwareProcInfo = tmpAllProcNeedSort.get(Integer.valueOf(awareProcInfo.mPid));
                if (newAwareProcInfo != null) {
                    return func.test(newAwareProcInfo);
                }
                return func.test(awareProcInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean checkSystemApp(AwareProcessInfo awareProcInfo) {
        int uid = awareProcInfo.mProcInfo.mUid;
        int tmpCurrentUserId = this.mCurrentUserId;
        if (uid <= 10000) {
            return true;
        }
        if (uid <= tmpCurrentUserId * LaserTSMService.EXCUTE_OTA_RESULT_SUCCESS || uid > (LaserTSMService.EXCUTE_OTA_RESULT_SUCCESS * tmpCurrentUserId) + 10000) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean checkNonCurrentUser(AwareProcessInfo awareProcInfo) {
        return !AwareIntelligentRecg.getInstance().isCurrentUser(awareProcInfo.mProcInfo.mUid, this.mCurrentUserId);
    }

    /* access modifiers changed from: private */
    public boolean checkPrevOneClean(AwareProcessInfo awareProcInfo) {
        ProcessInfo tmpSystemuiProcInfo = this.mSystemuiProcInfo;
        int tmpCurrentUserId = this.mCurrentUserId;
        if (tmpSystemuiProcInfo == null) {
            return false;
        }
        if (tmpCurrentUserId == 0) {
            if (!"pers-top-activity".equals(tmpSystemuiProcInfo.mAdjType)) {
                return false;
            }
        } else if (!"top-activity".equals(tmpSystemuiProcInfo.mAdjType)) {
            return false;
        }
        AwareAppLruBase tmpAppLruBase = this.mPrevAmsBase;
        if (tmpAppLruBase == null) {
            return false;
        }
        ArrayList<String> packageNames = awareProcInfo.mProcInfo.mPackageName;
        if (packageNames == null || packageNames.isEmpty()) {
            return false;
        }
        ArrayMap<Integer, AwareProcessInfo> allProcNeedSort = this.mAllProcNeedSort;
        if (allProcNeedSort == null || allProcNeedSort.isEmpty()) {
            AwareLog.e(TAG, "mAllProcNeedSort is null");
            return false;
        } else if (awareProcInfo.mProcInfo.mUid != tmpAppLruBase.mUid) {
            return false;
        } else {
            if (!AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
                return true;
            }
            AwareProcessInfo prevProcInfo = allProcNeedSort.get(Integer.valueOf(tmpAppLruBase.mPid));
            if (prevProcInfo == null) {
                return false;
            }
            return isPkgIncludeForTgt(packageNames, prevProcInfo.mProcInfo.mPackageName);
        }
    }

    /* access modifiers changed from: private */
    public boolean checkAssocWithFg(AwareProcessInfo awareProcInfo) {
        ArrayMap<Integer, ArrayList<String>> tmpForeGroundUid = this.mForeGroundUid;
        ArraySet<Integer> tmpForeGroundAssocPid = this.mForeGroundAssocPid;
        if (tmpForeGroundUid == null) {
            return false;
        }
        if ((!tmpForeGroundUid.containsKey(Integer.valueOf(awareProcInfo.mProcInfo.mUid)) && !tmpForeGroundAssocPid.contains(Integer.valueOf(awareProcInfo.mProcInfo.mPid))) || awareProcInfo.mProcInfo.mForegroundActivities) {
            return false;
        }
        if (!AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
            return true;
        }
        return isPkgIncludeForTgt(awareProcInfo.mProcInfo.mPackageName, tmpForeGroundUid.get(Integer.valueOf(awareProcInfo.mProcInfo.mUid)));
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004a A[RETURN] */
    public boolean checkKeySysProc(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mType != 2) {
            return false;
        }
        if (procInfo.mCurAdj == 500) {
            return true;
        }
        boolean condition = !awareProcInfo.mHasShownUi && procInfo.mCreatedTime != -1;
        long timeCost = SystemClock.elapsedRealtime() - procInfo.mCreatedTime;
        if (procInfo.mUid < 10000) {
            if (procInfo.mCurAdj == 800) {
                return true;
            }
            if (!condition || timeCost >= AppMngConfig.getKeySysDecay()) {
                return false;
            }
            return true;
        } else if (condition && timeCost < AppMngConfig.getSysDecay()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean checkVisibleWindow(AwareProcessInfo awareProcInfo) {
        Set<String> tmpVisibleWindowList = this.mVisibleWindowList;
        if (tmpVisibleWindowList == null || tmpVisibleWindowList.isEmpty()) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mPackageName == null) {
            return false;
        }
        Iterator it = procInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (tmpVisibleWindowList.contains((String) it.next())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean checkRestrainedVisWin(AwareProcessInfo awareProcInfo) {
        Set<String> tmpRestrainedVisWinList = this.mRestrainedVisWinList;
        if (tmpRestrainedVisWinList == null || tmpRestrainedVisWinList.isEmpty()) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mPackageName == null) {
            return false;
        }
        Iterator it = procInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (tmpRestrainedVisWinList.contains((String) it.next())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean checkWidget(AwareProcessInfo awareProcInfo) {
        Set<String> tmpWidgetList = this.mWidgetList;
        if (tmpWidgetList == null || tmpWidgetList.isEmpty()) {
            return false;
        }
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mPackageName == null) {
            return false;
        }
        Iterator it = procInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (tmpWidgetList.contains((String) it.next())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean checkSoundRecord(AwareProcessInfo awareProcInfo) {
        ArrayMap<Integer, ProcessInfo> tmpAudioIn = this.mAudioIn;
        if (tmpAudioIn == null) {
            return false;
        }
        for (Map.Entry<Integer, ProcessInfo> m : tmpAudioIn.entrySet()) {
            ProcessInfo info = m.getValue();
            if (awareProcInfo.mProcInfo.mPid == info.mPid) {
                return true;
            }
            if (awareProcInfo.mProcInfo.mUid == info.mUid && (!AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid) || isPkgIncludeForTgt(awareProcInfo.mProcInfo.mPackageName, info.mPackageName))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean checkMusicPlay(AwareProcessInfo awareProcInfo) {
        ArrayMap<Integer, ProcessInfo> tmpAudioOut = this.mAudioOut;
        if (tmpAudioOut == null) {
            return false;
        }
        for (Map.Entry<Integer, ProcessInfo> m : tmpAudioOut.entrySet()) {
            ProcessInfo info = m.getValue();
            if (awareProcInfo.mProcInfo.mUid == info.mUid && (!AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid) || isPkgIncludeForTgt(awareProcInfo.mProcInfo.mPackageName, info.mPackageName))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean checkKeyBackGround(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mCurAdj != 200) {
            return false;
        }
        if (!AwareAppMngSort.FG_SERVICE.equals(procInfo.mAdjType) && !AwareAppMngSort.ADJTYPE_SERVICE.equals(procInfo.mAdjType)) {
            return true;
        }
        Set<Integer> tmpKeyPercepServicePid = this.mKeyPercepServicePid;
        if (tmpKeyPercepServicePid != null && tmpKeyPercepServicePid.contains(Integer.valueOf(procInfo.mPid))) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean checkPrevious(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo.mProcInfo.mCurAdj == 700) {
            return true;
        }
        if (awareProcInfo.mProcInfo.mCurAdj < 200) {
            return false;
        }
        ArrayList<String> packageNames = awareProcInfo.mProcInfo.mPackageName;
        if (packageNames == null || packageNames.isEmpty()) {
            return false;
        }
        Set<AwareAppLruBase> tmpAppLruBase = new ArraySet<>(this.mPrevAwareBase);
        ArrayMap<Integer, AwareProcessInfo> allProcNeedSort = this.mAllProcNeedSort;
        if (allProcNeedSort == null) {
            AwareLog.e(TAG, "mAllProcNeedSort is null");
            return false;
        }
        for (AwareAppLruBase app : tmpAppLruBase) {
            if (awareProcInfo.mProcInfo.mUid == app.mUid) {
                if (AwareAppAssociate.isDealAsPkgUid(awareProcInfo.mProcInfo.mUid)) {
                    AwareProcessInfo prevProcInfo = allProcNeedSort.get(Integer.valueOf(app.mPid));
                    if (prevProcInfo != null && isPkgIncludeForTgt(packageNames, prevProcInfo.mProcInfo.mPackageName)) {
                    }
                }
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean checkLauncher(AwareProcessInfo awareProcInfo) {
        return awareProcInfo.mProcInfo.mUid == AwareAppAssociate.getInstance().getCurHomeProcessUid();
    }

    private String getMainPkgName(AwareProcessInfo awareProcInfo) {
        ArrayList<String> packageNames = awareProcInfo.mProcInfo.mPackageName;
        if (packageNames == null || packageNames.isEmpty()) {
            return null;
        }
        return packageNames.get(0);
    }

    /* access modifiers changed from: private */
    public boolean checkInputMethod(AwareProcessInfo awareProcInfo) {
        String mainPackageName = getMainPkgName(awareProcInfo);
        if (mainPackageName == null || !mainPackageName.equals(AwareIntelligentRecg.getInstance().getDefaultInputMethod())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean checkWallPaper(AwareProcessInfo awareProcInfo) {
        String mainPackageName = getMainPkgName(awareProcInfo);
        if (mainPackageName == null || !mainPackageName.equals(AwareIntelligentRecg.getInstance().getDefaultWallPaper())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean checkFrozen(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isAppFrozen(awareProcInfo.mProcInfo.mUid);
    }

    /* access modifiers changed from: private */
    public boolean checkBluetooth(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isAppBluetooth(awareProcInfo.mProcInfo.mUid);
    }

    /* access modifiers changed from: private */
    public boolean checkDownloadFrozen(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isNotBeClean(getMainPkgName(awareProcInfo));
    }

    /* access modifiers changed from: private */
    public boolean checkToastWindow(AwareProcessInfo awareProcInfo) {
        return AwareIntelligentRecg.getInstance().isToastWindow(awareProcInfo.mProcInfo.mPid);
    }

    /* access modifiers changed from: private */
    public boolean checkKeyBackgroupByState(int state, AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (this.mKeyBackgroupInstance == null) {
            return false;
        }
        return this.mKeyBackgroupInstance.checkKeyBackgroupByState(state, procInfo.mPid, procInfo.mUid, (List<String>) procInfo.mPackageName);
    }

    public boolean getNewProcesses() {
        if (SystemClock.elapsedRealtimeNanos() - this.mUpdateTime <= 10000000) {
            return true;
        }
        if (this.mHwAMS == null) {
            return false;
        }
        return updateNewProcesses();
    }

    private boolean updateNewProcesses() {
        int uid;
        boolean isInfosEmpty;
        boolean isInfosEmpty2;
        ProcessInfo tmp_systemuiProcInfo = ProcessInfoCollector.getInstance().getProcessInfoList();
        Map<Integer, AwareProcessBaseInfo> tmp_baseInfos = this.mHwAMS.getAllProcessBaseInfo();
        boolean isInfosEmpty3 = tmp_systemuiProcInfo.isEmpty() || tmp_baseInfos.isEmpty();
        if (isInfosEmpty3) {
            return false;
        }
        ArrayMap<Integer, AwareProcessInfo> tmp_allProcNeedSort = new ArrayMap<>();
        ArrayMap<Integer, ArrayList<String>> tmp_mForeGroundUid = new ArrayMap<>();
        ArraySet<Integer> tmp_mForeGroundAssocPid = new ArraySet<>();
        ArrayMap<Integer, ProcessInfo> tmp_mAudioIn = new ArrayMap<>();
        ArrayMap<Integer, ProcessInfo> tmp_mAudioOut = new ArrayMap<>();
        Set<Integer> tmp_keyPercepServicePid = new HashSet<>();
        Set<Integer> tmp_fgServiceUid = new HashSet<>();
        ArraySet<Integer> tmp_importUid = new ArraySet<>();
        ArrayMap<Integer, Integer> tmp_percepServicePid = new ArrayMap<>();
        int tmp_CurrentUserId = AwareAppAssociate.getInstance().getCurUserId();
        ProcessInfo tmp_systemuiProcInfo2 = null;
        Iterator<ProcessInfo> it = tmp_systemuiProcInfo.iterator();
        while (true) {
            ProcessInfo tmp_procInfos = tmp_systemuiProcInfo;
            if (!it.hasNext()) {
                break;
            }
            ProcessInfo tmp_systemuiProcInfo3 = it.next();
            if (tmp_systemuiProcInfo3 == null) {
                isInfosEmpty = isInfosEmpty3;
            } else {
                isInfosEmpty = isInfosEmpty3;
                AwareProcessBaseInfo updateInfo = tmp_baseInfos.get(Integer.valueOf(tmp_systemuiProcInfo3.mPid));
                if (updateInfo != null) {
                    Map<Integer, AwareProcessBaseInfo> tmp_baseInfos2 = tmp_baseInfos;
                    tmp_systemuiProcInfo3.mCurAdj = updateInfo.mCurAdj;
                    tmp_systemuiProcInfo3.mSetProcState = updateInfo.mSetProcState;
                    tmp_systemuiProcInfo3.mForegroundActivities = updateInfo.mForegroundActivities;
                    tmp_systemuiProcInfo3.mAdjType = updateInfo.mAdjType;
                    tmp_systemuiProcInfo3.mAppUid = updateInfo.mAppUid;
                    updateForeGroundUid(tmp_systemuiProcInfo3, tmp_mForeGroundUid, tmp_mForeGroundAssocPid);
                    Iterator<ProcessInfo> it2 = it;
                    AwareProcessInfo awareProcInfo = new AwareProcessInfo(tmp_systemuiProcInfo3.mPid, tmp_systemuiProcInfo3);
                    awareProcInfo.mHasShownUi = updateInfo.mHasShownUi;
                    tmp_allProcNeedSort.put(Integer.valueOf(tmp_systemuiProcInfo3.mPid), awareProcInfo);
                    updateAudioIn(awareProcInfo, tmp_systemuiProcInfo3, tmp_mAudioIn);
                    updateAudioOut(awareProcInfo, tmp_systemuiProcInfo3, tmp_mAudioOut);
                    AwareProcessInfo awareProcessInfo = awareProcInfo;
                    if (tmp_systemuiProcInfo3.mCurAdj < 200) {
                        tmp_importUid.add(Integer.valueOf(tmp_systemuiProcInfo3.mUid));
                    } else {
                        updatePerceptibleApp(tmp_systemuiProcInfo3, tmp_fgServiceUid, tmp_percepServicePid, tmp_importUid);
                    }
                    if (isSystemUI(tmp_systemuiProcInfo3, tmp_CurrentUserId)) {
                        tmp_systemuiProcInfo2 = tmp_systemuiProcInfo3;
                    }
                    tmp_systemuiProcInfo = tmp_procInfos;
                    isInfosEmpty2 = isInfosEmpty;
                    tmp_baseInfos = tmp_baseInfos2;
                    it = it2;
                }
            }
            tmp_systemuiProcInfo = tmp_procInfos;
            isInfosEmpty2 = isInfosEmpty;
        }
        boolean z = isInfosEmpty3;
        int pid = 0;
        int uid2 = 0;
        Iterator<Map.Entry<Integer, Integer>> it3 = tmp_percepServicePid.entrySet().iterator();
        while (it3.hasNext()) {
            Map.Entry<Integer, Integer> m = it3.next();
            int i = pid;
            pid = m.getKey().intValue();
            int i2 = uid2;
            int uid3 = m.getValue().intValue();
            Iterator<Map.Entry<Integer, Integer>> it4 = it3;
            if (tmp_importUid.contains(Integer.valueOf(uid3))) {
                tmp_keyPercepServicePid.add(Integer.valueOf(pid));
            } else if (!tmp_fgServiceUid.contains(Integer.valueOf(uid3))) {
                tmp_keyPercepServicePid.add(Integer.valueOf(pid));
            } else {
                Set<Integer> strong = new ArraySet<>();
                uid = uid3;
                AwareAppAssociate.getInstance().getAssocClientListForPid(pid, strong);
                Iterator<Integer> it5 = strong.iterator();
                while (true) {
                    if (!it5.hasNext()) {
                        break;
                    }
                    Iterator<Integer> it6 = it5;
                    Integer clientPid = it5.next();
                    Integer num = clientPid;
                    AwareProcessInfo awareProcInfoItem = tmp_allProcNeedSort.get(clientPid);
                    if (awareProcInfoItem == null) {
                        it5 = it6;
                    } else {
                        Set<Integer> strong2 = strong;
                        AwareProcessInfo awareProcessInfo2 = awareProcInfoItem;
                        if (awareProcInfoItem.mProcInfo.mCurAdj <= 200) {
                            tmp_keyPercepServicePid.add(Integer.valueOf(pid));
                            break;
                        }
                        it5 = it6;
                        strong = strong2;
                    }
                }
                it3 = it4;
                uid2 = uid;
            }
            uid = uid3;
            it3 = it4;
            uid2 = uid;
        }
        int i3 = pid;
        int i4 = uid2;
        this.mUpdateTime = SystemClock.elapsedRealtimeNanos();
        this.mCurrentUserId = tmp_CurrentUserId;
        this.mForeGroundUid = tmp_mForeGroundUid;
        this.mForeGroundAssocPid = tmp_mForeGroundAssocPid;
        this.mAllProcNeedSort = tmp_allProcNeedSort;
        this.mAudioIn = tmp_mAudioIn;
        this.mAudioOut = tmp_mAudioOut;
        this.mKeyPercepServicePid = tmp_keyPercepServicePid;
        this.mPrevAmsBase = AwareAppAssociate.getInstance().getPreviousByAmsInfo();
        this.mPrevAwareBase = AwareAppAssociate.getInstance().getPreviousAppOpt();
        this.mSystemuiProcInfo = tmp_systemuiProcInfo2;
        updateVisibleWindowList();
        updateWidgetList();
        updateGcm();
        return true;
    }

    private void updateGcm() {
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit != null) {
            List<String> tmp = new ArrayList<>();
            List<String> result = habit.getGCMAppList();
            if (result != null) {
                tmp.addAll(result);
            }
            this.mGcmAppList = tmp;
        }
    }

    private boolean isSystemUI(ProcessInfo procInfo, int currentUserId) {
        ArrayList<String> packageNames = procInfo.mPackageName;
        if (packageNames == null || packageNames.isEmpty() || !FingerViewController.PKGNAME_OF_KEYGUARD.equals(packageNames.get(0)) || currentUserId != UserHandle.getUserId(procInfo.mUid)) {
            return false;
        }
        return true;
    }

    private void updateAudioOut(AwareProcessInfo awareProcInfo, ProcessInfo procInfo, ArrayMap<Integer, ProcessInfo> tmp_mAudioOut) {
        if (checkKeyBackgroupByState(2, awareProcInfo)) {
            tmp_mAudioOut.put(Integer.valueOf(procInfo.mPid), procInfo);
        }
    }

    private void updateAudioIn(AwareProcessInfo awareProcInfo, ProcessInfo procInfo, ArrayMap<Integer, ProcessInfo> tmp_mAudioIn) {
        if (checkKeyBackgroupByState(1, awareProcInfo)) {
            tmp_mAudioIn.put(Integer.valueOf(procInfo.mPid), procInfo);
        }
    }

    private void updatePerceptibleApp(ProcessInfo procInfo, Set<Integer> tmp_fgServiceUid, ArrayMap<Integer, Integer> tmp_percepServicePid, ArraySet<Integer> tmp_importUid) {
        if (procInfo.mCurAdj != 200) {
            return;
        }
        if (AwareAppMngSort.FG_SERVICE.equals(procInfo.mAdjType)) {
            tmp_fgServiceUid.add(Integer.valueOf(procInfo.mUid));
        } else if (AwareAppMngSort.ADJTYPE_SERVICE.equals(procInfo.mAdjType)) {
            tmp_percepServicePid.put(Integer.valueOf(procInfo.mPid), Integer.valueOf(procInfo.mUid));
        } else {
            tmp_importUid.add(Integer.valueOf(procInfo.mUid));
        }
    }

    private void updateForeGroundUid(ProcessInfo procInfo, ArrayMap<Integer, ArrayList<String>> tmp_mForeGroundUid, ArraySet<Integer> tmp_mForeGroundAssocPid) {
        if (procInfo.mForegroundActivities) {
            AwareAppAssociate.getInstance().getAssocProvider(procInfo.mPid, tmp_mForeGroundAssocPid);
            if (AwareAppAssociate.isDealAsPkgUid(procInfo.mUid)) {
                tmp_mForeGroundUid.put(Integer.valueOf(procInfo.mUid), procInfo.mPackageName);
            } else {
                tmp_mForeGroundUid.put(Integer.valueOf(procInfo.mUid), null);
            }
        }
    }

    private void updateWidgetList() {
        this.mWidgetList = AwareAppAssociate.getInstance().getWidgetsPkg();
    }

    private void updateVisibleWindowList() {
        Set<Integer> visibleWindows = new ArraySet<>();
        Set<Integer> restrainedVisWins = new ArraySet<>();
        Set<String> tmpVisibleWindowList = new ArraySet<>();
        Set<String> tmpRestrainedVisWinList = new ArraySet<>();
        ArrayMap<Integer, AwareProcessInfo> tmpAllProcNeedSort = this.mAllProcNeedSort;
        if (tmpAllProcNeedSort == null || tmpAllProcNeedSort.isEmpty()) {
            AwareLog.e(TAG, "mAllProcNeedSort is null");
            return;
        }
        AwareAppAssociate.getInstance().getVisibleWindows(visibleWindows, null);
        addVisibleWindowToList(visibleWindows, tmpVisibleWindowList, tmpAllProcNeedSort);
        this.mVisibleWindowList = tmpVisibleWindowList;
        AwareAppAssociate.getInstance().getVisibleWindowsInRestriction(restrainedVisWins);
        addVisibleWindowToList(restrainedVisWins, tmpRestrainedVisWinList, tmpAllProcNeedSort);
        this.mRestrainedVisWinList = tmpRestrainedVisWinList;
    }

    private void addVisibleWindowToList(Set<Integer> visibleWindows, Set<String> visibleWindowList, ArrayMap<Integer, AwareProcessInfo> procNeedSort) {
        for (Integer pid : visibleWindows) {
            AwareProcessInfo awareProcInfo = procNeedSort.get(pid);
            if (!(awareProcInfo == null || awareProcInfo.mProcInfo == null || awareProcInfo.mProcInfo.mPackageName == null)) {
                visibleWindowList.addAll(awareProcInfo.mProcInfo.mPackageName);
            }
        }
    }

    private boolean isPkgIncludeForTgt(ArrayList<String> tgtPkg, ArrayList<String> dstPkg) {
        if (tgtPkg == null || tgtPkg.isEmpty() || dstPkg == null) {
            return false;
        }
        Iterator<String> it = dstPkg.iterator();
        while (it.hasNext()) {
            String pkg = it.next();
            if (pkg != null && tgtPkg.contains(pkg)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<AwareProcessInfo> getAllProcNeedSort() {
        if (!getNewProcesses()) {
            AwareLog.e(TAG, "update processes status failed!");
            return null;
        }
        ArrayMap<Integer, AwareProcessInfo> tmpAllProcNeedSort = this.mAllProcNeedSort;
        if (tmpAllProcNeedSort != null && !tmpAllProcNeedSort.isEmpty()) {
            return new ArrayList<>(tmpAllProcNeedSort.values());
        }
        AwareLog.e(TAG, "mAllProcNeedSort is null");
        return null;
    }

    /* access modifiers changed from: private */
    public boolean checkForeground(AwareProcessInfo awareProcInfo) {
        Set<Integer> forePids = new ArraySet<>();
        AwareAppAssociate.getInstance().getForeGroundApp(forePids);
        for (Integer pid : forePids) {
            if (pid.equals(Integer.valueOf(awareProcInfo.mPid))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean checkBlind(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (procInfo.mPackageName == null) {
            return false;
        }
        Iterator it = procInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (this.mBlindPkg.contains((String) it.next())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean checkMusicInstant(AwareProcessInfo awareProcInfo) {
        ProcessInfo procInfo = awareProcInfo.mProcInfo;
        if (this.mKeyBackgroupInstance == null || procInfo == null) {
            return false;
        }
        return this.mKeyBackgroupInstance.checkAudioOutInstant(procInfo.mPid, procInfo.mUid, procInfo.mPackageName);
    }

    /* access modifiers changed from: private */
    public boolean checkNonSystemUser(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        return AwareAppMngSort.getInstance().checkNonSystemUser(awareProcInfo);
    }

    /* access modifiers changed from: private */
    public boolean checkInSmallSampleList(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isInSmallSampleList(awareProcInfo);
    }

    /* access modifiers changed from: private */
    public boolean checkScreenChangedAcheive(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isAchScreenChangedNum(awareProcInfo);
    }

    /* access modifiers changed from: private */
    public boolean checkScreenRecord(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isScreenRecord(awareProcInfo);
    }

    /* access modifiers changed from: private */
    public boolean checkCameraRecord(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isCameraRecord(awareProcInfo);
    }

    public void updateBlind(Set<String> blindPkgs) {
        if (blindPkgs == null) {
            this.mBlindPkg = new ArraySet();
        } else {
            this.mBlindPkg = blindPkgs;
        }
    }

    /* access modifiers changed from: private */
    public boolean checkGcm(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null || awareProcInfo.mProcInfo == null || this.mGcmAppList == null || awareProcInfo.mProcInfo.mPackageName == null) {
            return false;
        }
        Iterator it = awareProcInfo.mProcInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (this.mGcmAppList.contains((String) it.next())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean checkOverseaApp(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null || awareProcInfo.mProcInfo == null) {
            return false;
        }
        boolean result = false;
        Iterator it = awareProcInfo.mProcInfo.mPackageName.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            if (AppTypeRecoManager.getInstance().getAppWhereFrom((String) it.next()) != 0) {
                result = true;
                break;
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    public boolean checkIsRegKeepALive(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null || awareProcInfo.mProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isCurUserKeepALive(getMainPkgName(awareProcInfo), awareProcInfo.mProcInfo.mUid);
    }

    /* access modifiers changed from: private */
    public boolean checkTopImAppBase(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null || awareProcInfo.mProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isTopImAppBase(getMainPkgName(awareProcInfo));
    }

    /* access modifiers changed from: private */
    public boolean checkBluetoothConnect(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null || awareProcInfo.mProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isBluetoothConnect(awareProcInfo.mProcInfo.mPid);
    }

    /* access modifiers changed from: private */
    public boolean checkBluetoothLast(AwareProcessInfo awareProcInfo) {
        if (awareProcInfo == null || awareProcInfo.mProcInfo == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isBluetoothLast(awareProcInfo.mProcInfo.mPid, 120000);
    }
}
