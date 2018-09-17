package com.android.internal.telephony;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.deprecated.V1_0.IOemHookResponse.Stub;
import java.util.ArrayList;

public class OemHookResponse extends Stub {
    RIL mRil;

    public OemHookResponse(RIL ril) {
        this.mRil = ril;
    }

    public void sendRequestRawResponse(RadioResponseInfo responseInfo, ArrayList<Byte> data) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        if (rr != null) {
            Object ret = null;
            if (responseInfo.error == 0) {
                ret = RIL.arrayListToPrimitiveArray(data);
                RadioResponse.sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }

    public void sendRequestStringsResponse(RadioResponseInfo responseInfo, ArrayList<String> data) {
        RadioResponse.responseStringArrayList(this.mRil, responseInfo, data);
    }
}
