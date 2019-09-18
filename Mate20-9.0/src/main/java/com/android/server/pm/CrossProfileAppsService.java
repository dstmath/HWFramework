package com.android.server.pm;

import android.content.Context;
import com.android.server.SystemService;

public class CrossProfileAppsService extends SystemService {
    private CrossProfileAppsServiceImpl mServiceImpl;

    public CrossProfileAppsService(Context context) {
        super(context);
        this.mServiceImpl = new CrossProfileAppsServiceImpl(context);
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.pm.CrossProfileAppsServiceImpl, android.os.IBinder] */
    public void onStart() {
        publishBinderService("crossprofileapps", this.mServiceImpl);
    }
}
