package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.SubscriptionHelper;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;

public abstract class HwFullNetworkCheckStateQcomMtkBase extends HwFullNetworkCheckStateBase {
    private static final String LOG_TAG = "HwFullNetworkCheckStateQcomMtkBase";
    public HwFullNetworkChipOther mChipOther;

    public HwFullNetworkCheckStateQcomMtkBase(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        this.mChipOther = null;
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        logd("HwFullNetworkCheckStateQcomMtkBase constructor");
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg == null || msg.obj == null) {
            loge("msg or msg.obj is null, return!");
            return;
        }
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (msg.what != 2102) {
            logd("Unknown msg:" + msg.what);
            return;
        }
        int subId = msg.arg1;
        this.mChipOther.mNumOfGetPrefNwModeSuccess++;
        if (ar.getException() == null) {
            int modemNetworkMode = ((int[]) ar.getResult())[0];
            logd("subId = " + subId + " modemNetworkMode = " + modemNetworkMode);
            this.mChipOther.mModemPreferMode[subId] = modemNetworkMode;
        } else {
            logd("Failed to get preferred network mode for slot" + subId);
            this.mChipOther.mModemPreferMode[subId] = -1;
        }
        if (HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK) {
            checkNetworkTypeForSubscription(subId);
        } else if (this.mChipOther.mNumOfGetPrefNwModeSuccess == HwFullNetworkConstantsInner.SIM_NUM) {
            handleGetPreferredNetworkForMapping();
            this.mChipOther.mNumOfGetPrefNwModeSuccess = 0;
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public boolean checkIfAllCardsReady(Message msg) {
        logd("checkIfAllCardsReady!");
        checkCMCCUnbind();
        checkDefaultMainSlotForMDMCarrier();
        if (HwFullNetworkConfigInner.USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK) {
            boolean ready = true;
            int i = 0;
            while (true) {
                if (i < HwFullNetworkConstantsInner.SIM_NUM) {
                    if (this.mChipOther.mGetUiccCardsStatusDone[i]) {
                        if (this.mChipOther.mUiccCardsStatus != null) {
                            boolean isSubActivated = true;
                            if (HwTelephonyManager.getDefault().getSubState((long) i) != 1) {
                                isSubActivated = false;
                            }
                            logd("i = " + i + ", isSubActivated is " + isSubActivated);
                            if (this.mChipOther.mUiccCardsStatus[i] != IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT || this.mChipCommon.isCardPresent(i)) {
                                if (this.mChipOther.mSimHotPlugIn[i] && !this.mChipCommon.isSimInsertedArray[i]) {
                                    logd("SimHotPlugIn but sub not active, wait for a moment");
                                    ready = false;
                                    break;
                                }
                                i++;
                            } else {
                                logd("mUiccCardsStatus present but not insert, wait for a moment");
                                ready = false;
                                break;
                            }
                        } else {
                            logd("mUiccCardsStatus[" + i + "] == null");
                            ready = false;
                            break;
                        }
                    } else {
                        logd("mGetUiccCardsStatusDone[" + i + "] == false");
                        ready = false;
                        break;
                    }
                } else {
                    break;
                }
            }
            if (!ready) {
                return false;
            }
        }
        if ("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED".equals(msg.obj)) {
            return processSubInfoRecordUpdated(msg.arg1);
        }
        if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(msg.obj)) {
            return processSubStateChanged();
        }
        if ("android.intent.action.SIM_STATE_CHANGED".equals(msg.obj)) {
            return processSimStateChanged();
        }
        logd("checkIfAllCardsReady no thing is matched.");
        return false;
    }

    public boolean processSimStateChanged() {
        logd("processSimStateChanged!");
        boolean isSetDefault4GNeed = false;
        int new4GSlotId = this.defaultMainSlot;
        if (this.mChipOther.is4GSlotReviewNeeded == 1) {
            logd("processSimStateChanged: auto mode!");
            isSetDefault4GNeed = judgeDefaultMainSlot();
            new4GSlotId = this.defaultMainSlot;
        } else if (this.mChipOther.is4GSlotReviewNeeded == 2) {
            logd("processSimStateChanged: fix mode!");
            this.mChipOther.judgeNwMode(this.mChipOther.mUserPref4GSlot);
            isSetDefault4GNeed = this.mChipOther.isSetDefault4GSlotNeeded(this.mChipOther.mUserPref4GSlot);
            new4GSlotId = this.mChipOther.mUserPref4GSlot;
        } else {
            logd("processSimStateChanged: other mode!");
        }
        if (isSetDefault4GNeed) {
            this.mChipOther.judgeNwMode(new4GSlotId);
            if (this.mChipCommon.isSet4GSlotInProgress) {
                logd("There is event in progress");
                return false;
            }
            this.mChipCommon.default4GSlot = new4GSlotId;
            this.mCheckStateHandler.obtainMessage(202, new4GSlotId, 0).sendToTarget();
            return false;
        }
        logd("processSimStateChanged: there is no need to set the 4G slot");
        if (2 == this.mChipOther.is4GSlotReviewNeeded) {
            checkNetworkType();
        }
        return false;
    }

    private boolean processSubInfoRecordUpdated(int detectedType) {
        logd("processSubInfoRecordUpdated!");
        if (4 != detectedType) {
            if (judgeDefaultMainSlot()) {
                this.mChipOther.judgeNwMode(this.defaultMainSlot);
                if (this.mChipCommon.isSet4GSlotInProgress) {
                    logd("There is event in progress");
                    return false;
                }
                logd("Need set main slot!");
                return true;
            }
            logd("there is no need to set the 4G slot");
            return false;
        } else if (SubscriptionControllerEx.getInstance().getDefaultDataSubId() != 0) {
            return processExtraValueNoChange();
        } else {
            logd("sub id is 0");
            return judgeDefaultMainSlot();
        }
    }

