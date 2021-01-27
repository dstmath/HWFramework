package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.process.HwVSimProcessor;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.vsim.HwVSimBaseController;
import java.util.Arrays;

public abstract class HwVSimModemAdapter {
    public static final boolean IS_FAST_SWITCH_SIMSLOT = SystemPropertiesEx.getBoolean("ro.config.fast_switch_simslot", false);
    private static final String LOG_TAG = "ModemController";
    public static final int MAX_SUB_COUNT = (PHONE_COUNT + 1);
    public static final int PHONE_COUNT = TelephonyManagerEx.getDefault().getPhoneCount();
    public static final String PROPERTY_COMMRIL_MODE = "persist.radio.commril_mode";
    private static final int SUB_COUNT_CROSS;
    private static final int SUB_COUNT_SWAP;
    protected CommandsInterfaceEx[] mCis;
    protected Context mContext;
    protected CommandsInterfaceEx mVSimCi;
    protected HwVSimBaseController mVSimController;

    public abstract void checkDisableSimCondition(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void checkEnableSimCondition(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void checkSwitchModeSimCondition(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void doDisableStateExit(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void doEnableStateEnter(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract int getAllAbilityNetworkTypeOnModem1(boolean z);

    public abstract int getPoffSubForEDWork(HwVSimRequest hwVSimRequest);

    public abstract void getSimState(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract boolean isNeedRadioOnM2();

    /* access modifiers changed from: protected */
    public abstract void logd(String str);

    public abstract void onCardPowerOffDoneInEWork(HwVSimProcessor hwVSimProcessor, int i);

    public abstract void onEDWorkTransitionState(HwVSimProcessor hwVSimProcessor);

    public abstract void onGetSimSlotDone(HwVSimProcessor hwVSimProcessor, AsyncResultEx asyncResultEx);

    public abstract SimStateInfo onGetSimStateDone(AsyncResultEx asyncResultEx);

    public abstract void onQueryCardTypeDone(HwVSimProcessor hwVSimProcessor, AsyncResultEx asyncResultEx);

    public abstract void onRadioPowerOffDone(HwVSimProcessor hwVSimProcessor, AsyncResultEx asyncResultEx);

    public abstract void onRadioPowerOffSlaveModemDone(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void onSetNetworkRatAndSrvdomainDone(HwVSimProcessor hwVSimProcessor, AsyncResultEx asyncResultEx);

    public abstract void onSimHotPlugOut();

    public abstract void onSwitchCommrilDone(HwVSimProcessor hwVSimProcessor, AsyncResultEx asyncResultEx);

    public abstract void onSwitchSlotDone(HwVSimProcessor hwVSimProcessor, AsyncResultEx asyncResultEx);

    public abstract void radioPowerOff(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    public abstract void setActiveModemMode(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest, int i);

    public abstract void switchSimSlot(HwVSimProcessor hwVSimProcessor, HwVSimRequest hwVSimRequest);

    static {
        int i = PHONE_COUNT;
        if (i > 2) {
            i = 2;
        }
        SUB_COUNT_SWAP = i;
        int i2 = MAX_SUB_COUNT;
        if (i2 > 3) {
            i2 = 3;
        }
        SUB_COUNT_CROSS = i2;
    }

    protected HwVSimModemAdapter(HwVSimBaseController vsimController, Context context, CommandsInterfaceEx vsimCi, CommandsInterfaceEx[] cis) {
        this.mVSimController = vsimController;
        this.mContext = context;
        this.mVSimCi = vsimCi;
        this.mCis = cis;
    }

    public CommandsInterfaceEx getCiBySub(int subId) {
        return HwVSimUtilsInner.getCiBySub(subId, this.mVSimCi, this.mCis);
    }

    public int restoreSavedNetworkMode(int modemId) {
        int networkMode;
        int networkMode2 = HwTelephonyManagerInner.getDefault().getNetworkModeFromDB(modemId);
        int savedNetworkMode = HwVSimPhoneFactory.getVSimSavedNetworkMode(modemId);
        if (savedNetworkMode != -1) {
            networkMode = this.mVSimController.convertSavedNetworkMode(savedNetworkMode);
        } else {
            networkMode = this.mVSimController.convertSavedNetworkMode(networkMode2);
        }
        logd("restoreSavedNetworkMode modemId = " + modemId + " networkMode = " + networkMode + " savedNetworkMode = " + savedNetworkMode);
        return networkMode;
    }

    public void saveNetworkTypeToDB(int slotId, int setPrefMode) {
        HwTelephonyManagerInner.getDefault().saveNetworkModeToDB(slotId, setPrefMode);
        logd("=VSIM= save network mode " + setPrefMode + " for slotId : " + slotId + "to database success!");
    }

    public void saveM0NetworkMode(int mode) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "preferred_network_mode2", mode);
        logd("=VSIM= save network mode " + mode + " for vsim to database success!");
    }

    public void networksScan(HwVSimProcessor processor, int slotId) {
        Message onCompleted = null;
        if (processor != null) {
            onCompleted = processor.obtainMessage(24, null);
        }
        CommandsInterfaceEx ci = getCiBySub(slotId);
        if (ci != null) {
            ci.getAvailableNetworks(onCompleted);
        }
    }

    public void getTrafficData(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor != null && request != null) {
            Message onCompleted = processor.obtainMessage(15, request);
            CommandsInterfaceEx ci = getCiBySub(request.mSubId);
            if (ci != null) {
                ci.getTrafficData(onCompleted);
            }
        }
    }

    public void clearTrafficData(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor != null && request != null) {
            Message onCompleted = processor.obtainMessage(13, request);
            CommandsInterfaceEx ci = getCiBySub(request.mSubId);
            if (ci != null) {
                ci.clearTrafficData(onCompleted);
            }
        }
    }

    public void setApDsFlowCfg(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor != null && request != null) {
            int subId = request.mSubId;
            Message onCompleted = processor.obtainMessage(17, request);
            int[] paramApds = (int[]) request.getArgument();
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null && paramApds != null && paramApds.length >= 4) {
                ci.setApDsFlowCfg(paramApds[0], paramApds[1], paramApds[2], paramApds[3], onCompleted);
            }
        }
    }

    public void setDsFlowNvCfg(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor != null && request != null) {
            int subId = request.mSubId;
            int[] paramDs = (int[]) request.getArgument();
            Message onCompleted = processor.obtainMessage(19, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null && paramDs != null && paramDs.length >= 2) {
                ci.setDsFlowNvCfg(paramDs[0], paramDs[1], onCompleted);
            }
        }
    }

    public void getSimStateViaSysinfoEx(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor != null && request != null) {
            Message onCompleted = processor.obtainMessage(23, request);
            CommandsInterfaceEx commandsInterfaceEx = this.mVSimCi;
            if (commandsInterfaceEx != null) {
                commandsInterfaceEx.getSimStateViaSysinfoEx(onCompleted);
            }
        }
    }

