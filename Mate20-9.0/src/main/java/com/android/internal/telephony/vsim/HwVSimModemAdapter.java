package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.process.HwVSimProcessor;
import java.util.Arrays;

public abstract class HwVSimModemAdapter {
    public static final boolean IS_FAST_SWITCH_SIMSLOT = SystemProperties.getBoolean("ro.config.fast_switch_simslot", false);
    private static final String LOG_TAG = "ModemController";
    public static final int MAX_SUB_COUNT = (PHONE_COUNT + 1);
    public static final int PHONE_COUNT = TelephonyManager.getDefault().getPhoneCount();
    public static final String PROPERTY_COMMRIL_MODE = "persist.radio.commril_mode";
    private static final int SUB_COUNT_CROSS;
    private static final int SUB_COUNT_SWAP;
    protected CommandsInterface[] mCis;
    protected Context mContext;
    protected CommandsInterface mVSimCi;
    protected HwVSimController mVSimController;

    public static class ExpectPara {
        private HwVSimSlotSwitchController.CommrilMode expectCommrilMode;
        private int expectSlot;

        public int getExpectSlot() {
            return this.expectSlot;
        }

        public HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode() {
            return this.expectCommrilMode;
        }

        public void setExpectSlot(int expect) {
            this.expectSlot = expect;
        }

        public void setExpectCommrilMode(HwVSimSlotSwitchController.CommrilMode expect) {
            this.expectCommrilMode = expect;
        }
    }

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

