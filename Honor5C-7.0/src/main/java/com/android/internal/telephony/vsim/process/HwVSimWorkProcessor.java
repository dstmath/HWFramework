package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.EnableParam;
import com.android.internal.telephony.vsim.HwVSimController.ProcessState;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimModemAdapter.SimStateInfo;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public abstract class HwVSimWorkProcessor extends HwVSimProcessor {
    protected static final int GET_ICC_CARD_STATUS_RETRY_TIMES = 5;
    public static final String LOG_TAG = "VSimWorkProcessor";
    protected static final int PHONE_COUNT = 0;
    protected int[] mGetIccCardStatusTimes;
    protected boolean mInDSDSPreProcess;
    protected HwVSimController mVSimController;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.process.HwVSimWorkProcessor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.process.HwVSimWorkProcessor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.process.HwVSimWorkProcessor.<clinit>():void");
    }

    protected abstract void logd(String str);

    protected abstract void onCardPowerOffDone(Message message);

    protected abstract void onCardPowerOnDone(Message message);

    protected abstract void onRadioPowerOnDone(Message message);

    protected abstract void onSetActiveModemModeDone(Message message);

    protected abstract void onSetPreferredNetworkTypeDone(Message message);

    protected abstract void onSetTeeDataReadyDone(Message message);

    protected abstract void onSwitchSlotDone(Message message);

    public HwVSimWorkProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
        this.mInDSDSPreProcess = false;
    }

    public void onEnter() {
        logd("onEnter");
        HwVSimRequest request = this.mRequest;
        if (request != null) {
            this.mModemAdapter.handleSubSwapProcess(this, request);
            if (this.mVSimController.isEnableProcess() && !this.mVSimController.isDirectProcess()) {
                this.mModemAdapter.setHwVSimPowerOn(this, request);
            }
            if (HwVSimUtilsInner.isPlatformRealTripple() && HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT && isSwapProcess()) {
                this.mInDSDSPreProcess = true;
                int slaveSlot = request.getMainSlot() == 0 ? 1 : 0;
                this.mVSimController.setProhibitSubUpdateSimNoChange(slaveSlot, true);
                this.mModemAdapter.radioPowerOff(this, request, slaveSlot);
            } else {
                this.mModemAdapter.radioPowerOff(this, request);
            }
            setProcessState(ProcessState.PROCESS_STATE_WORK);
        }
    }

    public void onExit() {
        logd("onExit");
    }

    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    protected void onRadioPowerOffDone(Message msg) {
        logd("onRadioPowerOffDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 2);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            if (this.mInDSDSPreProcess) {
                this.mInDSDSPreProcess = false;
                HwVSimRequest request = this.mRequest;
                if (request != null) {
                    this.mModemAdapter.radioPowerOff(this, request);
                } else {
                    return;
                }
            }
            this.mModemAdapter.onRadioPowerOffDone(this, ar);
        }
    }

    protected void onGetSimStateDone(Message msg) {
        logd("onGetSimStateDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 3);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            SimStateInfo ssInfo = this.mModemAdapter.onGetSimStateDone(this, ar);
            if (ssInfo != null) {
                logd("onGetSimStateDone ssInfo index = " + ssInfo.simIndex);
                logd("onGetSimStateDone ssInfo simEnable = " + ssInfo.simEnable);
                logd("onGetSimStateDone ssInfo simSub = " + ssInfo.simSub);
                logd("onGetSimStateDone ssInfo simNetInfo = " + ssInfo.simNetInfo);
            }
            HwVSimRequest request = ar.userObj;
            int subCount = request.getSubCount();
            int subId = request.mSubId;
            for (int i = 0; i < subCount; i++) {
                if (subId == request.getSubIdByIndex(i)) {
                    request.setSimStateMark(i, false);
                }
            }
            if (ssInfo != null) {
                this.mModemAdapter.cardPowerOff(this, request, subId, ssInfo.simIndex);
            }
        }
    }

    protected void getIccCardStatus(HwVSimRequest request, int subId) {
        logd("onCardPowerOffDone->getIccCardStatus,wait card status is absent");
        setIccCardStatusRetryTimes(subId, 0);
        this.mModemAdapter.getIccCardStatus(this, request, subId);
    }

    protected void onGetIccCardStatusDone(Message msg) {
        AsyncResult ar = msg.obj;
        if (isAsyncResultValidForRequestNotSupport(ar)) {
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            IccCardStatus status = ar.result;
            logd("onGetIccCardStatusDone:mCardState[" + subId + "]=" + (status != null ? status.mCardState : " state is null"));
            int retryTimes = getIccCardStatusRetryTimes(subId);
            if (status == null || status.mCardState == CardState.CARDSTATE_ABSENT || retryTimes >= GET_ICC_CARD_STATUS_RETRY_TIMES) {
                int subCount = request.getSubCount();
                for (int i = 0; i < subCount; i++) {
                    if (subId == request.getSubIdByIndex(i)) {
                        request.setGetIccCardStatusMark(i, false);
                    }
                }
                setIccCardStatusRetryTimes(subId, 0);
                if (isAllMarkClear(request)) {
                    afterGetAllCardStateDone();
                }
            } else {
                retryTimes++;
                setIccCardStatusRetryTimes(subId, retryTimes);
                logd("onGetIccCardStatusDone: retry getIccCardStatus,Times=" + retryTimes);
                this.mModemAdapter.getIccCardStatus(this, request, subId);
            }
        }
    }

    protected void afterGetAllCardStateDone() {
        logd("afterGetAllCardStateDone - do nothing.");
    }

    protected void setIccCardStatusRetryTimes(int subId, int times) {
        if (this.mGetIccCardStatusTimes == null) {
            this.mGetIccCardStatusTimes = new int[HwVSimModemAdapter.MAX_SUB_COUNT];
        }
        if (subId >= 0 && subId < this.mGetIccCardStatusTimes.length) {
            this.mGetIccCardStatusTimes[subId] = times;
        }
    }

    protected int getIccCardStatusRetryTimes(int subId) {
        if (this.mGetIccCardStatusTimes == null || subId < 0 || subId >= this.mGetIccCardStatusTimes.length) {
            return GET_ICC_CARD_STATUS_RETRY_TIMES;
        }
        return this.mGetIccCardStatusTimes[subId];
    }

    protected void onGetPreferredNetworkTypeDone(Message msg) {
        logd("onGetPreferredNetworkTypeDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 9);
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            this.mModemAdapter.onGetPreferredNetworkTypeDone(this, ar);
            HwVSimRequest request = ar.userObj;
            int subId = request.mSubId;
            EnableParam param = getEnableParam(request);
            if (param == null) {
                doEnableProcessException(ar, request, Integer.valueOf(3));
                return;
            }
            int networkMode = acqorderToNetworkMode(param.acqorder);
            logd("set preferred network to " + networkMode);
            this.mModemAdapter.setPreferredNetworkType(this, request, subId, networkMode);
        }
    }

    protected int acqorderToNetworkMode(String acqorder) {
        HwVSimRequest request = this.mRequest;
        if (request == null) {
            return 3;
        }
        int[] cardTypes = request.getCardTypes();
        if (cardTypes == null) {
            return 3;
        }
        int cardCount = cardTypes.length;
        if (cardCount == 0) {
            return 3;
        }
        boolean isULOnly;
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
        if (isDirectProcess()) {
            isULOnly = getULOnlyProp();
        } else {
            boolean hasIccCardOnM2 = false;
            boolean ulOnlyMode = getVSimULOnlyMode();
            if (HwVSimUtilsInner.isChinaTelecom() && HwVSimUtilsInner.isPlatformRealTripple()) {
                int userReservedSubId = getUserReservedSubId();
                int mainSlot = HwVSimPhoneFactory.getVSimSavedMainSlot();
                if (mainSlot == -1) {
                    mainSlot = 0;
                }
                int slaveSlot = mainSlot == 0 ? 1 : 0;
                if (insertedCardCount != 0) {
                    if (isCardPresent[mainSlot] && isCardPresent[slaveSlot] && (ulOnlyMode || slaveSlot == userReservedSubId)) {
                        hasIccCardOnM2 = true;
                    } else if (!isCardPresent[mainSlot] && isCardPresent[slaveSlot]) {
                        hasIccCardOnM2 = true;
                    }
                }
            } else if (ulOnlyMode && insertedCardCount == PHONE_COUNT) {
                hasIccCardOnM2 = true;
            }
            isULOnly = hasIccCardOnM2;
            setULOnlyProp(Boolean.valueOf(hasIccCardOnM2));
        }
        return calcNetworkModeByAcqorder(acqorder, isULOnly);
    }

    private int calcNetworkModeByAcqorder(String acqorder, boolean isULOnly) {
        if (isULOnly) {
            if ("0201".equals(acqorder) || "02".equals(acqorder)) {
                return 2;
            }
            return 12;
        } else if ("0201".equals(acqorder)) {
            return 3;
        } else {
            if ("01".equals(acqorder)) {
                return 1;
            }
            return 9;
        }
    }

    protected int modifyNetworkMode(int oldNetworkMode, boolean removeG) {
        int networkMode = oldNetworkMode;
        if (!removeG) {
            switch (oldNetworkMode) {
                case HwVSimUtilsInner.STATE_EB /*2*/:
                    networkMode = 3;
                    break;
                case HwVSimEventReport.VSIM_PROCESS_TYPE_DB /*12*/:
                    networkMode = 9;
                    break;
                default:
                    break;
            }
        }
        switch (oldNetworkMode) {
            case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                networkMode = 2;
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_GET_NETWORK_TYPE /*9*/:
                networkMode = 12;
                break;
        }
        if (networkMode != oldNetworkMode) {
            setULOnlyProp(Boolean.valueOf(removeG));
        }
        return networkMode;
    }

    protected EnableParam getEnableParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getEnableParam(request);
    }

    protected void setULOnlyProp(Boolean isULOnly) {
        if (this.mVSimController != null) {
            this.mVSimController.setULOnlyProp(isULOnly);
        }
    }

    protected boolean getULOnlyProp() {
        if (this.mVSimController != null) {
            return this.mVSimController.getULOnlyProp();
        }
        return false;
    }

    protected boolean getVSimULOnlyMode() {
        if (this.mVSimController != null) {
            return this.mVSimController.getVSimULOnlyMode();
        }
        return false;
    }

    protected int getUserReservedSubId() {
        if (this.mVSimController != null) {
            return this.mVSimController.getUserReservedSubId();
        }
        return -1;
    }

    public boolean isDirectProcess() {
        if (this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.isDirectProcess();
    }
}
