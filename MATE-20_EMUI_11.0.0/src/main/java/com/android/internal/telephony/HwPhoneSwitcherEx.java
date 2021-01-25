package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import com.huawei.android.net.MatchAllNetworkSpecifierEx;
import com.huawei.android.net.NetworkCapabilitiesEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.CallManagerExt;
import com.huawei.internal.telephony.PhoneConstantsEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.dataconnection.DcRequestExt;
import com.huawei.internal.telephony.vsim.VSimUtilsInnerEx;
import java.util.List;

public final class HwPhoneSwitcherEx implements IHwPhoneSwitcherEx {
    private static final int ALLOW_DATA_RETRY_DELAY = 15000;
    private static final int DEFAULT_DSDS_MODE = -1;
    private static final int DEFAULT_SMART_SWITCH_STATE = -1;
    private static final int DEFAULT_VALUE_STATE = 0;
    private static final int DSDS2 = 0;
    private static final int DSDS3 = 1;
    private static final String DSDS_KEY = "dsdsmode";
    private static final int DSDS_STATE_UPDATE_DELAY_TIMER_MS = 1000;
    private static final int EVENT_DSDS_MODE_CHANGE = 1;
    private static final int INVALID_STATE = -1;
    private static final String LOG_TAG = "HwPhoneSwitcherEx";
    private static final int MAX_CONNECT_FAILURE_COUNT = 10;
    private static final int MAX_PS_NUM = 2;
    private static final int NONUSER_INITIATED_SWITCH = 1;
    private static final String SETTINGS_INTELLIGENCE_CARD_SWITCH = "intelligence_card_switch";
    private static final int SOFT_SWITCH_ACTIVE_PHONES_NUM = 2;
    private static final boolean SUPPORT_SMART_DUAL_CARD = SystemPropertiesEx.getBoolean("ro.odm.smart_dual_card", false);
    private static final int SWITCHING_STATE = 1;
    private static final int SWITCH_DISABLED = 0;
    private static final int SWITCH_ENABLED = 1;
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    private static final String TELEPHONY_SOFT_SWITCH = "telephony_soft_switch";
    private static final int USER_INITIATED_SWITCH = 0;
    private int[] mAllowDataFailures;
    private Context mContext;
    private int mDsdsMode = -1;
    private boolean mIsInNsaState = false;
    private boolean mIsNrAvailable = false;
    private boolean mNrServiceOn = false;
    private BroadcastReceiver mPhoneSwitcherBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwPhoneSwitcherEx.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("com.huawei.action.ACTION_HW_DSDS_MODE_STATE".equals(action)) {
                    HwPhoneSwitcherEx.this.onDsdsModeChanged(intent);
                }
                if ("android.intent.action.SERVICE_STATE".equals(action)) {
                    HwPhoneSwitcherEx.this.onServiceStateChanged(intent);
                }
            }
        }
    };
    private final PhoneSwitcherHandler mPhoneSwitcherHandler;
    private IHwPhoneSwitcherInner mPhoneSwitcherInner = null;
    private PreferNetworkDb mPreferNetworkDb = null;
    private SmartSwitchObserver mSmartSwitchObserver = null;
    private int mSmartSwitchState = -1;
    private SubscriptionControllerEx mSubscriptionController = null;

    public HwPhoneSwitcherEx(IHwPhoneSwitcherInner phoneSwitcher, int numPhones) {
        this.mPhoneSwitcherInner = phoneSwitcher;
        this.mSubscriptionController = SubscriptionControllerEx.getInstance();
        this.mAllowDataFailures = new int[numPhones];
        this.mContext = phoneSwitcher.getPhoneContext();
        this.mSmartSwitchState = Settings.Global.getInt(this.mContext.getContentResolver(), SETTINGS_INTELLIGENCE_CARD_SWITCH, 0);
        log("init smart switch state: " + this.mSmartSwitchState);
        this.mPhoneSwitcherHandler = new PhoneSwitcherHandler();
        if (SUPPORT_SMART_DUAL_CARD) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.huawei.action.ACTION_HW_DSDS_MODE_STATE");
            filter.addAction("android.intent.action.SERVICE_STATE");
            this.mContext.registerReceiver(this.mPhoneSwitcherBroadcastReceiver, filter);
            this.mSmartSwitchObserver = new SmartSwitchObserver(new Handler());
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SETTINGS_INTELLIGENCE_CARD_SWITCH), true, this.mSmartSwitchObserver);
            ContentResolver cr = this.mContext.getContentResolver();
            if (cr != null) {
                this.mPreferNetworkDb = new PreferNetworkDb(new Handler());
                cr.registerContentObserver(Settings.Global.getUriFor("preferred_network_mode"), true, this.mPreferNetworkDb);
            }
            this.mNrServiceOn = HwNetworkTypeUtils.isNrServiceOn(Settings.Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode", -1));
        }
    }

    public void addNetworkCapability(NetworkCapabilities netCap) {
        NetworkCapabilitiesEx nc = new NetworkCapabilitiesEx(netCap);
        nc.addCapability(25);
        nc.addCapability(26);
        nc.addCapability(27);
        nc.addCapability(28);
        nc.addCapability(29);
        nc.addCapability(30);
        nc.addCapability(31);
        nc.addCapability(32);
    }

    public NetworkCapabilities generateNetCapForVowifi() {
        NetworkCapabilitiesEx netCapForVowifi = new NetworkCapabilitiesEx();
        netCapForVowifi.addTransportType(1);
        netCapForVowifi.addCapability(0);
        netCapForVowifi.setNetworkSpecifier(new MatchAllNetworkSpecifierEx());
        return netCapForVowifi.getNetworkCapabilities();
    }

    public void handleMessage(Handler phoneSwitcherHandler, PhoneExt[] phones, Message msg) {
        if (msg != null) {
            int i = msg.what;
            if (i == 111) {
                onRetryAllowData(msg.arg1);
            } else if (i == 112) {
                log("EVENT_VOICE_CALL_ENDED");
                int ddsPhoneId = SubscriptionManagerEx.getPhoneId(this.mSubscriptionController.getDefaultDataSubId());
                if (SubscriptionManagerEx.isValidPhoneId(ddsPhoneId) && !isAnyVoiceCallActiveOnDevice() && getConnectFailureCount(ddsPhoneId) > 0) {
                    this.mPhoneSwitcherInner.resendDataAllowedForEx(ddsPhoneId);
                }
            }
        }
    }

    private void onRetryAllowData(int phoneId) {
        if (SubscriptionManagerEx.getPhoneId(this.mSubscriptionController.getDefaultDataSubId()) == phoneId) {
            log("Running retry connect/allow_data");
            this.mPhoneSwitcherInner.resendDataAllowedForEx(phoneId);
            return;
        }
        log("Dds sub changed");
        resetConnectFailureCount(phoneId);
    }

    private void resetConnectFailureCount(int phoneId) {
        this.mAllowDataFailures[phoneId] = 0;
    }

    private int getConnectFailureCount(int phoneId) {
        return this.mAllowDataFailures[phoneId];
    }

    private boolean isAnyVoiceCallActiveOnDevice() {
        boolean ret = CallManagerExt.getState() != PhoneConstantsEx.StateEx.IDLE.ordinal();
        log("isAnyVoiceCallActiveOnDevice: " + ret);
        return ret;
    }

    public int getTopPrioritySubscriptionId(List<DcRequestExt> prioritizedDcRequests, int[] phoneSubscriptions) {
        DcRequestExt request;
        int phoneId;
        if (VSimUtilsInnerEx.isVSimOn()) {
            return VSimUtilsInnerEx.getTopPrioritySubscriptionId();
        }
        if (prioritizedDcRequests == null || phoneSubscriptions == null || prioritizedDcRequests.size() <= 0 || (request = prioritizedDcRequests.get(0)) == null || request.getNetworkRequest() == null || (phoneId = this.mPhoneSwitcherInner.phoneIdForRequestForEx(request.getNetworkRequest(), request.getApnType())) < 0 || phoneId >= phoneSubscriptions.length) {
            return SubscriptionManager.getDefaultDataSubscriptionId();
        }
        return phoneSubscriptions[phoneId];
    }

    public int calActivePhonesNum(Context context, int maxActivePhones) {
        if (context == null) {
            return maxActivePhones;
        }
        if (1 == Settings.System.getInt(context.getContentResolver(), TELEPHONY_SOFT_SWITCH, 0)) {
            return 2;
        }
        return maxActivePhones;
    }

    public void informDdsToQcril(int dataSub) {
        if (!HuaweiTelephonyConfigs.isQcomPlatform()) {
            return;
        }
        if (HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyFactoryImpl").getHwDataConnectionManager().isSwitchingToSlave()) {
            this.mSubscriptionController.informDdsToQcril(dataSub, 1);
        } else {
            this.mSubscriptionController.informDdsToQcril(dataSub, 0);
        }
    }

    public boolean isSmartSwitchOnSwithing() {
        if (SystemPropertiesEx.getInt("persist.sys.smart_switch_state", 0) == 1) {
            return true;
        }
        return false;
    }

    public boolean isDualPsAllowedForSmartSwitch() {
        if (isSmartSwitchEnabled()) {
            return isDualPsAllowed();
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDualPsAllowed() {
        boolean isDualPsAllowed = true;
        if (this.mDsdsMode != 1 || (!this.mIsInNsaState && !this.mIsNrAvailable)) {
            isDualPsAllowed = false;
        }
        log("isDualPsAllowed " + isDualPsAllowed);
        return isDualPsAllowed;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDsdsModeChanged(Intent intent) {
        int newDsdsMode = intent.getIntExtra(DSDS_KEY, 0);
        log("onDsdsModeChanged mDsdsMode:" + this.mDsdsMode + ", newDsdsMode:" + newDsdsMode);
        boolean isNrSupport = HwTelephonyManager.getDefault().isNrSupported();
        if (this.mPhoneSwitcherHandler.hasMessages(1) && this.mDsdsMode == newDsdsMode) {
            this.mPhoneSwitcherHandler.removeMessages(1);
        }
        if (this.mDsdsMode == newDsdsMode) {
            return;
        }
        if (isNrSupport) {
            PhoneSwitcherHandler phoneSwitcherHandler = this.mPhoneSwitcherHandler;
            phoneSwitcherHandler.sendMessageDelayed(phoneSwitcherHandler.obtainMessage(1, newDsdsMode, 0), 1000);
            return;
        }
        PhoneSwitcherHandler phoneSwitcherHandler2 = this.mPhoneSwitcherHandler;
        phoneSwitcherHandler2.sendMessage(phoneSwitcherHandler2.obtainMessage(1, newDsdsMode, 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onServiceStateChanged(Intent intent) {
        boolean isNrAvailable = false;
        int currentPhoneId = intent.getIntExtra("slot", 0);
        int mainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (currentPhoneId == mainSlot) {
            PhoneExt phone = PhoneFactoryExt.getPhone(mainSlot);
            ServiceState ss = phone != null ? phone.getServiceState() : null;
            if (ss == null) {
                loge("null ss");
                return;
            }
            HwServiceStateTrackerEx hwSstEx = HwServiceStateTrackerEx.getInstance(mainSlot);
            if (hwSstEx == null) {
                loge("null hwSstEx");
                return;
            }
            int nsaState = hwSstEx.getChangedNsaState();
            log("onServiceStateChanged nsaState:" + nsaState);
            boolean isInNsaState = nsaState >= 2 && nsaState <= 5;
            if (isInNsaState != this.mIsInNsaState && this.mNrServiceOn) {
                log("isInNsaState changed from " + this.mIsInNsaState + " to " + isInNsaState);
                this.mIsInNsaState = isInNsaState;
                dualPsChangeProcessForSmartSwitch();
            }
            int configNetType = ServiceStateEx.getConfigRadioTechnology(ss);
            log("onServiceStateChanged config netType:" + configNetType);
            if (configNetType == 20 || configNetType == 13 || configNetType == 19) {
                isNrAvailable = true;
            }
            if (isNrAvailable != this.mIsNrAvailable) {
                log("isNrAvailable changed from " + this.mIsNrAvailable + " to " + isNrAvailable);
                this.mIsNrAvailable = isNrAvailable;
                dualPsChangeProcessForSmartSwitch();
            }
        }
    }

    private void sendDualPsStateBroadcast() {
        Intent intent = new Intent("com.huawei.action.ACTION_HW_DUAL_PS_STATE");
        intent.putExtra("dualPsAllowed", isDualPsAllowed());
        this.mContext.sendBroadcast(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dualPsChangeProcessForSmartSwitch() {
        if (isSmartSwitchEnabled()) {
            this.mPhoneSwitcherInner.onDualPsStateChanged(isDualPsAllowed(), "modemCfgChanged");
        }
        sendDualPsStateBroadcast();
    }

    private boolean isSmartSwitchEnabled() {
        int retVal = 0;
        if (SUPPORT_SMART_DUAL_CARD) {
            retVal = this.mSmartSwitchState;
        }
        return retVal == 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String l) {
        RlogEx.i(LOG_TAG, l);
    }

    private void loge(String s) {
        RlogEx.e(LOG_TAG, s);
    }

    /* access modifiers changed from: private */
    public class PhoneSwitcherHandler extends Handler {
        private PhoneSwitcherHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int newDsdsMode;
            if (msg.what == 1 && (newDsdsMode = msg.arg1) != HwPhoneSwitcherEx.this.mDsdsMode) {
                HwPhoneSwitcherEx hwPhoneSwitcherEx = HwPhoneSwitcherEx.this;
                hwPhoneSwitcherEx.log("EVENT_DSDS_MODE_CHANGE, dsds mode changed from " + HwPhoneSwitcherEx.this.mDsdsMode + " to " + newDsdsMode);
                HwPhoneSwitcherEx.this.mDsdsMode = newDsdsMode;
                HwPhoneSwitcherEx.this.dualPsChangeProcessForSmartSwitch();
            }
        }
    }

    private class SmartSwitchObserver extends ContentObserver {
        SmartSwitchObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            boolean z = false;
            int retVal = Settings.Global.getInt(HwPhoneSwitcherEx.this.mContext.getContentResolver(), HwPhoneSwitcherEx.SETTINGS_INTELLIGENCE_CARD_SWITCH, 0);
            HwPhoneSwitcherEx.this.log("Smart switch changed to " + retVal);
            if (retVal == 1) {
                HwPhoneSwitcherEx.this.mSmartSwitchState = 1;
                HwPhoneSwitcherEx.this.mPhoneSwitcherInner.onDualPsStateChanged(HwPhoneSwitcherEx.this.isDualPsAllowed(), "smartSwitchChanged");
                return;
            }
            HwPhoneSwitcherEx.this.mSmartSwitchState = 0;
            IHwPhoneSwitcherInner iHwPhoneSwitcherInner = HwPhoneSwitcherEx.this.mPhoneSwitcherInner;
            if (PhoneFactoryExt.MAX_ACTIVE_PHONES == 2) {
                z = true;
            }
            iHwPhoneSwitcherInner.onDualPsStateChanged(z, "smartSwitchChanged");
        }
    }

    private class PreferNetworkDb extends ContentObserver {
        PreferNetworkDb(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            int curPreMode = Settings.Global.getInt(HwPhoneSwitcherEx.this.mContext.getContentResolver(), "preferred_network_mode", -1);
            HwPhoneSwitcherEx.this.mNrServiceOn = HwNetworkTypeUtils.isNrServiceOn(curPreMode);
            HwPhoneSwitcherEx hwPhoneSwitcherEx = HwPhoneSwitcherEx.this;
            hwPhoneSwitcherEx.log("Receive PREFERRED_NETWORK_MODE changed:" + HwPhoneSwitcherEx.this.mNrServiceOn + ", ignore..");
        }
    }
}
