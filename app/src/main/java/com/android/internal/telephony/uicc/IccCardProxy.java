package com.android.internal.telephony.uicc;

import android.app.ActivityManagerNative;
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
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.TelephonyEventLog;
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
    private RegistrantList mAbsentRegistrants;
    private CdmaSubscriptionSourceManager mCdmaSSM;
    private CommandsInterface mCi;
    private Context mContext;
    private int mCurrentAppType;
    private State mExternalState;
    private IccRecords mIccRecords;
    private boolean mInitialized;
    private final Object mLock;
    private RegistrantList mNetworkLockedRegistrants;
    private Integer mPhoneId;
    private RegistrantList mPinLockedRegistrants;
    private boolean mQuietMode;
    private boolean mRadioOn;
    private TelephonyManager mTelephonyManager;
    private UiccCardApplication mUiccApplication;
    private UiccCard mUiccCard;
    private UiccController mUiccController;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues() {
        if (-com-android-internal-telephony-IccCardConstants$StateSwitchesValues != null) {
            return -com-android-internal-telephony-IccCardConstants$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ABSENT.ordinal()] = EVENT_RADIO_OFF_OR_UNAVAILABLE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CARD_IO_ERROR.ordinal()] = EVENT_RADIO_ON;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DEACTIVED.ordinal()] = 15;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.NETWORK_LOCKED.ordinal()] = EVENT_ICC_CHANGED;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.NOT_READY.ordinal()] = EVENT_ICC_ABSENT;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.PERM_DISABLED.ordinal()] = EVENT_ICC_LOCKED;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.PIN_REQUIRED.ordinal()] = EVENT_APP_READY;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.PUK_REQUIRED.ordinal()] = EVENT_RECORDS_LOADED;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.READY.ordinal()] = EVENT_IMSI_READY;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[State.UNKNOWN.ordinal()] = 16;
        } catch (NoSuchFieldError e10) {
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
            iArr[AppState.APPSTATE_DETECTED.ordinal()] = EVENT_RADIO_OFF_OR_UNAVAILABLE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppState.APPSTATE_PIN.ordinal()] = EVENT_RADIO_ON;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppState.APPSTATE_PUK.ordinal()] = EVENT_ICC_CHANGED;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppState.APPSTATE_READY.ordinal()] = EVENT_ICC_ABSENT;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppState.APPSTATE_SUBSCRIPTION_PERSO.ordinal()] = EVENT_ICC_LOCKED;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppState.APPSTATE_UNKNOWN.ordinal()] = EVENT_APP_READY;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues = iArr;
        return iArr;
    }

    public IccCardProxy(Context context, CommandsInterface ci, int phoneId) {
        this.mPhoneId = null;
        this.mLock = new Object();
        this.mAbsentRegistrants = new RegistrantList();
        this.mPinLockedRegistrants = new RegistrantList();
        this.mNetworkLockedRegistrants = new RegistrantList();
        this.mCurrentAppType = EVENT_RADIO_OFF_OR_UNAVAILABLE;
        this.mUiccController = null;
        this.mUiccCard = null;
        this.mUiccApplication = null;
        this.mIccRecords = null;
        this.mCdmaSSM = null;
        this.mRadioOn = false;
        this.mQuietMode = false;
        this.mInitialized = false;
        this.mExternalState = State.UNKNOWN;
        log("ctor: ci=" + ci + " phoneId=" + phoneId);
        this.mContext = context;
        this.mCi = ci;
        this.mPhoneId = Integer.valueOf(phoneId);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(context, ci, this, EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED, null);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, EVENT_ICC_CHANGED, null);
        ci.registerForOn(this, EVENT_RADIO_ON, null);
        ci.registerForOffOrNotAvailable(this, EVENT_RADIO_OFF_OR_UNAVAILABLE, null);
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
                this.mCurrentAppType = EVENT_RADIO_OFF_OR_UNAVAILABLE;
            } else {
                this.mCurrentAppType = EVENT_RADIO_ON;
            }
            updateQuietMode();
            updateActiveRecord();
        }
    }

    private void updateActiveRecord() {
        log("updateActiveRecord app type = " + this.mCurrentAppType + "mIccRecords = " + this.mIccRecords);
        if (this.mIccRecords != null) {
            if (this.mCurrentAppType == EVENT_RADIO_ON) {
                if (this.mCdmaSSM.getCdmaSubscriptionSource() == 0) {
                    log("Setting Ruim Record as active");
                    this.mIccRecords.recordsRequired();
                }
            } else if (this.mCurrentAppType == EVENT_RADIO_OFF_OR_UNAVAILABLE) {
                log("Setting SIM Record as active");
                this.mIccRecords.recordsRequired();
            }
        }
    }

    private void updateQuietMode() {
        synchronized (this.mLock) {
            boolean newQuietMode;
            int cdmaSource = -1;
            if (this.mCurrentAppType == EVENT_RADIO_OFF_OR_UNAVAILABLE) {
                newQuietMode = false;
                log("updateQuietMode: 3GPP subscription -> newQuietMode=" + false);
            } else {
                cdmaSource = this.mCdmaSSM != null ? this.mCdmaSSM.getCdmaSubscriptionSource() : -1;
                newQuietMode = cdmaSource == EVENT_RADIO_OFF_OR_UNAVAILABLE ? this.mCurrentAppType == EVENT_RADIO_ON ? DBG : false : false;
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
                setExternalState(this.mExternalState, DBG);
            }
            log("updateQuietMode: QuietMode is " + this.mQuietMode + " (app_type=" + this.mCurrentAppType + " cdmaSource=" + cdmaSource + ")");
            this.mInitialized = DBG;
            sendMessage(obtainMessage(EVENT_ICC_CHANGED, new AsyncResult(null, this.mPhoneId, null)));
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_RADIO_OFF_OR_UNAVAILABLE /*1*/:
                this.mRadioOn = false;
                if (RadioState.RADIO_UNAVAILABLE == this.mCi.getRadioState()) {
                    setExternalState(State.NOT_READY);
                    break;
                }
                break;
            case EVENT_RADIO_ON /*2*/:
                this.mRadioOn = DBG;
                if (!this.mInitialized) {
                    updateQuietMode();
                    break;
                }
                break;
            case EVENT_ICC_CHANGED /*3*/:
                Integer index = getUiccIndex(msg);
                if ((index == null || index.equals(this.mPhoneId)) && this.mInitialized) {
                    updateIccAvailability();
                    break;
                }
            case EVENT_ICC_ABSENT /*4*/:
                this.mAbsentRegistrants.notifyRegistrants();
                setExternalState(State.ABSENT);
                break;
            case EVENT_ICC_LOCKED /*5*/:
                processLockedState();
                break;
            case EVENT_APP_READY /*6*/:
                setExternalState(State.READY);
                break;
            case EVENT_RECORDS_LOADED /*7*/:
                if (this.mIccRecords != null) {
                    String operator = this.mIccRecords.getOperatorNumeric();
                    log("operator=" + operator + " mPhoneId=" + this.mPhoneId);
                    if (operator == null || operator.length() < EVENT_ICC_CHANGED) {
                        loge("EVENT_RECORDS_LOADED Operator name is null");
                    } else {
                        log("update icc_operator_numeric=" + operator);
                        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mPhoneId.intValue(), operator);
                        String countryCode = operator.substring(0, EVENT_ICC_CHANGED);
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
                if (this.mUiccCard != null && !this.mUiccCard.areCarrierPriviligeRulesLoaded()) {
                    this.mUiccCard.registerForCarrierPrivilegeRulesLoaded(this, EVENT_CARRIER_PRIVILIGES_LOADED, null);
                    break;
                } else {
                    onRecordsLoaded();
                    break;
                }
                break;
            case EVENT_IMSI_READY /*8*/:
                broadcastIccStateChangedIntent("IMSI", null);
                break;
            case EVENT_NETWORK_LOCKED /*9*/:
                this.mNetworkLockedRegistrants.notifyRegistrants();
                setExternalState(State.NETWORK_LOCKED);
                break;
            case EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED /*11*/:
                updateQuietMode();
                updateActiveRecord();
                break;
            case EVENT_ICC_RECORD_EVENTS /*500*/:
                if (this.mCurrentAppType == EVENT_RADIO_OFF_OR_UNAVAILABLE && this.mIccRecords != null && ((Integer) msg.obj.result).intValue() == EVENT_RADIO_ON) {
                    this.mTelephonyManager.setSimOperatorNameForPhone(this.mPhoneId.intValue(), this.mIccRecords.getServiceProviderName());
                    break;
                }
            case EVENT_SUBSCRIPTION_ACTIVATED /*501*/:
                log("EVENT_SUBSCRIPTION_ACTIVATED");
                onSubscriptionActivated();
                break;
            case EVENT_SUBSCRIPTION_DEACTIVATED /*502*/:
                log("EVENT_SUBSCRIPTION_DEACTIVATED");
                onSubscriptionDeactivated();
                break;
            case EVENT_CARRIER_PRIVILIGES_LOADED /*503*/:
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

    private void updateIccAvailability() {
        synchronized (this.mLock) {
            UiccCard newCard = this.mUiccController.getUiccCard(this.mPhoneId.intValue());
            CardState state = CardState.CARDSTATE_ABSENT;
            UiccCardApplication uiccCardApplication = null;
            IccRecords newRecords = null;
            if (newCard != null) {
                log("mCurrentAppType = " + this.mCurrentAppType + ", mPhoneId = " + this.mPhoneId);
                this.mCurrentAppType = processCurrentAppType(newCard, this.mCurrentAppType, this.mPhoneId.intValue());
                log("mCurrentAppType = " + this.mCurrentAppType);
                state = newCard.getCardState();
                uiccCardApplication = newCard.getApplication(this.mCurrentAppType);
                log("state = " + state + ", newApp = " + uiccCardApplication);
                if (uiccCardApplication != null) {
                    newRecords = uiccCardApplication.getIccRecords();
                }
            }
            log("mIccRecords[old, new] = [" + this.mIccRecords + " , " + newRecords + "]");
            log("mUiccApplication[old, new] = [" + this.mUiccApplication + " , " + uiccCardApplication + "]");
            log("mUiccCard[old, new] = [" + this.mUiccCard + " , " + newCard + "]");
            if (this.mIccRecords == newRecords && this.mUiccApplication == uiccCardApplication) {
                if (this.mUiccCard != newCard) {
                }
                updateExternalState();
            }
            log("Icc changed. Reregestering.");
            unregisterUiccCardEvents();
            this.mUiccCard = newCard;
            this.mUiccApplication = uiccCardApplication;
            setUiccApplication(uiccCardApplication);
            queryFdn();
            this.mIccRecords = newRecords;
            registerUiccCardEvents();
            updateActiveRecord();
            updateExternalState();
        }
    }

    void resetProperties() {
        if (this.mCurrentAppType == EVENT_RADIO_OFF_OR_UNAVAILABLE) {
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
            if (!VSimUtilsInner.isPlatformTwoModems() || this.mCi == null || this.mCi.isRadioAvailable()) {
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
        } else if (this.mUiccApplication == null) {
            setExternalState(State.NOT_READY);
        } else {
            switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues()[this.mUiccApplication.getState().ordinal()]) {
                case EVENT_RADIO_OFF_OR_UNAVAILABLE /*1*/:
                    HandleDetectedState();
                    break;
                case EVENT_RADIO_ON /*2*/:
                    setExternalState(State.PIN_REQUIRED);
                    break;
                case EVENT_ICC_CHANGED /*3*/:
                    setExternalState(State.PUK_REQUIRED);
                    break;
                case EVENT_ICC_ABSENT /*4*/:
                    setExternalState(State.READY);
                    break;
                case EVENT_ICC_LOCKED /*5*/:
                    if (this.mUiccApplication.getPersoSubState() != PersoSubState.PERSOSUBSTATE_SIM_NETWORK) {
                        custSetExternalState(this.mUiccApplication.getPersoSubState());
                        this.mExternalState = State.NETWORK_LOCKED;
                        loge("updateExternalState: set mPhoneId=" + this.mPhoneId + " mExternalState=" + this.mExternalState);
                        break;
                    }
                    setExternalState(State.NETWORK_LOCKED);
                    break;
                case EVENT_APP_READY /*6*/:
                    setExternalState(State.UNKNOWN);
                    break;
            }
            custUpdateExternalState(getState());
        }
    }

    private void registerUiccCardEvents() {
        if (this.mUiccCard != null) {
            this.mUiccCard.registerForAbsent(this, EVENT_ICC_ABSENT, null);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.registerForReady(this, EVENT_APP_READY, null);
            this.mUiccApplication.registerForLocked(this, EVENT_ICC_LOCKED, null);
            this.mUiccApplication.registerForNetworkLocked(this, EVENT_NETWORK_LOCKED, null);
            registerForFdnStatusChange(this);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.registerForImsiReady(this, EVENT_IMSI_READY, null);
            this.mIccRecords.registerForRecordsLoaded(this, EVENT_RECORDS_LOADED, null);
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
            if (this.mPhoneId == null || !SubscriptionManager.isValidSlotId(this.mPhoneId.intValue())) {
                loge("broadcastIccStateChangedIntent: mPhoneId=" + this.mPhoneId + " is invalid; Return!!");
            } else if (this.mQuietMode) {
                log("broadcastIccStateChangedIntent: QuietMode NOT Broadcasting intent ACTION_SIM_STATE_CHANGED  value=" + value + " reason=" + reason);
            } else {
                Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
                intent.addFlags(67108864);
                intent.putExtra("phoneName", "Phone");
                intent.putExtra("ss", value);
                intent.putExtra(TelephonyEventLog.DATA_KEY_DATA_DEACTIVATE_REASON, reason);
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhoneId.intValue());
                VSimUtilsInner.putVSimExtraForIccStateChanged(intent, this.mPhoneId.intValue(), value);
                log("broadcastIccStateChangedIntent intent ACTION_SIM_STATE_CHANGED value=" + value + " reason=" + reason + " for mPhoneId=" + this.mPhoneId);
                ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
            }
        }
    }

    private void setExternalState(State newState, boolean override) {
        synchronized (this.mLock) {
            if (this.mPhoneId == null || !SubscriptionManager.isValidSlotId(this.mPhoneId.intValue())) {
                loge("setExternalState: mPhoneId=" + this.mPhoneId + " is invalid; Return!!");
            } else if (override || newState != this.mExternalState) {
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
            } else {
                loge("setExternalState: !override and newstate unchanged from " + newState);
            }
        }
    }

    public boolean blockPinStateForDualCards(State s) {
        log("blockPinStateForDualCards s = " + s + ", mPhoneId " + this.mPhoneId);
        if (s.isPinLocked()) {
            boolean IS_OVERSEA_USIM_SUPPORT;
            boolean mNeedSwitchCommrilMode;
            if (!HwTelephonyFactory.getHwUiccManager().isFullNetworkSupported()) {
                mNeedSwitchCommrilMode = false;
            } else if (HwTelephonyFactory.getHwUiccManager().getCommrilMode()) {
                mNeedSwitchCommrilMode = DBG;
            } else {
                mNeedSwitchCommrilMode = HwTelephonyFactory.getHwUiccManager().isRestartingRild();
            }
            boolean mBlockPinForeignUsim = false;
            if (this.IS_CHINA_TELECOM) {
                IS_OVERSEA_USIM_SUPPORT = SystemProperties.getBoolean("persist.radio.supp_oversea_usim", false);
            } else {
                IS_OVERSEA_USIM_SUPPORT = false;
            }
            if (IS_OVERSEA_USIM_SUPPORT) {
                mBlockPinForeignUsim = HwTelephonyFactory.getHwUiccManager().getSwitchTag();
            }
            if (VSimUtilsInner.isVSimInProcess()) {
                if (VSimUtilsInner.needBlockPin(this.mPhoneId.intValue())) {
                    log("vsim block pin for phone id " + this.mPhoneId);
                    return DBG;
                }
                log("vsim no need block pin for phone id " + this.mPhoneId + ", just pass");
            } else if ((SystemProperties.getBoolean("persist.sys.dualcards", false) || SystemProperties.getBoolean("ro.config.full_network_support", false)) && (HwTelephonyFactory.getHwUiccManager().getSwitchingSlot() || r2 || r1)) {
                log("setExternalState getWaitingSwitchBalongSlot is true, so return");
                return DBG;
            }
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processLockedState() {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                if (this.mUiccApplication.getPin1State() != PinState.PINSTATE_ENABLED_PERM_BLOCKED) {
                    switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues()[this.mUiccApplication.getState().ordinal()]) {
                        case EVENT_RADIO_ON /*2*/:
                            this.mPinLockedRegistrants.notifyRegistrants();
                            setExternalState(State.PIN_REQUIRED);
                            break;
                        case EVENT_ICC_CHANGED /*3*/:
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
            case EVENT_RADIO_OFF_OR_UNAVAILABLE /*1*/:
                return "ABSENT";
            case EVENT_RADIO_ON /*2*/:
                return "CARD_IO_ERROR";
            case EVENT_ICC_CHANGED /*3*/:
                return "LOCKED";
            case EVENT_ICC_ABSENT /*4*/:
                return "NOT_READY";
            case EVENT_ICC_LOCKED /*5*/:
                return "LOCKED";
            case EVENT_APP_READY /*6*/:
                return "LOCKED";
            case EVENT_RECORDS_LOADED /*7*/:
                return "LOCKED";
            case EVENT_IMSI_READY /*8*/:
                return "READY";
            default:
                return "UNKNOWN";
        }
    }

    private String getIccStateReason(State state) {
        switch (-getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues()[state.ordinal()]) {
            case EVENT_RADIO_ON /*2*/:
                return "CARD_IO_ERROR";
            case EVENT_ICC_CHANGED /*3*/:
                return "NETWORK";
            case EVENT_ICC_LOCKED /*5*/:
                return "PERM_DISABLED";
            case EVENT_APP_READY /*6*/:
                return "PIN";
            case EVENT_RECORDS_LOADED /*7*/:
                return "PUK";
            default:
                return null;
        }
    }

    public State getState() {
        if (!VSimUtilsInner.isPlatformTwoModems() || this.mCi == null || this.mCi.isRadioAvailable()) {
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

    public void supplyPin(String pin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPin(pin, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
                return;
            }
        }
    }

    public void supplyPuk(String puk, String newPin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPuk(puk, newPin, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
                return;
            }
        }
    }

    public void supplyPin2(String pin2, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPin2(pin2, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
                return;
            }
        }
    }

    public void supplyPuk2(String puk2, String newPin2, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPuk2(puk2, newPin2, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
                return;
            }
        }
    }

    public void supplyNetworkDepersonalization(String pin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyNetworkDepersonalization(pin, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("CommandsInterface is not set.");
                onComplete.sendToTarget();
                return;
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

    public void setIccLockEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.setIccLockEnabled(enabled, password, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
                return;
            }
        }
    }

    public void setIccFdnEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.setIccFdnEnabled(enabled, password, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
                return;
            }
        }
    }

    public void changeIccLockPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.changeIccLockPassword(oldPassword, newPassword, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
                return;
            }
        }
    }

    public void changeIccFdnPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.changeIccFdnPassword(oldPassword, newPassword, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
                return;
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
            return DBG;
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
        for (i = 0; i < this.mAbsentRegistrants.size(); i += EVENT_RADIO_OFF_OR_UNAVAILABLE) {
            pw.println("  mAbsentRegistrants[" + i + "]=" + ((Registrant) this.mAbsentRegistrants.get(i)).getHandler());
        }
        pw.println(" mPinLockedRegistrants: size=" + this.mPinLockedRegistrants.size());
        for (i = 0; i < this.mPinLockedRegistrants.size(); i += EVENT_RADIO_OFF_OR_UNAVAILABLE) {
            pw.println("  mPinLockedRegistrants[" + i + "]=" + ((Registrant) this.mPinLockedRegistrants.get(i)).getHandler());
        }
        pw.println(" mNetworkLockedRegistrants: size=" + this.mNetworkLockedRegistrants.size());
        for (i = 0; i < this.mNetworkLockedRegistrants.size(); i += EVENT_RADIO_OFF_OR_UNAVAILABLE) {
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
        return EVENT_RADIO_OFF_OR_UNAVAILABLE;
    }

    public static int getEventAppReadyHw() {
        return EVENT_APP_READY;
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
