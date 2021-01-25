package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfigInner;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.PhoneStateListenerEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.util.LogEx;
import java.util.List;

public class HwDualCardSwitcher extends Handler {
    private static final int EVENT_AUTO_SWITCH_DONE = 2;
    private static final int EVENT_AUTO_SWITCH_TIMEOUT = 3;
    private static final int EVENT_PROMPT_TO_SWITCH = 1;
    private static final String EXTRA_DATA_SWITCH = "data_switch";
    private static final int MAX_TIME_WAIT_AUTO_SWITCH = 30000;
    private static final int MAX_TIME_WAIT_TO_PROMPT = 10000;
    private static final int MCCMNC_LEN_MINIMUM = 5;
    private static final int MCC_LENGTH = 3;
    private static final String MCC_OF_CN = "460";
    private static final Object M_LOCK = new Object();
    private static final int SIM_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final String TAG = "HwDualCardSwitcher";
    private static HwDualCardSwitcher mInstance;
    private int mAutoSwitchingCount = 0;
    private final Context mContext;
    private final SparseArray<ServiceStateListener> mListeners = new SparseArray<>();
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.android.internal.telephony.HwDualCardSwitcher.AnonymousClass1 */

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            HwDualCardSwitcher.this.logd("onSubscriptionsChanged");
            HwDualCardSwitcher.this.registerListeners();
        }
    };
    private int mPrimarySlot = 0;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwDualCardSwitcher.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwDualCardSwitcher.this.loge("intent is null, return.");
            } else if ("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED".equals(intent.getAction())) {
                HwDualCardSwitcher.this.processSubInfoRecordUpdated(intent);
            }
        }
    };
    private int mSecondarySlot = 1;
    private final ServiceState[] mServiceState = new ServiceState[SIM_NUM];
    private boolean mShouldAutoSwitch = false;

    private HwDualCardSwitcher(Context context) {
        this.mContext = context;
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED"));
        SubscriptionManager subscriptionManager = (SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service");
        if (subscriptionManager != null) {
            subscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        }
    }

    public static HwDualCardSwitcher make(Context context) {
        synchronized (M_LOCK) {
            if (mInstance != null) {
                throw new RuntimeException("HwDualCardSwitcher.make() should only be called once");
            }
            mInstance = new HwDualCardSwitcher(context);
        }
        return mInstance;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processSubInfoRecordUpdated(Intent intent) {
        if (HwFullNetworkConfigInner.isCustomVersion()) {
            logd("processSubInfoRecordUpdated, AIS/SMART/MTN custom verion. can't auto switch card.");
            return;
        }
        int status = intent.getIntExtra("simDetectStatus", -1);
        if (status != -1) {
            logd("sim state " + status);
        }
        if (status == 1 || status == 3) {
            logd("sim card changed, check if need to auto switch.");
            if (hasMessages(3)) {
                removeMessages(3);
            }
            sendMessageDelayed(obtainMessage(3), 30000);
            this.mShouldAutoSwitch = true;
            checkIfNeedToSwitchSlot();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerListeners() {
        List<SubscriptionInfo> subscriptions = SubscriptionControllerEx.getInstance() != null ? SubscriptionControllerEx.getInstance().getActiveSubscriptionInfoList(ContextEx.getOpPackageName(this.mContext)) : null;
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            int subId = this.mListeners.keyAt(i);
            if (!containsSubId(subscriptions, subId)) {
                int phoneId = this.mListeners.valueAt(i).mPhoneId;
                logd("cancel listen and revove = " + subId + ", phoneId = " + phoneId);
                if (SubscriptionManagerEx.isValidSlotIndex(phoneId)) {
                    this.mServiceState[phoneId] = null;
                }
                this.mListeners.valueAt(i).cancelListen(this.mContext);
                this.mListeners.remove(subId);
            }
        }
        if (subscriptions != null) {
            int subscriptionsSize = subscriptions.size();
            for (int i2 = 0; i2 < subscriptionsSize; i2++) {
                int subId2 = subscriptions.get(i2).getSubscriptionId();
                if (this.mListeners.indexOfKey(subId2) < 0) {
                    ServiceStateListener listener = new ServiceStateListener(subscriptions.get(i2).getSimSlotIndex(), subId2);
                    listener.listen(this.mContext);
                    this.mListeners.put(subId2, listener);
                }
            }
        }
    }

    private boolean containsSubId(List<SubscriptionInfo> subInfos, int subId) {
        if (subInfos == null) {
            return false;
        }
        int subInfoSize = subInfos.size();
        for (int i = 0; i < subInfoSize; i++) {
            if (subInfos.get(i).getSubscriptionId() == subId) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkIfNeedToSwitchSlot() {
        if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate() || !HwVSimUtils.isAllowALSwitch())) {
            logd("vsim is working, so return");
            return;
        }
        if (HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() != 0) {
            logd("MDM carrier is working, so return");
        } else if (!shouldSwitch() || HwFullNetworkManager.getInstance().get4GSlotInProgress() || HwFullNetworkManager.getInstance().isRestartRildProgress()) {
            if (hasMessages(1)) {
                removeMessages(1);
            }
        } else if (this.mShouldAutoSwitch) {
            HwFullNetworkManager.getInstance().setMainSlot(this.mSecondarySlot, obtainMessage(2, this.mSecondarySlot, 0));
            this.mAutoSwitchingCount++;
        } else if (hasMessages(1)) {
            if (this.mServiceState[this.mPrimarySlot].getState() == 0) {
                removeMessages(1);
                notifyDualCardsManager();
            }
        } else if (this.mServiceState[this.mPrimarySlot].getState() == 0) {
            notifyDualCardsManager();
        } else {
            sendMessageDelayed(obtainMessage(1), 10000);
        }
    }

    private boolean shouldSwitch() {
        boolean otherIsInService = false;
        boolean otherIsRoaming = false;
        String mccNoService = null;
        for (int sub = 0; sub < SIM_NUM; sub++) {
            if (this.mServiceState[sub] == null) {
                return false;
            }
        }
        if (HwFullNetworkConfigInner.isCustomVersion()) {
            logd("processSubInfoRecordUpdated, AIS/SMART/MTN custom verion. can't auto switch card.");
            return false;
        }
        this.mPrimarySlot = HwFullNetworkManager.getInstance().getUserSwitchDualCardSlots();
        this.mSecondarySlot = this.mPrimarySlot == 0 ? 1 : 0;
        String regPlmn = null;
        boolean primaryIsRoaming = false;
        boolean primaryIsInService = false;
        for (int sub2 = 0; sub2 < SIM_NUM; sub2++) {
            if (this.mPrimarySlot == sub2) {
                if (this.mServiceState[sub2].getState() == 0) {
                    primaryIsInService = true;
                    primaryIsRoaming = this.mServiceState[sub2].getRoaming();
                }
            } else if (this.mServiceState[sub2].getState() == 0) {
                otherIsInService = true;
                otherIsRoaming = this.mServiceState[sub2].getRoaming();
                regPlmn = this.mServiceState[sub2].getOperatorNumeric();
            }
        }
        if (otherIsInService && !otherIsRoaming) {
            HwTelephonyManagerInner hwTelephonyManagerInner = HwTelephonyManagerInner.getDefault();
            if (hwTelephonyManagerInner != null && hwTelephonyManagerInner.isCTSimCard(this.mSecondarySlot) && !TextUtils.isEmpty(regPlmn) && !regPlmn.startsWith(MCC_OF_CN)) {
                StringBuilder sb = new StringBuilder();
                sb.append("CT card not roaming, register ");
                sb.append(LogEx.getLogHWInfo() ? regPlmn : "***");
                logd(sb.toString());
                PhoneExt[] phones = PhoneFactoryExt.getPhones();
                if (!(phones == null || phones[this.mPrimarySlot] == null)) {
                    String imsi = phones[this.mPrimarySlot].getSubscriberId();
                    if (TextUtils.isEmpty(imsi) || imsi.length() < 5) {
                        loge("Invalid imsi of primary card.");
                        return false;
                    } else if (imsi.startsWith(MCC_OF_CN)) {
                        return false;
                    }
                }
            }
            if (primaryIsInService && primaryIsRoaming) {
                logd("primary card is roaming, should switch to slot " + this.mSecondarySlot);
                return true;
            } else if (primaryIsInService || (hwTelephonyManagerInner != null && hwTelephonyManagerInner.isCTSimCard(this.mPrimarySlot) && !TextUtils.isEmpty(regPlmn) && regPlmn.startsWith(MCC_OF_CN))) {
                return false;
            } else {
                PhoneExt[] phones2 = PhoneFactoryExt.getPhones();
                if (!(phones2 == null || phones2[this.mPrimarySlot] == null)) {
                    String imsi2 = phones2[this.mPrimarySlot].getSubscriberId();
                    if (TextUtils.isEmpty(imsi2) || imsi2.length() < 5) {
                        return false;
                    }
                    mccNoService = imsi2.substring(0, 3);
                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append("secondary card register ");
                sb2.append(LogEx.getLogHWInfo() ? regPlmn : "***");
                sb2.append(", primary card's mcc is ");
                sb2.append(LogEx.getLogHWInfo() ? mccNoService : "***");
                logd(sb2.toString());
                if (!TextUtils.isEmpty(regPlmn) && regPlmn.length() >= 5) {
                    boolean isRoaming = true ^ regPlmn.substring(0, 3).equals(mccNoService);
                    if (isRoaming) {
                        logd("primary card is no service, should switch to slot " + this.mSecondarySlot);
                    }
                    return isRoaming;
                }
            }
        }
        return false;
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg != null) {
            switch (msg.what) {
                case 1:
                    logd("Receive EVENT_PROMPT_TO_SWITCH");
                    handlePromptToSwitch();
                    return;
                case 2:
                    logd("Receive EVENT_AUTO_SWITCH_DONE");
                    handleAutoSwitchDone(msg);
                    return;
                case 3:
                    logd("Receive EVENT_AUTO_SWITCH_TIMEOUT");
                    cancelAutoSwitch();
                    return;
                default:
                    logd("default message " + msg.what);
                    return;
            }
        }
    }

    private void notifyDualCardsManager() {
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        intent.putExtra(EXTRA_DATA_SWITCH, this.mSecondarySlot);
        this.mContext.sendBroadcast(intent, "android.permission.MODIFY_PHONE_STATE");
        logd("send ACTION_SUBINFO_RECORD_UPDATED");
    }

    private void handlePromptToSwitch() {
        if (shouldSwitch()) {
            notifyDualCardsManager();
        } else {
            logd("no need send ACTION_SUBINFO_RECORD_UPDATED");
        }
    }

    private void handleAutoSwitchDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null) {
            loge("auto switch data slot failed.");
            if (!this.mShouldAutoSwitch) {
                notifyDualCardsManager();
            }
        } else {
            logd("auto switch data slot success.");
            this.mShouldAutoSwitch = false;
            if (hasMessages(3)) {
                removeMessages(3);
            }
            if (HwSubscriptionManager.getInstance() != null) {
                HwSubscriptionManager.getInstance().setUserPrefDataSlotId(msg.arg1);
            } else {
                loge("HwSubscriptionManager is null!!");
            }
        }
        this.mAutoSwitchingCount--;
    }

    private void cancelAutoSwitch() {
        if (this.mShouldAutoSwitch) {
            loge("cancel auto switch, try to prompt.");
            this.mShouldAutoSwitch = false;
            if (this.mAutoSwitchingCount == 0) {
                notifyDualCardsManager();
            }
        }
    }

    public void stop() {
        if (this.mShouldAutoSwitch) {
            this.mShouldAutoSwitch = false;
            logd("auto switch stopped.");
        }
        if (hasMessages(1)) {
            removeMessages(1);
        }
        if (hasMessages(3)) {
            removeMessages(3);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String message) {
        RlogEx.i(TAG, message);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String message) {
        RlogEx.e(TAG, message);
    }

    /* access modifiers changed from: private */
    public class ServiceStateListener extends PhoneStateListenerEx {
        int mPhoneId;

        ServiceStateListener(int phoneId, int subId) {
            super(subId);
            this.mPhoneId = phoneId;
            HwDualCardSwitcher.this.logd("ServiceStateListener create subId:" + subId + " phoneId:" + phoneId);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.internal.telephony.HwDualCardSwitcher$ServiceStateListener */
        /* JADX WARN: Multi-variable type inference failed */
        /* access modifiers changed from: package-private */
        public void listen(Context context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
            if (tm == null) {
                HwDualCardSwitcher.this.loge("listen, Cannot create TelephonyManager");
            } else {
                tm.listen(this, 1);
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.internal.telephony.HwDualCardSwitcher$ServiceStateListener */
        /* JADX WARN: Multi-variable type inference failed */
        /* access modifiers changed from: package-private */
        public void cancelListen(Context context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
            if (tm == null) {
                HwDualCardSwitcher.this.loge("cancel listen, Cannot create TelephonyManager");
            } else {
                tm.listen(this, 0);
            }
        }

        public void onServiceStateChanged(ServiceState state) {
            if (state != null) {
                int slotId = SubscriptionManagerEx.getSlotIndex(this.mSubscription);
                if (SubscriptionManagerEx.isValidSlotIndex(slotId)) {
                    HwDualCardSwitcher.this.mServiceState[slotId] = state;
                    HwDualCardSwitcher.this.checkIfNeedToSwitchSlot();
                }
            }
        }
    }
}
