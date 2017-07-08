package com.android.internal.telephony.intelligentdataswitch;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkFactory;
import android.os.Looper;
import android.os.Message;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.text.TextUtils;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class IDSManager extends StateMachine {
    private static final String TAG = "IDSManager ";
    private int mAbnormalReason;
    private int mAbnormalSlotId;
    private Context mContext;
    private boolean mDataSwitchBroadcastSend;
    private int mDataSwitchForbiddenInterval;
    private int mDefaultDataSlotWeakCount;
    private DsmDefaultState mDefaultState;
    private int mGoodEcioForCdma;
    private int mGoodRscpForWandTD;
    private int mGoodRsrpForLte;
    private int mGoodRsrqForLte;
    private int mGoodRssiForCdma;
    private int mGoodRssiForEvdo;
    private int mGoodSignalStrengthForGsm;
    private int mGoodSnrForEvdo;
    private DsmHaltingState mHaltingState;
    private IntelligentDataSwitch mIDS;
    private DsmIdleState mIdleState;
    private IDSInfoRecord mIdsInfoRecord;
    private int mPdpDisconnectExcpRecoverTimer;
    private int mPdpSetupFailureRecoverTimer;
    private Timer mSignalWeakTimer;
    private boolean mStayInForbiddenStateBeforeTimeUp;
    private DsmSwitchForbiddenState mSwitchForbiddenState;
    private DsmSwitchOngoingState mSwitchOngoingState;
    private DsmSwitchTriggeredState mSwitchTriggeredState;
    private Timer mVoiceCallBackGroundTimer;
    private boolean mVoiceCallUiGoBackGround;
    private int mWeakEcioForCdma;
    private int mWeakPsrpForLte;
    private int mWeakRscpForWandTD;
    private int mWeakRsrqForLte;
    private int mWeakRssiForCdma;
    private int mWeakRssiForEvdo;
    private int mWeakSignalStrengthForGsm;
    private int mWeakSnrForEvdo;

    private class DsmDefaultState extends State {
        private DsmDefaultState() {
        }

        public void enter() {
            IDSManager.this.logd("DsmDefaultState: enter-------------------------------------------");
        }

        public void exit() {
            IDSManager.this.logd("DsmDefaultState: exit");
            IDSManager.this.logd("");
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            IDSManager.this.logd("DsmDefaultState msg=" + msg.what);
            switch (msg.what) {
                case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                    IDSManager.this.mIdsInfoRecord.updataUserDefaultDataSlot(msg.arg1);
                    break;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                    IDSManager.this.mIdsInfoRecord.saveServiceState(msg.arg1, (ServiceState) msg.obj);
                    break;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_CARD_POWER_ON /*7*/:
                    IDSManager.this.mIdsInfoRecord.saveSignalStrength(msg.arg1, (SignalStrength) msg.obj);
                    break;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_NETWORK_TYPE /*10*/:
                    IDSManager.this.mIdsInfoRecord.setIdsState(((Boolean) msg.obj).booleanValue());
                    break;
                case HwVSimEventReport.VSIM_PROCESS_TYPE_DB /*12*/:
                    IDSManager.this.mIdsInfoRecord.setAirplaneState(((Boolean) msg.obj).booleanValue());
                    break;
                case HwVSimEventReport.VSIM_PROCESS_TYPE_DC /*13*/:
                    IDSManager.this.mIdsInfoRecord.setWifiState(((Boolean) msg.obj).booleanValue());
                    break;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_DISABLE_VSIM_DONE /*14*/:
                    IDSManager.this.mIdsInfoRecord.setUserDcState(((Boolean) msg.obj).booleanValue());
                    break;
                case HwVSimConstants.EVENT_SET_APDSFLOWCFG_DONE /*17*/:
                    IDSManager.this.mIdsInfoRecord.setVoiceCallStatus(msg.arg1, msg.arg2);
                    if (IDSManager.this.mVoiceCallBackGroundTimer != null || !IDSManager.this.mIdsInfoRecord.isOnlyDataSubInVoiceCall()) {
                        if (!IDSManager.this.mIdsInfoRecord.getVoicecallStatus(IDSManager.this.mIdsInfoRecord.getUserDefaultDataSlot())) {
                            IDSManager.this.stopVoiceCallIntoBackGroundTimer();
                            break;
                        }
                    }
                    IDSManager.this.startVoiceCallIntoBackGroundTimer(IDSConstants.SIGNAL_MAX_WEAK_TIMER);
                    break;
                    break;
                default:
                    IDSManager.this.loge("DsmDefaultState: ignore msg.what = " + msg.what);
                    retVal = false;
                    break;
            }
            if (!(IDSManager.this.isIntelligentDataSwitchValid() || IDSManager.this.getCurrentState() == IDSManager.this.mHaltingState)) {
                IDSManager.this.transitionTo(IDSManager.this.mHaltingState);
            }
            return retVal;
        }
    }

    private class DsmHaltingState extends State {
        private DsmHaltingState() {
        }

        public void enter() {
            IDSManager.this.logd("DsmHaltingState: enter-------------------------------------------");
            IDSManager.this.resetDataSwitchForbiddenInterval();
            IDSManager.this.stopTriggerDataSwitch();
        }

        public void exit() {
            IDSManager.this.logd("DsmHaltingState: exit");
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            IDSManager.this.logd("DsmHaltingState msg=" + msg.what);
            switch (msg.what) {
                case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                    IDSManager.this.mIdsInfoRecord.saveServiceState(msg.arg1, (ServiceState) msg.obj);
                    break;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_NETWORK_TYPE /*10*/:
                    IDSManager.this.mIdsInfoRecord.setIdsState(((Boolean) msg.obj).booleanValue());
                    break;
                case HwVSimEventReport.VSIM_PROCESS_TYPE_DB /*12*/:
                    IDSManager.this.mIdsInfoRecord.setAirplaneState(((Boolean) msg.obj).booleanValue());
                    break;
                case HwVSimEventReport.VSIM_PROCESS_TYPE_DC /*13*/:
                    IDSManager.this.mIdsInfoRecord.setWifiState(((Boolean) msg.obj).booleanValue());
                    break;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_DISABLE_VSIM_DONE /*14*/:
                    IDSManager.this.mIdsInfoRecord.setUserDcState(((Boolean) msg.obj).booleanValue());
                    break;
                default:
                    IDSManager.this.loge("DsmHaltingState: ignore msg.what = " + msg.what);
                    retVal = false;
                    break;
            }
            if (IDSManager.this.isIntelligentDataSwitchValid()) {
                IDSManager.this.logd("DsmHaltingState: transition To DsmIdleState");
                IDSManager.this.transitionTo(IDSManager.this.mIdleState);
            }
            return retVal;
        }
    }

    private class DsmIdleState extends State {
        private DsmIdleState() {
        }

        public void enter() {
            IDSManager.this.logd("DsmIdleState: enter-------------------------------------------");
            IDSManager.this.stopTriggerDataSwitch();
        }

        public void exit() {
            IDSManager.this.logd("DsmIdleState: exit");
        }

        public boolean processMessage(Message msg) {
            int reason;
            IDSManager.this.logd("DsmIdleState msg=" + msg.what);
            int slotId = msg.arg1;
            switch (msg.what) {
                case HwVSimUtilsInner.STATE_EB /*2*/:
                    reason = 3;
                    break;
                case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                    reason = 2;
                    break;
                case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                    reason = 1;
                    break;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                    IDSManager.this.mIdsInfoRecord.saveServiceState(slotId, (ServiceState) msg.obj);
                    if (!IDSManager.this.mIdsInfoRecord.isRoamingOn()) {
                        reason = 4;
                        slotId = IDSManager.this.findAbnormalSlot();
                        break;
                    }
                    IDSManager.this.logd("DsmIdleState slot " + slotId + " is roaming");
                    IDSManager.this.transitionTo(IDSManager.this.mHaltingState);
                    return true;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_CARD_POWER_ON /*7*/:
                    IDSManager.this.mIdsInfoRecord.saveSignalStrength(slotId, (SignalStrength) msg.obj);
                    reason = 5;
                    slotId = IDSManager.this.findAbnormalSlot();
                    break;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_NETWORK_CONNECTED /*16*/:
                    reason = 6;
                    slotId = IDSManager.this.mIdsInfoRecord.getUserDefaultDataSlot();
                    break;
                default:
                    IDSManager.this.logd("DsmIdleState: ignore msg.what = " + msg.what);
                    return false;
            }
            if (IDSManager.this.triggerDataSwitch(slotId, reason)) {
                IDSManager.this.transitionTo(IDSManager.this.mSwitchTriggeredState);
            }
            return true;
        }
    }

    private class DsmSwitchForbiddenState extends State {
        private DsmSwitchForbiddenState() {
        }

        public void enter() {
            IDSManager.this.startDataSwitchTriggerForbiddenTimer();
            IDSManager.this.logd("DsmSwitchForbiddenState: enter-------------------------------------------");
        }

        public void exit() {
            IDSManager.this.removeMessages(9);
            IDSManager.this.logd("DsmSwitchForbiddenState: exit");
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            int slotId = msg.arg1;
            int reason = -1;
            IDSManager.this.logd("DsmSwitchForbiddenState msg=" + msg.what);
            switch (msg.what) {
                case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                    if (msg.arg1 == IDSManager.this.mAbnormalSlotId) {
                        IDSManager.this.logd("DsmSwitchForbiddenState: EVENT_DATA_STATE_CONNECTED on slot = " + msg.arg1);
                        IDSManager.this.stopTriggerDataSwitch();
                    }
                    return true;
                case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                    IDSManager.this.logd("DsmSwitchForbiddenState: EVENT_DEFAULT_DATA_SUB_CHANGED on slot = " + msg.arg1);
                    IDSManager.this.mIdsInfoRecord.updataUserDefaultDataSlot(msg.arg1);
                    if (-1 != IDSManager.this.mAbnormalSlotId) {
                        IDSManager.this.stopTriggerDataSwitch();
                    }
                    return true;
                case HwVSimUtilsInner.STATE_EB /*2*/:
                    reason = 3;
                    slotId = IDSManager.this.mIdsInfoRecord.getUserDefaultDataSlot();
                    break;
                case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                    reason = 2;
                    break;
                case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                    reason = 1;
                    break;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                    IDSManager.this.mIdsInfoRecord.saveServiceState(slotId, (ServiceState) msg.obj);
                    if (!IDSManager.this.mIdsInfoRecord.isRoamingOn()) {
                        reason = 4;
                        slotId = IDSManager.this.findAbnormalSlot();
                        break;
                    }
                    IDSManager.this.transitionTo(IDSManager.this.mHaltingState);
                    return true;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_CARD_POWER_ON /*7*/:
                    IDSManager.this.mIdsInfoRecord.saveSignalStrength(slotId, (SignalStrength) msg.obj);
                    reason = 5;
                    slotId = IDSManager.this.findAbnormalSlot();
                    break;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_GET_NETWORK_TYPE /*9*/:
                    handleDataSwitchAllowed();
                    return true;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_NETWORK_CONNECTED /*16*/:
                    reason = 6;
                    slotId = IDSManager.this.mIdsInfoRecord.getUserDefaultDataSlot();
                    break;
                default:
                    IDSManager.this.logd("DsmSwitchForbiddenState: ignore msg.what = " + msg.what);
                    retVal = false;
                    break;
            }
            if (slotId == IDSManager.this.mIdsInfoRecord.getUserDefaultDataSlot()) {
                IDSManager.this.abnormalHappenedInForbiddenState(reason);
            }
            IDSManager.this.logd("DsmSwitchForbiddenState, process message over , mAbnormalSlotId = " + IDSManager.this.mAbnormalSlotId + ", mAbnormalReason =" + IDSManager.this.mAbnormalReason);
            return retVal;
        }

        private void handleDataSwitchAllowed() {
            IDSManager.this.logd("handleDataSwitchAllowed : Data Switch forbidden time has past,mAbnormalSlotId = " + IDSManager.this.mAbnormalSlotId);
            IDSManager.this.mStayInForbiddenStateBeforeTimeUp = false;
            if (IDSManager.this.triggerDataSwitch(IDSManager.this.mAbnormalSlotId, IDSManager.this.mAbnormalReason)) {
                IDSManager.this.transitionTo(IDSManager.this.mSwitchTriggeredState);
                return;
            }
            IDSManager.this.resetDataSwitchForbiddenInterval();
            IDSManager.this.transitionTo(IDSManager.this.mIdleState);
        }
    }

    private class DsmSwitchOngoingState extends State {
        private DsmSwitchOngoingState() {
        }

        public void enter() {
            IDSManager.this.logd("DsmSwitchOngoingState: enter-------------------------------------------");
            IDSManager.this.sendMessageDelayed(IDSManager.this.obtainMessage(15), HwVSimConstants.INITIAL_TIMEOUT);
        }

        public void exit() {
            IDSManager.this.removeMessages(15);
            IDSManager.this.logd("DsmSwitchOngoingState: exit");
        }

        public boolean processMessage(Message msg) {
            IDSManager.this.logd("DsmSwitchOngoingState msg=" + msg.what);
            switch (msg.what) {
                case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                    IDSManager.this.handleDataConnected(msg.arg1);
                    return true;
                case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                    if (IDSManager.this.mAbnormalSlotId == msg.arg1) {
                        return true;
                    }
                    IDSManager.this.logd("DsmSwitchOngoingState: failure happened on candidate slotId " + msg.arg1 + ",setDefaultDataSubId = " + IDSManager.this.mAbnormalSlotId);
                    IDSManager.this.dataSwitchFailed();
                    return true;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_PLMN_SELINFO /*15*/:
                    IDSManager.this.logd("DsmSwitchOngoingState: no state change on candidate slotId " + (1 - IDSManager.this.mAbnormalSlotId));
                    IDSManager.this.dataSwitchFailed();
                    return true;
                default:
                    IDSManager.this.logd("DsmSwitchOngoingState: ignore msg.what = " + msg.what);
                    return false;
            }
        }
    }

    private class DsmSwitchTriggeredState extends State {
        private DsmSwitchTriggeredState() {
        }

        public void enter() {
            IDSManager.this.logd("DsmSwitchTriggeredState: enter-------------------------------------------");
        }

        public void exit() {
            IDSManager.this.logd("DsmSwitchTriggeredState: exit");
        }

        public boolean processMessage(Message msg) {
            IDSManager.this.logd("DsmSwitchTriggeredState msg=" + msg.what);
            switch (msg.what) {
                case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                    IDSManager.this.logd("DsmSwitchTriggeredState: EVENT_DATA_STATE_CONNECTED on slot = " + msg.arg1);
                    if (msg.arg1 != IDSManager.this.mAbnormalSlotId) {
                        return true;
                    }
                    IDSManager.this.transitionTo(IDSManager.this.mIdleState);
                    return true;
                case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                    IDSManager.this.logd("DsmSwitchTriggeredState: EVENT_DEFAULT_DATA_SUB_CHANGED on slot = " + msg.arg1);
                    IDSManager.this.mIdsInfoRecord.updataUserDefaultDataSlot(msg.arg1);
                    IDSManager.this.transitionTo(IDSManager.this.mIdleState);
                    return true;
                case HwVSimUtilsInner.STATE_EB /*2*/:
                    IDSManager.this.logd("DsmSwitchTriggeredState: EVENT_DATA_STALL_HAPPENED exit");
                    IDSManager.this.triggerDataSwitch(msg.arg1, 3);
                    IDSManager.this.transitionTo(IDSManager.this.mSwitchOngoingState);
                    return true;
                case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
                    if (IDSManager.this.handleDataSwitchTrigger()) {
                        IDSManager.this.transitionTo(IDSManager.this.mSwitchOngoingState);
                        return true;
                    }
                    IDSManager.this.transitionTo(IDSManager.this.mIdleState);
                    return true;
                default:
                    IDSManager.this.logd("DsmSwitchTriggeredState: ignore msg.what = " + msg.what);
                    return false;
            }
        }
    }

    public IDSManager(Context context, String name, Looper looper, IntelligentDataSwitch ids) {
        super(name + "Manager", looper);
        this.mDataSwitchForbiddenInterval = IDSConstants.TWO_MINUTE_TO_WAIT;
        this.mIdsInfoRecord = null;
        this.mStayInForbiddenStateBeforeTimeUp = false;
        this.mDataSwitchBroadcastSend = false;
        this.mAbnormalSlotId = -1;
        this.mAbnormalReason = -1;
        this.mPdpSetupFailureRecoverTimer = IDSConstants.ONE_MINUTE_TO_WAIT;
        this.mPdpDisconnectExcpRecoverTimer = IDSConstants.ONE_MINUTE_TO_WAIT;
        this.mGoodRsrpForLte = -90;
        this.mWeakPsrpForLte = IDSConstants.WEAK_LTE_SIGNAL_STRENGTH_VALUE;
        this.mGoodRsrqForLte = -10;
        this.mWeakRsrqForLte = -15;
        this.mGoodRscpForWandTD = -85;
        this.mWeakRscpForWandTD = -100;
        this.mGoodSignalStrengthForGsm = -85;
        this.mWeakSignalStrengthForGsm = -100;
        this.mGoodRssiForEvdo = -85;
        this.mWeakRssiForEvdo = -100;
        this.mGoodSnrForEvdo = 7;
        this.mWeakSnrForEvdo = 1;
        this.mGoodRssiForCdma = -85;
        this.mWeakRssiForCdma = -100;
        this.mGoodEcioForCdma = -12;
        this.mWeakEcioForCdma = -15;
        this.mIDS = null;
        this.mContext = null;
        this.mDefaultDataSlotWeakCount = 0;
        this.mSignalWeakTimer = null;
        this.mDefaultState = new DsmDefaultState();
        this.mHaltingState = new DsmHaltingState();
        this.mVoiceCallBackGroundTimer = null;
        this.mVoiceCallUiGoBackGround = false;
        this.mIdleState = new DsmIdleState();
        this.mSwitchTriggeredState = new DsmSwitchTriggeredState();
        this.mSwitchOngoingState = new DsmSwitchOngoingState();
        this.mSwitchForbiddenState = new DsmSwitchForbiddenState();
        this.mContext = context;
        setLogRecSize(300);
        setLogOnlyTransitions(true);
        this.mIDS = ids;
        this.mIdsInfoRecord = new IDSInfoRecord();
        this.mIdsInfoRecord.setUserDcState(this.mIDS.isUserDataEnabled());
        this.mIdsInfoRecord.setAirplaneState(this.mIDS.isAirplaneModeOn());
        configureDataSwitchQos();
        configureDataSwitchWaitDsRecoverTimer();
        configureDataSwitchInterval();
        addState(this.mDefaultState);
        addState(this.mHaltingState, this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mSwitchTriggeredState, this.mDefaultState);
        addState(this.mSwitchOngoingState, this.mDefaultState);
        addState(this.mSwitchForbiddenState, this.mDefaultState);
        setInitialState(this.mHaltingState);
    }

    private void startDataSwitchTriggerForbiddenTimer() {
        logd("start Data Switch Trigger Forbidden Timer mDataSwitchForbiddenInterval = " + this.mDataSwitchForbiddenInterval);
        sendMessageDelayed(obtainMessage(9), (long) this.mDataSwitchForbiddenInterval);
    }

    private int getNetworkRat(int slotId) {
        int nwRat = 0;
        ServiceState serviceState = this.mIdsInfoRecord.getServiceState(slotId);
        if (serviceState != null) {
            nwRat = getRatType(serviceState.getDataNetworkType());
        }
        logd("getNetworkRat: return nwRat = " + nwRat);
        return nwRat;
    }

    private void handleDataConnected(int slotId) {
        logd("handleDataConnected slotId = " + slotId + " success");
        if (slotId != this.mAbnormalSlotId) {
            switch (this.mAbnormalReason) {
                case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                case HwVSimUtilsInner.STATE_EB /*2*/:
                case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                    logd("handleDataConnected: mStayInForbiddenStateBeforeTimeUp = true ");
                    this.mStayInForbiddenStateBeforeTimeUp = true;
                    break;
                default:
                    this.mStayInForbiddenStateBeforeTimeUp = false;
                    break;
            }
            cleanDataSwitchFlags();
            transitionTo(this.mSwitchForbiddenState);
        }
    }

    private void cleanDataSwitchFlags() {
        this.mAbnormalReason = -1;
        this.mAbnormalSlotId = -1;
        logd("cleanDataSwitchFlags over ");
    }

    private void stopTriggerDataSwitch() {
        logd("stopAbnormalDisconnectTimer Enter ");
        removeDataSwitchMsg();
        cleanDataSwitchFlags();
    }

    private void removeDataSwitchMsg() {
        logd("removeDataSwitchMsg Enter ");
        removeMessages(5);
    }

    private void saveAbnormalSlotAndReason(int slotId, int reason) {
        this.mAbnormalSlotId = slotId;
        this.mAbnormalReason = reason;
        logd("saveAbnormalSlotAndReason: mAbnormalSlotId = " + this.mAbnormalSlotId + ", mAbnormalReason = " + this.mAbnormalReason);
    }

    private int getSwitchTriggerMsgDelayTimer() {
        switch (this.mAbnormalReason) {
            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                return this.mPdpSetupFailureRecoverTimer;
            case HwVSimUtilsInner.STATE_EB /*2*/:
                if (this.mIdsInfoRecord.isOnlyDataSubInVoiceCall()) {
                    return 0;
                }
                return this.mPdpDisconnectExcpRecoverTimer;
            case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
            case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                return 0;
            default:
                return 0;
        }
    }

    private void sendDataSwitchTriggerMsg() {
        int timeToDelay = getSwitchTriggerMsgDelayTimer();
        logd("sendDataSwitchTriggerMsg: timeToDelay = " + timeToDelay);
        if (timeToDelay == 0) {
            sendMessage(obtainMessage(5));
        } else {
            sendMessageDelayed(obtainMessage(5), (long) timeToDelay);
        }
    }

    private boolean triggerDataSwitch(int slotId, int reason) {
        logd("triggerDataSwitch: slotId = " + slotId + ", reason = " + reason);
        if (!this.mIDS.isSlotIdValid(slotId)) {
            return false;
        }
        saveAbnormalSlotAndReason(slotId, reason);
        sendDataSwitchTriggerMsg();
        return true;
    }

    private int changeRatToGeneration(int rat) {
        logd("changeRatToGeneration: rat = " + rat);
        switch (rat) {
            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
            case HwVSimUtilsInner.STATE_EB /*2*/:
                return 2;
            case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
            case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
                return 3;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                return 4;
            default:
                return 0;
        }
    }

    private boolean isSignalStrengthWeak(int slotId) {
        logd("isSignalStrengthWeak: slotId = " + slotId);
        if (1 >= getSignalStrengthLevel(getNetworkRat(slotId), this.mIdsInfoRecord.getSignalStrength(slotId))) {
            return true;
        }
        return false;
    }

    protected void startSignalWeakTimer(int timerLen) {
        logd("startSignalWeakTimer! timerLen =" + timerLen);
        this.mSignalWeakTimer = new Timer();
        this.mSignalWeakTimer.schedule(new TimerTask() {
            public void run() {
                IDSManager.this.mDefaultDataSlotWeakCount = 0;
                IDSManager.this.mSignalWeakTimer = null;
                IDSManager.this.logd("SignalWeakTimer time out: mDefaultDataSlotWeakCount is reseted");
            }
        }, (long) timerLen);
    }

    protected void stopSignalWeakTimer() {
        logd(" stopSignalWeakTimer!");
        this.mDefaultDataSlotWeakCount = -1;
        if (this.mSignalWeakTimer != null) {
            this.mSignalWeakTimer.cancel();
            this.mSignalWeakTimer = null;
        }
        this.mDefaultDataSlotWeakCount = 0;
    }

    private int findAbnormalSlotWhenRatIdentical(int ratGeneration) {
        if (this.mIdsInfoRecord.isAllSignalStrengthValid()) {
            loge("findAbnormalSlotWhenRatIdentical: mSignalStrength[0] =" + this.mIdsInfoRecord.getSignalStrength(0) + ", mSignalStrength[1] = " + this.mIdsInfoRecord.getSignalStrength(1));
            return -1;
        }
        int currentDds = this.mIdsInfoRecord.getUserDefaultDataSlot();
        if (isSignalStrengthWeak(currentDds) && !isSignalStrengthWeak(1 - currentDds)) {
            if (this.mSignalWeakTimer == null) {
                startSignalWeakTimer(IDSConstants.SIGNAL_MAX_WEAK_TIMER);
                return -1;
            }
            this.mDefaultDataSlotWeakCount++;
            logd("findAbnormalSlotWhenRatIdentical: defaultDataSlotWeakCount = " + this.mDefaultDataSlotWeakCount);
            if (5 == this.mDefaultDataSlotWeakCount) {
                stopSignalWeakTimer();
                return currentDds;
            }
        }
        return -1;
    }

    private int findAbnormalSlot() {
        int currentDds = this.mIdsInfoRecord.getUserDefaultDataSlot();
        logd("findAbnormalSlot: Enter, mUserDefaultDataSlot = " + currentDds);
        int ddsRat = getNetworkRat(currentDds);
        int nonDdsRat = getNetworkRat(1 - currentDds);
        int defaultSlotRatGen = changeRatToGeneration(ddsRat);
        int otherSlotRatGen = changeRatToGeneration(nonDdsRat);
        if (defaultSlotRatGen < otherSlotRatGen) {
            return currentDds;
        }
        if (defaultSlotRatGen != otherSlotRatGen || defaultSlotRatGen == 0) {
            logd("findAbnormalSlot: no Abnormal Slot find");
            return -1;
        } else if (5 == ddsRat) {
            return -1;
        } else {
            if (5 == nonDdsRat) {
                return currentDds;
            }
            return findAbnormalSlotWhenRatIdentical(defaultSlotRatGen);
        }
    }

    private int getRatType(int type) {
        logd("getRatType: type = " + type);
        switch (type) {
            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
            case HwVSimUtilsInner.STATE_EB /*2*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_NETWORK_CONNECTED /*16*/:
                return 1;
            case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_ACTIVE_MODEM_MODE /*8*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_GET_NETWORK_TYPE /*9*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_NETWORK_TYPE /*10*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_PLMN_SELINFO /*15*/:
                return 5;
            case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_CARD_POWER_ON /*7*/:
                return 2;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
            case HwVSimEventReport.VSIM_PROCESS_TYPE_DB /*12*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_DISABLE_VSIM_DONE /*14*/:
                return 3;
            case HwVSimEventReport.VSIM_PROCESS_TYPE_DC /*13*/:
                return 6;
            case HwVSimConstants.EVENT_SET_APDSFLOWCFG_DONE /*17*/:
                return 4;
            default:
                return 0;
        }
    }

    private boolean isVsimOn() {
        return false;
    }

    private boolean switchNotAllowed() {
        if (!checkRegisterStateForCandidateSlot(1 - this.mAbnormalSlotId) || NetworkFactory.isDualCellDataEnable() || this.mIdsInfoRecord.isBothDataSubInVoiceCall()) {
            return true;
        }
        return isVsimOn();
    }

    private boolean needSwitchInVoiceCall() {
        boolean candidateSlotRatHigher = changeRatToGeneration(getNetworkRat(this.mAbnormalSlotId)) <= changeRatToGeneration(getNetworkRat(1 - this.mAbnormalSlotId));
        boolean defaultDataDisconnect = DataState.CONNECTED != this.mIDS.getApnState(this.mAbnormalSlotId);
        logd("candidateSlotRatHigher = " + candidateSlotRatHigher + ", defaultDataDisconnect " + defaultDataDisconnect + ", mVoiceCallUiGoBackGround " + this.mVoiceCallUiGoBackGround);
        if (!this.mIdsInfoRecord.isOnlyDataSubInVoiceCall() || !this.mVoiceCallUiGoBackGround) {
            return false;
        }
        if (defaultDataDisconnect) {
            return true;
        }
        return candidateSlotRatHigher;
    }

    private boolean isSwitchDdsAllowed() {
        if (switchNotAllowed()) {
            return false;
        }
        if (this.mIdsInfoRecord.noVoiceCall()) {
            return true;
        }
        return needSwitchInVoiceCall();
    }

    private boolean handleDataSwitchTrigger() {
        if (!isSwitchDdsAllowed()) {
            return false;
        }
        doDataSwitch();
        return true;
    }

    private void updataDataSwitchForbiddenTimer() {
    }

    private void sendIntentToApp() {
        if (!this.mDataSwitchBroadcastSend) {
            this.mDataSwitchBroadcastSend = true;
            this.mContext.sendBroadcast(new Intent("android.intent.action.INTELLGENT_DATA_SWITCH_TRIGGERED"));
            logd("sendIntentToApp: send DATA_SWITCH_TRIGGERED Intent to Application");
        }
    }

    private void doDataSwitch() {
        sendIntentToApp();
        SubscriptionController.getInstance().setDefaultDataSubId(1 - this.mAbnormalSlotId);
        updataDataSwitchForbiddenTimer();
        this.mVoiceCallUiGoBackGround = false;
        logd("doDataSwitch: setDefaultDataSubId to " + (1 - this.mAbnormalSlotId) + ",mAbnormalReason = " + this.mAbnormalReason + ",  mDataSwitchForbiddenInterval update to " + this.mDataSwitchForbiddenInterval);
    }

    private boolean checkRegisterStateForCandidateSlot(int slot) {
        ServiceState serviceState = this.mIdsInfoRecord.getServiceState(slot);
        if (serviceState == null || serviceState.getDataRegState() != 0) {
            loge("checkRegisterStateForCandidateSlot: slot = " + slot + "Gprs not Attached");
            return false;
        }
        logd("checkRegisterStateForCandidateSlot: slot = " + slot + "Gprs Attached");
        return true;
    }

    private int getLteLevel(SignalStrength signalStrngth) {
        if (signalStrngth == null) {
            loge("getLteLevel: signalStrngth = null");
            return 0;
        }
        int lteRsrp = signalStrngth.getLteRsrp();
        int lteRsrq = signalStrngth.getLteRsrq();
        int rsrpLevel = 0;
        int rsrqLevel = 0;
        if (lteRsrp > -44) {
            rsrpLevel = 0;
        } else if (lteRsrp >= this.mGoodRsrpForLte) {
            rsrpLevel = 4;
        } else if (lteRsrp < this.mGoodRsrpForLte && lteRsrp >= this.mWeakPsrpForLte) {
            rsrpLevel = 3;
        } else if (lteRsrp < this.mWeakPsrpForLte) {
            rsrpLevel = 1;
        }
        if (lteRsrq == Integer.MAX_VALUE) {
            rsrqLevel = 0;
        } else if (lteRsrq >= this.mGoodRsrqForLte) {
            rsrqLevel = 4;
        } else if (lteRsrq < this.mGoodRsrqForLte && lteRsrq >= this.mWeakRsrqForLte) {
            rsrqLevel = 3;
        } else if (lteRsrq < this.mWeakRsrqForLte) {
            rsrqLevel = 1;
        }
        int level = rsrpLevel < rsrqLevel ? rsrpLevel : rsrqLevel;
        logd("getLTELevel - Return level =  " + level + ", lteRsrp = " + lteRsrp + ", lteRsrq = " + lteRsrq);
        return level;
    }

    private int getGsmLevel(SignalStrength signalStrngth) {
        if (signalStrngth == null) {
            loge("getGsmLevel: signalStrngth = null");
            return 0;
        }
        int gsmSS = signalStrngth.getGsmSignalStrength();
        int level = 0;
        if (gsmSS == 0 || 99 == gsmSS) {
            level = 0;
        } else if (gsmSS >= this.mGoodSignalStrengthForGsm) {
            level = 4;
        } else if (gsmSS < this.mGoodSignalStrengthForGsm && gsmSS >= this.mWeakSignalStrengthForGsm) {
            level = 3;
        } else if (gsmSS < this.mWeakSignalStrengthForGsm) {
            level = 1;
        }
        logd("getGsmLevel: return level = " + level + ", gsmSS = " + gsmSS);
        return level;
    }

    private int getTdsOrWLevel(SignalStrength signalStrngth) {
        if (signalStrngth == null) {
            loge("getTdsOrWLevel: signalStrngth = null");
            return 0;
        }
        int wcdmaRscp = signalStrngth.getWcdmaRscp();
        int level = 0;
        if (wcdmaRscp >= this.mGoodRscpForWandTD) {
            level = 4;
        } else if (wcdmaRscp < this.mGoodRscpForWandTD && wcdmaRscp >= this.mWeakRscpForWandTD) {
            level = 3;
        } else if (wcdmaRscp < this.mWeakRscpForWandTD) {
            level = 1;
        }
        logd("getTdsOrWLevel: return level = " + level + ", wcdmaRscp = " + wcdmaRscp);
        return level;
    }

    private int getEvdoLevel(SignalStrength signalStrngth) {
        if (signalStrngth == null) {
            loge("getEvdoLevel: signalStrngth = null");
            return 0;
        }
        int evdoDbm = signalStrngth.getEvdoDbm();
        int evdoSnr = signalStrngth.getEvdoSnr();
        int levelEvdoDbm = 0;
        int levelEvdoSnr = 0;
        if (-1 == evdoDbm) {
            levelEvdoDbm = 0;
        } else if (evdoDbm >= this.mGoodRssiForEvdo) {
            levelEvdoDbm = 4;
        } else if (evdoDbm < this.mGoodRssiForEvdo && evdoDbm >= this.mWeakRssiForEvdo) {
            levelEvdoDbm = 3;
        } else if (evdoDbm < this.mWeakRssiForEvdo) {
            levelEvdoDbm = 1;
        }
        if (-1 == evdoSnr) {
            levelEvdoSnr = 0;
        } else if (evdoSnr >= this.mGoodSnrForEvdo) {
            levelEvdoSnr = 4;
        } else if (evdoSnr < this.mGoodSnrForEvdo && evdoSnr >= this.mWeakSnrForEvdo) {
            levelEvdoSnr = 3;
        } else if (evdoSnr < this.mWeakSnrForEvdo) {
            levelEvdoSnr = 1;
        }
        int evdolevel = levelEvdoDbm < levelEvdoSnr ? levelEvdoDbm : levelEvdoSnr;
        logd("getEvdoLevel = " + evdolevel + ", evdoDbm = " + evdoDbm + ", evdoSnr = " + evdoSnr);
        return evdolevel;
    }

    private int getCdmaLevel(SignalStrength signalStrngth) {
        if (signalStrngth == null) {
            loge("getCdmaLevel: signalStrngth = null");
            return 0;
        }
        int cdmaDbm = signalStrngth.getCdmaDbm();
        int cdmaEcio = signalStrngth.getCdmaEcio();
        int levelDbm = 0;
        int levelEcio = 0;
        if (-1 == cdmaDbm) {
            levelDbm = 0;
        } else if (cdmaDbm >= this.mGoodRssiForCdma) {
            levelDbm = 4;
        } else if (cdmaDbm < this.mGoodRssiForCdma && cdmaDbm >= this.mWeakRssiForCdma) {
            levelDbm = 3;
        } else if (cdmaDbm < this.mWeakRssiForCdma) {
            levelDbm = 1;
        }
        if (-1 == cdmaEcio) {
            levelEcio = 0;
        } else if (cdmaEcio >= this.mGoodEcioForCdma) {
            levelEcio = 4;
        } else if (cdmaEcio < this.mGoodEcioForCdma && cdmaEcio >= this.mWeakEcioForCdma) {
            levelEcio = 3;
        } else if (cdmaEcio < this.mWeakEcioForCdma) {
            levelEcio = 1;
        }
        int cdmalevel = levelDbm < levelEcio ? levelDbm : levelEcio;
        logd("getCdmaLevel = " + cdmalevel + ", cdmaDbm = " + cdmaDbm + ", cdmaEcio = " + cdmaEcio);
        return cdmalevel;
    }

    private void configureDataSwitchInterval() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setQosValueForEachRat(int index, String[] qosValueString) {
        switch (index) {
            case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                this.mGoodRsrpForLte = Integer.parseInt(qosValueString[0]);
                this.mWeakPsrpForLte = Integer.parseInt(qosValueString[1]);
                this.mGoodRsrqForLte = Integer.parseInt(qosValueString[2]);
                this.mWeakRsrqForLte = Integer.parseInt(qosValueString[3]);
                break;
            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                this.mGoodRscpForWandTD = Integer.parseInt(qosValueString[0]);
                this.mWeakRscpForWandTD = Integer.parseInt(qosValueString[1]);
                break;
            case HwVSimUtilsInner.STATE_EB /*2*/:
                this.mGoodSignalStrengthForGsm = Integer.parseInt(qosValueString[0]);
                this.mWeakSignalStrengthForGsm = Integer.parseInt(qosValueString[1]);
                break;
            case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                this.mGoodRssiForEvdo = Integer.parseInt(qosValueString[0]);
                this.mWeakRssiForEvdo = Integer.parseInt(qosValueString[1]);
                this.mGoodSnrForEvdo = Integer.parseInt(qosValueString[2]);
                this.mWeakSnrForEvdo = Integer.parseInt(qosValueString[3]);
                break;
            case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                this.mGoodRssiForCdma = Integer.parseInt(qosValueString[0]);
                this.mWeakRssiForCdma = Integer.parseInt(qosValueString[1]);
                this.mGoodEcioForCdma = Integer.parseInt(qosValueString[2]);
                this.mWeakEcioForCdma = Integer.parseInt(qosValueString[3]);
                break;
            default:
                try {
                    loge("setQosValueForEachRat, out of range");
                    break;
                } catch (NumberFormatException ex) {
                    loge("setQosValueForEachRat, NumberFormatException error: " + ex);
                    break;
                }
        }
        if (4 == index) {
            logd("setQosValueForEachRat: mGoodRsrpForLte =" + this.mGoodRsrpForLte + ",mWeakPsrpForLte = " + this.mWeakPsrpForLte + ",mGoodRsrqForLte = " + this.mGoodRsrqForLte + ",mWeakRsrqForLte = " + this.mWeakRsrqForLte + ",mGoodRscpForWandTD = " + this.mGoodRscpForWandTD + ",mWeakRscpForWandTD = " + this.mWeakRscpForWandTD + ",mGoodSignalStrengthForGsm = " + this.mGoodSignalStrengthForGsm + ",mWeakSignalStrengthForGsm = " + this.mWeakSignalStrengthForGsm + ",mGoodRssiForEvdo = " + this.mGoodRssiForEvdo + ",mWeakRssiForEvdo = " + this.mWeakRssiForEvdo + ",mGoodSnrForEvdo = " + this.mGoodSnrForEvdo + ",mWeakSnrForEvdo = " + this.mWeakSnrForEvdo + ",mGoodRssiForCdma = " + this.mGoodRssiForCdma + ",mWeakRssiForCdma = " + this.mWeakRssiForCdma + ",mGoodEcioForCdma = " + this.mGoodEcioForCdma + ",mWeakEcioForCdma = " + this.mWeakEcioForCdma);
        }
    }

    private void configureDataSwitchQos() {
        String[] eachRatQosString = IDSConstants.DATA_SWITCH_QOS_STRING.split(";");
        if (5 == eachRatQosString.length) {
            for (int ratNum = 0; ratNum < 5; ratNum++) {
                String[] qosValueString = eachRatQosString[ratNum].split(",");
                if (4 == qosValueString.length) {
                    setQosValueForEachRat(ratNum, qosValueString);
                } else {
                    loge("configureDataSwitchQos: eachRatQosString[" + ratNum + "] is inllegal which is : " + eachRatQosString[ratNum]);
                }
            }
            return;
        }
        loge("configureDataSwitchQos: DATA_SWITCH_QOS_STRING is inllegal which is : " + IDSConstants.DATA_SWITCH_QOS_STRING);
    }

    private void configureDataSwitchWaitDsRecoverTimer() {
        if (TextUtils.isEmpty(IDSConstants.WAIT_DS_RECOVERY_TIMER)) {
            loge("configureDataSwitchWaitDsRecoverTimer: WAIT_DS_RECOVERY_TIMER is null, use default value");
            return;
        }
        String[] waitDsRecoverTimer = IDSConstants.WAIT_DS_RECOVERY_TIMER.split(",");
        if (waitDsRecoverTimer.length <= 1 || waitDsRecoverTimer[0] == null || waitDsRecoverTimer[1] == null) {
            loge("configureDataSwitchWaitDsRecoverTimer: WAIT_DS_RECOVERY_TIMER is inllegal, which is : " + IDSConstants.WAIT_DS_RECOVERY_TIMER);
        } else {
            try {
                this.mPdpSetupFailureRecoverTimer = Integer.parseInt(waitDsRecoverTimer[0]);
                this.mPdpDisconnectExcpRecoverTimer = Integer.parseInt(waitDsRecoverTimer[1]);
            } catch (NumberFormatException ex) {
                loge("configureDataSwitchWaitDsRecoverTimer, NumberFormatException error : " + ex);
            }
            logd("configureDataSwitchWaitDsRecoverTimer: mPdpSetupFailureRecoverTimer = " + this.mPdpSetupFailureRecoverTimer + ",mPdpDisconnectExcpRecoverTimer = " + this.mPdpDisconnectExcpRecoverTimer);
        }
    }

    private void resetDataSwitchForbiddenInterval() {
    }

    private boolean isIntelligentDataSwitchValid() {
        if (!this.mIdsInfoRecord.getIdsState() || this.mIdsInfoRecord.getAirplaneState() || !this.mIdsInfoRecord.getUserDcState() || this.mIdsInfoRecord.isRoamingOn() || this.mIdsInfoRecord.getWifiState()) {
            return false;
        }
        return true;
    }

    private boolean isPackageOnTop(Context context, String cmdName) {
        List<RunningTaskInfo> runningTaskInfos = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        String cmpNameTemp = null;
        if (!(runningTaskInfos == null || runningTaskInfos.get(0) == null || ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity == null)) {
            cmpNameTemp = ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity.getPackageName();
        }
        if (cmpNameTemp == null) {
            return false;
        }
        logd("isPackageOnTop: topPackageName = " + cmpNameTemp);
        return cmpNameTemp.equals(cmdName);
    }

    protected void startVoiceCallIntoBackGroundTimer(int timerLen) {
        logd("startVoiceCallIntoBackGroundTimer!mUserDefaultDataSlot =" + this.mIdsInfoRecord.getUserDefaultDataSlot() + " is starting voice call");
        this.mVoiceCallBackGroundTimer = new Timer();
        this.mVoiceCallBackGroundTimer.schedule(new TimerTask() {
            public void run() {
                if (IDSManager.this.isPackageOnTop(IDSManager.this.mContext, IDSConstants.CALL_UI_NAME)) {
                    IDSManager.this.startVoiceCallIntoBackGroundTimer(IDSConstants.CHECK_VOICE_CALL_UI_ON_TOP_TIMER);
                    IDSManager.this.logd("startVoiceCallUIIntoBackGroundTimer: start timer again to check is user switch call serivice to backGround");
                    return;
                }
                IDSManager.this.mVoiceCallUiGoBackGround = true;
                IDSManager.this.mVoiceCallBackGroundTimer = null;
                IDSManager.this.sendMessage(IDSManager.this.obtainMessage(16));
                IDSManager.this.logd("startVoiceCallUIIntoBackGroundTimer: Trigger Data switch cause user switch call serivice to backGround");
            }
        }, (long) timerLen);
    }

    protected void stopVoiceCallIntoBackGroundTimer() {
        logd(" stopVoiceCallIntoBackGroundTimer!");
        if (this.mVoiceCallBackGroundTimer != null) {
            this.mVoiceCallBackGroundTimer.cancel();
            this.mVoiceCallBackGroundTimer = null;
        }
        this.mVoiceCallUiGoBackGround = false;
    }

    private int getSignalStrengthLevel(int rat, SignalStrength signalStrength) {
        int signalStrengthLevel = 0;
        int otherSignalStrengthLevel = 0;
        int lteLevel;
        int tdsOrWLevel;
        int gsmLevel;
        switch (rat) {
            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                signalStrengthLevel = getGsmLevel(signalStrength);
                lteLevel = getLteLevel(signalStrength);
                tdsOrWLevel = getTdsOrWLevel(signalStrength);
                if (lteLevel <= tdsOrWLevel) {
                    otherSignalStrengthLevel = tdsOrWLevel;
                    break;
                }
                otherSignalStrengthLevel = lteLevel;
                break;
            case HwVSimUtilsInner.STATE_EB /*2*/:
                signalStrengthLevel = getCdmaLevel(signalStrength);
                break;
            case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                signalStrengthLevel = getEvdoLevel(signalStrength);
                break;
            case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
                signalStrengthLevel = getTdsOrWLevel(signalStrength);
                lteLevel = getLteLevel(signalStrength);
                gsmLevel = getGsmLevel(signalStrength);
                if (lteLevel <= gsmLevel) {
                    otherSignalStrengthLevel = gsmLevel;
                    break;
                }
                otherSignalStrengthLevel = lteLevel;
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                signalStrengthLevel = getLteLevel(signalStrength);
                gsmLevel = getGsmLevel(signalStrength);
                tdsOrWLevel = getTdsOrWLevel(signalStrength);
                if (tdsOrWLevel <= gsmLevel) {
                    otherSignalStrengthLevel = gsmLevel;
                    break;
                }
                otherSignalStrengthLevel = tdsOrWLevel;
                break;
        }
        logd("getSignalStrengthLevel: return signalStrengthLevel = " + signalStrengthLevel + "otherSignalStrengthLevel = " + otherSignalStrengthLevel);
        return signalStrengthLevel < otherSignalStrengthLevel ? 2 : signalStrengthLevel;
    }

    private void dataSwitchFailed() {
        logd("dataSwitchFailed: set Default Data sub back to " + this.mAbnormalSlotId);
        SubscriptionController.getInstance().setDefaultDataSubId(this.mAbnormalSlotId);
        if (!this.mStayInForbiddenStateBeforeTimeUp) {
            this.mStayInForbiddenStateBeforeTimeUp = true;
            resetDataSwitchForbiddenInterval();
        }
        cleanDataSwitchFlags();
        transitionTo(this.mSwitchForbiddenState);
    }

    private void updateAbnormalSlotAndReason(int reason) {
        switch (this.mAbnormalReason) {
            case HwVSimUtilsInner.VSIM_SUB_INVALID /*-1*/:
            case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                saveAbnormalSlotAndReason(this.mIdsInfoRecord.getUserDefaultDataSlot(), reason);
            default:
        }
    }

    private void abnormalHappenedInForbiddenState(int reason) {
        if (this.mStayInForbiddenStateBeforeTimeUp || !triggerDataSwitch(this.mIdsInfoRecord.getUserDefaultDataSlot(), reason)) {
            updateAbnormalSlotAndReason(reason);
        } else {
            transitionTo(this.mSwitchTriggeredState);
        }
    }

    protected void logd(String info) {
        IDSConstants.logd(TAG, info);
    }

    protected void loge(String info) {
        IDSConstants.loge(TAG, info);
    }
}
