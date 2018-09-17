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
import android.provider.Settings.Global;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.util.List;

public class HwSubscriptionManager extends Handler {
    private static final boolean DBG = true;
    public static final int DEFAULT_SLOT_ID = 0;
    public static final int DEFAULT_SUB_ID = 0;
    private static final boolean ERROR = true;
    private static final int EVENT_SET_SUBSCRIPTION_TIMEOUT = 11;
    private static final int EVENT_SET_UICC_SUBSCRIPTION_DONE = 10;
    private static final String LOG_TAG = "HwSubMgr";
    public static final int SUB_INIT_STATE = 255;
    private static final int SUB_NUMS = 0;
    private static final int TIME_SET_SUBSCRIPTION_TIMEOUT = 90000;
    private static final String USER_DATACALL_SUBSCRIPTION = "user_datacall_sub";
    private static HwSubscriptionManager mHwSubscriptionManager;
    private static UiccController mUiccController;
    private static SubscriptionControllerUtils subscriptionControllerUtils;
    private boolean mCardChange;
    private CommandsInterface[] mCi;
    private Message mCompleteMsg;
    private Context mContext;
    private Handler mHandler;
    private BroadcastReceiver mReceiver;
    private boolean mSetSubscriptionInProgress;
    private RegistrantList[] mSubActivatedRegistrantsOnSlot;
    private RegistrantList[] mSubDeactivatedRegistrantsOnSlot;
    private SubscriptionController mSubscriptionController;
    private SubscriptionHelper mSubscriptionHelper;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwSubscriptionManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwSubscriptionManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwSubscriptionManager.<clinit>():void");
    }

    public HwSubscriptionManager(Context context, CommandsInterface[] ci) {
        this.mSetSubscriptionInProgress = false;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int slotId;
                if ("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(intent.getAction())) {
                    slotId = intent.getIntExtra("subscription", -1);
                    HwSubscriptionManager.this.mSetSubscriptionInProgress = false;
                    HwSubscriptionManager.this.mHandler.removeMessages(HwSubscriptionManager.EVENT_SET_SUBSCRIPTION_TIMEOUT);
                    int result = intent.getIntExtra("operationResult", HwSubscriptionManager.SUB_NUMS);
                    HwSubscriptionManager.logd("Received ACTION_SUBSCRIPTION_SET_UICC_RESULT on slotId: " + slotId + ", result = " + result);
                    if (result == 1) {
                        HwSubscriptionManager.this.sendCompleteMsg(new RuntimeException("setSubScription fail!!!"));
                    } else {
                        HwSubscriptionManager.this.sendCompleteMsg(null);
                    }
                } else if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(intent.getAction())) {
                    slotId = intent.getIntExtra("subscription", -1);
                    int intValue = intent.getIntExtra("intContent", HwSubscriptionManager.SUB_NUMS);
                    String column = intent.getStringExtra("columnName");
                    HwSubscriptionManager.logd("Received ACTION_SUBINFO_CONTENT_CHANGE on slotId: " + slotId + " for " + column + ", intValue: " + intValue);
                    if ("sub_state".equals(column) && -1 != slotId) {
                        if (intValue == 1) {
                            HwSubscriptionManager.this.notifySlotSubscriptionActivated(slotId);
                        } else if (intValue == 0) {
                            HwSubscriptionManager.this.notifySlotSubscriptionDeactivated(slotId);
                        }
                    }
                }
            }
        };
        this.mContext = context;
        this.mCi = ci;
        this.mHandler = this;
        this.mSubscriptionController = SubscriptionController.getInstance();
        mUiccController = UiccController.getInstance();
        IntentFilter filter = new IntentFilter("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        filter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        this.mContext.registerReceiver(this.mReceiver, filter);
        this.mSubDeactivatedRegistrantsOnSlot = new RegistrantList[SUB_NUMS];
        this.mSubActivatedRegistrantsOnSlot = new RegistrantList[SUB_NUMS];
        for (int i = SUB_NUMS; i < SUB_NUMS; i++) {
            this.mSubActivatedRegistrantsOnSlot[i] = new RegistrantList();
            this.mSubDeactivatedRegistrantsOnSlot[i] = new RegistrantList();
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
        switch (msg.what) {
            case EVENT_SET_SUBSCRIPTION_TIMEOUT /*11*/:
                logd("EVENT_SET_SUBSCRIPTION_TIMEOUT");
                this.mSetSubscriptionInProgress = false;
                sendCompleteMsg(new RuntimeException("setSubScription timeout!!!"));
            default:
        }
    }

    public boolean setSubscription(int slotId, boolean activate, Message onCompleteMsg) {
        boolean isNeedSetDefault4GSlot = false;
        logd("setSubscription: slotId = " + slotId + ", activate = " + activate);
        if (!setSubscriptionCheck(slotId)) {
            return false;
        }
        int subId = getSubIdFromSlotId(slotId);
        this.mCompleteMsg = onCompleteMsg;
        int otherSlotId = slotId == 0 ? 1 : SUB_NUMS;
        if (activate) {
            if (this.mSubscriptionController.getSubState(subId) == 1) {
                logd("setSubscription: slotId = " + subId + " is already ACTIVED.");
                sendCompleteMsg(null);
            } else {
                boolean isCMCCHybird;
                if (HwAllInOneController.IS_CMCC_4GSWITCH_DISABLE && HwAllInOneController.getInstance().isCMCCCardBySlotId(slotId)) {
                    isCMCCHybird = HwAllInOneController.getInstance().isCMCCHybird();
                } else {
                    isCMCCHybird = false;
                }
                if (HwAllInOneController.IS_HISI_DSDX && HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == otherSlotId) {
                    if (this.mSubscriptionController.getSubState(otherSlotId) == 0) {
                        isCMCCHybird = ERROR;
                    }
                    isNeedSetDefault4GSlot = isCMCCHybird;
                }
                if (isNeedSetDefault4GSlot) {
                    boolean z;
                    if (HwVSimUtils.isVSimEnabled()) {
                        z = ERROR;
                    } else {
                        z = HwVSimUtils.isVSimCauseCardReload();
                    }
                    if (!z) {
                        if (HwAllInOneController.IS_FAST_SWITCH_SIMSLOT) {
                            this.mSubscriptionController.activateSubId(subId);
                        }
                        this.mSubscriptionController.setSubState(getSubIdFromSlotId(slotId), 1);
                        HwAllInOneController.getInstance().setDefault4GSlot(slotId, null);
                        return ERROR;
                    }
                }
                this.mSubscriptionController.activateSubId(subId);
                this.mHandler.sendEmptyMessageDelayed(EVENT_SET_SUBSCRIPTION_TIMEOUT, 90000);
            }
        } else if (this.mSubscriptionController.getSubState(subId) == 0) {
            logd("setSubscription: slotId = " + subId + " is already INACTIVED.");
            sendCompleteMsg(null);
        } else {
            if (HwAllInOneController.IS_HISI_DSDX && HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == slotId && this.mSubscriptionController.getSubState(otherSlotId) == 1) {
                if (!(!HwVSimUtils.isVSimEnabled() ? HwVSimUtils.isVSimCauseCardReload() : ERROR)) {
                    if (HwAllInOneController.IS_FAST_SWITCH_SIMSLOT) {
                        this.mSubscriptionController.deactivateSubId(subId);
                    }
                    this.mSubscriptionController.setSubState(getSubIdFromSlotId(slotId), SUB_NUMS);
                    HwAllInOneController.getInstance().setDefault4GSlot(otherSlotId, null);
                    return ERROR;
                }
            }
            this.mSubscriptionController.deactivateSubId(subId);
            this.mHandler.sendEmptyMessageDelayed(EVENT_SET_SUBSCRIPTION_TIMEOUT, 90000);
        }
        return ERROR;
    }

    private boolean setSubscriptionCheck(int slotId) {
        if (slotId < 0 || slotId >= SUB_NUMS) {
            loge("setSubscriptionCheck: slotId is not correct : " + slotId);
            return false;
        }
        if (this.mSubscriptionController == null) {
            this.mSubscriptionController = SubscriptionController.getInstance();
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
        }
        UiccCard uiccCard = mUiccController.getUiccCard(slotId);
        CardState cardState = CardState.CARDSTATE_ABSENT;
        if (uiccCard != null) {
            cardState = uiccCard.getCardState();
        }
        if (cardState != CardState.CARDSTATE_PRESENT) {
            logd("setSubscriptionCheck: Card is not present in slot " + slotId + ", return false");
            return false;
        } else if (TelephonyManager.getDefault().getCallState(getSubIdFromSlotId(slotId)) != 0) {
            logw("setSubscriptionCheck: Call State is not IDLE, can't set subscription!");
            return false;
        } else if (!HwAllInOneController.IS_HISI_DSDX || slotId != HwTelephonyManagerInner.getDefault().getDefault4GSlotId() || TelephonyManager.getDefault().getCallState() == 0) {
            return ERROR;
        } else {
            logw("setSubscriptionCheck: Call State is not IDLE, can't set default sub subscription!");
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void updateUserPreferences(boolean setDds) {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionController.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        int mActCount = SUB_NUMS;
        SubscriptionInfo mNextActivatedSub = null;
        int defaultSubId = this.mSubscriptionController.getDefaultSubId();
        int defaultDataSubId = this.mSubscriptionController.getDefaultDataSubId();
        int defaultVoiceSubId = this.mSubscriptionController.getDefaultVoiceSubId();
        int defaultSmsSubId = this.mSubscriptionController.getDefaultSmsSubId();
        logd("updateUserPreferences: defaultSubId = " + defaultSubId + ",defaultDataSubId = " + defaultDataSubId + ",defaultVoiceSubId = " + defaultVoiceSubId + ",defaultSmsSubId = " + defaultSmsSubId + ",setDDs = " + setDds);
        if (subInfoList == null) {
            logd("updateUserPreferences: subscription are not avaiable!!! Exit !");
            return;
        }
        for (SubscriptionInfo subInfo : subInfoList) {
            if (this.mSubscriptionController.getSubState(subInfo.getSubscriptionId()) == 1) {
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
                if (this.mSubscriptionController.getSubState(defaultSubId) != 0) {
                }
                subscriptionControllerUtils.setDefaultFallbackSubId(this.mSubscriptionController, mNextActivatedSub.getSubscriptionId());
                int dataSubState = this.mSubscriptionController.getSubState(defaultDataSubId);
                int dataSimState = TelephonyManager.getDefault().getSimState(defaultDataSubId);
                if (!(setDds || dataSubState == 0)) {
                    if (dataSimState == 1) {
                    }
                    if (this.mSubscriptionController.getSubState(defaultVoiceSubId) != 0) {
                    }
                    if (!this.mSubscriptionController.isVoicePromptEnabled()) {
                        this.mSubscriptionController.setDefaultVoiceSubId(mNextActivatedSub.getSubscriptionId());
                    }
                    if (this.mSubscriptionController.getSubState(defaultSmsSubId) != 0) {
                    }
                    if (!this.mSubscriptionController.isSMSPromptEnabled()) {
                        this.mSubscriptionController.setDefaultSmsSubId(mNextActivatedSub.getSubscriptionId());
                    }
                }
                if (dataSubState == 0 || dataSimState == 1) {
                    defaultDataSubId = mNextActivatedSub.getSubscriptionId();
                }
                this.mSubscriptionController.setDefaultDataSubId(defaultDataSubId);
                if (this.mSubscriptionController.getSubState(defaultVoiceSubId) != 0) {
                }
                if (this.mSubscriptionController.isVoicePromptEnabled()) {
                    this.mSubscriptionController.setDefaultVoiceSubId(mNextActivatedSub.getSubscriptionId());
                }
                if (this.mSubscriptionController.getSubState(defaultSmsSubId) != 0) {
                }
                if (this.mSubscriptionController.isSMSPromptEnabled()) {
                    this.mSubscriptionController.setDefaultSmsSubId(mNextActivatedSub.getSubscriptionId());
                }
            } else if (mActCount == subInfoList.size()) {
                int UserPrefDataSubId;
                int default4Gslot;
                int UserPrefDefaultSubId = getSubIdFromSlotId(Global.getInt(this.mContext.getContentResolver(), HwAllInOneController.USER_DEFAULT_SUBSCRIPTION, SUB_NUMS));
                if (UserPrefDefaultSubId == defaultSubId && UserPrefDefaultSubId == defaultVoiceSubId) {
                    if (UserPrefDefaultSubId != defaultSmsSubId) {
                    }
                    UserPrefDataSubId = getSubIdFromSlotId(Global.getInt(this.mContext.getContentResolver(), USER_DATACALL_SUBSCRIPTION, SUB_NUMS));
                    if (!(UserPrefDataSubId == defaultDataSubId || HwAllInOneController.IS_HISI_DSDX)) {
                        logd("updateUserPreferences: set UserPrefDataSubId from " + defaultDataSubId + "to " + UserPrefDataSubId);
                        this.mSubscriptionController.setDefaultDataSubId(UserPrefDataSubId);
                    }
                    logd("isSet4GSlotManuallyTriggered = " + HwAllInOneController.getInstance().isSet4GSlotManuallyTriggered);
                    if (HwAllInOneController.IS_HISI_DSDX) {
                        if (!HwAllInOneController.getInstance().isSet4GSlotManuallyTriggered) {
                            default4Gslot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                            if (default4Gslot != defaultDataSubId) {
                                logd("updateUserPreferences: set defaultDataSubId to default4Gslot from " + defaultDataSubId + " to " + default4Gslot);
                                this.mSubscriptionController.setDefaultDataSubId(default4Gslot);
                            }
                        }
                    }
                    if (!HwModemCapability.isCapabilitySupport(9)) {
                        HwAllInOneController.getInstance().setDefault4GSlotForCMCC();
                    }
                }
                logd("updateUserPreferences: set UserPrefDefaultSubId from " + defaultSubId + "to " + UserPrefDefaultSubId);
                subscriptionControllerUtils.setDefaultFallbackSubId(this.mSubscriptionController, UserPrefDefaultSubId);
                this.mSubscriptionController.setDefaultSmsSubId(UserPrefDefaultSubId);
                this.mSubscriptionController.setDefaultVoiceSubId(UserPrefDefaultSubId);
                UserPrefDataSubId = getSubIdFromSlotId(Global.getInt(this.mContext.getContentResolver(), USER_DATACALL_SUBSCRIPTION, SUB_NUMS));
                logd("updateUserPreferences: set UserPrefDataSubId from " + defaultDataSubId + "to " + UserPrefDataSubId);
                this.mSubscriptionController.setDefaultDataSubId(UserPrefDataSubId);
                logd("isSet4GSlotManuallyTriggered = " + HwAllInOneController.getInstance().isSet4GSlotManuallyTriggered);
                if (HwAllInOneController.IS_HISI_DSDX) {
                    if (HwAllInOneController.getInstance().isSet4GSlotManuallyTriggered) {
                        default4Gslot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                        if (default4Gslot != defaultDataSubId) {
                            logd("updateUserPreferences: set defaultDataSubId to default4Gslot from " + defaultDataSubId + " to " + default4Gslot);
                            this.mSubscriptionController.setDefaultDataSubId(default4Gslot);
                        }
                    }
                }
                if (HwModemCapability.isCapabilitySupport(9)) {
                    HwAllInOneController.getInstance().setDefault4GSlotForCMCC();
                }
            }
            logd("updateUserPreferences: after current DataSub = " + this.mSubscriptionController.getDefaultDataSubId() + " VoiceSub = " + this.mSubscriptionController.getDefaultVoiceSubId() + " SmsSub = " + this.mSubscriptionController.getDefaultSmsSubId());
        }
    }

    public int getUserPrefDataSubId() {
        int[] userPrefDataSubId = this.mSubscriptionController.getSubIdUsingSlotId(Global.getInt(this.mContext.getContentResolver(), USER_DATACALL_SUBSCRIPTION, SUB_NUMS));
        logd("getUserPrefDataSubId: userPrefDataSubId = " + userPrefDataSubId[SUB_NUMS]);
        return userPrefDataSubId[SUB_NUMS];
    }

    public void updateDataSlot() {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionController.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        int mActCount = SUB_NUMS;
        if (subInfoList != null) {
            for (SubscriptionInfo subInfo : subInfoList) {
                if (this.mSubscriptionController.getSubState(subInfo.getSubscriptionId()) == 1) {
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

    private void sendCompleteMsg(Exception e) {
        logd("sendCompleteMsg to target, Exception = " + e);
        if (this.mCompleteMsg != null) {
            AsyncResult.forMessage(this.mCompleteMsg).exception = e;
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
        Global.putInt(this.mContext.getContentResolver(), HwAllInOneController.USER_DEFAULT_SUBSCRIPTION, slotId);
        int subId = getSubIdFromSlotId(slotId);
        subscriptionControllerUtils.setDefaultFallbackSubId(this.mSubscriptionController, subId);
        this.mSubscriptionController.setDefaultSmsSubId(subId);
        this.mSubscriptionController.setDefaultVoiceSubId(subId);
    }

    public void setUserPrefDataSlotId(int slotId) {
        logd("setUserPrefDataSubId: slotId = " + slotId);
        if (slotId < 0 || slotId >= SUB_NUMS) {
            loge("setUserPrefDefaultSubId: invalid slotId!!!");
            return;
        }
        Global.putInt(this.mContext.getContentResolver(), USER_DATACALL_SUBSCRIPTION, slotId);
        this.mSubscriptionController.setDefaultDataSubId(getSubIdFromSlotId(slotId));
    }

    protected int getDefaultDataSlotId() {
        int slotId = this.mSubscriptionController.getSlotId(this.mSubscriptionController.getDefaultDataSubId());
        logd("getDefaultDataSlotId: slotId = " + slotId);
        return slotId;
    }

    protected int getSubIdFromSlotId(int slotId) {
        return this.mSubscriptionController.getSubId(slotId)[SUB_NUMS];
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

    private void notifySlotSubscriptionActivated(int slotId) {
        logd("notifySlotSubscriptionActivated: slotId = " + slotId);
        this.mSubActivatedRegistrantsOnSlot[slotId].notifyRegistrants();
    }

    private void notifySlotSubscriptionDeactivated(int slotId) {
        logd("notifySlotSubscriptionDeactivated: slotId = " + slotId);
        this.mSubDeactivatedRegistrantsOnSlot[slotId].notifyRegistrants();
    }

    private static void logd(String message) {
        Rlog.d(LOG_TAG, "[HwSubscriptionManager]" + message);
    }

    private static void logw(String message) {
        Rlog.w(LOG_TAG, "[HwSubscriptionManager]" + message);
    }

    private static void loge(String message) {
        Rlog.e(LOG_TAG, "[HwSubscriptionManager]" + message);
    }
}
