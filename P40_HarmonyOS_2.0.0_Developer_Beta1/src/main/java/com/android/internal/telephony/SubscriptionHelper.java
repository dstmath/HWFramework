package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.SubscriptionInfo;
import android.util.Log;
import com.android.internal.telephony.uicc.IccCardStatusUtils;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionInfoEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandExceptionEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.IccRefreshResponseEx;
import com.huawei.internal.telephony.uicc.IccUtilsEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import com.huawei.internal.telephony.uicc.UiccProfileEx;
import java.util.Arrays;
import java.util.List;

public class SubscriptionHelper extends Handler {
    private static final String APM_SIM_NOT_PWDN_PROPERTY = "persist.radio.apm_sim_not_pwdn";
    private static final int EVENT_RADIO_AVAILABLE = 5;
    private static final int EVENT_RADIO_ON = 4;
    private static final int EVENT_REFRESH = 2;
    private static final int EVENT_SET_UICC_SUBSCRIPTION_DONE = 1;
    private static final String ICCID_STRING_FOR_NO_SIM = "";
    private static final boolean IS_SETUICCSUB_BY_SLOT = SystemPropertiesEx.getBoolean("ro.config.setuiccsub_by_slot", false);
    private static final String LOG_TAG = "SubscriptionHelper";
    private static final int PHONE_NUMBER_ONE = 1;
    private static final int SIM_CHANGED = -1;
    private static final int SIM_NEW = -2;
    private static final int SIM_NOT_CHANGE = 0;
    private static final int SIM_NOT_INSERT = -99;
    public static final int SIM_REPOSITION = -3;
    private static final int SIM_WITH_SAME_ICCID1 = 1;
    private static final int SIM_WITH_SAME_ICCID2 = 2;
    private static final int STATUS_NO_SIM_INSERTED = 0;
    private static final int STATUS_SIM1_INSERTED = 1;
    private static final int STATUS_SIM2_INSERTED = 2;
    private static final int STATUS_SIM3_INSERTED = 4;
    private static final int SUB_1 = 1;
    public static final int SUB_INIT_STATE = -1;
    public static final int SUB_SET_UICC_FAIL = -100;
    public static final int SUB_SIM_NOT_INSERTED = -99;
    private static final int SUB_SIM_REFRESH = -101;
    private static final boolean sApmSIMNotPwdn;
    private static SubscriptionHelper sInstance;
    private int INVALID_VALUE = -1;
    private CommandsInterfaceEx[] mCi;
    private Context mContext;
    private int[] mInsertSimState;
    private boolean[] mNeedResetSub;
    private int mNewCardCount = 0;
    private int mNewSimStatus = 0;
    private int[] mNewSubState;
    private int mNumPhones;
    private boolean mNwModeUpdated = false;
    private String[] mOldIccId;
    private int[] mSubStatus;
    private boolean mTriggerDds = false;
    private final ContentObserver nwModeObserver = new ContentObserver(new Handler()) {
        /* class com.android.internal.telephony.SubscriptionHelper.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfUpdate) {
            SubscriptionHelper.logd("NwMode Observer onChange hit !!!");
            if (SubscriptionHelper.this.mNwModeUpdated) {
                SubscriptionHelper.this.updateNwModesInSubIdTable(true);
            }
        }
    };

    static {
        boolean z = true;
        if (SystemPropertiesEx.getInt(APM_SIM_NOT_PWDN_PROPERTY, 0) != 1) {
            z = false;
        }
        sApmSIMNotPwdn = z;
    }

    private SubscriptionHelper(Context c, CommandsInterfaceEx[] ci) {
        this.mContext = c;
        this.mCi = ci;
        this.mNumPhones = TelephonyManagerEx.getDefault().getPhoneCount();
        int i = this.mNumPhones;
        this.mSubStatus = new int[i];
        this.mNewSubState = new int[i];
        this.mNeedResetSub = new boolean[i];
        this.mInsertSimState = new int[i];
        this.mOldIccId = new String[i];
        for (int i2 = 0; i2 < this.mNumPhones; i2++) {
            this.mSubStatus[i2] = -1;
            this.mNewSubState[i2] = this.INVALID_VALUE;
            this.mNeedResetSub[i2] = false;
            this.mInsertSimState[i2] = 0;
            this.mOldIccId[i2] = null;
            Integer index = Integer.valueOf(i2);
            this.mCi[i2].registerForIccRefresh(this, 2, index);
            this.mCi[i2].registerForOn(this, 4, index);
            this.mCi[i2].registerForAvailable(this, 5, index);
            ContentResolver contentResolver = this.mContext.getContentResolver();
            contentResolver.registerContentObserver(Settings.Global.getUriFor("preferred_network_mode" + i2), false, this.nwModeObserver);
        }
        logd("SubscriptionHelper init by Context, num phones = " + this.mNumPhones + " ApmSIMNotPwdn = " + sApmSIMNotPwdn);
    }

    public static SubscriptionHelper init(Context context, CommandsInterfaceEx[] ci) {
        SubscriptionHelper subscriptionHelper;
        synchronized (SubscriptionHelper.class) {
            if (sInstance == null) {
                sInstance = new SubscriptionHelper(context, ci);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            subscriptionHelper = sInstance;
        }
        return subscriptionHelper;
    }

    public static SubscriptionHelper getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    public static void logd(String message) {
        RlogEx.i(LOG_TAG, message);
    }

    private static void logi(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNwModesInSubIdTable(boolean override) {
        SubscriptionControllerEx subscriptionControllerEx = SubscriptionControllerEx.getInstance();
        for (int i = 0; i < this.mNumPhones; i++) {
            int[] subIdList = SubscriptionManagerEx.getSubId(i);
            if (subIdList != null && subIdList[0] >= 0) {
                int nwModeInDb = HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, i);
                int nwModeinSubIdTable = subscriptionControllerEx.getNwMode(subIdList[0]);
                logd("updateNwModesInSubIdTable: nwModeinSubIdTable: " + nwModeinSubIdTable + ", nwModeInDb: " + nwModeInDb);
                if (override || nwModeinSubIdTable == -1) {
                    subscriptionControllerEx.setNwMode(subIdList[0], nwModeInDb);
                }
            }
        }
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        AsyncResultEx asyncResultEx;
        int i = msg.what;
        if (i == 1) {
            logd("EVENT_SET_UICC_SUBSCRIPTION_DONE");
            processSetUiccSubscriptionDone(msg);
        } else if (i == 2) {
            logd("EVENT_REFRESH");
            processSimRefresh(AsyncResultEx.from(msg.obj));
        } else if ((i == 4 || i == 5) && (asyncResultEx = AsyncResultEx.from(msg.obj)) != null) {
            Integer Index = (Integer) asyncResultEx.getUserObj();
            logd("[EVENT_RADIO_ON or EVENT_RADIO_AVAILABLE]: Index" + Index);
            if (Index.intValue() != this.INVALID_VALUE && this.mNewSubState[Index.intValue()] != this.INVALID_VALUE && this.mNeedResetSub[Index.intValue()]) {
                logd("[EVENT_RADIO_ON or EVENT_RADIO_AVAILABLE]: Need to reset UICC Subscription,Index = " + Index + ";mNewSubState = " + this.mNewSubState[Index.intValue()]);
                setUiccSubscription(Index.intValue(), this.mNewSubState[Index.intValue()]);
            }
        }
    }

    public boolean needSubActivationAfterRefresh(int slotId) {
        return this.mNumPhones > 1 && this.mSubStatus[slotId] == SUB_SIM_REFRESH;
    }

    public synchronized void resetInsertSimState() {
        logd("[resetInsertSimState]: reset the sInsertSimState to not change");
        for (int i = 0; i < this.mNumPhones; i++) {
            this.mInsertSimState[i] = 0;
        }
    }

    public synchronized void resetStateAndIccIdInfos() {
        logd("[resetStateAndIccIdInfos]: reset the sInsertSimState and old iccid.");
        for (int i = 0; i < this.mNumPhones; i++) {
            this.mInsertSimState[i] = 0;
            this.mOldIccId[i] = null;
        }
        this.mNewSimStatus = 0;
        this.mNewCardCount = 0;
    }

    /* access modifiers changed from: package-private */
    public void recordSimState(int slotId, String iccid) {
        logd("recordSimState, slotId = " + slotId + ", iccid = " + SubscriptionInfoEx.givePrintableIccid(iccid));
        if (slotId >= 0 && slotId < this.mNumPhones) {
            boolean isIccidChanged = false;
            this.mInsertSimState[slotId] = 0;
            if ("".equals(iccid)) {
                this.mInsertSimState[slotId] = -99;
            }
            String decIccId = IccUtilsEx.getDecimalSubstring(iccid);
            for (int i = 0; i < this.mNumPhones; i++) {
                String[] strArr = this.mOldIccId;
                if (strArr[i] == null) {
                    strArr[i] = getOldIccId(i);
                    logd("getOldIccId: oldIccId[" + i + "] = " + SubscriptionInfoEx.givePrintableIccid(this.mOldIccId[i]) + ", sIccId = " + SubscriptionInfoEx.givePrintableIccid(iccid));
                }
            }
            if (!this.mOldIccId[slotId].isEmpty()) {
                if (this.mInsertSimState[slotId] == 0 && iccid != null && !iccid.equals(this.mOldIccId[slotId]) && (decIccId == null || !decIccId.equals(this.mOldIccId[slotId]))) {
                    isIccidChanged = true;
                }
                if (isIccidChanged) {
                    this.mInsertSimState[slotId] = -1;
                }
            } else {
                int[] iArr = this.mInsertSimState;
                if (iArr[slotId] == 0) {
                    iArr[slotId] = -1;
                }
            }
            checkIfInsertNewSim(slotId, this.mOldIccId[slotId], iccid, decIccId);
            int[] iArr2 = this.mInsertSimState;
            if (iArr2[slotId] == -1) {
                iArr2[slotId] = -3;
            }
            logd("recordSimState, slotId = " + slotId + ", state = " + this.mInsertSimState[slotId]);
        }
    }

    private String getOldIccId(int slotId) {
        List<SubscriptionInfo> oldSubInfo = SubscriptionControllerEx.getInstance().getSubInfoUsingSlotIndexPrivileged(slotId);
        if (oldSubInfo == null || oldSubInfo.size() <= 0) {
            logd("getOldIccId: No SIM in slot " + slotId + " last time");
            return "";
        }
        String oldIccId = oldSubInfo.get(0).getIccId();
        logd("getOldIccId: oldSubId = " + oldSubInfo.get(0).getSubscriptionId());
        return oldIccId;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x005a  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0073  */
    private void checkIfInsertNewSim(int slotId, String oldIccId, String iccid, String decIccId) {
        boolean hasTwoSimIccIdCards;
        int[] iArr = this.mInsertSimState;
        if (iArr[slotId] == -99) {
            logd("checkIfInsertNewSim: No SIM inserted in slot " + slotId + " this time");
            if (this.mNumPhones == 1) {
                HwUiccManagerImpl.getDefault().updateUserPreferences(false);
                return;
            }
            return;
        }
        if (iArr[slotId] == slotId + 1 && oldIccId != null) {
            if (oldIccId.equals(iccid + Integer.toString(this.mInsertSimState[slotId]))) {
                hasTwoSimIccIdCards = true;
                if (!hasTwoSimIccIdCards) {
                    logd("checkIfInsertNewSim: same iccid not change index = " + slotId);
                    this.mInsertSimState[slotId] = 0;
                    return;
                } else if (isNewSim(iccid, decIccId, this.mOldIccId)) {
                    this.mNewCardCount++;
                    if (slotId == 0) {
                        this.mNewSimStatus |= 1;
                    } else if (slotId == 1) {
                        this.mNewSimStatus = 2 | this.mNewSimStatus;
                    } else if (slotId == 2) {
                        this.mNewSimStatus |= 4;
                    }
                    this.mInsertSimState[slotId] = SIM_NEW;
                    return;
                } else {
                    return;
                }
            }
        }
        hasTwoSimIccIdCards = false;
        if (!hasTwoSimIccIdCards) {
        }
    }

    private boolean isNewSim(String iccId, String decIccId, String[] oldIccId) {
        boolean newSim = true;
        int i = 0;
        while (true) {
            if (i < this.mNumPhones) {
                if (iccId == null || !iccId.equals(oldIccId[i])) {
                    if (decIccId != null && decIccId.equals(oldIccId[i])) {
                        newSim = false;
                        break;
                    }
                    i++;
                } else {
                    newSim = false;
                    break;
                }
            } else {
                break;
            }
        }
        logd("isNewSim: newSim = " + newSim);
        return newSim;
    }

    public void updateSubActivation(int slotId, IccCardStatusExt.CardStateEx[] cardState) {
        SubscriptionControllerEx subCtrlr = SubscriptionControllerEx.getInstance();
        boolean setUiccSent = false;
        logd("updateSubActivation, slot[" + slotId + "] simStatus = " + this.mInsertSimState[slotId]);
        int subId = SubscriptionManagerEx.getSubIdUsingSlotId(slotId);
        int[] iArr = this.mInsertSimState;
        int i = iArr[slotId];
        boolean isVSimSkipUpdateUserPref = true;
        if (i != -99) {
            if (!(i == -3 || i == SIM_NEW)) {
                if (i == 0) {
                    setUiccSent = handleNotChangeSimCard(subCtrlr, subId, slotId);
                } else if (!(i == 1 || i == 2)) {
                    loge("updateSubActivation, slot [" + slotId + "], incorrect status: " + this.mInsertSimState[slotId]);
                }
            }
            setUiccSent = handleNewSimCard(subCtrlr, subId, slotId);
        } else {
            this.mSubStatus[slotId] = iArr[slotId];
            logd("updateSubActivation, slot[" + slotId + "] sim is not insert.");
        }
        if (isAllSubsAvailable() && !setUiccSent && isPlugOutDone(slotId, cardState)) {
            logd("updateSubActivation, Received all sim info, update user pref subs, triggerDds= " + this.mTriggerDds);
            if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || ((!HwVSimUtils.isVSimDsdsVersionOne() || !HwVSimUtils.isVSimEnabled()) && !HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload())) {
                isVSimSkipUpdateUserPref = false;
            }
            if (isVSimSkipUpdateUserPref) {
                sendDefaultChangedBroadcastForVsim(subCtrlr);
                logd("updateSubActivation, vsim skip updateUserPreferences");
            } else {
                HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyFactoryImpl").createHwUiccManager().updateUserPreferences(this.mTriggerDds);
            }
            this.mTriggerDds = false;
        }
    }

    private void sendDefaultChangedBroadcastForVsim(SubscriptionControllerEx subCtrlr) {
        List<SubscriptionInfo> subInfoList = subCtrlr.getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        if (subInfoList == null) {
            logd("sendDefaultChangedBroadcastForVsim subInfoList = null");
            return;
        }
        int actCount = 0;
        SubscriptionInfo nextActivatedSub = null;
        for (SubscriptionInfo subInfo : subInfoList) {
            if (subCtrlr.getSubState(subInfo.getSimSlotIndex()) == 1) {
                actCount++;
                if (nextActivatedSub == null) {
                    nextActivatedSub = subInfo;
                }
            }
        }
        if (actCount == 1) {
            logd("sendDefaultChangedBroadcastForVsim actCount = 1 subid = " + nextActivatedSub.getSubscriptionId());
            subCtrlr.sendDefaultChangedBroadcast(nextActivatedSub.getSubscriptionId());
        } else if (actCount == subInfoList.size()) {
            int userPrefDefaultSubId = SubscriptionManagerEx.getSubIdUsingSlotId(Settings.Global.getInt(this.mContext.getContentResolver(), "user_default_sub", 0));
            logd("sendDefaultChangedBroadcastForVsim subid = " + userPrefDefaultSubId);
            subCtrlr.sendDefaultChangedBroadcast(userPrefDefaultSubId);
        } else {
            logd("sendDefaultChangedBroadcastForVsim do nothing");
        }
    }

    private boolean handleNewSimCard(SubscriptionControllerEx subCtrlr, int subId, int slotId) {
        if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || (!HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload())) {
            logd("handleNewSimCard, slot[" + slotId + "] sim has changed, should activate it.");
            if (HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(slotId, "disable-sub")) {
                setUiccSubscription(1, 0);
                return true;
            }
            subCtrlr.activateSubId(subId);
            if (HwTelephonyManager.getDefault().isPlatformSupportVsim()) {
                HwVSimUtils.setSubActived(slotId);
            }
            this.mNewSubState[slotId] = 1;
            return true;
        }
        logd("handleNewSimCard, vsim caused sim load, skip it.");
        return false;
    }

