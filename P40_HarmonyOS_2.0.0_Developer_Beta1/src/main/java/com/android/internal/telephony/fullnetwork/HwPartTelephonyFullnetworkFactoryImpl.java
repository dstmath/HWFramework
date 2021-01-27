package com.android.internal.telephony.fullnetwork;

import com.android.internal.telephony.DefaultHwHotplugController;
import com.android.internal.telephony.DefaultHwSubscriptionManager;
import com.android.internal.telephony.DefaultInCallDataStateManager;
import com.android.internal.telephony.HwHotplugControllerImpl;
import com.android.internal.telephony.HwSubscriptionManagerImpl;
import com.android.internal.telephony.InCallDataStateManagerImpl;

public class HwPartTelephonyFullnetworkFactoryImpl extends DefaultHwPartTelephonyFullnetworkFactory {
    public DefaultHwFullNetworkManager getHwFullnetworkManager() {
        return HwFullNetworkManagerImpl.getInstance();
    }

    public DefaultHwSubscriptionManager getHwSubscriptionManager() {
        return HwSubscriptionManagerImpl.getInstance();
    }

    public DefaultHwHotplugController getHotplugController() {
        return HwHotplugControllerImpl.getInstance();
    }

    public DefaultInCallDataStateManager getInCallDataStateManager() {
        return InCallDataStateManagerImpl.getInstance();
    }
}
