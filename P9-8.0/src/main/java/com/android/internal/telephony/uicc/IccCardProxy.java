package com.android.internal.telephony.uicc;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.PersoSubState;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccCardStatus.PinState;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class IccCardProxy extends AbstractIccCardProxy implements IccCard {
    private static final /* synthetic */ int[] -com-android-internal-telephony-IccCardConstants$StateSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues = null;
    public static final String ACTION_INTERNAL_SIM_STATE_CHANGED = "android.intent.action.internal_sim_state_changed";
    private static final boolean DBG = true;
    private static final int EVENT_APP_READY = 6;
    private static final int EVENT_CARRIER_PRIVILIGES_LOADED = 503;
    private static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 11;
    private static final int EVENT_ICC_ABSENT = 4;
    private static final int EVENT_ICC_CHANGED = 3;
    private static final int EVENT_ICC_LOCKED = 5;
    private static final int EVENT_ICC_RECORD_EVENTS = 500;
    private static final int EVENT_IMSI_READY = 8;
    private static final int EVENT_NETWORK_LOCKED = 9;
    private static final int EVENT_RADIO_OFF_OR_UNAVAILABLE = 1;
    private static final int EVENT_RADIO_ON = 2;
    private static final int EVENT_RECORDS_LOADED = 7;
    private static final int EVENT_SUBSCRIPTION_ACTIVATED = 501;
    private static final int EVENT_SUBSCRIPTION_DEACTIVATED = 502;
    private static final String LOG_TAG = "IccCardProxy";
    private RegistrantList mAbsentRegistrants = new RegistrantList();
    private CdmaSubscriptionSourceManager mCdmaSSM = null;
    private CommandsInterface mCi;
    private Context mContext;
    private int mCurrentAppType = 1;
    private State mExternalState = State.UNKNOWN;
    private IccRecords mIccRecords = null;
    private boolean mInitialized = false;
    private final Object mLock = new Object();
    private RegistrantList mNetworkLockedRegistrants = new RegistrantList();
    private Integer mPhoneId = null;
    private RegistrantList mPinLockedRegistrants = new RegistrantList();
    private boolean mQuietMode = false;
    private boolean mRadioOn = false;
    private TelephonyManager mTelephonyManager;
    private UiccCardApplication mUiccApplication = null;
    private UiccCard mUiccCard = null;
    private UiccController mUiccController = null;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues() {
        if (-com-android-internal-telephony-IccCardConstants$StateSwitchesValues != null) {
            return -com-android-internal-telephony-IccCardConstants$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ABSENT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CARD_IO_ERROR.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.CARD_RESTRICTED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DEACTIVED.ordinal()] = 16;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.NETWORK_LOCKED.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.NOT_READY.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.PERM_DISABLED.ordinal()] = 6;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.PIN_REQUIRED.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.PUK_REQUIRED.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[State.READY.ordinal()] = 9;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[State.UNKNOWN.ordinal()] = 17;
        } catch (NoSuchFieldError e11) {
        }
        -com-android-internal-telephony-IccCardConstants$StateSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues() {
        if (-com-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues != null) {
            return -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues;
        }
        int[] iArr = new int[AppState.values().length];
        try {
            iArr[AppState.APPSTATE_DETECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppState.APPSTATE_PIN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppState.APPSTATE_PUK.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppState.APPSTATE_READY.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppState.APPSTATE_SUBSCRIPTION_PERSO.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppState.APPSTATE_UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues = iArr;
        return iArr;
    }

    public IccCardProxy(Context context, CommandsInterface ci, int phoneId) {
        log("ctor: ci=" + ci + " phoneId=" + phoneId);
        this.mContext = context;
        this.mCi = ci;
        this.mPhoneId = Integer.valueOf(phoneId);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(context, ci, this, 11, null);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 3, null);
        ci.registerForOn(this, 2, null);
        ci.registerForOffOrNotAvailable(this, 1, null);
        resetProperties();
        setExternalState(State.NOT_READY, false);
    }

    public void dispose() {
        synchronized (this.mLock) {
            log("Disposing");
            this.mUiccController.unregisterForIccChanged(this);
            this.mUiccController = null;
            this.mCi.unregisterForOn(this);
            this.mCi.unregisterForOffOrNotAvailable(this);
            this.mCdmaSSM.dispose(this);
        }
    }

    public void setVoiceRadioTech(int radioTech) {
        synchronized (this.mLock) {
            log("Setting radio tech " + ServiceState.rilRadioTechnologyToString(radioTech));
            if (ServiceState.isGsm(radioTech)) {
                this.mCurrentAppType = 1;
            } else {
                this.mCurrentAppType = 2;
            }
            updateQuietMode();
            updateActiveRecord();
        }
    }

    private void updateActiveRecord() {
        log("updateActiveRecord app type = " + this.mCurrentAppType + "mIccRecords = " + this.mIccRecords);
        if (this.mIccRecords != null) {
            if (this.mCurrentAppType == 2) {
                if (this.mCdmaSSM.getCdmaSubscriptionSource() == 0) {
                    log("Setting Ruim Record as active");
                    this.mIccRecords.recordsRequired();
                }
            } else if (this.mCurrentAppType == 1) {
                log("Setting SIM Record as active");
                this.mIccRecords.recordsRequired();
            }
        }
    }

    private void updateQuietMode() {
        synchronized (this.mLock) {
            boolean newQuietMode;
            int cdmaSource = -1;
            if (this.mCurrentAppType == 1) {
                newQuietMode = false;
                log("updateQuietMode: 3GPP subscription -> newQuietMode=" + false);
            } else {
                cdmaSource = this.mCdmaSSM != null ? this.mCdmaSSM.getCdmaSubscriptionSource() : -1;
                newQuietMode = cdmaSource == 1 ? this.mCurrentAppType == 2 : false;
            }
            if (!this.mQuietMode && newQuietMode) {
                log("Switching to QuietMode.");
                setExternalState(State.READY);
                this.mQuietMode = newQuietMode;
            } else if (!this.mQuietMode || newQuietMode) {
                log("updateQuietMode: no changes don't setExternalState");
            } else {
                log("updateQuietMode: Switching out from QuietMode. Force broadcast of current state=" + this.mExternalState);
                this.mQuietMode = newQuietMode;
                setExternalState(this.mExternalState, true);
            }
            log("updateQuietMode: QuietMode is " + this.mQuietMode + " (app_type=" + this.mCurrentAppType + " cdmaSource=" + cdmaSource + ")");
            this.mInitialized = true;
            sendMessage(obtainMessage(3, new AsyncResult(null, this.mPhoneId, null)));
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mRadioOn = false;
                if (RadioState.RADIO_UNAVAILABLE == this.mCi.getRadioState()) {
                    setExternalState(State.NOT_READY);
                    break;
                }
                break;
            case 2:
                this.mRadioOn = true;
                if (!this.mInitialized) {
                    updateQuietMode();
                    break;
                }
                break;
            case 3:
                Integer index = getUiccIndex(msg);
                if ((index == null || (index.equals(this.mPhoneId) ^ 1) == 0) && this.mInitialized) {
                    updateIccAvailability();
                    break;
                }
            case 4:
                this.mAbsentRegistrants.notifyRegistrants();
                setExternalState(State.ABSENT);
                break;
            case 5:
                processLockedState();
                break;
            case 6:
                setExternalState(State.READY);
                break;
            case 7:
                if (this.mIccRecords != null) {
                    String operator = this.mIccRecords.getOperatorNumeric();
                    log("operator=" + operator + " mPhoneId=" + this.mPhoneId);
                    if (TextUtils.isEmpty(operator) || operator.length() < 3) {
                        loge("EVENT_RECORDS_LOADED Operator name is null");
                    } else {
                        log("update icc_operator_numeric=" + operator);
                        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mPhoneId.intValue(), operator);
                        String countryCode = operator.substring(0, 3);
                        if (countryCode != null) {
                            try {
                                this.mTelephonyManager.setSimCountryIsoForPhone(this.mPhoneId.intValue(), MccTable.countryCodeForMcc(Integer.parseInt(countryCode)));
                            } catch (Exception e) {
                                loge("countryCodeForMcc error for countryCode = " + countryCode);
                            }
                        } else {
                            loge("EVENT_RECORDS_LOADED Country code is null");
                        }
                    }
                }
                if (this.mUiccCard != null && (this.mUiccCard.areCarrierPriviligeRulesLoaded() ^ 1) != 0) {
                    this.mUiccCard.registerForCarrierPrivilegeRulesLoaded(this, 503, null);
                    break;
                } else {
                    onRecordsLoaded();
                    break;
                }
                break;
            case 8:
                broadcastIccStateChangedIntent("IMSI", null);
                break;
            case 9:
                this.mNetworkLockedRegistrants.notifyRegistrants();
                setExternalState(State.NETWORK_LOCKED);
                break;
            case 11:
                updateQuietMode();
                updateActiveRecord();
                break;
            case EVENT_ICC_RECORD_EVENTS /*500*/:
                if (this.mCurrentAppType == 1 && this.mIccRecords != null && ((Integer) msg.obj.result).intValue() == 2) {
                    this.mTelephonyManager.setSimOperatorNameForPhone(this.mPhoneId.intValue(), this.mIccRecords.getServiceProviderName());
                    break;
                }
            case 501:
                log("EVENT_SUBSCRIPTION_ACTIVATED");
                onSubscriptionActivated();
                break;
            case 502:
                log("EVENT_SUBSCRIPTION_DEACTIVATED");
                onSubscriptionDeactivated();
                break;
            case 503:
                log("EVENT_CARRIER_PRIVILEGES_LOADED");
                if (this.mUiccCard != null) {
                    this.mUiccCard.unregisterForCarrierPrivilegeRulesLoaded(this);
                }
                onRecordsLoaded();
                break;
            default:
                loge("Unhandled message with number: " + msg.what);
                handleMessageExtend(msg);
                break;
        }
        handleCustMessage(msg);
    }

    private void onSubscriptionActivated() {
        updateIccAvailability();
        updateStateProperty();
    }

    private void onSubscriptionDeactivated() {
        resetProperties();
        updateIccAvailability();
        updateStateProperty();
    }

    private void onRecordsLoaded() {
        broadcastIccStateChangedIntent("LOADED", null);
    }

    /* JADX WARNING: Missing block: B:18:0x00be, code:
            if (r7.mUiccCard != r1) goto L_0x009c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateIccAvailability() {
        synchronized (this.mLock) {
            UiccCard newCard = this.mUiccController.getUiccCard(this.mPhoneId.intValue());
            CardState state = CardState.CARDSTATE_ABSENT;
            UiccCardApplication newApp = null;
            IccRecords newRecords = null;
            if (newCard != null) {
                log("mCurrentAppType = " + this.mCurrentAppType + ", mPhoneId = " + this.mPhoneId);
                this.mCurrentAppType = processCurrentAppType(newCard, this.mCurrentAppType, this.mPhoneId.intValue());
                log("mCurrentAppType = " + this.mCurrentAppType);
                state = newCard.getCardState();
                newApp = newCard.getApplication(this.mCurrentAppType);
                log("state = " + state + ", newApp = " + newApp);
                if (newApp != null) {
                    newRecords = newApp.getIccRecords();
                }
            }
            if (this.mIccRecords == newRecords && this.mUiccApplication == newApp) {
            }
            log("Icc changed. Reregestering.");
            unregisterUiccCardEvents();
            this.mUiccCard = newCard;
            this.mUiccApplication = newApp;
            setUiccApplication(newApp);
            queryFdn();
            this.mIccRecords = newRecords;
            registerUiccCardEvents();
            updateActiveRecord();
            updateExternalState();
        }
    }

    void resetProperties() {
        if (this.mCurrentAppType == 1) {
            log("update icc_operator_numeric=");
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mPhoneId.intValue(), "");
            this.mTelephonyManager.setSimCountryIsoForPhone(this.mPhoneId.intValue(), "");
            this.mTelephonyManager.setSimOperatorNameForPhone(this.mPhoneId.intValue(), "");
        }
    }

    private void HandleDetectedState() {
    }

    private void updateExternalState() {
        if (this.mUiccCard == null) {
            if (!VSimUtilsInner.isPlatformTwoModems() || this.mCi == null || (this.mCi.isRadioAvailable() ^ 1) == 0) {
                setExternalState(State.NOT_READY);
                return;
            }
            log("[2Cards] update the state to absent");
            setExternalState(State.ABSENT);
        } else if (this.mUiccCard.getCardState() == CardState.CARDSTATE_ABSENT) {
            if (this.mRadioOn) {
                setExternalState(State.ABSENT);
            } else if (isSimAbsent(this.mContext, this.mUiccCard, this.mRadioOn)) {
                setExternalState(State.ABSENT);
                log("updateExternalState ABSENT");
            } else {
                setExternalState(State.NOT_READY);
            }
        } else if (this.mUiccCard.getCardState() == CardState.CARDSTATE_ERROR) {
            setExternalState(State.CARD_IO_ERROR);
        } else if (this.mUiccCard.getCardState() == CardState.CARDSTATE_RESTRICTED) {
            setExternalState(State.CARD_RESTRICTED);
        } else if (this.mUiccApplication == null) {
            setExternalState(State.NOT_READY);
        } else {
            switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues()[this.mUiccApplication.getState().ordinal()]) {
                case 1:
                    HandleDetectedState();
                    break;
                case 2:
                    setExternalState(State.PIN_REQUIRED);
                    break;
                case 3:
                    if (!this.mUiccApplication.getPin1State().isPermBlocked()) {
                        setExternalState(State.PUK_REQUIRED);
                        break;
                    } else {
                        setExternalState(State.PERM_DISABLED);
                        return;
                    }
                case 4:
                    setExternalState(State.READY);
                    break;
                case 5:
                    if (this.mUiccApplication.getPersoSubState() != PersoSubState.PERSOSUBSTATE_SIM_NETWORK) {
                        if (this.mUiccApplication.getPersoSubState() != PersoSubState.PERSOSUBSTATE_READY && this.mUiccApplication.getPersoSubState() != PersoSubState.PERSOSUBSTATE_UNKNOWN) {
                            custSetExternalState(this.mUiccApplication.getPersoSubState());
                            this.mExternalState = State.NETWORK_LOCKED;
                            loge("updateExternalState: set mPhoneId=" + this.mPhoneId + " mExternalState=" + this.mExternalState);
                            break;
                        }
                        setExternalState(State.UNKNOWN);
                        custResetExternalState(State.UNKNOWN);
                        break;
                    }
                    setExternalState(State.NETWORK_LOCKED);
                    break;
                    break;
                case 6:
                    setExternalState(State.UNKNOWN);
                    break;
            }
            custUpdateExternalState(getState());
        }
    }

    private void registerUiccCardEvents() {
        if (this.mUiccCard != null) {
            this.mUiccCard.registerForAbsent(this, 4, null);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.registerForReady(this, 6, null);
            this.mUiccApplication.registerForLocked(this, 5, null);
            this.mUiccApplication.registerForNetworkLocked(this, 9, null);
            registerForFdnStatusChange(this);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.registerForImsiReady(this, 8, null);
            this.mIccRecords.registerForRecordsLoaded(this, 7, null);
            this.mIccRecords.registerForRecordsEvents(this, EVENT_ICC_RECORD_EVENTS, null);
        }
        registerUiccCardEventsExtend();
    }

    private void unregisterUiccCardEvents() {
        if (this.mUiccCard != null) {
            this.mUiccCard.unregisterForAbsent(this);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.unregisterForReady(this);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.unregisterForLocked(this);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.unregisterForNetworkLocked(this);
        }
        unregisterForFdnStatusChange(this);
        if (this.mIccRecords != null) {
            this.mIccRecords.unregisterForImsiReady(this);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.unregisterForRecordsLoaded(this);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.unregisterForRecordsEvents(this);
        }
        unregisterUiccCardEventsExtend();
    }

    private void updateStateProperty() {
        this.mTelephonyManager.setSimStateForPhone(this.mPhoneId.intValue(), getState().toString());
    }

    private void broadcastIccStateChangedIntent(String value, String reason) {
        synchronized (this.mLock) {
            if (this.mPhoneId == null || (SubscriptionManager.isValidSlotIndex(this.mPhoneId.intValue()) ^ 1) != 0) {
                loge("broadcastIccStateChangedIntent: mPhoneId=" + this.mPhoneId + " is invalid; Return!!");
            } else if (this.mQuietMode) {
                log("broadcastIccStateChangedIntent: QuietMode NOT Broadcasting intent ACTION_SIM_STATE_CHANGED  value=" + value + " reason=" + reason);
            } else {
                Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
                intent.addFlags(67108864);
                intent.putExtra("phoneName", "Phone");
                intent.putExtra("ss", value);
                intent.putExtra("reason", reason);
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhoneId.intValue());
                VSimUtilsInner.putVSimExtraForIccStateChanged(intent, this.mPhoneId.intValue(), value);
                log("broadcastIccStateChangedIntent intent ACTION_SIM_STATE_CHANGED value=" + value + " reason=" + reason + " for mPhoneId=" + this.mPhoneId);
                ActivityManager.broadcastStickyIntent(intent, -1);
            }
        }
    }

    /* JADX WARNING: Missing block: B:32:0x00cc, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setExternalState(State newState, boolean override) {
        synchronized (this.mLock) {
            if (this.mPhoneId == null || (SubscriptionManager.isValidSlotIndex(this.mPhoneId.intValue()) ^ 1) != 0) {
                loge("setExternalState: mPhoneId=" + this.mPhoneId + " is invalid; Return!!");
                return;
            }
            if (!override) {
                if (newState == this.mExternalState) {
                    loge("setExternalState: !override and newstate unchanged from " + newState);
                    return;
                }
            }
            if (State.ABSENT == newState || State.NOT_READY == newState) {
                custResetExternalState(newState);
            }
            if (blockPinStateForDualCards(newState)) {
                return;
            }
            this.mExternalState = modifySimStateForVsim(this.mPhoneId.intValue(), newState);
            loge("setExternalState: set mPhoneId=" + this.mPhoneId + " mExternalState=" + this.mExternalState);
            this.mTelephonyManager.setSimStateForPhone(this.mPhoneId.intValue(), getState().toString());
            processSimLockStateForCT();
            broadcastIccStateChangedIntent(getIccStateIntentString(this.mExternalState), getIccStateReason(this.mExternalState));
            if (State.ABSENT == this.mExternalState) {
                this.mAbsentRegistrants.notifyRegistrants();
            }
        }
    }

    public boolean blockPinStateForDualCards(State s) {
        log("blockPinStateForDualCards s = " + s + ", mPhoneId " + this.mPhoneId);
        if (s.isPinLocked()) {
            boolean mNeedSwitchCommrilMode;
            if (!HwTelephonyFactory.getHwUiccManager().isFullNetworkSupported()) {
                mNeedSwitchCommrilMode = false;
            } else if (HwTelephonyFactory.getHwUiccManager().getCommrilMode()) {
                mNeedSwitchCommrilMode = true;
            } else {
                mNeedSwitchCommrilMode = HwTelephonyFactory.getHwUiccManager().isRestartingRild();
            }
            boolean mBlockPinForeignUsim = false;
            if (this.IS_CHINA_TELECOM ? SystemProperties.getBoolean("persist.radio.supp_oversea_usim", false) : false) {
                mBlockPinForeignUsim = HwTelephonyFactory.getHwUiccManager().getSwitchTag();
            }
            if (VSimUtilsInner.isVSimInProcess()) {
                if (VSimUtilsInner.needBlockPin(this.mPhoneId.intValue())) {
                    log("vsim block pin for phone id " + this.mPhoneId);
                    return true;
                }
                log("vsim no need block pin for phone id " + this.mPhoneId + ", just pass");
            } else if ((SystemProperties.getBoolean("persist.sys.dualcards", false) || SystemProperties.getBoolean("ro.config.full_network_support", false)) && (HwTelephonyFactory.getHwUiccManager().getSwitchingSlot() || mNeedSwitchCommrilMode || mBlockPinForeignUsim)) {
                log("setExternalState getWaitingSwitchBalongSlot is true, so return");
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:17:0x002e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processLockedState() {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                if (this.mUiccApplication.getPin1State() != PinState.PINSTATE_ENABLED_PERM_BLOCKED) {
                    switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues()[this.mUiccApplication.getState().ordinal()]) {
                        case 2:
                            this.mPinLockedRegistrants.notifyRegistrants();
                            setExternalState(State.PIN_REQUIRED);
                            break;
                        case 3:
                            setExternalState(State.PUK_REQUIRED);
                            break;
                    }
                }
                setExternalState(State.PERM_DISABLED);
                return;
            }
        }
    }

    private void setExternalState(State newState) {
        setExternalState(newState, false);
    }

    public boolean getIccRecordsLoaded() {
        synchronized (this.mLock) {
            if (this.mIccRecords != null) {
                boolean recordsLoaded = this.mIccRecords.getRecordsLoaded();
                return recordsLoaded;
            }
            return false;
        }
    }

    private String getIccStateIntentString(State state) {
        switch (-getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues()[state.ordinal()]) {
            case 1:
                return "ABSENT";
            case 2:
                return "CARD_IO_ERROR";
            case 3:
                return "CARD_RESTRICTED";
            case 4:
                return "LOCKED";
            case 5:
                return "NOT_READY";
            case 6:
                return "LOCKED";
            case 7:
                return "LOCKED";
            case 8:
                return "LOCKED";
            case 9:
                return "READY";
            default:
                return "UNKNOWN";
        }
    }

    private String getIccStateReason(State state) {
        switch (-getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues()[state.ordinal()]) {
            case 2:
                return "CARD_IO_ERROR";
            case 3:
                return "CARD_RESTRICTED";
            case 4:
                return "NETWORK";
            case 6:
                return "PERM_DISABLED";
            case 7:
                return "PIN";
            case 8:
                return "PUK";
            default:
                return null;
        }
    }

    public State getState() {
        if (!VSimUtilsInner.isPlatformTwoModems() || this.mCi == null || (this.mCi.isRadioAvailable() ^ 1) == 0) {
            State state;
            synchronized (this.mLock) {
                state = this.mExternalState;
            }
            return state;
        }
        log("[2Cards]pending sub" + this.mPhoneId + " getState return ABSENT!");
        return State.ABSENT;
    }

    public IccRecords getIccRecords() {
        IccRecords iccRecords;
        synchronized (this.mLock) {
            iccRecords = this.mIccRecords;
        }
        return iccRecords;
    }

    public IccFileHandler getIccFileHandler() {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                IccFileHandler iccFileHandler = this.mUiccApplication.getIccFileHandler();
                return iccFileHandler;
            }
            return null;
        }
    }

    public void registerForAbsent(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mAbsentRegistrants.add(r);
            if (getState() == State.ABSENT) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForAbsent(Handler h) {
        synchronized (this.mLock) {
            this.mAbsentRegistrants.remove(h);
        }
    }

    public void registerForNetworkLocked(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mNetworkLockedRegistrants.add(r);
            if (getState() == State.NETWORK_LOCKED) {
                r.notifyRegistrant();
            }
            custRegisterForNetworkLocked(h, what, obj);
        }
    }

    public void unregisterForNetworkLocked(Handler h) {
        synchronized (this.mLock) {
            this.mNetworkLockedRegistrants.remove(h);
            custUnregisterForNetworkLocked(h);
        }
    }

    public void registerForLocked(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mPinLockedRegistrants.add(r);
            if (getState().isPinLocked()) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForLocked(Handler h) {
        synchronized (this.mLock) {
            this.mPinLockedRegistrants.remove(h);
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyPin(String pin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPin(pin, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyPuk(String puk, String newPin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPuk(puk, newPin, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyPin2(String pin2, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPin2(pin2, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyPuk2(String puk2, String newPin2, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPuk2(puk2, newPin2, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyNetworkDepersonalization(String pin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyNetworkDepersonalization(pin, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("CommandsInterface is not set.");
                onComplete.sendToTarget();
            }
        }
    }

    public boolean getIccLockEnabled() {
        boolean booleanValue;
        synchronized (this.mLock) {
            booleanValue = Boolean.valueOf(this.mUiccApplication != null ? this.mUiccApplication.getIccLockEnabled() : false).booleanValue();
        }
        return booleanValue;
    }

    public boolean getIccFdnEnabled() {
        boolean booleanValue;
        synchronized (this.mLock) {
            booleanValue = Boolean.valueOf(this.mUiccApplication != null ? this.mUiccApplication.getIccFdnEnabled() : false).booleanValue();
        }
        return booleanValue;
    }

    public boolean getIccFdnAvailable() {
        return this.mUiccApplication != null ? this.mUiccApplication.getIccFdnAvailable() : false;
    }

    public boolean getIccPin2Blocked() {
        return Boolean.valueOf(this.mUiccApplication != null ? this.mUiccApplication.getIccPin2Blocked() : false).booleanValue();
    }

    public boolean getIccPuk2Blocked() {
        return Boolean.valueOf(this.mUiccApplication != null ? this.mUiccApplication.getIccPuk2Blocked() : false).booleanValue();
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setIccLockEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.setIccLockEnabled(enabled, password, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setIccFdnEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.setIccFdnEnabled(enabled, password, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void changeIccLockPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.changeIccLockPassword(oldPassword, newPassword, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void changeIccFdnPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.changeIccFdnPassword(oldPassword, newPassword, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
            }
        }
    }

    public String getServiceProviderName() {
        synchronized (this.mLock) {
            if (this.mIccRecords != null) {
                String serviceProviderName = this.mIccRecords.getServiceProviderName();
                return serviceProviderName;
            }
            return null;
        }
    }

    public boolean isApplicationOnIcc(AppType type) {
        boolean booleanValue;
        synchronized (this.mLock) {
            booleanValue = Boolean.valueOf(this.mUiccCard != null ? this.mUiccCard.isApplicationOnIcc(type) : false).booleanValue();
        }
        return booleanValue;
    }

    public boolean hasIccCard() {
        synchronized (this.mLock) {
            if (this.mUiccCard == null || this.mUiccCard.getCardState() == CardState.CARDSTATE_ABSENT) {
                return false;
            }
            return true;
        }
    }

    private void setSystemProperty(String property, String value) {
        TelephonyManager.setTelephonyProperty(this.mPhoneId.intValue(), property, value);
    }

    public IccRecords getIccRecord() {
        return this.mIccRecords;
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("IccCardProxy: " + this);
        pw.println(" mContext=" + this.mContext);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mAbsentRegistrants: size=" + this.mAbsentRegistrants.size());
        for (i = 0; i < this.mAbsentRegistrants.size(); i++) {
            pw.println("  mAbsentRegistrants[" + i + "]=" + ((Registrant) this.mAbsentRegistrants.get(i)).getHandler());
        }
        pw.println(" mPinLockedRegistrants: size=" + this.mPinLockedRegistrants.size());
        for (i = 0; i < this.mPinLockedRegistrants.size(); i++) {
            pw.println("  mPinLockedRegistrants[" + i + "]=" + ((Registrant) this.mPinLockedRegistrants.get(i)).getHandler());
        }
        pw.println(" mNetworkLockedRegistrants: size=" + this.mNetworkLockedRegistrants.size());
        for (i = 0; i < this.mNetworkLockedRegistrants.size(); i++) {
            pw.println("  mNetworkLockedRegistrants[" + i + "]=" + ((Registrant) this.mNetworkLockedRegistrants.get(i)).getHandler());
        }
        pw.println(" mCurrentAppType=" + this.mCurrentAppType);
        pw.println(" mUiccController=" + this.mUiccController);
        pw.println(" mUiccCard=" + this.mUiccCard);
        pw.println(" mUiccApplication=" + this.mUiccApplication);
        pw.println(" mIccRecords=" + this.mIccRecords);
        pw.println(" mCdmaSSM=" + this.mCdmaSSM);
        pw.println(" mRadioOn=" + this.mRadioOn);
        pw.println(" mQuietMode=" + this.mQuietMode);
        pw.println(" mInitialized=" + this.mInitialized);
        pw.println(" mExternalState=" + this.mExternalState);
        pw.flush();
    }

    public int getPhoneIdHw() {
        return this.mPhoneId.intValue();
    }

    public CommandsInterface getCiHw() {
        return this.mCi;
    }

    public static int getEventRadioOffOrUnavailableHw() {
        return 1;
    }

    public static int getEventAppReadyHw() {
        return 6;
    }

    public UiccCard getUiccCardHw() {
        return this.mUiccCard;
    }

    public IccRecords getIccRecordsHw() {
        return this.mIccRecords;
    }

    public void setRadioOnHw(boolean value) {
        this.mRadioOn = value;
    }

    public void registerUiccCardEventsHw() {
        registerUiccCardEvents();
    }

    public void unregisterUiccCardEventsHw() {
        unregisterUiccCardEvents();
    }

    public void broadcastIccStateChangedIntentHw(String value, String reason) {
        broadcastIccStateChangedIntent(value, reason);
    }

    public void setExternalStateHw(State newState) {
        setExternalState(newState);
    }

    public String getIccStateIntentStringHw(State state) {
        return getIccStateIntentString(state);
    }
}
