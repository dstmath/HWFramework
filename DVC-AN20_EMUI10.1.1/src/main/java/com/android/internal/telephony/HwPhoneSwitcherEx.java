package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.MatchAllNetworkSpecifier;
import android.net.NetworkCapabilities;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.android.internal.telephony.dataconnection.InCallDataStateMachine;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import java.util.List;

public final class HwPhoneSwitcherEx implements IHwPhoneSwitcherEx {
    private static final int ALLOW_DATA_RETRY_DELAY = 15000;
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
    private static final boolean SUPPORT_SMART_DUAL_CARD = SystemProperties.getBoolean("ro.odm.smart_dual_card", false);
    private static final int SWITCHING_STATE = 1;
    private static final int SWITCH_DISABLED = 0;
    private static final int SWITCH_ENABLED = 1;
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    private static final String TELEPHONY_SOFT_SWITCH = "telephony_soft_switch";
    private static final int USER_INITIATED_SWITCH = 0;
    private int[] mAllowDataFailure;
    private Context mContext;
    private int mDsdsMode = -1;
    private boolean mIsInNsaState = false;
    private boolean mIsNrAvailable = false;
    private boolean mManualDdsSwitch = false;
    private boolean mNrServiceOn = false;
    private int mNumPhones;
    private BroadcastReceiver mPhoneSwitcherBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwPhoneSwitcherEx.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (InCallDataStateMachine.ACTION_HW_DSDS_MODE_STATE.equals(action)) {
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
        this.mNumPhones = numPhones;
        this.mAllowDataFailure = new int[numPhones];
        this.mContext = phoneSwitcher.getPhoneContext();
        this.mSmartSwitchState = Settings.Global.getInt(this.mContext.getContentResolver(), SETTINGS_INTELLIGENCE_CARD_SWITCH, 0);
        log("init smart switch state: " + this.mSmartSwitchState);
        this.mPhoneSwitcherHandler = new PhoneSwitcherHandler();
        if (SUPPORT_SMART_DUAL_CARD) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(InCallDataStateMachine.ACTION_HW_DSDS_MODE_STATE);
            filter.addAction("android.intent.action.SERVICE_STATE");
            this.mContext.registerReceiver(this.mPhoneSwitcherBroadcastReceiver, filter);
            this.mSmartSwitchObserver = new SmartSwitchObserver(new Handler());
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SETTINGS_INTELLIGENCE_CARD_SWITCH), true, this.mSmartSwitchObserver);
            ContentResolver cr = this.mContext.getContentResolver();
            if (this.mPreferNetworkDb == null && cr != null) {
                this.mPreferNetworkDb = new PreferNetworkDb(new Handler());
                cr.registerContentObserver(Settings.Global.getUriFor("preferred_network_mode"), true, this.mPreferNetworkDb);
            }
            this.mNrServiceOn = HwNetworkTypeUtils.isNrServiceOn(Settings.Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode", -1));
        }
    }

    public void addNetworkCapability(NetworkCapabilities netCap) {
        netCap.addCapability(25);
        netCap.addCapability(26);
        netCap.addCapability(27);
        netCap.addCapability(28);
        netCap.addCapability(29);
        netCap.addCapability(30);
        netCap.addCapability(31);
        netCap.addCapability(32);
    }

    public NetworkCapabilities generateNetCapForVowifi() {
        NetworkCapabilities netCapForVowifi = new NetworkCapabilities();
        netCapForVowifi.addTransportType(1);
        netCapForVowifi.addCapability(0);
        netCapForVowifi.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        return netCapForVowifi;
    }

    public void handleMessage(Handler phoneSwitcherHandler, Phone[] phones, Message msg) {
        int i = msg.what;
        if (i == 111) {
            onRetryAllowData(msg.arg1);
        } else if (i == 112) {
            log("EVENT_VOICE_CALL_ENDED");
            int ddsPhoneId = SubscriptionManagerEx.getPhoneId(this.mSubscriptionController.getDefaultDataSubId());
            if (SubscriptionManager.isValidPhoneId(ddsPhoneId) && !isAnyVoiceCallActiveOnDevice() && getConnectFailureCount(ddsPhoneId) > 0) {
                this.mPhoneSwitcherInner.resendDataAllowedForEx(ddsPhoneId);
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
        this.mAllowDataFailure[phoneId] = 0;
    }

    private void incConnectFailureCount(int phoneId) {
        int[] iArr = this.mAllowDataFailure;
        iArr[phoneId] = iArr[phoneId] + 1;
    }

    private int getConnectFailureCount(int phoneId) {
        return this.mAllowDataFailure[phoneId];
    }

    private void handleConnectMaxFailure(int phoneId) {
        resetConnectFailureCount(phoneId);
        int ddsPhoneId = SubscriptionManagerEx.getPhoneId(this.mSubscriptionController.getDefaultDataSubId());
        if (ddsPhoneId > 0 && ddsPhoneId < this.mNumPhones && phoneId == ddsPhoneId) {
            log("ALLOW_DATA retries exhausted on phoneId = " + phoneId);
            enforceDds(ddsPhoneId);
        }
    }

    private void enforceDds(int phoneId) {
        int[] subId = SubscriptionManagerEx.getSubId(phoneId);
        log("enforceDds: subId = " + subId[0]);
        this.mSubscriptionController.setDefaultDataSubId(subId[0]);
    }

    private boolean isAnyVoiceCallActiveOnDevice() {
        boolean ret = CallManager.getInstance().getState() != PhoneConstants.State.IDLE;
        log("isAnyVoiceCallActiveOnDevice: " + ret);
        return ret;
    }

    private void onAllowDataResponse(Handler phoneSwitcherHandler, Phone[] phones, int phoneId, AsyncResult ar) {
        if (ar.userObj != null) {
            Message message = (Message) ar.userObj;
            AsyncResult.forMessage(message, ar.result, ar.exception);
            message.sendToTarget();
        }
        if (ar.exception != null) {
            incConnectFailureCount(phoneId);
            if (isAnyVoiceCallActiveOnDevice()) {
                log("Wait for call end indication");
                return;
            }
            log("Allow_data failed on phoneId = " + phoneId + ", failureCount = " + getConnectFailureCount(phoneId));
            if (getConnectFailureCount(phoneId) >= MAX_CONNECT_FAILURE_COUNT) {
                handleConnectMaxFailure(phoneId);
                return;
            }
            log("Scheduling retry connect/allow_data");
            if (phoneSwitcherHandler.hasMessages(111, phones[phoneId])) {
                log("already has EVENT_RETRY_ALLOW_DATA, phoneId: " + phoneId + ", remove it and reset count");
                phoneSwitcherHandler.removeMessages(111, phones[phoneId]);
                resetConnectFailureCount(phoneId);
            }
            phoneSwitcherHandler.sendMessageDelayed(phoneSwitcherHandler.obtainMessage(111, phoneId, 0, phones[phoneId]), 15000);
            return;
        }
        log("Allow_data success on phoneId = " + phoneId);
        resetConnectFailureCount(phoneId);
        this.mPhoneSwitcherInner.getActivePhoneRegistrants(phoneId).notifyRegistrants();
    }

    public int getTopPrioritySubscriptionId(List<DcRequest> prioritizedDcRequests, int[] phoneSubscriptions) {
        DcRequest request;
        int phoneId;
        if (VSimUtilsInner.isVSimOn()) {
            return VSimUtilsInner.getTopPrioritySubscriptionId();
        }
        if (prioritizedDcRequests == null || phoneSubscriptions == null || prioritizedDcRequests.size() <= 0 || (request = prioritizedDcRequests.get(0)) == null || (phoneId = this.mPhoneSwitcherInner.phoneIdForRequestForEx(request.networkRequest, request.apnType)) < 0 || phoneId >= phoneSubscriptions.length) {
            return SubscriptionManager.getDefaultDataSubscriptionId();
        }
        return phoneSubscriptions[phoneId];
    }

    public int calActivePhonesNum(Context context, int maxActivePhones) {
        if (1 == Settings.System.getInt(context.getContentResolver(), TELEPHONY_SOFT_SWITCH, 0)) {
            return 2;
        }
        return maxActivePhones;
    }

    public void informDdsToQcril(int dataSub) {
        if (!HuaweiTelephonyConfigs.isQcomPlatform()) {
            return;
        }
        if (HwTelephonyFactory.getHwDataConnectionManager().isSwitchingToSlave()) {
            this.mSubscriptionController.informDdsToQcril(dataSub, 1);
        } else {
            this.mSubscriptionController.informDdsToQcril(dataSub, 0);
        }
    }

    public boolean isSmartSwitchOnSwithing() {
        if (SystemProperties.getInt("persist.sys.smart_switch_state", 0) == 1) {
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
    public class PhoneSwitcherHandler extends Handler {
        private PhoneSwitcherHandler() {
        }

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
        public SmartSwitchObserver(Handler handler) {
            super(handler);
        }

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
            if (PhoneFactory.MAX_ACTIVE_PHONES == 2) {
                z = true;
            }
            iHwPhoneSwitcherInner.onDualPsStateChanged(z, "smartSwitchChanged");
        }
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

    private class PreferNetworkDb extends ContentObserver {
        PreferNetworkDb(Handler handler) {
            super(handler);
        }

        public void onChange(boolean isSelfChange) {
            int curPreMode = Settings.Global.getInt(HwPhoneSwitcherEx.this.mContext.getContentResolver(), "preferred_network_mode", -1);
            HwPhoneSwitcherEx.this.mNrServiceOn = HwNetworkTypeUtils.isNrServiceOn(curPreMode);
            HwPhoneSwitcherEx hwPhoneSwitcherEx = HwPhoneSwitcherEx.this;
            hwPhoneSwitcherEx.log("Receive PREFERRED_NETWORK_MODE changed:" + HwPhoneSwitcherEx.this.mNrServiceOn + ", ignore..");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onServiceStateChanged(Intent intent) {
        boolean isNrAvailable = false;
        int currentPhoneId = intent.getIntExtra("slot", 0);
        int mainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (currentPhoneId == mainSlot) {
            Phone phone = PhoneFactory.getPhone(mainSlot);
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
            int configNetType = ss.getHwNetworkType();
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
        Rlog.i(LOG_TAG, l);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }
}
