package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import java.util.ArrayList;
import java.util.Arrays;
import vendor.huawei.hardware.radio.V2_0.RILUnsolMsgPayload;
import vendor.huawei.hardware.radio.V2_1.HwSignalStrength_2_1;
import vendor.huawei.hardware.radio.V2_2.ApDsFlowInfoReport;
import vendor.huawei.hardware.radio.V2_2.IRadioIndication;

public class HwCommonRadioIndication extends IRadioIndication.Stub {
    private static final int AS_DS_FLOW_INFO_REPORT_ARRAY_LENGTH = 7;
    private static final String LOG_TAG = "HwCommonRadioIndication";
    private static final int SIGNAL_EXCELLENT = -98;
    private static final int SIGNAL_GOOD = -111;
    private static final int SIGNAL_GREAT = -106;
    private static final int SIGNAL_MODERATE = -116;
    private static final int SIGNAL_POOR = -121;
    private static final int SIGNAL_STRENGTH_EXCELLENT = 5;
    private RIL mRil;

    HwCommonRadioIndication(RIL ril) {
        this.mRil = ril;
    }

    public void UnsolMsg(int indicationType, int msgId, RILUnsolMsgPayload payload) throws RemoteException {
        this.mRil.unsljLog(msgId);
        if (msgId == 2021) {
            handleResidentNetworkChanged(indicationType, processString(payload));
        } else if (msgId == 2023) {
            handleCsChannelInfo(indicationType, processInts(payload));
        } else if (msgId == 2030) {
            plmnSearchInfo(indicationType, processInts(payload));
        } else if (msgId == 2054) {
            handleExistNetworkInfo(processString(payload));
        } else if (msgId == 2086) {
            dsdsMode(indicationType, processInts(payload));
        } else if (msgId == 2088) {
            mimo4REnable(indicationType, processInts(payload));
        } else if (msgId == 2103) {
            reportDl256QamState(indicationType, processInts(payload));
        } else if (msgId == 2107) {
            plmnListSearchResult(convertHwHalOperators(processStrings(payload)));
        } else if (msgId == 2025) {
            handleECCXlema(indicationType, processString(payload));
        } else if (msgId == 2026) {
            networkRejectCase(indicationType, processStrings(payload));
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

    private String processString(RILUnsolMsgPayload payload) {
        return payload.strData;
    }

    private void logd(String msg) {
        Rlog.d("HwCommonRadioIndication[" + this.mRil.mPhoneId + "]", msg);
    }

    private void logi(String msg) {
        Rlog.i("HwCommonRadioIndication[" + this.mRil.mPhoneId + "]", msg);
    }

    private void handleExistNetworkInfo(String state) {
        this.mRil.existNetworkInfo(state);
    }

    private void handleCsChannelInfo(int indicationType, int[] channelInfos) {
        this.mRil.processIndication(indicationType);
        this.mRil.recordVoiceBandInfo(channelInfos);
        this.mRil.unsljLog(3003);
        if (this.mRil.mSpeechInfoRegistrants != null) {
            logi("RIL.java is ready for submitting SPEECHINFO");
            this.mRil.mSpeechInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, channelInfos, (Throwable) null));
        }
    }

