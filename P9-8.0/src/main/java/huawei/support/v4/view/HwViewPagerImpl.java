package huawei.support.v4.view;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.v4.interfaces.HwControlFactory.HwViewPager;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.Scroller;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwViewPagerImpl implements HwViewPager {
    private static final float mRatio = 0.33333334f;
    private String TAG = "HwViewPagerImpl";
    private ActionBar mActionBar;
    private Context mContext;
    private float mQuarterWidth;
    private Method mgetTabContainerMethod = null;

    public HwViewPagerImpl(Context context) {
        this.mQuarterWidth = ((float) context.getResources().getDisplayMetrics().widthPixels) * mRatio;
        createTabScrollingMethod(context);
        this.mContext = context;
    }

    public Scroller createScroller(Context context) {
        return new Scroller(context, new PathInterpolator(0.325f, 0.63f, 0.05f, 1.0f));
    }

    public void tabScrollerFollowed(int position, float offset) {
        if (this.mActionBar == null) {
            this.mActionBar = getActionBar();
        }
        if (this.mActionBar != null && offset >= 0.0f && this.mgetTabContainerMethod != null) {
            try {
                this.mgetTabContainerMethod.invoke(null, new Object[]{this.mActionBar, Integer.valueOf(position), Float.valueOf(offset)});
            } catch (IllegalArgumentException e) {
                Log.w(this.TAG, "mgetTabContainerMethod invoke catch IllegalArgumentException");
            } catch (IllegalAccessException e2) {
                Log.w(this.TAG, "mgetTabContainerMethod invoke catch IllegalAccessException");
            } catch (InvocationTargetException e3) {
                Log.w(this.TAG, "mgetTabContainerMethod invoke catch InvocationTargetException");
            }
        }
    }

    public boolean canScrollEdge() {
        ActionBar actionbar = getActionBar();
        if (actionbar == null || actionbar.getTabCount() <= 1) {
            return false;
        }
        return true;
    }

    public float scrollEdgeBound(boolean left, float oldScroller, float deltax, float bound) {
        float scroller = oldScroller + (mRatio * deltax);
        if (left) {
            return Math.max(scroller, bound - this.mQuarterWidth);
        }
        return Math.min(scroller, this.mQuarterWidth + bound);
    }

    public Interpolator createCubicBezierInterpolator(Context context) {
        try {
            Class<?> animUtilclazz = new PathClassLoader("/system/framework/hwEmui.jar", context.getClassLoader()).loadClass("com.huawei.hwanimation.CubicBezierInterpolator");
            if (animUtilclazz == null) {
                return null;
            }
            return (Interpolator) animUtilclazz.getConstructor(new Class[]{Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE}).newInstance(new Object[]{Float.valueOf(0.325f), Float.valueOf(0.63f), Float.valueOf(0.05f), Float.valueOf(1.0f)});
        } catch (ClassNotFoundException e) {
            Log.w(this.TAG, "create Cubic Bezier Interpolator catch ClassNotFoundException");
            return null;
        } catch (NoSuchMethodException e2) {
            Log.w(this.TAG, "create Cubic Bezier Interpolator catch NoSuchMethodException");
            return null;
        } catch (IllegalArgumentException e3) {
            Log.w(this.TAG, "create Cubic Bezier Interpolator catch IllegalArgumentException");
            return null;
        } catch (InstantiationException e4) {
            Log.w(this.TAG, "create Cubic Bezier Interpolator catch InstantiationException");
            return null;
        } catch (IllegalAccessException e5) {
            Log.w(this.TAG, "create Cubic Bezier Interpolator catch IllegalAccessException");
            return null;
        } catch (InvocationTargetException e6) {
            Log.w(this.TAG, "create Cubic Bezier Interpolator catch InvocationTargetException");
            return null;
        }
    }

    private void createTabScrollingMethod(Context context) {
        try {
            Class<?> actionBarExUtilclazz = new PathClassLoader("/system/framework/hwframework.jar", context.getClassLoader()).loadClass("com.huawei.android.app.ActionBarEx");
            if (actionBarExUtilclazz != null) {
                this.mgetTabContainerMethod = actionBarExUtilclazz.getDeclaredMethod("setTabScrollingOffsets", new Class[]{ActionBar.class, Integer.TYPE, Float.TYPE});
            }
        } catch (ClassNotFoundException e) {
            Log.w(this.TAG, "create Tab Scrolling Method catch ClassNotFoundException");
        } catch (NoSuchMethodException e2) {
            Log.w(this.TAG, "create Tab Scrolling Method catch NoSuchMethodException");
        }
    }

    private ActionBar getActionBar() {
        Context context = this.mContext;
        Activity activity = null;
        while (activity == null && context != null) {
            if (context instanceof Activity) {
                activity = (Activity) context;
            } else {
                context = context instanceof ContextWrapper ? ((ContextWrapper) context).getBaseContext() : null;
            }
        }
        if (activity == null) {
            return null;
        }
        return activity.getActionBar();
    }
}
