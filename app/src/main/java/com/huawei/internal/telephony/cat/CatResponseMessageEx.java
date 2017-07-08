package com.huawei.internal.telephony.cat;

import com.android.internal.telephony.cat.CatResponseMessage;

public class CatResponseMessageEx {
    public static void setEventDownload(CatResponseMessage obj, int event, byte[] addedInfo) {
        obj.setEventDownload(event, addedInfo);
    }

    public static void setAdditionalInfo(CatResponseMessage obj, boolean includeAdditionalInfo, int additionalInfo) {
        obj.setAdditionalInfo(includeAdditionalInfo, additionalInfo);
    }
}
