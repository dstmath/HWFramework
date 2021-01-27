package huawei.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import androidhwext.R;
import huawei.android.widget.SwipeItemMangerImpl;
import huawei.android.widget.ViewDragHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SwipeLayout extends FrameLayout {
    private static final int ANGLE_SIXTY = 60;
    private static final int ANGLE_THIRTY = 30;
    private static final int ANGLE_THRESHOLD = 45;
    private static final int CHILD_COUNT_TWO = 2;
    private static final DragEdge DEFAULT_DRAG_EDGE = DragEdge.Right;
    private static final int DEFAULT_LIST_SIZE = 10;
    private static final int DEFAULT_MAP_SIZE = 16;
    private static final int DRAG_BOTTOM = 8;
    private static final int DRAG_LEFT = 1;
    private static final int DRAG_RIGHT = 2;
    private static final int DRAG_TOP = 4;
    public static final int EMPTY_LAYOUT = 0;
    private static final float FLOAT_ZORO_POINT_FIVE = 0.5f;
    private static final int INVALID_GRIVITY = -1;
    private static final float INVALID_VALUE = -1.0f;
    private static final int M_EDGE_SWIPES_OFFSET_SIZE = 4;
    private static float M_WILL_OPEN_PERCENT_AFTER_CLOSE = 0.25f;
    private static float M_WILL_OPEN_PERCENT_AFTER_OPEN = 0.75f;
    private static final String TAG = "SwipeLayout";
    private DragEdge mCurrentDragEdge;
    private DoubleClickListener mDoubleClickListener;
    private float mDownX;
    private float mDownY;
    private int mDragDistance;
    private LinkedHashMap<DragEdge, View> mDragEdges;
    private ViewDragHelper mDragHelper;
    private ViewDragHelper.Callback mDragHelperCallback;
    private float[] mEdgeSwipesOffset;
    private int mEventCounter;
    private GestureDetector mGestureDetector;
    private Rect mHitSurfaceRect;
    private boolean mIsBeingDragged;
    private boolean mIsClickToClose;
    boolean mIsCloseBeforeDrag;
    private boolean mIsDragThenClose;
    private boolean mIsSwipeEnabled;
    private List<OnLayout> mOnLayoutListeners;
    private Map<View, ArrayList<OnRevealListener>> mRevealListeners;
    private Map<View, Boolean> mShowEntirely;
    private ShowMode mShowMode;
    private List<SwipeDenier> mSwipeDeniers;
    private List<SwipeListener> mSwipeListeners;
    private boolean[] mSwipesEnabled;
    private int mTouchSlop;
    private Map<View, Rect> mViewBoundCache;
    private float mWillOpenPercentAfterClose;
    private float mWillOpenPercentAfterOpen;

    public interface DoubleClickListener {
        void onDoubleClick(SwipeLayout swipeLayout, boolean z);
    }

    public interface OnLayout {
        void onLayout(SwipeLayout swipeLayout);
    }

    public interface OnRevealListener {
        void onReveal(View view, DragEdge dragEdge, float f, int i);
    }

    public enum Status {
        Middle,
        Open,
        Close
    }

    public interface SwipeDenier {
        boolean shouldDenySwipe(MotionEvent motionEvent);
    }

    public interface SwipeListener {
        void onClose(SwipeLayout swipeLayout);

        void onDragThenClose(SwipeLayout swipeLayout, float f);

        void onHandRelease(SwipeLayout swipeLayout, float f, float f2);

        void onOpen(SwipeLayout swipeLayout);

        void onStartClose(SwipeLayout swipeLayout);

        void onStartOpen(SwipeLayout swipeLayout);

        void onUpdate(SwipeLayout swipeLayout, int i, int i2);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: huawei.android.widget.SwipeLayout$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$huawei$android$widget$SwipeLayout$DragEdge = new int[DragEdge.values().length];

        static {
            try {
                $SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[DragEdge.Top.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[DragEdge.Bottom.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[DragEdge.Left.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[DragEdge.Right.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public enum DragEdge {
        Left(0),
        Top(1),
        Right(2),
        Bottom(3);
        
        private final int mId;

        private DragEdge(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum ShowMode {
        LayDown(0),
        PullOut(1);
        
        private final int mId;

        private ShowMode(int id) {
            this.mId = id;
        }

        public int getId() {
            return this.mId;
        }
    }

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIsCloseBeforeDrag = true;
        this.mCurrentDragEdge = DEFAULT_DRAG_EDGE;
        this.mDragDistance = 0;
        this.mDragEdges = new LinkedHashMap<>(16);
        this.mEdgeSwipesOffset = new float[4];
        this.mSwipeListeners = new ArrayList((int) DEFAULT_LIST_SIZE);
        this.mSwipeDeniers = new ArrayList((int) DEFAULT_LIST_SIZE);
        this.mRevealListeners = new HashMap(16);
        this.mShowEntirely = new HashMap(16);
        this.mViewBoundCache = new HashMap(16);
        this.mIsSwipeEnabled = true;
        this.mSwipesEnabled = new boolean[]{true, true, true, true};
        this.mIsClickToClose = false;
        this.mIsDragThenClose = false;
        this.mWillOpenPercentAfterOpen = M_WILL_OPEN_PERCENT_AFTER_OPEN;
        this.mWillOpenPercentAfterClose = M_WILL_OPEN_PERCENT_AFTER_CLOSE;
        this.mEventCounter = 0;
        this.mDownX = INVALID_VALUE;
        this.mDownY = INVALID_VALUE;
        this.mDragHelperCallback = new ViewDragHelper.Callback() {
            /* class huawei.android.widget.SwipeLayout.AnonymousClass1 */

            @Override // huawei.android.widget.ViewDragHelper.Callback
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                if (child == SwipeLayout.this.getSurfaceView()) {
                    return clampViewPositionHorizontalSurfaceView(left);
                }
                if (SwipeLayout.this.getCurrentBottomView() == child) {
                    return clampViewPositionHorizontalCurrentBottomView(left);
                }
                return left;
            }

            private int clampViewPositionHorizontalSurfaceView(int left) {
                int i = AnonymousClass2.$SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[SwipeLayout.this.mCurrentDragEdge.ordinal()];
                if (i == 1 || i == 2) {
                    return SwipeLayout.this.getPaddingLeft();
                }
                if (i != 3) {
                    if (i == 4) {
                        if (left > SwipeLayout.this.getPaddingLeft()) {
                            return SwipeLayout.this.getPaddingLeft();
                        }
                        if (left < SwipeLayout.this.getPaddingLeft() - SwipeLayout.this.mDragDistance) {
                            return SwipeLayout.this.getPaddingLeft() - SwipeLayout.this.mDragDistance;
                        }
                    }
                } else if (left < SwipeLayout.this.getPaddingLeft()) {
                    return SwipeLayout.this.getPaddingLeft();
                } else {
                    if (left > SwipeLayout.this.getPaddingLeft() + SwipeLayout.this.mDragDistance) {
                        return SwipeLayout.this.getPaddingLeft() + SwipeLayout.this.mDragDistance;
                    }
                }
                return left;
            }

            private int clampViewPositionHorizontalCurrentBottomView(int left) {
                int i = AnonymousClass2.$SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[SwipeLayout.this.mCurrentDragEdge.ordinal()];
                if (i == 1 || i == 2) {
                    return SwipeLayout.this.getPaddingLeft();
                }
                if (i != 3) {
                    if (i == 4 && SwipeLayout.this.mShowMode == ShowMode.PullOut && left < SwipeLayout.this.getMeasuredWidth() - SwipeLayout.this.mDragDistance) {
                        return SwipeLayout.this.getMeasuredWidth() - SwipeLayout.this.mDragDistance;
                    }
                } else if (SwipeLayout.this.mShowMode == ShowMode.PullOut && left > SwipeLayout.this.getPaddingLeft()) {
                    return SwipeLayout.this.getPaddingLeft();
                }
                return left;
            }

            @Override // huawei.android.widget.ViewDragHelper.Callback
            public int clampViewPositionVertical(View child, int top, int dy) {
                if (child == SwipeLayout.this.getSurfaceView()) {
                    int i = AnonymousClass2.$SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[SwipeLayout.this.mCurrentDragEdge.ordinal()];
                    if (i != 1) {
                        if (i != 2) {
                            if (i == 3 || i == 4) {
                                return SwipeLayout.this.getPaddingTop();
                            }
                        } else if (top < SwipeLayout.this.getPaddingTop() - SwipeLayout.this.mDragDistance) {
                            return SwipeLayout.this.getPaddingTop() - SwipeLayout.this.mDragDistance;
                        } else {
                            if (top > SwipeLayout.this.getPaddingTop()) {
                                return SwipeLayout.this.getPaddingTop();
                            }
                        }
                    } else if (top < SwipeLayout.this.getPaddingTop()) {
                        return SwipeLayout.this.getPaddingTop();
                    } else {
                        if (top > SwipeLayout.this.getPaddingTop() + SwipeLayout.this.mDragDistance) {
                            return SwipeLayout.this.getPaddingTop() + SwipeLayout.this.mDragDistance;
                        }
                    }
                } else {
                    View surfaceView = SwipeLayout.this.getSurfaceView();
                    int surfaceViewTop = surfaceView == null ? 0 : surfaceView.getTop();
                    int i2 = AnonymousClass2.$SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[SwipeLayout.this.mCurrentDragEdge.ordinal()];
                    if (i2 == 1) {
                        return getPositionVertical(top, dy, surfaceViewTop);
                    }
                    if (i2 == 2) {
                        return getBottomPositionVertical(top, dy, surfaceViewTop);
                    }
                    if (i2 == 3 || i2 == 4) {
                        return SwipeLayout.this.getPaddingTop();
                    }
                }
                return top;
            }

            private int getBottomPositionVertical(int top, int dy, int surfaceViewTop) {
                if (SwipeLayout.this.mShowMode == ShowMode.PullOut) {
                    if (top < SwipeLayout.this.getMeasuredHeight() - SwipeLayout.this.mDragDistance) {
                        return SwipeLayout.this.getMeasuredHeight() - SwipeLayout.this.mDragDistance;
                    }
                } else if (surfaceViewTop + dy >= SwipeLayout.this.getPaddingTop()) {
                    return SwipeLayout.this.getPaddingTop();
                } else {
                    if (surfaceViewTop + dy <= SwipeLayout.this.getPaddingTop() - SwipeLayout.this.mDragDistance) {
                        return SwipeLayout.this.getPaddingTop() - SwipeLayout.this.mDragDistance;
                    }
                }
                return top;
            }

            private int getPositionVertical(int top, int dy, int surfaceViewTop) {
                if (SwipeLayout.this.mShowMode == ShowMode.PullOut) {
                    if (top > SwipeLayout.this.getPaddingTop()) {
                        return SwipeLayout.this.getPaddingTop();
                    }
                } else if (surfaceViewTop + dy < SwipeLayout.this.getPaddingTop()) {
                    return SwipeLayout.this.getPaddingTop();
                } else {
                    if (surfaceViewTop + dy > SwipeLayout.this.getPaddingTop() + SwipeLayout.this.mDragDistance) {
                        return SwipeLayout.this.getPaddingTop() + SwipeLayout.this.mDragDistance;
                    }
                }
                return top;
            }

            @Override // huawei.android.widget.ViewDragHelper.Callback
            public boolean tryCaptureView(View child, int pointerId) {
                boolean z = false;
                boolean result = child == SwipeLayout.this.getSurfaceView() || SwipeLayout.this.getBottomViews().contains(child);
                if (result) {
                    SwipeLayout swipeLayout = SwipeLayout.this;
                    if (swipeLayout.getOpenStatus() == Status.Close) {
                        z = true;
                    }
                    swipeLayout.mIsCloseBeforeDrag = z;
                }
                return result;
            }

            @Override // huawei.android.widget.ViewDragHelper.Callback
            public int getViewHorizontalDragRange(View child) {
                return SwipeLayout.this.mDragDistance;
            }

            @Override // huawei.android.widget.ViewDragHelper.Callback
            public int getViewVerticalDragRange(View child) {
                return SwipeLayout.this.mDragDistance;
            }

            @Override // huawei.android.widget.ViewDragHelper.Callback
            public void onViewReleased(View releasedChild, float velocityX, float velocityY) {
                super.onViewReleased(releasedChild, velocityX, velocityY);
                SwipeLayout swipeLayout = SwipeLayout.this;
                swipeLayout.processHandRelease(velocityX, velocityY, swipeLayout.mIsCloseBeforeDrag);
                for (SwipeListener listener : SwipeLayout.this.mSwipeListeners) {
                    listener.onHandRelease(SwipeLayout.this, velocityX, velocityY);
                }
                SwipeLayout.this.invalidate();
            }

            @Override // huawei.android.widget.ViewDragHelper.Callback
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                View surfaceView = SwipeLayout.this.getSurfaceView();
                if (surfaceView != null) {
                    View currentBottomView = SwipeLayout.this.getCurrentBottomView();
                    int evLeft = surfaceView.getLeft();
                    int evRight = surfaceView.getRight();
                    int evTop = surfaceView.getTop();
                    int evBottom = surfaceView.getBottom();
                    if (changedView == surfaceView) {
                        if (SwipeLayout.this.mShowMode == ShowMode.PullOut && currentBottomView != null) {
                            if (SwipeLayout.this.mCurrentDragEdge == DragEdge.Left || SwipeLayout.this.mCurrentDragEdge == DragEdge.Right) {
                                currentBottomView.offsetLeftAndRight(dx);
                            } else {
                                currentBottomView.offsetTopAndBottom(dy);
                            }
                        }
                    } else if (SwipeLayout.this.getBottomViews().contains(changedView)) {
                        if (SwipeLayout.this.mShowMode == ShowMode.PullOut) {
                            surfaceView.offsetLeftAndRight(dx);
                            surfaceView.offsetTopAndBottom(dy);
                        } else {
                            SwipeLayout swipeLayout = SwipeLayout.this;
                            Rect rect = swipeLayout.computeBottomLayDown(swipeLayout.mCurrentDragEdge);
                            if (currentBottomView != null) {
                                currentBottomView.layout(rect.left, rect.top, rect.right, rect.bottom);
                            }
                            int newLeft = surfaceView.getLeft() + dx;
                            int newTop = surfaceView.getTop() + dy;
                            if (SwipeLayout.this.mCurrentDragEdge == DragEdge.Left && newLeft < SwipeLayout.this.getPaddingLeft()) {
                                newLeft = SwipeLayout.this.getPaddingLeft();
                            } else if (SwipeLayout.this.mCurrentDragEdge == DragEdge.Right && newLeft > SwipeLayout.this.getPaddingLeft()) {
                                newLeft = SwipeLayout.this.getPaddingLeft();
                            } else if (SwipeLayout.this.mCurrentDragEdge == DragEdge.Top && newTop < SwipeLayout.this.getPaddingTop()) {
                                newTop = SwipeLayout.this.getPaddingTop();
                            } else if (SwipeLayout.this.mCurrentDragEdge == DragEdge.Bottom && newTop > SwipeLayout.this.getPaddingTop()) {
                                newTop = SwipeLayout.this.getPaddingTop();
                            }
                            surfaceView.layout(newLeft, newTop, SwipeLayout.this.getMeasuredWidth() + newLeft, SwipeLayout.this.getMeasuredHeight() + newTop);
                        }
                    }
                    SwipeLayout.this.dispatchRevealEvent(evLeft, evTop, evRight, evBottom);
                    SwipeLayout.this.dispatchSwipeEvent(evLeft, evTop, dx, dy);
                    SwipeLayout.this.invalidate();
                    SwipeLayout.this.captureChildrenBound();
                }
            }
        };
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mDragHelper = ViewDragHelper.create(this, this.mDragHelperCallback);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout);
        this.mEdgeSwipesOffset[DragEdge.Left.getId()] = array.getDimension(1, 0.0f);
        this.mEdgeSwipesOffset[DragEdge.Right.getId()] = array.getDimension(2, 0.0f);
        this.mEdgeSwipesOffset[DragEdge.Top.getId()] = array.getDimension(3, 0.0f);
        this.mEdgeSwipesOffset[DragEdge.Bottom.getId()] = array.getDimension(4, 0.0f);
        setClickToClose(array.getBoolean(6, this.mIsClickToClose));
        setDragThenClose(array.getBoolean(7, this.mIsDragThenClose));
        int dragEdgeChoices = array.getInt(0, 0);
        if ((dragEdgeChoices & 1) == 1) {
            this.mDragEdges.put(DragEdge.Left, null);
        }
        if ((dragEdgeChoices & 2) == 2) {
            this.mDragEdges.put(DragEdge.Right, null);
        }
        if ((dragEdgeChoices & 4) == 4) {
            this.mDragEdges.put(DragEdge.Top, null);
        }
        if ((dragEdgeChoices & 8) == 8) {
            this.mDragEdges.put(DragEdge.Bottom, null);
        }
        this.mShowMode = ShowMode.values()[array.getInt(5, ShowMode.PullOut.getId())];
        array.recycle();
    }

    public void addSwipeListener(SwipeListener listener) {
        this.mSwipeListeners.add(listener);
    }

    public void removeSwipeListener(SwipeListener listener) {
        this.mSwipeListeners.remove(listener);
    }

    public void removeAllSwipeListener() {
        this.mSwipeListeners.clear();
    }

    public boolean hasUserSetSwipeListener() {
        if (this.mSwipeListeners.isEmpty()) {
            return false;
        }
        boolean isHasUserListener = false;
        for (SwipeListener listener : this.mSwipeListeners) {
            if (!(listener instanceof SwipeItemMangerImpl.SwipeMemory)) {
                isHasUserListener = true;
            }
        }
        return isHasUserListener;
    }

    public void addSwipeDenier(SwipeDenier denier) {
        List<SwipeDenier> list = this.mSwipeDeniers;
        if (list != null && denier != null) {
            list.add(denier);
        }
    }

    public void removeSwipeDenier(SwipeDenier denier) {
        List<SwipeDenier> list = this.mSwipeDeniers;
        if (list != null && denier != null) {
            list.remove(denier);
        }
    }

    public void removeAllSwipeDeniers() {
        List<SwipeDenier> list = this.mSwipeDeniers;
        if (list != null) {
            list.clear();
        }
    }

    public void addRevealListener(int childId, OnRevealListener listener) {
        View child = findViewById(childId);
        if (child != null) {
            if (!this.mShowEntirely.containsKey(child)) {
                this.mShowEntirely.put(child, false);
            }
            if (this.mRevealListeners.get(child) == null) {
                this.mRevealListeners.put(child, new ArrayList<>((int) DEFAULT_LIST_SIZE));
            }
            this.mRevealListeners.get(child).add(listener);
            return;
        }
        throw new IllegalArgumentException("Child does not belong to SwipeListener.");
    }

    public void addRevealListener(int[] childIds, OnRevealListener listener) {
        for (int i : childIds) {
            addRevealListener(i, listener);
        }
    }

    public void removeRevealListener(int childId, OnRevealListener listener) {
        View child = findViewById(childId);
        if (child != null) {
            this.mShowEntirely.remove(child);
            if (this.mRevealListeners.containsKey(child)) {
                this.mRevealListeners.get(child).remove(listener);
            }
        }
    }

    public void removeAllRevealListeners(int childId) {
        View child = findViewById(childId);
        if (child != null) {
            this.mRevealListeners.remove(child);
            this.mShowEntirely.remove(child);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void captureChildrenBound() {
        View currentSurfaceView = getSurfaceView();
        View currentBottomView = getCurrentBottomView();
        if (getOpenStatus() == Status.Close) {
            this.mViewBoundCache.remove(currentSurfaceView);
            this.mViewBoundCache.remove(currentBottomView);
            return;
        }
        View[] views = {currentSurfaceView, currentBottomView};
        for (View child : views) {
            Rect rect = this.mViewBoundCache.get(child);
            if (rect == null) {
                rect = new Rect();
                this.mViewBoundCache.put(child, rect);
            }
            if (child != null) {
                rect.left = child.getLeft();
                rect.top = child.getTop();
                rect.right = child.getRight();
                rect.bottom = child.getBottom();
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003e, code lost:
        if (r20 >= r6) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0059, code lost:
        if (r23 <= r7) goto L_0x005f;
     */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x005c  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0062 A[ADDED_TO_REGION] */
    public boolean isViewTotallyFirstShowed(View child, Rect relativePosition, DragEdge edge, int surfaceLeft, int surfaceTop, int surfaceRight, int surfaceBottom) {
        boolean leftAndRight;
        if (this.mShowEntirely.get(child).booleanValue()) {
            return false;
        }
        int childLeft = relativePosition.left;
        int childRight = relativePosition.right;
        int childTop = relativePosition.top;
        int childBottom = relativePosition.bottom;
        boolean topAndButtom = true;
        if (getShowMode() == ShowMode.LayDown) {
            if (edge == DragEdge.Right) {
                if (surfaceRight <= childLeft) {
                    leftAndRight = true;
                    if (edge != DragEdge.Top) {
                        if (surfaceTop >= childBottom) {
                            if (!leftAndRight || topAndButtom) {
                                return true;
                            }
                            return false;
                        }
                    }
                    if (edge != DragEdge.Bottom) {
                    }
                    topAndButtom = false;
                    if (!leftAndRight) {
                    }
                    return true;
                }
            }
            if (edge == DragEdge.Left) {
            }
            leftAndRight = false;
            if (edge != DragEdge.Top) {
            }
            if (edge != DragEdge.Bottom) {
            }
            topAndButtom = false;
            if (!leftAndRight) {
            }
            return true;
        } else if (getShowMode() != ShowMode.PullOut) {
            return false;
        } else {
            boolean leftAndRight2 = (edge == DragEdge.Right && childRight <= getWidth()) || (edge == DragEdge.Left && childLeft >= getPaddingLeft());
            if ((edge != DragEdge.Top || childTop < getPaddingTop()) && (edge != DragEdge.Bottom || childBottom > getHeight())) {
                topAndButtom = false;
            }
            if (leftAndRight2 || topAndButtom) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isViewShowing(View child, Rect relativePosition, DragEdge availableEdge, int surfaceLeft, int surfaceTop, int surfaceRight, int surfaceBottom) {
        int childLeft = relativePosition.left;
        int childRight = relativePosition.right;
        int childTop = relativePosition.top;
        int childBottom = relativePosition.bottom;
        if (getShowMode() == ShowMode.LayDown) {
            int i = AnonymousClass2.$SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[availableEdge.ordinal()];
            if (i == 1) {
                return surfaceTop >= childTop && surfaceTop < childBottom;
            }
            if (i == 2) {
                return surfaceBottom > childTop && surfaceBottom <= childBottom;
            }
            if (i == 3) {
                return surfaceLeft < childRight && surfaceLeft >= childLeft;
            }
            if (i != 4) {
                return false;
            }
            return surfaceRight > childLeft && surfaceRight <= childRight;
        } else if (getShowMode() != ShowMode.PullOut) {
            return false;
        } else {
            int i2 = AnonymousClass2.$SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[availableEdge.ordinal()];
            if (i2 == 1) {
                return childTop < getPaddingTop() && childBottom >= getPaddingTop();
            }
            if (i2 == 2) {
                return childTop < getHeight() && childTop >= getPaddingTop();
            }
            if (i2 == 3) {
                return childRight >= getPaddingLeft() && childLeft < getPaddingLeft();
            }
            if (i2 != 4) {
                return false;
            }
            return childLeft <= getWidth() && childRight > getWidth();
        }
    }

    /* access modifiers changed from: protected */
    public Rect getRelativePosition(View child) {
        View curView = child;
        Rect rect = new Rect(curView.getLeft(), curView.getTop(), 0, 0);
        while (curView.getParent() != null && curView != getRootView()) {
            ViewParent view = curView.getParent();
            if (!(view instanceof View) || (curView = (View) view) == this) {
                break;
            }
            rect.left += curView.getLeft();
            rect.top += curView.getTop();
        }
        rect.right = rect.left + child.getMeasuredWidth();
        rect.bottom = rect.top + child.getMeasuredHeight();
        return rect;
    }

    /* access modifiers changed from: protected */
    public void dispatchSwipeEvent(int surfaceLeft, int surfaceTop, int dx, int dy) {
        boolean isOpen = true;
        int i = AnonymousClass2.$SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[getDragEdge().ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i == 4 && dx > 0) {
                        isOpen = false;
                    }
                } else if (dx < 0) {
                    isOpen = false;
                }
            } else if (dy > 0) {
                isOpen = false;
            }
        } else if (dy < 0) {
            isOpen = false;
        }
        dispatchSwipeEvent(surfaceLeft, surfaceTop, isOpen);
    }

    /* access modifiers changed from: protected */
    public void dispatchSwipeEvent(int surfaceLeft, int surfaceTop, boolean isOpen) {
        safeBottomView();
        Status status = getOpenStatus();
        if (!this.mSwipeListeners.isEmpty()) {
            this.mEventCounter++;
            for (SwipeListener listener : this.mSwipeListeners) {
                if (this.mEventCounter == 1) {
                    if (isOpen) {
                        listener.onStartOpen(this);
                    } else {
                        listener.onStartClose(this);
                    }
                }
                listener.onUpdate(this, surfaceLeft - getPaddingLeft(), surfaceTop - getPaddingTop());
            }
            if (status == Status.Close) {
                for (SwipeListener listener2 : this.mSwipeListeners) {
                    listener2.onClose(this);
                }
                this.mEventCounter = 0;
            }
            if (status == Status.Open) {
                View currentBottomView = getCurrentBottomView();
                if (currentBottomView != null) {
                    currentBottomView.setEnabled(true);
                }
                for (SwipeListener listener3 : this.mSwipeListeners) {
                    listener3.onOpen(this);
                }
                this.mEventCounter = 0;
            }
        }
    }

    private void safeBottomView() {
        Status status = getOpenStatus();
        List<View> bottoms = getBottomViews();
        if (status == Status.Close) {
            for (View bottom : bottoms) {
                if (!(bottom == null || bottom.getVisibility() == 4)) {
                    bottom.setVisibility(4);
                }
            }
            return;
        }
        View currentBottomView = getCurrentBottomView();
        if (!(currentBottomView == null || currentBottomView.getVisibility() == 0)) {
            currentBottomView.setVisibility(0);
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchRevealEvent(int surfaceLeft, int surfaceTop, int surfaceRight, int surfaceBottom) {
        if (!this.mRevealListeners.isEmpty()) {
            for (Map.Entry<View, ArrayList<OnRevealListener>> entry : this.mRevealListeners.entrySet()) {
                View child = entry.getKey();
                Rect rect = getRelativePosition(child);
                if (isViewShowing(child, rect, this.mCurrentDragEdge, surfaceLeft, surfaceTop, surfaceRight, surfaceBottom)) {
                    this.mShowEntirely.put(child, false);
                    AnimParam animParam = new AnimParam();
                    if (getShowMode() == ShowMode.LayDown) {
                        animParam = getAnimParamWhenLayDown(rect, new Rect(surfaceLeft, surfaceTop, surfaceRight, surfaceBottom), child);
                    } else if (getShowMode() == ShowMode.PullOut) {
                        animParam = getAnimParamWhenPullOut(rect, child);
                    } else {
                        animParam.setDistance(0);
                        animParam.setFraction(0.0f);
                    }
                    int distance = animParam.getDistance();
                    float fraction = animParam.getFraction();
                    Iterator<OnRevealListener> it = entry.getValue().iterator();
                    while (it.hasNext()) {
                        it.next().onReveal(child, this.mCurrentDragEdge, Math.abs(fraction), distance);
                        if (Math.abs(fraction) == 1.0f) {
                            this.mShowEntirely.put(child, true);
                        }
                    }
                }
                if (isViewTotallyFirstShowed(child, rect, this.mCurrentDragEdge, surfaceLeft, surfaceTop, surfaceRight, surfaceBottom)) {
                    notifyListeners(child, entry);
                }
            }
        }
    }

    private void notifyListeners(View child, Map.Entry<View, ArrayList<OnRevealListener>> entry) {
        this.mShowEntirely.put(child, true);
        Iterator<OnRevealListener> it = entry.getValue().iterator();
        while (it.hasNext()) {
            OnRevealListener listener = it.next();
            if (this.mCurrentDragEdge == DragEdge.Left || this.mCurrentDragEdge == DragEdge.Right) {
                listener.onReveal(child, this.mCurrentDragEdge, 1.0f, child.getWidth());
            } else {
                listener.onReveal(child, this.mCurrentDragEdge, 1.0f, child.getHeight());
            }
        }
    }

    private AnimParam getAnimParamWhenLayDown(Rect rect, Rect surfaceRect, View child) {
        AnimParam animParam = new AnimParam();
        int distance = 0;
        float fraction = 0.0f;
        int i = AnonymousClass2.$SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[this.mCurrentDragEdge.ordinal()];
        if (i == 1) {
            distance = rect.top - surfaceRect.top;
            fraction = calFraction(distance, child.getHeight());
        } else if (i == 2) {
            distance = rect.bottom - surfaceRect.bottom;
            fraction = calFraction(distance, child.getHeight());
        } else if (i == 3) {
            distance = rect.left - surfaceRect.left;
            fraction = calFraction(distance, child.getWidth());
        } else if (i == 4) {
            distance = rect.right - surfaceRect.right;
            fraction = calFraction(distance, child.getWidth());
        }
        animParam.setDistance(distance);
        animParam.setFraction(fraction);
        return animParam;
    }

    private AnimParam getAnimParamWhenPullOut(Rect rect, View child) {
        AnimParam animParam = new AnimParam();
        int distance = 0;
        float fraction = 0.0f;
        int i = AnonymousClass2.$SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[this.mCurrentDragEdge.ordinal()];
        if (i == 1) {
            distance = rect.bottom - getPaddingTop();
            fraction = calFraction(distance, child.getHeight());
        } else if (i == 2) {
            distance = rect.top - getHeight();
            fraction = calFraction(distance, child.getHeight());
        } else if (i == 3) {
            distance = rect.right - getPaddingLeft();
            fraction = calFraction(distance, child.getWidth());
        } else if (i == 4) {
            distance = rect.left - getWidth();
            fraction = calFraction(distance, child.getWidth());
        }
        animParam.setDistance(distance);
        animParam.setFraction(fraction);
        return animParam;
    }

    private float calFraction(int distance, int total) {
        if (total == 0) {
            return 0.0f;
        }
        return (float) (distance / total);
    }

    @Override // android.view.View
    public void computeScroll() {
        super.computeScroll();
        if (this.mDragHelper.continueSettling(true)) {
            postInvalidateOnAnimation();
        }
    }

    /* access modifiers changed from: private */
    public class AnimParam {
        private int mDistance;
        private float mFraction;

        private AnimParam() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getDistance() {
            return this.mDistance;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private float getFraction() {
            return this.mFraction;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setDistance(int distance) {
            this.mDistance = distance;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setFraction(float fraction) {
            this.mFraction = fraction;
        }
    }

    public void addOnLayoutListener(OnLayout layout) {
        if (layout != null) {
            if (this.mOnLayoutListeners == null) {
                this.mOnLayoutListeners = new ArrayList((int) DEFAULT_LIST_SIZE);
            }
            this.mOnLayoutListeners.add(layout);
        }
    }

    public void removeOnLayoutListener(OnLayout layout) {
        List<OnLayout> list;
        if (layout != null && (list = this.mOnLayoutListeners) != null) {
            list.remove(layout);
        }
    }

    public void clearDragEdge() {
        this.mDragEdges.clear();
    }

    public void setDrag(DragEdge dragEdge, int childId) {
        clearDragEdge();
        addDrag(dragEdge, childId);
    }

    public void setDrag(DragEdge dragEdge, View child) {
        clearDragEdge();
        addDrag(dragEdge, child);
    }

    public void addDrag(DragEdge dragEdge, int childId) {
        addDrag(dragEdge, findViewById(childId), null);
    }

    public void addDrag(DragEdge dragEdge, View child) {
        addDrag(dragEdge, child, null);
    }

    public void addDrag(DragEdge dragEdge, View child, ViewGroup.LayoutParams params) {
        if (child != null) {
            ViewGroup.LayoutParams paramsTemp = params;
            if (params == null) {
                paramsTemp = generateDefaultLayoutParams();
            }
            if (!checkLayoutParams(paramsTemp)) {
                paramsTemp = generateLayoutParams(paramsTemp);
            }
            int gravity = -1;
            int i = AnonymousClass2.$SwitchMap$huawei$android$widget$SwipeLayout$DragEdge[dragEdge.ordinal()];
            if (i == 1) {
                gravity = 48;
            } else if (i == 2) {
                gravity = 80;
            } else if (i == 3) {
                gravity = 3;
            } else if (i == 4) {
                gravity = 5;
            }
            if (paramsTemp instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) paramsTemp).gravity = gravity;
            }
            addView(child, 0, paramsTemp);
        }
    }

    @Override // android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child != null) {
            int gravity = 0;
            try {
                Object obj = params.getClass().getField("gravity").get(params);
                if (obj instanceof Integer) {
                    gravity = ((Integer) obj).intValue();
                }
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "addView NoSuchFieldException error");
            } catch (Exception e2) {
                Log.e(TAG, "addView error");
            }
            if (gravity <= 0) {
                Iterator<Map.Entry<DragEdge, View>> it = this.mDragEdges.entrySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Map.Entry<DragEdge, View> entry = it.next();
                    if (entry.getValue() == null) {
                        this.mDragEdges.put(entry.getKey(), child);
                        break;
                    }
                }
            } else {
                int gravity2 = Gravity.getAbsoluteGravity(gravity, getLayoutDirection());
                if ((gravity2 & 3) == 3) {
                    this.mDragEdges.put(DragEdge.Left, child);
                }
                if ((gravity2 & 5) == 5) {
                    this.mDragEdges.put(DragEdge.Right, child);
                }
                if ((gravity2 & 48) == 48) {
                    this.mDragEdges.put(DragEdge.Top, child);
                }
                if ((gravity2 & 80) == 80) {
                    this.mDragEdges.put(DragEdge.Bottom, child);
                }
            }
            if (child.getParent() != this) {
                super.addView(child, index, params);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        if (Status.Middle != getOpenStatus()) {
            updateBottomViews();
            List<OnLayout> list = this.mOnLayoutListeners;
            if (list != null) {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    this.mOnLayoutListeners.get(i).onLayout(this);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void layoutPullOut() {
        View surfaceView = getSurfaceView();
        Rect surfaceRect = this.mViewBoundCache.get(surfaceView);
        if (surfaceRect == null) {
            surfaceRect = computeSurfaceLayoutArea(false);
        }
        if (surfaceView != null) {
            surfaceView.layout(surfaceRect.left, surfaceRect.top, surfaceRect.right, surfaceRect.bottom);
            bringChildToFront(surfaceView);
        }
        View currentBottomView = getCurrentBottomView();
        Rect bottomViewRect = this.mViewBoundCache.get(currentBottomView);
        if (bottomViewRect == null) {
            bottomViewRect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, surfaceRect);
        }
        if (currentBottomView != null) {
            currentBottomView.layout(bottomViewRect.left, bottomViewRect.top, bottomViewRect.right, bottomViewRect.bottom);
        }
    }

    /* access modifiers changed from: package-private */
    public void layoutLayDown() {
        View surfaceView = getSurfaceView();
        Rect surfaceRect = this.mViewBoundCache.get(surfaceView);
        if (surfaceRect == null) {
            surfaceRect = computeSurfaceLayoutArea(false);
        }
        if (surfaceView != null) {
            surfaceView.layout(surfaceRect.left, surfaceRect.top, surfaceRect.right, surfaceRect.bottom);
            bringChildToFront(surfaceView);
        }
        View currentBottomView = getCurrentBottomView();
        Rect bottomViewRect = this.mViewBoundCache.get(currentBottomView);
        if (bottomViewRect == null) {
            bottomViewRect = computeBottomLayoutAreaViaSurface(ShowMode.LayDown, surfaceRect);
        }
        if (currentBottomView != null) {
            currentBottomView.layout(bottomViewRect.left, bottomViewRect.top, bottomViewRect.right, bottomViewRect.bottom);
        }
    }

    private void checkCanDrag(MotionEvent event) {
        DragEdge dragEdge;
        if (!this.mIsBeingDragged) {
            if (getOpenStatus() == Status.Middle) {
                this.mIsBeingDragged = true;
                return;
            }
            Status status = getOpenStatus();
            float distanceX = event.getRawX() - this.mDownX;
            float distanceY = event.getRawY() - this.mDownY;
            float angle = 0.0f;
            if (Float.compare(distanceX, 0.0f) != 0) {
                angle = Math.abs(distanceY / distanceX);
            }
            float angle2 = (float) Math.toDegrees(Math.atan((double) angle));
            if (getOpenStatus() == Status.Close) {
                if (angle2 < 45.0f) {
                    if (distanceX > 0.0f && isLeftSwipeEnabled()) {
                        dragEdge = DragEdge.Left;
                    } else if (distanceX < 0.0f && isRightSwipeEnabled()) {
                        dragEdge = DragEdge.Right;
                    } else {
                        return;
                    }
                } else if (distanceY > 0.0f && isTopSwipeEnabled()) {
                    dragEdge = DragEdge.Top;
                } else if (distanceY < 0.0f && isBottomSwipeEnabled()) {
                    dragEdge = DragEdge.Bottom;
                } else {
                    return;
                }
                setCurrentDragEdge(dragEdge);
            }
            checkDrag(status, distanceX, distanceY, angle2);
        }
    }

    private void checkDrag(Status status, float distanceX, float distanceY, float angle) {
        boolean isDoNothing = false;
        boolean z = true;
        if (this.mCurrentDragEdge == DragEdge.Right) {
            boolean suitable = ((status == Status.Open && (distanceX > ((float) this.mTouchSlop) ? 1 : (distanceX == ((float) this.mTouchSlop) ? 0 : -1)) > 0) || (status == Status.Close && (distanceX > ((float) (-this.mTouchSlop)) ? 1 : (distanceX == ((float) (-this.mTouchSlop)) ? 0 : -1)) < 0)) || status == Status.Middle;
            if (angle > 30.0f || !suitable) {
                isDoNothing = true;
            }
        }
        if (this.mCurrentDragEdge == DragEdge.Left) {
            boolean suitable2 = ((status == Status.Open && (distanceX > ((float) (-this.mTouchSlop)) ? 1 : (distanceX == ((float) (-this.mTouchSlop)) ? 0 : -1)) < 0) || (status == Status.Close && (distanceX > ((float) this.mTouchSlop) ? 1 : (distanceX == ((float) this.mTouchSlop) ? 0 : -1)) > 0)) || status == Status.Middle;
            if (angle > 30.0f || !suitable2) {
                isDoNothing = true;
            }
        }
        if (this.mCurrentDragEdge == DragEdge.Top) {
            boolean suitable3 = ((status == Status.Open && (distanceY > ((float) (-this.mTouchSlop)) ? 1 : (distanceY == ((float) (-this.mTouchSlop)) ? 0 : -1)) < 0) || (status == Status.Close && (distanceY > ((float) this.mTouchSlop) ? 1 : (distanceY == ((float) this.mTouchSlop) ? 0 : -1)) > 0)) || status == Status.Middle;
            if (angle < 60.0f || !suitable3) {
                isDoNothing = true;
            }
        }
        if (this.mCurrentDragEdge == DragEdge.Bottom) {
            boolean suitable4 = ((status == Status.Open && (distanceY > ((float) this.mTouchSlop) ? 1 : (distanceY == ((float) this.mTouchSlop) ? 0 : -1)) > 0) || (status == Status.Close && (distanceY > ((float) (-this.mTouchSlop)) ? 1 : (distanceY == ((float) (-this.mTouchSlop)) ? 0 : -1)) < 0)) || status == Status.Middle;
            if (angle < 60.0f || !suitable4) {
                isDoNothing = true;
            }
        }
        if (isDoNothing) {
            z = false;
        }
        this.mIsBeingDragged = z;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent event) {
        ViewParent parent;
        if (!isSwipeEnabled()) {
            return false;
        }
        if (this.mIsClickToClose && getOpenStatus() == Status.Open && isTouchOnSurface(event)) {
            return true;
        }
        for (SwipeDenier denier : this.mSwipeDeniers) {
            if (denier != null && denier.shouldDenySwipe(event)) {
                return false;
            }
        }
        int action = event.getAction();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    boolean isBeforeCheck = this.mIsBeingDragged;
                    checkCanDrag(event);
                    if (this.mIsBeingDragged && (parent = getParent()) != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    if (!isBeforeCheck && this.mIsBeingDragged) {
                        return false;
                    }
                } else if (action != 3) {
                    this.mDragHelper.processTouchEvent(event);
                }
            }
            this.mIsBeingDragged = false;
            this.mDragHelper.processTouchEvent(event);
        } else {
            this.mDragHelper.processTouchEvent(event);
            this.mIsBeingDragged = false;
            this.mDownX = event.getRawX();
            this.mDownY = event.getRawY();
            if (getOpenStatus() == Status.Middle) {
                this.mIsBeingDragged = true;
            }
        }
        return this.mIsBeingDragged;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        if (!isSwipeEnabled()) {
            return super.onTouchEvent(event);
        }
        int action = event.getActionMasked();
        GestureDetector gestureDetector = this.mGestureDetector;
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    checkCanDrag(event);
                    if (this.mIsBeingDragged) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        this.mDragHelper.processTouchEvent(event);
                    }
                } else if (action != 3) {
                    this.mDragHelper.processTouchEvent(event);
                }
            }
            this.mIsBeingDragged = false;
            this.mDragHelper.processTouchEvent(event);
        } else {
            this.mDragHelper.processTouchEvent(event);
            this.mDownX = event.getRawX();
            this.mDownY = event.getRawY();
        }
        if (super.onTouchEvent(event) || this.mIsBeingDragged || action == 0) {
            return true;
        }
        return false;
    }

    public boolean isClickToClose() {
        return this.mIsClickToClose;
    }

    public void setClickToClose(boolean isEnabled) {
        this.mIsClickToClose = isEnabled;
    }

    public void setDragThenClose(boolean isEnabled) {
        this.mIsDragThenClose = isEnabled;
    }

    public void setSwipeEnabled(boolean isEnabled) {
        this.mIsSwipeEnabled = isEnabled;
    }

    public boolean isSwipeEnabled() {
        return this.mIsSwipeEnabled;
    }

    public boolean isLeftSwipeEnabled() {
        View bottomView = this.mDragEdges.get(DragEdge.Left);
        return bottomView != null && bottomView.getParent() == this && bottomView != getSurfaceView() && this.mSwipesEnabled[DragEdge.Left.getId()];
    }

    public void setLeftSwipeEnabled(boolean isLeftSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Left.getId()] = isLeftSwipeEnabled;
    }

    public boolean isRightSwipeEnabled() {
        View bottomView = this.mDragEdges.get(DragEdge.Right);
        return bottomView != null && bottomView.getParent() == this && bottomView != getSurfaceView() && this.mSwipesEnabled[DragEdge.Right.getId()];
    }

    public void setRightSwipeEnabled(boolean isRightSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Right.getId()] = isRightSwipeEnabled;
    }

    public boolean isTopSwipeEnabled() {
        View bottomView = this.mDragEdges.get(DragEdge.Top);
        return bottomView != null && bottomView.getParent() == this && bottomView != getSurfaceView() && this.mSwipesEnabled[DragEdge.Top.getId()];
    }

    public void setTopSwipeEnabled(boolean isTopSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Top.getId()] = isTopSwipeEnabled;
    }

    public boolean isBottomSwipeEnabled() {
        View bottomView = this.mDragEdges.get(DragEdge.Bottom);
        return bottomView != null && bottomView.getParent() == this && bottomView != getSurfaceView() && this.mSwipesEnabled[DragEdge.Bottom.getId()];
    }

    public void setBottomSwipeEnabled(boolean isBbottomSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Bottom.getId()] = isBbottomSwipeEnabled;
    }

    public float getWillOpenPercentAfterOpen() {
        return this.mWillOpenPercentAfterOpen;
    }

    public void setWillOpenPercentAfterOpen(float willOpenPercentAfterOpen) {
        this.mWillOpenPercentAfterOpen = willOpenPercentAfterOpen;
    }

    public float getWillOpenPercentAfterClose() {
        return this.mWillOpenPercentAfterClose;
    }

    public void setWillOpenPercentAfterClose(float willOpenPercentAfterClose) {
        this.mWillOpenPercentAfterClose = willOpenPercentAfterClose;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean insideAdapterView() {
        return getAdapterView() != null;
    }

    private AdapterView getAdapterView() {
        ViewParent viewParent = getParent();
        if (viewParent instanceof AdapterView) {
            return (AdapterView) viewParent;
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performAdapterViewItemClick() {
        AdapterView view;
        int position;
        if (getOpenStatus() == Status.Close) {
            ViewParent viewParent = getParent();
            if ((viewParent instanceof AdapterView) && (position = (view = (AdapterView) viewParent).getPositionForView(this)) != -1) {
                view.performItemClick(view.getChildAt(position - view.getFirstVisiblePosition()), position, view.getAdapter().getItemId(position));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean performAdapterViewItemLongClick() {
        AdapterView view;
        int position;
        if (getOpenStatus() != Status.Close) {
            return false;
        }
        ViewParent viewParent = getParent();
        if (!(viewParent instanceof AdapterView) || (position = (view = (AdapterView) viewParent).getPositionForView(this)) == -1) {
            return false;
        }
        long itemIdAtPosition = view.getItemIdAtPosition(position);
        boolean isHandled = false;
        try {
            Method performLongPress = AbsListView.class.getDeclaredMethod("performLongPress", View.class, Integer.TYPE, Long.TYPE);
            performLongPress.setAccessible(true);
            Object isHandledTemp = performLongPress.invoke(view, this, Integer.valueOf(position), Long.valueOf(itemIdAtPosition));
            if (isHandledTemp instanceof Boolean) {
                return ((Boolean) isHandledTemp).booleanValue();
            }
            return false;
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
            Log.e("SwipwLayout", "performAdapterViewItemLongClick Exception");
            if (view.getOnItemLongClickListener() != null) {
                isHandled = view.getOnItemLongClickListener().onItemLongClick(view, this, position, itemIdAtPosition);
            }
            if (!isHandled) {
                return isHandled;
            }
            view.performHapticFeedback(0);
            return isHandled;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (insideAdapterView()) {
            setOnLongClickListener(new CustOnLongClickListener());
        }
        if (this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(getContext(), new SwipeDetector());
        }
    }

    private static class CustOnLongClickListener implements View.OnLongClickListener {
        private CustOnLongClickListener() {
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTouchOnSurface(MotionEvent event) {
        View surfaceView = getSurfaceView();
        if (surfaceView == null) {
            return false;
        }
        if (this.mHitSurfaceRect == null) {
            this.mHitSurfaceRect = new Rect();
        }
        surfaceView.getHitRect(this.mHitSurfaceRect);
        return this.mHitSurfaceRect.contains((int) event.getX(), (int) event.getY());
    }

    class SwipeDetector extends GestureDetector.SimpleOnGestureListener {
        SwipeDetector() {
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onSingleTapUp(MotionEvent event) {
            if (SwipeLayout.this.mIsClickToClose && SwipeLayout.this.isTouchOnSurface(event)) {
                SwipeLayout.this.close();
            }
            if (SwipeLayout.this.insideAdapterView()) {
                SwipeLayout.this.performAdapterViewItemClick();
            }
            return super.onSingleTapUp(event);
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public void onLongPress(MotionEvent event) {
            if (SwipeLayout.this.insideAdapterView()) {
                SwipeLayout.this.performAdapterViewItemLongClick();
            }
            super.onLongPress(event);
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnDoubleTapListener
        public boolean onDoubleTap(MotionEvent event) {
            View target;
            if (SwipeLayout.this.mDoubleClickListener != null) {
                View bottom = SwipeLayout.this.getCurrentBottomView();
                View surface = SwipeLayout.this.getSurfaceView();
                boolean z = false;
                boolean isLeftAndRight = event.getX() > ((float) bottom.getLeft()) && event.getX() < ((float) bottom.getRight());
                boolean isTopAndButtom = event.getY() > ((float) bottom.getTop()) && event.getY() < ((float) bottom.getBottom());
                if (!isLeftAndRight || !isTopAndButtom) {
                    target = surface;
                } else {
                    target = bottom;
                }
                DoubleClickListener doubleClickListener = SwipeLayout.this.mDoubleClickListener;
                SwipeLayout swipeLayout = SwipeLayout.this;
                if (target == surface) {
                    z = true;
                }
                doubleClickListener.onDoubleClick(swipeLayout, z);
            }
            return true;
        }
    }

    public void setDragDistance(int max) {
        if (max < 0) {
            this.mDragDistance = dp2px(0.0f);
        } else {
            this.mDragDistance = dp2px((float) max);
        }
        requestLayout();
    }

    public void setShowMode(ShowMode mode) {
        this.mShowMode = mode;
        requestLayout();
    }

    public DragEdge getDragEdge() {
        return this.mCurrentDragEdge;
    }

    public int getDragDistance() {
        return this.mDragDistance;
    }

    public ShowMode getShowMode() {
        return this.mShowMode;
    }

    public View getSurfaceView() {
        if (getChildCount() == 0) {
            return null;
        }
        return getChildAt(getChildCount() - 1);
    }

    public View getCurrentBottomView() {
        List<View> bottoms = getBottomViews();
        if (this.mCurrentDragEdge.getId() < bottoms.size()) {
            return bottoms.get(this.mCurrentDragEdge.getId());
        }
        return null;
    }

    public List<View> getBottomViews() {
        ArrayList<View> bottoms = new ArrayList<>((int) DEFAULT_LIST_SIZE);
        for (DragEdge dragEdge : DragEdge.values()) {
            bottoms.add(this.mDragEdges.get(dragEdge));
        }
        return bottoms;
    }

    public Status getOpenStatus() {
        View surfaceView = getSurfaceView();
        if (surfaceView == null) {
            return Status.Close;
        }
        int surfaceLeft = surfaceView.getLeft();
        int surfaceTop = surfaceView.getTop();
        if (surfaceLeft == getPaddingLeft() && surfaceTop == getPaddingTop()) {
            return Status.Close;
        }
        if (surfaceLeft == getPaddingLeft() - this.mDragDistance || surfaceLeft == getPaddingLeft() + this.mDragDistance || surfaceTop == getPaddingTop() - this.mDragDistance || surfaceTop == getPaddingTop() + this.mDragDistance) {
            return Status.Open;
        }
        return Status.Middle;
    }

    /* access modifiers changed from: protected */
    public void processHandRelease(float velocityX, float velocityY, boolean isCloseBeforeDragged) {
        float minVelocity = this.mDragHelper.getMinVelocity();
        View surfaceView = getSurfaceView();
        DragEdge currentDragEdge = this.mCurrentDragEdge;
        if (currentDragEdge != null && surfaceView != null) {
            float willOpenPercent = isCloseBeforeDragged ? this.mWillOpenPercentAfterClose : this.mWillOpenPercentAfterOpen;
            if (currentDragEdge == DragEdge.Left) {
                dragEdgeLeft(velocityX, minVelocity, willOpenPercent);
            } else if (currentDragEdge == DragEdge.Right) {
                dragEdgeRight(velocityX, minVelocity, willOpenPercent);
            } else if (currentDragEdge == DragEdge.Top) {
                if (velocityY > minVelocity) {
                    open();
                } else if (velocityY < (-minVelocity)) {
                    close();
                } else if (getOpenPercentTop() > willOpenPercent) {
                    open();
                } else {
                    close();
                }
            } else if (currentDragEdge != DragEdge.Bottom) {
            } else {
                if (velocityY > minVelocity) {
                    close();
                } else if (velocityY < (-minVelocity)) {
                    open();
                } else if ((-getOpenPercentTop()) > willOpenPercent) {
                    open();
                } else {
                    close();
                }
            }
        }
    }

    private float getOpenPercentTop() {
        if (this.mDragDistance != 0) {
            return (((float) getSurfaceView().getTop()) * 1.0f) / ((float) this.mDragDistance);
        }
        return 0.0f;
    }

    private float getOpenPercentLeft() {
        if (this.mDragDistance != 0) {
            return (((float) getSurfaceView().getLeft()) * 1.0f) / ((float) this.mDragDistance);
        }
        return 0.0f;
    }

    private void dragEdgeRight(float velocityX, float minVelocity, float willOpenPercent) {
        if (velocityX > minVelocity) {
            close();
        } else if (velocityX < (-minVelocity)) {
            open();
        } else if ((-getOpenPercentLeft()) > willOpenPercent) {
            open();
        } else {
            close();
        }
    }

    private void dragEdgeLeft(float velocityX, float minVelocity, float willOpenPercent) {
        if (velocityX > minVelocity) {
            open();
            if (this.mIsDragThenClose) {
                close();
                for (SwipeListener listener : this.mSwipeListeners) {
                    listener.onDragThenClose(this, velocityX);
                }
            }
        } else if (velocityX < (-minVelocity)) {
            close();
        } else if (getOpenPercentLeft() <= willOpenPercent) {
            close();
        } else {
            open();
            if (this.mIsDragThenClose) {
                close();
                for (SwipeListener listener2 : this.mSwipeListeners) {
                    listener2.onDragThenClose(this, velocityX);
                }
            }
        }
    }

    public void open() {
        open(true, true);
    }

    public void open(boolean isSmooth) {
        open(isSmooth, true);
    }

    public void open(boolean isSmooth, boolean isNotify) {
        View surface = getSurfaceView();
        View bottom = getCurrentBottomView();
        if (surface != null) {
            Rect rect = computeSurfaceLayoutArea(true);
            if (isSmooth) {
                this.mDragHelper.smoothSlideViewTo(surface, rect.left, rect.top);
            } else {
                int dx = rect.left - surface.getLeft();
                int dy = rect.top - surface.getTop();
                surface.layout(rect.left, rect.top, rect.right, rect.bottom);
                if (getShowMode() == ShowMode.PullOut) {
                    Rect rect1 = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, rect);
                    if (bottom != null) {
                        bottom.layout(rect1.left, rect1.top, rect1.right, rect1.bottom);
                    }
                }
                if (isNotify) {
                    dispatchRevealEvent(rect.left, rect.top, rect.right, rect.bottom);
                    dispatchSwipeEvent(rect.left, rect.top, dx, dy);
                } else {
                    safeBottomView();
                }
            }
            invalidate();
            captureChildrenBound();
        }
    }

    public void open(DragEdge edge) {
        setCurrentDragEdge(edge);
        open(true, true);
    }

    public void open(boolean isSmooth, DragEdge edge) {
        setCurrentDragEdge(edge);
        open(isSmooth, true);
    }

    public void open(boolean isSmooth, boolean isNotify, DragEdge edge) {
        setCurrentDragEdge(edge);
        open(isSmooth, isNotify);
    }

    public void close() {
        close(true, true);
    }

    public void close(boolean isSmooth) {
        close(isSmooth, true);
    }

    public void close(boolean isSmooth, boolean isNotify) {
        View surface = getSurfaceView();
        if (surface != null) {
            if (isSmooth) {
                this.mDragHelper.smoothSlideViewTo(getSurfaceView(), getPaddingLeft(), getPaddingTop());
            } else {
                Rect rect = computeSurfaceLayoutArea(false);
                int dx = rect.left - surface.getLeft();
                int dy = rect.top - surface.getTop();
                surface.layout(rect.left, rect.top, rect.right, rect.bottom);
                if (this.mShowMode == ShowMode.PullOut) {
                    Rect rect1 = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, rect);
                    View bottomView = getCurrentBottomView();
                    if (bottomView != null) {
                        bottomView.layout(rect1.left, rect1.top, rect1.right, rect1.bottom);
                    }
                }
                if (isNotify) {
                    dispatchRevealEvent(rect.left, rect.top, rect.right, rect.bottom);
                    dispatchSwipeEvent(rect.left, rect.top, dx, dy);
                } else {
                    safeBottomView();
                }
            }
            invalidate();
            captureChildrenBound();
        }
    }

    public void toggle() {
        toggle(true);
    }

    public void toggle(boolean isSmooth) {
        if (getOpenStatus() == Status.Open) {
            close(isSmooth);
        } else if (getOpenStatus() == Status.Close) {
            open(isSmooth);
        }
    }

    private Rect computeSurfaceLayoutArea(boolean isOpen) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        if (isOpen) {
            if (this.mCurrentDragEdge == DragEdge.Left) {
                left = getPaddingLeft() + this.mDragDistance;
            } else if (this.mCurrentDragEdge == DragEdge.Right) {
                left = getPaddingLeft() - this.mDragDistance;
            } else {
                top = this.mCurrentDragEdge == DragEdge.Top ? getPaddingTop() + this.mDragDistance : getPaddingTop() - this.mDragDistance;
            }
        }
        return new Rect(left, top, getMeasuredWidth() + left, getMeasuredHeight() + top);
    }

    private Rect computeBottomLayoutAreaViaSurface(ShowMode mode, Rect surfaceArea) {
        View bottomView = getCurrentBottomView();
        int left = surfaceArea.left;
        int top = surfaceArea.top;
        int right = surfaceArea.right;
        int bottom = surfaceArea.bottom;
        if (mode == ShowMode.PullOut) {
            if (this.mCurrentDragEdge == DragEdge.Left) {
                left = surfaceArea.left - this.mDragDistance;
            } else if (this.mCurrentDragEdge == DragEdge.Right) {
                left = surfaceArea.right;
            } else {
                top = this.mCurrentDragEdge == DragEdge.Top ? surfaceArea.top - this.mDragDistance : surfaceArea.bottom;
            }
            int i = 0;
            if (this.mCurrentDragEdge == DragEdge.Left || this.mCurrentDragEdge == DragEdge.Right) {
                bottom = surfaceArea.bottom;
                if (bottomView != null) {
                    i = bottomView.getMeasuredWidth();
                }
                right = left + i;
            } else {
                if (bottomView != null) {
                    i = bottomView.getMeasuredHeight();
                }
                bottom = top + i;
                right = surfaceArea.right;
            }
        } else if (mode == ShowMode.LayDown) {
            if (this.mCurrentDragEdge == DragEdge.Left) {
                right = left + this.mDragDistance;
            } else if (this.mCurrentDragEdge == DragEdge.Right) {
                left = right - this.mDragDistance;
            } else if (this.mCurrentDragEdge == DragEdge.Top) {
                bottom = top + this.mDragDistance;
            } else {
                top = bottom - this.mDragDistance;
            }
        }
        return new Rect(left, top, right, bottom);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Rect computeBottomLayDown(DragEdge dragEdge) {
        int bottom;
        int right;
        int left = getPaddingLeft();
        int top = getPaddingTop();
        if (dragEdge == DragEdge.Right) {
            left = getMeasuredWidth() - this.mDragDistance;
        } else if (dragEdge == DragEdge.Bottom) {
            top = getMeasuredHeight() - this.mDragDistance;
        }
        if (dragEdge == DragEdge.Left || dragEdge == DragEdge.Right) {
            right = this.mDragDistance + left;
            bottom = getMeasuredHeight() + top;
        } else {
            right = getMeasuredWidth() + left;
            bottom = this.mDragDistance + top;
        }
        return new Rect(left, top, right, bottom);
    }

    public void setOnDoubleClickListener(DoubleClickListener doubleClickListener) {
        this.mDoubleClickListener = doubleClickListener;
    }

    private int dp2px(float dp) {
        return (int) ((getContext().getResources().getDisplayMetrics().density * dp) + FLOAT_ZORO_POINT_FIVE);
    }

    @Deprecated
    public void setDragEdge(DragEdge dragEdge) {
        clearDragEdge();
        if (getChildCount() >= 2) {
            this.mDragEdges.put(dragEdge, getChildAt(getChildCount() - 2));
        }
        setCurrentDragEdge(dragEdge);
    }

    /* access modifiers changed from: protected */
    public void onChildViewRemoved(View child) {
        for (Map.Entry<DragEdge, View> entry : new HashMap(this.mDragEdges).entrySet()) {
            if (entry.getValue() == child) {
                this.mDragEdges.remove(entry.getKey());
            }
        }
    }

    public Map<DragEdge, View> getDragEdgeMap() {
        return this.mDragEdges;
    }

    @Deprecated
    public List<DragEdge> getDragEdges() {
        return new ArrayList(this.mDragEdges.keySet());
    }

    @Deprecated
    public void setDragEdges(List<DragEdge> dragEdges) {
        clearDragEdge();
        int min = dragEdges.size() <= getChildCount() + -1 ? dragEdges.size() : getChildCount() - 1;
        for (int i = 0; i < min; i++) {
            this.mDragEdges.put(dragEdges.get(i), getChildAt(i));
        }
        if (dragEdges.size() == 0 || dragEdges.contains(DEFAULT_DRAG_EDGE)) {
            setCurrentDragEdge(DEFAULT_DRAG_EDGE);
        } else {
            setCurrentDragEdge(dragEdges.get(0));
        }
    }

    @Deprecated
    public void setDragEdges(DragEdge... dragEdges) {
        clearDragEdge();
        setDragEdges(Arrays.asList(dragEdges));
    }

    @Deprecated
    public void setBottomViewIds(int leftId, int rightId, int topId, int bottomId) {
        addDrag(DragEdge.Left, findViewById(leftId));
        addDrag(DragEdge.Right, findViewById(rightId));
        addDrag(DragEdge.Top, findViewById(topId));
        addDrag(DragEdge.Bottom, findViewById(bottomId));
    }

    private float getCurrentOffset() {
        DragEdge dragEdge = this.mCurrentDragEdge;
        if (dragEdge == null) {
            return 0.0f;
        }
        return this.mEdgeSwipesOffset[dragEdge.getId()];
    }

    private void setCurrentDragEdge(DragEdge dragEdge) {
        this.mCurrentDragEdge = dragEdge;
        updateBottomViews();
    }

    private void updateBottomViews() {
        View currentBottomView = getCurrentBottomView();
        if (currentBottomView != null) {
            if (this.mCurrentDragEdge == DragEdge.Left || this.mCurrentDragEdge == DragEdge.Right) {
                this.mDragDistance = currentBottomView.getMeasuredWidth() - dp2px(getCurrentOffset());
            } else {
                this.mDragDistance = currentBottomView.getMeasuredHeight() - dp2px(getCurrentOffset());
            }
        }
        if (this.mShowMode == ShowMode.PullOut) {
            layoutPullOut();
        } else if (this.mShowMode == ShowMode.LayDown) {
            layoutLayDown();
        }
        safeBottomView();
    }
}
