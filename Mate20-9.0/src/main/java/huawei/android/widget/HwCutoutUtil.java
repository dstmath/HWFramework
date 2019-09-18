package huawei.android.widget;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.provider.Settings;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

public class HwCutoutUtil {
    private static final String DISPLAY_NOTCH_STATUS = "display_notch_status";
    private static final int DISPLAY_NOTCH_STATUS_DEFAULT = 0;
    private static final int DISPLAY_NOTCH_STATUS_HIDE = 1;
    private static final String KEY_NAVIGATION_BAR_STATUS = "navigationbar_is_min";
    private static final int LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS = 1;
    private static final String TAG = "HwCutoutUtil";
    private int mCutoutPadding;
    private int mDisplayRotate;
    private boolean mNeedFitCutout;
    private boolean mViewInCutoutArea;

    public HwCutoutUtil() {
        this.mCutoutPadding = 0;
        this.mDisplayRotate = 0;
        this.mNeedFitCutout = false;
        this.mViewInCutoutArea = true;
        this.mCutoutPadding = 0;
        this.mDisplayRotate = 0;
        this.mNeedFitCutout = false;
        this.mViewInCutoutArea = true;
    }

    public static boolean isNavigationBarExist(Context context) {
        boolean exist = false;
        if (context == null) {
            return false;
        }
        if (Settings.Global.getInt(context.getContentResolver(), KEY_NAVIGATION_BAR_STATUS, 0) == 0) {
            exist = true;
        }
        return exist;
    }

    public static boolean getDisplayCutoutStatus(Context context) {
        boolean exist = false;
        if (context == null) {
            return false;
        }
        if (Settings.Secure.getInt(context.getContentResolver(), DISPLAY_NOTCH_STATUS, 0) == 0) {
            exist = true;
        }
        return exist;
    }

    private static Activity getActivityFromContext(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static boolean needDoCutoutFit(View view, Context context) {
        int systemUiVisibillity = 0;
        WindowManager.LayoutParams attrs = null;
        if (view != null) {
            systemUiVisibillity = view.getWindowSystemUiVisibility();
        }
        Activity activity = getActivityFromContext(context);
        if (activity != null) {
            attrs = activity.getWindow().getAttributes();
        }
        if (attrs == null || (((systemUiVisibillity & 1024) == 0 && (systemUiVisibillity & 2048) == 0 && (systemUiVisibillity & 512) == 0 && (attrs.flags & 67108864) == 0) || attrs.layoutInDisplayCutoutMode != 1 || !getDisplayCutoutStatus(context))) {
            return false;
        }
        return true;
    }

    public static int getDisplayRotate(Context context) {
        if (context != null) {
            WindowManager wmManager = (WindowManager) context.getSystemService("window");
            if (wmManager != null) {
                int rotation = wmManager.getDefaultDisplay().getRotation();
                int rotate = rotation;
                return rotation;
            }
        }
        return 0;
    }

    public void checkCutoutStatus(WindowInsets insets, View view, Context context) {
        this.mCutoutPadding = 0;
        this.mDisplayRotate = 0;
        this.mNeedFitCutout = false;
        this.mViewInCutoutArea = true;
        DisplayCutout displayCutout = null;
        if (insets != null) {
            displayCutout = insets.getDisplayCutout();
        }
        if (displayCutout != null && view != null && context != null && needDoCutoutFit(view, context)) {
            int rotate = getDisplayRotate(context);
            boolean noNavigationBar = !isNavigationBarExist(context);
            if (1 == rotate) {
                this.mNeedFitCutout = true;
                this.mDisplayRotate = 1;
                this.mCutoutPadding = displayCutout.getSafeInsetLeft();
            } else if (3 == rotate && noNavigationBar) {
                this.mNeedFitCutout = true;
                this.mDisplayRotate = 3;
                this.mCutoutPadding = displayCutout.getSafeInsetRight();
            }
        }
    }

    public void checkViewInCutoutArea(View view) {
        Rect rect = new Rect();
        Rect rootrect = new Rect();
        if (view != null && view.getGlobalVisibleRect(rect) && view.getRootView().getGlobalVisibleRect(rootrect)) {
            if (this.mNeedFitCutout && 1 == this.mDisplayRotate && rect.left < this.mCutoutPadding) {
                this.mViewInCutoutArea = true;
            } else if (!this.mNeedFitCutout || 3 != this.mDisplayRotate || rootrect.right - rect.right >= this.mCutoutPadding) {
                this.mViewInCutoutArea = false;
            } else {
                this.mViewInCutoutArea = true;
            }
        }
    }

    public void doCutoutPadding(View view, int left, int right) {
        if (view == null) {
            return;
        }
        if (this.mNeedFitCutout && 1 == this.mDisplayRotate && this.mViewInCutoutArea) {
            view.setPadding(this.mCutoutPadding + left, view.getPaddingTop(), right, view.getPaddingBottom());
        } else if (!this.mNeedFitCutout || 3 != this.mDisplayRotate || !this.mViewInCutoutArea) {
            view.setPadding(left, view.getPaddingTop(), right, view.getPaddingBottom());
        } else {
            view.setPadding(left, view.getPaddingTop(), this.mCutoutPadding + right, view.getPaddingBottom());
        }
    }

    public int getCutoutPadding() {
        return this.mCutoutPadding;
    }

    public boolean getNeedFitCutout() {
        return this.mNeedFitCutout;
    }

    public int getDisplayRotate() {
        return this.mDisplayRotate;
    }

    public boolean isFirstItemNeedFitCutout(int currentItemIdex) {
        return this.mNeedFitCutout && 0 == currentItemIdex && 1 == this.mDisplayRotate;
    }

    public int caculateScrollBarPadding(int scrollbarPosition) {
        if (this.mNeedFitCutout && 1 == this.mDisplayRotate && this.mViewInCutoutArea && scrollbarPosition == 1) {
            return this.mCutoutPadding;
        }
        if (!this.mNeedFitCutout || 3 != this.mDisplayRotate || !this.mViewInCutoutArea || scrollbarPosition != 2) {
            return 0;
        }
        return -this.mCutoutPadding;
    }

    public boolean getViewInCutoutArea() {
        return this.mViewInCutoutArea;
    }
}
