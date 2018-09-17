package com.android.internal.telephony;

import android.content.Intent;
import android.os.AsyncResult;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.ArrayList;
import java.util.List;
import vendor.huawei.hardware.radio.V1_0.RILAPDsFlowInfoReport;
import vendor.huawei.hardware.radio.V1_0.RILImsCallModify;
import vendor.huawei.hardware.radio.V1_0.RILImsHandover;
import vendor.huawei.hardware.radio.V1_0.RILImsModifyEndCause;
import vendor.huawei.hardware.radio.V1_0.RILImsMtStatusReport;
import vendor.huawei.hardware.radio.V1_0.RILImsSrvstatusList;
import vendor.huawei.hardware.radio.V1_0.RILImsSuppSvcNotification;
import vendor.huawei.hardware.radio.V1_0.RILUnsolMsgPayload;
import vendor.huawei.hardware.radio.V1_0.RILVsimOtaSmsResponse;
import vendor.huawei.hardware.radio.V1_0.RILVtFlowInfoReport;

public class HwRadioIndication extends RadioIndication {
    private static final String ACTION_HW_XPASS_RESELECT_INFO = "android.intent.action.HW_XPASS_RESELECT_INFO";
    private static final int AS_DS_FLOW_INFO_REPORT_ARRAY_LENGTH = 7;
    private static final String LOG_TAG = "HwRadioIndication";
    private static final int SIGNAL_STRENGTH_DATA_LEN = 15;
    static int countAfterBoot = 0;
    HwRILReferenceImpl hwRilReferImpl;
    RIL mRil;

    HwRadioIndication(RIL ril) {
        super(ril);
        this.mRil = ril;
        this.hwRilReferImpl = new HwRILReferenceImpl(ril);
    }

