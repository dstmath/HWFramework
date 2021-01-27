package com.android.internal.telephony.vsim;

import android.content.Context;
import android.os.RemoteException;
import com.android.internal.telephony.HwPartOptTelephonyFactory;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.vsim.IGetVsimServiceCallback;

public class HwPhoneServiceVsimEx {
    private DefaultHwPhoneServiceVsimEx mHwPhoneServiceVsimEx;

    public HwPhoneServiceVsimEx(Context context, PhoneExt[] phones) {
        this.mHwPhoneServiceVsimEx = HwPartOptTelephonyFactory.getTelephonyFactory().getVSimFactory().getHwPhoneServiceVsimEx(context, phones);
    }

    public int getPlatformSupportVsimVer(int what) {
        return this.mHwPhoneServiceVsimEx.getPlatformSupportVsimVer(what);
    }

    public String getRegPlmn(int slotId) {
        return this.mHwPhoneServiceVsimEx.getRegPlmn(slotId);
    }

    public boolean setVsimUserReservedSubId(int slotId) {
        return this.mHwPhoneServiceVsimEx.setVsimUserReservedSubId(slotId);
    }

    public int getVsimUserReservedSubId() {
        return this.mHwPhoneServiceVsimEx.getVsimUserReservedSubId();
    }

    public void blockingGetVsimService(IGetVsimServiceCallback callback) throws RemoteException {
        this.mHwPhoneServiceVsimEx.blockingGetVsimService(callback);
    }

    public boolean isVsimEnabledByDatabase() {
        return this.mHwPhoneServiceVsimEx.isVsimEnabledByDatabase();
    }
}