        public String toString() {
            return "SimStateInfo{simIndex=" + this.simIndex + ", simEnable=" + this.simEnable + ", simSub=" + this.simSub + ", simNetInfo=" + this.simNetInfo + '}';
        }
    }

    public abstract void checkDisableSimCondition(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void checkEnableSimCondition(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void checkSwitchModeSimCondition(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void doDisableStateExit(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void doEnableStateEnter(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract int getAllAbilityNetworkTypeOnModem1(boolean z);

    public abstract void getModemSupportVSimVersion(HwVSimProcessor hwVSimProcessor, int i);

    public abstract void getModemSupportVSimVersionInner(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract int getPoffSubForEDWork(HwVSimRequest hwVSimRequest);

    public abstract void getSimState(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract boolean isNeedRadioOnM2();

    /* access modifiers changed from: protected */
    public abstract void logd(String str);

    public abstract void onCardPowerOffDoneInEWork(HwVSimProcessor hwVSimProcessor, int i);

    public abstract void onEDWorkTransitionState(HwVSimProcessor hwVSimProcessor);

    public abstract void onGetModemSupportVSimVersionDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onGetSimSlotDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract SimStateInfo onGetSimStateDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onQueryCardTypeDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onRadioPowerOffDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onRadioPowerOffSlaveModemDone(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void onSetNetworkRatAndSrvdomainDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onSimHotPlugOut();

    public abstract void onSwitchCommrilDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void onSwitchSlotDone(HwVSimProcessor hwVSimProcessor, AsyncResult asyncResult);

    public abstract void radioPowerOff(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void setActiveModemMode(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest, int i);

    public abstract void switchSimSlot(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    static {
        int i = 2;
        if (PHONE_COUNT <= 2) {
            i = PHONE_COUNT;
        }
        SUB_COUNT_SWAP = i;
        int i2 = 3;
        if (MAX_SUB_COUNT <= 3) {
            i2 = MAX_SUB_COUNT;
        }
        SUB_COUNT_CROSS = i2;
    }

    protected HwVSimModemAdapter(HwVSimController vsimController, Context context, CommandsInterface vsimCi, CommandsInterface[] cis) {
        this.mVSimController = vsimController;
        this.mContext = context;
        this.mVSimCi = vsimCi;
        this.mCis = cis;
    }

    public CommandsInterface getCiBySub(int subId) {
        return HwVSimUtilsInner.getCiBySub(subId, this.mVSimCi, this.mCis);
    }

    public int restoreSavedNetworkMode(int modemId) {
        int networkMode;
        int networkMode2 = HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, modemId);
        int savedNetworkMode = HwVSimPhoneFactory.getVSimSavedNetworkMode(modemId);
        if (savedNetworkMode != -1) {
            networkMode = this.mVSimController.convertSavedNetworkMode(savedNetworkMode);
        } else {
            networkMode = this.mVSimController.convertSavedNetworkMode(networkMode2);
        }
        logd("restoreSavedNetworkMode modemId = " + modemId + " networkMode = " + networkMode + " savedNetworkMode = " + savedNetworkMode);
        return networkMode;
    }

    public void saveNetworkTypeToDB(int subId, int setPrefMode) {
        HwNetworkTypeUtils.saveNetworkModeToDB(this.mContext, subId, setPrefMode);
        logd("=VSIM= save network mode " + setPrefMode + " for subId : " + subId + "to database success!");
    }

    public void saveM0NetworkMode(int mode) {
    }

    public void getRegPlmn(HwVSimProcessor processor, HwVSimRequest request) {
        int subId = request.mSubId;
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.getRegPlmn(processor.obtainMessage(11, request));
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
        int subId = request.mSubId;
        Message onCompleted = processor.obtainMessage(17, request);
        int[] paramApds = (int[]) request.getArgument();
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null && paramApds != null && paramApds.length >= 4) {
            ci.setApDsFlowCfg(paramApds[0], paramApds[1], paramApds[2], paramApds[3], onCompleted);
        }
    }

    public void setDsFlowNvCfg(HwVSimProcessor processor, HwVSimRequest request) {
        int subId = request.mSubId;
        int[] paramDs = (int[]) request.getArgument();
        Message onCompleted = processor.obtainMessage(19, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null && paramDs != null && paramDs.length >= 2) {
            ci.setDsFlowNvCfg(paramDs[0], paramDs[1], onCompleted);
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

    public void onGetPreferredNetworkTypeDone(HwVSimProcessor processor, AsyncResult ar, int modemId) {
        int modemNetworkMode = ((int[]) ar.result)[0];
        logd("modemNetworkMode = " + modemNetworkMode + " for modemId: " + modemId);
        saveNetworkMode(modemId, modemNetworkMode);
    }

    public int parseModemSupportVSimVersionResult(HwVSimProcessor processor, AsyncResult ar) {
        int modemVer;
        if (processor == null || ar == null) {
            logd("parseModemSupportVSimVersionResult, param is null !");
            return -1;
        }
        if (ar.exception != null) {
            if (processor.isRequestNotSupport(ar.exception)) {
                modemVer = -2;
                logd("parse modem vsim version failed for request not support");
            } else {
                modemVer = -1;
                logd("parse modem vsim version failed, exception: " + ar.exception);
            }
        } else if (ar.result == null || ((int[]) ar.result).length <= 0) {
            modemVer = -1;
            logd("the result of modem vsim version is null");
        } else {
            modemVer = ((int[]) ar.result)[0];
        }
        return modemVer;
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

    public void getPreferredNetworkType(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(48, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.getPreferredNetworkType(onCompleted);
        }
    }

    public void setPreferredNetworkType(HwVSimProcessor processor, HwVSimRequest request, int subId, int networkMode) {
        Message onCompleted = null;
        if (!(processor == null || request == null)) {
            request.mSubId = subId;
            onCompleted = processor.obtainMessage(49, request);
        }
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.setPreferredNetworkType(networkMode, onCompleted);
        }
        if (2 == subId) {
            saveM0NetworkMode(networkMode);
        }
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
        cardPowerOff(subId, simIndex, processor.obtainMessage(42, request));
    }

    /* access modifiers changed from: package-private */
    public void cardPowerOff(int subId, int simIndex, Message onCompleted) {
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
        Message onCompleted = null;
        if (!(request == null || processor == null)) {
            request.mSubId = subId;
            onCompleted = processor.obtainMessage(46, request);
        }
        if (!HwVSimUtilsInner.IS_DSDSPOWER_SUPPORT || subId == 2 || this.mVSimController.getSubState(subId) != 0) {
            if (this.mContext != null) {
                boolean isAirplaneMode = false;
                if (Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
                    isAirplaneMode = true;
                }
                if (isAirplaneMode) {
                    if (onCompleted != null) {
                        AsyncResult.forMessage(onCompleted);
                        onCompleted.sendToTarget();
                    }
                    logd("radioPowerOn: airplane mode is on.");
                    return;
                }
            }
            CommandsInterface ci = getCiBySub(subId);
            getPhoneBySub(subId).getServiceStateTracker().setDesiredPowerState(true);
            if (ci != null) {
                ci.setRadioPower(true, onCompleted);
            }
            return;
        }
        if (onCompleted != null) {
            AsyncResult.forMessage(onCompleted);
            onCompleted.sendToTarget();
        }
        logd("radioPowerOn: not active, subId =" + subId);
    }

    public void radioPowerOff(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(41, request);
        CommandsInterface ci = getCiBySub(subId);
        getPhoneBySub(subId).getServiceStateTracker().setDesiredPowerState(false);
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
        setHwVSimPowerOnOff(isVSimOnM0 ? 2 : subId, true);
    }

    public void setHwVSimPowerOff(HwVSimProcessor processor, HwVSimRequest request) {
        int subId = request.getMainSlot();
        logd("set VSim power off after disable, subId: " + subId);
        setHwVSimPowerOnOff(subId, false);
    }

    public void setHwVSimPowerOnOff(int subId, boolean bPowerOn) {
        logd("set VSim power on off, subId: " + subId + ", bPowerOn: " + bPowerOn);
        int power = bPowerOn;
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.setHwVSimPower((int) power, null);
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

    /* access modifiers changed from: protected */
    public boolean handleCheckAirPlaneMode(HwVSimProcessor processor, HwVSimRequest request) {
        boolean isAirplaneMode = false;
        if (this.mContext != null) {
            boolean z = false;
            if (Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
                z = true;
            }
            isAirplaneMode = z;
        }
        boolean isVSimOn = this.mVSimController.getIsVSimOn();
        logd("isAirplaneMode = " + isAirplaneMode + "; isVSimOn = " + isVSimOn);
        if (isVSimOn || !isAirplaneMode) {
            return false;
        }
        processor.doProcessException(null, request);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleCheckVSimIsOn(HwVSimProcessor processor, HwVSimRequest request) {
        boolean isVSimOn = this.mVSimController.getIsVSimOn();
        HwVSimController.EnableParam param = this.mVSimController.getEnableParam(request);
        boolean isNeedSwitchHardSim = param != null && HwVSimUtilsInner.isValidSlotId(param.cardInModem1);
        if (!isVSimOn || isNeedSwitchHardSim) {
            return false;
        }
        logd("preparing hot process");
        processor.setProcessType(HwVSimController.ProcessType.PROCESS_TYPE_DIRECT);
        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.mEventInfo, 4);
        int[] subs = getSimSlotTable();
        if (subs.length == 0) {
            processor.doProcessException(null, request);
        }
        request.setSubs(subs);
        processor.transitionToState(3);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleCheckRebootOrNormal(HwVSimProcessor processor, HwVSimRequest request) {
        logd("preparing cold process");
        getAllCardTypes(processor, request);
        return true;
    }

    /* access modifiers changed from: protected */
    public void saveNetworkMode(int modemId, int modemNetworkMode) {
        this.mVSimController.saveNetworkMode(modemId, modemNetworkMode);
    }

    /* access modifiers changed from: protected */
    public void setSimSlotTable(int[] slots) {
        this.mVSimController.setSimSlotTable(slots);
    }

    /* access modifiers changed from: protected */
    public int[] getSimSlotTable() {
        return this.mVSimController.getSimSlotTable();
    }

    /* access modifiers changed from: protected */
    public void setVSimSavedMainSlot(int subId) {
        this.mVSimController.setVSimSavedMainSlot(subId);
    }

    /* access modifiers changed from: protected */
    public int getVSimSavedMainSlot() {
        return this.mVSimController.getVSimSavedMainSlot();
    }

    /* access modifiers changed from: protected */
    public HwVSimSlotSwitchController.CommrilMode getCommrilMode() {
        return this.mVSimController.getCommrilMode();
    }

    /* access modifiers changed from: protected */
    public HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode(int mainSlot, int[] cardType) {
        return this.mVSimController.getExpectCommrilMode(mainSlot, cardType);
    }

    /* access modifiers changed from: protected */
    public void switchCommrilMode(HwVSimSlotSwitchController.CommrilMode expectCommrilMode, int expectSlot, int mainSlot, boolean isVSimOn, Message onCompleteMsg) {
        this.mVSimController.switchCommrilMode(expectCommrilMode, expectSlot, mainSlot, isVSimOn, onCompleteMsg);
    }

    /* access modifiers changed from: protected */
    public Phone getPhoneBySub(int subId) {
        return this.mVSimController.getPhoneBySub(subId);
    }

    private int[] fillSubSwap(int mainSlot) {
        int[] subs = new int[SUB_COUNT_SWAP];
        int sharedSubId = mainSlot;
        int i = 0;
        if (0 < subs.length) {
            logd("fillSubSwap : sub[" + 0 + "] = " + sharedSubId);
            subs[0] = sharedSubId;
            i = 0 + 1;
        }
        if (i < subs.length) {
            logd("fillSubSwap : sub[" + i + "] = " + 2);
            subs[i] = 2;
            int i2 = i + 1;
        }
        return subs;
    }

    private int[] fillSubCross(int mainSlot) {
        int[] subs = new int[SUB_COUNT_CROSS];
        int sharedSubId = mainSlot;
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        int i = 0;
        if (0 < subs.length) {
            logd("fillSubCross : sub[" + 0 + "] = " + sharedSubId);
            subs[0] = sharedSubId;
            i = 0 + 1;
        }
        if (i < subs.length) {
            logd("fillSubCross : sub[" + i + "] = " + slaveSlot);
            subs[i] = slaveSlot;
            i++;
        }
        if (i < subs.length) {
            logd("fillSubCross : sub[" + i + "] = " + 2);
            subs[i] = 2;
            int i2 = i + 1;
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

    public void registerIccStatusChangedForGetCardCount(int subId, Handler handler) {
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            logd("registerIccStatusChangedForGetCardCount subId:" + subId);
            ci.registerForIccStatusChanged(handler, 86, Integer.valueOf(subId));
        }
    }

    public void unregisterIccStatusChangedForGetCardCount(int subId, Handler handler) {
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            logd("unregisterIccStatusChangedForGetCardCount subId:" + subId);
            ci.unregisterForIccStatusChanged(handler);
        }
    }

    public void getIccCardStatusForGetCardCount(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        request.mSubId = subId;
        logd("getIccCardStatus subId:" + subId);
        Message onCompleted = processor.obtainMessage(85, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.getIccCardStatus(onCompleted);
        }
    }

    public void setCdmaModeSide(HwVSimProcessor processor, HwVSimRequest request, int subId, int modemID) {
        request.mSubId = subId;
        Message onCompleted = processor.obtainMessage(80, request);
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.setCdmaModeSide(modemID, onCompleted);
        }
    }

    /* access modifiers changed from: package-private */
    public void getIMSI(int subId) {
        CommandsInterface ci = getCiBySub(subId);
        if (ci != null) {
            ci.getIMSI(null);
        }
    }

    public void getRadioCapability() {
        if (HwVSimSlotSwitchController.IS_FAST_SWITCH_SIMSLOT) {
            for (int subId = 0; subId < PHONE_COUNT; subId++) {
                Phone phone = getPhoneBySub(subId);
                if (phone == null) {
                    logd("getRadioCapability: active phone not found, return.");
                    return;
                }
                CommandsInterface ci = getCiBySub(subId);
                if (ci == null) {
                    logd("getRadioCapability: ci is null, return.");
                    return;
                }
                logd("getRadioCapability: get radio capability for subId: " + subId);
                ci.getRadioCapability(phone.obtainMessage(35));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public HwVSimSlotSwitchController.CommrilMode getAssumedCommrilMode(int mainSlot, int[] cardTypes) {
        HwVSimSlotSwitchController.CommrilMode assumedCommrilMode;
        if (HwVSimUtilsInner.isChinaTelecom()) {
            assumedCommrilMode = HwVSimSlotSwitchController.CommrilMode.getCLGMode();
        } else {
            assumedCommrilMode = getExpectCommrilMode(mainSlot, cardTypes);
        }
        if (assumedCommrilMode == HwVSimSlotSwitchController.CommrilMode.NON_MODE) {
            assumedCommrilMode = getCommrilMode();
        }
        logd("getAssumedCommrilMode: assumedCommrilMode = " + assumedCommrilMode);
        return assumedCommrilMode;
    }

    /* access modifiers changed from: package-private */
    public boolean calcIsNeedSwitchCommrilMode(HwVSimSlotSwitchController.CommrilMode expect, HwVSimSlotSwitchController.CommrilMode current) {
        if (expect == HwVSimSlotSwitchController.CommrilMode.NON_MODE) {
            return false;
        }
        if (expect != current) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public int getExpectSlotForDisable(int[] cardTypes, int mainSlot, int savedMainSlot) {
        logd("getExpectSlotForDisable cardTypes = " + Arrays.toString(cardTypes) + " mainSlot = " + mainSlot + " savedMainSlot = " + savedMainSlot);
        int insertedCardCount = HwVSimUtilsInner.getInsertedCardCount(cardTypes);
        StringBuilder sb = new StringBuilder();
        sb.append("getExpectSlotForDisable: inserted card count = ");
        sb.append(insertedCardCount);
        logd(sb.toString());
        int slaveSlot = HwVSimUtilsInner.getAnotherSlotId(mainSlot);
        if (insertedCardCount == 0) {
            return savedMainSlot;
        }
        if (insertedCardCount == 1) {
            return slaveSlot;
        }
        int savedMainSlotSubState = this.mVSimController.getSubState(savedMainSlot);
        int anotherSlot = HwVSimUtilsInner.getAnotherSlotId(savedMainSlot);
        return (this.mVSimController.getSubState(anotherSlot) == 0 || savedMainSlotSubState != 0) ? getExpectSlotForDisableForCmcc(HwFullNetworkManager.getInstance().getDefaultMainSlotByIccId(savedMainSlot)) : anotherSlot;
    }

    /* access modifiers changed from: package-private */
    public int getExpectSlotForDisableForCmcc(int expectSlot) {
        if (!HwVSimUtilsInner.IS_CMCC_4GSWITCH_DISABLE) {
            return expectSlot;
        }
        if (HwFullNetworkManager.getInstance().isCMCCHybird()) {
            int i = 0;
            if (!HwFullNetworkManager.getInstance().isCMCCCardBySlotId(0)) {
                i = 1;
            }
            int cmccCardIndex = i;
            if (this.mVSimController.getSubState(cmccCardIndex) != 0) {
                expectSlot = cmccCardIndex;
            }
            logd("getExpectSlotForDisableForCmcc expectSlot = " + expectSlot);
        }
        return expectSlot;
    }
}
