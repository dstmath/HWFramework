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
    private static final float CONTROL_X1 = 0.325f;
    private static final float CONTROL_X2 = 0.05f;
    private static final float CONTROL_Y1 = 0.63f;
    private static final float CONTROL_Y2 = 1.0f;
    private static final float RATIO = 0.33333334f;
    private static final String TAG = "HwViewPagerImpl";
    private ActionBar mActionBar;
    private Context mContext;
    private Method mGetTabContainerMethod = null;
    private float mQuarterWidth;

    public HwViewPagerImpl(Context context) {
        Log.i(TAG, "new HwViewPagerImpl");
        this.mQuarterWidth = ((float) context.getResources().getDisplayMetrics().widthPixels) * RATIO;
        createTabScrollingMethod(context);
        this.mContext = context;
    }

    @Override // huawei.android.widget.HwViewPager
    public Scroller createScroller(Context context) {
        return new Scroller(context, new PathInterpolator(CONTROL_X1, CONTROL_Y1, CONTROL_X2, 1.0f));
    }

    @Override // huawei.android.widget.HwViewPager
    public void tabScrollerFollowed(int position, float offset) {
        if (this.mActionBar == null) {
            this.mActionBar = getActionBar();
        }
        if ((this.mActionBar == null || offset < 0.0f || this.mGetTabContainerMethod == null) ? false : true) {
            try {
                this.mGetTabContainerMethod.invoke(null, this.mActionBar, Integer.valueOf(position), Float.valueOf(offset));
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "mGetTabContainerMethod invoke catch IllegalArgumentException");
            } catch (IllegalAccessException e2) {
                Log.w(TAG, "mGetTabContainerMethod invoke catch IllegalAccessException");
            } catch (InvocationTargetException e3) {
                Log.w(TAG, "mGetTabContainerMethod invoke catch InvocationTargetException");
            }
        }
    }

    @Override // huawei.android.widget.HwViewPager
    public boolean canScrollEdge() {
        ActionBar actionbar = getActionBar();
        if (actionbar == null || actionbar.getTabCount() <= 1) {
            return false;
        }
        return true;
    }

    @Override // huawei.android.widget.HwViewPager
    public float scrollEdgeBound(boolean isLeft, float oldScroller, float deltax, float bound) {
        float scroller = (RATIO * deltax) + oldScroller;
        if (isLeft) {
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
                this.mGetTabContainerMethod = actionBarExUtilclazz.getDeclaredMethod("setTabScrollingOffsets", ActionBar.class, Integer.TYPE, Float.TYPE);
            }
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "create Tab Scrolling Method catch ClassNotFoundException");
        } catch (NoSuchMethodException e2) {
            Log.w(TAG, "create Tab Scrolling Method catch NoSuchMethodException");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0020  */
    /* JADX WARNING: Removed duplicated region for block: B:19:? A[RETURN, SYNTHETIC] */
    private ActionBar getActionBar() {
        Context context = this.mContext;
        Activity activity = null;
        while (true) {
            Context context2 = null;
            if (activity != null || context == null) {
                if (activity != null) {
                    return null;
                }
                return activity.getActionBar();
            } else if (context instanceof Activity) {
                activity = (Activity) context;
            } else {
                if (context instanceof ContextWrapper) {
                    context2 = ((ContextWrapper) context).getBaseContext();
                }
                context = context2;
            }
        }
        if (activity != null) {
        }
    }
}
