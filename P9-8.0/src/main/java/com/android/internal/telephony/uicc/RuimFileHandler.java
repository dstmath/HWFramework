package com.android.internal.telephony.uicc;

import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public final class RuimFileHandler extends IccFileHandler {
    static final String LOG_TAG = "RuimFH";

    public RuimFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
    }

    public void loadEFImgTransparent(int fileid, int highOffset, int lowOffset, int length, Message onLoaded) {
        int i = fileid;
        this.mCi.iccIOForApp(192, i, getEFPath(IccConstants.EF_IMG), 0, 0, 10, null, null, this.mAid, obtainMessage(10, fileid, 0, onLoaded));
    }

    protected String getEFPath(int efid) {
        if (efid == 28474) {
            return "3F007F10";
        }
        switch (efid) {
            case IccConstants.EF_CSIM_IMSIM /*28450*/:
            case IccConstants.EF_CSIM_CDMAHOME /*28456*/:
            case IccConstants.EF_CST /*28466*/:
            case 28474:
            case IccConstants.EF_SMS /*28476*/:
            case 28481:
            case IccConstants.EF_CSIM_MDN /*28484*/:
            case IccConstants.EF_CSIM_MIPUPP /*28493*/:
            case IccConstants.EF_CSIM_EPRL /*28506*/:
                return "3F007F25";
            default:
                return getCommonIccEFPath(efid);
        }
    }

    protected void logd(String msg) {
        Rlog.d(LOG_TAG, "[RuimFileHandler] " + msg);
    }

    protected void loge(String msg) {
        Rlog.e(LOG_TAG, "[RuimFileHandler] " + msg);
    }
}
