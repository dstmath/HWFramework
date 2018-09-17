package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwAllInOneController;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimDWorkProcessor extends HwVSimWorkProcessor {
    public static final String LOG_TAG = "VSimDWorkProcessor";

    public HwVSimDWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case 2:
                onGetSimStateDone(msg);
                return true;
            case 41:
                onRadioPowerOffDone(msg);
                return true;
            case 42:
                onCardPowerOffDone(msg);
                return true;
            case 43:
                onSwitchSlotDone(msg);
                return true;
            case HwVSimConstants.EVENT_SET_TEE_DATA_READY_DONE /*44*/:
                onSetTeeDataReadyDone(msg);
                return true;
            case HwVSimConstants.EVENT_CARD_POWER_ON_DONE /*45*/:
                onCardPowerOnDone(msg);
                return true;
            case HwVSimConstants.EVENT_RADIO_POWER_ON_DONE /*46*/:
                onRadioPowerOnDone(msg);
                return true;
            case HwVSimConstants.EVENT_SET_ACTIVE_MODEM_MODE_DONE /*47*/:
                onSetActiveModemModeDone(msg);
                return true;
            case HwVSimConstants.EVENT_SET_PREFERRED_NETWORK_TYPE_DONE /*49*/:
                onSetPreferredNetworkTypeDone(msg);
                return true;
            case HwVSimConstants.EVENT_DISABLE_VSIM_DONE /*53*/:
                onDisableVSimDone(msg);
                return true;
            case HwVSimConstants.EVENT_GET_ICC_STATUS_DONE /*79*/:
                onGetIccCardStatusDone(msg);
                return true;
            case HwVSimConstants.EVENT_SET_CDMA_MODE_SIDE_DONE /*80*/:
                onSetCdmaModeSideDone(msg);
                return true;
            default:
                return false;
        }
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doDisableProcessException(ar, request);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void onCardPowerOffDone(Message msg) {
        logd("onCardPowerOffDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 4);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            int subCount = request.getSubCount();
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setCardOnOffMark(i, false);
                }
            }
            if (subId != 2) {
                logd("dispose uicc card: " + subId);
                this.mVSimController.disposeCard(subId);
            }
            getIccCardStatus(request, subId);
        }
    }

    protected void afterGetAllCardStateDone() {
        logd("afterGetAllCardStateDone");
        int expectSlot = this.mRequest.getExpectSlot();
        logd("restore user switch dual card slot to expectSlot: " + expectSlot);
        if (expectSlot != -1) {
            HwVSimPhoneFactory.setUserSwitchDualCardSlots(expectSlot);
        }
        this.mVSimController.setVSimCurCardType(-1);
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            this.mModemAdapter.setActiveModemMode(this, this.mRequest, 2);
            return;
        }
        if (this.mVSimController.clearVsimToTA() != 0) {
            logd("warning, clear vsim to TA not success");
        }
        this.mModemAdapter.setTeeDataReady(this, this.mRequest, 2);
    }

    protected void onSetTeeDataReadyDone(Message msg) {
        logd("onSetTeeDataReadyDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 6);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.setActiveModemMode(this, this.mRequest, ar.userObj.mSubId);
        }
    }

    protected void onSetActiveModemModeDone(Message msg) {
        logd("onSetActiveModemModeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 8);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            int networkMode = this.mModemAdapter.restoreSavedNetworkMode(this, 0);
            logd("onSetActiveModemModeDone, networkMode = " + networkMode + ", modemId = " + 0);
            this.mModemAdapter.setPreferredNetworkType(this, this.mRequest, ar.userObj.mSubId, networkMode);
        }
    }

    protected void onSetPreferredNetworkTypeDone(Message msg) {
        logd("onSetPreferredNetworkTypeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 10);
        if (isAsyncResultValid(msg.obj)) {
            int flag = this.mVSimController.getPreferredNetworkTypeDisableFlag();
            logd("onSetPreferredNetworkTypeDone, flag = " + flag);
            switch (flag) {
                case 1:
                    this.mModemAdapter.saveNetworkTypeToDB(HwTelephonyManagerInner.getDefault().getDefault4GSlotId(), this.mModemAdapter.restoreSavedNetworkMode(this, 0));
                    int subId = this.mRequest.getExpectSlot();
                    int slaveSlot = subId == 0 ? 1 : 0;
                    int networkMode = this.mModemAdapter.restoreSavedNetworkMode(this, 1);
                    logd("onSetPreferredNetworkTypeDone, subId = " + slaveSlot + ", networkMode = " + networkMode);
                    if (HwVSimUtilsInner.IS_CMCC_4GSWITCH_DISABLE && HwAllInOneController.getInstance().isCMCCHybird()) {
                        logd("onSetPreferredNetworkTypeDone, sub " + subId + " is not cmcc card, so set GSM_ONLY");
                        networkMode = HwTelephonyManagerInner.getDefault().isCDMASimCard(slaveSlot) ? 5 : 1;
                    }
                    this.mModemAdapter.setPreferredNetworkType(this, this.mRequest, slaveSlot, networkMode);
                    this.mVSimController.setPreferredNetworkTypeDisableFlag(2);
                    break;
                case 2:
                    if (HwVSimUtilsInner.isDualImsSupported()) {
                        this.mModemAdapter.saveNetworkTypeToDB(this.mRequest.getExpectSlot() == 0 ? 1 : 0, this.mModemAdapter.restoreSavedNetworkMode(this, 1));
                    }
                    cardPowerOn(msg);
                    this.mVSimController.setPreferredNetworkTypeDisableFlag(0);
                    break;
                default:
                    this.mModemAdapter.switchSimSlot(this, this.mRequest);
                    break;
            }
            return;
        }
        HwVSimModemAdapter hwVSimModemAdapter = this.mModemAdapter;
        if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT) {
            this.mVSimController.setPreferredNetworkTypeDisableFlag(0);
        }
    }

    protected void onSwitchSlotDone(Message msg) {
        logd("onSwitchSlotDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 5);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar) && this.mModemAdapter.isDoneAllSwitchSlot(this, ar)) {
            this.mModemAdapter.onSwitchSlotDone(this, ar);
            this.mVSimController.clearAllMarkForCardReload();
            HwVSimRequest request = ar.userObj;
            int subId = request.getMainSlot();
            this.mModemAdapter.getRadioCapability();
            HwVSimModemAdapter hwVSimModemAdapter = this.mModemAdapter;
            if (!HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT && request.getIsNeedSwitchCommrilMode()) {
                this.mVSimController.setBlockPinFlag(true);
                this.mVSimController.setBlockPinTable(subId, true);
            }
            hwVSimModemAdapter = this.mModemAdapter;
            if (HwVSimModemAdapter.IS_FAST_SWITCH_SIMSLOT && request.getIsNeedSwitchCommrilMode()) {
                setCdmaModeSide(msg);
            } else {
                restoreSavedNetworkForM0(request);
            }
        }
    }

    protected void onSetCdmaModeSideDone(Message msg) {
        logd("onSetCdmaModeSideDone");
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            HwVSimRequest request = ar.userObj;
            HwVSimController.getInstance().setIsWaitingSwitchCdmaModeSide(false);
            SystemProperties.set(HwVSimModemAdapter.PROPERTY_COMMRIL_MODE, request.getExpectCommrilMode().toString());
            this.mModemAdapter.getRadioCapability();
            restoreSavedNetworkForM0(request);
        }
    }

    private void restoreSavedNetworkForM0(HwVSimRequest request) {
        int subId = request.getExpectSlot();
        int networkMode = this.mModemAdapter.restoreSavedNetworkMode(this, 0);
        this.mVSimController.setPreferredNetworkTypeDisableFlag(1);
        this.mModemAdapter.setPreferredNetworkType(this, request, subId, networkMode);
    }

    private void cardPowerOn(Message msg) {
        HwVSimRequest request = msg.obj.userObj;
        int subId = request.getMainSlot();
        if (isCrossProcess()) {
            for (int i = 0; i < HwVSimModemAdapter.PHONE_COUNT; i++) {
                subId = request.getSubIdByIndex(i);
                request.setCardOnOffMark(i, true);
                request.setPowerOnOffMark(i, true);
                HwVSimRequest cloneRequest = request.clone();
                logd("cardPowerOn:cross subId = " + subId);
                this.mModemAdapter.cardPowerOn(this, cloneRequest, subId, 1);
                this.mVSimController.setMarkForCardReload(subId, true);
            }
        } else if (isSwapProcess()) {
            logd("cardPowerOn:swap subId = " + subId);
            this.mModemAdapter.cardPowerOn(this, this.mRequest, subId, 1);
            this.mVSimController.setMarkForCardReload(subId, true);
        }
    }

    protected void onCardPowerOnDone(Message msg) {
        logd("onCardPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 7);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidNoProcessException(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            int subCount = request.getSubCount();
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setCardOnOffMark(i, false);
                }
            }
            if (ar.exception != null) {
                this.mVSimController.setMarkForCardReload(subId, false);
            }
            this.mModemAdapter.radioPowerOn(this, request, subId);
        }
    }

    protected void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 11);
        AsyncResult ar = msg.obj;
        if (isNeedWaitNvCfgMatchAndRestartRild() || (isAsyncResultValid(ar) ^ 1) == 0) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            int subCount = request.getSubCount();
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setPowerOnOffMark(i, false);
                }
            }
            if (isAllMarkClear(request)) {
                logd("onRadioPowerOnDone:isAllMarkClear");
                disableVSimDone();
            }
        }
    }

    protected void onDisableVSimDone(Message msg) {
        logd("onDisableVSimDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 14);
        notifyResult(this.mRequest, Boolean.valueOf(true));
        transitionToState(7);
    }

    protected void disableVSimDone() {
        logd("disableVSimDone");
        Message onCompleted = obtainMessage(53, this.mRequest);
        AsyncResult.forMessage(onCompleted);
        onCompleted.sendToTarget();
    }

    protected boolean isNeedWaitNvCfgMatchAndRestartRild() {
        return HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol() && HwVSimController.getInstance().getInsertedCardCount() != 0;
    }
}
