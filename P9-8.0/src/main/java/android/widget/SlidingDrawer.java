package android.widget;

import android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

@Deprecated
public class SlidingDrawer extends ViewGroup {
    private static final int ANIMATION_FRAME_DURATION = 16;
    private static final int COLLAPSED_FULL_CLOSED = -10002;
    private static final int EXPANDED_FULL_OPEN = -10001;
    private static final float MAXIMUM_ACCELERATION = 2000.0f;
    private static final float MAXIMUM_MAJOR_VELOCITY = 200.0f;
    private static final float MAXIMUM_MINOR_VELOCITY = 150.0f;
    private static final float MAXIMUM_TAP_VELOCITY = 100.0f;
    public static final int ORIENTATION_HORIZONTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;
    private static final int TAP_THRESHOLD = 6;
    private static final int VELOCITY_UNITS = 1000;
    private boolean mAllowSingleTap;
    private boolean mAnimateOnClick;
    private float mAnimatedAcceleration;
    private float mAnimatedVelocity;
    private boolean mAnimating;
    private long mAnimationLastTime;
    private float mAnimationPosition;
    private int mBottomOffset;
    private View mContent;
    private final int mContentId;
    private long mCurrentAnimationTime;
    private boolean mExpanded;
    private final Rect mFrame;
    private View mHandle;
    private int mHandleHeight;
    private final int mHandleId;
    private int mHandleWidth;
    private final Rect mInvalidate;
    private boolean mLocked;
    private final int mMaximumAcceleration;
    private final int mMaximumMajorVelocity;
    private final int mMaximumMinorVelocity;
    private final int mMaximumTapVelocity;
    private OnDrawerCloseListener mOnDrawerCloseListener;
    private OnDrawerOpenListener mOnDrawerOpenListener;
    private OnDrawerScrollListener mOnDrawerScrollListener;
    private final Runnable mSlidingRunnable;
    private final int mTapThreshold;
    private int mTopOffset;
    private int mTouchDelta;
    private boolean mTracking;
    private VelocityTracker mVelocityTracker;
    private final int mVelocityUnits;
    private boolean mVertical;

    private class DrawerToggler implements OnClickListener {
        /* synthetic */ DrawerToggler(SlidingDrawer this$0, DrawerToggler -this1) {
            this();
        }

        private DrawerToggler() {
        }

        public void onClick(View v) {
            if (!SlidingDrawer.this.mLocked) {
                if (SlidingDrawer.this.mAnimateOnClick) {
                    SlidingDrawer.this.animateToggle();
                } else {
                    SlidingDrawer.this.toggle();
                }
            }
        }
    }

    public interface OnDrawerCloseListener {
        void onDrawerClosed();
    }

    public interface OnDrawerOpenListener {
        void onDrawerOpened();
    }

    public interface OnDrawerScrollListener {
        void onScrollEnded();

        void onScrollStarted();
    }

    public SlidingDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SlidingDrawer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        boolean z;
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mFrame = new Rect();
        this.mInvalidate = new Rect();
        this.mSlidingRunnable = new Runnable() {
            public void run() {
                SlidingDrawer.this.doAnimation();
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingDrawer, defStyleAttr, defStyleRes);
        if (a.getInt(0, 1) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mVertical = z;
        this.mBottomOffset = (int) a.getDimension(1, 0.0f);
        this.mTopOffset = (int) a.getDimension(2, 0.0f);
        this.mAllowSingleTap = a.getBoolean(3, true);
        this.mAnimateOnClick = a.getBoolean(6, true);
        int handleId = a.getResourceId(4, 0);
        if (handleId == 0) {
            throw new IllegalArgumentException("The handle attribute is required and must refer to a valid child.");
        }
        int contentId = a.getResourceId(5, 0);
        if (contentId == 0) {
            throw new IllegalArgumentException("The content attribute is required and must refer to a valid child.");
        } else if (handleId == contentId) {
            throw new IllegalArgumentException("The content and handle attributes must refer to different children.");
        } else {
            this.mHandleId = handleId;
            this.mContentId = contentId;
            float density = getResources().getDisplayMetrics().density;
            this.mTapThreshold = (int) ((6.0f * density) + 0.5f);
            this.mMaximumTapVelocity = (int) ((MAXIMUM_TAP_VELOCITY * density) + 0.5f);
            this.mMaximumMinorVelocity = (int) ((MAXIMUM_MINOR_VELOCITY * density) + 0.5f);
            this.mMaximumMajorVelocity = (int) ((MAXIMUM_MAJOR_VELOCITY * density) + 0.5f);
            this.mMaximumAcceleration = (int) ((MAXIMUM_ACCELERATION * density) + 0.5f);
            this.mVelocityUnits = (int) ((1000.0f * density) + 0.5f);
            a.recycle();
            setAlwaysDrawnWithCacheEnabled(false);
        }
    }

    protected void onFinishInflate() {
        this.mHandle = findViewById(this.mHandleId);
        if (this.mHandle == null) {
            throw new IllegalArgumentException("The handle attribute is must refer to an existing child.");
        }
        this.mHandle.setOnClickListener(new DrawerToggler(this, null));
        this.mContent = findViewById(this.mContentId);
        if (this.mContent == null) {
            throw new IllegalArgumentException("The content attribute is must refer to an existing child.");
        }
        this.mContent.setVisibility(8);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == 0 || heightSpecMode == 0) {
            throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
        }
        View handle = this.mHandle;
        measureChild(handle, widthMeasureSpec, heightMeasureSpec);
        if (this.mVertical) {
            this.mContent.measure(MeasureSpec.makeMeasureSpec(widthSpecSize, 1073741824), MeasureSpec.makeMeasureSpec((heightSpecSize - handle.getMeasuredHeight()) - this.mTopOffset, 1073741824));
        } else {
            this.mContent.measure(MeasureSpec.makeMeasureSpec((widthSpecSize - handle.getMeasuredWidth()) - this.mTopOffset, 1073741824), MeasureSpec.makeMeasureSpec(heightSpecSize, 1073741824));
        }
        -wrap6(widthSpecSize, heightSpecSize);
    }

