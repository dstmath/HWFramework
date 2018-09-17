package android.view;

import android.animation.LayoutTransition;
import android.animation.LayoutTransition.TransitionListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pools.SynchronizedPool;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionMode.Callback;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.FlagToString;
import android.view.ViewDebug.IntToString;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LayoutAnimationController.AnimationParameters;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Predicate;
import com.hisi.perfhub.PerfHub;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.microedition.khronos.opengles.GL11;

public abstract class ViewGroup extends View implements ViewParent, ViewManager {
    private static final int ARRAY_CAPACITY_INCREMENT = 12;
    private static final int ARRAY_INITIAL_CAPACITY = 12;
    private static final int CHILD_LEFT_INDEX = 0;
    private static final int CHILD_TOP_INDEX = 1;
    protected static final int CLIP_TO_PADDING_MASK = 34;
    private static final boolean DBG = false;
    public static boolean DEBUG_DRAW = false;
    private static final int[] DESCENDANT_FOCUSABILITY_FLAGS = null;
    private static final int FLAG_ADD_STATES_FROM_CHILDREN = 8192;
    private static final int FLAG_ALWAYS_DRAWN_WITH_CACHE = 16384;
    private static final int FLAG_ANIMATION_CACHE = 64;
    static final int FLAG_ANIMATION_DONE = 16;
    private static final int FLAG_CHILDREN_DRAWN_WITH_CACHE = 32768;
    static final int FLAG_CLEAR_TRANSFORMATION = 256;
    static final int FLAG_CLIP_CHILDREN = 1;
    private static final int FLAG_CLIP_TO_PADDING = 2;
    protected static final int FLAG_DISALLOW_INTERCEPT = 524288;
    static final int FLAG_INVALIDATE_REQUIRED = 4;
    static final int FLAG_IS_TRANSITION_GROUP = 16777216;
    static final int FLAG_IS_TRANSITION_GROUP_SET = 33554432;
    private static final int FLAG_LAYOUT_MODE_WAS_EXPLICITLY_SET = 8388608;
    private static final int FLAG_MASK_FOCUSABILITY = 393216;
    private static final int FLAG_NOTIFY_ANIMATION_LISTENER = 512;
    private static final int FLAG_NOTIFY_CHILDREN_ON_DRAWABLE_STATE_CHANGE = 65536;
    static final int FLAG_OPTIMIZE_INVALIDATE = 128;
    private static final int FLAG_PADDING_NOT_NULL = 32;
    private static final int FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW = 4194304;
    private static final int FLAG_RUN_ANIMATION = 8;
    private static final int FLAG_SHOW_CONTEXT_MENU_WITH_COORDS = 536870912;
    private static final int FLAG_SPLIT_MOTION_EVENTS = 2097152;
    private static final int FLAG_START_ACTION_MODE_FOR_CHILD_IS_NOT_TYPED = 268435456;
    private static final int FLAG_START_ACTION_MODE_FOR_CHILD_IS_TYPED = 134217728;
    protected static final int FLAG_SUPPORT_STATIC_TRANSFORMATIONS = 2048;
    static final int FLAG_TOUCHSCREEN_BLOCKS_FOCUS = 67108864;
    protected static final int FLAG_USE_CHILD_DRAWING_ORDER = 1024;
    public static final int FOCUS_AFTER_DESCENDANTS = 262144;
    public static final int FOCUS_BEFORE_DESCENDANTS = 131072;
    public static final int FOCUS_BLOCK_DESCENDANTS = 393216;
    public static final int LAYOUT_MODE_CLIP_BOUNDS = 0;
    public static int LAYOUT_MODE_DEFAULT = 0;
    public static final int LAYOUT_MODE_OPTICAL_BOUNDS = 1;
    private static final int LAYOUT_MODE_UNDEFINED = -1;
    public static final int PERSISTENT_ALL_CACHES = 3;
    public static final int PERSISTENT_ANIMATION_CACHE = 1;
    public static final int PERSISTENT_NO_CACHE = 0;
    public static final int PERSISTENT_SCROLLING_CACHE = 2;
    private static final ActionMode SENTINEL_ACTION_MODE = null;
    private static final String TAG = "ViewGroup";
    private static float[] sDebugLines;
    private static Paint sDebugPaint;
    private AnimationListener mAnimationListener;
    Paint mCachePaint;
    @ExportedProperty(category = "layout")
    private int mChildCountWithTransientState;
    private Transformation mChildTransformation;
    private View[] mChildren;
    private int mChildrenCount;
    private HashSet<View> mChildrenInterestedInDrag;
    private DragEvent mCurrentDragStartEvent;
    private View mCurrentDragView;
    protected ArrayList<View> mDisappearingChildren;
    private HoverTarget mFirstHoverTarget;
    private TouchTarget mFirstTouchTarget;
    private View mFocused;
    @ExportedProperty(flagMapping = {@FlagToString(equals = 1, mask = 1, name = "CLIP_CHILDREN"), @FlagToString(equals = 2, mask = 2, name = "CLIP_TO_PADDING"), @FlagToString(equals = 32, mask = 32, name = "PADDING_NOT_NULL")}, formatToHexString = true)
    protected int mGroupFlags;
    private boolean mHoveredSelf;
    RectF mInvalidateRegion;
    Transformation mInvalidationTransformation;
    private boolean mIsInterestedInDrag;
    @ExportedProperty(category = "events")
    private int mLastTouchDownIndex;
    @ExportedProperty(category = "events")
    private long mLastTouchDownTime;
    @ExportedProperty(category = "events")
    private float mLastTouchDownX;
    @ExportedProperty(category = "events")
    private float mLastTouchDownY;
    private LayoutAnimationController mLayoutAnimationController;
    private boolean mLayoutCalledWhileSuppressed;
    private int mLayoutMode;
    private TransitionListener mLayoutTransitionListener;
    private PointF mLocalPoint;
    private int mNestedScrollAxes;
    protected OnHierarchyChangeListener mOnHierarchyChangeListener;
    protected int mPersistentDrawingCache;
    private ArrayList<View> mPreSortedChildren;
    boolean mSuppressLayout;
    private float[] mTempPoint;
    private List<Integer> mTransientIndices;
    private List<View> mTransientViews;
    private LayoutTransition mTransition;
    private ArrayList<View> mTransitioningViews;
    private ArrayList<View> mVisibilityChangingChildren;

    static class ChildListForAccessibility {
        private static final int MAX_POOL_SIZE = 32;
        private static final SynchronizedPool<ChildListForAccessibility> sPool = null;
        private final ArrayList<View> mChildren;
        private final ArrayList<ViewLocationHolder> mHolders;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewGroup.ChildListForAccessibility.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.ViewGroup.ChildListForAccessibility.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewGroup.ChildListForAccessibility.<clinit>():void");
        }

        ChildListForAccessibility() {
            this.mChildren = new ArrayList();
            this.mHolders = new ArrayList();
        }

        public static ChildListForAccessibility obtain(ViewGroup parent, boolean sort) {
            ChildListForAccessibility list = (ChildListForAccessibility) sPool.acquire();
            if (list == null) {
                list = new ChildListForAccessibility();
            }
            list.init(parent, sort);
            return list;
        }

        public void recycle() {
            clear();
            sPool.release(this);
        }

        public int getChildCount() {
            return this.mChildren.size();
        }

        public View getChildAt(int index) {
            return (View) this.mChildren.get(index);
        }

        public int getChildIndex(View child) {
            return this.mChildren.indexOf(child);
        }

        private void init(ViewGroup parent, boolean sort) {
            int i;
            ArrayList<View> children = this.mChildren;
            int childCount = parent.getChildCount();
            for (i = ViewGroup.PERSISTENT_NO_CACHE; i < childCount; i += ViewGroup.PERSISTENT_ANIMATION_CACHE) {
                children.add(parent.getChildAt(i));
            }
            if (sort) {
                ArrayList<ViewLocationHolder> holders = this.mHolders;
                for (i = ViewGroup.PERSISTENT_NO_CACHE; i < childCount; i += ViewGroup.PERSISTENT_ANIMATION_CACHE) {
                    holders.add(ViewLocationHolder.obtain(parent, (View) children.get(i)));
                }
                sort(holders);
                for (i = ViewGroup.PERSISTENT_NO_CACHE; i < childCount; i += ViewGroup.PERSISTENT_ANIMATION_CACHE) {
                    ViewLocationHolder holder = (ViewLocationHolder) holders.get(i);
                    children.set(i, holder.mView);
                    holder.recycle();
                }
                holders.clear();
            }
        }

        private void sort(ArrayList<ViewLocationHolder> holders) {
            try {
                ViewLocationHolder.setComparisonStrategy(ViewGroup.PERSISTENT_ANIMATION_CACHE);
                Collections.sort(holders);
            } catch (IllegalArgumentException e) {
                ViewLocationHolder.setComparisonStrategy(ViewGroup.PERSISTENT_SCROLLING_CACHE);
                Collections.sort(holders);
            }
        }

