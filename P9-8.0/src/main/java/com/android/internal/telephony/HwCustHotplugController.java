package com.android.internal.telephony;

import android.content.Context;

public class HwCustHotplugController {
    protected Context mContext;

    public HwCustHotplugController(Context context) {
        this.mContext = context;
    }

    public String change4GString(String str) {
        return str;
    }
}
