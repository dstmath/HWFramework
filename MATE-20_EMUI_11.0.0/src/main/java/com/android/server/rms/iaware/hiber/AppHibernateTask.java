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
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.iaware.feature.MemoryFeatureEx;
import com.android.server.rms.iaware.hiber.bean.AbsAppInfo;
import com.android.server.rms.iaware.hiber.bean.HiberAppInfo;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.hiber.constant.EnumReclaimResult;
import com.android.server.rms.iaware.hiber.util.AppHiberUtil;
import com.android.server.rms.iaware.memory.action.GpuCompressAction;
import com.android.server.rms.iaware.memory.utils.CpuReader;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import com.huawei.server.rme.hyperhold.Swap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AppHibernateTask {
    private static final int INTERACTION_INTERVAL = 4000;
    private static final AbsAppInfo INVALID_ABS_APP_INFO = new AbsAppInfo(-1, "");
    private static final Object LOCK = new Object();
    private static final String TAG_PG = "AppHiber_Task";
    private static final int TRY_ACQUIRE_SEM_INTERVAL = 5000;
    private static final int TRY_HANDLE_FREEZE_INTERVAL = 10000;
    private static AppHibernateTask sAppHibernateTask = null;
    private AppHibernateMgr mAppHiberMgr;
    private Context mContext;
    private CpuReader mCpuReader;
    private volatile AbsAppInfo mCurFrontAbsApp;
    private final ArraySet<AbsAppInfo> mFreezeHashSet = new ArraySet<>();
    private final Semaphore mFreezeSemaphore = new Semaphore(1);
    private Handler mHiberEventHandler;
    private final AtomicBoolean mIsScreenOff = new AtomicBoolean(false);
    private int mLastInputEvent = 0;
    private long mLastInputTime = 0;
    private long mLastResEventTime = 0;
    private PowerKit mPgSdk = null;
    private final ArrayMap<AbsAppInfo, ArraySet<HiberAppInfo>> mReclaimedRecordMap = new ArrayMap<>();
    private AbsAppInfo mReclaimingApp;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private PowerKit.Sink mStateRecognitionListener = new PowerKit.Sink() {
        /* class com.android.server.rms.iaware.hiber.AppHibernateTask.AnonymousClass1 */

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            StringBuilder sb = new StringBuilder();
            sb.append("onStateChanged Enter ");
            sb.append(eventType == 1 ? "FRZ CallBK: " : "THW CallBK: ");
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
                AwareLog.i(AppHibernateTask.TAG_PG, "pkg == null || pkg.trim().isEmpty()");
            } else if (AppHiberUtil.illegalUid(uid)) {
                AwareLog.i(AppHibernateTask.TAG_PG, "uid = " + uid + " not in the range of [10000,+)");
            } else {
                AbsAppInfo keyVlaue = new AbsAppInfo(uid, pkg);
                if (eventType == 1) {
                    synchronized (AppHibernateTask.this.mFreezeHashSet) {
                        AppHibernateTask.this.mFreezeHashSet.add(keyVlaue);
                    }
                    AppHibernateTask.this.sendMsgToHiberEventHandler(eventType, 0);
                } else if (eventType == 2) {
                    synchronized (AppHibernateTask.this.mFreezeHashSet) {
                        AppHibernateTask.this.mFreezeHashSet.remove(keyVlaue);
                    }
                    AppHibernateTask.this.unFrozenInterrupt(keyVlaue, false);
                } else {
                    AwareLog.i(AppHibernateTask.TAG_PG, "eventType is not frozen/thawed, Neglect!");
                }
            }
        }
    };

    private AppHibernateTask() {
        AbsAppInfo absAppInfo = INVALID_ABS_APP_INFO;
        this.mReclaimingApp = absAppInfo;
        this.mCurFrontAbsApp = absAppInfo;
        this.mAppHiberMgr = AppHibernateMgr.getInstance();
        this.mCpuReader = CpuReader.getInstance();
        initHandler();
    }

    public static AppHibernateTask getInstance() {
        AppHibernateTask appHibernateTask;
        synchronized (LOCK) {
            if (sAppHibernateTask == null) {
                sAppHibernateTask = new AppHibernateTask();
            }
            appHibernateTask = sAppHibernateTask;
        }
        return appHibernateTask;
    }

    public void initBeforeCreate(Context context) {
        this.mContext = context;
    }

    public void create() {
        AwareLog.d(TAG_PG, "create Enter");
        sendMsgToHiberEventHandler(AppHibernateCst.ETYPE_MSG_WHAT_CREATE, 0);
    }

    public void destory() {
        AwareLog.d(TAG_PG, "destory Enter");
        sendMsgToHiberEventHandler(AppHibernateCst.ETYPE_MSG_WHAT_DESTORY, 0);
    }

    public int interruptReclaim(int uid, String pkgName, long timeStamp) {
        if (!this.mRunning.get()) {
            AwareLog.w(TAG_PG, "interruptReclaim failed, because AppHibernateTask is not enable");
            return -1;
        } else if (AppHiberUtil.illegalUid(uid) || AppHiberUtil.isStrEmpty(pkgName)) {
            return -1;
        } else {
            AbsAppInfo targetApp = new AbsAppInfo(uid, pkgName);
            setResAppEventData(targetApp, timeStamp);
            return unFrozenInterrupt(targetApp, true);
        }
    }

    public void setScreenState(int screenState) {
        if (!this.mRunning.get()) {
            AwareLog.w(TAG_PG, "setScreenState failed, because AppHibernateTask is not enable");
        } else if (screenState == 90011) {
            this.mIsScreenOff.set(true);
        } else if (screenState == 20011) {
            this.mIsScreenOff.set(false);
        } else {
            AwareLog.i(TAG_PG, screenState + " is not EVENT_SCREEN_OFF/ON, Neglect!");
        }
    }

    public ArrayList<DumpData> getDumpData(int time) {
        try {
            this.mFreezeSemaphore.acquire();
            AwareLog.d(TAG_PG, "getDumpData mFreezeSemaphore : acquire");
            if (this.mRunning.get()) {
                this.mAppHiberMgr.doHiberDumpApi(1);
            }
            this.mFreezeSemaphore.release();
            AwareLog.d(TAG_PG, "getDumpData mFreezeSemaphore : release");
            return AppHiberRadar.getInstance().getDumpData(time);
        } catch (InterruptedException e) {
            AwareLog.e(TAG_PG, "getDumpData happened InterruptedException");
            return new ArrayList<>();
        }
    }

    public ArrayList<StatisticsData> getStatisticsData() {
        try {
            this.mFreezeSemaphore.acquire();
            AwareLog.d(TAG_PG, "getStatisticsData mFreezeSemaphore : acquire");
            if (this.mRunning.get()) {
                this.mAppHiberMgr.doHiberDumpApi(2);
            }
            this.mFreezeSemaphore.release();
            AwareLog.d(TAG_PG, "getStatisticsData mFreezeSemaphore : release");
            return AppHiberRadar.getInstance().getStatisticsData();
        } catch (InterruptedException e) {
            AwareLog.e(TAG_PG, "getStatisticsData happened InterruptedException");
            return new ArrayList<>();
        }
    }

    public boolean isAppHiberEnabled() {
        return this.mRunning.get();
    }

    public int reclaimApp(AwareProcessInfo awareProcessInfo) {
        if (awareProcessInfo == null) {
            AwareLog.i(TAG_PG, "recliamApps refused, because awareProcessInfo is Null");
            return EnumReclaimResult.OTHER_ERR.getValue();
        } else if (this.mIsScreenOff.get()) {
            AwareLog.i(TAG_PG, "recliamApps refused, because Screen Off ");
            return EnumReclaimResult.OTHER_ERR.getValue();
        } else {
            ArraySet<HiberAppInfo> currentList = new ArraySet<>();
            ProcessInfo process = awareProcessInfo.procProcInfo;
            if (AppHiberUtil.illegalProcessInfo(process)) {
                return EnumReclaimResult.OTHER_ERR.getValue();
            }
            int tmpUid = process.mUid;
            String tmpPkgName = (String) process.mPackageName.get(0);
            if (AppHiberUtil.isStrEmpty(tmpPkgName)) {
                AwareLog.d(TAG_PG, "the awareProcessInfo.mProcInfo.mPackageName is empty, Illeagal! Return.");
                return EnumReclaimResult.OTHER_ERR.getValue();
            }
            currentList.add(new HiberAppInfo(process.mUid, tmpPkgName, process.mPid, process.mProcessName));
            try {
                this.mFreezeSemaphore.acquire();
                AwareLog.d(TAG_PG, "reclaimApp mFreezeSemaphore : acquire");
                int retValue = EnumReclaimResult.OTHER_ERR.getValue();
                if (this.mRunning.get()) {
                    retValue = analysisApbInfo(new AbsAppInfo(tmpUid, tmpPkgName), currentList);
                }
                this.mFreezeSemaphore.release();
                AwareLog.d(TAG_PG, "reclaimApp mFreezeSemaphore : release");
                return retValue;
            } catch (InterruptedException e) {
                AwareLog.e(TAG_PG, "reclaimApp happened InterruptedException");
                return EnumReclaimResult.OTHER_ERR.getValue();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMsgToHiberEventHandler(int eventType, long delay) {
        Handler handler = this.mHiberEventHandler;
        if (handler == null) {
            AwareLog.e(TAG_PG, "sendMsgToHiberEventHandler exit, because mHiberEventHandler == null");
            return;
        }
        if (eventType == 90001 || eventType == 90002) {
            removeAllMsgFromHiberEventHandler();
        } else if (eventType == 90005) {
            handler.removeMessages(AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK);
        } else if (eventType == 1) {
            handler.removeMessages(1);
            synchronized (this.mFreezeHashSet) {
                if (this.mFreezeHashSet.isEmpty()) {
                    AwareLog.i(TAG_PG, "mFreezeHashSet is null, no need to request reclaim");
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
        Handler handler = this.mHiberEventHandler;
        if (handler == null) {
            AwareLog.w(TAG_PG, "mHiberEventHandler == null");
            return;
        }
        handler.removeMessages(AppHibernateCst.ETYPE_MSG_WHAT_CREATE);
        removeAllMsgFromHiberEventHandlerExcpCreate();
    }

    private void removeAllMsgFromHiberEventHandlerExcpCreate() {
        Handler handler = this.mHiberEventHandler;
        if (handler != null) {
            handler.removeMessages(AppHibernateCst.ETYPE_MSG_WHAT_DESTORY);
            this.mHiberEventHandler.removeMessages(1);
            this.mHiberEventHandler.removeMessages(AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK);
        }
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mHiberEventHandler = new HiberHandler(looper);
        } else {
            this.mHiberEventHandler = new HiberHandler(BackgroundThreadEx.getLooper());
        }
    }

    /* access modifiers changed from: package-private */
    public final class HiberHandler extends Handler {
        HiberHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.w(AppHibernateTask.TAG_PG, "msg == null");
                return;
            }
            int i = msg.what;
            if (i == 1) {
                AppHibernateTask.this.frozenInHandleMsg();
            } else if (i != 90005) {
                switch (i) {
                    case AppHibernateCst.ETYPE_MSG_WHAT_CREATE /* 90001 */:
                        AppHibernateTask.this.createInHandleMsg();
                        return;
                    case AppHibernateCst.ETYPE_MSG_WHAT_DESTORY /* 90002 */:
                        AppHibernateTask.this.destoryInHandleMsg();
                        return;
                    default:
                        AwareLog.w(AppHibernateTask.TAG_PG, "msg.what = " + msg.what + " is Invalid !");
                        return;
                }
            } else {
                AppHibernateTask.this.getPgSdk();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createInHandleMsg() {
        if (this.mRunning.get()) {
            AwareLog.d(TAG_PG, "AppHiberTask has been Created!");
            return;
        }
        this.mAppHiberMgr.notifyHiberStart();
        getPgSdk();
        this.mRunning.set(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void destoryInHandleMsg() {
        if (!this.mRunning.get()) {
            AwareLog.d(TAG_PG, "AppHiberTask has been Destroyed!");
            return;
        }
        try {
            this.mFreezeSemaphore.acquire();
            AwareLog.d(TAG_PG, "destoryInHandleMsg mFreezeSemaphore : acquire");
            this.mRunning.set(false);
            callPgUnRegisterListener();
            this.mPgSdk = null;
            removeAllMsgFromHiberEventHandlerExcpCreate();
            clearLocalData();
            this.mAppHiberMgr.notifyHiberStop();
            this.mFreezeSemaphore.release();
            AwareLog.d(TAG_PG, "destoryInHandleMsg mFreezeSemaphore : release");
        } catch (InterruptedException e) {
            AwareLog.e(TAG_PG, "destoryInHandleMsg happened InterruptedException");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int frozenInHandleMsg() {
        if (this.mIsScreenOff.get()) {
            AwareLog.i(TAG_PG, "Screen Off State, frozenInHandleMsg Return");
            return -1;
        } else if (!MemoryReader.isZramOk()) {
            AwareLog.i(TAG_PG, "Zram Space may be full, frozenInHandleMsg Return");
            return -1;
        } else if (isInteracting()) {
            AwareLog.i(TAG_PG, "at the moment: Interactioning, frozenInHandleMsg delay 10 s");
            sendMsgToHiberEventHandler(1, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            return -1;
        } else if (isCpuLoadHeavy()) {
            AwareLog.i(TAG_PG, "at the moment: CpuLoad is heavy, frozenInHandleMsg delay 1 min");
            sendMsgToHiberEventHandler(1, AppHibernateCst.DELAY_ONE_MINS);
            return -1;
        } else {
            int retValue = -1;
            long delayTime = 5000;
            if (this.mFreezeSemaphore.tryAcquire()) {
                AwareLog.d(TAG_PG, "frozenInHandleMsg mFreezeSemaphore : tryAcquire");
                AbsAppInfo targetApp = null;
                synchronized (this.mFreezeHashSet) {
                    if (!this.mFreezeHashSet.isEmpty()) {
                        targetApp = this.mFreezeHashSet.iterator().next();
                        this.mFreezeHashSet.remove(targetApp);
                    }
                }
                if (!Swap.getInstance().isSwapEnabled()) {
                    retValue = analysisApbInfo(targetApp, AppHiberUtil.getHiberProcInfoListByAbsAppInfo(this.mContext, targetApp));
                }
                if (!(!MemoryFeatureEx.IS_UP_MEMORY_FEATURE.get() || MemoryConstant.getConfigGmcSwitch() == 0 || targetApp == null)) {
                    GpuCompressAction.doGmc(targetApp.uid);
                }
                this.mFreezeSemaphore.release();
                AwareLog.d(TAG_PG, "frozenInHandleMsg mFreezeSemaphore : release");
                delayTime = 0;
            }
            sendMsgToHiberEventHandler(1, delayTime);
            return retValue;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int unFrozenInterrupt(AbsAppInfo keyValue, boolean needRmv) {
        ArraySet<HiberAppInfo> tmpSet;
        synchronized (this.mReclaimedRecordMap) {
            tmpSet = this.mReclaimedRecordMap.get(keyValue);
        }
        if (tmpSet == null) {
            AwareLog.d(TAG_PG, "[uid = " + keyValue.uid + ", pkg = " + keyValue.pkgName + "] not in relaimedMap");
            return 0;
        }
        int retValue = 0;
        if (!this.mIsScreenOff.get() && keyValue.equals(this.mReclaimingApp)) {
            retValue = this.mAppHiberMgr.doHiberFrzApi(keyValue.pkgName, AppHiberUtil.getPidsFromList(tmpSet), 0);
        }
        if (needRmv) {
            synchronized (this.mReclaimedRecordMap) {
                this.mReclaimedRecordMap.remove(keyValue);
            }
        }
        return retValue;
    }

    private int analysisApbInfo(AbsAppInfo keyValue, ArraySet<HiberAppInfo> currentChildList) {
        ArraySet<HiberAppInfo> hisChildList;
        int[] pidArray;
        if (AppHiberUtil.illegalAbsAppInfo(keyValue) || AppHiberUtil.illegalHiberAppInfoArraySet(currentChildList)) {
            synchronized (this.mReclaimedRecordMap) {
                this.mReclaimedRecordMap.remove(keyValue);
            }
            return EnumReclaimResult.OTHER_ERR.getValue();
        }
        synchronized (this.mReclaimedRecordMap) {
            hisChildList = this.mReclaimedRecordMap.get(keyValue);
            pidArray = AppHiberUtil.getDiffPidArray(hisChildList, currentChildList);
        }
        if (pidArray.length == 0) {
            AwareLog.d(TAG_PG, keyValue.pkgName + " has no diff pid for reclaim! analysisApbInfo Return");
            return EnumReclaimResult.HAS_BEEN_RECLAIMED.getValue();
        }
        int cmdRet = this.mAppHiberMgr.doHiberFrzApi(keyValue.pkgName, pidArray, 1);
        if (cmdRet == -1) {
            AwareLog.d(TAG_PG, keyValue.pkgName + " send to native err! analysisApbInfo Return");
            return EnumReclaimResult.SEND_PRO_TO_NATIVE_ERR.getValue();
        }
        if (!AppHiberUtil.illegalHiberAppInfoArraySet(hisChildList)) {
            currentChildList.addAll((ArraySet<? extends HiberAppInfo>) hisChildList);
        }
        synchronized (this.mReclaimedRecordMap) {
            ArraySet<HiberAppInfo> validReclaimedSet = new ArraySet<>();
            validReclaimedSet.addAll((ArraySet<? extends HiberAppInfo>) currentChildList);
            this.mReclaimedRecordMap.put(keyValue, validReclaimedSet);
        }
        return analysisApbInfoInternal(keyValue, currentChildList, pidArray, cmdRet);
    }

    private int analysisApbInfoInternal(AbsAppInfo keyValue, ArraySet<HiberAppInfo> currentChildList, int[] pidArray, int cmdRet) {
        int[] failArray = AppHibernateCst.EMPTY_INT_ARRAY;
        if (isTopFrontApp(keyValue)) {
            this.mAppHiberMgr.doHiberFrzApi(keyValue.pkgName, pidArray, 0);
            failArray = pidArray;
            AwareLog.i(TAG_PG, keyValue.pkgName + " is Front, stop reclaim.");
        } else if (MemoryConstant.getConfigReclaimFileCache() || !MemoryConstant.isKernCompressEnable()) {
            this.mReclaimingApp = keyValue;
            failArray = this.mAppHiberMgr.doHiberReclaimApi();
            this.mReclaimingApp = INVALID_ABS_APP_INFO;
        }
        if (failArray.length <= 0) {
            return cmdRet;
        }
        List<HiberAppInfo> tmpList = new ArrayList<>();
        for (int pid : failArray) {
            Iterator<HiberAppInfo> it = currentChildList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                HiberAppInfo aware = it.next();
                if (aware.pid == pid) {
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
        return EnumReclaimResult.RECLAIM_BE_INTERRUPT.getValue();
    }

    private void clearLocalData() {
        synchronized (this.mReclaimedRecordMap) {
            this.mReclaimedRecordMap.clear();
        }
        synchronized (this.mFreezeHashSet) {
            this.mFreezeHashSet.clear();
        }
        this.mLastInputEvent = 0;
        this.mLastInputTime = 0;
        this.mLastResEventTime = 0;
        AbsAppInfo absAppInfo = INVALID_ABS_APP_INFO;
        this.mReclaimingApp = absAppInfo;
        this.mCurFrontAbsApp = absAppInfo;
    }

    private void callPgRegisterListener() {
        PowerKit powerKit = this.mPgSdk;
        if (powerKit != null) {
            try {
                powerKit.enableStateEvent(this.mStateRecognitionListener, 6);
            } catch (RemoteException e) {
                this.mPgSdk = null;
                AwareLog.e(TAG_PG, "mPgSdk registerSink && enableStateEvent happened RemoteException");
            }
        }
    }

    private void callPgUnRegisterListener() {
        PowerKit powerKit = this.mPgSdk;
        if (powerKit != null) {
            try {
                powerKit.disableStateEvent(this.mStateRecognitionListener, 6);
            } catch (RemoteException e) {
                AwareLog.e(TAG_PG, "callPG unRegisterListener happened RemoteException");
            }
        }
    }

    private String callPgGetTopFrontApp() {
        PowerKit powerKit = this.mPgSdk;
        if (powerKit == null) {
            return null;
        }
        try {
            return powerKit.getTopFrontApp(this.mContext);
        } catch (RemoteException e) {
            AwareLog.e(TAG_PG, "callPG getTopFrontApp happened RemoteException ");
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getPgSdk() {
        if (this.mPgSdk != null) {
            return true;
        }
        this.mPgSdk = PowerKit.getInstance();
        if (this.mPgSdk != null) {
            callPgRegisterListener();
        }
        if (this.mPgSdk != null) {
            return true;
        }
        sendMsgToHiberEventHandler(AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK, AppHibernateCst.DELAY_ONE_MINS);
        return false;
    }

    public int setLastInputEventData(int lastInputEvent, long lastInputTime) {
        if (!this.mRunning.get()) {
            AwareLog.w(TAG_PG, "setLastInputEventData failed , because AppHibernateTask is not enable");
            return -1;
        }
        this.mLastInputEvent = lastInputEvent;
        this.mLastInputTime = lastInputTime;
        if (this.mLastInputEvent != 10001) {
            return 0;
        }
        forceInterruptReclaim();
        return 0;
    }

    private int forceInterruptReclaim() {
        if (AppHiberUtil.illegalAbsAppInfo(this.mReclaimingApp)) {
            return -1;
        }
        return unFrozenInterrupt(this.mReclaimingApp, false);
    }

    private boolean isInteracting() {
        int i = this.mLastInputEvent;
        if (i == 10001) {
            return true;
        }
        if (i == 80001) {
            return SystemClock.uptimeMillis() - this.mLastInputTime < 4000;
        }
        AwareLog.i(TAG_PG, "mLastInputEvent=" + this.mLastInputEvent + " is not EVENT_TOUCH_DOWN/UP, Neglect!");
        return false;
    }

    private boolean isCpuLoadHeavy() {
        return this.mCpuReader.getCpuPercent() > MemoryConstant.getNormalThresHold();
    }

    public ArrayMap<Integer, HiberAppInfo> getRelaimedRecord() {
        ArrayMap<Integer, HiberAppInfo> returnMap = new ArrayMap<>();
        if (!this.mRunning.get()) {
            AwareLog.w(TAG_PG, "getRelaimedList failed , because AppHibernateTask is not enable");
            return returnMap;
        }
        synchronized (this.mReclaimedRecordMap) {
            if (this.mReclaimedRecordMap.isEmpty()) {
                AwareLog.i(TAG_PG, "current reclaimed record is Empty");
                return returnMap;
            }
            for (Map.Entry<AbsAppInfo, ArraySet<HiberAppInfo>> entry : this.mReclaimedRecordMap.entrySet()) {
                Iterator<HiberAppInfo> it = entry.getValue().iterator();
                while (it.hasNext()) {
                    HiberAppInfo appinfo = it.next();
                    returnMap.put(Integer.valueOf(appinfo.pid), appinfo);
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
            return AppHiberUtil.isTheSameAppUnderMultiUser(callPgGetTopFrontApp(), keyValue);
        }
        return keyValue.equals(this.mCurFrontAbsApp);
    }
}
