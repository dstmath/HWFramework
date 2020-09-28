package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import com.huawei.android.app.ActivityManagerNativeEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import com.huawei.internal.telephony.uicc.UiccSlotEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;

public class HwVSimIccCardProxy {
    private static final int EVENT_APP_READY = 5;
    private static final int EVENT_ICC_CHANGED = 3;
    private static final int EVENT_RADIO_OFF_OR_UNAVAILABLE = 1;
    private static final int EVENT_RADIO_ON = 2;
    private static final String LOG_TAG = "VSimIccCardProxy";
    private CommandsInterfaceEx mCi;
    private Context mContext;
    private int mCurrentAppType = UiccControllerExt.APP_FAM_3GPP;
    private IccCardConstantsEx.StateEx mExternalState = IccCardConstantsEx.StateEx.UNKNOWN;
    private final Handler mHandler = new VsimIccCardProxyHandler(this);
    private IccRecordsEx mIccRecords = null;
    private boolean mInitialized = false;
    private final Object mLock = new Object();
    private boolean mRadioOn = false;
    private UiccCardApplicationEx mUiccApplication = null;
    private UiccCardExt mUiccCard = null;
    private HwVSimUiccController mUiccController = null;
    private UiccSlotEx mUiccSlot = null;

    public HwVSimIccCardProxy(Context context, CommandsInterfaceEx ci) {
        logd("ctor: ci=" + ci);
        this.mContext = context;
        this.mCi = ci;
        this.mUiccController = HwVSimUiccController.getInstance();
        this.mUiccController.registerForIccChanged(this.mHandler, 3, null);
        this.mCi.registerForOn(this.mHandler, 2, (Object) null);
        this.mCi.registerForOffOrNotAvailable(this.mHandler, 1, (Object) null);
        setExternalState(IccCardConstantsEx.StateEx.NOT_READY, false);
    }

    public void dispose() {
        synchronized (this.mLock) {
            logd("Disposing");
            this.mUiccController.unregisterForIccChanged(this.mHandler);
            this.mUiccController = null;
        }
    }

    private static class VsimIccCardProxyHandler extends Handler {
        WeakReference<HwVSimIccCardProxy> mWeakReference;

        VsimIccCardProxyHandler(HwVSimIccCardProxy hwVSimIccCardProxy) {
            this.mWeakReference = new WeakReference<>(hwVSimIccCardProxy);
        }

