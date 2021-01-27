package com.huawei.internal.telephony.vsim.process;

import android.os.Message;
import android.telephony.HwTelephonyManager;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.vsim.HwVSimControllerGetter;
import com.huawei.internal.telephony.vsim.HwVSimMtkController;
import com.huawei.internal.telephony.vsim.HwVSimMtkDualModem;

public class HwVSimMtkEnableProcessor extends HwVSimMtkProcessor {
    private static final String LOG_TAG = "HwVSimMtkEnableProcessor";
    private IccCardStatusExt.CardStateEx mVSimMtkCardState = IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT;

    public HwVSimMtkEnableProcessor(HwVSimMtkController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onEnter() {
        logi("onEnter");
        this.mController.cmdSemRelease();
        HwVSimRequest request = this.mRequest;
        if (request == null) {
            transitionToState(0);
            return;
        }
        this.mController.setProcessAction(HwVSimConstants.ProcessAction.PROCESS_ACTION_ENABLE);
        mockGetSimSlot(request);
        request.createGotCardType(HwVSimModemAdapter.PHONE_COUNT);
        request.createCardTypes(HwVSimModemAdapter.PHONE_COUNT);
        this.mModemAdapter.getAllCardTypes(this, request);
        this.mController.backUpMobileDataEnableState();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logi("onExit");
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void transitionToState(int state) {
        this.mController.transitionToState(state);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public Message obtainMessage(int what, Object obj) {
        return this.mController.obtainMessage(what, obj);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i == 48) {
            onGetPreferredNetworkTypeDone(msg);
            return true;
        } else if (i == 49) {
            onSetPreferedNetworkTypeDone(msg);
            return true;
        } else if (i == 51) {
            onEnableVSimDone(msg);
            return true;
        } else if (i == 56) {
            onQueryCardTypeDone(msg);
            return true;
        } else if (i == 95) {
            onEnableExternalSimDone(msg);
            return true;
        } else if (i != 97) {
            switch (i) {
                case HwVSimConstants.EVENT_DISABLE_EXTERNAL_SIM_DONE /* 99 */:
                    onDisableExternalSimDone(msg);
                    return true;
                case 100:
                    onCmdNotifyPlugOutExternalSim(msg);
                    return true;
                case HwVSimConstants.EVENT_NOTIFY_PLUG_OUT_DONE /* 101 */:
                    onNotifyPlugOutExternalSimDone(msg);
                    return true;
                case HwVSimConstants.EVENT_SET_MAIN_SLOT_DONE /* 102 */:
                    onSetMainSlotDone(msg);
                    return true;
                default:
                    return false;
            }
        } else {
            onNotifyPlugInDone(msg);
            return true;
        }
    }

    private void mockGetSimSlot(HwVSimRequest request) {
        if (this.mModemAdapter instanceof HwVSimMtkDualModem) {
            int mainSlot = HwTelephonyManager.getDefault().getDefault4GSlotId();
            int secondarySlot = HwVSimUtilsInner.getAnotherSlotId(mainSlot);
            int vsimSlotId = HwVSimPhoneFactory.getVSimEnabledSubId();
            boolean isVsinOnModem0 = HwVSimControllerGetter.get().isVSimOn();
            int[] slots = {mainSlot, secondarySlot};
            int[] responseSlots = new int[HwVSimModemAdapter.MAX_SUB_COUNT];
            if (isVsinOnModem0) {
                responseSlots[0] = 2;
                responseSlots[1] = HwVSimUtilsInner.getAnotherSlotId(vsimSlotId);
                responseSlots[2] = vsimSlotId;
                mainSlot = vsimSlotId;
            } else {
                responseSlots[0] = slots[0];
                responseSlots[1] = slots[1];
                responseSlots[2] = 2;
            }
            ((HwVSimMtkDualModem) this.mModemAdapter).afterGetSimSlot(request, mainSlot, isVsinOnModem0, slots, responseSlots);
        }
    }

    private void onQueryCardTypeDone(Message msg) {
        logi("onQueryCardTypeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mController.mEventInfo, 1);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onQueryCardTypeDone(this, ar);
            if (this.mRequest.isGotAllCardTypes()) {
                int slotInModem0 = this.mRequest.getMainSlot();
                logi("onQueryCardTypeDone, get pref network type for subId :" + slotInModem0);
                this.mModemAdapter.getPreferredNetworkType(this, this.mRequest, slotInModem0);
            }
        }
    }

    private void onGetPreferredNetworkTypeDone(Message msg) {
        logi("onGetPreferredNetworkTypeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mController.mEventInfo, 9);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null) {
            boolean isNotSupport = false;
            if (ar.getException() != null && isRequestNotSupport(ar.getException())) {
                isNotSupport = true;
            }
            if (isAsyncResultValidForRequestNotSupport(ar)) {
                HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
                int subId = request.mSubId;
                if (subId == request.getMainSlot() || isNotSupport) {
                    int slotInModem1 = 0;
                    if (!isNotSupport) {
                        this.mModemAdapter.onGetPreferredNetworkTypeDone(ar, 0);
                    }
                    HwVSimRequest cloneRequest = request.clone();
                    if (subId == 0) {
                        slotInModem1 = 1;
                    }
                    logi("onGetPreferredNetworkTypeDone, get pref network type for subId :" + slotInModem1);
                    this.mModemAdapter.getPreferredNetworkType(this, cloneRequest, slotInModem1);
                    return;
                }
                this.mModemAdapter.onGetPreferredNetworkTypeDone(ar, 1);
                this.mModemAdapter.checkEnableSimCondition(this, this.mRequest);
            }
        }
    }

    private void onCmdNotifyPlugOutExternalSim(Message msg) {
        logi("onCmdNotifyPlugOutExternalSim");
        this.mController.sendVsimEvent(((HwVSimRequest) msg.obj).getExpectSlot(), 3, null, obtainMessage(HwVSimConstants.EVENT_NOTIFY_PLUG_OUT_DONE, this.mRequest));
    }

    private void onNotifyPlugOutExternalSimDone(Message msg) {
        logi("onNotifyPlugOutExternalSimDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        this.mController.handleMessageDone(msg.arg1);
        if (isAsyncResultValid(ar)) {
            this.mController.sendVsimEvent(((HwVSimRequest) ar.getUserObj()).getExpectSlot(), 2, null, obtainMessage(99, this.mRequest));
        }
    }

    private void onDisableExternalSimDone(Message msg) {
        logi("onDisableExternalSimDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        this.mController.handleMessageDone(msg.arg1);
        if (isAsyncResultValid(ar)) {
            this.mController.sendVsimEvent(((HwVSimRequest) ar.getUserObj()).getExpectSlot(), 1, null, obtainMessage(95, this.mRequest));
        }
    }

    private void onEnableExternalSimDone(Message msg) {
        logi("onEnableExternalSimDone");
        Message response = obtainMessage(HwVSimConstants.EVENT_SET_MAIN_SLOT_DONE, this.mRequest);
        this.mController.handleMessageDone(msg.arg1);
        if (isAsyncResultValid(AsyncResultEx.from(msg.obj))) {
            int slotId = this.mRequest.getExpectSlot();
            if (this.mRequest.getMainSlot() != slotId) {
                logi("start set main slot to " + slotId);
                HwFullNetworkManager.getInstance().setMainSlot(slotId, response);
                return;
            }
            logi("no need to set main slot.");
            setDefaultDataSlotForMainSlot();
            setPrefNetworkForM0(null, this.mRequest, slotId);
        }
    }

    private void onSetMainSlotDone(Message msg) {
        logi("onSetMainSlotDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            setDefaultDataSlotForMainSlot();
            setPrefNetworkForM0(ar, this.mRequest, this.mRequest.getExpectSlot());
        }
    }

    private void setPrefNetworkForM0(AsyncResultEx ar, HwVSimRequest request, int slotId) {
        HwVSimConstants.EnableParam param = this.mController.getEnableParam(request);
        if (param == null) {
            doEnableProcessException(ar, request, 3);
            return;
        }
        int networkMode = calcNetworkModeByAcqorder(param.acqOrder);
        logi("set m0 preferred network to " + networkMode);
        this.mModemAdapter.setPreferredNetworkType(this, request, slotId, networkMode);
    }

    private void onSetPreferedNetworkTypeDone(Message msg) {
        logi("onSetPreferedNetworkTypeDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mController.mEventInfo, 10);
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            int flag = this.mController.getPreferredNetworkTypeEnableFlag();
            logi("onSetPreferedNetworkTypeDone, flag = " + flag);
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            int slotIdInModem0 = request.getExpectSlot();
            HwVSimConstants.EnableParam enableParam = (HwVSimConstants.EnableParam) request.getArgument();
            if (enableParam == null) {
                doProcessException(null, null);
                return;
            }
            int slotIdInModem1 = HwVSimUtilsInner.getAnotherSlotId(slotIdInModem0);
            int networkTypeInModem1 = this.mModemAdapter.getAllAbilityNetworkTypeOnModem1(true);
            if (flag == 0) {
                logi("set " + slotIdInModem1 + " networkType to " + networkTypeInModem1);
                this.mModemAdapter.setPreferredNetworkType(this, request, slotIdInModem1, networkTypeInModem1);
                this.mController.setPreferredNetworkTypeEnableFlag(1);
                this.mModemAdapter.saveNetworkTypeToDB(slotIdInModem0, calcNetworkModeByAcqorder(enableParam.acqOrder));
                return;
            }
            this.mController.setPreferredNetworkTypeEnableFlag(0);
            this.mModemAdapter.saveNetworkTypeToDB(slotIdInModem1, networkTypeInModem1);
            Message response = obtainMessage(97, this.mRequest);
            this.mController.sendVsimEvent(slotIdInModem0, 4, enableParam.imsi + "|" + enableParam.apnType + "|" + enableParam.taPath + "|" + enableParam.cardType + "|" + enableParam.challenge, response);
        }
    }

    private void onNotifyPlugInDone(Message msg) {
        logi("onNotifyPlugInDone");
        this.mController.handleMessageDone(msg.arg1);
        if (isAsyncResultValid(AsyncResultEx.from(msg.obj))) {
            this.mController.setVSimCurCardType(this.mController.getCardTypeFromEnableParam(this.mRequest));
            this.mController.notifyVsimPhoneSwitch(this.mRequest.getExpectSlot());
            this.mController.setIsVSimOn(true);
            enableVSimDone();
        }
    }

    private void enableVSimDone() {
        logi("enableVSimDone");
        Message onCompleted = obtainMessage(51, this.mRequest);
        AsyncResultEx.forMessage(onCompleted);
        onCompleted.sendToTarget();
    }

    private void onEnableVSimDone(Message msg) {
        logd("onEnableVSimDone");
        HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mController.mEventInfo, 12);
        notifyResult(this.mRequest, 0);
        transitionToState(0);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void doProcessException(AsyncResultEx ar, HwVSimRequest request) {
        doEnableProcessException(ar, request, 3);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.telephony.vsim.process.HwVSimMtkProcessor
    public void logi(String content) {
        HwVSimLog.VSimLogI(LOG_TAG, content);
    }
}
