package com.huawei.server.magicwin;

import android.util.DisplayMetrics;

public interface DeviceAttribute {
    DisplayMetrics getDisplayMetrics();

    boolean isFoldableDevice();

    boolean isInFoldedStatus();

    boolean isLocalContainer();

    boolean isPadDevice();

    boolean isVirtualContainer();
}
