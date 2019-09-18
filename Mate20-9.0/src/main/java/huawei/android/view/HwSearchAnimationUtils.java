package huawei.android.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.ActionMenuPresenter;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.HwActionMenuPresenter;
import huawei.android.widget.HwToolbar;
import huawei.android.widget.loader.ResLoader;

public class HwSearchAnimationUtils {
    private static final boolean DBG = true;
    private static final int ENTER_SEARCH = 1;
    private static final int EXIT_SEARCH = 0;
    private static final String TAG = "HwSearchAnimationUtils";

    private static class ViewWrapper {
        private ViewGroup.LayoutParams mLayoutParams;
        private int mState;
        private View mTarget;

        ViewWrapper(View target, int state) {
            this.mTarget = target;
            this.mState = state;
            this.mLayoutParams = target.getLayoutParams();
        }

        public int getWidth() {
            return this.mLayoutParams.width;
        }

        public void setWidth(int width) {
            this.mLayoutParams.width = width;
            this.mTarget.requestLayout();
            Log.d(HwSearchAnimationUtils.TAG, "ViewWrapper.setWidth: state=" + this.mState + ", view=" + this.mTarget.toString() + ", input width=" + width + ", effect width=" + this.mTarget.getWidth());
        }

        public int getHeight() {
            return this.mLayoutParams.height;
        }

        public void setHeight(int height) {
            this.mLayoutParams.height = height;
            this.mTarget.requestLayout();
            Log.d(HwSearchAnimationUtils.TAG, "ViewWrapper.setWidth: state=" + this.mState + ", view=" + this.mTarget.toString() + ", input height=" + height + ", effect height=" + this.mTarget.getHeight());
        }
    }

    /* JADX WARNING: type inference failed for: r3v1, types: [android.animation.Animator] */
    /* JADX WARNING: Multi-variable type inference failed */
    public static Animator getSearchFixedOpenAnimator(Context context, View maskView) {
        int resId = ResLoader.getInstance().getIdentifier(context, "animator", "search_mask");
        ObjectAnimator objectAnimator = null;
        if (resId != 0) {
            objectAnimator = AnimatorInflater.loadAnimator(context, resId);
            if (objectAnimator != null) {
                objectAnimator.setTarget(maskView);
            }
        } else {
            Log.w(TAG, "search_mask animator file not found");
        }
        return objectAnimator;
    }

    /* JADX WARNING: type inference failed for: r3v1, types: [android.animation.Animator] */
    /* JADX WARNING: Multi-variable type inference failed */
    public static Animator getSearchFixedCloseAnimator(Context context, View maskView) {
        int resId = ResLoader.getInstance().getIdentifier(context, "animator", "search_unmask");
        ObjectAnimator objectAnimator = null;
        if (resId != 0) {
            objectAnimator = AnimatorInflater.loadAnimator(context, resId);
            if (objectAnimator != null) {
                objectAnimator.setTarget(maskView);
            }
        } else {
            Log.w(TAG, "search_unmask animator file not found");
        }
        return objectAnimator;
    }

