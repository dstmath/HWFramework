package com.android.internal.telephony.vsim;

import android.app.ActivityManager;
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
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.DcFailCause;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.process.HwVSimDReadyProcessor;
import com.android.internal.telephony.vsim.process.HwVSimDWorkProcessor;
import com.android.internal.telephony.vsim.process.HwVSimDefaultProcessor;
import com.android.internal.telephony.vsim.process.HwVSimDisableProcessor;
import com.android.internal.telephony.vsim.process.HwVSimEReadyProcessor;
import com.android.internal.telephony.vsim.process.HwVSimEWorkProcessor;
import com.android.internal.telephony.vsim.process.HwVSimEnableProcessor;
import com.android.internal.telephony.vsim.process.HwVSimInitialProcessor;
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
    public static final int STATE_RESTART_RILD = 14;
    public static final int STATE_SWITCHMODE = 11;
    public static final int STATE_SWITCHMODE_READY = 13;
    public static final int STATE_SWITCHMODE_WORK = 12;
    private static final int SWITCH_DELAY_TIME = 2000;
    private static final int VSIM_MODEM_COUNT = SystemProperties.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static final String VSIM_PKG_NAME = "com.huawei.skytone";
    private static final int VSIM_STATE_POLL_SLEEP_MSEC = 500;
    private static final Object sCheckSubActivedLock = new Object();
    /* access modifiers changed from: private */
    public static HwVSimController sInstance = null;
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
    private VSimDReadyState mDReadyState = new VSimDReadyState();
    private VSimDWorkState mDWorkState = new VSimDWorkState();
    private VSimDefaultState mDefaultState = new VSimDefaultState();
    /* access modifiers changed from: private */
    public boolean mDisableFailMark;
    /* access modifiers changed from: private */
    public HwVSimRequest mDisableRequest;
    /* access modifiers changed from: private */
    public int mDisableRetryCount;
    private VSimDisableState mDisableState = new VSimDisableState();
    private VSimEReadyState mEReadyState = new VSimEReadyState();
    private VSimEWorkState mEWorkState = new VSimEWorkState();
    /* access modifiers changed from: private */
    public HwVSimRequest mEnableRequest;
    private VSimEnableState mEnableState = new VSimEnableState();
    public HwVSimEventReport.VSimEventInfo mEventInfo;
    private InitialState mInitialState = new InitialState();
    private int mInsertedSimCount = 0;
    private boolean mIsRegNetworkReceiver;
    private boolean mIsSubActivationUpdate;
    private boolean mIsVSimCauseCardReload;
    private boolean mIsVSimOn;
    private boolean mIsWaitingNvMatchUnsol = false;
    private boolean mIsWaitingSwitchSimSlot = false;
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
    /* access modifiers changed from: private */
    public HwVSimModemAdapter mModemAdapter;
    private boolean[] mNeedSimLoadedMark;
    /* access modifiers changed from: private */
    public int mNetworkScanIsRunning = 0;
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
    private RestartRildState mRestartRildState = new RestartRildState();
    /* access modifiers changed from: private */
    public int mRetryCountForHotPlug = 0;
    private int mRule = -1;
    private ServiceStateHandler mServiceStateHandlerForVsim;
    private ServiceStateHandler[] mServiceStateHandlers;
    private int[] mSimCardTypes;
    private int[] mSimSlotsTable;
    private VSimSwitchModeReadyState mSmReadyState = new VSimSwitchModeReadyState();
    private VSimSwitchModeWorkState mSmWorkState = new VSimSwitchModeWorkState();
    private String mSpn;
    private int[] mSubStates;
    /* access modifiers changed from: private */
    public HwVSimRequest mSwitchModeRequest;
    private VSimSwitchModeState mSwitchModeState = new VSimSwitchModeState();
    private UiccController mUiccController;
    private int mVSimCardCount = -1;
    private CommandsInterface mVSimCi;
    public long mVSimEnterTime;
    private HwVSimEventHandler mVSimEventHandler;
    private HwVSimEventReport mVSimEventReport;
    private HandlerThread mVSimEventThread;
    private int mVSimFrameworkSupportVer = 10000;
    private boolean[] mVSimLockPower;
    private int mVSimModemSupportVer = -2;
    private Phone mVSimPhone;
    private int mVSimSavedMainSlot = -1;
    private HwVSimSlotSwitchController mVSimSlotSwitchController;
    private HwVSimUiccController mVSimUiccController;
    private HashMap<Integer, String> mWhatToStringMap;

    public static class EnableParam {
        public String acqorder;
        public int apnType;
        public int cardInModem1;
        public int cardType;
        public String challenge;
        public String imsi;
        public boolean isVSimOn = false;
        public int operation;
        public String taPath;
        public int vsimLoc;

        public EnableParam(String imsi2, int cardType2, int apnType2, String acqorder2, String challenge2, int operation2, String tapath, int vsimloc, int cardInModem12) {
            this.imsi = imsi2;
            this.cardType = cardType2;
            this.apnType = apnType2;
            this.acqorder = acqorder2;
            this.challenge = challenge2;
            this.operation = operation2;
            this.taPath = tapath;
            this.vsimLoc = vsimloc;
            this.cardInModem1 = cardInModem12;
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
                case 1000:
                    HwVSimController hwVSimController = HwVSimController.this;
                    hwVSimController.logd("EVENT_HOTPLUG_SWITCHMODE, count = " + HwVSimController.this.mRetryCountForHotPlug);
                    if (HwVSimController.this.canProcessSwitchMode()) {
                        boolean unused = HwVSimController.this.switchVSimWorkMode(HwVSimController.this.getWorkMode(), true);
                        return;
                    }
                    HwVSimController.this.handleSwitchModeDelay();
                    return;
                case 1001:
                    HwVSimController hwVSimController2 = HwVSimController.this;
                    hwVSimController2.logd("EVENT_VSIM_DISABLE_RETRY, count = " + HwVSimController.this.mDisableRetryCount);
                    if (HwVSimController.this.mDisableRetryCount >= 3) {
                        HwVSimController.this.logd("max count, abort retry");
                        return;
                    }
                    int unused2 = HwVSimController.this.mDisableRetryCount = HwVSimController.this.mDisableRetryCount + 1;
                    if (!HwVSimController.this.mDisableFailMark) {
                        HwVSimController.this.logd("no fail mark, abort retry");
                        return;
                    } else {
                        HwVSimController.this.disableVSim();
                        return;
                    }
                default:
                    return;
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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("InitialState: what = " + HwVSimController.this.getWhatToString(msg.what));
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
            return this == PROCESS_ACTION_ENABLE || this == PROCESS_ACTION_ENABLE_OFFLINE;
        }

        public boolean isDisableProcess() {
            return this == PROCESS_ACTION_DISABLE || this == PROCESS_ACTION_DISABLE_OFFLINE;
        }

        public boolean isOfflineProcess() {
            return this == PROCESS_ACTION_ENABLE_OFFLINE || this == PROCESS_ACTION_DISABLE_OFFLINE;
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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("RestartRildState: what = " + HwVSimController.this.getWhatToString(msg.what));
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
            if (msg.what == 1) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    HwVSimController.this.logd("exception, return");
                    return;
                }
                ServiceState newSS = (ServiceState) ar.result;
                notifyServiceStateChanged(newSS, this.mSS, this.mPhone);
                this.mSS = new ServiceState(newSS);
            }
        }

        private void notifyServiceStateChanged(ServiceState newSS, ServiceState oldSS, Phone phone) {
            if (newSS == null || oldSS == null || phone == null) {
                HwVSimController.this.logd("ss or phone is null, return!");
                return;
            }
            HwVSimController.this.logd("newSS:" + newSS + " oldSS:" + oldSS);
            boolean registeredStatuChanged = newSS.getVoiceRegState() != oldSS.getVoiceRegState();
            boolean plmnStatuChanged = !equalsHandlesNulls(newSS.getVoiceOperatorNumeric(), oldSS.getVoiceOperatorNumeric());
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
            }
            return a.equals(b);
        }
    }

    private class VSimDReadyState extends State {
        HwVSimDReadyProcessor mProcessor;

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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("DReadyState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimDWorkState extends State {
        HwVSimDWorkProcessor mProcessor;

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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("DWorkState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimDefaultState extends State {
        HwVSimDefaultProcessor mProcessor;

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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("DefaultState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }

        public int networksScan(int subId, int type) {
            if (this.mProcessor != null) {
                return this.mProcessor.networksScan(subId, type);
            }
            int unused = HwVSimController.this.mNetworkScanIsRunning = 0;
            return 1;
        }
    }

    private class VSimDisableState extends State {
        HwVSimDisableProcessor mProcessor;

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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("DisableState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimEReadyState extends State {
        HwVSimEReadyProcessor mProcessor;

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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("EReadyState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimEWorkState extends State {
        HwVSimEWorkProcessor mProcessor;

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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("EWorkState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimEnableState extends State {
        HwVSimEnableProcessor mProcessor;

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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("EnableState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeReadyState extends State {
        HwVSimSReadyProcessor mProcessor;

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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("VSimSReadyState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeState extends State {
        HwVSimSwitchModeProcessor mProcessor;

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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("VSimSwitchModeState: what = " + HwVSimController.this.getWhatToString(msg.what));
            if (this.mProcessor == null) {
                return false;
            }
            return this.mProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeWorkState extends State {
        HwVSimSWorkProcessor mProcessor;

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
            HwVSimController hwVSimController = HwVSimController.this;
            hwVSimController.logd("VSimSWorkState: what = " + HwVSimController.this.getWhatToString(msg.what));
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

        public WorkModeParam(int workMode2, int oldMode2, boolean isHotplug2) {
            this.workMode = workMode2;
            this.oldMode = oldMode2;
            this.isHotplug = isHotplug2;
        }
    }

    public static void create(Context context, Phone vsimPhone, CommandsInterface vsimCi, Phone[] phones, CommandsInterface[] cis) {
        slogd("create");
        synchronized (sLock) {
            if (sInstance == null) {
                HwVSimController hwVSimController = new HwVSimController(context, vsimPhone, vsimCi, phones, cis);
                sInstance = hwVSimController;
                sInstance.start();
            } else {
                throw new RuntimeException("VSimController already created");
            }
        }
    }

    public static HwVSimController getInstance() {
        HwVSimController hwVSimController;
        synchronized (sLock) {
            if (sInstance != null) {
                hwVSimController = sInstance;
            } else {
                throw new RuntimeException("VSimController not yet created");
            }
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
        this.mEventInfo = new HwVSimEventReport.VSimEventInfo();
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mEventInfo, -1);
        this.mLastReportTime = 0;
        this.mVSimEnterTime = 0;
        this.mVSimEventThread = new HandlerThread("VSimEventThread");
        this.mVSimEventThread.start();
        this.mVSimEventHandler = new HwVSimEventHandler(this.mVSimEventThread.getLooper());
        this.mApkObserver = new HwVSimApkObserver();
        this.mCardTypes = null;
        this.mSimSlotsTable = null;
        this.mIsVSimOn = false;
        this.mVSimLockPower = new boolean[HwVSimModemAdapter.MAX_SUB_COUNT];
        for (int i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
            this.mVSimLockPower[i] = false;
        }
        this.mIsVSimCauseCardReload = false;
        this.mNeedSimLoadedMark = new boolean[HwVSimModemAdapter.MAX_SUB_COUNT];
        for (int i2 = 0; i2 < HwVSimModemAdapter.MAX_SUB_COUNT; i2++) {
            this.mNeedSimLoadedMark[i2] = false;
        }
        this.mIsSubActivationUpdate = false;
        this.mSubStates = new int[HwVSimModemAdapter.PHONE_COUNT];
        this.mSimCardTypes = new int[HwVSimModemAdapter.PHONE_COUNT];
        this.mCheckSubActivated = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        this.mProhibitSubUpdateSimNoChange = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        for (int i3 = 0; i3 < this.mSubStates.length; i3++) {
            this.mSimCardTypes[i3] = -1;
            this.mSubStates[i3] = -1;
            this.mCheckSubActivated[i3] = false;
            this.mProhibitSubUpdateSimNoChange[i3] = false;
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
        addState(this.mSwitchModeState, this.mDefaultState);
        addState(this.mRestartRildState, this.mDefaultState);
        addState(this.mEWorkState, this.mEnableState);
        addState(this.mEReadyState, this.mEnableState);
        addState(this.mDWorkState, this.mDisableState);
        addState(this.mDReadyState, this.mDisableState);
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

    /* access modifiers changed from: protected */
    public void unhandledMessage(Message msg) {
        logd(" - unhandledMessage: msg.what=" + msg.what);
    }

    /* access modifiers changed from: protected */
    public String getWhatToString(int what) {
        String result = null;
        if (this.mWhatToStringMap != null) {
            result = this.mWhatToStringMap.get(Integer.valueOf(what));
        }
        if (result != null) {
            return result;
        }
        return "<unknown message> - " + what;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        HwVSimController.super.dump(fd, pw, args);
        StringBuilder sb = new StringBuilder();
        sb.append(" mSimSlotsTable=");
        sb.append(this.mSimSlotsTable == null ? "[]" : Arrays.toString(this.mSimSlotsTable));
        pw.println(sb.toString());
    }

    public int enableVSim(int operation, String imsi, int cardType, int apnType, String acqorder, String tapath, int vsimloc, String challenge) {
        int i = operation;
        StringBuilder sb = new StringBuilder();
        sb.append("enableVSim, operation: ");
        sb.append(i);
        sb.append(", cardType: ");
        int i2 = cardType;
        sb.append(i2);
        sb.append(", apnType: ");
        int i3 = apnType;
        sb.append(i3);
        sb.append(", acqorder: ");
        String str = acqorder;
        sb.append(str);
        sb.append(", vsimloc: ");
        int i4 = vsimloc;
        sb.append(i4);
        logd(sb.toString());
        if (i == 5) {
            return 3;
        }
        switch (i) {
            case 1:
                return enableVSim(imsi, i2, i3, str, tapath, i4, challenge, i, -1);
            case 2:
                return setApn(imsi, i2, i3, tapath, challenge, false);
            default:
                logd("enableVSim do nothing");
                return 3;
        }
    }

    public int enableVSim(int operation, Bundle bundle) {
        Bundle bundle2 = bundle;
        int result = 3;
        if (bundle2 == null) {
            logd("enableVSim, bundle is null, return fail.");
            return 3;
        }
        String imsi = bundle2.getString(HwVSimConstants.ENABLE_PARA_IMSI);
        int cardType = bundle2.getInt(HwVSimConstants.ENABLE_PARA_CARDTYPE);
        int apnType = bundle2.getInt(HwVSimConstants.ENABLE_PARA_APNTYPE);
        String acqorder = bundle2.getString(HwVSimConstants.ENABLE_PARA_ACQORDER);
        String tapath = bundle2.getString(HwVSimConstants.ENABLE_PARA_TAPATH);
        int vsimloc = bundle2.getInt(HwVSimConstants.ENABLE_PARA_VSIMLOC);
        String challenge = bundle2.getString(HwVSimConstants.ENABLE_PARA_CHALLENGE);
        int cardInModem1 = bundle2.getInt(HwVSimConstants.ENABLE_PARA_CARD_IN_MODEM1, -1);
        switch (operation) {
            case 1:
            case 6:
                result = enableVSim(imsi, cardType, apnType, acqorder, tapath, vsimloc, challenge, operation, cardInModem1);
                break;
            case 2:
                result = setApn(imsi, cardType, apnType, tapath, challenge, false);
                break;
            case 7:
                String spn = bundle2.getString("spn");
                int rule = bundle2.getInt(HwVSimConstants.ENABLE_PARA_VSIM_RULE, -1);
                setSpn(spn);
                setRule(rule);
                int i = rule;
                String str = spn;
                result = enableVSim(imsi, cardType, apnType, acqorder, tapath, vsimloc, challenge, operation, cardInModem1);
                break;
            default:
                logd("enableVSim, op is invalid, do nothing");
                break;
        }
        return result;
    }

    public boolean isSupportVSimByOperation(int operation) {
        switch (operation) {
            case 1:
            case 2:
            case 7:
                return true;
            case 6:
                return HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.isDualImsSupported();
            default:
                return false;
        }
    }

    public void setSpn(String spn) {
        logd("setSpn to " + spn);
        this.mSpn = spn;
    }

    public String getSpn() {
        logd("getSpn " + this.mSpn);
        return this.mSpn;
    }

    public void setRule(int rule) {
        logd("setRule to " + rule);
        this.mRule = rule;
    }

    public int getRule() {
        logd("getRule " + this.mRule);
        return this.mRule;
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
                this.mVSimEventHandler.removeMessages(1001);
                HwVSimPhoneFactory.setVSimEnabledSubId(-1);
                HwVSimPhoneFactory.setVSimUserEnabled(0);
                clearApDsFlowCfg();
                if (this.mApkObserver != null) {
                    this.mApkObserver.stopWatching();
                }
                if (HwVSimUtilsInner.isPlatformTwoModemsActual()) {
                    setupDataOnVSimEnded();
                }
            } else {
                this.mDisableFailMark = true;
                logd("send EVENT_VSIM_DISABLE_RETRY");
                this.mVSimEventHandler.sendEmptyMessageDelayed(1001, HwVSimConstants.VSIM_DISABLE_RETRY_TIMEOUT);
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

    /* access modifiers changed from: private */
    public boolean switchVSimWorkMode(int workMode, boolean isHotplug) {
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
        if (VSIM_PKG_NAME.equals(callingAppName) || HSF_PKG_NAME.equals(callingAppName)) {
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
        if (VSIM_PKG_NAME.equals(callingAppName) || HSF_PKG_NAME.equals(callingAppName)) {
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
        if (VSIM_PKG_NAME.equals(callingAppName) || HSF_PKG_NAME.equals(callingAppName)) {
            boolean result = setApDsFlowCfg(subId, repFlag, threshold, totalThreshold, oper);
            if (!result || repFlag != 1) {
                setDsFlowNvCfg(subId, 0, 0);
            } else {
                setDsFlowNvCfg(subId, 1, 10);
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
        boolean z = false;
        if (this.mProcessAction == null) {
            return false;
        }
        if (this.mProcessAction.isEnableProcess() || this.mProcessAction.isDisableProcess() || this.mProcessAction.isSwitchModeProcess()) {
            z = true;
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
        if (this.mVSimEventHandler.hasMessages(1000)) {
            this.mVSimEventHandler.removeMessages(1000);
        }
        this.mNvMatchController.startNvMatchUnsolListener();
        if (needSwtich) {
            initCheckSubActived(cardTypes);
            Message msg = this.mVSimEventHandler.obtainMessage();
            msg.what = 1000;
            this.mVSimEventHandler.sendMessage(msg);
            return;
        }
        processNoNeedSwitchModeForCmcc(cardTypes);
    }

    /* access modifiers changed from: private */
    public void handleSwitchModeDelay() {
        if (this.mVSimEventHandler.hasMessages(1000)) {
            this.mVSimEventHandler.removeMessages(1000);
        }
        this.mRetryCountForHotPlug++;
        if (this.mRetryCountForHotPlug > MAX_RETRY_COUNT) {
            logd("handleSwitchModeDelay has retry 150 times, no try again");
            this.mRetryCountForHotPlug = 0;
            return;
        }
        Message msg = this.mVSimEventHandler.obtainMessage();
        msg.what = 1000;
        this.mVSimEventHandler.sendMessageDelayed(msg, HwVSimConstants.GET_MODEM_SUPPORT_VERSION_INTERVAL);
    }

    public IccCardConstants.State modifySimStateForVsim(int phoneId, IccCardConstants.State s) {
        if (HwVSimUtilsInner.isPlatformTwoModemsActual()) {
            boolean isSubOnM2 = isSubOnM2(phoneId);
            logd("modifySimStateForVsim : phoneid = " + phoneId + ", sub on M2 is " + isSubOnM2);
            if (isSubOnM2) {
                logd("modifySimStateForVsim  : State = " + s + " to ABSENT.");
                return IccCardConstants.State.ABSENT;
            }
        }
        return s;
    }

    public boolean needBlockUnReservedForVsim(int subId) {
        if (HwVSimUtilsInner.isPlatformTwoModemsActual()) {
            boolean isSubOnM2 = isSubOnM2(subId);
            logd("needBlockForVsim : phoneid = " + subId + ", sub on M2 is " + isSubOnM2);
            if (isSubOnM2) {
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
        long cardLoadTimeout;
        logd("set mNeedSimLoadedMark[" + subId + "] = " + value);
        boolean oldAllFalse = true;
        for (int i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
            if (isPlatformTwoModems() && !HwVSimUtilsInner.isRadioAvailable(i)) {
                logi("setMarkForCardReload skip pending sub" + i);
            } else if (this.mNeedSimLoadedMark[i]) {
                oldAllFalse = false;
            }
        }
        logd("oldAllFalse = " + oldAllFalse);
        this.mNeedSimLoadedMark[subId] = value;
        boolean newAllFalse = true;
        for (int i2 = 0; i2 < HwVSimModemAdapter.MAX_SUB_COUNT; i2++) {
            if (isPlatformTwoModems() && !HwVSimUtilsInner.isRadioAvailable(i2)) {
                logi("setMarkForCardReload skip pending sub" + i2);
            } else if (this.mNeedSimLoadedMark[i2]) {
                newAllFalse = false;
            }
        }
        logd("newAllFalse = " + newAllFalse);
        if ((oldAllFalse && !newAllFalse) || (!oldAllFalse && newAllFalse)) {
            setVSimCauseCardReload(value);
            if (getHandler() != null) {
                getHandler().removeMessages(61);
                if (value) {
                    if (isPlatformTwoModems()) {
                        cardLoadTimeout = 120000;
                    } else if (HwVSimUtilsInner.isDualImsSupported()) {
                        cardLoadTimeout = HwVSimConstants.CARD_RELOAD_TIMEOUT_FOR_TRI_MODEM_DUAL_IMS;
                    } else {
                        cardLoadTimeout = HwVSimConstants.CARD_RELOAD_TIMEOUT_FOR_TRI_MODEM;
                    }
                    getHandler().sendEmptyMessageDelayed(61, cardLoadTimeout);
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
        logd("savedNetworkMode = " + savedNetworkMode + " for modemId = " + modemId);
        if (savedNetworkMode == -1) {
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

    public HwVSimSlotSwitchController.CommrilMode getCommrilMode() {
        if (this.mVSimSlotSwitchController == null) {
            return (HwVSimSlotSwitchController.CommrilMode) Enum.valueOf(HwVSimSlotSwitchController.CommrilMode.class, "CLG_MODE");
        }
        return this.mVSimSlotSwitchController.getCommrilMode();
    }

    public HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode(int mainSlot, int[] cardType) {
        if (this.mVSimSlotSwitchController == null) {
            return HwVSimSlotSwitchController.CommrilMode.NON_MODE;
        }
        return this.mVSimSlotSwitchController.getExpectCommrilMode(mainSlot, cardType);
    }

    public void switchCommrilMode(HwVSimSlotSwitchController.CommrilMode expectCommrilMode, int expectSlot, int mainSlot, boolean isVSimOn, Message onCompleteMsg) {
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

    public int getSimSlotTableLastSlotId() {
        if (this.mSimSlotsTable != null) {
            return this.mSimSlotsTable[this.mSimSlotsTable.length - 1];
        }
        return -1;
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
        boolean z = false;
        if (operation != 5 || !isVSimEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("canProcessEnable defaultstate:");
            sb.append(getCurrentState() == this.mDefaultState);
            logd(sb.toString());
            if (getCurrentState() == this.mDefaultState) {
                z = true;
            }
            return z;
        }
        logd("canProcessEnable vsim is on, can not process offline request");
        return false;
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
        return getCurrentState() == this.mDefaultState || getCurrentState() == this.mSmReadyState || getCurrentState() == this.mEReadyState;
    }

    public boolean canProcessRestartRild() {
        return getCurrentState() == this.mDefaultState && !isWaitingSwitchSimSlot();
    }

    private boolean isWaitingSwitchSimSlot() {
        if (!isVSimEnabled() && isPlatformTwoModems()) {
            return this.mIsWaitingSwitchSimSlot;
        }
        return false;
    }

    public void setIsWaitingSwitchSimSlot(boolean mark) {
        logd("setIsWaitingSwitchSimSlot, mark = " + mark);
        this.mIsWaitingSwitchSimSlot = mark;
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
        this.mVSimCi.registerForAvailable(h, what, 2);
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

    public EnableParam getEnableParam(HwVSimRequest request) {
        if (request == null) {
            return null;
        }
        Object arg = request.getArgument();
        EnableParam param = null;
        if (arg != null && (arg instanceof EnableParam)) {
            param = (EnableParam) arg;
        }
        return param;
    }

    public WorkModeParam getWorkModeParam(HwVSimRequest request) {
        if (request == null) {
            return null;
        }
        Object arg = request.getArgument();
        WorkModeParam param = null;
        if (arg != null) {
            param = (WorkModeParam) arg;
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
        if (this.mIsVSimOn) {
            logd("vsim is on, no need to restore default data");
            return;
        }
        setInteralDataForDSDS(0);
        setInteralDataForDSDS(1);
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
        this.mVSimPhone.updateDataConnectionTracker();
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
        if (this.mAlternativeUserReservedSubId != -1) {
            return HwVSimUtilsInner.getAnotherSlotId(this.mAlternativeUserReservedSubId);
        }
        if (reservedSub != -1) {
            return HwVSimUtilsInner.getAnotherSlotId(reservedSub);
        }
        return -1;
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
                for (NetworkInfo state : info) {
                    if (state.getState() == NetworkInfo.State.CONNECTED) {
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

    public int updateUiccCardCount() {
        int i = 0;
        this.mInsertedSimCount = 0;
        while (true) {
            if (i >= HwVSimModemAdapter.PHONE_COUNT) {
                break;
            }
            UiccCard card = this.mUiccController.getUiccCard(i);
            if (card == null) {
                logd("uicc card " + i + " not ready");
                break;
            }
            logd("updateUiccCardCount cardstate = " + card.getCardState() + " i = " + i);
            if (card.getCardState() == IccCardStatus.CardState.CARDSTATE_PRESENT) {
                this.mInsertedSimCount++;
            }
            i++;
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

    private int enableVSim(String imsi, int cardType, int apnType, String acqorder, String tapath, int vsimloc, String challenge, int operation, int cardInModem1) {
        StringBuilder sb = new StringBuilder();
        sb.append("enableVSim cardType: ");
        int i = cardType;
        sb.append(i);
        sb.append(" apnType: ");
        int i2 = apnType;
        sb.append(i2);
        sb.append(" acqorder: ");
        String str = acqorder;
        sb.append(str);
        sb.append(" cardInModem1:");
        int i3 = cardInModem1;
        sb.append(i3);
        logd(sb.toString());
        cmdSem_acquire();
        logd("enableVSim subId = " + 2);
        HwVSimPhoneFactory.setVSimEnabledSubId(2);
        EnableParam param = new EnableParam(imsi, i, i2, str, challenge, operation, tapath, vsimloc, i3);
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

    /* access modifiers changed from: private */
    public void enableEnterReport() {
        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mEventInfo, 1);
        if (getCardTypeFromEnableParam(this.mEnableRequest) == 1) {
            HwVSimEventReport.VSimEventInfoUtils.setCardType(this.mEventInfo, 1);
        } else if (getCardTypeFromEnableParam(this.mEnableRequest) == 2) {
            HwVSimEventReport.VSimEventInfoUtils.setCardType(this.mEventInfo, 2);
        } else {
            HwVSimEventReport.VSimEventInfoUtils.setCardType(this.mEventInfo, -1);
        }
        this.mVSimEnterTime = SystemClock.elapsedRealtime();
        logi("vsim enter time is " + this.mVSimEnterTime);
        HwVSimEventReport.VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
    }

    /* access modifiers changed from: private */
    public void enableExitReport() {
        if (this.mEnableRequest != null) {
            int iResult = 3;
            Object oResult = this.mEnableRequest.getResult();
            if (oResult != null) {
                iResult = ((Integer) oResult).intValue();
            }
            int i = 1;
            if (iResult == 0) {
                HwVSimEventReport.VSimEventInfoUtils.setResultType(this.mEventInfo, 1);
                HwVSimEventReport.VSimEventInfoUtils.setSimOperator(this.mEventInfo, getOperatorNumeric());
                HwVSimEventReport.VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
                HwVSimEventReport.VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, -1);
                HwVSimEventReport.VSimEventInfoUtils.setSimMode(this.mEventInfo, -1);
                HwVSimEventReport.VSimEventInfoUtils.setSlotsTable(this.mEventInfo, -1);
                HwVSimEventReport.VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, -1);
                HwVSimEventReport.VSimEventInfoUtils.setCardPresent(this.mEventInfo, -1);
                HwVSimEventReport.VSimEventInfoUtils.setWorkMode(this.mEventInfo, -1);
            } else {
                HwVSimEventReport.VSimEventInfoUtils.setResultType(this.mEventInfo, 2);
                HwVSimEventReport.VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
                HwVSimEventReport.VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
                HwVSimEventReport.VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
                HwVSimEventReport.VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, HwVSimPhoneFactory.getVSimSavedMainSlot());
                HwVSimEventReport.VSimEventInfo vSimEventInfo = this.mEventInfo;
                if (this.mIsVSimOn) {
                    i = 11;
                }
                HwVSimEventReport.VSimEventInfoUtils.setSimMode(vSimEventInfo, i);
                HwVSimEventReport.VSimEventInfoUtils.setSlotsTable(this.mEventInfo, getSlotsTableNumeric(this.mSimSlotsTable));
                HwVSimEventReport.VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, HwVSimPhoneFactory.getVSimSavedNetworkMode(0));
                HwVSimEventReport.VSimEventInfoUtils.setWorkMode(this.mEventInfo, getWorkMode());
            }
        }
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    /* access modifiers changed from: private */
    public void disableEnterReport() {
        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mEventInfo, 11);
    }

    /* access modifiers changed from: private */
    public void disableExitReport() {
        if (this.mDisableRequest != null) {
            boolean iResult = false;
            Object oResult = this.mDisableRequest.getResult();
            if (oResult != null) {
                iResult = ((Boolean) oResult).booleanValue();
            }
            if (iResult) {
                HwVSimEventReport.VSimEventInfoUtils.setResultType(this.mEventInfo, 1);
            } else {
                HwVSimEventReport.VSimEventInfoUtils.setResultType(this.mEventInfo, 2);
            }
        }
        HwVSimEventReport.VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
        HwVSimEventReport.VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setSimMode(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setSlotsTable(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setCardPresent(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setWorkMode(this.mEventInfo, -1);
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    /* access modifiers changed from: private */
    public void switchEnterReport() {
        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mEventInfo, 1);
        HwVSimEventReport.VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
        this.mVSimEnterTime = SystemClock.elapsedRealtime();
        logi("vsim enter time is " + this.mVSimEnterTime);
        HwVSimEventReport.VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
    }

    /* access modifiers changed from: private */
    public void switchExitReport() {
        if (this.mSwitchModeRequest != null) {
            boolean iResult = false;
            Object oResult = this.mSwitchModeRequest.getResult();
            if (oResult != null) {
                iResult = ((Boolean) oResult).booleanValue();
            }
            if (iResult) {
                HwVSimEventReport.VSimEventInfoUtils.setResultType(this.mEventInfo, 1);
            } else {
                HwVSimEventReport.VSimEventInfoUtils.setResultType(this.mEventInfo, 2);
            }
            HwVSimEventReport.VSimEventInfoUtils.setSimOperator(this.mEventInfo, getOperatorNumeric());
            HwVSimEventReport.VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setSimMode(this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setSlotsTable(this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setCardPresent(this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setWorkMode(this.mEventInfo, -1);
        }
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void setApnReport(int result) {
        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mEventInfo, 1);
        if (result == 0) {
            HwVSimEventReport.VSimEventInfoUtils.setResultType(this.mEventInfo, 1);
        } else {
            HwVSimEventReport.VSimEventInfoUtils.setResultType(this.mEventInfo, 2);
        }
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mEventInfo, 1);
        HwVSimEventReport.VSimEventInfoUtils.setCardType(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setSimOperator(this.mEventInfo, "");
        HwVSimEventReport.VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setSimMode(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setSlotsTable(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setCardPresent(this.mEventInfo, -1);
        HwVSimEventReport.VSimEventInfoUtils.setWorkMode(this.mEventInfo, -1);
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    private void saveVSimWorkMode(int workMode) {
        switch (workMode) {
            case 0:
                setUserReservedSubId(0);
                return;
            case 1:
                setUserReservedSubId(1);
                return;
            default:
                return;
        }
    }

    private void reportEvent(HwVSimEventReport eventReport, HwVSimEventReport.VSimEventInfo eventInfo) {
        if (SystemClock.elapsedRealtime() - this.mLastReportTime < HwVSimConstants.WAIT_FOR_SIM_STATUS_CHANGED_UNSOL_TIMEOUT) {
            logi("too short, last report time is " + this.mLastReportTime);
            return;
        }
        if (eventReport != null) {
            eventReport.reportEvent(this.mEventInfo);
            this.mLastReportTime = SystemClock.elapsedRealtime();
        }
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mEventInfo, -1);
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
        int mainSlot;
        if (slotsTable == null) {
            return -1;
        }
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

    /* access modifiers changed from: private */
    public int getWorkMode() {
        int reservedSubId = getUserReservedSubId();
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
        List<ActivityManager.RunningAppProcessInfo> appProcessList = am.getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == callingPid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private void initWhatToStringMap() {
        this.mWhatToStringMap = new HashMap<>();
        this.mWhatToStringMap.put(2, "EVENT_GET_SIM_STATE_DONE");
        this.mWhatToStringMap.put(5, "EVENT_SLOTSWITCH_INIT_DONE");
        this.mWhatToStringMap.put(40, "CMD_ENABLE_VSIM");
        this.mWhatToStringMap.put(41, "EVENT_RADIO_POWER_OFF_DONE");
        this.mWhatToStringMap.put(42, "EVENT_CARD_POWER_OFF_DONE");
        this.mWhatToStringMap.put(43, "EVENT_SWITCH_SLOT_DONE");
        this.mWhatToStringMap.put(45, "EVENT_CARD_POWER_ON_DONE");
        this.mWhatToStringMap.put(46, "EVENT_RADIO_POWER_ON_DONE");
        this.mWhatToStringMap.put(47, "EVENT_SET_ACTIVE_MODEM_MODE_DONE");
        this.mWhatToStringMap.put(48, "EVENT_GET_PREFERRED_NETWORK_TYPE_DONE");
        this.mWhatToStringMap.put(49, "EVENT_SET_PREFERRED_NETWORK_TYPE_DONE");
        this.mWhatToStringMap.put(50, "EVENT_NETWORK_CONNECTED");
        this.mWhatToStringMap.put(51, "EVENT_ENABLE_VSIM_DONE");
        this.mWhatToStringMap.put(10, "CMD_GET_RESERVED_PLMN");
        this.mWhatToStringMap.put(16, "CMD_SET_APDSFLOWCFG");
        this.mWhatToStringMap.put(54, "EVENT_GET_SIM_SLOT_DONE");
        this.mWhatToStringMap.put(55, "EVENT_SWITCH_COMMRIL_DONE");
        this.mWhatToStringMap.put(56, "EVENT_QUERY_CARD_TYPE_DONE");
        this.mWhatToStringMap.put(57, "EVENT_ENABLE_VSIM_FINISH");
        this.mWhatToStringMap.put(65, "EVENT_VSIM_PLMN_SELINFO");
        this.mWhatToStringMap.put(66, "EVENT_SET_NETWORK_RAT_AND_SRVDOMAIN_DONE");
        this.mWhatToStringMap.put(71, "EVENT_NETWORK_CONNECT_TIMEOUT");
        this.mWhatToStringMap.put(72, "EVENT_CMD_INTERRUPT");
        this.mWhatToStringMap.put(3, "EVENT_ICC_CHANGED");
        this.mWhatToStringMap.put(17, "EVENT_SET_APDSFLOWCFG_DONE");
        this.mWhatToStringMap.put(18, "CMD_SET_DSFLOWNVCFG");
        this.mWhatToStringMap.put(19, "EVENT_SET_DSFLOWNVCFG_DONE");
        this.mWhatToStringMap.put(12, "CMD_CLEAR_TRAFFICDATA");
        this.mWhatToStringMap.put(13, "EVENT_CLEAR_TRAFFICDATA_DONE");
        this.mWhatToStringMap.put(14, "CMD_GET_TRAFFICDATA");
        this.mWhatToStringMap.put(15, "EVENT_GET_TRAFFICDATA_DONE");
        this.mWhatToStringMap.put(58, "CMD_SWITCH_WORKMODE");
        this.mWhatToStringMap.put(11, "EVENT_GET_RESERVED_PLMN_DONE");
        this.mWhatToStringMap.put(52, "CMD_DISABLE_VSIM");
        this.mWhatToStringMap.put(53, "EVENT_DISABLE_VSIM_DONE");
        this.mWhatToStringMap.put(59, "EVENT_SWITCH_WORKMODE_DONE");
        this.mWhatToStringMap.put(60, "EVENT_SWITCH_WORKMODE_FINISH");
        this.mWhatToStringMap.put(61, "EVENT_CARD_RELOAD_TIMEOUT");
        this.mWhatToStringMap.put(73, "EVENT_GET_MODEM_SUPPORT_VSIM_VER_DONE");
        this.mWhatToStringMap.put(24, "EVENT_NETWORK_SCAN_COMPLETED");
        this.mWhatToStringMap.put(74, "EVENT_DSDS_AUTO_SETMODEM_DONE");
        this.mWhatToStringMap.put(75, "EVENT_INITIAL_TIMEOUT");
        this.mWhatToStringMap.put(76, "EVENT_INITIAL_SUBSTATE_DONE");
        this.mWhatToStringMap.put(77, "EVENT_INITIAL_UPDATE_CARDTYPE");
        this.mWhatToStringMap.put(29, "CMD_GET_MODEMSUPPORTVSIMVER_INNER");
        this.mWhatToStringMap.put(30, "EVENT_GET_MODEMSUPPORTVSIMVER_INNER_DONE");
        this.mWhatToStringMap.put(78, "EVENT_INITIAL_GET_MODEM_SUPPORT_VSIM_VER");
        this.mWhatToStringMap.put(79, "EVENT_GET_ICC_STATUS_DONE");
        this.mWhatToStringMap.put(80, "EVENT_SET_CDMA_MODE_SIDE_DONE");
        this.mWhatToStringMap.put(81, "EVENT_JUDGE_RESTART_RILD_NV_MATCH");
        this.mWhatToStringMap.put(82, "EVENT_JUDGE_RESTART_RILD_NV_MATCH_TIMEOUT");
        this.mWhatToStringMap.put(83, "EVENT_RADIO_AVAILABLE");
        this.mWhatToStringMap.put(84, "CMD_RESTART_RILD_FOR_NV_MATCH");
        this.mWhatToStringMap.put(22, "CMD_GET_SIM_STATE_VIA_SYSINFOEX");
        this.mWhatToStringMap.put(23, "EVENT_GET_SIM_STATE_VIA_SYSINFOEX");
        this.mWhatToStringMap.put(85, "EVENT_GET_ICC_STATUS_DONE_FOR_GET_CARD_COUNT");
        this.mWhatToStringMap.put(86, "EVENT_ICC_STATUS_CHANGED_FOR_CARD_COUNT");
        this.mWhatToStringMap.put(87, "EVENT_ICC_STATUS_CHANGED_FOR_CARD_COUNT_TIMEOUT");
    }

    private boolean cmdSem_acquire() {
        boolean acquired;
        if (this.mCmdSem == null) {
            return false;
        }
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
            logd("cmd sem not acquired");
            acquired = false;
        } catch (Throwable th) {
            throw th;
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
                while (true) {
                    if (SystemClock.elapsedRealtime() >= endTime) {
                        break;
                    } else if (HwVSimController.this.canProcessDisable()) {
                        HwVSimController.this.logd("vsim idle");
                        break;
                    } else {
                        SystemClock.sleep(500);
                    }
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
        StringBuilder sb = new StringBuilder();
        sb.append("subId = ");
        sb.append(subId);
        logi(sb.toString());
        if (subId == -1) {
            return 3;
        }
        int result = writeApnToTA(imsi, cardType, apnType, tapath, challenge, isForHash);
        if (result == 0) {
            result = ((Integer) sendRequest(20, null, subId)).intValue();
        }
        this.mVSimPhone.setUserDataEnabled(false);
        this.mVSimPhone.setUserDataEnabled(true);
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
        boolean[] isCardPresent = new boolean[cardCount];
        for (int i = 0; i < cardCount; i++) {
            if (cardTypes[i] == 0) {
                isCardPresent[i] = false;
            } else {
                isCardPresent[i] = true;
            }
        }
        for (int i2 = 0; i2 < cardCount; i2++) {
            if (isSubOnM2(i2) && isCardPresent[i2]) {
                return true;
            }
        }
        return false;
    }

    private boolean needSwitchModeHotplug(int[] cardTypes) {
        if (HwVSimUtilsInner.isChinaTelecom()) {
            return needSwitchModeHotplugForTelecom(cardTypes);
        }
        if (!HwVSimUtilsInner.isFullNetworkSupported()) {
            logd("needSwitchModeHotplug false CSIM on m1");
            return false;
        } else if (isPlatformTwoModems() || isVSimInProcess() || !isVSimEnabled()) {
            return false;
        } else {
            int mainSlot = getMainSlot(this.mSimSlotsTable);
            int oldInsertedCardCount = getInsertedCardCount();
            int newInsertedCardCount = getInsertedCardCount(cardTypes);
            int slaveSlot = mainSlot == 0 ? 1 : 0;
            int reservedSub = getUserReservedSubId();
            if (newInsertedCardCount == 2 && reservedSub != -1) {
                int noReservedSub = reservedSub == 0 ? 1 : 0;
                logd("needSwitchModeHotplug: noReservedSub = " + noReservedSub + " set to no sim");
                cardTypes[noReservedSub] = 0;
            }
            HwVSimSlotSwitchController.CommrilMode expect = getExpectCommrilMode(mainSlot, cardTypes);
            if (expect == HwVSimSlotSwitchController.CommrilMode.NON_MODE) {
                return false;
            }
            HwVSimSlotSwitchController.CommrilMode current = getCommrilMode();
            logd("needSwitchModeHotplug: oldInsertedCardCount = " + oldInsertedCardCount + ", newInsertedCardCount = " + newInsertedCardCount);
            logd("needSwitchModeHotplug: expect = " + expect + ", current = " + current);
            logd("needSwitchModeHotplug: mainSlot = " + mainSlot + ", slaveSlot = " + slaveSlot);
            logd("needSwitchModeHotplug: cardTypes[0] = " + cardTypes[0] + "cardTypes[1] = " + cardTypes[1]);
            boolean singleCardOnM2AndNoSimOnM1 = cardTypes[mainSlot] != 0 && cardTypes[slaveSlot] == 0;
            if (checkIfHasDualCdmaCard(cardTypes, expect, mainSlot)) {
                return false;
            }
            if (checkIfHasCdmaCardInDualImsSupport(cardTypes, current, mainSlot) || checkIfHasCdmaCard(cardTypes, expect, mainSlot)) {
                return true;
            }
            if (expect != current && !HwVSimUtilsInner.isDualImsSupported()) {
                return true;
            }
            if (!singleCardOnM2AndNoSimOnM1) {
                return false;
            }
            logd("needSwitchModeHotplug: singleCardOnM2AndNoSimOnM1 return true.");
            return true;
        }
    }

    private boolean needSwitchModeHotplugForTelecom(int[] cardTypes) {
        int mainSlot = getMainSlot(this.mSimSlotsTable);
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        logd("needSwitchModeHotplugForTelecom:slaveSlot=" + slaveSlot + " mainSlot =" + mainSlot + " cardTypes[0] = " + cardTypes[0] + " cardTypes[1] = " + cardTypes[1]);
        boolean singleGsmOnM1OrCdmaOnM2 = (cardTypes[slaveSlot] == 1 && cardTypes[mainSlot] != 1) || !(cardTypes[slaveSlot] == 2 || cardTypes[slaveSlot] == 3 || (cardTypes[mainSlot] != 2 && cardTypes[mainSlot] != 3));
        if (HwVSimUtilsInner.isPlatformTwoModemsActual()) {
            int reservedSub = getUserReservedSubId();
            if (getInsertedCardCount(cardTypes) == 2 && reservedSub != -1) {
                int noReservedSub = reservedSub == 0 ? 1 : 0;
                logd("needSwitchModeHotplugForTelecom: noReservedSub = " + noReservedSub + " set to no sim");
                cardTypes[noReservedSub] = 0;
            }
            HwVSimSlotSwitchController.CommrilMode expect = getExpectCommrilMode(mainSlot, cardTypes);
            if (expect == HwVSimSlotSwitchController.CommrilMode.NON_MODE) {
                return false;
            }
            HwVSimSlotSwitchController.CommrilMode current = getCommrilMode();
            logd("needSwitchModeHotplugForTelecom: expect = " + expect + ", current = " + current);
            return checkIfHasCdmaCardForTelecom(cardTypes, expect, mainSlot) || expect != current;
        } else if (singleGsmOnM1OrCdmaOnM2) {
            return true;
        }
    }

    private boolean needSwitchModeHotplugForCMCC(int[] cardTypes) {
        int cmccCardIndex = 0;
        if (!HwVSimUtilsInner.IS_CMCC_4GSWITCH_DISABLE) {
            return false;
        }
        int insertCardCount = getInsertedCardCount(cardTypes);
        logd("needSwitchModeHotplugForCMCC: insertCardCount = " + insertCardCount);
        boolean result = false;
        if (insertCardCount == 2 && HwFullNetworkManager.getInstance().isCMCCHybird()) {
            if (!HwFullNetworkManager.getInstance().isCMCCCardBySlotId(0)) {
                cmccCardIndex = 1;
            }
            if (getSubState(cmccCardIndex) != 0) {
                result = true;
            }
            logd("needSwitchModeHotplugForCMCC: hybird, result = " + result);
        }
        return result;
    }

    private void processNoNeedSwitchModeForCmcc(int[] cardTypes) {
        if (HwVSimUtilsInner.IS_CMCC_4GSWITCH_DISABLE) {
            if (!isVSimEnabled()) {
                logd("processNoNeedSwitchModeForCmcc: vsim is off, return.");
                return;
            }
            int insertCardCount = getInsertedCardCount(cardTypes);
            logd("processNoNeedSwitchModeForCmcc: insertCardCount = " + insertCardCount);
            if (insertCardCount > 0 && !isDisableProcess()) {
                int slotInModem1 = HwVSimUtilsInner.getAnotherSlotId(getMainSlot(this.mSimSlotsTable));
                int networkTypeInModem1 = HwVSimUtilsInner.getNetworkTypeInModem1ForCmcc(this.mModemAdapter.restoreSavedNetworkMode(slotInModem1));
                logd("processNoNeedSwitchModeForCmcc: slotInModem1 = " + slotInModem1 + ", mode = " + networkTypeInModem1);
                this.mModemAdapter.setPreferredNetworkType(null, null, slotInModem1, networkTypeInModem1);
            }
        }
    }

    private boolean checkIfHasCdmaCardInDualImsSupport(int[] cardTypes, HwVSimSlotSwitchController.CommrilMode current, int mainSlot) {
        boolean z = false;
        if (!HwVSimUtilsInner.isDualImsSupported()) {
            return false;
        }
        HwVSimSlotSwitchController.CommrilMode vimOnCommrilMode = this.mVSimSlotSwitchController.getVSimOnCommrilMode(mainSlot, cardTypes);
        logd("needSwitchModeHotplug: vimOnCommrilMode = " + vimOnCommrilMode);
        if (current != vimOnCommrilMode) {
            z = true;
        }
        return z;
    }

    private boolean checkIfHasCdmaCard(int[] cardTypes, HwVSimSlotSwitchController.CommrilMode expect, int mainSlot) {
        if (HwVSimUtilsInner.isDualImsSupported()) {
            return false;
        }
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        if (HwVSimSlotSwitchController.CommrilMode.isCLGMode(expect, cardTypes, mainSlot) || HwVSimSlotSwitchController.CommrilMode.isCGMode(expect, cardTypes, mainSlot)) {
            if (getCommrilMode() == HwVSimSlotSwitchController.CommrilMode.getULGMode()) {
                logd("checkIfHasCdmaCard: ULG to CG");
                return true;
            } else if (!HwVSimSlotSwitchController.isCDMACard(cardTypes[slaveSlot]) && HwVSimSlotSwitchController.isCDMACard(cardTypes[mainSlot])) {
                logd("checkIfHasCdmaCard: cdma card match to c-modem");
                return true;
            }
        }
        return false;
    }

    private boolean checkIfHasCdmaCardForTelecom(int[] cardTypes, HwVSimSlotSwitchController.CommrilMode expect, int mainSlot) {
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        if (HwVSimSlotSwitchController.CommrilMode.isCLGMode(expect, cardTypes, mainSlot) || HwVSimSlotSwitchController.CommrilMode.isCGMode(expect, cardTypes, mainSlot)) {
            if (getCommrilMode() == HwVSimSlotSwitchController.CommrilMode.getULGMode()) {
                logd("checkIfHasCdmaCardForTelecom: ULG to CG");
                return true;
            } else if ((cardTypes[slaveSlot] == 0 || cardTypes[slaveSlot] == 1) && (cardTypes[mainSlot] == 2 || cardTypes[mainSlot] == 3)) {
                logd("checkIfHasCdmaCardForTelecom for telecom: cdma card match to c-modem");
                return true;
            }
        }
        return false;
    }

    private boolean checkIfHasDualCdmaCard(int[] cardTypes, HwVSimSlotSwitchController.CommrilMode expect, int mainSlot) {
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        if (!HwVSimSlotSwitchController.CommrilMode.isCLGMode(expect, cardTypes, mainSlot) || getCommrilMode() != HwVSimSlotSwitchController.CommrilMode.getCGMode() || !HwVSimSlotSwitchController.isCDMACard(cardTypes[slaveSlot]) || !HwVSimSlotSwitchController.isCDMACard(cardTypes[mainSlot])) {
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

    public boolean isPinBlocked(int subId) {
        logd("isPinBlocked, subId = " + subId + ", mBlockPinFlag = " + this.mBlockPinFlag + ", mBlockPinTable = " + Arrays.toString(this.mBlockPinTable) + ", mPinBlockedTable = " + Arrays.toString(this.mPinBlockedTable));
        if (subId < 0 || subId >= HwVSimModemAdapter.PHONE_COUNT || !this.mBlockPinFlag || !this.mBlockPinTable[subId]) {
            return false;
        }
        return this.mPinBlockedTable[subId];
    }

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
                isCardPresent = card.getCardState() == IccCardStatus.CardState.CARDSTATE_PRESENT;
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
            isDataEnabled = this.mPhones[dataSubId].isDataEnabled();
            allowData(dataSubId);
        }
        if (isDataEnabled) {
            Message msg = this.mMainHandler.obtainMessage();
            msg.what = 2;
            msg.arg1 = dataSubId;
            this.mMainHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public void onSetupDataOnVSimEnded(int phoneId) {
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

    /* access modifiers changed from: private */
    public void onUpdateUserPreferences() {
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

    /* access modifiers changed from: private */
    public void onUpdateSubActivation() {
        logd("onUpdateSubActivation");
    }

    /* access modifiers changed from: private */
    public void onAutoSwitchSimSlot() {
        logd("onAutoSwitchSimSlot");
        if (HwVSimUtilsInner.isPlatformRealTripple() && HwFullNetworkConfig.IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT) {
            setSubActivationUpdate(false);
            HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(getAllowDataSlotId());
        }
        if (isPlatformTwoModems() != 0) {
            setSubActivationUpdate(false);
            int default4GSlotId = getAllowDataSlotId();
            if (default4GSlotId != HwTelephonyManagerInner.getDefault().getDefault4GSlotId()) {
                this.mNvMatchController.storeIfNeedRestartRildForNvMatch(false);
                HwFullNetworkManager.getInstance().setMainSlot(default4GSlotId, null);
            } else {
                HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(default4GSlotId);
            }
            HwFullNetworkManager.getInstance().saveMainCardIccId(HwFullNetworkManager.getInstance().getFullIccid(default4GSlotId));
            setIsWaitingSwitchSimSlot(false);
        }
        setSavedMainSlotAndCardCount(-1, -1);
    }

    /* access modifiers changed from: private */
    public void onAutoSetAllowData() {
        int allowDataSlotId = getAllowDataSlotId();
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
    }

    public void setSavedMainSlotAndCardCount(int savedMainSlot, int cardCount) {
        if (isPlatformTwoModems()) {
            logd("setSavedMainSlotAndCardCount, savedMainSlot: " + savedMainSlot + " , cardCount: " + cardCount);
            this.mVSimSavedMainSlot = savedMainSlot;
            this.mVSimCardCount = cardCount;
        }
    }

    private int getAllowDataSlotId() {
        int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            return default4GSlotId;
        }
        int allowDataSlotId = default4GSlotId;
        if (this.mVSimCardCount == 1) {
            allowDataSlotId = getSwitchActivatedSubId(default4GSlotId);
        } else if (this.mVSimCardCount == 2 && this.mVSimSavedMainSlot != -1) {
            int savedMainSlotSubState = getSubState(this.mVSimSavedMainSlot);
            int anotherSlot = HwVSimUtilsInner.getAnotherSlotId(this.mVSimSavedMainSlot);
            if (getSubState(anotherSlot) == 0 || savedMainSlotSubState != 0) {
                this.mVSimSavedMainSlot = HwFullNetworkManager.getInstance().getDefaultMainSlotByIccId(this.mVSimSavedMainSlot);
                allowDataSlotId = this.mModemAdapter.getExpectSlotForDisableForCmcc(this.mVSimSavedMainSlot);
            } else {
                allowDataSlotId = anotherSlot;
            }
        }
        logd("getAllowDataSlotId mVSimCardCount:" + this.mVSimCardCount + ", mVSimSavedMainSlot:" + this.mVSimSavedMainSlot + ", default4GSlotId:" + default4GSlotId + ", allowDataSlotId:" + allowDataSlotId);
        return allowDataSlotId;
    }

    private int getSwitchActivatedSubId(int oldActivatedSubId) {
        int activatedSubId = oldActivatedSubId;
        SubscriptionController subController = SubscriptionController.getInstance();
        if (subController == null) {
            logd("getSwitchActivatedSubId: sub controller not found subId = " + activatedSubId);
            return activatedSubId;
        }
        if (HwFullNetworkConfig.IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT) {
            boolean[] isSubActivated = new boolean[this.mCis.length];
            int i = 0;
            int activatedSubCount = 0;
            for (int i2 = 0; i2 < this.mCis.length; i2++) {
                if (subController.getSubState(subController.getSubId(i2)[0]) == 1) {
                    isSubActivated[i2] = true;
                    activatedSubCount++;
                } else {
                    isSubActivated[i2] = false;
                }
            }
            logd("getSwitchActivatedSubId: isSubActivated = " + Arrays.toString(isSubActivated));
            if (activatedSubCount == 1 && !isSubActivated[oldActivatedSubId]) {
                if (oldActivatedSubId == 0) {
                    i = 1;
                }
                activatedSubId = i;
            }
        }
        logd("getSwitchActivatedSubId: oldActivatedSubId = " + oldActivatedSubId + ", activatedSubId = " + activatedSubId);
        return activatedSubId;
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
                this.mMainHandler.sendMessageDelayed(this.mMainHandler.obtainMessage(6), HwVSimConstants.WAIT_FOR_SIM_STATUS_CHANGED_UNSOL_TIMEOUT);
            }
        }
    }

    public void clearRecoverMarkToFalseMessage() {
        logd("clearRecoverMarkToFalseMessage mIsVSimCauseCardReload" + this.mIsVSimCauseCardReload);
        if (HwVSimUtilsInner.isDualImsSupported() && !isPlatformTwoModems() && this.mIsVSimCauseCardReload && this.mMainHandler.hasMessages(6)) {
            logd("mIsVSimCauseCardReload is true, let it be false and delete message.");
            this.mIsVSimCauseCardReload = false;
            this.mMainHandler.removeMessages(6);
        }
    }

    /* access modifiers changed from: private */
    public void onRecoverMarkToFalse() {
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
        if (HwFullNetworkConfig.IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT) {
            return false;
        }
        logd("isSubActivationUpdate : " + this.mIsSubActivationUpdate);
        return this.mIsSubActivationUpdate;
    }

    public int convertSavedNetworkMode(int networkMode) {
        int convertedNetworkMode = networkMode;
        if (networkMode != 8 && networkMode != 63) {
            switch (networkMode) {
                case 4:
                case 5:
                case 6:
                    convertedNetworkMode = 3;
                    break;
            }
        } else {
            convertedNetworkMode = 9;
        }
        logd("networkMode : " + networkMode + ", convertedNetworkMode : " + convertedNetworkMode);
        return convertedNetworkMode;
    }

    public void updateSubState(int subId, int subState) {
        synchronized (sSubStateLock) {
            if (subId >= 0) {
                try {
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
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public int getSubState(int subId) {
        int subState;
        synchronized (sSubStateLock) {
            subState = 0;
            if (subId >= 0) {
                try {
                    if (subId < this.mSubStates.length) {
                        subState = this.mSubStates[subId];
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            logd("[SUB" + subId + "] getSubState : " + subState);
        }
        return subState;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0027, code lost:
        return;
     */
    public void updateSimCardTypes(int[] cardTypes) {
        synchronized (sSimCardTypesLock) {
            if (cardTypes == null) {
                try {
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                this.mSimCardTypes = (int[]) cardTypes.clone();
                if (isProcessInit()) {
                    Handler h = getHandler();
                    if (h != null) {
                        h.sendMessage(h.obtainMessage(77));
                    }
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
                try {
                    if (subId < this.mCheckSubActivated.length) {
                        logd("[SUB" + subId + "] set sub actived");
                        this.mCheckSubActivated[subId] = false;
                    }
                } catch (Throwable th) {
                    throw th;
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
                                try {
                                    if (i < this.mCheckSubActivated.length) {
                                        logd("[SUB" + i + "] sub state is inactive.");
                                        this.mCheckSubActivated[i] = true;
                                    }
                                } catch (Throwable th) {
                                    throw th;
                                }
                            }
                        }
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

    public synchronized void setIsWaitingNvMatchUnsol(boolean value) {
        this.mIsWaitingNvMatchUnsol = value;
        logd("set mIsWaitingNvMatchUnsol = " + this.mIsWaitingNvMatchUnsol);
    }

    public synchronized boolean getIsWaitingNvMatchUnsol() {
        return this.mIsWaitingNvMatchUnsol;
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
        return HwVSimPhoneFactory.getPendingDeviceInfoFromSP(prefKey, getSimSlotTableLastSlotId());
    }

    public void recoverSimMode() {
    }

    public void checkMmsStart(int subId) {
    }

    public void checkMmsStop(int subId) {
    }

    public boolean isM2CSOnly() {
        return false;
    }

    public boolean isMmsOnM2() {
        return false;
    }

    public boolean setVSimULOnlyMode(boolean isULOnly) {
        return true;
    }

    public boolean getVSimULOnlyMode() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void simHotPlugOut(int slotId) {
        if (isVSimEnabled()) {
            logd("simHotPlugOut, slotId: " + slotId);
            if (slotId >= 0 && slotId < HwVSimModemAdapter.MAX_SUB_COUNT) {
                setMarkForCardReload(slotId, false);
            }
            this.mModemAdapter.onSimHotPlugOut();
        }
    }

    /* access modifiers changed from: package-private */
    public void simHotPlugIn(int slotId) {
        logd("simHotPlugIn, slotId: " + slotId);
        if (HwVSimUtilsInner.IS_CMCC_4GSWITCH_DISABLE && isVSimEnabled()) {
            boolean dualLteCap = HwVSimUtilsInner.isDualImsSwitchOpened();
            int maxCap = this.mModemAdapter.getAllAbilityNetworkTypeOnModem1(dualLteCap);
            logd("simHotPlugIn,restore MODEM1 maxCap:" + maxCap + ",duallteCap:" + dualLteCap);
            HwVSimPhoneFactory.setVSimSavedNetworkMode(1, maxCap);
        }
    }

    /* access modifiers changed from: package-private */
    public void setAlternativeUserReservedSubId(int subId) {
        if (HwVSimUtilsInner.isPlatformTwoModemsActual()) {
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
