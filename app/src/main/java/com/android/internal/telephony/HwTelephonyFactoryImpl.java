package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.HwTelephonyFactory.HwTelephonyFactoryInterface;

public class HwTelephonyFactoryImpl implements HwTelephonyFactoryInterface {
    public HwUiccManager getHwUiccManager() {
        return HwUiccManagerImpl.getDefault();
    }

    public HwNetworkManager getHwNetworkManager() {
        return HwNetworkManagerImpl.getDefault();
    }

    public HwPhoneManager getHwPhoneManager() {
        return HwPhoneManagerImpl.getDefault();
    }

    public HwDataConnectionManager getHwDataConnectionManager() {
        return HwDataConnectionManagerImpl.getDefault();
    }

    public HwInnerSmsManager getHwInnerSmsManager() {
        return HwInnerSmsManagerImpl.getDefault();
    }

    public HwTelephonyBaseManager getHwTelephonyBaseManager() {
        return HwTelephonyBaseManagerImpl.getDefault();
    }

    public HwDataServiceChrManager getHwDataServiceChrManager() {
        return HwDataServiceChrManagerImpl.getDefault();
    }

    public HwInnerVSimManager getHwInnerVSimManager() {
        return HwInnerVSimManagerImpl.getDefault();
    }

    public PhoneSubInfoController getHwSubInfoController(Context cxt, Phone[] phone) {
        return new StubPhoneSubInfo(cxt, phone);
    }

    public HwVolteChrManager getHwVolteChrManager() {
        return HwVolteChrManagerImpl.getDefault();
    }

    public HwChrServiceManager getHwChrServiceManager() {
        return HwChrServiceManagerImpl.getDefault();
    }
}
