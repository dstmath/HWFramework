package com.android.server.rms.handler;

import android.content.Context;
import com.android.server.location.HwGnssLogHandlerMsgID;

public class ResourceDispatcher {
    private static final boolean DEBUG = false;
    private static final String TAG = "RMS.ResourceDispatcher";

    public static HwSysResHandler dispath(int resourceType, Context context) {
        switch (resourceType) {
            case HwGnssLogHandlerMsgID.UPDATENTPERRORTIME /*19*/:
                return AppHandler.getInstance(context);
            case HwGnssLogHandlerMsgID.UPDATEBINDERRORTIME /*20*/:
                return MemoryHandler.getInstance(context);
            default:
                return null;
        }
    }
}
