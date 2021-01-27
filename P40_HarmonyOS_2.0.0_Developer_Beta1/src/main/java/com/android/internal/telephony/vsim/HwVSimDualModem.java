package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.Message;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.process.HwVSimProcessor;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.vsim.HwVSimBaseController;
import java.util.Arrays;

public class HwVSimDualModem extends HwVSimModemAdapter {
    private static final String LOG_TAG = "HwVSimDualModem";
    private static final Object mLock = new Object();
    private static HwVSimDualModem sModem;

    protected HwVSimDualModem(HwVSimBaseController vsimController, Context context, CommandsInterfaceEx vsimCi, CommandsInterfaceEx[] cis) {
        super(vsimController, context, vsimCi, cis);
    }

    public static HwVSimDualModem create(HwVSimBaseController vsimController, Context context, CommandsInterfaceEx vsimCi, CommandsInterfaceEx[] cis) {
        HwVSimDualModem hwVSimDualModem;
        synchronized (mLock) {
            if (sModem == null) {
                sModem = new HwVSimDualModem(vsimController, context, vsimCi, cis);
                hwVSimDualModem = sModem;
            } else {
                throw new RuntimeException("VSimController already created");
            }
        }
        return hwVSimDualModem;
    }

    public static HwVSimDualModem getInstance() {
        HwVSimDualModem hwVSimDualModem;
        synchronized (mLock) {
            if (sModem != null) {
                hwVSimDualModem = sModem;
            } else {
                throw new RuntimeException("VSimController not yet created");
            }
        }
        return hwVSimDualModem;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0074: APUT  
      (r9v0 'responseSlots' int[] A[D('responseSlots' int[])])
      (0 ??[int, short, byte, char])
      (wrap: int : 0x0072: AGET  (r3v13 int) = (r8v1 'slots' int[] A[D('slots' int[])]), (0 ??[int, short, byte, char]))
     */
    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void onGetSimSlotDone(HwVSimProcessor processor, AsyncResultEx ar) {
        boolean isVSimOnM0;
        int mainSlot;
        if (ar == null || ar.getUserObj() == null) {
            loge("onGetSimSlotDone, param is null !");
            return;
        }
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        logd("onGetSimSlotDone : subId = " + request.mSubId);
        if (ar.getException() == null && ar.getResult() != null && ((int[]) ar.getResult()).length == 2) {
            int[] slots = (int[]) ar.getResult();
            int[] responseSlots = new int[3];
            CommandsInterfaceEx ci = getCiBySub(2);
            logd("onGetSimSlotDone : result = " + Arrays.toString(slots));
            if (ci != null) {
                isVSimOnM0 = HwVSimUtilsInner.isRadioAvailable(2);
            } else {
                isVSimOnM0 = false;
            }
            responseSlots[0] = slots[0];
            responseSlots[1] = slots[1];
            responseSlots[2] = 2;
            if (slots[0] == 0 && slots[1] == 1 && !isVSimOnM0) {
                mainSlot = 0;
            } else if (slots[0] == 1 && slots[1] == 0 && !isVSimOnM0) {
                mainSlot = 1;
            } else if (slots[0] == 2 && slots[1] == 1 && isVSimOnM0) {
                responseSlots[2] = 0;
                mainSlot = 0;
            } else if (slots[0] == 2 && slots[1] == 0 && isVSimOnM0) {
                responseSlots[2] = 1;
                mainSlot = 1;
            } else {
                loge("[2Cards]getSimSlot fail , setMainSlot = 0");
                mainSlot = 0;
            }
            afterGetSimSlot(request, mainSlot, isVSimOnM0, slots, responseSlots);
        } else if (processor != null) {
            processor.doProcessException(ar, request);
        }
    }

    public void afterGetSimSlot(HwVSimRequest request, int mainSlot, boolean isVSimOnM0, int[] slots, int[] responseSlots) {
        setSimSlotTable(responseSlots);
        HwVSimPhoneFactory.setPropPersistRadioSimSlotCfg(slots);
        logd("afterGetSimSlot : mainSlot = " + mainSlot + ", isVSimOnM0 = " + isVSimOnM0);
        request.setMainSlot(mainSlot);
        request.setIsVSimOnM0(isVSimOnM0);
        request.setGotSimSlotMark(true);
        if (!isVSimOnM0) {
            HwVSimPhoneFactory.savePendingDeviceInfoToSP();
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public HwVSimModemAdapter.SimStateInfo onGetSimStateDone(AsyncResultEx ar) {
        if (ar == null) {
            return null;
        }
        int subId = ((HwVSimRequest) ar.getUserObj()).mSubId;
        int simIndex = 1;
        if (subId == 2) {
            simIndex = 11;
        }
        logd("onGetSimStateDone : subId = " + subId + ", simIndex = " + simIndex);
        int simEnable = 0;
        int simSub = 0;
        int simNetinfo = 0;
        if (ar.getException() == null && ar.getResult() != null && ((int[]) ar.getResult()).length > 3) {
            simIndex = ((int[]) ar.getResult())[0];
            simEnable = ((int[]) ar.getResult())[1];
            simSub = ((int[]) ar.getResult())[2];
            simNetinfo = ((int[]) ar.getResult())[3];
            logd("onGetSimStateDone : simIndex= " + simIndex + ", simEnable= " + simEnable + ", simSub= " + simSub + ", simNetinfo= " + simNetinfo);
        }
        return new HwVSimModemAdapter.SimStateInfo(simIndex, simEnable, simSub, simNetinfo);
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void getAllCardTypes(HwVSimProcessor processor, HwVSimRequest request) {
        if (!(processor == null || request == null)) {
            for (int subId = 0; subId < PHONE_COUNT; subId++) {
                if (getCiBySub(subId) == null || !HwVSimUtilsInner.isRadioAvailable(subId)) {
                    request.setGotCardType(subId, true);
                    int cardTypeBackup = HwVSimPhoneFactory.getUnReservedSubCardType();
                    if (cardTypeBackup != -1) {
                        logd("getAllCardTypes: use backup cardtype " + cardTypeBackup + " instead of 0(NO_SIM)");
                        request.setCardType(subId, cardTypeBackup);
                    } else {
                        request.setCardType(subId, 0);
                    }
                } else {
                    request.setGotCardType(subId, false);
                    request.setCardType(subId, 0);
                    getCardTypes(processor, request.clone(), subId);
                }
            }
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void onQueryCardTypeDone(HwVSimProcessor processor, AsyncResultEx ar) {
        if (processor == null || ar == null || ar.getUserObj() == null) {
            loge("onQueryCardTypeDone, param is null !");
            return;
        }
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        int subId = request.mSubId;
        request.setCardType(subId, ((int[]) ar.getResult())[0] & 15);
        request.setGotCardType(subId, true);
        logd("onQueryCardTypeDone : subId = " + subId);
        if (request.isGotAllCardTypes()) {
            request.logCardTypes();
            this.mVSimController.updateCardTypes(request.getCardTypes());
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void checkEnableSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        HwVSimModemAdapter.ExpectPara expectPara;
        if (processor == null || request == null) {
            loge("checkEnableSimCondition, param is null !");
            return;
        }
        int[] cardTypes = request.getCardTypes();
        if (cardTypes == null) {
            loge("checkEnableSimCondition, cardTypes is null !");
        } else if (cardTypes.length == 0) {
            loge("checkEnableSimCondition, cardCount == 0 !");
        } else {
            int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
            logd("Enable: inserted card count = " + insertedCardCount);
            HwVSimSlotSwitchController.CommrilMode currentCommrilMode = getCommrilMode();
            logd("Enable: currentCommrilMode = " + currentCommrilMode);
            int mainSlot = request.getMainSlot();
            logd("Enable: mainSlot = " + mainSlot);
            int savedMainSlot = getVSimSavedMainSlot();
            logd("Enable: savedMainSlot = " + savedMainSlot);
            if (savedMainSlot == -1) {
                setVSimSavedMainSlot(mainSlot);
            }
            if (insertedCardCount == 0) {
                expectPara = getExpectParaCheckEnableNoSim(request);
            } else if (insertedCardCount == 1) {
                expectPara = getExpectParaCheckEnableOneSim(request);
            } else {
                int reservedSub = this.mVSimController.getUserReservedSubId();
                if (reservedSub == -1) {
                    reservedSub = mainSlot;
                    logd("Enable: reserved sub not set, this time set to " + mainSlot);
                }
                expectPara = getExpectParaCheckEnableTwoSim(request, reservedSub);
            }
            HwVSimSlotSwitchController.CommrilMode expectCommrilMode = expectPara.getExpectCommrilMode();
            int expectSlot = expectPara.getExpectSlot();
            setAlternativeUserReservedSubId(expectSlot);
            processAfterCheckEnableCondition(processor, request, expectCommrilMode, expectSlot, currentCommrilMode);
        }
    }

    private HwVSimModemAdapter.ExpectPara getExpectParaCheckEnableNoSim(HwVSimRequest request) {
        HwVSimModemAdapter.ExpectPara expectPara = new HwVSimModemAdapter.ExpectPara();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int mainSlot = request.getMainSlot();
        int slaveSlot = HwVSimUtilsInner.getAnotherSlotId(mainSlot);
        HwVSimSlotSwitchController.CommrilMode expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
        int expectSlot = isVSimOnM0 ? mainSlot : slaveSlot;
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckEnableNoSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private HwVSimModemAdapter.ExpectPara getExpectParaCheckEnableOneSim(HwVSimRequest request) {
        HwVSimModemAdapter.ExpectPara expectPara = new HwVSimModemAdapter.ExpectPara();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int mainSlot = request.getMainSlot();
        int slaveSlot = HwVSimUtilsInner.getAnotherSlotId(mainSlot);
        HwVSimSlotSwitchController.CommrilMode expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
        int[] cardTypes = request.getCardTypes();
        int expectSlot = isVSimOnM0 ? mainSlot : slaveSlot;
        for (int i = 0; i < cardTypes.length; i++) {
            if (cardTypes[i] == 0) {
                expectSlot = i;
            }
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckEnableOneSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private HwVSimModemAdapter.ExpectPara getExpectParaCheckEnableTwoSim(HwVSimRequest request, int reservedSub) {
        HwVSimModemAdapter.ExpectPara expectPara = new HwVSimModemAdapter.ExpectPara();
        int slotInM2 = HwVSimUtilsInner.getAnotherSlotId(reservedSub);
        if (this.mVSimController.getSubState(reservedSub) == 0 && this.mVSimController.getSubState(slotInM2) != 0) {
            logd("getExpectParaCheckEnableTwoSim, slot in m1 is inactive, so move to m2.");
            slotInM2 = reservedSub;
        }
        expectPara.setExpectSlot(slotInM2);
        HwVSimSlotSwitchController.CommrilMode expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.HISI_CG_MODE;
        expectPara.setExpectCommrilMode(expectCommrilMode);
        int[] cardTypes = request.getCardTypes();
        if (slotInM2 >= 0 && slotInM2 < cardTypes.length) {
            HwVSimPhoneFactory.setUnReservedSubCardType(cardTypes[slotInM2]);
        }
        logd("getExpectParaCheckEnableTwoSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + slotInM2);
        return expectPara;
    }

    private void processAfterCheckEnableCondition(HwVSimProcessor processor, HwVSimRequest request, HwVSimSlotSwitchController.CommrilMode expectCommrilMode, int expectSlot, HwVSimSlotSwitchController.CommrilMode currentCommrilMode) {
        logd("Enable: processWhenCheckEnableCondition");
        logd("Enable: expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot + " currentCommrilMode = " + currentCommrilMode);
        boolean isNeedSwitchCommrilMode = calcIsNeedSwitchCommrilMode(expectCommrilMode, currentCommrilMode);
        StringBuilder sb = new StringBuilder();
        sb.append("Enable: isNeedSwitchCommrilMode = ");
        sb.append(isNeedSwitchCommrilMode);
        logd(sb.toString());
        if (isNeedSwitchCommrilMode) {
            request.setIsNeedSwitchCommrilMode(true);
            request.setExpectCommrilMode(expectCommrilMode);
        }
        request.setExpectSlot(expectSlot);
        if (setProcessType(processor, request)) {
            loge("setProcessType, occor an error, return");
        } else {
            processor.transitionToState(3);
        }
    }

    /* access modifiers changed from: protected */
    public boolean setProcessType(HwVSimProcessor processor, HwVSimRequest request) {
        int mainSlot = request.getMainSlot();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int expectSlot = request.getExpectSlot();
        boolean isNeedSwitchCommrilMode = request.getIsNeedSwitchCommrilMode();
        if (expectSlot != mainSlot) {
            processor.setProcessType(HwVSimConstants.ProcessType.PROCESS_TYPE_CROSS);
            HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.getVSimEventInfo(), 2);
            return false;
        } else if (!isVSimOnM0 || isNeedSwitchCommrilMode) {
            processor.setProcessType(HwVSimConstants.ProcessType.PROCESS_TYPE_SWAP);
            HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.getVSimEventInfo(), 1);
            return false;
        } else {
            int[] subs = getSimSlotTable();
            if (subs.length == 0) {
                processor.doProcessException(null, request);
                return true;
            }
            request.setSubs(subs);
            processor.setProcessType(HwVSimConstants.ProcessType.PROCESS_TYPE_DIRECT);
            HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.getVSimEventInfo(), 4);
            return false;
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void checkDisableSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        int[] cardTypes;
        if (request != null && processor != null && (cardTypes = request.getCardTypes()) != null && cardTypes.length != 0) {
            int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
            logd("Disable: inserted card count = " + insertedCardCount);
            int savedMainSlot = getVSimSavedMainSlot();
            logd("Disable: savedMainSlot = " + savedMainSlot);
            HwVSimSlotSwitchController.CommrilMode currentCommrilMode = getCommrilMode();
            logd("Disable: currentCommrilMode = " + currentCommrilMode);
            int mainSlot = request.getMainSlot();
            logd("Disable: mainSlot = " + mainSlot);
            int expectSlot = getExpectSlotForDisable(cardTypes, mainSlot, savedMainSlot);
            logd("Disable: expectSlot = " + expectSlot);
            request.setExpectSlot(expectSlot);
            HwVSimSlotSwitchController.CommrilMode expectCommrilMode = getVSimOffCommrilMode(expectSlot, cardTypes);
            logd("Disable: expectCommrilMode = " + expectCommrilMode);
            boolean isNeedSwitchCommrilMode = calcIsNeedSwitchCommrilMode(expectCommrilMode, currentCommrilMode);
            logd("Disable: isNeedSwitchCommrilMode = " + isNeedSwitchCommrilMode);
            request.setIsNeedSwitchCommrilMode(isNeedSwitchCommrilMode);
            if (IS_FAST_SWITCH_SIMSLOT && isNeedSwitchCommrilMode) {
                request.setExpectCommrilMode(expectCommrilMode);
            }
            if (processor.isReadyProcess()) {
                HwVSimPhoneFactory.setIsVsimEnabledProp(false);
                getIMSI(expectSlot);
                processor.transitionToState(0);
                return;
            }
            if (expectSlot == mainSlot) {
                processor.setProcessType(HwVSimConstants.ProcessType.PROCESS_TYPE_SWAP);
            } else {
                processor.setProcessType(HwVSimConstants.ProcessType.PROCESS_TYPE_CROSS);
            }
            processor.transitionToState(6);
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void radioPowerOff(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor == null || request == null) {
            loge("radioPowerOff, param is null !");
            return;
        }
        request.createPowerOnOffMark();
        request.createGetSimStateMark();
        request.createCardOnOffMark();
        int subCount = request.getSubCount();
        logd("onEnter subCount = " + subCount);
        for (int i = 0; i < subCount; i++) {
            int subId = request.getSubIdByIndex(i);
            if (getCiBySub(subId) == null || !HwVSimUtilsInner.isRadioAvailable(subId)) {
                logd("[2cards]don't operate card in modem2.");
                request.setPowerOnOffMark(i, false);
                request.setSimStateMark(i, false);
                request.setCardOnOffMark(i, false);
                request.setGetIccCardStatusMark(i, false);
                PhoneExt phone = getPhoneBySub(subId);
                if (phone != null) {
                    phone.getServiceStateTracker().setDesiredPowerState(false);
                }
            } else {
                request.setPowerOnOffMark(i, true);
                request.setSimStateMark(i, true);
                request.setCardOnOffMark(i, true);
                request.setGetIccCardStatusMark(i, true);
                radioPowerOff(processor, request.clone(), subId);
            }
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void onRadioPowerOffDone(HwVSimProcessor processor, AsyncResultEx ar) {
        if (processor == null || ar == null || ar.getUserObj() == null) {
            loge("onRadioPowerOffDone, param is null !");
            return;
        }
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        int subId = request.mSubId;
        logd("onRadioPowerOffDone : subId = " + subId);
        int subCount = request.getSubCount();
        for (int i = 0; i < subCount; i++) {
            if (subId == request.getSubIdByIndex(i)) {
                request.setPowerOnOffMark(i, false);
                request.setSimStateMark(i, false);
            }
        }
        int simIndex = 1;
        if (subId == 2) {
            simIndex = 11;
        }
        cardPowerOff(processor, request, subId, simIndex);
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void onSwitchCommrilDone(HwVSimProcessor processor, AsyncResultEx ar) {
        if (processor != null && ar != null) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            int subId = request.mSubId;
            logd("onSwitchCommrilDone : subId = " + subId);
            int mainSlot = request.getMainSlot();
            int expectSlot = request.getExpectSlot();
            if (!(mainSlot == expectSlot || expectSlot == -1)) {
                logd("onSwitchCommrilDone : adjust mainSlot to " + expectSlot);
                request.setMainSlot(expectSlot);
            }
            processor.setProcessType(HwVSimConstants.ProcessType.PROCESS_TYPE_SWAP);
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void switchSimSlot(HwVSimProcessor processor, HwVSimRequest request) {
        int modem0;
        int modem1;
        int modem2;
        if (processor == null || request == null) {
            loge("switchSimSlot, param is null !");
            return;
        }
        int mainSlot = request.getMainSlot();
        int slaveSlot = HwVSimUtilsInner.getAnotherSlotId(mainSlot);
        int subId = mainSlot;
        boolean isSwap = processor.isSwapProcess();
        if (processor.isEnableProcess()) {
            logd("switchSimSlot, enable!");
            modem2 = request.getExpectSlot();
            modem1 = HwVSimUtilsInner.getAnotherSlotId(modem2);
            modem0 = 2;
            if (request.getIsVSimOnM0()) {
                subId = 2;
            }
        } else if (processor.isDisableProcess()) {
            logd("switchSimSlot, disable !");
            int savedMainSlot = request.getExpectSlot();
            if (isSwap) {
                modem0 = mainSlot;
                modem1 = slaveSlot;
            } else if (savedMainSlot == 0) {
                modem0 = 0;
                modem1 = 1;
            } else {
                modem0 = 1;
                modem1 = 0;
            }
            subId = 2;
            logd("switchSimSlot, main slot set to dds: " + modem0);
            HwSubscriptionManager.getInstance().setDefaultDataSubIdToDbBySlotId(modem0);
            modem2 = 2;
        } else {
            processor.doProcessException(null, request);
            return;
        }
        int[] oldSlots = getSimSlotTable();
        int[] slots = createSimSlotsTable(modem0, modem1, modem2);
        request.setSlots(slots);
        request.mSubId = subId;
        boolean needSwich = false;
        if (!(oldSlots.length == 3 && oldSlots[0] == slots[0] && oldSlots[1] == slots[1] && oldSlots[2] == slots[2])) {
            needSwich = true;
        }
        Message onCompleted = processor.obtainMessage(43, request);
        if (needSwich) {
            logd("switchSimSlot subId " + subId + " modem0 = " + modem0 + " modem1 = " + modem1 + " modem2 = " + modem2);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null) {
                ci.hotSwitchSimSlot(modem0, modem1, modem2, onCompleted);
                return;
            }
            return;
        }
        logd("switchSimSlot return success");
        AsyncResultEx.forMessage(onCompleted, (Object) null, (Throwable) null);
        onCompleted.sendToTarget();
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void onSwitchSlotDone(HwVSimProcessor processor, AsyncResultEx ar) {
        if (processor != null && ar != null) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            int[] slots = request.getSlots();
            if (slots == null) {
                processor.doProcessException(null, request);
                return;
            }
            setSimSlotTable(slots);
            HwVSimPhoneFactory.setPropPersistRadioSimSlotCfg(slots);
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void setActiveModemMode(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            logd("setActiveModemMode, subId = " + subId);
            Message onCompleted = processor.obtainMessage(47, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci == null) {
                AsyncResultEx.forMessage(onCompleted);
                onCompleted.sendToTarget();
            } else if (processor.isEnableProcess()) {
                ci.setActiveModemMode(1, onCompleted);
            } else if (processor.isDisableProcess()) {
                if (request.getCardCount() == 1) {
                    ci.setActiveModemMode(0, onCompleted);
                } else {
                    ci.setActiveModemMode(1, onCompleted);
                }
            } else if (processor.isSwitchModeProcess()) {
                ci.setActiveModemMode(1, onCompleted);
            } else {
                AsyncResultEx.forMessage(onCompleted);
                onCompleted.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public int getPoffSubForEDWork(HwVSimRequest request) {
        if (request == null) {
            return -1;
        }
        request.mSubId = 2;
        return 2;
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public boolean isNeedRadioOnM2() {
        return false;
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void onSetNetworkRatAndSrvdomainDone(HwVSimProcessor processor, AsyncResultEx ar) {
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void doEnableStateEnter(HwVSimProcessor processor, HwVSimRequest request) {
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void doDisableStateExit(HwVSimProcessor processor, HwVSimRequest request) {
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void onEDWorkTransitionState(HwVSimProcessor processor) {
        if (processor != null) {
            processor.transitionToState(0);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String log) {
        HwVSimLog.error(LOG_TAG, log);
    }

    private int[] createSimSlotsTable(int m0, int m1, int m2) {
        int[] slots = new int[MAX_SUB_COUNT];
        slots[0] = m0;
        slots[1] = m1;
        slots[2] = m2;
        return slots;
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void checkSwitchModeSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void getSimState(HwVSimProcessor processor, HwVSimRequest request) {
    }

    /* access modifiers changed from: protected */
    public void setAlternativeUserReservedSubId(int expectSlot) {
        int slotInM1 = HwVSimUtilsInner.getAnotherSlotId(expectSlot);
        this.mVSimController.setAlternativeUserReservedSubId(slotInM1);
        logd("setAlternativeUserReservedSubId: set subId is " + slotInM1 + ".");
    }

    private HwVSimSlotSwitchController.CommrilMode getVSimOffCommrilMode(int mainSlot, int[] cardTypes) {
        HwVSimSlotSwitchController.CommrilMode vSimOffCommrilMode;
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        boolean mainSlotIsCDMACard = HwVSimSlotSwitchController.isCDMACard(cardTypes[mainSlot]);
        boolean slaveSlotIsCDMACard = HwVSimSlotSwitchController.isCDMACard(cardTypes[slaveSlot]);
        if (mainSlotIsCDMACard && slaveSlotIsCDMACard) {
            vSimOffCommrilMode = HwVSimSlotSwitchController.CommrilMode.HISI_CGUL_MODE;
        } else if (mainSlotIsCDMACard) {
            vSimOffCommrilMode = HwVSimSlotSwitchController.CommrilMode.HISI_CGUL_MODE;
        } else if (slaveSlotIsCDMACard) {
            vSimOffCommrilMode = HwVSimSlotSwitchController.CommrilMode.HISI_CG_MODE;
        } else {
            vSimOffCommrilMode = getCurrentCommrilMode();
            logd("no c-card, not change commril mode. vSimOnCommrilMode = " + vSimOffCommrilMode);
        }
        logd("getVSimOffCommrilMode: mainSlot = " + mainSlot + ", cardTypes = " + Arrays.toString(cardTypes) + ", mode = " + vSimOffCommrilMode);
        return vSimOffCommrilMode;
    }

    private HwVSimSlotSwitchController.CommrilMode getCurrentCommrilMode() {
        String mode = SystemPropertiesEx.get(HwVSimModemAdapter.PROPERTY_COMMRIL_MODE, "HISI_CGUL_MODE");
        HwVSimSlotSwitchController.CommrilMode result = HwVSimSlotSwitchController.CommrilMode.NON_MODE;
        try {
            return (HwVSimSlotSwitchController.CommrilMode) Enum.valueOf(HwVSimSlotSwitchController.CommrilMode.class, mode);
        } catch (IllegalArgumentException e) {
            logd("getCommrilMode, IllegalArgumentException, mode = " + mode);
            return result;
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void onSimHotPlugOut() {
        HwVSimPhoneFactory.setUnReservedSubCardType(-1);
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void onRadioPowerOffSlaveModemDone(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor == null || request == null || !processor.isEnableProcess()) {
            logd("onRadioPowerOffSlaveModemDone, do nothing.");
            return;
        }
        int slotIdInModem1 = request.mSubId;
        int slotIndex = slotIdInModem1 == 2 ? 11 : 1;
        logd("onRadioPowerOffSlaveModemDone, slotIdInModem1 = " + slotIdInModem1);
        cardPowerOff(slotIdInModem1, slotIndex, null);
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public void onCardPowerOffDoneInEWork(HwVSimProcessor processor, int subId) {
        if (processor != null && processor.isEnableProcess() && processor.isWorkProcess()) {
            int unReservedSlotId = HwVSimUtilsInner.getAnotherSlotId(this.mVSimController.getUserReservedSubId());
            if (subId == unReservedSlotId) {
                logd("onCardPowerOffDoneInEWork, will dispose card " + subId);
                this.mVSimController.disposeCard(unReservedSlotId);
                return;
            }
            logd("onCardPowerOffDoneInEWork, not dispose card " + subId);
        }
    }

    @Override // com.android.internal.telephony.vsim.HwVSimModemAdapter
    public int getAllAbilityNetworkTypeOnModem1(boolean duallteCapOpened) {
        if (duallteCapOpened) {
            return 9;
        }
        return 3;
    }
}
