package com.android.internal.telephony.cat;

/* access modifiers changed from: package-private */
/* compiled from: CommandParams */
public class LanguageParams extends CommandParams {
    String mLanguage;

    LanguageParams(CommandDetails cmdDet, String lang) {
        super(cmdDet);
        this.mLanguage = lang;
    }
}
