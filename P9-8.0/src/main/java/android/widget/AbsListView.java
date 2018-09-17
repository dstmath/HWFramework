package android.widget;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.FilterComparison;
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
import android.os.Parcelable.Creator;
import android.os.StrictMode;
import android.os.StrictMode.Span;
import android.os.Trace;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Jlog;
import android.util.Log;
import android.util.LogException;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.StateSet;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.BaseSavedState;
import android.view.ViewConfiguration;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.IntToString;
import android.view.ViewHierarchyEncoder;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Filter.FilterListener;
import android.widget.RemoteViews.OnClickHandler;
import android.widget.RemoteViewsAdapter.AsyncRemoteAdapterAction;
import android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsListView extends AdapterView<ListAdapter> implements TextWatcher, OnGlobalLayoutListener, FilterListener, OnTouchModeChangeListener, RemoteAdapterConnectionCallback {
    private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;
    public static final int CHOICE_MODE_MULTIPLE = 2;
    public static final int CHOICE_MODE_MULTIPLE_MODAL = 3;
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
    static final int OVERSCROLL_LIMIT_DIVISOR = 3;
    private static final boolean PROFILE_FLINGING = false;
    private static final boolean PROFILE_SCROLLING = false;
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
    private int mActivePointerId;
    protected ListAdapter mAdapter;
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
    private ContextMenuInfo mContextMenuInfo;
    AdapterDataSetObserver mDataSetObserver;
    private InputConnection mDefInputConnection;
    private boolean mDeferNotifyDataSetChanged;
    private float mDensityScale;
    private int mDirection;
    boolean mDrawSelectorOnTop;
    private EdgeEffect mEdgeGlowBottom;
    private EdgeEffect mEdgeGlowTop;
    private FastScroller mFastScroll;
    boolean mFastScrollAlwaysVisible;
    boolean mFastScrollEnabled;
    private int mFastScrollStyle;
    private boolean mFiltered;
    private int mFirstPositionDistanceGuess;
    private boolean mFlingProfilingStarted;
    private FlingRunnable mFlingRunnable;
    private Span mFlingStrictSpan;
    private boolean mForceTranscriptScroll;
    private boolean mGlobalLayoutListenerAddedFilter;
    private boolean mHasPerformedLongPress;
    private IHwWechatOptimize mIHwWechatOptimize;
    private boolean mIsChildViewEnabled;
    private boolean mIsDetaching;
    final boolean[] mIsScrap;
    private int mLastAccessibilityScrollEventFromIndex;
    private int mLastAccessibilityScrollEventToIndex;
    private int mLastHandledItemCount;
    private int mLastPositionDistanceGuess;
    private int mLastScrollState;
    private int mLastTouchMode;
    int mLastY;
    int mLayoutMode;
    protected boolean mListAnimationSwitch;
    Rect mListPadding;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    int mMotionCorrection;
    int mMotionPosition;
    int mMotionViewNewTop;
    int mMotionViewOriginalTop;
    int mMotionX;
    int mMotionY;
    MultiChoiceModeWrapper mMultiChoiceModeCallback;
    private int mNestedYOffset;
    private OnScrollListener mOnScrollListener;
    int mOverflingDistance;
    int mOverscrollDistance;
    int mOverscrollMax;
    private final Thread mOwnerThread;
    private CheckForKeyLongPress mPendingCheckForKeyLongPress;
    private CheckForLongPress mPendingCheckForLongPress;
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
    private Span mScrollStrictSpan;
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
    private final float[] mTmpPoint;
    private Rect mTouchFrame;
    int mTouchMode;
    private Runnable mTouchModeReset;
    private int mTouchSlop;
    private int mTranscriptMode;
    private float mVelocityScale;
    private VelocityTracker mVelocityTracker;
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

    class AdapterDataSetObserver extends AdapterDataSetObserver {
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

    private class WindowRunnnable {
        private int mOriginalAttachCount;

        /* synthetic */ WindowRunnnable(AbsListView this$0, WindowRunnnable -this1) {
            this();
        }

        private WindowRunnnable() {
        }

        public void rememberWindowAttachCount() {
            this.mOriginalAttachCount = AbsListView.this.-wrap4();
        }

        public boolean sameWindow() {
            return AbsListView.this.-wrap4() == this.mOriginalAttachCount;
        }
    }

    private class CheckForKeyLongPress extends WindowRunnnable implements Runnable {
        /* synthetic */ CheckForKeyLongPress(AbsListView this$0, CheckForKeyLongPress -this1) {
            this();
        }

        private CheckForKeyLongPress() {
            super(AbsListView.this, null);
        }

        public void run() {
            if (AbsListView.this.isPressed() && AbsListView.this.mSelectedPosition >= 0) {
                View v = AbsListView.this.getChildAt(AbsListView.this.mSelectedPosition - AbsListView.this.mFirstPosition);
                if (AbsListView.this.mDataChanged) {
                    AbsListView.this.setPressed(false);
                    if (v != null) {
                        v.setPressed(false);
                        return;
                    }
                    return;
                }
                boolean handled = false;
                if (sameWindow()) {
                    handled = AbsListView.this.performLongPress(v, AbsListView.this.mSelectedPosition, AbsListView.this.mSelectedRowId);
                }
                if (handled) {
                    AbsListView.this.setPressed(false);
                    v.setPressed(false);
                }
            }
        }
    }

    private class CheckForLongPress extends WindowRunnnable implements Runnable {
        private static final int INVALID_COORD = -1;
        private float mX;
        private float mY;

        /* synthetic */ CheckForLongPress(AbsListView this$0, CheckForLongPress -this1) {
            this();
        }

        private CheckForLongPress() {
            super(AbsListView.this, null);
            this.mX = -1.0f;
            this.mY = -1.0f;
        }

        private void setCoords(float x, float y) {
            this.mX = x;
            this.mY = y;
        }

        public void run() {
            View child = AbsListView.this.getChildAt(AbsListView.this.mMotionPosition - AbsListView.this.mFirstPosition);
            if (child != null) {
                int longPressPosition = AbsListView.this.mMotionPosition;
                long longPressId = AbsListView.this.mAdapter.getItemId(AbsListView.this.mMotionPosition);
                boolean handled = false;
                if (!(!sameWindow() || (AbsListView.this.mDataChanged ^ 1) == 0 || AbsListView.this.getRootView().isLongPressSwipe())) {
                    handled = (this.mX == -1.0f || this.mY == -1.0f) ? AbsListView.this.performLongPress(child, longPressPosition, longPressId) : AbsListView.this.performLongPress(child, longPressPosition, longPressId, this.mX, this.mY);
                }
                if (handled) {
                    AbsListView.this.mHasPerformedLongPress = true;
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

        /* synthetic */ CheckForTap(AbsListView this$0, CheckForTap -this1) {
            this();
        }

        private CheckForTap() {
        }

        public void run() {
            if (AbsListView.this.mTouchMode == 0) {
                AbsListView.this.mTouchMode = 1;
                View child = AbsListView.this.getChildAt(AbsListView.this.mMotionPosition - AbsListView.this.mFirstPosition);
                if (child != null && (child.hasExplicitFocusable() ^ 1) != 0) {
                    AbsListView.this.mLayoutMode = 0;
                    if (AbsListView.this.mDataChanged) {
                        AbsListView.this.mTouchMode = 2;
                        return;
                    }
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
                            AbsListView.this.mPendingCheckForLongPress = new CheckForLongPress(AbsListView.this, null);
                        }
                        AbsListView.this.mPendingCheckForLongPress.setCoords(this.x, this.y);
                        AbsListView.this.mPendingCheckForLongPress.rememberWindowAttachCount();
                        AbsListView.this.postDelayed(AbsListView.this.mPendingCheckForLongPress, (long) longPressTimeout);
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
        private float mLastSDRRatio = -1.0f;
        private final OverScroller mScroller;
        private boolean mSuppressIdleStateChangeCall;

        FlingRunnable() {
            this.mScroller = new OverScroller(AbsListView.this.getContext());
        }

        void start(int initialVelocity) {
            int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            this.mLastFlingY = initialY;
            this.mScroller.setInterpolator(null);
            AbsListView.this.setStableItemHeight(this.mScroller, AbsListView.this.mFlingRunnable);
            this.mScroller.fling(0, initialY, 0, initialVelocity, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            AbsListView.this.mTouchMode = 4;
            this.mSuppressIdleStateChangeCall = false;
            AbsListView.this.postOnAnimation(this);
            if (AbsListView.this.mFlingStrictSpan == null) {
                AbsListView.this.mFlingStrictSpan = StrictMode.enterCriticalSpan("AbsListView-fling");
            }
        }

        void startSpringback() {
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

        void startOverfling(int initialVelocity) {
            this.mScroller.setInterpolator(null);
            this.mScroller.fling(0, AbsListView.this.mScrollY, 0, initialVelocity, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, AbsListView.this.getHeight());
            AbsListView.this.mTouchMode = 6;
            this.mSuppressIdleStateChangeCall = false;
            AbsListView.this.invalidate();
            AbsListView.this.postOnAnimation(this);
        }

        void edgeReached(int delta) {
            ViewRootImpl viewRoot = AbsListView.this.getViewRootImpl();
            if (HwFrameworkFactory.getHwNsdImpl().checkIs2DSDRCase(AbsListView.this.getContext(), viewRoot)) {
                Log.i("2DSDR", "APS: 2DSDR: AbsListView.edgeReached, we will clear SDR ratio");
                viewRoot.setSDRRatio(-1.0f);
                viewRoot.getView().invalidate();
                this.mLastSDRRatio = -1.0f;
            }
            if (AbsListView.this.hasSpringAnimatorMask()) {
                this.mScroller.notifyVerticalEdgeReached(AbsListView.this.mScrollY, 0, (int) (((float) AbsListView.this.getHeight()) * 0.5f));
            } else {
                this.mScroller.notifyVerticalEdgeReached(AbsListView.this.mScrollY, 0, AbsListView.this.mOverflingDistance);
            }
            int overscrollMode = AbsListView.this.getOverScrollMode();
            if (overscrollMode == 0 || (overscrollMode == 1 && (AbsListView.this.contentFits() ^ 1) != 0)) {
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

        void startScroll(int distance, int duration, boolean linear, boolean suppressEndFlingStateChangeCall) {
            int initialY = distance < 0 ? Integer.MAX_VALUE : 0;
            this.mLastFlingY = initialY;
            this.mScroller.setInterpolator(linear ? AbsListView.sLinearInterpolator : null);
            this.mScroller.startScroll(0, initialY, 0, distance, duration);
            AbsListView.this.mTouchMode = 4;
            this.mSuppressIdleStateChangeCall = suppressEndFlingStateChangeCall;
            AbsListView.this.postOnAnimation(this);
        }

        void endFling() {
            ViewRootImpl viewRoot = AbsListView.this.getViewRootImpl();
            if (HwFrameworkFactory.getHwNsdImpl().checkIs2DSDRCase(AbsListView.this.getContext(), viewRoot)) {
                Log.i("2DSDR", "APS: 2DSDR: AbsListView.FlingRunnable.endFling, we will clear SDR ratio");
                viewRoot.setSDRRatio(-1.0f);
                this.mLastSDRRatio = -1.0f;
                viewRoot.getView().invalidate();
            }
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
                AbsListView.this.mFlingStrictSpan = null;
            }
        }

        void flywheelTouch() {
            AbsListView.this.postDelayed(this.mCheckFlywheel, 40);
        }

        int adjustDeltaIn2DSDR(int ratioBase, int origDelta) {
            int visibleHeight = AbsListView.this.getChildAt(0).getHeight() + AbsListView.this.getChildAt(0).getTop();
            if ((visibleHeight + origDelta) % ratioBase != 0) {
                int i = 0;
                while (i < ratioBase) {
                    int newDelta = origDelta > 0 ? origDelta + i : origDelta - i;
                    if ((visibleHeight + newDelta) % ratioBase == 0) {
                        return newDelta;
                    }
                    i++;
                }
            }
            return origDelta;
        }

        int dealWith2DSDR(int delta, OverScroller scroller) {
            ViewRootImpl viewRoot = AbsListView.this.getViewRootImpl();
            if (!(!HwFrameworkFactory.getHwNsdImpl().checkIs2DSDRCase(AbsListView.this.getContext(), viewRoot) || viewRoot.getView() == null || AbsListView.this.getChildAt(0) == null)) {
                int ratioBase = HwFrameworkFactory.getHwNsdImpl().computeSDRRatioBase(AbsListView.this.getContext(), viewRoot.getView(), AbsListView.this);
                float ratioToSet = HwFrameworkFactory.getHwNsdImpl().computeSDRRatio(AbsListView.this.getContext(), viewRoot.getView(), AbsListView.this, (float) AbsListView.this.mMaximumVelocity, scroller.getCurrVelocity(), ratioBase);
                if (0.0f < ratioToSet && ratioToSet <= 1.0f) {
                    boolean isNearEdge = false;
                    if ((delta < 0 && AbsListView.this.mItemCount - (AbsListView.this.mFirstPosition + AbsListView.this.getChildCount()) < 2) || (delta >= 0 && AbsListView.this.mFirstPosition < 2)) {
                        if (this.mLastSDRRatio != -1.0f) {
                            viewRoot.setSDRRatio(-1.0f);
                            this.mLastSDRRatio = -1.0f;
                        }
                        isNearEdge = true;
                    }
                    if (!isNearEdge) {
                        if (((double) Math.abs(ratioToSet - this.mLastSDRRatio)) > 1.0E-7d) {
                            Log.i("2DSDR", "APS: 2DSDR: AbsListView.FlingRunnable.run(), SDR ratio = " + ratioToSet + ", layer height = " + viewRoot.getView().getHeight() + ", scroll view height = " + AbsListView.this.getHeight());
                            viewRoot.setSDRRatio(ratioToSet);
                            this.mLastSDRRatio = ratioToSet;
                        }
                        delta = adjustDeltaIn2DSDR(ratioBase, delta);
                    }
                    viewRoot.getView().invalidate();
                }
            }
            return delta;
        }

        private boolean isNeedOverScroll() {
            int overscrollMode = AbsListView.this.getOverScrollMode();
            if (overscrollMode == 0) {
                return true;
            }
            if (overscrollMode == 1) {
                return AbsListView.this.contentFits() ^ 1;
            }
            return false;
        }

        /* JADX WARNING: Missing block: B:9:0x001f, code:
            if (r31.mScroller.isFinished() == false) goto L_0x0022;
     */
        /* JADX WARNING: Missing block: B:10:0x0021, code:
            return;
     */
        /* JADX WARNING: Missing block: B:12:0x0028, code:
            if (r31.this$0.mDataChanged == false) goto L_0x0031;
     */
        /* JADX WARNING: Missing block: B:13:0x002a, code:
            r31.this$0.layoutChildren();
     */
        /* JADX WARNING: Missing block: B:15:0x0037, code:
            if (r31.this$0.mItemCount == 0) goto L_0x0043;
     */
        /* JADX WARNING: Missing block: B:17:0x0041, code:
            if (r31.this$0.getChildCount() != 0) goto L_0x0047;
     */
        /* JADX WARNING: Missing block: B:18:0x0043, code:
            endFling();
     */
        /* JADX WARNING: Missing block: B:19:0x0046, code:
            return;
     */
        /* JADX WARNING: Missing block: B:20:0x0047, code:
            r28 = r31.mScroller;
            r24 = r28.computeScrollOffset();
            r30 = r28.getCurrY();
            r21 = dealWith2DSDR(r31.mLastFlingY - r30, r28);
     */
        /* JADX WARNING: Missing block: B:21:0x0065, code:
            if (r21 <= 0) goto L_0x0113;
     */
        /* JADX WARNING: Missing block: B:22:0x0067, code:
            r31.this$0.mMotionPosition = r31.this$0.mFirstPosition;
            r31.this$0.mMotionViewOriginalTop = r31.this$0.getChildAt(0).getTop();
            r21 = java.lang.Math.min(((r31.this$0.getHeight() - android.widget.AbsListView.-get12(r31.this$0)) - android.widget.AbsListView.-get13(r31.this$0)) - 1, r21);
     */
        /* JADX WARNING: Missing block: B:23:0x00a8, code:
            r25 = r31.this$0.getChildAt(r31.this$0.mMotionPosition - r31.this$0.mFirstPosition);
            r27 = 0;
     */
        /* JADX WARNING: Missing block: B:24:0x00bf, code:
            if (r25 == null) goto L_0x00c5;
     */
        /* JADX WARNING: Missing block: B:25:0x00c1, code:
            r27 = r25.getTop();
     */
        /* JADX WARNING: Missing block: B:26:0x00c5, code:
            r16 = r31.this$0.trackMotionScroll(r21, r21);
     */
        /* JADX WARNING: Missing block: B:27:0x00d1, code:
            if (r16 == false) goto L_0x0164;
     */
        /* JADX WARNING: Missing block: B:28:0x00d3, code:
            if (r21 == 0) goto L_0x0164;
     */
        /* JADX WARNING: Missing block: B:29:0x00d5, code:
            r17 = true;
     */
        /* JADX WARNING: Missing block: B:30:0x00d7, code:
            if (r17 == false) goto L_0x0168;
     */
        /* JADX WARNING: Missing block: B:31:0x00d9, code:
            if (r25 == null) goto L_0x0109;
     */
        /* JADX WARNING: Missing block: B:32:0x00db, code:
            r5 = -(r21 - (r25.getTop() - r27));
     */
        /* JADX WARNING: Missing block: B:33:0x00ec, code:
            if (r31.this$0.hasSpringAnimatorMask() != false) goto L_0x0109;
     */
        /* JADX WARNING: Missing block: B:34:0x00ee, code:
            android.widget.AbsListView.-wrap3(r31.this$0, 0, r5, 0, android.widget.AbsListView.-get16(r31.this$0), 0, 0, 0, r31.this$0.mOverflingDistance, false);
     */
        /* JADX WARNING: Missing block: B:35:0x0109, code:
            if (r24 == false) goto L_0x0112;
     */
        /* JADX WARNING: Missing block: B:36:0x010b, code:
            edgeReached(r21);
     */
        /* JADX WARNING: Missing block: B:37:0x0112, code:
            return;
     */
        /* JADX WARNING: Missing block: B:38:0x0113, code:
            r26 = r31.this$0.getChildCount() - 1;
            r31.this$0.mMotionPosition = r31.this$0.mFirstPosition + r26;
            r31.this$0.mMotionViewOriginalTop = r31.this$0.getChildAt(r26).getTop();
            r21 = java.lang.Math.max(-(((r31.this$0.getHeight() - android.widget.AbsListView.-get12(r31.this$0)) - android.widget.AbsListView.-get13(r31.this$0)) - 1), r21);
     */
        /* JADX WARNING: Missing block: B:39:0x0164, code:
            r17 = false;
     */
        /* JADX WARNING: Missing block: B:40:0x0168, code:
            if (r24 == false) goto L_0x01d1;
     */
        /* JADX WARNING: Missing block: B:42:0x016c, code:
            if ((r17 ^ 1) == 0) goto L_0x01d1;
     */
        /* JADX WARNING: Missing block: B:43:0x016e, code:
            if (r16 == false) goto L_0x0177;
     */
        /* JADX WARNING: Missing block: B:44:0x0170, code:
            r31.this$0.invalidate();
     */
        /* JADX WARNING: Missing block: B:45:0x0177, code:
            r31.mLastFlingY = r30;
            r31.this$0.postOnAnimation(r31);
     */
        /* JADX WARNING: Missing block: B:47:0x0192, code:
            if (android.widget.AbsListView.-get8(r31.this$0).isWechatOptimizeEffect() == false) goto L_0x0112;
     */
        /* JADX WARNING: Missing block: B:49:0x01a0, code:
            if (android.widget.AbsListView.-get8(r31.this$0).isWechatFling() == false) goto L_0x0112;
     */
        /* JADX WARNING: Missing block: B:51:0x01b9, code:
            if (java.lang.Math.abs(r28.getCurrVelocity()) >= ((float) android.widget.AbsListView.-get8(r31.this$0).getWechatIdleVelocity())) goto L_0x0112;
     */
        /* JADX WARNING: Missing block: B:52:0x01bb, code:
            android.widget.AbsListView.-get8(r31.this$0).setWechatFling(false);
            r31.this$0.reportScrollStateChange(0);
     */
        /* JADX WARNING: Missing block: B:53:0x01d1, code:
            endFling();
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
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
                    }
                    int scrollY = AbsListView.this.mScrollY;
                    int currY = scroller.getCurrY();
                    if (!AbsListView.this.-wrap3(0, currY - scrollY, 0, scrollY, 0, 0, 0, AbsListView.this.mOverflingDistance, false)) {
                        AbsListView.this.invalidate();
                        AbsListView.this.postOnAnimation(this);
                        break;
                    }
                    boolean crossDown = scrollY <= 0 && currY > 0;
                    boolean crossUp = scrollY >= 0 && currY < 0;
                    if (!crossDown && !crossUp) {
                        startSpringback();
                        break;
                    }
                    int velocity = (int) scroller.getCurrVelocity();
                    if (crossUp) {
                        velocity = -velocity;
                    }
                    scroller.abortAnimation();
                    start(velocity);
                    break;
                    break;
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
                return LogException.NO_VALUE;
            }
            return this.mTarget.getTextBeforeCursor(n, flags);
        }

        public CharSequence getTextAfterCursor(int n, int flags) {
            if (this.mTarget == null) {
                return LogException.NO_VALUE;
            }
            return this.mTarget.getTextAfterCursor(n, flags);
        }

        public CharSequence getSelectedText(int flags) {
            if (this.mTarget == null) {
                return LogException.NO_VALUE;
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
            return this.mTarget != null ? this.mTarget.finishComposingText() : true;
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

    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {
        @ExportedProperty(category = "list")
        boolean forceAdd;
        boolean isEnabled;
        long itemId = -1;
        @ExportedProperty(category = "list")
        boolean recycledHeaderFooter;
        int scrappedFromPosition;
        @ExportedProperty(category = "list", mapping = {@IntToString(from = -1, to = "ITEM_VIEW_TYPE_IGNORE"), @IntToString(from = -2, to = "ITEM_VIEW_TYPE_HEADER_OR_FOOTER")})
        int viewType;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(int w, int h, int viewType) {
            super(w, h);
            this.viewType = viewType;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        protected void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("list:viewType", this.viewType);
            encoder.addProperty("list:recycledHeaderFooter", this.recycledHeaderFooter);
            encoder.addProperty("list:forceAdd", this.forceAdd);
            encoder.addProperty("list:isEnabled", this.isEnabled);
        }
    }

    class ListItemAccessibilityDelegate extends AccessibilityDelegate {
        ListItemAccessibilityDelegate() {
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            AbsListView.this.onInitializeAccessibilityNodeInfoForItem(host, AbsListView.this.getPositionForView(host), info);
        }

        /* JADX WARNING: Missing block: B:7:0x0018, code:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean performAccessibilityAction(View host, int action, Bundle arguments) {
            if (super.performAccessibilityAction(host, action, arguments)) {
                return true;
            }
            int position = AbsListView.this.getPositionForView(host);
            if (position == -1 || AbsListView.this.mAdapter == null || position >= AbsListView.this.mAdapter.getCount()) {
                return false;
            }
            android.view.ViewGroup.LayoutParams lp = host.getLayoutParams();
            int isItemEnabled;
            if (lp instanceof LayoutParams) {
                isItemEnabled = ((LayoutParams) lp).isEnabled;
            } else {
                isItemEnabled = 0;
            }
            if (!AbsListView.this.isEnabled() || (isItemEnabled ^ 1) != 0) {
                return false;
            }
            switch (action) {
                case 4:
                    if (AbsListView.this.getSelectedItemPosition() == position) {
                        return false;
                    }
                    AbsListView.this.setSelection(position);
                    return true;
                case 8:
                    if (AbsListView.this.getSelectedItemPosition() != position) {
                        return false;
                    }
                    AbsListView.this.setSelection(-1);
                    return true;
                case 16:
                    if (!AbsListView.this.isItemClickable(host)) {
                        return false;
                    }
                    return AbsListView.this.performItemClick(host, position, AbsListView.this.getItemIdAtPosition(position));
                case 32:
                    if (!AbsListView.this.isLongClickable()) {
                        return false;
                    }
                    return AbsListView.this.performLongPress(host, position, AbsListView.this.getItemIdAtPosition(position));
                default:
                    return false;
            }
        }
    }

    public interface MultiChoiceModeListener extends Callback {
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

        /* synthetic */ PerformClick(AbsListView this$0, PerformClick -this1) {
            this();
        }

        private PerformClick() {
            super(AbsListView.this, null);
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
                int viewTravelCount;
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
                if (viewTravelCount > 0) {
                    this.mScrollDuration = 200 / viewTravelCount;
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
                    int viewTravelCount;
                    int firstPos = AbsListView.this.mFirstPosition;
                    int lastPos = (firstPos + childCount) - 1;
                    int clampedPosition = Math.max(0, Math.min(AbsListView.this.getCount() - 1, position));
                    int posTravel;
                    int boundTravel;
                    if (clampedPosition < firstPos) {
                        int boundPosFromLast = lastPos - boundPosition;
                        if (boundPosFromLast >= 1) {
                            posTravel = (firstPos - clampedPosition) + 1;
                            boundTravel = boundPosFromLast - 1;
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
                            posTravel = (clampedPosition - lastPos) + 1;
                            boundTravel = boundPosFromFirst - 1;
                            if (boundTravel < posTravel) {
                                viewTravelCount = boundTravel;
                                this.mMode = 3;
                            } else {
                                viewTravelCount = posTravel;
                                this.mMode = 1;
                            }
                        } else {
                            return;
                        }
                    } else {
                        scrollToVisible(clampedPosition, boundPosition, 200);
                        return;
                    }
                    if (viewTravelCount > 0) {
                        this.mScrollDuration = 200 / viewTravelCount;
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

        public void startWithOffset(final int position, final int offset, final int duration) {
            stop();
            if (AbsListView.this.mDataChanged) {
                int postOffset = offset;
                AbsListView.this.mPositionScrollAfterLayout = new Runnable() {
                    public void run() {
                        PositionScroller.this.startWithOffset(position, offset, duration);
                    }
                };
                return;
            }
            int childCount = AbsListView.this.getChildCount();
            if (childCount != 0) {
                int viewTravelCount;
                offset += AbsListView.this.getPaddingTop();
                this.mTargetPos = Math.max(0, Math.min(AbsListView.this.getCount() - 1, position));
                this.mOffsetFromTop = offset;
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
                    AbsListView.this.smoothScrollBy(AbsListView.this.getChildAt(this.mTargetPos - firstPos).getTop() - offset, duration, true, false);
                    return;
                }
                float screenTravelCount = ((float) viewTravelCount) / ((float) childCount);
                if (screenTravelCount >= 1.0f) {
                    duration = (int) (((float) duration) / screenTravelCount);
                }
                this.mScrollDuration = duration;
                this.mLastSeenPos = -1;
                AbsListView.this.postOnAnimation(this);
            }
        }

        private void scrollToVisible(int targetPos, int boundPos, int duration) {
            int firstPos = AbsListView.this.mFirstPosition;
            int lastPos = (firstPos + AbsListView.this.getChildCount()) - 1;
            int paddedTop = AbsListView.this.mListPadding.top;
            int paddedBottom = AbsListView.this.getHeight() - AbsListView.this.mListPadding.bottom;
            if (targetPos < firstPos || targetPos > lastPos) {
                Log.w(AbsListView.TAG, "scrollToVisible called with targetPos " + targetPos + " not visible [" + firstPos + ", " + lastPos + "]");
            }
            if (boundPos < firstPos || boundPos > lastPos) {
                boundPos = -1;
            }
            View targetChild = AbsListView.this.getChildAt(targetPos - firstPos);
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
                if (boundPos >= 0) {
                    View boundChild = AbsListView.this.getChildAt(boundPos - firstPos);
                    int boundTop = boundChild.getTop();
                    int boundBottom = boundChild.getBottom();
                    int absScroll = Math.abs(scrollBy);
                    if (scrollBy < 0 && boundBottom + absScroll > paddedBottom) {
                        scrollBy = Math.max(0, boundBottom - paddedBottom);
                    } else if (scrollBy > 0 && boundTop - absScroll < paddedTop) {
                        scrollBy = Math.min(0, boundTop - paddedTop);
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
            int lastViewIndex;
            int lastPos;
            View lastView;
            int childCount;
            int extraScroll;
            switch (this.mMode) {
                case 1:
                    lastViewIndex = AbsListView.this.getChildCount() - 1;
                    lastPos = firstPos + lastViewIndex;
                    if (lastViewIndex >= 0) {
                        if (lastPos != this.mLastSeenPos) {
                            lastView = AbsListView.this.getChildAt(lastViewIndex);
                            AbsListView.this.smoothScrollBy((lastView.getHeight() - (listHeight - lastView.getTop())) + (lastPos < AbsListView.this.mItemCount + -1 ? Math.max(AbsListView.this.mListPadding.bottom, this.mExtraScroll) : AbsListView.this.mListPadding.bottom), this.mScrollDuration, true, lastPos < this.mTargetPos);
                            this.mLastSeenPos = lastPos;
                            if (lastPos < this.mTargetPos) {
                                AbsListView.this.postOnAnimation(this);
                                break;
                            }
                        }
                        AbsListView.this.postOnAnimation(this);
                        return;
                    }
                    return;
                    break;
                case 2:
                    if (firstPos != this.mLastSeenPos) {
                        View firstView = AbsListView.this.getChildAt(0);
                        if (firstView != null) {
                            AbsListView.this.smoothScrollBy(firstView.getTop() - (firstPos > 0 ? Math.max(this.mExtraScroll, AbsListView.this.mListPadding.top) : AbsListView.this.mListPadding.top), this.mScrollDuration, true, firstPos > this.mTargetPos);
                            this.mLastSeenPos = firstPos;
                            if (firstPos > this.mTargetPos) {
                                AbsListView.this.postOnAnimation(this);
                                break;
                            }
                        }
                        return;
                    }
                    AbsListView.this.postOnAnimation(this);
                    return;
                    break;
                case 3:
                    childCount = AbsListView.this.getChildCount();
                    if (firstPos != this.mBoundPos && childCount > 1 && firstPos + childCount < AbsListView.this.mItemCount) {
                        int nextPos = firstPos + 1;
                        if (nextPos != this.mLastSeenPos) {
                            View nextView = AbsListView.this.getChildAt(1);
                            int nextViewHeight = nextView.getHeight();
                            int nextViewTop = nextView.getTop();
                            extraScroll = Math.max(AbsListView.this.mListPadding.bottom, this.mExtraScroll);
                            if (nextPos >= this.mBoundPos) {
                                if (nextViewTop <= extraScroll) {
                                    AbsListView.this.reportScrollStateChange(0);
                                    break;
                                } else {
                                    AbsListView.this.smoothScrollBy(nextViewTop - extraScroll, this.mScrollDuration, true, false);
                                    break;
                                }
                            }
                            AbsListView.this.smoothScrollBy(Math.max(0, (nextViewHeight + nextViewTop) - extraScroll), this.mScrollDuration, true, true);
                            this.mLastSeenPos = nextPos;
                            AbsListView.this.postOnAnimation(this);
                            break;
                        }
                        AbsListView.this.postOnAnimation(this);
                        return;
                    }
                    AbsListView.this.reportScrollStateChange(0);
                    return;
                    break;
                case 4:
                    lastViewIndex = AbsListView.this.getChildCount() - 2;
                    if (lastViewIndex >= 0) {
                        lastPos = firstPos + lastViewIndex;
                        if (lastPos != this.mLastSeenPos) {
                            lastView = AbsListView.this.getChildAt(lastViewIndex);
                            int lastViewHeight = lastView.getHeight();
                            int lastViewTop = lastView.getTop();
                            int lastViewPixelsShowing = listHeight - lastViewTop;
                            extraScroll = Math.max(AbsListView.this.mListPadding.top, this.mExtraScroll);
                            this.mLastSeenPos = lastPos;
                            if (lastPos <= this.mBoundPos) {
                                int bottom = listHeight - extraScroll;
                                int lastViewBottom = lastViewTop + lastViewHeight;
                                if (bottom <= lastViewBottom) {
                                    AbsListView.this.reportScrollStateChange(0);
                                    break;
                                } else {
                                    AbsListView.this.smoothScrollBy(-(bottom - lastViewBottom), this.mScrollDuration, true, false);
                                    break;
                                }
                            }
                            AbsListView.this.smoothScrollBy(-(lastViewPixelsShowing - extraScroll), this.mScrollDuration, true, true);
                            AbsListView.this.postOnAnimation(this);
                            break;
                        }
                        AbsListView.this.postOnAnimation(this);
                        return;
                    }
                    return;
                case 5:
                    if (this.mLastSeenPos != firstPos) {
                        this.mLastSeenPos = firstPos;
                        childCount = AbsListView.this.getChildCount();
                        int position = this.mTargetPos;
                        lastPos = (firstPos + childCount) - 1;
                        View firstChild = AbsListView.this.getChildAt(0);
                        if (firstChild != null) {
                            int firstChildHeight = firstChild.getHeight();
                            View lastChild = AbsListView.this.getChildAt(childCount - 1);
                            if (lastChild != null) {
                                float firstPositionVisiblePart;
                                float lastPositionVisiblePart;
                                int lastChildHeight = lastChild.getHeight();
                                if (((float) firstChildHeight) == 0.0f) {
                                    firstPositionVisiblePart = 1.0f;
                                } else {
                                    firstPositionVisiblePart = ((float) (firstChild.getTop() + firstChildHeight)) / ((float) firstChildHeight);
                                }
                                if (((float) lastChildHeight) == 0.0f) {
                                    lastPositionVisiblePart = 1.0f;
                                } else {
                                    lastPositionVisiblePart = ((float) ((AbsListView.this.getHeight() + lastChildHeight) - lastChild.getBottom())) / ((float) lastChildHeight);
                                }
                                float viewTravelCount = 0.0f;
                                if (position < firstPos) {
                                    viewTravelCount = (((float) (firstPos - position)) + (1.0f - firstPositionVisiblePart)) + 1.0f;
                                } else if (position > lastPos) {
                                    viewTravelCount = ((float) (position - lastPos)) + (1.0f - lastPositionVisiblePart);
                                }
                                float modifier = Math.min(Math.abs(viewTravelCount / ((float) childCount)), 1.0f);
                                if (position >= firstPos) {
                                    if (position <= lastPos) {
                                        int distance = AbsListView.this.getChildAt(position - firstPos).getTop() - this.mOffsetFromTop;
                                        AbsListView.this.smoothScrollBy(distance, (int) (((float) this.mScrollDuration) * (((float) Math.abs(distance)) / ((float) AbsListView.this.getHeight()))), true, false);
                                        break;
                                    }
                                    AbsListView.this.smoothScrollBy((int) (((float) AbsListView.this.getHeight()) * modifier), (int) (((float) this.mScrollDuration) * modifier), true, true);
                                    AbsListView.this.postOnAnimation(this);
                                    break;
                                }
                                AbsListView.this.smoothScrollBy((int) (((float) (-AbsListView.this.getHeight())) * modifier), (int) (((float) this.mScrollDuration) * modifier), true, true);
                                AbsListView.this.postOnAnimation(this);
                                break;
                            }
                            return;
                        }
                        return;
                    }
                    AbsListView.this.postOnAnimation(this);
                    return;
            }
        }
    }

    class RecycleBin {
        private View[] mActiveViews = new View[0];
        private ArrayList<View> mCurrentScrap;
        private int mFirstActivePosition;
        private RecyclerListener mRecyclerListener;
        private ArrayList<View>[] mScrapViews;
        private ArrayList<View> mSkippedScrap;
        private SparseArray<View> mTransientStateViews;
        private LongSparseArray<View> mTransientStateViewsById;
        private int mViewTypeCount;

        RecycleBin() {
        }

        public void setViewTypeCount(int viewTypeCount) {
            if (viewTypeCount < 1) {
                throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
            }
            ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
            for (int i = 0; i < viewTypeCount; i++) {
                scrapViews[i] = new ArrayList();
            }
            this.mViewTypeCount = viewTypeCount;
            this.mCurrentScrap = scrapViews[0];
            this.mScrapViews = scrapViews;
        }

        public void markChildrenDirty() {
            int i;
            int count;
            ArrayList<View> scrap;
            int scrapCount;
            if (this.mViewTypeCount == 1) {
                scrap = this.mCurrentScrap;
                scrapCount = scrap.size();
                for (i = 0; i < scrapCount; i++) {
                    ((View) scrap.get(i)).forceLayout();
                }
            } else {
                int typeCount = this.mViewTypeCount;
                for (i = 0; i < typeCount; i++) {
                    scrap = this.mScrapViews[i];
                    scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        ((View) scrap.get(j)).forceLayout();
                    }
                }
            }
            if (this.mTransientStateViews != null) {
                count = this.mTransientStateViews.size();
                for (i = 0; i < count; i++) {
                    ((View) this.mTransientStateViews.valueAt(i)).forceLayout();
                }
            }
            if (this.mTransientStateViewsById != null) {
                count = this.mTransientStateViewsById.size();
                for (i = 0; i < count; i++) {
                    ((View) this.mTransientStateViewsById.valueAt(i)).forceLayout();
                }
            }
        }

        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0;
        }

        void clear() {
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

        void fillActiveViews(int childCount, int firstActivePosition) {
            if (this.mActiveViews.length < childCount) {
                this.mActiveViews = new View[childCount];
            }
            this.mFirstActivePosition = firstActivePosition;
            View[] activeViews = this.mActiveViews;
            for (int i = 0; i < childCount; i++) {
                View child = AbsListView.this.getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if ((lp != null && lp.viewType != -2) || AbsListView.this.mListAnimationSwitch) {
                    activeViews[i] = child;
                    lp.scrappedFromPosition = firstActivePosition + i;
                }
            }
        }

        View getActiveView(int position) {
            int index = position - this.mFirstActivePosition;
            View[] activeViews = this.mActiveViews;
            if (index < 0 || index >= activeViews.length) {
                return null;
            }
            View match = activeViews[index];
            activeViews[index] = null;
            return match;
        }

        View getTransientStateView(int position) {
            View result;
            if (AbsListView.this.mAdapter == null || !AbsListView.this.mAdapterHasStableIds || this.mTransientStateViewsById == null) {
                if (this.mTransientStateViews != null) {
                    int index = this.mTransientStateViews.indexOfKey(position);
                    if (index >= 0) {
                        result = (View) this.mTransientStateViews.valueAt(index);
                        this.mTransientStateViews.removeAt(index);
                        return result;
                    }
                }
                return null;
            }
            long id = AbsListView.this.mAdapter.getItemId(position);
            result = (View) this.mTransientStateViewsById.get(id);
            this.mTransientStateViewsById.remove(id);
            return result;
        }

        void clearTransientStateViews() {
            int N;
            int i;
            SparseArray<View> viewsByPos = this.mTransientStateViews;
            if (viewsByPos != null) {
                N = viewsByPos.size();
                for (i = 0; i < N; i++) {
                    removeDetachedView((View) viewsByPos.valueAt(i), false);
                }
                viewsByPos.clear();
            }
            LongSparseArray<View> viewsById = this.mTransientStateViewsById;
            if (viewsById != null) {
                N = viewsById.size();
                for (i = 0; i < N; i++) {
                    removeDetachedView((View) viewsById.valueAt(i), false);
                }
                viewsById.clear();
            }
        }

        View getScrapView(int position) {
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

        void addScrapView(View scrap, int position) {
            LayoutParams lp = (LayoutParams) scrap.getLayoutParams();
            if (lp != null) {
                lp.scrappedFromPosition = position;
                int viewType = lp.viewType;
                if (shouldRecycleViewType(viewType)) {
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
                            this.mTransientStateViewsById = new LongSparseArray();
                        }
                        this.mTransientStateViewsById.put(lp.itemId, scrap);
                    } else if (AbsListView.this.mDataChanged) {
                        clearScrapForRebind(scrap);
                        getSkippedScrap().add(scrap);
                    } else {
                        if (this.mTransientStateViews == null) {
                            this.mTransientStateViews = new SparseArray();
                        }
                        this.mTransientStateViews.put(position, scrap);
                    }
                    return;
                }
                if (viewType != -2) {
                    getSkippedScrap().add(scrap);
                }
            }
        }

        private ArrayList<View> getSkippedScrap() {
            if (this.mSkippedScrap == null) {
                this.mSkippedScrap = new ArrayList();
            }
            return this.mSkippedScrap;
        }

        void removeSkippedScrap() {
            if (this.mSkippedScrap != null) {
                int count = this.mSkippedScrap.size();
                for (int i = 0; i < count; i++) {
                    removeDetachedView((View) this.mSkippedScrap.get(i), false);
                }
                this.mSkippedScrap.clear();
            }
        }

        void scrapActiveViews() {
            View[] activeViews = this.mActiveViews;
            boolean hasListener = this.mRecyclerListener != null;
            boolean multipleScraps = this.mViewTypeCount > 1;
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
                                this.mTransientStateViewsById = new LongSparseArray();
                            }
                            this.mTransientStateViewsById.put(AbsListView.this.mAdapter.getItemId(this.mFirstActivePosition + i), victim);
                        } else if (!AbsListView.this.mDataChanged) {
                            if (this.mTransientStateViews == null) {
                                this.mTransientStateViews = new SparseArray();
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

        void fullyDetachScrapViews() {
            int viewTypeCount = this.mViewTypeCount;
            ArrayList<View>[] scrapViews = this.mScrapViews;
            for (int i = 0; i < viewTypeCount; i++) {
                ArrayList<View> scrapPile = scrapViews[i];
                for (int j = scrapPile.size() - 1; j >= 0; j--) {
                    View view = (View) scrapPile.get(j);
                    if (view.isTemporarilyDetached()) {
                        removeDetachedView(view, false);
                    }
                }
            }
        }

        private void pruneScrapViews() {
            int i;
            View v;
            int maxViews = this.mActiveViews.length;
            int viewTypeCount = this.mViewTypeCount;
            ArrayList<View>[] scrapViews = this.mScrapViews;
            for (i = 0; i < viewTypeCount; i++) {
                ArrayList<View> scrapPile = scrapViews[i];
                int size = scrapPile.size();
                while (size > maxViews) {
                    size--;
                    scrapPile.remove(size);
                }
            }
            SparseArray<View> transViewsByPos = this.mTransientStateViews;
            if (transViewsByPos != null) {
                i = 0;
                while (i < transViewsByPos.size()) {
                    v = (View) transViewsByPos.valueAt(i);
                    if (!v.hasTransientState()) {
                        removeDetachedView(v, false);
                        transViewsByPos.removeAt(i);
                        i--;
                    }
                    i++;
                }
            }
            LongSparseArray<View> transViewsById = this.mTransientStateViewsById;
            if (transViewsById != null) {
                i = 0;
                while (i < transViewsById.size()) {
                    v = (View) transViewsById.valueAt(i);
                    if (!v.hasTransientState()) {
                        removeDetachedView(v, false);
                        transViewsById.removeAt(i);
                        i--;
                    }
                    i++;
                }
            }
        }

        void reclaimScrapViews(List<View> views) {
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

        void setCacheColorHint(int color) {
            int i;
            ArrayList<View> scrap;
            int scrapCount;
            if (this.mViewTypeCount == 1) {
                scrap = this.mCurrentScrap;
                scrapCount = scrap.size();
                for (i = 0; i < scrapCount; i++) {
                    ((View) scrap.get(i)).setDrawingCacheBackgroundColor(color);
                }
            } else {
                int typeCount = this.mViewTypeCount;
                for (i = 0; i < typeCount; i++) {
                    scrap = this.mScrapViews[i];
                    scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        ((View) scrap.get(j)).setDrawingCacheBackgroundColor(color);
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
            View scrap;
            for (int i = size - 1; i >= 0; i--) {
                LayoutParams params = (LayoutParams) ((View) scrapViews.get(i)).getLayoutParams();
                if (AbsListView.this.mAdapterHasStableIds) {
                    if (AbsListView.this.mAdapter.getItemId(position) == params.itemId) {
                        return (View) scrapViews.remove(i);
                    }
                } else if (params.scrappedFromPosition == position) {
                    scrap = (View) scrapViews.remove(i);
                    clearScrapForRebind(scrap);
                    return scrap;
                }
            }
            scrap = (View) scrapViews.remove(size - 1);
            clearScrapForRebind(scrap);
            return scrap;
        }

        private void clearScrap(ArrayList<View> scrap) {
            int scrapCount = scrap.size();
            for (int j = 0; j < scrapCount; j++) {
                removeDetachedView((View) scrap.remove((scrapCount - 1) - j), false);
            }
        }

        private void clearScrapForRebind(View view) {
            view.clearAccessibilityFocus();
            view.setAccessibilityDelegate(null);
        }

        private void removeDetachedView(View child, boolean animate) {
            child.setAccessibilityDelegate(null);
            AbsListView.this.-wrap6(child, animate);
        }
    }

    public interface RecyclerListener {
        void onMovedToScrapHeap(View view);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
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

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            boolean z = false;
            super(in);
            this.selectedId = in.readLong();
            this.firstId = in.readLong();
            this.viewTop = in.readInt();
            this.position = in.readInt();
            this.height = in.readInt();
            this.filter = in.readString();
            if (in.readByte() != (byte) 0) {
                z = true;
            }
            this.inActionMode = z;
            this.checkedItemCount = in.readInt();
            this.checkState = in.readSparseBooleanArray();
            int N = in.readInt();
            if (N > 0) {
                this.checkIdState = new LongSparseArray();
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
            out.writeByte((byte) (this.inActionMode ? 1 : 0));
            out.writeInt(this.checkedItemCount);
            out.writeSparseBooleanArray(this.checkState);
            int N = this.checkIdState != null ? this.checkIdState.size() : 0;
            out.writeInt(N);
            for (int i = 0; i < N; i++) {
                out.writeLong(this.checkIdState.keyAt(i));
                out.writeInt(((Integer) this.checkIdState.valueAt(i)).intValue());
            }
        }

        public String toString() {
            return "AbsListView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " selectedId=" + this.selectedId + " firstId=" + this.firstId + " viewTop=" + this.viewTop + " position=" + this.position + " height=" + this.height + " filter=" + this.filter + " checkState=" + this.checkState + "}";
        }
    }

    public interface SelectionBoundsAdjuster {
        void adjustListItemSelectionBounds(Rect rect);
    }

    abstract void fillGap(boolean z);

    abstract int findMotionRow(int i);

    abstract void setSelectionInt(int i);

    public AbsListView(Context context) {
        super(context);
        this.mChoiceMode = 0;
        this.mLayoutMode = 0;
        this.mListAnimationSwitch = false;
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
        this(context, attrs, R.attr.absListViewStyle);
    }

    public AbsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AbsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mChoiceMode = 0;
        this.mLayoutMode = 0;
        this.mListAnimationSwitch = false;
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
                this.mCheckedIdStates = new LongSparseArray();
            }
        }
        if (this.mCheckStates != null) {
            this.mCheckStates.clear();
        }
        if (this.mCheckedIdStates != null) {
            this.mCheckedIdStates.clear();
        }
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
        if (this.mChoiceMode != 0) {
            if (value && this.mChoiceMode == 3 && this.mChoiceActionMode == null) {
                if (this.mMultiChoiceModeCallback == null || (this.mMultiChoiceModeCallback.hasWrappedCallback() ^ 1) != 0) {
                    throw new IllegalStateException("AbsListView: attempted to start selection mode for CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was supplied. Call setMultiChoiceModeListener to set a callback.");
                }
                this.mChoiceActionMode = startActionMode(this.mMultiChoiceModeCallback);
            }
            boolean itemCheckChanged;
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
                itemCheckChanged = oldValue != value;
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
                boolean updateIds = this.mCheckedIdStates != null ? this.mAdapter.hasStableIds() : false;
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
                } else if (this.mCheckStates.size() == 0 || (this.mCheckStates.valueAt(0) ^ 1) != 0) {
                    this.mCheckedItemCount = 0;
                }
            }
            if (!(this.mInLayout || (this.mBlockLayoutRequests ^ 1) == 0 || !itemCheckChanged)) {
                this.mDataChanged = true;
                rememberSyncState();
                requestLayout();
            }
        }
    }

    public boolean performItemClick(View view, int position, long id) {
        boolean handled = false;
        boolean dispatchItemClick = true;
        if (this.mChoiceMode != 0) {
            handled = true;
            boolean checkedStateChanged = false;
            if (this.mChoiceMode == 2 || (this.mChoiceMode == 3 && this.mChoiceActionMode != null)) {
                boolean checked = getCheckedStateForMultiSelect(this.mCheckStates.get(position, false) ^ 1);
                this.mCheckStates.put(position, checked);
                if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                    if (checked) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    } else {
                        this.mCheckedIdStates.delete(this.mAdapter.getItemId(position));
                    }
                }
                if (checked) {
                    this.mCheckedItemCount++;
                } else {
                    this.mCheckedItemCount--;
                }
                if (this.mChoiceActionMode != null) {
                    this.mMultiChoiceModeCallback.onItemCheckedStateChanged(this.mChoiceActionMode, position, id, checked);
                    dispatchItemClick = false;
                }
                checkedStateChanged = true;
            } else if (this.mChoiceMode == 1) {
                if (this.mCheckStates.get(position, false) ^ 1) {
                    this.mCheckStates.clear();
                    this.mCheckStates.put(position, true);
                    if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                        this.mCheckedIdStates.clear();
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    }
                    this.mCheckedItemCount = 1;
                } else if (this.mCheckStates.size() == 0 || (this.mCheckStates.valueAt(0) ^ 1) != 0) {
                    this.mCheckedItemCount = 0;
                }
                checkedStateChanged = true;
            }
            if (checkedStateChanged) {
                updateOnScreenCheckedViews();
            }
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
                this.mCheckedIdStates = new LongSparseArray(0);
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

    private boolean contentFits() {
        boolean z = true;
        int childCount = getChildCount();
        if (childCount == 0) {
            return true;
        }
        if (childCount != this.mItemCount) {
            return false;
        }
        if (getChildAt(0).getTop() < this.mListPadding.top) {
            z = false;
        } else if (getChildAt(childCount - 1).getBottom() > getHeight() - this.mListPadding.bottom) {
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

    private void setFastScrollerEnabledUiThread(boolean enabled) {
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
            if (alwaysShow && (this.mFastScrollEnabled ^ 1) != 0) {
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

    private void setFastScrollerAlwaysVisibleUiThread(boolean alwaysShow) {
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
            if (this.mFastScrollEnabled) {
                z = this.mFastScrollAlwaysVisible;
            }
            return z;
        }
        if (this.mFastScroll.isEnabled()) {
            z = this.mFastScroll.isAlwaysShowEnabled();
        }
        return z;
    }

    public int getVerticalScrollbarWidth() {
        if (this.mFastScroll == null || !this.mFastScroll.isEnabled()) {
            return super.getVerticalScrollbarWidth();
        }
        return Math.max(super.getVerticalScrollbarWidth(), this.mFastScroll.getWidth());
    }

    @ExportedProperty
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

    protected boolean isVerticalScrollBarHidden() {
        return isFastScrollEnabled();
    }

    public void setSmoothScrollbarEnabled(boolean enabled) {
        this.mSmoothScrollbarEnabled = enabled;
    }

    @ExportedProperty
    public boolean isSmoothScrollbarEnabled() {
        return this.mSmoothScrollbarEnabled;
    }

    public void setOnScrollListener(OnScrollListener l) {
        this.mOnScrollListener = l;
        invokeOnItemScrollListener();
    }

    void invokeOnItemScrollListener() {
        if (this.mFastScroll != null) {
            this.mFastScroll.onScroll(this.mFirstPosition, getChildCount(), this.mItemCount);
        }
        if (this.mOnScrollListener != null) {
            this.mOnScrollListener.onScroll(this, this.mFirstPosition, getChildCount(), this.mItemCount);
        }
        onScrollChanged(0, 0, 0, 0);
    }

    public void sendAccessibilityEventInternal(int eventType) {
        if (eventType == 4096) {
            int firstVisiblePosition = getFirstVisiblePosition();
            int lastVisiblePosition = getLastVisiblePosition();
            if (this.mLastAccessibilityScrollEventFromIndex != firstVisiblePosition || this.mLastAccessibilityScrollEventToIndex != lastVisiblePosition) {
                this.mLastAccessibilityScrollEventFromIndex = firstVisiblePosition;
                this.mLastAccessibilityScrollEventToIndex = lastVisiblePosition;
            } else {
                return;
            }
        }
        super.sendAccessibilityEventInternal(eventType);
    }

    public CharSequence getAccessibilityClassName() {
        return AbsListView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (isEnabled()) {
            if (canScrollUp()) {
                info.addAction(AccessibilityAction.ACTION_SCROLL_BACKWARD);
                info.addAction(AccessibilityAction.ACTION_SCROLL_UP);
                info.setScrollable(true);
            }
            if (canScrollDown()) {
                info.addAction(AccessibilityAction.ACTION_SCROLL_FORWARD);
                info.addAction(AccessibilityAction.ACTION_SCROLL_DOWN);
                info.setScrollable(true);
            }
        }
        info.removeAction(AccessibilityAction.ACTION_CLICK);
        info.setClickable(false);
    }

    int getSelectionModeForAccessibility() {
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
        if (super.-wrap10(action, arguments)) {
            return true;
        }
        switch (action) {
            case 4096:
            case R.id.accessibilityActionScrollDown /*16908346*/:
                if (!isEnabled() || !canScrollDown()) {
                    return false;
                }
                smoothScrollBy((getHeight() - this.mListPadding.top) - this.mListPadding.bottom, 200);
                return true;
            case 8192:
            case R.id.accessibilityActionScrollUp /*16908344*/:
                if (!isEnabled() || !canScrollUp()) {
                    return false;
                }
                smoothScrollBy(-((getHeight() - this.mListPadding.top) - this.mListPadding.bottom), 200);
                return true;
            default:
                return false;
        }
    }

    public View findViewByAccessibilityIdTraversal(int accessibilityId) {
        if (accessibilityId == getAccessibilityViewId()) {
            return this;
        }
        return super.findViewByAccessibilityIdTraversal(accessibilityId);
    }

    @ExportedProperty
    public boolean isScrollingCacheEnabled() {
        return this.mScrollingCacheEnabled;
    }

    public void setScrollingCacheEnabled(boolean enabled) {
        if (this.mScrollingCacheEnabled && (enabled ^ 1) != 0) {
            clearScrollingCache();
        }
        this.mScrollingCacheEnabled = enabled;
    }

    public void setTextFilterEnabled(boolean textFilterEnabled) {
        this.mTextFilterEnabled = textFilterEnabled;
    }

    @ExportedProperty
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
        setSelector(getContext().getDrawable(R.drawable.list_selector_background));
    }

    @ExportedProperty
    public boolean isStackFromBottom() {
        return this.mStackFromBottom;
    }

    public void setStackFromBottom(boolean stackFromBottom) {
        if (this.mStackFromBottom != stackFromBottom) {
            this.mStackFromBottom = stackFromBottom;
            requestLayoutIfNecessary();
        }
    }

    void requestLayoutIfNecessary() {
        if (getChildCount() > 0) {
            resetList();
            requestLayout();
            invalidate();
        }
    }

    public Parcelable onSaveInstanceState() {
        dismissPopup();
        SavedState ss = new SavedState(super.-wrap0());
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
        boolean z = this.mChoiceMode == 3 && this.mChoiceActionMode != null;
        ss.inActionMode = z;
        if (this.mCheckStates != null) {
            ss.checkState = this.mCheckStates.clone();
        }
        if (this.mCheckedIdStates != null) {
            LongSparseArray<Integer> idState = new LongSparseArray();
            int count = this.mCheckedIdStates.size();
            for (int i = 0; i < count; i++) {
                idState.put(this.mCheckedIdStates.keyAt(i), (Integer) this.mCheckedIdStates.valueAt(i));
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
        super.-wrap2(ss.getSuperState());
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
        if (!this.mTextFilterEnabled || !(getAdapter() instanceof Filterable)) {
            return false;
        }
        if (((Filterable) getAdapter()).getFilter() != null) {
            return true;
        }
        return false;
    }

    public void setFilterText(String filterText) {
        if (this.mTextFilterEnabled && (TextUtils.isEmpty(filterText) ^ 1) != 0) {
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

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && this.mSelectedPosition < 0 && (isInTouchMode() ^ 1) != 0) {
            if (!(isAttachedToWindow() || this.mAdapter == null)) {
                this.mDataChanged = true;
                this.mOldItemCount = this.mItemCount;
                this.mItemCount = this.mAdapter.getCount();
            }
            resurrectSelection();
        }
    }

    public void requestLayout() {
        if (!this.mBlockLayoutRequests && (this.mInLayout ^ 1) != 0) {
            super.requestLayout();
        }
    }

    void resetList() {
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

    protected int computeVerticalScrollExtent() {
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
        view = getChildAt(count - 1);
        int bottom = view.getBottom();
        height = view.getHeight();
        if (height > 0) {
            extent -= ((bottom - getHeight()) * 100) / height;
        }
        return extent;
    }

    protected int computeVerticalScrollOffset() {
        int firstPosition = this.mFirstPosition;
        int childCount = getChildCount();
        if (firstPosition >= 0 && childCount > 0) {
            if (this.mSmoothScrollbarEnabled) {
                View view = getChildAt(0);
                int top = view.getTop();
                int height = view.getHeight();
                if (height > 0) {
                    return Math.max(((firstPosition * 100) - ((top * 100) / height)) + ((int) (((((float) this.mScrollY) / ((float) getHeight())) * ((float) this.mItemCount)) * 100.0f)), 0);
                }
            }
            int index;
            int count = this.mItemCount;
            if (firstPosition == 0) {
                index = 0;
            } else if (firstPosition + childCount == count) {
                index = count;
            } else {
                index = firstPosition + (childCount / 2);
            }
            return (int) (((float) firstPosition) + (((float) childCount) * (((float) index) / ((float) count))));
        }
        return 0;
    }

    protected int computeVerticalScrollRange() {
        if (!this.mSmoothScrollbarEnabled) {
            return this.mItemCount;
        }
        int result = Math.max(this.mItemCount * 100, 0);
        if (this.mScrollY != 0) {
            return result + Math.abs((int) (((((float) this.mScrollY) / ((float) getHeight())) * ((float) this.mItemCount)) * 100.0f));
        }
        return result;
    }

    protected float getTopFadingEdgeStrength() {
        int count = getChildCount();
        float fadeEdge = super.getTopFadingEdgeStrength();
        if (count == 0) {
            return fadeEdge;
        }
        if (this.mFirstPosition > 0) {
            return 1.0f;
        }
        int top = getChildAt(0).getTop();
        float fadeLength = (float) getVerticalFadingEdgeLength();
        if (top < this.mPaddingTop) {
            fadeEdge = ((float) (-(top - this.mPaddingTop))) / fadeLength;
        }
        return fadeEdge;
    }

    protected float getBottomFadingEdgeStrength() {
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
            fadeEdge = ((float) ((bottom - height) + this.mPaddingBottom)) / fadeLength;
        }
        return fadeEdge;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean z = true;
        if (this.mSelector == null) {
            useDefaultSelector();
        }
        Rect listPadding = this.mListPadding;
        listPadding.left = this.mSelectionLeftPadding + this.mPaddingLeft;
        listPadding.top = this.mSelectionTopPadding + this.mPaddingTop;
        listPadding.right = this.mSelectionRightPadding + this.mPaddingRight;
        listPadding.bottom = this.mSelectionBottomPadding + this.mPaddingBottom;
        if (this.mTranscriptMode == 1) {
            int childCount = getChildCount();
            int listBottom = getHeight() - getPaddingBottom();
            View lastChild = getChildAt(childCount - 1);
            int lastBottom = lastChild != null ? lastChild.getBottom() : listBottom;
            if (this.mFirstPosition + childCount < this.mLastHandledItemCount) {
                z = false;
            } else if (lastBottom > listBottom) {
                z = false;
            }
            this.mForceTranscriptScroll = z;
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
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

    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = super.-wrap13(left, top, right, bottom);
        if (changed) {
            boolean visible = getWindowVisibility() == 0;
            if (this.mFiltered && visible && this.mPopup != null && this.mPopup.isShowing()) {
                positionPopup();
            }
        }
        return changed;
    }

    protected void layoutChildren() {
    }

    View getAccessibilityFocusedChild(View focusedView) {
        View viewParent = focusedView.getParent();
        while ((viewParent instanceof View) && viewParent != this) {
            focusedView = viewParent;
            viewParent = viewParent.getParent();
        }
        if (viewParent instanceof View) {
            return focusedView;
        }
        return null;
    }

    void updateScrollIndicators() {
        int i = 0;
        if (this.mScrollUp != null) {
            this.mScrollUp.setVisibility(canScrollUp() ? 0 : 4);
        }
        if (this.mScrollDown != null) {
            View view = this.mScrollDown;
            if (!canScrollDown()) {
                i = 4;
            }
            view.setVisibility(i);
        }
    }

    private boolean canScrollUp() {
        boolean canScrollUp = this.mFirstPosition > 0;
        if (canScrollUp || getChildCount() <= 0) {
            return canScrollUp;
        }
        return getChildAt(0).getTop() < this.mListPadding.top;
    }

    private boolean canScrollDown() {
        int count = getChildCount();
        boolean canScrollDown = this.mFirstPosition + count < this.mItemCount;
        if (canScrollDown || count <= 0) {
            return canScrollDown;
        }
        return getChildAt(count + -1).getBottom() > this.mBottom - this.mListPadding.bottom;
    }

    @ExportedProperty
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

    View obtainView(int position, boolean[] outMetadata) {
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
        android.view.ViewGroup.LayoutParams lp;
        android.view.ViewGroup.LayoutParams vlp = child.getLayoutParams();
        LayoutParams lp2;
        if (vlp == null) {
            lp2 = (LayoutParams) generateDefaultLayoutParams();
        } else if (checkLayoutParams(vlp)) {
            lp2 = (LayoutParams) vlp;
        } else {
            lp2 = (LayoutParams) generateLayoutParams(vlp);
        }
        if (this.mAdapterHasStableIds) {
            lp2.itemId = this.mAdapter.getItemId(position);
        }
        lp2.viewType = getHwItemViewType(position);
        lp2.isEnabled = this.mAdapter.isEnabled(position);
        if (lp2 != vlp) {
            child.-wrap18(lp2);
        }
    }

    public void onInitializeAccessibilityNodeInfoForItem(View view, int position, AccessibilityNodeInfo info) {
        ListAdapter adapter = getAdapter();
        if (position != -1 && adapter != null && position <= adapter.getCount() - 1) {
            android.view.ViewGroup.LayoutParams lp = view.getLayoutParams();
            int isItemEnabled;
            if (lp instanceof LayoutParams) {
                isItemEnabled = ((LayoutParams) lp).isEnabled;
            } else {
                isItemEnabled = 0;
            }
            if (isEnabled() && (isItemEnabled ^ 1) == 0) {
                if (position == getSelectedItemPosition()) {
                    info.setSelected(true);
                    info.addAction(AccessibilityAction.ACTION_CLEAR_SELECTION);
                } else {
                    info.addAction(AccessibilityAction.ACTION_SELECT);
                }
                if (isItemClickable(view)) {
                    info.addAction(AccessibilityAction.ACTION_CLICK);
                    info.setClickable(true);
                }
                if (isLongClickable()) {
                    info.addAction(AccessibilityAction.ACTION_LONG_CLICK);
                    info.setLongClickable(true);
                }
                return;
            }
            info.setEnabled(false);
        }
    }

    private boolean isItemClickable(View view) {
        return view.hasExplicitFocusable() ^ 1;
    }

    void positionSelectorLikeTouch(int position, View sel, float x, float y) {
        positionSelector(position, sel, true, x, y);
    }

    void positionSelectorLikeFocus(int position, View sel) {
        if (this.mSelector == null || this.mSelectorPosition == position || position == -1) {
            positionSelector(position, sel);
            return;
        }
        Rect bounds = this.mSelectorRect;
        positionSelector(position, sel, true, bounds.exactCenterX(), bounds.exactCenterY());
    }

    void positionSelector(int position, View sel) {
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

    protected void dispatchDraw(Canvas canvas) {
        int saveCount = 0;
        boolean clipToPadding = (this.mGroupFlags & 34) == 34;
        if (clipToPadding) {
            saveCount = canvas.save();
            int scrollX = this.mScrollX;
            int scrollY = this.mScrollY;
            canvas.clipRect(this.mPaddingLeft + scrollX, this.mPaddingTop + scrollY, ((this.mRight + scrollX) - this.mLeft) - this.mPaddingRight, ((this.mBottom + scrollY) - this.mTop) - this.mPaddingBottom);
            this.mGroupFlags &= -35;
        }
        boolean drawSelectorOnTop = this.mDrawSelectorOnTop;
        if (!drawSelectorOnTop) {
            drawSelector(canvas);
        }
        super.dispatchDraw(canvas);
        if (drawSelectorOnTop) {
            drawSelector(canvas);
        }
        if (clipToPadding) {
            canvas.restoreToCount(saveCount);
            this.mGroupFlags |= 34;
        }
    }

    protected boolean isPaddingOffsetRequired() {
        return (this.mGroupFlags & 34) != 34;
    }

    protected int getLeftPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? 0 : -this.mPaddingLeft;
    }

    protected int getTopPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? 0 : -this.mPaddingTop;
    }

    protected int getRightPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? 0 : this.mPaddingRight;
    }

    protected int getBottomPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? 0 : this.mPaddingBottom;
    }

    protected void internalSetPadding(int left, int top, int right, int bottom) {
        super.internalSetPadding(left, top, right, bottom);
        if (isLayoutRequested()) {
            handleBoundsChange();
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        handleBoundsChange();
        if (this.mFastScroll != null) {
            this.mFastScroll.onSizeChanged(w, h, oldw, oldh);
        }
    }

    void handleBoundsChange() {
        if (!this.mInLayout) {
            int childCount = getChildCount();
            if (childCount > 0) {
                this.mDataChanged = true;
                rememberSyncState();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    android.view.ViewGroup.LayoutParams lp = child.getLayoutParams();
                    if (lp == null || lp.width < 1 || lp.height < 1) {
                        child.forceLayout();
                    }
                }
            }
        }
    }

    boolean touchModeDrawsInPressedState() {
        switch (this.mTouchMode) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }

    boolean shouldShowSelector() {
        if (!isFocused() || (isInTouchMode() ^ 1) == 0) {
            return touchModeDrawsInPressedState() ? isPressed() : false;
        } else {
            return true;
        }
    }

    private void drawSelector(Canvas canvas) {
        if (!this.mSelectorRect.isEmpty()) {
            Drawable selector = this.mSelector;
            selector.setBounds(this.mSelectorRect);
            selector.draw(canvas);
        }
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

    void keyPressed() {
        if (isEnabled() && (isClickable() ^ 1) == 0) {
            Drawable selector = this.mSelector;
            Rect selectorRect = this.mSelectorRect;
            if (selector != null && ((isFocused() || touchModeDrawsInPressedState()) && (selectorRect.isEmpty() ^ 1) != 0)) {
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
                if (longClickable && (this.mDataChanged ^ 1) != 0) {
                    if (this.mPendingCheckForKeyLongPress == null) {
                        this.mPendingCheckForKeyLongPress = new CheckForKeyLongPress(this, null);
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

    void updateSelectorState() {
        Drawable selector = this.mSelector;
        if (selector != null && selector.isStateful()) {
            if (!shouldShowSelector()) {
                selector.setState(StateSet.NOTHING);
            } else if (selector.setState(getDrawableStateForSelector())) {
                invalidateDrawable(selector);
            }
        }
    }

    protected void drawableStateChanged() {
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
        for (int i = state.length - 1; i >= 0; i--) {
            if (state[i] == enabledState) {
                enabledPos = i;
                break;
            }
        }
        if (enabledPos >= 0) {
            System.arraycopy(state, enabledPos + 1, state, enabledPos, (state.length - enabledPos) - 1);
        }
        return state;
    }

    public boolean verifyDrawable(Drawable dr) {
        return this.mSelector != dr ? super.verifyDrawable(dr) : true;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mSelector != null) {
            this.mSelector.jumpToCurrentState();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewTreeObserver treeObserver = getViewTreeObserver();
        treeObserver.addOnTouchModeChangeListener(this);
        if (!(!this.mTextFilterEnabled || this.mPopup == null || (this.mGlobalLayoutListenerAddedFilter ^ 1) == 0)) {
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

    protected void onDetachedFromWindow() {
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
        int touchMode = isInTouchMode() ? 0 : 1;
        if (hasWindowFocus) {
            if (this.mFiltered && (this.mPopupHidden ^ 1) != 0) {
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
        } else {
            -wrap7(false);
            if (this.mFlingRunnable != null) {
                removeCallbacks(this.mFlingRunnable);
                this.mFlingRunnable.mSuppressIdleStateChangeCall = false;
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
        }
        this.mLastTouchMode = touchMode;
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (this.mFastScroll != null) {
            this.mFastScroll.setScrollbarPosition(getVerticalScrollbarPosition());
        }
    }

    ContextMenuInfo createContextMenuInfo(View view, int position, long id) {
        return new AdapterContextMenuInfo(view, position, id);
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

    boolean performLongPress(View child, int longPressPosition, long longPressId) {
        return performLongPress(child, longPressPosition, longPressId, -1.0f, -1.0f);
    }

    boolean performLongPress(View child, int longPressPosition, long longPressId, float x, float y) {
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

    protected ContextMenuInfo getContextMenuInfo() {
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

    protected void dispatchSetPressed(boolean pressed) {
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
        int i;
        int motionCorrectionCompensation = 0;
        if (this.mMotionCorrection != 0) {
            motionCorrectionCompensation = this.mMotionCorrection > 0 ? -1 : 1;
            this.mMotionCorrection += motionCorrectionCompensation;
        }
        int rawDeltaY = y - this.mMotionY;
        int scrollOffsetCorrection = 0;
        int scrollConsumedCorrection = 0;
        if (this.mLastY == Integer.MIN_VALUE) {
            rawDeltaY -= this.mMotionCorrection;
        }
        if (this.mLastY != Integer.MIN_VALUE) {
            i = this.mLastY - y;
        } else {
            i = -rawDeltaY;
        }
        if (dispatchNestedPreScroll(0, i, this.mScrollConsumed, this.mScrollOffset)) {
            rawDeltaY += this.mScrollConsumed[1];
            scrollOffsetCorrection = -this.mScrollOffset[1];
            scrollConsumedCorrection = this.mScrollConsumed[1];
            if (vtev != null) {
                vtev.offsetLocation(0.0f, (float) this.mScrollOffset[1]);
                this.mNestedYOffset += this.mScrollOffset[1];
            }
        }
        int deltaY = rawDeltaY;
        int incrementalDeltaY = this.mLastY != Integer.MIN_VALUE ? ((y - this.mLastY) + scrollConsumedCorrection) - motionCorrectionCompensation : deltaY;
        int lastYCorrection = 0;
        View motionView;
        int overscrollMode;
        if (this.mTouchMode == 3) {
            if (this.mScrollStrictSpan == null) {
                this.mScrollStrictSpan = StrictMode.enterCriticalSpan("AbsListView-scroll");
            }
            if (y != this.mLastY) {
                int motionIndex;
                if ((this.mGroupFlags & 524288) == 0 && Math.abs(rawDeltaY) > this.mTouchSlop) {
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
                int motionViewPrevTop = 0;
                motionView = getChildAt(motionIndex);
                if (motionView != null) {
                    motionViewPrevTop = motionView.getTop();
                }
                boolean atEdge = false;
                if (incrementalDeltaY != 0) {
                    atEdge = trackMotionScroll(deltaY, incrementalDeltaY);
                }
                motionView = getChildAt(motionIndex);
                if (motionView != null) {
                    int motionViewRealTop = motionView.getTop();
                    if (atEdge) {
                        int overscroll = (-incrementalDeltaY) - (motionViewRealTop - motionViewPrevTop);
                        if (dispatchNestedScroll(0, overscroll - incrementalDeltaY, 0, overscroll, this.mScrollOffset)) {
                            lastYCorrection = 0 - this.mScrollOffset[1];
                            if (vtev != null) {
                                vtev.offsetLocation(0.0f, (float) this.mScrollOffset[1]);
                                this.mNestedYOffset += this.mScrollOffset[1];
                            }
                        } else {
                            boolean atOverscrollEdge = -wrap3(0, overscroll, 0, this.mScrollY, 0, 0, 0, this.mOverscrollDistance, true);
                            if (atOverscrollEdge && this.mVelocityTracker != null) {
                                this.mVelocityTracker.clear();
                            }
                            overscrollMode = getOverScrollMode();
                            if (overscrollMode == 0 || (overscrollMode == 1 && (contentFits() ^ 1) != 0)) {
                                if (!atOverscrollEdge) {
                                    this.mDirection = 0;
                                    this.mTouchMode = 5;
                                }
                                if (this.mEdgeGlowTop != null) {
                                    if (incrementalDeltaY > 0) {
                                        this.mEdgeGlowTop.onPull(((float) (-overscroll)) / ((float) getHeight()), ((float) x) / ((float) getWidth()));
                                        if (!this.mEdgeGlowBottom.isFinished()) {
                                            this.mEdgeGlowBottom.onRelease();
                                        }
                                        invalidateTopGlow();
                                    } else if (incrementalDeltaY < 0) {
                                        this.mEdgeGlowBottom.onPull(((float) overscroll) / ((float) getHeight()), 1.0f - (((float) x) / ((float) getWidth())));
                                        if (!this.mEdgeGlowTop.isFinished()) {
                                            this.mEdgeGlowTop.onRelease();
                                        }
                                        invalidateBottomGlow();
                                    }
                                }
                            }
                        }
                    }
                    this.mMotionY = (y + lastYCorrection) + scrollOffsetCorrection;
                }
                this.mLastY = (y + lastYCorrection) + scrollOffsetCorrection;
            }
        } else if (this.mTouchMode == 5 && y != this.mLastY) {
            int oldScroll = this.mScrollY;
            int newScroll = oldScroll - incrementalDeltaY;
            int newDirection = y > this.mLastY ? 1 : -1;
            if (this.mDirection == 0) {
                this.mDirection = newDirection;
            }
            int overScrollDistance = -incrementalDeltaY;
            if ((newScroll >= 0 || oldScroll < 0) && (newScroll <= 0 || oldScroll > 0)) {
                incrementalDeltaY = 0;
            } else {
                overScrollDistance = -oldScroll;
                incrementalDeltaY += overScrollDistance;
            }
            if (overScrollDistance != 0) {
                -wrap3(0, overScrollDistance, 0, this.mScrollY, 0, 0, 0, this.mOverscrollDistance, true);
                overscrollMode = getOverScrollMode();
                if ((overscrollMode == 0 || (overscrollMode == 1 && (contentFits() ^ 1) != 0)) && this.mEdgeGlowTop != null) {
                    if (rawDeltaY > 0) {
                        this.mEdgeGlowTop.onPull(((float) overScrollDistance) / ((float) getHeight()), ((float) x) / ((float) getWidth()));
                        if (!this.mEdgeGlowBottom.isFinished()) {
                            this.mEdgeGlowBottom.onRelease();
                        }
                        invalidateTopGlow();
                    } else if (rawDeltaY < 0) {
                        this.mEdgeGlowBottom.onPull(((float) overScrollDistance) / ((float) getHeight()), 1.0f - (((float) x) / ((float) getWidth())));
                        if (!this.mEdgeGlowTop.isFinished()) {
                            this.mEdgeGlowTop.onRelease();
                        }
                        invalidateBottomGlow();
                    }
                }
            }
            if (incrementalDeltaY != 0) {
                if (this.mScrollY != 0) {
                    this.mScrollY = 0;
                    invalidateParentIfNeeded();
                }
                trackMotionScroll(incrementalDeltaY, incrementalDeltaY);
                this.mTouchMode = 3;
                int motionPosition = findClosestMotionRow(y);
                this.mMotionCorrection = 0;
                motionView = getChildAt(motionPosition - this.mFirstPosition);
                this.mMotionViewOriginalTop = motionView != null ? motionView.getTop() : 0;
                this.mMotionY = y + scrollOffsetCorrection;
                this.mMotionPosition = motionPosition;
            }
            this.mLastY = (y + 0) + scrollOffsetCorrection;
            this.mDirection = newDirection;
        }
    }

    private void invalidateTopGlow() {
        if (this.mEdgeGlowTop != null) {
            boolean clipToPadding = getClipToPadding();
            int top = clipToPadding ? this.mPaddingTop : 0;
            invalidate(clipToPadding ? this.mPaddingLeft : 0, top, clipToPadding ? getWidth() - this.mPaddingRight : getWidth(), this.mEdgeGlowTop.getMaxHeight() + top);
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

    protected boolean handleScrollBarDragging(MotionEvent event) {
        return false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean z = true;
        if (isEnabled()) {
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }
            if (this.mIsDetaching || (isAttachedToWindow() ^ 1) != 0) {
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
            int x;
            int y;
            int motionPosition;
            switch (actionMasked) {
                case 0:
                    onTouchDown(ev);
                    break;
                case 1:
                    onTouchUp(ev);
                    break;
                case 2:
                    onTouchMove(ev, vtev);
                    break;
                case 3:
                    onTouchCancel();
                    break;
                case 5:
                    int index = ev.getActionIndex();
                    int id = ev.getPointerId(index);
                    x = (int) ev.getX(index);
                    y = (int) ev.getY(index);
                    this.mMotionCorrection = 0;
                    this.mActivePointerId = id;
                    this.mMotionX = x;
                    this.mMotionY = y;
                    motionPosition = pointToPosition(x, y);
                    if (motionPosition >= 0) {
                        this.mMotionViewOriginalTop = getChildAt(motionPosition - this.mFirstPosition).getTop();
                        dismissCurrentPressed();
                        this.mMotionPosition = motionPosition;
                    }
                    this.mLastY = y;
                    break;
                case 6:
                    onSecondaryPointerUp(ev);
                    x = this.mMotionX;
                    y = this.mMotionY;
                    motionPosition = pointToPosition(x, y);
                    if (motionPosition >= 0) {
                        this.mMotionViewOriginalTop = getChildAt(motionPosition - this.mFirstPosition).getTop();
                        this.mMotionPosition = motionPosition;
                    }
                    this.mLastY = y;
                    break;
            }
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.addMovement(vtev);
            }
            vtev.recycle();
            return true;
        }
        if (!isClickable()) {
            z = isLongClickable();
        }
        return z;
    }

    private void onTouchDown(MotionEvent ev) {
        this.mHasPerformedLongPress = false;
        this.mActivePointerId = ev.getPointerId(0);
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
                        this.mPendingCheckForTap = new CheckForTap(this, null);
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
            switch (this.mTouchMode) {
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
                        }
                        setPressed(false);
                        if (motionView != null) {
                            motionView.setPressed(false);
                        }
                        removeCallbacks(this.mTouchMode == 0 ? this.mPendingCheckForTap : this.mPendingCheckForLongPress);
                        this.mTouchMode = 2;
                        updateSelectorState();
                        break;
                    }
                    break;
                case 3:
                case 5:
                    scrollIfNeeded((int) ev.getX(pointerIndex), y, vtev);
                    break;
            }
            onMultiSelectMove(ev, pointerIndex);
        }
    }

    protected void onTouchUp(MotionEvent ev) {
        VelocityTracker velocityTracker;
        int initialVelocity;
        switch (this.mTouchMode) {
            case 0:
            case 1:
            case 2:
                int motionPosition = this.mMotionPosition;
                final View child = getChildAt(motionPosition - this.mFirstPosition);
                if (child != null) {
                    if (this.mTouchMode != 0) {
                        child.setPressed(false);
                    }
                    float x = ev.getX();
                    boolean inList = x > ((float) this.mListPadding.left) && x < ((float) (getWidth() - this.mListPadding.right));
                    if (inList && (child.hasExplicitFocusable() ^ 1) != 0) {
                        if (this.mPerformClick == null) {
                            this.mPerformClick = new PerformClick(this, null);
                        }
                        final PerformClick performClick = this.mPerformClick;
                        performClick.mClickMotionPosition = motionPosition;
                        performClick.rememberWindowAttachCount();
                        this.mResurrectToPosition = motionPosition;
                        if (this.mTouchMode == 0 || this.mTouchMode == 1) {
                            CheckForTap checkForTap;
                            if (this.mTouchMode == 0) {
                                checkForTap = this.mPendingCheckForTap;
                            } else {
                                Object checkForTap2 = this.mPendingCheckForLongPress;
                            }
                            removeCallbacks(checkForTap2);
                            this.mLayoutMode = 0;
                            if (this.mDataChanged || !this.mAdapter.isEnabled(motionPosition)) {
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
                                        AbsListView.this.mTouchModeReset = null;
                                        AbsListView.this.mTouchMode = -1;
                                        child.setPressed(false);
                                        AbsListView.this.setPressed(false);
                                        if (!AbsListView.this.mDataChanged && (AbsListView.this.mIsDetaching ^ 1) != 0 && AbsListView.this.isAttachedToWindow()) {
                                            performClick.run();
                                        }
                                    }
                                };
                                postDelayed(this.mTouchModeReset, (long) getPressedStateDuration());
                            }
                            return;
                        } else if (!this.mDataChanged && this.mAdapter.isEnabled(motionPosition)) {
                            performClick.run();
                        }
                    }
                }
                this.mTouchMode = -1;
                updateSelectorState();
                break;
            case 3:
                int childCount = getChildCount();
                if (childCount <= 0) {
                    this.mTouchMode = -1;
                    reportScrollStateChange(0);
                    break;
                }
                int firstChildTop = getChildAt(0).getTop();
                int lastChildBottom = getChildAt(childCount - 1).getBottom();
                int contentTop = this.mListPadding.top;
                int contentBottom = getHeight() - this.mListPadding.bottom;
                if (this.mFirstPosition == 0 && firstChildTop >= contentTop && this.mFirstPosition + childCount < this.mItemCount && lastChildBottom <= getHeight() - contentBottom) {
                    this.mTouchMode = -1;
                    reportScrollStateChange(0);
                    break;
                }
                velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                initialVelocity = (int) (velocityTracker.getYVelocity(this.mActivePointerId) * this.mVelocityScale);
                boolean flingVelocity = Math.abs(initialVelocity) > this.mMinimumVelocity;
                if (flingVelocity && ((this.mFirstPosition != 0 || firstChildTop != contentTop - this.mOverscrollDistance) && (this.mFirstPosition + childCount != this.mItemCount || lastChildBottom != this.mOverscrollDistance + contentBottom))) {
                    if (!dispatchNestedPreFling(0.0f, (float) (-initialVelocity))) {
                        if (this.mFlingRunnable == null) {
                            this.mFlingRunnable = new FlingRunnable();
                        }
                        reportScrollStateChange(2);
                        if (this.mIHwWechatOptimize.isWechatOptimizeEffect()) {
                            if (Jlog.isBetaUser()) {
                                Jlog.d(312, "ListViewSpeed", Math.abs(initialVelocity) / 2, LogException.NO_VALUE);
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
                    this.mTouchMode = -1;
                    reportScrollStateChange(0);
                    break;
                }
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
                if (flingVelocity) {
                    if ((dispatchNestedPreFling(0.0f, (float) (-initialVelocity)) ^ 1) != 0) {
                        dispatchNestedFling(0.0f, (float) (-initialVelocity), false);
                        break;
                    }
                }
                break;
            case 5:
                if (this.mFlingRunnable == null) {
                    this.mFlingRunnable = new FlingRunnable();
                }
                velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                initialVelocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                reportScrollStateChange(2);
                if (!hasSpringAnimatorMask()) {
                    if (Math.abs(initialVelocity) <= this.mMinimumVelocity) {
                        this.mFlingRunnable.startSpringback();
                        break;
                    } else {
                        this.mFlingRunnable.startOverfling(-initialVelocity);
                        break;
                    }
                }
                this.mFlingRunnable.startSpringback();
                break;
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

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (this.mScrollY != scrollY) {
            onScrollChanged(this.mScrollX, scrollY, this.mScrollX, this.mScrollY);
            this.mScrollY = scrollY;
            invalidateParentIfNeeded();
            awakenScrollBars();
        }
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 8:
                float axisValue;
                if (event.isFromSource(2)) {
                    axisValue = event.getAxisValue(9);
                } else if (event.isFromSource(4194304)) {
                    axisValue = event.getAxisValue(26);
                } else {
                    axisValue = 0.0f;
                }
                int delta = Math.round(this.mVerticalScrollFactor * axisValue);
                if (!(delta == 0 || trackMotionScroll(delta, delta))) {
                    return true;
                }
            case 11:
                if (event.isFromSource(2)) {
                    int actionButton = event.getActionButton();
                    if ((actionButton == 32 || actionButton == 2) && ((this.mTouchMode == 0 || this.mTouchMode == 1) && performStylusButtonPressAction(event))) {
                        removeCallbacks(this.mPendingCheckForLongPress);
                        removeCallbacks(this.mPendingCheckForTap);
                        break;
                    }
                }
                break;
        }
        return super.-wrap8(event);
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
        View motionView = getChildAt(getChildCount() / 2);
        int oldTop = motionView != null ? motionView.getTop() : 0;
        if (motionView == null || trackMotionScroll(-dyUnconsumed, -dyUnconsumed)) {
            int myUnconsumed = dyUnconsumed;
            int myConsumed = 0;
            if (motionView != null) {
                myConsumed = motionView.getTop() - oldTop;
                myUnconsumed = dyUnconsumed - myConsumed;
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
        int i = 0;
        super.draw(canvas);
        if (this.mEdgeGlowTop != null) {
            int width;
            int height;
            int translateX;
            int translateY;
            int restoreCount;
            int scrollY = this.mScrollY;
            boolean clipToPadding = getClipToPadding();
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
                restoreCount = canvas.save();
                canvas.clipRect(translateX, translateY, translateX + width, this.mEdgeGlowTop.getMaxHeight() + translateY);
                canvas.translate((float) translateX, (float) (Math.min(0, this.mFirstPositionDistanceGuess + scrollY) + translateY));
                this.mEdgeGlowTop.setSize(width, height);
                if (this.mEdgeGlowTop.draw(canvas)) {
                    invalidateTopGlow();
                }
                canvas.restoreToCount(restoreCount);
            }
            if (!this.mEdgeGlowBottom.isFinished()) {
                restoreCount = canvas.save();
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
                canvas.restoreToCount(restoreCount);
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
        if (this.mIsDetaching || (isAttachedToWindow() ^ 1) != 0) {
            return false;
        }
        if (this.mFastScroll != null && this.mFastScroll.onInterceptTouchEvent(ev)) {
            return true;
        }
        int y;
        switch (actionMasked) {
            case 0:
                int touchMode = this.mTouchMode;
                if (touchMode == 6 || touchMode == 5) {
                    this.mMotionCorrection = 0;
                    return true;
                }
                int x = (int) ev.getX();
                y = (int) ev.getY();
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
                switch (this.mTouchMode) {
                    case 0:
                        int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                        if (pointerIndex == -1) {
                            pointerIndex = 0;
                            this.mActivePointerId = ev.getPointerId(0);
                        }
                        y = (int) ev.getY(pointerIndex);
                        initVelocityTrackerIfNotExists();
                        this.mVelocityTracker.addMovement(ev);
                        if (startScrollIfNeeded((int) ev.getX(pointerIndex), y, null)) {
                            return true;
                        }
                        break;
                }
                break;
            case 6:
                onSecondaryPointerUp(ev);
                break;
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

    void reportScrollStateChange(int newState) {
        if (newState != this.mLastScrollState && this.mOnScrollListener != null) {
            this.mLastScrollState = newState;
            this.mOnScrollListener.onScrollStateChanged(this, newState);
        }
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

    AbsPositionScroller createPositionScroller() {
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

    void smoothScrollBy(int distance, int duration, boolean linear, boolean suppressEndFlingStateChangeCall) {
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

    void smoothScrollByOffset(int position) {
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
        if (this.mScrollingCacheEnabled && (this.mCachingStarted ^ 1) != 0 && (isHardwareAccelerated() ^ 1) != 0) {
            -wrap8(true);
            -wrap7(true);
            this.mCachingActive = true;
            this.mCachingStarted = true;
        }
    }

    private void clearScrollingCache() {
        if (!isHardwareAccelerated()) {
            if (this.mClearScrollingCache == null) {
                this.mClearScrollingCache = new Runnable() {
                    public void run() {
                        if (AbsListView.this.mCachingStarted) {
                            AbsListView absListView = AbsListView.this;
                            AbsListView.this.mCachingActive = false;
                            absListView.mCachingStarted = false;
                            AbsListView.this.-wrap8(false);
                            if ((AbsListView.this.mPersistentDrawingCache & 2) == 0) {
                                AbsListView.this.-wrap7(false);
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
        boolean z = true;
        int childCount = getChildCount();
        if (childCount == 0) {
            return false;
        }
        int firstPosition = this.mFirstPosition;
        Rect listPadding = this.mListPadding;
        if (direction > 0) {
            int lastBottom = getChildAt(childCount - 1).getBottom();
            if (firstPosition + childCount >= this.mItemCount && lastBottom <= getHeight() - listPadding.bottom) {
                z = false;
            }
            return z;
        }
        int firstTop = getChildAt(0).getTop();
        if (firstPosition <= 0 && firstTop >= listPadding.top) {
            z = false;
        }
        return z;
    }

    boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return true;
        }
        int firstTop = getChildAt(0).getTop();
        int lastBottom = getChildAt(childCount - 1).getBottom();
        Rect listPadding = this.mListPadding;
        int effectivePaddingTop = 0;
        int effectivePaddingBottom = 0;
        if ((this.mGroupFlags & 34) == 34) {
            effectivePaddingTop = listPadding.top;
            effectivePaddingBottom = listPadding.bottom;
        }
        int spaceAbove = effectivePaddingTop - firstTop;
        int spaceBelow = lastBottom - (getHeight() - effectivePaddingBottom);
        int height = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
        if (deltaY < 0) {
            deltaY = Math.max(-(height - 1), deltaY);
        } else {
            deltaY = Math.min(height - 1, deltaY);
        }
        if (incrementalDeltaY < 0) {
            incrementalDeltaY = Math.max(-(height - 1), incrementalDeltaY);
        } else {
            incrementalDeltaY = Math.min(height - 1, incrementalDeltaY);
        }
        int firstPosition = this.mFirstPosition;
        if (firstPosition == 0) {
            this.mFirstPositionDistanceGuess = firstTop - listPadding.top;
        } else {
            this.mFirstPositionDistanceGuess += incrementalDeltaY;
        }
        if (firstPosition + childCount == this.mItemCount) {
            this.mLastPositionDistanceGuess = listPadding.bottom + lastBottom;
        } else {
            this.mLastPositionDistanceGuess += incrementalDeltaY;
        }
        boolean cannotScrollDown = (firstPosition != 0 || firstTop < listPadding.top) ? false : incrementalDeltaY >= 0;
        boolean cannotScrollUp = (firstPosition + childCount != this.mItemCount || lastBottom > getHeight() - listPadding.bottom) ? false : incrementalDeltaY <= 0;
        if (cannotScrollDown || cannotScrollUp) {
            boolean z;
            if (incrementalDeltaY != 0) {
                z = true;
            } else {
                z = false;
            }
            return z;
        }
        boolean down = incrementalDeltaY < 0;
        boolean inTouchMode = isInTouchMode();
        if (inTouchMode) {
            hideSelector();
        }
        int headerViewsCount = getHeaderViewsCount();
        int footerViewsStart = this.mItemCount - getFooterViewsCount();
        int start = 0;
        int count = 0;
        int i;
        View child;
        int position;
        if (!down) {
            int bottom = getHeight() - incrementalDeltaY;
            if ((this.mGroupFlags & 34) == 34) {
                bottom -= listPadding.bottom;
            }
            for (i = childCount - 1; i >= 0; i--) {
                child = getChildAt(i);
                if (child.getTop() <= bottom) {
                    break;
                }
                start = i;
                count++;
                position = firstPosition + i;
                if (position >= headerViewsCount && position < footerViewsStart) {
                    child.clearAccessibilityFocus();
                    this.mRecycler.addScrapView(child, position);
                }
            }
        } else {
            int top = -incrementalDeltaY;
            if ((this.mGroupFlags & 34) == 34) {
                top += listPadding.top;
            }
            for (i = 0; i < childCount; i++) {
                child = getChildAt(i);
                if (child.getBottom() >= top) {
                    break;
                }
                count++;
                position = firstPosition + i;
                if (position >= headerViewsCount && position < footerViewsStart) {
                    child.clearAccessibilityFocus();
                    this.mRecycler.addScrapView(child, position);
                }
            }
        }
        this.mMotionViewNewTop = this.mMotionViewOriginalTop + deltaY;
        this.mBlockLayoutRequests = true;
        if (count > 0) {
            detachViewsFromParent(start, count);
            this.mRecycler.removeSkippedScrap();
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
        offsetChildrenTopAndBottom(incrementalDeltaY);
        if (down) {
            this.mFirstPosition += count;
        }
        int absIncrementalDeltaY = Math.abs(incrementalDeltaY);
        if (spaceAbove < absIncrementalDeltaY || spaceBelow < absIncrementalDeltaY) {
            fillGap(down);
        }
        this.mRecycler.fullyDetachScrapViews();
        int childIndex;
        if (!inTouchMode && this.mSelectedPosition != -1) {
            childIndex = this.mSelectedPosition - this.mFirstPosition;
            if (childIndex >= 0 && childIndex < getChildCount()) {
                positionSelector(this.mSelectedPosition, getChildAt(childIndex));
            }
        } else if (this.mSelectorPosition != -1) {
            childIndex = this.mSelectorPosition - this.mFirstPosition;
            if (childIndex >= 0 && childIndex < getChildCount()) {
                positionSelector(-1, getChildAt(childIndex));
            }
        } else {
            this.mSelectorRect.setEmpty();
        }
        this.mBlockLayoutRequests = false;
        invokeOnItemScrollListener();
        return false;
    }

    int getHeaderViewsCount() {
        return 0;
    }

    int getFooterViewsCount() {
        return 0;
    }

    void hideSelector() {
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

    int reconcileSelectedPosition() {
        int position = this.mSelectedPosition;
        if (position < 0) {
            position = this.mResurrectToPosition;
        }
        return Math.min(Math.max(0, position), this.mItemCount - 1);
    }

    int findClosestMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return -1;
        }
        int motionRow = findMotionRow(y);
        if (motionRow == -1) {
            motionRow = (this.mFirstPosition + childCount) - 1;
        }
        return motionRow;
    }

    public void invalidateViews() {
        this.mDataChanged = true;
        rememberSyncState();
        requestLayout();
        invalidate();
    }

    boolean resurrectSelectionIfNeeded() {
        if (this.mSelectedPosition >= 0 || !resurrectSelection()) {
            return false;
        }
        updateSelectorState();
        return true;
    }

    boolean resurrectSelection() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return false;
        }
        int selectedPos;
        boolean z;
        int selectedTop = 0;
        int childrenTop = this.mListPadding.top;
        int childrenBottom = (this.mBottom - this.mTop) - this.mListPadding.bottom;
        int firstPosition = this.mFirstPosition;
        int toPosition = this.mResurrectToPosition;
        boolean down = true;
        int i;
        int top;
        if (toPosition >= firstPosition && toPosition < firstPosition + childCount) {
            selectedPos = toPosition;
            View selected = getChildAt(toPosition - this.mFirstPosition);
            selectedTop = selected.getTop();
            int selectedBottom = selected.getBottom();
            if (selectedTop < childrenTop) {
                selectedTop = childrenTop + getVerticalFadingEdgeLength();
            } else if (selectedBottom > childrenBottom) {
                selectedTop = (childrenBottom - selected.getMeasuredHeight()) - getVerticalFadingEdgeLength();
            }
        } else if (toPosition < firstPosition) {
            selectedPos = firstPosition;
            for (i = 0; i < childCount; i++) {
                top = getChildAt(i).getTop();
                if (i == 0) {
                    selectedTop = top;
                    if (firstPosition > 0 || top < childrenTop) {
                        childrenTop += getVerticalFadingEdgeLength();
                    }
                }
                if (top >= childrenTop) {
                    selectedPos = firstPosition + i;
                    selectedTop = top;
                    break;
                }
            }
        } else {
            int itemCount = this.mItemCount;
            down = false;
            selectedPos = (firstPosition + childCount) - 1;
            for (i = childCount - 1; i >= 0; i--) {
                View v = getChildAt(i);
                top = v.getTop();
                int bottom = v.getBottom();
                if (i == childCount - 1) {
                    selectedTop = top;
                    if (firstPosition + childCount < itemCount || bottom > childrenBottom) {
                        childrenBottom -= getVerticalFadingEdgeLength();
                    }
                }
                if (bottom <= childrenBottom) {
                    selectedPos = firstPosition + i;
                    selectedTop = top;
                    break;
                }
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
        selectedPos = lookForSelectablePosition(selectedPos, down);
        if (selectedPos < firstPosition || selectedPos > getLastVisiblePosition()) {
            selectedPos = -1;
        } else {
            this.mLayoutMode = 4;
            updateSelectorState();
            setSelectionInt(selectedPos);
            invokeOnItemScrollListener();
        }
        reportScrollStateChange(0);
        if (selectedPos >= 0) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    void confirmCheckedPositionsById() {
        this.mCheckStates.clear();
        boolean checkedCountChanged = false;
        int checkedIndex = 0;
        while (checkedIndex < this.mCheckedIdStates.size()) {
            long id = this.mCheckedIdStates.keyAt(checkedIndex);
            int lastPos = ((Integer) this.mCheckedIdStates.valueAt(checkedIndex)).intValue();
            if (id != this.mAdapter.getItemId(lastPos)) {
                int start = Math.max(0, lastPos - 20);
                int end = Math.min(lastPos + 20, this.mItemCount);
                boolean found = false;
                for (int searchPos = start; searchPos < end; searchPos++) {
                    if (id == this.mAdapter.getItemId(searchPos)) {
                        found = true;
                        this.mCheckStates.put(searchPos, true);
                        this.mCheckedIdStates.setValueAt(checkedIndex, Integer.valueOf(searchPos));
                        break;
                    }
                }
                if (!found) {
                    this.mCheckedIdStates.delete(id);
                    checkedIndex--;
                    this.mCheckedItemCount--;
                    checkedCountChanged = true;
                    if (!(this.mChoiceActionMode == null || this.mMultiChoiceModeCallback == null)) {
                        this.mMultiChoiceModeCallback.onItemCheckedStateChanged(this.mChoiceActionMode, lastPos, id, false);
                    }
                }
            } else {
                this.mCheckStates.put(lastPos, true);
            }
            checkedIndex++;
        }
        if (checkedCountChanged && this.mChoiceActionMode != null) {
            this.mChoiceActionMode.invalidate();
        }
    }

    protected void handleDataChanged() {
        int i = 3;
        int count = this.mItemCount;
        int lastHandledItemCount = this.mLastHandledItemCount;
        this.mLastHandledItemCount = this.mItemCount;
        if (!(this.mChoiceMode == 0 || this.mAdapter == null || !this.mAdapter.hasStableIds())) {
            confirmCheckedPositionsById();
        }
        this.mRecycler.clearTransientStateViews();
        if (count > 0) {
            int newPos;
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
                        newPos = findSyncPosition();
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
            if (!isInTouchMode()) {
                newPos = getSelectedItemPosition();
                if (newPos >= count) {
                    newPos = count - 1;
                }
                if (newPos < 0) {
                    newPos = 0;
                }
                int selectablePos = lookForSelectablePosition(newPos, true);
                if (selectablePos >= 0) {
                    setNextSelectedPositionInt(selectablePos);
                    return;
                }
                selectablePos = lookForSelectablePosition(newPos, false);
                if (selectablePos >= 0) {
                    setNextSelectedPositionInt(selectablePos);
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

    protected void onDisplayHint(int hint) {
        super.onDisplayHint(hint);
        switch (hint) {
            case 0:
                if (!(!this.mFiltered || this.mPopup == null || (this.mPopup.isShowing() ^ 1) == 0)) {
                    showPopup();
                    break;
                }
            case 4:
                if (this.mPopup != null && this.mPopup.isShowing()) {
                    dismissPopup();
                    break;
                }
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
        if (this.mPopup.isShowing()) {
            this.mPopup.update(xy[0], bottomGap, -1, -1);
        } else {
            this.mPopup.showAtLocation((View) this, 81, xy[0], bottomGap);
        }
    }

    static int getDistance(Rect source, Rect dest, int direction) {
        int sX;
        int sY;
        int dX;
        int dY;
        switch (direction) {
            case 1:
            case 2:
                sX = source.right + (source.width() / 2);
                sY = source.top + (source.height() / 2);
                dX = dest.left + (dest.width() / 2);
                dY = dest.top + (dest.height() / 2);
                break;
            case 17:
                sX = source.left;
                sY = source.top + (source.height() / 2);
                dX = dest.right;
                dY = dest.top + (dest.height() / 2);
                break;
            case 33:
                sX = source.left + (source.width() / 2);
                sY = source.top;
                dX = dest.left + (dest.width() / 2);
                dY = dest.bottom;
                break;
            case 66:
                sX = source.right;
                sY = source.top + (source.height() / 2);
                dX = dest.left;
                dY = dest.top + (dest.height() / 2);
                break;
            case 130:
                sX = source.left + (source.width() / 2);
                sY = source.bottom;
                dX = dest.left + (dest.width() / 2);
                dY = dest.top;
                break;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
        }
        int deltaX = dX - sX;
        int deltaY = dY - sY;
        return (deltaY * deltaY) + (deltaX * deltaX);
    }

    protected boolean isInFilterMode() {
        return this.mFiltered;
    }

    boolean sendToTextFilter(int keyCode, int count, KeyEvent event) {
        if (!acceptFilter()) {
            return false;
        }
        boolean handled = false;
        boolean okToSend = true;
        switch (keyCode) {
            case 4:
                if (this.mFiltered && this.mPopup != null && this.mPopup.isShowing()) {
                    if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                        DispatcherState state = getKeyDispatcherState();
                        if (state != null) {
                            state.startTracking(event, this);
                        }
                        handled = true;
                    } else if (event.getAction() == 1 && event.isTracking() && (event.isCanceled() ^ 1) != 0) {
                        handled = true;
                        this.mTextFilter.setText(LogException.NO_VALUE);
                    }
                }
                okToSend = false;
                break;
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 66:
                okToSend = false;
                break;
            case 62:
                okToSend = this.mFiltered;
                break;
        }
        if (okToSend) {
            createTextFilter(true);
            KeyEvent forwardEvent = event;
            if (event.getRepeatCount() > 0) {
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
            this.mPopup.setAnimationStyle(R.style.Animation_TypingFilter);
        } else {
            this.mPopup.setAnimationStyle(R.style.Animation_TypingFilterRestore);
        }
    }

    private EditText getTextFilterInput() {
        if (this.mTextFilter == null) {
            this.mTextFilter = (EditText) LayoutInflater.from(getContext()).inflate((int) R.layout.typing_filter, null);
            this.mTextFilter.setRawInputType(177);
            this.mTextFilter.setImeOptions(268435456);
            this.mTextFilter.addTextChangedListener(this);
        }
        return this.mTextFilter;
    }

    public void clearTextFilter() {
        if (this.mFiltered) {
            getTextFilterInput().setText(LogException.NO_VALUE);
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
            if (this.mFiltered && this.mPopup != null && (this.mPopup.isShowing() ^ 1) != 0 && (this.mPopupHidden ^ 1) != 0) {
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

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -2, 0);
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
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

    @ExportedProperty(category = "drawing")
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
        return new AsyncRemoteAdapterAction(this, intent);
    }

    public void setRemoteViewsAdapter(Intent intent, boolean isAsync) {
        if (this.mRemoteAdapter == null || !new FilterComparison(intent).equals(new FilterComparison(this.mRemoteAdapter.getRemoteViewsServiceIntent()))) {
            this.mDeferNotifyDataSetChanged = false;
            this.mRemoteAdapter = new RemoteViewsAdapter(getContext(), intent, this, isAsync);
            if (this.mRemoteAdapter.isDataReady()) {
                setAdapter(this.mRemoteAdapter);
            }
        }
    }

    public void setRemoteViewsOnClickHandler(OnClickHandler handler) {
        if (this.mRemoteAdapter != null) {
            this.mRemoteAdapter.setRemoteViewsOnClickHandler(handler);
        }
    }

    public void deferNotifyDataSetChanged() {
        this.mDeferNotifyDataSetChanged = true;
    }

    public boolean onRemoteAdapterConnected() {
        if (this.mRemoteAdapter != this.mAdapter) {
            setAdapter(this.mRemoteAdapter);
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

    void setVisibleRangeHint(int start, int end) {
        if (this.mRemoteAdapter != null) {
            this.mRemoteAdapter.setVisibleRangeHint(start, end);
        }
    }

    public void setRecyclerListener(RecyclerListener listener) {
        this.mRecycler.mRecyclerListener = listener;
    }

    int getHeightForPosition(int position) {
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
            if (isInTouchMode()) {
                this.mResurrectToPosition = position;
            } else {
                position = lookForSelectablePosition(position, true);
                if (position >= 0) {
                    setNextSelectedPositionInt(position);
                }
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

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
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

    protected void setEdgeGlowTopBottom(EdgeEffect edgeGlowTop, EdgeEffect edgeGlowBottom) {
        this.mEdgeGlowTop = edgeGlowTop;
        this.mEdgeGlowBottom = edgeGlowBottom;
    }

    protected Object getScrollerInner() {
        return this.mFastScroll;
    }

    protected void setScrollerInner(FastScroller scroller) {
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

    protected void setIgnoreScrollMultiSelectStub() {
    }

    protected void enableScrollMultiSelectStub() {
    }

    protected boolean getCheckedStateForMultiSelect(boolean curState) {
        return curState;
    }

    protected void onMultiSelectMove(MotionEvent ev, int pointerIndex) {
    }

    protected void enterMultiSelectModeIfNeeded(int motionPosition, int x) {
    }

    protected void dismissCurrentPressed() {
    }

    protected void setStableItemHeight(OverScroller scroller, FlingRunnable fr) {
    }

    protected int adjustFlingDistance(int delta) {
        if (delta > 0) {
            return Math.min(((getHeight() - this.mPaddingBottom) - this.mPaddingTop) - 1, delta);
        }
        return Math.max(-(((getHeight() - this.mPaddingBottom) - this.mPaddingTop) - 1), delta);
    }

    protected boolean hasScrollMultiSelectMask() {
        return false;
    }

    protected boolean hasSpringAnimatorMask() {
        return false;
    }

    protected boolean hasHighSpeedStableMask() {
        return true;
    }

    protected int getPressedStateDuration() {
        return ViewConfiguration.getPressedStateDuration();
    }

    protected void adjustSelector(int pos, Rect rect) {
    }
}