    public void getDevSubMode(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor != null && request != null) {
            Message onCompleted = processor.obtainMessage(26, request);
            CommandsInterfaceEx commandsInterfaceEx = this.mVSimCi;
            if (commandsInterfaceEx != null) {
                commandsInterfaceEx.getDevSubMode(onCompleted);
            }
        }
    }

    public void getPreferredNetworkTypeVSim(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor != null && request != null) {
            Message onCompleted = processor.obtainMessage(28, request);
            CommandsInterfaceEx ci = getCiBySub(request.mSubId);
            if (ci != null) {
                ci.getPreferredNetworkType(onCompleted);
            }
        }
    }

    public void onGetPreferredNetworkTypeDone(AsyncResultEx ar, int modemId) {
        if (ar != null) {
            int modemNetworkMode = ((int[]) ar.getResult())[0];
            logd("modemNetworkMode = " + modemNetworkMode + " for modemId: " + modemId);
            saveNetworkMode(modemId, modemNetworkMode);
        }
    }

    public void getSimSlot(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            Message onCompleted = processor.obtainMessage(54, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null) {
                ci.getBalongSim(onCompleted);
            }
        }
    }

    public void getSimState(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            Message onCompleted = processor.obtainMessage(2, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null) {
                ci.getSimState(onCompleted);
            }
        }
    }

    public void getPreferredNetworkType(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            Message onCompleted = processor.obtainMessage(48, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null) {
                ci.getPreferredNetworkType(onCompleted);
            }
        }
    }

    public void setPreferredNetworkType(HwVSimProcessor processor, HwVSimRequest request, int subId, int networkMode) {
        Message onCompleted = null;
        if (!(processor == null || request == null)) {
            request.mSubId = subId;
            onCompleted = processor.obtainMessage(49, request);
        }
        CommandsInterfaceEx ci = getCiBySub(subId);
        if (ci != null) {
            ci.setPreferredNetworkType(networkMode, onCompleted);
        }
        if (2 == subId) {
            saveM0NetworkMode(networkMode);
        }
    }

    public void cardPowerOn(HwVSimProcessor processor, HwVSimRequest request, int subId, int simIndex) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            Message onCompleted = processor.obtainMessage(45, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null) {
                ci.setSimState(simIndex, 1, onCompleted);
            }
            if (subId == 2 && simIndex == 11) {
                this.mVSimController.setIsVSimOn(true);
                logd("cardPowerOn setIsVSimOn : true");
            }
        }
    }

    public void cardPowerOff(HwVSimProcessor processor, HwVSimRequest request, int subId, int simIndex) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            cardPowerOff(subId, simIndex, processor.obtainMessage(42, request));
        }
    }

    /* access modifiers changed from: package-private */
    public void cardPowerOff(int subId, int simIndex, Message onCompleted) {
        CommandsInterfaceEx ci = getCiBySub(subId);
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
            Context context = this.mContext;
            if (context != null) {
                boolean isAirplaneMode = false;
                if (Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0) {
                    isAirplaneMode = true;
                }
                if (isAirplaneMode) {
                    if (onCompleted != null) {
                        AsyncResultEx.forMessage(onCompleted);
                        onCompleted.sendToTarget();
                    }
                    logd("radioPowerOn: airplane mode is on.");
                    return;
                }
            }
            CommandsInterfaceEx ci = getCiBySub(subId);
            PhoneExt phone = getPhoneBySub(subId);
            if (phone != null) {
                phone.getServiceStateTracker().setDesiredPowerState(true);
            }
            if (ci != null) {
                ci.setRadioPower(true, onCompleted);
                return;
            }
            return;
        }
        if (onCompleted != null) {
            AsyncResultEx.forMessage(onCompleted);
            onCompleted.sendToTarget();
        }
        logd("radioPowerOn: not active, subId =" + subId);
    }

    public void radioPowerOff(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            Message onCompleted = processor.obtainMessage(41, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            PhoneExt phone = getPhoneBySub(subId);
            if (phone != null) {
                phone.getServiceStateTracker().setDesiredPowerState(false);
            }
            if (ci != null) {
                ci.setRadioPower(false, onCompleted);
            }
        }
    }

    public void getCardTypes(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            Message onCompleted = processor.obtainMessage(56, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null) {
                ci.queryCardType(onCompleted);
            }
        }
    }

    public void getAllCardTypes(HwVSimProcessor processor, HwVSimRequest request) {
        if (!(processor == null || request == null)) {
            for (int slotId = 0; slotId < PHONE_COUNT; slotId++) {
                request.setGotCardType(slotId, false);
                request.setCardType(slotId, 0);
                getCardTypes(processor, request.clone(), slotId);
            }
        }
    }

    public void setNetworkRatAndSrvdomain(HwVSimProcessor processor, HwVSimRequest request, int subId, int rat, int srvDomain) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            Message onCompleted = processor.obtainMessage(66, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null) {
                ci.setNetworkRatAndSrvDomainCfg(rat, srvDomain, onCompleted);
            }
        }
    }

    public void setHwVSimPowerOn(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor != null && request != null) {
            int subId = request.getMainSlot();
            boolean isVSimOnM0 = request.getIsVSimOnM0();
            logd("set VSim power on before enable, isVSimOnM0: " + isVSimOnM0 + ", subId: " + subId);
            setHwVSimPowerOnOff(isVSimOnM0 ? 2 : subId, true);
        }
    }

    public void setHwVSimPowerOff(HwVSimRequest request) {
        if (request != null) {
            int subId = request.getMainSlot();
            logd("set VSim power off after disable, subId: " + subId);
            setHwVSimPowerOnOff(subId, false);
        }
    }

    public void setHwVSimPowerOnOff(int subId, boolean bPowerOn) {
        logd("set VSim power on off, subId: " + subId + ", bPowerOn: " + bPowerOn);
        CommandsInterfaceEx ci = getCiBySub(subId);
        if (ci != null) {
            ci.setHwVSimPower(bPowerOn ? 1 : 0, (Message) null);
        }
    }

    public void handleSubSwapProcess(HwVSimProcessor processor, HwVSimRequest request) {
        if (processor != null && request != null) {
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
    }

    public void checkVSimCondition(HwVSimProcessor processor, HwVSimRequest request) {
        if (!handleCheckAirPlaneMode(processor, request) && !handleCheckVSimIsOn(processor, request) && !handleCheckRebootOrNormal(processor, request)) {
            logd("check vsim condition, but do nothing.");
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleCheckAirPlaneMode(HwVSimProcessor processor, HwVSimRequest request) {
        boolean z = false;
        if (processor == null || request == null) {
            return false;
        }
        boolean isAirplaneMode = false;
        Context context = this.mContext;
        if (context != null) {
            if (Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0) {
                z = true;
            }
            isAirplaneMode = z;
        }
        boolean isVSimOn = this.mVSimController.isVSimOn();
        logd("isAirplaneMode = " + isAirplaneMode + "; isVSimOn = " + isVSimOn);
        if (isVSimOn || !isAirplaneMode) {
            return false;
        }
        processor.doProcessException(null, request);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean handleCheckVSimIsOn(HwVSimProcessor processor, HwVSimRequest request) {
        boolean isNeedSwitchHardSim = false;
        if (processor == null || request == null) {
            return false;
        }
        boolean isVSimOn = this.mVSimController.isVSimOn();
        HwVSimConstants.EnableParam param = this.mVSimController.getEnableParam(request);
        if (param != null && HwVSimUtilsInner.isValidSlotId(param.cardInModem1)) {
            isNeedSwitchHardSim = true;
        }
        if (!isVSimOn || isNeedSwitchHardSim) {
            return false;
        }
        logd("preparing hot process");
        processor.setProcessType(HwVSimConstants.ProcessType.PROCESS_TYPE_DIRECT);
        HwVSimEventReport.VSimEventInfoUtils.setPocessType(this.mVSimController.getVSimEventInfo(), 4);
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
        if (processor == null || request == null) {
            return false;
        }
        logd("preparing cold process");
        getAllCardTypes(processor, request);
        return true;
    }

    /* access modifiers changed from: protected */
    public void saveNetworkMode(int modemId, int modemNetworkMode) {
        this.mVSimController.saveNetworkMode(modemId, modemNetworkMode);
    }

    /* access modifiers changed from: protected */
    public int[] getSimSlotTable() {
        return this.mVSimController.getSimSlotTable();
    }

    /* access modifiers changed from: protected */
    public void setSimSlotTable(int[] slots) {
        this.mVSimController.setSimSlotTable(slots);
    }

    /* access modifiers changed from: protected */
    public int getVSimSavedMainSlot() {
        return this.mVSimController.getVSimSavedMainSlot();
    }

    /* access modifiers changed from: protected */
    public void setVSimSavedMainSlot(int subId) {
        this.mVSimController.setVSimSavedMainSlot(subId);
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
    public PhoneExt getPhoneBySub(int subId) {
        return this.mVSimController.getPhoneBySub(subId);
    }

    private int[] fillSubSwap(int mainSlot) {
        int[] subs = new int[SUB_COUNT_SWAP];
        int index = 0;
        if (0 < subs.length) {
            logd("fillSubSwap : sub[0] = " + mainSlot);
            subs[0] = mainSlot;
            index = 0 + 1;
        }
        if (index < subs.length) {
            logd("fillSubSwap : sub[" + index + "] = 2");
            int i = index + 1;
            subs[index] = 2;
        }
        return subs;
    }

    private int[] fillSubCross(int mainSlot) {
        int[] subs = new int[SUB_COUNT_CROSS];
        int slaveSlot = mainSlot == 0 ? 1 : 0;
        int i = 0;
        if (0 < subs.length) {
            logd("fillSubCross : sub[0] = " + mainSlot);
            subs[0] = mainSlot;
            i = 0 + 1;
        }
        if (i < subs.length) {
            logd("fillSubCross : sub[" + i + "] = " + slaveSlot);
            subs[i] = slaveSlot;
            i++;
        }
        if (i < subs.length) {
            logd("fillSubCross : sub[" + i + "] = 2");
            int i2 = i + 1;
            subs[i] = 2;
        }
        return subs;
    }

    public void getIccCardStatus(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            logd("getIccCardStatus subId:" + subId);
            Message onCompleted = processor.obtainMessage(79, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null) {
                ci.getIccCardStatus(onCompleted);
            }
        }
    }

    public void registerIccStatusChangedForGetCardCount(int subId, Handler handler) {
        CommandsInterfaceEx ci = getCiBySub(subId);
        if (ci != null) {
            logd("registerIccStatusChangedForGetCardCount subId:" + subId);
            ci.registerForIccStatusChanged(handler, 86, Integer.valueOf(subId));
        }
    }

    public void unregisterIccStatusChangedForGetCardCount(int subId, Handler handler) {
        CommandsInterfaceEx ci = getCiBySub(subId);
        if (ci != null) {
            logd("unregisterIccStatusChangedForGetCardCount subId:" + subId);
            ci.unregisterForIccStatusChanged(handler);
        }
    }

    public void getIccCardStatusForGetCardCount(HwVSimProcessor processor, HwVSimRequest request, int subId) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            logd("getIccCardStatus subId:" + subId);
            Message onCompleted = processor.obtainMessage(85, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null) {
                ci.getIccCardStatus(onCompleted);
            }
        }
    }

    public void setCdmaModeSide(HwVSimProcessor processor, HwVSimRequest request, int subId, int modemID) {
        if (processor != null && request != null) {
            request.mSubId = subId;
            Message onCompleted = processor.obtainMessage(80, request);
            CommandsInterfaceEx ci = getCiBySub(subId);
            if (ci != null) {
                ci.setCdmaModeSide(modemID, onCompleted);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getIMSI(int subId) {
        CommandsInterfaceEx ci = getCiBySub(subId);
        if (ci != null) {
            ci.getIMSI((Message) null);
        }
    }

    public void getRadioCapability() {
        if (HwVSimSlotSwitchController.IS_FAST_SWITCH_SIMSLOT) {
            for (int subId = 0; subId < PHONE_COUNT; subId++) {
                PhoneExt phone = getPhoneBySub(subId);
                if (phone == null) {
                    logd("getRadioCapability: active phone not found, return.");
                    return;
                }
                CommandsInterfaceEx ci = getCiBySub(subId);
                if (ci == null) {
                    logd("getRadioCapability: ci is null, return.");
                    return;
                }
                logd("getRadioCapability: get radio capability for subId: " + subId);
                ci.getRadioCapability(phone.obtainMessage(35));
            }
        }
    }

    public void openChipSession(HwVSimProcessor processor, HwVSimRequest request, int slotId) {
        if (processor != null && request != null) {
            request.mSubId = slotId;
            Message msg = processor.obtainMessage(88, request);
            CommandsInterfaceEx ci = getCiBySub(slotId);
            if (ci != null) {
                ci.sendMutiChipSessionConfig(1, msg);
                this.mVSimController.setIsSessionOpen(true);
            }
        }
    }

    public void closeChipSession(int slotId) {
        CommandsInterfaceEx ci = getCiBySub(slotId);
        if (ci != null && this.mVSimController.getIsSessionOpen()) {
            logd("close chip session for " + slotId);
            ci.sendMutiChipSessionConfig(0, (Message) null);
            this.mVSimController.setIsSessionOpen(false);
        }
    }

    public void sendVsimDataToModem(HwVSimProcessor processor, HwVSimRequest request, int slotId) {
        if (processor != null && request != null) {
            request.mSubId = slotId;
            Message msg = processor.obtainMessage(91, request);
            CommandsInterfaceEx ci = getCiBySub(slotId);
            if (ci != null) {
                ci.sendVsimDataToModem(msg);
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
        if (expect == HwVSimSlotSwitchController.CommrilMode.NON_MODE || !HwVSimSlotSwitchController.IS_HISI_CDMA_SUPPORTED || expect == current) {
            return false;
        }
        return true;
    }

    /* JADX INFO: Multiple debug info for r2v4 int: [D('savedMainSlotSubState' int), D('expectSlot' int)] */
    /* access modifiers changed from: protected */
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
        if (this.mVSimController.getSubState(anotherSlot) == 0 || savedMainSlotSubState != 0) {
            return getExpectSlotForDisableForCmcc(HwFullNetworkManager.getInstance().getDefaultMainSlotByIccId(savedMainSlot));
        }
        return anotherSlot;
    }

    /* access modifiers changed from: package-private */
    public int getExpectSlotForDisableForCmcc(int expectSlot) {
        if (!HwFullNetworkManager.getInstance().isCMCCDsdxDisable()) {
            return expectSlot;
        }
        if (HwFullNetworkManager.getInstance().isCMCCHybird()) {
            int cmccCardIndex = !HwFullNetworkManager.getInstance().isCMCCCardBySlotId(0) ? 1 : 0;
            if (this.mVSimController.getSubState(cmccCardIndex) != 0) {
                expectSlot = cmccCardIndex;
            }
            logd("getExpectSlotForDisableForCmcc expectSlot = " + expectSlot);
        }
        return expectSlot;
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

    public static class ExpectPara {
        private HwVSimSlotSwitchController.CommrilMode expectCommrilMode;
        private int expectSlot;

        public int getExpectSlot() {
            return this.expectSlot;
        }

        public void setExpectSlot(int expect) {
            this.expectSlot = expect;
        }

        public HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode() {
            return this.expectCommrilMode;
        }

        public void setExpectCommrilMode(HwVSimSlotSwitchController.CommrilMode expect) {
            this.expectCommrilMode = expect;
        }
    }
}
