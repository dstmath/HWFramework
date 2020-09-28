package com.android.internal.telephony.uicc;

import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public final class UsimFileHandler extends IccFileHandler implements IccConstants {
    static final String LOG_TAG = "UsimFH";

    public UsimFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccFileHandler
    public String getEFPath(int efid) {
        if (efid == 28475 || efid == 28476 || efid == 28491 || efid == 28492) {
            return "3F007FFF";
        }
        switch (efid) {
            case IccConstants.EF_PBR:
                return "3F007F105F3A";
            case IccConstants.EF_LI:
            case IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS:
            case IccConstants.EF_HPPLMN:
            case IccConstants.EF_SST:
            case IccConstants.EF_SPN:
            case IccConstants.EF_SDN:
            case IccConstants.EF_EXT5:
            case IccConstants.EF_FPLMN:
            case IccConstants.EF_AD:
            case IccConstants.EF_SPDI:
            case IccConstants.EF_EHPLMN:
            case IccConstants.EF_LRPLMNSI:
                return "3F007FFF";
            default:
                switch (efid) {
                    case IccConstants.EF_CFF_CPHS:
                    case IccConstants.EF_SPN_CPHS:
                    case IccConstants.EF_CSP_CPHS:
                    case IccConstants.EF_INFO_CPHS:
                    case IccConstants.EF_MAILBOX_CPHS:
                    case IccConstants.EF_SPN_SHORT_CPHS:
                        return "3F007FFF";
                    default:
                        switch (efid) {
                            case IccConstants.EF_GID1:
                            case IccConstants.EF_GID2:
                            case IccConstants.EF_MSISDN:
                                return "3F007FFF";
                            default:
                                switch (efid) {
                                    case IccConstants.EF_PLMN_W_ACT:
                                    case IccConstants.EF_OPLMN_W_ACT:
                                    case IccConstants.EF_HPLMN_W_ACT:
                                        return "3F007FFF";
                                    default:
                                        switch (efid) {
                                            case IccConstants.EF_PNN:
                                            case IccConstants.EF_OPL:
                                            case IccConstants.EF_MBDN:
                                            case IccConstants.EF_EXT6:
                                            case IccConstants.EF_MBI:
                                            case IccConstants.EF_MWIS:
                                            case IccConstants.EF_CFIS:
                                                return "3F007FFF";
                                            default:
                                                String path = getCommonIccEFPath(efid);
                                                if (path == null) {
                                                    return "3F007F105F3A";
                                                }
                                                return path;
                                        }
                                }
                        }
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
