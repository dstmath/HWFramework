package com.huawei.internal.telephony;

import android.telephony.RadioAccessFamily;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class RadioAccessFamilyEx {
    public static final int RAF_1xRTT = 64;
    public static final int RAF_UNKNOWN = 0;
    private RadioAccessFamily mRadioAccessFamily;

    public RadioAccessFamilyEx(int phoneId, int radioAccessFamily) {
        this.mRadioAccessFamily = new RadioAccessFamily(phoneId, radioAccessFamily);
    }

    public RadioAccessFamily getRadioAccessFamily() {
        return this.mRadioAccessFamily;
    }
}
