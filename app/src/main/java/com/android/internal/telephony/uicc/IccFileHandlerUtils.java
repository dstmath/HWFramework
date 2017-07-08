package com.android.internal.telephony.uicc;

public class IccFileHandlerUtils {
    public static UiccCardApplication getParentApp(IccFileHandler iccFileHandler) {
        if (iccFileHandler != null) {
            return iccFileHandler.mParentApp;
        }
        return null;
    }
}
