package com.android.internal.telephony.dataconnection;

import android.net.LinkProperties;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.uicc.IccRecords;

public class HwCustDataConnectionImpl extends HwCustDataConnection {
    private static final boolean HWDBG = true;
    private static final String TAG = "HwCustDataConnectionImpl";

    private void log(String message) {
        Rlog.d(TAG, message);
    }

    public boolean setMtuIfNeeded(LinkProperties lp, Phone phone) {
        if (phone == null || phone.mIccRecords == null || phone.mIccRecords.get() == null) {
            return false;
        }
        String mccmnc = ((IccRecords) phone.mIccRecords.get()).getOperatorNumeric();
        String plmnsConfig = System.getString(phone.getContext().getContentResolver(), "hw_set_mtu_by_mccmnc");
        log("mccmnc = " + mccmnc + " plmnsConfig = " + plmnsConfig);
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccmnc)) {
            return false;
        }
        String[] plmns = plmnsConfig.split(";");
        int length = plmns.length;
        int i = 0;
        while (i < length) {
            String[] mcc = plmns[i].split(",");
            if (mcc.length != 2) {
                return false;
            }
            int mtuValue = Integer.parseInt(mcc[1]);
            if ((!"all".equals(mcc[0]) || mtuValue == 0) && (!mccmnc.equals(mcc[0]) || mtuValue == 0)) {
                i++;
            } else {
                lp.setMtu(mtuValue);
                log("set MTU by cust to " + mtuValue);
                return HWDBG;
            }
        }
        return false;
    }

    public boolean whetherSetApnByCust(Phone phone) {
        if (phone == null || phone.mIccRecords == null || phone.mIccRecords.get() == null) {
            return false;
        }
        String mccmnc = ((IccRecords) phone.mIccRecords.get()).getOperatorNumeric();
        String plmnsConfig = System.getString(phone.getContext().getContentResolver(), "hw_set_apn_by_mccmnc");
        int dataRadioTech = phone.getServiceState().getRilDataRadioTechnology();
        log("mccmnc = " + mccmnc + " plmnsConfig = " + plmnsConfig + " dataRadioTech = " + dataRadioTech);
        if (TextUtils.isEmpty(plmnsConfig) || TextUtils.isEmpty(mccmnc) || dataRadioTech != 14) {
            return false;
        }
        for (String plmn : plmnsConfig.split(",")) {
            if (plmn.equals(mccmnc)) {
                return HWDBG;
            }
        }
        return false;
    }
}
