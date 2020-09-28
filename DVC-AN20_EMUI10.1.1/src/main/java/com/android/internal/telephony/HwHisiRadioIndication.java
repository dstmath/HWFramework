package com.android.internal.telephony;

import android.content.Intent;
import android.os.AsyncResult;
import android.os.Bundle;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.ArrayList;
import vendor.huawei.hardware.hisiradio.V1_0.RILAPDsFlowInfoReport;
import vendor.huawei.hardware.hisiradio.V1_0.RILUnsolMsgPayload;
import vendor.huawei.hardware.hisiradio.V1_0.RILVsimOtaSmsResponse;
import vendor.huawei.hardware.hisiradio.V1_0.RilSysInfor;
import vendor.huawei.hardware.hisiradio.V1_1.HwSignalStrength_1_1;
import vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication;

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
            this.mRil.mSimHotPlugRegistrants.notifyRegistrants(new AsyncResult((Object) null, states, (Throwable) null));
        }
    }

    public void simIccidChanged(int indicationType, String data) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1521);
        if (this.mRil.mIccidChangedRegistrants != null) {
            this.mRil.mIccidChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, data, (Throwable) null));
        }
    }

    public void plmnSearchInfo(int indicationType, int[] states) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3010);
        if (this.mRil.mRegPLMNSelInfoRegistrants != null) {
            this.mRil.mRegPLMNSelInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, states, (Throwable) null));
        }
    }

    public void crrConnIdd(int indicationType, int[] states) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3047);
        if (this.mRil.mHwCrrConnIndRegistrants != null) {
            this.mRil.mHwCrrConnIndRegistrants.notifyRegistrants(new AsyncResult((Object) null, states, (Throwable) null));
        }
        this.mRil.crrConnRet = states;
    }

    public void dsdsMode(int indicationType, int[] states) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3048);
        if (this.mRil.mDSDSModeStateRegistrants != null) {
            this.mRil.mDSDSModeStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, states, (Throwable) null));
        }
    }

    public void networkRejectCase(int indicationType, String[] cases) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3006);
        if (this.mRil.mNetRejectRegistrant != null) {
            this.mRil.mNetRejectRegistrant.notifyRegistrants(new AsyncResult((Object) null, cases, (Throwable) null));
        }
    }

    public void rsrvccStateNotify(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(1522);
        if (this.mRil.mRSrvccStateRegistrants != null) {
            this.mRil.mRSrvccStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    public void vsimRdhRequest(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3007);
        if (this.mRil.mVsimRDHRegistrant != null) {
            this.mRil.mVsimRDHRegistrant.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        }
    }

    public void vsimTeeTaskTimeout(int indicationType, int[] result) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3027);
        if (this.mRil.mVsimTimerTaskExpiredRegistrant != null) {
            this.mRil.mVsimTimerTaskExpiredRegistrant.notifyRegistrant(new AsyncResult((Object) null, result, (Throwable) null));
        }
    }

    public void restartRildNvMatch(int indicationType, int[] result) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3125);
        if (this.mRil.mRestartRildNvMatchRegistrant != null) {
            this.mRil.mRestartRildNvMatchRegistrant.notifyRegistrant(new AsyncResult((Object) null, result, (Throwable) null));
        }
    }

    public void voicePreferenceStatusReport(int indicationType, int state) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3032);
        Rlog.d(LOG_TAG, "notifyVpStatus: state = " + state);
        if (this.mRil.mReportVpStatusRegistrants != null) {
            this.mRil.mReportVpStatusRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(state), (Throwable) null));
        }
    }

    public void limitPdpAct(int indicationType, int[] states) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3051);
        if (this.mRil.mLimitPDPActRegistrants != null) {
            this.mRil.mLimitPDPActRegistrants.notifyRegistrants(new AsyncResult((Object) null, states, (Throwable) null));
        }
    }

    public void balongModemReset(int indicationType, int[] data) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3061);
        if (this.mRil.mBalongModemResetRegistrants != null) {
            this.mRil.mBalongModemResetRegistrants.notifyRegistrants(new AsyncResult((Object) null, data, (Throwable) null));
        }
    }

    public void mimo4REnable(int indicationType, int[] data) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3059);
        if (this.mRil.m4RMimoStatusRegistrants != null) {
            this.mRil.m4RMimoStatusRegistrants.notifyRegistrants(new AsyncResult((Object) null, data, (Throwable) null));
        }
        this.mRil.lastMimoStatus = data;
    }

    public void reportDl256QamState(int indicationType, int data) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3074);
        int[] state = {data};
        if (this.mRil.m256QamStatusRegistrants != null) {
            this.mRil.m256QamStatusRegistrants.notifyRegistrants(new AsyncResult((Object) null, state, (Throwable) null));
        }
        this.mRil.mLast256QamStatus = state;
    }

    public void UnsolMsg(int indicationType, int msgId, RILUnsolMsgPayload payload) {
        this.mRil.unsljLog(msgId);
        if (msgId == 2019) {
            simHotplugChanged(indicationType, processInts(payload));
        } else if (msgId == 2020) {
            simIccidChanged(indicationType, processString(payload));
        } else if (msgId == 2026) {
            networkRejectCase(indicationType, processStrings(payload));
        } else if (msgId == 2027) {
            vsimRdhRequest(indicationType);
        } else if (msgId == 2030) {
            plmnSearchInfo(indicationType, processInts(payload));
        } else if (msgId == 2043) {
            balongModemReset(indicationType, processInts(payload));
        } else if (msgId == 2047) {
            vsimTeeTaskTimeout(indicationType, processInts(payload));
        } else if (msgId == 2067) {
            crrConnIdd(indicationType, processInts(payload));
        } else if (msgId == 2071) {
            limitPdpAct(indicationType, processInts(payload));
        } else if (msgId == 2086) {
            dsdsMode(indicationType, processInts(payload));
        } else if (msgId == 2088) {
            mimo4REnable(indicationType, processInts(payload));
        } else if (msgId == 2091) {
            simMatchedNvCfgFinishedInd(indicationType, processInt(payload));
        } else if (msgId == 2051) {
            hwXpassReselectInfo(payload);
        } else if (msgId == 2052) {
            voicePreferenceStatusReport(indicationType, processInt(payload));
        } else if (msgId == 2080) {
            laaStateChanged(indicationType, processInts(payload));
        } else if (msgId != 2081) {
            switch (msgId) {
                case 2076:
                    rsrvccStateNotify(indicationType);
                    return;
                case 2077:
                    currentHwSignalStrengthInd(indicationType, convertHwHalSignalStrength(processInts(payload), this.mRil.mPhoneId.intValue()));
                    return;
                case 2078:
                    restartRildNvMatch(indicationType, processInts(payload));
                    return;
                default:
                    return;
            }
        } else {
            callAltSrvInd(indicationType);
        }
    }

    public void currentHwSignalStrengthInd(int indicationType, SignalStrength signalStrength) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogvRet(1125, signalStrength);
        if (this.mRil.mSignalStrengthRegistrant != null) {
            this.mRil.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult((Object) null, signalStrength, (Throwable) null));
        }
    }

    public static SignalStrength convertHwHalSignalStrength(int[] payload, int phoneId) {
        if (payload.length < 15) {
            Rlog.d(LOG_TAG, "signal strength data is illegal");
            return new SignalStrength();
        }
        SignalStrength signalStrength = new SignalStrength(new CellSignalStrengthCdma(payload[4], payload[5], payload[6], payload[7], payload[8]), new CellSignalStrengthGsm(payload[0], payload[1], Integer.MAX_VALUE), new CellSignalStrengthWcdma(Integer.MAX_VALUE, Integer.MAX_VALUE, payload[2], Integer.MAX_VALUE, payload[3]), new CellSignalStrengthTdscdma(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthLte(payload[9], payload[10], payload[11], payload[12], payload[13], Integer.MAX_VALUE), new CellSignalStrengthNr());
        signalStrength.setPhoneId(phoneId);
        return signalStrength;
    }

    public void imsaToVowifiMsg(int indicationType, ArrayList<Byte> msgs) {
        this.mRil.processIndication(indicationType);
        byte[] msgArray = RIL.arrayListToPrimitiveArray(msgs);
        this.mRil.unsljLog(3041);
        Rlog.d(LOG_TAG, "RIL_UNSOL_HW_IMSA_VOWIFI_MSG " + IccUtils.bytesToHexString(msgArray));
        if (this.mRil.mCommonImsaToMapconInfoRegistrant != null) {
            this.mRil.mCommonImsaToMapconInfoRegistrant.notifyRegistrant(new AsyncResult((Object) null, msgArray, (Throwable) null));
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
            this.mRil.mVsimApDsFlowInfoRegistrant.notifyRegistrant(new AsyncResult((Object) null, new String[]{apDsFlowInfo.currDsTime, apDsFlowInfo.txRate, apDsFlowInfo.rxRate, apDsFlowInfo.currTxFlow, apDsFlowInfo.currRxFlow, apDsFlowInfo.totalTxFlow, apDsFlowInfo.totalRxFlow}, (Throwable) null));
        }
    }

    public void laaStateChanged(int indicationType, int[] laaStates) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3057);
        if (this.mRil.mLaaStateChangeRegistrants != null) {
            logi("laaStateChanged,notifyRegistrants");
            this.mRil.mLaaStateChangeRegistrants.notifyRegistrants(new AsyncResult((Object) null, laaStates, (Throwable) null));
        }
    }

    public void callAltSrvInd(int indicationType) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3056);
        if (this.mRil.mCallAltSrvRegistrants != null) {
            this.mRil.mCallAltSrvRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
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
        logi("simMatchedNvCfgFinishedInd: result = " + result);
        if (this.mRil.mNvCfgFinishedRegistrants != null) {
            this.mRil.mNvCfgFinishedRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(result), (Throwable) null));
        }
    }

    public void updateUlfreqRPT(int type, int rat, int ulfreq, int ulbw) {
        this.mRil.unsljLog(type);
        logi("updateUlfreqRPT received : type = " + type + " rat = " + rat + " ulfreq = " + ulfreq + " ulbw = " + ulbw);
        if (this.mRil.mUlfreqStateRegistrants != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("rat", rat);
            bundle.putInt("ulfreq", ulfreq);
            bundle.putInt("ulbw", ulbw);
            this.mRil.mUlfreqStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, bundle, (Throwable) null));
        }
    }

    public void simMatchRestartRildInd(int indicationType, int result) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3060);
        if (this.mRil.mHwRestartRildStatusRegistrants != null) {
            this.mRil.mHwRestartRildStatusRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(result), (Throwable) null));
        }
    }

    public void recPseBaseStationReport(int indicationType, int result) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3062);
        logi("recPseBaseStationReport: result = " + result);
        if (this.mRil.mHwAntiFakeBaseStationRegistrants != null) {
            this.mRil.mHwAntiFakeBaseStationRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(result), (Throwable) null));
        }
    }

    public void currentHwSignalStrength_1_1(int indicationType, HwSignalStrength_1_1 signalStrength) {
        this.mRil.processIndication(indicationType);
        HwHisiRIL hwHisiRIL = this.mRil;
        SignalStrength ss = hwHisiRIL.convertHalSignalStrength_1_1(signalStrength, hwHisiRIL.mPhoneId.intValue());
        this.mRil.unsljLogRet(1125, ss);
        if (this.mRil.mSignalStrengthRegistrant != null) {
            this.mRil.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult((Object) null, ss, (Throwable) null));
        }
    }

    private void logi(String msg) {
        Rlog.i("HwRadioIndication[" + this.mRil.mPhoneId + "]", msg);
    }

    public void currentRrcConnetionState(int indicationType, int result) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(3122, Integer.valueOf(result));
        if (this.mRil.mHwRrcConnStateRegistrants != null) {
            this.mRil.mHwRrcConnStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(result), (Throwable) null));
        }
    }
}
