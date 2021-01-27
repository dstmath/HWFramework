package com.android.internal.telephony;

import android.telephony.Rlog;
import vendor.huawei.hardware.mtkradio.V1_4.ISmartRatSwitchRadioIndication;

public class HwMtkSmartRatSwitchRadioIndication extends ISmartRatSwitchRadioIndication.Stub {
    private static final String LOG_TAG = "HwMtkSmartRatSwitchRadioInd";
    private HwMtkRIL mMtkRil;

    HwMtkSmartRatSwitchRadioIndication(RIL ril) {
        this.mMtkRil = (HwMtkRIL) ril;
    }

    public void smartRatSwitchInd(int indicationType, int info) {
    }

    public void codecActiveInd(int indicationType, int instanceId, boolean isUse) {
    }

    public void codecFpsInd(int indicationType, int instanceId, int fps) {
    }

    public void codecResolutionInd(int indicationType, int instanceId, int wide, int height) {
    }

    public void codecEmptyInd(int indicationType, int instanceId, boolean isEmpty) {
    }

    private void logi(String msg) {
        Rlog.i("HwMtkSmartRatSwitchRadioInd[" + this.mMtkRil.mPhoneId + "]", msg);
    }
}
