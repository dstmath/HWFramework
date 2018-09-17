package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwAllInOneController;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.vsim.HwVSimController.ProcessType;
import com.android.internal.telephony.vsim.HwVSimController.WorkModeParam;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimModemAdapter.ExpectPara;
import com.android.internal.telephony.vsim.HwVSimModemAdapter.SimStateInfo;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController.CommrilMode;
import com.android.internal.telephony.vsim.process.HwVSimProcessor;
import java.util.Arrays;

public class HwVSimTriModem extends HwVSimModemAdapter {
    private static final String LOG_TAG = "HwVSimTriModem";
    private static final Object mLock = new Object();
    private static HwVSimTriModem sModem;
    private boolean mIsM0CSOnly = false;
    private boolean mIsM2CSOnly = false;
    private int mM0NetworkMode;
    private boolean mMmsOnM2 = false;

    public static HwVSimTriModem create(HwVSimController vsimController, Context context, CommandsInterface vsimCi, CommandsInterface[] cis) {
        HwVSimTriModem hwVSimTriModem;
        synchronized (mLock) {
            if (sModem != null) {
                throw new RuntimeException("TriModemController already created");
            }
            sModem = new HwVSimTriModem(vsimController, context, vsimCi, cis);
            hwVSimTriModem = sModem;
        }
        return hwVSimTriModem;
    }

    public static HwVSimTriModem getInstance() {
        HwVSimTriModem hwVSimTriModem;
        synchronized (mLock) {
            if (sModem == null) {
                throw new RuntimeException("TriModemController not yet created");
            }
            hwVSimTriModem = sModem;
        }
        return hwVSimTriModem;
    }

    private HwVSimTriModem(HwVSimController vsimController, Context context, CommandsInterface vsimCi, CommandsInterface[] cis) {
        super(vsimController, context, vsimCi, cis);
        logd("1 mIsM2CSOnly = false");
        this.mM0NetworkMode = -1;
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    public void onRadioPowerOffDone(HwVSimProcessor processor, AsyncResult ar) {
        if (processor != null && ar != null) {
            HwVSimRequest request = ar.userObj;
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
        HwVSimRequest request = ar.userObj;
        logd("onGetSimSlotDone : subId = " + request.mSubId);
        if (ar.exception == null && ar.result != null && ((int[]) ar.result).length == 3) {
            int mainSlot;
            boolean isVSimOnM0;
            int[] slots = ar.result;
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
                mainSlot = request.getMainSlot();
                isVSimOnM0 = false;
            }
            logd("onGetSimSlotDone : mainSlot = " + mainSlot);
            logd("onGetSimSlotDone : isVSimOnM0 = " + isVSimOnM0);
            request.setMainSlot(mainSlot);
            request.setIsVSimOnM0(isVSimOnM0);
            request.setGotSimSlotMark(true);
            return;
        }
        processor.doProcessException(ar, request);
    }

    public void onReconnectGetSimSlotDone(HwVSimProcessor processor, AsyncResult ar) {
        logd("onReconnectGetSimSlotDone");
        onGetSimSlotDone(processor, ar);
        HwVSimRequest request = ar.userObj;
        if (request.getIsVSimOnM0()) {
            int mainSlot = request.getMainSlot();
            logd("onReconnectGetSimSlotDone mainSlot:" + mainSlot + " vsimSlot:" + 2);
            HwVSimRequest vsimRequest = request.clone();
            vsimRequest.mSubId = 2;
            radioPowerOff(processor, vsimRequest, 2);
            HwVSimRequest mainRequest = request.clone();
            mainRequest.mSubId = mainSlot;
            radioPowerOff(processor, mainRequest, mainSlot);
            processor.transitionToState(9);
            return;
        }
        logd("onReconnectGetSimSlotDone vsim not open ,do not reconnect process");
        processor.transitionToState(0);
    }

    public SimStateInfo onGetSimStateDone(HwVSimProcessor processor, AsyncResult ar) {
        int subId = ar.userObj.mSubId;
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
        return new SimStateInfo(simIndex, simEnable, simSub, simNetinfo);
    }

    public void onQueryCardTypeDone(HwVSimProcessor processor, AsyncResult ar) {
        HwVSimRequest request = ar.userObj;
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
        HwVSimRequest request = ar.userObj;
        logd("onSwitchCommrilDone : subId = " + request.mSubId);
        int mainSlot = request.getMainSlot();
        int expectSlot = request.getExpectSlot();
        if (!(mainSlot == expectSlot || expectSlot == -1)) {
            logd("onSwitchCommrilDone : adjust mainSlot to " + expectSlot);
            request.setMainSlot(expectSlot);
        }
        processor.setProcessType(ProcessType.PROCESS_TYPE_SWAP);
        VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 1);
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
        boolean isULOnly = this.mVSimController.getULOnlyProp();
        int insertedCardCount = this.mVSimController.getInsertedCardCount();
        boolean hasIccCardOnM2 = this.mVSimController.hasIccCardOnM2();
        logd("insertedCardCount = " + insertedCardCount + ", isULOnly = " + isULOnly + ", hasIccCardOnM2 = " + hasIccCardOnM2);
        if (insertedCardCount == 0 || ((insertedCardCount == 1 && !(hasIccCardOnM2 && (isULOnly ^ 1) == 0)) || !(insertedCardCount != 2 || (isULOnly ^ 1) == 0 || (HwVSimUtilsInner.isPlatformRealTripple() ^ 1) == 0))) {
            logd("m2 no need to radio on");
            return false;
        }
        logd("m2 need to radio on");
        return true;
    }

