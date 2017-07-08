package com.android.internal.telephony.vsim;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.ServiceState;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwAllInOneController;
import com.android.internal.telephony.HwFullNetwork;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.HwVSimPhone;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.DcFailCause;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.HwVSimDctController;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfo;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController.CommrilMode;
import com.android.internal.telephony.vsim.process.HwVSimDReadyProcessor;
import com.android.internal.telephony.vsim.process.HwVSimDWorkProcessor;
import com.android.internal.telephony.vsim.process.HwVSimDefaultProcessor;
import com.android.internal.telephony.vsim.process.HwVSimDisableProcessor;
import com.android.internal.telephony.vsim.process.HwVSimEReadyProcessor;
import com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor;
import com.android.internal.telephony.vsim.process.HwVSimEnableProcessor;
import com.android.internal.telephony.vsim.process.HwVSimInitialProcessor;
import com.android.internal.telephony.vsim.process.HwVSimRcReadyProcessor;
import com.android.internal.telephony.vsim.process.HwVSimRcWorkProcessor;
import com.android.internal.telephony.vsim.process.HwVSimReconnectProcessor;
import com.android.internal.telephony.vsim.process.HwVSimSReadyProcessor;
import com.android.internal.telephony.vsim.process.HwVSimSWorkProcessor;
import com.android.internal.telephony.vsim.process.HwVSimSwitchModeProcessor;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwVSimController extends StateMachine {
    public static final int CARDTYPE_INVALID = -1;
    private static final int CARDTYPE_MAIN = 1;
    private static final int CARDTYPE_SUB = 2;
    private static final int EVENT_AUTO_SET_ALLOW_DATA = 5;
    private static final int EVENT_AUTO_SWITCH_SLOT = 4;
    private static final int EVENT_SERVICE_STATE_CHANGE = 1;
    private static final int EVENT_SETUP_DATA_ON_VSIM_ENDED = 2;
    private static final int EVENT_UPDATE_SUB_ACTIVATION = 3;
    private static final int EVENT_UPDATE_USER_PREFERENCES = 1;
    public static final int GETMODE_RESULT_ERROR = -1;
    public static final int GETMODE_RESULT_SIM = 0;
    public static final int GETMODE_RESULT_VSIM = 1;
    private static final String HSF_PKG_NAME = "com.huawei.android.hsf";
    private static final String LOG_TAG = "VSimController";
    private static final int MAX_RETRY_COUNT = 150;
    private static final int MAX_VSIM_WAIT_TIME = 60000;
    private static final int REPORT_INTERVAL_MILLIS = 5000;
    public static final int STATE_DEFAULT = 0;
    public static final int STATE_DISABLE = 5;
    public static final int STATE_DISABLE_READY = 7;
    public static final int STATE_DISABLE_WORK = 6;
    public static final int STATE_ENABLE = 2;
    public static final int STATE_ENABLE_READY = 4;
    public static final int STATE_ENABLE_WORK = 3;
    public static final int STATE_INITIAL = 1;
    public static final int STATE_RECONNECT = 8;
    public static final int STATE_RECONNECT_READY = 10;
    public static final int STATE_RECONNECT_WORK = 9;
    public static final int STATE_SWITCHMODE = 11;
    public static final int STATE_SWITCHMODE_READY = 13;
    public static final int STATE_SWITCHMODE_WORK = 12;
    private static final int SWITCH_DELAY_TIME = 2000;
    private static final int VSIM_MODEM_COUNT = 0;
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static final int VSIM_OP_ENABLEVSIM = 1;
    private static final int VSIM_OP_ENABLEVSIM_FORHASH = 3;
    public static final int VSIM_OP_ENABLEVSIM_OFFLINE = 5;
    private static final int VSIM_OP_SETAPN = 2;
    private static final int VSIM_OP_SETAPN_FORHASH = 4;
    private static final String VSIM_PKG_NAME = "com.huawei.skytone";
    private static final int VSIM_STATE_POLL_SLEEP_MSEC = 500;
    private static final Object sCheckSubActivedLock = null;
    private static HwVSimController sInstance;
    private static final Object sLock = null;
    private static final Object sSimCardTypesLock = null;
    private static final Object sSubStateLock = null;
    private boolean isWaitingSwitchCdmaModeSide;
    private HwVSimApkObserver mApkObserver;
    private boolean mBlockPinFlag;
    private boolean[] mBlockPinTable;
    private int[] mCardTypes;
    private boolean[] mCheckSubActivated;
    private CommandsInterface[] mCis;
    private Semaphore mCmdSem;
    private AtomicBoolean mCmdSemAcquired;
    private Context mContext;
    private int mCurCardType;
    private VSimDReadyState mDReadyState;
    private VSimDWorkState mDWorkState;
    private VSimDefaultState mDefaultState;
    private boolean mDisableFailMark;
    private HwVSimRequest mDisableRequest;
    private int mDisableRetryCount;
    private VSimDisableState mDisableState;
    private VSimEReadyState mEReadyState;
    private VSimEWorkState mEWorkState;
    private HwVSimRequest mEnableRequest;
    private VSimEnableState mEnableState;
    public VSimEventInfo mEventInfo;
    private InitialState mInitialState;
    private int mInsertedSimCount;
    private boolean mIsRegNetworkReceiver;
    private boolean mIsSubActivationUpdate;
    private boolean mIsVSimCauseCardReload;
    private boolean mIsVSimOn;
    private long mLastReportTime;
    private int mLastRilFailCause;
    private Handler mMainHandler;
    private HwVSimModemAdapter mModemAdapter;
    private boolean[] mNeedSimLoadedMark;
    private int mNetworkScanIsRunning;
    private int mNetworkScanSubId;
    private int mNetworkScanType;
    private final BroadcastReceiver mNetworkStateReceiver;
    private int mOldCardType;
    private Phone[] mPhones;
    private boolean[] mPinBlockedTable;
    protected ProcessAction mProcessAction;
    protected ProcessState mProcessState;
    protected ProcessType mProcessType;
    private boolean[] mProhibitSubUpdateSimNoChange;
    private VSimRcReadyState mRcReadyState;
    private VSimRcWorkState mRcWorkState;
    private VSimReconnectState mReconnectState;
    private int mRetryCountForHotPlug;
    private ServiceStateHandler mServiceStateHandlerForVsim;
    private ServiceStateHandler[] mServiceStateHandlers;
    private int[] mSimCardTypes;
    private int[] mSimSlotsTable;
    private VSimSwitchModeReadyState mSmReadyState;
    private VSimSwitchModeWorkState mSmWorkState;
    private int[] mSubStates;
    private HwVSimRequest mSwitchModeRequest;
    private VSimSwitchModeState mSwitchModeState;
    private UiccController mUiccController;
    private CommandsInterface mVSimCi;
    public long mVSimEnterTime;
    private HwVSimEventHandler mVSimEventHandler;
    private HwVSimEventReport mVSimEventReport;
    private HandlerThread mVSimEventThread;
    private int mVSimFrameworkSupportVer;
    private boolean[] mVSimLockPower;
    private int mVSimModemSupportVer;
    private Phone mVSimPhone;
    private HwVSimSlotSwitchController mVSimSlotSwitchController;
    private HwVSimUiccController mVSimUiccController;
    private HashMap<Integer, String> mWhatToStringMap;

    /* renamed from: com.android.internal.telephony.vsim.HwVSimController.3 */
    class AnonymousClass3 extends Thread {
        final /* synthetic */ long val$endTime;

        AnonymousClass3(long val$endTime) {
            this.val$endTime = val$endTime;
        }

        public void run() {
            HwVSimController.this.logd("Waiting for vsim ...");
            while (SystemClock.elapsedRealtime() < this.val$endTime) {
                if (HwVSimController.this.canProcessDisable()) {
                    HwVSimController.this.logd("vsim idle");
                    break;
                }
                SystemClock.sleep(500);
            }
            if (!HwVSimController.this.canProcessDisable()) {
                HwVSimController.this.logd("Timed out waiting for vsim idle");
            }
        }
    }

    public static class EnableParam {
        public String acqorder;
        public int apnType;
        public int cardType;
        public String challenge;
        public String imsi;
        public boolean isVSimOn;
        public int operation;
        public String taPath;
        public int vsimLoc;

        public EnableParam(String imsi, int cardType, int apnType, String acqorder, String challenge, int operation, String tapath, int vsimloc) {
            this.imsi = imsi;
            this.cardType = cardType;
            this.apnType = apnType;
            this.acqorder = acqorder;
            this.challenge = challenge;
            this.isVSimOn = false;
            this.operation = operation;
            this.taPath = tapath;
            this.vsimLoc = vsimloc;
        }
    }

    private class HwVSimEventHandler extends Handler {
        public static final int EVENT_HOTPLUG_SWITCHMODE = 1000;
        public static final int EVENT_VSIM_DISABLE_RETRY = 1001;

        public HwVSimEventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_HOTPLUG_SWITCHMODE /*1000*/:
                    HwVSimController.this.logd("EVENT_HOTPLUG_SWITCHMODE, count = " + HwVSimController.this.mRetryCountForHotPlug);
                    if (HwVSimController.this.canProcessSwitchMode() && HwVSimController.this.isSubActivedDone()) {
                        HwVSimController.this.switchVSimWorkMode(HwVSimController.this.getWorkMode(), true);
                        return;
                    }
                    HwVSimController.this.handleSwitchModeDelay();
                case EVENT_VSIM_DISABLE_RETRY /*1001*/:
                    HwVSimController.this.logd("EVENT_VSIM_DISABLE_RETRY, count = " + HwVSimController.this.mDisableRetryCount);
                    if (HwVSimController.this.mDisableRetryCount >= HwVSimController.VSIM_OP_ENABLEVSIM_FORHASH) {
                        HwVSimController.this.logd("max count, abort retry");
                        return;
                    }
                    HwVSimController hwVSimController = HwVSimController.this;
                    hwVSimController.mDisableRetryCount = hwVSimController.mDisableRetryCount + HwVSimController.VSIM_OP_ENABLEVSIM;
                    if (HwVSimController.this.mDisableFailMark) {
                        HwVSimController.this.disableVSim();
                    } else {
                        HwVSimController.this.logd("no fail mark, abort retry");
                    }
                default:
            }
        }
    }

    private class InitialState extends State {
        HwVSimInitialProcessor mProcessor;

        private InitialState() {
        }

        public void enter() {
            HwVSimController.this.logd("InitialState: enter");
            this.mProcessor = new HwVSimInitialProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, null);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("InitialState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("InitialState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    public enum ProcessAction {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimController.ProcessAction.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimController.ProcessAction.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimController.ProcessAction.<clinit>():void");
        }

        public boolean isEnableProcess() {
            if (this == PROCESS_ACTION_ENABLE || this == PROCESS_ACTION_ENABLE_OFFLINE) {
                return true;
            }
            return false;
        }

        public boolean isDisableProcess() {
            if (this == PROCESS_ACTION_DISABLE || this == PROCESS_ACTION_DISABLE_OFFLINE) {
                return true;
            }
            return false;
        }

        public boolean isOfflineProcess() {
            if (this == PROCESS_ACTION_ENABLE_OFFLINE || this == PROCESS_ACTION_DISABLE_OFFLINE) {
                return true;
            }
            return false;
        }

        public boolean isReconnectProcess() {
            return this == PROCESS_ACTION_RECONNECT;
        }

        public boolean isSwitchModeProcess() {
            return this == PROCESS_ACTION_SWITCHWORKMODE;
        }
    }

    public enum ProcessState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimController.ProcessState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimController.ProcessState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimController.ProcessState.<clinit>():void");
        }

        public boolean isWorkProcess() {
            return this == PROCESS_STATE_WORK;
        }

        public boolean isReadyProcess() {
            return this == PROCESS_STATE_READY;
        }
    }

    public enum ProcessType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimController.ProcessType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimController.ProcessType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimController.ProcessType.<clinit>():void");
        }

        public boolean isSwapProcess() {
            return this == PROCESS_TYPE_SWAP;
        }

        public boolean isCrossProcess() {
            return this == PROCESS_TYPE_CROSS;
        }

        public boolean isDirectProcess() {
            return this == PROCESS_TYPE_DIRECT;
        }
    }

    private final class ServiceStateHandler extends Handler {
        Phone mPhone;
        ServiceState mSS;
        final /* synthetic */ HwVSimController this$0;

        public ServiceStateHandler(HwVSimController this$0, Looper looper, Phone phone) {
            this.this$0 = this$0;
            super(looper);
            this.mPhone = null;
            this.mSS = new ServiceState();
            this.mPhone = phone;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwVSimController.VSIM_OP_ENABLEVSIM /*1*/:
                    AsyncResult ar = msg.obj;
                    if (ar.exception == null) {
                        ServiceState newSS = ar.result;
                        notifyServiceStateChanged(newSS, this.mSS, this.mPhone);
                        this.mSS = new ServiceState(newSS);
                        break;
                    }
                    this.this$0.logd("exception, return");
            }
        }

        private void notifyServiceStateChanged(ServiceState newSS, ServiceState oldSS, Phone phone) {
            if (newSS == null || oldSS == null || phone == null) {
                this.this$0.logd("ss or phone is null, return!");
                return;
            }
            this.this$0.logd("newSS:" + newSS + " oldSS:" + oldSS);
            boolean registeredStatuChanged = newSS.getVoiceRegState() != oldSS.getVoiceRegState();
            boolean plmnStatuChanged = !equalsHandlesNulls(newSS.getVoiceOperatorNumeric(), oldSS.getVoiceOperatorNumeric());
            this.this$0.logd("notifyServiceStateChanged registeredStatuChanged:" + registeredStatuChanged + " plmnStatuChanged:" + plmnStatuChanged + " phoneId:" + phone.getPhoneId());
            if (registeredStatuChanged || plmnStatuChanged) {
                this.this$0.logd("sendBroadcastAsUser HW_VSIM_SERVICE_STATE_CHANGED");
                int phoneId = phone.getPhoneId();
                int subId = phone.getSubId();
                Intent intent = new Intent(HwVSimConstants.HW_VSIM_SERVICE_STATE_CHANGED);
                Bundle data = new Bundle();
                newSS.fillInNotifierBundle(data);
                intent.putExtras(data);
                intent.putExtra("subscription", subId);
                intent.putExtra("slot", phoneId);
                phone.getContext().sendBroadcastAsUser(intent, UserHandle.ALL, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
            }
        }

        private boolean equalsHandlesNulls(Object a, Object b) {
            if (a == null) {
                return b == null;
            } else {
                return a.equals(b);
            }
        }
    }

    private class VSimDReadyState extends State {
        HwVSimDReadyProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimDReadyState(HwVSimController this$0, VSimDReadyState vSimDReadyState) {
            this(this$0);
        }

        private VSimDReadyState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("DReadyState: enter");
            this.mProcessor = new HwVSimDReadyProcessor(HwVSimController.sInstance, this.this$0.mModemAdapter, this.this$0.mDisableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("DReadyState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("DReadyState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimDWorkState extends State {
        HwVSimDWorkProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimDWorkState(HwVSimController this$0, VSimDWorkState vSimDWorkState) {
            this(this$0);
        }

        private VSimDWorkState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("DWorkState: enter");
            this.mProcessor = new HwVSimDWorkProcessor(HwVSimController.sInstance, this.this$0.mModemAdapter, this.this$0.mDisableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("DWorkState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("DWorkState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimDefaultState extends State {
        HwVSimDefaultProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimDefaultState(HwVSimController this$0, VSimDefaultState vSimDefaultState) {
            this(this$0);
        }

        private VSimDefaultState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("DefaultState: enter");
            this.mProcessor = new HwVSimDefaultProcessor(HwVSimController.sInstance, this.this$0.mModemAdapter, null);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("DefaultState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("DefaultState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }

        public int networksScan(int subId, int type) {
            if (this.mProcessor != null) {
                return this.mProcessor.networksScan(subId, type);
            }
            this.this$0.mNetworkScanIsRunning = HwVSimController.VSIM_MODEM_COUNT;
            return HwVSimController.VSIM_OP_ENABLEVSIM;
        }
    }

    private class VSimDisableState extends State {
        HwVSimDisableProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimDisableState(HwVSimController this$0, VSimDisableState vSimDisableState) {
            this(this$0);
        }

        private VSimDisableState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("DisableState: enter");
            this.this$0.disableEnterReport();
            this.mProcessor = new HwVSimDisableProcessor(HwVSimController.sInstance, this.this$0.mModemAdapter, this.this$0.mDisableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("DisableState: exit");
            this.this$0.disableExitReport();
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("DisableState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimEReadyState extends State {
        HwVSimEReadyProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimEReadyState(HwVSimController this$0, VSimEReadyState vSimEReadyState) {
            this(this$0);
        }

        private VSimEReadyState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("EReadyState: enter");
            this.mProcessor = HwVSimEReadyProcessor.create(HwVSimController.sInstance, this.this$0.mModemAdapter, this.this$0.mEnableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("EReadyState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("EReadyState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimEWorkState extends State {
        HwVSimEWorkProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimEWorkState(HwVSimController this$0, VSimEWorkState vSimEWorkState) {
            this(this$0);
        }

        private VSimEWorkState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("VSimEWorkState: enter");
            this.mProcessor = HwVSimEWorkProcessor.create(HwVSimController.sInstance, this.this$0.mModemAdapter, this.this$0.mEnableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("VSimEWorkState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("EWorkState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimEnableState extends State {
        HwVSimEnableProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimEnableState(HwVSimController this$0, VSimEnableState vSimEnableState) {
            this(this$0);
        }

        private VSimEnableState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("EnableState: enter");
            this.this$0.enableEnterReport();
            this.mProcessor = new HwVSimEnableProcessor(HwVSimController.sInstance, this.this$0.mModemAdapter, this.this$0.mEnableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("EnableState: exit");
            this.this$0.enableExitReport();
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("EnableState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimRcReadyState extends State {
        HwVSimRcReadyProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimRcReadyState(HwVSimController this$0, VSimRcReadyState vSimRcReadyState) {
            this(this$0);
        }

        private VSimRcReadyState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("VSimRcReadyState: enter");
            this.mProcessor = new HwVSimRcReadyProcessor(HwVSimController.sInstance, this.this$0.mModemAdapter, this.this$0.mDisableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("VSimRcReadyState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("VSimRcReadyState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimRcWorkState extends State {
        HwVSimRcWorkProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimRcWorkState(HwVSimController this$0, VSimRcWorkState vSimRcWorkState) {
            this(this$0);
        }

        private VSimRcWorkState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("VSimRcWorkState: enter");
            this.mProcessor = new HwVSimRcWorkProcessor(HwVSimController.sInstance, this.this$0.mModemAdapter, this.this$0.mDisableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("VSimRcWorkState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("VSimRcWorkState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimReconnectState extends State {
        HwVSimReconnectProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimReconnectState(HwVSimController this$0, VSimReconnectState vSimReconnectState) {
            this(this$0);
        }

        private VSimReconnectState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("VSimReconnectState: enter");
            this.this$0.reconnectEnterReport();
            this.mProcessor = new HwVSimReconnectProcessor(HwVSimController.sInstance, this.this$0.mModemAdapter, null);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("VSimReconnectState: exit");
            this.this$0.reconnectExitReport();
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("VSimReconnectState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeReadyState extends State {
        HwVSimSReadyProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimSwitchModeReadyState(HwVSimController this$0, VSimSwitchModeReadyState vSimSwitchModeReadyState) {
            this(this$0);
        }

        private VSimSwitchModeReadyState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("VSimSReadyState: enter");
            this.mProcessor = new HwVSimSReadyProcessor(HwVSimController.sInstance, this.this$0.mModemAdapter, this.this$0.mSwitchModeRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("VSimSReadyState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("VSimSReadyState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeState extends State {
        HwVSimSwitchModeProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimSwitchModeState(HwVSimController this$0, VSimSwitchModeState vSimSwitchModeState) {
            this(this$0);
        }

        private VSimSwitchModeState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("VSimSwitchModeState: enter");
            this.this$0.switchEnterReport();
            this.mProcessor = new HwVSimSwitchModeProcessor(HwVSimController.sInstance, this.this$0.mModemAdapter, this.this$0.mSwitchModeRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("VSimSwitchModeState: exit");
            this.this$0.switchExitReport();
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("VSimSwitchModeState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeWorkState extends State {
        HwVSimSWorkProcessor mProcessor;
        final /* synthetic */ HwVSimController this$0;

        /* synthetic */ VSimSwitchModeWorkState(HwVSimController this$0, VSimSwitchModeWorkState vSimSwitchModeWorkState) {
            this(this$0);
        }

        private VSimSwitchModeWorkState(HwVSimController this$0) {
            this.this$0 = this$0;
        }

        public void enter() {
            this.this$0.logd("VSimSWorkState: enter");
            this.mProcessor = new HwVSimSWorkProcessor(HwVSimController.sInstance, this.this$0.mModemAdapter, this.this$0.mSwitchModeRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            this.this$0.logd("VSimSWorkState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            this.this$0.logd("VSimSWorkState: what = " + this.this$0.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    public static class WorkModeParam {
        public boolean isHotplug;
        public int oldMode;
        public int workMode;

        public WorkModeParam(int workMode, int oldMode, boolean isHotplug) {
            this.workMode = workMode;
            this.oldMode = oldMode;
            this.isHotplug = isHotplug;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimController.<clinit>():void");
    }

    private void initWhatToStringMap() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimController.initWhatToStringMap():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimController.initWhatToStringMap():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimController.initWhatToStringMap():void");
    }

    public static void create(Context context, Phone vsimPhone, CommandsInterface vsimCi, Phone[] phones, CommandsInterface[] cis) {
        slogd("create");
        synchronized (sLock) {
            if (sInstance != null) {
                throw new RuntimeException("VSimController already created");
            }
            sInstance = new HwVSimController(context, vsimPhone, vsimCi, phones, cis);
            sInstance.start();
        }
    }

    public static HwVSimController getInstance() {
        HwVSimController hwVSimController;
        synchronized (sLock) {
            if (sInstance == null) {
                throw new RuntimeException("VSimController not yet created");
            }
            hwVSimController = sInstance;
        }
        return hwVSimController;
    }

    public static boolean isInstantiated() {
        synchronized (sLock) {
            if (sInstance == null) {
                return false;
            }
            return true;
        }
    }

    private HwVSimController(Context context, Phone vsimPhone, CommandsInterface vsimCi, Phone[] phones, CommandsInterface[] cis) {
        int i;
        super(LOG_TAG);
        this.mVSimModemSupportVer = -2;
        this.mVSimFrameworkSupportVer = 10000;
        this.mRetryCountForHotPlug = VSIM_MODEM_COUNT;
        this.mInsertedSimCount = VSIM_MODEM_COUNT;
        this.mNetworkScanIsRunning = VSIM_MODEM_COUNT;
        this.mNetworkScanType = VSIM_MODEM_COUNT;
        this.mNetworkScanSubId = VSIM_MODEM_COUNT;
        this.isWaitingSwitchCdmaModeSide = false;
        this.mMainHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwVSimController.VSIM_OP_ENABLEVSIM /*1*/:
                        HwVSimController.this.logi("mMainHandler: EVENT_UPDATE_USER_PREFERENCES");
                        HwVSimController.this.onUpdateUserPreferences();
                    case HwVSimController.VSIM_OP_SETAPN /*2*/:
                        HwVSimController.this.onSetupDataOnVSimEnded(msg.arg1);
                    case HwVSimController.VSIM_OP_ENABLEVSIM_FORHASH /*3*/:
                        HwVSimController.this.logd("EVENT_UPDATE_SUB_ACTIVATION");
                        HwVSimController.this.onUpdateSubActivation();
                    case HwVSimController.VSIM_OP_SETAPN_FORHASH /*4*/:
                        HwVSimController.this.logd("EVENT_AUTO_SWITCH_SLOT");
                        HwVSimController.this.onAutoSwitchSimSlot();
                    case HwVSimController.VSIM_OP_ENABLEVSIM_OFFLINE /*5*/:
                        HwVSimController.this.logd("EVENT_AUTO_SET_ALLOW_DATA");
                        HwVSimController.this.onAutoSetAllowData();
                    default:
                }
            }
        };
        this.mDefaultState = new VSimDefaultState();
        this.mInitialState = new InitialState();
        this.mEnableState = new VSimEnableState();
        this.mEWorkState = new VSimEWorkState();
        this.mEReadyState = new VSimEReadyState();
        this.mDisableState = new VSimDisableState();
        this.mDWorkState = new VSimDWorkState();
        this.mDReadyState = new VSimDReadyState();
        this.mReconnectState = new VSimReconnectState();
        this.mRcWorkState = new VSimRcWorkState();
        this.mRcReadyState = new VSimRcReadyState();
        this.mSwitchModeState = new VSimSwitchModeState();
        this.mSmWorkState = new VSimSwitchModeWorkState();
        this.mSmReadyState = new VSimSwitchModeReadyState();
        this.mNetworkStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (HwVSimController.this.isNetworkConnected()) {
                    HwVSimController.this.getHandler().obtainMessage(50).sendToTarget();
                }
            }
        };
        logd("HwVSimController");
        initWhatToStringMap();
        this.mCmdSem = new Semaphore(VSIM_OP_ENABLEVSIM);
        this.mCmdSemAcquired = new AtomicBoolean(false);
        this.mContext = context;
        this.mVSimPhone = vsimPhone;
        this.mPhones = phones;
        this.mVSimCi = vsimCi;
        this.mCis = cis;
        this.mModemAdapter = createModemAdapter(context, vsimCi, cis);
        this.mVSimSlotSwitchController = HwVSimSlotSwitchController.getInstance();
        this.mVSimUiccController = HwVSimUiccController.getInstance();
        this.mUiccController = UiccController.getInstance();
        this.mCurCardType = GETMODE_RESULT_ERROR;
        this.mOldCardType = GETMODE_RESULT_ERROR;
        this.mIsRegNetworkReceiver = false;
        this.mVSimEventReport = new HwVSimEventReport(context);
        this.mEventInfo = new VSimEventInfo();
        VSimEventInfoUtils.setCauseType(this.mEventInfo, GETMODE_RESULT_ERROR);
        this.mLastReportTime = 0;
        this.mVSimEnterTime = 0;
        if (!HwVSimUtilsInner.isPlatformRealTripple()) {
            HandlerThread t = new HandlerThread("VSimDctThread");
            t.start();
            HwVSimDctController.makeDctController(this.mVSimPhone, t.getLooper());
        }
        this.mVSimEventThread = new HandlerThread("VSimEventThread");
        this.mVSimEventThread.start();
        this.mVSimEventHandler = new HwVSimEventHandler(this.mVSimEventThread.getLooper());
        this.mApkObserver = new HwVSimApkObserver();
        this.mCardTypes = null;
        this.mSimSlotsTable = null;
        this.mIsVSimOn = false;
        this.mVSimLockPower = new boolean[HwVSimModemAdapter.MAX_SUB_COUNT];
        for (i = VSIM_MODEM_COUNT; i < HwVSimModemAdapter.MAX_SUB_COUNT; i += VSIM_OP_ENABLEVSIM) {
            this.mVSimLockPower[i] = false;
        }
        this.mIsVSimCauseCardReload = false;
        this.mNeedSimLoadedMark = new boolean[HwVSimModemAdapter.MAX_SUB_COUNT];
        for (i = VSIM_MODEM_COUNT; i < HwVSimModemAdapter.MAX_SUB_COUNT; i += VSIM_OP_ENABLEVSIM) {
            this.mNeedSimLoadedMark[i] = false;
        }
        this.mIsSubActivationUpdate = false;
        this.mSubStates = new int[HwVSimModemAdapter.PHONE_COUNT];
        this.mSimCardTypes = new int[HwVSimModemAdapter.PHONE_COUNT];
        this.mCheckSubActivated = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        this.mProhibitSubUpdateSimNoChange = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        for (i = VSIM_MODEM_COUNT; i < this.mSubStates.length; i += VSIM_OP_ENABLEVSIM) {
            this.mSimCardTypes[i] = GETMODE_RESULT_ERROR;
            this.mSubStates[i] = GETMODE_RESULT_ERROR;
            this.mCheckSubActivated[i] = false;
            this.mProhibitSubUpdateSimNoChange[i] = false;
        }
        this.mBlockPinFlag = false;
        this.mBlockPinTable = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        this.mPinBlockedTable = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        this.mDisableRetryCount = VSIM_OP_ENABLEVSIM_FORHASH;
        this.mDisableFailMark = false;
        logi("VSIM_MODEM_COUNT: " + VSIM_MODEM_COUNT);
        addServiceStateChangeListener();
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mEnableState, this.mDefaultState);
        addState(this.mDisableState, this.mDefaultState);
        addState(this.mReconnectState, this.mDefaultState);
        addState(this.mSwitchModeState, this.mDefaultState);
        addState(this.mEWorkState, this.mEnableState);
        addState(this.mEReadyState, this.mEnableState);
        addState(this.mDWorkState, this.mDisableState);
        addState(this.mDReadyState, this.mDisableState);
        addState(this.mRcWorkState, this.mReconnectState);
        addState(this.mRcReadyState, this.mReconnectState);
        addState(this.mSmWorkState, this.mSwitchModeState);
        addState(this.mSmReadyState, this.mSwitchModeState);
        setInitialState(this.mInitialState);
    }

    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    public void logi(String s) {
        HwVSimLog.VSimLogI(LOG_TAG, s);
    }

    private static void slogd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private void addServiceStateChangeListener() {
        this.mServiceStateHandlers = new ServiceStateHandler[this.mPhones.length];
        for (int i = VSIM_MODEM_COUNT; i < this.mPhones.length; i += VSIM_OP_ENABLEVSIM) {
            this.mServiceStateHandlers[i] = new ServiceStateHandler(this, Looper.myLooper(), this.mPhones[i]);
            this.mPhones[i].registerForServiceStateChanged(this.mServiceStateHandlers[i], VSIM_OP_ENABLEVSIM, null);
        }
        this.mServiceStateHandlerForVsim = new ServiceStateHandler(this, Looper.myLooper(), this.mVSimPhone);
        this.mVSimPhone.registerForServiceStateChanged(this.mServiceStateHandlerForVsim, VSIM_OP_ENABLEVSIM, null);
    }

    protected void unhandledMessage(Message msg) {
        logd(" - unhandledMessage: msg.what=" + msg.what);
    }

    protected String getWhatToString(int what) {
        String str = null;
        if (this.mWhatToStringMap != null) {
            str = (String) this.mWhatToStringMap.get(Integer.valueOf(what));
        }
        if (str == null) {
            return "<unknown message> - " + what;
        }
        return str;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        pw.println(" mSimSlotsTable=" + (this.mSimSlotsTable == null ? "[]" : Arrays.toString(this.mSimSlotsTable)));
    }

    public int enableVSim(int operation, String imsi, int cardType, int apnType, String acqorder, String tapath, int vsimloc, String challenge) {
        logd("enableVSim, operation: " + operation + ", cardType: " + cardType + ", apnType: " + apnType + ", acqorder: " + acqorder + ", vsimloc: " + vsimloc);
        switch (operation) {
            case VSIM_OP_ENABLEVSIM /*1*/:
                return enableVSim(imsi, cardType, apnType, acqorder, tapath, vsimloc, challenge, operation);
            case VSIM_OP_SETAPN /*2*/:
                return setApn(imsi, cardType, apnType, tapath, challenge, false);
            case VSIM_OP_ENABLEVSIM_OFFLINE /*5*/:
                return VSIM_OP_ENABLEVSIM_FORHASH;
            default:
                logd("enableVSim do nothing");
                return VSIM_OP_ENABLEVSIM_FORHASH;
        }
    }

    public boolean disableVSim() {
        logd("disableVSim");
        if (!isVSimEnabled()) {
            logi("VSIM is disabled already, disableVSim result = true!");
            return true;
        } else if (isProcessInit()) {
            logi("VSIM is initing, disableVSim result = false!");
            return false;
        } else {
            cmdSem_acquire();
            if (!canProcessDisable()) {
                Handler h = getHandler();
                if (h != null) {
                    h.sendEmptyMessage(72);
                }
            }
            waitVSimIdle(MAX_VSIM_WAIT_TIME);
            logd("disableVSim subId = " + VSIM_OP_SETAPN);
            Object oResult = sendRequest(52, null, VSIM_OP_SETAPN);
            boolean iResult = false;
            if (oResult != null) {
                iResult = ((Boolean) oResult).booleanValue();
            }
            if (iResult) {
                this.mDisableFailMark = false;
                logd("remove EVENT_VSIM_DISABLE_RETRY");
                this.mVSimEventHandler.removeMessages(HwVSimEventHandler.EVENT_VSIM_DISABLE_RETRY);
                HwVSimPhoneFactory.setVSimEnabledSubId(GETMODE_RESULT_ERROR);
                HwVSimPhoneFactory.setVSimUserEnabled(VSIM_MODEM_COUNT);
                if (this.mApkObserver != null) {
                    this.mApkObserver.stopWatching();
                }
                if (!HwVSimUtilsInner.isPlatformRealTripple()) {
                    setupDataOnVSimEnded();
                }
            } else {
                this.mDisableFailMark = true;
                logd("send EVENT_VSIM_DISABLE_RETRY");
                this.mVSimEventHandler.sendEmptyMessageDelayed(HwVSimEventHandler.EVENT_VSIM_DISABLE_RETRY, HwVSimConstants.VSIM_DISABLE_RETRY_TIMEOUT);
            }
            logd("disableVSim result = " + iResult);
            return iResult;
        }
    }

    public boolean switchVSimWorkMode(int workMode) {
        logd("switchVSimWorkMode : " + workMode);
        cmdSem_acquire();
        return switchVSimWorkMode(workMode, false);
    }

    private boolean switchVSimWorkMode(int workMode, boolean isHotplug) {
        int oldMode = getWorkMode();
        saveVSimWorkMode(workMode);
        Object oResult = sendRequest(58, new WorkModeParam(workMode, oldMode, isHotplug), VSIM_OP_SETAPN);
        boolean iResult = false;
        if (oResult != null) {
            iResult = ((Boolean) oResult).booleanValue();
        }
        if (!iResult) {
            saveVSimWorkMode(oldMode);
        }
        logd("switchVSimWorkMode result = " + iResult);
        return iResult;
    }

    public int getVSimSubId() {
        return HwVSimPhoneFactory.getVSimEnabledSubId();
    }

    public void setModemVSimVersion(int version) {
        this.mVSimModemSupportVer = version;
    }

    public int getPlatformSupportVSimVer(int key) {
        switch (key) {
            case VSIM_MODEM_COUNT /*0*/:
                return this.mVSimModemSupportVer;
            case VSIM_OP_ENABLEVSIM /*1*/:
                return this.mVSimFrameworkSupportVer;
            default:
                return GETMODE_RESULT_ERROR;
        }
    }

    public boolean setVSimULOnlyMode(boolean isULOnly) {
        HwVSimPhoneFactory.setVSimULOnlyMode(isULOnly);
        return true;
    }

    public boolean getVSimULOnlyMode() {
        return HwVSimPhoneFactory.getVSimULOnlyMode(true);
    }

    public boolean setUserReservedSubId(int subId) {
        logd("setUserReservedSubId, subId = " + subId);
        HwVSimPhoneFactory.setVSimUserReservedSubId(subId);
        return true;
    }

    public int getUserReservedSubId() {
        return HwVSimPhoneFactory.getVSimUserReservedSubId();
    }

    public String getRegPlmn(int subId) {
        logd("getRegPlmn, subId = " + subId);
        if (subId == 0 || subId == VSIM_OP_ENABLEVSIM || subId == VSIM_OP_SETAPN) {
            return (String) sendRequest(STATE_RECONNECT_READY, null, subId);
        }
        return null;
    }

    public String getTrafficData() {
        logd("getTrafficData");
        int subId = getVSimSubId();
        if (subId != VSIM_OP_SETAPN) {
            logd("getTrafficData VSim not enabled");
            return null;
        }
        String callingAppName = getCallingAppName();
        if (VSIM_PKG_NAME.equals(callingAppName) || HSF_PKG_NAME.equals(callingAppName)) {
            return Arrays.toString((String[]) sendRequest(14, null, subId));
        }
        logd("getTrafficData not allowed, calling app is " + getCallingAppName());
        return null;
    }

    public boolean clearTrafficData() {
        logd("clearTrafficData");
        int subId = getVSimSubId();
        if (subId != VSIM_OP_SETAPN) {
            logd("clearTrafficData VSim not enabled");
            return false;
        }
        String callingAppName = getCallingAppName();
        if (VSIM_PKG_NAME.equals(callingAppName) || HSF_PKG_NAME.equals(callingAppName)) {
            boolean result = ((Boolean) sendRequest(STATE_SWITCHMODE_WORK, null, subId)).booleanValue();
            logd("clearTrafficData result = " + result);
            return result;
        }
        logd("clearTrafficData not allowed, calling app is " + getCallingAppName());
        return false;
    }

    public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) {
        int subId = getVSimSubId();
        logd("dsFlowCfg, subId = " + subId);
        if (subId != VSIM_OP_SETAPN) {
            logd("dsFlowCfg VSim not enabled");
            return false;
        }
        String callingAppName = getCallingAppName();
        if (VSIM_PKG_NAME.equals(callingAppName) || HSF_PKG_NAME.equals(callingAppName)) {
            boolean result = setApDsFlowCfg(subId, repFlag, threshold, totalThreshold, oper);
            if (result && repFlag == VSIM_OP_ENABLEVSIM) {
                setDsFlowNvCfg(subId, VSIM_OP_ENABLEVSIM, STATE_RECONNECT_READY);
            } else {
                setDsFlowNvCfg(subId, VSIM_MODEM_COUNT, VSIM_MODEM_COUNT);
            }
            return result;
        }
        logd("dsFlowCfg not allowed, calling app is " + getCallingAppName());
        return false;
    }

    public int getSimStateViaSysinfoEx(int subId) {
        logd("getSimStateViaSysinfoEx");
        if (subId == VSIM_OP_SETAPN) {
            return ((Integer) sendRequest(22, null, subId)).intValue();
        }
        logd("getSimStateViaSysinfoEx VSim not enabled");
        return GETMODE_RESULT_ERROR;
    }

    public String getDevSubMode(int subId) {
        logd("getDevSubMode");
        if (subId == VSIM_OP_SETAPN) {
            return (String) sendRequest(25, null, subId);
        }
        logd("getDevSubMode VSim not enabled");
        return null;
    }

    public String getPreferredNetworkTypeForVSim(int subId) {
        logd("getPreferredNetworkTypeForVSim");
        if (subId == VSIM_OP_SETAPN) {
            return (String) sendRequest(27, null, subId);
        }
        logd("getPreferredNetworkTypeForVSim VSim not enabled");
        return null;
    }

    public int getSimMode(int subId) {
        if (subId != VSIM_OP_SETAPN) {
            return VSIM_MODEM_COUNT;
        }
        return VSIM_OP_ENABLEVSIM;
    }

    public int getVSimCurCardType() {
        int cardType = this.mCurCardType;
        logd("getVSimCurCardType = " + cardType);
        return cardType;
    }

    public boolean isVSimInProcess() {
        if (this.mProcessAction == null) {
            return false;
        }
        boolean z;
        if (this.mProcessAction.isEnableProcess() || this.mProcessAction.isDisableProcess()) {
            z = true;
        } else {
            z = this.mProcessAction.isSwitchModeProcess();
        }
        return z;
    }

    public boolean isVSimOn() {
        return this.mIsVSimOn;
    }

    public void setLastRilFailCause(int cause) {
        logd("setLastRilFailCause cause=" + cause);
        this.mLastRilFailCause = cause;
        if (DcFailCause.fromInt(cause).isPermanentFail()) {
            logd("setLastRilFailCause sendBroadcast");
            Intent intent = new Intent("com.huawei.vsim.action.DIAL_FAILED_ACTION");
            intent.putExtra("reason", cause);
            this.mContext.sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
        }
    }

    public int getLastRilFailCause() {
        return this.mLastRilFailCause;
    }

    public int dialupForVSim() {
        logd("dialupForVSim");
        if (!this.mIsVSimOn || this.mVSimPhone == null || this.mVSimPhone.mDcTracker == null) {
            return GETMODE_RESULT_ERROR;
        }
        logd("dialupForVSim EVENT_TRY_SETUP_DATA");
        DcTracker dcTracker = this.mVSimPhone.mDcTracker;
        dcTracker.sendMessage(dcTracker.obtainMessage(270339, "userDataEnabled"));
        return VSIM_MODEM_COUNT;
    }

    public void setUserSwitchDualCardSlots(int subscription) {
        if (this.mVSimSlotSwitchController != null) {
            this.mVSimSlotSwitchController.setUserSwitchDualCardSlots(subscription);
        }
    }

    public boolean isSubOnM2(int subId) {
        boolean z = false;
        if (this.mSimSlotsTable == null) {
            return false;
        }
        if (this.mSimSlotsTable[VSIM_OP_SETAPN] == subId) {
            z = true;
        }
        return z;
    }

    public void disposeCard(int index) {
        if (this.mUiccController != null) {
            this.mUiccController.disposeCard(index);
        }
    }

    public void processHotPlug(int[] cardTypes) {
        boolean needSwtich = needSwitchModeHotplug(cardTypes);
        this.mRetryCountForHotPlug = VSIM_MODEM_COUNT;
        if (this.mVSimEventHandler.hasMessages(HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE)) {
            this.mVSimEventHandler.removeMessages(HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
        }
        if (needSwtich) {
            initCheckSubActived(cardTypes);
            Message msg = this.mVSimEventHandler.obtainMessage();
            msg.what = HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE;
            this.mVSimEventHandler.sendMessage(msg);
        }
    }

    private void handleSwitchModeDelay() {
        if (this.mVSimEventHandler.hasMessages(HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE)) {
            this.mVSimEventHandler.removeMessages(HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
        }
        this.mRetryCountForHotPlug += VSIM_OP_ENABLEVSIM;
        if (this.mRetryCountForHotPlug > MAX_RETRY_COUNT) {
            logd("handleSwitchModeDelay has retry 150 times, no try again");
            this.mRetryCountForHotPlug = VSIM_MODEM_COUNT;
            return;
        }
        Message msg = this.mVSimEventHandler.obtainMessage();
        msg.what = HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE;
        this.mVSimEventHandler.sendMessageDelayed(msg, 2000);
    }

    public IccCardConstants.State modifySimStateForVsim(int phoneId, IccCardConstants.State s) {
        if (!HwVSimUtilsInner.isPlatformRealTripple()) {
            boolean isSubOnM2 = isSubOnM2(phoneId);
            boolean isULGMode = !getULOnlyProp();
            logd("modifySimStateForVsim : phoneid = " + phoneId + ", sub on M2 is " + isSubOnM2 + ", ULG Mode is " + isULGMode);
            if (isULGMode && isSubOnM2) {
                logd("modifySimStateForVsim  : State = " + s + " to ABSENT.");
                return IccCardConstants.State.ABSENT;
            }
        }
        return s;
    }

    public boolean needBlockUnReservedForVsim(int subId) {
        if (!(HwVSimUtilsInner.isPlatformRealTripple() || isPlatformTwoModems())) {
            boolean isSubOnM2 = isSubOnM2(subId);
            boolean isULGMode = !getULOnlyProp();
            logd("needBlockForVsim : phoneid = " + subId + ", sub on M2 is " + isSubOnM2 + ", ULG Mode is " + isULGMode);
            if (isULGMode && isSubOnM2) {
                logd("needBlockForVsim : true");
                return true;
            }
        }
        return false;
    }

    public boolean isVSimCauseCardReload() {
        logd("get isVSimCauseCardReload: " + this.mIsVSimCauseCardReload);
        return this.mIsVSimCauseCardReload;
    }

    public void setVSimCauseCardReload(boolean value) {
        this.mIsVSimCauseCardReload = value;
        logd("mIsVSimCauseCardReload = " + this.mIsVSimCauseCardReload);
        if (!value) {
            updateSubActivation();
        }
    }

    public void setMarkForCardReload(int subId, boolean value) {
        logd("set mNeedSimLoadedMark[" + subId + "] = " + value);
        boolean oldAllFalse = true;
        int i = VSIM_MODEM_COUNT;
        while (i < HwVSimModemAdapter.MAX_SUB_COUNT) {
            if (isPlatformTwoModems() && !getCiBySub(i).isRadioAvailable()) {
                logi("setMarkForCardReload skip pending sub" + i);
            } else if (this.mNeedSimLoadedMark[i]) {
                oldAllFalse = false;
            }
            i += VSIM_OP_ENABLEVSIM;
        }
        logd("oldAllFalse = " + oldAllFalse);
        this.mNeedSimLoadedMark[subId] = value;
        boolean newAllFalse = true;
        i = VSIM_MODEM_COUNT;
        while (i < HwVSimModemAdapter.MAX_SUB_COUNT) {
            if (isPlatformTwoModems() && !getCiBySub(i).isRadioAvailable()) {
                logi("setMarkForCardReload skip pending sub" + i);
            } else if (this.mNeedSimLoadedMark[i]) {
                newAllFalse = false;
            }
            i += VSIM_OP_ENABLEVSIM;
        }
        logd("newAllFalse = " + newAllFalse);
        if ((oldAllFalse && !newAllFalse) || (!oldAllFalse && newAllFalse)) {
            setVSimCauseCardReload(value);
            if (getHandler() != null) {
                getHandler().removeMessages(61);
                if (value) {
                    getHandler().sendEmptyMessageDelayed(61, HwVSimConstants.CARD_RELOAD_TIMEOUT);
                }
            }
        }
        logd("set mNeedSimLoadedMark[" + subId + "] = " + value + " funtion end");
    }

    public void clearAllMarkForCardReload() {
        logd("clear all mark for mNeedSimLoadedMark");
        for (int i = VSIM_MODEM_COUNT; i < HwVSimModemAdapter.MAX_SUB_COUNT; i += VSIM_OP_ENABLEVSIM) {
            this.mNeedSimLoadedMark[i] = false;
        }
    }

    public void setVSimSavedMainSlot(int subId) {
        HwVSimPhoneFactory.setVSimSavedMainSlot(subId);
    }

    public int getVSimSavedMainSlot() {
        return HwVSimPhoneFactory.getVSimSavedMainSlot();
    }

    public boolean setApDsFlowCfg(int subId, int config, int threshold, int total_threshold, int oper) {
        logd("setApDsFlowCfg");
        int[] param = new int[VSIM_OP_SETAPN_FORHASH];
        param[VSIM_MODEM_COUNT] = config;
        param[VSIM_OP_ENABLEVSIM] = threshold;
        param[VSIM_OP_SETAPN] = total_threshold;
        param[VSIM_OP_ENABLEVSIM_FORHASH] = oper;
        return ((Boolean) sendRequest(16, param, subId)).booleanValue();
    }

    public boolean setDsFlowNvCfg(int subId, int enable, int interval) {
        logd("setDsFlowNvCfg, enable = " + enable);
        int[] param = new int[VSIM_OP_SETAPN];
        param[VSIM_MODEM_COUNT] = enable;
        param[VSIM_OP_ENABLEVSIM] = interval;
        return ((Boolean) sendRequest(18, param, subId)).booleanValue();
    }

    public int networksScan(int subId, int type) {
        if (this.mNetworkScanIsRunning == VSIM_OP_ENABLEVSIM) {
            logd("networksScan is running");
            return VSIM_OP_ENABLEVSIM;
        } else if (subId != VSIM_OP_SETAPN) {
            return VSIM_MODEM_COUNT;
        } else {
            this.mNetworkScanIsRunning = VSIM_OP_ENABLEVSIM;
            this.mNetworkScanSubId = subId;
            this.mNetworkScanType = type;
            return this.mDefaultState.networksScan(subId, type);
        }
    }

    public boolean isVSimEnabled() {
        return HwVSimPhoneFactory.getVSimEnabledSubId() != GETMODE_RESULT_ERROR;
    }

    public void saveNetworkMode(int modemNetworkMode) {
        int savedNetworkMode = HwVSimPhoneFactory.getVSimSavedNetworkMode();
        logd("savedNetworkMode = " + savedNetworkMode);
        if (savedNetworkMode == GETMODE_RESULT_ERROR) {
            savedNetworkMode = modemNetworkMode;
            HwVSimPhoneFactory.setVSimSavedNetworkMode(modemNetworkMode);
        }
    }

    public int writeVsimToTA(String imsi, int cardType, int apnType, String challenge, String taPath, int vsimLoc, int modemID) {
        logd("writeVsimToTA");
        if (challenge != null && challenge.length() != 0) {
            return new HwVSimOperateTA().operateTA(VSIM_OP_ENABLEVSIM, imsi, cardType, apnType, challenge, false, taPath, vsimLoc, modemID);
        }
        logd("invalid param challenge");
        return STATE_DISABLE_WORK;
    }

    public void transitionToState(int state) {
        switch (state) {
            case VSIM_MODEM_COUNT /*0*/:
                transitionTo(this.mDefaultState);
            case VSIM_OP_ENABLEVSIM /*1*/:
                transitionTo(this.mInitialState);
            case VSIM_OP_SETAPN /*2*/:
                transitionTo(this.mEnableState);
            case VSIM_OP_ENABLEVSIM_FORHASH /*3*/:
                transitionTo(this.mEWorkState);
            case VSIM_OP_SETAPN_FORHASH /*4*/:
                transitionTo(this.mEReadyState);
            case VSIM_OP_ENABLEVSIM_OFFLINE /*5*/:
                transitionTo(this.mDisableState);
            case STATE_DISABLE_WORK /*6*/:
                transitionTo(this.mDWorkState);
            case STATE_DISABLE_READY /*7*/:
                transitionTo(this.mDReadyState);
            case STATE_RECONNECT /*8*/:
                transitionTo(this.mReconnectState);
            case STATE_RECONNECT_WORK /*9*/:
                transitionTo(this.mRcWorkState);
            case STATE_RECONNECT_READY /*10*/:
                transitionTo(this.mRcReadyState);
            case STATE_SWITCHMODE /*11*/:
                transitionTo(this.mSwitchModeState);
            case STATE_SWITCHMODE_WORK /*12*/:
                transitionTo(this.mSmWorkState);
            case STATE_SWITCHMODE_READY /*13*/:
                transitionTo(this.mSmReadyState);
            default:
        }
    }

    public CommandsInterface getCiBySub(int subId) {
        return HwVSimUtilsInner.getCiBySub(subId, this.mVSimCi, this.mCis);
    }

    public Phone getPhoneBySub(int subId) {
        return HwVSimUtilsInner.getPhoneBySub(subId, this.mVSimPhone, this.mPhones);
    }

    public CommrilMode getCommrilMode() {
        if (this.mVSimSlotSwitchController == null) {
            return (CommrilMode) Enum.valueOf(CommrilMode.class, "CLG_MODE");
        }
        return this.mVSimSlotSwitchController.getCommrilMode();
    }

    public CommrilMode getExpectCommrilMode(int mainSlot, int[] cardType) {
        if (this.mVSimSlotSwitchController == null) {
            return CommrilMode.NON_MODE;
        }
        return this.mVSimSlotSwitchController.getExpectCommrilMode(mainSlot, cardType);
    }

    public int getChinaTelecomMainSlot(int[] cardType) {
        return VSIM_MODEM_COUNT;
    }

    public void switchCommrilMode(CommrilMode expectCommrilMode, int expectSlot, int mainSlot, boolean isVSimOn, Message onCompleteMsg) {
        if (this.mVSimSlotSwitchController != null) {
            if (this.mProcessAction == null || !this.mProcessAction.isEnableProcess()) {
                setBlockPinFlag(false);
            } else {
                setBlockPinFlag(true);
                setBlockPinTable(expectSlot, true);
                if (this.mModemAdapter != null) {
                    this.mModemAdapter.setHwVSimPowerOnOff(mainSlot, true);
                }
            }
            this.mVSimSlotSwitchController.switchCommrilMode(expectCommrilMode, expectSlot, mainSlot, isVSimOn, onCompleteMsg);
        }
    }

    public int clearVsimToTA() {
        logd("clearVsimToTA");
        return new HwVSimOperateTA().operateTA(VSIM_OP_ENABLEVSIM_FORHASH, null, VSIM_MODEM_COUNT, VSIM_MODEM_COUNT, null, false, null, VSIM_MODEM_COUNT, VSIM_MODEM_COUNT);
    }

    public void broadcastVSimServiceReady() {
        logd("broadcastVSimServiceReady");
        this.mContext.sendStickyBroadcastAsUser(new Intent("com.huawei.vsim.action.VSIM_SERVICE_READY"), UserHandle.ALL);
    }

    public void setSimSlotTable(int[] slots) {
        if (slots != null) {
            this.mSimSlotsTable = (int[]) slots.clone();
        }
    }

    public int[] getSimSlotTable() {
        return this.mSimSlotsTable != null ? (int[]) this.mSimSlotsTable.clone() : new int[VSIM_MODEM_COUNT];
    }

    public void setIsVSimOn(boolean isVSimOn) {
        this.mIsVSimOn = isVSimOn;
        logd("setIsVSimOn mIsVSimOn = " + this.mIsVSimOn);
    }

    public boolean getIsVSimOn() {
        return this.mIsVSimOn;
    }

    public boolean isEnableProcess() {
        if (this.mProcessAction == null) {
            return false;
        }
        return this.mProcessAction.isEnableProcess();
    }

    public boolean isDisableProcess() {
        if (this.mProcessAction == null) {
            return false;
        }
        return this.mProcessAction.isDisableProcess();
    }

    public boolean isReconnectProcess() {
        if (this.mProcessAction == null) {
            return false;
        }
        return this.mProcessAction.isReconnectProcess();
    }

    public boolean isSwitchModeProcess() {
        if (this.mProcessAction == null) {
            return false;
        }
        return this.mProcessAction.isSwitchModeProcess();
    }

    public boolean isSwapProcess() {
        if (this.mProcessType == null) {
            return false;
        }
        return this.mProcessType.isSwapProcess();
    }

    public boolean isCrossProcess() {
        if (this.mProcessType == null) {
            return false;
        }
        return this.mProcessType.isCrossProcess();
    }

    public boolean isDirectProcess() {
        if (this.mProcessType == null) {
            return false;
        }
        return this.mProcessType.isDirectProcess();
    }

    public boolean isWorkProcess() {
        if (this.mProcessState == null) {
            return false;
        }
        return this.mProcessState.isWorkProcess();
    }

    public boolean isReadyProcess() {
        if (this.mProcessState == null) {
            return false;
        }
        return this.mProcessState.isReadyProcess();
    }

    public void setProcessType(ProcessType type) {
        this.mProcessType = type;
    }

    public void setProcessAction(ProcessAction action) {
        this.mProcessAction = action;
    }

    public void setProcessState(ProcessState state) {
        this.mProcessState = state;
    }

    public boolean canProcessEnable(int operation) {
        boolean z = true;
        if (operation == VSIM_OP_ENABLEVSIM_OFFLINE && isVSimEnabled()) {
            logd("canProcessEnable vsim is on, can not process offline request");
            return false;
        }
        boolean z2;
        StringBuilder append = new StringBuilder().append("canProcessEnable defaultstate:");
        if (getCurrentState() == this.mDefaultState) {
            z2 = true;
        } else {
            z2 = false;
        }
        logd(append.append(z2).toString());
        if (getCurrentState() != this.mDefaultState) {
            z = false;
        }
        return z;
    }

    public boolean isProcessInit() {
        return getCurrentState() == this.mInitialState;
    }

    public boolean canProcessDisable() {
        return getCurrentState() == this.mDefaultState;
    }

    public boolean canProcessSwitchMode() {
        return getCurrentState() == this.mDefaultState;
    }

    public void setEnableRequest(HwVSimRequest request) {
        this.mEnableRequest = request;
    }

    public void setDisableRequest(HwVSimRequest request) {
        this.mDisableRequest = request;
    }

    public void setSwitchModeRequest(HwVSimRequest request) {
        this.mSwitchModeRequest = request;
    }

    public void registerForVSimOn(Handler h, int what, Object obj) {
        if (this.mVSimCi != null) {
            this.mVSimCi.registerForOn(h, what, obj);
        }
    }

    public void unregisterForVSimOn(Handler h) {
        if (this.mVSimCi != null) {
            this.mVSimCi.unregisterForOn(h);
        }
    }

    public void registerForVSimNotAvailable(Handler h, int what, Object obj) {
        if (this.mVSimCi != null) {
            this.mVSimCi.registerForNotAvailable(h, what, obj);
        }
    }

    public void unregisterForVSimNotAvailable(Handler h) {
        if (this.mVSimCi != null) {
            this.mVSimCi.unregisterForNotAvailable(h);
        }
    }

    public void setOnVsimRegPLMNSelInfo(Handler h, int what, Object obj) {
        if (h != null && this.mVSimCi != null) {
            this.mVSimCi.setOnVsimRegPLMNSelInfo(h, what, obj);
        }
    }

    public void unSetOnVsimRegPLMNSelInfo(Handler h) {
        if (h != null && this.mVSimCi != null) {
            this.mVSimCi.unSetOnVsimRegPLMNSelInfo(h);
        }
    }

    public void registerForVSimIccChanged(Handler h, int what, Object obj) {
        if (this.mVSimUiccController != null) {
            this.mVSimUiccController.registerForIccChanged(h, what, obj);
        }
    }

    public void unregisterForVSimIccChanged(Handler h) {
        if (this.mVSimUiccController != null) {
            this.mVSimUiccController.unregisterForIccChanged(h);
        }
    }

    public void setULOnlyProp(Boolean isULOnly) {
        HwVSimPhoneFactory.setULOnlyProp(isULOnly.booleanValue());
    }

    public boolean getULOnlyProp() {
        return HwVSimPhoneFactory.getULOnlyProp();
    }

    public EnableParam getEnableParam(HwVSimRequest request) {
        if (request == null) {
            return null;
        }
        EnableParam arg = request.getArgument();
        EnableParam param = null;
        if (arg != null) {
            param = arg;
        }
        return param;
    }

    public WorkModeParam getWorkModeParam(HwVSimRequest request) {
        if (request == null) {
            return null;
        }
        WorkModeParam arg = request.getArgument();
        WorkModeParam param = null;
        if (arg != null) {
            param = arg;
        }
        return param;
    }

    public UiccCard getUiccCard(int subId) {
        if (subId < 0 || subId >= HwVSimModemAdapter.MAX_SUB_COUNT) {
            return null;
        }
        if (subId == VSIM_OP_SETAPN) {
            if (this.mVSimUiccController == null) {
                return null;
            }
            return this.mVSimUiccController.getUiccCard();
        } else if (this.mUiccController == null) {
            return null;
        } else {
            return this.mUiccController.getUiccCard(subId);
        }
    }

    public void allowDefaultData() {
        if (!HwVSimUtilsInner.isPlatformRealTripple()) {
            allowData(VSIM_MODEM_COUNT);
            allowData(VSIM_OP_ENABLEVSIM);
        } else if (this.mIsVSimOn) {
            logd("vsim is on, no need to restore default data");
        } else {
            setInteralDataForDSDS(VSIM_MODEM_COUNT);
            setInteralDataForDSDS(VSIM_OP_ENABLEVSIM);
        }
    }

    private void setInteralDataForDSDS(int dataSubId) {
        logd("setInteralDataForDSDS data sub: " + dataSubId);
        if (dataSubId < 0 || dataSubId >= this.mPhones.length) {
            logd("data sub invalid");
            return;
        }
        Phone phone = getPhoneBySub(dataSubId);
        if (phone == null) {
            logd("phone not found");
            return;
        }
        DcTracker dcTracker = phone.mDcTracker;
        if (dcTracker == null) {
            logd("dctracker not found");
            return;
        }
        logd("call set internal data on sub: " + dataSubId);
        dcTracker.setInternalDataEnabledFlag(true);
    }

    public void allowData(int dataSubId) {
        logd("allowData data sub: " + dataSubId);
        if (dataSubId != VSIM_OP_SETAPN && (dataSubId < 0 || dataSubId >= this.mPhones.length)) {
            logd("data sub invalid");
            return;
        }
        Phone phone = getPhoneBySub(dataSubId);
        if (phone == null) {
            logd("phone not found");
            return;
        }
        DcTracker dcTracker = phone.mDcTracker;
        if (dcTracker == null) {
            logd("dctracker not found");
            return;
        }
        logd("call set data allowed on sub: " + dataSubId);
        dcTracker.setDataAllowed(true, null);
    }

    public void switchDDS() {
        logd("switchDDS");
        ((HwVSimPhone) this.mVSimPhone).updateDataConnectionTracker();
        if (this.mPhones != null) {
            int len = this.mPhones.length;
            logd("call cleanUpAllConnections mProxyPhones.length=" + len);
            for (int phoneId = VSIM_MODEM_COUNT; phoneId < len; phoneId += VSIM_OP_ENABLEVSIM) {
                logd("call cleanUpAllConnections phoneId=" + phoneId);
                Phone phone = this.mPhones[phoneId];
                if (phone == null) {
                    logd("active phone not found");
                } else {
                    DcTracker dcTracker = phone.mDcTracker;
                    if (dcTracker == null) {
                        logd("dcTracker not found");
                    } else {
                        dcTracker.setDataAllowed(false, null);
                        dcTracker.cleanUpAllConnections("DDS switch");
                    }
                }
            }
        }
    }

    public int getCardTypeFromEnableParam(HwVSimRequest request) {
        if (request == null) {
            return GETMODE_RESULT_ERROR;
        }
        EnableParam param = (EnableParam) request.getArgument();
        if (param == null) {
            return GETMODE_RESULT_ERROR;
        }
        return param.cardType;
    }

    public void setVSimCurCardType(int cardType) {
        this.mOldCardType = this.mCurCardType;
        this.mCurCardType = cardType;
        logd("setVSimCurCardType, card type is " + this.mCurCardType + ", old card type is " + this.mOldCardType);
        broadcastVSimCardType();
    }

    public int getVSimOccupiedSubId() {
        int subId = getVSimSubId();
        int reservedSub = getUserReservedSubId();
        if (subId == GETMODE_RESULT_ERROR) {
            return GETMODE_RESULT_ERROR;
        }
        if (getVSimULOnlyMode()) {
            return HwVSimModemAdapter.PHONE_COUNT;
        }
        if (reservedSub == GETMODE_RESULT_ERROR) {
            return GETMODE_RESULT_ERROR;
        }
        if (reservedSub == 0) {
            return VSIM_OP_ENABLEVSIM;
        }
        return VSIM_MODEM_COUNT;
    }

    public boolean isDoingSlotSwitch() {
        return getCurrentState() == this.mDReadyState;
    }

    public int getCardPresentNumeric(boolean[] isCardPresent) {
        if (isCardPresent == null) {
            return GETMODE_RESULT_ERROR;
        }
        if (!isCardPresent[VSIM_MODEM_COUNT] && !isCardPresent[VSIM_OP_ENABLEVSIM]) {
            return VSIM_MODEM_COUNT;
        }
        if (isCardPresent[VSIM_MODEM_COUNT] && !isCardPresent[VSIM_OP_ENABLEVSIM]) {
            return VSIM_OP_ENABLEVSIM;
        }
        if (!isCardPresent[VSIM_MODEM_COUNT] && isCardPresent[VSIM_OP_ENABLEVSIM]) {
            return VSIM_OP_SETAPN;
        }
        if (isCardPresent[VSIM_MODEM_COUNT] && isCardPresent[VSIM_OP_ENABLEVSIM]) {
            return VSIM_OP_ENABLEVSIM_FORHASH;
        }
        return GETMODE_RESULT_ERROR;
    }

    public void registerNetStateReceiver() {
        if (!this.mIsRegNetworkReceiver) {
            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.mContext.registerReceiver(this.mNetworkStateReceiver, mFilter);
            this.mIsRegNetworkReceiver = true;
        }
    }

    public void unregisterNetStateReceiver() {
        if (this.mIsRegNetworkReceiver) {
            this.mContext.unregisterReceiver(this.mNetworkStateReceiver);
            this.mIsRegNetworkReceiver = false;
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (cm != null) {
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = VSIM_MODEM_COUNT; i < info.length; i += VSIM_OP_ENABLEVSIM) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        logi("exist a network connected, type: " + info[i].getType());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void updateCardTypes(int[] cardTypes) {
        if (cardTypes != null) {
            logd("updateCardTypes, cardTypes = " + Arrays.toString(cardTypes));
            this.mCardTypes = (int[]) cardTypes.clone();
        }
    }

    public int getInsertedCardCount() {
        if (this.mCardTypes == null) {
            return VSIM_MODEM_COUNT;
        }
        return getInsertedCardCount((int[]) this.mCardTypes.clone());
    }

    public int getInsertedCardCount(int[] cardTypes) {
        if (cardTypes == null) {
            return VSIM_MODEM_COUNT;
        }
        int cardCount = cardTypes.length;
        if (cardCount == 0) {
            return VSIM_MODEM_COUNT;
        }
        int insertedCardCount = VSIM_MODEM_COUNT;
        for (int i = VSIM_MODEM_COUNT; i < cardCount; i += VSIM_OP_ENABLEVSIM) {
            if (cardTypes[i] != 0) {
                insertedCardCount += VSIM_OP_ENABLEVSIM;
            }
        }
        return insertedCardCount;
    }

    public boolean hasIccCardOnM2() {
        if (this.mCardTypes == null) {
            return false;
        }
        return hasIccCardOnM2((int[]) this.mCardTypes.clone());
    }

    public boolean isPlatformTwoModems() {
        return VSIM_MODEM_COUNT == VSIM_OP_SETAPN;
    }

    public boolean isM2CSOnly() {
        if (this.mModemAdapter == null || !(this.mModemAdapter instanceof HwVSimTriModem)) {
            return false;
        }
        return ((HwVSimTriModem) this.mModemAdapter).isM2CSOnly();
    }

    public boolean isMmsOnM2() {
        if (this.mModemAdapter == null || !(this.mModemAdapter instanceof HwVSimTriModem)) {
            return false;
        }
        return ((HwVSimTriModem) this.mModemAdapter).isMmsOnM2();
    }

    public void checkMmsStart(int subId) {
        if (this.mModemAdapter != null && (this.mModemAdapter instanceof HwVSimTriModem)) {
            ((HwVSimTriModem) this.mModemAdapter).checkMmsStart(subId);
        }
    }

    public void checkMmsStop(int subId) {
        if (this.mModemAdapter != null && (this.mModemAdapter instanceof HwVSimTriModem)) {
            ((HwVSimTriModem) this.mModemAdapter).checkMmsStop(subId);
        }
    }

    public int updateUiccCardCount() {
        this.mInsertedSimCount = VSIM_MODEM_COUNT;
        for (int i = VSIM_MODEM_COUNT; i < HwVSimModemAdapter.PHONE_COUNT; i += VSIM_OP_ENABLEVSIM) {
            UiccCard card = this.mUiccController.getUiccCard(i);
            if (card == null) {
                logd("uicc card " + i + " not ready");
                break;
            }
            logd("updateUiccCardCount cardstate = " + card.getCardState() + " i = " + i);
            if (card.getCardState() == CardState.CARDSTATE_PRESENT) {
                this.mInsertedSimCount += VSIM_OP_ENABLEVSIM;
            }
        }
        logd("updateUiccCardCount mInsertedSimCount = " + this.mInsertedSimCount);
        return this.mInsertedSimCount;
    }

    public int getInsertedSimCount() {
        return this.mInsertedSimCount;
    }

    public void broadcastQueryResults(AsyncResult ar) {
        this.mNetworkScanIsRunning = VSIM_MODEM_COUNT;
        Intent intent = new Intent("android.intent.action.ACTION_NETWORK_SCAN_COMPLETE");
        intent.addFlags(536870912);
        intent.putParcelableArrayListExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_OPEARTORINFO, (ArrayList) ar.result);
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_TYPE, this.mNetworkScanType);
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, this.mNetworkScanSubId);
        logd("type = " + this.mNetworkScanType + ", subId = " + this.mNetworkScanSubId);
        this.mContext.sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    private int enableVSim(String imsi, int cardType, int apnType, String acqorder, String tapath, int vsimloc, String challenge, int operation) {
        logd("enableVSim cardType: " + cardType + " apnType: " + apnType + " acqorder: " + acqorder);
        cmdSem_acquire();
        logd("enableVSim subId = " + VSIM_OP_SETAPN);
        HwVSimPhoneFactory.setVSimEnabledSubId(VSIM_OP_SETAPN);
        EnableParam param = new EnableParam(imsi, cardType, apnType, acqorder, challenge, operation, tapath, vsimloc);
        if (this.mApkObserver != null) {
            this.mApkObserver.startWatching();
        }
        int result = ((Integer) sendRequest(40, param, VSIM_OP_SETAPN)).intValue();
        if (result == 0) {
            HwVSimPhoneFactory.setVSimUserEnabled(VSIM_OP_ENABLEVSIM);
        } else {
            setApDsFlowCfg(VSIM_OP_SETAPN, VSIM_MODEM_COUNT, VSIM_MODEM_COUNT, VSIM_MODEM_COUNT, VSIM_MODEM_COUNT);
            setDsFlowNvCfg(VSIM_OP_SETAPN, VSIM_MODEM_COUNT, VSIM_MODEM_COUNT);
            if (!(result == VSIM_OP_ENABLEVSIM_OFFLINE || HwVSimPhoneFactory.getVSimUserEnabled() == VSIM_OP_ENABLEVSIM)) {
                logd("enable failure, call disable vsim");
                this.mDisableRetryCount = VSIM_MODEM_COUNT;
                disableVSim();
            }
        }
        logd("enableVSim result = " + result);
        return result;
    }

    private Object sendRequest(int command, Object argument, int subId) {
        return HwVSimRequest.sendRequest(getHandler(), command, argument, subId);
    }

    private void enableEnterReport() {
        VSimEventInfoUtils.setPocessType(this.mEventInfo, VSIM_OP_ENABLEVSIM);
        if (getCardTypeFromEnableParam(this.mEnableRequest) == VSIM_OP_ENABLEVSIM) {
            VSimEventInfoUtils.setCardType(this.mEventInfo, VSIM_OP_ENABLEVSIM);
        } else if (getCardTypeFromEnableParam(this.mEnableRequest) == VSIM_OP_SETAPN) {
            VSimEventInfoUtils.setCardType(this.mEventInfo, VSIM_OP_SETAPN);
        } else {
            VSimEventInfoUtils.setCardType(this.mEventInfo, GETMODE_RESULT_ERROR);
        }
        this.mVSimEnterTime = SystemClock.elapsedRealtime();
        logi("vsim enter time is " + this.mVSimEnterTime);
        VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
    }

    private void enableExitReport() {
        int i = VSIM_OP_ENABLEVSIM;
        if (this.mEnableRequest != null) {
            int iResult = VSIM_OP_ENABLEVSIM_FORHASH;
            Object oResult = this.mEnableRequest.getResult();
            if (oResult != null) {
                iResult = ((Integer) oResult).intValue();
            }
            if (iResult == 0) {
                VSimEventInfoUtils.setResultType(this.mEventInfo, VSIM_OP_ENABLEVSIM);
                VSimEventInfoUtils.setSimOperator(this.mEventInfo, getOperatorNumeric());
                VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, GETMODE_RESULT_ERROR);
                VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, GETMODE_RESULT_ERROR);
                VSimEventInfoUtils.setSimMode(this.mEventInfo, GETMODE_RESULT_ERROR);
                VSimEventInfoUtils.setSlotsTable(this.mEventInfo, GETMODE_RESULT_ERROR);
                VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, GETMODE_RESULT_ERROR);
                VSimEventInfoUtils.setCardPresent(this.mEventInfo, GETMODE_RESULT_ERROR);
                VSimEventInfoUtils.setWorkMode(this.mEventInfo, GETMODE_RESULT_ERROR);
            } else {
                VSimEventInfoUtils.setResultType(this.mEventInfo, VSIM_OP_SETAPN);
                VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
                VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
                VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, GETMODE_RESULT_ERROR);
                VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, HwVSimPhoneFactory.getVSimSavedMainSlot());
                VSimEventInfo vSimEventInfo = this.mEventInfo;
                if (this.mIsVSimOn) {
                    i = STATE_SWITCHMODE;
                }
                VSimEventInfoUtils.setSimMode(vSimEventInfo, i);
                VSimEventInfoUtils.setSlotsTable(this.mEventInfo, getSlotsTableNumeric(this.mSimSlotsTable));
                VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, HwVSimPhoneFactory.getVSimSavedNetworkMode());
                VSimEventInfoUtils.setWorkMode(this.mEventInfo, getWorkMode());
            }
        }
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void disableEnterReport() {
        VSimEventInfoUtils.setPocessType(this.mEventInfo, STATE_SWITCHMODE);
    }

    private void disableExitReport() {
        if (this.mDisableRequest != null) {
            boolean iResult = false;
            Object oResult = this.mDisableRequest.getResult();
            if (oResult != null) {
                iResult = ((Boolean) oResult).booleanValue();
            }
            if (iResult) {
                VSimEventInfoUtils.setResultType(this.mEventInfo, VSIM_OP_ENABLEVSIM);
            } else {
                VSimEventInfoUtils.setResultType(this.mEventInfo, VSIM_OP_SETAPN);
            }
        }
        VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
        VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
        VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSimMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSlotsTable(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setCardPresent(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setWorkMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void reconnectEnterReport() {
        VSimEventInfoUtils.setPocessType(this.mEventInfo, VSIM_OP_ENABLEVSIM);
        VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
        this.mVSimEnterTime = SystemClock.elapsedRealtime();
        logi("vsim enter time is " + this.mVSimEnterTime);
        VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
    }

    private void reconnectExitReport() {
        VSimEventInfoUtils.setResultType(this.mEventInfo, VSIM_OP_ENABLEVSIM);
        VSimEventInfoUtils.setSimOperator(this.mEventInfo, getOperatorNumeric());
        VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSimMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSlotsTable(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setCardPresent(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setWorkMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void switchEnterReport() {
        VSimEventInfoUtils.setPocessType(this.mEventInfo, VSIM_OP_ENABLEVSIM);
        VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
        this.mVSimEnterTime = SystemClock.elapsedRealtime();
        logi("vsim enter time is " + this.mVSimEnterTime);
        VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
    }

    private void switchExitReport() {
        if (this.mSwitchModeRequest != null) {
            boolean iResult = false;
            Object oResult = this.mSwitchModeRequest.getResult();
            if (oResult != null) {
                iResult = ((Boolean) oResult).booleanValue();
            }
            if (iResult) {
                VSimEventInfoUtils.setResultType(this.mEventInfo, VSIM_OP_ENABLEVSIM);
            } else {
                VSimEventInfoUtils.setResultType(this.mEventInfo, VSIM_OP_SETAPN);
            }
            VSimEventInfoUtils.setSimOperator(this.mEventInfo, getOperatorNumeric());
            VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, GETMODE_RESULT_ERROR);
            VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, GETMODE_RESULT_ERROR);
            VSimEventInfoUtils.setSimMode(this.mEventInfo, GETMODE_RESULT_ERROR);
            VSimEventInfoUtils.setSlotsTable(this.mEventInfo, GETMODE_RESULT_ERROR);
            VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, GETMODE_RESULT_ERROR);
            VSimEventInfoUtils.setCardPresent(this.mEventInfo, GETMODE_RESULT_ERROR);
            VSimEventInfoUtils.setWorkMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        }
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void setApnReport(int result) {
        VSimEventInfoUtils.setPocessType(this.mEventInfo, VSIM_OP_ENABLEVSIM);
        if (result == 0) {
            VSimEventInfoUtils.setResultType(this.mEventInfo, VSIM_OP_ENABLEVSIM);
        } else {
            VSimEventInfoUtils.setResultType(this.mEventInfo, VSIM_OP_SETAPN);
        }
        VSimEventInfoUtils.setCauseType(this.mEventInfo, VSIM_OP_ENABLEVSIM);
        VSimEventInfoUtils.setCardType(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
        VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
        VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSimMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSlotsTable(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setCardPresent(this.mEventInfo, GETMODE_RESULT_ERROR);
        VSimEventInfoUtils.setWorkMode(this.mEventInfo, GETMODE_RESULT_ERROR);
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void saveVSimWorkMode(int workMode) {
        switch (workMode) {
            case VSIM_MODEM_COUNT /*0*/:
                setVSimULOnlyMode(false);
                setUserReservedSubId(VSIM_MODEM_COUNT);
            case VSIM_OP_ENABLEVSIM /*1*/:
                setVSimULOnlyMode(false);
                setUserReservedSubId(VSIM_OP_ENABLEVSIM);
            case VSIM_OP_SETAPN /*2*/:
                setVSimULOnlyMode(true);
            default:
        }
    }

    private void reportEvent(HwVSimEventReport eventReport, VSimEventInfo eventInfo) {
        if (SystemClock.elapsedRealtime() - this.mLastReportTime < 5000) {
            logi("too short, last report time is " + this.mLastReportTime);
            return;
        }
        if (eventReport != null) {
            eventReport.reportEvent(this.mEventInfo);
            this.mLastReportTime = SystemClock.elapsedRealtime();
        }
        VSimEventInfoUtils.setCauseType(this.mEventInfo, GETMODE_RESULT_ERROR);
    }

    private String getOperatorNumeric() {
        if (this.mVSimUiccController == null) {
            return "";
        }
        IccRecords r = this.mVSimUiccController.getIccRecords(VSIM_OP_ENABLEVSIM);
        if (r == null) {
            return "";
        }
        String operator = r.getOperatorNumeric();
        if (operator == null) {
            operator = "";
        }
        logd("getOperatorNumberic - returning from card: " + operator);
        return operator;
    }

    private int getSlotsTableNumeric(int[] slotsTable) {
        if (slotsTable == null) {
            return GETMODE_RESULT_ERROR;
        }
        if (slotsTable[VSIM_MODEM_COUNT] == 0 && slotsTable[VSIM_OP_ENABLEVSIM] == VSIM_OP_ENABLEVSIM && slotsTable[VSIM_OP_SETAPN] == VSIM_OP_SETAPN) {
            return VSIM_MODEM_COUNT;
        }
        if (slotsTable[VSIM_MODEM_COUNT] == VSIM_OP_ENABLEVSIM && slotsTable[VSIM_OP_ENABLEVSIM] == 0 && slotsTable[VSIM_OP_SETAPN] == VSIM_OP_SETAPN) {
            return VSIM_OP_ENABLEVSIM;
        }
        if (slotsTable[VSIM_MODEM_COUNT] == VSIM_OP_SETAPN && slotsTable[VSIM_OP_ENABLEVSIM] == VSIM_OP_ENABLEVSIM && slotsTable[VSIM_OP_SETAPN] == 0) {
            return VSIM_OP_SETAPN;
        }
        if (slotsTable[VSIM_MODEM_COUNT] == VSIM_OP_SETAPN && slotsTable[VSIM_OP_ENABLEVSIM] == 0 && slotsTable[VSIM_OP_SETAPN] == VSIM_OP_ENABLEVSIM) {
            return VSIM_OP_ENABLEVSIM_FORHASH;
        }
        return GETMODE_RESULT_ERROR;
    }

    private int getMainSlot(int[] slotsTable) {
        if (slotsTable == null) {
            return GETMODE_RESULT_ERROR;
        }
        int mainSlot;
        if (slotsTable[VSIM_MODEM_COUNT] == 0 && slotsTable[VSIM_OP_ENABLEVSIM] == VSIM_OP_ENABLEVSIM && slotsTable[VSIM_OP_SETAPN] == VSIM_OP_SETAPN) {
            mainSlot = VSIM_MODEM_COUNT;
        } else if (slotsTable[VSIM_MODEM_COUNT] == VSIM_OP_ENABLEVSIM && slotsTable[VSIM_OP_ENABLEVSIM] == 0 && slotsTable[VSIM_OP_SETAPN] == VSIM_OP_SETAPN) {
            mainSlot = VSIM_OP_ENABLEVSIM;
        } else if (slotsTable[VSIM_MODEM_COUNT] == VSIM_OP_SETAPN && slotsTable[VSIM_OP_ENABLEVSIM] == VSIM_OP_ENABLEVSIM && slotsTable[VSIM_OP_SETAPN] == 0) {
            mainSlot = VSIM_MODEM_COUNT;
        } else if (slotsTable[VSIM_MODEM_COUNT] == VSIM_OP_SETAPN && slotsTable[VSIM_OP_ENABLEVSIM] == 0 && slotsTable[VSIM_OP_SETAPN] == VSIM_OP_ENABLEVSIM) {
            mainSlot = VSIM_OP_ENABLEVSIM;
        } else {
            mainSlot = GETMODE_RESULT_ERROR;
        }
        return mainSlot;
    }

    private int getWorkMode() {
        boolean isULOnly = getVSimULOnlyMode();
        int reservedSubId = getUserReservedSubId();
        if (isULOnly) {
            return VSIM_OP_SETAPN;
        }
        if (reservedSubId == 0) {
            return VSIM_MODEM_COUNT;
        }
        if (reservedSubId == VSIM_OP_ENABLEVSIM) {
            return VSIM_OP_ENABLEVSIM;
        }
        return VSIM_OP_SETAPN;
    }

    private HwVSimModemAdapter createModemAdapter(Context context, CommandsInterface vsimCi, CommandsInterface[] cis) {
        if (isPlatformTwoModems()) {
            return HwVSimDualModem.create(this, context, vsimCi, cis);
        }
        return HwVSimTriModem.create(this, context, vsimCi, cis);
    }

    private String getCallingAppName() {
        int callingPid = Binder.getCallingPid();
        ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
        if (am == null) {
            return null;
        }
        List<RunningAppProcessInfo> appProcessList = am.getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == callingPid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private boolean cmdSem_acquire() {
        if (this.mCmdSem == null) {
            return false;
        }
        boolean acquired;
        try {
            logd("cmd sem try acquire");
            acquired = this.mCmdSem.tryAcquire(60000, TimeUnit.MILLISECONDS);
            logd("cmd sem acquired");
            synchronized (this) {
                if (acquired) {
                    if (this.mCmdSemAcquired != null) {
                        logd("cmd sem mark acquired");
                        this.mCmdSemAcquired.set(true);
                    }
                }
            }
        } catch (InterruptedException e) {
            acquired = false;
            logd("cmd sem not acquired");
        }
        return acquired;
    }

    public void cmdSem_release() {
        if (this.mCmdSem != null && this.mCmdSemAcquired != null) {
            synchronized (this) {
                if (this.mCmdSemAcquired.get()) {
                    logd("cmd sem release");
                    this.mCmdSem.release();
                    this.mCmdSemAcquired.set(false);
                } else {
                    logd("cmd sem already released");
                }
            }
        }
    }

    private void waitVSimIdle(int timeout) {
        Thread t = new AnonymousClass3(SystemClock.elapsedRealtime() + ((long) timeout));
        t.start();
        try {
            t.join((long) timeout);
        } catch (InterruptedException e) {
            logd("Interrupted");
        }
    }

    private void broadcastVSimCardType() {
        logd("broadcastVSimCardType, card type is " + this.mCurCardType + ", old card type is " + this.mOldCardType);
        String SUB_ID = HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID;
        String VSIM_CARDTYPE = "vsim_cardtype";
        String VSIM_OLD_CARDTYPE = "vsim_old_cardtype";
        Intent intent = new Intent("com.huawei.vsim.action.VSIM_CARD_RELOAD");
        intent.putExtra("subscription", VSIM_OP_SETAPN);
        intent.putExtra("phone", VSIM_OP_SETAPN);
        intent.putExtra("slot", VSIM_OP_SETAPN);
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, VSIM_OP_SETAPN);
        intent.putExtra("vsim_cardtype", this.mCurCardType);
        intent.putExtra("vsim_old_cardtype", this.mOldCardType);
        this.mContext.sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    private int setApn(String imsi, int cardType, int apnType, String tapath, String challenge, boolean isForHash) {
        logd("setApn cardType: " + cardType + " apnType: " + apnType + " isForHash: " + isForHash);
        int subId = HwVSimPhoneFactory.getVSimEnabledSubId();
        logi("subId = " + subId);
        if (subId == GETMODE_RESULT_ERROR) {
            return VSIM_OP_ENABLEVSIM_FORHASH;
        }
        this.mVSimPhone.setDataEnabled(false);
        int result = writeApnToTA(imsi, cardType, apnType, tapath, challenge, isForHash);
        if (result == 0) {
            result = ((Integer) sendRequest(20, null, subId)).intValue();
        }
        this.mVSimPhone.setDataEnabled(true);
        logi("setApn result = " + result);
        setApnReport(result);
        return result;
    }

    private int writeApnToTA(String imsi, int cardType, int apnType, String tapath, String challenge, boolean isForHash) {
        logi("writeApnToTA");
        if (challenge != null && challenge.length() != 0) {
            return new HwVSimOperateTA().operateTA(VSIM_OP_SETAPN, imsi, cardType, apnType, challenge, isForHash, tapath, VSIM_MODEM_COUNT, VSIM_MODEM_COUNT);
        }
        logi("invalid param challenge");
        return VSIM_OP_ENABLEVSIM;
    }

    private boolean hasIccCardOnM2(int[] cardTypes) {
        if (cardTypes == null) {
            return false;
        }
        int cardCount = cardTypes.length;
        if (cardCount == 0) {
            return false;
        }
        int i;
        boolean[] isCardPresent = new boolean[cardCount];
        for (i = VSIM_MODEM_COUNT; i < cardCount; i += VSIM_OP_ENABLEVSIM) {
            if (cardTypes[i] == 0) {
                isCardPresent[i] = false;
            } else {
                isCardPresent[i] = true;
            }
        }
        i = VSIM_MODEM_COUNT;
        while (i < cardCount) {
            if (isSubOnM2(i) && isCardPresent[i]) {
                return true;
            }
            i += VSIM_OP_ENABLEVSIM;
        }
        return false;
    }

    private boolean needSwitchModeHotplug(int[] cardTypes) {
        if (HwVSimUtilsInner.isChinaTelecom()) {
            return needSwitchModeHotplugForTelecom(cardTypes);
        }
        int mainSlot = getMainSlot(this.mSimSlotsTable);
        if (hasIccCardOnM2(cardTypes) && !getULOnlyProp() && getVSimULOnlyMode()) {
            if (HwVSimUtilsInner.isFullNetworkSupported() || !(cardTypes[mainSlot] == VSIM_OP_SETAPN || cardTypes[mainSlot] == VSIM_OP_ENABLEVSIM_FORHASH)) {
                logd("needSwitchModeHotplug:icc card on m2");
                return true;
            }
            logd("needSwitchModeHotplug false CSIM on m2");
            return false;
        } else if (HwVSimUtilsInner.isFullNetworkSupported()) {
            int oldInsertedCardCount = getInsertedCardCount();
            int newInsertedCardCount = getInsertedCardCount(cardTypes);
            int slaveSlot = mainSlot == 0 ? VSIM_OP_ENABLEVSIM : VSIM_MODEM_COUNT;
            if (!getVSimULOnlyMode()) {
                int reservedSub = getUserReservedSubId();
                if (oldInsertedCardCount == VSIM_OP_SETAPN && newInsertedCardCount == VSIM_OP_SETAPN && reservedSub != GETMODE_RESULT_ERROR) {
                    int noReservedSub = reservedSub == 0 ? VSIM_OP_ENABLEVSIM : VSIM_MODEM_COUNT;
                    logd("needSwitchModeHotplug: noReservedSub = " + noReservedSub + " set to no sim");
                    cardTypes[noReservedSub] = VSIM_MODEM_COUNT;
                }
            }
            CommrilMode expect = getExpectCommrilMode(mainSlot, cardTypes);
            if (expect == CommrilMode.NON_MODE) {
                return false;
            }
            CommrilMode current = getCommrilMode();
            logd("needSwitchModeHotplug: oldInsertedCardCount = " + oldInsertedCardCount + ", newInsertedCardCount = " + newInsertedCardCount);
            logd("needSwitchModeHotplug: expect = " + expect + ", current = " + current);
            logd("needSwitchModeHotplug: mainSlot = " + mainSlot + ", slaveSlot = " + slaveSlot);
            logd("needSwitchModeHotplug: cardTypes[0] = " + cardTypes[VSIM_MODEM_COUNT] + "cardTypes[1] = " + cardTypes[VSIM_OP_ENABLEVSIM]);
            if (CommrilMode.isCLGMode(expect, cardTypes, mainSlot) || CommrilMode.isCGMode(expect, cardTypes, mainSlot)) {
                if (current == CommrilMode.getULGMode()) {
                    logd("needSwitchModeHotplug: ULG to CG");
                    return true;
                } else if (!HwVSimSlotSwitchController.isCDMACard(cardTypes[slaveSlot]) && HwVSimSlotSwitchController.isCDMACard(cardTypes[mainSlot])) {
                    logd("needSwitchModeHotplug: cdma card match to c-modem");
                    return true;
                }
            } else if (expect != current) {
                return true;
            }
            return false;
        } else {
            logd("needSwitchModeHotplug false CSIM on m1");
            return false;
        }
    }

    private boolean needSwitchModeHotplugForTelecom(int[] cardTypes) {
        if (hasIccCardOnM2(cardTypes) && !getULOnlyProp() && getVSimULOnlyMode()) {
            logd("needSwitchModeHotplugForTelecom: icc card on m2");
            return true;
        }
        int mainSlot = getMainSlot(this.mSimSlotsTable);
        int slaveSlot = mainSlot == 0 ? VSIM_OP_ENABLEVSIM : VSIM_MODEM_COUNT;
        logd("needSwitchModeHotplugForTelecom:slaveSlot=" + slaveSlot + " mainSlot =" + mainSlot + " cardTypes[0] = " + cardTypes[VSIM_MODEM_COUNT] + " cardTypes[1] = " + cardTypes[VSIM_OP_ENABLEVSIM]);
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            return (cardTypes[slaveSlot] == VSIM_OP_ENABLEVSIM && cardTypes[mainSlot] != VSIM_OP_ENABLEVSIM) || !(cardTypes[slaveSlot] == VSIM_OP_SETAPN || cardTypes[slaveSlot] == VSIM_OP_ENABLEVSIM_FORHASH || (cardTypes[mainSlot] != VSIM_OP_SETAPN && cardTypes[mainSlot] != VSIM_OP_ENABLEVSIM_FORHASH));
        } else {
            if (!getVSimULOnlyMode()) {
                int reservedSub = getUserReservedSubId();
                if (getInsertedCardCount(cardTypes) == VSIM_OP_SETAPN && reservedSub != GETMODE_RESULT_ERROR) {
                    int noReservedSub = reservedSub == 0 ? VSIM_OP_ENABLEVSIM : VSIM_MODEM_COUNT;
                    logd("needSwitchModeHotplugForTelecom: noReservedSub = " + noReservedSub + " set to no sim");
                    cardTypes[noReservedSub] = VSIM_MODEM_COUNT;
                }
            }
            CommrilMode expect = getExpectCommrilMode(mainSlot, cardTypes);
            if (expect == CommrilMode.NON_MODE) {
                return false;
            }
            CommrilMode current = getCommrilMode();
            logd("needSwitchModeHotplugForTelecom: expect = " + expect + ", current = " + current);
            if (CommrilMode.isCLGMode(expect, cardTypes, mainSlot) || CommrilMode.isCGMode(expect, cardTypes, mainSlot)) {
                if (current == CommrilMode.getULGMode()) {
                    logd("needSwitchModeHotplug: ULG to CG");
                    return true;
                } else if ((cardTypes[slaveSlot] == 0 || cardTypes[slaveSlot] == VSIM_OP_ENABLEVSIM) && (cardTypes[mainSlot] == VSIM_OP_SETAPN || cardTypes[mainSlot] == VSIM_OP_ENABLEVSIM_FORHASH)) {
                    logd("needSwitchModeHotplug: cdma card match to c-modem");
                    return true;
                }
            } else if (expect != current) {
                return true;
            }
        }
    }

    public void setBlockPinFlag(boolean value) {
        this.mBlockPinFlag = value;
        logd("mBlockPinFlag = " + this.mBlockPinFlag);
    }

    public void setBlockPinTable(int subId, boolean value) {
        if (this.mBlockPinTable != null && this.mPinBlockedTable != null) {
            for (int i = VSIM_MODEM_COUNT; i < HwVSimModemAdapter.PHONE_COUNT; i += VSIM_OP_ENABLEVSIM) {
                this.mBlockPinTable[i] = false;
                this.mPinBlockedTable[i] = false;
            }
            if (subId >= 0 && subId < HwVSimModemAdapter.PHONE_COUNT) {
                this.mBlockPinTable[subId] = value;
                logi("mBlockPinTable = " + Arrays.toString(this.mBlockPinTable));
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean needBlockPin(int subId) {
        logd("needBlockPin check for subId " + subId);
        logd("mBlockPinFlag = " + this.mBlockPinFlag + ", mBlockPinTable = " + Arrays.toString(this.mBlockPinTable) + ", mPinBlockedTable = " + Arrays.toString(this.mPinBlockedTable));
        if (subId < 0 || subId >= HwVSimModemAdapter.PHONE_COUNT || !this.mBlockPinFlag || !this.mBlockPinTable[subId]) {
            return false;
        }
        this.mPinBlockedTable[subId] = true;
        logd("mPinBlockedTable = " + Arrays.toString(this.mPinBlockedTable));
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isPinBlocked(int subId) {
        logd("isPinBlocked, subId = " + subId + ", mBlockPinFlag = " + this.mBlockPinFlag + ", mBlockPinTable = " + Arrays.toString(this.mBlockPinTable) + ", mPinBlockedTable = " + Arrays.toString(this.mPinBlockedTable));
        if (subId < 0 || subId >= HwVSimModemAdapter.PHONE_COUNT || !this.mBlockPinFlag || !this.mBlockPinTable[subId]) {
            return false;
        }
        return this.mPinBlockedTable[subId];
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isPinNeedBlock(int subId) {
        logd("isPinNeedBlock, subId = " + subId + ", mBlockPinFlag = " + this.mBlockPinFlag + ", mBlockPinTable = " + Arrays.toString(this.mBlockPinTable));
        if (subId < 0 || subId >= HwVSimModemAdapter.PHONE_COUNT || !this.mBlockPinFlag || !this.mBlockPinTable[subId]) {
            return false;
        }
        return true;
    }

    public void disposeCardForPinBlock(int subId) {
        if (subId >= 0 && subId < HwVSimModemAdapter.PHONE_COUNT) {
            boolean isCardPresent = false;
            UiccCard card = this.mUiccController.getUiccCard(subId);
            if (card != null) {
                isCardPresent = card.getCardState() == CardState.CARDSTATE_PRESENT;
            }
            if (isCardPresent && isPinNeedBlock(subId)) {
                logd("disposeCardForPinBlock: card present and pin blocked");
                this.mUiccController.disposeCard(subId);
            }
        }
    }

    private void setupDataOnVSimEnded() {
        boolean isDataEnabled = false;
        int dataSubId = GETMODE_RESULT_ERROR;
        SubscriptionController subController = SubscriptionController.getInstance();
        if (subController != null) {
            dataSubId = subController.getDefaultDataSubId();
        }
        logd("setupDataOnVSimEnded data sub: " + dataSubId);
        if (dataSubId != GETMODE_RESULT_ERROR && dataSubId < this.mPhones.length) {
            isDataEnabled = this.mPhones[dataSubId].getDataEnabled();
            allowData(dataSubId);
        }
        if (isDataEnabled) {
            Message msg = this.mMainHandler.obtainMessage();
            msg.what = VSIM_OP_SETAPN;
            msg.arg1 = dataSubId;
            this.mMainHandler.sendMessage(msg);
        }
    }

    private void onSetupDataOnVSimEnded(int phoneId) {
        logd("onSetupDataOnVSimEnded phoneId=" + phoneId);
        Phone phone = getPhoneBySub(phoneId);
        if (phone != null) {
            DcTracker dcTracker = phone.mDcTracker;
            if (dcTracker != null) {
                dcTracker.setupDataOnConnectableApns(HwVSimConstants.PHONE_REASON_VSIM_ENDED, null);
            }
        }
    }

    public void updateUserPreferences() {
        this.mMainHandler.obtainMessage(VSIM_OP_ENABLEVSIM).sendToTarget();
    }

    private void onUpdateUserPreferences() {
        logd("onUpdateUserPreferences");
        HwTelephonyFactory.getHwUiccManager().updateUserPreferences(false);
    }

    public boolean isEnableProhibitByDisableRetry() {
        if (HwVSimPhoneFactory.getVSimUserEnabled() == VSIM_OP_ENABLEVSIM) {
            return false;
        }
        return this.mDisableFailMark;
    }

    public void updateSubActivation() {
        logd("updateSubActivation");
        if (isVSimCauseCardReload()) {
            logd("need wait card reload");
            return;
        }
        this.mMainHandler.sendMessage(this.mMainHandler.obtainMessage(VSIM_OP_ENABLEVSIM_FORHASH));
        if (isVSimEnabled()) {
            logd("skip auto switch slot when vsim enabled");
        } else {
            this.mMainHandler.sendMessage(this.mMainHandler.obtainMessage(VSIM_OP_ENABLEVSIM_OFFLINE));
        }
    }

    private void onUpdateSubActivation() {
        logd("onUpdateSubActivation");
        if (HwVSimUtilsInner.isFullNetworkSupported()) {
            HwFullNetwork.getInstance().checkIfAllCardsReady();
        }
    }

    private void onAutoSwitchSimSlot() {
        logd("onAutoSwitchSimSlot");
        if (HwAllInOneController.IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT) {
            setSubActivationUpdate(false);
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            int activatedSlotId = getSwitchActivatedSubId(default4GSlotId);
            logd("default4GSlotId = " + default4GSlotId + ", activatedSlotId = " + activatedSlotId);
            if (default4GSlotId != activatedSlotId) {
                logd("auto switch 4G slot after vsim disabled");
                HwAllInOneController.getInstance().setDefault4GSlot(activatedSlotId, null);
            }
        }
    }

    private void onAutoSetAllowData() {
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            int allowDataSlotId = getSwitchActivatedSubId(HwTelephonyManagerInner.getDefault().getDefault4GSlotId());
            if (this.mPhones != null) {
                int len = this.mPhones.length;
                logd("onAutoSetAllowData, mPhones.length=" + len);
                for (int phoneId = VSIM_MODEM_COUNT; phoneId < len; phoneId += VSIM_OP_ENABLEVSIM) {
                    Phone phone = this.mPhones[phoneId];
                    if (phone == null) {
                        logd("active phone not found");
                    } else {
                        DcTracker dcTracker = phone.mDcTracker;
                        if (dcTracker == null) {
                            logd("dcTracker not found");
                        } else if (phoneId == allowDataSlotId) {
                            logd("call set data allowed on sub: " + phoneId);
                            dcTracker.setDataAllowed(true, this.mMainHandler.obtainMessage(VSIM_OP_SETAPN_FORHASH));
                        } else {
                            logd("call set data not allowed on sub: " + phoneId);
                            dcTracker.setDataAllowed(false, null);
                        }
                    }
                }
                return;
            }
            logd("onAutoSetAllowData, mPhones null");
            return;
        }
        setSubActivationUpdate(false);
    }

    private int getSwitchActivatedSubId(int oldActivatedSubId) {
        int activatedSubId = oldActivatedSubId;
        SubscriptionController subController = SubscriptionController.getInstance();
        if (subController == null) {
            logd("sub controller not found subId = " + oldActivatedSubId);
            return oldActivatedSubId;
        }
        if (HwAllInOneController.IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT) {
            int activatedSubCount = VSIM_MODEM_COUNT;
            boolean[] isSubActivated = new boolean[this.mCis.length];
            for (int i = VSIM_MODEM_COUNT; i < this.mCis.length; i += VSIM_OP_ENABLEVSIM) {
                if (subController.getSubState(subController.getSubId(i)[VSIM_MODEM_COUNT]) == VSIM_OP_ENABLEVSIM) {
                    isSubActivated[i] = true;
                    activatedSubCount += VSIM_OP_ENABLEVSIM;
                } else {
                    isSubActivated[i] = false;
                }
            }
            logd("isSubActivated = " + Arrays.toString(isSubActivated));
            if (activatedSubCount == VSIM_OP_ENABLEVSIM && !isSubActivated[oldActivatedSubId]) {
                activatedSubId = oldActivatedSubId == 0 ? VSIM_OP_ENABLEVSIM : VSIM_MODEM_COUNT;
            }
        }
        logd("oldActivatedSubId = " + oldActivatedSubId + ", activatedSubId = " + activatedSubId);
        return activatedSubId;
    }

    public void setSubActivationUpdate(boolean isUpate) {
        logd("setSubActivationUpdate : " + isUpate);
        this.mIsSubActivationUpdate = isUpate;
    }

    public void processAutoSetPowerupModemDone() {
        if (isProcessInit()) {
            Handler h = getHandler();
            if (h != null) {
                h.sendMessage(h.obtainMessage(74));
            }
        }
    }

    public boolean isSubActivationUpdate() {
        if (HwAllInOneController.IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT) {
            return false;
        }
        logd("isSubActivationUpdate : " + this.mIsSubActivationUpdate);
        return this.mIsSubActivationUpdate;
    }

    public int convertSavedNetworkMode(int networkMode) {
        int convertedNetworkMode = networkMode;
        switch (networkMode) {
            case VSIM_OP_SETAPN_FORHASH /*4*/:
            case VSIM_OP_ENABLEVSIM_OFFLINE /*5*/:
            case STATE_DISABLE_WORK /*6*/:
                convertedNetworkMode = VSIM_OP_ENABLEVSIM_FORHASH;
                break;
            case STATE_RECONNECT /*8*/:
                convertedNetworkMode = STATE_RECONNECT_WORK;
                break;
        }
        logd("networkMode : " + networkMode + ", convertedNetworkMode : " + convertedNetworkMode);
        return convertedNetworkMode;
    }

    public void updateSubState(int subId, int subState) {
        synchronized (sSubStateLock) {
            if (subId >= 0) {
                if (subId < this.mSubStates.length) {
                    this.mSubStates[subId] = subState;
                    logd("[SUB" + subId + "] updateSubState : " + subState);
                    if (isProcessInit()) {
                        Handler h = getHandler();
                        if (h != null) {
                            h.sendMessage(h.obtainMessage(76));
                        }
                    }
                }
            }
        }
    }

    public int getSubState(int subId) {
        int subState;
        synchronized (sSubStateLock) {
            subState = VSIM_MODEM_COUNT;
            if (subId >= 0) {
                if (subId < this.mSubStates.length) {
                    subState = this.mSubStates[subId];
                }
            }
            logd("[SUB" + subId + "] getSubState : " + subState);
        }
        return subState;
    }

    public void updateSimCardTypes(int[] cardTypes) {
        synchronized (sSimCardTypesLock) {
            if (cardTypes == null) {
                return;
            }
            this.mSimCardTypes = (int[]) cardTypes.clone();
            if (isProcessInit()) {
                Handler h = getHandler();
                if (h != null) {
                    h.sendMessage(h.obtainMessage(77));
                }
            }
        }
    }

    public int[] getSimCardTypes() {
        int[] iArr;
        synchronized (sSimCardTypesLock) {
            iArr = (int[]) this.mSimCardTypes.clone();
        }
        return iArr;
    }

    public void setSubActived(int subId) {
        synchronized (sCheckSubActivedLock) {
            if (subId >= 0) {
                if (subId < this.mCheckSubActivated.length) {
                    logd("[SUB" + subId + "] set sub actived");
                    this.mCheckSubActivated[subId] = false;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSubActivedDone() {
        synchronized (sCheckSubActivedLock) {
            int i = VSIM_MODEM_COUNT;
            while (true) {
                if (i >= this.mCheckSubActivated.length) {
                    break;
                } else if (this.mCheckSubActivated[i]) {
                    logd("[SUB" + i + "] is not active");
                    return false;
                } else {
                    i += VSIM_OP_ENABLEVSIM;
                }
            }
            i = VSIM_MODEM_COUNT;
            while (true) {
                if (i < this.mCheckSubActivated.length) {
                    this.mCheckSubActivated[i] = false;
                    i += VSIM_OP_ENABLEVSIM;
                } else {
                    return true;
                }
            }
        }
    }

    private void initCheckSubActived(int[] cardTypes) {
        if (cardTypes != null) {
            for (int i = VSIM_MODEM_COUNT; i < cardTypes.length; i += VSIM_OP_ENABLEVSIM) {
                synchronized (sCheckSubActivedLock) {
                    if (i >= 0) {
                        if (i < this.mCheckSubActivated.length) {
                            this.mCheckSubActivated[i] = false;
                        }
                    }
                }
                if (cardTypes[i] != 0) {
                    SubscriptionController subController = SubscriptionController.getInstance();
                    if (subController == null) {
                        logd("sub controller not found");
                        return;
                    } else if (subController.getSubState(i) == 0) {
                        synchronized (sCheckSubActivedLock) {
                            if (i >= 0) {
                                if (i < this.mCheckSubActivated.length) {
                                    logd("[SUB" + i + "] sub state is inactive.");
                                    this.mCheckSubActivated[i] = true;
                                }
                            }
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
    }

    public boolean prohibitSubUpdateSimNoChange(int subId) {
        if (this.mProhibitSubUpdateSimNoChange != null && subId >= 0 && subId < HwVSimModemAdapter.PHONE_COUNT) {
            return this.mProhibitSubUpdateSimNoChange[subId];
        }
        return false;
    }

    public void setProhibitSubUpdateSimNoChange(int subId, boolean value) {
        if (this.mProhibitSubUpdateSimNoChange != null && subId >= 0 && subId < HwVSimModemAdapter.PHONE_COUNT) {
            this.mProhibitSubUpdateSimNoChange[subId] = value;
            logd("setProhibitSubUpdateSimNoChange, new values are " + Arrays.toString(this.mProhibitSubUpdateSimNoChange));
        }
    }

    public void clearProhibitSubUpdateSimNoChange() {
        logd("clearProhibitSubUpdateSimNoChange");
        if (this.mProhibitSubUpdateSimNoChange != null) {
            for (int i = VSIM_MODEM_COUNT; i < HwVSimModemAdapter.PHONE_COUNT; i += VSIM_OP_ENABLEVSIM) {
                this.mProhibitSubUpdateSimNoChange[i] = false;
            }
        }
    }

    public void setIsWaitingSwitchCdmaModeSide(boolean value) {
        this.isWaitingSwitchCdmaModeSide = value;
        logd("set isWaitingSwitchCdmaModeSide = " + this.isWaitingSwitchCdmaModeSide);
    }

    public boolean getIsWaitingSwitchCdmaModeSide() {
        return this.isWaitingSwitchCdmaModeSide;
    }

    public boolean needBlockPinInBoot() {
        return false;
    }

    public boolean mainSlotPinBusy() {
        return false;
    }

    public boolean isVSimReconnecting() {
        return false;
    }

    public boolean hasHardIccCardForVSim(int subId) {
        return false;
    }

    public String getPendingDeviceInfoFromSP(String prefKey) {
        return null;
    }

    public void recoverSimMode() {
    }

    public int getRadioOnSubId() {
        for (int subId = VSIM_MODEM_COUNT; subId < HwVSimModemAdapter.MAX_SUB_COUNT; subId += VSIM_OP_ENABLEVSIM) {
            CommandsInterface ci = getCiBySub(subId);
            if (ci != null && ci.getRadioState().isOn()) {
                return subId;
            }
        }
        return VSIM_MODEM_COUNT;
    }
}
