package com.android.internal.telephony;

import android.os.Message;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.PhoneExt;

public class HwNetworkManagerImpl extends DefaultHwNetworkManager {
    private static final boolean DBG = true;
    private static final String TAG = "HwNetworkManagerImpl";
    private static HwNetworkManagerImpl sInstance = new HwNetworkManagerImpl();

    public static HwNetworkManagerImpl getDefault() {
        return sInstance;
    }

    public boolean isNetworkModeAsynchronized(PhoneExt phone) {
        HwDataConnectionManager sHwDataConnectionManager = HwDataConnectionManagerImpl.getDefault();
        if (sHwDataConnectionManager == null || !sHwDataConnectionManager.getNamSwitcherForSoftbank() || !sHwDataConnectionManager.isSoftBankCard(phone) || sHwDataConnectionManager.isValidMsisdn(phone)) {
            return false;
        }
        RlogEx.i(TAG, "no msisdn softbank card");
        return true;
    }

    public void setPreferredNetworkTypeForNoMdn(PhoneExt phone, int settingMode) {
        if (HwNetworkTypeUtils.isLteServiceOn(settingMode)) {
            phone.setPreferredNetworkType(3, (Message) null);
        } else {
            phone.setPreferredNetworkType(settingMode, (Message) null);
        }
    }

    public void factoryResetNetworkTypeForNoMdn(PhoneExt phone) {
        setPreferredNetworkTypeForNoMdn(phone, HwNetworkTypeUtils.getNetworkModeFromDB(phone.getContext(), phone.getPhoneId()));
        HwNetworkTypeUtils.saveNetworkModeToDB(phone.getContext(), phone.getPhoneId(), 9);
    }

    public void handle4GSwitcherForNoMdn(PhoneExt phone, int nwMode) {
        HwNetworkTypeUtils.saveNetworkModeToDB(phone.getContext(), phone.getPhoneId(), nwMode);
        phone.setPreferredNetworkType(3, (Message) null);
    }
}
