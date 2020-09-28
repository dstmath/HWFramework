package com.android.internal.telephony.euicc;

import android.os.Message;
import com.android.internal.telephony.euicc.EuiccConnector;

public interface IHwEuiccConnectorEx {
    void cancelSession();

    boolean handleConnectedStateMessage(Message message);

    void requestDefaultSmdpAddress(String str, EuiccConnector.RequestDefaultSmdpAddressCommandCallback requestDefaultSmdpAddressCommandCallback);

    void resetMemory(String str, int i, EuiccConnector.ResetMemoryCommandCallback resetMemoryCommandCallback);

    void setDefaultSmdpAddress(String str, String str2, EuiccConnector.SetDefaultSmdpAddressCommandCallback setDefaultSmdpAddressCommandCallback);
}
