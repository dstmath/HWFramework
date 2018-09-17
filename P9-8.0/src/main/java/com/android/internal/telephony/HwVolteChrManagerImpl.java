package com.android.internal.telephony;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.VideoProfile;
import android.telephony.Rlog;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneConnection;
import java.util.ArrayList;

public class HwVolteChrManagerImpl implements HwVolteChrManager {
    private static final int ANSWER_FAIL = 4;
    private static final String CALL_COUNT_LIST = "call_count_list";
    private static final String CALL_FAIL_TYPE = "call_fail_type";
    private static final String CALL_STATE = "call_state";
    private static final String CALL_TYPE_AUDIO = "VoLTE";
    public static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    public static final String CHR_DATA = "chr_data";
    private static final int CS_REDIAL = 5;
    private static final String Call_TYPE_VIDEO = "VT";
    public static final boolean DBG = true;
    private static final int DROP_CALL = 2;
    public static final String FAULT_ID = "fault_id";
    private static final int FAULT_IMS_REG_FAIL_EVENT = 4002;
    private static final int FAULT_IMS_SS_PERFORMANCE_EVENT = 4004;
    private static final int FAULT_IMS_VT_FAIL_EVENT = 4003;
    private static final int FAULT_VOLTE_PERFORMANCE_EVENT = 4001;
    private static final int HANDOVER_CANCELED = 3;
    private static final int HANDOVER_COMPLETED = 1;
    private static final int HANDOVER_FAILED = 2;
    private static final int HANDOVER_STARTED = 0;
    private static final int HANGUP_FAIL = 3;
    public static final String INTENT_CHR = "com.huawei.android.chr.action.ACTION_REPORT_CHR";
    private static final String INVITE_TO_RING_TIME = "invite_to_ring_time";
    private static final String IS_MULTI_PARTY = "is_multi_party";
    public static final String LOG_TAG = "HwVolteChrManager";
    public static final int MAX_MONITOR_TIME = 65535;
    private static final String MEDIA_TYPE = "media_type";
    private static final int MODIFY_FAIL = 8;
    public static final String MODULE_ID = "module_id";
    private static final int MO_FAIL = 1;
    private static final int MT_EVENT = 2;
    private static final int MT_FAIL = 7;
    private static final int MT_NW_FAIL_EVENT = 9;
    public static final int NO_SERVICE_CAUSE_INCALL = 36867;
    public static final int NO_SERVICE_CAUSE_MO = 36865;
    public static final int NO_SERVICE_CAUSE_MT = 36866;
    public static final int NO_SERVICE_OFFSET_BASE = 36864;
    private static final String PRECISE_DISCONNECT_CAUSE = "precise_disconnect_cause";
    private static final int RESERVE_FAIL = 10;
    private static final int SRVCC_CANCEL = 9;
    private static final int SRVCC_FAIL = 6;
    private static final int VOLTE_MODULE_ID = 4000;
    private static final int VOLTE_STATISTIC_EVENT = 4005;
    private static HwVolteChrManager mInstance = new HwVolteChrManagerImpl();
    public Context mContext;
    public boolean mIsMissedCallTipsOn = false;
    public int mRemoteCauseCode = -1;
    public boolean mSrvccFlag = false;

    public enum CallCountEvent {
        Mo,
        Mt,
        Connected,
        MoFailUE,
        MoFailNW,
        MtFailUE,
        MtFailNW,
        DropCallUE,
        DropCallNW,
        SRVCCSuccess,
        SRVCCFail,
        CSRedial
    }

    public enum CallEndReason {
        CALL_END_NORMAL,
        CALL_FAIL_UE,
        CALL_FAIL_NW
    }

    public static HwVolteChrManager getDefault() {
        return mInstance;
    }

    public void init(Context context) {
        this.mContext = context;
    }

