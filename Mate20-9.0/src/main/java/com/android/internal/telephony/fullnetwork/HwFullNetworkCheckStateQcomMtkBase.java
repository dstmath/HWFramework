package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.IccCardStatus;

public abstract class HwFullNetworkCheckStateQcomMtkBase extends HwFullNetworkCheckStateBase {
    private static final String LOG_TAG = "HwFullNetworkCheckStateQcomMtkBase";
    public HwFullNetworkChipOther mChipOther;

    public HwFullNetworkCheckStateQcomMtkBase(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        this.mChipOther = null;
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        logd("HwFullNetworkCheckStateQcomMtkBase constructor");
    }

    public void handleMessage(Message msg) {
        int modemNetworkMode;
        if (msg == null || msg.obj == null) {
            loge("msg or msg.obj is null, return!");
            return;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        if (msg.what != 2102) {
            logd("Unknown msg:" + msg.what);
        } else {
            int subId = msg.arg1;
            this.mChipOther.mNumOfGetPrefNwModeSuccess++;
            if (ar.exception == null) {
                logd("subId = " + subId + " modemNetworkMode = " + modemNetworkMode);
                this.mChipOther.mModemPreferMode[subId] = modemNetworkMode;
            } else {
                logd("Failed to get preferred network mode for slot" + subId);
                this.mChipOther.mModemPreferMode[subId] = -1;
            }
            if (HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK) {
                checkNetworkTypeForSubscription(subId);
            } else if (this.mChipOther.mNumOfGetPrefNwModeSuccess == HwFullNetworkConstants.SIM_NUM) {
                handleGetPreferredNetworkForMapping();
                this.mChipOther.mNumOfGetPrefNwModeSuccess = 0;
            }
        }
    }

    public boolean checkIfAllCardsReady(Message msg) {
        logd("checkIfAllCardsReady!");
        if (HwFullNetworkConfig.USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK) {
            boolean ready = true;
            int i = 0;
            while (true) {
                if (i < HwFullNetworkConstants.SIM_NUM) {
                    if (this.mChipOther.mGetUiccCardsStatusDone[i]) {
                        if (this.mChipOther.mUiccCardsStatus != null) {
                            boolean z = true;
                            if (SubscriptionController.getInstance().getSubState(i) != 1) {
                                z = false;
                            }
                            boolean isSubActivated = z;
                            logd("i = " + i + ", isSubActivated is " + isSubActivated);
                            if (this.mChipOther.mUiccCardsStatus[i] == IccCardStatus.CardState.CARDSTATE_PRESENT && !this.mChipCommon.isCardPresent(i)) {
                                logd("mUiccCardsStatus present but not insert, wait for a moment");
                                ready = false;
                                break;
                            }
                            i++;
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
        return false;
    }

    public boolean processSimStateChanged() {
        logd("processSimStateChanged!");
        boolean isSetDefault4GNeed = false;
        int new4GSlotId = this.defaultMainSlot;
        if (1 == this.mChipOther.is4GSlotReviewNeeded) {
            logd("processSimStateChanged: auto mode!");
            isSetDefault4GNeed = judgeDefaultMainSlot();
            new4GSlotId = this.defaultMainSlot;
        } else if (2 == this.mChipOther.is4GSlotReviewNeeded) {
            logd("processSimStateChanged: fix mode!");
            this.mChipOther.judgeNwMode(this.mChipOther.mUserPref4GSlot);
            isSetDefault4GNeed = this.mChipOther.isSetDefault4GSlotNeeded(this.mChipOther.mUserPref4GSlot);
            new4GSlotId = this.mChipOther.mUserPref4GSlot;
        }
        if (isSetDefault4GNeed) {
            this.mChipOther.judgeNwMode(new4GSlotId);
            if (true == this.mChipCommon.isSet4GSlotInProgress) {
                logd("There is event in progress");
                return false;
            }
            this.mChipCommon.default4GSlot = new4GSlotId;
            this.mCheckStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_SET_MAIN_SLOT, new4GSlotId, 0).sendToTarget();
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
        if (4 == detectedType) {
            int userPref4GSlot = this.mChipCommon.getUserSwitchDualCardSlots();
            boolean set4GDefaltSlot = (userPref4GSlot < HwFullNetworkConstants.SIM_NUM && !this.mChipCommon.isSimInsertedArray[userPref4GSlot]) || userPref4GSlot >= HwFullNetworkConstants.SIM_NUM;
            boolean need4GCheckWhenBoot = HwFullNetworkConfig.IS_CT_4GSWITCH_DISABLE && this.mChipCommon.subCarrierTypeArray[0].isCTCard() != this.mChipCommon.subCarrierTypeArray[1].isCTCard();
            if (set4GDefaltSlot) {
                judgeDefaultMainSlot();
                userPref4GSlot = this.defaultMainSlot;
            } else if (need4GCheckWhenBoot && judgeDefaultMainSlot()) {
                userPref4GSlot = this.defaultMainSlot;
            }
            if (!this.mChipCommon.judgeSubCarrierTypeByMccMnc(userPref4GSlot)) {
                this.mChipOther.is4GSlotReviewNeeded = 2;
                this.mChipOther.mUserPref4GSlot = userPref4GSlot;
                this.mChipCommon.subCarrierTypeArray[userPref4GSlot] = HwFullNetworkConstants.SubCarrierType.OTHER;
                logd("userPref4GSlot=" + userPref4GSlot + " SubCarrierType=" + this.mChipCommon.subCarrierTypeArray[userPref4GSlot] + " Need to check in Sim State Change!");
                return false;
            }
            this.mChipOther.judgeNwMode(userPref4GSlot);
            if (this.mChipOther.isSetDefault4GSlotNeeded(userPref4GSlot)) {
                logd("setDefault4GSlot when networkmode change!");
                return true;
            }
            for (int sub = 0; sub < HwFullNetworkConstants.SIM_NUM; sub++) {
                if (this.mCis[sub] != null) {
                    this.mCis[sub].getPreferredNetworkType(obtainMessage(HwFullNetworkConstants.EVENT_GET_PREF_NETWORK_DONE, sub, 0));
                }
            }
            return false;
        } else if (judgeDefaultMainSlot()) {
            this.mChipOther.judgeNwMode(this.defaultMainSlot);
            if (true == this.mChipCommon.isSet4GSlotInProgress) {
                logd("There is event in progress");
                return false;
            }
            logd("Need set main slot!");
            return true;
        } else {
            logd("there is no need to set the 4G slot");
            return false;
        }
    }

    private boolean processSubStateChanged() {
        logd("processSubStateChanged!");
        if (judgeDefaultMainSlot()) {
            PhoneFactory.getSubInfoRecordUpdater().resetInsertSimState();
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

    public int getDefaultMainSlot() {
        return this.defaultMainSlot;
    }

    /* access modifiers changed from: protected */
    public void checkNetworkType() {
        for (int sub = 0; sub < HwFullNetworkConstants.SIM_NUM; sub++) {
            if (this.mCis[sub] != null) {
                this.mCis[sub].getPreferredNetworkType(obtainMessage(HwFullNetworkConstants.EVENT_GET_PREF_NETWORK_DONE, sub, 0));
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
        if (dataSub != curr4GSlot) {
            logd("handleGetPreferredNetworkForMapping dataSub = " + dataSub + " ;curr4GSlot = " + curr4GSlot);
            HwTelephonyManagerInner.getDefault().setDefaultDataSlotId(curr4GSlot);
            HwSubscriptionManager.getInstance().setUserPrefDefaultSlotId(curr4GSlot);
        }
        boolean diff = false;
        int i = 0;
        while (true) {
            if (i >= HwFullNetworkConstants.SIM_NUM) {
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
            this.mChipOther.setLteServiceAbility();
            return;
        }
        logd("handleGetPreferredNetworkForMapping PreferMode same");
    }

    /* access modifiers changed from: protected */
    public boolean judgeSetDefault4GSlotForCMCC(int cmccSlotId) {
        if (cmccSlotId == this.mChipCommon.getUserSwitchDualCardSlots()) {
            return false;
        }
        this.defaultMainSlot = cmccSlotId;
        return true;
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
