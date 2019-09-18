package com.android.internal.telephony.uicc;

import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public final class SIMFileHandler extends IccFileHandler implements IccConstants {
    static final String LOG_TAG = "SIMFileHandler";

    public SIMFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
    }

    /* access modifiers changed from: protected */
    public String getEFPath(int efid) {
        if (!(efid == 28433 || efid == 28472)) {
            if (efid == 28476) {
                return "3F007F10";
            }
            if (!(efid == 28486 || efid == 28512 || efid == 28589 || efid == 28621)) {
                switch (efid) {
                    case IccConstants.EF_CFF_CPHS:
                    case IccConstants.EF_SPN_CPHS:
                    case IccConstants.EF_CSP_CPHS:
                    case IccConstants.EF_INFO_CPHS:
                    case IccConstants.EF_MAILBOX_CPHS:
                    case IccConstants.EF_SPN_SHORT_CPHS:
                        break;
                    default:
                        switch (efid) {
                            case IccConstants.EF_GID1:
                            case IccConstants.EF_GID2:
                                break;
                            default:
                                switch (efid) {
                                    case IccConstants.EF_PNN:
                                    case IccConstants.EF_OPL:
                                    case IccConstants.EF_MBDN:
                                    case IccConstants.EF_EXT6:
                                    case IccConstants.EF_MBI:
                                    case IccConstants.EF_MWIS:
                                    case IccConstants.EF_CFIS:
                                        break;
                                    default:
                                        String path = getCommonIccEFPath(efid);
                                        if (path == null) {
                                            Rlog.e(LOG_TAG, "Error: EF Path being returned in null");
                                        }
                                        return path;
                                }
                        }
                }
            }
        }
        return "3F007F20";
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
