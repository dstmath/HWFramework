package com.android.internal.telephony;

import android.hardware.radio.V1_0.RadioResponseInfo;
import java.util.ArrayList;
import vendor.huawei.hardware.radio.deprecated.V1_0.IOemHookResponse;

public class HwOemHookResponse extends IOemHookResponse.Stub {
    private RIL mRil;

    HwOemHookResponse(RIL ril) {
        this.mRil = ril;
    }

    public void sendRequestRawResponse(RadioResponseInfo responseInfo, ArrayList<Byte> data) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        if (rr != null) {
            byte[] ret = null;
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
