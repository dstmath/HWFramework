package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.IntArray;
import android.view.DisplayListCanvas;
import android.view.MotionEvent;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.internal.R;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final CellState[][] mCellStates;
    private final Path mCurrentPath;
    private final int mDotCircleRadius;
    private final int mDotRadius;
    private final int mDotRadiusActivated;
    private boolean mDrawingProfilingStarted;
    private boolean mEnableHapticFeedback;
    private int mErrorColor;
    private PatternExploreByTouchHelper mExploreByTouchHelper;
    private final Interpolator mFastOutSlowInInterpolator;
    private float mHeight;
    private float mHitFactor;
    private float mInProgressX;
    private float mInProgressY;
    private boolean mInStealthMode;
    private boolean mInputEnabled;
    private final Interpolator mInterpolator;
    private final Rect mInvalidate;
    private boolean mIsHwTheme;
    private float mLastCellCenterX;
    private float mLastCellCenterY;
    private final int mLineRadius;
    private final Interpolator mLinearOutSlowInInterpolator;
    private Drawable mNotSelectedDrawable;
    private OnPatternListener mOnPatternListener;
    private final Paint mPaint;
    private int mPathColor;
    private final Paint mPathPaint;
    private final ArrayList<Cell> mPattern;
    private DisplayMode mPatternDisplayMode;
    private final boolean[][] mPatternDrawLookup;
    private boolean mPatternInProgress;
    private int mRegularColor;
    private Drawable mSelectedDrawable;
    private float mSquareHeight;
    private float mSquareWidth;
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

        private Cell(int row, int column) {
            checkRange(row, column);
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return this.row;
        }

        public int getColumn() {
            return this.column;
        }

        public static Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }

        private static void checkRange(int row, int column) {
            if (row < 0 || row > 2) {
                throw new IllegalArgumentException("row must be in range 0-2");
            } else if (column < 0 || column > 2) {
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
        private HashMap<Integer, VirtualViewContainer> mItems = new HashMap();
        private Rect mTempRect = new Rect();

        class VirtualViewContainer {
            CharSequence description;

            public VirtualViewContainer(CharSequence description) {
                this.description = description;
            }
        }

        public PatternExploreByTouchHelper(View forView) {
            super(forView);
        }

        protected int getVirtualViewAt(float x, float y) {
            return getVirtualViewIdForHit(x, y);
        }

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            if (LockPatternView.this.mPatternInProgress) {
                for (int i = 1; i < 10; i++) {
                    if (!this.mItems.containsKey(Integer.valueOf(i))) {
                        this.mItems.put(Integer.valueOf(i), new VirtualViewContainer(getTextForVirtualView(i)));
                    }
                    virtualViewIds.add(i);
                }
            }
        }

        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            if (this.mItems.containsKey(Integer.valueOf(virtualViewId))) {
                event.getText().add(((VirtualViewContainer) this.mItems.get(Integer.valueOf(virtualViewId))).description);
            }
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);
            if (!LockPatternView.this.mPatternInProgress) {
                event.setContentDescription(LockPatternView.this.getContext().getText(R.string.lockscreen_access_pattern_area));
            }
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            node.setText(getTextForVirtualView(virtualViewId));
            node.setContentDescription(getTextForVirtualView(virtualViewId));
            if (LockPatternView.this.mPatternInProgress) {
                node.setFocusable(true);
                if (isClickable(virtualViewId)) {
                    node.addAction(AccessibilityAction.ACTION_CLICK);
                    node.setClickable(isClickable(virtualViewId));
                }
            }
            node.setBoundsInParent(getBoundsForVirtualView(virtualViewId));
        }

        private boolean isClickable(int virtualViewId) {
            if (virtualViewId == Integer.MIN_VALUE) {
                return false;
            }
            return LockPatternView.this.mPatternDrawLookup[(virtualViewId - 1) / 3][(virtualViewId - 1) % 3] ^ 1;
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            switch (action) {
                case 16:
                    return onItemClicked(virtualViewId);
                default:
                    return false;
            }
        }

        boolean onItemClicked(int index) {
            invalidateVirtualView(index);
            sendEventForVirtualView(index, 1);
            return true;
        }

        private Rect getBoundsForVirtualView(int virtualViewId) {
            int ordinal = virtualViewId - 1;
            Rect bounds = this.mTempRect;
            int row = ordinal / 3;
            int col = ordinal % 3;
            CellState cell = LockPatternView.this.mCellStates[row][col];
            float centerX = LockPatternView.this.getCenterXForColumn(col);
            float centerY = LockPatternView.this.getCenterYForRow(row);
            float cellheight = (LockPatternView.this.mSquareHeight * LockPatternView.this.mHitFactor) * 0.5f;
            float cellwidth = (LockPatternView.this.mSquareWidth * LockPatternView.this.mHitFactor) * 0.5f;
            bounds.left = (int) (centerX - cellwidth);
            bounds.right = (int) (centerX + cellwidth);
            bounds.top = (int) (centerY - cellheight);
            bounds.bottom = (int) (centerY + cellheight);
            return bounds;
        }

        private CharSequence getTextForVirtualView(int virtualViewId) {
            return LockPatternView.this.getResources().getString(R.string.lockscreen_access_pattern_cell_added_verbose, new Object[]{Integer.valueOf(virtualViewId)});
        }

        private int getVirtualViewIdForHit(float x, float y) {
            int rowHit = LockPatternView.this.getRowHit(y);
            if (rowHit < 0) {
                return Integer.MIN_VALUE;
            }
            int columnHit = LockPatternView.this.getColumnHit(x);
            if (columnHit < 0) {
                return Integer.MIN_VALUE;
            }
            return LockPatternView.this.mPatternDrawLookup[rowHit][columnHit] ? ((rowHit * 3) + columnHit) + 1 : Integer.MIN_VALUE;
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
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

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

        /* synthetic */ SavedState(Parcelable superState, String serializedPattern, int displayMode, boolean inputEnabled, boolean inStealthMode, boolean tactileFeedbackEnabled, SavedState -this6) {
            this(superState, serializedPattern, displayMode, inputEnabled, inStealthMode, tactileFeedbackEnabled);
        }

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
        this.mPattern = new ArrayList(9);
        this.mPatternDrawLookup = (boolean[][]) Array.newInstance(Boolean.TYPE, new int[]{3, 3});
        this.mInProgressX = -1.0f;
        this.mInProgressY = -1.0f;
        this.mPatternDisplayMode = DisplayMode.Correct;
        this.mInputEnabled = true;
        this.mInStealthMode = false;
        this.mEnableHapticFeedback = true;
        this.mPatternInProgress = false;
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
        this.mIsHwTheme = HwWidgetFactory.checkIsHwTheme(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LockPatternView, R.attr.lockPatternStyle, R.style.Widget_LockPatternView);
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
        this.mRegularColor = context.getColor(33882269);
        this.mSuccessColor = context.getColor(33882270);
        this.mRegularColor = a.getColor(3, this.mRegularColor);
        this.mErrorColor = a.getColor(1, 0);
        this.mSuccessColor = a.getColor(4, this.mSuccessColor);
        this.mPathPaint.setColor(a.getColor(2, this.mRegularColor));
        this.mPathPaint.setStyle(Style.FILL);
        this.mPathPaint.setStrokeJoin(Join.ROUND);
        this.mPathPaint.setStrokeCap(Cap.ROUND);
        this.mPathPaint.setStrokeWidth(2.0f);
        this.mLineRadius = getResources().getDimensionPixelSize(34472102);
        this.mDotCircleRadius = getResources().getDimensionPixelSize(34472101);
        this.mDotRadius = getResources().getDimensionPixelSize(34472099);
        this.mDotRadiusActivated = getResources().getDimensionPixelSize(34472100);
        this.mUseLockPatternDrawable = getResources().getBoolean(R.bool.use_lock_pattern_drawable);
        if (this.mUseLockPatternDrawable) {
            this.mSelectedDrawable = getResources().getDrawable(R.drawable.lockscreen_selected);
            this.mNotSelectedDrawable = getResources().getDrawable(R.drawable.lockscreen_notselected);
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
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, R.interpolator.fast_out_slow_in);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, R.interpolator.linear_out_slow_in);
        this.mExploreByTouchHelper = new PatternExploreByTouchHelper(this);
        setAccessibilityDelegate(this.mExploreByTouchHelper);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
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
            if (this.mPattern.size() == 0) {
                throw new IllegalStateException("you must have a pattern to animate if you want to set the display mode to animate");
            }
            this.mAnimatingPeriodStart = SystemClock.elapsedRealtime();
            Cell first = (Cell) this.mPattern.get(0);
            this.mInProgressX = getCenterXForColumn(first.getColumn());
            this.mInProgressY = getCenterYForRow(first.getRow());
            clearPatternDrawLookup();
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
        cellState.alpha = startAlpha;
        cellState.translationY = startTranslationY;
        cellState.radius = ((float) this.mDotRadius) * startScale;
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.setInterpolator(interpolator);
        final CellState cellState2 = cellState;
        final float f = startAlpha;
        final float f2 = endAlpha;
        final float f3 = startTranslationY;
        final float f4 = endTranslationY;
        final float f5 = startScale;
        final float f6 = endScale;
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = ((Float) animation.getAnimatedValue()).floatValue();
                cellState2.alpha = ((1.0f - t) * f) + (f2 * t);
                cellState2.translationY = ((1.0f - t) * f3) + (f4 * t);
                cellState2.radius = ((float) LockPatternView.this.mDotRadius) * (((1.0f - t) * f5) + (f6 * t));
                LockPatternView.this.invalidate();
            }
        });
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

    private void startCellStateAnimationHw(final CellState cellState, float startAlpha, float endAlpha, float startTranslationY, float endTranslationY, float startScale, float endScale, long delay, long duration, Interpolator interpolator, Runnable finishRunnable) {
        cellState.alpha = endAlpha;
        cellState.translationY = endTranslationY;
        cellState.radius = ((float) this.mDotRadius) * endScale;
        cellState.hwAnimating = true;
        cellState.hwCenterY = CanvasProperty.createFloat(getCenterYForRow(cellState.row) + startTranslationY);
        cellState.hwCenterX = CanvasProperty.createFloat(getCenterXForColumn(cellState.col));
        cellState.hwRadius = CanvasProperty.createFloat(((float) this.mDotRadius) * startScale);
        this.mPaint.setColor(getCurrentColor(false));
        this.mPaint.setAlpha((int) (255.0f * startAlpha));
        cellState.hwPaint = CanvasProperty.createPaint(new Paint(this.mPaint));
        startRtFloatAnimation(cellState.hwCenterY, getCenterYForRow(cellState.row) + endTranslationY, delay, duration, interpolator);
        startRtFloatAnimation(cellState.hwRadius, ((float) this.mDotRadius) * endScale, delay, duration, interpolator);
        final Runnable runnable = finishRunnable;
        startRtAlphaAnimation(cellState, endAlpha, delay, duration, interpolator, new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                cellState.hwAnimating = false;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        invalidate();
    }

    private void startRtAlphaAnimation(CellState cellState, float endAlpha, long delay, long duration, Interpolator interpolator, AnimatorListener listener) {
        RenderNodeAnimator animator = new RenderNodeAnimator(cellState.hwPaint, 1, (float) ((int) (255.0f * endAlpha)));
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.setInterpolator(interpolator);
        animator.setTarget((View) this);
        animator.addListener(listener);
        animator.start();
    }

    private void startRtFloatAnimation(CanvasProperty<Float> property, float endValue, long delay, long duration, Interpolator interpolator) {
        RenderNodeAnimator animator = new RenderNodeAnimator((CanvasProperty) property, endValue);
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.setInterpolator(interpolator);
        animator.setTarget((View) this);
        animator.start();
    }

    private void notifyCellAdded() {
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternCellAdded(this.mPattern);
        }
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void notifyPatternStarted() {
        sendAccessEvent(R.string.lockscreen_access_pattern_start);
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternStart();
        }
    }

    private void notifyPatternDetected() {
        sendAccessEvent(R.string.lockscreen_access_pattern_detected);
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternDetected(this.mPattern);
        }
    }

    private void notifyPatternCleared() {
        sendAccessEvent(R.string.lockscreen_access_pattern_cleared);
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternCleared();
        }
    }

    public void clearPattern() {
        resetPattern();
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
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
            }
        }
    }

    public void disableInput() {
        this.mInputEnabled = false;
    }

    public void enableInput() {
        this.mInputEnabled = true;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
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
            Cell lastCell = (Cell) this.mPattern.get(this.mPattern.size() - 1);
            if (lastCell != null) {
                this.mLastCellCenterX = getCenterXForColumn(lastCell.getColumn());
                this.mLastCellCenterY = getCenterYForRow(lastCell.getRow());
            }
        }
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case Integer.MIN_VALUE:
                return Math.max(specSize, desired);
            case 0:
                return desired;
            default:
                return specSize;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minimumWidth = getSuggestedMinimumWidth();
        int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);
        switch (this.mAspect) {
            case 0:
                viewHeight = Math.min(viewWidth, viewHeight);
                viewWidth = viewHeight;
                break;
            case 1:
                viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case 2:
                viewWidth = Math.min(viewWidth, viewHeight);
                break;
        }
        -wrap3(viewWidth, viewHeight);
    }

    private Cell detectAndAddHit(float x, float y) {
        Cell cell = checkForNewHit(x, y);
        if (cell == null) {
            return null;
        }
        Cell fillInGapCell = null;
        ArrayList<Cell> pattern = this.mPattern;
        if (!pattern.isEmpty()) {
            Cell lastCell = (Cell) pattern.get(pattern.size() - 1);
            int dRow = cell.row - lastCell.row;
            int dColumn = cell.column - lastCell.column;
            int fillInRow = lastCell.row;
            int fillInColumn = lastCell.column;
            if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                fillInRow = lastCell.row + (dRow > 0 ? 1 : -1);
            }
            if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                fillInColumn = lastCell.column + (dColumn > 0 ? 1 : -1);
            }
            fillInGapCell = Cell.of(fillInRow, fillInColumn);
        }
        if (!(fillInGapCell == null || (this.mPatternDrawLookup[fillInGapCell.row][fillInGapCell.column] ^ 1) == 0)) {
            addCellToPattern(fillInGapCell);
        }
        if (this.mIsHwTheme && (this.mInStealthMode ^ 1) != 0) {
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
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = ((Float) animation.getAnimatedValue()).floatValue();
                cellState.lineEndX = ((1.0f - t) * f) + (f2 * t);
                cellState.lineEndY = ((1.0f - t) * f3) + (f4 * t);
                LockPatternView.this.invalidate();
            }
        });
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

    private void startRadiusAnimation(float start, float end, long duration, Interpolator interpolator, final CellState state, final Runnable endRunnable) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{start, end});
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
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

    private int getRowHit(float y) {
        float squareHeight = this.mSquareHeight;
        float hitSize = squareHeight * this.mHitFactor;
        float offset = ((float) this.mPaddingTop) + ((squareHeight - hitSize) / 2.0f);
        for (int i = 0; i < 3; i++) {
            float hitTop = offset + (((float) i) * squareHeight);
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }
        return -1;
    }

    private int getColumnHit(float x) {
        float squareWidth = this.mSquareWidth;
        float hitSize = squareWidth * this.mHitFactor;
        float offset = ((float) this.mPaddingLeft) + ((squareWidth - hitSize) / 2.0f);
        for (int i = 0; i < 3; i++) {
            float hitLeft = offset + (((float) i) * squareWidth);
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }

    public boolean onHoverEvent(MotionEvent event) {
        if (AccessibilityManager.getInstance(this.mContext).isTouchExplorationEnabled()) {
            int action = event.getAction();
            switch (action) {
                case 7:
                    event.setAction(2);
                    break;
                case 9:
                    event.setAction(0);
                    break;
                case 10:
                    event.setAction(1);
                    break;
            }
            onTouchEvent(event);
            event.setAction(action);
        }
        return super.-wrap9(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mInputEnabled || (isEnabled() ^ 1) != 0) {
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
        int historySize = event.getHistorySize();
        this.mTmpInvalidateRect.setEmpty();
        boolean invalidateNow = false;
        int i = 0;
        while (i < historySize + 1) {
            float x = i < historySize ? event.getHistoricalX(i) : event.getX();
            float y = i < historySize ? event.getHistoricalY(i) : event.getY();
            Cell hitCell = detectAndAddHit(x, y);
            int patternSize = this.mPattern.size();
            if (hitCell != null && patternSize == 1) {
                setPatternInProgress(true);
                notifyPatternStarted();
            }
            float dx = Math.abs(x - this.mInProgressX);
            float dy = Math.abs(y - this.mInProgressY);
            if (dx > 0.0f || dy > 0.0f) {
                invalidateNow = true;
            }
            if (this.mPatternInProgress && patternSize > 0) {
                Cell lastCell = (Cell) this.mPattern.get(patternSize - 1);
                float lastCellCenterX = getCenterXForColumn(lastCell.column);
                float lastCellCenterY = getCenterYForRow(lastCell.row);
                float left = Math.min(lastCellCenterX, x);
                float right = Math.max(lastCellCenterX, x);
                float top = Math.min(lastCellCenterY, y);
                float bottom = Math.max(lastCellCenterY, y);
                if (hitCell != null) {
                    float width = this.mSquareWidth * 0.5f;
                    float height = this.mSquareHeight * 0.5f;
                    float hitCellCenterX = getCenterXForColumn(hitCell.column);
                    float hitCellCenterY = getCenterYForRow(hitCell.row);
                    left = Math.min(hitCellCenterX - width, left);
                    right = Math.max(hitCellCenterX + width, right);
                    top = Math.min(hitCellCenterY - height, top);
                    bottom = Math.max(hitCellCenterY + height, bottom);
                }
                if (hitCell == null && this.mIsHwTheme && (this.mInStealthMode ^ 1) != 0) {
                    lastCellAnimation(lastCell, x, y);
                }
                this.mTmpInvalidateRect.union(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
            }
            i++;
        }
        this.mInProgressX = event.getX();
        this.mInProgressY = event.getY();
        if (invalidateNow) {
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

    private float getCenterXForColumn(int column) {
        return (((float) this.mPaddingLeft) + (((float) column) * this.mSquareWidth)) + (this.mSquareWidth / 2.0f);
    }

    private float getCenterYForRow(int row) {
        return (((float) this.mPaddingTop) + (((float) row) * this.mSquareHeight)) + (this.mSquareHeight / 2.0f);
    }

    protected void onDraw(Canvas canvas) {
        int i;
        Cell cell;
        float centerX;
        float centerY;
        ArrayList<Cell> pattern = this.mPattern;
        int count = pattern.size();
        boolean[][] drawLookup = this.mPatternDrawLookup;
        if (this.mPatternDisplayMode == DisplayMode.Animate) {
            int spotInCycle = ((int) (SystemClock.elapsedRealtime() - this.mAnimatingPeriodStart)) % ((count + 1) * 700);
            int numCircles = spotInCycle / 700;
            clearPatternDrawLookup();
            for (i = 0; i < numCircles; i++) {
                cell = (Cell) pattern.get(i);
                drawLookup[cell.getRow()][cell.getColumn()] = true;
            }
            boolean needToUpdateInProgressPoint = numCircles > 0 ? numCircles < count : false;
            if (needToUpdateInProgressPoint) {
                float percentageOfNextCircle = ((float) (spotInCycle % 700)) / 700.0f;
                Cell currentCell = (Cell) pattern.get(numCircles - 1);
                centerX = getCenterXForColumn(currentCell.column);
                centerY = getCenterYForRow(currentCell.row);
                Cell nextCell = (Cell) pattern.get(numCircles);
                float dy = percentageOfNextCircle * (getCenterYForRow(nextCell.row) - centerY);
                this.mInProgressX = centerX + (percentageOfNextCircle * (getCenterXForColumn(nextCell.column) - centerX));
                this.mInProgressY = centerY + dy;
            }
            invalidate();
        }
        Path currentPath = this.mCurrentPath;
        currentPath.rewind();
        for (i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                CellState cellState = this.mCellStates[i][j];
                centerY = getCenterYForRow(i);
                centerX = getCenterXForColumn(j);
                if (!this.mInStealthMode && this.mIsHwTheme) {
                    if (isLastCell(Cell.of(i, j))) {
                        centerX = this.mLastCellCenterX;
                        centerY = this.mLastCellCenterY;
                    }
                }
                float translationY = cellState.translationY;
                if (this.mUseLockPatternDrawable) {
                    drawCellDrawable(canvas, i, j, cellState.radius, drawLookup[i][j]);
                } else if (isHardwareAccelerated() && cellState.hwAnimating) {
                    ((DisplayListCanvas) canvas).drawCircle(cellState.hwCenterX, cellState.hwCenterY, cellState.hwRadius, cellState.hwPaint);
                } else {
                    drawCircle(canvas, (float) ((int) centerX), ((float) ((int) centerY)) + translationY, cellState.radius, drawLookup[i][j], cellState.alpha);
                }
            }
        }
        if (!(this.mInStealthMode ^ 1)) {
            return;
        }
        if (this.mIsHwTheme) {
            this.mPathPaint.setStyle(Style.FILL);
            this.mPathPaint.setColor(this.mPathColor);
            this.mPathPaint.setAlpha(this.mAlphaTransparent);
            this.mPathPaint.setStrokeWidth(2.0f);
            drawHwPath(pattern, drawLookup, currentPath, canvas);
            return;
        }
        this.mPathPaint.setColor(getCurrentColor(true));
        boolean anyCircles = false;
        float lastX = 0.0f;
        float lastY = 0.0f;
        for (i = 0; i < count; i++) {
            cell = (Cell) pattern.get(i);
            if (!drawLookup[cell.row][cell.column]) {
                break;
            }
            anyCircles = true;
            centerX = getCenterXForColumn(cell.column);
            centerY = getCenterYForRow(cell.row);
            if (i != 0) {
                CellState state = this.mCellStates[cell.row][cell.column];
                currentPath.rewind();
                currentPath.moveTo(lastX, lastY);
                if (state.lineEndX == Float.MIN_VALUE || state.lineEndY == Float.MIN_VALUE) {
                    currentPath.lineTo(centerX, centerY);
                } else {
                    currentPath.lineTo(state.lineEndX, state.lineEndY);
                }
                canvas.drawPath(currentPath, this.mPathPaint);
            }
            lastX = centerX;
            lastY = centerY;
        }
        if ((this.mPatternInProgress || this.mPatternDisplayMode == DisplayMode.Animate) && anyCircles) {
            currentPath.rewind();
            currentPath.moveTo(lastX, lastY);
            currentPath.lineTo(this.mInProgressX, this.mInProgressY);
            this.mPathPaint.setAlpha((int) (calculateLastSegmentAlpha(this.mInProgressX, this.mInProgressY, lastX, lastY) * 255.0f));
            canvas.drawPath(currentPath, this.mPathPaint);
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

    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.-wrap0(), LockPatternUtils.patternToString(this.mPattern), this.mPatternDisplayMode.ordinal(), this.mInputEnabled, this.mInStealthMode, this.mEnableHapticFeedback, null);
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.-wrap2(ss.getSuperState());
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
        final int row = cell.getRow();
        final int column = cell.getColumn();
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
            valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float t = ((Float) animation.getAnimatedValue()).floatValue();
                    LockPatternView.this.mLastCellCenterX = ((1.0f - t) * f) + (f2 * t);
                    LockPatternView.this.mLastCellCenterY = ((1.0f - t) * f3) + (f4 * t);
                    LockPatternView.this.mCellStates[row][column].hwCenterX = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterX);
                    LockPatternView.this.mCellStates[row][column].hwCenterY = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterY);
                    LockPatternView.this.invalidate();
                }
            });
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
                return;
            }
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
        final float centerX = getCenterXForColumn(column);
        final float centerY = getCenterYForRow(row);
        final CellState cellState = this.mCellStates[row][column];
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        final float f = currentX;
        final float f2 = currentY;
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = ((Float) animation.getAnimatedValue()).floatValue();
                LockPatternView.this.mLastCellCenterX = ((1.0f - t) * f) + (centerX * t);
                LockPatternView.this.mLastCellCenterY = ((1.0f - t) * f2) + (centerY * t);
                cellState.hwCenterX = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterX);
                cellState.hwCenterY = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterY);
                LockPatternView.this.invalidate();
            }
        });
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
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
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
        Cell cell;
        int count = pattern.size();
        boolean anyCircles = false;
        float lastX = 0.0f;
        float lastY = 0.0f;
        for (int i = 0; i < count; i++) {
            cell = (Cell) pattern.get(i);
            if (!drawLookup[cell.row][cell.column]) {
                break;
            }
            anyCircles = true;
            float centerX = getCenterXForColumn(cell.column);
            float centerY = getCenterYForRow(cell.row);
            if (i != 0) {
                if (isLastCell(cell)) {
                    centerX = this.mLastCellCenterX;
                    centerY = this.mLastCellCenterY;
                }
                connectTwoCells(currentPath, (float) this.mDotCircleRadius, lastX, lastY, centerX, centerY);
            }
            lastX = centerX;
            lastY = centerY;
        }
        if ((this.mPatternInProgress || this.mPatternDisplayMode == DisplayMode.Animate) && anyCircles) {
            cell = touchACell(this.mInProgressX, this.mInProgressY);
            if (cell == null || (isLastCell(cell) ^ 1) != 0) {
                float currX = this.mInProgressX;
                float currY = this.mInProgressY;
                float margin = (float) this.mLineRadius;
                if (this.mInProgressX < margin) {
                    currX = margin;
                }
                if (this.mInProgressX > this.mWidth - margin) {
                    currX = this.mWidth - margin;
                }
                if (this.mInProgressY < margin) {
                    currY = margin;
                }
                if (this.mInProgressY > this.mHeight - margin) {
                    currY = this.mHeight - margin;
                }
                connectCellToPoint(currentPath, (float) this.mDotCircleRadius, this.mLastCellCenterX, this.mLastCellCenterY, currX, currY);
            }
        }
        if (count == 1) {
            currentPath.addCircle(this.mLastCellCenterX, this.mLastCellCenterY, (float) this.mDotCircleRadius, Direction.CCW);
        }
        canvas.drawPath(currentPath, this.mPathPaint);
    }

    private void connectTwoCells(Path currentPath, float radius, float startX, float startY, float endX, float endY) {
        double midAngle = Math.atan(0.2d);
        double midRadius = Math.sqrt(((double) (this.mLineRadius * this.mLineRadius)) + ((((double) radius) * 1.6d) * (((double) radius) * 1.6d)));
        double baseAngle = Math.abs(startX - endX) < 1.0f ? endY < startY ? 1.5707963267948966d : -1.5707963267948966d : -Math.atan2((double) (endY - startY), (double) (endX - startX));
        currentPath.moveTo((float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
        currentPath.arcTo(startX - radius, startY - radius, startX + radius, startY + radius, (float) Math.toDegrees((-baseAngle) + 0.7853981633974483d), 270.0f, false);
        currentPath.quadTo((float) (((double) startX) + (((double) (1.05f * radius)) * Math.cos(0.39269908169872414d + baseAngle))), (float) (((double) startY) - (((double) (1.05f * radius)) * Math.sin(0.39269908169872414d + baseAngle))), (float) (((double) startX) + (Math.cos(baseAngle + midAngle) * midRadius)), (float) (((double) startY) - (Math.sin(baseAngle + midAngle) * midRadius)));
        currentPath.lineTo((float) (((double) endX) + (Math.cos((baseAngle - midAngle) + 3.141592653589793d) * midRadius)), (float) (((double) endY) - (Math.sin((baseAngle - midAngle) + 3.141592653589793d) * midRadius)));
        currentPath.lineTo((float) (((double) endX) + (Math.cos((3.141592653589793d + baseAngle) + midAngle) * midRadius)), (float) (((double) endY) - (Math.sin((3.141592653589793d + baseAngle) + midAngle) * midRadius)));
        currentPath.lineTo((float) (((double) startX) + (Math.cos(baseAngle - midAngle) * midRadius)), (float) (((double) startY) - (Math.sin(baseAngle - midAngle) * midRadius)));
        currentPath.quadTo((float) (((double) startX) + (((double) (1.05f * radius)) * Math.cos(baseAngle - 0.39269908169872414d))), (float) (((double) startY) - (((double) (1.05f * radius)) * Math.sin(baseAngle - 0.39269908169872414d))), (float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
        currentPath.moveTo((float) (((double) endX) + (Math.cos((3.141592653589793d + baseAngle) + midAngle) * midRadius)), (float) (((double) endY) - (Math.sin((3.141592653589793d + baseAngle) + midAngle) * midRadius)));
        currentPath.arcTo(endX - radius, endY - radius, endX + radius, endY + radius, (float) Math.toDegrees((3.141592653589793d - baseAngle) + 0.7853981633974483d), 270.0f, false);
        currentPath.quadTo((float) (((double) endX) + (((double) (1.05f * radius)) * Math.cos((3.141592653589793d + baseAngle) + 0.39269908169872414d))), (float) (((double) endY) - (((double) (1.05f * radius)) * Math.sin((3.141592653589793d + baseAngle) + 0.39269908169872414d))), (float) (((double) endX) + (Math.cos((3.141592653589793d + baseAngle) + midAngle) * midRadius)), (float) (((double) endY) - (Math.sin((3.141592653589793d + baseAngle) + midAngle) * midRadius)));
        currentPath.lineTo((float) (((double) endX) + (Math.cos((baseAngle - midAngle) + 3.141592653589793d) * midRadius)), (float) (((double) endY) - (Math.sin((baseAngle - midAngle) + 3.141592653589793d) * midRadius)));
        currentPath.quadTo((float) (((double) endX) + (((double) (1.05f * radius)) * Math.cos((3.141592653589793d + baseAngle) - 0.39269908169872414d))), (float) (((double) endY) - (((double) (1.05f * radius)) * Math.sin((3.141592653589793d + baseAngle) - 0.39269908169872414d))), (float) (((double) endX) + (((double) radius) * Math.cos((3.141592653589793d + baseAngle) - 0.7853981633974483d))), (float) (((double) endY) - (((double) radius) * Math.sin((3.141592653589793d + baseAngle) - 0.7853981633974483d))));
        invalidate();
    }

    private void connectCellToPoint(Path currentPath, float radius, float startX, float startY, float endX, float endY) {
        double midAngle = Math.atan(0.2d);
        double midRadius = Math.sqrt(((double) (this.mLineRadius * this.mLineRadius)) + ((((double) radius) * 1.6d) * (((double) radius) * 1.6d)));
        float distance = (float) Math.hypot((double) (endX - startX), (double) (endY - startY));
        double baseAngle = Math.abs(startX - endX) < 1.0f ? endY < startY ? 1.5707963267948966d : -1.5707963267948966d : -Math.atan2((double) (endY - startY), (double) (endX - startX));
        float endCenterX = (float) (((double) endX) + (((((double) (radius / 2.0f)) * Math.cos(1.5707963267948966d + baseAngle)) + (((double) (radius / 2.0f)) * Math.cos(baseAngle - 1.5707963267948966d))) / 2.0d));
        float endCenterY = (float) (((double) endY) - (((((double) (radius / 2.0f)) * Math.sin(1.5707963267948966d + baseAngle)) + (((double) (radius / 2.0f)) * Math.sin(baseAngle - 1.5707963267948966d))) / 2.0d));
        if (distance > 2.0f * radius) {
            currentPath.moveTo((float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
            currentPath.arcTo(startX - radius, startY - radius, startX + radius, startY + radius, (float) Math.toDegrees((-baseAngle) + 0.7853981633974483d), -90.0f, false);
            currentPath.quadTo((float) (((double) startX) + (((double) (1.05f * radius)) * Math.cos(0.39269908169872414d + baseAngle))), (float) (((double) startY) - (((double) (1.05f * radius)) * Math.sin(0.39269908169872414d + baseAngle))), (float) (((double) startX) + (Math.cos(baseAngle + midAngle) * midRadius)), (float) (((double) startY) - (Math.sin(baseAngle + midAngle) * midRadius)));
            currentPath.lineTo((float) (((double) endX) + (((double) this.mLineRadius) * Math.cos(1.5707963267948966d + baseAngle))), (float) (((double) endY) - (((double) this.mLineRadius) * Math.sin(1.5707963267948966d + baseAngle))));
            currentPath.lineTo((float) (((double) endX) + (((double) this.mLineRadius) * Math.cos(baseAngle - 1.5707963267948966d))), (float) (((double) endY) - (((double) this.mLineRadius) * Math.sin(baseAngle - 1.5707963267948966d))));
            currentPath.lineTo((float) (((double) startX) + (Math.cos(baseAngle - midAngle) * midRadius)), (float) (((double) startY) - (Math.sin(baseAngle - midAngle) * midRadius)));
            currentPath.quadTo((float) (((double) startX) + (((double) (1.05f * radius)) * Math.cos(baseAngle - 0.39269908169872414d))), (float) (((double) startY) - (((double) (1.05f * radius)) * Math.sin(baseAngle - 0.39269908169872414d))), (float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
            currentPath.addArc(endCenterX - ((float) this.mLineRadius), endCenterY - ((float) this.mLineRadius), endCenterX + ((float) this.mLineRadius), endCenterY + ((float) this.mLineRadius), (float) Math.toDegrees((-baseAngle) - 1.5707963267948966d), 180.0f);
        } else if (distance > radius) {
            currentPath.moveTo((float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
            currentPath.arcTo(startX - radius, startY - radius, startX + radius, startY + radius, (float) Math.toDegrees((-baseAngle) + 0.7853981633974483d), -90.0f, false);
            currentPath.quadTo((float) (((double) startX) + (((double) radius) * Math.cos(0.39269908169872414d + baseAngle))), (float) (((double) startY) - (((double) radius) * Math.sin(0.39269908169872414d + baseAngle))), (float) (((double) endX) + (((double) this.mLineRadius) * Math.cos(1.5707963267948966d + baseAngle))), (float) (((double) endY) - (((double) this.mLineRadius) * Math.sin(1.5707963267948966d + baseAngle))));
            currentPath.lineTo((float) (((double) endX) + (((double) this.mLineRadius) * Math.cos(baseAngle - 1.5707963267948966d))), (float) (((double) endY) - (((double) this.mLineRadius) * Math.sin(baseAngle - 1.5707963267948966d))));
            currentPath.quadTo((float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.39269908169872414d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.39269908169872414d))), (float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
            currentPath.addArc(endCenterX - ((float) this.mLineRadius), endCenterY - ((float) this.mLineRadius), endCenterX + ((float) this.mLineRadius), endCenterY + ((float) this.mLineRadius), (float) Math.toDegrees((-baseAngle) - 1.5707963267948966d), 180.0f);
        }
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
        boolean isAtCenter = Math.abs(this.mLastCellCenterX - getCenterXForColumn(lastCell.getColumn())) < 0.01f ? Math.abs(this.mLastCellCenterY - getCenterYForRow(lastCell.getRow())) < 0.01f : false;
        if (touchedCell != null && isLastCell(touchedCell)) {
            moveToTouchArea(touchedCell, currCenterX, curreCenterY, x, y, false);
        } else if (!isAtCenter && cellState.moveAnimator == null) {
            cellBackToCenter(lastCell, currCenterX, curreCenterY);
        }
    }

    private void backToCenterAfterActionUp() {
        Cell lastCell = (Cell) this.mPattern.get(this.mPattern.size() - 1);
        CellState cellState = this.mCellStates[lastCell.getRow()][lastCell.getColumn()];
        this.mLastCellCenterX = getCenterXForColumn(lastCell.getColumn());
        this.mLastCellCenterY = getCenterYForRow(lastCell.getRow());
        cellState.hwCenterX = CanvasProperty.createFloat(this.mLastCellCenterX);
        cellState.hwCenterY = CanvasProperty.createFloat(this.mLastCellCenterY);
        invalidate();
    }
}
