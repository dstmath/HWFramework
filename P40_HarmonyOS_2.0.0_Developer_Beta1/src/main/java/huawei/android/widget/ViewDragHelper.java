package huawei.android.widget;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import java.util.Arrays;

public class ViewDragHelper {
    private static final int BASE_SETTLE_DURATION = 256;
    public static final int DIRECTION_ALL = 3;
    public static final int DIRECTION_HORIZONTAL = 1;
    public static final int DIRECTION_VERTICAL = 2;
    private static final double DISTANCE_INFLUENCE_FOR_SNAP_RATE = 0.4712389167638204d;
    public static final int EDGE_ALL = 15;
    public static final int EDGE_BOTTOM = 8;
    public static final int EDGE_LEFT = 1;
    public static final int EDGE_RIGHT = 2;
    private static final int EDGE_SIZE = 20;
    public static final int EDGE_TOP = 4;
    private static final int FOUR_TIMES_RATE = 4;
    private static final float HALF_RATE = 0.5f;
    private static final Interpolator INTERPOLATOR = new Interpolator() {
        /* class huawei.android.widget.ViewDragHelper.AnonymousClass1 */

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float value) {
            float param = value - 1.0f;
            return (param * param * param * param * param) + 1.0f;
        }
    };
    public static final int INVALID_POINTER = -1;
    private static final int MAX_SETTLE_DURATION = 600;
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_SETTLING = 2;
    private static final String TAG = "ViewDragHelper";
    private static final int VELOCITY_UNITS = 1000;
    private int mActivePointerId = -1;
    private final Callback mCallback;
    private View mCapturedView;
    private int mDragState;
    private int[] mEdgeDragsInProgressPoints;
    private int[] mEdgeDragsLockedPoints;
    private int mEdgeSize;
    private int[] mInitialEdgesTouchedPoints;
    private float[] mInitialMotionxPoints;
    private float[] mInitialMotionyPoints;
    private boolean mIsReleaseInProgress;
    private float[] mLastMotionxPoints;
    private float[] mLastMotionyPoints;
    private float mMaxVelocity;
    private float mMinVelocity;
    private final ViewGroup mParentView;
    private int mPointersDown;
    private Scroller mScroller;
    private final Runnable mSetIdleRunnable = new Runnable() {
        /* class huawei.android.widget.ViewDragHelper.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            ViewDragHelper.this.setDragState(0);
        }
    };
    private int mTouchSlop;
    private int mTrackingEdges;
    private VelocityTracker mVelocityTracker;

    public static abstract class Callback {
        public abstract boolean tryCaptureView(View view, int i);

        public void onViewDragStateChanged(int state) {
        }

        public void onViewPositionChanged(View changedView, int left, int top, int deltaX, int deltaY) {
        }

        public void onViewCaptured(View capturedChild, int activePointerId) {
        }

        public void onViewReleased(View releasedChild, float velX, float velY) {
        }

        public void onEdgeTouched(int edgeFlags, int pointerId) {
        }

        public boolean onEdgeLock(int edgeFlags) {
            return false;
        }

        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
        }

        public int getOrderedChildIndex(int index) {
            return index;
        }

        public int getViewHorizontalDragRange(View child) {
            return 0;
        }

        public int getViewVerticalDragRange(View child) {
            return 0;
        }

        public int clampViewPositionHorizontal(View child, int left, int deltaX) {
            return 0;
        }

        public int clampViewPositionVertical(View child, int top, int deltaY) {
            return 0;
        }
    }

    private ViewDragHelper(Context context, ViewGroup forParent, Callback callback) {
        if (forParent == null) {
            throw new IllegalArgumentException("Parent view may not be null");
        } else if (callback != null) {
            this.mParentView = forParent;
            this.mCallback = callback;
            ViewConfiguration configuration = ViewConfiguration.get(context);
            this.mEdgeSize = (int) ((20.0f * context.getResources().getDisplayMetrics().density) + HALF_RATE);
            this.mTouchSlop = configuration.getScaledTouchSlop();
            this.mMaxVelocity = (float) configuration.getScaledMaximumFlingVelocity();
            this.mMinVelocity = (float) configuration.getScaledMinimumFlingVelocity();
            this.mScroller = new Scroller(context, INTERPOLATOR);
        } else {
            throw new IllegalArgumentException("Callback may not be null");
        }
    }

    public static ViewDragHelper create(ViewGroup forParent, Callback callback) {
        return new ViewDragHelper(forParent.getContext(), forParent, callback);
    }

    public static ViewDragHelper create(ViewGroup forParent, float sensitivity, Callback callback) {
        ViewDragHelper helper = create(forParent, callback);
        helper.mTouchSlop = (int) (((float) helper.mTouchSlop) * (1.0f / sensitivity));
        return helper;
    }

    public void setMinVelocity(float minVel) {
        this.mMinVelocity = minVel;
    }

    public float getMinVelocity() {
        return this.mMinVelocity;
    }

    public int getViewDragState() {
        return this.mDragState;
    }

    public void setEdgeTrackingEnabled(int edgeFlags) {
        this.mTrackingEdges = edgeFlags;
    }

    public int getEdgeSize() {
        return this.mEdgeSize;
    }

    public void captureChildView(View childView, int activePointerId) {
        if (childView.getParent() == this.mParentView) {
            this.mCapturedView = childView;
            this.mActivePointerId = activePointerId;
            this.mCallback.onViewCaptured(childView, activePointerId);
            setDragState(1);
            return;
        }
        throw new IllegalArgumentException("captureChildView: parameter must be a descendant of the ViewDragHelper's tracked parent view (" + this.mParentView + ")");
    }

    public View getCapturedView() {
        return this.mCapturedView;
    }

    public int getActivePointerId() {
        return this.mActivePointerId;
    }

    public int getTouchSlop() {
        return this.mTouchSlop;
    }

    public void cancel() {
        this.mActivePointerId = -1;
        clearMotionHistory();
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void abort() {
        cancel();
        if (this.mDragState == 2) {
            int oldX = this.mScroller.getCurrX();
            int oldY = this.mScroller.getCurrY();
            this.mScroller.abortAnimation();
            int newX = this.mScroller.getCurrX();
            int newY = this.mScroller.getCurrY();
            this.mCallback.onViewPositionChanged(this.mCapturedView, newX, newY, newX - oldX, newY - oldY);
        }
        setDragState(0);
    }

    public boolean smoothSlideViewTo(View child, int finalLeft, int finalTop) {
        this.mCapturedView = child;
        this.mActivePointerId = -1;
        boolean isContinueSliding = forceSettleCapturedViewAt(finalLeft, finalTop, 0, 0);
        if (!isContinueSliding && this.mDragState == 0 && this.mCapturedView != null) {
            this.mCapturedView = null;
        }
        return isContinueSliding;
    }

    public boolean settleCapturedViewAt(int finalLeft, int finalTop) {
        if (this.mIsReleaseInProgress) {
            return forceSettleCapturedViewAt(finalLeft, finalTop, (int) this.mVelocityTracker.getXVelocity(this.mActivePointerId), (int) this.mVelocityTracker.getYVelocity(this.mActivePointerId));
        }
        throw new IllegalStateException("Cannot settleCapturedViewAt outside of a call to Callback#onViewReleased");
    }

    private boolean forceSettleCapturedViewAt(int finalLeft, int finalTop, int velX, int velY) {
        int startLeft = this.mCapturedView.getLeft();
        int startTop = this.mCapturedView.getTop();
        int deltaX = finalLeft - startLeft;
        int deltaY = finalTop - startTop;
        if (deltaX == 0 && deltaY == 0) {
            this.mScroller.abortAnimation();
            setDragState(0);
            return false;
        }
        this.mScroller.startScroll(startLeft, startTop, deltaX, deltaY, computeSettleDuration(this.mCapturedView, deltaX, deltaY, velX, velY));
        setDragState(2);
        return true;
    }

    private int computeSettleDuration(View child, int deltaX, int deltaY, int velocityX, int velocityY) {
        float f;
        float f2;
        float f3;
        float f4;
        int velX = clampMag(velocityX, (int) this.mMinVelocity, (int) this.mMaxVelocity);
        int velY = clampMag(velocityY, (int) this.mMinVelocity, (int) this.mMaxVelocity);
        int absDx = Math.abs(deltaX);
        int absDy = Math.abs(deltaY);
        int absVelX = Math.abs(velX);
        int absVelY = Math.abs(velY);
        int addedVel = absVelX + absVelY;
        int addedDistance = absDx + absDy;
        if (velX != 0) {
            f2 = (float) absVelX;
            f = (float) addedVel;
        } else {
            f2 = (float) absDx;
            f = (float) addedDistance;
        }
        float weightX = f2 / f;
        if (velY != 0) {
            f4 = (float) absVelY;
            f3 = (float) addedVel;
        } else {
            f4 = (float) absDy;
            f3 = (float) addedDistance;
        }
        float weightY = f4 / f3;
        return (int) ((((float) computeAxisDuration(deltaX, velX, this.mCallback.getViewHorizontalDragRange(child))) * weightX) + (((float) computeAxisDuration(deltaY, velY, this.mCallback.getViewVerticalDragRange(child))) * weightY));
    }

    private int computeAxisDuration(int delta, int velocity, int motionRange) {
        int duration;
        if (delta == 0) {
            return 0;
        }
        int width = this.mParentView.getWidth();
        int halfWidth = width >> 1;
        float widthRatio = ((float) Math.abs(delta)) / ((float) width);
        float distance = ((float) halfWidth) + (((float) halfWidth) * distanceInfluenceForSnapDuration(widthRatio < 1.0f ? widthRatio : 1.0f));
        int tmpVelocity = Math.abs(velocity);
        if (tmpVelocity > 0) {
            duration = Math.round(Math.abs(distance / ((float) tmpVelocity)) * 1000.0f) * 4;
        } else {
            duration = (int) ((1.0f + (((float) Math.abs(delta)) / ((float) motionRange))) * 256.0f);
        }
        return duration < MAX_SETTLE_DURATION ? duration : MAX_SETTLE_DURATION;
    }

    private int clampMag(int value, int absMin, int absMax) {
        int absValue = Math.abs(value);
        if (absValue < absMin) {
            return 0;
        }
        if (absValue > absMax) {
            return value > 0 ? absMax : -absMax;
        }
        return value;
    }

    private float clampMag(float value, float absMin, float absMax) {
        float absValue = Math.abs(value);
        if (absValue < absMin) {
            return 0.0f;
        }
        if (absValue > absMax) {
            return value > 0.0f ? absMax : -absMax;
        }
        return value;
    }

    private float distanceInfluenceForSnapDuration(float value) {
        return (float) Math.sin(((double) (value - HALF_RATE)) * DISTANCE_INFLUENCE_FOR_SNAP_RATE);
    }

    public void flingCapturedView(int minLeft, int minTop, int maxLeft, int maxTop) {
        if (this.mIsReleaseInProgress) {
            this.mScroller.fling(this.mCapturedView.getLeft(), this.mCapturedView.getTop(), (int) this.mVelocityTracker.getXVelocity(this.mActivePointerId), (int) this.mVelocityTracker.getYVelocity(this.mActivePointerId), minLeft, maxLeft, minTop, maxTop);
            setDragState(2);
            return;
        }
        throw new IllegalStateException("Cannot flingCapturedView outside of a call to Callback#onViewReleased");
    }

    public boolean continueSettling(boolean isDeferCallbacks) {
        if (this.mDragState == 2) {
            boolean isKeepGoing = this.mScroller.computeScrollOffset();
            int currX = this.mScroller.getCurrX();
            int currY = this.mScroller.getCurrY();
            int deltaX = currX - this.mCapturedView.getLeft();
            int deltaY = currY - this.mCapturedView.getTop();
            if (deltaX != 0) {
                this.mCapturedView.offsetLeftAndRight(deltaX);
            }
            if (deltaY != 0) {
                this.mCapturedView.offsetTopAndBottom(deltaY);
            }
            if (!(deltaX == 0 && deltaY == 0)) {
                this.mCallback.onViewPositionChanged(this.mCapturedView, currX, currY, deltaX, deltaY);
            }
            if (isKeepGoing && this.mScroller.getFinalX() == currX && this.mScroller.getFinalY() == currY) {
                this.mScroller.abortAnimation();
                isKeepGoing = false;
            }
            if (!isKeepGoing) {
                if (isDeferCallbacks) {
                    this.mParentView.post(this.mSetIdleRunnable);
                } else {
                    setDragState(0);
                }
            }
        }
        if (this.mDragState == 2) {
            return true;
        }
        return false;
    }

    private void dispatchViewReleased(float velX, float velY) {
        this.mIsReleaseInProgress = true;
        this.mCallback.onViewReleased(this.mCapturedView, velX, velY);
        this.mIsReleaseInProgress = false;
        if (this.mDragState == 1) {
            setDragState(0);
        }
    }

    private void clearMotionHistory() {
        float[] fArr = this.mInitialMotionxPoints;
        if (fArr != null) {
            Arrays.fill(fArr, 0.0f);
            Arrays.fill(this.mInitialMotionyPoints, 0.0f);
            Arrays.fill(this.mLastMotionxPoints, 0.0f);
            Arrays.fill(this.mLastMotionyPoints, 0.0f);
            Arrays.fill(this.mInitialEdgesTouchedPoints, 0);
            Arrays.fill(this.mEdgeDragsInProgressPoints, 0);
            Arrays.fill(this.mEdgeDragsLockedPoints, 0);
            this.mPointersDown = 0;
        }
    }

    private void clearMotionHistory(int pointerId) {
        float[] fArr = this.mInitialMotionxPoints;
        if (fArr != null) {
            fArr[pointerId] = 0.0f;
            this.mInitialMotionyPoints[pointerId] = 0.0f;
            this.mLastMotionxPoints[pointerId] = 0.0f;
            this.mLastMotionyPoints[pointerId] = 0.0f;
            this.mInitialEdgesTouchedPoints[pointerId] = 0;
            this.mEdgeDragsInProgressPoints[pointerId] = 0;
            this.mEdgeDragsLockedPoints[pointerId] = 0;
            this.mPointersDown &= ~(1 << pointerId);
        }
    }

    private void ensureMotionHistorySizeForId(int pointerId) {
        float[] fArr = this.mInitialMotionxPoints;
        if (fArr == null || fArr.length <= pointerId) {
            float[] imxPoints = new float[(pointerId + 1)];
            float[] imyPoints = new float[(pointerId + 1)];
            float[] lmxPoints = new float[(pointerId + 1)];
            float[] lmyPoints = new float[(pointerId + 1)];
            int[] iitPoints = new int[(pointerId + 1)];
            int[] editPoints = new int[(pointerId + 1)];
            int[] edlPoints = new int[(pointerId + 1)];
            float[] fArr2 = this.mInitialMotionxPoints;
            if (fArr2 != null) {
                System.arraycopy(fArr2, 0, imxPoints, 0, fArr2.length);
                float[] fArr3 = this.mInitialMotionyPoints;
                System.arraycopy(fArr3, 0, imyPoints, 0, fArr3.length);
                float[] fArr4 = this.mLastMotionxPoints;
                System.arraycopy(fArr4, 0, lmxPoints, 0, fArr4.length);
                float[] fArr5 = this.mLastMotionyPoints;
                System.arraycopy(fArr5, 0, lmyPoints, 0, fArr5.length);
                int[] iArr = this.mInitialEdgesTouchedPoints;
                System.arraycopy(iArr, 0, iitPoints, 0, iArr.length);
                int[] iArr2 = this.mEdgeDragsInProgressPoints;
                System.arraycopy(iArr2, 0, editPoints, 0, iArr2.length);
                int[] iArr3 = this.mEdgeDragsLockedPoints;
                System.arraycopy(iArr3, 0, edlPoints, 0, iArr3.length);
            }
            this.mInitialMotionxPoints = imxPoints;
            this.mInitialMotionyPoints = imyPoints;
            this.mLastMotionxPoints = lmxPoints;
            this.mLastMotionyPoints = lmyPoints;
            this.mInitialEdgesTouchedPoints = iitPoints;
            this.mEdgeDragsInProgressPoints = editPoints;
            this.mEdgeDragsLockedPoints = edlPoints;
        }
    }

    private void saveInitialMotion(float eventX, float eventY, int pointerId) {
        ensureMotionHistorySizeForId(pointerId);
        this.mInitialMotionxPoints[pointerId] = eventX;
        this.mLastMotionxPoints[pointerId] = eventX;
        this.mInitialMotionyPoints[pointerId] = eventY;
        this.mLastMotionyPoints[pointerId] = eventY;
        this.mInitialEdgesTouchedPoints[pointerId] = getEdgesTouched((int) eventX, (int) eventY);
        this.mPointersDown |= 1 << pointerId;
    }

    private void saveLastMotion(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            int pointerId = event.getPointerId(i);
            float eventX = event.getX(i);
            float eventY = event.getY(i);
            this.mLastMotionxPoints[pointerId] = eventX;
            this.mLastMotionyPoints[pointerId] = eventY;
        }
    }

    public boolean isPointerDown(int pointerId) {
        return (this.mPointersDown & (1 << pointerId)) != 0;
    }

    /* access modifiers changed from: package-private */
    public void setDragState(int state) {
        this.mParentView.removeCallbacks(this.mSetIdleRunnable);
        if (this.mDragState != state) {
            this.mDragState = state;
            this.mCallback.onViewDragStateChanged(state);
            if (this.mDragState == 0) {
                this.mCapturedView = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean tryCaptureViewForDrag(View toCapture, int pointerId) {
        if (toCapture == this.mCapturedView && this.mActivePointerId == pointerId) {
            return true;
        }
        if (toCapture == null || !this.mCallback.tryCaptureView(toCapture, pointerId)) {
            return false;
        }
        this.mActivePointerId = pointerId;
        captureChildView(toCapture, pointerId);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean canScroll(View view, boolean isCheckV, int deltaX, int deltaY, int coordX, int coordY) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int scrollX = view.getScrollX();
            int scrollY = view.getScrollY();
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                int childScrollX = coordX + scrollX;
                int childScrollY = coordY + scrollY;
                int left = child.getLeft();
                int top = child.getTop();
                if (((left <= childScrollX && child.getRight() > childScrollX) && (top <= childScrollY && child.getBottom() > childScrollY)) && canScroll(child, true, deltaX, deltaY, childScrollX - left, childScrollY - top)) {
                    return true;
                }
            }
        }
        if (!isCheckV) {
            return false;
        }
        if (!view.canScrollHorizontally(-deltaX)) {
            if (!view.canScrollVertically(-deltaY)) {
                return false;
            }
        }
        return true;
    }

    public boolean shouldInterceptTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();
        if (action == 0) {
            cancel();
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);
        if (action != 0) {
            if (action != 1) {
                if (action != 2) {
                    if (action != 3) {
                        if (action == 5) {
                            interceptPointerDownEvent(event, actionIndex);
                        } else if (action == 6) {
                            clearMotionHistory(event.getPointerId(actionIndex));
                        }
                    }
                } else if (!(this.mInitialMotionxPoints == null || this.mInitialMotionyPoints == null)) {
                    interceptMoveEvent(event);
                }
            }
            cancel();
        } else {
            interceptDownEvent(event);
        }
        if (this.mDragState == 1) {
            return true;
        }
        return false;
    }

    private void interceptPointerDownEvent(MotionEvent event, int actionIndex) {
        View toCapture;
        int pointerId = event.getPointerId(actionIndex);
        float eventX = event.getX(actionIndex);
        float eventY = event.getY(actionIndex);
        saveInitialMotion(eventX, eventY, pointerId);
        if (this.mDragState == 0) {
            int edgesTouched = this.mInitialEdgesTouchedPoints[pointerId];
            int i = this.mTrackingEdges;
            if ((edgesTouched & i) != 0) {
                this.mCallback.onEdgeTouched(i & edgesTouched, pointerId);
                return;
            }
        }
        if (this.mDragState == 2 && (toCapture = findTopChildUnder((int) eventX, (int) eventY)) == this.mCapturedView) {
            tryCaptureViewForDrag(toCapture, pointerId);
        }
    }

    private void interceptDownEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        int pointerId = event.getPointerId(0);
        saveInitialMotion(eventX, eventY, pointerId);
        View toCapture = findTopChildUnder((int) eventX, (int) eventY);
        if (toCapture == this.mCapturedView && this.mDragState == 2) {
            tryCaptureViewForDrag(toCapture, pointerId);
        }
        int edgesTouched = this.mInitialEdgesTouchedPoints[pointerId];
        int i = this.mTrackingEdges;
        if ((edgesTouched & i) != 0) {
            this.mCallback.onEdgeTouched(i & edgesTouched, pointerId);
        }
    }

    private void interceptMoveEvent(MotionEvent event) {
        int pointerCount;
        MotionEvent motionEvent = event;
        int pointerCount2 = event.getPointerCount();
        int i = 0;
        while (true) {
            if (i >= pointerCount2) {
                break;
            }
            int pointerId = motionEvent.getPointerId(i);
            float eventX = motionEvent.getX(i);
            float eventY = motionEvent.getY(i);
            float deltaX = eventX - this.mInitialMotionxPoints[pointerId];
            float deltaY = eventY - this.mInitialMotionyPoints[pointerId];
            View toCapture = findTopChildUnder((int) eventX, (int) eventY);
            boolean isPastSlop = checkTouchSlop(toCapture, deltaX, deltaY);
            if (isPastSlop) {
                int oldLeft = toCapture.getLeft();
                int newLeft = this.mCallback.clampViewPositionHorizontal(toCapture, ((int) deltaX) + oldLeft, (int) deltaX);
                int oldTop = toCapture.getTop();
                pointerCount = pointerCount2;
                int newTop = this.mCallback.clampViewPositionVertical(toCapture, ((int) deltaY) + oldTop, (int) deltaY);
                int horizontalDragRange = this.mCallback.getViewHorizontalDragRange(toCapture);
                int verticalDragRange = this.mCallback.getViewVerticalDragRange(toCapture);
                boolean z = false;
                boolean isInvalidRange = (horizontalDragRange == 0 || horizontalDragRange > 0) & (newLeft == oldLeft);
                if (verticalDragRange == 0 || (verticalDragRange > 0 && newTop == oldTop)) {
                    z = true;
                }
                if (isInvalidRange && z) {
                    break;
                }
            } else {
                pointerCount = pointerCount2;
            }
            reportNewEdgeDrags(deltaX, deltaY, pointerId);
            if (this.mDragState == 1 || (isPastSlop && tryCaptureViewForDrag(toCapture, pointerId))) {
                break;
            }
            i++;
            motionEvent = event;
            pointerCount2 = pointerCount;
        }
        saveLastMotion(event);
    }

    public void processTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();
        if (action == 0) {
            cancel();
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);
        if (action == 0) {
            processDownEvent(event);
        } else if (action == 1) {
            if (this.mDragState == 1) {
                releaseViewForPointerUp();
            }
            cancel();
        } else if (action == 2) {
            processMoveEvent(event);
        } else if (action == 3) {
            if (this.mDragState == 1) {
                dispatchViewReleased(0.0f, 0.0f);
            }
            cancel();
        } else if (action == 5) {
            processPointerDownEvent(event, actionIndex);
        } else if (action == 6) {
            processPointerUpEvent(event, actionIndex);
        }
    }

    private void processDownEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        int pointerId = event.getPointerId(0);
        View toCapture = findTopChildUnder((int) eventX, (int) eventY);
        saveInitialMotion(eventX, eventY, pointerId);
        tryCaptureViewForDrag(toCapture, pointerId);
        int edgesTouched = this.mInitialEdgesTouchedPoints[pointerId];
        int i = this.mTrackingEdges;
        if ((edgesTouched & i) != 0) {
            this.mCallback.onEdgeTouched(i & edgesTouched, pointerId);
        }
    }

    private void processPointerDownEvent(MotionEvent event, int actionIndex) {
        int pointerId = event.getPointerId(actionIndex);
        float eventX = event.getX(actionIndex);
        float eventY = event.getY(actionIndex);
        saveInitialMotion(eventX, eventY, pointerId);
        if (this.mDragState == 0) {
            tryCaptureViewForDrag(findTopChildUnder((int) eventX, (int) eventY), pointerId);
            int edgesTouched = this.mInitialEdgesTouchedPoints[pointerId];
            int i = this.mTrackingEdges;
            if ((edgesTouched & i) != 0) {
                this.mCallback.onEdgeTouched(i & edgesTouched, pointerId);
                return;
            }
        }
        if (isCapturedViewUnder((int) eventX, (int) eventY)) {
            tryCaptureViewForDrag(this.mCapturedView, pointerId);
        }
    }

    private void processMoveEvent(MotionEvent event) {
        if (this.mDragState == 1) {
            int index = event.findPointerIndex(this.mActivePointerId);
            float eventX = event.getX(index);
            float eventY = event.getY(index);
            float[] fArr = this.mLastMotionxPoints;
            int i = this.mActivePointerId;
            int idX = (int) (eventX - fArr[i]);
            int idY = (int) (eventY - this.mLastMotionyPoints[i]);
            dragTo(this.mCapturedView.getLeft() + idX, this.mCapturedView.getTop() + idY, idX, idY);
            saveLastMotion(event);
            return;
        }
        int pointerCount = event.getPointerCount();
        for (int i2 = 0; i2 < pointerCount; i2++) {
            int pointerId = event.getPointerId(i2);
            float eventX2 = event.getX(i2);
            float eventY2 = event.getY(i2);
            float deltaX = eventX2 - this.mInitialMotionxPoints[pointerId];
            float deltaY = eventY2 - this.mInitialMotionyPoints[pointerId];
            reportNewEdgeDrags(deltaX, deltaY, pointerId);
            if (this.mDragState == 1) {
                break;
            }
            View toCapture = findTopChildUnder((int) eventX2, (int) eventY2);
            if (checkTouchSlop(toCapture, deltaX, deltaY) && tryCaptureViewForDrag(toCapture, pointerId)) {
                break;
            }
        }
        saveLastMotion(event);
    }

    private void processPointerUpEvent(MotionEvent event, int actionIndex) {
        int pointerId = event.getPointerId(actionIndex);
        if (this.mDragState == 1 && pointerId == this.mActivePointerId) {
            int newActivePointer = -1;
            int pointerCount = event.getPointerCount();
            int i = 0;
            while (true) {
                if (i >= pointerCount) {
                    break;
                }
                int id = event.getPointerId(i);
                if (id != this.mActivePointerId) {
                    View findTopChildUnder = findTopChildUnder((int) event.getX(i), (int) event.getY(i));
                    View view = this.mCapturedView;
                    if (findTopChildUnder == view && tryCaptureViewForDrag(view, id)) {
                        newActivePointer = this.mActivePointerId;
                        break;
                    }
                }
                i++;
            }
            if (newActivePointer == -1) {
                releaseViewForPointerUp();
            }
        }
        clearMotionHistory(pointerId);
    }

    private void reportNewEdgeDrags(float deltaX, float deltaY, int pointerId) {
        int dragsStarted = 0;
        if (checkNewEdgeDrag(deltaX, deltaY, pointerId, 1)) {
            dragsStarted = 0 | 1;
        }
        if (checkNewEdgeDrag(deltaY, deltaX, pointerId, 4)) {
            dragsStarted |= 4;
        }
        if (checkNewEdgeDrag(deltaX, deltaY, pointerId, 2)) {
            dragsStarted |= 2;
        }
        if (checkNewEdgeDrag(deltaY, deltaX, pointerId, 8)) {
            dragsStarted |= 8;
        }
        if (dragsStarted != 0) {
            int[] iArr = this.mEdgeDragsInProgressPoints;
            iArr[pointerId] = iArr[pointerId] | dragsStarted;
            this.mCallback.onEdgeDragStarted(dragsStarted, pointerId);
        }
    }

    private boolean checkNewEdgeDrag(float delta, float oldDelta, int pointerId, int edge) {
        float absDelta = Math.abs(delta);
        float absOldDelta = Math.abs(oldDelta);
        if (!(((this.mInitialEdgesTouchedPoints[pointerId] & edge) != edge) | ((this.mTrackingEdges & edge) == 0) | ((this.mEdgeDragsLockedPoints[pointerId] & edge) == edge)) && !((this.mEdgeDragsInProgressPoints[pointerId] & edge) == edge)) {
            int i = this.mTouchSlop;
            if (absDelta > ((float) i) || absOldDelta > ((float) i)) {
                if (absDelta >= HALF_RATE * absOldDelta || !this.mCallback.onEdgeLock(edge)) {
                    return (this.mEdgeDragsInProgressPoints[pointerId] & edge) == 0 && absDelta > ((float) this.mTouchSlop);
                }
                int[] iArr = this.mEdgeDragsLockedPoints;
                iArr[pointerId] = iArr[pointerId] | edge;
                return false;
            }
        }
        return false;
    }

    private boolean checkTouchSlop(View child, float deltaX, float deltaY) {
        if (child == null) {
            return false;
        }
        boolean isCheckHorizontal = this.mCallback.getViewHorizontalDragRange(child) > 0;
        boolean isCheckVertical = this.mCallback.getViewVerticalDragRange(child) > 0;
        if (isCheckHorizontal && isCheckVertical) {
            float f = (deltaX * deltaX) + (deltaY * deltaY);
            int i = this.mTouchSlop;
            if (f > ((float) (i * i))) {
                return true;
            }
            return false;
        } else if (isCheckHorizontal) {
            if (Math.abs(deltaX) > ((float) this.mTouchSlop)) {
                return true;
            }
            return false;
        } else if (!isCheckVertical || Math.abs(deltaY) <= ((float) this.mTouchSlop)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean checkTouchSlop(int directions) {
        int count = this.mInitialMotionxPoints.length;
        for (int i = 0; i < count; i++) {
            if (checkTouchSlop(directions, i)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkTouchSlop(int directions, int pointerId) {
        if (!isPointerDown(pointerId)) {
            return false;
        }
        boolean isCheckHorizontal = (directions & 1) == 1;
        boolean isCheckVertical = (directions & 2) == 2;
        float deltaX = this.mLastMotionxPoints[pointerId] - this.mInitialMotionxPoints[pointerId];
        float deltaY = this.mLastMotionyPoints[pointerId] - this.mInitialMotionyPoints[pointerId];
        if (isCheckHorizontal && isCheckVertical) {
            float f = (deltaX * deltaX) + (deltaY * deltaY);
            int i = this.mTouchSlop;
            if (f > ((float) (i * i))) {
                return true;
            }
            return false;
        } else if (isCheckHorizontal) {
            if (Math.abs(deltaX) > ((float) this.mTouchSlop)) {
                return true;
            }
            return false;
        } else if (!isCheckVertical || Math.abs(deltaY) <= ((float) this.mTouchSlop)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isEdgeTouched(int edges) {
        int count = this.mInitialEdgesTouchedPoints.length;
        for (int i = 0; i < count; i++) {
            if (isEdgeTouched(edges, i)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEdgeTouched(int edges, int pointerId) {
        return isPointerDown(pointerId) && (this.mInitialEdgesTouchedPoints[pointerId] & edges) != 0;
    }

    private void releaseViewForPointerUp() {
        this.mVelocityTracker.computeCurrentVelocity(VELOCITY_UNITS, this.mMaxVelocity);
        dispatchViewReleased(clampMag(this.mVelocityTracker.getXVelocity(this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity), clampMag(this.mVelocityTracker.getYVelocity(this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity));
    }

    private void dragTo(int left, int top, int deltaX, int deltaY) {
        int clampedX = left;
        int clampedY = top;
        int oldLeft = this.mCapturedView.getLeft();
        int oldTop = this.mCapturedView.getTop();
        if (deltaX != 0) {
            clampedX = this.mCallback.clampViewPositionHorizontal(this.mCapturedView, left, deltaX);
            this.mCapturedView.offsetLeftAndRight(clampedX - oldLeft);
        }
        if (deltaY != 0) {
            clampedY = this.mCallback.clampViewPositionVertical(this.mCapturedView, top, deltaY);
            this.mCapturedView.offsetTopAndBottom(clampedY - oldTop);
        }
        if (deltaX != 0 || deltaY != 0) {
            this.mCallback.onViewPositionChanged(this.mCapturedView, clampedX, clampedY, clampedX - oldLeft, clampedY - oldTop);
        }
    }

    public boolean isCapturedViewUnder(int positionX, int positionY) {
        return isViewUnder(this.mCapturedView, positionX, positionY);
    }

    public boolean isViewUnder(View view, int positionX, int positionY) {
        if (view != null && view.getLeft() <= positionX && view.getRight() > positionX && view.getTop() <= positionY && view.getBottom() > positionY) {
            return true;
        }
        return false;
    }

    public View findTopChildUnder(int positionX, int positionY) {
        for (int i = this.mParentView.getChildCount() - 1; i >= 0; i--) {
            View child = this.mParentView.getChildAt(this.mCallback.getOrderedChildIndex(i));
            if ((child.getLeft() <= positionX && child.getRight() > positionX && child.getTop() <= positionY) && child.getBottom() > positionY) {
                return child;
            }
        }
        return null;
    }

    private int getEdgesTouched(int eventX, int eventY) {
        int result = 0;
        if (this.mParentView.getLeft() + this.mEdgeSize > eventX) {
            result = 0 | 1;
        }
        if (this.mParentView.getTop() + this.mEdgeSize > eventY) {
            result |= 4;
        }
        if (this.mParentView.getRight() - this.mEdgeSize < eventX) {
            result |= 2;
        }
        if (this.mParentView.getBottom() - this.mEdgeSize < eventY) {
            return result | 8;
        }
        return result;
    }
}