    private boolean handleNotChangeSimCard(SubscriptionControllerEx subCtrlr, int subId, int slotId) {
        if (this.mNeedResetSub[slotId]) {
            logd("slot[" + slotId + "], sim no change, but mNeedResetSub, skip it");
            return true;
        } else if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.getIsWaitingSwitchCdmaModeSide()) {
            logd("slot[" + slotId + "], sim no change, but isWaitingSwitchCdmaModeSide, skip it");
            return true;
        } else if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.getIsWaitingNvMatchUnsol()) {
            logd("slot[" + slotId + "], sim no change, but isWaitingNvMatchUnsol, skip it");
            return true;
        } else if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || !HwVSimUtils.prohibitSubUpdateSimNoChange(slotId)) {
            int[] iArr = this.mNewSubState;
            int subState = iArr[slotId] != this.INVALID_VALUE ? iArr[slotId] : subCtrlr.getSubState(slotId);
            logd("slot[" + slotId + "], sim no change, subState should be " + subState);
            if (subState == 1) {
                subCtrlr.activateSubId(subId);
            } else {
                subCtrlr.deactivateSubId(subId);
            }
            this.mNewSubState[slotId] = subState;
            return true;
        } else {
            logd("slot[" + slotId + "], sim no change, but vsim prohibit, skip it");
            return true;
        }
    }

    public void updateNwMode() {
        updateNwModesInSubIdTable(false);
        this.mNwModeUpdated = true;
    }

    public void setUiccSubscription(int slotId, int subStatus) {
        int numApplication;
        int appType;
        logd("setUiccSubscription: slotId:" + slotId + ", subStatus:" + subStatus);
        if (SubscriptionManagerEx.isValidSlotIndex(slotId)) {
            UiccCardExt uiccCard = UiccControllerExt.getInstance().getUiccCard(slotId);
            int i = 1;
            if (!HuaweiTelephonyConfigs.isQcomPlatform()) {
                Message msgSetUiccSubDone = Message.obtain(this, 1, slotId, subStatus);
                if ((TelephonyManagerEx.MultiSimVariantsExt.DSDS == TelephonyManagerEx.getMultiSimConfiguration()) || SystemPropertiesEx.getBoolean("ro.hwpp.set_uicc_by_radiopower", false)) {
                    this.mNewSubState[slotId] = this.INVALID_VALUE;
                    this.mNeedResetSub[slotId] = false;
                    PhoneFactoryExt.getPhone(slotId).setRadioPower(subStatus != 0, msgSetUiccSubDone);
                    if (HwTelephonyManager.getDefault().isPlatformSupportVsim()) {
                        if (subStatus == 0) {
                            i = 0;
                        }
                        HwVSimUtils.updateSubState(slotId, i);
                        return;
                    }
                    return;
                }
                this.mCi[slotId].setUiccSubscription(slotId, 0, slotId, subStatus, msgSetUiccSubDone);
            } else if (uiccCard == null || uiccCard.getUiccProfile().getNumApplications() == 0) {
                logd("setUiccSubscription: slotId:" + slotId + " card info not available");
                PhoneFactoryExt.resetIccid(slotId);
                Message msgSetUiccSubDone2 = Message.obtain(this, 1, slotId, subStatus);
                AsyncResultEx.forMessage(msgSetUiccSubDone2, false, (Exception) CommandExceptionEx.fromRilErrno(2));
                msgSetUiccSubDone2.sendToTarget();
            } else if (!IS_SETUICCSUB_BY_SLOT) {
                UiccProfileEx uiccProfileEx = uiccCard.getUiccProfile();
                int numApplication2 = uiccProfileEx.getNumApplications();
                boolean set3GPP2Done = false;
                boolean set3GPPDone = false;
                int i2 = 0;
                while (i2 < numApplication2) {
                    int appType2 = uiccProfileEx.getApplicationIndex(i2) != null ? uiccProfileEx.getApplicationIndex(i2).getType() : 0;
                    if (set3GPPDone) {
                        appType = appType2;
                        numApplication = numApplication2;
                    } else if (appType2 == 2 || appType2 == 1) {
                        numApplication = numApplication2;
                        this.mCi[slotId].setUiccSubscription(slotId, i2, slotId, subStatus, Message.obtain(this, 1, slotId, subStatus));
                        set3GPPDone = true;
                        if (set3GPPDone || !set3GPP2Done) {
                            i2++;
                            numApplication2 = numApplication;
                        } else {
                            return;
                        }
                    } else {
                        appType = appType2;
                        numApplication = numApplication2;
                    }
                    if (!set3GPP2Done && (appType == 4 || appType == 3)) {
                        this.mCi[slotId].setUiccSubscription(slotId, i2, slotId, subStatus, Message.obtain(this, 1, slotId, subStatus));
                        set3GPP2Done = true;
                    }
                    if (set3GPPDone) {
                    }
                    i2++;
                    numApplication2 = numApplication;
                }
            } else {
                this.mCi[slotId].setUiccSubscription(slotId, 0, slotId, subStatus, Message.obtain(this, 1, slotId, subStatus));
            }
        }
    }

    private void processSetUiccSubscriptionDone(Message msg) {
        SubscriptionControllerEx subCtrlr = SubscriptionControllerEx.getInstance();
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        int slotId = msg.arg1;
        int newSubState = msg.arg2;
        boolean isVSimSkipUpdateUserPref = true;
        if (ar == null || ar.getException() != null) {
            loge("Exception in SET_UICC_SUBSCRIPTION, slotId = " + slotId + " newSubState " + newSubState);
            this.mSubStatus[slotId] = -100;
            if (ar != null && CommandExceptionEx.isSpecificError(ar.getException(), CommandExceptionEx.Error.RADIO_NOT_AVAILABLE)) {
                this.mNewSubState[slotId] = newSubState;
                this.mNeedResetSub[slotId] = true;
                this.mSubStatus[slotId] = -1;
                logd("Store subinfo and set mNeedResetSub to true because of RADIO_NOT_AVAILABLE, mNeedResetSub[" + slotId + "]:" + this.mNeedResetSub[slotId]);
            }
            broadcastSetUiccResult(slotId, newSubState, 1);
            return;
        }
        if (newSubState != subCtrlr.getSubState(slotId)) {
            subCtrlr.setSubState(slotId, newSubState);
        }
        broadcastSetUiccResult(slotId, newSubState, 0);
        this.mSubStatus[slotId] = newSubState;
        if (isAllSubsAvailable()) {
            logd("Received all subs, now update user preferred subs, slotid = " + slotId + " newSubState = " + newSubState + " mTriggerDds = " + this.mTriggerDds);
            if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || ((!HwVSimUtils.isVSimDsdsVersionOne() || !HwVSimUtils.isVSimEnabled()) && !HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload())) {
                isVSimSkipUpdateUserPref = false;
            }
            if (isVSimSkipUpdateUserPref) {
                logd("vsim skip updateUserPreferences");
            } else {
                HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyFactoryImpl").createHwUiccManager().updateUserPreferences(this.mTriggerDds);
            }
            if (this.mTriggerDds && !HuaweiTelephonyConfigs.isHisiPlatform()) {
                HwSubscriptionManagerUtils.getInstance().updateDataSlot();
            }
            this.mTriggerDds = false;
        }
        this.mNewSubState[slotId] = this.INVALID_VALUE;
        this.mNeedResetSub[slotId] = false;
    }

    private void processSimRefresh(AsyncResultEx ar) {
        if (ar == null || ar.getException() != null || ar.getResult() == null) {
            loge("processSimRefresh received without input");
            return;
        }
        Integer index = (Integer) ar.getUserObj();
        IccRefreshResponseEx state = IccRefreshResponseEx.from(ar.getResult());
        StringBuilder sb = new StringBuilder();
        sb.append(" Received SIM refresh, reset sub state ");
        sb.append(index);
        sb.append(" old sub state ");
        sb.append(this.mSubStatus[index.intValue()]);
        sb.append(" refreshResult = ");
        sb.append(state != null ? Integer.valueOf(state.getRefreshResult()) : "exception");
        logi(sb.toString());
        if (state != null && state.getRefreshResult() == 2) {
            this.mSubStatus[index.intValue()] = SUB_SIM_REFRESH;
        }
    }

    private void broadcastSetUiccResult(int slotId, int newSubState, int result) {
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        intent.addFlags(16777216);
        SubscriptionManagerEx.putPhoneIdAndSubIdExtra(intent, slotId);
        intent.putExtra("operationResult", result);
        intent.putExtra("newSubState", newSubState);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandleEx.ALL);
    }

    private boolean isAllSubsAvailable() {
        boolean allSubsAvailable = PhoneFactoryExt.isAllIccIdQueryDoneHw();
        for (int i = 0; i < this.mNumPhones; i++) {
            if (this.mSubStatus[i] == -1) {
                allSubsAvailable = false;
            }
        }
        return allSubsAvailable;
    }

    private boolean isPlugOutDone(int slotId, IccCardStatusExt.CardStateEx[] cardState) {
        boolean isPlugOutDone = true;
        if (!HwCardTrayInfo.getInstance().isCardTrayOut(0)) {
            return true;
        }
        for (int i = 0; i < this.mNumPhones; i++) {
            if (i != slotId && IccCardStatusUtils.isCardPresentHw(cardState[i])) {
                logd("PlugOut not finish");
                isPlugOutDone = false;
            }
        }
        return isPlugOutDone;
    }

    public boolean isRadioOn(int phoneId) {
        return this.mCi[phoneId].getRadioState() == 1;
    }

    public boolean isRadioAvailable(int phoneId) {
        return this.mCi[phoneId].getRadioState() != 2;
    }

    public boolean isApmSIMNotPwdn() {
        return sApmSIMNotPwdn;
    }

    public boolean proceedToHandleIccEvent(int slotId) {
        int apmState = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
        if (!sApmSIMNotPwdn && (!isRadioOn(slotId) || apmState == 1)) {
            logi(" proceedToHandleIccEvent, radio off/unavailable, slotId = " + slotId);
            this.mSubStatus[slotId] = -1;
        }
        if (apmState == 1 && !sApmSIMNotPwdn) {
            logd(" proceedToHandleIccEvent, sApmSIMNotPwdn = " + sApmSIMNotPwdn);
            return false;
        } else if (isRadioAvailable(slotId)) {
            return true;
        } else {
            logi(" proceedToHandleIccEvent, radio not available, slotId = " + slotId);
            if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || !HwVSimUtils.isPlatformTwoModems() || HwVSimUtils.isRadioAvailable(slotId)) {
                return false;
            }
            logi("proceedToHandleIccEvent, vsim pending sub");
            return true;
        }
    }

    public String[] getOldIccId() {
        String[] strArr = this.mOldIccId;
        return (String[]) Arrays.copyOf(strArr, strArr.length);
    }

    public int getNewCardCount() {
        return this.mNewCardCount;
    }

    public int getNewSimStatus() {
        return this.mNewSimStatus;
    }

    public int[] getInsertSimState() {
        int[] iArr = this.mInsertSimState;
        return Arrays.copyOf(iArr, iArr.length);
    }
}
