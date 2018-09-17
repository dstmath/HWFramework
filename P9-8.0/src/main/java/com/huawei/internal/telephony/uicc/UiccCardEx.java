package com.huawei.internal.telephony.uicc;

import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.uicc.UiccCard;

public class UiccCardEx {
    public static CatService getCatService(UiccCard obj) {
        if (obj != null) {
            return obj.getCatService();
        }
        return null;
    }
}
