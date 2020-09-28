package huawei.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import androidhwext.R;
import com.huawei.android.view.WindowManagerEx;
import java.lang.annotation.RCUnownedRef;

public class HwWidgetSafeInsets {
    private static final int CUTOUT_MODE_ALAWYS = 1;
    private static final int CUTOUT_MODE_DEFAULT = 0;
    private static final int CUTOUT_MODE_NEVER = 2;
    private static final int FLAG_REQUEST_APPLY = 2;
    private static final int FLAG_UPDATE_INSETS_PADDING = 1;
    private static final String KEY_NAVIGATION_BAR_STATUS = "navigationbar_is_min";
    private static final int LOCATION_SIZE = 2;
    private static final String TAG = "HwWidgetSafeInsets";
    private final ApplyInsetsAction mApplyInsetsAction = new ApplyInsetsAction();
    private int mCutoutMode = 0;
    private boolean mIsDealTop = false;
    private boolean mIsInitOriginPaddings = false;
    private boolean mIsNavigationExist = false;
    private boolean mIsUserSetPaddings = false;
    private final Rect mOriginPadding = new Rect();
    private int mPrivateFlag = 0;
    private final Rect mSafeInsets = new Rect();
    @RCUnownedRef
    private final View mTarget;

    /* access modifiers changed from: private */
    public static class ApplyInsetsAction implements Runnable {
        View mTargetView;

        private ApplyInsetsAction() {
            this.mTargetView = null;
        }

        public void run() {
            this.mTargetView.requestLayout();
            this.mTargetView = null;
        }
    }

    public HwWidgetSafeInsets(View target) {
        this.mTarget = target;
    }

    public boolean isCutoutModeNever() {
        return this.mCutoutMode == 2;
    }

    public void parseHwDisplayCutout(Context context, AttributeSet attributeSet) {
        if (context == null || attributeSet == null) {
            Log.w(TAG, "parse cutout mode error");
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.HwDisplayCutout);
        this.mCutoutMode = array.getInt(0, 0);
        array.recycle();
        Log.i(TAG, "parse cutout mode : " + this.mCutoutMode);
    }

    public void updateOriginPadding(int left, int top, int right, int bottom) {
        updateOriginPadding(new Rect(left, top, right, bottom));
    }

    public void updateOriginPadding(Rect rect) {
        if (this.mTarget.isAttachedToWindow() && (this.mPrivateFlag & 1) == 0) {
            if (!this.mIsInitOriginPaddings) {
                this.mOriginPadding.set(rect);
                this.mIsInitOriginPaddings = true;
                this.mPrivateFlag |= 2;
                return;
            }
            this.mIsUserSetPaddings = true;
        }
    }

    public void updateWindowInsets(WindowInsets windowInsets) {
        Rect rect = WindowManagerEx.LayoutParamsEx.getDisplaySafeInsets(windowInsets);
        boolean isNavigationExist = isNavigationBarExist(this.mTarget);
        if (!this.mSafeInsets.equals(rect) || this.mIsNavigationExist != isNavigationExist) {
            this.mSafeInsets.set(rect);
            this.mPrivateFlag |= 2;
            this.mIsNavigationExist = isNavigationExist;
            View view = this.mTarget;
            if (view != null) {
                view.requestLayout();
            }
        }
    }

    public void applyDisplaySafeInsets(View view, Rect rect, boolean forceLayout) {
        if (view != null && rect != null && !isCutoutModeNever()) {
            int i = this.mPrivateFlag;
            if ((i & 2) != 0) {
                this.mPrivateFlag = i | 1;
                view.setPadding(rect.left, rect.top, rect.right, rect.bottom);
                this.mPrivateFlag &= -2;
                if (forceLayout) {
                    this.mTarget.removeCallbacks(this.mApplyInsetsAction);
                    ApplyInsetsAction applyInsetsAction = this.mApplyInsetsAction;
                    View view2 = this.mTarget;
                    applyInsetsAction.mTargetView = view2;
                    view2.post(applyInsetsAction);
                    this.mPrivateFlag &= -3;
                }
            }
        }
    }

