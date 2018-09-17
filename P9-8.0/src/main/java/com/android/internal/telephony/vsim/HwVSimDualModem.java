package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.vsim.HwVSimController.ProcessType;
import com.android.internal.telephony.vsim.HwVSimModemAdapter.ExpectPara;
import com.android.internal.telephony.vsim.HwVSimModemAdapter.SimStateInfo;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController.CommrilMode;
import com.android.internal.telephony.vsim.process.HwVSimProcessor;
import java.util.Arrays;

public class HwVSimDualModem extends HwVSimModemAdapter {
    private static final String LOG_TAG = "DualModemController";
    private static final Object mLock = new Object();
    private static HwVSimDualModem sModem;

    public static HwVSimDualModem create(HwVSimController vsimController, Context context, CommandsInterface vsimCi, CommandsInterface[] cis) {
        HwVSimDualModem hwVSimDualModem;
        synchronized (mLock) {
            if (sModem != null) {
                throw new RuntimeException("VSimController already created");
            }
            sModem = new HwVSimDualModem(vsimController, context, vsimCi, cis);
            hwVSimDualModem = sModem;
        }
        return hwVSimDualModem;
    }

    public static HwVSimDualModem getInstance() {
        HwVSimDualModem hwVSimDualModem;
        synchronized (mLock) {
            if (sModem == null) {
                throw new RuntimeException("VSimController not yet created");
            }
            hwVSimDualModem = sModem;
        }
        return hwVSimDualModem;
    }

    private HwVSimDualModem(HwVSimController vsimController, Context context, CommandsInterface vsimCi, CommandsInterface[] cis) {
        super(vsimController, context, vsimCi, cis);
    }

    public void onGetSimSlotDone(HwVSimProcessor processor, AsyncResult ar) {
        if (ar == null || ar.userObj == null) {
            loge("onGetSimSlotDone, param is null !");
            return;
        }
        HwVSimRequest request = ar.userObj;
        boolean isVSimOnM0 = false;
        logd("onGetSimSlotDone : subId = " + request.mSubId);
        if (ar.exception == null && ar.result != null && ((int[]) ar.result).length == 2) {
            int mainSlot;
            int[] slots = ar.result;
            int[] responseSlots = new int[3];
            CommandsInterface ci = getCiBySub(2);
            logd("onGetSimSlotDone : result = " + Arrays.toString(slots));
            if (ci != null) {
                isVSimOnM0 = ci.isRadioAvailable();
            }
            if (slots[0] == 1 && slots[1] == 2 && (isVSimOnM0 ^ 1) != 0) {
                mainSlot = 0;
                responseSlots[0] = 0;
                responseSlots[1] = 1;
                responseSlots[2] = 2;
            } else if (slots[0] == 2 && slots[1] == 1 && (isVSimOnM0 ^ 1) != 0) {
                mainSlot = 1;
                responseSlots[0] = 1;
                responseSlots[1] = 0;
                responseSlots[2] = 2;
            } else if (slots[0] == 1 && slots[1] == 2 && isVSimOnM0) {
                mainSlot = 0;
                responseSlots[0] = 2;
                responseSlots[1] = 1;
                responseSlots[2] = 0;
            } else if (slots[0] == 2 && slots[1] == 1 && isVSimOnM0) {
                mainSlot = 1;
                responseSlots[0] = 1;
                responseSlots[1] = 2;
                responseSlots[2] = 0;
            } else {
                mainSlot = 0;
                if (isVSimOnM0) {
                    responseSlots[0] = 2;
                    responseSlots[1] = 1;
                    responseSlots[2] = 0;
                } else {
                    responseSlots[0] = 0;
                    responseSlots[1] = 1;
                    responseSlots[2] = 2;
                }
                loge("[2Cards]getSimSlot fail , setMainSlot=0 ");
            }
            setSimSlotTable(responseSlots);
            logd("onGetSimSlotDone : mainSlot = " + mainSlot);
            logd("onGetSimSlotDone : isVSimOnM0 = " + isVSimOnM0);
            request.setMainSlot(mainSlot);
            request.setIsVSimOnM0(isVSimOnM0);
            request.setGotSimSlotMark(true);
        } else {
            processor.doProcessException(ar, request);
        }
    }

