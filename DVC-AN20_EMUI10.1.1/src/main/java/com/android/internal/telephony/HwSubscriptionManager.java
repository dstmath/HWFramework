package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import java.util.List;

public class HwSubscriptionManager extends Handler {
    private static final boolean DBG = true;
    public static final int DEFAULT_SLOT_ID = 0;
    public static final int DEFAULT_SUB_ID = 0;
    private static final boolean ERROR = true;
    private static final int EVENT_FAST_SWITCH_SIM_SLOT_RESULT = 12;
    private static final int EVENT_SET_SUBSCRIPTION_TIMEOUT = 11;
    private static final int EVENT_SET_UICC_SUBSCRIPTION_DONE = 10;
    private static final String LOG_TAG = "HwSubMgr";
    private static final int SUB_0 = 0;
    private static final int SUB_1 = 1;
    public static final int SUB_INIT_STATE = 255;
    private static final int SUB_NUMS = TelephonyManager.getDefault().getPhoneCount();
    private static final int TIME_SET_SUBSCRIPTION_TIMEOUT = 90000;
    private static final String USER_DATACALL_SUBSCRIPTION = "user_datacall_sub";
    private static boolean hasRetry = false;
    private static HwSubscriptionManager mHwSubscriptionManager;
    private static UiccController mUiccController;
    private boolean mCardChange;
    private CommandsInterface[] mCi;
    private Message mCompleteMsg;
    private Context mContext;
    private Handler mHandler;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.android.internal.telephony.HwSubscriptionManager.AnonymousClass2 */

