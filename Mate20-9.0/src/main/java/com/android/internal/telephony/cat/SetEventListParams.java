package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class SetEventListParams extends CommandParams {
    int[] mEventInfo;

    SetEventListParams(CommandDetails cmdDet, int[] eventInfo) {
        super(cmdDet);
        this.mEventInfo = eventInfo;
    }
}