    private void handleResidentNetworkChanged(int indicationType, String network) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3001);
        if (this.mRil.mUnsolRplmnsStateRegistrant != null) {
            this.mRil.mUnsolRplmnsStateRegistrant.notifyRegistrants(new AsyncResult((Object) null, network, (Throwable) null));
        }
    }

    private void handleECCXlema(int indicationType, String eccNum) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3005);
        logi("handleECCXlema: eccNum = " + eccNum);
        if (this.mRil.mECCNumRegistrant != null) {
            this.mRil.mECCNumRegistrant.notifyRegistrant(new AsyncResult((Object) null, eccNum, (Throwable) null));
        }
    }

    public void currentHwSignalStrength_2_1(int indicationType, HwSignalStrength_2_1 signalStrength) {
        this.mRil.processIndication(indicationType);
        RIL ril = this.mRil;
        SignalStrength ss = ril.convertHalSignalStrength_2_1(signalStrength, ril.mPhoneId.intValue());
        this.mRil.unsljLogRet(1125, ss);
        if (this.mRil.mSignalStrengthRegistrant != null) {
            this.mRil.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult((Object) null, ss, (Throwable) null));
        }
    }

    public void currentRrcConnetionState(int indicationType, int state) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLogRet(3122, Integer.valueOf(state));
        if (this.mRil.mHwRrcConnStateRegistrants != null) {
            this.mRil.mHwRrcConnStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, Integer.valueOf(state), (Throwable) null));
        }
    }

    public void plmnSearchInfo(int indicationType, int[] states) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3010);
        if (this.mRil.mRegPLMNSelInfoRegistrants != null) {
            this.mRil.mRegPLMNSelInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, states, (Throwable) null));
        }
    }

    public void networkRejectCase(int indicationType, String[] cases) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3006);
        if (this.mRil.mNetRejectRegistrant != null) {
            this.mRil.mNetRejectRegistrant.notifyRegistrants(new AsyncResult((Object) null, cases, (Throwable) null));
        }
    }

    public void plmnListSearchResult(ArrayList<OperatorInfo> ret) {
        this.mRil.unsljLogRet(2107, ret);
        this.mRil.mOperatorRegistrants.notifyRegistrants(new AsyncResult((Object) null, ret, (Throwable) null));
    }

    public void dsdsMode(int indicationType, int[] states) {
        logi("dsdsMode indicationType=" + indicationType + ",states=" + Arrays.toString(states));
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3048);
        if (this.mRil.mDSDSModeStateRegistrants != null) {
            this.mRil.mDSDSModeStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, states, (Throwable) null));
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

    public void reportDl256QamState(int indicationType, int[] data) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3074);
        if (this.mRil.m256QamStatusRegistrants != null) {
            this.mRil.m256QamStatusRegistrants.notifyRegistrants(new AsyncResult((Object) null, data, (Throwable) null));
        }
        this.mRil.mLast256QamStatus = data;
    }

    private String[] processStrings(RILUnsolMsgPayload payload) {
        int numStrings = payload.strDatas.size();
        String[] response = new String[numStrings];
        for (int i = 0; i < numStrings; i++) {
            response[i] = (String) payload.strDatas.get(i);
        }
        return response;
    }

    /* JADX INFO: Multiple debug info for r1v3 java.lang.String: [D('alphaLong' java.lang.String), D('index' int)] */
    /* JADX INFO: Multiple debug info for r2v3 java.lang.String: [D('alphaShort' java.lang.String), D('index' int)] */
    /* JADX INFO: Multiple debug info for r3v2 java.lang.String: [D('index' int), D('operatorNumeric' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r4v1 java.lang.String: [D('status' java.lang.String), D('index' int)] */
    private ArrayList<OperatorInfo> convertHwHalOperators(String[] response) {
        ArrayList<OperatorInfo> operators = new ArrayList<>();
        int index = 0;
        while (index < response.length) {
            try {
                int index2 = index + 1;
                String alphaLong = response[index];
                int index3 = index2 + 1;
                String alphaShort = response[index2];
                int index4 = index3 + 1;
                String operatorNumeric = response[index3];
                int index5 = index4 + 1;
                String status = response[index4];
                int index6 = index5 + 1;
                int rsrp = Integer.parseInt(response[index5]);
                OperatorInfo operator = new OperatorInfo(alphaLong, alphaShort, operatorNumeric, status);
                operator.setLevel(getLevel(rsrp));
                operators.add(operator);
                index = index6;
            } catch (NumberFormatException e) {
                Rlog.e(LOG_TAG, "convertHwHalOperators is Exception ");
            }
        }
        return operators;
    }

    private int getLevel(int rsrp) {
        if (rsrp <= SIGNAL_POOR) {
            return 0;
        }
        if (rsrp <= SIGNAL_MODERATE) {
            return 1;
        }
        if (rsrp <= SIGNAL_GOOD) {
            return 2;
        }
        if (rsrp <= SIGNAL_GREAT) {
            return 3;
        }
        if (rsrp <= SIGNAL_EXCELLENT) {
            return 4;
        }
        return 5;
    }

    public void apDsFlowInfoReport(int indicationType, ApDsFlowInfoReport apDsFlowInfo) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3035);
        if (this.mRil.mVsimApDsFlowInfoRegistrant != null) {
            this.mRil.mVsimApDsFlowInfoRegistrant.notifyRegistrant(new AsyncResult((Object) null, new String[]{apDsFlowInfo.currDsTime, apDsFlowInfo.txRate, apDsFlowInfo.rxRate, apDsFlowInfo.currTxFlow, apDsFlowInfo.currRxFlow, apDsFlowInfo.totalTxFlow, apDsFlowInfo.totalRxFlow}, (Throwable) null));
        }
    }

    public void teeTimeOutReport(int indicationType, int taskId) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3027);
        if (this.mRil.mVsimTimerTaskExpiredRegistrant != null) {
            this.mRil.mVsimTimerTaskExpiredRegistrant.notifyRegistrant(new AsyncResult((Object) null, new int[]{taskId}, (Throwable) null));
        }
    }
}
