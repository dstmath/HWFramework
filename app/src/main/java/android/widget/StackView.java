package android.widget;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.TableMaskFilter;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TimedRemoteCaller;
import android.view.MotionEvent;
import android.view.RemotableViewMethod;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.AsyncService;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.lang.ref.WeakReference;

@RemoteView
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

    /* renamed from: android.widget.StackView.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ View val$view;

        AnonymousClass1(View val$view) {
            this.val$view = val$view;
        }

        public void run() {
            this.val$view.setAlpha(StackView.PERSPECTIVE_SCALE_FACTOR);
        }
    }

    private static class HolographicHelper {
        private static final int CLICK_FEEDBACK = 1;
        private static final int RES_OUT = 0;
        private final Paint mBlurPaint;
        private final Canvas mCanvas;
        private float mDensity;
        private final Paint mErasePaint;
        private final Paint mHolographicPaint;
        private final Matrix mIdentityMatrix;
        private BlurMaskFilter mLargeBlurMaskFilter;
        private final Canvas mMaskCanvas;
        private BlurMaskFilter mSmallBlurMaskFilter;
        private final int[] mTmpXY;

        HolographicHelper(Context context) {
            this.mHolographicPaint = new Paint();
            this.mErasePaint = new Paint();
            this.mBlurPaint = new Paint();
            this.mCanvas = new Canvas();
            this.mMaskCanvas = new Canvas();
            this.mTmpXY = new int[StackView.GESTURE_SLIDE_DOWN];
            this.mIdentityMatrix = new Matrix();
            this.mDensity = context.getResources().getDisplayMetrics().density;
            this.mHolographicPaint.setFilterBitmap(true);
            this.mHolographicPaint.setMaskFilter(TableMaskFilter.CreateClipTable(StackView.ITEMS_SLIDE_UP, 30));
            this.mErasePaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
            this.mErasePaint.setFilterBitmap(true);
            this.mSmallBlurMaskFilter = new BlurMaskFilter(this.mDensity * 2.0f, Blur.NORMAL);
            this.mLargeBlurMaskFilter = new BlurMaskFilter(this.mDensity * 4.0f, Blur.NORMAL);
        }

        Bitmap createClickOutline(View v, int color) {
            return createOutline(v, CLICK_FEEDBACK, color);
        }

        Bitmap createResOutline(View v, int color) {
            return createOutline(v, StackView.ITEMS_SLIDE_UP, color);
        }

        Bitmap createOutline(View v, int type, int color) {
            this.mHolographicPaint.setColor(color);
            if (type == 0) {
                this.mBlurPaint.setMaskFilter(this.mSmallBlurMaskFilter);
            } else if (type == CLICK_FEEDBACK) {
                this.mBlurPaint.setMaskFilter(this.mLargeBlurMaskFilter);
            }
            if (v.getMeasuredWidth() == 0 || v.getMeasuredHeight() == 0) {
                return null;
            }
            Bitmap bitmap = Bitmap.createBitmap(v.getResources().getDisplayMetrics(), v.getMeasuredWidth(), v.getMeasuredHeight(), Config.ARGB_8888);
            this.mCanvas.setBitmap(bitmap);
            float rotationX = v.getRotationX();
            float rotation = v.getRotation();
            float translationY = v.getTranslationY();
            float translationX = v.getTranslationX();
            v.setRotationX(StackView.PERSPECTIVE_SCALE_FACTOR);
            v.setRotation(StackView.PERSPECTIVE_SCALE_FACTOR);
            v.setTranslationY(StackView.PERSPECTIVE_SCALE_FACTOR);
            v.setTranslationX(StackView.PERSPECTIVE_SCALE_FACTOR);
            v.draw(this.mCanvas);
            v.setRotationX(rotationX);
            v.setRotation(rotation);
            v.setTranslationY(translationY);
            v.setTranslationX(translationX);
            drawOutline(this.mCanvas, bitmap);
            this.mCanvas.setBitmap(null);
            return bitmap;
        }

        void drawOutline(Canvas dest, Bitmap src) {
            int[] xy = this.mTmpXY;
            Bitmap mask = src.extractAlpha(this.mBlurPaint, xy);
            this.mMaskCanvas.setBitmap(mask);
            this.mMaskCanvas.drawBitmap(src, (float) (-xy[StackView.ITEMS_SLIDE_UP]), (float) (-xy[CLICK_FEEDBACK]), this.mErasePaint);
            dest.drawColor(StackView.ITEMS_SLIDE_UP, Mode.CLEAR);
            dest.setMatrix(this.mIdentityMatrix);
            dest.drawBitmap(mask, (float) xy[StackView.ITEMS_SLIDE_UP], (float) xy[CLICK_FEEDBACK], this.mHolographicPaint);
            this.mMaskCanvas.setBitmap(null);
            mask.recycle();
        }
    }

    class LayoutParams extends android.view.ViewGroup.LayoutParams {
        private final Rect globalInvalidateRect;
        int horizontalOffset;
        private final Rect invalidateRect;
        private final RectF invalidateRectf;
        View mView;
        private final Rect parentRect;
        int verticalOffset;

        LayoutParams(View view) {
            super((int) StackView.ITEMS_SLIDE_UP, (int) StackView.ITEMS_SLIDE_UP);
            this.parentRect = new Rect();
            this.invalidateRect = new Rect();
            this.invalidateRectf = new RectF();
            this.globalInvalidateRect = new Rect();
            this.width = StackView.ITEMS_SLIDE_UP;
            this.height = StackView.ITEMS_SLIDE_UP;
            this.horizontalOffset = StackView.ITEMS_SLIDE_UP;
            this.verticalOffset = StackView.ITEMS_SLIDE_UP;
            this.mView = view;
        }

        LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.parentRect = new Rect();
            this.invalidateRect = new Rect();
            this.invalidateRectf = new RectF();
            this.globalInvalidateRect = new Rect();
            this.horizontalOffset = StackView.ITEMS_SLIDE_UP;
            this.verticalOffset = StackView.ITEMS_SLIDE_UP;
            this.width = StackView.ITEMS_SLIDE_UP;
            this.height = StackView.ITEMS_SLIDE_UP;
        }

        void invalidateGlobalRegion(View v, Rect r) {
            this.globalInvalidateRect.set(r);
            this.globalInvalidateRect.union(StackView.ITEMS_SLIDE_UP, StackView.ITEMS_SLIDE_UP, StackView.this.getWidth(), StackView.this.getHeight());
            View p = v;
            if (v.getParent() != null ? v.getParent() instanceof View : false) {
                boolean firstPass = true;
                this.parentRect.set(StackView.ITEMS_SLIDE_UP, StackView.ITEMS_SLIDE_UP, StackView.ITEMS_SLIDE_UP, StackView.ITEMS_SLIDE_UP);
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

        Rect getInvalidateRect() {
            return this.invalidateRect;
        }

        void resetInvalidateRect() {
            this.invalidateRect.set(StackView.ITEMS_SLIDE_UP, StackView.ITEMS_SLIDE_UP, StackView.ITEMS_SLIDE_UP, StackView.ITEMS_SLIDE_UP);
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
            if (this.mView != null) {
                this.mView.requestLayout();
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

    private static class StackFrame extends FrameLayout {
        WeakReference<ObjectAnimator> sliderAnimator;
        WeakReference<ObjectAnimator> transformAnimator;

        public StackFrame(Context context) {
            super(context);
        }

        void setTransformAnimator(ObjectAnimator oa) {
            this.transformAnimator = new WeakReference(oa);
        }

        void setSliderAnimator(ObjectAnimator oa) {
            this.sliderAnimator = new WeakReference(oa);
        }

        boolean cancelTransformAnimator() {
            if (this.transformAnimator != null) {
                ObjectAnimator oa = (ObjectAnimator) this.transformAnimator.get();
                if (oa != null) {
                    oa.cancel();
                    return true;
                }
            }
            return false;
        }

        boolean cancelSliderAnimator() {
            if (this.sliderAnimator != null) {
                ObjectAnimator oa = (ObjectAnimator) this.sliderAnimator.get();
                if (oa != null) {
                    oa.cancel();
                    return true;
                }
            }
            return false;
        }
    }

    private class StackSlider {
        static final int BEGINNING_OF_STACK_MODE = 1;
        static final int END_OF_STACK_MODE = 2;
        static final int NORMAL_MODE = 0;
        int mMode;
        View mView;
        float mXProgress;
        float mYProgress;

        public StackSlider() {
            this.mMode = StackView.ITEMS_SLIDE_UP;
        }

        public StackSlider(StackSlider copy) {
            this.mMode = StackView.ITEMS_SLIDE_UP;
            this.mView = copy.mView;
            this.mYProgress = copy.mYProgress;
            this.mXProgress = copy.mXProgress;
            this.mMode = copy.mMode;
        }

        private float cubic(float r) {
            return ((float) (Math.pow((double) ((2.0f * r) - android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL), 3.0d) + 1.0d)) / 2.0f;
        }

        private float highlightAlphaInterpolator(float r) {
            if (r < 0.4f) {
                return cubic(r / 0.4f) * 0.85f;
            }
            return cubic(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - ((r - 0.4f) / 0.6f)) * 0.85f;
        }

        private float viewAlphaInterpolator(float r) {
            if (r > 0.3f) {
                return (r - 0.3f) / StackView.SLIDE_UP_RATIO;
            }
            return StackView.PERSPECTIVE_SCALE_FACTOR;
        }

        private float rotationInterpolator(float r) {
            if (r < StackView.SWIPE_THRESHOLD_RATIO) {
                return StackView.PERSPECTIVE_SCALE_FACTOR;
            }
            return (r - StackView.SWIPE_THRESHOLD_RATIO) / 0.8f;
        }

        void setView(View v) {
            this.mView = v;
        }

        public void setYProgress(float r) {
            r = Math.max(StackView.PERSPECTIVE_SCALE_FACTOR, Math.min(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, r));
            this.mYProgress = r;
            if (this.mView != null) {
                LayoutParams viewLp = (LayoutParams) this.mView.getLayoutParams();
                LayoutParams highlightLp = (LayoutParams) StackView.this.mHighlight.getLayoutParams();
                int stackDirection = StackView.this.mStackMode == 0 ? BEGINNING_OF_STACK_MODE : StackView.INVALID_POINTER;
                if (Float.compare(StackView.PERSPECTIVE_SCALE_FACTOR, this.mYProgress) == 0 || Float.compare(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, this.mYProgress) == 0) {
                    if (this.mView.getLayerType() != 0) {
                        this.mView.setLayerType(StackView.ITEMS_SLIDE_UP, null);
                    }
                } else if (this.mView.getLayerType() == 0) {
                    this.mView.setLayerType(END_OF_STACK_MODE, null);
                }
                switch (this.mMode) {
                    case StackView.ITEMS_SLIDE_UP /*0*/:
                        viewLp.setVerticalOffset(Math.round(((-r) * ((float) stackDirection)) * ((float) StackView.this.mSlideAmount)));
                        highlightLp.setVerticalOffset(Math.round(((-r) * ((float) stackDirection)) * ((float) StackView.this.mSlideAmount)));
                        StackView.this.mHighlight.setAlpha(highlightAlphaInterpolator(r));
                        float alpha = viewAlphaInterpolator(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - r);
                        if (this.mView.getAlpha() == StackView.PERSPECTIVE_SCALE_FACTOR && alpha != StackView.PERSPECTIVE_SCALE_FACTOR && this.mView.getVisibility() != 0) {
                            this.mView.setVisibility(StackView.ITEMS_SLIDE_UP);
                        } else if (alpha == StackView.PERSPECTIVE_SCALE_FACTOR && this.mView.getAlpha() != StackView.PERSPECTIVE_SCALE_FACTOR && this.mView.getVisibility() == 0) {
                            this.mView.setVisibility(StackView.FRAME_PADDING);
                        }
                        this.mView.setAlpha(alpha);
                        this.mView.setRotationX((((float) stackDirection) * 90.0f) * rotationInterpolator(r));
                        StackView.this.mHighlight.setRotationX((((float) stackDirection) * 90.0f) * rotationInterpolator(r));
                        break;
                    case BEGINNING_OF_STACK_MODE /*1*/:
                        r = (android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - r) * StackView.SWIPE_THRESHOLD_RATIO;
                        viewLp.setVerticalOffset(Math.round((((float) stackDirection) * r) * ((float) StackView.this.mSlideAmount)));
                        highlightLp.setVerticalOffset(Math.round((((float) stackDirection) * r) * ((float) StackView.this.mSlideAmount)));
                        StackView.this.mHighlight.setAlpha(highlightAlphaInterpolator(r));
                        break;
                    case END_OF_STACK_MODE /*2*/:
                        r *= StackView.SWIPE_THRESHOLD_RATIO;
                        viewLp.setVerticalOffset(Math.round((((float) (-stackDirection)) * r) * ((float) StackView.this.mSlideAmount)));
                        highlightLp.setVerticalOffset(Math.round((((float) (-stackDirection)) * r) * ((float) StackView.this.mSlideAmount)));
                        StackView.this.mHighlight.setAlpha(highlightAlphaInterpolator(r));
                        break;
                }
            }
        }

        public void setXProgress(float r) {
            r = Math.max(-2.0f, Math.min(2.0f, r));
            this.mXProgress = r;
            if (this.mView != null) {
                LayoutParams highlightLp = (LayoutParams) StackView.this.mHighlight.getLayoutParams();
                r *= StackView.SWIPE_THRESHOLD_RATIO;
                ((LayoutParams) this.mView.getLayoutParams()).setHorizontalOffset(Math.round(((float) StackView.this.mSlideAmount) * r));
                highlightLp.setHorizontalOffset(Math.round(((float) StackView.this.mSlideAmount) * r));
            }
        }

        void setMode(int mode) {
            this.mMode = mode;
        }

        float getDurationForNeutralPosition() {
            return getDuration(false, StackView.PERSPECTIVE_SCALE_FACTOR);
        }

        float getDurationForOffscreenPosition() {
            return getDuration(true, StackView.PERSPECTIVE_SCALE_FACTOR);
        }

        float getDurationForNeutralPosition(float velocity) {
            return getDuration(false, velocity);
        }

        float getDurationForOffscreenPosition(float velocity) {
            return getDuration(true, velocity);
        }

        private float getDuration(boolean invert, float velocity) {
            if (this.mView == null) {
                return StackView.PERSPECTIVE_SCALE_FACTOR;
            }
            LayoutParams viewLp = (LayoutParams) this.mView.getLayoutParams();
            float d = (float) Math.hypot((double) viewLp.horizontalOffset, (double) viewLp.verticalOffset);
            float maxd = (float) Math.hypot((double) StackView.this.mSlideAmount, (double) (((float) StackView.this.mSlideAmount) * 0.4f));
            if (velocity == StackView.PERSPECTIVE_SCALE_FACTOR) {
                return (invert ? android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - (d / maxd) : d / maxd) * 400.0f;
            }
            float duration;
            if (invert) {
                duration = d / Math.abs(velocity);
            } else {
                duration = (maxd - d) / Math.abs(velocity);
            }
            if (duration < 50.0f || duration > 400.0f) {
                return getDuration(invert, StackView.PERSPECTIVE_SCALE_FACTOR);
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

    public StackView(Context context) {
        this(context, null);
    }

    public StackView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.stackViewStyle);
    }

    public StackView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, ITEMS_SLIDE_UP);
    }

    public StackView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.TAG = "StackView";
        this.mTouchRect = new Rect();
        this.mYVelocity = ITEMS_SLIDE_UP;
        this.mSwipeGestureType = ITEMS_SLIDE_UP;
        this.mTransitionIsSetup = false;
        this.mClickFeedbackIsValid = false;
        this.mFirstLayoutHappened = false;
        this.mLastInteractionTime = 0;
        this.stackInvalidateRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StackView, defStyleAttr, defStyleRes);
        this.mResOutColor = a.getColor(ITEMS_SLIDE_UP, ITEMS_SLIDE_UP);
        this.mClickColor = a.getColor(ITEMS_SLIDE_DOWN, ITEMS_SLIDE_UP);
        a.recycle();
        initStackView();
    }

    private void initStackView() {
        configureViewAnimator(NUM_ACTIVE_VIEWS, ITEMS_SLIDE_DOWN);
        setStaticTransformationsEnabled(true);
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mActivePointerId = INVALID_POINTER;
        this.mHighlight = new ImageView(getContext());
        this.mHighlight.setLayoutParams(new LayoutParams(this.mHighlight));
        addViewInLayout(this.mHighlight, INVALID_POINTER, new LayoutParams(this.mHighlight));
        this.mClickFeedback = new ImageView(getContext());
        this.mClickFeedback.setLayoutParams(new LayoutParams(this.mClickFeedback));
        addViewInLayout(this.mClickFeedback, INVALID_POINTER, new LayoutParams(this.mClickFeedback));
        this.mClickFeedback.setVisibility(FRAME_PADDING);
        this.mStackSlider = new StackSlider();
        if (sHolographicHelper == null) {
            sHolographicHelper = new HolographicHelper(this.mContext);
        }
        setClipChildren(false);
        setClipToPadding(false);
        this.mStackMode = ITEMS_SLIDE_DOWN;
        this.mWhichChild = INVALID_POINTER;
        this.mFramePadding = (int) Math.ceil((double) (4.0f * this.mContext.getResources().getDisplayMetrics().density));
    }

    void transformViewForTransition(int fromIndex, int toIndex, View view, boolean animate) {
        if (!animate) {
            ((StackFrame) view).cancelSliderAnimator();
            view.setRotationX(PERSPECTIVE_SCALE_FACTOR);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            lp.setVerticalOffset(ITEMS_SLIDE_UP);
            lp.setHorizontalOffset(ITEMS_SLIDE_UP);
        }
        if (fromIndex == INVALID_POINTER && toIndex == getNumActiveViews() + INVALID_POINTER) {
            transformViewAtIndex(toIndex, view, false);
            view.setVisibility(ITEMS_SLIDE_UP);
            view.setAlpha(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        } else if (fromIndex == 0 && toIndex == ITEMS_SLIDE_DOWN) {
            ((StackFrame) view).cancelSliderAnimator();
            view.setVisibility(ITEMS_SLIDE_UP);
            duration = Math.round(this.mStackSlider.getDurationForNeutralPosition((float) this.mYVelocity));
            animationSlider = new StackSlider(this.mStackSlider);
            animationSlider.setView(view);
            if (animate) {
                r14 = new float[ITEMS_SLIDE_DOWN];
                r14[ITEMS_SLIDE_UP] = PERSPECTIVE_SCALE_FACTOR;
                PropertyValuesHolder slideInY = PropertyValuesHolder.ofFloat("YProgress", r14);
                r14 = new float[ITEMS_SLIDE_DOWN];
                r14[ITEMS_SLIDE_UP] = PERSPECTIVE_SCALE_FACTOR;
                r13 = new PropertyValuesHolder[GESTURE_SLIDE_DOWN];
                r13[ITEMS_SLIDE_UP] = PropertyValuesHolder.ofFloat("XProgress", r14);
                r13[ITEMS_SLIDE_DOWN] = slideInY;
                ObjectAnimator slideIn = ObjectAnimator.ofPropertyValuesHolder(animationSlider, r13);
                slideIn.setDuration((long) duration);
                slideIn.setInterpolator(new LinearInterpolator());
                ((StackFrame) view).setSliderAnimator(slideIn);
                slideIn.start();
            } else {
                animationSlider.setYProgress(PERSPECTIVE_SCALE_FACTOR);
                animationSlider.setXProgress(PERSPECTIVE_SCALE_FACTOR);
            }
        } else if (fromIndex == ITEMS_SLIDE_DOWN && toIndex == 0) {
            ((StackFrame) view).cancelSliderAnimator();
            duration = Math.round(this.mStackSlider.getDurationForOffscreenPosition((float) this.mYVelocity));
            animationSlider = new StackSlider(this.mStackSlider);
            animationSlider.setView(view);
            if (animate) {
                r14 = new float[ITEMS_SLIDE_DOWN];
                r14[ITEMS_SLIDE_UP] = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
                PropertyValuesHolder slideOutY = PropertyValuesHolder.ofFloat("YProgress", r14);
                r14 = new float[ITEMS_SLIDE_DOWN];
                r14[ITEMS_SLIDE_UP] = PERSPECTIVE_SCALE_FACTOR;
                r13 = new PropertyValuesHolder[GESTURE_SLIDE_DOWN];
                r13[ITEMS_SLIDE_UP] = PropertyValuesHolder.ofFloat("XProgress", r14);
                r13[ITEMS_SLIDE_DOWN] = slideOutY;
                ObjectAnimator slideOut = ObjectAnimator.ofPropertyValuesHolder(animationSlider, r13);
                slideOut.setDuration((long) duration);
                slideOut.setInterpolator(new LinearInterpolator());
                ((StackFrame) view).setSliderAnimator(slideOut);
                slideOut.start();
            } else {
                animationSlider.setYProgress(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                animationSlider.setXProgress(PERSPECTIVE_SCALE_FACTOR);
            }
        } else if (toIndex == 0) {
            view.setAlpha(PERSPECTIVE_SCALE_FACTOR);
            view.setVisibility(FRAME_PADDING);
        } else if ((fromIndex == 0 || fromIndex == ITEMS_SLIDE_DOWN) && toIndex > ITEMS_SLIDE_DOWN) {
            view.setVisibility(ITEMS_SLIDE_UP);
            view.setAlpha(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            view.setRotationX(PERSPECTIVE_SCALE_FACTOR);
            lp = (LayoutParams) view.getLayoutParams();
            lp.setVerticalOffset(ITEMS_SLIDE_UP);
            lp.setHorizontalOffset(ITEMS_SLIDE_UP);
        } else if (fromIndex == INVALID_POINTER) {
            view.setAlpha(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            view.setVisibility(ITEMS_SLIDE_UP);
        } else if (toIndex == INVALID_POINTER) {
            if (animate) {
                postDelayed(new AnonymousClass1(view), MIN_TIME_BETWEEN_SCROLLS);
            } else {
                view.setAlpha(PERSPECTIVE_SCALE_FACTOR);
            }
        }
        if (toIndex != INVALID_POINTER) {
            transformViewAtIndex(toIndex, view, animate);
        }
    }

    private void transformViewAtIndex(int index, View view, boolean animate) {
        float maxPerspectiveShiftY = this.mPerspectiveShiftY;
        float maxPerspectiveShiftX = this.mPerspectiveShiftX;
        int i = this.mStackMode;
        if (r0 == ITEMS_SLIDE_DOWN) {
            index = (this.mMaxNumActiveViews - index) + INVALID_POINTER;
            if (index == this.mMaxNumActiveViews + INVALID_POINTER) {
                index += INVALID_POINTER;
            }
        } else {
            index += INVALID_POINTER;
            if (index < 0) {
                index += ITEMS_SLIDE_DOWN;
            }
        }
        float f = (float) index;
        float r = (r0 * android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) / ((float) (this.mMaxNumActiveViews - 2));
        float scale = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - ((android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - r) * PERSPECTIVE_SCALE_FACTOR);
        float transY = (r * maxPerspectiveShiftY) + ((scale - android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) * ((((float) getMeasuredHeight()) * 0.9f) / 2.0f));
        float transX = ((android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - r) * maxPerspectiveShiftX) + ((android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - scale) * ((((float) getMeasuredWidth()) * 0.9f) / 2.0f));
        if (view instanceof StackFrame) {
            ((StackFrame) view).cancelTransformAnimator();
        }
        if (animate) {
            float[] fArr = new float[ITEMS_SLIDE_DOWN];
            fArr[ITEMS_SLIDE_UP] = transX;
            PropertyValuesHolder translationX = PropertyValuesHolder.ofFloat("translationX", fArr);
            fArr = new float[ITEMS_SLIDE_DOWN];
            fArr[ITEMS_SLIDE_UP] = transY;
            PropertyValuesHolder translationY = PropertyValuesHolder.ofFloat("translationY", fArr);
            fArr = new float[ITEMS_SLIDE_DOWN];
            fArr[ITEMS_SLIDE_UP] = scale;
            PropertyValuesHolder scalePropX = PropertyValuesHolder.ofFloat("scaleX", fArr);
            fArr = new float[ITEMS_SLIDE_DOWN];
            fArr[ITEMS_SLIDE_UP] = scale;
            PropertyValuesHolder scalePropY = PropertyValuesHolder.ofFloat("scaleY", fArr);
            PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[FRAME_PADDING];
            propertyValuesHolderArr[ITEMS_SLIDE_UP] = scalePropX;
            propertyValuesHolderArr[ITEMS_SLIDE_DOWN] = scalePropY;
            propertyValuesHolderArr[GESTURE_SLIDE_DOWN] = translationY;
            propertyValuesHolderArr[3] = translationX;
            ObjectAnimator oa = ObjectAnimator.ofPropertyValuesHolder(view, propertyValuesHolderArr);
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
            v.setVisibility(ITEMS_SLIDE_UP);
        }
    }

    @RemotableViewMethod
    public void showNext() {
        if (this.mSwipeGestureType == 0) {
            if (!this.mTransitionIsSetup) {
                View v = getViewAtRelativeIndex(ITEMS_SLIDE_DOWN);
                if (v != null) {
                    setupStackSlider(v, ITEMS_SLIDE_UP);
                    this.mStackSlider.setYProgress(PERSPECTIVE_SCALE_FACTOR);
                    this.mStackSlider.setXProgress(PERSPECTIVE_SCALE_FACTOR);
                }
            }
            super.showNext();
        }
    }

    @RemotableViewMethod
    public void showPrevious() {
        if (this.mSwipeGestureType == 0) {
            if (!this.mTransitionIsSetup) {
                View v = getViewAtRelativeIndex(ITEMS_SLIDE_UP);
                if (v != null) {
                    setupStackSlider(v, ITEMS_SLIDE_UP);
                    this.mStackSlider.setYProgress(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                    this.mStackSlider.setXProgress(PERSPECTIVE_SCALE_FACTOR);
                }
            }
            super.showPrevious();
        }
    }

    void showOnly(int childIndex, boolean animate) {
        super.showOnly(childIndex, animate);
        for (int i = this.mCurrentWindowEnd; i >= this.mCurrentWindowStart; i += INVALID_POINTER) {
            int index = modulo(i, getWindowSize());
            if (((ViewAndMetaData) this.mViewsMap.get(Integer.valueOf(index))) != null) {
                View v = ((ViewAndMetaData) this.mViewsMap.get(Integer.valueOf(index))).view;
                if (v != null) {
                    v.bringToFront();
                }
            }
        }
        if (this.mHighlight != null) {
            this.mHighlight.bringToFront();
        }
        this.mTransitionIsSetup = false;
        this.mClickFeedbackIsValid = false;
    }

    void updateClickFeedback() {
        if (!this.mClickFeedbackIsValid) {
            View v = getViewAtRelativeIndex(ITEMS_SLIDE_DOWN);
            if (v != null) {
                this.mClickFeedback.setImageBitmap(sHolographicHelper.createClickOutline(v, this.mClickColor));
                this.mClickFeedback.setTranslationX(v.getTranslationX());
                this.mClickFeedback.setTranslationY(v.getTranslationY());
            }
            this.mClickFeedbackIsValid = true;
        }
    }

    void showTapFeedback(View v) {
        updateClickFeedback();
        this.mClickFeedback.setVisibility(ITEMS_SLIDE_UP);
        this.mClickFeedback.bringToFront();
        invalidate();
    }

    void hideTapFeedback(View v) {
        this.mClickFeedback.setVisibility(FRAME_PADDING);
        invalidate();
    }

    private void updateChildTransforms() {
        for (int i = ITEMS_SLIDE_UP; i < getNumActiveViews(); i += ITEMS_SLIDE_DOWN) {
            View v = getViewAtRelativeIndex(i);
            if (v != null) {
                transformViewAtIndex(i, v, false);
            }
        }
    }

    FrameLayout getFrameForChild() {
        StackFrame fl = new StackFrame(this.mContext);
        fl.setPadding(this.mFramePadding, this.mFramePadding, this.mFramePadding, this.mFramePadding);
        return fl;
    }

    void applyTransformForChildAtIndex(View child, int relativeIndex) {
    }

    protected void dispatchDraw(Canvas canvas) {
        boolean expandClipRegion = false;
        canvas.getClipBounds(this.stackInvalidateRect);
        int childCount = getChildCount();
        for (int i = ITEMS_SLIDE_UP; i < childCount; i += ITEMS_SLIDE_DOWN) {
            Rect childInvalidateRect;
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (!((lp.horizontalOffset == 0 && lp.verticalOffset == 0) || child.getAlpha() == PERSPECTIVE_SCALE_FACTOR)) {
                if (child.getVisibility() != 0) {
                }
                childInvalidateRect = lp.getInvalidateRect();
                if (!childInvalidateRect.isEmpty()) {
                    expandClipRegion = true;
                    this.stackInvalidateRect.union(childInvalidateRect);
                }
            }
            lp.resetInvalidateRect();
            childInvalidateRect = lp.getInvalidateRect();
            if (!childInvalidateRect.isEmpty()) {
                expandClipRegion = true;
                this.stackInvalidateRect.union(childInvalidateRect);
            }
        }
        if (expandClipRegion) {
            canvas.save(GESTURE_SLIDE_DOWN);
            canvas.clipRect(this.stackInvalidateRect, Op.UNION);
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
            this.mSwipeThreshold = Math.round(((float) newSlideAmount) * SWIPE_THRESHOLD_RATIO);
        }
        if (Float.compare(this.mPerspectiveShiftY, this.mNewPerspectiveShiftY) != 0 || Float.compare(this.mPerspectiveShiftX, this.mNewPerspectiveShiftX) != 0) {
            this.mPerspectiveShiftY = this.mNewPerspectiveShiftY;
            this.mPerspectiveShiftX = this.mNewPerspectiveShiftX;
            updateChildTransforms();
        }
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & GESTURE_SLIDE_DOWN) != 0) {
            switch (event.getAction()) {
                case PGSdk.TYPE_VIDEO /*8*/:
                    float vscroll = event.getAxisValue(9);
                    if (vscroll < PERSPECTIVE_SCALE_FACTOR) {
                        pacedScroll(false);
                        return true;
                    } else if (vscroll > PERSPECTIVE_SCALE_FACTOR) {
                        pacedScroll(true);
                        return true;
                    }
                    break;
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

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean z = false;
        switch (ev.getAction() & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
            case ITEMS_SLIDE_UP /*0*/:
                if (this.mActivePointerId == INVALID_POINTER) {
                    this.mInitialX = ev.getX();
                    this.mInitialY = ev.getY();
                    this.mActivePointerId = ev.getPointerId(ITEMS_SLIDE_UP);
                    break;
                }
                break;
            case ITEMS_SLIDE_DOWN /*1*/:
            case HwCfgFilePolicy.BASE /*3*/:
                this.mActivePointerId = INVALID_POINTER;
                this.mSwipeGestureType = ITEMS_SLIDE_UP;
                break;
            case GESTURE_SLIDE_DOWN /*2*/:
                int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (pointerIndex != INVALID_POINTER) {
                    beginGestureIfNeeded(ev.getY(pointerIndex) - this.mInitialY);
                    break;
                }
                Log.d("StackView", "Error: No data for our primary pointer.");
                return false;
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                onSecondaryPointerUp(ev);
                break;
        }
        if (this.mSwipeGestureType != 0) {
            z = true;
        }
        return z;
    }

    private void beginGestureIfNeeded(float deltaY) {
        boolean z = true;
        if (((int) Math.abs(deltaY)) > this.mTouchSlop && this.mSwipeGestureType == 0) {
            int swipeGestureType = deltaY < PERSPECTIVE_SCALE_FACTOR ? ITEMS_SLIDE_DOWN : GESTURE_SLIDE_DOWN;
            cancelLongPress();
            requestDisallowInterceptTouchEvent(true);
            if (this.mAdapter != null) {
                int stackMode;
                int adapterCount = getCount();
                int activeIndex = this.mStackMode == 0 ? swipeGestureType == GESTURE_SLIDE_DOWN ? ITEMS_SLIDE_UP : ITEMS_SLIDE_DOWN : swipeGestureType == GESTURE_SLIDE_DOWN ? ITEMS_SLIDE_DOWN : ITEMS_SLIDE_UP;
                boolean endOfStack = (this.mLoopViews && adapterCount == ITEMS_SLIDE_DOWN) ? (this.mStackMode == 0 && swipeGestureType == ITEMS_SLIDE_DOWN) ? true : this.mStackMode == ITEMS_SLIDE_DOWN && swipeGestureType == GESTURE_SLIDE_DOWN : false;
                boolean beginningOfStack = (this.mLoopViews && adapterCount == ITEMS_SLIDE_DOWN) ? (this.mStackMode == ITEMS_SLIDE_DOWN && swipeGestureType == ITEMS_SLIDE_DOWN) ? true : this.mStackMode == 0 && swipeGestureType == GESTURE_SLIDE_DOWN : false;
                if (this.mLoopViews && !beginningOfStack && !endOfStack) {
                    stackMode = ITEMS_SLIDE_UP;
                } else if (this.mCurrentWindowStartUnbounded + activeIndex == INVALID_POINTER || beginningOfStack) {
                    activeIndex += ITEMS_SLIDE_DOWN;
                    stackMode = ITEMS_SLIDE_DOWN;
                } else if (this.mCurrentWindowStartUnbounded + activeIndex == adapterCount + INVALID_POINTER || endOfStack) {
                    stackMode = GESTURE_SLIDE_DOWN;
                } else {
                    stackMode = ITEMS_SLIDE_UP;
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

    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        int action = ev.getAction();
        int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
        if (pointerIndex == INVALID_POINTER) {
            Log.d("StackView", "Error: No data for our primary pointer.");
            return false;
        }
        float newY = ev.getY(pointerIndex);
        float deltaY = newY - this.mInitialY;
        float deltaX = ev.getX(pointerIndex) - this.mInitialX;
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        switch (action & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
            case ITEMS_SLIDE_DOWN /*1*/:
                handlePointerUp(ev);
                break;
            case GESTURE_SLIDE_DOWN /*2*/:
                beginGestureIfNeeded(deltaY);
                float rx = deltaX / (((float) this.mSlideAmount) * android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                float r;
                if (this.mSwipeGestureType == GESTURE_SLIDE_DOWN) {
                    r = ((deltaY - (((float) this.mTouchSlop) * android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL)) / ((float) this.mSlideAmount)) * android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
                    if (this.mStackMode == ITEMS_SLIDE_DOWN) {
                        r = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - r;
                    }
                    this.mStackSlider.setYProgress(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - r);
                    this.mStackSlider.setXProgress(rx);
                    return true;
                } else if (this.mSwipeGestureType == ITEMS_SLIDE_DOWN) {
                    r = ((-((((float) this.mTouchSlop) * android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) + deltaY)) / ((float) this.mSlideAmount)) * android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
                    if (this.mStackMode == ITEMS_SLIDE_DOWN) {
                        r = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - r;
                    }
                    this.mStackSlider.setYProgress(r);
                    this.mStackSlider.setXProgress(rx);
                    return true;
                }
                break;
            case HwCfgFilePolicy.BASE /*3*/:
                this.mActivePointerId = INVALID_POINTER;
                this.mSwipeGestureType = ITEMS_SLIDE_UP;
                break;
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                onSecondaryPointerUp(ev);
                break;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int activePointerIndex = ev.getActionIndex();
        if (ev.getPointerId(activePointerIndex) == this.mActivePointerId) {
            View v = getViewAtRelativeIndex(this.mSwipeGestureType == GESTURE_SLIDE_DOWN ? ITEMS_SLIDE_UP : ITEMS_SLIDE_DOWN);
            if (v != null) {
                for (int index = ITEMS_SLIDE_UP; index < ev.getPointerCount(); index += ITEMS_SLIDE_DOWN) {
                    if (index != activePointerIndex) {
                        float x = ev.getX(index);
                        float y = ev.getY(index);
                        this.mTouchRect.set(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                        if (this.mTouchRect.contains(Math.round(x), Math.round(y))) {
                            float oldX = ev.getX(activePointerIndex);
                            this.mInitialY += y - ev.getY(activePointerIndex);
                            this.mInitialX += x - oldX;
                            this.mActivePointerId = ev.getPointerId(index);
                            if (this.mVelocityTracker != null) {
                                this.mVelocityTracker.clear();
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
        int deltaY = (int) (ev.getY(ev.findPointerIndex(this.mActivePointerId)) - this.mInitialY);
        this.mLastInteractionTime = System.currentTimeMillis();
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.computeCurrentVelocity(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, (float) this.mMaximumVelocity);
            this.mYVelocity = (int) this.mVelocityTracker.getYVelocity(this.mActivePointerId);
        }
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
        if (deltaY > this.mSwipeThreshold && this.mSwipeGestureType == GESTURE_SLIDE_DOWN && this.mStackSlider.mMode == 0) {
            this.mSwipeGestureType = ITEMS_SLIDE_UP;
            if (this.mStackMode == 0) {
                showPrevious();
            } else {
                showNext();
            }
            this.mHighlight.bringToFront();
        } else if (deltaY < (-this.mSwipeThreshold) && this.mSwipeGestureType == ITEMS_SLIDE_DOWN && this.mStackSlider.mMode == 0) {
            this.mSwipeGestureType = ITEMS_SLIDE_UP;
            if (this.mStackMode == 0) {
                showNext();
            } else {
                showPrevious();
            }
            this.mHighlight.bringToFront();
        } else if (this.mSwipeGestureType == ITEMS_SLIDE_DOWN) {
            finalYProgress = (float) (this.mStackMode == ITEMS_SLIDE_DOWN ? ITEMS_SLIDE_DOWN : ITEMS_SLIDE_UP);
            if (this.mStackMode == 0 || this.mStackSlider.mMode != 0) {
                duration = Math.round(this.mStackSlider.getDurationForNeutralPosition());
            } else {
                duration = Math.round(this.mStackSlider.getDurationForOffscreenPosition());
            }
            animationSlider = new StackSlider(this.mStackSlider);
            r10 = new float[ITEMS_SLIDE_DOWN];
            r10[ITEMS_SLIDE_UP] = finalYProgress;
            snapBackY = PropertyValuesHolder.ofFloat("YProgress", r10);
            r10 = new float[ITEMS_SLIDE_DOWN];
            r10[ITEMS_SLIDE_UP] = PERSPECTIVE_SCALE_FACTOR;
            r9 = new PropertyValuesHolder[GESTURE_SLIDE_DOWN];
            r9[ITEMS_SLIDE_UP] = PropertyValuesHolder.ofFloat("XProgress", r10);
            r9[ITEMS_SLIDE_DOWN] = snapBackY;
            pa = ObjectAnimator.ofPropertyValuesHolder(animationSlider, r9);
            pa.setDuration((long) duration);
            pa.setInterpolator(new LinearInterpolator());
            pa.start();
        } else if (this.mSwipeGestureType == GESTURE_SLIDE_DOWN) {
            finalYProgress = (float) (this.mStackMode == ITEMS_SLIDE_DOWN ? ITEMS_SLIDE_UP : ITEMS_SLIDE_DOWN);
            if (this.mStackMode == ITEMS_SLIDE_DOWN || this.mStackSlider.mMode != 0) {
                duration = Math.round(this.mStackSlider.getDurationForNeutralPosition());
            } else {
                duration = Math.round(this.mStackSlider.getDurationForOffscreenPosition());
            }
            animationSlider = new StackSlider(this.mStackSlider);
            r10 = new float[ITEMS_SLIDE_DOWN];
            r10[ITEMS_SLIDE_UP] = finalYProgress;
            snapBackY = PropertyValuesHolder.ofFloat("YProgress", r10);
            r10 = new float[ITEMS_SLIDE_DOWN];
            r10[ITEMS_SLIDE_UP] = PERSPECTIVE_SCALE_FACTOR;
            r9 = new PropertyValuesHolder[GESTURE_SLIDE_DOWN];
            r9[ITEMS_SLIDE_UP] = PropertyValuesHolder.ofFloat("XProgress", r10);
            r9[ITEMS_SLIDE_DOWN] = snapBackY;
            pa = ObjectAnimator.ofPropertyValuesHolder(animationSlider, r9);
            pa.setDuration((long) duration);
            pa.start();
        }
        this.mActivePointerId = INVALID_POINTER;
        this.mSwipeGestureType = ITEMS_SLIDE_UP;
    }

    LayoutParams createOrReuseLayoutParams(View v) {
        android.view.ViewGroup.LayoutParams currentLp = v.getLayoutParams();
        if (!(currentLp instanceof LayoutParams)) {
            return new LayoutParams(v);
        }
        LayoutParams lp = (LayoutParams) currentLp;
        lp.setHorizontalOffset(ITEMS_SLIDE_UP);
        lp.setVerticalOffset(ITEMS_SLIDE_UP);
        lp.width = ITEMS_SLIDE_UP;
        lp.width = ITEMS_SLIDE_UP;
        return lp;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        checkForAndHandleDataChanged();
        int childCount = getChildCount();
        for (int i = ITEMS_SLIDE_UP; i < childCount; i += ITEMS_SLIDE_DOWN) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.layout(this.mPaddingLeft + lp.horizontalOffset, this.mPaddingTop + lp.verticalOffset, lp.horizontalOffset + (this.mPaddingLeft + child.getMeasuredWidth()), lp.verticalOffset + (this.mPaddingTop + child.getMeasuredHeight()));
        }
        onLayout();
    }

    public void advance() {
        long timeSinceLastInteraction = System.currentTimeMillis() - this.mLastInteractionTime;
        if (this.mAdapter != null) {
            if (!(getCount() == ITEMS_SLIDE_DOWN && this.mLoopViews) && this.mSwipeGestureType == 0 && timeSinceLastInteraction > TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS) {
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
        int maxWidth = ITEMS_SLIDE_UP;
        int maxHeight = ITEMS_SLIDE_UP;
        for (int i = ITEMS_SLIDE_UP; i < count; i += ITEMS_SLIDE_DOWN) {
            View child = getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, RtlSpacingHelper.UNDEFINED), MeasureSpec.makeMeasureSpec(childHeight, RtlSpacingHelper.UNDEFINED));
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
        this.mNewPerspectiveShiftX = ((float) measuredWidth) * PERSPECTIVE_SHIFT_FACTOR_Y;
        this.mNewPerspectiveShiftY = ((float) measuredHeight) * PERSPECTIVE_SHIFT_FACTOR_Y;
        if (maxWidth > 0 && count > 0 && maxWidth < childWidth) {
            this.mNewPerspectiveShiftX = (float) (measuredWidth - maxWidth);
        }
        if (maxHeight > 0 && count > 0 && maxHeight < childHeight) {
            this.mNewPerspectiveShiftY = (float) (measuredHeight - maxHeight);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean haveChildRefSize = (this.mReferenceChildWidth == INVALID_POINTER || this.mReferenceChildHeight == INVALID_POINTER) ? false : true;
        if (heightSpecMode == 0) {
            if (haveChildRefSize) {
                heightSpecSize = (Math.round(((float) this.mReferenceChildHeight) * 2.1111112f) + this.mPaddingTop) + this.mPaddingBottom;
            } else {
                heightSpecSize = ITEMS_SLIDE_UP;
            }
        } else if (heightSpecMode == RtlSpacingHelper.UNDEFINED) {
            if (haveChildRefSize) {
                int height = (Math.round(((float) this.mReferenceChildHeight) * 2.1111112f) + this.mPaddingTop) + this.mPaddingBottom;
                if (height <= heightSpecSize) {
                    heightSpecSize = height;
                } else {
                    heightSpecSize |= AsyncService.CMD_ASYNC_SERVICE_DESTROY;
                }
            } else {
                heightSpecSize = ITEMS_SLIDE_UP;
            }
        }
        if (widthSpecMode == 0) {
            if (haveChildRefSize) {
                widthSpecSize = (Math.round(((float) this.mReferenceChildWidth) * 2.1111112f) + this.mPaddingLeft) + this.mPaddingRight;
            } else {
                widthSpecSize = ITEMS_SLIDE_UP;
            }
        } else if (heightSpecMode == RtlSpacingHelper.UNDEFINED) {
            if (haveChildRefSize) {
                int width = (this.mReferenceChildWidth + this.mPaddingLeft) + this.mPaddingRight;
                if (width <= widthSpecSize) {
                    widthSpecSize = width;
                } else {
                    widthSpecSize |= AsyncService.CMD_ASYNC_SERVICE_DESTROY;
                }
            } else {
                widthSpecSize = ITEMS_SLIDE_UP;
            }
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
        measureChildren();
    }

    public CharSequence getAccessibilityClassName() {
        return StackView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        boolean z = true;
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (getChildCount() <= ITEMS_SLIDE_DOWN) {
            z = false;
        }
        info.setScrollable(z);
        if (isEnabled()) {
            if (getDisplayedChild() < getChildCount() + INVALID_POINTER) {
                info.addAction((int) HwPerformance.PERF_EVENT_RAW_REQ);
            }
            if (getDisplayedChild() > 0) {
                info.addAction((int) AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            }
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (!isEnabled()) {
            return false;
        }
        switch (action) {
            case HwPerformance.PERF_EVENT_RAW_REQ /*4096*/:
                if (getDisplayedChild() >= getChildCount() + INVALID_POINTER) {
                    return false;
                }
                showNext();
                return true;
            case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD /*8192*/:
                if (getDisplayedChild() <= 0) {
                    return false;
                }
                showPrevious();
                return true;
            default:
                return false;
        }
    }
}
