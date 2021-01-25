package com.android.ims;

import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import android.util.Singleton;
import com.android.ims.IHwImsUtManager;
import com.android.ims.internal.IImsUt;

public class HwImsUtManager {
    private static final String IMS_UT_SERVICE_NAME = "ims_ut";
    private static final Singleton<IHwImsUtManager> I_IMS_UT_MANAGER_SINGLETON = new Singleton<IHwImsUtManager>() {
        /* class com.android.ims.HwImsUtManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public IHwImsUtManager create() {
            try {
                IImsUt imsUtService = HwImsUtManager.getImsUt();
                if (imsUtService != null) {
                    return IHwImsUtManager.Stub.asInterface(imsUtService.getHwInnerService());
                }
                HwImsUtManager.loge("getImsUt - can't get ims_ut service");
                return null;
            } catch (RemoteException e) {
                IImsUt unused = HwImsUtManager.iImsUt = null;
                HwImsUtManager.loge("I_IMS_UT_MANAGER_SINGLETON : RemoteException");
                return null;
            }
        }
    };
    private static final String TAG = "HwImsUtManager";
    private static IImsUt iImsUt;

    /* access modifiers changed from: private */
    public static IImsUt getImsUt() {
        IImsUt iImsUt2 = iImsUt;
        if (iImsUt2 != null) {
            return iImsUt2;
        }
        iImsUt = IImsUt.Stub.asInterface(ServiceManager.getService(IMS_UT_SERVICE_NAME));
        return iImsUt;
    }

    private static IHwImsUtManager getService() {
        return (IHwImsUtManager) I_IMS_UT_MANAGER_SINGLETON.get();
    }

    private static IHwImsUtEx getHwImsUtEx(int phoneId) {
        log("getHwImsUtEx: phoneId is" + phoneId);
        if (getService() != null) {
            return HwImsFactory.getHwImsUtEx(getService(), phoneId);
        }
        loge("getHwImsUtEx - can't get IHwImsUtManager service");
        return null;
    }

    public static boolean isSupportCFT(int phoneId) {
        IHwImsUtEx hwImsUtEx = getHwImsUtEx(phoneId);
        if (hwImsUtEx != null) {
            return hwImsUtEx.isSupportCFT();
        }
        loge("isSupportCFT: hwImsUtEx is null");
        return false;
    }

    public static boolean isUtEnable(int phoneId) {
        IHwImsUtEx hwImsUtEx = getHwImsUtEx(phoneId);
        if (hwImsUtEx != null) {
            return hwImsUtEx.isUtEnable();
        }
        loge("isUtEnable: hwImsUtEx is null");
        return false;
    }

    public static void updateCallForwardUncondTimer(int startHour, int startMinute, int endHour, int endMinute, int action, int condition, String number, Message result, int phoneId, ImsUt mImsUt) {
        IHwImsUtEx hwImsUtEx = getHwImsUtEx(phoneId);
        if (hwImsUtEx != null) {
            hwImsUtEx.updateCallForwardUncondTimer(startHour, startMinute, endHour, endMinute, action, condition, number, result, mImsUt);
        } else {
            loge("updateCallForwardUncondTimer: hwImsUtEx is null");
        }
    }

    public static void updateCallBarringOption(String password, int cbType, boolean enable, int serviceClass, Message result, String[] barrList, int phoneId, ImsUt mImsUt) {
        IHwImsUtEx hwImsUtEx = getHwImsUtEx(phoneId);
        if (hwImsUtEx != null) {
            hwImsUtEx.updateCallBarringOption(password, cbType, enable, serviceClass, result, barrList, mImsUt);
        } else {
            loge("updateCallBarringOption: hwImsUtEx is null");
        }
    }

    public static void queryCallForwardForServiceClass(int condition, String number, int serviceClass, Message result, int phoneId, ImsUt mImsUt) {
        IHwImsUtEx hwImsUtEx = getHwImsUtEx(phoneId);
        if (hwImsUtEx != null) {
            hwImsUtEx.queryCallForwardForServiceClass(condition, number, serviceClass, result, mImsUt);
        } else {
            loge("queryCallForwardForServiceClass: hwImsUtEx is null");
        }
    }

    public static String getUtIMPUFromNetwork(int phoneId, ImsUt mImsUt) {
        IHwImsUtEx hwImsUtEx = getHwImsUtEx(phoneId);
        if (hwImsUtEx != null) {
            return hwImsUtEx.getUtIMPUFromNetwork(mImsUt);
        }
        loge("getUtIMPUFromNetwork: hwImsUtEx is null");
        return null;
    }

    public static void processECT(int phoneId, ImsUt mImsUt) {
        IHwImsUtEx hwImsUtEx = getHwImsUtEx(phoneId);
        if (hwImsUtEx != null) {
            hwImsUtEx.processECT(mImsUt);
        } else {
            loge("processECT: hwImsUtEx is null");
        }
    }

    private static void log(String s) {
        Rlog.d(TAG, s);
    }

    /* access modifiers changed from: private */
    public static void loge(String s) {
        Rlog.e(TAG, s);
    }
}
