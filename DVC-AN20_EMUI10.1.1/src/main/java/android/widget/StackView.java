package android.widget;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.TableMaskFilter;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.RemotableViewMethod;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterViewAnimator;
import android.widget.RemoteViews;
import com.android.internal.R;
import java.lang.ref.WeakReference;

@RemoteViews.RemoteView
public class StackView extends AdapterViewAnimator {
    private static final int DEFAULT_ANIMATION_DURATION = 400;
    private static final int FRAME_PADDING = 4;
    private static final int GESTURE_NONE = 0;
    private static final int GESTURE_SLIDE_DOWN = 2;
    private static final int GESTURE_SLIDE_UP = 1;
    private static final int INVALID_POINTER = -1;
    private static final int ITEMS_SLIDE_DOWN = 1;
    private static final int ITEMS_SLIDE_UP = 0;
    private static final int MINIMUM_ANIMATION_DURATION = 50;
    private static final int MIN_TIME_BETWEEN_INTERACTION_AND_AUTOADVANCE = 5000;
    private static final long MIN_TIME_BETWEEN_SCROLLS = 100;
    private static final int NUM_ACTIVE_VIEWS = 5;
    private static final float PERSPECTIVE_SCALE_FACTOR = 0.0f;
    private static final float PERSPECTIVE_SHIFT_FACTOR_X = 0.1f;
    private static final float PERSPECTIVE_SHIFT_FACTOR_Y = 0.1f;
    private static final float SLIDE_UP_RATIO = 0.7f;
    private static final int STACK_RELAYOUT_DURATION = 100;
    private static final float SWIPE_THRESHOLD_RATIO = 0.2f;
    private static HolographicHelper sHolographicHelper;
    private final String TAG;
    private int mActivePointerId;
    private int mClickColor;
    private ImageView mClickFeedback;
    private boolean mClickFeedbackIsValid;
    private boolean mFirstLayoutHappened;
    private int mFramePadding;
    private ImageView mHighlight;
    private float mInitialX;
    private float mInitialY;
    private long mLastInteractionTime;
    private long mLastScrollTime;
    private int mMaximumVelocity;
    private float mNewPerspectiveShiftX;
    private float mNewPerspectiveShiftY;
    private float mPerspectiveShiftX;
    private float mPerspectiveShiftY;
    private int mResOutColor;
    private int mSlideAmount;
    private int mStackMode;
    private StackSlider mStackSlider;
    private int mSwipeGestureType;
    private int mSwipeThreshold;
    private final Rect mTouchRect;
    private int mTouchSlop;
    private boolean mTransitionIsSetup;
    private VelocityTracker mVelocityTracker;
    private int mYVelocity;
    private final Rect stackInvalidateRect;

    public StackView(Context context) {
        this(context, null);
    }