    public void applyDisplaySafeInsets(boolean forceLayout) {
        if ((this.mPrivateFlag & 2) != 0) {
            View view = this.mTarget;
            applyDisplaySafeInsets(view, getDisplaySafeInsets(view), forceLayout);
        }
    }

    public boolean isShouldApply() {
        return (this.mPrivateFlag & 2) != 0;
    }

    public Rect getDisplaySafeInsets(View view, Rect originPadding) {
        int realRadiusSize;
        Rect paddingRect = new Rect();
        if (view == null || originPadding == null) {
            return paddingRect;
        }
        paddingRect.set(originPadding);
        if (isCutoutModeNever()) {
            return paddingRect;
        }
        Rect rect = new Rect();
        Rect rootViewRect = new Rect();
        int[] viewLocation = new int[2];
        view.getLocationInWindow(viewLocation);
        rect.set(viewLocation[0], viewLocation[1], viewLocation[0] + view.getWidth(), viewLocation[1] + view.getHeight());
        View rootView = view.getRootView();
        if (rootView == null) {
            return paddingRect;
        }
        rootView.getLocationInWindow(viewLocation);
        rootViewRect.set(viewLocation[0], viewLocation[1], viewLocation[0] + rootView.getWidth(), viewLocation[1] + rootView.getHeight());
        if (this.mCutoutMode == 1) {
            if (this.mSafeInsets.left > 0) {
                paddingRect.left = originPadding.left + this.mSafeInsets.left;
            }
            if (this.mSafeInsets.right > 0) {
                paddingRect.right = originPadding.right + this.mSafeInsets.right;
            }
        } else {
            if (this.mSafeInsets.left > 0 && rect.left < this.mSafeInsets.left) {
                paddingRect.left = originPadding.left + this.mSafeInsets.left;
            }
            if (this.mSafeInsets.right > 0 && rootViewRect.right - this.mSafeInsets.right < rect.right) {
                paddingRect.right = originPadding.right + this.mSafeInsets.right;
            }
        }
        if (isPortrait(view.getContext())) {
            if (this.mIsDealTop) {
                if (this.mSafeInsets.top <= 0 || rect.top >= this.mSafeInsets.top) {
                    if (this.mIsUserSetPaddings) {
                        paddingRect.top = view.getPaddingTop();
                    }
                } else if (rect.top + view.getPaddingTop() < this.mSafeInsets.top) {
                    paddingRect.top = this.mSafeInsets.top;
                } else {
                    paddingRect.top = view.getPaddingTop();
                }
            }
            int radiusSize = RadiusSizeUtils.getRadiusSize(view.getContext());
            if (radiusSize > 0 && (realRadiusSize = computeRealRadiusSize(view, radiusSize)) > 0) {
                paddingRect.bottom = originPadding.bottom + realRadiusSize;
            }
        } else if (this.mIsDealTop && this.mIsUserSetPaddings) {
            paddingRect.top = view.getPaddingTop();
        }
        return paddingRect;
    }

    public Rect getDisplaySafeInsets(View view) {
        return getDisplaySafeInsets(view, this.mOriginPadding);
    }

    private int computeRealRadiusSize(View view, int radiusSize) {
        if (view == null) {
            return 0;
        }
        int[] realSize = new int[2];
        view.getLocationOnScreen(realSize);
        Context context = view.getContext();
        if (context == null) {
            return 0;
        }
        DisplayMetrics outMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealMetrics(outMetrics);
        return ((realSize[1] + view.getMeasuredHeight()) + radiusSize) - outMetrics.heightPixels;
    }

    private boolean isNavigationBarExist(View view) {
        if (view == null || Settings.Global.getInt(view.getContext().getContentResolver(), KEY_NAVIGATION_BAR_STATUS, 0) != 0) {
            return false;
        }
        return true;
    }

    public void setDealTop(boolean isDealTop) {
        this.mIsDealTop = isDealTop;
    }

    private boolean isPortrait(Context context) {
        WindowManager windowManager;
        int rotate = 0;
        if (!(context == null || (windowManager = (WindowManager) context.getSystemService("window")) == null)) {
            rotate = windowManager.getDefaultDisplay().getRotation();
        }
        return rotate == 0 || rotate == 2;
    }
}
