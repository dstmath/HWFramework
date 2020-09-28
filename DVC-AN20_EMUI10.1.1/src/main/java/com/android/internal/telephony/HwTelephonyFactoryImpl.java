package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.euicc.HwEuiccConnectorEx;
import com.android.internal.telephony.euicc.HwEuiccControllerEx;
import com.android.internal.telephony.euicc.IHwEuiccConnectorEx;
import com.android.internal.telephony.euicc.IHwEuiccConnectorInner;
import com.android.internal.telephony.euicc.IHwEuiccControllerEx;
import com.android.internal.telephony.euicc.IHwEuiccControllerInner;
import com.android.internal.telephony.imsphone.HwImsPhoneCallTrackerMgr;
import com.android.internal.telephony.imsphone.HwImsPhoneCallTrackerMgrImpl;

public class HwTelephonyFactoryImpl implements HwTelephonyFactory.HwTelephonyFactoryInterface {
    public HwPhoneManager getHwPhoneManager() {
        return HwPhoneManagerImpl.getDefault();
    }

    public HwTelephonyBaseManager getHwTelephonyBaseManager() {
        return HwTelephonyBaseManagerImpl.getDefault();
    }

    public HwDataServiceChrManager getHwDataServiceChrManager() {
        return HwDataServiceChrManagerImpl.getDefault();
    }

    public HwVolteChrManager getHwVolteChrManager() {
        return HwVolteChrManagerImpl.getDefault();
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

    public IHwEuiccControllerEx getHwEuiccControllerEx(Context context, IHwEuiccControllerInner euiccController) {
        return new HwEuiccControllerEx(context, euiccController);
    }

    public IHwEuiccConnectorEx getHwEuiccConnectorEx(Context context, IHwEuiccConnectorInner uiccConnector) {
        return new HwEuiccConnectorEx(context, uiccConnector);
    }
}
