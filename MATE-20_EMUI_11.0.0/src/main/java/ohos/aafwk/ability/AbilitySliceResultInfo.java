package ohos.aafwk.ability;

import ohos.aafwk.content.Intent;

public final class AbilitySliceResultInfo {
    public static final int REQ_FOR_NORESULT = -1;
    public final String fromName;
    public final int requestCode;
    public final Intent resultIntent;
    public final AbilitySlice to;

    AbilitySliceResultInfo(String str, AbilitySlice abilitySlice, Intent intent, int i) {
        this.fromName = str;
        this.to = abilitySlice;
        this.resultIntent = intent;
        this.requestCode = i;
    }
}
