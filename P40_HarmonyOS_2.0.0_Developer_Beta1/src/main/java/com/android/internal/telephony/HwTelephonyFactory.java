package com.android.internal.telephony;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import com.android.internal.telephony.emergency.HwEmergencyNumberTrackerMgr;
import com.android.internal.telephony.euicc.IHwEuiccConnectorEx;
import com.android.internal.telephony.euicc.IHwEuiccConnectorInner;
import com.android.internal.telephony.euicc.IHwEuiccControllerEx;
import com.android.internal.telephony.euicc.IHwEuiccControllerInner;
import com.android.internal.telephony.imsphone.HwImsPhoneCallTrackerMgr;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;

public class HwTelephonyFactory {
    private static final String TAG = "HwTelephonyFactory";
    private static final Object mLock = new Object();
    private static volatile HwTelephonyFactoryInterface obj = null;

    public interface HwTelephonyFactoryInterface {
        HwDataServiceChrManager getHwDataServiceChrManager();

        IHwEuiccConnectorEx getHwEuiccConnectorEx(Context context, IHwEuiccConnectorInner iHwEuiccConnectorInner);

        IHwEuiccControllerEx getHwEuiccControllerEx(Context context, IHwEuiccControllerInner iHwEuiccControllerInner);

        HwImsPhoneCallTrackerMgr getHwImsPhoneCallTrackerMgr();

        HwTelephonyBaseManager getHwTelephonyBaseManager();

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
                Log.e(TAG, ": HwTelephonyFactory exception");
            }
        }
        if (obj != null) {
            Log.v(TAG, ": successes to get AllImpl object and return....");
            return obj;
        }
        Log.e(TAG, ": failes to get AllImpl object");
        return null;
    }

    public static HwUiccManager getHwUiccManager() {
        return HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwUiccManager();
    }

    public static HwNetworkManager getHwNetworkManager() {
        return HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).getHwNetworkManager();
    }

    public static HwReportManager getHwReportManager() {
        return HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).getHwReportManager();
    }

    public static HwDataServiceChrManager getHwDataServiceChrManager() {
        return getImplObject().getHwDataServiceChrManager();
    }

    public static HwPhoneManager getHwPhoneManager() {
        return HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwPhoneManagerImpl();
    }

    public static HwDataConnectionManager getHwDataConnectionManager() {
        return HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).getHwDataConnectionManager();
    }

    public static HwInnerSmsManager getHwInnerSmsManager() {
        return HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwInnerSmsManager();
    }

    public static HwTelephonyBaseManager getHwTelephonyBaseManager() {
        return getImplObject().getHwTelephonyBaseManager();
    }

    public static HwVolteChrManager getHwVolteChrManager() {
        return getImplObject().getHwVolteChrManager();
    }

    public static HwInnerVSimManager getHwInnerVSimManager() {
        return HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_VSIM_FACTORY_IMPL_NAME).createHwInnerVSimManager();
    }

    public static HwChrServiceManager getHwChrServiceManager() {
        return HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).getHwChrServiceManager();
    }

    public static HwTelephonyChrManager getHwTelephonyChrManager() {
        return HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).getHwTelephonyChrManager();
    }

    public static IHwPhoneSwitcherEx getHwPhoneSwitcherEx(IHwPhoneSwitcherInner phoneSwitcher, int numPhones) {
        return HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwPhoneSwitcherEx(phoneSwitcher, numPhones);
    }

    public static HwImsPhoneCallTrackerMgr getHwImsPhoneCallTrackerMgr() {
        HwTelephonyFactoryInterface obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwImsPhoneCallTrackerMgr();
        }
        Log.e(TAG, "HwTelephonyFactoryImpl get by reflect is null");
        return null;
    }

    public static HwEmergencyNumberTrackerMgr getHwEmergencyNumberTrackerMgr() {
        return HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).getHwEmergencyNumberTrackerMgr();
    }

    public static IHwEuiccControllerEx getHwEuiccControllerEx(Context context, IHwEuiccControllerInner euiccController) {
        return getImplObject().getHwEuiccControllerEx(context, euiccController);
    }

    public static IHwEuiccConnectorEx getHwEuiccConnectorEx(Context context, IHwEuiccConnectorInner uiccConnector) {
        return getImplObject().getHwEuiccConnectorEx(context, uiccConnector);
    }

    public static void createHwSlicesNetworkFactory(DcTrackerEx dcTracker, Looper looper, Context context, int phoneId) {
        HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwSlicesNetworkFactory(dcTracker, looper, context, phoneId);
    }
}