    public void updateCallLog(ImsPhoneConnection conn, ImsPhone phone) {
        int causeCode = conn.getDisconnectCause();
        CallEndReason reason = getCallEndReason(causeCode, this.mRemoteCauseCode);
        chrLog("updateCallLog causecode=" + causeCode + ",mRemoteCauseCode=" + this.mRemoteCauseCode + ", reason=" + reason);
        String subfix = CALL_TYPE_AUDIO;
        if (VideoProfile.isVideo(conn.getVideoState())) {
            chrLog("call type is Video");
            subfix = Call_TYPE_VIDEO;
        }
        ArrayList<String> mList = new ArrayList();
        Bundle mB = new Bundle();
        if (!conn.isIncoming()) {
            mList.add(CallCountEvent.Mo.toString() + subfix);
        } else if (!this.mIsMissedCallTipsOn) {
            mList.add(CallCountEvent.Mt.toString() + subfix);
        }
        if (conn.getDurationMillis() > 0) {
            mList.add(CallCountEvent.Connected.toString() + subfix);
            if (CallEndReason.CALL_FAIL_UE == reason) {
                mList.add(CallCountEvent.DropCallUE.toString() + subfix);
            } else if (CallEndReason.CALL_FAIL_NW == reason) {
                mList.add(CallCountEvent.DropCallNW.toString() + subfix);
            }
            callLostEvent(conn, causeCode, phone);
        } else if (!conn.isIncoming()) {
            if (CallEndReason.CALL_FAIL_UE == reason) {
                mList.add(CallCountEvent.MoFailUE.toString() + subfix);
            } else if (CallEndReason.CALL_FAIL_NW == reason) {
                mList.add(CallCountEvent.MoFailNW.toString() + subfix);
            }
            callLostEvent(conn, causeCode, phone);
        } else if (CallEndReason.CALL_FAIL_UE == reason) {
            mList.add(CallCountEvent.MtFailUE.toString() + subfix);
        } else if (CallEndReason.CALL_FAIL_NW == reason) {
            mList.add(CallCountEvent.MtFailNW.toString() + subfix);
        }
        this.mRemoteCauseCode = -1;
        mB.putStringArrayList(CALL_COUNT_LIST, mList);
        sendVolteChrBroadcast(mB, VOLTE_STATISTIC_EVENT);
    }

    private void callLostEvent(ImsPhoneConnection conn, int causeCode, ImsPhone phone) {
        if (causeCode != 3 && causeCode != 2) {
            if (conn.getDurationMillis() > 0) {
                chrLog("ims call dropped");
                triggerCallLostEvent(conn, phone, 2);
            } else if (!conn.isIncoming()) {
                chrLog("mo call failed");
                triggerCallLostEvent(conn, phone, 1);
            }
        }
    }

    public void updateMtCallLog(int event) {
        this.mIsMissedCallTipsOn = true;
        if (2 == event || 9 == event) {
            ArrayList<String> mList = new ArrayList();
            Bundle mB = new Bundle();
            mList.add(2 == event ? CallCountEvent.Mt.toString() + CALL_TYPE_AUDIO : CallCountEvent.MtFailNW.toString() + CALL_TYPE_AUDIO);
            mB.putStringArrayList(CALL_COUNT_LIST, mList);
            sendVolteChrBroadcast(mB, VOLTE_STATISTIC_EVENT);
        }
    }

    public void triggerCallLostEvent(ImsPhoneConnection conn, ImsPhone phone, int callType) {
        chrLog("triggerCallLostEvent callType=" + callType);
        if (1 != callType || (phone.getServiceState().getState() == 0 && !this.mSrvccFlag)) {
            Bundle mB = new Bundle();
            mB.putInt(CALL_FAIL_TYPE, callType);
            mB.putInt(PRECISE_DISCONNECT_CAUSE, this.mRemoteCauseCode);
            mB.putInt(MEDIA_TYPE, conn.getVideoState());
            mB.putInt(CALL_STATE, conn.getState().ordinal());
            mB.putBoolean(IS_MULTI_PARTY, conn.getCall().isMultiparty());
            sendVolteChrBroadcast(mB, FAULT_VOLTE_PERFORMANCE_EVENT);
            return;
        }
        this.mSrvccFlag = false;
        chrLog("normal redial, return");
    }

