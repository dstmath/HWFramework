package com.android.server.wm;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;

public class HwWallpaperAnimation {
    public static final String ACTION_WALLPAPER_ANIMATE = "com.huawei.wallpaper.animate";
    private static final String ANIMATION_TYPE = "animation_type";
    private static final String ANIMATION_TYPE_SCALE = "scale";
    private static final String DURATION = "duration";
    private static final String INTERPOLATOR_TYPE = "interpolator_type";
    private static final String PATH_CONTROL_X1 = "path_controlX1";
    private static final String PATH_CONTROL_X2 = "path_controlX2";
    private static final String PATH_CONTROL_Y1 = "path_controlY1";
    private static final String PATH_CONTROL_Y2 = "path_controlY2";
    private static final String PATH_INTERPOLATOR = "path_interpolator";
    private static final String PIVOT_X_TYPE = "pivotXType";
    private static final String PIVOT_X_VALUE = "pivotXValue";
    private static final String PIVOT_Y_TYPE = "pivotYType";
    private static final String PIVOT_Y_VALUE = "pivotYValue";
    private static final String SCALE_FROM_X = "scale_from_x";
    private static final String SCALE_FROM_Y = "scale_from_y";
    private static final String SCALE_TO_X = "scale_to_x";
    private static final String SCALE_TO_Y = "scale_to_y";

    public static Animation createAnimation(Bundle bundle) {
        AnimationSet set = new AnimationSet(false);
        set.setFillAfter(true);
        if (bundle == null) {
            return set;
        }
        String animationType = bundle.getString(ANIMATION_TYPE);
        if (!TextUtils.isEmpty(animationType) && ANIMATION_TYPE_SCALE.equalsIgnoreCase(animationType)) {
            set.addAnimation(createScaleAnimation(bundle));
        }
        return set;
    }

    private static Animation createScaleAnimation(Bundle bundle) {
        int duration = bundle.getInt(DURATION, 0);
        float fromX = bundle.getFloat(SCALE_FROM_X, 0.0f);
        float fromY = bundle.getFloat(SCALE_FROM_Y, 0.0f);
        Animation animation = new ScaleAnimation(fromX, bundle.getFloat(SCALE_TO_X, 0.0f), fromY, bundle.getFloat(SCALE_TO_Y, 0.0f), bundle.getInt(PIVOT_X_TYPE, 0), bundle.getFloat(PIVOT_X_VALUE, 0.0f), bundle.getInt(PIVOT_Y_TYPE, 0), bundle.getFloat(PIVOT_Y_VALUE, 0.0f));
        animation.setInterpolator(createInterpolator(bundle));
        animation.setDuration((long) duration);
        return animation;
    }

    private static Interpolator createInterpolator(Bundle bundle) {
        String interpolatorType = bundle.getString(INTERPOLATOR_TYPE);
        if (TextUtils.isEmpty(interpolatorType)) {
            return new LinearInterpolator();
        }
        if (PATH_INTERPOLATOR.equalsIgnoreCase(interpolatorType)) {
            return createPathInterpolator(bundle);
        }
        return new LinearInterpolator();
    }

    private static Interpolator createPathInterpolator(Bundle bundle) {
        return new PathInterpolator(bundle.getFloat(PATH_CONTROL_X1), bundle.getFloat(PATH_CONTROL_Y1), bundle.getFloat(PATH_CONTROL_X2), bundle.getFloat(PATH_CONTROL_Y2));
    }
}