    public void simHotplugChanged(int indicationType, int[] states) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1520);
        if (this.mRil.mSimHotPlugRegistrants != null) {
            this.mRil.mSimHotPlugRegistrants.notifyRegistrants(new AsyncResult(null, states, null));
        }
    }

    public void simIccidChanged(int indicationType, String data) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1521);
        if (this.mRil.mIccidChangedRegistrants != null) {
            this.mRil.mIccidChangedRegistrants.notifyRegistrants(new AsyncResult(null, data, null));
        }
    }

    public void plmnSearchInfo(int indicationType, int[] states) {
        this.mRil.processIndication(indicationType);
        if (this.mRil.mRegPLMNSelInfoRegistrants != null) {
            this.mRil.mRegPLMNSelInfoRegistrants.notifyRegistrants(new AsyncResult(null, states, null));
        }
    }

    public void crrConnIdd(int indicationType, int[] states) {
        this.mRil.processIndication(indicationType);
        if (this.mRil.mHwCrrConnIndRegistrants != null) {
            this.mRil.mHwCrrConnIndRegistrants.notifyRegistrants(new AsyncResult(null, states, null));
        }
        this.mRil.crrConnRet = states;
    }

    public void residentNetworkChanged(int indicationType, String network) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3001);
        if (this.mRil.mUnsolRplmnsStateRegistrant != null) {
            this.mRil.mUnsolRplmnsStateRegistrant.notifyRegistrants(new AsyncResult(null, network, null));
        }
    }

    public void networkRejectCase(int indicationType, String[] cases) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3006);
        if (this.mRil.mNetRejectRegistrant != null) {
            this.mRil.mNetRejectRegistrant.notifyRegistrant(new AsyncResult(null, cases, null));
        }
    }

    public void rsrvccStateNotify(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1522);
        if (this.mRil.mRSrvccStateRegistrants != null) {
            this.mRil.mRSrvccStateRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
    }

    public void vsimRdhRequest(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3007);
        if (this.mRil.mVsimRDHRegistrant != null) {
            this.mRil.mVsimRDHRegistrant.notifyRegistrant(new AsyncResult(null, null, null));
        }
    }

    public void vsimTeeTaskTimeout(int indicationType, int[] result) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3027);
        if (this.mRil.mVsimTimerTaskExpiredRegistrant != null) {
            this.mRil.mVsimTimerTaskExpiredRegistrant.notifyRegistrant(new AsyncResult(null, result, null));
        }
    }

    public void restartRildNvMatch(int indicationType, int[] result) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3125);
        if (this.mRil.mRestartRildNvMatchRegistrant != null) {
            this.mRil.mRestartRildNvMatchRegistrant.notifyRegistrant(new AsyncResult(null, result, null));
        }
    }

    public void voicePreferenceStatusReport(int indicationType, int state) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3032);
        Rlog.d(LOG_TAG, "notifyVpStatus: state = " + state);
        if (this.mRil.mReportVpStatusRegistrants != null) {
            this.mRil.mReportVpStatusRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(state), null));
        }
    }

    public void callECCXlema(int indicationType, String eccNum) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3005);
        Rlog.d(LOG_TAG, "callECCXlema: eccNum = " + eccNum);
        if (this.mRil.mECCNumRegistrant != null) {
            this.mRil.mECCNumRegistrant.notifyRegistrant(new AsyncResult(null, eccNum, null));
        }
    }

    public void csChannelInfo(int indicationType, int[] channelInfos) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3003);
        if (this.mRil.mSpeechInfoRegistrants != null) {
            Rlog.d(LOG_TAG, "RIL.java is ready for submitting SPEECHINFO");
            this.mRil.mSpeechInfoRegistrants.notifyRegistrants(new AsyncResult(null, channelInfos, null));
        }
    }

    public void limitPdpAct(int indicationType, int[] states) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3051);
        if (this.mRil.mLimitPDPActRegistrants != null) {
            this.mRil.mLimitPDPActRegistrants.notifyRegistrants(new AsyncResult(null, states, null));
        }
    }

    public void balongModemReset(int indicationType, int[] data) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3061);
        if (this.mRil.mBalongModemResetRegistrants != null) {
            this.mRil.mBalongModemResetRegistrants.notifyRegistrants(new AsyncResult(null, data, null));
        }
    }

    public void UnsolMsg(int indicationType, int msgId, RILUnsolMsgPayload payload) {
        this.mRil.unsljLog(msgId);
        switch (msgId) {
            case 1067:
                simHotplugChanged(indicationType, processInts(payload));
                return;
            case 1068:
                simIccidChanged(indicationType, processString(payload));
                return;
            case 1069:
                residentNetworkChanged(indicationType, processString(payload));
                return;
            case 1071:
                csChannelInfo(indicationType, processInts(payload));
                return;
            case 1073:
                callECCXlema(indicationType, processString(payload));
                return;
            case 1074:
                networkRejectCase(indicationType, processStrings(payload));
                return;
            case 1075:
                vsimRdhRequest(indicationType);
                return;
            case 1078:
                plmnSearchInfo(indicationType, processInts(payload));
                return;
            case 1088:
                uimLockcardInd(indicationType, processInts(payload));
                return;
            case 1091:
                balongModemReset(indicationType, processInts(payload));
                return;
            case 1095:
                vsimTeeTaskTimeout(indicationType, processInts(payload));
                return;
            case 1099:
                hwXpassReselectInfo(payload);
                return;
            case 1100:
                voicePreferenceStatusReport(indicationType, processInt(payload));
                return;
            case 1102:
                this.hwRilReferImpl.existNetworkInfo(processString(payload));
                return;
            case 1115:
                crrConnIdd(indicationType, processInts(payload));
                return;
            case 1119:
                limitPdpAct(indicationType, processInts(payload));
                return;
            case 1124:
                rsrvccStateNotify(indicationType);
                return;
            case 1125:
                currentHwSignalStrengthInd(indicationType, convertHwHalSignalStrength(processInts(payload)));
                return;
            case 1126:
                restartRildNvMatch(indicationType, processInts(payload));
                return;
            case 1128:
                laaStateChanged(indicationType, processInts(payload));
                return;
            case 1129:
                callAltSrvInd(indicationType);
                return;
            default:
                return;
        }
    }

    public void currentHwSignalStrengthInd(int indicationType, SignalStrength signalStrength) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogvRet(1125, signalStrength);
        if (this.mRil.mSignalStrengthRegistrant != null) {
            this.mRil.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult(null, signalStrength, null));
        }
    }

    public static SignalStrength convertHwHalSignalStrength(int[] payload) {
        if (payload.length >= 15) {
            return new SignalStrength(payload[0], payload[1], payload[2], payload[3], payload[4], payload[5], payload[6], payload[7], payload[8], payload[9], payload[10], payload[11], payload[12], payload[13], payload[14], true);
        }
        Rlog.d(LOG_TAG, "signal strength data is illegal");
        return new SignalStrength();
    }

    public void imsaToVowifiMsg(int indicationType, ArrayList<Byte> msgs) {
        this.mRil.processIndication(indicationType);
        byte[] msgArray = RIL.arrayListToPrimitiveArray(msgs);
        this.mRil.unsljLog(3041);
        Rlog.d(LOG_TAG, "RIL_UNSOL_HW_IMSA_VOWIFI_MSG " + IccUtils.bytesToHexString(msgArray));
        if (this.mRil.mCommonImsaToMapconInfoRegistrant != null) {
            this.mRil.mCommonImsaToMapconInfoRegistrant.notifyRegistrant(new AsyncResult(null, msgArray, null));
        }
    }

    private int[] processInts(RILUnsolMsgPayload payload) {
        int numInts = payload.nDatas.size();
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = ((Integer) payload.nDatas.get(i)).intValue();
        }
        return response;
    }

    private String[] processStrings(RILUnsolMsgPayload payload) {
        int numStrings = payload.strDatas.size();
        String[] response = new String[numStrings];
        for (int i = 0; i < numStrings; i++) {
            response[i] = (String) payload.strDatas.get(i);
        }
        return response;
    }

    private int processInt(RILUnsolMsgPayload payload) {
        return payload.nData;
    }

    private String processString(RILUnsolMsgPayload payload) {
        return payload.strData;
    }

    public void hwXpassReselectInfo(RILUnsolMsgPayload payload) {
        int[] result_temp = processInts(payload);
        if (result_temp != null) {
            Rlog.d(LOG_TAG, "result_temp[0]=" + result_temp[0] + "   ,result_temp[1]=" + result_temp[1]);
        }
        if (countAfterBoot == 0) {
            Rlog.d(LOG_TAG, "countAfterBoot =" + countAfterBoot);
            if (result_temp == null) {
                return;
            }
            if (result_temp[0] == 1 || result_temp[1] == 1) {
                Intent intent = new Intent();
                intent.setAction(ACTION_HW_XPASS_RESELECT_INFO);
                this.mRil.getContext().sendBroadcast(intent);
                Rlog.d(LOG_TAG, "sendBroadcast:ACTION_HW_XPASS_RESELECT_INFO");
                countAfterBoot = 1;
            }
        }
    }

    public void apDsFlowInfoReport(int indicationType, RILAPDsFlowInfoReport apDsFlowInfo) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3035);
        if (this.mRil.mVsimApDsFlowInfoRegistrant != null) {
            this.mRil.mVsimApDsFlowInfoRegistrant.notifyRegistrant(new AsyncResult(null, new String[]{apDsFlowInfo.currDsTime, apDsFlowInfo.txRate, apDsFlowInfo.rxRate, apDsFlowInfo.currTxFlow, apDsFlowInfo.currRxFlow, apDsFlowInfo.totalTxFlow, apDsFlowInfo.totalRxFlow}, null));
        }
    }

    public void laaStateChanged(int indicationType, int[] laaStates) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3057);
        if (this.mRil.mLaaStateChangeRegistrants != null) {
            Rlog.d(LOG_TAG, "laaStateChanged,notifyRegistrants");
            this.mRil.mLaaStateChangeRegistrants.notifyRegistrants(new AsyncResult(null, laaStates, null));
        }
    }

    public void callAltSrvInd(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3056);
        if (this.mRil.mCallAltSrvRegistrants != null) {
            this.mRil.mCallAltSrvRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
    }

    public void dsFlowInfoReport(int indicationType, RILAPDsFlowInfoReport apDsFlowInfo) {
    }

    public void vsimOtaSmsReport(int indicationType, RILVsimOtaSmsResponse vsimOtaSms) {
    }

    public void imsHandoverInd(int indicationType, RILImsHandover imsHandover) {
    }

    public void imsSrvStatusInd(int type, RILImsSrvstatusList imsSrvStatus) {
    }

    public void imsCallModifyInd(int type, RILImsCallModify imsCallModify) {
    }

    public void imsCallModifyEndCauseInd(int type, RILImsModifyEndCause imsModifyEndCause) {
    }

    public void imsCallMtStatusInd(int type, RILImsMtStatusReport imsCallMtStatus) {
    }

    public void imsSuppSrvInd(int type, RILImsSuppSvcNotification imsSuppSvcNofitication) {
    }

    public void imsaToVowifiMsg(int type, List<Integer> list) {
    }

    public void vtFlowInfoReport(int indicationType, RILVtFlowInfoReport rilVtFlowInfoReport) {
    }

    public void uimLockcardInd(int indicationType, int[] data) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3020);
        this.mRil.notifyIccUimLockRegistrants();
    }
}