    public void onSetNetworkRatAndSrvdomainDone(HwVSimProcessor processor, AsyncResult ar) {
        this.mIsM2CSOnly = true;
        logd("onSetNetworkRatAndSrvdomainDone mIsM2CSOnly = true");
    }

    public void checkEnableSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        if (request != null) {
            int[] cardTypes = request.getCardTypes();
            if (cardTypes != null && cardTypes.length != 0) {
                int expectSlot;
                boolean[] isCardPresent = HwVSimUtilsInner.getCardState(cardTypes);
                int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
                logd("Enable: inserted card count = " + insertedCardCount);
                VSimEventInfoUtils.setCardPresent(this.mVSimController.mEventInfo, this.mVSimController.getCardPresentNumeric(isCardPresent));
                CommrilMode currentCommrilMode = getCommrilMode();
                logd("Enable: currentCommrilMode = " + currentCommrilMode);
                int mainSlot = request.getMainSlot();
                logd("Enable: mainSlot = " + mainSlot);
                CommrilMode assumedCommrilMode = CommrilMode.NON_MODE;
                CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
                assumedCommrilMode = getAssumedCommrilMode(mainSlot, cardTypes);
                int savedMainSlot = getVSimSavedMainSlot();
                logd("Enable: savedMainSlot = " + savedMainSlot);
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
                } else {
                    boolean isVSimULOnlyMode = getVSimULOnlyMode();
                    logd("Enable: isVSimULOnlyMode = " + isVSimULOnlyMode);
                    if (isVSimULOnlyMode) {
                        expectPara = getExpectParaCheckEnableTwoSimULOnly(processor, request, assumedCommrilMode);
                        expectCommrilMode = expectPara.getExpectCommrilMode();
                        expectSlot = expectPara.getExpectSlot();
                    } else {
                        int reservedSub = this.mVSimController.getUserReservedSubId();
                        if ((!HwVSimUtilsInner.isPlatformRealTripple() || HwVSimUtilsInner.isVSimDsdsVersionOne()) && reservedSub == -1) {
                            logd("Enable: reserved sub not set");
                            processor.notifyResult(request, Integer.valueOf(7));
                            processor.transitionToState(0);
                            return;
                        } else if (HwVSimUtilsInner.isDualImsSupported()) {
                            expectPara = getExpectParaCheckEnable(request, assumedCommrilMode);
                            expectCommrilMode = expectPara.getExpectCommrilMode();
                            expectSlot = expectPara.getExpectSlot();
                        } else {
                            expectPara = getExpectParaCheckEnableTwoSimULG(processor, request, assumedCommrilMode);
                            expectCommrilMode = expectPara.getExpectCommrilMode();
                            expectSlot = expectPara.getExpectSlot();
                        }
                    }
                }
                setAlternativeUserReservedSubId(insertedCardCount, expectSlot);
                processAfterCheckEnableCondition(processor, request, expectCommrilMode, expectSlot, currentCommrilMode);
            }
        }
    }

    private CommrilMode getAssumedCommrilMode(int mainSlot, int[] cardTypes) {
        CommrilMode assumedCommrilMode = CommrilMode.NON_MODE;
        if (HwVSimUtilsInner.isChinaTelecom() && HwVSimUtilsInner.isPlatformRealTripple()) {
            assumedCommrilMode = CommrilMode.getCLGMode();
        } else {
            assumedCommrilMode = getExpectCommrilMode(mainSlot, cardTypes);
        }
        if (assumedCommrilMode == CommrilMode.NON_MODE) {
            assumedCommrilMode = getCommrilMode();
        }
        logd("getAssumedCommrilMode: assumedCommrilMode = " + assumedCommrilMode);
        return assumedCommrilMode;
    }

    private ExpectPara getExpectParaCheckEnable(HwVSimRequest request, CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckEnable assumedCommrilMode = " + assumedCommrilMode);
        ExpectPara expectPara = new ExpectPara();
        int mainSlot = request.getMainSlot();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        int[] cardTypes = request.getCardTypes();
        boolean isVSimOn = request.getIsVSimOnM0();
        int expectSlot = isVSimOn ? mainSlot : slaveSlot;
        logd("getExpectParaCheckEnable isVSimOn = " + isVSimOn + " expectSlot = " + expectSlot);
        expectSlot = getExpectSlotForCmcc(expectSlot, isVSimOn);
        HwVSimSlotSwitchController instance = HwVSimSlotSwitchController.getInstance();
        if (isVSimOn) {
            mainSlot = expectSlot;
        }
        CommrilMode expectCommrilMode = instance.getVSimOnCommrilMode(isVSimOn, mainSlot, cardTypes);
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckEnable expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private int getExpectSlotForCmcc(int expectSlot, boolean isVSimOn) {
        if (!isVSimOn || !HwVSimUtilsInner.IS_CMCC_4GSWITCH_DISABLE) {
            return expectSlot;
        }
        if (HwAllInOneController.getInstance().isCMCCHybird()) {
            if (HwAllInOneController.getInstance().isCMCCCardBySlotId(0)) {
                expectSlot = 1;
            } else {
                expectSlot = 0;
            }
            logd("getExpectSlotForCmcc expectSlot = " + expectSlot);
        }
        return expectSlot;
    }

    private int getExpectSlotForDisableForCmcc(int expectSlot) {
        if (!HwVSimUtilsInner.IS_CMCC_4GSWITCH_DISABLE) {
            return expectSlot;
        }
        if (HwAllInOneController.getInstance().isCMCCHybird()) {
            if (HwAllInOneController.getInstance().isCMCCCardBySlotId(0)) {
                expectSlot = 0;
            } else {
                expectSlot = 1;
            }
            logd("getExpectSlotForDisableForCmcc expectSlot = " + expectSlot);
        }
        return expectSlot;
    }

    private ExpectPara getExpectParaCheckEnableNoSim(HwVSimProcessor processor, HwVSimRequest request, CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckEnableNoSim assumedCommrilMode = " + assumedCommrilMode);
        ExpectPara expectPara = new ExpectPara();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        if (CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (HwVSimUtilsInner.isChinaTelecom()) {
                expectCommrilMode = CommrilMode.getCGMode();
            } else {
                expectCommrilMode = CommrilMode.getULGMode();
            }
        } else if (CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getCGMode();
        } else if (CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getULGMode();
        }
        int expectSlot = isVSimOnM0 ? mainSlot : slaveSlot;
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
        if (CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getCGMode();
            expectSlot = slaveSlot;
            if (HwVSimUtilsInner.isChinaTelecom() && isVSimOnM0) {
                expectSlot = mainSlot;
            }
        } else if (CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getCGMode();
            expectSlot = mainSlot;
        } else if (CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getULGMode();
            expectSlot = isCardPresent[mainSlot] ? slaveSlot : mainSlot;
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckEnableOneSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private ExpectPara getExpectParaCheckEnableTwoSimULOnly(HwVSimProcessor processor, HwVSimRequest request, CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckEnableTwoSimULOnly assumedCommrilMode = " + assumedCommrilMode);
        ExpectPara expectPara = new ExpectPara();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        int expectSlot = -1;
        if (CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getCGMode();
            expectSlot = slaveSlot;
            if (HwVSimUtilsInner.isChinaTelecom() && isVSimOnM0) {
                expectSlot = mainSlot;
            }
        } else if (CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getCGMode();
            expectSlot = mainSlot;
        } else if (CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getULGMode();
            expectSlot = mainSlot;
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckEnableTwoSimULOnly expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private ExpectPara getExpectParaCheckEnableTwoSimULG(HwVSimProcessor processor, HwVSimRequest request, CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckEnableTwoSimULG assumedCommrilMode = " + assumedCommrilMode);
        ExpectPara expectPara = new ExpectPara();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int reservedSub = this.mVSimController.getUserReservedSubId();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        int expectSlot = -1;
        if (CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (HwVSimUtilsInner.isChinaTelecom() && HwVSimUtilsInner.isPlatformRealTripple()) {
                expectCommrilMode = CommrilMode.getCGMode();
                if (isVSimOnM0) {
                    expectSlot = mainSlot;
                } else {
                    expectSlot = slaveSlot;
                }
            } else if (reservedSub == mainSlot) {
                expectCommrilMode = CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            } else if (reservedSub == slaveSlot) {
                if (HwVSimSlotSwitchController.isCDMACard(cardTypes[reservedSub])) {
                    expectCommrilMode = CommrilMode.getCGMode();
                } else {
                    expectCommrilMode = CommrilMode.getULGMode();
                }
                expectSlot = mainSlot;
            } else {
                expectCommrilMode = CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            }
        } else if (CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (reservedSub == mainSlot) {
                expectCommrilMode = CommrilMode.getULGMode();
                expectSlot = slaveSlot;
            } else {
                expectCommrilMode = CommrilMode.getCGMode();
                expectSlot = mainSlot;
            }
        } else if (CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getULGMode();
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
        logd("processAfterCheckEnableCondition");
        logd("Enable: expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot + " currentCommrilMode = " + currentCommrilMode);
        int mainSlot = request.getMainSlot();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        boolean isNeedSwitchCommrilMode = calcIsNeedSwitchCommrilMode(expectCommrilMode, currentCommrilMode);
        logd("Enable: isNeedSwitchCommrilMode = " + isNeedSwitchCommrilMode);
        logd("Enable: expectSlot = " + expectSlot + " mainSlot = " + mainSlot);
        if (IS_FAST_SWITCH_SIMSLOT && isNeedSwitchCommrilMode) {
            request.setIsNeedSwitchCommrilMode(isNeedSwitchCommrilMode);
            request.setExpectCommrilMode(expectCommrilMode);
        }
        if (IS_FAST_SWITCH_SIMSLOT || !isNeedSwitchCommrilMode) {
            if (expectSlot != mainSlot) {
                processor.setProcessType(ProcessType.PROCESS_TYPE_CROSS);
                VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 2);
            } else if (isVSimOnM0) {
                int[] subs = getSimSlotTable();
                if (subs.length == 0) {
                    processor.doProcessException(null, request);
                    return;
                }
                request.setSubs(subs);
                processor.setProcessType(ProcessType.PROCESS_TYPE_DIRECT);
                VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 4);
            } else {
                processor.setProcessType(ProcessType.PROCESS_TYPE_SWAP);
                VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 1);
            }
            processor.transitionToState(3);
        } else {
            request.setExpectSlot(expectSlot);
            switchCommrilMode(expectCommrilMode, expectSlot, mainSlot, isVSimOnM0, processor.obtainMessage(55, request));
        }
    }

    public void checkDisableSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        if (request != null) {
            int[] cardTypes = request.getCardTypes();
            if (cardTypes != null && cardTypes.length != 0) {
                CommrilMode expectCommrilMode;
                int savedMainSlot = getVSimSavedMainSlot();
                logd("Disable: savedMainSlot = " + savedMainSlot);
                CommrilMode currentCommrilMode = getCommrilMode();
                logd("Disable: currentCommrilMode = " + currentCommrilMode);
                int mainSlot = request.getMainSlot();
                logd("Disable: mainSlot = " + mainSlot);
                int expectSlot = getExpectSlot(cardTypes, mainSlot, savedMainSlot);
                logd("Disable: expectSlot = " + expectSlot);
                request.setExpectSlot(expectSlot);
                if (HwVSimUtilsInner.isChinaTelecom()) {
                    expectCommrilMode = CommrilMode.getCLGMode();
                } else {
                    expectCommrilMode = getExpectCommrilMode(expectSlot, cardTypes);
                }
                logd("Disable: expectCommrilMode = " + expectCommrilMode);
                boolean isNeedSwitchCommrilMode = calcIsNeedSwitchCommrilMode(expectCommrilMode, currentCommrilMode);
                logd("Disable: isNeedSwitchCommrilMode = " + isNeedSwitchCommrilMode);
                request.setIsNeedSwitchCommrilMode(isNeedSwitchCommrilMode);
                if (IS_FAST_SWITCH_SIMSLOT && isNeedSwitchCommrilMode) {
                    request.setExpectCommrilMode(expectCommrilMode);
                }
                if (!processor.isReadyProcess()) {
                    if (!IS_FAST_SWITCH_SIMSLOT && isNeedSwitchCommrilMode) {
                        processor.setProcessType(ProcessType.PROCESS_TYPE_SWAP);
                        VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 11);
                    } else if (expectSlot == mainSlot) {
                        processor.setProcessType(ProcessType.PROCESS_TYPE_SWAP);
                        VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 11);
                    } else {
                        processor.setProcessType(ProcessType.PROCESS_TYPE_CROSS);
                        VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 12);
                    }
                    processor.transitionToState(6);
                } else if (IS_FAST_SWITCH_SIMSLOT || !isNeedSwitchCommrilMode) {
                    HwVSimPhoneFactory.setIsVsimEnabledProp(false);
                    getIMSI(expectSlot);
                    processor.transitionToState(0);
                } else {
                    switchCommrilMode(expectCommrilMode, expectSlot, mainSlot, false, processor.obtainMessage(55, request));
                }
            }
        }
    }

    private int getExpectSlot(int[] cardTypes, int mainSlot, int savedMainSlot) {
        logd("getExpectSlot cardTypes = " + Arrays.toString(cardTypes) + " mainSlot = " + mainSlot + " savedMainSlot = " + savedMainSlot);
        int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
        logd("getExpectSlot: inserted card count = " + insertedCardCount);
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        if (insertedCardCount == 0) {
            return savedMainSlot;
        }
        if (insertedCardCount != 1) {
            int savedMainSlotSubState = this.mVSimController.getSubState(savedMainSlot);
            int anotherSlot = savedMainSlot == 0 ? 1 : 0;
            if (this.mVSimController.getSubState(anotherSlot) == 0 || savedMainSlotSubState != 0) {
                return getExpectSlotForDisableForCmcc(savedMainSlot);
            }
            return anotherSlot;
        } else if (HwVSimUtilsInner.isPlatformRealTripple() || (HwVSimUtilsInner.isChinaTelecom() ^ 1) != 0) {
            return slaveSlot;
        } else {
            if (HwVSimSlotSwitchController.isCDMACard(cardTypes[mainSlot])) {
                return mainSlot;
            }
            if (cardTypes[mainSlot] == 1) {
                return slaveSlot;
            }
            if (HwVSimSlotSwitchController.isCDMACard(cardTypes[slaveSlot])) {
                return slaveSlot;
            }
            return mainSlot;
        }
    }

    public void checkSwitchModeSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        if (request != null) {
            int[] cardTypes = request.getCardTypes();
            if (cardTypes != null && cardTypes.length != 0) {
                int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
                logd("Switch mode: inserted card count = " + insertedCardCount);
                CommrilMode currentCommrilMode = getCommrilMode();
                logd("Switch mode: currentCommrilMode = " + currentCommrilMode);
                int mainSlot = request.getMainSlot();
                CommrilMode assumedCommrilMode = CommrilMode.NON_MODE;
                CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
                assumedCommrilMode = getAssumedCommrilMode(mainSlot, cardTypes);
                logd("Switch mode: assumedCommrilMode = " + assumedCommrilMode);
                if (request.getIsVSimOnM0()) {
                    int expectSlot;
                    ExpectPara expectPara;
                    if (insertedCardCount == 0) {
                        expectPara = getExpectParaCheckSwitchNoSim(processor, request, assumedCommrilMode);
                        expectCommrilMode = expectPara.getExpectCommrilMode();
                        expectSlot = expectPara.getExpectSlot();
                    } else if (insertedCardCount == 1) {
                        expectPara = getExpectParaCheckSwitchOneSim(processor, request, assumedCommrilMode);
                        expectCommrilMode = expectPara.getExpectCommrilMode();
                        expectSlot = expectPara.getExpectSlot();
                    } else {
                        boolean isVSimULOnlyMode = getSwitchModeVSimULOnlyMode(request);
                        logd("Switch mode: isVSimULOnlyMode = " + isVSimULOnlyMode);
                        logd("Switch mode: mainSlot = " + mainSlot);
                        if (isVSimULOnlyMode) {
                            expectPara = getExpectParaCheckSwitchTwoSimULOnly(processor, request, assumedCommrilMode);
                            expectCommrilMode = expectPara.getExpectCommrilMode();
                            expectSlot = expectPara.getExpectSlot();
                        } else {
                            int reservedSub = getSwtichModeUserReservedSubId(request);
                            if ((!HwVSimUtilsInner.isPlatformRealTripple() || HwVSimUtilsInner.isVSimDsdsVersionOne()) && reservedSub == -1) {
                                logd("Switch mode: reserved sub not set");
                                processor.notifyResult(request, Boolean.valueOf(false));
                                processor.transitionToState(0);
                                return;
                            } else if (HwVSimUtilsInner.isDualImsSupported()) {
                                expectPara = getExpectParaCheckEnable(request, assumedCommrilMode);
                                expectCommrilMode = expectPara.getExpectCommrilMode();
                                expectSlot = expectPara.getExpectSlot();
                            } else {
                                expectPara = getExpectParaCheckSwitchTwoSimULG(processor, request, assumedCommrilMode);
                                expectCommrilMode = expectPara.getExpectCommrilMode();
                                expectSlot = expectPara.getExpectSlot();
                            }
                        }
                    }
                    logd("Switch mode: expectCommrilMode = " + expectCommrilMode);
                    logd("Switch mode: isNeedSwitchCommrilMode = " + calcIsNeedSwitchCommrilMode(expectCommrilMode, currentCommrilMode));
                    logd("Switch mode: expectSlot = " + expectSlot + ", mainSlot = " + mainSlot);
                    processAfterCheckSwitchCondition(processor, request, expectCommrilMode, expectSlot, currentCommrilMode);
                    return;
                }
                processor.notifyResult(request, Boolean.valueOf(false));
                processor.transitionToState(0);
            }
        }
    }

    private ExpectPara getExpectParaCheckSwitchNoSim(HwVSimProcessor processor, HwVSimRequest request, CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckSwitchNoSim assumedCommrilMode = " + assumedCommrilMode);
        ExpectPara expectPara = new ExpectPara();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        int expectSlot = -1;
        if (CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (HwVSimUtilsInner.isChinaTelecom()) {
                expectCommrilMode = CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            } else {
                expectCommrilMode = CommrilMode.getULGMode();
                expectSlot = mainSlot;
            }
        } else if (CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getULGMode();
            expectSlot = mainSlot;
        } else if (CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getULGMode();
            expectSlot = mainSlot;
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckSwitchNoSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private ExpectPara getExpectParaCheckSwitchOneSim(HwVSimProcessor processor, HwVSimRequest request, CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckSwitchOneSim assumedCommrilMode = " + assumedCommrilMode);
        ExpectPara expectPara = new ExpectPara();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        int expectSlot = -1;
        boolean[] isCardPresent = HwVSimUtilsInner.getCardState(cardTypes);
        if (CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getCGMode();
            expectSlot = slaveSlot;
        } else if (CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getCGMode();
            expectSlot = mainSlot;
        } else if (CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getULGMode();
            if (isCardPresent[mainSlot]) {
                expectSlot = slaveSlot;
            } else {
                expectSlot = mainSlot;
            }
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckSwitchOneSim expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private ExpectPara getExpectParaCheckSwitchTwoSimULOnly(HwVSimProcessor processor, HwVSimRequest request, CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckSwitchTwoULOnly assumedCommrilMode = " + assumedCommrilMode);
        ExpectPara expectPara = new ExpectPara();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        int expectSlot = -1;
        if (CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getCGMode();
            if (HwVSimUtilsInner.isChinaTelecom()) {
                expectSlot = mainSlot;
            } else {
                expectSlot = slaveSlot;
            }
        } else if (CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getCGMode();
            expectSlot = mainSlot;
        } else if (CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getULGMode();
            expectSlot = mainSlot;
        }
        expectPara.setExpectSlot(expectSlot);
        expectPara.setExpectCommrilMode(expectCommrilMode);
        logd("getExpectParaCheckSwitchTwoULOnly expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot);
        return expectPara;
    }

    private ExpectPara getExpectParaCheckSwitchTwoSimULG(HwVSimProcessor processor, HwVSimRequest request, CommrilMode assumedCommrilMode) {
        logd("getExpectParaCheckSwitchTwoSimULG assumedCommrilMode = " + assumedCommrilMode);
        ExpectPara expectPara = new ExpectPara();
        int mainSlot = request.getMainSlot();
        int[] cardTypes = request.getCardTypes();
        int reservedSub = this.mVSimController.getUserReservedSubId();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        int expectSlot = -1;
        if (CommrilMode.isCLGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (HwVSimUtilsInner.isChinaTelecom()) {
                expectCommrilMode = CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            } else if (reservedSub == mainSlot) {
                expectCommrilMode = CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            } else if (reservedSub == slaveSlot) {
                if (HwVSimSlotSwitchController.isCDMACard(cardTypes[reservedSub])) {
                    expectCommrilMode = CommrilMode.getCGMode();
                } else {
                    expectCommrilMode = CommrilMode.getULGMode();
                }
                expectSlot = mainSlot;
            } else {
                expectCommrilMode = CommrilMode.getCGMode();
                expectSlot = slaveSlot;
            }
        } else if (CommrilMode.isCGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            if (reservedSub == mainSlot) {
                expectCommrilMode = CommrilMode.getULGMode();
                expectSlot = slaveSlot;
            } else {
                expectCommrilMode = CommrilMode.getCGMode();
                expectSlot = mainSlot;
            }
        } else if (CommrilMode.isULGMode(assumedCommrilMode, cardTypes, mainSlot)) {
            expectCommrilMode = CommrilMode.getULGMode();
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

    private void processAfterCheckSwitchCondition(HwVSimProcessor processor, HwVSimRequest request, CommrilMode expectCommrilMode, int expectSlot, CommrilMode currentCommrilMode) {
        logd("processAfterCheckSwitchCondition");
        logd("Switch: expectCommrilMode = " + expectCommrilMode + " expectSlot = " + expectSlot + " currentCommrilMode = " + currentCommrilMode);
        int mainSlot = request.getMainSlot();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        boolean isNeedSwitchCommrilMode = calcIsNeedSwitchCommrilMode(expectCommrilMode, currentCommrilMode);
        logd("Switch mode: isNeedSwitchCommrilMode = " + isNeedSwitchCommrilMode);
        logd("Switch mode: expectSlot = " + expectSlot + " mainSlot = " + mainSlot);
        if (expectSlot != mainSlot || (isNeedSwitchCommrilMode ^ 1) == 0) {
            if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT) {
                this.mVSimController.updateSubState(0, 1);
                this.mVSimController.updateSubState(1, 1);
            }
            if (IS_FAST_SWITCH_SIMSLOT && isNeedSwitchCommrilMode) {
                request.setIsNeedSwitchCommrilMode(isNeedSwitchCommrilMode);
                request.setExpectCommrilMode(expectCommrilMode);
            }
            if (IS_FAST_SWITCH_SIMSLOT || !isNeedSwitchCommrilMode) {
                if (expectSlot != mainSlot) {
                    processor.setProcessType(ProcessType.PROCESS_TYPE_CROSS);
                } else {
                    processor.setProcessType(ProcessType.PROCESS_TYPE_SWAP);
                }
                processor.transitionToState(12);
            } else {
                request.setExpectSlot(expectSlot);
                switchCommrilMode(expectCommrilMode, expectSlot, mainSlot, isVSimOnM0, processor.obtainMessage(55, request));
            }
            return;
        }
        logd("Switch mode: no need to switch sim slot and commril mode.");
        processor.notifyResult(request, Boolean.valueOf(true));
        processor.transitionToState(0);
    }

    public void switchSimSlot(HwVSimProcessor processor, HwVSimRequest request) {
        int modem0;
        int modem1;
        int modem2;
        int mainSlot = request.getMainSlot();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        logd("switchSimSlot mainSlot = " + mainSlot + ", slaveSlot = " + slaveSlot);
        int subId = mainSlot;
        int expectSlot;
        if (processor.isEnableProcess() || processor.isSwitchModeProcess()) {
            if (processor.isSwapProcess()) {
                modem0 = 2;
                modem1 = slaveSlot;
                modem2 = mainSlot;
            } else if (processor.isCrossProcess()) {
                expectSlot = slaveSlot;
                logd("expectSlot = " + expectSlot);
                request.setExpectSlot(expectSlot);
                modem0 = 2;
                modem1 = mainSlot;
                modem2 = slaveSlot;
            } else {
                processor.doProcessException(null, request);
                return;
            }
            if (request.getIsVSimOnM0()) {
                subId = 2;
            }
        } else if (processor.isDisableProcess()) {
            expectSlot = request.getExpectSlot();
            if (processor.isSwapProcess()) {
                modem0 = mainSlot;
                modem1 = slaveSlot;
            } else if (expectSlot == 0) {
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
        int[] oldSlots = getSimSlotTable();
        int[] slots = createSimSlotsTable(modem0, modem1, modem2);
        request.setSlots(slots);
        request.mSubId = subId;
        boolean needSwich = (oldSlots.length == 3 && oldSlots[0] == slots[0] && oldSlots[1] == slots[1]) ? oldSlots[2] != slots[2] : true;
        Message onCompleted = processor.obtainMessage(43, request);
        if (needSwich) {
            logd("switchSimSlot subId " + subId + " modem0 = " + modem0 + " modem1 = " + modem1 + " modem2 = " + modem2);
            CommandsInterface ci = getCiBySub(subId);
            if (ci != null) {
                ci.hotSwitchSimSlot(modem0, modem1, modem2, onCompleted);
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

    public boolean isDoneAllSwitchSlot(HwVSimProcessor processor, AsyncResult ar) {
        return true;
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

    public int restoreSavedNetworkMode(HwVSimProcessor processor, int modemId) {
        int networkMode = super.restoreSavedNetworkMode(processor, modemId);
        logd("restoreSavedNetworkMode mM0NetworkMode = " + this.mM0NetworkMode);
        return networkMode;
    }

    public void saveM0NetworkMode(int mode) {
        this.mM0NetworkMode = mode;
        logd("saveM0NetworkMode mM0NetworkMode = " + this.mM0NetworkMode);
    }

    public void doEnableStateEnter(HwVSimProcessor processor, HwVSimRequest request) {
        logd("Enable state enter: mIsM0CSOnly = " + this.mIsM0CSOnly + ", mIsM2CSOnly  = " + this.mIsM2CSOnly);
        this.mIsM0CSOnly = false;
        this.mIsM2CSOnly = false;
        logd("mIsM0CSOnly = false, mIsM2CSOnly = false");
    }

    public void doDisableStateExit(HwVSimProcessor processor, HwVSimRequest request) {
        logd("disable state exit: mIsM0CSOnly = " + this.mIsM0CSOnly + ", mIsM2CSOnly  = " + this.mIsM2CSOnly);
        this.mMmsOnM2 = false;
        this.mIsM0CSOnly = false;
        this.mIsM2CSOnly = false;
        this.mM0NetworkMode = -1;
        logd("mIsM0CSOnly = false, mIsM2CSOnly = false");
    }

    public boolean isM2CSOnly() {
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            return false;
        }
        logd("isM2CSOnly = " + this.mIsM2CSOnly);
        return this.mIsM2CSOnly;
    }

    public boolean isMmsOnM2() {
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            return false;
        }
        return this.mMmsOnM2;
    }

    public boolean isSubOnM2(int subId) {
        if (this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.isSubOnM2(subId);
    }

    public void checkMmsStart(int subId) {
        if (!HwVSimUtilsInner.isPlatformRealTripple()) {
            logd("checkMmsStart, subId = " + subId + ", mMmsOnM2 =" + this.mMmsOnM2);
            if (isSubOnM2(subId) && !this.mMmsOnM2) {
                this.mMmsOnM2 = true;
                logd("checkMmsStart mMmsOnM2 = " + this.mMmsOnM2);
                int m0Rat = this.mM0NetworkMode;
                CommandsInterface ciVSim = getCiBySub(2);
                if (ciVSim != null) {
                    ciVSim.setNetworkRatAndSrvDomainCfg(m0Rat, 0, null);
                }
                CommandsInterface ci = getCiBySub(subId);
                if (ci != null) {
                    ci.setNetworkRatAndSrvDomainCfg(1, 2, null);
                }
                this.mIsM0CSOnly = true;
                this.mIsM2CSOnly = false;
                logd("checkMmsStart mM0NetworkMode = " + this.mM0NetworkMode);
            }
        }
    }

    public void checkMmsStop(int subId) {
        if (!HwVSimUtilsInner.isPlatformRealTripple()) {
            logd("checkMmsStop, subId = " + subId + ", mMmsOnM2 =" + this.mMmsOnM2);
            if (isSubOnM2(subId) && this.mMmsOnM2) {
                this.mMmsOnM2 = false;
                logd("checkMmsStop mMmsOnM2 = " + this.mMmsOnM2);
                CommandsInterface ci = getCiBySub(subId);
                if (ci != null) {
                    ci.setNetworkRatAndSrvDomainCfg(1, 0, null);
                }
                int m0Rat = this.mM0NetworkMode;
                CommandsInterface ciVSim = getCiBySub(2);
                if (ciVSim != null) {
                    ciVSim.setNetworkRatAndSrvDomainCfg(m0Rat, 2, null);
                }
                this.mIsM0CSOnly = false;
                this.mIsM2CSOnly = true;
                logd("checkMmsStop mM0NetworkMode = " + this.mM0NetworkMode);
            }
        }
    }

    protected int[] createSimSlotsTable(int m0, int m1, int m2) {
        int[] slots = new int[MAX_SUB_COUNT];
        slots[0] = m0;
        slots[1] = m1;
        slots[2] = m2;
        return slots;
    }

    private boolean getSwitchModeVSimULOnlyMode(HwVSimRequest request) {
        WorkModeParam param = getWorkModeParam(request);
        if (param == null) {
            return true;
        }
        switch (param.workMode) {
            case 0:
                return false;
            case 1:
                return false;
            case 2:
                if (HwVSimUtilsInner.isPlatformRealTripple()) {
                    return false;
                }
                return true;
            default:
                return true;
        }
    }

    private int getSwtichModeUserReservedSubId(HwVSimRequest request) {
        WorkModeParam param = getWorkModeParam(request);
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

    protected WorkModeParam getWorkModeParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getWorkModeParam(request);
    }

    private boolean calcIsNeedSwitchCommrilMode(CommrilMode expect, CommrilMode current) {
        if (expect == CommrilMode.NON_MODE || expect == current) {
            return false;
        }
        return true;
    }

    private boolean isGotAllCardTypes(HwVSimRequest request) {
        if (request == null) {
            return false;
        }
        return request.isGotAllCardTypes();
    }

    private void setAlternativeUserReservedSubId(int cardCount, int expectSlot) {
        if (HwVSimUtilsInner.isVSimDsdsVersionOne() && -1 == this.mVSimController.getUserReservedSubId()) {
            if (cardCount == 0 || cardCount == 1) {
                int modem1Slot = expectSlot == 0 ? 1 : 0;
                this.mVSimController.setAlternativeUserReservedSubId(modem1Slot);
                logd("setAlternativeUserReservedSubId: set subId is " + modem1Slot + ".");
            }
        }
    }
}
