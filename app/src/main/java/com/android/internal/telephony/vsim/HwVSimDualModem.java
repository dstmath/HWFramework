package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.vsim.HwVSimController.ProcessType;
import com.android.internal.telephony.vsim.HwVSimModemAdapter.SimStateInfo;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController.CommrilMode;
import com.android.internal.telephony.vsim.process.HwVSimProcessor;
import java.util.Arrays;

public class HwVSimDualModem extends HwVSimModemAdapter {
    private static final String LOG_TAG = "DualModemController";
    private static final Object mLock = null;
    private static HwVSimDualModem sModem;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimDualModem.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimDualModem.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimDualModem.<clinit>():void");
    }

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
            if (slots[0] == 1 && slots[1] == 2 && !isVSimOnM0) {
                mainSlot = 0;
                responseSlots[0] = 0;
                responseSlots[1] = 1;
                responseSlots[2] = 2;
            } else if (slots[0] == 2 && slots[1] == 1 && !isVSimOnM0) {
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
            return;
        }
        int cardCount = cardTypes.length;
        if (cardCount == 0) {
            loge("checkEnableSimCondition, cardCount == 0 !");
            return;
        }
        CommrilMode assumedCommrilMode;
        boolean[] isCardPresent = new boolean[cardCount];
        int insertedCardCount = 0;
        for (int i = 0; i < cardCount; i++) {
            if (cardTypes[i] == 0) {
                isCardPresent[i] = false;
            } else {
                isCardPresent[i] = true;
                insertedCardCount++;
            }
        }
        logd("inserted card count = " + insertedCardCount);
        CommrilMode currentCommrilMode = getCommrilMode();
        logd("currentCommrilMode = " + currentCommrilMode);
        int mainSlot = request.getMainSlot();
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        CommrilMode expectCommrilMode = CommrilMode.NON_MODE;
        int expectSlot = -1;
        boolean isVSimOnM0 = request.getIsVSimOnM0();
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
        if (insertedCardCount == 0) {
            if (assumedCommrilMode == CommrilMode.CLG_MODE) {
                if (HwVSimUtilsInner.isChinaTelecom()) {
                    expectCommrilMode = CommrilMode.CG_MODE;
                    expectSlot = isVSimOnM0 ? mainSlot : slaveSlot;
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
        } else if (insertedCardCount != 1) {
            int reservedSub = this.mVSimController.getUserReservedSubId();
            if (reservedSub == -1) {
                logd("Enable: reserved sub not set");
                processor.notifyResult(request, Integer.valueOf(7));
                processor.transitionToState(0);
                return;
            } else if (assumedCommrilMode == CommrilMode.CLG_MODE) {
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
                expectSlot = reservedSub == mainSlot ? slaveSlot : mainSlot;
            }
        } else if (assumedCommrilMode == CommrilMode.CLG_MODE) {
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
        boolean isNeedSwitchCommrilMode = calcIsNeedSwitchCommrilMode(expectCommrilMode, currentCommrilMode);
        logd("isNeedSwitchCommrilMode = " + isNeedSwitchCommrilMode);
        if (isNeedSwitchCommrilMode) {
            request.setExpectSlot(expectSlot);
            switchCommrilMode(expectCommrilMode, expectSlot, mainSlot, isVSimOnM0, processor.obtainMessage(55, request));
        } else {
            if (expectSlot == slaveSlot) {
                processor.setProcessType(ProcessType.PROCESS_TYPE_CROSS);
            } else {
                processor.setProcessType(ProcessType.PROCESS_TYPE_SWAP);
            }
            processor.transitionToState(3);
        }
    }

    public void checkDisableSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        if (request != null) {
            int[] cardTypes = request.getCardTypes();
            if (cardTypes != null) {
                if (cardCount != 0) {
                    CommrilMode expectCommrilMode;
                    int expectSlot;
                    int insertedCardCount = 0;
                    for (int i : cardTypes) {
                        if (i != 0) {
                            insertedCardCount++;
                        }
                    }
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
                    if (insertedCardCount == 0) {
                        expectSlot = savedMainSlot;
                    } else if (insertedCardCount == 1) {
                        expectSlot = slaveSlot;
                    } else {
                        expectSlot = savedMainSlot;
                    }
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
                        processor.transitionToState(0);
                    }
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
                logd("expectSlot = " + slaveSlot);
                request.setExpectSlot(slaveSlot);
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
        Message message = null;
        if (processor.isDisableProcess() && isCross) {
            HwVSimRequest M1Request = request.clone();
            M1Request.mSubId = modem1;
            message = processor.obtainMessage(43, M1Request);
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
            ci.hotSwitchSimSlotFor2Modem(modem0, modem1, modem2, message);
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
