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
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Filter.FilterListener;
import android.widget.RemoteViews.OnClickHandler;
import android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Protocol;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

public abstract class AbsListView extends AdapterView<ListAdapter> implements TextWatcher, OnGlobalLayoutListener, FilterListener, OnTouchModeChangeListener, RemoteAdapterConnectionCallback {
    private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;
    public static final int CHOICE_MODE_MULTIPLE = 2;
    public static final int CHOICE_MODE_MULTIPLE_MODAL = 3;
    public static final int CHOICE_MODE_NONE = 0;
    public static final int CHOICE_MODE_SINGLE = 1;
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
    static final Interpolator sLinearInterpolator = null;
    private ListItemAccessibilityDelegate mAccessibilityDelegate;
    private int mActivePointerId;
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
    int mWidthMeasureSpec;

    /* renamed from: android.widget.AbsListView.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ boolean val$enabled;

        AnonymousClass1(boolean val$enabled) {
            this.val$enabled = val$enabled;
        }

        public void run() {
            AbsListView.this.setFastScrollerEnabledUiThread(this.val$enabled);
        }
    }

    /* renamed from: android.widget.AbsListView.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ boolean val$alwaysShow;

        AnonymousClass2(boolean val$alwaysShow) {
            this.val$alwaysShow = val$alwaysShow;
        }

        public void run() {
            AbsListView.this.setFastScrollerAlwaysVisibleUiThread(this.val$alwaysShow);
        }
    }

    /* renamed from: android.widget.AbsListView.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ View val$child;
        final /* synthetic */ PerformClick val$performClick;

        AnonymousClass3(View val$child, PerformClick val$performClick) {
            this.val$child = val$child;
            this.val$performClick = val$performClick;
        }

