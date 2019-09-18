package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.process.HwVSimProcessor;
import java.util.Arrays;

public class HwVSimTriModem extends HwVSimModemAdapter {
    private static final String LOG_TAG = "HwVSimTriModem";
    private static final Object mLock = new Object();
    private static HwVSimTriModem sModem;

    public static HwVSimTriModem create(HwVSimController vsimController, Context context, CommandsInterface vsimCi, CommandsInterface[] cis) {
        HwVSimTriModem hwVSimTriModem;
        synchronized (mLock) {
            if (sModem == null) {
                sModem = new HwVSimTriModem(vsimController, context, vsimCi, cis);
                hwVSimTriModem = sModem;
            } else {
                throw new RuntimeException("TriModemController already created");
            }
        }
        return hwVSimTriModem;
    }

    public static HwVSimTriModem getInstance() {
        HwVSimTriModem hwVSimTriModem;
        synchronized (mLock) {
            if (sModem != null) {
                hwVSimTriModem = sModem;
            } else {
                throw new RuntimeException("TriModemController not yet created");
            }
        }
        return hwVSimTriModem;
    }

    private HwVSimTriModem(HwVSimController vsimController, Context context, CommandsInterface vsimCi, CommandsInterface[] cis) {
        super(vsimController, context, vsimCi, cis);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    public void onRadioPowerOffDone(HwVSimProcessor processor, AsyncResult ar) {
        if (processor != null && ar != null) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            if (request != null) {
                int subId = request.mSubId;
                logd("onRadioPowerOffDone : subId = " + subId);
                int subCount = request.getSubCount();
                for (int i = 0; i < subCount; i++) {
                    if (subId == request.getSubIdByIndex(i)) {
                        request.setPowerOnOffMark(i, false);
                    }
                }
                getSimState(processor, request, subId);
            }
        }
    }

    public void onGetSimSlotDone(HwVSimProcessor processor, AsyncResult ar) {
        boolean isVSimOnM0;
        int mainSlot;
        HwVSimRequest request = (HwVSimRequest) ar.userObj;
        int subId = request.mSubId;
        logd("onGetSimSlotDone : subId = " + subId);
        if (ar.exception == null && ar.result != null && ((int[]) ar.result).length == 3) {
            int[] slots = (int[]) ar.result;
            logd("onGetSimSlotDone : result = " + Arrays.toString(slots));
            setSimSlotTable(slots);
            if (slots[0] == 0 && slots[1] == 1 && slots[2] == 2) {
                mainSlot = 0;
                isVSimOnM0 = false;
            } else if (slots[0] == 1 && slots[1] == 0 && slots[2] == 2) {
                mainSlot = 1;
                isVSimOnM0 = false;
            } else if (slots[0] == 2 && slots[1] == 1 && slots[2] == 0) {
                mainSlot = 0;
                isVSimOnM0 = true;
            } else if (slots[0] == 2 && slots[1] == 0 && slots[2] == 1) {
                mainSlot = 1;
                isVSimOnM0 = true;
            } else {
                isVSimOnM0 = false;
                mainSlot = request.getMainSlot();
            }
            logd("onGetSimSlotDone : mainSlot = " + mainSlot);
            logd("onGetSimSlotDone : isVSimOnM0 = " + isVSimOnM0);
            HwVSimPhoneFactory.setPropPersistRadioSimSlotCfg(slots);
            request.setMainSlot(mainSlot);
            request.setIsVSimOnM0(isVSimOnM0);
            request.setGotSimSlotMark(true);
            return;
        }
        processor.doProcessException(ar, request);
    }

    public HwVSimModemAdapter.SimStateInfo onGetSimStateDone(HwVSimProcessor processor, AsyncResult ar) {
        int subId = ((HwVSimRequest) ar.userObj).mSubId;
        int simIndex = 1;
        if (subId == 2) {
            simIndex = 11;
        }
        int simEnable = 0;
        int simSub = 0;
        int simNetinfo = 0;
        if (ar.exception == null && ar.result != null && ((int[]) ar.result).length > 3) {
            simIndex = ((int[]) ar.result)[0];
            simEnable = ((int[]) ar.result)[1];
            simSub = ((int[]) ar.result)[2];
            simNetinfo = ((int[]) ar.result)[3];
        }
        logd("onGetSimStateDone : subId = " + subId + ", simIndex = " + simIndex + ", simEnable = " + simEnable + ", simNetinfo = " + simNetinfo);
        return new HwVSimModemAdapter.SimStateInfo(simIndex, simEnable, simSub, simNetinfo);
    }

