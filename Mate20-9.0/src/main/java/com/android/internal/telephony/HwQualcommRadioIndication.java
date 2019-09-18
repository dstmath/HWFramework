package com.android.internal.telephony;

import android.os.AsyncResult;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import vendor.huawei.hardware.qcomradio.V1_0.IQcomRadioIndication;
import vendor.huawei.hardware.qcomradio.V1_0.RILUnsolMsgPayload;

public class HwQualcommRadioIndication extends IQcomRadioIndication.Stub {
    private static final int SIGNAL_STRENGTH_DATA_LEN = 15;
    private static final String TAG = "HwQualcommRadioIndication";
    private HwQualcommRIL mQcomRil;

    HwQualcommRadioIndication(RIL ril) {
        this.mQcomRil = (HwQualcommRIL) ril;
    }

    public void UnsolMsg(int indicationType, int msgId, RILUnsolMsgPayload payload) {
        this.mQcomRil.unsljLog(msgId);
        if (msgId == 2077) {
            currentHwSignalStrengthInd(indicationType, convertHwHalSignalStrength(processInts(payload)));
        }
    }

    public void currentHwSignalStrengthInd(int indicationType, SignalStrength signalStrength) {
        this.mQcomRil.processIndication(indicationType);
        this.mQcomRil.unsljLogvRet(2077, signalStrength);
        if (this.mQcomRil.mSignalStrengthRegistrant != null) {
            this.mQcomRil.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult(null, signalStrength, null));
        }
    }

    public static SignalStrength convertHwHalSignalStrength(int[] payload) {
        int[] iArr = payload;
        if (iArr.length < 15) {
            Rlog.d(TAG, "signal strength data is illegal");
            return new SignalStrength();
        }
        SignalStrength signalStrength = new SignalStrength(iArr[0], iArr[1], iArr[9], iArr[10], iArr[11], iArr[12], iArr[13], iArr[14], iArr[15], iArr[16], iArr[17], iArr[18], iArr[20], iArr[5], iArr[7], iArr[6]);
        return signalStrength;
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
}
