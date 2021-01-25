package com.android.internal.telephony.uicc;

import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public final class RuimFileHandler extends IccFileHandler {
    static final String LOG_TAG = "RuimFH";

    public RuimFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        super(app, aid, ci);
    }

    @Override // com.android.internal.telephony.uicc.IccFileHandler
    public void loadEFImgTransparent(int fileid, int highOffset, int lowOffset, int length, Message onLoaded) {
        this.mCi.iccIOForApp(192, fileid, getEFPath(IccConstants.EF_IMG), 0, 0, 10, null, null, this.mAid, obtainMessage(10, fileid, 0, onLoaded));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccFileHandler
    public String getEFPath(int efid) {
        if (efid == 28474) {
            return "3F007F10";
        }
        if (efid == 28450 || efid == 28456 || efid == 28466 || efid == 28474 || efid == 28476 || efid == 28481 || efid == 28484 || efid == 28493 || efid == 28506) {
            return "3F007F25";
        }
        return getCommonIccEFPath(efid);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccFileHandler
    public void logd(String msg) {
        Rlog.i(LOG_TAG, "[RuimFileHandler] " + msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccFileHandler
    public void loge(String msg) {
        Rlog.e(LOG_TAG, "[RuimFileHandler] " + msg);
    }
}
