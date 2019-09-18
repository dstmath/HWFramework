package com.android.internal.telephony;

import android.content.Intent;
import android.os.AsyncResult;
import android.os.Bundle;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.ArrayList;
import vendor.huawei.hardware.hisiradio.V1_0.RILAPDsFlowInfoReport;
import vendor.huawei.hardware.hisiradio.V1_0.RILUnsolMsgPayload;
import vendor.huawei.hardware.hisiradio.V1_0.RILVsimOtaSmsResponse;
import vendor.huawei.hardware.hisiradio.V1_0.RilSysInfor;
import vendor.huawei.hardware.hisiradio.V1_1.HwSignalStrength_1_1;
import vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication;

public class HwHisiRadioIndication extends IHisiRadioIndication.Stub {
    private static final String ACTION_HW_XPASS_RESELECT_INFO = "android.intent.action.HW_XPASS_RESELECT_INFO";
    private static final int AS_DS_FLOW_INFO_REPORT_ARRAY_LENGTH = 7;
    private static final String LOG_TAG = "HwRadioIndication";
    private static final int PROP_VALUE_STEP = 2;
    private static final int SIGNAL_STRENGTH_DATA_LEN = 15;
    private int countAfterBoot = 0;
    HwHisiRIL mRil;

    public HwHisiRadioIndication() {
    }

    HwHisiRadioIndication(HwHisiRIL ril) {
        this.mRil = ril;
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
        this.mRil.unsljLog(3010);
        if (this.mRil.mRegPLMNSelInfoRegistrants != null) {
            this.mRil.mRegPLMNSelInfoRegistrants.notifyRegistrants(new AsyncResult(null, states, null));
        }
    }

