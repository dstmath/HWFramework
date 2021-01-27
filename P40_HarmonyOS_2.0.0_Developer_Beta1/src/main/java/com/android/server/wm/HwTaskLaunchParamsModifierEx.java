package com.android.server.wm;

import android.content.Context;
import android.freeform.HwFreeFormUtils;
import android.util.DisplayMetrics;
import com.android.server.gesture.GestureNavConst;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import com.huawei.utils.HwPartResourceUtils;
import java.util.HashMap;

public class HwTaskLaunchParamsModifierEx extends HwTaskLaunchParamsModifierBridgeEx {
    private static final HashMap<String, Integer> DEFAULT_FREEFORM_MAPS = new HashMap<>(5);
    private static final float DEFAULT_HEIGHT_RATION_MOBILE = 0.85f;
    private static final float DEFAULT_HEIGHT_RATION_TABLET = 0.57f;
    private static final int DEFAULT_MARGIN = 8;
    private static final float DEFAULT_WINDTH_LAND_RATION_MOBILE = 0.8f;
    private static final float DEFAULT_WINDTH_LAND_RATION_TABLET = 0.8f;
    private static final float DEFAULT_WINDTH_RATION_MOBILE = 0.66f;
    private static final float DEFAULT_WINDTH_RATION_TABLET = 0.45f;
    private static final boolean IS_NOTCH_PROP = (!SystemPropertiesEx.get("ro.config.hw_notch_size", BuildConfig.FLAVOR).equals(BuildConfig.FLAVOR));
    private static final boolean IS_TABLET = GestureNavConst.DEVICE_TYPE_TABLET.equals(SystemPropertiesEx.get("ro.build.charateristics"));
    private static final String TAG = "HwTaskLaunchParamsModifierEx";
    private static int sDefaultFreeformHeight;
    private static int sDefaultFreeformStartX;
    private static int sDefaultFreeformStartY;
    private static int sDefaultFreeformWidth;
    private static DisplayInfoEx sDisplayInfo = null;
    private static DisplayMetrics sDisplayMetrics = null;
    private static int sNotchSize = 0;

