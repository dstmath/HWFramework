package com.android.internal.telephony.fullnetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.IccCardStatus;

public class HwFullNetworkInitStateQcomMtkBase extends HwFullNetworkInitStateBase {
    private static final String LOG_TAG = "HwFullNetworkInitStateQcomMtkBase";
    protected HwFullNetworkChipOther mChipOther;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwFullNetworkInitStateQcomMtkBase.this.loge("intent is null, return");
                return;
            }
            String action = intent.getAction();
            if ("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED".equals(action)) {
                HwFullNetworkInitStateQcomMtkBase.this.processSubInfoRecordUpdated(intent);
            } else if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(action)) {
                HwFullNetworkInitStateQcomMtkBase.this.processSubStateChanged(intent);
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                HwFullNetworkInitStateQcomMtkBase.this.processSimStateChanged(intent);
            }
        }
    };

    public HwFullNetworkInitStateQcomMtkBase(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkInitStateQcomMtkBase constructor");
        IntentFilter filter = new IntentFilter("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        filter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, filter);
        if (HwFullNetworkConfig.USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK) {
            for (int i = 0; i < this.mCis.length; i++) {
                this.mCis[i].registerForSimHotPlug(this, HwFullNetworkConstants.EVENT_SIM_HOTPLUG, Integer.valueOf(i));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initParams() {
        for (int i = 0; i < HwFullNetworkConstants.SIM_NUM; i++) {
            mChipCommon.subCarrierTypeArray[i] = HwFullNetworkConstants.SubCarrierType.OTHER;
            this.mChipOther.mSetUiccSubscriptionResult[i] = -1;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v11, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: android.os.AsyncResult} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void handleMessage(Message msg) {
        if (msg == null || msg.obj == null) {
            loge("msg or msg.obj is null, return!");
            return;
        }
        Integer index = mChipCommon.getCiIndex(msg);
        if (!mChipCommon.isValidIndex(index.intValue())) {
            loge("Invalid index : " + index + " received with event " + msg.what);
            return;
        }
        AsyncResult ar = null;
        if (msg.obj instanceof AsyncResult) {
            ar = msg.obj;
        }
        int i = msg.what;
        if (i == 1008) {
            logd("Received EVENT_SIM_HOTPLUG on index " + index);
            onSimHotPlug(ar, index);
        } else if (i != 2001) {
            super.handleMessage(msg);
        } else {
            logd("EVENT_RADIO_ON_PROCESS_SIM_STATE  on index " + index);
            this.mCis[index.intValue()].unregisterForOn(this);
            processSimStateChanged("IMSI", index.intValue());
        }
    }

    /* access modifiers changed from: private */
    public void processSubInfoRecordUpdated(Intent intent) {
        int status = intent.getIntExtra("simDetectStatus", -1);
        if (status != -1) {
            boolean isSupportLte = true;
            if (4 != status) {
                logd("processSubInfoRecordUpdated, cards in the slots are changed with status: " + status);
                this.mChipOther.mNeedSetLteServiceAbility = true;
                this.mChipOther.refreshCardState();
                mChipCommon.judgeSubCarrierType();
                this.mChipOther.is4GSlotReviewNeeded = 1;
                this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT, status, 0, "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED").sendToTarget();
            } else {
                if (!HwFullNetworkConfig.IS_FULL_NETWORK_SUPPORTED && !HwFullNetworkConfig.IS_CMCC_CU_DSDX_ENABLE && !HwFullNetworkConfig.IS_CMCC_4G_DSDX_ENABLE) {
                    isSupportLte = false;
                }
                if (isSupportLte) {
                    if (mChipCommon.isSet4GSlotInProgress) {
                        logd("processSubInfoRecordUpdated: setting lte slot is in progress, ignore this event");
                        return;
                    }
                    logd("processSubInfoRecordUpdated EXTRA_VALUE_NOCHANGE check!");
                    this.mChipOther.refreshCardState();
                    mChipCommon.judgeSubCarrierType();
                    this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT, status, 0, "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED").sendToTarget();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void processSubStateChanged(Intent intent) {
        int slotId = intent.getIntExtra("subscription", -1000);
        int subState = intent.getIntExtra("intContent", 0);
        if ("sub_state".equals(intent.getStringExtra("columnName")) && mChipCommon.isValidIndex(slotId)) {
            logd("processSubStateChanged: slot Id = " + slotId + ", subState = " + subState);
            if (mChipCommon.isSet4GSlotInProgress) {
                logd("processSubStateChanged: set lte slot is in progress, ignore this event");
                return;
            }
            boolean oldSimCardTypeIsCMCCCard = mChipCommon.subCarrierTypeArray[slotId].isCMCCCard();
            logd("processSubStateChanged: oldSubCarrierType = " + mChipCommon.subCarrierTypeArray[slotId]);
            this.mChipOther.refreshCardState();
            mChipCommon.judgeSubCarrierType();
            this.mChipOther.is4GSlotReviewNeeded = 1;
            if (HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE && (mChipCommon.subCarrierTypeArray[slotId].isCMCCCard() || (subState != 1 && oldSimCardTypeIsCMCCCard))) {
                this.mChipOther.mNeedSetLteServiceAbility = true;
            }
            this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT, subState, 0, "android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE").sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void processSimStateChanged(Intent intent) {
        String simState = intent.getStringExtra("ss");
        int slotId = intent.getIntExtra("slot", -1000);
        if ("IMSI".equals(simState) && mChipCommon.isValidIndex(slotId)) {
            processSimStateChanged(simState, slotId);
        }
    }

    private void processSimStateChanged(String simState, int slotId) {
        boolean isSubCarrierTypeChanged = true;
        if (1 == this.mChipOther.is4GSlotReviewNeeded || 2 == this.mChipOther.is4GSlotReviewNeeded) {
            if (CommandsInterface.RadioState.RADIO_ON != this.mCis[slotId].getRadioState()) {
                logd("processSimStateChanged radioState =" + this.mCis[slotId].getRadioState());
                this.mCis[slotId].registerForOn(this, HwFullNetworkConstants.EVENT_RADIO_ON_PROCESS_SIM_STATE, Integer.valueOf(slotId));
                return;
            }
            logd("processSimStateChanged: check if update main card for slot " + slotId);
            HwFullNetworkConstants.SubCarrierType oldSubCarrierType = mChipCommon.subCarrierTypeArray[slotId];
            this.mChipOther.refreshCardState();
            mChipCommon.judgeSubCarrierType();
            mChipCommon.judgeSubCarrierTypeByMccMnc(slotId);
            logd("processSimStateChanged:oldSubCarrierType is " + oldSubCarrierType + ", newSubCarrierType is " + mChipCommon.subCarrierTypeArray[slotId]);
            if (oldSubCarrierType == mChipCommon.subCarrierTypeArray[slotId] || mChipCommon.subCarrierTypeArray[slotId].isReCheckFail()) {
                isSubCarrierTypeChanged = false;
            }
            if (!isSubCarrierTypeChanged) {
                logd("processSimStateChanged: no need to update main card!");
                if (2 == this.mChipOther.is4GSlotReviewNeeded && !mChipCommon.isSet4GSlotInProgress) {
                    this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_NETWORK_TYPE).sendToTarget();
                }
            }
            this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT, "android.intent.action.SIM_STATE_CHANGED").sendToTarget();
        }
    }

    private void onSimHotPlug(AsyncResult ar, Integer index) {
        if (ar != null && ar.result != null && (ar.result instanceof int[]) && ((int[]) ar.result).length > 0 && HwFullNetworkConstants.HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) ar.result)[0]) {
            disposeCardStatus(index.intValue());
        }
    }

    private void disposeCardStatus(int slotID) {
        logd("disposeCardStatus slotID = " + slotID);
        if (slotID >= 0 && slotID < HwFullNetworkConstants.SIM_NUM) {
            this.mChipOther.mGetUiccCardsStatusDone[slotID] = false;
            this.mChipOther.mUiccCardsStatus[slotID] = null;
        }
    }

    public void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        logd("onGetIccCardStatusDone on index " + index);
        if (HwFullNetworkConfig.USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK) {
            if (ar.exception != null || ar.result == null) {
                loge("onGetIccCardStatusDone: occur an ex:" + ar.exception);
            } else if (!mChipCommon.isValidIndex(index.intValue())) {
                loge("onGetIccCardStatusDone: invalid index : " + index);
            } else {
                if (ar.result instanceof IccCardStatus) {
                    this.mChipOther.mGetUiccCardsStatusDone[index.intValue()] = true;
                    this.mChipOther.mUiccCardsStatus[index.intValue()] = ((IccCardStatus) ar.result).mCardState;
                    logd("onGetIccCardStatusDone: status = " + this.mChipOther.mUiccCardsStatus[index.intValue()] + ", index = " + index);
                }
            }
        }
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
