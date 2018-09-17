package com.android.internal.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.ActionMode;
import android.view.ActionMode.Callback2;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import com.android.internal.R;
import com.android.internal.util.Preconditions;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuBuilder.Callback;
import com.android.internal.widget.FloatingToolbar;
import java.util.Arrays;

public final class FloatingActionMode extends ActionMode {
    private static final int MAX_HIDE_DURATION = 3000;
    private static final int MOVING_HIDE_DELAY = 50;
    private final int mBottomAllowance;
    private final Callback2 mCallback;
    private final Rect mContentRect;
    private final Rect mContentRectOnScreen;
    private final Context mContext;
    private final Point mDisplaySize;
    private FloatingToolbar mFloatingToolbar;
    private FloatingToolbarVisibilityHelper mFloatingToolbarVisibilityHelper;
    private final Runnable mHideOff = new Runnable() {
        public void run() {
            if (FloatingActionMode.this.isViewStillActive()) {
                FloatingActionMode.this.mFloatingToolbarVisibilityHelper.setHideRequested(false);
                FloatingActionMode.this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
            }
        }
    };
    private final MenuBuilder mMenu;
    private final Runnable mMovingOff = new Runnable() {
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

    private static final class FloatingToolbarVisibilityHelper {
        private boolean mActive;
        private boolean mHideRequested;
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
            this.mMoving = moving;
        }

        public void setOutOfBounds(boolean outOfBounds) {
            this.mOutOfBounds = outOfBounds;
        }

        public void setWindowFocused(boolean windowFocused) {
            this.mWindowFocused = windowFocused;
        }

        public void updateToolbarVisibility() {
            if (this.mActive) {
                if (this.mHideRequested || this.mMoving || this.mOutOfBounds || (this.mWindowFocused ^ 1) != 0) {
                    this.mToolbar.hide();
                } else {
                    this.mToolbar.show();
                }
            }
        }
    }

