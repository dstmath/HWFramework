package huawei.android.widget.appbar;

import android.app.ActionBar;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import huawei.android.widget.HwToolbar;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwExpandedAppbarController {
    public static final int EXTANED_APPBAR_LAYOUT = 0;
    private static final float FLOAT_ACCURACY_OFFSET = 0.5f;
    public static final int HIDE_APPBAR_LAYOUT = 1;
    private static final int INVALID_FLAG = -1;
    public static final int LIST_APPBAR_LAYOUT = 2;
    public static final int STATUS_COLLAPSED = 2;
    public static final int STATUS_EXPANDED = 0;
    public static final int STATUS_EXPANDING = 1;
    public static final int SUBTITLE_BEHAVIOR_DEFAULT_SHOW = 0;
    public static final int SUBTITLE_BEHAVIOR_GRADUALLY_HIDDEN = 1;
    private static final String TAG = "HwExpandedAppbarController";
    private Activity mActivity;
    private HwAppBarLayout mAppBarLayout = null;
    private ViewGroup mContentContainer;
    private HwCoordinatorLayout mCoordinatorLayout = null;
    private View mCustomView;
    private int mExpanedAppbarHeight;
    private int mExpanedAppbarHeightNoSubtitle;
    private int mFlag = -1;
    private HwCollapsingToolbarLayout mHwCollapsingToolbarLayout;
    private HwToolbar mHwToolbar;
    private View mRootView;

    public interface OnDragListener {
        void onDrag(int i, int i2);
    }

    public HwExpandedAppbarController(Activity activity) {
        this.mActivity = activity;
        initExpanedAppbarHeight();
    }

    private void initViews(int flag) {
        if (this.mRootView != null) {
            if (flag != 0) {
                if (flag != 1) {
                    if (flag != 2) {
                        return;
                    }
                }
                this.mContentContainer = (ViewGroup) this.mRootView.findViewById(34603052);
                this.mHwToolbar = (HwToolbar) this.mRootView.findViewById(34603551);
            }
            this.mHwCollapsingToolbarLayout = (HwCollapsingToolbarLayout) this.mRootView.findViewById(34603046);
            this.mContentContainer = (ViewGroup) this.mRootView.findViewById(34603052);
            this.mHwToolbar = (HwToolbar) this.mRootView.findViewById(34603551);
        } else if (this.mActivity != null) {
            if (flag != 0) {
                if (flag != 1) {
                    if (flag != 2) {
                        return;
                    }
                }
                this.mContentContainer = (ViewGroup) this.mActivity.findViewById(34603052);
                this.mHwToolbar = (HwToolbar) this.mActivity.findViewById(34603551);
            }
            this.mHwCollapsingToolbarLayout = (HwCollapsingToolbarLayout) this.mActivity.findViewById(34603046);
            this.mContentContainer = (ViewGroup) this.mActivity.findViewById(34603052);
            this.mHwToolbar = (HwToolbar) this.mActivity.findViewById(34603551);
        }
    }

    public void setContentView(int layoutRes) {
        Object inflaterFromActivity = this.mActivity.getSystemService("layout_inflater");
        if (inflaterFromActivity != null && (inflaterFromActivity instanceof LayoutInflater)) {
            View view = ((LayoutInflater) inflaterFromActivity).inflate(layoutRes, (ViewGroup) null, true);
            initViews(this.mFlag);
            ViewGroup viewGroup = this.mContentContainer;
            if (viewGroup != null) {
                viewGroup.removeAllViews();
                this.mContentContainer.addView(view);
            }
        }
        setAppBarLayoutHeightForNoSubTitle();
    }

    public void setContentView(int layoutRes, View rootView) {
        this.mRootView = rootView;
        Object inflaterFromActivity = this.mActivity.getSystemService("layout_inflater");
        if (inflaterFromActivity != null && (inflaterFromActivity instanceof LayoutInflater)) {
            View view = ((LayoutInflater) inflaterFromActivity).inflate(layoutRes, (ViewGroup) null, true);
            initViews(this.mFlag);
            ViewGroup viewGroup = this.mContentContainer;
            if (viewGroup != null) {
                viewGroup.removeAllViews();
                this.mContentContainer.addView(view);
            }
        }
        setAppBarLayoutHeightForNoSubTitle();
    }

    public void setBubbleCount(int count) {
        HwCollapsingToolbarLayout hwCollapsingToolbarLayout = this.mHwCollapsingToolbarLayout;
        if (hwCollapsingToolbarLayout != null) {
            hwCollapsingToolbarLayout.setBubbleCount(count);
            return;
        }
        HwToolbar hwToolbar = this.mHwToolbar;
        if (hwToolbar != null) {
            hwToolbar.setBubbleCount(count);
        }
    }

    public void setTitle(CharSequence title) {
        HwCollapsingToolbarLayout hwCollapsingToolbarLayout = this.mHwCollapsingToolbarLayout;
        if (hwCollapsingToolbarLayout != null) {
            hwCollapsingToolbarLayout.setTitle(title);
        }
    }

    public void setSubTitle(CharSequence subTitle) {
        int layoutHeight;
        HwCollapsingToolbarLayout hwCollapsingToolbarLayout = this.mHwCollapsingToolbarLayout;
        if (hwCollapsingToolbarLayout != null) {
            if (hwCollapsingToolbarLayout.getSubTitle() == null || subTitle == null) {
                if (this.mHwCollapsingToolbarLayout.getSubTitle() == subTitle) {
                    return;
                }
            } else if (this.mHwCollapsingToolbarLayout.getSubTitle().equals(subTitle)) {
                return;
            }
            this.mHwCollapsingToolbarLayout.setSubTitle(subTitle);
            if (this.mAppBarLayout != null) {
                if (subTitle == null) {
                    layoutHeight = this.mExpanedAppbarHeightNoSubtitle;
                } else {
                    layoutHeight = this.mExpanedAppbarHeight;
                }
                this.mAppBarLayout.setDefaultHeight(layoutHeight);
            }
        }
    }

    public void setEyebrowTitle(CharSequence eyebrowTitle) {
        HwCollapsingToolbarLayout hwCollapsingToolbarLayout = this.mHwCollapsingToolbarLayout;
        if (hwCollapsingToolbarLayout != null) {
            hwCollapsingToolbarLayout.setEyebrowTitle(eyebrowTitle);
        }
    }

    public int getLayoutRes(int layoutType) {
        this.mFlag = layoutType;
        int i = this.mFlag;
        if (i == 0) {
            return ResLoader.getInstance().getIdentifier(this.mActivity, ResLoaderUtil.LAYOUT, "emui_expaned_appbar_nested_layout");
        }
        if (i == 1) {
            return ResLoader.getInstance().getIdentifier(this.mActivity, ResLoaderUtil.LAYOUT, "emui_expaned_appbar_hide_layout");
        }
        if (i != 2) {
            return 0;
        }
        return ResLoader.getInstance().getIdentifier(this.mActivity, ResLoaderUtil.LAYOUT, "emui_expaned_appbar_list_layout");
    }

    public HwAppBarLayout getAppBarLayout() {
        HwAppBarLayout hwAppBarLayout = this.mAppBarLayout;
        if (hwAppBarLayout != null) {
            return hwAppBarLayout;
        }
        View view = this.mRootView;
        if (view != null) {
            this.mAppBarLayout = (HwAppBarLayout) view.findViewById(ResLoader.getInstance().getIdentifier(this.mActivity, ResLoaderUtil.ID, "appbar_layout"));
            return this.mAppBarLayout;
        }
        Activity activity = this.mActivity;
        if (activity != null) {
            this.mAppBarLayout = (HwAppBarLayout) activity.findViewById(ResLoader.getInstance().getIdentifier(this.mActivity, ResLoaderUtil.ID, "appbar_layout"));
        }
        return this.mAppBarLayout;
    }

    private void setAppBarLayoutHeightForNoSubTitle() {
        if (this.mHwCollapsingToolbarLayout != null && getAppBarLayout() != null && this.mHwCollapsingToolbarLayout.getSubTitle() == null) {
            getAppBarLayout().setDefaultHeight(this.mExpanedAppbarHeightNoSubtitle);
            this.mHwCollapsingToolbarLayout.resetTextBaselineY();
        }
    }

    private void initExpanedAppbarHeight() {
        int expanedAppbarHeightId = ResLoader.getInstance().getIdentifier(this.mActivity, ResLoaderUtil.DIMEN, "expaned_appbar_height");
        int expanedAppbarHeightNoSubtitleId = ResLoader.getInstance().getIdentifier(this.mActivity, ResLoaderUtil.DIMEN, "expaned_appbar_height_no_subtitle");
        this.mExpanedAppbarHeight = this.mActivity.getResources().getDimensionPixelSize(expanedAppbarHeightId);
        this.mExpanedAppbarHeightNoSubtitle = this.mActivity.getResources().getDimensionPixelSize(expanedAppbarHeightNoSubtitleId);
    }

    public void setPermitCollapse(boolean isPermit) {
        HwCoordinatorLayout hwCoordinatorLayout = this.mCoordinatorLayout;
        if (hwCoordinatorLayout != null) {
            hwCoordinatorLayout.mIsPertmitCollapse = isPermit;
            return;
        }
        View view = this.mRootView;
        if (view != null) {
            this.mCoordinatorLayout = (HwCoordinatorLayout) view.findViewById(ResLoader.getInstance().getIdentifier(this.mActivity, ResLoaderUtil.ID, "coordinator_layout"));
        } else {
            Activity activity = this.mActivity;
            if (activity != null) {
                this.mCoordinatorLayout = (HwCoordinatorLayout) activity.findViewById(ResLoader.getInstance().getIdentifier(this.mActivity, ResLoaderUtil.ID, "coordinator_layout"));
            }
        }
        HwCoordinatorLayout hwCoordinatorLayout2 = this.mCoordinatorLayout;
        if (hwCoordinatorLayout2 != null) {
            hwCoordinatorLayout2.mIsPertmitCollapse = isPermit;
        } else {
            Log.e(TAG, "setPermitCollapse: fail to get HwCoordinatorLayout.");
        }
    }

    public void setCustomView(View customView) {
        HwCollapsingToolbarLayout hwCollapsingToolbarLayout = this.mHwCollapsingToolbarLayout;
        if (hwCollapsingToolbarLayout != null) {
            this.mCustomView = customView;
            hwCollapsingToolbarLayout.setCustomView(this.mCustomView);
        }
    }

    public View getCustomView() {
        return this.mCustomView;
    }

    public HwToolbar getToolbar() {
        return this.mHwToolbar;
    }

    public void setOnDragListener(OnDragListener dragListener) {
        HwCollapsingToolbarLayout hwCollapsingToolbarLayout = this.mHwCollapsingToolbarLayout;
        if (hwCollapsingToolbarLayout != null) {
            hwCollapsingToolbarLayout.setOnDragListener(dragListener);
        }
    }

    public void setExpanded(boolean isExpanded, boolean hasAnimate) {
        HwAppBarLayout hwAppBarLayout = getAppBarLayout();
        if (hwAppBarLayout != null) {
            hwAppBarLayout.setExpanded(isExpanded, hasAnimate);
        }
    }

    public void setActived(boolean isEnabled) {
        setPermitCollapse(isEnabled);
    }

    public boolean isActived() {
        HwCoordinatorLayout hwCoordinatorLayout = this.mCoordinatorLayout;
        if (hwCoordinatorLayout != null) {
            return hwCoordinatorLayout.mIsPertmitCollapse;
        }
        return true;
    }

    public void reLayout() {
        HwToolbar hwToolbar = this.mHwToolbar;
        if (hwToolbar != null) {
            if (hwToolbar.getIconLayout() == null) {
                this.mHwToolbar.initIconLayout();
            }
            if (this.mActivity != null) {
                this.mActivity.getActionBar().setCustomView(this.mHwToolbar.getIconLayout(), new ActionBar.LayoutParams(-1, -2));
            }
        }
    }

    public int getMaxVisibleHeight() {
        if (getAppBarLayout() != null) {
            return getAppBarLayout().getHeight();
        }
        return 0;
    }

    public int getMinVisibleHeight() {
        if (getAppBarLayout() != null) {
            return getAppBarLayout().getHeight() - getAppBarLayout().getTotalScrollRange();
        }
        return 0;
    }

    public int getCurrentVisibleHeight() {
        if (getAppBarLayout() != null) {
            return getAppBarLayout().getCurrentVisibleHeight();
        }
        return -1;
    }

    public int getExpandedStatus() {
        if (getCurrentVisibleHeight() == getMaxVisibleHeight()) {
            return 0;
        }
        if (getCurrentVisibleHeight() == getMinVisibleHeight()) {
            return 2;
        }
        if (getCurrentVisibleHeight() <= getMinVisibleHeight() || getCurrentVisibleHeight() >= getMaxVisibleHeight()) {
            return -1;
        }
        return 1;
    }

    public void expand(int visibleHeight, boolean hasAnimate) {
        HwAppBarLayout hwAppBarLayout = getAppBarLayout();
        if (hwAppBarLayout != null) {
            hwAppBarLayout.expand(visibleHeight, hasAnimate);
        }
    }

    public View getAppbar() {
        return getAppBarLayout();
    }

    public void setSubTitleBehavior(int behaviorFlag) {
        HwCollapsingToolbarLayout hwCollapsingToolbarLayout = this.mHwCollapsingToolbarLayout;
        if (hwCollapsingToolbarLayout != null) {
            hwCollapsingToolbarLayout.setSubTitleBehavior(behaviorFlag);
        }
    }

    public int getSubTitleBehavior() {
        HwCollapsingToolbarLayout hwCollapsingToolbarLayout = this.mHwCollapsingToolbarLayout;
        if (hwCollapsingToolbarLayout != null) {
            return hwCollapsingToolbarLayout.getSubTitleBehavior();
        }
        return -1;
    }

    public void notifyContentOverScroll(int overScrollY) {
        HwAppBarLayout hwAppBarLayout = getAppBarLayout();
        if (hwAppBarLayout != null) {
            hwAppBarLayout.notifyContentOverScroll(overScrollY);
        }
    }

    public void setCustomViewSmoothScaleEnabled(boolean isSmoothScaleEnabled) {
        HwCollapsingToolbarLayout hwCollapsingToolbarLayout = this.mHwCollapsingToolbarLayout;
        if (hwCollapsingToolbarLayout != null) {
            hwCollapsingToolbarLayout.setCustomViewSmoothScaleEnabled(isSmoothScaleEnabled);
        }
    }

    public void setBubbleContentDescription(int resId) {
        HwCollapsingToolbarLayout hwCollapsingToolbarLayout = this.mHwCollapsingToolbarLayout;
        if (hwCollapsingToolbarLayout != null) {
            hwCollapsingToolbarLayout.setBubbleDescriptionId(resId);
        }
    }

    public void setImportantForAccessibility(int mode) {
        HwCollapsingToolbarLayout hwCollapsingToolbarLayout = this.mHwCollapsingToolbarLayout;
        if (hwCollapsingToolbarLayout != null) {
            hwCollapsingToolbarLayout.setImportantForAccessibility(mode);
        }
    }
}