        public void handleMessage(Message msg) {
            HwVSimIccCardProxy mVSimIccCardProxy = this.mWeakReference.get();
            if (mVSimIccCardProxy == null) {
                HwVSimIccCardProxy.slogi("mVSimIccCardProxy is null, return.");
                return;
            }
            switch (msg.what) {
                case 1:
                    HwVSimIccCardProxy.slogi("EVENT_RADIO_OFF_OR_UNAVAILABLE");
                    mVSimIccCardProxy.mRadioOn = false;
                    if (2 == mVSimIccCardProxy.mCi.getRadioState()) {
                        mVSimIccCardProxy.setExternalState(IccCardConstantsEx.StateEx.NOT_READY);
                        return;
                    } else {
                        mVSimIccCardProxy.setExternalState(IccCardConstantsEx.StateEx.ABSENT);
                        return;
                    }
                case 2:
                    HwVSimIccCardProxy.slogi("EVENT_RADIO_ON");
                    mVSimIccCardProxy.mRadioOn = true;
                    if (!mVSimIccCardProxy.mInitialized) {
                        mVSimIccCardProxy.mInitialized = true;
                        sendMessage(obtainMessage(3));
                        return;
                    }
                    return;
                case 3:
                    HwVSimIccCardProxy.slogi("EVENT_ICC_CHANGED");
                    if (mVSimIccCardProxy.mInitialized) {
                        mVSimIccCardProxy.updateIccAvailability();
                        return;
                    }
                    return;
                case 4:
                default:
                    HwVSimIccCardProxy.slogi("Unhandled message with number: " + msg.what);
                    return;
                case 5:
                    HwVSimIccCardProxy.slogi("EVENT_APP_READY");
                    mVSimIccCardProxy.setExternalState(IccCardConstantsEx.StateEx.READY);
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateIccAvailability() {
        logd("updateIccAvailability");
        synchronized (this.mLock) {
            UiccSlotEx newSlot = this.mUiccController.getUiccSlot();
            UiccCardExt newCard = this.mUiccController.getUiccCard();
            UiccCardApplicationEx newApp = null;
            IccRecordsEx newRecords = null;
            if (!(newCard == null || (newApp = newCard.getApplication(this.mCurrentAppType)) == null)) {
                newRecords = newApp.getIccRecords();
            }
            if (!(this.mIccRecords == newRecords && this.mUiccApplication == newApp && this.mUiccCard == newCard && this.mUiccSlot == newSlot)) {
                logi("Icc changed. Reregestering.");
                unregisterUiccCardEvents();
                this.mUiccSlot = newSlot;
                this.mUiccCard = newCard;
                this.mUiccApplication = newApp;
                this.mIccRecords = newRecords;
                registerUiccCardEvents();
            }
            updateExternalState();
        }
    }

    private void updateExternalState() {
        logd("updateExternalState");
        if (this.mUiccCard == null || this.mUiccCard.getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) {
            if ((this.mRadioOn || !isAirplaneMode()) && this.mUiccCard != null) {
                logd("Radio on or airplanemode off, set state to ABSENT");
                setExternalState(IccCardConstantsEx.StateEx.ABSENT);
                return;
            }
            logd("Radio off or uicccard is null, set state to NOT_READY");
            setExternalState(IccCardConstantsEx.StateEx.NOT_READY);
        } else if (this.mUiccCard.getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_ERROR) {
            setExternalState(IccCardConstantsEx.StateEx.CARD_IO_ERROR);
        } else if (this.mUiccApplication == null) {
            logd("mUiccApplication not found");
            setExternalState(IccCardConstantsEx.StateEx.NOT_READY);
        } else {
            switch (AnonymousClass1.$SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$AppStateEx[this.mUiccApplication.getState().ordinal()]) {
                case 1:
                    logd("APPSTATE_UNKNOWN");
                    setExternalState(IccCardConstantsEx.StateEx.UNKNOWN);
                    return;
                case 2:
                    logd("APPSTATE_READY");
                    setExternalState(IccCardConstantsEx.StateEx.READY);
                    return;
                default:
                    return;
            }
        }
    }

    private void registerUiccCardEvents() {
        if (this.mUiccApplication != null) {
            this.mUiccApplication.registerForReady(this.mHandler, 5, (Object) null);
        }
    }

    private void unregisterUiccCardEvents() {
        if (this.mUiccApplication != null) {
            this.mUiccApplication.unregisterForReady(this.mHandler);
        }
    }

    private boolean isAirplaneMode() {
        return Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.uicc.HwVSimIccCardProxy$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx = new int[IccCardConstantsEx.StateEx.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$AppStateEx = new int[IccCardApplicationStatusEx.AppStateEx.values().length];

        static {
            try {
                $SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[IccCardConstantsEx.StateEx.ABSENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[IccCardConstantsEx.StateEx.PIN_REQUIRED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[IccCardConstantsEx.StateEx.PUK_REQUIRED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[IccCardConstantsEx.StateEx.NETWORK_LOCKED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[IccCardConstantsEx.StateEx.READY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[IccCardConstantsEx.StateEx.NOT_READY.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[IccCardConstantsEx.StateEx.PERM_DISABLED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[IccCardConstantsEx.StateEx.CARD_IO_ERROR.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[IccCardConstantsEx.StateEx.CARD_RESTRICTED.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[IccCardConstantsEx.StateEx.LOADED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$AppStateEx[IccCardApplicationStatusEx.AppStateEx.APPSTATE_UNKNOWN.ordinal()] = 1;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$AppStateEx[IccCardApplicationStatusEx.AppStateEx.APPSTATE_READY.ordinal()] = 2;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    private String getIccStateIntentString(IccCardConstantsEx.StateEx state) {
        switch (AnonymousClass1.$SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[state.ordinal()]) {
            case 1:
                return "ABSENT";
            case 2:
                return "LOCKED";
            case 3:
                return "LOCKED";
            case 4:
                return "LOCKED";
            case 5:
                return "READY";
            case 6:
                return "NOT_READY";
            case 7:
                return "LOCKED";
            case 8:
                return "CARD_IO_ERROR";
            case 9:
                return "CARD_RESTRICTED";
            case 10:
                return "LOADED";
            default:
                return "UNKNOWN";
        }
    }

    private String getIccStateReason(IccCardConstantsEx.StateEx state) {
        switch (AnonymousClass1.$SwitchMap$com$huawei$internal$telephony$IccCardConstantsEx$StateEx[state.ordinal()]) {
            case 2:
                return "PIN";
            case 3:
                return "PUK";
            case 4:
                return "NETWORK";
            case 5:
            case 6:
            default:
                return null;
            case 7:
                return "PERM_DISABLED";
            case 8:
                return "CARD_IO_ERROR";
            case 9:
                return "CARD_RESTRICTED";
        }
    }

    public void broadcastIccStateChangedIntent(IccCardConstantsEx.StateEx state) {
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
            logi("broadcastIccStateChangedIntent intent ACTION_VSIM_STATE_CHANGED value=" + value + " reason=" + reason + " for VSIM");
            ActivityManagerNativeEx.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
        }
    }

    private void setExternalState(IccCardConstantsEx.StateEx newState, boolean override) {
        synchronized (this.mLock) {
            if (!override) {
                if (newState == this.mExternalState) {
                    loge("setExternalState: !override and newstate unchanged from " + newState);
                    return;
                }
            }
            this.mExternalState = newState;
            logi("setExternalState: mExternalState=" + this.mExternalState);
            SystemPropertiesEx.set("gsm.vsim.state", getState().toString());
            broadcastIccStateChangedIntent(getIccStateIntentString(this.mExternalState), getIccStateReason(this.mExternalState));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setExternalState(IccCardConstantsEx.StateEx newState) {
        setExternalState(newState, false);
    }

    public IccCardConstantsEx.StateEx getState() {
        IccCardConstantsEx.StateEx stateEx;
        synchronized (this.mLock) {
            stateEx = this.mExternalState;
        }
        return stateEx;
    }

    public boolean hasIccCard() {
        boolean z;
        synchronized (this.mLock) {
            z = (this.mUiccCard == null || this.mUiccCard.getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) ? false : true;
        }
        return z;
    }

    private void logd(String s) {
        RlogEx.d(LOG_TAG, s);
    }

    private void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }

    private void logi(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: private */
    public static void slogi(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (fd != null && pw != null && args != null) {
            pw.println("VSimIccCardProxy: " + this);
            pw.println(" mContext=" + this.mContext);
            pw.println(" mCi=" + this.mCi);
            synchronized (this.mLock) {
                pw.println(" mUiccController=" + this.mUiccController);
            }
            pw.println(" mUiccCard=" + this.mUiccCard);
            pw.println(" mUiccApplication=" + this.mUiccApplication);
            synchronized (this.mLock) {
                pw.println(" mIccRecords=" + this.mIccRecords);
                pw.println(" mExternalState=" + this.mExternalState);
            }
            pw.flush();
        }
    }
}
