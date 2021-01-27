package com.android.server.rms.iaware.appmng;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfoEx;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CmpTypeInfo;
import android.rms.iaware.IAwareCMSManager;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.mtm.iaware.appmng.AwareProcessState;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.AppStartupDataMgr;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.android.server.rms.iaware.cpu.NetManager;
import com.android.server.rms.iaware.srms.SrmsDumpRadar;
import com.huawei.android.app.ActivityManagerNativeExt;
import com.huawei.android.content.pm.UserInfoExAdapter;
import com.huawei.android.os.UserHandleEx;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
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
    private static final int CLS_INDEX = 1;
    private static final int CMP_LENGTH = 2;
    private static final long DELAY_LONGTIME_SCREENON_MS = 30000;
    private static final String FAKEA_ACTIVITY_WHITE_LIST = "fake_white";
    private static final String FAKEA_AUTO_WAKEUP = "autowakeup";
    private static final String FAKEA_FINGER_PRINT = "fingerprint";
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
    private static final String FAKEA_SCEEN_UNLOCKED = "scr-unlocked";
    private static final String FAKEA_UNINSTALL = "uninstall";
    private static final long FAKE_ACTIVITY_LAUNCH_WHEN_SCREENON_MS = 5000;
    private static final long FAKE_ACTIVIT_IS_DYNAMIC = 4;
    private static final long FAKE_ACTIVIT_IS_PERSISTENT = 3;
    private static final int GIVEUP_RECOGNIZE_AUTO_WAKEUP = 1;
    private static final int GIVEUP_RECOGNIZE_KEY_CODE_BACK = 4;
    private static final int GIVEUP_RECOGNIZE_KEY_CODE_HOME = 5;
    private static final int GIVEUP_RECOGNIZE_LAUNCH_ACTIVITY = 3;
    private static final int GIVEUP_RECOGNIZE_MULTI_WAKEUP = 2;
    private static final int GIVEUP_RECOGNIZE_NO_GIVEUP = 0;
    private static final int GIVEUP_TIME_FOR_DEFUALT_WAKEUP_MS = 30000;
    private static final long GIVEUP_TIME_FOR_FAST_SCREEN_OFF_MS = 30000;
    private static final int INSERT_SUS_PKGNAME_CACHE_NORMAL_CREATE = 1;
    private static final int JAVA_MSG_SLEEP = 100;
    private static final int JAVA_MSG_STOP_SEND = 105;
    private static final int JAVA_MSG_WAKEUP = 101;
    private static final int LOAD_CMPTYPE_MESSAGE_DELAY_MS = 2000;
    private static final Object LOCK = new Object();
    private static final int MSG_DELETE_DATA = 3;
    private static final int MSG_FORCE_STOP = 11;
    private static final int MSG_INSERT_DATA = 2;
    private static final int MSG_INSERT_SUSPECTED_FAKE_ACTIVITY = 8;
    private static final int MSG_LOAD_DATA = 1;
    private static final int MSG_RECG_FAKE_ACTIVITY = 4;
    private static final int MSG_RECG_LONGTIME_SCREENON = 5;
    private static final int MSG_REMOVE_THIS_TIME_OF_RECG = 13;
    private static final int MSG_REPORT_APP_UPDATE = 9;
    private static final int MSG_SENDTO_BIG_DATA = 10;
    private static final int MSG_SLEEP = 6;
    private static final int MSG_STOP_SEND_EVENT = 12;
    private static final int MSG_WAKEUP = 7;
    private static final int NATIVE_EVENT_KEY = 2;
    private static final int NATIVE_EVENT_MOTION = 3;
    private static final int PKG_INDEX = 0;
    private static final String TAG = "AwareFakeActivityRecg";
    private static final int TOUCH_EVENT_FROM_APP = 12;
    private static final int TYPE_FAKEA_AUTO_WAKEUP = 5;
    private static final int TYPE_FAKEA_FINGER_PRINT = 10;
    private static final int TYPE_FAKEA_FORCE_STOP = 8;
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
    private static final int TYPE_SCEEN_UNLOCKED = 14;
    private static AwareFakeActivityRecg sFakeActivityRecg = null;
    private static int[] sFakeTypes = new int[13];
    private static boolean sRecgFakeActivityEnabled = true;
    private final HashMap<Integer, Integer> mCrashUidPids = new HashMap<>();
    private int mCurUserId = 0;
    private final AtomicInteger mDataLoadCount = new AtomicInteger(2);
    private AppStartupDataMgr mDataMgr = new AppStartupDataMgr();
    private boolean mDebugCost = false;
    private final HashMap<String, Long> mFakeActivities = new HashMap<>();
    private FakeActivityRecgHandler mFakeActivityMsgHandler = null;
    private int mGiveupReason = 0;
    private boolean mGiveupRecognize = false;
    private long mGiveupRecongnizeTimeMs = 0;
    private final Set<Integer> mGiveupUids = new HashSet();
    private long mGotoSleepingTimeMs = 0;
    private long mGotoWakeupTimeMs = 0;
    private InputManagerServiceEx mHwInputManagerService = null;
    private boolean mIsFingerprintWakeup = false;
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);
    private boolean mIsPowerkeyInteractive = false;
    private boolean mIsPowerkeyWakeup = false;
    private boolean mIsScreenLocked = true;
    private AtomicBoolean mNeedProcessTouchEventNotify = new AtomicBoolean(false);
    private AtomicBoolean mScreenOnExpired = new AtomicBoolean(false);
    private final HashMap<Integer, Long> mScreenonUids = new HashMap<>();
    private final HashMap<String, Integer> mSuspectedActivities = new HashMap<>();
    private final Set<String> mTempFakeActivities = new HashSet();
    private final HashMap<Integer, Integer> mTouchEventPids = new HashMap<>();
    private final Set<String> mWhiteFakeActivities = new HashSet();

    /* access modifiers changed from: private */
    public static class FakeActivityInfo {
        public int callerPid;
        public int callerUid;
        public String cmp;
        public boolean isScreenOn;
        private String reason;

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

    /* access modifiers changed from: private */
    public class FakeActivityRecgHandler extends Handler {
        public FakeActivityRecgHandler(Looper looper) {
            super(looper);
        }

        private void handleMessageBecauseOfNsiq(Message msg) {
            int i = msg.what;
            if (i != 1) {
                CmpTypeInfo deleteCmpInfo = null;
                if (i == 2) {
                    if (msg.obj instanceof CmpTypeInfo) {
                        deleteCmpInfo = (CmpTypeInfo) msg.obj;
                    }
                    AwareFakeActivityRecg.this.insertFakeActivityToDb(deleteCmpInfo);
                } else if (i == 3) {
                    if (msg.obj instanceof CmpTypeInfo) {
                        deleteCmpInfo = (CmpTypeInfo) msg.obj;
                    }
                    AwareFakeActivityRecg.this.deleteFakeActivityFromDb(deleteCmpInfo);
                } else if (i == 9) {
                    AwareFakeActivityRecg.this.handleReportAppUpdateMsg(msg);
                } else if (i == 13) {
                    AwareFakeActivityRecg.this.handleRemoveThisTimeOfRecg();
                }
            } else {
                AwareFakeActivityRecg.this.initRecognizedFakeActivity();
            }
        }

        @Override // android.os.Handler
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
                case 7:
                case AwareProcessState.STATE_FOREGROUND /* 9 */:
                default:
                    handleMessageBecauseOfNsiq(msg);
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
                case NetManager.MSG_NET_GAME_ENABLE /* 12 */:
                    AwareFakeActivityRecg.this.handleStopNativeEventMsg();
                    return;
            }
        }
    }

    public static AwareFakeActivityRecg self() {
        AwareFakeActivityRecg awareFakeActivityRecg;
        synchronized (LOCK) {
            if (sFakeActivityRecg == null) {
                sFakeActivityRecg = new AwareFakeActivityRecg();
            }
            awareFakeActivityRecg = sFakeActivityRecg;
        }
        return awareFakeActivityRecg;
    }

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            if (this.mFakeActivityMsgHandler == null) {
                initHandler();
            }
            try {
                UserInfoExAdapter currentUser = ActivityManagerNativeExt.getCurrentUser();
                if (currentUser != null) {
                    this.mCurUserId = currentUser.getUserId();
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
    /* access modifiers changed from: public */
    private boolean insertFakeActivityToDb(CmpTypeInfo info) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                IAwareCMSManager.insertCmpTypeInfo(awareService, info);
                return true;
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return false;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "inSertCmpRecgInfo RemoteException");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean deleteFakeActivityFromDb(CmpTypeInfo info) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                IAwareCMSManager.deleteCmpTypeInfo(awareService, info);
                return true;
            }
            AwareLog.e(TAG, "can not find service IAwareCMSService.");
            return false;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "deleteCmpRecgInfo RemoteException");
            return false;
        }
    }

    private boolean loadFakeActivityInfo() {
        List<CmpTypeInfo> list = null;
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                list = IAwareCMSManager.getCmpTypeList(awareService);
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
                    loadFakeActivityInfoInternal(comp);
                }
            }
        }
        return true;
    }

    private void loadFakeActivityInfoInternal(String comp) {
        synchronized (this.mFakeActivities) {
            if (this.mFakeActivities.get(comp) == null) {
                AwareLog.i(TAG, "load fakeactivity from db:" + comp);
                this.mFakeActivities.put(comp, Long.valueOf((long) FAKE_ACTIVIT_IS_DYNAMIC));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initRecognizedFakeActivity() {
        if (loadFakeActivityInfo()) {
            AwareLog.i(TAG, "load fakea from db OK ");
        } else if (this.mDataLoadCount.get() >= 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("send load fakea from db again mDataLoadCount=");
            sb.append(this.mDataLoadCount.get() - 1);
            AwareLog.i(TAG, sb.toString());
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
    /* access modifiers changed from: public */
    private void sendToBigDataMessage(String compName, String reason, int type) {
        if (type < 13 && type >= 0) {
            int[] iArr = sFakeTypes;
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
        this.mNeedProcessTouchEventNotify.set(false);
        setGiveupRecognize(false, 0);
        notifyPowerkeyInteractive(false);
        setFingerprintWakeup(false);
        setPowerkeyWakeup(false);
        deleteAllSuspectedActivities();
        clearTouchEventPids();
        clearCrashUidPids();
        InputManagerServiceEx inputManagerServiceEx = this.mHwInputManagerService;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.responseTouchEvent(false);
        }
        clearTempFakeActivities();
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mFakeActivityMsgHandler = new FakeActivityRecgHandler(looper);
        } else {
            this.mFakeActivityMsgHandler = new FakeActivityRecgHandler(BackgroundThreadEx.getLooper());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLongTimeScreenOn() {
        setSceenOnExpired(true);
        clearGiveupUids();
        clearManyCachesAndStatus();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSleepMsg() {
        clearManyCachesAndStatus();
        setScreenLockState(true);
    }

    private void handleWakeup() {
        if (!suspectedActivityIsEmpty()) {
            this.mNeedProcessTouchEventNotify.set(true);
            InputManagerServiceEx inputManagerServiceEx = this.mHwInputManagerService;
            if (inputManagerServiceEx != null) {
                inputManagerServiceEx.responseTouchEvent(true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStopNativeEventMsg() {
        this.mNeedProcessTouchEventNotify.set(false);
        InputManagerServiceEx inputManagerServiceEx = this.mHwInputManagerService;
        if (inputManagerServiceEx != null) {
            inputManagerServiceEx.responseTouchEvent(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleInsertSuspectMsg(String compName) {
        insertSuspectedActivity(compName, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReportAppUpdateMsg(Message msg) {
        int eventId = msg.arg1;
        Bundle args = msg.getData();
        if (eventId == 2) {
            String pkgName = args.getString(AwareUserHabit.USER_HABIT_PACKAGE_NAME);
            int uid = args.getInt("uid");
            if (pkgName != null) {
                removeRecognizedActivitiesDueUninstall(pkgName, uid);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSendToBigdataMsg(FakeActivityInfo fakeInfo) {
        SrmsDumpRadar.getInstance().updateFakeData(fakeInfo.cmp, fakeInfo.reason);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRemoveThisTimeOfRecg() {
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
        if (event == 100) {
            sendSleepMessage();
        } else if (event == 101) {
            handleWakeup();
        } else if (event == 105) {
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
            return longValue.longValue();
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
        }
        long saveTime = getLastScreenonLaunchTime(targetUid);
        if (callerUid != targetUid) {
            insertGiveupUids(targetUid);
            AwareLog.i(TAG, "insert giveupuids for fakeactivity in screenoff for different uid callerUid=" + callerUid + " appUid=" + targetUid + " for " + compName);
            insertScreenonUids(targetUid, launchTime);
            return true;
        } else if (isInGiveupUids(targetUid)) {
            return true;
        } else {
            if (saveTime == 0 || launchTime - saveTime > 30000) {
                return false;
            }
            AwareLog.i(TAG, "insert giveupuids for fakeactivity interval=" + (launchTime - saveTime) + " sameuid=" + targetUid + " for " + compName);
            insertGiveupUids(targetUid);
            return true;
        }
    }

    private void setGiveupRecognize(boolean giveup, int giveupReason) {
        synchronized (LOCK) {
            if (giveup) {
                this.mGiveupRecognize = true;
                this.mGiveupRecongnizeTimeMs = System.currentTimeMillis();
                this.mGiveupReason = giveupReason;
            } else if (this.mGiveupRecognize && System.currentTimeMillis() - this.mGiveupRecongnizeTimeMs > 30000) {
                this.mGiveupRecognize = false;
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
        synchronized (LOCK) {
            if (this.mIsPowerkeyInteractive) {
                this.mGiveupRecognize = true;
                this.mGiveupRecongnizeTimeMs = System.currentTimeMillis();
                this.mGiveupReason = 1;
                return true;
            } else if (this.mGiveupRecognize) {
                return true;
            } else {
                if (isPowerkeyWakeup()) {
                    return false;
                }
                this.mGiveupRecognize = true;
                this.mGiveupRecongnizeTimeMs = System.currentTimeMillis();
                this.mGiveupReason = 1;
                return true;
            }
        }
    }

    private void dumpScreenOnUids(PrintWriter pw) {
        long curTimeMs = System.currentTimeMillis();
        if (this.mGotoWakeupTimeMs > 0) {
            pw.println("current dumpTimeMs=" + curTimeMs + " wakuptmeMs=" + this.mGotoWakeupTimeMs + " has screenon=" + ((curTimeMs - this.mGotoWakeupTimeMs) / 1000) + "s");
        }
        synchronized (this.mScreenonUids) {
            pw.println("mScreenonUids.size=" + this.mScreenonUids.size());
            for (Map.Entry<Integer, Long> entry : this.mScreenonUids.entrySet()) {
                pw.print("[" + entry.getKey() + " savetimeToCurtime " + ((curTimeMs - entry.getValue().longValue()) / 1000) + "s] ");
            }
            pw.println("");
        }
    }

    private boolean isRecognizeFakeActivityEnabled() {
        return sRecgFakeActivityEnabled && this.mFakeActivityMsgHandler != null && this.mCurUserId == 0;
    }

    private void sendSceenOnLongTimeMsg() {
        setSceenOnExpired(false);
        this.mFakeActivityMsgHandler.removeMessages(5);
        this.mFakeActivityMsgHandler.sendEmptyMessageDelayed(5, 30000);
    }

    private void removeSceenOnLongTimeMsg() {
        setSceenOnExpired(true);
        this.mFakeActivityMsgHandler.removeMessages(5);
    }

    private void sendBigDataAfterRemove(String compName, long intervalMs) {
        if (removeFakeActivity(compName)) {
            AwareLog.i(TAG, "recognized fakeactivity is created during screen-on, remove it from fakecache:" + compName + " fromsleeptonow=" + intervalMs + "ms");
            sendToBigDataMessage(compName, FAKEA_REMOVED_WHEN_SCREEN_ON, 0);
        }
    }

    private boolean removeOrRecgFakeActivity(FakeActivityInfo fakeActivityInfo, int targetUid, long gotoSleepTimeMs) {
        String str;
        String compName = fakeActivityInfo.cmp;
        boolean isScreenOn = fakeActivityInfo.isScreenOn;
        int callerUid = fakeActivityInfo.callerUid;
        int callerPid = fakeActivityInfo.callerPid;
        long curTimeMs = System.currentTimeMillis();
        if (!isInFakeActivity(compName)) {
            str = TAG;
            if (setAndReturnWhetherNeedGiveup(callerUid, targetUid, curTimeMs, isScreenOn, compName)) {
                AwareLog.i(str, "give up recognize uid=" + callerUid + " for " + compName + " for fakeactivity because of giveupuids");
                sendToBigDataMessage(compName, FAKEA_GIVEUP, 4);
                return false;
            }
        } else {
            str = TAG;
        }
        if (isScreenOn) {
            long intervalMs = System.currentTimeMillis() - gotoSleepTimeMs;
            if (!isInFakeActivity(compName)) {
                return false;
            }
            if (intervalMs > FAKE_ACTIVITY_LAUNCH_WHEN_SCREENON_MS) {
                sendBigDataAfterRemove(compName, intervalMs);
                return false;
            }
            AwareLog.i(str, compName + " is fakea, cant be removed during screen-on and prevented now, intervalMs=" + intervalMs);
            sendToBigDataMessage(compName, FAKEA_PREVENTED_WHEN_SCREEN_ON, 1);
            return true;
        }
        boolean shouldPrevent = preventFakeActivityOrPreProcess(compName, callerPid, callerUid);
        if (!shouldPrevent) {
            return shouldPrevent;
        }
        AwareLog.i(str, "Prevent " + compName + " because of fakeactivity  screenon=" + isScreenOn + " callerpid = " + callerPid);
        return shouldPrevent;
    }

    private boolean isInFakeActivity(String compName) {
        synchronized (this.mFakeActivities) {
            if (this.mFakeActivities.get(compName) != null) {
                return true;
            }
            return false;
        }
    }

    private void insertFakeActivityAndWriteDb(String compName, long insertType) {
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
    /* access modifiers changed from: public */
    private void deleteSuspectedActivityByPkgName(String pkgname) {
        synchronized (this.mSuspectedActivities) {
            Iterator<Map.Entry<String, Integer>> iter = this.mSuspectedActivities.entrySet().iterator();
            while (iter.hasNext()) {
                String compName = iter.next().getKey();
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
        if (giveup == 0) {
            return "no giveup";
        }
        if (giveup == 1) {
            return "auto wakeup";
        }
        if (giveup == 2) {
            return "multi wakeup";
        }
        if (giveup == 3) {
            return "launch activity";
        }
        if (giveup == 4) {
            return "key back";
        }
        if (giveup != 5) {
            return "unknow reason";
        }
        return "key home";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRecognizeFakeActivity(FakeActivityInfo startinfo) {
        String reason;
        String compName = startinfo.cmp;
        if (!getSceenOnExpired()) {
            if (isFingerprintWakeup()) {
                AwareLog.i(TAG, "give up recognize for " + compName + " for fakeactivity because of fingerprint wakeup");
                deleteSuspectedActivity(compName);
                sendToBigDataMessage(compName, FAKEA_FINGER_PRINT, 10);
            } else if (!this.mIsScreenLocked) {
                AwareLog.i(TAG, "give up recognize for " + compName + " for fakeactivity because of screen unlocked");
                deleteSuspectedActivity(compName);
                sendToBigDataMessage(compName, FAKEA_SCEEN_UNLOCKED, 14);
            } else if (isInCrashUidPids(startinfo.callerUid)) {
                AwareLog.i(TAG, "give up recognize for " + compName + " for fakeactivity because of crash");
                deleteSuspectedActivity(compName);
                deleteCrashUidPids(startinfo.callerUid);
                sendToBigDataMessage(compName, FAKEA_PROC_CRASH, 9);
            } else if (needGiveupRecgForAutoWakeup()) {
                synchronized (LOCK) {
                    reason = getGiveupStringReason(this.mGiveupReason);
                }
                AwareLog.i(TAG, "give up recognize for " + compName + " for fakeactivity beause of " + reason);
                deleteSuspectedActivity(compName);
                sendToBigDataMessage(compName, FAKEA_AUTO_WAKEUP, 5);
            } else {
                boolean isTouchEventEmpty = touchEventPidsIsEmpty();
                AwareLog.i(TAG, "isTouchEventEmpty=" + isTouchEventEmpty + " pid=" + startinfo.callerPid + " uid=" + startinfo.callerUid + " for fakea " + compName);
                if (!startinfo.isScreenOn || !isTouchEventEmpty) {
                    AwareLog.i(TAG, compName + " is not fakeactivity because destroied after a touch");
                    sendToBigDataMessage(compName, FAKEA_HAS_TOUCH, 7);
                } else {
                    insertFakeActivityAndWriteDb(compName, FAKE_ACTIVIT_IS_DYNAMIC);
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
            Iterator<Map.Entry<String, Long>> iter = this.mFakeActivities.entrySet().iterator();
            while (iter.hasNext()) {
                String compName = iter.next().getKey();
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
                Iterator<Map.Entry<String, Integer>> iter2 = this.mSuspectedActivities.entrySet().iterator();
                while (iter2.hasNext()) {
                    String compName2 = iter2.next().getKey();
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
            for (Map.Entry<String, Long> entry : this.mFakeActivities.entrySet()) {
                String keyStr = entry.getKey();
                if (FAKE_ACTIVIT_IS_PERSISTENT == entry.getValue().longValue()) {
                    strValue = "persistentfake";
                } else {
                    strValue = "dynamicfake";
                }
                pw.println(keyStr + CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + strValue);
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
        pw.println("removed-won=" + sFakeTypes[0]);
        pw.println("prevented-won=" + sFakeTypes[1]);
        pw.println("prevented-woff=" + sFakeTypes[2]);
        pw.println("suspected=" + sFakeTypes[3]);
        pw.println("giveup=" + sFakeTypes[4]);
        pw.println("autowakeup=" + sFakeTypes[5]);
        pw.println("isfake=" + sFakeTypes[6]);
        pw.println("hastouch=" + sFakeTypes[7]);
        pw.println("forcestop=" + sFakeTypes[8]);
        pw.println("proccrash=" + sFakeTypes[9]);
        pw.println("fingerprint=" + sFakeTypes[10]);
        pw.println("uninstall=" + sFakeTypes[11]);
        pw.println("remove-ttr=" + sFakeTypes[12]);
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
        synchronized (LOCK) {
            pw.println("mGiveupRecognize=" + this.mGiveupRecognize + " reason=" + getGiveupStringReason(this.mGiveupReason));
            if (System.currentTimeMillis() - this.mGiveupRecongnizeTimeMs > 30000) {
                pw.println("giveup recognize left=0s");
            } else {
                pw.println("giveup recognize left=" + (30000 - (System.currentTimeMillis() - this.mGiveupRecongnizeTimeMs)) + "ms");
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
        synchronized (LOCK) {
            this.mGiveupRecognize = true;
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
            String separator = System.lineSeparator();
            dumpSuspectedFakeActivities(pw);
            pw.println(separator);
            dumpFakeActivities(pw);
            pw.println(separator);
            dumpTouchEventPids(pw);
            pw.println(separator);
            dumpVariables(pw);
            pw.println(separator);
            dumpTempFakeActivities(pw);
            pw.println(separator);
            dumpWhiteListFakeActivities(pw);
        }
    }

    public void recognizeFakeActivity(String compName, int pid, int uid) {
        if (isRecognizeFakeActivityEnabled() && compName != null && !getSceenOnExpired() && isUserOwnerApp(uid) && getSuspectedActivityValue(compName) != null) {
            sendRecgFakeActivityMessage(compName, AwareWakeUpManager.getInstance().isScreenOn(), pid, uid);
        }
    }

    public boolean shouldPreventStartActivity(ActivityInfo activityInfo, int callerPid, int callerUid) {
        if (!isRecognizeFakeActivityEnabled() || activityInfo == null || !isUserOwnerApp(activityInfo.applicationInfo.uid)) {
            return false;
        }
        long start = 0;
        boolean shouldPrevent = false;
        if (this.mDebugCost) {
            start = System.nanoTime();
        }
        String compName = ActivityInfoEx.getComponentName(activityInfo).flattenToShortString();
        if (compName == null) {
            return false;
        }
        boolean isScreenOn = AwareWakeUpManager.getInstance().isScreenOn();
        if (isScreenOn && !getSceenOnExpired()) {
            setGiveupRecognize(true, 3);
        }
        if (needCheckPrevent(activityInfo.applicationInfo)) {
            shouldPrevent = removeOrRecgFakeActivity(new FakeActivityInfo(compName, isScreenOn, callerPid, callerUid), activityInfo.applicationInfo.uid, this.mGotoSleepingTimeMs);
        }
        if (this.mDebugCost) {
            AwareLog.i(TAG, "shouldPreventStartActivity for fakea cost=" + ((System.nanoTime() - start) / 1000));
        }
        return shouldPrevent;
    }

    private boolean needCheckPrevent(ApplicationInfo applicationInfo) {
        String pkgName;
        if (applicationInfo != null && !this.mDataMgr.isSystemBaseApp(applicationInfo) && (pkgName = applicationInfo.packageName) != null && DecisionMaker.getInstance().getAppStartPolicy(pkgName, AppMngConstant.AppStartSource.THIRD_ACTIVITY) == 0) {
            return true;
        }
        return false;
    }

    public void processNativeEventNotify(int eventType, int eventValue, int keyAction, int pid, int uid) {
        if (isRecognizeFakeActivityEnabled()) {
            if (eventType == 2 && keyAction == 0) {
                AwareLog.i(TAG, "receive keyevent " + eventValue + " action=" + keyAction + " for fakeactivity");
                if (eventValue == 3) {
                    setGiveupRecognize(true, 5);
                } else if (eventValue == 4) {
                    setGiveupRecognize(true, 4);
                } else if (eventValue == 26) {
                    setPowerkeyWakeup(true);
                }
            } else if (this.mNeedProcessTouchEventNotify.get() && eventType == 3 && insertTouchEventPids(pid, 12)) {
                AwareLog.i(TAG, "inputdispather reported pid=" + pid + ",uid=" + uid + " for fakeactivity");
            }
        }
    }

    public void setFingerprintWakeup(boolean isWakeup) {
        if (isRecognizeFakeActivityEnabled()) {
            this.mIsFingerprintWakeup = isWakeup;
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

    public void notifyPowerkeyInteractive(boolean isInteractive) {
        if (isRecognizeFakeActivityEnabled()) {
            this.mIsPowerkeyInteractive = isInteractive;
        }
    }

    public void notifyWakeupResult(boolean isWakenupThisTime) {
        if (Looper.myLooper() != null) {
            AwareIntelligentRecg.getInstance().updateScreenOnFromPm(isWakenupThisTime);
            if (isRecognizeFakeActivityEnabled()) {
                if (isWakenupThisTime) {
                    handleWakeup();
                } else {
                    notifyGiveupRecgDueMultiWakeup();
                }
            }
        }
    }

    public void onWakefulnessChanged(int wakefulness) {
        if (isRecognizeFakeActivityEnabled()) {
            if (wakefulness == 1) {
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

    public void setInputManagerService(InputManagerServiceEx inputManagerService) {
        this.mHwInputManagerService = inputManagerService;
    }

    public static void commEnable() {
        self().initialize();
        sRecgFakeActivityEnabled = true;
    }

    public static void commDisable() {
        sRecgFakeActivityEnabled = false;
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

    public void notifyProcessWillDie(boolean[] dieReasons, String packageName, int pid, int uid) {
        if (dieReasons.length == 3) {
            boolean byForceStop = dieReasons[0];
            boolean crashed = dieReasons[1];
            boolean z = dieReasons[2];
            if (isRecognizeFakeActivityEnabled()) {
                if (byForceStop && packageName != null) {
                    sendForceStopMessage(packageName);
                } else if (crashed && pid > 0 && uid > 0) {
                    insertCrashUidPids(pid, uid);
                }
            }
        }
    }

    public void initUserSwitch(int userId) {
        if (sRecgFakeActivityEnabled) {
            this.mCurUserId = userId;
        }
    }

    private boolean isUserOwnerApp(int uid) {
        return UserHandleEx.getUserId(uid) == 0;
    }

    private void initWhiteList() {
        ArrayList<String> whiteList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_START.getDesc(), FAKEA_ACTIVITY_WHITE_LIST);
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

    public void setScreenLockState(boolean state) {
        if (isRecognizeFakeActivityEnabled()) {
            this.mIsScreenLocked = state;
        }
    }
}
