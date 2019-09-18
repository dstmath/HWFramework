package com.android.server.rms.iaware.hiber;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.os.BackgroundThread;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.iaware.feature.MemoryFeature2;
import com.android.server.rms.iaware.hiber.bean.AbsAppInfo;
import com.android.server.rms.iaware.hiber.bean.HiberAppInfo;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.hiber.constant.EReclaimResult;
import com.android.server.rms.iaware.hiber.util.AppHiberUtil;
import com.android.server.rms.iaware.memory.action.GpuCompressAction;
import com.android.server.rms.iaware.memory.utils.CpuReader;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.huawei.pgmng.plug.PGSdk;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AppHibernateTask {
    private static final AbsAppInfo INVALID_ABSAPPINFO = new AbsAppInfo(-1, "");
    private static final String TAG_PG = "AppHiber_Task";
    private static AppHibernateTask mAppHibernateTask = null;
    private static final Object mLock = new Object();
    /* access modifiers changed from: private */
    public ArraySet<AbsAppInfo> frzHashSet = new ArraySet<>();
    private final Semaphore frzSemaphore = new Semaphore(1);
    private AppHibernateMgr mAppHiberMgr = AppHibernateMgr.getInstance();
    private Context mContext;
    private CpuReader mCpuReader = CpuReader.getInstance();
    private AbsAppInfo mCurFrontAbsApp = INVALID_ABSAPPINFO;
    private Handler mHiberEventHandler = new HiberHanldler(BackgroundThread.get().getLooper());
    private final AtomicBoolean mIsScreenOff = new AtomicBoolean(false);
    private int mLastInputEvent = 0;
    private long mLastInputTime = 0;
    private long mLastResEventTime = 0;
    private PGSdk mPGSdk = null;
    private ArrayMap<AbsAppInfo, ArraySet<HiberAppInfo>> mReclaimedRecordMap = new ArrayMap<>();
    private AbsAppInfo mReclaimingApp = INVALID_ABSAPPINFO;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private PGSdk.Sink mStateRecognitionListener = new PGSdk.Sink() {
        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("onStateChanged    Enter  ");
            if (eventType == 1) {
                str = "FRZ CallBK: ";
            } else {
                str = "THW CallBK: ";
            }
            sb.append(str);
            sb.append(", pkg[");
            sb.append(pkg);
            sb.append("], uid= ");
            sb.append(uid);
            sb.append(", pid= ");
            sb.append(pid);
            AwareLog.d(AppHibernateTask.TAG_PG, sb.toString());
            if (stateType != 6) {
                AwareLog.i(AppHibernateTask.TAG_PG, "stateType" + stateType + " != STATE_HIBERNATE, return");
            } else if (AppHiberUtil.isStrEmpty(pkg)) {
                AwareLog.i(AppHibernateTask.TAG_PG, "null == pkg || pkg.trim().isEmpty()");
            } else if (AppHiberUtil.illegalUid(uid)) {
                AwareLog.i(AppHibernateTask.TAG_PG, "uid = " + uid + " not in the range of [10000,+)");
            } else {
                AbsAppInfo keyVlaue = new AbsAppInfo(uid, pkg);
                if (1 == eventType) {
                    synchronized (AppHibernateTask.this.frzHashSet) {
                        AppHibernateTask.this.frzHashSet.add(keyVlaue);
                    }
                    AppHibernateTask.this.sendMsgToHiberEventHandler(eventType, 0);
                } else if (2 == eventType) {
                    synchronized (AppHibernateTask.this.frzHashSet) {
                        AppHibernateTask.this.frzHashSet.remove(keyVlaue);
                    }
                    int unused = AppHibernateTask.this.unFrozenInterrupt(keyVlaue, false);
                } else {
                    AwareLog.i(AppHibernateTask.TAG_PG, "eventType is not frozen/thawed, Neglect!");
                }
            }
        }
    };

    final class HiberHanldler extends Handler {
        public HiberHanldler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.w(AppHibernateTask.TAG_PG, "null == msg");
                return;
            }
            int i = msg.what;
            if (i == 1) {
                int unused = AppHibernateTask.this.frozenInhandleMsg();
            } else if (i != 90005) {
                switch (i) {
                    case AppHibernateCst.ETYPE_MSG_WHAT_CREATE:
                        AppHibernateTask.this.createInhandleMsg();
                        break;
                    case AppHibernateCst.ETYPE_MSG_WHAT_DESTORY:
                        AppHibernateTask.this.destoryInhandleMsg();
                        break;
                    default:
                        AwareLog.w(AppHibernateTask.TAG_PG, "msg.what = " + msg.what + "  is Invalid !");
                        break;
                }
            } else {
                boolean unused2 = AppHibernateTask.this.getPGSdk();
            }
        }
    }

    public static AppHibernateTask getInstance() {
        AppHibernateTask appHibernateTask;
        synchronized (mLock) {
            if (mAppHibernateTask == null) {
                mAppHibernateTask = new AppHibernateTask();
            }
            appHibernateTask = mAppHibernateTask;
        }
        return appHibernateTask;
    }

    private AppHibernateTask() {
    }

    public void initBeforeCreate(Context context) {
        this.mContext = context;
    }

    public void create() {
        AwareLog.d(TAG_PG, "create  Enter");
        sendMsgToHiberEventHandler(AppHibernateCst.ETYPE_MSG_WHAT_CREATE, 0);
    }

    public void destory() {
        AwareLog.d(TAG_PG, "destory  Enter");
        sendMsgToHiberEventHandler(AppHibernateCst.ETYPE_MSG_WHAT_DESTORY, 0);
    }

    public int interruptReclaim(int uid, String pkgName, long timestamp) {
        if (!this.mRunning.get()) {
            AwareLog.w(TAG_PG, "interruptReclaim     failed  , because AppHibernateTask is not  enable");
            return -1;
        } else if (AppHiberUtil.illegalUid(uid) || AppHiberUtil.isStrEmpty(pkgName)) {
            return -1;
        } else {
            AbsAppInfo targetApp = new AbsAppInfo(uid, pkgName);
            setResAppEventData(targetApp, timestamp);
            return unFrozenInterrupt(targetApp, true);
        }
    }

    public void setScreenState(int screenState) {
        if (!this.mRunning.get()) {
            AwareLog.w(TAG_PG, "setScreenState     failed  , because AppHibernateTask is not  enable");
            return;
        }
        if (screenState == 90011) {
            this.mIsScreenOff.set(true);
        } else if (screenState == 20011) {
            this.mIsScreenOff.set(false);
        } else {
            AwareLog.i(TAG_PG, screenState + " is not EVENT_SCREEN_OFF/ON, Neglect!");
        }
    }

    public ArrayList<DumpData> getDumpData(int time) {
        try {
            this.frzSemaphore.acquire();
            AwareLog.d(TAG_PG, "getDumpData frzSemaphore : acquire");
            if (this.mRunning.get()) {
                this.mAppHiberMgr.doHiberDumpApi(1);
            }
            this.frzSemaphore.release();
            AwareLog.d(TAG_PG, "getDumpData frzSemaphore : release");
            return AppHiberRadar.getInstance().getDumpData(time);
        } catch (InterruptedException e) {
            AwareLog.e(TAG_PG, "getDumpData happened InterruptedException");
            return null;
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        try {
            this.frzSemaphore.acquire();
            AwareLog.d(TAG_PG, "getStatisticsData frzSemaphore : acquire");
            if (this.mRunning.get()) {
                this.mAppHiberMgr.doHiberDumpApi(2);
            }
            this.frzSemaphore.release();
            AwareLog.d(TAG_PG, "getStatisticsData frzSemaphore : release");
            return AppHiberRadar.getInstance().getStatisticsData();
        } catch (InterruptedException e) {
            AwareLog.e(TAG_PG, "getStatisticsData happened InterruptedException");
            return null;
        }
    }

    public boolean isAppHiberEnabled() {
        return this.mRunning.get();
    }

    public int reclaimApp(AwareProcessInfo awareProcessInfo) {
        if (awareProcessInfo == null) {
            AwareLog.i(TAG_PG, "recliamApps     refused, because awareProcessInfo is Null");
            return EReclaimResult.OTHER_ERR.getValue();
        } else if (this.mIsScreenOff.get()) {
            AwareLog.i(TAG_PG, "recliamApps     refused, because Screen   Off ");
            return EReclaimResult.OTHER_ERR.getValue();
        } else {
            ArraySet<HiberAppInfo> currentList = new ArraySet<>();
            ProcessInfo process = awareProcessInfo.mProcInfo;
            if (AppHiberUtil.illegalProcessInfo(process)) {
                return EReclaimResult.OTHER_ERR.getValue();
            }
            int tmpUid = process.mUid;
            String tmpPkgName = (String) process.mPackageName.get(0);
            if (AppHiberUtil.isStrEmpty(tmpPkgName)) {
                AwareLog.d(TAG_PG, "the awareProcessInfo.mProcInfo.mPackageName is empty, Illeagal! Return.");
                return EReclaimResult.OTHER_ERR.getValue();
            }
            currentList.add(new HiberAppInfo(process.mUid, tmpPkgName, process.mPid, process.mProcessName));
            try {
                this.frzSemaphore.acquire();
                AwareLog.d(TAG_PG, "reclaimApp frzSemaphore : acquire");
                int retValue = EReclaimResult.OTHER_ERR.getValue();
                if (this.mRunning.get()) {
                    retValue = analysisAPBInfo(new AbsAppInfo(tmpUid, tmpPkgName), currentList);
                }
                this.frzSemaphore.release();
                AwareLog.d(TAG_PG, "reclaimApp frzSemaphore : release");
                return retValue;
            } catch (InterruptedException e) {
                AwareLog.e(TAG_PG, "reclaimApp happened InterruptedException");
                return EReclaimResult.OTHER_ERR.getValue();
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendMsgToHiberEventHandler(int eventType, long delay) {
        if (this.mHiberEventHandler == null) {
            AwareLog.e(TAG_PG, "sendMsgToHiberEventHandler     exit  , because  NULL == mHiberEventHandler");
            return;
        }
        if (eventType == 90001 || eventType == 90002) {
            removeAllMsgFromHiberEventHandler();
        } else if (eventType == 90005) {
            this.mHiberEventHandler.removeMessages(AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK);
        } else if (eventType == 1) {
            this.mHiberEventHandler.removeMessages(1);
            synchronized (this.frzHashSet) {
                if (this.frzHashSet.isEmpty()) {
                    AwareLog.i(TAG_PG, "frzHashSet  is null, no need to request reclaim");
                    return;
                }
            }
        } else {
            AwareLog.i(TAG_PG, "eventType=" + eventType + " is not the legal msgWhat, Neglect!");
            return;
        }
        Message msg = this.mHiberEventHandler.obtainMessage();
        msg.what = eventType;
        this.mHiberEventHandler.sendMessageDelayed(msg, delay);
    }

    private void removeAllMsgFromHiberEventHandler() {
        if (this.mHiberEventHandler == null) {
            AwareLog.w(TAG_PG, "null == mHiberEventHandler");
            return;
        }
        this.mHiberEventHandler.removeMessages(AppHibernateCst.ETYPE_MSG_WHAT_CREATE);
        removeAllMsgFromHiberEventHandlerExcpCreate();
    }

    private void removeAllMsgFromHiberEventHandlerExcpCreate() {
        if (this.mHiberEventHandler != null) {
            this.mHiberEventHandler.removeMessages(AppHibernateCst.ETYPE_MSG_WHAT_DESTORY);
            this.mHiberEventHandler.removeMessages(1);
            this.mHiberEventHandler.removeMessages(AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK);
        }
    }

    /* access modifiers changed from: private */
    public void createInhandleMsg() {
        if (this.mRunning.get()) {
            AwareLog.d(TAG_PG, "AppHiberTask has been Created!");
            return;
        }
        this.mAppHiberMgr.notifyHiberStart();
        getPGSdk();
        this.mRunning.set(true);
    }

    /* access modifiers changed from: private */
    public void destoryInhandleMsg() {
        if (!this.mRunning.get()) {
            AwareLog.d(TAG_PG, "AppHiberTask has been Destroyed!");
            return;
        }
        try {
            this.frzSemaphore.acquire();
            AwareLog.d(TAG_PG, "destoryInhandleMsg frzSemaphore : acquire");
            this.mRunning.set(false);
            callPGunRegisterListener();
            this.mPGSdk = null;
            removeAllMsgFromHiberEventHandlerExcpCreate();
            clearLocalData();
            this.mAppHiberMgr.notifyHiberStop();
            this.frzSemaphore.release();
            AwareLog.d(TAG_PG, "destoryInhandleMsg frzSemaphore : release");
        } catch (InterruptedException e) {
            AwareLog.e(TAG_PG, "destoryInhandleMsg happened InterruptedException");
        }
    }

    /* access modifiers changed from: private */
    public int frozenInhandleMsg() {
        if (this.mIsScreenOff.get()) {
            AwareLog.i(TAG_PG, " Screen   Off  State, frozenInhandleMsg Return");
            return -1;
        } else if (!MemoryReader.isZramOK()) {
            AwareLog.i(TAG_PG, " Zram Space may be full, frozenInhandleMsg Return");
            return -1;
        } else if (isInteracting()) {
            AwareLog.i(TAG_PG, "at the moment: Interactioning, frozenInhandleMsg delay 10 s");
            sendMsgToHiberEventHandler(1, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            return -1;
        } else if (isCpuLoadHeavy()) {
            AwareLog.i(TAG_PG, "at the moment: CpuLoad is heavy, frozenInhandleMsg delay 1 min");
            sendMsgToHiberEventHandler(1, AppHibernateCst.DELAY_ONE_MINS);
            return -1;
        } else {
            int retValue = -1;
            long delayTime = 5000;
            if (this.frzSemaphore.tryAcquire()) {
                AwareLog.d(TAG_PG, "frozenInhandleMsg frzSemaphore : tryAcquire");
                AbsAppInfo targetApp = null;
                synchronized (this.frzHashSet) {
                    if (!this.frzHashSet.isEmpty()) {
                        targetApp = this.frzHashSet.iterator().next();
                        this.frzHashSet.remove(targetApp);
                    }
                }
                retValue = analysisAPBInfo(targetApp, AppHiberUtil.getHiberProcInfoListByAbsAppInfo(this.mContext, targetApp));
                if (!(!MemoryFeature2.isUpMemoryFeature.get() || MemoryConstant.getConfigGmcSwitch() == 0 || targetApp == null)) {
                    GpuCompressAction.doGmc(targetApp.mUid);
                }
                this.frzSemaphore.release();
                AwareLog.d(TAG_PG, "frozenInhandleMsg frzSemaphore : release");
                delayTime = 0;
            }
            sendMsgToHiberEventHandler(1, delayTime);
            return retValue;
        }
    }

    /* access modifiers changed from: private */
    public int unFrozenInterrupt(AbsAppInfo keyValue, boolean needRmv) {
        ArraySet<HiberAppInfo> tmpSet;
        synchronized (this.mReclaimedRecordMap) {
            tmpSet = this.mReclaimedRecordMap.get(keyValue);
        }
        if (tmpSet == null) {
            AwareLog.d(TAG_PG, "[uid = " + keyValue.mUid + ", pkg = " + keyValue.mPkgName + "] not in relaimedMap");
            return 0;
        }
        int retValue = 0;
        if (!this.mIsScreenOff.get() && keyValue.equals(this.mReclaimingApp)) {
            retValue = this.mAppHiberMgr.doHiberFrzApi(keyValue.mPkgName, AppHiberUtil.getPidsFromList(tmpSet), 0);
        }
        if (needRmv) {
            synchronized (this.mReclaimedRecordMap) {
                this.mReclaimedRecordMap.remove(keyValue);
            }
        }
        return retValue;
    }

    private int analysisAPBInfo(AbsAppInfo keyValue, ArraySet<HiberAppInfo> currentChildList) {
        ArraySet arraySet;
        int[] pidArray;
        if (AppHiberUtil.illegalAbsAppInfo(keyValue) || AppHiberUtil.illegalHiberAppInfoArraySet(currentChildList)) {
            synchronized (this.mReclaimedRecordMap) {
                this.mReclaimedRecordMap.remove(keyValue);
            }
            return EReclaimResult.OTHER_ERR.getValue();
        }
        synchronized (this.mReclaimedRecordMap) {
            arraySet = this.mReclaimedRecordMap.get(keyValue);
            pidArray = AppHiberUtil.getDiffPidArray(arraySet, currentChildList);
        }
        if (pidArray.length == 0) {
            AwareLog.d(TAG_PG, keyValue.mPkgName + "  has no diff pid for reclaim! analysisAPBInfo Return");
            return EReclaimResult.HAS_BEEN_RECLAIMED.getValue();
        }
        int cmdRet = this.mAppHiberMgr.doHiberFrzApi(keyValue.mPkgName, pidArray, 1);
        if (-1 == cmdRet) {
            AwareLog.d(TAG_PG, keyValue.mPkgName + "  send to native err! analysisAPBInfo Return");
            return EReclaimResult.SEND_PRO_TO_NATIVE_ERR.getValue();
        }
        if (!AppHiberUtil.illegalHiberAppInfoArraySet(arraySet)) {
            currentChildList.addAll(arraySet);
        }
        synchronized (this.mReclaimedRecordMap) {
            ArraySet<HiberAppInfo> validReclaimedSet = new ArraySet<>();
            validReclaimedSet.addAll(currentChildList);
            this.mReclaimedRecordMap.put(keyValue, validReclaimedSet);
        }
        int[] failArray = AppHibernateCst.EMPTY_INT_ARRAY;
        if (isTopFrontApp(keyValue)) {
            this.mAppHiberMgr.doHiberFrzApi(keyValue.mPkgName, pidArray, 0);
            failArray = pidArray;
            AwareLog.i(TAG_PG, keyValue.mPkgName + "  is Front, stop reclaim.");
        } else if (MemoryConstant.getConfigReclaimFileCache() || !MemoryConstant.isKernCompressEnable()) {
            this.mReclaimingApp = keyValue;
            failArray = this.mAppHiberMgr.doHiberReclaimApi();
            this.mReclaimingApp = INVALID_ABSAPPINFO;
        }
        if (failArray.length > 0) {
            List<HiberAppInfo> tmpList = new ArrayList<>();
            for (int pid : failArray) {
                Iterator<HiberAppInfo> it = currentChildList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    HiberAppInfo aware = it.next();
                    if (aware.mPid == pid) {
                        tmpList.add(aware);
                        break;
                    }
                }
            }
            currentChildList.removeAll(tmpList);
            AwareLog.i(TAG_PG, "reclaim " + Arrays.toString(pidArray) + ", be interrupted " + Arrays.toString(failArray));
            synchronized (this.mReclaimedRecordMap) {
                if (this.mReclaimedRecordMap.containsKey(keyValue)) {
                    if (AppHiberUtil.illegalHiberAppInfoArraySet(currentChildList)) {
                        this.mReclaimedRecordMap.remove(keyValue);
                    } else {
                        this.mReclaimedRecordMap.put(keyValue, currentChildList);
                    }
                }
            }
            cmdRet = EReclaimResult.RECLAIM_BE_INTERRUPT.getValue();
        }
        return cmdRet;
    }

    private void clearLocalData() {
        synchronized (this.mReclaimedRecordMap) {
            this.mReclaimedRecordMap.clear();
        }
        synchronized (this.frzHashSet) {
            this.frzHashSet.clear();
        }
        this.mLastInputEvent = 0;
        this.mLastInputTime = 0;
        this.mLastResEventTime = 0;
        this.mReclaimingApp = INVALID_ABSAPPINFO;
        this.mCurFrontAbsApp = INVALID_ABSAPPINFO;
    }

    private void callPGregisterListener() {
        if (this.mPGSdk != null) {
            try {
                this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 6);
            } catch (RemoteException e) {
                this.mPGSdk = null;
                AwareLog.e(TAG_PG, "mPGSdk registerSink && enableStateEvent happend RemoteException ");
            }
        }
    }

    private void callPGunRegisterListener() {
        if (this.mPGSdk != null) {
            try {
                this.mPGSdk.disableStateEvent(this.mStateRecognitionListener, 6);
            } catch (RemoteException e) {
                AwareLog.e(TAG_PG, "callPG unRegisterListener  happend RemoteException ");
            }
        }
    }

    private String callPGGetTopFrontApp() {
        if (this.mPGSdk == null) {
            return null;
        }
        try {
            return this.mPGSdk.getTopFrontApp(this.mContext);
        } catch (RemoteException e) {
            AwareLog.e(TAG_PG, "callPG getTopFrontApp  happend RemoteException ");
            return null;
        }
    }

    /* access modifiers changed from: private */
    public boolean getPGSdk() {
        if (this.mPGSdk != null) {
            return true;
        }
        this.mPGSdk = PGSdk.getInstance();
        if (this.mPGSdk != null) {
            callPGregisterListener();
        }
        if (this.mPGSdk != null) {
            return true;
        }
        sendMsgToHiberEventHandler(AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK, AppHibernateCst.DELAY_ONE_MINS);
        return false;
    }

    public int setLastInputEventData(int lastInputEvent, long lastInputTime) {
        if (!this.mRunning.get()) {
            AwareLog.w(TAG_PG, "setLastInputEventData     failed  , because AppHibernateTask is not  enable");
            return -1;
        }
        this.mLastInputEvent = lastInputEvent;
        this.mLastInputTime = lastInputTime;
        if (this.mLastInputEvent == 10001) {
            foreceInterruptReclaim();
        }
        return 0;
    }

    private int foreceInterruptReclaim() {
        if (AppHiberUtil.illegalAbsAppInfo(this.mReclaimingApp)) {
            return -1;
        }
        return unFrozenInterrupt(this.mReclaimingApp, false);
    }

    private boolean isInteracting() {
        if (this.mLastInputEvent == 10001) {
            return true;
        }
        if (this.mLastInputEvent == 80001) {
            return SystemClock.uptimeMillis() - this.mLastInputTime < 4000;
        }
        AwareLog.i(TAG_PG, "mLastInputEvent=" + this.mLastInputEvent + " is not EVENT_TOUCH_DOWN/UP, Neglect!");
        return false;
    }

    private boolean isCpuLoadHeavy() {
        return this.mCpuReader.getCpuPercent() > MemoryConstant.getNormalThresHold();
    }

    public ArrayMap<Integer, HiberAppInfo> getRelaimedRecord() {
        if (!this.mRunning.get()) {
            AwareLog.w(TAG_PG, "getRelaimedList     failed  , because AppHibernateTask is not  enable");
            return null;
        }
        synchronized (this.mReclaimedRecordMap) {
            if (this.mReclaimedRecordMap.isEmpty()) {
                AwareLog.i(TAG_PG, "current  reclaimed record is Empty");
                return null;
            }
            ArrayMap<Integer, HiberAppInfo> returnMap = new ArrayMap<>();
            for (Map.Entry<AbsAppInfo, ArraySet<HiberAppInfo>> entry : this.mReclaimedRecordMap.entrySet()) {
                Iterator it = entry.getValue().iterator();
                while (it.hasNext()) {
                    HiberAppInfo appinfo = (HiberAppInfo) it.next();
                    returnMap.put(Integer.valueOf(appinfo.mPid), appinfo);
                }
            }
            return returnMap;
        }
    }

    private void setResAppEventData(AbsAppInfo keyValue, long lastEventTime) {
        if (!keyValue.equals(this.mCurFrontAbsApp)) {
            this.mCurFrontAbsApp = keyValue;
            this.mLastResEventTime = lastEventTime;
        }
    }

    private boolean isTopFrontApp(AbsAppInfo keyValue) {
        if (AppHiberUtil.illegalAbsAppInfo(this.mCurFrontAbsApp) || SystemClock.uptimeMillis() - this.mLastResEventTime >= 500) {
            return AppHiberUtil.isTheSameAppUnderMultiUser(callPGGetTopFrontApp(), keyValue);
        }
        return keyValue.equals(this.mCurFrontAbsApp);
    }
}
