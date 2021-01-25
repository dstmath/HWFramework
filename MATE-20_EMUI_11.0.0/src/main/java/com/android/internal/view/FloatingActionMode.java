package com.android.internal.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.HwMwUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.PopupWindow;
import com.android.internal.R;
import com.android.internal.util.Preconditions;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.widget.FloatingToolbar;
import com.huawei.android.app.HwActivityTaskManager;
import java.util.Arrays;

public final class FloatingActionMode extends ActionMode {
    private static final int MAX_HIDE_DURATION = 3000;
    private static final int MOVING_HIDE_DELAY = 50;
    private final int mBottomAllowance;
    private final ActionMode.Callback2 mCallback;
    private final Rect mContentRect;
    private final Rect mContentRectOnScreen;
    private final Context mContext;
    private final Point mDisplaySize;
    private FloatingToolbar mFloatingToolbar;
    private FloatingToolbarVisibilityHelper mFloatingToolbarVisibilityHelper;
    private final Runnable mHideOff = new Runnable() {
        /* class com.android.internal.view.FloatingActionMode.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            if (FloatingActionMode.this.isViewStillActive()) {
                FloatingActionMode.this.mFloatingToolbarVisibilityHelper.setHideRequested(false);
                FloatingActionMode.this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
            }
        }
    };
    private final MenuBuilder mMenu;
    private final Runnable mMovingOff = new Runnable() {
        /* class com.android.internal.view.FloatingActionMode.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            if (FloatingActionMode.this.isViewStillActive()) {
                FloatingActionMode.this.mFloatingToolbarVisibilityHelper.setMoving(false);
                FloatingActionMode.this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
            }
        }
    };
    private final View mOriginatingView;
    private final Rect mPreviousContentRectOnScreen;
    private final int[] mPreviousViewPositionOnScreen;
    private final Rect mPreviousViewRectOnScreen;
    private final int[] mRootViewPositionOnScreen;
    private final Rect mScreenRect;
    private final int[] mViewPositionOnScreen;
    private final Rect mViewRectOnScreen;

    public FloatingActionMode(Context context, ActionMode.Callback2 callback, View originatingView, FloatingToolbar floatingToolbar) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mCallback = (ActionMode.Callback2) Preconditions.checkNotNull(callback);
        this.mMenu = new MenuBuilder(context).setDefaultShowAsAction(1);
        setType(1);
        this.mMenu.setCallback(new MenuBuilder.Callback() {
            /* class com.android.internal.view.FloatingActionMode.AnonymousClass3 */

            @Override // com.android.internal.view.menu.MenuBuilder.Callback
            public void onMenuModeChange(MenuBuilder menu) {
            }

            @Override // com.android.internal.view.menu.MenuBuilder.Callback
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                return FloatingActionMode.this.mCallback.onActionItemClicked(FloatingActionMode.this, item);
            }
        });
        this.mContentRect = new Rect();
        this.mContentRectOnScreen = new Rect();
        this.mPreviousContentRectOnScreen = new Rect();
        this.mViewPositionOnScreen = new int[2];
        this.mPreviousViewPositionOnScreen = new int[2];
        this.mRootViewPositionOnScreen = new int[2];
        this.mViewRectOnScreen = new Rect();
        this.mPreviousViewRectOnScreen = new Rect();
        this.mScreenRect = new Rect();
        this.mOriginatingView = (View) Preconditions.checkNotNull(originatingView);
        this.mOriginatingView.getLocationOnScreen(this.mViewPositionOnScreen);
        this.mBottomAllowance = context.getResources().getDimensionPixelSize(R.dimen.content_rect_bottom_clip_allowance);
        this.mDisplaySize = new Point();
        setFloatingToolbar((FloatingToolbar) Preconditions.checkNotNull(floatingToolbar));
    }

    private void setFloatingToolbar(FloatingToolbar floatingToolbar) {
        this.mFloatingToolbar = floatingToolbar.setMenu(this.mMenu).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            /* class com.android.internal.view.$$Lambda$FloatingActionMode$LU5MpPuKYDtwlFAuYhXYfzgLNLE */

            @Override // android.view.MenuItem.OnMenuItemClickListener
            public final boolean onMenuItemClick(MenuItem menuItem) {
                return FloatingActionMode.this.lambda$setFloatingToolbar$0$FloatingActionMode(menuItem);
            }
        });
        this.mFloatingToolbarVisibilityHelper = new FloatingToolbarVisibilityHelper(this.mFloatingToolbar);
        this.mFloatingToolbarVisibilityHelper.activate();
    }

    public /* synthetic */ boolean lambda$setFloatingToolbar$0$FloatingActionMode(MenuItem item) {
        return this.mMenu.performItemAction(item, 0);
    }

    @Override // android.view.ActionMode
    public void setTitle(CharSequence title) {
    }

    @Override // android.view.ActionMode
    public void setTitle(int resId) {
    }

    @Override // android.view.ActionMode
    public void setSubtitle(CharSequence subtitle) {
    }

    @Override // android.view.ActionMode
    public void setSubtitle(int resId) {
    }

    @Override // android.view.ActionMode
    public void setCustomView(View view) {
    }

    @Override // android.view.ActionMode
    public void invalidate() {
        this.mCallback.onPrepareActionMode(this, this.mMenu);
        invalidateContentRect();
    }

    @Override // android.view.ActionMode
    public void invalidateContentRect() {
        this.mCallback.onGetContentRect(this, this.mOriginatingView, this.mContentRect);
        repositionToolbar();
    }

    public void updateViewLocationInWindow() {
        this.mOriginatingView.getLocationOnScreen(this.mViewPositionOnScreen);
        this.mOriginatingView.getRootView().getLocationOnScreen(this.mRootViewPositionOnScreen);
        this.mOriginatingView.getGlobalVisibleRect(this.mViewRectOnScreen);
        Rect rect = this.mViewRectOnScreen;
        int[] iArr = this.mRootViewPositionOnScreen;
        rect.offset(iArr[0], iArr[1]);
        if (!Arrays.equals(this.mViewPositionOnScreen, this.mPreviousViewPositionOnScreen) || !this.mViewRectOnScreen.equals(this.mPreviousViewRectOnScreen)) {
            repositionToolbar();
            int[] iArr2 = this.mPreviousViewPositionOnScreen;
            int[] iArr3 = this.mViewPositionOnScreen;
            iArr2[0] = iArr3[0];
            iArr2[1] = iArr3[1];
            this.mPreviousViewRectOnScreen.set(this.mViewRectOnScreen);
        }
    }

    private void repositionToolbar() {
        this.mContentRectOnScreen.set(this.mContentRect);
        Rect rect = this.mContentRectOnScreen;
        int[] iArr = this.mViewPositionOnScreen;
        rect.offset(iArr[0], iArr[1]);
        if (!isContentRectWithinBounds() || !isOriginatingViewVisible()) {
            this.mFloatingToolbarVisibilityHelper.setOutOfBounds(true);
            this.mContentRectOnScreen.setEmpty();
        } else {
            this.mFloatingToolbarVisibilityHelper.setOutOfBounds(false);
            Rect rect2 = this.mContentRectOnScreen;
            rect2.set(Math.max(rect2.left, this.mViewRectOnScreen.left), Math.max(this.mContentRectOnScreen.top, this.mViewRectOnScreen.top), Math.min(this.mContentRectOnScreen.right, this.mViewRectOnScreen.right), Math.min(this.mContentRectOnScreen.bottom, this.mViewRectOnScreen.bottom + this.mBottomAllowance));
            if (!this.mContentRectOnScreen.equals(this.mPreviousContentRectOnScreen)) {
                this.mOriginatingView.removeCallbacks(this.mMovingOff);
                this.mFloatingToolbarVisibilityHelper.setMoving(true);
                this.mOriginatingView.postDelayed(this.mMovingOff, 50);
                this.mFloatingToolbar.setContentRect(this.mContentRectOnScreen);
                this.mFloatingToolbar.updateLayout();
            }
        }
        this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
        this.mPreviousContentRectOnScreen.set(this.mContentRectOnScreen);
    }

    private boolean isOriginatingViewVisible() {
        return this.mOriginatingView.getGlobalVisibleRect(new Rect());
    }

    private boolean isContentRectWithinBounds() {
        ((WindowManager) this.mContext.getSystemService(WindowManager.class)).getDefaultDisplay().getRealSize(this.mDisplaySize);
        if (!HwMwUtils.ENABLED || !this.mContext.getResources().getConfiguration().windowConfiguration.inHwMagicWindowingMode()) {
            this.mScreenRect.set(0, 0, this.mDisplaySize.x, this.mDisplaySize.y);
        } else {
            HwMwUtils.performPolicy(1002, this.mOriginatingView, this.mScreenRect, this.mDisplaySize);
        }
        Configuration config = this.mContext.getResources().getConfiguration();
        if (config != null && config.windowConfiguration.inHwPCFreeFormWindowingMode() && HwActivityTaskManager.isPCMultiCastMode()) {
            Rect bounds = config.windowConfiguration.getBounds();
            if (bounds != null && !bounds.isEmpty()) {
                this.mScreenRect.set(bounds);
            }
        } else if (config != null && config.windowConfiguration.inHwFreeFormWindowingMode()) {
            Rect rect = this.mScreenRect;
            int[] iArr = this.mViewPositionOnScreen;
            rect.set(iArr[0], iArr[1], iArr[0] + this.mDisplaySize.x, this.mViewPositionOnScreen[1] + this.mDisplaySize.y);
        }
        return intersectsClosed(this.mContentRectOnScreen, this.mScreenRect) && intersectsClosed(this.mContentRectOnScreen, this.mViewRectOnScreen);
    }

    private static boolean intersectsClosed(Rect a, Rect b) {
        return a.left <= b.right && b.left <= a.right && a.top <= b.bottom && b.top <= a.bottom;
    }

    @Override // android.view.ActionMode
    public void hide(long duration) {
        if (duration == -1) {
            duration = ViewConfiguration.getDefaultActionModeHideDuration();
        }
        long duration2 = Math.min(3000L, duration);
        this.mOriginatingView.removeCallbacks(this.mHideOff);
        if (duration2 <= 0) {
            this.mHideOff.run();
            return;
        }
        this.mFloatingToolbarVisibilityHelper.setHideRequested(true);
        this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
        this.mOriginatingView.postDelayed(this.mHideOff, duration2);
    }

    public void setOutsideTouchable(boolean outsideTouchable, PopupWindow.OnDismissListener onDismiss) {
        this.mFloatingToolbar.setOutsideTouchable(outsideTouchable, onDismiss);
    }

    @Override // android.view.ActionMode
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        this.mFloatingToolbarVisibilityHelper.setWindowFocused(hasWindowFocus);
        this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
    }

    @Override // android.view.ActionMode
    public void finish() {
        reset();
        this.mCallback.onDestroyActionMode(this);
    }

    @Override // android.view.ActionMode
    public Menu getMenu() {
        return this.mMenu;
    }

    @Override // android.view.ActionMode
    public CharSequence getTitle() {
        return null;
    }

    @Override // android.view.ActionMode
    public CharSequence getSubtitle() {
        return null;
    }

    @Override // android.view.ActionMode
    public View getCustomView() {
        return null;
    }

    @Override // android.view.ActionMode
    public MenuInflater getMenuInflater() {
        return new MenuInflater(this.mContext);
    }

    private void reset() {
        this.mFloatingToolbar.dismiss();
        this.mFloatingToolbarVisibilityHelper.deactivate();
        this.mOriginatingView.removeCallbacks(this.mMovingOff);
        this.mOriginatingView.removeCallbacks(this.mHideOff);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isViewStillActive() {
        return this.mOriginatingView.getWindowVisibility() == 0 && this.mOriginatingView.isShown();
    }

    /* access modifiers changed from: private */
    public static final class FloatingToolbarVisibilityHelper {
        private static final long MIN_SHOW_DURATION_FOR_MOVE_HIDE = 500;
        private boolean mActive;
        private boolean mHideRequested;
        private long mLastShowTime;
        private boolean mMoving;
        private boolean mOutOfBounds;
        private final FloatingToolbar mToolbar;
        private boolean mWindowFocused = true;

        public FloatingToolbarVisibilityHelper(FloatingToolbar toolbar) {
            this.mToolbar = (FloatingToolbar) Preconditions.checkNotNull(toolbar);
        }

        public void activate() {
            this.mHideRequested = false;
            this.mMoving = false;
            this.mOutOfBounds = false;
            this.mWindowFocused = true;
            this.mActive = true;
        }

        public void deactivate() {
            this.mActive = false;
            this.mToolbar.dismiss();
        }

        public void setHideRequested(boolean hide) {
            this.mHideRequested = hide;
        }

        public void setMoving(boolean moving) {
            boolean showingLongEnough = System.currentTimeMillis() - this.mLastShowTime > MIN_SHOW_DURATION_FOR_MOVE_HIDE;
            if (!moving || showingLongEnough) {
                this.mMoving = moving;
            }
        }

        public void setOutOfBounds(boolean outOfBounds) {
            this.mOutOfBounds = outOfBounds;
        }

        public void setWindowFocused(boolean windowFocused) {
            this.mWindowFocused = windowFocused;
        }

        public void updateToolbarVisibility() {
            if (this.mActive) {
                if (this.mHideRequested || this.mMoving || this.mOutOfBounds || !this.mWindowFocused) {
                    this.mToolbar.hide();
                    return;
                }
                this.mToolbar.show();
                this.mLastShowTime = System.currentTimeMillis();
            }
        }
    }
}
