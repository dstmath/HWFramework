package huawei.android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.HwSpringBackHelper;
import android.widget.OverScroller;

public class ListViewFlingCoordinator {
    private static final int OVER_SCROLL_NEVER = 2;
    private static final int SCROLL_AXIS_VERTICAL = 2;
    private static final int TOUCH_MODE_OVERFLING = 6;
    private static final int TOUCH_MODE_OVER_SCROLL = 5;
    private static final int TOUCH_MODE_REST = -1;
    private static final int TOUCH_MODE_SCROLL = 3;
    private AbsListView mAbsListView;
    private Context mContext;
    private EdgeFlingRunnable mEdgeFlingRunnable;
    private ScrollViewStatusChecker mHeaderScrollViewStatusChecker;
    private int mLastFlingY;
    private int mLastMotionY = Integer.MAX_VALUE;
    private int mLastTouchEvent;
    private int mOverFlingDistance;
    private OverFlingRunnable mOverFlingRunnable;
    private OverScroller mScroller;

    static /* synthetic */ int access$812(ListViewFlingCoordinator x0, int x1) {
        int i = x0.mLastFlingY + x1;
        x0.mLastFlingY = i;
        return i;
    }

    @TargetApi(9)
    public ListViewFlingCoordinator(AbsListView absListView) {
        this.mAbsListView = absListView;
        this.mContext = this.mAbsListView.getContext();
        this.mScroller = new OverScroller(this.mContext);
        setNestedScrollParentStatusChecker(new HeaderScrollViewStatusChecker(absListView));
        this.mOverFlingDistance = ViewConfiguration.get(this.mContext).getScaledOverflingDistance();
        this.mEdgeFlingRunnable = new EdgeFlingRunnable();
        this.mOverFlingRunnable = new OverFlingRunnable();
    }

    public void onInterceptTouchEvent(MotionEvent event) {
        if (event != null) {
            this.mLastTouchEvent = event.getActionMasked();
            if (this.mLastTouchEvent == 0) {
                resetFlingRunnableState();
                this.mLastMotionY = (int) event.getY();
                this.mLastFlingY = 0;
            }
        }
    }

    public void onTouchEvent(MotionEvent event) {
        if (event != null) {
            this.mLastTouchEvent = event.getActionMasked();
            if (this.mLastTouchEvent == 1) {
                this.mLastMotionY = Integer.MAX_VALUE;
            }
        }
    }

