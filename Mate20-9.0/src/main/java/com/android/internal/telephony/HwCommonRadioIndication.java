package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.RemoteException;
import android.telephony.Rlog;
import vendor.huawei.hardware.radio.V2_0.IRadioIndication;
import vendor.huawei.hardware.radio.V2_0.RILUnsolMsgPayload;

public class HwCommonRadioIndication extends IRadioIndication.Stub {
    private RIL mRil;
    private String mTag = ("HwCommonRadioIndication[" + this.mRil.mPhoneId + "]");

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
        Rlog.d(this.mTag, msg);
    }

    private void handleExistNetworkInfo(String state) {
        this.mRil.existNetworkInfo(state);
    }

    private void handleCsChannelInfo(int indicationType, int[] channelInfos) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3003);
        if (this.mRil.mSpeechInfoRegistrants != null) {
            logd("RIL.java is ready for submitting SPEECHINFO");
            this.mRil.mSpeechInfoRegistrants.notifyRegistrants(new AsyncResult(null, channelInfos, null));
        }
    }

    private void handleResidentNetworkChanged(int indicationType, String network) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3001);
        if (this.mRil.mUnsolRplmnsStateRegistrant != null) {
            this.mRil.mUnsolRplmnsStateRegistrant.notifyRegistrants(new AsyncResult(null, network, null));
        }
    }

    private void handleECCXlema(int indicationType, String eccNum) {
        this.mRil.processIndication(indicationType);
        this.mRil.unsljLog(3005);
        logd("handleECCXlema: eccNum = " + eccNum);
        if (this.mRil.mECCNumRegistrant != null) {
            this.mRil.mECCNumRegistrant.notifyRegistrant(new AsyncResult(null, eccNum, null));
        }
    }
}
