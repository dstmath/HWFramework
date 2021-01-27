package com.android.internal.telephony.separated;

import com.android.internal.telephony.separated.metrics.DefaultTelephonyMetrics;
import com.android.internal.telephony.separated.metrics.TelephonyMetrics;
import com.android.internal.telephony.separated.rcs.DefaultRcsMessageManager;
import com.android.internal.telephony.separated.rcs.RcsMessageManagerImpl;

public class TelephonySeparatedFactoryImpl extends DefaultTelephonySeparatedFactory {
    public DefaultRcsMessageManager getRcsMessageManager() {
        return RcsMessageManagerImpl.getInstance();
    }

    public DefaultTelephonyMetrics getTelephonyMetrics() {
        return TelephonyMetrics.getInstance();
    }
}
