package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.uicc.IccCardStatusUtils;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import com.huawei.internal.telephony.uicc.UiccProfileEx;
import java.util.List;

public class SubscriptionHelper extends Handler {
    private static final String APM_SIM_NOT_PWDN_PROPERTY = "persist.radio.apm_sim_not_pwdn";
    public static final byte[] C1 = {98, 94, -52, 117, -82, 28, -44, 66, 28, 61, -110, -119, -75, 70, 2, 85};
    private static final int EVENT_RADIO_AVAILABLE = 5;
    private static final int EVENT_RADIO_ON = 4;
    private static final int EVENT_REFRESH = 2;
    private static final int EVENT_SET_UICC_SUBSCRIPTION_DONE = 1;
    private static final String ICCID_STRING_FOR_NO_SIM = "";
    private static final boolean IS_SETUICCSUB_BY_SLOT = SystemProperties.getBoolean("ro.config.setuiccsub_by_slot", false);
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
    private static boolean mNwModeUpdated = false;
    private static final boolean sApmSIMNotPwdn;
    private static SubscriptionHelper sInstance;
    private static int sNumPhones;
    private static boolean sTriggerDds = false;
    private int INVALID_VALUE = -1;
    private CommandsInterfaceEx[] mCi;
    private Context mContext;
    private int[] mInsertSimState;
    private boolean[] mNeedResetSub;
    private int mNewCardCount = 0;
    private int mNewSimStatus = 0;
    private int[] mNewSubState;
    private String[] mOldIccId;
    private int[] mSubStatus;
    private final ContentObserver nwModeObserver = new ContentObserver(new Handler()) {
        /* class com.android.internal.telephony.SubscriptionHelper.AnonymousClass1 */

        public void onChange(boolean selfUpdate) {
            SubscriptionHelper.logd("NwMode Observer onChange hit !!!");
            if (SubscriptionHelper.mNwModeUpdated) {
                SubscriptionHelper.this.updateNwModesInSubIdTable(true);
            }
        }
    };

