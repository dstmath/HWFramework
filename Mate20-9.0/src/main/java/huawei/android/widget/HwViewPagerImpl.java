package huawei.android.widget;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
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
        Log.i(this.TAG, "new HwViewPagerImpl");
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
        float scroller = (mRatio * deltax) + oldScroller;
        if (left) {
            float max = bound - this.mQuarterWidth;
            return scroller > max ? scroller : max;
        }
        float min = this.mQuarterWidth + bound;
        return scroller < min ? scroller : min;
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
