package com.huawei.internal.telephony.vsim;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import com.android.internal.telephony.HwVSimPhoneFactory;
import com.android.internal.telephony.HwVSimService;
import com.android.internal.telephony.vsim.HwVSimApkObserver;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimControllerUtils;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.IccCardConstantsEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.vsim.util.ArrayUtils;
import com.huawei.internal.util.StateMachineEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class HwVSimBaseController extends StateMachineEx {
    private static final String LOG_TAG = "HwVSimBaseController";
    private static boolean sIsInstantiated;
    private final BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        /* class com.huawei.internal.telephony.vsim.HwVSimBaseController.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            HwVSimBaseController.this.checkIfInAirplaneMode();
        }
    };
    protected int mAlternativeUserReservedSubId = -1;
    protected final HwVSimApkObserver mApkObserver;
    protected int[] mCardTypes;
    protected final CommandsInterfaceEx[] mCis;
    protected final Context mContext;
    protected int mCurCardType;
    protected boolean mDisableFailMark;
    protected int mDisableRetryCount;
    public HwVSimEventReport.VSimEventInfo mEventInfo;
    private boolean mIsRegAirplaneModeReceiver;
    private boolean mIsVsimOn;
    protected long mLastReportTime;
    protected Handler mMainHandler;
    protected HwVSimModemAdapter mModemAdapter;
    protected int mNetworkScanIsRunning = 0;
    protected int mNetworkScanSubId = 0;
    protected int mNetworkScanType = 0;
    protected int mOldCardType;
    protected final PhoneExt[] mPhones;
    private int mPreferredNetworkTypeDisableFlag = 0;
    private int mPreferredNetworkTypeEnableFlag = 0;
    protected HwVSimConstants.ProcessAction mProcessAction;
    protected HwVSimConstants.ProcessState mProcessState;
    protected HwVSimConstants.ProcessType mProcessType;
    private int mRule = -1;
    private int[] mSimSlotsTable;
    private String mSpn;
    protected final int[] mSubStates;
    protected final HwVSimEventReport mVSimEventReport;
    protected HashMap<Integer, String> mWhatToStringMap;
    private final Object subStateLock = new Object();

    public abstract void broadcastVSimCardType();

    public abstract boolean clearTrafficData();

    public abstract int dialupForVSim();

    public abstract boolean disableVSim();

    public abstract void disposeCard(int i);

    public abstract boolean dsFlowCfg(int i, int i2, int i3, int i4);

    /* access modifiers changed from: protected */
    public abstract int enableVSim(HwVSimConstants.EnableParam enableParam);

    public abstract int getCardPresentNumeric(boolean[] zArr);

    public abstract CommandsInterfaceEx getCiBySub(int i);

    public abstract HwVSimSlotSwitchController.CommrilMode getCommrilMode();

    public abstract String getDevSubMode(int i);

    public abstract HwVSimSlotSwitchController.CommrilMode getExpectCommrilMode(int i, int[] iArr);

    public abstract boolean getIsSessionOpen();

    public abstract boolean getIsWaitingNvMatchUnsol();

    public abstract boolean getIsWaitingSwitchCdmaModeSide();

    public abstract String getPendingDeviceInfoFromSP(String str);

    public abstract PhoneExt getPhoneBySub(int i);

    public abstract String getPreferredNetworkTypeForVsim(int i);

    public abstract int getSimStateViaSysinfoEx(int i);

    public abstract String getTrafficData();

    public abstract boolean hasVSimIccCard();

    public abstract boolean isDoingSlotSwitch();

    /* access modifiers changed from: protected */
    public abstract boolean isProcessInit();

    public abstract boolean isSubActivationUpdate();

    public abstract boolean isVSimCauseCardReload();

    public abstract IccCardConstantsEx.StateEx modifySimStateForVsim(int i, IccCardConstantsEx.StateEx stateEx);

    public abstract boolean needBlockPin(int i);

    public abstract boolean needBlockUnReservedForVsim(int i);

    public abstract void processHotPlug(int[] iArr);

    public abstract boolean prohibitSubUpdateSimNoChange(int i);

    public abstract int scanVsimAvailableNetworks(int i, int i2);

    /* access modifiers changed from: protected */
    public abstract int setApn(HwVSimConstants.ApnParams apnParams);

    public abstract void setIsSessionOpen(boolean z);

    public abstract void setMarkForCardReload(int i, boolean z);

    public abstract void setSubActived(int i);

    public abstract void simHotPlugIn(int i);

    public abstract void simHotPlugOut(int i);

    public abstract boolean switchVsimWorkMode(int i);

    public abstract void updateSimCardTypes(int[] iArr);

    protected HwVSimBaseController(Context context, PhoneExt[] phones, CommandsInterfaceEx[] cis) {
        super("VSimController");
        this.mContext = context;
        this.mPhones = phones;
        this.mCis = cis;
        this.mIsVsimOn = false;
        if (context != null) {
            sIsInstantiated = true;
        }
        this.mSubStates = new int[HwVSimModemAdapter.PHONE_COUNT];
        int i = 0;
        while (true) {
            int[] iArr = this.mSubStates;
            if (i < iArr.length) {
                iArr[i] = -1;
                i++;
            } else {
                this.mDisableRetryCount = 3;
                this.mDisableFailMark = false;
                this.mCurCardType = -1;
                this.mOldCardType = -1;
                this.mSimSlotsTable = null;
                this.mCardTypes = null;
                initWhatToStringMap();
                this.mVSimEventReport = new HwVSimEventReport(context);
                this.mEventInfo = new HwVSimEventReport.VSimEventInfo();
                HwVSimEventReport.VSimEventInfoUtils.setCauseType(this.mEventInfo, -1);
                this.mLastReportTime = 0;
                this.mApkObserver = new HwVSimApkObserver();
                return;
            }
        }
    }

    public static boolean isInstantiated() {
        return sIsInstantiated;
    }

    public void broadcastVsimServiceReady() {
        logi("broadcastVsimServiceReady");
        this.mContext.sendBroadcast(new Intent("com.huawei.vsim.action.VSIM_SERVICE_READY"), HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    public int convertSavedNetworkMode(int networkMode) {
        int convertedNetworkMode = networkMode;
        if (networkMode == 4 || networkMode == 5 || networkMode == 6) {
            convertedNetworkMode = 3;
        } else if (networkMode == 8 || networkMode == 63) {
            convertedNetworkMode = 9;
        }
        logd("networkMode : " + networkMode + ", convertedNetworkMode : " + convertedNetworkMode);
        return convertedNetworkMode;
    }

    public int getVsimSlotId() {
        return HwVSimPhoneFactory.getVSimEnabledSubId();
    }

    public boolean isVSimEnabled() {
        return HwVSimPhoneFactory.getVSimEnabledSubId() != -1;
    }

    public boolean isVSimOn() {
        return this.mIsVsimOn;
    }

    public void setIsVSimOn(boolean isVSimOn) {
        this.mIsVsimOn = isVSimOn;
        logi("setIsVSimOn mIsVSimOn = " + this.mIsVsimOn);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (fd != null && pw != null && args != null) {
            HwVSimBaseController.super.dump(fd, pw, args);
            StringBuilder sb = new StringBuilder();
            sb.append(" mSimSlotsTable=");
            int[] iArr = this.mSimSlotsTable;
            sb.append(iArr == null ? "[]" : Arrays.toString(iArr));
            pw.println(sb.toString());
        }
    }

    public boolean isVSimInProcess() {
        HwVSimConstants.ProcessAction processAction = this.mProcessAction;
        if (processAction == null) {
            return false;
        }
        if (processAction.isEnableProcess() || this.mProcessAction.isDisableProcess() || this.mProcessAction.isSwitchModeProcess()) {
            return true;
        }
        return false;
    }

    public int enableVSim(int operation, Bundle bundle) {
        int result = 3;
        if (bundle == null) {
            loge("enableVSim, bundle is null, return fail.");
            return 3;
        }
        String imsi = bundle.getString(HwVSimConstants.ENABLE_PARA_IMSI);
        int cardType = bundle.getInt(HwVSimConstants.ENABLE_PARA_CARDTYPE);
        int apnType = bundle.getInt(HwVSimConstants.ENABLE_PARA_APNTYPE);
        String acqOrder = bundle.getString(HwVSimConstants.ENABLE_PARA_ACQORDER);
        String taPath = bundle.getString(HwVSimConstants.ENABLE_PARA_TAPATH);
        int vSimLoc = bundle.getInt(HwVSimConstants.ENABLE_PARA_VSIMLOC);
        String challenge = bundle.getString(HwVSimConstants.ENABLE_PARA_CHALLENGE);
        int cardInModem1 = bundle.getInt(HwVSimConstants.ENABLE_PARA_CARD_IN_MODEM1, -1);
        int supportVSimCa = bundle.getInt(HwVSimConstants.ENABLE_PARA_BATCH_WAFER, 0);
        logi("enableVSim start, operation : " + operation);
        if (operation != 1) {
            if (operation == 2) {
                HwVSimConstants.ApnParams params = new HwVSimConstants.ApnParams();
                params.imsi = imsi;
                params.cardType = cardType;
                params.apnType = apnType;
                params.taPath = taPath;
                params.challenge = challenge;
                params.isForHash = false;
                params.supportVSimCa = supportVSimCa;
                result = setApn(params);
            } else if (!(operation == 6 || operation == 7)) {
                logd("enableVSim, op is invalid, do nothing");
            }
            logi("enableVSim end, result : " + result);
            return result;
        }
        String spn = bundle.getString(HwVSimConstants.ENABLE_PARA_VSIM_SPN);
        int rule = bundle.getInt(HwVSimConstants.ENABLE_PARA_VSIM_RULE, -1);
        logi("enableVSim, spn:" + spn + " , rule:" + rule);
        if (!(rule == -1 || spn == null)) {
            this.mSpn = spn;
            this.mRule = rule;
        }
        result = enableVSim(new HwVSimConstants.EnableParam(imsi, cardType, apnType, acqOrder, challenge, operation, taPath, vSimLoc, cardInModem1, supportVSimCa));
        logi("enableVSim end, result : " + result);
        return result;
    }

    public String getSpn() {
        logi("getSpn " + this.mSpn);
        return this.mSpn;
    }

    public int getRule() {
        logi("getRule " + this.mRule);
        return this.mRule;
    }

    public int getCardTypeFromEnableParam(HwVSimRequest request) {
        HwVSimConstants.EnableParam param;
        if (request == null || !(request.getArgument() instanceof HwVSimConstants.EnableParam) || (param = (HwVSimConstants.EnableParam) request.getArgument()) == null) {
            return -1;
        }
        return param.cardType;
    }

    public int getVSimCurCardType() {
        int cardType = this.mCurCardType;
        logd("getVSimCurCardType = " + cardType);
        return cardType;
    }

    public void setVSimCurCardType(int cardType) {
        this.mOldCardType = this.mCurCardType;
        this.mCurCardType = cardType;
        logd("setVSimCurCardType, card type is " + this.mCurCardType + ", old card type is " + this.mOldCardType);
        broadcastVSimCardType();
    }

    public int[] getSimSlotTable() {
        int[] iArr = this.mSimSlotsTable;
        return iArr != null ? (int[]) iArr.clone() : new int[0];
    }

    public void setSimSlotTable(int[] slots) {
        if (slots != null) {
            this.mSimSlotsTable = (int[]) slots.clone();
        }
    }

    public int getSimSlotTableLastSlotId() {
        if (ArrayUtils.isEmpty(this.mSimSlotsTable)) {
            return -1;
        }
        int[] iArr = this.mSimSlotsTable;
        return iArr[iArr.length - 1];
    }

    public int getVSimSavedMainSlot() {
        return HwVSimPhoneFactory.getVSimSavedMainSlot();
    }

    public void setVSimSavedMainSlot(int subId) {
        HwVSimPhoneFactory.setVSimSavedMainSlot(subId);
    }

    public HwVSimEventReport.VSimEventInfo getVSimEventInfo() {
        return this.mEventInfo;
    }

    public void saveNetworkMode(int modemId, int modemNetworkMode) {
        int savedNetworkMode = HwVSimPhoneFactory.getVSimSavedNetworkMode(modemId);
        logd("savedNetworkMode = " + savedNetworkMode + " for modemId = " + modemId);
        if (savedNetworkMode == -1) {
            HwVSimPhoneFactory.setVSimSavedNetworkMode(modemId, modemNetworkMode);
        }
    }

    public void syncSubState() {
        Handler handler;
        synchronized (this.subStateLock) {
            for (int i = 0; i < this.mSubStates.length; i++) {
                this.mSubStates[i] = HwTelephonyManager.getDefault().getSubState((long) i);
                logi("update slotId = " + i + " to " + this.mSubStates[i]);
            }
            if (isProcessInit() && (handler = getHandler()) != null) {
                handler.sendMessage(handler.obtainMessage(76));
            }
        }
    }

    public int getSubState(int slotId) {
        int subState;
        synchronized (this.subStateLock) {
            subState = 0;
            if (slotId >= 0) {
                if (slotId < this.mSubStates.length) {
                    subState = this.mSubStates[slotId];
                }
            }
            logd("[SLOT" + slotId + "] getSubState : " + subState);
        }
        return subState;
    }

    public void updateSubState(int slotId, int subState) {
        synchronized (this.subStateLock) {
            if (slotId >= 0) {
                if (slotId < this.mSubStates.length) {
                    this.mSubStates[slotId] = subState;
                    logd("[SLOT" + slotId + "] updateSubState : " + subState);
                    Handler handler = getHandler();
                    if (isProcessInit() && handler != null) {
                        handler.sendMessage(handler.obtainMessage(76));
                    }
                }
            }
        }
    }

    public void broadcastQueryResults(AsyncResultEx ar) {
        this.mNetworkScanIsRunning = 0;
        Intent intent = new Intent("com.huawei.vsim.action.NETWORK_SCAN_COMPLETE");
        intent.addFlags(536870912);
        HwVSimControllerUtils.putNetworkScanOperatorInfoArrayListInIntent(ar, intent, HwVSimConstants.EXTRA_NETWORK_SCAN_OPEARTORINFO);
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_TYPE, this.mNetworkScanType);
        intent.putExtra("subId", this.mNetworkScanSubId);
        logi("type = " + this.mNetworkScanType + ", slotId = " + this.mNetworkScanSubId);
        this.mContext.sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    public HwVSimConstants.EnableParam getEnableParam(HwVSimRequest request) {
        if (request == null) {
            return null;
        }
        Object arg = request.getArgument();
        if (arg instanceof HwVSimConstants.EnableParam) {
            return (HwVSimConstants.EnableParam) arg;
        }
        return null;
    }

    public HwVSimConstants.WorkModeParam getWorkModeParam(HwVSimRequest request) {
        if (request == null) {
            return null;
        }
        Object arg = request.getArgument();
        if (arg instanceof HwVSimConstants.WorkModeParam) {
            return (HwVSimConstants.WorkModeParam) arg;
        }
        return null;
    }

    public int getUserReservedSubId() {
        return HwVSimPhoneFactory.getVSimUserReservedSubId(this.mContext);
    }

    public void updateCardTypes(int[] cardTypes) {
        if (cardTypes != null) {
            logd("updateCardTypes, cardTypes = " + Arrays.toString(cardTypes));
            this.mCardTypes = (int[]) cardTypes.clone();
        }
    }

    public int getInsertedCardCount() {
        int[] iArr = this.mCardTypes;
        if (iArr == null) {
            return 0;
        }
        return getInsertedCardCount((int[]) iArr.clone());
    }

    public int[] getCardTypes() {
        int[] iArr = this.mCardTypes;
        return iArr == null ? new int[0] : (int[]) iArr.clone();
    }

    public int getInsertedCardCount(int[] cardTypes) {
        int cardCount = ArrayUtils.size(cardTypes);
        int insertedCardCount = 0;
        for (int i = 0; i < cardCount; i++) {
            if (cardTypes[i] != 0) {
                insertedCardCount++;
            }
        }
        return insertedCardCount;
    }

    public void setAlternativeUserReservedSubId(int subId) {
        if (HwVSimUtilsInner.isPlatformTwoModemsActual()) {
            this.mAlternativeUserReservedSubId = subId;
        }
    }

    public void logd(String content) {
        HwVSimLog.debug(LOG_TAG, content);
    }

    public void logi(String content) {
        HwVSimLog.info(LOG_TAG, content);
    }

    public void loge(String content) {
        HwVSimLog.error(LOG_TAG, content);
    }

    public String getWhatToString(int what) {
        String result = null;
        HashMap<Integer, String> hashMap = this.mWhatToStringMap;
        if (hashMap != null) {
            result = hashMap.get(Integer.valueOf(what));
        }
        if (result != null) {
            return result;
        }
        return "<unknown message> - " + what;
    }

    private void initWhatToStringMap() {
        this.mWhatToStringMap = new HashMap<>();
        this.mWhatToStringMap.put(2, "EVENT_GET_SIM_STATE_DONE");
        this.mWhatToStringMap.put(5, "EVENT_SLOTSWITCH_INIT_DONE");
        this.mWhatToStringMap.put(40, "CMD_ENABLE_VSIM");
        this.mWhatToStringMap.put(41, "EVENT_RADIO_POWER_OFF_DONE");
        this.mWhatToStringMap.put(42, "EVENT_CARD_POWER_OFF_DONE");
        this.mWhatToStringMap.put(43, "EVENT_SWITCH_SLOT_DONE");
        this.mWhatToStringMap.put(45, "EVENT_CARD_POWER_ON_DONE");
        this.mWhatToStringMap.put(46, "EVENT_RADIO_POWER_ON_DONE");
        this.mWhatToStringMap.put(47, "EVENT_SET_ACTIVE_MODEM_MODE_DONE");
        this.mWhatToStringMap.put(48, "EVENT_GET_PREFERRED_NETWORK_TYPE_DONE");
        this.mWhatToStringMap.put(49, "EVENT_SET_PREFERRED_NETWORK_TYPE_DONE");
        this.mWhatToStringMap.put(50, "EVENT_NETWORK_CONNECTED");
        this.mWhatToStringMap.put(51, "EVENT_ENABLE_VSIM_DONE");
        this.mWhatToStringMap.put(16, "CMD_SET_APDSFLOWCFG");
        this.mWhatToStringMap.put(54, "EVENT_GET_SIM_SLOT_DONE");
        this.mWhatToStringMap.put(56, "EVENT_QUERY_CARD_TYPE_DONE");
        this.mWhatToStringMap.put(57, "EVENT_ENABLE_VSIM_FINISH");
        this.mWhatToStringMap.put(65, "EVENT_VSIM_PLMN_SELINFO");
        this.mWhatToStringMap.put(66, "EVENT_SET_NETWORK_RAT_AND_SRVDOMAIN_DONE");
        this.mWhatToStringMap.put(71, "EVENT_NETWORK_CONNECT_TIMEOUT");
        this.mWhatToStringMap.put(72, "EVENT_CMD_INTERRUPT");
        this.mWhatToStringMap.put(3, "EVENT_ICC_CHANGED");
        this.mWhatToStringMap.put(17, "EVENT_SET_APDSFLOWCFG_DONE");
        this.mWhatToStringMap.put(18, "CMD_SET_DSFLOWNVCFG");
        this.mWhatToStringMap.put(19, "EVENT_SET_DSFLOWNVCFG_DONE");
        this.mWhatToStringMap.put(12, "CMD_CLEAR_TRAFFICDATA");
        this.mWhatToStringMap.put(13, "EVENT_CLEAR_TRAFFICDATA_DONE");
        this.mWhatToStringMap.put(14, "CMD_GET_TRAFFICDATA");
        this.mWhatToStringMap.put(15, "EVENT_GET_TRAFFICDATA_DONE");
        this.mWhatToStringMap.put(58, "CMD_SWITCH_WORKMODE");
        this.mWhatToStringMap.put(52, "CMD_DISABLE_VSIM");
        this.mWhatToStringMap.put(53, "EVENT_DISABLE_VSIM_DONE");
        this.mWhatToStringMap.put(59, "EVENT_SWITCH_WORKMODE_DONE");
        this.mWhatToStringMap.put(60, "EVENT_SWITCH_WORKMODE_FINISH");
        this.mWhatToStringMap.put(61, "EVENT_CARD_RELOAD_TIMEOUT");
        this.mWhatToStringMap.put(24, "EVENT_NETWORK_SCAN_COMPLETED");
        this.mWhatToStringMap.put(75, "EVENT_INITIAL_TIMEOUT");
        this.mWhatToStringMap.put(76, "EVENT_INITIAL_SUBSTATE_DONE");
        this.mWhatToStringMap.put(77, "EVENT_INITIAL_UPDATE_CARDTYPE");
        this.mWhatToStringMap.put(79, "EVENT_GET_ICC_STATUS_DONE");
        this.mWhatToStringMap.put(80, "EVENT_SET_CDMA_MODE_SIDE_DONE");
        this.mWhatToStringMap.put(81, "EVENT_JUDGE_RESTART_RILD_NV_MATCH");
        this.mWhatToStringMap.put(82, "EVENT_JUDGE_RESTART_RILD_NV_MATCH_TIMEOUT");
        this.mWhatToStringMap.put(83, "EVENT_RADIO_AVAILABLE");
        this.mWhatToStringMap.put(84, "CMD_RESTART_RILD_FOR_NV_MATCH");
        this.mWhatToStringMap.put(22, "CMD_GET_SIM_STATE_VIA_SYSINFOEX");
        this.mWhatToStringMap.put(23, "EVENT_GET_SIM_STATE_VIA_SYSINFOEX");
        this.mWhatToStringMap.put(85, "EVENT_GET_ICC_STATUS_DONE_FOR_GET_CARD_COUNT");
        this.mWhatToStringMap.put(86, "EVENT_ICC_STATUS_CHANGED_FOR_CARD_COUNT");
        this.mWhatToStringMap.put(87, "EVENT_ICC_STATUS_CHANGED_FOR_CARD_COUNT_TIMEOUT");
        this.mWhatToStringMap.put(94, "EVENT_CMD_ENABLE_EXTERNAL_SIM");
        this.mWhatToStringMap.put(95, "EVENT_ENABLE_EXTERNAL_SIM_DONE");
        this.mWhatToStringMap.put(96, "EVENT_CMD_NOTIFY_PLUG_IN");
        this.mWhatToStringMap.put(97, "EVENT_NOTIFY_PLUG_IN_DONE");
        this.mWhatToStringMap.put(98, "EVENT_CMD_DISABLE_EXTERNAL_SIM");
        this.mWhatToStringMap.put(99, "EVENT_DISABLE_EXTERNAL_SIM_DONE");
        this.mWhatToStringMap.put(100, "EVENT_CMD_NOTIFY_PLUG_OUT");
        this.mWhatToStringMap.put(Integer.valueOf((int) HwVSimConstants.EVENT_NOTIFY_PLUG_OUT_DONE), "EVENT_NOTIFY_PLUG_OUT_DONE");
        this.mWhatToStringMap.put(Integer.valueOf((int) HwVSimConstants.EVENT_SET_MAIN_SLOT_DONE), "EVENT_SET_MAIN_SLOT_DONE");
        this.mWhatToStringMap.put(Integer.valueOf((int) HwVSimConstants.EVENT_SERVICE_STATE_CHANGE), "EVENT_SERVICE_STATE_CHANGE");
    }

    public void setProcessType(HwVSimConstants.ProcessType type) {
        this.mProcessType = type;
    }

    public void setProcessAction(HwVSimConstants.ProcessAction action) {
        this.mProcessAction = action;
    }

    public void setProcessState(HwVSimConstants.ProcessState state) {
        this.mProcessState = state;
    }

    public int getVSimOccupiedSlotId() {
        int result;
        int slotId = getVsimSlotId();
        int reservedSub = getUserReservedSubId();
        if (slotId == -1) {
            result = -1;
        } else {
            int i = this.mAlternativeUserReservedSubId;
            if (i != -1) {
                result = HwVSimUtilsInner.getAnotherSlotId(i);
            } else if (reservedSub != -1) {
                result = HwVSimUtilsInner.getAnotherSlotId(reservedSub);
            } else {
                result = -1;
            }
        }
        logd("getVSimOccupiedSlotId, slotId:" + slotId + " m:" + this.mAlternativeUserReservedSubId + " return:" + result);
        return result;
    }

    public void registerAirplaneModeReceiver() {
        logi("register air plane mode receiver.");
        if (!checkIfInAirplaneMode() && !this.mIsRegAirplaneModeReceiver) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.AIRPLANE_MODE");
            this.mContext.registerReceiver(this.mAirplaneModeReceiver, filter);
            this.mIsRegAirplaneModeReceiver = true;
        }
    }

    public void unregisterAirplaneModeReceiver() {
        logi("unregister air plane mode receiver.");
        if (this.mIsRegAirplaneModeReceiver) {
            this.mContext.unregisterReceiver(this.mAirplaneModeReceiver);
            this.mIsRegAirplaneModeReceiver = false;
        }
    }

    public void onAirplaneModeOn() {
        logi("onAirplaneModeOn, try to dispose service");
        HwVSimService.dispose();
        unregisterAirplaneModeReceiver();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkIfInAirplaneMode() {
        boolean isAirplaneModeOn = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1) {
            isAirplaneModeOn = true;
        }
        logi("mAirplaneModeReceiver, isAirplaneModeOn: " + isAirplaneModeOn);
        if (isAirplaneModeOn) {
            Handler handler = this.mMainHandler;
            handler.sendMessage(handler.obtainMessage(93));
        }
        return isAirplaneModeOn;
    }

    public boolean isEnableProhibitByDisableRetry() {
        if (HwVSimPhoneFactory.getVSimUserEnabled() == 1) {
            return false;
        }
        return this.mDisableFailMark;
    }

    public void allowDefaultData() {
        if (isVSimOn()) {
            logi("vsim is on, no need to restore default data");
            return;
        }
        setInteralDataForDSDS(0);
        setInteralDataForDSDS(1);
    }

    private void setInteralDataForDSDS(int dataSlotId) {
        logd("setInteralDataForDSDS data sub: " + dataSlotId);
        if (dataSlotId < 0 || dataSlotId >= this.mPhones.length) {
            logd("data sub invalid");
            return;
        }
        PhoneExt phone = getPhoneBySub(dataSlotId);
        if (phone == null) {
            logd("phone not found");
            return;
        }
        phone.setInternalDataEnabled(true);
        logd("call set internal data on sub: " + dataSlotId);
    }

    public int getPreferredNetworkTypeEnableFlag() {
        return this.mPreferredNetworkTypeEnableFlag;
    }

    public void setPreferredNetworkTypeEnableFlag(int flag) {
        this.mPreferredNetworkTypeEnableFlag = flag;
        logd("mPreferredNetworkTypeEnableFlag = " + flag);
    }

    public int getPreferredNetworkTypeDisableFlag() {
        return this.mPreferredNetworkTypeDisableFlag;
    }

    public void setPreferredNetworkTypeDisableFlag(int flag) {
        this.mPreferredNetworkTypeDisableFlag = flag;
        logd("mPreferredNetworkTypeDisableFlag = " + flag);
    }

    /* access modifiers changed from: protected */
    public Object sendRequest(int command, Object argument, int slotId) {
        return HwVSimRequest.sendRequest(getHandler(), command, argument, slotId);
    }

    /* access modifiers changed from: protected */
    public <T> T sendRequest(int command, Object argument, int slotId, Class<T> resultClz) {
        T t = (T) HwVSimRequest.sendRequest(getHandler(), command, argument, slotId);
        if (resultClz.isInstance(t)) {
            return t;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("sendRequest result:");
        sb.append(t == null ? "<NULL>" : t.getClass().getCanonicalName());
        sb.append(" can not cast DescClz:");
        sb.append(resultClz);
        loge(sb.toString());
        return null;
    }

    /* access modifiers changed from: protected */
    public String getCallingAppName() {
        List<ActivityManager.RunningAppProcessInfo> appProcessList;
        int callingPid = Binder.getCallingPid();
        ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
        if (am == null || (appProcessList = am.getRunningAppProcesses()) == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == callingPid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public boolean setApDsFlowCfg(int slotId, int config, int threshold, int totalThreshold, int oper) {
        logd("setApDsFlowCfg");
        return ((Boolean) sendRequest(16, new int[]{config, threshold, totalThreshold, oper}, slotId)).booleanValue();
    }

    public boolean setDsFlowNvCfg(int slotId, int enable, int interval) {
        logd("setDsFlowNvCfg, enable = " + enable);
        return ((Boolean) sendRequest(18, new int[]{enable, interval}, slotId)).booleanValue();
    }

    public boolean isNeedBroadcastVSimAbsentState() {
        return false;
    }
}
