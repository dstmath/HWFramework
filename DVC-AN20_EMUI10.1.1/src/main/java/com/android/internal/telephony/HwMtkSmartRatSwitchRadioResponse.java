package com.android.internal.telephony;

import android.hardware.radio.V1_0.RadioResponseInfo;
import android.telephony.Rlog;
import vendor.huawei.hardware.mtkradio.V1_4.ISmartRatSwitchRadioResponse;

public class HwMtkSmartRatSwitchRadioResponse extends ISmartRatSwitchRadioResponse.Stub {
    private static final String LOG_TAG = "HwMtkSmartRatSwitchRadioResponse";
    private HwMtkRIL mMtkRil;

    public HwMtkSmartRatSwitchRadioResponse(RIL ril) {
        this.mMtkRil = (HwMtkRIL) ril;
    }

    public void smartRatSwitchResponse(RadioResponseInfo responseInfo) {
    }

    public void getSmartRatSwitchResponse(RadioResponseInfo responseInfo, int state) {
    }

    public void setSmartSceneSwitchResponse(RadioResponseInfo responseInfo) {
    }

    private void logi(String msg) {
        Rlog.i("HwMtkSmartRatSwitchRadioResponse[" + this.mMtkRil.mPhoneId + "]", msg);
    }
}
