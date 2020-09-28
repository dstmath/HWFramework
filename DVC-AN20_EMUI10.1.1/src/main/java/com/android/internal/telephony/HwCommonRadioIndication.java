package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import vendor.huawei.hardware.radio.V2_0.RILUnsolMsgPayload;
import vendor.huawei.hardware.radio.V2_1.HwSignalStrength_2_1;
import vendor.huawei.hardware.radio.V2_1.IRadioIndication;

public class HwCommonRadioIndication extends IRadioIndication.Stub {
    private static final String LOG_TAG = "HwCommonRadioIndication";
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
        } else if (msgId == 2025) {
            handleECCXlema(indicationType, processString(payload));
        } else if (msgId == 2054) {
            handleExistNetworkInfo(processString(payload));
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
}
