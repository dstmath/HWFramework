package com.android.server;

import android.content.Context;
import android.os.Handler;
import java.io.PrintWriter;

public class HwCustLocationManagerService {
    public static final String DEL_PKG = "pkg";
    public static final int EVENT_REMOVE_PACKAGE_LOCATION = 3001;

    public HwCustLocationManagerService(Object obj) {
    }

    public void initHwLocationPowerTracker(Context context, Handler handler) {
    }

    public void hwLocationPowerTrackerRecordRequest(String pkgName, int quality, boolean isIntent) {
    }

    public void hwLocationPowerTrackerRemoveRequest(String pkgName) {
    }

    public void hwLocationPowerTrackerDump(PrintWriter pw) {
    }
}
