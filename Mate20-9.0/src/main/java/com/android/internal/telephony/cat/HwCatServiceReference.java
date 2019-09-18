package com.android.internal.telephony.cat;

import android.util.Log;
import com.android.internal.telephony.cat.AbstractCatService;

public class HwCatServiceReference implements AbstractCatService.CatServiceReference {
    public String strLanguageNotificationCode = null;

    public String getLanguageNotificationCode() {
        Log.d("HwCatService", "Enter getLanguageNotificationCode in HwCatService");
        return this.strLanguageNotificationCode;
    }

    public void setLanguageNotificationCode(String languageNotificationCode) {
        Log.d("HwCatService", "Enter setLanguageNotificationCode in HwCatService");
        this.strLanguageNotificationCode = languageNotificationCode;
    }
}
