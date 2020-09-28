package huawei.android.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.SearchView;
import android.widget.Toolbar;
import huawei.android.view.HwSearchAnimationUtils;
import huawei.android.widget.HwToolbar;
import huawei.android.widget.appbar.HwAppBarLayout;
import huawei.android.widget.appbar.HwExpandedAppbarController;

public class HwSearchAnimation {
    private static final int DEFAULT_MEASURE_SPEC = View.MeasureSpec.makeMeasureSpec(0, 0);
    private static final int LOCATION_SIZE = 2;
    private static final String TAG = "HwSearchAnimation";
    private Activity mActivity = null;
    private HwSearchAnimationUtils.AnimationParams mAnimationParams;
    private HwAppBarLayout mAppbar;
    private HwExpandedAppbarController mAppbarCtrl;
    private int mAppbarOriginalHeight = 0;
    private ViewGroup mArrowHostBack;
    private ViewGroup mArrowParentBack;
    private View mBackView;
    private Animator mCancelAnimator;
    private View mExtraView;
    private int mExtraViewOriginalHeight;
    private boolean mIsFastScroll;
    private boolean mIsGettingCancelAnimator = false;
    private boolean mIsGettingOpenAnimator = false;
    private boolean mIsHorizontalScroll;
    private boolean mIsInited = false;
    private boolean mIsVerticalScroll;
    private View mList;
    private float mListScrollBase = 0.0f;
    private float mListScrollRange = 0.0f;
    private View mMaskView;
    private Animator mOpenAnimator;
    private ViewGroup mSearchHostBack;
    private ViewGroup mSearchParentBack;
    private View mSearchView;
    private Toolbar mToolBar;

    public HwSearchAnimation(HwExpandedAppbarController controller, HwSearchAnimationUtils.AnimationParams params, View list) {
        if (controller == null || params == null) {
            Log.e(TAG, "HwSearchAnimation: input is null");
            return;
        }
        this.mAppbarCtrl = controller;
        this.mAppbar = controller.getAppBarLayout();
        if (this.mAppbar == null) {
            Log.e(TAG, "HwSearchAnimation: fail to get appbar layout");
            return;
        }
        this.mAnimationParams = params;
        this.mList = list;
        this.mIsInited = checkAnimationParams();
    }

    public HwSearchAnimation(Toolbar toolbar, HwSearchAnimationUtils.AnimationParams params, View list) {
        if (toolbar == null || params == null) {
            Log.e(TAG, "HwSearchAnimation: input is null");
            return;
        }
        this.mAnimationParams = params;
        this.mToolBar = toolbar;
        this.mList = list;
        this.mIsInited = checkAnimationParams();
    }

    private boolean checkAnimationParams() {
        HwSearchAnimationUtils.AnimationParams animationParams = this.mAnimationParams;
        if (animationParams == null) {
            return false;
        }
        this.mSearchView = animationParams.getSearchView();
        if (this.mSearchView == null) {
            return false;
        }
        this.mBackView = this.mAnimationParams.getBackButton();
        if (this.mBackView == null) {
            return false;
        }
        this.mMaskView = this.mAnimationParams.getMask();
        return true;
    }

    public void setExtraView(View view) {
        this.mExtraView = view;
        View view2 = this.mExtraView;
        if (view2 != null) {
            this.mExtraViewOriginalHeight = view2.getHeight();
            if (this.mExtraViewOriginalHeight <= 0) {
                View view3 = this.mExtraView;
                int i = DEFAULT_MEASURE_SPEC;
                view3.measure(i, i);
                this.mExtraViewOriginalHeight = this.mExtraView.getMeasuredHeight();
                if (this.mExtraViewOriginalHeight <= 0) {
                    Log.e(TAG, "setExtraView: fail to get the view's height");
                    this.mExtraView = null;
                }
            }
        }
    }

    @TargetApi(21)
    private Animator getBelowToolbarOpenAnimator() {
        this.mIsGettingOpenAnimator = true;
        int searchHeight = this.mSearchView.getHeight();
        if (this.mSearchView.getWidth() != this.mToolBar.getWidth() || searchHeight <= 0) {
            Log.e(TAG, "getBelowToolbarOpenAnimator: search's width is not equal to toolbar's.");
            this.mIsGettingOpenAnimator = false;
            return null;
        }
        final int dstMarginStart = getSearchMarginStart();
        final float dstTranslationY = (float) (-this.mToolBar.getHeight());
        if (dstMarginStart <= 0 || dstTranslationY >= 0.0f) {
            Log.e(TAG, "getBelowToolbarOpenAnimator: fail to get the animator values.");
            this.mIsGettingOpenAnimator = false;
            return null;
        }
        setBackViewBeforeOpenAnimator((int) dstTranslationY);
        ValueAnimator animatorOthers = HwSearchAnimationUtils.getWholeTimeAnimator(this.mSearchView, searchHeight, searchHeight, true);
        animatorOthers.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.view.HwSearchAnimation.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                HwSearchAnimationUtils.procSearchWholeTimeAnimation(HwSearchAnimation.this.mSearchView, ((float) dstMarginStart) * value);
                HwSearchAnimationUtils.procWholeTimeAlphaAnimation(HwSearchAnimation.this.mMaskView, HwSearchAnimation.this.mBackView, value);
                float oldTranslation = HwSearchAnimation.this.mSearchView.getTranslationY();
                float newTranslation = dstTranslationY * value;
                HwSearchAnimation.this.scrollListWithSearchView(oldTranslation, newTranslation, animation.getAnimatedFraction(), true);
                HwSearchAnimation.this.mSearchView.setTranslationY(newTranslation);
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(HwSearchAnimationUtils.getAppBarAnimator(this.mToolBar, searchHeight, searchHeight, true), animatorOthers);
        procOpenAnimationListener(animatorSet);
        this.mIsGettingOpenAnimator = false;
        this.mOpenAnimator = animatorSet;
        return animatorSet;
    }

