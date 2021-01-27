package com.huawei.hwaps;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.RenderNode;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public interface IHwApsImpl {
    public static final int APS_DROP_EMPTY_FRAME = 67108864;
    public static final int APS_FEATURE_MASK = 268435455;
    public static final int APS_FPS_ENABLE_BIT = 1;
    public static final int APS_LCD_30HZ = 65536;
    public static final int APS_PARTIAL_UPDATE = 33554432;
    public static final int APS_ROG = 524288;
    public static final int APS_SDR_SCREEN_COMPAT = 32768;
    public static final int LOW_RESOLUTION_COMPOSITION_OFF = 1;
    public static final int LOW_RESOLUTION_COMPOSITION_ON = 2;
    public static final int LOW_RESOLUTION_FEATURE_OFF = 0;
    public static final int ROG_CHANGE_APP_NORMAL = 0;
    public static final int ROG_CHANGE_APP_SHOULD_KILL = 1;
    public static final int ROG_CHANGE_APP_SHOULD_NOT_KILL = 2;

    void adaptPowerSave(Context context, MotionEvent motionEvent);

    void adjustPmDisplayMetricsInRog(DisplayMetrics displayMetrics);

    void applyToConfigurationByResolutionRatio(boolean z, float f, Configuration configuration);

    boolean checkAndApplyToDmByRatio(float f, DisplayMetrics displayMetrics);

    int getAppKillModeWhenRogChange(Context context, String str);

    int getCustScreenDimDurationLocked(int i);

    int getLowResolutionMode(String str, WindowManager.LayoutParams layoutParams, float f);

    float getResolutionRatioByPkgName(String str);

    float getResolutionRatioByPkgName(String str, String str2);

    void initAps(Context context, int i, int i2);

    boolean isDropEmptyFrame(View view);

    boolean isIn1kResolutionof2kScreen();

    boolean isNonEmptyFrameCase(ViewGroup viewGroup, View view);

    boolean isSupportAps();

    boolean isSupportApsPartialUpdate();

    boolean isValidSdrRatio(float f);

    void powerCtroll();

    void processApsPointerEvent(Context context, String str, int i, int i2, MotionEvent motionEvent);

    void savePartialUpdateDirty(Rect rect, int i, int i2, int i3, int i4, Context context, String str);

    Display.Mode scaleDisplayModeInRog(Display.Mode mode);

    Display.Mode[] scaleDisplayModesInRog(Display.Mode[] modeArr);

    void scaleInsetsWhenSdrUpInRog(String str, Rect rect);

    void setApsOnPause();

    void setPartialDirtyToNative(Rect rect, RenderNode renderNode, int i, int i2, Context context, String str);
}