    public StackView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843838);
    }

    public StackView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public StackView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.TAG = "StackView";
        this.mTouchRect = new Rect();
        this.mYVelocity = 0;
        this.mSwipeGestureType = 0;
        this.mTransitionIsSetup = false;
        this.mClickFeedbackIsValid = false;
        this.mFirstLayoutHappened = false;
        this.mLastInteractionTime = 0;
        this.stackInvalidateRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StackView, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.StackView, attrs, a, defStyleAttr, defStyleRes);
        this.mResOutColor = a.getColor(1, 0);
        this.mClickColor = a.getColor(0, 0);
        a.recycle();
        initStackView();
    }

    private void initStackView() {
        configureViewAnimator(5, 1);
        setStaticTransformationsEnabled(true);
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mActivePointerId = -1;
        this.mHighlight = new ImageView(getContext());
        ImageView imageView = this.mHighlight;
        imageView.setLayoutParams(new LayoutParams(imageView));
        ImageView imageView2 = this.mHighlight;
        addViewInLayout(imageView2, -1, new LayoutParams(imageView2));
        this.mClickFeedback = new ImageView(getContext());
        ImageView imageView3 = this.mClickFeedback;
        imageView3.setLayoutParams(new LayoutParams(imageView3));
        ImageView imageView4 = this.mClickFeedback;
        addViewInLayout(imageView4, -1, new LayoutParams(imageView4));
        this.mClickFeedback.setVisibility(4);
        this.mStackSlider = new StackSlider();
        if (sHolographicHelper == null) {
            sHolographicHelper = new HolographicHelper(this.mContext);
        }
        setClipChildren(false);
        setClipToPadding(false);
        this.mStackMode = 1;
        this.mWhichChild = -1;
        this.mFramePadding = (int) Math.ceil((double) (4.0f * this.mContext.getResources().getDisplayMetrics().density));
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AdapterViewAnimator
    public void transformViewForTransition(int fromIndex, int toIndex, final View view, boolean animate) {
        if (!animate) {
            ((StackFrame) view).cancelSliderAnimator();
            view.setRotationX(0.0f);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            lp.setVerticalOffset(0);
            lp.setHorizontalOffset(0);
        }
        if (fromIndex == -1 && toIndex == getNumActiveViews() - 1) {
            transformViewAtIndex(toIndex, view, false);
            view.setVisibility(0);
            view.setAlpha(1.0f);
        } else if (fromIndex == 0 && toIndex == 1) {
            ((StackFrame) view).cancelSliderAnimator();
            view.setVisibility(0);
            int duration = Math.round(this.mStackSlider.getDurationForNeutralPosition((float) this.mYVelocity));
            StackSlider animationSlider = new StackSlider(this.mStackSlider);
            animationSlider.setView(view);
            if (animate) {
                ObjectAnimator slideIn = ObjectAnimator.ofPropertyValuesHolder(animationSlider, PropertyValuesHolder.ofFloat("XProgress", 0.0f), PropertyValuesHolder.ofFloat("YProgress", 0.0f));
                slideIn.setDuration((long) duration);
                slideIn.setInterpolator(new LinearInterpolator());
                ((StackFrame) view).setSliderAnimator(slideIn);
                slideIn.start();
            } else {
                animationSlider.setYProgress(0.0f);
                animationSlider.setXProgress(0.0f);
            }
        } else if (fromIndex == 1 && toIndex == 0) {
            ((StackFrame) view).cancelSliderAnimator();
            int duration2 = Math.round(this.mStackSlider.getDurationForOffscreenPosition((float) this.mYVelocity));
            StackSlider animationSlider2 = new StackSlider(this.mStackSlider);
            animationSlider2.setView(view);
            if (animate) {
                ObjectAnimator slideOut = ObjectAnimator.ofPropertyValuesHolder(animationSlider2, PropertyValuesHolder.ofFloat("XProgress", 0.0f), PropertyValuesHolder.ofFloat("YProgress", 1.0f));
                slideOut.setDuration((long) duration2);
                slideOut.setInterpolator(new LinearInterpolator());
                ((StackFrame) view).setSliderAnimator(slideOut);
                slideOut.start();
            } else {
                animationSlider2.setYProgress(1.0f);
                animationSlider2.setXProgress(0.0f);
            }
        } else if (toIndex == 0) {
            view.setAlpha(0.0f);
            view.setVisibility(4);
        } else if ((fromIndex == 0 || fromIndex == 1) && toIndex > 1) {
            view.setVisibility(0);
            view.setAlpha(1.0f);
            view.setRotationX(0.0f);
            LayoutParams lp2 = (LayoutParams) view.getLayoutParams();
            lp2.setVerticalOffset(0);
            lp2.setHorizontalOffset(0);
        } else if (fromIndex == -1) {
            view.setAlpha(1.0f);
            view.setVisibility(0);
        } else if (toIndex == -1) {
            if (animate) {
                postDelayed(new Runnable() {
                    /* class android.widget.StackView.AnonymousClass1 */

                    public void run() {
                        view.setAlpha(0.0f);
                    }
                }, MIN_TIME_BETWEEN_SCROLLS);
            } else {
                view.setAlpha(0.0f);
            }
        }
        if (toIndex != -1) {
            transformViewAtIndex(toIndex, view, animate);
        }
    }

    private void transformViewAtIndex(int index, View view, boolean animate) {
        int index2;
        float maxPerspectiveShiftY = this.mPerspectiveShiftY;
        float maxPerspectiveShiftX = this.mPerspectiveShiftX;
        if (this.mStackMode == 1) {
            index2 = (this.mMaxNumActiveViews - index) - 1;
            if (index2 == this.mMaxNumActiveViews - 1) {
                index2--;
            }
        } else {
            index2 = index - 1;
            if (index2 < 0) {
                index2++;
            }
        }
        float r = (((float) index2) * 1.0f) / ((float) (this.mMaxNumActiveViews - 2));
        float scale = 1.0f - ((1.0f - r) * 0.0f);
        float transY = (r * maxPerspectiveShiftY) + ((scale - 1.0f) * ((((float) getMeasuredHeight()) * 0.9f) / 2.0f));
        float transX = ((1.0f - r) * maxPerspectiveShiftX) + ((1.0f - scale) * ((((float) getMeasuredWidth()) * 0.9f) / 2.0f));
        if (view instanceof StackFrame) {
            ((StackFrame) view).cancelTransformAnimator();
        }
        if (animate) {
            PropertyValuesHolder translationX = PropertyValuesHolder.ofFloat("translationX", transX);
            ObjectAnimator oa = ObjectAnimator.ofPropertyValuesHolder(view, PropertyValuesHolder.ofFloat("scaleX", scale), PropertyValuesHolder.ofFloat("scaleY", scale), PropertyValuesHolder.ofFloat("translationY", transY), translationX);
            oa.setDuration(MIN_TIME_BETWEEN_SCROLLS);
            if (view instanceof StackFrame) {
                ((StackFrame) view).setTransformAnimator(oa);
            }
            oa.start();
            return;
        }
        view.setTranslationX(transX);
        view.setTranslationY(transY);
        view.setScaleX(scale);
        view.setScaleY(scale);
    }

    private void setupStackSlider(View v, int mode) {
        this.mStackSlider.setMode(mode);
        if (v != null) {
            this.mHighlight.setImageBitmap(sHolographicHelper.createResOutline(v, this.mResOutColor));
            this.mHighlight.setRotation(v.getRotation());
            this.mHighlight.setTranslationY(v.getTranslationY());
            this.mHighlight.setTranslationX(v.getTranslationX());
            this.mHighlight.bringToFront();
            v.bringToFront();
            this.mStackSlider.setView(v);
            v.setVisibility(0);
        }
    }

    @Override // android.widget.AdapterViewAnimator
    @RemotableViewMethod
    public void showNext() {
        View v;
        if (this.mSwipeGestureType == 0) {
            if (!this.mTransitionIsSetup && (v = getViewAtRelativeIndex(1)) != null) {
                setupStackSlider(v, 0);
                this.mStackSlider.setYProgress(0.0f);
                this.mStackSlider.setXProgress(0.0f);
            }
            super.showNext();
        }
    }

    @Override // android.widget.AdapterViewAnimator
    @RemotableViewMethod
    public void showPrevious() {
        View v;
        if (this.mSwipeGestureType == 0) {
            if (!this.mTransitionIsSetup && (v = getViewAtRelativeIndex(0)) != null) {
                setupStackSlider(v, 0);
                this.mStackSlider.setYProgress(1.0f);
                this.mStackSlider.setXProgress(0.0f);
            }
            super.showPrevious();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AdapterViewAnimator
    public void showOnly(int childIndex, boolean animate) {
        View v;
        super.showOnly(childIndex, animate);
        for (int i = this.mCurrentWindowEnd; i >= this.mCurrentWindowStart; i--) {
            int index = modulo(i, getWindowSize());
            if (!(((AdapterViewAnimator.ViewAndMetaData) this.mViewsMap.get(Integer.valueOf(index))) == null || (v = ((AdapterViewAnimator.ViewAndMetaData) this.mViewsMap.get(Integer.valueOf(index))).view) == null)) {
                v.bringToFront();
            }
        }
        ImageView imageView = this.mHighlight;
        if (imageView != null) {
            imageView.bringToFront();
        }
        this.mTransitionIsSetup = false;
        this.mClickFeedbackIsValid = false;
    }

    /* access modifiers changed from: package-private */
    public void updateClickFeedback() {
        if (!this.mClickFeedbackIsValid) {
            View v = getViewAtRelativeIndex(1);
            if (v != null) {
                this.mClickFeedback.setImageBitmap(sHolographicHelper.createClickOutline(v, this.mClickColor));
                this.mClickFeedback.setTranslationX(v.getTranslationX());
                this.mClickFeedback.setTranslationY(v.getTranslationY());
            }
            this.mClickFeedbackIsValid = true;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AdapterViewAnimator
    public void showTapFeedback(View v) {
        updateClickFeedback();
        this.mClickFeedback.setVisibility(0);
        this.mClickFeedback.bringToFront();
        invalidate();
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AdapterViewAnimator
    public void hideTapFeedback(View v) {
        this.mClickFeedback.setVisibility(4);
        invalidate();
    }

    private void updateChildTransforms() {
        for (int i = 0; i < getNumActiveViews(); i++) {
            View v = getViewAtRelativeIndex(i);
            if (v != null) {
                transformViewAtIndex(i, v, false);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class StackFrame extends FrameLayout {
        WeakReference<ObjectAnimator> sliderAnimator;
        WeakReference<ObjectAnimator> transformAnimator;

        public StackFrame(Context context) {
            super(context);
        }

        /* access modifiers changed from: package-private */
        public void setTransformAnimator(ObjectAnimator oa) {
            this.transformAnimator = new WeakReference<>(oa);
        }

        /* access modifiers changed from: package-private */
        public void setSliderAnimator(ObjectAnimator oa) {
            this.sliderAnimator = new WeakReference<>(oa);
        }

        /* access modifiers changed from: package-private */
        public boolean cancelTransformAnimator() {
            ObjectAnimator oa;
            WeakReference<ObjectAnimator> weakReference = this.transformAnimator;
            if (weakReference == null || (oa = weakReference.get()) == null) {
                return false;
            }
            oa.cancel();
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean cancelSliderAnimator() {
            ObjectAnimator oa;
            WeakReference<ObjectAnimator> weakReference = this.sliderAnimator;
            if (weakReference == null || (oa = weakReference.get()) == null) {
                return false;
            }
            oa.cancel();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AdapterViewAnimator
    public FrameLayout getFrameForChild() {
        StackFrame fl = new StackFrame(this.mContext);
        int i = this.mFramePadding;
        fl.setPadding(i, i, i, i);
        return fl;
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AdapterViewAnimator
    public void applyTransformForChildAtIndex(View child, int relativeIndex) {
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void dispatchDraw(Canvas canvas) {
        boolean expandClipRegion = false;
        canvas.getClipBounds(this.stackInvalidateRect);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if ((lp.horizontalOffset == 0 && lp.verticalOffset == 0) || child.getAlpha() == 0.0f || child.getVisibility() != 0) {
                lp.resetInvalidateRect();
            }
            Rect childInvalidateRect = lp.getInvalidateRect();
            if (!childInvalidateRect.isEmpty()) {
                expandClipRegion = true;
                this.stackInvalidateRect.union(childInvalidateRect);
            }
        }
        if (expandClipRegion) {
            canvas.save();
            canvas.clipRectUnion(this.stackInvalidateRect);
            super.dispatchDraw(canvas);
            canvas.restore();
            return;
        }
        super.dispatchDraw(canvas);
    }

    private void onLayout() {
        if (!this.mFirstLayoutHappened) {
            this.mFirstLayoutHappened = true;
            updateChildTransforms();
        }
        int newSlideAmount = Math.round(((float) getMeasuredHeight()) * SLIDE_UP_RATIO);
        if (this.mSlideAmount != newSlideAmount) {
            this.mSlideAmount = newSlideAmount;
            this.mSwipeThreshold = Math.round(((float) newSlideAmount) * 0.2f);
        }
        if (Float.compare(this.mPerspectiveShiftY, this.mNewPerspectiveShiftY) != 0 || Float.compare(this.mPerspectiveShiftX, this.mNewPerspectiveShiftX) != 0) {
            this.mPerspectiveShiftY = this.mNewPerspectiveShiftY;
            this.mPerspectiveShiftX = this.mNewPerspectiveShiftX;
            updateChildTransforms();
        }
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & 2) != 0 && event.getAction() == 8) {
            float vscroll = event.getAxisValue(9);
            if (vscroll < 0.0f) {
                pacedScroll(false);
                return true;
            } else if (vscroll > 0.0f) {
                pacedScroll(true);
                return true;
            }
        }
        return super.onGenericMotionEvent(event);
    }

    private void pacedScroll(boolean up) {
        if (System.currentTimeMillis() - this.mLastScrollTime > MIN_TIME_BETWEEN_SCROLLS) {
            if (up) {
                showPrevious();
            } else {
                showNext();
            }
            this.mLastScrollTime = System.currentTimeMillis();
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & 255;
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                    if (pointerIndex == -1) {
                        Log.d("StackView", "Error: No data for our primary pointer.");
                        return false;
                    }
                    beginGestureIfNeeded(ev.getY(pointerIndex) - this.mInitialY);
                } else if (action != 3) {
                    if (action == 6) {
                        onSecondaryPointerUp(ev);
                    }
                }
            }
            this.mActivePointerId = -1;
            this.mSwipeGestureType = 0;
        } else if (this.mActivePointerId == -1) {
            this.mInitialX = ev.getX();
            this.mInitialY = ev.getY();
            this.mActivePointerId = ev.getPointerId(0);
        }
        return this.mSwipeGestureType != 0;
    }

    private void beginGestureIfNeeded(float deltaY) {
        int activeIndex;
        int stackMode;
        if (((int) Math.abs(deltaY)) > this.mTouchSlop && this.mSwipeGestureType == 0) {
            boolean z = true;
            int swipeGestureType = deltaY < 0.0f ? 1 : 2;
            cancelLongPress();
            requestDisallowInterceptTouchEvent(true);
            if (this.mAdapter != null) {
                int adapterCount = getCount();
                if (this.mStackMode == 0) {
                    activeIndex = swipeGestureType == 2 ? 0 : 1;
                } else {
                    activeIndex = swipeGestureType == 2 ? 1 : 0;
                }
                boolean endOfStack = this.mLoopViews && adapterCount == 1 && ((this.mStackMode == 0 && swipeGestureType == 1) || (this.mStackMode == 1 && swipeGestureType == 2));
                boolean beginningOfStack = this.mLoopViews && adapterCount == 1 && ((this.mStackMode == 1 && swipeGestureType == 1) || (this.mStackMode == 0 && swipeGestureType == 2));
                if (this.mLoopViews && !beginningOfStack && !endOfStack) {
                    stackMode = 0;
                } else if (this.mCurrentWindowStartUnbounded + activeIndex == -1 || beginningOfStack) {
                    activeIndex++;
                    stackMode = 1;
                } else if (this.mCurrentWindowStartUnbounded + activeIndex == adapterCount - 1 || endOfStack) {
                    stackMode = 2;
                } else {
                    stackMode = 0;
                }
                if (stackMode != 0) {
                    z = false;
                }
                this.mTransitionIsSetup = z;
                View v = getViewAtRelativeIndex(activeIndex);
                if (v != null) {
                    setupStackSlider(v, stackMode);
                    this.mSwipeGestureType = swipeGestureType;
                    cancelHandleClick();
                }
            }
        }
    }

    @Override // android.widget.AdapterViewAnimator, android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        int action = ev.getAction();
        int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
        if (pointerIndex == -1) {
            Log.d("StackView", "Error: No data for our primary pointer.");
            return false;
        }
        float newY = ev.getY(pointerIndex);
        float newX = ev.getX(pointerIndex);
        float deltaY = newY - this.mInitialY;
        float deltaX = newX - this.mInitialX;
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        int i = action & 255;
        if (i == 1) {
            handlePointerUp(ev);
        } else if (i == 2) {
            beginGestureIfNeeded(deltaY);
            int i2 = this.mSlideAmount;
            float rx = deltaX / (((float) i2) * 1.0f);
            int i3 = this.mSwipeGestureType;
            if (i3 == 2) {
                float r = ((deltaY - (((float) this.mTouchSlop) * 1.0f)) / ((float) i2)) * 1.0f;
                if (this.mStackMode == 1) {
                    r = 1.0f - r;
                }
                this.mStackSlider.setYProgress(1.0f - r);
                this.mStackSlider.setXProgress(rx);
                return true;
            } else if (i3 == 1) {
                float r2 = ((-((((float) this.mTouchSlop) * 1.0f) + deltaY)) / ((float) i2)) * 1.0f;
                if (this.mStackMode == 1) {
                    r2 = 1.0f - r2;
                }
                this.mStackSlider.setYProgress(r2);
                this.mStackSlider.setXProgress(rx);
                return true;
            }
        } else if (i == 3) {
            this.mActivePointerId = -1;
            this.mSwipeGestureType = 0;
        } else if (i == 6) {
            onSecondaryPointerUp(ev);
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int activePointerIndex = ev.getActionIndex();
        if (ev.getPointerId(activePointerIndex) == this.mActivePointerId) {
            View v = getViewAtRelativeIndex(this.mSwipeGestureType == 2 ? 0 : 1);
            if (v != null) {
                for (int index = 0; index < ev.getPointerCount(); index++) {
                    if (index != activePointerIndex) {
                        float x = ev.getX(index);
                        float y = ev.getY(index);
                        this.mTouchRect.set(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                        if (this.mTouchRect.contains(Math.round(x), Math.round(y))) {
                            float oldX = ev.getX(activePointerIndex);
                            this.mInitialY += y - ev.getY(activePointerIndex);
                            this.mInitialX += x - oldX;
                            this.mActivePointerId = ev.getPointerId(index);
                            VelocityTracker velocityTracker = this.mVelocityTracker;
                            if (velocityTracker != null) {
                                velocityTracker.clear();
                                return;
                            }
                            return;
                        }
                    }
                }
                handlePointerUp(ev);
            }
        }
    }

    private void handlePointerUp(MotionEvent ev) {
        int duration;
        int duration2;
        int deltaY = (int) (ev.getY(ev.findPointerIndex(this.mActivePointerId)) - this.mInitialY);
        this.mLastInteractionTime = System.currentTimeMillis();
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
            this.mYVelocity = (int) this.mVelocityTracker.getYVelocity(this.mActivePointerId);
        }
        VelocityTracker velocityTracker2 = this.mVelocityTracker;
        if (velocityTracker2 != null) {
            velocityTracker2.recycle();
            this.mVelocityTracker = null;
        }
        if (deltaY > this.mSwipeThreshold && this.mSwipeGestureType == 2 && this.mStackSlider.mMode == 0) {
            this.mSwipeGestureType = 0;
            if (this.mStackMode == 0) {
                showPrevious();
            } else {
                showNext();
            }
            this.mHighlight.bringToFront();
        } else if (deltaY < (-this.mSwipeThreshold) && this.mSwipeGestureType == 1 && this.mStackSlider.mMode == 0) {
            this.mSwipeGestureType = 0;
            if (this.mStackMode == 0) {
                showNext();
            } else {
                showPrevious();
            }
            this.mHighlight.bringToFront();
        } else {
            int i = this.mSwipeGestureType;
            float finalYProgress = 1.0f;
            if (i == 1) {
                if (this.mStackMode != 1) {
                    finalYProgress = 0.0f;
                }
                if (this.mStackMode == 0 || this.mStackSlider.mMode != 0) {
                    duration2 = Math.round(this.mStackSlider.getDurationForNeutralPosition());
                } else {
                    duration2 = Math.round(this.mStackSlider.getDurationForOffscreenPosition());
                }
                ObjectAnimator pa = ObjectAnimator.ofPropertyValuesHolder(new StackSlider(this.mStackSlider), PropertyValuesHolder.ofFloat("XProgress", 0.0f), PropertyValuesHolder.ofFloat("YProgress", finalYProgress));
                pa.setDuration((long) duration2);
                pa.setInterpolator(new LinearInterpolator());
                pa.start();
            } else if (i == 2) {
                if (this.mStackMode == 1) {
                    finalYProgress = 0.0f;
                }
                if (this.mStackMode == 1 || this.mStackSlider.mMode != 0) {
                    duration = Math.round(this.mStackSlider.getDurationForNeutralPosition());
                } else {
                    duration = Math.round(this.mStackSlider.getDurationForOffscreenPosition());
                }
                ObjectAnimator pa2 = ObjectAnimator.ofPropertyValuesHolder(new StackSlider(this.mStackSlider), PropertyValuesHolder.ofFloat("XProgress", 0.0f), PropertyValuesHolder.ofFloat("YProgress", finalYProgress));
                pa2.setDuration((long) duration);
                pa2.start();
            }
        }
        this.mActivePointerId = -1;
        this.mSwipeGestureType = 0;
    }

    /* access modifiers changed from: private */
    public class StackSlider {
        static final int BEGINNING_OF_STACK_MODE = 1;
        static final int END_OF_STACK_MODE = 2;
        static final int NORMAL_MODE = 0;
        int mMode = 0;
        View mView;
        float mXProgress;
        float mYProgress;

        public StackSlider() {
        }

        public StackSlider(StackSlider copy) {
            this.mView = copy.mView;
            this.mYProgress = copy.mYProgress;
            this.mXProgress = copy.mXProgress;
            this.mMode = copy.mMode;
        }

        private float cubic(float r) {
            return ((float) (Math.pow((double) ((r * 2.0f) - 1.0f), 3.0d) + 1.0d)) / 2.0f;
        }

        private float highlightAlphaInterpolator(float r) {
            if (r < 0.4f) {
                return cubic(r / 0.4f) * 0.85f;
            }
            return cubic(1.0f - ((r - 0.4f) / (1.0f - 0.4f))) * 0.85f;
        }

        private float viewAlphaInterpolator(float r) {
            if (r > 0.3f) {
                return (r - 0.3f) / (1.0f - 0.3f);
            }
            return 0.0f;
        }

        private float rotationInterpolator(float r) {
            if (r < 0.2f) {
                return 0.0f;
            }
            return (r - 0.2f) / (1.0f - 0.2f);
        }

        /* access modifiers changed from: package-private */
        public void setView(View v) {
            this.mView = v;
        }

        public void setYProgress(float r) {
            float r2 = Math.max(0.0f, Math.min(1.0f, r));
            this.mYProgress = r2;
            View view = this.mView;
            if (view != null) {
                LayoutParams viewLp = (LayoutParams) view.getLayoutParams();
                LayoutParams highlightLp = (LayoutParams) StackView.this.mHighlight.getLayoutParams();
                int stackDirection = StackView.this.mStackMode == 0 ? 1 : -1;
                if (Float.compare(0.0f, this.mYProgress) == 0 || Float.compare(1.0f, this.mYProgress) == 0) {
                    if (this.mView.getLayerType() != 0) {
                        this.mView.setLayerType(0, null);
                    }
                } else if (this.mView.getLayerType() == 0) {
                    this.mView.setLayerType(2, null);
                }
                int i = this.mMode;
                if (i == 0) {
                    viewLp.setVerticalOffset(Math.round((-r2) * ((float) stackDirection) * ((float) StackView.this.mSlideAmount)));
                    highlightLp.setVerticalOffset(Math.round((-r2) * ((float) stackDirection) * ((float) StackView.this.mSlideAmount)));
                    StackView.this.mHighlight.setAlpha(highlightAlphaInterpolator(r2));
                    float alpha = viewAlphaInterpolator(1.0f - r2);
                    if (this.mView.getAlpha() == 0.0f && alpha != 0.0f && this.mView.getVisibility() != 0) {
                        this.mView.setVisibility(0);
                    } else if (alpha == 0.0f && this.mView.getAlpha() != 0.0f && this.mView.getVisibility() == 0) {
                        this.mView.setVisibility(4);
                    }
                    this.mView.setAlpha(alpha);
                    this.mView.setRotationX(((float) stackDirection) * 90.0f * rotationInterpolator(r2));
                    StackView.this.mHighlight.setRotationX(((float) stackDirection) * 90.0f * rotationInterpolator(r2));
                } else if (i == 1) {
                    float r3 = (1.0f - r2) * 0.2f;
                    viewLp.setVerticalOffset(Math.round(((float) stackDirection) * r3 * ((float) StackView.this.mSlideAmount)));
                    highlightLp.setVerticalOffset(Math.round(((float) stackDirection) * r3 * ((float) StackView.this.mSlideAmount)));
                    StackView.this.mHighlight.setAlpha(highlightAlphaInterpolator(r3));
                } else if (i == 2) {
                    float r4 = r2 * 0.2f;
                    viewLp.setVerticalOffset(Math.round(((float) (-stackDirection)) * r4 * ((float) StackView.this.mSlideAmount)));
                    highlightLp.setVerticalOffset(Math.round(((float) (-stackDirection)) * r4 * ((float) StackView.this.mSlideAmount)));
                    StackView.this.mHighlight.setAlpha(highlightAlphaInterpolator(r4));
                }
            }
        }

        public void setXProgress(float r) {
            float r2 = Math.max(-2.0f, Math.min(2.0f, r));
            this.mXProgress = r2;
            View view = this.mView;
            if (view != null) {
                float r3 = r2 * 0.2f;
                ((LayoutParams) view.getLayoutParams()).setHorizontalOffset(Math.round(((float) StackView.this.mSlideAmount) * r3));
                ((LayoutParams) StackView.this.mHighlight.getLayoutParams()).setHorizontalOffset(Math.round(((float) StackView.this.mSlideAmount) * r3));
            }
        }

        /* access modifiers changed from: package-private */
        public void setMode(int mode) {
            this.mMode = mode;
        }

        /* access modifiers changed from: package-private */
        public float getDurationForNeutralPosition() {
            return getDuration(false, 0.0f);
        }

        /* access modifiers changed from: package-private */
        public float getDurationForOffscreenPosition() {
            return getDuration(true, 0.0f);
        }

        /* access modifiers changed from: package-private */
        public float getDurationForNeutralPosition(float velocity) {
            return getDuration(false, velocity);
        }

        /* access modifiers changed from: package-private */
        public float getDurationForOffscreenPosition(float velocity) {
            return getDuration(true, velocity);
        }

        private float getDuration(boolean invert, float velocity) {
            float duration;
            View view = this.mView;
            if (view == null) {
                return 0.0f;
            }
            LayoutParams viewLp = (LayoutParams) view.getLayoutParams();
            float d = (float) Math.hypot((double) viewLp.horizontalOffset, (double) viewLp.verticalOffset);
            float maxd = (float) Math.hypot((double) StackView.this.mSlideAmount, (double) (((float) StackView.this.mSlideAmount) * 0.4f));
            if (d > maxd) {
                d = maxd;
            }
            if (velocity == 0.0f) {
                return (invert ? 1.0f - (d / maxd) : d / maxd) * 400.0f;
            }
            if (invert) {
                duration = d / Math.abs(velocity);
            } else {
                duration = (maxd - d) / Math.abs(velocity);
            }
            if (duration < 50.0f || duration > 400.0f) {
                return getDuration(invert, 0.0f);
            }
            return duration;
        }

        public float getYProgress() {
            return this.mYProgress;
        }

        public float getXProgress() {
            return this.mXProgress;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AdapterViewAnimator
    public LayoutParams createOrReuseLayoutParams(View v) {
        ViewGroup.LayoutParams currentLp = v.getLayoutParams();
        if (!(currentLp instanceof LayoutParams)) {
            return new LayoutParams(v);
        }
        LayoutParams lp = (LayoutParams) currentLp;
        lp.setHorizontalOffset(0);
        lp.setVerticalOffset(0);
        lp.width = 0;
        lp.width = 0;
        return lp;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AdapterViewAnimator, android.view.View, android.widget.AdapterView, android.view.ViewGroup
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        checkForAndHandleDataChanged();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int childRight = this.mPaddingLeft + child.getMeasuredWidth();
            int childBottom = this.mPaddingTop + child.getMeasuredHeight();
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.layout(this.mPaddingLeft + lp.horizontalOffset, this.mPaddingTop + lp.verticalOffset, lp.horizontalOffset + childRight, lp.verticalOffset + childBottom);
        }
        onLayout();
    }

    @Override // android.widget.AdapterViewAnimator, android.widget.Advanceable
    public void advance() {
        long timeSinceLastInteraction = System.currentTimeMillis() - this.mLastInteractionTime;
        if (this.mAdapter != null) {
            if ((getCount() != 1 || !this.mLoopViews) && this.mSwipeGestureType == 0 && timeSinceLastInteraction > 5000) {
                showNext();
            }
        }
    }

    private void measureChildren() {
        int count = getChildCount();
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int childWidth = (Math.round(((float) measuredWidth) * 0.9f) - this.mPaddingLeft) - this.mPaddingRight;
        int childHeight = (Math.round(((float) measuredHeight) * 0.9f) - this.mPaddingTop) - this.mPaddingBottom;
        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.measure(View.MeasureSpec.makeMeasureSpec(childWidth, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(childHeight, Integer.MIN_VALUE));
            if (!(child == this.mHighlight || child == this.mClickFeedback)) {
                int childMeasuredWidth = child.getMeasuredWidth();
                int childMeasuredHeight = child.getMeasuredHeight();
                if (childMeasuredWidth > maxWidth) {
                    maxWidth = childMeasuredWidth;
                }
                if (childMeasuredHeight > maxHeight) {
                    maxHeight = childMeasuredHeight;
                }
            }
        }
        this.mNewPerspectiveShiftX = ((float) measuredWidth) * 0.1f;
        this.mNewPerspectiveShiftY = ((float) measuredHeight) * 0.1f;
        if (maxWidth > 0 && count > 0 && maxWidth < childWidth) {
            this.mNewPerspectiveShiftX = (float) (measuredWidth - maxWidth);
        }
        if (maxHeight > 0 && count > 0 && maxHeight < childHeight) {
            this.mNewPerspectiveShiftY = (float) (measuredHeight - maxHeight);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AdapterViewAnimator, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int i2 = 0;
        boolean haveChildRefSize = (this.mReferenceChildWidth == -1 || this.mReferenceChildHeight == -1) ? false : true;
        if (heightSpecMode == 0) {
            if (haveChildRefSize) {
                i = Math.round(((float) this.mReferenceChildHeight) * (1.1111112f + 1.0f)) + this.mPaddingTop + this.mPaddingBottom;
            } else {
                i = 0;
            }
            heightSpecSize = i;
        } else if (heightSpecMode == Integer.MIN_VALUE) {
            if (haveChildRefSize) {
                int height = Math.round(((float) this.mReferenceChildHeight) * (1.1111112f + 1.0f)) + this.mPaddingTop + this.mPaddingBottom;
                if (height <= heightSpecSize) {
                    heightSpecSize = height;
                } else {
                    heightSpecSize |= 16777216;
                }
            } else {
                heightSpecSize = 0;
            }
        }
        if (widthSpecMode == 0) {
            if (haveChildRefSize) {
                i2 = Math.round(((float) this.mReferenceChildWidth) * (1.0f + 1.1111112f)) + this.mPaddingLeft + this.mPaddingRight;
            }
            widthSpecSize = i2;
        } else if (heightSpecMode == Integer.MIN_VALUE) {
            if (haveChildRefSize) {
                int width = this.mReferenceChildWidth + this.mPaddingLeft + this.mPaddingRight;
                if (width <= widthSpecSize) {
                    widthSpecSize = width;
                } else {
                    widthSpecSize |= 16777216;
                }
            } else {
                widthSpecSize = 0;
            }
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
        measureChildren();
    }

    @Override // android.widget.AdapterViewAnimator, android.view.View, android.widget.AdapterView, android.view.ViewGroup
    public CharSequence getAccessibilityClassName() {
        return StackView.class.getName();
    }

    @Override // android.view.View, android.widget.AdapterView, android.view.ViewGroup
    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        info.setScrollable(getChildCount() > 1);
        if (isEnabled()) {
            if (getDisplayedChild() < getChildCount() - 1) {
                info.addAction(4096);
            }
            if (getDisplayedChild() > 0) {
                info.addAction(8192);
            }
        }
    }

    @Override // android.view.View
    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (!isEnabled()) {
            return false;
        }
        if (action != 4096) {
            if (action != 8192 || getDisplayedChild() <= 0) {
                return false;
            }
            showPrevious();
            return true;
        } else if (getDisplayedChild() >= getChildCount() - 1) {
            return false;
        } else {
            showNext();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class LayoutParams extends ViewGroup.LayoutParams {
        private final Rect globalInvalidateRect;
        int horizontalOffset;
        private final Rect invalidateRect;
        private final RectF invalidateRectf;
        View mView;
        private final Rect parentRect;
        int verticalOffset;

        LayoutParams(View view) {
            super(0, 0);
            this.parentRect = new Rect();
            this.invalidateRect = new Rect();
            this.invalidateRectf = new RectF();
            this.globalInvalidateRect = new Rect();
            this.width = 0;
            this.height = 0;
            this.horizontalOffset = 0;
            this.verticalOffset = 0;
            this.mView = view;
        }

        LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.parentRect = new Rect();
            this.invalidateRect = new Rect();
            this.invalidateRectf = new RectF();
            this.globalInvalidateRect = new Rect();
            this.horizontalOffset = 0;
            this.verticalOffset = 0;
            this.width = 0;
            this.height = 0;
        }

        /* access modifiers changed from: package-private */
        public void invalidateGlobalRegion(View v, Rect r) {
            this.globalInvalidateRect.set(r);
            this.globalInvalidateRect.union(0, 0, StackView.this.getWidth(), StackView.this.getHeight());
            View p = v;
            if (v.getParent() != null && (v.getParent() instanceof View)) {
                boolean firstPass = true;
                this.parentRect.set(0, 0, 0, 0);
                while (p.getParent() != null && (p.getParent() instanceof View) && !this.parentRect.contains(this.globalInvalidateRect)) {
                    if (!firstPass) {
                        this.globalInvalidateRect.offset(p.getLeft() - p.getScrollX(), p.getTop() - p.getScrollY());
                    }
                    firstPass = false;
                    p = (View) p.getParent();
                    this.parentRect.set(p.getScrollX(), p.getScrollY(), p.getWidth() + p.getScrollX(), p.getHeight() + p.getScrollY());
                    p.invalidate(this.globalInvalidateRect.left, this.globalInvalidateRect.top, this.globalInvalidateRect.right, this.globalInvalidateRect.bottom);
                }
                p.invalidate(this.globalInvalidateRect.left, this.globalInvalidateRect.top, this.globalInvalidateRect.right, this.globalInvalidateRect.bottom);
            }
        }

        /* access modifiers changed from: package-private */
        public Rect getInvalidateRect() {
            return this.invalidateRect;
        }

        /* access modifiers changed from: package-private */
        public void resetInvalidateRect() {
            this.invalidateRect.set(0, 0, 0, 0);
        }

        public void setVerticalOffset(int newVerticalOffset) {
            setOffsets(this.horizontalOffset, newVerticalOffset);
        }

        public void setHorizontalOffset(int newHorizontalOffset) {
            setOffsets(newHorizontalOffset, this.verticalOffset);
        }

        public void setOffsets(int newHorizontalOffset, int newVerticalOffset) {
            int horizontalOffsetDelta = newHorizontalOffset - this.horizontalOffset;
            this.horizontalOffset = newHorizontalOffset;
            int verticalOffsetDelta = newVerticalOffset - this.verticalOffset;
            this.verticalOffset = newVerticalOffset;
            View view = this.mView;
            if (view != null) {
                view.requestLayout();
                int left = Math.min(this.mView.getLeft() + horizontalOffsetDelta, this.mView.getLeft());
                int right = Math.max(this.mView.getRight() + horizontalOffsetDelta, this.mView.getRight());
                this.invalidateRectf.set((float) left, (float) Math.min(this.mView.getTop() + verticalOffsetDelta, this.mView.getTop()), (float) right, (float) Math.max(this.mView.getBottom() + verticalOffsetDelta, this.mView.getBottom()));
                float xoffset = -this.invalidateRectf.left;
                float yoffset = -this.invalidateRectf.top;
                this.invalidateRectf.offset(xoffset, yoffset);
                this.mView.getMatrix().mapRect(this.invalidateRectf);
                this.invalidateRectf.offset(-xoffset, -yoffset);
                this.invalidateRect.set((int) Math.floor((double) this.invalidateRectf.left), (int) Math.floor((double) this.invalidateRectf.top), (int) Math.ceil((double) this.invalidateRectf.right), (int) Math.ceil((double) this.invalidateRectf.bottom));
                invalidateGlobalRegion(this.mView, this.invalidateRect);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class HolographicHelper {
        private static final int CLICK_FEEDBACK = 1;
        private static final int RES_OUT = 0;
        private final Paint mBlurPaint = new Paint();
        private final Canvas mCanvas = new Canvas();
        private float mDensity;
        private final Paint mErasePaint = new Paint();
        private final Paint mHolographicPaint = new Paint();
        private final Matrix mIdentityMatrix = new Matrix();
        private BlurMaskFilter mLargeBlurMaskFilter;
        private final Canvas mMaskCanvas = new Canvas();
        private BlurMaskFilter mSmallBlurMaskFilter;
        private final int[] mTmpXY = new int[2];

        HolographicHelper(Context context) {
            this.mDensity = context.getResources().getDisplayMetrics().density;
            this.mHolographicPaint.setFilterBitmap(true);
            this.mHolographicPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
            this.mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            this.mErasePaint.setFilterBitmap(true);
            this.mSmallBlurMaskFilter = new BlurMaskFilter(this.mDensity * 2.0f, BlurMaskFilter.Blur.NORMAL);
            this.mLargeBlurMaskFilter = new BlurMaskFilter(this.mDensity * 4.0f, BlurMaskFilter.Blur.NORMAL);
        }

        /* access modifiers changed from: package-private */
        public Bitmap createClickOutline(View v, int color) {
            return createOutline(v, 1, color);
        }

        /* access modifiers changed from: package-private */
        public Bitmap createResOutline(View v, int color) {
            return createOutline(v, 0, color);
        }

        /* access modifiers changed from: package-private */
        public Bitmap createOutline(View v, int type, int color) {
            this.mHolographicPaint.setColor(color);
            if (type == 0) {
                this.mBlurPaint.setMaskFilter(this.mSmallBlurMaskFilter);
            } else if (type == 1) {
                this.mBlurPaint.setMaskFilter(this.mLargeBlurMaskFilter);
            }
            if (v.getMeasuredWidth() == 0 || v.getMeasuredHeight() == 0) {
                return null;
            }
            Bitmap bitmap = Bitmap.createBitmap(v.getResources().getDisplayMetrics(), v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            this.mCanvas.setBitmap(bitmap);
            float rotationX = v.getRotationX();
            float rotation = v.getRotation();
            float translationY = v.getTranslationY();
            float translationX = v.getTranslationX();
            v.setRotationX(0.0f);
            v.setRotation(0.0f);
            v.setTranslationY(0.0f);
            v.setTranslationX(0.0f);
            v.draw(this.mCanvas);
            v.setRotationX(rotationX);
            v.setRotation(rotation);
            v.setTranslationY(translationY);
            v.setTranslationX(translationX);
            drawOutline(this.mCanvas, bitmap);
            this.mCanvas.setBitmap(null);
            return bitmap;
        }

        /* access modifiers changed from: package-private */
        public void drawOutline(Canvas dest, Bitmap src) {
            int[] xy = this.mTmpXY;
            Bitmap mask = src.extractAlpha(this.mBlurPaint, xy);
            this.mMaskCanvas.setBitmap(mask);
            this.mMaskCanvas.drawBitmap(src, (float) (-xy[0]), (float) (-xy[1]), this.mErasePaint);
            dest.drawColor(0, PorterDuff.Mode.CLEAR);
            dest.setMatrix(this.mIdentityMatrix);
            dest.drawBitmap(mask, (float) xy[0], (float) xy[1], this.mHolographicPaint);
            this.mMaskCanvas.setBitmap(null);
            mask.recycle();
        }
    }
}
