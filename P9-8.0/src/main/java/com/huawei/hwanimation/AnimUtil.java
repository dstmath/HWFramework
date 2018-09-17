package com.huawei.hwanimation;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.AttributeSet;
import com.android.internal.R;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AnimUtil {
    private static final boolean DEBUG = false;
    private static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static final boolean IS_NOVA_PERF = SystemProperties.getBoolean("ro.config.hw_nova_performance", false);
    private static final String TAG = "AnimUtil";
    public static final int TRANSIT_ACTIVITY_CLOSE = 2;
    public static final int TRANSIT_ACTIVITY_OPEN = 1;
    public static final int TRANSIT_TASK_CLOSE = 4;
    public static final int TRANSIT_TASK_OPEN = 3;
    private static String sHwAnimResPackageName = "androidhwext";
    private Context mClientContext = null;
    private IBinder mClientToken = null;

    public AnimUtil(Context context) {
        this.mClientContext = context;
        initToken();
    }

    public CubicBezierInterpolator getCubicBezierInterpolator(String name, Context context, AttributeSet attrs) {
        if (name.equals("cubicBezierInterpolator")) {
            return new CubicBezierInterpolator(context, attrs);
        }
        if (name.equals("cubicBezierReverseInterpolator")) {
            return new CubicBezierReverseInterpolator(context, attrs);
        }
        return null;
    }

    public CubicBezierInterpolator getCubicBezierInterpolator(String name, Resources res, Theme theme, AttributeSet attrs) {
        if (name.equals("cubicBezierInterpolator")) {
            return new CubicBezierInterpolator(res, theme, attrs);
        }
        if (name.equals("cubicBezierReverseInterpolator")) {
            return new CubicBezierReverseInterpolator(res, theme, attrs);
        }
        return null;
    }

    public void overrideTransition(int transit) {
        if (this.mClientToken != null) {
            try {
                Context hwAnimationContext = this.mClientContext.createPackageContext(sHwAnimResPackageName, 0);
                if (hwAnimationContext != null) {
                    overrideTransitionInternal(transit, hwAnimationContext);
                }
            } catch (NameNotFoundException e) {
            }
        }
    }

    private void overrideTransitionInternal(int transit, Context context) {
        int enterAnim = 0;
        int exitAnim = 0;
        int resId = 0;
        if (IS_EMUI_LITE || IS_NOVA_PERF) {
            resId = context.getResources().getIdentifier("HwAnimation_lite", "style", sHwAnimResPackageName);
        }
        if (resId == 0) {
            resId = context.getResources().getIdentifier("HwAnimation", "style", sHwAnimResPackageName);
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
                ActivityManagerNative.getDefault().overridePendingTransition(this.mClientToken, sHwAnimResPackageName, enterAnim, exitAnim);
            } catch (RemoteException e) {
            }
        }
    }

    private void initToken() {
        try {
            Method method = Activity.class.getDeclaredMethod("getActivityToken", new Class[0]);
            method.setAccessible(true);
            this.mClientToken = (IBinder) method.invoke(this.mClientContext, new Object[0]);
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e2) {
        } catch (IllegalAccessException e3) {
        } catch (InvocationTargetException e4) {
        }
    }
}
