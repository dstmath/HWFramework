package com.android.internal.telephony.euicc;

public interface IHwEuiccControllerInner {
    EuiccConnector getEuiccConnector();

    void startOtaUpdatingIfNecessary();

    void startOtaUpdatingIfNecessary(int i);
}
