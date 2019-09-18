package com.android.internal.telephony;

import android.content.Context;
import android.util.Log;
import com.android.internal.telephony.imsphone.HwImsPhoneCallTrackerMgr;

public class HwTelephonyFactory {
    private static final String TAG = "HwTelephonyFactory";
    private static final Object mLock = new Object();
    private static volatile HwTelephonyFactoryInterface obj = null;

    public interface HwTelephonyFactoryInterface {
        HwChrServiceManager getHwChrServiceManager();

        HwDataConnectionManager getHwDataConnectionManager();

        HwDataServiceChrManager getHwDataServiceChrManager();

        HwImsPhoneCallTrackerMgr getHwImsPhoneCallTrackerMgr();

        HwInnerSmsManager getHwInnerSmsManager();

        HwInnerVSimManager getHwInnerVSimManager();

        HwNetworkManager getHwNetworkManager();

        HwPhoneManager getHwPhoneManager();

        IHwPhoneSwitcherEx getHwPhoneSwitcherEx(IHwPhoneSwitcherInner iHwPhoneSwitcherInner, int i);

        HwReportManager getHwReportManager();

        PhoneSubInfoController getHwSubInfoController(Context context, Phone[] phoneArr);

        HwTelephonyBaseManager getHwTelephonyBaseManager();

        HwTelephonyChrManager getHwTelephonyChrManager();

        HwUiccManager getHwUiccManager();

        IHwUiccSmsControllerEx getHwUiccSmsControllerEx(IHwUiccSmsControllerInner iHwUiccSmsControllerInner);

        HwVolteChrManager getHwVolteChrManager();
    }

    private static HwTelephonyFactoryInterface getImplObject() {
        if (obj != null) {
            return obj;
        }
        synchronized (mLock) {
            try {
                obj = (HwTelephonyFactoryInterface) Class.forName("com.android.internal.telephony.HwTelephonyFactoryImpl").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception is " + e);
            }
        }
        if (obj != null) {
            Log.v(TAG, ": successes to get AllImpl object and return....");
            return obj;
        }
        Log.e(TAG, ": failes to get AllImpl object");
        return null;
    }

    public static PhoneSubInfoController getHwSubInfoController(Context cxt, Phone[] phone) {
        HwTelephonyFactoryInterface obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwSubInfoController(cxt, phone);
        }
        return new PhoneSubInfoController(cxt, phone);
    }

    public static HwUiccManager getHwUiccManager() {
        return getImplObject().getHwUiccManager();
    }

    public static HwNetworkManager getHwNetworkManager() {
        return getImplObject().getHwNetworkManager();
    }

    public static HwReportManager getHwReportManager() {
        return getImplObject().getHwReportManager();
    }

    public static HwDataServiceChrManager getHwDataServiceChrManager() {
        return getImplObject().getHwDataServiceChrManager();
    }

    public static HwPhoneManager getHwPhoneManager() {
        return getImplObject().getHwPhoneManager();
    }

    public static HwDataConnectionManager getHwDataConnectionManager() {
        return getImplObject().getHwDataConnectionManager();
    }

    public static HwInnerSmsManager getHwInnerSmsManager() {
        return getImplObject().getHwInnerSmsManager();
    }

    public static HwTelephonyBaseManager getHwTelephonyBaseManager() {
        return getImplObject().getHwTelephonyBaseManager();
    }

    public static HwVolteChrManager getHwVolteChrManager() {
        return getImplObject().getHwVolteChrManager();
    }

    public static HwInnerVSimManager getHwInnerVSimManager() {
        return getImplObject().getHwInnerVSimManager();
    }

    public static HwChrServiceManager getHwChrServiceManager() {
        return getImplObject().getHwChrServiceManager();
    }

    public static HwTelephonyChrManager getHwTelephonyChrManager() {
        return getImplObject().getHwTelephonyChrManager();
    }

    public static IHwUiccSmsControllerEx getHwUiccSmsControllerEx(IHwUiccSmsControllerInner uiccSmsController) {
        return getImplObject().getHwUiccSmsControllerEx(uiccSmsController);
    }

    public static IHwPhoneSwitcherEx getHwPhoneSwitcherEx(IHwPhoneSwitcherInner phoneSwitcher, int numPhones) {
        return getImplObject().getHwPhoneSwitcherEx(phoneSwitcher, numPhones);
    }

    public static HwImsPhoneCallTrackerMgr getHwImsPhoneCallTrackerMgr() {
        HwTelephonyFactoryInterface obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwImsPhoneCallTrackerMgr();
        }
        Log.e(TAG, "HwTelephonyFactoryImpl get by reflect is null");
        return null;
    }
}
