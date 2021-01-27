package com.android.server.wifi;

import com.android.internal.annotations.VisibleForTesting;

@VisibleForTesting
public interface BuildProperties {
    boolean isEngBuild();

    boolean isUserBuild();

    boolean isUserdebugBuild();
}
