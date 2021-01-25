package com.android.internal.telephony.vsim;

import android.os.RemoteException;
import com.huawei.hwparttelephony.BuildConfig;
import com.huawei.internal.telephony.vsim.IGetVsimServiceCallback;

public class DefaultHwPhoneServiceVsimEx {
    private static final int INVALID = -1;

    public int getPlatformSupportVsimVer(int what) {
        return -1;
    }

    public String getRegPlmn(int slotId) {
        return BuildConfig.FLAVOR;
    }

    public boolean setVsimUserReservedSubId(int slotId) {
        return false;
    }

    public int getVsimUserReservedSubId() {
        return -1;
    }

    public void blockingGetVsimService(IGetVsimServiceCallback callback) throws RemoteException {
        if (callback != null) {
            callback.onComplete(null);
        }
    }

    public boolean isVsimEnabledByDatabase() {
        return false;
    }
}
