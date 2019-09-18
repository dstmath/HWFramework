package com.android.internal.telephony;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.config.V1_0.IRadioConfigResponse;
import android.hardware.radio.config.V1_0.SimSlotStatus;
import android.telephony.Rlog;
import com.android.internal.telephony.uicc.IccSlotStatus;
import java.util.ArrayList;

public class RadioConfigResponse extends IRadioConfigResponse.Stub {
    private static final String TAG = "RadioConfigResponse";
    private final RadioConfig mRadioConfig;

    public RadioConfigResponse(RadioConfig radioConfig) {
        this.mRadioConfig = radioConfig;
    }

    public void getSimSlotsStatusResponse(RadioResponseInfo responseInfo, ArrayList<SimSlotStatus> slotStatus) {
        RILRequest rr = this.mRadioConfig.processResponse(responseInfo);
        if (rr != null) {
            ArrayList<IccSlotStatus> ret = RadioConfig.convertHalSlotStatus(slotStatus);
            if (responseInfo.error == 0) {
                RadioResponse.sendMessageResponse(rr.mResult, ret);
                StringBuilder sb = new StringBuilder();
                sb.append(rr.serialString());
                sb.append("< ");
                RadioConfig radioConfig = this.mRadioConfig;
                sb.append(RadioConfig.requestToString(rr.mRequest));
                sb.append(" ");
                sb.append(ret.toString());
                Rlog.d(TAG, sb.toString());
                return;
            }
            rr.onError(responseInfo.error, ret);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(rr.serialString());
            sb2.append("< ");
            RadioConfig radioConfig2 = this.mRadioConfig;
            sb2.append(RadioConfig.requestToString(rr.mRequest));
            sb2.append(" error ");
            sb2.append(responseInfo.error);
            Rlog.e(TAG, sb2.toString());
            return;
        }
        Rlog.e(TAG, "getSimSlotsStatusResponse: Error " + responseInfo.toString());
    }

    public void setSimSlotsMappingResponse(RadioResponseInfo responseInfo) {
        RILRequest rr = this.mRadioConfig.processResponse(responseInfo);
        if (rr == null) {
            Rlog.e(TAG, "setSimSlotsMappingResponse: Error " + responseInfo.toString());
        } else if (responseInfo.error == 0) {
            RadioResponse.sendMessageResponse(rr.mResult, null);
            StringBuilder sb = new StringBuilder();
            sb.append(rr.serialString());
            sb.append("< ");
            RadioConfig radioConfig = this.mRadioConfig;
            sb.append(RadioConfig.requestToString(rr.mRequest));
            Rlog.d(TAG, sb.toString());
        } else {
            rr.onError(responseInfo.error, null);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(rr.serialString());
            sb2.append("< ");
            RadioConfig radioConfig2 = this.mRadioConfig;
            sb2.append(RadioConfig.requestToString(rr.mRequest));
            sb2.append(" error ");
            sb2.append(responseInfo.error);
            Rlog.e(TAG, sb2.toString());
        }
    }
}
