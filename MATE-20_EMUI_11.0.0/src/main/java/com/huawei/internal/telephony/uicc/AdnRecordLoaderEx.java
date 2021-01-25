package com.huawei.internal.telephony.uicc;

import android.os.Message;
import com.android.internal.telephony.uicc.AdnRecordLoader;
import com.android.internal.telephony.uicc.IIccFileHandlerInner;
import com.android.internal.telephony.uicc.IccFileHandler;

public class AdnRecordLoaderEx {
    private AdnRecordLoaderEx() {
    }

    public static void updateEF(IIccFileHandlerInner iIccFileHandlerInner, AdnRecordExt adn, int efid, int extensionEF, int recordNumber, String pin2, Message response) {
        if (iIccFileHandlerInner instanceof IccFileHandler) {
            AdnRecordLoader.updateEFHw((IccFileHandler) iIccFileHandlerInner, adn, efid, extensionEF, recordNumber, pin2, response);
        }
    }
}
