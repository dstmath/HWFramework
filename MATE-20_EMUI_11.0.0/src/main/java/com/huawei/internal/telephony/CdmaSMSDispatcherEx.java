package com.huawei.internal.telephony;

import com.android.internal.telephony.cdma.CdmaSMSDispatcher;

public class CdmaSMSDispatcherEx extends SMSDispatcherEx {
    private CdmaSMSDispatcher mCdmaSmsDispatcher;

    public void setCdmaSmsDispatcher(CdmaSMSDispatcher cdmaSmsDispatcher) {
        this.mSmsDispatcher = cdmaSmsDispatcher;
    }
}
