package huawei.android.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import huawei.android.widget.appbar.HwAppBarLayout;
import huawei.android.widget.appbar.HwExpandedAppbarController;

public class SearchbarAutoHideAnimation {
    private static final int ANIMATION_TIME_MS = 250;
    private static final float APPEAR_OR_HIDE_THRESHOLD = 0.5f;
    private static final float DELTA_Y_THRESHOLD_FOR_PROC = 0.1f;
    private static final int POINT_SIZE = 2;
    private static final String TAG = "SearchbarAutoHide";
    private Activity mActivity;
    private HwAppBarLayout mAppBarLayout;
    private HwExpandedAppbarController mAppbarController;
    private Animator mAppearAnimator;
    private Context mContext;
    private Animator mCurrentAnimator;
    private Animator mHideAnimator;
    private boolean mIsEnableListScroll;
    private boolean mIsInitialized;
    private float mLastY;
    private int mPointerId;
    private SearchView mSearchView;
    private View mSlipView;
    private LinearLayout mTargetLayout;
    private int mTargetViewOriginalHeight;
    private ViewGroup mTargetViewParentLayout;
    private int mTargetViewParentLayoutOriginalHeight;

    @TargetApi(11)
    public SearchbarAutoHideAnimation(Context context, HwExpandedAppbarController appbarController, ViewGroup searchViewHolder, SearchView searchView, View list) {
        this(context, appbarController, searchViewHolder, (LinearLayout) searchView, list);
        this.mSearchView = searchView;
    }

    public SearchbarAutoHideAnimation(Context context, HwExpandedAppbarController appbarController, ViewGroup targetViewHolder, LinearLayout targetView, View slipView) {
        this.mIsInitialized = false;
        this.mTargetViewParentLayoutOriginalHeight = 0;
        this.mTargetViewOriginalHeight = 0;
        this.mCurrentAnimator = null;
        this.mAppBarLayout = null;
        this.mAppbarController = null;
        this.mIsEnableListScroll = false;
        this.mActivity = null;
        this.mContext = context;
        this.mTargetViewParentLayout = targetViewHolder;
        this.mTargetLayout = targetView;
        this.mSlipView = slipView;
        if (!checkParameters(appbarController)) {
            Log.e(TAG, "SearchbarAutoHideAnimation : call checkParameters failed.");
        }
    }

    private boolean checkParameters(HwExpandedAppbarController appbarController) {
        if (appbarController != null) {
            this.mAppbarController = appbarController;
            this.mAppBarLayout = appbarController.getAppBarLayout();
            if (this.mAppBarLayout == null) {
                Log.w(TAG, "checkParameters: fail to get getAppBarLayout.");
            }
        }
        if (this.mContext == null) {
            Log.e(TAG, "checkParameters: params is illegal");
            return false;
        } else if (this.mTargetLayout == null && this.mSearchView == null) {
            Log.e(TAG, "checkParameters: target view is illegal");
            return false;
        } else {
            SearchView searchView = this.mSearchView;
            if (searchView != null) {
                this.mTargetViewOriginalHeight = searchView.getHeight();
            } else {
                this.mTargetViewOriginalHeight = this.mTargetLayout.getHeight();
            }
            if (!isExistSearchViewParentLayout()) {
                return true;
            }
            this.mTargetViewParentLayoutOriginalHeight = this.mTargetViewParentLayout.getHeight();
            if (this.mTargetViewParentLayoutOriginalHeight == this.mTargetViewOriginalHeight) {
                return true;
            }
            Log.w(TAG, "checkParameters: holder's height is not equal to target's, change to equal now. holderH " + this.mTargetViewParentLayoutOriginalHeight + ", targetViewH " + this.mTargetViewOriginalHeight);
            this.mTargetViewParentLayoutOriginalHeight = this.mTargetViewOriginalHeight;
            return true;
        }
    }

    private void setAppBarCollapseEnable(boolean isEnable) {
        HwExpandedAppbarController hwExpandedAppbarController = this.mAppbarController;
        if (hwExpandedAppbarController != null) {
            hwExpandedAppbarController.setPermitCollapse(isEnable);
        }
    }

    private void initWhenActionDown(float currentY) {
        this.mIsInitialized = false;
        if (this.mTargetViewOriginalHeight == 0) {
            Log.e(TAG, "initWhenActionDown : mTargetViewOriginalHeight is 0.");
            return;
        }
        SearchView searchView = this.mSearchView;
        if (searchView != null) {
            searchView.setGravity(80);
        } else {
            this.mTargetLayout.setGravity(80);
        }
        this.mLastY = currentY;
        this.mIsInitialized = true;
        Animator animator = this.mCurrentAnimator;
        if (animator != null) {
            animator.cancel();
            this.mCurrentAnimator = null;
        }
    }