    public void onQueryCardTypeDone(HwVSimProcessor processor, AsyncResult ar) {
        HwVSimRequest request = (HwVSimRequest) ar.userObj;
        int subId = request.mSubId;
        request.setCardType(subId, ((int[]) ar.result)[0] & 15);
        request.setGotCardType(subId, true);
        logd("onQueryCardTypeDone : subId = " + subId);
        if (isGotAllCardTypes(request)) {
            request.logCardTypes();
            this.mVSimController.updateCardTypes(request.getCardTypes());
        }
    }

    public void onSwitchCommrilDone(HwVSimProcessor processor, AsyncResult ar) {
        HwVSimRequest request = (HwVSimRequest) ar.userObj;
        int subId = request.mSubId;
        logd("onSwitchCommrilDone : subId = " + subId);
        int mainSlot = request.getMainSlot();
        int expectSlot = request.getExpectSlot();
        if (!(mainSlot == expectSlot || expectSlot == -1)) {
            logd("onSwitchCommrilDone : adjust mainSlot to " + expectSlot);
            request.setMainSlot(expectSlot);
        }
        processor.setProcessType(HwVSimController.ProcessType.PROCESS_TYPE_SWAP);
        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 1);
    }

    public void onSwitchSlotDone(HwVSimProcessor processor, AsyncResult ar) {
        HwVSimRequest request = (HwVSimRequest) ar.userObj;
        int[] slots = request.getSlots();
        if (slots == null) {
            processor.doProcessException(null, request);
            return;
        }
        setSimSlotTable(slots);
        HwVSimPhoneFactory.setPropPersistRadioSimSlotCfg(slots);
    }

    public void getModemSupportVSimVersion(HwVSimProcessor processor, int subId) {
        Message onCompleted = processor.obtainMessage(73, null);
        logd("start to get modem support vsim version.");
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.getModemSupportVSimVersion(onCompleted);
        }
    }

    public void onGetModemSupportVSimVersionDone(HwVSimProcessor processor, AsyncResult ar) {
        if (processor == null || ar == null) {
            logd("onGetModemSupportVSimVersionDone, param is null !");
            return;
        }
        int modemVer = parseModemSupportVSimVersionResult(processor, ar);
        this.mVSimController.setModemVSimVersion(modemVer);
        logd("modem support vsim version is: " + modemVer);
    }

    public void getModemSupportVSimVersionInner(HwVSimProcessor processor, HwVSimRequest request) {
        Message onCompleted = processor.obtainMessage(30, request);
        logd("start to get modem support vsim version for inner.");
        CommandsInterface ci = getCiBySub(request.mSubId);
        if (ci != null) {
            ci.getModemSupportVSimVersion(onCompleted);
        }
    }

    public boolean isNeedRadioOnM2() {
        if (HwVSimUtilsInner.isVSimDsdsVersionOne()) {
            logd("isVSimDsdsVersionOne,M2 no need set radio_power On");
            return false;
        }
        int insertedCardCount = this.mVSimController.getInsertedCardCount();
        logd("insertedCardCount = " + insertedCardCount);
        if (insertedCardCount == 0 || insertedCardCount == 1) {
            logd("m2 no need to radio on");
            return false;
        }
        logd("m2 need to radio on");
        return true;
    }

    public void onSetNetworkRatAndSrvdomainDone(HwVSimProcessor processor, AsyncResult ar) {
    }

    public void checkEnableSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        HwVSimModemAdapter.ExpectPara expectPara;
        HwVSimProcessor hwVSimProcessor = processor;
        HwVSimRequest hwVSimRequest = request;
        if (hwVSimRequest != null) {
            int[] cardTypes = request.getCardTypes();
            if (cardTypes != null && cardTypes.length != 0) {
                boolean[] isCardPresent = HwVSimUtilsInner.getCardState(cardTypes);
                int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
                logd("Enable: inserted card count = " + insertedCardCount);
                HwVSimEventReport.VSimEventInfoUtils.setCardPresent(this.mVSimController.mEventInfo, this.mVSimController.getCardPresentNumeric(isCardPresent));
                HwVSimSlotSwitchController.CommrilMode currentCommrilMode = getCommrilMode();
                logd("Enable: currentCommrilMode = " + currentCommrilMode);
                int mainSlot = request.getMainSlot();
                logd("Enable: mainSlot = " + mainSlot);
                HwVSimSlotSwitchController.CommrilMode assumedCommrilMode = getAssumedCommrilMode(mainSlot, cardTypes);
                int savedMainSlot = getVSimSavedMainSlot();
                logd("Enable: savedMainSlot = " + savedMainSlot);
                if (savedMainSlot == -1) {
                    setVSimSavedMainSlot(mainSlot);
                }
                if (insertedCardCount == 0) {
                    expectPara = getExpectParaCheckEnableNoSim(hwVSimRequest, assumedCommrilMode);
                } else if (insertedCardCount == 1) {
                    expectPara = getExpectParaCheckEnableOneSim(hwVSimRequest, assumedCommrilMode);
                } else {
                    int reservedSub = this.mVSimController.getUserReservedSubId();
                    if (HwVSimUtilsInner.isVSimDsdsVersionOne() && reservedSub == -1) {
                        logd("Enable: reserved sub not set");
                        hwVSimProcessor.notifyResult(hwVSimRequest, 7);
                        hwVSimProcessor.transitionToState(0);
                        return;
                    } else if (HwVSimUtilsInner.isDualImsSupported()) {
                        expectPara = getExpectParaCheckEnableOrSwitch(hwVSimRequest);
                    } else {
                        expectPara = getExpectParaCheckEnableTwoSimULG(hwVSimRequest, assumedCommrilMode);
                    }
                }
                HwVSimModemAdapter.ExpectPara expectPara2 = expectPara;
                HwVSimSlotSwitchController.CommrilMode expectCommrilMode = expectPara2.getExpectCommrilMode();
                int expectSlot = expectPara2.getExpectSlot();
                setAlternativeUserReservedSubId(insertedCardCount, expectSlot);
                HwVSimModemAdapter.ExpectPara expectPara3 = expectPara2;
                int i = savedMainSlot;
                processAfterCheckEnableCondition(hwVSimProcessor, hwVSimRequest, expectCommrilMode, expectSlot, currentCommrilMode);
            }
        }
    }

    private HwVSimModemAdapter.ExpectPara getExpectParaCheckEnableOrSwitch(HwVSimRequest request) {
        int expectSlot;
        HwVSimModemAdapter.ExpectPara expectPara = new HwVSimModemAdapter.ExpectPara();
        int mainSlot = request.getMainSlot();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        int[] cardTypes = request.getCardTypes();
        boolean isVSimOn = request.getIsVSimOnM0();
        HwVSimController.EnableParam param = this.mVSimController.getEnableParam(request);
        if (param == null || !HwVSimUtilsInner.isValidSlotId(param.cardInModem1)) {
            boolean isSwapFirst = HwVSimUtilsInner.isPlatformNeedWaitNvMatchUnsol();
            logd("getExpectParaCheckEnableOrSwitch isSwapFirst = " + isSwapFirst);
            if (isSwapFirst) {
                expectSlot = mainSlot;
                if (this.mVSimController.getSubState(slaveSlot) == 0 && this.mVSimController.getSubState(mainSlot) != 0) {
                    logd("getExpectParaCheckEnableOrSwitch, slot in m1 is inactive, so move to m2.");
                    expectSlot = slaveSlot;
                }
            } else {
                expectSlot = isVSimOn ? mainSlot : slaveSlot;
            }
        } else {
            logd("getExpectParaCheckEnableOrSwitch param.cardInModem1 = " + param.cardInModem1);
            expectSlot = HwVSimUtilsInner.getAnotherSlotId(param.cardInModem1);
        }
        int expectSlot2 = expectSlot;
        logd("getExpectParaCheckEnableOrSwitch isVSimOn = " + isVSimOn + " expectSlot = " + expectSlot2);
        HwVSimSlotSwitchController.CommrilMode expectCommrilMode = HwVSimSlotSwitchController.getInstance().getVSimOnCommrilMode(expectSlot2, cardTypes);
        expectPara.setExpectSlot(expectSlot2);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckEnableOrSwitch expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot2);
        return expectPara;
    }

    private HwVSimModemAdapter.ExpectPara getExpectParaCheckEnableNoSim(HwVSimRequest request, HwVSimSlotSwitchController.CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckEnableNoSim assumedCommrilMode = " + assumedCommrilMode);
        HwVSimModemAdapter.ExpectPara expectPara = new HwVSimModemAdapter.ExpectPara();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        HwVSimSlotSwitchController.CommrilMode expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.NON_MODE;
        if (HwVSimSlotSwitchController.CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (HwVSimUtilsInner.isChinaTelecom()) {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
            } else {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
            }
        } else if (HwVSimSlotSwitchController.CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
        } else if (HwVSimSlotSwitchController.CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
        }
        int expectSlot = isVSimOnM0 ? mainSlot : slaveSlot;
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckEnableNoSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private HwVSimModemAdapter.ExpectPara getExpectParaCheckEnableOneSim(HwVSimRequest request, HwVSimSlotSwitchController.CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckEnableOneSim assumedCommrilMode = " + assumedCommrilMode);
        HwVSimModemAdapter.ExpectPara expectPara = new HwVSimModemAdapter.ExpectPara();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        HwVSimSlotSwitchController.CommrilMode expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.NON_MODE;
        int expectSlot = -1;
        boolean[] isCardPresent = HwVSimUtilsInner.getCardState(cardTypes);
        if (HwVSimSlotSwitchController.CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
            expectSlot = slaveSlot;
            if (HwVSimUtilsInner.isChinaTelecom() && isVSimOnM0) {
                expectSlot = mainSlot;
            }
        } else if (HwVSimSlotSwitchController.CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
            expectSlot = mainSlot;
        } else if (HwVSimSlotSwitchController.CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
            expectSlot = (isCardPresent == null || !isCardPresent[mainSlot]) ? mainSlot : slaveSlot;
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckEnableOneSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private HwVSimModemAdapter.ExpectPara getExpectParaCheckEnableTwoSimULG(HwVSimRequest request, HwVSimSlotSwitchController.CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckEnableTwoSimULG assumedCommrilMode = " + assumedCommrilMode);
        HwVSimModemAdapter.ExpectPara expectPara = new HwVSimModemAdapter.ExpectPara();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int reservedSub = this.mVSimController.getUserReservedSubId();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        HwVSimSlotSwitchController.CommrilMode expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.NON_MODE;
        int expectSlot = -1;
        if (HwVSimSlotSwitchController.CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (HwVSimUtilsInner.isChinaTelecom() && HwVSimUtilsInner.isPlatformRealTripple()) {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
                expectSlot = isVSimOnM0 ? mainSlot : slaveSlot;
            } else if (reservedSub == mainSlot) {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            } else if (reservedSub == slaveSlot) {
                if (HwVSimSlotSwitchController.isCDMACard(cardTypes[reservedSub])) {
                    expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
                } else {
                    expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
                }
                expectSlot = mainSlot;
            } else {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            }
        } else if (HwVSimSlotSwitchController.CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (reservedSub == mainSlot) {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
                expectSlot = slaveSlot;
            } else {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
                expectSlot = mainSlot;
            }
        } else if (HwVSimSlotSwitchController.CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
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

    private void processAfterCheckEnableCondition(HwVSimProcessor processor, HwVSimRequest request, HwVSimSlotSwitchController.CommrilMode expectCommrilMode, int expectSlot, HwVSimSlotSwitchController.CommrilMode currentCommrilMode) {
        logd("Enable: processAfterCheckEnableCondition");
        logd("Enable: expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot + " currentCommrilMode = " + currentCommrilMode);
        int mainSlot = request.getMainSlot();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        boolean isNeedSwitchCommrilMode = calcIsNeedSwitchCommrilMode(expectCommrilMode, currentCommrilMode);
        StringBuilder sb = new StringBuilder();
        sb.append("Enable: isNeedSwitchCommrilMode = ");
        sb.append(isNeedSwitchCommrilMode);
        logd(sb.toString());
        logd("Enable: expectSlot = " + expectSlot + " mainSlot = " + mainSlot);
        if (IS_FAST_SWITCH_SIMSLOT && isNeedSwitchCommrilMode) {
            request.setIsNeedSwitchCommrilMode(true);
            request.setExpectCommrilMode(expectCommrilMode);
        }
        if (IS_FAST_SWITCH_SIMSLOT || !isNeedSwitchCommrilMode) {
            if (expectSlot != mainSlot) {
                processor.setProcessType(HwVSimController.ProcessType.PROCESS_TYPE_CROSS);
                HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 2);
            } else if (!isVSimOnM0 || isNeedSwitchCommrilMode) {
                processor.setProcessType(HwVSimController.ProcessType.PROCESS_TYPE_SWAP);
                HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 1);
            } else {
                int[] subs = getSimSlotTable();
                if (subs.length == 0) {
                    processor.doProcessException(null, request);
                    return;
                }
                request.setSubs(subs);
                processor.setProcessType(HwVSimController.ProcessType.PROCESS_TYPE_DIRECT);
                HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 4);
            }
            processor.transitionToState(3);
        } else {
            request.setExpectSlot(expectSlot);
            switchCommrilMode(expectCommrilMode, expectSlot, mainSlot, isVSimOnM0, processor.obtainMessage(55, request));
        }
    }

    public void checkDisableSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        HwVSimSlotSwitchController.CommrilMode expectCommrilMode;
        HwVSimProcessor hwVSimProcessor = processor;
        HwVSimRequest hwVSimRequest = request;
        if (hwVSimRequest != null) {
            int[] cardTypes = request.getCardTypes();
            if (cardTypes != null && cardTypes.length != 0) {
                int savedMainSlot = getVSimSavedMainSlot();
                logd("Disable: savedMainSlot = " + savedMainSlot);
                HwVSimSlotSwitchController.CommrilMode currentCommrilMode = getCommrilMode();
                logd("Disable: currentCommrilMode = " + currentCommrilMode);
                int mainSlot = request.getMainSlot();
                logd("Disable: mainSlot = " + mainSlot);
                int expectSlot = getExpectSlotForDisable(cardTypes, mainSlot, savedMainSlot);
                HwFullNetworkManager.getInstance().saveMainCardIccId(HwFullNetworkManager.getInstance().getFullIccid(expectSlot));
                logd("Disable: expectSlot = " + expectSlot);
                hwVSimRequest.setExpectSlot(expectSlot);
                if (HwVSimUtilsInner.isChinaTelecom()) {
                    expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCLGMode();
                } else {
                    expectCommrilMode = getExpectCommrilMode(expectSlot, cardTypes);
                }
                HwVSimSlotSwitchController.CommrilMode expectCommrilMode2 = expectCommrilMode;
                logd("Disable: expectCommrilMode = " + expectCommrilMode2);
                boolean isNeedSwitchCommrilMode = calcIsNeedSwitchCommrilMode(expectCommrilMode2, currentCommrilMode);
                logd("Disable: isNeedSwitchCommrilMode = " + isNeedSwitchCommrilMode);
                hwVSimRequest.setIsNeedSwitchCommrilMode(isNeedSwitchCommrilMode);
                if (IS_FAST_SWITCH_SIMSLOT && isNeedSwitchCommrilMode) {
                    hwVSimRequest.setExpectCommrilMode(expectCommrilMode2);
                }
                if (!processor.isReadyProcess()) {
                    boolean isNeedSwitchCommrilMode2 = isNeedSwitchCommrilMode;
                    if (!IS_FAST_SWITCH_SIMSLOT && isNeedSwitchCommrilMode2) {
                        hwVSimProcessor.setProcessType(HwVSimController.ProcessType.PROCESS_TYPE_SWAP);
                        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 11);
                    } else if (expectSlot == mainSlot) {
                        hwVSimProcessor.setProcessType(HwVSimController.ProcessType.PROCESS_TYPE_SWAP);
                        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 11);
                    } else {
                        hwVSimProcessor.setProcessType(HwVSimController.ProcessType.PROCESS_TYPE_CROSS);
                        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 12);
                    }
                    hwVSimProcessor.transitionToState(6);
                } else if (IS_FAST_SWITCH_SIMSLOT || !isNeedSwitchCommrilMode) {
                    HwVSimPhoneFactory.setIsVsimEnabledProp(false);
                    getIMSI(expectSlot);
                    hwVSimProcessor.transitionToState(0);
                } else {
                    boolean z = isNeedSwitchCommrilMode;
                    switchCommrilMode(expectCommrilMode2, expectSlot, mainSlot, false, hwVSimProcessor.obtainMessage(55, hwVSimRequest));
                }
            }
        }
    }

    public void checkSwitchModeSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        HwVSimModemAdapter.ExpectPara expectPara;
        HwVSimProcessor hwVSimProcessor = processor;
        HwVSimRequest hwVSimRequest = request;
        if (hwVSimRequest != null) {
            int[] cardTypes = request.getCardTypes();
            if (cardTypes != null && cardTypes.length != 0) {
                int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
                logd("Switch mode: inserted card count = " + insertedCardCount);
                HwVSimSlotSwitchController.CommrilMode currentCommrilMode = getCommrilMode();
                logd("Switch mode: currentCommrilMode = " + currentCommrilMode);
                int mainSlot = request.getMainSlot();
                HwVSimSlotSwitchController.CommrilMode assumedCommrilMode = getAssumedCommrilMode(mainSlot, cardTypes);
                logd("Switch mode: assumedCommrilMode = " + assumedCommrilMode);
                if (!request.getIsVSimOnM0()) {
                    hwVSimProcessor.notifyResult(hwVSimRequest, false);
                    hwVSimProcessor.transitionToState(0);
                    return;
                }
                if (insertedCardCount == 0) {
                    expectPara = getExpectParaCheckSwitchNoSim(hwVSimRequest, assumedCommrilMode);
                } else if (insertedCardCount == 1) {
                    expectPara = getExpectParaCheckSwitchOneSim(hwVSimRequest, assumedCommrilMode);
                } else {
                    logd("Switch mode: two sim, mainSlot = " + mainSlot);
                    int reservedSub = getSwtichModeUserReservedSubId(hwVSimRequest);
                    if (HwVSimUtilsInner.isVSimDsdsVersionOne() && reservedSub == -1) {
                        logd("Switch mode: reserved sub not set");
                        hwVSimProcessor.notifyResult(hwVSimRequest, false);
                        hwVSimProcessor.transitionToState(0);
                        return;
                    } else if (HwVSimUtilsInner.isDualImsSupported()) {
                        expectPara = getExpectParaCheckEnableOrSwitch(hwVSimRequest);
                    } else {
                        expectPara = getExpectParaCheckSwitchTwoSimULG(hwVSimRequest, assumedCommrilMode);
                    }
                }
                HwVSimModemAdapter.ExpectPara expectPara2 = expectPara;
                HwVSimModemAdapter.ExpectPara expectPara3 = expectPara2;
                processAfterCheckSwitchCondition(hwVSimProcessor, hwVSimRequest, expectPara2.getExpectCommrilMode(), expectPara2.getExpectSlot(), currentCommrilMode);
            }
        }
    }

    private HwVSimModemAdapter.ExpectPara getExpectParaCheckSwitchNoSim(HwVSimRequest request, HwVSimSlotSwitchController.CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckSwitchNoSim assumedCommrilMode = " + assumedCommrilMode);
        HwVSimModemAdapter.ExpectPara expectPara = new HwVSimModemAdapter.ExpectPara();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        HwVSimSlotSwitchController.CommrilMode expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.NON_MODE;
        int expectSlot = -1;
        if (HwVSimSlotSwitchController.CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (HwVSimUtilsInner.isChinaTelecom()) {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            } else {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
                expectSlot = mainSlot;
            }
        } else if (HwVSimSlotSwitchController.CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
            expectSlot = mainSlot;
        } else if (HwVSimSlotSwitchController.CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
            expectSlot = mainSlot;
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckSwitchNoSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private HwVSimModemAdapter.ExpectPara getExpectParaCheckSwitchOneSim(HwVSimRequest request, HwVSimSlotSwitchController.CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckSwitchOneSim assumedCommrilMode = " + assumedCommrilMode);
        HwVSimModemAdapter.ExpectPara expectPara = new HwVSimModemAdapter.ExpectPara();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        HwVSimSlotSwitchController.CommrilMode expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.NON_MODE;
        int expectSlot = -1;
        boolean[] isCardPresent = HwVSimUtilsInner.getCardState(cardTypes);
        if (HwVSimSlotSwitchController.CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
            expectSlot = slaveSlot;
        } else if (HwVSimSlotSwitchController.CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
            expectSlot = mainSlot;
        } else if (HwVSimSlotSwitchController.CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
            if (isCardPresent == null || !isCardPresent[mainSlot]) {
                expectSlot = mainSlot;
            } else {
                expectSlot = slaveSlot;
            }
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckSwitchOneSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private HwVSimModemAdapter.ExpectPara getExpectParaCheckSwitchTwoSimULG(HwVSimRequest request, HwVSimSlotSwitchController.CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckSwitchTwoSimULG assumedCommrilMode = " + assumedCommrilMode);
        HwVSimModemAdapter.ExpectPara expectPara = new HwVSimModemAdapter.ExpectPara();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int reservedSub = this.mVSimController.getUserReservedSubId();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        HwVSimSlotSwitchController.CommrilMode expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.NON_MODE;
        int expectSlot = -1;
        if (HwVSimSlotSwitchController.CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (HwVSimUtilsInner.isChinaTelecom()) {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            } else if (reservedSub == mainSlot) {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            } else if (reservedSub == slaveSlot) {
                if (HwVSimSlotSwitchController.isCDMACard(cardTypes[reservedSub])) {
                    expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
                } else {
                    expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
                }
                expectSlot = mainSlot;
            } else {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            }
        } else if (HwVSimSlotSwitchController.CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (reservedSub == mainSlot) {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
                expectSlot = slaveSlot;
            } else {
                expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCGMode();
                expectSlot = mainSlot;
            }
        } else if (HwVSimSlotSwitchController.CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = HwVSimSlotSwitchController.CommrilMode.getULGMode();
            if (reservedSub == mainSlot) {
                expectSlot = slaveSlot;
            } else {
                expectSlot = mainSlot;
            }
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckSwitchTwoSimULG expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private void processAfterCheckSwitchCondition(HwVSimProcessor processor, HwVSimRequest request, HwVSimSlotSwitchController.CommrilMode expectCommrilMode, int expectSlot, HwVSimSlotSwitchController.CommrilMode currentCommrilMode) {
        logd("Switch mode: processAfterCheckSwitchCondition");
        logd("Switch mode: expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot + " currentCommrilMode = " + currentCommrilMode);
        int mainSlot = request.getMainSlot();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        boolean isNeedSwitchCommrilMode = calcIsNeedSwitchCommrilMode(expectCommrilMode, currentCommrilMode);
        StringBuilder sb = new StringBuilder();
        sb.append("Switch mode: isNeedSwitchCommrilMode = ");
        sb.append(isNeedSwitchCommrilMode);
        logd(sb.toString());
        logd("Switch mode: expectSlot = " + expectSlot + " mainSlot = " + mainSlot);
        if (expectSlot != mainSlot || isNeedSwitchCommrilMode) {
            if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
                this.mVSimController.updateSubState(0, 1);
                this.mVSimController.updateSubState(1, 1);
            }
            if (IS_FAST_SWITCH_SIMSLOT && isNeedSwitchCommrilMode) {
                request.setIsNeedSwitchCommrilMode(true);
                request.setExpectCommrilMode(expectCommrilMode);
            }
            if (IS_FAST_SWITCH_SIMSLOT || !isNeedSwitchCommrilMode) {
                if (expectSlot != mainSlot) {
                    processor.setProcessType(HwVSimController.ProcessType.PROCESS_TYPE_CROSS);
                } else {
                    processor.setProcessType(HwVSimController.ProcessType.PROCESS_TYPE_SWAP);
                }
                processor.transitionToState(12);
            } else {
                request.setExpectSlot(expectSlot);
                switchCommrilMode(expectCommrilMode, expectSlot, mainSlot, isVSimOnM0, processor.obtainMessage(55, request));
            }
            return;
        }
        logd("Switch mode: no need to switch sim slot and commril mode.");
        processor.notifyResult(request, true);
        processor.transitionToState(0);
    }

    public void switchSimSlot(HwVSimProcessor processor, HwVSimRequest request) {
        int modem2;
        int modem0;
        int modem02;
        int modem22;
        int modem1;
        int modem03;
        int mainSlot = request.getMainSlot();
        boolean needSwich = true;
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        logd("switchSimSlot mainSlot = " + mainSlot + ", slaveSlot = " + slaveSlot);
        int subId = mainSlot;
        if (processor.isEnableProcess() || processor.isSwitchModeProcess()) {
            if (processor.isSwapProcess()) {
                modem02 = 2;
                modem0 = slaveSlot;
                modem22 = mainSlot;
            } else if (processor.isCrossProcess() != 0) {
                logd("expectSlot = " + slaveSlot);
                request.setExpectSlot(slaveSlot);
                modem02 = 2;
                modem0 = mainSlot;
                modem22 = slaveSlot;
            } else {
                processor.doProcessException(null, request);
                return;
            }
            modem2 = modem22;
            if (request.getIsVSimOnM0()) {
                subId = 2;
            }
        } else if (processor.isDisableProcess()) {
            int expectSlot = request.getExpectSlot();
            if (processor.isSwapProcess()) {
                modem03 = mainSlot;
                modem1 = slaveSlot;
            } else if (expectSlot == 0) {
                modem03 = 0;
                modem1 = 1;
            } else {
                modem03 = 1;
                modem1 = 0;
            }
            modem2 = 2;
            subId = 2;
            logd("switchSimSlot, main slot set to dds: " + modem03);
            SubscriptionController.getInstance().setDataSubId(modem03);
            modem02 = modem03;
            modem0 = modem1;
        } else {
            processor.doProcessException(null, request);
            return;
        }
        int modem23 = modem2;
        int[] oldSlots = getSimSlotTable();
        int[] slots = createSimSlotsTable(modem02, modem0, modem23);
        request.setSlots(slots);
        request.mSubId = subId;
        if (oldSlots.length == 3 && oldSlots[0] == slots[0] && oldSlots[1] == slots[1] && oldSlots[2] == slots[2]) {
            needSwich = false;
        }
        Message onCompleted = processor.obtainMessage(43, request);
        if (needSwich) {
            logd("switchSimSlot subId " + subId + " modem0 = " + modem02 + " modem1 = " + modem0 + " modem2 = " + modem23);
            CommandsInterface ci = getCiBySub(subId);
            if (ci != null) {
                ci.hotSwitchSimSlot(modem02, modem0, modem23, onCompleted);
            }
        } else {
            AsyncResult.forMessage(onCompleted, null, null);
            onCompleted.sendToTarget();
        }
    }

    public void setActiveModemMode(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        logd("setActiveModemMode, subId = " + subId);
        Message onCompleted = processor.obtainMessage(47, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci == null) {
            return;
        }
        if (processor.isEnableProcess()) {
            if (HwVSimUtilsInner.isPlatformRealTripple()) {
                ci.setActiveModemMode(1, onCompleted);
            } else if (processor.isSwapProcess()) {
                ci.setActiveModemMode(1, onCompleted);
                ci.setUEOperationMode(1, null);
            } else {
                ci.setActiveModemMode(0, onCompleted);
                ci.setUEOperationMode(0, null);
            }
        } else if (processor.isDisableProcess()) {
            if (!HwVSimUtilsInner.isPlatformRealTripple()) {
                ci.setActiveModemMode(0, onCompleted);
                ci.setUEOperationMode(0, null);
            } else if (this.mVSimController.getInsertedCardCount() == 1) {
                ci.setActiveModemMode(0, onCompleted);
            } else {
                ci.setActiveModemMode(1, onCompleted);
            }
        } else if (!processor.isSwitchModeProcess()) {
        } else {
            if (HwVSimUtilsInner.isPlatformRealTripple()) {
                ci.setActiveModemMode(1, onCompleted);
            } else if (processor.isSwapProcess()) {
                ci.setActiveModemMode(1, onCompleted);
                ci.setUEOperationMode(1, null);
            } else {
                ci.setActiveModemMode(0, onCompleted);
                ci.setUEOperationMode(0, null);
            }
        }
    }

    public void radioPowerOff(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor != null && request != null) {
            request.createPowerOnOffMark();
            request.createGetSimStateMark();
            request.createCardOnOffMark();
            request.createGetIccCardStatusMark();
            int subCount = request.getSubCount();
            logd("radioPowerOff:subCount = " + subCount);
            for (int i = 0; i < subCount; i++) {
                int subId = request.getSubIdByIndex(i);
                request.setPowerOnOffMark(i, true);
                request.setSimStateMark(i, true);
                request.setCardOnOffMark(i, true);
                request.setGetIccCardStatusMark(i, true);
                radioPowerOff(processor, request.clone(), subId);
            }
        }
    }

    public void getSimState(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor != null && request != null) {
            int[] subs = getSimSlotTable();
            if (subs.length != 0) {
                request.setSubs(subs);
                request.createPowerOnOffMark();
                request.createGetSimStateMark();
                request.createCardOnOffMark();
                request.createGetIccCardStatusMark();
                int subCount = request.getSubCount();
                logd("getSimState:subCount = " + subCount);
                for (int i = 0; i < subCount; i++) {
                    int subId = request.getSubIdByIndex(i);
                    request.setSimStateMark(i, true);
                    getSimState(processor, request.clone(), subId);
                }
            }
        }
    }

    public int getPoffSubForEDWork(HwVSimRequest request) {
        if (request == null) {
            return -1;
        }
        return 2;
    }

    public void onEDWorkTransitionState(HwVSimProcessor processor) {
        if (processor != null) {
            processor.transitionToState(4);
        }
    }

    public void doEnableStateEnter(HwVSimProcessor processor, HwVSimRequest request) {
    }

    public void doDisableStateExit(HwVSimProcessor processor, HwVSimRequest request) {
    }

    private int[] createSimSlotsTable(int m0, int m1, int m2) {
        int[] slots = new int[MAX_SUB_COUNT];
        slots[0] = m0;
        slots[1] = m1;
        slots[2] = m2;
        return slots;
    }

    private int getSwtichModeUserReservedSubId(HwVSimRequest request) {
        HwVSimController.WorkModeParam param = getWorkModeParam(request);
        if (param == null) {
            return -1;
        }
        switch (param.workMode) {
            case 0:
                return 0;
            case 1:
                return 1;
            default:
                return -1;
        }
    }

    private HwVSimController.WorkModeParam getWorkModeParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getWorkModeParam(request);
    }

    private boolean isGotAllCardTypes(HwVSimRequest request) {
        return request != null && request.isGotAllCardTypes();
    }

    private void setAlternativeUserReservedSubId(int cardCount, int expectSlot) {
        if (HwVSimUtilsInner.isVSimDsdsVersionOne() && -1 == this.mVSimController.getUserReservedSubId()) {
            int modem1Slot = 1;
            if (cardCount == 0 || cardCount == 1) {
                if (expectSlot != 0) {
                    modem1Slot = 0;
                }
                this.mVSimController.setAlternativeUserReservedSubId(modem1Slot);
                logd("setAlternativeUserReservedSubId: set subId is " + modem1Slot + ".");
            }
        }
    }

    public void onSimHotPlugOut() {
    }

    public void onRadioPowerOffSlaveModemDone(HwVSimProcessor processor, HwVSimRequest request) {
    }

    public void onCardPowerOffDoneInEWork(HwVSimProcessor processor, int subId) {
    }

    public int getAllAbilityNetworkTypeOnModem1(boolean duallteCapOpened) {
        if (HwVSimUtilsInner.isDualImsSupported() && duallteCapOpened) {
            return 9;
        }
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            return 3;
        }
        return 1;
    }
}
