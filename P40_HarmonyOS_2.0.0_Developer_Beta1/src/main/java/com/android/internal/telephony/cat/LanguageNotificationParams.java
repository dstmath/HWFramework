package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class LanguageNotificationParams extends CommandParams {
    String language;

    LanguageNotificationParams(CommandDetails cmdDet, String Language) {
        super(cmdDet);
        this.language = Language;
    }
}
