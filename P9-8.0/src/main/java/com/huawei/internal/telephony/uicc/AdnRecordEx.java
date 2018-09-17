package com.huawei.internal.telephony.uicc;

import android.os.Parcel;
import com.android.internal.telephony.uicc.AdnRecord;

public class AdnRecordEx {
    public static int describeContents(AdnRecord obj) {
        return 0;
    }

    public static void writeToParcel(AdnRecord obj, Parcel dest, int flags) {
        obj.writeToParcel(dest, flags);
    }
}