    private boolean isActivityInactive(View view) {
        if (this.mActivity == null) {
            this.mActivity = HwSearchAnimationUtils.getActivity(view);
        }
        Activity activity = this.mActivity;
        if (activity == null) {
            return false;
        }
        if (activity.isFinishing()) {
            Log.w(TAG, "isActivityInactive: Activity is finishing");
            return true;
        } else if (!this.mActivity.isDestroyed()) {
            return false;
        } else {
            Log.w(TAG, "isActivityInactive: Activity is destroy");
            return true;
        }
    }

    private void listProcess(int currentHeight, int targetHeight, boolean isInAnimation) {
        View view;
        if (this.mIsEnableListScroll && (view = this.mSlipView) != null && !isActivityInactive(view) && isInAnimation) {
            View view2 = this.mSlipView;
            if (view2 instanceof AbsListView) {
                ((AbsListView) view2).scrollListBy(currentHeight - targetHeight);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void changeViewsParams(int targetHeight, boolean isInAnimation) {
        if (isExistSearchViewParentLayout()) {
            this.mTargetViewParentLayout.getLayoutParams().height = targetHeight;
            this.mTargetViewParentLayout.requestLayout();
        }
        SearchView searchView = this.mSearchView;
        if (searchView != null) {
            listProcess(searchView.getHeight(), targetHeight, isInAnimation);
            this.mSearchView.getLayoutParams().height = targetHeight;
            this.mSearchView.requestLayout();
            return;
        }
        listProcess(this.mTargetLayout.getHeight(), targetHeight, isInAnimation);
        this.mTargetLayout.getLayoutParams().height = targetHeight;
        this.mTargetLayout.requestLayout();
    }

    @TargetApi(11)
    private Animator getSearchBarAutoAppearAnimation() {
        int searchViewHolderH = getSearchViewHolderCurrentHeight();
        int searchViewHolderOriginalH = getSearchViewHolderOriginalHeight();
        if (searchViewHolderH == searchViewHolderOriginalH) {
            return null;
        }
        ValueAnimator viewHolderAnimator = ValueAnimator.ofInt(searchViewHolderH, searchViewHolderOriginalH);
        viewHolderAnimator.setDuration(250L);
        viewHolderAnimator.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563661));
        viewHolderAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.view.SearchbarAutoHideAnimation.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator animation) {
                SearchbarAutoHideAnimation.this.changeViewsParams(((Integer) animation.getAnimatedValue()).intValue(), true);
            }
        });
        viewHolderAnimator.addListener(new MyAnimatorListenner());
        this.mAppearAnimator = viewHolderAnimator;
        return viewHolderAnimator;
    }

    @TargetApi(11)
    private Animator getSearchBarAutoHideAnimation() {
        int searchViewHolderH = getSearchViewHolderCurrentHeight();
        if (searchViewHolderH == 0) {
            return null;
        }
        ValueAnimator viewHolderAnimator = ValueAnimator.ofInt(searchViewHolderH, 0);
        viewHolderAnimator.setDuration(250L);
        viewHolderAnimator.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563661));
        viewHolderAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.view.SearchbarAutoHideAnimation.AnonymousClass2 */

            public void onAnimationUpdate(ValueAnimator animation) {
                SearchbarAutoHideAnimation.this.changeViewsParams(((Integer) animation.getAnimatedValue()).intValue(), true);
            }
        });
        viewHolderAnimator.addListener(new MyAnimatorListenner());
        this.mHideAnimator = viewHolderAnimator;
        return viewHolderAnimator;
    }

    private boolean isExistSearchViewParentLayout() {
        if (!this.mIsEnableListScroll && this.mTargetViewParentLayout != null) {
            return true;
        }
        return false;
    }

    private int getSearchViewHolderCurrentHeight() {
        if (isExistSearchViewParentLayout()) {
            int searchViewHolderH = this.mTargetViewParentLayout.getLayoutParams().height;
            return searchViewHolderH >= 0 ? searchViewHolderH : this.mTargetViewParentLayout.getHeight();
        }
        SearchView searchView = this.mSearchView;
        if (searchView != null) {
            int targetViewH = searchView.getLayoutParams().height;
            return targetViewH >= 0 ? targetViewH : this.mSearchView.getHeight();
        }
        int targetViewH2 = this.mTargetLayout.getLayoutParams().height;
        return targetViewH2 >= 0 ? targetViewH2 : this.mTargetLayout.getHeight();
    }

    private int getSearchViewHolderOriginalHeight() {
        return isExistSearchViewParentLayout() ? this.mTargetViewParentLayoutOriginalHeight : this.mTargetViewOriginalHeight;
    }

    private Animator getSearchBarAutoAppearOrHideAnimation() {
        if (!this.mIsInitialized) {
            Log.e(TAG, "getSearchBarAutoAppearOrHideAnimation: the object has not been initialized yet.");
            return null;
        } else if (this.mCurrentAnimator != null) {
            Log.w(TAG, "getSearchBarAutoAppearOrHideAnimation: last animation has not finished.");
            return null;
        } else {
            int searchViewHolderH = getSearchViewHolderCurrentHeight();
            int searchViewHolderOriginalH = getSearchViewHolderOriginalHeight();
            if (searchViewHolderH == 0 || searchViewHolderH == searchViewHolderOriginalH) {
                return null;
            }
            if (((float) searchViewHolderH) > ((float) searchViewHolderOriginalH) * 0.5f) {
                return getSearchBarAutoAppearAnimation();
            }
            return getSearchBarAutoHideAnimation();
        }
    }

    private boolean setSearchBarHeightByScrollSize(float motionDeltaY) {
        int deltaHeight;
        int searchViewHolderH = getSearchViewHolderCurrentHeight();
        int searchViewHolderOriginalH = getSearchViewHolderOriginalHeight();
        int scrollY = new Float(motionDeltaY).intValue();
        if (scrollY > 0) {
            if (searchViewHolderH == searchViewHolderOriginalH) {
                setAppBarCollapseEnable(true);
                return false;
            } else if (searchViewHolderH + scrollY >= searchViewHolderOriginalH) {
                deltaHeight = searchViewHolderOriginalH - searchViewHolderH;
            } else {
                deltaHeight = scrollY;
            }
        } else if (searchViewHolderH == 0) {
            setAppBarCollapseEnable(true);
            return false;
        } else if (searchViewHolderH + scrollY <= 0) {
            deltaHeight = -searchViewHolderH;
        } else {
            deltaHeight = scrollY;
        }
        if (deltaHeight == 0) {
            return false;
        }
        changeViewsParams(searchViewHolderH + deltaHeight, false);
        return true;
    }

    private boolean handleSearchViewAutoAppearOrHide(float currentY) {
        if (!this.mIsInitialized) {
            Log.e(TAG, "handleSearchViewAutoAppearOrHide: the object has not been initialized yet.");
            return false;
        }
        float scrollY = currentY - this.mLastY;
        if (Math.abs(scrollY) < 0.1f) {
            return false;
        }
        this.mLastY = currentY;
        HwAppBarLayout hwAppBarLayout = this.mAppBarLayout;
        if (hwAppBarLayout != null && hwAppBarLayout.isCollapsible()) {
            if (!this.mAppBarLayout.isCollapsed() && scrollY < 0.0f) {
                return false;
            }
            int searchViewHolderH = getSearchViewHolderCurrentHeight();
            int searchViewHolderOriginalH = getSearchViewHolderOriginalHeight();
            if (this.mAppBarLayout.isCollapsed() && searchViewHolderH != searchViewHolderOriginalH) {
                setAppBarCollapseEnable(false);
            }
        }
        Animator animator = this.mCurrentAnimator;
        if (animator != null) {
            animator.cancel();
            this.mCurrentAnimator = null;
        }
        return setSearchBarHeightByScrollSize(scrollY) ? false : false;
    }

    private boolean isTouchInSlipView(MotionEvent motionEvent) {
        View view = this.mSlipView;
        if (view == null) {
            return true;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = this.mSlipView.getMeasuredWidth() + left;
        int bottom = this.mSlipView.getMeasuredHeight() + top;
        int x = (int) motionEvent.getRawX();
        int y = (int) motionEvent.getRawY();
        if (y <= top || y >= bottom || x <= left || x >= right) {
            return false;
        }
        return true;
    }

    private boolean isNeedToHandle(MotionEvent motionEvent) {
        if (!this.mIsInitialized) {
            Log.e(TAG, "isNeedToHandle: the object has not been initialized yet.");
            return false;
        } else if (!isTouchInSlipView(motionEvent)) {
            return false;
        } else {
            SearchView searchView = this.mSearchView;
            if (searchView == null) {
                return true;
            }
            ViewGroup.LayoutParams params = searchView.getLayoutParams();
            if (!(params instanceof ViewGroup.MarginLayoutParams) || ((ViewGroup.MarginLayoutParams) params).getMarginStart() == 0) {
                return true;
            }
            return false;
        }
    }

    public boolean handleMotionEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            return false;
        }
        int action = motionEvent.getActionMasked();
        int actionPointerIndex = motionEvent.getActionIndex();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    int pointerIndex = motionEvent.findPointerIndex(this.mPointerId);
                    if (pointerIndex < 0) {
                        pointerIndex = 0;
                    }
                    if (!this.mIsInitialized) {
                        initWhenActionDown(motionEvent.getRawY(pointerIndex));
                    }
                    if (!isNeedToHandle(motionEvent)) {
                        this.mLastY = motionEvent.getRawY(pointerIndex);
                        return false;
                    } else if (handleSearchViewAutoAppearOrHide(motionEvent.getRawY(pointerIndex))) {
                        return true;
                    }
                } else if (action != 3) {
                    if (action == 5) {
                        this.mPointerId = motionEvent.getPointerId(actionPointerIndex);
                        initWhenActionDown(motionEvent.getRawY(actionPointerIndex));
                    } else if (action == 6) {
                        onTouchPointerUp(motionEvent);
                    }
                }
            }
            if (!isNeedToHandle(motionEvent)) {
                int pointerIndex2 = motionEvent.findPointerIndex(this.mPointerId);
                if (pointerIndex2 < 0) {
                    pointerIndex2 = 0;
                }
                this.mLastY = motionEvent.getRawY(pointerIndex2);
                return false;
            }
            onTouchCancelAnimator(motionEvent);
        } else {
            this.mPointerId = motionEvent.getPointerId(0);
            initWhenActionDown(motionEvent.getRawY(0));
        }
        return false;
    }

    private void onTouchPointerUp(MotionEvent motionEvent) {
        int nowPointerIndex = (motionEvent.getAction() & 65280) >> 8;
        if (this.mPointerId == motionEvent.getPointerId(nowPointerIndex)) {
            int newPointerIndex = nowPointerIndex == 0 ? 1 : 0;
            this.mPointerId = motionEvent.getPointerId(newPointerIndex);
            initWhenActionDown(motionEvent.getRawY(newPointerIndex));
        }
    }

    private void onTouchCancelAnimator(MotionEvent motionEvent) {
        Animator animator = getSearchBarAutoAppearOrHideAnimation();
        if (animator != null) {
            animator.start();
        }
        setAppBarCollapseEnable(true);
        this.mIsInitialized = false;
    }

    /* access modifiers changed from: private */
    public class MyAnimatorListenner extends AnimatorListenerAdapter {
        private MyAnimatorListenner() {
        }

        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            SearchbarAutoHideAnimation.this.mCurrentAnimator = null;
            if (SearchbarAutoHideAnimation.this.mSearchView != null) {
                SearchbarAutoHideAnimation.this.mSearchView.requestLayout();
            } else {
                SearchbarAutoHideAnimation.this.mTargetLayout.requestLayout();
            }
        }

        public void onAnimationStart(Animator animation, boolean isReverse) {
            if (!isReverse) {
                SearchbarAutoHideAnimation.this.mCurrentAnimator = animation;
            }
        }
    }

    public void setListScrollEnabled(boolean enabled) {
        if (this.mSlipView == null) {
            Log.e(TAG, "setEnableListScroll: list is null.");
        } else {
            this.mIsEnableListScroll = enabled;
        }
    }

    public void startAnimation(boolean isHide) {
        Animator animator;
        SearchView searchView = this.mSearchView;
        if (searchView != null) {
            ViewGroup.LayoutParams params = searchView.getLayoutParams();
            if ((params instanceof ViewGroup.MarginLayoutParams) && ((ViewGroup.MarginLayoutParams) params).getMarginStart() != 0) {
                Log.w(TAG, "startAnimation: search view's margin start is not zero");
                return;
            }
        }
        SearchView searchView2 = this.mSearchView;
        if (searchView2 != null) {
            searchView2.setGravity(80);
        } else {
            LinearLayout linearLayout = this.mTargetLayout;
            if (linearLayout != null) {
                linearLayout.setGravity(80);
            }
        }
        Animator animator2 = this.mAppearAnimator;
        if (animator2 != null && animator2.isRunning()) {
            if (isHide) {
                this.mAppearAnimator.cancel();
            } else {
                return;
            }
        }
        Animator animator3 = this.mHideAnimator;
        if (animator3 != null && animator3.isRunning()) {
            if (!isHide) {
                this.mHideAnimator.cancel();
            } else {
                return;
            }
        }
        if (isHide) {
            animator = getSearchBarAutoHideAnimation();
        } else {
            animator = getSearchBarAutoAppearAnimation();
        }
        if (animator != null) {
            animator.start();
        }
    }

    public void cancelAnimation(boolean isHide) {
        if (isHide) {
            Animator animator = this.mHideAnimator;
            if (animator != null && animator.isRunning()) {
                this.mHideAnimator.cancel();
                changeViewsParams(0, true);
                return;
            }
            return;
        }
        Animator animator2 = this.mAppearAnimator;
        if (animator2 != null && animator2.isRunning()) {
            this.mAppearAnimator.cancel();
            changeViewsParams(this.mTargetViewOriginalHeight, true);
        }
    }
}
