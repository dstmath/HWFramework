package com.huawei.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.TelephonyComponentFactory;

public class GsmCdmaPhoneEx extends PhoneExt {
    /* access modifiers changed from: protected */
    public void initGsmCdmaPhone(Context context, CommandsInterfaceEx ci, PhoneNotifierEx notifier, int phoneId, int precisePhoneType) {
        setPhone(new GsmCdmaPhone(context, ci.getCommandsInterface(), notifier.getPhoneNotifier(), phoneId, precisePhoneType, TelephonyComponentFactory.getInstance()));
        this.mPhoneId = phoneId;
    }
}
