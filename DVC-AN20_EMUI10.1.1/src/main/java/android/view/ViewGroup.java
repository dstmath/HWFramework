package android.view;

import android.animation.LayoutTransition;
import android.annotation.UnsupportedAppUsage;
import android.app.WindowConfiguration;
import android.common.HwFrameworkFactory;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.freeform.HwFreeFormUtils;
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
import android.hwcontrol.HwWidgetFactory;
import android.media.TtmlUtils;
import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pools;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewDebug;
import android.view.WindowInsetsAnimationListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Transformation;
import android.view.autofill.Helper;
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import android.widget.HwWidgetAppAttrsHelper;
import com.android.internal.R;
import com.android.internal.policy.DecorView;
import java.lang.annotation.RCUnownedThisRef;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public abstract class ViewGroup extends View implements ViewParent, ViewManager {
    private static final int ARRAY_CAPACITY_INCREMENT = 12;
    private static final int ARRAY_INITIAL_CAPACITY = 12;
    private static final int CHILD_LEFT_INDEX = 0;
    private static final int CHILD_TOP_INDEX = 1;
    protected static final int CLIP_TO_PADDING_MASK = 34;
    @UnsupportedAppUsage
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
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123983692)
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
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769647)
    protected static final int FLAG_SUPPORT_STATIC_TRANSFORMATIONS = 2048;
    static final int FLAG_TOUCHSCREEN_BLOCKS_FOCUS = 67108864;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769377)
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
        /* class android.view.ViewGroup.AnonymousClass1 */

        @Override // android.view.ActionMode
        public void setTitle(CharSequence title) {
        }

        @Override // android.view.ActionMode
        public void setTitle(int resId) {
        }

        @Override // android.view.ActionMode
        public void setSubtitle(CharSequence subtitle) {
        }

        @Override // android.view.ActionMode
        public void setSubtitle(int resId) {
        }

        @Override // android.view.ActionMode
        public void setCustomView(View view) {
        }

        @Override // android.view.ActionMode
        public void invalidate() {
        }

        @Override // android.view.ActionMode
        public void finish() {
        }

        @Override // android.view.ActionMode
        public Menu getMenu() {
            return null;
        }

        @Override // android.view.ActionMode
        public CharSequence getTitle() {
            return null;
        }

        @Override // android.view.ActionMode
        public CharSequence getSubtitle() {
            return null;
        }

        @Override // android.view.ActionMode
        public View getCustomView() {
            return null;
        }

        @Override // android.view.ActionMode
        public MenuInflater getMenuInflater() {
            return null;
        }
    };
    private static final String TAG = "ViewGroup";
    private static float[] sDebugLines;
    private Animation.AnimationListener mAnimationListener;
    private HwWidgetAppAttrsHelper mAppAttrsHelper;
    Paint mCachePaint;
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    private int mChildCountWithTransientState;
    private Transformation mChildTransformation;
    int mChildUnhandledKeyListeners;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private View[] mChildren;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private int mChildrenCount;
    private HashSet<View> mChildrenInterestedInDrag;
    private View mCurrentDragChild;
    private DragEvent mCurrentDragStartEvent;
    private View mDefaultFocus;
    @UnsupportedAppUsage
    protected ArrayList<View> mDisappearingChildren;
    private HoverTarget mFirstHoverTarget;
    @UnsupportedAppUsage
    private TouchTarget mFirstTouchTarget;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private View mFocused;
    View mFocusedInCluster;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769411)
    @ViewDebug.ExportedProperty(flagMapping = {@ViewDebug.FlagToString(equals = 1, mask = 1, name = "CLIP_CHILDREN"), @ViewDebug.FlagToString(equals = 2, mask = 2, name = "CLIP_TO_PADDING"), @ViewDebug.FlagToString(equals = 32, mask = 32, name = "PADDING_NOT_NULL")}, formatToHexString = true)
    protected int mGroupFlags;
    private boolean mHoveredSelf;
    RectF mInvalidateRegion;
    Transformation mInvalidationTransformation;
    private boolean mIsInterestedInDrag;
    private boolean mIsSmoothScrollInit;
    @ViewDebug.ExportedProperty(category = "events")
    private int mLastTouchDownIndex;
    @ViewDebug.ExportedProperty(category = "events")
    private long mLastTouchDownTime;
    @ViewDebug.ExportedProperty(category = "events")
    private float mLastTouchDownX;
    @ViewDebug.ExportedProperty(category = "events")
    private float mLastTouchDownY;
    private LayoutAnimationController mLayoutAnimationController;
    private boolean mLayoutCalledWhileSuppressed;
    private int mLayoutMode;
    private LayoutTransition.TransitionListener mLayoutTransitionListener;
    private PointF mLocalPoint;
    private int mNestedScrollAxes;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123768704)
    protected OnHierarchyChangeListener mOnHierarchyChangeListener;
    @UnsupportedAppUsage
    protected int mPersistentDrawingCache;
    private ArrayList<View> mPreSortedChildren;
    private Method mScrollToPosition;
    private Method mSmoothScroll;
    boolean mSuppressLayout;
    private float[] mTempPoint;
    private View mTooltipHoverTarget;
    private boolean mTooltipHoveredSelf;
    private List<Integer> mTransientIndices;
    private List<View> mTransientViews;
    private LayoutTransition mTransition;
    private ArrayList<View> mTransitioningViews;
    private ArrayList<View> mVisibilityChangingChildren;

    public interface OnHierarchyChangeListener {
        void onChildViewAdded(View view, View view2);

        void onChildViewRemoved(View view, View view2);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public abstract void onLayout(boolean z, int i, int i2, int i3, int i4);

    public static class LayoutParams {
        @Deprecated
        public static final int FILL_PARENT = -1;
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;
        @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT, mapping = {@ViewDebug.IntToString(from = -1, to = "MATCH_PARENT"), @ViewDebug.IntToString(from = -2, to = "WRAP_CONTENT")})
        public int height;
        public LayoutAnimationController.AnimationParameters layoutAnimationParameters;
        @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT, mapping = {@ViewDebug.IntToString(from = -1, to = "MATCH_PARENT"), @ViewDebug.IntToString(from = -2, to = "WRAP_CONTENT")})
        public int width;

        public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<LayoutParams> {
            private int mLayout_heightId;
            private int mLayout_widthId;
            private boolean mPropertiesMapped = false;

            @Override // android.view.inspector.InspectionCompanion
            public void mapProperties(PropertyMapper propertyMapper) {
                SparseArray<String> layout_heightEnumMapping = new SparseArray<>();
                layout_heightEnumMapping.put(-2, "wrap_content");
                layout_heightEnumMapping.put(-1, "match_parent");
                Objects.requireNonNull(layout_heightEnumMapping);
                this.mLayout_heightId = propertyMapper.mapIntEnum("layout_height", 16842997, new IntFunction() {
                    /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                    @Override // java.util.function.IntFunction
                    public final Object apply(int i) {
                        return (String) SparseArray.this.get(i);
                    }
                });
                SparseArray<String> layout_widthEnumMapping = new SparseArray<>();
                layout_widthEnumMapping.put(-2, "wrap_content");
                layout_widthEnumMapping.put(-1, "match_parent");
                Objects.requireNonNull(layout_widthEnumMapping);
                this.mLayout_widthId = propertyMapper.mapIntEnum("layout_width", 16842996, new IntFunction() {
                    /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                    @Override // java.util.function.IntFunction
                    public final Object apply(int i) {
                        return (String) SparseArray.this.get(i);
                    }
                });
                this.mPropertiesMapped = true;
            }

            public void readProperties(LayoutParams node, PropertyReader propertyReader) {
                if (this.mPropertiesMapped) {
                    propertyReader.readIntEnum(this.mLayout_heightId, node.height);
                    propertyReader.readIntEnum(this.mLayout_widthId, node.width);
                    return;
                }
                throw new InspectionCompanion.UninitializedPropertyMapException();
            }
        }

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

        @UnsupportedAppUsage
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
        @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
        public int bottomMargin;
        @UnsupportedAppUsage
        @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
        private int endMargin;
        @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
        public int leftMargin;
        @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT, flagMapping = {@ViewDebug.FlagToString(equals = 3, mask = 3, name = "LAYOUT_DIRECTION"), @ViewDebug.FlagToString(equals = 4, mask = 4, name = "LEFT_MARGIN_UNDEFINED_MASK"), @ViewDebug.FlagToString(equals = 8, mask = 8, name = "RIGHT_MARGIN_UNDEFINED_MASK"), @ViewDebug.FlagToString(equals = 16, mask = 16, name = "RTL_COMPATIBILITY_MODE_MASK"), @ViewDebug.FlagToString(equals = 32, mask = 32, name = "NEED_RESOLUTION_MASK")}, formatToHexString = true)
        byte mMarginFlags;
        @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
        public int rightMargin;
        @UnsupportedAppUsage
        @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
        private int startMargin;
        @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
        public int topMargin;

        public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<MarginLayoutParams> {
            private int mLayout_marginBottomId;
            private int mLayout_marginLeftId;
            private int mLayout_marginRightId;
            private int mLayout_marginTopId;
            private boolean mPropertiesMapped = false;

            @Override // android.view.inspector.InspectionCompanion
            public void mapProperties(PropertyMapper propertyMapper) {
                this.mLayout_marginBottomId = propertyMapper.mapInt("layout_marginBottom", 16843002);
                this.mLayout_marginLeftId = propertyMapper.mapInt("layout_marginLeft", 16842999);
                this.mLayout_marginRightId = propertyMapper.mapInt("layout_marginRight", 16843001);
                this.mLayout_marginTopId = propertyMapper.mapInt("layout_marginTop", 16843000);
                this.mPropertiesMapped = true;
            }

            public void readProperties(MarginLayoutParams node, PropertyReader propertyReader) {
                if (this.mPropertiesMapped) {
                    propertyReader.readInt(this.mLayout_marginBottomId, node.bottomMargin);
                    propertyReader.readInt(this.mLayout_marginLeftId, node.leftMargin);
                    propertyReader.readInt(this.mLayout_marginRightId, node.rightMargin);
                    propertyReader.readInt(this.mLayout_marginTopId, node.topMargin);
                    return;
                }
                throw new InspectionCompanion.UninitializedPropertyMapException();
            }
        }

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

        @UnsupportedAppUsage
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
            int i = this.startMargin;
            if (i != Integer.MIN_VALUE) {
                return i;
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
            int i = this.endMargin;
            if (i != Integer.MIN_VALUE) {
                return i;
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
            if (layoutDirection == 0 || layoutDirection == 1) {
                byte b = this.mMarginFlags;
                if (layoutDirection != (b & 3)) {
                    this.mMarginFlags = (byte) (b & -4);
                    this.mMarginFlags = (byte) (this.mMarginFlags | (layoutDirection & 3));
                    if (isMarginRelative()) {
                        this.mMarginFlags = (byte) (this.mMarginFlags | 32);
                    } else {
                        this.mMarginFlags = (byte) (this.mMarginFlags & -33);
                    }
                }
            }
        }

        public int getLayoutDirection() {
            return this.mMarginFlags & 3;
        }

        @Override // android.view.ViewGroup.LayoutParams
        public void resolveLayoutDirection(int layoutDirection) {
            setLayoutDirection(layoutDirection);
            if (isMarginRelative() && (this.mMarginFlags & 32) == 32) {
                doResolveMargins();
            }
        }

        private void doResolveMargins() {
            int i;
            int i2;
            byte b = this.mMarginFlags;
            if ((b & WifiScanner.PnoSettings.PnoNetwork.FLAG_SAME_NETWORK) == 16) {
                if ((b & 4) == 4 && (i2 = this.startMargin) > Integer.MIN_VALUE) {
                    this.leftMargin = i2;
                }
                if ((this.mMarginFlags & 8) == 8 && (i = this.endMargin) > Integer.MIN_VALUE) {
                    this.rightMargin = i;
                }
            } else if ((b & 3) != 1) {
                int i3 = this.startMargin;
                if (i3 <= Integer.MIN_VALUE) {
                    i3 = 0;
                }
                this.leftMargin = i3;
                int i4 = this.endMargin;
                if (i4 <= Integer.MIN_VALUE) {
                    i4 = 0;
                }
                this.rightMargin = i4;
            } else {
                int i5 = this.endMargin;
                if (i5 <= Integer.MIN_VALUE) {
                    i5 = 0;
                }
                this.leftMargin = i5;
                int i6 = this.startMargin;
                if (i6 <= Integer.MIN_VALUE) {
                    i6 = 0;
                }
                this.rightMargin = i6;
            }
            this.mMarginFlags = (byte) (this.mMarginFlags & -33);
        }

        public boolean isLayoutRtl() {
            return (this.mMarginFlags & 3) == 1;
        }

        @Override // android.view.ViewGroup.LayoutParams
        public void onDebugDraw(View view, Canvas canvas, Paint paint) {
            Insets oi = View.isLayoutModeOptical(view.mParent) ? view.getOpticalInsets() : Insets.NONE;
            ViewGroup.fillDifference(canvas, view.getLeft() + oi.left, view.getTop() + oi.top, view.getRight() - oi.right, view.getBottom() - oi.bottom, this.leftMargin, this.topMargin, this.rightMargin, this.bottomMargin, paint);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.ViewGroup.LayoutParams
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

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<ViewGroup> {
        private int mAddStatesFromChildrenId;
        private int mAlwaysDrawnWithCacheId;
        private int mAnimationCacheId;
        private int mClipChildrenId;
        private int mClipToPaddingId;
        private int mDescendantFocusabilityId;
        private int mLayoutAnimationId;
        private int mLayoutModeId;
        private int mPersistentDrawingCacheId;
        private boolean mPropertiesMapped = false;
        private int mSplitMotionEventsId;
        private int mTouchscreenBlocksFocusId;
        private int mTransitionGroupId;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mAddStatesFromChildrenId = propertyMapper.mapBoolean("addStatesFromChildren", 16842992);
            this.mAlwaysDrawnWithCacheId = propertyMapper.mapBoolean("alwaysDrawnWithCache", 16842991);
            this.mAnimationCacheId = propertyMapper.mapBoolean("animationCache", 16842989);
            this.mClipChildrenId = propertyMapper.mapBoolean("clipChildren", 16842986);
            this.mClipToPaddingId = propertyMapper.mapBoolean("clipToPadding", 16842987);
            SparseArray<String> descendantFocusabilityEnumMapping = new SparseArray<>();
            descendantFocusabilityEnumMapping.put(131072, "beforeDescendants");
            descendantFocusabilityEnumMapping.put(262144, "afterDescendants");
            descendantFocusabilityEnumMapping.put(393216, "blocksDescendants");
            Objects.requireNonNull(descendantFocusabilityEnumMapping);
            this.mDescendantFocusabilityId = propertyMapper.mapIntEnum("descendantFocusability", 16842993, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mLayoutAnimationId = propertyMapper.mapObject("layoutAnimation", 16842988);
            SparseArray<String> layoutModeEnumMapping = new SparseArray<>();
            layoutModeEnumMapping.put(0, "clipBounds");
            layoutModeEnumMapping.put(1, "opticalBounds");
            Objects.requireNonNull(layoutModeEnumMapping);
            this.mLayoutModeId = propertyMapper.mapIntEnum("layoutMode", 16843738, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            SparseArray<String> persistentDrawingCacheEnumMapping = new SparseArray<>();
            persistentDrawingCacheEnumMapping.put(0, "none");
            persistentDrawingCacheEnumMapping.put(1, "animation");
            persistentDrawingCacheEnumMapping.put(2, "scrolling");
            persistentDrawingCacheEnumMapping.put(3, "all");
            Objects.requireNonNull(persistentDrawingCacheEnumMapping);
            this.mPersistentDrawingCacheId = propertyMapper.mapIntEnum("persistentDrawingCache", 16842990, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mSplitMotionEventsId = propertyMapper.mapBoolean("splitMotionEvents", 16843503);
            this.mTouchscreenBlocksFocusId = propertyMapper.mapBoolean("touchscreenBlocksFocus", 16843919);
            this.mTransitionGroupId = propertyMapper.mapBoolean("transitionGroup", 16843777);
            this.mPropertiesMapped = true;
        }

        public void readProperties(ViewGroup node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readBoolean(this.mAddStatesFromChildrenId, node.addStatesFromChildren());
                propertyReader.readBoolean(this.mAlwaysDrawnWithCacheId, node.isAlwaysDrawnWithCacheEnabled());
                propertyReader.readBoolean(this.mAnimationCacheId, node.isAnimationCacheEnabled());
                propertyReader.readBoolean(this.mClipChildrenId, node.getClipChildren());
                propertyReader.readBoolean(this.mClipToPaddingId, node.getClipToPadding());
                propertyReader.readIntEnum(this.mDescendantFocusabilityId, node.getDescendantFocusability());
                propertyReader.readObject(this.mLayoutAnimationId, node.getLayoutAnimation());
                propertyReader.readIntEnum(this.mLayoutModeId, node.getLayoutMode());
                propertyReader.readIntEnum(this.mPersistentDrawingCacheId, node.getPersistentDrawingCache());
                propertyReader.readBoolean(this.mSplitMotionEventsId, node.isMotionEventSplittingEnabled());
                propertyReader.readBoolean(this.mTouchscreenBlocksFocusId, node.getTouchscreenBlocksFocus());
                propertyReader.readBoolean(this.mTransitionGroupId, node.isTransitionGroup());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
    }

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
        this.mIsSmoothScrollInit = false;
        this.mSuppressLayout = false;
        this.mLayoutCalledWhileSuppressed = false;
        this.mChildCountWithTransientState = 0;
        this.mTransientIndices = null;
        this.mTransientViews = null;
        this.mChildUnhandledKeyListeners = 0;
        this.mLayoutTransitionListener = new LayoutTransition.TransitionListener() {
            /* class android.view.ViewGroup.AnonymousClass4 */

            @Override // android.animation.LayoutTransition.TransitionListener
            @RCUnownedThisRef
            public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                if (transitionType == 3) {
                    ViewGroup.this.startViewTransition(view);
                }
            }

            @Override // android.animation.LayoutTransition.TransitionListener
            public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                if (ViewGroup.this.mLayoutCalledWhileSuppressed && !transition.isChangingLayout()) {
                    ViewGroup.this.requestLayout();
                    ViewGroup.this.mLayoutCalledWhileSuppressed = false;
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
        this.mAppAttrsHelper = HwWidgetFactory.getHwWidgetAppAttrsHelper(this.mContext);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewGroup, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.ViewGroup, attrs, a, defStyleAttr, defStyleRes);
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
                    if (id > 0) {
                        setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this.mContext, id));
                        break;
                    } else {
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
                    if (a.getBoolean(attr, false)) {
                        setLayoutTransition(new LayoutTransition());
                        break;
                    } else {
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
    @Override // android.view.View
    public void handleFocusGainInternal(int direction, Rect previouslyFocusedRect) {
        View view = this.mFocused;
        if (view != null) {
            view.unFocus(this);
            this.mFocused = null;
            this.mFocusedInCluster = null;
        }
        super.handleFocusGainInternal(direction, previouslyFocusedRect);
    }

    @Override // android.view.ViewParent
    public void requestChildFocus(View child, View focused) {
        if (getDescendantFocusability() != 393216) {
            super.unFocus(focused);
            View view = this.mFocused;
            if (view != child) {
                if (view != null) {
                    view.unFocus(focused);
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
        View view = this.mDefaultFocus;
        if (view == null || !view.isFocusedByDefault()) {
            this.mDefaultFocus = child;
            if (this.mParent instanceof ViewGroup) {
                ((ViewGroup) this.mParent).setDefaultFocus(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearDefaultFocus(View child) {
        View view = this.mDefaultFocus;
        if (view == child || view == null || !view.isFocusedByDefault()) {
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
    @Override // android.view.View
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

    @Override // android.view.ViewParent
    public void focusableViewAvailable(View v) {
        if (this.mParent != null && getDescendantFocusability() != 393216 && (this.mViewFlags & 12) == 0) {
            if (!isFocusableInTouchMode() && shouldBlockFocusForTouchscreen()) {
                return;
            }
            if (!isFocused() || getDescendantFocusability() == 262144) {
                this.mParent.focusableViewAvailable(v);
            }
        }
    }

    @Override // android.view.ViewParent
    public boolean showContextMenuForChild(View originalView) {
        if (!isShowingContextMenuWithCoords() && this.mParent != null && this.mParent.showContextMenuForChild(originalView)) {
            return true;
        }
        return false;
    }

    public final boolean isShowingContextMenuWithCoords() {
        return (this.mGroupFlags & 536870912) != 0;
    }

    @Override // android.view.ViewParent
    public boolean showContextMenuForChild(View originalView, float x, float y) {
        try {
            this.mGroupFlags |= 536870912;
            if (showContextMenuForChild(originalView)) {
                return true;
            }
            this.mGroupFlags = -536870913 & this.mGroupFlags;
            if (this.mParent == null || !this.mParent.showContextMenuForChild(originalView, x, y)) {
                return false;
            }
            return true;
        } finally {
            this.mGroupFlags = -536870913 & this.mGroupFlags;
        }
    }

    @Override // android.view.ViewParent
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        int i = this.mGroupFlags;
        if ((134217728 & i) != 0) {
            return SENTINEL_ACTION_MODE;
        }
        try {
            this.mGroupFlags = i | 268435456;
            return startActionModeForChild(originalView, callback, 0);
        } finally {
            this.mGroupFlags = -268435457 & this.mGroupFlags;
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // android.view.ViewParent
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
        int i = this.mGroupFlags;
        if ((268435456 & i) == 0 && type == 0) {
            try {
                this.mGroupFlags = i | 134217728;
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

    @Override // android.view.View
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

    @Override // android.view.ViewParent
    public View focusSearch(View focused, int direction) {
        if (isRootNamespace()) {
            return FocusFinder.getInstance().findNextFocus(this, focused, direction);
        }
        if (this.mParent != null) {
            return this.mParent.focusSearch(focused, direction);
        }
        return null;
    }

    @Override // android.view.ViewParent
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        return false;
    }

    @Override // android.view.ViewParent
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

    @Override // android.view.ViewParent
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

    @Override // android.view.View
    public boolean hasTransientState() {
        return this.mChildCountWithTransientState > 0 || super.hasTransientState();
    }

    @Override // android.view.View
    public boolean dispatchUnhandledMove(View focused, int direction) {
        View view = this.mFocused;
        return view != null && view.dispatchUnhandledMove(focused, direction);
    }

    @Override // android.view.ViewParent
    public void clearChildFocus(View child) {
        this.mFocused = null;
        if (this.mParent != null) {
            this.mParent.clearChildFocus(this);
        }
    }

    @Override // android.view.View
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
    @Override // android.view.View
    public void unFocus(View focused) {
        View view = this.mFocused;
        if (view == null) {
            super.unFocus(focused);
            return;
        }
        view.unFocus(focused);
        this.mFocused = null;
    }

    public View getFocusedChild() {
        return this.mFocused;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:14:0x0016 */
    /* JADX WARN: Multi-variable type inference failed */
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

    @Override // android.view.View
    public boolean hasFocus() {
        return ((this.mPrivateFlags & 2) == 0 && this.mFocused == null) ? false : true;
    }

    @Override // android.view.View
    public View findFocus() {
        if (isFocused()) {
            return this;
        }
        View view = this.mFocused;
        if (view != null) {
            return view.findFocus();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
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
            if (dispatchExplicit && child.hasExplicitFocusable()) {
                return true;
            }
            if (!dispatchExplicit && child.hasFocusable()) {
                return true;
            }
        }
        return false;
    }

    @Override // android.view.View
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        int focusableCount = views.size();
        int descendantFocusability = getDescendantFocusability();
        boolean blockFocusForTouchscreen = shouldBlockFocusForTouchscreen();
        boolean focusSelf = isFocusableInTouchMode() || !blockFocusForTouchscreen;
        if (descendantFocusability != 393216) {
            if (blockFocusForTouchscreen) {
                focusableMode |= 1;
            }
            if (descendantFocusability == 131072 && focusSelf) {
                super.addFocusables(views, direction, focusableMode);
            }
            int count = 0;
            View[] children = new View[this.mChildrenCount];
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
        } else if (focusSelf) {
            super.addFocusables(views, direction, focusableMode);
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // android.view.View
    public void addKeyboardNavigationClusters(Collection<View> views, int direction) {
        int focusableCount = views.size();
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
            int count = 0;
            View[] visibleChildren = new View[this.mChildrenCount];
            for (int i = 0; i < this.mChildrenCount; i++) {
                View child = this.mChildren[i];
                if ((child.mViewFlags & 12) == 0) {
                    visibleChildren[count] = child;
                    count++;
                }
            }
            FocusFinder.sort(visibleChildren, 0, count, this, isLayoutRtl());
            for (int i2 = 0; i2 < count; i2++) {
                visibleChildren[i2].addKeyboardNavigationClusters(views, direction);
            }
        }
    }

    public void setTouchscreenBlocksFocus(boolean touchscreenBlocksFocus) {
        View newFocus;
        if (touchscreenBlocksFocus) {
            this.mGroupFlags |= 67108864;
            if (hasFocus() && !isKeyboardNavigationCluster() && !getDeepestFocusedChild().isFocusableInTouchMode() && (newFocus = focusSearch(2)) != null) {
                newFocus.requestFocus();
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
        return getTouchscreenBlocksFocus() && this.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) && (!isKeyboardNavigationCluster() || (!hasFocus() && findKeyboardNavigationCluster() == this));
    }

    @Override // android.view.View
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

    @Override // android.view.View
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

    @Override // android.view.View
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

    @Override // android.view.View
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        super.dispatchWindowFocusChanged(hasFocus);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchWindowFocusChanged(hasFocus);
        }
    }

    @Override // android.view.View
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

    @Override // android.view.View
    @UnsupportedAppUsage
    public void makeOptionalFitsSystemWindows() {
        super.makeOptionalFitsSystemWindows();
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].makeOptionalFitsSystemWindows();
        }
    }

    @Override // android.view.View
    public void dispatchDisplayHint(int hint) {
        super.dispatchDisplayHint(hint);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchDisplayHint(hint);
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void onChildVisibilityChanged(View child, int oldVisibility, int newVisibility) {
        LayoutTransition layoutTransition = this.mTransition;
        if (layoutTransition != null) {
            if (newVisibility == 0) {
                layoutTransition.showChild(this, child, oldVisibility);
            } else {
                layoutTransition.hideChild(this, child, newVisibility);
                ArrayList<View> arrayList = this.mTransitioningViews;
                if (arrayList != null && arrayList.contains(child)) {
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
    @Override // android.view.View
    public void dispatchVisibilityChanged(View changedView, int visibility) {
        super.dispatchVisibilityChanged(changedView, visibility);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchVisibilityChanged(changedView, visibility);
        }
    }

    @Override // android.view.View
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
    @Override // android.view.View
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

    @Override // android.view.View
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

    @Override // android.view.ViewParent
    public void recomputeViewAttributes(View child) {
        ViewParent parent;
        if (this.mAttachInfo != null && !this.mAttachInfo.mRecomputeGlobalAttributes && (parent = this.mParent) != null) {
            parent.recomputeViewAttributes(this);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
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

    @Override // android.view.ViewParent
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
    @Override // android.view.View
    public boolean dispatchDragEnterExitInPreN(DragEvent event) {
        View view;
        if (event.mAction == 6 && (view = this.mCurrentDragChild) != null) {
            view.dispatchDragEnterExitInPreN(event);
            this.mCurrentDragChild = null;
        }
        return this.mIsInterestedInDrag && super.dispatchDragEnterExitInPreN(event);
    }

    @Override // android.view.View
    public boolean dispatchDragEvent(DragEvent event) {
        boolean eventWasConsumed;
        boolean retval = false;
        float tx = event.mX;
        float ty = event.mY;
        ClipData td = event.mClipData;
        PointF localPoint = getLocalPoint();
        int i = event.mAction;
        if (i == 1) {
            this.mCurrentDragChild = null;
            this.mCurrentDragStartEvent = DragEvent.obtain(event);
            HashSet<View> hashSet = this.mChildrenInterestedInDrag;
            if (hashSet == null) {
                this.mChildrenInterestedInDrag = new HashSet<>();
            } else {
                hashSet.clear();
            }
            int count = this.mChildrenCount;
            View[] children = this.mChildren;
            for (int i2 = 0; i2 < count; i2++) {
                View child = children[i2];
                child.mPrivateFlags2 &= -4;
                if (child.getVisibility() == 0 && notifyChildOfDragStart(children[i2])) {
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
        } else if (i == 2 || i == 3) {
            View target = findFrontmostDroppableChildAt(event.mX, event.mY, localPoint);
            if (target != this.mCurrentDragChild) {
                if (sCascadedDragDrop) {
                    int action = event.mAction;
                    event.mX = 0.0f;
                    event.mY = 0.0f;
                    event.mClipData = null;
                    View view = this.mCurrentDragChild;
                    if (view != null) {
                        event.mAction = 6;
                        view.dispatchDragEnterExitInPreN(event);
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
        } else if (i != 4) {
            return false;
        } else {
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
            DragEvent dragEvent = this.mCurrentDragStartEvent;
            if (dragEvent != null) {
                dragEvent.recycle();
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
        DragEvent dragEvent = this.mCurrentDragStartEvent;
        dragEvent.mX = point[0];
        dragEvent.mY = point[1];
        boolean canAccept = child.dispatchDragEvent(dragEvent);
        DragEvent dragEvent2 = this.mCurrentDragStartEvent;
        dragEvent2.mX = tx;
        dragEvent2.mY = ty;
        dragEvent2.mEventHandlerWasCalled = false;
        if (canAccept) {
            this.mChildrenInterestedInDrag.add(child);
            if (!child.canAcceptDrag()) {
                child.mPrivateFlags2 |= 1;
                child.refreshDrawableState();
            }
        }
        return canAccept;
    }

    @Override // android.view.View
    public void dispatchWindowSystemUiVisiblityChanged(int visible) {
        super.dispatchWindowSystemUiVisiblityChanged(visible);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchWindowSystemUiVisiblityChanged(visible);
        }
    }

    @Override // android.view.View
    public void dispatchSystemUiVisibilityChanged(int visible) {
        super.dispatchSystemUiVisibilityChanged(visible);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchSystemUiVisibilityChanged(visible);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
    public boolean updateLocalSystemUiVisibility(int localValue, int localChanges) {
        boolean changed = super.updateLocalSystemUiVisibility(localValue, localChanges);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            changed |= children[i].updateLocalSystemUiVisibility(localValue, localChanges);
        }
        return changed;
    }

    @Override // android.view.View
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if ((this.mPrivateFlags & 18) == 18) {
            return super.dispatchKeyEventPreIme(event);
        }
        View view = this.mFocused;
        if (view == null || (view.mPrivateFlags & 16) != 16) {
            return false;
        }
        return this.mFocused.dispatchKeyEventPreIme(event);
    }

    @Override // android.view.View
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onKeyEvent(event, 1);
        }
        if ((this.mPrivateFlags & 18) != 18) {
            View view = this.mFocused;
            if (view != null && (view.mPrivateFlags & 16) == 16 && this.mFocused.dispatchKeyEvent(event)) {
                return true;
            }
        } else if (super.dispatchKeyEvent(event)) {
            return true;
        }
        if (this.mInputEventConsistencyVerifier == null) {
            return false;
        }
        this.mInputEventConsistencyVerifier.onUnhandledEvent(event, 1);
        return false;
    }

    @Override // android.view.View
    public boolean dispatchStatusBarTop() {
        boolean result = false;
        View[] arrayOfView = this.mChildren;
        if (arrayOfView != null) {
            for (View view : arrayOfView) {
                if (view != null) {
                    result |= view.dispatchStatusBarTop();
                }
            }
        }
        if (result || !isNeedScrollToTop() || !isScrollToTopEnabled()) {
            return result;
        }
        if (!this.mIsSmoothScrollInit) {
            try {
                this.mSmoothScroll = getClass().getMethod("smoothScrollToPosition", Integer.TYPE);
                this.mScrollToPosition = getClass().getMethod("scrollToPosition", Integer.TYPE);
            } catch (NoSuchMethodException e) {
            }
            this.mIsSmoothScrollInit = true;
        }
        if (!(this.mSmoothScroll == null || this.mScrollToPosition == null)) {
            try {
                if (computeVerticalScrollOffset() > computeVerticalScrollExtent() * 3) {
                    this.mScrollToPosition.invoke(this, Integer.valueOf(getChildCount() * 3));
                }
                this.mSmoothScroll.invoke(this, 0);
                return true;
            } catch (IllegalAccessException e2) {
                Log.e(TAG, "dispatchStatusBarTop:IllegalAccessException");
            } catch (InvocationTargetException e3) {
                Log.e(TAG, "dispatchStatusBarTop:InvocationTargetException");
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isScrollToTopEnabled() {
        if (!(getVisibility() == 0) || !getLocalVisibleRect(new Rect())) {
            return false;
        }
        if (this.mAppAttrsHelper == null) {
            this.mAppAttrsHelper = HwWidgetFactory.getHwWidgetAppAttrsHelper(this.mContext);
        }
        String pkgName = this.mContext.getPackageName();
        if (!TextUtils.isEmpty(pkgName)) {
            return this.mAppAttrsHelper.isScrollTopEnabled(pkgName);
        }
        Log.e(TAG, "isScrollToTopEnabled: get pkgName fail");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isNeedScrollToTop() {
        return canScrollVertically(-1) || canScrollVertically(1);
    }

    @Override // android.view.View
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        if ((this.mPrivateFlags & 18) == 18) {
            return super.dispatchKeyShortcutEvent(event);
        }
        View view = this.mFocused;
        if (view == null || (view.mPrivateFlags & 16) != 16) {
            return false;
        }
        return this.mFocused.dispatchKeyShortcutEvent(event);
    }

    @Override // android.view.View
    public boolean dispatchTrackballEvent(MotionEvent event) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTrackballEvent(event, 1);
        }
        if ((this.mPrivateFlags & 18) != 18) {
            View view = this.mFocused;
            if (view != null && (view.mPrivateFlags & 16) == 16 && this.mFocused.dispatchTrackballEvent(event)) {
                return true;
            }
        } else if (super.dispatchTrackballEvent(event)) {
            return true;
        }
        if (this.mInputEventConsistencyVerifier == null) {
            return false;
        }
        this.mInputEventConsistencyVerifier.onUnhandledEvent(event, 1);
        return false;
    }

    @Override // android.view.View
    public boolean dispatchCapturedPointerEvent(MotionEvent event) {
        if ((this.mPrivateFlags & 18) != 18) {
            View view = this.mFocused;
            if (view == null || (view.mPrivateFlags & 16) != 16 || !this.mFocused.dispatchCapturedPointerEvent(event)) {
                return false;
            }
            return true;
        } else if (super.dispatchCapturedPointerEvent(event)) {
            return true;
        } else {
            return false;
        }
    }

    @Override // android.view.View
    public void dispatchPointerCaptureChanged(boolean hasCapture) {
        exitHoverTargets();
        super.dispatchPointerCaptureChanged(hasCapture);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchPointerCaptureChanged(hasCapture);
        }
    }

    @Override // android.view.View
    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        PointerIcon pointerIcon;
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
                if (child.canReceivePointerEvents() && isTransformedTouchPointInView(x, y, child, null) && (pointerIcon = dispatchResolvePointerIcon(event, pointerIndex, child)) != null) {
                    if (preorderedList != null) {
                        preorderedList.clear();
                    }
                    return pointerIcon;
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

    /* JADX INFO: Multiple debug info for r0v1 int: [D('childIndex1' int), D('childIndex' int)] */
    private int getAndVerifyPreorderedIndex(int childrenCount, int i, boolean customOrder) {
        if (!customOrder) {
            return i;
        }
        int childIndex = getChildDrawingOrder(childrenCount, i);
        if (childIndex < childrenCount) {
            return childIndex;
        }
        throw new IndexOutOfBoundsException("getChildDrawingOrder() returned invalid index " + childIndex + " (child count is " + childrenCount + ")");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x011a  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0163  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x016c  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x01c1  */
    @Override // android.view.View
    public boolean dispatchHoverEvent(MotionEvent event) {
        boolean handled;
        boolean newHoveredSelf;
        boolean z;
        boolean interceptHover;
        HoverTarget firstOldHoverTarget;
        boolean wasHovered;
        HoverTarget lastHoverTarget;
        int action = event.getAction();
        boolean interceptHover2 = onInterceptHoverEvent(event);
        event.setAction(action);
        MotionEvent eventNoHistory = event;
        HoverTarget firstOldHoverTarget2 = this.mFirstHoverTarget;
        PointF pointF = null;
        this.mFirstHoverTarget = null;
        if (!interceptHover2 && action != 10) {
            float x = event.getX();
            float y = event.getY();
            int childrenCount = this.mChildrenCount;
            if (childrenCount != 0) {
                ArrayList<View> preorderedList = buildOrderedChildList();
                boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
                View[] children = this.mChildren;
                MotionEvent eventNoHistory2 = eventNoHistory;
                int i = childrenCount - 1;
                handled = false;
                HoverTarget lastHoverTarget2 = null;
                while (true) {
                    if (i < 0) {
                        eventNoHistory = eventNoHistory2;
                        break;
                    }
                    View child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, customOrder));
                    if (!child.canReceivePointerEvents()) {
                        interceptHover = interceptHover2;
                        firstOldHoverTarget = firstOldHoverTarget2;
                    } else if (!isTransformedTouchPointInView(x, y, child, pointF)) {
                        interceptHover = interceptHover2;
                        firstOldHoverTarget = firstOldHoverTarget2;
                    } else {
                        HoverTarget hoverTarget = firstOldHoverTarget2;
                        interceptHover = interceptHover2;
                        HoverTarget predecessor = null;
                        while (true) {
                            if (hoverTarget == null) {
                                hoverTarget = HoverTarget.obtain(child);
                                wasHovered = false;
                                break;
                            } else if (hoverTarget.child == child) {
                                if (predecessor != null) {
                                    predecessor.next = hoverTarget.next;
                                    firstOldHoverTarget2 = firstOldHoverTarget2;
                                } else {
                                    firstOldHoverTarget2 = hoverTarget.next;
                                }
                                hoverTarget.next = null;
                                wasHovered = true;
                            } else {
                                predecessor = hoverTarget;
                                hoverTarget = hoverTarget.next;
                                firstOldHoverTarget2 = firstOldHoverTarget2;
                            }
                        }
                        if (lastHoverTarget2 != null) {
                            lastHoverTarget2.next = hoverTarget;
                        } else {
                            this.mFirstHoverTarget = hoverTarget;
                        }
                        if (action == 9) {
                            if (!wasHovered) {
                                handled |= dispatchTransformedGenericPointerEvent(event, child);
                                lastHoverTarget = hoverTarget;
                            } else {
                                lastHoverTarget = hoverTarget;
                            }
                        } else if (action != 7) {
                            lastHoverTarget = hoverTarget;
                        } else if (!wasHovered) {
                            MotionEvent eventNoHistory3 = obtainMotionEventNoHistoryOrSelf(eventNoHistory2);
                            lastHoverTarget = hoverTarget;
                            eventNoHistory3.setAction(9);
                            eventNoHistory3.setAction(action);
                            handled = handled | dispatchTransformedGenericPointerEvent(eventNoHistory3, child) | dispatchTransformedGenericPointerEvent(eventNoHistory3, child);
                            eventNoHistory2 = eventNoHistory3;
                        } else {
                            lastHoverTarget = hoverTarget;
                            handled |= dispatchTransformedGenericPointerEvent(event, child);
                        }
                        if (handled) {
                            eventNoHistory = eventNoHistory2;
                            break;
                        }
                        lastHoverTarget2 = lastHoverTarget;
                        i--;
                        customOrder = customOrder;
                        interceptHover2 = interceptHover;
                        pointF = null;
                    }
                    firstOldHoverTarget2 = firstOldHoverTarget;
                    i--;
                    customOrder = customOrder;
                    interceptHover2 = interceptHover;
                    pointF = null;
                }
                if (preorderedList != null) {
                    preorderedList.clear();
                }
                while (firstOldHoverTarget2 != null) {
                    View child2 = firstOldHoverTarget2.child;
                    if (action == 10) {
                        handled |= dispatchTransformedGenericPointerEvent(event, child2);
                    } else {
                        if (action == 7) {
                            boolean hoverExitPending = event.isHoverExitPending();
                            event.setHoverExitPending(true);
                            dispatchTransformedGenericPointerEvent(event, child2);
                            event.setHoverExitPending(hoverExitPending);
                        }
                        eventNoHistory = obtainMotionEventNoHistoryOrSelf(eventNoHistory);
                        eventNoHistory.setAction(10);
                        dispatchTransformedGenericPointerEvent(eventNoHistory, child2);
                        eventNoHistory.setAction(action);
                    }
                    HoverTarget nextOldHoverTarget = firstOldHoverTarget2.next;
                    firstOldHoverTarget2.recycle();
                    firstOldHoverTarget2 = nextOldHoverTarget;
                }
                newHoveredSelf = handled && action != 10 && !event.isHoverExitPending();
                z = this.mHoveredSelf;
                if (newHoveredSelf == z) {
                    if (z) {
                        if (action == 10) {
                            handled |= super.dispatchHoverEvent(event);
                        } else {
                            if (action == 7) {
                                super.dispatchHoverEvent(event);
                            }
                            eventNoHistory = obtainMotionEventNoHistoryOrSelf(eventNoHistory);
                            eventNoHistory.setAction(10);
                            super.dispatchHoverEvent(eventNoHistory);
                            eventNoHistory.setAction(action);
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
                            eventNoHistory.setAction(action);
                            handled = handled | super.dispatchHoverEvent(eventNoHistory) | super.dispatchHoverEvent(eventNoHistory);
                            this.mHoveredSelf = true;
                        }
                    }
                } else if (newHoveredSelf) {
                    handled |= super.dispatchHoverEvent(event);
                }
                if (eventNoHistory != event) {
                    eventNoHistory.recycle();
                }
                return handled;
            }
        }
        handled = false;
        while (firstOldHoverTarget2 != null) {
        }
        if (handled) {
        }
        z = this.mHoveredSelf;
        if (newHoveredSelf == z) {
        }
        if (eventNoHistory != event) {
        }
        return handled;
    }

    private void exitHoverTargets() {
        if (this.mHoveredSelf || this.mFirstHoverTarget != null) {
            long now = SystemClock.uptimeMillis();
            MotionEvent event = MotionEvent.obtain(now, now, 10, 0.0f, 0.0f, 0);
            event.setSource(4098);
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
                event.setSource(4098);
                view.dispatchHoverEvent(event);
                event.recycle();
                return;
            }
            predecessor = target;
            target = next;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
    public boolean dispatchTooltipHoverEvent(MotionEvent event) {
        int action = event.getAction();
        if (action != 7) {
            if (action != 9 && action == 10) {
                View view = this.mTooltipHoverTarget;
                if (view != null) {
                    view.dispatchTooltipHoverEvent(event);
                    this.mTooltipHoverTarget = null;
                } else if (this.mTooltipHoveredSelf) {
                    super.dispatchTooltipHoverEvent(event);
                    this.mTooltipHoveredSelf = false;
                }
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
                if (child.canReceivePointerEvents() && isTransformedTouchPointInView(x, y, child, null) && dispatchTooltipHoverEvent(event, child)) {
                    newTarget = child;
                    break;
                }
                i--;
            }
            if (preorderedList != null) {
                preorderedList.clear();
            }
        }
        View view2 = this.mTooltipHoverTarget;
        if (view2 != newTarget) {
            if (view2 != null) {
                event.setAction(10);
                this.mTooltipHoverTarget.dispatchTooltipHoverEvent(event);
                event.setAction(action);
            }
            this.mTooltipHoverTarget = newTarget;
        }
        if (this.mTooltipHoverTarget == null) {
            this.mTooltipHoveredSelf = super.dispatchTooltipHoverEvent(event);
            return this.mTooltipHoveredSelf;
        } else if (!this.mTooltipHoveredSelf) {
            return true;
        } else {
            this.mTooltipHoveredSelf = false;
            event.setAction(10);
            super.dispatchTooltipHoverEvent(event);
            event.setAction(action);
            return true;
        }
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
            event.setSource(4098);
            dispatchTooltipHoverEvent(event);
            event.recycle();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean hasHoveredChild() {
        return this.mFirstHoverTarget != null;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean pointInHoveredChild(MotionEvent event) {
        if (this.mFirstHoverTarget != null) {
            return isTransformedTouchPointInView(event.getX(), event.getY(), this.mFirstHoverTarget.child, null);
        }
        return false;
    }

    @Override // android.view.View
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
        if (!event.isFromSource(8194)) {
            return false;
        }
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        if ((action == 7 || action == 9) && isOnScrollbar(x, y)) {
            return true;
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
    @Override // android.view.View
    public boolean dispatchGenericPointerEvent(MotionEvent event) {
        int childrenCount = this.mChildrenCount;
        if (childrenCount != 0) {
            float x = event.getX();
            float y = event.getY();
            ArrayList<View> preorderedList = buildOrderedChildList();
            boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
            View[] children = this.mChildren;
            for (int i = childrenCount - 1; i >= 0; i--) {
                View child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, customOrder));
                if (child.canReceivePointerEvents() && isTransformedTouchPointInView(x, y, child, null) && dispatchTransformedGenericPointerEvent(event, child)) {
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
    @Override // android.view.View
    public boolean dispatchGenericFocusedEvent(MotionEvent event) {
        if ((this.mPrivateFlags & 18) == 18) {
            return super.dispatchGenericFocusedEvent(event);
        }
        View view = this.mFocused;
        if (view == null || (view.mPrivateFlags & 16) != 16) {
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

    /* JADX INFO: Multiple debug info for r5v4 android.view.View[]: [D('action' int), D('children' android.view.View[])] */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x01ed  */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x01f4  */
    @Override // android.view.View
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean intercepted;
        boolean handled;
        boolean alreadyDispatchedToNewTouchTarget;
        boolean z;
        boolean alreadyDispatchedToNewTouchTarget2;
        boolean handled2;
        boolean z2;
        int idBitsToAssign;
        boolean alreadyDispatchedToNewTouchTarget3;
        int childrenCount;
        float x;
        boolean customOrder;
        float x2;
        View childWithAccessibilityFocus;
        boolean z3;
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTouchEvent(ev, 1);
        }
        if (ev.isTargetAccessibilityFocus() && isAccessibilityFocusedViewOrHost()) {
            ev.setTargetAccessibilityFocus(false);
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
                    boolean intercepted2 = onInterceptTouchEvent(ev);
                    ev.setAction(action);
                    intercepted = intercepted2;
                } else {
                    intercepted = false;
                }
            } else {
                intercepted = true;
            }
            if (intercepted || this.mFirstTouchTarget != null) {
                ev.setTargetAccessibilityFocus(false);
            }
            boolean canceled = resetCancelNextUpFlag(this) || actionMasked == 3;
            boolean split = (this.mGroupFlags & 2097152) != 0;
            TouchTarget newTouchTarget = null;
            boolean alreadyDispatchedToNewTouchTarget4 = false;
            if (canceled || intercepted) {
                handled2 = false;
                z2 = false;
                alreadyDispatchedToNewTouchTarget2 = false;
            } else {
                View childWithAccessibilityFocus2 = ev.isTargetAccessibilityFocus() ? findChildWithAccessibilityFocus() : null;
                if (actionMasked == 0 || ((split && actionMasked == 5) || actionMasked == 7)) {
                    int actionIndex = ev.getActionIndex();
                    if (split) {
                        idBitsToAssign = 1 << ev.getPointerId(actionIndex);
                    } else {
                        idBitsToAssign = -1;
                    }
                    removePointersFromTouchTargets(idBitsToAssign);
                    int childrenCount2 = this.mChildrenCount;
                    if (0 != 0 || childrenCount2 == 0) {
                        handled = false;
                        z = false;
                        alreadyDispatchedToNewTouchTarget = false;
                    } else {
                        float y = ev.getX(actionIndex);
                        float y2 = ev.getY(actionIndex);
                        handled = false;
                        ArrayList<View> preorderedList = buildTouchDispatchChildList();
                        boolean customOrder2 = preorderedList == null && isChildrenDrawingOrderEnabled();
                        View[] children = this.mChildren;
                        TouchTarget newTouchTarget2 = null;
                        int i = childrenCount2 - 1;
                        while (true) {
                            if (i < 0) {
                                alreadyDispatchedToNewTouchTarget3 = alreadyDispatchedToNewTouchTarget4;
                                z = false;
                                break;
                            }
                            alreadyDispatchedToNewTouchTarget3 = alreadyDispatchedToNewTouchTarget4;
                            int childIndex = getAndVerifyPreorderedIndex(childrenCount2, i, customOrder2);
                            int i2 = i;
                            View child = getAndVerifyPreorderedView(preorderedList, children, childIndex);
                            if (childWithAccessibilityFocus2 != null) {
                                if (childWithAccessibilityFocus2 != child) {
                                    childrenCount = childrenCount2;
                                    x = y;
                                    customOrder = customOrder2;
                                    x2 = y2;
                                    y2 = x2;
                                    i = i2 - 1;
                                    actionIndex = actionIndex;
                                    alreadyDispatchedToNewTouchTarget4 = alreadyDispatchedToNewTouchTarget3;
                                    customOrder2 = customOrder;
                                    y = x;
                                    childrenCount2 = childrenCount;
                                } else {
                                    childWithAccessibilityFocus2 = null;
                                    i2 = childrenCount2 - 1;
                                }
                            }
                            if (child.canReceivePointerEvents()) {
                                customOrder = customOrder2;
                                if (!isTransformedTouchPointInView(y, y2, child, null)) {
                                    childrenCount = childrenCount2;
                                    x = y;
                                    x2 = y2;
                                    childWithAccessibilityFocus = childWithAccessibilityFocus2;
                                    z3 = false;
                                } else {
                                    TouchTarget newTouchTarget3 = getTouchTarget(child);
                                    if (newTouchTarget3 != null) {
                                        newTouchTarget3.pointerIdBits |= idBitsToAssign;
                                        newTouchTarget2 = newTouchTarget3;
                                        z = false;
                                        break;
                                    }
                                    x = y;
                                    resetCancelNextUpFlag(child);
                                    if (dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {
                                        this.mLastTouchDownTime = ev.getDownTime();
                                        if (preorderedList != null) {
                                            int j = 0;
                                            while (true) {
                                                if (j >= childrenCount2) {
                                                    break;
                                                } else if (children[childIndex] == this.mChildren[j]) {
                                                    this.mLastTouchDownIndex = j;
                                                    break;
                                                } else {
                                                    j++;
                                                    childrenCount2 = childrenCount2;
                                                }
                                            }
                                        } else {
                                            this.mLastTouchDownIndex = childIndex;
                                        }
                                        this.mLastTouchDownX = ev.getX();
                                        this.mLastTouchDownY = ev.getY();
                                        newTouchTarget2 = addTouchTarget(child, idBitsToAssign);
                                        alreadyDispatchedToNewTouchTarget3 = true;
                                        z = false;
                                    } else {
                                        childrenCount = childrenCount2;
                                        x2 = y2;
                                        ev.setTargetAccessibilityFocus(false);
                                        newTouchTarget2 = newTouchTarget3;
                                        y2 = x2;
                                        i = i2 - 1;
                                        actionIndex = actionIndex;
                                        alreadyDispatchedToNewTouchTarget4 = alreadyDispatchedToNewTouchTarget3;
                                        customOrder2 = customOrder;
                                        y = x;
                                        childrenCount2 = childrenCount;
                                    }
                                }
                            } else {
                                childrenCount = childrenCount2;
                                x = y;
                                customOrder = customOrder2;
                                x2 = y2;
                                childWithAccessibilityFocus = childWithAccessibilityFocus2;
                                z3 = false;
                            }
                            ev.setTargetAccessibilityFocus(z3);
                            childWithAccessibilityFocus2 = childWithAccessibilityFocus;
                            y2 = x2;
                            i = i2 - 1;
                            actionIndex = actionIndex;
                            alreadyDispatchedToNewTouchTarget4 = alreadyDispatchedToNewTouchTarget3;
                            customOrder2 = customOrder;
                            y = x;
                            childrenCount2 = childrenCount;
                        }
                        if (preorderedList != null) {
                            preorderedList.clear();
                        }
                        newTouchTarget = newTouchTarget2;
                        alreadyDispatchedToNewTouchTarget = alreadyDispatchedToNewTouchTarget3;
                    }
                    if (newTouchTarget == null && this.mFirstTouchTarget != null) {
                        newTouchTarget = this.mFirstTouchTarget;
                        while (newTouchTarget.next != null) {
                            newTouchTarget = newTouchTarget.next;
                        }
                        newTouchTarget.pointerIdBits |= idBitsToAssign;
                    }
                    if (this.mFirstTouchTarget != null) {
                        handled3 = dispatchTransformedTouchEvent(ev, canceled, null, -1);
                    } else {
                        TouchTarget predecessor = null;
                        TouchTarget target = this.mFirstTouchTarget;
                        while (target != null) {
                            TouchTarget next = target.next;
                            if (!alreadyDispatchedToNewTouchTarget || target != newTouchTarget) {
                                boolean cancelChild = (resetCancelNextUpFlag(target.child) || intercepted) ? true : z;
                                if (dispatchTransformedTouchEvent(ev, cancelChild, target.child, target.pointerIdBits)) {
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
                        removePointersFromTouchTargets(1 << ev.getPointerId(ev.getActionIndex()));
                    }
                } else {
                    handled2 = false;
                    z2 = false;
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
            this.mInputEventConsistencyVerifier.onUnhandledEvent(ev, 1);
        }
        return handled3;
    }

    public ArrayList<View> buildTouchDispatchChildList() {
        return buildOrderedChildList();
    }

    private View findChildWithAccessibilityFocus() {
        View current;
        ViewRootImpl viewRoot = getViewRootImpl();
        if (viewRoot == null || (current = viewRoot.getAccessibilityFocusedHost()) == null) {
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
                event.setSource(4098);
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

    @UnsupportedAppUsage
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
                event.setSource(4098);
                view.dispatchTouchEvent(event);
                event.recycle();
                return;
            }
            predecessor = target;
            target = next;
        }
    }

    private float[] getTempPoint() {
        if (this.mTempPoint == null) {
            this.mTempPoint = new float[2];
        }
        return this.mTempPoint;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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
        } else if (child != null && !child.hasIdentityMatrix()) {
            transformedEvent = MotionEvent.obtain(event);
        } else if (child == null) {
            return super.dispatchTouchEvent(event);
        } else {
            float offsetX = (float) (this.mScrollX - child.mLeft);
            float offsetY = (float) (this.mScrollY - child.mTop);
            event.offsetLocation(offsetX, offsetY);
            boolean handled3 = child.dispatchTouchEvent(event);
            event.offsetLocation(-offsetX, -offsetY);
            return handled3;
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
        int i = this.mGroupFlags;
        if ((33554432 & i) != 0) {
            return (i & 16777216) != 0;
        }
        ViewOutlineProvider outlineProvider = getOutlineProvider();
        return (getBackground() == null && getTransitionName() == null && (outlineProvider == null || outlineProvider == ViewOutlineProvider.BACKGROUND)) ? false : true;
    }

    public void setTransitionGroup(boolean isTransitionGroup) {
        this.mGroupFlags |= 33554432;
        if (isTransitionGroup) {
            this.mGroupFlags |= 16777216;
        } else {
            this.mGroupFlags &= -16777217;
        }
    }

    @Override // android.view.ViewParent
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
        if (!ev.isFromSource(8194) || ev.getAction() != 0 || !ev.isButtonPressed(1) || !isOnScrollbarThumb(ev.getX(), ev.getY())) {
            return false;
        }
        return true;
    }

    @Override // android.view.View
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
        if (result && !isLayoutValid() && (this.mPrivateFlags & 1) == 0) {
            this.mPrivateFlags |= 1;
        }
        return result;
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

    @Override // android.view.View
    public boolean restoreDefaultFocus() {
        if (this.mDefaultFocus == null || getDescendantFocusability() == 393216 || (this.mDefaultFocus.mViewFlags & 12) != 0 || !this.mDefaultFocus.restoreDefaultFocus()) {
            return super.restoreDefaultFocus();
        }
        return true;
    }

    /* JADX INFO: finally extract failed */
    @Override // android.view.View
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

    @Override // android.view.View
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

    @Override // android.view.View
    public void dispatchStartTemporaryDetach() {
        super.dispatchStartTemporaryDetach();
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchStartTemporaryDetach();
        }
    }

    @Override // android.view.View
    public void dispatchFinishTemporaryDetach() {
        super.dispatchFinishTemporaryDetach();
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchFinishTemporaryDetach();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
    @UnsupportedAppUsage
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
        List<Integer> list = this.mTransientIndices;
        int transientCount = list == null ? 0 : list.size();
        for (int i2 = 0; i2 < transientCount; i2++) {
            View view = this.mTransientViews.get(i2);
            view.dispatchAttachedToWindow(info, combineVisibility(visibility, view.getVisibility()));
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
    public void dispatchScreenStateChanged(int screenState) {
        super.dispatchScreenStateChanged(screenState);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchScreenStateChanged(screenState);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
    public void dispatchMovedToDisplay(Display display, Configuration config) {
        super.dispatchMovedToDisplay(display, config);
        int count = this.mChildrenCount;
        View[] children = this.mChildren;
        for (int i = 0; i < count; i++) {
            children[i].dispatchMovedToDisplay(display, config);
        }
    }

    @Override // android.view.View
    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        boolean handled;
        if (includeForAccessibility() && (handled = super.dispatchPopulateAccessibilityEventInternal(event))) {
            return handled;
        }
        ChildListForAccessibility children = ChildListForAccessibility.obtain(this, true);
        try {
            int childCount = children.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = children.getChildAt(i);
                if ((child.mViewFlags & 12) == 0) {
                    boolean handled2 = child.dispatchPopulateAccessibilityEvent(event);
                    if (handled2) {
                        return handled2;
                    }
                }
            }
            children.recycle();
            return false;
        } finally {
            children.recycle();
        }
    }

    @Override // android.view.View
    public void dispatchProvideStructure(ViewStructure structure) {
        int childrenCount;
        int childIndex;
        super.dispatchProvideStructure(structure);
        if (!isAssistBlocked() && structure.getChildCount() == 0 && (childrenCount = this.mChildrenCount) > 0) {
            if (isLaidOut()) {
                structure.setChildCount(childrenCount);
                ArrayList<View> preorderedList = buildOrderedChildList();
                boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
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
                                preorderedList = new ArrayList<>(childrenCount);
                                for (int j3 = 0; j3 < childrenCount; j3++) {
                                    preorderedList.add(this.mChildren[permutation[j3]]);
                                }
                            }
                        } else {
                            throw e;
                        }
                    }
                    getAndVerifyPreorderedView(preorderedList, this.mChildren, childIndex).dispatchProvideStructure(structure.newChild(i));
                }
                if (preorderedList != null) {
                    preorderedList.clear();
                }
            } else if (Helper.sVerbose) {
                Log.v("View", "dispatchProvideStructure(): not laid out, ignoring " + childrenCount + " children of " + getAccessibilityViewId());
            }
        }
    }

    @Override // android.view.View
    public void dispatchProvideAutofillStructure(ViewStructure structure, int flags) {
        super.dispatchProvideAutofillStructure(structure, flags);
        if (structure.getChildCount() == 0) {
            if (isLaidOut()) {
                ChildListForAutofill children = getChildrenForAutofill(flags);
                int childrenCount = children.size();
                structure.setChildCount(childrenCount);
                for (int i = 0; i < childrenCount; i++) {
                    ((View) children.get(i)).dispatchProvideAutofillStructure(structure.newChild(i), flags);
                }
                children.recycle();
            } else if (Helper.sVerbose) {
                Log.v("View", "dispatchProvideAutofillStructure(): not laid out, ignoring " + this.mChildrenCount + " children of " + getAutofillId());
            }
        }
    }

    private ChildListForAutofill getChildrenForAutofill(int flags) {
        ChildListForAutofill children = ChildListForAutofill.obtain();
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

    @Override // android.view.View
    @UnsupportedAppUsage
    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (getAccessibilityNodeProvider() == null && this.mAttachInfo != null) {
            ArrayList<View> childrenForAccessibility = this.mAttachInfo.mTempArrayList;
            childrenForAccessibility.clear();
            addChildrenForAccessibility(childrenForAccessibility);
            int childrenForAccessibilityCount = childrenForAccessibility.size();
            for (int i = 0; i < childrenForAccessibilityCount; i++) {
                info.addChildUnchecked(childrenForAccessibility.get(i));
            }
            childrenForAccessibility.clear();
        }
    }

    @Override // android.view.View
    public CharSequence getAccessibilityClassName() {
        return ViewGroup.class.getName();
    }

    @Override // android.view.ViewParent
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

    @Override // android.view.View
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
    @Override // android.view.View
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
        ArrayList<View> preorderedList = buildOrderedChildList();
        boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
        View[] children = this.mChildren;
        int childIndex = customOrder ? getChildDrawingOrder(childrenCount, index) : index;
        if (preorderedList == null) {
            if (childIndex < 0 || childIndex >= children.length) {
                return null;
            }
            return children[childIndex];
        } else if (childIndex < 0 || childIndex >= preorderedList.size()) {
            return null;
        } else {
            return preorderedList.get(childIndex);
        }
    }

    @Override // android.view.ViewParent
    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) {
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
    @UnsupportedAppUsage
    public void dispatchDetachedFromWindow() {
        cancelAndClearTouchTargets(null);
        exitHoverTargets();
        exitTooltipHoverTargets();
        int transientCount = 0;
        this.mLayoutCalledWhileSuppressed = false;
        this.mChildrenInterestedInDrag = null;
        this.mIsInterestedInDrag = false;
        DragEvent dragEvent = this.mCurrentDragStartEvent;
        if (dragEvent != null) {
            dragEvent.recycle();
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
        if (this.mTransientViews != null) {
            transientCount = this.mTransientIndices.size();
        }
        for (int i2 = 0; i2 < transientCount; i2++) {
            View view = this.mTransientViews.get(i2);
            if (view != null) {
                view.dispatchDetachedFromWindow();
            }
        }
        super.dispatchDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void internalSetPadding(int left, int top, int right, int bottom) {
        super.internalSetPadding(left, top, right, bottom);
        if ((this.mPaddingLeft | this.mPaddingTop | this.mPaddingRight | this.mPaddingBottom) != 0) {
            this.mGroupFlags |= 32;
        } else {
            this.mGroupFlags &= -33;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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
    @Override // android.view.View
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

    /*  JADX ERROR: StackOverflowError in pass: MarkFinallyVisitor
        java.lang.StackOverflowError
        	at jadx.core.dex.nodes.InsnNode.isSame(InsnNode.java:303)
        	at jadx.core.dex.instructions.InvokeNode.isSame(InvokeNode.java:77)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.sameInsns(MarkFinallyVisitor.java:451)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.compareBlocks(MarkFinallyVisitor.java:436)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:408)
        	at jadx.core.dex.visitors.MarkFinallyVisitor.checkBlocksTree(MarkFinallyVisitor.java:411)
        */
    @Override // android.view.View
    public android.graphics.Bitmap createSnapshot(android.view.ViewDebug.CanvasProvider r8, boolean r9) {
        /*
            r7 = this;
            int r0 = r7.mChildrenCount
            r1 = 0
            if (r9 == 0) goto L_0x0023
            int[] r1 = new int[r0]
            r2 = 0
        L_0x0008:
            if (r2 >= r0) goto L_0x0023
            android.view.View r3 = r7.getChildAt(r2)
            int r4 = r3.getVisibility()
            r1[r2] = r4
            r4 = r1[r2]
            if (r4 != 0) goto L_0x0020
            int r4 = r3.mViewFlags
            r4 = r4 & -13
            r4 = r4 | 4
            r3.mViewFlags = r4
        L_0x0020:
            int r2 = r2 + 1
            goto L_0x0008
        L_0x0023:
            android.graphics.Bitmap r2 = super.createSnapshot(r8, r9)     // Catch:{ all -> 0x003f }
            if (r9 == 0) goto L_0x003e
            r3 = 0
        L_0x002a:
            if (r3 >= r0) goto L_0x003e
            android.view.View r4 = r7.getChildAt(r3)
            int r5 = r4.mViewFlags
            r5 = r5 & -13
            r6 = r1[r3]
            r6 = r6 & 12
            r5 = r5 | r6
            r4.mViewFlags = r5
            int r3 = r3 + 1
            goto L_0x002a
        L_0x003e:
            return r2
        L_0x003f:
            r2 = move-exception
            if (r9 == 0) goto L_0x0057
            r3 = 0
        L_0x0043:
            if (r3 >= r0) goto L_0x0057
            android.view.View r4 = r7.getChildAt(r3)
            int r5 = r4.mViewFlags
            r5 = r5 & -13
            r6 = r1[r3]
            r6 = r6 & 12
            r5 = r5 | r6
            r4.mViewFlags = r5
            int r3 = r3 + 1
            goto L_0x0043
        L_0x0057:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.ViewGroup.createSnapshot(android.view.ViewDebug$CanvasProvider, boolean):android.graphics.Bitmap");
    }

    /* access modifiers changed from: package-private */
    public boolean isLayoutModeOptical() {
        return this.mLayoutMode == 1;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
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
                x1 = x2;
                x2 = x1;
            }
            if (y1 > y2) {
                y1 = y2;
                y2 = y1;
            }
            canvas.drawRect((float) x1, (float) y1, (float) x2, (float) y2, paint);
        }
    }

    private static int sign(int x) {
        return x >= 0 ? 1 : -1;
    }

    private static void drawCorner(Canvas c, Paint paint, int x1, int y1, int dx, int dy, int lw) {
        fillRect(c, paint, x1, y1, x1 + dx, y1 + (sign(dy) * lw));
        fillRect(c, paint, x1, y1, x1 + (sign(dx) * lw), y1 + dy);
    }

    private static void drawRectCorners(Canvas canvas, int x1, int y1, int x2, int y2, Paint paint, int lineLength, int lineWidth) {
        drawCorner(canvas, paint, x1, y1, lineLength, lineLength, lineWidth);
        drawCorner(canvas, paint, x1, y2, lineLength, -lineLength, lineWidth);
        drawCorner(canvas, paint, x2, y1, -lineLength, lineLength, lineWidth);
        drawCorner(canvas, paint, x2, y2, -lineLength, -lineLength, lineWidth);
    }

    /* access modifiers changed from: private */
    public static void fillDifference(Canvas canvas, int x2, int y2, int x3, int y3, int dx1, int dy1, int dx2, int dy2, Paint paint) {
        int x1 = x2 - dx1;
        int x4 = x3 + dx2;
        fillRect(canvas, paint, x1, y2 - dy1, x4, y2);
        fillRect(canvas, paint, x1, y2, x2, y3);
        fillRect(canvas, paint, x3, y2, x4, y3);
        fillRect(canvas, paint, x1, y3, x4, y3 + dy2);
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
        paint.setColor(-65536);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < getChildCount(); i++) {
            View c = getChildAt(i);
            if (c.getVisibility() != 8) {
                Insets insets = c.getOpticalInsets();
                drawRect(canvas, paint, insets.left + c.getLeft(), insets.top + c.getTop(), (c.getRight() - insets.right) - 1, (c.getBottom() - insets.bottom) - 1);
            }
        }
        paint.setColor(Color.argb(63, 255, 0, 255));
        paint.setStyle(Paint.Style.FILL);
        onDebugDrawMargins(canvas, paint);
        paint.setColor(DEBUG_CORNERS_COLOR);
        paint.setStyle(Paint.Style.FILL);
        int lineLength = dipsToPixels(8);
        int lineWidth = dipsToPixels(1);
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            View c2 = getChildAt(i2);
            if (c2.getVisibility() != 8) {
                drawRectCorners(canvas, c2.getLeft(), c2.getTop(), c2.getRight(), c2.getBottom(), paint, lineLength, lineWidth);
            }
        }
    }

    /* JADX INFO: Multiple debug info for r8v6 android.view.animation.LayoutAnimationController: [D('i' int), D('controller' android.view.animation.LayoutAnimationController)] */
    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void dispatchDraw(Canvas canvas) {
        boolean usingRenderNodeProperties = canvas.isRecordingFor(this.mRenderNode);
        int childrenCount = this.mChildrenCount;
        View[] children = this.mChildren;
        int flags = this.mGroupFlags;
        if ((flags & 8) != 0 && canAnimate()) {
            boolean z = !isHardwareAccelerated();
            for (int i = 0; i < childrenCount; i++) {
                View child = children[i];
                if (child != null && (child.mViewFlags & 12) == 0) {
                    attachLayoutAnimationParameters(child, child.getLayoutParams(), i, childrenCount);
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
            Animation.AnimationListener animationListener = this.mAnimationListener;
            if (animationListener != null) {
                animationListener.onAnimationStart(controller.getAnimation());
            }
        }
        int clipSaveCount = 0;
        boolean customOrder = false;
        boolean clipToPadding = (flags & 34) == 34;
        if (clipToPadding) {
            clipSaveCount = canvas.save(2);
            canvas.clipRect(this.mScrollX + this.mPaddingLeft, this.mScrollY + this.mPaddingTop, ((this.mScrollX + this.mRight) - this.mLeft) - this.mPaddingRight, ((this.mScrollY + this.mBottom) - this.mTop) - this.mPaddingBottom);
        }
        this.mPrivateFlags &= -65;
        this.mGroupFlags &= -5;
        long drawingTime = getDrawingTime();
        if (usingRenderNodeProperties) {
            canvas.insertReorderBarrier();
        }
        List<Integer> list = this.mTransientIndices;
        int transientCount = list == null ? 0 : list.size();
        int transientIndex = transientCount != 0 ? 0 : -1;
        ArrayList<View> preorderedList = usingRenderNodeProperties ? null : buildOrderedChildList();
        if (preorderedList == null && isChildrenDrawingOrderEnabled()) {
            customOrder = true;
        }
        boolean more = false;
        int i2 = 0;
        while (i2 < childrenCount) {
            while (transientIndex >= 0 && this.mTransientIndices.get(transientIndex).intValue() == i2) {
                View transientChild = this.mTransientViews.get(transientIndex);
                if ((transientChild.mViewFlags & 12) == 0 || transientChild.getAnimation() != null) {
                    more |= drawChild(canvas, transientChild, drawingTime);
                }
                int transientIndex2 = transientIndex + 1;
                if (transientIndex2 >= transientCount) {
                    transientIndex2 = -1;
                }
                flags = flags;
            }
            View child2 = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i2, customOrder));
            if ((child2.mViewFlags & 12) == 0 || child2.getAnimation() != null) {
                more |= drawChild(canvas, child2, drawingTime);
            }
            i2++;
            flags = flags;
            childrenCount = childrenCount;
        }
        while (transientIndex >= 0) {
            View transientChild2 = this.mTransientViews.get(transientIndex);
            if ((transientChild2.mViewFlags & 12) == 0 || transientChild2.getAnimation() != null) {
                more |= drawChild(canvas, transientChild2, drawingTime);
            }
            transientIndex++;
            if (transientIndex >= transientCount) {
                break;
            }
        }
        if (preorderedList != null) {
            preorderedList.clear();
        }
        if (this.mDisappearingChildren != null) {
            ArrayList<View> disappearingChildren = this.mDisappearingChildren;
            for (int i3 = disappearingChildren.size() - 1; i3 >= 0; i3--) {
                more |= drawChild(canvas, disappearingChildren.get(i3), drawingTime);
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
        int flags2 = this.mGroupFlags;
        if ((flags2 & 4) == 4) {
            invalidate(true);
        }
        if ((flags2 & 16) == 0 && (flags2 & 512) == 0 && this.mLayoutAnimationController.isDone() && !more) {
            this.mGroupFlags |= 512;
            post(new Runnable() {
                /* class android.view.ViewGroup.AnonymousClass2 */

                public void run() {
                    ViewGroup.this.notifyAnimationListener();
                }
            });
        }
    }

    @Override // android.view.View
    public ViewGroupOverlay getOverlay() {
        if (this.mOverlay == null) {
            this.mOverlay = new ViewGroupOverlay(this.mContext, this);
        }
        return (ViewGroupOverlay) this.mOverlay;
    }

    /* access modifiers changed from: protected */
    public int getChildDrawingOrder(int childCount, int drawingPosition) {
        return drawingPosition;
    }

    public final int getChildDrawingOrder(int drawingPosition) {
        return getChildDrawingOrder(getChildCount(), drawingPosition);
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
        ArrayList<View> arrayList = this.mPreSortedChildren;
        if (arrayList == null) {
            this.mPreSortedChildren = new ArrayList<>(childrenCount);
        } else {
            arrayList.clear();
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
    /* access modifiers changed from: public */
    private void notifyAnimationListener() {
        this.mGroupFlags &= -513;
        this.mGroupFlags |= 16;
        if (this.mAnimationListener != null) {
            post(new Runnable() {
                /* class android.view.ViewGroup.AnonymousClass3 */

                public void run() {
                    ViewGroup.this.mAnimationListener.onAnimationEnd(ViewGroup.this.mLayoutAnimationController.getAnimation());
                }
            });
        }
        invalidate(true);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    @UnsupportedAppUsage
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
    @Override // android.view.View
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

    @Override // android.view.View
    public void dispatchSetSelected(boolean selected) {
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = 0; i < count; i++) {
            children[i].setSelected(selected);
        }
    }

    @Override // android.view.View
    public void dispatchSetActivated(boolean activated) {
        View[] children = this.mChildren;
        int count = this.mChildrenCount;
        for (int i = 0; i < count; i++) {
            children[i].setActivated(activated);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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

    @Override // android.view.View
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
    @Override // android.view.View
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
    @Override // android.view.View
    public <T extends View> T findViewTraversal(int id) {
        T t;
        if (id == this.mID) {
            return this;
        }
        View[] where = this.mChildren;
        int len = this.mChildrenCount;
        for (int i = 0; i < len; i++) {
            View v = where[i];
            if ((v.mPrivateFlags & 8) == 0 && (t = (T) v.findViewById(id)) != null) {
                return t;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public <T extends View> T findViewWithTagTraversal(Object tag) {
        T t;
        if (tag != null && tag.equals(this.mTag)) {
            return this;
        }
        View[] where = this.mChildren;
        int len = this.mChildrenCount;
        for (int i = 0; i < len; i++) {
            View v = where[i];
            if ((v.mPrivateFlags & 8) == 0 && (t = (T) v.findViewWithTag(tag)) != null) {
                return t;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        T t;
        if (predicate.test(this)) {
            return this;
        }
        View[] where = this.mChildren;
        int len = this.mChildrenCount;
        for (int i = 0; i < len; i++) {
            View v = where[i];
            if (!(v == childToSkip || (v.mPrivateFlags & 8) != 0 || (t = (T) v.findViewByPredicate(predicate)) == null)) {
                return t;
            }
        }
        return null;
    }

    @UnsupportedAppUsage
    public void addTransientView(View view, int index) {
        if (index >= 0 && view != null) {
            if (view.mParent == null) {
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
                if (this.mAttachInfo != null) {
                    view.dispatchAttachedToWindow(this.mAttachInfo, this.mViewFlags & 12);
                }
                invalidate(true);
                return;
            }
            throw new IllegalStateException("The specified view already has a parent " + view.mParent);
        }
    }

    @UnsupportedAppUsage
    public void removeTransientView(View view) {
        List<View> list = this.mTransientViews;
        if (list != null) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                if (view == this.mTransientViews.get(i)) {
                    this.mTransientViews.remove(i);
                    this.mTransientIndices.remove(i);
                    view.mParent = null;
                    if (view.mAttachInfo != null) {
                        view.dispatchDetachedFromWindow();
                    }
                    invalidate(true);
                    return;
                }
            }
        }
    }

    @UnsupportedAppUsage
    public int getTransientViewCount() {
        List<Integer> list = this.mTransientIndices;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public int getTransientViewIndex(int position) {
        List<Integer> list;
        if (position < 0 || (list = this.mTransientIndices) == null || position >= list.size()) {
            return -1;
        }
        return this.mTransientIndices.get(position).intValue();
    }

    @UnsupportedAppUsage
    public View getTransientView(int position) {
        List<View> list = this.mTransientViews;
        if (list == null || position >= list.size()) {
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
            if (params == null && (params = generateDefaultLayoutParams()) == null) {
                throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
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

    @Override // android.view.ViewManager
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

    @Override // android.view.ViewManager
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
    @UnsupportedAppUsage
    public void dispatchViewAdded(View child) {
        onViewAdded(child);
        OnHierarchyChangeListener onHierarchyChangeListener = this.mOnHierarchyChangeListener;
        if (onHierarchyChangeListener != null) {
            onHierarchyChangeListener.onChildViewAdded(this, child);
        }
    }

    public void onViewAdded(View child) {
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchViewRemoved(View child) {
        onViewRemoved(child);
        OnHierarchyChangeListener onHierarchyChangeListener = this.mOnHierarchyChangeListener;
        if (onHierarchyChangeListener != null) {
            onHierarchyChangeListener.onChildViewRemoved(this, child);
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
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        clearCachedLayoutMode();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearCachedLayoutMode();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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
            child.mPrivateFlags = (child.mPrivateFlags & -2097153) | 32;
            return true;
        }
        throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
    }

    /* access modifiers changed from: protected */
    public void cleanupLayoutState(View child) {
        child.mPrivateFlags &= -4097;
    }

    private void addViewInner(View child, int index, LayoutParams params, boolean preventRequestLayout) {
        LayoutTransition layoutTransition = this.mTransition;
        if (layoutTransition != null) {
            layoutTransition.cancel(3);
        }
        if (child.getParent() == null) {
            LayoutTransition layoutTransition2 = this.mTransition;
            if (layoutTransition2 != null) {
                layoutTransition2.addChild(this, child);
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
            List<Integer> list = this.mTransientIndices;
            if (list != null) {
                int transientCount = list.size();
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
            int i2 = this.mLastTouchDownIndex;
            if (i2 >= index) {
                this.mLastTouchDownIndex = i2 + 1;
            }
        } else {
            throw new IndexOutOfBoundsException("index=" + index + " count=" + count);
        }
    }

    private void removeFromArray(int index) {
        View[] children = this.mChildren;
        ArrayList<View> arrayList = this.mTransitioningViews;
        if ((arrayList == null || !arrayList.contains(children[index])) && children[index] != null) {
            children[index].mParent = null;
        }
        int count = this.mChildrenCount;
        if (index == count - 1) {
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
        int i3 = this.mLastTouchDownIndex;
        if (i3 == index) {
            this.mLastTouchDownTime = 0;
            this.mLastTouchDownIndex = -1;
        } else if (i3 > index) {
            this.mLastTouchDownIndex = i3 - 1;
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

    @Override // android.view.ViewManager
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
        ArrayList<View> arrayList;
        LayoutTransition layoutTransition = this.mTransition;
        if (layoutTransition != null) {
            layoutTransition.removeChild(this, view);
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
        if (view.getAnimation() != null || ((arrayList = this.mTransitioningViews) != null && arrayList.contains(view))) {
            addDisappearingView(view);
        } else if (view.mAttachInfo != null) {
            view.dispatchDetachedFromWindow();
        }
        int transientCount = 0;
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
        List<Integer> list = this.mTransientIndices;
        if (list != null) {
            transientCount = list.size();
        }
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
        LayoutTransition layoutTransition = this.mTransition;
        if (layoutTransition != null) {
            layoutTransition.addTransitionListener(this.mLayoutTransitionListener);
        }
    }

    public LayoutTransition getLayoutTransition() {
        return this.mTransition;
    }

    private void removeViewsInternal(int start, int count) {
        ArrayList<View> arrayList;
        int end = start + count;
        if (start < 0 || count < 0 || end > this.mChildrenCount) {
            throw new IndexOutOfBoundsException();
        }
        View focused = this.mFocused;
        boolean detach = this.mAttachInfo != null;
        boolean clearChildFocus = false;
        View clearDefaultFocus = null;
        View[] children = this.mChildren;
        for (int i = start; i < end; i++) {
            View view = children[i];
            LayoutTransition layoutTransition = this.mTransition;
            if (layoutTransition != null) {
                layoutTransition.removeChild(this, view);
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
            if (view.getAnimation() != null || ((arrayList = this.mTransitioningViews) != null && arrayList.contains(view))) {
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
        ArrayList<View> arrayList;
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
                LayoutTransition layoutTransition = this.mTransition;
                if (layoutTransition != null) {
                    layoutTransition.removeChild(this, view);
                }
                if (view == focused) {
                    view.unFocus(null);
                    clearChildFocus = true;
                }
                view.clearAccessibilityFocus();
                cancelTouchTarget(view);
                cancelHoverTarget(view);
                if (view.getAnimation() != null || ((arrayList = this.mTransitioningViews) != null && arrayList.contains(view))) {
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
            View view2 = this.mDefaultFocus;
            if (view2 != null) {
                clearDefaultFocus(view2);
            }
            View view3 = this.mFocusedInCluster;
            if (view3 != null) {
                clearFocusedInCluster(view3);
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
        ArrayList<View> arrayList;
        LayoutTransition layoutTransition = this.mTransition;
        if (layoutTransition != null) {
            layoutTransition.removeChild(this, child);
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
        if ((animate && child.getAnimation() != null) || ((arrayList = this.mTransitioningViews) != null && arrayList.contains(child))) {
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
        child.mPrivateFlags = (child.mPrivateFlags & -2097153 & -32769) | 32 | Integer.MIN_VALUE;
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

    @Override // android.view.ViewParent
    public void onDescendantInvalidated(View child, View target) {
        if (!HwFrameworkFactory.getHwApsImpl().isDropEmptyFrame(target)) {
            this.mPrivateFlags |= target.mPrivateFlags & 64;
            if ((target.mPrivateFlags & -2097153) != 0) {
                this.mPrivateFlags = (this.mPrivateFlags & -2097153) | 2097152;
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
    }

    @Override // android.view.ViewParent
    @Deprecated
    public final void invalidateChild(View child, Rect dirty) {
        Matrix childMatrix;
        Matrix transformMatrix;
        View.AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo == null || !attachInfo.mHardwareAccelerated || !HwFrameworkFactory.getHwApsImpl().isNonEmptyFrameCase(this, child)) {
            ViewParent parent = this;
            if (attachInfo != null) {
                boolean z = true;
                boolean drawAnimation = (child.mPrivateFlags & 64) != 0;
                Matrix childMatrix2 = child.getMatrix();
                if (child.mLayerType != 0) {
                    this.mPrivateFlags |= Integer.MIN_VALUE;
                    this.mPrivateFlags &= -32769;
                }
                int[] location = attachInfo.mInvalidateChildLocation;
                location[0] = child.mLeft;
                location[1] = child.mTop;
                if (!childMatrix2.isIdentity() || (this.mGroupFlags & 2048) != 0) {
                    RectF boundingRect = attachInfo.mTmpTransformRect;
                    boundingRect.set(dirty);
                    if ((this.mGroupFlags & 2048) != 0) {
                        Transformation t = attachInfo.mTmpTransformation;
                        if (getChildStaticTransformation(child, t)) {
                            transformMatrix = attachInfo.mTmpMatrix;
                            transformMatrix.set(t.getMatrix());
                            if (!childMatrix2.isIdentity()) {
                                transformMatrix.preConcat(childMatrix2);
                            }
                        } else {
                            transformMatrix = childMatrix2;
                        }
                    } else {
                        transformMatrix = childMatrix2;
                    }
                    transformMatrix.mapRect(boundingRect);
                    dirty.set((int) Math.floor((double) boundingRect.left), (int) Math.floor((double) boundingRect.top), (int) Math.ceil((double) boundingRect.right), (int) Math.ceil((double) boundingRect.bottom));
                }
                while (true) {
                    View view = null;
                    if (parent instanceof View) {
                        view = (View) parent;
                    }
                    if (drawAnimation) {
                        if (view != null) {
                            view.mPrivateFlags |= 64;
                        } else if (parent instanceof ViewRootImpl) {
                            ((ViewRootImpl) parent).mIsAnimating = z;
                        }
                    }
                    if (!(view == null || (view.mPrivateFlags & 2097152) == 2097152)) {
                        view.mPrivateFlags = (view.mPrivateFlags & -2097153) | 2097152;
                    }
                    parent = parent.invalidateChildInParent(location, dirty);
                    if (view != null) {
                        Matrix m = view.getMatrix();
                        if (!m.isIdentity()) {
                            RectF boundingRect2 = attachInfo.mTmpTransformRect;
                            boundingRect2.set(dirty);
                            m.mapRect(boundingRect2);
                            childMatrix = childMatrix2;
                            dirty.set((int) Math.floor((double) boundingRect2.left), (int) Math.floor((double) boundingRect2.top), (int) Math.ceil((double) boundingRect2.right), (int) Math.ceil((double) boundingRect2.bottom));
                        } else {
                            childMatrix = childMatrix2;
                        }
                    } else {
                        childMatrix = childMatrix2;
                    }
                    if (parent != null) {
                        childMatrix2 = childMatrix;
                        z = true;
                    } else {
                        return;
                    }
                }
            }
        } else {
            onDescendantInvalidated(child, child);
        }
    }

    @Override // android.view.ViewParent
    @Deprecated
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        if ((this.mPrivateFlags & 32800) == 0) {
            return null;
        }
        int i = this.mGroupFlags;
        if ((i & 144) != 128) {
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
            if ((i & 1) == 1) {
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
            if (theParent != this) {
                throw new IllegalArgumentException("parameter must be a descendant of this view");
            } else if (offsetFromChildToParent) {
                rect.offset(descendant.mLeft - descendant.mScrollX, descendant.mTop - descendant.mScrollY);
            } else {
                rect.offset(descendant.mScrollX - descendant.mLeft, descendant.mScrollY - descendant.mTop);
            }
        }
    }

    @UnsupportedAppUsage
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

    @Override // android.view.ViewParent
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

    @Override // android.view.View
    public final void layout(int l, int t, int r, int b) {
        LayoutTransition layoutTransition;
        if (this.mSuppressLayout || ((layoutTransition = this.mTransition) != null && layoutTransition.isChangingLayout())) {
            this.mLayoutCalledWhileSuppressed = true;
            return;
        }
        LayoutTransition layoutTransition2 = this.mTransition;
        if (layoutTransition2 != null) {
            layoutTransition2.layoutChange(this);
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
    @Override // android.view.View
    public void invalidateInheritedLayoutMode(int layoutModeOfRoot) {
        int i = this.mLayoutMode;
        if (!(i == -1 || i == layoutModeOfRoot || hasBooleanFlag(8388608))) {
            setLayoutMode(-1, false);
            int N = getChildCount();
            for (int i2 = 0; i2 < N; i2++) {
                getChildAt(i2).invalidateInheritedLayoutMode(layoutModeOfRoot);
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

    /* JADX INFO: Multiple debug info for r0v4 int: [D('output' java.lang.String), D('count' int)] */
    /* access modifiers changed from: protected */
    @Override // android.view.View
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
        if (child.toString().contains("com.tencent.mm.ui.LayoutListenerView")) {
            View rootView = getRootView();
            if ((rootView instanceof DecorView) && WindowConfiguration.isHwMultiStackWindowingMode(((DecorView) rootView).getWindowMode())) {
                lp.height = -1;
            }
        }
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
        ArrayList<View> arrayList = this.mTransitioningViews;
        return arrayList != null && arrayList.contains(view);
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
        ArrayList<View> arrayList = this.mTransitioningViews;
        if (arrayList != null) {
            arrayList.remove(view);
            ArrayList<View> disappearingChildren = this.mDisappearingChildren;
            if (disappearingChildren != null && disappearingChildren.contains(view)) {
                disappearingChildren.remove(view);
                ArrayList<View> arrayList2 = this.mVisibilityChangingChildren;
                if (arrayList2 == null || !arrayList2.contains(view)) {
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

    @Override // android.view.View
    public boolean gatherTransparentRegion(Region region) {
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
            for (int i = 0; i < childrenCount; i++) {
                View child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, customOrder));
                if (((child.mViewFlags & 12) == 0 || child.getAnimation() != null) && !child.gatherTransparentRegion(region)) {
                    noneOfTheChildrenAreTransparent = false;
                }
            }
            if (preorderedList != null) {
                preorderedList.clear();
            }
        }
        return meOpaque || noneOfTheChildrenAreTransparent;
    }

    @Override // android.view.ViewParent
    public void requestTransparentRegion(View child) {
        if (child != null) {
            child.mPrivateFlags |= 512;
            if (this.mParent != null) {
                this.mParent.requestTransparentRegion(this);
            }
        }
    }

    @Override // android.view.ViewParent
    public void subtractObscuredTouchableRegion(Region touchableRegion, View view) {
        int childrenCount = this.mChildrenCount;
        ArrayList<View> preorderedList = buildTouchDispatchChildList();
        boolean customOrder = preorderedList == null && isChildrenDrawingOrderEnabled();
        View[] children = this.mChildren;
        for (int i = childrenCount - 1; i >= 0; i--) {
            View child = getAndVerifyPreorderedView(preorderedList, children, getAndVerifyPreorderedIndex(childrenCount, i, customOrder));
            if (child == view) {
                break;
            }
            if (child.canReceivePointerEvents()) {
                applyOpToRegionByBounds(touchableRegion, child, Region.Op.DIFFERENCE);
            }
        }
        applyOpToRegionByBounds(touchableRegion, this, Region.Op.INTERSECT);
        ViewParent parent = getParent();
        if (parent != null) {
            parent.subtractObscuredTouchableRegion(touchableRegion, this);
        }
    }

    private static void applyOpToRegionByBounds(Region region, View view, Region.Op op) {
        int[] locationInWindow = new int[2];
        view.getLocationInWindow(locationInWindow);
        int x = locationInWindow[0];
        int y = locationInWindow[1];
        region.op(x, y, x + view.getWidth(), y + view.getHeight(), op);
    }

    @Override // android.view.View
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        DecorView decorView;
        WindowInsets insets2 = super.dispatchApplyWindowInsets(insets);
        if (HwFreeFormUtils.isFreeFormEnable() && insets2.isConsumed() && toString().contains("TestTimeForChatting") && getParent() != null && getRootView() != null && getParent().toString().contains("FitSystemWindowLayoutView") && (decorView = (DecorView) getRootView()) != null && !decorView.isFreeform() && decorView.isNeedNavBar()) {
            Insets rect = decorView.getRootWindowInsets().getSystemWindowInsets();
            insets2.replaceSystemWindowInsets(rect.toRect());
            internalSetPadding(rect.left, rect.top, rect.right, rect.bottom);
        }
        if (View.sBrokenInsetsDispatch) {
            return brokenDispatchApplyWindowInsets(insets2);
        }
        return newDispatchApplyWindowInsets(insets2);
    }

    private WindowInsets brokenDispatchApplyWindowInsets(WindowInsets insets) {
        if (!insets.isConsumed()) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                insets = getChildAt(i).dispatchApplyWindowInsets(insets);
                if (insets.isConsumed()) {
                    break;
                }
            }
        }
        return insets;
    }

    private WindowInsets newDispatchApplyWindowInsets(WindowInsets insets) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).dispatchApplyWindowInsets(insets);
        }
        return insets;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
    public void dispatchWindowInsetsAnimationStarted(WindowInsetsAnimationListener.InsetsAnimation animation) {
        super.dispatchWindowInsetsAnimationStarted(animation);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).dispatchWindowInsetsAnimationStarted(animation);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
    public WindowInsets dispatchWindowInsetsAnimationProgress(WindowInsets insets) {
        WindowInsets insets2 = super.dispatchWindowInsetsAnimationProgress(insets);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).dispatchWindowInsetsAnimationProgress(insets2);
        }
        return insets2;
    }

    /* access modifiers changed from: package-private */
    @Override // android.view.View
    public void dispatchWindowInsetsAnimationFinished(WindowInsetsAnimationListener.InsetsAnimation animation) {
        super.dispatchWindowInsetsAnimationFinished(animation);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).dispatchWindowInsetsAnimationFinished(animation);
        }
    }

    public Animation.AnimationListener getLayoutAnimationListener() {
        return this.mAnimationListener;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void drawableStateChanged() {
        super.drawableStateChanged();
        int i = this.mGroupFlags;
        if ((65536 & i) == 0) {
            return;
        }
        if ((i & 8192) == 0) {
            View[] children = this.mChildren;
            int count = this.mChildrenCount;
            for (int i2 = 0; i2 < count; i2++) {
                View child = children[i2];
                if ((child.mViewFlags & 4194304) != 0) {
                    child.refreshDrawableState();
                }
            }
            return;
        }
        throw new IllegalStateException("addStateFromChildren cannot be enabled if a child has duplicateParentState set to true");
    }

    @Override // android.view.View
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
    @Override // android.view.View
    public int[] onCreateDrawableState(int extraSpace) {
        if ((this.mGroupFlags & 8192) == 0) {
            return super.onCreateDrawableState(extraSpace);
        }
        int need = 0;
        int n = getChildCount();
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

    @Override // android.view.ViewParent
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

    @Override // android.view.View
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

    @Override // android.view.View
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

    @Override // android.view.View
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

    @Override // android.view.View
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

    @Override // android.view.View
    @UnsupportedAppUsage
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
    @Override // android.view.View
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

    @Override // android.view.View
    public void resolveLayoutParams() {
        super.resolveLayoutParams();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).resolveLayoutParams();
        }
    }

    @Override // android.view.View
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

    @Override // android.view.View
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

    @Override // android.view.View
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

    @Override // android.view.View
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
    @Override // android.view.View
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

    @Override // android.view.ViewParent
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return false;
    }

    @Override // android.view.ViewParent
    public void onNestedScrollAccepted(View child, View target, int axes) {
        this.mNestedScrollAxes = axes;
    }

    @Override // android.view.ViewParent
    public void onStopNestedScroll(View child) {
        stopNestedScroll();
        this.mNestedScrollAxes = 0;
    }

    @Override // android.view.ViewParent
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null);
    }

    @Override // android.view.ViewParent
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        dispatchNestedPreScroll(dx, dy, consumed, null);
    }

    @Override // android.view.ViewParent
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override // android.view.ViewParent
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

    @Override // android.view.View
    public void captureTransitioningViews(List<View> transitioningViews) {
        if (getVisibility() == 0) {
            if (isTransitionGroup()) {
                transitioningViews.add(this);
                return;
            }
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).captureTransitioningViews(transitioningViews);
            }
        }
    }

    @Override // android.view.View
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
    @Override // android.view.View
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
    @Override // android.view.View
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
        if (onUnhandledKeyEvent(evt)) {
            return this;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static final class TouchTarget {
        public static final int ALL_POINTER_IDS = -1;
        private static final int MAX_RECYCLED = 32;
        private static TouchTarget sRecycleBin;
        private static final Object sRecycleLock = new Object[0];
        private static int sRecycledCount;
        @UnsupportedAppUsage
        public View child;
        public TouchTarget next;
        public int pointerIdBits;

        @UnsupportedAppUsage
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
                target.child = child2;
                target.pointerIdBits = pointerIdBits2;
                return target;
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

    /* access modifiers changed from: private */
    public static final class HoverTarget {
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
                target.child = child2;
                return target;
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

    /* access modifiers changed from: private */
    public static class ChildListForAutofill extends ArrayList<View> {
        private static final int MAX_POOL_SIZE = 32;
        private static final Pools.SimplePool<ChildListForAutofill> sPool = new Pools.SimplePool<>(32);

        private ChildListForAutofill() {
        }

        public static ChildListForAutofill obtain() {
            ChildListForAutofill list = sPool.acquire();
            if (list == null) {
                return new ChildListForAutofill();
            }
            return list;
        }

        public void recycle() {
            clear();
            sPool.release(this);
        }
    }

    /* access modifiers changed from: package-private */
    public static class ChildListForAccessibility {
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

    /* access modifiers changed from: package-private */
    public static class ViewLocationHolder implements Comparable<ViewLocationHolder> {
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
            View child1 = holder1.mView.findViewByPredicateTraversal(new Predicate(view1Bounds) {
                /* class android.view.$$Lambda$ViewGroup$ViewLocationHolder$QbO7cM0ULKe25a7bfXG3VH6DB0c */
                private final /* synthetic */ Rect f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    Rect rect = Rect.this;
                    Rect rect2 = this.f$1;
                    return ((View) obj).getBoundsOnScreen(rect, true);
                }
            }, null);
            View child2 = holder2.mView.findViewByPredicateTraversal(new Predicate(view2Bounds) {
                /* class android.view.$$Lambda$ViewGroup$ViewLocationHolder$AjKvqdj7SGGIzA5qrlZUuu71jl8 */
                private final /* synthetic */ Rect f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    Rect rect = Rect.this;
                    Rect rect2 = this.f$1;
                    return ((View) obj).getBoundsOnScreen(rect, true);
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

    private static void drawRect(Canvas canvas, Paint paint, int x1, int y1, int x2, int y2) {
        if (sDebugLines == null) {
            sDebugLines = new float[16];
        }
        float[] fArr = sDebugLines;
        fArr[0] = (float) x1;
        fArr[1] = (float) y1;
        fArr[2] = (float) x2;
        fArr[3] = (float) y1;
        fArr[4] = (float) x2;
        fArr[5] = (float) y1;
        fArr[6] = (float) x2;
        fArr[7] = (float) y2;
        fArr[8] = (float) x2;
        fArr[9] = (float) y2;
        fArr[10] = (float) x1;
        fArr[11] = (float) y2;
        fArr[12] = (float) x1;
        fArr[13] = (float) y2;
        fArr[14] = (float) x1;
        fArr[15] = (float) y1;
        canvas.drawLines(fArr, paint);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    @UnsupportedAppUsage
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

    public void attachHwViewToParent(View child, int index, LayoutParams params) {
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params);
        }
        child.setLayoutParams(params);
        attachViewToParent(child, index, params);
    }

    public void detachHwViewFromParent(View child) {
        detachViewFromParent(child);
    }
}
