package com.android.internal.telephony.cat;

public interface IHwCatServiceEx {
    default String getLanguageNotificationCode() {
        return null;
    }

    default void setLanguageNotificationCode(String strLanguageNotificationCode) {
    }
}
