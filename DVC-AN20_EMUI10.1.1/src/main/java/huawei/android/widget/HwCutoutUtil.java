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
    private boolean mIsNeedFitCutout;
    private boolean mIsViewInCutoutArea;

    public HwCutoutUtil() {
        this.mCutoutPadding = 0;
        this.mDisplayRotate = 0;
        this.mIsNeedFitCutout = false;
        this.mIsViewInCutoutArea = true;
        this.mCutoutPadding = 0;
        this.mDisplayRotate = 0;
        this.mIsNeedFitCutout = false;
        this.mIsViewInCutoutArea = true;
    }

    public static boolean isNavigationBarExist(Context context) {
        if (context == null || Settings.Global.getInt(context.getContentResolver(), KEY_NAVIGATION_BAR_STATUS, 0) != 0) {
            return false;
        }
        return true;
    }

    public static boolean getDisplayCutoutStatus(Context context) {
        if (context == null || Settings.Secure.getInt(context.getContentResolver(), DISPLAY_NOTCH_STATUS, 0) != 0) {
            return false;
        }
        return true;
    }

    private static Activity getActivityFromContext(Context context) {
        for (Context curContext = context; curContext instanceof ContextWrapper; curContext = ((ContextWrapper) curContext).getBaseContext()) {
            if (curContext instanceof Activity) {
                return (Activity) curContext;
            }
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
        if (attrs == null) {
            return false;
        }
        if (((systemUiVisibillity & 1024) != 0 || (systemUiVisibillity & 2048) != 0 || (systemUiVisibillity & 512) != 0 || (attrs.flags & 67108864) != 0) && attrs.layoutInDisplayCutoutMode == 1 && getDisplayCutoutStatus(context)) {
            return true;
        }
        return false;
    }

    public static int getDisplayRotate(Context context) {
        WindowManager windowManager;
        if (context == null || (windowManager = (WindowManager) context.getSystemService("window")) == null) {
            return 0;
        }
        return windowManager.getDefaultDisplay().getRotation();
    }

    public void checkCutoutStatus(WindowInsets insets, View view, Context context) {
        boolean isNeedFitCutout = false;
        this.mCutoutPadding = 0;
        this.mDisplayRotate = 0;
        this.mIsNeedFitCutout = false;
        this.mIsViewInCutoutArea = true;
        DisplayCutout displayCutout = null;
        if (insets != null) {
            displayCutout = insets.getDisplayCutout();
        }
        if (!(displayCutout == null || view == null || context == null || !needDoCutoutFit(view, context))) {
            isNeedFitCutout = true;
        }
        if (isNeedFitCutout) {
            int rotate = getDisplayRotate(context);
            boolean isNavigationBarExist = isNavigationBarExist(context);
            if (rotate == 1) {
                this.mIsNeedFitCutout = true;
                this.mDisplayRotate = 1;
                this.mCutoutPadding = displayCutout.getSafeInsetLeft();
            } else if (rotate == 3 && !isNavigationBarExist) {
                this.mIsNeedFitCutout = true;
                this.mDisplayRotate = 3;
                this.mCutoutPadding = displayCutout.getSafeInsetRight();
            }
        }
    }

    public void checkViewInCutoutArea(View view) {
        Rect rect = new Rect();
        Rect rootrect = new Rect();
        if (view != null && view.getGlobalVisibleRect(rect) && view.getRootView().getGlobalVisibleRect(rootrect)) {
            if (this.mIsNeedFitCutout && this.mDisplayRotate == 1 && rect.left < this.mCutoutPadding) {
                this.mIsViewInCutoutArea = true;
            } else if (!this.mIsNeedFitCutout || this.mDisplayRotate != 3 || rootrect.right - rect.right >= this.mCutoutPadding) {
                this.mIsViewInCutoutArea = false;
            } else {
                this.mIsViewInCutoutArea = true;
            }
        }
    }

    public void doCutoutPadding(View view, int left, int right) {
        if (view == null) {
            return;
        }
        if (this.mIsNeedFitCutout && this.mDisplayRotate == 1 && this.mIsViewInCutoutArea) {
            view.setPadding(this.mCutoutPadding + left, view.getPaddingTop(), right, view.getPaddingBottom());
        } else if (!this.mIsNeedFitCutout || this.mDisplayRotate != 3 || !this.mIsViewInCutoutArea) {
            view.setPadding(left, view.getPaddingTop(), right, view.getPaddingBottom());
        } else {
            view.setPadding(left, view.getPaddingTop(), this.mCutoutPadding + right, view.getPaddingBottom());
        }
    }

    public int getCutoutPadding() {
        return this.mCutoutPadding;
    }

    public boolean getNeedFitCutout() {
        return this.mIsNeedFitCutout;
    }

    public int getDisplayRotate() {
        return this.mDisplayRotate;
    }

    public boolean isFirstItemNeedFitCutout(int currentItemIdex) {
        return this.mIsNeedFitCutout && 0 == currentItemIdex && this.mDisplayRotate == 1;
    }

    public int caculateScrollBarPadding(int scrollbarPosition) {
        if (this.mIsNeedFitCutout && this.mDisplayRotate == 1 && this.mIsViewInCutoutArea && scrollbarPosition == 1) {
            return this.mCutoutPadding;
        }
        if (!this.mIsNeedFitCutout || this.mDisplayRotate != 3 || !this.mIsViewInCutoutArea || scrollbarPosition != 2) {
            return 0;
        }
        return -this.mCutoutPadding;
    }

    public boolean getViewInCutoutArea() {
        return this.mIsViewInCutoutArea;
    }
}