    public void crrConnIdd(int indicationType, int[] states) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3047);
        if (this.mRil.mHwCrrConnIndRegistrants != null) {
            this.mRil.mHwCrrConnIndRegistrants.notifyRegistrants(new AsyncResult(null, states, null));
        }
        this.mRil.crrConnRet = states;
    }

    public void dsdsMode(int indicationType, int[] states) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3048);
        if (this.mRil.mDSDSModeStateRegistrants != null) {
            this.mRil.mDSDSModeStateRegistrants.notifyRegistrants(new AsyncResult(null, states, null));
        }
    }

    public void networkRejectCase(int indicationType, String[] cases) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3006);
        if (this.mRil.mNetRejectRegistrant != null) {
            this.mRil.mNetRejectRegistrant.notifyRegistrants(new AsyncResult(null, cases, null));
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

    public void mimo4REnable(int indicationType, int[] data) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3059);
        if (this.mRil.m4RMimoStatusRegistrants != null) {
            this.mRil.m4RMimoStatusRegistrants.notifyRegistrants(new AsyncResult(null, data, null));
        }
        this.mRil.lastMimoStatus = data;
    }

    public void UnsolMsg(int indicationType, int msgId, RILUnsolMsgPayload payload) {
        this.mRil.unsljLog(msgId);
        switch (msgId) {
            case 2019:
                simHotplugChanged(indicationType, processInts(payload));
                return;
            case 2020:
                simIccidChanged(indicationType, processString(payload));
                return;
            case 2026:
                networkRejectCase(indicationType, processStrings(payload));
                return;
            case 2027:
                vsimRdhRequest(indicationType);
                return;
            case 2030:
                plmnSearchInfo(indicationType, processInts(payload));
                return;
            case 2043:
                balongModemReset(indicationType, processInts(payload));
                return;
            case 2047:
                vsimTeeTaskTimeout(indicationType, processInts(payload));
                return;
            case 2051:
                hwXpassReselectInfo(payload);
                return;
            case 2052:
                voicePreferenceStatusReport(indicationType, processInt(payload));
                return;
            case 2067:
                crrConnIdd(indicationType, processInts(payload));
                return;
            case 2071:
                limitPdpAct(indicationType, processInts(payload));
                return;
            case 2076:
                rsrvccStateNotify(indicationType);
                return;
            case 2077:
                currentHwSignalStrengthInd(indicationType, convertHwHalSignalStrength(processInts(payload), this.mRil.mPhoneId.intValue()));
                return;
            case 2078:
                restartRildNvMatch(indicationType, processInts(payload));
                return;
            case 2080:
                laaStateChanged(indicationType, processInts(payload));
                return;
            case 2081:
                callAltSrvInd(indicationType);
                return;
            case 2086:
                dsdsMode(indicationType, processInts(payload));
                return;
            case 2088:
                mimo4REnable(indicationType, processInts(payload));
                return;
            case 2091:
                simMatchedNvCfgFinishedInd(indicationType, processInt(payload));
                return;
            default:
                return;
        }
    }

    public void currentHwSignalStrengthInd(int indicationType, SignalStrength signalStrength) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogvRet(2077, signalStrength);
        if (this.mRil.mSignalStrengthRegistrant != null) {
            this.mRil.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult(null, signalStrength, null));
        }
    }

    public static SignalStrength convertHwHalSignalStrength(int[] payload, int phoneId) {
        int[] iArr = payload;
        if (iArr.length < 15) {
            Rlog.d(LOG_TAG, "signal strength data is illegal");
            return new SignalStrength();
        }
        SignalStrength signalStrength = new SignalStrength(iArr[0], iArr[1], iArr[4], iArr[5], iArr[6], iArr[7], iArr[8], iArr[9], iArr[10], iArr[11], iArr[12], iArr[13], iArr[14], 0, iArr[2], iArr[3], phoneId);
        return signalStrength;
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
        Rlog.d(LOG_TAG, "result_temp[0]=" + result_temp[0] + "   ,result_temp[1]=" + result_temp[1]);
        if (this.countAfterBoot == 0) {
            Rlog.d(LOG_TAG, "countAfterBoot =" + this.countAfterBoot);
            if (result_temp[0] == 1 || result_temp[1] == 1) {
                Intent intent = new Intent();
                intent.setAction(ACTION_HW_XPASS_RESELECT_INFO);
                this.mRil.getContext().sendBroadcast(intent);
                Rlog.d(LOG_TAG, "sendBroadcast:ACTION_HW_XPASS_RESELECT_INFO");
                this.countAfterBoot = 1;
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

    public void sysInforInd(int indicationType, RilSysInfor rilVtFlowInfoReport) {
    }

    public void simMatchedNvCfgFinishedInd(int indicationType, int result) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3058);
        Rlog.d(LOG_TAG, "simMatchedNvCfgFinishedInd: result = " + result);
        if (this.mRil.mNvCfgFinishedRegistrants != null) {
            this.mRil.mNvCfgFinishedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(result), null));
        }
    }

    public void updateUlfreqRPT(int type, int rat, int ulfreq, int ulbw) {
        this.mRil.unsljLog(type);
        Rlog.d(LOG_TAG, "updateUlfreqRPT received : type = " + type + ",rat = " + rat + "(1.wcdma;2.HRPD;3.LTE), ulfreq = " + ulfreq + "(Hz*10), ulbw = " + ulbw + "(k)");
        if (this.mRil.mUlfreqStateRegistrants != null) {
            Rlog.d(LOG_TAG, "uplink frequency report ,notifyRegistrants");
            Bundle bundle = new Bundle();
            bundle.putInt("rat", rat);
            bundle.putInt("ulfreq", ulfreq);
            bundle.putInt("ulbw", ulbw);
            this.mRil.mUlfreqStateRegistrants.notifyRegistrants(new AsyncResult(null, bundle, null));
        }
    }

    public void simMatchRestartRildInd(int indicationType, int result) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3060);
        if (this.mRil.mHwRestartRildStatusRegistrants != null) {
            this.mRil.mHwRestartRildStatusRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(result), null));
        }
    }

    public void recPseBaseStationReport(int indicationType, int result) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3062);
        Rlog.d(LOG_TAG, "recPseBaseStationReport: result = " + result);
        if (this.mRil.mHwAntiFakeBaseStationRegistrants != null) {
            this.mRil.mHwAntiFakeBaseStationRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(result), null));
        }
    }

    public void currentHwSignalStrength_1_1(int indicationType, HwSignalStrength_1_1 signalStrength) {
        this.mRil.processIndication(indicationType);
        SignalStrength ss = this.mRil.convertHalSignalStrength_1_1(signalStrength, this.mRil.mPhoneId.intValue());
        this.mRil.unsljLogRet(1125, ss);
        if (this.mRil.mSignalStrengthRegistrant != null) {
            this.mRil.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult(null, ss, null));
        }
    }
}