    public SimStateInfo onGetSimStateDone(HwVSimProcessor processor, AsyncResult ar) {
        int subId = ar.userObj.mSubId;
        int simIndex = 1;
        if (subId == 2) {
            simIndex = 11;
        }
        logd("onGetSimStateDone : subId = " + subId + ", simIndex = " + simIndex);
        int simEnable = 0;
        int simSub = 0;
        int simNetinfo = 0;
        if (ar.exception == null && ar.result != null && ((int[]) ar.result).length > 3) {
            simIndex = ((int[]) ar.result)[0];
            simEnable = ((int[]) ar.result)[1];
            simSub = ((int[]) ar.result)[2];
            simNetinfo = ((int[]) ar.result)[3];
            logd("onGetSimStateDone : simIndex= " + simIndex + ", simEnable= " + simEnable + ", simSub= " + simSub + ", simNetinfo= " + simNetinfo);
        }
        return new SimStateInfo(simIndex, simEnable, simSub, simNetinfo);
    }

    public void getAllCardTypes(HwVSimProcessor processor, HwVSimRequest request) {
        for (int i = 0; i < PHONE_COUNT; i++) {
            int subId = i;
            CommandsInterface ci = getCiBySub(i);
            if (ci == null || !ci.isRadioAvailable()) {
                request.setGotCardType(i, true);
                request.setCardType(i, 0);
            } else {
                request.setGotCardType(i, false);
                request.setCardType(i, 0);
                getCardTypes(processor, request.clone(), subId);
            }
        }
    }

    public void onQueryCardTypeDone(HwVSimProcessor processor, AsyncResult ar) {
        if (processor == null || ar == null || ar.userObj == null) {
            loge("onQueryCardTypeDone, param is null !");
            return;
        }
        HwVSimRequest request = ar.userObj;
        int subId = request.mSubId;
        request.setCardType(subId, ((int[]) ar.result)[0] & 15);
        request.setGotCardType(subId, true);
        logd("onQueryCardTypeDone : subId = " + subId);
        if (request.isGotAllCardTypes()) {
            request.logCardTypes();
        }
    }

