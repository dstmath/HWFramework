package com.huawei.telephony;

import android.os.Looper;
import android.telephony.PhoneStateListener;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class PhoneStateListenerEx extends PhoneStateListener {
    public PhoneStateListenerEx(Looper looper) {
        super(looper);
    }
}
