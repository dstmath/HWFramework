package com.android.internal.telephony.uicc;

import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public final class SIMFileHandler extends IccFileHandler implements IccConstants {
    static final String LOG_TAG = "SIMFileHandler";

    public SIMFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccFileHandler
    public String getEFPath(int efid) {
        if (efid == 28433 || efid == 28472) {
            return "3F007F20";
        }
        if (efid == 28476) {
            return "3F007F10";
        }
        if (efid == 28486 || efid == 28512 || efid == 28589 || efid == 28621 || efid == 28478 || efid == 28479) {
            return "3F007F20";
        }
        switch (efid) {
            case IccConstants.EF_CFF_CPHS /* 28435 */:
            case IccConstants.EF_SPN_CPHS /* 28436 */:
            case IccConstants.EF_CSP_CPHS /* 28437 */:
            case IccConstants.EF_INFO_CPHS /* 28438 */:
            case IccConstants.EF_MAILBOX_CPHS /* 28439 */:
            case IccConstants.EF_SPN_SHORT_CPHS /* 28440 */:
                return "3F007F20";
            default:
                switch (efid) {
                    case IccConstants.EF_PNN /* 28613 */:
                    case IccConstants.EF_OPL /* 28614 */:
                    case IccConstants.EF_MBDN /* 28615 */:
                    case IccConstants.EF_EXT6 /* 28616 */:
                    case IccConstants.EF_MBI /* 28617 */:
                    case IccConstants.EF_MWIS /* 28618 */:
                    case IccConstants.EF_CFIS /* 28619 */:
                        return "3F007F20";
                    default:
                        String path = getCommonIccEFPath(efid);
                        if (path == null) {
                            Rlog.e(LOG_TAG, "Error: EF Path being returned in null");
                        }
                        return path;
                }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccFileHandler
    public void logd(String msg) {
        Rlog.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccFileHandler
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