    protected void dispatchDraw(Canvas canvas) {
        int i = 0;
        long drawingTime = getDrawingTime();
        View handle = this.mHandle;
        boolean isVertical = this.mVertical;
        drawChild(canvas, handle, drawingTime);
        if (this.mTracking || this.mAnimating) {
            Bitmap cache = this.mContent.getDrawingCache();
            if (cache == null) {
                canvas.save();
                float left = (float) (isVertical ? 0 : handle.getLeft() - this.mTopOffset);
                if (isVertical) {
                    i = handle.getTop() - this.mTopOffset;
                }
                canvas.translate(left, (float) i);
                drawChild(canvas, this.mContent, drawingTime);
                canvas.restore();
            } else if (isVertical) {
                canvas.drawBitmap(cache, 0.0f, (float) handle.getBottom(), null);
            } else {
                canvas.drawBitmap(cache, (float) handle.getRight(), 0.0f, null);
            }
        } else if (this.mExpanded) {
            drawChild(canvas, this.mContent, drawingTime);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!this.mTracking) {
            int childLeft;
            int childTop;
            int width = r - l;
            int height = b - t;
            View handle = this.mHandle;
            int childWidth = handle.getMeasuredWidth();
            int childHeight = handle.getMeasuredHeight();
            View content = this.mContent;
            if (this.mVertical) {
                childLeft = (width - childWidth) / 2;
                childTop = this.mExpanded ? this.mTopOffset : (height - childHeight) + this.mBottomOffset;
                content.layout(0, this.mTopOffset + childHeight, content.getMeasuredWidth(), (this.mTopOffset + childHeight) + content.getMeasuredHeight());
            } else {
                childLeft = this.mExpanded ? this.mTopOffset : (width - childWidth) + this.mBottomOffset;
                childTop = (height - childHeight) / 2;
                content.layout(this.mTopOffset + childWidth, 0, (this.mTopOffset + childWidth) + content.getMeasuredWidth(), content.getMeasuredHeight());
            }
            handle.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            this.mHandleHeight = handle.getHeight();
            this.mHandleWidth = handle.getWidth();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.mLocked) {
            return false;
        }
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        Rect frame = this.mFrame;
        View handle = this.mHandle;
        handle.getHitRect(frame);
        if (!this.mTracking && (frame.contains((int) x, (int) y) ^ 1) != 0) {
            return false;
        }
        if (action == 0) {
            this.mTracking = true;
            handle.setPressed(true);
            prepareContent();
            if (this.mOnDrawerScrollListener != null) {
                this.mOnDrawerScrollListener.onScrollStarted();
            }
            if (this.mVertical) {
                int top = this.mHandle.getTop();
                this.mTouchDelta = ((int) y) - top;
                prepareTracking(top);
            } else {
                int left = this.mHandle.getLeft();
                this.mTouchDelta = ((int) x) - left;
                prepareTracking(left);
            }
            this.mVelocityTracker.addMovement(event);
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:78:0x0112  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mLocked) {
            return true;
        }
        boolean z;
        if (this.mTracking) {
            this.mVelocityTracker.addMovement(event);
            switch (event.getAction()) {
                case 1:
                case 3:
                    boolean negative;
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(this.mVelocityUnits);
                    float yVelocity = velocityTracker.getYVelocity();
                    float xVelocity = velocityTracker.getXVelocity();
                    boolean vertical = this.mVertical;
                    if (vertical) {
                        negative = yVelocity < 0.0f;
                        if (xVelocity < 0.0f) {
                            xVelocity = -xVelocity;
                        }
                        if (xVelocity > ((float) this.mMaximumMinorVelocity)) {
                            xVelocity = (float) this.mMaximumMinorVelocity;
                        }
                    } else {
                        negative = xVelocity < 0.0f;
                        if (yVelocity < 0.0f) {
                            yVelocity = -yVelocity;
                        }
                        if (yVelocity > ((float) this.mMaximumMinorVelocity)) {
                            yVelocity = (float) this.mMaximumMinorVelocity;
                        }
                    }
                    float velocity = (float) Math.hypot((double) xVelocity, (double) yVelocity);
                    if (negative) {
                        velocity = -velocity;
                    }
                    int top = this.mHandle.getTop();
                    int left = this.mHandle.getLeft();
                    if (Math.abs(velocity) >= ((float) this.mMaximumTapVelocity)) {
                        if (!vertical) {
                            top = left;
                        }
                        performFling(top, velocity, false, true);
                        break;
                    }
                    if (!vertical) {
                        if (!vertical) {
                        }
                        performFling(top, velocity, false, true);
                        break;
                    }
                    if (vertical) {
                        top = left;
                    }
                    performFling(top, velocity, false, true);
                    break;
                    if (!this.mAllowSingleTap) {
                        if (!vertical) {
                            top = left;
                        }
                        performFling(top, velocity, false, true);
                        break;
                    }
                    playSoundEffect(0);
                    if (!this.mExpanded) {
                        if (!vertical) {
                            top = left;
                        }
                        animateOpen(top, true);
                        break;
                    }
                    if (!vertical) {
                        top = left;
                    }
                    animateClose(top, true);
                    break;
                case 2:
                    moveHandle(((int) (this.mVertical ? event.getY() : event.getX())) - this.mTouchDelta);
                    break;
            }
        }
        if (this.mTracking || this.mAnimating) {
            z = true;
        } else {
            z = super.onTouchEvent(event);
        }
        return z;
    }

