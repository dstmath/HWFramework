package com.huawei.internal.telephony;

import com.android.internal.telephony.uicc.HwIccUtils;

public class HwIccUtilsEx {
    public static int getAlphaTagEncodingLength(String alphaTag) {
        return HwIccUtils.getAlphaTagEncodingLength(alphaTag);
    }
}