    public void startScrollerOnFling(boolean isFlingConsumed, int velocityY) {
        if (!isFlingConsumed && velocityY != 0) {
            if (!isFlingRunnableFinished()) {
                resetFlingRunnableState();
            }
            this.mScroller.fling(0, 0, 0, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
    }

    public boolean isNeedOverFlingMoreAtEdge(boolean isAtEnd, int overshoot, int deltaY) {
        if (this.mHeaderScrollViewStatusChecker.getScrollingViewStatus() == -2) {
            return true;
        }
        if (!isAtEnd) {
            this.mScroller.computeScrollOffset();
            this.mLastFlingY = this.mScroller.getCurrY();
            return false;
        } else if (this.mLastTouchEvent != 1 || deltaY == 0) {
            return false;
        } else {
            int offset = overshoot + deltaY;
            this.mLastFlingY -= offset;
            startEdgeFling(this.mScroller, deltaY, offset);
            return false;
        }
    }

    public int getHeaderScrollViewHeight() {
        return this.mHeaderScrollViewStatusChecker.getScrollingViewHeight();
    }

    public boolean isNeedFlingOnTop() {
        return this.mHeaderScrollViewStatusChecker.getScrollingViewStatus() != -2;
    }

    public boolean checkNestedScrollEnabled(int deltaY) {
        boolean canNestedScroll = false;
        int status = this.mHeaderScrollViewStatusChecker.getScrollingViewStatus();
        boolean z = true;
        if (deltaY < 0) {
            canNestedScroll = status != 0;
        }
        if (deltaY > 0) {
            if (status == 2) {
                z = false;
            }
            canNestedScroll = z;
        }
        if (this.mAbsListView.getTouchMode() == 5 && status == 0) {
            return false;
        }
        return canNestedScroll;
    }

    public boolean isOverScrollEnabled(int directionY) {
        int status = this.mHeaderScrollViewStatusChecker.getScrollingViewStatus();
        if (status == -2 || status == -1) {
            return true;
        }
        int scrollY = this.mAbsListView.getScrollY();
        if (((status == 0 || status == 3) && scrollY <= 0) || ((status == 2 && scrollY >= 0) || (status == 0 && scrollY >= 0 && directionY > 0))) {
            return true;
        }
        return false;
    }

    public boolean isNestedPreScrollEnabled() {
        int status = this.mHeaderScrollViewStatusChecker.getScrollingViewStatus();
        return this.mAbsListView.getTouchMode() != 5 || !(status == 0 || status == 3) || this.mAbsListView.getScrollY() >= 0;
    }

    private void resetFlingRunnableState() {
        if (!this.mOverFlingRunnable.isFinished()) {
            this.mOverFlingRunnable.abortAnimation();
            this.mAbsListView.resetOverScrollState();
        }
        if (!this.mEdgeFlingRunnable.isFinished()) {
            this.mEdgeFlingRunnable.abortAnimation();
        }
    }

    private void setNestedScrollParentStatusChecker(ScrollViewStatusChecker scrollViewStatusChecker) {
        this.mHeaderScrollViewStatusChecker = scrollViewStatusChecker;
    }

    private void startEdgeFling(OverScroller scroller, int deltaY, int offset) {
        if (!isFlingRunnableFinished()) {
            resetFlingRunnableState();
        }
        OverScroller parentOverScroller = this.mAbsListView.getOverScroller();
        if (parentOverScroller != null) {
            parentOverScroller.abortAnimation();
        }
        if (this.mAbsListView.getOverScrollMode() != 2) {
            HwSpringBackHelper parentSpringBackHelper = this.mAbsListView.getSpringBackHelper();
            if (parentSpringBackHelper != null) {
                parentSpringBackHelper.abortAnimation();
            }
            this.mAbsListView.startNestedScroll(2);
            this.mEdgeFlingRunnable.fling(deltaY, offset);
        }
    }

    private boolean isFlingRunnableFinished() {
        boolean isEdgeFlingFinished = this.mEdgeFlingRunnable.isFinished();
        boolean isOverFlingFinished = this.mOverFlingRunnable.isFinished();
        if (!isEdgeFlingFinished || !isOverFlingFinished) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public class EdgeFlingRunnable implements Runnable {
        private static final int NESTED_SCROLL_AND_OVER_SCROLL_STOPPED = 0;
        private static final int NESTED_SCROLL_STARTED_AND_OVER_SCROLL_STOPPED = 1;
        private static final int NESTED_SCROLL_STOPPED_AND_OVER_SCROLL_STARTED = 2;
        private static final int SCROLL_CONSUMED_ARRAY_SIZE = 2;
        private int mDeltaY;
        private int mDyLeft;
        private int mEdgeFlingStage;
        private boolean mIsFinished;
        private int mOffset;
        private int[] mScrollConsumeds;

        private EdgeFlingRunnable() {
            this.mIsFinished = true;
            this.mEdgeFlingStage = 0;
            this.mScrollConsumeds = new int[2];
        }

        /* access modifiers changed from: package-private */
        public void fling(int deltaY, int offset) {
            this.mDeltaY = deltaY;
            this.mEdgeFlingStage = 1;
            this.mIsFinished = false;
            this.mOffset = offset;
            run();
        }

        @Override // java.lang.Runnable
        public void run() {
            if (!this.mIsFinished) {
                if (this.mDyLeft < 0 && this.mEdgeFlingStage == 2) {
                    ListViewFlingCoordinator.this.mAbsListView.overScrollYBy(this.mDyLeft, ListViewFlingCoordinator.this.mAbsListView.getScrollY(), 0, ListViewFlingCoordinator.this.mOverFlingDistance, false);
                    startOverFling(this.mDeltaY, ListViewFlingCoordinator.this.mScroller.getCurrVelocity());
                    abortAnimation();
                    this.mDyLeft = 0;
                } else if (ListViewFlingCoordinator.this.mScroller.computeScrollOffset()) {
                    nestedScrollProc();
                    if (ListViewFlingCoordinator.this.mScroller.isFinished()) {
                        abortAnimation();
                    } else {
                        ListViewFlingCoordinator.this.mAbsListView.postOnAnimation(this);
                    }
                } else {
                    abortAnimation();
                }
            }
        }

        private void nestedScrollProc() {
            int[] scrollConsumeds = this.mScrollConsumeds;
            int currY = ListViewFlingCoordinator.this.mScroller.getCurrY();
            if (Math.abs(ListViewFlingCoordinator.this.mLastFlingY) > Math.abs(currY)) {
                ListViewFlingCoordinator.access$812(ListViewFlingCoordinator.this, this.mOffset);
            }
            if (Math.abs(ListViewFlingCoordinator.this.mLastFlingY) > Math.abs(currY)) {
                ListViewFlingCoordinator.this.mLastFlingY = 0;
            }
            int deltaY = currY - ListViewFlingCoordinator.this.mLastFlingY;
            ListViewFlingCoordinator.this.mLastFlingY = currY;
            int lastHeaderScrollViewHeight = ListViewFlingCoordinator.this.mHeaderScrollViewStatusChecker.getScrollingViewHeight();
            if (ListViewFlingCoordinator.this.mAbsListView.dispatchNestedPreScroll(0, deltaY, scrollConsumeds, null)) {
                deltaY -= scrollConsumeds[1];
            }
            if (ListViewFlingCoordinator.this.mHeaderScrollViewStatusChecker.getScrollingViewStatus() == -2) {
                abortAnimation();
            } else if (deltaY == 0 || !ListViewFlingCoordinator.this.mAbsListView.dispatchNestedScroll(0, 0, 0, deltaY, null)) {
                startOverFling(this.mDeltaY, ListViewFlingCoordinator.this.mScroller.getCurrVelocity());
                abortAnimation();
            } else {
                float velocity = ListViewFlingCoordinator.this.mScroller.getCurrVelocity();
                int headerScrollViewStatus = ListViewFlingCoordinator.this.mHeaderScrollViewStatusChecker.getScrollingViewStatus();
                if (velocity <= 0.0f) {
                    return;
                }
                if ((headerScrollViewStatus == 0 && this.mDeltaY > 0) || (headerScrollViewStatus == 2 && this.mDeltaY < 0)) {
                    this.mDyLeft = (ListViewFlingCoordinator.this.mHeaderScrollViewStatusChecker.getScrollingViewHeight() - lastHeaderScrollViewHeight) + deltaY;
                    if (deltaY >= 0 || this.mDyLeft >= 0) {
                        startOverFling(this.mDeltaY, velocity);
                        abortAnimation();
                        return;
                    }
                    this.mEdgeFlingStage = 2;
                    run();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isFinished() {
            return this.mIsFinished;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @TargetApi(9)
        private void abortAnimation() {
            ListViewFlingCoordinator.this.mAbsListView.stopNestedScroll();
            if (ListViewFlingCoordinator.this.mScroller != null) {
                ListViewFlingCoordinator.this.mScroller.abortAnimation();
            }
            this.mEdgeFlingStage = 0;
            ListViewFlingCoordinator.this.mLastFlingY = 0;
            this.mIsFinished = true;
            ListViewFlingCoordinator.this.mAbsListView.setTouchMode(-1);
        }

        private void startOverFling(int deltaY, float velocity) {
            this.mEdgeFlingStage = 2;
            ListViewFlingCoordinator.this.mAbsListView.setTouchMode(6);
            if (deltaY < 0) {
                velocity = -velocity;
            }
            ListViewFlingCoordinator.this.mOverFlingRunnable.start(-velocity);
            ListViewFlingCoordinator.this.mScroller.abortAnimation();
        }
    }

    /* access modifiers changed from: private */
    public class OverFlingRunnable implements Runnable {
        private HwSpringBackHelper mHwSpringBackHelper;
        private boolean mIsFinished = true;

        OverFlingRunnable() {
            if (ListViewFlingCoordinator.this.mAbsListView.hasAnimatorMask()) {
                this.mHwSpringBackHelper = HwWidgetFactory.getHwSpringBackHelper();
            }
        }

        /* access modifiers changed from: package-private */
        public void start(float velocity) {
            if (velocity == 0.0f) {
                this.mIsFinished = true;
                return;
            }
            HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
            if (hwSpringBackHelper != null) {
                hwSpringBackHelper.overFling(velocity, ListViewFlingCoordinator.this.mAbsListView.getScrollY(), 0);
                this.mIsFinished = false;
                ListViewFlingCoordinator.this.mAbsListView.postOnAnimation(this);
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            HwSpringBackHelper hwSpringBackHelper;
            if (!this.mIsFinished && (hwSpringBackHelper = this.mHwSpringBackHelper) != null) {
                boolean isScrollFinished = !hwSpringBackHelper.computeScrollOffset();
                int scrollY = ListViewFlingCoordinator.this.mAbsListView.getScrollY();
                ListViewFlingCoordinator.this.mAbsListView.overScrollYBy(this.mHwSpringBackHelper.getCurrentOffset() - scrollY, scrollY, 0, ListViewFlingCoordinator.this.mOverFlingDistance, false);
                ListViewFlingCoordinator.this.mAbsListView.postOnAnimation(this);
                if (isScrollFinished) {
                    ListViewFlingCoordinator.this.mAbsListView.setTouchMode(-1);
                    abortAnimation();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void abortAnimation() {
            this.mIsFinished = true;
            HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
            if (hwSpringBackHelper != null) {
                hwSpringBackHelper.abortAnimation();
            }
            this.mHwSpringBackHelper = HwWidgetFactory.getHwSpringBackHelper();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isFinished() {
            return this.mIsFinished;
        }
    }
}
