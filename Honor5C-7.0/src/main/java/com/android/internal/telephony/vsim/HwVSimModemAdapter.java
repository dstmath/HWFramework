package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.vsim.HwVSimController.ProcessType;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController.CommrilMode;
import com.android.internal.telephony.vsim.process.HwVSimProcessor;

public abstract class HwVSimModemAdapter {
    private static final String LOG_TAG = "ModemController";
    public static final int MAX_SUB_COUNT = 0;
    public static final int PHONE_COUNT = 0;
    private static final int SUB_COUNT_CROSS = 0;
    private static final int SUB_COUNT_SWAP = 0;
    protected CommandsInterface[] mCis;
    protected Context mContext;
    protected CommandsInterface mVSimCi;
    protected HwVSimController mVSimController;

    public static class SimStateInfo {
        public int simEnable;
        public int simIndex;
        public int simNetInfo;
        public int simSub;

        public SimStateInfo(int index, int enable, int sub, int netInfo) {
            this.simIndex = index;
            this.simEnable = enable;
            this.simSub = sub;
            this.simNetInfo = netInfo;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimModemAdapter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimModemAdapter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimModemAdapter.<clinit>():void");
    }

    public abstract void checkDisableSimCondition(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void checkEnableSimCondition(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void checkSwitchModeSimCondition(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void doDisableStateExit(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void doEnableStateEnter(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void getModemSupportVSimVersion(HwVSimProcessor hwVSimProcessor, int i);

    public abstract int getPoffSubForEDWork(HwVSimRequest hwVSimRequest);

    public abstract void getSimState(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract boolean isDoneAllSwitchSlot(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract boolean isNeedRadioOnM2();

    protected abstract void logd(String str);

    public abstract void onEDWorkTransitionState(HwVSimProcessor hwVSimProcessor);

    public abstract void onGetModemSupportVSimVersionDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onGetSimSlotDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract SimStateInfo onGetSimStateDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onQueryCardTypeDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onRadioPowerOffDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onReconnectGetSimSlotDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onSetNetworkRatAndSrvdomainDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onSwitchCommrilDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onSwitchSlotDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void radioPowerOff(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void setActiveModemMode(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest, int i);

    public abstract void switchSimSlot(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    protected HwVSimModemAdapter(HwVSimController vsimController, Context context, CommandsInterface vsimCi, CommandsInterface[] cis) {
        this.mVSimController = vsimController;
        this.mContext = context;
        this.mVSimCi = vsimCi;
        this.mCis = cis;
    }

    public CommandsInterface getCiBySub(int subId) {
        return HwVSimUtilsInner.getCiBySub(subId, this.mVSimCi, this.mCis);
    }

    public int restoreSavedNetworkMode(HwVSimProcessor processor) {
        int networkMode = Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode", 9);
        int savedNetworkMode = HwVSimPhoneFactory.getVSimSavedNetworkMode();
        if (savedNetworkMode != -1) {
            networkMode = this.mVSimController.convertSavedNetworkMode(savedNetworkMode);
            HwVSimPhoneFactory.setVSimSavedNetworkMode(-1);
        } else {
            networkMode = this.mVSimController.convertSavedNetworkMode(networkMode);
        }
        logd("restoreSavedNetworkMode networkMode = " + networkMode + " savedNetworkMode = " + savedNetworkMode);
        return networkMode;
    }

    public void saveM0NetworkMode(int mode) {
    }

    public void getRegPlmn(HwVSimProcessor processor, HwVSimRequest request) {
        int subId = request.mSubId;
        Message onCompleted = processor.obtainMessage(11, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.getOperator(onCompleted);
            return;
        }
        logd("ci[" + subId + "] is null");
        processor.doProcessException(null, request);
    }

    public void networksScan(HwVSimProcessor processor) {
        Message onCompleted = processor.obtainMessage(24, null);
        if (this.mVSimCi != null) {
            this.mVSimCi.getAvailableNetworks(onCompleted);
        }
    }

    public void getTrafficData(HwVSimProcessor processor, HwVSimRequest request) {
        Message onCompleted = processor.obtainMessage(15, request);
        if (this.mVSimCi != null) {
            this.mVSimCi.getTrafficData(onCompleted);
        }
    }

    public void clearTrafficData(HwVSimProcessor processor, HwVSimRequest request) {
        Message onCompleted = processor.obtainMessage(13, request);
        if (this.mVSimCi != null) {
            this.mVSimCi.clearTrafficData(onCompleted);
        }
    }

    public void setApDsFlowCfg(HwVSimProcessor processor, HwVSimRequest request) {
        Message onCompleted = processor.obtainMessage(17, request);
        int[] paramApds = (int[]) request.getArgument();
        if (this.mVSimCi != null) {
            this.mVSimCi.setApDsFlowCfg(paramApds[0], paramApds[1], paramApds[2], paramApds[3], onCompleted);
        }
    }

    public void setDsFlowNvCfg(HwVSimProcessor processor, HwVSimRequest request) {
        int[] paramDs = (int[]) request.getArgument();
        Message onCompleted = processor.obtainMessage(19, request);
        if (this.mVSimCi != null) {
            this.mVSimCi.setDsFlowNvCfg(paramDs[0], paramDs[1], onCompleted);
        }
    }

    public void getSimStateViaSysinfoEx(HwVSimProcessor processor, HwVSimRequest request) {
        Message onCompleted = processor.obtainMessage(23, request);
        if (this.mVSimCi != null) {
            this.mVSimCi.getSimStateViaSysinfoEx(onCompleted);
        }
    }

    public void getDevSubMode(HwVSimProcessor processor, HwVSimRequest request) {
        Message onCompleted = processor.obtainMessage(26, request);
        if (this.mVSimCi != null) {
            this.mVSimCi.getDevSubMode(onCompleted);
        }
    }

    public void getPreferredNetworkTypeVSim(HwVSimProcessor processor, HwVSimRequest request) {
        Message onCompleted = processor.obtainMessage(28, request);
        if (this.mVSimCi != null) {
            this.mVSimCi.getPreferredNetworkType(onCompleted);
        }
    }

    public void onGetPreferredNetworkTypeDone(HwVSimProcessor processor, AsyncResult ar) {
        int modemNetworkMode = ((int[]) ar.result)[0];
        logd("modemNetworkMode = " + modemNetworkMode);
        saveNetworkMode(modemNetworkMode);
    }

    public void getSimSlot(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(54, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.getBalongSim(onCompleted);
        }
    }

    public void getSimState(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(2, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.getSimState(onCompleted);
        }
    }

    public void setTeeDataReady(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(44, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci == null) {
            return;
        }
        if (processor.isEnableProcess() || processor.isReconnectProcess() || processor.isSwitchModeProcess()) {
            ci.setTEEDataReady(1, 1, 1, onCompleted);
        } else if (processor.isDisableProcess()) {
            ci.setTEEDataReady(1, 0, 0, onCompleted);
        }
    }

    public void setApnReady(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(21, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.setTEEDataReady(1, 0, 0, onCompleted);
        }
    }

    public void getPreferredNetworkType(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(48, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.getPreferredNetworkType(onCompleted);
        }
    }

    public void setPreferredNetworkType(HwVSimProcessor processor, HwVSimRequest request, int subId, int networkMode) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(49, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.setPreferredNetworkType(networkMode, onCompleted);
        }
        saveM0NetworkMode(networkMode);
    }

    public void cardPowerOn(HwVSimProcessor processor, HwVSimRequest request, int subId, int simIndex) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(45, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.setSimState(simIndex, 1, onCompleted);
        }
        if (subId == 2 && simIndex == 11) {
            this.mVSimController.setIsVSimOn(true);
            logd("cardPowerOn setIsVSimOn : true");
        }
    }

    public void cardPowerOff(HwVSimProcessor processor, HwVSimRequest request, int subId, int simIndex) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(42, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.setSimState(simIndex, 0, onCompleted);
        }
        if (subId == 2 && simIndex == 11) {
            this.mVSimController.setIsVSimOn(false);
            logd("cardPowerOff setIsVSimOn : false");
        }
    }

    public void radioPowerOn(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(46, request);
        CommandsInterface ci = getCiBySub(subId);
        ((GsmCdmaPhone) getPhoneBySub(subId)).getServiceStateTracker().setDesiredPowerState(true);
        if (ci != null) {
            ci.setRadioPower(true, onCompleted);
        }
    }

    public void radioPowerOff(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(41, request);
        CommandsInterface ci = getCiBySub(subId);
        ((GsmCdmaPhone) getPhoneBySub(subId)).getServiceStateTracker().setDesiredPowerState(false);
        if (ci != null) {
            ci.setRadioPower(false, onCompleted);
        }
    }

    public void getCardTypes(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(56, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.queryCardType(onCompleted);
        }
    }

    public void getAllCardTypes(HwVSimProcessor processor, HwVSimRequest request) {
        for (int i = 0; i < PHONE_COUNT; i++) {
            request.setGotCardType(i, false);
            request.setCardType(i, 0);
            getCardTypes(processor, request.clone(), i);
        }
    }

    public void setNetworkRatAndSrvdomain(HwVSimProcessor processor, HwVSimRequest request, int subId, int rat, int srvDomain) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(66, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.setNetworkRatAndSrvDomainCfg(rat, srvDomain, onCompleted);
        }
    }

    public void setHwVSimPowerOn(HwVSimProcessor processor, HwVSimRequest request) {
        int subId = request.getMainSlot();
        boolean isVSimOnM0 = request.getIsVSimOnM0();
        logd("set VSim power on before enable, isVSimOnM0: " + isVSimOnM0 + ", subId: " + subId);
        if (isVSimOnM0) {
            subId = 2;
        }
        setHwVSimPowerOnOff(subId, true);
    }

    public void setHwVSimPowerOff(HwVSimProcessor processor, HwVSimRequest request) {
        int subId = request.getMainSlot();
        logd("set VSim power off after disable, subId: " + subId);
        setHwVSimPowerOnOff(subId, false);
    }

    public void setHwVSimPowerOnOff(int subId, boolean bPowerOn) {
        logd("set VSim power on off, subId: " + subId + ", bPowerOn: " + bPowerOn);
        int power = bPowerOn ? 1 : 0;
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.setHwVSimPower(power, null);
        }
    }

    public void hvCheckCard(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        Message onCompleted = processor.obtainMessage(70, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.hvCheckCard(onCompleted);
        }
    }

    public void handleSubSwapProcess(HwVSimProcessor processor, HwVSimRequest request) {
        int[] subs = null;
        if (processor.isSwapProcess()) {
            logd("isSwapProcess getMainSlot=" + request.getMainSlot());
            subs = fillSubSwap(request.getMainSlot());
        } else if (processor.isCrossProcess()) {
            logd("isCrossProcess getMainSlot=" + request.getMainSlot());
            subs = fillSubCross(request.getMainSlot());
        }
        request.setSubs(subs);
    }

    public void checkVSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        if (!handleCheckAirPlaneMode(processor, request) && !handleCheckVSimIsOn(processor, request) && !handleCheckRebootOrNormal(processor, request)) {
            logd("check vsim condition, but do nothing.");
        }
    }

    protected boolean handleCheckAirPlaneMode(HwVSimProcessor processor, HwVSimRequest request) {
        boolean isAirplaneMode = false;
        if (this.mContext != null) {
            isAirplaneMode = System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
        }
        boolean isVSimOn = this.mVSimController.getIsVSimOn();
        logd("isAirplaneMode = " + isAirplaneMode + "; isVSimOn = " + isVSimOn);
        if (isVSimOn || !isAirplaneMode) {
            return false;
        }
        processor.doProcessException(null, request);
        return true;
    }

    protected boolean handleCheckVSimIsOn(HwVSimProcessor processor, HwVSimRequest request) {
        if (!this.mVSimController.getIsVSimOn()) {
            return false;
        }
        logd("preparing hot process");
        processor.setProcessType(ProcessType.PROCESS_TYPE_DIRECT);
        VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 4);
        int[] subs = getSimSlotTable();
        if (subs.length == 0) {
            processor.doProcessException(null, request);
        }
        request.setSubs(subs);
        processor.transitionToState(3);
        return true;
    }

    protected boolean handleCheckRebootOrNormal(HwVSimProcessor processor, HwVSimRequest request) {
        logd("preparing cold process");
        getAllCardTypes(processor, request);
        return true;
    }

    protected void saveNetworkMode(int modemNetworkMode) {
        this.mVSimController.saveNetworkMode(modemNetworkMode);
    }

    protected void setSimSlotTable(int[] slots) {
        this.mVSimController.setSimSlotTable(slots);
    }

    protected int[] getSimSlotTable() {
        return this.mVSimController.getSimSlotTable();
    }

    protected void setVSimSavedMainSlot(int subId) {
        this.mVSimController.setVSimSavedMainSlot(subId);
    }

    protected int getVSimSavedMainSlot() {
        return this.mVSimController.getVSimSavedMainSlot();
    }

    protected CommrilMode getCommrilMode() {
        return this.mVSimController.getCommrilMode();
    }

    protected CommrilMode getExpectCommrilMode(int mainSlot, int[] cardType) {
        return this.mVSimController.getExpectCommrilMode(mainSlot, cardType);
    }

    protected int getChinaTelecomMainSlot(int[] cardType) {
        return this.mVSimController.getChinaTelecomMainSlot(cardType);
    }

    protected boolean getVSimULOnlyMode() {
        return this.mVSimController.getVSimULOnlyMode();
    }

    protected void switchCommrilMode(CommrilMode expectCommrilMode, int expectSlot, int mainSlot, boolean isVSimOn, Message onCompleteMsg) {
        this.mVSimController.switchCommrilMode(expectCommrilMode, expectSlot, mainSlot, isVSimOn, onCompleteMsg);
    }

    protected Phone getPhoneBySub(int subId) {
        return this.mVSimController.getPhoneBySub(subId);
    }

    private int[] fillSubSwap(int mainSlot) {
        int[] subs = new int[SUB_COUNT_SWAP];
        int sharedSubId = mainSlot;
        int i = 0;
        if (subs.length > 0) {
            logd("fillSubSwap : sub[" + 0 + "] = " + mainSlot);
            i = 1;
            subs[0] = mainSlot;
        }
        if (i < subs.length) {
            logd("fillSubSwap : sub[" + i + "] = " + 2);
            int i2 = i + 1;
            subs[i] = 2;
            i = i2;
        }
        return subs;
    }

    private int[] fillSubCross(int mainSlot) {
        int[] subs = new int[SUB_COUNT_CROSS];
        int sharedSubId = mainSlot;
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        int i = 0;
        if (subs.length > 0) {
            logd("fillSubCross : sub[" + 0 + "] = " + mainSlot);
            i = 1;
            subs[0] = mainSlot;
        }
        if (i < subs.length) {
            logd("fillSubCross : sub[" + i + "] = " + slaveSlot);
            int i2 = i + 1;
            subs[i] = slaveSlot;
            i = i2;
        }
        if (i < subs.length) {
            logd("fillSubCross : sub[" + i + "] = " + 2);
            i2 = i + 1;
            subs[i] = 2;
            i = i2;
        }
        return subs;
    }

    public void getIccCardStatus(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        logd("getIccCardStatus subId:" + subId);
        Message onCompleted = processor.obtainMessage(79, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.getIccCardStatus(onCompleted);
        }
    }
}
