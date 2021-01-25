package com.huawei.android.content;

import android.content.IntentFilter;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class IntentFilterEx {
    private IntentFilterEx() {
    }

    public static void setIdentifier(IntentFilter intentFilter, String string) {
        intentFilter.setIdentifier(string);
    }
}
