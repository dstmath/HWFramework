package com.android.internal.telephony.uicc;

import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public final class IsimFileHandler extends IccFileHandler implements IccConstants {
    static final String LOG_TAG = "IsimFH";

    public IsimFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
    }

    /* access modifiers changed from: protected */
    public String getEFPath(int efid) {
        if (!(efid == 28423 || efid == 28425)) {
            switch (efid) {
                case IccConstants.EF_IMPI:
                case IccConstants.EF_DOMAIN:
                case IccConstants.EF_IMPU:
                    break;
                default:
                    return getCommonIccEFPath(efid);
            }
        }
        return "3F007FFF";
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
