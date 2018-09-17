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
import com.android.server.rms.iaware.hiber.bean.AbsAppInfo;
import com.android.server.rms.iaware.hiber.bean.HiberAppInfo;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.hiber.constant.EReclaimResult;
import com.android.server.rms.iaware.hiber.util.AppHiberUtil;
import com.android.server.rms.iaware.memory.utils.CpuReader;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.huawei.pgmng.plug.PGSdk;
import com.huawei.pgmng.plug.PGSdk.Sink;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AppHibernateTask {
    private static final AbsAppInfo INVALID_ABSAPPINFO = null;
    private static final String TAG_PG = "AppHiber_Task";
    private static AppHibernateTask mAppHibernateTask;
    private ArraySet<AbsAppInfo> frzHashSet;
    private final Semaphore frzSemaphore;
    private AppHibernateMgr mAppHiberMgr;
    private Context mContext;
    private CpuReader mCpuReader;
    private AbsAppInfo mCurFrontAbsApp;
    private Handler mHiberEventHandler;
    private final AtomicBoolean mIsScreenOff;
    private int mLastInputEvent;
    private long mLastInputTime;
    private long mLastResEventTime;
    private PGSdk mPGSdk;
    private ArrayMap<AbsAppInfo, ArraySet<HiberAppInfo>> mReclaimedRecordMap;
    private AbsAppInfo mReclaimingApp;
    private final AtomicBoolean mRunning;
    private Sink mStateRecognitionListener;

    final class HiberHanldler extends Handler {
        public HiberHanldler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.w(AppHibernateTask.TAG_PG, "null == msg");
                return;
            }
            switch (msg.what) {
                case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                    AppHibernateTask.this.frozenInhandleMsg();
                    break;
                case AppHibernateCst.ETYPE_MSG_WHAT_CREATE /*90001*/:
                    AppHibernateTask.this.createInhandleMsg();
                    break;
                case AppHibernateCst.ETYPE_MSG_WHAT_DESTORY /*90002*/:
                    AppHibernateTask.this.destoryInhandleMsg();
                    break;
                case AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK /*90005*/:
                    AppHibernateTask.this.getPGSdk();
                    break;
                default:
                    AwareLog.w(AppHibernateTask.TAG_PG, "msg.what = " + msg.what + "  is Invalid !");
                    break;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.hiber.AppHibernateTask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.hiber.AppHibernateTask.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.hiber.AppHibernateTask.<clinit>():void");
    }

    public static synchronized AppHibernateTask getInstance() {
        AppHibernateTask appHibernateTask;
        synchronized (AppHibernateTask.class) {
            if (mAppHibernateTask == null) {
                mAppHibernateTask = new AppHibernateTask();
            }
            appHibernateTask = mAppHibernateTask;
        }
        return appHibernateTask;
    }

    private AppHibernateTask() {
        this.mPGSdk = null;
        this.mRunning = new AtomicBoolean(false);
        this.mIsScreenOff = new AtomicBoolean(false);
        this.frzSemaphore = new Semaphore(1);
        this.mReclaimedRecordMap = new ArrayMap();
        this.frzHashSet = new ArraySet();
        this.mStateRecognitionListener = new Sink() {
            public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
                String str;
                String str2 = AppHibernateTask.TAG_PG;
                StringBuilder append = new StringBuilder().append("onStateChanged    Enter  ");
                if (eventType == 1) {
                    str = "FRZ CallBK: ";
                } else {
                    str = "THW CallBK: ";
                }
                AwareLog.d(str2, append.append(str).append(", pkg[").append(pkg).append("], uid= ").append(uid).append(", pid= ").append(pid).toString());
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
                        AppHibernateTask.this.unFrozenInterrupt(keyVlaue, false);
                    } else {
                        AwareLog.i(AppHibernateTask.TAG_PG, "eventType is not frozen/thawed, Neglect!");
                    }
                }
            }
        };
        this.mLastInputEvent = 0;
        this.mLastInputTime = 0;
        this.mLastResEventTime = 0;
        this.mReclaimingApp = INVALID_ABSAPPINFO;
        this.mCurFrontAbsApp = INVALID_ABSAPPINFO;
        this.mAppHiberMgr = AppHibernateMgr.getInstance();
        this.mCpuReader = CpuReader.getInstance();
        this.mHiberEventHandler = new HiberHanldler(BackgroundThread.get().getLooper());
    }

    public boolean initBeforeCreate(Context context) {
        if (context == null) {
            return false;
        }
        this.mContext = context;
        return true;
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
        if (this.mRunning.get()) {
            if (screenState == 90011) {
                this.mIsScreenOff.set(true);
            } else if (screenState == 20011) {
                this.mIsScreenOff.set(false);
            } else {
                AwareLog.i(TAG_PG, screenState + " is not EVENT_SCREEN_OFF/ON, Neglect!");
            }
            return;
        }
        AwareLog.w(TAG_PG, "setScreenState     failed  , because AppHibernateTask is not  enable");
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
            ArraySet<HiberAppInfo> currentList = new ArraySet();
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

    private void sendMsgToHiberEventHandler(int eventType, long delay) {
        if (this.mHiberEventHandler == null) {
            AwareLog.e(TAG_PG, "sendMsgToHiberEventHandler     exit  , because  NULL == mHiberEventHandler");
            return;
        }
        if (eventType == AppHibernateCst.ETYPE_MSG_WHAT_CREATE || eventType == AppHibernateCst.ETYPE_MSG_WHAT_DESTORY) {
            removeAllMsgFromHiberEventHandler();
        } else if (eventType == AppHibernateCst.ETYPE_CONNECT_WITH_PG_SDK) {
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

    private void createInhandleMsg() {
        if (this.mRunning.get()) {
            AwareLog.d(TAG_PG, "AppHiberTask has been Created!");
            return;
        }
        this.mAppHiberMgr.notifyHiberStart();
        getPGSdk();
        this.mRunning.set(true);
    }

    private void destoryInhandleMsg() {
        if (this.mRunning.get()) {
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
                return;
            } catch (InterruptedException e) {
                AwareLog.e(TAG_PG, "destoryInhandleMsg happened InterruptedException");
                return;
            }
        }
        AwareLog.d(TAG_PG, "AppHiberTask has been Destroyed!");
    }

    private int frozenInhandleMsg() {
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
                AbsAppInfo absAppInfo = null;
                synchronized (this.frzHashSet) {
                    if (!this.frzHashSet.isEmpty()) {
                        absAppInfo = (AbsAppInfo) this.frzHashSet.iterator().next();
                        this.frzHashSet.remove(absAppInfo);
                    }
                }
                retValue = analysisAPBInfo(absAppInfo, AppHiberUtil.getHiberProcInfoListByAbsAppInfo(this.mContext, absAppInfo));
                this.frzSemaphore.release();
                AwareLog.d(TAG_PG, "frozenInhandleMsg frzSemaphore : release");
                delayTime = 0;
            }
            sendMsgToHiberEventHandler(1, delayTime);
            return retValue;
        }
    }

    private int unFrozenInterrupt(AbsAppInfo keyValue, boolean needRmv) {
        synchronized (this.mReclaimedRecordMap) {
            ArraySet<HiberAppInfo> tmpSet = (ArraySet) this.mReclaimedRecordMap.get(keyValue);
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
        if (AppHiberUtil.illegalAbsAppInfo(keyValue) || AppHiberUtil.illegalHiberAppInfoArraySet(currentChildList)) {
            synchronized (this.mReclaimedRecordMap) {
                this.mReclaimedRecordMap.remove(keyValue);
            }
            return EReclaimResult.OTHER_ERR.getValue();
        }
        synchronized (this.mReclaimedRecordMap) {
            ArraySet<HiberAppInfo> hisChildList = (ArraySet) this.mReclaimedRecordMap.get(keyValue);
            int[] pidArray = AppHiberUtil.getDiffPidArray(hisChildList, currentChildList);
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
        if (!AppHiberUtil.illegalHiberAppInfoArraySet(hisChildList)) {
            currentChildList.addAll(hisChildList);
        }
        synchronized (this.mReclaimedRecordMap) {
            ArraySet<HiberAppInfo> validReclaimedSet = new ArraySet();
            validReclaimedSet.addAll(currentChildList);
            this.mReclaimedRecordMap.put(keyValue, validReclaimedSet);
        }
        int[] failArray = AppHibernateCst.EMPTY_INT_ARRAY;
        if (isTopFrontApp(keyValue)) {
            this.mAppHiberMgr.doHiberFrzApi(keyValue.mPkgName, pidArray, 0);
            failArray = pidArray;
            AwareLog.i(TAG_PG, keyValue.mPkgName + "  is Front, stop reclaim.");
        } else {
            this.mReclaimingApp = keyValue;
            failArray = this.mAppHiberMgr.doHiberReclaimApi();
            this.mReclaimingApp = INVALID_ABSAPPINFO;
        }
        if (failArray.length > 0) {
            List<HiberAppInfo> tmpList = new ArrayList();
            for (int pid : failArray) {
                for (HiberAppInfo aware : currentChildList) {
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

    private boolean getPGSdk() {
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
        if (this.mRunning.get()) {
            this.mLastInputEvent = lastInputEvent;
            this.mLastInputTime = lastInputTime;
            if (this.mLastInputEvent == 10001) {
                foreceInterruptReclaim();
            }
            return 0;
        }
        AwareLog.w(TAG_PG, "setLastInputEventData     failed  , because AppHibernateTask is not  enable");
        return -1;
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
        } else {
            AwareLog.i(TAG_PG, "mLastInputEvent=" + this.mLastInputEvent + " is not EVENT_TOUCH_DOWN/UP, Neglect!");
            return false;
        }
    }

    private boolean isCpuLoadHeavy() {
        return this.mCpuReader.getCpuPercent() > MemoryConstant.getNormalThresHold();
    }

    public ArrayMap<Integer, HiberAppInfo> getRelaimedRecord() {
        if (this.mRunning.get()) {
            synchronized (this.mReclaimedRecordMap) {
                if (this.mReclaimedRecordMap.isEmpty()) {
                    AwareLog.i(TAG_PG, "current  reclaimed record is Empty");
                    return null;
                }
                ArrayMap<Integer, HiberAppInfo> returnMap = new ArrayMap();
                for (Entry<AbsAppInfo, ArraySet<HiberAppInfo>> entry : this.mReclaimedRecordMap.entrySet()) {
                    for (HiberAppInfo appinfo : (ArraySet) entry.getValue()) {
                        returnMap.put(Integer.valueOf(appinfo.mPid), appinfo);
                    }
                }
                return returnMap;
            }
        }
        AwareLog.w(TAG_PG, "getRelaimedList     failed  , because AppHibernateTask is not  enable");
        return null;
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
