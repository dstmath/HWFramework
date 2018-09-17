package com.android.internal.telephony.uicc;

import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public final class IsimFileHandler extends IccFileHandler implements IccConstants {
    static final String LOG_TAG = "IsimFH";

    public IsimFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
    }

    protected String getEFPath(int efid) {
        switch (efid) {
            case IccConstants.EF_IMPI /*28418*/:
            case IccConstants.EF_DOMAIN /*28419*/:
            case IccConstants.EF_IMPU /*28420*/:
            case IccConstants.EF_IST /*28423*/:
            case IccConstants.EF_PCSCF /*28425*/:
                return "3F007FFF";
            default:
                return getCommonIccEFPath(efid);
        }
    }

    protected void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    protected void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