    public HashMap<String, Integer> computeDefaultParaForFreeForm(ActivityDisplayEx activityDisplay, Context context) {
        DisplayInfoEx displayInfoEx;
        if (activityDisplay == null || activityDisplay.isActivityDisplayEmpty() || context == null) {
            HwFreeFormUtils.log(TAG, "computeDefaultParaForFreeForm input null.");
            return DEFAULT_FREEFORM_MAPS;
        }
        sDisplayMetrics = new DisplayMetrics();
        activityDisplay.getDisplay().getRealMetrics(sDisplayMetrics);
        sDisplayInfo = new DisplayInfoEx();
        DisplayInfoEx.getDisplayInfo(activityDisplay.getDisplay(), sDisplayInfo);
        int navbarHeight = context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("navigation_bar_height"));
        int navbarWidth = context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("navigation_bar_width"));
        int width = sDisplayMetrics.widthPixels;
        int height = sDisplayMetrics.heightPixels;
        if (IS_NOTCH_PROP && (displayInfoEx = sDisplayInfo) != null && !displayInfoEx.isEmpty()) {
            int i = 0;
            if (sDisplayInfo.getRotation() == 0) {
                sNotchSize = 0;
            } else if (sDisplayInfo.getRotation() == 2) {
                if (sDisplayInfo.getDisplayCutout() != null) {
                    i = sDisplayInfo.getDisplayCutout().getBoundingRects().get(0).height();
                }
                sNotchSize = i;
            } else if (sDisplayInfo.getRotation() == 1) {
                sNotchSize = 0;
            } else {
                if (sDisplayInfo.getDisplayCutout() != null) {
                    i = sDisplayInfo.getDisplayCutout().getBoundingRects().get(0).width();
                }
                sNotchSize = i;
            }
        }
        getDefaultBounds(width, height, navbarHeight, navbarWidth);
        DisplayInfoEx displayInfoEx2 = sDisplayInfo;
        if (displayInfoEx2 != null) {
            DEFAULT_FREEFORM_MAPS.put("rotation", Integer.valueOf(displayInfoEx2.getRotation()));
            HwFreeFormUtils.log(TAG, "X:" + sDefaultFreeformStartX + " Y:" + sDefaultFreeformStartY + " defaultWidth:" + sDefaultFreeformWidth + " defaultHeight:" + sDefaultFreeformHeight + " width:" + width + " height:" + height + " notchSize:" + sNotchSize + " rotation:" + sDisplayInfo.getRotation() + ",navbarHeight = " + navbarHeight + ",navbarWidth = " + navbarWidth);
        }
        DEFAULT_FREEFORM_MAPS.put("sDefaultFreeformStartX", Integer.valueOf(sDefaultFreeformStartX));
        DEFAULT_FREEFORM_MAPS.put("sDefaultFreeformStartY", Integer.valueOf(sDefaultFreeformStartY));
        DEFAULT_FREEFORM_MAPS.put("sDefaultFreeformWidth", Integer.valueOf(sDefaultFreeformWidth));
        DEFAULT_FREEFORM_MAPS.put("sDefaultFreeformHeight", Integer.valueOf(sDefaultFreeformHeight));
        return DEFAULT_FREEFORM_MAPS;
    }

    private static void getDefaultBounds(int width, int height, int navbarHeight, int navbarWidth) {
        if (!IS_TABLET) {
            if (width < height) {
                sDefaultFreeformStartX = ((int) (((float) width) * 0.33999997f)) - dip2px(8);
                sDefaultFreeformStartY = (((height - ((int) (((float) width) * DEFAULT_HEIGHT_RATION_MOBILE))) - dip2px(16)) - navbarHeight) - sNotchSize;
                sDefaultFreeformWidth = (int) (((float) width) * DEFAULT_WINDTH_RATION_MOBILE);
                sDefaultFreeformHeight = (int) (((float) width) * DEFAULT_HEIGHT_RATION_MOBILE);
                return;
            }
            sDefaultFreeformStartX = (((width - ((int) (((float) height) * DEFAULT_HEIGHT_RATION_MOBILE))) - dip2px(8)) - navbarWidth) - sNotchSize;
            sDefaultFreeformStartY = ((int) (((float) height) * 0.19999999f)) - dip2px(16);
            sDefaultFreeformWidth = (int) (((float) height) * DEFAULT_HEIGHT_RATION_MOBILE);
            sDefaultFreeformHeight = (int) (((float) height) * 0.8f);
        } else if (width < height) {
            sDefaultFreeformStartX = ((int) (((float) width) * 0.55f)) - dip2px(8);
            sDefaultFreeformStartY = ((height - ((int) (((float) width) * DEFAULT_HEIGHT_RATION_TABLET))) - dip2px(16)) - navbarHeight;
            sDefaultFreeformWidth = (int) (((float) width) * DEFAULT_WINDTH_RATION_TABLET);
            sDefaultFreeformHeight = (int) (((float) width) * DEFAULT_HEIGHT_RATION_TABLET);
        } else {
            sDefaultFreeformStartX = ((width - ((int) (((float) height) * DEFAULT_HEIGHT_RATION_TABLET))) - dip2px(8)) - navbarWidth;
            sDefaultFreeformStartY = ((int) (((float) height) * 0.19999999f)) - dip2px(16);
            sDefaultFreeformWidth = (int) (((float) height) * DEFAULT_HEIGHT_RATION_TABLET);
            sDefaultFreeformHeight = (int) (((float) height) * 0.8f);
        }
    }

    private static int dip2px(int dpValue) {
        DisplayMetrics displayMetrics = sDisplayMetrics;
        if (displayMetrics == null) {
            return 0;
        }
        return (int) ((((float) dpValue) * displayMetrics.density) + 0.5f);
    }
}
