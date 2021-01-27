package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.app.slice.Slice;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.iawareperf.IHwRtgSchedImpl;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;
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
import android.view.ViewRootImpl;
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
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.RemoteViews;
import android.widget.RemoteViewsAdapter;
import com.android.internal.R;
import com.huawei.emui.hiexperience.hwperf.speedloader.HwPerfSpeedLoader;
import huawei.android.widget.HwOnEditEventListener;
import huawei.android.widget.HwOnMultiSelectListener;
import huawei.android.widget.HwOnScrollListener;
import huawei.android.widget.HwOnSearchEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;

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
    static final int OVERSCROLL_LIMIT_DIVISOR = 3;
    private static final boolean PROFILE_FLINGING = false;
    private static final boolean PROFILE_SCROLLING = false;
    private static final int SHIFT_POSITION_SIZE = 2;
    private static final int SLOW_DOWN_INTERPOLATOR_FOR_LAST_ANIMATION = 10;
    private static final int SLOW_DOWN_SCREEN_NUMBER = 2;
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
    private static final int UP_DIRECTION = -1;
    static final Interpolator sLinearInterpolator = new LinearInterpolator();
    private ListItemAccessibilityDelegate mAccessibilityDelegate;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mActivePointerId;
    @UnsupportedAppUsage
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
    @UnsupportedAppUsage
    ActionMode mChoiceActionMode;
    int mChoiceMode;
    private Runnable mClearScrollingCache;
    @UnsupportedAppUsage
    private ContextMenu.ContextMenuInfo mContextMenuInfo;
    @UnsupportedAppUsage
    AdapterDataSetObserver mDataSetObserver;
    private InputConnection mDefInputConnection;
    private boolean mDeferNotifyDataSetChanged;
    private float mDensityScale;
    private int mDirection;
    boolean mDrawSelectorOnTop;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123768444)
    private EdgeEffect mEdgeGlowBottom;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769408)
    private EdgeEffect mEdgeGlowTop;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123768941)
    private FastScroller mFastScroll;
    boolean mFastScrollAlwaysVisible;
    boolean mFastScrollEnabled;
    private int mFastScrollStyle;
    private boolean mFiltered;
    private int mFirstPositionDistanceGuess;
    private boolean mFlingProfilingStarted;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private FlingRunnable mFlingRunnable;
    private StrictMode.Span mFlingStrictSpan;
    private boolean mForceTranscriptScroll;
    private boolean mGlobalLayoutListenerAddedFilter;
    private boolean mHasPerformedLongPress;
    private HwCompoundEventDetector mHwCompoundEventDetector;
    private HwGenericEventDetector mHwGenericEventDetector;
    private HwKeyEventDetector mHwKeyEventDetector;
    HwParallelWorker mHwParallelWorker;
    private HwPerfSpeedLoader mHwPerfSpeedLoader;
    private IHwWechatOptimize mIHwWechatOptimize;
    protected boolean mIsAutoScroll;
    @UnsupportedAppUsage
    private boolean mIsChildViewEnabled;
    private boolean mIsDetaching;
    private boolean mIsMultiChoiceContinuousEnable;
    private boolean mIsMultiChoiceEnable;
    final boolean[] mIsScrap;
    private boolean mIsSelectContinuous;
    private boolean mIsSelectDiscrete;
    private int mLastAccessibilityScrollEventFromIndex;
    private int mLastAccessibilityScrollEventToIndex;
    private int mLastHandledItemCount;
    private int mLastPositionDistanceGuess;
    private int mLastScrollState;
    private int mLastTouchMode;
    int mLastY;
    @UnsupportedAppUsage
    int mLayoutMode;
    Rect mListPadding;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124051740)
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    int mMotionCorrection;
    @UnsupportedAppUsage
    int mMotionPosition;
    int mMotionViewNewTop;
    int mMotionViewOriginalTop;
    int mMotionX;
    @UnsupportedAppUsage
    int mMotionY;
    MultiChoiceModeWrapper mMultiChoiceModeCallback;
    protected boolean mMultiSelectAutoScrollFlag;
    private int mNestedYOffset;
    private HwOnScrollListener mOnGenericMotionScrollListener;
    private HwOnMultiSelectListener mOnMultiSelectListener;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769353)
    private OnScrollListener mOnScrollListener;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769379)
    int mOverflingDistance;
    @UnsupportedAppUsage
    int mOverscrollDistance;
    int mOverscrollMax;
    private final Thread mOwnerThread;
    private CheckForKeyLongPress mPendingCheckForKeyLongPress;
    @UnsupportedAppUsage
    private CheckForLongPress mPendingCheckForLongPress;
    @UnsupportedAppUsage
    private CheckForTap mPendingCheckForTap;
    private SavedState mPendingSync;
    private PerformClick mPerformClick;
    @UnsupportedAppUsage
    PopupWindow mPopup;
    private boolean mPopupHidden;
    Runnable mPositionScrollAfterLayout;
    @UnsupportedAppUsage
    AbsPositionScroller mPositionScroller;
    private InputConnectionWrapper mPublicInputConnection;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769398)
    final RecycleBin mRecycler;
    private RemoteViewsAdapter mRemoteAdapter;
    int mResurrectToPosition;
    int mSavedTouchDownMotionPosition;
    private final int[] mScrollConsumed;
    View mScrollDown;
    private final int[] mScrollOffset;
    private boolean mScrollProfilingStarted;
    private StrictMode.Span mScrollStrictSpan;
    View mScrollUp;
    boolean mScrollingCacheEnabled;
    int mSelectedTop;
    @UnsupportedAppUsage
    int mSelectionBottomPadding;
    int mSelectionLeftPadding;
    int mSelectionRightPadding;
    @UnsupportedAppUsage
    int mSelectionTopPadding;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    Drawable mSelector;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    int mSelectorPosition;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    Rect mSelectorRect;
    private int[] mSelectorState;
    private int mShiftPositionFirst;
    private int mShiftPositionSecond;
    private boolean mSmoothScrollbarEnabled;
    boolean mStackFromBottom;
    EditText mTextFilter;
    private boolean mTextFilterEnabled;
    private final float[] mTmpPoint;
    private Rect mTouchFrame;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769413)
    int mTouchMode;
    private Runnable mTouchModeReset;
    @UnsupportedAppUsage
    private int mTouchSlop;
    private int mTranscriptMode;
    private float mVelocityScale;
    @UnsupportedAppUsage
    private VelocityTracker mVelocityTracker;
    private float mVerticalScrollFactor;
    int mWidthMeasureSpec;

    public interface MultiChoiceModeListener extends ActionMode.Callback {
        void onItemCheckedStateChanged(ActionMode actionMode, int i, long j, boolean z);
    }

    public interface OnScrollListener {
        public static final int SCROLL_STATE_FLING = 2;
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_TOUCH_SCROLL = 1;

        void onScroll(AbsListView absListView, int i, int i2, int i3);

        void onScrollStateChanged(AbsListView absListView, int i);
    }

    public interface RecyclerListener {
        void onMovedToScrapHeap(View view);
    }

    public interface SelectionBoundsAdjuster {
        void adjustListItemSelectionBounds(Rect rect);
    }

    /* access modifiers changed from: package-private */
    public abstract void fillGap(boolean z);

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public abstract int findMotionRow(int i);

    /* access modifiers changed from: package-private */
    public abstract void setSelectionInt(int i);

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<AbsListView> {
        private int mCacheColorHintId;
        private int mChoiceModeId;
        private int mDrawSelectorOnTopId;
        private int mFastScrollEnabledId;
        private int mListSelectorId;
        private boolean mPropertiesMapped = false;
        private int mScrollingCacheId;
        private int mSmoothScrollbarId;
        private int mStackFromBottomId;
        private int mTextFilterEnabledId;
        private int mTranscriptModeId;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mCacheColorHintId = propertyMapper.mapColor("cacheColorHint", 16843009);
            SparseArray<String> choiceModeEnumMapping = new SparseArray<>();
            choiceModeEnumMapping.put(0, "none");
            choiceModeEnumMapping.put(1, "singleChoice");
            choiceModeEnumMapping.put(2, "multipleChoice");
            choiceModeEnumMapping.put(3, "multipleChoiceModal");
            Objects.requireNonNull(choiceModeEnumMapping);
            this.mChoiceModeId = propertyMapper.mapIntEnum("choiceMode", 16843051, new IntFunction() {
                /* class android.widget.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mDrawSelectorOnTopId = propertyMapper.mapBoolean("drawSelectorOnTop", 16843004);
            this.mFastScrollEnabledId = propertyMapper.mapBoolean("fastScrollEnabled", 16843302);
            this.mListSelectorId = propertyMapper.mapObject("listSelector", 16843003);
            this.mScrollingCacheId = propertyMapper.mapBoolean("scrollingCache", 16843006);
            this.mSmoothScrollbarId = propertyMapper.mapBoolean("smoothScrollbar", 16843313);
            this.mStackFromBottomId = propertyMapper.mapBoolean("stackFromBottom", 16843005);
            this.mTextFilterEnabledId = propertyMapper.mapBoolean("textFilterEnabled", 16843007);
            SparseArray<String> transcriptModeEnumMapping = new SparseArray<>();
            transcriptModeEnumMapping.put(0, "disabled");
            transcriptModeEnumMapping.put(1, "normal");
            transcriptModeEnumMapping.put(2, "alwaysScroll");
            Objects.requireNonNull(transcriptModeEnumMapping);
            this.mTranscriptModeId = propertyMapper.mapIntEnum("transcriptMode", 16843008, new IntFunction() {
                /* class android.widget.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mPropertiesMapped = true;
        }

        public void readProperties(AbsListView node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readColor(this.mCacheColorHintId, node.getCacheColorHint());
                propertyReader.readIntEnum(this.mChoiceModeId, node.getChoiceMode());
                propertyReader.readBoolean(this.mDrawSelectorOnTopId, node.isDrawSelectorOnTop());
                propertyReader.readBoolean(this.mFastScrollEnabledId, node.isFastScrollEnabled());
                propertyReader.readObject(this.mListSelectorId, node.getSelector());
                propertyReader.readBoolean(this.mScrollingCacheId, node.isScrollingCacheEnabled());
                propertyReader.readBoolean(this.mSmoothScrollbarId, node.isSmoothScrollbarEnabled());
                propertyReader.readBoolean(this.mStackFromBottomId, node.isStackFromBottom());
                propertyReader.readBoolean(this.mTextFilterEnabledId, node.isTextFilterEnabled());
                propertyReader.readIntEnum(this.mTranscriptModeId, node.getTranscriptMode());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performSelectContinuous() {
        int i = this.mShiftPositionSecond;
        if (!(i == -1 || i == this.mMotionPosition)) {
            setItemsChecked(false);
        }
        int i2 = this.mShiftPositionSecond;
        int i3 = this.mMotionPosition;
        if (i2 != i3) {
            this.mShiftPositionSecond = i3;
            setItemsChecked(true);
        }
    }

    public AbsListView(Context context) {
        super(context);
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
        this.mEdgeGlowTop = new EdgeEffect(this.mContext);
        this.mEdgeGlowBottom = new EdgeEffect(this.mContext);
        this.mDirection = 0;
        this.mHwKeyEventDetector = null;
        this.mShiftPositionFirst = -1;
        this.mShiftPositionSecond = -1;
        this.mOnMultiSelectListener = new HwOnMultiSelectListener() {
            /* class android.widget.AbsListView.AnonymousClass1 */

            @Override // huawei.android.widget.HwOnMultiSelectListener
            public boolean onSelectContinuous(boolean isBeginPos, MotionEvent event) {
                if (!AbsListView.this.mIsMultiChoiceContinuousEnable) {
                    return false;
                }
                if (((AbsListView.this.mChoiceMode != 3 || AbsListView.this.mChoiceActionMode == null) && AbsListView.this.mChoiceMode != 2) || AbsListView.this.mMotionPosition == -1) {
                    return false;
                }
                AbsListView.this.mIsSelectContinuous = true;
                return true;
            }

            @Override // huawei.android.widget.HwOnMultiSelectListener
            public boolean onSelectDiscrete(MotionEvent event) {
                if (!AbsListView.this.mIsMultiChoiceEnable || AbsListView.this.mChoiceMode != 3 || AbsListView.this.mChoiceActionMode != null || AbsListView.this.mMotionPosition == -1) {
                    return false;
                }
                AbsListView.this.mIsSelectDiscrete = true;
                return true;
            }

            @Override // huawei.android.widget.HwOnMultiSelectListener
            public boolean onCancel(MotionEvent event) {
                return false;
            }
        };
        this.mHwGenericEventDetector = null;
        this.mOnGenericMotionScrollListener = new HwOnScrollListener() {
            /* class android.widget.AbsListView.AnonymousClass2 */

            @Override // huawei.android.widget.HwOnScrollListener
            public boolean onScrollBy(float deltaX, float deltaY, MotionEvent event) {
                AbsListView.this.scrollListBy((int) deltaY);
                return true;
            }
        };
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
        this.mEdgeGlowTop = new EdgeEffect(this.mContext);
        this.mEdgeGlowBottom = new EdgeEffect(this.mContext);
        this.mDirection = 0;
        this.mHwKeyEventDetector = null;
        this.mShiftPositionFirst = -1;
        this.mShiftPositionSecond = -1;
        this.mOnMultiSelectListener = new HwOnMultiSelectListener() {
            /* class android.widget.AbsListView.AnonymousClass1 */

            @Override // huawei.android.widget.HwOnMultiSelectListener
            public boolean onSelectContinuous(boolean isBeginPos, MotionEvent event) {
                if (!AbsListView.this.mIsMultiChoiceContinuousEnable) {
                    return false;
                }
                if (((AbsListView.this.mChoiceMode != 3 || AbsListView.this.mChoiceActionMode == null) && AbsListView.this.mChoiceMode != 2) || AbsListView.this.mMotionPosition == -1) {
                    return false;
                }
                AbsListView.this.mIsSelectContinuous = true;
                return true;
            }

            @Override // huawei.android.widget.HwOnMultiSelectListener
            public boolean onSelectDiscrete(MotionEvent event) {
                if (!AbsListView.this.mIsMultiChoiceEnable || AbsListView.this.mChoiceMode != 3 || AbsListView.this.mChoiceActionMode != null || AbsListView.this.mMotionPosition == -1) {
                    return false;
                }
                AbsListView.this.mIsSelectDiscrete = true;
                return true;
            }

            @Override // huawei.android.widget.HwOnMultiSelectListener
            public boolean onCancel(MotionEvent event) {
                return false;
            }
        };
        this.mHwGenericEventDetector = null;
        this.mOnGenericMotionScrollListener = new HwOnScrollListener() {
            /* class android.widget.AbsListView.AnonymousClass2 */

            @Override // huawei.android.widget.HwOnScrollListener
            public boolean onScrollBy(float deltaX, float deltaY, MotionEvent event) {
                AbsListView.this.scrollListBy((int) deltaY);
                return true;
            }
        };
        initAbsListView();
        this.mOwnerThread = Thread.currentThread();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AbsListView, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.AbsListView, attrs, a, defStyleAttr, defStyleRes);
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
        int i;
        int i2 = this.mAddItemViewPosition;
        if (i2 == -1 || i2 != position || (i = this.mAddItemViewType) == -10000) {
            return this.mAdapter.getItemViewType(position);
        }
        return i;
    }

    public boolean isItemChecked(int position) {
        SparseBooleanArray sparseBooleanArray;
        if (this.mChoiceMode == 0 || (sparseBooleanArray = this.mCheckStates) == null) {
            return false;
        }
        return sparseBooleanArray.get(position);
    }

    public int getCheckedItemPosition() {
        SparseBooleanArray sparseBooleanArray;
        if (this.mChoiceMode == 1 && (sparseBooleanArray = this.mCheckStates) != null && sparseBooleanArray.size() == 1) {
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
        SparseBooleanArray sparseBooleanArray = this.mCheckStates;
        if (sparseBooleanArray != null) {
            sparseBooleanArray.clear();
        }
        LongSparseArray<Integer> longSparseArray = this.mCheckedIdStates;
        if (longSparseArray != null) {
            longSparseArray.clear();
        }
        this.mCheckedItemCount = 0;
    }

    public void setItemChecked(int position, boolean value) {
        boolean itemCheckChanged;
        int i = this.mChoiceMode;
        if (i != 0) {
            if (value && i == 3 && this.mChoiceActionMode == null) {
                MultiChoiceModeWrapper multiChoiceModeWrapper = this.mMultiChoiceModeCallback;
                if (multiChoiceModeWrapper == null || !multiChoiceModeWrapper.hasWrappedCallback()) {
                    throw new IllegalStateException("AbsListView: attempted to start selection mode for CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was supplied. Call setMultiChoiceModeListener to set a callback.");
                }
                this.mChoiceActionMode = startActionMode(this.mMultiChoiceModeCallback);
            }
            int i2 = this.mChoiceMode;
            boolean z = false;
            if (i2 == 2 || i2 == 3) {
                updateShiftPositions(position);
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

    @Override // android.widget.AdapterView
    public boolean performItemClick(View view, int position, long id) {
        boolean handled = false;
        boolean dispatchItemClick = true;
        int i = this.mChoiceMode;
        if (i != 0) {
            boolean checkedStateChanged = false;
            if (i == 2 || (i == 3 && this.mChoiceActionMode != null)) {
                updateShiftPositions(position);
                boolean checked = getCheckedStateForMultiSelect(!this.mCheckStates.get(position, false));
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
                ActionMode actionMode = this.mChoiceActionMode;
                if (actionMode != null) {
                    this.mMultiChoiceModeCallback.onItemCheckedStateChanged(actionMode, position, id, checked);
                    dispatchItemClick = false;
                }
                checkedStateChanged = true;
            } else if (this.mChoiceMode == 1) {
                if (!this.mCheckStates.get(position, false)) {
                    this.mCheckStates.clear();
                    this.mCheckStates.put(position, true);
                    if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                        this.mCheckedIdStates.clear();
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
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
            handled = true;
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
        ListAdapter listAdapter;
        if (choiceMode == 8) {
            this.mMultiSelectAutoScrollFlag = true;
            return;
        }
        this.mChoiceMode = choiceMode;
        ActionMode actionMode = this.mChoiceActionMode;
        if (actionMode != null) {
            actionMode.finish();
            clearShiftPositions();
            this.mChoiceActionMode = null;
        }
        if (this.mChoiceMode != 0) {
            if (this.mCheckStates == null) {
                this.mCheckStates = new SparseBooleanArray(0);
            }
            if (this.mCheckedIdStates == null && (listAdapter = this.mAdapter) != null && listAdapter.hasStableIds()) {
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
    /* access modifiers changed from: public */
    private boolean contentFits() {
        int childCount = getChildCount();
        if (childCount == 0) {
            return true;
        }
        if (childCount != this.mItemCount) {
            return false;
        }
        if (getChildAt(0).getTop() < this.mListPadding.top || getChildAt(childCount - 1).getBottom() > getHeight() - this.mListPadding.bottom) {
            return false;
        }
        return true;
    }

    public void setFastScrollEnabled(final boolean enabled) {
        if (this.mFastScrollEnabled != enabled) {
            this.mFastScrollEnabled = enabled;
            if (isOwnerThread()) {
                setFastScrollerEnabledUiThread(enabled);
            } else {
                post(new Runnable() {
                    /* class android.widget.AbsListView.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        AbsListView.this.setFastScrollerEnabledUiThread(enabled);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setFastScrollerEnabledUiThread(boolean enabled) {
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller != null) {
            fastScroller.setEnabled(enabled);
        } else if (enabled) {
            this.mFastScroll = (FastScroller) HwWidgetFactory.getHwFastScroller(this, this.mFastScrollStyle, this.mContext);
            this.mFastScroll.setEnabled(true);
        }
        resolvePadding();
        FastScroller fastScroller2 = this.mFastScroll;
        if (fastScroller2 != null) {
            fastScroller2.updateLayout();
        }
    }

    public void setFastScrollStyle(int styleResId) {
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller == null) {
            this.mFastScrollStyle = styleResId;
        } else {
            fastScroller.setStyle(styleResId);
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
                    /* class android.widget.AbsListView.AnonymousClass4 */

                    @Override // java.lang.Runnable
                    public void run() {
                        AbsListView.this.setFastScrollerAlwaysVisibleUiThread(alwaysShow);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setFastScrollerAlwaysVisibleUiThread(boolean alwaysShow) {
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller != null) {
            fastScroller.setAlwaysShow(alwaysShow);
        }
    }

    private boolean isOwnerThread() {
        return this.mOwnerThread == Thread.currentThread();
    }

    public boolean isFastScrollAlwaysVisible() {
        FastScroller fastScroller = this.mFastScroll;
        return fastScroller == null ? this.mFastScrollEnabled && this.mFastScrollAlwaysVisible : fastScroller.isEnabled() && this.mFastScroll.isAlwaysShowEnabled();
    }

    @Override // android.view.View
    public int getVerticalScrollbarWidth() {
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller == null || !fastScroller.isEnabled()) {
            return super.getVerticalScrollbarWidth();
        }
        return Math.max(super.getVerticalScrollbarWidth(), this.mFastScroll.getWidth());
    }

    @ViewDebug.ExportedProperty
    public boolean isFastScrollEnabled() {
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller == null) {
            return this.mFastScrollEnabled;
        }
        return fastScroller.isEnabled();
    }

    @Override // android.view.View
    public void setVerticalScrollbarPosition(int position) {
        super.setVerticalScrollbarPosition(position);
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller != null) {
            fastScroller.setScrollbarPosition(position);
        }
    }

    @Override // android.view.View
    public void setScrollBarStyle(int style) {
        super.setScrollBarStyle(style);
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller != null) {
            fastScroller.setScrollBarStyle(style);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    @UnsupportedAppUsage
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
    @UnsupportedAppUsage
    public void invokeOnItemScrollListener() {
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller != null) {
            fastScroller.onScroll(this.mFirstPosition, getChildCount(), this.mItemCount);
        }
        OnScrollListener onScrollListener = this.mOnScrollListener;
        if (onScrollListener != null) {
            onScrollListener.onScroll(this, this.mFirstPosition, getChildCount(), this.mItemCount);
        }
        onScrollChanged(0, 0, 0, 0);
    }

    @Override // android.view.View, android.view.accessibility.AccessibilityEventSource
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

    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return AbsListView.class.getName();
    }

    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
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
        int choiceMode = getChoiceMode();
        if (choiceMode == 0) {
            return 0;
        }
        if (choiceMode != 1) {
            return (choiceMode == 2 || choiceMode == 3) ? 2 : 0;
        }
        return 1;
    }

    @Override // android.view.View
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

    @Override // android.view.View
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

    /* access modifiers changed from: package-private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class android.widget.AbsListView.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        LongSparseArray<Integer> checkIdState;
        SparseBooleanArray checkState;
        int checkedItemCount;
        String filter;
        @UnsupportedAppUsage
        long firstId;
        int height;
        boolean inActionMode;
        int position;
        long selectedId;
        int shiftPositionFirst;
        int shiftPositionSecond;
        @UnsupportedAppUsage
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
            this.shiftPositionFirst = in.readInt();
            this.shiftPositionSecond = in.readInt();
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
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
            LongSparseArray<Integer> longSparseArray = this.checkIdState;
            int N = longSparseArray != null ? longSparseArray.size() : 0;
            out.writeInt(N);
            for (int i = 0; i < N; i++) {
                out.writeLong(this.checkIdState.keyAt(i));
                out.writeInt(this.checkIdState.valueAt(i).intValue());
            }
            out.writeInt(this.shiftPositionFirst);
            out.writeInt(this.shiftPositionSecond);
        }

        public String toString() {
            return "AbsListView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " selectedId=" + this.selectedId + " firstId=" + this.firstId + " viewTop=" + this.viewTop + " position=" + this.position + " height=" + this.height + " filter=" + this.filter + " checkState=" + this.checkState + "}";
        }
    }

    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        EditText textFilter;
        Editable filterText;
        dismissPopup();
        SavedState ss = new SavedState(super.onSaveInstanceState());
        SavedState savedState = this.mPendingSync;
        if (savedState != null) {
            ss.selectedId = savedState.selectedId;
            ss.firstId = this.mPendingSync.firstId;
            ss.viewTop = this.mPendingSync.viewTop;
            ss.position = this.mPendingSync.position;
            ss.height = this.mPendingSync.height;
            ss.filter = this.mPendingSync.filter;
            ss.inActionMode = this.mPendingSync.inActionMode;
            ss.checkedItemCount = this.mPendingSync.checkedItemCount;
            ss.checkState = this.mPendingSync.checkState;
            ss.checkIdState = this.mPendingSync.checkIdState;
            ss.shiftPositionSecond = this.mPendingSync.shiftPositionSecond;
            ss.shiftPositionFirst = this.mPendingSync.shiftPositionFirst;
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
        if (!(!this.mFiltered || (textFilter = this.mTextFilter) == null || (filterText = textFilter.getText()) == null)) {
            ss.filter = filterText.toString();
        }
        if (this.mChoiceMode != 3 || this.mChoiceActionMode == null) {
            z = false;
        }
        ss.inActionMode = z;
        SparseBooleanArray sparseBooleanArray = this.mCheckStates;
        if (sparseBooleanArray != null) {
            ss.checkState = sparseBooleanArray.clone();
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
        RemoteViewsAdapter remoteViewsAdapter = this.mRemoteAdapter;
        if (remoteViewsAdapter != null) {
            remoteViewsAdapter.saveRemoteViewsCache();
        }
        ss.shiftPositionFirst = this.mShiftPositionFirst;
        ss.shiftPositionSecond = this.mShiftPositionSecond;
        return ss;
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        MultiChoiceModeWrapper multiChoiceModeWrapper;
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
        if (ss.inActionMode && this.mChoiceMode == 3 && (multiChoiceModeWrapper = this.mMultiChoiceModeCallback) != null) {
            this.mChoiceActionMode = startActionMode(multiChoiceModeWrapper);
        }
        this.mShiftPositionFirst = ss.shiftPositionFirst;
        this.mShiftPositionSecond = ss.shiftPositionSecond;
        requestLayout();
    }

    private boolean acceptFilter() {
        return this.mTextFilterEnabled && (getAdapter() instanceof Filterable) && ((Filterable) getAdapter()).getFilter() != null;
    }

    public void setFilterText(String filterText) {
        if (this.mTextFilterEnabled && !TextUtils.isEmpty(filterText)) {
            createTextFilter(false);
            this.mTextFilter.setText(filterText);
            this.mTextFilter.setSelection(filterText.length());
            ListAdapter listAdapter = this.mAdapter;
            if (listAdapter instanceof Filterable) {
                if (this.mPopup == null) {
                    ((Filterable) listAdapter).getFilter().filter(filterText);
                }
                this.mFiltered = true;
                this.mDataSetObserver.clearSavedState();
            }
        }
    }

    public CharSequence getTextFilter() {
        EditText editText;
        if (!this.mTextFilterEnabled || (editText = this.mTextFilter) == null) {
            return null;
        }
        return editText.getText();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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

    @Override // android.view.View, android.view.ViewParent
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
    @Override // android.view.View
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
            return extent - (((bottom - getHeight()) * 100) / height2);
        }
        return extent;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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
    @Override // android.view.View
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
    @Override // android.view.View
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
    @Override // android.view.View
    public float getBottomFadingEdgeStrength() {
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
            return ((float) ((bottom - height) + this.mPaddingBottom)) / fadeLength;
        }
        return fadeEdge;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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
    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
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
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller != null) {
            fastScroller.onItemCountChanged(getChildCount(), this.mItemCount);
        }
        this.mInLayout = false;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean setFrame(int left, int top, int right, int bottom) {
        PopupWindow popupWindow;
        boolean changed = super.setFrame(left, top, right, bottom);
        if (changed) {
            boolean visible = getWindowVisibility() == 0;
            if (this.mFiltered && visible && (popupWindow = this.mPopup) != null && popupWindow.isShowing()) {
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
        View view = this.mScrollUp;
        int i = 0;
        if (view != null) {
            view.setVisibility(canScrollUp() ? 0 : 4);
        }
        View view2 = this.mScrollDown;
        if (view2 != null) {
            if (!canScrollDown()) {
                i = 4;
            }
            view2.setVisibility(i);
        }
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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

    @Override // android.widget.AdapterView
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
        View updatedView;
        Trace.traceBegin(8, "obtainView");
        long obtainViewStartTime = System.nanoTime();
        IHwRtgSchedImpl hwRtgSchedImpl = HwFrameworkFactory.getHwRtgSchedImpl();
        if (hwRtgSchedImpl != null) {
            Trace.traceBegin(8, "ObtainView Message Send");
            hwRtgSchedImpl.doObtainView();
            Trace.traceEnd(8);
        }
        outMetadata[0] = false;
        View transientView = this.mRecycler.getTransientStateView(position);
        if (transientView != null) {
            if (((LayoutParams) transientView.getLayoutParams()).viewType == this.mAdapter.getItemViewType(position) && (updatedView = this.mAdapter.getView(position, transientView, this)) != transientView) {
                setItemViewLayoutParams(updatedView, position);
                this.mRecycler.addScrapView(updatedView, position);
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
        int i = this.mCacheColorHint;
        if (i != 0) {
            child.setDrawingCacheBackgroundColor(i);
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
        HwParallelWorker hwParallelWorker = this.mHwParallelWorker;
        if (hwParallelWorker != null && hwParallelWorker.isPrefetchOptimizeEnable()) {
            this.mHwParallelWorker.recordObtainViewTimeDelay(System.nanoTime() - obtainViewStartTime);
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

    /* access modifiers changed from: package-private */
    public class ListItemAccessibilityDelegate extends View.AccessibilityDelegate {
        ListItemAccessibilityDelegate() {
        }

        @Override // android.view.View.AccessibilityDelegate
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            AbsListView.this.onInitializeAccessibilityNodeInfoForItem(host, AbsListView.this.getPositionForView(host), info);
        }

        @Override // android.view.View.AccessibilityDelegate
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

    public void onInitializeAccessibilityNodeInfoForItem(View view, int position, AccessibilityNodeInfo info) {
        boolean isItemEnabled;
        ListAdapter adapter = (ListAdapter) getAdapter();
        if (position != -1 && adapter != null && position <= adapter.getCount() - 1) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof LayoutParams) {
                isItemEnabled = ((LayoutParams) lp).isEnabled && isEnabled();
            } else {
                isItemEnabled = false;
            }
            info.setEnabled(isItemEnabled);
            if (position == getSelectedItemPosition()) {
                info.setSelected(true);
                addAccessibilityActionIfEnabled(info, isItemEnabled, AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_SELECTION);
            } else {
                addAccessibilityActionIfEnabled(info, isItemEnabled, AccessibilityNodeInfo.AccessibilityAction.ACTION_SELECT);
            }
            if (isItemClickable(view)) {
                addAccessibilityActionIfEnabled(info, isItemEnabled, AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
                info.setClickable(true);
            }
            if (isLongClickable()) {
                addAccessibilityActionIfEnabled(info, isItemEnabled, AccessibilityNodeInfo.AccessibilityAction.ACTION_LONG_CLICK);
                info.setLongClickable(true);
            }
        }
    }

    private void addAccessibilityActionIfEnabled(AccessibilityNodeInfo info, boolean enabled, AccessibilityNodeInfo.AccessibilityAction action) {
        if (enabled) {
            info.addAction(action);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isItemClickable(View view) {
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

    @UnsupportedAppUsage
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

    /* JADX INFO: Multiple debug info for r3v0 boolean: [D('scrollX' int), D('drawSelectorOnTop' boolean)] */
    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
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
            this.mGroupFlags = 34 | this.mGroupFlags;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean isPaddingOffsetRequired() {
        return (this.mGroupFlags & 34) != 34;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int getLeftPaddingOffset() {
        if ((this.mGroupFlags & 34) == 34) {
            return 0;
        }
        return -this.mPaddingLeft;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int getTopPaddingOffset() {
        if ((this.mGroupFlags & 34) == 34) {
            return 0;
        }
        return -this.mPaddingTop;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int getRightPaddingOffset() {
        if ((this.mGroupFlags & 34) == 34) {
            return 0;
        }
        return this.mPaddingRight;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int getBottomPaddingOffset() {
        if ((this.mGroupFlags & 34) == 34) {
            return 0;
        }
        return this.mPaddingBottom;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void internalSetPadding(int left, int top, int right, int bottom) {
        super.internalSetPadding(left, top, right, bottom);
        if (isLayoutRequested()) {
            handleBoundsChange();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        handleBoundsChange();
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller != null) {
            fastScroller.onSizeChanged(w, h, oldw, oldh);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleBoundsChange() {
        int childCount;
        if (!this.mInLayout && (childCount = getChildCount()) > 0) {
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

    /* access modifiers changed from: package-private */
    public boolean touchModeDrawsInPressedState() {
        int i = this.mTouchMode;
        if (i == 1 || i == 2) {
            return true;
        }
        return false;
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

    public boolean isDrawSelectorOnTop() {
        return this.mDrawSelectorOnTop;
    }

    public void setSelector(int resID) {
        setSelector(getContext().getDrawable(resID));
    }

    public void setSelector(Drawable sel) {
        Drawable drawable = this.mSelector;
        if (drawable != null) {
            drawable.setCallback(null);
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
            if (selector == null) {
                return;
            }
            if ((isFocused() || touchModeDrawsInPressedState()) && !selectorRect.isEmpty()) {
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
    @UnsupportedAppUsage
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
    @Override // android.view.ViewGroup, android.view.View
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

    @Override // android.view.View
    public boolean verifyDrawable(Drawable dr) {
        return this.mSelector == dr || super.verifyDrawable(dr);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        Drawable drawable = this.mSelector;
        if (drawable != null) {
            drawable.jumpToCurrentState();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
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
        HwCompoundEventDetector hwCompoundEventDetector = this.mHwCompoundEventDetector;
        if (hwCompoundEventDetector != null) {
            hwCompoundEventDetector.setOnMultiSelectEventListener(this, this.mOnMultiSelectListener);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        AdapterDataSetObserver adapterDataSetObserver;
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
        ListAdapter listAdapter = this.mAdapter;
        if (!(listAdapter == null || (adapterDataSetObserver = this.mDataSetObserver) == null)) {
            listAdapter.unregisterDataSetObserver(adapterDataSetObserver);
            this.mDataSetObserver = null;
        }
        StrictMode.Span span = this.mScrollStrictSpan;
        if (span != null) {
            span.finish();
            this.mScrollStrictSpan = null;
        }
        StrictMode.Span span2 = this.mFlingStrictSpan;
        if (span2 != null) {
            span2.finish();
            this.mFlingStrictSpan = null;
        }
        FlingRunnable flingRunnable = this.mFlingRunnable;
        if (flingRunnable != null) {
            removeCallbacks(flingRunnable);
        }
        AbsPositionScroller absPositionScroller = this.mPositionScroller;
        if (absPositionScroller != null) {
            absPositionScroller.stop();
        }
        Runnable runnable = this.mClearScrollingCache;
        if (runnable != null) {
            removeCallbacks(runnable);
        }
        PerformClick performClick = this.mPerformClick;
        if (performClick != null) {
            removeCallbacks(performClick);
        }
        Runnable runnable2 = this.mTouchModeReset;
        if (runnable2 != null) {
            removeCallbacks(runnable2);
            this.mTouchModeReset.run();
        }
        HwCompoundEventDetector hwCompoundEventDetector = this.mHwCompoundEventDetector;
        if (hwCompoundEventDetector != null) {
            hwCompoundEventDetector.onDetachedFromWindow();
        }
        this.mIsDetaching = false;
    }

    @Override // android.view.View
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        int touchMode = !isInTouchMode();
        if (!hasWindowFocus) {
            setChildrenDrawingCacheEnabled(false);
            FlingRunnable flingRunnable = this.mFlingRunnable;
            if (flingRunnable != null) {
                removeCallbacks(flingRunnable);
                this.mFlingRunnable.mSuppressIdleStateChangeCall = false;
                this.mFlingRunnable.endFling();
                AbsPositionScroller absPositionScroller = this.mPositionScroller;
                if (absPositionScroller != null) {
                    absPositionScroller.stop();
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
            int i = this.mLastTouchMode;
            if (!(touchMode == i || i == -1)) {
                if (touchMode == 1) {
                    resurrectSelection();
                } else {
                    hideSelector();
                    this.mLayoutMode = 0;
                    layoutChildren();
                }
            }
        }
        this.mLastTouchMode = touchMode;
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller != null) {
            fastScroller.setScrollbarPosition(getVerticalScrollbarPosition());
        }
    }

    /* access modifiers changed from: package-private */
    public ContextMenu.ContextMenuInfo createContextMenuInfo(View view, int position, long id) {
        return new AdapterView.AdapterContextMenuInfo(view, position, id);
    }

    @Override // android.view.View
    public void onCancelPendingInputEvents() {
        super.onCancelPendingInputEvents();
        PerformClick performClick = this.mPerformClick;
        if (performClick != null) {
            removeCallbacks(performClick);
        }
        CheckForTap checkForTap = this.mPendingCheckForTap;
        if (checkForTap != null) {
            removeCallbacks(checkForTap);
        }
        CheckForLongPress checkForLongPress = this.mPendingCheckForLongPress;
        if (checkForLongPress != null) {
            removeCallbacks(checkForLongPress);
        }
        CheckForKeyLongPress checkForKeyLongPress = this.mPendingCheckForKeyLongPress;
        if (checkForKeyLongPress != null) {
            removeCallbacks(checkForKeyLongPress);
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
            return AbsListView.this.getWindowAttachCount() == this.mOriginalAttachCount;
        }
    }

    /* access modifiers changed from: private */
    public class PerformClick extends WindowRunnnable implements Runnable {
        int mClickMotionPosition;

        private PerformClick() {
            super();
        }

        @Override // java.lang.Runnable
        public void run() {
            if (!AbsListView.this.mDataChanged) {
                ListAdapter adapter = AbsListView.this.mAdapter;
                int motionPosition = this.mClickMotionPosition;
                if (adapter != null && AbsListView.this.mItemCount > 0 && motionPosition != -1 && motionPosition < adapter.getCount() && sameWindow() && adapter.isEnabled(motionPosition)) {
                    AbsListView absListView = AbsListView.this;
                    View view = absListView.getChildAt(motionPosition - absListView.mFirstPosition);
                    if (view != null) {
                        AbsListView.this.performItemClick(view, motionPosition, adapter.getItemId(motionPosition));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class CheckForLongPress extends WindowRunnnable implements Runnable {
        private static final int INVALID_COORD = -1;
        private float mX;
        private float mY;

        private CheckForLongPress() {
            super();
            this.mX = -1.0f;
            this.mY = -1.0f;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setCoords(float x, float y) {
            this.mX = x;
            this.mY = y;
        }

        @Override // java.lang.Runnable
        public void run() {
            int motionPosition = AbsListView.this.mMotionPosition;
            AbsListView absListView = AbsListView.this;
            View child = absListView.getChildAt(motionPosition - absListView.mFirstPosition);
            if (child != null) {
                int longPressPosition = AbsListView.this.mMotionPosition;
                long longPressId = AbsListView.this.mAdapter.getItemId(AbsListView.this.mMotionPosition);
                boolean handled = false;
                if (sameWindow() && !AbsListView.this.mDataChanged && !AbsListView.this.getRootView().isLongPressSwipe()) {
                    float f = this.mX;
                    if (f != -1.0f) {
                        float f2 = this.mY;
                        if (f2 != -1.0f) {
                            handled = AbsListView.this.performLongPress(child, longPressPosition, longPressId, f, f2);
                        }
                    }
                    handled = AbsListView.this.performLongPress(child, longPressPosition, longPressId);
                }
                if (handled) {
                    AbsListView.this.mHasPerformedLongPress = true;
                    AbsListView absListView2 = AbsListView.this;
                    absListView2.mTouchMode = -1;
                    absListView2.setPressed(false);
                    child.setPressed(false);
                    return;
                }
                AbsListView.this.mTouchMode = 2;
            }
        }
    }

    private class CheckForKeyLongPress extends WindowRunnnable implements Runnable {
        private CheckForKeyLongPress() {
            super();
        }

        @Override // java.lang.Runnable
        public void run() {
            if (AbsListView.this.isPressed() && AbsListView.this.mSelectedPosition >= 0) {
                View v = AbsListView.this.getChildAt(AbsListView.this.mSelectedPosition - AbsListView.this.mFirstPosition);
                if (!AbsListView.this.mDataChanged) {
                    boolean handled = false;
                    if (sameWindow()) {
                        AbsListView absListView = AbsListView.this;
                        handled = absListView.performLongPress(v, absListView.mSelectedPosition, AbsListView.this.mSelectedRowId);
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

    private boolean performStylusButtonPressAction(MotionEvent ev) {
        View child;
        if (this.mChoiceMode != 3 || this.mChoiceActionMode != null || (child = getChildAt(this.mMotionPosition - this.mFirstPosition)) == null || !performLongPress(child, this.mMotionPosition, this.mAdapter.getItemId(this.mMotionPosition))) {
            return false;
        }
        this.mTouchMode = -1;
        setPressed(false);
        child.setPressed(false);
        return true;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean performLongPress(View child, int longPressPosition, long longPressId) {
        return performLongPress(child, longPressPosition, longPressId, -1.0f, -1.0f);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
    @Override // android.view.View
    public ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return this.mContextMenuInfo;
    }

    @Override // android.view.View
    public boolean showContextMenu() {
        return showContextMenuInternal(0.0f, 0.0f, false);
    }

    @Override // android.view.View
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

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean showContextMenuForChild(View originalView) {
        if (isShowingContextMenuWithCoords()) {
            return false;
        }
        return showContextMenuForChildInternal(originalView, 0.0f, 0.0f, false);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
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
        if (handled) {
            return handled;
        }
        this.mContextMenuInfo = createContextMenuInfo(getChildAt(longPressPosition - this.mFirstPosition), longPressPosition, longPressId);
        if (useOffsets) {
            return super.showContextMenuForChild(originalView, x, y);
        }
        return super.showContextMenuForChild(originalView);
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        HwKeyEventDetector hwKeyEventDetector;
        if (event == null || (hwKeyEventDetector = this.mHwKeyEventDetector) == null || !hwKeyEventDetector.onKeyEvent(event.getKeyCode(), event)) {
            return false;
        }
        return true;
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        HwKeyEventDetector hwKeyEventDetector;
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
        if (event == null || (hwKeyEventDetector = this.mHwKeyEventDetector) == null || !hwKeyEventDetector.onKeyEvent(event.getKeyCode(), event)) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void dispatchSetPressed(boolean pressed) {
    }

    @Override // android.view.ViewGroup, android.view.View
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

    /* access modifiers changed from: private */
    public final class CheckForTap implements Runnable {
        float x;
        float y;

        private CheckForTap() {
        }

        @Override // java.lang.Runnable
        public void run() {
            AbsListView.this.resetTouchModeToDownIfNeed();
            if (AbsListView.this.mTouchMode == 0) {
                AbsListView absListView = AbsListView.this;
                absListView.mTouchMode = 1;
                View child = absListView.getChildAt(absListView.mMotionPosition - AbsListView.this.mFirstPosition);
                if (child != null && !child.hasExplicitFocusable()) {
                    AbsListView absListView2 = AbsListView.this;
                    absListView2.mLayoutMode = 0;
                    if (!absListView2.mDataChanged) {
                        float[] point = AbsListView.this.mTmpPoint;
                        point[0] = this.x;
                        point[1] = this.y;
                        AbsListView.this.transformPointToViewLocal(point, child);
                        child.drawableHotspotChanged(point[0], point[1]);
                        child.setPressed(true);
                        AbsListView.this.setPressed(true);
                        AbsListView.this.layoutChildren();
                        AbsListView absListView3 = AbsListView.this;
                        absListView3.positionSelector(absListView3.mMotionPosition, child);
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
                                AbsListView absListView4 = AbsListView.this;
                                absListView4.mPendingCheckForLongPress = new CheckForLongPress();
                            }
                            AbsListView.this.mPendingCheckForLongPress.setCoords(this.x, this.y);
                            AbsListView.this.mPendingCheckForLongPress.rememberWindowAttachCount();
                            AbsListView absListView5 = AbsListView.this;
                            absListView5.postDelayed(absListView5.mPendingCheckForLongPress, (long) longPressTimeout);
                        } else {
                            AbsListView.this.mTouchMode = 2;
                        }
                        if (AbsListView.this.mIsSelectDiscrete || AbsListView.this.mIsSelectContinuous) {
                            if (AbsListView.this.mIsSelectDiscrete) {
                                AbsListView.this.mHasPerformedLongPress = true;
                                AbsListView absListView6 = AbsListView.this;
                                absListView6.setItemChecked(absListView6.mMotionPosition, true);
                                AbsListView.this.mIsSelectDiscrete = false;
                            } else {
                                AbsListView.this.performSelectContinuous();
                                AbsListView.this.mIsSelectContinuous = false;
                            }
                            AbsListView absListView7 = AbsListView.this;
                            absListView7.removeCallbacks(absListView7.mPendingCheckForLongPress);
                            AbsListView.this.mTouchMode = -1;
                            return;
                        }
                        return;
                    }
                    AbsListView.this.mTouchMode = 2;
                }
            }
        }
    }

    private boolean startScrollIfNeeded(int x, int y, MotionEvent vtev) {
        int deltaY = y - this.mMotionY;
        int distance = Math.abs(deltaY);
        boolean overscroll = this.mScrollY != 0;
        if ((!overscroll && distance <= this.mTouchSlop) || (getNestedScrollAxes() & 2) != 0) {
            return false;
        }
        if (ViewRootImpl.DEBUG_VIEW_TRACE) {
            Trace.traceBegin(8, "Beyond " + this.mTouchSlop + "px");
            Log.d(TAG, "AdaptVsyncOffsetInfo Beyond " + this.mTouchSlop + "px");
            Trace.traceEnd(8);
        }
        createScrollingCache();
        if (!overscroll || !canOverScroll(deltaY)) {
            this.mTouchMode = 3;
            int i = this.mTouchSlop;
            if (deltaY <= 0) {
                i = -i;
            }
            this.mMotionCorrection = i;
        } else {
            this.mTouchMode = 5;
            this.mMotionCorrection = 0;
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
        int motionCorrectionCompensation;
        int i;
        int scrollOffsetCorrection;
        int scrollConsumedCorrection;
        int rawDeltaY;
        int incrementalDeltaY;
        int incrementalDeltaY2;
        int incrementalDeltaY3;
        int newDirection;
        int incrementalDeltaY4;
        EdgeEffect edgeEffect;
        int motionIndex;
        int motionViewPrevTop;
        boolean atEdge;
        int motionIndex2;
        VelocityTracker velocityTracker;
        ViewParent parent;
        int i2 = this.mMotionCorrection;
        int newDirection2 = -1;
        if (i2 != 0) {
            int motionCorrectionCompensation2 = i2 > 0 ? -1 : 1;
            this.mMotionCorrection += motionCorrectionCompensation2;
            motionCorrectionCompensation = motionCorrectionCompensation2;
        } else {
            motionCorrectionCompensation = 0;
        }
        int rawDeltaY2 = y - this.mMotionY;
        if (this.mLastY == Integer.MIN_VALUE) {
            rawDeltaY2 -= this.mMotionCorrection;
        }
        int i3 = this.mLastY;
        if (i3 != Integer.MIN_VALUE) {
            i = (i3 - y) + motionCorrectionCompensation;
        } else {
            i = -rawDeltaY2;
        }
        if (dispatchNestedPreScroll(0, i, this.mScrollConsumed, this.mScrollOffset)) {
            int[] iArr = this.mScrollConsumed;
            int rawDeltaY3 = rawDeltaY2 + iArr[1];
            int[] iArr2 = this.mScrollOffset;
            int scrollOffsetCorrection2 = -iArr2[1];
            int scrollConsumedCorrection2 = iArr[1];
            if (vtev != null) {
                this.mNestedYOffset += iArr2[1];
            }
            rawDeltaY = rawDeltaY3;
            scrollOffsetCorrection = scrollOffsetCorrection2;
            scrollConsumedCorrection = scrollConsumedCorrection2;
        } else {
            rawDeltaY = rawDeltaY2;
            scrollOffsetCorrection = 0;
            scrollConsumedCorrection = 0;
        }
        int i4 = this.mLastY;
        if (i4 != Integer.MIN_VALUE) {
            incrementalDeltaY = ((y - i4) + scrollConsumedCorrection) - motionCorrectionCompensation;
        } else {
            incrementalDeltaY = rawDeltaY;
        }
        int lastYCorrection = 0;
        int i5 = this.mTouchMode;
        if (i5 == 3) {
            if (this.mScrollStrictSpan == null) {
                this.mScrollStrictSpan = StrictMode.enterCriticalSpan("AbsListView-scroll");
            }
            if (y != this.mLastY) {
                if ((this.mGroupFlags & 524288) == 0 && Math.abs(rawDeltaY) > this.mTouchSlop && (parent = getParent()) != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                int i6 = this.mSavedTouchDownMotionPosition;
                if (i6 >= 0) {
                    motionIndex = i6 - this.mFirstPosition;
                    this.mMotionPosition = this.mSavedTouchDownMotionPosition;
                } else {
                    motionIndex = getChildCount() / 2;
                }
                View motionView = getChildAt(motionIndex);
                if (motionView != null) {
                    motionViewPrevTop = motionView.getTop();
                } else {
                    motionViewPrevTop = 0;
                }
                if (incrementalDeltaY != 0) {
                    boolean atEdge2 = trackMotionScroll(rawDeltaY, incrementalDeltaY);
                    motionIndex2 = this.mMotionPosition - this.mFirstPosition;
                    atEdge = atEdge2;
                } else {
                    motionIndex2 = motionIndex;
                    atEdge = false;
                }
                View motionView2 = getChildAt(motionIndex2);
                if (motionView2 != null) {
                    int motionViewRealTop = motionView2.getTop();
                    if (atEdge) {
                        int overscroll = (-incrementalDeltaY) - (motionViewRealTop - motionViewPrevTop);
                        if (dispatchNestedScroll(0, overscroll - incrementalDeltaY, 0, overscroll, this.mScrollOffset)) {
                            int[] iArr3 = this.mScrollOffset;
                            lastYCorrection = 0 - iArr3[1];
                            if (vtev != null) {
                                this.mNestedYOffset += iArr3[1];
                            }
                        } else {
                            boolean atOverscrollEdge = overScrollBy(0, overscroll, 0, this.mScrollY, 0, 0, 0, this.mOverscrollDistance, true);
                            if (atOverscrollEdge && (velocityTracker = this.mVelocityTracker) != null) {
                                velocityTracker.clear();
                            }
                            int overscrollMode = getOverScrollMode();
                            if (overscrollMode == 0 || (overscrollMode == 1 && !contentFits())) {
                                if (!atOverscrollEdge && canOverScroll(incrementalDeltaY)) {
                                    this.mDirection = 0;
                                    this.mTouchMode = 5;
                                }
                                EdgeEffect edgeEffect2 = this.mEdgeGlowTop;
                                if (edgeEffect2 != null) {
                                    if (incrementalDeltaY > 0) {
                                        edgeEffect2.onPull(((float) (-overscroll)) / ((float) getHeight()), ((float) x) / ((float) getWidth()));
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
                    this.mMotionY = y + lastYCorrection + scrollOffsetCorrection;
                }
                this.mLastY = y + lastYCorrection + scrollOffsetCorrection;
            }
        } else if (i5 == 5 && y != this.mLastY) {
            int oldScroll = this.mScrollY;
            int newScroll = oldScroll - incrementalDeltaY;
            if (y > this.mLastY) {
                newDirection2 = 1;
            }
            if (this.mDirection == 0) {
                this.mDirection = newDirection2;
            }
            int overScrollDistance = -incrementalDeltaY;
            if ((newScroll >= 0 || oldScroll < 0) && (newScroll <= 0 || oldScroll > 0)) {
                incrementalDeltaY2 = overScrollDistance;
                incrementalDeltaY3 = 0;
            } else {
                int overScrollDistance2 = -oldScroll;
                incrementalDeltaY2 = overScrollDistance2;
                incrementalDeltaY3 = incrementalDeltaY + overScrollDistance2;
            }
            if (incrementalDeltaY2 == 0 || !canOverScroll(newDirection2)) {
                incrementalDeltaY4 = incrementalDeltaY3;
                newDirection = newDirection2;
            } else {
                incrementalDeltaY4 = incrementalDeltaY3;
                newDirection = newDirection2;
                overScrollBy(0, incrementalDeltaY2, 0, this.mScrollY, 0, 0, 0, this.mOverscrollDistance, true);
                int overscrollMode2 = getOverScrollMode();
                if ((overscrollMode2 == 0 || (overscrollMode2 == 1 && !contentFits())) && (edgeEffect = this.mEdgeGlowTop) != null) {
                    if (rawDeltaY > 0) {
                        edgeEffect.onPull(((float) incrementalDeltaY2) / ((float) getHeight()), ((float) x) / ((float) getWidth()));
                        if (!this.mEdgeGlowBottom.isFinished()) {
                            this.mEdgeGlowBottom.onRelease();
                        }
                        invalidateTopGlow();
                    } else if (rawDeltaY < 0) {
                        this.mEdgeGlowBottom.onPull(((float) incrementalDeltaY2) / ((float) getHeight()), 1.0f - (((float) x) / ((float) getWidth())));
                        if (!this.mEdgeGlowTop.isFinished()) {
                            this.mEdgeGlowTop.onRelease();
                        }
                        invalidateBottomGlow();
                    }
                }
            }
            if (incrementalDeltaY4 != 0) {
                if (this.mScrollY != 0) {
                    this.mScrollY = 0;
                    invalidateParentIfNeeded();
                }
                trackMotionScroll(incrementalDeltaY4, incrementalDeltaY4);
                this.mTouchMode = 3;
                int motionPosition = findClosestMotionRow(y);
                int i7 = 0;
                this.mMotionCorrection = 0;
                View motionView3 = getChildAt(motionPosition - this.mFirstPosition);
                if (motionView3 != null) {
                    i7 = motionView3.getTop();
                }
                this.mMotionViewOriginalTop = i7;
                this.mMotionY = y + scrollOffsetCorrection;
                this.mMotionPosition = motionPosition;
            }
            this.mLastY = y + 0 + scrollOffsetCorrection;
            this.mDirection = newDirection;
        }
    }

    private void invalidateTopGlow() {
        if (shouldDisplayEdgeEffects()) {
            boolean clipToPadding = getClipToPadding();
            int left = 0;
            int top = clipToPadding ? this.mPaddingTop : 0;
            if (clipToPadding) {
                left = this.mPaddingLeft;
            }
            int right = getWidth();
            if (clipToPadding) {
                right -= this.mPaddingRight;
            }
            invalidate(left, top, right, this.mEdgeGlowTop.getMaxHeight() + top);
        }
    }

    private void invalidateBottomGlow() {
        if (shouldDisplayEdgeEffects()) {
            boolean clipToPadding = getClipToPadding();
            int bottom = getHeight();
            if (clipToPadding) {
                bottom -= this.mPaddingBottom;
            }
            int left = clipToPadding ? this.mPaddingLeft : 0;
            int right = getWidth();
            if (clipToPadding) {
                right -= this.mPaddingRight;
            }
            invalidate(left, bottom - this.mEdgeGlowBottom.getMaxHeight(), right, bottom);
        }
    }

    @Override // android.view.ViewTreeObserver.OnTouchModeChangeListener
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
            FlingRunnable flingRunnable = this.mFlingRunnable;
            if (flingRunnable != null) {
                flingRunnable.endFling();
            }
            AbsPositionScroller absPositionScroller = this.mPositionScroller;
            if (absPositionScroller != null) {
                absPositionScroller.stop();
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
    @Override // android.view.View
    public boolean handleScrollBarDragging(MotionEvent event) {
        return false;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return isClickable() || isLongClickable();
        }
        if (this.mPositionScroller != null && (!this.mIsAutoScroll || ev.getActionMasked() == 1)) {
            this.mPositionScroller.stop();
        }
        if (this.mIsDetaching || !isAttachedToWindow()) {
            return false;
        }
        startNestedScroll(2);
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller != null && fastScroller.onTouchEvent(ev)) {
            return true;
        }
        initVelocityTrackerIfNotExists();
        MotionEvent vtev = MotionEvent.obtain(ev);
        int actionMasked = ev.getActionMasked();
        if (actionMasked == 0) {
            this.mNestedYOffset = 0;
        }
        vtev.offsetLocation(0.0f, (float) this.mNestedYOffset);
        if (actionMasked == 0) {
            onTouchDown(ev);
        } else if (actionMasked == 1) {
            this.mIsAutoScroll = false;
            onTouchUpEx(ev);
        } else if (actionMasked == 2) {
            onTouchMove(ev, vtev);
        } else if (actionMasked == 3) {
            onTouchCancel();
        } else if (actionMasked == 5) {
            int index = ev.getActionIndex();
            int id = ev.getPointerId(index);
            int x = (int) ev.getX(index);
            int y = (int) ev.getY(index);
            this.mMotionCorrection = 0;
            this.mActivePointerId = id;
            this.mMotionX = x;
            this.mMotionY = y;
            int motionPosition = pointToPosition(x, y);
            if (motionPosition >= 0) {
                this.mMotionViewOriginalTop = getChildAt(motionPosition - this.mFirstPosition).getTop();
                dismissCurrentPressed();
                this.mMotionPosition = motionPosition;
                this.mSavedTouchDownMotionPosition = motionPosition;
            }
            this.mLastY = y;
        } else if (actionMasked == 6) {
            onSecondaryPointerUp(ev);
            int x2 = this.mMotionX;
            int y2 = this.mMotionY;
            int motionPosition2 = pointToPosition(x2, y2);
            if (motionPosition2 >= 0) {
                this.mMotionViewOriginalTop = getChildAt(motionPosition2 - this.mFirstPosition).getTop();
                this.mMotionPosition = motionPosition2;
            }
            this.mLastY = y2;
        }
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;
    }

    private void onTouchDown(MotionEvent ev) {
        this.mHasPerformedLongPress = false;
        this.mIsSelectDiscrete = false;
        this.mIsSelectContinuous = false;
        this.mActivePointerId = ev.getPointerId(0);
        hideSelector();
        if (this.mTouchMode == 6) {
            this.mFlingRunnable.endFling();
            AbsPositionScroller absPositionScroller = this.mPositionScroller;
            if (absPositionScroller != null) {
                absPositionScroller.stop();
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
                } else if (motionPosition >= 0 && ((ListAdapter) getAdapter()).isEnabled(motionPosition)) {
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
            this.mSavedTouchDownMotionPosition = motionPosition;
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
            float x = ev.getX(pointerIndex);
            int motionPosition = findMotionRow(y);
            if (isMeetMultiSelectConditions(motionPosition)) {
                enterMultiSelectModeIfNeeded(motionPosition, (int) x);
            }
            int i = this.mTouchMode;
            if (i == 0 || i == 1 || i == 2) {
                if (!startScrollIfNeeded((int) ev.getX(pointerIndex), y, vtev)) {
                    View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
                    if (!pointInView(x, (float) y, (float) this.mTouchSlop)) {
                        setPressed(false);
                        if (motionView != null) {
                            motionView.setPressed(false);
                        }
                        removeCallbacks(this.mTouchMode == 0 ? this.mPendingCheckForTap : this.mPendingCheckForLongPress);
                        this.mTouchMode = 2;
                        updateSelectorState();
                    } else if (motionView != null) {
                        float[] point = this.mTmpPoint;
                        point[0] = x;
                        point[1] = (float) y;
                        transformPointToViewLocal(point, motionView);
                        motionView.drawableHotspotChanged(point[0], point[1]);
                    }
                }
            } else if (i == 3 || i == 5) {
                scrollIfNeeded((int) ev.getX(pointerIndex), y, vtev);
            }
            if (!this.mIsSelectContinuous) {
                onMultiSelectMove(ev, pointerIndex);
            }
        }
    }

    private boolean isMeetMultiSelectConditions(int motionPosition) {
        boolean isInMotionPosition = motionPosition >= 0;
        int i = this.mTouchMode;
        return (i != 4 && i != 7 && i != 5 && i != 3 && i != -1) && isInMotionPosition;
    }

    /* access modifiers changed from: protected */
    public void onTouchUpEx(MotionEvent ev) {
        onTouchUp(ev);
    }

    /* JADX INFO: Multiple debug info for r1v8 int: [D('motionPosition' int), D('childCount' int)] */
    private void onTouchUp(MotionEvent ev) {
        EdgeEffect edgeEffect;
        int i;
        int i2 = this.mTouchMode;
        if (i2 == 0 || i2 == 1 || i2 == 2) {
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
                    int i3 = this.mTouchMode;
                    if (i3 == 0 || i3 == 1) {
                        removeCallbacks(this.mTouchMode == 0 ? this.mPendingCheckForTap : this.mPendingCheckForLongPress);
                        this.mLayoutMode = 0;
                        if (this.mDataChanged || !this.mAdapter.isEnabled(childCount)) {
                            this.mTouchMode = -1;
                            updateSelectorState();
                            return;
                        }
                        this.mTouchMode = 1;
                        setSelectedPositionInt(this.mMotionPosition);
                        layoutChildren();
                        child.setPressed(true);
                        positionSelector(this.mMotionPosition, child);
                        setPressed(true);
                        Drawable drawable = this.mSelector;
                        if (drawable != null) {
                            Drawable d = drawable.getCurrent();
                            if (d != null && (d instanceof TransitionDrawable)) {
                                ((TransitionDrawable) d).resetTransition();
                            }
                            this.mSelector.setHotspot(x, ev.getY());
                        }
                        Runnable runnable = this.mTouchModeReset;
                        if (runnable != null) {
                            removeCallbacks(runnable);
                        }
                        if (this.mIsSelectDiscrete || this.mIsSelectContinuous) {
                            if (this.mIsSelectDiscrete) {
                                setItemChecked(this.mMotionPosition, true);
                                this.mIsSelectDiscrete = false;
                            } else {
                                performSelectContinuous();
                                this.mIsSelectContinuous = false;
                            }
                            this.mTouchMode = -1;
                            child.setPressed(false);
                            setPressed(false);
                            return;
                        }
                        this.mTouchModeReset = new Runnable() {
                            /* class android.widget.AbsListView.AnonymousClass5 */

                            @Override // java.lang.Runnable
                            public void run() {
                                AbsListView.this.mTouchModeReset = null;
                                AbsListView.this.mTouchMode = -1;
                                child.setPressed(false);
                                AbsListView.this.setPressed(false);
                                if (!AbsListView.this.mDataChanged && !AbsListView.this.mIsDetaching && AbsListView.this.isAttachedToWindow()) {
                                    performClick.run();
                                }
                            }
                        };
                        postDelayed(this.mTouchModeReset, (long) getPressedStateDuration());
                        return;
                    } else if (!this.mDataChanged && this.mAdapter.isEnabled(childCount)) {
                        performClick.run();
                    }
                }
            }
            this.mTouchMode = -1;
            updateSelectorState();
        } else if (i2 == 3) {
            int childCount2 = getChildCount();
            if (childCount2 > 0) {
                int firstChildTop = getChildAt(0).getTop();
                int lastChildBottom = getChildAt(childCount2 - 1).getBottom();
                int contentTop = this.mListPadding.top;
                int contentBottom = getHeight() - this.mListPadding.bottom;
                if (this.mFirstPosition != 0 || firstChildTop < contentTop || this.mFirstPosition + childCount2 >= this.mItemCount || lastChildBottom > getHeight() - contentBottom) {
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                    int initialVelocity = (int) (velocityTracker.getYVelocity(this.mActivePointerId) * this.mVelocityScale);
                    boolean flingVelocity = Math.abs(initialVelocity) > this.mMinimumVelocity;
                    if (!flingVelocity) {
                        i = -1;
                    } else if ((this.mFirstPosition == 0 && firstChildTop == contentTop - this.mOverscrollDistance && !isNeedFlingOnTop()) || (this.mFirstPosition + childCount2 == this.mItemCount && lastChildBottom == this.mOverscrollDistance + contentBottom)) {
                        i = -1;
                    } else if (!dispatchNestedPreFling(0.0f, (float) (-initialVelocity))) {
                        if (this.mFlingRunnable == null) {
                            this.mFlingRunnable = new FlingRunnable();
                        }
                        reportScrollStateChange(2);
                        if (this.mIHwWechatOptimize.isWechatOptimizeEffect() && Math.abs(initialVelocity) > 3600) {
                            if (Jlog.isBetaUser()) {
                                Jlog.d(312, "ListViewSpeed", Math.abs(initialVelocity) / 2, "");
                            }
                            if (Math.abs(initialVelocity) > this.mIHwWechatOptimize.getWechatFlingVelocity()) {
                                this.mIHwWechatOptimize.setWechatFling(true);
                            } else {
                                reportScrollStateChange(0);
                            }
                        }
                        this.mFlingRunnable.start(-initialVelocity);
                        dispatchNestedFling(0.0f, (float) (-initialVelocity), true);
                    } else {
                        this.mTouchMode = -1;
                        reportScrollStateChange(0);
                    }
                    this.mTouchMode = i;
                    reportScrollStateChange(0);
                    FlingRunnable flingRunnable = this.mFlingRunnable;
                    if (flingRunnable != null) {
                        flingRunnable.endFling();
                        this.mFlingRunnable.startSpringback();
                    } else {
                        this.mFlingRunnable = new FlingRunnable();
                        this.mFlingRunnable.startSpringback();
                    }
                    AbsPositionScroller absPositionScroller = this.mPositionScroller;
                    if (absPositionScroller != null) {
                        absPositionScroller.stop();
                    }
                    if (flingVelocity && !dispatchNestedPreFling(0.0f, (float) (-initialVelocity))) {
                        dispatchNestedFling(0.0f, (float) (-initialVelocity), false);
                    }
                } else {
                    this.mTouchMode = -1;
                    reportScrollStateChange(0);
                }
            } else {
                this.mTouchMode = -1;
                reportScrollStateChange(0);
            }
        } else if (i2 == 5) {
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
        if (shouldDisplayEdgeEffects() && (edgeEffect = this.mEdgeGlowTop) != null) {
            edgeEffect.onRelease();
            this.mEdgeGlowBottom.onRelease();
        }
        invalidate();
        removeCallbacks(this.mPendingCheckForLongPress);
        recycleVelocityTracker();
        this.mActivePointerId = -1;
        StrictMode.Span span = this.mScrollStrictSpan;
        if (span != null) {
            span.finish();
            this.mScrollStrictSpan = null;
        }
    }

    private boolean shouldDisplayEdgeEffects() {
        return getOverScrollMode() != 2;
    }

    private void onTouchCancel() {
        EdgeEffect edgeEffect;
        int i = this.mTouchMode;
        if (i == 5) {
            if (this.mFlingRunnable == null) {
                this.mFlingRunnable = new FlingRunnable();
            }
            this.mFlingRunnable.startSpringback();
        } else if (i != 6) {
            this.mTouchMode = -1;
            setPressed(false);
            View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
            if (motionView != null) {
                motionView.setPressed(false);
            }
            clearScrollingCache();
            removeCallbacks(this.mPendingCheckForLongPress);
            recycleVelocityTracker();
        }
        if (shouldDisplayEdgeEffects() && (edgeEffect = this.mEdgeGlowTop) != null) {
            edgeEffect.onRelease();
            this.mEdgeGlowBottom.onRelease();
        }
        this.mActivePointerId = -1;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (this.mScrollY != scrollY) {
            onScrollChanged(this.mScrollX, scrollY, this.mScrollX, this.mScrollY);
            this.mScrollY = scrollY;
            invalidateParentIfNeeded();
            awakenScrollBars();
        }
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent event) {
        float axisValue;
        int actionButton;
        int i;
        HwCompoundEventDetector hwCompoundEventDetector = this.mHwCompoundEventDetector;
        if (hwCompoundEventDetector != null && hwCompoundEventDetector.onGenericMotionEvent(event)) {
            return true;
        }
        HwGenericEventDetector hwGenericEventDetector = this.mHwGenericEventDetector;
        if (hwGenericEventDetector != null && hwGenericEventDetector.onGenericMotionEvent(event)) {
            return true;
        }
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
        } else if (action == 11 && event.isFromSource(2) && (((actionButton = event.getActionButton()) == 32 || actionButton == 2) && (((i = this.mTouchMode) == 0 || i == 1) && performStylusButtonPressAction(event)))) {
            removeCallbacks(this.mPendingCheckForLongPress);
            removeCallbacks(this.mPendingCheckForTap);
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

    public void registerVelocityListener(HwPerfSpeedLoader hwPerfSpeedLoader) {
        this.mHwPerfSpeedLoader = hwPerfSpeedLoader;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & 2) != 0;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(2);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int myUnconsumed;
        int myConsumed;
        View motionView = getChildAt(getChildCount() / 2);
        int oldTop = motionView != null ? motionView.getTop() : 0;
        if (motionView == null || trackMotionScroll(-dyUnconsumed, -dyUnconsumed)) {
            if (motionView != null) {
                int myConsumed2 = motionView.getTop() - oldTop;
                myUnconsumed = dyUnconsumed - myConsumed2;
                myConsumed = myConsumed2;
            } else {
                myUnconsumed = dyUnconsumed;
                myConsumed = 0;
            }
            dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null);
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        int childCount = getChildCount();
        if (consumed || childCount <= 0 || !canScrollList((int) velocityY) || Math.abs(velocityY) <= ((float) this.mMinimumVelocity)) {
            return dispatchNestedFling(velocityX, velocityY, consumed);
        }
        reportScrollStateChange(2);
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable();
        }
        if (dispatchNestedPreFling(0.0f, velocityY)) {
            return true;
        }
        this.mFlingRunnable.start((int) velocityY);
        return true;
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        int translateY;
        int translateX;
        int height;
        int width;
        super.draw(canvas);
        if (shouldDisplayEdgeEffects() && this.mEdgeGlowTop != null) {
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
            this.mEdgeGlowTop.setSize(width, height);
            this.mEdgeGlowBottom.setSize(width, height);
            int i = 0;
            if (!this.mEdgeGlowTop.isFinished()) {
                int restoreCount = canvas.save();
                canvas.clipRect(translateX, translateY, translateX + width, this.mEdgeGlowTop.getMaxHeight() + translateY);
                canvas.translate((float) translateX, (float) (Math.min(0, this.mFirstPositionDistanceGuess + scrollY) + translateY));
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
                if (this.mEdgeGlowBottom.draw(canvas)) {
                    invalidateBottomGlow();
                }
                canvas.restoreToCount(restoreCount2);
            }
        }
    }

    private void initOrResetVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptHoverEvent(MotionEvent event) {
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller == null || !fastScroller.onInterceptHoverEvent(event)) {
            return super.onInterceptHoverEvent(event);
        }
        return true;
    }

    @Override // android.view.ViewGroup, android.view.View
    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        PointerIcon pointerIcon;
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller == null || (pointerIcon = fastScroller.onResolvePointerIcon(event, pointerIndex)) == null) {
            return super.onResolvePointerIcon(event, pointerIndex);
        }
        return pointerIcon;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        AbsPositionScroller absPositionScroller = this.mPositionScroller;
        if (absPositionScroller != null) {
            absPositionScroller.stop();
        }
        if (this.mIsDetaching || !isAttachedToWindow()) {
            return false;
        }
        FastScroller fastScroller = this.mFastScroll;
        if (fastScroller != null && fastScroller.onInterceptTouchEvent(ev)) {
            return true;
        }
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked != 2) {
                    if (actionMasked != 3) {
                        if (actionMasked == 6) {
                            onSecondaryPointerUp(ev);
                        }
                    }
                } else if (this.mTouchMode == 0) {
                    int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                    if (pointerIndex == -1) {
                        pointerIndex = 0;
                        this.mActivePointerId = ev.getPointerId(0);
                    }
                    int x = (int) ev.getX(pointerIndex);
                    int y = (int) ev.getY(pointerIndex);
                    initVelocityTrackerIfNotExists();
                    this.mVelocityTracker.addMovement(ev);
                    int motionPosition = findMotionRow(y);
                    int i = this.mTouchMode;
                    if (((i == 4 || i == 7 || i == 5) ? false : true) && motionPosition >= 0 && Math.abs(y - this.mMotionY) > this.mTouchSlop) {
                        enterMultiSelectModeIfNeeded(motionPosition, x);
                        if (this.mTouchMode == 7) {
                            return true;
                        }
                    }
                    MotionEvent motionEvent = MotionEvent.obtain(ev);
                    if (startScrollIfNeeded((int) ev.getX(pointerIndex), y, motionEvent)) {
                        motionEvent.recycle();
                        return true;
                    }
                    motionEvent.recycle();
                }
            }
            this.mTouchMode = -1;
            this.mActivePointerId = -1;
            recycleVelocityTracker();
            reportScrollStateChange(0);
            stopNestedScroll();
        } else {
            int touchMode = this.mTouchMode;
            if (touchMode == 6 || touchMode == 5) {
                this.mMotionCorrection = 0;
                return true;
            }
            int x2 = (int) ev.getX();
            int y2 = (int) ev.getY();
            this.mActivePointerId = ev.getPointerId(0);
            int motionPosition2 = findMotionRow(y2);
            if (touchMode != 4 && motionPosition2 >= 0) {
                this.mMotionViewOriginalTop = getChildAt(motionPosition2 - this.mFirstPosition).getTop();
                this.mMotionX = x2;
                this.mMotionY = y2;
                this.mMotionPosition = motionPosition2;
                this.mSavedTouchDownMotionPosition = motionPosition2;
                this.mTouchMode = 0;
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

    @Override // android.view.ViewGroup, android.view.View
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
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769710)
    public void reportScrollStateChange(int newState) {
        OnScrollListener onScrollListener;
        if (newState != this.mLastScrollState && (onScrollListener = this.mOnScrollListener) != null) {
            this.mLastScrollState = newState;
            onScrollListener.onScrollStateChanged(this, newState);
        }
    }

    public class FlingRunnable implements Runnable {
        private static final int FLYWHEEL_TIMEOUT = 40;
        private boolean isNeedOverFling = true;
        private final Runnable mCheckFlywheel = new Runnable() {
            /* class android.widget.AbsListView.FlingRunnable.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                int activeId = AbsListView.this.mActivePointerId;
                VelocityTracker vt = AbsListView.this.mVelocityTracker;
                OverScroller scroller = FlingRunnable.this.mScroller;
                if (vt != null && activeId != -1) {
                    vt.computeCurrentVelocity(1000, (float) AbsListView.this.mMaximumVelocity);
                    float yvel = -vt.getYVelocity(activeId);
                    if (Math.abs(yvel) < ((float) AbsListView.this.mMinimumVelocity) || !scroller.isScrollingInDirection(0.0f, yvel)) {
                        if (AbsListView.this.mTouchMode != 6 || !AbsListView.this.canOverScroll(scroller.getFinalY() - scroller.getStartY())) {
                            FlingRunnable.this.endFling();
                            AbsListView.this.mTouchMode = 3;
                        } else {
                            FlingRunnable.this.endFling();
                            AbsListView.this.mTouchMode = 5;
                        }
                        AbsListView.this.reportScrollStateChange(1);
                        return;
                    }
                    AbsListView.this.postDelayed(this, 40);
                }
            }
        };
        private HwSpringBackHelper mHwSpringBackHelper;
        private int mLastFlingY;
        @UnsupportedAppUsage
        private final OverScroller mScroller;
        private boolean mSuppressIdleStateChangeCall;

        FlingRunnable() {
            this.mScroller = new OverScroller(AbsListView.this.getContext());
            if (AbsListView.this.hasSpringAnimatorMask()) {
                this.mHwSpringBackHelper = HwWidgetFactory.getHwSpringBackHelper();
            }
        }

        /* access modifiers changed from: package-private */
        @UnsupportedAppUsage(maxTargetSdk = 28)
        public void start(int initialVelocity) {
            int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            this.mLastFlingY = initialY;
            this.mScroller.setInterpolator(null);
            AbsListView absListView = AbsListView.this;
            absListView.setStableItemHeight(this.mScroller, absListView.mFlingRunnable);
            this.isNeedOverFling = true;
            this.mScroller.fling(0, initialY, 0, initialVelocity, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            AbsListView absListView2 = AbsListView.this;
            absListView2.mTouchMode = 4;
            this.mSuppressIdleStateChangeCall = false;
            absListView2.postOnAnimation(this);
            if (AbsListView.this.mHwPerfSpeedLoader != null) {
                AbsListView.this.mHwPerfSpeedLoader.onFlingStart();
            }
            if (AbsListView.this.mFlingStrictSpan == null) {
                AbsListView.this.mFlingStrictSpan = StrictMode.enterCriticalSpan("AbsListView-fling");
            }
        }

        /* access modifiers changed from: package-private */
        public void startSpringback() {
            this.isNeedOverFling = true;
            this.mSuppressIdleStateChangeCall = false;
            if (this.mScroller.springBack(0, AbsListView.this.mScrollY, 0, 0, 0, 0)) {
                AbsListView absListView = AbsListView.this;
                absListView.mTouchMode = 6;
                HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
                if (hwSpringBackHelper != null) {
                    hwSpringBackHelper.springBack(absListView.mScrollY, 0, 0);
                }
                AbsListView.this.invalidate();
                AbsListView.this.postOnAnimation(this);
                return;
            }
            AbsListView absListView2 = AbsListView.this;
            absListView2.mTouchMode = -1;
            absListView2.reportScrollStateChange(0);
        }

        /* access modifiers changed from: package-private */
        public void startOverfling(int initialVelocity) {
            this.isNeedOverFling = true;
            this.mScroller.setInterpolator(null);
            this.mScroller.fling(0, AbsListView.this.mScrollY, 0, initialVelocity, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, AbsListView.this.getHeight());
            AbsListView absListView = AbsListView.this;
            absListView.mTouchMode = 6;
            this.mSuppressIdleStateChangeCall = false;
            absListView.invalidate();
            AbsListView.this.postOnAnimation(this);
        }

        /* access modifiers changed from: package-private */
        public void edgeReached(int delta) {
            if (!AbsListView.this.hasSpringAnimatorMask()) {
                this.mScroller.notifyVerticalEdgeReached(AbsListView.this.mScrollY, 0, AbsListView.this.mOverflingDistance);
            } else {
                this.mScroller.notifyVerticalEdgeReached(AbsListView.this.mScrollY, 0, (int) (((float) AbsListView.this.getHeight()) * 0.5f));
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
                HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
                if (hwSpringBackHelper != null) {
                    if (this.isNeedOverFling) {
                        hwSpringBackHelper.overFling((float) (delta > 0 ? -vel : vel), AbsListView.this.mScrollY, 0);
                    }
                    this.mScroller.abortAnimation();
                }
            } else {
                AbsListView absListView = AbsListView.this;
                absListView.mTouchMode = -1;
                if (absListView.mPositionScroller != null) {
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
            HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
            if (hwSpringBackHelper != null) {
                hwSpringBackHelper.abortAnimation();
            }
            this.mScroller.setInterpolator(linear ? AbsListView.sLinearInterpolator : null);
            this.isNeedOverFling = false;
            this.mScroller.startScroll(0, initialY, 0, distance, duration);
            AbsListView absListView = AbsListView.this;
            absListView.mTouchMode = 4;
            this.mSuppressIdleStateChangeCall = suppressEndFlingStateChangeCall;
            absListView.postOnAnimation(this);
        }

        /* access modifiers changed from: package-private */
        @UnsupportedAppUsage(maxTargetSdk = 28)
        public void endFling() {
            if (!AbsListView.this.mIsAutoScroll) {
                AbsListView.this.mTouchMode = -1;
            }
            AbsListView.this.removeCallbacks(this);
            AbsListView.this.removeCallbacks(this.mCheckFlywheel);
            if (!this.mSuppressIdleStateChangeCall) {
                AbsListView.this.reportScrollStateChange(0);
            }
            AbsListView.this.clearScrollingCache();
            this.isNeedOverFling = true;
            this.mScroller.abortAnimation();
            HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
            if (hwSpringBackHelper != null) {
                hwSpringBackHelper.abortAnimation();
            }
            if (AbsListView.this.mFlingStrictSpan != null) {
                AbsListView.this.mFlingStrictSpan.finish();
                AbsListView.this.mFlingStrictSpan = null;
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

        private void startHwSpringBack() {
            OverScroller scroller = this.mScroller;
            HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
            if (hwSpringBackHelper == null || !hwSpringBackHelper.computeScrollOffset()) {
                endFling();
                return;
            }
            scroller.computeScrollOffset();
            int scrollY = AbsListView.this.mScrollY;
            AbsListView absListView = AbsListView.this;
            if (absListView.overScrollBy(0, this.mHwSpringBackHelper.getCurrentOffset() - scrollY, 0, scrollY, 0, 0, 0, absListView.mOverflingDistance, false)) {
                startSpringback();
                return;
            }
            AbsListView.this.invalidate();
            AbsListView.this.postOnAnimation(this);
        }

        /* JADX WARNING: Removed duplicated region for block: B:44:0x009e  */
        @Override // java.lang.Runnable
        public void run() {
            int delta;
            int i = AbsListView.this.mTouchMode;
            boolean crossUp = true;
            if (i != 3) {
                if (i != 4) {
                    if (i != 5) {
                        if (i != 6) {
                            endFling();
                            return;
                        }
                        OverScroller scroller = this.mScroller;
                        HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
                        if (hwSpringBackHelper != null && !hwSpringBackHelper.isFinished()) {
                            startHwSpringBack();
                            return;
                        } else if (scroller.computeScrollOffset()) {
                            int scrollY = AbsListView.this.mScrollY;
                            int currY = scroller.getCurrY();
                            AbsListView absListView = AbsListView.this;
                            if (absListView.overScrollBy(0, currY - scrollY, 0, scrollY, 0, 0, 0, absListView.mOverflingDistance, false)) {
                                boolean crossDown = scrollY <= 0 && currY > 0;
                                if (scrollY < 0 || currY >= 0) {
                                    crossUp = false;
                                }
                                if (crossDown || crossUp) {
                                    int velocity = (int) scroller.getCurrVelocity();
                                    if (crossUp) {
                                        velocity = -velocity;
                                    }
                                    scroller.abortAnimation();
                                    start(velocity);
                                    return;
                                }
                                startSpringback();
                                return;
                            }
                            AbsListView.this.invalidate();
                            AbsListView.this.postOnAnimation(this);
                            return;
                        } else {
                            endFling();
                            return;
                        }
                    } else if (!isNeedOverScroll()) {
                        endFling();
                        return;
                    }
                }
                if (AbsListView.this.mDataChanged) {
                    AbsListView.this.layoutChildren();
                }
                if (AbsListView.this.mItemCount != 0 || AbsListView.this.getChildCount() == 0) {
                    endFling();
                }
                OverScroller scroller2 = this.mScroller;
                boolean more = scroller2.computeScrollOffset();
                int flingY = scroller2.getCurrY();
                int delta2 = this.mLastFlingY - flingY;
                if (delta2 > 0) {
                    AbsListView absListView2 = AbsListView.this;
                    absListView2.mMotionPosition = absListView2.mFirstPosition;
                    AbsListView.this.mMotionViewOriginalTop = AbsListView.this.getChildAt(0).getTop();
                    delta = Math.min(((AbsListView.this.getHeight() - AbsListView.this.mPaddingBottom) - AbsListView.this.mPaddingTop) - 1, delta2);
                } else {
                    int offsetToLast = AbsListView.this.getChildCount() - 1;
                    AbsListView absListView3 = AbsListView.this;
                    absListView3.mMotionPosition = absListView3.mFirstPosition + offsetToLast;
                    AbsListView.this.mMotionViewOriginalTop = AbsListView.this.getChildAt(offsetToLast).getTop();
                    delta = Math.max(-(((AbsListView.this.getHeight() - AbsListView.this.mPaddingBottom) - AbsListView.this.mPaddingTop) - 1), delta2);
                }
                AbsListView absListView4 = AbsListView.this;
                View motionView = absListView4.getChildAt(absListView4.mMotionPosition - AbsListView.this.mFirstPosition);
                int oldTop = 0;
                if (motionView != null) {
                    oldTop = motionView.getTop();
                }
                boolean atEdge = AbsListView.this.trackMotionScroll(delta, delta);
                if (!atEdge || delta == 0) {
                    crossUp = false;
                }
                int overshoot = 0;
                if (motionView != null) {
                    overshoot = crossUp ? -(delta - (motionView.getTop() - oldTop)) : 0;
                }
                boolean isNeedMore = AbsListView.this.isNeedOverFlingMoreAtEdge(more, crossUp, overshoot, delta);
                if (crossUp) {
                    if (motionView != null) {
                        more = isNeedMore;
                        if (!AbsListView.this.hasSpringAnimatorMask()) {
                            AbsListView absListView5 = AbsListView.this;
                            absListView5.overScrollBy(0, overshoot, 0, absListView5.mScrollY, 0, 0, 0, AbsListView.this.mOverflingDistance, false);
                        }
                    }
                    if (more) {
                        edgeReached(delta);
                        return;
                    }
                    return;
                }
                if (!more || crossUp) {
                    endFling();
                } else {
                    if (atEdge) {
                        AbsListView.this.invalidate();
                    }
                    this.mLastFlingY = flingY;
                    AbsListView.this.postOnAnimation(this);
                }
                if (AbsListView.this.mHwPerfSpeedLoader != null) {
                    AbsListView.this.mHwPerfSpeedLoader.onFlingRunning(scroller2.getCurrVelocity());
                }
                if (AbsListView.this.mIHwWechatOptimize.isWechatOptimizeEffect() && AbsListView.this.mIHwWechatOptimize.isWechatFling() && Math.abs(scroller2.getCurrVelocity()) < ((float) AbsListView.this.mIHwWechatOptimize.getWechatIdleVelocity())) {
                    AbsListView.this.mIHwWechatOptimize.setWechatFling(false);
                    AbsListView.this.reportScrollStateChange(0);
                    return;
                }
                return;
            }
            if (this.mScroller.isFinished()) {
                return;
            }
            if (AbsListView.this.mDataChanged) {
            }
            if (AbsListView.this.mItemCount != 0) {
            }
            endFling();
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
        if (this.mIsAutoScroll) {
            smoothScrollBy(distance, duration, true, false);
        } else {
            smoothScrollBy(distance, duration, false, false);
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
            if (this.mTouchMode == 4) {
                this.mFlingRunnable.mSuppressIdleStateChangeCall = false;
            }
            this.mFlingRunnable.endFling();
            AbsPositionScroller absPositionScroller = this.mPositionScroller;
            if (absPositionScroller != null) {
                absPositionScroller.stop();
                return;
            }
            return;
        }
        reportScrollStateChange(2);
        this.mFlingRunnable.startScroll(distance, duration, linear, suppressEndFlingStateChangeCall);
    }

    /* access modifiers changed from: package-private */
    public void smoothScrollByOffset(int position) {
        View child;
        int index = -1;
        if (position < 0) {
            index = getFirstVisiblePosition();
        } else if (position > 0) {
            index = getLastVisiblePosition();
        }
        if (index > -1 && (child = getChildAt(index - getFirstVisiblePosition())) != null) {
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

    private void createScrollingCache() {
        if (this.mScrollingCacheEnabled && !this.mCachingStarted && !isHardwareAccelerated()) {
            setChildrenDrawnWithCacheEnabled(true);
            setChildrenDrawingCacheEnabled(true);
            this.mCachingActive = true;
            this.mCachingStarted = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearScrollingCache() {
        if (!isHardwareAccelerated()) {
            if (this.mClearScrollingCache == null) {
                this.mClearScrollingCache = new Runnable() {
                    /* class android.widget.AbsListView.AnonymousClass6 */

                    @Override // java.lang.Runnable
                    public void run() {
                        if (AbsListView.this.mCachingStarted) {
                            AbsListView absListView = AbsListView.this;
                            absListView.mCachingActive = false;
                            absListView.mCachingStarted = false;
                            absListView.setChildrenDrawnWithCacheEnabled(false);
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
        if (childCount == 0) {
            return false;
        }
        int firstPosition = this.mFirstPosition;
        Rect listPadding = this.mListPadding;
        if (direction > 0) {
            int lastBottom = getChildAt(childCount - 1).getBottom();
            if (firstPosition + childCount < this.mItemCount || lastBottom > getHeight() - listPadding.bottom) {
                return true;
            }
            return false;
        }
        int firstTop = getChildAt(0).getTop();
        if (firstPosition > 0 || firstTop < listPadding.top) {
            return true;
        }
        return false;
    }

    /* JADX INFO: Multiple debug info for r10v8 int: [D('effectivePaddingBottom' int), D('top' int)] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x01e4, code lost:
        if (r25 < r1) goto L_0x01e9;
     */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x024c  */
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124051739)
    public boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
        int deltaY2;
        int incrementalDeltaY2;
        boolean z;
        boolean z2;
        int spaceBelow;
        int start;
        int headerViewsCount;
        int childIndex;
        int headerViewsCount2;
        int top;
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
        int spaceBelow2 = lastBottom - (getHeight() - effectivePaddingBottom);
        int height = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
        if (deltaY < 0) {
            deltaY2 = Math.max(-(height - 1), deltaY);
        } else {
            deltaY2 = Math.min(height - 1, deltaY);
        }
        if (incrementalDeltaY < 0) {
            incrementalDeltaY2 = Math.max(-(height - 1), incrementalDeltaY);
        } else {
            incrementalDeltaY2 = Math.min(height - 1, incrementalDeltaY);
        }
        int firstPosition = this.mFirstPosition;
        if (firstPosition == 0) {
            this.mFirstPositionDistanceGuess = firstTop - listPadding.top;
        } else {
            this.mFirstPositionDistanceGuess += incrementalDeltaY2;
        }
        if (firstPosition + childCount == this.mItemCount) {
            this.mLastPositionDistanceGuess = listPadding.bottom + lastBottom;
        } else {
            this.mLastPositionDistanceGuess += incrementalDeltaY2;
        }
        boolean cannotScrollDown = firstPosition == 0 && firstTop >= listPadding.top && incrementalDeltaY2 >= 0;
        boolean cannotScrollUp = firstPosition + childCount == this.mItemCount && lastBottom <= getHeight() - listPadding.bottom && incrementalDeltaY2 <= 0;
        if (cannotScrollDown) {
            z2 = false;
            z = true;
        } else if (cannotScrollUp) {
            z2 = false;
            z = true;
        } else {
            boolean down = incrementalDeltaY2 < 0;
            boolean inTouchMode = isInTouchMode();
            if (inTouchMode) {
                hideSelector();
            }
            int headerViewsCount3 = getHeaderViewsCount();
            int footerViewsStart = this.mItemCount - getFooterViewsCount();
            int start2 = 0;
            int count = 0;
            if (down) {
                int top2 = -incrementalDeltaY2;
                if ((this.mGroupFlags & 34) == 34) {
                    top2 += listPadding.top;
                }
                int i = 0;
                while (true) {
                    if (i >= childCount) {
                        spaceBelow = spaceBelow2;
                        break;
                    }
                    View child = getChildAt(i);
                    spaceBelow = spaceBelow2;
                    if (child.getBottom() >= top2) {
                        break;
                    }
                    count++;
                    int position = firstPosition + i;
                    if (position < headerViewsCount3 || position >= footerViewsStart) {
                        top = top2;
                    } else {
                        child.clearAccessibilityFocus();
                        top = top2;
                        this.mRecycler.addScrapView(child, position);
                    }
                    i++;
                    top2 = top;
                    spaceBelow2 = spaceBelow;
                }
                start = 0;
                headerViewsCount = count;
            } else {
                spaceBelow = spaceBelow2;
                int bottom = getHeight() - incrementalDeltaY2;
                if ((this.mGroupFlags & 34) == 34) {
                    bottom -= listPadding.bottom;
                }
                int i2 = childCount - 1;
                while (true) {
                    if (i2 < 0) {
                        break;
                    }
                    View child2 = getChildAt(i2);
                    if (child2.getTop() <= bottom) {
                        break;
                    }
                    start2 = i2;
                    count++;
                    int position2 = firstPosition + i2;
                    if (position2 < headerViewsCount3 || position2 >= footerViewsStart) {
                        headerViewsCount2 = headerViewsCount3;
                    } else {
                        child2.clearAccessibilityFocus();
                        headerViewsCount2 = headerViewsCount3;
                        this.mRecycler.addScrapView(child2, position2);
                    }
                    i2--;
                    headerViewsCount3 = headerViewsCount2;
                }
                start = start2;
                headerViewsCount = count;
            }
            this.mMotionViewNewTop = this.mMotionViewOriginalTop + deltaY2;
            this.mBlockLayoutRequests = true;
            if (headerViewsCount > 0) {
                detachViewsFromParent(start, headerViewsCount);
                this.mRecycler.removeSkippedScrap();
            }
            if (!awakenScrollBars()) {
                invalidate();
            }
            boolean isAtEdge = false;
            if (incrementalDeltaY2 > 0) {
                if (this.mFirstPosition == 0) {
                    if (firstTop + incrementalDeltaY2 >= listPadding.top) {
                        isAtEdge = true;
                    }
                }
            } else if (this.mFirstPosition + childCount == this.mItemCount) {
                if (lastBottom + incrementalDeltaY2 <= getHeight() - listPadding.bottom) {
                    isAtEdge = true;
                }
            }
            offsetChildrenTopAndBottom(incrementalDeltaY2);
            if (down) {
                this.mFirstPosition += headerViewsCount;
            }
            int absIncrementalDeltaY = Math.abs(incrementalDeltaY2);
            HwParallelWorker hwParallelWorker = this.mHwParallelWorker;
            if (hwParallelWorker != null && hwParallelWorker.isPrefetchOptimizeEnable() && this.mHwParallelWorker.getPrefetchTaskStatus() == 0) {
                this.mHwParallelWorker.postTaskToParallelWorkerByListView(down);
            }
            if (spaceAbove >= absIncrementalDeltaY) {
            }
            fillGap(down);
            HwParallelWorker hwParallelWorker2 = this.mHwParallelWorker;
            if (hwParallelWorker2 != null && hwParallelWorker2.isPrefetchOptimizeEnable()) {
                this.mHwParallelWorker.resetPrefetchTaskStatus();
            }
            this.mRecycler.fullyDetachScrapViews();
            boolean selectorOnScreen = false;
            if (!inTouchMode) {
                if (this.mSelectedPosition != -1) {
                    int childIndex2 = this.mSelectedPosition - this.mFirstPosition;
                    if (childIndex2 >= 0 && childIndex2 < getChildCount()) {
                        positionSelector(this.mSelectedPosition, getChildAt(childIndex2));
                        selectorOnScreen = true;
                    }
                    if (!selectorOnScreen) {
                        this.mSelectorRect.setEmpty();
                    }
                    this.mBlockLayoutRequests = false;
                    invokeOnItemScrollListener();
                    return isAtEdge;
                }
            }
            int childCount2 = this.mSelectorPosition;
            if (childCount2 != -1 && (childIndex = childCount2 - this.mFirstPosition) >= 0 && childIndex < getChildCount()) {
                positionSelector(this.mSelectorPosition, getChildAt(childIndex));
                selectorOnScreen = true;
            }
            if (!selectorOnScreen) {
            }
            this.mBlockLayoutRequests = false;
            invokeOnItemScrollListener();
            return isAtEdge;
        }
        return incrementalDeltaY2 != 0 ? z : z2;
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
    @UnsupportedAppUsage
    public boolean resurrectSelectionIfNeeded() {
        if (this.mSelectedPosition >= 0 || !resurrectSelection()) {
            return false;
        }
        updateSelectorState();
        return true;
    }

    /* JADX INFO: Multiple debug info for r10v3 int: [D('itemCount' int), D('selectedPos' int)] */
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
                int selectedPos2 = this.mItemCount;
                down = false;
                int selectedPos3 = (firstPosition + childCount) - 1;
                int i = childCount - 1;
                while (true) {
                    if (i < 0) {
                        selectedPos = selectedPos3;
                        break;
                    }
                    View v = getChildAt(i);
                    int top = v.getTop();
                    int bottom = v.getBottom();
                    if (i == childCount - 1) {
                        selectedTop = top;
                        if (firstPosition + childCount < selectedPos2 || bottom > childrenBottom) {
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
                int i2 = 0;
                while (true) {
                    if (i2 >= childCount) {
                        break;
                    }
                    int top2 = getChildAt(i2).getTop();
                    if (i2 == 0) {
                        selectedTop = top2;
                        if (firstPosition > 0 || top2 < childrenTop) {
                            childrenTop += getVerticalFadingEdgeLength();
                        }
                    }
                    if (top2 >= childrenTop) {
                        selectedPos = firstPosition + i2;
                        selectedTop = top2;
                        break;
                    }
                    i2++;
                }
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
        AbsPositionScroller absPositionScroller = this.mPositionScroller;
        if (absPositionScroller != null) {
            absPositionScroller.stop();
        }
        this.mTouchMode = -1;
        clearScrollingCache();
        this.mSpecificTop = selectedTop;
        int selectedPos4 = lookForSelectablePosition(selectedPos, down);
        if (selectedPos4 < firstPosition || selectedPos4 > getLastVisiblePosition()) {
            selectedPos4 = -1;
        } else {
            this.mLayoutMode = 4;
            updateSelectorState();
            setSelectionInt(selectedPos4);
            invokeOnItemScrollListener();
        }
        reportScrollStateChange(0);
        if (selectedPos4 >= 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void confirmCheckedPositionsById() {
        ActionMode actionMode;
        int i;
        boolean found;
        MultiChoiceModeWrapper multiChoiceModeWrapper;
        this.mCheckStates.clear();
        boolean checkedCountChanged = false;
        int checkedIndex = 0;
        while (checkedIndex < this.mCheckedIdStates.size()) {
            long id = this.mCheckedIdStates.keyAt(checkedIndex);
            int lastPos = this.mCheckedIdStates.valueAt(checkedIndex).intValue();
            boolean z = true;
            if (id != this.mAdapter.getItemId(lastPos)) {
                int start = Math.max(0, lastPos - 20);
                int end = Math.min(lastPos + 20, this.mItemCount);
                int searchPos = start;
                while (true) {
                    if (searchPos >= end) {
                        found = false;
                        break;
                    } else if (id == this.mAdapter.getItemId(searchPos)) {
                        this.mCheckStates.put(searchPos, z);
                        this.mCheckedIdStates.setValueAt(checkedIndex, Integer.valueOf(searchPos));
                        found = true;
                        break;
                    } else {
                        searchPos++;
                        z = true;
                    }
                }
                if (!found) {
                    this.mCheckedIdStates.delete(id);
                    checkedIndex--;
                    this.mCheckedItemCount--;
                    checkedCountChanged = true;
                    ActionMode actionMode2 = this.mChoiceActionMode;
                    if (actionMode2 != null && (multiChoiceModeWrapper = this.mMultiChoiceModeCallback) != null) {
                        multiChoiceModeWrapper.onItemCheckedStateChanged(actionMode2, lastPos, id, false);
                    }
                }
                i = 1;
            } else {
                i = 1;
                this.mCheckStates.put(lastPos, true);
            }
            checkedIndex += i;
        }
        if (checkedCountChanged && (actionMode = this.mChoiceActionMode) != null) {
            actionMode.invalidate();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AdapterView
    public void handleDataChanged() {
        ListAdapter listAdapter;
        int count = this.mItemCount;
        int lastHandledItemCount = this.mLastHandledItemCount;
        this.mLastHandledItemCount = this.mItemCount;
        HwParallelWorker hwParallelWorker = this.mHwParallelWorker;
        if (hwParallelWorker != null && hwParallelWorker.isPrefetchOptimizeEnable()) {
            this.mHwParallelWorker.clearPrefetchInfo();
        }
        if (!(this.mChoiceMode == 0 || (listAdapter = this.mAdapter) == null || !listAdapter.hasStableIds())) {
            confirmCheckedPositionsById();
        }
        this.mRecycler.clearTransientStateViews();
        int i = 3;
        if (count > 0) {
            if (this.mNeedSync) {
                this.mNeedSync = false;
                this.mPendingSync = null;
                int i2 = this.mTranscriptMode;
                if (i2 == 2) {
                    this.mLayoutMode = 3;
                    return;
                }
                if (i2 == 1) {
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
                int childCount2 = this.mSyncMode;
                if (childCount2 != 0) {
                    if (childCount2 == 1) {
                        this.mLayoutMode = 5;
                        this.mSyncPosition = Math.min(Math.max(0, this.mSyncPosition), count - 1);
                        return;
                    }
                } else if (isInTouchMode()) {
                    this.mLayoutMode = 5;
                    this.mSyncPosition = Math.min(Math.max(0, this.mSyncPosition), count - 1);
                    return;
                } else {
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
                }
            }
            if (!isInTouchMode()) {
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
    @Override // android.view.View
    public void onDisplayHint(int hint) {
        PopupWindow popupWindow;
        PopupWindow popupWindow2;
        super.onDisplayHint(hint);
        if (hint != 0) {
            if (hint == 4 && (popupWindow2 = this.mPopup) != null && popupWindow2.isShowing()) {
                dismissPopup();
            }
        } else if (this.mFiltered && (popupWindow = this.mPopup) != null && !popupWindow.isShowing()) {
            showPopup();
        }
        this.mPopupHidden = hint == 4;
    }

    private void dismissPopup() {
        PopupWindow popupWindow = this.mPopup;
        if (popupWindow != null) {
            popupWindow.dismiss();
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
            this.mPopup.showAtLocation(this, 81, xy[0], bottomGap);
        } else {
            this.mPopup.update(xy[0], bottomGap, -1, -1);
        }
    }

    static int getDistance(Rect source, Rect dest, int direction) {
        int dX;
        int sY;
        int dY;
        int sX;
        if (direction == 1 || direction == 2) {
            sX = source.right + (source.width() / 2);
            sY = source.top + (source.height() / 2);
            dX = dest.left + (dest.width() / 2);
            dY = dest.top + (dest.height() / 2);
        } else if (direction == 17) {
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
        } else if (direction == 130) {
            sX = source.left + (source.width() / 2);
            sY = source.bottom;
            dX = dest.left + (dest.width() / 2);
            dY = dest.top;
        } else {
            throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
        }
        int deltaX = dX - sX;
        int deltaY = dY - sY;
        return (deltaY * deltaY) + (deltaX * deltaX);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AdapterView
    public boolean isInFilterMode() {
        return this.mFiltered;
    }

    /* access modifiers changed from: package-private */
    public boolean sendToTextFilter(int keyCode, int count, KeyEvent event) {
        PopupWindow popupWindow;
        if (!acceptFilter()) {
            return false;
        }
        boolean handled = false;
        boolean okToSend = true;
        if (keyCode == 4) {
            if (this.mFiltered && (popupWindow = this.mPopup) != null && popupWindow.isShowing()) {
                if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.startTracking(event, this);
                    }
                    handled = true;
                } else if (event.getAction() == 1 && event.isTracking() && !event.isCanceled()) {
                    handled = true;
                    this.mTextFilter.setText("");
                }
            }
            okToSend = false;
        } else if (keyCode != 62) {
            if (keyCode != 66) {
                switch (keyCode) {
                }
            }
            okToSend = false;
        } else {
            okToSend = this.mFiltered;
        }
        if (!okToSend) {
            return handled;
        }
        createTextFilter(true);
        KeyEvent forwardEvent = event;
        if (forwardEvent.getRepeatCount() > 0) {
            forwardEvent = KeyEvent.changeTimeRepeat(event, event.getEventTime(), 0);
        }
        int action = event.getAction();
        if (action == 0) {
            return this.mTextFilter.onKeyDown(keyCode, forwardEvent);
        }
        if (action == 1) {
            return this.mTextFilter.onKeyUp(keyCode, forwardEvent);
        }
        if (action != 2) {
            return handled;
        }
        return this.mTextFilter.onKeyMultiple(keyCode, count, event);
    }

    @Override // android.view.View
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

        @Override // android.view.inputmethod.InputConnection
        public boolean reportFullscreenMode(boolean enabled) {
            return AbsListView.this.mDefInputConnection.reportFullscreenMode(enabled);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean performEditorAction(int editorAction) {
            if (editorAction != 6) {
                return false;
            }
            InputMethodManager imm = (InputMethodManager) AbsListView.this.getContext().getSystemService(InputMethodManager.class);
            if (imm == null) {
                return true;
            }
            imm.hideSoftInputFromWindow(AbsListView.this.getWindowToken(), 0);
            return true;
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean sendKeyEvent(KeyEvent event) {
            return AbsListView.this.mDefInputConnection.sendKeyEvent(event);
        }

        @Override // android.view.inputmethod.InputConnection
        public CharSequence getTextBeforeCursor(int n, int flags) {
            InputConnection inputConnection = this.mTarget;
            if (inputConnection == null) {
                return "";
            }
            return inputConnection.getTextBeforeCursor(n, flags);
        }

        @Override // android.view.inputmethod.InputConnection
        public CharSequence getTextAfterCursor(int n, int flags) {
            InputConnection inputConnection = this.mTarget;
            if (inputConnection == null) {
                return "";
            }
            return inputConnection.getTextAfterCursor(n, flags);
        }

        @Override // android.view.inputmethod.InputConnection
        public CharSequence getSelectedText(int flags) {
            InputConnection inputConnection = this.mTarget;
            if (inputConnection == null) {
                return "";
            }
            return inputConnection.getSelectedText(flags);
        }

        @Override // android.view.inputmethod.InputConnection
        public int getCursorCapsMode(int reqModes) {
            InputConnection inputConnection = this.mTarget;
            if (inputConnection == null) {
                return 16384;
            }
            return inputConnection.getCursorCapsMode(reqModes);
        }

        @Override // android.view.inputmethod.InputConnection
        public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
            return getTarget().getExtractedText(request, flags);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            return getTarget().deleteSurroundingText(beforeLength, afterLength);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
            return getTarget().deleteSurroundingTextInCodePoints(beforeLength, afterLength);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            return getTarget().setComposingText(text, newCursorPosition);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean setComposingRegion(int start, int end) {
            return getTarget().setComposingRegion(start, end);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean finishComposingText() {
            InputConnection inputConnection = this.mTarget;
            return inputConnection == null || inputConnection.finishComposingText();
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return getTarget().commitText(text, newCursorPosition);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean commitCompletion(CompletionInfo text) {
            return getTarget().commitCompletion(text);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean commitCorrection(CorrectionInfo correctionInfo) {
            return getTarget().commitCorrection(correctionInfo);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean setSelection(int start, int end) {
            return getTarget().setSelection(start, end);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean performContextMenuAction(int id) {
            return getTarget().performContextMenuAction(id);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean beginBatchEdit() {
            return getTarget().beginBatchEdit();
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean endBatchEdit() {
            return getTarget().endBatchEdit();
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean clearMetaKeyStates(int states) {
            return getTarget().clearMetaKeyStates(states);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean performPrivateCommand(String action, Bundle data) {
            return getTarget().performPrivateCommand(action, data);
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean requestCursorUpdates(int cursorUpdateMode) {
            return getTarget().requestCursorUpdates(cursorUpdateMode);
        }

        @Override // android.view.inputmethod.InputConnection
        public Handler getHandler() {
            return getTarget().getHandler();
        }

        @Override // android.view.inputmethod.InputConnection
        public void closeConnection() {
            getTarget().closeConnection();
        }

        @Override // android.view.inputmethod.InputConnection
        public boolean commitContent(InputContentInfo inputContentInfo, int flags, Bundle opts) {
            return getTarget().commitContent(inputContentInfo, flags, opts);
        }
    }

    @Override // android.view.View
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private EditText getTextFilterInput() {
        if (this.mTextFilter == null) {
            this.mTextFilter = (EditText) LayoutInflater.from(getContext()).inflate(R.layout.typing_filter, (ViewGroup) null);
            this.mTextFilter.setRawInputType(177);
            this.mTextFilter.setImeOptions(268435456);
            this.mTextFilter.addTextChangedListener(this);
        }
        return this.mTextFilter;
    }

    public void clearTextFilter() {
        if (this.mFiltered) {
            getTextFilterInput().setText("");
            this.mFiltered = false;
            PopupWindow popupWindow = this.mPopup;
            if (popupWindow != null && popupWindow.isShowing()) {
                dismissPopup();
            }
        }
    }

    public boolean hasTextFilter() {
        return this.mFiltered;
    }

    @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
    public void onGlobalLayout() {
        PopupWindow popupWindow;
        if (!isShown()) {
            PopupWindow popupWindow2 = this.mPopup;
            if (popupWindow2 != null && popupWindow2.isShowing()) {
                dismissPopup();
            }
        } else if (this.mFiltered && (popupWindow = this.mPopup) != null && !popupWindow.isShowing() && !this.mPopupHidden) {
            showPopup();
        }
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override // android.text.TextWatcher
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
            ListAdapter listAdapter = this.mAdapter;
            if (listAdapter instanceof Filterable) {
                Filter f = ((Filterable) listAdapter).getFilter();
                if (f != null) {
                    f.filter(s, this);
                    return;
                }
                throw new IllegalStateException("You cannot call onTextChanged with a non filterable adapter");
            }
        }
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable s) {
    }

    @Override // android.widget.Filter.FilterListener
    public void onFilterComplete(int count) {
        if (this.mSelectedPosition < 0 && count > 0) {
            this.mResurrectToPosition = -1;
            resurrectSelection();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -2, 0);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public void setTranscriptMode(int mode) {
        this.mTranscriptMode = mode;
    }

    public int getTranscriptMode() {
        return this.mTranscriptMode;
    }

    @Override // android.view.View
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
        EdgeEffect edgeEffect;
        if (shouldDisplayEdgeEffects() && (edgeEffect = this.mEdgeGlowTop) != null) {
            edgeEffect.finish();
            this.mEdgeGlowBottom.finish();
        }
    }

    public void setRemoteViewsAdapter(Intent intent) {
        setRemoteViewsAdapter(intent, false);
    }

    public Runnable setRemoteViewsAdapterAsync(Intent intent) {
        return new RemoteViewsAdapter.AsyncRemoteAdapterAction(this, intent);
    }

    @Override // android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback
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
        RemoteViewsAdapter remoteViewsAdapter = this.mRemoteAdapter;
        if (remoteViewsAdapter != null) {
            remoteViewsAdapter.setRemoteViewsOnClickHandler(handler);
        }
    }

    @Override // android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback
    public void deferNotifyDataSetChanged() {
        this.mDeferNotifyDataSetChanged = true;
    }

    @Override // android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback
    public boolean onRemoteAdapterConnected() {
        RemoteViewsAdapter remoteViewsAdapter = this.mRemoteAdapter;
        if (remoteViewsAdapter != this.mAdapter) {
            setAdapter((ListAdapter) remoteViewsAdapter);
            if (this.mDeferNotifyDataSetChanged) {
                this.mRemoteAdapter.notifyDataSetChanged();
                this.mDeferNotifyDataSetChanged = false;
            }
            return false;
        } else if (remoteViewsAdapter == null) {
            return false;
        } else {
            remoteViewsAdapter.superNotifyDataSetChanged();
            return true;
        }
    }

    @Override // android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback
    public void onRemoteAdapterDisconnected() {
    }

    /* access modifiers changed from: package-private */
    public void setVisibleRangeHint(int start, int end) {
        RemoteViewsAdapter remoteViewsAdapter = this.mRemoteAdapter;
        if (remoteViewsAdapter != null) {
            remoteViewsAdapter.setVisibleRangeHint(start, end);
        }
    }

    public void setEdgeEffectColor(int color) {
        setTopEdgeEffectColor(color);
        setBottomEdgeEffectColor(color);
    }

    public void setBottomEdgeEffectColor(int color) {
        this.mEdgeGlowBottom.setColor(color);
        invalidateBottomGlow();
    }

    public void setTopEdgeEffectColor(int color) {
        this.mEdgeGlowTop.setColor(color);
        invalidateTopGlow();
    }

    public int getTopEdgeEffectColor() {
        return this.mEdgeGlowTop.getColor();
    }

    public int getBottomEdgeEffectColor() {
        return this.mEdgeGlowBottom.getColor();
    }

    public void setRecyclerListener(RecyclerListener listener) {
        this.mRecycler.mRecyclerListener = listener;
    }

    /* access modifiers changed from: package-private */
    public class AdapterDataSetObserver extends AdapterView<ListAdapter>.AdapterDataSetObserver {
        AdapterDataSetObserver() {
            super();
        }

        @Override // android.widget.AdapterView.AdapterDataSetObserver, android.database.DataSetObserver
        public void onChanged() {
            super.onChanged();
            if (AbsListView.this.mFastScroll != null) {
                AbsListView.this.mFastScroll.onSectionsChanged();
            }
        }

        @Override // android.widget.AdapterView.AdapterDataSetObserver, android.database.DataSetObserver
        public void onInvalidated() {
            super.onInvalidated();
            if (AbsListView.this.mFastScroll != null) {
                AbsListView.this.mFastScroll.onSectionsChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class MultiChoiceModeWrapper implements MultiChoiceModeListener {
        private MultiChoiceModeListener mWrapped;

        MultiChoiceModeWrapper() {
        }

        public void setWrapped(MultiChoiceModeListener wrapped) {
            this.mWrapped = wrapped;
        }

        public boolean hasWrappedCallback() {
            return this.mWrapped != null;
        }

        @Override // android.view.ActionMode.Callback
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (!this.mWrapped.onCreateActionMode(mode, menu)) {
                return false;
            }
            AbsListView.this.setLongClickable(false);
            return true;
        }

        @Override // android.view.ActionMode.Callback
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return this.mWrapped.onPrepareActionMode(mode, menu);
        }

        @Override // android.view.ActionMode.Callback
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return this.mWrapped.onActionItemClicked(mode, item);
        }

        @Override // android.view.ActionMode.Callback
        public void onDestroyActionMode(ActionMode mode) {
            this.mWrapped.onDestroyActionMode(mode);
            AbsListView absListView = AbsListView.this;
            absListView.mChoiceActionMode = null;
            absListView.clearChoices();
            AbsListView absListView2 = AbsListView.this;
            absListView2.mDataChanged = true;
            absListView2.rememberSyncState();
            AbsListView.this.requestLayout();
            AbsListView.this.setLongClickable(true);
        }

        @Override // android.widget.AbsListView.MultiChoiceModeListener
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            this.mWrapped.onItemCheckedStateChanged(mode, position, id, checked);
            if (AbsListView.this.getCheckedItemCount() == 0) {
                mode.finish();
                AbsListView.this.clearShiftPositions();
            }
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        @ViewDebug.ExportedProperty(category = Slice.HINT_LIST)
        boolean forceAdd;
        boolean isEnabled;
        long itemId = -1;
        @ViewDebug.ExportedProperty(category = Slice.HINT_LIST)
        boolean recycledHeaderFooter;
        @UnsupportedAppUsage
        int scrappedFromPosition;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        @ViewDebug.ExportedProperty(category = Slice.HINT_LIST, mapping = {@ViewDebug.IntToString(from = -1, to = "ITEM_VIEW_TYPE_IGNORE"), @ViewDebug.IntToString(from = -2, to = "ITEM_VIEW_TYPE_HEADER_OR_FOOTER")})
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
        @Override // android.view.ViewGroup.LayoutParams
        public void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("list:viewType", this.viewType);
            encoder.addProperty("list:recycledHeaderFooter", this.recycledHeaderFooter);
            encoder.addProperty("list:forceAdd", this.forceAdd);
            encoder.addProperty("list:isEnabled", this.isEnabled);
        }
    }

    /* access modifiers changed from: package-private */
    public class RecycleBin {
        private View[] mActiveViews = new View[0];
        private ArrayList<View> mCurrentScrap;
        private int mFirstActivePosition;
        @UnsupportedAppUsage
        private RecyclerListener mRecyclerListener;
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
            SparseArray<View> sparseArray = this.mTransientStateViews;
            if (sparseArray != null) {
                int count = sparseArray.size();
                for (int i3 = 0; i3 < count; i3++) {
                    this.mTransientStateViews.valueAt(i3).forceLayout();
                }
            }
            LongSparseArray<View> longSparseArray = this.mTransientStateViewsById;
            if (longSparseArray != null) {
                int count2 = longSparseArray.size();
                for (int i4 = 0; i4 < count2; i4++) {
                    this.mTransientStateViewsById.valueAt(i4).forceLayout();
                }
            }
        }

        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0;
        }

        /* access modifiers changed from: package-private */
        @UnsupportedAppUsage
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
            int index;
            if (AbsListView.this.mAdapter == null || !AbsListView.this.mAdapterHasStableIds || this.mTransientStateViewsById == null) {
                SparseArray<View> sparseArray = this.mTransientStateViews;
                if (sparseArray == null || (index = sparseArray.indexOfKey(position)) < 0) {
                    return null;
                }
                View result = this.mTransientStateViews.valueAt(index);
                this.mTransientStateViews.removeAt(index);
                return result;
            }
            long id = AbsListView.this.mAdapter.getItemId(position);
            View result2 = this.mTransientStateViewsById.get(id);
            this.mTransientStateViewsById.remove(id);
            return result2;
        }

        /* JADX INFO: Multiple debug info for r2v0 android.util.LongSparseArray<android.view.View>: [D('N' int), D('viewsById' android.util.LongSparseArray<android.view.View>)] */
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
            ArrayList<View>[] arrayListArr = this.mScrapViews;
            if (whichScrap < arrayListArr.length) {
                return retrieveFromScrap(arrayListArr[whichScrap], position);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public void addScrapView(View scrap, int position) {
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
                        RecyclerListener recyclerListener = this.mRecyclerListener;
                        if (recyclerListener != null) {
                            recyclerListener.onMovedToScrapHeap(scrap);
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
                } else if (viewType != -2) {
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
            ArrayList<View> arrayList = this.mSkippedScrap;
            if (arrayList != null) {
                int count = arrayList.size();
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

        /* JADX INFO: Multiple debug info for r3v2 android.util.SparseArray<android.view.View>: [D('transViewsByPos' android.util.SparseArray<android.view.View>), D('i' int)] */
        /* JADX INFO: Multiple debug info for r5v0 android.util.LongSparseArray<android.view.View>: [D('transViewsById' android.util.LongSparseArray<android.view.View>), D('i' int)] */
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

        /* JADX INFO: Multiple debug info for r0v1 android.view.View[]: [D('typeCount' int), D('activeViews' android.view.View[])] */
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
            View[] activeViews = this.mActiveViews;
            for (View victim : activeViews) {
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
                AbsPositionScroller absPositionScroller = this.mPositionScroller;
                if (absPositionScroller != null) {
                    absPositionScroller.stop();
                }
                requestLayout();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
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

    /* access modifiers changed from: package-private */
    public static abstract class AbsPositionScroller {
        public abstract void start(int i);

        public abstract void start(int i, int i2);

        public abstract void startWithOffset(int i, int i2);

        public abstract void startWithOffset(int i, int i2, int i3);

        public abstract void stop();

        AbsPositionScroller() {
        }
    }

    /* access modifiers changed from: package-private */
    public class PositionScroller extends AbsPositionScroller implements Runnable {
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

        @Override // android.widget.AbsListView.AbsPositionScroller
        public void start(final int position) {
            int viewTravelCount;
            stop();
            if (AbsListView.this.mDataChanged) {
                AbsListView.this.mPositionScrollAfterLayout = new Runnable() {
                    /* class android.widget.AbsListView.PositionScroller.AnonymousClass1 */

                    @Override // java.lang.Runnable
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

        @Override // android.widget.AbsListView.AbsPositionScroller
        public void start(final int position, final int boundPosition) {
            int viewTravelCount;
            stop();
            if (boundPosition == -1) {
                start(position);
            } else if (AbsListView.this.mDataChanged) {
                AbsListView.this.mPositionScrollAfterLayout = new Runnable() {
                    /* class android.widget.AbsListView.PositionScroller.AnonymousClass2 */

                    @Override // java.lang.Runnable
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

        @Override // android.widget.AbsListView.AbsPositionScroller
        public void startWithOffset(int position, int offset) {
            startWithOffset(position, offset, 200);
        }

        @Override // android.widget.AbsListView.AbsPositionScroller
        public void startWithOffset(final int position, final int offset, final int duration) {
            int viewTravelCount;
            stop();
            if (AbsListView.this.mDataChanged) {
                AbsListView.this.mPositionScrollAfterLayout = new Runnable() {
                    /* class android.widget.AbsListView.PositionScroller.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        PositionScroller.this.startWithOffset(position, offset, duration);
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
                int i = this.mTargetPos;
                if (i < firstPos) {
                    viewTravelCount = firstPos - i;
                } else if (i > lastPos) {
                    viewTravelCount = i - lastPos;
                } else {
                    AbsListView.this.smoothScrollBy(AbsListView.this.getChildAt(i - firstPos).getTop() - offset2, duration, true, false);
                    return;
                }
                float screenTravelCount = ((float) viewTravelCount) / ((float) childCount);
                this.mScrollDuration = screenTravelCount < 1.0f ? duration : (int) (((float) duration) / screenTravelCount);
                this.mLastSeenPos = -1;
                AbsListView.this.postOnAnimation(this);
            }
        }

        private void scrollToVisible(int targetPos, int boundPos, int duration) {
            int boundPos2 = boundPos;
            int firstPos = AbsListView.this.mFirstPosition;
            int lastPos = (firstPos + AbsListView.this.getChildCount()) - 1;
            int paddedTop = AbsListView.this.mListPadding.top;
            int paddedBottom = AbsListView.this.getHeight() - AbsListView.this.mListPadding.bottom;
            if (targetPos < firstPos || targetPos > lastPos) {
                Log.w(AbsListView.TAG, "scrollToVisible called with targetPos " + targetPos + " not visible [" + firstPos + ", " + lastPos + "]");
            }
            if (boundPos2 < firstPos || boundPos2 > lastPos) {
                boundPos2 = -1;
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
                if (boundPos2 >= 0) {
                    View boundChild = AbsListView.this.getChildAt(boundPos2 - firstPos);
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

        @Override // android.widget.AbsListView.AbsPositionScroller
        public void stop() {
            AbsListView.this.removeCallbacks(this);
        }

        @Override // java.lang.Runnable
        public void run() {
            int i;
            float firstPositionVisiblePart;
            float lastPositionVisiblePart;
            int listHeight = AbsListView.this.getHeight();
            int firstPos = AbsListView.this.mFirstPosition;
            int i2 = this.mMode;
            boolean z = false;
            if (i2 == 1) {
                int lastViewIndex = AbsListView.this.getChildCount() - 1;
                int lastPos = firstPos + lastViewIndex;
                if (lastViewIndex >= 0) {
                    if (lastPos == this.mLastSeenPos) {
                        AbsListView.this.postOnAnimation(this);
                        return;
                    }
                    View lastView = AbsListView.this.getChildAt(lastViewIndex);
                    int scrollBy = (lastView.getHeight() - (listHeight - lastView.getTop())) + (lastPos < AbsListView.this.mItemCount - 1 ? Math.max(AbsListView.this.mListPadding.bottom, this.mExtraScroll) : AbsListView.this.mListPadding.bottom);
                    AbsListView absListView = AbsListView.this;
                    int i3 = this.mScrollDuration;
                    if (lastPos < this.mTargetPos) {
                        z = true;
                    }
                    absListView.smoothScrollBy(scrollBy, i3, true, z);
                    this.mLastSeenPos = lastPos;
                    if (lastPos < this.mTargetPos) {
                        AbsListView.this.postOnAnimation(this);
                    }
                }
            } else if (i2 != 2) {
                if (i2 == 3) {
                    int childCount = AbsListView.this.getChildCount();
                    if (firstPos == this.mBoundPos || childCount <= 1) {
                        i = 0;
                    } else if (firstPos + childCount >= AbsListView.this.mItemCount) {
                        i = 0;
                    } else {
                        int nextPos = firstPos + 1;
                        if (nextPos == this.mLastSeenPos) {
                            AbsListView.this.postOnAnimation(this);
                            return;
                        }
                        View nextView = AbsListView.this.getChildAt(1);
                        int nextViewHeight = nextView.getHeight();
                        int nextViewTop = nextView.getTop();
                        int extraScroll = Math.max(AbsListView.this.mListPadding.bottom, this.mExtraScroll);
                        if (nextPos < this.mBoundPos) {
                            AbsListView.this.smoothScrollBy(Math.max(0, (nextViewHeight + nextViewTop) - extraScroll), this.mScrollDuration, true, true);
                            this.mLastSeenPos = nextPos;
                            AbsListView.this.postOnAnimation(this);
                            return;
                        } else if (nextViewTop > extraScroll) {
                            AbsListView.this.smoothScrollBy(nextViewTop - extraScroll, this.mScrollDuration, true, false);
                            return;
                        } else {
                            AbsListView.this.reportScrollStateChange(0);
                            return;
                        }
                    }
                    AbsListView.this.reportScrollStateChange(i);
                } else if (i2 == 4) {
                    int lastViewIndex2 = AbsListView.this.getChildCount() - 2;
                    if (lastViewIndex2 >= 0) {
                        int lastPos2 = firstPos + lastViewIndex2;
                        if (lastPos2 == this.mLastSeenPos) {
                            AbsListView.this.postOnAnimation(this);
                            return;
                        }
                        View lastView2 = AbsListView.this.getChildAt(lastViewIndex2);
                        int lastViewHeight = lastView2.getHeight();
                        int lastViewTop = lastView2.getTop();
                        int lastViewPixelsShowing = listHeight - lastViewTop;
                        int extraScroll2 = Math.max(AbsListView.this.mListPadding.top, this.mExtraScroll);
                        this.mLastSeenPos = lastPos2;
                        if (lastPos2 > this.mBoundPos) {
                            AbsListView.this.smoothScrollBy(-(lastViewPixelsShowing - extraScroll2), this.mScrollDuration, true, true);
                            AbsListView.this.postOnAnimation(this);
                            return;
                        }
                        int bottom = listHeight - extraScroll2;
                        int lastViewBottom = lastViewTop + lastViewHeight;
                        if (bottom > lastViewBottom) {
                            AbsListView.this.smoothScrollBy(-(bottom - lastViewBottom), this.mScrollDuration, true, false);
                        } else {
                            AbsListView.this.reportScrollStateChange(0);
                        }
                    }
                } else if (i2 == 5) {
                    if (this.mLastSeenPos == firstPos) {
                        AbsListView.this.postOnAnimation(this);
                        return;
                    }
                    this.mLastSeenPos = firstPos;
                    int childCount2 = AbsListView.this.getChildCount();
                    if (childCount2 > 0) {
                        int position = this.mTargetPos;
                        int lastPos3 = (firstPos + childCount2) - 1;
                        View firstChild = AbsListView.this.getChildAt(0);
                        if (firstChild != null) {
                            int firstChildHeight = firstChild.getHeight();
                            View lastChild = AbsListView.this.getChildAt(childCount2 - 1);
                            if (lastChild != null) {
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
                                    viewTravelCount = ((float) (firstPos - position)) + (1.0f - firstPositionVisiblePart) + 1.0f;
                                } else if (position > lastPos3) {
                                    viewTravelCount = ((float) (position - lastPos3)) + (1.0f - lastPositionVisiblePart);
                                }
                                float screenTravelCount = viewTravelCount / ((float) childCount2);
                                float modifier = Math.min(Math.abs(screenTravelCount), 1.0f);
                                if (position < firstPos) {
                                    int distance = (int) (((float) (-AbsListView.this.getHeight())) * modifier);
                                    int duration = (int) (((float) this.mScrollDuration) * modifier);
                                    if (screenTravelCount >= 2.0f) {
                                        duration = 0;
                                    }
                                    AbsListView.this.smoothScrollBy(distance, duration, false, true);
                                    AbsListView.this.postOnAnimation(this);
                                } else if (position > lastPos3) {
                                    AbsListView.this.smoothScrollBy((int) (((float) AbsListView.this.getHeight()) * modifier), (int) (((float) this.mScrollDuration) * modifier), true, true);
                                    AbsListView.this.postOnAnimation(this);
                                } else {
                                    int distance2 = AbsListView.this.getChildAt(position - firstPos).getTop() - this.mOffsetFromTop;
                                    AbsListView.this.smoothScrollBy(distance2, ((int) (((float) this.mScrollDuration) * (((float) Math.abs(distance2)) / ((float) AbsListView.this.getHeight())))) * 10, false, false);
                                }
                            }
                        }
                    }
                }
            } else if (firstPos == this.mLastSeenPos) {
                AbsListView.this.postOnAnimation(this);
            } else {
                boolean z2 = false;
                View firstView = AbsListView.this.getChildAt(0);
                if (firstView != null) {
                    int firstViewTop = firstView.getTop();
                    int extraScroll3 = firstPos > 0 ? Math.max(this.mExtraScroll, AbsListView.this.mListPadding.top) : AbsListView.this.mListPadding.top;
                    AbsListView absListView2 = AbsListView.this;
                    int i4 = firstViewTop - extraScroll3;
                    int i5 = this.mScrollDuration;
                    if (firstPos > this.mTargetPos) {
                        z2 = true;
                    }
                    absListView2.smoothScrollBy(i4, i5, true, z2);
                    this.mLastSeenPos = firstPos;
                    if (firstPos > this.mTargetPos) {
                        AbsListView.this.postOnAnimation(this);
                    }
                }
            }
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

    @Override // android.view.View
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

    public boolean hasAnimatorMask() {
        return hasSpringAnimatorMask();
    }

    public OverScroller getOverScroller() {
        FlingRunnable flingRunnable = this.mFlingRunnable;
        if (flingRunnable != null) {
            return flingRunnable.mScroller;
        }
        return null;
    }

    public HwSpringBackHelper getSpringBackHelper() {
        FlingRunnable flingRunnable = this.mFlingRunnable;
        if (flingRunnable != null) {
            return flingRunnable.mHwSpringBackHelper;
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isNeedOverFlingMoreAtEdge(boolean isScrollerRunning, boolean isAtEnd, int overshoot, int deltaY) {
        return isScrollerRunning;
    }

    public void resetOverScrollState() {
        if (this.mScrollY != 0) {
            invalidateParentCaches();
            finishGlows();
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isNeedFlingOnTop() {
        return false;
    }

    public boolean overScrollYBy(int deltaY, int scrollY, int scrollRangeY, int maxOverScrollY, boolean isTouchEvent) {
        return overScrollBy(0, deltaY, 0, scrollY, 0, scrollRangeY, 0, maxOverScrollY, isTouchEvent);
    }

    /* access modifiers changed from: protected */
    public boolean canOverScroll(int directionY) {
        return true;
    }

    private void createCompoundEventDetector() {
        this.mHwCompoundEventDetector = HwWidgetFactory.getCompoundEventDetector(getContext());
    }

    private void setItemsChecked(boolean isChecked) {
        int[] positions = getMinAndMaxPositions();
        int oldShirtPositionFirst = this.mShiftPositionFirst;
        int oldShirtPositionSecond = this.mShiftPositionSecond;
        for (int i = positions[0]; i <= positions[1]; i++) {
            if (i != this.mShiftPositionFirst || isChecked) {
                setItemChecked(i, isChecked);
            }
        }
        this.mShiftPositionFirst = oldShirtPositionFirst;
        this.mShiftPositionSecond = oldShirtPositionSecond;
    }

    private int[] getMinAndMaxPositions() {
        int[] positions = new int[2];
        int i = this.mShiftPositionFirst;
        int i2 = this.mShiftPositionSecond;
        if (i <= i2) {
            positions[0] = i;
            positions[1] = i2;
        } else {
            positions[0] = i2;
            positions[1] = i;
        }
        return positions;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearShiftPositions() {
        this.mShiftPositionFirst = -1;
        this.mShiftPositionSecond = -1;
    }

    private void updateShiftPositions(int position) {
        this.mShiftPositionFirst = position;
        this.mShiftPositionSecond = -1;
    }

    public void setExtendedMultiChoiceEnabled(boolean isContinuousSelect, boolean isEnabled) {
        if (isContinuousSelect) {
            this.mIsMultiChoiceContinuousEnable = isEnabled;
        } else {
            this.mIsMultiChoiceEnable = isEnabled;
        }
        if (isEnabled && this.mHwCompoundEventDetector == null) {
            createCompoundEventDetector();
            HwCompoundEventDetector hwCompoundEventDetector = this.mHwCompoundEventDetector;
            if (hwCompoundEventDetector != null) {
                hwCompoundEventDetector.setOnMultiSelectEventListener(this, this.mOnMultiSelectListener);
            }
        }
        if (!this.mIsMultiChoiceContinuousEnable && !this.mIsMultiChoiceEnable) {
            this.mHwCompoundEventDetector = null;
        }
    }

    public boolean isExtendedMultiChoiceEnabled(boolean isContinuousSelect) {
        return isContinuousSelect ? this.mIsMultiChoiceContinuousEnable : this.mIsMultiChoiceEnable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetTouchModeToDownIfNeed() {
        if (this.mTouchMode != 7) {
            return;
        }
        if (this.mIsSelectDiscrete || this.mIsSelectContinuous) {
            setTouchMode(0);
        }
    }

    @Override // android.view.View
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        HwGenericEventDetector hwGenericEventDetector = this.mHwGenericEventDetector;
        if (hwGenericEventDetector == null || !hwGenericEventDetector.interceptGenericMotionEvent(event)) {
            return super.dispatchGenericMotionEvent(event);
        }
        return true;
    }

    public void setExtendScrollEnabled(boolean isEnabled) {
        if (!isEnabled) {
            this.mHwGenericEventDetector = null;
            return;
        }
        if (this.mHwGenericEventDetector == null) {
            this.mHwGenericEventDetector = HwWidgetFactory.getGenericEventDetector(getContext());
        }
        HwGenericEventDetector hwGenericEventDetector = this.mHwGenericEventDetector;
        if (hwGenericEventDetector != null) {
            hwGenericEventDetector.setOnScrollListener(this.mOnGenericMotionScrollListener);
        }
    }

    public boolean isExtendScrollEnabled() {
        return this.mHwGenericEventDetector != null;
    }

    public void setSensitivity(float sensitivity) {
        HwGenericEventDetector hwGenericEventDetector = this.mHwGenericEventDetector;
        if (hwGenericEventDetector != null) {
            hwGenericEventDetector.setSensitivity(sensitivity);
        }
    }

    public float getSensitivity() {
        HwGenericEventDetector hwGenericEventDetector = this.mHwGenericEventDetector;
        if (hwGenericEventDetector != null) {
            return hwGenericEventDetector.getSensitivity();
        }
        return 1.0f;
    }

    /* access modifiers changed from: protected */
    public void setSensitivityMode(int mode) {
        HwGenericEventDetector hwGenericEventDetector = this.mHwGenericEventDetector;
        if (hwGenericEventDetector != null) {
            hwGenericEventDetector.setSensitivityMode(mode);
        }
    }

    public void setOnEditEventListener(HwOnEditEventListener listener) {
        if (this.mHwKeyEventDetector == null) {
            this.mHwKeyEventDetector = HwWidgetFactory.getKeyEventDetector(getContext());
        }
        this.mHwKeyEventDetector.setOnEditEventListener(listener);
    }

    public HwOnEditEventListener getOnEditEventListener() {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector != null) {
            return hwKeyEventDetector.getOnEditEventListener();
        }
        return null;
    }

    public void setOnSearchEventListener(HwOnSearchEventListener listener) {
        if (this.mHwKeyEventDetector == null) {
            this.mHwKeyEventDetector = HwWidgetFactory.getKeyEventDetector(getContext());
        }
        this.mHwKeyEventDetector.setOnSearchEventListener(listener);
    }

    public HwOnSearchEventListener getOnSearchEventListener() {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector != null) {
            return hwKeyEventDetector.getOnSearchEventListener();
        }
        return null;
    }
}
