package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hwcontrol.HwWidgetFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.StrictMode;
import android.os.StrictMode.Span;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewHierarchyEncoder;
import android.view.ViewParent;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Protocol;
import com.hisi.perfhub.PerfHub;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.util.List;

public class ScrollView extends FrameLayout {
    static final int ANIMATED_SCROLL_GAP = 250;
    private static final int INVALID_POINTER = -1;
    static final float MAX_SCROLL_FACTOR = 0.5f;
    private static final String TAG = "ScrollView";
    private int mActivePointerId;
    private View mChildToScrollTo;
    private EdgeEffect mEdgeGlowBottom;
    private EdgeEffect mEdgeGlowTop;
    @ExportedProperty(category = "layout")
    private boolean mFillViewport;
    private Span mFlingStrictSpan;
    private boolean mIsBeingDragged;
    private boolean mIsHwTheme;
    private boolean mIsLayoutDirty;
    private int mLastMotionY;
    private long mLastScroll;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private int mNestedYOffset;
    private int mOverflingDistance;
    private int mOverscrollDistance;
    private SavedState mSavedState;
    private final int[] mScrollConsumed;
    private final int[] mScrollOffset;
    private Span mScrollStrictSpan;
    private OverScroller mScroller;
    private boolean mSmoothScrollingEnabled;
    private final Rect mTempRect;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        public int scrollPosition;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.ScrollView.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.ScrollView.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ScrollView.SavedState.<clinit>():void");
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            this.scrollPosition = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.scrollPosition);
        }

        public String toString() {
            return "HorizontalScrollView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " scrollPosition=" + this.scrollPosition + "}";
        }
    }

    public ScrollView(Context context) {
        this(context, null);
    }

    public ScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.scrollViewStyle);
    }

    public ScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTempRect = new Rect();
        this.mIsLayoutDirty = true;
        this.mChildToScrollTo = null;
        this.mIsBeingDragged = false;
        this.mSmoothScrollingEnabled = true;
        this.mActivePointerId = INVALID_POINTER;
        this.mScrollOffset = new int[2];
        this.mScrollConsumed = new int[2];
        this.mScrollStrictSpan = null;
        this.mFlingStrictSpan = null;
        initScrollView();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollView, defStyleAttr, defStyleRes);
        setFillViewport(a.getBoolean(0, false));
        a.recycle();
        this.mIsHwTheme = HwWidgetFactory.checkIsHwTheme(context, attrs);
        if (this.mIsHwTheme) {
            setOverScrollMode(0);
        }
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    protected float getTopFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }
        int length = getVerticalFadingEdgeLength();
        if (this.mScrollY < length) {
            return ((float) this.mScrollY) / ((float) length);
        }
        return LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
    }

    protected float getBottomFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }
        int length = getVerticalFadingEdgeLength();
        int span = (getChildAt(0).getBottom() - this.mScrollY) - (getHeight() - this.mPaddingBottom);
        if (span < length) {
            return ((float) span) / ((float) length);
        }
        return LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
    }

    public int getMaxScrollAmount() {
        return (int) (((float) (this.mBottom - this.mTop)) * MAX_SCROLL_FACTOR);
    }

    private void initScrollView() {
        this.mScroller = new OverScroller(getContext());
        setFocusable(true);
        setDescendantFocusability(Protocol.BASE_DATA_CONNECTION);
        setWillNotDraw(false);
        ViewConfiguration configuration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mOverscrollDistance = configuration.getScaledOverscrollDistance();
        this.mOverflingDistance = configuration.getScaledOverflingDistance();
    }

    public void addView(View child) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
        super.addView(child);
    }

    public void addView(View child, int index) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
        super.addView(child, index);
    }

    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
        super.addView(child, params);
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }
        super.addView(child, index, params);
    }

    private boolean canScroll() {
        boolean z = false;
        View child = getChildAt(0);
        if (child == null) {
            return false;
        }
        if (getHeight() < (this.mPaddingTop + child.getHeight()) + this.mPaddingBottom) {
            z = true;
        }
        return z;
    }

    public boolean isFillViewport() {
        return this.mFillViewport;
    }

    public void setFillViewport(boolean fillViewport) {
        if (fillViewport != this.mFillViewport) {
            this.mFillViewport = fillViewport;
            requestLayout();
        }
    }

    public boolean isSmoothScrollingEnabled() {
        return this.mSmoothScrollingEnabled;
    }

    public void setSmoothScrollingEnabled(boolean smoothScrollingEnabled) {
        this.mSmoothScrollingEnabled = smoothScrollingEnabled;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mFillViewport && MeasureSpec.getMode(heightMeasureSpec) != 0 && getChildCount() > 0) {
            int widthPadding;
            int heightPadding;
            View child = getChildAt(0);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
            if (getContext().getApplicationInfo().targetSdkVersion >= 23) {
                widthPadding = ((this.mPaddingLeft + this.mPaddingRight) + lp.leftMargin) + lp.rightMargin;
                heightPadding = ((this.mPaddingTop + this.mPaddingBottom) + lp.topMargin) + lp.bottomMargin;
            } else {
                widthPadding = this.mPaddingLeft + this.mPaddingRight;
                heightPadding = this.mPaddingTop + this.mPaddingBottom;
            }
            int desiredHeight = getMeasuredHeight() - heightPadding;
            if (child.getMeasuredHeight() < desiredHeight) {
                child.measure(ViewGroup.getChildMeasureSpec(widthMeasureSpec, widthPadding, lp.width), MeasureSpec.makeMeasureSpec(desiredHeight, EditorInfo.IME_FLAG_NO_ENTER_ACTION));
            }
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return !super.dispatchKeyEvent(event) ? executeKeyEvent(event) : true;
    }

    public boolean executeKeyEvent(KeyEvent event) {
        boolean z = false;
        this.mTempRect.setEmpty();
        if (canScroll()) {
            boolean handled = false;
            if (event.getAction() == 0) {
                switch (event.getKeyCode()) {
                    case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
                        if (!event.isAltPressed()) {
                            handled = arrowScroll(33);
                            break;
                        }
                        handled = fullScroll(33);
                        break;
                    case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
                        if (!event.isAltPressed()) {
                            handled = arrowScroll(LogPower.END_CHG_ROTATION);
                            break;
                        }
                        handled = fullScroll(LogPower.END_CHG_ROTATION);
                        break;
                    case RILConstants.RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION /*62*/:
                        pageScroll(event.isShiftPressed() ? 33 : LogPower.END_CHG_ROTATION);
                        break;
                }
            }
            return handled;
        } else if (!isFocused() || event.getKeyCode() == 4) {
            return false;
        } else {
            View currentFocused = findFocus();
            if (currentFocused == this) {
                currentFocused = null;
            }
            View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, LogPower.END_CHG_ROTATION);
            if (!(nextFocused == null || nextFocused == this)) {
                z = nextFocused.requestFocus(LogPower.END_CHG_ROTATION);
            }
            return z;
        }
    }

    private boolean inChild(int x, int y) {
        boolean z = false;
        if (getChildCount() <= 0) {
            return false;
        }
        int scrollY = this.mScrollY;
        View child = getChildAt(0);
        if (y >= child.getTop() - scrollY && y < child.getBottom() - scrollY && x >= child.getLeft() && x < child.getRight()) {
            z = true;
        }
        return z;
    }

    private void initOrResetVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean z = false;
        int action = ev.getAction();
        if ((action == 2 && this.mIsBeingDragged) || super.onInterceptTouchEvent(ev)) {
            return true;
        }
        if (getScrollY() == 0 && !canScrollVertically(1)) {
            return false;
        }
        int y;
        switch (action & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
                y = (int) ev.getY();
                if (!inChild((int) ev.getX(), y)) {
                    this.mIsBeingDragged = false;
                    recycleVelocityTracker();
                    break;
                }
                this.mLastMotionY = y;
                this.mActivePointerId = ev.getPointerId(0);
                initOrResetVelocityTracker();
                this.mVelocityTracker.addMovement(ev);
                this.mScroller.computeScrollOffset();
                if (!this.mScroller.isFinished()) {
                    z = true;
                }
                this.mIsBeingDragged = z;
                if (this.mIsBeingDragged && this.mScrollStrictSpan == null) {
                    this.mScrollStrictSpan = StrictMode.enterCriticalSpan("ScrollView-scroll");
                }
                startNestedScroll(2);
                break;
            case HwCfgFilePolicy.EMUI /*1*/:
            case HwCfgFilePolicy.BASE /*3*/:
                this.mIsBeingDragged = false;
                this.mActivePointerId = INVALID_POINTER;
                recycleVelocityTracker();
                if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                    postInvalidateOnAnimation();
                }
                stopNestedScroll();
                break;
            case HwCfgFilePolicy.PC /*2*/:
                int activePointerId = this.mActivePointerId;
                if (activePointerId != INVALID_POINTER) {
                    int pointerIndex = ev.findPointerIndex(activePointerId);
                    if (pointerIndex != INVALID_POINTER) {
                        y = (int) ev.getY(pointerIndex);
                        if (Math.abs(y - this.mLastMotionY) > this.mTouchSlop && (getNestedScrollAxes() & 2) == 0) {
                            this.mIsBeingDragged = true;
                            this.mLastMotionY = y;
                            initVelocityTrackerIfNotExists();
                            this.mVelocityTracker.addMovement(ev);
                            this.mNestedYOffset = 0;
                            if (this.mScrollStrictSpan == null) {
                                this.mScrollStrictSpan = StrictMode.enterCriticalSpan("ScrollView-scroll");
                            }
                            ViewParent parent = getParent();
                            if (parent != null) {
                                parent.requestDisallowInterceptTouchEvent(true);
                                break;
                            }
                        }
                    }
                    Log.e(TAG, "Invalid pointerId=" + activePointerId + " in onInterceptTouchEvent");
                    break;
                }
                break;
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                onSecondaryPointerUp(ev);
                break;
        }
        return this.mIsBeingDragged;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        initVelocityTrackerIfNotExists();
        MotionEvent vtev = MotionEvent.obtain(ev);
        int actionMasked = ev.getActionMasked();
        if (actionMasked == 0) {
            this.mNestedYOffset = 0;
        }
        vtev.offsetLocation(0.0f, (float) this.mNestedYOffset);
        ViewParent parent;
        int index;
        switch (actionMasked) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
                if (getChildCount() != 0) {
                    boolean z = !this.mScroller.isFinished();
                    this.mIsBeingDragged = z;
                    if (z) {
                        parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    if (!this.mScroller.isFinished()) {
                        this.mScroller.abortAnimation();
                        if (this.mFlingStrictSpan != null) {
                            this.mFlingStrictSpan.finish();
                            this.mFlingStrictSpan = null;
                        }
                    }
                    this.mLastMotionY = (int) ev.getY();
                    this.mActivePointerId = ev.getPointerId(0);
                    startNestedScroll(2);
                    break;
                }
                return false;
            case HwCfgFilePolicy.EMUI /*1*/:
                if (this.mIsBeingDragged) {
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, (float) this.mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                    if (Math.abs(initialVelocity) > this.mMinimumVelocity) {
                        flingWithNestedDispatch(-initialVelocity);
                    } else {
                        if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                            postInvalidateOnAnimation();
                        }
                    }
                    this.mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                if (this.mIsHwTheme && ((isTopEdge() || isBottomEdge()) && !this.mScroller.isOverScrolled())) {
                    if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                        postInvalidateOnAnimation();
                    }
                    awakenScrollBars();
                    break;
                }
            case HwCfgFilePolicy.PC /*2*/:
                int activePointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (activePointerIndex != INVALID_POINTER) {
                    int y = (int) ev.getY(activePointerIndex);
                    int deltaY = this.mLastMotionY - y;
                    if (dispatchNestedPreScroll(0, deltaY, this.mScrollConsumed, this.mScrollOffset)) {
                        deltaY -= this.mScrollConsumed[1];
                        vtev.offsetLocation(0.0f, (float) this.mScrollOffset[1]);
                        this.mNestedYOffset += this.mScrollOffset[1];
                    }
                    if (!this.mIsBeingDragged && Math.abs(deltaY) > this.mTouchSlop) {
                        parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                        this.mIsBeingDragged = true;
                        if (deltaY > 0) {
                            deltaY -= this.mTouchSlop;
                        } else {
                            deltaY += this.mTouchSlop;
                        }
                    }
                    if (this.mIsBeingDragged) {
                        this.mLastMotionY = y - this.mScrollOffset[1];
                        int oldY = this.mScrollY;
                        int range = getScrollRange();
                        int overscrollMode = getOverScrollMode();
                        boolean canOverscroll = overscrollMode != 0 ? overscrollMode == 1 && range > 0 : true;
                        if (overScrollBy(0, deltaY, 0, this.mScrollY, 0, range, 0, this.mOverscrollDistance, true) && !hasNestedScrollingParent()) {
                            this.mVelocityTracker.clear();
                        }
                        int scrolledDeltaY = this.mScrollY - oldY;
                        if (!dispatchNestedScroll(0, scrolledDeltaY, 0, deltaY - scrolledDeltaY, this.mScrollOffset)) {
                            if (canOverscroll) {
                                int pulledToY = oldY + deltaY;
                                if (pulledToY < 0) {
                                    this.mEdgeGlowTop.onPull(((float) deltaY) / ((float) getHeight()), ev.getX(activePointerIndex) / ((float) getWidth()));
                                    if (!this.mEdgeGlowBottom.isFinished()) {
                                        this.mEdgeGlowBottom.onRelease();
                                    }
                                } else if (pulledToY > range) {
                                    this.mEdgeGlowBottom.onPull(((float) deltaY) / ((float) getHeight()), LayoutParams.BRIGHTNESS_OVERRIDE_FULL - (ev.getX(activePointerIndex) / ((float) getWidth())));
                                    if (!this.mEdgeGlowTop.isFinished()) {
                                        this.mEdgeGlowTop.onRelease();
                                    }
                                }
                                if (!(this.mEdgeGlowTop == null || (this.mEdgeGlowTop.isFinished() && this.mEdgeGlowBottom.isFinished()))) {
                                    postInvalidateOnAnimation();
                                    break;
                                }
                            }
                        }
                        this.mLastMotionY -= this.mScrollOffset[1];
                        vtev.offsetLocation(0.0f, (float) this.mScrollOffset[1]);
                        this.mNestedYOffset += this.mScrollOffset[1];
                        break;
                    }
                }
                Log.e(TAG, "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                break;
                break;
            case HwCfgFilePolicy.BASE /*3*/:
                if (this.mIsBeingDragged && getChildCount() > 0) {
                    if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                        postInvalidateOnAnimation();
                    }
                    this.mActivePointerId = INVALID_POINTER;
                    endDrag();
                    break;
                }
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                index = ev.getActionIndex();
                if (index != INVALID_POINTER) {
                    this.mLastMotionY = (int) ev.getY(index);
                    this.mActivePointerId = ev.getPointerId(index);
                    break;
                }
                Log.e(TAG, "Invalid index=" + index + " in onTouchEvent");
                break;
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                onSecondaryPointerUp(ev);
                index = ev.findPointerIndex(this.mActivePointerId);
                if (index != INVALID_POINTER) {
                    this.mLastMotionY = (int) ev.getY(index);
                    break;
                }
                Log.e(TAG, "Invalid index=" + index + " in onTouchEvent");
                break;
        }
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int newPointerIndex = 0;
        int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> 8;
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            if (pointerIndex == 0) {
                newPointerIndex = 1;
            }
            this.mLastMotionY = (int) ev.getY(newPointerIndex);
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.clear();
            }
        }
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & 2) != 0) {
            switch (event.getAction()) {
                case PGSdk.TYPE_VIDEO /*8*/:
                    if (!this.mIsBeingDragged) {
                        float vscroll = event.getAxisValue(9);
                        if (vscroll != 0.0f) {
                            int delta = (int) (getVerticalScrollFactor() * vscroll);
                            int range = getScrollRange();
                            int oldScrollY = this.mScrollY;
                            int newScrollY = oldScrollY - delta;
                            if (newScrollY < 0) {
                                newScrollY = 0;
                            } else if (newScrollY > range) {
                                newScrollY = range;
                            }
                            if (newScrollY != oldScrollY) {
                                super.scrollTo(this.mScrollX, newScrollY);
                                return true;
                            }
                        }
                    }
                    break;
            }
        }
        return super.onGenericMotionEvent(event);
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (this.mScroller.isFinished()) {
            super.scrollTo(scrollX, scrollY);
        } else {
            int oldX = this.mScrollX;
            int oldY = this.mScrollY;
            this.mScrollX = scrollX;
            this.mScrollY = scrollY;
            invalidateParentIfNeeded();
            onScrollChanged(this.mScrollX, this.mScrollY, oldX, oldY);
            if (clampedY) {
                this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange());
            }
        }
        awakenScrollBars();
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (!isEnabled()) {
            return false;
        }
        int targetScrollY;
        switch (action) {
            case HwPerformance.PERF_EVENT_RAW_REQ /*4096*/:
            case R.id.accessibilityActionScrollDown /*16908346*/:
                targetScrollY = Math.min(this.mScrollY + ((getHeight() - this.mPaddingBottom) - this.mPaddingTop), getScrollRange());
                if (targetScrollY == this.mScrollY) {
                    return false;
                }
                smoothScrollTo(0, targetScrollY);
                return true;
            case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD /*8192*/:
            case R.id.accessibilityActionScrollUp /*16908344*/:
                targetScrollY = Math.max(this.mScrollY - ((getHeight() - this.mPaddingBottom) - this.mPaddingTop), 0);
                if (targetScrollY == this.mScrollY) {
                    return false;
                }
                smoothScrollTo(0, targetScrollY);
                return true;
            default:
                return false;
        }
    }

    public CharSequence getAccessibilityClassName() {
        return ScrollView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (isEnabled()) {
            int scrollRange = getScrollRange();
            if (scrollRange > 0) {
                info.setScrollable(true);
                if (this.mScrollY > 0) {
                    info.addAction(AccessibilityAction.ACTION_SCROLL_BACKWARD);
                    info.addAction(AccessibilityAction.ACTION_SCROLL_UP);
                }
                if (this.mScrollY < scrollRange) {
                    info.addAction(AccessibilityAction.ACTION_SCROLL_FORWARD);
                    info.addAction(AccessibilityAction.ACTION_SCROLL_DOWN);
                }
            }
        }
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        boolean scrollable = false;
        super.onInitializeAccessibilityEventInternal(event);
        if (getScrollRange() > 0) {
            scrollable = true;
        }
        event.setScrollable(scrollable);
        event.setScrollX(this.mScrollX);
        event.setScrollY(this.mScrollY);
        event.setMaxScrollX(this.mScrollX);
        event.setMaxScrollY(getScrollRange());
    }

    private int getScrollRange() {
        if (getChildCount() > 0) {
            return Math.max(0, getChildAt(0).getHeight() - ((getHeight() - this.mPaddingBottom) - this.mPaddingTop));
        }
        return 0;
    }

    private View findFocusableViewInBounds(boolean topFocus, int top, int bottom) {
        List<View> focusables = getFocusables(2);
        View focusCandidate = null;
        boolean foundFullyContainedFocusable = false;
        int count = focusables.size();
        for (int i = 0; i < count; i++) {
            View view = (View) focusables.get(i);
            int viewTop = view.getTop();
            int viewBottom = view.getBottom();
            if (top < viewBottom && viewTop < bottom) {
                boolean viewIsFullyContained = top < viewTop ? viewBottom < bottom : false;
                if (focusCandidate == null) {
                    focusCandidate = view;
                    foundFullyContainedFocusable = viewIsFullyContained;
                } else {
                    boolean viewIsCloserToBoundary = (!topFocus || viewTop >= focusCandidate.getTop()) ? !topFocus && viewBottom > focusCandidate.getBottom() : true;
                    if (foundFullyContainedFocusable) {
                        if (viewIsFullyContained && viewIsCloserToBoundary) {
                            focusCandidate = view;
                        }
                    } else if (viewIsFullyContained) {
                        focusCandidate = view;
                        foundFullyContainedFocusable = true;
                    } else if (viewIsCloserToBoundary) {
                        focusCandidate = view;
                    }
                }
            }
        }
        return focusCandidate;
    }

    public boolean pageScroll(int direction) {
        boolean down = direction == LogPower.END_CHG_ROTATION;
        int height = getHeight();
        if (down) {
            this.mTempRect.top = getScrollY() + height;
            int count = getChildCount();
            if (count > 0) {
                View view = getChildAt(count + INVALID_POINTER);
                if (this.mTempRect.top + height > view.getBottom()) {
                    this.mTempRect.top = view.getBottom() - height;
                }
            }
        } else {
            this.mTempRect.top = getScrollY() - height;
            if (this.mTempRect.top < 0) {
                this.mTempRect.top = 0;
            }
        }
        this.mTempRect.bottom = this.mTempRect.top + height;
        return scrollAndFocus(direction, this.mTempRect.top, this.mTempRect.bottom);
    }

    public boolean fullScroll(int direction) {
        boolean down = direction == LogPower.END_CHG_ROTATION;
        int height = getHeight();
        this.mTempRect.top = 0;
        this.mTempRect.bottom = height;
        if (down) {
            int count = getChildCount();
            if (count > 0) {
                this.mTempRect.bottom = getChildAt(count + INVALID_POINTER).getBottom() + this.mPaddingBottom;
                this.mTempRect.top = this.mTempRect.bottom - height;
            }
        }
        return scrollAndFocus(direction, this.mTempRect.top, this.mTempRect.bottom);
    }

    private boolean scrollAndFocus(int direction, int top, int bottom) {
        boolean handled = true;
        int height = getHeight();
        int containerTop = getScrollY();
        int containerBottom = containerTop + height;
        boolean up = direction == 33;
        View newFocused = findFocusableViewInBounds(up, top, bottom);
        if (newFocused == null) {
            newFocused = this;
        }
        if (top < containerTop || bottom > containerBottom) {
            doScrollY(up ? top - containerTop : bottom - containerBottom);
        } else {
            handled = false;
        }
        if (newFocused != findFocus()) {
            newFocused.requestFocus(direction);
        }
        return handled;
    }

    public boolean arrowScroll(int direction) {
        View currentFocused = findFocus();
        if (currentFocused == this) {
            currentFocused = null;
        }
        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);
        int maxJump = getMaxScrollAmount();
        if (nextFocused == null || !isWithinDeltaOfScreen(nextFocused, maxJump, getHeight())) {
            int scrollDelta = maxJump;
            if (direction == 33 && getScrollY() < maxJump) {
                scrollDelta = getScrollY();
            } else if (direction == LogPower.END_CHG_ROTATION && getChildCount() > 0) {
                int daBottom = getChildAt(0).getBottom();
                int screenBottom = (getScrollY() + getHeight()) - this.mPaddingBottom;
                if (daBottom - screenBottom < maxJump) {
                    scrollDelta = daBottom - screenBottom;
                }
            }
            if (scrollDelta == 0) {
                return false;
            }
            if (direction != LogPower.END_CHG_ROTATION) {
                scrollDelta = -scrollDelta;
            }
            doScrollY(scrollDelta);
        } else {
            nextFocused.getDrawingRect(this.mTempRect);
            offsetDescendantRectToMyCoords(nextFocused, this.mTempRect);
            doScrollY(computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
            nextFocused.requestFocus(direction);
        }
        if (currentFocused != null && currentFocused.isFocused() && isOffScreen(currentFocused)) {
            int descendantFocusability = getDescendantFocusability();
            setDescendantFocusability(Protocol.BASE_WIFI);
            requestFocus();
            setDescendantFocusability(descendantFocusability);
        }
        return true;
    }

    private boolean isOffScreen(View descendant) {
        return !isWithinDeltaOfScreen(descendant, 0, getHeight());
    }

    private boolean isWithinDeltaOfScreen(View descendant, int delta, int height) {
        descendant.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(descendant, this.mTempRect);
        if (this.mTempRect.bottom + delta < getScrollY() || this.mTempRect.top - delta > getScrollY() + height) {
            return false;
        }
        return true;
    }

    private void doScrollY(int delta) {
        if (delta == 0) {
            return;
        }
        if (this.mSmoothScrollingEnabled) {
            smoothScrollBy(0, delta);
        } else {
            scrollBy(0, delta);
        }
    }

    public final void smoothScrollBy(int dx, int dy) {
        if (getChildCount() != 0) {
            if (AnimationUtils.currentAnimationTimeMillis() - this.mLastScroll > 250) {
                int maxY = Math.max(0, getChildAt(0).getHeight() - ((getHeight() - this.mPaddingBottom) - this.mPaddingTop));
                int scrollY = this.mScrollY;
                this.mScroller.startScroll(this.mScrollX, scrollY, 0, Math.max(0, Math.min(scrollY + dy, maxY)) - scrollY);
                postInvalidateOnAnimation();
            } else {
                if (!this.mScroller.isFinished()) {
                    this.mScroller.abortAnimation();
                    if (this.mFlingStrictSpan != null) {
                        this.mFlingStrictSpan.finish();
                        this.mFlingStrictSpan = null;
                    }
                }
                scrollBy(dx, dy);
            }
            this.mLastScroll = AnimationUtils.currentAnimationTimeMillis();
        }
    }

    public final void smoothScrollTo(int x, int y) {
        smoothScrollBy(x - this.mScrollX, y - this.mScrollY);
    }

    protected int computeVerticalScrollRange() {
        int contentHeight = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
        if (getChildCount() == 0) {
            return contentHeight;
        }
        int scrollRange = getChildAt(0).getBottom();
        int scrollY = this.mScrollY;
        int overscrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overscrollBottom) {
            scrollRange += scrollY - overscrollBottom;
        }
        return scrollRange;
    }

    protected int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        child.measure(ViewGroup.getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight, child.getLayoutParams().width), MeasureSpec.makeSafeMeasureSpec(Math.max(0, MeasureSpec.getSize(parentHeightMeasureSpec) - (this.mPaddingTop + this.mPaddingBottom)), 0));
    }

    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        child.measure(ViewGroup.getChildMeasureSpec(parentWidthMeasureSpec, (((this.mPaddingLeft + this.mPaddingRight) + lp.leftMargin) + lp.rightMargin) + widthUsed, lp.width), MeasureSpec.makeSafeMeasureSpec(Math.max(0, MeasureSpec.getSize(parentHeightMeasureSpec) - ((((this.mPaddingTop + this.mPaddingBottom) + lp.topMargin) + lp.bottomMargin) + heightUsed)), 0));
    }

    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            int oldX = this.mScrollX;
            int oldY = this.mScrollY;
            int x = this.mScroller.getCurrX();
            int y = this.mScroller.getCurrY();
            if (!(oldX == x && oldY == y)) {
                int range = getScrollRange();
                int overscrollMode = getOverScrollMode();
                boolean canOverscroll = overscrollMode != 0 ? overscrollMode == 1 && range > 0 : true;
                overScrollBy(x - oldX, y - oldY, oldX, oldY, 0, range, 0, this.mOverflingDistance, false);
                onScrollChanged(this.mScrollX, this.mScrollY, oldX, oldY);
                if (canOverscroll) {
                    if (y < 0 && oldY >= 0) {
                        this.mEdgeGlowTop.onAbsorb((int) this.mScroller.getCurrVelocity());
                    } else if (y > range && oldY <= range) {
                        this.mEdgeGlowBottom.onAbsorb((int) this.mScroller.getCurrVelocity());
                    }
                }
            }
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        } else if (this.mFlingStrictSpan != null) {
            this.mFlingStrictSpan.finish();
            this.mFlingStrictSpan = null;
        }
    }

    private void scrollToChild(View child) {
        child.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(child, this.mTempRect);
        int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(this.mTempRect);
        if (scrollDelta != 0) {
            scrollBy(0, scrollDelta);
        }
    }

    private boolean scrollToChildRect(Rect rect, boolean immediate) {
        boolean scroll;
        int delta = computeScrollDeltaToGetChildRectOnScreen(rect);
        if (delta != 0) {
            scroll = true;
        } else {
            scroll = false;
        }
        if (scroll) {
            if (immediate) {
                scrollBy(0, delta);
            } else {
                smoothScrollBy(0, delta);
            }
        }
        return scroll;
    }

    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        if (getChildCount() == 0) {
            return 0;
        }
        int height = getHeight();
        int screenTop = getScrollY();
        int screenBottom = screenTop + height;
        int fadingEdge = getVerticalFadingEdgeLength();
        if (rect.top > 0) {
            screenTop += fadingEdge;
        }
        if (rect.bottom < getChildAt(0).getHeight()) {
            screenBottom -= fadingEdge;
        }
        int scrollYDelta = 0;
        if (rect.bottom > screenBottom && rect.top > screenTop) {
            if (rect.height() > height) {
                scrollYDelta = (rect.top - screenTop) + 0;
            } else {
                scrollYDelta = (rect.bottom - screenBottom) + 0;
            }
            scrollYDelta = Math.min(scrollYDelta, getChildAt(0).getBottom() - screenBottom);
        } else if (rect.top < screenTop && rect.bottom < screenBottom) {
            if (rect.height() > height) {
                scrollYDelta = 0 - (screenBottom - rect.bottom);
            } else {
                scrollYDelta = 0 - (screenTop - rect.top);
            }
            scrollYDelta = Math.max(scrollYDelta, -getScrollY());
        }
        return scrollYDelta;
    }

    public void requestChildFocus(View child, View focused) {
        if (this.mIsLayoutDirty) {
            this.mChildToScrollTo = focused;
        } else {
            scrollToChild(focused);
        }
        super.requestChildFocus(child, focused);
    }

    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        View nextFocus;
        if (direction == 2) {
            direction = LogPower.END_CHG_ROTATION;
        } else if (direction == 1) {
            direction = 33;
        }
        if (previouslyFocusedRect == null) {
            nextFocus = FocusFinder.getInstance().findNextFocus(this, null, direction);
        } else {
            nextFocus = FocusFinder.getInstance().findNextFocusFromRect(this, previouslyFocusedRect, direction);
        }
        if (nextFocus == null || isOffScreen(nextFocus)) {
            return false;
        }
        return nextFocus.requestFocus(direction, previouslyFocusedRect);
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        rectangle.offset(child.getLeft() - child.getScrollX(), child.getTop() - child.getScrollY());
        return scrollToChildRect(rectangle, immediate);
    }

    public void requestLayout() {
        this.mIsLayoutDirty = true;
        super.requestLayout();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mScrollStrictSpan != null) {
            this.mScrollStrictSpan.finish();
            this.mScrollStrictSpan = null;
        }
        if (this.mFlingStrictSpan != null) {
            this.mFlingStrictSpan.finish();
            this.mFlingStrictSpan = null;
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mIsLayoutDirty = false;
        if (this.mChildToScrollTo != null && isViewDescendantOf(this.mChildToScrollTo, this)) {
            scrollToChild(this.mChildToScrollTo);
        }
        this.mChildToScrollTo = null;
        if (!isLaidOut()) {
            if (this.mSavedState != null) {
                this.mScrollY = this.mSavedState.scrollPosition;
                this.mSavedState = null;
            }
            int scrollRange = Math.max(0, (getChildCount() > 0 ? getChildAt(0).getMeasuredHeight() : 0) - (((b - t) - this.mPaddingBottom) - this.mPaddingTop));
            if (this.mScrollY > scrollRange) {
                this.mScrollY = scrollRange;
            } else if (this.mScrollY < 0) {
                this.mScrollY = 0;
            }
        }
        scrollTo(this.mScrollX, this.mScrollY);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        View currentFocused = findFocus();
        if (!(currentFocused == null || this == currentFocused || !isWithinDeltaOfScreen(currentFocused, 0, oldh))) {
            currentFocused.getDrawingRect(this.mTempRect);
            offsetDescendantRectToMyCoords(currentFocused, this.mTempRect);
            doScrollY(computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
        }
    }

    private static boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        return theParent instanceof ViewGroup ? isViewDescendantOf((View) theParent, parent) : false;
    }

    public void fling(int velocityY) {
        if (getChildCount() > 0) {
            int height = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
            int i = velocityY;
            int i2 = 0;
            int i3 = 0;
            int i4 = 0;
            int i5 = 0;
            this.mScroller.fling(this.mScrollX, this.mScrollY, 0, i, i2, i3, i4, Math.max(0, getChildAt(0).getHeight() - height), i5, height / 2);
            if (this.mFlingStrictSpan == null) {
                this.mFlingStrictSpan = StrictMode.enterCriticalSpan("ScrollView-fling");
            }
            postInvalidateOnAnimation();
        }
    }

    private void flingWithNestedDispatch(int velocityY) {
        boolean canFling = (this.mScrollY > 0 || velocityY > 0) ? this.mScrollY < getScrollRange() || velocityY < 0 : false;
        if (!dispatchNestedPreFling(0.0f, (float) velocityY)) {
            dispatchNestedFling(0.0f, (float) velocityY, canFling);
            if (canFling) {
                fling(velocityY);
            }
        }
    }

    private void endDrag() {
        this.mIsBeingDragged = false;
        recycleVelocityTracker();
        if (this.mEdgeGlowTop != null) {
            this.mEdgeGlowTop.onRelease();
            this.mEdgeGlowBottom.onRelease();
        }
        if (this.mScrollStrictSpan != null) {
            this.mScrollStrictSpan.finish();
            this.mScrollStrictSpan = null;
        }
    }

    public void scrollTo(int x, int y) {
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            x = clamp(x, (getWidth() - this.mPaddingRight) - this.mPaddingLeft, child.getWidth());
            y = clamp(y, (getHeight() - this.mPaddingBottom) - this.mPaddingTop, child.getHeight());
            if (x != this.mScrollX || y != this.mScrollY) {
                super.scrollTo(x, y);
            }
        }
    }

    public void setOverScrollMode(int mode) {
        if (mode == 2) {
            this.mEdgeGlowTop = null;
            this.mEdgeGlowBottom = null;
        } else if (this.mEdgeGlowTop == null) {
            Context context = getContext();
            this.mEdgeGlowTop = new EdgeEffect(context);
            this.mEdgeGlowBottom = new EdgeEffect(context);
        }
        super.setOverScrollMode(mode);
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & 2) != 0;
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(2);
    }

    public void onStopNestedScroll(View target) {
        super.onStopNestedScroll(target);
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int oldScrollY = this.mScrollY;
        scrollBy(0, dyUnconsumed);
        int myConsumed = this.mScrollY - oldScrollY;
        dispatchNestedScroll(0, myConsumed, 0, dyUnconsumed - myConsumed, null);
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (consumed) {
            return false;
        }
        flingWithNestedDispatch((int) velocityY);
        return true;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mEdgeGlowTop != null) {
            int restoreCount;
            int width;
            int height;
            float translateX;
            float translateY;
            int scrollY = this.mScrollY;
            boolean clipToPadding = getClipToPadding();
            if (!this.mEdgeGlowTop.isFinished()) {
                restoreCount = canvas.save();
                if (clipToPadding) {
                    width = (getWidth() - this.mPaddingLeft) - this.mPaddingRight;
                    height = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
                    translateX = (float) this.mPaddingLeft;
                    translateY = (float) this.mPaddingTop;
                } else {
                    width = getWidth();
                    height = getHeight();
                    translateX = 0.0f;
                    translateY = 0.0f;
                }
                canvas.translate(translateX, ((float) Math.min(0, scrollY)) + translateY);
                this.mEdgeGlowTop.setSize(width, height);
                if (this.mEdgeGlowTop.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount);
            }
            if (!this.mEdgeGlowBottom.isFinished()) {
                restoreCount = canvas.save();
                if (clipToPadding) {
                    width = (getWidth() - this.mPaddingLeft) - this.mPaddingRight;
                    height = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
                    translateX = (float) this.mPaddingLeft;
                    translateY = (float) this.mPaddingTop;
                } else {
                    width = getWidth();
                    height = getHeight();
                    translateX = 0.0f;
                    translateY = 0.0f;
                }
                canvas.translate(((float) (-width)) + translateX, ((float) (Math.max(getScrollRange(), scrollY) + height)) + translateY);
                canvas.rotate(180.0f, (float) width, 0.0f);
                this.mEdgeGlowBottom.setSize(width, height);
                if (this.mEdgeGlowBottom.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount);
            }
        }
    }

    private static int clamp(int n, int my, int child) {
        if (my >= child || n < 0) {
            return 0;
        }
        if (my + n > child) {
            return child - my;
        }
        return n;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 18) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mSavedState = ss;
        requestLayout();
    }

    protected Parcelable onSaveInstanceState() {
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 18) {
            return super.onSaveInstanceState();
        }
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.scrollPosition = this.mScrollY;
        return ss;
    }

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("fillViewport", this.mFillViewport);
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (this.mIsHwTheme) {
            if ((isTopEdge() || isBottomEdge()) && isTouchEvent) {
                deltaY = getElasticInterpolation(deltaY, scrollY);
            }
            maxOverScrollY = getHeight();
            invalidate();
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    private int getElasticInterpolation(int delta, int currentPos) {
        if (isBottomEdge()) {
            currentPos -= getScrollRange();
        }
        float len = (float) Math.abs(currentPos);
        int newDelta = (int) ((Math.sqrt(((double) (250.0f * ((float) Math.abs(delta)))) + Math.pow((double) len, 2.0d)) - ((double) len)) * ((double) Math.signum((float) delta)));
        if (Math.abs(newDelta) > Math.abs(delta)) {
            return delta;
        }
        return newDelta;
    }

    private boolean isTopEdge() {
        return getScrollY() < 0;
    }

    private boolean isBottomEdge() {
        return getScrollRange() <= getScrollY();
    }
}
