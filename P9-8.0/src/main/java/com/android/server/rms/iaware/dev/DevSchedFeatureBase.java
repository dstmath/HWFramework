package com.android.server.rms.iaware.dev;

import android.content.Context;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;

public abstract class DevSchedFeatureBase {
    private static final String TAG = "DevSchedFeatureBase";

    public abstract boolean handlerNaviStatus(boolean z);

    public DevSchedFeatureBase(Context context) {
    }

    public boolean handleResAppData(long timestamp, int event, AttrSegments attrSegments) {
        return false;
    }
}
