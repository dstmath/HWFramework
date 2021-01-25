package com.android.internal.telephony.cat;

import android.util.Log;

public class HwCatServiceReference extends DefaultHwCatServiceEx {
    private String strLanguageNotificationCode = null;

    public HwCatServiceReference(ICatServiceInner catServiceInner) {
    }

    public String getLanguageNotificationCode() {
        Log.d("HwCatService", "Enter getLanguageNotificationCode in HwCatService");
        return this.strLanguageNotificationCode;
    }

    public void setLanguageNotificationCode(String languageNotificationCode) {
        Log.d("HwCatService", "Enter setLanguageNotificationCode in HwCatService");
        this.strLanguageNotificationCode = languageNotificationCode;
    }
}
