package com.android.internal.telephony.vsim;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.common.HwFrameworkFactory;
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
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
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
import com.android.internal.telephony.vsim.process.HwVSimRestartRildProcessor;
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
    private static final int EVENT_RECOVER_MARK_TO_FALSE = 6;
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
    private static final int MAX_VSIM_WAIT_TIME = 90000;
    private static final int RECOVER_MARK_DELAY_TIME = 5000;
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
    public static final int STATE_RESTART_RILD = 14;
    public static final int STATE_SWITCHMODE = 11;
    public static final int STATE_SWITCHMODE_READY = 13;
    public static final int STATE_SWITCHMODE_WORK = 12;
    private static final int SWITCH_DELAY_TIME = 2000;
    private static final int VSIM_MODEM_COUNT = SystemProperties.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static final int VSIM_OP_ENABLEVSIM = 1;
    private static final int VSIM_OP_ENABLEVSIM_FORHASH = 3;
    public static final int VSIM_OP_ENABLEVSIM_OFFLINE = 5;
    private static final int VSIM_OP_SETAPN = 2;
    private static final int VSIM_OP_SETAPN_FORHASH = 4;
    private static final String VSIM_PKG_NAME = "com.huawei.skytone";
    private static final int VSIM_STATE_POLL_SLEEP_MSEC = 500;
    private static final Object sCheckSubActivedLock = new Object();
    private static HwVSimController sInstance = null;
    private static final Object sLock = new Object();
    private static final Object sSimCardTypesLock = new Object();
    private static final Object sSubStateLock = new Object();
    private boolean isWaitingSwitchCdmaModeSide = false;
    private int mAlternativeUserReservedSubId = -1;
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
    private VSimDReadyState mDReadyState = new VSimDReadyState(this, null);
    private VSimDWorkState mDWorkState = new VSimDWorkState(this, null);
    private VSimDefaultState mDefaultState = new VSimDefaultState(this, null);
    private boolean mDisableFailMark;
    private HwVSimRequest mDisableRequest;
    private int mDisableRetryCount;
    private VSimDisableState mDisableState = new VSimDisableState(this, null);
    private VSimEReadyState mEReadyState = new VSimEReadyState(this, null);
    private VSimEWorkState mEWorkState = new VSimEWorkState(this, null);
    private HwVSimRequest mEnableRequest;
    private VSimEnableState mEnableState = new VSimEnableState(this, null);
    public VSimEventInfo mEventInfo;
    private InitialState mInitialState = new InitialState(this, null);
    private int mInsertedSimCount = 0;
    private boolean mIsRegNetworkReceiver;
    private boolean mIsSubActivationUpdate;
    private boolean mIsVSimCauseCardReload;
    private boolean mIsVSimOn;
    private long mLastReportTime;
    private int mLastRilFailCause;
    private Handler mMainHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwVSimController.this.logi("mMainHandler: EVENT_UPDATE_USER_PREFERENCES");
                    HwVSimController.this.onUpdateUserPreferences();
                    return;
                case 2:
                    HwVSimController.this.onSetupDataOnVSimEnded(msg.arg1);
                    return;
                case 3:
                    HwVSimController.this.logd("EVENT_UPDATE_SUB_ACTIVATION");
                    HwVSimController.this.onUpdateSubActivation();
                    return;
                case 4:
                    HwVSimController.this.logd("EVENT_AUTO_SWITCH_SLOT");
                    HwVSimController.this.onAutoSwitchSimSlot();
                    return;
                case 5:
                    HwVSimController.this.logd("EVENT_AUTO_SET_ALLOW_DATA");
                    HwVSimController.this.onAutoSetAllowData();
                    return;
                case 6:
                    HwVSimController.this.logd("EVENT_RECOVER_MARK_TO_FALSE");
                    HwVSimController.this.onRecoverMarkToFalse();
                    return;
                default:
                    return;
            }
        }
    };
    private HwVSimModemAdapter mModemAdapter;
    private boolean[] mNeedSimLoadedMark;
    private int mNetworkScanIsRunning = 0;
    private int mNetworkScanSubId = 0;
    private int mNetworkScanType = 0;
    private final BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (HwVSimController.this.isNetworkConnected()) {
                HwVSimController.this.getHandler().obtainMessage(50).sendToTarget();
            }
        }
    };
    private HwVSimNvMatchController mNvMatchController;
    private int mOldCardType;
    private Phone[] mPhones;
    private boolean[] mPinBlockedTable;
    private int mPreferredNetworkTypeDisableFlag = 0;
    private int mPreferredNetworkTypeEnableFlag = 0;
    protected ProcessAction mProcessAction;
    protected ProcessState mProcessState;
    protected ProcessType mProcessType;
    private boolean[] mProhibitSubUpdateSimNoChange;
    private VSimRcReadyState mRcReadyState = new VSimRcReadyState(this, null);
    private VSimRcWorkState mRcWorkState = new VSimRcWorkState(this, null);
    private VSimReconnectState mReconnectState = new VSimReconnectState(this, null);
    private RestartRildState mRestartRildState = new RestartRildState(this, null);
    private int mRetryCountForHotPlug = 0;
    private ServiceStateHandler mServiceStateHandlerForVsim;
    private ServiceStateHandler[] mServiceStateHandlers;
    private int[] mSimCardTypes;
    private int[] mSimSlotsTable;
    private VSimSwitchModeReadyState mSmReadyState = new VSimSwitchModeReadyState(this, null);
    private VSimSwitchModeWorkState mSmWorkState = new VSimSwitchModeWorkState(this, null);
    private int[] mSubStates;
    private HwVSimRequest mSwitchModeRequest;
    private VSimSwitchModeState mSwitchModeState = new VSimSwitchModeState(this, null);
    private UiccController mUiccController;
    private CommandsInterface mVSimCi;
    public long mVSimEnterTime;
    private HwVSimEventHandler mVSimEventHandler;
    private HwVSimEventReport mVSimEventReport;
    private HandlerThread mVSimEventThread;
    private int mVSimFrameworkSupportVer = 10000;
    private boolean[] mVSimLockPower;
    private int mVSimModemSupportVer = -2;
    private Phone mVSimPhone;
    private HwVSimSlotSwitchController mVSimSlotSwitchController;
    private HwVSimUiccController mVSimUiccController;
    private HashMap<Integer, String> mWhatToStringMap;

    public static class EnableParam {
        public String acqorder;
        public int apnType;
        public int cardType;
        public String challenge;
        public String imsi;
        public boolean isVSimOn = false;
        public int operation;
        public String taPath;
        public int vsimLoc;

        public EnableParam(String imsi, int cardType, int apnType, String acqorder, String challenge, int operation, String tapath, int vsimloc) {
            this.imsi = imsi;
            this.cardType = cardType;
            this.apnType = apnType;
            this.acqorder = acqorder;
            this.challenge = challenge;
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
                    if (HwVSimController.this.canProcessSwitchMode()) {
                        HwVSimController.this.switchVSimWorkMode(HwVSimController.this.getWorkMode(), true);
                        return;
                    }
                    HwVSimController.this.handleSwitchModeDelay();
                    return;
                case EVENT_VSIM_DISABLE_RETRY /*1001*/:
                    HwVSimController.this.logd("EVENT_VSIM_DISABLE_RETRY, count = " + HwVSimController.this.mDisableRetryCount);
                    if (HwVSimController.this.mDisableRetryCount >= 3) {
                        HwVSimController.this.logd("max count, abort retry");
                        return;
                    }
                    HwVSimController hwVSimController = HwVSimController.this;
                    hwVSimController.mDisableRetryCount = hwVSimController.mDisableRetryCount + 1;
                    if (HwVSimController.this.mDisableFailMark) {
                        HwVSimController.this.disableVSim();
                        return;
                    } else {
                        HwVSimController.this.logd("no fail mark, abort retry");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    private class InitialState extends State {
        HwVSimInitialProcessor mProcessor;

        /* synthetic */ InitialState(HwVSimController this$0, InitialState -this1) {
            this();
        }

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
        PROCESS_ACTION_NONE,
        PROCESS_ACTION_ENABLE,
        PROCESS_ACTION_DISABLE,
        PROCESS_ACTION_RECONNECT,
        PROCESS_ACTION_ENABLE_OFFLINE,
        PROCESS_ACTION_DISABLE_OFFLINE,
        PROCESS_ACTION_SWITCHWORKMODE,
        PROCESS_ACTION_MAX;

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
        PROCESS_STATE_NONE,
        PROCESS_STATE_WORK,
        PROCESS_STATE_READY,
        PROCESS_STATE_MAX;

        public boolean isWorkProcess() {
            return this == PROCESS_STATE_WORK;
        }

        public boolean isReadyProcess() {
            return this == PROCESS_STATE_READY;
        }
    }

    public enum ProcessType {
        PROCESS_TYPE_NONE,
        PROCESS_TYPE_SWAP,
        PROCESS_TYPE_CROSS,
        PROCESS_TYPE_DIRECT,
        PROCESS_TYPE_MAX;

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

    private class RestartRildState extends State {
        HwVSimRestartRildProcessor mProcessor;

        /* synthetic */ RestartRildState(HwVSimController this$0, RestartRildState -this1) {
            this();
        }

        private RestartRildState() {
        }

        public void enter() {
            HwVSimController.this.logd("RestartRildState: enter");
            this.mProcessor = new HwVSimRestartRildProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, null);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("RestartRildState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("RestartRildState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private final class ServiceStateHandler extends Handler {
        Phone mPhone = null;
        ServiceState mSS = new ServiceState();

        public ServiceStateHandler(Looper looper, Phone phone) {
            super(looper);
            this.mPhone = phone;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AsyncResult ar = msg.obj;
                    if (ar.exception == null) {
                        ServiceState newSS = ar.result;
                        notifyServiceStateChanged(newSS, this.mSS, this.mPhone);
                        this.mSS = new ServiceState(newSS);
                        break;
                    }
                    HwVSimController.this.logd("exception, return");
                    return;
            }
        }

        private void notifyServiceStateChanged(ServiceState newSS, ServiceState oldSS, Phone phone) {
            if (newSS == null || oldSS == null || phone == null) {
                HwVSimController.this.logd("ss or phone is null, return!");
                return;
            }
            HwVSimController.this.logd("newSS:" + newSS + " oldSS:" + oldSS);
            boolean registeredStatuChanged = newSS.getVoiceRegState() != oldSS.getVoiceRegState();
            boolean plmnStatuChanged = equalsHandlesNulls(newSS.getVoiceOperatorNumeric(), oldSS.getVoiceOperatorNumeric()) ^ 1;
            HwVSimController.this.logd("notifyServiceStateChanged registeredStatuChanged:" + registeredStatuChanged + " plmnStatuChanged:" + plmnStatuChanged + " phoneId:" + phone.getPhoneId());
            if (registeredStatuChanged || plmnStatuChanged) {
                HwVSimController.this.logd("sendBroadcastAsUser HW_VSIM_SERVICE_STATE_CHANGED");
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

        /* synthetic */ VSimDReadyState(HwVSimController this$0, VSimDReadyState -this1) {
            this();
        }

        private VSimDReadyState() {
        }

        public void enter() {
            HwVSimController.this.logd("DReadyState: enter");
            this.mProcessor = new HwVSimDReadyProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, HwVSimController.this.mDisableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("DReadyState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("DReadyState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimDWorkState extends State {
        HwVSimDWorkProcessor mProcessor;

        /* synthetic */ VSimDWorkState(HwVSimController this$0, VSimDWorkState -this1) {
            this();
        }

        private VSimDWorkState() {
        }

        public void enter() {
            HwVSimController.this.logd("DWorkState: enter");
            this.mProcessor = new HwVSimDWorkProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, HwVSimController.this.mDisableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("DWorkState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("DWorkState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimDefaultState extends State {
        HwVSimDefaultProcessor mProcessor;

        /* synthetic */ VSimDefaultState(HwVSimController this$0, VSimDefaultState -this1) {
            this();
        }

        private VSimDefaultState() {
        }

        public void enter() {
            HwVSimController.this.logd("DefaultState: enter");
            this.mProcessor = new HwVSimDefaultProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, null);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("DefaultState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("DefaultState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }

        public int networksScan(int subId, int type) {
            if (this.mProcessor != null) {
                return this.mProcessor.networksScan(subId, type);
            }
            HwVSimController.this.mNetworkScanIsRunning = 0;
            return 1;
        }
    }

    private class VSimDisableState extends State {
        HwVSimDisableProcessor mProcessor;

        /* synthetic */ VSimDisableState(HwVSimController this$0, VSimDisableState -this1) {
            this();
        }

        private VSimDisableState() {
        }

        public void enter() {
            HwVSimController.this.logd("DisableState: enter");
            HwVSimController.this.disableEnterReport();
            this.mProcessor = new HwVSimDisableProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, HwVSimController.this.mDisableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("DisableState: exit");
            HwVSimController.this.disableExitReport();
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("DisableState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimEReadyState extends State {
        HwVSimEReadyProcessor mProcessor;

        /* synthetic */ VSimEReadyState(HwVSimController this$0, VSimEReadyState -this1) {
            this();
        }

        private VSimEReadyState() {
        }

        public void enter() {
            HwVSimController.this.logd("EReadyState: enter");
            this.mProcessor = HwVSimEReadyProcessor.create(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, HwVSimController.this.mEnableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("EReadyState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("EReadyState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimEWorkState extends State {
        HwVSimEWorkProcessor mProcessor;

        /* synthetic */ VSimEWorkState(HwVSimController this$0, VSimEWorkState -this1) {
            this();
        }

        private VSimEWorkState() {
        }

        public void enter() {
            HwVSimController.this.logd("VSimEWorkState: enter");
            this.mProcessor = HwVSimEWorkProcessor.create(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, HwVSimController.this.mEnableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("VSimEWorkState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("EWorkState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimEnableState extends State {
        HwVSimEnableProcessor mProcessor;

        /* synthetic */ VSimEnableState(HwVSimController this$0, VSimEnableState -this1) {
            this();
        }

        private VSimEnableState() {
        }

        public void enter() {
            HwVSimController.this.logd("EnableState: enter");
            HwVSimController.this.enableEnterReport();
            this.mProcessor = new HwVSimEnableProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, HwVSimController.this.mEnableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("EnableState: exit");
            HwVSimController.this.enableExitReport();
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("EnableState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimRcReadyState extends State {
        HwVSimRcReadyProcessor mProcessor;

        /* synthetic */ VSimRcReadyState(HwVSimController this$0, VSimRcReadyState -this1) {
            this();
        }

        private VSimRcReadyState() {
        }

        public void enter() {
            HwVSimController.this.logd("VSimRcReadyState: enter");
            this.mProcessor = new HwVSimRcReadyProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, HwVSimController.this.mDisableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("VSimRcReadyState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("VSimRcReadyState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimRcWorkState extends State {
        HwVSimRcWorkProcessor mProcessor;

        /* synthetic */ VSimRcWorkState(HwVSimController this$0, VSimRcWorkState -this1) {
            this();
        }

        private VSimRcWorkState() {
        }

        public void enter() {
            HwVSimController.this.logd("VSimRcWorkState: enter");
            this.mProcessor = new HwVSimRcWorkProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, HwVSimController.this.mDisableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("VSimRcWorkState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("VSimRcWorkState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimReconnectState extends State {
        HwVSimReconnectProcessor mProcessor;

        /* synthetic */ VSimReconnectState(HwVSimController this$0, VSimReconnectState -this1) {
            this();
        }

        private VSimReconnectState() {
        }

        public void enter() {
            HwVSimController.this.logd("VSimReconnectState: enter");
            HwVSimController.this.reconnectEnterReport();
            this.mProcessor = new HwVSimReconnectProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, null);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("VSimReconnectState: exit");
            HwVSimController.this.reconnectExitReport();
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("VSimReconnectState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeReadyState extends State {
        HwVSimSReadyProcessor mProcessor;

        /* synthetic */ VSimSwitchModeReadyState(HwVSimController this$0, VSimSwitchModeReadyState -this1) {
            this();
        }

        private VSimSwitchModeReadyState() {
        }

        public void enter() {
            HwVSimController.this.logd("VSimSReadyState: enter");
            this.mProcessor = new HwVSimSReadyProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, HwVSimController.this.mSwitchModeRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("VSimSReadyState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("VSimSReadyState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeState extends State {
        HwVSimSwitchModeProcessor mProcessor;

        /* synthetic */ VSimSwitchModeState(HwVSimController this$0, VSimSwitchModeState -this1) {
            this();
        }

        private VSimSwitchModeState() {
        }

        public void enter() {
            HwVSimController.this.logd("VSimSwitchModeState: enter");
            HwVSimController.this.switchEnterReport();
            this.mProcessor = new HwVSimSwitchModeProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, HwVSimController.this.mSwitchModeRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("VSimSwitchModeState: exit");
            HwVSimController.this.switchExitReport();
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("VSimSwitchModeState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeWorkState extends State {
        HwVSimSWorkProcessor mProcessor;

        /* synthetic */ VSimSwitchModeWorkState(HwVSimController this$0, VSimSwitchModeWorkState -this1) {
            this();
        }

        private VSimSwitchModeWorkState() {
        }

        public void enter() {
            HwVSimController.this.logd("VSimSWorkState: enter");
            this.mProcessor = new HwVSimSWorkProcessor(HwVSimController.sInstance, HwVSimController.this.mModemAdapter, HwVSimController.this.mSwitchModeRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimController.this.logd("VSimSWorkState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimController.this.logd("VSimSWorkState: what = " + HwVSimController.this.getWhatToString(msg.what));
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
        logd("HwVSimController");
        initWhatToStringMap();
        this.mCmdSem = new Semaphore(1);
        this.mCmdSemAcquired = new AtomicBoolean(false);
        this.mContext = context;
        this.mVSimPhone = vsimPhone;
        this.mPhones = phones;
        this.mVSimCi = vsimCi;
        this.mCis = cis;
        this.mModemAdapter = createModemAdapter(context, vsimCi, cis);
        this.mNvMatchController = HwVSimNvMatchController.create(this);
        this.mVSimSlotSwitchController = HwVSimSlotSwitchController.getInstance();
        this.mVSimUiccController = HwVSimUiccController.getInstance();
        this.mUiccController = UiccController.getInstance();
        this.mCurCardType = -1;
        this.mOldCardType = -1;
        this.mIsRegNetworkReceiver = false;
        this.mVSimEventReport = new HwVSimEventReport(context);
        this.mEventInfo = new VSimEventInfo();
        VSimEventInfoUtils.setCauseType(this.mEventInfo, -1);
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
        for (i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
            this.mVSimLockPower[i] = false;
        }
        this.mIsVSimCauseCardReload = false;
        this.mNeedSimLoadedMark = new boolean[HwVSimModemAdapter.MAX_SUB_COUNT];
        for (i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
            this.mNeedSimLoadedMark[i] = false;
        }
        this.mIsSubActivationUpdate = false;
        this.mSubStates = new int[HwVSimModemAdapter.PHONE_COUNT];
        this.mSimCardTypes = new int[HwVSimModemAdapter.PHONE_COUNT];
        this.mCheckSubActivated = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        this.mProhibitSubUpdateSimNoChange = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        for (i = 0; i < this.mSubStates.length; i++) {
            this.mSimCardTypes[i] = -1;
            this.mSubStates[i] = -1;
            this.mCheckSubActivated[i] = false;
            this.mProhibitSubUpdateSimNoChange[i] = false;
        }
        this.mBlockPinFlag = false;
        this.mBlockPinTable = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        this.mPinBlockedTable = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        this.mDisableRetryCount = 3;
        this.mDisableFailMark = false;
        logi("VSIM_MODEM_COUNT: " + VSIM_MODEM_COUNT);
        addServiceStateChangeListener();
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mEnableState, this.mDefaultState);
        addState(this.mDisableState, this.mDefaultState);
        addState(this.mReconnectState, this.mDefaultState);
        addState(this.mSwitchModeState, this.mDefaultState);
        addState(this.mRestartRildState, this.mDefaultState);
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
        for (int i = 0; i < this.mPhones.length; i++) {
            this.mServiceStateHandlers[i] = new ServiceStateHandler(Looper.myLooper(), this.mPhones[i]);
            this.mPhones[i].registerForServiceStateChanged(this.mServiceStateHandlers[i], 1, null);
        }
        this.mServiceStateHandlerForVsim = new ServiceStateHandler(Looper.myLooper(), this.mVSimPhone);
        this.mVSimPhone.registerForServiceStateChanged(this.mServiceStateHandlerForVsim, 1, null);
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
            case 1:
                return enableVSim(imsi, cardType, apnType, acqorder, tapath, vsimloc, challenge, operation);
            case 2:
                return setApn(imsi, cardType, apnType, tapath, challenge, false);
            case 5:
                return 3;
            default:
                logd("enableVSim do nothing");
                return 3;
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
            logd("disableVSim subId = " + 2);
            Object oResult = sendRequest(52, null, 2);
            boolean iResult = false;
            if (oResult != null) {
                iResult = ((Boolean) oResult).booleanValue();
            }
            if (iResult) {
                this.mDisableFailMark = false;
                logd("remove EVENT_VSIM_DISABLE_RETRY");
                this.mVSimEventHandler.removeMessages(HwVSimEventHandler.EVENT_VSIM_DISABLE_RETRY);
                HwVSimPhoneFactory.setVSimEnabledSubId(-1);
                HwVSimPhoneFactory.setVSimUserEnabled(0);
                clearApDsFlowCfg();
                if (this.mApkObserver != null) {
                    this.mApkObserver.stopWatching();
                }
                if (!HwVSimUtilsInner.isPlatformRealTripple() || HwVSimUtilsInner.isVSimDsdsVersionOne()) {
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
        Object oResult = sendRequest(58, new WorkModeParam(workMode, oldMode, isHotplug), 2);
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
            case 0:
                if (this.mVSimModemSupportVer == -1) {
                    this.mVSimModemSupportVer = getModemSupportVSimVersionInner();
                }
                return this.mVSimModemSupportVer;
            case 1:
                return this.mVSimFrameworkSupportVer;
            default:
                return -1;
        }
    }

    private int getModemSupportVSimVersionInner() {
        if (Process.myPid() == Binder.getCallingPid()) {
            logd("getModemSupportVSimVersionInner same process obtain.");
            return -1;
        }
        int version = ((Integer) sendRequest(29, null, getRadioOnSubId())).intValue();
        logd("getModemSupportVSimVersionInner get modem support version = " + version);
        return version;
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
        if (subId == 0 || subId == 1 || subId == 2) {
            return (String) sendRequest(10, null, subId);
        }
        return null;
    }

    public String getTrafficData() {
        logd("getTrafficData");
        int subId = getVSimSubId();
        if (subId != 2) {
            logd("getTrafficData VSim not enabled");
            return null;
        }
        String callingAppName = getCallingAppName();
        if (VSIM_PKG_NAME.equals(callingAppName) || (HSF_PKG_NAME.equals(callingAppName) ^ 1) == 0) {
            return Arrays.toString((String[]) sendRequest(14, null, subId));
        }
        logd("getTrafficData not allowed, calling app is " + getCallingAppName());
        return null;
    }

    public boolean clearTrafficData() {
        logd("clearTrafficData");
        int subId = getVSimSubId();
        if (subId != 2) {
            logd("clearTrafficData VSim not enabled");
            return false;
        }
        String callingAppName = getCallingAppName();
        if (VSIM_PKG_NAME.equals(callingAppName) || (HSF_PKG_NAME.equals(callingAppName) ^ 1) == 0) {
            boolean result = ((Boolean) sendRequest(12, null, subId)).booleanValue();
            logd("clearTrafficData result = " + result);
            return result;
        }
        logd("clearTrafficData not allowed, calling app is " + getCallingAppName());
        return false;
    }

    public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) {
        int subId = getVSimSubId();
        logd("dsFlowCfg, subId = " + subId);
        if (subId != 2) {
            subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            logd("dsFlowCfg, VSim not enabled, to mainslot: " + subId);
        }
        String callingAppName = getCallingAppName();
        if (VSIM_PKG_NAME.equals(callingAppName) || (HSF_PKG_NAME.equals(callingAppName) ^ 1) == 0) {
            boolean result = setApDsFlowCfg(subId, repFlag, threshold, totalThreshold, oper);
            if (result && repFlag == 1) {
                setDsFlowNvCfg(subId, 1, 10);
            } else {
                setDsFlowNvCfg(subId, 0, 0);
            }
            return result;
        }
        logd("dsFlowCfg not allowed, calling app is " + getCallingAppName());
        return false;
    }

    public int getSimStateViaSysinfoEx(int subId) {
        logd("getSimStateViaSysinfoEx");
        if (subId == 2) {
            return ((Integer) sendRequest(22, null, subId)).intValue();
        }
        logd("getSimStateViaSysinfoEx VSim not enabled");
        return -1;
    }

    public String getDevSubMode(int subId) {
        logd("getDevSubMode");
        if (subId == 2) {
            return (String) sendRequest(25, null, subId);
        }
        logd("getDevSubMode VSim not enabled");
        return null;
    }

    public String getPreferredNetworkTypeForVSim(int subId) {
        logd("getPreferredNetworkTypeForVSim");
        if (subId == 2) {
            return (String) sendRequest(27, null, subId);
        }
        logd("getPreferredNetworkTypeForVSim VSim not enabled");
        return null;
    }

    public int getSimMode(int subId) {
        if (subId != 2) {
            return 0;
        }
        return 1;
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
        if (DcFailCause.fromInt(cause).isPermanentFailure(this.mContext, this.mVSimPhone.getSubId())) {
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
            return -1;
        }
        logd("dialupForVSim EVENT_TRY_SETUP_DATA");
        DcTracker dcTracker = this.mVSimPhone.mDcTracker;
        dcTracker.sendMessage(dcTracker.obtainMessage(270339, "userDataEnabled"));
        return 0;
    }

    public boolean isSubOnM2(int subId) {
        boolean z = false;
        if (this.mSimSlotsTable == null) {
            return false;
        }
        if (this.mSimSlotsTable[2] == subId) {
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
        this.mRetryCountForHotPlug = 0;
        if (this.mVSimEventHandler.hasMessages(HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE)) {
            this.mVSimEventHandler.removeMessages(HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
        }
        this.mNvMatchController.startNvMatchUnsolListener();
        if (needSwtich) {
            initCheckSubActived(cardTypes);
            Message msg = this.mVSimEventHandler.obtainMessage();
            msg.what = HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE;
            this.mVSimEventHandler.sendMessage(msg);
            return;
        }
        processNoNeedSwitchModeForCmcc(cardTypes);
    }

    private void handleSwitchModeDelay() {
        if (this.mVSimEventHandler.hasMessages(HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE)) {
            this.mVSimEventHandler.removeMessages(HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE);
        }
        this.mRetryCountForHotPlug++;
        if (this.mRetryCountForHotPlug > MAX_RETRY_COUNT) {
            logd("handleSwitchModeDelay has retry 150 times, no try again");
            this.mRetryCountForHotPlug = 0;
            return;
        }
        Message msg = this.mVSimEventHandler.obtainMessage();
        msg.what = HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE;
        this.mVSimEventHandler.sendMessageDelayed(msg, HwVSimConstants.GET_MODEM_SUPPORT_VERSION_INTERVAL);
    }

    public IccCardConstants.State modifySimStateForVsim(int phoneId, IccCardConstants.State s) {
        if (!HwVSimUtilsInner.isPlatformRealTripple() || HwVSimUtilsInner.isVSimDsdsVersionOne()) {
            boolean isSubOnM2 = isSubOnM2(phoneId);
            boolean isULGMode = getULOnlyProp() ^ 1;
            logd("modifySimStateForVsim : phoneid = " + phoneId + ", sub on M2 is " + isSubOnM2 + ", ULG Mode is " + isULGMode);
            if (isULGMode && isSubOnM2) {
                logd("modifySimStateForVsim  : State = " + s + " to ABSENT.");
                return IccCardConstants.State.ABSENT;
            }
        }
        return s;
    }

    public boolean needBlockUnReservedForVsim(int subId) {
        if (!(HwVSimUtilsInner.isPlatformRealTripple() || (isPlatformTwoModems() ^ 1) == 0) || HwVSimUtilsInner.isVSimDsdsVersionOne()) {
            boolean isSubOnM2 = isSubOnM2(subId);
            boolean isULGMode = getULOnlyProp() ^ 1;
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
        int i = 0;
        while (i < HwVSimModemAdapter.MAX_SUB_COUNT) {
            if (isPlatformTwoModems() && (getCiBySub(i).isRadioAvailable() ^ 1) != 0) {
                logi("setMarkForCardReload skip pending sub" + i);
            } else if (this.mNeedSimLoadedMark[i]) {
                oldAllFalse = false;
            }
            i++;
        }
        logd("oldAllFalse = " + oldAllFalse);
        this.mNeedSimLoadedMark[subId] = value;
        boolean newAllFalse = true;
        i = 0;
        while (i < HwVSimModemAdapter.MAX_SUB_COUNT) {
            if (isPlatformTwoModems() && (getCiBySub(i).isRadioAvailable() ^ 1) != 0) {
                logi("setMarkForCardReload skip pending sub" + i);
            } else if (this.mNeedSimLoadedMark[i]) {
                newAllFalse = false;
            }
            i++;
        }
        logd("newAllFalse = " + newAllFalse);
        if ((oldAllFalse && (newAllFalse ^ 1) != 0) || (!oldAllFalse && newAllFalse)) {
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
        for (int i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
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
        return ((Boolean) sendRequest(16, new int[]{config, threshold, total_threshold, oper}, subId)).booleanValue();
    }

    public boolean setDsFlowNvCfg(int subId, int enable, int interval) {
        logd("setDsFlowNvCfg, enable = " + enable);
        return ((Boolean) sendRequest(18, new int[]{enable, interval}, subId)).booleanValue();
    }

    public int networksScan(int subId, int type) {
        if (this.mNetworkScanIsRunning == 1) {
            logd("networksScan is running");
            return 1;
        } else if (subId != 2) {
            return 0;
        } else {
            this.mNetworkScanIsRunning = 1;
            this.mNetworkScanSubId = subId;
            this.mNetworkScanType = type;
            return this.mDefaultState.networksScan(subId, type);
        }
    }

    public boolean isVSimEnabled() {
        return HwVSimPhoneFactory.getVSimEnabledSubId() != -1;
    }

    public void saveNetworkMode(int modemId, int modemNetworkMode) {
        int savedNetworkMode = HwVSimPhoneFactory.getVSimSavedNetworkMode(modemId);
        logd("savedNetworkMode = " + savedNetworkMode);
        if (savedNetworkMode == -1) {
            savedNetworkMode = modemNetworkMode;
            HwVSimPhoneFactory.setVSimSavedNetworkMode(modemId, modemNetworkMode);
        }
    }

    public int writeVsimToTA(String imsi, int cardType, int apnType, String challenge, String taPath, int vsimLoc, int modemID) {
        logd("writeVsimToTA");
        if (challenge != null && challenge.length() != 0) {
            return HwVSimOperateTA.getDefault().operateTA(1, imsi, cardType, apnType, challenge, false, taPath, vsimLoc, modemID);
        }
        logd("invalid param challenge");
        return 6;
    }

    public void transitionToState(int state) {
        switch (state) {
            case 0:
                transitionTo(this.mDefaultState);
                return;
            case 1:
                transitionTo(this.mInitialState);
                return;
            case 2:
                transitionTo(this.mEnableState);
                return;
            case 3:
                transitionTo(this.mEWorkState);
                return;
            case 4:
                transitionTo(this.mEReadyState);
                return;
            case 5:
                transitionTo(this.mDisableState);
                return;
            case 6:
                transitionTo(this.mDWorkState);
                return;
            case 7:
                transitionTo(this.mDReadyState);
                return;
            case 8:
                transitionTo(this.mReconnectState);
                return;
            case 9:
                transitionTo(this.mRcWorkState);
                return;
            case 10:
                transitionTo(this.mRcReadyState);
                return;
            case 11:
                transitionTo(this.mSwitchModeState);
                return;
            case 12:
                transitionTo(this.mSmWorkState);
                return;
            case 13:
                transitionTo(this.mSmReadyState);
                return;
            case 14:
                transitionTo(this.mRestartRildState);
                return;
            default:
                return;
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
        return 0;
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
        return HwVSimOperateTA.getDefault().operateTA(3, null, 0, 0, null, false, null, 0, 0);
    }

    public void broadcastVSimServiceReady() {
        logd("broadcastVSimServiceReady");
        this.mContext.sendBroadcast(new Intent("com.huawei.vsim.action.VSIM_SERVICE_READY"), HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    public void setSimSlotTable(int[] slots) {
        if (slots != null) {
            this.mSimSlotsTable = (int[]) slots.clone();
        }
    }

    public int[] getSimSlotTable() {
        return this.mSimSlotsTable != null ? (int[]) this.mSimSlotsTable.clone() : new int[0];
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
        if (operation == 5 && isVSimEnabled()) {
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

    public boolean canStartNvMatchListener() {
        if (getCurrentState() == this.mDefaultState || getCurrentState() == this.mSmReadyState || getCurrentState() == this.mEReadyState) {
            return true;
        }
        return false;
    }

    public boolean canProcessRestartRild() {
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

    public void setOnRestartRildNvMatch(int subId, Handler h, int what, Object obj) {
        if (h != null) {
            CommandsInterface ci = getCiBySub(subId);
            logd("setOnRestartRildNvMatch, subId = " + subId);
            if (ci != null) {
                ci.setOnRestartRildNvMatch(h, what, obj);
            }
        }
    }

    public void unSetOnRestartRildNvMatch(int subId, Handler h) {
        if (h != null) {
            CommandsInterface ci = getCiBySub(subId);
            logd("unSetOnRestartRildNvMatch, subId = " + subId);
            if (ci != null) {
                ci.unSetOnRestartRildNvMatch(h);
            }
        }
    }

    public void setOnRadioAvaliable(Handler h, int what, Object obj) {
        for (int i = 0; i < this.mCis.length; i++) {
            this.mCis[i].registerForAvailable(h, what, Integer.valueOf(i));
        }
        this.mVSimCi.registerForAvailable(h, what, Integer.valueOf(2));
    }

    public void unSetOnRadioAvaliable(Handler h) {
        for (CommandsInterface unregisterForAvailable : this.mCis) {
            unregisterForAvailable.unregisterForAvailable(h);
        }
        this.mVSimCi.unregisterForAvailable(h);
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
        if (subId == 2) {
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
            allowData(0);
            allowData(1);
        } else if (this.mIsVSimOn) {
            logd("vsim is on, no need to restore default data");
        } else {
            setInteralDataForDSDS(0);
            setInteralDataForDSDS(1);
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
        if (dataSubId != 2 && (dataSubId < 0 || dataSubId >= this.mPhones.length)) {
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
            for (int phoneId = 0; phoneId < len; phoneId++) {
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
            return -1;
        }
        EnableParam param = (EnableParam) request.getArgument();
        if (param == null) {
            return -1;
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
        if (subId == -1) {
            return -1;
        }
        if (getVSimULOnlyMode()) {
            return HwVSimModemAdapter.PHONE_COUNT;
        }
        if (reservedSub != -1) {
            return reservedSub == 0 ? 1 : 0;
        } else {
            if (this.mAlternativeUserReservedSubId == 0) {
                return 1;
            }
            return this.mAlternativeUserReservedSubId == 1 ? 0 : -1;
        }
    }

    public boolean isDoingSlotSwitch() {
        return getCurrentState() == this.mDReadyState;
    }

    public int getCardPresentNumeric(boolean[] isCardPresent) {
        if (isCardPresent == null) {
            return -1;
        }
        if (!isCardPresent[0] && !isCardPresent[1]) {
            return 0;
        }
        if (isCardPresent[0] && !isCardPresent[1]) {
            return 1;
        }
        if (!isCardPresent[0] && isCardPresent[1]) {
            return 2;
        }
        if (isCardPresent[0] && isCardPresent[1]) {
            return 3;
        }
        return -1;
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
                for (int i = 0; i < info.length; i++) {
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
            return 0;
        }
        return getInsertedCardCount((int[]) this.mCardTypes.clone());
    }

    public int getInsertedCardCount(int[] cardTypes) {
        if (cardTypes == null) {
            return 0;
        }
        if (cardCount == 0) {
            return 0;
        }
        int insertedCardCount = 0;
        for (int i : cardTypes) {
            if (i != 0) {
                insertedCardCount++;
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
        return VSIM_MODEM_COUNT == 2;
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
        this.mInsertedSimCount = 0;
        for (int i = 0; i < HwVSimModemAdapter.PHONE_COUNT; i++) {
            UiccCard card = this.mUiccController.getUiccCard(i);
            if (card == null) {
                logd("uicc card " + i + " not ready");
                break;
            }
            logd("updateUiccCardCount cardstate = " + card.getCardState() + " i = " + i);
            if (card.getCardState() == CardState.CARDSTATE_PRESENT) {
                this.mInsertedSimCount++;
            }
        }
        logd("updateUiccCardCount mInsertedSimCount = " + this.mInsertedSimCount);
        return this.mInsertedSimCount;
    }

    public int getInsertedSimCount() {
        return this.mInsertedSimCount;
    }

    public void broadcastQueryResults(AsyncResult ar) {
        this.mNetworkScanIsRunning = 0;
        Intent intent = new Intent("com.huawei.vsim.action.NETWORK_SCAN_COMPLETE");
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
        logd("enableVSim subId = " + 2);
        HwVSimPhoneFactory.setVSimEnabledSubId(2);
        EnableParam param = new EnableParam(imsi, cardType, apnType, acqorder, challenge, operation, tapath, vsimloc);
        if (this.mApkObserver != null) {
            this.mApkObserver.startWatching(this.mContext);
        }
        int result = ((Integer) sendRequest(40, param, 2)).intValue();
        if (result == 0) {
            HwVSimPhoneFactory.setVSimUserEnabled(1);
        } else {
            setApDsFlowCfg(2, 0, 0, 0, 0);
            setDsFlowNvCfg(2, 0, 0);
            if (!(result == 5 || HwVSimPhoneFactory.getVSimUserEnabled() == 1)) {
                logd("enable failure, call disable vsim");
                this.mDisableRetryCount = 0;
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
        VSimEventInfoUtils.setPocessType(this.mEventInfo, 1);
        if (getCardTypeFromEnableParam(this.mEnableRequest) == 1) {
            VSimEventInfoUtils.setCardType(this.mEventInfo, 1);
        } else if (getCardTypeFromEnableParam(this.mEnableRequest) == 2) {
            VSimEventInfoUtils.setCardType(this.mEventInfo, 2);
        } else {
            VSimEventInfoUtils.setCardType(this.mEventInfo, -1);
        }
        this.mVSimEnterTime = SystemClock.elapsedRealtime();
        logi("vsim enter time is " + this.mVSimEnterTime);
        VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
    }

    private void enableExitReport() {
        int i = 1;
        if (this.mEnableRequest != null) {
            int iResult = 3;
            Object oResult = this.mEnableRequest.getResult();
            if (oResult != null) {
                iResult = ((Integer) oResult).intValue();
            }
            if (iResult == 0) {
                VSimEventInfoUtils.setResultType(this.mEventInfo, 1);
                VSimEventInfoUtils.setSimOperator(this.mEventInfo, getOperatorNumeric());
                VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
                VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, -1);
                VSimEventInfoUtils.setSimMode(this.mEventInfo, -1);
                VSimEventInfoUtils.setSlotsTable(this.mEventInfo, -1);
                VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, -1);
                VSimEventInfoUtils.setCardPresent(this.mEventInfo, -1);
                VSimEventInfoUtils.setWorkMode(this.mEventInfo, -1);
            } else {
                VSimEventInfoUtils.setResultType(this.mEventInfo, 2);
                VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
                VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
                VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
                VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, HwVSimPhoneFactory.getVSimSavedMainSlot());
                VSimEventInfo vSimEventInfo = this.mEventInfo;
                if (this.mIsVSimOn) {
                    i = 11;
                }
                VSimEventInfoUtils.setSimMode(vSimEventInfo, i);
                VSimEventInfoUtils.setSlotsTable(this.mEventInfo, getSlotsTableNumeric(this.mSimSlotsTable));
                VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, HwVSimPhoneFactory.getVSimSavedNetworkMode(0));
                VSimEventInfoUtils.setWorkMode(this.mEventInfo, getWorkMode());
            }
        }
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void disableEnterReport() {
        VSimEventInfoUtils.setPocessType(this.mEventInfo, 11);
    }

    private void disableExitReport() {
        if (this.mDisableRequest != null) {
            boolean iResult = false;
            Object oResult = this.mDisableRequest.getResult();
            if (oResult != null) {
                iResult = ((Boolean) oResult).booleanValue();
            }
            if (iResult) {
                VSimEventInfoUtils.setResultType(this.mEventInfo, 1);
            } else {
                VSimEventInfoUtils.setResultType(this.mEventInfo, 2);
            }
        }
        VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
        VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
        VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
        VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, -1);
        VSimEventInfoUtils.setSimMode(this.mEventInfo, -1);
        VSimEventInfoUtils.setSlotsTable(this.mEventInfo, -1);
        VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, -1);
        VSimEventInfoUtils.setCardPresent(this.mEventInfo, -1);
        VSimEventInfoUtils.setWorkMode(this.mEventInfo, -1);
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void reconnectEnterReport() {
        VSimEventInfoUtils.setPocessType(this.mEventInfo, 1);
        VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
        this.mVSimEnterTime = SystemClock.elapsedRealtime();
        logi("vsim enter time is " + this.mVSimEnterTime);
        VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
    }

    private void reconnectExitReport() {
        VSimEventInfoUtils.setResultType(this.mEventInfo, 1);
        VSimEventInfoUtils.setSimOperator(this.mEventInfo, getOperatorNumeric());
        VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
        VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, -1);
        VSimEventInfoUtils.setSimMode(this.mEventInfo, -1);
        VSimEventInfoUtils.setSlotsTable(this.mEventInfo, -1);
        VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, -1);
        VSimEventInfoUtils.setCardPresent(this.mEventInfo, -1);
        VSimEventInfoUtils.setWorkMode(this.mEventInfo, -1);
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void switchEnterReport() {
        VSimEventInfoUtils.setPocessType(this.mEventInfo, 1);
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
                VSimEventInfoUtils.setResultType(this.mEventInfo, 1);
            } else {
                VSimEventInfoUtils.setResultType(this.mEventInfo, 2);
            }
            VSimEventInfoUtils.setSimOperator(this.mEventInfo, getOperatorNumeric());
            VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
            VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, -1);
            VSimEventInfoUtils.setSimMode(this.mEventInfo, -1);
            VSimEventInfoUtils.setSlotsTable(this.mEventInfo, -1);
            VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, -1);
            VSimEventInfoUtils.setCardPresent(this.mEventInfo, -1);
            VSimEventInfoUtils.setWorkMode(this.mEventInfo, -1);
        }
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void setApnReport(int result) {
        VSimEventInfoUtils.setPocessType(this.mEventInfo, 1);
        if (result == 0) {
            VSimEventInfoUtils.setResultType(this.mEventInfo, 1);
        } else {
            VSimEventInfoUtils.setResultType(this.mEventInfo, 2);
        }
        VSimEventInfoUtils.setCauseType(this.mEventInfo, 1);
        VSimEventInfoUtils.setCardType(this.mEventInfo, -1);
        VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
        VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
        VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
        VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, -1);
        VSimEventInfoUtils.setSimMode(this.mEventInfo, -1);
        VSimEventInfoUtils.setSlotsTable(this.mEventInfo, -1);
        VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, -1);
        VSimEventInfoUtils.setCardPresent(this.mEventInfo, -1);
        VSimEventInfoUtils.setWorkMode(this.mEventInfo, -1);
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void saveVSimWorkMode(int workMode) {
        switch (workMode) {
            case 0:
                setVSimULOnlyMode(false);
                setUserReservedSubId(0);
                return;
            case 1:
                setVSimULOnlyMode(false);
                setUserReservedSubId(1);
                return;
            case 2:
                setVSimULOnlyMode(true);
                return;
            default:
                return;
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
        VSimEventInfoUtils.setCauseType(this.mEventInfo, -1);
    }

    private String getOperatorNumeric() {
        if (this.mVSimUiccController == null) {
            return "";
        }
        IccRecords r = this.mVSimUiccController.getIccRecords(1);
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
            return -1;
        }
        if (slotsTable[0] == 0 && slotsTable[1] == 1 && slotsTable[2] == 2) {
            return 0;
        }
        if (slotsTable[0] == 1 && slotsTable[1] == 0 && slotsTable[2] == 2) {
            return 1;
        }
        if (slotsTable[0] == 2 && slotsTable[1] == 1 && slotsTable[2] == 0) {
            return 2;
        }
        if (slotsTable[0] == 2 && slotsTable[1] == 0 && slotsTable[2] == 1) {
            return 3;
        }
        return -1;
    }

    private int getMainSlot(int[] slotsTable) {
        if (slotsTable == null) {
            return -1;
        }
        int mainSlot;
        if (slotsTable[0] == 0 && slotsTable[1] == 1 && slotsTable[2] == 2) {
            mainSlot = 0;
        } else if (slotsTable[0] == 1 && slotsTable[1] == 0 && slotsTable[2] == 2) {
            mainSlot = 1;
        } else if (slotsTable[0] == 2 && slotsTable[1] == 1 && slotsTable[2] == 0) {
            mainSlot = 0;
        } else if (slotsTable[0] == 2 && slotsTable[1] == 0 && slotsTable[2] == 1) {
            mainSlot = 1;
        } else {
            mainSlot = -1;
        }
        return mainSlot;
    }

    private int getWorkMode() {
        boolean isULOnly = getVSimULOnlyMode();
        int reservedSubId = getUserReservedSubId();
        if (isULOnly) {
            return 2;
        }
        if (reservedSubId == 0) {
            return 0;
        }
        if (reservedSubId == 1) {
            return 1;
        }
        return 2;
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

    private void initWhatToStringMap() {
        this.mWhatToStringMap = new HashMap();
        this.mWhatToStringMap.put(Integer.valueOf(2), "EVENT_GET_SIM_STATE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(5), "EVENT_SLOTSWITCH_INIT_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(40), "CMD_ENABLE_VSIM");
        this.mWhatToStringMap.put(Integer.valueOf(41), "EVENT_RADIO_POWER_OFF_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(42), "EVENT_CARD_POWER_OFF_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(43), "EVENT_SWITCH_SLOT_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(44), "EVENT_SET_TEE_DATA_READY_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(45), "EVENT_CARD_POWER_ON_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(46), "EVENT_RADIO_POWER_ON_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(47), "EVENT_SET_ACTIVE_MODEM_MODE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(48), "EVENT_GET_PREFERRED_NETWORK_TYPE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(49), "EVENT_SET_PREFERRED_NETWORK_TYPE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(50), "EVENT_NETWORK_CONNECTED");
        this.mWhatToStringMap.put(Integer.valueOf(51), "EVENT_ENABLE_VSIM_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(10), "CMD_GET_RESERVED_PLMN");
        this.mWhatToStringMap.put(Integer.valueOf(16), "CMD_SET_APDSFLOWCFG");
        this.mWhatToStringMap.put(Integer.valueOf(54), "EVENT_GET_SIM_SLOT_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(55), "EVENT_SWITCH_COMMRIL_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(56), "EVENT_QUERY_CARD_TYPE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(57), "EVENT_ENABLE_VSIM_FINISH");
        this.mWhatToStringMap.put(Integer.valueOf(65), "EVENT_VSIM_PLMN_SELINFO");
        this.mWhatToStringMap.put(Integer.valueOf(66), "EVENT_SET_NETWORK_RAT_AND_SRVDOMAIN_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(71), "EVENT_NETWORK_CONNECT_TIMEOUT");
        this.mWhatToStringMap.put(Integer.valueOf(72), "EVENT_CMD_INTERRUPT");
        this.mWhatToStringMap.put(Integer.valueOf(3), "EVENT_ICC_CHANGED");
        this.mWhatToStringMap.put(Integer.valueOf(17), "EVENT_SET_APDSFLOWCFG_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(18), "CMD_SET_DSFLOWNVCFG");
        this.mWhatToStringMap.put(Integer.valueOf(19), "EVENT_SET_DSFLOWNVCFG_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(12), "CMD_CLEAR_TRAFFICDATA");
        this.mWhatToStringMap.put(Integer.valueOf(13), "EVENT_CLEAR_TRAFFICDATA_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(14), "CMD_GET_TRAFFICDATA");
        this.mWhatToStringMap.put(Integer.valueOf(15), "EVENT_GET_TRAFFICDATA_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(58), "CMD_SWITCH_WORKMODE");
        this.mWhatToStringMap.put(Integer.valueOf(11), "EVENT_GET_RESERVED_PLMN_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(52), "CMD_DISABLE_VSIM");
        this.mWhatToStringMap.put(Integer.valueOf(64), "EVENT_RECONNECT_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(53), "EVENT_DISABLE_VSIM_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(59), "EVENT_SWITCH_WORKMODE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(60), "EVENT_SWITCH_WORKMODE_FINISH");
        this.mWhatToStringMap.put(Integer.valueOf(61), "EVENT_CARD_RELOAD_TIMEOUT");
        this.mWhatToStringMap.put(Integer.valueOf(69), "EVENT_HV_CHECK_CARD");
        this.mWhatToStringMap.put(Integer.valueOf(70), "EVENT_HV_CHECK_CARD_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(64), "EVENT_RECONNECT_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(62), "EVENT_RADIO_STATE_CHANGED_ON");
        this.mWhatToStringMap.put(Integer.valueOf(63), "EVENT_RADIO_STATE_CHANGED_NOTAVAILABLE");
        this.mWhatToStringMap.put(Integer.valueOf(73), "EVENT_GET_MODEM_SUPPORT_VSIM_VER_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(24), "EVENT_NETWORK_SCAN_COMPLETED");
        this.mWhatToStringMap.put(Integer.valueOf(74), "EVENT_DSDS_AUTO_SETMODEM_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(75), "EVENT_INITIAL_TIMEOUT");
        this.mWhatToStringMap.put(Integer.valueOf(76), "EVENT_INITIAL_SUBSTATE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(77), "EVENT_INITIAL_UPDATE_CARDTYPE");
        this.mWhatToStringMap.put(Integer.valueOf(29), "CMD_GET_MODEMSUPPORTVSIMVER_INNER");
        this.mWhatToStringMap.put(Integer.valueOf(30), "EVENT_GET_MODEMSUPPORTVSIMVER_INNER_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(78), "EVENT_INITIAL_GET_MODEM_SUPPORT_VSIM_VER");
        this.mWhatToStringMap.put(Integer.valueOf(79), "EVENT_GET_ICC_STATUS_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(80), "EVENT_SET_CDMA_MODE_SIDE_DONE");
        this.mWhatToStringMap.put(Integer.valueOf(81), "EVENT_JUDGE_RESTART_RILD_NV_MATCH");
        this.mWhatToStringMap.put(Integer.valueOf(82), "EVENT_JUDGE_RESTART_RILD_NV_MATCH_TIMEOUT");
        this.mWhatToStringMap.put(Integer.valueOf(83), "EVENT_RADIO_AVAILABLE");
        this.mWhatToStringMap.put(Integer.valueOf(84), "CMD_RESTART_RILD_FOR_NV_MATCH");
    }

    private boolean cmdSem_acquire() {
        if (this.mCmdSem == null) {
            return false;
        }
        boolean acquired;
        try {
            logd("cmd sem try acquire");
            acquired = this.mCmdSem.tryAcquire(90000, TimeUnit.MILLISECONDS);
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
        final long endTime = SystemClock.elapsedRealtime() + ((long) timeout);
        Thread t = new Thread() {
            public void run() {
                HwVSimController.this.logd("Waiting for vsim ...");
                while (SystemClock.elapsedRealtime() < endTime) {
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
        };
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
        intent.putExtra("subscription", 2);
        intent.putExtra("phone", 2);
        intent.putExtra("slot", 2);
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, 2);
        intent.putExtra("vsim_cardtype", this.mCurCardType);
        intent.putExtra("vsim_old_cardtype", this.mOldCardType);
        this.mContext.sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    private int setApn(String imsi, int cardType, int apnType, String tapath, String challenge, boolean isForHash) {
        logd("setApn cardType: " + cardType + " apnType: " + apnType + " isForHash: " + isForHash);
        int subId = HwVSimPhoneFactory.getVSimEnabledSubId();
        logi("subId = " + subId);
        if (subId == -1) {
            return 3;
        }
        int result = writeApnToTA(imsi, cardType, apnType, tapath, challenge, isForHash);
        if (result == 0) {
            result = ((Integer) sendRequest(20, null, subId)).intValue();
        }
        this.mVSimPhone.setDataEnabled(false);
        this.mVSimPhone.setDataEnabled(true);
        logi("setApn result = " + result);
        setApnReport(result);
        return result;
    }

    private int writeApnToTA(String imsi, int cardType, int apnType, String tapath, String challenge, boolean isForHash) {
        logi("writeApnToTA");
        if (challenge != null && challenge.length() != 0) {
            return HwVSimOperateTA.getDefault().operateTA(2, imsi, cardType, apnType, challenge, isForHash, tapath, 0, 0);
        }
        logi("invalid param challenge");
        return 1;
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
        for (i = 0; i < cardCount; i++) {
            if (cardTypes[i] == 0) {
                isCardPresent[i] = false;
            } else {
                isCardPresent[i] = true;
            }
        }
        i = 0;
        while (i < cardCount) {
            if (isSubOnM2(i) && isCardPresent[i]) {
                return true;
            }
            i++;
        }
        return false;
    }

    private boolean needSwitchModeHotplug(int[] cardTypes) {
        if (HwVSimUtilsInner.isChinaTelecom()) {
            return needSwitchModeHotplugForTelecom(cardTypes);
        }
        int mainSlot = getMainSlot(this.mSimSlotsTable);
        boolean isM2hasSimAndM0IsULG = (!hasIccCardOnM2(cardTypes) || (getULOnlyProp() ^ 1) == 0) ? false : getVSimULOnlyMode();
        if (isM2hasSimAndM0IsULG) {
            boolean isCSIMOnM2 = !HwVSimUtilsInner.isFullNetworkSupported() ? cardTypes[mainSlot] != 2 ? cardTypes[mainSlot] == 3 : true : false;
            if (isCSIMOnM2) {
                logd("needSwitchModeHotplug false CSIM on m2");
                return false;
            }
            logd("needSwitchModeHotplug:icc card on m2");
            return true;
        } else if (HwVSimUtilsInner.isFullNetworkSupported()) {
            int oldInsertedCardCount = getInsertedCardCount();
            int newInsertedCardCount = getInsertedCardCount(cardTypes);
            int slaveSlot = mainSlot == 0 ? 1 : 0;
            if (!getVSimULOnlyMode()) {
                int reservedSub = getUserReservedSubId();
                if (newInsertedCardCount == 2 && reservedSub != -1) {
                    int noReservedSub = reservedSub == 0 ? 1 : 0;
                    logd("needSwitchModeHotplug: noReservedSub = " + noReservedSub + " set to no sim");
                    cardTypes[noReservedSub] = 0;
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
            logd("needSwitchModeHotplug: cardTypes[0] = " + cardTypes[0] + "cardTypes[1] = " + cardTypes[1]);
            boolean singleCardOnM2AndNoSimOnM1 = cardTypes[mainSlot] != 0 ? cardTypes[slaveSlot] == 0 : false;
            if (checkIfHasDualCdmaCard(cardTypes, expect, mainSlot)) {
                return false;
            }
            if (checkIfHasCdmaCardInDualImsSupport(cardTypes, current, mainSlot)) {
                return true;
            }
            if (checkIfHasCdmaCard(cardTypes, expect, mainSlot)) {
                return true;
            }
            if (expect != current && (HwVSimUtilsInner.isDualImsSupported() ^ 1) != 0) {
                return true;
            }
            if (!singleCardOnM2AndNoSimOnM1) {
                return needSwitchModeHotplugForCMCC(cardTypes);
            }
            logd("needSwitchModeHotplug: singleCardOnM2AndNoSimOnM1 return true.");
            return true;
        } else {
            logd("needSwitchModeHotplug false CSIM on m1");
            return false;
        }
    }

    private boolean needSwitchModeHotplugForTelecom(int[] cardTypes) {
        if (hasIccCardOnM2(cardTypes) && (getULOnlyProp() ^ 1) != 0 && getVSimULOnlyMode()) {
            logd("needSwitchModeHotplugForTelecom: icc card on m2");
            return true;
        }
        int mainSlot = getMainSlot(this.mSimSlotsTable);
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        logd("needSwitchModeHotplugForTelecom:slaveSlot=" + slaveSlot + " mainSlot =" + mainSlot + " cardTypes[0] = " + cardTypes[0] + " cardTypes[1] = " + cardTypes[1]);
        boolean singleGsmOnM1OrCdmaOnM2 = (cardTypes[slaveSlot] != 1 || cardTypes[mainSlot] == 1) ? (cardTypes[slaveSlot] == 2 || cardTypes[slaveSlot] == 3) ? false : cardTypes[mainSlot] != 2 ? cardTypes[mainSlot] == 3 : true : true;
        if (!HwVSimUtilsInner.isPlatformRealTripple()) {
            if (!getVSimULOnlyMode()) {
                int reservedSub = getUserReservedSubId();
                if (getInsertedCardCount(cardTypes) == 2 && reservedSub != -1) {
                    int noReservedSub = reservedSub == 0 ? 1 : 0;
                    logd("needSwitchModeHotplugForTelecom: noReservedSub = " + noReservedSub + " set to no sim");
                    cardTypes[noReservedSub] = 0;
                }
            }
            CommrilMode expect = getExpectCommrilMode(mainSlot, cardTypes);
            if (expect == CommrilMode.NON_MODE) {
                return false;
            }
            CommrilMode current = getCommrilMode();
            logd("needSwitchModeHotplugForTelecom: expect = " + expect + ", current = " + current);
            return checkIfHasCdmaCardForTelecom(cardTypes, expect, mainSlot) || expect != current;
        } else if (singleGsmOnM1OrCdmaOnM2) {
            return true;
        }
    }

    private boolean needSwitchModeHotplugForCMCC(int[] cardTypes) {
        if (!HwVSimUtilsInner.IS_CMCC_4GSWITCH_DISABLE) {
            return false;
        }
        int insertCardCount = getInsertedCardCount(cardTypes);
        logd("needSwitchModeHotplugForCMCC: insertCardCount = " + insertCardCount);
        boolean result = false;
        if (insertCardCount == 2 && HwAllInOneController.getInstance().isCMCCHybird()) {
            logd("needSwitchModeHotplugForCMCC: hybird");
            result = true;
        }
        return result;
    }

    private void processNoNeedSwitchModeForCmcc(int[] cardTypes) {
        if (HwVSimUtilsInner.IS_CMCC_4GSWITCH_DISABLE) {
            int insertCardCount = getInsertedCardCount(cardTypes);
            logd("processNoNeedSwitchModeForCmcc: insertCardCount = " + insertCardCount);
            if (insertCardCount == 1) {
                int subId;
                if (cardTypes[0] == 0) {
                    subId = 1;
                } else {
                    subId = 0;
                }
                this.mModemAdapter.setPreferredNetworkType(null, null, subId, HwVSimPhoneFactory.getVSimSavedNetworkMode(0));
            }
        }
    }

    private boolean checkIfHasCdmaCardInDualImsSupport(int[] cardTypes, CommrilMode current, int mainSlot) {
        boolean z = true;
        if (!HwVSimUtilsInner.isDualImsSupported()) {
            return false;
        }
        CommrilMode vimOnCommrilMode = this.mVSimSlotSwitchController.getVSimOnCommrilMode(true, mainSlot, cardTypes);
        logd("needSwitchModeHotplug: vimOnCommrilMode = " + vimOnCommrilMode);
        if (current == vimOnCommrilMode) {
            z = false;
        }
        return z;
    }

    private boolean checkIfHasCdmaCard(int[] cardTypes, CommrilMode expect, int mainSlot) {
        if (HwVSimUtilsInner.isDualImsSupported()) {
            return false;
        }
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        if (CommrilMode.isCLGMode(expect, cardTypes, mainSlot) || CommrilMode.isCGMode(expect, cardTypes, mainSlot)) {
            if (getCommrilMode() == CommrilMode.getULGMode()) {
                logd("checkIfHasCdmaCard: ULG to CG");
                return true;
            } else if (!HwVSimSlotSwitchController.isCDMACard(cardTypes[slaveSlot]) && HwVSimSlotSwitchController.isCDMACard(cardTypes[mainSlot])) {
                logd("checkIfHasCdmaCard: cdma card match to c-modem");
                return true;
            }
        }
        return false;
    }

    private boolean checkIfHasCdmaCardForTelecom(int[] cardTypes, CommrilMode expect, int mainSlot) {
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        if (CommrilMode.isCLGMode(expect, cardTypes, mainSlot) || CommrilMode.isCGMode(expect, cardTypes, mainSlot)) {
            if (getCommrilMode() == CommrilMode.getULGMode()) {
                logd("checkIfHasCdmaCardForTelecom: ULG to CG");
                return true;
            } else if ((cardTypes[slaveSlot] == 0 || cardTypes[slaveSlot] == 1) && (cardTypes[mainSlot] == 2 || cardTypes[mainSlot] == 3)) {
                logd("checkIfHasCdmaCardForTelecom for telecom: cdma card match to c-modem");
                return true;
            }
        }
        return false;
    }

    private boolean checkIfHasDualCdmaCard(int[] cardTypes, CommrilMode expect, int mainSlot) {
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        if (!CommrilMode.isCLGMode(expect, cardTypes, mainSlot) || getCommrilMode() != CommrilMode.getCGMode() || !HwVSimSlotSwitchController.isCDMACard(cardTypes[slaveSlot]) || !HwVSimSlotSwitchController.isCDMACard(cardTypes[mainSlot])) {
            return false;
        }
        logd("checkIfHasDualCdmaCard: two cdma cards insert.");
        return true;
    }

    public void setPreferredNetworkTypeEnableFlag(int flag) {
        this.mPreferredNetworkTypeEnableFlag = flag;
        logd("mPreferredNetworkTypeEnableFlag = " + flag);
    }

    public int getPreferredNetworkTypeEnableFlag() {
        return this.mPreferredNetworkTypeEnableFlag;
    }

    public void setPreferredNetworkTypeDisableFlag(int flag) {
        this.mPreferredNetworkTypeDisableFlag = flag;
        logd("mPreferredNetworkTypeDisableFlag = " + flag);
    }

    public int getPreferredNetworkTypeDisableFlag() {
        return this.mPreferredNetworkTypeDisableFlag;
    }

    public void setBlockPinFlag(boolean value) {
        this.mBlockPinFlag = value;
        logd("mBlockPinFlag = " + this.mBlockPinFlag);
    }

    public void setBlockPinTable(int subId, boolean value) {
        if (this.mBlockPinTable != null && this.mPinBlockedTable != null) {
            for (int i = 0; i < HwVSimModemAdapter.PHONE_COUNT; i++) {
                this.mBlockPinTable[i] = false;
                this.mPinBlockedTable[i] = false;
            }
            if (subId >= 0 && subId < HwVSimModemAdapter.PHONE_COUNT) {
                this.mBlockPinTable[subId] = value;
                logi("mBlockPinTable = " + Arrays.toString(this.mBlockPinTable));
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x005a, code:
            return false;
     */
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

    /* JADX WARNING: Missing block: B:4:0x004d, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isPinBlocked(int subId) {
        logd("isPinBlocked, subId = " + subId + ", mBlockPinFlag = " + this.mBlockPinFlag + ", mBlockPinTable = " + Arrays.toString(this.mBlockPinTable) + ", mPinBlockedTable = " + Arrays.toString(this.mPinBlockedTable));
        if (subId < 0 || subId >= HwVSimModemAdapter.PHONE_COUNT || !this.mBlockPinFlag || !this.mBlockPinTable[subId]) {
            return false;
        }
        return this.mPinBlockedTable[subId];
    }

    /* JADX WARNING: Missing block: B:4:0x003c, code:
            return false;
     */
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
        int dataSubId = -1;
        SubscriptionController subController = SubscriptionController.getInstance();
        if (subController != null) {
            dataSubId = subController.getDefaultDataSubId();
        }
        logd("setupDataOnVSimEnded data sub: " + dataSubId);
        if (dataSubId != -1 && dataSubId < this.mPhones.length) {
            isDataEnabled = this.mPhones[dataSubId].getDataEnabled();
            allowData(dataSubId);
        }
        if (isDataEnabled) {
            Message msg = this.mMainHandler.obtainMessage();
            msg.what = 2;
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
        this.mMainHandler.obtainMessage(1).sendToTarget();
    }

    private void onUpdateUserPreferences() {
        logd("onUpdateUserPreferences");
        HwTelephonyFactory.getHwUiccManager().updateUserPreferences(false);
    }

    public boolean isEnableProhibitByDisableRetry() {
        if (HwVSimPhoneFactory.getVSimUserEnabled() == 1) {
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
        this.mMainHandler.sendMessage(this.mMainHandler.obtainMessage(3));
        if (isVSimEnabled()) {
            logd("skip auto switch slot when vsim enabled");
        } else {
            this.mMainHandler.sendMessage(this.mMainHandler.obtainMessage(5));
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
            HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(HwTelephonyManagerInner.getDefault().getDefault4GSlotId());
        }
    }

    private void onAutoSetAllowData() {
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            int allowDataSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            if (this.mPhones != null) {
                int len = this.mPhones.length;
                logd("onAutoSetAllowData, mPhones.length=" + len);
                for (int phoneId = 0; phoneId < len; phoneId++) {
                    Phone phone = this.mPhones[phoneId];
                    if (phone == null) {
                        logd("active phone not found");
                    } else {
                        DcTracker dcTracker = phone.mDcTracker;
                        if (dcTracker == null) {
                            logd("dcTracker not found");
                        } else if (phoneId == allowDataSlotId) {
                            logd("call set data allowed on sub: " + phoneId);
                            dcTracker.setDataAllowed(true, this.mMainHandler.obtainMessage(4));
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

    public void setSubActivationUpdate(boolean isUpate) {
        logd("setSubActivationUpdate : " + isUpate);
        this.mIsSubActivationUpdate = isUpate;
    }

    public void delaymIsVSimCauseCardReloadRecover() {
        if (HwVSimUtilsInner.isDualImsSupported()) {
            logd("delaymIsVSimCauseCardReloadRecover mIsVSimCauseCardReload: " + this.mIsVSimCauseCardReload);
            if (!this.mIsVSimCauseCardReload) {
                this.mIsVSimCauseCardReload = true;
                this.mMainHandler.sendMessageDelayed(this.mMainHandler.obtainMessage(6), 5000);
            }
        }
    }

    private void onRecoverMarkToFalse() {
        logd("onRecoverMarkToFalse mIsVSimCauseCardReload: " + this.mIsVSimCauseCardReload);
        this.mIsVSimCauseCardReload = false;
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
            case 4:
            case 5:
            case 6:
                convertedNetworkMode = 3;
                break;
            case 8:
                convertedNetworkMode = 9;
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
            subState = 0;
            if (subId >= 0) {
                if (subId < this.mSubStates.length) {
                    subState = this.mSubStates[subId];
                }
            }
            logd("[SUB" + subId + "] getSubState : " + subState);
        }
        return subState;
    }

    /* JADX WARNING: Missing block: B:12:0x0025, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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

    private void initCheckSubActived(int[] cardTypes) {
        if (cardTypes != null) {
            for (int i = 0; i < cardTypes.length; i++) {
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
            for (int i = 0; i < HwVSimModemAdapter.PHONE_COUNT; i++) {
                this.mProhibitSubUpdateSimNoChange[i] = false;
            }
        }
    }

    public synchronized void setIsWaitingSwitchCdmaModeSide(boolean value) {
        this.isWaitingSwitchCdmaModeSide = value;
        logd("set isWaitingSwitchCdmaModeSide = " + this.isWaitingSwitchCdmaModeSide);
    }

    public synchronized boolean getIsWaitingSwitchCdmaModeSide() {
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

    public void simHotPlugOut(int slotId) {
        logd("simHotPlugOut, slotId: " + slotId);
        if (slotId >= 0 && slotId < HwVSimModemAdapter.MAX_SUB_COUNT) {
            setMarkForCardReload(slotId, false);
        }
    }

    public void setAlternativeUserReservedSubId(int subId) {
        if (HwVSimUtilsInner.isVSimDsdsVersionOne()) {
            this.mAlternativeUserReservedSubId = subId;
        }
    }

    public int getRadioOnSubId() {
        for (int subId = 0; subId < HwVSimModemAdapter.MAX_SUB_COUNT; subId++) {
            CommandsInterface ci = getCiBySub(subId);
            if (ci != null && ci.getRadioState().isOn()) {
                return subId;
            }
        }
        return 0;
    }

    public void clearApDsFlowCfg() {
        int subId = getVSimSubId();
        logd("clearApDsFlowCfg, subId = " + subId);
        if (subId != 2) {
            subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            logd("clearApDsFlowCfg, VSim not enabled, to mainslot: " + subId);
        }
        setApDsFlowCfg(subId, 0, 0, 0, 0);
        setDsFlowNvCfg(subId, 0, 0);
    }
}