    private boolean processExtraValueNoChange() {
        int userPref4GSlot = this.mChipCommon.getUserSwitchDualCardSlots();
        boolean set4GDefaltSlot = (userPref4GSlot < HwFullNetworkConstantsInner.SIM_NUM && !this.mChipCommon.isSimInsertedArray[userPref4GSlot]) || userPref4GSlot >= HwFullNetworkConstantsInner.SIM_NUM;
        boolean need4GCheckWhenBoot = (HwFullNetworkConfigInner.IS_CT_4GSWITCH_DISABLE || HwTelephonyManagerInner.getDefault().getDefaultMainSlotCarrier() == 2) && this.mChipCommon.subCarrierTypeArray[0].isCTCard() != this.mChipCommon.subCarrierTypeArray[1].isCTCard();
        if (set4GDefaltSlot) {
            judgeDefaultMainSlot();
            userPref4GSlot = this.defaultMainSlot;
        } else if (!need4GCheckWhenBoot || !judgeDefaultMainSlot()) {
            logd("setDefault4GSlot do not set 4G slot.");
        } else {
            userPref4GSlot = this.defaultMainSlot;
        }
        if (!this.mChipCommon.judgeSubCarrierTypeByMccMnc(userPref4GSlot)) {
            this.mChipOther.is4GSlotReviewNeeded = 2;
            this.mChipOther.mUserPref4GSlot = userPref4GSlot;
            this.mChipCommon.subCarrierTypeArray[userPref4GSlot] = HwFullNetworkConstantsInner.SubCarrierType.OTHER;
            logd("userPref4GSlot=" + userPref4GSlot + " SubCarrierType=" + this.mChipCommon.subCarrierTypeArray[userPref4GSlot] + " Need to check in Sim State Change!");
            return false;
        }
        this.mChipOther.judgeNwMode(userPref4GSlot);
        if (this.mChipOther.isSetDefault4GSlotNeeded(userPref4GSlot)) {
            logd("setDefault4GSlot when networkmode change!");
            return true;
        }
        for (int sub = 0; sub < HwFullNetworkConstantsInner.SIM_NUM; sub++) {
            if (this.mCis[sub] != null) {
                this.mCis[sub].getPreferredNetworkType(obtainMessage(HwFullNetworkConstantsInner.EVENT_GET_PREF_NETWORK_DONE, sub, 0));
            }
        }
        return false;
    }

    private boolean processSubStateChanged() {
        logd("processSubStateChanged!");
        if (judgeDefaultMainSlot()) {
            SubscriptionHelper.getInstance().resetInsertSimState();
            this.mChipOther.judgeNwMode(this.defaultMainSlot);
            return true;
        }
        logd("there is no need to set the 4G slot");
        return false;
    }

    public boolean judgeDefaultMainSlot() {
        if (judgeDefaultMainSlotForMDM()) {
            return true;
        }
        this.defaultMainSlot = this.mOperatorBase.getDefaultMainSlot(false);
        return this.mOperatorBase.isMainSlotFound();
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public int getDefaultMainSlot() {
        return this.defaultMainSlot;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public void checkNetworkType() {
        for (int sub = 0; sub < HwFullNetworkConstantsInner.SIM_NUM; sub++) {
            if (this.mCis[sub] != null) {
                this.mCis[sub].getPreferredNetworkType(obtainMessage(HwFullNetworkConstantsInner.EVENT_GET_PREF_NETWORK_DONE, sub, 0));
            }
        }
    }

    private void checkNetworkTypeForSubscription(int subId) {
        int prefNwMode = this.mChipOther.getNetworkTypeFromDB(subId);
        if (prefNwMode != this.mChipOther.mModemPreferMode[subId]) {
            loge("preferred network mode for sub " + subId + " is " + prefNwMode);
            HwTelephonyManagerInner hwTelephonyManager = HwTelephonyManagerInner.getDefault();
            if (hwTelephonyManager != null) {
                hwTelephonyManager.setLteServiceAbility(subId, hwTelephonyManager.getLteServiceAbility(subId));
            }
        }
    }

    private void handleGetPreferredNetworkForMapping() {
        int curr4GSlot = this.mChipCommon.getUserSwitchDualCardSlots();
        int dataSub = SubscriptionManager.getDefaultDataSubscriptionId();
        if (SubscriptionManagerEx.getSlotIndex(dataSub) != curr4GSlot) {
            logd("handleGetPreferredNetworkForMapping dataSub = " + dataSub + " ;curr4GSlot = " + curr4GSlot);
            HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(curr4GSlot);
            HwSubscriptionManager.getInstance().setUserPrefDefaultSlotId(curr4GSlot);
        }
        boolean diff = false;
        int i = 0;
        while (true) {
            if (i >= HwFullNetworkConstantsInner.SIM_NUM) {
                break;
            }
            int prefNwMode = this.mChipOther.getNetworkTypeFromDB(i);
            logd("subid = " + i + " prefNwMode = " + prefNwMode);
            if (this.mChipOther.mModemPreferMode[i] != prefNwMode) {
                logd("modemprefermode is not same with prefer mode in slot = " + i);
                diff = true;
                break;
            }
            i++;
        }
        if (diff) {
            this.mChipOther.mNeedSetLteServiceAbility = true;
            this.mChipOther.setServiceAbility();
            return;
        }
        logd("handleGetPreferredNetworkForMapping PreferMode same");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public boolean judgeSetDefault4GSlotForCMCC(int cmccSlotId) {
        if (cmccSlotId == this.mChipCommon.getUserSwitchDualCardSlots()) {
            return false;
        }
        this.defaultMainSlot = cmccSlotId;
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
