package android.gesture;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import com.android.internal.R;
import java.util.ArrayList;

public class GestureOverlayView extends FrameLayout {
    private static final boolean DITHER_FLAG = true;
    private static final int FADE_ANIMATION_RATE = 16;
    private static final boolean GESTURE_RENDERING_ANTIALIAS = true;
    public static final int GESTURE_STROKE_TYPE_MULTIPLE = 1;
    public static final int GESTURE_STROKE_TYPE_SINGLE = 0;
    public static final int ORIENTATION_HORIZONTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;
    private int mCertainGestureColor;
    private int mCurrentColor;
    /* access modifiers changed from: private */
    public Gesture mCurrentGesture;
    private float mCurveEndX;
    private float mCurveEndY;
    /* access modifiers changed from: private */
    public long mFadeDuration;
    private boolean mFadeEnabled;
    private long mFadeOffset;
    /* access modifiers changed from: private */
    public float mFadingAlpha;
    /* access modifiers changed from: private */
    public boolean mFadingHasStarted;
    private final FadeOutRunnable mFadingOut;
    /* access modifiers changed from: private */
    public long mFadingStart;
    private final Paint mGesturePaint;
    private float mGestureStrokeAngleThreshold;
    private float mGestureStrokeLengthThreshold;
    private float mGestureStrokeSquarenessTreshold;
    private int mGestureStrokeType;
    private float mGestureStrokeWidth;
    private boolean mGestureVisible;
    private boolean mHandleGestureActions;
    private boolean mInterceptEvents;
    /* access modifiers changed from: private */
    public final AccelerateDecelerateInterpolator mInterpolator;
    private final Rect mInvalidRect;
    private int mInvalidateExtraBorder;
    /* access modifiers changed from: private */
    public boolean mIsFadingOut;
    private boolean mIsGesturing;
    private boolean mIsListeningForGestures;
    private final ArrayList<OnGestureListener> mOnGestureListeners;
    private final ArrayList<OnGesturePerformedListener> mOnGesturePerformedListeners;
    private final ArrayList<OnGesturingListener> mOnGesturingListeners;
    private int mOrientation;
    /* access modifiers changed from: private */
    public final Path mPath;
    /* access modifiers changed from: private */
    public boolean mPreviousWasGesturing;
    /* access modifiers changed from: private */
    public boolean mResetGesture;
    private final ArrayList<GesturePoint> mStrokeBuffer;
    private float mTotalLength;
    private int mUncertainGestureColor;
    private float mX;
    private float mY;

    private class FadeOutRunnable implements Runnable {
        boolean fireActionPerformed;
        boolean resetMultipleStrokes;

        private FadeOutRunnable() {
        }

        public void run() {
            if (GestureOverlayView.this.mIsFadingOut) {
                long duration = AnimationUtils.currentAnimationTimeMillis() - GestureOverlayView.this.mFadingStart;
                if (duration > GestureOverlayView.this.mFadeDuration) {
                    if (this.fireActionPerformed) {
                        GestureOverlayView.this.fireOnGesturePerformed();
                    }
                    boolean unused = GestureOverlayView.this.mPreviousWasGesturing = false;
                    boolean unused2 = GestureOverlayView.this.mIsFadingOut = false;
                    boolean unused3 = GestureOverlayView.this.mFadingHasStarted = false;
                    GestureOverlayView.this.mPath.rewind();
                    Gesture unused4 = GestureOverlayView.this.mCurrentGesture = null;
                    GestureOverlayView.this.setPaintAlpha(255);
                } else {
                    boolean unused5 = GestureOverlayView.this.mFadingHasStarted = true;
                    float unused6 = GestureOverlayView.this.mFadingAlpha = 1.0f - GestureOverlayView.this.mInterpolator.getInterpolation(Math.max(0.0f, Math.min(1.0f, ((float) duration) / ((float) GestureOverlayView.this.mFadeDuration))));
                    GestureOverlayView.this.setPaintAlpha((int) (255.0f * GestureOverlayView.this.mFadingAlpha));
                    GestureOverlayView.this.postDelayed(this, 16);
                }
            } else if (this.resetMultipleStrokes) {
                boolean unused7 = GestureOverlayView.this.mResetGesture = true;
            } else {
                GestureOverlayView.this.fireOnGesturePerformed();
                boolean unused8 = GestureOverlayView.this.mFadingHasStarted = false;
                GestureOverlayView.this.mPath.rewind();
                Gesture unused9 = GestureOverlayView.this.mCurrentGesture = null;
                boolean unused10 = GestureOverlayView.this.mPreviousWasGesturing = false;
                GestureOverlayView.this.setPaintAlpha(255);
            }
            GestureOverlayView.this.invalidate();
        }
    }

