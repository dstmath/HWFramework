package com.android.internal.telephony;

import android.hardware.radio.config.V1_0.IRadioConfigIndication;
import android.hardware.radio.config.V1_0.SimSlotStatus;
import android.os.AsyncResult;
import android.telephony.Rlog;
import com.android.internal.telephony.uicc.IccSlotStatus;
import java.util.ArrayList;

public class RadioConfigIndication extends IRadioConfigIndication.Stub {
    private static final String TAG = "RadioConfigIndication";
    private final RadioConfig mRadioConfig;

    public RadioConfigIndication(RadioConfig radioConfig) {
        this.mRadioConfig = radioConfig;
    }

    public void simSlotsStatusChanged(int indicationType, ArrayList<SimSlotStatus> slotStatus) {
        ArrayList<IccSlotStatus> ret = RadioConfig.convertHalSlotStatus(slotStatus);
        Rlog.d(TAG, "[UNSL]<  UNSOL_SIM_SLOT_STATUS_CHANGED " + ret.toString());
        if (this.mRadioConfig.mSimSlotStatusRegistrant != null) {
            this.mRadioConfig.mSimSlotStatusRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
        }
    }
}
