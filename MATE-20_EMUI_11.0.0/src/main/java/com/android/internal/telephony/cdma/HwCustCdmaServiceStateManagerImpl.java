package com.android.internal.telephony.cdma;

import android.os.SystemProperties;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.GsmCdmaPhone;
import com.huawei.internal.telephony.PhoneExt;

public class HwCustCdmaServiceStateManagerImpl extends HwCustCdmaServiceStateManager {
    private static final int HOME_ERI_INDEX = 1;
    private static final boolean SET_PLMN_TO_ERITEXT = SystemProperties.getBoolean("ro.config.set_plmn_to_eritext", false);
    private static final String TAG = "HwCustCdmaServiceStateManagerImpl";
    private static final String USE_WHEN_ERI_TEXT_EMPTY = "Roaming Indicator On";

    public String setEriBasedPlmn(PhoneExt phoneExt, String actualPlmnValue) {
        GsmCdmaPhone phone = (GsmCdmaPhone) phoneExt.getPhone();
        if (SET_PLMN_TO_ERITEXT && getCombinedRegState(phone) == 0) {
            int iconIndex = phone.getCdmaEriIconIndex();
            if (iconIndex != 1) {
                actualPlmnValue = phone.getCdmaEriText();
                if (TextUtils.isEmpty(actualPlmnValue)) {
                    actualPlmnValue = USE_WHEN_ERI_TEXT_EMPTY;
                }
            }
            Log.d(TAG, "setEriBasedPlmn -> eriIndex :" + iconIndex + " ,modifiedPlmnValue :" + actualPlmnValue);
        }
        return actualPlmnValue;
    }

    private int getCombinedRegState(GsmCdmaPhone phone) {
        ServiceState sS = phone.getServiceState();
        int regState = sS.getVoiceRegState();
        int dataRegState = sS.getDataRegState();
        return (regState == 1 && dataRegState == 0) ? dataRegState : regState;
    }
}
