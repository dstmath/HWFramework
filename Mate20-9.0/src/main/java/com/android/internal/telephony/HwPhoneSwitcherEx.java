package com.android.internal.telephony;

import android.content.Context;
import android.net.MatchAllNetworkSpecifier;
import android.net.NetworkCapabilities;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import java.util.List;

public final class HwPhoneSwitcherEx implements IHwPhoneSwitcherEx {
    private static final int ALLOW_DATA_RETRY_DELAY = 15000;
    private static final String LOG_TAG = "HwPhoneSwitcherEx";
    private static final int MAX_CONNECT_FAILURE_COUNT = 10;
    private static final int NONUSER_INITIATED_SWITCH = 1;
    private static final int SOFT_SWITCH_ACTIVE_PHONES_NUM = 2;
    private static final int SWITCH_DISABLED = 0;
    private static final int SWITCH_ENABLED = 1;
    private static final String TELEPHONY_SOFT_SWITCH = "telephony_soft_switch";
    private static final int USER_INITIATED_SWITCH = 0;
    private int[] mAllowDataFailure;
    private boolean mManualDdsSwitch = false;
    private int mNumPhones;
    private IHwPhoneSwitcherInner mPhoneSwitcherInner = null;
    private SubscriptionController mSubscriptionController = null;

    public HwPhoneSwitcherEx(IHwPhoneSwitcherInner phoneSwitcher, int numPhones) {
        this.mPhoneSwitcherInner = phoneSwitcher;
        this.mSubscriptionController = SubscriptionController.getInstance();
        this.mNumPhones = numPhones;
        this.mAllowDataFailure = new int[numPhones];
    }

    public void addNetworkCapability(NetworkCapabilities netCap) {
        netCap.addCapability(23);
        netCap.addCapability(24);
        netCap.addCapability(25);
        netCap.addCapability(26);
        netCap.addCapability(27);
        netCap.addCapability(28);
        netCap.addCapability(29);
        netCap.addCapability(30);
    }

    public NetworkCapabilities generateNetCapForVowifi() {
        NetworkCapabilities netCapForVowifi = new NetworkCapabilities();
        netCapForVowifi.addTransportType(1);
        netCapForVowifi.addCapability(0);
        netCapForVowifi.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        return netCapForVowifi;
    }

    public void handleMessage(Handler phoneSwitcherHandler, Phone[] phones, Message msg) {
        switch (msg.what) {
            case 110:
                onAllowDataResponse(phoneSwitcherHandler, phones, msg.arg1, (AsyncResult) msg.obj);
                return;
            case 111:
                onRetryAllowData(msg.arg1);
                return;
            case 112:
                log("EVENT_VOICE_CALL_ENDED");
                int ddsPhoneId = this.mSubscriptionController.getPhoneId(this.mSubscriptionController.getDefaultDataSubId());
                if (SubscriptionManager.isValidPhoneId(ddsPhoneId) && !isAnyVoiceCallActiveOnDevice() && getConnectFailureCount(ddsPhoneId) > 0) {
                    this.mPhoneSwitcherInner.resendDataAllowedForEx(ddsPhoneId);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void onRetryAllowData(int phoneId) {
        if (this.mSubscriptionController.getPhoneId(this.mSubscriptionController.getDefaultDataSubId()) == phoneId) {
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
        int ddsPhoneId = this.mSubscriptionController.getPhoneId(this.mSubscriptionController.getDefaultDataSubId());
        if (ddsPhoneId > 0 && ddsPhoneId < this.mNumPhones && phoneId == ddsPhoneId) {
            log("ALLOW_DATA retries exhausted on phoneId = " + phoneId);
            enforceDds(ddsPhoneId);
        }
    }

    private void enforceDds(int phoneId) {
        int[] subId = this.mSubscriptionController.getSubId(phoneId);
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
            if (getConnectFailureCount(phoneId) >= 10) {
                handleConnectMaxFailure(phoneId);
            } else {
                log("Scheduling retry connect/allow_data");
                if (phoneSwitcherHandler.hasMessages(111, phones[phoneId])) {
                    log("already has EVENT_RETRY_ALLOW_DATA, phoneId: " + phoneId + ", remove it and reset count");
                    phoneSwitcherHandler.removeMessages(111, phones[phoneId]);
                    resetConnectFailureCount(phoneId);
                }
                phoneSwitcherHandler.sendMessageDelayed(phoneSwitcherHandler.obtainMessage(111, phoneId, 0, phones[phoneId]), 15000);
            }
        } else {
            log("Allow_data success on phoneId = " + phoneId);
            resetConnectFailureCount(phoneId);
            this.mPhoneSwitcherInner.getActivePhoneRegistrants(phoneId).notifyRegistrants();
        }
    }

    public int getTopPrioritySubscriptionId(List<DcRequest> prioritizedDcRequests, int[] phoneSubscriptions) {
        if (VSimUtilsInner.isVSimOn()) {
            return VSimUtilsInner.getTopPrioritySubscriptionId();
        }
        if (prioritizedDcRequests.size() > 0) {
            DcRequest request = prioritizedDcRequests.get(0);
            if (request != null) {
                int phoneId = this.mPhoneSwitcherInner.phoneIdForRequestForEx(request.networkRequest, request.apnId);
                if (phoneId >= 0 && phoneId < phoneSubscriptions.length) {
                    return phoneSubscriptions[phoneId];
                }
            }
        }
        return SubscriptionManager.getDefaultDataSubscriptionId();
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

    private void log(String l) {
        Rlog.d(LOG_TAG, l);
    }
}
