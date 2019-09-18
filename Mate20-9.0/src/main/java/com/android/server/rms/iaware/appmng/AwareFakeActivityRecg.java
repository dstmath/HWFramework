package com.android.server.rms.iaware.appmng;

import android.app.ActivityManagerNative;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CmpTypeInfo;
import android.rms.iaware.IAwareCMSManager;
import com.android.internal.os.BackgroundThread;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.input.HwInputManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.AppStartupDataMgr;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.srms.SRMSDumpRadar;
import huawei.com.android.server.policy.stylus.StylusGestureSettings;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AwareFakeActivityRecg {
    private static final int CLSINDEX = 1;
    private static final int CMPLENGTH = 2;
    private static final long DELAY_LONGTIME_SCREENON_MS = 30000;
    private static final long FAKEACTIVITY_LAUNCH_WHEN_SCREENON_MS = 5000;
    private static final String FAKEACTIVITY_WHITELIST = "fake_white";
    public static final long FAKEACTIVIT_IS_DYNAMIC = 4;
    public static final long FAKEACTIVIT_IS_PERSISTENT = 3;
    private static final String FAKEA_AUTO_WAKEUP = "autowakeup";
    private static final String FAKEA_FINGERPRINT = "fingerprint";
    private static final String FAKEA_FORCESTOP = "forcestop";
    private static final String FAKEA_GIVEUP = "giveup";
    private static final String FAKEA_HAS_TOUCH = "hastouch";
    private static final String FAKEA_INSERT_SUSPECTED = "suspected";
    private static final String FAKEA_IS_FAKE = "isfake";
    private static final String FAKEA_PREVENTED_WHEN_SCREEN_OFF = "prevented-woff";
    private static final String FAKEA_PREVENTED_WHEN_SCREEN_ON = "prevented-won";
    private static final String FAKEA_PROC_CRASH = "proccrash";
    private static final String FAKEA_REMOVED_WHEN_SCREEN_ON = "removed-won";
    private static final String FAKEA_REMOVE_THIS_TIME_OF_RECG = "remove-ttr";
    private static final String FAKEA_UNINSTALL = "uninstall";
    private static final int GIVEUP_RECOGNIZE_AUTO_WAKEUP = 1;
    private static final int GIVEUP_RECOGNIZE_KEYCODE_BACK = 4;
    private static final int GIVEUP_RECOGNIZE_KEYCODE_HOME = 5;
    private static final int GIVEUP_RECOGNIZE_LAUNCH_ACTIVITY = 3;
    private static final int GIVEUP_RECOGNIZE_MULTI_WAKEUP = 2;
    private static final int GIVEUP_RECOGNIZE_NO_GIVEUP = 0;
    private static final long GIVEUP_TIME_FOR_FASTSCREENOFF_MS = 30000;
    private static final int GIVEUP_TIME_FOR_INITIATIVEWAKEUP_MS = 30000;
    public static final int INSERT_SUSPKGNAMECACHE_FORCESTOP = 2;
    private static final int INSERT_SUSPKGNAMECACHE_NORMALCREATE = 1;
    private static final int INSERT_SUSPKGNAMECACHE_NOVALUE = 0;
    private static final int JAVA_MSG_SLEEP = 100;
    private static final int JAVA_MSG_STOP_SEND = 105;
    private static final int JAVA_MSG_WAKEUP = 101;
    private static final int LOAD_CMPTYPE_MESSAGE_DELAY_MS = 2000;
    private static final int MSG_DELETEDATA = 3;
    private static final int MSG_FORCE_STOP = 11;
    private static final int MSG_INSERTDATA = 2;
    private static final int MSG_INSERT_SUSPECTED_FAKEACTIVITY = 8;
    private static final int MSG_LOADDATA = 1;
    private static final int MSG_RECG_FAKEACTIVITY = 4;
    private static final int MSG_RECG_LONGTIME_SCREENON = 5;
    private static final int MSG_REMOVE_THIS_TIME_OF_RECG = 13;
    private static final int MSG_REPORT_APPUPDATE = 9;
    private static final int MSG_SENDTO_BIGDATA = 10;
    private static final int MSG_SLEEP = 6;
    private static final int MSG_STOP_SENDEVENT = 12;
    private static final int MSG_WAKEUP = 7;
    private static final int NATIVE_EVENT_KEY = 2;
    private static final int NATIVE_EVENT_MOTION = 3;
    private static final int PKGINDEX = 0;
    private static final String TAG = "AwareFakeActivityRecg";
    public static final int TOUCHEVENT_FROM_APP = 12;
    public static final int TOUCHEVENT_NOEVENT = 0;
    private static final int TYPE_FAKEA_AUTO_WAKEUP = 5;
    private static final int TYPE_FAKEA_FINGERPRINT = 10;
    private static final int TYPE_FAKEA_FORCESTOP = 8;
    private static final int TYPE_FAKEA_GIVEUP = 4;
    private static final int TYPE_FAKEA_HAS_TOUCH = 7;
    private static final int TYPE_FAKEA_INSERT_SUSPECTED = 3;
    private static final int TYPE_FAKEA_IS_FAKE = 6;
    private static final int TYPE_FAKEA_PREVENTED_WHEN_SCREEN_OFF = 2;
    private static final int TYPE_FAKEA_PREVENTED_WHEN_SCREEN_ON = 1;
    private static final int TYPE_FAKEA_PROC_CRASH = 9;
    private static final int TYPE_FAKEA_REMOVED_WHEN_SCREEN_ON = 0;
    private static final int TYPE_FAKEA_REMOVE_THIS_TIME_OF_RECG = 12;
    private static final int TYPE_FAKEA_TOTAL = 13;
    private static final int TYPE_FAKEA_UNINSTALL = 11;
    private static AwareFakeActivityRecg mFakeActivityRecg = null;
    private static int[] mFakeTypes = new int[13];
    private static final Object mLock = new Object();
    private static boolean mRecgFakeActivityEnabled = true;
    private boolean DEBUG_COST = false;
    private boolean bGiveupRecognize = false;
    private AtomicBoolean bNeedProcessTouchEventNotify = new AtomicBoolean(false);
    private HashMap<Integer, Integer> mCrashUidPids = new HashMap<>();
    private int mCurUserId = 0;
    private final AtomicInteger mDataLoadCount = new AtomicInteger(2);
    private AppStartupDataMgr mDataMgr = new AppStartupDataMgr();
    private HashMap<String, Long> mFakeActivities = new HashMap<>();
    private FakeActivityRecgHandler mFakeActivityMsgHandler = null;
    private int mGiveupReason = 0;
    private long mGiveupRecongnizeTimeMs = 0;
    private Set<Integer> mGiveupUids = new HashSet();
    private long mGotoSleepingTimeMs = 0;
    private long mGotoWakeupTimeMs = 0;
    HwInputManagerService mHwInputManagerService = null;
    private boolean mIsFingerprintWakeup = false;
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private boolean mIsPowerkeyInteractive = false;
    private boolean mIsPowerkeyWakeup = false;
    private AtomicBoolean mScreenOnExpired = new AtomicBoolean(false);
    private HashMap<Integer, Long> mScreenonUids = new HashMap<>();
    private HashMap<String, Integer> mSuspectedActivities = new HashMap<>();
    private Set<String> mTempFakeActivities = new HashSet();
    private HashMap<Integer, Integer> mTouchEventPids = new HashMap<>();
    private Set<String> mWhiteFakeActivities = new HashSet();

    static class FakeActivityInfo {
        public int callerPid;
        public int callerUid;
        public String cmp;
        public boolean isScreenOn;
        /* access modifiers changed from: private */
        public String reason;

        public FakeActivityInfo(String cmp2, boolean isScreenOn2, int pid, int uid) {
            this.isScreenOn = isScreenOn2;
            this.callerPid = pid;
            this.callerUid = uid;
            this.cmp = cmp2;
        }

        public FakeActivityInfo(String cmp2, String reason2) {
            this.cmp = cmp2;
            this.reason = reason2;
        }
    }

    private class FakeActivityRecgHandler extends Handler {
        public FakeActivityRecgHandler(Looper looper) {
            super(looper);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: android.rms.iaware.CmpTypeInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v11, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v9, resolved type: android.rms.iaware.CmpTypeInfo} */
        /* JADX WARNING: Multi-variable type inference failed */
        private void handleMessageBecauseOfNSIQ(Message msg) {
            int i = msg.what;
            if (i == 9) {
                AwareFakeActivityRecg.this.handleReportAppUpdateMsg(msg);
            } else if (i != 13) {
                CmpTypeInfo cmpinfo = null;
                switch (i) {
                    case 1:
                        AwareFakeActivityRecg.this.initRecognizedFakeActivity();
                        return;
                    case 2:
                        if (msg.obj instanceof CmpTypeInfo) {
                            cmpinfo = msg.obj;
                        }
                        boolean unused = AwareFakeActivityRecg.this.insertFakeActivityToDB(cmpinfo);
                        return;
                    case 3:
                        if (msg.obj instanceof CmpTypeInfo) {
                            cmpinfo = msg.obj;
                        }
                        boolean unused2 = AwareFakeActivityRecg.this.deleteFakeActivityFromDB(cmpinfo);
                        return;
                    default:
                        return;
                }
            } else {
                AwareFakeActivityRecg.this.handleRemoveThisTimeOfRecg();
            }
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4:
                    if (msg.obj instanceof FakeActivityInfo) {
                        AwareFakeActivityRecg.this.handleRecognizeFakeActivity((FakeActivityInfo) msg.obj);
                        return;
                    }
                    return;
                case 5:
                    AwareFakeActivityRecg.this.handleLongTimeScreenOn();
                    return;
                case 6:
                    AwareFakeActivityRecg.this.handleSleepMsg();
                    return;
                case 8:
                    if (msg.obj instanceof String) {
                        String compName = (String) msg.obj;
                        AwareFakeActivityRecg.this.handleInsertSuspectMsg(compName);
                        AwareFakeActivityRecg.this.sendToBigDataMessage(compName, AwareFakeActivityRecg.FAKEA_INSERT_SUSPECTED, 3);
                        return;
                    }
                    return;
                case 10:
                    if (msg.obj instanceof FakeActivityInfo) {
                        AwareFakeActivityRecg.this.handleSendToBigdataMsg((FakeActivityInfo) msg.obj);
                        return;
                    }
                    return;
                case 11:
                    if (msg.obj instanceof String) {
                        AwareFakeActivityRecg.this.deleteSuspectedActivityByPkgName((String) msg.obj);
                        return;
                    }
                    return;
                case 12:
                    AwareFakeActivityRecg.this.handleStopNativeEventMsg();
                    return;
                default:
                    handleMessageBecauseOfNSIQ(msg);
                    return;
            }
        }
    }

    private AwareFakeActivityRecg() {
    }

    public static synchronized AwareFakeActivityRecg self() {
        AwareFakeActivityRecg awareFakeActivityRecg;
        synchronized (AwareFakeActivityRecg.class) {
            if (mFakeActivityRecg == null) {
                mFakeActivityRecg = new AwareFakeActivityRecg();
            }
            awareFakeActivityRecg = mFakeActivityRecg;
        }
        return awareFakeActivityRecg;
    }

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            if (this.mFakeActivityMsgHandler == null) {
                this.mFakeActivityMsgHandler = new FakeActivityRecgHandler(BackgroundThread.get().getLooper());
            }
            try {
                UserInfo currentUser = ActivityManagerNative.getDefault().getCurrentUser();
                if (currentUser != null) {
                    this.mCurUserId = currentUser.id;
                }
            } catch (RemoteException e) {
                AwareLog.e(TAG, "getCurrentUserId RemoteException");
            }
            initWhiteList();
            sendLoadAppTypeMessage();
            this.mIsInitialized.set(true);
        }
    }

    /* access modifiers changed from: private */
    public boolean insertFakeActivityToDB(CmpTypeInfo info) {
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                IAwareCMSManager.insertCmpTypeInfo(awareservice, info);
                return true;
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return false;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "inSertCmpRecgInfo RemoteException");
        }
    }

    /* access modifiers changed from: private */
    public boolean deleteFakeActivityFromDB(CmpTypeInfo info) {
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                IAwareCMSManager.deleteCmpTypeInfo(awareservice, info);
                return true;
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return false;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "deleteCmpRecgInfo RemoteException");
        }
    }

    private boolean loadFakeActivityInfo() {
        List<CmpTypeInfo> list = null;
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                list = IAwareCMSManager.getCmpTypeList(awareservice);
            } else {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "loadRecgResultInfo RemoteException");
        }
        if (list == null) {
            return false;
        }
        for (CmpTypeInfo info : list) {
            if (info.getType() == 1) {
                String comp = info.getCmp();
                if (isInWhiteList(comp)) {
                    sendUpdateCmpInfoMessage(info.getPkgName(), info.getCls(), 1, false);
                } else {
                    synchronized (this.mFakeActivities) {
                        if (this.mFakeActivities.get(comp) == null) {
                            AwareLog.i(TAG, "load fakeactivity from db:" + comp);
                            this.mFakeActivities.put(comp, 4L);
                        }
                    }
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void initRecognizedFakeActivity() {
        if (loadFakeActivityInfo()) {
            AwareLog.i(TAG, "load fakea from db OK ");
        } else if (this.mDataLoadCount.get() >= 0) {
            AwareLog.i(TAG, "send load fakea from db again mDataLoadCount=" + (this.mDataLoadCount.get() - 1));
            sendLoadAppTypeMessage();
            this.mDataLoadCount.decrementAndGet();
        } else {
            AwareLog.e(TAG, "laod fakea from db failed");
        }
    }

    private void sendUpdateCmpInfoMessage(String pkg, String cls, int type, boolean added) {
        CmpTypeInfo info = new CmpTypeInfo();
        info.setPkgName(pkg);
        info.setCls(cls);
        info.setType(type);
        info.setTime(System.currentTimeMillis());
        Message msg = this.mFakeActivityMsgHandler.obtainMessage();
        msg.obj = info;
        msg.what = added ? 2 : 3;
        this.mFakeActivityMsgHandler.sendMessage(msg);
    }

    private void sendInsertSuspectMsg(String compName) {
        Message msg = this.mFakeActivityMsgHandler.obtainMessage();
        msg.obj = compName;
        msg.what = 8;
        this.mFakeActivityMsgHandler.sendMessage(msg);
    }

    private void sendRecgFakeActivityMessage(String compName, boolean isScreenOn, int pid, int uid) {
        FakeActivityInfo appStartInfo = new FakeActivityInfo(compName, isScreenOn, pid, uid);
        Message msg = this.mFakeActivityMsgHandler.obtainMessage();
        msg.obj = appStartInfo;
        msg.what = 4;
        this.mFakeActivityMsgHandler.sendMessage(msg);
    }

    private void sendLoadAppTypeMessage() {
        Message msg = this.mFakeActivityMsgHandler.obtainMessage();
        msg.what = 1;
        this.mFakeActivityMsgHandler.sendMessageDelayed(msg, 2000);
    }

    private void sendSleepMessage() {
        Message msg = this.mFakeActivityMsgHandler.obtainMessage();
        msg.what = 6;
        this.mFakeActivityMsgHandler.sendMessage(msg);
    }

    private void sendStopSendEventMessage() {
        Message msg = this.mFakeActivityMsgHandler.obtainMessage();
        msg.what = 12;
        this.mFakeActivityMsgHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public void sendToBigDataMessage(String compName, String reason, int type) {
        if (type < 13 && type >= 0) {
            int[] iArr = mFakeTypes;
            iArr[type] = iArr[type] + 1;
        }
        FakeActivityInfo appStartInfo = new FakeActivityInfo(compName, reason);
        Message msg = this.mFakeActivityMsgHandler.obtainMessage();
        msg.obj = appStartInfo;
        msg.what = 10;
        this.mFakeActivityMsgHandler.sendMessage(msg);
    }

    private void sendForceStopMessage(String packageName) {
        Message msg = this.mFakeActivityMsgHandler.obtainMessage();
        msg.obj = packageName;
        msg.what = 11;
        this.mFakeActivityMsgHandler.sendMessage(msg);
    }

    private void sendRemoveThisTimeOfRecgMessage() {
        Message msg = this.mFakeActivityMsgHandler.obtainMessage();
        msg.what = 13;
        this.mFakeActivityMsgHandler.sendMessage(msg);
    }

    private void clearManyCachesAndStatus() {
        this.bNeedProcessTouchEventNotify.set(false);
        setGiveupRecognize(false, 0);
        notifyPowerkeyInteractive(false);
        setFingerprintWakeup(false);
        setPowerkeyWakeup(false);
        deleteAllSuspectedActivities();
        clearTouchEventPids();
        clearCrashUidPids();
        if (this.mHwInputManagerService != null) {
            this.mHwInputManagerService.responseTouchEvent(false);
        }
        clearTempFakeActivities();
    }

    /* access modifiers changed from: private */
    public void handleLongTimeScreenOn() {
        setSceenOnExpired(true);
        clearGiveupUids();
        clearManyCachesAndStatus();
    }

    /* access modifiers changed from: private */
    public void handleSleepMsg() {
        clearManyCachesAndStatus();
    }

    private void handleWakeup() {
        if (!suspectedActivityIsEmpty()) {
            this.bNeedProcessTouchEventNotify.set(true);
            if (this.mHwInputManagerService != null) {
                this.mHwInputManagerService.responseTouchEvent(true);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleStopNativeEventMsg() {
        this.bNeedProcessTouchEventNotify.set(false);
        if (this.mHwInputManagerService != null) {
            this.mHwInputManagerService.responseTouchEvent(false);
        }
    }

    /* access modifiers changed from: private */
    public void handleInsertSuspectMsg(String compName) {
        insertSuspectedActivity(compName, 1);
    }

    /* access modifiers changed from: private */
    public void handleReportAppUpdateMsg(Message msg) {
        int eventId = msg.arg1;
        Bundle args = msg.getData();
        if (eventId == 2) {
            String pkgName = args.getString(AwareUserHabit.USERHABIT_PACKAGE_NAME);
            int uid = args.getInt("uid");
            if (pkgName != null) {
                removeRecognizedActivitiesDueUninstall(pkgName, uid);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSendToBigdataMsg(FakeActivityInfo fakeInfo) {
        SRMSDumpRadar.getInstance().updateFakeData(fakeInfo.cmp, fakeInfo.reason);
    }

    /* access modifiers changed from: private */
    public void handleRemoveThisTimeOfRecg() {
        synchronized (this.mTempFakeActivities) {
            for (String compName : this.mTempFakeActivities) {
                removeFakeActivity(compName);
                AwareLog.i(TAG, "remove recognized fakeactivity for this time, remove " + compName);
                sendToBigDataMessage(compName, FAKEA_REMOVE_THIS_TIME_OF_RECG, 12);
            }
        }
        clearTempFakeActivities();
    }

    private void sendEventToInputDispatcher(int event) {
        if (event != 105) {
            switch (event) {
                case 100:
                    sendSleepMessage();
                    return;
                case 101:
                    handleWakeup();
                    return;
                default:
                    return;
            }
        } else {
            sendStopSendEventMessage();
        }
    }

    private void insertScreenonUids(int uid, long launchTime) {
        synchronized (this.mScreenonUids) {
            this.mScreenonUids.put(Integer.valueOf(uid), Long.valueOf(launchTime));
        }
    }

    private void deleteScreenonUids(int uid) {
        synchronized (this.mScreenonUids) {
            this.mScreenonUids.remove(Integer.valueOf(uid));
        }
    }

    private long getLastScreenonLaunchTime(int uid) {
        synchronized (this.mScreenonUids) {
            Long longValue = this.mScreenonUids.get(Integer.valueOf(uid));
            if (longValue == null) {
                return 0;
            }
            long longValue2 = longValue.longValue();
            return longValue2;
        }
    }

    private void deleteGiveupUids(int uid) {
        synchronized (this.mGiveupUids) {
            this.mGiveupUids.remove(Integer.valueOf(uid));
        }
    }

    private void insertGiveupUids(int uid) {
        synchronized (this.mGiveupUids) {
            if (!this.mGiveupUids.contains(Integer.valueOf(uid))) {
                this.mGiveupUids.add(Integer.valueOf(uid));
            }
        }
    }

    private boolean isInGiveupUids(int uid) {
        synchronized (this.mGiveupUids) {
            if (this.mGiveupUids.contains(Integer.valueOf(uid))) {
                return true;
            }
            return false;
        }
    }

    private void clearGiveupUids() {
        synchronized (this.mGiveupUids) {
            this.mGiveupUids.clear();
        }
    }

    private void insertTempFakeActivities(String compName) {
        synchronized (this.mTempFakeActivities) {
            this.mTempFakeActivities.add(compName);
        }
    }

    private void clearTempFakeActivities() {
        synchronized (this.mTempFakeActivities) {
            this.mTempFakeActivities.clear();
        }
    }

    private boolean isTmepFakeActivitiesEmpty() {
        boolean isEmpty;
        synchronized (this.mTempFakeActivities) {
            isEmpty = this.mTempFakeActivities.isEmpty();
        }
        return isEmpty;
    }

    private boolean setAndReturnWhetherNeedGiveup(int callerUid, int targetUid, long launchTime, boolean isScreenOn, String compName) {
        if (isInWhiteList(compName)) {
            return true;
        }
        if (isScreenOn) {
            insertScreenonUids(targetUid, launchTime);
            return false;
        } else if (callerUid != targetUid) {
            insertGiveupUids(targetUid);
            AwareLog.i(TAG, "insert giveupuids for fakeactivity in screenoff for different uid callerUid=" + callerUid + " appUid=" + targetUid + " for " + compName);
            insertScreenonUids(targetUid, launchTime);
            return true;
        } else if (isInGiveupUids(targetUid)) {
            return true;
        } else {
            long lastScreenonLaunchTime = getLastScreenonLaunchTime(targetUid);
            long saveTime = lastScreenonLaunchTime;
            if (lastScreenonLaunchTime == 0 || launchTime - saveTime > HwArbitrationDEFS.DelayTimeMillisA) {
                return false;
            }
            AwareLog.i(TAG, "insert giveupuids for fakeactivity interval=" + (launchTime - saveTime) + " sameuid=" + targetUid + " for " + compName);
            insertGiveupUids(targetUid);
            return true;
        }
    }

    private void setGiveupRecognize(boolean bGiveup, int giveupReason) {
        synchronized (mLock) {
            if (!bGiveup) {
                try {
                    if (this.bGiveupRecognize && System.currentTimeMillis() - this.mGiveupRecongnizeTimeMs > HwArbitrationDEFS.DelayTimeMillisA) {
                        this.bGiveupRecognize = false;
                        this.mGiveupReason = giveupReason;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                this.bGiveupRecognize = true;
                this.mGiveupRecongnizeTimeMs = System.currentTimeMillis();
                this.mGiveupReason = giveupReason;
            }
        }
    }

    private boolean isFingerprintWakeup() {
        return this.mIsFingerprintWakeup;
    }

    private boolean isPowerkeyWakeup() {
        return this.mIsPowerkeyWakeup;
    }

    private void setPowerkeyWakeup(boolean bWakeup) {
        this.mIsPowerkeyWakeup = bWakeup;
    }

    private boolean needGiveupRecgForAutoWakeup() {
        synchronized (mLock) {
            if (this.mIsPowerkeyInteractive) {
                this.bGiveupRecognize = true;
                this.mGiveupRecongnizeTimeMs = System.currentTimeMillis();
                this.mGiveupReason = 1;
                return true;
            } else if (this.bGiveupRecognize) {
                return true;
            } else {
                if (isPowerkeyWakeup()) {
                    return false;
                }
                this.bGiveupRecognize = true;
                this.mGiveupRecongnizeTimeMs = System.currentTimeMillis();
                this.mGiveupReason = 1;
                return true;
            }
        }
    }

    private void dumpScreenOnUids(PrintWriter pw) {
        long curTimeMs = System.currentTimeMillis();
        if (this.mGotoWakeupTimeMs > 0) {
            pw.println("current dumpTimeMs=" + curTimeMs + " wakuptmeMs=" + this.mGotoWakeupTimeMs + " has screenon=" + ((curTimeMs - this.mGotoWakeupTimeMs) / 1000) + StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX);
        }
        synchronized (this.mScreenonUids) {
            pw.println("mScreenonUids.size=" + this.mScreenonUids.size());
            for (Map.Entry entry : this.mScreenonUids.entrySet()) {
                pw.print("[" + ((Integer) entry.getKey()) + " savetimeToCurtime " + ((curTimeMs - ((Long) entry.getValue()).longValue()) / 1000) + "s] ");
            }
            pw.println("");
        }
    }

    private boolean isRecognizeFakeActivityEnabled() {
        return mRecgFakeActivityEnabled && this.mFakeActivityMsgHandler != null && this.mCurUserId == 0;
    }

    private void sendSceenOnLongTimeMsg() {
        setSceenOnExpired(false);
        this.mFakeActivityMsgHandler.removeMessages(5);
        this.mFakeActivityMsgHandler.sendEmptyMessageDelayed(5, HwArbitrationDEFS.DelayTimeMillisA);
    }

    private void removeSceenOnLongTimeMsg() {
        setSceenOnExpired(true);
        this.mFakeActivityMsgHandler.removeMessages(5);
    }

    private boolean removeOrRecgFakeActivity(boolean isScreenOn, int callerUid, int callerPid, int targetUid, String compName, long gotoSleepTimeMs) {
        boolean z = isScreenOn;
        int i = callerUid;
        int i2 = callerPid;
        String str = compName;
        boolean shouldPrevent = false;
        long curTimeMs = System.currentTimeMillis();
        if (isInFakeActivity(str) || !setAndReturnWhetherNeedGiveup(i, targetUid, curTimeMs, z, str)) {
            if (z) {
                long intervalMs = System.currentTimeMillis() - gotoSleepTimeMs;
                if (isInFakeActivity(str)) {
                    if (intervalMs <= FAKEACTIVITY_LAUNCH_WHEN_SCREENON_MS) {
                        AwareLog.i(TAG, str + " is fakea, cant be removed during screen-on and prevented now, intervalMs=" + intervalMs);
                        shouldPrevent = true;
                        sendToBigDataMessage(str, FAKEA_PREVENTED_WHEN_SCREEN_ON, 1);
                    } else if (removeFakeActivity(str)) {
                        AwareLog.i(TAG, "recognized fakeactivity is created during screen-on, remove it from fakecache:" + str + " fromsleeptonow=" + intervalMs + "ms");
                        sendToBigDataMessage(str, FAKEA_REMOVED_WHEN_SCREEN_ON, 0);
                    }
                }
            } else {
                shouldPrevent = preventFakeActivityOrPreProcess(str, i2, i);
                if (shouldPrevent) {
                    AwareLog.i(TAG, "Prevent " + str + " because of fakeactivity  screenon=" + z + " callerpid = " + i2);
                }
            }
            return shouldPrevent;
        }
        AwareLog.i(TAG, "give up recognize uid=" + i + " for " + str + " for fakeactivity because of giveupuids");
        sendToBigDataMessage(str, FAKEA_GIVEUP, 4);
        return false;
    }

    private boolean isInFakeActivity(String compName) {
        synchronized (this.mFakeActivities) {
            if (this.mFakeActivities.get(compName) != null) {
                return true;
            }
            return false;
        }
    }

    private void insertFakeActivityAndWriteDB(String compName, long insertType) {
        String[] strs = null;
        synchronized (this.mFakeActivities) {
            if (this.mFakeActivities.get(compName) == null) {
                strs = compName.split("/");
                this.mFakeActivities.put(compName, Long.valueOf(insertType));
                AwareLog.i(TAG, "adding fakeactivity " + compName + " to fakecache");
            }
        }
        if (strs != null && strs.length == 2) {
            sendUpdateCmpInfoMessage(strs[0], strs[1], 1, true);
        }
    }

    private void insertSuspectedAcitivtyUnlocked(String compName, int flag) {
        this.mSuspectedActivities.put(compName, Integer.valueOf(flag));
        AwareLog.i(TAG, "add " + compName + " to suspected cache for fakeactivity because of normal create");
    }

    /* access modifiers changed from: private */
    public void deleteSuspectedActivityByPkgName(String pkgname) {
        synchronized (this.mSuspectedActivities) {
            Iterator iter = this.mSuspectedActivities.entrySet().iterator();
            while (iter.hasNext()) {
                String compName = (String) iter.next().getKey();
                String[] strs = compName.split("/");
                if (strs.length == 2) {
                    if (strs[0].equals(pkgname)) {
                        iter.remove();
                        AwareLog.i(TAG, "remove " + compName + " for fakea by pkgname=" + pkgname);
                        sendToBigDataMessage(compName, FAKEA_FORCESTOP, 8);
                    }
                }
            }
        }
    }

    private void insertSuspectedActivity(String compName, int flag) {
        synchronized (this.mSuspectedActivities) {
            insertSuspectedAcitivtyUnlocked(compName, flag);
        }
    }

    private void deleteSuspectedActivity(String compName) {
        synchronized (this.mSuspectedActivities) {
            this.mSuspectedActivities.remove(compName);
        }
        AwareLog.i(TAG, "removing fakeactivity " + compName + " from suspected cache");
    }

    private void deleteAllSuspectedActivities() {
        synchronized (this.mSuspectedActivities) {
            this.mSuspectedActivities.clear();
        }
    }

    private boolean insertTouchEventPids(int pid, int touchType) {
        synchronized (this.mTouchEventPids) {
            if (this.mTouchEventPids.get(Integer.valueOf(pid)) != null) {
                return false;
            }
            this.mTouchEventPids.put(Integer.valueOf(pid), Integer.valueOf(touchType));
            return true;
        }
    }

    private boolean touchEventPidsIsEmpty() {
        boolean isEmpty;
        synchronized (this.mTouchEventPids) {
            isEmpty = this.mTouchEventPids.isEmpty();
        }
        return isEmpty;
    }

    private void clearTouchEventPids() {
        synchronized (this.mTouchEventPids) {
            this.mTouchEventPids.clear();
        }
    }

    private Integer getSuspectedActivityValue(String compName) {
        Integer num;
        synchronized (this.mSuspectedActivities) {
            num = this.mSuspectedActivities.get(compName);
        }
        return num;
    }

    private boolean preventFakeActivityOrPreProcess(String compName, int callerPid, int callerUid) {
        if (isInFakeActivity(compName)) {
            sendToBigDataMessage(compName, FAKEA_PREVENTED_WHEN_SCREEN_OFF, 2);
            return true;
        }
        sendInsertSuspectMsg(compName);
        return false;
    }

    private String getGiveupStringReason(int giveup) {
        switch (giveup) {
            case 0:
                return "no giveup";
            case 1:
                return "auto wakeup";
            case 2:
                return "multi wakeup";
            case 3:
                return "launch activity";
            case 4:
                return "key back";
            case 5:
                return "key home";
            default:
                return "unknow reason";
        }
    }

    /* access modifiers changed from: private */
    public void handleRecognizeFakeActivity(FakeActivityInfo startinfo) {
        String reason;
        String compName = startinfo.cmp;
        if (!getSceenOnExpired()) {
            if (isFingerprintWakeup()) {
                AwareLog.i(TAG, "give up recognize for " + compName + " for fakeactivity because of fingerprint wakeup");
                deleteSuspectedActivity(compName);
                sendToBigDataMessage(compName, FAKEA_FINGERPRINT, 10);
            } else if (isInCrashUidPids(startinfo.callerUid)) {
                AwareLog.i(TAG, "give up recognize for " + compName + " for fakeactivity because of crash");
                deleteSuspectedActivity(compName);
                deleteCrashUidPids(startinfo.callerUid);
                sendToBigDataMessage(compName, FAKEA_PROC_CRASH, 9);
            } else if (needGiveupRecgForAutoWakeup()) {
                synchronized (mLock) {
                    reason = getGiveupStringReason(this.mGiveupReason);
                }
                AwareLog.i(TAG, "give up recognize for " + compName + " for fakeactivity beause of " + reason);
                deleteSuspectedActivity(compName);
                sendToBigDataMessage(compName, FAKEA_AUTO_WAKEUP, 5);
            } else {
                boolean noTouchEvent = touchEventPidsIsEmpty();
                AwareLog.i(TAG, "noTouchEvent=" + noTouchEvent + " pid=" + startinfo.callerPid + " uid=" + startinfo.callerUid + " for fakea " + compName);
                if (!startinfo.isScreenOn || !noTouchEvent) {
                    AwareLog.i(TAG, compName + " is not fakeactivity because destroied after a touch");
                    sendToBigDataMessage(compName, FAKEA_HAS_TOUCH, 7);
                } else {
                    insertFakeActivityAndWriteDB(compName, 4);
                    sendToBigDataMessage(compName, FAKEA_IS_FAKE, 6);
                    insertTempFakeActivities(compName);
                }
                deleteSuspectedActivity(compName);
                if (suspectedActivityIsEmpty()) {
                    sendEventToInputDispatcher(105);
                }
            }
        }
    }

    private boolean suspectedActivityIsEmpty() {
        synchronized (this.mSuspectedActivities) {
            if (this.mSuspectedActivities.isEmpty()) {
                return true;
            }
            return false;
        }
    }

    private void setSceenOnExpired(boolean expired) {
        this.mScreenOnExpired.set(expired);
    }

    private boolean getSceenOnExpired() {
        return this.mScreenOnExpired.get();
    }

    private boolean removeFakeActivity(String compName) {
        synchronized (this.mFakeActivities) {
            if (this.mFakeActivities.get(compName) == null) {
                return false;
            }
            String[] strs = compName.split("/");
            if (strs.length != 2) {
                return false;
            }
            sendUpdateCmpInfoMessage(strs[0], strs[1], 1, false);
            this.mFakeActivities.remove(compName);
            return true;
        }
    }

    private void removeRecognizedActivitiesDueUninstall(String pkg, int uid) {
        deleteScreenonUids(uid);
        deleteGiveupUids(uid);
        boolean bRemoved = false;
        synchronized (this.mFakeActivities) {
            Iterator iter = this.mFakeActivities.entrySet().iterator();
            while (iter.hasNext()) {
                String compName = (String) iter.next().getKey();
                String[] strs = compName.split("/");
                if (strs.length == 2) {
                    if (pkg.equals(strs[0])) {
                        sendUpdateCmpInfoMessage(strs[0], strs[1], 1, false);
                        iter.remove();
                        AwareLog.i(TAG, "remove " + compName + " for fakea due to unstall");
                        sendToBigDataMessage(compName, FAKEA_UNINSTALL, 11);
                        bRemoved = true;
                    }
                }
            }
        }
        if (!bRemoved) {
            synchronized (this.mSuspectedActivities) {
                Iterator iter2 = this.mSuspectedActivities.entrySet().iterator();
                while (iter2.hasNext()) {
                    String compName2 = (String) iter2.next().getKey();
                    String[] strs2 = compName2.split("/");
                    if (strs2.length == 2) {
                        if (pkg.equals(strs2[0])) {
                            iter2.remove();
                            AwareLog.i(TAG, "remove " + compName2 + " for fakea due to unstall");
                            sendToBigDataMessage(compName2, FAKEA_UNINSTALL, 11);
                        }
                    }
                }
            }
        }
    }

    private void dumpSuspectedFakeActivities(PrintWriter pw) {
        pw.println("dump suspected fake activities begin:");
        synchronized (this.mSuspectedActivities) {
            for (String str : this.mSuspectedActivities.keySet()) {
                pw.println(str);
            }
        }
        pw.println("dump suspected fake activities end");
    }

    private void dumpFakeActivities(PrintWriter pw) {
        String strValue;
        pw.println("dump really recognized fake activities begin:");
        synchronized (this.mFakeActivities) {
            for (Map.Entry entry : this.mFakeActivities.entrySet()) {
                String keyStr = (String) entry.getKey();
                if (3 == ((Long) entry.getValue()).longValue()) {
                    strValue = "persistentfake";
                } else {
                    strValue = "dynamicfake";
                }
                pw.println(keyStr + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + strValue);
            }
        }
        pw.println("dump really recognized fake activities end");
    }

    private void dumpTouchEventPids(PrintWriter pw) {
        pw.println("dump process that has touchevent for suspected activity begin:");
        synchronized (this.mTouchEventPids) {
            pw.println("[pid touchtype]=" + this.mTouchEventPids);
        }
        pw.println("dump process that has touchevent for suspected activity end");
    }

    private void dumpBigdata(PrintWriter pw) {
        pw.println("removed-won=" + mFakeTypes[0]);
        pw.println("prevented-won=" + mFakeTypes[1]);
        pw.println("prevented-woff=" + mFakeTypes[2]);
        pw.println("suspected=" + mFakeTypes[3]);
        pw.println("giveup=" + mFakeTypes[4]);
        pw.println("autowakeup=" + mFakeTypes[5]);
        pw.println("isfake=" + mFakeTypes[6]);
        pw.println("hastouch=" + mFakeTypes[7]);
        pw.println("forcestop=" + mFakeTypes[8]);
        pw.println("proccrash=" + mFakeTypes[9]);
        pw.println("fingerprint=" + mFakeTypes[10]);
        pw.println("uninstall=" + mFakeTypes[11]);
        pw.println("remove-ttr=" + mFakeTypes[12]);
    }

    private void dumpVariables(PrintWriter pw) {
        pw.println("");
        dumpScreenOnUids(pw);
        synchronized (this.mGiveupUids) {
            pw.println("mGiveupUids.size=" + this.mGiveupUids.size());
            pw.println("mGiveupUids=" + this.mGiveupUids);
        }
        synchronized (this.mCrashUidPids) {
            pw.println("mCrashUidPids.size=" + this.mCrashUidPids.size());
            pw.println("mCrashUidPids=" + this.mCrashUidPids);
        }
        pw.println("mIsPowerkeyInteractive=" + this.mIsPowerkeyInteractive);
        synchronized (mLock) {
            pw.println("bGiveupRecognize=" + this.bGiveupRecognize + " reason=" + getGiveupStringReason(this.mGiveupReason));
            if (System.currentTimeMillis() - this.mGiveupRecongnizeTimeMs > HwArbitrationDEFS.DelayTimeMillisA) {
                pw.println("giveup recognize left=0s");
            } else {
                pw.println("giveup recognize left=" + (HwArbitrationDEFS.DelayTimeMillisA - (System.currentTimeMillis() - this.mGiveupRecongnizeTimeMs)) + "ms");
            }
        }
        pw.println("isFingerprintWakeup=" + isFingerprintWakeup());
        pw.println("isPowerkeyWakeup=" + isPowerkeyWakeup());
        dumpBigdata(pw);
    }

    private void dumpTempFakeActivities(PrintWriter pw) {
        pw.println("dump temp fake activities for this time of recognize begin:");
        synchronized (this.mTempFakeActivities) {
            for (String str : this.mTempFakeActivities) {
                pw.println(str);
            }
        }
        pw.println("dump temp fake activities end");
    }

    private void dumpWhiteListFakeActivities(PrintWriter pw) {
        pw.println("dump fake activities in white list begin:");
        synchronized (this.mWhiteFakeActivities) {
            for (String str : this.mWhiteFakeActivities) {
                pw.println(str);
            }
        }
        pw.println("dump fake activities in white list end");
    }

    private void insertCrashUidPids(int pid, int uid) {
        synchronized (this.mCrashUidPids) {
            this.mCrashUidPids.put(Integer.valueOf(uid), Integer.valueOf(pid));
        }
    }

    private void deleteCrashUidPids(int uid) {
        synchronized (this.mCrashUidPids) {
            this.mCrashUidPids.remove(Integer.valueOf(uid));
        }
    }

    private void clearCrashUidPids() {
        synchronized (this.mCrashUidPids) {
            this.mCrashUidPids.clear();
        }
    }

    private boolean isInCrashUidPids(int uid) {
        synchronized (this.mCrashUidPids) {
            if (this.mCrashUidPids.get(Integer.valueOf(uid)) != null) {
                return true;
            }
            return false;
        }
    }

    private void notifyGiveupRecgDueMultiWakeup() {
        synchronized (mLock) {
            this.bGiveupRecognize = true;
            this.mGiveupRecongnizeTimeMs = System.currentTimeMillis();
            this.mGiveupReason = 2;
        }
        if (!isTmepFakeActivitiesEmpty()) {
            sendRemoveThisTimeOfRecgMessage();
        }
    }

    public void dumpRecgFakeActivity(PrintWriter pw, String[] args) {
        if (pw != null) {
            if (!isRecognizeFakeActivityEnabled()) {
                pw.println("AwareFakeActivityRecg feature disabled.");
                return;
            }
            dumpSuspectedFakeActivities(pw);
            pw.println("\n");
            dumpFakeActivities(pw);
            pw.println("\n");
            dumpTouchEventPids(pw);
            pw.println("\n");
            dumpVariables(pw);
            pw.println("\n");
            dumpTempFakeActivities(pw);
            pw.println("\n");
            dumpWhiteListFakeActivities(pw);
        }
    }

    public void recognizeFakeActivity(String compName, boolean isScreenOn, int pid, int uid) {
        if (isRecognizeFakeActivityEnabled() && compName != null && !getSceenOnExpired() && isUserOwnerApp(uid) && getSuspectedActivityValue(compName) != null) {
            sendRecgFakeActivityMessage(compName, isScreenOn, pid, uid);
        }
    }

    public boolean shouldPreventStartActivity(ActivityInfo aInfo, int callerPid, int callerUid, boolean isScreenOn) {
        ActivityInfo activityInfo = aInfo;
        if (!isRecognizeFakeActivityEnabled() || activityInfo == null || !isUserOwnerApp(activityInfo.applicationInfo.uid)) {
            return false;
        }
        long start = 0;
        boolean shouldPrevent = false;
        if (this.DEBUG_COST) {
            start = System.nanoTime();
        }
        String compName = aInfo.getComponentName().flattenToShortString();
        if (compName == null) {
            return false;
        }
        if (isScreenOn && !getSceenOnExpired()) {
            setGiveupRecognize(true, 3);
        }
        if (needCheckPrevent(activityInfo.applicationInfo)) {
            shouldPrevent = removeOrRecgFakeActivity(isScreenOn, callerUid, callerPid, activityInfo.applicationInfo.uid, compName, this.mGotoSleepingTimeMs);
        }
        if (this.DEBUG_COST) {
            AwareLog.i(TAG, "shouldPreventStartActivity for fakea cost=" + ((System.nanoTime() - start) / 1000));
        }
        return shouldPrevent;
    }

    private boolean needCheckPrevent(ApplicationInfo applicationInfo) {
        if (applicationInfo == null || this.mDataMgr.isSystemBaseApp(applicationInfo)) {
            return false;
        }
        String pkgName = applicationInfo.packageName;
        if (pkgName != null && DecisionMaker.getInstance().getAppStartPolicy(pkgName, AppMngConstant.AppStartSource.THIRD_ACTIVITY) == 0) {
            return true;
        }
        return false;
    }

    public void processNativeEventNotify(int eventType, int eventValue, int keyAction, int pid, int uid) {
        if (isRecognizeFakeActivityEnabled()) {
            if (2 == eventType && keyAction == 0) {
                AwareLog.i(TAG, "receive keyevent " + eventValue + " action=" + keyAction + " for fakeactivity");
                if (eventValue != 26) {
                    switch (eventValue) {
                        case 3:
                            setGiveupRecognize(true, 5);
                            break;
                        case 4:
                            setGiveupRecognize(true, 4);
                            break;
                    }
                } else {
                    setPowerkeyWakeup(true);
                }
                return;
            }
            if (this.bNeedProcessTouchEventNotify.get() && 3 == eventType && insertTouchEventPids(pid, 12)) {
                AwareLog.i(TAG, "inputdispather reported pid=" + pid + ",uid=" + uid + " for fakeactivity");
            }
        }
    }

    public void setFingerprintWakeup(boolean bWakeup) {
        if (isRecognizeFakeActivityEnabled()) {
            this.mIsFingerprintWakeup = bWakeup;
        }
    }

    private void deInitialize() {
        synchronized (this.mScreenonUids) {
            this.mScreenonUids.clear();
        }
        clearGiveupUids();
        synchronized (this.mFakeActivities) {
            this.mFakeActivities.clear();
        }
        synchronized (this.mSuspectedActivities) {
            this.mSuspectedActivities.clear();
        }
        synchronized (this.mTouchEventPids) {
            this.mTouchEventPids.clear();
        }
        synchronized (this.mWhiteFakeActivities) {
            this.mWhiteFakeActivities.clear();
        }
        clearCrashUidPids();
        clearTempFakeActivities();
        this.mIsInitialized.set(false);
    }

    public void notifyPowerkeyInteractive(boolean bInteractive) {
        if (isRecognizeFakeActivityEnabled()) {
            this.mIsPowerkeyInteractive = bInteractive;
        }
    }

    public void notifyWakeupResult(boolean isWakenupThisTime) {
        AwareIntelligentRecg.getInstance().updateScreenOnFromPm(isWakenupThisTime);
        if (isRecognizeFakeActivityEnabled()) {
            if (isWakenupThisTime) {
                handleWakeup();
            } else {
                notifyGiveupRecgDueMultiWakeup();
            }
        }
    }

    public void onWakefulnessChanged(int wakefulness) {
        if (isRecognizeFakeActivityEnabled()) {
            if (1 == wakefulness) {
                AwareLog.i(TAG, "go to wake up for fakeactivity");
                this.mGotoWakeupTimeMs = System.currentTimeMillis();
                sendSceenOnLongTimeMsg();
                if (!suspectedActivityIsEmpty()) {
                    sendEventToInputDispatcher(101);
                }
            } else if (wakefulness == 0) {
                AwareLog.i(TAG, "go to sleep for fakeactivity");
                this.mGotoSleepingTimeMs = System.currentTimeMillis();
                sendEventToInputDispatcher(100);
                removeSceenOnLongTimeMsg();
            }
        }
    }

    public void setInputManagerService(HwInputManagerService inputManagerService) {
        this.mHwInputManagerService = inputManagerService;
    }

    public static void commEnable() {
        self().initialize();
        mRecgFakeActivityEnabled = true;
    }

    public static void commDisable() {
        mRecgFakeActivityEnabled = false;
        self().deInitialize();
    }

    public void reportAppUpdate(int eventId, Bundle args) {
        if (isRecognizeFakeActivityEnabled()) {
            Message msg = this.mFakeActivityMsgHandler.obtainMessage();
            msg.what = 9;
            msg.arg1 = eventId;
            msg.setData(args);
            this.mFakeActivityMsgHandler.sendMessage(msg);
        }
    }

    public void notifyProcessWillDie(boolean byForceStop, boolean crashed, boolean byAnr, String packageName, int pid, int uid) {
        if (isRecognizeFakeActivityEnabled()) {
            if (byForceStop && packageName != null) {
                sendForceStopMessage(packageName);
            } else if (crashed && pid > 0 && uid > 0) {
                insertCrashUidPids(pid, uid);
            }
        }
    }

    public void initUserSwitch(int userId) {
        if (mRecgFakeActivityEnabled) {
            this.mCurUserId = userId;
        }
    }

    private boolean isUserOwnerApp(int uid) {
        return UserHandle.getUserId(uid) == 0;
    }

    private void initWhiteList() {
        ArrayList<String> whiteList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), FAKEACTIVITY_WHITELIST);
        if (whiteList != null) {
            synchronized (this.mWhiteFakeActivities) {
                this.mWhiteFakeActivities.clear();
                this.mWhiteFakeActivities.addAll(whiteList);
            }
        }
    }

    private boolean isInWhiteList(String cmp) {
        if (cmp == null) {
            return false;
        }
        synchronized (this.mWhiteFakeActivities) {
            if (this.mWhiteFakeActivities.contains(cmp)) {
                return true;
            }
            return false;
        }
    }
}
