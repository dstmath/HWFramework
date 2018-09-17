package com.huawei.forcerotation;

import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;

public interface IForceRotationManager {
    public static final boolean FORCE_ROTATION_DEBUG = false;
    public static final String FORCE_ROTATION_SERVICE_NAME = "forceRotationService";
    public static final int FORCE_ROTATION_SWITCH_STATUS_CLOSED = 0;
    public static final int FORCE_ROTATION_SWITCH_STATUS_OPEN = 1;
    public static final String KEY_OF_FORCE_ROTATION_SWITCH = "force_rotation_mode";

    void applyForceRotationLayout(IBinder iBinder, Rect rect);

    boolean isAppForceLandRotatable(String str, IBinder iBinder);

    boolean isAppInForceRotationWhiteList(String str);

    boolean isForceRotationSupported();

    boolean isForceRotationSwitchOpen();

    boolean isForceRotationSwitchOpen(Context context);

    int recalculateWidthForForceRotation(int i, int i2, int i3);

    boolean saveOrUpdateForceRotationAppInfo(String str, String str2, IBinder iBinder, int i);

    void showToastIfNeeded(String str, int i, String str2, IBinder iBinder);
}
