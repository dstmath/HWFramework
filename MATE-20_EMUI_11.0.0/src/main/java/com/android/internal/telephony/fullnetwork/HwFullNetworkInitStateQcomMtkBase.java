package com.android.internal.telephony.fullnetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;

public class HwFullNetworkInitStateQcomMtkBase extends HwFullNetworkInitStateBase {
    private static final String LOG_TAG = "HwFullNetworkInitStateQcomMtkBase";
    protected HwFullNetworkChipOther mChipOther;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateQcomMtkBase.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
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
            } else {
                HwFullNetworkInitStateQcomMtkBase.this.loge("action is not normal");
            }
        }
    };

    public HwFullNetworkInitStateQcomMtkBase(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkInitStateQcomMtkBase constructor");
        IntentFilter filter = new IntentFilter("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        filter.addAction("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, filter);
        if (HwFullNetworkConfigInner.USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK) {
            for (int i = 0; i < this.mCis.length; i++) {
                this.mCis[i].registerForSimHotPlug(this, (int) HwFullNetworkConstantsInner.EVENT_SIM_HOTPLUG, Integer.valueOf(i));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initParams() {
        for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
            mChipCommon.subCarrierTypeArray[i] = HwFullNetworkConstantsInner.SubCarrierType.OTHER;
            this.mChipOther.mSetUiccSubscriptionResult[i] = -1;
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase, android.os.Handler
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
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
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
    /* access modifiers changed from: public */
    private void processSubInfoRecordUpdated(Intent intent) {
        int status = intent.getIntExtra("simDetectStatus", -1);
        if (status != -1) {
            boolean isSupportLte = true;
            if (4 != status) {
                logd("processSubInfoRecordUpdated, cards in the slots are changed with status: " + status);
                this.mChipOther.mNeedSetLteServiceAbility = true;
                this.mChipOther.refreshCardState();
                mChipCommon.judgeSubCarrierType();
                this.mChipOther.is4GSlotReviewNeeded = 1;
                this.mStateHandler.obtainMessage(201, status, 0, "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED").sendToTarget();
                return;
            }
            if (!HwFullNetworkConfigInner.IS_FULL_NETWORK_SUPPORTED && !HwFullNetworkConfigInner.IS_CMCC_CU_DSDX_ENABLE && !HwFullNetworkConfigInner.isCMCCDsdxEnable()) {
                isSupportLte = false;
            }
            if (!isSupportLte) {
                return;
            }
            if (mChipCommon.isSet4GSlotInProgress) {
                logd("processSubInfoRecordUpdated: setting lte slot is in progress, ignore this event");
                return;
            }
            logd("processSubInfoRecordUpdated EXTRA_VALUE_NOCHANGE check!");
            this.mChipOther.refreshCardState();
            mChipCommon.judgeSubCarrierType();
            this.mStateHandler.obtainMessage(201, status, 0, "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED").sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processSubStateChanged(Intent intent) {
        int slotId = intent.getIntExtra("subscription", -1000);
        boolean isCmccCardStateInvalid = false;
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
            if (mChipCommon.subCarrierTypeArray[slotId].isCMCCCard() || (subState != 1 && oldSimCardTypeIsCMCCCard)) {
                isCmccCardStateInvalid = true;
            }
            if (HwFullNetworkConfigInner.isCMCCDsdxDisable() && isCmccCardStateInvalid) {
                this.mChipOther.mNeedSetLteServiceAbility = true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processSimStateChanged(Intent intent) {
        String simState = intent.getStringExtra("ss");
        int slotId = intent.getIntExtra("phone", -1000);
        if ("IMSI".equals(simState) && mChipCommon.isValidIndex(slotId)) {
            processSimStateChanged(simState, slotId);
        }
    }

    private void processSimStateChanged(String simState, int slotId) {
        boolean isSubCarrierTypeChanged = true;
        if (1 != this.mChipOther.is4GSlotReviewNeeded && 2 != this.mChipOther.is4GSlotReviewNeeded) {
            return;
        }
        if (1 != this.mCis[slotId].getRadioState()) {
            logd("processSimStateChanged radioState =" + this.mCis[slotId].getRadioState());
            this.mCis[slotId].registerForOn(this, (int) HwFullNetworkConstantsInner.EVENT_RADIO_ON_PROCESS_SIM_STATE, Integer.valueOf(slotId));
            return;
        }
        logd("processSimStateChanged: check if update main card for slot " + slotId);
        HwFullNetworkConstantsInner.SubCarrierType oldSubCarrierType = mChipCommon.subCarrierTypeArray[slotId];
        this.mChipOther.refreshCardState();
        mChipCommon.judgeSubCarrierType();
        mChipCommon.judgeSubCarrierTypeByMccMnc(slotId);
        logd("processSimStateChanged:oldSubCarrierType is " + oldSubCarrierType + ", newSubCarrierType is " + mChipCommon.subCarrierTypeArray[slotId]);
        if (oldSubCarrierType == mChipCommon.subCarrierTypeArray[slotId] || mChipCommon.subCarrierTypeArray[slotId].isReCheckFail()) {
            isSubCarrierTypeChanged = false;
        }
        if (!isSubCarrierTypeChanged) {
            logd("processSimStateChanged: no need to update main card!");
            if (this.mChipOther.is4GSlotReviewNeeded == 2 && !mChipCommon.isSet4GSlotInProgress) {
                this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_CHECK_NETWORK_TYPE).sendToTarget();
            }
        }
        this.mStateHandler.obtainMessage(201, "android.intent.action.SIM_STATE_CHANGED").sendToTarget();
    }

    private void onSimHotPlug(AsyncResultEx ar, Integer index) {
        if (ar != null && ar.getResult() != null && (ar.getResult() instanceof int[]) && ((int[]) ar.getResult()).length > 0) {
            if (HwFullNetworkConstantsInner.HotplugState.STATE_PLUG_IN.ordinal() == ((int[]) ar.getResult())[0]) {
                disposeCardStatus(index.intValue());
                setStatePlugInOrOut(index.intValue(), true);
            } else if (HwFullNetworkConstantsInner.HotplugState.STATE_PLUG_OUT.ordinal() == ((int[]) ar.getResult())[0]) {
                setStatePlugInOrOut(index.intValue(), false);
            } else {
                logd("onSimHotPlug not in or out");
            }
        }
    }

    private void setStatePlugInOrOut(int slotID, boolean in) {
        logd("setStatePlugInOrOut slotID = " + slotID + "   in=" + in);
        if (slotID >= 0 && slotID < HwFullNetworkConstantsInner.SIM_NUM) {
            this.mChipOther.mSimHotPlugIn[slotID] = in;
        }
    }

    private void disposeCardStatus(int slotID) {
        logd("disposeCardStatus slotID = " + slotID);
        if (slotID >= 0 && slotID < HwFullNetworkConstantsInner.SIM_NUM) {
            this.mChipOther.mGetUiccCardsStatusDone[slotID] = false;
            this.mChipOther.mUiccCardsStatus[slotID] = null;
        }
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void onGetIccCardStatusDone(Object ar, Integer index) {
        AsyncResultEx asyncResultEx;
        logd("onGetIccCardStatusDone on index " + index);
        if (!HwFullNetworkConfigInner.USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK || (asyncResultEx = AsyncResultEx.from(ar)) == null) {
            return;
        }
        if (asyncResultEx.getException() != null || asyncResultEx.getResult() == null) {
            loge("onGetIccCardStatusDone: occur an ex:" + asyncResultEx.getException());
        } else if (!mChipCommon.isValidIndex(index.intValue())) {
            loge("onGetIccCardStatusDone: invalid index : " + index);
        } else {
            IccCardStatusExt iccCardConstantsEx = IccCardStatusExt.from(asyncResultEx.getResult());
            if (iccCardConstantsEx != null) {
                boolean z = true;
                this.mChipOther.mGetUiccCardsStatusDone[index.intValue()] = true;
                this.mChipOther.mUiccCardsStatus[index.intValue()] = iccCardConstantsEx.getCardState();
                logd("onGetIccCardStatusDone: status = " + this.mChipOther.mUiccCardsStatus[index.intValue()] + ", index = " + index);
                int intValue = index.intValue();
                if (iccCardConstantsEx.getCardState() != IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT) {
                    z = false;
                }
                setStatePlugInOrOut(intValue, z);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
