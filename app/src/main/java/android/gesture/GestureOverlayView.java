package android.gesture;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Process;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
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
    private Gesture mCurrentGesture;
    private float mCurveEndX;
    private float mCurveEndY;
    private long mFadeDuration;
    private boolean mFadeEnabled;
    private long mFadeOffset;
    private float mFadingAlpha;
    private boolean mFadingHasStarted;
    private final FadeOutRunnable mFadingOut;
    private long mFadingStart;
    private final Paint mGesturePaint;
    private float mGestureStrokeAngleThreshold;
    private float mGestureStrokeLengthThreshold;
    private float mGestureStrokeSquarenessTreshold;
    private int mGestureStrokeType;
    private float mGestureStrokeWidth;
    private boolean mGestureVisible;
    private boolean mHandleGestureActions;
    private boolean mInterceptEvents;
    private final AccelerateDecelerateInterpolator mInterpolator;
    private final Rect mInvalidRect;
    private int mInvalidateExtraBorder;
    private boolean mIsFadingOut;
    private boolean mIsGesturing;
    private boolean mIsListeningForGestures;
    private final ArrayList<OnGestureListener> mOnGestureListeners;
    private final ArrayList<OnGesturePerformedListener> mOnGesturePerformedListeners;
    private final ArrayList<OnGesturingListener> mOnGesturingListeners;
    private int mOrientation;
    private final Path mPath;
    private boolean mPreviousWasGesturing;
    private boolean mResetGesture;
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
                    GestureOverlayView.this.mPreviousWasGesturing = false;
                    GestureOverlayView.this.mIsFadingOut = false;
                    GestureOverlayView.this.mFadingHasStarted = false;
                    GestureOverlayView.this.mPath.rewind();
                    GestureOverlayView.this.mCurrentGesture = null;
                    GestureOverlayView.this.setPaintAlpha(Process.PROC_TERM_MASK);
                } else {
                    GestureOverlayView.this.mFadingHasStarted = GestureOverlayView.GESTURE_RENDERING_ANTIALIAS;
                    GestureOverlayView.this.mFadingAlpha = Engine.DEFAULT_VOLUME - GestureOverlayView.this.mInterpolator.getInterpolation(Math.max(0.0f, Math.min(Engine.DEFAULT_VOLUME, ((float) duration) / ((float) GestureOverlayView.this.mFadeDuration))));
                    GestureOverlayView.this.setPaintAlpha((int) (GestureOverlayView.this.mFadingAlpha * 255.0f));
                    GestureOverlayView.this.postDelayed(this, 16);
                }
            } else if (this.resetMultipleStrokes) {
                GestureOverlayView.this.mResetGesture = GestureOverlayView.GESTURE_RENDERING_ANTIALIAS;
            } else {
                GestureOverlayView.this.fireOnGesturePerformed();
                GestureOverlayView.this.mFadingHasStarted = false;
                GestureOverlayView.this.mPath.rewind();
                GestureOverlayView.this.mCurrentGesture = null;
                GestureOverlayView.this.mPreviousWasGesturing = false;
                GestureOverlayView.this.setPaintAlpha(Process.PROC_TERM_MASK);
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
        this.mFadeEnabled = GESTURE_RENDERING_ANTIALIAS;
        this.mCertainGestureColor = Color.YELLOW;
        this.mUncertainGestureColor = 1224736512;
        this.mGestureStrokeWidth = 12.0f;
        this.mInvalidateExtraBorder = 10;
        this.mGestureStrokeType = ORIENTATION_HORIZONTAL;
        this.mGestureStrokeLengthThreshold = 50.0f;
        this.mGestureStrokeSquarenessTreshold = 0.275f;
        this.mGestureStrokeAngleThreshold = 40.0f;
        this.mOrientation = ORIENTATION_VERTICAL;
        this.mInvalidRect = new Rect();
        this.mPath = new Path();
        this.mGestureVisible = GESTURE_RENDERING_ANTIALIAS;
        this.mIsGesturing = false;
        this.mPreviousWasGesturing = false;
        this.mInterceptEvents = GESTURE_RENDERING_ANTIALIAS;
        this.mStrokeBuffer = new ArrayList(100);
        this.mOnGestureListeners = new ArrayList();
        this.mOnGesturePerformedListeners = new ArrayList();
        this.mOnGesturingListeners = new ArrayList();
        this.mIsFadingOut = false;
        this.mFadingAlpha = Engine.DEFAULT_VOLUME;
        this.mInterpolator = new AccelerateDecelerateInterpolator();
        this.mFadingOut = new FadeOutRunnable();
        init();
    }

    public GestureOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 18219034);
    }

    public GestureOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, ORIENTATION_HORIZONTAL);
    }

    public GestureOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mGesturePaint = new Paint();
        this.mFadeDuration = 150;
        this.mFadeOffset = 420;
        this.mFadeEnabled = GESTURE_RENDERING_ANTIALIAS;
        this.mCertainGestureColor = Color.YELLOW;
        this.mUncertainGestureColor = 1224736512;
        this.mGestureStrokeWidth = 12.0f;
        this.mInvalidateExtraBorder = 10;
        this.mGestureStrokeType = ORIENTATION_HORIZONTAL;
        this.mGestureStrokeLengthThreshold = 50.0f;
        this.mGestureStrokeSquarenessTreshold = 0.275f;
        this.mGestureStrokeAngleThreshold = 40.0f;
        this.mOrientation = ORIENTATION_VERTICAL;
        this.mInvalidRect = new Rect();
        this.mPath = new Path();
        this.mGestureVisible = GESTURE_RENDERING_ANTIALIAS;
        this.mIsGesturing = false;
        this.mPreviousWasGesturing = false;
        this.mInterceptEvents = GESTURE_RENDERING_ANTIALIAS;
        this.mStrokeBuffer = new ArrayList(100);
        this.mOnGestureListeners = new ArrayList();
        this.mOnGesturePerformedListeners = new ArrayList();
        this.mOnGesturingListeners = new ArrayList();
        this.mIsFadingOut = false;
        this.mFadingAlpha = Engine.DEFAULT_VOLUME;
        this.mInterpolator = new AccelerateDecelerateInterpolator();
        this.mFadingOut = new FadeOutRunnable();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GestureOverlayView, defStyleAttr, defStyleRes);
        this.mGestureStrokeWidth = a.getFloat(ORIENTATION_VERTICAL, this.mGestureStrokeWidth);
        this.mInvalidateExtraBorder = Math.max(ORIENTATION_VERTICAL, ((int) this.mGestureStrokeWidth) - 1);
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
        this.mOrientation = a.getInt(ORIENTATION_HORIZONTAL, this.mOrientation);
        a.recycle();
        init();
    }

    private void init() {
        setWillNotDraw(false);
        Paint gesturePaint = this.mGesturePaint;
        gesturePaint.setAntiAlias(GESTURE_RENDERING_ANTIALIAS);
        gesturePaint.setColor(this.mCertainGestureColor);
        gesturePaint.setStyle(Style.STROKE);
        gesturePaint.setStrokeJoin(Join.ROUND);
        gesturePaint.setStrokeCap(Cap.ROUND);
        gesturePaint.setStrokeWidth(this.mGestureStrokeWidth);
        gesturePaint.setDither(GESTURE_RENDERING_ANTIALIAS);
        this.mCurrentColor = this.mCertainGestureColor;
        setPaintAlpha(Process.PROC_TERM_MASK);
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
        this.mInvalidateExtraBorder = Math.max(ORIENTATION_VERTICAL, ((int) gestureStrokeWidth) - 1);
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
        path.computeBounds(bounds, GESTURE_RENDERING_ANTIALIAS);
        this.mPath.rewind();
        this.mPath.addPath(path, (-bounds.left) + ((((float) getWidth()) - bounds.width()) / 2.0f), (-bounds.top) + ((((float) getHeight()) - bounds.height()) / 2.0f));
        this.mResetGesture = GESTURE_RENDERING_ANTIALIAS;
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
            this.mHandleGestureActions = GESTURE_RENDERING_ANTIALIAS;
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
            setPaintAlpha((int) (this.mFadingAlpha * 255.0f));
        } else {
            setPaintAlpha(Process.PROC_TERM_MASK);
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

    private void setPaintAlpha(int alpha) {
        this.mGesturePaint.setColor(((this.mCurrentColor << 8) >>> 8) | ((((this.mCurrentColor >>> 24) * (alpha + (alpha >> 7))) >> 8) << 24));
    }

    public void clear(boolean animated) {
        clear(animated, false, GESTURE_RENDERING_ANTIALIAS);
    }

    private void clear(boolean animated, boolean fireActionPerformed, boolean immediate) {
        setPaintAlpha(Process.PROC_TERM_MASK);
        removeCallbacks(this.mFadingOut);
        this.mResetGesture = false;
        this.mFadingOut.fireActionPerformed = fireActionPerformed;
        this.mFadingOut.resetMultipleStrokes = false;
        if (!animated || this.mCurrentGesture == null) {
            this.mFadingAlpha = Engine.DEFAULT_VOLUME;
            this.mIsFadingOut = false;
            this.mFadingHasStarted = false;
            if (immediate) {
                this.mCurrentGesture = null;
                this.mPath.rewind();
                invalidate();
                return;
            } else if (fireActionPerformed) {
                postDelayed(this.mFadingOut, this.mFadeOffset);
                return;
            } else if (this.mGestureStrokeType == ORIENTATION_VERTICAL) {
                this.mFadingOut.resetMultipleStrokes = GESTURE_RENDERING_ANTIALIAS;
                postDelayed(this.mFadingOut, this.mFadeOffset);
                return;
            } else {
                this.mCurrentGesture = null;
                this.mPath.rewind();
                invalidate();
                return;
            }
        }
        this.mFadingAlpha = Engine.DEFAULT_VOLUME;
        this.mIsFadingOut = GESTURE_RENDERING_ANTIALIAS;
        this.mFadingHasStarted = false;
        this.mFadingStart = AnimationUtils.currentAnimationTimeMillis() + this.mFadeOffset;
        postDelayed(this.mFadingOut, this.mFadeOffset);
    }

    public void cancelClearAnimation() {
        setPaintAlpha(Process.PROC_TERM_MASK);
        this.mIsFadingOut = false;
        this.mFadingHasStarted = false;
        removeCallbacks(this.mFadingOut);
        this.mPath.rewind();
        this.mCurrentGesture = null;
    }

    public void cancelGesture() {
        int i;
        this.mIsListeningForGestures = false;
        this.mCurrentGesture.addStroke(new GestureStroke(this.mStrokeBuffer));
        long now = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, ORIENTATION_HORIZONTAL);
        ArrayList<OnGestureListener> listeners = this.mOnGestureListeners;
        int count = listeners.size();
        for (i = ORIENTATION_HORIZONTAL; i < count; i += ORIENTATION_VERTICAL) {
            ((OnGestureListener) listeners.get(i)).onGestureCancelled(this, event);
        }
        event.recycle();
        clear(false);
        this.mIsGesturing = false;
        this.mPreviousWasGesturing = false;
        this.mStrokeBuffer.clear();
        ArrayList<OnGesturingListener> otherListeners = this.mOnGesturingListeners;
        count = otherListeners.size();
        for (i = ORIENTATION_HORIZONTAL; i < count; i += ORIENTATION_VERTICAL) {
            ((OnGesturingListener) otherListeners.get(i)).onGesturingEnded(this);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelClearAnimation();
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return super.dispatchTouchEvent(event);
        }
        boolean z;
        if (this.mIsGesturing || (this.mCurrentGesture != null && this.mCurrentGesture.getStrokesCount() > 0 && this.mPreviousWasGesturing)) {
            z = this.mInterceptEvents;
        } else {
            z = false;
        }
        processEvent(event);
        if (z) {
            event.setAction(3);
        }
        super.dispatchTouchEvent(event);
        return GESTURE_RENDERING_ANTIALIAS;
    }

    private boolean processEvent(MotionEvent event) {
        switch (event.getAction()) {
            case ORIENTATION_HORIZONTAL /*0*/:
                touchDown(event);
                invalidate();
                return GESTURE_RENDERING_ANTIALIAS;
            case ORIENTATION_VERTICAL /*1*/:
                if (this.mIsListeningForGestures) {
                    touchUp(event, false);
                    invalidate();
                    return GESTURE_RENDERING_ANTIALIAS;
                }
                break;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                if (this.mIsListeningForGestures) {
                    Rect rect = touchMove(event);
                    if (rect != null) {
                        invalidate(rect);
                    }
                    return GESTURE_RENDERING_ANTIALIAS;
                }
                break;
            case Engine.DEFAULT_STREAM /*3*/:
                if (this.mIsListeningForGestures) {
                    touchUp(event, GESTURE_RENDERING_ANTIALIAS);
                    invalidate();
                    return GESTURE_RENDERING_ANTIALIAS;
                }
                break;
        }
        return false;
    }

    private void touchDown(MotionEvent event) {
        this.mIsListeningForGestures = GESTURE_RENDERING_ANTIALIAS;
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
            setPaintAlpha(Process.PROC_TERM_MASK);
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
        for (int i = ORIENTATION_HORIZONTAL; i < count; i += ORIENTATION_VERTICAL) {
            ((OnGestureListener) listeners.get(i)).onGestureStarted(this, event);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Rect touchMove(MotionEvent event) {
        Rect areaToRefresh = null;
        float x = event.getX();
        float y = event.getY();
        float previousX = this.mX;
        float previousY = this.mY;
        float dx = Math.abs(x - previousX);
        float dy = Math.abs(y - previousY);
        if (dx >= 3.0f || dy >= 3.0f) {
            int count;
            int i;
            areaToRefresh = this.mInvalidRect;
            int border = this.mInvalidateExtraBorder;
            areaToRefresh.set(((int) this.mCurveEndX) - border, ((int) this.mCurveEndY) - border, ((int) this.mCurveEndX) + border, ((int) this.mCurveEndY) + border);
            float cX = (x + previousX) / 2.0f;
            this.mCurveEndX = cX;
            float cY = (y + previousY) / 2.0f;
            this.mCurveEndY = cY;
            this.mPath.quadTo(previousX, previousY, cX, cY);
            areaToRefresh.union(((int) previousX) - border, ((int) previousY) - border, ((int) previousX) + border, ((int) previousY) + border);
            areaToRefresh.union(((int) cX) - border, ((int) cY) - border, ((int) cX) + border, ((int) cY) + border);
            this.mX = x;
            this.mY = y;
            this.mStrokeBuffer.add(new GesturePoint(x, y, event.getEventTime()));
            if (this.mHandleGestureActions && !this.mIsGesturing) {
                this.mTotalLength += (float) Math.hypot((double) dx, (double) dy);
                if (this.mTotalLength > this.mGestureStrokeLengthThreshold) {
                    OrientedBoundingBox box = GestureUtils.computeOrientedBoundingBox(this.mStrokeBuffer);
                    float angle = Math.abs(box.orientation);
                    if (angle > 90.0f) {
                        angle = 180.0f - angle;
                    }
                    if (box.squareness <= this.mGestureStrokeSquarenessTreshold) {
                        int i2 = this.mOrientation;
                        if (r0 == ORIENTATION_VERTICAL) {
                        } else {
                            if (angle > this.mGestureStrokeAngleThreshold) {
                            }
                        }
                    }
                    this.mIsGesturing = GESTURE_RENDERING_ANTIALIAS;
                    setCurrentColor(this.mCertainGestureColor);
                    ArrayList<OnGesturingListener> listeners = this.mOnGesturingListeners;
                    count = listeners.size();
                    for (i = ORIENTATION_HORIZONTAL; i < count; i += ORIENTATION_VERTICAL) {
                        ((OnGesturingListener) listeners.get(i)).onGesturingStarted(this);
                    }
                }
            }
            ArrayList<OnGestureListener> listeners2 = this.mOnGestureListeners;
            count = listeners2.size();
            for (i = ORIENTATION_HORIZONTAL; i < count; i += ORIENTATION_VERTICAL) {
                ((OnGestureListener) listeners2.get(i)).onGesture(this, event);
            }
        }
        return areaToRefresh;
    }

    private void touchUp(MotionEvent event, boolean cancel) {
        int count;
        int i;
        this.mIsListeningForGestures = false;
        if (this.mCurrentGesture != null) {
            this.mCurrentGesture.addStroke(new GestureStroke(this.mStrokeBuffer));
            if (cancel) {
                cancelGesture(event);
            } else {
                boolean z;
                ArrayList<OnGestureListener> listeners = this.mOnGestureListeners;
                count = listeners.size();
                for (i = ORIENTATION_HORIZONTAL; i < count; i += ORIENTATION_VERTICAL) {
                    ((OnGestureListener) listeners.get(i)).onGestureEnded(this, event);
                }
                if (this.mHandleGestureActions) {
                    z = this.mFadeEnabled;
                } else {
                    z = false;
                }
                clear(z, this.mHandleGestureActions ? this.mIsGesturing : false, false);
            }
        } else {
            cancelGesture(event);
        }
        this.mStrokeBuffer.clear();
        this.mPreviousWasGesturing = this.mIsGesturing;
        this.mIsGesturing = false;
        ArrayList<OnGesturingListener> listeners2 = this.mOnGesturingListeners;
        count = listeners2.size();
        for (i = ORIENTATION_HORIZONTAL; i < count; i += ORIENTATION_VERTICAL) {
            ((OnGesturingListener) listeners2.get(i)).onGesturingEnded(this);
        }
    }

    private void cancelGesture(MotionEvent event) {
        ArrayList<OnGestureListener> listeners = this.mOnGestureListeners;
        int count = listeners.size();
        for (int i = ORIENTATION_HORIZONTAL; i < count; i += ORIENTATION_VERTICAL) {
            ((OnGestureListener) listeners.get(i)).onGestureCancelled(this, event);
        }
        clear(false);
    }

    private void fireOnGesturePerformed() {
        ArrayList<OnGesturePerformedListener> actionListeners = this.mOnGesturePerformedListeners;
        int count = actionListeners.size();
        for (int i = ORIENTATION_HORIZONTAL; i < count; i += ORIENTATION_VERTICAL) {
            ((OnGesturePerformedListener) actionListeners.get(i)).onGesturePerformed(this, this.mCurrentGesture);
        }
    }
}