    @TargetApi(21)
    private Animator getBelowToolbarCancelAnimator() {
        this.mIsGettingCancelAnimator = true;
        int searchHeight = this.mSearchView.getHeight();
        if (((int) this.mSearchView.getTranslationY()) != (-this.mToolBar.getHeight()) || searchHeight <= 0) {
            this.mSearchView.clearFocus();
            Log.e(TAG, "getBelowToolbarCancelAnimator: SearchView's translation is not match the condition.");
            this.mIsGettingCancelAnimator = false;
            return null;
        }
        final int originalMarginStart = getSearchCurrentMarginStart();
        final float originalTranslationY = this.mSearchView.getTranslationY();
        ValueAnimator animatorOthers = HwSearchAnimationUtils.getWholeTimeAnimator(this.mSearchView, searchHeight, (int) (-originalTranslationY), false);
        animatorOthers.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.view.HwSearchAnimation.AnonymousClass2 */

            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                HwSearchAnimationUtils.procWholeTimeAlphaAnimation(HwSearchAnimation.this.mMaskView, HwSearchAnimation.this.mBackView, value);
                HwSearchAnimationUtils.procSearchWholeTimeAnimation(HwSearchAnimation.this.mSearchView, ((float) originalMarginStart) * value);
                float oldTranslation = HwSearchAnimation.this.mSearchView.getTranslationY();
                float newTranslation = originalTranslationY * value;
                HwSearchAnimation.this.scrollListWithSearchView(oldTranslation, newTranslation, animation.getAnimatedFraction(), false);
                HwSearchAnimation.this.mSearchView.setTranslationY(newTranslation);
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(HwSearchAnimationUtils.getAppBarAnimator(this.mToolBar, searchHeight, (int) (-originalTranslationY), false), animatorOthers);
        procCancelAnimationListener(animatorSet);
        this.mIsGettingCancelAnimator = false;
        this.mCancelAnimator = animatorSet;
        return animatorSet;
    }

    /* access modifiers changed from: protected */
    public ViewGroup getHostViewGroup(View view) {
        ViewGroup viewGroup;
        ViewGroup viewGroup2;
        if (view == this.mSearchView && (viewGroup2 = this.mSearchHostBack) != null) {
            return viewGroup2;
        }
        if (view == this.mBackView && (viewGroup = this.mArrowHostBack) != null) {
            return viewGroup;
        }
        HwAppBarLayout hwAppBarLayout = this.mAppbar;
        if (hwAppBarLayout == null) {
            return null;
        }
        ViewParent appbarParent = hwAppBarLayout.getParent();
        ViewParent hostParent = appbarParent != null ? appbarParent.getParent() : null;
        if (hostParent instanceof ViewGroup) {
            return (ViewGroup) hostParent;
        }
        Log.e(TAG, "getHostViewGroup: parent is not a instance of ViewGroup");
        return null;
    }