    private void animateClose(int position, boolean notifyScrollListener) {
        prepareTracking(position);
        performFling(position, (float) this.mMaximumAcceleration, true, notifyScrollListener);
    }

    private void animateOpen(int position, boolean notifyScrollListener) {
        prepareTracking(position);
        performFling(position, (float) (-this.mMaximumAcceleration), true, notifyScrollListener);
    }

    /* JADX WARNING: Missing block: B:30:0x0079, code:
            if (r10 > ((float) (-r8.mMaximumMajorVelocity))) goto L_0x007b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void performFling(int position, float velocity, boolean always, boolean notifyScrollListener) {
        this.mAnimationPosition = (float) position;
        this.mAnimatedVelocity = velocity;
        if (this.mExpanded) {
            if (!always && velocity <= ((float) this.mMaximumMajorVelocity)) {
                if (position <= (this.mVertical ? this.mHandleHeight : this.mHandleWidth) + this.mTopOffset || velocity <= ((float) (-this.mMaximumMajorVelocity))) {
                    this.mAnimatedAcceleration = (float) (-this.mMaximumAcceleration);
                    if (velocity > 0.0f) {
                        this.mAnimatedVelocity = 0.0f;
                    }
                }
            }
            this.mAnimatedAcceleration = (float) this.mMaximumAcceleration;
            if (velocity < 0.0f) {
                this.mAnimatedVelocity = 0.0f;
            }
        } else {
            if (!always) {
                if (velocity <= ((float) this.mMaximumMajorVelocity)) {
                    if (position > (this.mVertical ? getHeight() : getWidth()) / 2) {
                    }
                }
                this.mAnimatedAcceleration = (float) this.mMaximumAcceleration;
                if (velocity < 0.0f) {
                    this.mAnimatedVelocity = 0.0f;
                }
            }
            this.mAnimatedAcceleration = (float) (-this.mMaximumAcceleration);
            if (velocity > 0.0f) {
                this.mAnimatedVelocity = 0.0f;
            }
        }
        long now = SystemClock.uptimeMillis();
        this.mAnimationLastTime = now;
        this.mCurrentAnimationTime = now + 16;
        this.mAnimating = true;
        removeCallbacks(this.mSlidingRunnable);
        postDelayed(this.mSlidingRunnable, 16);
        stopTracking(notifyScrollListener);
    }

    private void prepareTracking(int position) {
        this.mTracking = true;
        this.mVelocityTracker = VelocityTracker.obtain();
        if (this.mExpanded ^ 1) {
            int height;
            int i;
            this.mAnimatedAcceleration = (float) this.mMaximumAcceleration;
            this.mAnimatedVelocity = (float) this.mMaximumMajorVelocity;
            int i2 = this.mBottomOffset;
            if (this.mVertical) {
                height = getHeight();
                i = this.mHandleHeight;
            } else {
                height = getWidth();
                i = this.mHandleWidth;
            }
            this.mAnimationPosition = (float) ((height - i) + i2);
            moveHandle((int) this.mAnimationPosition);
            this.mAnimating = true;
            removeCallbacks(this.mSlidingRunnable);
            long now = SystemClock.uptimeMillis();
            this.mAnimationLastTime = now;
            this.mCurrentAnimationTime = 16 + now;
            this.mAnimating = true;
            return;
        }
        if (this.mAnimating) {
            this.mAnimating = false;
            removeCallbacks(this.mSlidingRunnable);
        }
        moveHandle(position);
    }

    private void moveHandle(int position) {
        View handle = this.mHandle;
        Rect frame;
        Rect region;
        if (this.mVertical) {
            if (position == EXPANDED_FULL_OPEN) {
                handle.offsetTopAndBottom(this.mTopOffset - handle.getTop());
                invalidate();
            } else if (position == COLLAPSED_FULL_CLOSED) {
                handle.offsetTopAndBottom((((this.mBottomOffset + this.mBottom) - this.mTop) - this.mHandleHeight) - handle.getTop());
                invalidate();
            } else {
                int top = handle.getTop();
                int deltaY = position - top;
                if (position < this.mTopOffset) {
                    deltaY = this.mTopOffset - top;
                } else if (deltaY > (((this.mBottomOffset + this.mBottom) - this.mTop) - this.mHandleHeight) - top) {
                    deltaY = (((this.mBottomOffset + this.mBottom) - this.mTop) - this.mHandleHeight) - top;
                }
                handle.offsetTopAndBottom(deltaY);
                frame = this.mFrame;
                region = this.mInvalidate;
                handle.getHitRect(frame);
                region.set(frame);
                region.union(frame.left, frame.top - deltaY, frame.right, frame.bottom - deltaY);
                region.union(0, frame.bottom - deltaY, getWidth(), (frame.bottom - deltaY) + this.mContent.getHeight());
                invalidate(region);
            }
        } else if (position == EXPANDED_FULL_OPEN) {
            handle.offsetLeftAndRight(this.mTopOffset - handle.getLeft());
            invalidate();
        } else if (position == COLLAPSED_FULL_CLOSED) {
            handle.offsetLeftAndRight((((this.mBottomOffset + this.mRight) - this.mLeft) - this.mHandleWidth) - handle.getLeft());
            invalidate();
        } else {
            int left = handle.getLeft();
            int deltaX = position - left;
            if (position < this.mTopOffset) {
                deltaX = this.mTopOffset - left;
            } else if (deltaX > (((this.mBottomOffset + this.mRight) - this.mLeft) - this.mHandleWidth) - left) {
                deltaX = (((this.mBottomOffset + this.mRight) - this.mLeft) - this.mHandleWidth) - left;
            }
            handle.offsetLeftAndRight(deltaX);
            frame = this.mFrame;
            region = this.mInvalidate;
            handle.getHitRect(frame);
            region.set(frame);
            region.union(frame.left - deltaX, frame.top, frame.right - deltaX, frame.bottom);
            region.union(frame.right - deltaX, 0, (frame.right - deltaX) + this.mContent.getWidth(), getHeight());
            invalidate(region);
        }
    }

    private void prepareContent() {
        if (!this.mAnimating) {
            View content = this.mContent;
            if (content.isLayoutRequested()) {
                if (this.mVertical) {
                    int childHeight = this.mHandleHeight;
                    content.measure(MeasureSpec.makeMeasureSpec(this.mRight - this.mLeft, 1073741824), MeasureSpec.makeMeasureSpec(((this.mBottom - this.mTop) - childHeight) - this.mTopOffset, 1073741824));
                    content.layout(0, this.mTopOffset + childHeight, content.getMeasuredWidth(), (this.mTopOffset + childHeight) + content.getMeasuredHeight());
                } else {
                    int childWidth = this.mHandle.getWidth();
                    content.measure(MeasureSpec.makeMeasureSpec(((this.mRight - this.mLeft) - childWidth) - this.mTopOffset, 1073741824), MeasureSpec.makeMeasureSpec(this.mBottom - this.mTop, 1073741824));
                    content.layout(this.mTopOffset + childWidth, 0, (this.mTopOffset + childWidth) + content.getMeasuredWidth(), content.getMeasuredHeight());
                }
            }
            content.getViewTreeObserver().dispatchOnPreDraw();
            if (!content.isHardwareAccelerated()) {
                content.buildDrawingCache();
            }
            content.setVisibility(8);
        }
    }

    private void stopTracking(boolean notifyScrollListener) {
        this.mHandle.setPressed(false);
        this.mTracking = false;
        if (notifyScrollListener && this.mOnDrawerScrollListener != null) {
            this.mOnDrawerScrollListener.onScrollEnded();
        }
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void doAnimation() {
        if (this.mAnimating) {
            incrementAnimation();
            if (this.mAnimationPosition >= ((float) (((this.mVertical ? getHeight() : getWidth()) + this.mBottomOffset) - 1))) {
                this.mAnimating = false;
                closeDrawer();
            } else if (this.mAnimationPosition < ((float) this.mTopOffset)) {
                this.mAnimating = false;
                openDrawer();
            } else {
                moveHandle((int) this.mAnimationPosition);
                this.mCurrentAnimationTime += 16;
                postDelayed(this.mSlidingRunnable, 16);
            }
        }
    }

    private void incrementAnimation() {
        long now = SystemClock.uptimeMillis();
        float t = ((float) (now - this.mAnimationLastTime)) / 1000.0f;
        float position = this.mAnimationPosition;
        float v = this.mAnimatedVelocity;
        float a = this.mAnimatedAcceleration;
        this.mAnimationPosition = ((v * t) + position) + (((0.5f * a) * t) * t);
        this.mAnimatedVelocity = (a * t) + v;
        this.mAnimationLastTime = now;
    }

    public void toggle() {
        if (this.mExpanded) {
            closeDrawer();
        } else {
            openDrawer();
        }
        invalidate();
        requestLayout();
    }

    public void animateToggle() {
        if (this.mExpanded) {
            animateClose();
        } else {
            animateOpen();
        }
    }

    public void open() {
        openDrawer();
        invalidate();
        requestLayout();
        sendAccessibilityEvent(32);
    }

    public void close() {
        closeDrawer();
        invalidate();
        requestLayout();
    }

    public void animateClose() {
        prepareContent();
        OnDrawerScrollListener scrollListener = this.mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateClose(this.mVertical ? this.mHandle.getTop() : this.mHandle.getLeft(), false);
        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    public void animateOpen() {
        prepareContent();
        OnDrawerScrollListener scrollListener = this.mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateOpen(this.mVertical ? this.mHandle.getTop() : this.mHandle.getLeft(), false);
        sendAccessibilityEvent(32);
        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    public CharSequence getAccessibilityClassName() {
        return SlidingDrawer.class.getName();
    }

    private void closeDrawer() {
        moveHandle(COLLAPSED_FULL_CLOSED);
        this.mContent.setVisibility(8);
        this.mContent.destroyDrawingCache();
        if (this.mExpanded) {
            this.mExpanded = false;
            if (this.mOnDrawerCloseListener != null) {
                this.mOnDrawerCloseListener.onDrawerClosed();
            }
        }
    }

    private void openDrawer() {
        moveHandle(EXPANDED_FULL_OPEN);
        this.mContent.setVisibility(0);
        if (!this.mExpanded) {
            this.mExpanded = true;
            if (this.mOnDrawerOpenListener != null) {
                this.mOnDrawerOpenListener.onDrawerOpened();
            }
        }
    }

    public void setOnDrawerOpenListener(OnDrawerOpenListener onDrawerOpenListener) {
        this.mOnDrawerOpenListener = onDrawerOpenListener;
    }

    public void setOnDrawerCloseListener(OnDrawerCloseListener onDrawerCloseListener) {
        this.mOnDrawerCloseListener = onDrawerCloseListener;
    }

    public void setOnDrawerScrollListener(OnDrawerScrollListener onDrawerScrollListener) {
        this.mOnDrawerScrollListener = onDrawerScrollListener;
    }

    public View getHandle() {
        return this.mHandle;
    }

    public View getContent() {
        return this.mContent;
    }

    public void unlock() {
        this.mLocked = false;
    }

    public void lock() {
        this.mLocked = true;
    }

    public boolean isOpened() {
        return this.mExpanded;
    }

    public boolean isMoving() {
        return !this.mTracking ? this.mAnimating : true;
    }
}
