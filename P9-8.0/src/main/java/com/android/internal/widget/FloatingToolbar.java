package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManagerGlobal;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class FloatingToolbar {
    public static final String FLOATING_TOOLBAR_TAG = "floating_toolbar";
    private static final OnMenuItemClickListener NO_OP_MENUITEM_CLICK_LISTENER = new -$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s();
    private static final String TAG = "FloatingToolbar";
    private static final boolean[] mHwFloatingToolbarFlag = new boolean[1];
    private static int mHwFloatingToolbarHeightBias;
    private final Rect mContentRect;
    private final Context mContext;
    private Menu mMenu;
    private OnMenuItemClickListener mMenuItemClickListener;
    private final OnLayoutChangeListener mOrientationChangeHandler;
    private final FloatingToolbarPopup mPopup;
    private final Rect mPreviousContentRect;
    private List<MenuItem> mShowingMenuItems;
    private int mSuggestedWidth;
    private boolean mWidthChanged;
    private final Window mWindow;

    private static final class FloatingToolbarPopup {
        private static final int MAX_OVERFLOW_SIZE = 4;
        private static final int MIN_OVERFLOW_SIZE = 2;
        private final Drawable mArrow;
        private final AnimationSet mCloseOverflowAnimation;
        private final ViewGroup mContentContainer;
        private final Context mContext;
        private final Point mCoordsOnWindow = new Point();
        private final AnimatorSet mDismissAnimation;
        private boolean mDismissed = true;
        private final Interpolator mFastOutLinearInInterpolator;
        private final Interpolator mFastOutSlowInInterpolator;
        private boolean mHidden;
        private final AnimatorSet mHideAnimation;
        private final int mIconTextSpacing;
        private final OnComputeInternalInsetsListener mInsetsComputer = new com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s.AnonymousClass1(this);
        private boolean mIsOverflowOpen;
        private final int mLineHeight;
        private final Interpolator mLinearOutSlowInInterpolator;
        private final Interpolator mLogAccelerateInterpolator;
        private final ViewGroup mMainPanel;
        private Size mMainPanelSize;
        private final int mMarginHorizontal;
        private final int mMarginVertical;
        private final OnClickListener mMenuItemButtonOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                if ((v.getTag() instanceof MenuItem) && FloatingToolbarPopup.this.mOnMenuItemClickListener != null) {
                    FloatingToolbarPopup.this.mOnMenuItemClickListener.onMenuItemClick((MenuItem) v.getTag());
                }
            }
        };
        private OnMenuItemClickListener mOnMenuItemClickListener;
        private final AnimationSet mOpenOverflowAnimation;
        private boolean mOpenOverflowUpwards;
        private final Drawable mOverflow;
        private final AnimationListener mOverflowAnimationListener;
        private final ImageButton mOverflowButton;
        private final Size mOverflowButtonSize;
        private final OverflowPanel mOverflowPanel;
        private Size mOverflowPanelSize;
        private final OverflowPanelViewHelper mOverflowPanelViewHelper;
        private final View mParent;
        private final PopupWindow mPopupWindow;
        private final Runnable mPreparePopupContentRTLHelper = new Runnable() {
            public void run() {
                FloatingToolbarPopup.this.setPanelsStatesAtRestingPosition();
                FloatingToolbarPopup.this.setContentAreaAsTouchableSurface();
                FloatingToolbarPopup.this.mContentContainer.setAlpha(1.0f);
            }
        };
        private final AnimatorSet mShowAnimation;
        private final int[] mTmpCoords = new int[2];
        private final AnimatedVectorDrawable mToArrow;
        private final AnimatedVectorDrawable mToOverflow;
        private final Region mTouchableRegion = new Region();
        private int mTransitionDurationScale;
        private final Rect mViewPortOnScreen = new Rect();

        private static final class LogAccelerateInterpolator implements Interpolator {
            private static final int BASE = 100;
            private static final float LOGS_SCALE = (1.0f / computeLog(1.0f, 100));

            /* synthetic */ LogAccelerateInterpolator(LogAccelerateInterpolator -this0) {
                this();
            }

            private LogAccelerateInterpolator() {
            }

            private static float computeLog(float t, int base) {
                return (float) (1.0d - Math.pow((double) base, (double) (-t)));
            }

            public float getInterpolation(float t) {
                return 1.0f - (computeLog(1.0f - t, 100) * LOGS_SCALE);
            }
        }

        private static final class OverflowPanel extends ListView {
            private final FloatingToolbarPopup mPopup;

            OverflowPanel(FloatingToolbarPopup popup) {
                super(((FloatingToolbarPopup) Preconditions.checkNotNull(popup)).mContext);
                this.mPopup = popup;
                setScrollBarDefaultDelayBeforeFade(ViewConfiguration.getScrollDefaultDelay() * 3);
                setScrollIndicators(3);
            }

            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mPopup.mOverflowPanelSize.getHeight() - this.mPopup.mOverflowButtonSize.getHeight(), 1073741824));
            }

            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (this.mPopup.isOverflowAnimating()) {
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }

            protected boolean awakenScrollBars() {
                return super.-wrap0();
            }
        }

        private static final class OverflowPanelViewHelper {
            private final View mCalculator = createMenuButton(null);
            private final Context mContext;
            private final int mIconTextSpacing;
            private final int mSidePadding;

            public OverflowPanelViewHelper(Context context) {
                this.mContext = (Context) Preconditions.checkNotNull(context);
                this.mIconTextSpacing = context.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_menu_button_side_padding);
                this.mSidePadding = context.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_overflow_side_padding);
            }

            public View getView(MenuItem menuItem, int minimumWidth, View convertView) {
                Preconditions.checkNotNull(menuItem);
                if (convertView != null) {
                    FloatingToolbar.updateMenuItemButton(convertView, menuItem, this.mIconTextSpacing);
                } else {
                    convertView = createMenuButton(menuItem);
                }
                convertView.setMinimumWidth(minimumWidth);
                return convertView;
            }

            public int calculateWidth(MenuItem menuItem) {
                FloatingToolbar.updateMenuItemButton(this.mCalculator, menuItem, this.mIconTextSpacing);
                this.mCalculator.measure(0, 0);
                return this.mCalculator.getMeasuredWidth();
            }

            private View createMenuButton(MenuItem menuItem) {
                View button = FloatingToolbar.createMenuItemButton(this.mContext, menuItem, this.mIconTextSpacing);
                button.setPadding(this.mSidePadding, 0, this.mSidePadding, 0);
                return button;
            }
        }

        /* synthetic */ void lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup_16966(InternalInsetsInfo info) {
            info.contentInsets.setEmpty();
            info.visibleInsets.setEmpty();
            info.touchableRegion.set(this.mTouchableRegion);
            info.setTouchableInsets(3);
        }

        public FloatingToolbarPopup(Context context, View parent) {
            this.mParent = (View) Preconditions.checkNotNull(parent);
            this.mContext = (Context) Preconditions.checkNotNull(context);
            this.mContentContainer = FloatingToolbar.createContentContainer(context);
            this.mPopupWindow = FloatingToolbar.createPopupWindow(this.mContentContainer);
            this.mMarginHorizontal = parent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_horizontal_margin);
            this.mMarginVertical = parent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_vertical_margin);
            FloatingToolbar.mHwFloatingToolbarHeightBias = FloatingToolbar.getFloatingToolbarHeightBias(this.mContext);
            this.mLineHeight = context.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_height);
            this.mIconTextSpacing = context.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_menu_button_side_padding);
            this.mLogAccelerateInterpolator = new LogAccelerateInterpolator();
            this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.fast_out_slow_in);
            this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.linear_out_slow_in);
            this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.fast_out_linear_in);
            this.mArrow = this.mContext.getResources().getDrawable(R.drawable.ft_avd_tooverflow, this.mContext.getTheme());
            this.mArrow.setAutoMirrored(true);
            this.mOverflow = this.mContext.getResources().getDrawable(R.drawable.ft_avd_toarrow, this.mContext.getTheme());
            this.mOverflow.setAutoMirrored(true);
            this.mToArrow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(R.drawable.ft_avd_toarrow_animation, this.mContext.getTheme());
            this.mToArrow.setAutoMirrored(true);
            this.mToOverflow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(R.drawable.ft_avd_tooverflow_animation, this.mContext.getTheme());
            this.mToOverflow.setAutoMirrored(true);
            this.mOverflowButton = createOverflowButton();
            this.mOverflowButtonSize = measure(this.mOverflowButton);
            this.mMainPanel = createMainPanel();
            this.mOverflowPanelViewHelper = new OverflowPanelViewHelper(this.mContext);
            this.mOverflowPanel = createOverflowPanel();
            this.mOverflowAnimationListener = createOverflowAnimationListener();
            this.mOpenOverflowAnimation = new AnimationSet(true);
            this.mOpenOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
            this.mCloseOverflowAnimation = new AnimationSet(true);
            this.mCloseOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
            this.mShowAnimation = FloatingToolbar.createEnterAnimation(this.mContentContainer);
            this.mDismissAnimation = FloatingToolbar.createExitAnimation(this.mContentContainer, 0, new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    FloatingToolbarPopup.this.mPopupWindow.dismiss();
                    FloatingToolbarPopup.this.mContentContainer.removeAllViews();
                }
            });
            this.mHideAnimation = FloatingToolbar.createExitAnimation(this.mContentContainer, 0, new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    FloatingToolbarPopup.this.mPopupWindow.dismiss();
                }
            });
        }

        public void layoutMenuItems(List<MenuItem> menuItems, OnMenuItemClickListener menuItemClickListener, int suggestedWidth) {
            this.mOnMenuItemClickListener = menuItemClickListener;
            cancelOverflowAnimations();
            clearPanels();
            menuItems = layoutMainPanelItems(menuItems, getAdjustedToolbarWidth(suggestedWidth));
            if (!menuItems.isEmpty()) {
                layoutOverflowPanelItems(menuItems);
            }
            updatePopupSize();
        }

        public void show(Rect contentRectOnScreen) {
            Preconditions.checkNotNull(contentRectOnScreen);
            if (!isShowing()) {
                this.mHidden = false;
                this.mDismissed = false;
                cancelDismissAndHideAnimations();
                cancelOverflowAnimations();
                refreshCoordinatesAndOverflowDirection(contentRectOnScreen);
                preparePopupContent();
                this.mPopupWindow.showAtLocation(this.mParent, 0, this.mCoordsOnWindow.x, this.mCoordsOnWindow.y);
                setTouchableSurfaceInsetsComputer();
                runShowAnimation();
            }
        }

        public void dismiss() {
            if (!this.mDismissed) {
                this.mHidden = false;
                this.mDismissed = true;
                this.mHideAnimation.cancel();
                runDismissAnimation();
                setZeroTouchableSurface();
            }
        }

        public void hide() {
            if (isShowing()) {
                this.mHidden = true;
                runHideAnimation();
                setZeroTouchableSurface();
            }
        }

        public boolean isShowing() {
            return !this.mDismissed ? this.mHidden ^ 1 : false;
        }

        public boolean isHidden() {
            return this.mHidden;
        }

        public void updateCoordinates(Rect contentRectOnScreen) {
            Preconditions.checkNotNull(contentRectOnScreen);
            if (isShowing() && (this.mPopupWindow.isShowing() ^ 1) == 0) {
                cancelOverflowAnimations();
                refreshCoordinatesAndOverflowDirection(contentRectOnScreen);
                preparePopupContent();
                this.mPopupWindow.update(this.mCoordsOnWindow.x, this.mCoordsOnWindow.y, this.mPopupWindow.getWidth(), this.mPopupWindow.getHeight());
            }
        }

        private void refreshCoordinatesAndOverflowDirection(Rect contentRectOnScreen) {
            int y;
            refreshViewPort();
            int x = Math.min(contentRectOnScreen.centerX() - (this.mPopupWindow.getWidth() / 2), this.mViewPortOnScreen.right - this.mPopupWindow.getWidth());
            int statusBarHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
            if (this.mViewPortOnScreen.top < statusBarHeight) {
                this.mViewPortOnScreen.top = statusBarHeight;
            }
            int availableHeightAboveContent = contentRectOnScreen.top - this.mViewPortOnScreen.top;
            int availableHeightBelowContent = this.mViewPortOnScreen.bottom - contentRectOnScreen.bottom;
            int margin = this.mMarginVertical * 2;
            int toolbarHeightWithVerticalMargin = this.mLineHeight + margin;
            if (hasOverflow()) {
                int minimumOverflowHeightWithMargin = calculateOverflowHeight(2) + margin;
                int availableHeightThroughContentDown = (this.mViewPortOnScreen.bottom - contentRectOnScreen.top) + toolbarHeightWithVerticalMargin;
                int availableHeightThroughContentUp = (contentRectOnScreen.bottom - this.mViewPortOnScreen.top) + toolbarHeightWithVerticalMargin;
                if (availableHeightAboveContent >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightAboveContent - margin);
                    y = contentRectOnScreen.top - this.mPopupWindow.getHeight();
                    this.mOpenOverflowUpwards = true;
                } else if (availableHeightAboveContent >= toolbarHeightWithVerticalMargin && availableHeightThroughContentDown >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightThroughContentDown - margin);
                    y = contentRectOnScreen.top - toolbarHeightWithVerticalMargin;
                    this.mOpenOverflowUpwards = false;
                } else if (availableHeightBelowContent >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightBelowContent - margin);
                    y = contentRectOnScreen.bottom;
                    this.mOpenOverflowUpwards = false;
                } else if (availableHeightBelowContent < toolbarHeightWithVerticalMargin || this.mViewPortOnScreen.height() < minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(this.mViewPortOnScreen.height() - margin);
                    y = this.mViewPortOnScreen.top;
                    this.mOpenOverflowUpwards = false;
                } else {
                    updateOverflowHeight(availableHeightThroughContentUp - margin);
                    y = (contentRectOnScreen.bottom + toolbarHeightWithVerticalMargin) - this.mPopupWindow.getHeight();
                    this.mOpenOverflowUpwards = true;
                }
            } else if (availableHeightAboveContent >= toolbarHeightWithVerticalMargin) {
                y = contentRectOnScreen.top - toolbarHeightWithVerticalMargin;
            } else if (availableHeightBelowContent >= toolbarHeightWithVerticalMargin) {
                y = contentRectOnScreen.bottom;
            } else if (availableHeightBelowContent >= this.mLineHeight) {
                y = contentRectOnScreen.bottom - this.mMarginVertical;
            } else {
                y = Math.max(this.mViewPortOnScreen.top, contentRectOnScreen.top - toolbarHeightWithVerticalMargin);
            }
            this.mParent.getRootView().getLocationOnScreen(this.mTmpCoords);
            int rootViewLeftOnScreen = this.mTmpCoords[0];
            int rootViewTopOnScreen = this.mTmpCoords[1];
            this.mParent.getRootView().getLocationInWindow(this.mTmpCoords);
            this.mCoordsOnWindow.set(Math.max(0, x - (rootViewLeftOnScreen - this.mTmpCoords[0])), y - (rootViewTopOnScreen - this.mTmpCoords[1]));
        }

        private void runShowAnimation() {
            this.mShowAnimation.start();
        }

        private void runDismissAnimation() {
            this.mDismissAnimation.start();
        }

        private void runHideAnimation() {
            this.mHideAnimation.start();
        }

        private void cancelDismissAndHideAnimations() {
            this.mDismissAnimation.cancel();
            this.mHideAnimation.cancel();
        }

        private void cancelOverflowAnimations() {
            this.mContentContainer.clearAnimation();
            this.mMainPanel.animate().cancel();
            this.mOverflowPanel.animate().cancel();
            this.mToArrow.stop();
            this.mToOverflow.stop();
        }

        private void openOverflow() {
            float overflowButtonTargetX;
            final int targetWidth = this.mOverflowPanelSize.getWidth();
            final int targetHeight = this.mOverflowPanelSize.getHeight() + FloatingToolbar.mHwFloatingToolbarHeightBias;
            final int startWidth = this.mContentContainer.getWidth();
            final int startHeight = this.mContentContainer.getHeight();
            final float startY = this.mContentContainer.getY();
            final float left = this.mContentContainer.getX();
            final float right = left + ((float) this.mContentContainer.getWidth());
            Animation widthAnimation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    FloatingToolbarPopup.setWidth(FloatingToolbarPopup.this.mContentContainer, startWidth + ((int) (((float) (targetWidth - startWidth)) * interpolatedTime)));
                    if (FloatingToolbarPopup.this.isInRTLMode()) {
                        FloatingToolbarPopup.this.mContentContainer.setX(left);
                        FloatingToolbarPopup.this.mMainPanel.setX(0.0f);
                        FloatingToolbarPopup.this.mOverflowPanel.setX(0.0f);
                        return;
                    }
                    FloatingToolbarPopup.this.mContentContainer.setX(right - ((float) FloatingToolbarPopup.this.mContentContainer.getWidth()));
                    FloatingToolbarPopup.this.mMainPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - startWidth));
                    FloatingToolbarPopup.this.mOverflowPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - targetWidth));
                }
            };
            Animation heightAnimation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    FloatingToolbarPopup.setHeight(FloatingToolbarPopup.this.mContentContainer, startHeight + ((int) (((float) (targetHeight - startHeight)) * interpolatedTime)));
                    if (FloatingToolbarPopup.this.mOpenOverflowUpwards) {
                        FloatingToolbarPopup.this.mContentContainer.setY(startY - ((float) (FloatingToolbarPopup.this.mContentContainer.getHeight() - startHeight)));
                        FloatingToolbarPopup.this.positionContentYCoordinatesIfOpeningOverflowUpwards();
                    }
                }
            };
            final float overflowButtonStartX = this.mOverflowButton.getX();
            if (isInRTLMode()) {
                overflowButtonTargetX = (((float) targetWidth) + overflowButtonStartX) - ((float) this.mOverflowButton.getWidth());
            } else {
                overflowButtonTargetX = (overflowButtonStartX - ((float) targetWidth)) + ((float) this.mOverflowButton.getWidth());
            }
            Animation overflowButtonAnimation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    int i;
                    float overflowButtonX = overflowButtonStartX + ((overflowButtonTargetX - overflowButtonStartX) * interpolatedTime);
                    if (FloatingToolbarPopup.this.isInRTLMode()) {
                        i = 0;
                    } else {
                        i = FloatingToolbarPopup.this.mContentContainer.getWidth() - startWidth;
                    }
                    FloatingToolbarPopup.this.mOverflowButton.setX(overflowButtonX + ((float) i));
                }
            };
            widthAnimation.setInterpolator(this.mLogAccelerateInterpolator);
            widthAnimation.setDuration((long) getAdjustedDuration(250));
            heightAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            heightAnimation.setDuration((long) getAdjustedDuration(250));
            overflowButtonAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            overflowButtonAnimation.setDuration((long) getAdjustedDuration(250));
            this.mOpenOverflowAnimation.getAnimations().clear();
            this.mOpenOverflowAnimation.getAnimations().clear();
            this.mOpenOverflowAnimation.addAnimation(widthAnimation);
            this.mOpenOverflowAnimation.addAnimation(heightAnimation);
            this.mOpenOverflowAnimation.addAnimation(overflowButtonAnimation);
            this.mContentContainer.startAnimation(this.mOpenOverflowAnimation);
            this.mIsOverflowOpen = true;
            this.mMainPanel.animate().alpha(0.0f).withLayer().setInterpolator(this.mLinearOutSlowInInterpolator).setDuration(250).start();
            this.mOverflowPanel.setAlpha(1.0f);
        }

        private void closeOverflow() {
            float overflowButtonTargetX;
            final int targetWidth = this.mMainPanelSize.getWidth();
            final int startWidth = this.mContentContainer.getWidth();
            final float left = this.mContentContainer.getX();
            final float right = left + ((float) this.mContentContainer.getWidth());
            Animation widthAnimation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    FloatingToolbarPopup.setWidth(FloatingToolbarPopup.this.mContentContainer, startWidth + ((int) (((float) (targetWidth - startWidth)) * interpolatedTime)));
                    if (FloatingToolbarPopup.this.isInRTLMode()) {
                        FloatingToolbarPopup.this.mContentContainer.setX(left);
                        FloatingToolbarPopup.this.mMainPanel.setX(0.0f);
                        FloatingToolbarPopup.this.mOverflowPanel.setX(0.0f);
                        return;
                    }
                    FloatingToolbarPopup.this.mContentContainer.setX(right - ((float) FloatingToolbarPopup.this.mContentContainer.getWidth()));
                    FloatingToolbarPopup.this.mMainPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - targetWidth));
                    FloatingToolbarPopup.this.mOverflowPanel.setX((float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - startWidth));
                }
            };
            final int targetHeight = this.mMainPanelSize.getHeight() + FloatingToolbar.mHwFloatingToolbarHeightBias;
            final int startHeight = this.mContentContainer.getHeight();
            final float bottom = this.mContentContainer.getY() + ((float) this.mContentContainer.getHeight());
            Animation heightAnimation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    FloatingToolbarPopup.setHeight(FloatingToolbarPopup.this.mContentContainer, startHeight + ((int) (((float) (targetHeight - startHeight)) * interpolatedTime)));
                    if (FloatingToolbarPopup.this.mOpenOverflowUpwards) {
                        FloatingToolbarPopup.this.mContentContainer.setY(bottom - ((float) FloatingToolbarPopup.this.mContentContainer.getHeight()));
                        FloatingToolbarPopup.this.positionContentYCoordinatesIfOpeningOverflowUpwards();
                    }
                }
            };
            final float overflowButtonStartX = this.mOverflowButton.getX();
            if (isInRTLMode()) {
                overflowButtonTargetX = (overflowButtonStartX - ((float) startWidth)) + ((float) this.mOverflowButton.getWidth());
            } else {
                overflowButtonTargetX = (((float) startWidth) + overflowButtonStartX) - ((float) this.mOverflowButton.getWidth());
            }
            Animation overflowButtonAnimation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    int i;
                    float overflowButtonX = overflowButtonStartX + ((overflowButtonTargetX - overflowButtonStartX) * interpolatedTime);
                    if (FloatingToolbarPopup.this.isInRTLMode()) {
                        i = 0;
                    } else {
                        i = FloatingToolbarPopup.this.mContentContainer.getWidth() - startWidth;
                    }
                    FloatingToolbarPopup.this.mOverflowButton.setX(overflowButtonX + ((float) i));
                }
            };
            widthAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            widthAnimation.setDuration((long) getAdjustedDuration(250));
            heightAnimation.setInterpolator(this.mLogAccelerateInterpolator);
            heightAnimation.setDuration((long) getAdjustedDuration(250));
            overflowButtonAnimation.setInterpolator(this.mFastOutSlowInInterpolator);
            overflowButtonAnimation.setDuration((long) getAdjustedDuration(250));
            this.mCloseOverflowAnimation.getAnimations().clear();
            this.mCloseOverflowAnimation.addAnimation(widthAnimation);
            this.mCloseOverflowAnimation.addAnimation(heightAnimation);
            this.mCloseOverflowAnimation.addAnimation(overflowButtonAnimation);
            this.mContentContainer.startAnimation(this.mCloseOverflowAnimation);
            this.mIsOverflowOpen = false;
            this.mMainPanel.animate().alpha(1.0f).withLayer().setInterpolator(this.mFastOutLinearInInterpolator).setDuration(100).start();
            this.mOverflowPanel.animate().alpha(0.0f).withLayer().setInterpolator(this.mLinearOutSlowInInterpolator).setDuration(150).start();
        }

        private void setPanelsStatesAtRestingPosition() {
            this.mOverflowButton.setEnabled(true);
            this.mOverflowPanel.awakenScrollBars();
            Size containerSize;
            if (this.mIsOverflowOpen) {
                containerSize = new Size(this.mOverflowPanelSize.getWidth(), this.mOverflowPanelSize.getHeight() + FloatingToolbar.mHwFloatingToolbarHeightBias);
                setSize(this.mContentContainer, containerSize);
                this.mMainPanel.setAlpha(0.0f);
                this.mMainPanel.setVisibility(4);
                this.mOverflowPanel.setAlpha(1.0f);
                this.mOverflowPanel.setVisibility(0);
                this.mOverflowButton.setImageDrawable(this.mArrow);
                this.mOverflowButton.setContentDescription(this.mContext.getString(R.string.floating_toolbar_close_overflow_description));
                if (isInRTLMode()) {
                    this.mContentContainer.setX((float) this.mMarginHorizontal);
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX((float) (containerSize.getWidth() - this.mOverflowButtonSize.getWidth()));
                    this.mOverflowPanel.setX(0.0f);
                } else {
                    this.mContentContainer.setX((float) ((this.mPopupWindow.getWidth() - containerSize.getWidth()) - this.mMarginHorizontal));
                    this.mMainPanel.setX(-this.mContentContainer.getX());
                    this.mOverflowButton.setX(0.0f);
                    this.mOverflowPanel.setX(0.0f);
                }
                if (this.mOpenOverflowUpwards) {
                    this.mContentContainer.setY((float) this.mMarginVertical);
                    this.mMainPanel.setY((float) (containerSize.getHeight() - this.mContentContainer.getHeight()));
                    this.mOverflowButton.setY((float) ((containerSize.getHeight() - this.mOverflowButtonSize.getHeight()) - FloatingToolbar.mHwFloatingToolbarHeightBias));
                    this.mOverflowPanel.setY(0.0f);
                    return;
                }
                this.mContentContainer.setY((float) this.mMarginVertical);
                this.mMainPanel.setY(0.0f);
                this.mOverflowButton.setY(0.0f);
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
                return;
            }
            containerSize = new Size(this.mMainPanelSize.getWidth(), this.mMainPanelSize.getHeight() + FloatingToolbar.mHwFloatingToolbarHeightBias);
            setSize(this.mContentContainer, containerSize);
            this.mMainPanel.setAlpha(1.0f);
            this.mMainPanel.setVisibility(0);
            this.mOverflowPanel.setAlpha(0.0f);
            this.mOverflowPanel.setVisibility(4);
            this.mOverflowButton.setImageDrawable(this.mOverflow);
            this.mOverflowButton.setContentDescription(this.mContext.getString(R.string.floating_toolbar_open_overflow_description));
            if (hasOverflow()) {
                if (isInRTLMode()) {
                    this.mContentContainer.setX((float) this.mMarginHorizontal);
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX(0.0f);
                    this.mOverflowPanel.setX(0.0f);
                } else {
                    this.mContentContainer.setX((float) ((this.mPopupWindow.getWidth() - containerSize.getWidth()) - this.mMarginHorizontal));
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX((float) (containerSize.getWidth() - this.mOverflowButtonSize.getWidth()));
                    this.mOverflowPanel.setX((float) (containerSize.getWidth() - this.mOverflowPanelSize.getWidth()));
                }
                if (this.mOpenOverflowUpwards) {
                    this.mContentContainer.setY((float) (((this.mMarginVertical + this.mOverflowPanelSize.getHeight()) - containerSize.getHeight()) + FloatingToolbar.mHwFloatingToolbarHeightBias));
                    this.mMainPanel.setY(0.0f);
                    this.mOverflowButton.setY(0.0f);
                    this.mOverflowPanel.setY((float) (containerSize.getHeight() - this.mOverflowPanelSize.getHeight()));
                    return;
                }
                this.mContentContainer.setY((float) this.mMarginVertical);
                this.mMainPanel.setY(0.0f);
                this.mOverflowButton.setY(0.0f);
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
                return;
            }
            this.mContentContainer.setX((float) this.mMarginHorizontal);
            this.mContentContainer.setY((float) this.mMarginVertical);
            this.mMainPanel.setX(0.0f);
            this.mMainPanel.setY(0.0f);
        }

        private void updateOverflowHeight(int suggestedHeight) {
            if (hasOverflow()) {
                int newHeight = calculateOverflowHeight((suggestedHeight - this.mOverflowButtonSize.getHeight()) / this.mLineHeight);
                if (this.mOverflowPanelSize.getHeight() != newHeight) {
                    this.mOverflowPanelSize = new Size(this.mOverflowPanelSize.getWidth(), newHeight);
                }
                setSize(this.mOverflowPanel, this.mOverflowPanelSize);
                if (this.mIsOverflowOpen) {
                    setSize(this.mContentContainer, this.mOverflowPanelSize);
                    if (this.mOpenOverflowUpwards) {
                        int deltaHeight = this.mOverflowPanelSize.getHeight() - newHeight;
                        this.mContentContainer.setY(this.mContentContainer.getY() + ((float) deltaHeight));
                        this.mOverflowButton.setY(this.mOverflowButton.getY() - ((float) deltaHeight));
                    }
                } else {
                    setSize(this.mContentContainer, this.mMainPanelSize);
                }
                updatePopupSize();
            }
        }

        private void updatePopupSize() {
            int width = 0;
            int height = 0;
            if (this.mMainPanelSize != null) {
                width = Math.max(0, this.mMainPanelSize.getWidth());
                height = Math.max(0, this.mMainPanelSize.getHeight());
            }
            if (this.mOverflowPanelSize != null) {
                width = Math.max(width, this.mOverflowPanelSize.getWidth());
                height = Math.max(height, this.mOverflowPanelSize.getHeight());
            }
            this.mPopupWindow.setWidth((this.mMarginHorizontal * 2) + width);
            this.mPopupWindow.setHeight((this.mMarginVertical * 2) + height);
            maybeComputeTransitionDurationScale();
        }

        private void refreshViewPort() {
            this.mParent.getWindowVisibleDisplayFrame(this.mViewPortOnScreen);
            FloatingToolbar.adjustWindowVisibleDisplayFrame(this.mViewPortOnScreen);
        }

        private int getAdjustedToolbarWidth(int suggestedWidth) {
            int width = suggestedWidth;
            refreshViewPort();
            int maximumWidth = this.mViewPortOnScreen.width() - (this.mParent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_horizontal_margin) * 2);
            if (suggestedWidth <= 0) {
                width = this.mParent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_preferred_width);
            }
            return Math.min(width, maximumWidth);
        }

        private void setZeroTouchableSurface() {
            this.mTouchableRegion.setEmpty();
        }

        private void setContentAreaAsTouchableSurface() {
            int width;
            int height;
            Preconditions.checkNotNull(this.mMainPanelSize);
            if (this.mIsOverflowOpen) {
                Preconditions.checkNotNull(this.mOverflowPanelSize);
                width = this.mOverflowPanelSize.getWidth();
                height = this.mOverflowPanelSize.getHeight();
            } else {
                width = this.mMainPanelSize.getWidth();
                height = this.mMainPanelSize.getHeight();
            }
            this.mTouchableRegion.set((int) this.mContentContainer.getX(), (int) this.mContentContainer.getY(), ((int) this.mContentContainer.getX()) + width, ((int) this.mContentContainer.getY()) + height);
        }

        private void setTouchableSurfaceInsetsComputer() {
            ViewTreeObserver viewTreeObserver = this.mPopupWindow.getContentView().getRootView().getViewTreeObserver();
            viewTreeObserver.removeOnComputeInternalInsetsListener(this.mInsetsComputer);
            viewTreeObserver.addOnComputeInternalInsetsListener(this.mInsetsComputer);
        }

        private boolean isInRTLMode() {
            if (this.mContext.getApplicationInfo().hasRtlSupport()) {
                return this.mContext.getResources().getConfiguration().getLayoutDirection() == 1;
            } else {
                return false;
            }
        }

        private boolean hasOverflow() {
            return this.mOverflowPanelSize != null;
        }

        public List<MenuItem> layoutMainPanelItems(List<MenuItem> menuItems, int toolbarWidth) {
            MenuItem menuItem;
            Preconditions.checkNotNull(menuItems);
            int availableWidth = toolbarWidth;
            LinkedList<MenuItem> remainingMenuItems = new LinkedList();
            LinkedList<MenuItem> overflowMenuItems = new LinkedList();
            for (MenuItem menuItem2 : menuItems) {
                if (menuItem2.requiresOverflow()) {
                    overflowMenuItems.add(menuItem2);
                } else {
                    remainingMenuItems.add(menuItem2);
                }
            }
            remainingMenuItems.addAll(overflowMenuItems);
            this.mMainPanel.removeAllViews();
            this.mMainPanel.setPaddingRelative(0, 0, 0, 0);
            int lastGroupId = -1;
            boolean isFirstItem = true;
            while (!remainingMenuItems.isEmpty()) {
                menuItem2 = (MenuItem) remainingMenuItems.peek();
                if (!isFirstItem && menuItem2.requiresOverflow()) {
                    break;
                }
                View menuItemButton = FloatingToolbar.createMenuItemButton(this.mContext, menuItem2, this.mIconTextSpacing);
                if (isFirstItem) {
                    if (remainingMenuItems.size() == 1) {
                        menuItemButton.setBackgroundResource(33751246);
                    } else if (isInRTLMode()) {
                        menuItemButton.setBackgroundResource(33751245);
                    } else {
                        menuItemButton.setBackgroundResource(33751244);
                    }
                } else if (remainingMenuItems.size() == 1) {
                    if (isInRTLMode()) {
                        menuItemButton.setBackgroundResource(33751244);
                    } else {
                        menuItemButton.setBackgroundResource(33751245);
                    }
                }
                if (isFirstItem) {
                    menuItemButton.setPaddingRelative((int) (((double) menuItemButton.getPaddingStart()) * 1.5d), menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd(), menuItemButton.getPaddingBottom());
                }
                boolean isLastItem = remainingMenuItems.size() == 1;
                if (isLastItem) {
                    menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart(), menuItemButton.getPaddingTop(), (int) (((double) menuItemButton.getPaddingEnd()) * 1.5d), menuItemButton.getPaddingBottom());
                }
                menuItemButton.measure(0, 0);
                int menuItemButtonWidth = Math.min(menuItemButton.getMeasuredWidth(), toolbarWidth);
                boolean isNewGroup = (isFirstItem || lastGroupId == menuItem2.getGroupId()) ? false : true;
                int extraPadding = isNewGroup ? menuItemButton.getPaddingEnd() * 2 : 0;
                boolean canFitWithOverflow = menuItemButtonWidth <= (availableWidth - this.mOverflowButtonSize.getWidth()) - extraPadding;
                boolean canFitNoOverflow = isLastItem && menuItemButtonWidth <= availableWidth - extraPadding;
                if (!canFitWithOverflow && !canFitNoOverflow) {
                    break;
                }
                if (isNewGroup) {
                    View divider = FloatingToolbar.createDivider(this.mContext);
                    int dividerWidth = divider.getLayoutParams().width;
                    View previousButton = this.mMainPanel.getChildAt(this.mMainPanel.getChildCount() - 1);
                    previousButton.setPaddingRelative(previousButton.getPaddingStart(), previousButton.getPaddingTop(), (previousButton.getPaddingEnd() + (extraPadding / 2)) - dividerWidth, previousButton.getPaddingBottom());
                    LayoutParams prevParams = previousButton.getLayoutParams();
                    prevParams.width += (extraPadding / 2) - dividerWidth;
                    previousButton.-wrap18(prevParams);
                    menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart() + (extraPadding / 2), menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd(), menuItemButton.getPaddingBottom());
                    this.mMainPanel.addView(divider);
                }
                setButtonTagAndClickListener(menuItemButton, menuItem2);
                menuItemButton.setTooltipText(menuItem2.getTooltipText());
                this.mMainPanel.addView(menuItemButton);
                LayoutParams params = menuItemButton.getLayoutParams();
                params.width = (extraPadding / 2) + menuItemButtonWidth;
                menuItemButton.-wrap18(params);
                availableWidth -= menuItemButtonWidth + extraPadding;
                remainingMenuItems.pop();
                lastGroupId = menuItem2.getGroupId();
                isFirstItem = false;
            }
            if (!remainingMenuItems.isEmpty()) {
                this.mMainPanel.setPaddingRelative(0, 0, this.mOverflowButtonSize.getWidth(), 0);
            }
            this.mMainPanelSize = measure(this.mMainPanel);
            return remainingMenuItems;
        }

        private void layoutOverflowPanelItems(List<MenuItem> menuItems) {
            ArrayAdapter<MenuItem> overflowPanelAdapter = (ArrayAdapter) this.mOverflowPanel.getAdapter();
            overflowPanelAdapter.clear();
            int size = menuItems.size();
            for (int i = 0; i < size; i++) {
                overflowPanelAdapter.add((MenuItem) menuItems.get(i));
            }
            this.mOverflowPanel.setAdapter(overflowPanelAdapter);
            if (this.mOpenOverflowUpwards) {
                this.mOverflowPanel.setY(0.0f);
            } else {
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
            }
            this.mOverflowPanelSize = new Size(Math.max(getOverflowWidth(), this.mOverflowButtonSize.getWidth()), calculateOverflowHeight(4));
            setSize(this.mOverflowPanel, this.mOverflowPanelSize);
        }

        private void preparePopupContent() {
            this.mContentContainer.removeAllViews();
            if (hasOverflow()) {
                this.mContentContainer.addView(this.mOverflowPanel);
            }
            this.mContentContainer.addView(this.mMainPanel);
            if (hasOverflow()) {
                this.mContentContainer.addView(this.mOverflowButton);
            }
            setPanelsStatesAtRestingPosition();
            setContentAreaAsTouchableSurface();
            if (isInRTLMode()) {
                this.mContentContainer.setAlpha(0.0f);
                this.mContentContainer.post(this.mPreparePopupContentRTLHelper);
            }
        }

        private void clearPanels() {
            this.mOverflowPanelSize = null;
            this.mMainPanelSize = null;
            this.mIsOverflowOpen = false;
            this.mMainPanel.removeAllViews();
            ArrayAdapter<MenuItem> overflowPanelAdapter = (ArrayAdapter) this.mOverflowPanel.getAdapter();
            overflowPanelAdapter.clear();
            this.mOverflowPanel.setAdapter(overflowPanelAdapter);
            this.mContentContainer.removeAllViews();
        }

        private void positionContentYCoordinatesIfOpeningOverflowUpwards() {
            if (this.mOpenOverflowUpwards) {
                this.mMainPanel.setY((float) ((this.mContentContainer.getHeight() - this.mMainPanelSize.getHeight()) - FloatingToolbar.mHwFloatingToolbarHeightBias));
                this.mOverflowButton.setY((float) ((this.mContentContainer.getHeight() - this.mOverflowButton.getHeight()) - FloatingToolbar.mHwFloatingToolbarHeightBias));
                this.mOverflowPanel.setY((float) ((this.mContentContainer.getHeight() - this.mOverflowPanelSize.getHeight()) - FloatingToolbar.mHwFloatingToolbarHeightBias));
            }
        }

        private int getOverflowWidth() {
            int overflowWidth = 0;
            int count = this.mOverflowPanel.getAdapter().getCount();
            for (int i = 0; i < count; i++) {
                overflowWidth = Math.max(this.mOverflowPanelViewHelper.calculateWidth((MenuItem) this.mOverflowPanel.getAdapter().getItem(i)), overflowWidth);
            }
            return overflowWidth;
        }

        private int calculateOverflowHeight(int maxItemSize) {
            int actualSize = Math.min(4, Math.min(Math.max(2, maxItemSize), this.mOverflowPanel.getCount()));
            int extension = 0;
            if (actualSize < this.mOverflowPanel.getCount()) {
                extension = (int) (((float) this.mLineHeight) * 0.5f);
            }
            return ((this.mLineHeight * actualSize) + this.mOverflowButtonSize.getHeight()) + extension;
        }

        private void setButtonTagAndClickListener(View menuItemButton, MenuItem menuItem) {
            menuItemButton.setTag(menuItem);
            menuItemButton.setOnClickListener(this.mMenuItemButtonOnClickListener);
        }

        private int getAdjustedDuration(int originalDuration) {
            if (this.mTransitionDurationScale < 150) {
                return Math.max(originalDuration - 50, 0);
            }
            if (this.mTransitionDurationScale > 300) {
                return originalDuration + 50;
            }
            return (int) (((float) originalDuration) * ValueAnimator.getDurationScale());
        }

        private void maybeComputeTransitionDurationScale() {
            if (this.mMainPanelSize != null && this.mOverflowPanelSize != null) {
                int w = this.mMainPanelSize.getWidth() - this.mOverflowPanelSize.getWidth();
                int h = this.mOverflowPanelSize.getHeight() - this.mMainPanelSize.getHeight();
                this.mTransitionDurationScale = (int) (Math.sqrt((double) ((w * w) + (h * h))) / ((double) this.mContentContainer.getContext().getResources().getDisplayMetrics().density));
            }
        }

        private ViewGroup createMainPanel() {
            return new LinearLayout(this.mContext) {
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    if (FloatingToolbarPopup.this.isOverflowAnimating()) {
                        widthMeasureSpec = MeasureSpec.makeMeasureSpec(FloatingToolbarPopup.this.mMainPanelSize.getWidth(), 1073741824);
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }

                public boolean onInterceptTouchEvent(MotionEvent ev) {
                    return FloatingToolbarPopup.this.isOverflowAnimating();
                }
            };
        }

        private ImageButton createOverflowButton() {
            ImageButton overflowButton = (ImageButton) LayoutInflater.from(this.mContext).inflate((int) R.layout.floating_popup_overflow_button, null);
            overflowButton.setImageDrawable(this.mOverflow);
            overflowButton.setOnClickListener(new com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s.AnonymousClass3(this, overflowButton));
            return overflowButton;
        }

        /* synthetic */ void lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup_72553(ImageButton overflowButton, View v) {
            if (this.mIsOverflowOpen) {
                overflowButton.setImageDrawable(this.mToOverflow);
                this.mToOverflow.start();
                closeOverflow();
                return;
            }
            overflowButton.setImageDrawable(this.mToArrow);
            this.mToArrow.start();
            openOverflow();
        }

        private OverflowPanel createOverflowPanel() {
            OverflowPanel overflowPanel = new OverflowPanel(this);
            overflowPanel.-wrap18(new LayoutParams(-1, -1));
            overflowPanel.setDivider(null);
            overflowPanel.setDividerHeight(0);
            overflowPanel.setAdapter(new ArrayAdapter<MenuItem>(this.mContext, 0) {
                public View getView(int position, View convertView, ViewGroup parent) {
                    return FloatingToolbarPopup.this.mOverflowPanelViewHelper.getView((MenuItem) getItem(position), FloatingToolbarPopup.this.mOverflowPanelSize.getWidth(), convertView);
                }
            });
            overflowPanel.setOnItemClickListener(new com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s.AnonymousClass4(this, overflowPanel));
            return overflowPanel;
        }

        /* synthetic */ void lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup_73918(OverflowPanel overflowPanel, AdapterView adapterView, View view, int position, long id) {
            MenuItem menuItem = (MenuItem) overflowPanel.getAdapter().getItem(position);
            if (this.mOnMenuItemClickListener != null) {
                this.mOnMenuItemClickListener.onMenuItemClick(menuItem);
            }
        }

        private boolean isOverflowAnimating() {
            int overflowOpening;
            boolean overflowClosing;
            if (this.mOpenOverflowAnimation.hasStarted()) {
                overflowOpening = this.mOpenOverflowAnimation.hasEnded() ^ 1;
            } else {
                overflowOpening = 0;
            }
            if (this.mCloseOverflowAnimation.hasStarted()) {
                overflowClosing = this.mCloseOverflowAnimation.hasEnded() ^ 1;
            } else {
                overflowClosing = false;
            }
            return overflowOpening == 0 ? overflowClosing : true;
        }

        private AnimationListener createOverflowAnimationListener() {
            return new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    FloatingToolbarPopup.this.mOverflowButton.setEnabled(false);
                    FloatingToolbarPopup.this.mMainPanel.setVisibility(0);
                    FloatingToolbarPopup.this.mOverflowPanel.setVisibility(0);
                }

                public void onAnimationEnd(Animation animation) {
                    FloatingToolbarPopup.this.mContentContainer.post(new com.android.internal.widget.-$Lambda$nZD8NeHZxo4kFQHu5zIWiAfZj2s.AnonymousClass2(this));
                }

                /* synthetic */ void lambda$-com_android_internal_widget_FloatingToolbar$FloatingToolbarPopup$13_75644() {
                    FloatingToolbarPopup.this.setPanelsStatesAtRestingPosition();
                    FloatingToolbarPopup.this.setContentAreaAsTouchableSurface();
                }

                public void onAnimationRepeat(Animation animation) {
                }
            };
        }

        private static Size measure(View view) {
            boolean z;
            if (view.getParent() == null) {
                z = true;
            } else {
                z = false;
            }
            Preconditions.checkState(z);
            view.measure(0, 0);
            return new Size(view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        private static void setSize(View view, int width, int height) {
            view.setMinimumWidth(width);
            view.setMinimumHeight(height);
            LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new LayoutParams(0, 0);
            }
            params.width = width;
            params.height = height;
            view.-wrap18(params);
        }

        private static void setSize(View view, Size size) {
            setSize(view, size.getWidth(), size.getHeight());
        }

        private static void setWidth(View view, int width) {
            setSize(view, width, view.getLayoutParams().height);
        }

        private static void setHeight(View view, int height) {
            setSize(view, view.getLayoutParams().width, height);
        }
    }

    public FloatingToolbar(Context context, Window window) {
        this(context, window, false);
    }

    public FloatingToolbar(Context context, Window window, boolean flag) {
        this.mContentRect = new Rect();
        this.mPreviousContentRect = new Rect();
        this.mShowingMenuItems = new ArrayList();
        this.mMenuItemClickListener = NO_OP_MENUITEM_CLICK_LISTENER;
        this.mWidthChanged = true;
        this.mOrientationChangeHandler = new OnLayoutChangeListener() {
            private final Rect mNewRect = new Rect();
            private final Rect mOldRect = new Rect();

            public void onLayoutChange(View view, int newLeft, int newRight, int newTop, int newBottom, int oldLeft, int oldRight, int oldTop, int oldBottom) {
                this.mNewRect.set(newLeft, newRight, newTop, newBottom);
                this.mOldRect.set(oldLeft, oldRight, oldTop, oldBottom);
                if (FloatingToolbar.this.mPopup.isShowing() && (this.mNewRect.equals(this.mOldRect) ^ 1) != 0) {
                    FloatingToolbar.this.mWidthChanged = true;
                    FloatingToolbar.this.updateLayout();
                }
            }
        };
        mHwFloatingToolbarFlag[0] = flag;
        this.mContext = applyDefaultTheme((Context) Preconditions.checkNotNull(context));
        this.mWindow = (Window) Preconditions.checkNotNull(window);
        this.mPopup = new FloatingToolbarPopup(this.mContext, window.getDecorView());
    }

    public FloatingToolbar setMenu(Menu menu) {
        this.mMenu = (Menu) Preconditions.checkNotNull(menu);
        return this;
    }

    public FloatingToolbar setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        if (menuItemClickListener != null) {
            this.mMenuItemClickListener = menuItemClickListener;
        } else {
            this.mMenuItemClickListener = NO_OP_MENUITEM_CLICK_LISTENER;
        }
        return this;
    }

    public FloatingToolbar setContentRect(Rect rect) {
        this.mContentRect.set((Rect) Preconditions.checkNotNull(rect));
        return this;
    }

    public FloatingToolbar setSuggestedWidth(int suggestedWidth) {
        this.mWidthChanged = ((double) Math.abs(suggestedWidth - this.mSuggestedWidth)) > ((double) this.mSuggestedWidth) * 0.2d;
        this.mSuggestedWidth = suggestedWidth;
        return this;
    }

    public FloatingToolbar show() {
        registerOrientationHandler();
        doShow();
        return this;
    }

    public FloatingToolbar updateLayout() {
        if (this.mPopup.isShowing()) {
            doShow();
        }
        return this;
    }

    public void dismiss() {
        unregisterOrientationHandler();
        this.mPopup.dismiss();
    }

    public void hide() {
        this.mPopup.hide();
    }

    public boolean isShowing() {
        return this.mPopup.isShowing();
    }

    public boolean isHidden() {
        return this.mPopup.isHidden();
    }

    private void doShow() {
        List<MenuItem> menuItems = getVisibleAndEnabledMenuItems(this.mMenu);
        Object selectAll = null;
        String webviewSelectAllName = "id/select_action_menu_select_all";
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem mi = (MenuItem) menuItems.get(i);
            int itemId = mi.getItemId();
            if (i != 0) {
                try {
                    if (this.mContext != null && (itemId >> 24) == 3 && this.mContext.getResources().getResourceName(itemId).indexOf(webviewSelectAllName) >= 0) {
                        selectAll = mi;
                    }
                } catch (NotFoundException e) {
                    Log.w(TAG, "Resources.NotFoundException id = " + itemId);
                }
            }
        }
        if (selectAll != null) {
            menuItems.remove(selectAll);
            menuItems.add(0, selectAll);
        }
        tidy(menuItems);
        if (!isCurrentlyShowing(menuItems) || this.mWidthChanged) {
            this.mPopup.dismiss();
            this.mPopup.layoutMenuItems(menuItems, this.mMenuItemClickListener, this.mSuggestedWidth);
            this.mShowingMenuItems = menuItems;
        }
        if (!this.mPopup.isShowing()) {
            this.mPopup.show(this.mContentRect);
        } else if (!this.mPreviousContentRect.equals(this.mContentRect)) {
            this.mPopup.updateCoordinates(this.mContentRect);
        }
        this.mWidthChanged = false;
        this.mPreviousContentRect.set(this.mContentRect);
    }

    private boolean isCurrentlyShowing(List<MenuItem> menuItems) {
        if (this.mShowingMenuItems == null || menuItems.size() != this.mShowingMenuItems.size()) {
            return false;
        }
        int size = menuItems.size();
        for (int i = 0; i < size; i++) {
            MenuItem menuItem = (MenuItem) menuItems.get(i);
            MenuItem showingItem = (MenuItem) this.mShowingMenuItems.get(i);
            if (menuItem.getItemId() != showingItem.getItemId() || (TextUtils.equals(menuItem.getTitle(), showingItem.getTitle()) ^ 1) != 0 || (Objects.equals(menuItem.getIcon(), showingItem.getIcon()) ^ 1) != 0 || menuItem.getGroupId() != showingItem.getGroupId()) {
                return false;
            }
        }
        return true;
    }

    private List<MenuItem> getVisibleAndEnabledMenuItems(Menu menu) {
        List<MenuItem> menuItems = new ArrayList();
        int i = 0;
        while (menu != null && i < menu.size()) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.isVisible() && menuItem.isEnabled()) {
                Menu subMenu = menuItem.getSubMenu();
                if (subMenu != null) {
                    menuItems.addAll(getVisibleAndEnabledMenuItems(subMenu));
                } else {
                    menuItems.add(menuItem);
                }
            }
            i++;
        }
        return menuItems;
    }

    private void tidy(List<MenuItem> menuItems) {
        int assistItemIndex = -1;
        Drawable assistItemDrawable = null;
        int size = menuItems.size();
        for (int i = 0; i < size; i++) {
            MenuItem menuItem = (MenuItem) menuItems.get(i);
            if (menuItem.getItemId() == R.id.textAssist) {
                assistItemIndex = i;
                assistItemDrawable = menuItem.getIcon();
            }
            if (!TextUtils.isEmpty(menuItem.getTitle())) {
                menuItem.setIcon(null);
            }
        }
        if (assistItemIndex > -1) {
            MenuItem assistMenuItem = (MenuItem) menuItems.remove(assistItemIndex);
            assistMenuItem.setIcon(assistItemDrawable);
            menuItems.add(0, assistMenuItem);
        }
    }

    private void registerOrientationHandler() {
        unregisterOrientationHandler();
        this.mWindow.getDecorView().addOnLayoutChangeListener(this.mOrientationChangeHandler);
    }

    private void unregisterOrientationHandler() {
        this.mWindow.getDecorView().removeOnLayoutChangeListener(this.mOrientationChangeHandler);
    }

    private static View createMenuItemButton(Context context, MenuItem menuItem, int iconTextSpacing) {
        View menuItemButton = LayoutInflater.from(context).inflate((int) R.layout.floating_popup_menu_button, null);
        if (menuItem != null) {
            updateMenuItemButton(menuItemButton, menuItem, iconTextSpacing);
        }
        return menuItemButton;
    }

    private static void updateMenuItemButton(View menuItemButton, MenuItem menuItem, int iconTextSpacing) {
        TextView buttonText = (TextView) menuItemButton.findViewById(R.id.floating_toolbar_menu_item_text);
        if (TextUtils.isEmpty(menuItem.getTitle())) {
            buttonText.setVisibility(8);
        } else {
            buttonText.setVisibility(0);
            buttonText.setText(menuItem.getTitle());
            setMenuItemButtonInputType(buttonText);
        }
        ImageView buttonIcon = (ImageView) menuItemButton.findViewById(R.id.floating_toolbar_menu_item_image);
        if (menuItem.getIcon() == null) {
            buttonIcon.setVisibility(8);
            if (buttonText != null) {
                buttonText.setPaddingRelative(0, 0, 0, 0);
            }
        } else {
            buttonIcon.setVisibility(0);
            buttonIcon.setImageDrawable(menuItem.getIcon());
            if (buttonText != null) {
                buttonText.setPaddingRelative(iconTextSpacing, 0, 0, 0);
            }
        }
        CharSequence contentDescription = menuItem.getContentDescription();
        if (TextUtils.isEmpty(contentDescription)) {
            menuItemButton.setContentDescription(menuItem.getTitle());
        } else {
            menuItemButton.setContentDescription(contentDescription);
        }
    }

    private static ViewGroup createContentContainer(Context context) {
        ViewGroup contentContainer = (ViewGroup) LayoutInflater.from(context).inflate((int) R.layout.floating_popup_container, null);
        if (mHwFloatingToolbarFlag[0]) {
            contentContainer.setElevation(0.0f);
        }
        contentContainer.-wrap18(new LayoutParams(-2, -2));
        contentContainer.setTag(FLOATING_TOOLBAR_TAG);
        return contentContainer;
    }

    private static PopupWindow createPopupWindow(ViewGroup content) {
        View popupContentHolder = new LinearLayout(content.getContext());
        PopupWindow popupWindow = new PopupWindow(popupContentHolder);
        popupWindow.setClippingEnabled(false);
        popupWindow.setWindowLayoutType(1005);
        popupWindow.setAnimationStyle(0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        content.-wrap18(new LayoutParams(-2, -2));
        popupContentHolder.addView(content);
        return popupWindow;
    }

    private static View createDivider(Context context) {
        View divider = new View(context);
        int _1dp = (int) TypedValue.applyDimension(1, 1.0f, context.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(_1dp, -1);
        params.setMarginsRelative(0, _1dp * 10, 0, _1dp * 10);
        divider.-wrap18(params);
        TypedArray a = context.obtainStyledAttributes(new TypedValue().data, new int[]{R.attr.floatingToolbarDividerColor});
        divider.setBackgroundColor(a.getColor(0, 0));
        a.recycle();
        divider.setImportantForAccessibility(2);
        divider.setEnabled(false);
        divider.setFocusable(false);
        divider.setContentDescription(null);
        return divider;
    }

    private static AnimatorSet createEnterAnimation(View view) {
        AnimatorSet animation = new AnimatorSet();
        Animator[] animatorArr = new Animator[1];
        animatorArr[0] = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{0.0f, 1.0f}).setDuration(150);
        animation.playTogether(animatorArr);
        return animation;
    }

    private static AnimatorSet createExitAnimation(View view, int startDelay, AnimatorListener listener) {
        AnimatorSet animation = new AnimatorSet();
        Animator[] animatorArr = new Animator[1];
        animatorArr[0] = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{1.0f, 0.0f}).setDuration(0);
        animation.playTogether(animatorArr);
        animation.setStartDelay((long) startDelay);
        animation.addListener(listener);
        return animation;
    }

    private static Context applyDefaultTheme(Context originalContext) {
        return new ContextThemeWrapper(originalContext, getThemeId(originalContext));
    }

    protected static void setMenuItemButtonInputType(TextView textview) {
        if (mHwFloatingToolbarFlag[0]) {
            textview.setAllCaps(false);
            textview.setInputType(16384);
        }
    }

    protected static int getThemeId(Context originalContext) {
        if (mHwFloatingToolbarFlag[0]) {
            return originalContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        }
        TypedArray a = originalContext.obtainStyledAttributes(new int[]{R.attr.isLightTheme});
        int themeId = a.getBoolean(0, true) ? R.style.Theme_Material_Light : R.style.Theme_Material;
        a.recycle();
        return themeId;
    }

    protected static void adjustWindowVisibleDisplayFrame(Rect rect) {
        if (mHwFloatingToolbarFlag[0] && rect.equals(new Rect(-10000, -10000, 10000, 10000))) {
            Display d = DisplayManagerGlobal.getInstance().getRealDisplay(0);
            if (d != null) {
                d.getRectSize(rect);
            }
        }
    }

    protected static int getFloatingToolbarHeightBias(Context context) {
        return context.getResources().getDimensionPixelSize(34472163);
    }
}
