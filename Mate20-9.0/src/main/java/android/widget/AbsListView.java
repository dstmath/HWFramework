package android.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.os.Trace;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Jlog;
import android.util.JlogConstants;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.StateSet;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.RemoteViews;
import android.widget.RemoteViewsAdapter;
import com.android.internal.R;
import com.huawei.emui.hiexperience.hwperf.speedloader.HwPerfSpeedLoader;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsListView extends AdapterView<ListAdapter> implements TextWatcher, ViewTreeObserver.OnGlobalLayoutListener, Filter.FilterListener, ViewTreeObserver.OnTouchModeChangeListener, RemoteViewsAdapter.RemoteAdapterConnectionCallback {
    private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;
    public static final int CHOICE_MODE_MULTIPLE = 2;
    public static final int CHOICE_MODE_MULTIPLE_MODAL = 3;
    public static final int CHOICE_MODE_MULTIPLE_MODAL_AUTO_SCROLL = 8;
    public static final int CHOICE_MODE_NONE = 0;
    public static final int CHOICE_MODE_SINGLE = 1;
    static final String DISABLE_HW_MULTI_SELECT_MODE = "disable-multi-select-move";
    static final String ENABLE_HW_MULTI_SELECT_MODE = "enable-multi-select-move";
    private static final int INVALID_POINTER = -1;
    static final int LAYOUT_FORCE_BOTTOM = 3;
    static final int LAYOUT_FORCE_TOP = 1;
    static final int LAYOUT_MOVE_SELECTION = 6;
    static final int LAYOUT_NORMAL = 0;
    static final int LAYOUT_SET_SELECTION = 2;
    static final int LAYOUT_SPECIFIC = 4;
    static final int LAYOUT_SYNC = 5;
    private static final String LOG_TAG = "OverScrollerOptimization";
    static final int OVERSCROLL_LIMIT_DIVISOR = 3;
    private static final boolean PROFILE_FLINGING = false;
    private static final boolean PROFILE_SCROLLING = false;
    private static final int SLOW_DOWN_INTERPOLATOR_FOR_LAST_ANIMATION = 10;
    private static final int SLOW_DOWN_SCREEN_NUMBER = 2;
    /* access modifiers changed from: private */
    public static final boolean SMART_SLIDE_PROPERTIES = SystemProperties.getBoolean("uifirst_listview_optimization_enable", false);
    private static final String TAG = "AbsListView";
    static final int TOUCH_MODE_DONE_WAITING = 2;
    static final int TOUCH_MODE_DOWN = 0;
    static final int TOUCH_MODE_FLING = 4;
    private static final int TOUCH_MODE_OFF = 1;
    private static final int TOUCH_MODE_ON = 0;
    static final int TOUCH_MODE_OVERFLING = 6;
    static final int TOUCH_MODE_OVERSCROLL = 5;
    static final int TOUCH_MODE_REST = -1;
    static final int TOUCH_MODE_SCROLL = 3;
    static final int TOUCH_MODE_TAP = 1;
    private static final int TOUCH_MODE_UNKNOWN = -1;
    public static final int TRANSCRIPT_MODE_ALWAYS_SCROLL = 2;
    public static final int TRANSCRIPT_MODE_DISABLED = 0;
    public static final int TRANSCRIPT_MODE_NORMAL = 1;
    static final Interpolator sLinearInterpolator = new LinearInterpolator();
    private ListItemAccessibilityDelegate mAccessibilityDelegate;
    /* access modifiers changed from: private */
    public int mActivePointerId;
    ListAdapter mAdapter;
    boolean mAdapterHasStableIds;
    int mAddItemViewPosition;
    int mAddItemViewType;
    private int mCacheColorHint;
    boolean mCachingActive;
    boolean mCachingStarted;
    SparseBooleanArray mCheckStates;
    LongSparseArray<Integer> mCheckedIdStates;
    int mCheckedItemCount;
    ActionMode mChoiceActionMode;
    int mChoiceMode;
    private Runnable mClearScrollingCache;
    private ContextMenu.ContextMenuInfo mContextMenuInfo;
    AdapterDataSetObserver mDataSetObserver;
    /* access modifiers changed from: private */
    public InputConnection mDefInputConnection;
    private boolean mDeferNotifyDataSetChanged;
    private float mDensityScale;
    private int mDirection;
    boolean mDrawSelectorOnTop;
    /* access modifiers changed from: private */
    public EdgeEffect mEdgeGlowBottom;
    /* access modifiers changed from: private */
    public EdgeEffect mEdgeGlowTop;
    /* access modifiers changed from: private */
    public FastScroller mFastScroll;
    boolean mFastScrollAlwaysVisible;
    boolean mFastScrollEnabled;
    private int mFastScrollStyle;
    private boolean mFiltered;
    private int mFirstPositionDistanceGuess;
    private boolean mFlingProfilingStarted;
    /* access modifiers changed from: private */
    public FlingRunnable mFlingRunnable;
    /* access modifiers changed from: private */
    public StrictMode.Span mFlingStrictSpan;
    /* access modifiers changed from: private */
    public float mFlingThreshold;
    private boolean mForceTranscriptScroll;
    private boolean mGlobalLayoutListenerAddedFilter;
    /* access modifiers changed from: private */
    public boolean mHasPerformedLongPress;
    /* access modifiers changed from: private */
    public HwPerfSpeedLoader mHwPerfSpeedLoader;
    /* access modifiers changed from: private */
    public IHwWechatOptimize mIHwWechatOptimize;
    protected boolean mIsAutoScroll;
    private boolean mIsChildViewEnabled;
    /* access modifiers changed from: private */
    public boolean mIsDetaching;
    final boolean[] mIsScrap;
    private int mLastAccessibilityScrollEventFromIndex;
    private int mLastAccessibilityScrollEventToIndex;
    private int mLastHandledItemCount;
    private int mLastPositionDistanceGuess;
    private int mLastScrollState;
    private int mLastTouchMode;
    int mLastY;
    int mLayoutMode;
    Rect mListPadding;
    /* access modifiers changed from: private */
    public int mMaximumVelocity;
    /* access modifiers changed from: private */
    public int mMinimumVelocity;
    int mMotionCorrection;
    /* access modifiers changed from: private */
    public float mMotionEventDownPosition;
    /* access modifiers changed from: private */
    public float mMotionEventUpPosition;
    int mMotionPosition;
    int mMotionViewNewTop;
    int mMotionViewOriginalTop;
    int mMotionX;
    int mMotionY;
    MultiChoiceModeWrapper mMultiChoiceModeCallback;
    protected boolean mMultiSelectAutoScrollFlag;
    private int mNestedYOffset;
    private OnScrollListener mOnScrollListener;
    int mOverflingDistance;
    int mOverscrollDistance;
    int mOverscrollMax;
    private final Thread mOwnerThread;
    private CheckForKeyLongPress mPendingCheckForKeyLongPress;
    /* access modifiers changed from: private */
    public CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap;
    private SavedState mPendingSync;
    private PerformClick mPerformClick;
    PopupWindow mPopup;
    private boolean mPopupHidden;
    Runnable mPositionScrollAfterLayout;
    AbsPositionScroller mPositionScroller;
    private InputConnectionWrapper mPublicInputConnection;
    final RecycleBin mRecycler;
    private RemoteViewsAdapter mRemoteAdapter;
    int mResurrectToPosition;
    private final int[] mScrollConsumed;
    View mScrollDown;
    private final int[] mScrollOffset;
    private boolean mScrollProfilingStarted;
    private StrictMode.Span mScrollStrictSpan;
    View mScrollUp;
    boolean mScrollingCacheEnabled;
    int mSelectedTop;
    int mSelectionBottomPadding;
    int mSelectionLeftPadding;
    int mSelectionRightPadding;
    int mSelectionTopPadding;
    Drawable mSelector;
    int mSelectorPosition;
    Rect mSelectorRect;
    private int[] mSelectorState;
    private boolean mSmoothScrollbarEnabled;
    boolean mStackFromBottom;
    EditText mTextFilter;
    private boolean mTextFilterEnabled;
    /* access modifiers changed from: private */
    public final float[] mTmpPoint;
    private Rect mTouchFrame;
    int mTouchMode;
    /* access modifiers changed from: private */
    public Runnable mTouchModeReset;
    private int mTouchSlop;
    private int mTranscriptMode;
    private float mVelocityScale;
    /* access modifiers changed from: private */
    public VelocityTracker mVelocityTracker;
    private float mVerticalScrollFactor;
    int mWidthMeasureSpec;

    static abstract class AbsPositionScroller {
        public abstract void start(int i);

        public abstract void start(int i, int i2);

        public abstract void startWithOffset(int i, int i2);

        public abstract void startWithOffset(int i, int i2, int i3);

        public abstract void stop();

        AbsPositionScroller() {
        }
    }

    class AdapterDataSetObserver extends AdapterView<ListAdapter>.AdapterDataSetObserver {
        AdapterDataSetObserver() {
            super();
        }

        public void onChanged() {
            super.onChanged();
            if (AbsListView.this.mFastScroll != null) {
                AbsListView.this.mFastScroll.onSectionsChanged();
            }
        }

        public void onInvalidated() {
            super.onInvalidated();
            if (AbsListView.this.mFastScroll != null) {
                AbsListView.this.mFastScroll.onSectionsChanged();
            }
        }
    }

    private class CheckForKeyLongPress extends WindowRunnnable implements Runnable {
        private CheckForKeyLongPress() {
            super();
        }

        public void run() {
            if (AbsListView.this.isPressed() && AbsListView.this.mSelectedPosition >= 0) {
                View v = AbsListView.this.getChildAt(AbsListView.this.mSelectedPosition - AbsListView.this.mFirstPosition);
                if (!AbsListView.this.mDataChanged) {
                    boolean handled = false;
                    if (sameWindow()) {
                        handled = AbsListView.this.performLongPress(v, AbsListView.this.mSelectedPosition, AbsListView.this.mSelectedRowId);
                    }
                    if (handled) {
                        AbsListView.this.setPressed(false);
                        if (v != null) {
                            v.setPressed(false);
                            return;
                        }
                        return;
                    }
                    return;
                }
                AbsListView.this.setPressed(false);
                if (v != null) {
                    v.setPressed(false);
                }
            }
        }
    }

    private class CheckForLongPress extends WindowRunnnable implements Runnable {
        private static final int INVALID_COORD = -1;
        private float mX;
        private float mY;

        private CheckForLongPress() {
            super();
            this.mX = -1.0f;
            this.mY = -1.0f;
        }

        /* access modifiers changed from: private */
        public void setCoords(float x, float y) {
            this.mX = x;
            this.mY = y;
        }

        public void run() {
            View child = AbsListView.this.getChildAt(AbsListView.this.mMotionPosition - AbsListView.this.mFirstPosition);
            if (child != null) {
                int longPressPosition = AbsListView.this.mMotionPosition;
                long longPressId = AbsListView.this.mAdapter.getItemId(AbsListView.this.mMotionPosition);
                boolean handled = false;
                if (sameWindow() && !AbsListView.this.mDataChanged && !AbsListView.this.getRootView().isLongPressSwipe()) {
                    if (this.mX == -1.0f || this.mY == -1.0f) {
                        handled = AbsListView.this.performLongPress(child, longPressPosition, longPressId);
                    } else {
                        handled = AbsListView.this.performLongPress(child, longPressPosition, longPressId, this.mX, this.mY);
                    }
                }
                if (handled) {
                    boolean unused = AbsListView.this.mHasPerformedLongPress = true;
                    AbsListView.this.mTouchMode = -1;
                    AbsListView.this.setPressed(false);
                    child.setPressed(false);
                    return;
                }
                AbsListView.this.mTouchMode = 2;
            }
        }
    }

    private final class CheckForTap implements Runnable {
        float x;
        float y;

        private CheckForTap() {
        }

        public void run() {
            if (AbsListView.this.mTouchMode == 0) {
                AbsListView.this.mTouchMode = 1;
                View child = AbsListView.this.getChildAt(AbsListView.this.mMotionPosition - AbsListView.this.mFirstPosition);
                if (child != null && !child.hasExplicitFocusable()) {
                    AbsListView.this.mLayoutMode = 0;
                    if (!AbsListView.this.mDataChanged) {
                        float[] point = AbsListView.this.mTmpPoint;
                        point[0] = this.x;
                        point[1] = this.y;
                        AbsListView.this.transformPointToViewLocal(point, child);
                        child.drawableHotspotChanged(point[0], point[1]);
                        child.setPressed(true);
                        AbsListView.this.setPressed(true);
                        AbsListView.this.layoutChildren();
                        AbsListView.this.positionSelector(AbsListView.this.mMotionPosition, child);
                        AbsListView.this.refreshDrawableState();
                        int longPressTimeout = ViewConfiguration.getLongPressTimeout();
                        boolean longClickable = AbsListView.this.isLongClickable();
                        if (AbsListView.this.mSelector != null) {
                            Drawable d = AbsListView.this.mSelector.getCurrent();
                            if (d != null && (d instanceof TransitionDrawable)) {
                                if (longClickable) {
                                    ((TransitionDrawable) d).startTransition(longPressTimeout);
                                } else {
                                    ((TransitionDrawable) d).resetTransition();
                                }
                            }
                            AbsListView.this.mSelector.setHotspot(this.x, this.y);
                        }
                        if (longClickable) {
                            if (AbsListView.this.mPendingCheckForLongPress == null) {
                                CheckForLongPress unused = AbsListView.this.mPendingCheckForLongPress = new CheckForLongPress();
                            }
                            AbsListView.this.mPendingCheckForLongPress.setCoords(this.x, this.y);
                            AbsListView.this.mPendingCheckForLongPress.rememberWindowAttachCount();
                            AbsListView.this.postDelayed(AbsListView.this.mPendingCheckForLongPress, (long) longPressTimeout);
                            return;
                        }
                        AbsListView.this.mTouchMode = 2;
                        return;
                    }
                    AbsListView.this.mTouchMode = 2;
                }
            }
        }
    }

    public class FlingRunnable implements Runnable {
        private static final int FLYWHEEL_TIMEOUT = 40;
        private final Runnable mCheckFlywheel = new Runnable() {
            public void run() {
                int activeId = AbsListView.this.mActivePointerId;
                VelocityTracker vt = AbsListView.this.mVelocityTracker;
                OverScroller scroller = FlingRunnable.this.mScroller;
                if (vt != null && activeId != -1) {
                    vt.computeCurrentVelocity(1000, (float) AbsListView.this.mMaximumVelocity);
                    float yvel = -vt.getYVelocity(activeId);
                    if (Math.abs(yvel) < ((float) AbsListView.this.mMinimumVelocity) || !scroller.isScrollingInDirection(0.0f, yvel)) {
                        if (AbsListView.this.mTouchMode == 6) {
                            FlingRunnable.this.endFling();
                            AbsListView.this.mTouchMode = 5;
                        } else {
                            FlingRunnable.this.endFling();
                            AbsListView.this.mTouchMode = 3;
                        }
                        AbsListView.this.reportScrollStateChange(1);
                    } else {
                        AbsListView.this.postDelayed(this, 40);
                    }
                }
            }
        };
        private int mLastFlingY;
        /* access modifiers changed from: private */
        public final OverScroller mScroller;
        /* access modifiers changed from: private */
        public boolean mSuppressIdleStateChangeCall;
        private final float mflingThresholdInchPerSecond = 1.53f;

        FlingRunnable() {
            this.mScroller = new OverScroller(AbsListView.this.getContext());
            if (AbsListView.SMART_SLIDE_PROPERTIES) {
                float unused = AbsListView.this.mFlingThreshold = 1.53f * this.mScroller.getScreenPPI();
                if (AbsListView.this.mFlingThreshold == 0.0f) {
                    float unused2 = AbsListView.this.mFlingThreshold = (float) AbsListView.this.mMinimumVelocity;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void start(int initialVelocity) {
            int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            this.mLastFlingY = initialY;
            this.mScroller.setInterpolator(null);
            AbsListView.this.setStableItemHeight(this.mScroller, AbsListView.this.mFlingRunnable);
            if (AbsListView.SMART_SLIDE_PROPERTIES) {
                Log.d(AbsListView.LOG_TAG, "transmit the parameter and distance to OverScroller fling");
                this.mScroller.fling(0, initialY, 0, initialVelocity, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 0, 0, AbsListView.this.mMotionEventUpPosition - AbsListView.this.mMotionEventDownPosition);
            } else {
                this.mScroller.fling(0, initialY, 0, initialVelocity, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            }
            AbsListView.this.mTouchMode = 4;
            this.mSuppressIdleStateChangeCall = false;
            AbsListView.this.postOnAnimation(this);
            if (AbsListView.this.mHwPerfSpeedLoader != null) {
                AbsListView.this.mHwPerfSpeedLoader.onFlingStart();
            }
            if (AbsListView.this.mFlingStrictSpan == null) {
                StrictMode.Span unused = AbsListView.this.mFlingStrictSpan = StrictMode.enterCriticalSpan("AbsListView-fling");
            }
        }

        /* access modifiers changed from: package-private */
        public void startSpringback() {
            this.mSuppressIdleStateChangeCall = false;
            if (this.mScroller.springBack(0, AbsListView.this.mScrollY, 0, 0, 0, 0)) {
                AbsListView.this.mTouchMode = 6;
                AbsListView.this.invalidate();
                AbsListView.this.postOnAnimation(this);
                return;
            }
            AbsListView.this.mTouchMode = -1;
            AbsListView.this.reportScrollStateChange(0);
        }

        /* access modifiers changed from: package-private */
        public void startOverfling(int initialVelocity) {
            this.mScroller.setInterpolator(null);
            this.mScroller.fling(0, AbsListView.this.mScrollY, 0, initialVelocity, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, AbsListView.this.getHeight());
            AbsListView.this.mTouchMode = 6;
            this.mSuppressIdleStateChangeCall = false;
            AbsListView.this.invalidate();
            AbsListView.this.postOnAnimation(this);
        }

        /* access modifiers changed from: package-private */
        public void edgeReached(int delta) {
            if (!AbsListView.this.hasSpringAnimatorMask()) {
                this.mScroller.notifyVerticalEdgeReached(AbsListView.this.mScrollY, 0, AbsListView.this.mOverflingDistance);
            } else {
                this.mScroller.notifyVerticalEdgeReached(AbsListView.this.mScrollY, 0, (int) (0.5f * ((float) AbsListView.this.getHeight())));
            }
            int overscrollMode = AbsListView.this.getOverScrollMode();
            if (overscrollMode == 0 || (overscrollMode == 1 && !AbsListView.this.contentFits())) {
                AbsListView.this.mTouchMode = 6;
                int vel = (int) this.mScroller.getCurrVelocity();
                if (AbsListView.this.mEdgeGlowTop != null) {
                    if (delta > 0) {
                        AbsListView.this.mEdgeGlowTop.onAbsorb(vel);
                    } else {
                        AbsListView.this.mEdgeGlowBottom.onAbsorb(vel);
                    }
                }
            } else {
                AbsListView.this.mTouchMode = -1;
                if (AbsListView.this.mPositionScroller != null) {
                    AbsListView.this.mPositionScroller.stop();
                }
            }
            AbsListView.this.invalidate();
            AbsListView.this.postOnAnimation(this);
        }

        /* access modifiers changed from: package-private */
        public void startScroll(int distance, int duration, boolean linear, boolean suppressEndFlingStateChangeCall) {
            int initialY = distance < 0 ? Integer.MAX_VALUE : 0;
            this.mLastFlingY = initialY;
            this.mScroller.setInterpolator(linear ? AbsListView.sLinearInterpolator : null);
            this.mScroller.startScroll(0, initialY, 0, distance, duration);
            AbsListView.this.mTouchMode = 4;
            this.mSuppressIdleStateChangeCall = suppressEndFlingStateChangeCall;
            AbsListView.this.postOnAnimation(this);
        }

        /* access modifiers changed from: package-private */
        public void endFling() {
            AbsListView.this.mTouchMode = -1;
            AbsListView.this.removeCallbacks(this);
            AbsListView.this.removeCallbacks(this.mCheckFlywheel);
            if (!this.mSuppressIdleStateChangeCall) {
                AbsListView.this.reportScrollStateChange(0);
            }
            AbsListView.this.clearScrollingCache();
            this.mScroller.abortAnimation();
            if (AbsListView.this.mFlingStrictSpan != null) {
                AbsListView.this.mFlingStrictSpan.finish();
                StrictMode.Span unused = AbsListView.this.mFlingStrictSpan = null;
            }
        }

        /* access modifiers changed from: package-private */
        public void flywheelTouch() {
            AbsListView.this.postDelayed(this.mCheckFlywheel, 40);
        }

        private boolean isNeedOverScroll() {
            int overscrollMode = AbsListView.this.getOverScrollMode();
            if (overscrollMode == 0) {
                return true;
            }
            if (overscrollMode != 1 || AbsListView.this.contentFits()) {
                return false;
            }
            return true;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:29:0x007f, code lost:
            if (r0.mScroller.isFinished() == false) goto L_0x0082;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0081, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0086, code lost:
            if (r0.this$0.mDataChanged == false) goto L_0x008d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0088, code lost:
            r0.this$0.layoutChildren();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x0091, code lost:
            if (r0.this$0.mItemCount == 0) goto L_0x01d4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0099, code lost:
            if (r0.this$0.getChildCount() != 0) goto L_0x009d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x009d, code lost:
            r1 = r0.mScroller;
            r4 = r1.computeScrollOffset();
            r5 = r1.getCurrY();
            r6 = r0.mLastFlingY - r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x00aa, code lost:
            if (r6 <= 0) goto L_0x00dc;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ac, code lost:
            r0.this$0.mMotionPosition = r0.this$0.mFirstPosition;
            r0.this$0.mMotionViewOriginalTop = r0.this$0.getChildAt(0).getTop();
            r6 = java.lang.Math.min(((r0.this$0.getHeight() - android.widget.AbsListView.access$3800(r0.this$0)) - android.widget.AbsListView.access$3900(r0.this$0)) - 1, r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x00dc, code lost:
            r7 = r0.this$0.getChildCount() - 1;
            r0.this$0.mMotionPosition = r0.this$0.mFirstPosition + r7;
            r0.this$0.mMotionViewOriginalTop = r0.this$0.getChildAt(r7).getTop();
            r6 = java.lang.Math.max(-(((r0.this$0.getHeight() - android.widget.AbsListView.access$4000(r0.this$0)) - android.widget.AbsListView.access$4100(r0.this$0)) - 1), r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x0114, code lost:
            r7 = r0.this$0.getChildAt(r0.this$0.mMotionPosition - r0.this$0.mFirstPosition);
            r8 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x0124, code lost:
            if (r7 == null) goto L_0x012a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x0126, code lost:
            r8 = r7.getTop();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x012a, code lost:
            r9 = r0.this$0.trackMotionScroll(r6, r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x0130, code lost:
            if (r9 == false) goto L_0x0135;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:0x0132, code lost:
            if (r6 == 0) goto L_0x0135;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:48:0x0135, code lost:
            r3 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:49:0x0136, code lost:
            if (r3 == false) goto L_0x016b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:50:0x0138, code lost:
            if (r7 == null) goto L_0x0165;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:51:0x013a, code lost:
            r2 = -(r6 - (r7.getTop() - r8));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:52:0x0148, code lost:
            if (r0.this$0.hasSpringAnimatorMask() != false) goto L_0x0165;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:0x014a, code lost:
            android.widget.AbsListView.access$4300(r0.this$0, 0, r2, 0, android.widget.AbsListView.access$4200(r0.this$0), 0, 0, 0, r0.this$0.mOverflingDistance, false);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x0165, code lost:
            if (r4 == false) goto L_0x01d3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:55:0x0167, code lost:
            edgeReached(r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:56:0x016b, code lost:
            if (r4 == false) goto L_0x017e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x016d, code lost:
            if (r3 != false) goto L_0x017e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:58:0x016f, code lost:
            if (r9 == false) goto L_0x0176;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:0x0171, code lost:
            r0.this$0.invalidate();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:0x0176, code lost:
            r0.mLastFlingY = r5;
            r0.this$0.postOnAnimation(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x017e, code lost:
            endFling();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:63:0x0187, code lost:
            if (android.widget.AbsListView.access$2800(r0.this$0) == null) goto L_0x0196;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:64:0x0189, code lost:
            android.widget.AbsListView.access$2800(r0.this$0).onFlingRunning(r1.getCurrVelocity());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:66:0x01a0, code lost:
            if (android.widget.AbsListView.access$4400(r0.this$0).isWechatOptimizeEffect() == false) goto L_0x01d3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:68:0x01ac, code lost:
            if (android.widget.AbsListView.access$4400(r0.this$0).isWechatFling() == false) goto L_0x01d3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:70:0x01c3, code lost:
            if (java.lang.Math.abs(r1.getCurrVelocity()) >= ((float) android.widget.AbsListView.access$4400(r0.this$0).getWechatIdleVelocity())) goto L_0x01d3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:71:0x01c5, code lost:
            android.widget.AbsListView.access$4400(r0.this$0).setWechatFling(false);
            r0.this$0.reportScrollStateChange(0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:72:0x01d3, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:73:0x01d4, code lost:
            endFling();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:74:0x01d7, code lost:
            return;
         */
        public void run() {
            boolean crossUp = false;
            boolean atEnd = true;
            switch (AbsListView.this.mTouchMode) {
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    if (!isNeedOverScroll()) {
                        endFling();
                        return;
                    }
                    break;
                case 6:
                    OverScroller scroller = this.mScroller;
                    if (!scroller.computeScrollOffset()) {
                        endFling();
                        break;
                    } else {
                        int scrollY = AbsListView.this.mScrollY;
                        int currY = scroller.getCurrY();
                        if (!AbsListView.this.overScrollBy(0, currY - scrollY, 0, scrollY, 0, 0, 0, AbsListView.this.mOverflingDistance, false)) {
                            AbsListView.this.invalidate();
                            AbsListView.this.postOnAnimation(this);
                            break;
                        } else {
                            boolean crossDown = scrollY <= 0 && currY > 0;
                            if (scrollY >= 0 && currY < 0) {
                                crossUp = true;
                            }
                            if (!crossDown && !crossUp) {
                                startSpringback();
                                break;
                            } else {
                                int velocity = (int) scroller.getCurrVelocity();
                                if (crossUp) {
                                    velocity = -velocity;
                                }
                                scroller.abortAnimation();
                                start(velocity);
                                break;
                            }
                        }
                    }
                default:
                    endFling();
                    return;
            }
        }
    }

    private class InputConnectionWrapper implements InputConnection {
        private final EditorInfo mOutAttrs;
        private InputConnection mTarget;

        public InputConnectionWrapper(EditorInfo outAttrs) {
            this.mOutAttrs = outAttrs;
        }

        private InputConnection getTarget() {
            if (this.mTarget == null) {
                this.mTarget = AbsListView.this.getTextFilterInput().onCreateInputConnection(this.mOutAttrs);
            }
            return this.mTarget;
        }

        public boolean reportFullscreenMode(boolean enabled) {
            return AbsListView.this.mDefInputConnection.reportFullscreenMode(enabled);
        }

        public boolean performEditorAction(int editorAction) {
            if (editorAction != 6) {
                return false;
            }
            InputMethodManager imm = (InputMethodManager) AbsListView.this.getContext().getSystemService(InputMethodManager.class);
            if (imm != null) {
                imm.hideSoftInputFromWindow(AbsListView.this.getWindowToken(), 0);
            }
            return true;
        }

        public boolean sendKeyEvent(KeyEvent event) {
            return AbsListView.this.mDefInputConnection.sendKeyEvent(event);
        }

        public CharSequence getTextBeforeCursor(int n, int flags) {
            if (this.mTarget == null) {
                return "";
            }
            return this.mTarget.getTextBeforeCursor(n, flags);
        }

        public CharSequence getTextAfterCursor(int n, int flags) {
            if (this.mTarget == null) {
                return "";
            }
            return this.mTarget.getTextAfterCursor(n, flags);
        }

        public CharSequence getSelectedText(int flags) {
            if (this.mTarget == null) {
                return "";
            }
            return this.mTarget.getSelectedText(flags);
        }

        public int getCursorCapsMode(int reqModes) {
            if (this.mTarget == null) {
                return 16384;
            }
            return this.mTarget.getCursorCapsMode(reqModes);
        }

        public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
            return getTarget().getExtractedText(request, flags);
        }

        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            return getTarget().deleteSurroundingText(beforeLength, afterLength);
        }

        public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
            return getTarget().deleteSurroundingTextInCodePoints(beforeLength, afterLength);
        }

        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            return getTarget().setComposingText(text, newCursorPosition);
        }

        public boolean setComposingRegion(int start, int end) {
            return getTarget().setComposingRegion(start, end);
        }

        public boolean finishComposingText() {
            return this.mTarget == null || this.mTarget.finishComposingText();
        }

        public boolean commitText(CharSequence text, int newCursorPosition) {
            return getTarget().commitText(text, newCursorPosition);
        }

        public boolean commitCompletion(CompletionInfo text) {
            return getTarget().commitCompletion(text);
        }

        public boolean commitCorrection(CorrectionInfo correctionInfo) {
            return getTarget().commitCorrection(correctionInfo);
        }

        public boolean setSelection(int start, int end) {
            return getTarget().setSelection(start, end);
        }

        public boolean performContextMenuAction(int id) {
            return getTarget().performContextMenuAction(id);
        }

        public boolean beginBatchEdit() {
            return getTarget().beginBatchEdit();
        }

        public boolean endBatchEdit() {
            return getTarget().endBatchEdit();
        }

        public boolean clearMetaKeyStates(int states) {
            return getTarget().clearMetaKeyStates(states);
        }

        public boolean performPrivateCommand(String action, Bundle data) {
            return getTarget().performPrivateCommand(action, data);
        }

        public boolean requestCursorUpdates(int cursorUpdateMode) {
            return getTarget().requestCursorUpdates(cursorUpdateMode);
        }

        public Handler getHandler() {
            return getTarget().getHandler();
        }

        public void closeConnection() {
            getTarget().closeConnection();
        }

        public boolean commitContent(InputContentInfo inputContentInfo, int flags, Bundle opts) {
            return getTarget().commitContent(inputContentInfo, flags, opts);
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        @ViewDebug.ExportedProperty(category = "list")
        boolean forceAdd;
        boolean isEnabled;
        long itemId = -1;
        @ViewDebug.ExportedProperty(category = "list")
        boolean recycledHeaderFooter;
        int scrappedFromPosition;
        @ViewDebug.ExportedProperty(category = "list", mapping = {@ViewDebug.IntToString(from = -1, to = "ITEM_VIEW_TYPE_IGNORE"), @ViewDebug.IntToString(from = -2, to = "ITEM_VIEW_TYPE_HEADER_OR_FOOTER")})
        int viewType;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(int w, int h, int viewType2) {
            super(w, h);
            this.viewType = viewType2;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        /* access modifiers changed from: protected */
        public void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("list:viewType", this.viewType);
            encoder.addProperty("list:recycledHeaderFooter", this.recycledHeaderFooter);
            encoder.addProperty("list:forceAdd", this.forceAdd);
            encoder.addProperty("list:isEnabled", this.isEnabled);
        }
    }

    class ListItemAccessibilityDelegate extends View.AccessibilityDelegate {
        ListItemAccessibilityDelegate() {
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            AbsListView.this.onInitializeAccessibilityNodeInfoForItem(host, AbsListView.this.getPositionForView(host), info);
        }

        public boolean performAccessibilityAction(View host, int action, Bundle arguments) {
            boolean isItemEnabled;
            if (super.performAccessibilityAction(host, action, arguments)) {
                return true;
            }
            int position = AbsListView.this.getPositionForView(host);
            if (position == -1 || AbsListView.this.mAdapter == null || position >= AbsListView.this.mAdapter.getCount()) {
                return false;
            }
            ViewGroup.LayoutParams lp = host.getLayoutParams();
            if (lp instanceof LayoutParams) {
                isItemEnabled = ((LayoutParams) lp).isEnabled;
            } else {
                isItemEnabled = false;
            }
            if (!AbsListView.this.isEnabled() || !isItemEnabled) {
                return false;
            }
            if (action != 4) {
                if (action != 8) {
                    if (action != 16) {
                        if (action != 32 || !AbsListView.this.isLongClickable()) {
                            return false;
                        }
                        return AbsListView.this.performLongPress(host, position, AbsListView.this.getItemIdAtPosition(position));
                    } else if (!AbsListView.this.isItemClickable(host)) {
                        return false;
                    } else {
                        return AbsListView.this.performItemClick(host, position, AbsListView.this.getItemIdAtPosition(position));
                    }
                } else if (AbsListView.this.getSelectedItemPosition() != position) {
                    return false;
                } else {
                    AbsListView.this.setSelection(-1);
                    return true;
                }
            } else if (AbsListView.this.getSelectedItemPosition() == position) {
                return false;
            } else {
                AbsListView.this.setSelection(position);
                return true;
            }
        }
    }

    public interface MultiChoiceModeListener extends ActionMode.Callback {
        void onItemCheckedStateChanged(ActionMode actionMode, int i, long j, boolean z);
    }

    class MultiChoiceModeWrapper implements MultiChoiceModeListener {
        private MultiChoiceModeListener mWrapped;

        MultiChoiceModeWrapper() {
        }

        public void setWrapped(MultiChoiceModeListener wrapped) {
            this.mWrapped = wrapped;
        }

        public boolean hasWrappedCallback() {
            return this.mWrapped != null;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (!this.mWrapped.onCreateActionMode(mode, menu)) {
                return false;
            }
            AbsListView.this.setLongClickable(false);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return this.mWrapped.onPrepareActionMode(mode, menu);
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return this.mWrapped.onActionItemClicked(mode, item);
        }

        public void onDestroyActionMode(ActionMode mode) {
            this.mWrapped.onDestroyActionMode(mode);
            AbsListView.this.mChoiceActionMode = null;
            AbsListView.this.clearChoices();
            AbsListView.this.mDataChanged = true;
            AbsListView.this.rememberSyncState();
            AbsListView.this.requestLayout();
            AbsListView.this.setLongClickable(true);
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            this.mWrapped.onItemCheckedStateChanged(mode, position, id, checked);
            if (AbsListView.this.getCheckedItemCount() == 0) {
                mode.finish();
            }
        }
    }

    public interface OnScrollListener {
        public static final int SCROLL_STATE_FLING = 2;
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_TOUCH_SCROLL = 1;

        void onScroll(AbsListView absListView, int i, int i2, int i3);

        void onScrollStateChanged(AbsListView absListView, int i);
    }

    private class PerformClick extends WindowRunnnable implements Runnable {
        int mClickMotionPosition;

        private PerformClick() {
            super();
        }

        public void run() {
            if (!AbsListView.this.mDataChanged) {
                ListAdapter adapter = AbsListView.this.mAdapter;
                int motionPosition = this.mClickMotionPosition;
                if (adapter != null && AbsListView.this.mItemCount > 0 && motionPosition != -1 && motionPosition < adapter.getCount() && sameWindow() && adapter.isEnabled(motionPosition)) {
                    View view = AbsListView.this.getChildAt(motionPosition - AbsListView.this.mFirstPosition);
                    if (view != null) {
                        AbsListView.this.performItemClick(view, motionPosition, adapter.getItemId(motionPosition));
                    }
                }
            }
        }
    }

    class PositionScroller extends AbsPositionScroller implements Runnable {
        private static final int MOVE_DOWN_BOUND = 3;
        private static final int MOVE_DOWN_POS = 1;
        private static final int MOVE_OFFSET = 5;
        private static final int MOVE_UP_BOUND = 4;
        private static final int MOVE_UP_POS = 2;
        private static final int SCROLL_DURATION = 200;
        private int mBoundPos;
        private final int mExtraScroll;
        private int mLastSeenPos;
        private int mMode;
        private int mOffsetFromTop;
        private int mScrollDuration;
        private int mTargetPos;

        PositionScroller() {
            this.mExtraScroll = ViewConfiguration.get(AbsListView.this.mContext).getScaledFadingEdgeLength();
        }

        public void start(final int position) {
            int viewTravelCount;
            stop();
            if (AbsListView.this.mDataChanged) {
                AbsListView.this.mPositionScrollAfterLayout = new Runnable() {
                    public void run() {
                        PositionScroller.this.start(position);
                    }
                };
                return;
            }
            int childCount = AbsListView.this.getChildCount();
            if (childCount != 0) {
                int firstPos = AbsListView.this.mFirstPosition;
                int lastPos = (firstPos + childCount) - 1;
                int clampedPosition = Math.max(0, Math.min(AbsListView.this.getCount() - 1, position));
                if (clampedPosition < firstPos) {
                    viewTravelCount = (firstPos - clampedPosition) + 1;
                    this.mMode = 2;
                } else if (clampedPosition > lastPos) {
                    viewTravelCount = (clampedPosition - lastPos) + 1;
                    this.mMode = 1;
                } else {
                    scrollToVisible(clampedPosition, -1, 200);
                    return;
                }
                int viewTravelCount2 = viewTravelCount;
                if (viewTravelCount2 > 0) {
                    this.mScrollDuration = 200 / viewTravelCount2;
                } else {
                    this.mScrollDuration = 200;
                }
                this.mTargetPos = clampedPosition;
                this.mBoundPos = -1;
                this.mLastSeenPos = -1;
                AbsListView.this.postOnAnimation(this);
            }
        }

        public void start(final int position, final int boundPosition) {
            int viewTravelCount;
            stop();
            if (boundPosition == -1) {
                start(position);
            } else if (AbsListView.this.mDataChanged) {
                AbsListView.this.mPositionScrollAfterLayout = new Runnable() {
                    public void run() {
                        PositionScroller.this.start(position, boundPosition);
                    }
                };
            } else {
                int childCount = AbsListView.this.getChildCount();
                if (childCount != 0) {
                    int firstPos = AbsListView.this.mFirstPosition;
                    int lastPos = (firstPos + childCount) - 1;
                    int clampedPosition = Math.max(0, Math.min(AbsListView.this.getCount() - 1, position));
                    if (clampedPosition < firstPos) {
                        int boundPosFromLast = lastPos - boundPosition;
                        if (boundPosFromLast >= 1) {
                            int posTravel = (firstPos - clampedPosition) + 1;
                            int boundTravel = boundPosFromLast - 1;
                            if (boundTravel < posTravel) {
                                viewTravelCount = boundTravel;
                                this.mMode = 4;
                            } else {
                                viewTravelCount = posTravel;
                                this.mMode = 2;
                            }
                        } else {
                            return;
                        }
                    } else if (clampedPosition > lastPos) {
                        int boundPosFromFirst = boundPosition - firstPos;
                        if (boundPosFromFirst >= 1) {
                            int posTravel2 = (clampedPosition - lastPos) + 1;
                            viewTravelCount = boundPosFromFirst - 1;
                            if (viewTravelCount < posTravel2) {
                                int i = viewTravelCount;
                                this.mMode = 3;
                            } else {
                                this.mMode = 1;
                                viewTravelCount = posTravel2;
                            }
                        } else {
                            return;
                        }
                    } else {
                        scrollToVisible(clampedPosition, boundPosition, 200);
                        return;
                    }
                    int viewTravelCount2 = viewTravelCount;
                    if (viewTravelCount2 > 0) {
                        this.mScrollDuration = 200 / viewTravelCount2;
                    } else {
                        this.mScrollDuration = 200;
                    }
                    this.mTargetPos = clampedPosition;
                    this.mBoundPos = boundPosition;
                    this.mLastSeenPos = -1;
                    AbsListView.this.postOnAnimation(this);
                }
            }
        }

        public void startWithOffset(int position, int offset) {
            startWithOffset(position, offset, 200);
        }

        public void startWithOffset(final int position, int offset, final int duration) {
            int viewTravelCount;
            stop();
            if (AbsListView.this.mDataChanged) {
                final int postOffset = offset;
                AbsListView.this.mPositionScrollAfterLayout = new Runnable() {
                    public void run() {
                        PositionScroller.this.startWithOffset(position, postOffset, duration);
                    }
                };
                return;
            }
            int childCount = AbsListView.this.getChildCount();
            if (childCount != 0) {
                int offset2 = offset + AbsListView.this.getPaddingTop();
                this.mTargetPos = Math.max(0, Math.min(AbsListView.this.getCount() - 1, position));
                this.mOffsetFromTop = offset2;
                this.mBoundPos = -1;
                this.mLastSeenPos = -1;
                this.mMode = 5;
                int firstPos = AbsListView.this.mFirstPosition;
                int lastPos = (firstPos + childCount) - 1;
                if (this.mTargetPos < firstPos) {
                    viewTravelCount = firstPos - this.mTargetPos;
                } else if (this.mTargetPos > lastPos) {
                    viewTravelCount = this.mTargetPos - lastPos;
                } else {
                    AbsListView.this.smoothScrollBy(AbsListView.this.getChildAt(this.mTargetPos - firstPos).getTop() - offset2, duration, true, false);
                    return;
                }
                float screenTravelCount = ((float) viewTravelCount) / ((float) childCount);
                this.mScrollDuration = screenTravelCount < 1.0f ? duration : (int) (((float) duration) / screenTravelCount);
                this.mLastSeenPos = -1;
                AbsListView.this.postOnAnimation(this);
            }
        }

        private void scrollToVisible(int targetPos, int boundPos, int duration) {
            int i = targetPos;
            int boundPos2 = boundPos;
            int firstPos = AbsListView.this.mFirstPosition;
            int lastPos = (firstPos + AbsListView.this.getChildCount()) - 1;
            int paddedTop = AbsListView.this.mListPadding.top;
            int paddedBottom = AbsListView.this.getHeight() - AbsListView.this.mListPadding.bottom;
            if (i < firstPos || i > lastPos) {
                Log.w(AbsListView.TAG, "scrollToVisible called with targetPos " + i + " not visible [" + firstPos + ", " + lastPos + "]");
            }
            if (boundPos2 < firstPos || boundPos2 > lastPos) {
                boundPos2 = -1;
            }
            View targetChild = AbsListView.this.getChildAt(i - firstPos);
            int targetTop = targetChild.getTop();
            int targetBottom = targetChild.getBottom();
            int scrollBy = 0;
            if (targetBottom > paddedBottom) {
                scrollBy = targetBottom - paddedBottom;
            }
            if (targetTop < paddedTop) {
                scrollBy = targetTop - paddedTop;
            }
            if (scrollBy != 0) {
                if (boundPos2 >= 0) {
                    View boundChild = AbsListView.this.getChildAt(boundPos2 - firstPos);
                    int boundTop = boundChild.getTop();
                    int boundBottom = boundChild.getBottom();
                    int absScroll = Math.abs(scrollBy);
                    if (scrollBy >= 0 || boundBottom + absScroll <= paddedBottom) {
                        if (scrollBy > 0 && boundTop - absScroll < paddedTop) {
                            scrollBy = Math.min(0, boundTop - paddedTop);
                        }
                    } else {
                        int i2 = boundPos2;
                        scrollBy = Math.max(0, boundBottom - paddedBottom);
                    }
                }
                AbsListView.this.smoothScrollBy(scrollBy, duration);
            }
        }

        public void stop() {
            AbsListView.this.removeCallbacks(this);
        }

        public void run() {
            int listHeight = AbsListView.this.getHeight();
            int firstPos = AbsListView.this.mFirstPosition;
            boolean z = false;
            switch (this.mMode) {
                case 1:
                    int lastViewIndex = AbsListView.this.getChildCount() - 1;
                    int lastPos = firstPos + lastViewIndex;
                    if (lastViewIndex >= 0) {
                        if (lastPos != this.mLastSeenPos) {
                            View lastView = AbsListView.this.getChildAt(lastViewIndex);
                            int scrollBy = (lastView.getHeight() - (listHeight - lastView.getTop())) + (lastPos < AbsListView.this.mItemCount - 1 ? Math.max(AbsListView.this.mListPadding.bottom, this.mExtraScroll) : AbsListView.this.mListPadding.bottom);
                            if (AbsListView.this.mIsAutoScroll) {
                                this.mScrollDuration = scrollBy;
                            }
                            AbsListView absListView = AbsListView.this;
                            int i = this.mScrollDuration;
                            if (lastPos < this.mTargetPos) {
                                z = true;
                            }
                            absListView.smoothScrollBy(scrollBy, i, true, z);
                            this.mLastSeenPos = lastPos;
                            if (lastPos < this.mTargetPos) {
                                AbsListView.this.postOnAnimation(this);
                                break;
                            }
                        } else {
                            AbsListView.this.postOnAnimation(this);
                            return;
                        }
                    } else {
                        return;
                    }
                    break;
                case 2:
                    if (firstPos != this.mLastSeenPos) {
                        boolean z2 = false;
                        View firstView = AbsListView.this.getChildAt(0);
                        if (firstView != null) {
                            int firstViewTop = firstView.getTop();
                            int extraScroll = firstPos > 0 ? Math.max(this.mExtraScroll, AbsListView.this.mListPadding.top) : AbsListView.this.mListPadding.top;
                            if (AbsListView.this.mIsAutoScroll) {
                                this.mScrollDuration = extraScroll - firstViewTop;
                            }
                            AbsListView absListView2 = AbsListView.this;
                            int i2 = firstViewTop - extraScroll;
                            int i3 = this.mScrollDuration;
                            if (firstPos > this.mTargetPos) {
                                z2 = true;
                            }
                            absListView2.smoothScrollBy(i2, i3, true, z2);
                            this.mLastSeenPos = firstPos;
                            if (firstPos > this.mTargetPos) {
                                AbsListView.this.postOnAnimation(this);
                                break;
                            }
                        } else {
                            return;
                        }
                    } else {
                        AbsListView.this.postOnAnimation(this);
                        return;
                    }
                    break;
                case 3:
                    int childCount = AbsListView.this.getChildCount();
                    if (firstPos != this.mBoundPos && childCount > 1 && firstPos + childCount < AbsListView.this.mItemCount) {
                        int nextPos = firstPos + 1;
                        if (nextPos != this.mLastSeenPos) {
                            View nextView = AbsListView.this.getChildAt(1);
                            int nextViewHeight = nextView.getHeight();
                            int nextViewTop = nextView.getTop();
                            int extraScroll2 = Math.max(AbsListView.this.mListPadding.bottom, this.mExtraScroll);
                            if (nextPos >= this.mBoundPos) {
                                if (nextViewTop <= extraScroll2) {
                                    AbsListView.this.reportScrollStateChange(0);
                                    break;
                                } else {
                                    AbsListView.this.smoothScrollBy(nextViewTop - extraScroll2, this.mScrollDuration, true, false);
                                    break;
                                }
                            } else {
                                AbsListView.this.smoothScrollBy(Math.max(0, (nextViewHeight + nextViewTop) - extraScroll2), this.mScrollDuration, true, true);
                                this.mLastSeenPos = nextPos;
                                AbsListView.this.postOnAnimation(this);
                                break;
                            }
                        } else {
                            AbsListView.this.postOnAnimation(this);
                            return;
                        }
                    } else {
                        AbsListView.this.reportScrollStateChange(0);
                        return;
                    }
                    break;
                case 4:
                    int lastViewIndex2 = AbsListView.this.getChildCount() - 2;
                    if (lastViewIndex2 >= 0) {
                        int lastPos2 = firstPos + lastViewIndex2;
                        if (lastPos2 != this.mLastSeenPos) {
                            View lastView2 = AbsListView.this.getChildAt(lastViewIndex2);
                            int lastViewHeight = lastView2.getHeight();
                            int lastViewTop = lastView2.getTop();
                            int lastViewPixelsShowing = listHeight - lastViewTop;
                            int extraScroll3 = Math.max(AbsListView.this.mListPadding.top, this.mExtraScroll);
                            this.mLastSeenPos = lastPos2;
                            if (lastPos2 <= this.mBoundPos) {
                                int bottom = listHeight - extraScroll3;
                                int lastViewBottom = lastViewTop + lastViewHeight;
                                if (bottom <= lastViewBottom) {
                                    AbsListView.this.reportScrollStateChange(0);
                                    break;
                                } else {
                                    int i4 = lastViewIndex2;
                                    AbsListView.this.smoothScrollBy(-(bottom - lastViewBottom), this.mScrollDuration, true, false);
                                    break;
                                }
                            } else {
                                AbsListView.this.smoothScrollBy(-(lastViewPixelsShowing - extraScroll3), this.mScrollDuration, true, true);
                                AbsListView.this.postOnAnimation(this);
                                break;
                            }
                        } else {
                            AbsListView.this.postOnAnimation(this);
                            return;
                        }
                    } else {
                        return;
                    }
                case 5:
                    if (this.mLastSeenPos != firstPos) {
                        this.mLastSeenPos = firstPos;
                        int childCount2 = AbsListView.this.getChildCount();
                        int position = this.mTargetPos;
                        int lastPos3 = (firstPos + childCount2) - 1;
                        View firstChild = AbsListView.this.getChildAt(0);
                        if (firstChild != null) {
                            int firstChildHeight = firstChild.getHeight();
                            View lastChild = AbsListView.this.getChildAt(childCount2 - 1);
                            if (lastChild != null) {
                                int lastChildHeight = lastChild.getHeight();
                                float firstPositionVisiblePart = ((float) firstChildHeight) == 0.0f ? 1.0f : ((float) (firstChild.getTop() + firstChildHeight)) / ((float) firstChildHeight);
                                float lastPositionVisiblePart = ((float) lastChildHeight) == 0.0f ? 1.0f : ((float) ((AbsListView.this.getHeight() + lastChildHeight) - lastChild.getBottom())) / ((float) lastChildHeight);
                                float viewTravelCount = 0.0f;
                                if (position < firstPos) {
                                    viewTravelCount = ((float) (firstPos - position)) + (1.0f - firstPositionVisiblePart) + 1.0f;
                                } else if (position > lastPos3) {
                                    viewTravelCount = ((float) (position - lastPos3)) + (1.0f - lastPositionVisiblePart);
                                }
                                float screenTravelCount = viewTravelCount / ((float) childCount2);
                                float modifier = Math.min(Math.abs(screenTravelCount), 1.0f);
                                if (position >= firstPos) {
                                    float f = screenTravelCount;
                                    View view = firstChild;
                                    int i5 = firstChildHeight;
                                    if (position <= lastPos3) {
                                        int distance = AbsListView.this.getChildAt(position - firstPos).getTop() - this.mOffsetFromTop;
                                        AbsListView.this.smoothScrollBy(distance, ((int) (((float) this.mScrollDuration) * (((float) Math.abs(distance)) / ((float) AbsListView.this.getHeight())))) * 10, false, false);
                                        break;
                                    } else {
                                        AbsListView.this.smoothScrollBy((int) (((float) AbsListView.this.getHeight()) * modifier), (int) (((float) this.mScrollDuration) * modifier), true, true);
                                        AbsListView.this.postOnAnimation(this);
                                        break;
                                    }
                                } else {
                                    int distance2 = (int) (((float) (-AbsListView.this.getHeight())) * modifier);
                                    int i6 = childCount2;
                                    int duration = (int) (((float) this.mScrollDuration) * modifier);
                                    if (screenTravelCount >= 2.0f) {
                                        duration = 0;
                                    }
                                    float f2 = screenTravelCount;
                                    View view2 = firstChild;
                                    int i7 = firstChildHeight;
                                    AbsListView.this.smoothScrollBy(distance2, duration, false, true);
                                    AbsListView.this.postOnAnimation(this);
                                    break;
                                }
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        AbsListView.this.postOnAnimation(this);
                        return;
                    }
            }
        }
    }

    class RecycleBin {
        private View[] mActiveViews = new View[0];
        private ArrayList<View> mCurrentScrap;
        private int mFirstActivePosition;
        /* access modifiers changed from: private */
        public RecyclerListener mRecyclerListener;
        private ArrayList<View>[] mScrapViews;
        private ArrayList<View> mSkippedScrap;
        private SparseArray<View> mTransientStateViews;
        private LongSparseArray<View> mTransientStateViewsById;
        private int mViewTypeCount;

        RecycleBin() {
        }

        public void setViewTypeCount(int viewTypeCount) {
            if (viewTypeCount >= 1) {
                ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
                for (int i = 0; i < viewTypeCount; i++) {
                    scrapViews[i] = new ArrayList<>();
                }
                this.mViewTypeCount = viewTypeCount;
                this.mCurrentScrap = scrapViews[0];
                this.mScrapViews = scrapViews;
                return;
            }
            throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
        }

        public void markChildrenDirty() {
            if (this.mViewTypeCount == 1) {
                ArrayList<View> scrap = this.mCurrentScrap;
                int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    scrap.get(i).forceLayout();
                }
            } else {
                int typeCount = this.mViewTypeCount;
                for (int i2 = 0; i2 < typeCount; i2++) {
                    ArrayList<View> scrap2 = this.mScrapViews[i2];
                    int scrapCount2 = scrap2.size();
                    for (int j = 0; j < scrapCount2; j++) {
                        scrap2.get(j).forceLayout();
                    }
                }
            }
            if (this.mTransientStateViews != null) {
                int count = this.mTransientStateViews.size();
                for (int i3 = 0; i3 < count; i3++) {
                    this.mTransientStateViews.valueAt(i3).forceLayout();
                }
            }
            if (this.mTransientStateViewsById != null) {
                int count2 = this.mTransientStateViewsById.size();
                for (int i4 = 0; i4 < count2; i4++) {
                    this.mTransientStateViewsById.valueAt(i4).forceLayout();
                }
            }
        }

        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0;
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            if (this.mViewTypeCount == 1) {
                clearScrap(this.mCurrentScrap);
            } else {
                int typeCount = this.mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    clearScrap(this.mScrapViews[i]);
                }
            }
            clearTransientStateViews();
        }

        /* access modifiers changed from: package-private */
        public void fillActiveViews(int childCount, int firstActivePosition) {
            if (this.mActiveViews.length < childCount) {
                this.mActiveViews = new View[childCount];
            }
            this.mFirstActivePosition = firstActivePosition;
            View[] activeViews = this.mActiveViews;
            for (int i = 0; i < childCount; i++) {
                View child = AbsListView.this.getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (!(lp == null || lp.viewType == -2)) {
                    activeViews[i] = child;
                    lp.scrappedFromPosition = firstActivePosition + i;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public View getActiveView(int position) {
            int index = position - this.mFirstActivePosition;
            View[] activeViews = this.mActiveViews;
            if (index < 0 || index >= activeViews.length) {
                return null;
            }
            View match = activeViews[index];
            activeViews[index] = null;
            return match;
        }

        /* access modifiers changed from: package-private */
        public View getTransientStateView(int position) {
            if (AbsListView.this.mAdapter == null || !AbsListView.this.mAdapterHasStableIds || this.mTransientStateViewsById == null) {
                if (this.mTransientStateViews != null) {
                    int index = this.mTransientStateViews.indexOfKey(position);
                    if (index >= 0) {
                        View result = this.mTransientStateViews.valueAt(index);
                        this.mTransientStateViews.removeAt(index);
                        return result;
                    }
                }
                return null;
            }
            long id = AbsListView.this.mAdapter.getItemId(position);
            View result2 = this.mTransientStateViewsById.get(id);
            this.mTransientStateViewsById.remove(id);
            return result2;
        }

        /* access modifiers changed from: package-private */
        public void clearTransientStateViews() {
            SparseArray<View> viewsByPos = this.mTransientStateViews;
            if (viewsByPos != null) {
                int N = viewsByPos.size();
                for (int i = 0; i < N; i++) {
                    removeDetachedView(viewsByPos.valueAt(i), false);
                }
                viewsByPos.clear();
            }
            LongSparseArray<View> viewsById = this.mTransientStateViewsById;
            if (viewsById != null) {
                int N2 = viewsById.size();
                for (int i2 = 0; i2 < N2; i2++) {
                    removeDetachedView(viewsById.valueAt(i2), false);
                }
                viewsById.clear();
            }
        }

        /* access modifiers changed from: package-private */
        public View getScrapView(int position) {
            int whichScrap = AbsListView.this.getHwItemViewType(position);
            if (whichScrap < 0) {
                return null;
            }
            if (this.mViewTypeCount == 1) {
                return retrieveFromScrap(this.mCurrentScrap, position);
            }
            if (whichScrap < this.mScrapViews.length) {
                return retrieveFromScrap(this.mScrapViews[whichScrap], position);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public void addScrapView(View scrap, int position) {
            LayoutParams lp = (LayoutParams) scrap.getLayoutParams();
            if (lp != null) {
                lp.scrappedFromPosition = position;
                int viewType = lp.viewType;
                if (!shouldRecycleViewType(viewType)) {
                    if (viewType != -2) {
                        getSkippedScrap().add(scrap);
                    }
                    return;
                }
                scrap.dispatchStartTemporaryDetach();
                AbsListView.this.notifyViewAccessibilityStateChangedIfNeeded(1);
                if (!scrap.hasTransientState()) {
                    clearScrapForRebind(scrap);
                    if (this.mViewTypeCount == 1) {
                        this.mCurrentScrap.add(scrap);
                    } else {
                        this.mScrapViews[viewType].add(scrap);
                    }
                    if (this.mRecyclerListener != null) {
                        this.mRecyclerListener.onMovedToScrapHeap(scrap);
                    }
                } else if (AbsListView.this.mAdapter != null && AbsListView.this.mAdapterHasStableIds) {
                    if (this.mTransientStateViewsById == null) {
                        this.mTransientStateViewsById = new LongSparseArray<>();
                    }
                    this.mTransientStateViewsById.put(lp.itemId, scrap);
                } else if (!AbsListView.this.mDataChanged) {
                    if (this.mTransientStateViews == null) {
                        this.mTransientStateViews = new SparseArray<>();
                    }
                    this.mTransientStateViews.put(position, scrap);
                } else {
                    clearScrapForRebind(scrap);
                    getSkippedScrap().add(scrap);
                }
            }
        }

        private ArrayList<View> getSkippedScrap() {
            if (this.mSkippedScrap == null) {
                this.mSkippedScrap = new ArrayList<>();
            }
            return this.mSkippedScrap;
        }

        /* access modifiers changed from: package-private */
        public void removeSkippedScrap() {
            if (this.mSkippedScrap != null) {
                int count = this.mSkippedScrap.size();
                for (int i = 0; i < count; i++) {
                    removeDetachedView(this.mSkippedScrap.get(i), false);
                }
                this.mSkippedScrap.clear();
            }
        }

        /* access modifiers changed from: package-private */
        public void scrapActiveViews() {
            View[] activeViews = this.mActiveViews;
            boolean multipleScraps = true;
            boolean hasListener = this.mRecyclerListener != null;
            if (this.mViewTypeCount <= 1) {
                multipleScraps = false;
            }
            ArrayList<View> scrapViews = this.mCurrentScrap;
            for (int i = activeViews.length - 1; i >= 0; i--) {
                View victim = activeViews[i];
                if (victim != null) {
                    LayoutParams lp = (LayoutParams) victim.getLayoutParams();
                    int whichScrap = lp.viewType;
                    activeViews[i] = null;
                    if (victim.hasTransientState()) {
                        victim.dispatchStartTemporaryDetach();
                        if (AbsListView.this.mAdapter != null && AbsListView.this.mAdapterHasStableIds) {
                            if (this.mTransientStateViewsById == null) {
                                this.mTransientStateViewsById = new LongSparseArray<>();
                            }
                            this.mTransientStateViewsById.put(AbsListView.this.mAdapter.getItemId(this.mFirstActivePosition + i), victim);
                        } else if (!AbsListView.this.mDataChanged) {
                            if (this.mTransientStateViews == null) {
                                this.mTransientStateViews = new SparseArray<>();
                            }
                            this.mTransientStateViews.put(this.mFirstActivePosition + i, victim);
                        } else if (whichScrap != -2) {
                            removeDetachedView(victim, false);
                        }
                    } else if (shouldRecycleViewType(whichScrap)) {
                        if (multipleScraps) {
                            scrapViews = this.mScrapViews[whichScrap];
                        }
                        lp.scrappedFromPosition = this.mFirstActivePosition + i;
                        removeDetachedView(victim, false);
                        scrapViews.add(victim);
                        if (hasListener) {
                            this.mRecyclerListener.onMovedToScrapHeap(victim);
                        }
                    } else if (whichScrap != -2) {
                        removeDetachedView(victim, false);
                    }
                }
            }
            pruneScrapViews();
        }

        /* access modifiers changed from: package-private */
        public void fullyDetachScrapViews() {
            int viewTypeCount = this.mViewTypeCount;
            ArrayList<View>[] scrapViews = this.mScrapViews;
            for (int i = 0; i < viewTypeCount; i++) {
                ArrayList<View> scrapPile = scrapViews[i];
                for (int j = scrapPile.size() - 1; j >= 0; j--) {
                    View view = scrapPile.get(j);
                    if (view.isTemporarilyDetached()) {
                        removeDetachedView(view, false);
                    }
                }
            }
        }

        private void pruneScrapViews() {
            int maxViews = this.mActiveViews.length;
            int viewTypeCount = this.mViewTypeCount;
            ArrayList<View>[] scrapViews = this.mScrapViews;
            for (int i = 0; i < viewTypeCount; i++) {
                ArrayList<View> scrapPile = scrapViews[i];
                int size = scrapPile.size();
                while (size > maxViews) {
                    size--;
                    scrapPile.remove(size);
                }
            }
            SparseArray<View> transViewsByPos = this.mTransientStateViews;
            if (transViewsByPos != null) {
                int i2 = 0;
                while (i2 < transViewsByPos.size()) {
                    View v = transViewsByPos.valueAt(i2);
                    if (!v.hasTransientState()) {
                        removeDetachedView(v, false);
                        transViewsByPos.removeAt(i2);
                        i2--;
                    }
                    i2++;
                }
            }
            LongSparseArray<View> transViewsById = this.mTransientStateViewsById;
            if (transViewsById != null) {
                int i3 = 0;
                while (i3 < transViewsById.size()) {
                    View v2 = transViewsById.valueAt(i3);
                    if (!v2.hasTransientState()) {
                        removeDetachedView(v2, false);
                        transViewsById.removeAt(i3);
                        i3--;
                    }
                    i3++;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void reclaimScrapViews(List<View> views) {
            if (this.mViewTypeCount == 1) {
                views.addAll(this.mCurrentScrap);
                return;
            }
            int viewTypeCount = this.mViewTypeCount;
            ArrayList<View>[] scrapViews = this.mScrapViews;
            for (int i = 0; i < viewTypeCount; i++) {
                views.addAll(scrapViews[i]);
            }
        }

        /* access modifiers changed from: package-private */
        public void setCacheColorHint(int color) {
            if (this.mViewTypeCount == 1) {
                ArrayList<View> scrap = this.mCurrentScrap;
                int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    scrap.get(i).setDrawingCacheBackgroundColor(color);
                }
            } else {
                int typeCount = this.mViewTypeCount;
                for (int i2 = 0; i2 < typeCount; i2++) {
                    ArrayList<View> scrap2 = this.mScrapViews[i2];
                    int scrapCount2 = scrap2.size();
                    for (int j = 0; j < scrapCount2; j++) {
                        scrap2.get(j).setDrawingCacheBackgroundColor(color);
                    }
                }
            }
            for (View victim : this.mActiveViews) {
                if (victim != null) {
                    victim.setDrawingCacheBackgroundColor(color);
                }
            }
        }

        private View retrieveFromScrap(ArrayList<View> scrapViews, int position) {
            int size = scrapViews.size();
            if (size <= 0) {
                return null;
            }
            for (int i = size - 1; i >= 0; i--) {
                LayoutParams params = (LayoutParams) scrapViews.get(i).getLayoutParams();
                if (AbsListView.this.mAdapterHasStableIds) {
                    if (AbsListView.this.mAdapter.getItemId(position) == params.itemId) {
                        return scrapViews.remove(i);
                    }
                } else if (params.scrappedFromPosition == position) {
                    View scrap = scrapViews.remove(i);
                    clearScrapForRebind(scrap);
                    return scrap;
                }
            }
            View scrap2 = scrapViews.remove(size - 1);
            clearScrapForRebind(scrap2);
            return scrap2;
        }

        private void clearScrap(ArrayList<View> scrap) {
            int scrapCount = scrap.size();
            for (int j = 0; j < scrapCount; j++) {
                removeDetachedView(scrap.remove((scrapCount - 1) - j), false);
            }
        }

        private void clearScrapForRebind(View view) {
            view.clearAccessibilityFocus();
            view.setAccessibilityDelegate(null);
        }

        private void removeDetachedView(View child, boolean animate) {
            child.setAccessibilityDelegate(null);
            AbsListView.this.removeDetachedView(child, animate);
        }
    }

    public interface RecyclerListener {
        void onMovedToScrapHeap(View view);
    }

    static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        LongSparseArray<Integer> checkIdState;
        SparseBooleanArray checkState;
        int checkedItemCount;
        String filter;
        long firstId;
        int height;
        boolean inActionMode;
        int position;
        long selectedId;
        int viewTop;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.selectedId = in.readLong();
            this.firstId = in.readLong();
            this.viewTop = in.readInt();
            this.position = in.readInt();
            this.height = in.readInt();
            this.filter = in.readString();
            this.inActionMode = in.readByte() != 0;
            this.checkedItemCount = in.readInt();
            this.checkState = in.readSparseBooleanArray();
            int N = in.readInt();
            if (N > 0) {
                this.checkIdState = new LongSparseArray<>();
                for (int i = 0; i < N; i++) {
                    this.checkIdState.put(in.readLong(), Integer.valueOf(in.readInt()));
                }
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(this.selectedId);
            out.writeLong(this.firstId);
            out.writeInt(this.viewTop);
            out.writeInt(this.position);
            out.writeInt(this.height);
            out.writeString(this.filter);
            out.writeByte(this.inActionMode ? (byte) 1 : 0);
            out.writeInt(this.checkedItemCount);
            out.writeSparseBooleanArray(this.checkState);
            int N = this.checkIdState != null ? this.checkIdState.size() : 0;
            out.writeInt(N);
            for (int i = 0; i < N; i++) {
                out.writeLong(this.checkIdState.keyAt(i));
                out.writeInt(this.checkIdState.valueAt(i).intValue());
            }
        }

        public String toString() {
            return "AbsListView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " selectedId=" + this.selectedId + " firstId=" + this.firstId + " viewTop=" + this.viewTop + " position=" + this.position + " height=" + this.height + " filter=" + this.filter + " checkState=" + this.checkState + "}";
        }
    }

    public interface SelectionBoundsAdjuster {
        void adjustListItemSelectionBounds(Rect rect);
    }

    private class WindowRunnnable {
        private int mOriginalAttachCount;

        private WindowRunnnable() {
        }

        public void rememberWindowAttachCount() {
            this.mOriginalAttachCount = AbsListView.this.getWindowAttachCount();
        }

        public boolean sameWindow() {
            return AbsListView.this.getWindowAttachCount() == this.mOriginalAttachCount;
        }
    }

    /* access modifiers changed from: package-private */
    public abstract void fillGap(boolean z);

    /* access modifiers changed from: package-private */
    public abstract int findMotionRow(int i);

    /* access modifiers changed from: package-private */
    public abstract void setSelectionInt(int i);

    public AbsListView(Context context) {
        super(context);
        this.mFlingThreshold = 0.0f;
        this.mChoiceMode = 0;
        this.mLayoutMode = 0;
        this.mDeferNotifyDataSetChanged = false;
        this.mDrawSelectorOnTop = false;
        this.mSelectorPosition = -1;
        this.mSelectorRect = new Rect();
        this.mRecycler = new RecycleBin();
        this.mSelectionLeftPadding = 0;
        this.mSelectionTopPadding = 0;
        this.mSelectionRightPadding = 0;
        this.mSelectionBottomPadding = 0;
        this.mListPadding = new Rect();
        this.mWidthMeasureSpec = 0;
        this.mTouchMode = -1;
        this.mSelectedTop = 0;
        this.mSmoothScrollbarEnabled = true;
        this.mResurrectToPosition = -1;
        this.mContextMenuInfo = null;
        this.mAddItemViewType = -10000;
        this.mAddItemViewPosition = -1;
        this.mIsAutoScroll = false;
        this.mMultiSelectAutoScrollFlag = false;
        this.mLastTouchMode = -1;
        this.mScrollProfilingStarted = false;
        this.mFlingProfilingStarted = false;
        this.mScrollStrictSpan = null;
        this.mFlingStrictSpan = null;
        this.mLastScrollState = 0;
        this.mVelocityScale = 1.0f;
        this.mIsScrap = new boolean[1];
        this.mScrollOffset = new int[2];
        this.mScrollConsumed = new int[2];
        this.mTmpPoint = new float[2];
        this.mNestedYOffset = 0;
        this.mActivePointerId = -1;
        this.mDirection = 0;
        initAbsListView();
        this.mOwnerThread = Thread.currentThread();
        setVerticalScrollBarEnabled(true);
        TypedArray a = context.obtainStyledAttributes(R.styleable.View);
        initializeScrollbarsInternal(a);
        a.recycle();
    }

    public AbsListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842858);
    }

    public AbsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AbsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mFlingThreshold = 0.0f;
        this.mChoiceMode = 0;
        this.mLayoutMode = 0;
        this.mDeferNotifyDataSetChanged = false;
        this.mDrawSelectorOnTop = false;
        this.mSelectorPosition = -1;
        this.mSelectorRect = new Rect();
        this.mRecycler = new RecycleBin();
        this.mSelectionLeftPadding = 0;
        this.mSelectionTopPadding = 0;
        this.mSelectionRightPadding = 0;
        this.mSelectionBottomPadding = 0;
        this.mListPadding = new Rect();
        this.mWidthMeasureSpec = 0;
        this.mTouchMode = -1;
        this.mSelectedTop = 0;
        this.mSmoothScrollbarEnabled = true;
        this.mResurrectToPosition = -1;
        this.mContextMenuInfo = null;
        this.mAddItemViewType = -10000;
        this.mAddItemViewPosition = -1;
        this.mIsAutoScroll = false;
        this.mMultiSelectAutoScrollFlag = false;
        this.mLastTouchMode = -1;
        this.mScrollProfilingStarted = false;
        this.mFlingProfilingStarted = false;
        this.mScrollStrictSpan = null;
        this.mFlingStrictSpan = null;
        this.mLastScrollState = 0;
        this.mVelocityScale = 1.0f;
        this.mIsScrap = new boolean[1];
        this.mScrollOffset = new int[2];
        this.mScrollConsumed = new int[2];
        this.mTmpPoint = new float[2];
        this.mNestedYOffset = 0;
        this.mActivePointerId = -1;
        this.mDirection = 0;
        initAbsListView();
        this.mOwnerThread = Thread.currentThread();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AbsListView, defStyleAttr, defStyleRes);
        Drawable selector = a.getDrawable(0);
        if (selector != null) {
            setSelector(selector);
        }
        this.mDrawSelectorOnTop = a.getBoolean(1, false);
        setStackFromBottom(a.getBoolean(2, false));
        setScrollingCacheEnabled(a.getBoolean(3, true));
        setTextFilterEnabled(a.getBoolean(4, false));
        setTranscriptMode(a.getInt(5, 0));
        setCacheColorHint(a.getColor(6, 0));
        setSmoothScrollbarEnabled(a.getBoolean(9, true));
        setChoiceMode(a.getInt(7, 0));
        setFastScrollEnabled(a.getBoolean(8, false));
        setFastScrollStyle(a.getResourceId(11, 0));
        setFastScrollAlwaysVisible(a.getBoolean(10, false));
        a.recycle();
        if (context.getResources().getConfiguration().uiMode == 6) {
            setRevealOnFocusHint(false);
        }
    }

    private void initAbsListView() {
        setClickable(true);
        setFocusableInTouchMode(true);
        setWillNotDraw(false);
        setAlwaysDrawnWithCacheEnabled(false);
        setScrollingCacheEnabled(true);
        ViewConfiguration configuration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mVerticalScrollFactor = configuration.getScaledVerticalScrollFactor();
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mOverscrollDistance = configuration.getScaledOverscrollDistance();
        this.mOverflingDistance = configuration.getScaledOverflingDistance();
        this.mDensityScale = getContext().getResources().getDisplayMetrics().density;
        this.mIHwWechatOptimize = HwWidgetFactory.getHwWechatOptimize();
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

    public void setAdapter(ListAdapter adapter) {
        if (adapter != null) {
            this.mAdapterHasStableIds = this.mAdapter.hasStableIds();
            if (this.mChoiceMode != 0 && this.mAdapterHasStableIds && this.mCheckedIdStates == null) {
                this.mCheckedIdStates = new LongSparseArray<>();
            }
        }
        clearChoices();
    }

    public int getCheckedItemCount() {
        return this.mCheckedItemCount;
    }

    public int getHwItemViewType(int position) {
        if (this.mAddItemViewPosition == -1 || this.mAddItemViewPosition != position || this.mAddItemViewType == -10000) {
            return this.mAdapter.getItemViewType(position);
        }
        return this.mAddItemViewType;
    }

    public boolean isItemChecked(int position) {
        if (this.mChoiceMode == 0 || this.mCheckStates == null) {
            return false;
        }
        return this.mCheckStates.get(position);
    }

    public int getCheckedItemPosition() {
        if (this.mChoiceMode == 1 && this.mCheckStates != null && this.mCheckStates.size() == 1) {
            return this.mCheckStates.keyAt(0);
        }
        return -1;
    }

    public SparseBooleanArray getCheckedItemPositions() {
        if (this.mChoiceMode != 0) {
            return this.mCheckStates;
        }
        return null;
    }

    public long[] getCheckedItemIds() {
        if (this.mChoiceMode == 0 || this.mCheckedIdStates == null || this.mAdapter == null) {
            return new long[0];
        }
        LongSparseArray<Integer> idStates = this.mCheckedIdStates;
        int count = idStates.size();
        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = idStates.keyAt(i);
        }
        return ids;
    }

    public void clearChoices() {
        if (this.mCheckStates != null) {
            this.mCheckStates.clear();
        }
        if (this.mCheckedIdStates != null) {
            this.mCheckedIdStates.clear();
        }
        this.mCheckedItemCount = 0;
    }

    public void setItemChecked(int position, boolean value) {
        boolean itemCheckChanged;
        if (this.mChoiceMode != 0) {
            if (value && this.mChoiceMode == 3 && this.mChoiceActionMode == null) {
                if (this.mMultiChoiceModeCallback == null || !this.mMultiChoiceModeCallback.hasWrappedCallback()) {
                    throw new IllegalStateException("AbsListView: attempted to start selection mode for CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was supplied. Call setMultiChoiceModeListener to set a callback.");
                }
                this.mChoiceActionMode = startActionMode(this.mMultiChoiceModeCallback);
            }
            boolean z = false;
            if (this.mChoiceMode == 2 || this.mChoiceMode == 3) {
                boolean oldValue = this.mCheckStates.get(position);
                this.mCheckStates.put(position, value);
                if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                    if (value) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    } else {
                        this.mCheckedIdStates.delete(this.mAdapter.getItemId(position));
                    }
                }
                if (oldValue != value) {
                    z = true;
                }
                itemCheckChanged = z;
                if (itemCheckChanged) {
                    if (value) {
                        this.mCheckedItemCount++;
                    } else {
                        this.mCheckedItemCount--;
                    }
                }
                if (this.mChoiceActionMode != null) {
                    this.mMultiChoiceModeCallback.onItemCheckedStateChanged(this.mChoiceActionMode, position, this.mAdapter.getItemId(position), value);
                }
            } else {
                boolean updateIds = this.mCheckedIdStates != null && this.mAdapter.hasStableIds();
                itemCheckChanged = isItemChecked(position) != value;
                if (value || isItemChecked(position)) {
                    this.mCheckStates.clear();
                    if (updateIds) {
                        this.mCheckedIdStates.clear();
                    }
                }
                if (value) {
                    this.mCheckStates.put(position, true);
                    if (updateIds) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    }
                    this.mCheckedItemCount = 1;
                } else if (this.mCheckStates.size() == 0 || !this.mCheckStates.valueAt(0)) {
                    this.mCheckedItemCount = 0;
                }
            }
            if (!this.mInLayout && !this.mBlockLayoutRequests && itemCheckChanged) {
                this.mDataChanged = true;
                rememberSyncState();
                requestLayout();
            }
        }
    }

    public boolean performItemClick(View view, int position, long id) {
        boolean handled;
        int i = position;
        boolean dispatchItemClick = true;
        if (this.mChoiceMode != 0) {
            handled = true;
            boolean checkedStateChanged = false;
            if (this.mChoiceMode == 2 || (this.mChoiceMode == 3 && this.mChoiceActionMode != null)) {
                boolean checked = getCheckedStateForMultiSelect(!this.mCheckStates.get(i, false));
                this.mCheckStates.put(i, checked);
                if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                    if (checked) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(i), Integer.valueOf(i));
                    } else {
                        this.mCheckedIdStates.delete(this.mAdapter.getItemId(i));
                    }
                }
                if (checked) {
                    this.mCheckedItemCount++;
                } else {
                    this.mCheckedItemCount--;
                }
                if (this.mChoiceActionMode != null) {
                    this.mMultiChoiceModeCallback.onItemCheckedStateChanged(this.mChoiceActionMode, i, id, checked);
                    dispatchItemClick = false;
                }
                checkedStateChanged = true;
            } else if (this.mChoiceMode == 1) {
                if (!this.mCheckStates.get(i, false)) {
                    this.mCheckStates.clear();
                    this.mCheckStates.put(i, true);
                    if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                        this.mCheckedIdStates.clear();
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(i), Integer.valueOf(i));
                    }
                    this.mCheckedItemCount = 1;
                } else if (this.mCheckStates.size() == 0 || !this.mCheckStates.valueAt(0)) {
                    this.mCheckedItemCount = 0;
                }
                checkedStateChanged = true;
            }
            if (checkedStateChanged) {
                updateOnScreenCheckedViews();
            }
        } else {
            handled = false;
        }
        if (dispatchItemClick) {
            return handled | super.performItemClick(view, position, id);
        }
        return handled;
    }

    private void updateOnScreenCheckedViews() {
        int firstPos = this.mFirstPosition;
        int count = getChildCount();
        boolean useActivated = getContext().getApplicationInfo().targetSdkVersion >= 11;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int position = firstPos + i;
            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(this.mCheckStates.get(position));
            } else if (useActivated) {
                child.setActivated(this.mCheckStates.get(position));
            }
        }
    }

    public int getChoiceMode() {
        return this.mChoiceMode;
    }

    public void setChoiceMode(int choiceMode) {
        if (choiceMode == 8) {
            this.mMultiSelectAutoScrollFlag = true;
            return;
        }
        this.mChoiceMode = choiceMode;
        if (this.mChoiceActionMode != null) {
            this.mChoiceActionMode.finish();
            this.mChoiceActionMode = null;
        }
        if (this.mChoiceMode != 0) {
            if (this.mCheckStates == null) {
                this.mCheckStates = new SparseBooleanArray(0);
            }
            if (this.mCheckedIdStates == null && this.mAdapter != null && this.mAdapter.hasStableIds()) {
                this.mCheckedIdStates = new LongSparseArray<>(0);
            }
            if (this.mChoiceMode == 3) {
                clearChoices();
                setLongClickable(true);
            }
        }
    }

    public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
        if (this.mMultiChoiceModeCallback == null) {
            this.mMultiChoiceModeCallback = new MultiChoiceModeWrapper();
        }
        this.mMultiChoiceModeCallback.setWrapped(listener);
    }

    /* access modifiers changed from: private */
    public boolean contentFits() {
        int childCount = getChildCount();
        boolean z = true;
        if (childCount == 0) {
            return true;
        }
        if (childCount != this.mItemCount) {
            return false;
        }
        if (getChildAt(0).getTop() < this.mListPadding.top || getChildAt(childCount - 1).getBottom() > getHeight() - this.mListPadding.bottom) {
            z = false;
        }
        return z;
    }

    public void setFastScrollEnabled(final boolean enabled) {
        if (this.mFastScrollEnabled != enabled) {
            this.mFastScrollEnabled = enabled;
            if (isOwnerThread()) {
                setFastScrollerEnabledUiThread(enabled);
            } else {
                post(new Runnable() {
                    public void run() {
                        AbsListView.this.setFastScrollerEnabledUiThread(enabled);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public void setFastScrollerEnabledUiThread(boolean enabled) {
        if (this.mFastScroll != null) {
            this.mFastScroll.setEnabled(enabled);
        } else if (enabled) {
            this.mFastScroll = (FastScroller) HwWidgetFactory.getHwFastScroller(this, this.mFastScrollStyle, this.mContext);
            this.mFastScroll.setEnabled(true);
        }
        resolvePadding();
        if (this.mFastScroll != null) {
            this.mFastScroll.updateLayout();
        }
    }

    public void setFastScrollStyle(int styleResId) {
        if (this.mFastScroll == null) {
            this.mFastScrollStyle = styleResId;
        } else {
            this.mFastScroll.setStyle(styleResId);
        }
    }

    public void setFastScrollAlwaysVisible(final boolean alwaysShow) {
        if (this.mFastScrollAlwaysVisible != alwaysShow) {
            if (alwaysShow && !this.mFastScrollEnabled) {
                setFastScrollEnabled(true);
            }
            this.mFastScrollAlwaysVisible = alwaysShow;
            if (isOwnerThread()) {
                setFastScrollerAlwaysVisibleUiThread(alwaysShow);
            } else {
                post(new Runnable() {
                    public void run() {
                        AbsListView.this.setFastScrollerAlwaysVisibleUiThread(alwaysShow);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public void setFastScrollerAlwaysVisibleUiThread(boolean alwaysShow) {
        if (this.mFastScroll != null) {
            this.mFastScroll.setAlwaysShow(alwaysShow);
        }
    }

    private boolean isOwnerThread() {
        return this.mOwnerThread == Thread.currentThread();
    }

    public boolean isFastScrollAlwaysVisible() {
        boolean z = false;
        if (this.mFastScroll == null) {
            if (this.mFastScrollEnabled && this.mFastScrollAlwaysVisible) {
                z = true;
            }
            return z;
        }
        if (this.mFastScroll.isEnabled() && this.mFastScroll.isAlwaysShowEnabled()) {
            z = true;
        }
        return z;
    }

    public int getVerticalScrollbarWidth() {
        if (this.mFastScroll == null || !this.mFastScroll.isEnabled()) {
            return super.getVerticalScrollbarWidth();
        }
        return Math.max(super.getVerticalScrollbarWidth(), this.mFastScroll.getWidth());
    }

    @ViewDebug.ExportedProperty
    public boolean isFastScrollEnabled() {
        if (this.mFastScroll == null) {
            return this.mFastScrollEnabled;
        }
        return this.mFastScroll.isEnabled();
    }

    public void setVerticalScrollbarPosition(int position) {
        super.setVerticalScrollbarPosition(position);
        if (this.mFastScroll != null) {
            this.mFastScroll.setScrollbarPosition(position);
        }
    }

    public void setScrollBarStyle(int style) {
        super.setScrollBarStyle(style);
        if (this.mFastScroll != null) {
            this.mFastScroll.setScrollBarStyle(style);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isVerticalScrollBarHidden() {
        return isFastScrollEnabled();
    }

    public void setSmoothScrollbarEnabled(boolean enabled) {
        this.mSmoothScrollbarEnabled = enabled;
    }

    @ViewDebug.ExportedProperty
    public boolean isSmoothScrollbarEnabled() {
        return this.mSmoothScrollbarEnabled;
    }

    public void setOnScrollListener(OnScrollListener l) {
        this.mOnScrollListener = l;
        invokeOnItemScrollListener();
    }

    /* access modifiers changed from: package-private */
    public void invokeOnItemScrollListener() {
        if (this.mFastScroll != null) {
            this.mFastScroll.onScroll(this.mFirstPosition, getChildCount(), this.mItemCount);
        }
        if (this.mOnScrollListener != null) {
            this.mOnScrollListener.onScroll(this, this.mFirstPosition, getChildCount(), this.mItemCount);
        }
        onScrollChanged(0, 0, 0, 0);
    }

    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        if (event.getEventType() == 4096) {
            int firstVisiblePosition = getFirstVisiblePosition();
            int lastVisiblePosition = getLastVisiblePosition();
            if (this.mLastAccessibilityScrollEventFromIndex != firstVisiblePosition || this.mLastAccessibilityScrollEventToIndex != lastVisiblePosition) {
                this.mLastAccessibilityScrollEventFromIndex = firstVisiblePosition;
                this.mLastAccessibilityScrollEventToIndex = lastVisiblePosition;
            } else {
                return;
            }
        }
        super.sendAccessibilityEventUnchecked(event);
    }

    public CharSequence getAccessibilityClassName() {
        return AbsListView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (isEnabled()) {
            if (canScrollUp()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP);
                info.setScrollable(true);
            }
            if (canScrollDown()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN);
                info.setScrollable(true);
            }
        }
        info.removeAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
        info.setClickable(false);
    }

    /* access modifiers changed from: package-private */
    public int getSelectionModeForAccessibility() {
        switch (getChoiceMode()) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
            case 3:
                return 2;
            default:
                return 0;
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (action != 4096) {
            if (action == 8192 || action == 16908344) {
                if (!isEnabled() || !canScrollUp()) {
                    return false;
                }
                smoothScrollBy(-((getHeight() - this.mListPadding.top) - this.mListPadding.bottom), 200);
                return true;
            } else if (action != 16908346) {
                return false;
            }
        }
        if (!isEnabled() || !canScrollDown()) {
            return false;
        }
        smoothScrollBy((getHeight() - this.mListPadding.top) - this.mListPadding.bottom, 200);
        return true;
    }

    public View findViewByAccessibilityIdTraversal(int accessibilityId) {
        if (accessibilityId == getAccessibilityViewId()) {
            return this;
        }
        return super.findViewByAccessibilityIdTraversal(accessibilityId);
    }

    @ViewDebug.ExportedProperty
    public boolean isScrollingCacheEnabled() {
        return this.mScrollingCacheEnabled;
    }

    public void setScrollingCacheEnabled(boolean enabled) {
        if (this.mScrollingCacheEnabled && !enabled) {
            clearScrollingCache();
        }
        this.mScrollingCacheEnabled = enabled;
    }

    public void setTextFilterEnabled(boolean textFilterEnabled) {
        this.mTextFilterEnabled = textFilterEnabled;
    }

    @ViewDebug.ExportedProperty
    public boolean isTextFilterEnabled() {
        return this.mTextFilterEnabled;
    }

    public void getFocusedRect(Rect r) {
        View view = getSelectedView();
        if (view == null || view.getParent() != this) {
            super.getFocusedRect(r);
            return;
        }
        view.getFocusedRect(r);
        offsetDescendantRectToMyCoords(view, r);
    }

    private void useDefaultSelector() {
        setSelector(getContext().getDrawable(17301602));
    }

    @ViewDebug.ExportedProperty
    public boolean isStackFromBottom() {
        return this.mStackFromBottom;
    }

    public void setStackFromBottom(boolean stackFromBottom) {
        if (this.mStackFromBottom != stackFromBottom) {
            this.mStackFromBottom = stackFromBottom;
            requestLayoutIfNecessary();
        }
    }

    /* access modifiers changed from: package-private */
    public void requestLayoutIfNecessary() {
        if (getChildCount() > 0) {
            resetList();
            requestLayout();
            invalidate();
        }
    }

    public Parcelable onSaveInstanceState() {
        dismissPopup();
        SavedState ss = new SavedState(super.onSaveInstanceState());
        if (this.mPendingSync != null) {
            ss.selectedId = this.mPendingSync.selectedId;
            ss.firstId = this.mPendingSync.firstId;
            ss.viewTop = this.mPendingSync.viewTop;
            ss.position = this.mPendingSync.position;
            ss.height = this.mPendingSync.height;
            ss.filter = this.mPendingSync.filter;
            ss.inActionMode = this.mPendingSync.inActionMode;
            ss.checkedItemCount = this.mPendingSync.checkedItemCount;
            ss.checkState = this.mPendingSync.checkState;
            ss.checkIdState = this.mPendingSync.checkIdState;
            return ss;
        }
        boolean z = true;
        boolean haveChildren = getChildCount() > 0 && this.mItemCount > 0;
        long selectedId = getSelectedItemId();
        ss.selectedId = selectedId;
        ss.height = getHeight();
        if (selectedId >= 0) {
            ss.viewTop = this.mSelectedTop;
            ss.position = getSelectedItemPosition();
            ss.firstId = -1;
        } else if (!haveChildren || this.mFirstPosition <= 0) {
            ss.viewTop = 0;
            ss.firstId = -1;
            ss.position = 0;
        } else {
            ss.viewTop = getChildAt(0).getTop();
            int firstPos = this.mFirstPosition;
            if (firstPos >= this.mItemCount) {
                firstPos = this.mItemCount - 1;
            }
            ss.position = firstPos;
            ss.firstId = this.mAdapter.getItemId(firstPos);
        }
        ss.filter = null;
        if (this.mFiltered) {
            EditText textFilter = this.mTextFilter;
            if (textFilter != null) {
                Editable filterText = textFilter.getText();
                if (filterText != null) {
                    ss.filter = filterText.toString();
                }
            }
        }
        if (this.mChoiceMode != 3 || this.mChoiceActionMode == null) {
            z = false;
        }
        ss.inActionMode = z;
        if (this.mCheckStates != null) {
            ss.checkState = this.mCheckStates.clone();
        }
        if (this.mCheckedIdStates != null) {
            LongSparseArray<Integer> idState = new LongSparseArray<>();
            int count = this.mCheckedIdStates.size();
            for (int i = 0; i < count; i++) {
                idState.put(this.mCheckedIdStates.keyAt(i), this.mCheckedIdStates.valueAt(i));
            }
            ss.checkIdState = idState;
        }
        ss.checkedItemCount = this.mCheckedItemCount;
        if (this.mRemoteAdapter != null) {
            this.mRemoteAdapter.saveRemoteViewsCache();
        }
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mDataChanged = true;
        this.mSyncHeight = (long) ss.height;
        if (ss.selectedId >= 0) {
            this.mNeedSync = true;
            this.mPendingSync = ss;
            this.mSyncRowId = ss.selectedId;
            this.mSyncPosition = ss.position;
            this.mSpecificTop = ss.viewTop;
            this.mSyncMode = 0;
        } else if (ss.firstId >= 0) {
            setSelectedPositionInt(-1);
            setNextSelectedPositionInt(-1);
            this.mSelectorPosition = -1;
            this.mNeedSync = true;
            this.mPendingSync = ss;
            this.mSyncRowId = ss.firstId;
            this.mSyncPosition = ss.position;
            this.mSpecificTop = ss.viewTop;
            this.mSyncMode = 1;
        }
        setFilterText(ss.filter);
        if (ss.checkState != null) {
            this.mCheckStates = ss.checkState;
        }
        if (ss.checkIdState != null) {
            this.mCheckedIdStates = ss.checkIdState;
        }
        this.mCheckedItemCount = ss.checkedItemCount;
        if (ss.inActionMode && this.mChoiceMode == 3 && this.mMultiChoiceModeCallback != null) {
            this.mChoiceActionMode = startActionMode(this.mMultiChoiceModeCallback);
        }
        requestLayout();
    }

    private boolean acceptFilter() {
        return this.mTextFilterEnabled && (getAdapter() instanceof Filterable) && ((Filterable) getAdapter()).getFilter() != null;
    }

    public void setFilterText(String filterText) {
        if (this.mTextFilterEnabled && !TextUtils.isEmpty(filterText)) {
            createTextFilter(false);
            this.mTextFilter.setText((CharSequence) filterText);
            this.mTextFilter.setSelection(filterText.length());
            if (this.mAdapter instanceof Filterable) {
                if (this.mPopup == null) {
                    ((Filterable) this.mAdapter).getFilter().filter(filterText);
                }
                this.mFiltered = true;
                this.mDataSetObserver.clearSavedState();
            }
        }
    }

    public CharSequence getTextFilter() {
        if (!this.mTextFilterEnabled || this.mTextFilter == null) {
            return null;
        }
        return this.mTextFilter.getText();
    }

    /* access modifiers changed from: protected */
    public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && this.mSelectedPosition < 0 && !isInTouchMode()) {
            if (!isAttachedToWindow() && this.mAdapter != null) {
                this.mDataChanged = true;
                this.mOldItemCount = this.mItemCount;
                this.mItemCount = this.mAdapter.getCount();
            }
            resurrectSelection();
        }
    }

    public void requestLayout() {
        if (!this.mBlockLayoutRequests && !this.mInLayout) {
            super.requestLayout();
        }
    }

    /* access modifiers changed from: package-private */
    public void resetList() {
        removeAllViewsInLayout();
        this.mFirstPosition = 0;
        this.mDataChanged = false;
        this.mPositionScrollAfterLayout = null;
        this.mNeedSync = false;
        this.mPendingSync = null;
        this.mOldSelectedPosition = -1;
        this.mOldSelectedRowId = Long.MIN_VALUE;
        setSelectedPositionInt(-1);
        setNextSelectedPositionInt(-1);
        this.mSelectedTop = 0;
        this.mSelectorPosition = -1;
        this.mSelectorRect.setEmpty();
        invalidate();
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollExtent() {
        int count = getChildCount();
        if (count <= 0) {
            return 0;
        }
        if (!this.mSmoothScrollbarEnabled) {
            return 1;
        }
        int extent = count * 100;
        View view = getChildAt(0);
        int top = view.getTop();
        int height = view.getHeight();
        if (height > 0) {
            extent += (top * 100) / height;
        }
        View view2 = getChildAt(count - 1);
        int bottom = view2.getBottom();
        int height2 = view2.getHeight();
        if (height2 > 0) {
            extent -= ((bottom - getHeight()) * 100) / height2;
        }
        return extent;
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollOffset() {
        int index;
        int firstPosition = this.mFirstPosition;
        int childCount = getChildCount();
        if (firstPosition >= 0 && childCount > 0) {
            if (this.mSmoothScrollbarEnabled) {
                View view = getChildAt(0);
                int top = view.getTop();
                int height = view.getHeight();
                if (height > 0) {
                    return Math.max(((firstPosition * 100) - ((top * 100) / height)) + ((int) ((((float) this.mScrollY) / ((float) getHeight())) * ((float) this.mItemCount) * 100.0f)), 0);
                }
            } else {
                int count = this.mItemCount;
                if (firstPosition == 0) {
                    index = 0;
                } else if (firstPosition + childCount == count) {
                    index = count;
                } else {
                    index = (childCount / 2) + firstPosition;
                }
                return (int) (((float) firstPosition) + (((float) childCount) * (((float) index) / ((float) count))));
            }
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollRange() {
        if (!this.mSmoothScrollbarEnabled) {
            return this.mItemCount;
        }
        int result = Math.max(this.mItemCount * 100, 0);
        if (this.mScrollY != 0) {
            return result + Math.abs((int) ((((float) this.mScrollY) / ((float) getHeight())) * ((float) this.mItemCount) * 100.0f));
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public float getTopFadingEdgeStrength() {
        int count = getChildCount();
        float fadeEdge = super.getTopFadingEdgeStrength();
        if (count == 0) {
            return fadeEdge;
        }
        if (this.mFirstPosition > 0) {
            return 1.0f;
        }
        int top = getChildAt(0).getTop();
        return top < this.mPaddingTop ? ((float) (-(top - this.mPaddingTop))) / ((float) getVerticalFadingEdgeLength()) : fadeEdge;
    }

    /* access modifiers changed from: protected */
    public float getBottomFadingEdgeStrength() {
        float f;
        int count = getChildCount();
        float fadeEdge = super.getBottomFadingEdgeStrength();
        if (count == 0) {
            return fadeEdge;
        }
        if ((this.mFirstPosition + count) - 1 < this.mItemCount - 1) {
            return 1.0f;
        }
        int bottom = getChildAt(count - 1).getBottom();
        int height = getHeight();
        float fadeLength = (float) getVerticalFadingEdgeLength();
        if (bottom > height - this.mPaddingBottom) {
            f = ((float) ((bottom - height) + this.mPaddingBottom)) / fadeLength;
        } else {
            f = fadeEdge;
        }
        return f;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mSelector == null) {
            useDefaultSelector();
        }
        Rect listPadding = this.mListPadding;
        listPadding.left = this.mSelectionLeftPadding + this.mPaddingLeft;
        listPadding.top = this.mSelectionTopPadding + this.mPaddingTop;
        listPadding.right = this.mSelectionRightPadding + this.mPaddingRight;
        listPadding.bottom = this.mSelectionBottomPadding + this.mPaddingBottom;
        boolean z = true;
        if (this.mTranscriptMode == 1) {
            int childCount = getChildCount();
            int listBottom = getHeight() - getPaddingBottom();
            View lastChild = getChildAt(childCount - 1);
            int lastBottom = lastChild != null ? lastChild.getBottom() : listBottom;
            if (this.mFirstPosition + childCount < this.mLastHandledItemCount || lastBottom > listBottom) {
                z = false;
            }
            this.mForceTranscriptScroll = z;
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mInLayout = true;
        int childCount = getChildCount();
        if (changed) {
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).forceLayout();
            }
            this.mRecycler.markChildrenDirty();
        }
        layoutChildren();
        this.mOverscrollMax = (b - t) / 3;
        if (this.mFastScroll != null) {
            this.mFastScroll.onItemCountChanged(getChildCount(), this.mItemCount);
        }
        this.mInLayout = false;
    }

    /* access modifiers changed from: protected */
    public boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = super.setFrame(left, top, right, bottom);
        if (changed) {
            boolean visible = getWindowVisibility() == 0;
            if (this.mFiltered && visible && this.mPopup != null && this.mPopup.isShowing()) {
                positionPopup();
            }
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    public void layoutChildren() {
    }

    /* access modifiers changed from: package-private */
    public View getAccessibilityFocusedChild(View focusedView) {
        ViewParent viewParent = focusedView.getParent();
        while ((viewParent instanceof View) && viewParent != this) {
            focusedView = (View) viewParent;
            viewParent = viewParent.getParent();
        }
        if (!(viewParent instanceof View)) {
            return null;
        }
        return focusedView;
    }

    /* access modifiers changed from: package-private */
    public void updateScrollIndicators() {
        int i = 4;
        if (this.mScrollUp != null) {
            this.mScrollUp.setVisibility(canScrollUp() ? 0 : 4);
        }
        if (this.mScrollDown != null) {
            View view = this.mScrollDown;
            if (canScrollDown()) {
                i = 0;
            }
            view.setVisibility(i);
        }
    }

    private boolean canScrollUp() {
        boolean canScrollUp = true;
        boolean canScrollUp2 = this.mFirstPosition > 0;
        if (canScrollUp2 || getChildCount() <= 0) {
            return canScrollUp2;
        }
        if (getChildAt(0).getTop() >= this.mListPadding.top) {
            canScrollUp = false;
        }
        return canScrollUp;
    }

    private boolean canScrollDown() {
        int count = getChildCount();
        boolean canScrollDown = false;
        boolean canScrollDown2 = this.mFirstPosition + count < this.mItemCount;
        if (canScrollDown2 || count <= 0) {
            return canScrollDown2;
        }
        if (getChildAt(count - 1).getBottom() > this.mBottom - this.mListPadding.bottom) {
            canScrollDown = true;
        }
        return canScrollDown;
    }

    @ViewDebug.ExportedProperty
    public View getSelectedView() {
        if (this.mItemCount <= 0 || this.mSelectedPosition < 0) {
            return null;
        }
        return getChildAt(this.mSelectedPosition - this.mFirstPosition);
    }

    public int getListPaddingTop() {
        return this.mListPadding.top;
    }

    public int getListPaddingBottom() {
        return this.mListPadding.bottom;
    }

    public int getListPaddingLeft() {
        return this.mListPadding.left;
    }

    public int getListPaddingRight() {
        return this.mListPadding.right;
    }

    /* access modifiers changed from: package-private */
    public View obtainView(int position, boolean[] outMetadata) {
        Trace.traceBegin(8, "obtainView");
        outMetadata[0] = false;
        View transientView = this.mRecycler.getTransientStateView(position);
        if (transientView != null) {
            if (((LayoutParams) transientView.getLayoutParams()).viewType == this.mAdapter.getItemViewType(position)) {
                View updatedView = this.mAdapter.getView(position, transientView, this);
                if (updatedView != transientView) {
                    setItemViewLayoutParams(updatedView, position);
                    this.mRecycler.addScrapView(updatedView, position);
                }
            }
            outMetadata[0] = true;
            transientView.dispatchFinishTemporaryDetach();
            return transientView;
        }
        View scrapView = this.mRecycler.getScrapView(position);
        View child = this.mAdapter.getView(position, scrapView, this);
        if (scrapView != null) {
            if (child != scrapView) {
                this.mRecycler.addScrapView(scrapView, position);
            } else if (child.isTemporarilyDetached()) {
                outMetadata[0] = true;
                child.dispatchFinishTemporaryDetach();
            }
        }
        if (this.mCacheColorHint != 0) {
            child.setDrawingCacheBackgroundColor(this.mCacheColorHint);
        }
        if (child.getImportantForAccessibility() == 0) {
            child.setImportantForAccessibility(1);
        }
        setItemViewLayoutParams(child, position);
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            if (this.mAccessibilityDelegate == null) {
                this.mAccessibilityDelegate = new ListItemAccessibilityDelegate();
            }
            if (child.getAccessibilityDelegate() == null) {
                child.setAccessibilityDelegate(this.mAccessibilityDelegate);
            }
        }
        Trace.traceEnd(8);
        return child;
    }

    private void setItemViewLayoutParams(View child, int position) {
        LayoutParams lp;
        ViewGroup.LayoutParams vlp = child.getLayoutParams();
        if (vlp == null) {
            lp = (LayoutParams) generateDefaultLayoutParams();
        } else if (!checkLayoutParams(vlp)) {
            lp = (LayoutParams) generateLayoutParams(vlp);
        } else {
            lp = (LayoutParams) vlp;
        }
        if (this.mAdapterHasStableIds) {
            lp.itemId = this.mAdapter.getItemId(position);
        }
        lp.viewType = getHwItemViewType(position);
        lp.isEnabled = this.mAdapter.isEnabled(position);
        if (lp != vlp) {
            child.setLayoutParams(lp);
        }
    }

    public void onInitializeAccessibilityNodeInfoForItem(View view, int position, AccessibilityNodeInfo info) {
        boolean isItemEnabled;
        ListAdapter adapter = getAdapter();
        if (position != -1 && adapter != null && position <= adapter.getCount() - 1) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof LayoutParams) {
                isItemEnabled = ((LayoutParams) lp).isEnabled;
            } else {
                isItemEnabled = false;
            }
            if (!isEnabled() || !isItemEnabled) {
                info.setEnabled(false);
                return;
            }
            if (position == getSelectedItemPosition()) {
                info.setSelected(true);
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_SELECTION);
            } else {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SELECT);
            }
            if (isItemClickable(view)) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
                info.setClickable(true);
            }
            if (isLongClickable()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_LONG_CLICK);
                info.setLongClickable(true);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isItemClickable(View view) {
        return !view.hasExplicitFocusable();
    }

    /* access modifiers changed from: package-private */
    public void positionSelectorLikeTouch(int position, View sel, float x, float y) {
        positionSelector(position, sel, true, x, y);
    }

    /* access modifiers changed from: package-private */
    public void positionSelectorLikeFocus(int position, View sel) {
        if (this.mSelector == null || this.mSelectorPosition == position || position == -1) {
            positionSelector(position, sel);
            return;
        }
        Rect bounds = this.mSelectorRect;
        positionSelector(position, sel, true, bounds.exactCenterX(), bounds.exactCenterY());
    }

    /* access modifiers changed from: package-private */
    public void positionSelector(int position, View sel) {
        positionSelector(position, sel, false, -1.0f, -1.0f);
    }

    private void positionSelector(int position, View sel, boolean manageHotspot, float x, float y) {
        boolean positionChanged = position != this.mSelectorPosition;
        if (position != -1) {
            this.mSelectorPosition = position;
        }
        Rect selectorRect = this.mSelectorRect;
        selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom());
        if (position != -1) {
            adjustSelector(position, selectorRect);
        }
        if (sel instanceof SelectionBoundsAdjuster) {
            ((SelectionBoundsAdjuster) sel).adjustListItemSelectionBounds(selectorRect);
        }
        selectorRect.left -= this.mSelectionLeftPadding;
        selectorRect.top -= this.mSelectionTopPadding;
        selectorRect.right += this.mSelectionRightPadding;
        selectorRect.bottom += this.mSelectionBottomPadding;
        boolean isChildViewEnabled = sel.isEnabled();
        if (this.mIsChildViewEnabled != isChildViewEnabled) {
            this.mIsChildViewEnabled = isChildViewEnabled;
        }
        Drawable selector = this.mSelector;
        if (selector != null) {
            if (positionChanged) {
                selector.setVisible(false, false);
                selector.setState(StateSet.NOTHING);
            }
            selector.setBounds(selectorRect);
            if (positionChanged) {
                if (getVisibility() == 0) {
                    selector.setVisible(true, false);
                }
                updateSelectorState();
            }
            if (manageHotspot) {
                selector.setHotspot(x, y);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        int saveCount = 0;
        boolean clipToPadding = (this.mGroupFlags & 34) == 34;
        if (clipToPadding) {
            saveCount = canvas.save();
            int scrollX = this.mScrollX;
            int scrollY = this.mScrollY;
            canvas.clipRect(this.mPaddingLeft + scrollX, this.mPaddingTop + scrollY, ((this.mRight + scrollX) - this.mLeft) - this.mPaddingRight, ((this.mBottom + scrollY) - this.mTop) - this.mPaddingBottom);
            this.mGroupFlags &= -35;
        }
        int scrollX2 = this.mDrawSelectorOnTop;
        if (scrollX2 == 0) {
            drawSelector(canvas);
        }
        super.dispatchDraw(canvas);
        if (scrollX2 != 0) {
            drawSelector(canvas);
        }
        if (clipToPadding) {
            canvas.restoreToCount(saveCount);
            this.mGroupFlags = 34 | this.mGroupFlags;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isPaddingOffsetRequired() {
        return (this.mGroupFlags & 34) != 34;
    }

    /* access modifiers changed from: protected */
    public int getLeftPaddingOffset() {
        if ((this.mGroupFlags & 34) == 34) {
            return 0;
        }
        return -this.mPaddingLeft;
    }

    /* access modifiers changed from: protected */
    public int getTopPaddingOffset() {
        if ((this.mGroupFlags & 34) == 34) {
            return 0;
        }
        return -this.mPaddingTop;
    }

    /* access modifiers changed from: protected */
    public int getRightPaddingOffset() {
        if ((this.mGroupFlags & 34) == 34) {
            return 0;
        }
        return this.mPaddingRight;
    }

    /* access modifiers changed from: protected */
    public int getBottomPaddingOffset() {
        if ((this.mGroupFlags & 34) == 34) {
            return 0;
        }
        return this.mPaddingBottom;
    }

    /* access modifiers changed from: protected */
    public void internalSetPadding(int left, int top, int right, int bottom) {
        super.internalSetPadding(left, top, right, bottom);
        if (isLayoutRequested()) {
            handleBoundsChange();
        }
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        handleBoundsChange();
        if (this.mFastScroll != null) {
            this.mFastScroll.onSizeChanged(w, h, oldw, oldh);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleBoundsChange() {
        if (!this.mInLayout) {
            int childCount = getChildCount();
            if (childCount > 0) {
                this.mDataChanged = true;
                rememberSyncState();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    ViewGroup.LayoutParams lp = child.getLayoutParams();
                    if (lp == null || lp.width < 1 || lp.height < 1) {
                        child.forceLayout();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean touchModeDrawsInPressedState() {
        switch (this.mTouchMode) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShowSelector() {
        return (isFocused() && !isInTouchMode()) || (touchModeDrawsInPressedState() && isPressed());
    }

    private void drawSelector(Canvas canvas) {
        if (shouldDrawSelector()) {
            Drawable selector = this.mSelector;
            selector.setBounds(this.mSelectorRect);
            selector.draw(canvas);
        }
    }

    public final boolean shouldDrawSelector() {
        return !this.mSelectorRect.isEmpty();
    }

    public void setDrawSelectorOnTop(boolean onTop) {
        this.mDrawSelectorOnTop = onTop;
    }

    public void setSelector(int resID) {
        setSelector(getContext().getDrawable(resID));
    }

    public void setSelector(Drawable sel) {
        if (this.mSelector != null) {
            this.mSelector.setCallback(null);
            unscheduleDrawable(this.mSelector);
        }
        this.mSelector = sel;
        Rect padding = new Rect();
        sel.getPadding(padding);
        this.mSelectionLeftPadding = padding.left;
        this.mSelectionTopPadding = padding.top;
        this.mSelectionRightPadding = padding.right;
        this.mSelectionBottomPadding = padding.bottom;
        sel.setCallback(this);
        updateSelectorState();
    }

    public Drawable getSelector() {
        return this.mSelector;
    }

    /* access modifiers changed from: package-private */
    public void keyPressed() {
        if (isEnabled() && isClickable()) {
            Drawable selector = this.mSelector;
            Rect selectorRect = this.mSelectorRect;
            if (selector != null && ((isFocused() || touchModeDrawsInPressedState()) && !selectorRect.isEmpty())) {
                View v = getChildAt(this.mSelectedPosition - this.mFirstPosition);
                if (v != null) {
                    if (!v.hasExplicitFocusable()) {
                        v.setPressed(true);
                    } else {
                        return;
                    }
                }
                setPressed(true);
                boolean longClickable = isLongClickable();
                Drawable d = selector.getCurrent();
                if (d != null && (d instanceof TransitionDrawable)) {
                    if (longClickable) {
                        ((TransitionDrawable) d).startTransition(ViewConfiguration.getLongPressTimeout());
                    } else {
                        ((TransitionDrawable) d).resetTransition();
                    }
                }
                if (longClickable && !this.mDataChanged) {
                    if (this.mPendingCheckForKeyLongPress == null) {
                        this.mPendingCheckForKeyLongPress = new CheckForKeyLongPress();
                    }
                    this.mPendingCheckForKeyLongPress.rememberWindowAttachCount();
                    postDelayed(this.mPendingCheckForKeyLongPress, (long) ViewConfiguration.getLongPressTimeout());
                }
            }
        }
    }

    public void setScrollIndicators(View up, View down) {
        this.mScrollUp = up;
        this.mScrollDown = down;
    }

    /* access modifiers changed from: package-private */
    public void updateSelectorState() {
        Drawable selector = this.mSelector;
        if (selector != null && selector.isStateful()) {
            if (!shouldShowSelector()) {
                selector.setState(StateSet.NOTHING);
            } else if (selector.setState(getDrawableStateForSelector())) {
                invalidateDrawable(selector);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        updateSelectorState();
    }

    private int[] getDrawableStateForSelector() {
        if (this.mIsChildViewEnabled) {
            return super.getDrawableState();
        }
        int enabledState = ENABLED_STATE_SET[0];
        int[] state = onCreateDrawableState(1);
        int enabledPos = -1;
        int i = state.length - 1;
        while (true) {
            if (i < 0) {
                break;
            } else if (state[i] == enabledState) {
                enabledPos = i;
                break;
            } else {
                i--;
            }
        }
        if (enabledPos >= 0) {
            System.arraycopy(state, enabledPos + 1, state, enabledPos, (state.length - enabledPos) - 1);
        }
        return state;
    }

    public boolean verifyDrawable(Drawable dr) {
        return this.mSelector == dr || super.verifyDrawable(dr);
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mSelector != null) {
            this.mSelector.jumpToCurrentState();
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewTreeObserver treeObserver = getViewTreeObserver();
        treeObserver.addOnTouchModeChangeListener(this);
        if (this.mTextFilterEnabled && this.mPopup != null && !this.mGlobalLayoutListenerAddedFilter) {
            treeObserver.addOnGlobalLayoutListener(this);
        }
        if (this.mAdapter != null && this.mDataSetObserver == null) {
            this.mDataSetObserver = new AdapterDataSetObserver();
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
            this.mDataChanged = true;
            this.mOldItemCount = this.mItemCount;
            this.mItemCount = this.mAdapter.getCount();
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mIsDetaching = true;
        dismissPopup();
        this.mRecycler.clear();
        ViewTreeObserver treeObserver = getViewTreeObserver();
        treeObserver.removeOnTouchModeChangeListener(this);
        if (this.mTextFilterEnabled && this.mPopup != null) {
            treeObserver.removeOnGlobalLayoutListener(this);
            this.mGlobalLayoutListenerAddedFilter = false;
        }
        if (!(this.mAdapter == null || this.mDataSetObserver == null)) {
            this.mAdapter.unregisterDataSetObserver(this.mDataSetObserver);
            this.mDataSetObserver = null;
        }
        if (this.mScrollStrictSpan != null) {
            this.mScrollStrictSpan.finish();
            this.mScrollStrictSpan = null;
        }
        if (this.mFlingStrictSpan != null) {
            this.mFlingStrictSpan.finish();
            this.mFlingStrictSpan = null;
        }
        if (this.mFlingRunnable != null) {
            removeCallbacks(this.mFlingRunnable);
        }
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        if (this.mClearScrollingCache != null) {
            removeCallbacks(this.mClearScrollingCache);
        }
        if (this.mPerformClick != null) {
            removeCallbacks(this.mPerformClick);
        }
        if (this.mTouchModeReset != null) {
            removeCallbacks(this.mTouchModeReset);
            this.mTouchModeReset.run();
        }
        this.mIsDetaching = false;
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        int touchMode = isInTouchMode() ^ 1;
        if (!hasWindowFocus) {
            setChildrenDrawingCacheEnabled(false);
            if (this.mFlingRunnable != null) {
                removeCallbacks(this.mFlingRunnable);
                boolean unused = this.mFlingRunnable.mSuppressIdleStateChangeCall = false;
                this.mFlingRunnable.endFling();
                if (this.mPositionScroller != null) {
                    this.mPositionScroller.stop();
                }
                if (this.mScrollY != 0) {
                    this.mScrollY = 0;
                    invalidateParentCaches();
                    finishGlows();
                    invalidate();
                }
            }
            dismissPopup();
            if (touchMode == 1) {
                this.mResurrectToPosition = this.mSelectedPosition;
            }
        } else {
            if (this.mFiltered && !this.mPopupHidden) {
                showPopup();
            }
            if (!(touchMode == this.mLastTouchMode || this.mLastTouchMode == -1)) {
                if (touchMode == 1) {
                    resurrectSelection();
                } else {
                    hideSelector();
                    this.mLayoutMode = 0;
                    layoutChildren();
                }
            }
        }
        this.mLastTouchMode = (int) touchMode;
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (this.mFastScroll != null) {
            this.mFastScroll.setScrollbarPosition(getVerticalScrollbarPosition());
        }
    }

    /* access modifiers changed from: package-private */
    public ContextMenu.ContextMenuInfo createContextMenuInfo(View view, int position, long id) {
        return new AdapterView.AdapterContextMenuInfo(view, position, id);
    }

    public void onCancelPendingInputEvents() {
        super.onCancelPendingInputEvents();
        if (this.mPerformClick != null) {
            removeCallbacks(this.mPerformClick);
        }
        if (this.mPendingCheckForTap != null) {
            removeCallbacks(this.mPendingCheckForTap);
        }
        if (this.mPendingCheckForLongPress != null) {
            removeCallbacks(this.mPendingCheckForLongPress);
        }
        if (this.mPendingCheckForKeyLongPress != null) {
            removeCallbacks(this.mPendingCheckForKeyLongPress);
        }
    }

    private boolean performStylusButtonPressAction(MotionEvent ev) {
        if (this.mChoiceMode == 3 && this.mChoiceActionMode == null) {
            View child = getChildAt(this.mMotionPosition - this.mFirstPosition);
            if (child != null && performLongPress(child, this.mMotionPosition, this.mAdapter.getItemId(this.mMotionPosition))) {
                this.mTouchMode = -1;
                setPressed(false);
                child.setPressed(false);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean performLongPress(View child, int longPressPosition, long longPressId) {
        return performLongPress(child, longPressPosition, longPressId, -1.0f, -1.0f);
    }

    /* access modifiers changed from: package-private */
    public boolean performLongPress(View child, int longPressPosition, long longPressId, float x, float y) {
        if (this.mChoiceMode == 3) {
            if (this.mChoiceActionMode == null) {
                ActionMode startActionMode = startActionMode(this.mMultiChoiceModeCallback);
                this.mChoiceActionMode = startActionMode;
                if (startActionMode != null) {
                    setItemChecked(longPressPosition, true);
                    performHapticFeedback(0);
                }
            }
            return true;
        }
        boolean handled = false;
        if (this.mOnItemLongClickListener != null) {
            handled = this.mOnItemLongClickListener.onItemLongClick(this, child, longPressPosition, longPressId);
        }
        if (!handled) {
            this.mContextMenuInfo = createContextMenuInfo(child, longPressPosition, longPressId);
            if (x == -1.0f || y == -1.0f) {
                handled = super.showContextMenuForChild(this);
            } else {
                handled = super.showContextMenuForChild(this, x, y);
            }
        }
        if (handled) {
            performHapticFeedback(0);
        }
        return handled;
    }

    /* access modifiers changed from: protected */
    public ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return this.mContextMenuInfo;
    }

    public boolean showContextMenu() {
        return showContextMenuInternal(0.0f, 0.0f, false);
    }

    public boolean showContextMenu(float x, float y) {
        return showContextMenuInternal(x, y, true);
    }

    private boolean showContextMenuInternal(float x, float y, boolean useOffsets) {
        int position = pointToPosition((int) x, (int) y);
        if (position != -1) {
            long id = this.mAdapter.getItemId(position);
            View child = getChildAt(position - this.mFirstPosition);
            if (child != null) {
                this.mContextMenuInfo = createContextMenuInfo(child, position, id);
                if (useOffsets) {
                    return super.showContextMenuForChild(this, x, y);
                }
                return super.showContextMenuForChild(this);
            }
        }
        if (useOffsets) {
            return super.showContextMenu(x, y);
        }
        return super.showContextMenu();
    }

    public boolean showContextMenuForChild(View originalView) {
        if (isShowingContextMenuWithCoords()) {
            return false;
        }
        return showContextMenuForChildInternal(originalView, 0.0f, 0.0f, false);
    }

    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return showContextMenuForChildInternal(originalView, x, y, true);
    }

    private boolean showContextMenuForChildInternal(View originalView, float x, float y, boolean useOffsets) {
        int longPressPosition = getPositionForView(originalView);
        if (longPressPosition < 0) {
            return false;
        }
        long longPressId = this.mAdapter.getItemId(longPressPosition);
        boolean handled = false;
        if (this.mOnItemLongClickListener != null) {
            handled = this.mOnItemLongClickListener.onItemLongClick(this, originalView, longPressPosition, longPressId);
        }
        if (!handled) {
            this.mContextMenuInfo = createContextMenuInfo(getChildAt(longPressPosition - this.mFirstPosition), longPressPosition, longPressId);
            if (useOffsets) {
                handled = super.showContextMenuForChild(originalView, x, y);
            } else {
                handled = super.showContextMenuForChild(originalView);
            }
        }
        return handled;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.isConfirmKey(keyCode)) {
            if (!isEnabled()) {
                return true;
            }
            if (isClickable() && isPressed() && this.mSelectedPosition >= 0 && this.mAdapter != null && this.mSelectedPosition < this.mAdapter.getCount()) {
                View view = getChildAt(this.mSelectedPosition - this.mFirstPosition);
                if (view != null) {
                    performItemClick(view, this.mSelectedPosition, this.mSelectedRowId);
                    view.setPressed(false);
                }
                setPressed(false);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /* access modifiers changed from: protected */
    public void dispatchSetPressed(boolean pressed) {
    }

    public void dispatchDrawableHotspotChanged(float x, float y) {
    }

    public int pointToPosition(int x, int y) {
        Rect frame = this.mTouchFrame;
        if (frame == null) {
            this.mTouchFrame = new Rect();
            frame = this.mTouchFrame;
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return this.mFirstPosition + i;
                }
            }
        }
        return -1;
    }

    public long pointToRowId(int x, int y) {
        int position = pointToPosition(x, y);
        if (position >= 0) {
            return this.mAdapter.getItemId(position);
        }
        return Long.MIN_VALUE;
    }

    private boolean startScrollIfNeeded(int x, int y, MotionEvent vtev) {
        int deltaY = y - this.mMotionY;
        int distance = Math.abs(deltaY);
        boolean overscroll = this.mScrollY != 0;
        if ((!overscroll && distance <= this.mTouchSlop) || (getNestedScrollAxes() & 2) != 0) {
            return false;
        }
        createScrollingCache();
        if (overscroll) {
            this.mTouchMode = 5;
            this.mMotionCorrection = 0;
        } else {
            this.mTouchMode = 3;
            this.mMotionCorrection = deltaY > 0 ? this.mTouchSlop : -this.mTouchSlop;
        }
        removeCallbacks(this.mPendingCheckForLongPress);
        setPressed(false);
        View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
        if (motionView != null) {
            motionView.setPressed(false);
        }
        reportScrollStateChange(1);
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        scrollIfNeeded(x, y, vtev);
        return true;
    }

    private void scrollIfNeeded(int x, int y, MotionEvent vtev) {
        int overScrollDistance;
        int incrementalDeltaY;
        int newDirection;
        int incrementalDeltaY2;
        int i;
        int motionIndex;
        int i2 = x;
        int i3 = y;
        MotionEvent motionEvent = vtev;
        int motionCorrectionCompensation = 0;
        int i4 = -1;
        if (this.mMotionCorrection != 0) {
            motionCorrectionCompensation = this.mMotionCorrection > 0 ? -1 : 1;
            this.mMotionCorrection += motionCorrectionCompensation;
        }
        int motionCorrectionCompensation2 = motionCorrectionCompensation;
        int rawDeltaY = i3 - this.mMotionY;
        int scrollOffsetCorrection = 0;
        int scrollConsumedCorrection = 0;
        if (this.mLastY == Integer.MIN_VALUE) {
            rawDeltaY -= this.mMotionCorrection;
        }
        if (dispatchNestedPreScroll(0, this.mLastY != Integer.MIN_VALUE ? this.mLastY - i3 : -rawDeltaY, this.mScrollConsumed, this.mScrollOffset)) {
            rawDeltaY += this.mScrollConsumed[1];
            scrollOffsetCorrection = -this.mScrollOffset[1];
            scrollConsumedCorrection = this.mScrollConsumed[1];
            if (motionEvent != null) {
                motionEvent.offsetLocation(0.0f, (float) this.mScrollOffset[1]);
                this.mNestedYOffset += this.mScrollOffset[1];
            }
        }
        int rawDeltaY2 = rawDeltaY;
        int scrollOffsetCorrection2 = scrollOffsetCorrection;
        int deltaY = rawDeltaY2;
        int incrementalDeltaY3 = this.mLastY != Integer.MIN_VALUE ? ((i3 - this.mLastY) + scrollConsumedCorrection) - motionCorrectionCompensation2 : deltaY;
        int lastYCorrection = 0;
        if (this.mTouchMode == 3) {
            if (this.mScrollStrictSpan == null) {
                this.mScrollStrictSpan = StrictMode.enterCriticalSpan("AbsListView-scroll");
            }
            if (i3 != this.mLastY) {
                if ((this.mGroupFlags & 524288) == 0 && Math.abs(rawDeltaY2) > this.mTouchSlop) {
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (this.mMotionPosition >= 0) {
                    motionIndex = this.mMotionPosition - this.mFirstPosition;
                } else {
                    motionIndex = getChildCount() / 2;
                }
                int motionIndex2 = motionIndex;
                int motionViewPrevTop = 0;
                View motionView = getChildAt(motionIndex2);
                if (motionView != null) {
                    motionViewPrevTop = motionView.getTop();
                }
                int motionViewPrevTop2 = motionViewPrevTop;
                boolean atEdge = false;
                if (incrementalDeltaY3 != 0) {
                    atEdge = trackMotionScroll(deltaY, incrementalDeltaY3);
                }
                boolean atEdge2 = atEdge;
                View motionView2 = getChildAt(motionIndex2);
                if (motionView2 != null) {
                    int motionViewRealTop = motionView2.getTop();
                    if (atEdge2) {
                        int overscroll = (-incrementalDeltaY3) - (motionViewRealTop - motionViewPrevTop2);
                        int overscroll2 = overscroll;
                        View view = motionView2;
                        int i5 = motionIndex2;
                        int incrementalDeltaY4 = incrementalDeltaY3;
                        if (dispatchNestedScroll(0, overscroll - incrementalDeltaY3, 0, overscroll2, this.mScrollOffset)) {
                            lastYCorrection = 0 - this.mScrollOffset[1];
                            if (motionEvent != null) {
                                motionEvent.offsetLocation(0.0f, (float) this.mScrollOffset[1]);
                                this.mNestedYOffset += this.mScrollOffset[1];
                            }
                            int i6 = deltaY;
                            int i7 = rawDeltaY2;
                        } else {
                            int i8 = deltaY;
                            int i9 = rawDeltaY2;
                            boolean atOverscrollEdge = overScrollBy(0, overscroll2, 0, this.mScrollY, 0, 0, 0, this.mOverscrollDistance, true);
                            if (atOverscrollEdge && this.mVelocityTracker != null) {
                                this.mVelocityTracker.clear();
                            }
                            int overscrollMode = getOverScrollMode();
                            if (overscrollMode == 0 || (overscrollMode == 1 && !contentFits())) {
                                if (!atOverscrollEdge) {
                                    this.mDirection = 0;
                                    this.mTouchMode = 5;
                                }
                                if (this.mEdgeGlowTop != null) {
                                    int incrementalDeltaY5 = incrementalDeltaY4;
                                    if (incrementalDeltaY5 > 0) {
                                        this.mEdgeGlowTop.onPull(((float) (-overscroll2)) / ((float) getHeight()), ((float) i2) / ((float) getWidth()));
                                        if (!this.mEdgeGlowBottom.isFinished()) {
                                            this.mEdgeGlowBottom.onRelease();
                                        }
                                        invalidateTopGlow();
                                    } else {
                                        int overscroll3 = overscroll2;
                                        if (incrementalDeltaY5 < 0) {
                                            this.mEdgeGlowBottom.onPull(((float) overscroll3) / ((float) getHeight()), 1.0f - (((float) i2) / ((float) getWidth())));
                                            if (!this.mEdgeGlowTop.isFinished()) {
                                                this.mEdgeGlowTop.onRelease();
                                            }
                                            invalidateBottomGlow();
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        int i10 = motionIndex2;
                        int i11 = deltaY;
                        int i12 = rawDeltaY2;
                    }
                    this.mMotionY = i3 + lastYCorrection + scrollOffsetCorrection2;
                } else {
                    int i13 = motionIndex2;
                    int i14 = deltaY;
                    int i15 = rawDeltaY2;
                }
                this.mLastY = i3 + lastYCorrection + scrollOffsetCorrection2;
                return;
            }
            int i16 = rawDeltaY2;
            return;
        }
        int rawDeltaY3 = rawDeltaY2;
        if (this.mTouchMode == 5 && i3 != this.mLastY) {
            int oldScroll = this.mScrollY;
            int newScroll = oldScroll - incrementalDeltaY3;
            if (i3 > this.mLastY) {
                i4 = 1;
            }
            int newDirection2 = i4;
            if (this.mDirection == 0) {
                this.mDirection = newDirection2;
            }
            int overScrollDistance2 = -incrementalDeltaY3;
            if ((newScroll >= 0 || oldScroll < 0) && (newScroll <= 0 || oldScroll > 0)) {
                overScrollDistance = overScrollDistance2;
                incrementalDeltaY = 0;
            } else {
                int overScrollDistance3 = -oldScroll;
                incrementalDeltaY = incrementalDeltaY3 + overScrollDistance3;
                overScrollDistance = overScrollDistance3;
            }
            if (overScrollDistance != 0) {
                incrementalDeltaY2 = incrementalDeltaY;
                int overScrollDistance4 = overScrollDistance;
                newDirection = newDirection2;
                int i17 = oldScroll;
                overScrollBy(0, overScrollDistance, 0, this.mScrollY, 0, 0, 0, this.mOverscrollDistance, true);
                int overscrollMode2 = getOverScrollMode();
                if (overscrollMode2 != 0 && (overscrollMode2 != 1 || contentFits())) {
                    int i18 = overScrollDistance4;
                } else if (this.mEdgeGlowTop == null) {
                } else if (rawDeltaY3 > 0) {
                    this.mEdgeGlowTop.onPull(((float) overScrollDistance4) / ((float) getHeight()), ((float) i2) / ((float) getWidth()));
                    if (!this.mEdgeGlowBottom.isFinished()) {
                        this.mEdgeGlowBottom.onRelease();
                    }
                    invalidateTopGlow();
                } else {
                    int overScrollDistance5 = overScrollDistance4;
                    if (rawDeltaY3 < 0) {
                        this.mEdgeGlowBottom.onPull(((float) overScrollDistance5) / ((float) getHeight()), 1.0f - (((float) i2) / ((float) getWidth())));
                        if (!this.mEdgeGlowTop.isFinished()) {
                            this.mEdgeGlowTop.onRelease();
                        }
                        invalidateBottomGlow();
                    }
                }
            } else {
                incrementalDeltaY2 = incrementalDeltaY;
                int i19 = overScrollDistance;
                newDirection = newDirection2;
                int i20 = oldScroll;
            }
            int incrementalDeltaY6 = incrementalDeltaY2;
            if (incrementalDeltaY6 != 0) {
                if (this.mScrollY != 0) {
                    i = 0;
                    this.mScrollY = 0;
                    invalidateParentIfNeeded();
                } else {
                    i = 0;
                }
                trackMotionScroll(incrementalDeltaY6, incrementalDeltaY6);
                this.mTouchMode = 3;
                int motionPosition = findClosestMotionRow(i3);
                this.mMotionCorrection = i;
                View motionView3 = getChildAt(motionPosition - this.mFirstPosition);
                if (motionView3 != null) {
                    i = motionView3.getTop();
                }
                this.mMotionViewOriginalTop = i;
                this.mMotionY = i3 + scrollOffsetCorrection2;
                this.mMotionPosition = motionPosition;
            }
            this.mLastY = i3 + 0 + scrollOffsetCorrection2;
            this.mDirection = newDirection;
            int i21 = incrementalDeltaY6;
        }
    }

    private void invalidateTopGlow() {
        if (this.mEdgeGlowTop != null) {
            boolean clipToPadding = getClipToPadding();
            int left = 0;
            int top = clipToPadding ? this.mPaddingTop : 0;
            if (clipToPadding) {
                left = this.mPaddingLeft;
            }
            invalidate(left, top, clipToPadding ? getWidth() - this.mPaddingRight : getWidth(), this.mEdgeGlowTop.getMaxHeight() + top);
        }
    }

    private void invalidateBottomGlow() {
        if (this.mEdgeGlowBottom != null) {
            boolean clipToPadding = getClipToPadding();
            int bottom = clipToPadding ? getHeight() - this.mPaddingBottom : getHeight();
            invalidate(clipToPadding ? this.mPaddingLeft : 0, bottom - this.mEdgeGlowBottom.getMaxHeight(), clipToPadding ? getWidth() - this.mPaddingRight : getWidth(), bottom);
        }
    }

    public void onTouchModeChanged(boolean isInTouchMode) {
        if (isInTouchMode) {
            hideSelector();
            if (getHeight() > 0 && getChildCount() > 0) {
                layoutChildren();
            }
            updateSelectorState();
            return;
        }
        int touchMode = this.mTouchMode;
        if (touchMode == 5 || touchMode == 6) {
            if (this.mFlingRunnable != null) {
                this.mFlingRunnable.endFling();
            }
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }
            if (this.mScrollY != 0) {
                this.mScrollY = 0;
                invalidateParentCaches();
                finishGlows();
                invalidate();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleScrollBarDragging(MotionEvent event) {
        return false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean z = true;
        if (!isEnabled()) {
            if (!isClickable() && !isLongClickable()) {
                z = false;
            }
            return z;
        }
        if (this.mPositionScroller != null && (!this.mIsAutoScroll || ev.getActionMasked() == 1)) {
            this.mPositionScroller.stop();
        }
        if (this.mIsDetaching || !isAttachedToWindow()) {
            return false;
        }
        startNestedScroll(2);
        if (this.mFastScroll != null && this.mFastScroll.onTouchEvent(ev)) {
            return true;
        }
        initVelocityTrackerIfNotExists();
        MotionEvent vtev = MotionEvent.obtain(ev);
        int actionMasked = ev.getActionMasked();
        if (actionMasked == 0) {
            this.mNestedYOffset = 0;
        }
        vtev.offsetLocation(0.0f, (float) this.mNestedYOffset);
        switch (actionMasked) {
            case 0:
                onTouchDown(ev);
                break;
            case 1:
                this.mIsAutoScroll = false;
                onTouchUpEx(ev);
                break;
            case 2:
                onTouchMove(ev, vtev);
                break;
            case 3:
                onTouchCancel();
                break;
            case 5:
                int y = ev.getActionIndex();
                int id = ev.getPointerId(y);
                int x = (int) ev.getX(y);
                int y2 = (int) ev.getY(y);
                this.mMotionCorrection = 0;
                this.mActivePointerId = id;
                this.mMotionX = x;
                this.mMotionY = y2;
                int motionPosition = pointToPosition(x, y2);
                if (motionPosition >= 0) {
                    this.mMotionViewOriginalTop = getChildAt(motionPosition - this.mFirstPosition).getTop();
                    dismissCurrentPressed();
                    this.mMotionPosition = motionPosition;
                }
                this.mLastY = y2;
                break;
            case 6:
                onSecondaryPointerUp(ev);
                int x2 = this.mMotionX;
                int y3 = this.mMotionY;
                int motionPosition2 = pointToPosition(x2, y3);
                if (motionPosition2 >= 0) {
                    this.mMotionViewOriginalTop = getChildAt(motionPosition2 - this.mFirstPosition).getTop();
                    this.mMotionPosition = motionPosition2;
                }
                this.mLastY = y3;
                break;
        }
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;
    }

    private void onTouchDown(MotionEvent ev) {
        if (SMART_SLIDE_PROPERTIES) {
            this.mMotionEventDownPosition = ev.getY();
        }
        this.mHasPerformedLongPress = false;
        this.mActivePointerId = ev.getPointerId(0);
        hideSelector();
        if (this.mTouchMode == 6) {
            this.mFlingRunnable.endFling();
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }
            this.mTouchMode = 5;
            this.mMotionX = (int) ev.getX();
            this.mMotionY = (int) ev.getY();
            this.mLastY = this.mMotionY;
            this.mMotionCorrection = 0;
            this.mDirection = 0;
        } else {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            int motionPosition = pointToPosition(x, y);
            if (!this.mDataChanged) {
                if (this.mTouchMode == 4) {
                    createScrollingCache();
                    this.mTouchMode = 3;
                    this.mMotionCorrection = 0;
                    motionPosition = findMotionRow(y);
                    this.mFlingRunnable.flywheelTouch();
                } else if (motionPosition >= 0 && getAdapter().isEnabled(motionPosition)) {
                    this.mTouchMode = 0;
                    enterMultiSelectModeIfNeeded(motionPosition, x);
                    if (this.mPendingCheckForTap == null) {
                        this.mPendingCheckForTap = new CheckForTap();
                    }
                    this.mPendingCheckForTap.x = ev.getX();
                    this.mPendingCheckForTap.y = ev.getY();
                    postDelayed(this.mPendingCheckForTap, (long) ViewConfiguration.getTapTimeout());
                }
            }
            if (motionPosition >= 0) {
                this.mMotionViewOriginalTop = getChildAt(motionPosition - this.mFirstPosition).getTop();
            }
            this.mMotionX = x;
            this.mMotionY = y;
            this.mMotionPosition = motionPosition;
            this.mLastY = Integer.MIN_VALUE;
        }
        if (this.mTouchMode == 0 && this.mMotionPosition != -1 && performButtonActionOnTouchDown(ev)) {
            removeCallbacks(this.mPendingCheckForTap);
        }
    }

    private void onTouchMove(MotionEvent ev, MotionEvent vtev) {
        if (!this.mHasPerformedLongPress) {
            int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
            if (pointerIndex == -1) {
                pointerIndex = 0;
                this.mActivePointerId = ev.getPointerId(0);
            }
            if (this.mDataChanged) {
                layoutChildren();
            }
            int y = (int) ev.getY(pointerIndex);
            int i = this.mTouchMode;
            if (i != 5) {
                switch (i) {
                    case 0:
                    case 1:
                    case 2:
                        if (!startScrollIfNeeded((int) ev.getX(pointerIndex), y, vtev)) {
                            View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
                            float x = ev.getX(pointerIndex);
                            if (pointInView(x, (float) y, (float) this.mTouchSlop)) {
                                if (motionView != null) {
                                    float[] point = this.mTmpPoint;
                                    point[0] = x;
                                    point[1] = (float) y;
                                    transformPointToViewLocal(point, motionView);
                                    motionView.drawableHotspotChanged(point[0], point[1]);
                                    break;
                                }
                            } else {
                                setPressed(false);
                                if (motionView != null) {
                                    motionView.setPressed(false);
                                }
                                removeCallbacks(this.mTouchMode == 0 ? this.mPendingCheckForTap : this.mPendingCheckForLongPress);
                                this.mTouchMode = 2;
                                updateSelectorState();
                                break;
                            }
                        }
                        break;
                    case 3:
                        break;
                }
            }
            scrollIfNeeded((int) ev.getX(pointerIndex), y, vtev);
            onMultiSelectMove(ev, pointerIndex);
        }
    }

    /* access modifiers changed from: protected */
    public void onTouchUpEx(MotionEvent ev) {
        onTouchUp(ev);
    }

    private void onTouchUp(MotionEvent ev) {
        boolean flingVelocity;
        if (SMART_SLIDE_PROPERTIES) {
            this.mMotionEventUpPosition = ev.getY();
        }
        int i = this.mTouchMode;
        if (i != 5) {
            switch (i) {
                case 0:
                case 1:
                case 2:
                    int childCount = this.mMotionPosition;
                    final View child = getChildAt(childCount - this.mFirstPosition);
                    if (child != null) {
                        if (this.mTouchMode != 0) {
                            child.setPressed(false);
                        }
                        float x = ev.getX();
                        if ((x > ((float) this.mListPadding.left) && x < ((float) (getWidth() - this.mListPadding.right))) && !child.hasExplicitFocusable()) {
                            if (this.mPerformClick == null) {
                                this.mPerformClick = new PerformClick();
                            }
                            final PerformClick performClick = this.mPerformClick;
                            performClick.mClickMotionPosition = childCount;
                            performClick.rememberWindowAttachCount();
                            this.mResurrectToPosition = childCount;
                            if (this.mTouchMode == 0 || this.mTouchMode == 1) {
                                removeCallbacks(this.mTouchMode == 0 ? this.mPendingCheckForTap : this.mPendingCheckForLongPress);
                                this.mLayoutMode = 0;
                                if (this.mDataChanged || !this.mAdapter.isEnabled(childCount)) {
                                    this.mTouchMode = -1;
                                    updateSelectorState();
                                } else {
                                    this.mTouchMode = 1;
                                    setSelectedPositionInt(this.mMotionPosition);
                                    layoutChildren();
                                    child.setPressed(true);
                                    positionSelector(this.mMotionPosition, child);
                                    setPressed(true);
                                    if (this.mSelector != null) {
                                        Drawable d = this.mSelector.getCurrent();
                                        if (d != null && (d instanceof TransitionDrawable)) {
                                            ((TransitionDrawable) d).resetTransition();
                                        }
                                        this.mSelector.setHotspot(x, ev.getY());
                                    }
                                    if (this.mTouchModeReset != null) {
                                        removeCallbacks(this.mTouchModeReset);
                                    }
                                    this.mTouchModeReset = new Runnable() {
                                        public void run() {
                                            Runnable unused = AbsListView.this.mTouchModeReset = null;
                                            AbsListView.this.mTouchMode = -1;
                                            child.setPressed(false);
                                            AbsListView.this.setPressed(false);
                                            if (!AbsListView.this.mDataChanged && !AbsListView.this.mIsDetaching && AbsListView.this.isAttachedToWindow()) {
                                                performClick.run();
                                            }
                                        }
                                    };
                                    postDelayed(this.mTouchModeReset, (long) getPressedStateDuration());
                                }
                                return;
                            } else if (!this.mDataChanged && this.mAdapter.isEnabled(childCount)) {
                                performClick.run();
                            }
                        }
                    }
                    this.mTouchMode = -1;
                    updateSelectorState();
                    break;
                case 3:
                    int childCount2 = getChildCount();
                    if (childCount2 <= 0) {
                        this.mTouchMode = -1;
                        reportScrollStateChange(0);
                        break;
                    } else {
                        int firstChildTop = getChildAt(0).getTop();
                        int lastChildBottom = getChildAt(childCount2 - 1).getBottom();
                        int contentTop = this.mListPadding.top;
                        int contentBottom = getHeight() - this.mListPadding.bottom;
                        if (this.mFirstPosition == 0 && firstChildTop >= contentTop && this.mFirstPosition + childCount2 < this.mItemCount && lastChildBottom <= getHeight() - contentBottom) {
                            this.mTouchMode = -1;
                            reportScrollStateChange(0);
                            break;
                        } else {
                            VelocityTracker velocityTracker = this.mVelocityTracker;
                            velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                            int initialVelocity = (int) (velocityTracker.getYVelocity(this.mActivePointerId) * this.mVelocityScale);
                            if (SMART_SLIDE_PROPERTIES) {
                                flingVelocity = ((float) Math.abs(initialVelocity)) > this.mFlingThreshold;
                            } else {
                                flingVelocity = Math.abs(initialVelocity) > this.mMinimumVelocity;
                            }
                            if (flingVelocity && ((this.mFirstPosition != 0 || firstChildTop != contentTop - this.mOverscrollDistance) && (this.mFirstPosition + childCount2 != this.mItemCount || lastChildBottom != this.mOverscrollDistance + contentBottom))) {
                                if (dispatchNestedPreFling(0.0f, (float) (-initialVelocity))) {
                                    this.mTouchMode = -1;
                                    reportScrollStateChange(0);
                                    break;
                                } else {
                                    if (this.mFlingRunnable == null) {
                                        this.mFlingRunnable = new FlingRunnable();
                                    }
                                    reportScrollStateChange(2);
                                    if (this.mIHwWechatOptimize.isWechatOptimizeEffect() && Math.abs(initialVelocity) > 3600) {
                                        if (Jlog.isBetaUser()) {
                                            Jlog.d(JlogConstants.JLID_MISC_EVENT_STAT, "ListViewSpeed", Math.abs(initialVelocity) / 2, "");
                                        }
                                        if (Math.abs(initialVelocity) > this.mIHwWechatOptimize.getWechatFlingVelocity()) {
                                            this.mIHwWechatOptimize.setWechatFling(true);
                                        } else {
                                            reportScrollStateChange(0);
                                        }
                                    }
                                    this.mFlingRunnable.start(-initialVelocity);
                                    dispatchNestedFling(0.0f, (float) (-initialVelocity), true);
                                    break;
                                }
                            } else {
                                this.mTouchMode = -1;
                                reportScrollStateChange(0);
                                if (this.mFlingRunnable != null) {
                                    this.mFlingRunnable.endFling();
                                    this.mFlingRunnable.startSpringback();
                                } else {
                                    this.mFlingRunnable = new FlingRunnable();
                                    this.mFlingRunnable.startSpringback();
                                }
                                if (this.mPositionScroller != null) {
                                    this.mPositionScroller.stop();
                                }
                                if (flingVelocity && !dispatchNestedPreFling(0.0f, (float) (-initialVelocity))) {
                                    dispatchNestedFling(0.0f, (float) (-initialVelocity), false);
                                    break;
                                }
                            }
                        }
                    }
                    break;
            }
        } else {
            if (this.mFlingRunnable == null) {
                this.mFlingRunnable = new FlingRunnable();
            }
            VelocityTracker velocityTracker2 = this.mVelocityTracker;
            velocityTracker2.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
            int initialVelocity2 = (int) velocityTracker2.getYVelocity(this.mActivePointerId);
            reportScrollStateChange(2);
            if (hasSpringAnimatorMask()) {
                this.mFlingRunnable.startSpringback();
            } else if (Math.abs(initialVelocity2) > this.mMinimumVelocity) {
                this.mFlingRunnable.startOverfling(-initialVelocity2);
            } else {
                this.mFlingRunnable.startSpringback();
            }
        }
        setPressed(false);
        if (this.mEdgeGlowTop != null) {
            this.mEdgeGlowTop.onRelease();
            this.mEdgeGlowBottom.onRelease();
        }
        invalidate();
        removeCallbacks(this.mPendingCheckForLongPress);
        recycleVelocityTracker();
        this.mActivePointerId = -1;
        if (this.mScrollStrictSpan != null) {
            this.mScrollStrictSpan.finish();
            this.mScrollStrictSpan = null;
        }
    }

    private void onTouchCancel() {
        switch (this.mTouchMode) {
            case 5:
                if (this.mFlingRunnable == null) {
                    this.mFlingRunnable = new FlingRunnable();
                }
                this.mFlingRunnable.startSpringback();
                break;
            case 6:
                break;
            default:
                this.mTouchMode = -1;
                setPressed(false);
                View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
                if (motionView != null) {
                    motionView.setPressed(false);
                }
                clearScrollingCache();
                removeCallbacks(this.mPendingCheckForLongPress);
                recycleVelocityTracker();
                break;
        }
        if (this.mEdgeGlowTop != null) {
            this.mEdgeGlowTop.onRelease();
            this.mEdgeGlowBottom.onRelease();
        }
        this.mActivePointerId = -1;
    }

    /* access modifiers changed from: protected */
    public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (this.mScrollY != scrollY) {
            onScrollChanged(this.mScrollX, scrollY, this.mScrollX, this.mScrollY);
            this.mScrollY = scrollY;
            invalidateParentIfNeeded();
            awakenScrollBars();
        }
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        float axisValue;
        int action = event.getAction();
        if (action == 8) {
            if (event.isFromSource(2)) {
                axisValue = event.getAxisValue(9);
            } else if (event.isFromSource(4194304)) {
                axisValue = event.getAxisValue(26);
            } else {
                axisValue = 0.0f;
            }
            int delta = Math.round(this.mVerticalScrollFactor * axisValue);
            if (delta != 0 && !trackMotionScroll(delta, delta)) {
                return true;
            }
        } else if (action == 11 && event.isFromSource(2)) {
            int actionButton = event.getActionButton();
            if ((actionButton == 32 || actionButton == 2) && ((this.mTouchMode == 0 || this.mTouchMode == 1) && performStylusButtonPressAction(event))) {
                removeCallbacks(this.mPendingCheckForLongPress);
                removeCallbacks(this.mPendingCheckForTap);
            }
        }
        return super.onGenericMotionEvent(event);
    }

    public void fling(int velocityY) {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable();
        }
        reportScrollStateChange(2);
        this.mFlingRunnable.start(velocityY);
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & 2) != 0;
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(2);
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int myUnconsumed;
        int myConsumed;
        int i = dyUnconsumed;
        View motionView = getChildAt(getChildCount() / 2);
        int oldTop = motionView != null ? motionView.getTop() : 0;
        if (motionView == null || trackMotionScroll(-i, -i)) {
            int myUnconsumed2 = i;
            if (motionView != null) {
                int myConsumed2 = motionView.getTop() - oldTop;
                myUnconsumed = myUnconsumed2 - myConsumed2;
                myConsumed = myConsumed2;
            } else {
                myUnconsumed = myUnconsumed2;
                myConsumed = 0;
            }
            dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null);
        }
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        int childCount = getChildCount();
        if (consumed || childCount <= 0 || !canScrollList((int) velocityY) || Math.abs(velocityY) <= ((float) this.mMinimumVelocity)) {
            return dispatchNestedFling(velocityX, velocityY, consumed);
        }
        reportScrollStateChange(2);
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable();
        }
        if (!dispatchNestedPreFling(0.0f, velocityY)) {
            this.mFlingRunnable.start((int) velocityY);
        }
        return true;
    }

    public void draw(Canvas canvas) {
        int translateY;
        int translateX;
        int height;
        int width;
        super.draw(canvas);
        if (this.mEdgeGlowTop != null) {
            int scrollY = this.mScrollY;
            boolean clipToPadding = getClipToPadding();
            int i = 0;
            if (clipToPadding) {
                width = (getWidth() - this.mPaddingLeft) - this.mPaddingRight;
                height = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
                translateX = this.mPaddingLeft;
                translateY = this.mPaddingTop;
            } else {
                width = getWidth();
                height = getHeight();
                translateX = 0;
                translateY = 0;
            }
            if (!this.mEdgeGlowTop.isFinished()) {
                int restoreCount = canvas.save();
                canvas.clipRect(translateX, translateY, translateX + width, this.mEdgeGlowTop.getMaxHeight() + translateY);
                canvas.translate((float) translateX, (float) (Math.min(0, this.mFirstPositionDistanceGuess + scrollY) + translateY));
                this.mEdgeGlowTop.setSize(width, height);
                if (this.mEdgeGlowTop.draw(canvas)) {
                    invalidateTopGlow();
                }
                canvas.restoreToCount(restoreCount);
            }
            if (!this.mEdgeGlowBottom.isFinished()) {
                int restoreCount2 = canvas.save();
                canvas.clipRect(translateX, (translateY + height) - this.mEdgeGlowBottom.getMaxHeight(), translateX + width, translateY + height);
                int edgeX = (-width) + translateX;
                int max = Math.max(getHeight(), this.mLastPositionDistanceGuess + scrollY);
                if (clipToPadding) {
                    i = this.mPaddingBottom;
                }
                canvas.translate((float) edgeX, (float) (max - i));
                canvas.rotate(180.0f, (float) width, 0.0f);
                this.mEdgeGlowBottom.setSize(width, height);
                if (this.mEdgeGlowBottom.draw(canvas)) {
                    invalidateBottomGlow();
                }
                canvas.restoreToCount(restoreCount2);
            }
        }
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

    public boolean onInterceptHoverEvent(MotionEvent event) {
        if (this.mFastScroll == null || !this.mFastScroll.onInterceptHoverEvent(event)) {
            return super.onInterceptHoverEvent(event);
        }
        return true;
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        if (this.mFastScroll != null) {
            PointerIcon pointerIcon = this.mFastScroll.onResolvePointerIcon(event, pointerIndex);
            if (pointerIcon != null) {
                return pointerIcon;
            }
        }
        return super.onResolvePointerIcon(event, pointerIndex);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        if (this.mIsDetaching || !isAttachedToWindow()) {
            return false;
        }
        if (this.mFastScroll != null && this.mFastScroll.onInterceptTouchEvent(ev)) {
            return true;
        }
        if (actionMasked != 6) {
            switch (actionMasked) {
                case 0:
                    int touchMode = this.mTouchMode;
                    if (touchMode == 6 || touchMode == 5) {
                        this.mMotionCorrection = 0;
                        return true;
                    }
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    this.mActivePointerId = ev.getPointerId(0);
                    int motionPosition = findMotionRow(y);
                    if (touchMode != 4 && motionPosition >= 0) {
                        this.mMotionViewOriginalTop = getChildAt(motionPosition - this.mFirstPosition).getTop();
                        this.mMotionX = x;
                        this.mMotionY = y;
                        this.mMotionPosition = motionPosition;
                        this.mTouchMode = 0;
                        enterMultiSelectModeIfNeeded(motionPosition, x);
                        clearScrollingCache();
                    }
                    this.mLastY = Integer.MIN_VALUE;
                    initOrResetVelocityTracker();
                    this.mVelocityTracker.addMovement(ev);
                    this.mNestedYOffset = 0;
                    startNestedScroll(2);
                    if (touchMode == 4) {
                        return true;
                    }
                    break;
                case 1:
                case 3:
                    this.mTouchMode = -1;
                    this.mActivePointerId = -1;
                    recycleVelocityTracker();
                    reportScrollStateChange(0);
                    stopNestedScroll();
                    break;
                case 2:
                    if (this.mTouchMode == 0) {
                        int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                        if (pointerIndex == -1) {
                            pointerIndex = 0;
                            this.mActivePointerId = ev.getPointerId(0);
                        }
                        initVelocityTrackerIfNotExists();
                        this.mVelocityTracker.addMovement(ev);
                        if (startScrollIfNeeded((int) ev.getX(pointerIndex), (int) ev.getY(pointerIndex), null)) {
                            return true;
                        }
                    }
                    break;
            }
        } else {
            onSecondaryPointerUp(ev);
        }
        return false;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = (ev.getAction() & 65280) >> 8;
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mMotionX = (int) ev.getX(newPointerIndex);
            this.mMotionY = (int) ev.getY(newPointerIndex);
            this.mMotionCorrection = 0;
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    public void addTouchables(ArrayList<View> views) {
        int count = getChildCount();
        int firstPosition = this.mFirstPosition;
        ListAdapter adapter = this.mAdapter;
        if (adapter != null) {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (adapter.isEnabled(firstPosition + i)) {
                    views.add(child);
                }
                child.addTouchables(views);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportScrollStateChange(int newState) {
        if (newState != this.mLastScrollState && this.mOnScrollListener != null) {
            this.mLastScrollState = newState;
            this.mOnScrollListener.onScrollStateChanged(this, newState);
        }
    }

    public void registerVelocityListener(HwPerfSpeedLoader hwPerfSpeedLoader) {
        this.mHwPerfSpeedLoader = hwPerfSpeedLoader;
    }

    public void setFriction(float friction) {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable();
        }
        this.mFlingRunnable.mScroller.setFriction(friction);
    }

    public void setVelocityScale(float scale) {
        this.mVelocityScale = scale;
    }

    /* access modifiers changed from: package-private */
    public AbsPositionScroller createPositionScroller() {
        return new PositionScroller();
    }

    public void smoothScrollToPosition(int position) {
        if (this.mPositionScroller == null) {
            this.mPositionScroller = createPositionScroller();
        }
        this.mPositionScroller.start(position);
    }

    public void smoothScrollToPositionFromTop(int position, int offset, int duration) {
        if (this.mPositionScroller == null) {
            this.mPositionScroller = createPositionScroller();
        }
        this.mPositionScroller.startWithOffset(position, offset, duration);
    }

    public void smoothScrollToPositionFromTop(int position, int offset) {
        if (this.mPositionScroller == null) {
            this.mPositionScroller = createPositionScroller();
        }
        this.mPositionScroller.startWithOffset(position, offset);
    }

    public void smoothScrollToPosition(int position, int boundPosition) {
        if (this.mPositionScroller == null) {
            this.mPositionScroller = createPositionScroller();
        }
        this.mPositionScroller.start(position, boundPosition);
    }

    public void smoothScrollBy(int distance, int duration) {
        smoothScrollBy(distance, duration, false, false);
    }

    /* access modifiers changed from: package-private */
    public void smoothScrollBy(int distance, int duration, boolean linear, boolean suppressEndFlingStateChangeCall) {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable();
        }
        int firstPos = this.mFirstPosition;
        int childCount = getChildCount();
        int lastPos = firstPos + childCount;
        int topLimit = getPaddingTop();
        int bottomLimit = getHeight() - getPaddingBottom();
        if (distance == 0 || this.mItemCount == 0 || childCount == 0 || ((firstPos == 0 && getChildAt(0).getTop() == topLimit && distance < 0) || (lastPos == this.mItemCount && getChildAt(childCount - 1).getBottom() == bottomLimit && distance > 0))) {
            this.mFlingRunnable.endFling();
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
                return;
            }
            return;
        }
        reportScrollStateChange(2);
        this.mFlingRunnable.startScroll(distance, duration, linear, suppressEndFlingStateChangeCall);
    }

    /* access modifiers changed from: package-private */
    public void smoothScrollByOffset(int position) {
        int index = -1;
        if (position < 0) {
            index = getFirstVisiblePosition();
        } else if (position > 0) {
            index = getLastVisiblePosition();
        }
        if (index > -1) {
            View child = getChildAt(index - getFirstVisiblePosition());
            if (child != null) {
                Rect visibleRect = new Rect();
                if (child.getGlobalVisibleRect(visibleRect)) {
                    float visibleArea = ((float) (visibleRect.width() * visibleRect.height())) / ((float) (child.getWidth() * child.getHeight()));
                    if (position < 0 && visibleArea < 0.75f) {
                        index++;
                    } else if (position > 0 && visibleArea < 0.75f) {
                        index--;
                    }
                }
                smoothScrollToPosition(Math.max(0, Math.min(getCount(), index + position)));
            }
        }
    }

    private void createScrollingCache() {
        if (this.mScrollingCacheEnabled && !this.mCachingStarted && !isHardwareAccelerated()) {
            setChildrenDrawnWithCacheEnabled(true);
            setChildrenDrawingCacheEnabled(true);
            this.mCachingActive = true;
            this.mCachingStarted = true;
        }
    }

    /* access modifiers changed from: private */
    public void clearScrollingCache() {
        if (!isHardwareAccelerated()) {
            if (this.mClearScrollingCache == null) {
                this.mClearScrollingCache = new Runnable() {
                    public void run() {
                        if (AbsListView.this.mCachingStarted) {
                            AbsListView absListView = AbsListView.this;
                            AbsListView.this.mCachingActive = false;
                            absListView.mCachingStarted = false;
                            AbsListView.this.setChildrenDrawnWithCacheEnabled(false);
                            if ((AbsListView.this.mPersistentDrawingCache & 2) == 0) {
                                AbsListView.this.setChildrenDrawingCacheEnabled(false);
                            }
                            if (!AbsListView.this.isAlwaysDrawnWithCacheEnabled()) {
                                AbsListView.this.invalidate();
                            }
                        }
                    }
                };
            }
            post(this.mClearScrollingCache);
        }
    }

    public void scrollListBy(int y) {
        trackMotionScroll(-y, -y);
    }

    public boolean canScrollList(int direction) {
        int childCount = getChildCount();
        boolean z = false;
        if (childCount == 0) {
            return false;
        }
        int firstPosition = this.mFirstPosition;
        Rect listPadding = this.mListPadding;
        if (direction > 0) {
            int lastBottom = getChildAt(childCount - 1).getBottom();
            if (firstPosition + childCount < this.mItemCount || lastBottom > getHeight() - listPadding.bottom) {
                z = true;
            }
            return z;
        }
        int firstTop = getChildAt(0).getTop();
        if (firstPosition > 0 || firstTop < listPadding.top) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x01d4  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x01ed  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0177  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0185  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x018d  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01a6  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x01cc  */
    public boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
        int deltaY2;
        int incrementalDeltaY2;
        boolean z;
        boolean z2;
        int count;
        boolean selectorOnScreen;
        int childCount;
        int top;
        int i = deltaY;
        int i2 = incrementalDeltaY;
        int count2 = getChildCount();
        if (count2 == 0) {
            return true;
        }
        int firstTop = getChildAt(0).getTop();
        int lastBottom = getChildAt(count2 - 1).getBottom();
        Rect listPadding = this.mListPadding;
        int effectivePaddingTop = 0;
        int effectivePaddingBottom = 0;
        if ((this.mGroupFlags & 34) == 34) {
            effectivePaddingTop = listPadding.top;
            effectivePaddingBottom = listPadding.bottom;
        }
        int spaceAbove = effectivePaddingTop - firstTop;
        int end = getHeight() - effectivePaddingBottom;
        int spaceBelow = lastBottom - end;
        int height = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
        if (i < 0) {
            deltaY2 = Math.max(-(height - 1), i);
        } else {
            deltaY2 = Math.min(height - 1, i);
        }
        if (i2 < 0) {
            incrementalDeltaY2 = Math.max(-(height - 1), i2);
        } else {
            incrementalDeltaY2 = Math.min(height - 1, i2);
        }
        int firstPosition = this.mFirstPosition;
        if (firstPosition == 0) {
            this.mFirstPositionDistanceGuess = firstTop - listPadding.top;
        } else {
            this.mFirstPositionDistanceGuess += incrementalDeltaY2;
        }
        if (firstPosition + count2 == this.mItemCount) {
            this.mLastPositionDistanceGuess = listPadding.bottom + lastBottom;
        } else {
            this.mLastPositionDistanceGuess += incrementalDeltaY2;
        }
        boolean cannotScrollDown = firstPosition == 0 && firstTop >= listPadding.top && incrementalDeltaY2 >= 0;
        int i3 = firstTop;
        boolean cannotScrollUp = firstPosition + count2 == this.mItemCount && lastBottom <= getHeight() - listPadding.bottom && incrementalDeltaY2 <= 0;
        if (cannotScrollDown) {
            int i4 = count2;
            boolean z3 = cannotScrollDown;
            boolean z4 = cannotScrollUp;
            int i5 = lastBottom;
            int i6 = effectivePaddingTop;
            int i7 = effectivePaddingBottom;
            int i8 = end;
            z2 = false;
            z = true;
        } else if (cannotScrollUp) {
            int i9 = deltaY2;
            int i10 = count2;
            boolean z5 = cannotScrollDown;
            boolean z6 = cannotScrollUp;
            int i11 = lastBottom;
            int i12 = effectivePaddingTop;
            int i13 = effectivePaddingBottom;
            int i14 = end;
            z2 = false;
            z = true;
        } else {
            boolean down = incrementalDeltaY2 < 0;
            boolean inTouchMode = isInTouchMode();
            if (inTouchMode) {
                hideSelector();
            }
            boolean z7 = cannotScrollDown;
            int headerViewsCount = getHeaderViewsCount();
            boolean z8 = cannotScrollUp;
            int footerViewsStart = this.mItemCount - getFooterViewsCount();
            int start = 0;
            int count3 = 0;
            if (down) {
                int i15 = lastBottom;
                int top2 = -incrementalDeltaY2;
                int i16 = effectivePaddingTop;
                int i17 = effectivePaddingBottom;
                if ((this.mGroupFlags & 34) == 34) {
                    top2 += listPadding.top;
                }
                int i18 = 0;
                while (true) {
                    if (i18 >= count2) {
                        break;
                    }
                    View child = getChildAt(i18);
                    int end2 = end;
                    if (child.getBottom() >= top2) {
                        break;
                    }
                    count3++;
                    int position = firstPosition + i18;
                    if (position < headerViewsCount || position >= footerViewsStart) {
                        top = top2;
                    } else {
                        child.clearAccessibilityFocus();
                        top = top2;
                        this.mRecycler.addScrapView(child, position);
                    }
                    i18++;
                    end = end2;
                    top2 = top;
                }
            } else {
                int i19 = effectivePaddingTop;
                int i20 = effectivePaddingBottom;
                int i21 = end;
                int bottom = getHeight() - incrementalDeltaY2;
                if ((this.mGroupFlags & 34) == 34) {
                    bottom -= listPadding.bottom;
                }
                int i22 = count2 - 1;
                while (true) {
                    if (i22 < 0) {
                        int childCount2 = count2;
                        break;
                    }
                    View child2 = getChildAt(i22);
                    if (child2.getTop() <= bottom) {
                        break;
                    }
                    start = i22;
                    count3++;
                    int position2 = firstPosition + i22;
                    if (position2 < headerViewsCount || position2 >= footerViewsStart) {
                        childCount = count2;
                    } else {
                        child2.clearAccessibilityFocus();
                        childCount = count2;
                        this.mRecycler.addScrapView(child2, position2);
                    }
                    i22--;
                    count2 = childCount;
                }
                int start2 = start;
                count = count3;
                this.mMotionViewNewTop = this.mMotionViewOriginalTop + deltaY2;
                this.mBlockLayoutRequests = true;
                if (count > 0) {
                    detachViewsFromParent(start2, count);
                    this.mRecycler.removeSkippedScrap();
                }
                if (!awakenScrollBars()) {
                    invalidate();
                }
                offsetChildrenTopAndBottom(incrementalDeltaY2);
                if (down) {
                    this.mFirstPosition += count;
                }
                int absIncrementalDeltaY = Math.abs(incrementalDeltaY2);
                if (spaceAbove < absIncrementalDeltaY || spaceBelow < absIncrementalDeltaY) {
                    fillGap(down);
                }
                this.mRecycler.fullyDetachScrapViews();
                selectorOnScreen = false;
                if (inTouchMode) {
                    int i23 = deltaY2;
                    if (this.mSelectedPosition != -1) {
                        int childIndex = this.mSelectedPosition - this.mFirstPosition;
                        if (childIndex < 0 || childIndex >= getChildCount()) {
                        } else {
                            int i24 = count;
                            positionSelector(this.mSelectedPosition, getChildAt(childIndex));
                            selectorOnScreen = true;
                        }
                        if (!selectorOnScreen) {
                            this.mSelectorRect.setEmpty();
                        }
                        this.mBlockLayoutRequests = false;
                        invokeOnItemScrollListener();
                        return false;
                    }
                } else {
                    int i25 = count;
                }
                if (this.mSelectorPosition != -1) {
                    int childIndex2 = this.mSelectorPosition - this.mFirstPosition;
                    if (childIndex2 >= 0 && childIndex2 < getChildCount()) {
                        positionSelector(this.mSelectorPosition, getChildAt(childIndex2));
                        selectorOnScreen = true;
                    }
                }
                if (!selectorOnScreen) {
                }
                this.mBlockLayoutRequests = false;
                invokeOnItemScrollListener();
                return false;
            }
            int childCount3 = count2;
            int start22 = start;
            count = count3;
            this.mMotionViewNewTop = this.mMotionViewOriginalTop + deltaY2;
            this.mBlockLayoutRequests = true;
            if (count > 0) {
            }
            if (!awakenScrollBars()) {
            }
            offsetChildrenTopAndBottom(incrementalDeltaY2);
            if (down) {
            }
            int absIncrementalDeltaY2 = Math.abs(incrementalDeltaY2);
            fillGap(down);
            this.mRecycler.fullyDetachScrapViews();
            selectorOnScreen = false;
            if (inTouchMode) {
            }
            if (this.mSelectorPosition != -1) {
            }
            if (!selectorOnScreen) {
            }
            this.mBlockLayoutRequests = false;
            invokeOnItemScrollListener();
            return false;
        }
        if (incrementalDeltaY2 != 0) {
            z2 = z;
        }
        return z2;
    }

    /* access modifiers changed from: package-private */
    public int getHeaderViewsCount() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int getFooterViewsCount() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void hideSelector() {
        if (this.mSelectedPosition != -1) {
            if (this.mLayoutMode != 4) {
                this.mResurrectToPosition = this.mSelectedPosition;
            }
            if (this.mNextSelectedPosition >= 0 && this.mNextSelectedPosition != this.mSelectedPosition) {
                this.mResurrectToPosition = this.mNextSelectedPosition;
            }
            setSelectedPositionInt(-1);
            setNextSelectedPositionInt(-1);
            this.mSelectedTop = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public int reconcileSelectedPosition() {
        int position = this.mSelectedPosition;
        if (position < 0) {
            position = this.mResurrectToPosition;
        }
        return Math.min(Math.max(0, position), this.mItemCount - 1);
    }

    /* access modifiers changed from: package-private */
    public int findClosestMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return -1;
        }
        int motionRow = findMotionRow(y);
        return motionRow != -1 ? motionRow : (this.mFirstPosition + childCount) - 1;
    }

    public void invalidateViews() {
        this.mDataChanged = true;
        rememberSyncState();
        requestLayout();
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public boolean resurrectSelectionIfNeeded() {
        if (this.mSelectedPosition >= 0 || !resurrectSelection()) {
            return false;
        }
        updateSelectorState();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean resurrectSelection() {
        int selectedPos;
        int childCount = getChildCount();
        if (childCount <= 0) {
            return false;
        }
        int selectedTop = 0;
        int childrenTop = this.mListPadding.top;
        int childrenBottom = (this.mBottom - this.mTop) - this.mListPadding.bottom;
        int firstPosition = this.mFirstPosition;
        int toPosition = this.mResurrectToPosition;
        boolean down = true;
        if (toPosition < firstPosition || toPosition >= firstPosition + childCount) {
            if (toPosition >= firstPosition) {
                int itemCount = this.mItemCount;
                down = false;
                int selectedPos2 = (firstPosition + childCount) - 1;
                int i = childCount - 1;
                while (true) {
                    if (i < 0) {
                        selectedPos = selectedPos2;
                        break;
                    }
                    View v = getChildAt(i);
                    int top = v.getTop();
                    int bottom = v.getBottom();
                    if (i == childCount - 1) {
                        selectedTop = top;
                        if (firstPosition + childCount < itemCount || bottom > childrenBottom) {
                            childrenBottom -= getVerticalFadingEdgeLength();
                        }
                    }
                    if (bottom <= childrenBottom) {
                        selectedTop = top;
                        selectedPos = firstPosition + i;
                        break;
                    }
                    i--;
                }
            } else {
                selectedPos = firstPosition;
                int childrenTop2 = childrenTop;
                int selectedTop2 = 0;
                int i2 = 0;
                while (true) {
                    if (i2 >= childCount) {
                        break;
                    }
                    int top2 = getChildAt(i2).getTop();
                    if (i2 == 0) {
                        selectedTop2 = top2;
                        if (firstPosition > 0 || top2 < childrenTop2) {
                            childrenTop2 += getVerticalFadingEdgeLength();
                        }
                    }
                    if (top2 >= childrenTop2) {
                        selectedPos = firstPosition + i2;
                        selectedTop2 = top2;
                        break;
                    }
                    i2++;
                }
                selectedTop = selectedTop2;
            }
        } else {
            selectedPos = toPosition;
            View selected = getChildAt(selectedPos - this.mFirstPosition);
            selectedTop = selected.getTop();
            int selectedBottom = selected.getBottom();
            if (selectedTop < childrenTop) {
                selectedTop = childrenTop + getVerticalFadingEdgeLength();
            } else if (selectedBottom > childrenBottom) {
                selectedTop = (childrenBottom - selected.getMeasuredHeight()) - getVerticalFadingEdgeLength();
            }
        }
        this.mResurrectToPosition = -1;
        removeCallbacks(this.mFlingRunnable);
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        this.mTouchMode = -1;
        clearScrollingCache();
        this.mSpecificTop = selectedTop;
        int selectedPos3 = lookForSelectablePosition(selectedPos, down);
        if (selectedPos3 < firstPosition || selectedPos3 > getLastVisiblePosition()) {
            selectedPos3 = -1;
        } else {
            this.mLayoutMode = 4;
            updateSelectorState();
            setSelectionInt(selectedPos3);
            invokeOnItemScrollListener();
        }
        reportScrollStateChange(0);
        return selectedPos3 >= 0;
    }

    /* access modifiers changed from: package-private */
    public void confirmCheckedPositionsById() {
        this.mCheckStates.clear();
        int i = 0;
        boolean checkedCountChanged = false;
        int checkedIndex = 0;
        while (checkedIndex < this.mCheckedIdStates.size()) {
            long id = this.mCheckedIdStates.keyAt(checkedIndex);
            int lastPos = this.mCheckedIdStates.valueAt(checkedIndex).intValue();
            if (id != this.mAdapter.getItemId(lastPos)) {
                int start = Math.max(i, lastPos - 20);
                int end = Math.min(lastPos + 20, this.mItemCount);
                boolean found = false;
                int searchPos = start;
                while (true) {
                    if (searchPos >= end) {
                        break;
                    } else if (id == this.mAdapter.getItemId(searchPos)) {
                        found = true;
                        this.mCheckStates.put(searchPos, true);
                        this.mCheckedIdStates.setValueAt(checkedIndex, Integer.valueOf(searchPos));
                        break;
                    } else {
                        searchPos++;
                    }
                }
                if (!found) {
                    this.mCheckedIdStates.delete(id);
                    checkedIndex--;
                    this.mCheckedItemCount--;
                    checkedCountChanged = true;
                    if (!(this.mChoiceActionMode == null || this.mMultiChoiceModeCallback == null)) {
                        int i2 = end;
                        this.mMultiChoiceModeCallback.onItemCheckedStateChanged(this.mChoiceActionMode, lastPos, id, false);
                    }
                }
            } else {
                this.mCheckStates.put(lastPos, true);
            }
            checkedIndex++;
            i = 0;
        }
        if (checkedCountChanged && this.mChoiceActionMode != null) {
            this.mChoiceActionMode.invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void handleDataChanged() {
        int count = this.mItemCount;
        int lastHandledItemCount = this.mLastHandledItemCount;
        this.mLastHandledItemCount = this.mItemCount;
        if (!(this.mChoiceMode == 0 || this.mAdapter == null || !this.mAdapter.hasStableIds())) {
            confirmCheckedPositionsById();
        }
        this.mRecycler.clearTransientStateViews();
        int i = 3;
        if (count > 0) {
            if (this.mNeedSync) {
                this.mNeedSync = false;
                this.mPendingSync = null;
                if (this.mTranscriptMode == 2) {
                    this.mLayoutMode = 3;
                    return;
                }
                if (this.mTranscriptMode == 1) {
                    if (this.mForceTranscriptScroll) {
                        this.mForceTranscriptScroll = false;
                        this.mLayoutMode = 3;
                        return;
                    }
                    int childCount = getChildCount();
                    int listBottom = getHeight() - getPaddingBottom();
                    View lastChild = getChildAt(childCount - 1);
                    int lastBottom = lastChild != null ? lastChild.getBottom() : listBottom;
                    if (this.mFirstPosition + childCount < lastHandledItemCount || lastBottom > listBottom) {
                        awakenScrollBars();
                    } else {
                        this.mLayoutMode = 3;
                        return;
                    }
                }
                switch (this.mSyncMode) {
                    case 0:
                        if (isInTouchMode()) {
                            this.mLayoutMode = 5;
                            this.mSyncPosition = Math.min(Math.max(0, this.mSyncPosition), count - 1);
                            return;
                        }
                        int newPos = findSyncPosition();
                        if (newPos >= 0 && lookForSelectablePosition(newPos, true) == newPos) {
                            this.mSyncPosition = newPos;
                            if (this.mSyncHeight == ((long) getHeight())) {
                                this.mLayoutMode = 5;
                            } else {
                                this.mLayoutMode = 2;
                            }
                            setNextSelectedPositionInt(newPos);
                            return;
                        }
                    case 1:
                        this.mLayoutMode = 5;
                        this.mSyncPosition = Math.min(Math.max(0, this.mSyncPosition), count - 1);
                        return;
                }
            }
            if (isInTouchMode() == 0) {
                int newPos2 = getSelectedItemPosition();
                if (newPos2 >= count) {
                    newPos2 = count - 1;
                }
                if (newPos2 < 0) {
                    newPos2 = 0;
                }
                int selectablePos = lookForSelectablePosition(newPos2, true);
                if (selectablePos >= 0) {
                    setNextSelectedPositionInt(selectablePos);
                    return;
                }
                int selectablePos2 = lookForSelectablePosition(newPos2, false);
                if (selectablePos2 >= 0) {
                    setNextSelectedPositionInt(selectablePos2);
                    return;
                }
            } else if (this.mResurrectToPosition >= 0) {
                return;
            }
        }
        if (!this.mStackFromBottom) {
            i = 1;
        }
        this.mLayoutMode = i;
        this.mSelectedPosition = -1;
        this.mSelectedRowId = Long.MIN_VALUE;
        this.mNextSelectedPosition = -1;
        this.mNextSelectedRowId = Long.MIN_VALUE;
        this.mNeedSync = false;
        this.mPendingSync = null;
        this.mSelectorPosition = -1;
        checkSelectionChanged();
    }

    /* access modifiers changed from: protected */
    public void onDisplayHint(int hint) {
        super.onDisplayHint(hint);
        if (hint != 0) {
            if (hint == 4 && this.mPopup != null && this.mPopup.isShowing()) {
                dismissPopup();
            }
        } else if (this.mFiltered && this.mPopup != null && !this.mPopup.isShowing()) {
            showPopup();
        }
        this.mPopupHidden = hint == 4;
    }

    private void dismissPopup() {
        if (this.mPopup != null) {
            this.mPopup.dismiss();
        }
    }

    private void showPopup() {
        if (getWindowVisibility() == 0) {
            createTextFilter(true);
            positionPopup();
            checkFocus();
        }
    }

    private void positionPopup() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int[] xy = new int[2];
        getLocationOnScreen(xy);
        int bottomGap = ((screenHeight - xy[1]) - getHeight()) + ((int) (this.mDensityScale * 20.0f));
        if (!this.mPopup.isShowing()) {
            this.mPopup.showAtLocation((View) this, 81, xy[0], bottomGap);
        } else {
            this.mPopup.update(xy[0], bottomGap, -1, -1);
        }
    }

    static int getDistance(Rect source, Rect dest, int direction) {
        int dY;
        int dX;
        int sY;
        int sX;
        if (direction == 17) {
            sX = source.left;
            sY = source.top + (source.height() / 2);
            dX = dest.right;
            dY = dest.top + (dest.height() / 2);
        } else if (direction == 33) {
            sX = source.left + (source.width() / 2);
            sY = source.top;
            dX = dest.left + (dest.width() / 2);
            dY = dest.bottom;
        } else if (direction == 66) {
            sX = source.right;
            sY = source.top + (source.height() / 2);
            dX = dest.left;
            dY = dest.top + (dest.height() / 2);
        } else if (direction != 130) {
            switch (direction) {
                case 1:
                case 2:
                    sX = source.right + (source.width() / 2);
                    sY = source.top + (source.height() / 2);
                    dX = dest.left + (dest.width() / 2);
                    dY = dest.top + (dest.height() / 2);
                    break;
                default:
                    throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
            }
        } else {
            sX = source.left + (source.width() / 2);
            sY = source.bottom;
            dX = dest.left + (dest.width() / 2);
            dY = dest.top;
        }
        int deltaX = dX - sX;
        int deltaY = dY - sY;
        return (deltaY * deltaY) + (deltaX * deltaX);
    }

    /* access modifiers changed from: protected */
    public boolean isInFilterMode() {
        return this.mFiltered;
    }

    /* access modifiers changed from: package-private */
    public boolean sendToTextFilter(int keyCode, int count, KeyEvent event) {
        if (!acceptFilter()) {
            return false;
        }
        boolean handled = false;
        boolean okToSend = true;
        if (keyCode == 4) {
            if (this.mFiltered && this.mPopup != null && this.mPopup.isShowing()) {
                if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.startTracking(event, this);
                    }
                    handled = true;
                } else if (event.getAction() == 1 && event.isTracking() && !event.isCanceled()) {
                    handled = true;
                    this.mTextFilter.setText((CharSequence) "");
                }
            }
            okToSend = false;
        } else if (keyCode != 62) {
            if (keyCode != 66) {
                switch (keyCode) {
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                        break;
                }
            }
            okToSend = false;
        } else {
            okToSend = this.mFiltered;
        }
        if (okToSend) {
            createTextFilter(true);
            KeyEvent forwardEvent = event;
            if (forwardEvent.getRepeatCount() > 0) {
                forwardEvent = KeyEvent.changeTimeRepeat(event, event.getEventTime(), 0);
            }
            switch (event.getAction()) {
                case 0:
                    handled = this.mTextFilter.onKeyDown(keyCode, forwardEvent);
                    break;
                case 1:
                    handled = this.mTextFilter.onKeyUp(keyCode, forwardEvent);
                    break;
                case 2:
                    handled = this.mTextFilter.onKeyMultiple(keyCode, count, event);
                    break;
            }
        }
        return handled;
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (!isTextFilterEnabled()) {
            return null;
        }
        if (this.mPublicInputConnection == null) {
            this.mDefInputConnection = new BaseInputConnection((View) this, false);
            this.mPublicInputConnection = new InputConnectionWrapper(outAttrs);
        }
        outAttrs.inputType = 177;
        outAttrs.imeOptions = 6;
        return this.mPublicInputConnection;
    }

    public boolean checkInputConnectionProxy(View view) {
        return view == this.mTextFilter;
    }

    private void createTextFilter(boolean animateEntrance) {
        if (this.mPopup == null) {
            PopupWindow p = new PopupWindow(getContext());
            p.setFocusable(false);
            p.setTouchable(false);
            p.setInputMethodMode(2);
            p.setContentView(getTextFilterInput());
            p.setWidth(-2);
            p.setHeight(-2);
            p.setBackgroundDrawable(null);
            this.mPopup = p;
            getViewTreeObserver().addOnGlobalLayoutListener(this);
            this.mGlobalLayoutListenerAddedFilter = true;
        }
        if (animateEntrance) {
            this.mPopup.setAnimationStyle(16974595);
        } else {
            this.mPopup.setAnimationStyle(16974596);
        }
    }

    /* access modifiers changed from: private */
    public EditText getTextFilterInput() {
        if (this.mTextFilter == null) {
            this.mTextFilter = (EditText) LayoutInflater.from(getContext()).inflate(17367324, (ViewGroup) null);
            this.mTextFilter.setRawInputType(177);
            this.mTextFilter.setImeOptions(268435456);
            this.mTextFilter.addTextChangedListener(this);
        }
        return this.mTextFilter;
    }

    public void clearTextFilter() {
        if (this.mFiltered) {
            getTextFilterInput().setText((CharSequence) "");
            this.mFiltered = false;
            if (this.mPopup != null && this.mPopup.isShowing()) {
                dismissPopup();
            }
        }
    }

    public boolean hasTextFilter() {
        return this.mFiltered;
    }

    public void onGlobalLayout() {
        if (isShown()) {
            if (this.mFiltered && this.mPopup != null && !this.mPopup.isShowing() && !this.mPopupHidden) {
                showPopup();
            }
        } else if (this.mPopup != null && this.mPopup.isShowing()) {
            dismissPopup();
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isTextFilterEnabled()) {
            createTextFilter(true);
            int length = s.length();
            boolean showing = this.mPopup.isShowing();
            if (!showing && length > 0) {
                showPopup();
                this.mFiltered = true;
            } else if (showing && length == 0) {
                dismissPopup();
                this.mFiltered = false;
            }
            if (this.mAdapter instanceof Filterable) {
                Filter f = ((Filterable) this.mAdapter).getFilter();
                if (f != null) {
                    f.filter(s, this);
                    return;
                }
                throw new IllegalStateException("You cannot call onTextChanged with a non filterable adapter");
            }
        }
    }

    public void afterTextChanged(Editable s) {
    }

    public void onFilterComplete(int count) {
        if (this.mSelectedPosition < 0 && count > 0) {
            this.mResurrectToPosition = -1;
            resurrectSelection();
        }
    }

    /* access modifiers changed from: protected */
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -2, 0);
    }

    /* access modifiers changed from: protected */
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public void setTranscriptMode(int mode) {
        this.mTranscriptMode = mode;
    }

    public int getTranscriptMode() {
        return this.mTranscriptMode;
    }

    public int getSolidColor() {
        return this.mCacheColorHint;
    }

    public void setCacheColorHint(int color) {
        if (color != this.mCacheColorHint) {
            this.mCacheColorHint = color;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).setDrawingCacheBackgroundColor(color);
            }
            this.mRecycler.setCacheColorHint(color);
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public int getCacheColorHint() {
        return this.mCacheColorHint;
    }

    public void reclaimViews(List<View> views) {
        int childCount = getChildCount();
        RecyclerListener listener = this.mRecycler.mRecyclerListener;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp != null && this.mRecycler.shouldRecycleViewType(lp.viewType)) {
                views.add(child);
                child.setAccessibilityDelegate(null);
                if (listener != null) {
                    listener.onMovedToScrapHeap(child);
                }
            }
        }
        this.mRecycler.reclaimScrapViews(views);
        removeAllViewsInLayout();
    }

    private void finishGlows() {
        if (this.mEdgeGlowTop != null) {
            this.mEdgeGlowTop.finish();
            this.mEdgeGlowBottom.finish();
        }
    }

    public void setRemoteViewsAdapter(Intent intent) {
        setRemoteViewsAdapter(intent, false);
    }

    public Runnable setRemoteViewsAdapterAsync(Intent intent) {
        return new RemoteViewsAdapter.AsyncRemoteAdapterAction(this, intent);
    }

    public void setRemoteViewsAdapter(Intent intent, boolean isAsync) {
        if (this.mRemoteAdapter == null || !new Intent.FilterComparison(intent).equals(new Intent.FilterComparison(this.mRemoteAdapter.getRemoteViewsServiceIntent()))) {
            this.mDeferNotifyDataSetChanged = false;
            this.mRemoteAdapter = new RemoteViewsAdapter(getContext(), intent, this, isAsync);
            if (this.mRemoteAdapter.isDataReady()) {
                setAdapter((ListAdapter) this.mRemoteAdapter);
            }
        }
    }

    public void setRemoteViewsOnClickHandler(RemoteViews.OnClickHandler handler) {
        if (this.mRemoteAdapter != null) {
            this.mRemoteAdapter.setRemoteViewsOnClickHandler(handler);
        }
    }

    public void deferNotifyDataSetChanged() {
        this.mDeferNotifyDataSetChanged = true;
    }

    public boolean onRemoteAdapterConnected() {
        if (this.mRemoteAdapter != this.mAdapter) {
            setAdapter((ListAdapter) this.mRemoteAdapter);
            if (this.mDeferNotifyDataSetChanged) {
                this.mRemoteAdapter.notifyDataSetChanged();
                this.mDeferNotifyDataSetChanged = false;
            }
            return false;
        } else if (this.mRemoteAdapter == null) {
            return false;
        } else {
            this.mRemoteAdapter.superNotifyDataSetChanged();
            return true;
        }
    }

    public void onRemoteAdapterDisconnected() {
    }

    /* access modifiers changed from: package-private */
    public void setVisibleRangeHint(int start, int end) {
        if (this.mRemoteAdapter != null) {
            this.mRemoteAdapter.setVisibleRangeHint(start, end);
        }
    }

    public void setRecyclerListener(RecyclerListener listener) {
        RecyclerListener unused = this.mRecycler.mRecyclerListener = listener;
    }

    /* access modifiers changed from: package-private */
    public int getHeightForPosition(int position) {
        int firstVisiblePosition = getFirstVisiblePosition();
        int childCount = getChildCount();
        int index = position - firstVisiblePosition;
        if (index >= 0 && index < childCount) {
            return getChildAt(index).getHeight();
        }
        View view = obtainView(position, this.mIsScrap);
        view.measure(this.mWidthMeasureSpec, 0);
        int height = view.getMeasuredHeight();
        this.mRecycler.addScrapView(view, position);
        return height;
    }

    public void setSelectionFromTop(int position, int y) {
        if (this.mAdapter != null) {
            if (!isInTouchMode()) {
                position = lookForSelectablePosition(position, true);
                if (position >= 0) {
                    setNextSelectedPositionInt(position);
                }
            } else {
                this.mResurrectToPosition = position;
            }
            if (position >= 0) {
                this.mLayoutMode = 4;
                this.mSpecificTop = this.mListPadding.top + y;
                if (this.mNeedSync) {
                    this.mSyncPosition = position;
                    this.mSyncRowId = this.mAdapter.getItemId(position);
                }
                if (this.mPositionScroller != null) {
                    this.mPositionScroller.stop();
                }
                requestLayout();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("drawing:cacheColorHint", getCacheColorHint());
        encoder.addProperty("list:fastScrollEnabled", isFastScrollEnabled());
        encoder.addProperty("list:scrollingCacheEnabled", isScrollingCacheEnabled());
        encoder.addProperty("list:smoothScrollbarEnabled", isSmoothScrollbarEnabled());
        encoder.addProperty("list:stackFromBottom", isStackFromBottom());
        encoder.addProperty("list:textFilterEnabled", isTextFilterEnabled());
        View selectedView = getSelectedView();
        if (selectedView != null) {
            encoder.addPropertyKey("selectedView");
            selectedView.encode(encoder);
        }
    }

    /* access modifiers changed from: protected */
    public void setEdgeGlowTopBottom(EdgeEffect edgeGlowTop, EdgeEffect edgeGlowBottom) {
        this.mEdgeGlowTop = edgeGlowTop;
        this.mEdgeGlowBottom = edgeGlowBottom;
    }

    /* access modifiers changed from: protected */
    public Object getScrollerInner() {
        return this.mFastScroll;
    }

    /* access modifiers changed from: protected */
    public void setScrollerInner(FastScrollerEx scroller) {
        this.mFastScroll = scroller;
    }

    public void setTag(Object tag) {
        super.setTag(tag);
        if (tag != null && DISABLE_HW_MULTI_SELECT_MODE.equals(tag.toString())) {
            setIgnoreScrollMultiSelectStub();
        } else if (tag != null && ENABLE_HW_MULTI_SELECT_MODE.equals(tag.toString())) {
            enableScrollMultiSelectStub();
        }
    }

    public ActionMode getChoiceActionMode() {
        return this.mChoiceActionMode;
    }

    public int getTouchMode() {
        return this.mTouchMode;
    }

    public void setTouchMode(int mode) {
        this.mTouchMode = mode;
    }

    public int getMotionPosition() {
        return this.mMotionPosition;
    }

    public ListAdapter getAdapter() {
        return this.mAdapter;
    }

    public SparseBooleanArray getCheckStates() {
        return this.mCheckStates;
    }

    /* access modifiers changed from: protected */
    public void setIgnoreScrollMultiSelectStub() {
    }

    /* access modifiers changed from: protected */
    public void enableScrollMultiSelectStub() {
    }

    /* access modifiers changed from: protected */
    public boolean getCheckedStateForMultiSelect(boolean curState) {
        return curState;
    }

    /* access modifiers changed from: protected */
    public void onMultiSelectMove(MotionEvent ev, int pointerIndex) {
    }

    /* access modifiers changed from: protected */
    public void enterMultiSelectModeIfNeeded(int motionPosition, int x) {
    }

    /* access modifiers changed from: protected */
    public void dismissCurrentPressed() {
    }

    /* access modifiers changed from: protected */
    public void setStableItemHeight(OverScroller scroller, FlingRunnable fr) {
    }

    /* access modifiers changed from: protected */
    public int adjustFlingDistance(int delta) {
        if (delta > 0) {
            return Math.min(((getHeight() - this.mPaddingBottom) - this.mPaddingTop) - 1, delta);
        }
        return Math.max(-(((getHeight() - this.mPaddingBottom) - this.mPaddingTop) - 1), delta);
    }

    /* access modifiers changed from: protected */
    public boolean hasScrollMultiSelectMask() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean hasSpringAnimatorMask() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean hasHighSpeedStableMask() {
        return true;
    }

    /* access modifiers changed from: protected */
    public int getPressedStateDuration() {
        return ViewConfiguration.getPressedStateDuration();
    }

    /* access modifiers changed from: protected */
    public void adjustSelector(int pos, Rect rect) {
    }
}