    public static Animator getSearchBoxAlwaysOnAppbarOpenAnimator(Context context, View enterView, View exitView) {
        ResLoader resLoader = ResLoader.getInstance();
        int openEnterResId = resLoader.getIdentifier(context, "animator", "search_open_enter");
        int openExitResId = resLoader.getIdentifier(context, "animator", "search_open_exit");
        if (openEnterResId == 0 || openExitResId == 0) {
            Log.w(TAG, "search_open animator file not found");
            return null;
        }
        ObjectAnimator openEnterAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, openEnterResId);
        openEnterAnimator.setTarget(enterView);
        ObjectAnimator openExitAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, openExitResId);
        openExitAnimator.setTarget(exitView);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{openEnterAnimator, openExitAnimator});
        return animatorSet;
    }

    public static Animator getSearchBoxAlwaysOnAppbarCloseAnimator(Context context, View enterView, View exitView) {
        ResLoader resLoader = ResLoader.getInstance();
        int closeEnterResId = resLoader.getIdentifier(context, "animator", "search_close_enter");
        int closeExitResId = resLoader.getIdentifier(context, "animator", "search_close_exit");
        if (closeEnterResId == 0 || closeExitResId == 0) {
            Log.w(TAG, "search_close animator file not found");
            return null;
        }
        ObjectAnimator closeEnterAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, closeEnterResId);
        closeEnterAnimator.setTarget(enterView);
        ObjectAnimator closeExitAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, closeExitResId);
        closeExitAnimator.setTarget(exitView);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{closeEnterAnimator, closeExitAnimator});
        return animatorSet;
    }

    public static MenuItem.OnActionExpandListener getAlphaAnimatedOnActionExpandListener() {
        return new MenuItem.OnActionExpandListener() {
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (item == null) {
                    Log.w(HwSearchAnimationUtils.TAG, "MenuItem is null");
                    return false;
                }
                View actionView = item.getActionView();
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                alphaAnimation.setDuration(50);
                if (actionView != null) {
                    actionView.startAnimation(alphaAnimation);
                }
                return HwSearchAnimationUtils.DBG;
            }

            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (item == null) {
                    Log.w(HwSearchAnimationUtils.TAG, "MenuItem is null");
                    return false;
                }
                View actionView = item.getActionView();
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                alphaAnimation.setDuration(50);
                if (actionView != null) {
                    actionView.startAnimation(alphaAnimation);
                }
                return HwSearchAnimationUtils.DBG;
            }
        };
    }

    public static Animator getSearchBelowActionBarSearchAnimator(Context context, MenuItem searchMenuItem, HwToolbar toolbar, View search, View mask, View holder, View tmpCollapseBtn) {
        int xTranslation;
        float yTranslation;
        float f;
        AnimatorSet animatorSetTmp;
        int aniTimeLong;
        float yTranslation2;
        Context context2 = context;
        HwToolbar hwToolbar = toolbar;
        View view = search;
        View view2 = tmpCollapseBtn;
        AnimatorSet animatorSet = new AnimatorSet();
        ViewWrapper searchWrapper = new ViewWrapper(view, 1);
        ViewWrapper holderWrapper = new ViewWrapper(holder, 1);
        ViewWrapper toolbarWrapper = new ViewWrapper(hwToolbar, 1);
        int aniTimeShort = getIntegerFromRes(context2, "search_below_actionbar_toolbar_duration", 100);
        int aniTimeLong2 = getIntegerFromRes(context2, "search_below_actionbar_search_box_duration", 150);
        try {
            if (search.getWidth() != toolbar.getWidth()) {
                return null;
            }
            boolean isRtl = toolbar.getLayoutDirection() == 1;
            try {
                xTranslation = tmpCollapseBtn.getWidth();
                yTranslation = (float) (-search.getHeight());
                Log.d(TAG, "SearchAnimator: xTranslation " + xTranslation);
                Log.d(TAG, "SearchAnimator: yTranslation " + yTranslation);
                view2.setAlpha(0.0f);
                view2.setVisibility(0);
                if (isRtl) {
                    try {
                        f = (toolbar.getX() + ((float) toolbar.getWidth())) - ((float) tmpCollapseBtn.getWidth());
                    } catch (Exception e) {
                        e = e;
                        int i = aniTimeLong2;
                        int i2 = aniTimeShort;
                        ViewWrapper viewWrapper = toolbarWrapper;
                        ViewWrapper viewWrapper2 = holderWrapper;
                        Log.d(TAG, "SearchAnimator failed. Info: " + e.toString());
                        return null;
                    }
                } else {
                    f = toolbar.getX();
                }
                view2.setX(f);
                view2.setY(toolbar.getY());
                Log.d(TAG, "SearchAnimator: tmpCollapseBtn.x " + tmpCollapseBtn.getX());
                animatorSetTmp = new AnimatorSet();
                animatorSetTmp.setDuration((long) aniTimeLong2);
                aniTimeLong = aniTimeLong2;
            } catch (Exception e2) {
                e = e2;
                int i3 = aniTimeLong2;
                int i4 = aniTimeShort;
                ViewWrapper viewWrapper3 = toolbarWrapper;
                ViewWrapper viewWrapper4 = holderWrapper;
                Log.d(TAG, "SearchAnimator failed. Info: " + e.toString());
                return null;
            }
            try {
                AnimatorSet.Builder builder = animatorSetTmp.play(ObjectAnimator.ofFloat(view, "translationY", new float[]{0.0f, yTranslation}));
                if (!isRtl) {
                    yTranslation2 = yTranslation;
                    try {
                        builder.with(ObjectAnimator.ofFloat(view, "translationX", new float[]{0.0f, (float) xTranslation}));
                    } catch (Exception e3) {
                        e = e3;
                        int i5 = aniTimeShort;
                        ViewWrapper viewWrapper5 = toolbarWrapper;
                        ViewWrapper viewWrapper6 = holderWrapper;
                        int i6 = aniTimeLong;
                    }
                } else {
                    yTranslation2 = yTranslation;
                    builder.with(ObjectAnimator.ofFloat(view, "translationX", new float[]{0.0f, (float) (-xTranslation)}));
                }
                builder.with(ObjectAnimator.ofInt(searchWrapper, "width", new int[]{search.getWidth(), search.getWidth() - xTranslation}));
                Log.d(TAG, "SearchAnimator: search.width " + search.getWidth());
                builder.with(ObjectAnimator.ofFloat(mask, "Alpha", new float[]{0.0f, 0.2f}));
                int i7 = xTranslation;
                builder.with(ObjectAnimator.ofFloat(view2, "Alpha", new float[]{0.0f, 1.0f}));
                builder.with(ObjectAnimator.ofInt(holderWrapper, "height", new int[]{holder.getHeight(), holder.getHeight() - search.getHeight()}));
                Log.d(TAG, "SearchAnimator: holder.height " + holder.getHeight() + " search.height " + search.getHeight());
                if (toolbar.getHeight() != search.getHeight()) {
                    builder.with(ObjectAnimator.ofInt(toolbarWrapper, "height", new int[]{toolbar.getHeight(), search.getHeight()}));
                }
                Log.d(TAG, "SearchAnimator: toolbar.height " + toolbar.getHeight());
                ObjectAnimator animatorToolbarAlpha = ObjectAnimator.ofFloat(hwToolbar, "alpha", new float[]{1.0f, 0.0f});
                animatorToolbarAlpha.setDuration((long) aniTimeShort);
                animatorSet.playTogether(new Animator[]{animatorToolbarAlpha, animatorSetTmp});
                animatorSet.setInterpolator(AnimationUtils.loadInterpolator(context2, 17563661));
                r2 = r2;
                final MenuItem menuItem = searchMenuItem;
                AnimatorSet animatorSet2 = animatorSetTmp;
                final HwToolbar hwToolbar2 = hwToolbar;
                ObjectAnimator objectAnimator = animatorToolbarAlpha;
                AnonymousClass2 r0 = r2;
                float f2 = yTranslation2;
                final View view3 = view;
                int i8 = aniTimeLong;
                AnimatorSet.Builder builder2 = builder;
                final View view4 = view2;
                int i9 = aniTimeShort;
                final ViewWrapper viewWrapper7 = searchWrapper;
                ViewWrapper toolbarWrapper2 = toolbarWrapper;
                final ViewWrapper toolbarWrapper3 = holderWrapper;
                ViewWrapper viewWrapper8 = holderWrapper;
                final ViewWrapper holderWrapper2 = toolbarWrapper2;
                try {
                    AnonymousClass2 r2 = new Animator.AnimatorListener() {
                        public void onAnimationStart(Animator animation) {
                        }

                        public void onAnimationEnd(Animator animation) {
                            try {
                                MenuBuilder mMenu = (MenuBuilder) ReflectUtil.getObject(menuItem, "mMenu", MenuItemImpl.class);
                                if (mMenu != null) {
                                    ReflectUtil.callMethod(mMenu, "expandItemActionView", new Class[]{MenuItemImpl.class}, new Object[]{menuItem}, MenuBuilder.class);
                                } else {
                                    Log.d(HwSearchAnimationUtils.TAG, "mMenu is null, won't call expandItemActionView... searchMenuItem is " + menuItem);
                                }
                                HwActionMenuPresenter mActionMenuPresenter = (HwActionMenuPresenter) ReflectUtil.getObject(hwToolbar2, "mActionMenuPresenter", HwToolbar.class);
                                if (mActionMenuPresenter == null) {
                                    Log.d(HwSearchAnimationUtils.TAG, "search animation onAnimationEnd: mActionMenuPresenter is null");
                                    return;
                                }
                                View mOverflowButton = (View) ReflectUtil.getObject(mActionMenuPresenter, "mHwOverflowButton", ActionMenuPresenter.class);
                                if (mOverflowButton != null) {
                                    Log.d(HwSearchAnimationUtils.TAG, "search animation onAnimationEnd: hide overflow button");
                                    mOverflowButton.setVisibility(8);
                                }
                                if (menuItem != null) {
                                    menuItem.getActionView().getLayoutParams().width = -1;
                                    hwToolbar2.getLayoutParams().height = -2;
                                    hwToolbar2.setAlpha(1.0f);
                                    view3.setVisibility(4);
                                    view4.setVisibility(4);
                                    int heightSearch = viewWrapper7.getHeight();
                                    int heightHolder = toolbarWrapper3.getHeight();
                                    int heightToolbar = holderWrapper2.getHeight();
                                    Log.d(HwSearchAnimationUtils.TAG, "search animation onAnimationEnd: exec, search width is " + heightSearch + ", holder height is " + heightHolder + ", toolbar height is " + heightToolbar);
                                    return;
                                }
                                Log.d(HwSearchAnimationUtils.TAG, "searchMenuItem is null, animation end");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        public void onAnimationCancel(Animator animation) {
                        }

                        public void onAnimationRepeat(Animator animation) {
                        }
                    };
                    animatorSet.addListener(r0);
                    return animatorSet;
                } catch (Exception e4) {
                    e = e4;
                    Log.d(TAG, "SearchAnimator failed. Info: " + e.toString());
                    return null;
                }
            } catch (Exception e5) {
                e = e5;
                int i10 = aniTimeShort;
                ViewWrapper viewWrapper9 = toolbarWrapper;
                ViewWrapper viewWrapper10 = holderWrapper;
                int i11 = aniTimeLong;
                Log.d(TAG, "SearchAnimator failed. Info: " + e.toString());
                return null;
            }
        } catch (Exception e6) {
            e = e6;
            int i12 = aniTimeLong2;
            int i13 = aniTimeShort;
            ViewWrapper viewWrapper11 = toolbarWrapper;
            ViewWrapper viewWrapper12 = holderWrapper;
            Log.d(TAG, "SearchAnimator failed. Info: " + e.toString());
            return null;
        }
    }

    public static Animator getSearchBelowActionBarCancelAnimator(Context context, HwToolbar toolbar, View search, View mask, View holder, View tmpCollapseBtn) {
        float f;
        Context context2 = context;
        View view = search;
        View view2 = tmpCollapseBtn;
        AnimatorSet animatorSet = new AnimatorSet();
        ViewWrapper searchWrapper = new ViewWrapper(view, 0);
        ViewWrapper holderWrapper = new ViewWrapper(holder, 0);
        int aniTimeShort = getIntegerFromRes(context2, "search_below_actionbar_toolbar_duration", 100);
        int aniTimeLong = getIntegerFromRes(context2, "search_below_actionbar_search_box_duration", 150);
        int aniTimeDelay = getIntegerFromRes(context2, "search_below_actionbar_toolbar_delay", 50);
        try {
            boolean isRtl = toolbar.getLayoutDirection() == 1;
            try {
                int yTranslation = -toolbar.getHeight();
                int xTranslation = tmpCollapseBtn.getWidth();
                Log.d(TAG, "CancelAnimator: xTranslation " + xTranslation);
                Log.d(TAG, "CancelAnimator: yTranslation " + yTranslation);
                if (isRtl) {
                    try {
                        f = (toolbar.getX() + ((float) toolbar.getWidth())) - ((float) tmpCollapseBtn.getWidth());
                    } catch (Exception e) {
                        e = e;
                        int i = aniTimeDelay;
                    }
                } else {
                    f = toolbar.getX();
                }
                view2.setX(f);
                Log.d(TAG, "CancelAnimator: tmpCollapseBtn.x " + tmpCollapseBtn.getX());
                AnimatorSet animatorSetTmp = new AnimatorSet();
                animatorSetTmp.setDuration((long) aniTimeLong);
                AnimatorSet.Builder builder = animatorSetTmp.play(ObjectAnimator.ofFloat(view, "translationY", new float[]{(float) yTranslation, 0.0f}));
                if (!isRtl) {
                    int i2 = yTranslation;
                    builder.with(ObjectAnimator.ofFloat(view, "translationX", new float[]{(float) xTranslation, 0.0f}));
                } else {
                    builder.with(ObjectAnimator.ofFloat(view, "translationX", new float[]{(float) (-xTranslation), 0.0f}));
                }
                builder.with(ObjectAnimator.ofInt(searchWrapper, "width", new int[]{search.getWidth(), search.getWidth() + xTranslation}));
                Log.d(TAG, "CancelAnimator: search.width " + search.getWidth());
                builder.with(ObjectAnimator.ofFloat(mask, "Alpha", new float[]{0.2f, 0.0f}));
                builder.with(ObjectAnimator.ofFloat(view2, "Alpha", new float[]{1.0f, 0.0f}));
                builder.with(ObjectAnimator.ofInt(holderWrapper, "height", new int[]{holder.getHeight(), search.getHeight()}));
                Log.d(TAG, "CancelAnimator: holder.height " + holder.getHeight() + " search.height " + search.getHeight());
                ObjectAnimator animatorToolbarAlpha = ObjectAnimator.ofFloat(toolbar, "alpha", new float[]{0.0f, 1.0f});
                AnimatorSet.Builder builder2 = builder;
                animatorToolbarAlpha.setDuration((long) aniTimeShort);
                animatorToolbarAlpha.setStartDelay((long) aniTimeDelay);
                animatorSet.playTogether(new Animator[]{animatorToolbarAlpha, animatorSetTmp});
                animatorSet.setInterpolator(AnimationUtils.loadInterpolator(context2, 17563661));
                r2 = r2;
                AnimatorSet.Builder builder3 = builder2;
                final HwToolbar hwToolbar = toolbar;
                ObjectAnimator objectAnimator = animatorToolbarAlpha;
                AnonymousClass3 r0 = r2;
                final View view3 = view;
                AnimatorSet animatorSet2 = animatorSetTmp;
                final View view4 = view2;
                int i3 = xTranslation;
                final ViewWrapper viewWrapper = searchWrapper;
                int i4 = aniTimeDelay;
                final ViewWrapper viewWrapper2 = holderWrapper;
                try {
                    AnonymousClass3 r2 = new Animator.AnimatorListener() {
                        public void onAnimationStart(Animator animation) {
                            Log.d(HwSearchAnimationUtils.TAG, "back animation onAnimationStart: exec.");
                            try {
                                HwActionMenuPresenter mActionMenuPresenter = (HwActionMenuPresenter) ReflectUtil.getObject(HwToolbar.this, "mActionMenuPresenter", HwToolbar.class);
                                if (mActionMenuPresenter == null) {
                                    Log.d(HwSearchAnimationUtils.TAG, "back animation onAnimationStart: mActionMenuPresenter is null");
                                    return;
                                }
                                View mOverflowButton = (View) ReflectUtil.getObject(mActionMenuPresenter, "mHwOverflowButton", ActionMenuPresenter.class);
                                if (mOverflowButton != null) {
                                    Log.d(HwSearchAnimationUtils.TAG, "back animation onAnimationStart: show overflow button");
                                    mOverflowButton.setVisibility(0);
                                }
                                HwToolbar.this.setAlpha(0.0f);
                                view3.setVisibility(0);
                                view4.setVisibility(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        public void onAnimationEnd(Animator animation) {
                            view4.setVisibility(4);
                            view3.clearFocus();
                            int heightSearch = viewWrapper.getHeight();
                            int heightHolder = viewWrapper2.getHeight();
                            Log.d(HwSearchAnimationUtils.TAG, "back animation onAnimationEnd: exec, search width is " + heightSearch + ", holder height is " + heightHolder + " .");
                        }

                        public void onAnimationCancel(Animator animation) {
                        }

                        public void onAnimationRepeat(Animator animation) {
                        }
                    };
                    animatorSet.addListener(r0);
                    return animatorSet;
                } catch (Exception e2) {
                    e = e2;
                    Log.d(TAG, "CancelAnimator failed. Info: " + e.toString());
                    return null;
                }
            } catch (Exception e3) {
                e = e3;
                int i5 = aniTimeDelay;
                Log.d(TAG, "CancelAnimator failed. Info: " + e.toString());
                return null;
            }
        } catch (Exception e4) {
            e = e4;
            int i6 = aniTimeDelay;
            Log.d(TAG, "CancelAnimator failed. Info: " + e.toString());
            return null;
        }
    }

    private static int getIntegerFromRes(Context context, String resName, int defVal) {
        int val = defVal;
        int resId = ResLoader.getInstance().getIdentifier(context, "values", resName);
        if (resId == 0) {
            return val;
        }
        try {
            return context.getResources().getInteger(resId);
        } catch (Exception e) {
            Log.d(TAG, "getIntegerFromRes: " + resName + "not found, use default value.");
            return val;
        }
    }
}
