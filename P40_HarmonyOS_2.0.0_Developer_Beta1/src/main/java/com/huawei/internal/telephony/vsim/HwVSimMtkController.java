package com.huawei.internal.telephony.vsim;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyvsim.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.vsim.process.HwVSimMtkDefaultProcessor;
import com.huawei.internal.telephony.vsim.process.HwVSimMtkDisableProcessor;
import com.huawei.internal.telephony.vsim.process.HwVSimMtkEnableProcessor;
import com.huawei.internal.telephony.vsim.process.HwVSimMtkInitialProcessor;
import com.huawei.internal.telephony.vsim.util.ArrayUtils;
import com.huawei.internal.telephony.vsim.util.SafeUnBox;
import com.huawei.internal.util.StateEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HwVSimMtkController extends HwVSimBaseController {
    private static final int DEFAULT_INTERVAL = 10;
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwVSimMtkController";
    private static final String MOBILE_DATA = "mobile_data";
    private static final int REPORT_INTERVAL_MILLIS = 5000;
    private static HwVSimMtkController sInstance = null;
    private Semaphore mCmdSem;
    private AtomicBoolean mCmdSemAcquired;
    private int mCurrentPhoneId = -1;
    private VsimDefaultState mDefaultState = new VsimDefaultState();
    private HwVSimRequest mDisableRequest;
    private VsimDisableState mDisableState = new VsimDisableState();
    private HwVSimRequest mEnableRequest;
    private VsimEnableState mEnableState = new VsimEnableState();
    private ExternalSimManagerEx mExternalSimManagerEx;
    private final Set<IHwVSimPhoneSwitchCallback> mHwVsimPhoneSwitchCallbacks = new CopyOnWriteArraySet();
    private InitialState mInitialState = new InitialState();
    private HwVSimEventHandler mVSimEventHandler;
    private final HwVSimRilReceiveTask mVSimRilReceiveTask;
    private HwVSimOnSubscriptionsChangedListener vSimOnSubscriptionsChangedListener;

    private HwVSimMtkController(Context context, PhoneExt[] phones, CommandsInterfaceEx[] cis) {
        super(context, phones, cis);
        slogi("HwVSimMtkController...");
        this.mMainHandler = new MyHandler();
        this.mCmdSem = new Semaphore(1);
        this.mCmdSemAcquired = new AtomicBoolean(false);
        this.mExternalSimManagerEx = ExternalSimManagerEx.make(context, cis);
        this.mVSimRilReceiveTask = new HwVSimRilReceiveTask(phones, cis);
        this.mModemAdapter = HwVSimMtkDualModem.create(this, this.mContext, cis);
        registerForVsimPhoneSwitch(this.mVSimRilReceiveTask.getVSimPhoneSwitchCallback());
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mEnableState, this.mDefaultState);
        addState(this.mDisableState, this.mDefaultState);
        setInitialState(this.mInitialState);
        HandlerThread vSimEventThread = new HandlerThread("VSimEventThread");
        vSimEventThread.start();
        this.mVSimEventHandler = new HwVSimEventHandler(vSimEventThread.getLooper());
        slogi("HwVSimMtkController.");
    }

    public void dispose() {
        this.mVSimRilReceiveTask.stop();
    }

    public static void create(Context context, PhoneExt[] phones, CommandsInterfaceEx[] cis) {
        slogi("create");
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new HwVSimMtkController(context, phones, cis);
                sInstance.start();
            } else {
                throw new RuntimeException("VSimController already created");
            }
        }
    }

    public static boolean isInstantiated() {
        synchronized (LOCK) {
            if (sInstance == null) {
                return false;
            }
            return true;
        }
    }

    public static synchronized HwVSimMtkController getInstance() {
        HwVSimMtkController hwVSimMtkController;
        synchronized (HwVSimMtkController.class) {
            synchronized (LOCK) {
                if (sInstance != null) {
                    hwVSimMtkController = sInstance;
                } else {
                    throw new RuntimeException("HwVSimMtkController not yet created");
                }
            }
        }
        return hwVSimMtkController;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int enableVSim(HwVSimConstants.EnableParam param) {
        int result;
        if (param == null) {
            logi("enableVSim, bundle is null, return fail.");
            return 3;
        }
        String str = param.imsi;
        int cardType = param.cardType;
        int apnType = param.apnType;
        String acqorder = param.acqOrder;
        String str2 = param.taPath;
        int i = param.vsimLoc;
        String str3 = param.challenge;
        logi("enableVSim cardType: " + cardType + " apnType: " + apnType + " acqOrder: " + acqorder);
        cmdSemAcquire();
        int slotId = calcVsimSlotId();
        StringBuilder sb = new StringBuilder();
        sb.append("enableVSim slotId = ");
        sb.append(slotId);
        logi(sb.toString());
        HwVSimPhoneFactory.setVSimEnabledSubId(slotId);
        if (HwVSimUtilsInner.isSupportedVsimDynamicStartStop()) {
            unregisterAirplaneModeReceiver();
        }
        if (this.mApkObserver != null) {
            this.mApkObserver.startWatching(this.mContext);
        }
        int result2 = ((Integer) sendRequest(40, param, slotId)).intValue();
        if (result2 == 0) {
            HwVSimPhoneFactory.setVSimUserEnabled(1);
            result = result2;
        } else {
            result = result2;
            setApDsFlowCfg(slotId, 0, 0, 0, 0);
            if (!(result == 5 || HwVSimPhoneFactory.getVSimUserEnabled() == 1)) {
                logi("enable failure, call disable vsim");
                this.mDisableRetryCount = 0;
                disableVSim();
            }
        }
        logi("enableVSim result = " + result);
        return result;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int setApn(HwVSimConstants.ApnParams params) {
        logi("setApn params:" + params);
        PhoneExt phone = getVSimPhone();
        if (phone == null) {
            loge("setApn phone is null");
            return 3;
        }
        phone.setInternalDataEnabled(false);
        phone.setInternalDataEnabled(true);
        return 0;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean disableVSim() {
        Handler handler;
        logd("disableVSim ...");
        if (!isVSimEnabled()) {
            logi("VSIM is disabled already, disableVSim result = true!");
            return true;
        } else if (isProcessInit()) {
            logi("VSIM is initing, disableVSim result = false!");
            return false;
        } else {
            cmdSemAcquire();
            if (!canProcessDisable() && (handler = getHandler()) != null) {
                handler.sendEmptyMessage(72);
            }
            waitVSimIdle(HwVSimConstants.MAX_VSIM_WAIT_TIME);
            logd("disableVSim VSim PhoneId = " + this.mCurrentPhoneId);
            if (!isVSimEnabled()) {
                logi("Double Check: VSIM is disabled already, disableVSim result = true!");
                cmdSemRelease();
                return true;
            }
            setIsVSimOn(true);
            boolean success = SafeUnBox.unBox((Boolean) sendRequest(52, null, this.mCurrentPhoneId, Boolean.class), false);
            if (success) {
                this.mDisableFailMark = false;
                logd("disableVSim success, remove EVENT_VSIM_DISABLE_RETRY");
                this.mVSimEventHandler.removeMessages(1001);
                HwVSimPhoneFactory.setVSimEnabledSubId(-1);
                HwVSimPhoneFactory.setVSimUserEnabled(0);
                clearApDsFlowCfg();
                this.mApkObserver.stopWatching();
                if (HwVSimUtilsInner.isPlatformTwoModemsActual()) {
                    setUpDataOnVSimEnded();
                }
                if (HwVSimUtilsInner.isSupportedVsimDynamicStartStop()) {
                    registerAirplaneModeReceiver();
                }
            } else {
                this.mDisableFailMark = true;
                logd("send EVENT_VSIM_DISABLE_RETRY");
                this.mVSimEventHandler.sendEmptyMessageDelayed(1001, HwVSimConstants.VSIM_DISABLE_RETRY_TIMEOUT);
            }
            return success;
        }
    }

    private void clearApDsFlowCfg() {
        int subId = getVsimSlotId();
        logd("clearApDsFlowCfg, subId = " + subId);
        if (subId != 2) {
            subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            logd("clearApDsFlowCfg, VSim not enabled, to mainslot: " + subId);
        }
        setApDsFlowCfg(subId, 0, 0, 0, 0);
    }

    private void waitVSimIdle(int timeout) {
        final long endTime = SystemClock.elapsedRealtime() + ((long) timeout);
        Thread thread = new Thread() {
            /* class com.huawei.internal.telephony.vsim.HwVSimMtkController.AnonymousClass1 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                HwVSimMtkController.this.logd("Waiting for vsim ...");
                while (true) {
                    if (SystemClock.elapsedRealtime() >= endTime) {
                        break;
                    } else if (HwVSimMtkController.this.canProcessDisable()) {
                        HwVSimMtkController.this.logd("vsim idle");
                        break;
                    } else {
                        SystemClock.sleep(500);
                    }
                }
                if (!HwVSimMtkController.this.canProcessDisable()) {
                    HwVSimMtkController.this.logd("Timed out waiting for vsim idle");
                }
            }
        };
        thread.start();
        try {
            thread.join((long) timeout);
        } catch (InterruptedException e) {
            logd("Interrupted");
        }
    }

    private void setUpDataOnVSimEnded() {
        SubscriptionControllerEx subController = SubscriptionControllerEx.getInstance();
        if (subController != null) {
            int dataSubId = subController.getDefaultDataSubId();
            if (dataSubId != 999999) {
                int dataSlotId = SubscriptionControllerEx.getInstance().getSlotIndex(dataSubId);
                HwSubscriptionManager.getInstance().setDefaultDataSubIdBySlotId(dataSlotId);
                logi("setupDataOnVSimEnded, recover to dds to slot" + dataSlotId);
                return;
            }
            int dataSlotId2 = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            int dataSubId2 = SubscriptionManagerEx.getSubIdUsingSlotId(dataSubId);
            logi("setupDataOnVSimEnded, dataSubId: " + dataSubId2 + ", dataSlotId: " + dataSlotId2);
            if (!SubscriptionManagerEx.isValidSubscriptionId(dataSubId2) && HwTelephonyManager.getDefault().getCardType(dataSlotId2) != -1) {
                logi("setupDataOnVSimEnded, slot" + dataSlotId2 + " is present but subId is invalid, set dds later.");
                this.mMainHandler.post(new Runnable(dataSlotId2, dataSubId2) {
                    /* class com.huawei.internal.telephony.vsim.$$Lambda$HwVSimMtkController$IawXscG8UpKMWOWPA6B_efbPG0 */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        HwVSimMtkController.this.lambda$setUpDataOnVSimEnded$0$HwVSimMtkController(this.f$1, this.f$2);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean canProcessDisable() {
        return isInSpecificState(this.mDefaultState);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public String getTrafficData() {
        logi("getTrafficData");
        int slotId = getVsimSlotId();
        if (isVSimClosedBySlotId(slotId)) {
            logi("getTrafficData VSim not enabled");
            return null;
        }
        String callingAppName = getCallingAppName();
        if (TextUtils.isEmpty(callingAppName)) {
            return null;
        }
        if (callingAppName.startsWith(HwVSimConstants.VSIM_PKG_NAME_CHILD_THREAD) || callingAppName.equals(HwVSimConstants.VSIM_PKG_NAME)) {
            return Arrays.toString((String[]) sendRequest(14, null, slotId));
        }
        logi("getTrafficData not allowed, calling app is " + callingAppName);
        return null;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean clearTrafficData() {
        logi("clearTrafficData");
        int slotId = getVsimSlotId();
        if (isVSimClosedBySlotId(slotId)) {
            slotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            logi("clearTrafficData, VSim not enabled, to mainslot: " + slotId);
        }
        String callingAppName = getCallingAppName();
        if (TextUtils.isEmpty(callingAppName)) {
            return false;
        }
        if (callingAppName.startsWith(HwVSimConstants.VSIM_PKG_NAME_CHILD_THREAD) || callingAppName.equals(HwVSimConstants.VSIM_PKG_NAME)) {
            boolean result = ((Boolean) sendRequest(12, null, slotId)).booleanValue();
            logi("clearTrafficData result = " + result);
            return result;
        }
        logi("clearTrafficData not allowed, calling app is " + callingAppName);
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) {
        int slotId = getVsimSlotId();
        logi("dsFlowCfg, slotId = " + slotId);
        if (isVSimClosedBySlotId(slotId)) {
            slotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            logi("dsFlowCfg, VSim not enabled, to mainslot: " + slotId);
        }
        String callingAppName = getCallingAppName();
        if (TextUtils.isEmpty(callingAppName)) {
            return false;
        }
        if (callingAppName.startsWith(HwVSimConstants.VSIM_PKG_NAME_CHILD_THREAD) || callingAppName.equals(HwVSimConstants.VSIM_PKG_NAME)) {
            return setApDsFlowCfg(slotId, repFlag, threshold, totalThreshold, oper);
        }
        logi("dsFlowCfg not allowed, calling app is " + callingAppName);
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int getSimStateViaSysinfoEx(int slotId) {
        return 0;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int scanVsimAvailableNetworks(int slotId, int type) {
        if (this.mNetworkScanIsRunning == 1) {
            logi("scanVsimAvailableNetworks is running");
            return 1;
        } else if (isVSimClosedBySlotId(slotId)) {
            return 0;
        } else {
            this.mNetworkScanIsRunning = 1;
            this.mNetworkScanSubId = slotId;
            this.mNetworkScanType = type;
            return this.mDefaultState.networksScan(slotId);
        }
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public String getDevSubMode(int slotId) {
        return null;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public String getPreferredNetworkTypeForVsim(int slotId) {
        logi("getPreferredNetworkTypeForVsim");
        if (!isVSimClosedBySlotId(slotId)) {
            return (String) sendRequest(27, null, slotId);
        }
        logi("getPreferredNetworkTypeForVsim VSim not enabled");
        return null;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean switchVsimWorkMode(int workMode) {
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int dialupForVSim() {
        logi("MTK platform unsupport dialupForVSim");
        return -1;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean hasVSimIccCard() {
        PhoneExt phone = getVSimPhone();
        if (phone != null) {
            return phone.hasIccCard();
        }
        loge("hasVSimIccCard phone is null");
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean isVSimCauseCardReload() {
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void setMarkForCardReload(int slotId, boolean value) {
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean needBlockPin(int slotId) {
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean needBlockUnReservedForVsim(int slotId) {
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public String getPendingDeviceInfoFromSP(String prefKey) {
        return HwVSimPhoneFactory.getPendingDeviceInfoFromSP(prefKey, getSimSlotTableLastSlotId());
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public CommandsInterfaceEx getCiBySub(int slotId) {
        return (CommandsInterfaceEx) ArrayUtils.get(this.mCis, slotId, (Object) null);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public PhoneExt getPhoneBySub(int subId) {
        return (PhoneExt) ArrayUtils.get(this.mPhones, subId, (Object) null);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int getCardPresentNumeric(boolean[] isCardPresent) {
        return 0;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void disposeCard(int index) {
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean isDoingSlotSwitch() {
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void processHotPlug(int[] cardTypes) {
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public IccCardConstantsEx.StateEx modifySimStateForVsim(int phoneId, IccCardConstantsEx.StateEx state) {
        return state;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean isProcessInit() {
        return isInSpecificState(this.mInitialState);
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean getIsSessionOpen() {
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void setIsSessionOpen(boolean isOpen) {
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void setSubActived(int slotId) {
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void updateSimCardTypes(int[] cardTypes) {
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean isSubActivationUpdate() {
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean prohibitSubUpdateSimNoChange(int slotId) {
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean getIsWaitingSwitchCdmaModeSide() {
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean getIsWaitingNvMatchUnsol() {
        return false;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void simHotPlugOut(int slotId) {
        if (isVSimEnabled()) {
            logi("simHotPlugOut slotId = " + slotId);
            setCardTypesInvalid();
        }
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void simHotPlugIn(int slotId) {
        if (isVSimEnabled()) {
            logi("simHotPlugIn slotId = " + slotId);
            setCardTypesInvalid();
        }
    }

    private void setCardTypesInvalid() {
        if (!ArrayUtils.isEmpty(this.mCardTypes)) {
            for (int i = 0; i < this.mCardTypes.length; i++) {
                this.mCardTypes[i] = -1;
            }
        }
    }

    public boolean cardTypeValid() {
        logd("cardTypeValid: cardTypes:" + Arrays.toString(this.mCardTypes));
        if (ArrayUtils.isEmpty(this.mCardTypes)) {
            return false;
        }
        for (int i = 0; i < this.mCardTypes.length; i++) {
            if (this.mCardTypes[i] == -1) {
                return false;
            }
        }
        return true;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public HwVSimSlotSwitchController.CommrilMode getCommrilMode() {
        return null;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode(int mainSlot, int[] cardType) {
        return null;
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (fd != null && pw != null && args != null) {
            super.dump(fd, pw, args);
        }
    }

    public boolean registerForVsimPhoneSwitch(IHwVSimPhoneSwitchCallback callback) {
        if (callback == null) {
            return false;
        }
        return this.mHwVsimPhoneSwitchCallbacks.add(callback);
    }

    public boolean unregisterForVsimPhoneSwitch(IHwVSimPhoneSwitchCallback callback) {
        if (callback == null) {
            return false;
        }
        return this.mHwVsimPhoneSwitchCallbacks.remove(callback);
    }

    public void notifyVsimPhoneSwitch(int phoneId) {
        if (phoneId == this.mCurrentPhoneId) {
            logi("notifyVsimPhoneSwitch, no change, return.");
            return;
        }
        logi("notifyVsimPhoneSwitch has " + this.mHwVsimPhoneSwitchCallbacks.size() + " receivers.");
        for (IHwVSimPhoneSwitchCallback callback : this.mHwVsimPhoneSwitchCallbacks) {
            try {
                callback.onVsimPhoneSwitch(phoneId);
            } catch (RemoteException e) {
                loge("notifyVsimPhoneSwitch occur an exception.");
            }
        }
        this.mCurrentPhoneId = phoneId;
    }

    private PhoneExt getVSimPhone() {
        return this.mVSimRilReceiveTask.getVSimPhone();
    }

    private boolean cmdSemAcquire() {
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

    public void cmdSemRelease() {
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

    public int calcVsimSlotId() {
        int vsimSlotId = HwVSimPhoneFactory.getVSimEnabledSubId();
        if (vsimSlotId != -1) {
            logi("calcVsimSlotId, vsim is already open, use vsimSlotId = " + vsimSlotId);
            setIsVSimOn(true);
            return vsimSlotId;
        } else if (HwVSimUtilsInner.isPlatformRealTripple()) {
            return 2;
        } else {
            syncSubState();
            int simCount = 0;
            for (int i = 0; i < this.mSubStates.length; i++) {
                if (HwTelephonyManager.getDefault().getCardType(i) != -1) {
                    simCount++;
                }
            }
            int mainSlot = HwTelephonyManager.getDefault().getDefault4GSlotId();
            int secondarySlot = HwVSimUtilsInner.getAnotherSlotId(mainSlot);
            int userReservedSlot = getUserReservedSubId();
            if (userReservedSlot == -1) {
                logi("calcVsimSlotId, use main slot instead of reserved slot");
                userReservedSlot = mainSlot;
            }
            int userNonReservedlot = HwVSimUtilsInner.getAnotherSlotId(userReservedSlot);
            int result = 3;
            if (simCount == 0) {
                result = mainSlot;
            } else if (simCount == 1) {
                result = secondarySlot;
            } else if (simCount == 2) {
                result = userNonReservedlot;
            }
            logi("calcVsimSlotId, simCount = " + simCount + ", subState = " + Arrays.toString(this.mSubStates) + ", userReservedSlot = " + userReservedSlot + ", result = " + result);
            return result;
        }
    }

    public boolean canProcessEnable() {
        logd("canProcessEnable defaultstate:" + isInSpecificState(this.mDefaultState));
        return isInSpecificState(this.mDefaultState);
    }

    public void setEnableRequest(HwVSimRequest request) {
        this.mEnableRequest = request;
    }

    public void setDisableRequest(HwVSimRequest request) {
        this.mDisableRequest = request;
    }

    public int sendVsimEvent(int slotId, int requireType, String challenge, Message response) {
        if (this.mExternalSimManagerEx == null) {
            return -1;
        }
        return this.mExternalSimManagerEx.sendVsimEvent(HwVSimUtilsInner.convertSlotId(slotId), 1, requireType, challenge, response);
    }

    public void handleMessageDone(int transactionId) {
        logd("handleMessageDone, transactionId = " + transactionId);
        ExternalSimManagerEx externalSimManagerEx = this.mExternalSimManagerEx;
        if (externalSimManagerEx != null) {
            externalSimManagerEx.handleMessageDone(transactionId);
        }
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public int getVSimOccupiedSlotId() {
        int vsimOccupiedSlotId = super.getVSimOccupiedSlotId();
        if (vsimOccupiedSlotId != -1) {
            return vsimOccupiedSlotId;
        }
        int slotId = getVsimSlotId();
        if (slotId == -1 || !HwVSimUtilsInner.isMtkPlatformTwoModems()) {
            return -1;
        }
        logd("getVSimOccupiedSlotId, slotId:" + slotId);
        return slotId;
    }

    public void backUpMobileDataEnableState() {
        for (int phoneId = 0; phoneId < HwVSimModemAdapter.PHONE_COUNT; phoneId++) {
            if (HwVSimPhoneFactory.getVSimSavedMobileData(phoneId) == -1) {
                try {
                    ContentResolver contentResolver = this.mContext.getContentResolver();
                    HwVSimPhoneFactory.setVSimSavedMobileData(phoneId, Settings.Global.getInt(contentResolver, MOBILE_DATA + phoneId));
                } catch (Settings.SettingNotFoundException e) {
                    loge("backUpMobileDataEnableState occur a SettingNotFoundException");
                }
            } else {
                logi("backUpMobileDataEnableState phoneId: " + phoneId + " is already exist, return");
            }
        }
    }

    public void restoreSavedMobileDataEnableState() {
        TelephonyManagerEx.getPhoneCount();
        int defaultValue = -1;
        for (int phoneId = 0; phoneId < HwVSimModemAdapter.PHONE_COUNT; phoneId++) {
            int savedValue = HwVSimPhoneFactory.getVSimSavedMobileData(phoneId);
            logi("restoreSavedMobileDataEnableState, try phone(" + phoneId + ") recover to " + savedValue);
            if (savedValue != -1) {
                ContentResolver contentResolver = this.mContext.getContentResolver();
                SettingsEx.Global.putInt(contentResolver, MOBILE_DATA + phoneId, savedValue);
                defaultValue = savedValue;
            }
            HwVSimPhoneFactory.setVSimSavedMobileData(phoneId, -1);
        }
        if (defaultValue != -1) {
            logi("restoreSavedMobileDataEnableState, try default recover to " + defaultValue);
            SettingsEx.Global.putInt(this.mContext.getContentResolver(), MOBILE_DATA, defaultValue);
        }
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public void broadcastVSimCardType() {
        logi("broadcastVSimCardType, card type is " + this.mCurCardType + ", old card type is " + this.mOldCardType);
        int vsimSlotId = getVsimSlotId();
        Intent intent = new Intent("com.huawei.vsim.action.VSIM_CARD_RELOAD");
        intent.putExtra("subscription", HwVSimConstants.SUB_ID_VSIM);
        intent.putExtra("phone", vsimSlotId);
        intent.putExtra("slot", vsimSlotId);
        intent.putExtra("subId", HwVSimConstants.SUB_ID_VSIM);
        intent.putExtra(HwVSimConstants.VSIM_CARDTYPE, this.mCurCardType);
        intent.putExtra(HwVSimConstants.VSIM_OLD_CARDTYPE, this.mOldCardType);
        this.mContext.sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    private static void slogi(String content) {
        HwVSimLog.info(LOG_TAG, content);
    }

    public void transitionToState(int state) {
        if (state == 0) {
            transitionTo(this.mDefaultState);
        } else if (state == 1) {
            transitionTo(this.mInitialState);
        } else if (state == 2) {
            transitionTo(this.mEnableState);
        } else if (state == 5) {
            transitionTo(this.mDisableState);
        }
    }

    private boolean isVSimClosedBySlotId(int slotId) {
        return slotId == -1 || slotId != getVSimOccupiedSlotId();
    }

    @Override // com.huawei.internal.telephony.vsim.HwVSimBaseController
    public boolean isNeedBroadcastVSimAbsentState() {
        return this.mVSimRilReceiveTask.isNeedBroadcastVSimAbsentState();
    }

    /* access modifiers changed from: private */
    /* renamed from: startOnSubscriptionsChangedListener */
    public void lambda$setUpDataOnVSimEnded$0$HwVSimMtkController(int dataSlotId, int dataSubId) {
        logi("startOnSubscriptionsChangedListener, dataSlotId = " + dataSlotId + ", dataSubId = " + dataSubId);
        this.vSimOnSubscriptionsChangedListener = new HwVSimOnSubscriptionsChangedListener(dataSlotId, dataSubId);
        SubscriptionManager subscriptionManager = (SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service");
        if (subscriptionManager != null) {
            logi("startOnSubscriptionsChangedListener, addOnSubscriptionsChangedListener");
            subscriptionManager.addOnSubscriptionsChangedListener(this.vSimOnSubscriptionsChangedListener);
        }
    }

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            HwVSimMtkController hwVSimMtkController = HwVSimMtkController.this;
            hwVSimMtkController.logi("handle message, what = " + msg.what);
            if (msg.what == 93) {
                HwVSimMtkController.this.logd("EVENT_AIRPLANE_MODE_ON");
                HwVSimMtkController.this.onAirplaneModeOn();
            }
        }
    }

    /* access modifiers changed from: private */
    public class HwVSimOnSubscriptionsChangedListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        private final AtomicInteger mPreviousSubId;
        private final AtomicInteger mSlotId;

        public HwVSimOnSubscriptionsChangedListener(int dataSlotId, int dataSubId) {
            HwVSimMtkController.this.logd("HwVSimOnSubscriptionsChangedListener init");
            this.mSlotId = new AtomicInteger(dataSlotId);
            this.mPreviousSubId = new AtomicInteger(dataSubId);
        }

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            HwVSimMtkController.this.logd("HwVSimOnSubscriptionsChangedListener SubscriptionListener.onSubscriptionInfoChanged");
            int subId = SubscriptionManagerEx.getSubIdUsingSlotId(this.mSlotId.get());
            if (subId != this.mPreviousSubId.get() && SubscriptionManagerEx.isValidSubscriptionId(subId)) {
                this.mPreviousSubId.set(subId);
                HwVSimMtkController.this.logi("subId is valid, set to dds now!");
                HwSubscriptionManager.getInstance().setDefaultDataSubIdBySlotId(this.mSlotId.get());
                SubscriptionManager subscriptionManager = (SubscriptionManager) HwVSimMtkController.this.mContext.getSystemService("telephony_subscription_service");
                if (subscriptionManager != null) {
                    subscriptionManager.removeOnSubscriptionsChangedListener(HwVSimMtkController.this.vSimOnSubscriptionsChangedListener);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class HwVSimEventHandler extends Handler {
        private static final int EVENT_VSIM_DISABLE_RETRY = 1001;

        HwVSimEventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == EVENT_VSIM_DISABLE_RETRY) {
                HwVSimMtkController.this.logd("EVENT_VSIM_DISABLE_RETRY, count = " + HwVSimMtkController.this.mDisableRetryCount);
                if (HwVSimMtkController.this.mDisableRetryCount >= 3) {
                    HwVSimMtkController.this.logd("max count, abort retry");
                    return;
                }
                HwVSimMtkController.this.mDisableRetryCount++;
                if (!HwVSimMtkController.this.mDisableFailMark) {
                    HwVSimMtkController.this.logd("no fail mark, abort retry");
                } else {
                    HwVSimMtkController.this.disableVSim();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class VsimDefaultState extends StateEx {
        HwVSimMtkDefaultProcessor mProcessor;

        private VsimDefaultState() {
        }

        public void enter() {
            HwVSimMtkController.this.logi("DefaultState: enter");
            this.mProcessor = new HwVSimMtkDefaultProcessor(HwVSimMtkController.sInstance, HwVSimMtkController.this.mModemAdapter, null);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimMtkController.this.logi("DefaultState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimMtkController hwVSimMtkController = HwVSimMtkController.this;
            hwVSimMtkController.logi("DefaultState: what = " + HwVSimMtkController.this.getWhatToString(msg.what));
            HwVSimMtkDefaultProcessor hwVSimMtkDefaultProcessor = this.mProcessor;
            if (hwVSimMtkDefaultProcessor == null) {
                return false;
            }
            return hwVSimMtkDefaultProcessor.processMessage(msg);
        }

        public int networksScan(int slotId) {
            HwVSimMtkDefaultProcessor hwVSimMtkDefaultProcessor = this.mProcessor;
            if (hwVSimMtkDefaultProcessor != null) {
                return hwVSimMtkDefaultProcessor.networksScan(slotId);
            }
            HwVSimMtkController.this.mNetworkScanIsRunning = 0;
            return 1;
        }
    }

    /* access modifiers changed from: private */
    public class InitialState extends StateEx {
        HwVSimMtkInitialProcessor mProcessor;

        private InitialState() {
        }

        public void enter() {
            HwVSimMtkController.this.logi("InitialState: enter");
            this.mProcessor = new HwVSimMtkInitialProcessor(HwVSimMtkController.sInstance, HwVSimMtkController.this.mModemAdapter, null);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimMtkController.this.logi("InitialState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimMtkController hwVSimMtkController = HwVSimMtkController.this;
            hwVSimMtkController.logi("InitialState: what = " + HwVSimMtkController.this.getWhatToString(msg.what));
            HwVSimMtkInitialProcessor hwVSimMtkInitialProcessor = this.mProcessor;
            if (hwVSimMtkInitialProcessor == null) {
                return false;
            }
            return hwVSimMtkInitialProcessor.processMessage(msg);
        }
    }

    private class VsimEnableState extends StateEx {
        HwVSimMtkEnableProcessor mProcessor;

        private VsimEnableState() {
        }

        public void enter() {
            HwVSimMtkController.this.logi("EnableState: enter");
            this.mProcessor = new HwVSimMtkEnableProcessor(HwVSimMtkController.sInstance, HwVSimMtkController.this.mModemAdapter, HwVSimMtkController.this.mEnableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimMtkController.this.logi("EnableState: exit");
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimMtkController hwVSimMtkController = HwVSimMtkController.this;
            hwVSimMtkController.logi("EnableState: what = " + HwVSimMtkController.this.getWhatToString(msg.what));
            HwVSimMtkEnableProcessor hwVSimMtkEnableProcessor = this.mProcessor;
            if (hwVSimMtkEnableProcessor == null) {
                return false;
            }
            return hwVSimMtkEnableProcessor.processMessage(msg);
        }
    }

    private class VsimDisableState extends StateEx {
        HwVSimMtkDisableProcessor mProcessor;

        private VsimDisableState() {
        }

        public void enter() {
            HwVSimMtkController.this.logi("DisableState: enter");
            disableEnterReport();
            this.mProcessor = new HwVSimMtkDisableProcessor(HwVSimMtkController.sInstance, HwVSimMtkController.this.mModemAdapter, HwVSimMtkController.this.mDisableRequest);
            this.mProcessor.onEnter();
        }

        public void exit() {
            HwVSimMtkController.this.logi("DisableState: exit");
            disableExitReport();
            this.mProcessor.onExit();
            this.mProcessor = null;
        }

        public boolean processMessage(Message msg) {
            HwVSimMtkController hwVSimMtkController = HwVSimMtkController.this;
            hwVSimMtkController.logi("DisableState: what = " + HwVSimMtkController.this.getWhatToString(msg.what));
            HwVSimMtkDisableProcessor hwVSimMtkDisableProcessor = this.mProcessor;
            if (hwVSimMtkDisableProcessor == null) {
                return false;
            }
            return hwVSimMtkDisableProcessor.processMessage(msg);
        }

        private void disableEnterReport() {
            HwVSimEventReport.VSimEventInfoUtils.setPocessType(HwVSimMtkController.this.mEventInfo, 11);
        }

        private void disableExitReport() {
            if (HwVSimMtkController.this.mDisableRequest != null) {
                boolean iResult = false;
                Object oResult = HwVSimMtkController.this.mDisableRequest.getResult();
                if (oResult != null) {
                    iResult = ((Boolean) oResult).booleanValue();
                }
                if (iResult) {
                    HwVSimEventReport.VSimEventInfoUtils.setResultType(HwVSimMtkController.this.mEventInfo, 1);
                } else {
                    HwVSimEventReport.VSimEventInfoUtils.setResultType(HwVSimMtkController.this.mEventInfo, 2);
                }
            }
            HwVSimEventReport.VSimEventInfoUtils.setPsRegTime(HwVSimMtkController.this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setSimOperator(HwVSimMtkController.this.mEventInfo, BuildConfig.FLAVOR);
            HwVSimEventReport.VSimEventInfoUtils.setSavedCommrilMode(HwVSimMtkController.this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setSavedMainSlot(HwVSimMtkController.this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setSimMode(HwVSimMtkController.this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setSlotsTable(HwVSimMtkController.this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setSavedNetworkMode(HwVSimMtkController.this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setCardPresent(HwVSimMtkController.this.mEventInfo, -1);
            HwVSimEventReport.VSimEventInfoUtils.setWorkMode(HwVSimMtkController.this.mEventInfo, -1);
            HwVSimMtkController hwVSimMtkController = HwVSimMtkController.this;
            hwVSimMtkController.reportEvent(hwVSimMtkController.mVSimEventReport, HwVSimMtkController.this.mEventInfo);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
}
