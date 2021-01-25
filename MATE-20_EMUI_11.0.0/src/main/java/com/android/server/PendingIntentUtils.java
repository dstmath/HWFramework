package com.android.server;

import android.app.BroadcastOptions;
import android.os.Bundle;

public class PendingIntentUtils {
    public static Bundle createDontSendToRestrictedAppsBundle(Bundle bundle) {
        BroadcastOptions options = BroadcastOptions.makeBasic();
        options.setDontSendToRestrictedApps(true);
        if (bundle == null) {
            return options.toBundle();
        }
        bundle.putAll(options.toBundle());
        return bundle;
    }

    private PendingIntentUtils() {
    }
}