        public void onSubscriptionsChanged() {
            HwSubscriptionManager.logd("onSubscriptionsChanged");
            HwSubscriptionManager.this.registerListener();
        }
    };
    private CallStateListener mPhoneStateListener;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwSubscriptionManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            boolean targetSate = true;
            if ("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction())) {
                int slotId = intent.getIntExtra("phone", -1);
                HwSubscriptionManager.this.mSetSubscriptionInProgress = false;
                HwSubscriptionManager.this.mHandler.removeMessages(HwSubscriptionManager.EVENT_SET_SUBSCRIPTION_TIMEOUT);
                int result = intent.getIntExtra("operationResult", 0);
                HwSubscriptionManager.logd("Received ACTION_SUBSCRIPTION_SET_UICC_RESULT on slotId: " + slotId + ", result = " + result);
                if (result == 1) {
                    HwSubscriptionManager.this.sendCompleteMsg(new RuntimeException("setSubScription fail!!!"));
                    if (!HwSubscriptionManager.hasRetry && slotId == 1 && HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub")) {
                        boolean targetState = ((Integer) intent.getExtra("newSubState", -1)).intValue() != 0;
                        HwSubscriptionManager.logd("retry deactive sub2, targetState: " + targetState);
                        HwSubscriptionManager.this.setSubscription(1, targetState, null);
                        boolean unused = HwSubscriptionManager.hasRetry = true;
                    }
                } else {
                    HwSubscriptionManager.this.sendCompleteMsg(null);
                }
                if (slotId == 1 && HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub")) {
                    Intent intentForMDM = new Intent("android.intent.ACTION_MDM_DISABLE_SUB_RESULT");
                    intent.putExtra("disableSubResult", result);
                    context.sendBroadcast(intentForMDM);
                }
            } else if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(intent.getAction())) {
                int slotId2 = intent.getIntExtra("slot", -1);
                int intValue = intent.getIntExtra("intContent", 0);
                String column = intent.getStringExtra("columnName");
                HwSubscriptionManager.logd("Received ACTION_SUBINFO_CONTENT_CHANGE on slotId: " + slotId2 + " for " + column + ", intValue: " + intValue);
                if ("sub_state".equals(column) && -1 != slotId2) {
                    if (intValue == 1) {
                        HwSubscriptionManager.this.notifySlotSubscriptionActivated(slotId2);
                    } else if (intValue == 0) {
                        HwSubscriptionManager.this.notifySlotSubscriptionDeactivated(slotId2);
                    }
                }
            }
            if ("com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction())) {
                HwSubscriptionManager.logd("com.huawei.devicepolicy.action.POLICY_CHANGED");
                String action_tag = intent.getStringExtra("action_tag");
                if (!TextUtils.isEmpty(action_tag) && action_tag.equals("action_disable_sub")) {
                    int slotId3 = intent.getIntExtra("subId", -1);
                    if (slotId3 == 0) {
                    }
                    if (intent.getBooleanExtra("subState", false)) {
                        targetSate = false;
                    }
                    HwSubscriptionManager.this.setSubscription(slotId3, targetSate, null);
                    boolean unused2 = HwSubscriptionManager.hasRetry = false;
                }
            }
        }
    };
    private Message mSavedCompleteMsg;
    private boolean mSetSubscriptionInProgress = false;
    private RegistrantList[] mSubActivatedRegistrantsOnSlot;
    private RegistrantList[] mSubDeactivatedRegistrantsOnSlot;
    private SubscriptionControllerEx mSubscriptionController;
    private SubscriptionHelper mSubscriptionHelper;
    private TelephonyManager mTelephonyManager;

    public HwSubscriptionManager(Context context, CommandsInterface[] ci) {
        this.mContext = context;
        this.mCi = ci;
        this.mHandler = this;
        this.mSubscriptionController = SubscriptionControllerEx.getInstance();
        mUiccController = UiccController.getInstance();
        IntentFilter filter = new IntentFilter("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        filter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        filter.addAction("com.huawei.devicepolicy.action.POLICY_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, filter);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        ((SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service")).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        registerListener();
        int i = SUB_NUMS;
        this.mSubDeactivatedRegistrantsOnSlot = new RegistrantList[i];
        this.mSubActivatedRegistrantsOnSlot = new RegistrantList[i];
        for (int i2 = 0; i2 < SUB_NUMS; i2++) {
            this.mSubActivatedRegistrantsOnSlot[i2] = new RegistrantList();
            this.mSubDeactivatedRegistrantsOnSlot[i2] = new RegistrantList();
        }
        logd("Constructor - Complete");
    }

    public static HwSubscriptionManager init(Context c, CommandsInterface[] ci) {
        HwSubscriptionManager hwSubscriptionManager;
        synchronized (HwSubscriptionManager.class) {
            if (mHwSubscriptionManager == null) {
                mHwSubscriptionManager = new HwSubscriptionManager(c, ci);
            } else {
                logw("init() called multiple times!  mHwSubscriptionManager = " + mHwSubscriptionManager);
            }
            hwSubscriptionManager = mHwSubscriptionManager;
        }
        return hwSubscriptionManager;
    }

    public static HwSubscriptionManager getInstance() {
        if (mHwSubscriptionManager == null) {
            logw("getInstance null");
        }
        return mHwSubscriptionManager;
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == EVENT_SET_SUBSCRIPTION_TIMEOUT) {
            logd("EVENT_SET_SUBSCRIPTION_TIMEOUT");
            this.mSetSubscriptionInProgress = false;
            sendCompleteMsg(new RuntimeException("setSubScription timeout!!!"));
        } else if (i == 12) {
            logd("EVENT_FAST_SWITCH_SIM_SLOT_RESULT");
            logd("send mSavedCompleteMsg to target");
            if (this.mSavedCompleteMsg != null) {
                AsyncResult.forMessage(this.mSavedCompleteMsg).exception = ((AsyncResult) msg.obj).exception;
                this.mSavedCompleteMsg.sendToTarget();
                this.mSavedCompleteMsg = null;
            }
        }
    }

    public boolean setSubscription(int slotId, boolean activate, Message onCompleteMsg) {
        logd("setSubscription: slotId = " + slotId + ", activate = " + activate);
        boolean isNotVSim = false;
        if (!setSubscriptionCheck(slotId)) {
            return false;
        }
        HwFullNetworkManager.getInstance().resetUiccSubscriptionResultFlag(slotId);
        int subId = getSubIdFromSlotId(slotId);
        this.mCompleteMsg = onCompleteMsg;
        int otherSlotId = slotId == 0 ? 1 : 0;
        Message response = null;
        if (activate) {
            if (this.mSubscriptionController.getSubState(slotId) == 1) {
                logd("setSubscription: slotId = " + slotId + " is already ACTIVED.");
                sendCompleteMsg(null);
            } else {
                if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == otherSlotId && (this.mSubscriptionController.getSubState(otherSlotId) == 0 || ((HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 1 || HwFullNetworkManager.getInstance().isCMCCDsdxDisable()) && HwFullNetworkManager.getInstance().isCMCCCardBySlotId(slotId) && HwFullNetworkManager.getInstance().isCMCCHybird()) || ((HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 2 || HwFullNetworkConfig.IS_CT_4GSWITCH_DISABLE) && HwFullNetworkManager.getInstance().isCTCardBySlotId(slotId) && HwFullNetworkManager.getInstance().isCTHybird())) && SystemProperties.getBoolean("persist.sys.dualcards", false)) {
                    if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || (!HwVSimUtils.isVSimEnabled() && !HwVSimUtils.isVSimCauseCardReload())) {
                        isNotVSim = true;
                    }
                    if (HwFullNetworkConfig.IS_HISI_DSDX && isNotVSim) {
                        if (HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT) {
                            response = obtainMessage(12);
                            this.mSavedCompleteMsg = this.mCompleteMsg;
                            this.mCompleteMsg = null;
                            this.mSubscriptionController.activateSubId(subId);
                        }
                        this.mSubscriptionController.setSubState(slotId, 1);
                        HwFullNetworkManager.getInstance().setMainSlot(slotId, response);
                        return true;
                    } else if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
                        this.mSubscriptionController.activateSubId(subId);
                        this.mSubscriptionController.setSubState(slotId, 1);
                        HwFullNetworkManager.getInstance().setMainSlot(slotId, (Message) null);
                        return true;
                    } else {
                        logd("setSubscription:just activateSubId " + subId);
                    }
                }
                this.mSubscriptionController.activateSubId(subId);
                if (subId != 1 && HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(otherSlotId, "disable-data")) {
                    HwFullNetworkManager.getInstance().setMainSlot(slotId, (Message) null);
                }
                this.mHandler.sendEmptyMessageDelayed(EVENT_SET_SUBSCRIPTION_TIMEOUT, 90000);
            }
        } else if (this.mSubscriptionController.getSubState(slotId) == 0) {
            logd("setSubscription: slotId = " + slotId + " is already INACTIVED.");
            sendCompleteMsg(null);
        } else {
            if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == slotId && this.mSubscriptionController.getSubState(otherSlotId) == 1 && SystemProperties.getBoolean("persist.sys.dualcards", false)) {
                boolean isNotVSim2 = !HwTelephonyManager.getDefault().isPlatformSupportVsim() || (!HwVSimUtils.isVSimEnabled() && !HwVSimUtils.isVSimCauseCardReload());
                if (HwFullNetworkConfig.IS_HISI_DSDX && isNotVSim2) {
                    if (HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT) {
                        response = obtainMessage(12);
                        this.mSavedCompleteMsg = this.mCompleteMsg;
                        this.mCompleteMsg = null;
                        this.mSubscriptionController.deactivateSubId(subId);
                    }
                    this.mSubscriptionController.setSubState(slotId, 0);
                    HwFullNetworkManager.getInstance().setMainSlot(otherSlotId, response);
                    return true;
                } else if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
                    this.mSubscriptionController.deactivateSubId(subId);
                    this.mSubscriptionController.setSubState(slotId, 0);
                    HwFullNetworkManager.getInstance().setMainSlot(otherSlotId, (Message) null);
                    return true;
                } else {
                    logd("setSubscription:just deactivateSubId " + subId);
                }
            }
            this.mSubscriptionController.deactivateSubId(subId);
            this.mHandler.sendEmptyMessageDelayed(EVENT_SET_SUBSCRIPTION_TIMEOUT, 90000);
        }
        return true;
    }

    private static boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId < SUB_NUMS;
    }

    private boolean setSubscriptionCheck(int slotId) {
        if (!isValidSlotId(slotId)) {
            loge("setSubscriptionCheck: slotId is not correct : " + slotId);
            return false;
        }
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
        if (mUiccController == null) {
            mUiccController = UiccController.getInstance();
            if (mUiccController == null) {
                loge("setSubscriptionCheck: mUiccController is null... return false");
                return false;
            }
        }
        if (this.mSetSubscriptionInProgress) {
            logd("setSubscriptionCheck: operation is in processing!! return false");
            return false;
        } else if (HwFullNetworkManager.getInstance().get4GSlotInProgress()) {
            logd("setSubscriptionCheck: setDefault4GSlot is in processing!! return false");
            return false;
        } else {
            UiccCard uiccCard = mUiccController.getUiccCard(slotId);
            IccCardStatus.CardState cardState = IccCardStatus.CardState.CARDSTATE_ABSENT;
            if (uiccCard != null) {
                cardState = uiccCard.getCardState();
            }
            if (cardState != IccCardStatus.CardState.CARDSTATE_PRESENT) {
                logd("setSubscriptionCheck: Card is not present in slot " + slotId + ", return false");
                return false;
            } else if (TelephonyManager.getDefault().getCallState(getSubIdFromSlotId(slotId)) != 0) {
                logw("setSubscriptionCheck: Call State is not IDLE, can't set subscription!");
                return false;
            } else {
                int otherSlotId = slotId == 0 ? 1 : 0;
                if (slotId == HwTelephonyManagerInner.getDefault().getDefault4GSlotId() && TelephonyManager.getDefault().getCallState(getSubIdFromSlotId(otherSlotId)) != 0) {
                    logw("setSubscriptionCheck: Call State is not IDLE, can't set default sub subscription!");
                    return false;
                } else if (!HwFullNetworkManager.getInstance().isCMCCDsdxDisable() || !HwFullNetworkManager.getInstance().isCMCCCardBySlotId(slotId) || this.mSubscriptionController.getSubState(slotId) != 0 || HwFullNetworkManager.getInstance().isCMCCCardBySlotId(otherSlotId) || TelephonyManager.getDefault().getCallState(getSubIdFromSlotId(otherSlotId)) == 0) {
                    boolean isMDMCMCCHybird = HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 1 && HwFullNetworkManager.getInstance().isCMCCCardBySlotId(slotId) && !HwFullNetworkManager.getInstance().isCMCCCardBySlotId(otherSlotId);
                    boolean isMDMCTHybird = HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 2 && HwFullNetworkManager.getInstance().isCTCardBySlotId(slotId) && !HwFullNetworkManager.getInstance().isCTCardBySlotId(otherSlotId);
                    boolean isCallIdleState = this.mSubscriptionController.getSubState(slotId) == 0 && TelephonyManager.getDefault().getCallState(getSubIdFromSlotId(otherSlotId)) != 0;
                    if ((!isMDMCMCCHybird && !isMDMCTHybird) || !isCallIdleState) {
                        return true;
                    }
                    logw("setSubscriptionCheck: MDMCarrier: other card is not idle, MDMCarrier version can not active current card!");
                    return false;
                } else {
                    logw("setSubscriptionCheck: other card is not idle, TL version can not active CMCC card!");
                    return false;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateUserPreferences(boolean setDds) {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionController.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        int mActCount = 0;
        SubscriptionInfo mNextActivatedSub = null;
        int defaultSubId = SubscriptionManager.getDefaultSubscriptionId();
        int defaultDataSubId = this.mSubscriptionController.getDefaultDataSubId();
        int defaultVoiceSubId = SubscriptionManager.getDefaultVoiceSubscriptionId();
        int defaultSmsSubId = SubscriptionManager.getDefaultSmsSubscriptionId();
        logd("updateUserPreferences: defaultSubId = " + defaultSubId + ", defaultDataSubId = " + defaultDataSubId + ", defaultVoiceSubId = " + defaultVoiceSubId + ", defaultSmsSubId = " + defaultSmsSubId + ", setDDs = " + setDds);
        if (subInfoList == null) {
            this.mSubscriptionController.resetDefaultFallbackSubId();
            logd("updateUserPreferences: subscription are not avaiable!!! Exit !");
            return;
        }
        for (SubscriptionInfo subInfo : subInfoList) {
            if (this.mSubscriptionController.getSubState(subInfo.getSimSlotIndex()) == 1) {
                mActCount++;
                if (mNextActivatedSub == null) {
                    mNextActivatedSub = subInfo;
                }
            }
        }
        if (mActCount < 2) {
            this.mSubscriptionController.setSMSPromptEnabled(false);
            this.mSubscriptionController.setVoicePromptEnabled(false);
        }
        logd("updateUserPreferences: mActCount = " + mActCount);
        if (mNextActivatedSub != null) {
            if (mActCount == 1) {
                int defaultSlotId = SubscriptionManagerEx.getSlotIndex(defaultSubId);
                if (this.mSubscriptionController.getSubState(defaultSlotId) == 0 || TelephonyManager.getDefault().getSimState(defaultSlotId) == 1) {
                    this.mSubscriptionController.setDefaultFallbackSubId(mNextActivatedSub.getSubscriptionId());
                }
                int defaultDataSlotId = SubscriptionManagerEx.getSlotIndex(defaultDataSubId);
                int dataSubState = this.mSubscriptionController.getSubState(defaultDataSlotId);
                int dataSimState = TelephonyManager.getDefault().getSimState(defaultDataSlotId);
                if (!HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK && !HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING && (setDds || dataSubState == 0 || dataSimState == 1)) {
                    if (dataSubState == 0 || dataSimState == 1) {
                        if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || !HwVSimUtils.isVSimEnabled()) {
                            defaultDataSubId = mNextActivatedSub.getSubscriptionId();
                        } else {
                            logd("updateUserPreferences: vsim is enabled, block set dds, dataSubState = " + dataSubState);
                        }
                    }
                    this.mSubscriptionController.setDefaultDataSubId(defaultDataSubId);
                }
                int defaultVoiceSlotId = SubscriptionManagerEx.getSlotIndex(defaultVoiceSubId);
                if ((this.mSubscriptionController.getSubState(defaultVoiceSlotId) == 0 || TelephonyManager.getDefault().getSimState(defaultVoiceSlotId) == 1) && !this.mSubscriptionController.isVoicePromptEnabled()) {
                    this.mSubscriptionController.setDefaultVoiceSubId(mNextActivatedSub.getSubscriptionId());
                }
                int defaultSmsSlotId = SubscriptionManagerEx.getSlotIndex(defaultSmsSubId);
                if ((this.mSubscriptionController.getSubState(defaultSmsSlotId) == 0 || TelephonyManager.getDefault().getSimState(defaultSmsSlotId) == 1) && !this.mSubscriptionController.isSMSPromptEnabled()) {
                    this.mSubscriptionController.setDefaultSmsSubId(mNextActivatedSub.getSubscriptionId());
                }
            } else if (mActCount == subInfoList.size()) {
                int userPrefDefaultSubId = getSubIdFromSlotId(Settings.Global.getInt(this.mContext.getContentResolver(), "user_default_sub", 0));
                if (!(userPrefDefaultSubId == defaultSubId && userPrefDefaultSubId == defaultVoiceSubId && userPrefDefaultSubId == defaultSmsSubId)) {
                    logd("updateUserPreferences: set userPrefDefaultSubId from " + defaultSubId + "to " + userPrefDefaultSubId);
                    this.mSubscriptionController.setDefaultFallbackSubId(userPrefDefaultSubId);
                    this.mSubscriptionController.setDefaultSmsSubId(userPrefDefaultSubId);
                    this.mSubscriptionController.setDefaultVoiceSubId(userPrefDefaultSubId);
                }
                int userPrefDataSubId = getSubIdFromSlotId(Settings.Global.getInt(this.mContext.getContentResolver(), USER_DATACALL_SUBSCRIPTION, 0));
                if (userPrefDataSubId != defaultDataSubId && !HwFullNetworkConfig.IS_HISI_DSDX && !HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING && !HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK && !HwFullNetworkManager.getInstance().isSettingDefaultData()) {
                    logd("updateUserPreferences: set userPrefDataSubId from " + defaultDataSubId + "to " + userPrefDataSubId);
                    this.mSubscriptionController.setDefaultDataSubId(userPrefDataSubId);
                }
                if (HwFullNetworkConfig.IS_HISI_DSDX) {
                    int default4Gslot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                    if (getSubIdFromSlotId(default4Gslot) != defaultDataSubId && !HwFullNetworkManager.getInstance().isSettingDefaultData()) {
                        logd("updateUserPreferences: set dds to 4g slot from " + defaultDataSubId + " to " + default4Gslot);
                        getInstance().setDefaultDataSubIdBySlotId(default4Gslot);
                    }
                }
                if (HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-data")) {
                    HwFullNetworkManager.getInstance().setDefault4GSlotForMDM();
                }
            }
            logd("updateUserPreferences: after current DataSub = " + this.mSubscriptionController.getDefaultDataSubId() + " VoiceSub = " + SubscriptionManager.getDefaultVoiceSubscriptionId() + " SmsSub = " + SubscriptionManager.getDefaultSmsSubscriptionId());
        }
    }

    public int getUserPrefDataSubId() {
        int[] userPrefDataSubId = SubscriptionManagerEx.getSubId(Settings.Global.getInt(this.mContext.getContentResolver(), USER_DATACALL_SUBSCRIPTION, 0));
        logd("getUserPrefDataSubId: userPrefDataSubId = " + userPrefDataSubId[0]);
        return userPrefDataSubId[0];
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
                int UserPrefDataSlotId = getUserPrefDataSubId();
                logd("updateDataSlot: set UserPrefDefaultSubId from " + defaultSubId + "to " + UserPrefDataSlotId);
                this.mSubscriptionController.setDefaultDataSubId(UserPrefDataSlotId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCompleteMsg(Exception e) {
        logd("sendCompleteMsg to target, Exception = " + e);
        Message message = this.mCompleteMsg;
        if (message != null) {
            AsyncResult.forMessage(message).exception = e;
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
        Settings.Global.putInt(this.mContext.getContentResolver(), "user_default_sub", slotId);
        int subId = getSubIdFromSlotId(slotId);
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
        Settings.Global.putInt(this.mContext.getContentResolver(), USER_DATACALL_SUBSCRIPTION, slotId);
        int subId = getSubIdFromSlotId(slotId);
        if (subId != -1) {
            this.mSubscriptionController.setDefaultDataSubId(subId);
        } else {
            logd("setUserPrefDataSubId: sub id invalid.");
        }
    }

    /* access modifiers changed from: protected */
    public int getDefaultDataSlotId() {
        int slotId = SubscriptionManagerEx.getSlotIndex(this.mSubscriptionController.getDefaultDataSubId());
        logd("getDefaultDataSlotId: slotId = " + slotId);
        return slotId;
    }

    /* access modifiers changed from: protected */
    public int getSubIdFromSlotId(int slotId) {
        int[] subId = SubscriptionManagerEx.getSubId(slotId);
        if (subId == null || subId.length == 0) {
            return -1;
        }
        return subId[0];
    }

    public void registerForSubscriptionActivatedOnSlot(int slotId, Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mSubActivatedRegistrantsOnSlot[slotId]) {
            this.mSubActivatedRegistrantsOnSlot[slotId].add(r);
        }
    }

    public void unregisterForSubscriptionActivatedOnSlot(int slotId, Handler h) {
        synchronized (this.mSubActivatedRegistrantsOnSlot[slotId]) {
            this.mSubActivatedRegistrantsOnSlot[slotId].remove(h);
        }
    }

    public void registerForSubscriptionDeactivatedOnSlot(int slotId, Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        synchronized (this.mSubDeactivatedRegistrantsOnSlot[slotId]) {
            this.mSubDeactivatedRegistrantsOnSlot[slotId].add(r);
        }
    }

    public void unregisterForSubscriptionDeactivatedOnSlot(int slotId, Handler h) {
        synchronized (this.mSubDeactivatedRegistrantsOnSlot[slotId]) {
            this.mSubDeactivatedRegistrantsOnSlot[slotId].remove(h);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifySlotSubscriptionActivated(int slotId) {
        logd("notifySlotSubscriptionActivated: slotId = " + slotId);
        this.mSubActivatedRegistrantsOnSlot[slotId].notifyRegistrants();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifySlotSubscriptionDeactivated(int slotId) {
        logd("notifySlotSubscriptionDeactivated: slotId = " + slotId);
        this.mSubDeactivatedRegistrantsOnSlot[slotId].notifyRegistrants();
    }

    /* access modifiers changed from: private */
    public static void logd(String message) {
        RlogEx.i(LOG_TAG, "[HwSubscriptionManager]" + message);
    }

    private static void logw(String message) {
        RlogEx.w(LOG_TAG, "[HwSubscriptionManager]" + message);
    }

    private static void loge(String message) {
        RlogEx.e(LOG_TAG, "[HwSubscriptionManager]" + message);
    }

    public void setDefaultDataSubIdToDbBySlotId(int slotId) {
        int subId = SubscriptionController.getInstance().getSubIdUsingPhoneId(slotId);
        logd("setDefaultDataSubIdToDbBySlotId, slotId: " + slotId + ", subId: " + subId);
        SubscriptionController.getInstance().setDataSubId(subId);
    }

    public void setDefaultDataSubIdBySlotId(int slotId) {
        int subId = SubscriptionController.getInstance().getSubIdUsingPhoneId(slotId);
        logd("setDefaultDataSubIdBySlotId, slotId: " + slotId + ", subId: " + subId);
        SubscriptionController.getInstance().setDefaultDataSubId(subId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerListener() {
        int newSubId = SubscriptionController.getInstance().getSubIdUsingPhoneId(1);
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

    /* access modifiers changed from: private */
    public class CallStateListener extends PhoneStateListener {
        int mSubId;

        CallStateListener(int subId) {
            super(Integer.valueOf(subId));
            this.mSubId = subId;
            HwSubscriptionManager.logd("CallStateListener create subId:" + subId);
        }

        /* access modifiers changed from: package-private */
        public void listen() {
            HwSubscriptionManager.this.mTelephonyManager.listen(this, 32);
        }

        /* access modifiers changed from: package-private */
        public void cancelListen() {
            HwSubscriptionManager.this.mTelephonyManager.listen(this, 0);
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            HwSubscriptionManager.logd("onCallStateChanged   state : " + state);
            if (state == 0 && HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(1, "disable-sub")) {
                HwSubscriptionManager.this.setSubscription(1, false, null);
            }
        }
    }
}