        public void run() {
            AbsListView.this.mTouchModeReset = null;
            AbsListView.this.mTouchMode = AbsListView.TOUCH_MODE_UNKNOWN;
            this.val$child.setPressed(AbsListView.PROFILE_SCROLLING);
            AbsListView.this.setPressed(AbsListView.PROFILE_SCROLLING);
            if (!AbsListView.this.mDataChanged && !AbsListView.this.mIsDetaching && AbsListView.this.isAttachedToWindow()) {
                this.val$performClick.run();
            }
        }
    }

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

        private WindowRunnnable() {
        }

        public void rememberWindowAttachCount() {
            this.mOriginalAttachCount = AbsListView.this.getWindowAttachCount();
        }

        public boolean sameWindow() {
            return AbsListView.this.getWindowAttachCount() == this.mOriginalAttachCount ? true : AbsListView.PROFILE_SCROLLING;
        }
    }

    private class CheckForKeyLongPress extends WindowRunnnable implements Runnable {
        private CheckForKeyLongPress() {
            super(null);
        }

        public void run() {
            if (AbsListView.this.isPressed() && AbsListView.this.mSelectedPosition >= 0) {
                View v = AbsListView.this.getChildAt(AbsListView.this.mSelectedPosition - AbsListView.this.mFirstPosition);
                if (AbsListView.this.mDataChanged) {
                    AbsListView.this.setPressed(AbsListView.PROFILE_SCROLLING);
                    if (v != null) {
                        v.setPressed(AbsListView.PROFILE_SCROLLING);
                        return;
                    }
                    return;
                }
                boolean handled = AbsListView.PROFILE_SCROLLING;
                if (sameWindow()) {
                    handled = AbsListView.this.performLongPress(v, AbsListView.this.mSelectedPosition, AbsListView.this.mSelectedRowId);
                }
                if (handled) {
                    AbsListView.this.setPressed(AbsListView.PROFILE_SCROLLING);
                    v.setPressed(AbsListView.PROFILE_SCROLLING);
                }
            }
        }
    }

    private class CheckForLongPress extends WindowRunnnable implements Runnable {
        private static final int INVALID_COORD = -1;
        private float mX;
        private float mY;

        private CheckForLongPress() {
            super(null);
            this.mX = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            this.mY = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
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
                boolean handled = AbsListView.PROFILE_SCROLLING;
                if (sameWindow() && !AbsListView.this.mDataChanged) {
                    handled = (this.mX == android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE || this.mY == android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) ? AbsListView.this.performLongPress(child, longPressPosition, longPressId) : AbsListView.this.performLongPress(child, longPressPosition, longPressId, this.mX, this.mY);
                }
                if (handled) {
                    AbsListView.this.mHasPerformedLongPress = true;
                    AbsListView.this.mTouchMode = INVALID_COORD;
                    AbsListView.this.setPressed(AbsListView.PROFILE_SCROLLING);
                    child.setPressed(AbsListView.PROFILE_SCROLLING);
                    return;
                }
                AbsListView.this.mTouchMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL;
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
                AbsListView.this.mTouchMode = AbsListView.TRANSCRIPT_MODE_NORMAL;
                View child = AbsListView.this.getChildAt(AbsListView.this.mMotionPosition - AbsListView.this.mFirstPosition);
                if (child != null && !child.hasFocusable()) {
                    AbsListView.this.mLayoutMode = AbsListView.TRANSCRIPT_MODE_DISABLED;
                    if (AbsListView.this.mDataChanged) {
                        AbsListView.this.mTouchMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL;
                        return;
                    }
                    float[] point = AbsListView.this.mTmpPoint;
                    point[AbsListView.TRANSCRIPT_MODE_DISABLED] = this.x;
                    point[AbsListView.TRANSCRIPT_MODE_NORMAL] = this.y;
                    AbsListView.this.transformPointToViewLocal(point, child);
                    child.drawableHotspotChanged(point[AbsListView.TRANSCRIPT_MODE_DISABLED], point[AbsListView.TRANSCRIPT_MODE_NORMAL]);
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
                            AbsListView.this.mPendingCheckForLongPress = new CheckForLongPress(null);
                        }
                        AbsListView.this.mPendingCheckForLongPress.setCoords(this.x, this.y);
                        AbsListView.this.mPendingCheckForLongPress.rememberWindowAttachCount();
                        AbsListView.this.postDelayed(AbsListView.this.mPendingCheckForLongPress, (long) longPressTimeout);
                        return;
                    }
                    AbsListView.this.mTouchMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL;
                }
            }
        }
    }

    public class FlingRunnable implements Runnable {
        private static final int FLYWHEEL_TIMEOUT = 40;
        private final Runnable mCheckFlywheel;
        private int mLastFlingY;
        private float mLastSDRRatio;
        private final OverScroller mScroller;

        FlingRunnable() {
            this.mLastSDRRatio = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            this.mCheckFlywheel = new Runnable() {
                public void run() {
                    int activeId = AbsListView.this.mActivePointerId;
                    VelocityTracker vt = AbsListView.this.mVelocityTracker;
                    OverScroller scroller = FlingRunnable.this.mScroller;
                    if (vt != null && activeId != AbsListView.TOUCH_MODE_UNKNOWN) {
                        vt.computeCurrentVelocity(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, (float) AbsListView.this.mMaximumVelocity);
                        float yvel = -vt.getYVelocity(activeId);
                        if (Math.abs(yvel) < ((float) AbsListView.this.mMinimumVelocity) || !scroller.isScrollingInDirection(0.0f, yvel)) {
                            if (AbsListView.this.mTouchMode == AbsListView.TOUCH_MODE_OVERFLING) {
                                FlingRunnable.this.endFling();
                                AbsListView.this.mTouchMode = AbsListView.TOUCH_MODE_OVERSCROLL;
                            } else {
                                FlingRunnable.this.endFling();
                                AbsListView.this.mTouchMode = AbsListView.TOUCH_MODE_SCROLL;
                            }
                            AbsListView.this.reportScrollStateChange(AbsListView.TRANSCRIPT_MODE_NORMAL);
                        } else {
                            AbsListView.this.postDelayed(this, 40);
                        }
                    }
                }
            };
            this.mScroller = new OverScroller(AbsListView.this.getContext());
        }

        void start(int initialVelocity) {
            int initialY;
            if (initialVelocity < 0) {
                initialY = HwBootFail.STAGE_BOOT_SUCCESS;
            } else {
                initialY = AbsListView.TRANSCRIPT_MODE_DISABLED;
            }
            this.mLastFlingY = initialY;
            this.mScroller.setInterpolator(null);
            AbsListView.this.setStableItemHeight(this.mScroller, AbsListView.this.mFlingRunnable);
            this.mScroller.fling(AbsListView.TRANSCRIPT_MODE_DISABLED, initialY, AbsListView.TRANSCRIPT_MODE_DISABLED, initialVelocity, AbsListView.TRANSCRIPT_MODE_DISABLED, HwBootFail.STAGE_BOOT_SUCCESS, AbsListView.TRANSCRIPT_MODE_DISABLED, HwBootFail.STAGE_BOOT_SUCCESS);
            AbsListView.this.mTouchMode = AbsListView.TOUCH_MODE_FLING;
            AbsListView.this.postOnAnimation(this);
            if (AbsListView.this.mFlingStrictSpan == null) {
                AbsListView.this.mFlingStrictSpan = StrictMode.enterCriticalSpan("AbsListView-fling");
            }
        }

        void startSpringback() {
            if (this.mScroller.springBack(AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.this.mScrollY, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.TRANSCRIPT_MODE_DISABLED)) {
                AbsListView.this.mTouchMode = AbsListView.TOUCH_MODE_OVERFLING;
                AbsListView.this.invalidate();
                AbsListView.this.postOnAnimation(this);
                return;
            }
            AbsListView.this.mTouchMode = AbsListView.TOUCH_MODE_UNKNOWN;
            AbsListView.this.reportScrollStateChange(AbsListView.TRANSCRIPT_MODE_DISABLED);
        }

        void startOverfling(int initialVelocity) {
            this.mScroller.setInterpolator(null);
            this.mScroller.fling(AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.this.mScrollY, AbsListView.TRANSCRIPT_MODE_DISABLED, initialVelocity, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.TRANSCRIPT_MODE_DISABLED, RtlSpacingHelper.UNDEFINED, HwBootFail.STAGE_BOOT_SUCCESS, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.this.getHeight());
            AbsListView.this.mTouchMode = AbsListView.TOUCH_MODE_OVERFLING;
            AbsListView.this.invalidate();
            AbsListView.this.postOnAnimation(this);
        }

        void edgeReached(int delta) {
            ViewRootImpl viewRoot = AbsListView.this.getViewRootImpl();
            if (HwFrameworkFactory.getHwNsdImpl().checkIs2DSDRCase(AbsListView.this.getContext(), viewRoot)) {
                Log.i("2DSDR", "APS: 2DSDR: AbsListView.edgeReached, we will clear SDR ratio");
                viewRoot.setSDRRatio(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
                viewRoot.getView().invalidate();
                this.mLastSDRRatio = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            }
            if (AbsListView.this.hasSpringAnimatorMask()) {
                this.mScroller.notifyVerticalEdgeReached(AbsListView.this.mScrollY, AbsListView.TRANSCRIPT_MODE_DISABLED, (int) (((float) AbsListView.this.getHeight()) * 0.5f));
            } else {
                this.mScroller.notifyVerticalEdgeReached(AbsListView.this.mScrollY, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.this.mOverflingDistance);
            }
            int overscrollMode = AbsListView.this.getOverScrollMode();
            if (overscrollMode == 0 || (overscrollMode == AbsListView.TRANSCRIPT_MODE_NORMAL && !AbsListView.this.contentFits())) {
                AbsListView.this.mTouchMode = AbsListView.TOUCH_MODE_OVERFLING;
                int vel = (int) this.mScroller.getCurrVelocity();
                if (AbsListView.this.mEdgeGlowTop != null) {
                    if (delta > 0) {
                        AbsListView.this.mEdgeGlowTop.onAbsorb(vel);
                    } else {
                        AbsListView.this.mEdgeGlowBottom.onAbsorb(vel);
                    }
                }
            } else {
                AbsListView.this.mTouchMode = AbsListView.TOUCH_MODE_UNKNOWN;
                if (AbsListView.this.mPositionScroller != null) {
                    AbsListView.this.mPositionScroller.stop();
                }
            }
            AbsListView.this.invalidate();
            AbsListView.this.postOnAnimation(this);
        }

        void startScroll(int distance, int duration, boolean linear) {
            int initialY;
            if (distance < 0) {
                initialY = HwBootFail.STAGE_BOOT_SUCCESS;
            } else {
                initialY = AbsListView.TRANSCRIPT_MODE_DISABLED;
            }
            this.mLastFlingY = initialY;
            this.mScroller.setInterpolator(linear ? AbsListView.sLinearInterpolator : null);
            this.mScroller.startScroll(AbsListView.TRANSCRIPT_MODE_DISABLED, initialY, AbsListView.TRANSCRIPT_MODE_DISABLED, distance, duration);
            AbsListView.this.mTouchMode = AbsListView.TOUCH_MODE_FLING;
            AbsListView.this.postOnAnimation(this);
        }

        void endFling() {
            ViewRootImpl viewRoot = AbsListView.this.getViewRootImpl();
            if (HwFrameworkFactory.getHwNsdImpl().checkIs2DSDRCase(AbsListView.this.getContext(), viewRoot)) {
                Log.i("2DSDR", "APS: 2DSDR: AbsListView.FlingRunnable.endFling, we will clear SDR ratio");
                viewRoot.setSDRRatio(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
                this.mLastSDRRatio = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
                viewRoot.getView().invalidate();
            }
            AbsListView.this.mTouchMode = AbsListView.TOUCH_MODE_UNKNOWN;
            AbsListView.this.removeCallbacks(this);
            AbsListView.this.removeCallbacks(this.mCheckFlywheel);
            AbsListView.this.reportScrollStateChange(AbsListView.TRANSCRIPT_MODE_DISABLED);
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
            int visibleHeight = AbsListView.this.getChildAt(AbsListView.TRANSCRIPT_MODE_DISABLED).getHeight() + AbsListView.this.getChildAt(AbsListView.TRANSCRIPT_MODE_DISABLED).getTop();
            if ((visibleHeight + origDelta) % ratioBase != 0) {
                int i = AbsListView.TRANSCRIPT_MODE_DISABLED;
                while (i < ratioBase) {
                    int newDelta = origDelta > 0 ? origDelta + i : origDelta - i;
                    if ((visibleHeight + newDelta) % ratioBase == 0) {
                        return newDelta;
                    }
                    i += AbsListView.TRANSCRIPT_MODE_NORMAL;
                }
            }
            return origDelta;
        }

        int dealWith2DSDR(int delta, OverScroller scroller) {
            ViewRootImpl viewRoot = AbsListView.this.getViewRootImpl();
            if (!(!HwFrameworkFactory.getHwNsdImpl().checkIs2DSDRCase(AbsListView.this.getContext(), viewRoot) || viewRoot.getView() == null || AbsListView.this.getChildAt(AbsListView.TRANSCRIPT_MODE_DISABLED) == null)) {
                int ratioBase = HwFrameworkFactory.getHwNsdImpl().computeSDRRatioBase(AbsListView.this.getContext(), viewRoot.getView(), AbsListView.this);
                float ratioToSet = HwFrameworkFactory.getHwNsdImpl().computeSDRRatio(AbsListView.this.getContext(), viewRoot.getView(), AbsListView.this, (float) AbsListView.this.mMaximumVelocity, scroller.getCurrVelocity(), ratioBase);
                if (0.0f < ratioToSet && ratioToSet <= android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                    boolean isNearEdge = AbsListView.PROFILE_SCROLLING;
                    if (delta >= 0 || AbsListView.this.mItemCount - (AbsListView.this.mFirstPosition + AbsListView.this.getChildCount()) >= AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL) {
                        if (delta >= 0 && AbsListView.this.mFirstPosition < AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL) {
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
                    if (this.mLastSDRRatio != android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                        viewRoot.setSDRRatio(android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
                        this.mLastSDRRatio = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
                    }
                    isNearEdge = true;
                    if (isNearEdge) {
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

        public void run() {
            OverScroller scroller;
            switch (AbsListView.this.mTouchMode) {
                case AbsListView.TOUCH_MODE_SCROLL /*3*/:
                    if (this.mScroller.isFinished()) {
                        return;
                    }
                    break;
                case AbsListView.TOUCH_MODE_FLING /*4*/:
                    break;
                case AbsListView.TOUCH_MODE_OVERFLING /*6*/:
                    scroller = this.mScroller;
                    if (!scroller.computeScrollOffset()) {
                        endFling();
                        break;
                    }
                    int scrollY = AbsListView.this.mScrollY;
                    int currY = scroller.getCurrY();
                    if (!AbsListView.this.overScrollBy(AbsListView.TRANSCRIPT_MODE_DISABLED, currY - scrollY, AbsListView.TRANSCRIPT_MODE_DISABLED, scrollY, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.this.mOverflingDistance, AbsListView.PROFILE_SCROLLING)) {
                        AbsListView.this.invalidate();
                        AbsListView.this.postOnAnimation(this);
                        break;
                    }
                    boolean crossDown = (scrollY > 0 || currY <= 0) ? AbsListView.PROFILE_SCROLLING : true;
                    boolean crossUp = (scrollY < 0 || currY >= 0) ? AbsListView.PROFILE_SCROLLING : true;
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
            if (AbsListView.this.mDataChanged) {
                AbsListView.this.layoutChildren();
            }
            if (AbsListView.this.mItemCount == 0 || AbsListView.this.getChildCount() == 0) {
                endFling();
                return;
            }
            scroller = this.mScroller;
            boolean more = scroller.computeScrollOffset();
            int y = scroller.getCurrY();
            int delta = dealWith2DSDR(this.mLastFlingY - y, scroller);
            if (delta > 0) {
                AbsListView.this.mMotionPosition = AbsListView.this.mFirstPosition;
                AbsListView.this.mMotionViewOriginalTop = AbsListView.this.getChildAt(AbsListView.TRANSCRIPT_MODE_DISABLED).getTop();
                delta = AbsListView.this.adjustFlingDistance(delta);
            } else {
                int offsetToLast = AbsListView.this.getChildCount() + AbsListView.TOUCH_MODE_UNKNOWN;
                AbsListView.this.mMotionPosition = AbsListView.this.mFirstPosition + offsetToLast;
                AbsListView.this.mMotionViewOriginalTop = AbsListView.this.getChildAt(offsetToLast).getTop();
                delta = AbsListView.this.adjustFlingDistance(delta);
            }
            View motionView = AbsListView.this.getChildAt(AbsListView.this.mMotionPosition - AbsListView.this.mFirstPosition);
            int oldTop = AbsListView.TRANSCRIPT_MODE_DISABLED;
            if (motionView != null) {
                oldTop = motionView.getTop();
            }
            boolean atEdge = AbsListView.this.trackMotionScroll(delta, delta);
            boolean atEnd = (!atEdge || delta == 0) ? AbsListView.PROFILE_SCROLLING : true;
            if (atEnd) {
                if (motionView != null) {
                    int overshoot = -(delta - (motionView.getTop() - oldTop));
                    if (!AbsListView.this.hasSpringAnimatorMask()) {
                        AbsListView.this.overScrollBy(AbsListView.TRANSCRIPT_MODE_DISABLED, overshoot, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.this.mScrollY, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.TRANSCRIPT_MODE_DISABLED, AbsListView.this.mOverflingDistance, AbsListView.PROFILE_SCROLLING);
                    }
                }
                if (more) {
                    edgeReached(delta);
                }
            } else {
                if (!more || atEnd) {
                    endFling();
                } else {
                    if (atEdge) {
                        AbsListView.this.invalidate();
                    }
                    this.mLastFlingY = y;
                    AbsListView.this.postOnAnimation(this);
                }
                if (AbsListView.this.mIHwWechatOptimize.isWechatOptimizeEffect() && AbsListView.this.mIHwWechatOptimize.isWechatFling() && Math.abs(scroller.getCurrVelocity()) < ((float) AbsListView.this.mIHwWechatOptimize.getWechatIdleVelocity())) {
                    AbsListView.this.mIHwWechatOptimize.setWechatFling(AbsListView.PROFILE_SCROLLING);
                    AbsListView.this.reportScrollStateChange(AbsListView.TRANSCRIPT_MODE_DISABLED);
                }
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
            if (editorAction != AbsListView.TOUCH_MODE_OVERFLING) {
                return AbsListView.PROFILE_SCROLLING;
            }
            InputMethodManager imm = (InputMethodManager) AbsListView.this.getContext().getSystemService(InputMethodManager.class);
            if (imm != null) {
                imm.hideSoftInputFromWindow(AbsListView.this.getWindowToken(), AbsListView.TRANSCRIPT_MODE_DISABLED);
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
                return GL10.GL_LIGHT0;
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
    }

    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {
        @ExportedProperty(category = "list")
        boolean forceAdd;
        boolean isEnabled;
        long itemId;
        @ExportedProperty(category = "list")
        boolean recycledHeaderFooter;
        int scrappedFromPosition;
        @ExportedProperty(category = "list", mapping = {@IntToString(from = -1, to = "ITEM_VIEW_TYPE_IGNORE"), @IntToString(from = -2, to = "ITEM_VIEW_TYPE_HEADER_OR_FOOTER")})
        int viewType;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.itemId = -1;
        }

        public LayoutParams(int w, int h) {
            super(w, h);
            this.itemId = -1;
        }

        public LayoutParams(int w, int h, int viewType) {
            super(w, h);
            this.itemId = -1;
            this.viewType = viewType;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
            this.itemId = -1;
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean performAccessibilityAction(View host, int action, Bundle arguments) {
            if (super.performAccessibilityAction(host, action, arguments)) {
                return true;
            }
            int position = AbsListView.this.getPositionForView(host);
            if (position == AbsListView.TOUCH_MODE_UNKNOWN || AbsListView.this.mAdapter == null || position >= AbsListView.this.mAdapter.getCount()) {
                return AbsListView.PROFILE_SCROLLING;
            }
            android.view.ViewGroup.LayoutParams lp = host.getLayoutParams();
            boolean z;
            if (lp instanceof LayoutParams) {
                z = ((LayoutParams) lp).isEnabled;
            } else {
                z = AbsListView.PROFILE_SCROLLING;
            }
            if (!AbsListView.this.isEnabled() || !r2) {
                return AbsListView.PROFILE_SCROLLING;
            }
            switch (action) {
                case AbsListView.TOUCH_MODE_FLING /*4*/:
                    if (AbsListView.this.getSelectedItemPosition() == position) {
                        return AbsListView.PROFILE_SCROLLING;
                    }
                    AbsListView.this.setSelection(position);
                    return true;
                case PGSdk.TYPE_VIDEO /*8*/:
                    if (AbsListView.this.getSelectedItemPosition() != position) {
                        return AbsListView.PROFILE_SCROLLING;
                    }
                    AbsListView.this.setSelection(AbsListView.TOUCH_MODE_UNKNOWN);
                    return true;
                case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                    if (!AbsListView.this.isItemClickable(host)) {
                        return AbsListView.PROFILE_SCROLLING;
                    }
                    return AbsListView.this.performItemClick(host, position, AbsListView.this.getItemIdAtPosition(position));
                case IndexSearchConstants.INDEX_BUILD_FLAG_INTERNAL_FILE /*32*/:
                    if (!AbsListView.this.isLongClickable()) {
                        return AbsListView.PROFILE_SCROLLING;
                    }
                    return AbsListView.this.performLongPress(host, position, AbsListView.this.getItemIdAtPosition(position));
                default:
                    return AbsListView.PROFILE_SCROLLING;
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
            return this.mWrapped != null ? true : AbsListView.PROFILE_SCROLLING;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (!this.mWrapped.onCreateActionMode(mode, menu)) {
                return AbsListView.PROFILE_SCROLLING;
            }
            AbsListView.this.setLongClickable(AbsListView.PROFILE_SCROLLING);
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
            super(null);
        }

        public void run() {
            if (!AbsListView.this.mDataChanged) {
                ListAdapter adapter = AbsListView.this.mAdapter;
                int motionPosition = this.mClickMotionPosition;
                if (adapter != null && AbsListView.this.mItemCount > 0 && motionPosition != AbsListView.TOUCH_MODE_UNKNOWN && motionPosition < adapter.getCount() && sameWindow() && adapter.isEnabled(motionPosition)) {
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

        /* renamed from: android.widget.AbsListView.PositionScroller.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ int val$position;

            AnonymousClass1(int val$position) {
                this.val$position = val$position;
            }

            public void run() {
                PositionScroller.this.start(this.val$position);
            }
        }

        /* renamed from: android.widget.AbsListView.PositionScroller.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ int val$boundPosition;
            final /* synthetic */ int val$position;

            AnonymousClass2(int val$position, int val$boundPosition) {
                this.val$position = val$position;
                this.val$boundPosition = val$boundPosition;
            }

            public void run() {
                PositionScroller.this.start(this.val$position, this.val$boundPosition);
            }
        }

        /* renamed from: android.widget.AbsListView.PositionScroller.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ int val$duration;
            final /* synthetic */ int val$position;
            final /* synthetic */ int val$postOffset;

            AnonymousClass3(int val$position, int val$postOffset, int val$duration) {
                this.val$position = val$position;
                this.val$postOffset = val$postOffset;
                this.val$duration = val$duration;
            }

            public void run() {
                PositionScroller.this.startWithOffset(this.val$position, this.val$postOffset, this.val$duration);
            }
        }

        PositionScroller() {
            this.mExtraScroll = ViewConfiguration.get(AbsListView.this.mContext).getScaledFadingEdgeLength();
        }

        public void start(int position) {
            stop();
            if (AbsListView.this.mDataChanged) {
                AbsListView.this.mPositionScrollAfterLayout = new AnonymousClass1(position);
                return;
            }
            int childCount = AbsListView.this.getChildCount();
            if (childCount != 0) {
                int viewTravelCount;
                int firstPos = AbsListView.this.mFirstPosition;
                int lastPos = (firstPos + childCount) + AbsListView.TOUCH_MODE_UNKNOWN;
                int clampedPosition = Math.max(AbsListView.TRANSCRIPT_MODE_DISABLED, Math.min(AbsListView.this.getCount() + AbsListView.TOUCH_MODE_UNKNOWN, position));
                if (clampedPosition < firstPos) {
                    viewTravelCount = (firstPos - clampedPosition) + MOVE_DOWN_POS;
                    this.mMode = MOVE_UP_POS;
                } else if (clampedPosition > lastPos) {
                    viewTravelCount = (clampedPosition - lastPos) + MOVE_DOWN_POS;
                    this.mMode = MOVE_DOWN_POS;
                } else {
                    scrollToVisible(clampedPosition, AbsListView.TOUCH_MODE_UNKNOWN, SCROLL_DURATION);
                    return;
                }
                if (viewTravelCount > 0) {
                    this.mScrollDuration = SCROLL_DURATION / viewTravelCount;
                } else {
                    this.mScrollDuration = SCROLL_DURATION;
                }
                this.mTargetPos = clampedPosition;
                this.mBoundPos = AbsListView.TOUCH_MODE_UNKNOWN;
                this.mLastSeenPos = AbsListView.TOUCH_MODE_UNKNOWN;
                AbsListView.this.postOnAnimation(this);
            }
        }

        public void start(int position, int boundPosition) {
            stop();
            if (boundPosition == AbsListView.TOUCH_MODE_UNKNOWN) {
                start(position);
            } else if (AbsListView.this.mDataChanged) {
                AbsListView.this.mPositionScrollAfterLayout = new AnonymousClass2(position, boundPosition);
            } else {
                int childCount = AbsListView.this.getChildCount();
                if (childCount != 0) {
                    int viewTravelCount;
                    int firstPos = AbsListView.this.mFirstPosition;
                    int lastPos = (firstPos + childCount) + AbsListView.TOUCH_MODE_UNKNOWN;
                    int clampedPosition = Math.max(AbsListView.TRANSCRIPT_MODE_DISABLED, Math.min(AbsListView.this.getCount() + AbsListView.TOUCH_MODE_UNKNOWN, position));
                    int posTravel;
                    int boundTravel;
                    if (clampedPosition < firstPos) {
                        int boundPosFromLast = lastPos - boundPosition;
                        if (boundPosFromLast >= MOVE_DOWN_POS) {
                            posTravel = (firstPos - clampedPosition) + MOVE_DOWN_POS;
                            boundTravel = boundPosFromLast + AbsListView.TOUCH_MODE_UNKNOWN;
                            if (boundTravel < posTravel) {
                                viewTravelCount = boundTravel;
                                this.mMode = MOVE_UP_BOUND;
                            } else {
                                viewTravelCount = posTravel;
                                this.mMode = MOVE_UP_POS;
                            }
                        } else {
                            return;
                        }
                    } else if (clampedPosition > lastPos) {
                        int boundPosFromFirst = boundPosition - firstPos;
                        if (boundPosFromFirst >= MOVE_DOWN_POS) {
                            posTravel = (clampedPosition - lastPos) + MOVE_DOWN_POS;
                            boundTravel = boundPosFromFirst + AbsListView.TOUCH_MODE_UNKNOWN;
                            if (boundTravel < posTravel) {
                                viewTravelCount = boundTravel;
                                this.mMode = MOVE_DOWN_BOUND;
                            } else {
                                viewTravelCount = posTravel;
                                this.mMode = MOVE_DOWN_POS;
                            }
                        } else {
                            return;
                        }
                    } else {
                        scrollToVisible(clampedPosition, boundPosition, SCROLL_DURATION);
                        return;
                    }
                    if (viewTravelCount > 0) {
                        this.mScrollDuration = SCROLL_DURATION / viewTravelCount;
                    } else {
                        this.mScrollDuration = SCROLL_DURATION;
                    }
                    this.mTargetPos = clampedPosition;
                    this.mBoundPos = boundPosition;
                    this.mLastSeenPos = AbsListView.TOUCH_MODE_UNKNOWN;
                    AbsListView.this.postOnAnimation(this);
                }
            }
        }

        public void startWithOffset(int position, int offset) {
            startWithOffset(position, offset, SCROLL_DURATION);
        }

        public void startWithOffset(int position, int offset, int duration) {
            stop();
            if (AbsListView.this.mDataChanged) {
                int postOffset = offset;
                AbsListView.this.mPositionScrollAfterLayout = new AnonymousClass3(position, offset, duration);
                return;
            }
            int childCount = AbsListView.this.getChildCount();
            if (childCount != 0) {
                int viewTravelCount;
                offset += AbsListView.this.getPaddingTop();
                this.mTargetPos = Math.max(AbsListView.TRANSCRIPT_MODE_DISABLED, Math.min(AbsListView.this.getCount() + AbsListView.TOUCH_MODE_UNKNOWN, position));
                this.mOffsetFromTop = offset;
                this.mBoundPos = AbsListView.TOUCH_MODE_UNKNOWN;
                this.mLastSeenPos = AbsListView.TOUCH_MODE_UNKNOWN;
                this.mMode = MOVE_OFFSET;
                int firstPos = AbsListView.this.mFirstPosition;
                int lastPos = (firstPos + childCount) + AbsListView.TOUCH_MODE_UNKNOWN;
                if (this.mTargetPos < firstPos) {
                    viewTravelCount = firstPos - this.mTargetPos;
                } else if (this.mTargetPos > lastPos) {
                    viewTravelCount = this.mTargetPos - lastPos;
                } else {
                    AbsListView.this.smoothScrollBy(AbsListView.this.getChildAt(this.mTargetPos - firstPos).getTop() - offset, duration, true);
                    return;
                }
                float screenTravelCount = ((float) viewTravelCount) / ((float) childCount);
                if (screenTravelCount >= android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                    duration = (int) (((float) duration) / screenTravelCount);
                }
                this.mScrollDuration = duration;
                this.mLastSeenPos = AbsListView.TOUCH_MODE_UNKNOWN;
                AbsListView.this.postOnAnimation(this);
            }
        }

        private void scrollToVisible(int targetPos, int boundPos, int duration) {
            int firstPos = AbsListView.this.mFirstPosition;
            int lastPos = (firstPos + AbsListView.this.getChildCount()) + AbsListView.TOUCH_MODE_UNKNOWN;
            int paddedTop = AbsListView.this.mListPadding.top;
            int paddedBottom = AbsListView.this.getHeight() - AbsListView.this.mListPadding.bottom;
            if (targetPos < firstPos || targetPos > lastPos) {
                Log.w(AbsListView.TAG, "scrollToVisible called with targetPos " + targetPos + " not visible [" + firstPos + ", " + lastPos + "]");
            }
            if (boundPos < firstPos || boundPos > lastPos) {
                boundPos = AbsListView.TOUCH_MODE_UNKNOWN;
            }
            View targetChild = AbsListView.this.getChildAt(targetPos - firstPos);
            int targetTop = targetChild.getTop();
            int targetBottom = targetChild.getBottom();
            int scrollBy = AbsListView.TRANSCRIPT_MODE_DISABLED;
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
                        scrollBy = Math.max(AbsListView.TRANSCRIPT_MODE_DISABLED, boundBottom - paddedBottom);
                    } else if (scrollBy > 0 && boundTop - absScroll < paddedTop) {
                        scrollBy = Math.min(AbsListView.TRANSCRIPT_MODE_DISABLED, boundTop - paddedTop);
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
            int i;
            View lastView;
            int lastViewHeight;
            int lastViewPixelsShowing;
            int extraScroll;
            int childCount;
            switch (this.mMode) {
                case MOVE_DOWN_POS /*1*/:
                    lastViewIndex = AbsListView.this.getChildCount() + AbsListView.TOUCH_MODE_UNKNOWN;
                    lastPos = firstPos + lastViewIndex;
                    if (lastViewIndex >= 0) {
                        i = this.mLastSeenPos;
                        if (lastPos != r0) {
                            lastView = AbsListView.this.getChildAt(lastViewIndex);
                            lastViewHeight = lastView.getHeight();
                            lastViewPixelsShowing = listHeight - lastView.getTop();
                            if (lastPos < AbsListView.this.mItemCount + AbsListView.TOUCH_MODE_UNKNOWN) {
                                extraScroll = Math.max(AbsListView.this.mListPadding.bottom, this.mExtraScroll);
                            } else {
                                extraScroll = AbsListView.this.mListPadding.bottom;
                            }
                            int scrollBy = (lastViewHeight - lastViewPixelsShowing) + extraScroll;
                            AbsListView.this.smoothScrollBy(scrollBy, this.mScrollDuration, true);
                            this.mLastSeenPos = lastPos;
                            i = this.mTargetPos;
                            if (lastPos < r0) {
                                AbsListView.this.postOnAnimation(this);
                                break;
                            }
                        }
                        AbsListView.this.postOnAnimation(this);
                        return;
                    }
                    return;
                    break;
                case MOVE_UP_POS /*2*/:
                    i = this.mLastSeenPos;
                    if (firstPos != r0) {
                        View firstView = AbsListView.this.getChildAt(AbsListView.TRANSCRIPT_MODE_DISABLED);
                        if (firstView != null) {
                            int firstViewTop = firstView.getTop();
                            if (firstPos > 0) {
                                extraScroll = Math.max(this.mExtraScroll, AbsListView.this.mListPadding.top);
                            } else {
                                extraScroll = AbsListView.this.mListPadding.top;
                            }
                            AbsListView.this.smoothScrollBy(firstViewTop - extraScroll, this.mScrollDuration, true);
                            this.mLastSeenPos = firstPos;
                            i = this.mTargetPos;
                            if (firstPos > r0) {
                                AbsListView.this.postOnAnimation(this);
                                break;
                            }
                        }
                        return;
                    }
                    AbsListView.this.postOnAnimation(this);
                    return;
                    break;
                case MOVE_DOWN_BOUND /*3*/:
                    childCount = AbsListView.this.getChildCount();
                    i = this.mBoundPos;
                    if (firstPos != r0 && childCount > MOVE_DOWN_POS && firstPos + childCount < AbsListView.this.mItemCount) {
                        int nextPos = firstPos + MOVE_DOWN_POS;
                        if (nextPos != this.mLastSeenPos) {
                            View nextView = AbsListView.this.getChildAt(MOVE_DOWN_POS);
                            int nextViewHeight = nextView.getHeight();
                            int nextViewTop = nextView.getTop();
                            extraScroll = Math.max(AbsListView.this.mListPadding.bottom, this.mExtraScroll);
                            if (nextPos >= this.mBoundPos) {
                                if (nextViewTop > extraScroll) {
                                    AbsListView.this.smoothScrollBy(nextViewTop - extraScroll, this.mScrollDuration, true);
                                    break;
                                }
                            }
                            int i2 = (nextViewHeight + nextViewTop) - extraScroll;
                            AbsListView.this.smoothScrollBy(Math.max(AbsListView.TRANSCRIPT_MODE_DISABLED, r32), this.mScrollDuration, true);
                            this.mLastSeenPos = nextPos;
                            AbsListView.this.postOnAnimation(this);
                            break;
                        }
                        AbsListView.this.postOnAnimation(this);
                        return;
                    }
                    return;
                    break;
                case MOVE_UP_BOUND /*4*/:
                    lastViewIndex = AbsListView.this.getChildCount() - 2;
                    if (lastViewIndex >= 0) {
                        lastPos = firstPos + lastViewIndex;
                        i = this.mLastSeenPos;
                        if (lastPos != r0) {
                            lastView = AbsListView.this.getChildAt(lastViewIndex);
                            lastViewHeight = lastView.getHeight();
                            int lastViewTop = lastView.getTop();
                            lastViewPixelsShowing = listHeight - lastViewTop;
                            extraScroll = Math.max(AbsListView.this.mListPadding.top, this.mExtraScroll);
                            this.mLastSeenPos = lastPos;
                            i = this.mBoundPos;
                            if (lastPos <= r0) {
                                int bottom = listHeight - extraScroll;
                                int lastViewBottom = lastViewTop + lastViewHeight;
                                if (bottom > lastViewBottom) {
                                    AbsListView.this.smoothScrollBy(-(bottom - lastViewBottom), this.mScrollDuration, true);
                                    break;
                                }
                            }
                            AbsListView.this.smoothScrollBy(-(lastViewPixelsShowing - extraScroll), this.mScrollDuration, true);
                            AbsListView.this.postOnAnimation(this);
                            break;
                        }
                        AbsListView.this.postOnAnimation(this);
                        return;
                    }
                    return;
                    break;
                case MOVE_OFFSET /*5*/:
                    i = this.mLastSeenPos;
                    if (r0 != firstPos) {
                        this.mLastSeenPos = firstPos;
                        childCount = AbsListView.this.getChildCount();
                        int position = this.mTargetPos;
                        lastPos = (firstPos + childCount) + AbsListView.TOUCH_MODE_UNKNOWN;
                        int viewTravelCount = AbsListView.TRANSCRIPT_MODE_DISABLED;
                        if (position < firstPos) {
                            viewTravelCount = (firstPos - position) + MOVE_DOWN_POS;
                        } else if (position > lastPos) {
                            viewTravelCount = position - lastPos;
                        }
                        float modifier = Math.min(Math.abs(((float) viewTravelCount) / ((float) childCount)), android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                        int distance;
                        int duration;
                        if (position >= firstPos) {
                            if (position <= lastPos) {
                                AbsListView absListView = AbsListView.this;
                                distance = r0.getChildAt(position - firstPos).getTop() - this.mOffsetFromTop;
                                duration = (int) (((float) this.mScrollDuration) * (((float) Math.abs(distance)) / ((float) AbsListView.this.getHeight())));
                                AbsListView.this.smoothScrollBy(distance, duration, true);
                                break;
                            }
                            distance = (int) (((float) AbsListView.this.getHeight()) * modifier);
                            duration = (int) (((float) this.mScrollDuration) * modifier);
                            AbsListView.this.smoothScrollBy(distance, duration, true);
                            AbsListView.this.postOnAnimation(this);
                            break;
                        }
                        distance = (int) (((float) (-AbsListView.this.getHeight())) * modifier);
                        duration = (int) (((float) this.mScrollDuration) * modifier);
                        AbsListView.this.smoothScrollBy(distance, duration, true);
                        AbsListView.this.postOnAnimation(this);
                        break;
                    }
                    AbsListView.this.postOnAnimation(this);
            }
        }
    }

    class RecycleBin {
        private View[] mActiveViews;
        private ArrayList<View> mCurrentScrap;
        private int mFirstActivePosition;
        private RecyclerListener mRecyclerListener;
        private ArrayList<View>[] mScrapViews;
        private ArrayList<View> mSkippedScrap;
        private SparseArray<View> mTransientStateViews;
        private LongSparseArray<View> mTransientStateViewsById;
        private int mViewTypeCount;

        RecycleBin() {
            this.mActiveViews = new View[AbsListView.TRANSCRIPT_MODE_DISABLED];
        }

        public void setViewTypeCount(int viewTypeCount) {
            if (viewTypeCount < AbsListView.TRANSCRIPT_MODE_NORMAL) {
                throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
            }
            ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
            for (int i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < viewTypeCount; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                scrapViews[i] = new ArrayList();
            }
            this.mViewTypeCount = viewTypeCount;
            this.mCurrentScrap = scrapViews[AbsListView.TRANSCRIPT_MODE_DISABLED];
            this.mScrapViews = scrapViews;
        }

        public void markChildrenDirty() {
            int i;
            int count;
            ArrayList<View> scrap;
            int scrapCount;
            if (this.mViewTypeCount == AbsListView.TRANSCRIPT_MODE_NORMAL) {
                scrap = this.mCurrentScrap;
                scrapCount = scrap.size();
                for (i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < scrapCount; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                    ((View) scrap.get(i)).forceLayout();
                }
            } else {
                int typeCount = this.mViewTypeCount;
                for (i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < typeCount; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                    scrap = this.mScrapViews[i];
                    scrapCount = scrap.size();
                    for (int j = AbsListView.TRANSCRIPT_MODE_DISABLED; j < scrapCount; j += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                        ((View) scrap.get(j)).forceLayout();
                    }
                }
            }
            if (this.mTransientStateViews != null) {
                count = this.mTransientStateViews.size();
                for (i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < count; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                    ((View) this.mTransientStateViews.valueAt(i)).forceLayout();
                }
            }
            if (this.mTransientStateViewsById != null) {
                count = this.mTransientStateViewsById.size();
                for (i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < count; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                    ((View) this.mTransientStateViewsById.valueAt(i)).forceLayout();
                }
            }
        }

        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0 ? true : AbsListView.PROFILE_SCROLLING;
        }

        void clear() {
            if (this.mViewTypeCount == AbsListView.TRANSCRIPT_MODE_NORMAL) {
                clearScrap(this.mCurrentScrap);
            } else {
                int typeCount = this.mViewTypeCount;
                for (int i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < typeCount; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
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
            for (int i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < childCount; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                View child = AbsListView.this.getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (!(lp == null || lp.viewType == -2)) {
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
            if (AbsListView.this.mAdapter == null || !AbsListView.this.mAdapterHasStableIds || this.mTransientStateViewsById == null) {
                if (this.mTransientStateViews != null) {
                    int index = this.mTransientStateViews.indexOfKey(position);
                    if (index >= 0) {
                        View result = (View) this.mTransientStateViews.valueAt(index);
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
                for (i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < N; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                    removeDetachedView((View) viewsByPos.valueAt(i), AbsListView.PROFILE_SCROLLING);
                }
                viewsByPos.clear();
            }
            LongSparseArray<View> viewsById = this.mTransientStateViewsById;
            if (viewsById != null) {
                N = viewsById.size();
                for (i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < N; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                    removeDetachedView((View) viewsById.valueAt(i), AbsListView.PROFILE_SCROLLING);
                }
                viewsById.clear();
            }
        }

        View getScrapView(int position) {
            int whichScrap = AbsListView.this.getHwItemViewType(position);
            if (whichScrap < 0) {
                return null;
            }
            if (this.mViewTypeCount == AbsListView.TRANSCRIPT_MODE_NORMAL) {
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
                    AbsListView.this.notifyViewAccessibilityStateChangedIfNeeded(AbsListView.TRANSCRIPT_MODE_NORMAL);
                    if (!scrap.hasTransientState()) {
                        if (this.mViewTypeCount == AbsListView.TRANSCRIPT_MODE_NORMAL) {
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
                for (int i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < count; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                    removeDetachedView((View) this.mSkippedScrap.get(i), AbsListView.PROFILE_SCROLLING);
                }
                this.mSkippedScrap.clear();
            }
        }

        void scrapActiveViews() {
            View[] activeViews = this.mActiveViews;
            boolean hasListener = this.mRecyclerListener != null ? true : AbsListView.PROFILE_SCROLLING;
            boolean multipleScraps = this.mViewTypeCount > AbsListView.TRANSCRIPT_MODE_NORMAL ? true : AbsListView.PROFILE_SCROLLING;
            ArrayList<View> scrapViews = this.mCurrentScrap;
            for (int i = activeViews.length + AbsListView.TOUCH_MODE_UNKNOWN; i >= 0; i += AbsListView.TOUCH_MODE_UNKNOWN) {
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
                            removeDetachedView(victim, AbsListView.PROFILE_SCROLLING);
                        }
                    } else if (shouldRecycleViewType(whichScrap)) {
                        if (multipleScraps) {
                            scrapViews = this.mScrapViews[whichScrap];
                        }
                        lp.scrappedFromPosition = this.mFirstActivePosition + i;
                        removeDetachedView(victim, AbsListView.PROFILE_SCROLLING);
                        scrapViews.add(victim);
                        if (hasListener) {
                            this.mRecyclerListener.onMovedToScrapHeap(victim);
                        }
                    } else if (whichScrap != -2) {
                        removeDetachedView(victim, AbsListView.PROFILE_SCROLLING);
                    }
                }
            }
            pruneScrapViews();
        }

        void fullyDetachScrapViews() {
            int viewTypeCount = this.mViewTypeCount;
            ArrayList<View>[] scrapViews = this.mScrapViews;
            for (int i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < viewTypeCount; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                ArrayList<View> scrapPile = scrapViews[i];
                for (int j = scrapPile.size() + AbsListView.TOUCH_MODE_UNKNOWN; j >= 0; j += AbsListView.TOUCH_MODE_UNKNOWN) {
                    View view = (View) scrapPile.get(j);
                    if (view.isTemporarilyDetached()) {
                        removeDetachedView(view, AbsListView.PROFILE_SCROLLING);
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
            for (i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < viewTypeCount; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                ArrayList<View> scrapPile = scrapViews[i];
                int size = scrapPile.size();
                while (size > maxViews) {
                    size += AbsListView.TOUCH_MODE_UNKNOWN;
                    scrapPile.remove(size);
                }
            }
            SparseArray<View> transViewsByPos = this.mTransientStateViews;
            if (transViewsByPos != null) {
                i = AbsListView.TRANSCRIPT_MODE_DISABLED;
                while (i < transViewsByPos.size()) {
                    v = (View) transViewsByPos.valueAt(i);
                    if (!v.hasTransientState()) {
                        removeDetachedView(v, AbsListView.PROFILE_SCROLLING);
                        transViewsByPos.removeAt(i);
                        i += AbsListView.TOUCH_MODE_UNKNOWN;
                    }
                    i += AbsListView.TRANSCRIPT_MODE_NORMAL;
                }
            }
            LongSparseArray<View> transViewsById = this.mTransientStateViewsById;
            if (transViewsById != null) {
                i = AbsListView.TRANSCRIPT_MODE_DISABLED;
                while (i < transViewsById.size()) {
                    v = (View) transViewsById.valueAt(i);
                    if (!v.hasTransientState()) {
                        removeDetachedView(v, AbsListView.PROFILE_SCROLLING);
                        transViewsById.removeAt(i);
                        i += AbsListView.TOUCH_MODE_UNKNOWN;
                    }
                    i += AbsListView.TRANSCRIPT_MODE_NORMAL;
                }
            }
        }

        void reclaimScrapViews(List<View> views) {
            if (this.mViewTypeCount == AbsListView.TRANSCRIPT_MODE_NORMAL) {
                views.addAll(this.mCurrentScrap);
                return;
            }
            int viewTypeCount = this.mViewTypeCount;
            ArrayList<View>[] scrapViews = this.mScrapViews;
            for (int i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < viewTypeCount; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                views.addAll(scrapViews[i]);
            }
        }

        void setCacheColorHint(int color) {
            int i;
            ArrayList<View> scrap;
            int scrapCount;
            if (this.mViewTypeCount == AbsListView.TRANSCRIPT_MODE_NORMAL) {
                scrap = this.mCurrentScrap;
                scrapCount = scrap.size();
                for (i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < scrapCount; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                    ((View) scrap.get(i)).setDrawingCacheBackgroundColor(color);
                }
            } else {
                int typeCount = this.mViewTypeCount;
                for (i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < typeCount; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                    scrap = this.mScrapViews[i];
                    scrapCount = scrap.size();
                    for (int j = AbsListView.TRANSCRIPT_MODE_DISABLED; j < scrapCount; j += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                        ((View) scrap.get(j)).setDrawingCacheBackgroundColor(color);
                    }
                }
            }
            View[] activeViews = this.mActiveViews;
            int count = activeViews.length;
            for (i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < count; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                View victim = activeViews[i];
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
            for (int i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < size; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                LayoutParams params = (LayoutParams) ((View) scrapViews.get(i)).getLayoutParams();
                if (AbsListView.this.mAdapterHasStableIds) {
                    if (AbsListView.this.mAdapter.getItemId(position) == params.itemId) {
                        return (View) scrapViews.remove(i);
                    }
                } else if (params.scrappedFromPosition == position) {
                    scrap = (View) scrapViews.remove(i);
                    clearAccessibilityFromScrap(scrap);
                    return scrap;
                }
            }
            scrap = (View) scrapViews.remove(size + AbsListView.TOUCH_MODE_UNKNOWN);
            clearAccessibilityFromScrap(scrap);
            return scrap;
        }

        private void clearScrap(ArrayList<View> scrap) {
            int scrapCount = scrap.size();
            for (int j = AbsListView.TRANSCRIPT_MODE_DISABLED; j < scrapCount; j += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                removeDetachedView((View) scrap.remove((scrapCount + AbsListView.TOUCH_MODE_UNKNOWN) - j), AbsListView.PROFILE_SCROLLING);
            }
        }

        private void clearAccessibilityFromScrap(View view) {
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

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
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

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.AbsListView.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.AbsListView.SavedState.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.SavedState.<clinit>():void");
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            boolean z = AbsListView.PROFILE_SCROLLING;
            super(in);
            this.selectedId = in.readLong();
            this.firstId = in.readLong();
            this.viewTop = in.readInt();
            this.position = in.readInt();
            this.height = in.readInt();
            this.filter = in.readString();
            if (in.readByte() != null) {
                z = true;
            }
            this.inActionMode = z;
            this.checkedItemCount = in.readInt();
            this.checkState = in.readSparseBooleanArray();
            int N = in.readInt();
            if (N > 0) {
                this.checkIdState = new LongSparseArray();
                for (int i = AbsListView.TRANSCRIPT_MODE_DISABLED; i < N; i += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                    this.checkIdState.put(in.readLong(), Integer.valueOf(in.readInt()));
                }
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            int i;
            int N;
            super.writeToParcel(out, flags);
            out.writeLong(this.selectedId);
            out.writeLong(this.firstId);
            out.writeInt(this.viewTop);
            out.writeInt(this.position);
            out.writeInt(this.height);
            out.writeString(this.filter);
            if (this.inActionMode) {
                i = AbsListView.TRANSCRIPT_MODE_NORMAL;
            } else {
                i = AbsListView.TRANSCRIPT_MODE_DISABLED;
            }
            out.writeByte((byte) i);
            out.writeInt(this.checkedItemCount);
            out.writeSparseBooleanArray(this.checkState);
            if (this.checkIdState != null) {
                N = this.checkIdState.size();
            } else {
                N = AbsListView.TRANSCRIPT_MODE_DISABLED;
            }
            out.writeInt(N);
            for (int i2 = AbsListView.TRANSCRIPT_MODE_DISABLED; i2 < N; i2 += AbsListView.TRANSCRIPT_MODE_NORMAL) {
                out.writeLong(this.checkIdState.keyAt(i2));
                out.writeInt(((Integer) this.checkIdState.valueAt(i2)).intValue());
            }
        }

        public String toString() {
            return "AbsListView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " selectedId=" + this.selectedId + " firstId=" + this.firstId + " viewTop=" + this.viewTop + " position=" + this.position + " height=" + this.height + " filter=" + this.filter + " checkState=" + this.checkState + "}";
        }
    }

    public interface SelectionBoundsAdjuster {
        void adjustListItemSelectionBounds(Rect rect);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.AbsListView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.AbsListView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.<clinit>():void");
    }

    abstract void fillGap(boolean z);

    abstract int findMotionRow(int i);

    abstract void setSelectionInt(int i);

    public AbsListView(Context context) {
        super(context);
        this.mChoiceMode = TRANSCRIPT_MODE_DISABLED;
        this.mLayoutMode = TRANSCRIPT_MODE_DISABLED;
        this.mDeferNotifyDataSetChanged = PROFILE_SCROLLING;
        this.mDrawSelectorOnTop = PROFILE_SCROLLING;
        this.mSelectorPosition = TOUCH_MODE_UNKNOWN;
        this.mSelectorRect = new Rect();
        this.mRecycler = new RecycleBin();
        this.mSelectionLeftPadding = TRANSCRIPT_MODE_DISABLED;
        this.mSelectionTopPadding = TRANSCRIPT_MODE_DISABLED;
        this.mSelectionRightPadding = TRANSCRIPT_MODE_DISABLED;
        this.mSelectionBottomPadding = TRANSCRIPT_MODE_DISABLED;
        this.mListPadding = new Rect();
        this.mWidthMeasureSpec = TRANSCRIPT_MODE_DISABLED;
        this.mTouchMode = TOUCH_MODE_UNKNOWN;
        this.mSelectedTop = TRANSCRIPT_MODE_DISABLED;
        this.mSmoothScrollbarEnabled = true;
        this.mResurrectToPosition = TOUCH_MODE_UNKNOWN;
        this.mContextMenuInfo = null;
        this.mAddItemViewType = -10000;
        this.mAddItemViewPosition = TOUCH_MODE_UNKNOWN;
        this.mLastTouchMode = TOUCH_MODE_UNKNOWN;
        this.mScrollProfilingStarted = PROFILE_SCROLLING;
        this.mFlingProfilingStarted = PROFILE_SCROLLING;
        this.mScrollStrictSpan = null;
        this.mFlingStrictSpan = null;
        this.mLastScrollState = TRANSCRIPT_MODE_DISABLED;
        this.mVelocityScale = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        this.mIsScrap = new boolean[TRANSCRIPT_MODE_NORMAL];
        this.mScrollOffset = new int[TRANSCRIPT_MODE_ALWAYS_SCROLL];
        this.mScrollConsumed = new int[TRANSCRIPT_MODE_ALWAYS_SCROLL];
        this.mTmpPoint = new float[TRANSCRIPT_MODE_ALWAYS_SCROLL];
        this.mNestedYOffset = TRANSCRIPT_MODE_DISABLED;
        this.mActivePointerId = TOUCH_MODE_UNKNOWN;
        this.mDirection = TRANSCRIPT_MODE_DISABLED;
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
        this(context, attrs, defStyleAttr, TRANSCRIPT_MODE_DISABLED);
    }

    public AbsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mChoiceMode = TRANSCRIPT_MODE_DISABLED;
        this.mLayoutMode = TRANSCRIPT_MODE_DISABLED;
        this.mDeferNotifyDataSetChanged = PROFILE_SCROLLING;
        this.mDrawSelectorOnTop = PROFILE_SCROLLING;
        this.mSelectorPosition = TOUCH_MODE_UNKNOWN;
        this.mSelectorRect = new Rect();
        this.mRecycler = new RecycleBin();
        this.mSelectionLeftPadding = TRANSCRIPT_MODE_DISABLED;
        this.mSelectionTopPadding = TRANSCRIPT_MODE_DISABLED;
        this.mSelectionRightPadding = TRANSCRIPT_MODE_DISABLED;
        this.mSelectionBottomPadding = TRANSCRIPT_MODE_DISABLED;
        this.mListPadding = new Rect();
        this.mWidthMeasureSpec = TRANSCRIPT_MODE_DISABLED;
        this.mTouchMode = TOUCH_MODE_UNKNOWN;
        this.mSelectedTop = TRANSCRIPT_MODE_DISABLED;
        this.mSmoothScrollbarEnabled = true;
        this.mResurrectToPosition = TOUCH_MODE_UNKNOWN;
        this.mContextMenuInfo = null;
        this.mAddItemViewType = -10000;
        this.mAddItemViewPosition = TOUCH_MODE_UNKNOWN;
        this.mLastTouchMode = TOUCH_MODE_UNKNOWN;
        this.mScrollProfilingStarted = PROFILE_SCROLLING;
        this.mFlingProfilingStarted = PROFILE_SCROLLING;
        this.mScrollStrictSpan = null;
        this.mFlingStrictSpan = null;
        this.mLastScrollState = TRANSCRIPT_MODE_DISABLED;
        this.mVelocityScale = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        this.mIsScrap = new boolean[TRANSCRIPT_MODE_NORMAL];
        this.mScrollOffset = new int[TRANSCRIPT_MODE_ALWAYS_SCROLL];
        this.mScrollConsumed = new int[TRANSCRIPT_MODE_ALWAYS_SCROLL];
        this.mTmpPoint = new float[TRANSCRIPT_MODE_ALWAYS_SCROLL];
        this.mNestedYOffset = TRANSCRIPT_MODE_DISABLED;
        this.mActivePointerId = TOUCH_MODE_UNKNOWN;
        this.mDirection = TRANSCRIPT_MODE_DISABLED;
        initAbsListView();
        this.mOwnerThread = Thread.currentThread();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AbsListView, defStyleAttr, defStyleRes);
        Drawable selector = a.getDrawable(TRANSCRIPT_MODE_DISABLED);
        if (selector != null) {
            setSelector(selector);
        }
        this.mDrawSelectorOnTop = a.getBoolean(TRANSCRIPT_MODE_NORMAL, PROFILE_SCROLLING);
        setStackFromBottom(a.getBoolean(TRANSCRIPT_MODE_ALWAYS_SCROLL, PROFILE_SCROLLING));
        setScrollingCacheEnabled(a.getBoolean(TOUCH_MODE_SCROLL, true));
        setTextFilterEnabled(a.getBoolean(TOUCH_MODE_FLING, PROFILE_SCROLLING));
        setTranscriptMode(a.getInt(TOUCH_MODE_OVERSCROLL, TRANSCRIPT_MODE_DISABLED));
        setCacheColorHint(a.getColor(TOUCH_MODE_OVERFLING, TRANSCRIPT_MODE_DISABLED));
        setSmoothScrollbarEnabled(a.getBoolean(9, true));
        setChoiceMode(a.getInt(7, TRANSCRIPT_MODE_DISABLED));
        setFastScrollEnabled(a.getBoolean(8, PROFILE_SCROLLING));
        setFastScrollStyle(a.getResourceId(11, TRANSCRIPT_MODE_DISABLED));
        setFastScrollAlwaysVisible(a.getBoolean(10, PROFILE_SCROLLING));
        a.recycle();
    }

    private void initAbsListView() {
        setClickable(true);
        setFocusableInTouchMode(true);
        setWillNotDraw(PROFILE_SCROLLING);
        setAlwaysDrawnWithCacheEnabled(PROFILE_SCROLLING);
        setScrollingCacheEnabled(true);
        ViewConfiguration configuration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mOverscrollDistance = configuration.getScaledOverscrollDistance();
        this.mOverflingDistance = configuration.getScaledOverflingDistance();
        this.mDensityScale = getContext().getResources().getDisplayMetrics().density;
        this.mIHwWechatOptimize = HwWidgetFactory.getHwWechatOptimize();
    }

    public void setOverScrollMode(int mode) {
        if (mode == TRANSCRIPT_MODE_ALWAYS_SCROLL) {
            this.mEdgeGlowTop = null;
            this.mEdgeGlowBottom = null;
        } else if (this.mEdgeGlowTop == null) {
            Context context = getContext();
            this.mEdgeGlowTop = new EdgeEffect(context);
            this.mEdgeGlowBottom = new EdgeEffect(context);
        }
        super.setOverScrollMode(mode);
    }

    public /* bridge */ /* synthetic */ void setAdapter(Adapter adapter) {
        setAdapter((ListAdapter) adapter);
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
        if (this.mAddItemViewPosition == TOUCH_MODE_UNKNOWN || this.mAddItemViewPosition != position || this.mAddItemViewType == -10000) {
            return this.mAdapter.getItemViewType(position);
        }
        return this.mAddItemViewType;
    }

    public boolean isItemChecked(int position) {
        if (this.mChoiceMode == 0 || this.mCheckStates == null) {
            return PROFILE_SCROLLING;
        }
        return this.mCheckStates.get(position);
    }

    public int getCheckedItemPosition() {
        if (this.mChoiceMode == TRANSCRIPT_MODE_NORMAL && this.mCheckStates != null && this.mCheckStates.size() == TRANSCRIPT_MODE_NORMAL) {
            return this.mCheckStates.keyAt(TRANSCRIPT_MODE_DISABLED);
        }
        return TOUCH_MODE_UNKNOWN;
    }

    public SparseBooleanArray getCheckedItemPositions() {
        if (this.mChoiceMode != 0) {
            return this.mCheckStates;
        }
        return null;
    }

    public long[] getCheckedItemIds() {
        if (this.mChoiceMode == 0 || this.mCheckedIdStates == null || this.mAdapter == null) {
            return new long[TRANSCRIPT_MODE_DISABLED];
        }
        LongSparseArray<Integer> idStates = this.mCheckedIdStates;
        int count = idStates.size();
        long[] ids = new long[count];
        for (int i = TRANSCRIPT_MODE_DISABLED; i < count; i += TRANSCRIPT_MODE_NORMAL) {
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
        this.mCheckedItemCount = TRANSCRIPT_MODE_DISABLED;
    }

    public void setItemChecked(int position, boolean value) {
        if (this.mChoiceMode != 0) {
            if (value && this.mChoiceMode == TOUCH_MODE_SCROLL && this.mChoiceActionMode == null) {
                if (this.mMultiChoiceModeCallback == null || !this.mMultiChoiceModeCallback.hasWrappedCallback()) {
                    throw new IllegalStateException("AbsListView: attempted to start selection mode for CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was supplied. Call setMultiChoiceModeListener to set a callback.");
                }
                this.mChoiceActionMode = startActionMode(this.mMultiChoiceModeCallback);
            }
            boolean itemCheckChanged;
            if (this.mChoiceMode == TRANSCRIPT_MODE_ALWAYS_SCROLL || this.mChoiceMode == TOUCH_MODE_SCROLL) {
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
                    itemCheckChanged = true;
                } else {
                    itemCheckChanged = PROFILE_SCROLLING;
                }
                if (itemCheckChanged) {
                    if (value) {
                        this.mCheckedItemCount += TRANSCRIPT_MODE_NORMAL;
                    } else {
                        this.mCheckedItemCount += TOUCH_MODE_UNKNOWN;
                    }
                }
                if (this.mChoiceActionMode != null) {
                    this.mMultiChoiceModeCallback.onItemCheckedStateChanged(this.mChoiceActionMode, position, this.mAdapter.getItemId(position), value);
                }
            } else {
                boolean hasStableIds = this.mCheckedIdStates != null ? this.mAdapter.hasStableIds() : PROFILE_SCROLLING;
                itemCheckChanged = isItemChecked(position) != value ? true : PROFILE_SCROLLING;
                if (value || isItemChecked(position)) {
                    this.mCheckStates.clear();
                    if (hasStableIds) {
                        this.mCheckedIdStates.clear();
                    }
                }
                if (value) {
                    this.mCheckStates.put(position, true);
                    if (hasStableIds) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    }
                    this.mCheckedItemCount = TRANSCRIPT_MODE_NORMAL;
                } else if (this.mCheckStates.size() == 0 || !this.mCheckStates.valueAt(TRANSCRIPT_MODE_DISABLED)) {
                    this.mCheckedItemCount = TRANSCRIPT_MODE_DISABLED;
                }
            }
            if (!(this.mInLayout || this.mBlockLayoutRequests || !itemCheckChanged)) {
                this.mDataChanged = true;
                rememberSyncState();
                requestLayout();
            }
        }
    }

    public boolean performItemClick(View view, int position, long id) {
        boolean handled = PROFILE_SCROLLING;
        boolean dispatchItemClick = true;
        if (this.mChoiceMode != 0) {
            handled = true;
            boolean checkedStateChanged = PROFILE_SCROLLING;
            boolean checked;
            if (this.mChoiceMode == TRANSCRIPT_MODE_ALWAYS_SCROLL || (this.mChoiceMode == TOUCH_MODE_SCROLL && this.mChoiceActionMode != null)) {
                if (this.mCheckStates.get(position, PROFILE_SCROLLING)) {
                    checked = PROFILE_SCROLLING;
                } else {
                    checked = true;
                }
                checked = getCheckedStateForMultiSelect(checked);
                this.mCheckStates.put(position, checked);
                if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                    if (checked) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    } else {
                        this.mCheckedIdStates.delete(this.mAdapter.getItemId(position));
                    }
                }
                if (checked) {
                    this.mCheckedItemCount += TRANSCRIPT_MODE_NORMAL;
                } else {
                    this.mCheckedItemCount += TOUCH_MODE_UNKNOWN;
                }
                if (this.mChoiceActionMode != null) {
                    this.mMultiChoiceModeCallback.onItemCheckedStateChanged(this.mChoiceActionMode, position, id, checked);
                    dispatchItemClick = PROFILE_SCROLLING;
                }
                checkedStateChanged = true;
            } else if (this.mChoiceMode == TRANSCRIPT_MODE_NORMAL) {
                if (this.mCheckStates.get(position, PROFILE_SCROLLING)) {
                    checked = PROFILE_SCROLLING;
                } else {
                    checked = true;
                }
                if (checked) {
                    this.mCheckStates.clear();
                    this.mCheckStates.put(position, true);
                    if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                        this.mCheckedIdStates.clear();
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    }
                    this.mCheckedItemCount = TRANSCRIPT_MODE_NORMAL;
                } else if (this.mCheckStates.size() == 0 || !this.mCheckStates.valueAt(TRANSCRIPT_MODE_DISABLED)) {
                    this.mCheckedItemCount = TRANSCRIPT_MODE_DISABLED;
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
        boolean useActivated = getContext().getApplicationInfo().targetSdkVersion >= 11 ? true : PROFILE_SCROLLING;
        for (int i = TRANSCRIPT_MODE_DISABLED; i < count; i += TRANSCRIPT_MODE_NORMAL) {
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
                this.mCheckStates = new SparseBooleanArray(TRANSCRIPT_MODE_DISABLED);
            }
            if (this.mCheckedIdStates == null && this.mAdapter != null && this.mAdapter.hasStableIds()) {
                this.mCheckedIdStates = new LongSparseArray(TRANSCRIPT_MODE_DISABLED);
            }
            if (this.mChoiceMode == TOUCH_MODE_SCROLL) {
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
            return PROFILE_SCROLLING;
        }
        if (getChildAt(TRANSCRIPT_MODE_DISABLED).getTop() < this.mListPadding.top) {
            z = PROFILE_SCROLLING;
        } else if (getChildAt(childCount + TOUCH_MODE_UNKNOWN).getBottom() > getHeight() - this.mListPadding.bottom) {
            z = PROFILE_SCROLLING;
        }
        return z;
    }

    public void setFastScrollEnabled(boolean enabled) {
        if (this.mFastScrollEnabled != enabled) {
            this.mFastScrollEnabled = enabled;
            if (isOwnerThread()) {
                setFastScrollerEnabledUiThread(enabled);
            } else {
                post(new AnonymousClass1(enabled));
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

    public void setFastScrollAlwaysVisible(boolean alwaysShow) {
        if (this.mFastScrollAlwaysVisible != alwaysShow) {
            if (alwaysShow && !this.mFastScrollEnabled) {
                setFastScrollEnabled(true);
            }
            this.mFastScrollAlwaysVisible = alwaysShow;
            if (isOwnerThread()) {
                setFastScrollerAlwaysVisibleUiThread(alwaysShow);
            } else {
                post(new AnonymousClass2(alwaysShow));
            }
        }
    }

    private void setFastScrollerAlwaysVisibleUiThread(boolean alwaysShow) {
        if (this.mFastScroll != null) {
            this.mFastScroll.setAlwaysShow(alwaysShow);
        }
    }

    private boolean isOwnerThread() {
        return this.mOwnerThread == Thread.currentThread() ? true : PROFILE_SCROLLING;
    }

    public boolean isFastScrollAlwaysVisible() {
        boolean z = PROFILE_SCROLLING;
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
        onScrollChanged(TRANSCRIPT_MODE_DISABLED, TRANSCRIPT_MODE_DISABLED, TRANSCRIPT_MODE_DISABLED, TRANSCRIPT_MODE_DISABLED);
    }

    public void sendAccessibilityEventInternal(int eventType) {
        if (eventType == HwPerformance.PERF_EVENT_RAW_REQ) {
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
        info.setClickable(PROFILE_SCROLLING);
    }

    int getSelectionModeForAccessibility() {
        switch (getChoiceMode()) {
            case TRANSCRIPT_MODE_DISABLED /*0*/:
                return TRANSCRIPT_MODE_DISABLED;
            case TRANSCRIPT_MODE_NORMAL /*1*/:
                return TRANSCRIPT_MODE_NORMAL;
            case TRANSCRIPT_MODE_ALWAYS_SCROLL /*2*/:
            case TOUCH_MODE_SCROLL /*3*/:
                return TRANSCRIPT_MODE_ALWAYS_SCROLL;
            default:
                return TRANSCRIPT_MODE_DISABLED;
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        switch (action) {
            case HwPerformance.PERF_EVENT_RAW_REQ /*4096*/:
            case R.id.accessibilityActionScrollDown /*16908346*/:
                if (!isEnabled() || getLastVisiblePosition() >= getCount() + TOUCH_MODE_UNKNOWN) {
                    return PROFILE_SCROLLING;
                }
                smoothScrollBy((getHeight() - this.mListPadding.top) - this.mListPadding.bottom, StatisticalConstant.TYPE_WIFI_CONNECT_ACTION);
                return true;
            case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD /*8192*/:
            case R.id.accessibilityActionScrollUp /*16908344*/:
                if (!isEnabled() || this.mFirstPosition <= 0) {
                    return PROFILE_SCROLLING;
                }
                smoothScrollBy(-((getHeight() - this.mListPadding.top) - this.mListPadding.bottom), StatisticalConstant.TYPE_WIFI_CONNECT_ACTION);
                return true;
            default:
                return PROFILE_SCROLLING;
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
        if (this.mScrollingCacheEnabled && !enabled) {
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
        boolean haveChildren = (getChildCount() <= 0 || this.mItemCount <= 0) ? PROFILE_SCROLLING : true;
        long selectedId = getSelectedItemId();
        ss.selectedId = selectedId;
        ss.height = getHeight();
        if (selectedId >= 0) {
            ss.viewTop = this.mSelectedTop;
            ss.position = getSelectedItemPosition();
            ss.firstId = -1;
        } else if (!haveChildren || this.mFirstPosition <= 0) {
            ss.viewTop = TRANSCRIPT_MODE_DISABLED;
            ss.firstId = -1;
            ss.position = TRANSCRIPT_MODE_DISABLED;
        } else {
            ss.viewTop = getChildAt(TRANSCRIPT_MODE_DISABLED).getTop();
            int firstPos = this.mFirstPosition;
            if (firstPos >= this.mItemCount) {
                firstPos = this.mItemCount + TOUCH_MODE_UNKNOWN;
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
        boolean z = (this.mChoiceMode != TOUCH_MODE_SCROLL || this.mChoiceActionMode == null) ? PROFILE_SCROLLING : true;
        ss.inActionMode = z;
        if (this.mCheckStates != null) {
            ss.checkState = this.mCheckStates.clone();
        }
        if (this.mCheckedIdStates != null) {
            LongSparseArray<Integer> idState = new LongSparseArray();
            int count = this.mCheckedIdStates.size();
            for (int i = TRANSCRIPT_MODE_DISABLED; i < count; i += TRANSCRIPT_MODE_NORMAL) {
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
        super.onRestoreInstanceState(ss.getSuperState());
        this.mDataChanged = true;
        this.mSyncHeight = (long) ss.height;
        if (ss.selectedId >= 0) {
            this.mNeedSync = true;
            this.mPendingSync = ss;
            this.mSyncRowId = ss.selectedId;
            this.mSyncPosition = ss.position;
            this.mSpecificTop = ss.viewTop;
            this.mSyncMode = TRANSCRIPT_MODE_DISABLED;
        } else if (ss.firstId >= 0) {
            setSelectedPositionInt(TOUCH_MODE_UNKNOWN);
            setNextSelectedPositionInt(TOUCH_MODE_UNKNOWN);
            this.mSelectorPosition = TOUCH_MODE_UNKNOWN;
            this.mNeedSync = true;
            this.mPendingSync = ss;
            this.mSyncRowId = ss.firstId;
            this.mSyncPosition = ss.position;
            this.mSpecificTop = ss.viewTop;
            this.mSyncMode = TRANSCRIPT_MODE_NORMAL;
        }
        setFilterText(ss.filter);
        if (ss.checkState != null) {
            this.mCheckStates = ss.checkState;
        }
        if (ss.checkIdState != null) {
            this.mCheckedIdStates = ss.checkIdState;
        }
        this.mCheckedItemCount = ss.checkedItemCount;
        if (ss.inActionMode && this.mChoiceMode == TOUCH_MODE_SCROLL && this.mMultiChoiceModeCallback != null) {
            this.mChoiceActionMode = startActionMode(this.mMultiChoiceModeCallback);
        }
        requestLayout();
    }

    private boolean acceptFilter() {
        if (!this.mTextFilterEnabled || !(getAdapter() instanceof Filterable)) {
            return PROFILE_SCROLLING;
        }
        if (((Filterable) getAdapter()).getFilter() != null) {
            return true;
        }
        return PROFILE_SCROLLING;
    }

    public void setFilterText(String filterText) {
        if (this.mTextFilterEnabled && !TextUtils.isEmpty(filterText)) {
            createTextFilter(PROFILE_SCROLLING);
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
        if (gainFocus && this.mSelectedPosition < 0 && !isInTouchMode()) {
            if (!(isAttachedToWindow() || this.mAdapter == null)) {
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

    void resetList() {
        removeAllViewsInLayout();
        this.mFirstPosition = TRANSCRIPT_MODE_DISABLED;
        this.mDataChanged = PROFILE_SCROLLING;
        this.mPositionScrollAfterLayout = null;
        this.mNeedSync = PROFILE_SCROLLING;
        this.mPendingSync = null;
        this.mOldSelectedPosition = TOUCH_MODE_UNKNOWN;
        this.mOldSelectedRowId = Long.MIN_VALUE;
        setSelectedPositionInt(TOUCH_MODE_UNKNOWN);
        setNextSelectedPositionInt(TOUCH_MODE_UNKNOWN);
        this.mSelectedTop = TRANSCRIPT_MODE_DISABLED;
        this.mSelectorPosition = TOUCH_MODE_UNKNOWN;
        this.mSelectorRect.setEmpty();
        invalidate();
    }

    protected int computeVerticalScrollExtent() {
        int count = getChildCount();
        if (count <= 0) {
            return TRANSCRIPT_MODE_DISABLED;
        }
        if (!this.mSmoothScrollbarEnabled) {
            return TRANSCRIPT_MODE_NORMAL;
        }
        int extent = count * 100;
        View view = getChildAt(TRANSCRIPT_MODE_DISABLED);
        int top = view.getTop();
        int height = view.getHeight();
        if (height > 0) {
            extent += (top * 100) / height;
        }
        view = getChildAt(count + TOUCH_MODE_UNKNOWN);
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
                View view = getChildAt(TRANSCRIPT_MODE_DISABLED);
                int top = view.getTop();
                int height = view.getHeight();
                if (height > 0) {
                    return Math.max(((firstPosition * 100) - ((top * 100) / height)) + ((int) (((((float) this.mScrollY) / ((float) getHeight())) * ((float) this.mItemCount)) * 100.0f)), TRANSCRIPT_MODE_DISABLED);
                }
            }
            int index;
            int count = this.mItemCount;
            if (firstPosition == 0) {
                index = TRANSCRIPT_MODE_DISABLED;
            } else if (firstPosition + childCount == count) {
                index = count;
            } else {
                index = firstPosition + (childCount / TRANSCRIPT_MODE_ALWAYS_SCROLL);
            }
            return (int) (((float) firstPosition) + (((float) childCount) * (((float) index) / ((float) count))));
        }
        return TRANSCRIPT_MODE_DISABLED;
    }

    protected int computeVerticalScrollRange() {
        if (!this.mSmoothScrollbarEnabled) {
            return this.mItemCount;
        }
        int result = Math.max(this.mItemCount * 100, TRANSCRIPT_MODE_DISABLED);
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
            return android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        }
        int top = getChildAt(TRANSCRIPT_MODE_DISABLED).getTop();
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
        if ((this.mFirstPosition + count) + TOUCH_MODE_UNKNOWN < this.mItemCount + TOUCH_MODE_UNKNOWN) {
            return android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        }
        int bottom = getChildAt(count + TOUCH_MODE_UNKNOWN).getBottom();
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
        if (this.mTranscriptMode == TRANSCRIPT_MODE_NORMAL) {
            int childCount = getChildCount();
            int listBottom = getHeight() - getPaddingBottom();
            View lastChild = getChildAt(childCount + TOUCH_MODE_UNKNOWN);
            int lastBottom = lastChild != null ? lastChild.getBottom() : listBottom;
            if (this.mFirstPosition + childCount < this.mLastHandledItemCount) {
                z = PROFILE_SCROLLING;
            } else if (lastBottom > listBottom) {
                z = PROFILE_SCROLLING;
            }
            this.mForceTranscriptScroll = z;
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mInLayout = true;
        int childCount = getChildCount();
        if (changed) {
            for (int i = TRANSCRIPT_MODE_DISABLED; i < childCount; i += TRANSCRIPT_MODE_NORMAL) {
                getChildAt(i).forceLayout();
            }
            this.mRecycler.markChildrenDirty();
        }
        layoutChildren();
        this.mOverscrollMax = (b - t) / TOUCH_MODE_SCROLL;
        if (this.mFastScroll != null) {
            this.mFastScroll.onItemCountChanged(getChildCount(), this.mItemCount);
        }
        this.mInLayout = PROFILE_SCROLLING;
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = super.setFrame(left, top, right, bottom);
        if (changed) {
            boolean visible = getWindowVisibility() == 0 ? true : PROFILE_SCROLLING;
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
        int i = TRANSCRIPT_MODE_DISABLED;
        if (this.mScrollUp != null) {
            int i2;
            View view = this.mScrollUp;
            if (canScrollUp()) {
                i2 = TRANSCRIPT_MODE_DISABLED;
            } else {
                i2 = TOUCH_MODE_FLING;
            }
            view.setVisibility(i2);
        }
        if (this.mScrollDown != null) {
            View view2 = this.mScrollDown;
            if (!canScrollDown()) {
                i = TOUCH_MODE_FLING;
            }
            view2.setVisibility(i);
        }
    }

    private boolean canScrollUp() {
        boolean canScrollUp = this.mFirstPosition > 0 ? true : PROFILE_SCROLLING;
        if (canScrollUp || getChildCount() <= 0) {
            return canScrollUp;
        }
        return getChildAt(TRANSCRIPT_MODE_DISABLED).getTop() < this.mListPadding.top ? true : PROFILE_SCROLLING;
    }

    private boolean canScrollDown() {
        boolean canScrollDown = PROFILE_SCROLLING;
        int count = getChildCount();
        if (this.mFirstPosition + count < this.mItemCount) {
            canScrollDown = true;
        }
        if (canScrollDown || count <= 0) {
            return canScrollDown;
        }
        return getChildAt(count + TOUCH_MODE_UNKNOWN).getBottom() > this.mBottom - this.mListPadding.bottom ? true : PROFILE_SCROLLING;
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

    View obtainView(int position, boolean[] isScrap) {
        Trace.traceBegin(8, "obtainView");
        isScrap[TRANSCRIPT_MODE_DISABLED] = PROFILE_SCROLLING;
        View transientView = this.mRecycler.getTransientStateView(position);
        if (transientView != null) {
            if (((LayoutParams) transientView.getLayoutParams()).viewType == this.mAdapter.getItemViewType(position)) {
                View updatedView = this.mAdapter.getView(position, transientView, this);
                if (updatedView != transientView) {
                    setItemViewLayoutParams(updatedView, position);
                    this.mRecycler.addScrapView(updatedView, position);
                }
            }
            isScrap[TRANSCRIPT_MODE_DISABLED] = true;
            transientView.dispatchFinishTemporaryDetach();
            return transientView;
        }
        View scrapView = this.mRecycler.getScrapView(position);
        View child = this.mAdapter.getView(position, scrapView, this);
        if (scrapView != null) {
            if (child != scrapView) {
                this.mRecycler.addScrapView(scrapView, position);
            } else if (child.isTemporarilyDetached()) {
                isScrap[TRANSCRIPT_MODE_DISABLED] = true;
                child.dispatchFinishTemporaryDetach();
            } else {
                isScrap[TRANSCRIPT_MODE_DISABLED] = PROFILE_SCROLLING;
            }
        }
        if (this.mCacheColorHint != 0) {
            child.setDrawingCacheBackgroundColor(this.mCacheColorHint);
        }
        if (child.getImportantForAccessibility() == 0) {
            child.setImportantForAccessibility(TRANSCRIPT_MODE_NORMAL);
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
        if (vlp == null) {
            lp = (LayoutParams) generateDefaultLayoutParams();
        } else if (checkLayoutParams(vlp)) {
            lp = (LayoutParams) vlp;
        } else {
            lp = (LayoutParams) generateLayoutParams(vlp);
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
        ListAdapter adapter = getAdapter();
        if (position != TOUCH_MODE_UNKNOWN && adapter != null && position <= adapter.getCount() + TOUCH_MODE_UNKNOWN) {
            android.view.ViewGroup.LayoutParams lp = view.getLayoutParams();
            boolean z;
            if (lp instanceof LayoutParams) {
                z = ((LayoutParams) lp).isEnabled;
            } else {
                z = PROFILE_SCROLLING;
            }
            if (isEnabled() && r1) {
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
            info.setEnabled(PROFILE_SCROLLING);
        }
    }

    private boolean isItemClickable(View view) {
        return view.hasFocusable() ? PROFILE_SCROLLING : true;
    }

    void positionSelectorLikeTouch(int position, View sel, float x, float y) {
        positionSelector(position, sel, true, x, y);
    }

    void positionSelectorLikeFocus(int position, View sel) {
        if (this.mSelector == null || this.mSelectorPosition == position || position == TOUCH_MODE_UNKNOWN) {
            positionSelector(position, sel);
            return;
        }
        Rect bounds = this.mSelectorRect;
        positionSelector(position, sel, true, bounds.exactCenterX(), bounds.exactCenterY());
    }

    void positionSelector(int position, View sel) {
        positionSelector(position, sel, PROFILE_SCROLLING, android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE, android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
    }

    private void positionSelector(int position, View sel, boolean manageHotspot, float x, float y) {
        boolean positionChanged = position != this.mSelectorPosition ? true : PROFILE_SCROLLING;
        if (position != TOUCH_MODE_UNKNOWN) {
            this.mSelectorPosition = position;
        }
        Rect selectorRect = this.mSelectorRect;
        selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom());
        if (position != TOUCH_MODE_UNKNOWN) {
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
                selector.setVisible(PROFILE_SCROLLING, PROFILE_SCROLLING);
                selector.setState(StateSet.NOTHING);
            }
            selector.setBounds(selectorRect);
            if (positionChanged) {
                if (getVisibility() == 0) {
                    selector.setVisible(true, PROFILE_SCROLLING);
                }
                updateSelectorState();
            }
            if (manageHotspot) {
                selector.setHotspot(x, y);
            }
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        int saveCount = TRANSCRIPT_MODE_DISABLED;
        boolean clipToPadding = (this.mGroupFlags & 34) == 34 ? true : PROFILE_SCROLLING;
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
        return (this.mGroupFlags & 34) != 34 ? true : PROFILE_SCROLLING;
    }

    protected int getLeftPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? TRANSCRIPT_MODE_DISABLED : -this.mPaddingLeft;
    }

    protected int getTopPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? TRANSCRIPT_MODE_DISABLED : -this.mPaddingTop;
    }

    protected int getRightPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? TRANSCRIPT_MODE_DISABLED : this.mPaddingRight;
    }

    protected int getBottomPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? TRANSCRIPT_MODE_DISABLED : this.mPaddingBottom;
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
                for (int i = TRANSCRIPT_MODE_DISABLED; i < childCount; i += TRANSCRIPT_MODE_NORMAL) {
                    View child = getChildAt(i);
                    android.view.ViewGroup.LayoutParams lp = child.getLayoutParams();
                    if (lp != null && lp.width >= TRANSCRIPT_MODE_NORMAL) {
                        if (lp.height >= TRANSCRIPT_MODE_NORMAL) {
                        }
                    }
                    child.forceLayout();
                }
            }
        }
    }

    boolean touchModeDrawsInPressedState() {
        switch (this.mTouchMode) {
            case TRANSCRIPT_MODE_NORMAL /*1*/:
            case TRANSCRIPT_MODE_ALWAYS_SCROLL /*2*/:
                return true;
            default:
                return PROFILE_SCROLLING;
        }
    }

    boolean shouldShowSelector() {
        if (!isFocused() || isInTouchMode()) {
            return touchModeDrawsInPressedState() ? isPressed() : PROFILE_SCROLLING;
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
        if (isEnabled() && isClickable()) {
            Drawable selector = this.mSelector;
            Rect selectorRect = this.mSelectorRect;
            if (selector != null && ((isFocused() || touchModeDrawsInPressedState()) && !selectorRect.isEmpty())) {
                View v = getChildAt(this.mSelectedPosition - this.mFirstPosition);
                if (v != null) {
                    if (!v.hasFocusable()) {
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
        int enabledState = ENABLED_STATE_SET[TRANSCRIPT_MODE_DISABLED];
        int[] state = onCreateDrawableState(TRANSCRIPT_MODE_NORMAL);
        int enabledPos = TOUCH_MODE_UNKNOWN;
        for (int i = state.length + TOUCH_MODE_UNKNOWN; i >= 0; i += TOUCH_MODE_UNKNOWN) {
            if (state[i] == enabledState) {
                enabledPos = i;
                break;
            }
        }
        if (enabledPos >= 0) {
            System.arraycopy(state, enabledPos + TRANSCRIPT_MODE_NORMAL, state, enabledPos, (state.length - enabledPos) + TOUCH_MODE_UNKNOWN);
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
        if (!(!this.mTextFilterEnabled || this.mPopup == null || this.mGlobalLayoutListenerAddedFilter)) {
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
            this.mGlobalLayoutListenerAddedFilter = PROFILE_SCROLLING;
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
        this.mIsDetaching = PROFILE_SCROLLING;
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        int touchMode = isInTouchMode() ? TRANSCRIPT_MODE_DISABLED : TRANSCRIPT_MODE_NORMAL;
        if (hasWindowFocus) {
            if (this.mFiltered && !this.mPopupHidden) {
                showPopup();
            }
            if (!(touchMode == this.mLastTouchMode || this.mLastTouchMode == TOUCH_MODE_UNKNOWN)) {
                if (touchMode == TRANSCRIPT_MODE_NORMAL) {
                    resurrectSelection();
                } else {
                    hideSelector();
                    this.mLayoutMode = TRANSCRIPT_MODE_DISABLED;
                    layoutChildren();
                }
            }
        } else {
            setChildrenDrawingCacheEnabled(PROFILE_SCROLLING);
            if (this.mFlingRunnable != null) {
                removeCallbacks(this.mFlingRunnable);
                this.mFlingRunnable.endFling();
                if (this.mPositionScroller != null) {
                    this.mPositionScroller.stop();
                }
                if (this.mScrollY != 0) {
                    this.mScrollY = TRANSCRIPT_MODE_DISABLED;
                    invalidateParentCaches();
                    finishGlows();
                    invalidate();
                }
            }
            dismissPopup();
            if (touchMode == TRANSCRIPT_MODE_NORMAL) {
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
        if (this.mChoiceMode == TOUCH_MODE_SCROLL && this.mChoiceActionMode == null) {
            View child = getChildAt(this.mMotionPosition - this.mFirstPosition);
            if (child != null && performLongPress(child, this.mMotionPosition, this.mAdapter.getItemId(this.mMotionPosition))) {
                this.mTouchMode = TOUCH_MODE_UNKNOWN;
                setPressed(PROFILE_SCROLLING);
                child.setPressed(PROFILE_SCROLLING);
                return true;
            }
        }
        return PROFILE_SCROLLING;
    }

    boolean performLongPress(View child, int longPressPosition, long longPressId) {
        return performLongPress(child, longPressPosition, longPressId, android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE, android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
    }

    boolean performLongPress(View child, int longPressPosition, long longPressId, float x, float y) {
        if (this.mChoiceMode == TOUCH_MODE_SCROLL) {
            if (this.mChoiceActionMode == null) {
                ActionMode startActionMode = startActionMode(this.mMultiChoiceModeCallback);
                this.mChoiceActionMode = startActionMode;
                if (startActionMode != null) {
                    setItemChecked(longPressPosition, true);
                    performHapticFeedback(TRANSCRIPT_MODE_DISABLED);
                }
            }
            return true;
        }
        boolean handled = PROFILE_SCROLLING;
        if (this.mOnItemLongClickListener != null) {
            handled = this.mOnItemLongClickListener.onItemLongClick(this, child, longPressPosition, longPressId);
        }
        if (!handled) {
            this.mContextMenuInfo = createContextMenuInfo(child, longPressPosition, longPressId);
            if (x == android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE || y == android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                handled = super.showContextMenuForChild(this);
            } else {
                handled = super.showContextMenuForChild(this, x, y);
            }
        }
        if (handled) {
            performHapticFeedback(TRANSCRIPT_MODE_DISABLED);
        }
        return handled;
    }

    protected ContextMenuInfo getContextMenuInfo() {
        return this.mContextMenuInfo;
    }

    public boolean showContextMenu() {
        return showContextMenuInternal(0.0f, 0.0f, PROFILE_SCROLLING);
    }

    public boolean showContextMenu(float x, float y) {
        return showContextMenuInternal(x, y, true);
    }

    private boolean showContextMenuInternal(float x, float y, boolean useOffsets) {
        int position = pointToPosition((int) x, (int) y);
        if (position != TOUCH_MODE_UNKNOWN) {
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
            return PROFILE_SCROLLING;
        }
        return showContextMenuForChildInternal(originalView, 0.0f, 0.0f, PROFILE_SCROLLING);
    }

    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return showContextMenuForChildInternal(originalView, x, y, true);
    }

    private boolean showContextMenuForChildInternal(View originalView, float x, float y, boolean useOffsets) {
        int longPressPosition = getPositionForView(originalView);
        if (longPressPosition < 0) {
            return PROFILE_SCROLLING;
        }
        long longPressId = this.mAdapter.getItemId(longPressPosition);
        boolean handled = PROFILE_SCROLLING;
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
        return PROFILE_SCROLLING;
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
                    view.setPressed(PROFILE_SCROLLING);
                }
                setPressed(PROFILE_SCROLLING);
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
        for (int i = getChildCount() + TOUCH_MODE_UNKNOWN; i >= 0; i += TOUCH_MODE_UNKNOWN) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return this.mFirstPosition + i;
                }
            }
        }
        return TOUCH_MODE_UNKNOWN;
    }

    public long pointToRowId(int x, int y) {
        int position = pointToPosition(x, y);
        if (position >= 0) {
            return this.mAdapter.getItemId(position);
        }
        return Long.MIN_VALUE;
    }

    private boolean startScrollIfNeeded(int x, int y, MotionEvent vtev) {
        boolean overscroll;
        int deltaY = y - this.mMotionY;
        int distance = Math.abs(deltaY);
        if (this.mScrollY != 0) {
            overscroll = true;
        } else {
            overscroll = PROFILE_SCROLLING;
        }
        if ((!overscroll && distance <= this.mTouchSlop) || (getNestedScrollAxes() & TRANSCRIPT_MODE_ALWAYS_SCROLL) != 0) {
            return PROFILE_SCROLLING;
        }
        createScrollingCache();
        if (overscroll) {
            this.mTouchMode = TOUCH_MODE_OVERSCROLL;
            this.mMotionCorrection = TRANSCRIPT_MODE_DISABLED;
        } else {
            this.mTouchMode = TOUCH_MODE_SCROLL;
            this.mMotionCorrection = deltaY > 0 ? this.mTouchSlop : -this.mTouchSlop;
        }
        removeCallbacks(this.mPendingCheckForLongPress);
        setPressed(PROFILE_SCROLLING);
        View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
        if (motionView != null) {
            motionView.setPressed(PROFILE_SCROLLING);
        }
        reportScrollStateChange(TRANSCRIPT_MODE_NORMAL);
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        scrollIfNeeded(x, y, vtev);
        return true;
    }

    private void scrollIfNeeded(int x, int y, MotionEvent vtev) {
        int i;
        int motionCorrectionCompensation = TRANSCRIPT_MODE_DISABLED;
        if (this.mMotionCorrection != 0) {
            motionCorrectionCompensation = this.mMotionCorrection > 0 ? TOUCH_MODE_UNKNOWN : TRANSCRIPT_MODE_NORMAL;
            this.mMotionCorrection += motionCorrectionCompensation;
        }
        int rawDeltaY = y - this.mMotionY;
        int scrollOffsetCorrection = TRANSCRIPT_MODE_DISABLED;
        int scrollConsumedCorrection = TRANSCRIPT_MODE_DISABLED;
        if (this.mLastY == RtlSpacingHelper.UNDEFINED) {
            rawDeltaY -= this.mMotionCorrection;
        }
        if (this.mLastY != RtlSpacingHelper.UNDEFINED) {
            i = this.mLastY - y;
        } else {
            i = -rawDeltaY;
        }
        if (dispatchNestedPreScroll(TRANSCRIPT_MODE_DISABLED, i, this.mScrollConsumed, this.mScrollOffset)) {
            rawDeltaY += this.mScrollConsumed[TRANSCRIPT_MODE_NORMAL];
            scrollOffsetCorrection = -this.mScrollOffset[TRANSCRIPT_MODE_NORMAL];
            scrollConsumedCorrection = this.mScrollConsumed[TRANSCRIPT_MODE_NORMAL];
            if (vtev != null) {
                vtev.offsetLocation(0.0f, (float) this.mScrollOffset[TRANSCRIPT_MODE_NORMAL]);
                this.mNestedYOffset += this.mScrollOffset[TRANSCRIPT_MODE_NORMAL];
            }
        }
        int deltaY = rawDeltaY;
        int incrementalDeltaY = this.mLastY != RtlSpacingHelper.UNDEFINED ? ((y - this.mLastY) + scrollConsumedCorrection) - motionCorrectionCompensation : deltaY;
        int lastYCorrection = TRANSCRIPT_MODE_DISABLED;
        View motionView;
        int overscrollMode;
        if (this.mTouchMode == TOUCH_MODE_SCROLL) {
            if (this.mScrollStrictSpan == null) {
                this.mScrollStrictSpan = StrictMode.enterCriticalSpan("AbsListView-scroll");
            }
            if (y != this.mLastY) {
                int motionIndex;
                if ((this.mGroupFlags & Protocol.BASE_CONNECTIVITY_MANAGER) == 0 && Math.abs(rawDeltaY) > this.mTouchSlop) {
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (this.mMotionPosition >= 0) {
                    motionIndex = this.mMotionPosition - this.mFirstPosition;
                } else {
                    motionIndex = getChildCount() / TRANSCRIPT_MODE_ALWAYS_SCROLL;
                }
                int motionViewPrevTop = TRANSCRIPT_MODE_DISABLED;
                motionView = getChildAt(motionIndex);
                if (motionView != null) {
                    motionViewPrevTop = motionView.getTop();
                }
                boolean atEdge = PROFILE_SCROLLING;
                if (incrementalDeltaY != 0) {
                    atEdge = trackMotionScroll(deltaY, incrementalDeltaY);
                }
                motionView = getChildAt(motionIndex);
                if (motionView != null) {
                    int motionViewRealTop = motionView.getTop();
                    if (atEdge) {
                        int overscroll = (-incrementalDeltaY) - (motionViewRealTop - motionViewPrevTop);
                        if (dispatchNestedScroll(TRANSCRIPT_MODE_DISABLED, overscroll - incrementalDeltaY, TRANSCRIPT_MODE_DISABLED, overscroll, this.mScrollOffset)) {
                            lastYCorrection = 0 - this.mScrollOffset[TRANSCRIPT_MODE_NORMAL];
                            if (vtev != null) {
                                vtev.offsetLocation(0.0f, (float) this.mScrollOffset[TRANSCRIPT_MODE_NORMAL]);
                                this.mNestedYOffset += this.mScrollOffset[TRANSCRIPT_MODE_NORMAL];
                            }
                        } else {
                            boolean atOverscrollEdge = overScrollBy(TRANSCRIPT_MODE_DISABLED, overscroll, TRANSCRIPT_MODE_DISABLED, this.mScrollY, TRANSCRIPT_MODE_DISABLED, TRANSCRIPT_MODE_DISABLED, TRANSCRIPT_MODE_DISABLED, this.mOverscrollDistance, true);
                            if (atOverscrollEdge && this.mVelocityTracker != null) {
                                this.mVelocityTracker.clear();
                            }
                            overscrollMode = getOverScrollMode();
                            if (overscrollMode == 0 || (overscrollMode == TRANSCRIPT_MODE_NORMAL && !contentFits())) {
                                if (!atOverscrollEdge) {
                                    this.mDirection = TRANSCRIPT_MODE_DISABLED;
                                    this.mTouchMode = TOUCH_MODE_OVERSCROLL;
                                }
                                if (this.mEdgeGlowTop != null) {
                                    if (incrementalDeltaY > 0) {
                                        this.mEdgeGlowTop.onPull(((float) (-overscroll)) / ((float) getHeight()), ((float) x) / ((float) getWidth()));
                                        if (!this.mEdgeGlowBottom.isFinished()) {
                                            this.mEdgeGlowBottom.onRelease();
                                        }
                                        invalidateTopGlow();
                                    } else if (incrementalDeltaY < 0) {
                                        this.mEdgeGlowBottom.onPull(((float) overscroll) / ((float) getHeight()), android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - (((float) x) / ((float) getWidth())));
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
        } else if (this.mTouchMode == TOUCH_MODE_OVERSCROLL && y != this.mLastY) {
            int oldScroll = this.mScrollY;
            int newScroll = oldScroll - incrementalDeltaY;
            int newDirection = y > this.mLastY ? TRANSCRIPT_MODE_NORMAL : TOUCH_MODE_UNKNOWN;
            if (this.mDirection == 0) {
                this.mDirection = newDirection;
            }
            int overScrollDistance = -incrementalDeltaY;
            if ((newScroll >= 0 || oldScroll < 0) && (newScroll <= 0 || oldScroll > 0)) {
                incrementalDeltaY = TRANSCRIPT_MODE_DISABLED;
            } else {
                overScrollDistance = -oldScroll;
                incrementalDeltaY += overScrollDistance;
            }
            if (overScrollDistance != 0) {
                overScrollBy(TRANSCRIPT_MODE_DISABLED, overScrollDistance, TRANSCRIPT_MODE_DISABLED, this.mScrollY, TRANSCRIPT_MODE_DISABLED, TRANSCRIPT_MODE_DISABLED, TRANSCRIPT_MODE_DISABLED, this.mOverscrollDistance, true);
                overscrollMode = getOverScrollMode();
                if ((overscrollMode == 0 || (overscrollMode == TRANSCRIPT_MODE_NORMAL && !contentFits())) && this.mEdgeGlowTop != null) {
                    if (rawDeltaY > 0) {
                        this.mEdgeGlowTop.onPull(((float) overScrollDistance) / ((float) getHeight()), ((float) x) / ((float) getWidth()));
                        if (!this.mEdgeGlowBottom.isFinished()) {
                            this.mEdgeGlowBottom.onRelease();
                        }
                        invalidateTopGlow();
                    } else if (rawDeltaY < 0) {
                        this.mEdgeGlowBottom.onPull(((float) overScrollDistance) / ((float) getHeight()), android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - (((float) x) / ((float) getWidth())));
                        if (!this.mEdgeGlowTop.isFinished()) {
                            this.mEdgeGlowTop.onRelease();
                        }
                        invalidateBottomGlow();
                    }
                }
            }
            if (incrementalDeltaY != 0) {
                if (this.mScrollY != 0) {
                    this.mScrollY = TRANSCRIPT_MODE_DISABLED;
                    invalidateParentIfNeeded();
                }
                trackMotionScroll(incrementalDeltaY, incrementalDeltaY);
                this.mTouchMode = TOUCH_MODE_SCROLL;
                int motionPosition = findClosestMotionRow(y);
                this.mMotionCorrection = TRANSCRIPT_MODE_DISABLED;
                motionView = getChildAt(motionPosition - this.mFirstPosition);
                this.mMotionViewOriginalTop = motionView != null ? motionView.getTop() : TRANSCRIPT_MODE_DISABLED;
                this.mMotionY = y + scrollOffsetCorrection;
                this.mMotionPosition = motionPosition;
            }
            this.mLastY = (y + TRANSCRIPT_MODE_DISABLED) + scrollOffsetCorrection;
            this.mDirection = newDirection;
        }
    }

    private void invalidateTopGlow() {
        if (this.mEdgeGlowTop != null) {
            boolean clipToPadding = getClipToPadding();
            int top = clipToPadding ? this.mPaddingTop : TRANSCRIPT_MODE_DISABLED;
            invalidate(clipToPadding ? this.mPaddingLeft : TRANSCRIPT_MODE_DISABLED, top, clipToPadding ? getWidth() - this.mPaddingRight : getWidth(), this.mEdgeGlowTop.getMaxHeight() + top);
        }
    }

    private void invalidateBottomGlow() {
        if (this.mEdgeGlowBottom != null) {
            boolean clipToPadding = getClipToPadding();
            int bottom = clipToPadding ? getHeight() - this.mPaddingBottom : getHeight();
            invalidate(clipToPadding ? this.mPaddingLeft : TRANSCRIPT_MODE_DISABLED, bottom - this.mEdgeGlowBottom.getMaxHeight(), clipToPadding ? getWidth() - this.mPaddingRight : getWidth(), bottom);
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
        if (touchMode == TOUCH_MODE_OVERSCROLL || touchMode == TOUCH_MODE_OVERFLING) {
            if (this.mFlingRunnable != null) {
                this.mFlingRunnable.endFling();
            }
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }
            if (this.mScrollY != 0) {
                this.mScrollY = TRANSCRIPT_MODE_DISABLED;
                invalidateParentCaches();
                finishGlows();
                invalidate();
            }
        }
    }

    protected boolean handleScrollBarDragging(MotionEvent event) {
        return PROFILE_SCROLLING;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean z = true;
        if (isEnabled()) {
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }
            if (this.mIsDetaching || !isAttachedToWindow()) {
                return PROFILE_SCROLLING;
            }
            startNestedScroll(TRANSCRIPT_MODE_ALWAYS_SCROLL);
            if (this.mFastScroll != null && this.mFastScroll.onTouchEvent(ev)) {
                return true;
            }
            initVelocityTrackerIfNotExists();
            MotionEvent vtev = MotionEvent.obtain(ev);
            int actionMasked = ev.getActionMasked();
            if (actionMasked == 0) {
                this.mNestedYOffset = TRANSCRIPT_MODE_DISABLED;
            }
            vtev.offsetLocation(0.0f, (float) this.mNestedYOffset);
            int x;
            int y;
            int motionPosition;
            switch (actionMasked) {
                case TRANSCRIPT_MODE_DISABLED /*0*/:
                    onTouchDown(ev);
                    break;
                case TRANSCRIPT_MODE_NORMAL /*1*/:
                    onTouchUp(ev);
                    break;
                case TRANSCRIPT_MODE_ALWAYS_SCROLL /*2*/:
                    onTouchMove(ev, vtev);
                    break;
                case TOUCH_MODE_SCROLL /*3*/:
                    onTouchCancel();
                    break;
                case TOUCH_MODE_OVERSCROLL /*5*/:
                    int index = ev.getActionIndex();
                    int id = ev.getPointerId(index);
                    x = (int) ev.getX(index);
                    y = (int) ev.getY(index);
                    this.mMotionCorrection = TRANSCRIPT_MODE_DISABLED;
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
                case TOUCH_MODE_OVERFLING /*6*/:
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
        this.mHasPerformedLongPress = PROFILE_SCROLLING;
        this.mActivePointerId = ev.getPointerId(TRANSCRIPT_MODE_DISABLED);
        if (this.mTouchMode == TOUCH_MODE_OVERFLING) {
            this.mFlingRunnable.endFling();
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }
            this.mTouchMode = TOUCH_MODE_OVERSCROLL;
            this.mMotionX = (int) ev.getX();
            this.mMotionY = (int) ev.getY();
            this.mLastY = this.mMotionY;
            this.mMotionCorrection = TRANSCRIPT_MODE_DISABLED;
            this.mDirection = TRANSCRIPT_MODE_DISABLED;
        } else {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            int motionPosition = pointToPosition(x, y);
            if (!this.mDataChanged) {
                if (this.mTouchMode == TOUCH_MODE_FLING) {
                    createScrollingCache();
                    this.mTouchMode = TOUCH_MODE_SCROLL;
                    this.mMotionCorrection = TRANSCRIPT_MODE_DISABLED;
                    motionPosition = findMotionRow(y);
                    this.mFlingRunnable.flywheelTouch();
                } else if (motionPosition >= 0 && getAdapter().isEnabled(motionPosition)) {
                    this.mTouchMode = TRANSCRIPT_MODE_DISABLED;
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
            this.mLastY = RtlSpacingHelper.UNDEFINED;
        }
        if (this.mTouchMode == 0 && this.mMotionPosition != TOUCH_MODE_UNKNOWN && performButtonActionOnTouchDown(ev)) {
            removeCallbacks(this.mPendingCheckForTap);
        }
    }

    private void onTouchMove(MotionEvent ev, MotionEvent vtev) {
        if (!this.mHasPerformedLongPress) {
            int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
            if (pointerIndex == TOUCH_MODE_UNKNOWN) {
                pointerIndex = TRANSCRIPT_MODE_DISABLED;
                this.mActivePointerId = ev.getPointerId(TRANSCRIPT_MODE_DISABLED);
            }
            if (this.mDataChanged) {
                layoutChildren();
            }
            int y = (int) ev.getY(pointerIndex);
            switch (this.mTouchMode) {
                case TRANSCRIPT_MODE_DISABLED /*0*/:
                case TRANSCRIPT_MODE_NORMAL /*1*/:
                case TRANSCRIPT_MODE_ALWAYS_SCROLL /*2*/:
                    if (!startScrollIfNeeded((int) ev.getX(pointerIndex), y, vtev)) {
                        View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
                        float x = ev.getX(pointerIndex);
                        if (pointInView(x, (float) y, (float) this.mTouchSlop)) {
                            if (motionView != null) {
                                float[] point = this.mTmpPoint;
                                point[TRANSCRIPT_MODE_DISABLED] = x;
                                point[TRANSCRIPT_MODE_NORMAL] = (float) y;
                                transformPointToViewLocal(point, motionView);
                                motionView.drawableHotspotChanged(point[TRANSCRIPT_MODE_DISABLED], point[TRANSCRIPT_MODE_NORMAL]);
                                break;
                            }
                        }
                        setPressed(PROFILE_SCROLLING);
                        if (motionView != null) {
                            motionView.setPressed(PROFILE_SCROLLING);
                        }
                        removeCallbacks(this.mTouchMode == 0 ? this.mPendingCheckForTap : this.mPendingCheckForLongPress);
                        this.mTouchMode = TRANSCRIPT_MODE_ALWAYS_SCROLL;
                        updateSelectorState();
                        break;
                    }
                    break;
                case TOUCH_MODE_SCROLL /*3*/:
                case TOUCH_MODE_OVERSCROLL /*5*/:
                    scrollIfNeeded((int) ev.getX(pointerIndex), y, vtev);
                    break;
            }
            onMultiSelectMove(ev, pointerIndex);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onTouchUp(MotionEvent ev) {
        VelocityTracker velocityTracker;
        int initialVelocity;
        switch (this.mTouchMode) {
            case TRANSCRIPT_MODE_DISABLED /*0*/:
            case TRANSCRIPT_MODE_NORMAL /*1*/:
            case TRANSCRIPT_MODE_ALWAYS_SCROLL /*2*/:
                int motionPosition = this.mMotionPosition;
                View child = getChildAt(motionPosition - this.mFirstPosition);
                if (child != null) {
                    boolean inList;
                    PerformClick performClick;
                    int i;
                    CheckForTap checkForTap;
                    Object obj;
                    Drawable d;
                    if (this.mTouchMode != 0) {
                        child.setPressed(PROFILE_SCROLLING);
                    }
                    float x = ev.getX();
                    if (x > ((float) this.mListPadding.left)) {
                        if (x < ((float) (getWidth() - this.mListPadding.right))) {
                            inList = true;
                            if (inList && !child.hasFocusable()) {
                                if (this.mPerformClick == null) {
                                    this.mPerformClick = new PerformClick(null);
                                }
                                performClick = this.mPerformClick;
                                performClick.mClickMotionPosition = motionPosition;
                                performClick.rememberWindowAttachCount();
                                this.mResurrectToPosition = motionPosition;
                                if (this.mTouchMode != 0) {
                                    i = this.mTouchMode;
                                    if (r0 != TRANSCRIPT_MODE_NORMAL) {
                                        if (!this.mDataChanged) {
                                            if (this.mAdapter.isEnabled(motionPosition)) {
                                                performClick.run();
                                            }
                                        }
                                    }
                                }
                                if (this.mTouchMode != 0) {
                                    checkForTap = this.mPendingCheckForTap;
                                } else {
                                    obj = this.mPendingCheckForLongPress;
                                }
                                removeCallbacks(checkForTap);
                                this.mLayoutMode = TRANSCRIPT_MODE_DISABLED;
                                if (!this.mDataChanged) {
                                    if (this.mAdapter.isEnabled(motionPosition)) {
                                        this.mTouchMode = TRANSCRIPT_MODE_NORMAL;
                                        setSelectedPositionInt(this.mMotionPosition);
                                        layoutChildren();
                                        child.setPressed(true);
                                        positionSelector(this.mMotionPosition, child);
                                        setPressed(true);
                                        if (this.mSelector != null) {
                                            d = this.mSelector.getCurrent();
                                            if (d != null && (d instanceof TransitionDrawable)) {
                                                ((TransitionDrawable) d).resetTransition();
                                            }
                                            this.mSelector.setHotspot(x, ev.getY());
                                        }
                                        if (this.mTouchModeReset != null) {
                                            removeCallbacks(this.mTouchModeReset);
                                        }
                                        this.mTouchModeReset = new AnonymousClass3(child, performClick);
                                        postDelayed(this.mTouchModeReset, (long) getPressedStateDuration());
                                        return;
                                    }
                                }
                                this.mTouchMode = TOUCH_MODE_UNKNOWN;
                                updateSelectorState();
                                return;
                            }
                        }
                    }
                    inList = PROFILE_SCROLLING;
                    if (this.mPerformClick == null) {
                        this.mPerformClick = new PerformClick(null);
                    }
                    performClick = this.mPerformClick;
                    performClick.mClickMotionPosition = motionPosition;
                    performClick.rememberWindowAttachCount();
                    this.mResurrectToPosition = motionPosition;
                    if (this.mTouchMode != 0) {
                        i = this.mTouchMode;
                        if (r0 != TRANSCRIPT_MODE_NORMAL) {
                            if (this.mDataChanged) {
                                if (this.mAdapter.isEnabled(motionPosition)) {
                                    performClick.run();
                                }
                            }
                        }
                    }
                    if (this.mTouchMode != 0) {
                        obj = this.mPendingCheckForLongPress;
                    } else {
                        checkForTap = this.mPendingCheckForTap;
                    }
                    removeCallbacks(checkForTap);
                    this.mLayoutMode = TRANSCRIPT_MODE_DISABLED;
                    if (this.mDataChanged) {
                        if (this.mAdapter.isEnabled(motionPosition)) {
                            this.mTouchMode = TRANSCRIPT_MODE_NORMAL;
                            setSelectedPositionInt(this.mMotionPosition);
                            layoutChildren();
                            child.setPressed(true);
                            positionSelector(this.mMotionPosition, child);
                            setPressed(true);
                            if (this.mSelector != null) {
                                d = this.mSelector.getCurrent();
                                ((TransitionDrawable) d).resetTransition();
                                this.mSelector.setHotspot(x, ev.getY());
                                break;
                            }
                            if (this.mTouchModeReset != null) {
                                removeCallbacks(this.mTouchModeReset);
                            }
                            this.mTouchModeReset = new AnonymousClass3(child, performClick);
                            postDelayed(this.mTouchModeReset, (long) getPressedStateDuration());
                            return;
                        }
                    }
                    this.mTouchMode = TOUCH_MODE_UNKNOWN;
                    updateSelectorState();
                    return;
                }
                this.mTouchMode = TOUCH_MODE_UNKNOWN;
                updateSelectorState();
                break;
            case TOUCH_MODE_SCROLL /*3*/:
                int childCount = getChildCount();
                if (childCount <= 0) {
                    this.mTouchMode = TOUCH_MODE_UNKNOWN;
                    reportScrollStateChange(TRANSCRIPT_MODE_DISABLED);
                    break;
                }
                int firstChildTop = getChildAt(TRANSCRIPT_MODE_DISABLED).getTop();
                int lastChildBottom = getChildAt(childCount + TOUCH_MODE_UNKNOWN).getBottom();
                int contentTop = this.mListPadding.top;
                int contentBottom = getHeight() - this.mListPadding.bottom;
                if (this.mFirstPosition == 0 && firstChildTop >= contentTop && this.mFirstPosition + childCount < this.mItemCount && lastChildBottom <= getHeight() - contentBottom) {
                    this.mTouchMode = TOUCH_MODE_UNKNOWN;
                    reportScrollStateChange(TRANSCRIPT_MODE_DISABLED);
                    break;
                }
                velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, (float) this.mMaximumVelocity);
                initialVelocity = (int) (velocityTracker.getYVelocity(this.mActivePointerId) * this.mVelocityScale);
                boolean flingVelocity = Math.abs(initialVelocity) > this.mMinimumVelocity ? true : PROFILE_SCROLLING;
                if (flingVelocity) {
                    if (this.mFirstPosition == 0) {
                        break;
                    }
                    if (this.mFirstPosition + childCount == this.mItemCount) {
                        break;
                    }
                    if (!dispatchNestedPreFling(0.0f, (float) (-initialVelocity))) {
                        if (this.mFlingRunnable == null) {
                            this.mFlingRunnable = new FlingRunnable();
                        }
                        reportScrollStateChange(TRANSCRIPT_MODE_ALWAYS_SCROLL);
                        if (this.mIHwWechatOptimize.isWechatOptimizeEffect()) {
                            if (Jlog.isBetaUser()) {
                                Jlog.d(MetricsEvent.ACTION_TUNER_NIGHT_MODE_ADJUST_TINT, "ListViewSpeed", Math.abs(initialVelocity) / TRANSCRIPT_MODE_ALWAYS_SCROLL, "");
                            }
                            if (Math.abs(initialVelocity) > this.mIHwWechatOptimize.getWechatFlingVelocity()) {
                                this.mIHwWechatOptimize.setWechatFling(true);
                            } else {
                                reportScrollStateChange(TRANSCRIPT_MODE_DISABLED);
                            }
                        }
                        this.mFlingRunnable.start(-initialVelocity);
                        dispatchNestedFling(0.0f, (float) (-initialVelocity), true);
                        break;
                    }
                    this.mTouchMode = TOUCH_MODE_UNKNOWN;
                    reportScrollStateChange(TRANSCRIPT_MODE_DISABLED);
                    break;
                }
                this.mTouchMode = TOUCH_MODE_UNKNOWN;
                reportScrollStateChange(TRANSCRIPT_MODE_DISABLED);
                if (this.mFlingRunnable != null) {
                    this.mFlingRunnable.endFling();
                }
                if (this.mPositionScroller != null) {
                    this.mPositionScroller.stop();
                }
                if (flingVelocity) {
                    if (!dispatchNestedPreFling(0.0f, (float) (-initialVelocity))) {
                        dispatchNestedFling(0.0f, (float) (-initialVelocity), PROFILE_SCROLLING);
                        break;
                    }
                }
                break;
            case TOUCH_MODE_OVERSCROLL /*5*/:
                if (this.mFlingRunnable == null) {
                    this.mFlingRunnable = new FlingRunnable();
                }
                velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, (float) this.mMaximumVelocity);
                initialVelocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                reportScrollStateChange(TRANSCRIPT_MODE_ALWAYS_SCROLL);
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
        setPressed(PROFILE_SCROLLING);
        if (this.mEdgeGlowTop != null) {
            this.mEdgeGlowTop.onRelease();
            this.mEdgeGlowBottom.onRelease();
        }
        invalidate();
        removeCallbacks(this.mPendingCheckForLongPress);
        recycleVelocityTracker();
        this.mActivePointerId = TOUCH_MODE_UNKNOWN;
        if (this.mScrollStrictSpan != null) {
            this.mScrollStrictSpan.finish();
            this.mScrollStrictSpan = null;
        }
    }

    private void onTouchCancel() {
        switch (this.mTouchMode) {
            case TOUCH_MODE_OVERSCROLL /*5*/:
                if (this.mFlingRunnable == null) {
                    this.mFlingRunnable = new FlingRunnable();
                }
                this.mFlingRunnable.startSpringback();
                break;
            case TOUCH_MODE_OVERFLING /*6*/:
                break;
            default:
                this.mTouchMode = TOUCH_MODE_UNKNOWN;
                setPressed(PROFILE_SCROLLING);
                View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
                if (motionView != null) {
                    motionView.setPressed(PROFILE_SCROLLING);
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
        this.mActivePointerId = TOUCH_MODE_UNKNOWN;
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
        if ((event.getSource() & TRANSCRIPT_MODE_ALWAYS_SCROLL) != 0) {
            switch (event.getAction()) {
                case PGSdk.TYPE_VIDEO /*8*/:
                    if (this.mTouchMode == TOUCH_MODE_UNKNOWN) {
                        float vscroll = event.getAxisValue(9);
                        if (vscroll != 0.0f) {
                            int delta = (int) (getVerticalScrollFactor() * vscroll);
                            if (!trackMotionScroll(delta, delta)) {
                                return true;
                            }
                        }
                    }
                    break;
                case PGSdk.TYPE_IM /*11*/:
                    int actionButton = event.getActionButton();
                    if ((actionButton == 32 || actionButton == TRANSCRIPT_MODE_ALWAYS_SCROLL) && ((this.mTouchMode == 0 || this.mTouchMode == TRANSCRIPT_MODE_NORMAL) && performStylusButtonPressAction(event))) {
                        removeCallbacks(this.mPendingCheckForLongPress);
                        removeCallbacks(this.mPendingCheckForTap);
                        break;
                    }
            }
        }
        return super.onGenericMotionEvent(event);
    }

    public void fling(int velocityY) {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable();
        }
        reportScrollStateChange(TRANSCRIPT_MODE_ALWAYS_SCROLL);
        this.mFlingRunnable.start(velocityY);
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & TRANSCRIPT_MODE_ALWAYS_SCROLL) != 0 ? true : PROFILE_SCROLLING;
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(TRANSCRIPT_MODE_ALWAYS_SCROLL);
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        View motionView = getChildAt(getChildCount() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
        int oldTop = motionView != null ? motionView.getTop() : TRANSCRIPT_MODE_DISABLED;
        if (motionView == null || trackMotionScroll(-dyUnconsumed, -dyUnconsumed)) {
            int myUnconsumed = dyUnconsumed;
            int myConsumed = TRANSCRIPT_MODE_DISABLED;
            if (motionView != null) {
                myConsumed = motionView.getTop() - oldTop;
                myUnconsumed = dyUnconsumed - myConsumed;
            }
            dispatchNestedScroll(TRANSCRIPT_MODE_DISABLED, myConsumed, TRANSCRIPT_MODE_DISABLED, myUnconsumed, null);
        }
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        int childCount = getChildCount();
        if (consumed || childCount <= 0 || !canScrollList((int) velocityY) || Math.abs(velocityY) <= ((float) this.mMinimumVelocity)) {
            return dispatchNestedFling(velocityX, velocityY, consumed);
        }
        reportScrollStateChange(TRANSCRIPT_MODE_ALWAYS_SCROLL);
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable();
        }
        if (!dispatchNestedPreFling(0.0f, velocityY)) {
            this.mFlingRunnable.start((int) velocityY);
        }
        return true;
    }

    public void draw(Canvas canvas) {
        int i = TRANSCRIPT_MODE_DISABLED;
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
                translateX = TRANSCRIPT_MODE_DISABLED;
                translateY = TRANSCRIPT_MODE_DISABLED;
            }
            if (!this.mEdgeGlowTop.isFinished()) {
                restoreCount = canvas.save();
                canvas.clipRect(translateX, translateY, translateX + width, this.mEdgeGlowTop.getMaxHeight() + translateY);
                canvas.translate((float) translateX, (float) (Math.min(TRANSCRIPT_MODE_DISABLED, this.mFirstPositionDistanceGuess + scrollY) + translateY));
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

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        if (this.mIsDetaching || !isAttachedToWindow()) {
            return PROFILE_SCROLLING;
        }
        if (this.mFastScroll != null && this.mFastScroll.onInterceptTouchEvent(ev)) {
            return true;
        }
        int y;
        switch (actionMasked) {
            case TRANSCRIPT_MODE_DISABLED /*0*/:
                int touchMode = this.mTouchMode;
                if (touchMode == TOUCH_MODE_OVERFLING || touchMode == TOUCH_MODE_OVERSCROLL) {
                    this.mMotionCorrection = TRANSCRIPT_MODE_DISABLED;
                    return true;
                }
                int x = (int) ev.getX();
                y = (int) ev.getY();
                this.mActivePointerId = ev.getPointerId(TRANSCRIPT_MODE_DISABLED);
                int motionPosition = findMotionRow(y);
                if (touchMode != TOUCH_MODE_FLING && motionPosition >= 0) {
                    this.mMotionViewOriginalTop = getChildAt(motionPosition - this.mFirstPosition).getTop();
                    this.mMotionX = x;
                    this.mMotionY = y;
                    this.mMotionPosition = motionPosition;
                    this.mTouchMode = TRANSCRIPT_MODE_DISABLED;
                    enterMultiSelectModeIfNeeded(motionPosition, x);
                    clearScrollingCache();
                }
                this.mLastY = RtlSpacingHelper.UNDEFINED;
                initOrResetVelocityTracker();
                this.mVelocityTracker.addMovement(ev);
                this.mNestedYOffset = TRANSCRIPT_MODE_DISABLED;
                startNestedScroll(TRANSCRIPT_MODE_ALWAYS_SCROLL);
                if (touchMode == TOUCH_MODE_FLING) {
                    return true;
                }
                break;
            case TRANSCRIPT_MODE_NORMAL /*1*/:
            case TOUCH_MODE_SCROLL /*3*/:
                this.mTouchMode = TOUCH_MODE_UNKNOWN;
                this.mActivePointerId = TOUCH_MODE_UNKNOWN;
                recycleVelocityTracker();
                reportScrollStateChange(TRANSCRIPT_MODE_DISABLED);
                stopNestedScroll();
                break;
            case TRANSCRIPT_MODE_ALWAYS_SCROLL /*2*/:
                switch (this.mTouchMode) {
                    case TRANSCRIPT_MODE_DISABLED /*0*/:
                        int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                        if (pointerIndex == TOUCH_MODE_UNKNOWN) {
                            pointerIndex = TRANSCRIPT_MODE_DISABLED;
                            this.mActivePointerId = ev.getPointerId(TRANSCRIPT_MODE_DISABLED);
                        }
                        y = (int) ev.getY(pointerIndex);
                        initVelocityTrackerIfNotExists();
                        this.mVelocityTracker.addMovement(ev);
                        if (startScrollIfNeeded((int) ev.getX(pointerIndex), y, null)) {
                            return true;
                        }
                        break;
                    default:
                        break;
                }
            case TOUCH_MODE_OVERFLING /*6*/:
                onSecondaryPointerUp(ev);
                break;
        }
        return PROFILE_SCROLLING;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> 8;
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            int newPointerIndex;
            if (pointerIndex == 0) {
                newPointerIndex = TRANSCRIPT_MODE_NORMAL;
            } else {
                newPointerIndex = TRANSCRIPT_MODE_DISABLED;
            }
            this.mMotionX = (int) ev.getX(newPointerIndex);
            this.mMotionY = (int) ev.getY(newPointerIndex);
            this.mMotionCorrection = TRANSCRIPT_MODE_DISABLED;
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    public void addTouchables(ArrayList<View> views) {
        int count = getChildCount();
        int firstPosition = this.mFirstPosition;
        ListAdapter adapter = this.mAdapter;
        if (adapter != null) {
            for (int i = TRANSCRIPT_MODE_DISABLED; i < count; i += TRANSCRIPT_MODE_NORMAL) {
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
        smoothScrollBy(distance, duration, PROFILE_SCROLLING);
    }

    void smoothScrollBy(int distance, int duration, boolean linear) {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable();
        }
        int firstPos = this.mFirstPosition;
        int childCount = getChildCount();
        int lastPos = firstPos + childCount;
        int topLimit = getPaddingTop();
        int bottomLimit = getHeight() - getPaddingBottom();
        if (distance == 0 || this.mItemCount == 0 || childCount == 0 || ((firstPos == 0 && getChildAt(TRANSCRIPT_MODE_DISABLED).getTop() == topLimit && distance < 0) || (lastPos == this.mItemCount && getChildAt(childCount + TOUCH_MODE_UNKNOWN).getBottom() == bottomLimit && distance > 0))) {
            this.mFlingRunnable.endFling();
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
                return;
            }
            return;
        }
        reportScrollStateChange(TRANSCRIPT_MODE_ALWAYS_SCROLL);
        this.mFlingRunnable.startScroll(distance, duration, linear);
    }

    void smoothScrollByOffset(int position) {
        int index = TOUCH_MODE_UNKNOWN;
        if (position < 0) {
            index = getFirstVisiblePosition();
        } else if (position > 0) {
            index = getLastVisiblePosition();
        }
        if (index > TOUCH_MODE_UNKNOWN) {
            View child = getChildAt(index - getFirstVisiblePosition());
            if (child != null) {
                Rect visibleRect = new Rect();
                if (child.getGlobalVisibleRect(visibleRect)) {
                    float visibleArea = ((float) (visibleRect.width() * visibleRect.height())) / ((float) (child.getWidth() * child.getHeight()));
                    if (position < 0 && visibleArea < 0.75f) {
                        index += TRANSCRIPT_MODE_NORMAL;
                    } else if (position > 0 && visibleArea < 0.75f) {
                        index += TOUCH_MODE_UNKNOWN;
                    }
                }
                smoothScrollToPosition(Math.max(TRANSCRIPT_MODE_DISABLED, Math.min(getCount(), index + position)));
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

    private void clearScrollingCache() {
        if (!isHardwareAccelerated()) {
            if (this.mClearScrollingCache == null) {
                this.mClearScrollingCache = new Runnable() {
                    public void run() {
                        if (AbsListView.this.mCachingStarted) {
                            AbsListView absListView = AbsListView.this;
                            AbsListView.this.mCachingActive = AbsListView.PROFILE_SCROLLING;
                            absListView.mCachingStarted = AbsListView.PROFILE_SCROLLING;
                            AbsListView.this.setChildrenDrawnWithCacheEnabled(AbsListView.PROFILE_SCROLLING);
                            if ((AbsListView.this.mPersistentDrawingCache & AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL) == 0) {
                                AbsListView.this.setChildrenDrawingCacheEnabled(AbsListView.PROFILE_SCROLLING);
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
            return PROFILE_SCROLLING;
        }
        int firstPosition = this.mFirstPosition;
        Rect listPadding = this.mListPadding;
        if (direction > 0) {
            int lastBottom = getChildAt(childCount + TOUCH_MODE_UNKNOWN).getBottom();
            if (firstPosition + childCount >= this.mItemCount && lastBottom <= getHeight() - listPadding.bottom) {
                z = PROFILE_SCROLLING;
            }
            return z;
        }
        int firstTop = getChildAt(TRANSCRIPT_MODE_DISABLED).getTop();
        if (firstPosition <= 0 && firstTop >= listPadding.top) {
            z = PROFILE_SCROLLING;
        }
        return z;
    }

    boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return true;
        }
        boolean cannotScrollUp;
        boolean z;
        int firstTop = getChildAt(TRANSCRIPT_MODE_DISABLED).getTop();
        int lastBottom = getChildAt(childCount + TOUCH_MODE_UNKNOWN).getBottom();
        Rect listPadding = this.mListPadding;
        int effectivePaddingTop = TRANSCRIPT_MODE_DISABLED;
        int effectivePaddingBottom = TRANSCRIPT_MODE_DISABLED;
        if ((this.mGroupFlags & 34) == 34) {
            effectivePaddingTop = listPadding.top;
            effectivePaddingBottom = listPadding.bottom;
        }
        int spaceAbove = effectivePaddingTop - firstTop;
        int spaceBelow = lastBottom - (getHeight() - effectivePaddingBottom);
        int height = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
        if (deltaY < 0) {
            deltaY = Math.max(-(height + TOUCH_MODE_UNKNOWN), deltaY);
        } else {
            deltaY = Math.min(height + TOUCH_MODE_UNKNOWN, deltaY);
        }
        if (incrementalDeltaY < 0) {
            incrementalDeltaY = Math.max(-(height + TOUCH_MODE_UNKNOWN), incrementalDeltaY);
        } else {
            incrementalDeltaY = Math.min(height + TOUCH_MODE_UNKNOWN, incrementalDeltaY);
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
        boolean cannotScrollDown = (firstPosition != 0 || firstTop < listPadding.top) ? PROFILE_SCROLLING : incrementalDeltaY >= 0 ? true : PROFILE_SCROLLING;
        if (firstPosition + childCount == this.mItemCount) {
            if (lastBottom <= getHeight() - listPadding.bottom) {
                cannotScrollUp = incrementalDeltaY <= 0 ? true : PROFILE_SCROLLING;
                if (!cannotScrollDown || cannotScrollUp) {
                    if (incrementalDeltaY == 0) {
                        z = true;
                    } else {
                        z = PROFILE_SCROLLING;
                    }
                    return z;
                }
                int i;
                int childIndex;
                boolean down = incrementalDeltaY < 0 ? true : PROFILE_SCROLLING;
                boolean inTouchMode = isInTouchMode();
                if (inTouchMode) {
                    hideSelector();
                }
                int headerViewsCount = getHeaderViewsCount();
                int footerViewsStart = this.mItemCount - getFooterViewsCount();
                int start = TRANSCRIPT_MODE_DISABLED;
                int count = TRANSCRIPT_MODE_DISABLED;
                int i2;
                View child;
                int position;
                if (!down) {
                    int bottom = getHeight() - incrementalDeltaY;
                    if ((this.mGroupFlags & 34) == 34) {
                        bottom -= listPadding.bottom;
                    }
                    for (i2 = childCount + TOUCH_MODE_UNKNOWN; i2 >= 0; i2 += TOUCH_MODE_UNKNOWN) {
                        child = getChildAt(i2);
                        if (child.getTop() <= bottom) {
                            break;
                        }
                        start = i2;
                        count += TRANSCRIPT_MODE_NORMAL;
                        position = firstPosition + i2;
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
                    for (i2 = TRANSCRIPT_MODE_DISABLED; i2 < childCount; i2 += TRANSCRIPT_MODE_NORMAL) {
                        child = getChildAt(i2);
                        if (child.getBottom() >= top) {
                            break;
                        }
                        count += TRANSCRIPT_MODE_NORMAL;
                        position = firstPosition + i2;
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
                if (!inTouchMode) {
                    i = this.mSelectedPosition;
                    if (r0 != TOUCH_MODE_UNKNOWN) {
                        childIndex = this.mSelectedPosition - this.mFirstPosition;
                        if (childIndex >= 0 && childIndex < getChildCount()) {
                            positionSelector(this.mSelectedPosition, getChildAt(childIndex));
                        }
                        this.mBlockLayoutRequests = PROFILE_SCROLLING;
                        invokeOnItemScrollListener();
                        return PROFILE_SCROLLING;
                    }
                }
                i = this.mSelectorPosition;
                if (r0 != TOUCH_MODE_UNKNOWN) {
                    childIndex = this.mSelectorPosition - this.mFirstPosition;
                    if (childIndex >= 0 && childIndex < getChildCount()) {
                        positionSelector(TOUCH_MODE_UNKNOWN, getChildAt(childIndex));
                    }
                } else {
                    this.mSelectorRect.setEmpty();
                }
                this.mBlockLayoutRequests = PROFILE_SCROLLING;
                invokeOnItemScrollListener();
                return PROFILE_SCROLLING;
            }
        }
        cannotScrollUp = PROFILE_SCROLLING;
        if (cannotScrollDown) {
        }
        if (incrementalDeltaY == 0) {
            z = PROFILE_SCROLLING;
        } else {
            z = true;
        }
        return z;
    }

    int getHeaderViewsCount() {
        return TRANSCRIPT_MODE_DISABLED;
    }

    int getFooterViewsCount() {
        return TRANSCRIPT_MODE_DISABLED;
    }

    void hideSelector() {
        if (this.mSelectedPosition != TOUCH_MODE_UNKNOWN) {
            if (this.mLayoutMode != TOUCH_MODE_FLING) {
                this.mResurrectToPosition = this.mSelectedPosition;
            }
            if (this.mNextSelectedPosition >= 0 && this.mNextSelectedPosition != this.mSelectedPosition) {
                this.mResurrectToPosition = this.mNextSelectedPosition;
            }
            setSelectedPositionInt(TOUCH_MODE_UNKNOWN);
            setNextSelectedPositionInt(TOUCH_MODE_UNKNOWN);
            this.mSelectedTop = TRANSCRIPT_MODE_DISABLED;
        }
    }

    int reconcileSelectedPosition() {
        int position = this.mSelectedPosition;
        if (position < 0) {
            position = this.mResurrectToPosition;
        }
        return Math.min(Math.max(TRANSCRIPT_MODE_DISABLED, position), this.mItemCount + TOUCH_MODE_UNKNOWN);
    }

    int findClosestMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return TOUCH_MODE_UNKNOWN;
        }
        int motionRow = findMotionRow(y);
        if (motionRow == TOUCH_MODE_UNKNOWN) {
            motionRow = (this.mFirstPosition + childCount) + TOUCH_MODE_UNKNOWN;
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
            return PROFILE_SCROLLING;
        }
        updateSelectorState();
        return true;
    }

    boolean resurrectSelection() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return PROFILE_SCROLLING;
        }
        int selectedPos;
        boolean z;
        int selectedTop = TRANSCRIPT_MODE_DISABLED;
        int childrenTop = this.mListPadding.top;
        int i = this.mBottom;
        int i2 = this.mTop;
        int childrenBottom = (r0 - r0) - this.mListPadding.bottom;
        int firstPosition = this.mFirstPosition;
        int toPosition = this.mResurrectToPosition;
        boolean down = true;
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
            for (i = TRANSCRIPT_MODE_DISABLED; i < childCount; i += TRANSCRIPT_MODE_NORMAL) {
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
            down = PROFILE_SCROLLING;
            selectedPos = (firstPosition + childCount) + TOUCH_MODE_UNKNOWN;
            for (i = childCount + TOUCH_MODE_UNKNOWN; i >= 0; i += TOUCH_MODE_UNKNOWN) {
                View v = getChildAt(i);
                top = v.getTop();
                int bottom = v.getBottom();
                if (i == childCount + TOUCH_MODE_UNKNOWN) {
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
        this.mResurrectToPosition = TOUCH_MODE_UNKNOWN;
        removeCallbacks(this.mFlingRunnable);
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        this.mTouchMode = TOUCH_MODE_UNKNOWN;
        clearScrollingCache();
        this.mSpecificTop = selectedTop;
        selectedPos = lookForSelectablePosition(selectedPos, down);
        if (selectedPos < firstPosition || selectedPos > getLastVisiblePosition()) {
            selectedPos = TOUCH_MODE_UNKNOWN;
        } else {
            this.mLayoutMode = TOUCH_MODE_FLING;
            updateSelectorState();
            setSelectionInt(selectedPos);
            invokeOnItemScrollListener();
        }
        reportScrollStateChange(TRANSCRIPT_MODE_DISABLED);
        if (selectedPos >= 0) {
            z = true;
        } else {
            z = PROFILE_SCROLLING;
        }
        return z;
    }

    void confirmCheckedPositionsById() {
        this.mCheckStates.clear();
        boolean checkedCountChanged = PROFILE_SCROLLING;
        int checkedIndex = TRANSCRIPT_MODE_DISABLED;
        while (checkedIndex < this.mCheckedIdStates.size()) {
            long id = this.mCheckedIdStates.keyAt(checkedIndex);
            int lastPos = ((Integer) this.mCheckedIdStates.valueAt(checkedIndex)).intValue();
            if (id != this.mAdapter.getItemId(lastPos)) {
                int start = Math.max(TRANSCRIPT_MODE_DISABLED, lastPos - 20);
                int end = Math.min(lastPos + CHECK_POSITION_SEARCH_DISTANCE, this.mItemCount);
                boolean found = PROFILE_SCROLLING;
                for (int searchPos = start; searchPos < end; searchPos += TRANSCRIPT_MODE_NORMAL) {
                    if (id == this.mAdapter.getItemId(searchPos)) {
                        found = true;
                        this.mCheckStates.put(searchPos, true);
                        this.mCheckedIdStates.setValueAt(checkedIndex, Integer.valueOf(searchPos));
                        break;
                    }
                }
                if (!found) {
                    this.mCheckedIdStates.delete(id);
                    checkedIndex += TOUCH_MODE_UNKNOWN;
                    this.mCheckedItemCount += TOUCH_MODE_UNKNOWN;
                    checkedCountChanged = true;
                    if (!(this.mChoiceActionMode == null || this.mMultiChoiceModeCallback == null)) {
                        this.mMultiChoiceModeCallback.onItemCheckedStateChanged(this.mChoiceActionMode, lastPos, id, PROFILE_SCROLLING);
                    }
                }
            } else {
                this.mCheckStates.put(lastPos, true);
            }
            checkedIndex += TRANSCRIPT_MODE_NORMAL;
        }
        if (checkedCountChanged && this.mChoiceActionMode != null) {
            this.mChoiceActionMode.invalidate();
        }
    }

    protected void handleDataChanged() {
        int i = TOUCH_MODE_SCROLL;
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
                this.mNeedSync = PROFILE_SCROLLING;
                this.mPendingSync = null;
                if (this.mTranscriptMode == TRANSCRIPT_MODE_ALWAYS_SCROLL) {
                    this.mLayoutMode = TOUCH_MODE_SCROLL;
                    return;
                }
                if (this.mTranscriptMode == TRANSCRIPT_MODE_NORMAL) {
                    if (this.mForceTranscriptScroll) {
                        this.mForceTranscriptScroll = PROFILE_SCROLLING;
                        this.mLayoutMode = TOUCH_MODE_SCROLL;
                        return;
                    }
                    int childCount = getChildCount();
                    int listBottom = getHeight() - getPaddingBottom();
                    View lastChild = getChildAt(childCount + TOUCH_MODE_UNKNOWN);
                    int lastBottom = lastChild != null ? lastChild.getBottom() : listBottom;
                    if (this.mFirstPosition + childCount < lastHandledItemCount || lastBottom > listBottom) {
                        awakenScrollBars();
                    } else {
                        this.mLayoutMode = TOUCH_MODE_SCROLL;
                        return;
                    }
                }
                switch (this.mSyncMode) {
                    case TRANSCRIPT_MODE_DISABLED /*0*/:
                        if (isInTouchMode()) {
                            this.mLayoutMode = TOUCH_MODE_OVERSCROLL;
                            this.mSyncPosition = Math.min(Math.max(TRANSCRIPT_MODE_DISABLED, this.mSyncPosition), count + TOUCH_MODE_UNKNOWN);
                            return;
                        }
                        newPos = findSyncPosition();
                        if (newPos >= 0 && lookForSelectablePosition(newPos, true) == newPos) {
                            this.mSyncPosition = newPos;
                            if (this.mSyncHeight == ((long) getHeight())) {
                                this.mLayoutMode = TOUCH_MODE_OVERSCROLL;
                            } else {
                                this.mLayoutMode = TRANSCRIPT_MODE_ALWAYS_SCROLL;
                            }
                            setNextSelectedPositionInt(newPos);
                            return;
                        }
                    case TRANSCRIPT_MODE_NORMAL /*1*/:
                        this.mLayoutMode = TOUCH_MODE_OVERSCROLL;
                        this.mSyncPosition = Math.min(Math.max(TRANSCRIPT_MODE_DISABLED, this.mSyncPosition), count + TOUCH_MODE_UNKNOWN);
                        return;
                }
            }
            if (!isInTouchMode()) {
                newPos = getSelectedItemPosition();
                if (newPos >= count) {
                    newPos = count + TOUCH_MODE_UNKNOWN;
                }
                if (newPos < 0) {
                    newPos = TRANSCRIPT_MODE_DISABLED;
                }
                int selectablePos = lookForSelectablePosition(newPos, true);
                if (selectablePos >= 0) {
                    setNextSelectedPositionInt(selectablePos);
                    return;
                }
                selectablePos = lookForSelectablePosition(newPos, PROFILE_SCROLLING);
                if (selectablePos >= 0) {
                    setNextSelectedPositionInt(selectablePos);
                    return;
                }
            } else if (this.mResurrectToPosition >= 0) {
                return;
            }
        }
        if (!this.mStackFromBottom) {
            i = TRANSCRIPT_MODE_NORMAL;
        }
        this.mLayoutMode = i;
        this.mSelectedPosition = TOUCH_MODE_UNKNOWN;
        this.mSelectedRowId = Long.MIN_VALUE;
        this.mNextSelectedPosition = TOUCH_MODE_UNKNOWN;
        this.mNextSelectedRowId = Long.MIN_VALUE;
        this.mNeedSync = PROFILE_SCROLLING;
        this.mPendingSync = null;
        this.mSelectorPosition = TOUCH_MODE_UNKNOWN;
        checkSelectionChanged();
    }

    protected void onDisplayHint(int hint) {
        boolean z;
        super.onDisplayHint(hint);
        switch (hint) {
            case TRANSCRIPT_MODE_DISABLED /*0*/:
                if (!(!this.mFiltered || this.mPopup == null || this.mPopup.isShowing())) {
                    showPopup();
                    break;
                }
            case TOUCH_MODE_FLING /*4*/:
                if (this.mPopup != null && this.mPopup.isShowing()) {
                    dismissPopup();
                    break;
                }
        }
        if (hint == TOUCH_MODE_FLING) {
            z = true;
        } else {
            z = PROFILE_SCROLLING;
        }
        this.mPopupHidden = z;
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
        int[] xy = new int[TRANSCRIPT_MODE_ALWAYS_SCROLL];
        getLocationOnScreen(xy);
        int bottomGap = ((screenHeight - xy[TRANSCRIPT_MODE_NORMAL]) - getHeight()) + ((int) (this.mDensityScale * 20.0f));
        if (this.mPopup.isShowing()) {
            this.mPopup.update(xy[TRANSCRIPT_MODE_DISABLED], bottomGap, TOUCH_MODE_UNKNOWN, TOUCH_MODE_UNKNOWN);
        } else {
            this.mPopup.showAtLocation((View) this, 81, xy[TRANSCRIPT_MODE_DISABLED], bottomGap);
        }
    }

    static int getDistance(Rect source, Rect dest, int direction) {
        int sX;
        int sY;
        int dX;
        int dY;
        switch (direction) {
            case TRANSCRIPT_MODE_NORMAL /*1*/:
            case TRANSCRIPT_MODE_ALWAYS_SCROLL /*2*/:
                sX = source.right + (source.width() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
                sY = source.top + (source.height() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
                dX = dest.left + (dest.width() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
                dY = dest.top + (dest.height() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
                break;
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                sX = source.left;
                sY = source.top + (source.height() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
                dX = dest.right;
                dY = dest.top + (dest.height() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
                break;
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                sX = source.left + (source.width() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
                sY = source.top;
                dX = dest.left + (dest.width() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
                dY = dest.bottom;
                break;
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                sX = source.right;
                sY = source.top + (source.height() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
                dX = dest.left;
                dY = dest.top + (dest.height() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
                break;
            case LogPower.END_CHG_ROTATION /*130*/:
                sX = source.left + (source.width() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
                sY = source.bottom;
                dX = dest.left + (dest.width() / TRANSCRIPT_MODE_ALWAYS_SCROLL);
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
            return PROFILE_SCROLLING;
        }
        boolean handled = PROFILE_SCROLLING;
        boolean okToSend = true;
        switch (keyCode) {
            case TOUCH_MODE_FLING /*4*/:
                if (this.mFiltered && this.mPopup != null && this.mPopup.isShowing()) {
                    if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                        DispatcherState state = getKeyDispatcherState();
                        if (state != null) {
                            state.startTracking(event, this);
                        }
                        handled = true;
                    } else if (event.getAction() == TRANSCRIPT_MODE_NORMAL && event.isTracking() && !event.isCanceled()) {
                        handled = true;
                        this.mTextFilter.setText((CharSequence) "");
                    }
                }
                okToSend = PROFILE_SCROLLING;
                break;
            case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
            case CHECK_POSITION_SEARCH_DISTANCE /*20*/:
            case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
            case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
            case HwPerformance.PERF_TAG_DEF_B_CPU_MIN /*23*/:
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                okToSend = PROFILE_SCROLLING;
                break;
            case RILConstants.RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION /*62*/:
                okToSend = this.mFiltered;
                break;
        }
        if (okToSend) {
            createTextFilter(true);
            KeyEvent forwardEvent = event;
            if (event.getRepeatCount() > 0) {
                forwardEvent = KeyEvent.changeTimeRepeat(event, event.getEventTime(), TRANSCRIPT_MODE_DISABLED);
            }
            switch (event.getAction()) {
                case TRANSCRIPT_MODE_DISABLED /*0*/:
                    handled = this.mTextFilter.onKeyDown(keyCode, forwardEvent);
                    break;
                case TRANSCRIPT_MODE_NORMAL /*1*/:
                    handled = this.mTextFilter.onKeyUp(keyCode, forwardEvent);
                    break;
                case TRANSCRIPT_MODE_ALWAYS_SCROLL /*2*/:
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
            this.mDefInputConnection = new BaseInputConnection((View) this, (boolean) PROFILE_SCROLLING);
            this.mPublicInputConnection = new InputConnectionWrapper(outAttrs);
        }
        outAttrs.inputType = LogPower.MEDIA_RECORDER_START;
        outAttrs.imeOptions = TOUCH_MODE_OVERFLING;
        return this.mPublicInputConnection;
    }

    public boolean checkInputConnectionProxy(View view) {
        return view == this.mTextFilter ? true : PROFILE_SCROLLING;
    }

    private void createTextFilter(boolean animateEntrance) {
        if (this.mPopup == null) {
            PopupWindow p = new PopupWindow(getContext());
            p.setFocusable(PROFILE_SCROLLING);
            p.setTouchable(PROFILE_SCROLLING);
            p.setInputMethodMode(TRANSCRIPT_MODE_ALWAYS_SCROLL);
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
            this.mTextFilter.setRawInputType(LogPower.MEDIA_RECORDER_START);
            this.mTextFilter.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            this.mTextFilter.addTextChangedListener(this);
        }
        return this.mTextFilter;
    }

    public void clearTextFilter() {
        if (this.mFiltered) {
            getTextFilterInput().setText((CharSequence) "");
            this.mFiltered = PROFILE_SCROLLING;
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
                this.mFiltered = PROFILE_SCROLLING;
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
            this.mResurrectToPosition = TOUCH_MODE_UNKNOWN;
            resurrectSelection();
        }
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(TOUCH_MODE_UNKNOWN, -2, TRANSCRIPT_MODE_DISABLED);
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public /* bridge */ /* synthetic */ android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return generateLayoutParams(attrs);
    }

    public LayoutParams m17generateLayoutParams(AttributeSet attrs) {
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
            for (int i = TRANSCRIPT_MODE_DISABLED; i < count; i += TRANSCRIPT_MODE_NORMAL) {
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
        for (int i = TRANSCRIPT_MODE_DISABLED; i < childCount; i += TRANSCRIPT_MODE_NORMAL) {
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
        if (this.mRemoteAdapter == null || !new FilterComparison(intent).equals(new FilterComparison(this.mRemoteAdapter.getRemoteViewsServiceIntent()))) {
            this.mDeferNotifyDataSetChanged = PROFILE_SCROLLING;
            this.mRemoteAdapter = new RemoteViewsAdapter(getContext(), intent, this);
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
                this.mDeferNotifyDataSetChanged = PROFILE_SCROLLING;
            }
            return PROFILE_SCROLLING;
        } else if (this.mRemoteAdapter == null) {
            return PROFILE_SCROLLING;
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
        view.measure(this.mWidthMeasureSpec, TRANSCRIPT_MODE_DISABLED);
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
                this.mLayoutMode = TOUCH_MODE_FLING;
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
        if (tag != null && "disable-multi-select-move".equals(tag.toString())) {
            setIgnoreScrollMultiSelectStub();
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

    public /* bridge */ /* synthetic */ Adapter getAdapter() {
        return getAdapter();
    }

    public ListAdapter m18getAdapter() {
        return this.mAdapter;
    }

    public SparseBooleanArray getCheckStates() {
        return this.mCheckStates;
    }

    protected void setIgnoreScrollMultiSelectStub() {
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
            return Math.min(((getHeight() - this.mPaddingBottom) - this.mPaddingTop) + TOUCH_MODE_UNKNOWN, delta);
        }
        return Math.max(-(((getHeight() - this.mPaddingBottom) - this.mPaddingTop) + TOUCH_MODE_UNKNOWN), delta);
    }

    protected boolean hasScrollMultiSelectMask() {
        return PROFILE_SCROLLING;
    }

    protected boolean hasSpringAnimatorMask() {
        return PROFILE_SCROLLING;
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