    public void checkEnableSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
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
            CommrilMode assumedCommrilMode;
            int expectSlot;
            int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
            logd("inserted card count = " + insertedCardCount);
            CommrilMode currentCommrilMode = getCommrilMode();
            logd("currentCommrilMode = " + currentCommrilMode);
            int mainSlot = request.getMainSlot();
            CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
            if (HwVSimUtilsInner.isChinaTelecom()) {
                assumedCommrilMode = CommrilMode.CLG_MODE;
            } else {
                assumedCommrilMode = getExpectCommrilMode(mainSlot, cardTypes);
            }
            logd("assumedCommrilMode = " + assumedCommrilMode);
            int savedMainSlot = getVSimSavedMainSlot();
            logd("savedMainSlot = " + savedMainSlot);
            if (savedMainSlot == -1) {
                setVSimSavedMainSlot(mainSlot);
            }
            ExpectPara expectPara;
            if (insertedCardCount == 0) {
                expectPara = getExpectParaCheckEnableNoSim(processor, request, assumedCommrilMode);
                expectCommrilMode = expectPara.getExpectCommrilMode();
                expectSlot = expectPara.getExpectSlot();
            } else if (insertedCardCount == 1) {
                expectPara = getExpectParaCheckEnableOneSim(processor, request, assumedCommrilMode);
                expectCommrilMode = expectPara.getExpectCommrilMode();
                expectSlot = expectPara.getExpectSlot();
            } else if (this.mVSimController.getUserReservedSubId() == -1) {
                logd("Enable: reserved sub not set");
                processor.notifyResult(request, Integer.valueOf(7));
                processor.transitionToState(0);
                return;
            } else {
                expectPara = getExpectParaCheckEnableTwoSimULG(processor, request, assumedCommrilMode);
                expectCommrilMode = expectPara.getExpectCommrilMode();
                expectSlot = expectPara.getExpectSlot();
            }
            processAfterCheckEnableCondition(processor, request, expectCommrilMode, expectSlot, currentCommrilMode);
        }
    }

    private ExpectPara getExpectParaCheckEnableNoSim(HwVSimProcessor processor, HwVSimRequest request, CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckEnableNoSim assumedCommrilMode = " + assumedCommrilMode);
        ExpectPara expectPara = new ExpectPara();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int mainSlot = request.getMainSlot();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        int expectSlot = -1;
        if (assumedCommrilMode == CommrilMode.CLG_MODE) {
            if (HwVSimUtilsInner.isChinaTelecom()) {
                expectCommrilMode = CommrilMode.CG_MODE;
                if (isVSimOnM0) {
                    expectSlot = mainSlot;
                } else {
                    expectSlot = slaveSlot;
                }
            } else {
                expectCommrilMode = CommrilMode.ULG_MODE;
                expectSlot = mainSlot;
            }
        } else if (assumedCommrilMode == CommrilMode.CG_MODE) {
            expectCommrilMode = CommrilMode.ULG_MODE;
            expectSlot = mainSlot;
        } else if (assumedCommrilMode == CommrilMode.ULG_MODE) {
            expectCommrilMode = CommrilMode.ULG_MODE;
            expectSlot = mainSlot;
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckEnableNoSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private ExpectPara getExpectParaCheckEnableOneSim(HwVSimProcessor processor, HwVSimRequest request, CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckEnableOneSim assumedCommrilMode = " + assumedCommrilMode);
        ExpectPara expectPara = new ExpectPara();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        int expectSlot = -1;
        boolean[] isCardPresent = HwVSimUtilsInner.getCardState(cardTypes);
        if (assumedCommrilMode == CommrilMode.CLG_MODE) {
            expectCommrilMode = CommrilMode.CG_MODE;
            expectSlot = slaveSlot;
            if (HwVSimUtilsInner.isChinaTelecom() && isVSimOnM0) {
                expectSlot = mainSlot;
            }
        } else if (assumedCommrilMode == CommrilMode.CG_MODE) {
            expectCommrilMode = CommrilMode.CG_MODE;
            expectSlot = mainSlot;
        } else if (assumedCommrilMode == CommrilMode.ULG_MODE) {
            expectCommrilMode = CommrilMode.ULG_MODE;
            expectSlot = isCardPresent[mainSlot] ? slaveSlot : mainSlot;
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckEnableOneSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private ExpectPara getExpectParaCheckEnableTwoSimULG(HwVSimProcessor processor, HwVSimRequest request, CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckEnableTwoSimULG assumedCommrilMode = " + assumedCommrilMode);
        ExpectPara expectPara = new ExpectPara();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int mainSlot = request.getMainSlot();
        int reservedSub = this.mVSimController.getUserReservedSubId();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        int expectSlot = -1;
        if (assumedCommrilMode == CommrilMode.CLG_MODE) {
            if (HwVSimUtilsInner.isChinaTelecom()) {
                expectCommrilMode = CommrilMode.CG_MODE;
                expectSlot = slaveSlot;
                if (isVSimOnM0) {
                    expectSlot = mainSlot;
                }
            } else if (reservedSub == mainSlot) {
                expectCommrilMode = CommrilMode.CG_MODE;
                expectSlot = slaveSlot;
            } else {
                expectCommrilMode = CommrilMode.ULG_MODE;
                expectSlot = mainSlot;
            }
        } else if (assumedCommrilMode == CommrilMode.CG_MODE) {
            if (reservedSub == mainSlot) {
                expectCommrilMode = CommrilMode.ULG_MODE;
                expectSlot = slaveSlot;
            } else {
                expectCommrilMode = CommrilMode.CG_MODE;
                expectSlot = mainSlot;
            }
        } else if (assumedCommrilMode == CommrilMode.ULG_MODE) {
            expectCommrilMode = CommrilMode.ULG_MODE;
            if (reservedSub == mainSlot) {
                expectSlot = slaveSlot;
            } else {
                expectSlot = mainSlot;
            }
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckEnableTwoSimULG expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private void processAfterCheckEnableCondition(HwVSimProcessor processor, HwVSimRequest request, CommrilMode expectCommrilMode, int expectSlot, CommrilMode currentCommrilMode) {
        logd("processWhenCheckEnableCondition");
        logd("Enable: expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot + " currentCommrilMode = " + currentCommrilMode);
        int mainSlot = request.getMainSlot();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        boolean isNeedSwitchCommrilMode = calcIsNeedSwitchCommrilMode(expectCommrilMode, currentCommrilMode);
        logd("isNeedSwitchCommrilMode = " + isNeedSwitchCommrilMode);
        if (isNeedSwitchCommrilMode) {
            request.setExpectSlot(expectSlot);
            switchCommrilMode(expectCommrilMode, expectSlot, mainSlot, isVSimOnM0, processor.obtainMessage(55, request));
            return;
        }
        if (expectSlot == slaveSlot) {
            processor.setProcessType(ProcessType.PROCESS_TYPE_CROSS);
        } else {
            processor.setProcessType(ProcessType.PROCESS_TYPE_SWAP);
        }
        processor.transitionToState(3);
    }

    public void checkDisableSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        if (request != null) {
            int[] cardTypes = request.getCardTypes();
            if (cardTypes != null && cardTypes.length != 0) {
                CommrilMode expectCommrilMode;
                int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
                logd("Disable: inserted card count = " + insertedCardCount);
                int savedMainSlot = getVSimSavedMainSlot();
                logd("Disable: savedMainSlot = " + savedMainSlot);
                CommrilMode currentCommrilMode = getCommrilMode();
                logd("Disable: currentCommrilMode = " + currentCommrilMode);
                int mainSlot = request.getMainSlot();
                int slaveSlot = mainSlot == 0 ? 1 : 0;
                if (HwVSimUtilsInner.isChinaTelecom()) {
                    expectCommrilMode = CommrilMode.CLG_MODE;
                } else {
                    expectCommrilMode = getExpectCommrilMode(savedMainSlot, cardTypes);
                }
                logd("Disable: expectCommrilMode = " + expectCommrilMode);
                int expectSlot = insertedCardCount == 1 ? slaveSlot : savedMainSlot;
                boolean isNeedSwitchCommrilMode = calcIsNeedSwitchCommrilMode(expectCommrilMode, currentCommrilMode);
                logd("Disable: isNeedSwitchCommrilMode = " + isNeedSwitchCommrilMode);
                if (!processor.isReadyProcess()) {
                    if (isNeedSwitchCommrilMode || expectSlot == mainSlot) {
                        processor.setProcessType(ProcessType.PROCESS_TYPE_SWAP);
                    } else {
                        processor.setProcessType(ProcessType.PROCESS_TYPE_CROSS);
                    }
                    processor.transitionToState(6);
                } else if (isNeedSwitchCommrilMode) {
                    request.setExpectSlot(expectSlot);
                    switchCommrilMode(expectCommrilMode, expectSlot, mainSlot, false, processor.obtainMessage(55, request));
                } else {
                    HwVSimPhoneFactory.setIsVsimEnabledProp(false);
                    getIMSI(expectSlot);
                    processor.transitionToState(0);
                }
            }
        }
    }

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
            CommandsInterface ci = getCiBySub(subId);
            if (ci == null || !ci.isRadioAvailable()) {
                logd("[2cards]don't operate on vsim card!");
                request.setPowerOnOffMark(i, false);
                request.setSimStateMark(i, false);
                request.setCardOnOffMark(i, false);
                ((GsmCdmaPhone) getPhoneBySub(subId)).getServiceStateTracker().setDesiredPowerState(false);
            } else {
                request.setPowerOnOffMark(i, true);
                request.setSimStateMark(i, true);
                request.setCardOnOffMark(i, true);
                radioPowerOff(processor, request.clone(), subId);
            }
        }
    }

    public void onRadioPowerOffDone(HwVSimProcessor processor, AsyncResult ar) {
        if (processor == null || ar == null || ar.userObj == null) {
            loge("onRadioPowerOffDone, param is null !");
            return;
        }
        HwVSimRequest request = ar.userObj;
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

    public void onSwitchCommrilDone(HwVSimProcessor processor, AsyncResult ar) {
        HwVSimRequest request = ar.userObj;
        logd("onSwitchCommrilDone : subId = " + request.mSubId);
        int mainSlot = request.getMainSlot();
        int expectSlot = request.getExpectSlot();
        if (!(mainSlot == expectSlot || expectSlot == -1)) {
            logd("onSwitchCommrilDone : adjust mainSlot to " + expectSlot);
            request.setMainSlot(expectSlot);
        }
        processor.setProcessType(ProcessType.PROCESS_TYPE_SWAP);
    }

    public void switchSimSlot(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor == null || request == null) {
            loge("switchSimSlot, param is null !");
            return;
        }
        int modem0;
        int modem1;
        int modem2;
        int mainSlot = request.getMainSlot();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        int subId = mainSlot;
        boolean isSwap = processor.isSwapProcess();
        boolean isCross = processor.isCrossProcess();
        if (processor.isEnableProcess()) {
            logd("switchSimSlot, enable !");
            if (isSwap) {
                modem0 = 2;
                modem1 = slaveSlot;
                modem2 = mainSlot;
            } else if (isCross) {
                int expectSlot = slaveSlot;
                logd("expectSlot = " + expectSlot);
                request.setExpectSlot(expectSlot);
                modem0 = 2;
                modem1 = mainSlot;
                modem2 = slaveSlot;
            } else {
                processor.doProcessException(null, request);
                return;
            }
        } else if (processor.isDisableProcess()) {
            logd("switchSimSlot, disable !");
            int savedMainSlot = HwVSimPhoneFactory.getVSimSavedMainSlot();
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
            modem2 = 2;
            subId = 2;
        } else {
            processor.doProcessException(null, request);
            return;
        }
        logd("switchSimSlot, isSwap = " + isSwap);
        logd("switchSimSlot, isCross = " + isCross);
        logd("switchSimSlot, modem0 = " + modem0 + "modem1 = " + modem1 + "modem2 = " + modem2);
        request.setSlots(createSimSlotsTable(modem0, modem1, modem2));
        request.mSubId = subId;
        Message onCompletedM1 = null;
        if (processor.isDisableProcess() && isCross) {
            HwVSimRequest M1Request = request.clone();
            M1Request.mSubId = modem1;
            onCompletedM1 = processor.obtainMessage(43, M1Request);
            request.mSubId = modem0;
            request.createSwitchSlotDoneMark();
            request.setSwitchSlotDoneMark(modem0, false);
            request.setSwitchSlotDoneMark(modem1, false);
            request.setSwitchSlotDoneMark(modem2, true);
        }
        Message onCompleted = processor.obtainMessage(43, request);
        CommandsInterface ci = getCiBySub(modem1);
        if (isSwap) {
            if (ci != null) {
                ci.updateSocketMapForSlaveSub(modem0, modem1, modem2);
            }
        } else if (isCross && ci != null) {
            ci.hotSwitchSimSlotFor2Modem(modem0, modem1, modem2, onCompletedM1);
        }
        ci = getCiBySub(modem2);
        if (ci != null) {
            ci.hotSwitchSimSlotFor2Modem(modem0, modem1, modem2, null);
        }
        ci = getCiBySub(modem0);
        if (ci != null) {
            ci.hotSwitchSimSlotFor2Modem(modem0, modem1, modem2, onCompleted);
        }
    }

    public boolean isDoneAllSwitchSlot(HwVSimProcessor processor, AsyncResult ar) {
        if (processor == null || ar == null) {
            return false;
        }
        HwVSimRequest request = ar.userObj;
        if (processor.isDisableProcess() && processor.isCrossProcess()) {
            int subId = request.mSubId;
            int subCount = request.getSubCount();
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setSwitchSlotDoneMark(subId, true);
                }
            }
            if (!request.isDoneAllSwitchSlot()) {
                return false;
            }
        }
        return true;
    }

    public void onSwitchSlotDone(HwVSimProcessor processor, AsyncResult ar) {
        HwVSimRequest request = ar.userObj;
        int[] slots = request.getSlots();
        if (slots == null) {
            processor.doProcessException(null, request);
        } else {
            setSimSlotTable(slots);
        }
    }

    public void setActiveModemMode(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(47, request);
        AsyncResult.forMessage(onCompleted);
        onCompleted.sendToTarget();
    }

    public int getPoffSubForEDWork(HwVSimRequest request) {
        if (request == null) {
            return -1;
        }
        request.mSubId = 2;
        return 2;
    }

    public void getModemSupportVSimVersion(HwVSimProcessor processor, int subId) {
        Message onCompleted = processor.obtainMessage(73, null);
        logd("start to get modem support vsim version.");
        onCompleted.sendToTarget();
    }

    public void onGetModemSupportVSimVersionDone(HwVSimProcessor processor, AsyncResult ar) {
        this.mVSimController.setModemVSimVersion(-2);
        logd("modem support vsim version is: -2");
    }

    public void getModemSupportVSimVersionInner(HwVSimProcessor processor, HwVSimRequest request) {
        Message onCompleted = processor.obtainMessage(30, request);
        logd("start to get modem support vsim version for inner.");
        AsyncResult.forMessage(onCompleted).exception = new CommandException(Error.REQUEST_NOT_SUPPORTED);
        onCompleted.sendToTarget();
    }

    public boolean isNeedRadioOnM2() {
        return false;
    }

    public void onSetNetworkRatAndSrvdomainDone(HwVSimProcessor processor, AsyncResult ar) {
    }

    public void doEnableStateEnter(HwVSimProcessor processor, HwVSimRequest request) {
    }

    public void doDisableStateExit(HwVSimProcessor processor, HwVSimRequest request) {
    }

    public void onEDWorkTransitionState(HwVSimProcessor processor) {
        if (processor != null) {
            processor.transitionToState(0);
        }
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    protected void loge(String s) {
        HwVSimLog.VSimLogE(LOG_TAG, s);
    }

    protected int[] createSimSlotsTable(int m0, int m1, int m2) {
        int[] slots = new int[MAX_SUB_COUNT];
        slots[0] = m0;
        slots[1] = m1;
        slots[2] = m2;
        return slots;
    }

    private boolean calcIsNeedSwitchCommrilMode(CommrilMode expect, CommrilMode current) {
        if (expect == CommrilMode.NON_MODE || expect == current) {
            return false;
        }
        return true;
    }

    public void onReconnectGetSimSlotDone(HwVSimProcessor processor, AsyncResult ar) {
    }

    public void checkSwitchModeSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
    }

    public void getSimState(HwVSimProcessor processor, HwVSimRequest request) {
    }
}
