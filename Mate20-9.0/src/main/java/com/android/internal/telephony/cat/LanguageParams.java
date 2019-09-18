package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class LanguageParams extends CommandParams {
    String mLanguage;

    LanguageParams(CommandDetails cmdDet, String lang) {
        super(cmdDet);
        this.mLanguage = lang;
    }
}
