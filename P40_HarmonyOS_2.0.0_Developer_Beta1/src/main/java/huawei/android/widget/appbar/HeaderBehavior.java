package huawei.android.widget.appbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

/* access modifiers changed from: package-private */
public abstract class HeaderBehavior<V extends View> extends ViewOffsetBehavior<V> {
    private static final int INVALID_POINTER = -1;
    private static final int MAX_VALUE = 1000;
    private int mActivePointerId = -1;
    private Runnable mFlingRunnable;
    private boolean mIsBeingDragged;
    private int mLastMotionY;
    OverScroller mScroller;
    private int mTouchSlop = -1;
    private VelocityTracker mVelocityTracker;

    HeaderBehavior() {
    }

    HeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
        if (r4 != 3) goto L_0x0068;
     */
    @Override // huawei.android.widget.appbar.HwCoordinatorLayout.Behavior
    public boolean onInterceptTouchEvent(HwCoordinatorLayout parent, V child, MotionEvent event) {
        if (event == null) {
            return false;
        }
        if (this.mTouchSlop < 0) {
            this.mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }
        if (event.getAction() == 2 && this.mIsBeingDragged) {
            return true;
        }
        int actionMasked = event.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    interceptTouchMoveEvent(event);
                }
            }
            this.mIsBeingDragged = false;
            this.mActivePointerId = -1;
            VelocityTracker velocityTracker = this.mVelocityTracker;
            if (velocityTracker != null) {
                velocityTracker.recycle();
                this.mVelocityTracker = null;
            }
        } else {
            this.mIsBeingDragged = false;
            int eventX = (int) event.getX();
            int eventY = (int) event.getY();
            if (canDragView(child) && parent.isPointInChildBounds(child, eventX, eventY)) {
                this.mLastMotionY = eventY;
                this.mActivePointerId = event.getPointerId(0);
                ensureVelocityTracker();
            }
        }
        VelocityTracker velocityTracker2 = this.mVelocityTracker;
        if (velocityTracker2 != null) {
            velocityTracker2.addMovement(event);
        }
        return this.mIsBeingDragged;
    }

    private void interceptTouchMoveEvent(MotionEvent event) {
        int pointerIndex;
        int activePointerId = this.mActivePointerId;
        if (activePointerId != -1 && (pointerIndex = event.findPointerIndex(activePointerId)) != -1) {
            int eventY = (int) event.getY(pointerIndex);
            if (this.mTouchSlop < Math.abs(eventY - this.mLastMotionY)) {
                this.mIsBeingDragged = true;
                this.mLastMotionY = eventY;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0020, code lost:
        if (r0 != 3) goto L_0x008b;
     */
    @Override // huawei.android.widget.appbar.HwCoordinatorLayout.Behavior
    public boolean onTouchEvent(HwCoordinatorLayout parent, V child, MotionEvent event) {
        if (this.mTouchSlop < 0) {
            this.mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }
        int actionMasked = event.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked == 1) {
                notifyAppBarIsMoved(child, false);
                VelocityTracker velocityTracker = this.mVelocityTracker;
                if (velocityTracker != null) {
                    velocityTracker.addMovement(event);
                    this.mVelocityTracker.computeCurrentVelocity(MAX_VALUE);
                    fling(parent, child, -getScrollRangeForDragFling(child), 0, this.mVelocityTracker.getYVelocity(this.mActivePointerId));
                }
            } else if (actionMasked == 2) {
                if (!touchMoveEvent(event, parent, child)) {
                    return false;
                }
                notifyAppBarIsMoved(child, true);
            }
            notifyAppBarIsMoved(child, false);
            this.mIsBeingDragged = false;
            this.mActivePointerId = -1;
            VelocityTracker velocityTracker2 = this.mVelocityTracker;
            if (velocityTracker2 != null) {
                velocityTracker2.recycle();
                this.mVelocityTracker = null;
            }
        } else {
            notifyAppBarIsMoved(child, true);
            int eventY = (int) event.getY();
            if (!parent.isPointInChildBounds(child, (int) event.getX(), eventY) || !canDragView(child)) {
                return false;
            }
            this.mLastMotionY = eventY;
            this.mActivePointerId = event.getPointerId(0);
            ensureVelocityTracker();
        }
        VelocityTracker velocityTracker3 = this.mVelocityTracker;
        if (velocityTracker3 != null) {
            velocityTracker3.addMovement(event);
        }
        return true;
    }

    private void notifyAppBarIsMoved(V child, boolean isAppBarMoved) {
        if (child instanceof HwAppBarLayout) {
            ((HwAppBarLayout) child).notifyAppBarIsMoved(isAppBarMoved);
        }
    }

    private boolean touchMoveEvent(MotionEvent event, HwCoordinatorLayout parent, V child) {
        int i;
        int activePointerIndex = event.findPointerIndex(this.mActivePointerId);
        if (activePointerIndex == -1) {
            return false;
        }
        int eventY = (int) event.getY(activePointerIndex);
        int deltaY = this.mLastMotionY - eventY;
        if (!this.mIsBeingDragged && Math.abs(deltaY) > (i = this.mTouchSlop)) {
            this.mIsBeingDragged = true;
            deltaY = deltaY > 0 ? deltaY - i : deltaY + i;
        }
        if (this.mIsBeingDragged) {
            this.mLastMotionY = eventY;
            scroll(parent, child, deltaY, getMaxDragOffset(child), 0);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int setHeaderTopBottomOffset(HwCoordinatorLayout parent, V header, int newOffset) {
        return setHeaderTopBottomOffset(parent, header, newOffset, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /* access modifiers changed from: package-private */
    public int setHeaderTopBottomOffset(HwCoordinatorLayout parent, V v, int newOffset, int minOffset, int maxOffset) {
        int realOffset;
        int curOffset = getTopAndBottomOffset();
        if (minOffset == 0 || curOffset < minOffset || curOffset > maxOffset || curOffset == (realOffset = MathUtils.clamp(newOffset, minOffset, maxOffset))) {
            return 0;
        }
        setTopAndBottomOffset(realOffset);
        return curOffset - realOffset;
    }

    /* access modifiers changed from: package-private */
    public int getTopBottomOffsetForScrollingSibling() {
        return getTopAndBottomOffset();
    }

    /* access modifiers changed from: package-private */
    public final int scroll(HwCoordinatorLayout coordinatorLayout, V header, int deltaY, int minOffset, int maxOffset) {
        return setHeaderTopBottomOffset(coordinatorLayout, header, getTopBottomOffsetForScrollingSibling() - deltaY, minOffset, maxOffset);
    }

    /* access modifiers changed from: package-private */
    public final boolean fling(HwCoordinatorLayout coordinatorLayout, V layout, int minOffset, int maxOffset, float velocityY) {
        Runnable runnable = this.mFlingRunnable;
        if (runnable != null) {
            layout.removeCallbacks(runnable);
            this.mFlingRunnable = null;
        }
        if (this.mScroller == null) {
            this.mScroller = new OverScroller(layout.getContext());
        }
        this.mScroller.fling(0, getTopAndBottomOffset(), 0, Math.round(velocityY), 0, 0, minOffset, maxOffset);
        if (this.mScroller.computeScrollOffset()) {
            this.mFlingRunnable = new FlingRunnable(coordinatorLayout, layout);
            postOnAnimation(layout, this.mFlingRunnable);
            return true;
        }
        onFlingFinished(coordinatorLayout, layout);
        return false;
    }

    public void postOnAnimation(View view, Runnable action) {
        view.postDelayed(action, getFrameTime());
    }

    /* access modifiers changed from: package-private */
    public long getFrameTime() {
        return ValueAnimator.getFrameDelay();
    }

    /* access modifiers changed from: package-private */
    public void onFlingFinished(HwCoordinatorLayout parent, V v) {
    }

    /* access modifiers changed from: package-private */
    public boolean canDragView(V v) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public int getMaxDragOffset(V view) {
        return -view.getHeight();
    }

    /* access modifiers changed from: package-private */
    public int getScrollRangeForDragFling(V view) {
        return view.getHeight();
    }

    private void ensureVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    /* access modifiers changed from: private */
    public class FlingRunnable implements Runnable {
        private final V mLayout;
        private final HwCoordinatorLayout mParent;

        FlingRunnable(HwCoordinatorLayout parent, V layout) {
            this.mParent = parent;
            this.mLayout = layout;
        }

        /* JADX DEBUG: Multi-variable search result rejected for r0v6, resolved type: huawei.android.widget.appbar.HeaderBehavior */
        /* JADX DEBUG: Multi-variable search result rejected for r0v7, resolved type: huawei.android.widget.appbar.HeaderBehavior */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.lang.Runnable
        public void run() {
            if (this.mLayout != null && HeaderBehavior.this.mScroller != null) {
                if (HeaderBehavior.this.mScroller.computeScrollOffset()) {
                    HeaderBehavior headerBehavior = HeaderBehavior.this;
                    headerBehavior.setHeaderTopBottomOffset(this.mParent, this.mLayout, headerBehavior.mScroller.getCurrY());
                    HeaderBehavior.this.postOnAnimation(this.mLayout, this);
                    return;
                }
                HeaderBehavior.this.onFlingFinished(this.mParent, this.mLayout);
            }
        }
    }
}
