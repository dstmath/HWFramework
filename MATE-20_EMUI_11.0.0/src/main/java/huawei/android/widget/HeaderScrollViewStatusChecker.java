package huawei.android.widget;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import huawei.android.widget.DecouplingUtil.ReflectUtil;

public class HeaderScrollViewStatusChecker implements ScrollViewStatusChecker {
    private static final String HW_APPBAR_LAYOUT_NAME = "huawei.android.widget.appbar.HwAppBarLayout";
    private static final String HW_APPBAR_PARENT_LAYOUT_NAME = "huawei.android.widget.appbar.HwCoordinatorLayout";
    private static final String HW_COLLAPSING_TOOLBAR_NAME = "huawei.android.widget.appbar.HwCollapsingToolbarLayout";
    private static final String HW_TOOLBAR_LAYOUT_NAME = "huawei.android.widget.HwToolbar";
    public static final int STATUS_COLLAPSED = 2;
    public static final int STATUS_EXPANDED = 0;
    public static final int STATUS_EXPANDING = 1;
    public static final int STATUS_INVALID = -1;
    public static final int STATUS_OVER_SCROLL = 3;
    public static final int UNSUPPORTED_NESTED_SCROLL_VIEW = -2;
    private ViewGroup mAppBarLayout = null;
    private int mAppBarLayoutExpandedHeight = Integer.MIN_VALUE;
    private Object mHwCoordinatorLayout = null;
    private ViewGroup mHwToolbar = null;
    private View mNestedScrollChild;

    public HeaderScrollViewStatusChecker(View nestedScrollChild) {
        this.mNestedScrollChild = nestedScrollChild;
    }

    private void searchAppBarAndToolBar(View nestedScrollChild) {
        ViewGroup viewGroup;
        if (nestedScrollChild != null) {
            for (ViewParent viewParent = nestedScrollChild.getParent(); viewParent != null; viewParent = viewParent.getParent()) {
                if (HW_APPBAR_PARENT_LAYOUT_NAME.equals(viewParent.getClass().getName())) {
                    if (viewParent instanceof ViewGroup) {
                        ViewGroup appBarParent = (ViewGroup) viewParent;
                        int childCount = appBarParent.getChildCount();
                        this.mHwCoordinatorLayout = viewParent;
                        for (int i = 0; i < childCount; i++) {
                            View child = appBarParent.getChildAt(i);
                            if (HW_APPBAR_LAYOUT_NAME.equals(child.getClass().getName()) && (child instanceof ViewGroup)) {
                                this.mAppBarLayout = (ViewGroup) child;
                                this.mHwToolbar = searchToolbar(this.mAppBarLayout);
                                if (this.mAppBarLayoutExpandedHeight == Integer.MIN_VALUE && (viewGroup = this.mAppBarLayout) != null) {
                                    this.mAppBarLayoutExpandedHeight = viewGroup.getHeight();
                                    return;
                                }
                                return;
                            }
                        }
                        return;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    private ViewGroup searchToolbar(ViewGroup appBarLayout) {
        if (appBarLayout.getChildCount() == 0) {
            return null;
        }
        View appbarChild = appBarLayout.getChildAt(0);
        if (!(appbarChild instanceof ViewGroup)) {
            return null;
        }
        ViewGroup collapsingToolbar = (ViewGroup) appbarChild;
        if (!HW_COLLAPSING_TOOLBAR_NAME.equals(collapsingToolbar.getClass().getName()) || collapsingToolbar.getChildCount() == 0) {
            return null;
        }
        View toolbar = collapsingToolbar.getChildAt(0);
        if (!(toolbar instanceof ViewGroup) || !HW_TOOLBAR_LAYOUT_NAME.equals(toolbar.getClass().getName())) {
            return null;
        }
        return (ViewGroup) toolbar;
    }

    @Override // huawei.android.widget.ScrollViewStatusChecker
    public int getScrollingViewStatus() {
        if (this.mHwCoordinatorLayout == null || this.mAppBarLayout == null || this.mHwToolbar == null) {
            searchAppBarAndToolBar(this.mNestedScrollChild);
            if (this.mHwCoordinatorLayout == null || this.mAppBarLayout == null || this.mHwToolbar == null || !isHeaderActive()) {
                return -2;
            }
        }
        if (!isHeaderActive()) {
            return -2;
        }
        return getAppBarLayoutStatus(this.mAppBarLayout);
    }

    @Override // huawei.android.widget.ScrollViewStatusChecker
    public int getScrollingViewHeight() {
        if (this.mHwCoordinatorLayout == null || this.mAppBarLayout == null || this.mHwToolbar == null) {
            searchAppBarAndToolBar(this.mNestedScrollChild);
            if (this.mHwCoordinatorLayout == null || this.mAppBarLayout == null || this.mHwToolbar == null || !isHeaderActive()) {
                return -2;
            }
        }
        return this.mAppBarLayout.getHeight() + this.mAppBarLayout.getTop();
    }

    private int getAppBarLayoutStatus(ViewGroup appBarLayout) {
        if (appBarLayout.getTop() == 0 && this.mAppBarLayoutExpandedHeight == appBarLayout.getHeight()) {
            return 0;
        }
        if (appBarLayout.getBottom() == this.mHwToolbar.getHeight()) {
            return 2;
        }
        if (appBarLayout.getHeight() > this.mAppBarLayoutExpandedHeight) {
            return 3;
        }
        if (appBarLayout.getTop() >= 0 || this.mAppBarLayoutExpandedHeight != appBarLayout.getHeight()) {
            return -1;
        }
        return 1;
    }

    private boolean isHeaderActive() {
        Object obj = this.mHwCoordinatorLayout;
        if (obj == null) {
            return false;
        }
        Object isPermitCollapse = ReflectUtil.getObject(obj, "mIsPertmitCollapse", obj.getClass());
        if (isPermitCollapse instanceof Boolean) {
            return ((Boolean) isPermitCollapse).booleanValue();
        }
        return false;
    }
}
