package com.android.internal.telephony.uicc;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwVSimIccCardProxy extends AbstractIccCardProxy implements IccCard {
    private static final /* synthetic */ int[] -com-android-internal-telephony-IccCardConstants$StateSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues = null;
    private static final int EVENT_APP_READY = 5;
    private static final int EVENT_ICC_ABSENT = 4;
    private static final int EVENT_ICC_CHANGED = 3;
    private static final int EVENT_RADIO_OFF_OR_UNAVAILABLE = 1;
    private static final int EVENT_RADIO_ON = 2;
    private static boolean HWDBG = false;
    private static boolean HWFLOW = false;
    private static final boolean HWLOGW_E = true;
    private static final String LOG_TAG = "VSimIccCardProxy";
    private CommandsInterface mCi;
    private Context mContext;
    private int mCurrentAppType = 1;
    private State mExternalState = State.UNKNOWN;
    private IccRecords mIccRecords = null;
    private boolean mInitialized = false;
    private final Object mLock = new Object();
    private boolean mRadioOn = false;
    private UiccCardApplication mUiccApplication = null;
    private UiccCard mUiccCard = null;
    private HwVSimUiccController mUiccController = null;

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
            iArr[State.CARD_RESTRICTED.ordinal()] = 11;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DEACTIVED.ordinal()] = 12;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.NETWORK_LOCKED.ordinal()] = 3;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.NOT_READY.ordinal()] = 4;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.PERM_DISABLED.ordinal()] = 5;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.PIN_REQUIRED.ordinal()] = 6;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.PUK_REQUIRED.ordinal()] = 7;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[State.READY.ordinal()] = 8;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[State.UNKNOWN.ordinal()] = 13;
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
            iArr[AppState.APPSTATE_DETECTED.ordinal()] = 11;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppState.APPSTATE_PIN.ordinal()] = 12;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppState.APPSTATE_PUK.ordinal()] = 13;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppState.APPSTATE_READY.ordinal()] = 1;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppState.APPSTATE_SUBSCRIPTION_PERSO.ordinal()] = 14;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppState.APPSTATE_UNKNOWN.ordinal()] = 2;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues = iArr;
        return iArr;
    }

    static {
        boolean z;
        boolean z2 = true;
        if (Log.HWLog) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(LOG_TAG, 3);
        } else {
            z = false;
        }
        HWDBG = z;
        if (!Log.HWINFO) {
            if (Log.HWModuleLog) {
                z2 = Log.isLoggable(LOG_TAG, 4);
            } else {
                z2 = false;
            }
        }
        HWFLOW = z2;
    }

    public HwVSimIccCardProxy(Context context, CommandsInterface ci) {
        if (HWDBG) {
            logd("ctor: ci=" + ci);
        }
        this.mContext = context;
        this.mCi = ci;
        this.mUiccController = HwVSimUiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 3, null);
        this.mCi.registerForOn(this, 2, null);
        this.mCi.registerForOffOrNotAvailable(this, 1, null);
        setExternalState(State.NOT_READY, false);
    }

    public void dispose() {
        synchronized (this.mLock) {
            if (HWDBG) {
                logd("Disposing");
            }
            this.mUiccController.unregisterForIccChanged(this);
            this.mUiccController = null;
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (HWFLOW) {
                    logi("EVENT_RADIO_OFF_OR_UNAVAILABLE");
                }
                this.mRadioOn = false;
                if (RadioState.RADIO_UNAVAILABLE == this.mCi.getRadioState()) {
                    setExternalState(State.NOT_READY);
                    return;
                } else {
                    setExternalState(State.ABSENT);
                    return;
                }
            case 2:
                if (HWFLOW) {
                    logi("EVENT_RADIO_ON");
                }
                this.mRadioOn = true;
                if (!this.mInitialized) {
                    this.mInitialized = true;
                    sendMessage(obtainMessage(3));
                    return;
                }
                return;
            case 3:
                if (HWFLOW) {
                    logi("EVENT_ICC_CHANGED");
                }
                if (this.mInitialized) {
                    updateIccAvailability();
                    return;
                }
                return;
            case 4:
                if (HWFLOW) {
                    logi("EVENT_ICC_ABSENT");
                }
                setExternalState(State.ABSENT);
                return;
            case 5:
                if (HWFLOW) {
                    logi("EVENT_APP_READY");
                }
                setExternalState(State.READY);
                return;
            default:
                loge("Unhandled message with number: " + msg.what);
                return;
        }
    }

    /* JADX WARNING: Missing block: B:24:0x0048, code:
            if (r5.mUiccCard != r1) goto L_0x002b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateIccAvailability() {
        if (HWDBG) {
            logd("updateIccAvailability");
        }
        synchronized (this.mLock) {
            UiccCard newCard = this.mUiccController.getUiccCard();
            UiccCardApplication newApp = null;
            IccRecords newRecords = null;
            if (newCard != null) {
                newApp = newCard.getApplication(this.mCurrentAppType);
                if (newApp != null) {
                    newRecords = newApp.getIccRecords();
                }
            }
            if (this.mIccRecords == newRecords && this.mUiccApplication == newApp) {
            }
            if (HWFLOW) {
                logi("Icc changed. Reregestering.");
            }
            unregisterUiccCardEvents();
            this.mUiccCard = newCard;
            this.mUiccApplication = newApp;
            this.mIccRecords = newRecords;
            registerUiccCardEvents();
            updateExternalState();
        }
    }

    private void updateExternalState() {
        if (HWDBG) {
            logd("updateExternalState");
        }
        if (this.mUiccCard == null || this.mUiccCard.getCardState() == CardState.CARDSTATE_ABSENT) {
            if ((this.mRadioOn || (isAirplaneMode() ^ 1) != 0) && this.mUiccCard != null) {
                if (HWDBG) {
                    logd("Radio on or airplanemode off, set state to ABSENT");
                }
                setExternalState(State.ABSENT);
            } else {
                if (HWDBG) {
                    logd("Radio off or uicccard is null, set state to NOT_READY");
                }
                setExternalState(State.NOT_READY);
            }
        } else if (this.mUiccCard.getCardState() == CardState.CARDSTATE_ERROR) {
            setExternalState(State.CARD_IO_ERROR);
        } else if (this.mUiccApplication == null) {
            if (HWDBG) {
                logd("mUiccApplication not found");
            }
            setExternalState(State.NOT_READY);
        } else {
            switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues()[this.mUiccApplication.getState().ordinal()]) {
                case 1:
                    if (HWDBG) {
                        logd("APPSTATE_READY");
                    }
                    setExternalState(State.READY);
                    break;
                case 2:
                    if (HWDBG) {
                        logd("APPSTATE_UNKNOWN");
                    }
                    setExternalState(State.UNKNOWN);
                    break;
            }
        }
    }

    private void registerUiccCardEvents() {
        if (this.mUiccCard != null) {
            this.mUiccCard.registerForAbsent(this, 4, null);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.registerForReady(this, 5, null);
        }
    }

    private void unregisterUiccCardEvents() {
        if (this.mUiccCard != null) {
            this.mUiccCard.unregisterForAbsent(this);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.unregisterForReady(this);
        }
    }

    private boolean isAirplaneMode() {
        return System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    private String getIccStateIntentString(State state) {
        switch (-getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues()[state.ordinal()]) {
            case 1:
                return "ABSENT";
            case 2:
                return "CARD_IO_ERROR";
            case 3:
                return "LOCKED";
            case 4:
                return "NOT_READY";
            case 5:
                return "LOCKED";
            case 6:
                return "LOCKED";
            case 7:
                return "LOCKED";
            case 8:
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
                return "NETWORK";
            case 5:
                return "PERM_DISABLED";
            case 6:
                return "PIN";
            case 7:
                return "PUK";
            default:
                return null;
        }
    }

    public void broadcastIccStateChangedIntent(State state) {
        broadcastIccStateChangedIntent(getIccStateIntentString(state), getIccStateReason(state));
    }

    private void broadcastIccStateChangedIntent(String value, String reason) {
        synchronized (this.mLock) {
            Intent intent = new Intent("com.huawei.vsim.action.VSIM_STATE_CHANGED");
            intent.addFlags(67108864);
            intent.putExtra("phoneName", "Phone");
            intent.putExtra("ss", value);
            intent.putExtra("reason", reason);
            intent.putExtra("subscription", 2);
            intent.putExtra("phone", 2);
            intent.putExtra("slot", 2);
            if (HWFLOW) {
                logi("broadcastIccStateChangedIntent intent ACTION_VSIM_STATE_CHANGED value=" + value + " reason=" + reason + " for VSIM");
            }
            ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
        }
    }

    private void setExternalState(State newState, boolean override) {
        synchronized (this.mLock) {
            if (!override) {
                if (newState == this.mExternalState) {
                    loge("setExternalState: !override and newstate unchanged from " + newState);
                    return;
                }
            }
            this.mExternalState = newState;
            if (HWFLOW) {
                logi("setExternalState: mExternalState=" + this.mExternalState);
            }
            SystemProperties.set("gsm.vsim.state", getState().toString());
            broadcastIccStateChangedIntent(getIccStateIntentString(this.mExternalState), getIccStateReason(this.mExternalState));
        }
    }

    private void setExternalState(State newState) {
        setExternalState(newState, false);
    }

    public State getState() {
        State state;
        synchronized (this.mLock) {
            state = this.mExternalState;
        }
        return state;
    }

    public IccRecords getIccRecords() {
        synchronized (this.mLock) {
        }
        return null;
    }

    public IccFileHandler getIccFileHandler() {
        synchronized (this.mLock) {
        }
        return null;
    }

    public void registerForAbsent(Handler h, int what, Object obj) {
    }

    public void unregisterForAbsent(Handler h) {
    }

    public void registerForNetworkLocked(Handler h, int what, Object obj) {
    }

    public void unregisterForNetworkLocked(Handler h) {
    }

    public void registerForLocked(Handler h, int what, Object obj) {
    }

    public void unregisterForLocked(Handler h) {
    }

    public void supplyPin(String pin, Message onComplete) {
        loge("supplyPin not supported by vsim");
        synchronized (this.mLock) {
            if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("ICC card is absent.");
                onComplete.sendToTarget();
                return;
            }
        }
    }

    public void supplyPuk(String puk, String newPin, Message onComplete) {
    }

    public void supplyPin2(String pin2, Message onComplete) {
    }

    public void supplyPuk2(String puk2, String newPin2, Message onComplete) {
    }

    public void supplyNetworkDepersonalization(String pin, Message onComplete) {
    }

    public boolean getIccLockEnabled() {
        return false;
    }

    public boolean getIccFdnEnabled() {
        return false;
    }

    public boolean getIccFdnAvailable() {
        return false;
    }

    public boolean getIccPin2Blocked() {
        return false;
    }

    public boolean getIccPuk2Blocked() {
        return false;
    }

    public void setIccLockEnabled(boolean enabled, String password, Message onComplete) {
    }

    public void setIccFdnEnabled(boolean enabled, String password, Message onComplete) {
    }

    public void changeIccLockPassword(String oldPassword, String newPassword, Message onComplete) {
    }

    public void changeIccFdnPassword(String oldPassword, String newPassword, Message onComplete) {
    }

    public String getServiceProviderName() {
        synchronized (this.mLock) {
        }
        return null;
    }

    public boolean isApplicationOnIcc(AppType type) {
        return false;
    }

    public boolean hasIccCard() {
        synchronized (this.mLock) {
            if (this.mUiccCard == null || this.mUiccCard.getCardState() == CardState.CARDSTATE_ABSENT) {
                return false;
            }
            return true;
        }
    }

    private void logd(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    private void logi(String msg) {
        Rlog.i(LOG_TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("VSimIccCardProxy: " + this);
        pw.println(" mContext=" + this.mContext);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mUiccController=" + this.mUiccController);
        pw.println(" mUiccCard=" + this.mUiccCard);
        pw.println(" mUiccApplication=" + this.mUiccApplication);
        pw.println(" mIccRecords=" + this.mIccRecords);
        pw.println(" mExternalState=" + this.mExternalState);
        pw.flush();
    }

    public void supplyDepersonalization(String pin, int type, Message onComplete) {
    }
}
