package android.telephony;

import android.content.Context;

public class HwCustTelephonyManager {
    Context mContext;

    public HwCustTelephonyManager() {
    }

    public HwCustTelephonyManager(Context context) {
        this.mContext = context;
    }

    public boolean isVZW() {
        return false;
    }

    public String getVZWLine1Number(int subId, String number, String mccmnc) {
        return null;
    }

    public void setDataEnabledVZW(Context context, int subId, boolean enable) {
    }
}
