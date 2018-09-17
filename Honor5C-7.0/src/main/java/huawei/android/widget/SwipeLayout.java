package huawei.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import androidhwext.R;
import huawei.android.widget.SwipeItemMangerImpl.SwipeMemory;
import huawei.android.widget.ViewDragHelper.Callback;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SwipeLayout extends FrameLayout {
    private static final /* synthetic */ int[] -huawei-android-widget-SwipeLayout$DragEdgeSwitchesValues = null;
    private static final int DRAG_BOTTOM = 8;
    private static final int DRAG_LEFT = 1;
    private static final int DRAG_RIGHT = 2;
    private static final int DRAG_TOP = 4;
    private static final DragEdge DefaultDragEdge = null;
    public static final int EMPTY_LAYOUT = 0;
    private static final String TAG = "SwipeLayout";
    private GestureDetector gestureDetector;
    private Rect hitSurfaceRect;
    private boolean mClickToClose;
    private DragEdge mCurrentDragEdge;
    private DoubleClickListener mDoubleClickListener;
    private int mDragDistance;
    private LinkedHashMap<DragEdge, View> mDragEdges;
    private ViewDragHelper mDragHelper;
    private Callback mDragHelperCallback;
    private boolean mDragThenClose;
    private float[] mEdgeSwipesOffset;
    private int mEventCounter;
    private boolean mIsBeingDragged;
    private List<OnLayout> mOnLayoutListeners;
    private Map<View, ArrayList<OnRevealListener>> mRevealListeners;
    private Map<View, Boolean> mShowEntirely;
    private ShowMode mShowMode;
    private List<SwipeDenier> mSwipeDeniers;
    private boolean mSwipeEnabled;
    private List<SwipeListener> mSwipeListeners;
    private boolean[] mSwipesEnabled;
    private int mTouchSlop;
    private Map<View, Rect> mViewBoundCache;
    private float mWillOpenPercentAfterClose;
    private float mWillOpenPercentAfterOpen;
    private float sX;
    private float sY;

    public interface SwipeListener {
        void onClose(SwipeLayout swipeLayout);

        void onDragThenClose(SwipeLayout swipeLayout, float f);

        void onHandRelease(SwipeLayout swipeLayout, float f, float f2);

        void onOpen(SwipeLayout swipeLayout);

        void onStartClose(SwipeLayout swipeLayout);

        void onStartOpen(SwipeLayout swipeLayout);

        void onUpdate(SwipeLayout swipeLayout, int i, int i2);
    }

    public interface OnLayout {
        void onLayout(SwipeLayout swipeLayout);
    }

    private static class CustOnLongClickListener implements OnLongClickListener {
        private CustOnLongClickListener() {
        }

        public boolean onLongClick(View v) {
            return true;
        }
    }

    public interface DoubleClickListener {
        void onDoubleClick(SwipeLayout swipeLayout, boolean z);
    }

    public enum DragEdge {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.widget.SwipeLayout.DragEdge.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.widget.SwipeLayout.DragEdge.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.widget.SwipeLayout.DragEdge.<clinit>():void");
        }
    }

    public interface OnRevealListener {
        void onReveal(View view, DragEdge dragEdge, float f, int i);
    }

    public enum ShowMode {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.widget.SwipeLayout.ShowMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.widget.SwipeLayout.ShowMode.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.widget.SwipeLayout.ShowMode.<clinit>():void");
        }
    }

    public enum Status {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.widget.SwipeLayout.Status.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.widget.SwipeLayout.Status.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.widget.SwipeLayout.Status.<clinit>():void");
        }
    }

    public interface SwipeDenier {
        boolean shouldDenySwipe(MotionEvent motionEvent);
    }

    class SwipeDetector extends SimpleOnGestureListener {
        final /* synthetic */ SwipeLayout this$0;

        SwipeDetector(SwipeLayout this$0) {
            this.this$0 = this$0;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            if (this.this$0.mClickToClose && this.this$0.isTouchOnSurface(e)) {
                this.this$0.close();
            }
            if (this.this$0.insideAdapterView()) {
                this.this$0.performAdapterViewItemClick();
            }
            return super.onSingleTapUp(e);
        }

        public void onLongPress(MotionEvent e) {
            if (this.this$0.insideAdapterView()) {
                this.this$0.performAdapterViewItemLongClick();
            }
            super.onLongPress(e);
        }

        public boolean onDoubleTap(MotionEvent e) {
            if (this.this$0.mDoubleClickListener != null) {
                View target;
                View bottom = this.this$0.getCurrentBottomView();
                View surface = this.this$0.getSurfaceView();
                if (bottom == null || e.getX() <= ((float) bottom.getLeft()) || e.getX() >= ((float) bottom.getRight()) || e.getY() <= ((float) bottom.getTop()) || e.getY() >= ((float) bottom.getBottom())) {
                    target = surface;
                } else {
                    target = bottom;
                }
                this.this$0.mDoubleClickListener.onDoubleClick(this.this$0, target == surface);
            }
            return true;
        }
    }

    private static /* synthetic */ int[] -gethuawei-android-widget-SwipeLayout$DragEdgeSwitchesValues() {
        if (-huawei-android-widget-SwipeLayout$DragEdgeSwitchesValues != null) {
            return -huawei-android-widget-SwipeLayout$DragEdgeSwitchesValues;
        }
        int[] iArr = new int[DragEdge.values().length];
        try {
            iArr[DragEdge.Bottom.ordinal()] = DRAG_LEFT;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DragEdge.Left.ordinal()] = DRAG_RIGHT;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DragEdge.Right.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DragEdge.Top.ordinal()] = DRAG_TOP;
        } catch (NoSuchFieldError e4) {
        }
        -huawei-android-widget-SwipeLayout$DragEdgeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.widget.SwipeLayout.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.widget.SwipeLayout.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.widget.SwipeLayout.<clinit>():void");
    }

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, EMPTY_LAYOUT);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCurrentDragEdge = DefaultDragEdge;
        this.mDragDistance = EMPTY_LAYOUT;
        this.mDragEdges = new LinkedHashMap();
        this.mEdgeSwipesOffset = new float[DRAG_TOP];
        this.mSwipeListeners = new ArrayList();
        this.mSwipeDeniers = new ArrayList();
        this.mRevealListeners = new HashMap();
        this.mShowEntirely = new HashMap();
        this.mViewBoundCache = new HashMap();
        this.mSwipeEnabled = true;
        this.mSwipesEnabled = new boolean[]{true, true, true, true};
        this.mClickToClose = false;
        this.mDragThenClose = false;
        this.mWillOpenPercentAfterOpen = 0.75f;
        this.mWillOpenPercentAfterClose = 0.25f;
        this.mDragHelperCallback = new Callback() {
            private static final /* synthetic */ int[] -huawei-android-widget-SwipeLayout$DragEdgeSwitchesValues = null;
            final /* synthetic */ int[] $SWITCH_TABLE$huawei$android$widget$SwipeLayout$DragEdge;
            boolean isCloseBeforeDrag;

            private static /* synthetic */ int[] -gethuawei-android-widget-SwipeLayout$DragEdgeSwitchesValues() {
                if (-huawei-android-widget-SwipeLayout$DragEdgeSwitchesValues != null) {
                    return -huawei-android-widget-SwipeLayout$DragEdgeSwitchesValues;
                }
                int[] iArr = new int[DragEdge.values().length];
                try {
                    iArr[DragEdge.Bottom.ordinal()] = SwipeLayout.DRAG_LEFT;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[DragEdge.Left.ordinal()] = SwipeLayout.DRAG_RIGHT;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[DragEdge.Right.ordinal()] = 3;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[DragEdge.Top.ordinal()] = SwipeLayout.DRAG_TOP;
                } catch (NoSuchFieldError e4) {
                }
                -huawei-android-widget-SwipeLayout$DragEdgeSwitchesValues = iArr;
                return iArr;
            }

            {
                this.isCloseBeforeDrag = true;
            }

            public int clampViewPositionHorizontal(View child, int left, int dx) {
                if (child != SwipeLayout.this.getSurfaceView()) {
                    if (SwipeLayout.this.getCurrentBottomView() == child) {
                        switch (AnonymousClass1.-gethuawei-android-widget-SwipeLayout$DragEdgeSwitchesValues()[SwipeLayout.this.mCurrentDragEdge.ordinal()]) {
                            case SwipeLayout.DRAG_LEFT /*1*/:
                            case SwipeLayout.DRAG_TOP /*4*/:
                                return SwipeLayout.this.getPaddingLeft();
                            case SwipeLayout.DRAG_RIGHT /*2*/:
                                if (SwipeLayout.this.mShowMode == ShowMode.PullOut && left > SwipeLayout.this.getPaddingLeft()) {
                                    return SwipeLayout.this.getPaddingLeft();
                                }
                            case ViewDragHelper.DIRECTION_ALL /*3*/:
                                if (SwipeLayout.this.mShowMode == ShowMode.PullOut && left < SwipeLayout.this.getMeasuredWidth() - SwipeLayout.this.mDragDistance) {
                                    return SwipeLayout.this.getMeasuredWidth() - SwipeLayout.this.mDragDistance;
                                }
                            default:
                                break;
                        }
                    }
                }
                switch (AnonymousClass1.-gethuawei-android-widget-SwipeLayout$DragEdgeSwitchesValues()[SwipeLayout.this.mCurrentDragEdge.ordinal()]) {
                    case SwipeLayout.DRAG_LEFT /*1*/:
                    case SwipeLayout.DRAG_TOP /*4*/:
                        return SwipeLayout.this.getPaddingLeft();
                    case SwipeLayout.DRAG_RIGHT /*2*/:
                        if (left < SwipeLayout.this.getPaddingLeft()) {
                            return SwipeLayout.this.getPaddingLeft();
                        }
                        if (left > SwipeLayout.this.getPaddingLeft() + SwipeLayout.this.mDragDistance) {
                            return SwipeLayout.this.getPaddingLeft() + SwipeLayout.this.mDragDistance;
                        }
                        break;
                    case ViewDragHelper.DIRECTION_ALL /*3*/:
                        if (left > SwipeLayout.this.getPaddingLeft()) {
                            return SwipeLayout.this.getPaddingLeft();
                        }
                        if (left < SwipeLayout.this.getPaddingLeft() - SwipeLayout.this.mDragDistance) {
                            return SwipeLayout.this.getPaddingLeft() - SwipeLayout.this.mDragDistance;
                        }
                        break;
                }
                return left;
            }

            public int clampViewPositionVertical(View child, int top, int dy) {
                if (child != SwipeLayout.this.getSurfaceView()) {
                    View surfaceView = SwipeLayout.this.getSurfaceView();
                    int surfaceViewTop = surfaceView == null ? SwipeLayout.EMPTY_LAYOUT : surfaceView.getTop();
                    switch (AnonymousClass1.-gethuawei-android-widget-SwipeLayout$DragEdgeSwitchesValues()[SwipeLayout.this.mCurrentDragEdge.ordinal()]) {
                        case SwipeLayout.DRAG_LEFT /*1*/:
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
                            break;
                        case SwipeLayout.DRAG_RIGHT /*2*/:
                        case ViewDragHelper.DIRECTION_ALL /*3*/:
                            return SwipeLayout.this.getPaddingTop();
                        case SwipeLayout.DRAG_TOP /*4*/:
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
                            break;
                        default:
                            break;
                    }
                }
                switch (AnonymousClass1.-gethuawei-android-widget-SwipeLayout$DragEdgeSwitchesValues()[SwipeLayout.this.mCurrentDragEdge.ordinal()]) {
                    case SwipeLayout.DRAG_LEFT /*1*/:
                        if (top < SwipeLayout.this.getPaddingTop() - SwipeLayout.this.mDragDistance) {
                            return SwipeLayout.this.getPaddingTop() - SwipeLayout.this.mDragDistance;
                        }
                        if (top > SwipeLayout.this.getPaddingTop()) {
                            return SwipeLayout.this.getPaddingTop();
                        }
                        break;
                    case SwipeLayout.DRAG_RIGHT /*2*/:
                    case ViewDragHelper.DIRECTION_ALL /*3*/:
                        return SwipeLayout.this.getPaddingTop();
                    case SwipeLayout.DRAG_TOP /*4*/:
                        if (top < SwipeLayout.this.getPaddingTop()) {
                            return SwipeLayout.this.getPaddingTop();
                        }
                        if (top > SwipeLayout.this.getPaddingTop() + SwipeLayout.this.mDragDistance) {
                            return SwipeLayout.this.getPaddingTop() + SwipeLayout.this.mDragDistance;
                        }
                        break;
                }
                return top;
            }

            public boolean tryCaptureView(View child, int pointerId) {
                boolean result;
                boolean z = true;
                if (child != SwipeLayout.this.getSurfaceView()) {
                    result = SwipeLayout.this.getBottomViews().contains(child);
                } else {
                    result = true;
                }
                if (result) {
                    if (SwipeLayout.this.getOpenStatus() != Status.Close) {
                        z = false;
                    }
                    this.isCloseBeforeDrag = z;
                }
                return result;
            }

            public int getViewHorizontalDragRange(View child) {
                return SwipeLayout.this.mDragDistance;
            }

            public int getViewVerticalDragRange(View child) {
                return SwipeLayout.this.mDragDistance;
            }

            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                SwipeLayout.this.processHandRelease(xvel, yvel, this.isCloseBeforeDrag);
                for (SwipeListener l : SwipeLayout.this.mSwipeListeners) {
                    l.onHandRelease(SwipeLayout.this, xvel, yvel);
                }
                SwipeLayout.this.invalidate();
            }

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
                            Rect rect = SwipeLayout.this.computeBottomLayDown(SwipeLayout.this.mCurrentDragEdge);
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
        this.mEventCounter = EMPTY_LAYOUT;
        this.sX = -1.0f;
        this.sY = -1.0f;
        this.gestureDetector = new GestureDetector(getContext(), new SwipeDetector(this));
        this.mDragHelper = ViewDragHelper.create(this, this.mDragHelperCallback);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout);
        int dragEdgeChoices = a.getInt(EMPTY_LAYOUT, EMPTY_LAYOUT);
        this.mEdgeSwipesOffset[DragEdge.Left.ordinal()] = a.getDimension(DRAG_LEFT, 0.0f);
        this.mEdgeSwipesOffset[DragEdge.Right.ordinal()] = a.getDimension(DRAG_RIGHT, 0.0f);
        this.mEdgeSwipesOffset[DragEdge.Top.ordinal()] = a.getDimension(3, 0.0f);
        this.mEdgeSwipesOffset[DragEdge.Bottom.ordinal()] = a.getDimension(DRAG_TOP, 0.0f);
        setClickToClose(a.getBoolean(6, this.mClickToClose));
        setDragThenClose(a.getBoolean(7, this.mDragThenClose));
        if ((dragEdgeChoices & DRAG_LEFT) == DRAG_LEFT) {
            this.mDragEdges.put(DragEdge.Left, null);
        }
        if ((dragEdgeChoices & DRAG_RIGHT) == DRAG_RIGHT) {
            this.mDragEdges.put(DragEdge.Right, null);
        }
        if ((dragEdgeChoices & DRAG_TOP) == DRAG_TOP) {
            this.mDragEdges.put(DragEdge.Top, null);
        }
        if ((dragEdgeChoices & DRAG_BOTTOM) == DRAG_BOTTOM) {
            this.mDragEdges.put(DragEdge.Bottom, null);
        }
        this.mShowMode = ShowMode.values()[a.getInt(5, ShowMode.PullOut.ordinal())];
        a.recycle();
    }

    public void addSwipeListener(SwipeListener l) {
        this.mSwipeListeners.add(l);
    }

    public void removeSwipeListener(SwipeListener l) {
        this.mSwipeListeners.remove(l);
    }

    public void removeAllSwipeListener() {
        this.mSwipeListeners.clear();
    }

    public boolean hasUserSetSwipeListener() {
        if (this.mSwipeListeners.isEmpty()) {
            return false;
        }
        boolean hasUserListener = false;
        for (SwipeListener l : this.mSwipeListeners) {
            if (!(l instanceof SwipeMemory)) {
                hasUserListener = true;
            }
        }
        return hasUserListener;
    }

    public void addSwipeDenier(SwipeDenier denier) {
        this.mSwipeDeniers.add(denier);
    }

    public void removeSwipeDenier(SwipeDenier denier) {
        this.mSwipeDeniers.remove(denier);
    }

    public void removeAllSwipeDeniers() {
        this.mSwipeDeniers.clear();
    }

    public void addRevealListener(int childId, OnRevealListener l) {
        View child = findViewById(childId);
        if (child == null) {
            throw new IllegalArgumentException("Child does not belong to SwipeListener.");
        }
        if (!this.mShowEntirely.containsKey(child)) {
            this.mShowEntirely.put(child, Boolean.valueOf(false));
        }
        if (this.mRevealListeners.get(child) == null) {
            this.mRevealListeners.put(child, new ArrayList());
        }
        ((ArrayList) this.mRevealListeners.get(child)).add(l);
    }

    public void addRevealListener(int[] childIds, OnRevealListener l) {
        int length = childIds.length;
        for (int i = EMPTY_LAYOUT; i < length; i += DRAG_LEFT) {
            addRevealListener(childIds[i], l);
        }
    }

    public void removeRevealListener(int childId, OnRevealListener l) {
        View child = findViewById(childId);
        if (child != null) {
            this.mShowEntirely.remove(child);
            if (this.mRevealListeners.containsKey(child)) {
                ((ArrayList) this.mRevealListeners.get(child)).remove(l);
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

    private void captureChildrenBound() {
        int i = EMPTY_LAYOUT;
        View currentSurfaceView = getSurfaceView();
        View currentBottomView = getCurrentBottomView();
        if (getOpenStatus() == Status.Close) {
            this.mViewBoundCache.remove(currentSurfaceView);
            this.mViewBoundCache.remove(currentBottomView);
            return;
        }
        View[] views = new View[DRAG_RIGHT];
        views[EMPTY_LAYOUT] = currentSurfaceView;
        views[DRAG_LEFT] = currentBottomView;
        int length = views.length;
        while (i < length) {
            View child = views[i];
            Rect rect = (Rect) this.mViewBoundCache.get(child);
            if (rect == null) {
                rect = new Rect();
                this.mViewBoundCache.put(child, rect);
            }
            rect.left = child.getLeft();
            rect.top = child.getTop();
            rect.right = child.getRight();
            rect.bottom = child.getBottom();
            i += DRAG_LEFT;
        }
    }

    protected boolean isViewTotallyFirstShowed(View child, Rect relativePosition, DragEdge edge, int surfaceLeft, int surfaceTop, int surfaceRight, int surfaceBottom) {
        if (((Boolean) this.mShowEntirely.get(child)).booleanValue()) {
            return false;
        }
        int childLeft = relativePosition.left;
        int childRight = relativePosition.right;
        int childTop = relativePosition.top;
        int childBottom = relativePosition.bottom;
        boolean r = false;
        if (getShowMode() == ShowMode.LayDown) {
            if ((edge != DragEdge.Right || surfaceRight > childLeft) && ((edge != DragEdge.Left || surfaceLeft < childRight) && (edge != DragEdge.Top || surfaceTop < childBottom))) {
                if (edge == DragEdge.Bottom && surfaceBottom <= childTop) {
                }
            }
            r = true;
        } else if (getShowMode() == ShowMode.PullOut) {
            if ((edge != DragEdge.Right || childRight > getWidth()) && ((edge != DragEdge.Left || childLeft < getPaddingLeft()) && (edge != DragEdge.Top || childTop < getPaddingTop()))) {
                if (edge == DragEdge.Bottom && childBottom <= getHeight()) {
                }
            }
            r = true;
        }
        return r;
    }

    protected boolean isViewShowing(View child, Rect relativePosition, DragEdge availableEdge, int surfaceLeft, int surfaceTop, int surfaceRight, int surfaceBottom) {
        int childLeft = relativePosition.left;
        int childRight = relativePosition.right;
        int childTop = relativePosition.top;
        int childBottom = relativePosition.bottom;
        if (getShowMode() != ShowMode.LayDown) {
            if (getShowMode() == ShowMode.PullOut) {
                switch (-gethuawei-android-widget-SwipeLayout$DragEdgeSwitchesValues()[availableEdge.ordinal()]) {
                    case DRAG_LEFT /*1*/:
                        if (childTop < getHeight() && childTop >= getPaddingTop()) {
                            return true;
                        }
                    case DRAG_RIGHT /*2*/:
                        if (childRight >= getPaddingLeft() && childLeft < getPaddingLeft()) {
                            return true;
                        }
                    case ViewDragHelper.DIRECTION_ALL /*3*/:
                        if (childLeft <= getWidth() && childRight > getWidth()) {
                            return true;
                        }
                    case DRAG_TOP /*4*/:
                        if (childTop < getPaddingTop() && childBottom >= getPaddingTop()) {
                            return true;
                        }
                    default:
                        break;
                }
            }
        }
        switch (-gethuawei-android-widget-SwipeLayout$DragEdgeSwitchesValues()[availableEdge.ordinal()]) {
            case DRAG_LEFT /*1*/:
                if (surfaceBottom > childTop && surfaceBottom <= childBottom) {
                    return true;
                }
            case DRAG_RIGHT /*2*/:
                if (surfaceLeft < childRight && surfaceLeft >= childLeft) {
                    return true;
                }
            case ViewDragHelper.DIRECTION_ALL /*3*/:
                if (surfaceRight > childLeft && surfaceRight <= childRight) {
                    return true;
                }
            case DRAG_TOP /*4*/:
                if (surfaceTop >= childTop && surfaceTop < childBottom) {
                    return true;
                }
        }
        return false;
    }

    protected Rect getRelativePosition(View child) {
        View t = child;
        Rect r = new Rect(child.getLeft(), child.getTop(), EMPTY_LAYOUT, EMPTY_LAYOUT);
        while (t.getParent() != null && t != getRootView()) {
            t = (View) t.getParent();
            if (t == this) {
                break;
            }
            r.left += t.getLeft();
            r.top += t.getTop();
        }
        r.right = r.left + child.getMeasuredWidth();
        r.bottom = r.top + child.getMeasuredHeight();
        return r;
    }

    protected void dispatchSwipeEvent(int surfaceLeft, int surfaceTop, int dx, int dy) {
        DragEdge edge = getDragEdge();
        boolean open = true;
        if (edge == DragEdge.Left) {
            if (dx < 0) {
                open = false;
            }
        } else if (edge == DragEdge.Right) {
            if (dx > 0) {
                open = false;
            }
        } else if (edge == DragEdge.Top) {
            if (dy < 0) {
                open = false;
            }
        } else if (edge == DragEdge.Bottom && dy > 0) {
            open = false;
        }
        dispatchSwipeEvent(surfaceLeft, surfaceTop, open);
    }

    protected void dispatchSwipeEvent(int surfaceLeft, int surfaceTop, boolean open) {
        safeBottomView();
        Status status = getOpenStatus();
        if (!this.mSwipeListeners.isEmpty()) {
            this.mEventCounter += DRAG_LEFT;
            for (SwipeListener l : this.mSwipeListeners) {
                if (this.mEventCounter == DRAG_LEFT) {
                    if (open) {
                        l.onStartOpen(this);
                    } else {
                        l.onStartClose(this);
                    }
                }
                l.onUpdate(this, surfaceLeft - getPaddingLeft(), surfaceTop - getPaddingTop());
            }
            if (status == Status.Close) {
                for (SwipeListener l2 : this.mSwipeListeners) {
                    l2.onClose(this);
                }
                this.mEventCounter = EMPTY_LAYOUT;
            }
            if (status == Status.Open) {
                View currentBottomView = getCurrentBottomView();
                if (currentBottomView != null) {
                    currentBottomView.setEnabled(true);
                }
                for (SwipeListener l22 : this.mSwipeListeners) {
                    l22.onOpen(this);
                }
                this.mEventCounter = EMPTY_LAYOUT;
            }
        }
    }

    private void safeBottomView() {
        Status status = getOpenStatus();
        List<View> bottoms = getBottomViews();
        if (status == Status.Close) {
            for (View bottom : bottoms) {
                if (!(bottom == null || bottom.getVisibility() == DRAG_TOP)) {
                    bottom.setVisibility(DRAG_TOP);
                }
            }
            return;
        }
        View currentBottomView = getCurrentBottomView();
        if (currentBottomView != null && currentBottomView.getVisibility() != 0) {
            currentBottomView.setVisibility(EMPTY_LAYOUT);
        }
    }

    protected void dispatchRevealEvent(int surfaceLeft, int surfaceTop, int surfaceRight, int surfaceBottom) {
        if (!this.mRevealListeners.isEmpty()) {
            for (Entry<View, ArrayList<OnRevealListener>> entry : this.mRevealListeners.entrySet()) {
                View child = (View) entry.getKey();
                Rect rect = getRelativePosition(child);
                if (isViewShowing(child, rect, this.mCurrentDragEdge, surfaceLeft, surfaceTop, surfaceRight, surfaceBottom)) {
                    this.mShowEntirely.put(child, Boolean.valueOf(false));
                    int distance = EMPTY_LAYOUT;
                    float fraction = 0.0f;
                    if (getShowMode() != ShowMode.LayDown) {
                        if (getShowMode() == ShowMode.PullOut) {
                            switch (-gethuawei-android-widget-SwipeLayout$DragEdgeSwitchesValues()[this.mCurrentDragEdge.ordinal()]) {
                                case DRAG_LEFT /*1*/:
                                    distance = rect.top - getHeight();
                                    fraction = ((float) distance) / ((float) child.getHeight());
                                    break;
                                case DRAG_RIGHT /*2*/:
                                    distance = rect.right - getPaddingLeft();
                                    fraction = ((float) distance) / ((float) child.getWidth());
                                    break;
                                case ViewDragHelper.DIRECTION_ALL /*3*/:
                                    distance = rect.left - getWidth();
                                    fraction = ((float) distance) / ((float) child.getWidth());
                                    break;
                                case DRAG_TOP /*4*/:
                                    distance = rect.bottom - getPaddingTop();
                                    fraction = ((float) distance) / ((float) child.getHeight());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    switch (-gethuawei-android-widget-SwipeLayout$DragEdgeSwitchesValues()[this.mCurrentDragEdge.ordinal()]) {
                        case DRAG_LEFT /*1*/:
                            distance = rect.bottom - surfaceBottom;
                            fraction = ((float) distance) / ((float) child.getHeight());
                            break;
                        case DRAG_RIGHT /*2*/:
                            distance = rect.left - surfaceLeft;
                            fraction = ((float) distance) / ((float) child.getWidth());
                            break;
                        case ViewDragHelper.DIRECTION_ALL /*3*/:
                            distance = rect.right - surfaceRight;
                            fraction = ((float) distance) / ((float) child.getWidth());
                            break;
                        case DRAG_TOP /*4*/:
                            distance = rect.top - surfaceTop;
                            fraction = ((float) distance) / ((float) child.getHeight());
                            break;
                    }
                    for (OnRevealListener l : (ArrayList) entry.getValue()) {
                        l.onReveal(child, this.mCurrentDragEdge, Math.abs(fraction), distance);
                        if (Math.abs(fraction) == 1.0f) {
                            this.mShowEntirely.put(child, Boolean.valueOf(true));
                        }
                    }
                }
                if (isViewTotallyFirstShowed(child, rect, this.mCurrentDragEdge, surfaceLeft, surfaceTop, surfaceRight, surfaceBottom)) {
                    this.mShowEntirely.put(child, Boolean.valueOf(true));
                    for (OnRevealListener l2 : (ArrayList) entry.getValue()) {
                        if (this.mCurrentDragEdge == DragEdge.Left || this.mCurrentDragEdge == DragEdge.Right) {
                            l2.onReveal(child, this.mCurrentDragEdge, 1.0f, child.getWidth());
                        } else {
                            l2.onReveal(child, this.mCurrentDragEdge, 1.0f, child.getHeight());
                        }
                    }
                }
            }
        }
    }

    public void computeScroll() {
        super.computeScroll();
        if (this.mDragHelper.continueSettling(true)) {
            postInvalidateOnAnimation();
        }
    }

    public void addOnLayoutListener(OnLayout l) {
        if (this.mOnLayoutListeners == null) {
            this.mOnLayoutListeners = new ArrayList();
        }
        this.mOnLayoutListeners.add(l);
    }

    public void removeOnLayoutListener(OnLayout l) {
        if (this.mOnLayoutListeners != null) {
            this.mOnLayoutListeners.remove(l);
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

    public void addDrag(DragEdge dragEdge, View child, LayoutParams params) {
        if (child != null) {
            if (params == null) {
                params = generateDefaultLayoutParams();
            }
            if (!checkLayoutParams(params)) {
                params = generateLayoutParams(params);
            }
            int gravity = -1;
            switch (-gethuawei-android-widget-SwipeLayout$DragEdgeSwitchesValues()[dragEdge.ordinal()]) {
                case DRAG_LEFT /*1*/:
                    gravity = 80;
                    break;
                case DRAG_RIGHT /*2*/:
                    gravity = 3;
                    break;
                case ViewDragHelper.DIRECTION_ALL /*3*/:
                    gravity = 5;
                    break;
                case DRAG_TOP /*4*/:
                    gravity = 48;
                    break;
            }
            if (params instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) params).gravity = gravity;
            }
            addView(child, EMPTY_LAYOUT, params);
        }
    }

    public void addView(View child, int index, LayoutParams params) {
        if (child != null) {
            int gravity = EMPTY_LAYOUT;
            try {
                gravity = ((Integer) params.getClass().getField("gravity").get(params)).intValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (gravity <= 0) {
                for (Entry<DragEdge, View> entry : this.mDragEdges.entrySet()) {
                    if (entry.getValue() == null) {
                        this.mDragEdges.put((DragEdge) entry.getKey(), child);
                        break;
                    }
                }
            }
            gravity = Gravity.getAbsoluteGravity(gravity, getLayoutDirection());
            if ((gravity & 3) == 3) {
                this.mDragEdges.put(DragEdge.Left, child);
            }
            if ((gravity & 5) == 5) {
                this.mDragEdges.put(DragEdge.Right, child);
            }
            if ((gravity & 48) == 48) {
                this.mDragEdges.put(DragEdge.Top, child);
            }
            if ((gravity & 80) == 80) {
                this.mDragEdges.put(DragEdge.Bottom, child);
            }
            if (child.getParent() != this) {
                super.addView(child, index, params);
            }
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (Status.Middle != getOpenStatus()) {
            updateBottomViews();
            if (this.mOnLayoutListeners != null) {
                for (int i = EMPTY_LAYOUT; i < this.mOnLayoutListeners.size(); i += DRAG_LEFT) {
                    ((OnLayout) this.mOnLayoutListeners.get(i)).onLayout(this);
                }
            }
        }
    }

    void layoutPullOut() {
        View surfaceView = getSurfaceView();
        Rect surfaceRect = (Rect) this.mViewBoundCache.get(surfaceView);
        if (surfaceRect == null) {
            surfaceRect = computeSurfaceLayoutArea(false);
        }
        if (surfaceView != null) {
            surfaceView.layout(surfaceRect.left, surfaceRect.top, surfaceRect.right, surfaceRect.bottom);
            bringChildToFront(surfaceView);
        }
        View currentBottomView = getCurrentBottomView();
        Rect bottomViewRect = (Rect) this.mViewBoundCache.get(currentBottomView);
        if (bottomViewRect == null) {
            bottomViewRect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, surfaceRect);
        }
        if (currentBottomView != null) {
            currentBottomView.layout(bottomViewRect.left, bottomViewRect.top, bottomViewRect.right, bottomViewRect.bottom);
        }
    }

    void layoutLayDown() {
        View surfaceView = getSurfaceView();
        Rect surfaceRect = (Rect) this.mViewBoundCache.get(surfaceView);
        if (surfaceRect == null) {
            surfaceRect = computeSurfaceLayoutArea(false);
        }
        if (surfaceView != null) {
            surfaceView.layout(surfaceRect.left, surfaceRect.top, surfaceRect.right, surfaceRect.bottom);
            bringChildToFront(surfaceView);
        }
        View currentBottomView = getCurrentBottomView();
        Rect bottomViewRect = (Rect) this.mViewBoundCache.get(currentBottomView);
        if (bottomViewRect == null) {
            bottomViewRect = computeBottomLayoutAreaViaSurface(ShowMode.LayDown, surfaceRect);
        }
        if (currentBottomView != null) {
            currentBottomView.layout(bottomViewRect.left, bottomViewRect.top, bottomViewRect.right, bottomViewRect.bottom);
        }
    }

    private void checkCanDrag(MotionEvent ev) {
        if (!this.mIsBeingDragged) {
            if (getOpenStatus() == Status.Middle) {
                this.mIsBeingDragged = true;
                return;
            }
            boolean suitable;
            boolean z;
            Status status = getOpenStatus();
            float distanceX = ev.getRawX() - this.sX;
            float distanceY = ev.getRawY() - this.sY;
            float angle = (float) Math.toDegrees(Math.atan((double) Math.abs(distanceY / distanceX)));
            if (getOpenStatus() == Status.Close) {
                DragEdge dragEdge;
                if (angle < 45.0f) {
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
            boolean doNothing = false;
            if (this.mCurrentDragEdge == DragEdge.Right) {
                suitable = (status != Status.Open || distanceX <= ((float) this.mTouchSlop)) ? status == Status.Close && distanceX < ((float) (-this.mTouchSlop)) : true;
                suitable = suitable || status == Status.Middle;
                if (angle > 30.0f || !suitable) {
                    doNothing = true;
                }
            }
            if (this.mCurrentDragEdge == DragEdge.Left) {
                suitable = (status != Status.Open || distanceX >= ((float) (-this.mTouchSlop))) ? status == Status.Close && distanceX > ((float) this.mTouchSlop) : true;
                suitable = suitable || status == Status.Middle;
                if (angle > 30.0f || !suitable) {
                    doNothing = true;
                }
            }
            if (this.mCurrentDragEdge == DragEdge.Top) {
                suitable = (status != Status.Open || distanceY >= ((float) (-this.mTouchSlop))) ? status == Status.Close && distanceY > ((float) this.mTouchSlop) : true;
                suitable = suitable || status == Status.Middle;
                if (angle < 60.0f || !suitable) {
                    doNothing = true;
                }
            }
            if (this.mCurrentDragEdge == DragEdge.Bottom) {
                suitable = (status != Status.Open || distanceY <= ((float) this.mTouchSlop)) ? status == Status.Close && distanceY < ((float) (-this.mTouchSlop)) : true;
                suitable = suitable || status == Status.Middle;
                if (angle < 60.0f || !suitable) {
                    doNothing = true;
                }
            }
            if (doNothing) {
                z = false;
            } else {
                z = true;
            }
            this.mIsBeingDragged = z;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isSwipeEnabled()) {
            return false;
        }
        if (this.mClickToClose && getOpenStatus() == Status.Open && isTouchOnSurface(ev)) {
            return true;
        }
        for (SwipeDenier denier : this.mSwipeDeniers) {
            if (denier != null && denier.shouldDenySwipe(ev)) {
                return false;
            }
        }
        switch (ev.getAction()) {
            case EMPTY_LAYOUT /*0*/:
                this.mDragHelper.processTouchEvent(ev);
                this.mIsBeingDragged = false;
                this.sX = ev.getRawX();
                this.sY = ev.getRawY();
                if (getOpenStatus() == Status.Middle) {
                    this.mIsBeingDragged = true;
                    break;
                }
                break;
            case DRAG_LEFT /*1*/:
            case ViewDragHelper.DIRECTION_ALL /*3*/:
                this.mIsBeingDragged = false;
                this.mDragHelper.processTouchEvent(ev);
                break;
            case DRAG_RIGHT /*2*/:
                boolean beforeCheck = this.mIsBeingDragged;
                checkCanDrag(ev);
                if (this.mIsBeingDragged) {
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (!beforeCheck && this.mIsBeingDragged) {
                    return false;
                }
            default:
                this.mDragHelper.processTouchEvent(ev);
                break;
        }
        return this.mIsBeingDragged;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean z = true;
        if (!isSwipeEnabled()) {
            return super.onTouchEvent(event);
        }
        int action = event.getActionMasked();
        this.gestureDetector.onTouchEvent(event);
        switch (action) {
            case EMPTY_LAYOUT /*0*/:
                this.mDragHelper.processTouchEvent(event);
                this.sX = event.getRawX();
                this.sY = event.getRawY();
                break;
            case DRAG_LEFT /*1*/:
            case ViewDragHelper.DIRECTION_ALL /*3*/:
                this.mIsBeingDragged = false;
                this.mDragHelper.processTouchEvent(event);
                break;
            case DRAG_RIGHT /*2*/:
                checkCanDrag(event);
                if (this.mIsBeingDragged) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    this.mDragHelper.processTouchEvent(event);
                    break;
                }
                break;
            default:
                this.mDragHelper.processTouchEvent(event);
                break;
        }
        if (!(super.onTouchEvent(event) || this.mIsBeingDragged || action == 0)) {
            z = false;
        }
        return z;
    }

    public boolean isClickToClose() {
        return this.mClickToClose;
    }

    public void setClickToClose(boolean clickToClose) {
        this.mClickToClose = clickToClose;
    }

    public void setDragThenClose(boolean dragThenClose) {
        this.mDragThenClose = dragThenClose;
    }

    public void setSwipeEnabled(boolean enabled) {
        this.mSwipeEnabled = enabled;
    }

    public boolean isSwipeEnabled() {
        return this.mSwipeEnabled;
    }

    public boolean isLeftSwipeEnabled() {
        View bottomView = (View) this.mDragEdges.get(DragEdge.Left);
        if (bottomView == null || bottomView.getParent() != this || bottomView == getSurfaceView()) {
            return false;
        }
        return this.mSwipesEnabled[DragEdge.Left.ordinal()];
    }

    public void setLeftSwipeEnabled(boolean leftSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Left.ordinal()] = leftSwipeEnabled;
    }

    public boolean isRightSwipeEnabled() {
        View bottomView = (View) this.mDragEdges.get(DragEdge.Right);
        if (bottomView == null || bottomView.getParent() != this || bottomView == getSurfaceView()) {
            return false;
        }
        return this.mSwipesEnabled[DragEdge.Right.ordinal()];
    }

    public void setRightSwipeEnabled(boolean rightSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Right.ordinal()] = rightSwipeEnabled;
    }

    public boolean isTopSwipeEnabled() {
        View bottomView = (View) this.mDragEdges.get(DragEdge.Top);
        if (bottomView == null || bottomView.getParent() != this || bottomView == getSurfaceView()) {
            return false;
        }
        return this.mSwipesEnabled[DragEdge.Top.ordinal()];
    }

    public void setTopSwipeEnabled(boolean topSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Top.ordinal()] = topSwipeEnabled;
    }

    public boolean isBottomSwipeEnabled() {
        View bottomView = (View) this.mDragEdges.get(DragEdge.Bottom);
        if (bottomView == null || bottomView.getParent() != this || bottomView == getSurfaceView()) {
            return false;
        }
        return this.mSwipesEnabled[DragEdge.Bottom.ordinal()];
    }

    public void setBottomSwipeEnabled(boolean bottomSwipeEnabled) {
        this.mSwipesEnabled[DragEdge.Bottom.ordinal()] = bottomSwipeEnabled;
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

    private boolean insideAdapterView() {
        return getAdapterView() != null;
    }

    private AdapterView getAdapterView() {
        ViewParent t = getParent();
        if (t instanceof AdapterView) {
            return (AdapterView) t;
        }
        return null;
    }

    private void performAdapterViewItemClick() {
        if (getOpenStatus() == Status.Close) {
            ViewParent t = getParent();
            if (t instanceof AdapterView) {
                AdapterView view = (AdapterView) t;
                int p = view.getPositionForView(this);
                if (p != -1) {
                    view.performItemClick(view.getChildAt(p - view.getFirstVisiblePosition()), p, view.getAdapter().getItemId(p));
                }
            }
        }
    }

    private boolean performAdapterViewItemLongClick() {
        if (getOpenStatus() != Status.Close) {
            return false;
        }
        ViewParent t = getParent();
        if (!(t instanceof AdapterView)) {
            return false;
        }
        AdapterView view = (AdapterView) t;
        int p = view.getPositionForView(this);
        if (p == -1) {
            return false;
        }
        long vId = view.getItemIdAtPosition(p);
        boolean handled = false;
        try {
            Method m = AbsListView.class.getDeclaredMethod("performLongPress", new Class[]{View.class, Integer.TYPE, Long.TYPE});
            m.setAccessible(true);
            handled = ((Boolean) m.invoke(view, new Object[]{this, Integer.valueOf(p), Long.valueOf(vId)})).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            if (view.getOnItemLongClickListener() != null) {
                handled = view.getOnItemLongClickListener().onItemLongClick(view, this, p, vId);
            }
            if (handled) {
                view.performHapticFeedback(EMPTY_LAYOUT);
            }
        }
        return handled;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (insideAdapterView()) {
            setOnLongClickListener(new CustOnLongClickListener());
        }
    }

    private boolean isTouchOnSurface(MotionEvent ev) {
        View surfaceView = getSurfaceView();
        if (surfaceView == null) {
            return false;
        }
        if (this.hitSurfaceRect == null) {
            this.hitSurfaceRect = new Rect();
        }
        surfaceView.getHitRect(this.hitSurfaceRect);
        return this.hitSurfaceRect.contains((int) ev.getX(), (int) ev.getY());
    }

    public void setDragDistance(int max) {
        if (max < 0) {
            max = EMPTY_LAYOUT;
        }
        this.mDragDistance = dp2px((float) max);
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
        if (this.mCurrentDragEdge.ordinal() < bottoms.size()) {
            return (View) bottoms.get(this.mCurrentDragEdge.ordinal());
        }
        return null;
    }

    public List<View> getBottomViews() {
        ArrayList<View> bottoms = new ArrayList();
        DragEdge[] values = DragEdge.values();
        int length = values.length;
        for (int i = EMPTY_LAYOUT; i < length; i += DRAG_LEFT) {
            bottoms.add((View) this.mDragEdges.get(values[i]));
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

    protected void processHandRelease(float xvel, float yvel, boolean isCloseBeforeDragged) {
        float minVelocity = this.mDragHelper.getMinVelocity();
        View surfaceView = getSurfaceView();
        DragEdge currentDragEdge = this.mCurrentDragEdge;
        if (currentDragEdge != null && surfaceView != null) {
            float willOpenPercent = isCloseBeforeDragged ? this.mWillOpenPercentAfterClose : this.mWillOpenPercentAfterOpen;
            if (currentDragEdge == DragEdge.Left) {
                if (xvel > minVelocity) {
                    open();
                    if (this.mDragThenClose) {
                        close();
                        for (SwipeListener l : this.mSwipeListeners) {
                            l.onDragThenClose(this, xvel);
                        }
                    }
                } else if (xvel < (-minVelocity)) {
                    close();
                } else if ((((float) getSurfaceView().getLeft()) * 1.0f) / ((float) this.mDragDistance) > willOpenPercent) {
                    open();
                    if (this.mDragThenClose) {
                        close();
                        for (SwipeListener l2 : this.mSwipeListeners) {
                            l2.onDragThenClose(this, xvel);
                        }
                    }
                } else {
                    close();
                }
            } else if (currentDragEdge == DragEdge.Right) {
                if (xvel > minVelocity) {
                    close();
                } else if (xvel < (-minVelocity)) {
                    open();
                } else if ((((float) (-getSurfaceView().getLeft())) * 1.0f) / ((float) this.mDragDistance) > willOpenPercent) {
                    open();
                } else {
                    close();
                }
            } else if (currentDragEdge == DragEdge.Top) {
                if (yvel > minVelocity) {
                    open();
                } else if (yvel < (-minVelocity)) {
                    close();
                } else if ((((float) getSurfaceView().getTop()) * 1.0f) / ((float) this.mDragDistance) > willOpenPercent) {
                    open();
                } else {
                    close();
                }
            } else if (currentDragEdge == DragEdge.Bottom) {
                if (yvel > minVelocity) {
                    close();
                } else if (yvel < (-minVelocity)) {
                    open();
                } else if ((((float) (-getSurfaceView().getTop())) * 1.0f) / ((float) this.mDragDistance) > willOpenPercent) {
                    open();
                } else {
                    close();
                }
            }
        }
    }

    public void open() {
        open(true, true);
    }

    public void open(boolean smooth) {
        open(smooth, true);
    }

    public void open(boolean smooth, boolean notify) {
        View surface = getSurfaceView();
        View bottom = getCurrentBottomView();
        if (surface != null) {
            Rect rect = computeSurfaceLayoutArea(true);
            if (smooth) {
                this.mDragHelper.smoothSlideViewTo(surface, rect.left, rect.top);
            } else {
                int dx = rect.left - surface.getLeft();
                int dy = rect.top - surface.getTop();
                surface.layout(rect.left, rect.top, rect.right, rect.bottom);
                if (getShowMode() == ShowMode.PullOut) {
                    Rect bRect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, rect);
                    if (bottom != null) {
                        bottom.layout(bRect.left, bRect.top, bRect.right, bRect.bottom);
                    }
                }
                if (notify) {
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

    public void open(boolean smooth, DragEdge edge) {
        setCurrentDragEdge(edge);
        open(smooth, true);
    }

    public void open(boolean smooth, boolean notify, DragEdge edge) {
        setCurrentDragEdge(edge);
        open(smooth, notify);
    }

    public void close() {
        close(true, true);
    }

    public void close(boolean smooth) {
        close(smooth, true);
    }

    public void close(boolean smooth, boolean notify) {
        View surface = getSurfaceView();
        if (surface != null) {
            if (smooth) {
                this.mDragHelper.smoothSlideViewTo(getSurfaceView(), getPaddingLeft(), getPaddingTop());
            } else {
                Rect rect = computeSurfaceLayoutArea(false);
                int dx = rect.left - surface.getLeft();
                int dy = rect.top - surface.getTop();
                surface.layout(rect.left, rect.top, rect.right, rect.bottom);
                if (this.mShowMode == ShowMode.PullOut) {
                    Rect bRect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, rect);
                    View bottomView = getCurrentBottomView();
                    if (bottomView != null) {
                        bottomView.layout(bRect.left, bRect.top, bRect.right, bRect.bottom);
                    }
                }
                if (notify) {
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

    public void toggle(boolean smooth) {
        if (getOpenStatus() == Status.Open) {
            close(smooth);
        } else if (getOpenStatus() == Status.Close) {
            open(smooth);
        }
    }

    private Rect computeSurfaceLayoutArea(boolean open) {
        int l = getPaddingLeft();
        int t = getPaddingTop();
        if (open) {
            if (this.mCurrentDragEdge == DragEdge.Left) {
                l = getPaddingLeft() + this.mDragDistance;
            } else if (this.mCurrentDragEdge == DragEdge.Right) {
                l = getPaddingLeft() - this.mDragDistance;
            } else {
                t = this.mCurrentDragEdge == DragEdge.Top ? getPaddingTop() + this.mDragDistance : getPaddingTop() - this.mDragDistance;
            }
        }
        return new Rect(l, t, getMeasuredWidth() + l, getMeasuredHeight() + t);
    }

    private Rect computeBottomLayoutAreaViaSurface(ShowMode mode, Rect surfaceArea) {
        int i = EMPTY_LAYOUT;
        Rect rect = surfaceArea;
        View bottomView = getCurrentBottomView();
        int bl = surfaceArea.left;
        int bt = surfaceArea.top;
        int br = surfaceArea.right;
        int bb = surfaceArea.bottom;
        if (mode == ShowMode.PullOut) {
            if (this.mCurrentDragEdge == DragEdge.Left) {
                bl = surfaceArea.left - this.mDragDistance;
            } else if (this.mCurrentDragEdge == DragEdge.Right) {
                bl = surfaceArea.right;
            } else {
                bt = this.mCurrentDragEdge == DragEdge.Top ? surfaceArea.top - this.mDragDistance : surfaceArea.bottom;
            }
            if (this.mCurrentDragEdge == DragEdge.Left || this.mCurrentDragEdge == DragEdge.Right) {
                bb = surfaceArea.bottom;
                if (bottomView != null) {
                    i = bottomView.getMeasuredWidth();
                }
                br = bl + i;
            } else {
                if (bottomView != null) {
                    i = bottomView.getMeasuredHeight();
                }
                bb = bt + i;
                br = surfaceArea.right;
            }
        } else if (mode == ShowMode.LayDown) {
            if (this.mCurrentDragEdge == DragEdge.Left) {
                br = bl + this.mDragDistance;
            } else if (this.mCurrentDragEdge == DragEdge.Right) {
                bl = br - this.mDragDistance;
            } else if (this.mCurrentDragEdge == DragEdge.Top) {
                bb = bt + this.mDragDistance;
            } else {
                bt = bb - this.mDragDistance;
            }
        }
        return new Rect(bl, bt, br, bb);
    }

    private Rect computeBottomLayDown(DragEdge dragEdge) {
        int br;
        int bb;
        int bl = getPaddingLeft();
        int bt = getPaddingTop();
        if (dragEdge == DragEdge.Right) {
            bl = getMeasuredWidth() - this.mDragDistance;
        } else if (dragEdge == DragEdge.Bottom) {
            bt = getMeasuredHeight() - this.mDragDistance;
        }
        if (dragEdge == DragEdge.Left || dragEdge == DragEdge.Right) {
            br = bl + this.mDragDistance;
            bb = bt + getMeasuredHeight();
        } else {
            br = bl + getMeasuredWidth();
            bb = bt + this.mDragDistance;
        }
        return new Rect(bl, bt, br, bb);
    }

    public void setOnDoubleClickListener(DoubleClickListener doubleClickListener) {
        this.mDoubleClickListener = doubleClickListener;
    }

    private int dp2px(float dp) {
        return (int) ((getContext().getResources().getDisplayMetrics().density * dp) + 0.5f);
    }

    @Deprecated
    public void setDragEdge(DragEdge dragEdge) {
        clearDragEdge();
        if (getChildCount() >= DRAG_RIGHT) {
            this.mDragEdges.put(dragEdge, getChildAt(getChildCount() - 2));
        }
        setCurrentDragEdge(dragEdge);
    }

    protected void onChildViewRemoved(View child) {
        for (Entry<DragEdge, View> entry : new HashMap(this.mDragEdges).entrySet()) {
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
        int size = Math.min(dragEdges.size(), getChildCount() - 1);
        for (int i = EMPTY_LAYOUT; i < size; i += DRAG_LEFT) {
            this.mDragEdges.put((DragEdge) dragEdges.get(i), getChildAt(i));
        }
        if (dragEdges.size() == 0 || dragEdges.contains(DefaultDragEdge)) {
            setCurrentDragEdge(DefaultDragEdge);
        } else {
            setCurrentDragEdge((DragEdge) dragEdges.get(EMPTY_LAYOUT));
        }
    }

    @Deprecated
    public void setDragEdges(DragEdge... mDragEdges) {
        clearDragEdge();
        setDragEdges(Arrays.asList(mDragEdges));
    }

    @Deprecated
    public void setBottomViewIds(int leftId, int rightId, int topId, int bottomId) {
        addDrag(DragEdge.Left, findViewById(leftId));
        addDrag(DragEdge.Right, findViewById(rightId));
        addDrag(DragEdge.Top, findViewById(topId));
        addDrag(DragEdge.Bottom, findViewById(bottomId));
    }

    private float getCurrentOffset() {
        if (this.mCurrentDragEdge == null) {
            return 0.0f;
        }
        return this.mEdgeSwipesOffset[this.mCurrentDragEdge.ordinal()];
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
