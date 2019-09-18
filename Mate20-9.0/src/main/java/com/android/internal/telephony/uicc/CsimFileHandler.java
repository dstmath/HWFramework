package com.android.internal.telephony.uicc;

import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public final class CsimFileHandler extends IccFileHandler implements IccConstants {
    static final String LOG_TAG = "CsimFH";

    public CsimFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
    }

    /* access modifiers changed from: protected */
    public String getEFPath(int efid) {
        if (!(efid == 28450 || efid == 28456 || efid == 28466 || efid == 28484 || efid == 28493 || efid == 28506)) {
            switch (efid) {
                case 28474:
                    return "3F007F10";
                case IccConstants.EF_FDN /*28475*/:
                case IccConstants.EF_SMS /*28476*/:
                    break;
                default:
                    switch (efid) {
                        case IccConstants.EF_MSISDN /*28480*/:
                        case 28481:
                            break;
                        default:
                            String path = getCommonIccEFPath(efid);
                            if (path == null) {
                                return "3F007F105F3A";
                            }
                            return path;
                    }
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
