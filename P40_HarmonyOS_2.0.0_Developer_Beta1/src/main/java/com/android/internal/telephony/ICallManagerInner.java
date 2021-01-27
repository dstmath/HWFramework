package com.android.internal.telephony;

import com.huawei.internal.telephony.PhoneConstantsExt;
import com.huawei.internal.telephony.PhoneExt;

public interface ICallManagerInner {
    IHwCallManagerEx getHwCallManagerEx();

    PhoneExt getPhoneHw(int i);

    PhoneConstantsExt.StateEx getStateEx();

    PhoneConstantsExt.StateEx getStateEx(int i);
}
