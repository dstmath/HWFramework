package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwCarrierConfigCardManager;
import com.android.internal.telephony.HwIccCardConstants;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.gsm.HwGsmMmiCode;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.app.ActivityManagerNativeEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.RegistrantEx;
import com.huawei.android.os.RegistrantListEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import com.huawei.internal.telephony.uicc.UiccProfileEx;

public class HwIccCardProxyReference extends Handler implements IHwUiccProfileEx {
    private static final int EVENT_FDN_STATUS_CHANGED = 1;
    private static final int EVENT_ICC_REFRESH = 500;
    public static final String INTENT_KEY_SUBSCRIPTION_MODE = "sub_mode";
    private static final String LOG_TAG = "HwIccCardProxyReference";
    private static final int PHY_SLOT_0 = 0;
    private boolean mBroadcastForNotCT4GCardDone = false;
    HwIccCardConstants.HwState mExternalState = HwIccCardConstants.HwState.UNKNOWN;
    private IUiccProfileInner mIccCardProxy;
    boolean mIccCardStateHW;
    private final Object mLock = new Object();
    private RegistrantListEx mNetworkLockedRegistrants = new RegistrantListEx();
    private UiccCardApplicationEx mUiccCardApplicationEx = null;
    private UiccProfileEx mUiccProfileEx;

    public HwIccCardProxyReference(IUiccProfileInner iccCardProxy) {
        this.mIccCardProxy = iccCardProxy;
        this.mUiccProfileEx = new UiccProfileEx();
        this.mUiccProfileEx.setUiccProfile(iccCardProxy);
    }

