package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
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
import android.hwcontrol.HwWidgetFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.AttributeSet;
import android.util.IntArray;
import android.view.DisplayListCanvas;
import android.view.MotionEvent;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.internal.R;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import huawei.cust.HwCfgFilePolicy;
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
    private OnPatternListener mOnPatternListener;
    private final Paint mPaint;
    private int mPathColor;
    private final Paint mPathPaint;
    private final ArrayList<Cell> mPattern;
    private DisplayMode mPatternDisplayMode;
    private final boolean[][] mPatternDrawLookup;
    private boolean mPatternInProgress;
    private int mRegularColor;
    private float mSquareHeight;
    private float mSquareWidth;
    private int mSuccessColor;
    private final Rect mTmpInvalidateRect;
    private float mWidth;

    /* renamed from: com.android.internal.widget.LockPatternView.10 */
    class AnonymousClass10 extends AnimatorListenerAdapter {
        final /* synthetic */ CellState val$cellState;

        AnonymousClass10(CellState val$cellState) {
            this.val$cellState = val$cellState;
        }

        public void onAnimationEnd(Animator animation) {
            this.val$cellState.moveAnimator = null;
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.11 */
    class AnonymousClass11 implements AnimatorUpdateListener {
        final /* synthetic */ CellState val$cellState;
        final /* synthetic */ float val$centerX;
        final /* synthetic */ float val$centerY;
        final /* synthetic */ float val$currentX;
        final /* synthetic */ float val$currentY;

        AnonymousClass11(float val$currentX, float val$centerX, float val$currentY, float val$centerY, CellState val$cellState) {
            this.val$currentX = val$currentX;
            this.val$centerX = val$centerX;
            this.val$currentY = val$currentY;
            this.val$centerY = val$centerY;
            this.val$cellState = val$cellState;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float t = ((Float) animation.getAnimatedValue()).floatValue();
            LockPatternView.this.mLastCellCenterX = ((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - t) * this.val$currentX) + (this.val$centerX * t);
            LockPatternView.this.mLastCellCenterY = ((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - t) * this.val$currentY) + (this.val$centerY * t);
            this.val$cellState.hwCenterX = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterX);
            this.val$cellState.hwCenterY = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterY);
            LockPatternView.this.invalidate();
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.12 */
    class AnonymousClass12 extends AnimatorListenerAdapter {
        final /* synthetic */ CellState val$cellState;

        AnonymousClass12(CellState val$cellState) {
            this.val$cellState = val$cellState;
        }

        public void onAnimationEnd(Animator animation) {
            this.val$cellState.moveAnimator = null;
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.13 */
    class AnonymousClass13 implements AnimatorUpdateListener {
        final /* synthetic */ CellState val$cellState;

        AnonymousClass13(CellState val$cellState) {
            this.val$cellState = val$cellState;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            this.val$cellState.radius = ((Float) animation.getAnimatedValue()).floatValue();
            LockPatternView.this.invalidate();
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.14 */
    class AnonymousClass14 extends AnimatorListenerAdapter {
        final /* synthetic */ CellState val$cellState;

        AnonymousClass14(CellState val$cellState) {
            this.val$cellState = val$cellState;
        }

        public void onAnimationEnd(Animator animation) {
            this.val$cellState.lineAnimator = null;
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.1 */
    class AnonymousClass1 implements AnimatorUpdateListener {
        final /* synthetic */ CellState val$cellState;
        final /* synthetic */ float val$endAlpha;
        final /* synthetic */ float val$endScale;
        final /* synthetic */ float val$endTranslationY;
        final /* synthetic */ float val$startAlpha;
        final /* synthetic */ float val$startScale;
        final /* synthetic */ float val$startTranslationY;

        AnonymousClass1(CellState val$cellState, float val$startAlpha, float val$endAlpha, float val$startTranslationY, float val$endTranslationY, float val$startScale, float val$endScale) {
            this.val$cellState = val$cellState;
            this.val$startAlpha = val$startAlpha;
            this.val$endAlpha = val$endAlpha;
            this.val$startTranslationY = val$startTranslationY;
            this.val$endTranslationY = val$endTranslationY;
            this.val$startScale = val$startScale;
            this.val$endScale = val$endScale;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float t = ((Float) animation.getAnimatedValue()).floatValue();
            this.val$cellState.alpha = ((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - t) * this.val$startAlpha) + (this.val$endAlpha * t);
            this.val$cellState.translationY = ((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - t) * this.val$startTranslationY) + (this.val$endTranslationY * t);
            this.val$cellState.radius = ((float) LockPatternView.this.mDotRadius) * (((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - t) * this.val$startScale) + (this.val$endScale * t));
            LockPatternView.this.invalidate();
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.2 */
    class AnonymousClass2 extends AnimatorListenerAdapter {
        final /* synthetic */ Runnable val$finishRunnable;

        AnonymousClass2(Runnable val$finishRunnable) {
            this.val$finishRunnable = val$finishRunnable;
        }

        public void onAnimationEnd(Animator animation) {
            if (this.val$finishRunnable != null) {
                this.val$finishRunnable.run();
            }
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.3 */
    class AnonymousClass3 extends AnimatorListenerAdapter {
        final /* synthetic */ CellState val$cellState;
        final /* synthetic */ Runnable val$finishRunnable;

        AnonymousClass3(CellState val$cellState, Runnable val$finishRunnable) {
            this.val$cellState = val$cellState;
            this.val$finishRunnable = val$finishRunnable;
        }

        public void onAnimationEnd(Animator animation) {
            this.val$cellState.hwAnimating = LockPatternView.PROFILE_DRAWING;
            if (this.val$finishRunnable != null) {
                this.val$finishRunnable.run();
            }
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ CellState val$cellState;

        AnonymousClass4(CellState val$cellState) {
            this.val$cellState = val$cellState;
        }

        public void run() {
            LockPatternView.this.startRadiusAnimation((float) LockPatternView.this.mDotRadiusActivated, (float) LockPatternView.this.mDotRadius, 192, LockPatternView.this.mFastOutSlowInInterpolator, this.val$cellState, null);
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.5 */
    class AnonymousClass5 implements AnimatorUpdateListener {
        final /* synthetic */ float val$startX;
        final /* synthetic */ float val$startY;
        final /* synthetic */ CellState val$state;
        final /* synthetic */ float val$targetX;
        final /* synthetic */ float val$targetY;

        AnonymousClass5(CellState val$state, float val$startX, float val$targetX, float val$startY, float val$targetY) {
            this.val$state = val$state;
            this.val$startX = val$startX;
            this.val$targetX = val$targetX;
            this.val$startY = val$startY;
            this.val$targetY = val$targetY;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float t = ((Float) animation.getAnimatedValue()).floatValue();
            this.val$state.lineEndX = ((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - t) * this.val$startX) + (this.val$targetX * t);
            this.val$state.lineEndY = ((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - t) * this.val$startY) + (this.val$targetY * t);
            LockPatternView.this.invalidate();
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.6 */
    class AnonymousClass6 extends AnimatorListenerAdapter {
        final /* synthetic */ CellState val$state;

        AnonymousClass6(CellState val$state) {
            this.val$state = val$state;
        }

        public void onAnimationEnd(Animator animation) {
            this.val$state.lineAnimator = null;
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.7 */
    class AnonymousClass7 implements AnimatorUpdateListener {
        final /* synthetic */ CellState val$state;

        AnonymousClass7(CellState val$state) {
            this.val$state = val$state;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            this.val$state.radius = ((Float) animation.getAnimatedValue()).floatValue();
            LockPatternView.this.invalidate();
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.8 */
    class AnonymousClass8 extends AnimatorListenerAdapter {
        final /* synthetic */ Runnable val$endRunnable;

        AnonymousClass8(Runnable val$endRunnable) {
            this.val$endRunnable = val$endRunnable;
        }

        public void onAnimationEnd(Animator animation) {
            this.val$endRunnable.run();
        }
    }

    /* renamed from: com.android.internal.widget.LockPatternView.9 */
    class AnonymousClass9 implements AnimatorUpdateListener {
        final /* synthetic */ int val$column;
        final /* synthetic */ float val$currCenterX;
        final /* synthetic */ float val$currCenterY;
        final /* synthetic */ int val$row;
        final /* synthetic */ float val$touchX;
        final /* synthetic */ float val$touchY;

        AnonymousClass9(float val$currCenterX, float val$touchX, float val$currCenterY, float val$touchY, int val$row, int val$column) {
            this.val$currCenterX = val$currCenterX;
            this.val$touchX = val$touchX;
            this.val$currCenterY = val$currCenterY;
            this.val$touchY = val$touchY;
            this.val$row = val$row;
            this.val$column = val$column;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float t = ((Float) animation.getAnimatedValue()).floatValue();
            LockPatternView.this.mLastCellCenterX = ((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - t) * this.val$currCenterX) + (this.val$touchX * t);
            LockPatternView.this.mLastCellCenterY = ((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - t) * this.val$currCenterY) + (this.val$touchY * t);
            LockPatternView.this.mCellStates[this.val$row][this.val$column].hwCenterX = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterX);
            LockPatternView.this.mCellStates[this.val$row][this.val$column].hwCenterY = CanvasProperty.createFloat(LockPatternView.this.mLastCellCenterY);
            LockPatternView.this.invalidate();
        }
    }

    public static final class Cell {
        private static final Cell[][] sCells = null;
        final int column;
        final int row;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.LockPatternView.Cell.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.LockPatternView.Cell.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternView.Cell.<clinit>():void");
        }

        private static Cell[][] createCells() {
            Cell[][] res = (Cell[][]) Array.newInstance(Cell.class, new int[]{3, 3});
            for (int i = LockPatternView.ASPECT_SQUARE; i < 3; i += LockPatternView.VIRTUAL_BASE_VIEW_ID) {
                for (int j = LockPatternView.ASPECT_SQUARE; j < 3; j += LockPatternView.VIRTUAL_BASE_VIEW_ID) {
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
            if (row < 0 || row > LockPatternView.ASPECT_LOCK_HEIGHT) {
                throw new IllegalArgumentException("row must be in range 0-2");
            } else if (column < 0 || column > LockPatternView.ASPECT_LOCK_HEIGHT) {
                throw new IllegalArgumentException("column must be in range 0-2");
            }
        }

        public String toString() {
            return "(row=" + this.row + ",clmn=" + this.column + ")";
        }
    }

    public static class CellState {
        float alpha;
        int col;
        boolean hwAnimating;
        CanvasProperty<Float> hwCenterX;
        CanvasProperty<Float> hwCenterY;
        CanvasProperty<Paint> hwPaint;
        CanvasProperty<Float> hwRadius;
        public ValueAnimator lineAnimator;
        public float lineEndX;
        public float lineEndY;
        public ValueAnimator moveAnimator;
        float radius;
        int row;
        float translationY;

        public CellState() {
            this.alpha = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            this.lineEndX = Float.MIN_VALUE;
            this.lineEndY = Float.MIN_VALUE;
        }
    }

    public enum DisplayMode {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.LockPatternView.DisplayMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.LockPatternView.DisplayMode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternView.DisplayMode.<clinit>():void");
        }
    }

    public interface OnPatternListener {
        void onPatternCellAdded(List<Cell> list);

        void onPatternCleared();

        void onPatternDetected(List<Cell> list);

        void onPatternStart();
    }

    private final class PatternExploreByTouchHelper extends ExploreByTouchHelper {
        private HashMap<Integer, VirtualViewContainer> mItems;
        private Rect mTempRect;
        final /* synthetic */ LockPatternView this$0;

        class VirtualViewContainer {
            CharSequence description;
            final /* synthetic */ PatternExploreByTouchHelper this$1;

            public VirtualViewContainer(PatternExploreByTouchHelper this$1, CharSequence description) {
                this.this$1 = this$1;
                this.description = description;
            }
        }

        public PatternExploreByTouchHelper(LockPatternView this$0, View forView) {
            this.this$0 = this$0;
            super(forView);
            this.mTempRect = new Rect();
            this.mItems = new HashMap();
        }

        protected int getVirtualViewAt(float x, float y) {
            return getVirtualViewIdForHit(x, y);
        }

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            if (this.this$0.mPatternInProgress) {
                for (int i = LockPatternView.VIRTUAL_BASE_VIEW_ID; i < 10; i += LockPatternView.VIRTUAL_BASE_VIEW_ID) {
                    if (!this.mItems.containsKey(Integer.valueOf(i))) {
                        this.mItems.put(Integer.valueOf(i), new VirtualViewContainer(this, getTextForVirtualView(i)));
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
            if (!this.this$0.mPatternInProgress) {
                event.setContentDescription(this.this$0.getContext().getText(R.string.lockscreen_access_pattern_area));
            }
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            node.setText(getTextForVirtualView(virtualViewId));
            node.setContentDescription(getTextForVirtualView(virtualViewId));
            if (this.this$0.mPatternInProgress) {
                node.setFocusable(true);
                if (isClickable(virtualViewId)) {
                    node.addAction(AccessibilityAction.ACTION_CLICK);
                    node.setClickable(isClickable(virtualViewId));
                }
            }
            node.setBoundsInParent(getBoundsForVirtualView(virtualViewId));
        }

        private boolean isClickable(int virtualViewId) {
            boolean z = LockPatternView.PROFILE_DRAWING;
            if (virtualViewId == RtlSpacingHelper.UNDEFINED) {
                return LockPatternView.PROFILE_DRAWING;
            }
            if (!this.this$0.mPatternDrawLookup[(virtualViewId - 1) / 3][(virtualViewId - 1) % 3]) {
                z = true;
            }
            return z;
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            switch (action) {
                case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                    return onItemClicked(virtualViewId);
                default:
                    return LockPatternView.PROFILE_DRAWING;
            }
        }

        boolean onItemClicked(int index) {
            invalidateVirtualView(index);
            sendEventForVirtualView(index, LockPatternView.VIRTUAL_BASE_VIEW_ID);
            return true;
        }

        private Rect getBoundsForVirtualView(int virtualViewId) {
            int ordinal = virtualViewId - 1;
            Rect bounds = this.mTempRect;
            int row = ordinal / 3;
            int col = ordinal % 3;
            CellState cell = this.this$0.mCellStates[row][col];
            float centerX = this.this$0.getCenterXForColumn(col);
            float centerY = this.this$0.getCenterYForRow(row);
            float cellheight = (this.this$0.mSquareHeight * this.this$0.mHitFactor) * 0.5f;
            float cellwidth = (this.this$0.mSquareWidth * this.this$0.mHitFactor) * 0.5f;
            bounds.left = (int) (centerX - cellwidth);
            bounds.right = (int) (centerX + cellwidth);
            bounds.top = (int) (centerY - cellheight);
            bounds.bottom = (int) (centerY + cellheight);
            return bounds;
        }

        private boolean shouldSpeakPassword() {
            boolean speakPassword = Secure.getIntForUser(this.this$0.mContext.getContentResolver(), "speak_password", LockPatternView.ASPECT_SQUARE, -3) != 0 ? true : LockPatternView.PROFILE_DRAWING;
            boolean isBluetoothA2dpOn = this.this$0.mAudioManager != null ? !this.this$0.mAudioManager.isWiredHeadsetOn() ? this.this$0.mAudioManager.isBluetoothA2dpOn() : true : LockPatternView.PROFILE_DRAWING;
            if (speakPassword) {
                return true;
            }
            return isBluetoothA2dpOn;
        }

        private CharSequence getTextForVirtualView(int virtualViewId) {
            Resources res = this.this$0.getResources();
            if (!shouldSpeakPassword()) {
                return res.getString(R.string.lockscreen_access_pattern_cell_added);
            }
            Object[] objArr = new Object[LockPatternView.VIRTUAL_BASE_VIEW_ID];
            objArr[LockPatternView.ASPECT_SQUARE] = Integer.valueOf(virtualViewId);
            return res.getString(R.string.lockscreen_access_pattern_cell_added_verbose, objArr);
        }

        private int getVirtualViewIdForHit(float x, float y) {
            int view = RtlSpacingHelper.UNDEFINED;
            int rowHit = this.this$0.getRowHit(y);
            if (rowHit < 0) {
                return RtlSpacingHelper.UNDEFINED;
            }
            int columnHit = this.this$0.getColumnHit(x);
            if (columnHit < 0) {
                return RtlSpacingHelper.UNDEFINED;
            }
            int dotId = ((rowHit * 3) + columnHit) + LockPatternView.VIRTUAL_BASE_VIEW_ID;
            if (this.this$0.mPatternDrawLookup[rowHit][columnHit]) {
                view = dotId;
            }
            return view;
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        private final int mDisplayMode;
        private final boolean mInStealthMode;
        private final boolean mInputEnabled;
        private final String mSerializedPattern;
        private final boolean mTactileFeedbackEnabled;

        /* renamed from: com.android.internal.widget.LockPatternView.SavedState.1 */
        static class AnonymousClass1 implements Creator<SavedState> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m27createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public /* bridge */ /* synthetic */ Object[] m28newArray(int size) {
                return newArray(size);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.LockPatternView.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.LockPatternView.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.LockPatternView.SavedState.<clinit>():void");
        }

        /* synthetic */ SavedState(Parcel in, SavedState savedState) {
            this(in);
        }

        /* synthetic */ SavedState(Parcelable superState, String serializedPattern, int displayMode, boolean inputEnabled, boolean inStealthMode, boolean tactileFeedbackEnabled, SavedState savedState) {
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
        this.mDrawingProfilingStarted = PROFILE_DRAWING;
        this.mPaint = new Paint();
        this.mPathPaint = new Paint();
        this.mPattern = new ArrayList(9);
        this.mPatternDrawLookup = (boolean[][]) Array.newInstance(Boolean.TYPE, new int[]{3, 3});
        this.mInProgressX = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        this.mInProgressY = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        this.mPatternDisplayMode = DisplayMode.Correct;
        this.mInputEnabled = true;
        this.mInStealthMode = PROFILE_DRAWING;
        this.mEnableHapticFeedback = true;
        this.mPatternInProgress = PROFILE_DRAWING;
        this.mHitFactor = 0.6f;
        this.mCurrentPath = new Path();
        this.mInvalidate = new Rect();
        this.mTmpInvalidateRect = new Rect();
        this.mInterpolator = new AccelerateInterpolator();
        this.mIsHwTheme = PROFILE_DRAWING;
        this.mLastCellCenterX = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        this.mLastCellCenterY = LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        this.mAlphaTransparent = LogPower.START_CHG_ROTATION;
        this.mPathColor = -3355444;
        this.mIsHwTheme = HwWidgetFactory.checkIsHwTheme(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LockPatternView);
        String aspect = a.getString(ASPECT_SQUARE);
        if ("square".equals(aspect)) {
            this.mAspect = ASPECT_SQUARE;
        } else if ("lock_width".equals(aspect)) {
            this.mAspect = VIRTUAL_BASE_VIEW_ID;
        } else if ("lock_height".equals(aspect)) {
            this.mAspect = ASPECT_LOCK_HEIGHT;
        } else {
            this.mAspect = ASPECT_SQUARE;
        }
        setClickable(true);
        this.mPathPaint.setAntiAlias(true);
        this.mPathPaint.setDither(true);
        this.mRegularColor = context.getColor(androidhwext.R.color.lock_pattern_view_regular_color_emui);
        this.mErrorColor = context.getColor(R.color.lock_pattern_view_error_color);
        this.mSuccessColor = context.getColor(androidhwext.R.color.lock_pattern_view_success_color_emui);
        this.mRegularColor = a.getColor(ASPECT_LOCK_HEIGHT, this.mRegularColor);
        this.mErrorColor = a.getColor(3, this.mErrorColor);
        this.mSuccessColor = a.getColor(4, this.mSuccessColor);
        this.mPathPaint.setColor(a.getColor(VIRTUAL_BASE_VIEW_ID, this.mRegularColor));
        this.mPathPaint.setStyle(Style.FILL);
        this.mPathPaint.setStrokeJoin(Join.ROUND);
        this.mPathPaint.setStrokeCap(Cap.ROUND);
        this.mPathPaint.setStrokeWidth(2.0f);
        this.mLineRadius = getResources().getDimensionPixelSize(androidhwext.R.dimen.lock_pattern_dot_line_width_emui);
        this.mDotCircleRadius = getResources().getDimensionPixelSize(androidhwext.R.dimen.lock_pattern_dot_circle_emui);
        this.mDotRadius = getResources().getDimensionPixelSize(androidhwext.R.dimen.lock_pattern_dot_size_emui);
        this.mDotRadiusActivated = getResources().getDimensionPixelSize(androidhwext.R.dimen.lock_pattern_dot_size_activated_emui);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mCellStates = (CellState[][]) Array.newInstance(CellState.class, new int[]{3, 3});
        for (int i = ASPECT_SQUARE; i < 3; i += VIRTUAL_BASE_VIEW_ID) {
            for (int j = ASPECT_SQUARE; j < 3; j += VIRTUAL_BASE_VIEW_ID) {
                this.mCellStates[i][j] = new CellState();
                this.mCellStates[i][j].radius = (float) this.mDotRadius;
                this.mCellStates[i][j].row = i;
                this.mCellStates[i][j].col = j;
            }
        }
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, R.interpolator.fast_out_slow_in);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, R.interpolator.linear_out_slow_in);
        this.mExploreByTouchHelper = new PatternExploreByTouchHelper(this, this);
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
            Cell first = (Cell) this.mPattern.get(ASPECT_SQUARE);
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
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{DRAG_THRESHHOLD, LayoutParams.BRIGHTNESS_OVERRIDE_FULL});
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.setInterpolator(interpolator);
        animator.addUpdateListener(new AnonymousClass1(cellState, startAlpha, endAlpha, startTranslationY, endTranslationY, startScale, endScale));
        animator.addListener(new AnonymousClass2(finishRunnable));
        animator.start();
    }

    private void startCellStateAnimationHw(CellState cellState, float startAlpha, float endAlpha, float startTranslationY, float endTranslationY, float startScale, float endScale, long delay, long duration, Interpolator interpolator, Runnable finishRunnable) {
        cellState.alpha = endAlpha;
        cellState.translationY = endTranslationY;
        cellState.radius = ((float) this.mDotRadius) * endScale;
        cellState.hwAnimating = true;
        cellState.hwCenterY = CanvasProperty.createFloat(getCenterYForRow(cellState.row) + startTranslationY);
        cellState.hwCenterX = CanvasProperty.createFloat(getCenterXForColumn(cellState.col));
        cellState.hwRadius = CanvasProperty.createFloat(((float) this.mDotRadius) * startScale);
        this.mPaint.setColor(getCurrentColor(PROFILE_DRAWING));
        this.mPaint.setAlpha((int) (255.0f * startAlpha));
        cellState.hwPaint = CanvasProperty.createPaint(new Paint(this.mPaint));
        startRtFloatAnimation(cellState.hwCenterY, getCenterYForRow(cellState.row) + endTranslationY, delay, duration, interpolator);
        startRtFloatAnimation(cellState.hwRadius, ((float) this.mDotRadius) * endScale, delay, duration, interpolator);
        startRtAlphaAnimation(cellState, endAlpha, delay, duration, interpolator, new AnonymousClass3(cellState, finishRunnable));
        invalidate();
    }

    private void startRtAlphaAnimation(CellState cellState, float endAlpha, long delay, long duration, Interpolator interpolator, AnimatorListener listener) {
        RenderNodeAnimator animator = new RenderNodeAnimator(cellState.hwPaint, VIRTUAL_BASE_VIEW_ID, (float) ((int) (255.0f * endAlpha)));
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
        for (int i = ASPECT_SQUARE; i < 3; i += VIRTUAL_BASE_VIEW_ID) {
            for (int j = ASPECT_SQUARE; j < 3; j += VIRTUAL_BASE_VIEW_ID) {
                this.mPatternDrawLookup[i][j] = PROFILE_DRAWING;
            }
        }
    }

    public void disableInput() {
        this.mInputEnabled = PROFILE_DRAWING;
    }

    public void enableInput() {
        this.mInputEnabled = true;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mSquareWidth = ((float) ((w - this.mPaddingLeft) - this.mPaddingRight)) / 3.0f;
        this.mSquareHeight = ((float) ((h - this.mPaddingTop) - this.mPaddingBottom)) / 3.0f;
        this.mExploreByTouchHelper.invalidateRoot();
        this.mWidth = (float) w;
        this.mHeight = (float) h;
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case RtlSpacingHelper.UNDEFINED /*-2147483648*/:
                return Math.max(specSize, desired);
            case ASPECT_SQUARE /*0*/:
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
            case ASPECT_SQUARE /*0*/:
                viewHeight = Math.min(viewWidth, viewHeight);
                viewWidth = viewHeight;
                break;
            case VIRTUAL_BASE_VIEW_ID /*1*/:
                viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_HEIGHT /*2*/:
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
            Cell lastCell = (Cell) pattern.get(pattern.size() - 1);
            int dRow = cell.row - lastCell.row;
            int dColumn = cell.column - lastCell.column;
            int fillInRow = lastCell.row;
            int fillInColumn = lastCell.column;
            if (Math.abs(dRow) == ASPECT_LOCK_HEIGHT && Math.abs(dColumn) != VIRTUAL_BASE_VIEW_ID) {
                fillInRow = lastCell.row + (dRow > 0 ? VIRTUAL_BASE_VIEW_ID : -1);
            }
            if (Math.abs(dColumn) == ASPECT_LOCK_HEIGHT && Math.abs(dRow) != VIRTUAL_BASE_VIEW_ID) {
                fillInColumn = lastCell.column + (dColumn > 0 ? VIRTUAL_BASE_VIEW_ID : -1);
            }
            fillInGapCell = Cell.of(fillInRow, fillInColumn);
        }
        if (!(fillInGapCell == null || this.mPatternDrawLookup[fillInGapCell.row][fillInGapCell.column])) {
            addCellToPattern(fillInGapCell);
        }
        if (this.mIsHwTheme && !this.mInStealthMode) {
            moveToTouchArea(cell, getCenterXForColumn(cell.getColumn()), getCenterYForRow(cell.getRow()), x, y, true);
        }
        addCellToPattern(cell);
        if (this.mEnableHapticFeedback) {
            performHapticFeedback(VIRTUAL_BASE_VIEW_ID, 3);
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
        CellState cellState = this.mCellStates[cell.row][cell.column];
        if (this.mIsHwTheme) {
            enlargeCellAnimation((float) this.mDotRadius, (float) this.mDotRadiusActivated, this.mLinearOutSlowInInterpolator, cellState);
            return;
        }
        startRadiusAnimation((float) this.mDotRadius, (float) this.mDotRadiusActivated, 96, this.mLinearOutSlowInInterpolator, cellState, new AnonymousClass4(cellState));
        startLineEndAnimation(cellState, this.mInProgressX, this.mInProgressY, getCenterXForColumn(cell.column), getCenterYForRow(cell.row));
    }

    private void startLineEndAnimation(CellState state, float startX, float startY, float targetX, float targetY) {
        float[] fArr = new float[ASPECT_LOCK_HEIGHT];
        fArr[ASPECT_SQUARE] = DRAG_THRESHHOLD;
        fArr[VIRTUAL_BASE_VIEW_ID] = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fArr);
        valueAnimator.addUpdateListener(new AnonymousClass5(state, startX, targetX, startY, targetY));
        valueAnimator.addListener(new AnonymousClass6(state));
        valueAnimator.setInterpolator(this.mFastOutSlowInInterpolator);
        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.start();
        state.lineAnimator = valueAnimator;
    }

    private void startRadiusAnimation(float start, float end, long duration, Interpolator interpolator, CellState state, Runnable endRunnable) {
        float[] fArr = new float[ASPECT_LOCK_HEIGHT];
        fArr[ASPECT_SQUARE] = start;
        fArr[VIRTUAL_BASE_VIEW_ID] = end;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fArr);
        valueAnimator.addUpdateListener(new AnonymousClass7(state));
        if (endRunnable != null) {
            valueAnimator.addListener(new AnonymousClass8(endRunnable));
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
        for (int i = ASPECT_SQUARE; i < 3; i += VIRTUAL_BASE_VIEW_ID) {
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
        for (int i = ASPECT_SQUARE; i < 3; i += VIRTUAL_BASE_VIEW_ID) {
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
                case HwCfgFilePolicy.CLOUD_APN /*7*/:
                    event.setAction(ASPECT_LOCK_HEIGHT);
                    break;
                case PGSdk.TYPE_SCRLOCK /*9*/:
                    event.setAction(ASPECT_SQUARE);
                    break;
                case PGSdk.TYPE_CLOCK /*10*/:
                    event.setAction(VIRTUAL_BASE_VIEW_ID);
                    break;
            }
            onTouchEvent(event);
            event.setAction(action);
        }
        return super.onHoverEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mInputEnabled || !isEnabled()) {
            return PROFILE_DRAWING;
        }
        switch (event.getAction()) {
            case ASPECT_SQUARE /*0*/:
                handleActionDown(event);
                return true;
            case VIRTUAL_BASE_VIEW_ID /*1*/:
                handleActionUp();
                return true;
            case ASPECT_LOCK_HEIGHT /*2*/:
                handleActionMove(event);
                return true;
            case HwCfgFilePolicy.BASE /*3*/:
                if (this.mPatternInProgress) {
                    setPatternInProgress(PROFILE_DRAWING);
                    resetPattern();
                    notifyPatternCleared();
                }
                return true;
            default:
                return PROFILE_DRAWING;
        }
    }

    private void setPatternInProgress(boolean progress) {
        this.mPatternInProgress = progress;
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void handleActionMove(MotionEvent event) {
        int historySize = event.getHistorySize();
        this.mTmpInvalidateRect.setEmpty();
        boolean invalidateNow = PROFILE_DRAWING;
        int i = ASPECT_SQUARE;
        while (i < historySize + VIRTUAL_BASE_VIEW_ID) {
            float x = i < historySize ? event.getHistoricalX(i) : event.getX();
            float y = i < historySize ? event.getHistoricalY(i) : event.getY();
            Cell hitCell = detectAndAddHit(x, y);
            int patternSize = this.mPattern.size();
            if (hitCell != null && patternSize == VIRTUAL_BASE_VIEW_ID) {
                setPatternInProgress(true);
                notifyPatternStarted();
            }
            float dx = Math.abs(x - this.mInProgressX);
            float dy = Math.abs(y - this.mInProgressY);
            if (dx > DRAG_THRESHHOLD || dy > DRAG_THRESHHOLD) {
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
                if (hitCell == null && this.mIsHwTheme && !this.mInStealthMode) {
                    lastCellAnimation(lastCell, x, y);
                }
                this.mTmpInvalidateRect.union(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
            }
            i += VIRTUAL_BASE_VIEW_ID;
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
            setPatternInProgress(PROFILE_DRAWING);
            cancelLineAnimations();
            notifyPatternDetected();
            invalidate();
        }
    }

    private void cancelLineAnimations() {
        for (int i = ASPECT_SQUARE; i < 3; i += VIRTUAL_BASE_VIEW_ID) {
            for (int j = ASPECT_SQUARE; j < 3; j += VIRTUAL_BASE_VIEW_ID) {
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
            setPatternInProgress(PROFILE_DRAWING);
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
        ArrayList<Cell> pattern = this.mPattern;
        int count = pattern.size();
        boolean[][] drawLookup = this.mPatternDrawLookup;
        if (this.mPatternDisplayMode == DisplayMode.Animate) {
            int spotInCycle = ((int) (SystemClock.elapsedRealtime() - this.mAnimatingPeriodStart)) % ((count + VIRTUAL_BASE_VIEW_ID) * MILLIS_PER_CIRCLE_ANIMATING);
            int numCircles = spotInCycle / MILLIS_PER_CIRCLE_ANIMATING;
            clearPatternDrawLookup();
            for (i = ASPECT_SQUARE; i < numCircles; i += VIRTUAL_BASE_VIEW_ID) {
                Cell cell = (Cell) pattern.get(i);
                drawLookup[cell.getRow()][cell.getColumn()] = true;
            }
            boolean needToUpdateInProgressPoint = numCircles > 0 ? numCircles < count ? true : PROFILE_DRAWING : PROFILE_DRAWING;
            if (needToUpdateInProgressPoint) {
                float percentageOfNextCircle = ((float) (spotInCycle % MILLIS_PER_CIRCLE_ANIMATING)) / 700.0f;
                Cell currentCell = (Cell) pattern.get(numCircles - 1);
                float centerX = getCenterXForColumn(currentCell.column);
                float centerY = getCenterYForRow(currentCell.row);
                Cell nextCell = (Cell) pattern.get(numCircles);
                float dy = percentageOfNextCircle * (getCenterYForRow(nextCell.row) - centerY);
                this.mInProgressX = centerX + (percentageOfNextCircle * (getCenterXForColumn(nextCell.column) - centerX));
                this.mInProgressY = centerY + dy;
            }
            invalidate();
        }
        Path currentPath = this.mCurrentPath;
        currentPath.rewind();
        i = ASPECT_SQUARE;
        while (i < 3) {
            int j = ASPECT_SQUARE;
            while (j < 3) {
                CellState cellState = this.mCellStates[i][j];
                centerY = getCenterYForRow(i);
                centerX = getCenterXForColumn(j);
                if (!this.mInStealthMode && this.mIsHwTheme && isLastCell(Cell.of(i, j))) {
                    centerX = this.mLastCellCenterX;
                    centerY = this.mLastCellCenterY;
                }
                float translationY = cellState.translationY;
                if (isHardwareAccelerated() && cellState.hwAnimating) {
                    ((DisplayListCanvas) canvas).drawCircle(cellState.hwCenterX, cellState.hwCenterY, cellState.hwRadius, cellState.hwPaint);
                } else {
                    drawCircle(canvas, (float) ((int) centerX), ((float) ((int) centerY)) + translationY, cellState.radius, drawLookup[i][j], cellState.alpha);
                }
                j += VIRTUAL_BASE_VIEW_ID;
            }
            i += VIRTUAL_BASE_VIEW_ID;
        }
        if (!(this.mInStealthMode ? PROFILE_DRAWING : true)) {
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
        boolean anyCircles = PROFILE_DRAWING;
        float lastX = DRAG_THRESHHOLD;
        float lastY = DRAG_THRESHHOLD;
        for (i = ASPECT_SQUARE; i < count; i += VIRTUAL_BASE_VIEW_ID) {
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
        return Math.min(LayoutParams.BRIGHTNESS_OVERRIDE_FULL, Math.max(DRAG_THRESHHOLD, ((((float) Math.sqrt((double) ((diffX * diffX) + (diffY * diffY)))) / this.mSquareWidth) - 0.3f) * 4.0f));
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

    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), LockPatternUtils.patternToString(this.mPattern), this.mPatternDisplayMode.ordinal(), this.mInputEnabled, this.mInStealthMode, this.mEnableHapticFeedback, null);
    }

    protected void onRestoreInstanceState(Parcelable state) {
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
            return PROFILE_DRAWING;
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
            CellState cellState = this.mCellStates[row][column];
            if (cellState.moveAnimator != null) {
                cellState.moveAnimator.cancel();
            }
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{DRAG_THRESHHOLD, LayoutParams.BRIGHTNESS_OVERRIDE_FULL});
            valueAnimator.addUpdateListener(new AnonymousClass9(currCenterX, touchX, currCenterY, touchY, row, column));
            valueAnimator.addListener(new AnonymousClass10(cellState));
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
        float centerX = getCenterXForColumn(column);
        float centerY = getCenterYForRow(row);
        CellState cellState = this.mCellStates[row][column];
        float[] fArr = new float[ASPECT_LOCK_HEIGHT];
        fArr[ASPECT_SQUARE] = DRAG_THRESHHOLD;
        fArr[VIRTUAL_BASE_VIEW_ID] = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fArr);
        valueAnimator.addUpdateListener(new AnonymousClass11(currentX, centerX, currentY, centerY, cellState));
        valueAnimator.addListener(new AnonymousClass12(cellState));
        valueAnimator.setInterpolator(this.mInterpolator);
        valueAnimator.setDuration(ANIM_DURATION);
        if (cellState.moveAnimator == null) {
            valueAnimator.start();
            this.mCellStates[row][column].moveAnimator = valueAnimator;
        }
    }

    private void enlargeCellAnimation(float start, float end, Interpolator interpolator, CellState cellState) {
        float[] fArr = new float[ASPECT_LOCK_HEIGHT];
        fArr[ASPECT_SQUARE] = start;
        fArr[VIRTUAL_BASE_VIEW_ID] = end;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fArr);
        valueAnimator.addUpdateListener(new AnonymousClass13(cellState));
        valueAnimator.addListener(new AnonymousClass14(cellState));
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.setDuration(ANIM_DURATION);
        valueAnimator.start();
        cellState.lineAnimator = valueAnimator;
    }

    private void drawHwPath(ArrayList<Cell> pattern, boolean[][] drawLookup, Path currentPath, Canvas canvas) {
        int count = pattern.size();
        boolean anyCircles = PROFILE_DRAWING;
        float lastX = DRAG_THRESHHOLD;
        float lastY = DRAG_THRESHHOLD;
        for (int i = ASPECT_SQUARE; i < count; i += VIRTUAL_BASE_VIEW_ID) {
            Cell cell = (Cell) pattern.get(i);
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
            if (cell == null || !isLastCell(cell)) {
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
        if (count == VIRTUAL_BASE_VIEW_ID) {
            currentPath.addCircle(this.mLastCellCenterX, this.mLastCellCenterY, (float) this.mDotCircleRadius, Direction.CCW);
        }
        canvas.drawPath(currentPath, this.mPathPaint);
    }

    private void connectTwoCells(Path currentPath, float radius, float startX, float startY, float endX, float endY) {
        double midAngle = Math.atan(0.2d);
        double midRadius = Math.sqrt(((double) (this.mLineRadius * this.mLineRadius)) + ((((double) radius) * 1.6d) * (((double) radius) * 1.6d)));
        double baseAngle = Math.abs(startX - endX) < LayoutParams.BRIGHTNESS_OVERRIDE_FULL ? endY < startY ? 1.5707963267948966d : -1.5707963267948966d : -Math.atan2((double) (endY - startY), (double) (endX - startX));
        currentPath.moveTo((float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
        currentPath.arcTo(startX - radius, startY - radius, startX + radius, startY + radius, (float) Math.toDegrees((-baseAngle) + 0.7853981633974483d), 270.0f, PROFILE_DRAWING);
        currentPath.quadTo((float) (((double) startX) + (((double) (1.05f * radius)) * Math.cos(0.39269908169872414d + baseAngle))), (float) (((double) startY) - (((double) (1.05f * radius)) * Math.sin(0.39269908169872414d + baseAngle))), (float) (((double) startX) + (Math.cos(baseAngle + midAngle) * midRadius)), (float) (((double) startY) - (Math.sin(baseAngle + midAngle) * midRadius)));
        currentPath.lineTo((float) (((double) endX) + (Math.cos((baseAngle - midAngle) + 3.141592653589793d) * midRadius)), (float) (((double) endY) - (Math.sin((baseAngle - midAngle) + 3.141592653589793d) * midRadius)));
        currentPath.lineTo((float) (((double) endX) + (Math.cos((3.141592653589793d + baseAngle) + midAngle) * midRadius)), (float) (((double) endY) - (Math.sin((3.141592653589793d + baseAngle) + midAngle) * midRadius)));
        currentPath.lineTo((float) (((double) startX) + (Math.cos(baseAngle - midAngle) * midRadius)), (float) (((double) startY) - (Math.sin(baseAngle - midAngle) * midRadius)));
        currentPath.quadTo((float) (((double) startX) + (((double) (1.05f * radius)) * Math.cos(baseAngle - 0.39269908169872414d))), (float) (((double) startY) - (((double) (1.05f * radius)) * Math.sin(baseAngle - 0.39269908169872414d))), (float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
        currentPath.moveTo((float) (((double) endX) + (Math.cos((3.141592653589793d + baseAngle) + midAngle) * midRadius)), (float) (((double) endY) - (Math.sin((3.141592653589793d + baseAngle) + midAngle) * midRadius)));
        currentPath.arcTo(endX - radius, endY - radius, endX + radius, endY + radius, (float) Math.toDegrees((3.141592653589793d - baseAngle) + 0.7853981633974483d), 270.0f, PROFILE_DRAWING);
        currentPath.quadTo((float) (((double) endX) + (((double) (1.05f * radius)) * Math.cos((3.141592653589793d + baseAngle) + 0.39269908169872414d))), (float) (((double) endY) - (((double) (1.05f * radius)) * Math.sin((3.141592653589793d + baseAngle) + 0.39269908169872414d))), (float) (((double) endX) + (Math.cos((3.141592653589793d + baseAngle) + midAngle) * midRadius)), (float) (((double) endY) - (Math.sin((3.141592653589793d + baseAngle) + midAngle) * midRadius)));
        currentPath.lineTo((float) (((double) endX) + (Math.cos((baseAngle - midAngle) + 3.141592653589793d) * midRadius)), (float) (((double) endY) - (Math.sin((baseAngle - midAngle) + 3.141592653589793d) * midRadius)));
        currentPath.quadTo((float) (((double) endX) + (((double) (1.05f * radius)) * Math.cos((3.141592653589793d + baseAngle) - 0.39269908169872414d))), (float) (((double) endY) - (((double) (1.05f * radius)) * Math.sin((3.141592653589793d + baseAngle) - 0.39269908169872414d))), (float) (((double) endX) + (((double) radius) * Math.cos((3.141592653589793d + baseAngle) - 0.7853981633974483d))), (float) (((double) endY) - (((double) radius) * Math.sin((3.141592653589793d + baseAngle) - 0.7853981633974483d))));
        invalidate();
    }

    private void connectCellToPoint(Path currentPath, float radius, float startX, float startY, float endX, float endY) {
        double midAngle = Math.atan(0.2d);
        double midRadius = Math.sqrt(((double) (this.mLineRadius * this.mLineRadius)) + ((((double) radius) * 1.6d) * (((double) radius) * 1.6d)));
        float distance = (float) Math.hypot((double) (endX - startX), (double) (endY - startY));
        double baseAngle = Math.abs(startX - endX) < LayoutParams.BRIGHTNESS_OVERRIDE_FULL ? endY < startY ? 1.5707963267948966d : -1.5707963267948966d : -Math.atan2((double) (endY - startY), (double) (endX - startX));
        float endCenterX = (float) (((double) endX) + (((((double) (radius / 2.0f)) * Math.cos(1.5707963267948966d + baseAngle)) + (((double) (radius / 2.0f)) * Math.cos(baseAngle - 1.5707963267948966d))) / 2.0d));
        float endCenterY = (float) (((double) endY) - (((((double) (radius / 2.0f)) * Math.sin(1.5707963267948966d + baseAngle)) + (((double) (radius / 2.0f)) * Math.sin(baseAngle - 1.5707963267948966d))) / 2.0d));
        if (distance > 2.0f * radius) {
            currentPath.moveTo((float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
            currentPath.arcTo(startX - radius, startY - radius, startX + radius, startY + radius, (float) Math.toDegrees((-baseAngle) + 0.7853981633974483d), -90.0f, PROFILE_DRAWING);
            currentPath.quadTo((float) (((double) startX) + (((double) (1.05f * radius)) * Math.cos(0.39269908169872414d + baseAngle))), (float) (((double) startY) - (((double) (1.05f * radius)) * Math.sin(0.39269908169872414d + baseAngle))), (float) (((double) startX) + (Math.cos(baseAngle + midAngle) * midRadius)), (float) (((double) startY) - (Math.sin(baseAngle + midAngle) * midRadius)));
            currentPath.lineTo((float) (((double) endX) + (((double) this.mLineRadius) * Math.cos(1.5707963267948966d + baseAngle))), (float) (((double) endY) - (((double) this.mLineRadius) * Math.sin(1.5707963267948966d + baseAngle))));
            currentPath.lineTo((float) (((double) endX) + (((double) this.mLineRadius) * Math.cos(baseAngle - 1.5707963267948966d))), (float) (((double) endY) - (((double) this.mLineRadius) * Math.sin(baseAngle - 1.5707963267948966d))));
            currentPath.lineTo((float) (((double) startX) + (Math.cos(baseAngle - midAngle) * midRadius)), (float) (((double) startY) - (Math.sin(baseAngle - midAngle) * midRadius)));
            currentPath.quadTo((float) (((double) startX) + (((double) (1.05f * radius)) * Math.cos(baseAngle - 0.39269908169872414d))), (float) (((double) startY) - (((double) (1.05f * radius)) * Math.sin(baseAngle - 0.39269908169872414d))), (float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
            currentPath.addArc(endCenterX - ((float) this.mLineRadius), endCenterY - ((float) this.mLineRadius), endCenterX + ((float) this.mLineRadius), endCenterY + ((float) this.mLineRadius), (float) Math.toDegrees((-baseAngle) - 1.5707963267948966d), 180.0f);
        } else if (distance > radius) {
            currentPath.moveTo((float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
            currentPath.arcTo(startX - radius, startY - radius, startX + radius, startY + radius, (float) Math.toDegrees((-baseAngle) + 0.7853981633974483d), -90.0f, PROFILE_DRAWING);
            currentPath.quadTo((float) (((double) startX) + (((double) radius) * Math.cos(0.39269908169872414d + baseAngle))), (float) (((double) startY) - (((double) radius) * Math.sin(0.39269908169872414d + baseAngle))), (float) (((double) endX) + (((double) this.mLineRadius) * Math.cos(1.5707963267948966d + baseAngle))), (float) (((double) endY) - (((double) this.mLineRadius) * Math.sin(1.5707963267948966d + baseAngle))));
            currentPath.lineTo((float) (((double) endX) + (((double) this.mLineRadius) * Math.cos(baseAngle - 1.5707963267948966d))), (float) (((double) endY) - (((double) this.mLineRadius) * Math.sin(baseAngle - 1.5707963267948966d))));
            currentPath.quadTo((float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.39269908169872414d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.39269908169872414d))), (float) (((double) startX) + (((double) radius) * Math.cos(baseAngle - 0.7853981633974483d))), (float) (((double) startY) - (((double) radius) * Math.sin(baseAngle - 0.7853981633974483d))));
            currentPath.addArc(endCenterX - ((float) this.mLineRadius), endCenterY - ((float) this.mLineRadius), endCenterX + ((float) this.mLineRadius), endCenterY + ((float) this.mLineRadius), (float) Math.toDegrees((-baseAngle) - 1.5707963267948966d), 180.0f);
        }
        invalidate();
    }

    private void resetCellRadius() {
        for (int i = ASPECT_SQUARE; i < 3; i += VIRTUAL_BASE_VIEW_ID) {
            for (int j = ASPECT_SQUARE; j < 3; j += VIRTUAL_BASE_VIEW_ID) {
                this.mCellStates[i][j].radius = (float) this.mDotRadius;
            }
        }
    }

    private void lastCellAnimation(Cell lastCell, float x, float y) {
        Cell touchedCell = touchACell(x, y);
        float currCenterX = this.mLastCellCenterX;
        float curreCenterY = this.mLastCellCenterY;
        CellState cellState = this.mCellStates[lastCell.getRow()][lastCell.getColumn()];
        boolean isAtCenter = Math.abs(this.mLastCellCenterX - getCenterXForColumn(lastCell.getColumn())) < 0.01f ? Math.abs(this.mLastCellCenterY - getCenterYForRow(lastCell.getRow())) < 0.01f ? true : PROFILE_DRAWING : PROFILE_DRAWING;
        if (touchedCell != null && isLastCell(touchedCell)) {
            moveToTouchArea(touchedCell, currCenterX, curreCenterY, x, y, PROFILE_DRAWING);
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