    public FloatingActionMode(Context context, Callback2 callback, View originatingView, FloatingToolbar floatingToolbar) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mCallback = (Callback2) Preconditions.checkNotNull(callback);
        this.mMenu = new MenuBuilder(context).setDefaultShowAsAction(1);
        setType(1);
        this.mMenu.setCallback(new Callback() {
            public void onMenuModeChange(MenuBuilder menu) {
            }

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
        this.mFloatingToolbar = floatingToolbar.setMenu(this.mMenu).setOnMenuItemClickListener(new -$Lambda$IoKM3AcgDw3Ok5aFi0zlym2p3IA(this));
        this.mFloatingToolbarVisibilityHelper = new FloatingToolbarVisibilityHelper(this.mFloatingToolbar);
        this.mFloatingToolbarVisibilityHelper.activate();
    }

    /* synthetic */ boolean lambda$-com_android_internal_view_FloatingActionMode_5017(MenuItem item) {
        return this.mMenu.performItemAction(item, 0);
    }

    public void setTitle(CharSequence title) {
    }

    public void setTitle(int resId) {
    }

    public void setSubtitle(CharSequence subtitle) {
    }

    public void setSubtitle(int resId) {
    }

    public void setCustomView(View view) {
    }

    public void invalidate() {
        this.mCallback.onPrepareActionMode(this, this.mMenu);
        invalidateContentRect();
    }

    public void invalidateContentRect() {
        this.mCallback.onGetContentRect(this, this.mOriginatingView, this.mContentRect);
        repositionToolbar();
    }

    public void updateViewLocationInWindow() {
        this.mOriginatingView.getLocationOnScreen(this.mViewPositionOnScreen);
        this.mOriginatingView.getRootView().getLocationOnScreen(this.mRootViewPositionOnScreen);
        this.mOriginatingView.getGlobalVisibleRect(this.mViewRectOnScreen);
        this.mViewRectOnScreen.offset(this.mRootViewPositionOnScreen[0], this.mRootViewPositionOnScreen[1]);
        if (!Arrays.equals(this.mViewPositionOnScreen, this.mPreviousViewPositionOnScreen) || (this.mViewRectOnScreen.equals(this.mPreviousViewRectOnScreen) ^ 1) != 0) {
            repositionToolbar();
            this.mPreviousViewPositionOnScreen[0] = this.mViewPositionOnScreen[0];
            this.mPreviousViewPositionOnScreen[1] = this.mViewPositionOnScreen[1];
            this.mPreviousViewRectOnScreen.set(this.mViewRectOnScreen);
        }
    }

    private void repositionToolbar() {
        this.mContentRectOnScreen.set(this.mContentRect);
        this.mContentRectOnScreen.offset(this.mViewPositionOnScreen[0], this.mViewPositionOnScreen[1]);
        if (isContentRectWithinBounds()) {
            this.mFloatingToolbarVisibilityHelper.setOutOfBounds(false);
            this.mContentRectOnScreen.set(Math.max(this.mContentRectOnScreen.left, this.mViewRectOnScreen.left), Math.max(this.mContentRectOnScreen.top, this.mViewRectOnScreen.top), Math.min(this.mContentRectOnScreen.right, this.mViewRectOnScreen.right), Math.min(this.mContentRectOnScreen.bottom, this.mViewRectOnScreen.bottom + this.mBottomAllowance));
            if (!this.mContentRectOnScreen.equals(this.mPreviousContentRectOnScreen)) {
                this.mOriginatingView.removeCallbacks(this.mMovingOff);
                this.mFloatingToolbarVisibilityHelper.setMoving(true);
                this.mOriginatingView.postDelayed(this.mMovingOff, 50);
                this.mFloatingToolbar.setContentRect(this.mContentRectOnScreen);
                this.mFloatingToolbar.updateLayout();
            }
        } else {
            this.mFloatingToolbarVisibilityHelper.setOutOfBounds(true);
            this.mContentRectOnScreen.setEmpty();
        }
        this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
        this.mPreviousContentRectOnScreen.set(this.mContentRectOnScreen);
    }

    private boolean isContentRectWithinBounds() {
        ((WindowManager) this.mContext.getSystemService(WindowManager.class)).getDefaultDisplay().getRealSize(this.mDisplaySize);
        this.mScreenRect.set(0, 0, this.mDisplaySize.x, this.mDisplaySize.y);
        if (intersectsClosed(this.mContentRectOnScreen, this.mScreenRect)) {
            return intersectsClosed(this.mContentRectOnScreen, this.mViewRectOnScreen);
        }
        return false;
    }

    private static boolean intersectsClosed(Rect a, Rect b) {
        if (a.left > b.right || b.left > a.right || a.top > b.bottom || b.top > a.bottom) {
            return false;
        }
        return true;
    }

    public void hide(long duration) {
        if (duration == -1) {
            duration = ViewConfiguration.getDefaultActionModeHideDuration();
        }
        duration = Math.min(3000, duration);
        this.mOriginatingView.removeCallbacks(this.mHideOff);
        if (duration <= 0) {
            this.mHideOff.run();
            return;
        }
        this.mFloatingToolbarVisibilityHelper.setHideRequested(true);
        this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
        this.mOriginatingView.postDelayed(this.mHideOff, duration);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        this.mFloatingToolbarVisibilityHelper.setWindowFocused(hasWindowFocus);
        this.mFloatingToolbarVisibilityHelper.updateToolbarVisibility();
    }

    public void finish() {
        reset();
        this.mCallback.onDestroyActionMode(this);
    }

    public Menu getMenu() {
        return this.mMenu;
    }

    public CharSequence getTitle() {
        return null;
    }

    public CharSequence getSubtitle() {
        return null;
    }

    public View getCustomView() {
        return null;
    }

    public MenuInflater getMenuInflater() {
        return new MenuInflater(this.mContext);
    }

    private void reset() {
        this.mFloatingToolbar.dismiss();
        this.mFloatingToolbarVisibilityHelper.deactivate();
        this.mOriginatingView.removeCallbacks(this.mMovingOff);
        this.mOriginatingView.removeCallbacks(this.mHideOff);
    }

    private boolean isViewStillActive() {
        if (this.mOriginatingView.getWindowVisibility() == 0) {
            return this.mOriginatingView.isShown();
        }
        return false;
    }
}
