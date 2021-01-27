package com.android.internal.telephony.vsim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.HwTelephonyManagerInner;
import android.text.TextUtils;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
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
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwparttelephonyvsim.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import com.huawei.internal.telephony.vsim.HwVSimBaseController;
import com.huawei.internal.telephony.vsim.ServiceStateHandler;
import com.huawei.internal.telephony.vsim.util.ArrayUtils;
import com.huawei.internal.util.StateEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwVSimController extends HwVSimBaseController {
    private static final int CARDTYPE_MAIN = 1;
    private static final int CARDTYPE_SUB = 2;
    private static final int EVENT_AUTO_SET_ALLOW_DATA = 5;
    private static final int EVENT_AUTO_SWITCH_SLOT = 4;
    private static final int EVENT_RECOVER_MARK_TO_FALSE = 6;
    private static final int EVENT_SETUP_DATA_ON_VSIM_ENDED = 2;
    private static final int EVENT_UPDATE_SUB_ACTIVATION = 3;
    private static final int EVENT_UPDATE_USER_PREFERENCES = 1;
    private static final boolean HW_DBG = SystemPropertiesEx.getBoolean("ro.debuggable", false);
    private static final String LOG_TAG = "VSimController";
    private static final int MAX_RETRY_COUNT = 150;
    private static final int MAX_VSIM_WAIT_TIME = 90000;
    private static final int RECOVER_MARK_DELAY_TIME = 5000;
    private static final int REPORT_INTERVAL_MILLIS = 5000;
    private static final int SWITCH_DELAY_TIME = 2000;
    private static final int VSIM_MODEM_COUNT = SystemPropertiesEx.getInt("ro.radio.vsim_modem_count", 3);
    private static final int VSIM_MODEM_COUNT_DEFAULT = 3;
    private static final int VSIM_STATE_POLL_SLEEP_MSEC = 500;
    private static final Object sCheckSubActivedLock = new Object();
    private static HwVSimController sInstance = null;
    private static final Object sLock = new Object();
    private static final Object sSimCardTypesLock = new Object();
    private boolean isWaitingSwitchCdmaModeSide = false;
    private boolean mBlockPinFlag;
    private boolean[] mBlockPinTable;
    private boolean[] mCheckSubActivated;
    private Semaphore mCmdSem;
    private AtomicBoolean mCmdSemAcquired;
    private VSimDReadyState mDReadyState = new VSimDReadyState();
    private VSimDWorkState mDWorkState = new VSimDWorkState();
    private VSimDefaultState mDefaultState = new VSimDefaultState();
    private HwVSimRequest mDisableRequest;
    private VSimDisableState mDisableState = new VSimDisableState();
    private VSimEReadyState mEReadyState = new VSimEReadyState();
    private VSimEWorkState mEWorkState = new VSimEWorkState();
    private HwVSimRequest mEnableRequest;
    private VSimEnableState mEnableState = new VSimEnableState();
    private InitialState mInitialState = new InitialState();
    private int mInsertedSimCount = 0;
    private boolean mIsRegAirplaneModeReceiver;
    private boolean mIsRegNetworkReceiver;
    private boolean mIsSessionOpen = false;
    private boolean mIsSubActivationUpdate;
    private boolean mIsTaOpen = false;
    private boolean mIsVSimCauseCardReload;
    private boolean mIsVSimOnSuccess = false;
    private boolean mIsWaitingNvMatchUnsol = false;
    private boolean mIsWaitingSwitchSimSlot = false;
    private long mLastReportTime;
    private boolean[] mNeedSimLoadedMark;
    private final BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.vsim.HwVSimController.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (HwVSimController.this.isNetworkConnected()) {
                HwVSimController.this.getHandler().obtainMessage(50).sendToTarget();
            }
        }
    };
    private HwVSimNvMatchController mNvMatchController;
    private boolean[] mPinBlockedTable;
    private boolean[] mProhibitSubUpdateSimNoChange;
    private RestartRildState mRestartRildState = new RestartRildState();
    private int mRetryCountForHotPlug = 0;
    private ServiceStateHandler mServiceStateHandlerForVsim;
    private int[] mSimCardTypes;
    private VSimSwitchModeReadyState mSmReadyState = new VSimSwitchModeReadyState();
    private VSimSwitchModeWorkState mSmWorkState = new VSimSwitchModeWorkState();
    private HwVSimRequest mSwitchModeRequest;
    private VSimSwitchModeState mSwitchModeState = new VSimSwitchModeState();
    private UiccControllerExt mUiccController;
    private int mVSimCardCount = -1;
    private CommandsInterfaceEx mVSimCi;
    public long mVSimEnterTime;
    private HwVSimEventHandler mVSimEventHandler;
    private HwVSimEventReport mVSimEventReport;
    private HandlerThread mVSimEventThread;
    private boolean[] mVSimLockPower;
    private PhoneExt mVSimPhone;
    private int mVSimSavedMainSlot = -1;
    private HwVSimSlotSwitchController mVSimSlotSwitchController;
    private HwVSimUiccController mVSimUiccController;

    static /* synthetic */ int access$4208(HwVSimController x0) {
        int i = x0.mDisableRetryCount;
        x0.mDisableRetryCount = i + 1;
        return i;
    }

    private HwVSimController(Context context, PhoneExt vsimPhone, CommandsInterfaceEx vsimCi, PhoneExt[] phones, CommandsInterfaceEx[] cis) {
        super(context, phones, cis);
        HwVSimLog.VSimLogI(LOG_TAG, "HwVSimController");
        this.mMainHandler = new MyHandler();
        this.mCmdSem = new Semaphore(1);
        this.mCmdSemAcquired = new AtomicBoolean(false);
        this.mVSimPhone = vsimPhone;
        this.mVSimCi = vsimCi;
        this.mModemAdapter = createModemAdapter(context, vsimCi, cis);
        this.mNvMatchController = HwVSimNvMatchController.create(this);
        this.mVSimSlotSwitchController = HwVSimSlotSwitchController.getInstance();
        this.mVSimUiccController = HwVSimUiccController.getInstance();
        this.mUiccController = UiccControllerExt.getInstance();
        this.mIsRegNetworkReceiver = false;
        this.mVSimEnterTime = 0;
        this.mVSimEventThread = new HandlerThread("VSimEventThread");
        this.mVSimEventThread.start();
        this.mVSimEventHandler = new HwVSimEventHandler(this.mVSimEventThread.getLooper());
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
        this.mSimCardTypes = new int[HwVSimModemAdapter.PHONE_COUNT];
        this.mCheckSubActivated = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        this.mProhibitSubUpdateSimNoChange = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        for (int i3 = 0; i3 < HwVSimModemAdapter.PHONE_COUNT; i3++) {
            this.mSimCardTypes[i3] = -1;
            this.mCheckSubActivated[i3] = false;
            this.mProhibitSubUpdateSimNoChange[i3] = false;
        }
        this.mBlockPinFlag = false;
        this.mBlockPinTable = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        this.mPinBlockedTable = new boolean[HwVSimModemAdapter.PHONE_COUNT];
        this.mDisableFailMark = false;
        HwVSimLog.VSimLogI(LOG_TAG, "VSIM_MODEM_COUNT: " + VSIM_MODEM_COUNT);
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

    public static void create(Context context, PhoneExt vsimPhone, CommandsInterfaceEx vsimCi, PhoneExt[] phones, CommandsInterfaceEx[] cis) {
        slogi("create");
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new HwVSimController(context, vsimPhone, vsimCi, phones, cis);
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

    private static void slogi(String content) {
        HwVSimLog.info(LOG_TAG, content);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void logd(String content) {
        HwVSimLog.info(LOG_TAG, content);
    }

    private void addServiceStateChangeListener() {
        this.mServiceStateHandlerForVsim = new ServiceStateHandler(Looper.myLooper(), this.mVSimPhone);
        this.mVSimPhone.registerForServiceStateChanged(this.mServiceStateHandlerForVsim, (int) HwVSimConstants.EVENT_SERVICE_STATE_CHANGE, (Object) null);
    }

    public void unhandledMessage(Message msg) {
        logd(" - unhandledMessage: msg.what=" + msg.what);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (fd != null && pw != null && args != null) {
            super.dump(fd, pw, args);
        }
    }

    public int enableVSim(int operation, String imsi, int cardType, int apnType, String acqorder, String tapath, int vsimloc, String challenge) {
        logd("enableVSim, operation: " + operation + ", cardType: " + cardType + ", apnType: " + apnType + ", acqOrder: " + acqorder + ", vsimloc: " + vsimloc);
        if (operation == 1) {
            return enableVSim(imsi, cardType, apnType, acqorder, tapath, vsimloc, challenge, operation, -1, 0);
        }
        if (operation == 2) {
            return setApn(imsi, cardType, apnType, tapath, challenge, false, 0);
        }
        if (operation == 5) {
            return 3;
        }
        logd("enableVSim do nothing");
        return 3;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int enableVSim(HwVSimConstants.EnableParam param) {
        return enableVSim(param.imsi, param.cardType, param.apnType, param.acqOrder, param.taPath, param.vsimLoc, param.challenge, param.operation, param.cardInModem1, param.supportVsimCa);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int setApn(HwVSimConstants.ApnParams params) {
        return setApn(params.imsi, params.cardType, params.apnType, params.taPath, params.challenge, params.isForHash, params.supportVSimCa);
    }

    public boolean getVSimOnSuccess() {
        logd("getVSimOnSuccess " + this.mIsVSimOnSuccess);
        return this.mIsVSimOnSuccess;
    }

    public void setVSimOnSuccess(boolean isSuccess) {
        logd("setVSimOnSuccess to " + isSuccess);
        this.mIsVSimOnSuccess = isSuccess;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean disableVSim() {
        Handler h;
        logd("disableVSim");
        if (!isVSimEnabled()) {
            logi("VSIM is disabled already, disableVSim result = true!");
            return true;
        } else if (isProcessInit()) {
            logi("VSIM is initing, disableVSim result = false!");
            return false;
        } else {
            cmdSem_acquire();
            if (!canProcessDisable() && (h = getHandler()) != null) {
                h.sendEmptyMessage(72);
            }
            waitVSimIdle(90000);
            logd("disableVSim subId = 2");
            if (!isVSimEnabled()) {
                logi("Double Check: VSIM is disabled already, disableVSim result = true!");
                cmdSem_release();
                return true;
            }
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
                if (HwVSimUtilsInner.isSupportedVsimDynamicStartStop()) {
                    registerAirplaneModeReceiver();
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

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean switchVsimWorkMode(int workMode) {
        logd("switchVsimWorkMode : " + workMode);
        cmdSem_acquire();
        return switchVsimWorkMode(workMode, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean switchVsimWorkMode(int workMode, boolean isHotplug) {
        int oldMode = getWorkMode();
        saveVSimWorkMode(workMode);
        Object oResult = sendRequest(58, new HwVSimConstants.WorkModeParam(workMode, oldMode, isHotplug), 2);
        boolean iResult = false;
        if (oResult != null) {
            iResult = ((Boolean) oResult).booleanValue();
        }
        if (!iResult) {
            saveVSimWorkMode(oldMode);
        }
        logd("switchVsimWorkMode result = " + iResult);
        return iResult;
    }

    public boolean setUserReservedSubId(int subId) {
        logd("setUserReservedSubId, subId = " + subId);
        HwVSimPhoneFactory.setVSimUserReservedSubId(this.mContext, subId);
        return true;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public String getTrafficData() {
        logd("getTrafficData");
        int subId = getVsimSlotId();
        if (subId != 2) {
            logd("getTrafficData VSim not enabled");
            return null;
        }
        String callingAppName = getCallingAppName();
        if (TextUtils.isEmpty(callingAppName)) {
            return null;
        }
        if (callingAppName.startsWith(HwVSimConstants.VSIM_PKG_NAME_CHILD_THREAD) || callingAppName.equals(HwVSimConstants.VSIM_PKG_NAME)) {
            return Arrays.toString((String[]) sendRequest(14, null, subId));
        }
        logd("getTrafficData not allowed, calling app is " + callingAppName);
        return null;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean clearTrafficData() {
        logd("clearTrafficData");
        int slotId = getVsimSlotId();
        if (slotId != 2) {
            slotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            logi("clearTrafficData, VSim not enabled, to mainslot: " + slotId);
        }
        String callingAppName = getCallingAppName();
        if (TextUtils.isEmpty(callingAppName)) {
            return false;
        }
        if (callingAppName.startsWith(HwVSimConstants.VSIM_PKG_NAME_CHILD_THREAD) || callingAppName.equals(HwVSimConstants.VSIM_PKG_NAME)) {
            boolean result = ((Boolean) sendRequest(12, null, slotId)).booleanValue();
            logd("clearTrafficData result = " + result);
            return result;
        }
        logd("clearTrafficData not allowed, calling app is " + callingAppName);
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) {
        int subId = getVsimSlotId();
        logd("dsFlowCfg, subId = " + subId);
        if (subId != 2) {
            subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            logd("dsFlowCfg, VSim not enabled, to mainslot: " + subId);
        }
        String callingAppName = getCallingAppName();
        if (TextUtils.isEmpty(callingAppName)) {
            return false;
        }
        if (callingAppName.startsWith(HwVSimConstants.VSIM_PKG_NAME_CHILD_THREAD) || callingAppName.equals(HwVSimConstants.VSIM_PKG_NAME)) {
            boolean result = setApDsFlowCfg(subId, repFlag, threshold, totalThreshold, oper);
            if (!result || repFlag != 1) {
                setDsFlowNvCfg(subId, 0, 0);
            } else {
                setDsFlowNvCfg(subId, 1, 10);
            }
            return result;
        }
        logd("dsFlowCfg not allowed, calling app is " + callingAppName);
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int getSimStateViaSysinfoEx(int subId) {
        logd("getSimStateViaSysinfoEx");
        if (subId == 2) {
            return ((Integer) sendRequest(22, null, subId)).intValue();
        }
        logd("getSimStateViaSysinfoEx VSim not enabled");
        return -1;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public String getDevSubMode(int subId) {
        logd("getDevSubMode");
        if (subId == 2) {
            return (String) sendRequest(25, null, subId);
        }
        logd("getDevSubMode VSim not enabled");
        return null;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public String getPreferredNetworkTypeForVsim(int subId) {
        logd("getPreferredNetworkTypeForVsim");
        if (subId == 2) {
            return (String) sendRequest(27, null, subId);
        }
        logd("getPreferredNetworkTypeForVsim VSim not enabled");
        return null;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int dialupForVSim() {
        PhoneExt phoneExt;
        logd("dialupForVSim");
        if (!isVSimOn() || (phoneExt = this.mVSimPhone) == null || phoneExt.getDcTracker() == null) {
            return -1;
        }
        logd("dialupForVSim EVENT_TRY_SETUP_DATA");
        DcTrackerEx dcTracker = this.mVSimPhone.getDcTracker();
        dcTracker.sendMessage(dcTracker.obtainMessage(270339, "userDataEnabled"));
        return 0;
    }

    public boolean isSubOnM2(int subId) {
        return ArrayUtils.get(getSimSlotTable(), 2, -1) == subId;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void disposeCard(int index) {
        UiccControllerExt uiccControllerExt = this.mUiccController;
        if (uiccControllerExt != null) {
            uiccControllerExt.disposeCard(index);
        }
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
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
    /* access modifiers changed from: public */
    private void handleSwitchModeDelay() {
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
        this.mVSimEventHandler.sendMessageDelayed(msg, 2000);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public IccCardConstantsEx.StateEx modifySimStateForVsim(int phoneId, IccCardConstantsEx.StateEx s) {
        if (HwVSimUtilsInner.isPlatformTwoModemsActual()) {
            boolean isSubOnM2 = isSubOnM2(phoneId);
            logd("modifySimStateForVsim : phoneid = " + phoneId + ", sub on M2 is " + isSubOnM2);
            if (isSubOnM2) {
                logd("modifySimStateForVsim  : State = " + s + " to ABSENT.");
                return IccCardConstantsEx.StateEx.ABSENT;
            }
        }
        return s;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean needBlockUnReservedForVsim(int slotId) {
        if (!HwVSimUtilsInner.isPlatformTwoModemsActual()) {
            return false;
        }
        boolean isSubOnM2 = isSubOnM2(slotId);
        logd("needBlockForVsim : phoneid = " + slotId + ", sub on M2 is " + isSubOnM2);
        if (!isSubOnM2) {
            return false;
        }
        logd("needBlockForVsim : true");
        return true;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
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

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void setMarkForCardReload(int slotId, boolean value) {
        long cardLoadTimeout;
        logd("set mNeedSimLoadedMark[" + slotId + "] = " + value);
        boolean oldAllFalse = true;
        for (int i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
            if (isPlatformTwoModems() && !HwVSimUtilsInner.isRadioAvailable(i)) {
                logi("setMarkForCardReload skip pending sub" + i);
            } else if (this.mNeedSimLoadedMark[i]) {
                oldAllFalse = false;
            }
        }
        logd("oldAllFalse = " + oldAllFalse);
        this.mNeedSimLoadedMark[slotId] = value;
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
                        cardLoadTimeout = HwVSimConstants.CARD_RELOAD_TIMEOUT_FOR_DUAL_MODEM;
                    } else if (HwVSimUtilsInner.isDualImsSupported()) {
                        cardLoadTimeout = HwVSimConstants.CARD_RELOAD_TIMEOUT_FOR_TRI_MODEM_DUAL_IMS;
                    } else {
                        cardLoadTimeout = HwVSimConstants.CARD_RELOAD_TIMEOUT_FOR_TRI_MODEM;
                    }
                    getHandler().sendEmptyMessageDelayed(61, cardLoadTimeout);
                }
            }
        }
        logd("set mNeedSimLoadedMark[" + slotId + "] = " + value + " funtion end");
    }

    public void clearAllMarkForCardReload() {
        logd("clear all mark for mNeedSimLoadedMark");
        for (int i = 0; i < HwVSimModemAdapter.MAX_SUB_COUNT; i++) {
            this.mNeedSimLoadedMark[i] = false;
        }
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int scanVsimAvailableNetworks(int subId, int type) {
        if (this.mNetworkScanIsRunning == 1) {
            logd("scanVsimAvailableNetworks is running");
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

    public int writeVsimToTA(int operation, HwVSimConstants.EnableParam param) {
        logd("writeVsimToTA");
        if (param == null) {
            logd("invalid param");
            return 1;
        } else if (param.challenge != null && param.challenge.length() != 0) {
            return HwVSimOperateTA.getDefault().operateTA(operation, param.imsi, param.cardType, param.apnType, param.challenge, false, param.taPath, param.vsimLoc, 0);
        } else {
            logd("invalid param challenge");
            return 6;
        }
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
            case 9:
            case 10:
            default:
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
        }
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public CommandsInterfaceEx getCiBySub(int slotId) {
        return HwVSimUtilsInner.getCiBySub(slotId, this.mVSimCi, this.mCis);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public PhoneExt getPhoneBySub(int subId) {
        return HwVSimUtilsInner.getPhoneBySub(subId, this.mVSimPhone, this.mPhones);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public HwVSimSlotSwitchController.CommrilMode getCommrilMode() {
        HwVSimSlotSwitchController hwVSimSlotSwitchController = this.mVSimSlotSwitchController;
        if (hwVSimSlotSwitchController == null) {
            return (HwVSimSlotSwitchController.CommrilMode) Enum.valueOf(HwVSimSlotSwitchController.CommrilMode.class, "HISI_CGUL_MODE");
        }
        return hwVSimSlotSwitchController.getCommrilMode();
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode(int mainSlot, int[] cardType) {
        HwVSimSlotSwitchController hwVSimSlotSwitchController = this.mVSimSlotSwitchController;
        if (hwVSimSlotSwitchController == null) {
            return HwVSimSlotSwitchController.CommrilMode.NON_MODE;
        }
        return hwVSimSlotSwitchController.getExpectCommrilMode(mainSlot, cardType);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean getIsSessionOpen() {
        return this.mIsSessionOpen;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void setIsSessionOpen(boolean isOpen) {
        this.mIsSessionOpen = isOpen;
    }

    public boolean getIsTaOpen() {
        return this.mIsTaOpen;
    }

    public void setIsTaOpen(boolean isOpen) {
        this.mIsTaOpen = isOpen;
    }

    public void closeTaSafely(HwVSimRequest request) {
        if (request != null && getIsTaOpen()) {
            Object object = request.getArgument();
            if (object instanceof HwVSimConstants.EnableParam) {
                writeVsimToTA(100, (HwVSimConstants.EnableParam) object);
                setIsTaOpen(false);
            }
        }
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

    public boolean canProcessEnable(int operation) {
        if (operation != 5 || !isVSimEnabled()) {
            logd("canProcessEnable defaultstate:" + isInSpecificState(this.mDefaultState));
            return isInSpecificState(this.mDefaultState);
        }
        logd("canProcessEnable vsim is on, can not process offline request");
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean isProcessInit() {
        return isInSpecificState(this.mInitialState);
    }

    public boolean canProcessDisable() {
        return isInSpecificState(this.mDefaultState);
    }

    public boolean canProcessSwitchMode() {
        return isInSpecificState(this.mDefaultState);
    }

    public boolean canStartNvMatchListener() {
        return isInSpecificState(this.mDefaultState) || isInSpecificState(this.mSmReadyState) || isInSpecificState(this.mEReadyState);
    }

    public boolean canProcessRestartRild() {
        return isInSpecificState(this.mDefaultState) && !isWaitingSwitchSimSlot();
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
        CommandsInterfaceEx commandsInterfaceEx;
        if (h != null && (commandsInterfaceEx = this.mVSimCi) != null) {
            commandsInterfaceEx.setOnRegPLMNSelInfo(h, what, obj);
        }
    }

    public void unSetOnVsimRegPLMNSelInfo(Handler h) {
        CommandsInterfaceEx commandsInterfaceEx;
        if (h != null && (commandsInterfaceEx = this.mVSimCi) != null) {
            commandsInterfaceEx.unSetOnVsimRegPLMNSelInfo(h);
        }
    }

    public void setOnRestartRildNvMatch(int subId, Handler h, int what, Object obj) {
        if (h != null) {
            CommandsInterfaceEx ci = getCiBySub(subId);
            logd("setOnRestartRildNvMatch, subId = " + subId);
            if (ci != null) {
                ci.setOnRestartRildNvMatch(h, what, obj);
            }
        }
    }

    public void unSetOnRestartRildNvMatch(int subId, Handler h) {
        if (h != null) {
            CommandsInterfaceEx ci = getCiBySub(subId);
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
        for (int i = 0; i < this.mCis.length; i++) {
            this.mCis[i].unregisterForAvailable(h);
        }
        this.mVSimCi.unregisterForAvailable(h);
    }

    public void registerForVSimIccChanged(Handler h, int what, Object obj) {
        HwVSimUiccController hwVSimUiccController = this.mVSimUiccController;
        if (hwVSimUiccController != null) {
            hwVSimUiccController.registerForIccChanged(h, what, obj);
        }
    }

    public void unregisterForVSimIccChanged(Handler h) {
        HwVSimUiccController hwVSimUiccController = this.mVSimUiccController;
        if (hwVSimUiccController != null) {
            hwVSimUiccController.unregisterForIccChanged(h);
        }
    }

    public UiccCardExt getUiccCard(int subId) {
        if (subId < 0 || subId >= HwVSimModemAdapter.MAX_SUB_COUNT) {
            return null;
        }
        if (subId == 2) {
            HwVSimUiccController hwVSimUiccController = this.mVSimUiccController;
            if (hwVSimUiccController == null) {
                return null;
            }
            return hwVSimUiccController.getUiccCard();
        }
        UiccControllerExt uiccControllerExt = this.mUiccController;
        if (uiccControllerExt == null) {
            return null;
        }
        return uiccControllerExt.getUiccCard(subId);
    }

    public void allowData(int dataSubId) {
        logd("allowData data sub: " + dataSubId);
        if (dataSubId != 2 && (dataSubId < 0 || dataSubId >= this.mPhones.length)) {
            logd("data sub invalid");
            return;
        }
        PhoneExt phone = getPhoneBySub(dataSubId);
        if (phone == null) {
            logd("phone not found");
            return;
        }
        DcTrackerEx dcTracker = phone.getDcTracker();
        if (dcTracker == null) {
            logd("dctracker not found");
            return;
        }
        logd("call set data allowed on sub: " + dataSubId);
        dcTracker.setDataAllowed(true, (Message) null);
    }

    public void switchDDS() {
        logd("switchDDS");
        this.mVSimPhone.updateDataConnectionTracker();
        if (this.mPhones != null) {
            int len = this.mPhones.length;
            logd("call cleanUpAllConnections mProxyPhones.length=" + len);
            for (int phoneId = 0; phoneId < len; phoneId++) {
                logd("call cleanUpAllConnections phoneId=" + phoneId);
                PhoneExt phone = this.mPhones[phoneId];
                if (phone == null) {
                    logd("active phone not found");
                } else {
                    DcTrackerEx dcTracker = phone.getDcTracker();
                    if (dcTracker == null) {
                        logd("dcTracker not found");
                    } else {
                        dcTracker.setDataAllowed(false, (Message) null);
                        dcTracker.cleanUpAllConnections("DDS switch");
                    }
                }
            }
        }
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int getVSimOccupiedSlotId() {
        int subId = getVsimSlotId();
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

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean isDoingSlotSwitch() {
        return isInSpecificState(this.mDReadyState);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
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
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.mContext.registerReceiver(this.mNetworkStateReceiver, filter);
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
        NetworkInfo[] info;
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (cm == null || (info = cm.getAllNetworkInfo()) == null) {
            return false;
        }
        for (int i = 0; i < info.length; i++) {
            if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                logi("exist a network connected, type: " + info[i].getType());
                return true;
            }
        }
        return false;
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
        this.mInsertedSimCount = 0;
        int i = 0;
        while (true) {
            if (i >= HwVSimModemAdapter.PHONE_COUNT) {
                break;
            }
            UiccCardExt card = this.mUiccController.getUiccCard(i);
            if (card == null) {
                logd("uicc card " + i + " not ready");
                break;
            }
            logd("updateUiccCardCount cardstate = " + card.getCardState() + " i = " + i);
            if (card.getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT) {
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

    private int enableVSim(String imsi, int cardType, int apnType, String acqorder, String tapath, int vsimloc, String challenge, int operation, int cardInModem1, int supportVsimCa) {
        logd("enableVSim cardType: " + cardType + " apnType: " + apnType + " acqOrder: " + acqorder + " cardInModem1: " + cardInModem1 + " supportVsimCa: " + supportVsimCa);
        cmdSem_acquire();
        StringBuilder sb = new StringBuilder();
        sb.append("enableVSim subId = ");
        sb.append(2);
        logd(sb.toString());
        HwVSimPhoneFactory.setVSimEnabledSubId(2);
        if (HwVSimUtilsInner.isSupportedVsimDynamicStartStop()) {
            unregisterAirplaneModeReceiver();
        }
        HwVSimConstants.EnableParam param = new HwVSimConstants.EnableParam(imsi, cardType, apnType, acqorder, challenge, operation, tapath, vsimloc, cardInModem1, supportVsimCa);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableEnterReport() {
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
    /* access modifiers changed from: public */
    private void enableExitReport() {
        HwVSimRequest hwVSimRequest = this.mEnableRequest;
        if (hwVSimRequest != null) {
            int iResult = 3;
            Object oResult = hwVSimRequest.getResult();
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
                HwVSimEventReport.VSimEventInfoUtils.setSimOperator(this.mEventInfo, BuildConfig.FLAVOR);
                HwVSimEventReport.VSimEventInfoUtils.setSavedCommrilMode(this.mEventInfo, -1);
                HwVSimEventReport.VSimEventInfoUtils.setSavedMainSlot(this.mEventInfo, HwVSimPhoneFactory.getVSimSavedMainSlot());
                HwVSimEventReport.VSimEventInfo vSimEventInfo = this.mEventInfo;
                if (isVSimOn()) {
                    i = 11;
                }
                HwVSimEventReport.VSimEventInfoUtils.setSimMode(vSimEventInfo, i);
                HwVSimEventReport.VSimEventInfoUtils.setSlotsTable(this.mEventInfo, getSlotsTableNumeric(getSimSlotTable()));
                HwVSimEventReport.VSimEventInfoUtils.setSavedNetworkMode(this.mEventInfo, HwVSimPhoneFactory.getVSimSavedNetworkMode(0));
                HwVSimEventReport.VSimEventInfoUtils.setWorkMode(this.mEventInfo, getWorkMode());
            }
        }
        reportEvent(this.mVSimEventReport, this.mEventInfo);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableEnterReport() {
        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mEventInfo, 11);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableExitReport() {
        HwVSimRequest hwVSimRequest = this.mDisableRequest;
        if (hwVSimRequest != null) {
            boolean iResult = false;
            Object oResult = hwVSimRequest.getResult();
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
        HwVSimEventReport.VSimEventInfoUtils.setSimOperator(this.mEventInfo, BuildConfig.FLAVOR);
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
    /* access modifiers changed from: public */
    private void switchEnterReport() {
        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mEventInfo, 1);
        HwVSimEventReport.VSimEventInfoUtils.setSimOperator(this.mEventInfo, BuildConfig.FLAVOR);
        this.mVSimEnterTime = SystemClock.elapsedRealtime();
        logi("vsim enter time is " + this.mVSimEnterTime);
        HwVSimEventReport.VSimEventInfoUtils.setPsRegTime(this.mEventInfo, -1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void switchExitReport() {
        HwVSimRequest hwVSimRequest = this.mSwitchModeRequest;
        if (hwVSimRequest != null) {
            boolean iResult = false;
            Object oResult = hwVSimRequest.getResult();
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
        HwVSimEventReport.VSimEventInfoUtils.setSimOperator(this.mEventInfo, BuildConfig.FLAVOR);
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
        if (workMode == 0) {
            setUserReservedSubId(0);
        } else if (workMode != 1) {
            if (workMode != 2) {
            }
        } else {
            setUserReservedSubId(1);
        }
    }

    private void reportEvent(HwVSimEventReport eventReport, HwVSimEventReport.VSimEventInfo eventInfo) {
        if (SystemClock.elapsedRealtime() - this.mLastReportTime < 5000) {
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
        IccRecordsEx r;
        if (this.mVSimUiccController == null || (r = this.mVSimUiccController.getIccRecords(UiccControllerExt.APP_FAM_3GPP)) == null) {
            return BuildConfig.FLAVOR;
        }
        String operator = r.getOperatorNumeric();
        if (operator == null) {
            operator = BuildConfig.FLAVOR;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getOperatorNumberic - returning from card: ");
        sb.append(HW_DBG ? operator : "***");
        logd(sb.toString());
        return operator;
    }

    private int getSlotsTableNumeric(int[] slotsTable) {
        int size = ArrayUtils.size(slotsTable);
        if (size < 3) {
            loge("getSlotsTableNumeric error size:" + size);
            return -1;
        } else if (slotsTable[0] == 0 && slotsTable[1] == 1 && slotsTable[2] == 2) {
            return 0;
        } else {
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
    }

    private int getMainSlot(int[] slotsTable) {
        int size = ArrayUtils.size(slotsTable);
        if (size < 3) {
            loge("getMainSlot error size:" + size);
            return -1;
        } else if (slotsTable[0] == 0 && slotsTable[1] == 1 && slotsTable[2] == 2) {
            return 0;
        } else {
            if (slotsTable[0] == 1 && slotsTable[1] == 0 && slotsTable[2] == 2) {
                return 1;
            }
            if (slotsTable[0] == 2 && slotsTable[1] == 1 && slotsTable[2] == 0) {
                return 0;
            }
            if (slotsTable[0] == 2 && slotsTable[1] == 0 && slotsTable[2] == 1) {
                return 1;
            }
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getWorkMode() {
        int reservedSubId = getUserReservedSubId();
        if (reservedSubId == 0) {
            return 0;
        }
        if (reservedSubId == 1) {
            return 1;
        }
        return 2;
    }

    private HwVSimModemAdapter createModemAdapter(Context context, CommandsInterfaceEx vsimCi, CommandsInterfaceEx[] cis) {
        if (isPlatformTwoModems()) {
            return HwVSimDualModem.create(this, context, vsimCi, cis);
        }
        return HwVSimTriModem.create(this, context, vsimCi, cis);
    }

    private boolean cmdSem_acquire() {
        if (this.mCmdSem == null) {
            return false;
        }
        try {
            logd("cmd sem try acquire");
            boolean acquired = this.mCmdSem.tryAcquire(90000, TimeUnit.MILLISECONDS);
            logd("cmd sem acquired");
            synchronized (this) {
                if (acquired) {
                    if (this.mCmdSemAcquired != null) {
                        logd("cmd sem mark acquired");
                        this.mCmdSemAcquired.set(true);
                    }
                }
            }
            return acquired;
        } catch (InterruptedException e) {
            logd("cmd sem not acquired");
            return false;
        }
    }

    public void cmdSem_release() {
        if (this.mCmdSem != null) {
            synchronized (this) {
                if (this.mCmdSemAcquired != null) {
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
    }

    private void waitVSimIdle(int timeout) {
        final long endTime = SystemClock.elapsedRealtime() + ((long) timeout);
        Thread t = new Thread() {
            /* class com.android.internal.telephony.vsim.HwVSimController.AnonymousClass2 */

            @Override // java.lang.Thread, java.lang.Runnable
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

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void broadcastVSimCardType() {
        logi("broadcastVSimCardType, card type is " + this.mCurCardType + ", old card type is " + this.mOldCardType);
        Intent intent = new Intent("com.huawei.vsim.action.VSIM_CARD_RELOAD");
        intent.putExtra("subscription", 2);
        intent.putExtra("phone", 2);
        intent.putExtra("slot", 2);
        intent.putExtra("subId", 2);
        intent.putExtra(HwVSimConstants.VSIM_CARDTYPE, this.mCurCardType);
        intent.putExtra(HwVSimConstants.VSIM_OLD_CARDTYPE, this.mOldCardType);
        this.mContext.sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    private int setApn(String imsi, int cardType, int apnType, String tapath, String challenge, boolean isForHash, int supportVsimCa) {
        boolean z;
        int result;
        logd("setApn cardType: " + cardType + " apnType: " + apnType + " isForHash: " + isForHash + " supportVsimCa: " + supportVsimCa);
        int subId = HwVSimPhoneFactory.getVSimEnabledSubId();
        StringBuilder sb = new StringBuilder();
        sb.append("subId = ");
        sb.append(subId);
        logi(sb.toString());
        if (subId == -1) {
            return 3;
        }
        if (supportVsimCa == 1) {
            result = ((Integer) sendRequest(20, new HwVSimConstants.EnableParam(imsi, cardType, apnType, BuildConfig.FLAVOR, challenge, 2, tapath, -1, -1, supportVsimCa), subId)).intValue();
            z = true;
        } else {
            z = true;
            result = writeApnToTA(imsi, cardType, apnType, tapath, challenge, isForHash, 0);
        }
        this.mVSimPhone.setInternalDataEnabled(false);
        this.mVSimPhone.setInternalDataEnabled(z);
        logi("setApn result = " + result);
        setApnReport(result);
        return result;
    }

    public int writeApnToTA(String imsi, int cardType, int apnType, String tapath, String challenge, boolean isForHash, int supportVsimCa) {
        logi("writeApnToTA");
        if (challenge != null) {
            if (challenge.length() != 0) {
                return HwVSimOperateTA.getDefault().operateTA(supportVsimCa == 1 ? 52 : 2, imsi, cardType, apnType, challenge, isForHash, tapath, 0, 0);
            }
        }
        logi("invalid param challenge");
        return 1;
    }

    private boolean hasIccCardOnM2(int[] cardTypes) {
        int cardCount;
        if (cardTypes == null || (cardCount = cardTypes.length) == 0) {
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
            int mainSlot = getMainSlot(getSimSlotTable());
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
        int mainSlot = getMainSlot(getSimSlotTable());
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
            if (checkIfHasCdmaCardForTelecom(cardTypes, expect, mainSlot) || expect != current) {
                return true;
            }
        } else if (singleGsmOnM1OrCdmaOnM2) {
            return true;
        }
        return false;
    }

    private void processNoNeedSwitchModeForCmcc(int[] cardTypes) {
        if (HwFullNetworkManager.getInstance().isCMCCDsdxDisable()) {
            if (!isVSimEnabled()) {
                logd("processNoNeedSwitchModeForCmcc: vsim is off, return.");
                return;
            }
            int insertCardCount = getInsertedCardCount(cardTypes);
            logd("processNoNeedSwitchModeForCmcc: insertCardCount = " + insertCardCount);
            if (insertCardCount > 0 && !isDisableProcess()) {
                int slotInModem1 = HwVSimUtilsInner.getAnotherSlotId(getMainSlot(getSimSlotTable()));
                int networkTypeInModem1 = HwVSimUtilsInner.getNetworkTypeInModem1ForCmcc(this.mModemAdapter.restoreSavedNetworkMode(slotInModem1));
                logd("processNoNeedSwitchModeForCmcc: slotInModem1 = " + slotInModem1 + ", mode = " + networkTypeInModem1);
                if (HwVSimUtilsInner.isNrServiceAbilityOn(this.mModemAdapter.restoreSavedNetworkMode(0))) {
                    networkTypeInModem1 = 65;
                }
                this.mModemAdapter.setPreferredNetworkType(null, null, slotInModem1, networkTypeInModem1);
            }
        }
    }

    private boolean checkIfHasCdmaCardInDualImsSupport(int[] cardTypes, HwVSimSlotSwitchController.CommrilMode current, int mainSlot) {
        if (!HwVSimUtilsInner.isDualImsSupported()) {
            return false;
        }
        HwVSimSlotSwitchController.CommrilMode vimOnCommrilMode = this.mVSimSlotSwitchController.getVSimOnCommrilMode(mainSlot, cardTypes);
        logd("needSwitchModeHotplug: vimOnCommrilMode = " + vimOnCommrilMode);
        if (current != vimOnCommrilMode) {
            return true;
        }
        return false;
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

    public void setBlockPinFlag(boolean value) {
        this.mBlockPinFlag = value;
        logd("mBlockPinFlag = " + this.mBlockPinFlag);
    }

    public void setBlockPinTable(int slotId, boolean value) {
        if (!(this.mBlockPinTable == null || this.mPinBlockedTable == null)) {
            for (int i = 0; i < HwVSimModemAdapter.PHONE_COUNT; i++) {
                this.mBlockPinTable[i] = false;
                this.mPinBlockedTable[i] = false;
            }
            if (slotId >= 0 && slotId < HwVSimModemAdapter.PHONE_COUNT) {
                this.mBlockPinTable[slotId] = value;
                logi("mBlockPinTable = " + Arrays.toString(this.mBlockPinTable));
            }
        }
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean needBlockPin(int slotId) {
        logd("needBlockPin check for subId " + slotId);
        logd("mBlockPinFlag = " + this.mBlockPinFlag + ", mBlockPinTable = " + Arrays.toString(this.mBlockPinTable) + ", mPinBlockedTable = " + Arrays.toString(this.mPinBlockedTable));
        if (slotId < 0 || slotId >= HwVSimModemAdapter.PHONE_COUNT || !this.mBlockPinFlag || !this.mBlockPinTable[slotId]) {
            return false;
        }
        this.mPinBlockedTable[slotId] = true;
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
            UiccCardExt card = this.mUiccController.getUiccCard(subId);
            if (card != null) {
                isCardPresent = card.getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT;
            }
            if (isCardPresent && isPinNeedBlock(subId)) {
                logd("disposeCardForPinBlock: card present and pin blocked");
                this.mUiccController.disposeCard(subId);
            }
        }
    }

    private void setupDataOnVSimEnded() {
        boolean isDataEnabled = false;
        int dataSlotId = -1;
        SubscriptionControllerEx subController = SubscriptionControllerEx.getInstance();
        if (subController != null) {
            dataSlotId = SubscriptionControllerEx.getInstance().getSlotIndex(subController.getDefaultDataSubId());
        }
        logd("setupDataOnVSimEnded data dataSlotId: " + dataSlotId);
        if (dataSlotId != -1 && dataSlotId < this.mPhones.length) {
            isDataEnabled = this.mPhones[dataSlotId].isDataEnabled();
            allowData(dataSlotId);
        }
        if (isDataEnabled) {
            Message msg = this.mMainHandler.obtainMessage();
            msg.what = 2;
            msg.arg1 = dataSlotId;
            this.mMainHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetupDataOnVSimEnded(int phoneId) {
        DcTrackerEx dcTracker;
        logd("onSetupDataOnVSimEnded phoneId=" + phoneId);
        PhoneExt phone = getPhoneBySub(phoneId);
        if (phone != null && (dcTracker = phone.getDcTracker()) != null) {
            dcTracker.setupDataOnConnectableApns(HwVSimConstants.PHONE_REASON_VSIM_ENDED, (String) null);
        }
    }

    public void updateUserPreferences() {
        this.mMainHandler.obtainMessage(1).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUpdateUserPreferences() {
        logd("onUpdateUserPreferences");
        HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyFactoryImpl").createHwUiccManager().updateUserPreferences(false);
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
    /* access modifiers changed from: public */
    private void onUpdateSubActivation() {
        logd("onUpdateSubActivation");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAutoSwitchSimSlot() {
        logd("onAutoSwitchSimSlot");
        if (HwVSimUtilsInner.isPlatformRealTripple() && HwFullNetworkConfig.IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT) {
            setSubActivationUpdate(false);
            HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(getAllowDataSlotId());
        }
        if (isPlatformTwoModems()) {
            setSubActivationUpdate(false);
            int default4GSlotId = getAllowDataSlotId();
            if (default4GSlotId != HwTelephonyManagerInner.getDefault().getDefault4GSlotId()) {
                this.mNvMatchController.storeIfNeedRestartRildForNvMatch(false);
                HwFullNetworkManager.getInstance().setMainSlot(default4GSlotId, (Message) null);
            } else {
                HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(default4GSlotId);
            }
            HwFullNetworkManager.getInstance().saveMainCardIccId(HwFullNetworkManager.getInstance().getFullIccid(default4GSlotId));
            setIsWaitingSwitchSimSlot(false);
        }
        setSavedMainSlotAndCardCount(-1, -1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAutoSetAllowData() {
        int allowDataSlotId = getAllowDataSlotId();
        if (this.mPhones != null) {
            int len = this.mPhones.length;
            logd("onAutoSetAllowData, mPhones.length=" + len);
            for (int phoneId = 0; phoneId < len; phoneId++) {
                PhoneExt phone = this.mPhones[phoneId];
                if (phone == null) {
                    logd("active phone not found");
                } else {
                    DcTrackerEx dcTracker = phone.getDcTracker();
                    if (dcTracker == null) {
                        logd("dcTracker not found");
                    } else if (phoneId == allowDataSlotId) {
                        logd("call set data allowed on sub: " + phoneId);
                        dcTracker.setDataAllowed(true, this.mMainHandler.obtainMessage(4));
                    } else {
                        logd("call set data not allowed on sub: " + phoneId);
                        dcTracker.setDataAllowed(false, (Message) null);
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
        int i;
        int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            return default4GSlotId;
        }
        int allowDataSlotId = default4GSlotId;
        int i2 = this.mVSimCardCount;
        if (i2 == 1) {
            allowDataSlotId = getSwitchActivatedSubId(default4GSlotId);
        } else if (i2 == 2 && (i = this.mVSimSavedMainSlot) != -1) {
            int savedMainSlotSubState = getSubState(i);
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
        int i;
        int activatedSubId = oldActivatedSubId;
        SubscriptionControllerEx subController = SubscriptionControllerEx.getInstance();
        if (subController == null) {
            logd("getSwitchActivatedSubId: sub controller not found subId = " + activatedSubId);
            return activatedSubId;
        }
        if (HwFullNetworkConfig.IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT) {
            int activatedSubCount = 0;
            boolean[] isSubActivated = new boolean[this.mCis.length];
            int i2 = 0;
            while (true) {
                i = 0;
                if (i2 >= this.mCis.length) {
                    break;
                }
                if (subController.getSubState(i2) == 1) {
                    isSubActivated[i2] = true;
                    activatedSubCount++;
                } else {
                    isSubActivated[i2] = false;
                }
                i2++;
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

    public void delaymIsVSimCauseCardReloadRecover() {
        if (HwVSimUtilsInner.isDualImsSupported()) {
            logd("delaymIsVSimCauseCardReloadRecover mIsVSimCauseCardReload: " + this.mIsVSimCauseCardReload);
            if (!this.mIsVSimCauseCardReload) {
                this.mIsVSimCauseCardReload = true;
                this.mMainHandler.sendMessageDelayed(this.mMainHandler.obtainMessage(6), 5000);
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
    /* access modifiers changed from: public */
    private void onRecoverMarkToFalse() {
        logd("onRecoverMarkToFalse mIsVSimCauseCardReload: " + this.mIsVSimCauseCardReload);
        this.mIsVSimCauseCardReload = false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean isSubActivationUpdate() {
        if (HwFullNetworkConfig.IS_HISI_DSDS_AUTO_SWITCH_4G_SLOT) {
            return false;
        }
        logd("isSubActivationUpdate : " + this.mIsSubActivationUpdate);
        return this.mIsSubActivationUpdate;
    }

    public void setSubActivationUpdate(boolean isUpate) {
        logd("setSubActivationUpdate : " + isUpate);
        this.mIsSubActivationUpdate = isUpate;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void updateSimCardTypes(int[] cardTypes) {
        Handler h;
        synchronized (sSimCardTypesLock) {
            if (cardTypes != null) {
                this.mSimCardTypes = (int[]) cardTypes.clone();
                if (isProcessInit() && (h = getHandler()) != null) {
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

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void setSubActived(int slotId) {
        synchronized (sCheckSubActivedLock) {
            if (slotId >= 0) {
                if (slotId < this.mCheckSubActivated.length) {
                    logd("[SLOT" + slotId + "] set sub actived");
                    this.mCheckSubActivated[slotId] = false;
                }
            }
        }
    }

    private void initCheckSubActived(int[] cardTypes) {
        if (cardTypes != null) {
            for (int i = 0; i < cardTypes.length; i++) {
                if (cardTypes[i] != 0) {
                    SubscriptionControllerEx subController = SubscriptionControllerEx.getInstance();
                    if (subController == null) {
                        logd("sub controller not found");
                        return;
                    } else if (subController.getSubState(i) == 0) {
                        synchronized (sCheckSubActivedLock) {
                            if (i >= 0) {
                                if (i < this.mCheckSubActivated.length) {
                                    logd("[SLOT" + i + "] sub state is inactive.");
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

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean prohibitSubUpdateSimNoChange(int slotId) {
        if (this.mProhibitSubUpdateSimNoChange != null && slotId >= 0 && slotId < HwVSimModemAdapter.PHONE_COUNT) {
            return this.mProhibitSubUpdateSimNoChange[slotId];
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

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public synchronized boolean getIsWaitingSwitchCdmaModeSide() {
        return this.isWaitingSwitchCdmaModeSide;
    }

    public synchronized void setIsWaitingSwitchCdmaModeSide(boolean value) {
        this.isWaitingSwitchCdmaModeSide = value;
        logd("set isWaitingSwitchCdmaModeSide = " + this.isWaitingSwitchCdmaModeSide);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public synchronized boolean getIsWaitingNvMatchUnsol() {
        return this.mIsWaitingNvMatchUnsol;
    }

    public synchronized void setIsWaitingNvMatchUnsol(boolean value) {
        this.mIsWaitingNvMatchUnsol = value;
        logd("set mIsWaitingNvMatchUnsol = " + this.mIsWaitingNvMatchUnsol);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public String getPendingDeviceInfoFromSP(String prefKey) {
        return HwVSimPhoneFactory.getPendingDeviceInfoFromSP(prefKey, getSimSlotTableLastSlotId());
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void simHotPlugOut(int slotId) {
        logd("simHotPlugOut, slotId: " + slotId);
        if (isVSimEnabled() || isVSimCauseCardReload()) {
            if (slotId >= 0 && slotId < HwVSimModemAdapter.MAX_SUB_COUNT) {
                setMarkForCardReload(slotId, false);
            }
            this.mModemAdapter.onSimHotPlugOut();
        }
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void simHotPlugIn(int slotId) {
        logd("simHotPlugIn, slotId: " + slotId);
        if (HwFullNetworkManager.getInstance().isCMCCDsdxDisable() && isVSimEnabled()) {
            boolean dualLteCap = HwVSimUtilsInner.isDualImsSwitchOpened();
            int maxCap = this.mModemAdapter.getAllAbilityNetworkTypeOnModem1(dualLteCap);
            logd("simHotPlugIn,restore MODEM1 maxCap:" + maxCap + ",duallteCap:" + dualLteCap);
            HwVSimPhoneFactory.setVSimSavedNetworkMode(1, maxCap);
        }
    }

    public int getRadioOnSubId() {
        int subId = 0;
        while (true) {
            boolean isRadioOn = false;
            if (subId >= HwVSimModemAdapter.MAX_SUB_COUNT) {
                return 0;
            }
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null) {
                if (ci.getRadioState() == 1) {
                    isRadioOn = true;
                }
                if (isRadioOn) {
                    return subId;
                }
            }
            subId++;
        }
    }

    public void clearApDsFlowCfg() {
        int subId = getVsimSlotId();
        logd("clearApDsFlowCfg, subId = " + subId);
        if (subId != 2) {
            subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            logd("clearApDsFlowCfg, VSim not enabled, to mainslot: " + subId);
        }
        setApDsFlowCfg(subId, 0, 0, 0, 0);
        setDsFlowNvCfg(subId, 0, 0);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean hasVSimIccCard() {
        PhoneExt vSimPhone = HwVSimPhoneFactory.getVSimPhone();
        return (vSimPhone != null && vSimPhone.hasIccCard()) || getVSimOnSuccess();
    }

    /* access modifiers changed from: private */
    public class VSimDefaultState extends StateEx {
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
            HwVSimDefaultProcessor hwVSimDefaultProcessor = this.mProcessor;
            if (hwVSimDefaultProcessor == null) {
                return false;
            }
            return hwVSimDefaultProcessor.processMessage(msg);
        }

        public int networksScan(int subId, int type) {
            HwVSimDefaultProcessor hwVSimDefaultProcessor = this.mProcessor;
            if (hwVSimDefaultProcessor != null) {
                return hwVSimDefaultProcessor.networksScan(subId, type);
            }
            HwVSimController.this.mNetworkScanIsRunning = 0;
            return 1;
        }
    }

    /* access modifiers changed from: private */
    public class InitialState extends StateEx {
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
            HwVSimInitialProcessor hwVSimInitialProcessor = this.mProcessor;
            if (hwVSimInitialProcessor == null) {
                return false;
            }
            return hwVSimInitialProcessor.processMessage(msg);
        }
    }

    private class RestartRildState extends StateEx {
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
            HwVSimRestartRildProcessor hwVSimRestartRildProcessor = this.mProcessor;
            if (hwVSimRestartRildProcessor == null) {
                return false;
            }
            return hwVSimRestartRildProcessor.processMessage(msg);
        }
    }

    private class VSimEnableState extends StateEx {
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
            HwVSimEnableProcessor hwVSimEnableProcessor = this.mProcessor;
            if (hwVSimEnableProcessor == null) {
                return false;
            }
            return hwVSimEnableProcessor.processMessage(msg);
        }
    }

    private class VSimEWorkState extends StateEx {
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
            HwVSimEWorkProcessor hwVSimEWorkProcessor = this.mProcessor;
            if (hwVSimEWorkProcessor == null) {
                return false;
            }
            return hwVSimEWorkProcessor.processMessage(msg);
        }
    }

    private class VSimEReadyState extends StateEx {
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
            HwVSimEReadyProcessor hwVSimEReadyProcessor = this.mProcessor;
            if (hwVSimEReadyProcessor == null) {
                return false;
            }
            return hwVSimEReadyProcessor.processMessage(msg);
        }
    }

    private class VSimDisableState extends StateEx {
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
            HwVSimDisableProcessor hwVSimDisableProcessor = this.mProcessor;
            if (hwVSimDisableProcessor == null) {
                return false;
            }
            return hwVSimDisableProcessor.processMessage(msg);
        }
    }

    private class VSimDWorkState extends StateEx {
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
            HwVSimDWorkProcessor hwVSimDWorkProcessor = this.mProcessor;
            if (hwVSimDWorkProcessor == null) {
                return false;
            }
            return hwVSimDWorkProcessor.processMessage(msg);
        }
    }

    private class VSimDReadyState extends StateEx {
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
            HwVSimDReadyProcessor hwVSimDReadyProcessor = this.mProcessor;
            if (hwVSimDReadyProcessor == null) {
                return false;
            }
            return hwVSimDReadyProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeState extends StateEx {
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
            HwVSimSwitchModeProcessor hwVSimSwitchModeProcessor = this.mProcessor;
            if (hwVSimSwitchModeProcessor == null) {
                return false;
            }
            return hwVSimSwitchModeProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeWorkState extends StateEx {
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
            HwVSimSWorkProcessor hwVSimSWorkProcessor = this.mProcessor;
            if (hwVSimSWorkProcessor == null) {
                return false;
            }
            return hwVSimSWorkProcessor.processMessage(msg);
        }
    }

    private class VSimSwitchModeReadyState extends StateEx {
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
            HwVSimSReadyProcessor hwVSimSReadyProcessor = this.mProcessor;
            if (hwVSimSReadyProcessor == null) {
                return false;
            }
            return hwVSimSReadyProcessor.processMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public class HwVSimEventHandler extends Handler {
        private static final int EVENT_HOTPLUG_SWITCHMODE = 1000;
        private static final int EVENT_VSIM_DISABLE_RETRY = 1001;

        HwVSimEventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == EVENT_HOTPLUG_SWITCHMODE) {
                HwVSimController hwVSimController = HwVSimController.this;
                hwVSimController.logd("EVENT_HOTPLUG_SWITCHMODE, count = " + HwVSimController.this.mRetryCountForHotPlug);
                if (HwVSimController.this.canProcessSwitchMode()) {
                    HwVSimController.this.switchVsimWorkMode(HwVSimController.this.getWorkMode(), true);
                    return;
                }
                HwVSimController.this.handleSwitchModeDelay();
            } else if (i == EVENT_VSIM_DISABLE_RETRY) {
                HwVSimController hwVSimController2 = HwVSimController.this;
                hwVSimController2.logd("EVENT_VSIM_DISABLE_RETRY, count = " + HwVSimController.this.mDisableRetryCount);
                if (HwVSimController.this.mDisableRetryCount >= 3) {
                    HwVSimController.this.logd("max count, abort retry");
                    return;
                }
                HwVSimController.access$4208(HwVSimController.this);
                if (!HwVSimController.this.mDisableFailMark) {
                    HwVSimController.this.logd("no fail mark, abort retry");
                } else {
                    HwVSimController.this.disableVSim();
                }
            }
        }
    }

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 93) {
                switch (i) {
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
            } else {
                HwVSimController.this.logd("EVENT_AIRPLANE_MODE_ON");
                HwVSimController.this.onAirplaneModeOn();
            }
        }
    }
}
