package com.android.internal.telephony.cat;

public class HwCommandParams extends CommandParams {
    String language;

    HwCommandParams(CommandDetails cmdDet, String Language) {
        super(cmdDet);
        this.language = Language;
    }
}