    private ViewGroup bringToRootFront(View view, ViewGroup hostView) {
        ViewGroup.MarginLayoutParams marginLayoutParams;
        if (hostView == null || view == null) {
            Log.e(TAG, "bringToRootFront view is null");
            return null;
        }
        ViewParent parent = (ViewGroup) view.getParent();
        if (!(parent instanceof ViewGroup)) {
            Log.e(TAG, "bringToRootFront: view's parent is not ViewGroup");
            return null;
        }
        ViewGroup parentView = (ViewGroup) parent;
        if (hostView == parentView) {
            return null;
        }
        int[] parentLocation = new int[2];
        int[] hostViewLocation = new int[2];
        parentView.getLocationOnScreen(parentLocation);
        hostView.getLocationOnScreen(hostViewLocation);
        parentLocation[1] = hostViewLocation[1] + this.mAppbar.getHeight() + this.mAppbar.getTop();
        view.offsetLeftAndRight(parentLocation[0] - hostViewLocation[0]);
        view.offsetTopAndBottom(parentLocation[1] - hostViewLocation[1]);
        parentView.removeView(view);
        hostView.addView(view);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof ViewGroup.MarginLayoutParams)) {
            marginLayoutParams = new ViewGroup.MarginLayoutParams(params);
        } else {
            marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
        }
        marginLayoutParams.topMargin = view.getTop();
        view.setLayoutParams(marginLayoutParams);
        return parentView;
    }

    private void backToOldViewParent(View view, ViewGroup hostView) {
        ViewGroup.MarginLayoutParams marginLayoutParams;
        ViewGroup parent = (ViewGroup) view.getParent();
        if (hostView == null) {
            Log.e(TAG, "backToOldViewParent: mSearchHostBack is null.");
        } else if (parent != hostView) {
            int[] parentLocation = new int[2];
            int[] hostViewLocation = new int[2];
            parent.getLocationOnScreen(parentLocation);
            hostView.getLocationOnScreen(hostViewLocation);
            hostViewLocation[1] = parentLocation[1] + this.mAppbar.getHeight() + this.mAppbar.getTop();
            view.offsetLeftAndRight(parentLocation[0] - hostViewLocation[0]);
            view.offsetTopAndBottom(parentLocation[1] - hostViewLocation[1]);
            parent.removeView(view);
            hostView.addView(view);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (!(params instanceof ViewGroup.MarginLayoutParams)) {
                marginLayoutParams = new ViewGroup.MarginLayoutParams(params);
            } else {
                marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
            }
            marginLayoutParams.topMargin = 0;
            view.setLayoutParams(marginLayoutParams);
            hostView.requestLayout();
            hostView.invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void setBackViewPosition(float positionY, float translationY) {
        this.mBackView.setY(positionY);
        this.mBackView.setTranslationY(translationY);
    }

    @TargetApi(11)
    private void procBeforeOpenAnimation(View backView, View search) {
        this.mSearchHostBack = getHostViewGroup(search);
        this.mArrowHostBack = getHostViewGroup(backView);
        ViewGroup parent = bringToRootFront(search, this.mSearchHostBack);
        if (parent != null) {
            this.mSearchParentBack = parent;
        }
        ViewGroup parent2 = bringToRootFront(backView, this.mArrowHostBack);
        if (parent2 != null) {
            this.mArrowParentBack = parent2;
        }
    }

    private void procAfterCancelAnimation(View backView, View search) {
        search.setTranslationY(0.0f);
        backView.setTranslationY(0.0f);
        backToOldViewParent(backView, this.mArrowParentBack);
        backToOldViewParent(search, this.mSearchParentBack);
        int top = search.getTop();
        search.offsetTopAndBottom(-top);
        backView.offsetTopAndBottom(-top);
    }

    private int getSearchCurrentMarginStart() {
        int dstTranslationX;
        ViewGroup.LayoutParams params = this.mSearchView.getLayoutParams();
        if (!(params instanceof ViewGroup.MarginLayoutParams) || (dstTranslationX = ((ViewGroup.MarginLayoutParams) params).getMarginStart()) <= 0) {
            return 0;
        }
        return dstTranslationX;
    }

    /* access modifiers changed from: protected */
    public int getSearchMarginStart() {
        View view = this.mBackView;
        if (view == null) {
            Log.e(TAG, "getSearchMarginStart mBackView is null");
            return 0;
        }
        int marginStart = view.getLayoutParams().width;
        if (marginStart > 0) {
            return marginStart;
        }
        int marginStart2 = this.mBackView.getWidth();
        if (marginStart2 > 0) {
            return marginStart2;
        }
        View view2 = this.mBackView;
        int i = DEFAULT_MEASURE_SPEC;
        view2.measure(i, i);
        int marginStart3 = this.mBackView.getMeasuredWidth();
        if (marginStart3 > 0) {
            return marginStart3;
        }
        return 0;
    }

    private void getListScrollRangeAndBase(float searchHeight, boolean isOpen) {
        HwAppBarLayout hwAppBarLayout = this.mAppbar;
        if (hwAppBarLayout != null) {
            int appAppearHeight = hwAppBarLayout.getHeight() + this.mAppbar.getTop();
            if (((float) appAppearHeight) >= searchHeight) {
                this.mListScrollRange = searchHeight;
            } else {
                this.mListScrollRange = (float) appAppearHeight;
            }
        } else {
            this.mListScrollRange = (float) this.mToolBar.getMeasuredHeight();
        }
        if (isOpen) {
            this.mListScrollBase = 0.0f;
        } else {
            this.mListScrollBase = -this.mListScrollRange;
        }
    }

    private void setListPaddingTop(int paddingTop) {
        View view = this.mList;
        if (view != null && view.getPaddingTop() != paddingTop) {
            this.mList.setPadding(this.mList.getPaddingLeft(), paddingTop, this.mList.getPaddingRight(), this.mList.getPaddingBottom());
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

    /* access modifiers changed from: protected */
    public void scrollListWithSearchView(float oldTranslationY, float newTranslationY, float fraction, boolean isOpen) {
        View view = this.mList;
        if (view != null && !isActivityInactive(view)) {
            float searchHeight = (float) this.mSearchView.getHeight();
            if (fraction == 0.0f) {
                getListScrollRangeAndBase(searchHeight, isOpen);
            }
            if ((this.mList instanceof AbsListView) && this.mListScrollBase != newTranslationY) {
                float newTranslation = newTranslationY;
                if (!isOpen) {
                    float f = this.mListScrollRange;
                    if (newTranslation > (-f)) {
                        if (oldTranslationY < (-f)) {
                            newTranslation = -f;
                        }
                    } else {
                        return;
                    }
                } else if (newTranslation > oldTranslationY || oldTranslationY > (-searchHeight)) {
                    float f2 = this.mListScrollRange;
                    if (newTranslation < (-f2)) {
                        newTranslation = -f2;
                    }
                } else {
                    return;
                }
                int paddingTop = this.mList.getPaddingTop();
                if (!isOpen && this.mList.getPaddingTop() == 0 && canScrollDown((AbsListView) this.mList)) {
                    setListPaddingTop((int) searchHeight);
                }
                float f3 = this.mListScrollBase;
                int delta = (int) (newTranslation - f3);
                if (delta != 0) {
                    this.mListScrollBase = f3 + ((float) delta);
                    scrollListView((AbsListView) this.mList, paddingTop, delta, searchHeight);
                }
            }
        }
    }

    private boolean canScrollDown(AbsListView listView) {
        View child;
        Adapter adapter = listView.getAdapter();
        boolean canScrollDown = false;
        if (adapter == null) {
            return false;
        }
        int count = listView.getChildCount();
        boolean canScrollDown2 = listView.getFirstVisiblePosition() + count < adapter.getCount();
        if (canScrollDown2 || count <= 0 || (child = listView.getChildAt(count - 1)) == null) {
            return canScrollDown2;
        }
        if (child.getBottom() > listView.getBottom() - listView.getPaddingBottom()) {
            canScrollDown = true;
        }
        return canScrollDown;
    }

    private void scrollListView(final AbsListView listView, int oldPaddingTop, final int delta, final float searchHeight) {
        if (oldPaddingTop >= this.mList.getPaddingTop() || delta <= oldPaddingTop) {
            scrollListView(listView, delta, searchHeight);
        } else {
            this.mList.post(new Runnable() {
                /* class huawei.android.view.HwSearchAnimation.AnonymousClass3 */

                public void run() {
                    HwSearchAnimation.this.scrollListView(listView, delta, searchHeight);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scrollListView(AbsListView list, int delta) {
        Adapter adapter = list.getAdapter();
        if (adapter != null) {
            int first = list.getFirstVisiblePosition();
            int childCount = list.getChildCount();
            if (first >= 0 && first + childCount <= adapter.getCount()) {
                list.scrollListBy(-delta);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scrollListView(final AbsListView list, int delta, float searchHeight) {
        int remain;
        View currentView = getCurrentView(list, delta);
        int lastPos = getCurrentPosition(currentView, delta);
        scrollListView(list, delta);
        int newPos = getCurrentPosition(currentView, delta);
        if (newPos != Integer.MIN_VALUE && newPos - lastPos != delta && (remain = (lastPos - newPos) + delta) != 0) {
            int newPaddingTop = list.getPaddingTop() + remain;
            if (delta < 0) {
                int topPos = getFirstItemViewTop(list);
                if (topPos != Integer.MIN_VALUE && topPos < list.getPaddingTop()) {
                    setListPaddingTop(topPos);
                    newPaddingTop = topPos + remain;
                }
                if (newPaddingTop <= 0) {
                    newPaddingTop = 0;
                }
            } else if (((float) newPaddingTop) >= searchHeight) {
                newPaddingTop = (int) searchHeight;
            }
            final int real = newPaddingTop - list.getPaddingTop();
            setListPaddingTop(newPaddingTop);
            if (delta >= 0) {
                list.post(new Runnable() {
                    /* class huawei.android.view.HwSearchAnimation.AnonymousClass4 */

                    public void run() {
                        HwSearchAnimation.this.scrollListView(list, real);
                    }
                });
            }
        }
    }

    private View getCurrentView(ViewGroup list, int delta) {
        int count = list.getChildCount();
        if (count <= 0) {
            return null;
        }
        if (delta < 0) {
            return list.getChildAt(count - 1);
        }
        return list.getChildAt(0);
    }

    private int getCurrentPosition(View view, int delta) {
        if (view == null) {
            return Integer.MIN_VALUE;
        }
        if (delta < 0) {
            return view.getBottom();
        }
        return view.getTop();
    }

    private int getFirstItemViewTop(AbsListView listView) {
        View child;
        if (listView.getFirstVisiblePosition() == 0 && listView.getChildCount() > 0 && (child = listView.getChildAt(0)) != null) {
            return child.getTop();
        }
        return Integer.MIN_VALUE;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean procSearchViewCollapsed(int searchOriginalH, float searchTranslationY) {
        if (this.mSearchView.getTranslationY() > ((float) (-searchOriginalH))) {
            this.mSearchView.setTranslationY(searchTranslationY);
            if (this.mSearchView.getTranslationY() > ((float) (-searchOriginalH))) {
                return false;
            }
            this.mSearchView.setTranslationY((float) (-searchOriginalH));
            return true;
        }
        this.mSearchView.setTranslationY(searchTranslationY);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void procSearchViewExpanded(float searchTranslationY, float oldSearchTranslationY) {
        View view = this.mSearchView;
        view.setTranslationY(view.getTranslationY() + (searchTranslationY - oldSearchTranslationY));
    }

    private void scrollBarProcess(View list, boolean isOpen, boolean isStart) {
        if (this.mList instanceof AbsListView) {
            AbsListView absListView = (AbsListView) list;
            if (isStart) {
                this.mIsVerticalScroll = absListView.isVerticalScrollBarEnabled();
                if (this.mIsVerticalScroll) {
                    absListView.setVerticalScrollBarEnabled(false);
                }
                this.mIsHorizontalScroll = absListView.isHorizontalScrollBarEnabled();
                if (this.mIsHorizontalScroll) {
                    absListView.setHorizontalScrollBarEnabled(false);
                }
                this.mIsFastScroll = absListView.isFastScrollEnabled();
                if (this.mIsHorizontalScroll) {
                    absListView.setFastScrollEnabled(false);
                    return;
                }
                return;
            }
            if (this.mIsVerticalScroll) {
                absListView.setVerticalScrollBarEnabled(true);
            }
            if (this.mIsHorizontalScroll) {
                absListView.setHorizontalScrollBarEnabled(true);
            }
            if (this.mIsFastScroll) {
                absListView.setFastScrollEnabled(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    @TargetApi(11)
    public void onOpenAnimationStart() {
        scrollBarProcess(this.mList, true, true);
        this.mBackView.setVisibility(0);
        if (this.mAppbar != null) {
            procBeforeOpenAnimation(this.mBackView, this.mSearchView);
        }
        View view = this.mSearchView;
        if ((view instanceof SearchView) && !view.hasFocus()) {
            this.mSearchView.requestFocus();
        }
    }

    /* access modifiers changed from: protected */
    public void onOpenAnimationEnd() {
        scrollBarProcess(this.mList, true, false);
        View view = this.mList;
        if (view != null && view.getPaddingTop() != 0) {
            setListPaddingTop(0);
        }
    }

    @TargetApi(11)
    private void procOpenAnimationListener(Animator animator) {
        if (this.mAppbar != null) {
            initAppbarInfo();
        }
        animator.addListener(new AnimatorListenerAdapter() {
            /* class huawei.android.view.HwSearchAnimation.AnonymousClass5 */

            public void onAnimationStart(Animator animation) {
                if (HwSearchAnimation.this.mAppbar != null) {
                    HwSearchAnimation.this.mAppbarCtrl.setPermitCollapse(false);
                }
                HwSearchAnimation.this.onOpenAnimationStart();
                HwSearchAnimationUtils.setSearchBackGroundColor(HwSearchAnimation.this.mSearchView, true);
            }

            public void onAnimationEnd(Animator animation) {
                HwSearchAnimation.this.onOpenAnimationEnd();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onCancelAnimationStart() {
        scrollBarProcess(this.mList, false, true);
    }

    /* access modifiers changed from: protected */
    public void onCancelAnimationEnd() {
        this.mBackView.setVisibility(4);
        this.mSearchView.clearFocus();
        ViewGroup.LayoutParams params = this.mSearchView.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) params).setMarginStart(0);
            this.mSearchView.setLayoutParams(params);
        }
        scrollBarProcess(this.mList, false, false);
        View view = this.mList;
        if (!(view == null || view.getPaddingTop() == this.mSearchView.getHeight())) {
            setListPaddingTop(this.mSearchView.getHeight());
        }
        if (this.mAppbar != null) {
            procAfterCancelAnimation(this.mBackView, this.mSearchView);
        }
    }

    @TargetApi(11)
    private void procCancelAnimationListener(Animator animator) {
        animator.addListener(new AnimatorListenerAdapter() {
            /* class huawei.android.view.HwSearchAnimation.AnonymousClass6 */

            public void onAnimationStart(Animator animation) {
                if (HwSearchAnimation.this.mAppbar != null) {
                    HwSearchAnimation.this.mAppbarCtrl.setPermitCollapse(false);
                    HwSearchAnimationUtils.setAppbarAlpha(HwSearchAnimation.this.mAppbar, 0.0f);
                }
                HwSearchAnimation.this.onCancelAnimationStart();
                HwSearchAnimationUtils.setSearchBackGroundColor(HwSearchAnimation.this.mSearchView, true);
            }

            public void onAnimationEnd(Animator animation) {
                if (HwSearchAnimation.this.mAppbar != null) {
                    HwSearchAnimation.this.mAppbarCtrl.setPermitCollapse(true);
                    HwSearchAnimationUtils.setAppbarAlpha(HwSearchAnimation.this.mAppbar, 1.0f);
                } else {
                    HwSearchAnimationUtils.setAppbarAlpha(HwSearchAnimation.this.mToolBar, 1.0f);
                }
                HwSearchAnimation.this.onCancelAnimationEnd();
                HwSearchAnimationUtils.setSearchBackGroundColor(HwSearchAnimation.this.mSearchView, false);
            }
        });
    }

    private boolean isMatchAnimationCondition(boolean isOpen) {
        ViewGroup.LayoutParams params = this.mSearchView.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParams = null;
        if (params instanceof ViewGroup.MarginLayoutParams) {
            marginParams = (ViewGroup.MarginLayoutParams) params;
        }
        if (this.mSearchView.getHeight() <= 0) {
            return false;
        }
        if (isOpen) {
            if (marginParams == null || marginParams.getMarginStart() == 0) {
                return true;
            }
            return false;
        } else if (this.mAppbarOriginalHeight <= 0) {
            return false;
        } else {
            if (marginParams != null && marginParams.getMarginStart() != 0) {
                return true;
            }
            this.mSearchView.clearFocus();
            return false;
        }
    }

    private boolean isRunning() {
        if (this.mIsGettingOpenAnimator || this.mIsGettingCancelAnimator) {
            return true;
        }
        Animator animator = this.mOpenAnimator;
        if (animator != null && animator.isRunning()) {
            return true;
        }
        Animator animator2 = this.mCancelAnimator;
        if (animator2 == null || !animator2.isRunning()) {
            return false;
        }
        return true;
    }

    public Animator getAnimator(boolean isOpen) {
        if (!this.mIsInited) {
            Log.e(TAG, "getAnimator: has not initialized.");
            return null;
        } else if (isRunning()) {
            Log.w(TAG, "getAnimator: isRunning.");
            return null;
        } else if (this.mAppbar != null) {
            if (isOpen) {
                return getBelowAppBarOpenAnimator();
            }
            return getBelowAppBarCancelAnimator();
        } else if (isOpen) {
            return getBelowToolbarOpenAnimator();
        } else {
            return getBelowToolbarCancelAnimator();
        }
    }

    private void setBackViewBeforeOpenAnimator(int dstTranslationY) {
        setBackViewPosition(this.mSearchView.getY(), (float) dstTranslationY);
        this.mBackView.setAlpha(0.0f);
        this.mBackView.setVisibility(0);
    }

    @TargetApi(11)
    private Animator getBelowAppBarOpenAnimator() {
        this.mIsGettingOpenAnimator = true;
        if (!this.mIsInited || !isMatchAnimationCondition(true)) {
            Log.e(TAG, "getBelowAppBarOpenAnimator: input params is illegal.");
            this.mIsGettingOpenAnimator = false;
            return null;
        }
        final int dstTranslationY = -(this.mAppbar.getHeight() + this.mAppbar.getTop());
        setBackViewBeforeOpenAnimator(dstTranslationY);
        final int dstMarginStart = getSearchMarginStart();
        final int searchHeight = this.mSearchView.getHeight();
        if (!HwSearchAnimationUtils.checkAnimatorInput(dstMarginStart, searchHeight, dstTranslationY)) {
            this.mIsGettingOpenAnimator = false;
            return null;
        }
        ValueAnimator animatorOthers = HwSearchAnimationUtils.getWholeTimeAnimator(this.mSearchView, searchHeight, -dstTranslationY, true);
        animatorOthers.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.view.HwSearchAnimation.AnonymousClass7 */
            private int mLastOffset = 0;

            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                HwSearchAnimationUtils.procSearchWholeTimeAnimation(HwSearchAnimation.this.mSearchView, ((float) dstMarginStart) * value);
                HwSearchAnimationUtils.procWholeTimeAlphaAnimation(HwSearchAnimation.this.mMaskView, HwSearchAnimation.this.mBackView, value);
                float currentTranslation = ((float) dstTranslationY) * value;
                float oldTranslation = HwSearchAnimation.this.mSearchView.getTranslationY();
                float fraction = animation.getAnimatedFraction();
                if (!HwSearchAnimation.this.procSearchViewCollapsed(searchHeight, currentTranslation)) {
                    HwSearchAnimation hwSearchAnimation = HwSearchAnimation.this;
                    hwSearchAnimation.scrollListWithSearchView(oldTranslation, hwSearchAnimation.mSearchView.getTranslationY(), fraction, true);
                    return;
                }
                HwSearchAnimation hwSearchAnimation2 = HwSearchAnimation.this;
                hwSearchAnimation2.scrollListWithSearchView(oldTranslation, hwSearchAnimation2.mSearchView.getTranslationY(), fraction, true);
                if (!HwSearchAnimation.this.mAppbar.isCollapsed()) {
                    int offsetY = (int) (-(((float) searchHeight) + currentTranslation));
                    HwSearchAnimation.this.mAppbar.setExpandedLayoutY(offsetY - this.mLastOffset);
                    this.mLastOffset = offsetY;
                }
            }
        });
        AnimatorSet animatorSetTmps = new AnimatorSet();
        animatorSetTmps.playTogether(HwSearchAnimationUtils.getAppBarAnimator(this.mAppbar, searchHeight, -dstTranslationY, true), animatorOthers);
        procOpenAnimationListener(animatorSetTmps);
        this.mIsGettingOpenAnimator = false;
        this.mOpenAnimator = animatorSetTmps;
        return animatorSetTmps;
    }

    @TargetApi(11)
    private Animator getBelowAppBarCancelAnimator() {
        this.mIsGettingCancelAnimator = true;
        initAppbarInfo();
        if (!this.mIsInited || !isMatchAnimationCondition(false)) {
            Log.e(TAG, "getBelowAppBarCancelAnimator: input params is illegal.");
            this.mIsGettingCancelAnimator = false;
            return null;
        }
        final int originalMarginStart = getSearchCurrentMarginStart();
        int searchHeight = this.mSearchView.getHeight();
        final int appAppearHeight = this.mAppbar.getHeight() + this.mAppbar.getTop();
        final float dstTranslationY = (float) this.mAppbarOriginalHeight;
        ValueAnimator animatorOthers = HwSearchAnimationUtils.getWholeTimeAnimator(this.mSearchView, searchHeight, (int) dstTranslationY, false);
        animatorOthers.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.view.HwSearchAnimation.AnonymousClass8 */
            private boolean isSearchViewCollapsed = true;
            private int lastOffset = (-HwSearchAnimation.this.mAppbar.getTop());
            private float oldSearchTranslation = (-dstTranslationY);

            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                HwSearchAnimationUtils.procWholeTimeAlphaAnimation(HwSearchAnimation.this.mMaskView, HwSearchAnimation.this.mBackView, value);
                HwSearchAnimationUtils.procSearchWholeTimeAnimation(HwSearchAnimation.this.mSearchView, ((float) originalMarginStart) * value);
                float searchTranslation = (-value) * dstTranslationY;
                if (this.isSearchViewCollapsed) {
                    int i = appAppearHeight;
                    int offsetY = ((int) (-searchTranslation)) - i;
                    if (offsetY <= 0) {
                        offsetY = 0;
                        this.isSearchViewCollapsed = false;
                        searchTranslation = (float) (-i);
                    }
                    HwSearchAnimation.this.mAppbar.setExpandedLayoutY(offsetY - this.lastOffset);
                    this.lastOffset = offsetY;
                }
                float fraction = animation.getAnimatedFraction();
                HwSearchAnimation.this.procSearchViewExpanded(searchTranslation, this.oldSearchTranslation);
                HwSearchAnimation.this.scrollListWithSearchView(this.oldSearchTranslation, searchTranslation, fraction, false);
                this.oldSearchTranslation = searchTranslation;
            }
        });
        AnimatorSet animatorSetTmps = new AnimatorSet();
        animatorSetTmps.playTogether(HwSearchAnimationUtils.getAppBarAnimator(this.mAppbar, searchHeight, (int) dstTranslationY, false), animatorOthers);
        procCancelAnimationListener(animatorSetTmps);
        this.mIsGettingCancelAnimator = false;
        this.mCancelAnimator = animatorSetTmps;
        return animatorSetTmps;
    }

    private void initAppbarInfo() {
        HwAppBarLayout hwAppBarLayout = this.mAppbar;
        if (hwAppBarLayout == null || !this.mIsInited) {
            Log.e(TAG, "initAppbarInfo: instantiation is failed.");
        } else if (this.mAppbarOriginalHeight == 0) {
            this.mAppbarOriginalHeight = hwAppBarLayout.getHeight();
        }
    }

    private void translateToOpenStatusForAppbar(float currentTranslation) {
        int dstTranslationY;
        this.mIsGettingOpenAnimator = true;
        int searchHeight = this.mSearchView.getHeight();
        int appbarVisibleH = this.mAppbar.getHeight() + this.mAppbar.getTop();
        if (currentTranslation > ((float) (-searchHeight))) {
            dstTranslationY = -appbarVisibleH;
        } else {
            dstTranslationY = ((int) currentTranslation) - (appbarVisibleH - searchHeight);
        }
        setBackViewBeforeOpenAnimator(dstTranslationY);
        this.mAppbarCtrl.setPermitCollapse(false);
        onOpenAnimationStart();
        HwSearchAnimationUtils.setSearchBackGroundColor(this.mSearchView, true);
        scrollListWithSearchView(currentTranslation, (float) (-searchHeight), 1.0f, true);
        HwSearchAnimationUtils.procSearchWholeTimeAnimation(this.mSearchView, (float) getSearchMarginStart());
        this.mSearchView.setTranslationY((float) dstTranslationY);
        HwSearchAnimationUtils.procWholeTimeAlphaAnimation(this.mMaskView, this.mBackView, 1.0f);
        this.mAppbar.setInnerAlpha(0.0f);
        this.mAppbar.setExpandedLayoutY(appbarVisibleH - searchHeight);
        this.mAppbarCtrl.setPermitCollapse(false);
        onOpenAnimationEnd();
        this.mIsGettingOpenAnimator = false;
    }

    private void translateToCancelStatusForAppbar(float currentTranslation) {
        this.mIsGettingCancelAnimator = true;
        onCancelAnimationStart();
        HwSearchAnimationUtils.procWholeTimeAlphaAnimation(this.mMaskView, this.mBackView, 0.0f);
        HwSearchAnimationUtils.procSearchWholeTimeAnimation(this.mSearchView, 0.0f);
        if (currentTranslation != 0.0f) {
            getListScrollRangeAndBase((float) this.mSearchView.getHeight(), false);
            float f = this.mListScrollBase;
            if (currentTranslation < f) {
                scrollListWithSearchView(f, 0.0f, 0.0f, false);
            } else {
                scrollListWithSearchView(currentTranslation, f - currentTranslation, 0.0f, false);
            }
        }
        this.mSearchView.setTranslationY(0.0f);
        this.mAppbarCtrl.setExpanded(true, false);
        this.mAppbar.setInnerAlpha(1.0f);
        this.mAppbarCtrl.setPermitCollapse(true);
        onCancelAnimationEnd();
        HwSearchAnimationUtils.setSearchBackGroundColor(this.mSearchView, false);
        this.mIsGettingCancelAnimator = false;
    }

    private void translateToOpenStatusForToolbar() {
        this.mIsGettingOpenAnimator = true;
        Toolbar toolbar = this.mToolBar;
        int i = DEFAULT_MEASURE_SPEC;
        toolbar.measure(i, i);
        int dstTranslationY = -this.mToolBar.getMeasuredHeight();
        setBackViewBeforeOpenAnimator(dstTranslationY);
        onOpenAnimationStart();
        HwSearchAnimationUtils.setSearchBackGroundColor(this.mSearchView, true);
        scrollListWithSearchView(this.mSearchView.getTranslationY(), (float) dstTranslationY, 1.0f, true);
        HwSearchAnimationUtils.procSearchWholeTimeAnimation(this.mSearchView, (float) getSearchMarginStart());
        this.mSearchView.setTranslationY((float) dstTranslationY);
        HwSearchAnimationUtils.procWholeTimeAlphaAnimation(this.mMaskView, this.mBackView, 1.0f);
        Toolbar toolbar2 = this.mToolBar;
        if (toolbar2 instanceof HwToolbar) {
            ((HwToolbar) toolbar2).setInnerAlpha(0.0f);
        } else {
            toolbar2.setAlpha(0.0f);
        }
        onOpenAnimationEnd();
        this.mIsGettingOpenAnimator = false;
    }

    private void translateToCancelStatusForToolbar() {
        this.mIsGettingCancelAnimator = true;
        onCancelAnimationStart();
        HwSearchAnimationUtils.procWholeTimeAlphaAnimation(this.mMaskView, this.mBackView, 0.0f);
        HwSearchAnimationUtils.procSearchWholeTimeAnimation(this.mSearchView, 0.0f);
        scrollListWithSearchView(this.mSearchView.getTranslationY(), 0.0f, 0.0f, false);
        this.mSearchView.setTranslationY(0.0f);
        Toolbar toolbar = this.mToolBar;
        if (toolbar instanceof HwToolbar) {
            ((HwToolbar) toolbar).setInnerAlpha(1.0f);
        } else {
            toolbar.setAlpha(1.0f);
        }
        HwSearchAnimationUtils.setSearchBackGroundColor(this.mSearchView, false);
        onCancelAnimationEnd();
        this.mIsGettingCancelAnimator = false;
    }

    public void setStatusWithoutAnimator(boolean isOpen) {
        if (this.mIsInited) {
            float currentTranslation = this.mSearchView.getTranslationY();
            Animator animator = this.mOpenAnimator;
            if (animator != null && animator.isRunning()) {
                this.mOpenAnimator.cancel();
            }
            Animator animator2 = this.mCancelAnimator;
            if (animator2 != null && animator2.isRunning()) {
                this.mCancelAnimator.cancel();
            }
            if (this.mAppbar != null) {
                if (isOpen) {
                    translateToOpenStatusForAppbar(currentTranslation);
                } else {
                    translateToCancelStatusForAppbar(currentTranslation);
                }
            }
            if (this.mToolBar == null) {
                return;
            }
            if (isOpen) {
                translateToOpenStatusForToolbar();
            } else {
                translateToCancelStatusForToolbar();
            }
        }
    }

    public void onConfigurationChanged() {
        if (this.mIsInited) {
            boolean isOpen = false;
            Animator animator = this.mOpenAnimator;
            if (animator != null && animator.isRunning()) {
                isOpen = true;
            }
            if (!isOpen) {
                ViewGroup.LayoutParams params = this.mSearchView.getLayoutParams();
                if ((params instanceof ViewGroup.MarginLayoutParams) && ((ViewGroup.MarginLayoutParams) params).getMarginStart() != 0) {
                    isOpen = true;
                }
            }
            Animator animator2 = this.mCancelAnimator;
            if (animator2 != null && animator2.isRunning()) {
                isOpen = false;
            }
            setStatusWithoutAnimator(isOpen);
        }
    }
}