    public void custSetExternalState(IccCardApplicationStatusEx.PersoSubStateEx ps) {
        synchronized (this.mLock) {
            HwIccCardConstants.HwState oldState = this.mExternalState;
            this.mExternalState = processPersoSubState(ps);
            int callbackHwState = processHwState(this.mExternalState);
            if (oldState != this.mExternalState) {
                setSystemProperty("gsm.sim.state", this.mIccCardProxy.getPhoneIdHw(), this.mExternalState.toString());
                broadcastIccStateChangedIntent(this.mExternalState.getIntentString(), this.mExternalState.getReason());
                logd("mExternalState = " + this.mExternalState + ", mExternalState = " + this.mExternalState.toString() + ", getIntentString = " + this.mExternalState.getIntentString() + ", getReason = " + this.mExternalState.getReason());
                this.mNetworkLockedRegistrants.notifyRegistrants((Object) null, Integer.valueOf(callbackHwState), (Throwable) null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int processHwState(HwIccCardConstants.HwState hs) {
        switch (AnonymousClass1.$SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[hs.ordinal()]) {
            case 1:
                return 3;
            case 2:
                return 4;
            case 3:
                return 5;
            case 4:
                return 6;
            case 5:
                return 7;
            case 6:
                return 8;
            case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                return 9;
            case 8:
                return 10;
            case HwGsmMmiCode.MATCH_GROUP_SIB /* 9 */:
                return 11;
            default:
                return 0;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.uicc.HwIccCardProxyReference$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState = new int[HwIccCardConstants.HwState.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx = new int[IccCardApplicationStatusEx.PersoSubStateEx.values().length];

        static {
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK_SUBSET.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_CORPORATE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_SERVICE_PROVIDER.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_SIM.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK_PUK.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_CORPORATE_PUK.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_SIM_SIM_PUK.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_NETWORK1.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_NETWORK2.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_HRPD.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_CORPORATE.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_RUIM.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_NETWORK1_PUK.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_NETWORK2_PUK.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_HRPD_PUK.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_CORPORATE_PUK.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER_PUK.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[IccCardApplicationStatusEx.PersoSubStateEx.PERSOSUBSTATE_RUIM_RUIM_PUK.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwIccCardConstants.HwState.NETWORK_LOCKED.ordinal()] = 1;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwIccCardConstants.HwState.SIM_NETWORK_SUBSET_LOCKED.ordinal()] = 2;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwIccCardConstants.HwState.SIM_CORPORATE_LOCKED.ordinal()] = 3;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwIccCardConstants.HwState.SIM_SERVICE_PROVIDER_LOCKED.ordinal()] = 4;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwIccCardConstants.HwState.SIM_SIM_LOCKED.ordinal()] = 5;
            } catch (NoSuchFieldError e27) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwIccCardConstants.HwState.SIM_NETWORK_LOCKED_PUK.ordinal()] = 6;
            } catch (NoSuchFieldError e28) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwIccCardConstants.HwState.SIM_NETWORK_SUBSET_LOCKED_PUK.ordinal()] = 7;
            } catch (NoSuchFieldError e29) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwIccCardConstants.HwState.SIM_CORPORATE_LOCKED_PUK.ordinal()] = 8;
            } catch (NoSuchFieldError e30) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$HwIccCardConstants$HwState[HwIccCardConstants.HwState.SIM_SERVICE_PROVIDER_LOCKED_PUK.ordinal()] = 9;
            } catch (NoSuchFieldError e31) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public HwIccCardConstants.HwState processPersoSubState(IccCardApplicationStatusEx.PersoSubStateEx ps) {
        HwIccCardConstants.HwState result = HwIccCardConstants.HwState.UNKNOWN;
        switch (AnonymousClass1.$SwitchMap$com$huawei$internal$telephony$uicc$IccCardApplicationStatusEx$PersoSubStateEx[ps.ordinal()]) {
            case 1:
                return HwIccCardConstants.HwState.NETWORK_LOCKED;
            case 2:
                return HwIccCardConstants.HwState.SIM_NETWORK_SUBSET_LOCKED;
            case 3:
                return HwIccCardConstants.HwState.SIM_CORPORATE_LOCKED;
            case 4:
                return HwIccCardConstants.HwState.SIM_SERVICE_PROVIDER_LOCKED;
            case 5:
                return HwIccCardConstants.HwState.SIM_SIM_LOCKED;
            case 6:
                return HwIccCardConstants.HwState.SIM_NETWORK_LOCKED_PUK;
            case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                return HwIccCardConstants.HwState.SIM_NETWORK_SUBSET_LOCKED_PUK;
            case 8:
                return HwIccCardConstants.HwState.SIM_CORPORATE_LOCKED_PUK;
            case HwGsmMmiCode.MATCH_GROUP_SIB /* 9 */:
                return HwIccCardConstants.HwState.SIM_SERVICE_PROVIDER_LOCKED_PUK;
            case 10:
            case 11:
            case HwGsmMmiCode.MATCH_GROUP_SIC /* 12 */:
            case 13:
            case 14:
            case HwGsmMmiCode.MATCH_GROUP_PWD_CONFIRM /* 15 */:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            default:
                return result;
        }
    }

    /* access modifiers changed from: package-private */
    public void broadcastIccStateChangedIntent(String value, String reason) {
        synchronized (this.mLock) {
            if (!SubscriptionManagerEx.isValidSlotIndex(this.mIccCardProxy.getPhoneIdHw())) {
                logd("broadcastIccStateChangedIntent: Card Index is not set; Return!!");
                return;
            }
            Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
            intent.addFlags(536870912);
            intent.putExtra("phoneName", "Phone");
            intent.putExtra("ss", value);
            intent.putExtra("reason", reason);
            SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent, this.mIccCardProxy.getPhoneIdHw());
            logd("Broadcasting intent ACTION_SIM_STATE_CHANGED " + value + " reason " + reason + " for mPhoneId : " + this.mIccCardProxy.getPhoneIdHw());
            ActivityManagerNativeEx.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
        }
    }

    private void setSystemProperty(String property, int slotId, String value) {
        TelephonyManagerEx.setTelephonyProperty(slotId, property, value);
    }

    public void supplyDepersonalization(String pin, int type, Message onComplete) {
        logd("supplyDepersonalization");
        synchronized (this.mLock) {
            this.mIccCardProxy.getCiHw().supplyDepersonalization(pin, type, onComplete);
        }
    }

    /* access modifiers changed from: package-private */
    public void logd(String s) {
        RlogEx.i(LOG_TAG, "[" + this.mIccCardProxy.getPhoneIdHw() + "]" + s);
    }

    public boolean getIccCardStateHw() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIccCardStateHW;
        }
        return z;
    }

    public void custUpdateExternalState(IccCardConstantsEx.StateEx s) {
        if (s == IccCardConstantsEx.StateEx.ABSENT || s == IccCardConstantsEx.StateEx.PIN_REQUIRED || s == IccCardConstantsEx.StateEx.PUK_REQUIRED || s == IccCardConstantsEx.StateEx.NETWORK_LOCKED) {
            this.mIccCardStateHW = true;
        } else {
            this.mIccCardStateHW = false;
        }
    }

    public void custRegisterForNetworkLocked(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            this.mNetworkLockedRegistrants.add(new RegistrantEx(h, what, obj));
        }
    }

    public void custUnregisterForNetworkLocked(Handler h) {
        synchronized (this.mLock) {
            this.mNetworkLockedRegistrants.remove(h);
        }
    }

    public void handleMessageExtend(Message msg) {
        if (msg.what != EVENT_ICC_REFRESH) {
            rlog("not handled event");
            return;
        }
        rlog("handleMessage get message EVENT_ICC_REFRESH");
        this.mIccCardProxy.broadcastIccStateChangedIntentHw("SIM_REFRESH", (String) null);
    }

    public void registerUiccCardEventsExtend() {
        if (this.mIccCardProxy.getIccRecordsHw() != null) {
            this.mIccCardProxy.getIccRecordsHw().registerForIccRefresh(this.mUiccProfileEx.getHandler(), (int) EVENT_ICC_REFRESH, (Object) null);
        }
    }

    public void unregisterUiccCardEventsExtend() {
        if (this.mIccCardProxy.getIccRecordsHw() != null) {
            this.mIccCardProxy.getIccRecordsHw().unRegisterForIccRefresh(this.mUiccProfileEx.getHandler());
        }
    }

    public void setUiccApplication(UiccCardApplicationEx uiccCardApplicationEx) {
        this.mUiccCardApplicationEx = uiccCardApplicationEx;
    }

    public void registerForFdnStatusChange(Handler h) {
        if (this.mUiccCardApplicationEx != null && HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            this.mUiccCardApplicationEx.registerForFdnStatusChange(h, 1, (Object) null);
        }
    }

    public void unregisterForFdnStatusChange(Handler h) {
        if (this.mUiccCardApplicationEx != null && HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            this.mUiccCardApplicationEx.unregisterForFdnStatusChange(h);
        }
    }

    public void queryFdn() {
        if (this.mUiccCardApplicationEx != null && HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            this.mUiccCardApplicationEx.queryFdn();
        }
    }

    public void handleCustMessage(Message msg) {
        if (msg.what == 1) {
            boolean isFdnActivated = false;
            int slotId = this.mIccCardProxy.getPhoneIdHw();
            int[] subId = SubscriptionManagerEx.getSubId(slotId);
            UiccCardApplicationEx uiccCardApplicationEx = this.mUiccCardApplicationEx;
            if (uiccCardApplicationEx != null) {
                isFdnActivated = uiccCardApplicationEx.getIccFdnAvailable() && this.mUiccCardApplicationEx.getIccFdnEnabled();
            }
            if (subId != null) {
                if (slotId == 0) {
                    SystemPropertiesEx.set("gsm.hw.fdn.activated1", String.valueOf(isFdnActivated));
                } else if (1 == slotId) {
                    SystemPropertiesEx.set("gsm.hw.fdn.activated2", String.valueOf(isFdnActivated));
                }
                logd("fddn EVENT_FDN_STATUS_CHANGED ,set PROPERTY_FDN_ACTIVATED to:" + String.valueOf(isFdnActivated) + " ,slotId:" + slotId);
                UiccControllerExt.getInstance().notifyFdnStatusChange();
            }
        }
    }

    private void rlog(String s) {
        RlogEx.i(LOG_TAG, s);
    }

    public int processCurrentAppType(UiccCardExt uiccCard, int defaultValue, int cardIndex) {
        int currentAppType = defaultValue;
        boolean IS_CHINA_TELECOM = SystemPropertiesEx.get("ro.config.hw_opta", "0").equals("92") && SystemPropertiesEx.get("ro.config.hw_optb", "0").equals("156");
        boolean isCDMASimCard = HwTelephonyManagerInner.getDefault().isCDMASimCard(cardIndex);
        logd("mCardIndex = " + cardIndex + ", isCDMASimCard = " + isCDMASimCard);
        if (!HwModemCapability.isCapabilitySupport(14)) {
            if (true != IS_CHINA_TELECOM && !isCDMASimCard) {
                return currentAppType;
            }
            PhoneExt phone = PhoneFactoryExt.getPhone(cardIndex);
            if (phone != null && 1 == phone.getPhoneType()) {
                currentAppType = UiccControllerExt.APP_FAM_3GPP;
            }
            if (phone == null || 2 != phone.getPhoneType()) {
                return currentAppType;
            }
            return UiccControllerExt.APP_FAM_3GPP2;
        } else if (uiccCard == null) {
            return currentAppType;
        } else {
            if ((isCDMASimCard || IS_CHINA_TELECOM) && UiccControllerExt.APP_FAM_3GPP2 == currentAppType && uiccCard.getCdmaSubscriptionAppIndex() < 0 && uiccCard.getGsmUmtsSubscriptionAppIndex() >= 0) {
                logd("defaultValue = " + defaultValue + ", currentAppType = " + currentAppType);
                currentAppType = UiccControllerExt.APP_FAM_3GPP;
            }
            if (UiccControllerExt.APP_FAM_3GPP != currentAppType || uiccCard.getCdmaSubscriptionAppIndex() < 0) {
                return currentAppType;
            }
            int currentAppType2 = UiccControllerExt.APP_FAM_3GPP2;
            logd("mCurrentAppType changes from APP_FAM_3GPP to APP_FAM_3GPP2");
            return currentAppType2;
        }
    }

    public int getUiccIndex(int requestPhoneId, Message msg) {
        AsyncResultEx ar;
        if (msg == null || (ar = AsyncResultEx.from(msg.obj)) == null || ar.getResult() == null || !(ar.getResult() instanceof Integer)) {
            return 0;
        }
        return ((Integer) ar.getResult()).intValue();
    }

    private boolean isAirplaneMode(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    public boolean isSimAbsent(Context context, UiccCardExt uiccCard) {
        boolean mDSDSPowerup = SystemPropertiesEx.getBoolean("ro.config.hw_dsdspowerup", false);
        if ((!isAirplaneMode(context) || mDSDSPowerup) && uiccCard != null) {
            return true;
        }
        return false;
    }

    public void processSimLockStateForCT() {
        IccCardConstantsEx.StateEx newState = this.mUiccProfileEx.getState();
        int mSlotId = this.mIccCardProxy.getPhoneIdHw();
        if ((SystemPropertiesEx.get("ro.config.hw_opta", "0").equals("92") && SystemPropertiesEx.get("ro.config.hw_optb", "0").equals("156")) && mSlotId == 0 && SystemPropertiesEx.getBoolean("ro.hwpp.simlock_no_pop_for_ct", false)) {
            if (IccCardConstantsEx.StateEx.CARD_IO_ERROR == newState && !this.mBroadcastForNotCT4GCardDone) {
                this.mBroadcastForNotCT4GCardDone = true;
                broadcastForHwCardManager();
            } else if (IccCardConstantsEx.StateEx.ABSENT == newState) {
                rlog("reset mBroadcastForNotCT4GCardDone!");
                this.mBroadcastForNotCT4GCardDone = false;
            }
        }
    }

    private void broadcastForHwCardManager() {
        rlog("[broadcastForHwCardManager]");
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        intent.putExtra("popupDialog", "true");
        ActivityManagerNativeEx.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
    }

    public void custResetExternalState() {
        synchronized (this.mLock) {
            this.mExternalState = HwIccCardConstants.HwState.UNKNOWN;
            logd("[custResetExternalState] reset mExternalState");
        }
    }

    public IccCardConstantsEx.StateEx modifySimStateForVsim(int phoneId, IccCardConstantsEx.StateEx s) {
        if (HwTelephonyManager.getDefault().isPlatformSupportVsim()) {
            return HwVSimUtils.modifySimStateForVsim(phoneId, s);
        }
        return s;
    }
}
