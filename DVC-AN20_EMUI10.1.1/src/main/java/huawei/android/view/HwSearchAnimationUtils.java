package huawei.android.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import huawei.android.widget.HwToolbar;
import huawei.android.widget.SearchView;
import huawei.android.widget.appbar.HwAppBarLayout;
import huawei.android.widget.appbar.HwExpandedAppbarController;
import huawei.android.widget.loader.ResLoader;

public class HwSearchAnimationUtils {
    private static final int ANIMATION_TIME_DURATION = 50;
    private static final int ANIMATOR_TIME_LONG = 150;
    private static final int ANIMATOR_TIME_SHORT = 100;
    private static final float MASK_VIEW_TARGET_ALPHA = 0.2f;
    private static final int SEARCH_FIXED_ANIMATION_OFFSET = 50;
    private static final int SEARCH_FIXED_ENTER_DURATION = 250;
    private static final int SEARCH_FIXED_EXIT_DURATION = 100;
    private static final int SEARCH_FIXED_TARGET_TRANSLATION_DP = 30;
    private static final String TAG = "HwSearchAnimationUtils";
    private static int mAppbarOriginalHeight = 0;
    private static int mAppbarScrollRange = 0;

    private HwSearchAnimationUtils() {
    }

    public static Animator getSearchFixedOpenAnimator(Context context, View maskView) {
        if (context == null || maskView == null) {
            Log.e(TAG, "getSearchFixedOpenAnimator parameter is wrong.");
            return null;
        }
        int resId = ResLoader.getInstance().getIdentifier(context, "animator", "search_mask");
        ObjectAnimator objectAnimator = null;
        if (resId != 0) {
            objectAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, resId);
            if (objectAnimator != null) {
                objectAnimator.setTarget(maskView);
            }
        } else {
            Log.w(TAG, "search_mask animator file not found");
        }
        return objectAnimator;
    }

    public static Animator getSearchFixedCloseAnimator(Context context, View maskView) {
        if (context == null || maskView == null) {
            Log.e(TAG, "getSearchFixedCloseAnimator parameter is wrong.");
            return null;
        }
        int resId = ResLoader.getInstance().getIdentifier(context, "animator", "search_unmask");
        ObjectAnimator objectAnimator = null;
        if (resId != 0) {
            objectAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, resId);
            if (objectAnimator != null) {
                objectAnimator.setTarget(maskView);
            }
        } else {
            Log.w(TAG, "search_unmask animator file not found");
        }
        return objectAnimator;
    }

    private static int dip2px(int dp) {
        return (int) TypedValue.applyDimension(1, (float) dp, Resources.getSystem().getDisplayMetrics());
    }

    @TargetApi(11)
    private static AnimatorSet getEnterAnimator(View target, boolean isOpen) {
        ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(target, "alpha", 0.0f, 1.0f);
        alphaAnimation.setDuration(250L);
        alphaAnimation.setStartDelay(50);
        alphaAnimation.setInterpolator(AnimationUtils.loadInterpolator(target.getContext(), 17563661));
        float valueFrom = (float) dip2px(SEARCH_FIXED_TARGET_TRANSLATION_DP);
        if (!isOpen) {
            valueFrom = -valueFrom;
        }
        ObjectAnimator translation = ObjectAnimator.ofFloat(target, "translationY", valueFrom, 0.0f);
        translation.setDuration(250L);
        translation.setStartDelay(50);
        translation.setInterpolator(AnimationUtils.loadInterpolator(target.getContext(), 17563661));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(alphaAnimation).with(translation);
        return animatorSet;
    }

    @TargetApi(11)
    private static AnimatorSet getExitAnimator(View target, boolean isOpen) {
        ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(target, "alpha", 1.0f, 0.0f);
        alphaAnimation.setDuration(100L);
        alphaAnimation.setInterpolator(AnimationUtils.loadInterpolator(target.getContext(), 17563661));
        float valueTo = (float) dip2px(SEARCH_FIXED_TARGET_TRANSLATION_DP);
        if (isOpen) {
            valueTo = -valueTo;
        }
        ObjectAnimator translation = ObjectAnimator.ofFloat(target, "translationY", 0.0f, valueTo);
        translation.setDuration(100L);
        translation.setInterpolator(AnimationUtils.loadInterpolator(target.getContext(), 17563661));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(alphaAnimation).with(translation);
        return animatorSet;
    }

    @TargetApi(11)
    public static Animator getSearchBoxAlwaysOnAppbarOpenAnimator(Context context, final View enterView, final View exitView) {
        if (enterView == null || context == null || exitView == null) {
            Log.e(TAG, "input parameters invalid.");
            return null;
        } else if (exitView.getAlpha() < 1.0f) {
            Log.e(TAG, "exit view's alpha is not 1, last animation has not end?");
            return null;
        } else {
            AnimatorSet openEnterAnimator = getEnterAnimator(enterView, true);
            AnimatorSet openExitAnimator = getExitAnimator(exitView, true);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(openEnterAnimator, openExitAnimator);
            openExitAnimator.addListener(new AnimatorListenerAdapter() {
                /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass1 */

                public void onAnimationEnd(Animator animation) {
                    exitView.setVisibility(4);
                }
            });
            openEnterAnimator.addListener(new AnimatorListenerAdapter() {
                /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass2 */

                public void onAnimationStart(Animator animation) {
                    enterView.setAlpha(0.0f);
                    enterView.setVisibility(0);
                }
            });
            return animatorSet;
        }
    }

    @TargetApi(11)
    public static Animator getSearchBoxAlwaysOnAppbarCloseAnimator(Context context, final View enterView, final View exitView) {
        if (enterView == null || context == null || exitView == null) {
            Log.e(TAG, "input parameters invalid.");
            return null;
        } else if (exitView.getAlpha() < 1.0f) {
            Log.e(TAG, "exit view's alpha is not 1, last animation has not end?");
            return null;
        } else {
            AnimatorSet closeEnterAnimator = getEnterAnimator(enterView, false);
            AnimatorSet closeExitAnimator = getExitAnimator(exitView, false);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(closeEnterAnimator, closeExitAnimator);
            closeEnterAnimator.addListener(new AnimatorListenerAdapter() {
                /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass3 */

                public void onAnimationStart(Animator animation) {
                    enterView.setAlpha(0.0f);
                    enterView.setVisibility(0);
                }
            });
            closeExitAnimator.addListener(new AnimatorListenerAdapter() {
                /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass4 */

                public void onAnimationEnd(Animator animation) {
                    exitView.setVisibility(4);
                }
            });
            return animatorSet;
        }
    }

    @TargetApi(14)
    public static MenuItem.OnActionExpandListener getAlphaAnimatedOnActionExpandListener() {
        return new MenuItem.OnActionExpandListener() {
            /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass5 */

            public boolean onMenuItemActionExpand(MenuItem item) {
                if (item == null) {
                    Log.w(HwSearchAnimationUtils.TAG, "MenuItem is null");
                    return false;
                }
                View actionView = item.getActionView();
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                alphaAnimation.setDuration(50);
                if (actionView == null) {
                    return true;
                }
                alphaAnimation.setInterpolator(AnimationUtils.loadInterpolator(actionView.getContext(), 17563661));
                actionView.startAnimation(alphaAnimation);
                return true;
            }

            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (item == null) {
                    Log.w(HwSearchAnimationUtils.TAG, "MenuItem is null");
                    return false;
                }
                View actionView = item.getActionView();
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                alphaAnimation.setDuration(50);
                if (actionView == null) {
                    return true;
                }
                alphaAnimation.setInterpolator(AnimationUtils.loadInterpolator(actionView.getContext(), 17563661));
                actionView.startAnimation(alphaAnimation);
                return true;
            }
        };
    }

    private static boolean isParametersValid(HwToolbar toolbar, View search, View mask, View holder, View tmpCollapseBtn) {
        if (toolbar == null || search == null || mask == null || holder == null || tmpCollapseBtn == null) {
            return false;
        }
        return true;
    }

    @TargetApi(11)
    public static Animator getSearchBelowActionBarSearchAnimator(Context context, MenuItem searchMenuItem, final HwToolbar toolbar, final View search, final View mask, final View holder, final View tmpCollapseBtn) {
        if (!isParametersValid(toolbar, search, mask, holder, tmpCollapseBtn)) {
            Log.e(TAG, "getSearchBelowActionBarSearchAnimator: paramenters ara invalid.");
            return null;
        }
        int searchHeight = search.getHeight();
        if (search.getWidth() != toolbar.getWidth() || searchHeight <= 0) {
            Log.e(TAG, "getSearchBelowActionBarSearchAnimator: search's width is not equal to toolbar's. searchHeight " + searchHeight);
            return null;
        }
        final int dstMarginStart = getSearchMarginStart(tmpCollapseBtn, search);
        if (dstMarginStart == 0) {
            Log.e(TAG, "getSearchBelowActionBarSearchAnimator: fail to get search view's target translationX.");
            return null;
        }
        boolean isRtl = toolbar.getLayoutDirection() == 1;
        float x = toolbar.getX();
        if (isRtl) {
            x = (x + ((float) toolbar.getWidth())) - ((float) tmpCollapseBtn.getWidth());
        }
        tmpCollapseBtn.setX(x);
        tmpCollapseBtn.setY(toolbar.getY());
        tmpCollapseBtn.setAlpha(0.0f);
        tmpCollapseBtn.setVisibility(0);
        ValueAnimator animatorOthers = getWholeTimeAnimator(search, searchHeight, searchHeight, true);
        animatorOthers.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass6 */
            float dstTranslationY = ((float) (-HwToolbar.this.getHeight()));

            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                int newHeight = HwToolbar.this.getHeight();
                if (newHeight > 0 && newHeight != ((int) (-this.dstTranslationY))) {
                    this.dstTranslationY = (float) (-newHeight);
                }
                HwSearchAnimationUtils.procSearchWholeTimeAnimation(search, ((float) dstMarginStart) * value);
                HwSearchAnimationUtils.procWholeTimeAlphaAnimation(mask, tmpCollapseBtn, value);
                search.setTranslationY(this.dstTranslationY * value);
                holder.getLayoutParams().height = search.getHeight() + ((int) (this.dstTranslationY * value));
                HwSearchAnimationUtils.requestLayoutTogether(search, holder, null, null);
            }
        });
        AnimatorSet animatorSetTmps = new AnimatorSet();
        animatorSetTmps.playTogether(getAppBarAnimator(toolbar, searchHeight, searchHeight, true), animatorOthers);
        procAnimatorListenerForToolbar(animatorSetTmps, toolbar, search, tmpCollapseBtn, true);
        return animatorSetTmps;
    }

    @TargetApi(11)
    private static void procAnimatorListenerForToolbar(Animator animator, final View appbar, final View search, final View backButton, final boolean isOpen) {
        animator.addListener(new AnimatorListenerAdapter() {
            /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass7 */

            public void onAnimationStart(Animator animation) {
                if (isOpen) {
                    HwSearchAnimationUtils.setSearchBackGroundColor(search, true);
                }
            }

            public void onAnimationEnd(Animator animation) {
                if (!isOpen) {
                    backButton.setVisibility(4);
                    search.clearFocus();
                    ViewGroup.LayoutParams params = search.getLayoutParams();
                    if (params instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) params).setMarginStart(0);
                    }
                    HwSearchAnimationUtils.setAppbarAlpha(appbar, 1.0f);
                    HwSearchAnimationUtils.setSearchBackGroundColor(search, false);
                }
            }
        });
    }

    @TargetApi(11)
    public static Animator getSearchBelowActionBarCancelAnimator(Context context, HwToolbar toolbar, final View search, final View mask, final View holder, final View tmpCollapseBtn) {
        if (!isParametersValid(toolbar, search, mask, holder, tmpCollapseBtn)) {
            Log.e(TAG, "getSearchBelowActionBarCancelAnimator: paramenters ara invalid.");
            return null;
        }
        final int searchHeight = search.getHeight();
        if (((int) search.getTranslationY()) != (-toolbar.getHeight()) || searchHeight <= 0) {
            search.clearFocus();
            Log.e(TAG, "getSearchBelowActionBarCancelAnimator: SearchView's translation is not match the condition.");
            return null;
        }
        final int originalMarginStart = getSearchMarginStart(tmpCollapseBtn, search);
        final float originalTranslationY = search.getTranslationY();
        ValueAnimator animatorOthers = getWholeTimeAnimator(search, searchHeight, (int) (-originalTranslationY), false);
        animatorOthers.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass8 */

            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                HwSearchAnimationUtils.procWholeTimeAlphaAnimation(mask, tmpCollapseBtn, value);
                HwSearchAnimationUtils.procSearchWholeTimeAnimation(search, ((float) originalMarginStart) * value);
                float translation = originalTranslationY * value;
                search.setTranslationY(translation);
                holder.getLayoutParams().height = searchHeight + ((int) translation);
                HwSearchAnimationUtils.requestLayoutTogether(search, holder, null, null);
            }
        });
        AnimatorSet animatorSetTmps = new AnimatorSet();
        animatorSetTmps.playTogether(getAppBarAnimator(toolbar, searchHeight, (int) (-originalTranslationY), false), animatorOthers);
        procAnimatorListenerForToolbar(animatorSetTmps, toolbar, search, tmpCollapseBtn, false);
        return animatorSetTmps;
    }

    public static class AnimationParams {
        private View mBackButton;
        private View mHolder;
        private View mMask;
        private View mSearchView;

        public AnimationParams(View searchView, View listMask, View holder, View backButton) {
            this.mSearchView = searchView;
            this.mMask = listMask;
            this.mHolder = holder;
            this.mBackButton = backButton;
        }

        public View getSearchView() {
            return this.mSearchView;
        }

        public void setSearchView(View searchView) {
            this.mSearchView = searchView;
        }

        public View getMask() {
            return this.mMask;
        }

        public void setMask(View listMask) {
            this.mMask = listMask;
        }

        public View getHolder() {
            return this.mHolder;
        }

        public void setHolder(View holder) {
            this.mHolder = holder;
        }

        public View getBackButton() {
            return this.mBackButton;
        }

        public void setBackButton(View backButton) {
            this.mBackButton = backButton;
        }
    }

    private static boolean checkForSearchBelowExtendAppBar(HwExpandedAppbarController appbarController, AnimationParams animationParams, boolean isOpen) {
        if (appbarController == null || appbarController.getAppBarLayout() == null || animationParams == null || animationParams.getSearchView() == null || animationParams.getSearchView().getHeight() <= 0 || animationParams.getMask() == null || animationParams.getHolder() == null || animationParams.getBackButton() == null) {
            return false;
        }
        boolean isMatch = isMatchAnimationCondition(animationParams, isOpen);
        if (!isMatch) {
            Log.e(TAG, "checkForSearchBelowExtendAppBar: do not match animation condition. isOpen " + isOpen);
        }
        return isMatch;
    }

    static boolean checkAnimatorInput(int dstMarginStart, int searchHeight, int dstTranslationY) {
        if (dstMarginStart <= 0) {
            Log.e(TAG, "checkAnimatorInput: fail to get search view's target MarginStart.");
            return false;
        } else if (searchHeight > 0 && dstTranslationY < 0) {
            return true;
        } else {
            Log.e(TAG, "checkAnimatorInput: searchHeight or dstTranslationY is invalid.");
            return false;
        }
    }

    private static boolean isMatchAnimationCondition(AnimationParams animationParams, boolean isOpen) {
        View searchView = animationParams.getSearchView();
        ViewGroup.LayoutParams params = searchView.getLayoutParams();
        ViewGroup.MarginLayoutParams marginParams = null;
        if (params instanceof ViewGroup.MarginLayoutParams) {
            marginParams = (ViewGroup.MarginLayoutParams) params;
        }
        if (isOpen) {
            if (searchView.getHeight() != animationParams.getHolder().getHeight()) {
                return false;
            }
            if (marginParams == null || marginParams.getMarginStart() == 0) {
                return true;
            }
            return false;
        } else if (mAppbarOriginalHeight == 0 || mAppbarScrollRange == 0) {
            return false;
        } else {
            if (marginParams != null && marginParams.getMarginStart() != 0) {
                return true;
            }
            searchView.clearFocus();
            return false;
        }
    }

    @TargetApi(11)
    static Animator getAppBarAnimator(View toolbar, int searchHeight, int distance, boolean isOpen) {
        String propertyName;
        Animator animatorActionBar;
        Context context = toolbar.getContext();
        if (toolbar instanceof HwAppBarLayout) {
            propertyName = "innerAlpha";
        } else if (toolbar instanceof HwToolbar) {
            propertyName = "innerAlpha";
        } else {
            propertyName = "alpha";
        }
        int aniTimeShort = getIntegerFromRes(context, "search_below_actionbar_toolbar_duration", 100);
        int aniTimeLong = (getIntegerFromRes(context, "search_below_actionbar_search_box_duration", ANIMATOR_TIME_LONG) * distance) / searchHeight;
        if (isOpen) {
            animatorActionBar = ObjectAnimator.ofFloat(toolbar, propertyName, 1.0f, 0.0f);
        } else {
            animatorActionBar = ObjectAnimator.ofFloat(toolbar, propertyName, 0.0f, 1.0f);
            aniTimeShort = (aniTimeShort * distance) / searchHeight;
            animatorActionBar.setStartDelay((long) (aniTimeLong - aniTimeShort));
        }
        animatorActionBar.setInterpolator(AnimationUtils.loadInterpolator(context, 17563661));
        animatorActionBar.setDuration((long) aniTimeShort);
        return animatorActionBar;
    }

    static ValueAnimator getWholeTimeAnimator(View search, int searchHeight, int distance, boolean isOpen) {
        ValueAnimator animatorOthers;
        Context context = search.getContext();
        int aniTimeLong = (getIntegerFromRes(context, "search_below_actionbar_search_box_duration", ANIMATOR_TIME_LONG) * distance) / searchHeight;
        if (isOpen) {
            animatorOthers = ValueAnimator.ofFloat(0.0f, 1.0f);
        } else {
            animatorOthers = ValueAnimator.ofFloat(1.0f, 0.0f);
        }
        animatorOthers.setInterpolator(AnimationUtils.loadInterpolator(context, 17563661));
        animatorOthers.setDuration((long) Math.abs(aniTimeLong));
        return animatorOthers;
    }

    private static void setViewsBeforeOpenAnimator(View tmpCollapseBtn, ViewGroup toolbar, View search) {
        Object parent = search.getParent();
        while (true) {
            ViewGroup viewGroup = (ViewGroup) parent;
            if (viewGroup == null) {
                break;
            }
            viewGroup.setClipChildren(false);
            Object object = viewGroup.getParent();
            if (!(object instanceof ViewGroup)) {
                break;
            }
            parent = object;
        }
        int toolbarVisibleH = toolbar.getHeight() + toolbar.getTop();
        tmpCollapseBtn.setY(search.getY());
        tmpCollapseBtn.setTranslationY((float) (-toolbarVisibleH));
        tmpCollapseBtn.setAlpha(0.0f);
        tmpCollapseBtn.setVisibility(0);
    }

    private static int getSearchMarginStart(View tmpCollapseBtn, View searchView) {
        int dstTranslationX;
        ViewGroup.LayoutParams params = searchView.getLayoutParams();
        if ((params instanceof ViewGroup.MarginLayoutParams) && (dstTranslationX = ((ViewGroup.MarginLayoutParams) params).getMarginStart()) > 0) {
            return dstTranslationX;
        }
        int dstTranslationX2 = tmpCollapseBtn.getLayoutParams().width;
        if (dstTranslationX2 > 0) {
            return dstTranslationX2;
        }
        int dstTranslationX3 = tmpCollapseBtn.getWidth();
        if (dstTranslationX3 > 0) {
            return dstTranslationX3;
        }
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        tmpCollapseBtn.measure(measureSpec, measureSpec);
        int dstTranslationX4 = tmpCollapseBtn.getMeasuredWidth();
        if (dstTranslationX4 > 0) {
            return dstTranslationX4;
        }
        return 0;
    }

    static void procSearchWholeTimeAnimation(View search, float searchMarginStart) {
        ViewGroup.MarginLayoutParams marginParams;
        ViewGroup.LayoutParams params = search.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            marginParams = (ViewGroup.MarginLayoutParams) params;
            marginParams.setMarginsRelative((int) searchMarginStart, marginParams.topMargin, marginParams.getMarginEnd(), marginParams.bottomMargin);
        } else {
            marginParams = new ViewGroup.MarginLayoutParams(params);
            marginParams.setMarginsRelative((int) searchMarginStart, 0, 0, 0);
        }
        search.setLayoutParams(marginParams);
    }

    static void procWholeTimeAlphaAnimation(View mask, View tmpCollapseBtn, float buttonAlpha) {
        if (mask != null) {
            mask.setAlpha(0.2f * buttonAlpha);
        }
        tmpCollapseBtn.setAlpha(buttonAlpha);
    }

    /* access modifiers changed from: private */
    public static boolean procSearchViewCollapsed(View search, View holder, int holderOriginalH, float searchTranslationY) {
        if (holder.getLayoutParams().height == 0) {
            return true;
        }
        holder.getLayoutParams().height = (int) (((float) holderOriginalH) + searchTranslationY);
        search.setTranslationY(searchTranslationY);
        if (holder.getLayoutParams().height > 0) {
            return false;
        }
        holder.getLayoutParams().height = 0;
        search.setTranslationY((float) (-search.getHeight()));
        return true;
    }

    static void requestLayoutTogether(View search, View holder, View mask, View backButton) {
        if (search != null) {
            search.requestLayout();
        }
        if (holder != null) {
            holder.requestLayout();
        }
        if (mask != null) {
            mask.requestLayout();
        }
        if (backButton != null) {
            backButton.requestLayout();
        }
    }

    @TargetApi(11)
    private static void procOpenAnimationListener(final HwExpandedAppbarController appbarController, final HwAppBarLayout toolbar, AnimationParams animationParams, Animator animator) {
        final View search = animationParams.getSearchView();
        final View holder = animationParams.getHolder();
        final View backButton = animationParams.getBackButton();
        if (mAppbarScrollRange == 0) {
            mAppbarScrollRange = toolbar.getDownNestedScrollRange();
        }
        if (mAppbarOriginalHeight == 0) {
            mAppbarOriginalHeight = toolbar.getHeight();
        }
        animator.addListener(new AnimatorListenerAdapter() {
            /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass9 */

            public void onAnimationStart(Animator animation) {
                HwExpandedAppbarController.this.setPermitCollapse(false);
                backButton.setVisibility(0);
                HwSearchAnimationUtils.setSearchBackGroundColor(search, true);
            }

            public void onAnimationEnd(Animator animation) {
                holder.getLayoutParams().height = search.getHeight();
                search.setTranslationY(0.0f);
                backButton.setTranslationY(0.0f);
                toolbar.getLayoutParams().height = 0;
                HwSearchAnimationUtils.requestLayoutTogether(search, holder, null, backButton);
                toolbar.requestLayout();
                HwExpandedAppbarController.this.setPermitCollapse(true);
            }
        });
    }

    @TargetApi(11)
    private static void procCancelAnimationListener(final HwExpandedAppbarController appbarController, final HwAppBarLayout toolbar, AnimationParams animationParams, Animator animator) {
        final View search = animationParams.getSearchView();
        final View holder = animationParams.getHolder();
        final View backButton = animationParams.getBackButton();
        animator.addListener(new AnimatorListenerAdapter() {
            /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass10 */

            public void onAnimationStart(Animator animation) {
                HwExpandedAppbarController.this.setPermitCollapse(false);
                toolbar.getLayoutParams().height = HwSearchAnimationUtils.mAppbarOriginalHeight;
                toolbar.setDownScrollRange(HwSearchAnimationUtils.mAppbarScrollRange);
                HwSearchAnimationUtils.setAppbarAlpha(toolbar, 0.0f);
                toolbar.setExpandedLayoutY(HwSearchAnimationUtils.mAppbarScrollRange);
                holder.getLayoutParams().height = 0;
                View view = search;
                view.setTranslationY((float) (-view.getHeight()));
                backButton.setTranslationY((float) (-search.getHeight()));
                HwSearchAnimationUtils.requestLayoutTogether(search, holder, null, backButton);
            }

            public void onAnimationEnd(Animator animation) {
                backButton.setVisibility(4);
                search.clearFocus();
                ViewGroup.LayoutParams params = search.getLayoutParams();
                if (params instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) params).setMarginStart(0);
                }
                ViewParent viewParent = search.getParent();
                if (viewParent instanceof ViewGroup) {
                    ((ViewGroup) viewParent).setClipChildren(true);
                }
                HwSearchAnimationUtils.setSearchBackGroundColor(search, false);
                HwExpandedAppbarController.this.setPermitCollapse(true);
            }
        });
    }

    @TargetApi(11)
    public static Animator getBelowAppBarOpenAnimator(HwExpandedAppbarController appbarController, AnimationParams animationParams) {
        if (!checkForSearchBelowExtendAppBar(appbarController, animationParams, true)) {
            Log.e(TAG, "getBelowAppBarOpenAnimator: input params is illegal.");
            return null;
        }
        final View search = animationParams.getSearchView();
        final View mask = animationParams.getMask();
        final View holder = animationParams.getHolder();
        final View backButton = animationParams.getBackButton();
        final HwAppBarLayout toolbar = appbarController.getAppBarLayout();
        setViewsBeforeOpenAnimator(backButton, toolbar, search);
        final int dstMarginStart = getSearchMarginStart(backButton, search);
        final int dstTranslationY = -(toolbar.getHeight() + toolbar.getTop());
        final int holderOriginalH = holder.getHeight();
        final int searchHeight = search.getHeight();
        if (!checkAnimatorInput(dstMarginStart, searchHeight, dstTranslationY)) {
            return null;
        }
        ValueAnimator animatorOthers = getWholeTimeAnimator(search, searchHeight, -dstTranslationY, true);
        animatorOthers.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass11 */
            private int mLastOffset = 0;

            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                HwSearchAnimationUtils.procSearchWholeTimeAnimation(search, ((float) dstMarginStart) * value);
                HwSearchAnimationUtils.procWholeTimeAlphaAnimation(mask, backButton, value);
                if (!HwSearchAnimationUtils.procSearchViewCollapsed(search, holder, holderOriginalH, ((float) dstTranslationY) * value)) {
                    HwSearchAnimationUtils.requestLayoutTogether(null, holder, null, null);
                } else if (!toolbar.isCollapsed()) {
                    int offsetY = ((int) ((-value) * ((float) dstTranslationY))) - searchHeight;
                    View view = backButton;
                    view.setTranslationY((view.getTranslationY() + ((float) offsetY)) - ((float) this.mLastOffset));
                    toolbar.setExpandedLayoutY(offsetY - this.mLastOffset);
                    this.mLastOffset = offsetY;
                    HwSearchAnimationUtils.requestLayoutTogether(search, holder, null, null);
                }
            }
        });
        AnimatorSet animatorSetTmps = new AnimatorSet();
        animatorSetTmps.playTogether(getAppBarAnimator(toolbar, searchHeight, -dstTranslationY, true), animatorOthers);
        procOpenAnimationListener(appbarController, toolbar, animationParams, animatorSetTmps);
        return animatorSetTmps;
    }

    /* access modifiers changed from: private */
    public static boolean procSearchViewExpanded(View search, View holder, float searchTranslationY, float translationOffset) {
        if (holder.getHeight() >= search.getHeight()) {
            Log.w(TAG, "procSearchViewExpanded: there is something wrong. holder.getHeight() " + holder.getHeight());
        }
        holder.getLayoutParams().height = (int) ((-searchTranslationY) - translationOffset);
        search.setTranslationY(((float) holder.getLayoutParams().height) + searchTranslationY);
        return false;
    }

    @TargetApi(11)
    public static Animator getBelowAppBarCancelAnimator(HwExpandedAppbarController appbarController, AnimationParams animationParams) {
        if (!checkForSearchBelowExtendAppBar(appbarController, animationParams, false)) {
            Log.e(TAG, "getBelowAppBarCancelAnimator: input params is illegal.");
            return null;
        }
        final View search = animationParams.getSearchView();
        final View mask = animationParams.getMask();
        final View holder = animationParams.getHolder();
        final View backButton = animationParams.getBackButton();
        final HwAppBarLayout toolbar = appbarController.getAppBarLayout();
        final float dstTranslationY = (float) mAppbarOriginalHeight;
        final int originalMarginStart = getSearchMarginStart(backButton, search);
        final float originalTranslationY = (float) (-search.getHeight());
        int searchHeight = search.getHeight();
        ValueAnimator animatorOthers = getWholeTimeAnimator(search, searchHeight, (int) dstTranslationY, false);
        animatorOthers.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class huawei.android.view.HwSearchAnimationUtils.AnonymousClass12 */
            private boolean isSearchViewCollapsed = true;
            private int lastOffset = (-HwAppBarLayout.this.getTop());

            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                HwSearchAnimationUtils.procWholeTimeAlphaAnimation(mask, backButton, value);
                HwSearchAnimationUtils.procSearchWholeTimeAnimation(search, ((float) originalMarginStart) * value);
                if (this.isSearchViewCollapsed) {
                    int offsetY = ((int) (dstTranslationY * value)) + ((int) originalTranslationY);
                    if (offsetY <= 0) {
                        offsetY = 0;
                        this.isSearchViewCollapsed = false;
                    }
                    View view = backButton;
                    view.setTranslationY((view.getTranslationY() + ((float) offsetY)) - ((float) this.lastOffset));
                    HwAppBarLayout.this.setExpandedLayoutY(offsetY - this.lastOffset);
                    this.lastOffset = offsetY;
                }
                if (!this.isSearchViewCollapsed) {
                    HwSearchAnimationUtils.procSearchViewExpanded(search, holder, originalTranslationY, dstTranslationY * value);
                }
                HwSearchAnimationUtils.requestLayoutTogether(search, holder, null, null);
            }
        });
        AnimatorSet animatorSetTmps = new AnimatorSet();
        animatorSetTmps.playTogether(getAppBarAnimator(toolbar, searchHeight, (int) dstTranslationY, false), animatorOthers);
        procCancelAnimationListener(appbarController, toolbar, animationParams, animatorSetTmps);
        return animatorSetTmps;
    }

    private static int getIntegerFromRes(Context context, String resName, int defVal) {
        int resId = ResLoader.getInstance().getIdentifier(context, "values", resName);
        if (resId == 0) {
            return defVal;
        }
        try {
            return context.getResources().getInteger(resId);
        } catch (Resources.NotFoundException e) {
            Log.d(TAG, "getIntegerFromRes: " + resName + "not found, use default value.");
            return defVal;
        } catch (Exception e2) {
            Log.d(TAG, "getIntegerFromRes: " + resName + "not found, other exception.");
            return defVal;
        }
    }

    static void setAppbarAlpha(View appbar, float alpha) {
        if (appbar instanceof HwAppBarLayout) {
            ((HwAppBarLayout) appbar).setInnerAlpha(alpha);
        } else if (appbar instanceof HwToolbar) {
            ((HwToolbar) appbar).setInnerAlpha(alpha);
        } else {
            appbar.setAlpha(alpha);
        }
    }

    static void setSearchBackGroundColor(View search, boolean isEnable) {
        if (search instanceof SearchView) {
            ((SearchView) search).setBackGroundEx(isEnable);
        }
    }

    static Activity getActivity(View view) {
        if (view == null) {
            Log.e(TAG, "getActivity: view is null");
            return null;
        }
        for (Context context = view.getContext(); context instanceof ContextWrapper; context = ((ContextWrapper) context).getBaseContext()) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
        }
        return null;
    }
}