    static {
        boolean z = true;
        if (SystemProperties.getInt(APM_SIM_NOT_PWDN_PROPERTY, 0) != 1) {
            z = false;
        }
        sApmSIMNotPwdn = z;
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

    private SubscriptionHelper(Context c, CommandsInterfaceEx[] ci) {
        this.mContext = c;
        this.mCi = ci;
        sNumPhones = TelephonyManager.getDefault().getPhoneCount();
        int i = sNumPhones;
        this.mSubStatus = new int[i];
        this.mNewSubState = new int[i];
        this.mNeedResetSub = new boolean[i];
        this.mInsertSimState = new int[i];
        this.mOldIccId = new String[i];
        for (int i2 = 0; i2 < sNumPhones; i2++) {
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
        logd("SubscriptionHelper init by Context, num phones = " + sNumPhones + " ApmSIMNotPwdn = " + sApmSIMNotPwdn);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNwModesInSubIdTable(boolean override) {
        SubscriptionControllerEx subscriptionControllerEx = SubscriptionControllerEx.getInstance();
        for (int i = 0; i < sNumPhones; i++) {
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

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            logd("EVENT_SET_UICC_SUBSCRIPTION_DONE");
            processSetUiccSubscriptionDone(msg);
        } else if (i == 2) {
            logd("EVENT_REFRESH");
            processSimRefresh((AsyncResult) msg.obj);
        } else if (i == 4 || i == 5) {
            Integer Index = (Integer) ((AsyncResult) msg.obj).userObj;
            logd("[EVENT_RADIO_ON or EVENT_RADIO_AVAILABLE]: Index" + Index);
            if (Index.intValue() != this.INVALID_VALUE && this.mNewSubState[Index.intValue()] != this.INVALID_VALUE && true == this.mNeedResetSub[Index.intValue()]) {
                logd("[EVENT_RADIO_ON or EVENT_RADIO_AVAILABLE]: Need to reset UICC Subscription,Index = " + Index + ";mNewSubState = " + this.mNewSubState[Index.intValue()]);
                setUiccSubscription(Index.intValue(), this.mNewSubState[Index.intValue()]);
            }
        }
    }

    public boolean needSubActivationAfterRefresh(int slotId) {
        return sNumPhones > 1 && this.mSubStatus[slotId] == SUB_SIM_REFRESH;
    }

    public synchronized void resetInsertSimState() {
        logd("[resetInsertSimState]: reset the sInsertSimState to not change");
        for (int i = 0; i < sNumPhones; i++) {
            this.mInsertSimState[i] = 0;
        }
    }

    public synchronized void resetStateAndIccIdInfos() {
        logd("[resetStateAndIccIdInfos]: reset the sInsertSimState and old iccid.");
        for (int i = 0; i < sNumPhones; i++) {
            this.mInsertSimState[i] = 0;
            this.mOldIccId[i] = null;
        }
        this.mNewSimStatus = 0;
        this.mNewCardCount = 0;
    }

    /* access modifiers changed from: package-private */
    public void recordSimState(int slotId, String iccid) {
        logd("recordSimState, slotId = " + slotId + ", iccid = " + SubscriptionInfo.givePrintableIccid(iccid));
        if (slotId >= 0 && slotId < sNumPhones) {
            boolean isIccidChanged = false;
            this.mInsertSimState[slotId] = 0;
            if (ICCID_STRING_FOR_NO_SIM.equals(iccid)) {
                this.mInsertSimState[slotId] = -99;
            }
            String decIccId = IccUtils.getDecimalSubstring(iccid);
            for (int i = 0; i < sNumPhones; i++) {
                String[] strArr = this.mOldIccId;
                if (strArr[i] == null) {
                    strArr[i] = getOldIccId(i);
                    logd("getOldIccId: oldIccId[" + i + "] = " + SubscriptionInfo.givePrintableIccid(this.mOldIccId[i]) + ", sIccId = " + SubscriptionInfo.givePrintableIccid(iccid));
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
        List<SubscriptionInfo> oldSubInfo = SubscriptionController.getInstance().getSubInfoUsingSlotIndexPrivileged(slotId);
        if (oldSubInfo == null || oldSubInfo.size() <= 0) {
            logd("getOldIccId: No SIM in slot " + slotId + " last time");
            return ICCID_STRING_FOR_NO_SIM;
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
            if (sNumPhones == 1) {
                HwTelephonyFactory.getHwUiccManager().updateUserPreferences(false);
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
                    } else if (slotId != 2) {
                        this.mNewSimStatus |= 0;
                    } else {
                        this.mNewSimStatus |= 4;
                    }
                    this.mInsertSimState[slotId] = -2;
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
            if (i < sNumPhones) {
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
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        boolean setUiccSent = false;
        logd("updateSubActivation, slot[" + slotId + "] simStatus = " + this.mInsertSimState[slotId]);
        int subId = subCtrlr.getSubIdUsingPhoneId(slotId);
        int[] iArr = this.mInsertSimState;
        int i = iArr[slotId];
        boolean isVSimSkipUpdateUserPref = true;
        if (i != -99) {
            if (!(i == -3 || i == -2)) {
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
            logd("updateSubActivation, Received all sim info, update user pref subs, triggerDds= " + sTriggerDds);
            if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || ((!HwVSimUtils.isVSimDsdsVersionOne() || !HwVSimUtils.isVSimEnabled()) && !HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload())) {
                isVSimSkipUpdateUserPref = false;
            }
            if (isVSimSkipUpdateUserPref) {
                sendDefaultChangedBroadcastForVsim(subCtrlr);
                logd("updateSubActivation, vsim skip updateUserPreferences");
            } else {
                HwTelephonyFactory.getHwUiccManager().updateUserPreferences(sTriggerDds);
            }
            sTriggerDds = false;
        }
    }

    private void sendDefaultChangedBroadcastForVsim(SubscriptionController subCtrlr) {
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
            int userPrefDefaultSubId = subCtrlr.getSubIdUsingPhoneId(Settings.Global.getInt(this.mContext.getContentResolver(), "user_default_sub", 0));
            logd("sendDefaultChangedBroadcastForVsim subid = " + userPrefDefaultSubId);
            subCtrlr.sendDefaultChangedBroadcast(userPrefDefaultSubId);
        } else {
            logd("sendDefaultChangedBroadcastForVsim do nothing");
        }
    }

    private boolean handleNewSimCard(SubscriptionController subCtrlr, int subId, int slotId) {
        if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || (!HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload())) {
            logd("slot[" + slotId + "] sim has changed, should activate it.");
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
        logd("vsim caused sim load, skip it.");
        return false;
    }

    private boolean handleNotChangeSimCard(SubscriptionController subCtrlr, int subId, int slotId) {
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
        mNwModeUpdated = true;
    }

    public void setUiccSubscription(int slotId, int subStatus) {
        int appType;
        int numApplication;
        int appType2;
        logd("setUiccSubscription: slotId:" + slotId + ", subStatus:" + subStatus);
        if (SubscriptionManagerEx.isValidSlotIndex(slotId)) {
            UiccCardExt uiccCard = UiccControllerExt.getInstance().getUiccCard(slotId);
            int i = 1;
            if (!HuaweiTelephonyConfigs.isQcomPlatform()) {
                Message msgSetUiccSubDone = Message.obtain(this, 1, slotId, subStatus);
                if ((TelephonyManager.MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration()) || SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", false)) {
                    this.mNewSubState[slotId] = this.INVALID_VALUE;
                    this.mNeedResetSub[slotId] = false;
                    PhoneFactory.getPhone(slotId).setRadioPower(subStatus != 0, msgSetUiccSubDone);
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
                PhoneFactory.getSubInfoRecordUpdater().resetIccid(slotId);
                Message msgSetUiccSubDone2 = Message.obtain(this, 1, slotId, subStatus);
                AsyncResult.forMessage(msgSetUiccSubDone2, false, CommandException.fromRilErrno(2));
                msgSetUiccSubDone2.sendToTarget();
            } else if (!IS_SETUICCSUB_BY_SLOT) {
                UiccProfileEx uiccProfileEx = uiccCard.getUiccProfile();
                int numApplication2 = uiccProfileEx.getNumApplications();
                boolean set3GPP2Done = false;
                boolean set3GPPDone = false;
                int i2 = 0;
                while (i2 < numApplication2) {
                    if (uiccProfileEx.getApplicationIndex(i2) != null) {
                        appType = uiccProfileEx.getApplicationIndex(i2).getType();
                    } else {
                        appType = 0;
                    }
                    if (set3GPPDone) {
                        appType2 = appType;
                        numApplication = numApplication2;
                    } else if (appType == 2 || appType == 1) {
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
                        appType2 = appType;
                        numApplication = numApplication2;
                    }
                    if (!set3GPP2Done && (appType2 == 4 || appType2 == 3)) {
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
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        AsyncResult ar = (AsyncResult) msg.obj;
        int slotId = msg.arg1;
        int newSubState = msg.arg2;
        boolean isVSimSkipUpdateUserPref = true;
        if (ar.exception != null) {
            loge("Exception in SET_UICC_SUBSCRIPTION, slotId = " + slotId + " newSubState " + newSubState);
            this.mSubStatus[slotId] = -100;
            if ((ar.exception instanceof CommandException) && ar.exception.getCommandError() == CommandException.Error.RADIO_NOT_AVAILABLE) {
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
            logd("Received all subs, now update user preferred subs, slotid = " + slotId + " newSubState = " + newSubState + " sTriggerDds = " + sTriggerDds);
            if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || ((!HwVSimUtils.isVSimDsdsVersionOne() || !HwVSimUtils.isVSimEnabled()) && !HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload())) {
                isVSimSkipUpdateUserPref = false;
            }
            if (isVSimSkipUpdateUserPref) {
                logd("vsim skip updateUserPreferences");
            } else {
                HwTelephonyFactory.getHwUiccManager().updateUserPreferences(sTriggerDds);
            }
            if (sTriggerDds && !HuaweiTelephonyConfigs.isHisiPlatform()) {
                HwTelephonyFactory.getHwUiccManager().updateDataSlot();
            }
            sTriggerDds = false;
        }
        this.mNewSubState[slotId] = this.INVALID_VALUE;
        this.mNeedResetSub[slotId] = false;
    }

    private void processSimRefresh(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("processSimRefresh received without input");
            return;
        }
        Integer index = (Integer) ar.userObj;
        IccRefreshResponse state = (IccRefreshResponse) ar.result;
        logi(" Received SIM refresh, reset sub state " + index + " old sub state " + this.mSubStatus[index.intValue()] + " refreshResult = " + state.refreshResult);
        if (state.refreshResult == 2) {
            this.mSubStatus[index.intValue()] = SUB_SIM_REFRESH;
        }
    }

    private void broadcastSetUiccResult(int slotId, int newSubState, int result) {
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        intent.addFlags(16777216);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, slotId);
        intent.putExtra("operationResult", result);
        intent.putExtra("newSubState", newSubState);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean isAllSubsAvailable() {
        boolean allSubsAvailable = PhoneFactory.getSubscriptionInfoUpdater().isAllIccIdQueryDoneHw();
        for (int i = 0; i < sNumPhones; i++) {
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
        for (int i = 0; i < sNumPhones; i++) {
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
        return this.mOldIccId;
    }

    public int getNewCardCount() {
        return this.mNewCardCount;
    }

    public int getNewSimStatus() {
        return this.mNewSimStatus;
    }

    public int[] getInsertSimState() {
        return this.mInsertSimState;
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
}
