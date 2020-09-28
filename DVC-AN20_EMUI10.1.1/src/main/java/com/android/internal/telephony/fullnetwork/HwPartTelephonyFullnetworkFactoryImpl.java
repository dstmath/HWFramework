package com.android.internal.telephony.fullnetwork;

import com.android.internal.telephony.DefaultHwHotplugController;
import com.android.internal.telephony.HwHotplugControllerImpl;

public class HwPartTelephonyFullnetworkFactoryImpl extends DefaultHwPartTelephonyFullnetworkFactory {
    public DefaultHwFullNetworkManager getHwFullnetworkManager() {
        return HwFullNetworkManagerImpl.getInstance();
    }

    public DefaultHwHotplugController getHotplugController() {
        return HwHotplugControllerImpl.getInstance();
    }
}
