package com.android.server.am;

import android.os.SystemProperties;

public class HwCustAppErrorsImpl extends HwCustAppErrors {
    public boolean isCustom() {
        return SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    }
}
