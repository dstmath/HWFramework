package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.provider.SettingsEx;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneStateListenerExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import java.util.List;

public class HwSubscriptionManagerImpl extends DefaultHwSubscriptionManager {
    private static final boolean DBG = true;
    private static final int DEFAULT_SLOT_ID = 0;
    private static final int DEFAULT_SUB_ID = 0;
    private static final int EVENT_FAST_SWITCH_SIM_SLOT_RESULT = 12;
    private static final int EVENT_SET_SUBSCRIPTION_TIMEOUT = 11;
    private static final int EVENT_SET_UICC_SUBSCRIPTION_DONE = 10;
    private static final int INT_INVALID_VALUE = -1;
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwSubscriptionManagerImpl";
    private static final int SUB_0 = 0;
    private static final int SUB_1 = 1;
    private static final int SUB_INIT_STATE = 255;
    private static final int SUB_NUMS = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final int TIME_SET_SUBSCRIPTION_TIMEOUT = 90000;
    private static boolean isRetryed = false;
    private static HwSubscriptionManagerImpl sHwSubscriptionManager;
    private boolean isSetSubscriptionInProgress = false;
    private CommandsInterfaceEx[] mCommandsInterfaceExes;
    private Message mCompleteMsg;
    private Handler mHandler;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.android.internal.telephony.HwSubscriptionManagerImpl.AnonymousClass1 */

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            HwSubscriptionManagerImpl.logd("onSubscriptionsChanged");
            HwSubscriptionManagerImpl.this.registerListener();
        }
    };
    private CallStateListener mPhoneStateListener;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwSubscriptionManagerImpl.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                HwSubscriptionManagerImpl.logd("context or intent is null!");
                return;
            }
            boolean equals = "com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction());
            boolean targetSate = HwSubscriptionManagerImpl.DBG;
            if (equals) {
                int slotId = intent.getIntExtra("phone", -1);
                HwSubscriptionManagerImpl.this.isSetSubscriptionInProgress = false;
                HwSubscriptionManagerImpl.this.mHandler.removeMessages(11);
                int result = intent.getIntExtra("operationResult", 0);
                HwSubscriptionManagerImpl.logd("Received ACTION_SUBSCRIPTION_SET_UICC_RESULT on slotId: " + slotId + ", result = " + result);
                if (result == 1) {
                    HwSubscriptionManagerImpl.this.sendCompleteMsg(new RuntimeException("setSubScription fail!!!"));
                    if (!HwSubscriptionManagerImpl.isRetryed && slotId == 1 && HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub")) {
                        boolean targetState = intent.getIntExtra("newSubState", -1) != 0;
                        HwSubscriptionManagerImpl.logd("retry deactive sub2, targetState: " + targetState);
                        HwSubscriptionManagerImpl.this.setSubscription(1, targetState, null);
                        boolean unused = HwSubscriptionManagerImpl.isRetryed = HwSubscriptionManagerImpl.DBG;
                    }
                } else {
                    HwSubscriptionManagerImpl.this.sendCompleteMsg(null);
                }
                if (slotId == 1 && HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub")) {
                    Intent intentForMDM = new Intent("android.intent.ACTION_MDM_DISABLE_SUB_RESULT");
                    intent.putExtra("disableSubResult", result);
                    context.sendBroadcast(intentForMDM);
                }
            }
            if ("com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction())) {
                HwSubscriptionManagerImpl.logd("com.huawei.devicepolicy.action.POLICY_CHANGED");
                String action_tag = intent.getStringExtra("action_tag");
                if (!TextUtils.isEmpty(action_tag) && "action_disable_sub".equals(action_tag)) {
                    int slotId2 = intent.getIntExtra("subId", -1);
                    if (slotId2 == 0) {
                    }
                    if (intent.getBooleanExtra("subState", false)) {
                        targetSate = false;
                    }
                    HwSubscriptionManagerImpl.this.setSubscription(slotId2, targetSate, null);
                    boolean unused2 = HwSubscriptionManagerImpl.isRetryed = false;
                }
            }
        }
    };
    private Message mSavedCompleteMsg;
    private SubscriptionHelper mSubscriptionHelper;
    private TelephonyManager mTelephonyManager;
    private UiccControllerExt mUiccController;

    private HwSubscriptionManagerImpl() {
    }

    public static HwSubscriptionManagerImpl getInstance() {
        HwSubscriptionManagerImpl hwSubscriptionManagerImpl;
        synchronized (LOCK) {
            if (sHwSubscriptionManager == null) {
                sHwSubscriptionManager = new HwSubscriptionManagerImpl();
            }
            hwSubscriptionManagerImpl = sHwSubscriptionManager;
        }
        return hwSubscriptionManagerImpl;
    }

    private static boolean isValidSlotId(int slotId) {
        if (slotId < 0 || slotId >= SUB_NUMS) {
            return false;
        }
        return DBG;
    }

    /* access modifiers changed from: private */
    public static void logd(String message) {
        RlogEx.i(LOG_TAG, message);
    }

    private static void logw(String message) {
        RlogEx.w(LOG_TAG, message);
    }

    private static void loge(String message) {
        RlogEx.e(LOG_TAG, message);
    }

    public void init(Context context, CommandsInterfaceEx[] commandsInterfaceExes) {
        HwSubscriptionManagerImpl.super.init(context, commandsInterfaceExes);
        this.mCommandsInterfaceExes = (CommandsInterfaceEx[]) commandsInterfaceExes.clone();
        this.mHandler = new MyHandler();
        this.mUiccController = UiccControllerExt.getInstance();
        IntentFilter filter = new IntentFilter("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        filter.addAction("com.huawei.devicepolicy.action.POLICY_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, filter);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        ((SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service")).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        registerListener();
        logd("Constructor - Complete");
    }

    private boolean checkNeedSetDefault4GSlot(int slotId, int otherSlotId) {
        boolean isTLHybirdActiveCMCC = (HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 1 || HwFullNetworkManager.getInstance().isCMCCDsdxDisable()) && HwFullNetworkManager.getInstance().isCMCCCardBySlotId(slotId) && HwFullNetworkManager.getInstance().isCMCCHybird();
        boolean isCTHybirdActive = (HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 2 || HwFullNetworkConfig.IS_CT_4GSWITCH_DISABLE) && HwFullNetworkManager.getInstance().isCTCardBySlotId(slotId) && HwFullNetworkManager.getInstance().isCTHybird();
        if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() != otherSlotId) {
            return false;
        }
        if ((this.mSubscriptionController.getSubState(otherSlotId) == 0 || isTLHybirdActiveCMCC || isCTHybirdActive) && SystemPropertiesEx.getBoolean("persist.sys.dualcards", false)) {
            return DBG;
        }
        return false;
    }

    private boolean activeSubscription(int slotId) {
        int subId = getSubidFromSlotId(slotId);
        boolean isVSim = false;
        int otherSlotId = slotId == 0 ? 1 : 0;
        Message response = null;
        if (checkNeedSetDefault4GSlot(slotId, otherSlotId)) {
            if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload())) {
                isVSim = true;
            }
            if (HwFullNetworkConfig.IS_HISI_DSDX && !isVSim) {
                if (HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT) {
                    response = this.mHandler.obtainMessage(EVENT_FAST_SWITCH_SIM_SLOT_RESULT);
                    this.mSavedCompleteMsg = this.mCompleteMsg;
                    this.mCompleteMsg = null;
                    this.mSubscriptionController.activateSubId(subId);
                }
                this.mSubscriptionController.setSubState(slotId, 1);
                HwFullNetworkManager.getInstance().setMainSlot(slotId, response);
                return DBG;
            } else if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
                this.mSubscriptionController.activateSubId(subId);
                this.mSubscriptionController.setSubState(slotId, 1);
                HwFullNetworkManager.getInstance().setMainSlot(slotId, (Message) null);
                return DBG;
            } else {
                logd("setSubscription:just activateSubId " + subId);
            }
        }
        this.mSubscriptionController.activateSubId(subId);
        if (subId != 1 && HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(otherSlotId, "disable-data")) {
            HwFullNetworkManager.getInstance().setMainSlot(slotId, (Message) null);
        }
        this.mHandler.sendEmptyMessageDelayed(11, 90000);
        return DBG;
    }

    private boolean deactiveSubscription(int slotId) {
        int subId = getSubidFromSlotId(slotId);
        int otherSlotId = slotId == 0 ? 1 : 0;
        Message response = null;
        if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == slotId && this.mSubscriptionController.getSubState(otherSlotId) == 1 && SystemPropertiesEx.getBoolean("persist.sys.dualcards", false)) {
            boolean isVSim = HwTelephonyManager.getDefault().isPlatformSupportVsim() && (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload());
            if (HwFullNetworkConfig.IS_HISI_DSDX && !isVSim) {
                if (HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT) {
                    response = this.mHandler.obtainMessage(EVENT_FAST_SWITCH_SIM_SLOT_RESULT);
                    this.mSavedCompleteMsg = this.mCompleteMsg;
                    this.mCompleteMsg = null;
                    this.mSubscriptionController.deactivateSubId(subId);
                }
                this.mSubscriptionController.setSubState(slotId, 0);
                HwFullNetworkManager.getInstance().setMainSlot(otherSlotId, response);
                return DBG;
            } else if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
                this.mSubscriptionController.deactivateSubId(subId);
                this.mSubscriptionController.setSubState(slotId, 0);
                HwFullNetworkManager.getInstance().setMainSlot(otherSlotId, (Message) null);
                return DBG;
            } else {
                logd("setSubscription:just deactivateSubId " + subId);
            }
        }
        this.mSubscriptionController.deactivateSubId(subId);
        this.mHandler.sendEmptyMessageDelayed(11, 90000);
        return DBG;
    }

    public boolean setSubscription(int slotId, boolean isActivate, Message onCompleteMsg) {
        logd("setSubscription: slotId = " + slotId + ", activate = " + isActivate);
        if (!setSubscriptionCheck(slotId)) {
            return false;
        }
        HwFullNetworkManager.getInstance().resetUiccSubscriptionResultFlag(slotId);
        getSubidFromSlotId(slotId);
        this.mCompleteMsg = onCompleteMsg;
        if (slotId == 0) {
        }
        if (this.mSubscriptionController.getSubState(slotId) == isActivate) {
            logd("setSubscription: slotId = " + slotId + " is already " + (isActivate ? 1 : 0));
            sendCompleteMsg(null);
            return DBG;
        } else if (isActivate) {
            return activeSubscription(slotId);
        } else {
            return deactiveSubscription(slotId);
        }
    }

    private boolean setSubscriptionCheckNullInstance() {
        if (this.mSubscriptionController == null) {
            this.mSubscriptionController = SubscriptionControllerEx.getInstance();
            if (this.mSubscriptionController == null) {
                loge("setSubscriptionCheck: mSubscriptionController is null... return false");
                return false;
            }
        }
        if (this.mSubscriptionHelper == null) {
            this.mSubscriptionHelper = SubscriptionHelper.getInstance();
            if (this.mSubscriptionHelper == null) {
                loge("setSubscriptionCheck: mSubscriptionHelper is null... return false");
                return false;
            }
        }
        if (this.mUiccController != null) {
            return DBG;
        }
        this.mUiccController = UiccControllerExt.getInstance();
        if (this.mUiccController != null) {
            return DBG;
        }
        loge("setSubscriptionCheck: mUiccController is null... return false");
        return false;
    }

    private boolean setSubscriptionCheckCardState(int slotId) {
        if (this.isSetSubscriptionInProgress) {
            logd("setSubscriptionCheck: operation is in processing!! return false");
            return false;
        } else if (HwFullNetworkManager.getInstance().get4GSlotInProgress()) {
            logd("setSubscriptionCheck: setDefault4GSlot is in processing!! return false");
            return false;
        } else {
            UiccCardExt uiccCard = this.mUiccController.getUiccCard(slotId);
            IccCardStatusExt.CardStateEx cardState = IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT;
            if (uiccCard != null) {
                cardState = uiccCard.getCardState();
            }
            if (cardState != IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT) {
                logd("setSubscriptionCheck: Card is not present in slot " + slotId + ", return false");
                return false;
            } else if (TelephonyManagerEx.getCallState(TelephonyManagerEx.getDefault(), getSubidFromSlotId(slotId)) != 0) {
                logw("setSubscriptionCheck: Call State is not IDLE, can't set subscription!");
                return false;
            } else {
                int otherSlotId = slotId == 0 ? 1 : 0;
                if (slotId != HwTelephonyManagerInner.getDefault().getDefault4GSlotId() || TelephonyManagerEx.getCallState(TelephonyManagerEx.getDefault(), getSubidFromSlotId(otherSlotId)) == 0) {
                    return DBG;
                }
                logw("setSubscriptionCheck: Call State is not IDLE, can't set default sub subscription!");
                return false;
            }
        }
    }

    private boolean setSubscriptionCheck(int slotId) {
        if (!isValidSlotId(slotId)) {
            loge("setSubscriptionCheck: slotId is not correct : " + slotId);
            return false;
        } else if (!setSubscriptionCheckNullInstance() || !setSubscriptionCheckCardState(slotId)) {
            return false;
        } else {
            int otherSlotId = slotId == 0 ? 1 : 0;
            if (!HwFullNetworkManager.getInstance().isCMCCDsdxDisable() || !HwFullNetworkManager.getInstance().isCMCCCardBySlotId(slotId) || this.mSubscriptionController.getSubState(slotId) != 0 || HwFullNetworkManager.getInstance().isCMCCCardBySlotId(otherSlotId) || TelephonyManagerEx.getCallState(TelephonyManagerEx.getDefault(), getSubidFromSlotId(otherSlotId)) == 0) {
                boolean isMDMCMCCHybird = HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 1 && HwFullNetworkManager.getInstance().isCMCCCardBySlotId(slotId) && !HwFullNetworkManager.getInstance().isCMCCCardBySlotId(otherSlotId);
                boolean isMDMCTHybird = HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 2 && HwFullNetworkManager.getInstance().isCTCardBySlotId(slotId) && !HwFullNetworkManager.getInstance().isCTCardBySlotId(otherSlotId);
                boolean isCallIdleState = this.mSubscriptionController.getSubState(slotId) == 0 && TelephonyManagerEx.getCallState(TelephonyManagerEx.getDefault(), getSubidFromSlotId(otherSlotId)) != 0;
                if ((!isMDMCMCCHybird && !isMDMCTHybird) || !isCallIdleState) {
                    return DBG;
                }
                logw("setSubscriptionCheck: MDMCarrier: other card is not idle, MDMCarrier version can not active current card!");
                return false;
            }
            logw("setSubscriptionCheck: other card is not idle, TL version can not active CMCC card!");
            return false;
        }
    }

    private void setUserPreferencesForMultiSims(int defaultSubId, int defaultDataSubId, int defaultVoiceSubId, int defaultSmsSubId) {
        int userPrefDefaultSubId = getSubidFromSlotId(SettingsEx.Global.getInt(this.mContext.getContentResolver(), HwFullNetworkConstantsInner.USER_DEFAULT_SUBSCRIPTION, 0));
        if (!(userPrefDefaultSubId == defaultSubId && userPrefDefaultSubId == defaultVoiceSubId && userPrefDefaultSubId == defaultSmsSubId)) {
            TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
            PhoneAccount phoneAccount = telecomManager.getPhoneAccount(telecomManager.getUserSelectedOutgoingPhoneAccount());
            HwTelephonyManager.getDefault();
            int defVoiceSubIdFromTelecom = HwTelephonyManager.getSubIdForPhoneAccount(phoneAccount);
            logd("updateUserPreferences: set userPrefDefaultSubId from " + defaultSubId + " to " + userPrefDefaultSubId + ",defVoiceSubIdFromTelecom=" + defVoiceSubIdFromTelecom);
            this.mSubscriptionController.setDefaultFallbackSubId(userPrefDefaultSubId);
            this.mSubscriptionController.setDefaultSmsSubId(userPrefDefaultSubId);
            if (defVoiceSubIdFromTelecom != defaultVoiceSubId) {
                this.mSubscriptionController.setDefaultVoiceSubId(defVoiceSubIdFromTelecom);
            }
        }
        int userPrefDataSubId = getSubidFromSlotId(SettingsEx.Global.getInt(this.mContext.getContentResolver(), "user_datacall_sub", 0));
        if (userPrefDataSubId != defaultDataSubId && !HwFullNetworkConfig.IS_HISI_DSDX && !HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING && !HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK && !HwFullNetworkManager.getInstance().isSettingDefaultData()) {
            logd("updateUserPreferences: set userPrefDataSubId from " + defaultDataSubId + "to " + userPrefDataSubId);
            this.mSubscriptionController.setDefaultDataSubId(userPrefDataSubId);
        }
        if (HwFullNetworkConfig.IS_HISI_DSDX) {
            int default4Gslot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            if (getSubidFromSlotId(default4Gslot) != defaultDataSubId && !HwFullNetworkManager.getInstance().isSettingDefaultData()) {
                logd("updateUserPreferences: set dds to 4g slot from " + defaultDataSubId + " to " + default4Gslot);
                setDefaultDataSubIdBySlotId(default4Gslot);
            }
        }
        if (HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-data")) {
            HwFullNetworkManager.getInstance().setDefault4GSlotForMDM();
        }
    }

    public void updateUserPreferences(boolean isSetDds) {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionController.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        int mActCount = 0;
        SubscriptionInfo nextActivatedSub = null;
        int defaultSubId = SubscriptionManager.getDefaultSubscriptionId();
        int defaultDataSubId = this.mSubscriptionController.getDefaultDataSubId();
        int defaultVoiceSubId = SubscriptionManager.getDefaultVoiceSubscriptionId();
        int defaultSmsSubId = SubscriptionManager.getDefaultSmsSubscriptionId();
        logd("updateUserPreferences: defaultSubId = " + defaultSubId + ", defaultDataSubId = " + defaultDataSubId + ", defaultVoiceSubId = " + defaultVoiceSubId + ", defaultSmsSubId = " + defaultSmsSubId + ", isSetDds = " + isSetDds);
        if (subInfoList == null) {
            this.mSubscriptionController.resetDefaultFallbackSubId();
            logd("updateUserPreferences: subscription are not avaiable!!! Exit !");
            return;
        }
        for (SubscriptionInfo subInfo : subInfoList) {
            if (this.mSubscriptionController.getSubState(subInfo.getSimSlotIndex()) == 1) {
                mActCount++;
                if (nextActivatedSub == null) {
                    nextActivatedSub = subInfo;
                }
            }
        }
        if (mActCount < 2) {
            this.mSubscriptionController.setSMSPromptEnabled(false);
            this.mSubscriptionController.setVoicePromptEnabled(false);
        }
        logd("updateUserPreferences: mActCount = " + mActCount);
        if (nextActivatedSub != null) {
            if (mActCount == 1) {
                setUserPreferences(nextActivatedSub.getSubscriptionId(), isSetDds, defaultDataSubId, defaultVoiceSubId, defaultSmsSubId);
            } else if (mActCount == subInfoList.size()) {
                setUserPreferencesForMultiSims(defaultSubId, defaultDataSubId, defaultVoiceSubId, defaultSmsSubId);
            }
            logd("updateUserPreferences: after current DataSub = " + this.mSubscriptionController.getDefaultDataSubId() + " VoiceSub = " + SubscriptionManager.getDefaultVoiceSubscriptionId() + " SmsSub = " + SubscriptionManager.getDefaultSmsSubscriptionId());
        }
    }

    public int getUserPrefDataSubId() {
        int[] userPrefDataSubIds = SubscriptionManagerEx.getSubId(SettingsEx.Global.getInt(this.mContext.getContentResolver(), "user_datacall_sub", 0));
        if (userPrefDataSubIds == null) {
            return -1;
        }
        logd("getUserPrefDataSubId: userPrefDataSubId = " + userPrefDataSubIds[0]);
        return userPrefDataSubIds[0];
    }

    public void updateDataSlot() {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionController.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        int mActCount = 0;
        if (subInfoList != null) {
            for (SubscriptionInfo subInfo : subInfoList) {
                if (this.mSubscriptionController.getSubState(subInfo.getSimSlotIndex()) == 1) {
                    mActCount++;
                }
            }
            logd("updateDataSlot: mActCount =" + mActCount);
            if (mActCount > 1) {
                int defaultSubId = this.mSubscriptionController.getDefaultDataSubId();
                int userPrefDataSlotId = getUserPrefDataSubId();
                logd("updateDataSlot: set UserPrefDefaultSubId from " + defaultSubId + "to " + userPrefDataSlotId);
                this.mSubscriptionController.setDefaultDataSubId(userPrefDataSlotId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCompleteMsg(Exception e) {
        logd("sendCompleteMsg to target, Exception");
        Message message = this.mCompleteMsg;
        if (message != null) {
            AsyncResultEx.forMessage(message).setException(e);
            this.mCompleteMsg.sendToTarget();
            this.mCompleteMsg = null;
        }
    }

    public void setUserPrefDefaultSlotId(int slotId) {
        logd("setUserPrefDefaultSubId: slotId = " + slotId);
        if (slotId < 0 || slotId >= SUB_NUMS) {
            loge("setUserPrefDefaultSubId: invalid slotId!!!");
            return;
        }
        SettingsEx.Global.putInt(this.mContext.getContentResolver(), HwFullNetworkConstantsInner.USER_DEFAULT_SUBSCRIPTION, slotId);
        int subId = getSubidFromSlotId(slotId);
        this.mSubscriptionController.setDefaultFallbackSubId(subId);
        this.mSubscriptionController.setDefaultSmsSubId(subId);
        this.mSubscriptionController.setDefaultVoiceSubId(subId);
    }

    public void setUserPrefDataSlotId(int slotId) {
        logd("setUserPrefDataSubId: slotId = " + slotId);
        if (slotId < 0 || slotId >= SUB_NUMS) {
            loge("setUserPrefDefaultSubId: invalid slotId!!!");
            return;
        }
        SettingsEx.Global.putInt(this.mContext.getContentResolver(), "user_datacall_sub", slotId);
        int subId = getSubidFromSlotId(slotId);
        if (subId != -1) {
            this.mSubscriptionController.setDefaultDataSubId(subId);
        } else {
            logd("setUserPrefDataSubId: sub id invalid.");
        }
    }

    private int getSubidFromSlotId(int slotId) {
        int[] subIds = SubscriptionManagerEx.getSubId(slotId);
        if (subIds == null || subIds.length == 0) {
            return -1;
        }
        return subIds[0];
    }

    public void setDefaultDataSubIdToDbBySlotId(int slotId) {
        int subId = SubscriptionManagerEx.getSubIdUsingSlotId(slotId);
        logd("setDefaultDataSubIdToDbBySlotId, slotId: " + slotId + ", subId: " + subId);
        SubscriptionControllerEx.getInstance().setDataSubId(subId);
    }

    public void setDefaultDataSubIdBySlotId(int slotId) {
        int subId = SubscriptionManagerEx.getSubIdUsingSlotId(slotId);
        logd("setDefaultDataSubIdBySlotId, slotId: " + slotId + ", subId: " + subId);
        SubscriptionControllerEx.getInstance().setDefaultDataSubId(subId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerListener() {
        int newSubId = SubscriptionManagerEx.getSubIdUsingSlotId(1);
        if (!SubscriptionManager.isValidSubscriptionId(newSubId)) {
            logd("sub id is invalid, cancel listen.");
            CallStateListener callStateListener = this.mPhoneStateListener;
            if (callStateListener != null) {
                callStateListener.cancelListen();
                this.mPhoneStateListener = null;
                return;
            }
            return;
        }
        CallStateListener callStateListener2 = this.mPhoneStateListener;
        if (callStateListener2 == null || callStateListener2.mSubId != newSubId) {
            logd("sub id is different, cancel first and create a new one." + newSubId);
            CallStateListener callStateListener3 = this.mPhoneStateListener;
            if (callStateListener3 != null) {
                callStateListener3.cancelListen();
            }
            this.mPhoneStateListener = new CallStateListener(newSubId);
            this.mPhoneStateListener.listen();
            return;
        }
        logd("sub id is not change, do nothing." + this.mPhoneStateListener.mSubId);
    }

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 11) {
                HwSubscriptionManagerImpl.logd("EVENT_SET_SUBSCRIPTION_TIMEOUT");
                HwSubscriptionManagerImpl.this.isSetSubscriptionInProgress = false;
                HwSubscriptionManagerImpl.this.sendCompleteMsg(new RuntimeException("setSubScription timeout!!!"));
            } else if (i == HwSubscriptionManagerImpl.EVENT_FAST_SWITCH_SIM_SLOT_RESULT) {
                HwSubscriptionManagerImpl.logd("EVENT_FAST_SWITCH_SIM_SLOT_RESULT");
                HwSubscriptionManagerImpl.logd("send mSavedCompleteMsg to target");
                AsyncResultEx ar = AsyncResultEx.from(msg.obj);
                if (ar != null && HwSubscriptionManagerImpl.this.mSavedCompleteMsg != null) {
                    AsyncResultEx.forMessage(HwSubscriptionManagerImpl.this.mSavedCompleteMsg).setException(ar.getException());
                    HwSubscriptionManagerImpl.this.mSavedCompleteMsg.sendToTarget();
                    HwSubscriptionManagerImpl.this.mSavedCompleteMsg = null;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class CallStateListener extends PhoneStateListenerExt {
        int mSubId;

        CallStateListener(int subId) {
            super(subId);
            this.mSubId = subId;
            HwSubscriptionManagerImpl.logd("CallStateListener create subId:" + subId);
        }

        /* access modifiers changed from: package-private */
        public void listen() {
            HwSubscriptionManagerImpl.this.mTelephonyManager.listen(getPhoneStateListener(), 32);
        }

        /* access modifiers changed from: package-private */
        public void cancelListen() {
            HwSubscriptionManagerImpl.this.mTelephonyManager.listen(getPhoneStateListener(), 0);
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            HwSubscriptionManagerImpl.logd("onCallStateChanged state : " + state);
            if (state == 0 && HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub")) {
                HwSubscriptionManagerImpl.this.setSubscription(1, false, null);
            }
        }
    }
}
