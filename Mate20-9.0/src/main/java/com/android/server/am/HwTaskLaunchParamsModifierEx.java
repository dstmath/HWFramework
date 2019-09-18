package com.android.server.am;

import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.view.DisplayInfo;
import com.huawei.server.am.IHwTaskLaunchParamsModifierEx;
import java.util.HashMap;

public class HwTaskLaunchParamsModifierEx implements IHwTaskLaunchParamsModifierEx {
    public static final HashMap<String, Integer> DEFAULT_FREEFORM_MAPS = new HashMap<>();
    private static final float DEFAULT_HEIGHT_RATION_MOBILE = 0.85f;
    private static final float DEFAULT_HEIGHT_RATION_TABLET = 0.57f;
    private static final int DEFAULT_MARGIN = 8;
    private static final float DEFAULT_WINDTH_LAND_RATION_MOBILE = 0.8f;
    private static final float DEFAULT_WINDTH_LAND_RATION_TABLET = 0.8f;
    private static final float DEFAULT_WINDTH_RATION_MOBILE = 0.66f;
    private static final float DEFAULT_WINDTH_RATION_TABLET = 0.45f;
    private static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", "").equals(""));
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.charateristics"));
    public static final String TAG = "HwTaskLaunchParamsModifierEx";
    private static int mDefaultFreeformHeight;
    private static int mDefaultFreeformStartX;
    private static int mDefaultFreeformStartY;
    private static int mDefaultFreeformWidth;

    public HashMap<String, Integer> computeDefaultParaForFreeForm(Rect rect, DisplayMetrics mDisplayMetrics, DisplayInfo displayInfo) {
        if (!isOrientationChanged(displayInfo)) {
            return DEFAULT_FREEFORM_MAPS;
        }
        int width = rect.width();
        int height = rect.height();
        if (!(width == displayInfo.appWidth || height == displayInfo.appHeight)) {
            width = displayInfo.appWidth;
            height = displayInfo.appHeight;
        }
        int notchSize = 0;
        if (IS_NOTCH_PROP && displayInfo != null) {
            int i = 0;
            if (displayInfo.rotation == 0 || displayInfo.rotation == 2) {
                if (displayInfo.displayCutout != null) {
                    i = displayInfo.displayCutout.getBounds().getBounds().height();
                }
                notchSize = i;
            } else {
                if (displayInfo.displayCutout != null) {
                    i = displayInfo.displayCutout.getBounds().getBounds().width();
                }
                notchSize = i;
            }
        }
        if (!IS_TABLET) {
            if (width < height) {
                mDefaultFreeformStartX = ((int) (((float) width) * 0.33999997f)) - dip2px(8, mDisplayMetrics);
                mDefaultFreeformStartY = ((height - ((int) (((float) width) * DEFAULT_HEIGHT_RATION_MOBILE))) - dip2px(16, mDisplayMetrics)) + notchSize;
                mDefaultFreeformWidth = (int) (((float) width) * DEFAULT_WINDTH_RATION_MOBILE);
                mDefaultFreeformHeight = (int) (((float) width) * DEFAULT_HEIGHT_RATION_MOBILE);
            } else {
                mDefaultFreeformStartX = ((width - ((int) (((float) height) * DEFAULT_HEIGHT_RATION_MOBILE))) - dip2px(8, mDisplayMetrics)) + notchSize;
                mDefaultFreeformStartY = ((int) (((float) height) * 0.19999999f)) - dip2px(16, mDisplayMetrics);
                mDefaultFreeformWidth = (int) (((float) height) * DEFAULT_HEIGHT_RATION_MOBILE);
                mDefaultFreeformHeight = (int) (((float) height) * 0.8f);
            }
        } else if (width < height) {
            mDefaultFreeformStartX = ((int) (((float) width) * 0.55f)) - dip2px(8, mDisplayMetrics);
            mDefaultFreeformStartY = (height - ((int) (((float) width) * DEFAULT_HEIGHT_RATION_TABLET))) - dip2px(16, mDisplayMetrics);
            mDefaultFreeformWidth = (int) (((float) width) * DEFAULT_WINDTH_RATION_TABLET);
            mDefaultFreeformHeight = (int) (((float) width) * DEFAULT_HEIGHT_RATION_TABLET);
        } else {
            mDefaultFreeformStartX = (width - ((int) (((float) height) * DEFAULT_HEIGHT_RATION_TABLET))) - dip2px(8, mDisplayMetrics);
            mDefaultFreeformStartY = ((int) (((float) height) * 0.19999999f)) - dip2px(16, mDisplayMetrics);
            mDefaultFreeformWidth = (int) (((float) height) * DEFAULT_HEIGHT_RATION_TABLET);
            mDefaultFreeformHeight = (int) (((float) height) * 0.8f);
        }
        HwFreeFormUtils.log(TAG, "X:" + mDefaultFreeformStartX + " Y:" + mDefaultFreeformStartY + " defaultWidth:" + mDefaultFreeformWidth + " defaultHeight:" + mDefaultFreeformHeight + " width:" + width + " height:" + height + " notchSize:" + notchSize + " rotation:" + displayInfo.rotation);
        DEFAULT_FREEFORM_MAPS.put("mDefaultFreeformStartX", Integer.valueOf(mDefaultFreeformStartX));
        DEFAULT_FREEFORM_MAPS.put("mDefaultFreeformStartY", Integer.valueOf(mDefaultFreeformStartY));
        DEFAULT_FREEFORM_MAPS.put("mDefaultFreeformWidth", Integer.valueOf(mDefaultFreeformWidth));
        DEFAULT_FREEFORM_MAPS.put("mDefaultFreeformHeight", Integer.valueOf(mDefaultFreeformHeight));
        DEFAULT_FREEFORM_MAPS.put("rotation", Integer.valueOf(displayInfo.rotation));
        return DEFAULT_FREEFORM_MAPS;
    }

    private static int dip2px(int dpValue, DisplayMetrics mDisplayMetrics) {
        if (mDisplayMetrics == null) {
            return 0;
        }
        return (int) ((((float) dpValue) * mDisplayMetrics.density) + 0.5f);
    }

    private boolean isOrientationChanged(DisplayInfo displayInfo) {
        if (DEFAULT_FREEFORM_MAPS == null || DEFAULT_FREEFORM_MAPS.isEmpty()) {
            return true;
        }
        if (displayInfo == null || DEFAULT_FREEFORM_MAPS.get("rotation").intValue() == displayInfo.rotation) {
            return false;
        }
        return true;
    }
}
