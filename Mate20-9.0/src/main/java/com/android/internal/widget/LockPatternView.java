package com.android.internal.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.IntArray;
import android.util.SparseArray;
import android.view.DisplayListCanvas;
import android.view.MotionEvent;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.internal.R;
import com.android.internal.os.PowerProfile;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class LockPatternView extends View {
    private static final long ANIM_DURATION = 100;
    private static final int ASPECT_LOCK_HEIGHT = 2;
    private static final int ASPECT_LOCK_WIDTH = 1;
    private static final int ASPECT_SQUARE = 0;
    public static final boolean DEBUG_A11Y = false;
    private static final float DRAG_THRESHHOLD = 0.0f;
    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;
    private static final boolean PROFILE_DRAWING = false;
    private static final String TAG = "LockPatternView";
    public static final int VIRTUAL_BASE_VIEW_ID = 1;
    private int mAlphaTransparent;
    private long mAnimatingPeriodStart;
    private int mAspect;
    private AudioManager mAudioManager;
    /* access modifiers changed from: private */
    public final CellState[][] mCellStates;
    private final Path mCurrentPath;
    private final int mDotCircleRadius;
    /* access modifiers changed from: private */
    public final int mDotRadius;
    /* access modifiers changed from: private */
    public final int mDotRadiusActivated;
    private boolean mDrawingProfilingStarted;
    private boolean mEnableHapticFeedback;
    private int mErrorColor;
    private PatternExploreByTouchHelper mExploreByTouchHelper;
    private boolean mFadePattern;
    /* access modifiers changed from: private */
    public final Interpolator mFastOutSlowInInterpolator;
    private float mHeight;
    /* access modifiers changed from: private */
    public float mHitFactor;
    private float mInProgressX;
    private float mInProgressY;
    private boolean mInStealthMode;
    private boolean mInputEnabled;
    private final Interpolator mInterpolator;
    private final Rect mInvalidate;
    private boolean mIsHwTheme;
    private boolean mIsInKeyguard;
    /* access modifiers changed from: private */
    public float mLastCellCenterX;
    /* access modifiers changed from: private */
    public float mLastCellCenterY;
    private long[] mLineFadeStart;
    private final int mLineRadius;
    private final Interpolator mLinearOutSlowInInterpolator;
    private Drawable mNotSelectedDrawable;
    private OnPatternListener mOnPatternListener;
    private final Paint mPaint;
    private int mPathColor;
    private final Paint mPathPaint;
    private final ArrayList<Cell> mPattern;
    private DisplayMode mPatternDisplayMode;
    /* access modifiers changed from: private */
    public final boolean[][] mPatternDrawLookup;
    /* access modifiers changed from: private */
    public boolean mPatternInProgress;
    private int mPickedColor;
    private int mRegularColor;
    private Drawable mSelectedDrawable;
    /* access modifiers changed from: private */
    public float mSquareHeight;
    /* access modifiers changed from: private */
    public float mSquareWidth;
    private int mSuccessColor;
    private final Rect mTmpInvalidateRect;
    private boolean mUseLockPatternDrawable;
    private float mWidth;

    public static final class Cell {
        private static final Cell[][] sCells = createCells();
        final int column;
        final int row;

        private static Cell[][] createCells() {
            Cell[][] res = (Cell[][]) Array.newInstance(Cell.class, new int[]{3, 3});
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    res[i][j] = new Cell(i, j);
                }
            }
            return res;
        }

        private Cell(int row2, int column2) {
            checkRange(row2, column2);
            this.row = row2;
            this.column = column2;
        }

        public int getRow() {
            return this.row;
        }

        public int getColumn() {
            return this.column;
        }

        public static Cell of(int row2, int column2) {
            checkRange(row2, column2);
            return sCells[row2][column2];
        }

        private static void checkRange(int row2, int column2) {
            if (row2 < 0 || row2 > 2) {
                throw new IllegalArgumentException("row must be in range 0-2");
            } else if (column2 < 0 || column2 > 2) {
                throw new IllegalArgumentException("column must be in range 0-2");
            }
        }

        public String toString() {
            return "(row=" + this.row + ",clmn=" + this.column + ")";
        }
    }

    public static class CellState {
        float alpha = 1.0f;
        int col;
        boolean hwAnimating;
        CanvasProperty<Float> hwCenterX;
        CanvasProperty<Float> hwCenterY;
        CanvasProperty<Paint> hwPaint;
        CanvasProperty<Float> hwRadius;
        public ValueAnimator lineAnimator;
        public float lineEndX = Float.MIN_VALUE;
        public float lineEndY = Float.MIN_VALUE;
        public ValueAnimator moveAnimator;
        float radius;
        int row;
        float translationY;
    }

    public enum DisplayMode {
        Correct,
        Animate,
        Wrong
    }

    public interface OnPatternListener {
        void onPatternCellAdded(List<Cell> list);

        void onPatternCleared();

        void onPatternDetected(List<Cell> list);

        void onPatternStart();
    }

    private final class PatternExploreByTouchHelper extends ExploreByTouchHelper {
        private final SparseArray<VirtualViewContainer> mItems = new SparseArray<>();
        private Rect mTempRect = new Rect();

        class VirtualViewContainer {
            CharSequence description;

            public VirtualViewContainer(CharSequence description2) {
                this.description = description2;
            }
        }

        public PatternExploreByTouchHelper(View forView) {
            super(forView);
            for (int i = 1; i < 10; i++) {
                this.mItems.put(i, new VirtualViewContainer(getTextForVirtualView(i)));
            }
        }

        /* access modifiers changed from: protected */
        public int getVirtualViewAt(float x, float y) {
            return getVirtualViewIdForHit(x, y);
        }

        /* access modifiers changed from: protected */
        public void getVisibleVirtualViews(IntArray virtualViewIds) {
            if (LockPatternView.this.mPatternInProgress) {
                for (int i = 1; i < 10; i++) {
                    virtualViewIds.add(i);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            VirtualViewContainer container = this.mItems.get(virtualViewId);
            if (container != null) {
                event.getText().add(container.description);
            }
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);
            if (!LockPatternView.this.mPatternInProgress) {
                event.setContentDescription(LockPatternView.this.getContext().getText(17040344));
            }
        }

        /* access modifiers changed from: protected */
        public void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            node.setText(getTextForVirtualView(virtualViewId));
            node.setContentDescription(getTextForVirtualView(virtualViewId));
            if (LockPatternView.this.mPatternInProgress) {
                node.setFocusable(true);
                if (isClickable(virtualViewId)) {
                    node.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
                    node.setClickable(isClickable(virtualViewId));
                }
            }
            node.setBoundsInParent(getBoundsForVirtualView(virtualViewId));
        }

        private boolean isClickable(int virtualViewId) {
            if (virtualViewId == Integer.MIN_VALUE) {
                return false;
            }
            return !LockPatternView.this.mPatternDrawLookup[(virtualViewId - 1) / 3][(virtualViewId - 1) % 3];
        }

        /* access modifiers changed from: protected */
        public boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            if (action != 16) {
                return false;
            }
            return onItemClicked(virtualViewId);
        }

        /* access modifiers changed from: package-private */
        public boolean onItemClicked(int index) {
            invalidateVirtualView(index);
            sendEventForVirtualView(index, 1);
            return true;
        }

        private Rect getBoundsForVirtualView(int virtualViewId) {
            int ordinal = virtualViewId - 1;
            Rect bounds = this.mTempRect;
            int row = ordinal / 3;
            int col = ordinal % 3;
            CellState cellState = LockPatternView.this.mCellStates[row][col];
            float centerX = LockPatternView.this.getCenterXForColumn(col);
            float centerY = LockPatternView.this.getCenterYForRow(row);
            float cellheight = LockPatternView.this.mSquareHeight * LockPatternView.this.mHitFactor * 0.5f;
            float cellwidth = LockPatternView.this.mSquareWidth * LockPatternView.this.mHitFactor * 0.5f;
            bounds.left = (int) (centerX - cellwidth);
            bounds.right = (int) (centerX + cellwidth);
            bounds.top = (int) (centerY - cellheight);
            bounds.bottom = (int) (centerY + cellheight);
            return bounds;
        }

        private CharSequence getTextForVirtualView(int virtualViewId) {
            return LockPatternView.this.getResources().getString(17040346, new Object[]{Integer.valueOf(virtualViewId)});
        }

        private int getVirtualViewIdForHit(float x, float y) {
            int rowHit = LockPatternView.this.getRowHit(y);
            int view = Integer.MIN_VALUE;
            if (rowHit < 0) {
                return Integer.MIN_VALUE;
            }
            int columnHit = LockPatternView.this.getColumnHit(x);
            if (columnHit < 0) {
                return Integer.MIN_VALUE;
            }
            int dotId = (rowHit * 3) + columnHit + 1;
            if (LockPatternView.this.mPatternDrawLookup[rowHit][columnHit]) {
                view = dotId;
            }
            return view;
        }
    }

    private static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final int mDisplayMode;
        private final boolean mInStealthMode;
        private final boolean mInputEnabled;
        private final String mSerializedPattern;
        private final boolean mTactileFeedbackEnabled;

        private SavedState(Parcelable superState, String serializedPattern, int displayMode, boolean inputEnabled, boolean inStealthMode, boolean tactileFeedbackEnabled) {
            super(superState);
            this.mSerializedPattern = serializedPattern;
            this.mDisplayMode = displayMode;
            this.mInputEnabled = inputEnabled;
            this.mInStealthMode = inStealthMode;
            this.mTactileFeedbackEnabled = tactileFeedbackEnabled;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mSerializedPattern = in.readString();
            this.mDisplayMode = in.readInt();
            this.mInputEnabled = ((Boolean) in.readValue(null)).booleanValue();
            this.mInStealthMode = ((Boolean) in.readValue(null)).booleanValue();
            this.mTactileFeedbackEnabled = ((Boolean) in.readValue(null)).booleanValue();
        }

        public String getSerializedPattern() {
            return this.mSerializedPattern;
        }

        public int getDisplayMode() {
            return this.mDisplayMode;
        }

        public boolean isInputEnabled() {
            return this.mInputEnabled;
        }

        public boolean isInStealthMode() {
            return this.mInStealthMode;
        }

        public boolean isTactileFeedbackEnabled() {
            return this.mTactileFeedbackEnabled;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.mSerializedPattern);
            dest.writeInt(this.mDisplayMode);
            dest.writeValue(Boolean.valueOf(this.mInputEnabled));
            dest.writeValue(Boolean.valueOf(this.mInStealthMode));
            dest.writeValue(Boolean.valueOf(this.mTactileFeedbackEnabled));
        }
    }

    public LockPatternView(Context context) {
        this(context, null);
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDrawingProfilingStarted = false;
        this.mPaint = new Paint();
        this.mPathPaint = new Paint();
        this.mPattern = new ArrayList<>(9);
        this.mPatternDrawLookup = (boolean[][]) Array.newInstance(boolean.class, new int[]{3, 3});
        this.mInProgressX = -1.0f;
        this.mInProgressY = -1.0f;
        this.mLineFadeStart = new long[9];
        this.mPatternDisplayMode = DisplayMode.Correct;
        this.mInputEnabled = true;
        this.mInStealthMode = false;
        this.mEnableHapticFeedback = true;
        this.mPatternInProgress = false;
        this.mFadePattern = true;
        this.mHitFactor = 0.6f;
        this.mCurrentPath = new Path();
        this.mInvalidate = new Rect();
        this.mTmpInvalidateRect = new Rect();
        this.mInterpolator = new AccelerateInterpolator();
        this.mIsHwTheme = false;
        this.mLastCellCenterX = -1.0f;
        this.mLastCellCenterY = -1.0f;
        this.mAlphaTransparent = 128;
        this.mPathColor = -3355444;
        this.mIsInKeyguard = false;
        this.mPickedColor = 0;
        this.mIsHwTheme = HwWidgetFactory.checkIsHwTheme(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LockPatternView, 17891431, 16974999);
        String aspect = a.getString(0);
        if ("square".equals(aspect)) {
            this.mAspect = 0;
        } else if ("lock_width".equals(aspect)) {
            this.mAspect = 1;
        } else if ("lock_height".equals(aspect)) {
            this.mAspect = 2;
        } else {
            this.mAspect = 0;
        }
        setClickable(true);
        this.mPathPaint.setAntiAlias(true);
        this.mPathPaint.setDither(true);
        if (!this.mIsInKeyguard || this.mPickedColor == 0) {
            this.mRegularColor = context.getColor(33882269);
            this.mSuccessColor = context.getColor(33882270);
            this.mRegularColor = a.getColor(3, this.mRegularColor);
            this.mSuccessColor = a.getColor(4, this.mSuccessColor);
        } else {
            this.mRegularColor = this.mPickedColor;
            this.mSuccessColor = this.mPickedColor;
        }
        this.mErrorColor = a.getColor(1, 0);
        this.mPathPaint.setColor(a.getColor(2, this.mRegularColor));
        this.mPathPaint.setStyle(Paint.Style.FILL);
        this.mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mPathPaint.setStrokeWidth(2.0f);
        this.mLineRadius = getResources().getDimensionPixelSize(34472102);
        this.mDotCircleRadius = getResources().getDimensionPixelSize(34472101);
        this.mDotRadius = getResources().getDimensionPixelSize(34472099);
        this.mDotRadiusActivated = getResources().getDimensionPixelSize(34472100);
        this.mUseLockPatternDrawable = getResources().getBoolean(17957114);
        if (this.mUseLockPatternDrawable) {
            this.mSelectedDrawable = getResources().getDrawable(17302944);
            this.mNotSelectedDrawable = getResources().getDrawable(17302942);
        }
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mCellStates = (CellState[][]) Array.newInstance(CellState.class, new int[]{3, 3});
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.mCellStates[i][j] = new CellState();
                this.mCellStates[i][j].radius = (float) this.mDotRadius;
                this.mCellStates[i][j].row = i;
                this.mCellStates[i][j].col = j;
            }
        }
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mExploreByTouchHelper = new PatternExploreByTouchHelper(this);
        setAccessibilityDelegate(this.mExploreByTouchHelper);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService(PowerProfile.POWER_AUDIO);
        a.recycle();
    }

    public CellState[][] getCellStates() {
        return this.mCellStates;
    }

    public boolean isInStealthMode() {
        return this.mInStealthMode;
    }

    public boolean isTactileFeedbackEnabled() {
        return this.mEnableHapticFeedback;
    }

    public void setInStealthMode(boolean inStealthMode) {
        this.mInStealthMode = inStealthMode;
    }

    public void setFadePattern(boolean fadePattern) {
        this.mFadePattern = fadePattern;
    }

    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
        this.mEnableHapticFeedback = tactileFeedbackEnabled;
    }

    public void setOnPatternListener(OnPatternListener onPatternListener) {
        this.mOnPatternListener = onPatternListener;
    }

    public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
        this.mPattern.clear();
        this.mPattern.addAll(pattern);
        clearPatternDrawLookup();
        for (Cell cell : pattern) {
            this.mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        }
        setDisplayMode(displayMode);
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (this.mPattern.size() != 0) {
                this.mAnimatingPeriodStart = SystemClock.elapsedRealtime();
                Cell first = this.mPattern.get(0);
                this.mInProgressX = getCenterXForColumn(first.getColumn());
                this.mInProgressY = getCenterYForRow(first.getRow());
                clearPatternDrawLookup();
            } else {
                throw new IllegalStateException("you must have a pattern to animate if you want to set the display mode to animate");
            }
        }
        invalidate();
    }

    public void startCellStateAnimation(CellState cellState, float startAlpha, float endAlpha, float startTranslationY, float endTranslationY, float startScale, float endScale, long delay, long duration, Interpolator interpolator, Runnable finishRunnable) {
        if (isHardwareAccelerated()) {
            startCellStateAnimationHw(cellState, startAlpha, endAlpha, startTranslationY, endTranslationY, startScale, endScale, delay, duration, interpolator, finishRunnable);
        } else {
            startCellStateAnimationSw(cellState, startAlpha, endAlpha, startTranslationY, endTranslationY, startScale, endScale, delay, duration, interpolator, finishRunnable);
        }
    }

    private void startCellStateAnimationSw(CellState cellState, float startAlpha, float endAlpha, float startTranslationY, float endTranslationY, float startScale, float endScale, long delay, long duration, Interpolator interpolator, Runnable finishRunnable) {
        CellState cellState2 = cellState;
        float f = startAlpha;
        cellState2.alpha = f;
        float f2 = startTranslationY;
        cellState2.translationY = f2;
        cellState2.radius = ((float) this.mDotRadius) * startScale;
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.setInterpolator(interpolator);
        final CellState cellState3 = cellState2;
        final float f3 = f;
        AnonymousClass1 r10 = r0;
        final float f4 = endAlpha;
        final float f5 = f2;
        final float f6 = endTranslationY;
        final float f7 = startScale;
        final float f8 = endScale;
        AnonymousClass1 r0 = new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = ((Float) animation.getAnimatedValue()).floatValue();
                cellState3.alpha = ((1.0f - t) * f3) + (f4 * t);
                cellState3.translationY = ((1.0f - t) * f5) + (f6 * t);
                cellState3.radius = ((float) LockPatternView.this.mDotRadius) * (((1.0f - t) * f7) + (f8 * t));
                LockPatternView.this.invalidate();
            }
        };
        animator.addUpdateListener(r10);
        final Runnable runnable = finishRunnable;
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        animator.start();
    }

    private void startCellStateAnimationHw(CellState cellState, float startAlpha, float endAlpha, float startTranslationY, float endTranslationY, float startScale, float endScale, long delay, long duration, Interpolator interpolator, Runnable finishRunnable) {
        final CellState cellState2 = cellState;
        float f = endTranslationY;
        float f2 = endAlpha;
        cellState2.alpha = f2;
        cellState2.translationY = f;
        cellState2.radius = ((float) this.mDotRadius) * endScale;
        cellState2.hwAnimating = true;
        cellState2.hwCenterY = CanvasProperty.createFloat(getCenterYForRow(cellState2.row) + startTranslationY);
        cellState2.hwCenterX = CanvasProperty.createFloat(getCenterXForColumn(cellState2.col));
        cellState2.hwRadius = CanvasProperty.createFloat(((float) this.mDotRadius) * startScale);
        this.mPaint.setColor(getCurrentColor(false));
        this.mPaint.setAlpha((int) (255.0f * startAlpha));
        cellState2.hwPaint = CanvasProperty.createPaint(new Paint(this.mPaint));
        long j = delay;
        long j2 = duration;
        Interpolator interpolator2 = interpolator;
        startRtFloatAnimation(cellState2.hwCenterY, getCenterYForRow(cellState2.row) + f, j, j2, interpolator2);
        startRtFloatAnimation(cellState2.hwRadius, ((float) this.mDotRadius) * endScale, j, j2, interpolator2);
        final Runnable runnable = finishRunnable;
        startRtAlphaAnimation(cellState2, f2, j, j2, interpolator, new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                cellState2.hwAnimating = false;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        invalidate();
    }

    private void startRtAlphaAnimation(CellState cellState, float endAlpha, long delay, long duration, Interpolator interpolator, Animator.AnimatorListener listener) {
        RenderNodeAnimator animator = new RenderNodeAnimator(cellState.hwPaint, 1, (float) ((int) (255.0f * endAlpha)));
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.setInterpolator(interpolator);
        animator.setTarget(this);
        animator.addListener(listener);
        animator.start();
    }

    private void startRtFloatAnimation(CanvasProperty<Float> property, float endValue, long delay, long duration, Interpolator interpolator) {
        RenderNodeAnimator animator = new RenderNodeAnimator(property, endValue);
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.setInterpolator(interpolator);
        animator.setTarget(this);
        animator.start();
    }

    private void notifyCellAdded() {
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternCellAdded(this.mPattern);
        }
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void notifyPatternStarted() {
        sendAccessEvent(17040349);
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternStart();
        }
    }

    private void notifyPatternDetected() {
        sendAccessEvent(17040348);
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternDetected(this.mPattern);
        }
    }

    private void notifyPatternCleared() {
        sendAccessEvent(17040347);
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternCleared();
        }
    }

    public void clearPattern() {
        resetPattern();
    }

    /* access modifiers changed from: protected */
    public boolean dispatchHoverEvent(MotionEvent event) {
        return super.dispatchHoverEvent(event) | this.mExploreByTouchHelper.dispatchHoverEvent(event);
    }

    private void resetPattern() {
        this.mPattern.clear();
        clearPatternDrawLookup();
        resetCellRadius();
        this.mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }

    private void clearPatternDrawLookup() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.mPatternDrawLookup[i][j] = false;
                this.mLineFadeStart[i + j] = 0;
            }
        }
    }

    public void disableInput() {
        this.mInputEnabled = false;
    }

    public void enableInput() {
        this.mInputEnabled = true;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        int width = (w - this.mPaddingLeft) - this.mPaddingRight;
        this.mSquareWidth = ((float) width) / 3.0f;
        int height = (h - this.mPaddingTop) - this.mPaddingBottom;
        this.mSquareHeight = ((float) height) / 3.0f;
        this.mExploreByTouchHelper.invalidateRoot();
        if (this.mUseLockPatternDrawable) {
            this.mNotSelectedDrawable.setBounds(this.mPaddingLeft, this.mPaddingTop, width, height);
            this.mSelectedDrawable.setBounds(this.mPaddingLeft, this.mPaddingTop, width, height);
        }
        this.mWidth = (float) w;
        this.mHeight = (float) h;
        if (!this.mPattern.isEmpty()) {
            Cell lastCell = this.mPattern.get(this.mPattern.size() - 1);
            if (lastCell != null) {
                this.mLastCellCenterX = getCenterXForColumn(lastCell.getColumn());
                this.mLastCellCenterY = getCenterYForRow(lastCell.getRow());
            }
        }
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int specSize = View.MeasureSpec.getSize(measureSpec);
        int mode = View.MeasureSpec.getMode(measureSpec);
        if (mode == Integer.MIN_VALUE) {
            return Math.max(specSize, desired);
        }
        if (mode != 0) {
            return specSize;
        }
        return desired;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minimumWidth = getSuggestedMinimumWidth();
        int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);
        switch (this.mAspect) {
            case 0:
                int min = Math.min(viewWidth, viewHeight);
                viewHeight = min;
                viewWidth = min;
                break;
            case 1:
                viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case 2:
                viewWidth = Math.min(viewWidth, viewHeight);
                break;
        }
        setMeasuredDimension(viewWidth, viewHeight);
    }

    private Cell detectAndAddHit(float x, float y) {
        Cell cell = checkForNewHit(x, y);
        if (cell == null) {
            return null;
        }
        Cell fillInGapCell = null;
        ArrayList<Cell> pattern = this.mPattern;
        if (!pattern.isEmpty()) {
            Cell lastCell = pattern.get(pattern.size() - 1);
            int dRow = cell.row - lastCell.row;
            int dColumn = cell.column - lastCell.column;
            int fillInRow = lastCell.row;
            int fillInColumn = lastCell.column;
            int i = -1;
            if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                fillInRow = lastCell.row + (dRow > 0 ? 1 : -1);
            }
            if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                int i2 = lastCell.column;
                if (dColumn > 0) {
                    i = 1;
                }
                fillInColumn = i2 + i;
            }
            fillInGapCell = Cell.of(fillInRow, fillInColumn);
        }
        Cell fillInGapCell2 = fillInGapCell;
        if (fillInGapCell2 != null && !this.mPatternDrawLookup[fillInGapCell2.row][fillInGapCell2.column]) {
            addCellToPattern(fillInGapCell2);
        }
        if (this.mIsHwTheme && !this.mInStealthMode) {
            moveToTouchArea(cell, getCenterXForColumn(cell.getColumn()), getCenterYForRow(cell.getRow()), x, y, true);
        }
        addCellToPattern(cell);
        if (this.mEnableHapticFeedback) {
            performHapticFeedback(1, 3);
        }
        return cell;
    }

    private void addCellToPattern(Cell newCell) {
        this.mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
        this.mPattern.add(newCell);
        if (!this.mInStealthMode) {
            startCellActivatedAnimation(newCell);
        }
        notifyCellAdded();
    }

    private void startCellActivatedAnimation(Cell cell) {
        final CellState cellState = this.mCellStates[cell.row][cell.column];
        if (this.mIsHwTheme) {
            enlargeCellAnimation((float) this.mDotRadius, (float) this.mDotRadiusActivated, this.mLinearOutSlowInInterpolator, cellState);
            return;
        }
        startRadiusAnimation((float) this.mDotRadius, (float) this.mDotRadiusActivated, 96, this.mLinearOutSlowInInterpolator, cellState, new Runnable() {
            public void run() {
                LockPatternView.this.startRadiusAnimation((float) LockPatternView.this.mDotRadiusActivated, (float) LockPatternView.this.mDotRadius, 192, LockPatternView.this.mFastOutSlowInInterpolator, cellState, null);
            }
        });
        startLineEndAnimation(cellState, this.mInProgressX, this.mInProgressY, getCenterXForColumn(cell.column), getCenterYForRow(cell.row));
    }

    private void startLineEndAnimation(final CellState state, float startX, float startY, float targetX, float targetY) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        final CellState cellState = state;
        final float f = startX;
        final float f2 = targetX;
        final float f3 = startY;
        final float f4 = targetY;
        AnonymousClass5 r1 = new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = ((Float) animation.getAnimatedValue()).floatValue();
                cellState.lineEndX = ((1.0f - t) * f) + (f2 * t);
                cellState.lineEndY = ((1.0f - t) * f3) + (f4 * t);
                LockPatternView.this.invalidate();
            }
        };
        valueAnimator.addUpdateListener(r1);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                state.lineAnimator = null;
            }
        });
        valueAnimator.setInterpolator(this.mFastOutSlowInInterpolator);
        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.start();
        state.lineAnimator = valueAnimator;
    }

    /* access modifiers changed from: private */
    public void startRadiusAnimation(float start, float end, long duration, Interpolator interpolator, final CellState state, final Runnable endRunnable) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{start, end});
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                state.radius = ((Float) animation.getAnimatedValue()).floatValue();
                LockPatternView.this.invalidate();
            }
        });
        if (endRunnable != null) {
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    endRunnable.run();
                }
            });
        }
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    private Cell checkForNewHit(float x, float y) {
        int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        int columnHit = getColumnHit(x);
        if (columnHit >= 0 && !this.mPatternDrawLookup[rowHit][columnHit]) {
            return Cell.of(rowHit, columnHit);
        }
        return null;
    }

    /* access modifiers changed from: private */
    public int getRowHit(float y) {
        float squareHeight = this.mSquareHeight;
        float hitSize = this.mHitFactor * squareHeight;
        float offset = ((float) this.mPaddingTop) + ((squareHeight - hitSize) / 2.0f);
        for (int i = 0; i < 3; i++) {
            float hitTop = (((float) i) * squareHeight) + offset;
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public int getColumnHit(float x) {
        float squareWidth = this.mSquareWidth;
        float hitSize = this.mHitFactor * squareWidth;
        float offset = ((float) this.mPaddingLeft) + ((squareWidth - hitSize) / 2.0f);
        for (int i = 0; i < 3; i++) {
            float hitLeft = (((float) i) * squareWidth) + offset;
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }

    public boolean onHoverEvent(MotionEvent event) {
        if (AccessibilityManager.getInstance(this.mContext).isTouchExplorationEnabled()) {
            int action = event.getAction();
            if (action != 7) {
                switch (action) {
                    case 9:
                        event.setAction(0);
                        break;
                    case 10:
                        event.setAction(1);
                        break;
                }
            } else {
                event.setAction(2);
            }
            onTouchEvent(event);
            event.setAction(action);
        }
        return super.onHoverEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mInputEnabled || !isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case 0:
                handleActionDown(event);
                return true;
            case 1:
                handleActionUp();
                return true;
            case 2:
                handleActionMove(event);
                return true;
            case 3:
                if (this.mPatternInProgress) {
                    setPatternInProgress(false);
                    resetPattern();
                    notifyPatternCleared();
                }
                return true;
            default:
                return false;
        }
    }

    private void setPatternInProgress(boolean progress) {
        this.mPatternInProgress = progress;
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void handleActionMove(MotionEvent event) {
        boolean invalidateNow;
        int historySize;
        MotionEvent motionEvent = event;
        int historySize2 = event.getHistorySize();
        this.mTmpInvalidateRect.setEmpty();
        boolean invalidateNow2 = false;
        int i = 0;
        while (i < historySize2 + 1) {
            float x = i < historySize2 ? motionEvent.getHistoricalX(i) : event.getX();
            float y = i < historySize2 ? motionEvent.getHistoricalY(i) : event.getY();
            Cell hitCell = detectAndAddHit(x, y);
            int patternSize = this.mPattern.size();
            if (hitCell != null && patternSize == 1) {
                setPatternInProgress(true);
                notifyPatternStarted();
            }
            float dx = Math.abs(x - this.mInProgressX);
            float dy = Math.abs(y - this.mInProgressY);
            if (dx > 0.0f || dy > 0.0f) {
                invalidateNow2 = true;
            }
            if (!this.mPatternInProgress || patternSize <= 0) {
                historySize = historySize2;
                invalidateNow = invalidateNow2;
            } else {
                ArrayList<Cell> pattern = this.mPattern;
                Cell lastCell = pattern.get(patternSize - 1);
                float lastCellCenterX = getCenterXForColumn(lastCell.column);
                float lastCellCenterY = getCenterYForRow(lastCell.row);
                float left = Math.min(lastCellCenterX, x);
                historySize = historySize2;
                float right = Math.max(lastCellCenterX, x);
                invalidateNow = invalidateNow2;
                float top = Math.min(lastCellCenterY, y);
                int i2 = patternSize;
                float bottom = Math.max(lastCellCenterY, y);
                if (hitCell != null) {
                    float f = dx;
                    float width = this.mSquareWidth * 0.5f;
                    float f2 = dy;
                    float dy2 = this.mSquareHeight * 0.5f;
                    ArrayList<Cell> arrayList = pattern;
                    float hitCellCenterX = getCenterXForColumn(hitCell.column);
                    float f3 = lastCellCenterX;
                    float lastCellCenterX2 = getCenterYForRow(hitCell.row);
                    float f4 = lastCellCenterY;
                    left = Math.min(hitCellCenterX - width, left);
                    right = Math.max(hitCellCenterX + width, right);
                    top = Math.min(lastCellCenterX2 - dy2, top);
                    bottom = Math.max(lastCellCenterX2 + dy2, bottom);
                } else {
                    float f5 = dy;
                    ArrayList<Cell> arrayList2 = pattern;
                    float f6 = lastCellCenterX;
                    float f7 = lastCellCenterY;
                }
                if (hitCell == null && this.mIsHwTheme && !this.mInStealthMode) {
                    lastCellAnimation(lastCell, x, y);
                }
                this.mTmpInvalidateRect.union(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
            }
            i++;
            historySize2 = historySize;
            invalidateNow2 = invalidateNow;
        }
        this.mInProgressX = event.getX();
        this.mInProgressY = event.getY();
        if (invalidateNow2) {
            this.mInvalidate.union(this.mTmpInvalidateRect);
            invalidate(this.mInvalidate);
            this.mInvalidate.set(this.mTmpInvalidateRect);
        }
    }

    private void sendAccessEvent(int resId) {
        announceForAccessibility(this.mContext.getString(resId));
    }

    private void handleActionUp() {
        if (!this.mPattern.isEmpty()) {
            backToCenterAfterActionUp();
            setPatternInProgress(false);
            cancelLineAnimations();
            notifyPatternDetected();
            if (this.mFadePattern) {
                clearPatternDrawLookup();
                this.mPatternDisplayMode = DisplayMode.Correct;
            }
            invalidate();
        }
    }

    private void cancelLineAnimations() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                CellState state = this.mCellStates[i][j];
                if (state.lineAnimator != null) {
                    state.lineAnimator.cancel();
                    state.lineEndX = Float.MIN_VALUE;
                    state.lineEndY = Float.MIN_VALUE;
                }
                if (state.moveAnimator != null) {
                    state.moveAnimator.cancel();
                }
            }
        }
    }

    private void handleActionDown(MotionEvent event) {
        resetPattern();
        float x = event.getX();
        float y = event.getY();
        Cell hitCell = detectAndAddHit(x, y);
        if (hitCell != null) {
            setPatternInProgress(true);
            this.mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else if (this.mPatternInProgress) {
            setPatternInProgress(false);
            notifyPatternCleared();
        }
        if (hitCell != null) {
            float startX = getCenterXForColumn(hitCell.column);
            float startY = getCenterYForRow(hitCell.row);
            float widthOffset = this.mSquareWidth / 2.0f;
            float heightOffset = this.mSquareHeight / 2.0f;
            invalidate((int) (startX - widthOffset), (int) (startY - heightOffset), (int) (startX + widthOffset), (int) (startY + heightOffset));
        }
        this.mInProgressX = x;
        this.mInProgressY = y;
    }

    /* access modifiers changed from: private */
    public float getCenterXForColumn(int column) {
        return ((float) this.mPaddingLeft) + (((float) column) * this.mSquareWidth) + (this.mSquareWidth / 2.0f);
    }

    /* access modifiers changed from: private */
    public float getCenterYForRow(int row) {
        return ((float) this.mPaddingTop) + (((float) row) * this.mSquareHeight) + (this.mSquareHeight / 2.0f);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        long elapsedRealtime;
        boolean anyCircles;
        boolean drawPath;
        char c;
        int count;
        Canvas canvas2 = canvas;
        if (this.mIsInKeyguard && this.mPickedColor != 0) {
            this.mRegularColor = this.mPickedColor;
            this.mSuccessColor = this.mPickedColor;
            this.mPathColor = this.mPickedColor;
        }
        ArrayList<Cell> pattern = this.mPattern;
        int count2 = pattern.size();
        boolean[][] drawLookup = this.mPatternDrawLookup;
        if (this.mPatternDisplayMode == DisplayMode.Animate) {
            int oneCycle = (count2 + 1) * 700;
            int spotInCycle = ((int) (SystemClock.elapsedRealtime() - this.mAnimatingPeriodStart)) % oneCycle;
            int numCircles = spotInCycle / 700;
            clearPatternDrawLookup();
            for (int i = 0; i < numCircles; i++) {
                Cell cell = pattern.get(i);
                drawLookup[cell.getRow()][cell.getColumn()] = true;
            }
            if (numCircles > 0 && numCircles < count2) {
                float percentageOfNextCircle = ((float) (spotInCycle % 700)) / 700.0f;
                Cell currentCell = pattern.get(numCircles - 1);
                float centerX = getCenterXForColumn(currentCell.column);
                float centerY = getCenterYForRow(currentCell.row);
                Cell nextCell = pattern.get(numCircles);
                int i2 = oneCycle;
                this.mInProgressX = centerX + ((getCenterXForColumn(nextCell.column) - centerX) * percentageOfNextCircle);
                this.mInProgressY = centerY + ((getCenterYForRow(nextCell.row) - centerY) * percentageOfNextCircle);
            }
            invalidate();
        }
        Path currentPath = this.mCurrentPath;
        currentPath.rewind();
        int i3 = 0;
        while (true) {
            int i4 = i3;
            int i5 = 3;
            if (i4 >= 3) {
                break;
            }
            int j = 0;
            while (true) {
                int j2 = j;
                if (j2 >= i5) {
                    break;
                }
                CellState cellState = this.mCellStates[i4][j2];
                float centerY2 = getCenterYForRow(i4);
                float centerX2 = getCenterXForColumn(j2);
                if (!this.mInStealthMode && this.mIsHwTheme && isLastCell(Cell.of(i4, j2))) {
                    centerX2 = this.mLastCellCenterX;
                    centerY2 = this.mLastCellCenterY;
                }
                float centerY3 = centerY2;
                float centerX3 = centerX2;
                float translationY = cellState.translationY;
                if (this.mUseLockPatternDrawable) {
                    float f = translationY;
                    float f2 = centerX3;
                    count = count2;
                    float f3 = centerY3;
                    drawCellDrawable(canvas2, i4, j2, cellState.radius, drawLookup[i4][j2]);
                } else {
                    float translationY2 = translationY;
                    float centerX4 = centerX3;
                    count = count2;
                    float centerY4 = centerY3;
                    if (!isHardwareAccelerated() || !cellState.hwAnimating) {
                        CellState cellState2 = cellState;
                        drawCircle(canvas2, (float) ((int) centerX4), ((float) ((int) centerY4)) + translationY2, cellState.radius, drawLookup[i4][j2], cellState.alpha);
                    } else {
                        ((DisplayListCanvas) canvas2).drawCircle(cellState.hwCenterX, cellState.hwCenterY, cellState.hwRadius, cellState.hwPaint);
                    }
                }
                j = j2 + 1;
                count2 = count;
                i5 = 3;
            }
            i3 = i4 + 1;
        }
        int count3 = count2;
        boolean i6 = !this.mInStealthMode;
        if (!i6) {
            boolean drawPath2 = i6;
            int i7 = count3;
        } else if (this.mIsHwTheme) {
            this.mPathPaint.setStyle(Paint.Style.FILL);
            this.mPathPaint.setColor(this.mPathColor);
            this.mPathPaint.setAlpha(this.mAlphaTransparent);
            this.mPathPaint.setStrokeWidth(2.0f);
            drawHwPath(pattern, drawLookup, currentPath, canvas2);
            boolean z = i6;
            int i8 = count3;
        } else {
            this.mPathPaint.setColor(getCurrentColor(true));
            boolean anyCircles2 = false;
            float lastX = 0.0f;
            float lastY = 0.0f;
            long elapsedRealtime2 = SystemClock.elapsedRealtime();
            int i9 = 0;
            while (true) {
                int i10 = i9;
                int count4 = count3;
                if (i10 >= count4) {
                    boolean drawPath3 = i6;
                    long j3 = elapsedRealtime2;
                    break;
                }
                Cell cell2 = pattern.get(i10);
                if (!drawLookup[cell2.row][cell2.column]) {
                    boolean z2 = i6;
                    long j4 = elapsedRealtime2;
                    break;
                }
                if (this.mLineFadeStart[i10] == 0) {
                    this.mLineFadeStart[i10] = SystemClock.elapsedRealtime();
                }
                float centerX5 = getCenterXForColumn(cell2.column);
                float centerY5 = getCenterYForRow(cell2.row);
                if (i10 != 0) {
                    drawPath = i6;
                    anyCircles = true;
                    c = 0;
                    int lineFadeVal = (int) Math.min(((float) (elapsedRealtime2 - this.mLineFadeStart[i10])) / 2.0f, 255.0f);
                    elapsedRealtime = elapsedRealtime2;
                    CellState state = this.mCellStates[cell2.row][cell2.column];
                    currentPath.rewind();
                    currentPath.moveTo(lastX, lastY);
                    if (state.lineEndX == Float.MIN_VALUE || state.lineEndY == Float.MIN_VALUE) {
                        currentPath.lineTo(centerX5, centerY5);
                        if (this.mFadePattern) {
                            this.mPathPaint.setAlpha(255 - lineFadeVal);
                        } else {
                            this.mPathPaint.setAlpha(255);
                        }
                    } else {
                        currentPath.lineTo(state.lineEndX, state.lineEndY);
                        if (this.mFadePattern) {
                            this.mPathPaint.setAlpha(255 - lineFadeVal);
                        } else {
                            this.mPathPaint.setAlpha(255);
                        }
                    }
                    canvas2.drawPath(currentPath, this.mPathPaint);
                } else {
                    drawPath = i6;
                    anyCircles = true;
                    elapsedRealtime = elapsedRealtime2;
                    c = 0;
                }
                lastX = centerX5;
                lastY = centerY5;
                count3 = count4;
                char c2 = c;
                anyCircles2 = anyCircles;
                elapsedRealtime2 = elapsedRealtime;
                i9 = i10 + 1;
                i6 = drawPath;
            }
            if ((this.mPatternInProgress || this.mPatternDisplayMode == DisplayMode.Animate) && anyCircles2) {
                currentPath.rewind();
                currentPath.moveTo(lastX, lastY);
                currentPath.lineTo(this.mInProgressX, this.mInProgressY);
                this.mPathPaint.setAlpha((int) (calculateLastSegmentAlpha(this.mInProgressX, this.mInProgressY, lastX, lastY) * 255.0f));
                canvas2.drawPath(currentPath, this.mPathPaint);
            }
        }
    }

    private float calculateLastSegmentAlpha(float x, float y, float lastX, float lastY) {
        float diffX = x - lastX;
        float diffY = y - lastY;
        return Math.min(1.0f, Math.max(0.0f, ((((float) Math.sqrt((double) ((diffX * diffX) + (diffY * diffY)))) / this.mSquareWidth) - 0.3f) * 4.0f));
    }

    private int getCurrentColor(boolean partOfPattern) {
        if (!partOfPattern || this.mInStealthMode || this.mPatternInProgress) {
            return this.mRegularColor;
        }
        if (this.mPatternDisplayMode == DisplayMode.Wrong) {
            return this.mErrorColor;
        }
        if (this.mPatternDisplayMode == DisplayMode.Correct || this.mPatternDisplayMode == DisplayMode.Animate) {
            return this.mSuccessColor;
        }
        throw new IllegalStateException("unknown display mode " + this.mPatternDisplayMode);
    }

    private void drawCircle(Canvas canvas, float centerX, float centerY, float radius, boolean partOfPattern, float alpha) {
        this.mPaint.setColor(getCurrentColor(partOfPattern));
        this.mPaint.setAlpha((int) (255.0f * alpha));
        canvas.drawCircle(centerX, centerY, radius, this.mPaint);
    }

    private void drawCellDrawable(Canvas canvas, int i, int j, float radius, boolean partOfPattern) {
        Rect dst = new Rect((int) (((float) this.mPaddingLeft) + (((float) j) * this.mSquareWidth)), (int) (((float) this.mPaddingTop) + (((float) i) * this.mSquareHeight)), (int) (((float) this.mPaddingLeft) + (((float) (j + 1)) * this.mSquareWidth)), (int) (((float) this.mPaddingTop) + (((float) (i + 1)) * this.mSquareHeight)));
        float scale = radius / ((float) this.mDotRadius);
        canvas.save();
        canvas.clipRect(dst);
        canvas.scale(scale, scale, (float) dst.centerX(), (float) dst.centerY());
        if (!partOfPattern || scale > 1.0f) {
            this.mNotSelectedDrawable.draw(canvas);
        } else {
            this.mSelectedDrawable.draw(canvas);
        }
        canvas.restore();
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState(), LockPatternUtils.patternToString(this.mPattern), this.mPatternDisplayMode.ordinal(), this.mInputEnabled, this.mInStealthMode, this.mEnableHapticFeedback);
        return savedState;
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setPattern(DisplayMode.Correct, LockPatternUtils.stringToPattern(ss.getSerializedPattern()));
        this.mPatternDisplayMode = DisplayMode.values()[ss.getDisplayMode()];
        this.mInputEnabled = ss.isInputEnabled();
        this.mInStealthMode = ss.isInStealthMode();
        this.mEnableHapticFeedback = ss.isTactileFeedbackEnabled();
    }

    private boolean isLastCell(Cell cell) {
        if (this.mPattern.isEmpty() || cell != this.mPattern.get(this.mPattern.size() - 1)) {
            return false;
        }
        return true;
    }

    private Cell touchACell(float x, float y) {
        int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        int columnHit = getColumnHit(x);
        if (columnHit < 0) {
            return null;
        }
        return Cell.of(rowHit, columnHit);
    }

    private void moveToTouchArea(Cell cell, float currCenterX, float currCenterY, float touchX, float touchY, boolean hasAnimation) {
        int row = cell.getRow();
        int column = cell.getColumn();
        if (hasAnimation) {
            final CellState cellState = this.mCellStates[row][column];
            if (cellState.moveAnimator != null) {
                cellState.moveAnimator.cancel();
            }
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            final float f = currCenterX;
            final float f2 = touchX;
            final float f3 = currCenterY;
            final float f4 = touchY;
            final int i = row;
            final int i2 = column;
            AnonymousClass9 r0 = new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float t = ((Float) animation.getAnimatedValue()).floatValue();
                    float unused = LockPatternView.this.mLastCellCenterX = ((1.0f - t) * f) + (f2 * t);
                    float unused2 = LockPatternView.this.mLastCellCenterY = ((1.0f - t) * f3) + (f4 * t);
                    LockPatternView.this.mCellStates[i][i2].hwCenterX = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterX);
                    LockPatternView.this.mCellStates[i][i2].hwCenterY = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterY);
                    LockPatternView.this.invalidate();
                }
            };
            valueAnimator.addUpdateListener(r0);
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    cellState.moveAnimator = null;
                }
            });
            valueAnimator.setDuration(ANIM_DURATION);
            valueAnimator.setInterpolator(this.mInterpolator);
            if (cellState.moveAnimator == null) {
                valueAnimator.start();
                cellState.moveAnimator = valueAnimator;
            }
            float f5 = touchX;
            float f6 = touchY;
            return;
        }
        this.mLastCellCenterX = touchX;
        this.mLastCellCenterY = touchY;
        this.mCellStates[row][column].hwCenterX = CanvasProperty.createFloat(touchX);
        this.mCellStates[row][column].hwCenterY = CanvasProperty.createFloat(touchY);
    }

    private void cellBackToCenter(Cell cell, float currentX, float currentY) {
        int row = cell.getRow();
        int column = cell.getColumn();
        float centerX = getCenterXForColumn(column);
        float centerY = getCenterYForRow(row);
        final CellState cellState = this.mCellStates[row][column];
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        final float f = currentX;
        final float f2 = centerX;
        final float f3 = currentY;
        final float f4 = centerY;
        final CellState cellState2 = cellState;
        AnonymousClass11 r0 = new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = ((Float) animation.getAnimatedValue()).floatValue();
                float unused = LockPatternView.this.mLastCellCenterX = ((1.0f - t) * f) + (f2 * t);
                float unused2 = LockPatternView.this.mLastCellCenterY = ((1.0f - t) * f3) + (f4 * t);
                cellState2.hwCenterX = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterX);
                cellState2.hwCenterY = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterY);
                LockPatternView.this.invalidate();
            }
        };
        valueAnimator.addUpdateListener(r0);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                cellState.moveAnimator = null;
            }
        });
        valueAnimator.setInterpolator(this.mInterpolator);
        valueAnimator.setDuration(ANIM_DURATION);
        if (cellState.moveAnimator == null) {
            valueAnimator.start();
            this.mCellStates[row][column].moveAnimator = valueAnimator;
        }
    }

    private void enlargeCellAnimation(float start, float end, Interpolator interpolator, final CellState cellState) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{start, end});
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                cellState.radius = ((Float) animation.getAnimatedValue()).floatValue();
                LockPatternView.this.invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                cellState.lineAnimator = null;
            }
        });
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.start();
        cellState.lineAnimator = valueAnimator;
    }

    private void drawHwPath(ArrayList<Cell> pattern, boolean[][] drawLookup, Path currentPath, Canvas canvas) {
        float currX;
        float currY;
        Path path = currentPath;
        int count = pattern.size();
        int i = 0;
        boolean anyCircles = false;
        float lastX = 0.0f;
        float lastY = 0.0f;
        while (true) {
            int i2 = i;
            if (i2 >= count) {
                ArrayList<Cell> arrayList = pattern;
                break;
            }
            Cell cell = pattern.get(i2);
            if (!drawLookup[cell.row][cell.column]) {
                break;
            }
            anyCircles = true;
            float centerX = getCenterXForColumn(cell.column);
            float centerY = getCenterYForRow(cell.row);
            if (i2 != 0) {
                if (isLastCell(cell)) {
                    centerX = this.mLastCellCenterX;
                    centerY = this.mLastCellCenterY;
                }
                float centerX2 = centerX;
                float centerY2 = centerY;
                connectTwoCells(path, (float) this.mDotCircleRadius, lastX, lastY, centerX2, centerY2);
                centerX = centerX2;
                centerY = centerY2;
            }
            lastX = centerX;
            lastY = centerY;
            i = i2 + 1;
        }
        if ((this.mPatternInProgress || this.mPatternDisplayMode == DisplayMode.Animate) && anyCircles) {
            Cell cell2 = touchACell(this.mInProgressX, this.mInProgressY);
            if (cell2 == null || !isLastCell(cell2)) {
                float currX2 = this.mInProgressX;
                float currY2 = this.mInProgressY;
                float margin = (float) this.mLineRadius;
                if (this.mInProgressX < margin) {
                    currX2 = margin;
                }
                if (this.mInProgressX > this.mWidth - margin) {
                    currX = this.mWidth - margin;
                } else {
                    currX = currX2;
                }
                if (this.mInProgressY < margin) {
                    currY2 = margin;
                }
                if (this.mInProgressY > this.mHeight - margin) {
                    currY = this.mHeight - margin;
                } else {
                    currY = currY2;
                }
                connectCellToPoint(path, (float) this.mDotCircleRadius, this.mLastCellCenterX, this.mLastCellCenterY, currX, currY);
            }
        }
        if (count == 1) {
            path.addCircle(this.mLastCellCenterX, this.mLastCellCenterY, (float) this.mDotCircleRadius, Path.Direction.CCW);
        }
        canvas.drawPath(path, this.mPathPaint);
    }

    private void connectTwoCells(Path currentPath, float radius, float startX, float startY, float endX, float endY) {
        double baseAngle;
        Path path = currentPath;
        float f = radius;
        float f2 = startX;
        float f3 = startY;
        float f4 = endX;
        float f5 = endY;
        double midAngle = Math.atan(0.2d);
        double midRadius = Math.sqrt(((double) (this.mLineRadius * this.mLineRadius)) + (((double) f) * 1.6d * ((double) f) * 1.6d));
        if (Math.abs(f2 - f4) < 1.0f) {
            baseAngle = f5 < f3 ? 1.5707963267948966d : -1.5707963267948966d;
        } else {
            baseAngle = -Math.atan2((double) (f5 - f3), (double) (f4 - f2));
        }
        double baseAngle2 = baseAngle;
        path.moveTo((float) (((double) f2) + (((double) f) * Math.cos(baseAngle2 - 0.7853981633974483d))), (float) (((double) f3) - (((double) f) * Math.sin(baseAngle2 - 0.7853981633974483d))));
        double baseAngle3 = baseAngle2;
        path.arcTo(f2 - f, f3 - f, f2 + f, f3 + f, (float) Math.toDegrees((-baseAngle2) + 0.7853981633974483d), 270.0f, false);
        path.quadTo((float) (((double) f2) + (((double) (1.05f * f)) * Math.cos(baseAngle3 + (0.7853981633974483d / 2.0d)))), (float) (((double) f3) - (((double) (1.05f * f)) * Math.sin(baseAngle3 + (0.7853981633974483d / 2.0d)))), (float) (((double) f2) + (Math.cos(baseAngle3 + midAngle) * midRadius)), (float) (((double) f3) - (Math.sin(baseAngle3 + midAngle) * midRadius)));
        float f6 = endX;
        float f7 = endY;
        path.lineTo((float) (((double) f6) + (Math.cos((baseAngle3 - midAngle) + 3.141592653589793d) * midRadius)), (float) (((double) f7) - (Math.sin((baseAngle3 - midAngle) + 3.141592653589793d) * midRadius)));
        path.lineTo((float) (((double) f6) + (Math.cos(3.141592653589793d + baseAngle3 + midAngle) * midRadius)), (float) (((double) f7) - (Math.sin((3.141592653589793d + baseAngle3) + midAngle) * midRadius)));
        path.lineTo((float) (((double) f2) + (Math.cos(baseAngle3 - midAngle) * midRadius)), (float) (((double) f3) - (Math.sin(baseAngle3 - midAngle) * midRadius)));
        path.quadTo((float) (((double) f2) + (((double) (1.05f * f)) * Math.cos(baseAngle3 - (0.7853981633974483d / 2.0d)))), (float) (((double) f3) - (((double) (1.05f * f)) * Math.sin(baseAngle3 - (0.7853981633974483d / 2.0d)))), (float) (((double) f2) + (((double) f) * Math.cos(baseAngle3 - 0.7853981633974483d))), (float) (((double) f3) - (((double) f) * Math.sin(baseAngle3 - 0.7853981633974483d))));
        path.moveTo((float) (((double) f6) + (Math.cos(3.141592653589793d + baseAngle3 + midAngle) * midRadius)), (float) (((double) f7) - (Math.sin((3.141592653589793d + baseAngle3) + midAngle) * midRadius)));
        float f8 = f7;
        float f9 = f6;
        path.arcTo(f6 - f, f7 - f, f6 + f, f7 + f, (float) Math.toDegrees((3.141592653589793d - baseAngle3) + 0.7853981633974483d), 270.0f, false);
        path.quadTo((float) (((double) f9) + (((double) (1.05f * f)) * Math.cos(3.141592653589793d + baseAngle3 + (0.7853981633974483d / 2.0d)))), (float) (((double) f8) - (((double) (1.05f * f)) * Math.sin((3.141592653589793d + baseAngle3) + (0.7853981633974483d / 2.0d)))), (float) (((double) f9) + (Math.cos(3.141592653589793d + baseAngle3 + midAngle) * midRadius)), (float) (((double) f8) - (Math.sin((3.141592653589793d + baseAngle3) + midAngle) * midRadius)));
        path.lineTo((float) (((double) f9) + (Math.cos((baseAngle3 - midAngle) + 3.141592653589793d) * midRadius)), (float) (((double) f8) - (Math.sin((baseAngle3 - midAngle) + 3.141592653589793d) * midRadius)));
        path.quadTo((float) (((double) f9) + (((double) (1.05f * f)) * Math.cos((3.141592653589793d + baseAngle3) - (0.7853981633974483d / 2.0d)))), (float) (((double) f8) - (((double) (1.05f * f)) * Math.sin((3.141592653589793d + baseAngle3) - (0.7853981633974483d / 2.0d)))), (float) (((double) f9) + (((double) f) * Math.cos((3.141592653589793d + baseAngle3) - 0.7853981633974483d))), (float) (((double) f8) - (((double) f) * Math.sin((3.141592653589793d + baseAngle3) - 0.7853981633974483d))));
        invalidate();
    }

    private void connectCellToPoint(Path currentPath, float radius, float startX, float startY, float endX, float endY) {
        double baseAngle;
        Path path = currentPath;
        float f = radius;
        float f2 = startX;
        float f3 = startY;
        float f4 = endX;
        float f5 = endY;
        double midAngle = Math.atan(0.2d);
        double midRadius = Math.sqrt(((double) (this.mLineRadius * this.mLineRadius)) + (((double) f) * 1.6d * ((double) f) * 1.6d));
        float distance = (float) Math.hypot((double) (f4 - f2), (double) (f5 - f3));
        if (Math.abs(f2 - f4) < 1.0f) {
            baseAngle = f5 < f3 ? 1.5707963267948966d : -1.5707963267948966d;
        } else {
            baseAngle = -Math.atan2((double) (f5 - f3), (double) (f4 - f2));
        }
        double baseAngle2 = baseAngle;
        float endCenterX = (float) (((double) f4) + (((((double) (f / 2.0f)) * Math.cos(baseAngle2 + 1.5707963267948966d)) + (((double) (f / 2.0f)) * Math.cos(baseAngle2 - 1.5707963267948966d))) / 2.0d));
        float endCenterY = (float) (((double) f5) - (((((double) (f / 2.0f)) * Math.sin(baseAngle2 + 1.5707963267948966d)) + (((double) (f / 2.0f)) * Math.sin(baseAngle2 - 1.5707963267948966d))) / 2.0d));
        if (distance > 2.0f * f) {
            float f6 = startY;
            path.moveTo((float) (((double) f2) + (((double) f) * Math.cos(baseAngle2 - 0.7853981633974483d))), (float) (((double) f6) - (((double) f) * Math.sin(baseAngle2 - 0.7853981633974483d))));
            float endCenterX2 = endCenterX;
            path.arcTo(f2 - f, f6 - f, f2 + f, f6 + f, (float) Math.toDegrees((-baseAngle2) + 0.7853981633974483d), -90.0f, false);
            double baseAngle3 = baseAngle2;
            float f7 = distance;
            path.quadTo((float) (((double) f2) + (((double) (1.05f * f)) * Math.cos(baseAngle3 + (0.7853981633974483d / 2.0d)))), (float) (((double) f6) - (((double) (1.05f * f)) * Math.sin(baseAngle3 + (0.7853981633974483d / 2.0d)))), (float) (((double) f2) + (Math.cos(baseAngle3 + midAngle) * midRadius)), (float) (((double) f6) - (Math.sin(baseAngle3 + midAngle) * midRadius)));
            float f8 = endX;
            float f9 = endY;
            float endCenterY2 = endCenterY;
            path.lineTo((float) (((double) f8) + (((double) this.mLineRadius) * Math.cos(baseAngle3 + 1.5707963267948966d))), (float) (((double) f9) - (((double) this.mLineRadius) * Math.sin(baseAngle3 + 1.5707963267948966d))));
            path.lineTo((float) (((double) f8) + (((double) this.mLineRadius) * Math.cos(baseAngle3 - 1.5707963267948966d))), (float) (((double) f9) - (((double) this.mLineRadius) * Math.sin(baseAngle3 - 1.5707963267948966d))));
            float f10 = startY;
            path.lineTo((float) (((double) f2) + (Math.cos(baseAngle3 - midAngle) * midRadius)), (float) (((double) f10) - (Math.sin(baseAngle3 - midAngle) * midRadius)));
            path.quadTo((float) (((double) f2) + (((double) (1.05f * f)) * Math.cos(baseAngle3 - (0.7853981633974483d / 2.0d)))), (float) (((double) f10) - (((double) (1.05f * f)) * Math.sin(baseAngle3 - (0.7853981633974483d / 2.0d)))), (float) (((double) f2) + (((double) f) * Math.cos(baseAngle3 - 0.7853981633974483d))), (float) (((double) f10) - (((double) f) * Math.sin(baseAngle3 - 0.7853981633974483d))));
            double d = baseAngle3;
            path.addArc(endCenterX2 - ((float) this.mLineRadius), endCenterY2 - ((float) this.mLineRadius), endCenterX2 + ((float) this.mLineRadius), endCenterY2 + ((float) this.mLineRadius), (float) Math.toDegrees((-baseAngle3) - 1.5707963267948966d), 180.0f);
        } else {
            float endCenterX3 = endCenterX;
            float endCenterY3 = endCenterY;
            float f11 = endY;
            double baseAngle4 = baseAngle2;
            if (distance > f) {
                float f12 = startX;
                float f13 = startY;
                path.moveTo((float) (((double) f12) + (((double) f) * Math.cos(baseAngle4 - 0.7853981633974483d))), (float) (((double) f13) - (((double) f) * Math.sin(baseAngle4 - 0.7853981633974483d))));
                path.arcTo(f12 - f, f13 - f, f12 + f, f13 + f, (float) Math.toDegrees((-baseAngle4) + 0.7853981633974483d), -90.0f, false);
                float f14 = endX;
                path.quadTo((float) (((double) f12) + (((double) f) * Math.cos((0.7853981633974483d / 2.0d) + baseAngle4))), (float) (((double) f13) - (((double) f) * Math.sin(baseAngle4 + (0.7853981633974483d / 2.0d)))), (float) (((double) f14) + (((double) this.mLineRadius) * Math.cos(baseAngle4 + 1.5707963267948966d))), (float) (((double) f11) - (((double) this.mLineRadius) * Math.sin(baseAngle4 + 1.5707963267948966d))));
                path.lineTo((float) (((double) f14) + (((double) this.mLineRadius) * Math.cos(baseAngle4 - 1.5707963267948966d))), (float) (((double) f11) - (((double) this.mLineRadius) * Math.sin(baseAngle4 - 1.5707963267948966d))));
                float f15 = startX;
                float f16 = radius;
                path.quadTo((float) (((double) f15) + (((double) f16) * Math.cos(baseAngle4 - (0.7853981633974483d / 2.0d)))), (float) (((double) f13) - (((double) f16) * Math.sin(baseAngle4 - (0.7853981633974483d / 2.0d)))), (float) (((double) f15) + (((double) f16) * Math.cos(baseAngle4 - 0.7853981633974483d))), (float) (((double) f13) - (((double) f16) * Math.sin(baseAngle4 - 0.7853981633974483d))));
                path.addArc(endCenterX3 - ((float) this.mLineRadius), endCenterY3 - ((float) this.mLineRadius), endCenterX3 + ((float) this.mLineRadius), endCenterY3 + ((float) this.mLineRadius), (float) Math.toDegrees((-baseAngle4) - 1.5707963267948966d), 180.0f);
                invalidate();
            }
        }
        float f17 = startY;
        invalidate();
    }

    private void resetCellRadius() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.mCellStates[i][j].radius = (float) this.mDotRadius;
            }
        }
    }

    private void lastCellAnimation(Cell lastCell, float x, float y) {
        Cell touchedCell = touchACell(x, y);
        float currCenterX = this.mLastCellCenterX;
        float curreCenterY = this.mLastCellCenterY;
        CellState cellState = this.mCellStates[lastCell.getRow()][lastCell.getColumn()];
        boolean isAtCenter = Math.abs(this.mLastCellCenterX - getCenterXForColumn(lastCell.getColumn())) < 0.01f && Math.abs(this.mLastCellCenterY - getCenterYForRow(lastCell.getRow())) < 0.01f;
        if (touchedCell != null && isLastCell(touchedCell)) {
            moveToTouchArea(touchedCell, currCenterX, curreCenterY, x, y, false);
        } else if (!isAtCenter && cellState.moveAnimator == null) {
            cellBackToCenter(lastCell, currCenterX, curreCenterY);
        }
    }

    private void backToCenterAfterActionUp() {
        Cell lastCell = this.mPattern.get(this.mPattern.size() - 1);
        CellState cellState = this.mCellStates[lastCell.getRow()][lastCell.getColumn()];
        this.mLastCellCenterX = getCenterXForColumn(lastCell.getColumn());
        this.mLastCellCenterY = getCenterYForRow(lastCell.getRow());
        cellState.hwCenterX = CanvasProperty.createFloat(this.mLastCellCenterX);
        cellState.hwCenterY = CanvasProperty.createFloat(this.mLastCellCenterY);
        invalidate();
    }

    public void setRegularColor(boolean isInKeyguard, int color) {
        this.mIsInKeyguard = isInKeyguard;
        this.mPickedColor = color;
    }
}
