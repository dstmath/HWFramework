package com.android.internal.telephony.euicc;

import android.service.euicc.IEuiccService;
import com.android.internal.telephony.euicc.EuiccConnector;

public interface IHwEuiccConnectorInner {
    IHwEuiccConnectorEx getEuiccConnectorEx();

    IEuiccService getEuiccService();

    void onCommandEndEx(EuiccConnector.BaseEuiccCommandCallback baseEuiccCommandCallback);
}
