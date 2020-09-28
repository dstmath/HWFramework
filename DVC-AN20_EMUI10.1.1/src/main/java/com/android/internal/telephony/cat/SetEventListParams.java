package com.android.internal.telephony.cat;

/* access modifiers changed from: package-private */
/* compiled from: CommandParams */
public class SetEventListParams extends CommandParams {
    int[] mEventInfo;

    SetEventListParams(CommandDetails cmdDet, int[] eventInfo) {
        super(cmdDet);
        this.mEventInfo = eventInfo;
    }
}
