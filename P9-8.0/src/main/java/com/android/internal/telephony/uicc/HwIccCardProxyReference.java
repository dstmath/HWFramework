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
import android.provider.Settings.System;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwIccCardConstants.HwState;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.AbstractIccCardProxy.IccCardProxyReference;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.PersoSubState;
import com.android.internal.telephony.vsim.HwVSimUtils;
import huawei.android.telephony.wrapper.WrapperFactory;

public class HwIccCardProxyReference extends Handler implements IccCardProxyReference {
    private static final /* synthetic */ int[] -com-android-internal-telephony-HwIccCardConstants$HwStateSwitchesValues = null;
    private static final /* synthetic */ int[] -com-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues = null;
    private static final int EVENT_FDN_STATUS_CHANGED = 1;
    private static final int EVENT_ICC_REFRESH = 101;
    public static final String INTENT_KEY_SUBSCRIPTION_MODE = "sub_mode";
    private static final String LOG_TAG = "HwIccCardProxyReference";
    private static final int PHY_SLOT_0 = 0;
    private static IccCardProxyUtils iccCardProxyUtils = new IccCardProxyUtils();
    private boolean mBroadcastForNotCT4GCardDone = false;
    HwState mExternalState = HwState.UNKNOWN;
    private IccCardProxy mIccCardProxy;
    boolean mIccCardStateHW;
    private final Object mLock = new Object();
    private RegistrantList mNetworkLockedRegistrants = new RegistrantList();
    private UiccCardApplication mUiccCardApplication = null;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-HwIccCardConstants$HwStateSwitchesValues() {
        if (-com-android-internal-telephony-HwIccCardConstants$HwStateSwitchesValues != null) {
            return -com-android-internal-telephony-HwIccCardConstants$HwStateSwitchesValues;
        }
        int[] iArr = new int[HwState.values().length];
        try {
            iArr[HwState.ABSENT.ordinal()] = 32;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[HwState.CARD_IO_ERROR.ordinal()] = 33;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[HwState.DEACTIVED.ordinal()] = 34;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[HwState.NETWORK_LOCKED.ordinal()] = 1;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[HwState.NOT_READY.ordinal()] = 35;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[HwState.PERM_DISABLED.ordinal()] = 36;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[HwState.PIN_REQUIRED.ordinal()] = 37;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[HwState.PUK_REQUIRED.ordinal()] = 38;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[HwState.READY.ordinal()] = 39;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[HwState.RUIM_CORPORATE_LOCKED.ordinal()] = 40;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[HwState.RUIM_HRPD_LOCKED.ordinal()] = 41;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[HwState.RUIM_NETWORK1_LOCKED.ordinal()] = 42;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[HwState.RUIM_NETWORK2_LOCKED.ordinal()] = 43;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[HwState.RUIM_RUIM_LOCKED.ordinal()] = 44;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[HwState.RUIM_SERVICE_PROVIDER_LOCKED.ordinal()] = 45;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[HwState.SIM_CORPORATE_LOCKED.ordinal()] = 2;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[HwState.SIM_CORPORATE_LOCKED_PUK.ordinal()] = 3;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[HwState.SIM_NETWORK_LOCKED_PUK.ordinal()] = 4;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[HwState.SIM_NETWORK_SUBSET_LOCKED.ordinal()] = 5;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[HwState.SIM_NETWORK_SUBSET_LOCKED_PUK.ordinal()] = 6;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[HwState.SIM_SERVICE_PROVIDER_LOCKED.ordinal()] = 7;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[HwState.SIM_SERVICE_PROVIDER_LOCKED_PUK.ordinal()] = 8;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[HwState.SIM_SIM_LOCKED.ordinal()] = 9;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[HwState.UNKNOWN.ordinal()] = 46;
        } catch (NoSuchFieldError e24) {
        }
        -com-android-internal-telephony-HwIccCardConstants$HwStateSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues() {
        if (-com-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues != null) {
            return -com-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues;
        }
        int[] iArr = new int[PersoSubState.values().length];
        try {
            iArr[PersoSubState.PERSOSUBSTATE_IN_PROGRESS.ordinal()] = 32;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_READY.ordinal()] = 33;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE.ordinal()] = 1;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE_PUK.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_HRPD.ordinal()] = 3;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_HRPD_PUK.ordinal()] = 4;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1.ordinal()] = 5;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1_PUK.ordinal()] = 6;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2.ordinal()] = 7;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2_PUK.ordinal()] = 8;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_RUIM.ordinal()] = 9;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_RUIM_PUK.ordinal()] = 10;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER.ordinal()] = 11;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER_PUK.ordinal()] = 12;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_CORPORATE.ordinal()] = 13;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_CORPORATE_PUK.ordinal()] = 14;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK.ordinal()] = 15;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_PUK.ordinal()] = 16;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET.ordinal()] = 17;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK.ordinal()] = 18;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER.ordinal()] = 19;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK.ordinal()] = 20;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SIM.ordinal()] = 21;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SIM_PUK.ordinal()] = 22;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_UNKNOWN.ordinal()] = 34;
        } catch (NoSuchFieldError e25) {
        }
        -com-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues = iArr;
        return iArr;
    }

    public HwIccCardProxyReference(IccCardProxy iccCardProxy) {
        this.mIccCardProxy = iccCardProxy;
    }

    public void custSetExternalState(PersoSubState ps) {
        synchronized (this.mLock) {
            HwState oldState = this.mExternalState;
            this.mExternalState = processPersoSubState(ps);
            int callbackHwState = processHwState(this.mExternalState);
            if (oldState != this.mExternalState) {
                setSystemProperty("gsm.sim.state", iccCardProxyUtils.getPhoneId(this.mIccCardProxy), this.mExternalState.toString());
                broadcastIccStateChangedIntent(this.mExternalState.getIntentString(), this.mExternalState.getReason());
                logd("mExternalState = " + this.mExternalState + ", mExternalState = " + this.mExternalState.toString() + ", getIntentString = " + this.mExternalState.getIntentString() + ", getReason = " + this.mExternalState.getReason());
                this.mNetworkLockedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(callbackHwState), null));
            }
        }
    }

    int processHwState(HwState hs) {
        switch (-getcom-android-internal-telephony-HwIccCardConstants$HwStateSwitchesValues()[hs.ordinal()]) {
            case 1:
                return 3;
            case 2:
                return 5;
            case 3:
                return 10;
            case 4:
                return 8;
            case 5:
                return 4;
            case 6:
                return 9;
            case 7:
                return 6;
            case 8:
                return 11;
            case 9:
                return 7;
            default:
                return 0;
        }
    }

    HwState processPersoSubState(PersoSubState ps) {
        HwState result = HwState.UNKNOWN;
        switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues()[ps.ordinal()]) {
            case 13:
                return HwState.SIM_CORPORATE_LOCKED;
            case 14:
                return HwState.SIM_CORPORATE_LOCKED_PUK;
            case 15:
                return HwState.NETWORK_LOCKED;
            case 16:
                return HwState.SIM_NETWORK_LOCKED_PUK;
            case 17:
                return HwState.SIM_NETWORK_SUBSET_LOCKED;
            case 18:
                return HwState.SIM_NETWORK_SUBSET_LOCKED_PUK;
            case 19:
                return HwState.SIM_SERVICE_PROVIDER_LOCKED;
            case 20:
                return HwState.SIM_SERVICE_PROVIDER_LOCKED_PUK;
            case 21:
                return HwState.SIM_SIM_LOCKED;
            default:
                return result;
        }
    }

    void broadcastIccStateChangedIntent(String value, String reason) {
        synchronized (this.mLock) {
            if (iccCardProxyUtils.getPhoneId(this.mIccCardProxy) == 0 || iccCardProxyUtils.getPhoneId(this.mIccCardProxy) == 1) {
                Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
                intent.addFlags(536870912);
                intent.putExtra("phoneName", "Phone");
                intent.putExtra("ss", value);
                intent.putExtra("reason", reason);
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, iccCardProxyUtils.getPhoneId(this.mIccCardProxy));
                logd("Broadcasting intent ACTION_SIM_STATE_CHANGED " + value + " reason " + reason + " for mPhoneId : " + iccCardProxyUtils.getPhoneId(this.mIccCardProxy));
                ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
                return;
            }
            logd("broadcastIccStateChangedIntent: Card Index is not set; Return!!");
        }
    }

    private void setSystemProperty(String property, int slotId, String value) {
        TelephonyManager.setTelephonyProperty(SubscriptionController.getInstance().getSubId(slotId)[0], property, value);
    }

    public void supplyDepersonalization(String pin, int type, Message onComplete) {
        logd("supplyDepersonalization");
        synchronized (this.mLock) {
            iccCardProxyUtils.getCi(this.mIccCardProxy).supplyDepersonalization(pin, type, onComplete);
        }
    }

    void logd(String s) {
        Rlog.d(LOG_TAG, "[" + iccCardProxyUtils.getPhoneId(this.mIccCardProxy) + "]" + s);
    }

    public boolean getIccCardStateHW() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIccCardStateHW;
        }
        return z;
    }

    public void custUpdateExternalState(State s) {
        if (s == State.ABSENT || s == State.PIN_REQUIRED || s == State.PUK_REQUIRED || s == State.NETWORK_LOCKED) {
            this.mIccCardStateHW = true;
        } else {
            this.mIccCardStateHW = false;
        }
    }

    public void custRegisterForNetworkLocked(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            this.mNetworkLockedRegistrants.add(new Registrant(h, what, obj));
        }
    }

    public void custUnregisterForNetworkLocked(Handler h) {
        synchronized (this.mLock) {
            this.mNetworkLockedRegistrants.remove(h);
        }
    }

    public void handleMessageExtend(Message msg) {
        if (IccCardProxyUtils.getEventRadioOffOrUnavailable() != msg.what) {
            switch (msg.what) {
                case EVENT_ICC_REFRESH /*101*/:
                    rlog("handleMessage get message EVENT_ICC_REFRESH");
                    iccCardProxyUtils.broadcastIccStateChangedIntent(this.mIccCardProxy, "SIM_REFRESH", null);
                    return;
                default:
                    rlog("not handled event");
                    return;
            }
        } else if (SystemProperties.getInt("persist.radio.apm_sim_not_pwdn", 0) == 0) {
            rlog("persist.radio.apm_sim_not_pwdn is not set");
            this.mIccCardProxy.handleMessage(msg);
        } else {
            iccCardProxyUtils.setRadioOn(this.mIccCardProxy, false);
            rlog("persist.radio.apm_sim_not_pwdn is set");
        }
    }

    public boolean updateExternalStateDeactived() {
        if (!WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled() || iccCardProxyUtils.getUiccCard(this.mIccCardProxy).getNumApplications() <= 0 || (iccCardProxyUtils.getUiccCard(this.mIccCardProxy).hasAppActived() ^ 1) == 0) {
            return false;
        }
        iccCardProxyUtils.setExternalState(this.mIccCardProxy, State.DEACTIVED);
        return true;
    }

    public String getIccStateIntentString(State state) {
        if (state == State.DEACTIVED) {
            return "DEACTIVED";
        }
        return iccCardProxyUtils.getIccStateIntentString(this.mIccCardProxy, state);
    }

    public void registerUiccCardEventsExtend() {
        if (iccCardProxyUtils.getIccRecords(this.mIccCardProxy) != null) {
            iccCardProxyUtils.getIccRecords(this.mIccCardProxy).registerForIccRefresh(this.mIccCardProxy, EVENT_ICC_REFRESH, null);
        }
    }

    public void unregisterUiccCardEventsExtend() {
        if (iccCardProxyUtils.getIccRecords(this.mIccCardProxy) != null) {
            iccCardProxyUtils.getIccRecords(this.mIccCardProxy).unRegisterForIccRefresh(this.mIccCardProxy);
        }
    }

    public void setUiccApplication(UiccCardApplication uiccCardApplication) {
        this.mUiccCardApplication = uiccCardApplication;
    }

    public void registerForFdnStatusChange(Handler h) {
        if (this.mUiccCardApplication != null && HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            this.mUiccCardApplication.registerForFdnStatusChange(h, 1, null);
        }
    }

    public void unregisterForFdnStatusChange(Handler h) {
        if (this.mUiccCardApplication != null && HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            this.mUiccCardApplication.unregisterForFdnStatusChange(h);
        }
    }

    public void queryFdn() {
        if (this.mUiccCardApplication != null && HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            this.mUiccCardApplication.queryFdn();
        }
    }

    public void handleCustMessage(Message msg) {
        switch (msg.what) {
            case 1:
                boolean isFdnActivated = false;
                int[] subId = SubscriptionController.getInstance().getSubId(iccCardProxyUtils.getPhoneId(this.mIccCardProxy));
                if (this.mUiccCardApplication != null) {
                    isFdnActivated = this.mUiccCardApplication.getIccFdnAvailable() ? this.mUiccCardApplication.getIccFdnEnabled() : false;
                }
                if (subId != null) {
                    if (subId[0] == 0) {
                        SystemProperties.set("gsm.hw.fdn.activated1", String.valueOf(isFdnActivated));
                    } else if (subId[0] == 1) {
                        SystemProperties.set("gsm.hw.fdn.activated2", String.valueOf(isFdnActivated));
                    }
                    Rlog.d(LOG_TAG, "fddn EVENT_FDN_STATUS_CHANGED ,set PROPERTY_FDN_ACTIVATED to:" + String.valueOf(isFdnActivated) + " ,subId:" + subId[0]);
                    UiccController.getInstance().notifyFdnStatusChange();
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void rlog(String s) {
        Rlog.d(LOG_TAG, s);
    }

    public int processCurrentAppType(UiccCard uiccCard, int defaultValue, int cardIndex) {
        boolean IS_CHINA_TELECOM;
        int currentAppType = defaultValue;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            IS_CHINA_TELECOM = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        } else {
            IS_CHINA_TELECOM = false;
        }
        boolean isCDMASimCard = HwTelephonyManagerInner.getDefault().isCDMASimCard(cardIndex);
        Rlog.d(LOG_TAG, "mCardIndex = " + cardIndex + ", isCDMASimCard = " + isCDMASimCard);
        if (HwModemCapability.isCapabilitySupport(14)) {
            if ((isCDMASimCard || IS_CHINA_TELECOM) && 2 == defaultValue && uiccCard.getCdmaSubscriptionAppIndex() < 0 && uiccCard.getGsmUmtsSubscriptionAppIndex() >= 0) {
                Rlog.d(LOG_TAG, "defaultValue = " + defaultValue + ", currentAppType = " + defaultValue);
                currentAppType = 1;
            }
            if (1 == currentAppType && uiccCard.getCdmaSubscriptionAppIndex() >= 0) {
                currentAppType = 2;
                Rlog.d(LOG_TAG, "mCurrentAppType changes from APP_FAM_3GPP to APP_FAM_3GPP2");
            }
            return currentAppType;
        }
        if (IS_CHINA_TELECOM || isCDMASimCard) {
            Phone phone = PhoneFactory.getPhone(cardIndex);
            if (phone != null && 1 == phone.getPhoneType()) {
                currentAppType = 1;
            }
            if (phone != null && 2 == phone.getPhoneType()) {
                currentAppType = 2;
            }
        }
        return currentAppType;
    }

    public Integer getUiccIndex(Message msg) {
        Integer index = Integer.valueOf(0);
        if (msg == null || msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = msg.obj;
        if (ar.result == null || !(ar.result instanceof Integer)) {
            return index;
        }
        return ar.result;
    }

    private boolean isAirplaneMode(Context context) {
        return System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    public boolean isSimAbsent(Context context, UiccCard uiccCard, boolean radioOn) {
        boolean mDSDSPowerup = SystemProperties.getBoolean("ro.config.hw_dsdspowerup", false);
        boolean bDSDA = MultiSimVariants.DSDA == TelephonyManager.getDefault().getMultiSimConfiguration();
        if ((!isAirplaneMode(context) || mDSDSPowerup || bDSDA) && uiccCard != null) {
            return true;
        }
        return false;
    }

    public void processSimLockStateForCT() {
        boolean IS_CHINA_TELECOM;
        State newState = this.mIccCardProxy.getState();
        int mSlotId = iccCardProxyUtils.getPhoneId(this.mIccCardProxy);
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            IS_CHINA_TELECOM = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        } else {
            IS_CHINA_TELECOM = false;
        }
        if (IS_CHINA_TELECOM && mSlotId == 0 && (SystemProperties.getBoolean("ro.hwpp.simlock_no_pop_for_ct", false) ^ 1) == 0) {
            if (State.CARD_IO_ERROR == newState && (this.mBroadcastForNotCT4GCardDone ^ 1) != 0) {
                this.mBroadcastForNotCT4GCardDone = true;
                broadcastForHwCardManager();
            } else if (State.ABSENT == newState) {
                rlog("reset mBroadcastForNotCT4GCardDone!");
                this.mBroadcastForNotCT4GCardDone = false;
            }
        }
    }

    private void broadcastForHwCardManager() {
        rlog("[broadcastForHwCardManager]");
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        intent.putExtra("popupDialog", "true");
        ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
    }

    public void custResetExternalState(State s) {
        synchronized (this.mLock) {
            this.mExternalState = HwState.UNKNOWN;
            logd("[custResetExternalState] reset mExternalState when State is" + s);
        }
    }

    public State modifySimStateForVsim(int phoneId, State s) {
        return HwVSimUtils.modifySimStateForVsim(phoneId, s);
    }
}
