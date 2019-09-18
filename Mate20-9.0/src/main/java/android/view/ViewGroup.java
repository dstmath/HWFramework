package android.view;

import android.animation.LayoutTransition;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pools;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Transformation;
import android.view.autofill.Helper;
import com.android.internal.R;
import java.lang.annotation.RCUnownedThisRef;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class ViewGroup extends View implements ViewParent, ViewManager {
    private static final int ARRAY_CAPACITY_INCREMENT = 12;
    private static final int ARRAY_INITIAL_CAPACITY = 12;
    private static final int CHILD_LEFT_INDEX = 0;
    private static final int CHILD_TOP_INDEX = 1;
    protected static final int CLIP_TO_PADDING_MASK = 34;
    private static final boolean DBG = false;
    private static final int[] DESCENDANT_FOCUSABILITY_FLAGS = {131072, 262144, 393216};
    private static final int FLAG_ADD_STATES_FROM_CHILDREN = 8192;
    @Deprecated
    private static final int FLAG_ALWAYS_DRAWN_WITH_CACHE = 16384;
    @Deprecated
    private static final int FLAG_ANIMATION_CACHE = 64;
    static final int FLAG_ANIMATION_DONE = 16;
    @Deprecated
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
    @Deprecated
    public static final int PERSISTENT_ALL_CACHES = 3;
    @Deprecated
    public static final int PERSISTENT_ANIMATION_CACHE = 1;
    @Deprecated
    public static final int PERSISTENT_NO_CACHE = 0;
    @Deprecated
    public static final int PERSISTENT_SCROLLING_CACHE = 2;
    private static final ActionMode SENTINEL_ACTION_MODE = new ActionMode() {
        public void setTitle(CharSequence title) {
        }

        public void setTitle(int resId) {
        }

        public void setSubtitle(CharSequence subtitle) {
        }

        public void setSubtitle(int resId) {
        }

        public void setCustomView(View view) {
        }

        public void invalidate() {
        }

        public void finish() {
        }

        public Menu getMenu() {
            return null;
        }

        public CharSequence getTitle() {
            return null;
        }

        public CharSequence getSubtitle() {
            return null;
        }

        public View getCustomView() {
            return null;
        }

        public MenuInflater getMenuInflater() {
            return null;
        }
    };
    private static final String TAG = "ViewGroup";
    private static float[] sDebugLines;
    /* access modifiers changed from: private */
    public Animation.AnimationListener mAnimationListener;
    Paint mCachePaint;
    @ViewDebug.ExportedProperty(category = "layout")
    private int mChildCountWithTransientState;
    private Transformation mChildTransformation;
    int mChildUnhandledKeyListeners;
    private View[] mChildren;
    private int mChildrenCount;
    private HashSet<View> mChildrenInterestedInDrag;
    private View mCurrentDragChild;
    private DragEvent mCurrentDragStartEvent;
    private View mDefaultFocus;
    protected ArrayList<View> mDisappearingChildren;
    private HoverTarget mFirstHoverTarget;
    private TouchTarget mFirstTouchTarget;
    private View mFocused;
    View mFocusedInCluster;
    @ViewDebug.ExportedProperty(flagMapping = {@ViewDebug.FlagToString(equals = 1, mask = 1, name = "CLIP_CHILDREN"), @ViewDebug.FlagToString(equals = 2, mask = 2, name = "CLIP_TO_PADDING"), @ViewDebug.FlagToString(equals = 32, mask = 32, name = "PADDING_NOT_NULL")}, formatToHexString = true)
    protected int mGroupFlags;
    private boolean mHoveredSelf;
    RectF mInvalidateRegion;
    Transformation mInvalidationTransformation;
    private boolean mIsInterestedInDrag;
    @ViewDebug.ExportedProperty(category = "events")
    private int mLastTouchDownIndex;
    @ViewDebug.ExportedProperty(category = "events")
    private long mLastTouchDownTime;
    @ViewDebug.ExportedProperty(category = "events")
    private float mLastTouchDownX;
    @ViewDebug.ExportedProperty(category = "events")
    private float mLastTouchDownY;
    /* access modifiers changed from: private */
    public LayoutAnimationController mLayoutAnimationController;
    /* access modifiers changed from: private */
    public boolean mLayoutCalledWhileSuppressed;
    private int mLayoutMode;
    private LayoutTransition.TransitionListener mLayoutTransitionListener;
    private PointF mLocalPoint;
    private int mNestedScrollAxes;
    protected OnHierarchyChangeListener mOnHierarchyChangeListener;
    protected int mPersistentDrawingCache;
    private ArrayList<View> mPreSortedChildren;
    boolean mSuppressLayout;
    private float[] mTempPoint;
    private View mTooltipHoverTarget;
    private boolean mTooltipHoveredSelf;
    private List<Integer> mTransientIndices;
    private List<View> mTransientViews;
    private LayoutTransition mTransition;
    /* access modifiers changed from: private */
    public ArrayList<View> mTransitioningViews;
    private ArrayList<View> mVisibilityChangingChildren;

    static class ChildListForAccessibility {
        private static final int MAX_POOL_SIZE = 32;
        private static final Pools.SynchronizedPool<ChildListForAccessibility> sPool = new Pools.SynchronizedPool<>(32);
        private final ArrayList<View> mChildren = new ArrayList<>();
        private final ArrayList<ViewLocationHolder> mHolders = new ArrayList<>();

        ChildListForAccessibility() {
        }

        public static ChildListForAccessibility obtain(ViewGroup parent, boolean sort) {
            ChildListForAccessibility list = sPool.acquire();
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
            return this.mChildren.get(index);
        }

        private void init(ViewGroup parent, boolean sort) {
            ArrayList<View> children = this.mChildren;
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                children.add(parent.getChildAt(i));
            }
            if (sort) {
                ArrayList<ViewLocationHolder> holders = this.mHolders;
                for (int i2 = 0; i2 < childCount; i2++) {
                    holders.add(ViewLocationHolder.obtain(parent, children.get(i2)));
                }
                sort(holders);
                for (int i3 = 0; i3 < childCount; i3++) {
                    ViewLocationHolder holder = holders.get(i3);
                    children.set(i3, holder.mView);
                    holder.recycle();
                }
                holders.clear();
            }
        }

        private void sort(ArrayList<ViewLocationHolder> holders) {
            try {
                ViewLocationHolder.setComparisonStrategy(1);
                Collections.sort(holders);
            } catch (IllegalArgumentException e) {
                ViewLocationHolder.setComparisonStrategy(2);
                Collections.sort(holders);
            }
        }

        private void clear() {
            this.mChildren.clear();
        }
    }

    static class ChildListForAutoFill extends ArrayList<View> {
        private static final int MAX_POOL_SIZE = 32;
        private static final Pools.SimplePool<ChildListForAutoFill> sPool = new Pools.SimplePool<>(32);

        ChildListForAutoFill() {
        }

        public static ChildListForAutoFill obtain() {
            ChildListForAutoFill list = sPool.acquire();
            if (list == null) {
                return new ChildListForAutoFill();
            }
            return list;
        }

        public void recycle() {
            clear();
            sPool.release(this);
        }
    }

    private static final class HoverTarget {
        private static final int MAX_RECYCLED = 32;
        private static HoverTarget sRecycleBin;
        private static final Object sRecycleLock = new Object[0];
        private static int sRecycledCount;
        public View child;
        public HoverTarget next;

        private HoverTarget() {
        }

        public static HoverTarget obtain(View child2) {
            HoverTarget target;
            if (child2 != null) {
                synchronized (sRecycleLock) {
                    if (sRecycleBin == null) {
                        target = new HoverTarget();
                    } else {
                        target = sRecycleBin;
                        sRecycleBin = target.next;
                        sRecycledCount--;
                        target.next = null;
                    }
                }
                HoverTarget target2 = target;
                target2.child = child2;
                return target2;
            }
            throw new IllegalArgumentException("child must be non-null");
        }

        public void recycle() {
            if (this.child != null) {
                synchronized (sRecycleLock) {
                    if (sRecycledCount < 32) {
                        this.next = sRecycleBin;
                        sRecycleBin = this;
                        sRecycledCount++;
                    } else {
                        this.next = null;
                    }
                    this.child = null;
                }
                return;
            }
            throw new IllegalStateException("already recycled once");
        }
    }

    public static class LayoutParams {
        @Deprecated
        public static final int FILL_PARENT = -1;
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;
        @ViewDebug.ExportedProperty(category = "layout", mapping = {@ViewDebug.IntToString(from = -1, to = "MATCH_PARENT"), @ViewDebug.IntToString(from = -2, to = "WRAP_CONTENT")})
        public int height;
        public LayoutAnimationController.AnimationParameters layoutAnimationParameters;
        @ViewDebug.ExportedProperty(category = "layout", mapping = {@ViewDebug.IntToString(from = -1, to = "MATCH_PARENT"), @ViewDebug.IntToString(from = -2, to = "WRAP_CONTENT")})
        public int width;

        public LayoutParams(Context c, AttributeSet attrs) {
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ViewGroup_Layout);
            setBaseAttributes(a, 0, 1);
            a.recycle();
        }

        public LayoutParams(int width2, int height2) {
            this.width = width2;
            this.height = height2;
        }

        public LayoutParams(LayoutParams source) {
            this.width = source.width;
            this.height = source.height;
        }

        LayoutParams() {
        }

        /* access modifiers changed from: protected */
        public void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
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
            if (size == -2) {
                return "wrap-content";
            }
            if (size == -1) {
                return "match-parent";
            }
            return String.valueOf(size);
        }

        /* access modifiers changed from: package-private */
        public void encode(ViewHierarchyEncoder encoder) {
            encoder.beginObject(this);
            encodeProperties(encoder);
            encoder.endObject();
        }

        /* access modifiers changed from: protected */
        public void encodeProperties(ViewHierarchyEncoder encoder) {
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
        @ViewDebug.ExportedProperty(category = "layout")
        public int bottomMargin;
        @ViewDebug.ExportedProperty(category = "layout")
        private int endMargin;
        @ViewDebug.ExportedProperty(category = "layout")
        public int leftMargin;
        @ViewDebug.ExportedProperty(category = "layout", flagMapping = {@ViewDebug.FlagToString(equals = 3, mask = 3, name = "LAYOUT_DIRECTION"), @ViewDebug.FlagToString(equals = 4, mask = 4, name = "LEFT_MARGIN_UNDEFINED_MASK"), @ViewDebug.FlagToString(equals = 8, mask = 8, name = "RIGHT_MARGIN_UNDEFINED_MASK"), @ViewDebug.FlagToString(equals = 16, mask = 16, name = "RTL_COMPATIBILITY_MODE_MASK"), @ViewDebug.FlagToString(equals = 32, mask = 32, name = "NEED_RESOLUTION_MASK")}, formatToHexString = true)
        byte mMarginFlags;
        @ViewDebug.ExportedProperty(category = "layout")
        public int rightMargin;
        @ViewDebug.ExportedProperty(category = "layout")
        private int startMargin;
        @ViewDebug.ExportedProperty(category = "layout")
        public int topMargin;

        public MarginLayoutParams(Context c, AttributeSet attrs) {
            this.startMargin = Integer.MIN_VALUE;
            this.endMargin = Integer.MIN_VALUE;
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ViewGroup_MarginLayout);
            setBaseAttributes(a, 0, 1);
            int margin = a.getDimensionPixelSize(2, -1);
            if (margin >= 0) {
                this.leftMargin = margin;
                this.topMargin = margin;
                this.rightMargin = margin;
                this.bottomMargin = margin;
            } else {
                int horizontalMargin = a.getDimensionPixelSize(9, -1);
                int verticalMargin = a.getDimensionPixelSize(10, -1);
                if (horizontalMargin >= 0) {
                    this.leftMargin = horizontalMargin;
                    this.rightMargin = horizontalMargin;
                } else {
                    this.leftMargin = a.getDimensionPixelSize(3, Integer.MIN_VALUE);
                    if (this.leftMargin == Integer.MIN_VALUE) {
                        this.mMarginFlags = (byte) (this.mMarginFlags | 4);
                        this.leftMargin = 0;
                    }
                    this.rightMargin = a.getDimensionPixelSize(5, Integer.MIN_VALUE);
                    if (this.rightMargin == Integer.MIN_VALUE) {
                        this.mMarginFlags = (byte) (this.mMarginFlags | 8);
                        this.rightMargin = 0;
                    }
                }
                this.startMargin = a.getDimensionPixelSize(7, Integer.MIN_VALUE);
                this.endMargin = a.getDimensionPixelSize(8, Integer.MIN_VALUE);
                if (verticalMargin >= 0) {
                    this.topMargin = verticalMargin;
                    this.bottomMargin = verticalMargin;
                } else {
                    this.topMargin = a.getDimensionPixelSize(4, 0);
                    this.bottomMargin = a.getDimensionPixelSize(6, 0);
                }
                if (isMarginRelative()) {
                    this.mMarginFlags = (byte) (this.mMarginFlags | 32);
                }
            }
            boolean hasRtlSupport = c.getApplicationInfo().hasRtlSupport();
            if (c.getApplicationInfo().targetSdkVersion < 17 || !hasRtlSupport) {
                this.mMarginFlags = (byte) (this.mMarginFlags | WifiScanner.PnoSettings.PnoNetwork.FLAG_SAME_NETWORK);
            }
            this.mMarginFlags = (byte) (0 | this.mMarginFlags);
            a.recycle();
        }

        public MarginLayoutParams(int width, int height) {
            super(width, height);
            this.startMargin = Integer.MIN_VALUE;
            this.endMargin = Integer.MIN_VALUE;
            this.mMarginFlags = (byte) (this.mMarginFlags | 4);
            this.mMarginFlags = (byte) (this.mMarginFlags | 8);
            this.mMarginFlags = (byte) (this.mMarginFlags & -33);
            this.mMarginFlags = (byte) (this.mMarginFlags & -17);
        }

        public MarginLayoutParams(MarginLayoutParams source) {
            this.startMargin = Integer.MIN_VALUE;
            this.endMargin = Integer.MIN_VALUE;
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
            this.startMargin = Integer.MIN_VALUE;
            this.endMargin = Integer.MIN_VALUE;
            this.mMarginFlags = (byte) (this.mMarginFlags | 4);
            this.mMarginFlags = (byte) (this.mMarginFlags | 8);
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
                this.mMarginFlags = (byte) (this.mMarginFlags | 32);
            } else {
                this.mMarginFlags = (byte) (this.mMarginFlags & -33);
            }
        }

        public void setMarginsRelative(int start, int top, int end, int bottom) {
            this.startMargin = start;
            this.topMargin = top;
            this.endMargin = end;
            this.bottomMargin = bottom;
            this.mMarginFlags = (byte) (this.mMarginFlags | 32);
        }

        public void setMarginStart(int start) {
            this.startMargin = start;
            this.mMarginFlags = (byte) (this.mMarginFlags | 32);
        }

        public int getMarginStart() {
            if (this.startMargin != Integer.MIN_VALUE) {
                return this.startMargin;
            }
            if ((this.mMarginFlags & 32) == 32) {
                doResolveMargins();
            }
            if ((this.mMarginFlags & 3) != 1) {
                return this.leftMargin;
            }
            return this.rightMargin;
        }

        public void setMarginEnd(int end) {
            this.endMargin = end;
            this.mMarginFlags = (byte) (this.mMarginFlags | 32);
        }

        public int getMarginEnd() {
            if (this.endMargin != Integer.MIN_VALUE) {
                return this.endMargin;
            }
            if ((this.mMarginFlags & 32) == 32) {
                doResolveMargins();
            }
            if ((this.mMarginFlags & 3) != 1) {
                return this.rightMargin;
            }
            return this.leftMargin;
        }

        public boolean isMarginRelative() {
            return (this.startMargin == Integer.MIN_VALUE && this.endMargin == Integer.MIN_VALUE) ? false : true;
        }

        public void setLayoutDirection(int layoutDirection) {
            if ((layoutDirection == 0 || layoutDirection == 1) && layoutDirection != (this.mMarginFlags & 3)) {
                this.mMarginFlags = (byte) (this.mMarginFlags & -4);
                this.mMarginFlags = (byte) (this.mMarginFlags | (layoutDirection & 3));
                if (isMarginRelative()) {
                    this.mMarginFlags = (byte) (this.mMarginFlags | 32);
                } else {
                    this.mMarginFlags = (byte) (this.mMarginFlags & -33);
                }
            }
        }

        public int getLayoutDirection() {
            return this.mMarginFlags & 3;
        }

        public void resolveLayoutDirection(int layoutDirection) {
            setLayoutDirection(layoutDirection);
            if (isMarginRelative() && (this.mMarginFlags & 32) == 32) {
                doResolveMargins();
            }
        }

        private void doResolveMargins() {
            if ((this.mMarginFlags & WifiScanner.PnoSettings.PnoNetwork.FLAG_SAME_NETWORK) == 16) {
                if ((this.mMarginFlags & 4) == 4 && this.startMargin > Integer.MIN_VALUE) {
                    this.leftMargin = this.startMargin;
                }
                if ((this.mMarginFlags & 8) == 8 && this.endMargin > Integer.MIN_VALUE) {
                    this.rightMargin = this.endMargin;
                }
            } else {
                int i = 0;
                if ((this.mMarginFlags & 3) != 1) {
                    this.leftMargin = this.startMargin > Integer.MIN_VALUE ? this.startMargin : 0;
                    if (this.endMargin > Integer.MIN_VALUE) {
                        i = this.endMargin;
                    }
                    this.rightMargin = i;
                } else {
                    this.leftMargin = this.endMargin > Integer.MIN_VALUE ? this.endMargin : 0;
                    if (this.startMargin > Integer.MIN_VALUE) {
                        i = this.startMargin;
                    }
                    this.rightMargin = i;
                }
            }
            this.mMarginFlags = (byte) (this.mMarginFlags & -33);
        }

        public boolean isLayoutRtl() {
            return (this.mMarginFlags & 3) == 1;
        }

        public void onDebugDraw(View view, Canvas canvas, Paint paint) {
            Insets oi = View.isLayoutModeOptical(view.mParent) ? view.getOpticalInsets() : Insets.NONE;
            ViewGroup.fillDifference(canvas, view.getLeft() + oi.left, view.getTop() + oi.top, view.getRight() - oi.right, view.getBottom() - oi.bottom, this.leftMargin, this.topMargin, this.rightMargin, this.bottomMargin, paint);
        }

        /* access modifiers changed from: protected */
        public void encodeProperties(ViewHierarchyEncoder encoder) {
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
        private static final Object sRecycleLock = new Object[0];
        private static int sRecycledCount;
        public View child;
        public TouchTarget next;
        public int pointerIdBits;

        private TouchTarget() {
        }

        public static TouchTarget obtain(View child2, int pointerIdBits2) {
            TouchTarget target;
            if (child2 != null) {
                synchronized (sRecycleLock) {
                    if (sRecycleBin == null) {
                        target = new TouchTarget();
                    } else {
                        target = sRecycleBin;
                        sRecycleBin = target.next;
                        sRecycledCount--;
                        target.next = null;
                    }
                }
                TouchTarget target2 = target;
                target2.child = child2;
                target2.pointerIdBits = pointerIdBits2;
                return target2;
            }
            throw new IllegalArgumentException("child must be non-null");
        }

        public void recycle() {
            if (this.child != null) {
                synchronized (sRecycleLock) {
                    if (sRecycledCount < 32) {
                        this.next = sRecycleBin;
                        sRecycleBin = this;
                        sRecycledCount++;
                    } else {
                        this.next = null;
                    }
                    this.child = null;
                }
                return;
            }
            throw new IllegalStateException("already recycled once");
        }
    }

    static class ViewLocationHolder implements Comparable<ViewLocationHolder> {
        public static final int COMPARISON_STRATEGY_LOCATION = 2;
        public static final int COMPARISON_STRATEGY_STRIPE = 1;
        private static final int MAX_POOL_SIZE = 32;
        private static int sComparisonStrategy = 1;
        private static final Pools.SynchronizedPool<ViewLocationHolder> sPool = new Pools.SynchronizedPool<>(32);
        private int mLayoutDirection;
        private final Rect mLocation = new Rect();
        private ViewGroup mRoot;
        public View mView;

        ViewLocationHolder() {
        }

        public static ViewLocationHolder obtain(ViewGroup root, View view) {
            ViewLocationHolder holder = sPool.acquire();
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

        public int compareTo(ViewLocationHolder another) {
            if (another == null) {
                return 1;
            }
            int boundsResult = compareBoundsOfTree(this, another);
            if (boundsResult != 0) {
                return boundsResult;
            }
            return this.mView.getAccessibilityViewId() - another.mView.getAccessibilityViewId();
        }

        private static int compareBoundsOfTree(ViewLocationHolder holder1, ViewLocationHolder holder2) {
            if (sComparisonStrategy == 1) {
                if (holder1.mLocation.bottom - holder2.mLocation.top <= 0) {
                    return -1;
                }
                if (holder1.mLocation.top - holder2.mLocation.bottom >= 0) {
                    return 1;
                }
            }
            if (holder1.mLayoutDirection == 0) {
                int leftDifference = holder1.mLocation.left - holder2.mLocation.left;
                if (leftDifference != 0) {
                    return leftDifference;
                }
            } else {
                int rightDifference = holder1.mLocation.right - holder2.mLocation.right;
                if (rightDifference != 0) {
                    return -rightDifference;
                }
            }
            int topDifference = holder1.mLocation.top - holder2.mLocation.top;
            if (topDifference != 0) {
                return topDifference;
            }
            int heightDiference = holder1.mLocation.height() - holder2.mLocation.height();
            if (heightDiference != 0) {
                return -heightDiference;
            }
            int widthDifference = holder1.mLocation.width() - holder2.mLocation.width();
            if (widthDifference != 0) {
                return -widthDifference;
            }
            Rect view1Bounds = new Rect();
            Rect view2Bounds = new Rect();
            Rect tempRect = new Rect();
            holder1.mView.getBoundsOnScreen(view1Bounds, true);
            holder2.mView.getBoundsOnScreen(view2Bounds, true);
            View child1 = holder1.mView.findViewByPredicateTraversal(new Predicate(tempRect, view1Bounds) {
                private final /* synthetic */ Rect f$0;
                private final /* synthetic */ Rect f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final boolean test(Object obj) {
                    return ((View) obj).getBoundsOnScreen(this.f$0, true);
                }
            }, null);
            View child2 = holder2.mView.findViewByPredicateTraversal(new Predicate(tempRect, view2Bounds) {
                private final /* synthetic */ Rect f$0;
                private final /* synthetic */ Rect f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final boolean test(Object obj) {
                    return ((View) obj).getBoundsOnScreen(this.f$0, true);
                }
            }, null);
            if (child1 != null && child2 != null) {
                return compareBoundsOfTree(obtain(holder1.mRoot, child1), obtain(holder1.mRoot, child2));
            }
            if (child1 != null) {
                return 1;
            }
            if (child2 != null) {
                return -1;
            }
            return 0;
        }

        private void init(ViewGroup root, View view) {
            Rect viewLocation = this.mLocation;
            view.getDrawingRect(viewLocation);
            root.offsetDescendantRectToMyCoords(view, viewLocation);
            this.mView = view;
            this.mRoot = root;
            this.mLayoutDirection = root.getLayoutDirection();
        }

        private void clear() {
            this.mView = null;
            this.mRoot = null;
            this.mLocation.set(0, 0, 0, 0);
        }
    }

    /* access modifiers changed from: protected */
    public abstract void onLayout(boolean z, int i, int i2, int i3, int i4);

    public ViewGroup(Context context) {
        this(context, null);
    }

    public ViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mLastTouchDownIndex = -1;
        this.mLayoutMode = -1;
        this.mSuppressLayout = false;
        this.mLayoutCalledWhileSuppressed = false;
        this.mChildCountWithTransientState = 0;
        this.mTransientIndices = null;
        this.mTransientViews = null;
        this.mChildUnhandledKeyListeners = 0;
        this.mLayoutTransitionListener = new LayoutTransition.TransitionListener() {
            @RCUnownedThisRef
            public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                if (transitionType == 3) {
                    ViewGroup.this.startViewTransition(view);
                }
            }

            public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                if (ViewGroup.this.mLayoutCalledWhileSuppressed && !transition.isChangingLayout()) {
                    ViewGroup.this.requestLayout();
                    boolean unused = ViewGroup.this.mLayoutCalledWhileSuppressed = false;
                }
                if (transitionType == 3 && ViewGroup.this.mTransitioningViews != null) {
                    ViewGroup.this.endViewTransition(view);
                }
            }
        };
        initViewGroup();
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initViewGroup() {
        if (!debugDraw()) {
            setFlags(128, 128);
        }
        this.mGroupFlags |= 1;
        this.mGroupFlags |= 2;
        this.mGroupFlags |= 16;
        this.mGroupFlags |= 64;
        this.mGroupFlags |= 16384;
        if (this.mContext.getApplicationInfo().targetSdkVersion >= 11) {
            this.mGroupFlags |= 2097152;
        }
        setDescendantFocusability(131072);
        this.mChildren = new View[12];
        this.mChildrenCount = 0;
        this.mPersistentDrawingCache = 2;
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewGroup, defStyleAttr, defStyleRes);
        int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    setClipChildren(a.getBoolean(attr, true));
                    break;
                case 1:
                    setClipToPadding(a.getBoolean(attr, true));
                    break;
                case 2:
                    int id = a.getResourceId(attr, -1);
                    if (id <= 0) {
                        break;
                    } else {
                        setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this.mContext, id));
                        break;
                    }
                case 3:
                    setAnimationCacheEnabled(a.getBoolean(attr, true));
                    break;
                case 4:
                    setPersistentDrawingCache(a.getInt(attr, 2));
                    break;
                case 5:
                    setAlwaysDrawnWithCacheEnabled(a.getBoolean(attr, true));
                    break;
                case 6:
                    setAddStatesFromChildren(a.getBoolean(attr, false));
                    break;
                case 7:
                    setDescendantFocusability(DESCENDANT_FOCUSABILITY_FLAGS[a.getInt(attr, 0)]);
                    break;
                case 8:
                    setMotionEventSplittingEnabled(a.getBoolean(attr, false));
                    break;
                case 9:
                    if (!a.getBoolean(attr, false)) {
                        break;
                    } else {
                        setLayoutTransition(new LayoutTransition());
                        break;
                    }
                case 10:
                    setLayoutMode(a.getInt(attr, -1));
                    break;
                case 11:
                    setTransitionGroup(a.getBoolean(attr, false));
                    break;
                case 12:
                    setTouchscreenBlocksFocus(a.getBoolean(attr, false));
                    break;
            }
        }
        a.recycle();
    }

    @ViewDebug.ExportedProperty(category = "focus", mapping = {@ViewDebug.IntToString(from = 131072, to = "FOCUS_BEFORE_DESCENDANTS"), @ViewDebug.IntToString(from = 262144, to = "FOCUS_AFTER_DESCENDANTS"), @ViewDebug.IntToString(from = 393216, to = "FOCUS_BLOCK_DESCENDANTS")})
    public int getDescendantFocusability() {
        return this.mGroupFlags & 393216;
    }

    public void setDescendantFocusability(int focusability) {
        if (focusability == 131072 || focusability == 262144 || focusability == 393216) {
            this.mGroupFlags &= -393217;
            this.mGroupFlags |= 393216 & focusability;
            return;
        }
        throw new IllegalArgumentException("must be one of FOCUS_BEFORE_DESCENDANTS, FOCUS_AFTER_DESCENDANTS, FOCUS_BLOCK_DESCENDANTS");
    }

    /* access modifiers changed from: package-private */
    public void handleFocusGainInternal(int direction, Rect previouslyFocusedRect) {
        if (this.mFocused != null) {
            this.mFocused.unFocus(this);
            this.mFocused = null;
            this.mFocusedInCluster = null;
        }
        super.handleFocusGainInternal(direction, previouslyFocusedRect);
    }

    public void requestChildFocus(View child, View focused) {
        if (getDescendantFocusability() != 393216) {
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

    /* access modifiers changed from: package-private */
    public void setDefaultFocus(View child) {
        if (this.mDefaultFocus == null || !this.mDefaultFocus.isFocusedByDefault()) {
            this.mDefaultFocus = child;
            if (this.mParent instanceof ViewGroup) {
                ((ViewGroup) this.mParent).setDefaultFocus(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearDefaultFocus(View child) {
        if (this.mDefaultFocus == child || this.mDefaultFocus == null || !this.mDefaultFocus.isFocusedByDefault()) {
            this.mDefaultFocus = null;
            for (int i = 0; i < this.mChildrenCount; i++) {
                View sibling = this.mChildren[i];
                if (sibling.isFocusedByDefault()) {
                    this.mDefaultFocus = sibling;
                    return;
                }
                if (this.mDefaultFocus == null && sibling.hasDefaultFocus()) {
                    this.mDefaultFocus = sibling;
                }
            }
            if (this.mParent instanceof ViewGroup) {
                ((ViewGroup) this.mParent).clearDefaultFocus(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasDefaultFocus() {
        return this.mDefaultFocus != null || super.hasDefaultFocus();
    }

    /* access modifiers changed from: package-private */
    public void clearFocusedInCluster(View child) {
        if (this.mFocusedInCluster == child) {
            clearFocusedInCluster();
        }
    }

    /* access modifiers changed from: package-private */
    public void clearFocusedInCluster() {
        View top = findKeyboardNavigationCluster();
        ViewParent parent = this;
        do {
            ((ViewGroup) parent).mFocusedInCluster = null;
            if (parent != top) {
                parent = parent.getParent();
            } else {
                return;
            }
        } while (parent instanceof ViewGroup);
    }

    public void focusableViewAvailable(View v) {
        if (this.mParent != null && getDescendantFocusability() != 393216 && (this.mViewFlags & 12) == 0) {
            if (!isFocusableInTouchMode() && shouldBlockFocusForTouchscreen()) {
                return;
            }
            if ((!isFocused() || getDescendantFocusability() == 262144) && this.mParent != null) {
                this.mParent.focusableViewAvailable(v);
            }
        }
    }

    public boolean showContextMenuForChild(View originalView) {
        boolean z = false;
        if (isShowingContextMenuWithCoords()) {
            return false;
        }
        if (this.mParent != null && this.mParent.showContextMenuForChild(originalView)) {
            z = true;
        }
        return z;
    }

    public final boolean isShowingContextMenuWithCoords() {
        return (this.mGroupFlags & 536870912) != 0;
    }

    public boolean showContextMenuForChild(View originalView, float x, float y) {
        try {
            this.mGroupFlags |= 536870912;
            boolean z = true;
            if (showContextMenuForChild(originalView)) {
                return true;
            }
            this.mGroupFlags = -536870913 & this.mGroupFlags;
            if (this.mParent == null || !this.mParent.showContextMenuForChild(originalView, x, y)) {
                z = false;
            }
            return z;
        } finally {
            this.mGroupFlags = -536870913 & this.mGroupFlags;
        }
    }

    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        if ((this.mGroupFlags & 134217728) != 0) {
            return SENTINEL_ACTION_MODE;
        }
        try {
            this.mGroupFlags |= 268435456;
            return startActionModeForChild(originalView, callback, 0);
        } finally {
            this.mGroupFlags = -268435457 & this.mGroupFlags;
        }
    }

    /* JADX INFO: finally extract failed */
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
        if ((this.mGroupFlags & 268435456) == 0 && type == 0) {
            try {
                this.mGroupFlags |= 134217728;
                ActionMode mode = startActionModeForChild(originalView, callback);
                this.mGroupFlags = -134217729 & this.mGroupFlags;
                if (mode != SENTINEL_ACTION_MODE) {
                    return mode;
                }
            } catch (Throwable th) {
                this.mGroupFlags = -134217729 & this.mGroupFlags;
                throw th;
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
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).dispatchActivityResult(who, requestCode, resultCode, data)) {
                return true;
            }
        }
        return false;
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
        return false;
    }

    public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        ViewParent parent = this.mParent;
        if (parent != null && onRequestSendAccessibilityEvent(child, event)) {
            return parent.requestSendAccessibilityEvent(this, event);
        }
        return false;
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
            this.mChildCountWithTransientState++;
        } else {
            this.mChildCountWithTransientState--;
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
        return this.mChildCountWithTransientState > 0 || super.hasTransientState();
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return this.mFocused != null && this.mFocused.dispatchUnhandledMove(focused, direction);
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

    /* access modifiers changed from: package-private */
    public void unFocus(View focused) {
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

    /* access modifiers changed from: package-private */
    public View getDeepestFocusedChild() {
        View v = this;
        while (true) {
            View view = null;
            if (v == null) {
                return null;
            }
            if (v.isFocused()) {
                return v;
            }
            if (v instanceof ViewGroup) {
                view = ((ViewGroup) v).getFocusedChild();
            }
            v = view;
        }
    }

    public boolean hasFocus() {
        return ((this.mPrivateFlags & 2) == 0 && this.mFocused == null) ? false : true;
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

    /* access modifiers changed from: package-private */
    public boolean hasFocusable(boolean allowAutoFocus, boolean dispatchExplicit) {
        if ((this.mViewFlags & 12) != 0) {
            return false;
        }
        if ((allowAutoFocus || getFocusable() != 16) && isFocusable()) {
            return true;
        }
        if (getDescendantFocusability() != 393216) {
            return hasFocusableChild(dispatchExplicit);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasFocusableChild(boolean dispatchExplicit) {
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            View child = children[i];
            if ((dispatchExplicit && child.hasExplicitFocusable()) || (!dispatchExplicit && child.hasFocusable())) {
                return true;
            }
        }
        return false;
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        int focusableCount = views.size();
        int descendantFocusability = getDescendantFocusability();
        boolean blockFocusForTouchscreen = shouldBlockFocusForTouchscreen();
        boolean focusSelf = isFocusableInTouchMode() || !blockFocusForTouchscreen;
        if (descendantFocusability == 393216) {
            if (focusSelf) {
                super.addFocusables(views, direction, focusableMode);
            }
            return;
        }
        if (blockFocusForTouchscreen) {
            focusableMode |= 1;
        }
        if (descendantFocusability == 131072 && focusSelf) {
            super.addFocusables(views, direction, focusableMode);
        }
        View[] children = new View[this.mChildrenCount];
        int count = 0;
        for (int i = 0; i < this.mChildrenCount; i++) {
            View child = this.mChildren[i];
            if ((child.mViewFlags & 12) == 0) {
                children[count] = child;
                count++;
            }
        }
        FocusFinder.sort(children, 0, count, this, isLayoutRtl());
        for (int i2 = 0; i2 < count; i2++) {
            children[i2].addFocusables(views, direction, focusableMode);
        }
        if (descendantFocusability == 262144 && focusSelf && focusableCount == views.size()) {
            super.addFocusables(views, direction, focusableMode);
        }
    }

    /* JADX INFO: finally extract failed */
    public void addKeyboardNavigationClusters(Collection<View> views, int direction) {
        int focusableCount = views.size();
        int i = 0;
        if (isKeyboardNavigationCluster()) {
            boolean blockedFocus = getTouchscreenBlocksFocus();
            try {
                setTouchscreenBlocksFocusNoRefocus(false);
                super.addKeyboardNavigationClusters(views, direction);
                setTouchscreenBlocksFocusNoRefocus(blockedFocus);
            } catch (Throwable th) {
                setTouchscreenBlocksFocusNoRefocus(blockedFocus);
                throw th;
            }
        } else {
            super.addKeyboardNavigationClusters(views, direction);
        }
        if (focusableCount == views.size() && getDescendantFocusability() != 393216) {
            View[] visibleChildren = new View[this.mChildrenCount];
            int count = 0;
            for (int i2 = 0; i2 < this.mChildrenCount; i2++) {
                View child = this.mChildren[i2];
                if ((child.mViewFlags & 12) == 0) {
                    visibleChildren[count] = child;
                    count++;
                }
            }
            FocusFinder.sort(visibleChildren, 0, count, this, isLayoutRtl());
            while (true) {
                int i3 = i;
                if (i3 < count) {
                    visibleChildren[i3].addKeyboardNavigationClusters(views, direction);
                    i = i3 + 1;
                } else {
                    return;
                }
            }
        }
    }

    public void setTouchscreenBlocksFocus(boolean touchscreenBlocksFocus) {
        if (touchscreenBlocksFocus) {
            this.mGroupFlags |= 67108864;
            if (hasFocus() && !isKeyboardNavigationCluster() && !getDeepestFocusedChild().isFocusableInTouchMode()) {
                View newFocus = focusSearch(2);
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

    private void setTouchscreenBlocksFocusNoRefocus(boolean touchscreenBlocksFocus) {
        if (touchscreenBlocksFocus) {
            this.mGroupFlags |= 67108864;
        } else {
            this.mGroupFlags &= -67108865;
        }
    }

    @ViewDebug.ExportedProperty(category = "focus")
    public boolean getTouchscreenBlocksFocus() {
        return (this.mGroupFlags & 67108864) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldBlockFocusForTouchscreen() {
        return getTouchscreenBlocksFocus() && this.mContext.getPackageManager().hasSystemFeature("android.hardware.touchscreen") && (!isKeyboardNavigationCluster() || (!hasFocus() && findKeyboardNavigationCluster() == this));
    }

    public void findViewsWithText(ArrayList<View> outViews, CharSequence text, int flags) {
        super.findViewsWithText(outViews, text, flags);
        int childrenCount = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < childrenCount; i++) {
            View child = children[i];
            if ((child.mViewFlags & 12) == 0 && (child.mPrivateFlags & 8) == 0) {
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
        for (int i = 0; i < childrenCount; i++) {
            View foundView2 = children[i].findViewByAccessibilityIdTraversal(accessibilityId);
            if (foundView2 != null) {
                return foundView2;
            }
        }
        return null;
    }

    public View findViewByAutofillIdTraversal(int autofillId) {
        View foundView = super.findViewByAutofillIdTraversal(autofillId);
        if (foundView != null) {
            return foundView;
        }
        int childrenCount = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < childrenCount; i++) {
            View foundView2 = children[i].findViewByAutofillIdTraversal(autofillId);
            if (foundView2 != null) {
                return foundView2;
            }
        }
        return null;
    }

    public void dispatchWindowFocusChanged(boolean hasFocus) {
        super.dispatchWindowFocusChanged(hasFocus);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchWindowFocusChanged(hasFocus);
        }
    }

    public void addTouchables(ArrayList<View> views) {
        super.addTouchables(views);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            View child = children[i];
            if ((child.mViewFlags & 12) == 0) {
                child.addTouchables(views);
            }
        }
    }

    public void makeOptionalFitsSystemWindows() {
        super.makeOptionalFitsSystemWindows();
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].makeOptionalFitsSystemWindows();
        }
    }

    public void dispatchDisplayHint(int hint) {
        super.dispatchDisplayHint(hint);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchDisplayHint(hint);
        }
    }

    /* access modifiers changed from: protected */
    public void onChildVisibilityChanged(View child, int oldVisibility, int newVisibility) {
        if (this.mTransition != null) {
            if (newVisibility == 0) {
                this.mTransition.showChild(this, child, oldVisibility);
            } else {
                this.mTransition.hideChild(this, child, newVisibility);
                if (this.mTransitioningViews != null && this.mTransitioningViews.contains(child)) {
                    if (this.mVisibilityChangingChildren == null) {
                        this.mVisibilityChangingChildren = new ArrayList<>();
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

    /* access modifiers changed from: protected */
    public void dispatchVisibilityChanged(View changedView, int visibility) {
        super.dispatchVisibilityChanged(changedView, visibility);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchVisibilityChanged(changedView, visibility);
        }
    }

    public void dispatchWindowVisibilityChanged(int visibility) {
        super.dispatchWindowVisibilityChanged(visibility);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            if (children[i] != null) {
                children[i].dispatchWindowVisibilityChanged(visibility);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dispatchVisibilityAggregated(boolean isVisible) {
        boolean isVisible2 = super.dispatchVisibilityAggregated(isVisible);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            if (children[i].getVisibility() == 0) {
                children[i].dispatchVisibilityAggregated(isVisible2);
            }
        }
        return isVisible2;
    }

    public void dispatchConfigurationChanged(Configuration newConfig) {
        super.dispatchConfigurationChanged(newConfig);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            if (children[i] != null) {
                children[i].dispatchConfigurationChanged(newConfig);
            }
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

    /* access modifiers changed from: package-private */
    public void dispatchCollectViewAttributes(View.AttachInfo attachInfo, int visibility) {
        if ((visibility & 12) == 0) {
            super.dispatchCollectViewAttributes(attachInfo, visibility);
            int count = this.mChildrenCount;
            View[] children = this.mChildren;
            for (int i = 0; i < count; i++) {
                View child = children[i];
                child.dispatchCollectViewAttributes(attachInfo, (child.mViewFlags & 12) | visibility);
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

    /* access modifiers changed from: package-private */
    public boolean dispatchDragEnterExitInPreN(DragEvent event) {
        if (event.mAction == 6 && this.mCurrentDragChild != null) {
            this.mCurrentDragChild.dispatchDragEnterExitInPreN(event);
            this.mCurrentDragChild = null;
        }
        return this.mIsInterestedInDrag && super.dispatchDragEnterExitInPreN(event);
    }

    public boolean dispatchDragEvent(DragEvent event) {
        boolean eventWasConsumed;
        boolean retval = false;
        float tx = event.mX;
        float ty = event.mY;
        ClipData td = event.mClipData;
        PointF localPoint = getLocalPoint();
        switch (event.mAction) {
            case 1:
                this.mCurrentDragChild = null;
                this.mCurrentDragStartEvent = DragEvent.obtain(event);
                if (this.mChildrenInterestedInDrag == null) {
                    this.mChildrenInterestedInDrag = new HashSet<>();
                } else {
                    this.mChildrenInterestedInDrag.clear();
                }
                int count = this.mChildrenCount;
                View[] children = this.mChildren;
                for (int i = 0; i < count; i++) {
                    View child = children[i];
                    child.mPrivateFlags2 &= -4;
                    if (child.getVisibility() == 0 && notifyChildOfDragStart(children[i])) {
                        retval = true;
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
            case 2:
            case 3:
                View target = findFrontmostDroppableChildAt(event.mX, event.mY, localPoint);
                if (target != this.mCurrentDragChild) {
                    if (sCascadedDragDrop) {
                        int action = event.mAction;
                        event.mX = 0.0f;
                        event.mY = 0.0f;
                        event.mClipData = null;
                        if (this.mCurrentDragChild != null) {
                            event.mAction = 6;
                            this.mCurrentDragChild.dispatchDragEnterExitInPreN(event);
                        }
                        if (target != null) {
                            event.mAction = 5;
                            target.dispatchDragEnterExitInPreN(event);
                        }
                        event.mAction = action;
                        event.mX = tx;
                        event.mY = ty;
                        event.mClipData = td;
                    }
                    this.mCurrentDragChild = target;
                }
                if (target == null && this.mIsInterestedInDrag) {
                    target = this;
                }
                if (target == null) {
                    return false;
                }
                if (target == this) {
                    return super.dispatchDragEvent(event);
                }
                event.mX = localPoint.x;
                event.mY = localPoint.y;
                boolean retval2 = target.dispatchDragEvent(event);
                event.mX = tx;
                event.mY = ty;
                if (!this.mIsInterestedInDrag) {
                    return retval2;
                }
                if (sCascadedDragDrop) {
                    eventWasConsumed = retval2;
                } else {
                    eventWasConsumed = event.mEventHandlerWasCalled;
                }
                if (!eventWasConsumed) {
                    return super.dispatchDragEvent(event);
                }
                return retval2;
            case 4:
                HashSet<View> childrenInterestedInDrag = this.mChildrenInterestedInDrag;
                if (childrenInterestedInDrag != null) {
                    Iterator<View> it = childrenInterestedInDrag.iterator();
                    while (it.hasNext()) {
                        if (it.next().dispatchDragEvent(event)) {
                            retval = true;
                        }
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
                this.mIsInterestedInDrag = false;
                return retval;
            default:
                return false;
        }
    }

    /* access modifiers changed from: package-private */
    public View findFrontmostDroppableChildAt(float x, float y, PointF outLocalPoint) {
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = count - 1; i >= 0; i--) {
            View child = children[i];
            if (child.canAcceptDrag() && isTransformedTouchPointInView(x, y, child, outLocalPoint)) {
                return child;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean notifyChildOfDragStart(View child) {
        float tx = this.mCurrentDragStartEvent.mX;
        float ty = this.mCurrentDragStartEvent.mY;
        float[] point = getTempPoint();
        point[0] = tx;
        point[1] = ty;
        transformPointToViewLocal(point, child);
        this.mCurrentDragStartEvent.mX = point[0];
        this.mCurrentDragStartEvent.mY = point[1];
        boolean canAccept = child.dispatchDragEvent(this.mCurrentDragStartEvent);
        this.mCurrentDragStartEvent.mX = tx;
        this.mCurrentDragStartEvent.mY = ty;
        this.mCurrentDragStartEvent.mEventHandlerWasCalled = false;
        if (canAccept) {
            this.mChildrenInterestedInDrag.add(child);
            if (!child.canAcceptDrag()) {
                child.mPrivateFlags2 |= 1;
                child.refreshDrawableState();
            }
        }
        return canAccept;
    }

    public void dispatchWindowSystemUiVisiblityChanged(int visible) {
        super.dispatchWindowSystemUiVisiblityChanged(visible);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchWindowSystemUiVisiblityChanged(visible);
        }
    }

    public void dispatchSystemUiVisibilityChanged(int visible) {
        super.dispatchSystemUiVisibilityChanged(visible);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchSystemUiVisibilityChanged(visible);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateLocalSystemUiVisibility(int localValue, int localChanges) {
        boolean changed = super.updateLocalSystemUiVisibility(localValue, localChanges);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            changed |= children[i].updateLocalSystemUiVisibility(localValue, localChanges);
        }
        return changed;
    }

    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if ((this.mPrivateFlags & 18) == 18) {
            return super.dispatchKeyEventPreIme(event);
        }
        if (this.mFocused == null || (this.mFocused.mPrivateFlags & 16) != 16) {
            return false;
        }
        return this.mFocused.dispatchKeyEventPreIme(event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onKeyEvent(event, 1);
        }
        if ((this.mPrivateFlags & 18) == 18) {
            if (super.dispatchKeyEvent(event)) {
                return true;
            }
        } else if (this.mFocused != null && (this.mFocused.mPrivateFlags & 16) == 16 && this.mFocused.dispatchKeyEvent(event)) {
            return true;
        }
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, 1);
        }
        return false;
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        if ((this.mPrivateFlags & 18) == 18) {
            return super.dispatchKeyShortcutEvent(event);
        }
        if (this.mFocused == null || (this.mFocused.mPrivateFlags & 16) != 16) {
            return false;
        }
        return this.mFocused.dispatchKeyShortcutEvent(event);
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTrackballEvent(event, 1);
        }
        if ((this.mPrivateFlags & 18) == 18) {
            if (super.dispatchTrackballEvent(event)) {
                return true;
            }
        } else if (this.mFocused != null && (this.mFocused.mPrivateFlags & 16) == 16 && this.mFocused.dispatchTrackballEvent(event)) {
            return true;
        }
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, 1);
        }
        return false;
    }

    public boolean dispatchCapturedPointerEvent(MotionEvent event) {
        if ((this.mPrivateFlags & 18) == 18) {
            if (super.dispatchCapturedPointerEvent(event)) {
                return true;
            }
        } else if (this.mFocused != null && (this.mFocused.mPrivateFlags & 16) == 16 && this.mFocused.dispatchCapturedPointerEvent(event)) {
            return true;
        }
        return false;
    }

    public void dispatchPointerCaptureChanged(boolean hasCapture) {
        exitHoverTargets();
        super.dispatchPointerCaptureChanged(hasCapture);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchPointerCaptureChanged(hasCapture);
        }
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        if (isOnScrollbarThumb(x, y) || isDraggingScrollBar()) {
            return PointerIcon.getSystemIcon(this.mContext, 1000);
        }
        int childrenCount = this.mChildrenCount;
        if (childrenCount != 0) {
            ArrayList<View> preorderedList = buildOrderedChildList();
            boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
            View[] children = this.mChildren;
            for (int i = childrenCount - 1; i >= 0; i--) {
                View child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, customOrder));
                if (canViewReceivePointerEvents(child) && isTransformedTouchPointInView(x, y, child, null)) {
                    PointerIcon pointerIcon = dispatchResolvePointerIcon(event, pointerIndex, child);
                    if (pointerIcon != null) {
                        if (preorderedList != null) {
                            preorderedList.clear();
                        }
                        return pointerIcon;
                    }
                }
            }
            if (preorderedList != null) {
                preorderedList.clear();
            }
        }
        return super.onResolvePointerIcon(event, pointerIndex);
    }

    private PointerIcon dispatchResolvePointerIcon(MotionEvent event, int pointerIndex, View child) {
        if (!child.hasIdentityMatrix()) {
            MotionEvent transformedEvent = getTransformedMotionEvent(event, child);
            PointerIcon pointerIcon = child.onResolvePointerIcon(transformedEvent, pointerIndex);
            transformedEvent.recycle();
            return pointerIcon;
        }
        float offsetX = (float) (this.mScrollX - child.mLeft);
        float offsetY = (float) (this.mScrollY - child.mTop);
        event.offsetLocation(offsetX, offsetY);
        PointerIcon pointerIcon2 = child.onResolvePointerIcon(event, pointerIndex);
        event.offsetLocation(-offsetX, -offsetY);
        return pointerIcon2;
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

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00f7  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0147  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x019c  */
    public boolean dispatchHoverEvent(MotionEvent event) {
        MotionEvent eventNoHistory;
        boolean newHoveredSelf;
        boolean customOrder;
        HoverTarget firstOldHoverTarget;
        boolean wasHovered;
        MotionEvent motionEvent = event;
        int action = event.getAction();
        boolean interceptHover = onInterceptHoverEvent(event);
        motionEvent.setAction(action);
        MotionEvent eventNoHistory2 = motionEvent;
        boolean handled = false;
        HoverTarget hoverTarget = this.mFirstHoverTarget;
        this.mFirstHoverTarget = null;
        if (!interceptHover && action != 10) {
            float x = event.getX();
            float y = event.getY();
            int childrenCount = this.mChildrenCount;
            if (childrenCount != 0) {
                ArrayList<View> preorderedList = buildOrderedChildList();
                boolean customOrder2 = preorderedList == null && isChildrenDrawingOrderEnabled();
                View[] children = this.mChildren;
                int i = childrenCount - 1;
                eventNoHistory = eventNoHistory2;
                HoverTarget lastHoverTarget = null;
                while (true) {
                    int i2 = i;
                    if (i2 < 0) {
                        HoverTarget hoverTarget2 = hoverTarget;
                        boolean z = customOrder2;
                        break;
                    }
                    boolean customOrder3 = customOrder2;
                    int childIndex = getAndVerifyPreorderedIndex(childrenCount, i2, customOrder3);
                    boolean interceptHover2 = interceptHover;
                    View child = getAndVerifyPreorderedView(preorderedList, children, childIndex);
                    if (canViewReceivePointerEvents(child)) {
                        int i3 = childIndex;
                        if (!isTransformedTouchPointInView(x, y, child, null)) {
                            firstOldHoverTarget = hoverTarget;
                            customOrder = customOrder3;
                        } else {
                            HoverTarget hoverTarget3 = hoverTarget;
                            firstOldHoverTarget = hoverTarget;
                            HoverTarget predecessor = null;
                            while (true) {
                                if (hoverTarget == null) {
                                    hoverTarget = HoverTarget.obtain(child);
                                    wasHovered = false;
                                    customOrder = customOrder3;
                                    break;
                                }
                                customOrder = customOrder3;
                                if (hoverTarget.child == child) {
                                    if (predecessor != null) {
                                        predecessor.next = hoverTarget.next;
                                    } else {
                                        firstOldHoverTarget = hoverTarget.next;
                                    }
                                    hoverTarget.next = null;
                                    wasHovered = true;
                                } else {
                                    predecessor = hoverTarget;
                                    hoverTarget = hoverTarget.next;
                                    customOrder3 = customOrder;
                                }
                            }
                            boolean wasHovered2 = wasHovered;
                            if (lastHoverTarget != null) {
                                lastHoverTarget.next = hoverTarget;
                            } else {
                                this.mFirstHoverTarget = hoverTarget;
                            }
                            lastHoverTarget = hoverTarget;
                            if (action == 9) {
                                if (!wasHovered2) {
                                    handled |= dispatchTransformedGenericPointerEvent(motionEvent, child);
                                }
                            } else if (action == 7) {
                                if (!wasHovered2) {
                                    eventNoHistory = obtainMotionEventNoHistoryOrSelf(eventNoHistory);
                                    eventNoHistory.setAction(9);
                                    eventNoHistory.setAction(action);
                                    handled = handled | dispatchTransformedGenericPointerEvent(eventNoHistory, child) | dispatchTransformedGenericPointerEvent(eventNoHistory, child);
                                } else {
                                    handled |= dispatchTransformedGenericPointerEvent(motionEvent, child);
                                }
                            }
                            if (handled) {
                                hoverTarget = firstOldHoverTarget;
                                break;
                            }
                        }
                    } else {
                        firstOldHoverTarget = hoverTarget;
                        customOrder = customOrder3;
                    }
                    hoverTarget = firstOldHoverTarget;
                    i = i2 - 1;
                    interceptHover = interceptHover2;
                    customOrder2 = customOrder;
                }
                if (preorderedList != null) {
                    preorderedList.clear();
                }
                while (hoverTarget != null) {
                    View child2 = hoverTarget.child;
                    if (action == 10) {
                        handled = dispatchTransformedGenericPointerEvent(motionEvent, child2) | handled;
                    } else {
                        if (action == 7) {
                            boolean hoverExitPending = event.isHoverExitPending();
                            motionEvent.setHoverExitPending(true);
                            dispatchTransformedGenericPointerEvent(motionEvent, child2);
                            motionEvent.setHoverExitPending(hoverExitPending);
                        }
                        MotionEvent eventNoHistory3 = obtainMotionEventNoHistoryOrSelf(eventNoHistory);
                        eventNoHistory3.setAction(10);
                        dispatchTransformedGenericPointerEvent(eventNoHistory3, child2);
                        eventNoHistory3.setAction(action);
                        eventNoHistory = eventNoHistory3;
                    }
                    HoverTarget nextOldHoverTarget = hoverTarget.next;
                    hoverTarget.recycle();
                    hoverTarget = nextOldHoverTarget;
                }
                newHoveredSelf = handled && action != 10 && !event.isHoverExitPending();
                if (newHoveredSelf == this.mHoveredSelf) {
                    if (this.mHoveredSelf) {
                        if (action == 10) {
                            handled = super.dispatchHoverEvent(event) | handled;
                        } else {
                            if (action == 7) {
                                super.dispatchHoverEvent(event);
                            }
                            MotionEvent eventNoHistory4 = obtainMotionEventNoHistoryOrSelf(eventNoHistory);
                            eventNoHistory4.setAction(10);
                            super.dispatchHoverEvent(eventNoHistory4);
                            eventNoHistory4.setAction(action);
                            eventNoHistory = eventNoHistory4;
                        }
                        this.mHoveredSelf = false;
                    }
                    if (newHoveredSelf) {
                        if (action == 9) {
                            handled |= super.dispatchHoverEvent(event);
                            this.mHoveredSelf = true;
                        } else if (action == 7) {
                            eventNoHistory = obtainMotionEventNoHistoryOrSelf(eventNoHistory);
                            eventNoHistory.setAction(9);
                            boolean handled2 = super.dispatchHoverEvent(eventNoHistory) | handled;
                            eventNoHistory.setAction(action);
                            handled = super.dispatchHoverEvent(eventNoHistory) | handled2;
                            this.mHoveredSelf = true;
                        }
                    }
                } else if (newHoveredSelf) {
                    handled |= super.dispatchHoverEvent(event);
                }
                if (eventNoHistory != motionEvent) {
                    eventNoHistory.recycle();
                }
                return handled;
            }
        }
        eventNoHistory = eventNoHistory2;
        while (hoverTarget != null) {
        }
        if (handled) {
        }
        if (newHoveredSelf == this.mHoveredSelf) {
        }
        if (eventNoHistory != motionEvent) {
        }
        return handled;
    }

    private void exitHoverTargets() {
        if (this.mHoveredSelf || this.mFirstHoverTarget != null) {
            long now = SystemClock.uptimeMillis();
            MotionEvent event = MotionEvent.obtain(now, now, 10, 0.0f, 0.0f, 0);
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
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
                MotionEvent event = MotionEvent.obtain(now, now, 10, 0.0f, 0.0f, 0);
                event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                view.dispatchHoverEvent(event);
                event.recycle();
                return;
            }
            predecessor = target;
            target = next;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dispatchTooltipHoverEvent(MotionEvent event) {
        MotionEvent motionEvent = event;
        int action = event.getAction();
        if (action != 7) {
            switch (action) {
                case 10:
                    if (this.mTooltipHoverTarget == null) {
                        if (this.mTooltipHoveredSelf) {
                            super.dispatchTooltipHoverEvent(event);
                            this.mTooltipHoveredSelf = false;
                            break;
                        }
                    } else {
                        this.mTooltipHoverTarget.dispatchTooltipHoverEvent(motionEvent);
                        this.mTooltipHoverTarget = null;
                        break;
                    }
                    break;
            }
            return false;
        }
        View newTarget = null;
        int childrenCount = this.mChildrenCount;
        if (childrenCount != 0) {
            float x = event.getX();
            float y = event.getY();
            ArrayList<View> preorderedList = buildOrderedChildList();
            boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
            View[] children = this.mChildren;
            int i = childrenCount - 1;
            while (true) {
                if (i < 0) {
                    break;
                }
                View child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, customOrder));
                if (canViewReceivePointerEvents(child) && isTransformedTouchPointInView(x, y, child, null) && dispatchTooltipHoverEvent(motionEvent, child)) {
                    newTarget = child;
                    break;
                }
                i--;
            }
            if (preorderedList != null) {
                preorderedList.clear();
            }
        }
        if (this.mTooltipHoverTarget != newTarget) {
            if (this.mTooltipHoverTarget != null) {
                motionEvent.setAction(10);
                this.mTooltipHoverTarget.dispatchTooltipHoverEvent(motionEvent);
                motionEvent.setAction(action);
            }
            this.mTooltipHoverTarget = newTarget;
        }
        if (this.mTooltipHoverTarget != null) {
            if (this.mTooltipHoveredSelf) {
                this.mTooltipHoveredSelf = false;
                motionEvent.setAction(10);
                super.dispatchTooltipHoverEvent(event);
                motionEvent.setAction(action);
            }
            return true;
        }
        this.mTooltipHoveredSelf = super.dispatchTooltipHoverEvent(event);
        return this.mTooltipHoveredSelf;
    }

    private boolean dispatchTooltipHoverEvent(MotionEvent event, View child) {
        if (!child.hasIdentityMatrix()) {
            MotionEvent transformedEvent = getTransformedMotionEvent(event, child);
            boolean result = child.dispatchTooltipHoverEvent(transformedEvent);
            transformedEvent.recycle();
            return result;
        }
        float offsetX = (float) (this.mScrollX - child.mLeft);
        float offsetY = (float) (this.mScrollY - child.mTop);
        event.offsetLocation(offsetX, offsetY);
        boolean result2 = child.dispatchTooltipHoverEvent(event);
        event.offsetLocation(-offsetX, -offsetY);
        return result2;
    }

    private void exitTooltipHoverTargets() {
        if (this.mTooltipHoveredSelf || this.mTooltipHoverTarget != null) {
            long now = SystemClock.uptimeMillis();
            MotionEvent event = MotionEvent.obtain(now, now, 10, 0.0f, 0.0f, 0);
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            dispatchTooltipHoverEvent(event);
            event.recycle();
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasHoveredChild() {
        return this.mFirstHoverTarget != null;
    }

    public void addChildrenForAccessibility(ArrayList<View> outChildren) {
        if (getAccessibilityNodeProvider() == null) {
            ChildListForAccessibility children = ChildListForAccessibility.obtain(this, true);
            try {
                int childrenCount = children.getChildCount();
                for (int i = 0; i < childrenCount; i++) {
                    View child = children.getChildAt(i);
                    if ((child.mViewFlags & 12) == 0) {
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
        return false;
    }

    private static MotionEvent obtainMotionEventNoHistoryOrSelf(MotionEvent event) {
        if (event.getHistorySize() == 0) {
            return event;
        }
        return MotionEvent.obtainNoHistory(event);
    }

    /* access modifiers changed from: protected */
    public boolean dispatchGenericPointerEvent(MotionEvent event) {
        int childrenCount = this.mChildrenCount;
        if (childrenCount != 0) {
            float x = event.getX();
            float y = event.getY();
            ArrayList<View> preorderedList = buildOrderedChildList();
            boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
            View[] children = this.mChildren;
            int i = childrenCount - 1;
            while (i >= 0) {
                View child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, customOrder));
                if (!canViewReceivePointerEvents(child) || !isTransformedTouchPointInView(x, y, child, null) || !dispatchTransformedGenericPointerEvent(event, child)) {
                    i--;
                } else {
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

    /* access modifiers changed from: protected */
    public boolean dispatchGenericFocusedEvent(MotionEvent event) {
        if ((this.mPrivateFlags & 18) == 18) {
            return super.dispatchGenericFocusedEvent(event);
        }
        if (this.mFocused == null || (this.mFocused.mPrivateFlags & 16) != 16) {
            return false;
        }
        return this.mFocused.dispatchGenericMotionEvent(event);
    }

    private boolean dispatchTransformedGenericPointerEvent(MotionEvent event, View child) {
        if (!child.hasIdentityMatrix()) {
            MotionEvent transformedEvent = getTransformedMotionEvent(event, child);
            boolean handled = child.dispatchGenericMotionEvent(transformedEvent);
            transformedEvent.recycle();
            return handled;
        }
        float offsetX = (float) (this.mScrollX - child.mLeft);
        float offsetY = (float) (this.mScrollY - child.mTop);
        event.offsetLocation(offsetX, offsetY);
        boolean handled2 = child.dispatchGenericMotionEvent(event);
        event.offsetLocation(-offsetX, -offsetY);
        return handled2;
    }

    private MotionEvent getTransformedMotionEvent(MotionEvent event, View child) {
        MotionEvent transformedEvent = MotionEvent.obtain(event);
        transformedEvent.offsetLocation((float) (this.mScrollX - child.mLeft), (float) (this.mScrollY - child.mTop));
        if (!child.hasIdentityMatrix()) {
            transformedEvent.transform(child.getInverseMatrix());
        }
        return transformedEvent;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0133, code lost:
        r4 = false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x01e3  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x01ea  */
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean intercepted;
        boolean handled;
        boolean alreadyDispatchedToNewTouchTarget;
        boolean alreadyDispatchedToNewTouchTarget2;
        boolean handled2;
        int idBitsToAssign;
        boolean alreadyDispatchedToNewTouchTarget3;
        TouchTarget newTouchTarget;
        float y;
        float x;
        boolean customOrder;
        int i;
        View childWithAccessibilityFocus;
        boolean z;
        boolean intercepted2;
        MotionEvent motionEvent = ev;
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTouchEvent(motionEvent, 1);
        }
        boolean z2 = false;
        if (ev.isTargetAccessibilityFocus() && isAccessibilityFocusedViewOrHost()) {
            motionEvent.setTargetAccessibilityFocus(false);
        }
        boolean handled3 = false;
        if (onFilterTouchEventForSecurity(ev)) {
            int action = ev.getAction();
            int actionMasked = action & 255;
            if (actionMasked == 0) {
                cancelAndClearTouchTargets(ev);
                resetTouchState();
            }
            if (actionMasked == 0 || this.mFirstTouchTarget != null) {
                if (!((this.mGroupFlags & 524288) != 0)) {
                    intercepted2 = onInterceptTouchEvent(ev);
                    motionEvent.setAction(action);
                } else {
                    intercepted2 = false;
                }
                intercepted = intercepted2;
            } else {
                intercepted = true;
            }
            if (intercepted || this.mFirstTouchTarget != null) {
                motionEvent.setTargetAccessibilityFocus(false);
            }
            boolean canceled = resetCancelNextUpFlag(this) || actionMasked == 3;
            boolean split = (this.mGroupFlags & 2097152) != 0;
            TouchTarget newTouchTarget2 = null;
            boolean alreadyDispatchedToNewTouchTarget4 = false;
            if (canceled || intercepted) {
                handled2 = false;
                int i2 = action;
                alreadyDispatchedToNewTouchTarget2 = false;
            } else {
                View childWithAccessibilityFocus2 = ev.isTargetAccessibilityFocus() ? findChildWithAccessibilityFocus() : null;
                if (actionMasked == 0 || ((split && actionMasked == 5) || actionMasked == 7)) {
                    int actionIndex = ev.getActionIndex();
                    if (split) {
                        idBitsToAssign = 1 << motionEvent.getPointerId(actionIndex);
                    } else {
                        idBitsToAssign = -1;
                    }
                    int idBitsToAssign2 = idBitsToAssign;
                    removePointersFromTouchTargets(idBitsToAssign2);
                    int childrenCount = this.mChildrenCount;
                    if (0 != 0 || childrenCount == 0) {
                        handled = false;
                        int i3 = action;
                        alreadyDispatchedToNewTouchTarget3 = false;
                        int i4 = actionIndex;
                    } else {
                        float x2 = motionEvent.getX(actionIndex);
                        float y2 = motionEvent.getY(actionIndex);
                        handled = false;
                        ArrayList<View> preorderedList = buildTouchDispatchChildList();
                        boolean customOrder2 = preorderedList == null && isChildrenDrawingOrderEnabled();
                        int i5 = action;
                        View[] children = this.mChildren;
                        int i6 = childrenCount - 1;
                        while (true) {
                            newTouchTarget = newTouchTarget2;
                            int i7 = i6;
                            if (i7 < 0) {
                                alreadyDispatchedToNewTouchTarget3 = alreadyDispatchedToNewTouchTarget4;
                                int i8 = actionIndex;
                                float f = y2;
                                boolean z3 = customOrder2;
                                z2 = false;
                                break;
                            }
                            alreadyDispatchedToNewTouchTarget3 = alreadyDispatchedToNewTouchTarget4;
                            int actionIndex2 = actionIndex;
                            boolean alreadyDispatchedToNewTouchTarget5 = customOrder2;
                            int childIndex = getAndVerifyPreorderedIndex(childrenCount, i7, alreadyDispatchedToNewTouchTarget5);
                            int i9 = i7;
                            View child = getAndVerifyPreorderedView(preorderedList, children, childIndex);
                            if (childWithAccessibilityFocus2 == null) {
                                i = i9;
                            } else if (childWithAccessibilityFocus2 != child) {
                                x = x2;
                                customOrder = alreadyDispatchedToNewTouchTarget5;
                                y = y2;
                                newTouchTarget2 = newTouchTarget;
                                i = i9;
                                i6 = i - 1;
                                alreadyDispatchedToNewTouchTarget4 = alreadyDispatchedToNewTouchTarget3;
                                actionIndex = actionIndex2;
                                customOrder2 = customOrder;
                                x2 = x;
                                y2 = y;
                            } else {
                                childWithAccessibilityFocus2 = null;
                                i = childrenCount - 1;
                            }
                            if (canViewReceivePointerEvents(child)) {
                                customOrder = alreadyDispatchedToNewTouchTarget5;
                                if (!isTransformedTouchPointInView(x2, y2, child, null)) {
                                    x = x2;
                                    y = y2;
                                    childWithAccessibilityFocus = childWithAccessibilityFocus2;
                                    z = false;
                                } else {
                                    TouchTarget newTouchTarget3 = getTouchTarget(child);
                                    if (newTouchTarget3 != null) {
                                        float f2 = x2;
                                        newTouchTarget3.pointerIdBits |= idBitsToAssign2;
                                        newTouchTarget = newTouchTarget3;
                                        float f3 = y2;
                                        break;
                                    }
                                    x = x2;
                                    resetCancelNextUpFlag(child);
                                    if (dispatchTransformedTouchEvent(motionEvent, false, child, idBitsToAssign2)) {
                                        float f4 = y2;
                                        View childWithAccessibilityFocus3 = childWithAccessibilityFocus2;
                                        this.mLastTouchDownTime = ev.getDownTime();
                                        if (preorderedList != null) {
                                            int j = 0;
                                            while (true) {
                                                if (j >= childrenCount) {
                                                    break;
                                                } else if (children[childIndex] == this.mChildren[j]) {
                                                    this.mLastTouchDownIndex = j;
                                                    break;
                                                } else {
                                                    j++;
                                                }
                                            }
                                        } else {
                                            this.mLastTouchDownIndex = childIndex;
                                        }
                                        this.mLastTouchDownX = ev.getX();
                                        this.mLastTouchDownY = ev.getY();
                                        newTouchTarget = addTouchTarget(child, idBitsToAssign2);
                                        alreadyDispatchedToNewTouchTarget3 = true;
                                        View view = childWithAccessibilityFocus3;
                                    } else {
                                        y = y2;
                                        View view2 = childWithAccessibilityFocus2;
                                        motionEvent.setTargetAccessibilityFocus(false);
                                        newTouchTarget2 = newTouchTarget3;
                                        i6 = i - 1;
                                        alreadyDispatchedToNewTouchTarget4 = alreadyDispatchedToNewTouchTarget3;
                                        actionIndex = actionIndex2;
                                        customOrder2 = customOrder;
                                        x2 = x;
                                        y2 = y;
                                    }
                                }
                            } else {
                                x = x2;
                                customOrder = alreadyDispatchedToNewTouchTarget5;
                                y = y2;
                                childWithAccessibilityFocus = childWithAccessibilityFocus2;
                                z = false;
                            }
                            motionEvent.setTargetAccessibilityFocus(z);
                            newTouchTarget2 = newTouchTarget;
                            childWithAccessibilityFocus2 = childWithAccessibilityFocus;
                            i6 = i - 1;
                            alreadyDispatchedToNewTouchTarget4 = alreadyDispatchedToNewTouchTarget3;
                            actionIndex = actionIndex2;
                            customOrder2 = customOrder;
                            x2 = x;
                            y2 = y;
                        }
                        if (preorderedList != null) {
                            preorderedList.clear();
                        }
                        newTouchTarget2 = newTouchTarget;
                    }
                    alreadyDispatchedToNewTouchTarget = alreadyDispatchedToNewTouchTarget3;
                    if (newTouchTarget2 == null && this.mFirstTouchTarget != null) {
                        TouchTarget newTouchTarget4 = this.mFirstTouchTarget;
                        while (newTouchTarget2.next != null) {
                            newTouchTarget4 = newTouchTarget2.next;
                        }
                        newTouchTarget2.pointerIdBits |= idBitsToAssign2;
                    }
                    if (this.mFirstTouchTarget != null) {
                        handled3 = dispatchTransformedTouchEvent(motionEvent, canceled, null, -1);
                    } else {
                        TouchTarget predecessor = null;
                        TouchTarget target = this.mFirstTouchTarget;
                        while (target != null) {
                            TouchTarget next = target.next;
                            if (!alreadyDispatchedToNewTouchTarget || target != newTouchTarget2) {
                                boolean cancelChild = (resetCancelNextUpFlag(target.child) || intercepted) ? true : z2;
                                if (dispatchTransformedTouchEvent(motionEvent, cancelChild, target.child, target.pointerIdBits)) {
                                    handled = true;
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
                            } else {
                                handled = true;
                            }
                            predecessor = target;
                            target = next;
                        }
                        handled3 = handled;
                    }
                    if (!canceled || actionMasked == 1 || actionMasked == 7) {
                        resetTouchState();
                    } else if (split && actionMasked == 6) {
                        removePointersFromTouchTargets(1 << motionEvent.getPointerId(ev.getActionIndex()));
                    }
                } else {
                    handled2 = false;
                    int i10 = action;
                    alreadyDispatchedToNewTouchTarget2 = false;
                }
            }
            alreadyDispatchedToNewTouchTarget = alreadyDispatchedToNewTouchTarget2;
            if (this.mFirstTouchTarget != null) {
            }
            if (!canceled) {
            }
            resetTouchState();
        }
        if (!handled3 && this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(motionEvent, 1);
        }
        return handled3;
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
        ViewParent parent = current.getParent();
        while (parent instanceof View) {
            if (parent == this) {
                return current;
            }
            current = (View) parent;
            parent = current.getParent();
        }
        return null;
    }

    private void resetTouchState() {
        clearTouchTargets();
        resetCancelNextUpFlag(this);
        this.mGroupFlags &= -524289;
        this.mNestedScrollAxes = 0;
    }

    private static boolean resetCancelNextUpFlag(View view) {
        if (view == null || (view.mPrivateFlags & 67108864) == 0) {
            return false;
        }
        view.mPrivateFlags &= -67108865;
        return true;
    }

    private void clearTouchTargets() {
        TouchTarget target = this.mFirstTouchTarget;
        if (target != null) {
            do {
                TouchTarget next = target.next;
                target.recycle();
                target = next;
            } while (target != null);
            this.mFirstTouchTarget = null;
        }
    }

    private void cancelAndClearTouchTargets(MotionEvent event) {
        if (this.mFirstTouchTarget != null) {
            boolean syntheticEvent = false;
            if (event == null) {
                long now = SystemClock.uptimeMillis();
                event = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
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

    private void removePointersFromTouchTargets(int pointerIdBits) {
        TouchTarget predecessor = null;
        TouchTarget target = this.mFirstTouchTarget;
        while (target != null) {
            TouchTarget next = target.next;
            if ((target.pointerIdBits & pointerIdBits) != 0) {
                target.pointerIdBits &= ~pointerIdBits;
                if (target.pointerIdBits == 0) {
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
                MotionEvent event = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                view.dispatchTouchEvent(event);
                event.recycle();
                return;
            }
            predecessor = target;
            target = next;
        }
    }

    private static boolean canViewReceivePointerEvents(View child) {
        return (child.mViewFlags & 12) == 0 || child.getAnimation() != null;
    }

    private float[] getTempPoint() {
        if (this.mTempPoint == null) {
            this.mTempPoint = new float[2];
        }
        return this.mTempPoint;
    }

    /* access modifiers changed from: protected */
    public boolean isTransformedTouchPointInView(float x, float y, View child, PointF outLocalPoint) {
        float[] point = getTempPoint();
        point[0] = x;
        point[1] = y;
        transformPointToViewLocal(point, child);
        boolean isInView = child.pointInView(point[0], point[1]);
        if (isInView && outLocalPoint != null) {
            outLocalPoint.set(point[0], point[1]);
        }
        return isInView;
    }

    public void transformPointToViewLocal(float[] point, View child) {
        point[0] = point[0] + ((float) (this.mScrollX - child.mLeft));
        point[1] = point[1] + ((float) (this.mScrollY - child.mTop));
        if (!child.hasIdentityMatrix()) {
            child.getInverseMatrix().mapPoints(point);
        }
    }

    private boolean dispatchTransformedTouchEvent(MotionEvent event, boolean cancel, View child, int desiredPointerIdBits) {
        boolean handled;
        MotionEvent transformedEvent;
        boolean handled2;
        boolean handled3;
        int oldAction = event.getAction();
        if (cancel || oldAction == 3) {
            event.setAction(3);
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
            return false;
        }
        if (newPointerIdBits != oldPointerIdBits) {
            transformedEvent = event.split(newPointerIdBits);
        } else if (child == null || child.hasIdentityMatrix()) {
            if (child == null) {
                handled3 = super.dispatchTouchEvent(event);
            } else {
                float offsetX = (float) (this.mScrollX - child.mLeft);
                float offsetY = (float) (this.mScrollY - child.mTop);
                event.offsetLocation(offsetX, offsetY);
                boolean handled4 = child.dispatchTouchEvent(event);
                event.offsetLocation(-offsetX, -offsetY);
                handled3 = handled4;
            }
            return handled3;
        } else {
            transformedEvent = MotionEvent.obtain(event);
        }
        if (child == null) {
            handled2 = super.dispatchTouchEvent(transformedEvent);
        } else {
            transformedEvent.offsetLocation((float) (this.mScrollX - child.mLeft), (float) (this.mScrollY - child.mTop));
            if (!child.hasIdentityMatrix()) {
                transformedEvent.transform(child.getInverseMatrix());
            }
            handled2 = child.dispatchTouchEvent(transformedEvent);
        }
        transformedEvent.recycle();
        return handled2;
    }

    public void setMotionEventSplittingEnabled(boolean split) {
        if (split) {
            this.mGroupFlags |= 2097152;
        } else {
            this.mGroupFlags &= -2097153;
        }
    }

    public boolean isMotionEventSplittingEnabled() {
        return (this.mGroupFlags & 2097152) == 2097152;
    }

    public boolean isTransitionGroup() {
        boolean z = false;
        if ((this.mGroupFlags & 33554432) != 0) {
            if ((this.mGroupFlags & 16777216) != 0) {
                z = true;
            }
            return z;
        }
        ViewOutlineProvider outlineProvider = getOutlineProvider();
        if (!(getBackground() == null && getTransitionName() == null && (outlineProvider == null || outlineProvider == ViewOutlineProvider.BACKGROUND))) {
            z = true;
        }
        return z;
    }

    public void setTransitionGroup(boolean isTransitionGroup) {
        this.mGroupFlags |= 33554432;
        if (isTransitionGroup) {
            this.mGroupFlags |= 16777216;
        } else {
            this.mGroupFlags &= -16777217;
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept != ((this.mGroupFlags & 524288) != 0)) {
            if (disallowIntercept) {
                this.mGroupFlags |= 524288;
            } else {
                this.mGroupFlags &= -524289;
            }
            if (this.mParent != null) {
                this.mParent.requestDisallowInterceptTouchEvent(disallowIntercept);
            }
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!ev.isFromSource(InputDevice.SOURCE_MOUSE) || ev.getAction() != 0 || !ev.isButtonPressed(1) || !isOnScrollbarThumb(ev.getX(), ev.getY())) {
            return false;
        }
        return true;
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        boolean result;
        int descendantFocusability = getDescendantFocusability();
        if (descendantFocusability == 131072) {
            boolean took = super.requestFocus(direction, previouslyFocusedRect);
            result = took ? took : onRequestFocusInDescendants(direction, previouslyFocusedRect);
        } else if (descendantFocusability == 262144) {
            boolean took2 = onRequestFocusInDescendants(direction, previouslyFocusedRect);
            result = took2 ? took2 : super.requestFocus(direction, previouslyFocusedRect);
        } else if (descendantFocusability == 393216) {
            result = super.requestFocus(direction, previouslyFocusedRect);
        } else {
            throw new IllegalStateException("descendant focusability must be one of FOCUS_BEFORE_DESCENDANTS, FOCUS_AFTER_DESCENDANTS, FOCUS_BLOCK_DESCENDANTS but is " + descendantFocusability);
        }
        boolean result2 = result;
        if (result2 && !isLayoutValid() && (this.mPrivateFlags & 1) == 0) {
            this.mPrivateFlags |= 1;
        }
        return result2;
    }

    /* access modifiers changed from: protected */
    public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int end;
        int increment;
        int index;
        int count = this.mChildrenCount;
        if ((direction & 2) != 0) {
            index = 0;
            increment = 1;
            end = count;
        } else {
            index = count - 1;
            increment = -1;
            end = -1;
        }
        View[] children = this.mChildren;
        for (int i = index; i != end; i += increment) {
            View child = children[i];
            if ((child.mViewFlags & 12) == 0 && child.requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
        }
        return false;
    }

    public boolean restoreDefaultFocus() {
        if (this.mDefaultFocus == null || getDescendantFocusability() == 393216 || (this.mDefaultFocus.mViewFlags & 12) != 0 || !this.mDefaultFocus.restoreDefaultFocus()) {
            return super.restoreDefaultFocus();
        }
        return true;
    }

    /* JADX INFO: finally extract failed */
    public boolean restoreFocusInCluster(int direction) {
        if (!isKeyboardNavigationCluster()) {
            return restoreFocusInClusterInternal(direction);
        }
        boolean blockedFocus = getTouchscreenBlocksFocus();
        try {
            setTouchscreenBlocksFocusNoRefocus(false);
            boolean restoreFocusInClusterInternal = restoreFocusInClusterInternal(direction);
            setTouchscreenBlocksFocusNoRefocus(blockedFocus);
            return restoreFocusInClusterInternal;
        } catch (Throwable th) {
            setTouchscreenBlocksFocusNoRefocus(blockedFocus);
            throw th;
        }
    }

    private boolean restoreFocusInClusterInternal(int direction) {
        if (this.mFocusedInCluster == null || getDescendantFocusability() == 393216 || (this.mFocusedInCluster.mViewFlags & 12) != 0 || !this.mFocusedInCluster.restoreFocusInCluster(direction)) {
            return super.restoreFocusInCluster(direction);
        }
        return true;
    }

    public boolean restoreFocusNotInCluster() {
        if (this.mFocusedInCluster != null) {
            return restoreFocusInCluster(130);
        }
        if (isKeyboardNavigationCluster() || (this.mViewFlags & 12) != 0) {
            return false;
        }
        int descendentFocusability = getDescendantFocusability();
        if (descendentFocusability == 393216) {
            return super.requestFocus(130, null);
        }
        if (descendentFocusability == 131072 && super.requestFocus(130, null)) {
            return true;
        }
        for (int i = 0; i < this.mChildrenCount; i++) {
            View child = this.mChildren[i];
            if (!child.isKeyboardNavigationCluster() && child.restoreFocusNotInCluster()) {
                return true;
            }
        }
        if (descendentFocusability != 262144 || hasFocusableChild(false)) {
            return false;
        }
        return super.requestFocus(130, null);
    }

    public void dispatchStartTemporaryDetach() {
        super.dispatchStartTemporaryDetach();
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchStartTemporaryDetach();
        }
    }

    public void dispatchFinishTemporaryDetach() {
        super.dispatchFinishTemporaryDetach();
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchFinishTemporaryDetach();
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchAttachedToWindow(View.AttachInfo info, int visibility) {
        this.mGroupFlags |= 4194304;
        super.dispatchAttachedToWindow(info, visibility);
        this.mGroupFlags &= -4194305;
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            View child = children[i];
            child.dispatchAttachedToWindow(info, combineVisibility(visibility, child.getVisibility()));
        }
        int transientCount = this.mTransientIndices == null ? 0 : this.mTransientIndices.size();
        for (int i2 = 0; i2 < transientCount; i2++) {
            View view = this.mTransientViews.get(i2);
            view.dispatchAttachedToWindow(info, combineVisibility(visibility, view.getVisibility()));
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchScreenStateChanged(int screenState) {
        super.dispatchScreenStateChanged(screenState);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchScreenStateChanged(screenState);
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchMovedToDisplay(Display display, Configuration config) {
        super.dispatchMovedToDisplay(display, config);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchMovedToDisplay(display, config);
        }
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        Throwable th;
        boolean handled = false;
        if (includeForAccessibility()) {
            handled = super.dispatchPopulateAccessibilityEventInternal(event);
            if (handled) {
                return handled;
            }
        }
        ChildListForAccessibility children = ChildListForAccessibility.obtain(this, true);
        try {
            int childCount = children.getChildCount();
            boolean z = handled;
            int i = 0;
            while (i < childCount) {
                try {
                    View child = children.getChildAt(i);
                    if ((child.mViewFlags & 12) == 0) {
                        boolean handled2 = child.dispatchPopulateAccessibilityEvent(event);
                        if (handled2) {
                            children.recycle();
                            return handled2;
                        }
                    }
                    i++;
                } catch (Throwable th2) {
                    th = th2;
                    children.recycle();
                    throw th;
                }
            }
            children.recycle();
            return false;
        } catch (Throwable th3) {
            boolean z2 = handled;
            th = th3;
            children.recycle();
            throw th;
        }
    }

    public void dispatchProvideStructure(ViewStructure structure) {
        int childIndex;
        super.dispatchProvideStructure(structure);
        if (!isAssistBlocked() && structure.getChildCount() == 0) {
            int childrenCount = this.mChildrenCount;
            if (childrenCount > 0) {
                if (!isLaidOut()) {
                    if (Helper.sVerbose) {
                        Log.v("View", "dispatchProvideStructure(): not laid out, ignoring " + childrenCount + " children of " + getAccessibilityViewId());
                    }
                    return;
                }
                structure.setChildCount(childrenCount);
                ArrayList<View> preorderedList = buildOrderedChildList();
                boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
                ArrayList<View> preorderedList2 = preorderedList;
                for (int i = 0; i < childrenCount; i++) {
                    try {
                        childIndex = getAndVerifyPreorderedIndex(childrenCount, i, customOrder);
                    } catch (IndexOutOfBoundsException e) {
                        childIndex = i;
                        if (this.mContext.getApplicationInfo().targetSdkVersion < 23) {
                            Log.w(TAG, "Bad getChildDrawingOrder while collecting assist @ " + i + " of " + childrenCount, e);
                            customOrder = false;
                            if (i > 0) {
                                int[] permutation = new int[childrenCount];
                                SparseBooleanArray usedIndices = new SparseBooleanArray();
                                for (int j = 0; j < i; j++) {
                                    permutation[j] = getChildDrawingOrder(childrenCount, j);
                                    usedIndices.put(permutation[j], true);
                                }
                                int nextIndex = 0;
                                for (int j2 = i; j2 < childrenCount; j2++) {
                                    while (usedIndices.get(nextIndex, false)) {
                                        nextIndex++;
                                    }
                                    permutation[j2] = nextIndex;
                                    nextIndex++;
                                }
                                preorderedList2 = new ArrayList<>(childrenCount);
                                for (int j3 = 0; j3 < childrenCount; j3++) {
                                    preorderedList2.add(this.mChildren[permutation[j3]]);
                                }
                            }
                        } else {
                            throw e;
                        }
                    }
                    getAndVerifyPreorderedView(preorderedList2, this.mChildren, childIndex).dispatchProvideStructure(structure.newChild(i));
                }
                if (preorderedList2 != null) {
                    preorderedList2.clear();
                }
            }
        }
    }

    public void dispatchProvideAutofillStructure(ViewStructure structure, int flags) {
        super.dispatchProvideAutofillStructure(structure, flags);
        if (structure.getChildCount() == 0) {
            if (!isLaidOut()) {
                if (Helper.sVerbose) {
                    Log.v("View", "dispatchProvideAutofillStructure(): not laid out, ignoring " + this.mChildrenCount + " children of " + getAutofillId());
                }
                return;
            }
            ChildListForAutoFill children = getChildrenForAutofill(flags);
            int childrenCount = children.size();
            structure.setChildCount(childrenCount);
            for (int i = 0; i < childrenCount; i++) {
                ((View) children.get(i)).dispatchProvideAutofillStructure(structure.newChild(i), flags);
            }
            children.recycle();
        }
    }

    private ChildListForAutoFill getChildrenForAutofill(int flags) {
        ChildListForAutoFill children = ChildListForAutoFill.obtain();
        populateChildrenForAutofill(children, flags);
        return children;
    }

    private void populateChildrenForAutofill(ArrayList<View> list, int flags) {
        int childrenCount = this.mChildrenCount;
        if (childrenCount > 0) {
            ArrayList<View> preorderedList = buildOrderedChildList();
            boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
            for (int i = 0; i < childrenCount; i++) {
                int childIndex = getAndVerifyPreorderedIndex(childrenCount, i, customOrder);
                View child = preorderedList == null ? this.mChildren[childIndex] : preorderedList.get(childIndex);
                if ((flags & 1) != 0 || child.isImportantForAutofill()) {
                    list.add(child);
                } else if (child instanceof ViewGroup) {
                    ((ViewGroup) child).populateChildrenForAutofill(list, flags);
                }
            }
        }
    }

    private static View getAndVerifyPreorderedView(ArrayList<View> preorderedList, View[] children, int childIndex) {
        if (preorderedList == null) {
            return children[childIndex];
        }
        View child = preorderedList.get(childIndex);
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
                for (int i = 0; i < childrenForAccessibilityCount; i++) {
                    info.addChildUnchecked(childrenForAccessibility.get(i));
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
            notifyViewAccessibilityStateChangedIfNeeded(1);
        } else if (this.mParent != null) {
            try {
                this.mParent.notifySubtreeAccessibilityStateChanged(this, source, changeType);
            } catch (AbstractMethodError e) {
                Log.e("View", this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
            }
        }
    }

    public void notifySubtreeAccessibilityStateChangedIfNeeded() {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && this.mAttachInfo != null) {
            if (getImportantForAccessibility() != 4 && !isImportantForAccessibility() && getChildCount() > 0) {
                ViewParent a11yParent = getParentForAccessibility();
                if (a11yParent instanceof View) {
                    ((View) a11yParent).notifySubtreeAccessibilityStateChangedIfNeeded();
                    return;
                }
            }
            super.notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void resetSubtreeAccessibilityStateChanged() {
        super.resetSubtreeAccessibilityStateChanged();
        View[] children = this.mChildren;
        int childCount = this.mChildrenCount;
        for (int i = 0; i < childCount; i++) {
            if (children[i] != null) {
                children[i].resetSubtreeAccessibilityStateChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getNumChildrenForAccessibility() {
        int numChildrenForAccessibility = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.includeForAccessibility()) {
                numChildrenForAccessibility++;
            } else if (child instanceof ViewGroup) {
                numChildrenForAccessibility += ((ViewGroup) child).getNumChildrenForAccessibility();
            }
        }
        return numChildrenForAccessibility;
    }

    public View dispatchFindView(int index, int childrenCount) {
        View child;
        ArrayList<View> preorderedList = buildOrderedChildList();
        boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
        View[] children = this.mChildren;
        int childIndex = customOrder ? getChildDrawingOrder(childrenCount, index) : index;
        if (preorderedList == null) {
            if (childIndex < 0 || childIndex >= children.length) {
                return null;
            }
            child = children[childIndex];
        } else if (childIndex < 0 || childIndex >= preorderedList.size()) {
            return null;
        } else {
            child = preorderedList.get(childIndex);
        }
        return child;
    }

    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void dispatchDetachedFromWindow() {
        cancelAndClearTouchTargets(null);
        exitHoverTargets();
        exitTooltipHoverTargets();
        this.mLayoutCalledWhileSuppressed = false;
        this.mChildrenInterestedInDrag = null;
        this.mIsInterestedInDrag = false;
        if (this.mCurrentDragStartEvent != null) {
            this.mCurrentDragStartEvent.recycle();
            this.mCurrentDragStartEvent = null;
        }
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            if (children[i] != null) {
                children[i].dispatchDetachedFromWindow();
            }
        }
        clearDisappearingChildren();
        int transientCount = this.mTransientViews == null ? 0 : this.mTransientIndices.size();
        for (int i2 = 0; i2 < transientCount; i2++) {
            View view = this.mTransientViews.get(i2);
            if (view != null) {
                view.dispatchDetachedFromWindow();
            }
        }
        super.dispatchDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void internalSetPadding(int left, int top, int right, int bottom) {
        super.internalSetPadding(left, top, right, bottom);
        if ((this.mPaddingLeft | this.mPaddingTop | this.mPaddingRight | this.mPaddingBottom) != 0) {
            this.mGroupFlags |= 32;
        } else {
            this.mGroupFlags &= -33;
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchSaveInstanceState(container);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            View c = children[i];
            if ((c.mViewFlags & 536870912) != 536870912) {
                c.dispatchSaveInstanceState(container);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchFreezeSelfOnly(SparseArray<Parcelable> container) {
        super.dispatchSaveInstanceState(container);
    }

    /* access modifiers changed from: protected */
    public void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        super.dispatchRestoreInstanceState(container);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            View c = children[i];
            if (!(c == null || (c.mViewFlags & 536870912) == 536870912)) {
                c.dispatchRestoreInstanceState(container);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchThawSelfOnly(SparseArray<Parcelable> container) {
        super.dispatchRestoreInstanceState(container);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void setChildrenDrawingCacheEnabled(boolean enabled) {
        if (enabled || (this.mPersistentDrawingCache & 3) != 3) {
            View[] children = this.mChildren;
            int count = this.mChildrenCount;
            for (int i = 0; i < count; i++) {
                children[i].setDrawingCacheEnabled(enabled);
            }
        }
    }

    public Bitmap createSnapshot(ViewDebug.CanvasProvider canvasProvider, boolean skipChildren) {
        int count = this.mChildrenCount;
        int[] visibilities = null;
        int i = 0;
        if (skipChildren) {
            visibilities = new int[count];
            for (int i2 = 0; i2 < count; i2++) {
                View child = getChildAt(i2);
                visibilities[i2] = child.getVisibility();
                if (visibilities[i2] == 0) {
                    child.mViewFlags = (child.mViewFlags & -13) | 4;
                }
            }
        }
        try {
            Bitmap createSnapshot = super.createSnapshot(canvasProvider, skipChildren);
            if (skipChildren) {
                while (i < count) {
                    View child2 = getChildAt(i);
                    child2.mViewFlags = (child2.mViewFlags & -13) | (visibilities[i] & 12);
                    i++;
                }
            }
            return createSnapshot;
        } catch (Throwable th) {
            if (skipChildren) {
                while (i < count) {
                    View child3 = getChildAt(i);
                    child3.mViewFlags = (child3.mViewFlags & -13) | (visibilities[i] & 12);
                    i++;
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isLayoutModeOptical() {
        return this.mLayoutMode == 1;
    }

    /* access modifiers changed from: package-private */
    public Insets computeOpticalInsets() {
        if (!isLayoutModeOptical()) {
            return Insets.NONE;
        }
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        for (int i = 0; i < this.mChildrenCount; i++) {
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
            if (x1 > x2) {
                int tmp = x1;
                x1 = x2;
                x2 = tmp;
            }
            if (y1 > y2) {
                int tmp2 = y1;
                y1 = y2;
                y2 = tmp2;
            }
            canvas.drawRect((float) x1, (float) y1, (float) x2, (float) y2, paint);
        }
    }

    private static int sign(int x) {
        return x >= 0 ? 1 : -1;
    }

    private static void drawCorner(Canvas c, Paint paint, int x1, int y1, int dx, int dy, int lw) {
        Canvas canvas = c;
        Paint paint2 = paint;
        int i = x1;
        int i2 = y1;
        fillRect(canvas, paint2, i, i2, x1 + dx, y1 + (sign(dy) * lw));
        fillRect(c, paint, x1, y1, x1 + (sign(dx) * lw), y1 + dy);
    }

    private static void drawRectCorners(Canvas canvas, int x1, int y1, int x2, int y2, Paint paint, int lineLength, int lineWidth) {
        int i = lineLength;
        Canvas canvas2 = canvas;
        Paint paint2 = paint;
        int i2 = x1;
        int i3 = i;
        int i4 = lineWidth;
        drawCorner(canvas2, paint2, i2, y1, i3, i, i4);
        drawCorner(canvas2, paint2, i2, y2, i3, -i, i4);
        drawCorner(canvas2, paint2, x2, y1, -i, i, i4);
        drawCorner(canvas, paint, x2, y2, -i, -i, lineWidth);
    }

    /* access modifiers changed from: private */
    public static void fillDifference(Canvas canvas, int x2, int y2, int x3, int y3, int dx1, int dy1, int dx2, int dy2, Paint paint) {
        int x1 = x2 - dx1;
        int x4 = x3 + dx2;
        Canvas canvas2 = canvas;
        Paint paint2 = paint;
        int i = x1;
        fillRect(canvas2, paint2, i, y2 - dy1, x4, y2);
        int i2 = y2;
        int i3 = y3;
        fillRect(canvas2, paint2, i, i2, x2, i3);
        int i4 = x4;
        fillRect(canvas2, paint2, x3, i2, i4, i3);
        fillRect(canvas2, paint2, x1, y3, i4, y3 + dy2);
    }

    /* access modifiers changed from: protected */
    public void onDebugDrawMargins(Canvas canvas, Paint paint) {
        for (int i = 0; i < getChildCount(); i++) {
            View c = getChildAt(i);
            c.getLayoutParams().onDebugDraw(c, canvas, paint);
        }
    }

    /* access modifiers changed from: protected */
    public void onDebugDraw(Canvas canvas) {
        Paint paint = getDebugPaint();
        paint.setColor(Menu.CATEGORY_MASK);
        paint.setStyle(Paint.Style.STROKE);
        int i = 0;
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 >= getChildCount()) {
                break;
            }
            View c = getChildAt(i3);
            if (c.getVisibility() != 8) {
                Insets insets = c.getOpticalInsets();
                drawRect(canvas, paint, insets.left + c.getLeft(), insets.top + c.getTop(), (c.getRight() - insets.right) - 1, (c.getBottom() - insets.bottom) - 1);
            }
            i2 = i3 + 1;
        }
        paint.setColor(Color.argb(63, 255, 0, 255));
        paint.setStyle(Paint.Style.FILL);
        onDebugDrawMargins(canvas, paint);
        paint.setColor(DEBUG_CORNERS_COLOR);
        paint.setStyle(Paint.Style.FILL);
        int lineLength = dipsToPixels(8);
        int lineWidth = dipsToPixels(1);
        while (true) {
            int i4 = i;
            if (i4 < getChildCount()) {
                View c2 = getChildAt(i4);
                if (c2.getVisibility() != 8) {
                    drawRectCorners(canvas, c2.getLeft(), c2.getTop(), c2.getRight(), c2.getBottom(), paint, lineLength, lineWidth);
                }
                i = i4 + 1;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        Canvas canvas2 = canvas;
        boolean usingRenderNodeProperties = canvas2.isRecordingFor(this.mRenderNode);
        int childrenCount = this.mChildrenCount;
        View[] children = this.mChildren;
        int transientIndex = this.mGroupFlags;
        int i = 0;
        if ((transientIndex & 8) != 0 && canAnimate()) {
            boolean z = !isHardwareAccelerated();
            for (int i2 = 0; i2 < childrenCount; i2++) {
                View child = children[i2];
                if (child != null && (child.mViewFlags & 12) == 0) {
                    attachLayoutAnimationParameters(child, child.getLayoutParams(), i2, childrenCount);
                    bindLayoutAnimation(child);
                }
            }
            LayoutAnimationController controller = this.mLayoutAnimationController;
            if (controller.willOverlap()) {
                this.mGroupFlags |= 128;
            }
            controller.start();
            this.mGroupFlags &= -9;
            this.mGroupFlags &= -17;
            if (this.mAnimationListener != null) {
                this.mAnimationListener.onAnimationStart(controller.getAnimation());
            }
        }
        int clipSaveCount = 0;
        boolean clipToPadding = (transientIndex & 34) == 34;
        if (clipToPadding) {
            clipSaveCount = canvas2.save(2);
            canvas2.clipRect(this.mScrollX + this.mPaddingLeft, this.mScrollY + this.mPaddingTop, ((this.mScrollX + this.mRight) - this.mLeft) - this.mPaddingRight, ((this.mScrollY + this.mBottom) - this.mTop) - this.mPaddingBottom);
        }
        this.mPrivateFlags &= -65;
        this.mGroupFlags &= -5;
        boolean more = false;
        long drawingTime = getDrawingTime();
        if (usingRenderNodeProperties) {
            canvas.insertReorderBarrier();
        }
        int transientCount = this.mTransientIndices == null ? 0 : this.mTransientIndices.size();
        int transientIndex2 = transientCount != 0 ? 0 : -1;
        ArrayList<View> preorderedList = usingRenderNodeProperties ? null : buildOrderedChildList();
        int customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
        while (i < childrenCount) {
            while (transientIndex2 >= 0 && this.mTransientIndices.get(transientIndex2).intValue() == i) {
                View transientChild = this.mTransientViews.get(transientIndex2);
                int flags = transientIndex;
                if ((transientChild.mViewFlags & 12) == 0 || transientChild.getAnimation() != null) {
                    more = drawChild(canvas2, transientChild, drawingTime) | more;
                }
                int transientIndex3 = transientIndex2 + 1;
                if (transientIndex3 >= transientCount) {
                    transientIndex3 = -1;
                }
                transientIndex = flags;
            }
            int flags2 = transientIndex;
            boolean customOrder2 = customOrder;
            int childrenCount2 = childrenCount;
            View child2 = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, customOrder2));
            View[] children2 = children;
            if ((child2.mViewFlags & 12) == 0 || child2.getAnimation() != null) {
                more |= drawChild(canvas2, child2, drawingTime);
            }
            i++;
            customOrder = customOrder2;
            transientIndex = flags2;
            childrenCount = childrenCount2;
            children = children2;
        }
        View[] viewArr = children;
        int i3 = transientIndex;
        int flags3 = customOrder;
        while (transientIndex2 >= 0) {
            View transientChild2 = this.mTransientViews.get(transientIndex2);
            if ((transientChild2.mViewFlags & 12) == 0 || transientChild2.getAnimation() != null) {
                more = drawChild(canvas2, transientChild2, drawingTime) | more;
            }
            transientIndex2++;
            if (transientIndex2 >= transientCount) {
                break;
            }
        }
        if (preorderedList != null) {
            preorderedList.clear();
        }
        if (this.mDisappearingChildren != null) {
            ArrayList<View> disappearingChildren = this.mDisappearingChildren;
            for (int i4 = disappearingChildren.size() - 1; i4 >= 0; i4--) {
                more |= drawChild(canvas2, disappearingChildren.get(i4), drawingTime);
            }
        }
        if (usingRenderNodeProperties) {
            canvas.insertInorderBarrier();
        }
        if (debugDraw()) {
            onDebugDraw(canvas);
        }
        if (clipToPadding) {
            canvas2.restoreToCount(clipSaveCount);
        }
        int flags4 = this.mGroupFlags;
        if ((flags4 & 4) == 4) {
            invalidate(true);
        }
        if ((flags4 & 16) == 0 && (flags4 & 512) == 0 && this.mLayoutAnimationController.isDone() && !more) {
            this.mGroupFlags |= 512;
            post(new Runnable() {
                public void run() {
                    ViewGroup.this.notifyAnimationListener();
                }
            });
        }
    }

    public ViewGroupOverlay getOverlay() {
        if (this.mOverlay == null) {
            this.mOverlay = new ViewGroupOverlay(this.mContext, this);
        }
        return (ViewGroupOverlay) this.mOverlay;
    }

    /* access modifiers changed from: protected */
    public int getChildDrawingOrder(int childCount, int i) {
        return i;
    }

    private boolean hasChildWithZ() {
        for (int i = 0; i < this.mChildrenCount; i++) {
            if (this.mChildren[i].getZ() != 0.0f) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<View> buildOrderedChildList() {
        int childrenCount = this.mChildrenCount;
        if (childrenCount <= 1 || !hasChildWithZ()) {
            return null;
        }
        if (this.mPreSortedChildren == null) {
            this.mPreSortedChildren = new ArrayList<>(childrenCount);
        } else {
            this.mPreSortedChildren.clear();
            this.mPreSortedChildren.ensureCapacity(childrenCount);
        }
        boolean customOrder = isChildrenDrawingOrderEnabled();
        for (int i = 0; i < childrenCount; i++) {
            View nextChild = this.mChildren[getAndVerifyPreorderedIndex(childrenCount, i, customOrder)];
            float currentZ = nextChild.getZ();
            int insertIndex = i;
            while (insertIndex > 0 && this.mPreSortedChildren.get(insertIndex - 1).getZ() > currentZ) {
                insertIndex--;
            }
            this.mPreSortedChildren.add(insertIndex, nextChild);
        }
        return this.mPreSortedChildren;
    }

    /* access modifiers changed from: private */
    public void notifyAnimationListener() {
        this.mGroupFlags &= -513;
        this.mGroupFlags |= 16;
        if (this.mAnimationListener != null) {
            post(new Runnable() {
                public void run() {
                    ViewGroup.this.mAnimationListener.onAnimationEnd(ViewGroup.this.mLayoutAnimationController.getAnimation());
                }
            });
        }
        invalidate(true);
    }

    /* access modifiers changed from: protected */
    public void dispatchGetDisplayList() {
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            View child = children[i];
            if ((child.mViewFlags & 12) == 0 || child.getAnimation() != null) {
                recreateChildDisplayList(child);
            }
        }
        int transientCount = this.mTransientViews == null ? 0 : this.mTransientIndices.size();
        for (int i2 = 0; i2 < transientCount; i2++) {
            View child2 = this.mTransientViews.get(i2);
            if ((child2.mViewFlags & 12) == 0 || child2.getAnimation() != null) {
                recreateChildDisplayList(child2);
            }
        }
        if (this.mOverlay != null) {
            recreateChildDisplayList(this.mOverlay.getOverlayView());
        }
        if (this.mDisappearingChildren != null) {
            ArrayList<View> disappearingChildren = this.mDisappearingChildren;
            int disappearingCount = disappearingChildren.size();
            for (int i3 = 0; i3 < disappearingCount; i3++) {
                recreateChildDisplayList(disappearingChildren.get(i3));
            }
        }
    }

    private void recreateChildDisplayList(View child) {
        child.mRecreateDisplayList = (child.mPrivateFlags & Integer.MIN_VALUE) != 0;
        child.mPrivateFlags &= Integer.MAX_VALUE;
        child.updateDisplayListIfDirty();
        child.mRecreateDisplayList = false;
    }

    /* access modifiers changed from: protected */
    public boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child != null) {
            return child.draw(canvas, this, drawingTime);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void getScrollIndicatorBounds(Rect out) {
        super.getScrollIndicatorBounds(out);
        if ((this.mGroupFlags & 34) == 34) {
            out.left += this.mPaddingLeft;
            out.right -= this.mPaddingRight;
            out.top += this.mPaddingTop;
            out.bottom -= this.mPaddingBottom;
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean getClipChildren() {
        return (this.mGroupFlags & 1) != 0;
    }

    public void setClipChildren(boolean clipChildren) {
        if (clipChildren != ((this.mGroupFlags & 1) == 1)) {
            setBooleanFlag(1, clipChildren);
            for (int i = 0; i < this.mChildrenCount; i++) {
                View child = getChildAt(i);
                if (child.mRenderNode != null) {
                    child.mRenderNode.setClipToBounds(clipChildren);
                }
            }
            invalidate(true);
        }
    }

    public void setClipToPadding(boolean clipToPadding) {
        if (hasBooleanFlag(2) != clipToPadding) {
            setBooleanFlag(2, clipToPadding);
            invalidate(true);
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean getClipToPadding() {
        return hasBooleanFlag(2);
    }

    public void dispatchSetSelected(boolean selected) {
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = 0; i < count; i++) {
            children[i].setSelected(selected);
        }
    }

    public void dispatchSetActivated(boolean activated) {
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = 0; i < count; i++) {
            children[i].setActivated(activated);
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchSetPressed(boolean pressed) {
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = 0; i < count; i++) {
            View child = children[i];
            if (!pressed || (!child.isClickable() && !child.isLongClickable())) {
                child.setPressed(pressed);
            }
        }
    }

    public void dispatchDrawableHotspotChanged(float x, float y) {
        int count = this.mChildrenCount;
        if (count != 0) {
            View[] children = this.mChildren;
            for (int i = 0; i < count; i++) {
                View child = children[i];
                boolean nonActionable = !child.isClickable() && !child.isLongClickable();
                boolean duplicatesState = (child.mViewFlags & 4194304) != 0;
                if (nonActionable || duplicatesState) {
                    float[] point = getTempPoint();
                    point[0] = x;
                    point[1] = y;
                    transformPointToViewLocal(point, child);
                    child.drawableHotspotChanged(point[0], point[1]);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchCancelPendingInputEvents() {
        super.dispatchCancelPendingInputEvents();
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = 0; i < count; i++) {
            children[i].dispatchCancelPendingInputEvents();
        }
    }

    /* access modifiers changed from: protected */
    public void setStaticTransformationsEnabled(boolean enabled) {
        setBooleanFlag(2048, enabled);
    }

    /* access modifiers changed from: protected */
    public boolean getChildStaticTransformation(View child, Transformation t) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public Transformation getChildTransformation() {
        if (this.mChildTransformation == null) {
            this.mChildTransformation = new Transformation();
        }
        return this.mChildTransformation;
    }

    /* access modifiers changed from: protected */
    public <T extends View> T findViewTraversal(int id) {
        if (id == this.mID) {
            return this;
        }
        View[] where = this.mChildren;
        int len = this.mChildrenCount;
        for (int i = 0; i < len; i++) {
            View v = where[i];
            if ((v.mPrivateFlags & 8) == 0) {
                View v2 = v.findViewById(id);
                if (v2 != null) {
                    return v2;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public <T extends View> T findViewWithTagTraversal(Object tag) {
        if (tag != null && tag.equals(this.mTag)) {
            return this;
        }
        View[] where = this.mChildren;
        int len = this.mChildrenCount;
        for (int i = 0; i < len; i++) {
            View v = where[i];
            if ((v.mPrivateFlags & 8) == 0) {
                View v2 = v.findViewWithTag(tag);
                if (v2 != null) {
                    return v2;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        if (predicate.test(this)) {
            return this;
        }
        View[] where = this.mChildren;
        int len = this.mChildrenCount;
        for (int i = 0; i < len; i++) {
            View v = where[i];
            if (v != childToSkip && (v.mPrivateFlags & 8) == 0) {
                View v2 = v.findViewByPredicate(predicate);
                if (v2 != null) {
                    return v2;
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
                int insertionIndex = 0;
                while (insertionIndex < oldSize && index >= this.mTransientIndices.get(insertionIndex).intValue()) {
                    insertionIndex++;
                }
                this.mTransientIndices.add(insertionIndex, Integer.valueOf(index));
                this.mTransientViews.add(insertionIndex, view);
            } else {
                this.mTransientIndices.add(Integer.valueOf(index));
                this.mTransientViews.add(view);
            }
            view.mParent = this;
            view.dispatchAttachedToWindow(this.mAttachInfo, this.mViewFlags & 12);
            invalidate(true);
        }
    }

    public void removeTransientView(View view) {
        if (this.mTransientViews != null) {
            int size = this.mTransientViews.size();
            for (int i = 0; i < size; i++) {
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
        if (this.mTransientIndices == null) {
            return 0;
        }
        return this.mTransientIndices.size();
    }

    public int getTransientViewIndex(int position) {
        if (position < 0 || this.mTransientIndices == null || position >= this.mTransientIndices.size()) {
            return -1;
        }
        return this.mTransientIndices.get(position).intValue();
    }

    public View getTransientView(int position) {
        if (this.mTransientViews == null || position >= this.mTransientViews.size()) {
            return null;
        }
        return this.mTransientViews.get(position);
    }

    public void addView(View child) {
        addView(child, -1);
    }

    public void addView(View child, int index) {
        if (child != null) {
            LayoutParams params = child.getLayoutParams();
            if (params == null) {
                params = generateDefaultLayoutParams();
                if (params == null) {
                    throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
                }
            }
            addView(child, index, params);
            return;
        }
        throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
    }

    public void addView(View child, int width, int height) {
        LayoutParams params = generateDefaultLayoutParams();
        params.width = width;
        params.height = height;
        addView(child, -1, params);
    }

    public void addView(View child, LayoutParams params) {
        addView(child, -1, params);
    }

    public void addView(View child, int index, LayoutParams params) {
        if (child != null) {
            requestLayout();
            invalidate(true);
            addViewInner(child, index, params, false);
            return;
        }
        throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
    }

    public void updateViewLayout(View view, LayoutParams params) {
        if (!checkLayoutParams(params)) {
            throw new IllegalArgumentException("Invalid LayoutParams supplied to " + this);
        } else if (view.mParent == this) {
            view.setLayoutParams(params);
        } else {
            throw new IllegalArgumentException("Given view not a child of " + this);
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(LayoutParams p) {
        return p != null;
    }

    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        this.mOnHierarchyChangeListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void dispatchViewAdded(View child) {
        onViewAdded(child);
        if (this.mOnHierarchyChangeListener != null) {
            this.mOnHierarchyChangeListener.onChildViewAdded(this, child);
        }
    }

    public void onViewAdded(View child) {
    }

    /* access modifiers changed from: package-private */
    public void dispatchViewRemoved(View child) {
        onViewRemoved(child);
        if (this.mOnHierarchyChangeListener != null) {
            this.mOnHierarchyChangeListener.onChildViewRemoved(this, child);
        }
    }

    public void onViewRemoved(View child) {
    }

    private void clearCachedLayoutMode() {
        if (!hasBooleanFlag(8388608)) {
            this.mLayoutMode = -1;
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        clearCachedLayoutMode();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearCachedLayoutMode();
    }

    /* access modifiers changed from: protected */
    public void destroyHardwareResources() {
        super.destroyHardwareResources();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).destroyHardwareResources();
        }
    }

    /* access modifiers changed from: protected */
    public boolean addViewInLayout(View child, int index, LayoutParams params) {
        return addViewInLayout(child, index, params, false);
    }

    /* access modifiers changed from: protected */
    public boolean addViewInLayout(View child, int index, LayoutParams params, boolean preventRequestLayout) {
        if (child != null) {
            child.mParent = null;
            addViewInner(child, index, params, preventRequestLayout);
            child.mPrivateFlags = (child.mPrivateFlags & -6291457) | 32;
            return true;
        }
        throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
    }

    /* access modifiers changed from: protected */
    public void cleanupLayoutState(View child) {
        child.mPrivateFlags &= -4097;
    }

    private void addViewInner(View child, int index, LayoutParams params, boolean preventRequestLayout) {
        if (this.mTransition != null) {
            this.mTransition.cancel(3);
        }
        if (child.getParent() == null) {
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
            if (child.hasUnhandledKeyListener()) {
                incrementChildUnhandledKeyListeners();
            }
            if (child.hasFocus()) {
                requestChildFocus(child, child.findFocus());
            }
            View.AttachInfo ai = this.mAttachInfo;
            if (ai != null && (this.mGroupFlags & 4194304) == 0) {
                boolean lastKeepOn = ai.mKeepScreenOn;
                ai.mKeepScreenOn = false;
                child.dispatchAttachedToWindow(this.mAttachInfo, this.mViewFlags & 12);
                if (ai.mKeepScreenOn) {
                    needGlobalAttributesUpdate(true);
                }
                ai.mKeepScreenOn = lastKeepOn;
            }
            if (child.isLayoutDirectionInherited()) {
                child.resetRtlProperties();
            }
            dispatchViewAdded(child);
            if ((child.mViewFlags & 4194304) == 4194304) {
                this.mGroupFlags |= 65536;
            }
            if (child.hasTransientState()) {
                childHasTransientStateChanged(child, true);
            }
            if (child.getVisibility() != 8) {
                notifySubtreeAccessibilityStateChangedIfNeeded();
            }
            if (this.mTransientIndices != null) {
                int transientCount = this.mTransientIndices.size();
                for (int i = 0; i < transientCount; i++) {
                    int oldIndex = this.mTransientIndices.get(i).intValue();
                    if (index <= oldIndex) {
                        this.mTransientIndices.set(i, Integer.valueOf(oldIndex + 1));
                    }
                }
            }
            if (this.mCurrentDragStartEvent != null && child.getVisibility() == 0) {
                notifyChildOfDragStart(child);
            }
            if (child.hasDefaultFocus()) {
                setDefaultFocus(child);
            }
            touchAccessibilityNodeProviderIfNeeded(child);
            View.sTriggerFlag = true;
            return;
        }
        throw new IllegalStateException("The specified child already has a parent. You must call removeView() on the child's parent first.");
    }

    private void touchAccessibilityNodeProviderIfNeeded(View child) {
        if (this.mContext.isAutofillCompatibilityEnabled()) {
            child.getAccessibilityNodeProvider();
        }
    }

    private void addInArray(View child, int index) {
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        int size = children.length;
        if (index == count) {
            if (size == count) {
                this.mChildren = new View[(size + 12)];
                System.arraycopy(children, 0, this.mChildren, 0, size);
                children = this.mChildren;
            }
            int i = this.mChildrenCount;
            this.mChildrenCount = i + 1;
            children[i] = child;
        } else if (index < count) {
            if (size == count) {
                this.mChildren = new View[(size + 12)];
                System.arraycopy(children, 0, this.mChildren, 0, index);
                System.arraycopy(children, index, this.mChildren, index + 1, count - index);
                children = this.mChildren;
            } else {
                System.arraycopy(children, index, children, index + 1, count - index);
            }
            children[index] = child;
            this.mChildrenCount++;
            if (this.mLastTouchDownIndex >= index) {
                this.mLastTouchDownIndex++;
            }
        } else {
            throw new IndexOutOfBoundsException("index=" + index + " count=" + count);
        }
    }

    private void removeFromArray(int index) {
        int count;
        View[] children = this.mChildren;
        if ((this.mTransitioningViews == null || !this.mTransitioningViews.contains(children[index])) && children[index] != null) {
            children[index].mParent = null;
        }
        if (index == this.mChildrenCount - 1) {
            int i = this.mChildrenCount - 1;
            this.mChildrenCount = i;
            children[i] = null;
        } else if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException();
        } else {
            System.arraycopy(children, index + 1, children, index, (count - index) - 1);
            int i2 = this.mChildrenCount - 1;
            this.mChildrenCount = i2;
            children[i2] = null;
        }
        if (this.mLastTouchDownIndex == index) {
            this.mLastTouchDownTime = 0;
            this.mLastTouchDownIndex = -1;
        } else if (this.mLastTouchDownIndex > index) {
            this.mLastTouchDownIndex--;
        }
    }

    private void removeFromArray(int start, int count) {
        View[] children = this.mChildren;
        int childrenCount = this.mChildrenCount;
        int start2 = Math.max(0, start);
        int end = Math.min(childrenCount, start2 + count);
        if (start2 != end) {
            if (end == childrenCount) {
                for (int i = start2; i < end; i++) {
                    children[i].mParent = null;
                    children[i] = null;
                }
            } else {
                for (int i2 = start2; i2 < end; i2++) {
                    children[i2].mParent = null;
                }
                System.arraycopy(children, end, children, start2, childrenCount - end);
                for (int i3 = childrenCount - (end - start2); i3 < childrenCount; i3++) {
                    children[i3] = null;
                }
            }
            this.mChildrenCount -= end - start2;
        }
    }

    private void bindLayoutAnimation(View child) {
        child.setAnimation(this.mLayoutAnimationController.getAnimationForView(child));
    }

    /* access modifiers changed from: protected */
    public void attachLayoutAnimationParameters(View child, LayoutParams params, int index, int count) {
        LayoutAnimationController.AnimationParameters animationParams = params.layoutAnimationParameters;
        if (animationParams == null) {
            animationParams = new LayoutAnimationController.AnimationParameters();
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
            return false;
        }
        removeViewInternal(index, view);
        return true;
    }

    private void removeViewInternal(int index, View view) {
        if (this.mTransition != null) {
            this.mTransition.removeChild(this, view);
        }
        boolean clearChildFocus = false;
        if (view == this.mFocused) {
            view.unFocus(null);
            clearChildFocus = true;
        }
        if (view == this.mFocusedInCluster) {
            clearFocusedInCluster(view);
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
            childHasTransientStateChanged(view, false);
        }
        needGlobalAttributesUpdate(false);
        removeFromArray(index);
        if (view.hasUnhandledKeyListener()) {
            decrementChildUnhandledKeyListeners();
        }
        if (view == this.mDefaultFocus) {
            clearDefaultFocus(view);
        }
        if (clearChildFocus) {
            clearChildFocus(view);
            if (!rootViewRequestFocus()) {
                notifyGlobalFocusCleared(this);
            }
        }
        dispatchViewRemoved(view);
        if (view.getVisibility() != 8) {
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
        int transientCount = this.mTransientIndices == null ? 0 : this.mTransientIndices.size();
        for (int i = 0; i < transientCount; i++) {
            int oldIndex = this.mTransientIndices.get(i).intValue();
            if (index < oldIndex) {
                this.mTransientIndices.set(i, Integer.valueOf(oldIndex - 1));
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
        boolean detach = this.mAttachInfo != null;
        View clearDefaultFocus = null;
        View[] children = this.mChildren;
        boolean clearChildFocus = false;
        for (int i = start; i < end; i++) {
            View view = children[i];
            if (this.mTransition != null) {
                this.mTransition.removeChild(this, view);
            }
            if (view == focused) {
                view.unFocus(null);
                clearChildFocus = true;
            }
            if (view == this.mDefaultFocus) {
                clearDefaultFocus = view;
            }
            if (view == this.mFocusedInCluster) {
                clearFocusedInCluster(view);
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
                childHasTransientStateChanged(view, false);
            }
            needGlobalAttributesUpdate(false);
            dispatchViewRemoved(view);
        }
        removeFromArray(start, count);
        if (clearDefaultFocus != null) {
            clearDefaultFocus(clearDefaultFocus);
        }
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
            this.mChildrenCount = 0;
            View focused = this.mFocused;
            boolean detach = this.mAttachInfo != null;
            boolean clearChildFocus = false;
            needGlobalAttributesUpdate(false);
            for (int i = count - 1; i >= 0; i--) {
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
                    childHasTransientStateChanged(view, false);
                }
                dispatchViewRemoved(view);
                view.mParent = null;
                children[i] = null;
            }
            if (this.mDefaultFocus != null) {
                clearDefaultFocus(this.mDefaultFocus);
            }
            if (this.mFocusedInCluster != null) {
                clearFocusedInCluster(this.mFocusedInCluster);
            }
            if (clearChildFocus) {
                clearChildFocus(focused);
                if (!rootViewRequestFocus()) {
                    notifyGlobalFocusCleared(focused);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeDetachedView(View child, boolean animate) {
        if (this.mTransition != null) {
            this.mTransition.removeChild(this, child);
        }
        if (child == this.mFocused) {
            child.clearFocus();
        }
        if (child == this.mDefaultFocus) {
            clearDefaultFocus(child);
        }
        if (child == this.mFocusedInCluster) {
            clearFocusedInCluster(child);
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
            childHasTransientStateChanged(child, false);
        }
        dispatchViewRemoved(child);
    }

    /* access modifiers changed from: protected */
    public void attachViewToParent(View child, int index, LayoutParams params) {
        child.mLayoutParams = params;
        if (index < 0) {
            index = this.mChildrenCount;
        }
        addInArray(child, index);
        child.mParent = this;
        child.mPrivateFlags = (child.mPrivateFlags & -6291457 & -32769) | 32 | Integer.MIN_VALUE;
        this.mPrivateFlags |= Integer.MIN_VALUE;
        if (child.hasFocus()) {
            requestChildFocus(child, child.findFocus());
        }
        dispatchVisibilityAggregated(isAttachedToWindow() && getWindowVisibility() == 0 && isShown());
        notifySubtreeAccessibilityStateChangedIfNeeded();
    }

    /* access modifiers changed from: protected */
    public void detachViewFromParent(View child) {
        removeFromArray(indexOfChild(child));
    }

    /* access modifiers changed from: protected */
    public void detachViewFromParent(int index) {
        removeFromArray(index);
    }

    /* access modifiers changed from: protected */
    public void detachViewsFromParent(int start, int count) {
        removeFromArray(start, count);
    }

    /* access modifiers changed from: protected */
    public void detachAllViewsFromParent() {
        int count = this.mChildrenCount;
        if (count > 0) {
            View[] children = this.mChildren;
            this.mChildrenCount = 0;
            for (int i = count - 1; i >= 0; i--) {
                children[i].mParent = null;
                children[i] = null;
            }
        }
    }

    public void onDescendantInvalidated(View child, View target) {
        this.mPrivateFlags |= target.mPrivateFlags & 64;
        if ((target.mPrivateFlags & -6291457) != 0) {
            this.mPrivateFlags = (this.mPrivateFlags & -6291457) | 2097152;
            this.mPrivateFlags &= -32769;
        }
        if (this.mLayerType == 1) {
            this.mPrivateFlags |= -2145386496;
            target = this;
        }
        if (this.mParent != null) {
            this.mParent.onDescendantInvalidated(this, target);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x0102  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0123  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x012b  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0160 A[SYNTHETIC] */
    @Deprecated
    public final void invalidateChild(View child, Rect dirty) {
        Matrix transformMatrix;
        Matrix transformMatrix2;
        View view = child;
        Rect rect = dirty;
        View.AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null && attachInfo.mHardwareAccelerated) {
            ViewRootImpl impl = view != null ? child.getViewRootImpl() : null;
            if (impl == null || !impl.mIsDropEmptyFrame || Thread.currentThread() != impl.mThread) {
                onDescendantInvalidated(view, view);
                return;
            }
            impl.mBeingInvalidatedChild = view;
            onDescendantInvalidated(view, view);
            impl.mBeingInvalidatedChild = null;
        }
        ViewParent parent = this;
        if (attachInfo != null) {
            boolean drawAnimation = (view.mPrivateFlags & 64) != 0;
            Matrix childMatrix = child.getMatrix();
            int opaqueFlag = child.isOpaque() && !drawAnimation && child.getAnimation() == null && childMatrix.isIdentity() ? 4194304 : 2097152;
            if (view.mLayerType != 0) {
                this.mPrivateFlags |= Integer.MIN_VALUE;
                this.mPrivateFlags &= -32769;
            }
            int[] location = attachInfo.mInvalidateChildLocation;
            location[0] = view.mLeft;
            location[1] = view.mTop;
            if (!childMatrix.isIdentity() || (this.mGroupFlags & 2048) != 0) {
                RectF boundingRect = attachInfo.mTmpTransformRect;
                boundingRect.set(rect);
                if ((this.mGroupFlags & 2048) != 0) {
                    Transformation t = attachInfo.mTmpTransformation;
                    if (getChildStaticTransformation(view, t)) {
                        transformMatrix2 = attachInfo.mTmpMatrix;
                        transformMatrix2.set(t.getMatrix());
                        if (!childMatrix.isIdentity()) {
                            transformMatrix2.preConcat(childMatrix);
                        }
                    } else {
                        transformMatrix2 = childMatrix;
                    }
                    transformMatrix = transformMatrix2;
                } else {
                    transformMatrix = childMatrix;
                }
                transformMatrix.mapRect(boundingRect);
                Matrix matrix = childMatrix;
                rect.set((int) Math.floor((double) boundingRect.left), (int) Math.floor((double) boundingRect.top), (int) Math.ceil((double) boundingRect.right), (int) Math.ceil((double) boundingRect.bottom));
            } else {
                Matrix matrix2 = childMatrix;
            }
            do {
                View view2 = null;
                if (parent instanceof View) {
                    view2 = (View) parent;
                }
                if (drawAnimation) {
                    if (view2 != null) {
                        view2.mPrivateFlags |= 64;
                    } else if (parent instanceof ViewRootImpl) {
                        ((ViewRootImpl) parent).mIsAnimating = true;
                        if (view2 == null) {
                            if ((view2.mViewFlags & 12288) != 0 && view2.getSolidColor() == 0) {
                                opaqueFlag = 2097152;
                            }
                            if ((view2.mPrivateFlags & 6291456) != 2097152) {
                                view2.mPrivateFlags = (view2.mPrivateFlags & -6291457) | opaqueFlag;
                            }
                        }
                        parent = parent.invalidateChildInParent(location, rect);
                        if (view2 == null) {
                            Matrix m = view2.getMatrix();
                            if (!m.isIdentity()) {
                                RectF boundingRect2 = attachInfo.mTmpTransformRect;
                                boundingRect2.set(rect);
                                m.mapRect(boundingRect2);
                                rect.set((int) Math.floor((double) boundingRect2.left), (int) Math.floor((double) boundingRect2.top), (int) Math.ceil((double) boundingRect2.right), (int) Math.ceil((double) boundingRect2.bottom));
                                continue;
                            } else {
                                continue;
                            }
                        }
                    }
                }
                if (view2 == null) {
                }
                parent = parent.invalidateChildInParent(location, rect);
                if (view2 == null) {
                }
            } while (parent != null);
        }
    }

    @Deprecated
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        if ((this.mPrivateFlags & 32800) == 0) {
            return null;
        }
        if ((this.mGroupFlags & 144) != 128) {
            dirty.offset(location[0] - this.mScrollX, location[1] - this.mScrollY);
            if ((this.mGroupFlags & 1) == 0) {
                dirty.union(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop);
            }
            int left = this.mLeft;
            int top = this.mTop;
            if ((this.mGroupFlags & 1) == 1 && !dirty.intersect(0, 0, this.mRight - left, this.mBottom - top)) {
                dirty.setEmpty();
            }
            location[0] = left;
            location[1] = top;
        } else {
            if ((this.mGroupFlags & 1) == 1) {
                dirty.set(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop);
            } else {
                dirty.union(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop);
            }
            location[0] = this.mLeft;
            location[1] = this.mTop;
            this.mPrivateFlags &= -33;
        }
        this.mPrivateFlags &= -32769;
        if (this.mLayerType != 0) {
            this.mPrivateFlags |= Integer.MIN_VALUE;
        }
        return this.mParent;
    }

    public final void offsetDescendantRectToMyCoords(View descendant, Rect rect) {
        offsetRectBetweenParentAndChild(descendant, rect, true, false);
    }

    public final void offsetRectIntoDescendantCoords(View descendant, Rect rect) {
        offsetRectBetweenParentAndChild(descendant, rect, false, false);
    }

    /* access modifiers changed from: package-private */
    public void offsetRectBetweenParentAndChild(View descendant, Rect rect, boolean offsetFromChildToParent, boolean clipToBounds) {
        if (descendant != this) {
            ViewParent theParent = descendant.mParent;
            while (theParent != null && (theParent instanceof View) && theParent != this) {
                if (offsetFromChildToParent) {
                    rect.offset(descendant.mLeft - descendant.mScrollX, descendant.mTop - descendant.mScrollY);
                    if (clipToBounds) {
                        View p = (View) theParent;
                        if (!rect.intersect(0, 0, p.mRight - p.mLeft, p.mBottom - p.mTop)) {
                            rect.setEmpty();
                        }
                    }
                } else {
                    if (clipToBounds) {
                        View p2 = (View) theParent;
                        if (!rect.intersect(0, 0, p2.mRight - p2.mLeft, p2.mBottom - p2.mTop)) {
                            rect.setEmpty();
                        }
                    }
                    rect.offset(descendant.mScrollX - descendant.mLeft, descendant.mScrollY - descendant.mTop);
                }
                descendant = (View) theParent;
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
        boolean invalidate = false;
        for (int i = 0; i < count; i++) {
            View v = children[i];
            v.mTop += offset;
            v.mBottom += offset;
            if (v.mRenderNode != null) {
                invalidate = true;
                v.mRenderNode.offsetTopAndBottom(offset);
            }
        }
        if (invalidate) {
            invalidateViewProperty(false, false);
        }
        notifySubtreeAccessibilityStateChangedIfNeeded();
    }

    public boolean getChildVisibleRect(View child, Rect r, Point offset) {
        return getChildVisibleRect(child, r, offset, false);
    }

    public boolean getChildVisibleRect(View child, Rect r, Point offset, boolean forceParentCheck) {
        float[] position;
        RectF rect = this.mAttachInfo != null ? this.mAttachInfo.mTmpTransformRect : new RectF();
        rect.set(r);
        if (!child.hasIdentityMatrix()) {
            child.getMatrix().mapRect(rect);
        }
        int dx = child.mLeft - this.mScrollX;
        int dy = child.mTop - this.mScrollY;
        rect.offset((float) dx, (float) dy);
        if (offset != null) {
            if (!child.hasIdentityMatrix()) {
                if (this.mAttachInfo != null) {
                    position = this.mAttachInfo.mTmpTransformLocation;
                } else {
                    position = new float[2];
                }
                position[0] = (float) offset.x;
                position[1] = (float) offset.y;
                child.getMatrix().mapPoints(position);
                offset.x = Math.round(position[0]);
                offset.y = Math.round(position[1]);
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
        if ((forceParentCheck || rectIsVisible) && (this.mGroupFlags & 34) == 34) {
            rectIsVisible = rect.intersect((float) this.mPaddingLeft, (float) this.mPaddingTop, (float) (width - this.mPaddingRight), (float) (height - this.mPaddingBottom));
        }
        if ((forceParentCheck || rectIsVisible) && this.mClipBounds != null) {
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

    /* access modifiers changed from: protected */
    public boolean canAnimate() {
        return this.mLayoutAnimationController != null;
    }

    public void startLayoutAnimation() {
        if (this.mLayoutAnimationController != null) {
            this.mGroupFlags |= 8;
            requestLayout();
        }
    }

    public void scheduleLayoutAnimation() {
        this.mGroupFlags |= 8;
    }

    public void setLayoutAnimation(LayoutAnimationController controller) {
        this.mLayoutAnimationController = controller;
        if (this.mLayoutAnimationController != null) {
            this.mGroupFlags |= 8;
        }
    }

    public LayoutAnimationController getLayoutAnimation() {
        return this.mLayoutAnimationController;
    }

    @Deprecated
    public boolean isAnimationCacheEnabled() {
        return (this.mGroupFlags & 64) == 64;
    }

    @Deprecated
    public void setAnimationCacheEnabled(boolean enabled) {
        setBooleanFlag(64, enabled);
    }

    @Deprecated
    public boolean isAlwaysDrawnWithCacheEnabled() {
        return (this.mGroupFlags & 16384) == 16384;
    }

    @Deprecated
    public void setAlwaysDrawnWithCacheEnabled(boolean always) {
        setBooleanFlag(16384, always);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean isChildrenDrawnWithCacheEnabled() {
        return (this.mGroupFlags & 32768) == 32768;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        setBooleanFlag(32768, enabled);
    }

    /* access modifiers changed from: protected */
    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean isChildrenDrawingOrderEnabled() {
        return (this.mGroupFlags & 1024) == 1024;
    }

    /* access modifiers changed from: protected */
    public void setChildrenDrawingOrderEnabled(boolean enabled) {
        setBooleanFlag(1024, enabled);
    }

    private boolean hasBooleanFlag(int flag) {
        return (this.mGroupFlags & flag) == flag;
    }

    private void setBooleanFlag(int flag, boolean value) {
        if (value) {
            this.mGroupFlags |= flag;
        } else {
            this.mGroupFlags &= ~flag;
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing", mapping = {@ViewDebug.IntToString(from = 0, to = "NONE"), @ViewDebug.IntToString(from = 1, to = "ANIMATION"), @ViewDebug.IntToString(from = 2, to = "SCROLLING"), @ViewDebug.IntToString(from = 3, to = "ALL")})
    @Deprecated
    public int getPersistentDrawingCache() {
        return this.mPersistentDrawingCache;
    }

    @Deprecated
    public void setPersistentDrawingCache(int drawingCacheToKeep) {
        this.mPersistentDrawingCache = drawingCacheToKeep & 3;
    }

    private void setLayoutMode(int layoutMode, boolean explicitly) {
        this.mLayoutMode = layoutMode;
        setBooleanFlag(8388608, explicitly);
    }

    /* access modifiers changed from: package-private */
    public void invalidateInheritedLayoutMode(int layoutModeOfRoot) {
        if (this.mLayoutMode != -1 && this.mLayoutMode != layoutModeOfRoot && !hasBooleanFlag(8388608)) {
            setLayoutMode(-1, false);
            int N = getChildCount();
            for (int i = 0; i < N; i++) {
                getChildAt(i).invalidateInheritedLayoutMode(layoutModeOfRoot);
            }
        }
    }

    public int getLayoutMode() {
        if (this.mLayoutMode == -1) {
            setLayoutMode(this.mParent instanceof ViewGroup ? ((ViewGroup) this.mParent).getLayoutMode() : LAYOUT_MODE_DEFAULT, false);
        }
        return this.mLayoutMode;
    }

    public void setLayoutMode(int layoutMode) {
        if (this.mLayoutMode != layoutMode) {
            invalidateInheritedLayoutMode(layoutMode);
            setLayoutMode(layoutMode, layoutMode != -1);
            requestLayout();
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateLayoutParams(LayoutParams p) {
        return p;
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    /* access modifiers changed from: protected */
    public void debug(int depth) {
        super.debug(depth);
        if (this.mFocused != null) {
            String output = debugIndent(depth);
            Log.d("View", output + "mFocused");
            this.mFocused.debug(depth + 1);
        }
        if (this.mDefaultFocus != null) {
            String output2 = debugIndent(depth);
            Log.d("View", output2 + "mDefaultFocus");
            this.mDefaultFocus.debug(depth + 1);
        }
        if (this.mFocusedInCluster != null) {
            String output3 = debugIndent(depth);
            Log.d("View", output3 + "mFocusedInCluster");
            this.mFocusedInCluster.debug(depth + 1);
        }
        if (this.mChildrenCount != 0) {
            String output4 = debugIndent(depth);
            Log.d("View", output4 + "{");
        }
        int count = this.mChildrenCount;
        for (int i = 0; i < count; i++) {
            this.mChildren[i].debug(depth + 1);
        }
        if (this.mChildrenCount != 0) {
            String output5 = debugIndent(depth);
            Log.d("View", output5 + "}");
        }
    }

    public int indexOfChild(View child) {
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            if (children[i] == child) {
                return i;
            }
        }
        return -1;
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

    /* access modifiers changed from: protected */
    public void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
        int size = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < size; i++) {
            View child = children[i];
            if ((child.mViewFlags & 12) != 8) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        LayoutParams lp = child.getLayoutParams();
        child.measure(getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight, lp.width), getChildMeasureSpec(parentHeightMeasureSpec, this.mPaddingTop + this.mPaddingBottom, lp.height));
    }

    /* access modifiers changed from: protected */
    public void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        child.measure(getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight + lp.leftMargin + lp.rightMargin + widthUsed, lp.width), getChildMeasureSpec(parentHeightMeasureSpec, this.mPaddingTop + this.mPaddingBottom + lp.topMargin + lp.bottomMargin + heightUsed, lp.height));
    }

    public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
        int specMode = View.MeasureSpec.getMode(spec);
        int i = 0;
        int size = Math.max(0, View.MeasureSpec.getSize(spec) - padding);
        int resultSize = 0;
        int resultMode = 0;
        if (specMode != Integer.MIN_VALUE) {
            if (specMode != 0) {
                if (specMode == 1073741824) {
                    if (childDimension >= 0) {
                        resultSize = childDimension;
                        resultMode = 1073741824;
                    } else if (childDimension == -1) {
                        resultSize = size;
                        resultMode = 1073741824;
                    } else if (childDimension == -2) {
                        resultSize = size;
                        resultMode = Integer.MIN_VALUE;
                    }
                }
            } else if (childDimension >= 0) {
                resultSize = childDimension;
                resultMode = 1073741824;
            } else if (childDimension == -1) {
                if (!View.sUseZeroUnspecifiedMeasureSpec) {
                    i = size;
                }
                resultSize = i;
                resultMode = 0;
            } else if (childDimension == -2) {
                if (!View.sUseZeroUnspecifiedMeasureSpec) {
                    i = size;
                }
                resultSize = i;
                resultMode = 0;
            }
        } else if (childDimension >= 0) {
            resultSize = childDimension;
            resultMode = 1073741824;
        } else if (childDimension == -1) {
            resultSize = size;
            resultMode = Integer.MIN_VALUE;
        } else if (childDimension == -2) {
            resultSize = size;
            resultMode = Integer.MIN_VALUE;
        }
        return View.MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }

    public void clearDisappearingChildren() {
        ArrayList<View> disappearingChildren = this.mDisappearingChildren;
        if (disappearingChildren != null) {
            int count = disappearingChildren.size();
            for (int i = 0; i < count; i++) {
                View view = disappearingChildren.get(i);
                if (view != null) {
                    if (view.mAttachInfo != null) {
                        view.dispatchDetachedFromWindow();
                    }
                    view.clearAnimation();
                }
            }
            disappearingChildren.clear();
            invalidate();
        }
    }

    private void addDisappearingView(View v) {
        ArrayList<View> disappearingChildren = this.mDisappearingChildren;
        if (disappearingChildren == null) {
            ArrayList<View> arrayList = new ArrayList<>();
            this.mDisappearingChildren = arrayList;
            disappearingChildren = arrayList;
        }
        disappearingChildren.add(v);
    }

    /* access modifiers changed from: package-private */
    public void finishAnimatingView(View view, Animation animation) {
        ArrayList<View> disappearingChildren = this.mDisappearingChildren;
        if (disappearingChildren != null && disappearingChildren.contains(view)) {
            disappearingChildren.remove(view);
            if (view.mAttachInfo != null) {
                view.dispatchDetachedFromWindow();
            }
            view.clearAnimation();
            this.mGroupFlags |= 4;
        }
        if (animation != null && !animation.getFillAfter()) {
            view.clearAnimation();
        }
        if ((view.mPrivateFlags & 65536) == 65536) {
            view.onAnimationEnd();
            view.mPrivateFlags &= -65537;
            this.mGroupFlags |= 4;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isViewTransitioning(View view) {
        return this.mTransitioningViews != null && this.mTransitioningViews.contains(view);
    }

    public void startViewTransition(View view) {
        if (view.mParent == this) {
            if (this.mTransitioningViews == null) {
                this.mTransitioningViews = new ArrayList<>();
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
            this.mLayoutCalledWhileSuppressed = false;
        }
    }

    public boolean isLayoutSuppressed() {
        return this.mSuppressLayout;
    }

    public boolean gatherTransparentRegion(Region region) {
        boolean z = false;
        boolean meOpaque = (this.mPrivateFlags & 512) == 0;
        if (meOpaque && region == null) {
            return true;
        }
        super.gatherTransparentRegion(region);
        int childrenCount = this.mChildrenCount;
        boolean noneOfTheChildrenAreTransparent = true;
        if (childrenCount > 0) {
            ArrayList<View> preorderedList = buildOrderedChildList();
            boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
            View[] children = this.mChildren;
            boolean noneOfTheChildrenAreTransparent2 = true;
            for (int i = 0; i < childrenCount; i++) {
                View child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, customOrder));
                if (((child.mViewFlags & 12) == 0 || child.getAnimation() != null) && !child.gatherTransparentRegion(region)) {
                    noneOfTheChildrenAreTransparent2 = false;
                }
            }
            if (preorderedList != null) {
                preorderedList.clear();
            }
            noneOfTheChildrenAreTransparent = noneOfTheChildrenAreTransparent2;
        }
        if (meOpaque || noneOfTheChildrenAreTransparent) {
            z = true;
        }
        return z;
    }

    public void requestTransparentRegion(View child) {
        if (child != null) {
            child.mPrivateFlags |= 512;
            if (this.mParent != null) {
                this.mParent.requestTransparentRegion(this);
            }
        }
    }

    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        WindowInsets insets2 = super.dispatchApplyWindowInsets(insets);
        if (!insets2.isConsumed()) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                insets2 = getChildAt(i).dispatchApplyWindowInsets(insets2);
                if (insets2.isConsumed()) {
                    break;
                }
            }
        }
        return insets2;
    }

    public Animation.AnimationListener getLayoutAnimationListener() {
        return this.mAnimationListener;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        if ((this.mGroupFlags & 65536) == 0) {
            return;
        }
        if ((this.mGroupFlags & 8192) == 0) {
            View[] children = this.mChildren;
            int count = this.mChildrenCount;
            for (int i = 0; i < count; i++) {
                View child = children[i];
                if ((child.mViewFlags & 4194304) != 0) {
                    child.refreshDrawableState();
                }
            }
            return;
        }
        throw new IllegalStateException("addStateFromChildren cannot be enabled if a child has duplicateParentState set to true");
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = 0; i < count; i++) {
            if (children[i] != null) {
                children[i].jumpDrawablesToCurrentState();
            }
        }
    }

    /* access modifiers changed from: protected */
    public int[] onCreateDrawableState(int extraSpace) {
        if ((this.mGroupFlags & 8192) == 0) {
            return super.onCreateDrawableState(extraSpace);
        }
        int n = getChildCount();
        int need = 0;
        for (int i = 0; i < n; i++) {
            int[] childState = getChildAt(i).getDrawableState();
            if (childState != null) {
                need += childState.length;
            }
        }
        int[] state = super.onCreateDrawableState(extraSpace + need);
        for (int i2 = 0; i2 < n; i2++) {
            int[] childState2 = getChildAt(i2).getDrawableState();
            if (childState2 != null) {
                state = mergeDrawableStates(state, childState2);
            }
        }
        return state;
    }

    public void setAddStatesFromChildren(boolean addsStates) {
        if (addsStates) {
            this.mGroupFlags |= 8192;
        } else {
            this.mGroupFlags &= -8193;
        }
        refreshDrawableState();
    }

    public boolean addStatesFromChildren() {
        return (this.mGroupFlags & 8192) != 0;
    }

    public void childDrawableStateChanged(View child) {
        if ((this.mGroupFlags & 8192) != 0) {
            refreshDrawableState();
        }
    }

    public void setLayoutAnimationListener(Animation.AnimationListener animationListener) {
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
            for (int i = 0; i < count; i++) {
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
            for (int i = 0; i < count; i++) {
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
            for (int i = 0; i < count; i++) {
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
            for (int i = 0; i < count; i++) {
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
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.isLayoutDirectionInherited() && !child.isPaddingResolved()) {
                child.resolvePadding();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void resolveDrawables() {
        super.resolveDrawables();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.isLayoutDirectionInherited() && !child.areDrawablesResolved()) {
                child.resolveDrawables();
            }
        }
    }

    public void resolveLayoutParams() {
        super.resolveLayoutParams();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).resolveLayoutParams();
        }
    }

    public void resetResolvedLayoutDirection() {
        super.resetResolvedLayoutDirection();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.isLayoutDirectionInherited()) {
                child.resetResolvedLayoutDirection();
            }
        }
    }

    public void resetResolvedTextDirection() {
        super.resetResolvedTextDirection();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.isTextDirectionInherited()) {
                child.resetResolvedTextDirection();
            }
        }
    }

    public void resetResolvedTextAlignment() {
        super.resetResolvedTextAlignment();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.isTextAlignmentInherited()) {
                child.resetResolvedTextAlignment();
            }
        }
    }

    public void resetResolvedPadding() {
        super.resetResolvedPadding();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.isLayoutDirectionInherited()) {
                child.resetResolvedPadding();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void resetResolvedDrawables() {
        super.resetResolvedDrawables();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
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
        return false;
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        this.mNestedScrollAxes = axes;
    }

    public void onStopNestedScroll(View child) {
        stopNestedScroll();
        this.mNestedScrollAxes = 0;
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

    /* access modifiers changed from: protected */
    public void onSetLayoutParams(View child, LayoutParams layoutParams) {
        requestLayout();
    }

    public void captureTransitioningViews(List<View> transitioningViews) {
        if (getVisibility() == 0) {
            if (isTransitionGroup()) {
                transitioningViews.add(this);
            } else {
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    getChildAt(i).captureTransitioningViews(transitioningViews);
                }
            }
        }
    }

    public void findNamedViews(Map<String, View> namedElements) {
        if (getVisibility() == 0 || this.mGhostView != null) {
            super.findNamedViews(namedElements);
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).findNamedViews(namedElements);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasUnhandledKeyListener() {
        return this.mChildUnhandledKeyListeners > 0 || super.hasUnhandledKeyListener();
    }

    /* access modifiers changed from: package-private */
    public void incrementChildUnhandledKeyListeners() {
        this.mChildUnhandledKeyListeners++;
        if (this.mChildUnhandledKeyListeners == 1 && (this.mParent instanceof ViewGroup)) {
            ((ViewGroup) this.mParent).incrementChildUnhandledKeyListeners();
        }
    }

    /* access modifiers changed from: package-private */
    public void decrementChildUnhandledKeyListeners() {
        this.mChildUnhandledKeyListeners--;
        if (this.mChildUnhandledKeyListeners == 0 && (this.mParent instanceof ViewGroup)) {
            ((ViewGroup) this.mParent).decrementChildUnhandledKeyListeners();
        }
    }

    /* access modifiers changed from: package-private */
    public View dispatchUnhandledKeyEvent(KeyEvent evt) {
        if (!hasUnhandledKeyListener()) {
            return null;
        }
        ArrayList<View> orderedViews = buildOrderedChildList();
        if (orderedViews != null) {
            try {
                for (int i = orderedViews.size() - 1; i >= 0; i--) {
                    View consumer = orderedViews.get(i).dispatchUnhandledKeyEvent(evt);
                    if (consumer != null) {
                        return consumer;
                    }
                }
                orderedViews.clear();
            } finally {
                orderedViews.clear();
            }
        } else {
            for (int i2 = getChildCount() - 1; i2 >= 0; i2--) {
                View consumer2 = getChildAt(i2).dispatchUnhandledKeyEvent(evt);
                if (consumer2 != null) {
                    return consumer2;
                }
            }
        }
        if (onUnhandledKeyEvent(evt) != 0) {
            return this;
        }
        return null;
    }

    private static void drawRect(Canvas canvas, Paint paint, int x1, int y1, int x2, int y2) {
        if (sDebugLines == null) {
            sDebugLines = new float[16];
        }
        sDebugLines[0] = (float) x1;
        sDebugLines[1] = (float) y1;
        sDebugLines[2] = (float) x2;
        sDebugLines[3] = (float) y1;
        sDebugLines[4] = (float) x2;
        sDebugLines[5] = (float) y1;
        sDebugLines[6] = (float) x2;
        sDebugLines[7] = (float) y2;
        sDebugLines[8] = (float) x2;
        sDebugLines[9] = (float) y2;
        sDebugLines[10] = (float) x1;
        sDebugLines[11] = (float) y2;
        sDebugLines[12] = (float) x1;
        sDebugLines[13] = (float) y2;
        sDebugLines[14] = (float) x1;
        sDebugLines[15] = (float) y1;
        canvas.drawLines(sDebugLines, paint);
    }

    /* access modifiers changed from: protected */
    public void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("focus:descendantFocusability", getDescendantFocusability());
        encoder.addProperty("drawing:clipChildren", getClipChildren());
        encoder.addProperty("drawing:clipToPadding", getClipToPadding());
        encoder.addProperty("drawing:childrenDrawingOrderEnabled", isChildrenDrawingOrderEnabled());
        encoder.addProperty("drawing:persistentDrawingCache", getPersistentDrawingCache());
        int n = getChildCount();
        encoder.addProperty("meta:__childCount__", (short) n);
        for (int i = 0; i < n; i++) {
            encoder.addPropertyKey("meta:__child__" + i);
            getChildAt(i).encode(encoder);
        }
    }
}