    public void triggerNoServiceDuringCallEvent(Call fgCall, Call rCall, Call bgCall) {
        int noServiceType;
        chrLog("triggerNoServiceDuringCallEvent.");
        if (fgCall == null || !fgCall.getState().isAlive()) {
            if (rCall != null && rCall.getState().isAlive()) {
                noServiceType = NO_SERVICE_CAUSE_MT;
            } else if (bgCall == null || !bgCall.getState().isAlive()) {
                chrLogE("triggerNoServiceDuringCallEvent. no active call");
                return;
            } else {
                noServiceType = NO_SERVICE_CAUSE_INCALL;
            }
        } else if (fgCall.isDialingOrAlerting()) {
            noServiceType = NO_SERVICE_CAUSE_MO;
        } else {
            noServiceType = NO_SERVICE_CAUSE_INCALL;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(CALL_FAIL_TYPE, 10);
        bundle.putInt(PRECISE_DISCONNECT_CAUSE, noServiceType);
        sendVolteChrBroadcast(bundle, FAULT_VOLTE_PERFORMANCE_EVENT);
    }

    public void triggerHangupFailedEvent() {
        chrLog("triggerHangupFailedEvent");
        Bundle mB = new Bundle();
        mB.putInt(CALL_FAIL_TYPE, 3);
        sendVolteChrBroadcast(mB, FAULT_VOLTE_PERFORMANCE_EVENT);
    }

    public void triggerAnswerFailedEvent(int callState) {
        chrLog("triggerAnswerFailedEvent callState=" + callState);
        Bundle mB = new Bundle();
        mB.putInt(CALL_FAIL_TYPE, 4);
        mB.putInt(CALL_STATE, callState);
        sendVolteChrBroadcast(mB, FAULT_VOLTE_PERFORMANCE_EVENT);
    }

    public void triggerMtCallFailEvent(long ringTime, long inviteTime, int failType, int failCause) {
        int duration;
        long delta = ringTime - inviteTime;
        chrLog("triggerMtCallFailEvent: ring/hang time: " + ringTime + ", invite time: " + inviteTime);
        if (0 >= delta || delta >= 65535) {
            duration = MAX_MONITOR_TIME;
        } else {
            duration = (int) delta;
        }
        Bundle mB = new Bundle();
        mB.putInt(CALL_FAIL_TYPE, failType);
        mB.putInt(PRECISE_DISCONNECT_CAUSE, failCause);
        mB.putInt(INVITE_TO_RING_TIME, duration);
        sendVolteChrBroadcast(mB, FAULT_VOLTE_PERFORMANCE_EVENT);
    }

    public boolean triggerImsRegFailEvent(ImsPhone imsPhone) {
        if (imsPhone == null) {
            throw new NullPointerException("HwVolteChrManagertriggerImsRegFailEvent: phone is null");
        }
        Phone defPhone = imsPhone.getDefaultPhone();
        if (defPhone == null) {
            chrLogE("getDefaultPhone failed!");
            return false;
        } else if (defPhone.getServiceState().getState() == 0 && defPhone.getServiceState().getNetworkType() == 13) {
            sendVolteChrBroadcast(null, FAULT_IMS_REG_FAIL_EVENT);
            return true;
        } else {
            chrLog("normal dereg, don't triggerImsRegFailEvent");
            return false;
        }
    }

    private void sendVolteChrBroadcast(Bundle data, int faultID) {
        chrLog("sendVolteChrBroadcast faultID=" + faultID);
        Intent mI = new Intent(INTENT_CHR);
        mI.putExtra(MODULE_ID, VOLTE_MODULE_ID);
        mI.putExtra(FAULT_ID, faultID);
        mI.putExtra(CHR_DATA, data);
        mI.setPackage("com.huawei.android.chr");
        this.mContext.sendBroadcast(mI, CHR_BROADCAST_PERMISSION);
    }

    private CallEndReason getCallEndReason(int causeCode, int remoteCauseCode) {
        if (causeCode == 3 || causeCode == 2) {
            return CallEndReason.CALL_END_NORMAL;
        }
        if (remoteCauseCode == 0) {
            return CallEndReason.CALL_END_NORMAL;
        }
        if (remoteCauseCode < 18732 || remoteCauseCode > 19038) {
            return CallEndReason.CALL_FAIL_UE;
        }
        return CallEndReason.CALL_FAIL_NW;
    }

    public void setSrvccFlag(boolean flag) {
        if (this.mSrvccFlag != flag) {
            chrLog("setSrvccFlag: " + flag);
        }
        this.mSrvccFlag = flag;
    }

    public void notifySrvccState(int state) {
        chrLog("notifySrvccState state = " + state);
        ArrayList<String> mList = new ArrayList();
        Bundle mB = new Bundle();
        switch (state) {
            case 1:
                mList.add(CallCountEvent.SRVCCSuccess.toString());
                break;
            case 2:
                triggerSrvccAbnormalEvent(2);
                mList.add(CallCountEvent.SRVCCFail.toString());
                break;
            case 3:
                triggerSrvccAbnormalEvent(3);
                mList.add(CallCountEvent.SRVCCFail.toString());
                break;
            default:
                return;
        }
        mB.putStringArrayList(CALL_COUNT_LIST, mList);
        sendVolteChrBroadcast(mB, VOLTE_STATISTIC_EVENT);
    }

    public void notifyCSRedial() {
        chrLog("notifyCSRedial");
        triggerCSRedialEvent();
        ArrayList<String> mList = new ArrayList();
        Bundle mB = new Bundle();
        mList.add(CallCountEvent.CSRedial.toString());
        mB.putStringArrayList(CALL_COUNT_LIST, mList);
        sendVolteChrBroadcast(mB, VOLTE_STATISTIC_EVENT);
    }

    private void triggerSrvccAbnormalEvent(int state) {
        chrLog("triggerSrvccFailEvent state=" + state);
        Bundle bundle = getVolteChrBundle();
        switch (state) {
            case 2:
                bundle.putInt(CALL_FAIL_TYPE, 6);
                sendVolteChrBroadcast(bundle, FAULT_VOLTE_PERFORMANCE_EVENT);
                return;
            case 3:
                bundle.putInt(CALL_FAIL_TYPE, 9);
                sendVolteChrBroadcast(bundle, FAULT_VOLTE_PERFORMANCE_EVENT);
                return;
            default:
                chrLogE("triggerSrvccAbnormalEvent unknown state=" + state);
                return;
        }
    }

    private void triggerCSRedialEvent() {
        chrLog("triggerCSRedialEvent");
        Bundle bundle = getVolteChrBundle();
        bundle.putInt(CALL_FAIL_TYPE, 5);
        sendVolteChrBroadcast(bundle, FAULT_VOLTE_PERFORMANCE_EVENT);
    }

    private Bundle getVolteChrBundle() {
        Bundle bundle = new Bundle();
        Connection connection = getFirstHandoverConnection();
        if (connection == null) {
            return bundle;
        }
        bundle.putInt(MEDIA_TYPE, connection.getVideoState());
        bundle.putInt(CALL_STATE, connection.getState().ordinal());
        bundle.putBoolean(IS_MULTI_PARTY, connection.getCall().isMultiparty());
        return bundle;
    }

    private Connection getFirstHandoverConnection() {
        Phone phone = getImsPhone();
        if (phone != null) {
            ArrayList<Connection> connections = ((ImsPhone) phone).getHandoverConnection();
            if (connections != null) {
                return (Connection) connections.get(0);
            }
        }
        chrLogE("getFirstHandoverConnection fail");
        return null;
    }

    private Phone getImsPhone() {
        Phone phone = PhoneFactory.getPhone(HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
        if (phone == null) {
            return null;
        }
        return phone.getImsPhone();
    }

    public void setRemoteCauseCode(int causeCode) {
        this.mRemoteCauseCode = causeCode;
    }

    private void chrLog(String s) {
        Rlog.d(LOG_TAG, " " + s);
    }

    private void chrLogE(String s) {
        Rlog.e(LOG_TAG, " [error] " + s);
    }
}
