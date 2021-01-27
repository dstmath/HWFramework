package com.huawei.internal.telephony;

import android.telephony.NrCellSsbId;
import java.util.ArrayList;

public class NrCellSsbIdExt {
    public static NrCellSsbId getDefaultObject() {
        return new NrCellSsbId(-1, -1, -1, -1, -1, -1, new ArrayList(), -1, new ArrayList());
    }
}
