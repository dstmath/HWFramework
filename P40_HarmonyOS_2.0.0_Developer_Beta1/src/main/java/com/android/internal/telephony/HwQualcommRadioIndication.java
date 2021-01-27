package com.android.internal.telephony;

import android.os.AsyncResult;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
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

    public static SignalStrength convertHwHalSignalStrength(int[] payload) {
        if (payload.length >= SIGNAL_STRENGTH_DATA_LEN) {
            return new SignalStrength(new CellSignalStrengthCdma(payload[4], payload[5], payload[6], payload[7], payload[8]), new CellSignalStrengthGsm(payload[0], payload[1], Integer.MAX_VALUE), new CellSignalStrengthWcdma(Integer.MAX_VALUE, Integer.MAX_VALUE, payload[2], Integer.MAX_VALUE, payload[3]), new CellSignalStrengthTdscdma(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthLte(payload[9], payload[10], payload[11], payload[12], payload[13], Integer.MAX_VALUE), new CellSignalStrengthNr());
        }
        Rlog.d(TAG, "signal strength data is illegal");
        return new SignalStrength();
    }

    public void UnsolMsg(int indicationType, int msgId, RILUnsolMsgPayload payload) {
        this.mQcomRil.unsljLog(msgId);
        if (msgId == 2019) {
            simHotplugChanged(indicationType, processInts(payload));
        } else if (msgId == 2077) {
            currentHwSignalStrengthInd(indicationType, convertHwHalSignalStrength(processInts(payload)));
        }
    }

    private void currentHwSignalStrengthInd(int indicationType, SignalStrength signalStrength) {
        this.mQcomRil.processIndication(indicationType);
        this.mQcomRil.unsljLogvRet(1125, signalStrength);
        if (this.mQcomRil.mSignalStrengthRegistrant != null) {
            this.mQcomRil.mSignalStrengthRegistrant.notifyRegistrant(new AsyncResult((Object) null, signalStrength, (Throwable) null));
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

    private void simHotplugChanged(int indicationType, int[] states) {
        this.mQcomRil.processIndication(indicationType);
        this.mQcomRil.unsljLogvRet(1520, states);
        if (this.mQcomRil.mSimHotPlugRegistrants != null) {
            this.mQcomRil.mSimHotPlugRegistrants.notifyRegistrants(new AsyncResult((Object) null, states, (Throwable) null));
        }
    }
}
