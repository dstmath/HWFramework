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
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.vsim.HwVSimEventReport;
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
    private int mCurrentAppType;
    private State mExternalState;
    private IccRecords mIccRecords;
    private boolean mInitialized;
    private final Object mLock;
    private boolean mRadioOn;
    private UiccCardApplication mUiccApplication;
    private UiccCard mUiccCard;
    private HwVSimUiccController mUiccController;

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
            iArr[State.DEACTIVED.ordinal()] = 11;
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
            iArr[State.PERM_DISABLED.ordinal()] = EVENT_APP_READY;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.PIN_REQUIRED.ordinal()] = 6;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.PUK_REQUIRED.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.READY.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[State.UNKNOWN.ordinal()] = 12;
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
            iArr[AppState.APPSTATE_READY.ordinal()] = EVENT_RADIO_OFF_OR_UNAVAILABLE;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppState.APPSTATE_SUBSCRIPTION_PERSO.ordinal()] = 14;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppState.APPSTATE_UNKNOWN.ordinal()] = EVENT_RADIO_ON;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.HwVSimIccCardProxy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.HwVSimIccCardProxy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.HwVSimIccCardProxy.<clinit>():void");
    }

    public HwVSimIccCardProxy(Context context, CommandsInterface ci) {
        this.mLock = new Object();
        this.mCurrentAppType = EVENT_RADIO_OFF_OR_UNAVAILABLE;
        this.mUiccController = null;
        this.mUiccCard = null;
        this.mUiccApplication = null;
        this.mIccRecords = null;
        this.mExternalState = State.UNKNOWN;
        this.mRadioOn = false;
        this.mInitialized = false;
        if (HWDBG) {
            logd("ctor: ci=" + ci);
        }
        this.mContext = context;
        this.mCi = ci;
        this.mUiccController = HwVSimUiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, EVENT_ICC_CHANGED, null);
        this.mCi.registerForOn(this, EVENT_RADIO_ON, null);
        this.mCi.registerForOffOrNotAvailable(this, EVENT_RADIO_OFF_OR_UNAVAILABLE, null);
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
            case EVENT_RADIO_OFF_OR_UNAVAILABLE /*1*/:
                if (HWFLOW) {
                    logi("EVENT_RADIO_OFF_OR_UNAVAILABLE");
                }
                this.mRadioOn = false;
                if (RadioState.RADIO_UNAVAILABLE == this.mCi.getRadioState()) {
                    setExternalState(State.NOT_READY);
                } else {
                    setExternalState(State.ABSENT);
                }
            case EVENT_RADIO_ON /*2*/:
                if (HWFLOW) {
                    logi("EVENT_RADIO_ON");
                }
                this.mRadioOn = HWLOGW_E;
                if (!this.mInitialized) {
                    this.mInitialized = HWLOGW_E;
                    sendMessage(obtainMessage(EVENT_ICC_CHANGED));
                }
            case EVENT_ICC_CHANGED /*3*/:
                if (HWFLOW) {
                    logi("EVENT_ICC_CHANGED");
                }
                if (this.mInitialized) {
                    updateIccAvailability();
                }
            case EVENT_ICC_ABSENT /*4*/:
                if (HWFLOW) {
                    logi("EVENT_ICC_ABSENT");
                }
                setExternalState(State.ABSENT);
            case EVENT_APP_READY /*5*/:
                if (HWFLOW) {
                    logi("EVENT_APP_READY");
                }
                setExternalState(State.READY);
            default:
                loge("Unhandled message with number: " + msg.what);
        }
    }

    private void updateIccAvailability() {
        if (HWDBG) {
            logd("updateIccAvailability");
        }
        synchronized (this.mLock) {
            UiccCard newCard = this.mUiccController.getUiccCard();
            UiccCardApplication uiccCardApplication = null;
            IccRecords newRecords = null;
            if (newCard != null) {
                uiccCardApplication = newCard.getApplication(this.mCurrentAppType);
                if (uiccCardApplication != null) {
                    newRecords = uiccCardApplication.getIccRecords();
                }
            }
            if (this.mIccRecords == newRecords && this.mUiccApplication == uiccCardApplication) {
                if (this.mUiccCard != newCard) {
                }
                updateExternalState();
            }
            if (HWFLOW) {
                logi("Icc changed. Reregestering.");
            }
            unregisterUiccCardEvents();
            this.mUiccCard = newCard;
            this.mUiccApplication = uiccCardApplication;
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
            if ((this.mRadioOn || !isAirplaneMode()) && this.mUiccCard != null) {
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
                case EVENT_RADIO_OFF_OR_UNAVAILABLE /*1*/:
                    if (HWDBG) {
                        logd("APPSTATE_READY");
                    }
                    setExternalState(State.READY);
                    break;
                case EVENT_RADIO_ON /*2*/:
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
            this.mUiccCard.registerForAbsent(this, EVENT_ICC_ABSENT, null);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.registerForReady(this, EVENT_APP_READY, null);
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
        return System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0 ? HWLOGW_E : false;
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
            case EVENT_APP_READY /*5*/:
                return "LOCKED";
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                return "LOCKED";
            case HwVSimEventReport.VSIM_CAUSE_TYPE_CARD_POWER_ON /*7*/:
                return "LOCKED";
            case HwVSimEventReport.VSIM_CAUSE_TYPE_ACTIVE_MODEM_MODE /*8*/:
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
            case EVENT_APP_READY /*5*/:
                return "PERM_DISABLED";
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                return "PIN";
            case HwVSimEventReport.VSIM_CAUSE_TYPE_CARD_POWER_ON /*7*/:
                return "PUK";
            default:
                return null;
        }
    }

    private void broadcastIccStateChangedIntent(String value, String reason) {
        synchronized (this.mLock) {
            Intent intent = new Intent("android.intent.action.VSIM_STATE_CHANGED");
            intent.addFlags(67108864);
            intent.putExtra("phoneName", "Phone");
            intent.putExtra("ss", value);
            intent.putExtra("reason", reason);
            intent.putExtra("subscription", EVENT_RADIO_ON);
            intent.putExtra("phone", EVENT_RADIO_ON);
            intent.putExtra("slot", EVENT_RADIO_ON);
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
            return HWLOGW_E;
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