        private void clear() {
            this.mChildren.clear();
        }
    }

    private static final class HoverTarget {
        private static final int MAX_RECYCLED = 32;
        private static HoverTarget sRecycleBin;
        private static final Object sRecycleLock = null;
        private static int sRecycledCount;
        public View child;
        public HoverTarget next;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewGroup.HoverTarget.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.ViewGroup.HoverTarget.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewGroup.HoverTarget.<clinit>():void");
        }

        private HoverTarget() {
        }

        public static HoverTarget obtain(View child) {
            if (child == null) {
                throw new IllegalArgumentException("child must be non-null");
            }
            HoverTarget target;
            synchronized (sRecycleLock) {
                if (sRecycleBin == null) {
                    target = new HoverTarget();
                } else {
                    target = sRecycleBin;
                    sRecycleBin = target.next;
                    sRecycledCount += ViewGroup.LAYOUT_MODE_UNDEFINED;
                    target.next = null;
                }
            }
            target.child = child;
            return target;
        }

        public void recycle() {
            if (this.child == null) {
                throw new IllegalStateException("already recycled once");
            }
            synchronized (sRecycleLock) {
                if (sRecycledCount < MAX_RECYCLED) {
                    this.next = sRecycleBin;
                    sRecycleBin = this;
                    sRecycledCount += ViewGroup.PERSISTENT_ANIMATION_CACHE;
                } else {
                    this.next = null;
                }
                this.child = null;
            }
        }
    }

    public static class LayoutParams {
        @Deprecated
        public static final int FILL_PARENT = -1;
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;
        @ExportedProperty(category = "layout", mapping = {@IntToString(from = -1, to = "MATCH_PARENT"), @IntToString(from = -2, to = "WRAP_CONTENT")})
        public int height;
        public AnimationParameters layoutAnimationParameters;
        @ExportedProperty(category = "layout", mapping = {@IntToString(from = -1, to = "MATCH_PARENT"), @IntToString(from = -2, to = "WRAP_CONTENT")})
        public int width;

        public LayoutParams(Context c, AttributeSet attrs) {
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ViewGroup_Layout);
            setBaseAttributes(a, ViewGroup.PERSISTENT_NO_CACHE, ViewGroup.PERSISTENT_ANIMATION_CACHE);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public LayoutParams(LayoutParams source) {
            this.width = source.width;
            this.height = source.height;
        }

        LayoutParams() {
        }

        protected void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
            this.width = a.getLayoutDimension(widthAttr, "layout_width");
            this.height = a.getLayoutDimension(heightAttr, "layout_height");
        }

        public void resolveLayoutDirection(int layoutDirection) {
        }

        public String debug(String output) {
            return output + "ViewGroup.LayoutParams={ width=" + sizeToString(this.width) + ", height=" + sizeToString(this.height) + " }";
        }

        public void onDebugDraw(View view, Canvas canvas, Paint paint) {
        }

        protected static String sizeToString(int size) {
            if (size == WRAP_CONTENT) {
                return "wrap-content";
            }
            if (size == MATCH_PARENT) {
                return "match-parent";
            }
            return String.valueOf(size);
        }

        void encode(ViewHierarchyEncoder encoder) {
            encoder.beginObject(this);
            encodeProperties(encoder);
            encoder.endObject();
        }

        protected void encodeProperties(ViewHierarchyEncoder encoder) {
            encoder.addProperty("width", this.width);
            encoder.addProperty("height", this.height);
        }
    }

    public static class MarginLayoutParams extends LayoutParams {
        public static final int DEFAULT_MARGIN_RELATIVE = Integer.MIN_VALUE;
        private static final int DEFAULT_MARGIN_RESOLVED = 0;
        private static final int LAYOUT_DIRECTION_MASK = 3;
        private static final int LEFT_MARGIN_UNDEFINED_MASK = 4;
        private static final int NEED_RESOLUTION_MASK = 32;
        private static final int RIGHT_MARGIN_UNDEFINED_MASK = 8;
        private static final int RTL_COMPATIBILITY_MODE_MASK = 16;
        private static final int UNDEFINED_MARGIN = Integer.MIN_VALUE;
        @ExportedProperty(category = "layout")
        public int bottomMargin;
        @ExportedProperty(category = "layout")
        private int endMargin;
        @ExportedProperty(category = "layout")
        public int leftMargin;
        @ExportedProperty(category = "layout", flagMapping = {@FlagToString(equals = 3, mask = 3, name = "LAYOUT_DIRECTION"), @FlagToString(equals = 4, mask = 4, name = "LEFT_MARGIN_UNDEFINED_MASK"), @FlagToString(equals = 8, mask = 8, name = "RIGHT_MARGIN_UNDEFINED_MASK"), @FlagToString(equals = 16, mask = 16, name = "RTL_COMPATIBILITY_MODE_MASK"), @FlagToString(equals = 32, mask = 32, name = "NEED_RESOLUTION_MASK")}, formatToHexString = true)
        byte mMarginFlags;
        @ExportedProperty(category = "layout")
        public int rightMargin;
        @ExportedProperty(category = "layout")
        private int startMargin;
        @ExportedProperty(category = "layout")
        public int topMargin;

        public MarginLayoutParams(Context c, AttributeSet attrs) {
            this.startMargin = UNDEFINED_MARGIN;
            this.endMargin = UNDEFINED_MARGIN;
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ViewGroup_MarginLayout);
            setBaseAttributes(a, DEFAULT_MARGIN_RESOLVED, ViewGroup.PERSISTENT_ANIMATION_CACHE);
            int margin = a.getDimensionPixelSize(ViewGroup.PERSISTENT_SCROLLING_CACHE, ViewGroup.LAYOUT_MODE_UNDEFINED);
            if (margin >= 0) {
                this.leftMargin = margin;
                this.topMargin = margin;
                this.rightMargin = margin;
                this.bottomMargin = margin;
            } else {
                this.leftMargin = a.getDimensionPixelSize(LAYOUT_DIRECTION_MASK, UNDEFINED_MARGIN);
                if (this.leftMargin == UNDEFINED_MARGIN) {
                    this.mMarginFlags = (byte) (this.mMarginFlags | LEFT_MARGIN_UNDEFINED_MASK);
                    this.leftMargin = DEFAULT_MARGIN_RESOLVED;
                }
                this.rightMargin = a.getDimensionPixelSize(5, UNDEFINED_MARGIN);
                if (this.rightMargin == UNDEFINED_MARGIN) {
                    this.mMarginFlags = (byte) (this.mMarginFlags | RIGHT_MARGIN_UNDEFINED_MASK);
                    this.rightMargin = DEFAULT_MARGIN_RESOLVED;
                }
                this.topMargin = a.getDimensionPixelSize(LEFT_MARGIN_UNDEFINED_MASK, DEFAULT_MARGIN_RESOLVED);
                this.bottomMargin = a.getDimensionPixelSize(6, DEFAULT_MARGIN_RESOLVED);
                this.startMargin = a.getDimensionPixelSize(7, UNDEFINED_MARGIN);
                this.endMargin = a.getDimensionPixelSize(RIGHT_MARGIN_UNDEFINED_MASK, UNDEFINED_MARGIN);
                if (isMarginRelative()) {
                    this.mMarginFlags = (byte) (this.mMarginFlags | NEED_RESOLUTION_MASK);
                }
            }
            boolean hasRtlSupport = c.getApplicationInfo().hasRtlSupport();
            if (c.getApplicationInfo().targetSdkVersion < 17 || !hasRtlSupport) {
                this.mMarginFlags = (byte) (this.mMarginFlags | RTL_COMPATIBILITY_MODE_MASK);
            }
            this.mMarginFlags = (byte) (this.mMarginFlags | DEFAULT_MARGIN_RESOLVED);
            a.recycle();
        }

        public MarginLayoutParams(int width, int height) {
            super(width, height);
            this.startMargin = UNDEFINED_MARGIN;
            this.endMargin = UNDEFINED_MARGIN;
            this.mMarginFlags = (byte) (this.mMarginFlags | LEFT_MARGIN_UNDEFINED_MASK);
            this.mMarginFlags = (byte) (this.mMarginFlags | RIGHT_MARGIN_UNDEFINED_MASK);
            this.mMarginFlags = (byte) (this.mMarginFlags & -33);
            this.mMarginFlags = (byte) (this.mMarginFlags & -17);
        }

        public MarginLayoutParams(MarginLayoutParams source) {
            this.startMargin = UNDEFINED_MARGIN;
            this.endMargin = UNDEFINED_MARGIN;
            this.width = source.width;
            this.height = source.height;
            this.leftMargin = source.leftMargin;
            this.topMargin = source.topMargin;
            this.rightMargin = source.rightMargin;
            this.bottomMargin = source.bottomMargin;
            this.startMargin = source.startMargin;
            this.endMargin = source.endMargin;
            this.mMarginFlags = source.mMarginFlags;
        }

        public MarginLayoutParams(LayoutParams source) {
            super(source);
            this.startMargin = UNDEFINED_MARGIN;
            this.endMargin = UNDEFINED_MARGIN;
            this.mMarginFlags = (byte) (this.mMarginFlags | LEFT_MARGIN_UNDEFINED_MASK);
            this.mMarginFlags = (byte) (this.mMarginFlags | RIGHT_MARGIN_UNDEFINED_MASK);
            this.mMarginFlags = (byte) (this.mMarginFlags & -33);
            this.mMarginFlags = (byte) (this.mMarginFlags & -17);
        }

        public final void copyMarginsFrom(MarginLayoutParams source) {
            this.leftMargin = source.leftMargin;
            this.topMargin = source.topMargin;
            this.rightMargin = source.rightMargin;
            this.bottomMargin = source.bottomMargin;
            this.startMargin = source.startMargin;
            this.endMargin = source.endMargin;
            this.mMarginFlags = source.mMarginFlags;
        }

        public void setMargins(int left, int top, int right, int bottom) {
            this.leftMargin = left;
            this.topMargin = top;
            this.rightMargin = right;
            this.bottomMargin = bottom;
            this.mMarginFlags = (byte) (this.mMarginFlags & -5);
            this.mMarginFlags = (byte) (this.mMarginFlags & -9);
            if (isMarginRelative()) {
                this.mMarginFlags = (byte) (this.mMarginFlags | NEED_RESOLUTION_MASK);
            } else {
                this.mMarginFlags = (byte) (this.mMarginFlags & -33);
            }
        }

        public void setMarginsRelative(int start, int top, int end, int bottom) {
            this.startMargin = start;
            this.topMargin = top;
            this.endMargin = end;
            this.bottomMargin = bottom;
            this.mMarginFlags = (byte) (this.mMarginFlags | NEED_RESOLUTION_MASK);
        }

        public void setMarginStart(int start) {
            this.startMargin = start;
            this.mMarginFlags = (byte) (this.mMarginFlags | NEED_RESOLUTION_MASK);
        }

        public int getMarginStart() {
            if (this.startMargin != UNDEFINED_MARGIN) {
                return this.startMargin;
            }
            if ((this.mMarginFlags & NEED_RESOLUTION_MASK) == NEED_RESOLUTION_MASK) {
                doResolveMargins();
            }
            switch (this.mMarginFlags & LAYOUT_DIRECTION_MASK) {
                case ViewGroup.PERSISTENT_ANIMATION_CACHE /*1*/:
                    return this.rightMargin;
                default:
                    return this.leftMargin;
            }
        }

        public void setMarginEnd(int end) {
            this.endMargin = end;
            this.mMarginFlags = (byte) (this.mMarginFlags | NEED_RESOLUTION_MASK);
        }

        public int getMarginEnd() {
            if (this.endMargin != UNDEFINED_MARGIN) {
                return this.endMargin;
            }
            if ((this.mMarginFlags & NEED_RESOLUTION_MASK) == NEED_RESOLUTION_MASK) {
                doResolveMargins();
            }
            switch (this.mMarginFlags & LAYOUT_DIRECTION_MASK) {
                case ViewGroup.PERSISTENT_ANIMATION_CACHE /*1*/:
                    return this.leftMargin;
                default:
                    return this.rightMargin;
            }
        }

        public boolean isMarginRelative() {
            return (this.startMargin == UNDEFINED_MARGIN && this.endMargin == UNDEFINED_MARGIN) ? ViewGroup.DBG : true;
        }

        public void setLayoutDirection(int layoutDirection) {
            if ((layoutDirection == 0 || layoutDirection == ViewGroup.PERSISTENT_ANIMATION_CACHE) && layoutDirection != (this.mMarginFlags & LAYOUT_DIRECTION_MASK)) {
                this.mMarginFlags = (byte) (this.mMarginFlags & -4);
                this.mMarginFlags = (byte) (this.mMarginFlags | (layoutDirection & LAYOUT_DIRECTION_MASK));
                if (isMarginRelative()) {
                    this.mMarginFlags = (byte) (this.mMarginFlags | NEED_RESOLUTION_MASK);
                } else {
                    this.mMarginFlags = (byte) (this.mMarginFlags & -33);
                }
            }
        }

        public int getLayoutDirection() {
            return this.mMarginFlags & LAYOUT_DIRECTION_MASK;
        }

        public void resolveLayoutDirection(int layoutDirection) {
            setLayoutDirection(layoutDirection);
            if (isMarginRelative() && (this.mMarginFlags & NEED_RESOLUTION_MASK) == NEED_RESOLUTION_MASK) {
                doResolveMargins();
            }
        }

        private void doResolveMargins() {
            int i = DEFAULT_MARGIN_RESOLVED;
            if ((this.mMarginFlags & RTL_COMPATIBILITY_MODE_MASK) != RTL_COMPATIBILITY_MODE_MASK) {
                int i2;
                switch (this.mMarginFlags & LAYOUT_DIRECTION_MASK) {
                    case ViewGroup.PERSISTENT_ANIMATION_CACHE /*1*/:
                        if (this.endMargin > UNDEFINED_MARGIN) {
                            i2 = this.endMargin;
                        } else {
                            i2 = DEFAULT_MARGIN_RESOLVED;
                        }
                        this.leftMargin = i2;
                        if (this.startMargin > UNDEFINED_MARGIN) {
                            i = this.startMargin;
                        }
                        this.rightMargin = i;
                        break;
                    default:
                        if (this.startMargin > UNDEFINED_MARGIN) {
                            i2 = this.startMargin;
                        } else {
                            i2 = DEFAULT_MARGIN_RESOLVED;
                        }
                        this.leftMargin = i2;
                        if (this.endMargin > UNDEFINED_MARGIN) {
                            i = this.endMargin;
                        }
                        this.rightMargin = i;
                        break;
                }
            }
            if ((this.mMarginFlags & LEFT_MARGIN_UNDEFINED_MASK) == LEFT_MARGIN_UNDEFINED_MASK && this.startMargin > UNDEFINED_MARGIN) {
                this.leftMargin = this.startMargin;
            }
            if ((this.mMarginFlags & RIGHT_MARGIN_UNDEFINED_MASK) == RIGHT_MARGIN_UNDEFINED_MASK && this.endMargin > UNDEFINED_MARGIN) {
                this.rightMargin = this.endMargin;
            }
            this.mMarginFlags = (byte) (this.mMarginFlags & -33);
        }

        public boolean isLayoutRtl() {
            return (this.mMarginFlags & LAYOUT_DIRECTION_MASK) == ViewGroup.PERSISTENT_ANIMATION_CACHE ? true : ViewGroup.DBG;
        }

        public void onDebugDraw(View view, Canvas canvas, Paint paint) {
            Insets oi = View.isLayoutModeOptical(view.mParent) ? view.getOpticalInsets() : Insets.NONE;
            ViewGroup.fillDifference(canvas, oi.left + view.getLeft(), oi.top + view.getTop(), view.getRight() - oi.right, view.getBottom() - oi.bottom, this.leftMargin, this.topMargin, this.rightMargin, this.bottomMargin, paint);
        }

        protected void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("leftMargin", this.leftMargin);
            encoder.addProperty("topMargin", this.topMargin);
            encoder.addProperty("rightMargin", this.rightMargin);
            encoder.addProperty("bottomMargin", this.bottomMargin);
            encoder.addProperty("startMargin", this.startMargin);
            encoder.addProperty("endMargin", this.endMargin);
        }
    }

    public interface OnHierarchyChangeListener {
        void onChildViewAdded(View view, View view2);

        void onChildViewRemoved(View view, View view2);
    }

    private static final class TouchTarget {
        public static final int ALL_POINTER_IDS = -1;
        private static final int MAX_RECYCLED = 32;
        private static TouchTarget sRecycleBin;
        private static final Object sRecycleLock = null;
        private static int sRecycledCount;
        public View child;
        public TouchTarget next;
        public int pointerIdBits;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewGroup.TouchTarget.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.ViewGroup.TouchTarget.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewGroup.TouchTarget.<clinit>():void");
        }

        private TouchTarget() {
        }

        public static TouchTarget obtain(View child, int pointerIdBits) {
            if (child == null) {
                throw new IllegalArgumentException("child must be non-null");
            }
            TouchTarget target;
            synchronized (sRecycleLock) {
                if (sRecycleBin == null) {
                    target = new TouchTarget();
                } else {
                    target = sRecycleBin;
                    sRecycleBin = target.next;
                    sRecycledCount += ALL_POINTER_IDS;
                    target.next = null;
                }
            }
            target.child = child;
            target.pointerIdBits = pointerIdBits;
            return target;
        }

        public void recycle() {
            if (this.child == null) {
                throw new IllegalStateException("already recycled once");
            }
            synchronized (sRecycleLock) {
                if (sRecycledCount < MAX_RECYCLED) {
                    this.next = sRecycleBin;
                    sRecycleBin = this;
                    sRecycledCount += ViewGroup.PERSISTENT_ANIMATION_CACHE;
                } else {
                    this.next = null;
                }
                this.child = null;
            }
        }
    }

    static class ViewLocationHolder implements Comparable<ViewLocationHolder> {
        public static final int COMPARISON_STRATEGY_LOCATION = 2;
        public static final int COMPARISON_STRATEGY_STRIPE = 1;
        private static final int MAX_POOL_SIZE = 32;
        private static int sComparisonStrategy;
        private static final SynchronizedPool<ViewLocationHolder> sPool = null;
        private int mLayoutDirection;
        private final Rect mLocation;
        public View mView;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewGroup.ViewLocationHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.ViewGroup.ViewLocationHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.view.ViewGroup.ViewLocationHolder.<clinit>():void");
        }

        ViewLocationHolder() {
            this.mLocation = new Rect();
        }

        public static ViewLocationHolder obtain(ViewGroup root, View view) {
            ViewLocationHolder holder = (ViewLocationHolder) sPool.acquire();
            if (holder == null) {
                holder = new ViewLocationHolder();
            }
            holder.init(root, view);
            return holder;
        }

        public static void setComparisonStrategy(int strategy) {
            sComparisonStrategy = strategy;
        }

        public void recycle() {
            clear();
            sPool.release(this);
        }

        public /* bridge */ /* synthetic */ int compareTo(Object another) {
            return compareTo((ViewLocationHolder) another);
        }

        public int compareTo(ViewLocationHolder another) {
            if (another == null) {
                return COMPARISON_STRATEGY_STRIPE;
            }
            if (sComparisonStrategy == COMPARISON_STRATEGY_STRIPE) {
                if (this.mLocation.bottom - another.mLocation.top <= 0) {
                    return ViewGroup.LAYOUT_MODE_UNDEFINED;
                }
                if (this.mLocation.top - another.mLocation.bottom >= 0) {
                    return COMPARISON_STRATEGY_STRIPE;
                }
            }
            if (this.mLayoutDirection == 0) {
                int leftDifference = this.mLocation.left - another.mLocation.left;
                if (leftDifference != 0) {
                    return leftDifference;
                }
            }
            int rightDifference = this.mLocation.right - another.mLocation.right;
            if (rightDifference != 0) {
                return -rightDifference;
            }
            int topDifference = this.mLocation.top - another.mLocation.top;
            if (topDifference != 0) {
                return topDifference;
            }
            int heightDiference = this.mLocation.height() - another.mLocation.height();
            if (heightDiference != 0) {
                return -heightDiference;
            }
            int widthDiference = this.mLocation.width() - another.mLocation.width();
            if (widthDiference != 0) {
                return -widthDiference;
            }
            return this.mView.getAccessibilityViewId() - another.mView.getAccessibilityViewId();
        }

        private void init(ViewGroup root, View view) {
            Rect viewLocation = this.mLocation;
            view.getDrawingRect(viewLocation);
            root.offsetDescendantRectToMyCoords(view, viewLocation);
            this.mView = view;
            this.mLayoutDirection = root.getLayoutDirection();
        }

        private void clear() {
            this.mView = null;
            this.mLocation.set(ViewGroup.PERSISTENT_NO_CACHE, ViewGroup.PERSISTENT_NO_CACHE, ViewGroup.PERSISTENT_NO_CACHE, ViewGroup.PERSISTENT_NO_CACHE);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewGroup.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.ViewGroup.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.ViewGroup.<clinit>():void");
    }

    private void removePointersFromTouchTargets(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewGroup.removePointersFromTouchTargets(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ViewGroup.removePointersFromTouchTargets(int):void");
    }

    private void setBooleanFlag(int r1, boolean r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewGroup.setBooleanFlag(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ViewGroup.setBooleanFlag(int, boolean):void");
    }

    protected abstract void onLayout(boolean z, int i, int i2, int i3, int i4);

    public ViewGroup(Context context) {
        this(context, null);
    }

    public ViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, PERSISTENT_NO_CACHE);
    }

    public ViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, PERSISTENT_NO_CACHE);
    }

    public ViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mLastTouchDownIndex = LAYOUT_MODE_UNDEFINED;
        this.mLayoutMode = LAYOUT_MODE_UNDEFINED;
        this.mSuppressLayout = DBG;
        this.mLayoutCalledWhileSuppressed = DBG;
        this.mChildCountWithTransientState = PERSISTENT_NO_CACHE;
        this.mTransientIndices = null;
        this.mTransientViews = null;
        this.mLayoutTransitionListener = new TransitionListener() {
            public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                if (transitionType == ViewGroup.PERSISTENT_ALL_CACHES) {
                    ViewGroup.this.startViewTransition(view);
                }
            }

            public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                if (ViewGroup.this.mLayoutCalledWhileSuppressed && !transition.isChangingLayout()) {
                    ViewGroup.this.requestLayout();
                    ViewGroup.this.mLayoutCalledWhileSuppressed = ViewGroup.DBG;
                }
                if (transitionType == ViewGroup.PERSISTENT_ALL_CACHES && ViewGroup.this.mTransitioningViews != null) {
                    ViewGroup.this.endViewTransition(view);
                }
            }
        };
        initViewGroup();
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private boolean debugDraw() {
        if (DEBUG_DRAW) {
            return true;
        }
        return this.mAttachInfo != null ? this.mAttachInfo.mDebugLayout : DBG;
    }

    private void initViewGroup() {
        if (!debugDraw()) {
            setFlags(FLAG_OPTIMIZE_INVALIDATE, FLAG_OPTIMIZE_INVALIDATE);
        }
        this.mGroupFlags |= PERSISTENT_ANIMATION_CACHE;
        this.mGroupFlags |= PERSISTENT_SCROLLING_CACHE;
        this.mGroupFlags |= FLAG_ANIMATION_DONE;
        this.mGroupFlags |= FLAG_ANIMATION_CACHE;
        this.mGroupFlags |= FLAG_ALWAYS_DRAWN_WITH_CACHE;
        if (this.mContext.getApplicationInfo().targetSdkVersion >= 11) {
            this.mGroupFlags |= FLAG_SPLIT_MOTION_EVENTS;
        }
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        this.mChildren = new View[ARRAY_INITIAL_CAPACITY];
        this.mChildrenCount = PERSISTENT_NO_CACHE;
        this.mPersistentDrawingCache = PERSISTENT_SCROLLING_CACHE;
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewGroup, defStyleAttr, defStyleRes);
        int N = a.getIndexCount();
        for (int i = PERSISTENT_NO_CACHE; i < N; i += PERSISTENT_ANIMATION_CACHE) {
            int attr = a.getIndex(i);
            switch (attr) {
                case PERSISTENT_NO_CACHE /*0*/:
                    setClipChildren(a.getBoolean(attr, true));
                    break;
                case PERSISTENT_ANIMATION_CACHE /*1*/:
                    setClipToPadding(a.getBoolean(attr, true));
                    break;
                case PERSISTENT_SCROLLING_CACHE /*2*/:
                    int id = a.getResourceId(attr, LAYOUT_MODE_UNDEFINED);
                    if (id <= 0) {
                        break;
                    }
                    setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this.mContext, id));
                    break;
                case PERSISTENT_ALL_CACHES /*3*/:
                    setAnimationCacheEnabled(a.getBoolean(attr, true));
                    break;
                case FLAG_INVALIDATE_REQUIRED /*4*/:
                    setPersistentDrawingCache(a.getInt(attr, PERSISTENT_SCROLLING_CACHE));
                    break;
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    setAlwaysDrawnWithCacheEnabled(a.getBoolean(attr, true));
                    break;
                case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                    setAddStatesFromChildren(a.getBoolean(attr, DBG));
                    break;
                case HwCfgFilePolicy.CLOUD_APN /*7*/:
                    setDescendantFocusability(DESCENDANT_FOCUSABILITY_FLAGS[a.getInt(attr, PERSISTENT_NO_CACHE)]);
                    break;
                case FLAG_RUN_ANIMATION /*8*/:
                    setMotionEventSplittingEnabled(a.getBoolean(attr, DBG));
                    break;
                case PGSdk.TYPE_SCRLOCK /*9*/:
                    if (!a.getBoolean(attr, DBG)) {
                        break;
                    }
                    setLayoutTransition(new LayoutTransition());
                    break;
                case PGSdk.TYPE_CLOCK /*10*/:
                    setLayoutMode(a.getInt(attr, LAYOUT_MODE_UNDEFINED));
                    break;
                case PGSdk.TYPE_IM /*11*/:
                    setTransitionGroup(a.getBoolean(attr, DBG));
                    break;
                case ARRAY_INITIAL_CAPACITY /*12*/:
                    setTouchscreenBlocksFocus(a.getBoolean(attr, DBG));
                    break;
                default:
                    break;
            }
        }
        a.recycle();
    }

    @ExportedProperty(category = "focus", mapping = {@IntToString(from = 131072, to = "FOCUS_BEFORE_DESCENDANTS"), @IntToString(from = 262144, to = "FOCUS_AFTER_DESCENDANTS"), @IntToString(from = 393216, to = "FOCUS_BLOCK_DESCENDANTS")})
    public int getDescendantFocusability() {
        return this.mGroupFlags & FOCUS_BLOCK_DESCENDANTS;
    }

    public void setDescendantFocusability(int focusability) {
        switch (focusability) {
            case FOCUS_BEFORE_DESCENDANTS /*131072*/:
            case FOCUS_AFTER_DESCENDANTS /*262144*/:
            case FOCUS_BLOCK_DESCENDANTS /*393216*/:
                this.mGroupFlags &= -393217;
                this.mGroupFlags |= FOCUS_BLOCK_DESCENDANTS & focusability;
            default:
                throw new IllegalArgumentException("must be one of FOCUS_BEFORE_DESCENDANTS, FOCUS_AFTER_DESCENDANTS, FOCUS_BLOCK_DESCENDANTS");
        }
    }

    void handleFocusGainInternal(int direction, Rect previouslyFocusedRect) {
        if (this.mFocused != null) {
            this.mFocused.unFocus(this);
            this.mFocused = null;
        }
        super.handleFocusGainInternal(direction, previouslyFocusedRect);
    }

    public void requestChildFocus(View child, View focused) {
        if (getDescendantFocusability() != FOCUS_BLOCK_DESCENDANTS) {
            super.unFocus(focused);
            if (this.mFocused != child) {
                if (this.mFocused != null) {
                    this.mFocused.unFocus(focused);
                }
                this.mFocused = child;
            }
            if (this.mParent != null) {
                this.mParent.requestChildFocus(this, focused);
            }
        }
    }

    public void focusableViewAvailable(View v) {
        if (this.mParent != null && getDescendantFocusability() != FOCUS_BLOCK_DESCENDANTS) {
            if (!isFocusableInTouchMode() && shouldBlockFocusForTouchscreen()) {
                return;
            }
            if (!isFocused() || getDescendantFocusability() == FOCUS_AFTER_DESCENDANTS) {
                this.mParent.focusableViewAvailable(v);
            }
        }
    }

    public boolean showContextMenuForChild(View originalView) {
        boolean z = DBG;
        if (isShowingContextMenuWithCoords()) {
            return DBG;
        }
        if (this.mParent != null) {
            z = this.mParent.showContextMenuForChild(originalView);
        }
        return z;
    }

    public final boolean isShowingContextMenuWithCoords() {
        return (this.mGroupFlags & FLAG_SHOW_CONTEXT_MENU_WITH_COORDS) != 0 ? true : DBG;
    }

    public boolean showContextMenuForChild(View originalView, float x, float y) {
        try {
            this.mGroupFlags |= FLAG_SHOW_CONTEXT_MENU_WITH_COORDS;
            if (showContextMenuForChild(originalView)) {
                return true;
            }
            this.mGroupFlags &= -536870913;
            return this.mParent != null ? this.mParent.showContextMenuForChild(originalView, x, y) : DBG;
        } finally {
            this.mGroupFlags &= -536870913;
        }
    }

    public ActionMode startActionModeForChild(View originalView, Callback callback) {
        if ((this.mGroupFlags & FLAG_START_ACTION_MODE_FOR_CHILD_IS_TYPED) != 0) {
            return SENTINEL_ACTION_MODE;
        }
        try {
            this.mGroupFlags |= FLAG_START_ACTION_MODE_FOR_CHILD_IS_NOT_TYPED;
            ActionMode startActionModeForChild = startActionModeForChild(originalView, callback, PERSISTENT_NO_CACHE);
            return startActionModeForChild;
        } finally {
            this.mGroupFlags &= -268435457;
        }
    }

    public ActionMode startActionModeForChild(View originalView, Callback callback, int type) {
        if ((this.mGroupFlags & FLAG_START_ACTION_MODE_FOR_CHILD_IS_NOT_TYPED) == 0 && type == 0) {
            try {
                this.mGroupFlags |= FLAG_START_ACTION_MODE_FOR_CHILD_IS_TYPED;
                ActionMode mode = startActionModeForChild(originalView, callback);
                if (mode != SENTINEL_ACTION_MODE) {
                    return mode;
                }
            } finally {
                this.mGroupFlags &= -134217729;
            }
        }
        if (this.mParent == null) {
            return null;
        }
        try {
            return this.mParent.startActionModeForChild(originalView, callback, type);
        } catch (AbstractMethodError e) {
            return this.mParent.startActionModeForChild(originalView, callback);
        }
    }

    public boolean dispatchActivityResult(String who, int requestCode, int resultCode, Intent data) {
        if (super.dispatchActivityResult(who, requestCode, resultCode, data)) {
            return true;
        }
        int childCount = getChildCount();
        for (int i = PERSISTENT_NO_CACHE; i < childCount; i += PERSISTENT_ANIMATION_CACHE) {
            if (getChildAt(i).dispatchActivityResult(who, requestCode, resultCode, data)) {
                return true;
            }
        }
        return DBG;
    }

    public View focusSearch(View focused, int direction) {
        if (isRootNamespace()) {
            return FocusFinder.getInstance().findNextFocus(this, focused, direction);
        }
        if (this.mParent != null) {
            return this.mParent.focusSearch(focused, direction);
        }
        return null;
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        return DBG;
    }

    public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        ViewParent parent = this.mParent;
        if (parent != null && onRequestSendAccessibilityEvent(child, event)) {
            return parent.requestSendAccessibilityEvent(this, event);
        }
        return DBG;
    }

    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        if (this.mAccessibilityDelegate != null) {
            return this.mAccessibilityDelegate.onRequestSendAccessibilityEvent(this, child, event);
        }
        return onRequestSendAccessibilityEventInternal(child, event);
    }

    public boolean onRequestSendAccessibilityEventInternal(View child, AccessibilityEvent event) {
        return true;
    }

    public void childHasTransientStateChanged(View child, boolean childHasTransientState) {
        boolean oldHasTransientState = hasTransientState();
        if (childHasTransientState) {
            this.mChildCountWithTransientState += PERSISTENT_ANIMATION_CACHE;
        } else {
            this.mChildCountWithTransientState += LAYOUT_MODE_UNDEFINED;
        }
        boolean newHasTransientState = hasTransientState();
        if (this.mParent != null && oldHasTransientState != newHasTransientState) {
            try {
                this.mParent.childHasTransientStateChanged(this, newHasTransientState);
            } catch (AbstractMethodError e) {
                Log.e(TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
            }
        }
    }

    public boolean hasTransientState() {
        return this.mChildCountWithTransientState <= 0 ? super.hasTransientState() : true;
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (this.mFocused != null) {
            return this.mFocused.dispatchUnhandledMove(focused, direction);
        }
        return DBG;
    }

    public void clearChildFocus(View child) {
        this.mFocused = null;
        if (this.mParent != null) {
            this.mParent.clearChildFocus(this);
        }
    }

    public void clearFocus() {
        if (this.mFocused == null) {
            super.clearFocus();
            return;
        }
        View focused = this.mFocused;
        this.mFocused = null;
        focused.clearFocus();
    }

    void unFocus(View focused) {
        if (this.mFocused == null) {
            super.unFocus(focused);
            return;
        }
        this.mFocused.unFocus(focused);
        this.mFocused = null;
    }

    public View getFocusedChild() {
        return this.mFocused;
    }

    View getDeepestFocusedChild() {
        for (View view = this; view != null; view = view instanceof ViewGroup ? ((ViewGroup) view).getFocusedChild() : null) {
            if (view.isFocused()) {
                return view;
            }
        }
        return null;
    }

    public boolean hasFocus() {
        return ((this.mPrivateFlags & PERSISTENT_SCROLLING_CACHE) == 0 && this.mFocused == null) ? DBG : true;
    }

    public View findFocus() {
        if (isFocused()) {
            return this;
        }
        if (this.mFocused != null) {
            return this.mFocused.findFocus();
        }
        return null;
    }

    public boolean hasFocusable() {
        if ((this.mViewFlags & ARRAY_INITIAL_CAPACITY) != 0) {
            return DBG;
        }
        if (isFocusable()) {
            return true;
        }
        if (getDescendantFocusability() != FOCUS_BLOCK_DESCENDANTS) {
            int count = this.mChildrenCount;
            View[] children = this.mChildren;
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                if (children[i].hasFocusable()) {
                    return true;
                }
            }
        }
        return DBG;
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        int focusableCount = views.size();
        int descendantFocusability = getDescendantFocusability();
        if (descendantFocusability != FOCUS_BLOCK_DESCENDANTS) {
            if (shouldBlockFocusForTouchscreen()) {
                focusableMode |= PERSISTENT_ANIMATION_CACHE;
            }
            int count = this.mChildrenCount;
            View[] children = this.mChildren;
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                View child = children[i];
                if ((child.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0) {
                    child.addFocusables(views, direction, focusableMode);
                }
            }
        }
        if (descendantFocusability == FOCUS_AFTER_DESCENDANTS && focusableCount != views.size()) {
            return;
        }
        if (isFocusableInTouchMode() || !shouldBlockFocusForTouchscreen()) {
            super.addFocusables(views, direction, focusableMode);
        }
    }

    public void setTouchscreenBlocksFocus(boolean touchscreenBlocksFocus) {
        if (touchscreenBlocksFocus) {
            this.mGroupFlags |= FLAG_TOUCHSCREEN_BLOCKS_FOCUS;
            if (hasFocus() && !getDeepestFocusedChild().isFocusableInTouchMode()) {
                View newFocus = focusSearch(PERSISTENT_SCROLLING_CACHE);
                if (newFocus != null) {
                    newFocus.requestFocus();
                    return;
                }
                return;
            }
            return;
        }
        this.mGroupFlags &= -67108865;
    }

    public boolean getTouchscreenBlocksFocus() {
        return (this.mGroupFlags & FLAG_TOUCHSCREEN_BLOCKS_FOCUS) != 0 ? true : DBG;
    }

    boolean shouldBlockFocusForTouchscreen() {
        if (getTouchscreenBlocksFocus()) {
            return this.mContext.getPackageManager().hasSystemFeature("android.hardware.touchscreen");
        }
        return DBG;
    }

    public void findViewsWithText(ArrayList<View> outViews, CharSequence text, int flags) {
        super.findViewsWithText(outViews, text, flags);
        int childrenCount = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < childrenCount; i += PERSISTENT_ANIMATION_CACHE) {
            View child = children[i];
            if ((child.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0 && (child.mPrivateFlags & FLAG_RUN_ANIMATION) == 0) {
                child.findViewsWithText(outViews, text, flags);
            }
        }
    }

    public View findViewByAccessibilityIdTraversal(int accessibilityId) {
        View foundView = super.findViewByAccessibilityIdTraversal(accessibilityId);
        if (foundView != null) {
            return foundView;
        }
        if (getAccessibilityNodeProvider() != null) {
            return null;
        }
        int childrenCount = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < childrenCount; i += PERSISTENT_ANIMATION_CACHE) {
            foundView = children[i].findViewByAccessibilityIdTraversal(accessibilityId);
            if (foundView != null) {
                return foundView;
            }
        }
        return null;
    }

    public void dispatchWindowFocusChanged(boolean hasFocus) {
        super.dispatchWindowFocusChanged(hasFocus);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchWindowFocusChanged(hasFocus);
        }
    }

    public void addTouchables(ArrayList<View> views) {
        super.addTouchables(views);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = children[i];
            if ((child.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0) {
                child.addTouchables(views);
            }
        }
    }

    public void makeOptionalFitsSystemWindows() {
        super.makeOptionalFitsSystemWindows();
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].makeOptionalFitsSystemWindows();
        }
    }

    public void dispatchDisplayHint(int hint) {
        super.dispatchDisplayHint(hint);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchDisplayHint(hint);
        }
    }

    protected void onChildVisibilityChanged(View child, int oldVisibility, int newVisibility) {
        if (this.mTransition != null) {
            if (newVisibility == 0) {
                this.mTransition.showChild(this, child, oldVisibility);
            } else {
                this.mTransition.hideChild(this, child, newVisibility);
                if (this.mTransitioningViews != null && this.mTransitioningViews.contains(child)) {
                    if (this.mVisibilityChangingChildren == null) {
                        this.mVisibilityChangingChildren = new ArrayList();
                    }
                    this.mVisibilityChangingChildren.add(child);
                    addDisappearingView(child);
                }
            }
        }
        if (newVisibility == 0 && this.mCurrentDragStartEvent != null && !this.mChildrenInterestedInDrag.contains(child)) {
            notifyChildOfDragStart(child);
        }
    }

    protected void dispatchVisibilityChanged(View changedView, int visibility) {
        super.dispatchVisibilityChanged(changedView, visibility);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchVisibilityChanged(changedView, visibility);
        }
    }

    public void dispatchWindowVisibilityChanged(int visibility) {
        super.dispatchWindowVisibilityChanged(visibility);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchWindowVisibilityChanged(visibility);
        }
    }

    boolean dispatchVisibilityAggregated(boolean isVisible) {
        isVisible = super.dispatchVisibilityAggregated(isVisible);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            if (children[i].getVisibility() == 0) {
                children[i].dispatchVisibilityAggregated(isVisible);
            }
        }
        return isVisible;
    }

    public void dispatchConfigurationChanged(Configuration newConfig) {
        super.dispatchConfigurationChanged(newConfig);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchConfigurationChanged(newConfig);
        }
    }

    public void recomputeViewAttributes(View child) {
        if (this.mAttachInfo != null && !this.mAttachInfo.mRecomputeGlobalAttributes) {
            ViewParent parent = this.mParent;
            if (parent != null) {
                parent.recomputeViewAttributes(this);
            }
        }
    }

    void dispatchCollectViewAttributes(AttachInfo attachInfo, int visibility) {
        if ((visibility & ARRAY_INITIAL_CAPACITY) == 0) {
            super.dispatchCollectViewAttributes(attachInfo, visibility);
            int count = this.mChildrenCount;
            View[] children = this.mChildren;
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                View child = children[i];
                child.dispatchCollectViewAttributes(attachInfo, (child.mViewFlags & ARRAY_INITIAL_CAPACITY) | visibility);
            }
        }
    }

    public void bringChildToFront(View child) {
        int index = indexOfChild(child);
        if (index >= 0) {
            removeFromArray(index);
            addInArray(child, this.mChildrenCount);
            child.mParent = this;
            requestLayout();
            invalidate();
        }
    }

    private PointF getLocalPoint() {
        if (this.mLocalPoint == null) {
            this.mLocalPoint = new PointF();
        }
        return this.mLocalPoint;
    }

    public boolean dispatchDragEvent(DragEvent event) {
        boolean retval = DBG;
        float tx = event.mX;
        float ty = event.mY;
        ViewRootImpl root = getViewRootImpl();
        PointF localPoint = getLocalPoint();
        View child;
        View target;
        View view;
        switch (event.mAction) {
            case PERSISTENT_ANIMATION_CACHE /*1*/:
                this.mCurrentDragView = null;
                this.mCurrentDragStartEvent = DragEvent.obtain(event);
                if (this.mChildrenInterestedInDrag == null) {
                    this.mChildrenInterestedInDrag = new HashSet();
                } else {
                    this.mChildrenInterestedInDrag.clear();
                }
                int count = this.mChildrenCount;
                View[] children = this.mChildren;
                for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                    child = children[i];
                    child.mPrivateFlags2 &= -4;
                    if (child.getVisibility() == 0) {
                        if (notifyChildOfDragStart(children[i])) {
                            retval = true;
                        }
                    }
                }
                this.mIsInterestedInDrag = super.dispatchDragEvent(event);
                if (this.mIsInterestedInDrag) {
                    retval = true;
                }
                if (retval) {
                    return retval;
                }
                this.mCurrentDragStartEvent.recycle();
                this.mCurrentDragStartEvent = null;
                return retval;
            case PERSISTENT_SCROLLING_CACHE /*2*/:
                target = findFrontmostDroppableChildAt(event.mX, event.mY, localPoint);
                if (target == null && this.mIsInterestedInDrag) {
                    target = this;
                }
                View view2 = this.mCurrentDragView;
                if (r0 != target) {
                    root.setDragFocus(target);
                    int action = event.mAction;
                    event.mX = 0.0f;
                    event.mY = 0.0f;
                    if (this.mCurrentDragView != null) {
                        view = this.mCurrentDragView;
                        event.mAction = 6;
                        if (view != this) {
                            view.dispatchDragEvent(event);
                            view.mPrivateFlags2 &= -3;
                            view.refreshDrawableState();
                        } else {
                            super.dispatchDragEvent(event);
                        }
                    }
                    this.mCurrentDragView = target;
                    if (target != null) {
                        event.mAction = 5;
                        if (target != this) {
                            target.dispatchDragEvent(event);
                            target.mPrivateFlags2 |= PERSISTENT_SCROLLING_CACHE;
                            target.refreshDrawableState();
                        } else {
                            super.dispatchDragEvent(event);
                        }
                    }
                    event.mAction = action;
                    event.mX = tx;
                    event.mY = ty;
                }
                if (target == null) {
                    return DBG;
                }
                if (target == this) {
                    return super.dispatchDragEvent(event);
                }
                event.mX = localPoint.x;
                event.mY = localPoint.y;
                retval = target.dispatchDragEvent(event);
                event.mX = tx;
                event.mY = ty;
                return retval;
            case PERSISTENT_ALL_CACHES /*3*/:
                target = findFrontmostDroppableChildAt(event.mX, event.mY, localPoint);
                if (target != null) {
                    event.mX = localPoint.x;
                    event.mY = localPoint.y;
                    retval = target.dispatchDragEvent(event);
                    event.mX = tx;
                    event.mY = ty;
                    return retval;
                } else if (this.mIsInterestedInDrag) {
                    return super.dispatchDragEvent(event);
                } else {
                    return DBG;
                }
            case FLAG_INVALIDATE_REQUIRED /*4*/:
                HashSet<View> childrenInterestedInDrag = this.mChildrenInterestedInDrag;
                if (childrenInterestedInDrag != null) {
                    for (View child2 : childrenInterestedInDrag) {
                        if (child2.dispatchDragEvent(event)) {
                            retval = true;
                        }
                        child2.mPrivateFlags2 &= -4;
                        child2.refreshDrawableState();
                    }
                    childrenInterestedInDrag.clear();
                }
                if (this.mCurrentDragStartEvent != null) {
                    this.mCurrentDragStartEvent.recycle();
                    this.mCurrentDragStartEvent = null;
                }
                if (!this.mIsInterestedInDrag) {
                    return retval;
                }
                if (super.dispatchDragEvent(event)) {
                    retval = true;
                }
                this.mIsInterestedInDrag = DBG;
                return retval;
            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                if (this.mCurrentDragView == null) {
                    return DBG;
                }
                view = this.mCurrentDragView;
                if (view != this) {
                    view.dispatchDragEvent(event);
                    view.mPrivateFlags2 &= -3;
                    view.refreshDrawableState();
                } else {
                    super.dispatchDragEvent(event);
                }
                this.mCurrentDragView = null;
                return DBG;
            default:
                return DBG;
        }
    }

    View findFrontmostDroppableChildAt(float x, float y, PointF outLocalPoint) {
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = count + LAYOUT_MODE_UNDEFINED; i >= 0; i += LAYOUT_MODE_UNDEFINED) {
            View child = children[i];
            if (child.canAcceptDrag() && isTransformedTouchPointInView(x, y, child, outLocalPoint)) {
                return child;
            }
        }
        return null;
    }

    boolean notifyChildOfDragStart(View child) {
        float tx = this.mCurrentDragStartEvent.mX;
        float ty = this.mCurrentDragStartEvent.mY;
        float[] point = getTempPoint();
        point[PERSISTENT_NO_CACHE] = tx;
        point[PERSISTENT_ANIMATION_CACHE] = ty;
        transformPointToViewLocal(point, child);
        this.mCurrentDragStartEvent.mX = point[PERSISTENT_NO_CACHE];
        this.mCurrentDragStartEvent.mY = point[PERSISTENT_ANIMATION_CACHE];
        boolean canAccept = child.dispatchDragEvent(this.mCurrentDragStartEvent);
        this.mCurrentDragStartEvent.mX = tx;
        this.mCurrentDragStartEvent.mY = ty;
        if (canAccept) {
            this.mChildrenInterestedInDrag.add(child);
            if (!child.canAcceptDrag()) {
                child.mPrivateFlags2 |= PERSISTENT_ANIMATION_CACHE;
                child.refreshDrawableState();
            }
        }
        return canAccept;
    }

    public void dispatchWindowSystemUiVisiblityChanged(int visible) {
        super.dispatchWindowSystemUiVisiblityChanged(visible);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchWindowSystemUiVisiblityChanged(visible);
        }
    }

    public void dispatchSystemUiVisibilityChanged(int visible) {
        super.dispatchSystemUiVisibilityChanged(visible);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchSystemUiVisibilityChanged(visible);
        }
    }

    boolean updateLocalSystemUiVisibility(int localValue, int localChanges) {
        boolean changed = super.updateLocalSystemUiVisibility(localValue, localChanges);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            changed |= children[i].updateLocalSystemUiVisibility(localValue, localChanges);
        }
        return changed;
    }

    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if ((this.mPrivateFlags & 18) == 18) {
            return super.dispatchKeyEventPreIme(event);
        }
        if (this.mFocused == null || (this.mFocused.mPrivateFlags & FLAG_ANIMATION_DONE) != FLAG_ANIMATION_DONE) {
            return DBG;
        }
        return this.mFocused.dispatchKeyEventPreIme(event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onKeyEvent(event, PERSISTENT_ANIMATION_CACHE);
        }
        if ((this.mPrivateFlags & 18) == 18) {
            if (super.dispatchKeyEvent(event)) {
                return true;
            }
        } else if (this.mFocused != null && (this.mFocused.mPrivateFlags & FLAG_ANIMATION_DONE) == FLAG_ANIMATION_DONE && this.mFocused.dispatchKeyEvent(event)) {
            return true;
        }
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, PERSISTENT_ANIMATION_CACHE);
        }
        return DBG;
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        if ((this.mPrivateFlags & 18) == 18) {
            return super.dispatchKeyShortcutEvent(event);
        }
        if (this.mFocused == null || (this.mFocused.mPrivateFlags & FLAG_ANIMATION_DONE) != FLAG_ANIMATION_DONE) {
            return DBG;
        }
        return this.mFocused.dispatchKeyShortcutEvent(event);
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTrackballEvent(event, PERSISTENT_ANIMATION_CACHE);
        }
        if ((this.mPrivateFlags & 18) == 18) {
            if (super.dispatchTrackballEvent(event)) {
                return true;
            }
        } else if (this.mFocused != null && (this.mFocused.mPrivateFlags & FLAG_ANIMATION_DONE) == FLAG_ANIMATION_DONE && this.mFocused.dispatchTrackballEvent(event)) {
            return true;
        }
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, PERSISTENT_ANIMATION_CACHE);
        }
        return DBG;
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        if (isOnScrollbarThumb(x, y) || isDraggingScrollBar()) {
            return PointerIcon.getSystemIcon(this.mContext, RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED);
        }
        int childrenCount = this.mChildrenCount;
        if (childrenCount != 0) {
            boolean isChildrenDrawingOrderEnabled;
            ArrayList<View> preorderedList = buildOrderedChildList();
            if (preorderedList == null) {
                isChildrenDrawingOrderEnabled = isChildrenDrawingOrderEnabled();
            } else {
                isChildrenDrawingOrderEnabled = DBG;
            }
            View[] children = this.mChildren;
            int i = childrenCount + LAYOUT_MODE_UNDEFINED;
            while (i >= 0) {
                View child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, isChildrenDrawingOrderEnabled));
                if (isTransformedTouchPointInView(x, y, child, getLocalPoint())) {
                    PointerIcon pointerIcon = dispatchResolvePointerIcon(event, pointerIndex, child);
                    if (pointerIcon != null) {
                        if (preorderedList != null) {
                            preorderedList.clear();
                        }
                        return pointerIcon;
                    }
                    if (preorderedList != null) {
                        preorderedList.clear();
                    }
                } else {
                    i += LAYOUT_MODE_UNDEFINED;
                }
            }
            if (preorderedList != null) {
                preorderedList.clear();
            }
        }
        return super.onResolvePointerIcon(event, pointerIndex);
    }

    private PointerIcon dispatchResolvePointerIcon(MotionEvent event, int pointerIndex, View child) {
        if (child.hasIdentityMatrix()) {
            float offsetX = (float) (this.mScrollX - child.mLeft);
            float offsetY = (float) (this.mScrollY - child.mTop);
            event.offsetLocation(offsetX, offsetY);
            PointerIcon pointerIcon = child.onResolvePointerIcon(event, pointerIndex);
            event.offsetLocation(-offsetX, -offsetY);
            return pointerIcon;
        }
        MotionEvent transformedEvent = getTransformedMotionEvent(event, child);
        pointerIcon = child.onResolvePointerIcon(transformedEvent, pointerIndex);
        transformedEvent.recycle();
        return pointerIcon;
    }

    private int getAndVerifyPreorderedIndex(int childrenCount, int i, boolean customOrder) {
        if (!customOrder) {
            return i;
        }
        int childIndex1 = getChildDrawingOrder(childrenCount, i);
        if (childIndex1 < childrenCount) {
            return childIndex1;
        }
        throw new IndexOutOfBoundsException("getChildDrawingOrder() returned invalid index " + childIndex1 + " (child count is " + childrenCount + ")");
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        View child;
        int action = event.getAction();
        boolean interceptHover = onInterceptHoverEvent(event);
        event.setAction(action);
        MotionEvent eventNoHistory = event;
        boolean z = DBG;
        HoverTarget firstOldHoverTarget = this.mFirstHoverTarget;
        this.mFirstHoverTarget = null;
        if (!(interceptHover || action == 10)) {
            float x = event.getX();
            float y = event.getY();
            int childrenCount = this.mChildrenCount;
            if (childrenCount != 0) {
                boolean isChildrenDrawingOrderEnabled;
                ArrayList<View> preorderedList = buildOrderedChildList();
                if (preorderedList == null) {
                    isChildrenDrawingOrderEnabled = isChildrenDrawingOrderEnabled();
                } else {
                    isChildrenDrawingOrderEnabled = DBG;
                }
                View[] children = this.mChildren;
                HoverTarget lastHoverTarget = null;
                for (int i = childrenCount + LAYOUT_MODE_UNDEFINED; i >= 0; i += LAYOUT_MODE_UNDEFINED) {
                    child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, isChildrenDrawingOrderEnabled));
                    if (canViewReceivePointerEvents(child) && isTransformedTouchPointInView(x, y, child, null)) {
                        boolean wasHovered;
                        int dispatchTransformedGenericPointerEvent;
                        HoverTarget hoverTarget = firstOldHoverTarget;
                        HoverTarget hoverTarget2 = null;
                        while (hoverTarget != null) {
                            View view = hoverTarget.child;
                            if (r0 == child) {
                                if (hoverTarget2 != null) {
                                    hoverTarget2.next = hoverTarget.next;
                                } else {
                                    firstOldHoverTarget = hoverTarget.next;
                                }
                                hoverTarget.next = null;
                                wasHovered = true;
                                if (lastHoverTarget == null) {
                                    lastHoverTarget.next = hoverTarget;
                                } else {
                                    this.mFirstHoverTarget = hoverTarget;
                                }
                                lastHoverTarget = hoverTarget;
                                if (action != 9) {
                                    if (!wasHovered) {
                                        dispatchTransformedGenericPointerEvent |= dispatchTransformedGenericPointerEvent(event, child);
                                    }
                                } else if (action == 7) {
                                    if (wasHovered) {
                                        eventNoHistory = obtainMotionEventNoHistoryOrSelf(eventNoHistory);
                                        eventNoHistory.setAction(9);
                                        z = dispatchTransformedGenericPointerEvent | dispatchTransformedGenericPointerEvent(eventNoHistory, child);
                                        eventNoHistory.setAction(action);
                                        dispatchTransformedGenericPointerEvent = z | dispatchTransformedGenericPointerEvent(eventNoHistory, child);
                                    } else {
                                        dispatchTransformedGenericPointerEvent |= dispatchTransformedGenericPointerEvent(event, child);
                                    }
                                }
                                if (dispatchTransformedGenericPointerEvent != 0) {
                                    break;
                                }
                            } else {
                                hoverTarget2 = hoverTarget;
                                hoverTarget = hoverTarget.next;
                            }
                        }
                        hoverTarget = HoverTarget.obtain(child);
                        wasHovered = DBG;
                        if (lastHoverTarget == null) {
                            this.mFirstHoverTarget = hoverTarget;
                        } else {
                            lastHoverTarget.next = hoverTarget;
                        }
                        lastHoverTarget = hoverTarget;
                        if (action != 9) {
                            if (action == 7) {
                                if (wasHovered) {
                                    dispatchTransformedGenericPointerEvent |= dispatchTransformedGenericPointerEvent(event, child);
                                } else {
                                    eventNoHistory = obtainMotionEventNoHistoryOrSelf(eventNoHistory);
                                    eventNoHistory.setAction(9);
                                    z = dispatchTransformedGenericPointerEvent | dispatchTransformedGenericPointerEvent(eventNoHistory, child);
                                    eventNoHistory.setAction(action);
                                    dispatchTransformedGenericPointerEvent = z | dispatchTransformedGenericPointerEvent(eventNoHistory, child);
                                }
                            }
                        } else if (wasHovered) {
                            dispatchTransformedGenericPointerEvent |= dispatchTransformedGenericPointerEvent(event, child);
                        }
                        if (dispatchTransformedGenericPointerEvent != 0) {
                            break;
                        }
                    }
                }
                if (preorderedList != null) {
                    preorderedList.clear();
                }
            }
        }
        while (firstOldHoverTarget != null) {
            child = firstOldHoverTarget.child;
            if (action == 10) {
                z |= dispatchTransformedGenericPointerEvent(event, child);
            } else {
                if (action == 7) {
                    dispatchTransformedGenericPointerEvent(event, child);
                }
                eventNoHistory = obtainMotionEventNoHistoryOrSelf(eventNoHistory);
                eventNoHistory.setAction(10);
                dispatchTransformedGenericPointerEvent(eventNoHistory, child);
                eventNoHistory.setAction(action);
            }
            HoverTarget nextOldHoverTarget = firstOldHoverTarget.next;
            firstOldHoverTarget.recycle();
            firstOldHoverTarget = nextOldHoverTarget;
        }
        boolean newHoveredSelf = z ? DBG : true;
        if (newHoveredSelf != this.mHoveredSelf) {
            if (this.mHoveredSelf) {
                if (action == 10) {
                    z |= super.dispatchHoverEvent(event);
                } else {
                    if (action == 7) {
                        super.dispatchHoverEvent(event);
                    }
                    eventNoHistory = obtainMotionEventNoHistoryOrSelf(eventNoHistory);
                    eventNoHistory.setAction(10);
                    super.dispatchHoverEvent(eventNoHistory);
                    eventNoHistory.setAction(action);
                }
                this.mHoveredSelf = DBG;
            }
            if (newHoveredSelf) {
                if (action == 9) {
                    z |= super.dispatchHoverEvent(event);
                    this.mHoveredSelf = true;
                } else if (action == 7) {
                    eventNoHistory = obtainMotionEventNoHistoryOrSelf(eventNoHistory);
                    eventNoHistory.setAction(9);
                    z |= super.dispatchHoverEvent(eventNoHistory);
                    eventNoHistory.setAction(action);
                    z |= super.dispatchHoverEvent(eventNoHistory);
                    this.mHoveredSelf = true;
                }
            }
        } else if (newHoveredSelf) {
            z |= super.dispatchHoverEvent(event);
        }
        if (eventNoHistory != event) {
            eventNoHistory.recycle();
        }
        return z;
    }

    private void exitHoverTargets() {
        if (this.mHoveredSelf || this.mFirstHoverTarget != null) {
            long now = SystemClock.uptimeMillis();
            MotionEvent event = MotionEvent.obtain(now, now, 10, 0.0f, 0.0f, PERSISTENT_NO_CACHE);
            event.setSource(PerfHub.PERF_EVENT_RESTART);
            dispatchHoverEvent(event);
            event.recycle();
        }
    }

    private void cancelHoverTarget(View view) {
        HoverTarget predecessor = null;
        HoverTarget target = this.mFirstHoverTarget;
        while (target != null) {
            HoverTarget next = target.next;
            if (target.child == view) {
                if (predecessor == null) {
                    this.mFirstHoverTarget = next;
                } else {
                    predecessor.next = next;
                }
                target.recycle();
                long now = SystemClock.uptimeMillis();
                MotionEvent event = MotionEvent.obtain(now, now, 10, 0.0f, 0.0f, PERSISTENT_NO_CACHE);
                event.setSource(PerfHub.PERF_EVENT_RESTART);
                view.dispatchHoverEvent(event);
                event.recycle();
                return;
            }
            predecessor = target;
            target = next;
        }
    }

    protected boolean hasHoveredChild() {
        return this.mFirstHoverTarget != null ? true : DBG;
    }

    public void addChildrenForAccessibility(ArrayList<View> outChildren) {
        if (getAccessibilityNodeProvider() == null) {
            ChildListForAccessibility children = ChildListForAccessibility.obtain(this, true);
            try {
                int childrenCount = children.getChildCount();
                for (int i = PERSISTENT_NO_CACHE; i < childrenCount; i += PERSISTENT_ANIMATION_CACHE) {
                    View child = children.getChildAt(i);
                    if ((child.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0) {
                        if (child.includeForAccessibility()) {
                            outChildren.add(child);
                        } else {
                            child.addChildrenForAccessibility(outChildren);
                        }
                    }
                }
            } finally {
                children.recycle();
            }
        }
    }

    public boolean onInterceptHoverEvent(MotionEvent event) {
        if (event.isFromSource(InputDevice.SOURCE_MOUSE)) {
            int action = event.getAction();
            float x = event.getX();
            float y = event.getY();
            if ((action == 7 || action == 9) && isOnScrollbar(x, y)) {
                return true;
            }
        }
        return DBG;
    }

    private static MotionEvent obtainMotionEventNoHistoryOrSelf(MotionEvent event) {
        if (event.getHistorySize() == 0) {
            return event;
        }
        return MotionEvent.obtainNoHistory(event);
    }

    protected boolean dispatchGenericPointerEvent(MotionEvent event) {
        int childrenCount = this.mChildrenCount;
        if (childrenCount != 0) {
            boolean isChildrenDrawingOrderEnabled;
            float x = event.getX();
            float y = event.getY();
            ArrayList<View> preorderedList = buildOrderedChildList();
            if (preorderedList == null) {
                isChildrenDrawingOrderEnabled = isChildrenDrawingOrderEnabled();
            } else {
                isChildrenDrawingOrderEnabled = DBG;
            }
            View[] children = this.mChildren;
            for (int i = childrenCount + LAYOUT_MODE_UNDEFINED; i >= 0; i += LAYOUT_MODE_UNDEFINED) {
                View child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, isChildrenDrawingOrderEnabled));
                if (canViewReceivePointerEvents(child) && isTransformedTouchPointInView(x, y, child, null) && dispatchTransformedGenericPointerEvent(event, child)) {
                    if (preorderedList != null) {
                        preorderedList.clear();
                    }
                    return true;
                }
            }
            if (preorderedList != null) {
                preorderedList.clear();
            }
        }
        return super.dispatchGenericPointerEvent(event);
    }

    protected boolean dispatchGenericFocusedEvent(MotionEvent event) {
        if ((this.mPrivateFlags & 18) == 18) {
            return super.dispatchGenericFocusedEvent(event);
        }
        if (this.mFocused == null || (this.mFocused.mPrivateFlags & FLAG_ANIMATION_DONE) != FLAG_ANIMATION_DONE) {
            return DBG;
        }
        return this.mFocused.dispatchGenericMotionEvent(event);
    }

    private boolean dispatchTransformedGenericPointerEvent(MotionEvent event, View child) {
        if (child.hasIdentityMatrix()) {
            float offsetX = (float) (this.mScrollX - child.mLeft);
            float offsetY = (float) (this.mScrollY - child.mTop);
            event.offsetLocation(offsetX, offsetY);
            boolean handled = child.dispatchGenericMotionEvent(event);
            event.offsetLocation(-offsetX, -offsetY);
            return handled;
        }
        MotionEvent transformedEvent = getTransformedMotionEvent(event, child);
        handled = child.dispatchGenericMotionEvent(transformedEvent);
        transformedEvent.recycle();
        return handled;
    }

    private MotionEvent getTransformedMotionEvent(MotionEvent event, View child) {
        float offsetX = (float) (this.mScrollX - child.mLeft);
        float offsetY = (float) (this.mScrollY - child.mTop);
        MotionEvent transformedEvent = MotionEvent.obtain(event);
        transformedEvent.offsetLocation(offsetX, offsetY);
        if (!child.hasIdentityMatrix()) {
            transformedEvent.transform(child.getInverseMatrix());
        }
        return transformedEvent;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTouchEvent(ev, PERSISTENT_ANIMATION_CACHE);
        }
        if (ev.isTargetAccessibilityFocus() && isAccessibilityFocusedViewOrHost()) {
            ev.setTargetAccessibilityFocus(DBG);
        }
        boolean z = DBG;
        if (onFilterTouchEventForSecurity(ev)) {
            boolean intercepted;
            int action = ev.getAction();
            int actionMasked = action & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
            if (actionMasked == 0) {
                cancelAndClearTouchTargets(ev);
                resetTouchState();
            }
            if (actionMasked == 0 || this.mFirstTouchTarget != null) {
                if ((this.mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0 ? true : DBG) {
                    intercepted = DBG;
                } else {
                    intercepted = onInterceptTouchEvent(ev);
                    ev.setAction(action);
                }
            } else {
                intercepted = true;
            }
            if (intercepted || this.mFirstTouchTarget != null) {
                ev.setTargetAccessibilityFocus(DBG);
            }
            boolean canceled = !resetCancelNextUpFlag(this) ? actionMasked == PERSISTENT_ALL_CACHES ? true : DBG : true;
            boolean split = (this.mGroupFlags & FLAG_SPLIT_MOTION_EVENTS) != 0 ? true : DBG;
            TouchTarget touchTarget = null;
            boolean alreadyDispatchedToNewTouchTarget = DBG;
            if (!(canceled || intercepted)) {
                int idBitsToAssign;
                View findChildWithAccessibilityFocus = ev.isTargetAccessibilityFocus() ? findChildWithAccessibilityFocus() : null;
                if (!(actionMasked == 0 || (split && actionMasked == 5))) {
                    if (actionMasked == 7) {
                    }
                }
                int actionIndex = ev.getActionIndex();
                if (split) {
                    idBitsToAssign = PERSISTENT_ANIMATION_CACHE << ev.getPointerId(actionIndex);
                } else {
                    idBitsToAssign = LAYOUT_MODE_UNDEFINED;
                }
                removePointersFromTouchTargets(idBitsToAssign);
                int childrenCount = this.mChildrenCount;
                if (childrenCount != 0) {
                    boolean isChildrenDrawingOrderEnabled;
                    float x = ev.getX(actionIndex);
                    float y = ev.getY(actionIndex);
                    ArrayList<View> preorderedList = buildTouchDispatchChildList();
                    if (preorderedList == null) {
                        isChildrenDrawingOrderEnabled = isChildrenDrawingOrderEnabled();
                    } else {
                        isChildrenDrawingOrderEnabled = DBG;
                    }
                    View[] children = this.mChildren;
                    int i = childrenCount + LAYOUT_MODE_UNDEFINED;
                    while (i >= 0) {
                        int childIndex = getAndVerifyPreorderedIndex(childrenCount, i, isChildrenDrawingOrderEnabled);
                        View child = getAndVerifyPreorderedView(preorderedList, children, childIndex);
                        if (findChildWithAccessibilityFocus != null) {
                            if (findChildWithAccessibilityFocus != child) {
                                continue;
                                i += LAYOUT_MODE_UNDEFINED;
                            } else {
                                findChildWithAccessibilityFocus = null;
                                i = childrenCount + LAYOUT_MODE_UNDEFINED;
                            }
                        }
                        if (canViewReceivePointerEvents(child) && isTransformedTouchPointInView(x, y, child, null)) {
                            touchTarget = getTouchTarget(child);
                            if (touchTarget != null) {
                                touchTarget.pointerIdBits |= idBitsToAssign;
                                break;
                            }
                            resetCancelNextUpFlag(child);
                            if (dispatchTransformedTouchEvent(ev, DBG, child, idBitsToAssign)) {
                                this.mLastTouchDownTime = ev.getDownTime();
                                if (preorderedList != null) {
                                    for (int j = PERSISTENT_NO_CACHE; j < childrenCount; j += PERSISTENT_ANIMATION_CACHE) {
                                        if (children[childIndex] == this.mChildren[j]) {
                                            this.mLastTouchDownIndex = j;
                                            break;
                                        }
                                    }
                                } else {
                                    this.mLastTouchDownIndex = childIndex;
                                }
                                this.mLastTouchDownX = ev.getX();
                                this.mLastTouchDownY = ev.getY();
                                touchTarget = addTouchTarget(child, idBitsToAssign);
                                alreadyDispatchedToNewTouchTarget = true;
                            } else {
                                ev.setTargetAccessibilityFocus(DBG);
                                i += LAYOUT_MODE_UNDEFINED;
                            }
                        } else {
                            ev.setTargetAccessibilityFocus(DBG);
                            i += LAYOUT_MODE_UNDEFINED;
                        }
                    }
                    if (preorderedList != null) {
                        preorderedList.clear();
                    }
                }
                if (touchTarget == null && this.mFirstTouchTarget != null) {
                    touchTarget = this.mFirstTouchTarget;
                    while (touchTarget.next != null) {
                        touchTarget = touchTarget.next;
                    }
                    touchTarget.pointerIdBits |= idBitsToAssign;
                }
            }
            if (this.mFirstTouchTarget == null) {
                z = dispatchTransformedTouchEvent(ev, canceled, null, LAYOUT_MODE_UNDEFINED);
            } else {
                TouchTarget predecessor = null;
                TouchTarget target = this.mFirstTouchTarget;
                while (target != null) {
                    TouchTarget next = target.next;
                    if (alreadyDispatchedToNewTouchTarget && target == r23) {
                        z = true;
                    } else {
                        boolean cancelChild;
                        if (resetCancelNextUpFlag(target.child)) {
                            cancelChild = true;
                        } else {
                            cancelChild = intercepted;
                        }
                        if (dispatchTransformedTouchEvent(ev, cancelChild, target.child, target.pointerIdBits)) {
                            z = true;
                        }
                        if (cancelChild) {
                            if (predecessor == null) {
                                this.mFirstTouchTarget = next;
                            } else {
                                predecessor.next = next;
                            }
                            target.recycle();
                            target = next;
                        }
                    }
                    predecessor = target;
                    target = next;
                }
            }
            if (canceled || actionMasked == PERSISTENT_ANIMATION_CACHE || actionMasked == 7) {
                resetTouchState();
            } else if (split && actionMasked == 6) {
                removePointersFromTouchTargets(PERSISTENT_ANIMATION_CACHE << ev.getPointerId(ev.getActionIndex()));
            }
        }
        if (!(z || this.mInputEventConsistencyVerifier == null)) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(ev, PERSISTENT_ANIMATION_CACHE);
        }
        return z;
    }

    public ArrayList<View> buildTouchDispatchChildList() {
        return buildOrderedChildList();
    }

    private View findChildWithAccessibilityFocus() {
        ViewRootImpl viewRoot = getViewRootImpl();
        if (viewRoot == null) {
            return null;
        }
        View current = viewRoot.getAccessibilityFocusedHost();
        if (current == null) {
            return null;
        }
        View parent = current.getParent();
        while (parent instanceof View) {
            if (parent == this) {
                return current;
            }
            current = parent;
            parent = current.getParent();
        }
        return null;
    }

    private void resetTouchState() {
        clearTouchTargets();
        resetCancelNextUpFlag(this);
        this.mGroupFlags &= -524289;
        this.mNestedScrollAxes = PERSISTENT_NO_CACHE;
    }

    private static boolean resetCancelNextUpFlag(View view) {
        if (view == null || (view.mPrivateFlags & FLAG_TOUCHSCREEN_BLOCKS_FOCUS) == 0) {
            return DBG;
        }
        view.mPrivateFlags &= -67108865;
        return true;
    }

    private void clearTouchTargets() {
        TouchTarget target = this.mFirstTouchTarget;
        if (target != null) {
            TouchTarget next;
            do {
                next = target.next;
                target.recycle();
                target = next;
            } while (next != null);
            this.mFirstTouchTarget = null;
        }
    }

    private void cancelAndClearTouchTargets(MotionEvent event) {
        if (this.mFirstTouchTarget != null) {
            boolean syntheticEvent = DBG;
            if (event == null) {
                long now = SystemClock.uptimeMillis();
                event = MotionEvent.obtain(now, now, PERSISTENT_ALL_CACHES, 0.0f, 0.0f, PERSISTENT_NO_CACHE);
                event.setSource(PerfHub.PERF_EVENT_RESTART);
                syntheticEvent = true;
            }
            for (TouchTarget target = this.mFirstTouchTarget; target != null; target = target.next) {
                resetCancelNextUpFlag(target.child);
                dispatchTransformedTouchEvent(event, true, target.child, target.pointerIdBits);
            }
            clearTouchTargets();
            if (syntheticEvent) {
                event.recycle();
            }
        }
    }

    private TouchTarget getTouchTarget(View child) {
        for (TouchTarget target = this.mFirstTouchTarget; target != null; target = target.next) {
            if (target.child == child) {
                return target;
            }
        }
        return null;
    }

    private TouchTarget addTouchTarget(View child, int pointerIdBits) {
        TouchTarget target = TouchTarget.obtain(child, pointerIdBits);
        target.next = this.mFirstTouchTarget;
        this.mFirstTouchTarget = target;
        return target;
    }

    private void cancelTouchTarget(View view) {
        TouchTarget predecessor = null;
        TouchTarget target = this.mFirstTouchTarget;
        while (target != null) {
            TouchTarget next = target.next;
            if (target.child == view) {
                if (predecessor == null) {
                    this.mFirstTouchTarget = next;
                } else {
                    predecessor.next = next;
                }
                target.recycle();
                long now = SystemClock.uptimeMillis();
                MotionEvent event = MotionEvent.obtain(now, now, PERSISTENT_ALL_CACHES, 0.0f, 0.0f, PERSISTENT_NO_CACHE);
                event.setSource(PerfHub.PERF_EVENT_RESTART);
                view.dispatchTouchEvent(event);
                event.recycle();
                return;
            }
            predecessor = target;
            target = next;
        }
    }

    private static boolean canViewReceivePointerEvents(View child) {
        return ((child.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0 || child.getAnimation() != null) ? true : DBG;
    }

    private float[] getTempPoint() {
        if (this.mTempPoint == null) {
            this.mTempPoint = new float[PERSISTENT_SCROLLING_CACHE];
        }
        return this.mTempPoint;
    }

    protected boolean isTransformedTouchPointInView(float x, float y, View child, PointF outLocalPoint) {
        float[] point = getTempPoint();
        point[PERSISTENT_NO_CACHE] = x;
        point[PERSISTENT_ANIMATION_CACHE] = y;
        transformPointToViewLocal(point, child);
        boolean isInView = child.pointInView(point[PERSISTENT_NO_CACHE], point[PERSISTENT_ANIMATION_CACHE]);
        if (isInView && outLocalPoint != null) {
            outLocalPoint.set(point[PERSISTENT_NO_CACHE], point[PERSISTENT_ANIMATION_CACHE]);
        }
        return isInView;
    }

    public void transformPointToViewLocal(float[] point, View child) {
        point[PERSISTENT_NO_CACHE] = point[PERSISTENT_NO_CACHE] + ((float) (this.mScrollX - child.mLeft));
        point[PERSISTENT_ANIMATION_CACHE] = point[PERSISTENT_ANIMATION_CACHE] + ((float) (this.mScrollY - child.mTop));
        if (!child.hasIdentityMatrix()) {
            child.getInverseMatrix().mapPoints(point);
        }
    }

    private boolean dispatchTransformedTouchEvent(MotionEvent event, boolean cancel, View child, int desiredPointerIdBits) {
        int oldAction = event.getAction();
        boolean handled;
        if (cancel || oldAction == PERSISTENT_ALL_CACHES) {
            event.setAction(PERSISTENT_ALL_CACHES);
            if (child == null) {
                handled = super.dispatchTouchEvent(event);
            } else {
                handled = child.dispatchTouchEvent(event);
            }
            event.setAction(oldAction);
            return handled;
        }
        int oldPointerIdBits = event.getPointerIdBits();
        int newPointerIdBits = oldPointerIdBits & desiredPointerIdBits;
        if (newPointerIdBits == 0) {
            return DBG;
        }
        MotionEvent transformedEvent;
        if (newPointerIdBits != oldPointerIdBits) {
            transformedEvent = event.split(newPointerIdBits);
        } else if (child == null || child.hasIdentityMatrix()) {
            if (child == null) {
                handled = super.dispatchTouchEvent(event);
            } else {
                float offsetX = (float) (this.mScrollX - child.mLeft);
                float offsetY = (float) (this.mScrollY - child.mTop);
                event.offsetLocation(offsetX, offsetY);
                handled = child.dispatchTouchEvent(event);
                event.offsetLocation(-offsetX, -offsetY);
            }
            return handled;
        } else {
            transformedEvent = MotionEvent.obtain(event);
        }
        if (child == null) {
            handled = super.dispatchTouchEvent(transformedEvent);
        } else {
            transformedEvent.offsetLocation((float) (this.mScrollX - child.mLeft), (float) (this.mScrollY - child.mTop));
            if (!child.hasIdentityMatrix()) {
                transformedEvent.transform(child.getInverseMatrix());
            }
            handled = child.dispatchTouchEvent(transformedEvent);
        }
        transformedEvent.recycle();
        return handled;
    }

    public void setMotionEventSplittingEnabled(boolean split) {
        if (split) {
            this.mGroupFlags |= FLAG_SPLIT_MOTION_EVENTS;
        } else {
            this.mGroupFlags &= -2097153;
        }
    }

    public boolean isMotionEventSplittingEnabled() {
        return (this.mGroupFlags & FLAG_SPLIT_MOTION_EVENTS) == FLAG_SPLIT_MOTION_EVENTS ? true : DBG;
    }

    public boolean isTransitionGroup() {
        boolean z = true;
        if ((this.mGroupFlags & FLAG_IS_TRANSITION_GROUP_SET) != 0) {
            if ((this.mGroupFlags & FLAG_IS_TRANSITION_GROUP) == 0) {
                z = DBG;
            }
            return z;
        }
        ViewOutlineProvider outlineProvider = getOutlineProvider();
        if (getBackground() == null && getTransitionName() == null && (outlineProvider == null || outlineProvider == ViewOutlineProvider.BACKGROUND)) {
            z = DBG;
        }
        return z;
    }

    public void setTransitionGroup(boolean isTransitionGroup) {
        this.mGroupFlags |= FLAG_IS_TRANSITION_GROUP_SET;
        if (isTransitionGroup) {
            this.mGroupFlags |= FLAG_IS_TRANSITION_GROUP;
        } else {
            this.mGroupFlags &= -16777217;
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        boolean z = DBG;
        if ((this.mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0) {
            z = true;
        }
        if (disallowIntercept != z) {
            if (disallowIntercept) {
                this.mGroupFlags |= FLAG_DISALLOW_INTERCEPT;
            } else {
                this.mGroupFlags &= -524289;
            }
            if (this.mParent != null) {
                this.mParent.requestDisallowInterceptTouchEvent(disallowIntercept);
            }
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return (ev.isFromSource(InputDevice.SOURCE_MOUSE) && ev.getAction() == 0 && ev.isButtonPressed(PERSISTENT_ANIMATION_CACHE) && isOnScrollbarThumb(ev.getX(), ev.getY())) ? true : DBG;
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        int descendantFocusability = getDescendantFocusability();
        boolean took;
        switch (descendantFocusability) {
            case FOCUS_BEFORE_DESCENDANTS /*131072*/:
                took = super.requestFocus(direction, previouslyFocusedRect);
                if (!took) {
                    took = onRequestFocusInDescendants(direction, previouslyFocusedRect);
                }
                return took;
            case FOCUS_AFTER_DESCENDANTS /*262144*/:
                took = onRequestFocusInDescendants(direction, previouslyFocusedRect);
                if (!took) {
                    took = super.requestFocus(direction, previouslyFocusedRect);
                }
                return took;
            case FOCUS_BLOCK_DESCENDANTS /*393216*/:
                return super.requestFocus(direction, previouslyFocusedRect);
            default:
                throw new IllegalStateException("descendant focusability must be one of FOCUS_BEFORE_DESCENDANTS, FOCUS_AFTER_DESCENDANTS, FOCUS_BLOCK_DESCENDANTS but is " + descendantFocusability);
        }
    }

    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int index;
        int increment;
        int end;
        int count = this.mChildrenCount;
        if ((direction & PERSISTENT_SCROLLING_CACHE) != 0) {
            index = PERSISTENT_NO_CACHE;
            increment = PERSISTENT_ANIMATION_CACHE;
            end = count;
        } else {
            index = count + LAYOUT_MODE_UNDEFINED;
            increment = LAYOUT_MODE_UNDEFINED;
            end = LAYOUT_MODE_UNDEFINED;
        }
        View[] children = this.mChildren;
        for (int i = index; i != end; i += increment) {
            View child = children[i];
            if ((child.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0 && child.requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
        }
        return DBG;
    }

    public void dispatchStartTemporaryDetach() {
        super.dispatchStartTemporaryDetach();
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchStartTemporaryDetach();
        }
    }

    public void dispatchFinishTemporaryDetach() {
        super.dispatchFinishTemporaryDetach();
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchFinishTemporaryDetach();
        }
    }

    void dispatchAttachedToWindow(AttachInfo info, int visibility) {
        int i;
        this.mGroupFlags |= FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW;
        super.dispatchAttachedToWindow(info, visibility);
        this.mGroupFlags &= -4194305;
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = children[i];
            child.dispatchAttachedToWindow(info, combineVisibility(visibility, child.getVisibility()));
        }
        int transientCount = this.mTransientIndices == null ? PERSISTENT_NO_CACHE : this.mTransientIndices.size();
        for (i = PERSISTENT_NO_CACHE; i < transientCount; i += PERSISTENT_ANIMATION_CACHE) {
            View view = (View) this.mTransientViews.get(i);
            view.dispatchAttachedToWindow(info, combineVisibility(visibility, view.getVisibility()));
        }
    }

    void dispatchScreenStateChanged(int screenState) {
        super.dispatchScreenStateChanged(screenState);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchScreenStateChanged(screenState);
        }
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        boolean z = DBG;
        if (includeForAccessibility()) {
            z = super.dispatchPopulateAccessibilityEventInternal(event);
            if (z) {
                return z;
            }
        }
        ChildListForAccessibility children = ChildListForAccessibility.obtain(this, true);
        try {
            int childCount = children.getChildCount();
            for (int i = PERSISTENT_NO_CACHE; i < childCount; i += PERSISTENT_ANIMATION_CACHE) {
                View child = children.getChildAt(i);
                if ((child.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0) {
                    z = child.dispatchPopulateAccessibilityEvent(event);
                    if (z) {
                        return z;
                    }
                }
            }
            children.recycle();
            return DBG;
        } finally {
            children.recycle();
        }
    }

    public void dispatchProvideStructure(ViewStructure structure) {
        super.dispatchProvideStructure(structure);
        if (!isAssistBlocked() && structure.getChildCount() == 0) {
            int childrenCount = getChildCount();
            if (childrenCount > 0) {
                boolean isChildrenDrawingOrderEnabled;
                structure.setChildCount(childrenCount);
                ArrayList<View> preorderedList = buildOrderedChildList();
                if (preorderedList == null) {
                    isChildrenDrawingOrderEnabled = isChildrenDrawingOrderEnabled();
                } else {
                    isChildrenDrawingOrderEnabled = DBG;
                }
                View[] children = this.mChildren;
                for (int i = PERSISTENT_NO_CACHE; i < childrenCount; i += PERSISTENT_ANIMATION_CACHE) {
                    int childIndex;
                    try {
                        childIndex = getAndVerifyPreorderedIndex(childrenCount, i, isChildrenDrawingOrderEnabled);
                    } catch (IndexOutOfBoundsException e) {
                        childIndex = i;
                        if (this.mContext.getApplicationInfo().targetSdkVersion < 23) {
                            Log.w(TAG, "Bad getChildDrawingOrder while collecting assist @ " + i + " of " + childrenCount, e);
                            isChildrenDrawingOrderEnabled = DBG;
                            if (i > 0) {
                                int j;
                                int[] permutation = new int[childrenCount];
                                SparseBooleanArray usedIndices = new SparseBooleanArray();
                                for (j = PERSISTENT_NO_CACHE; j < i; j += PERSISTENT_ANIMATION_CACHE) {
                                    permutation[j] = getChildDrawingOrder(childrenCount, j);
                                    usedIndices.put(permutation[j], true);
                                }
                                int nextIndex = PERSISTENT_NO_CACHE;
                                for (j = i; j < childrenCount; j += PERSISTENT_ANIMATION_CACHE) {
                                    while (usedIndices.get(nextIndex, DBG)) {
                                        nextIndex += PERSISTENT_ANIMATION_CACHE;
                                    }
                                    permutation[j] = nextIndex;
                                    nextIndex += PERSISTENT_ANIMATION_CACHE;
                                }
                                preorderedList = new ArrayList(childrenCount);
                                for (j = PERSISTENT_NO_CACHE; j < childrenCount; j += PERSISTENT_ANIMATION_CACHE) {
                                    preorderedList.add(children[permutation[j]]);
                                }
                            }
                        } else {
                            throw e;
                        }
                    }
                    getAndVerifyPreorderedView(preorderedList, children, childIndex).dispatchProvideStructure(structure.newChild(i));
                }
                if (preorderedList != null) {
                    preorderedList.clear();
                }
            }
        }
    }

    private static View getAndVerifyPreorderedView(ArrayList<View> preorderedList, View[] children, int childIndex) {
        if (preorderedList == null) {
            return children[childIndex];
        }
        View child = (View) preorderedList.get(childIndex);
        if (child != null) {
            return child;
        }
        throw new RuntimeException("Invalid preorderedList contained null child at index " + childIndex);
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (getAccessibilityNodeProvider() == null && this.mAttachInfo != null) {
            ArrayList<View> childrenForAccessibility = this.mAttachInfo.mTempArrayList;
            synchronized (childrenForAccessibility) {
                childrenForAccessibility.clear();
                addChildrenForAccessibility(childrenForAccessibility);
                int childrenForAccessibilityCount = childrenForAccessibility.size();
                for (int i = PERSISTENT_NO_CACHE; i < childrenForAccessibilityCount; i += PERSISTENT_ANIMATION_CACHE) {
                    info.addChildUnchecked((View) childrenForAccessibility.get(i));
                }
                childrenForAccessibility.clear();
            }
        }
    }

    public CharSequence getAccessibilityClassName() {
        return ViewGroup.class.getName();
    }

    public void notifySubtreeAccessibilityStateChanged(View child, View source, int changeType) {
        if (getAccessibilityLiveRegion() != 0) {
            notifyViewAccessibilityStateChangedIfNeeded(changeType);
        } else if (this.mParent != null) {
            try {
                this.mParent.notifySubtreeAccessibilityStateChanged(this, source, changeType);
            } catch (AbstractMethodError e) {
                Log.e("View", this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
            }
        }
    }

    void resetSubtreeAccessibilityStateChanged() {
        super.resetSubtreeAccessibilityStateChanged();
        View[] children = this.mChildren;
        int childCount = this.mChildrenCount;
        for (int i = PERSISTENT_NO_CACHE; i < childCount; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].resetSubtreeAccessibilityStateChanged();
        }
    }

    int getNumChildrenForAccessibility() {
        int numChildrenForAccessibility = PERSISTENT_NO_CACHE;
        for (int i = PERSISTENT_NO_CACHE; i < getChildCount(); i += PERSISTENT_ANIMATION_CACHE) {
            View child = getChildAt(i);
            if (child.includeForAccessibility()) {
                numChildrenForAccessibility += PERSISTENT_ANIMATION_CACHE;
            } else if (child instanceof ViewGroup) {
                numChildrenForAccessibility += ((ViewGroup) child).getNumChildrenForAccessibility();
            }
        }
        return numChildrenForAccessibility;
    }

    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) {
        return DBG;
    }

    void dispatchDetachedFromWindow() {
        int i;
        cancelAndClearTouchTargets(null);
        exitHoverTargets();
        this.mLayoutCalledWhileSuppressed = DBG;
        this.mChildrenInterestedInDrag = null;
        this.mIsInterestedInDrag = DBG;
        if (this.mCurrentDragStartEvent != null) {
            this.mCurrentDragStartEvent.recycle();
            this.mCurrentDragStartEvent = null;
        }
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchDetachedFromWindow();
        }
        clearDisappearingChildren();
        int transientCount = this.mTransientViews == null ? PERSISTENT_NO_CACHE : this.mTransientIndices.size();
        for (i = PERSISTENT_NO_CACHE; i < transientCount; i += PERSISTENT_ANIMATION_CACHE) {
            ((View) this.mTransientViews.get(i)).dispatchDetachedFromWindow();
        }
        super.dispatchDetachedFromWindow();
    }

    protected void internalSetPadding(int left, int top, int right, int bottom) {
        super.internalSetPadding(left, top, right, bottom);
        if ((((this.mPaddingLeft | this.mPaddingTop) | this.mPaddingRight) | this.mPaddingBottom) != 0) {
            this.mGroupFlags |= FLAG_PADDING_NOT_NULL;
        } else {
            this.mGroupFlags &= -33;
        }
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchSaveInstanceState(container);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View c = children[i];
            if ((c.mViewFlags & FLAG_SHOW_CONTEXT_MENU_WITH_COORDS) != FLAG_SHOW_CONTEXT_MENU_WITH_COORDS) {
                c.dispatchSaveInstanceState(container);
            }
        }
    }

    protected void dispatchFreezeSelfOnly(SparseArray<Parcelable> container) {
        super.dispatchSaveInstanceState(container);
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        super.dispatchRestoreInstanceState(container);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View c = children[i];
            if (!(c == null || (c.mViewFlags & FLAG_SHOW_CONTEXT_MENU_WITH_COORDS) == FLAG_SHOW_CONTEXT_MENU_WITH_COORDS)) {
                c.dispatchRestoreInstanceState(container);
            }
        }
    }

    protected void dispatchThawSelfOnly(SparseArray<Parcelable> container) {
        super.dispatchRestoreInstanceState(container);
    }

    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        if (enabled || (this.mPersistentDrawingCache & PERSISTENT_ALL_CACHES) != PERSISTENT_ALL_CACHES) {
            View[] children = this.mChildren;
            int count = this.mChildrenCount;
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                children[i].setDrawingCacheEnabled(enabled);
            }
        }
    }

    public Bitmap createSnapshot(Config quality, int backgroundColor, boolean skipChildren) {
        int i;
        View child;
        int count = this.mChildrenCount;
        int[] iArr = null;
        if (skipChildren) {
            iArr = new int[count];
            for (i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                child = getChildAt(i);
                iArr[i] = child.getVisibility();
                if (iArr[i] == 0) {
                    child.mViewFlags = (child.mViewFlags & -13) | FLAG_INVALIDATE_REQUIRED;
                }
            }
        }
        Bitmap b = super.createSnapshot(quality, backgroundColor, skipChildren);
        if (skipChildren) {
            for (i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                child = getChildAt(i);
                child.mViewFlags = (child.mViewFlags & -13) | (iArr[i] & ARRAY_INITIAL_CAPACITY);
            }
        }
        return b;
    }

    boolean isLayoutModeOptical() {
        return this.mLayoutMode == PERSISTENT_ANIMATION_CACHE ? true : DBG;
    }

    Insets computeOpticalInsets() {
        if (!isLayoutModeOptical()) {
            return Insets.NONE;
        }
        int left = PERSISTENT_NO_CACHE;
        int top = PERSISTENT_NO_CACHE;
        int right = PERSISTENT_NO_CACHE;
        int bottom = PERSISTENT_NO_CACHE;
        for (int i = PERSISTENT_NO_CACHE; i < this.mChildrenCount; i += PERSISTENT_ANIMATION_CACHE) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0) {
                Insets insets = child.getOpticalInsets();
                left = Math.max(left, insets.left);
                top = Math.max(top, insets.top);
                right = Math.max(right, insets.right);
                bottom = Math.max(bottom, insets.bottom);
            }
        }
        return Insets.of(left, top, right, bottom);
    }

    private static void fillRect(Canvas canvas, Paint paint, int x1, int y1, int x2, int y2) {
        if (x1 != x2 && y1 != y2) {
            int tmp;
            if (x1 > x2) {
                tmp = x1;
                x1 = x2;
                x2 = tmp;
            }
            if (y1 > y2) {
                tmp = y1;
                y1 = y2;
                y2 = tmp;
            }
            canvas.drawRect((float) x1, (float) y1, (float) x2, (float) y2, paint);
        }
    }

    private static int sign(int x) {
        return x >= 0 ? PERSISTENT_ANIMATION_CACHE : LAYOUT_MODE_UNDEFINED;
    }

    private static void drawCorner(Canvas c, Paint paint, int x1, int y1, int dx, int dy, int lw) {
        fillRect(c, paint, x1, y1, x1 + dx, y1 + (sign(dy) * lw));
        fillRect(c, paint, x1, y1, x1 + (sign(dx) * lw), y1 + dy);
    }

    private int dipsToPixels(int dips) {
        return (int) ((((float) dips) * getContext().getResources().getDisplayMetrics().density) + 0.5f);
    }

    private static void drawRectCorners(Canvas canvas, int x1, int y1, int x2, int y2, Paint paint, int lineLength, int lineWidth) {
        drawCorner(canvas, paint, x1, y1, lineLength, lineLength, lineWidth);
        drawCorner(canvas, paint, x1, y2, lineLength, -lineLength, lineWidth);
        drawCorner(canvas, paint, x2, y1, -lineLength, lineLength, lineWidth);
        drawCorner(canvas, paint, x2, y2, -lineLength, -lineLength, lineWidth);
    }

    private static void fillDifference(Canvas canvas, int x2, int y2, int x3, int y3, int dx1, int dy1, int dx2, int dy2, Paint paint) {
        int x1 = x2 - dx1;
        int x4 = x3 + dx2;
        int y4 = y3 + dy2;
        fillRect(canvas, paint, x1, y2 - dy1, x4, y2);
        fillRect(canvas, paint, x1, y2, x2, y3);
        fillRect(canvas, paint, x3, y2, x4, y3);
        fillRect(canvas, paint, x1, y3, x4, y4);
    }

    protected void onDebugDrawMargins(Canvas canvas, Paint paint) {
        for (int i = PERSISTENT_NO_CACHE; i < getChildCount(); i += PERSISTENT_ANIMATION_CACHE) {
            View c = getChildAt(i);
            c.getLayoutParams().onDebugDraw(c, canvas, paint);
        }
    }

    protected void onDebugDraw(Canvas canvas) {
        int i;
        Paint paint = getDebugPaint();
        paint.setColor(Menu.CATEGORY_MASK);
        paint.setStyle(Style.STROKE);
        for (i = PERSISTENT_NO_CACHE; i < getChildCount(); i += PERSISTENT_ANIMATION_CACHE) {
            View c = getChildAt(i);
            if (c.getVisibility() != FLAG_RUN_ANIMATION) {
                Insets insets = c.getOpticalInsets();
                drawRect(canvas, paint, insets.left + c.getLeft(), insets.top + c.getTop(), (c.getRight() - insets.right) + LAYOUT_MODE_UNDEFINED, (c.getBottom() - insets.bottom) + LAYOUT_MODE_UNDEFINED);
            }
        }
        paint.setColor(Color.argb(63, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, PERSISTENT_NO_CACHE, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE));
        paint.setStyle(Style.FILL);
        onDebugDrawMargins(canvas, paint);
        paint.setColor(Color.rgb(63, LogPower.MIME_TYPE, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE));
        paint.setStyle(Style.FILL);
        int lineLength = dipsToPixels(FLAG_RUN_ANIMATION);
        int lineWidth = dipsToPixels(PERSISTENT_ANIMATION_CACHE);
        for (i = PERSISTENT_NO_CACHE; i < getChildCount(); i += PERSISTENT_ANIMATION_CACHE) {
            c = getChildAt(i);
            if (c.getVisibility() != FLAG_RUN_ANIMATION) {
                drawRectCorners(canvas, c.getLeft(), c.getTop(), c.getRight(), c.getBottom(), paint, lineLength, lineWidth);
            }
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        int i;
        View child;
        boolean isChildrenDrawingOrderEnabled;
        boolean usingRenderNodeProperties = canvas.isRecordingFor(this.mRenderNode);
        int childrenCount = this.mChildrenCount;
        View[] children = this.mChildren;
        int flags = this.mGroupFlags;
        if ((flags & FLAG_RUN_ANIMATION) != 0 && canAnimate()) {
            if (isHardwareAccelerated()) {
            }
            for (i = PERSISTENT_NO_CACHE; i < childrenCount; i += PERSISTENT_ANIMATION_CACHE) {
                child = children[i];
                if (child != null) {
                    if ((child.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0) {
                        attachLayoutAnimationParameters(child, child.getLayoutParams(), i, childrenCount);
                        bindLayoutAnimation(child);
                    }
                }
            }
            LayoutAnimationController controller = this.mLayoutAnimationController;
            if (controller.willOverlap()) {
                this.mGroupFlags |= FLAG_OPTIMIZE_INVALIDATE;
            }
            controller.start();
            this.mGroupFlags &= -9;
            this.mGroupFlags &= -17;
            if (this.mAnimationListener != null) {
                this.mAnimationListener.onAnimationStart(controller.getAnimation());
            }
        }
        int clipSaveCount = PERSISTENT_NO_CACHE;
        boolean clipToPadding = (flags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK ? true : DBG;
        if (clipToPadding) {
            clipSaveCount = canvas.save();
            canvas.clipRect(this.mScrollX + this.mPaddingLeft, this.mScrollY + this.mPaddingTop, ((this.mScrollX + this.mRight) - this.mLeft) - this.mPaddingRight, ((this.mScrollY + this.mBottom) - this.mTop) - this.mPaddingBottom);
        }
        this.mPrivateFlags &= -65;
        this.mGroupFlags &= -5;
        boolean more = DBG;
        long drawingTime = getDrawingTime();
        if (usingRenderNodeProperties) {
            canvas.insertReorderBarrier();
        }
        int transientCount = this.mTransientIndices == null ? PERSISTENT_NO_CACHE : this.mTransientIndices.size();
        int transientIndex = transientCount != 0 ? PERSISTENT_NO_CACHE : LAYOUT_MODE_UNDEFINED;
        ArrayList<View> preorderedList = usingRenderNodeProperties ? null : buildOrderedChildList();
        if (preorderedList == null) {
            isChildrenDrawingOrderEnabled = isChildrenDrawingOrderEnabled();
        } else {
            isChildrenDrawingOrderEnabled = DBG;
        }
        for (i = PERSISTENT_NO_CACHE; i < childrenCount; i += PERSISTENT_ANIMATION_CACHE) {
            while (transientIndex >= 0) {
                if (((Integer) this.mTransientIndices.get(transientIndex)).intValue() != i) {
                    break;
                }
                View transientChild = (View) this.mTransientViews.get(transientIndex);
                if ((transientChild.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0 || transientChild.getAnimation() != null) {
                    more |= drawChild(canvas, transientChild, drawingTime);
                }
                transientIndex += PERSISTENT_ANIMATION_CACHE;
                if (transientIndex >= transientCount) {
                    transientIndex = LAYOUT_MODE_UNDEFINED;
                }
            }
            child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, isChildrenDrawingOrderEnabled));
            if ((child.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0 || child.getAnimation() != null) {
                more |= drawChild(canvas, child, drawingTime);
            }
        }
        while (transientIndex >= 0) {
            transientChild = (View) this.mTransientViews.get(transientIndex);
            if ((transientChild.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0 || transientChild.getAnimation() != null) {
                more |= drawChild(canvas, transientChild, drawingTime);
            }
            transientIndex += PERSISTENT_ANIMATION_CACHE;
            if (transientIndex >= transientCount) {
                break;
            }
        }
        if (preorderedList != null) {
            preorderedList.clear();
        }
        if (this.mDisappearingChildren != null) {
            ArrayList<View> disappearingChildren = this.mDisappearingChildren;
            for (i = disappearingChildren.size() + LAYOUT_MODE_UNDEFINED; i >= 0; i += LAYOUT_MODE_UNDEFINED) {
                more |= drawChild(canvas, (View) disappearingChildren.get(i), drawingTime);
            }
        }
        if (usingRenderNodeProperties) {
            canvas.insertInorderBarrier();
        }
        if (debugDraw()) {
            onDebugDraw(canvas);
        }
        if (clipToPadding) {
            canvas.restoreToCount(clipSaveCount);
        }
        flags = this.mGroupFlags;
        if ((flags & FLAG_INVALIDATE_REQUIRED) == FLAG_INVALIDATE_REQUIRED) {
            invalidate(true);
        }
        if ((flags & FLAG_ANIMATION_DONE) == 0 && (flags & FLAG_NOTIFY_ANIMATION_LISTENER) == 0) {
            if (this.mLayoutAnimationController.isDone() && !r22) {
                this.mGroupFlags |= FLAG_NOTIFY_ANIMATION_LISTENER;
                post(new Runnable() {
                    public void run() {
                        ViewGroup.this.notifyAnimationListener();
                    }
                });
            }
        }
    }

    public /* bridge */ /* synthetic */ ViewOverlay m8getOverlay() {
        return getOverlay();
    }

    public ViewGroupOverlay getOverlay() {
        if (this.mOverlay == null) {
            this.mOverlay = new ViewGroupOverlay(this.mContext, this);
        }
        return (ViewGroupOverlay) this.mOverlay;
    }

    protected int getChildDrawingOrder(int childCount, int i) {
        return i;
    }

    private boolean hasChildWithZ() {
        for (int i = PERSISTENT_NO_CACHE; i < this.mChildrenCount; i += PERSISTENT_ANIMATION_CACHE) {
            if (this.mChildren[i].getZ() != 0.0f) {
                return true;
            }
        }
        return DBG;
    }

    ArrayList<View> buildOrderedChildList() {
        int childrenCount = this.mChildrenCount;
        if (childrenCount <= PERSISTENT_ANIMATION_CACHE || !hasChildWithZ()) {
            return null;
        }
        if (this.mPreSortedChildren == null) {
            this.mPreSortedChildren = new ArrayList(childrenCount);
        } else {
            this.mPreSortedChildren.clear();
            this.mPreSortedChildren.ensureCapacity(childrenCount);
        }
        boolean customOrder = isChildrenDrawingOrderEnabled();
        for (int i = PERSISTENT_NO_CACHE; i < childrenCount; i += PERSISTENT_ANIMATION_CACHE) {
            View nextChild = this.mChildren[getAndVerifyPreorderedIndex(childrenCount, i, customOrder)];
            float currentZ = nextChild.getZ();
            int insertIndex = i;
            while (insertIndex > 0 && ((View) this.mPreSortedChildren.get(insertIndex + LAYOUT_MODE_UNDEFINED)).getZ() > currentZ) {
                insertIndex += LAYOUT_MODE_UNDEFINED;
            }
            this.mPreSortedChildren.add(insertIndex, nextChild);
        }
        return this.mPreSortedChildren;
    }

    private void notifyAnimationListener() {
        this.mGroupFlags &= -513;
        this.mGroupFlags |= FLAG_ANIMATION_DONE;
        if (this.mAnimationListener != null) {
            post(new Runnable() {
                public void run() {
                    ViewGroup.this.mAnimationListener.onAnimationEnd(ViewGroup.this.mLayoutAnimationController.getAnimation());
                }
            });
        }
        invalidate(true);
    }

    protected void dispatchGetDisplayList() {
        int i;
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = children[i];
            if ((child.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0 || child.getAnimation() != null) {
                recreateChildDisplayList(child);
            }
        }
        if (this.mOverlay != null) {
            recreateChildDisplayList(this.mOverlay.getOverlayView());
        }
        if (this.mDisappearingChildren != null) {
            ArrayList<View> disappearingChildren = this.mDisappearingChildren;
            int disappearingCount = disappearingChildren.size();
            for (i = PERSISTENT_NO_CACHE; i < disappearingCount; i += PERSISTENT_ANIMATION_CACHE) {
                recreateChildDisplayList((View) disappearingChildren.get(i));
            }
        }
    }

    private void recreateChildDisplayList(View child) {
        boolean z;
        if ((child.mPrivateFlags & RtlSpacingHelper.UNDEFINED) != 0) {
            z = true;
        } else {
            z = DBG;
        }
        child.mRecreateDisplayList = z;
        child.mPrivateFlags &= HwBootFail.STAGE_BOOT_SUCCESS;
        child.updateDisplayListIfDirty();
        child.mRecreateDisplayList = DBG;
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child != null) {
            return child.draw(canvas, this, drawingTime);
        }
        return DBG;
    }

    void getScrollIndicatorBounds(Rect out) {
        super.getScrollIndicatorBounds(out);
        if ((this.mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK ? true : DBG) {
            out.left += this.mPaddingLeft;
            out.right -= this.mPaddingRight;
            out.top += this.mPaddingTop;
            out.bottom -= this.mPaddingBottom;
        }
    }

    @ExportedProperty(category = "drawing")
    public boolean getClipChildren() {
        return (this.mGroupFlags & PERSISTENT_ANIMATION_CACHE) != 0 ? true : DBG;
    }

    public void setClipChildren(boolean clipChildren) {
        if (clipChildren != ((this.mGroupFlags & PERSISTENT_ANIMATION_CACHE) == PERSISTENT_ANIMATION_CACHE ? true : DBG)) {
            setBooleanFlag(PERSISTENT_ANIMATION_CACHE, clipChildren);
            for (int i = PERSISTENT_NO_CACHE; i < this.mChildrenCount; i += PERSISTENT_ANIMATION_CACHE) {
                View child = getChildAt(i);
                if (child.mRenderNode != null) {
                    child.mRenderNode.setClipToBounds(clipChildren);
                }
            }
            invalidate(true);
        }
    }

    public void setClipToPadding(boolean clipToPadding) {
        if (hasBooleanFlag(PERSISTENT_SCROLLING_CACHE) != clipToPadding) {
            setBooleanFlag(PERSISTENT_SCROLLING_CACHE, clipToPadding);
            invalidate(true);
        }
    }

    @ExportedProperty(category = "drawing")
    public boolean getClipToPadding() {
        return hasBooleanFlag(PERSISTENT_SCROLLING_CACHE);
    }

    public void dispatchSetSelected(boolean selected) {
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].setSelected(selected);
        }
    }

    public void dispatchSetActivated(boolean activated) {
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].setActivated(activated);
        }
    }

    protected void dispatchSetPressed(boolean pressed) {
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = children[i];
            if (!(pressed && (child.isClickable() || child.isLongClickable()))) {
                child.setPressed(pressed);
            }
        }
    }

    public void dispatchDrawableHotspotChanged(float x, float y) {
        int count = this.mChildrenCount;
        if (count != 0) {
            View[] children = this.mChildren;
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                View child = children[i];
                boolean nonActionable = (child.isClickable() || child.isLongClickable()) ? DBG : true;
                boolean duplicatesState = (child.mViewFlags & FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW) != 0 ? true : DBG;
                if (nonActionable || duplicatesState) {
                    float[] point = getTempPoint();
                    point[PERSISTENT_NO_CACHE] = x;
                    point[PERSISTENT_ANIMATION_CACHE] = y;
                    transformPointToViewLocal(point, child);
                    child.drawableHotspotChanged(point[PERSISTENT_NO_CACHE], point[PERSISTENT_ANIMATION_CACHE]);
                }
            }
        }
    }

    void dispatchCancelPendingInputEvents() {
        super.dispatchCancelPendingInputEvents();
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].dispatchCancelPendingInputEvents();
        }
    }

    protected void setStaticTransformationsEnabled(boolean enabled) {
        setBooleanFlag(FLAG_SUPPORT_STATIC_TRANSFORMATIONS, enabled);
    }

    protected boolean getChildStaticTransformation(View child, Transformation t) {
        return DBG;
    }

    Transformation getChildTransformation() {
        if (this.mChildTransformation == null) {
            this.mChildTransformation = new Transformation();
        }
        return this.mChildTransformation;
    }

    protected View findViewTraversal(int id) {
        if (id == this.mID) {
            return this;
        }
        View[] where = this.mChildren;
        int len = this.mChildrenCount;
        for (int i = PERSISTENT_NO_CACHE; i < len; i += PERSISTENT_ANIMATION_CACHE) {
            View v = where[i];
            if ((v.mPrivateFlags & FLAG_RUN_ANIMATION) == 0) {
                v = v.findViewById(id);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    protected View findViewWithTagTraversal(Object tag) {
        if (tag != null && tag.equals(this.mTag)) {
            return this;
        }
        View[] where = this.mChildren;
        int len = this.mChildrenCount;
        for (int i = PERSISTENT_NO_CACHE; i < len; i += PERSISTENT_ANIMATION_CACHE) {
            View v = where[i];
            if ((v.mPrivateFlags & FLAG_RUN_ANIMATION) == 0) {
                v = v.findViewWithTag(tag);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    protected View findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        if (predicate.apply(this)) {
            return this;
        }
        View[] where = this.mChildren;
        int len = this.mChildrenCount;
        for (int i = PERSISTENT_NO_CACHE; i < len; i += PERSISTENT_ANIMATION_CACHE) {
            View v = where[i];
            if (v != childToSkip && (v.mPrivateFlags & FLAG_RUN_ANIMATION) == 0) {
                v = v.findViewByPredicate(predicate);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    public void addTransientView(View view, int index) {
        if (index >= 0) {
            if (this.mTransientIndices == null) {
                this.mTransientIndices = new ArrayList();
                this.mTransientViews = new ArrayList();
            }
            int oldSize = this.mTransientIndices.size();
            if (oldSize > 0) {
                int insertionIndex = PERSISTENT_NO_CACHE;
                while (insertionIndex < oldSize && index >= ((Integer) this.mTransientIndices.get(insertionIndex)).intValue()) {
                    insertionIndex += PERSISTENT_ANIMATION_CACHE;
                }
                this.mTransientIndices.add(insertionIndex, Integer.valueOf(index));
                this.mTransientViews.add(insertionIndex, view);
            } else {
                this.mTransientIndices.add(Integer.valueOf(index));
                this.mTransientViews.add(view);
            }
            view.mParent = this;
            view.dispatchAttachedToWindow(this.mAttachInfo, this.mViewFlags & ARRAY_INITIAL_CAPACITY);
            invalidate(true);
        }
    }

    public void removeTransientView(View view) {
        if (this.mTransientViews != null) {
            int size = this.mTransientViews.size();
            for (int i = PERSISTENT_NO_CACHE; i < size; i += PERSISTENT_ANIMATION_CACHE) {
                if (view == this.mTransientViews.get(i)) {
                    this.mTransientViews.remove(i);
                    this.mTransientIndices.remove(i);
                    view.mParent = null;
                    view.dispatchDetachedFromWindow();
                    invalidate(true);
                    return;
                }
            }
        }
    }

    public int getTransientViewCount() {
        return this.mTransientIndices == null ? PERSISTENT_NO_CACHE : this.mTransientIndices.size();
    }

    public int getTransientViewIndex(int position) {
        if (position < 0 || this.mTransientIndices == null || position >= this.mTransientIndices.size()) {
            return LAYOUT_MODE_UNDEFINED;
        }
        return ((Integer) this.mTransientIndices.get(position)).intValue();
    }

    public View getTransientView(int position) {
        if (this.mTransientViews == null || position >= this.mTransientViews.size()) {
            return null;
        }
        return (View) this.mTransientViews.get(position);
    }

    public void addView(View child) {
        addView(child, (int) LAYOUT_MODE_UNDEFINED);
    }

    public void addView(View child, int index) {
        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = generateDefaultLayoutParams();
            if (params == null) {
                throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
            }
        }
        addView(child, index, params);
    }

    public void addView(View child, int width, int height) {
        LayoutParams params = generateDefaultLayoutParams();
        params.width = width;
        params.height = height;
        addView(child, (int) LAYOUT_MODE_UNDEFINED, params);
    }

    public void addView(View child, LayoutParams params) {
        addView(child, (int) LAYOUT_MODE_UNDEFINED, params);
    }

    public void addView(View child, int index, LayoutParams params) {
        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }
        requestLayout();
        invalidate(true);
        addViewInner(child, index, params, DBG);
    }

    public void updateViewLayout(View view, LayoutParams params) {
        if (!checkLayoutParams(params)) {
            throw new IllegalArgumentException("Invalid LayoutParams supplied to " + this);
        } else if (view.mParent != this) {
            throw new IllegalArgumentException("Given view not a child of " + this);
        } else {
            view.setLayoutParams(params);
        }
    }

    protected boolean checkLayoutParams(LayoutParams p) {
        return p != null ? true : DBG;
    }

    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        this.mOnHierarchyChangeListener = listener;
    }

    void dispatchViewAdded(View child) {
        onViewAdded(child);
        if (this.mOnHierarchyChangeListener != null) {
            this.mOnHierarchyChangeListener.onChildViewAdded(this, child);
        }
    }

    public void onViewAdded(View child) {
    }

    void dispatchViewRemoved(View child) {
        onViewRemoved(child);
        if (this.mOnHierarchyChangeListener != null) {
            this.mOnHierarchyChangeListener.onChildViewRemoved(this, child);
        }
    }

    public void onViewRemoved(View child) {
    }

    private void clearCachedLayoutMode() {
        if (!hasBooleanFlag(FLAG_LAYOUT_MODE_WAS_EXPLICITLY_SET)) {
            this.mLayoutMode = LAYOUT_MODE_UNDEFINED;
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        clearCachedLayoutMode();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearCachedLayoutMode();
    }

    protected boolean addViewInLayout(View child, int index, LayoutParams params) {
        return addViewInLayout(child, index, params, DBG);
    }

    protected boolean addViewInLayout(View child, int index, LayoutParams params, boolean preventRequestLayout) {
        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }
        child.mParent = null;
        addViewInner(child, index, params, preventRequestLayout);
        child.mPrivateFlags = (child.mPrivateFlags & -6291457) | FLAG_PADDING_NOT_NULL;
        return true;
    }

    protected void cleanupLayoutState(View child) {
        child.mPrivateFlags &= -4097;
    }

    private void addViewInner(View child, int index, LayoutParams params, boolean preventRequestLayout) {
        if (this.mTransition != null) {
            this.mTransition.cancel(PERSISTENT_ALL_CACHES);
        }
        if (child.getParent() != null) {
            throw new IllegalStateException("The specified child already has a parent. You must call removeView() on the child's parent first.");
        }
        if (this.mTransition != null) {
            this.mTransition.addChild(this, child);
        }
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params);
        }
        if (preventRequestLayout) {
            child.mLayoutParams = params;
        } else {
            child.setLayoutParams(params);
        }
        if (index < 0) {
            index = this.mChildrenCount;
        }
        addInArray(child, index);
        if (preventRequestLayout) {
            child.assignParent(this);
        } else {
            child.mParent = this;
        }
        if (child.hasFocus()) {
            requestChildFocus(child, child.findFocus());
        }
        AttachInfo ai = this.mAttachInfo;
        if (ai != null && (this.mGroupFlags & FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW) == 0) {
            boolean lastKeepOn = ai.mKeepScreenOn;
            ai.mKeepScreenOn = DBG;
            child.dispatchAttachedToWindow(this.mAttachInfo, this.mViewFlags & ARRAY_INITIAL_CAPACITY);
            if (ai.mKeepScreenOn) {
                needGlobalAttributesUpdate(true);
            }
            ai.mKeepScreenOn = lastKeepOn;
        }
        if (child.isLayoutDirectionInherited()) {
            child.resetRtlProperties();
        }
        dispatchViewAdded(child);
        if ((child.mViewFlags & FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW) == FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW) {
            this.mGroupFlags |= FLAG_NOTIFY_CHILDREN_ON_DRAWABLE_STATE_CHANGE;
        }
        if (child.hasTransientState()) {
            childHasTransientStateChanged(child, true);
        }
        if (child.getVisibility() != FLAG_RUN_ANIMATION) {
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
        if (this.mTransientIndices != null) {
            int transientCount = this.mTransientIndices.size();
            for (int i = PERSISTENT_NO_CACHE; i < transientCount; i += PERSISTENT_ANIMATION_CACHE) {
                int oldIndex = ((Integer) this.mTransientIndices.get(i)).intValue();
                if (index <= oldIndex) {
                    this.mTransientIndices.set(i, Integer.valueOf(oldIndex + PERSISTENT_ANIMATION_CACHE));
                }
            }
        }
        if (this.mCurrentDragStartEvent != null && child.getVisibility() == 0) {
            notifyChildOfDragStart(child);
        }
    }

    private void addInArray(View child, int index) {
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        int size = children.length;
        if (index == count) {
            if (size == count) {
                this.mChildren = new View[(size + ARRAY_INITIAL_CAPACITY)];
                System.arraycopy(children, PERSISTENT_NO_CACHE, this.mChildren, PERSISTENT_NO_CACHE, size);
                children = this.mChildren;
            }
            int i = this.mChildrenCount;
            this.mChildrenCount = i + PERSISTENT_ANIMATION_CACHE;
            children[i] = child;
        } else if (index < count) {
            if (size == count) {
                this.mChildren = new View[(size + ARRAY_INITIAL_CAPACITY)];
                System.arraycopy(children, PERSISTENT_NO_CACHE, this.mChildren, PERSISTENT_NO_CACHE, index);
                System.arraycopy(children, index, this.mChildren, index + PERSISTENT_ANIMATION_CACHE, count - index);
                children = this.mChildren;
            } else {
                System.arraycopy(children, index, children, index + PERSISTENT_ANIMATION_CACHE, count - index);
            }
            children[index] = child;
            this.mChildrenCount += PERSISTENT_ANIMATION_CACHE;
            if (this.mLastTouchDownIndex >= index) {
                this.mLastTouchDownIndex += PERSISTENT_ANIMATION_CACHE;
            }
        } else {
            throw new IndexOutOfBoundsException("index=" + index + " count=" + count);
        }
    }

    private void removeFromArray(int index) {
        boolean z = DBG;
        View[] children = this.mChildren;
        if (this.mTransitioningViews != null) {
            z = this.mTransitioningViews.contains(children[index]);
        }
        if (!(z || children[index] == null)) {
            children[index].mParent = null;
        }
        int count = this.mChildrenCount;
        int i;
        if (index == count + LAYOUT_MODE_UNDEFINED) {
            i = this.mChildrenCount + LAYOUT_MODE_UNDEFINED;
            this.mChildrenCount = i;
            children[i] = null;
        } else if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException();
        } else {
            System.arraycopy(children, index + PERSISTENT_ANIMATION_CACHE, children, index, (count - index) + LAYOUT_MODE_UNDEFINED);
            i = this.mChildrenCount + LAYOUT_MODE_UNDEFINED;
            this.mChildrenCount = i;
            children[i] = null;
        }
        if (this.mLastTouchDownIndex == index) {
            this.mLastTouchDownTime = 0;
            this.mLastTouchDownIndex = LAYOUT_MODE_UNDEFINED;
        } else if (this.mLastTouchDownIndex > index) {
            this.mLastTouchDownIndex += LAYOUT_MODE_UNDEFINED;
        }
    }

    private void removeFromArray(int start, int count) {
        View[] children = this.mChildren;
        int childrenCount = this.mChildrenCount;
        start = Math.max(PERSISTENT_NO_CACHE, start);
        int end = Math.min(childrenCount, start + count);
        if (start != end) {
            int i;
            if (end == childrenCount) {
                for (i = start; i < end; i += PERSISTENT_ANIMATION_CACHE) {
                    children[i].mParent = null;
                    children[i] = null;
                }
            } else {
                for (i = start; i < end; i += PERSISTENT_ANIMATION_CACHE) {
                    children[i].mParent = null;
                }
                System.arraycopy(children, end, children, start, childrenCount - end);
                for (i = childrenCount - (end - start); i < childrenCount; i += PERSISTENT_ANIMATION_CACHE) {
                    children[i] = null;
                }
            }
            this.mChildrenCount -= end - start;
        }
    }

    private void bindLayoutAnimation(View child) {
        child.setAnimation(this.mLayoutAnimationController.getAnimationForView(child));
    }

    protected void attachLayoutAnimationParameters(View child, LayoutParams params, int index, int count) {
        AnimationParameters animationParams = params.layoutAnimationParameters;
        if (animationParams == null) {
            animationParams = new AnimationParameters();
            params.layoutAnimationParameters = animationParams;
        }
        animationParams.count = count;
        animationParams.index = index;
    }

    public void removeView(View view) {
        if (removeViewInternal(view)) {
            requestLayout();
            invalidate(true);
        }
    }

    public void removeViewInLayout(View view) {
        removeViewInternal(view);
    }

    public void removeViewsInLayout(int start, int count) {
        removeViewsInternal(start, count);
    }

    public void removeViewAt(int index) {
        removeViewInternal(index, getChildAt(index));
        requestLayout();
        invalidate(true);
    }

    public void removeViews(int start, int count) {
        removeViewsInternal(start, count);
        requestLayout();
        invalidate(true);
    }

    private boolean removeViewInternal(View view) {
        int index = indexOfChild(view);
        if (index < 0) {
            return DBG;
        }
        removeViewInternal(index, view);
        return true;
    }

    private void removeViewInternal(int index, View view) {
        if (this.mTransition != null) {
            this.mTransition.removeChild(this, view);
        }
        boolean clearChildFocus = DBG;
        if (view == this.mFocused) {
            view.unFocus(null);
            clearChildFocus = true;
        }
        view.clearAccessibilityFocus();
        cancelTouchTarget(view);
        cancelHoverTarget(view);
        if (view.getAnimation() != null || (this.mTransitioningViews != null && this.mTransitioningViews.contains(view))) {
            addDisappearingView(view);
        } else if (view.mAttachInfo != null) {
            view.dispatchDetachedFromWindow();
        }
        if (view.hasTransientState()) {
            childHasTransientStateChanged(view, DBG);
        }
        needGlobalAttributesUpdate(DBG);
        removeFromArray(index);
        if (clearChildFocus) {
            clearChildFocus(view);
            if (!rootViewRequestFocus()) {
                notifyGlobalFocusCleared(this);
            }
        }
        dispatchViewRemoved(view);
        if (view.getVisibility() != FLAG_RUN_ANIMATION) {
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
        int transientCount = this.mTransientIndices == null ? PERSISTENT_NO_CACHE : this.mTransientIndices.size();
        for (int i = PERSISTENT_NO_CACHE; i < transientCount; i += PERSISTENT_ANIMATION_CACHE) {
            int oldIndex = ((Integer) this.mTransientIndices.get(i)).intValue();
            if (index < oldIndex) {
                this.mTransientIndices.set(i, Integer.valueOf(oldIndex + LAYOUT_MODE_UNDEFINED));
            }
        }
        if (this.mCurrentDragStartEvent != null) {
            this.mChildrenInterestedInDrag.remove(view);
        }
    }

    public void setLayoutTransition(LayoutTransition transition) {
        if (this.mTransition != null) {
            LayoutTransition previousTransition = this.mTransition;
            previousTransition.cancel();
            previousTransition.removeTransitionListener(this.mLayoutTransitionListener);
        }
        this.mTransition = transition;
        if (this.mTransition != null) {
            this.mTransition.addTransitionListener(this.mLayoutTransitionListener);
        }
    }

    public LayoutTransition getLayoutTransition() {
        return this.mTransition;
    }

    private void removeViewsInternal(int start, int count) {
        int end = start + count;
        if (start < 0 || count < 0 || end > this.mChildrenCount) {
            throw new IndexOutOfBoundsException();
        }
        View focused = this.mFocused;
        boolean detach = this.mAttachInfo != null ? true : DBG;
        boolean clearChildFocus = DBG;
        View[] children = this.mChildren;
        for (int i = start; i < end; i += PERSISTENT_ANIMATION_CACHE) {
            View view = children[i];
            if (this.mTransition != null) {
                this.mTransition.removeChild(this, view);
            }
            if (view == focused) {
                view.unFocus(null);
                clearChildFocus = true;
            }
            view.clearAccessibilityFocus();
            cancelTouchTarget(view);
            cancelHoverTarget(view);
            if (view.getAnimation() != null || (this.mTransitioningViews != null && this.mTransitioningViews.contains(view))) {
                addDisappearingView(view);
            } else if (detach) {
                view.dispatchDetachedFromWindow();
            }
            if (view.hasTransientState()) {
                childHasTransientStateChanged(view, DBG);
            }
            needGlobalAttributesUpdate(DBG);
            dispatchViewRemoved(view);
        }
        removeFromArray(start, count);
        if (clearChildFocus) {
            clearChildFocus(focused);
            if (!rootViewRequestFocus()) {
                notifyGlobalFocusCleared(focused);
            }
        }
    }

    public void removeAllViews() {
        removeAllViewsInLayout();
        requestLayout();
        invalidate(true);
    }

    public void removeAllViewsInLayout() {
        int count = this.mChildrenCount;
        if (count > 0) {
            View[] children = this.mChildren;
            this.mChildrenCount = PERSISTENT_NO_CACHE;
            View focused = this.mFocused;
            boolean detach = this.mAttachInfo != null ? true : DBG;
            boolean clearChildFocus = DBG;
            needGlobalAttributesUpdate(DBG);
            for (int i = count + LAYOUT_MODE_UNDEFINED; i >= 0; i += LAYOUT_MODE_UNDEFINED) {
                View view = children[i];
                if (this.mTransition != null) {
                    this.mTransition.removeChild(this, view);
                }
                if (view == focused) {
                    view.unFocus(null);
                    clearChildFocus = true;
                }
                view.clearAccessibilityFocus();
                cancelTouchTarget(view);
                cancelHoverTarget(view);
                if (view.getAnimation() != null || (this.mTransitioningViews != null && this.mTransitioningViews.contains(view))) {
                    addDisappearingView(view);
                } else if (detach) {
                    view.dispatchDetachedFromWindow();
                }
                if (view.hasTransientState()) {
                    childHasTransientStateChanged(view, DBG);
                }
                dispatchViewRemoved(view);
                view.mParent = null;
                children[i] = null;
            }
            if (clearChildFocus) {
                clearChildFocus(focused);
                if (!rootViewRequestFocus()) {
                    notifyGlobalFocusCleared(focused);
                }
            }
        }
    }

    protected void removeDetachedView(View child, boolean animate) {
        if (this.mTransition != null) {
            this.mTransition.removeChild(this, child);
        }
        if (child == this.mFocused) {
            child.clearFocus();
        }
        child.clearAccessibilityFocus();
        cancelTouchTarget(child);
        cancelHoverTarget(child);
        if ((animate && child.getAnimation() != null) || (this.mTransitioningViews != null && this.mTransitioningViews.contains(child))) {
            addDisappearingView(child);
        } else if (child.mAttachInfo != null) {
            child.dispatchDetachedFromWindow();
        }
        if (child.hasTransientState()) {
            childHasTransientStateChanged(child, DBG);
        }
        dispatchViewRemoved(child);
    }

    protected void attachViewToParent(View child, int index, LayoutParams params) {
        boolean z = DBG;
        child.mLayoutParams = params;
        if (index < 0) {
            index = this.mChildrenCount;
        }
        addInArray(child, index);
        child.mParent = this;
        child.mPrivateFlags = (((child.mPrivateFlags & -6291457) & -32769) | FLAG_PADDING_NOT_NULL) | RtlSpacingHelper.UNDEFINED;
        this.mPrivateFlags |= RtlSpacingHelper.UNDEFINED;
        if (child.hasFocus()) {
            requestChildFocus(child, child.findFocus());
        }
        if (isAttachedToWindow() && getWindowVisibility() == 0) {
            z = isShown();
        }
        dispatchVisibilityAggregated(z);
    }

    protected void detachViewFromParent(View child) {
        removeFromArray(indexOfChild(child));
    }

    protected void detachViewFromParent(int index) {
        removeFromArray(index);
    }

    protected void detachViewsFromParent(int start, int count) {
        removeFromArray(start, count);
    }

    protected void detachAllViewsFromParent() {
        int count = this.mChildrenCount;
        if (count > 0) {
            View[] children = this.mChildren;
            this.mChildrenCount = PERSISTENT_NO_CACHE;
            for (int i = count + LAYOUT_MODE_UNDEFINED; i >= 0; i += LAYOUT_MODE_UNDEFINED) {
                children[i].mParent = null;
                children[i] = null;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void invalidateChild(View child, Rect dirty) {
        ViewParent parent = this;
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            boolean isOpaque;
            Matrix transformMatrix;
            boolean drawAnimation = (child.mPrivateFlags & FLAG_ANIMATION_CACHE) == FLAG_ANIMATION_CACHE ? true : DBG;
            Matrix childMatrix = child.getMatrix();
            if (child.isOpaque() && !drawAnimation && child.getAnimation() == null) {
                isOpaque = childMatrix.isIdentity();
            } else {
                isOpaque = DBG;
            }
            int opaqueFlag = isOpaque ? FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW : FLAG_SPLIT_MOTION_EVENTS;
            if (child.mLayerType != 0) {
                this.mPrivateFlags |= RtlSpacingHelper.UNDEFINED;
                this.mPrivateFlags &= -32769;
            }
            int[] location = attachInfo.mInvalidateChildLocation;
            location[PERSISTENT_NO_CACHE] = child.mLeft;
            location[PERSISTENT_ANIMATION_CACHE] = child.mTop;
            if (childMatrix.isIdentity()) {
            }
            RectF boundingRect = attachInfo.mTmpTransformRect;
            boundingRect.set(dirty);
            if ((this.mGroupFlags & FLAG_SUPPORT_STATIC_TRANSFORMATIONS) != 0) {
                Transformation t = attachInfo.mTmpTransformation;
                if (getChildStaticTransformation(child, t)) {
                    transformMatrix = attachInfo.mTmpMatrix;
                    transformMatrix.set(t.getMatrix());
                    if (!childMatrix.isIdentity()) {
                        transformMatrix.preConcat(childMatrix);
                    }
                } else {
                    transformMatrix = childMatrix;
                }
            } else {
                transformMatrix = childMatrix;
            }
            transformMatrix.mapRect(boundingRect);
            dirty.set((int) Math.floor((double) boundingRect.left), (int) Math.floor((double) boundingRect.top), (int) Math.ceil((double) boundingRect.right), (int) Math.ceil((double) boundingRect.bottom));
            do {
                View view = null;
                if (parent instanceof View) {
                    view = (View) parent;
                }
                if (drawAnimation) {
                    if (view != null) {
                        view.mPrivateFlags |= FLAG_ANIMATION_CACHE;
                    } else if (parent instanceof ViewRootImpl) {
                        ((ViewRootImpl) parent).mIsAnimating = true;
                    }
                }
                if (view != null) {
                    if ((view.mViewFlags & GL11.GL_CLIP_PLANE0) != 0 && view.getSolidColor() == 0) {
                        opaqueFlag = FLAG_SPLIT_MOTION_EVENTS;
                    }
                    if ((view.mPrivateFlags & 6291456) != FLAG_SPLIT_MOTION_EVENTS) {
                        view.mPrivateFlags = (view.mPrivateFlags & -6291457) | opaqueFlag;
                    }
                }
                parent = parent.invalidateChildInParent(location, dirty);
                if (view != null) {
                    Matrix m = view.getMatrix();
                    if (m.isIdentity()) {
                        continue;
                    } else {
                        boundingRect = attachInfo.mTmpTransformRect;
                        boundingRect.set(dirty);
                        m.mapRect(boundingRect);
                        dirty.set((int) Math.floor((double) boundingRect.left), (int) Math.floor((double) boundingRect.top), (int) Math.ceil((double) boundingRect.right), (int) Math.ceil((double) boundingRect.bottom));
                        continue;
                    }
                }
            } while (parent != null);
        }
    }

    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        if ((this.mPrivateFlags & FLAG_PADDING_NOT_NULL) != FLAG_PADDING_NOT_NULL && (this.mPrivateFlags & FLAG_CHILDREN_DRAWN_WITH_CACHE) != FLAG_CHILDREN_DRAWN_WITH_CACHE) {
            return null;
        }
        if ((this.mGroupFlags & LogPower.DISABLE_SENSOR) != FLAG_OPTIMIZE_INVALIDATE) {
            dirty.offset(location[PERSISTENT_NO_CACHE] - this.mScrollX, location[PERSISTENT_ANIMATION_CACHE] - this.mScrollY);
            if ((this.mGroupFlags & PERSISTENT_ANIMATION_CACHE) == 0) {
                dirty.union(PERSISTENT_NO_CACHE, PERSISTENT_NO_CACHE, this.mRight - this.mLeft, this.mBottom - this.mTop);
            }
            int left = this.mLeft;
            int top = this.mTop;
            if ((this.mGroupFlags & PERSISTENT_ANIMATION_CACHE) == PERSISTENT_ANIMATION_CACHE && !dirty.intersect(PERSISTENT_NO_CACHE, PERSISTENT_NO_CACHE, this.mRight - left, this.mBottom - top)) {
                dirty.setEmpty();
            }
            this.mPrivateFlags &= -32769;
            location[PERSISTENT_NO_CACHE] = left;
            location[PERSISTENT_ANIMATION_CACHE] = top;
            if (this.mLayerType != 0) {
                this.mPrivateFlags |= RtlSpacingHelper.UNDEFINED;
            }
            return this.mParent;
        }
        this.mPrivateFlags &= -32801;
        location[PERSISTENT_NO_CACHE] = this.mLeft;
        location[PERSISTENT_ANIMATION_CACHE] = this.mTop;
        if ((this.mGroupFlags & PERSISTENT_ANIMATION_CACHE) == PERSISTENT_ANIMATION_CACHE) {
            dirty.set(PERSISTENT_NO_CACHE, PERSISTENT_NO_CACHE, this.mRight - this.mLeft, this.mBottom - this.mTop);
        } else {
            dirty.union(PERSISTENT_NO_CACHE, PERSISTENT_NO_CACHE, this.mRight - this.mLeft, this.mBottom - this.mTop);
        }
        if (this.mLayerType != 0) {
            this.mPrivateFlags |= RtlSpacingHelper.UNDEFINED;
        }
        return this.mParent;
    }

    public boolean damageChildDeferred(View child) {
        ViewParent parent = getParent();
        while (parent != null) {
            if (parent instanceof ViewGroup) {
                parent = parent.getParent();
            } else if (parent instanceof ViewRootImpl) {
                ((ViewRootImpl) parent).invalidate();
                return true;
            } else {
                parent = null;
            }
        }
        return DBG;
    }

    public void damageChild(View child, Rect dirty) {
        if (!damageChildDeferred(child)) {
            ViewParent parent = this;
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo != null) {
                int left = child.mLeft;
                int top = child.mTop;
                if (!child.getMatrix().isIdentity()) {
                    child.transformRect(dirty);
                }
                do {
                    if (parent instanceof ViewGroup) {
                        ViewGroup parentVG = (ViewGroup) parent;
                        if (parentVG.mLayerType != 0) {
                            parentVG.invalidate();
                            parent = null;
                            continue;
                        } else {
                            parent = parentVG.damageChildInParent(left, top, dirty);
                            left = parentVG.mLeft;
                            top = parentVG.mTop;
                            continue;
                        }
                    } else {
                        int[] location = attachInfo.mInvalidateChildLocation;
                        location[PERSISTENT_NO_CACHE] = left;
                        location[PERSISTENT_ANIMATION_CACHE] = top;
                        parent = parent.invalidateChildInParent(location, dirty);
                        continue;
                    }
                } while (parent != null);
            }
        }
    }

    protected ViewParent damageChildInParent(int left, int top, Rect dirty) {
        if (!((this.mPrivateFlags & FLAG_PADDING_NOT_NULL) == 0 && (this.mPrivateFlags & FLAG_CHILDREN_DRAWN_WITH_CACHE) == 0)) {
            dirty.offset(left - this.mScrollX, top - this.mScrollY);
            if ((this.mGroupFlags & PERSISTENT_ANIMATION_CACHE) == 0) {
                dirty.union(PERSISTENT_NO_CACHE, PERSISTENT_NO_CACHE, this.mRight - this.mLeft, this.mBottom - this.mTop);
            }
            if ((this.mGroupFlags & PERSISTENT_ANIMATION_CACHE) == 0 || dirty.intersect(PERSISTENT_NO_CACHE, PERSISTENT_NO_CACHE, this.mRight - this.mLeft, this.mBottom - this.mTop)) {
                if (!getMatrix().isIdentity()) {
                    transformRect(dirty);
                }
                return this.mParent;
            }
        }
        return null;
    }

    public final void offsetDescendantRectToMyCoords(View descendant, Rect rect) {
        offsetRectBetweenParentAndChild(descendant, rect, true, DBG);
    }

    public final void offsetRectIntoDescendantCoords(View descendant, Rect rect) {
        offsetRectBetweenParentAndChild(descendant, rect, DBG, DBG);
    }

    void offsetRectBetweenParentAndChild(View descendant, Rect rect, boolean offsetFromChildToParent, boolean clipToBounds) {
        if (descendant != this) {
            View theParent = descendant.mParent;
            while (theParent != null && (theParent instanceof View) && theParent != this) {
                View p;
                if (offsetFromChildToParent) {
                    rect.offset(descendant.mLeft - descendant.mScrollX, descendant.mTop - descendant.mScrollY);
                    if (clipToBounds) {
                        p = theParent;
                        if (!rect.intersect(PERSISTENT_NO_CACHE, PERSISTENT_NO_CACHE, p.mRight - p.mLeft, p.mBottom - p.mTop)) {
                            rect.setEmpty();
                        }
                    }
                } else {
                    if (clipToBounds) {
                        p = theParent;
                        if (!rect.intersect(PERSISTENT_NO_CACHE, PERSISTENT_NO_CACHE, p.mRight - p.mLeft, p.mBottom - p.mTop)) {
                            rect.setEmpty();
                        }
                    }
                    rect.offset(descendant.mScrollX - descendant.mLeft, descendant.mScrollY - descendant.mTop);
                }
                descendant = theParent;
                theParent = descendant.mParent;
            }
            if (theParent == this) {
                if (offsetFromChildToParent) {
                    rect.offset(descendant.mLeft - descendant.mScrollX, descendant.mTop - descendant.mScrollY);
                } else {
                    rect.offset(descendant.mScrollX - descendant.mLeft, descendant.mScrollY - descendant.mTop);
                }
                return;
            }
            throw new IllegalArgumentException("parameter must be a descendant of this view");
        }
    }

    public void offsetChildrenTopAndBottom(int offset) {
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        boolean invalidate = DBG;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View v = children[i];
            v.mTop += offset;
            v.mBottom += offset;
            if (v.mRenderNode != null) {
                invalidate = true;
                v.mRenderNode.offsetTopAndBottom(offset);
            }
        }
        if (invalidate) {
            invalidateViewProperty(DBG, DBG);
        }
        notifySubtreeAccessibilityStateChangedIfNeeded();
    }

    public boolean getChildVisibleRect(View child, Rect r, Point offset) {
        return getChildVisibleRect(child, r, offset, DBG);
    }

    public boolean getChildVisibleRect(View child, Rect r, Point offset, boolean forceParentCheck) {
        RectF rect;
        if (this.mAttachInfo != null) {
            rect = this.mAttachInfo.mTmpTransformRect;
        } else {
            rect = new RectF();
        }
        rect.set(r);
        if (!child.hasIdentityMatrix()) {
            child.getMatrix().mapRect(rect);
        }
        int dx = child.mLeft - this.mScrollX;
        int dy = child.mTop - this.mScrollY;
        rect.offset((float) dx, (float) dy);
        if (offset != null) {
            if (!child.hasIdentityMatrix()) {
                float[] position;
                if (this.mAttachInfo != null) {
                    position = this.mAttachInfo.mTmpTransformLocation;
                } else {
                    position = new float[PERSISTENT_SCROLLING_CACHE];
                }
                position[PERSISTENT_NO_CACHE] = (float) offset.x;
                position[PERSISTENT_ANIMATION_CACHE] = (float) offset.y;
                child.getMatrix().mapPoints(position);
                offset.x = Math.round(position[PERSISTENT_NO_CACHE]);
                offset.y = Math.round(position[PERSISTENT_ANIMATION_CACHE]);
            }
            offset.x += dx;
            offset.y += dy;
        }
        int width = this.mRight - this.mLeft;
        int height = this.mBottom - this.mTop;
        boolean rectIsVisible = true;
        if (this.mParent == null || ((this.mParent instanceof ViewGroup) && ((ViewGroup) this.mParent).getClipChildren())) {
            rectIsVisible = rect.intersect(0.0f, 0.0f, (float) width, (float) height);
        }
        if ((forceParentCheck || r9) && (this.mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
            rectIsVisible = rect.intersect((float) this.mPaddingLeft, (float) this.mPaddingTop, (float) (width - this.mPaddingRight), (float) (height - this.mPaddingBottom));
        }
        if ((forceParentCheck || r9) && this.mClipBounds != null) {
            rectIsVisible = rect.intersect((float) this.mClipBounds.left, (float) this.mClipBounds.top, (float) this.mClipBounds.right, (float) this.mClipBounds.bottom);
        }
        r.set((int) Math.floor((double) rect.left), (int) Math.floor((double) rect.top), (int) Math.ceil((double) rect.right), (int) Math.ceil((double) rect.bottom));
        if ((!forceParentCheck && !rectIsVisible) || this.mParent == null) {
            return rectIsVisible;
        }
        if (this.mParent instanceof ViewGroup) {
            return ((ViewGroup) this.mParent).getChildVisibleRect(this, r, offset, forceParentCheck);
        }
        return this.mParent.getChildVisibleRect(this, r, offset);
    }

    public final void layout(int l, int t, int r, int b) {
        if (this.mSuppressLayout || (this.mTransition != null && this.mTransition.isChangingLayout())) {
            this.mLayoutCalledWhileSuppressed = true;
            return;
        }
        if (this.mTransition != null) {
            this.mTransition.layoutChange(this);
        }
        super.layout(l, t, r, b);
    }

    protected boolean canAnimate() {
        return this.mLayoutAnimationController != null ? true : DBG;
    }

    public void startLayoutAnimation() {
        if (this.mLayoutAnimationController != null) {
            this.mGroupFlags |= FLAG_RUN_ANIMATION;
            requestLayout();
        }
    }

    public void scheduleLayoutAnimation() {
        this.mGroupFlags |= FLAG_RUN_ANIMATION;
    }

    public void setLayoutAnimation(LayoutAnimationController controller) {
        this.mLayoutAnimationController = controller;
        if (this.mLayoutAnimationController != null) {
            this.mGroupFlags |= FLAG_RUN_ANIMATION;
        }
    }

    public LayoutAnimationController getLayoutAnimation() {
        return this.mLayoutAnimationController;
    }

    public boolean isAnimationCacheEnabled() {
        return (this.mGroupFlags & FLAG_ANIMATION_CACHE) == FLAG_ANIMATION_CACHE ? true : DBG;
    }

    public void setAnimationCacheEnabled(boolean enabled) {
        setBooleanFlag(FLAG_ANIMATION_CACHE, enabled);
    }

    public boolean isAlwaysDrawnWithCacheEnabled() {
        return (this.mGroupFlags & FLAG_ALWAYS_DRAWN_WITH_CACHE) == FLAG_ALWAYS_DRAWN_WITH_CACHE ? true : DBG;
    }

    public void setAlwaysDrawnWithCacheEnabled(boolean always) {
        setBooleanFlag(FLAG_ALWAYS_DRAWN_WITH_CACHE, always);
    }

    protected boolean isChildrenDrawnWithCacheEnabled() {
        return (this.mGroupFlags & FLAG_CHILDREN_DRAWN_WITH_CACHE) == FLAG_CHILDREN_DRAWN_WITH_CACHE ? true : DBG;
    }

    protected void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        setBooleanFlag(FLAG_CHILDREN_DRAWN_WITH_CACHE, enabled);
    }

    @ExportedProperty(category = "drawing")
    protected boolean isChildrenDrawingOrderEnabled() {
        return (this.mGroupFlags & FLAG_USE_CHILD_DRAWING_ORDER) == FLAG_USE_CHILD_DRAWING_ORDER ? true : DBG;
    }

    protected void setChildrenDrawingOrderEnabled(boolean enabled) {
        setBooleanFlag(FLAG_USE_CHILD_DRAWING_ORDER, enabled);
    }

    private boolean hasBooleanFlag(int flag) {
        return (this.mGroupFlags & flag) == flag ? true : DBG;
    }

    @ExportedProperty(category = "drawing", mapping = {@IntToString(from = 0, to = "NONE"), @IntToString(from = 1, to = "ANIMATION"), @IntToString(from = 2, to = "SCROLLING"), @IntToString(from = 3, to = "ALL")})
    public int getPersistentDrawingCache() {
        return this.mPersistentDrawingCache;
    }

    public void setPersistentDrawingCache(int drawingCacheToKeep) {
        this.mPersistentDrawingCache = drawingCacheToKeep & PERSISTENT_ALL_CACHES;
    }

    private void setLayoutMode(int layoutMode, boolean explicitly) {
        this.mLayoutMode = layoutMode;
        setBooleanFlag(FLAG_LAYOUT_MODE_WAS_EXPLICITLY_SET, explicitly);
    }

    void invalidateInheritedLayoutMode(int layoutModeOfRoot) {
        if (this.mLayoutMode != LAYOUT_MODE_UNDEFINED && this.mLayoutMode != layoutModeOfRoot && !hasBooleanFlag(FLAG_LAYOUT_MODE_WAS_EXPLICITLY_SET)) {
            setLayoutMode(LAYOUT_MODE_UNDEFINED, DBG);
            int N = getChildCount();
            for (int i = PERSISTENT_NO_CACHE; i < N; i += PERSISTENT_ANIMATION_CACHE) {
                getChildAt(i).invalidateInheritedLayoutMode(layoutModeOfRoot);
            }
        }
    }

    public int getLayoutMode() {
        if (this.mLayoutMode == LAYOUT_MODE_UNDEFINED) {
            setLayoutMode(this.mParent instanceof ViewGroup ? ((ViewGroup) this.mParent).getLayoutMode() : LAYOUT_MODE_DEFAULT, DBG);
        }
        return this.mLayoutMode;
    }

    public void setLayoutMode(int layoutMode) {
        if (this.mLayoutMode != layoutMode) {
            invalidateInheritedLayoutMode(layoutMode);
            setLayoutMode(layoutMode, layoutMode != LAYOUT_MODE_UNDEFINED ? true : DBG);
            requestLayout();
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return p;
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    protected void debug(int depth) {
        super.debug(depth);
        if (this.mFocused != null) {
            Log.d("View", View.debugIndent(depth) + "mFocused");
        }
        if (this.mChildrenCount != 0) {
            Log.d("View", View.debugIndent(depth) + "{");
        }
        int count = this.mChildrenCount;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            this.mChildren[i].debug(depth + PERSISTENT_ANIMATION_CACHE);
        }
        if (this.mChildrenCount != 0) {
            Log.d("View", View.debugIndent(depth) + "}");
        }
    }

    public int indexOfChild(View child) {
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            if (children[i] == child) {
                return i;
            }
        }
        return LAYOUT_MODE_UNDEFINED;
    }

    public int getChildCount() {
        return this.mChildrenCount;
    }

    public View getChildAt(int index) {
        if (index < 0 || index >= this.mChildrenCount) {
            return null;
        }
        return this.mChildren[index];
    }

    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
        int size = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = PERSISTENT_NO_CACHE; i < size; i += PERSISTENT_ANIMATION_CACHE) {
            View child = children[i];
            if ((child.mViewFlags & ARRAY_INITIAL_CAPACITY) != FLAG_RUN_ANIMATION) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        LayoutParams lp = child.getLayoutParams();
        child.measure(getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight, lp.width), getChildMeasureSpec(parentHeightMeasureSpec, this.mPaddingTop + this.mPaddingBottom, lp.height));
    }

    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        child.measure(getChildMeasureSpec(parentWidthMeasureSpec, (((this.mPaddingLeft + this.mPaddingRight) + lp.leftMargin) + lp.rightMargin) + widthUsed, lp.width), getChildMeasureSpec(parentHeightMeasureSpec, (((this.mPaddingTop + this.mPaddingBottom) + lp.topMargin) + lp.bottomMargin) + heightUsed, lp.height));
    }

    public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
        int specMode = MeasureSpec.getMode(spec);
        int size = Math.max(PERSISTENT_NO_CACHE, MeasureSpec.getSize(spec) - padding);
        int resultSize = PERSISTENT_NO_CACHE;
        int resultMode = PERSISTENT_NO_CACHE;
        switch (specMode) {
            case RtlSpacingHelper.UNDEFINED /*-2147483648*/:
                if (childDimension < 0) {
                    if (childDimension != LAYOUT_MODE_UNDEFINED) {
                        if (childDimension == -2) {
                            resultSize = size;
                            resultMode = RtlSpacingHelper.UNDEFINED;
                            break;
                        }
                    }
                    resultSize = size;
                    resultMode = RtlSpacingHelper.UNDEFINED;
                    break;
                }
                resultSize = childDimension;
                resultMode = EditorInfo.IME_FLAG_NO_ENTER_ACTION;
                break;
                break;
            case PERSISTENT_NO_CACHE /*0*/:
                if (childDimension < 0) {
                    if (childDimension != LAYOUT_MODE_UNDEFINED) {
                        if (childDimension == -2) {
                            resultSize = View.sUseZeroUnspecifiedMeasureSpec ? PERSISTENT_NO_CACHE : size;
                            resultMode = PERSISTENT_NO_CACHE;
                            break;
                        }
                    }
                    resultSize = View.sUseZeroUnspecifiedMeasureSpec ? PERSISTENT_NO_CACHE : size;
                    resultMode = PERSISTENT_NO_CACHE;
                    break;
                }
                resultSize = childDimension;
                resultMode = EditorInfo.IME_FLAG_NO_ENTER_ACTION;
                break;
                break;
            case EditorInfo.IME_FLAG_NO_ENTER_ACTION /*1073741824*/:
                if (childDimension < 0) {
                    if (childDimension != LAYOUT_MODE_UNDEFINED) {
                        if (childDimension == -2) {
                            resultSize = size;
                            resultMode = RtlSpacingHelper.UNDEFINED;
                            break;
                        }
                    }
                    resultSize = size;
                    resultMode = EditorInfo.IME_FLAG_NO_ENTER_ACTION;
                    break;
                }
                resultSize = childDimension;
                resultMode = EditorInfo.IME_FLAG_NO_ENTER_ACTION;
                break;
                break;
        }
        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }

    public void clearDisappearingChildren() {
        ArrayList<View> disappearingChildren = this.mDisappearingChildren;
        if (disappearingChildren != null) {
            int count = disappearingChildren.size();
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                View view = (View) disappearingChildren.get(i);
                if (view.mAttachInfo != null) {
                    view.dispatchDetachedFromWindow();
                }
                view.clearAnimation();
            }
            disappearingChildren.clear();
            invalidate();
        }
    }

    private void addDisappearingView(View v) {
        ArrayList<View> disappearingChildren = this.mDisappearingChildren;
        if (disappearingChildren == null) {
            disappearingChildren = new ArrayList();
            this.mDisappearingChildren = disappearingChildren;
        }
        disappearingChildren.add(v);
    }

    void finishAnimatingView(View view, Animation animation) {
        ArrayList<View> disappearingChildren = this.mDisappearingChildren;
        if (disappearingChildren != null && disappearingChildren.contains(view)) {
            disappearingChildren.remove(view);
            if (view.mAttachInfo != null) {
                view.dispatchDetachedFromWindow();
            }
            view.clearAnimation();
            this.mGroupFlags |= FLAG_INVALIDATE_REQUIRED;
        }
        if (!(animation == null || animation.getFillAfter())) {
            view.clearAnimation();
        }
        if ((view.mPrivateFlags & FLAG_NOTIFY_CHILDREN_ON_DRAWABLE_STATE_CHANGE) == FLAG_NOTIFY_CHILDREN_ON_DRAWABLE_STATE_CHANGE) {
            view.onAnimationEnd();
            view.mPrivateFlags &= -65537;
            this.mGroupFlags |= FLAG_INVALIDATE_REQUIRED;
        }
    }

    boolean isViewTransitioning(View view) {
        return this.mTransitioningViews != null ? this.mTransitioningViews.contains(view) : DBG;
    }

    public void startViewTransition(View view) {
        if (view.mParent == this) {
            if (this.mTransitioningViews == null) {
                this.mTransitioningViews = new ArrayList();
            }
            this.mTransitioningViews.add(view);
        }
    }

    public void endViewTransition(View view) {
        if (this.mTransitioningViews != null) {
            this.mTransitioningViews.remove(view);
            ArrayList<View> disappearingChildren = this.mDisappearingChildren;
            if (disappearingChildren != null && disappearingChildren.contains(view)) {
                disappearingChildren.remove(view);
                if (this.mVisibilityChangingChildren == null || !this.mVisibilityChangingChildren.contains(view)) {
                    if (view.mAttachInfo != null) {
                        view.dispatchDetachedFromWindow();
                    }
                    if (view.mParent != null) {
                        view.mParent = null;
                    }
                } else {
                    this.mVisibilityChangingChildren.remove(view);
                }
                invalidate();
            }
        }
    }

    public void suppressLayout(boolean suppress) {
        this.mSuppressLayout = suppress;
        if (!suppress && this.mLayoutCalledWhileSuppressed) {
            requestLayout();
            this.mLayoutCalledWhileSuppressed = DBG;
        }
    }

    public boolean isLayoutSuppressed() {
        return this.mSuppressLayout;
    }

    public boolean gatherTransparentRegion(Region region) {
        boolean meOpaque = DBG;
        if ((this.mPrivateFlags & FLAG_NOTIFY_ANIMATION_LISTENER) == 0) {
            meOpaque = true;
        }
        if (meOpaque && region == null) {
            return true;
        }
        super.gatherTransparentRegion(region);
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        boolean noneOfTheChildrenAreTransparent = true;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = children[i];
            if (((child.mViewFlags & ARRAY_INITIAL_CAPACITY) == 0 || child.getAnimation() != null) && !child.gatherTransparentRegion(region)) {
                noneOfTheChildrenAreTransparent = DBG;
            }
        }
        if (meOpaque) {
            noneOfTheChildrenAreTransparent = true;
        }
        return noneOfTheChildrenAreTransparent;
    }

    public void requestTransparentRegion(View child) {
        if (child != null) {
            child.mPrivateFlags |= FLAG_NOTIFY_ANIMATION_LISTENER;
            if (this.mParent != null) {
                this.mParent.requestTransparentRegion(this);
            }
        }
    }

    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        insets = super.dispatchApplyWindowInsets(insets);
        if (!insets.isConsumed()) {
            int count = getChildCount();
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                insets = getChildAt(i).dispatchApplyWindowInsets(insets);
                if (insets.isConsumed()) {
                    break;
                }
            }
        }
        return insets;
    }

    public AnimationListener getLayoutAnimationListener() {
        return this.mAnimationListener;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if ((this.mGroupFlags & FLAG_NOTIFY_CHILDREN_ON_DRAWABLE_STATE_CHANGE) == 0) {
            return;
        }
        if ((this.mGroupFlags & FLAG_ADD_STATES_FROM_CHILDREN) != 0) {
            throw new IllegalStateException("addStateFromChildren cannot be enabled if a child has duplicateParentState set to true");
        }
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = children[i];
            if ((child.mViewFlags & FLAG_PREVENT_DISPATCH_ATTACHED_TO_WINDOW) != 0) {
                child.refreshDrawableState();
            }
        }
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            children[i].jumpDrawablesToCurrentState();
        }
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        if ((this.mGroupFlags & FLAG_ADD_STATES_FROM_CHILDREN) == 0) {
            return super.onCreateDrawableState(extraSpace);
        }
        int i;
        int need = PERSISTENT_NO_CACHE;
        int n = getChildCount();
        for (i = PERSISTENT_NO_CACHE; i < n; i += PERSISTENT_ANIMATION_CACHE) {
            int[] childState = getChildAt(i).getDrawableState();
            if (childState != null) {
                need += childState.length;
            }
        }
        int[] state = super.onCreateDrawableState(extraSpace + need);
        for (i = PERSISTENT_NO_CACHE; i < n; i += PERSISTENT_ANIMATION_CACHE) {
            childState = getChildAt(i).getDrawableState();
            if (childState != null) {
                state = View.mergeDrawableStates(state, childState);
            }
        }
        return state;
    }

    public void setAddStatesFromChildren(boolean addsStates) {
        if (addsStates) {
            this.mGroupFlags |= FLAG_ADD_STATES_FROM_CHILDREN;
        } else {
            this.mGroupFlags &= -8193;
        }
        refreshDrawableState();
    }

    public boolean addStatesFromChildren() {
        return (this.mGroupFlags & FLAG_ADD_STATES_FROM_CHILDREN) != 0 ? true : DBG;
    }

    public void childDrawableStateChanged(View child) {
        if ((this.mGroupFlags & FLAG_ADD_STATES_FROM_CHILDREN) != 0) {
            refreshDrawableState();
        }
    }

    public void setLayoutAnimationListener(AnimationListener animationListener) {
        this.mAnimationListener = animationListener;
    }

    public void requestTransitionStart(LayoutTransition transition) {
        ViewRootImpl viewAncestor = getViewRootImpl();
        if (viewAncestor != null) {
            viewAncestor.requestTransitionStart(transition);
        }
    }

    public boolean resolveRtlPropertiesIfNeeded() {
        boolean result = super.resolveRtlPropertiesIfNeeded();
        if (result) {
            int count = getChildCount();
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                View child = getChildAt(i);
                if (child.isLayoutDirectionInherited()) {
                    child.resolveRtlPropertiesIfNeeded();
                }
            }
        }
        return result;
    }

    public boolean resolveLayoutDirection() {
        boolean result = super.resolveLayoutDirection();
        if (result) {
            int count = getChildCount();
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                View child = getChildAt(i);
                if (child.isLayoutDirectionInherited()) {
                    child.resolveLayoutDirection();
                }
            }
        }
        return result;
    }

    public boolean resolveTextDirection() {
        boolean result = super.resolveTextDirection();
        if (result) {
            int count = getChildCount();
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                View child = getChildAt(i);
                if (child.isTextDirectionInherited()) {
                    child.resolveTextDirection();
                }
            }
        }
        return result;
    }

    public boolean resolveTextAlignment() {
        boolean result = super.resolveTextAlignment();
        if (result) {
            int count = getChildCount();
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                View child = getChildAt(i);
                if (child.isTextAlignmentInherited()) {
                    child.resolveTextAlignment();
                }
            }
        }
        return result;
    }

    public void resolvePadding() {
        super.resolvePadding();
        int count = getChildCount();
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = getChildAt(i);
            if (child.isLayoutDirectionInherited() && !child.isPaddingResolved()) {
                child.resolvePadding();
            }
        }
    }

    protected void resolveDrawables() {
        super.resolveDrawables();
        int count = getChildCount();
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = getChildAt(i);
            if (child.isLayoutDirectionInherited() && !child.areDrawablesResolved()) {
                child.resolveDrawables();
            }
        }
    }

    public void resolveLayoutParams() {
        super.resolveLayoutParams();
        int count = getChildCount();
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            getChildAt(i).resolveLayoutParams();
        }
    }

    public void resetResolvedLayoutDirection() {
        super.resetResolvedLayoutDirection();
        int count = getChildCount();
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = getChildAt(i);
            if (child.isLayoutDirectionInherited()) {
                child.resetResolvedLayoutDirection();
            }
        }
    }

    public void resetResolvedTextDirection() {
        super.resetResolvedTextDirection();
        int count = getChildCount();
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = getChildAt(i);
            if (child.isTextDirectionInherited()) {
                child.resetResolvedTextDirection();
            }
        }
    }

    public void resetResolvedTextAlignment() {
        super.resetResolvedTextAlignment();
        int count = getChildCount();
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = getChildAt(i);
            if (child.isTextAlignmentInherited()) {
                child.resetResolvedTextAlignment();
            }
        }
    }

    public void resetResolvedPadding() {
        super.resetResolvedPadding();
        int count = getChildCount();
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = getChildAt(i);
            if (child.isLayoutDirectionInherited()) {
                child.resetResolvedPadding();
            }
        }
    }

    protected void resetResolvedDrawables() {
        super.resetResolvedDrawables();
        int count = getChildCount();
        for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
            View child = getChildAt(i);
            if (child.isLayoutDirectionInherited()) {
                child.resetResolvedDrawables();
            }
        }
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return DBG;
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        this.mNestedScrollAxes = axes;
    }

    public void onStopNestedScroll(View child) {
        stopNestedScroll();
        this.mNestedScrollAxes = PERSISTENT_NO_CACHE;
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null);
    }

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        dispatchNestedPreScroll(dx, dy, consumed, null);
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    public int getNestedScrollAxes() {
        return this.mNestedScrollAxes;
    }

    protected void onSetLayoutParams(View child, LayoutParams layoutParams) {
    }

    public void captureTransitioningViews(List<View> transitioningViews) {
        if (getVisibility() == 0) {
            if (isTransitionGroup()) {
                transitioningViews.add(this);
            } else {
                int count = getChildCount();
                for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                    getChildAt(i).captureTransitioningViews(transitioningViews);
                }
            }
        }
    }

    public void findNamedViews(Map<String, View> namedElements) {
        if (getVisibility() == 0 || this.mGhostView != null) {
            super.findNamedViews(namedElements);
            int count = getChildCount();
            for (int i = PERSISTENT_NO_CACHE; i < count; i += PERSISTENT_ANIMATION_CACHE) {
                getChildAt(i).findNamedViews(namedElements);
            }
        }
    }

    private static Paint getDebugPaint() {
        if (sDebugPaint == null) {
            sDebugPaint = new Paint();
            sDebugPaint.setAntiAlias(DBG);
        }
        return sDebugPaint;
    }

    private static void drawRect(Canvas canvas, Paint paint, int x1, int y1, int x2, int y2) {
        if (sDebugLines == null) {
            sDebugLines = new float[FLAG_ANIMATION_DONE];
        }
        sDebugLines[PERSISTENT_NO_CACHE] = (float) x1;
        sDebugLines[PERSISTENT_ANIMATION_CACHE] = (float) y1;
        sDebugLines[PERSISTENT_SCROLLING_CACHE] = (float) x2;
        sDebugLines[PERSISTENT_ALL_CACHES] = (float) y1;
        sDebugLines[FLAG_INVALIDATE_REQUIRED] = (float) x2;
        sDebugLines[5] = (float) y1;
        sDebugLines[6] = (float) x2;
        sDebugLines[7] = (float) y2;
        sDebugLines[FLAG_RUN_ANIMATION] = (float) x2;
        sDebugLines[9] = (float) y2;
        sDebugLines[10] = (float) x1;
        sDebugLines[11] = (float) y2;
        sDebugLines[ARRAY_INITIAL_CAPACITY] = (float) x1;
        sDebugLines[13] = (float) y2;
        sDebugLines[14] = (float) x1;
        sDebugLines[15] = (float) y1;
        canvas.drawLines(sDebugLines, paint);
    }

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("focus:descendantFocusability", getDescendantFocusability());
        encoder.addProperty("drawing:clipChildren", getClipChildren());
        encoder.addProperty("drawing:clipToPadding", getClipToPadding());
        encoder.addProperty("drawing:childrenDrawingOrderEnabled", isChildrenDrawingOrderEnabled());
        encoder.addProperty("drawing:persistentDrawingCache", getPersistentDrawingCache());
        int n = getChildCount();
        encoder.addProperty("meta:__childCount__", (short) n);
        for (int i = PERSISTENT_NO_CACHE; i < n; i += PERSISTENT_ANIMATION_CACHE) {
            encoder.addPropertyKey("meta:__child__" + i);
            getChildAt(i).encode(encoder);
        }
    }
}
