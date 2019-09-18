package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.imsphone.HwImsPhoneCallTrackerMgr;
import com.android.internal.telephony.imsphone.HwImsPhoneCallTrackerMgrImpl;

public class HwTelephonyFactoryImpl implements HwTelephonyFactory.HwTelephonyFactoryInterface {
    public HwUiccManager getHwUiccManager() {
        return HwUiccManagerImpl.getDefault();
    }

    public HwNetworkManager getHwNetworkManager() {
        return HwNetworkManagerImpl.getDefault();
    }

    public HwReportManager getHwReportManager() {
        return HwReportManagerImpl.getDefault();
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

    public HwTelephonyChrManager getHwTelephonyChrManager() {
        return HwTelephonyChrManagerImpl.getDefault();
    }

    public IHwUiccSmsControllerEx getHwUiccSmsControllerEx(IHwUiccSmsControllerInner uiccSmsController) {
        return new HwUiccSmsControllerEx(uiccSmsController);
    }

    public IHwPhoneSwitcherEx getHwPhoneSwitcherEx(IHwPhoneSwitcherInner phoneSwitcher, int numPhones) {
        return new HwPhoneSwitcherEx(phoneSwitcher, numPhones);
    }

    public HwImsPhoneCallTrackerMgr getHwImsPhoneCallTrackerMgr() {
        return HwImsPhoneCallTrackerMgrImpl.getDefault();
    }
}
