package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManager;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;

public abstract class HwFullNetworkDefaultStateHisiBase extends HwFullNetworkDefaultStateBase {
    protected HwFullNetworkChipHisi mChipHisi = HwFullNetworkChipHisi.getInstance();

    public HwFullNetworkDefaultStateHisiBase(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkDefaultStateHisiBase constructor");
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase, android.os.Handler
    public void handleMessage(Message msg) {
        if (msg == null || msg.obj == null) {
            loge("msg or msg.obj is null, return!");
            return;
        }
        Integer index = this.mChipCommon.getCiIndex(msg);
        if (index.intValue() < 0 || index.intValue() >= this.mCis.length) {
            loge("Invalid index : " + index + " received with event " + msg.what);
            return;
        }
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        int i = msg.what;
        if (i == 1004) {
            logd("Received EVENT_ICC_GET_ATR_DONE on index " + index);
            if (ar != null && ar.getException() == null) {
                this.mChipHisi.handleIccATR((String) ar.getResult(), index);
            }
        } else if (i == 1007) {
            logd("Received EVENT_GET_CDMA_MODE_SIDE_DONE on index " + index);
            this.mChipHisi.onGetCdmaModeSideDone(ar, index);
        } else if (i != 1011) {
            super.handleMessage(msg);
        } else {
            logd("EVENT_CMCC_SET_NETWOR_DONE reveived for slot: " + msg.arg1);
            this.mChipHisi.handleSetCmccPrefNetwork(msg);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void onRadioUnavailable(Integer index) {
        logd("onRadioUnavailable, index " + index);
        this.mCis[index.intValue()].iccGetATR(obtainMessage(HwFullNetworkConstantsInner.EVENT_ICC_GET_ATR_DONE, index));
        this.mChipHisi.mRadioOns[index.intValue()] = false;
        this.mChipHisi.mSwitchTypes[index.intValue()] = -1;
        this.mChipHisi.mGetUiccCardsStatusDone[index.intValue()] = false;
        this.mChipHisi.mGetBalongSimSlotDone[index.intValue()] = false;
        this.mChipHisi.mCardTypes[index.intValue()] = -1;
        this.mChipCommon.mIccIds[index.intValue()] = null;
        if (this.mChipHisi.mHasRadioAvailable) {
            this.mChipHisi.mNvRestartRildDone = true;
        }
        this.mChipHisi.mAllCardsReady = false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void onRadioAvailable(Integer index) {
        logd("onRadioAvailable, index " + index);
        super.onRadioAvailable(index);
        boolean ready = true;
        if (HwFullNetworkConfigInner.IS_FAST_SWITCH_SIMSLOT && index.intValue() == this.mChipCommon.getUserSwitchDualCardSlots()) {
            this.mCis[index.intValue()].getCdmaModeSide(obtainMessage(HwFullNetworkConstantsInner.EVENT_GET_CDMA_MODE_SIDE_DONE, index));
        }
        this.mChipHisi.mRadioOns[index.intValue()] = true;
        int i = 0;
        while (true) {
            if (i >= HwFullNetworkConstantsInner.SIM_NUM) {
                break;
            } else if (!this.mChipHisi.mRadioOns[i]) {
                ready = false;
                logd("mRadioOns is " + this.mChipHisi.mRadioOns[i]);
                break;
            } else {
                i++;
            }
        }
        if (!ready) {
            logd("clean iccids!!");
            PhoneFactoryExt.cleanIccids();
            return;
        }
        boolean isVsimMatched = HwTelephonyManager.getDefault().isPlatformSupportVsim() && (HwVSimUtils.isVSimEnabled() || HwVSimUtils.isVSimCauseCardReload() || HwVSimUtils.isSubActivationUpdate());
        if (HwFullNetworkConfigInner.IS_HISI_DSDX && !isVsimMatched) {
            handleHisiDsdxDataService();
        }
        logd("EVENT_RADIO_AVAILABLE set isSet4GSlotInProgress to false");
        this.mChipHisi.setWaitingSwitchBalongSlot(false);
        HwFullNetworkChipHisi hwFullNetworkChipHisi = this.mChipHisi;
        hwFullNetworkChipHisi.mNvRestartRildDone = false;
        hwFullNetworkChipHisi.mHasRadioAvailable = true;
        if (!HwFullNetworkConfigInner.IS_FAST_SWITCH_SIMSLOT && this.mChipCommon.mSet4GSlotCompleteMsg != null) {
            AsyncResultEx.forMessage(this.mChipCommon.mSet4GSlotCompleteMsg, true, (Throwable) null);
            logd("Sending the mSet4GSlotCompleteMsg back!");
            this.mChipCommon.sendResponseToTarget(this.mChipCommon.mSet4GSlotCompleteMsg, 0);
            this.mChipCommon.mSet4GSlotCompleteMsg = null;
        }
        if (this.mChipHisi.mSetSdcsCompleteMsg != null) {
            AsyncResultEx.forMessage(this.mChipHisi.mSetSdcsCompleteMsg, true, (Throwable) null);
            logd("Sending the mSetSdcsCompleteMsg back!");
            this.mChipHisi.mSetSdcsCompleteMsg.sendToTarget();
            this.mChipHisi.mSetSdcsCompleteMsg = null;
        }
    }

    private void handleHisiDsdxDataService() {
        int dataSlotId = SubscriptionManagerEx.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
        int curr4GSlot = this.mChipCommon.getUserSwitchDualCardSlots();
        logd("handleHisiDsdxDataService dataSlotId:" + dataSlotId + ", currSlot:" + curr4GSlot);
        if (dataSlotId != curr4GSlot && this.mChipCommon.mSet4GSlotCompleteMsg == null) {
            if (SubscriptionControllerEx.getInstance() != null) {
                SubscriptionControllerEx.getInstance().setDefaultDataSubIdBySlotId(curr4GSlot);
            }
            logd("EVENT_RADIO_AVAILABLE set default data sub to 4G slot");
        }
        if ((this.mChipHisi.mNvRestartRildDone || this.mChipHisi.mSetSdcsCompleteMsg != null) && PhoneFactoryExt.onDataSubChange() == 0) {
            for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
                PhoneFactoryExt.resendDataAllowed(i);
                logd("EVENT_RADIO_AVAILABLE resend data allow with slot " + i);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void processPreBootCompleted() {
        logd("processPreBootCompleted");
        HwFullNetworkChipHisi hwFullNetworkChipHisi = this.mChipHisi;
        hwFullNetworkChipHisi.isPreBootCompleted = true;
        hwFullNetworkChipHisi.mAutoSwitchDualCardsSlotDone = false;
        this.mStateHandler.sendMessage(this.mStateHandler.obtainMessage(201, 0));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void setMainSlot(int slotId, Message responseMsg) {
        if (!SystemPropertiesEx.getBoolean("persist.sys.dualcards", false)) {
            loge("setMainSlot: main slot switch disabled, return failure");
            this.mChipCommon.sendResponseToTarget(responseMsg, 2);
        } else if (!this.mChipCommon.isValidIndex(slotId)) {
            loge("setDefault4GSlot: invalid slotid, return failure");
            this.mChipCommon.sendResponseToTarget(responseMsg, 2);
        } else {
            this.mChipCommon.prefer4GSlot = slotId;
            if (slotId == this.mChipCommon.getUserSwitchDualCardSlots()) {
                loge("setDefault4GSlot: the default 4G slot is already " + slotId);
                this.mChipCommon.sendResponseToTarget(responseMsg, 0);
            } else if (this.mChipCommon.isSet4GSlotInProgress) {
                loge("setDefault4GSlot: The setting is in progress, return failure");
                this.mChipCommon.sendResponseToTarget(responseMsg, 2);
            } else {
                logd("setDefault4GSlot: target slot id is: " + slotId);
                this.mChipCommon.mSet4GSlotCompleteMsg = responseMsg;
                this.mChipHisi.refreshCardState();
                this.mStateHandler.obtainMessage(202, slotId, 0, responseMsg).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void onServiceStateChangedForCMCC(Intent intent) {
        int cmccSlotId = this.mChipCommon.getCMCCCardSlotId();
        if (HwFullNetworkConfigInner.isCMCCDsdxDisable() && cmccSlotId != -1 && intent != null) {
            int slotId = intent.getIntExtra("slot", -1);
            ServiceState serviceState = ServiceStateEx.newFromBundle(intent.getExtras());
            if (slotId == cmccSlotId && this.mChipCommon.getCombinedRegState(serviceState) == 0) {
                int cmccSubId = -1;
                int[] subIds = SubscriptionManagerEx.getSubId(cmccSlotId);
                if (subIds != null && subIds.length > 0) {
                    cmccSubId = subIds[0];
                }
                boolean newRoamingState = TelephonyManagerEx.isNetworkRoaming(cmccSubId);
                boolean oldRoamingState = this.mChipCommon.getLastRoamingStateFromSP();
                logd("mPhoneStateListener cmcccSlotId = " + cmccSlotId + " oldRoamingState=" + oldRoamingState + " newRoamingState=" + newRoamingState);
                if (oldRoamingState != newRoamingState) {
                    this.mChipCommon.saveLastRoamingStateToSP(newRoamingState);
                    if (this.mChipCommon.needForceSetDefaultSlot(newRoamingState, cmccSlotId)) {
                        forceSetDefault4GSlotForCMCC(cmccSlotId);
                        return;
                    }
                    this.mChipHisi.setPrefNwForCmcc(this);
                }
            }
            if (slotId == cmccSlotId) {
                if (this.mChipCommon.mCmccSubIdOldState != 0 && this.mChipCommon.getCombinedRegState(serviceState) == 0 && !this.mChipCommon.isSet4GSlotInProgress) {
                    logd("OUT_OF_SERVICE -> IN_SERVICE, setPrefNW");
                    this.mChipHisi.setPrefNwForCmcc(this);
                }
                this.mChipCommon.mCmccSubIdOldState = this.mChipCommon.getCombinedRegState(serviceState);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void setLteServiceAbilityForQCOM(int subId, int ability, int lteOnMappingMode) {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void setServiceAbilityForQCOM(int slotId, int type, int ability, int serviceOnMappingMode) {
    }
}
