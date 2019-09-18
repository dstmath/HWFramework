package com.android.internal.telephony;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.internal.telephony.IHwUiccSmsManager;
import com.android.internal.telephony.ISms;

public class HwUiccSmsManager {
    private static final String TAG = "HwUiccSmsManager";

    private static ISms getISmsServiceOrThrow() {
        ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService("isms"));
        if (iccISms != null) {
            return iccISms;
        }
        throw new UnsupportedOperationException("Sms is not supported");
    }

    public static IHwUiccSmsManager getService() {
        try {
            return IHwUiccSmsManager.Stub.asInterface(getISmsServiceOrThrow().getHwInnerService());
        } catch (RemoteException e) {
            Rlog.e(TAG, "RemoteException error");
            return null;
        } catch (RuntimeException e2) {
            Rlog.e(TAG, "RuntimeException error");
            return null;
        }
    }

    public static boolean isUimSupportMeid(int subId) {
        IHwUiccSmsManager service = getService();
        if (service == null) {
            return false;
        }
        try {
            return service.isUimSupportMeid(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "isUimSupportMeid failed: catch RemoteException!");
            return false;
        }
    }

    public static String getMeidOrPesn(int subId) {
        IHwUiccSmsManager service = getService();
        if (service == null) {
            return "";
        }
        try {
            return service.getMeidOrPesn(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getMeidOrPesn failed: catch RemoteException!");
            return "";
        }
    }

    public static boolean setMeidOrPesn(int subId, String meid, String pesn) {
        IHwUiccSmsManager service = getService();
        if (service == null) {
            return false;
        }
        try {
            return service.setMeidOrPesn(subId, meid, pesn);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setMeidOrPesn failed: catch RemoteException!");
            return false;
        }
    }

    public static String getSmscAddrForSubscriber(int subId) {
        IHwUiccSmsManager service = getService();
        if (service == null) {
            return null;
        }
        try {
            return service.getSmscAddrForSubscriber(subId);
        } catch (RemoteException e) {
            Rlog.e(TAG, "getSmscAddrForSubscriber failed: catch RemoteException!");
            return null;
        }
    }

    public static boolean setSmscAddrForSubscriber(int subId, String smscAddr) {
        IHwUiccSmsManager service = getService();
        if (service == null) {
            return false;
        }
        try {
            return service.setSmscAddrForSubscriber(subId, smscAddr);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setSmscAddrForSubscriber failed: catch RemoteException!");
            return false;
        }
    }

    public static boolean setCellBroadcastRangeListForSubscriber(int subId, int[] messageIds, int ranType) {
        IHwUiccSmsManager service = getService();
        if (service == null) {
            return false;
        }
        try {
            return service.setCellBroadcastRangeListForSubscriber(subId, messageIds, ranType);
        } catch (RemoteException e) {
            Rlog.e(TAG, "setSmscAddrForSubscriber failed: catch RemoteException!");
            return false;
        }
    }

    public static void setEnabledSingleShiftTables(int[] tables) {
        IHwUiccSmsManager service = getService();
        if (service != null) {
            try {
                service.setEnabledSingleShiftTables(tables);
            } catch (RemoteException e) {
                Rlog.e(TAG, "setEnabledSingleShiftTables failed: catch RemoteException!");
            }
        }
    }

    public static void setSmsCodingNationalCode(String code) {
        IHwUiccSmsManager service = getService();
        if (service != null) {
            try {
                service.setSmsCodingNationalCode(code);
            } catch (RemoteException e) {
                Rlog.e(TAG, "setSmsCodingNationalCode failed: catch RemoteException!");
            }
        }
    }
}
