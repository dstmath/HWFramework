package com.huawei.hwanimation;

import android.app.Activity;
import android.app.ActivityTaskManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.animation.Animation;
import com.android.internal.R;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AnimUtil {
    private static final String CUBIC_BEZIER_INTERPOLATOR = "cubicBezierInterpolator";
    private static final String CUBIC_BEZIER_REVERSE_INTERPOLATOR = "cubicBezierReverseInterpolator";
    private static final boolean DEBUG = false;
    private static final String HW_ANIMATION = "HwAnimation";
    private static final String HW_ANIMATION_LITE = "HwAnimation_lite";
    private static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static final boolean IS_FORCE_FULL_ANIM_ENABLE = SystemProperties.getBoolean("hw_sc.force_full_anim_enable", false);
    private static final boolean IS_NOVA_PERF = SystemProperties.getBoolean("ro.config.hw_nova_performance", false);
    private static final String STYLE = "style";
    private static final String TAG = "AnimUtil";
    public static final int TRANSIT_ACTIVITY_CLOSE = 2;
    public static final int TRANSIT_ACTIVITY_OPEN = 1;
    public static final int TRANSIT_TASK_CLOSE = 4;
    public static final int TRANSIT_TASK_OPEN = 3;
    private static String sHwAnimResPackageName = "androidhwext";
    private Context mClientContext = null;
    private IBinder mClientToken = null;

    public AnimUtil() {
    }

    public AnimUtil(Context context) {
        this.mClientContext = context;
        initToken();
    }

    public CubicBezierInterpolator getCubicBezierInterpolator(String name, Context context, AttributeSet attrs) {
        if (CUBIC_BEZIER_INTERPOLATOR.equals(name)) {
            return new CubicBezierInterpolator(context, attrs);
        }
        if (CUBIC_BEZIER_REVERSE_INTERPOLATOR.equals(name)) {
            return new CubicBezierReverseInterpolator(context, attrs);
        }
        return null;
    }

    public CubicBezierInterpolator getCubicBezierInterpolator(String name, Resources res, Resources.Theme theme, AttributeSet attrs) {
        if (CUBIC_BEZIER_INTERPOLATOR.equals(name)) {
            return new CubicBezierInterpolator(res, theme, attrs);
        }
        if (CUBIC_BEZIER_REVERSE_INTERPOLATOR.equals(name)) {
            return new CubicBezierReverseInterpolator(res, theme, attrs);
        }
        return null;
    }

    public Animation getHwClipRectAnimation(Context context, AttributeSet attrs) {
        return new HwClipRectAnimation(context, attrs);
    }

    public void overrideTransition(int transit) {
        if (this.mClientToken != null) {
            try {
                Context hwAnimationContext = this.mClientContext.createPackageContext(sHwAnimResPackageName, 0);
                if (hwAnimationContext != null) {
                    overrideTransitionInternal(transit, hwAnimationContext);
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
    }

    private void overrideTransitionInternal(int transit, Context context) {
        int enterAnim = 0;
        int exitAnim = 0;
        int resId = 0;
        if ((IS_EMUI_LITE || IS_NOVA_PERF) && !IS_FORCE_FULL_ANIM_ENABLE) {
            resId = context.getResources().getIdentifier(HW_ANIMATION_LITE, STYLE, sHwAnimResPackageName);
        }
        if (resId == 0) {
            resId = context.getResources().getIdentifier(HW_ANIMATION, STYLE, sHwAnimResPackageName);
        }
        if (resId != 0) {
            TypedArray windowAnimationArray = context.obtainStyledAttributes(resId, R.styleable.WindowAnimation);
            if (transit == 1) {
                enterAnim = windowAnimationArray.getResourceId(4, 0);
                exitAnim = windowAnimationArray.getResourceId(5, 0);
            } else if (transit == 2) {
                enterAnim = windowAnimationArray.getResourceId(6, 0);
                exitAnim = windowAnimationArray.getResourceId(7, 0);
            } else if (transit == 3) {
                enterAnim = windowAnimationArray.getResourceId(8, 0);
                exitAnim = windowAnimationArray.getResourceId(9, 0);
            } else if (transit == 4) {
                enterAnim = windowAnimationArray.getResourceId(10, 0);
                exitAnim = windowAnimationArray.getResourceId(11, 0);
            }
            windowAnimationArray.recycle();
            try {
                ActivityTaskManager.getService().overridePendingTransition(this.mClientToken, sHwAnimResPackageName, enterAnim, exitAnim);
            } catch (RemoteException e) {
            }
        }
    }

    private void initToken() {
        try {
            Method method = Activity.class.getDeclaredMethod("getActivityToken", new Class[0]);
            method.setAccessible(true);
            this.mClientToken = (IBinder) method.invoke(this.mClientContext, new Object[0]);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
        }
    }
}
