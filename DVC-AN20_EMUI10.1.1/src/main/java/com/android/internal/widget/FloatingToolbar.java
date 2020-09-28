package com.android.internal.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManagerGlobal;
import android.hwcontrol.HwWidgetFactory;
import android.text.TextUtils;
import android.util.Size;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.hwext.internal.R;
import com.android.internal.util.Preconditions;
import com.android.internal.widget.FloatingToolbar;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class FloatingToolbar {
    public static final String FLOATING_TOOLBAR_TAG = "floating_toolbar";
    private static final String HW_FLOATING_TOOLBAR = "HwFloatingToolbar";
    private static final MenuItem.OnMenuItemClickListener NO_OP_MENUITEM_CLICK_LISTENER = $$Lambda$FloatingToolbar$7enOzxeypZYfdFYr1HzBLfj47k.INSTANCE;
    private static final boolean[] mHwFloatingToolbarFlag = new boolean[1];
    private static int mHwFloatingToolbarHeightBias;
    private final Rect mContentRect;
    private final Context mContext;
    private Menu mMenu;
    private MenuItem.OnMenuItemClickListener mMenuItemClickListener;
    private final Comparator<MenuItem> mMenuItemComparator;
    private final View.OnLayoutChangeListener mOrientationChangeHandler;
    private final FloatingToolbarPopup mPopup;
    private final Rect mPreviousContentRect;
    private List<MenuItem> mShowingMenuItems;
    private int mSuggestedWidth;
    private boolean mWidthChanged;
    private final Window mWindow;

    static /* synthetic */ boolean lambda$static$0(MenuItem item) {
        return false;
    }

    static /* synthetic */ int lambda$new$1(MenuItem menuItem1, MenuItem menuItem2) {
        if (menuItem1.getItemId() == 16908353) {
            return menuItem2.getItemId() == 16908353 ? 0 : -1;
        }
        if (menuItem2.getItemId() == 16908353) {
            return 1;
        }
        if (menuItem1.getItemId() == 16908319) {
            return -1;
        }
        if (menuItem2.getItemId() == 16908319) {
            return 1;
        }
        if (menuItem1.requiresActionButton()) {
            return menuItem2.requiresActionButton() ? 0 : -1;
        }
        if (menuItem2.requiresActionButton()) {
            return 1;
        }
        if (menuItem1.requiresOverflow()) {
            return !menuItem2.requiresOverflow();
        }
        if (menuItem2.requiresOverflow()) {
            return -1;
        }
        return menuItem1.getOrder() - menuItem2.getOrder();
    }

    public FloatingToolbar(Window window) {
        this(window, false);
    }

    public FloatingToolbar(Window window, boolean flag) {
        this.mContentRect = new Rect();
        this.mPreviousContentRect = new Rect();
        this.mShowingMenuItems = new ArrayList();
        this.mMenuItemClickListener = NO_OP_MENUITEM_CLICK_LISTENER;
        this.mWidthChanged = true;
        this.mOrientationChangeHandler = new View.OnLayoutChangeListener() {
            /* class com.android.internal.widget.FloatingToolbar.AnonymousClass1 */
            private final Rect mNewRect = new Rect();
            private final Rect mOldRect = new Rect();

            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View view, int newLeft, int newRight, int newTop, int newBottom, int oldLeft, int oldRight, int oldTop, int oldBottom) {
                this.mNewRect.set(newLeft, newRight, newTop, newBottom);
                this.mOldRect.set(oldLeft, oldRight, oldTop, oldBottom);
                if (FloatingToolbar.this.mPopup.isShowing() && !this.mNewRect.equals(this.mOldRect)) {
                    FloatingToolbar.this.mWidthChanged = true;
                    FloatingToolbar.this.updateLayout();
                }
            }
        };
        this.mMenuItemComparator = $$Lambda$FloatingToolbar$LutnsyBKrZiroTBekgIjhIyrl40.INSTANCE;
        mHwFloatingToolbarFlag[0] = flag;
        this.mContext = applyDefaultTheme(window.getContext());
        this.mWindow = (Window) Preconditions.checkNotNull(window);
        this.mPopup = new FloatingToolbarPopup(this.mContext, window.getDecorView());
    }

    public FloatingToolbar setMenu(Menu menu) {
        this.mMenu = (Menu) Preconditions.checkNotNull(menu);
        return this;
    }

    public FloatingToolbar setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener menuItemClickListener) {
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

    public void setOutsideTouchable(boolean outsideTouchable, PopupWindow.OnDismissListener onDismiss) {
        if (this.mPopup.setOutsideTouchable(outsideTouchable, onDismiss) && isShowing()) {
            dismiss();
            doShow();
        }
    }

    private void doShow() {
        List<MenuItem> menuItems = getVisibleAndEnabledMenuItems(this.mMenu);
        menuItems.sort(this.mMenuItemComparator);
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
            MenuItem menuItem = menuItems.get(i);
            MenuItem showingItem = this.mShowingMenuItems.get(i);
            if (menuItem.getItemId() != showingItem.getItemId() || !TextUtils.equals(menuItem.getTitle(), showingItem.getTitle()) || !Objects.equals(menuItem.getIcon(), showingItem.getIcon()) || menuItem.getGroupId() != showingItem.getGroupId()) {
                return false;
            }
        }
        return true;
    }

    private List<MenuItem> getVisibleAndEnabledMenuItems(Menu menu) {
        List<MenuItem> menuItems = new ArrayList<>();
        int i = 0;
        while (menu != null && i < menu.size()) {
            MenuItem menuItem = menu.getItem(i);
            processTextMenu(menuItem);
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

    private void processTextMenu(MenuItem menuItem) {
        Intent intent = menuItem.getIntent();
        if (intent != null) {
            String action = intent.getAction();
            PackageManager packageManager = this.mContext.getPackageManager();
            ResolveInfo info = packageManager.resolveActivity(intent, 0);
            if (Intent.ACTION_PROCESS_TEXT.equals(action) && info != null) {
                Drawable icon = info.loadIcon(packageManager);
                if ("com.huawei.browser".equals(info.activityInfo.packageName)) {
                    menuItem.setIcon(R.drawable.floatingtoolbar_serach);
                } else if (icon == null) {
                    menuItem.setIcon(R.drawable.floatingtoolbar_app);
                } else {
                    menuItem.setIcon(icon);
                }
            }
        }
    }

    private void registerOrientationHandler() {
        unregisterOrientationHandler();
        this.mWindow.getDecorView().addOnLayoutChangeListener(this.mOrientationChangeHandler);
    }

    private void unregisterOrientationHandler() {
        this.mWindow.getDecorView().removeOnLayoutChangeListener(this.mOrientationChangeHandler);
    }

    /* access modifiers changed from: private */
    public static final class FloatingToolbarPopup {
        private static final int MAX_OVERFLOW_SIZE = 4;
        private static final int MENU_ITEM_PADDING_RATE = 2;
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
        private final ViewTreeObserver.OnComputeInternalInsetsListener mInsetsComputer = new ViewTreeObserver.OnComputeInternalInsetsListener() {
            /* class com.android.internal.widget.$$Lambda$FloatingToolbar$FloatingToolbarPopup$77YZy6kisO5OnjlgtKp0Zi1V8EY */

            @Override // android.view.ViewTreeObserver.OnComputeInternalInsetsListener
            public final void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
                FloatingToolbar.FloatingToolbarPopup.this.lambda$new$0$FloatingToolbar$FloatingToolbarPopup(internalInsetsInfo);
            }
        };
        private boolean mIsOverflowOpen;
        private final int mLineHeight;
        private final Interpolator mLinearOutSlowInInterpolator;
        private final Interpolator mLogAccelerateInterpolator;
        private final ViewGroup mMainPanel;
        private Size mMainPanelSize;
        private final int mMarginHorizontal;
        private final int mMarginVertical;
        private final View.OnClickListener mMenuItemButtonOnClickListener = new View.OnClickListener() {
            /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass2 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if ((v.getTag() instanceof MenuItem) && FloatingToolbarPopup.this.mOnMenuItemClickListener != null) {
                    FloatingToolbarPopup.this.mOnMenuItemClickListener.onMenuItemClick((MenuItem) v.getTag());
                }
            }
        };
        private MenuItem.OnMenuItemClickListener mOnMenuItemClickListener;
        private final AnimationSet mOpenOverflowAnimation;
        private boolean mOpenOverflowUpwards;
        private final Drawable mOverflow;
        private final Animation.AnimationListener mOverflowAnimationListener;
        private final ImageButton mOverflowButton;
        private final Size mOverflowButtonSize;
        private final OverflowPanel mOverflowPanel;
        private Size mOverflowPanelSize;
        private final OverflowPanelViewHelper mOverflowPanelViewHelper;
        private final View mParent;
        private final PopupWindow mPopupWindow;
        private final Runnable mPreparePopupContentRTLHelper = new Runnable() {
            /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass1 */

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

        public /* synthetic */ void lambda$new$0$FloatingToolbar$FloatingToolbarPopup(ViewTreeObserver.InternalInsetsInfo info) {
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
            this.mMarginHorizontal = parent.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_horizontal_margin);
            this.mMarginVertical = parent.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_vertical_margin);
            int unused = FloatingToolbar.mHwFloatingToolbarHeightBias = FloatingToolbar.getFloatingToolbarHeightBias(this.mContext);
            this.mLineHeight = context.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_height);
            this.mIconTextSpacing = context.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_icon_text_spacing);
            this.mLogAccelerateInterpolator = new LogAccelerateInterpolator();
            this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563661);
            this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563662);
            this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563663);
            this.mArrow = this.mContext.getResources().getDrawable(com.android.internal.R.drawable.ft_avd_tooverflow, this.mContext.getTheme());
            this.mArrow.setAutoMirrored(true);
            this.mOverflow = this.mContext.getResources().getDrawable(com.android.internal.R.drawable.ft_avd_toarrow, this.mContext.getTheme());
            this.mOverflow.setAutoMirrored(true);
            this.mToArrow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(com.android.internal.R.drawable.ft_avd_toarrow_animation, this.mContext.getTheme());
            this.mToArrow.setAutoMirrored(true);
            this.mToOverflow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(com.android.internal.R.drawable.ft_avd_tooverflow_animation, this.mContext.getTheme());
            this.mToOverflow.setAutoMirrored(true);
            this.mOverflowButton = createOverflowButton();
            Size btnSize = measure(this.mOverflowButton);
            this.mOverflowButtonSize = new Size(btnSize.getWidth(), this.mContext.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_height));
            this.mMainPanel = createMainPanel();
            this.mOverflowPanelViewHelper = new OverflowPanelViewHelper(this.mContext, this.mIconTextSpacing);
            this.mOverflowPanel = createOverflowPanel();
            this.mOverflowAnimationListener = createOverflowAnimationListener();
            this.mOpenOverflowAnimation = new AnimationSet(true);
            this.mOpenOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
            this.mCloseOverflowAnimation = new AnimationSet(true);
            this.mCloseOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
            this.mShowAnimation = FloatingToolbar.createEnterAnimation(this.mContentContainer);
            this.mDismissAnimation = FloatingToolbar.createExitAnimation(this.mContentContainer, 0, new AnimatorListenerAdapter() {
                /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass3 */

                @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                public void onAnimationEnd(Animator animation) {
                    FloatingToolbarPopup.this.mPopupWindow.dismiss();
                    FloatingToolbarPopup.this.mContentContainer.removeAllViews();
                }
            });
            this.mHideAnimation = FloatingToolbar.createExitAnimation(this.mContentContainer, 0, new AnimatorListenerAdapter() {
                /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass4 */

                @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                public void onAnimationEnd(Animator animation) {
                    FloatingToolbarPopup.this.mPopupWindow.dismiss();
                }
            });
        }

        public boolean setOutsideTouchable(boolean outsideTouchable, PopupWindow.OnDismissListener onDismiss) {
            boolean ret = false;
            if (this.mPopupWindow.isOutsideTouchable() ^ outsideTouchable) {
                this.mPopupWindow.setOutsideTouchable(outsideTouchable);
                this.mPopupWindow.setFocusable(!outsideTouchable);
                ret = true;
            }
            this.mPopupWindow.setOnDismissListener(onDismiss);
            return ret;
        }

        public void layoutMenuItems(List<MenuItem> menuItems, MenuItem.OnMenuItemClickListener menuItemClickListener, int suggestedWidth) {
            this.mOnMenuItemClickListener = menuItemClickListener;
            cancelOverflowAnimations();
            clearPanels();
            List<MenuItem> menuItems2 = layoutMainPanelItems(menuItems, getAdjustedToolbarWidth(suggestedWidth));
            if (!menuItems2.isEmpty()) {
                layoutOverflowPanelItems(menuItems2);
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
            return !this.mDismissed && !this.mHidden;
        }

        public boolean isHidden() {
            return this.mHidden;
        }

        public void updateCoordinates(Rect contentRectOnScreen) {
            Preconditions.checkNotNull(contentRectOnScreen);
            if (isShowing() && this.mPopupWindow.isShowing()) {
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
            int statusBarHeight = this.mContext.getResources().getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
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
            int[] iArr = this.mTmpCoords;
            int rootViewLeftOnScreen = iArr[0];
            int rootViewTopOnScreen = iArr[1];
            this.mParent.getRootView().getLocationInWindow(this.mTmpCoords);
            int[] iArr2 = this.mTmpCoords;
            this.mCoordsOnWindow.set(Math.max(0, x - (rootViewLeftOnScreen - iArr2[0])), y - (rootViewTopOnScreen - iArr2[1]));
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
            final float overflowButtonTargetX;
            final int targetWidth = this.mOverflowPanelSize.getWidth();
            final int targetHeight = this.mOverflowPanelSize.getHeight() + FloatingToolbar.mHwFloatingToolbarHeightBias;
            final int startWidth = this.mContentContainer.getWidth();
            final int startHeight = this.mContentContainer.getHeight();
            final float startY = this.mContentContainer.getY();
            final float left = this.mContentContainer.getX();
            final float right = left + ((float) this.mContentContainer.getWidth());
            Animation widthAnimation = new Animation() {
                /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass5 */

                /* access modifiers changed from: protected */
                @Override // android.view.animation.Animation
                public void applyTransformation(float interpolatedTime, Transformation t) {
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
                /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass6 */

                /* access modifiers changed from: protected */
                @Override // android.view.animation.Animation
                public void applyTransformation(float interpolatedTime, Transformation t) {
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
                /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass7 */

                /* access modifiers changed from: protected */
                @Override // android.view.animation.Animation
                public void applyTransformation(float interpolatedTime, Transformation t) {
                    float deltaContainerWidth;
                    float f = overflowButtonStartX;
                    float overflowButtonX = f + ((overflowButtonTargetX - f) * interpolatedTime);
                    if (FloatingToolbarPopup.this.isInRTLMode()) {
                        deltaContainerWidth = 0.0f;
                    } else {
                        deltaContainerWidth = (float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - startWidth);
                    }
                    FloatingToolbarPopup.this.mOverflowButton.setX(overflowButtonX + deltaContainerWidth);
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
            final float overflowButtonTargetX;
            final int targetWidth = this.mMainPanelSize.getWidth();
            final int startWidth = this.mContentContainer.getWidth();
            final float left = this.mContentContainer.getX();
            final float right = left + ((float) this.mContentContainer.getWidth());
            Animation widthAnimation = new Animation() {
                /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass8 */

                /* access modifiers changed from: protected */
                @Override // android.view.animation.Animation
                public void applyTransformation(float interpolatedTime, Transformation t) {
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
                /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass9 */

                /* access modifiers changed from: protected */
                @Override // android.view.animation.Animation
                public void applyTransformation(float interpolatedTime, Transformation t) {
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
                /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass10 */

                /* access modifiers changed from: protected */
                @Override // android.view.animation.Animation
                public void applyTransformation(float interpolatedTime, Transformation t) {
                    float deltaContainerWidth;
                    float f = overflowButtonStartX;
                    float overflowButtonX = f + ((overflowButtonTargetX - f) * interpolatedTime);
                    if (FloatingToolbarPopup.this.isInRTLMode()) {
                        deltaContainerWidth = 0.0f;
                    } else {
                        deltaContainerWidth = (float) (FloatingToolbarPopup.this.mContentContainer.getWidth() - startWidth);
                    }
                    FloatingToolbarPopup.this.mOverflowButton.setX(overflowButtonX + deltaContainerWidth);
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

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setPanelsStatesAtRestingPosition() {
            this.mOverflowButton.setEnabled(true);
            this.mOverflowPanel.awakenScrollBars();
            if (this.mIsOverflowOpen) {
                Size containerSize = new Size(this.mOverflowPanelSize.getWidth(), this.mOverflowPanelSize.getHeight() + FloatingToolbar.mHwFloatingToolbarHeightBias);
                setSize(this.mContentContainer, containerSize);
                this.mMainPanel.setAlpha(0.0f);
                this.mMainPanel.setVisibility(4);
                this.mOverflowPanel.setAlpha(1.0f);
                this.mOverflowPanel.setVisibility(0);
                this.mOverflowButton.setImageDrawable(this.mArrow);
                this.mOverflowButton.setContentDescription(this.mContext.getString(com.android.internal.R.string.floating_toolbar_close_overflow_description));
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
            Size containerSize2 = new Size(this.mMainPanelSize.getWidth(), this.mMainPanelSize.getHeight() + FloatingToolbar.mHwFloatingToolbarHeightBias);
            setSize(this.mContentContainer, containerSize2);
            this.mMainPanel.setAlpha(1.0f);
            this.mMainPanel.setVisibility(0);
            this.mOverflowPanel.setAlpha(0.0f);
            this.mOverflowPanel.setVisibility(4);
            this.mOverflowButton.setImageDrawable(this.mOverflow);
            this.mOverflowButton.setContentDescription(this.mContext.getString(com.android.internal.R.string.floating_toolbar_open_overflow_description));
            if (hasOverflow()) {
                if (isInRTLMode()) {
                    this.mContentContainer.setX((float) this.mMarginHorizontal);
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX(0.0f);
                    this.mOverflowPanel.setX(0.0f);
                } else {
                    this.mContentContainer.setX((float) ((this.mPopupWindow.getWidth() - containerSize2.getWidth()) - this.mMarginHorizontal));
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX((float) (containerSize2.getWidth() - this.mOverflowButtonSize.getWidth()));
                    this.mOverflowPanel.setX((float) (containerSize2.getWidth() - this.mOverflowPanelSize.getWidth()));
                }
                if (this.mOpenOverflowUpwards) {
                    this.mContentContainer.setY((float) (((this.mMarginVertical + this.mOverflowPanelSize.getHeight()) - containerSize2.getHeight()) + FloatingToolbar.mHwFloatingToolbarHeightBias));
                    this.mMainPanel.setY(0.0f);
                    this.mOverflowButton.setY(0.0f);
                    this.mOverflowPanel.setY((float) (containerSize2.getHeight() - this.mOverflowPanelSize.getHeight()));
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
                        ViewGroup viewGroup = this.mContentContainer;
                        viewGroup.setY(viewGroup.getY() + ((float) deltaHeight));
                        ImageButton imageButton = this.mOverflowButton;
                        imageButton.setY(imageButton.getY() - ((float) deltaHeight));
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
            Size size = this.mMainPanelSize;
            if (size != null) {
                width = Math.max(0, size.getWidth());
                height = Math.max(0, this.mMainPanelSize.getHeight());
            }
            Size size2 = this.mOverflowPanelSize;
            if (size2 != null) {
                width = Math.max(width, size2.getWidth());
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
            int maximumWidth = this.mViewPortOnScreen.width() - (this.mParent.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_horizontal_margin) * 2);
            if (width <= 0) {
                width = this.mParent.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_preferred_width);
            }
            return Math.min(width, maximumWidth);
        }

        private void setZeroTouchableSurface() {
            this.mTouchableRegion.setEmpty();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setContentAreaAsTouchableSurface() {
            int height;
            int width;
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

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isInRTLMode() {
            if (!this.mContext.getApplicationInfo().hasRtlSupport() || this.mContext.getResources().getConfiguration().getLayoutDirection() != 1) {
                return false;
            }
            return true;
        }

        private boolean hasOverflow() {
            return this.mOverflowPanelSize != null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setMenuItemBackground(View item, String name) {
            if (item != null) {
                String resName = "item_background_" + name + "_emui";
                if (HwWidgetFactory.isHwEmphasizeTheme(this.mContext)) {
                    resName = resName + "_dark";
                }
                int resId = this.mContext.getResources().getIdentifier(resName, "drawable", "androidhwext");
                if (resId != 0) {
                    item.setBackgroundResource(resId);
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for r5v1, resolved type: int */
        /* JADX DEBUG: Multi-variable search result rejected for r5v2, resolved type: int */
        /* JADX DEBUG: Multi-variable search result rejected for r11v0, resolved type: boolean */
        /* JADX DEBUG: Multi-variable search result rejected for r5v10, resolved type: int */
        /* JADX DEBUG: Multi-variable search result rejected for r11v1, resolved type: boolean */
        /* JADX DEBUG: Multi-variable search result rejected for r11v3, resolved type: boolean */
        /* JADX WARN: Multi-variable type inference failed */
        public List<MenuItem> layoutMainPanelItems(List<MenuItem> menuItems, int toolbarWidth) {
            int i;
            Preconditions.checkNotNull(menuItems);
            int availableWidth = toolbarWidth;
            LinkedList<MenuItem> remainingMenuItems = new LinkedList<>();
            LinkedList<MenuItem> overflowMenuItems = new LinkedList<>();
            Iterator<MenuItem> it = menuItems.iterator();
            while (true) {
                i = 16908353;
                if (!it.hasNext()) {
                    break;
                }
                MenuItem menuItem = it.next();
                if (menuItem.getItemId() == 16908353 || !menuItem.requiresOverflow()) {
                    remainingMenuItems.add(menuItem);
                } else {
                    overflowMenuItems.add(menuItem);
                }
            }
            remainingMenuItems.addAll(overflowMenuItems);
            this.mMainPanel.removeAllViews();
            int i2 = 0;
            this.mMainPanel.setPaddingRelative(0, 0, 0, 0);
            boolean isFirstItem = true;
            Rect paddingRect = new Rect();
            while (true) {
                if (remainingMenuItems.isEmpty()) {
                    break;
                }
                MenuItem menuItem2 = remainingMenuItems.peek();
                if (!isFirstItem && menuItem2.requiresOverflow()) {
                    break;
                }
                boolean showIcon = (!isFirstItem || menuItem2.getItemId() != i) ? i2 : 1;
                View menuItemButton = FloatingToolbar.createMenuItemButton(this.mContext, menuItem2, this.mIconTextSpacing, showIcon);
                paddingRect.set(menuItemButton.getPaddingStart(), menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd(), menuItemButton.getPaddingBottom());
                if (showIcon == 0 && (menuItemButton instanceof LinearLayout)) {
                    ((LinearLayout) menuItemButton).setGravity(17);
                }
                if (isFirstItem) {
                    if (remainingMenuItems.size() == 1) {
                        setMenuItemBackground(menuItemButton, "material_light_single");
                    } else if (isInRTLMode()) {
                        setMenuItemBackground(menuItemButton, "material_light_right");
                    } else {
                        setMenuItemBackground(menuItemButton, "material_light_left");
                    }
                } else if (remainingMenuItems.size() != 1) {
                    setMenuItemBackground(menuItemButton, "light_middle");
                } else if (isInRTLMode()) {
                    setMenuItemBackground(menuItemButton, "material_light_left");
                } else {
                    setMenuItemBackground(menuItemButton, "material_light_right");
                }
                menuItemButton.setPaddingRelative(paddingRect.left, paddingRect.top, paddingRect.right, paddingRect.bottom);
                if (isFirstItem) {
                    menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart() * 2, menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd(), menuItemButton.getPaddingBottom());
                }
                int i3 = remainingMenuItems.size() == 1 ? 1 : i2;
                if (i3 != 0) {
                    menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart(), menuItemButton.getPaddingTop(), hasOverflow() ? i2 : menuItemButton.getPaddingEnd() * 2, menuItemButton.getPaddingBottom());
                }
                menuItemButton.measure(i2, i2);
                int menuItemButtonWidth = Math.min(menuItemButton.getMeasuredWidth(), toolbarWidth);
                int i4 = menuItemButtonWidth <= availableWidth - this.mOverflowButtonSize.getWidth() ? 1 : i2;
                int i5 = (i3 == 0 || menuItemButtonWidth > availableWidth) ? i2 : 1;
                if (i4 == 0 && i5 == 0) {
                    break;
                }
                setButtonTagAndClickListener(menuItemButton, menuItem2);
                menuItemButton.setTooltipText(menuItem2.getTooltipText());
                this.mMainPanel.addView(menuItemButton);
                ViewGroup.LayoutParams params = menuItemButton.getLayoutParams();
                params.width = menuItemButtonWidth;
                menuItemButton.setLayoutParams(params);
                availableWidth -= menuItemButtonWidth;
                remainingMenuItems.pop();
                menuItem2.getGroupId();
                isFirstItem = false;
                i2 = 0;
                i = 16908353;
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
                overflowPanelAdapter.add(menuItems.get(i));
            }
            this.mOverflowPanel.setAdapter((ListAdapter) overflowPanelAdapter);
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
                this.mContentContainer.addView(this.mOverflowButton, new ViewGroup.LayoutParams(-2, this.mOverflowButtonSize.getHeight()));
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
            this.mOverflowPanel.setAdapter((ListAdapter) overflowPanelAdapter);
            this.mContentContainer.removeAllViews();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
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
            return (this.mLineHeight * actualSize) + this.mOverflowButtonSize.getHeight() + extension;
        }

        private void setButtonTagAndClickListener(View menuItemButton, MenuItem menuItem) {
            menuItemButton.setTag(menuItem);
            menuItemButton.setOnClickListener(this.mMenuItemButtonOnClickListener);
        }

        private int getAdjustedDuration(int originalDuration) {
            int i = this.mTransitionDurationScale;
            if (i < 150) {
                return Math.max(originalDuration - 50, 0);
            }
            if (i > 300) {
                return originalDuration + 50;
            }
            return (int) (((float) originalDuration) * ValueAnimator.getDurationScale());
        }

        private void maybeComputeTransitionDurationScale() {
            Size size = this.mMainPanelSize;
            if (size != null && this.mOverflowPanelSize != null) {
                int w = size.getWidth() - this.mOverflowPanelSize.getWidth();
                int h = this.mOverflowPanelSize.getHeight() - this.mMainPanelSize.getHeight();
                this.mTransitionDurationScale = (int) (Math.sqrt((double) ((w * w) + (h * h))) / ((double) this.mContentContainer.getContext().getResources().getDisplayMetrics().density));
            }
        }

        private ViewGroup createMainPanel() {
            return new LinearLayout(this.mContext) {
                /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass11 */

                /* access modifiers changed from: protected */
                @Override // android.widget.LinearLayout, android.view.View
                public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    if (FloatingToolbarPopup.this.isOverflowAnimating() && FloatingToolbarPopup.this.mMainPanelSize != null) {
                        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(FloatingToolbarPopup.this.mMainPanelSize.getWidth(), 1073741824);
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }

                @Override // android.view.ViewGroup
                public boolean onInterceptTouchEvent(MotionEvent ev) {
                    return FloatingToolbarPopup.this.isOverflowAnimating();
                }
            };
        }

        private ImageButton createOverflowButton() {
            ImageButton overflowButton = (ImageButton) LayoutInflater.from(this.mContext).inflate(com.android.internal.R.layout.floating_popup_overflow_button, (ViewGroup) null);
            overflowButton.setImageDrawable(this.mOverflow);
            overflowButton.setOnClickListener(new View.OnClickListener(overflowButton) {
                /* class com.android.internal.widget.$$Lambda$FloatingToolbar$FloatingToolbarPopup$uEfRwR_1oHxMvRVdmbNRdukDM */
                private final /* synthetic */ ImageButton f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    FloatingToolbar.FloatingToolbarPopup.this.lambda$createOverflowButton$1$FloatingToolbar$FloatingToolbarPopup(this.f$1, view);
                }
            });
            return overflowButton;
        }

        public /* synthetic */ void lambda$createOverflowButton$1$FloatingToolbar$FloatingToolbarPopup(ImageButton overflowButton, View v) {
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
            overflowPanel.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            overflowPanel.setDivider(null);
            overflowPanel.setDividerHeight(0);
            overflowPanel.setAdapter((ListAdapter) new ArrayAdapter<MenuItem>(this.mContext, 0) {
                /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass12 */

                @Override // android.widget.ArrayAdapter, android.widget.Adapter
                public View getView(int position, View convertView, ViewGroup parent) {
                    View itemView = FloatingToolbarPopup.this.mOverflowPanelViewHelper.getView((MenuItem) getItem(position), FloatingToolbarPopup.this.mOverflowPanelSize.getWidth(), convertView);
                    Rect paddingRect = new Rect();
                    paddingRect.set(itemView.getPaddingStart(), itemView.getPaddingTop(), itemView.getPaddingEnd(), itemView.getPaddingBottom());
                    if (FloatingToolbarPopup.this.mOpenOverflowUpwards) {
                        if (position == 0) {
                            FloatingToolbarPopup.this.setMenuItemBackground(itemView, "light_top");
                        } else {
                            FloatingToolbarPopup.this.setMenuItemBackground(itemView, "light_middle_overflow");
                        }
                    } else if (position == getCount() - 1) {
                        FloatingToolbarPopup.this.setMenuItemBackground(itemView, "light_bottom");
                    } else {
                        FloatingToolbarPopup.this.setMenuItemBackground(itemView, "light_middle_overflow");
                    }
                    itemView.setPaddingRelative(paddingRect.left, paddingRect.top, paddingRect.right, paddingRect.bottom);
                    return itemView;
                }
            });
            overflowPanel.setOnItemClickListener(new AdapterView.OnItemClickListener(overflowPanel) {
                /* class com.android.internal.widget.$$Lambda$FloatingToolbar$FloatingToolbarPopup$E8FwnPCl7gZpcTlX_UaRPIBRnT0 */
                private final /* synthetic */ FloatingToolbar.FloatingToolbarPopup.OverflowPanel f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.widget.AdapterView.OnItemClickListener
                public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                    FloatingToolbar.FloatingToolbarPopup.this.lambda$createOverflowPanel$2$FloatingToolbar$FloatingToolbarPopup(this.f$1, adapterView, view, i, j);
                }
            });
            return overflowPanel;
        }

        public /* synthetic */ void lambda$createOverflowPanel$2$FloatingToolbar$FloatingToolbarPopup(OverflowPanel overflowPanel, AdapterView parent, View view, int position, long id) {
            MenuItem menuItem = (MenuItem) overflowPanel.getAdapter().getItem(position);
            MenuItem.OnMenuItemClickListener onMenuItemClickListener = this.mOnMenuItemClickListener;
            if (onMenuItemClickListener != null) {
                onMenuItemClickListener.onMenuItemClick(menuItem);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isOverflowAnimating() {
            return (this.mOpenOverflowAnimation.hasStarted() && !this.mOpenOverflowAnimation.hasEnded()) || (this.mCloseOverflowAnimation.hasStarted() && !this.mCloseOverflowAnimation.hasEnded());
        }

        private Animation.AnimationListener createOverflowAnimationListener() {
            return new Animation.AnimationListener() {
                /* class com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass13 */

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationStart(Animation animation) {
                    FloatingToolbarPopup.this.mOverflowButton.setEnabled(false);
                    FloatingToolbarPopup.this.mMainPanel.setVisibility(0);
                    FloatingToolbarPopup.this.mOverflowPanel.setVisibility(0);
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationEnd(Animation animation) {
                    FloatingToolbarPopup.this.mContentContainer.post(new Runnable() {
                        /* class com.android.internal.widget.$$Lambda$FloatingToolbar$FloatingToolbarPopup$13$7WTSUuAWkzil48e0QxuKTn0YOXI */

                        public final void run() {
                            FloatingToolbar.FloatingToolbarPopup.AnonymousClass13.this.lambda$onAnimationEnd$0$FloatingToolbar$FloatingToolbarPopup$13();
                        }
                    });
                }

                public /* synthetic */ void lambda$onAnimationEnd$0$FloatingToolbar$FloatingToolbarPopup$13() {
                    FloatingToolbarPopup.this.setPanelsStatesAtRestingPosition();
                    FloatingToolbarPopup.this.setContentAreaAsTouchableSurface();
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationRepeat(Animation animation) {
                }
            };
        }

        private static Size measure(View view) {
            Preconditions.checkState(view.getParent() == null);
            view.measure(0, 0);
            return new Size(view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        private static void setSize(View view, int width, int height) {
            view.setMinimumWidth(width);
            view.setMinimumHeight(height);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            ViewGroup.LayoutParams params2 = params == null ? new ViewGroup.LayoutParams(0, 0) : params;
            params2.width = width;
            params2.height = height;
            view.setLayoutParams(params2);
        }

        private static void setSize(View view, Size size) {
            setSize(view, size.getWidth(), size.getHeight());
        }

        /* access modifiers changed from: private */
        public static void setWidth(View view, int width) {
            setSize(view, width, view.getLayoutParams().height);
        }

        /* access modifiers changed from: private */
        public static void setHeight(View view, int height) {
            setSize(view, view.getLayoutParams().width, height);
        }

        /* access modifiers changed from: private */
        public static final class OverflowPanel extends ListView {
            private final FloatingToolbarPopup mPopup;

            OverflowPanel(FloatingToolbarPopup popup) {
                super(((FloatingToolbarPopup) Preconditions.checkNotNull(popup)).mContext);
                this.mPopup = popup;
                setScrollBarDefaultDelayBeforeFade(ViewConfiguration.getScrollDefaultDelay() * 3);
                if (!HwWidgetFactory.isHwTheme(this.mContext)) {
                    setScrollIndicators(3);
                }
            }

            /* access modifiers changed from: protected */
            @Override // android.widget.AbsListView, android.view.View, android.widget.ListView
            public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(this.mPopup.mOverflowPanelSize.getHeight() - this.mPopup.mOverflowButtonSize.getHeight(), 1073741824));
            }

            @Override // android.view.ViewGroup, android.view.View
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (this.mPopup.isOverflowAnimating()) {
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }

            /* access modifiers changed from: protected */
            @Override // android.view.View
            public boolean awakenScrollBars() {
                return super.awakenScrollBars();
            }
        }

        private static final class LogAccelerateInterpolator implements Interpolator {
            private static final int BASE = 100;
            private static final float LOGS_SCALE = (1.0f / computeLog(1.0f, 100));

            private LogAccelerateInterpolator() {
            }

            private static float computeLog(float t, int base) {
                return (float) (1.0d - Math.pow((double) base, (double) (-t)));
            }

            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float t) {
                return 1.0f - (computeLog(1.0f - t, 100) * LOGS_SCALE);
            }
        }

        /* access modifiers changed from: private */
        public static final class OverflowPanelViewHelper {
            private final View mCalculator = createMenuButton(null);
            private final Context mContext;
            private final int mIconTextSpacing;
            private final int mSidePadding;

            public OverflowPanelViewHelper(Context context, int iconTextSpacing) {
                this.mContext = (Context) Preconditions.checkNotNull(context);
                this.mIconTextSpacing = iconTextSpacing;
                this.mSidePadding = context.getResources().getDimensionPixelSize(com.android.internal.R.dimen.floating_toolbar_overflow_side_padding);
            }

            public View getView(MenuItem menuItem, int minimumWidth, View convertView) {
                Preconditions.checkNotNull(menuItem);
                if (convertView != null) {
                    FloatingToolbar.updateMenuItemButton(convertView, menuItem, this.mIconTextSpacing, shouldShowIcon(menuItem));
                } else {
                    convertView = createMenuButton(menuItem);
                }
                convertView.setMinimumWidth(minimumWidth);
                return convertView;
            }

            public int calculateWidth(MenuItem menuItem) {
                FloatingToolbar.updateMenuItemButton(this.mCalculator, menuItem, this.mIconTextSpacing, shouldShowIcon(menuItem));
                this.mCalculator.measure(0, 0);
                return this.mCalculator.getMeasuredWidth();
            }

            private View createMenuButton(MenuItem menuItem) {
                View button = FloatingToolbar.createMenuItemButton(this.mContext, menuItem, this.mIconTextSpacing, shouldShowIcon(menuItem));
                int i = this.mSidePadding;
                button.setPadding(i, 0, i, 0);
                return button;
            }

            private boolean shouldShowIcon(MenuItem menuItem) {
                if (menuItem != null) {
                    return true;
                }
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public static View createMenuItemButton(Context context, MenuItem menuItem, int iconTextSpacing, boolean showIcon) {
        View menuItemButton = LayoutInflater.from(context).inflate(com.android.internal.R.layout.floating_popup_menu_button, (ViewGroup) null);
        if (menuItem != null) {
            updateMenuItemButton(menuItemButton, menuItem, iconTextSpacing, showIcon);
        }
        return menuItemButton;
    }

    /* access modifiers changed from: private */
    public static void updateMenuItemButton(View menuItemButton, MenuItem menuItem, int iconTextSpacing, boolean showIcon) {
        TextView buttonText = (TextView) menuItemButton.findViewById(com.android.internal.R.id.floating_toolbar_menu_item_text);
        buttonText.setEllipsize(null);
        if (TextUtils.isEmpty(menuItem.getTitle())) {
            buttonText.setVisibility(8);
        } else {
            buttonText.setVisibility(0);
            buttonText.setText(menuItem.getTitle());
        }
        ImageView buttonIcon = (ImageView) menuItemButton.findViewById(com.android.internal.R.id.floating_toolbar_menu_item_image);
        if (menuItem.getIcon() == null || !showIcon) {
            buttonIcon.setVisibility(8);
            buttonText.setPaddingRelative(0, 0, 0, 0);
        } else {
            buttonIcon.setVisibility(0);
            buttonIcon.setImageDrawable(menuItem.getIcon());
            buttonText.setPaddingRelative(iconTextSpacing, 0, 0, 0);
        }
        CharSequence contentDescription = menuItem.getContentDescription();
        if (TextUtils.isEmpty(contentDescription)) {
            menuItemButton.setContentDescription(menuItem.getTitle());
        } else {
            menuItemButton.setContentDescription(contentDescription);
        }
    }

    /* access modifiers changed from: private */
    public static ViewGroup createContentContainer(Context context) {
        ViewGroup contentContainer = (ViewGroup) LayoutInflater.from(context).inflate(com.android.internal.R.layout.floating_popup_container, (ViewGroup) null);
        if (mHwFloatingToolbarFlag[0]) {
            contentContainer.setElevation(0.0f);
        }
        contentContainer.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        contentContainer.setTag(FLOATING_TOOLBAR_TAG);
        return contentContainer;
    }

    /* access modifiers changed from: private */
    public static PopupWindow createPopupWindow(ViewGroup content) {
        ViewGroup popupContentHolder = new LinearLayout(content.getContext());
        PopupWindow popupWindow = new PopupWindow(popupContentHolder);
        popupWindow.setClippingEnabled(false);
        popupWindow.setWindowLayoutType(1005);
        popupWindow.setAnimationStyle(0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        popupWindow.setTitle(HW_FLOATING_TOOLBAR);
        content.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        popupContentHolder.addView(content);
        return popupWindow;
    }

    /* access modifiers changed from: private */
    public static AnimatorSet createEnterAnimation(View view) {
        AnimatorSet animation = new AnimatorSet();
        animation.playTogether(ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f).setDuration(150L));
        return animation;
    }

    /* access modifiers changed from: private */
    public static AnimatorSet createExitAnimation(View view, int startDelay, Animator.AnimatorListener listener) {
        AnimatorSet animation = new AnimatorSet();
        animation.playTogether(ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f).setDuration(100L));
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
        if (!mHwFloatingToolbarFlag[0]) {
            TypedArray a = originalContext.obtainStyledAttributes(new int[]{16844176});
            int themeId = a.getBoolean(0, true) ? 16974391 : 16974372;
            a.recycle();
            return themeId;
        } else if (HwWidgetFactory.isHwDarkTheme(originalContext)) {
            return originalContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null);
        } else {
            if (HwWidgetFactory.isHwEmphasizeTheme(originalContext)) {
                return originalContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark.Emphasize", null, null);
            }
            return originalContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        }
    }

    protected static void adjustWindowVisibleDisplayFrame(Rect rect) {
        if (mHwFloatingToolbarFlag[0] && rect.equals(new Rect(-10000, -10000, 10000, 10000))) {
            DisplayManagerGlobal.getInstance().getRealDisplay(0).getRectSize(rect);
        }
    }

    protected static int getFloatingToolbarHeightBias(Context context) {
        return context.getResources().getDimensionPixelSize(34472163);
    }
}