    public interface OnGestureListener {
        void onGesture(GestureOverlayView gestureOverlayView, MotionEvent motionEvent);

        void onGestureCancelled(GestureOverlayView gestureOverlayView, MotionEvent motionEvent);

        void onGestureEnded(GestureOverlayView gestureOverlayView, MotionEvent motionEvent);

        void onGestureStarted(GestureOverlayView gestureOverlayView, MotionEvent motionEvent);
    }

    public interface OnGesturePerformedListener {
        void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture);
    }

    public interface OnGesturingListener {
        void onGesturingEnded(GestureOverlayView gestureOverlayView);

        void onGesturingStarted(GestureOverlayView gestureOverlayView);
    }

    public GestureOverlayView(Context context) {
        super(context);
        this.mGesturePaint = new Paint();
        this.mFadeDuration = 150;
        this.mFadeOffset = 420;
        this.mFadeEnabled = true;
        this.mCertainGestureColor = Color.YELLOW;
        this.mUncertainGestureColor = 1224736512;
        this.mGestureStrokeWidth = 12.0f;
        this.mInvalidateExtraBorder = 10;
        this.mGestureStrokeType = 0;
        this.mGestureStrokeLengthThreshold = 50.0f;
        this.mGestureStrokeSquarenessTreshold = 0.275f;
        this.mGestureStrokeAngleThreshold = 40.0f;
        this.mOrientation = 1;
        this.mInvalidRect = new Rect();
        this.mPath = new Path();
        this.mGestureVisible = true;
        this.mIsGesturing = false;
        this.mPreviousWasGesturing = false;
        this.mInterceptEvents = true;
        this.mStrokeBuffer = new ArrayList<>(100);
        this.mOnGestureListeners = new ArrayList<>();
        this.mOnGesturePerformedListeners = new ArrayList<>();
        this.mOnGesturingListeners = new ArrayList<>();
        this.mIsFadingOut = false;
        this.mFadingAlpha = 1.0f;
        this.mInterpolator = new AccelerateDecelerateInterpolator();
        this.mFadingOut = new FadeOutRunnable();
        init();
    }

    public GestureOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.gestureOverlayViewStyle);
    }

    public GestureOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GestureOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mGesturePaint = new Paint();
        this.mFadeDuration = 150;
        this.mFadeOffset = 420;
        this.mFadeEnabled = true;
        this.mCertainGestureColor = Color.YELLOW;
        this.mUncertainGestureColor = 1224736512;
        this.mGestureStrokeWidth = 12.0f;
        this.mInvalidateExtraBorder = 10;
        this.mGestureStrokeType = 0;
        this.mGestureStrokeLengthThreshold = 50.0f;
        this.mGestureStrokeSquarenessTreshold = 0.275f;
        this.mGestureStrokeAngleThreshold = 40.0f;
        this.mOrientation = 1;
        this.mInvalidRect = new Rect();
        this.mPath = new Path();
        this.mGestureVisible = true;
        this.mIsGesturing = false;
        this.mPreviousWasGesturing = false;
        this.mInterceptEvents = true;
        this.mStrokeBuffer = new ArrayList<>(100);
        this.mOnGestureListeners = new ArrayList<>();
        this.mOnGesturePerformedListeners = new ArrayList<>();
        this.mOnGesturingListeners = new ArrayList<>();
        this.mIsFadingOut = false;
        this.mFadingAlpha = 1.0f;
        this.mInterpolator = new AccelerateDecelerateInterpolator();
        this.mFadingOut = new FadeOutRunnable();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GestureOverlayView, defStyleAttr, defStyleRes);
        this.mGestureStrokeWidth = a.getFloat(1, this.mGestureStrokeWidth);
        this.mInvalidateExtraBorder = Math.max(1, ((int) this.mGestureStrokeWidth) - 1);
        this.mCertainGestureColor = a.getColor(2, this.mCertainGestureColor);
        this.mUncertainGestureColor = a.getColor(3, this.mUncertainGestureColor);
        this.mFadeDuration = (long) a.getInt(5, (int) this.mFadeDuration);
        this.mFadeOffset = (long) a.getInt(4, (int) this.mFadeOffset);
        this.mGestureStrokeType = a.getInt(6, this.mGestureStrokeType);
        this.mGestureStrokeLengthThreshold = a.getFloat(7, this.mGestureStrokeLengthThreshold);
        this.mGestureStrokeAngleThreshold = a.getFloat(9, this.mGestureStrokeAngleThreshold);
        this.mGestureStrokeSquarenessTreshold = a.getFloat(8, this.mGestureStrokeSquarenessTreshold);
        this.mInterceptEvents = a.getBoolean(10, this.mInterceptEvents);
        this.mFadeEnabled = a.getBoolean(11, this.mFadeEnabled);
        this.mOrientation = a.getInt(0, this.mOrientation);
        a.recycle();
        init();
    }

    private void init() {
        setWillNotDraw(false);
        Paint gesturePaint = this.mGesturePaint;
        gesturePaint.setAntiAlias(true);
        gesturePaint.setColor(this.mCertainGestureColor);
        gesturePaint.setStyle(Paint.Style.STROKE);
        gesturePaint.setStrokeJoin(Paint.Join.ROUND);
        gesturePaint.setStrokeCap(Paint.Cap.ROUND);
        gesturePaint.setStrokeWidth(this.mGestureStrokeWidth);
        gesturePaint.setDither(true);
        this.mCurrentColor = this.mCertainGestureColor;
        setPaintAlpha(255);
    }

    public ArrayList<GesturePoint> getCurrentStroke() {
        return this.mStrokeBuffer;
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    public void setGestureColor(int color) {
        this.mCertainGestureColor = color;
    }

    public void setUncertainGestureColor(int color) {
        this.mUncertainGestureColor = color;
    }

    public int getUncertainGestureColor() {
        return this.mUncertainGestureColor;
    }

    public int getGestureColor() {
        return this.mCertainGestureColor;
    }

    public float getGestureStrokeWidth() {
        return this.mGestureStrokeWidth;
    }

    public void setGestureStrokeWidth(float gestureStrokeWidth) {
        this.mGestureStrokeWidth = gestureStrokeWidth;
        this.mInvalidateExtraBorder = Math.max(1, ((int) gestureStrokeWidth) - 1);
        this.mGesturePaint.setStrokeWidth(gestureStrokeWidth);
    }

    public int getGestureStrokeType() {
        return this.mGestureStrokeType;
    }

    public void setGestureStrokeType(int gestureStrokeType) {
        this.mGestureStrokeType = gestureStrokeType;
    }

    public float getGestureStrokeLengthThreshold() {
        return this.mGestureStrokeLengthThreshold;
    }

    public void setGestureStrokeLengthThreshold(float gestureStrokeLengthThreshold) {
        this.mGestureStrokeLengthThreshold = gestureStrokeLengthThreshold;
    }

    public float getGestureStrokeSquarenessTreshold() {
        return this.mGestureStrokeSquarenessTreshold;
    }

    public void setGestureStrokeSquarenessTreshold(float gestureStrokeSquarenessTreshold) {
        this.mGestureStrokeSquarenessTreshold = gestureStrokeSquarenessTreshold;
    }

    public float getGestureStrokeAngleThreshold() {
        return this.mGestureStrokeAngleThreshold;
    }

    public void setGestureStrokeAngleThreshold(float gestureStrokeAngleThreshold) {
        this.mGestureStrokeAngleThreshold = gestureStrokeAngleThreshold;
    }

    public boolean isEventsInterceptionEnabled() {
        return this.mInterceptEvents;
    }

    public void setEventsInterceptionEnabled(boolean enabled) {
        this.mInterceptEvents = enabled;
    }

    public boolean isFadeEnabled() {
        return this.mFadeEnabled;
    }

    public void setFadeEnabled(boolean fadeEnabled) {
        this.mFadeEnabled = fadeEnabled;
    }

    public Gesture getGesture() {
        return this.mCurrentGesture;
    }

    public void setGesture(Gesture gesture) {
        if (this.mCurrentGesture != null) {
            clear(false);
        }
        setCurrentColor(this.mCertainGestureColor);
        this.mCurrentGesture = gesture;
        Path path = this.mCurrentGesture.toPath();
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        this.mPath.rewind();
        this.mPath.addPath(path, (-bounds.left) + ((((float) getWidth()) - bounds.width()) / 2.0f), (-bounds.top) + ((((float) getHeight()) - bounds.height()) / 2.0f));
        this.mResetGesture = true;
        invalidate();
    }

    public Path getGesturePath() {
        return this.mPath;
    }

    public Path getGesturePath(Path path) {
        path.set(this.mPath);
        return path;
    }

    public boolean isGestureVisible() {
        return this.mGestureVisible;
    }

    public void setGestureVisible(boolean visible) {
        this.mGestureVisible = visible;
    }

    public long getFadeOffset() {
        return this.mFadeOffset;
    }

    public void setFadeOffset(long fadeOffset) {
        this.mFadeOffset = fadeOffset;
    }

    public void addOnGestureListener(OnGestureListener listener) {
        this.mOnGestureListeners.add(listener);
    }

    public void removeOnGestureListener(OnGestureListener listener) {
        this.mOnGestureListeners.remove(listener);
    }

    public void removeAllOnGestureListeners() {
        this.mOnGestureListeners.clear();
    }

    public void addOnGesturePerformedListener(OnGesturePerformedListener listener) {
        this.mOnGesturePerformedListeners.add(listener);
        if (this.mOnGesturePerformedListeners.size() > 0) {
            this.mHandleGestureActions = true;
        }
    }

    public void removeOnGesturePerformedListener(OnGesturePerformedListener listener) {
        this.mOnGesturePerformedListeners.remove(listener);
        if (this.mOnGesturePerformedListeners.size() <= 0) {
            this.mHandleGestureActions = false;
        }
    }

    public void removeAllOnGesturePerformedListeners() {
        this.mOnGesturePerformedListeners.clear();
        this.mHandleGestureActions = false;
    }

    public void addOnGesturingListener(OnGesturingListener listener) {
        this.mOnGesturingListeners.add(listener);
    }

    public void removeOnGesturingListener(OnGesturingListener listener) {
        this.mOnGesturingListeners.remove(listener);
    }

    public void removeAllOnGesturingListeners() {
        this.mOnGesturingListeners.clear();
    }

    public boolean isGesturing() {
        return this.mIsGesturing;
    }

    private void setCurrentColor(int color) {
        this.mCurrentColor = color;
        if (this.mFadingHasStarted) {
            setPaintAlpha((int) (255.0f * this.mFadingAlpha));
        } else {
            setPaintAlpha(255);
        }
        invalidate();
    }

    public Paint getGesturePaint() {
        return this.mGesturePaint;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mCurrentGesture != null && this.mGestureVisible) {
            canvas.drawPath(this.mPath, this.mGesturePaint);
        }
    }

    /* access modifiers changed from: private */
    public void setPaintAlpha(int alpha) {
        this.mGesturePaint.setColor(((this.mCurrentColor << 8) >>> 8) | ((((this.mCurrentColor >>> 24) * (alpha + (alpha >> 7))) >> 8) << 24));
    }

    public void clear(boolean animated) {
        clear(animated, false, true);
    }

    private void clear(boolean animated, boolean fireActionPerformed, boolean immediate) {
        setPaintAlpha(255);
        removeCallbacks(this.mFadingOut);
        this.mResetGesture = false;
        this.mFadingOut.fireActionPerformed = fireActionPerformed;
        this.mFadingOut.resetMultipleStrokes = false;
        if (!animated || this.mCurrentGesture == null) {
            this.mFadingAlpha = 1.0f;
            this.mIsFadingOut = false;
            this.mFadingHasStarted = false;
            if (immediate) {
                this.mCurrentGesture = null;
                this.mPath.rewind();
                invalidate();
            } else if (fireActionPerformed) {
                postDelayed(this.mFadingOut, this.mFadeOffset);
            } else if (this.mGestureStrokeType == 1) {
                this.mFadingOut.resetMultipleStrokes = true;
                postDelayed(this.mFadingOut, this.mFadeOffset);
            } else {
                this.mCurrentGesture = null;
                this.mPath.rewind();
                invalidate();
            }
        } else {
            this.mFadingAlpha = 1.0f;
            this.mIsFadingOut = true;
            this.mFadingHasStarted = false;
            this.mFadingStart = AnimationUtils.currentAnimationTimeMillis() + this.mFadeOffset;
            postDelayed(this.mFadingOut, this.mFadeOffset);
        }
    }

    public void cancelClearAnimation() {
        setPaintAlpha(255);
        this.mIsFadingOut = false;
        this.mFadingHasStarted = false;
        removeCallbacks(this.mFadingOut);
        this.mPath.rewind();
        this.mCurrentGesture = null;
    }

    public void cancelGesture() {
        this.mIsListeningForGestures = false;
        this.mCurrentGesture.addStroke(new GestureStroke(this.mStrokeBuffer));
        long now = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
        ArrayList<OnGestureListener> listeners = this.mOnGestureListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onGestureCancelled(this, event);
        }
        event.recycle();
        clear(false);
        this.mIsGesturing = false;
        this.mPreviousWasGesturing = false;
        this.mStrokeBuffer.clear();
        ArrayList<OnGesturingListener> otherListeners = this.mOnGesturingListeners;
        int count2 = otherListeners.size();
        for (int i2 = 0; i2 < count2; i2++) {
            otherListeners.get(i2).onGesturingEnded(this);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelClearAnimation();
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return super.dispatchTouchEvent(event);
        }
        boolean cancelDispatch = (this.mIsGesturing || (this.mCurrentGesture != null && this.mCurrentGesture.getStrokesCount() > 0 && this.mPreviousWasGesturing)) && this.mInterceptEvents;
        processEvent(event);
        if (cancelDispatch) {
            event.setAction(3);
        }
        super.dispatchTouchEvent(event);
        return true;
    }

    private boolean processEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                touchDown(event);
                invalidate();
                return true;
            case 1:
                if (this.mIsListeningForGestures) {
                    touchUp(event, false);
                    invalidate();
                    return true;
                }
                break;
            case 2:
                if (this.mIsListeningForGestures) {
                    Rect rect = touchMove(event);
                    if (rect != null) {
                        invalidate(rect);
                    }
                    return true;
                }
                break;
            case 3:
                if (this.mIsListeningForGestures) {
                    touchUp(event, true);
                    invalidate();
                    return true;
                }
                break;
        }
        return false;
    }

    private void touchDown(MotionEvent event) {
        this.mIsListeningForGestures = true;
        float x = event.getX();
        float y = event.getY();
        this.mX = x;
        this.mY = y;
        this.mTotalLength = 0.0f;
        this.mIsGesturing = false;
        if (this.mGestureStrokeType == 0 || this.mResetGesture) {
            if (this.mHandleGestureActions) {
                setCurrentColor(this.mUncertainGestureColor);
            }
            this.mResetGesture = false;
            this.mCurrentGesture = null;
            this.mPath.rewind();
        } else if ((this.mCurrentGesture == null || this.mCurrentGesture.getStrokesCount() == 0) && this.mHandleGestureActions) {
            setCurrentColor(this.mUncertainGestureColor);
        }
        if (this.mFadingHasStarted) {
            cancelClearAnimation();
        } else if (this.mIsFadingOut) {
            setPaintAlpha(255);
            this.mIsFadingOut = false;
            this.mFadingHasStarted = false;
            removeCallbacks(this.mFadingOut);
        }
        if (this.mCurrentGesture == null) {
            this.mCurrentGesture = new Gesture();
        }
        this.mStrokeBuffer.add(new GesturePoint(x, y, event.getEventTime()));
        this.mPath.moveTo(x, y);
        int border = this.mInvalidateExtraBorder;
        this.mInvalidRect.set(((int) x) - border, ((int) y) - border, ((int) x) + border, ((int) y) + border);
        this.mCurveEndX = x;
        this.mCurveEndY = y;
        ArrayList<OnGestureListener> listeners = this.mOnGestureListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onGestureStarted(this, event);
        }
    }

    private Rect touchMove(MotionEvent event) {
        Rect areaToRefresh;
        float x = event.getX();
        float y = event.getY();
        float previousX = this.mX;
        float previousY = this.mY;
        float dx = Math.abs(x - previousX);
        float dy = Math.abs(y - previousY);
        if (dx >= 3.0f || dy >= 3.0f) {
            Rect areaToRefresh2 = this.mInvalidRect;
            int border = this.mInvalidateExtraBorder;
            areaToRefresh2.set(((int) this.mCurveEndX) - border, ((int) this.mCurveEndY) - border, ((int) this.mCurveEndX) + border, ((int) this.mCurveEndY) + border);
            float cX = (x + previousX) / 2.0f;
            this.mCurveEndX = cX;
            float f = (y + previousY) / 2.0f;
            this.mCurveEndY = f;
            float cY = f;
            this.mPath.quadTo(previousX, previousY, cX, cY);
            areaToRefresh2.union(((int) previousX) - border, ((int) previousY) - border, ((int) previousX) + border, ((int) previousY) + border);
            areaToRefresh2.union(((int) cX) - border, ((int) cY) - border, ((int) cX) + border, ((int) cY) + border);
            this.mX = x;
            this.mY = y;
            this.mStrokeBuffer.add(new GesturePoint(x, y, event.getEventTime()));
            if (!this.mHandleGestureActions || this.mIsGesturing) {
                areaToRefresh = areaToRefresh2;
                float f2 = x;
            } else {
                areaToRefresh = areaToRefresh2;
                float f3 = x;
                this.mTotalLength += (float) Math.hypot((double) dx, (double) dy);
                if (this.mTotalLength > this.mGestureStrokeLengthThreshold) {
                    OrientedBoundingBox box = GestureUtils.computeOrientedBoundingBox(this.mStrokeBuffer);
                    float angle = Math.abs(box.orientation);
                    if (angle > 90.0f) {
                        angle = 180.0f - angle;
                    }
                    if (box.squareness > this.mGestureStrokeSquarenessTreshold || (this.mOrientation != 1 ? angle > this.mGestureStrokeAngleThreshold : angle < this.mGestureStrokeAngleThreshold)) {
                        this.mIsGesturing = true;
                        setCurrentColor(this.mCertainGestureColor);
                        ArrayList<OnGesturingListener> listeners = this.mOnGesturingListeners;
                        int count = listeners.size();
                        for (int i = 0; i < count; i++) {
                            listeners.get(i).onGesturingStarted(this);
                        }
                    }
                }
            }
            ArrayList<OnGestureListener> listeners2 = this.mOnGestureListeners;
            int count2 = listeners2.size();
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 < count2) {
                    listeners2.get(i3).onGesture(this, event);
                    i2 = i3 + 1;
                } else {
                    MotionEvent motionEvent = event;
                    return areaToRefresh;
                }
            }
        } else {
            MotionEvent motionEvent2 = event;
            float f4 = x;
            return null;
        }
    }

    private void touchUp(MotionEvent event, boolean cancel) {
        this.mIsListeningForGestures = false;
        if (this.mCurrentGesture != null) {
            this.mCurrentGesture.addStroke(new GestureStroke(this.mStrokeBuffer));
            if (!cancel) {
                ArrayList<OnGestureListener> listeners = this.mOnGestureListeners;
                int count = listeners.size();
                for (int i = 0; i < count; i++) {
                    listeners.get(i).onGestureEnded(this, event);
                }
                boolean z = true;
                boolean z2 = this.mHandleGestureActions != 0 && this.mFadeEnabled;
                if (!this.mHandleGestureActions || !this.mIsGesturing) {
                    z = false;
                }
                clear(z2, z, false);
            } else {
                cancelGesture(event);
            }
        } else {
            cancelGesture(event);
        }
        this.mStrokeBuffer.clear();
        this.mPreviousWasGesturing = this.mIsGesturing;
        this.mIsGesturing = false;
        ArrayList<OnGesturingListener> listeners2 = this.mOnGesturingListeners;
        int count2 = listeners2.size();
        for (int i2 = 0; i2 < count2; i2++) {
            listeners2.get(i2).onGesturingEnded(this);
        }
    }

    private void cancelGesture(MotionEvent event) {
        ArrayList<OnGestureListener> listeners = this.mOnGestureListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).onGestureCancelled(this, event);
        }
        clear(false);
    }

    /* access modifiers changed from: private */
    public void fireOnGesturePerformed() {
        ArrayList<OnGesturePerformedListener> actionListeners = this.mOnGesturePerformedListeners;
        int count = actionListeners.size();
        for (int i = 0; i < count; i++) {
            actionListeners.get(i).onGesturePerformed(this, this.mCurrentGesture);
        }
    }
}
