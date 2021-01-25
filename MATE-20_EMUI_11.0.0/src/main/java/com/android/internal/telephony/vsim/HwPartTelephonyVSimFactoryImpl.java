package com.android.internal.telephony.vsim;

import android.content.Context;
import com.huawei.internal.telephony.PhoneExt;

public class HwPartTelephonyVSimFactoryImpl extends DefaultHwPartTelephonyVSimFactory {
    public DefaultHwVSimUtils getHwVSimUtils() {
        return HwVSimUtilsImpl.getInstance();
    }

    public DefaultHwPhoneServiceVsimEx getHwPhoneServiceVsimEx(Context context, PhoneExt[] phones) {
        return new HwPhoneServiceVsimExImpl(context, phones);
    }
}
