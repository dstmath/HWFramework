package android.view;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.common.HwFrameworkFactory;
import android.content.ClipData;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Canvas.EdgeType;
import android.graphics.Insets;
import android.graphics.Interpolator;
import android.graphics.Interpolator.Result;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.telephony.PhoneNumberUtils;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatProperty;
import android.util.Log;
import android.util.LongSparseLongArray;
import android.util.Pools.SynchronizedPool;
import android.util.Property;
import android.util.SparseArray;
import android.util.StateSet;
import android.util.SuperNotCalledException;
import android.util.TypedValue;
import android.view.AccessibilityIterators.TextSegmentIterator;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent.DispatcherState;
import android.view.ViewDebug.CapturedViewProperty;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.FlagToString;
import android.view.ViewDebug.IntToString;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.Window.OnFrameMetricsAvailableListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityEventSource;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Checkable;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ScrollBarDrawable;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Predicate;
import com.android.internal.util.Protocol;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.widget.ScrollBarUtils;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.android.animation.HwStateListAnimator;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.microedition.khronos.opengles.GL10;

public class View implements Callback, KeyEvent.Callback, AccessibilityEventSource {
    public static final int ACCESSIBILITY_CURSOR_POSITION_UNDEFINED = -1;
    public static final int ACCESSIBILITY_LIVE_REGION_ASSERTIVE = 2;
    static final int ACCESSIBILITY_LIVE_REGION_DEFAULT = 0;
    public static final int ACCESSIBILITY_LIVE_REGION_NONE = 0;
    public static final int ACCESSIBILITY_LIVE_REGION_POLITE = 1;
    static final int ALL_RTL_PROPERTIES_RESOLVED = 1610678816;
    public static final Property<View, Float> ALPHA = null;
    static final int CLICKABLE = 16384;
    static final int CONTEXT_CLICKABLE = 8388608;
    private static final boolean DBG = false;
    public static final String DEBUG_LAYOUT_PROPERTY = "debug.layout";
    static final int DISABLED = 32;
    public static final int DRAG_FLAG_GLOBAL = 256;
    public static final int DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION = 64;
    public static final int DRAG_FLAG_GLOBAL_PREFIX_URI_PERMISSION = 128;
    public static final int DRAG_FLAG_GLOBAL_URI_READ = 1;
    public static final int DRAG_FLAG_GLOBAL_URI_WRITE = 2;
    public static final int DRAG_FLAG_OPAQUE = 512;
    static final int DRAG_MASK = 3;
    static final int DRAWING_CACHE_ENABLED = 32768;
    public static final int DRAWING_CACHE_QUALITY_AUTO = 0;
    private static final int[] DRAWING_CACHE_QUALITY_FLAGS = null;
    public static final int DRAWING_CACHE_QUALITY_HIGH = 1048576;
    public static final int DRAWING_CACHE_QUALITY_LOW = 524288;
    static final int DRAWING_CACHE_QUALITY_MASK = 1572864;
    static final int DRAW_MASK = 128;
    static final int DUPLICATE_PARENT_STATE = 4194304;
    protected static final int[] EMPTY_STATE_SET = null;
    static final int ENABLED = 0;
    protected static final int[] ENABLED_FOCUSED_SELECTED_STATE_SET = null;
    protected static final int[] ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = null;
    protected static final int[] ENABLED_FOCUSED_STATE_SET = null;
    protected static final int[] ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET = null;
    static final int ENABLED_MASK = 32;
    protected static final int[] ENABLED_SELECTED_STATE_SET = null;
    protected static final int[] ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET = null;
    protected static final int[] ENABLED_STATE_SET = null;
    protected static final int[] ENABLED_WINDOW_FOCUSED_STATE_SET = null;
    static final int FADING_EDGE_HORIZONTAL = 4096;
    static final int FADING_EDGE_MASK = 12288;
    static final int FADING_EDGE_NONE = 0;
    static final int FADING_EDGE_VERTICAL = 8192;
    static final int FILTER_TOUCHES_WHEN_OBSCURED = 1024;
    public static final int FIND_VIEWS_WITH_ACCESSIBILITY_NODE_PROVIDERS = 4;
    public static final int FIND_VIEWS_WITH_CONTENT_DESCRIPTION = 2;
    public static final int FIND_VIEWS_WITH_TEXT = 1;
    private static final int FITS_SYSTEM_WINDOWS = 2;
    private static final int FOCUSABLE = 1;
    public static final int FOCUSABLES_ALL = 0;
    public static final int FOCUSABLES_TOUCH_MODE = 1;
    static final int FOCUSABLE_IN_TOUCH_MODE = 262144;
    private static final int FOCUSABLE_MASK = 1;
    protected static final int[] FOCUSED_SELECTED_STATE_SET = null;
    protected static final int[] FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = null;
    protected static final int[] FOCUSED_STATE_SET = null;
    protected static final int[] FOCUSED_WINDOW_FOCUSED_STATE_SET = null;
    public static final int FOCUS_BACKWARD = 1;
    public static final int FOCUS_DOWN = 130;
    public static final int FOCUS_FORWARD = 2;
    public static final int FOCUS_LEFT = 17;
    public static final int FOCUS_RIGHT = 66;
    public static final int FOCUS_UP = 33;
    public static final int GONE = 8;
    public static final int HAPTIC_FEEDBACK_ENABLED = 268435456;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_AUTO = 0;
    static final int IMPORTANT_FOR_ACCESSIBILITY_DEFAULT = 0;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_NO = 2;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS = 4;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_YES = 1;
    public static final int INVISIBLE = 4;
    public static final int KEEP_SCREEN_ON = 67108864;
    public static final int LAYER_TYPE_HARDWARE = 2;
    public static final int LAYER_TYPE_NONE = 0;
    public static final int LAYER_TYPE_SOFTWARE = 1;
    private static final int LAYOUT_DIRECTION_DEFAULT = 2;
    private static final int[] LAYOUT_DIRECTION_FLAGS = null;
    public static final int LAYOUT_DIRECTION_INHERIT = 2;
    public static final int LAYOUT_DIRECTION_LOCALE = 3;
    public static final int LAYOUT_DIRECTION_LTR = 0;
    static final int LAYOUT_DIRECTION_RESOLVED_DEFAULT = 0;
    public static final int LAYOUT_DIRECTION_RTL = 1;
    public static final int LAYOUT_DIRECTION_UNDEFINED = -1;
    static final int LONG_CLICKABLE = 2097152;
    public static final int MEASURED_HEIGHT_STATE_SHIFT = 16;
    public static final int MEASURED_SIZE_MASK = 16777215;
    public static final int MEASURED_STATE_MASK = -16777216;
    public static final int MEASURED_STATE_TOO_SMALL = 16777216;
    public static final int NAVIGATION_BAR_TRANSIENT = 134217728;
    public static final int NAVIGATION_BAR_TRANSLUCENT = Integer.MIN_VALUE;
    public static final int NAVIGATION_BAR_TRANSPARENT = 32768;
    public static final int NAVIGATION_BAR_UNHIDE = 536870912;
    private static final int NOT_FOCUSABLE = 0;
    public static final int NO_ID = -1;
    static final int OPTIONAL_FITS_SYSTEM_WINDOWS = 2048;
    public static final int OVER_SCROLL_ALWAYS = 0;
    public static final int OVER_SCROLL_IF_CONTENT_SCROLLS = 1;
    public static final int OVER_SCROLL_NEVER = 2;
    static final int PARENT_SAVE_DISABLED = 536870912;
    static final int PARENT_SAVE_DISABLED_MASK = 536870912;
    static final int PFLAG2_ACCESSIBILITY_FOCUSED = 67108864;
    static final int PFLAG2_ACCESSIBILITY_LIVE_REGION_MASK = 25165824;
    static final int PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT = 23;
    static final int PFLAG2_DRAG_CAN_ACCEPT = 1;
    static final int PFLAG2_DRAG_HOVERED = 2;
    static final int PFLAG2_DRAWABLE_RESOLVED = 1073741824;
    static final int PFLAG2_HAS_TRANSIENT_STATE = Integer.MIN_VALUE;
    static final int PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_MASK = 7340032;
    static final int PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_SHIFT = 20;
    static final int PFLAG2_LAYOUT_DIRECTION_MASK = 12;
    static final int PFLAG2_LAYOUT_DIRECTION_MASK_SHIFT = 2;
    static final int PFLAG2_LAYOUT_DIRECTION_RESOLVED = 32;
    static final int PFLAG2_LAYOUT_DIRECTION_RESOLVED_MASK = 48;
    static final int PFLAG2_LAYOUT_DIRECTION_RESOLVED_RTL = 16;
    static final int PFLAG2_PADDING_RESOLVED = 536870912;
    static final int PFLAG2_SUBTREE_ACCESSIBILITY_STATE_CHANGED = 134217728;
    private static final int[] PFLAG2_TEXT_ALIGNMENT_FLAGS = null;
    static final int PFLAG2_TEXT_ALIGNMENT_MASK = 57344;
    static final int PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT = 13;
    static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED = 65536;
    private static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED_DEFAULT = 131072;
    static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK = 917504;
    static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK_SHIFT = 17;
    private static final int[] PFLAG2_TEXT_DIRECTION_FLAGS = null;
    static final int PFLAG2_TEXT_DIRECTION_MASK = 448;
    static final int PFLAG2_TEXT_DIRECTION_MASK_SHIFT = 6;
    static final int PFLAG2_TEXT_DIRECTION_RESOLVED = 512;
    static final int PFLAG2_TEXT_DIRECTION_RESOLVED_DEFAULT = 1024;
    static final int PFLAG2_TEXT_DIRECTION_RESOLVED_MASK = 7168;
    static final int PFLAG2_TEXT_DIRECTION_RESOLVED_MASK_SHIFT = 10;
    static final int PFLAG2_VIEW_QUICK_REJECTED = 268435456;
    static final int PFLAG3_APPLYING_INSETS = 32;
    static final int PFLAG3_ASSIST_BLOCKED = 16384;
    static final int PFLAG3_CALLED_SUPER = 16;
    static final int PFLAG3_FITTING_SYSTEM_WINDOWS = 64;
    private static final int PFLAG3_HAS_OVERLAPPING_RENDERING_FORCED = 16777216;
    static final int PFLAG3_IS_LAID_OUT = 4;
    static final int PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT = 8;
    static final int PFLAG3_NESTED_SCROLLING_ENABLED = 128;
    private static final int PFLAG3_OVERLAPPING_RENDERING_FORCED_VALUE = 8388608;
    static final int PFLAG3_POINTER_ICON_LSHIFT = 15;
    static final int PFLAG3_POINTER_ICON_MASK = 8355840;
    private static final int PFLAG3_POINTER_ICON_NOT_SPECIFIED = 0;
    private static final int PFLAG3_POINTER_ICON_NULL = 32768;
    private static final int PFLAG3_POINTER_ICON_VALUE_START = 65536;
    static final int PFLAG3_SCROLL_INDICATOR_BOTTOM = 512;
    static final int PFLAG3_SCROLL_INDICATOR_END = 8192;
    static final int PFLAG3_SCROLL_INDICATOR_LEFT = 1024;
    static final int PFLAG3_SCROLL_INDICATOR_RIGHT = 2048;
    static final int PFLAG3_SCROLL_INDICATOR_START = 4096;
    static final int PFLAG3_SCROLL_INDICATOR_TOP = 256;
    static final int PFLAG3_TEMPORARY_DETACH = 33554432;
    static final int PFLAG3_VIEW_IS_ANIMATING_ALPHA = 2;
    static final int PFLAG3_VIEW_IS_ANIMATING_TRANSFORM = 1;
    static final int PFLAG_ACTIVATED = 1073741824;
    static final int PFLAG_ALPHA_SET = 262144;
    static final int PFLAG_ANIMATION_STARTED = 65536;
    private static final int PFLAG_AWAKEN_SCROLL_BARS_ON_ATTACH = 134217728;
    static final int PFLAG_CANCEL_NEXT_UP_EVENT = 67108864;
    static final int PFLAG_DIRTY = 2097152;
    static final int PFLAG_DIRTY_MASK = 6291456;
    static final int PFLAG_DIRTY_OPAQUE = 4194304;
    private static final int PFLAG_DOES_NOTHING_REUSE_PLEASE = 536870912;
    static final int PFLAG_DRAWABLE_STATE_DIRTY = 1024;
    static final int PFLAG_DRAWING_CACHE_VALID = 32768;
    static final int PFLAG_DRAWN = 32;
    static final int PFLAG_DRAW_ANIMATION = 64;
    static final int PFLAG_FOCUSED = 2;
    static final int PFLAG_FORCE_LAYOUT = 4096;
    static final int PFLAG_HAS_BOUNDS = 16;
    private static final int PFLAG_HOVERED = 268435456;
    static final int PFLAG_INVALIDATED = Integer.MIN_VALUE;
    static final int PFLAG_IS_ROOT_NAMESPACE = 8;
    static final int PFLAG_LAYOUT_REQUIRED = 8192;
    static final int PFLAG_MEASURED_DIMENSION_SET = 2048;
    static final int PFLAG_OPAQUE_BACKGROUND = 8388608;
    static final int PFLAG_OPAQUE_MASK = 25165824;
    static final int PFLAG_OPAQUE_SCROLLBARS = 16777216;
    private static final int PFLAG_PREPRESSED = 33554432;
    private static final int PFLAG_PRESSED = 16384;
    static final int PFLAG_REQUEST_TRANSPARENT_REGIONS = 512;
    private static final int PFLAG_SAVE_STATE_CALLED = 131072;
    static final int PFLAG_SCROLL_CONTAINER = 524288;
    static final int PFLAG_SCROLL_CONTAINER_ADDED = 1048576;
    static final int PFLAG_SELECTED = 4;
    static final int PFLAG_SKIP_DRAW = 128;
    static final int PFLAG_WANTS_FOCUS = 1;
    private static final int POPULATING_ACCESSIBILITY_EVENT_TYPES = 172479;
    protected static final int[] PRESSED_ENABLED_FOCUSED_SELECTED_STATE_SET = null;
    protected static final int[] PRESSED_ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = null;
    protected static final int[] PRESSED_ENABLED_FOCUSED_STATE_SET = null;
    protected static final int[] PRESSED_ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET = null;
    protected static final int[] PRESSED_ENABLED_SELECTED_STATE_SET = null;
    protected static final int[] PRESSED_ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET = null;
    protected static final int[] PRESSED_ENABLED_STATE_SET = null;
    protected static final int[] PRESSED_ENABLED_WINDOW_FOCUSED_STATE_SET = null;
    protected static final int[] PRESSED_FOCUSED_SELECTED_STATE_SET = null;
    protected static final int[] PRESSED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = null;
    protected static final int[] PRESSED_FOCUSED_STATE_SET = null;
    protected static final int[] PRESSED_FOCUSED_WINDOW_FOCUSED_STATE_SET = null;
    protected static final int[] PRESSED_SELECTED_STATE_SET = null;
    protected static final int[] PRESSED_SELECTED_WINDOW_FOCUSED_STATE_SET = null;
    protected static final int[] PRESSED_STATE_SET = null;
    protected static final int[] PRESSED_WINDOW_FOCUSED_STATE_SET = null;
    private static final int PROVIDER_BACKGROUND = 0;
    private static final int PROVIDER_BOUNDS = 2;
    private static final int PROVIDER_NONE = 1;
    private static final int PROVIDER_PADDED_BOUNDS = 3;
    public static final int PUBLIC_STATUS_BAR_VISIBILITY_MASK = 16375;
    public static final int RECENT_APPS_VISIBLE = 16384;
    public static final Property<View, Float> ROTATION = null;
    public static final Property<View, Float> ROTATION_X = null;
    public static final Property<View, Float> ROTATION_Y = null;
    static final int SAVE_DISABLED = 65536;
    static final int SAVE_DISABLED_MASK = 65536;
    public static final Property<View, Float> SCALE_X = null;
    public static final Property<View, Float> SCALE_Y = null;
    public static final int SCREEN_STATE_OFF = 0;
    public static final int SCREEN_STATE_ON = 1;
    static final int SCROLLBARS_HORIZONTAL = 256;
    static final int SCROLLBARS_INSET_MASK = 16777216;
    public static final int SCROLLBARS_INSIDE_INSET = 16777216;
    public static final int SCROLLBARS_INSIDE_OVERLAY = 0;
    static final int SCROLLBARS_MASK = 768;
    static final int SCROLLBARS_NONE = 0;
    public static final int SCROLLBARS_OUTSIDE_INSET = 50331648;
    static final int SCROLLBARS_OUTSIDE_MASK = 33554432;
    public static final int SCROLLBARS_OUTSIDE_OVERLAY = 33554432;
    static final int SCROLLBARS_STYLE_MASK = 50331648;
    static final int SCROLLBARS_VERTICAL = 512;
    public static final int SCROLLBAR_POSITION_DEFAULT = 0;
    public static final int SCROLLBAR_POSITION_LEFT = 1;
    public static final int SCROLLBAR_POSITION_RIGHT = 2;
    public static final int SCROLL_AXIS_HORIZONTAL = 1;
    public static final int SCROLL_AXIS_NONE = 0;
    public static final int SCROLL_AXIS_VERTICAL = 2;
    static final int SCROLL_INDICATORS_NONE = 0;
    static final int SCROLL_INDICATORS_PFLAG3_MASK = 16128;
    static final int SCROLL_INDICATORS_TO_PFLAGS3_LSHIFT = 8;
    public static final int SCROLL_INDICATOR_BOTTOM = 2;
    public static final int SCROLL_INDICATOR_END = 32;
    public static final int SCROLL_INDICATOR_LEFT = 4;
    public static final int SCROLL_INDICATOR_RIGHT = 8;
    public static final int SCROLL_INDICATOR_START = 16;
    public static final int SCROLL_INDICATOR_TOP = 1;
    protected static final int[] SELECTED_STATE_SET = null;
    protected static final int[] SELECTED_WINDOW_FOCUSED_STATE_SET = null;
    public static final int SOUND_EFFECTS_ENABLED = 134217728;
    public static final int STATUS_BAR_DISABLE_BACK = 4194304;
    public static final int STATUS_BAR_DISABLE_CLOCK = 8388608;
    public static final int STATUS_BAR_DISABLE_EXPAND = 65536;
    public static final int STATUS_BAR_DISABLE_HOME = 2097152;
    public static final int STATUS_BAR_DISABLE_NOTIFICATION_ALERTS = 262144;
    public static final int STATUS_BAR_DISABLE_NOTIFICATION_ICONS = 131072;
    public static final int STATUS_BAR_DISABLE_NOTIFICATION_TICKER = 524288;
    public static final int STATUS_BAR_DISABLE_RECENT = 16777216;
    public static final int STATUS_BAR_DISABLE_SEARCH = 33554432;
    public static final int STATUS_BAR_DISABLE_SYSTEM_INFO = 1048576;
    public static final int STATUS_BAR_HIDDEN = 1;
    public static final int STATUS_BAR_TRANSIENT = 67108864;
    public static final int STATUS_BAR_TRANSLUCENT = 1073741824;
    public static final int STATUS_BAR_TRANSPARENT = 8;
    public static final int STATUS_BAR_UNHIDE = 268435456;
    public static final int STATUS_BAR_VISIBLE = 0;
    public static final int SYSTEM_UI_CLEARABLE_FLAGS = 7;
    public static final int SYSTEM_UI_FLAG_FULLSCREEN = 4;
    public static final int SYSTEM_UI_FLAG_HIDE_NAVIGATION = 2;
    public static final int SYSTEM_UI_FLAG_IMMERSIVE = 2048;
    public static final int SYSTEM_UI_FLAG_IMMERSIVE_STICKY = 4096;
    public static final int SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = 1024;
    public static final int SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = 512;
    public static final int SYSTEM_UI_FLAG_LAYOUT_STABLE = 256;
    public static final int SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = 8192;
    public static final int SYSTEM_UI_FLAG_LOW_PROFILE = 1;
    public static final int SYSTEM_UI_FLAG_VISIBLE = 0;
    public static final int SYSTEM_UI_LAYOUT_FLAGS = 1536;
    public static final int SYSTEM_UI_TRANSPARENT = 32776;
    public static final int TEXT_ALIGNMENT_CENTER = 4;
    private static final int TEXT_ALIGNMENT_DEFAULT = 1;
    public static final int TEXT_ALIGNMENT_GRAVITY = 1;
    public static final int TEXT_ALIGNMENT_INHERIT = 0;
    static final int TEXT_ALIGNMENT_RESOLVED_DEFAULT = 1;
    public static final int TEXT_ALIGNMENT_TEXT_END = 3;
    public static final int TEXT_ALIGNMENT_TEXT_START = 2;
    public static final int TEXT_ALIGNMENT_VIEW_END = 6;
    public static final int TEXT_ALIGNMENT_VIEW_START = 5;
    public static final int TEXT_DIRECTION_ANY_RTL = 2;
    private static final int TEXT_DIRECTION_DEFAULT = 0;
    public static final int TEXT_DIRECTION_FIRST_STRONG = 1;
    public static final int TEXT_DIRECTION_FIRST_STRONG_LTR = 6;
    public static final int TEXT_DIRECTION_FIRST_STRONG_RTL = 7;
    public static final int TEXT_DIRECTION_INHERIT = 0;
    public static final int TEXT_DIRECTION_LOCALE = 5;
    public static final int TEXT_DIRECTION_LTR = 3;
    static final int TEXT_DIRECTION_RESOLVED_DEFAULT = 1;
    public static final int TEXT_DIRECTION_RTL = 4;
    public static final Property<View, Float> TRANSLATION_X = null;
    public static final Property<View, Float> TRANSLATION_Y = null;
    public static final Property<View, Float> TRANSLATION_Z = null;
    public static final int TV_PICTURE_IN_PICTURE_VISIBLE = 65536;
    private static final int UNDEFINED_PADDING = Integer.MIN_VALUE;
    private static final int VIEW_IS_ADVIEW = 0;
    private static final int VIEW_IS_NOT_ADVIEW_OR_DISABLE = 1;
    private static final int VIEW_IS_NOT_CHECKED = -1;
    protected static final String VIEW_LOG_TAG = "View";
    private static final int[] VISIBILITY_FLAGS = null;
    static final int VISIBILITY_MASK = 12;
    public static final int VISIBLE = 0;
    static final int WILL_NOT_CACHE_DRAWING = 131072;
    static final int WILL_NOT_DRAW = 128;
    protected static final int[] WINDOW_FOCUSED_STATE_SET = null;
    public static final Property<View, Float> X = null;
    public static final Property<View, Float> Y = null;
    public static final Property<View, Float> Z = null;
    private static SparseArray<String> mAttributeMap;
    public static boolean mDebugViewAttributes;
    private static boolean sAlwaysRemeasureExactly;
    private static boolean sCompatibilityDone;
    private static boolean sIgnoreMeasureCache;
    private static boolean sLayoutParamsAlwaysChanged;
    private static int sNextAccessibilityViewId;
    private static final AtomicInteger sNextGeneratedId = null;
    static boolean sTextureViewIgnoresDrawableSetters;
    static final ThreadLocal<Rect> sThreadLocal = null;
    private static boolean sUseBrokenMakeMeasureSpec;
    static boolean sUseZeroUnspecifiedMeasureSpec;
    private int mAccessibilityCursorPosition;
    AccessibilityDelegate mAccessibilityDelegate;
    private int mAccessibilityTraversalAfterId;
    private int mAccessibilityTraversalBeforeId;
    int mAccessibilityViewId;
    private ViewPropertyAnimator mAnimator;
    AttachInfo mAttachInfo;
    @ExportedProperty(category = "attributes", hasAdjacentMapping = true)
    public String[] mAttributes;
    @ExportedProperty(deepExport = true, prefix = "bg_")
    private Drawable mBackground;
    private RenderNode mBackgroundRenderNode;
    private int mBackgroundResource;
    private boolean mBackgroundSizeChanged;
    private TintInfo mBackgroundTint;
    @ExportedProperty(category = "layout")
    protected int mBottom;
    public boolean mCachingFailed;
    private boolean mCanTouchInOtherThread;
    Rect mClipBounds;
    private CharSequence mContentDescription;
    @ExportedProperty(deepExport = true)
    protected Context mContext;
    protected Animation mCurrentAnimation;
    private int[] mDrawableState;
    private Bitmap mDrawingCache;
    private int mDrawingCacheBackgroundColor;
    private ViewTreeObserver mFloatingTreeObserver;
    private boolean mForceRTL;
    @ExportedProperty(deepExport = true, prefix = "fg_")
    private ForegroundInfo mForegroundInfo;
    private ArrayList<FrameMetricsObserver> mFrameMetricsObservers;
    GhostView mGhostView;
    private boolean mHasPerformedLongPress;
    private HwStateListAnimator mHwStateListAnimator;
    @ExportedProperty(resolveId = true)
    int mID;
    private boolean mIgnoreNextUpEvent;
    private boolean mInContextButtonPress;
    protected final InputEventConsistencyVerifier mInputEventConsistencyVerifier;
    private SparseArray<Object> mKeyedTags;
    private int mLabelForId;
    private boolean mLastIsOpaque;
    Paint mLayerPaint;
    @ExportedProperty(category = "drawing", mapping = {@IntToString(from = 0, to = "NONE"), @IntToString(from = 1, to = "SOFTWARE"), @IntToString(from = 2, to = "HARDWARE")})
    int mLayerType;
    private Insets mLayoutInsets;
    protected LayoutParams mLayoutParams;
    @ExportedProperty(category = "layout")
    protected int mLeft;
    private boolean mLeftPaddingDefined;
    ListenerInfo mListenerInfo;
    private float mLongClickX;
    private float mLongClickY;
    private MatchIdPredicate mMatchIdPredicate;
    private MatchLabelForPredicate mMatchLabelForPredicate;
    private LongSparseLongArray mMeasureCache;
    @ExportedProperty(category = "measurement")
    int mMeasuredHeight;
    @ExportedProperty(category = "measurement")
    int mMeasuredWidth;
    @ExportedProperty(category = "measurement")
    private int mMinHeight;
    @ExportedProperty(category = "measurement")
    private int mMinWidth;
    private ViewParent mNestedScrollingParent;
    private int mNextFocusDownId;
    int mNextFocusForwardId;
    private int mNextFocusLeftId;
    private int mNextFocusRightId;
    private int mNextFocusUpId;
    int mOldHeightMeasureSpec;
    int mOldWidthMeasureSpec;
    ViewOutlineProvider mOutlineProvider;
    private int mOverScrollMode;
    ViewOverlay mOverlay;
    @ExportedProperty(category = "padding")
    protected int mPaddingBottom;
    @ExportedProperty(category = "padding")
    protected int mPaddingLeft;
    @ExportedProperty(category = "padding")
    protected int mPaddingRight;
    @ExportedProperty(category = "padding")
    protected int mPaddingTop;
    protected ViewParent mParent;
    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap;
    private PerformClick mPerformClick;
    private PointerIcon mPointerIcon;
    @ExportedProperty(flagMapping = {@FlagToString(equals = 4096, mask = 4096, name = "FORCE_LAYOUT"), @FlagToString(equals = 8192, mask = 8192, name = "LAYOUT_REQUIRED"), @FlagToString(equals = 32768, mask = 32768, name = "DRAWING_CACHE_INVALID", outputIf = false), @FlagToString(equals = 32, mask = 32, name = "DRAWN", outputIf = true), @FlagToString(equals = 32, mask = 32, name = "NOT_DRAWN", outputIf = false), @FlagToString(equals = 4194304, mask = 6291456, name = "DIRTY_OPAQUE"), @FlagToString(equals = 2097152, mask = 6291456, name = "DIRTY")}, formatToHexString = true)
    int mPrivateFlags;
    int mPrivateFlags2;
    int mPrivateFlags3;
    boolean mRecreateDisplayList;
    final RenderNode mRenderNode;
    private final Resources mResources;
    @ExportedProperty(category = "layout")
    protected int mRight;
    private boolean mRightPaddingDefined;
    private HandlerActionQueue mRunQueue;
    private ScrollabilityCache mScrollCache;
    private Drawable mScrollIndicatorDrawable;
    @ExportedProperty(category = "scrolling")
    protected int mScrollX;
    @ExportedProperty(category = "scrolling")
    protected int mScrollY;
    private SendViewScrolledAccessibilityEvent mSendViewScrolledAccessibilityEvent;
    SendViewStateChangedAccessibilityEvent mSendViewStateChangedAccessibilityEvent;
    private boolean mSendingHoverAccessibilityEvents;
    String mStartActivityRequestWho;
    private StateListAnimator mStateListAnimator;
    @ExportedProperty(flagMapping = {@FlagToString(equals = 1, mask = 1, name = "SYSTEM_UI_FLAG_LOW_PROFILE", outputIf = true), @FlagToString(equals = 2, mask = 2, name = "SYSTEM_UI_FLAG_HIDE_NAVIGATION", outputIf = true), @FlagToString(equals = 0, mask = 16375, name = "SYSTEM_UI_FLAG_VISIBLE", outputIf = true)}, formatToHexString = true)
    int mSystemUiVisibility;
    protected Object mTag;
    private int[] mTempNestedScrollConsumed;
    @ExportedProperty(category = "layout")
    protected int mTop;
    private TouchDelegate mTouchDelegate;
    private int mTouchSlop;
    TransformationInfo mTransformationInfo;
    int mTransientStateCount;
    private String mTransitionName;
    private Bitmap mUnscaledDrawingCache;
    private UnsetPressedState mUnsetPressedState;
    @ExportedProperty(category = "padding")
    protected int mUserPaddingBottom;
    @ExportedProperty(category = "padding")
    int mUserPaddingEnd;
    @ExportedProperty(category = "padding")
    protected int mUserPaddingLeft;
    int mUserPaddingLeftInitial;
    @ExportedProperty(category = "padding")
    protected int mUserPaddingRight;
    int mUserPaddingRightInitial;
    @ExportedProperty(category = "padding")
    int mUserPaddingStart;
    private float mVerticalScrollFactor;
    private int mVerticalScrollbarPosition;
    private int mViewCheckedResult;
    @ExportedProperty(formatToHexString = true)
    int mViewFlags;
    int mWindowAttachCount;

    public interface OnClickListener {
        void onClick(View view);
    }

    public interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View view);

        void onViewDetachedFromWindow(View view);
    }

    public static class AccessibilityDelegate {
        public void sendAccessibilityEvent(View host, int eventType) {
            host.sendAccessibilityEventInternal(eventType);
        }

        public boolean performAccessibilityAction(View host, int action, Bundle args) {
            return host.performAccessibilityActionInternal(action, args);
        }

        public void sendAccessibilityEventUnchecked(View host, AccessibilityEvent event) {
            host.sendAccessibilityEventUncheckedInternal(event);
        }

        public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            return host.dispatchPopulateAccessibilityEventInternal(event);
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            host.onPopulateAccessibilityEventInternal(event);
        }

        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            host.onInitializeAccessibilityEventInternal(event);
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            host.onInitializeAccessibilityNodeInfoInternal(info);
        }

        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
            return host.onRequestSendAccessibilityEventInternal(child, event);
        }

        public AccessibilityNodeProvider getAccessibilityNodeProvider(View host) {
            return null;
        }

        public AccessibilityNodeInfo createAccessibilityNodeInfo(View host) {
            return host.createAccessibilityNodeInfoInternal();
        }
    }

    public interface OnTouchListener {
        boolean onTouch(View view, MotionEvent motionEvent);
    }

    /* renamed from: android.view.View.10 */
    static class AnonymousClass10 extends FloatProperty<View> {
        AnonymousClass10(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setRotationY(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getRotationY());
        }
    }

    /* renamed from: android.view.View.11 */
    static class AnonymousClass11 extends FloatProperty<View> {
        AnonymousClass11(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setScaleX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getScaleX());
        }
    }

    /* renamed from: android.view.View.12 */
    static class AnonymousClass12 extends FloatProperty<View> {
        AnonymousClass12(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setScaleY(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getScaleY());
        }
    }

    /* renamed from: android.view.View.13 */
    class AnonymousClass13 implements Predicate<View> {
        final /* synthetic */ int val$id;

        AnonymousClass13(int val$id) {
            this.val$id = val$id;
        }

        public boolean apply(View t) {
            return t.mNextFocusForwardId == this.val$id ? true : View.DBG;
        }
    }

    /* renamed from: android.view.View.1 */
    static class AnonymousClass1 extends FloatProperty<View> {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setAlpha(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getAlpha());
        }
    }

    /* renamed from: android.view.View.2 */
    static class AnonymousClass2 extends FloatProperty<View> {
        AnonymousClass2(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setTranslationX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getTranslationX());
        }
    }

    /* renamed from: android.view.View.3 */
    static class AnonymousClass3 extends FloatProperty<View> {
        AnonymousClass3(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setTranslationY(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getTranslationY());
        }
    }

    /* renamed from: android.view.View.4 */
    static class AnonymousClass4 extends FloatProperty<View> {
        AnonymousClass4(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setTranslationZ(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getTranslationZ());
        }
    }

    /* renamed from: android.view.View.5 */
    static class AnonymousClass5 extends FloatProperty<View> {
        AnonymousClass5(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getX());
        }
    }

    /* renamed from: android.view.View.6 */
    static class AnonymousClass6 extends FloatProperty<View> {
        AnonymousClass6(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setY(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getY());
        }
    }

    /* renamed from: android.view.View.7 */
    static class AnonymousClass7 extends FloatProperty<View> {
        AnonymousClass7(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setZ(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getZ());
        }
    }

    /* renamed from: android.view.View.8 */
    static class AnonymousClass8 extends FloatProperty<View> {
        AnonymousClass8(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setRotation(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getRotation());
        }
    }

    /* renamed from: android.view.View.9 */
    static class AnonymousClass9 extends FloatProperty<View> {
        AnonymousClass9(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, float value) {
            object.setRotationX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getRotationX());
        }
    }

    static final class AttachInfo {
        int mAccessibilityFetchFlags;
        Drawable mAccessibilityFocusDrawable;
        int mAccessibilityWindowId;
        boolean mAlwaysConsumeNavBar;
        float mApplicationScale;
        Canvas mCanvas;
        final Rect mContentInsets;
        boolean mDebugLayout;
        int mDisabledSystemUiVisibility;
        final Display mDisplay;
        int mDisplayState;
        public Surface mDragSurface;
        IBinder mDragToken;
        long mDrawingTime;
        List<View> mEmptyPartialLayoutViews;
        boolean mForceReportNewAttributes;
        final InternalInsetsInfo mGivenInternalInsets;
        int mGlobalSystemUiVisibility;
        final Handler mHandler;
        boolean mHandlingPointerEvent;
        boolean mHardwareAccelerated;
        boolean mHardwareAccelerationRequested;
        ThreadedRenderer mHardwareRenderer;
        boolean mHasNonEmptyGivenInternalInsets;
        boolean mHasSystemUiListeners;
        boolean mHasWindowFocus;
        boolean mHighContrastText;
        IWindowId mIWindowId;
        boolean mIgnoreDirtyState;
        boolean mInTouchMode;
        final int[] mInvalidateChildLocation;
        boolean mKeepScreenOn;
        final DispatcherState mKeyDispatchState;
        boolean mNeedsUpdateLightCenter;
        final Rect mOutsets;
        final Rect mOverscanInsets;
        boolean mOverscanRequested;
        IBinder mPanelParentWindowToken;
        List<View> mPartialLayoutViews;
        List<RenderNode> mPendingAnimatingRenderNodes;
        final Point mPoint;
        boolean mRecomputeGlobalAttributes;
        final Callbacks mRootCallbacks;
        View mRootView;
        boolean mScalingRequired;
        final ArrayList<View> mScrollContainers;
        final IWindowSession mSession;
        boolean mSetIgnoreDirtyState;
        final Rect mStableInsets;
        int mSystemUiVisibility;
        final ArrayList<View> mTempArrayList;
        final Rect mTmpInvalRect;
        final int[] mTmpLocation;
        final Matrix mTmpMatrix;
        final Outline mTmpOutline;
        final List<RectF> mTmpRectList;
        final float[] mTmpTransformLocation;
        final RectF mTmpTransformRect;
        final RectF mTmpTransformRect1;
        final Transformation mTmpTransformation;
        final int[] mTransparentLocation;
        final ViewTreeObserver mTreeObserver;
        boolean mUnbufferedDispatchRequested;
        boolean mUse32BitDrawingCache;
        View mViewRequestingLayout;
        final ViewRootImpl mViewRootImpl;
        boolean mViewScrollChanged;
        boolean mViewVisibilityChanged;
        final Rect mVisibleInsets;
        final IWindow mWindow;
        WindowId mWindowId;
        int mWindowLeft;
        final IBinder mWindowToken;
        int mWindowTop;
        int mWindowVisibility;

        interface Callbacks {
            boolean performHapticFeedback(int i, boolean z);

            void playSoundEffect(int i);
        }

        static class InvalidateInfo {
            private static final int POOL_LIMIT = 10;
            private static final SynchronizedPool<InvalidateInfo> sPool = null;
            int bottom;
            int left;
            int right;
            View target;
            int top;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.View.AttachInfo.InvalidateInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.View.AttachInfo.InvalidateInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 9 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 10 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.view.View.AttachInfo.InvalidateInfo.<clinit>():void");
            }

            InvalidateInfo() {
            }

            public static InvalidateInfo obtain() {
                InvalidateInfo instance = (InvalidateInfo) sPool.acquire();
                return instance != null ? instance : new InvalidateInfo();
            }

            public void recycle() {
                this.target = null;
                sPool.release(this);
            }
        }

        AttachInfo(IWindowSession session, IWindow window, Display display, ViewRootImpl viewRootImpl, Handler handler, Callbacks effectPlayer) {
            this.mDisplayState = View.VISIBLE;
            this.mOverscanInsets = new Rect();
            this.mContentInsets = new Rect();
            this.mVisibleInsets = new Rect();
            this.mStableInsets = new Rect();
            this.mOutsets = new Rect();
            this.mGivenInternalInsets = new InternalInsetsInfo();
            this.mScrollContainers = new ArrayList();
            this.mKeyDispatchState = new DispatcherState();
            this.mSetIgnoreDirtyState = View.DBG;
            this.mTransparentLocation = new int[View.TEXT_DIRECTION_ANY_RTL];
            this.mInvalidateChildLocation = new int[View.TEXT_DIRECTION_ANY_RTL];
            this.mTmpLocation = new int[View.TEXT_DIRECTION_ANY_RTL];
            this.mTmpTransformLocation = new float[View.TEXT_DIRECTION_ANY_RTL];
            this.mTreeObserver = new ViewTreeObserver();
            this.mTmpInvalRect = new Rect();
            this.mTmpTransformRect = new RectF();
            this.mTmpTransformRect1 = new RectF();
            this.mTmpRectList = new ArrayList();
            this.mTmpMatrix = new Matrix();
            this.mTmpTransformation = new Transformation();
            this.mTmpOutline = new Outline();
            this.mTempArrayList = new ArrayList(24);
            this.mAccessibilityWindowId = HwBootFail.STAGE_BOOT_SUCCESS;
            this.mDebugLayout = SystemProperties.getBoolean(View.DEBUG_LAYOUT_PROPERTY, View.DBG);
            this.mPoint = new Point();
            this.mPartialLayoutViews = new ArrayList();
            this.mSession = session;
            this.mWindow = window;
            this.mWindowToken = window.asBinder();
            this.mDisplay = display;
            this.mViewRootImpl = viewRootImpl;
            this.mHandler = handler;
            this.mRootCallbacks = effectPlayer;
        }
    }

    public static class BaseSavedState extends AbsSavedState {
        public static final Creator<BaseSavedState> CREATOR = null;
        String mStartActivityRequestWhoSaved;

        /* renamed from: android.view.View.BaseSavedState.1 */
        static class AnonymousClass1 implements ClassLoaderCreator<BaseSavedState> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m3createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }

            public /* bridge */ /* synthetic */ Object m4createFromParcel(Parcel in, ClassLoader loader) {
                return createFromParcel(in, loader);
            }

            public BaseSavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new BaseSavedState(in, loader);
            }

            public /* bridge */ /* synthetic */ Object[] m5newArray(int size) {
                return newArray(size);
            }

            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.View.BaseSavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.View.BaseSavedState.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.view.View.BaseSavedState.<clinit>():void");
        }

        public BaseSavedState(Parcel source) {
            this(source, null);
        }

        public BaseSavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            this.mStartActivityRequestWhoSaved = source.readString();
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(this.mStartActivityRequestWhoSaved);
        }
    }

    private final class CheckForLongPress implements Runnable {
        private int mOriginalWindowAttachCount;
        private float mX;
        private float mY;
        final /* synthetic */ View this$0;

        /* synthetic */ CheckForLongPress(View this$0, CheckForLongPress checkForLongPress) {
            this(this$0);
        }

        private CheckForLongPress(View this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            if (this.this$0.isPressed() && this.this$0.mParent != null && this.mOriginalWindowAttachCount == this.this$0.mWindowAttachCount && this.this$0.performLongClick(this.mX, this.mY)) {
                this.this$0.mHasPerformedLongPress = true;
            }
        }

        public void setAnchor(float x, float y) {
            this.mX = x;
            this.mY = y;
        }

        public void rememberWindowAttachCount() {
            this.mOriginalWindowAttachCount = this.this$0.mWindowAttachCount;
        }
    }

    private final class CheckForTap implements Runnable {
        final /* synthetic */ View this$0;
        public float x;
        public float y;

        /* synthetic */ CheckForTap(View this$0, CheckForTap checkForTap) {
            this(this$0);
        }

        private CheckForTap(View this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            View view = this.this$0;
            view.mPrivateFlags &= -33554433;
            this.this$0.setPressed(true, this.x, this.y);
            this.this$0.checkForLongClick(ViewConfiguration.getTapTimeout(), this.x, this.y);
        }
    }

    private static class DeclaredOnClickListener implements OnClickListener {
        private final View mHostView;
        private final String mMethodName;
        private Context mResolvedContext;
        private Method mResolvedMethod;

        public DeclaredOnClickListener(View hostView, String methodName) {
            this.mHostView = hostView;
            this.mMethodName = methodName;
        }

        public void onClick(View v) {
            if (this.mResolvedMethod == null) {
                resolveMethod(this.mHostView.getContext(), this.mMethodName);
            }
            try {
                Method method = this.mResolvedMethod;
                Context context = this.mResolvedContext;
                Object[] objArr = new Object[View.VIEW_IS_NOT_ADVIEW_OR_DISABLE];
                objArr[View.VISIBLE] = v;
                method.invoke(context, objArr);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not execute non-public method for android:onClick", e);
            } catch (InvocationTargetException e2) {
                throw new IllegalStateException("Could not execute method for android:onClick", e2);
            }
        }

        private void resolveMethod(Context context, String name) {
            while (context != null) {
                try {
                    if (!context.isRestricted()) {
                        Class cls = context.getClass();
                        String str = this.mMethodName;
                        Class[] clsArr = new Class[View.VIEW_IS_NOT_ADVIEW_OR_DISABLE];
                        clsArr[View.VISIBLE] = View.class;
                        Method method = cls.getMethod(str, clsArr);
                        if (method != null) {
                            this.mResolvedMethod = method;
                            this.mResolvedContext = context;
                            return;
                        }
                    }
                } catch (NoSuchMethodException e) {
                }
                if (context instanceof ContextWrapper) {
                    context = ((ContextWrapper) context).getBaseContext();
                } else {
                    context = null;
                }
            }
            int id = this.mHostView.getId();
            throw new IllegalStateException("Could not find method " + this.mMethodName + "(View) in a parent or ancestor Context for android:onClick " + "attribute defined on view " + this.mHostView.getClass() + (id == View.VIEW_IS_NOT_CHECKED ? "" : " with id '" + this.mHostView.getContext().getResources().getResourceEntryName(id) + "'"));
        }
    }

    public static class DragShadowBuilder {
        private final WeakReference<View> mView;

        public DragShadowBuilder(View view) {
            this.mView = new WeakReference(view);
        }

        public DragShadowBuilder() {
            this.mView = new WeakReference(null);
        }

        public final View getView() {
            return (View) this.mView.get();
        }

        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
            View view = (View) this.mView.get();
            if (view != null) {
                outShadowSize.set(view.getWidth(), view.getHeight());
                outShadowTouchPoint.set(outShadowSize.x / View.TEXT_DIRECTION_ANY_RTL, outShadowSize.y / View.TEXT_DIRECTION_ANY_RTL);
                return;
            }
            Log.e(View.VIEW_LOG_TAG, "Asked for drag thumb metrics but no view");
        }

        public void onDrawShadow(Canvas canvas) {
            View view = (View) this.mView.get();
            if (view != null) {
                view.draw(canvas);
            } else {
                Log.e(View.VIEW_LOG_TAG, "Asked to draw drag shadow but no view");
            }
        }
    }

    private static class ForegroundInfo {
        private boolean mBoundsChanged;
        private Drawable mDrawable;
        private int mGravity;
        private boolean mInsidePadding;
        private final Rect mOverlayBounds;
        private final Rect mSelfBounds;
        private TintInfo mTintInfo;

        /* synthetic */ ForegroundInfo(ForegroundInfo foregroundInfo) {
            this();
        }

        private ForegroundInfo() {
            this.mGravity = LogPower.HW_PUSH_FINISH;
            this.mInsidePadding = true;
            this.mBoundsChanged = true;
            this.mSelfBounds = new Rect();
            this.mOverlayBounds = new Rect();
        }
    }

    static class ListenerInfo {
        OnApplyWindowInsetsListener mOnApplyWindowInsetsListener;
        private CopyOnWriteArrayList<OnAttachStateChangeListener> mOnAttachStateChangeListeners;
        public OnClickListener mOnClickListener;
        protected OnContextClickListener mOnContextClickListener;
        protected OnCreateContextMenuListener mOnCreateContextMenuListener;
        private OnDragListener mOnDragListener;
        protected OnFocusChangeListener mOnFocusChangeListener;
        private OnGenericMotionListener mOnGenericMotionListener;
        private OnHoverListener mOnHoverListener;
        private OnKeyListener mOnKeyListener;
        private ArrayList<OnLayoutChangeListener> mOnLayoutChangeListeners;
        protected OnLongClickListener mOnLongClickListener;
        protected OnScrollChangeListener mOnScrollChangeListener;
        private OnSystemUiVisibilityChangeListener mOnSystemUiVisibilityChangeListener;
        private OnTouchListener mOnTouchListener;

        ListenerInfo() {
        }
    }

    private class MatchIdPredicate implements Predicate<View> {
        public int mId;
        final /* synthetic */ View this$0;

        /* synthetic */ MatchIdPredicate(View this$0, MatchIdPredicate matchIdPredicate) {
            this(this$0);
        }

        private MatchIdPredicate(View this$0) {
            this.this$0 = this$0;
        }

        public /* bridge */ /* synthetic */ boolean apply(Object view) {
            return apply((View) view);
        }

        public boolean apply(View view) {
            return view.mID == this.mId ? true : View.DBG;
        }
    }

    private class MatchLabelForPredicate implements Predicate<View> {
        private int mLabeledId;
        final /* synthetic */ View this$0;

        /* synthetic */ MatchLabelForPredicate(View this$0, MatchLabelForPredicate matchLabelForPredicate) {
            this(this$0);
        }

        private MatchLabelForPredicate(View this$0) {
            this.this$0 = this$0;
        }

        public /* bridge */ /* synthetic */ boolean apply(Object view) {
            return apply((View) view);
        }

        public boolean apply(View view) {
            return view.mLabelForId == this.mLabeledId ? true : View.DBG;
        }
    }

    public static class MeasureSpec {
        public static final int AT_MOST = Integer.MIN_VALUE;
        public static final int EXACTLY = 1073741824;
        private static final int MODE_MASK = -1073741824;
        private static final int MODE_SHIFT = 30;
        public static final int UNSPECIFIED = 0;

        public MeasureSpec() {
        }

        public static int makeMeasureSpec(int size, int mode) {
            if (View.sUseBrokenMakeMeasureSpec) {
                return size + mode;
            }
            return (1073741823 & size) | (MODE_MASK & mode);
        }

        public static int makeSafeMeasureSpec(int size, int mode) {
            if (View.sUseZeroUnspecifiedMeasureSpec && mode == 0) {
                return View.VISIBLE;
            }
            return makeMeasureSpec(size, mode);
        }

        public static int getMode(int measureSpec) {
            return MODE_MASK & measureSpec;
        }

        public static int getSize(int measureSpec) {
            return 1073741823 & measureSpec;
        }

        static int adjust(int measureSpec, int delta) {
            int mode = getMode(measureSpec);
            int size = getSize(measureSpec);
            if (mode == 0) {
                return makeMeasureSpec(size, View.VISIBLE);
            }
            size += delta;
            if (size < 0) {
                Log.e(View.VIEW_LOG_TAG, "MeasureSpec.adjust: new size would be negative! (" + size + ") spec: " + toString(measureSpec) + " delta: " + delta);
                size = View.VISIBLE;
            }
            return makeMeasureSpec(size, mode);
        }

        public static String toString(int measureSpec) {
            int mode = getMode(measureSpec);
            int size = getSize(measureSpec);
            StringBuilder sb = new StringBuilder("MeasureSpec: ");
            if (mode == 0) {
                sb.append("UNSPECIFIED ");
            } else if (mode == EXACTLY) {
                sb.append("EXACTLY ");
            } else if (mode == AT_MOST) {
                sb.append("AT_MOST ");
            } else {
                sb.append(mode).append(" ");
            }
            sb.append(size);
            return sb.toString();
        }
    }

    public interface OnApplyWindowInsetsListener {
        WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets);
    }

    public interface OnContextClickListener {
        boolean onContextClick(View view);
    }

    public interface OnCreateContextMenuListener {
        void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenuInfo contextMenuInfo);
    }

    public interface OnDragListener {
        boolean onDrag(View view, DragEvent dragEvent);
    }

    public interface OnFocusChangeListener {
        void onFocusChange(View view, boolean z);
    }

    public interface OnGenericMotionListener {
        boolean onGenericMotion(View view, MotionEvent motionEvent);
    }

    public interface OnHoverListener {
        boolean onHover(View view, MotionEvent motionEvent);
    }

    public interface OnKeyListener {
        boolean onKey(View view, int i, KeyEvent keyEvent);
    }

    public interface OnLayoutChangeListener {
        void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8);
    }

    public interface OnLongClickListener {
        boolean onLongClick(View view);
    }

    public interface OnScrollChangeListener {
        void onScrollChange(View view, int i, int i2, int i3, int i4);
    }

    public interface OnSystemUiVisibilityChangeListener {
        void onSystemUiVisibilityChange(int i);
    }

    private final class PerformClick implements Runnable {
        final /* synthetic */ View this$0;

        /* synthetic */ PerformClick(View this$0, PerformClick performClick) {
            this(this$0);
        }

        private PerformClick(View this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            this.this$0.performClick();
        }
    }

    private static class ScrollabilityCache implements Runnable {
        public static final int DRAGGING_HORIZONTAL_SCROLL_BAR = 2;
        public static final int DRAGGING_VERTICAL_SCROLL_BAR = 1;
        public static final int FADING = 2;
        public static final int NOT_DRAGGING = 0;
        public static final int OFF = 0;
        public static final int ON = 1;
        private static final float[] OPAQUE = null;
        private static final float[] TRANSPARENT = null;
        public boolean fadeScrollBars;
        public long fadeStartTime;
        public int fadingEdgeLength;
        public View host;
        public float[] interpolatorValues;
        private int mLastColor;
        public final Rect mScrollBarBounds;
        public float mScrollBarDraggingPos;
        public int mScrollBarDraggingState;
        public final Matrix matrix;
        public final Paint paint;
        public ScrollBarDrawable scrollBar;
        public int scrollBarDefaultDelayBeforeFade;
        public int scrollBarFadeDuration;
        public final Interpolator scrollBarInterpolator;
        public int scrollBarSize;
        public Shader shader;
        public int state;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.View.ScrollabilityCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.View.ScrollabilityCache.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.view.View.ScrollabilityCache.<clinit>():void");
        }

        public ScrollabilityCache(ViewConfiguration configuration, View host) {
            this.scrollBarInterpolator = new Interpolator(ON, FADING);
            this.state = OFF;
            this.mScrollBarBounds = new Rect();
            this.mScrollBarDraggingState = OFF;
            this.mScrollBarDraggingPos = 0.0f;
            this.fadingEdgeLength = configuration.getScaledFadingEdgeLength();
            this.scrollBarSize = configuration.getScaledScrollBarSize();
            this.scrollBarDefaultDelayBeforeFade = ViewConfiguration.getScrollDefaultDelay();
            this.scrollBarFadeDuration = ViewConfiguration.getScrollBarFadeDuration();
            this.paint = new Paint();
            this.matrix = new Matrix();
            this.shader = new LinearGradient(0.0f, 0.0f, 0.0f, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, View.MEASURED_STATE_MASK, OFF, TileMode.CLAMP);
            this.paint.setShader(this.shader);
            this.paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
            this.host = host;
        }

        public void setFadeColor(int color) {
            if (color != this.mLastColor) {
                this.mLastColor = color;
                if (color != 0) {
                    this.shader = new LinearGradient(0.0f, 0.0f, 0.0f, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, View.MEASURED_STATE_MASK | color, color & View.MEASURED_SIZE_MASK, TileMode.CLAMP);
                    this.paint.setShader(this.shader);
                    this.paint.setXfermode(null);
                    return;
                }
                this.shader = new LinearGradient(0.0f, 0.0f, 0.0f, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, View.MEASURED_STATE_MASK, OFF, TileMode.CLAMP);
                this.paint.setShader(this.shader);
                this.paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
            }
        }

        public void run() {
            long now = AnimationUtils.currentAnimationTimeMillis();
            if (now >= this.fadeStartTime) {
                int nextFrame = (int) now;
                Interpolator interpolator = this.scrollBarInterpolator;
                interpolator.setKeyFrame(OFF, nextFrame, OPAQUE);
                interpolator.setKeyFrame(ON, nextFrame + this.scrollBarFadeDuration, TRANSPARENT);
                this.state = FADING;
                this.host.invalidate(true);
            }
        }
    }

    private class SendViewScrolledAccessibilityEvent implements Runnable {
        public volatile boolean mIsPending;
        final /* synthetic */ View this$0;

        /* synthetic */ SendViewScrolledAccessibilityEvent(View this$0, SendViewScrolledAccessibilityEvent sendViewScrolledAccessibilityEvent) {
            this(this$0);
        }

        private SendViewScrolledAccessibilityEvent(View this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            this.this$0.sendAccessibilityEvent(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            this.mIsPending = View.DBG;
        }
    }

    private class SendViewStateChangedAccessibilityEvent implements Runnable {
        private int mChangeTypes;
        private long mLastEventTimeMillis;
        private boolean mPosted;
        private boolean mPostedWithDelay;
        final /* synthetic */ View this$0;

        /* synthetic */ SendViewStateChangedAccessibilityEvent(View this$0, SendViewStateChangedAccessibilityEvent sendViewStateChangedAccessibilityEvent) {
            this(this$0);
        }

        private SendViewStateChangedAccessibilityEvent(View this$0) {
            this.this$0 = this$0;
            this.mChangeTypes = View.VISIBLE;
        }

        public void run() {
            this.mPosted = View.DBG;
            this.mPostedWithDelay = View.DBG;
            this.mLastEventTimeMillis = SystemClock.uptimeMillis();
            if (AccessibilityManager.getInstance(this.this$0.mContext).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain();
                event.setEventType(View.SYSTEM_UI_FLAG_IMMERSIVE);
                event.setContentChangeTypes(this.mChangeTypes);
                this.this$0.sendAccessibilityEventUnchecked(event);
            }
            this.mChangeTypes = View.VISIBLE;
        }

        public void runOrPost(int changeType) {
            this.mChangeTypes |= changeType;
            if (this.this$0.inLiveRegion()) {
                if (this.mPostedWithDelay) {
                    this.this$0.removeCallbacks(this);
                    this.mPostedWithDelay = View.DBG;
                }
                if (!this.mPosted) {
                    this.this$0.post(this);
                    this.mPosted = true;
                }
            } else if (!this.mPosted) {
                long timeSinceLastMillis = SystemClock.uptimeMillis() - this.mLastEventTimeMillis;
                long minEventIntevalMillis = ViewConfiguration.getSendRecurringAccessibilityEventsInterval();
                if (timeSinceLastMillis >= minEventIntevalMillis) {
                    this.this$0.removeCallbacks(this);
                    run();
                } else {
                    this.this$0.postDelayed(this, minEventIntevalMillis - timeSinceLastMillis);
                    this.mPostedWithDelay = true;
                }
            }
        }
    }

    static class TintInfo {
        boolean mHasTintList;
        boolean mHasTintMode;
        ColorStateList mTintList;
        Mode mTintMode;

        TintInfo() {
        }
    }

    static class TransformationInfo {
        @ExportedProperty
        float mAlpha;
        private Matrix mInverseMatrix;
        private final Matrix mMatrix;
        float mTransitionAlpha;

        TransformationInfo() {
            this.mMatrix = new Matrix();
            this.mAlpha = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            this.mTransitionAlpha = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        }
    }

    private final class UnsetPressedState implements Runnable {
        final /* synthetic */ View this$0;

        /* synthetic */ UnsetPressedState(View this$0, UnsetPressedState unsetPressedState) {
            this(this$0);
        }

        private UnsetPressedState(View this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            this.this$0.setPressed(View.DBG);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.View.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.View.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.View.<clinit>():void");
    }

    void setFlags(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.View.setFlags(int, int):void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.View.setFlags(int, int):void");
    }

    public void setScrollIndicators(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.View.setScrollIndicators(int, int):void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.View.setScrollIndicators(int, int):void");
    }

    boolean updateLocalSystemUiVisibility(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.View.updateLocalSystemUiVisibility(int, int):boolean
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.View.updateLocalSystemUiVisibility(int, int):boolean");
    }

    public void forceRTL(boolean flag) {
        this.mForceRTL = flag;
    }

    public View(Context context) {
        Resources resources = null;
        boolean z = DBG;
        this.mCurrentAnimation = null;
        this.mRecreateDisplayList = DBG;
        this.mID = VIEW_IS_NOT_CHECKED;
        this.mAccessibilityViewId = VIEW_IS_NOT_CHECKED;
        this.mAccessibilityCursorPosition = VIEW_IS_NOT_CHECKED;
        this.mTag = null;
        this.mForceRTL = DBG;
        this.mTransientStateCount = VISIBLE;
        this.mClipBounds = null;
        this.mPaddingLeft = VISIBLE;
        this.mPaddingRight = VISIBLE;
        this.mLabelForId = VIEW_IS_NOT_CHECKED;
        this.mAccessibilityTraversalBeforeId = VIEW_IS_NOT_CHECKED;
        this.mAccessibilityTraversalAfterId = VIEW_IS_NOT_CHECKED;
        this.mLeftPaddingDefined = DBG;
        this.mRightPaddingDefined = DBG;
        this.mOldWidthMeasureSpec = UNDEFINED_PADDING;
        this.mOldHeightMeasureSpec = UNDEFINED_PADDING;
        this.mLongClickX = Float.NaN;
        this.mLongClickY = Float.NaN;
        this.mDrawableState = null;
        this.mOutlineProvider = ViewOutlineProvider.BACKGROUND;
        this.mNextFocusLeftId = VIEW_IS_NOT_CHECKED;
        this.mNextFocusRightId = VIEW_IS_NOT_CHECKED;
        this.mNextFocusUpId = VIEW_IS_NOT_CHECKED;
        this.mNextFocusDownId = VIEW_IS_NOT_CHECKED;
        this.mNextFocusForwardId = VIEW_IS_NOT_CHECKED;
        this.mPendingCheckForTap = null;
        this.mTouchDelegate = null;
        this.mDrawingCacheBackgroundColor = VISIBLE;
        this.mAnimator = null;
        this.mLayerType = VISIBLE;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, VISIBLE) : null;
        this.mViewCheckedResult = VIEW_IS_NOT_CHECKED;
        this.mContext = context;
        if (context != null) {
            resources = context.getResources();
        }
        this.mResources = resources;
        this.mViewFlags = 402653184;
        this.mPrivateFlags2 = 140296;
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setOverScrollMode(VIEW_IS_NOT_ADVIEW_OR_DISABLE);
        this.mUserPaddingStart = UNDEFINED_PADDING;
        this.mUserPaddingEnd = UNDEFINED_PADDING;
        this.mRenderNode = RenderNode.create(getClass().getName(), this);
        if (!sCompatibilityDone && context != null) {
            boolean z2;
            int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
            if (targetSdkVersion <= PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK_SHIFT) {
                z2 = true;
            } else {
                z2 = DBG;
            }
            sUseBrokenMakeMeasureSpec = z2;
            if (targetSdkVersion < 19) {
                z2 = true;
            } else {
                z2 = DBG;
            }
            sIgnoreMeasureCache = z2;
            if (targetSdkVersion < PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT) {
                z2 = true;
            } else {
                z2 = DBG;
            }
            Canvas.sCompatibilityRestore = z2;
            if (targetSdkVersion < PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT) {
                z2 = true;
            } else {
                z2 = DBG;
            }
            sUseZeroUnspecifiedMeasureSpec = z2;
            if (targetSdkVersion <= PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT) {
                z2 = true;
            } else {
                z2 = DBG;
            }
            sAlwaysRemeasureExactly = z2;
            if (targetSdkVersion <= PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT) {
                z2 = true;
            } else {
                z2 = DBG;
            }
            sLayoutParamsAlwaysChanged = z2;
            if (targetSdkVersion <= PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT) {
                z = true;
            }
            sTextureViewIgnoresDrawableSetters = z;
            sCompatibilityDone = true;
        }
    }

    public View(Context context, AttributeSet attrs) {
        this(context, attrs, VISIBLE);
    }

    public View(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, VISIBLE);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public View(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        int i;
        int i2;
        this(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.View, defStyleAttr, defStyleRes);
        if (mDebugViewAttributes) {
            saveAttributeData(attrs, a);
        }
        Drawable background = null;
        int leftPadding = VIEW_IS_NOT_CHECKED;
        int topPadding = VIEW_IS_NOT_CHECKED;
        int rightPadding = VIEW_IS_NOT_CHECKED;
        int bottomPadding = VIEW_IS_NOT_CHECKED;
        int startPadding = UNDEFINED_PADDING;
        int endPadding = UNDEFINED_PADDING;
        int padding = VIEW_IS_NOT_CHECKED;
        int viewFlagValues = VISIBLE;
        int viewFlagMasks = VISIBLE;
        boolean setScrollContainer = DBG;
        int x = VISIBLE;
        int y = VISIBLE;
        float tx = 0.0f;
        float ty = 0.0f;
        float tz = 0.0f;
        float elevation = 0.0f;
        float rotation = 0.0f;
        float rotationX = 0.0f;
        float rotationY = 0.0f;
        float sx = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        float sy = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        boolean transformSet = DBG;
        int scrollbarStyle = VISIBLE;
        int overScrollMode = this.mOverScrollMode;
        boolean initializeScrollbars = DBG;
        boolean initializeScrollIndicators = DBG;
        boolean startPaddingDefined = DBG;
        boolean endPaddingDefined = DBG;
        boolean leftPaddingDefined = DBG;
        boolean rightPaddingDefined = DBG;
        int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        int N = a.getIndexCount();
        for (i = VISIBLE; i < N; i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
            int attr = a.getIndex(i);
            switch (attr) {
                case STATUS_BAR_TRANSPARENT /*8*/:
                    scrollbarStyle = a.getInt(attr, VISIBLE);
                    if (scrollbarStyle == 0) {
                        break;
                    }
                    viewFlagValues |= SCROLLBARS_STYLE_MASK & scrollbarStyle;
                    viewFlagMasks |= SCROLLBARS_STYLE_MASK;
                    break;
                case PGSdk.TYPE_SCRLOCK /*9*/:
                    this.mID = a.getResourceId(attr, VIEW_IS_NOT_CHECKED);
                    break;
                case PFLAG2_TEXT_DIRECTION_RESOLVED_MASK_SHIFT /*10*/:
                    this.mTag = a.getText(attr);
                    break;
                case PGSdk.TYPE_IM /*11*/:
                    x = a.getDimensionPixelOffset(attr, VISIBLE);
                    break;
                case VISIBILITY_MASK /*12*/:
                    y = a.getDimensionPixelOffset(attr, VISIBLE);
                    break;
                case PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT /*13*/:
                    background = a.getDrawable(attr);
                    break;
                case StatisticalConstant.TYPE_FINGER_BIAS_SPLIT_RIGHT /*14*/:
                    padding = a.getDimensionPixelSize(attr, VIEW_IS_NOT_CHECKED);
                    this.mUserPaddingLeftInitial = padding;
                    this.mUserPaddingRightInitial = padding;
                    leftPaddingDefined = true;
                    rightPaddingDefined = true;
                    break;
                case PFLAG3_POINTER_ICON_LSHIFT /*15*/:
                    leftPadding = a.getDimensionPixelSize(attr, VIEW_IS_NOT_CHECKED);
                    this.mUserPaddingLeftInitial = leftPadding;
                    leftPaddingDefined = true;
                    break;
                case SCROLL_INDICATOR_START /*16*/:
                    topPadding = a.getDimensionPixelSize(attr, VIEW_IS_NOT_CHECKED);
                    break;
                case PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK_SHIFT /*17*/:
                    rightPadding = a.getDimensionPixelSize(attr, VIEW_IS_NOT_CHECKED);
                    this.mUserPaddingRightInitial = rightPadding;
                    rightPaddingDefined = true;
                    break;
                case PerfHub.PERF_TAG_IPA_CONTROL_TEMP /*18*/:
                    bottomPadding = a.getDimensionPixelSize(attr, VIEW_IS_NOT_CHECKED);
                    break;
                case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
                    if (!a.getBoolean(attr, DBG)) {
                        break;
                    }
                    viewFlagValues |= VIEW_IS_NOT_ADVIEW_OR_DISABLE;
                    viewFlagMasks |= VIEW_IS_NOT_ADVIEW_OR_DISABLE;
                    break;
                case PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_SHIFT /*20*/:
                    if (!a.getBoolean(attr, DBG)) {
                        break;
                    }
                    viewFlagValues |= 262145;
                    viewFlagMasks |= 262145;
                    break;
                case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
                    int visibility = a.getInt(attr, VISIBLE);
                    if (visibility == 0) {
                        break;
                    }
                    viewFlagValues |= VISIBILITY_FLAGS[visibility];
                    viewFlagMasks |= VISIBILITY_MASK;
                    break;
                case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
                    if (!a.getBoolean(attr, DBG)) {
                        break;
                    }
                    viewFlagValues |= TEXT_DIRECTION_ANY_RTL;
                    viewFlagMasks |= TEXT_DIRECTION_ANY_RTL;
                    break;
                case PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT /*23*/:
                    int scrollbars = a.getInt(attr, VISIBLE);
                    if (scrollbars == 0) {
                        break;
                    }
                    viewFlagValues |= scrollbars;
                    viewFlagMasks |= SCROLLBARS_MASK;
                    initializeScrollbars = true;
                    break;
                case HwPerformance.PERF_TAG_DEF_B_CPU_MAX /*24*/:
                    if (targetSdkVersion >= 14) {
                        break;
                    }
                case PerfHub.PERF_TAG_DEF_GPU_MAX /*26*/:
                    this.mNextFocusLeftId = a.getResourceId(attr, VIEW_IS_NOT_CHECKED);
                    break;
                case PerfHub.PERF_TAG_DEF_DDR_MIN /*27*/:
                    this.mNextFocusRightId = a.getResourceId(attr, VIEW_IS_NOT_CHECKED);
                    break;
                case PerfHub.PERF_TAG_DEF_DDR_MAX /*28*/:
                    this.mNextFocusUpId = a.getResourceId(attr, VIEW_IS_NOT_CHECKED);
                    break;
                case PerfHub.PERF_TAG_DEF_HMP_UP_THRES /*29*/:
                    this.mNextFocusDownId = a.getResourceId(attr, VIEW_IS_NOT_CHECKED);
                    break;
                case StatisticalConstant.TYPE_POWERKEY_MAXXXXXX /*30*/:
                    if (!a.getBoolean(attr, DBG)) {
                        break;
                    }
                    viewFlagValues |= RECENT_APPS_VISIBLE;
                    viewFlagMasks |= RECENT_APPS_VISIBLE;
                    break;
                case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CHANGE /*31*/:
                    if (!a.getBoolean(attr, DBG)) {
                        break;
                    }
                    viewFlagValues |= STATUS_BAR_DISABLE_HOME;
                    viewFlagMasks |= STATUS_BAR_DISABLE_HOME;
                    break;
                case SCROLL_INDICATOR_END /*32*/:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues |= TV_PICTURE_IN_PICTURE_VISIBLE;
                        viewFlagMasks |= TV_PICTURE_IN_PICTURE_VISIBLE;
                        break;
                    }
                    break;
                case FOCUS_UP /*33*/:
                    int cacheQuality = a.getInt(attr, VISIBLE);
                    if (cacheQuality == 0) {
                        break;
                    }
                    viewFlagValues |= DRAWING_CACHE_QUALITY_FLAGS[cacheQuality];
                    viewFlagMasks |= DRAWING_CACHE_QUALITY_MASK;
                    break;
                case StatisticalConstant.TYPE_MULTIWINDOW_FRAME_SIZE_CHANGED /*34*/:
                    if (!a.getBoolean(attr, DBG)) {
                        break;
                    }
                    viewFlagValues |= STATUS_BAR_DISABLE_BACK;
                    viewFlagMasks |= STATUS_BAR_DISABLE_BACK;
                    break;
                case PerfHub.PERF_TAG_AVL_B_CPU_FREQ_LIST /*35*/:
                    if (targetSdkVersion < PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT && !(this instanceof FrameLayout)) {
                        break;
                    }
                    setForeground(a.getDrawable(attr));
                    break;
                    break;
                case PerfHub.PERF_TAG_AVL_GPU_FREQ_LIST /*36*/:
                    this.mMinWidth = a.getDimensionPixelSize(attr, VISIBLE);
                    break;
                case PerfHub.PERF_TAG_AVL_DDR_FREQ_LIST /*37*/:
                    this.mMinHeight = a.getDimensionPixelSize(attr, VISIBLE);
                    break;
                case PerfHub.PERF_TAG_CTRL_TYPE_NEW /*38*/:
                    if (targetSdkVersion < PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT && !(this instanceof FrameLayout)) {
                        break;
                    }
                    setForegroundGravity(a.getInt(attr, VISIBLE));
                    break;
                case PerfHub.PERF_TAG_MAX /*39*/:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues &= -134217729;
                        viewFlagMasks |= SOUND_EFFECTS_ENABLED;
                        break;
                    }
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_START /*40*/:
                    if (!a.getBoolean(attr, DBG)) {
                        break;
                    }
                    viewFlagValues |= STATUS_BAR_TRANSIENT;
                    viewFlagMasks |= STATUS_BAR_TRANSIENT;
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_ENTER /*41*/:
                    setScrollContainer = true;
                    if (!a.getBoolean(attr, DBG)) {
                        break;
                    }
                    setScrollContainer(true);
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_EXIT /*42*/:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues &= -268435457;
                        viewFlagMasks |= STATUS_BAR_UNHIDE;
                        break;
                    }
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_ENTER_2S_EXIT /*43*/:
                    if (!context.isRestricted()) {
                        String handlerName = a.getString(attr);
                        if (handlerName == null) {
                            break;
                        }
                        setOnClickListener(new DeclaredOnClickListener(this, handlerName));
                        break;
                    }
                    throw new IllegalStateException("The android:onClick attribute cannot be used within a restricted context");
                case RILConstants.RIL_REQUEST_CHANGE_BARRING_PASSWORD /*44*/:
                    setContentDescription(a.getString(attr));
                    break;
                case PFLAG2_LAYOUT_DIRECTION_RESOLVED_MASK /*48*/:
                    overScrollMode = a.getInt(attr, VIEW_IS_NOT_ADVIEW_OR_DISABLE);
                    break;
                case RILConstants.RIL_REQUEST_DTMF_START /*49*/:
                    if (!a.getBoolean(attr, DBG)) {
                        break;
                    }
                    viewFlagValues |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    viewFlagMasks |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    break;
                case StatisticalConstant.TYPE_SINGLEHAND_END /*50*/:
                    setAlpha(a.getFloat(attr, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL));
                    break;
                case StatisticalConstant.TYPE_WIFI_SURFING /*51*/:
                    setPivotX((float) a.getDimensionPixelOffset(attr, VISIBLE));
                    break;
                case StatisticalConstant.TYPE_WIFI_OPERATION_INFO /*52*/:
                    setPivotY((float) a.getDimensionPixelOffset(attr, VISIBLE));
                    break;
                case StatisticalConstant.TYPE_WIFI_DISCONNECT /*53*/:
                    tx = (float) a.getDimensionPixelOffset(attr, VISIBLE);
                    transformSet = true;
                    break;
                case StatisticalConstant.TYPE_WIFI_CONNECTION_ACTION /*54*/:
                    ty = (float) a.getDimensionPixelOffset(attr, VISIBLE);
                    transformSet = true;
                    break;
                case RILConstants.RIL_REQUEST_QUERY_CLIP /*55*/:
                    sx = a.getFloat(attr, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                    transformSet = true;
                    break;
                case RILConstants.RIL_REQUEST_LAST_DATA_CALL_FAIL_CAUSE /*56*/:
                    sy = a.getFloat(attr, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                    transformSet = true;
                    break;
                case RILConstants.RIL_REQUEST_DATA_CALL_LIST /*57*/:
                    rotation = a.getFloat(attr, 0.0f);
                    transformSet = true;
                    break;
                case RILConstants.RIL_REQUEST_RESET_RADIO /*58*/:
                    rotationX = a.getFloat(attr, 0.0f);
                    transformSet = true;
                    break;
                case RILConstants.RIL_REQUEST_OEM_HOOK_RAW /*59*/:
                    rotationY = a.getFloat(attr, 0.0f);
                    transformSet = true;
                    break;
                case StatisticalConstant.TYPE_MEDIA_FOUNCTION_STATICS /*60*/:
                    this.mVerticalScrollbarPosition = a.getInt(attr, VISIBLE);
                    break;
                case StatisticalConstant.TYPE_WIFI_END /*61*/:
                    this.mNextFocusForwardId = a.getResourceId(attr, VIEW_IS_NOT_CHECKED);
                    break;
                case RILConstants.RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION /*62*/:
                    setLayerType(a.getInt(attr, VISIBLE), null);
                    break;
                case RILConstants.RIL_REQUEST_WRITE_SMS_TO_SIM /*63*/:
                    int fadingEdge = a.getInt(attr, VISIBLE);
                    if (fadingEdge == 0) {
                        break;
                    }
                    viewFlagValues |= fadingEdge;
                    viewFlagMasks |= FADING_EDGE_MASK;
                    initializeFadingEdgeInternal(a);
                    break;
                case PFLAG_DRAW_ANIMATION /*64*/:
                    setImportantForAccessibility(a.getInt(attr, VISIBLE));
                    break;
                case RILConstants.RIL_REQUEST_SET_BAND_MODE /*65*/:
                    this.mPrivateFlags2 &= -449;
                    int textDirection = a.getInt(attr, VIEW_IS_NOT_CHECKED);
                    if (textDirection == VIEW_IS_NOT_CHECKED) {
                        break;
                    }
                    this.mPrivateFlags2 |= PFLAG2_TEXT_DIRECTION_FLAGS[textDirection];
                    break;
                case FOCUS_RIGHT /*66*/:
                    this.mPrivateFlags2 &= -57345;
                    this.mPrivateFlags2 |= PFLAG2_TEXT_ALIGNMENT_FLAGS[a.getInt(attr, VIEW_IS_NOT_ADVIEW_OR_DISABLE)];
                    break;
                case RILConstants.RIL_REQUEST_STK_GET_PROFILE /*67*/:
                    this.mPrivateFlags2 &= -61;
                    int layoutDirection = a.getInt(attr, VIEW_IS_NOT_CHECKED);
                    this.mPrivateFlags2 |= (layoutDirection != VIEW_IS_NOT_CHECKED ? LAYOUT_DIRECTION_FLAGS[layoutDirection] : TEXT_DIRECTION_ANY_RTL) << TEXT_DIRECTION_ANY_RTL;
                    break;
                case RILConstants.RIL_REQUEST_STK_SET_PROFILE /*68*/:
                    startPadding = a.getDimensionPixelSize(attr, UNDEFINED_PADDING);
                    if (startPadding == UNDEFINED_PADDING) {
                        startPaddingDefined = DBG;
                        break;
                    } else {
                        startPaddingDefined = true;
                        break;
                    }
                case RILConstants.RIL_REQUEST_STK_SEND_ENVELOPE_COMMAND /*69*/:
                    endPadding = a.getDimensionPixelSize(attr, UNDEFINED_PADDING);
                    if (endPadding == UNDEFINED_PADDING) {
                        endPaddingDefined = DBG;
                        break;
                    } else {
                        endPaddingDefined = true;
                        break;
                    }
                case StatisticalConstant.TYPE_NAVIGATIONBAR_END /*70*/:
                    setLabelFor(a.getResourceId(attr, VIEW_IS_NOT_CHECKED));
                    break;
                case StatisticalConstant.TYPE_SCREEN_SHOT /*71*/:
                    setAccessibilityLiveRegion(a.getInt(attr, VISIBLE));
                    break;
                case RILConstants.RIL_REQUEST_EXPLICIT_CALL_TRANSFER /*72*/:
                    tz = (float) a.getDimensionPixelOffset(attr, VISIBLE);
                    transformSet = true;
                    break;
                case RILConstants.RIL_REQUEST_SET_PREFERRED_NETWORK_TYPE /*73*/:
                    setTransitionName(a.getString(attr));
                    break;
                case RILConstants.RIL_REQUEST_GET_PREFERRED_NETWORK_TYPE /*74*/:
                    setNestedScrollingEnabled(a.getBoolean(attr, DBG));
                    break;
                case RILConstants.RIL_REQUEST_GET_NEIGHBORING_CELL_IDS /*75*/:
                    elevation = (float) a.getDimensionPixelOffset(attr, VISIBLE);
                    transformSet = true;
                    break;
                case RILConstants.RIL_REQUEST_SET_LOCATION_UPDATES /*76*/:
                    setStateListAnimator(AnimatorInflater.loadStateListAnimator(context, a.getResourceId(attr, VISIBLE)));
                    break;
                case RILConstants.RIL_REQUEST_CDMA_SET_SUBSCRIPTION_SOURCE /*77*/:
                    if (this.mBackgroundTint == null) {
                        this.mBackgroundTint = new TintInfo();
                    }
                    this.mBackgroundTint.mTintList = a.getColorStateList(77);
                    this.mBackgroundTint.mHasTintList = true;
                    break;
                case RILConstants.RIL_REQUEST_CDMA_SET_ROAMING_PREFERENCE /*78*/:
                    if (this.mBackgroundTint == null) {
                        this.mBackgroundTint = new TintInfo();
                    }
                    this.mBackgroundTint.mTintMode = Drawable.parseTintMode(a.getInt(78, VIEW_IS_NOT_CHECKED), null);
                    this.mBackgroundTint.mHasTintMode = true;
                    break;
                case RILConstants.RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE /*79*/:
                    if (targetSdkVersion < PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT && !(this instanceof FrameLayout)) {
                        break;
                    }
                    setForegroundTintList(a.getColorStateList(attr));
                    break;
                    break;
                case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                    if (targetSdkVersion < PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT && !(this instanceof FrameLayout)) {
                        break;
                    }
                    setForegroundTintMode(Drawable.parseTintMode(a.getInt(attr, VIEW_IS_NOT_CHECKED), null));
                    break;
                case StatisticalConstant.TYPE_TOUCH_FORCE_CALL_NAVIGATION /*81*/:
                    setOutlineProviderFromAttribute(a.getInt(81, VISIBLE));
                    break;
                case StatisticalConstant.TYPE_TOUCH_FORCE_OPEAN_APPLICATION /*82*/:
                    setAccessibilityTraversalBefore(a.getResourceId(attr, VIEW_IS_NOT_CHECKED));
                    break;
                case RILConstants.RIL_REQUEST_CDMA_QUERY_PREFERRED_VOICE_PRIVACY_MODE /*83*/:
                    setAccessibilityTraversalAfter(a.getResourceId(attr, VIEW_IS_NOT_CHECKED));
                    break;
                case RILConstants.RIL_REQUEST_CDMA_FLASH /*84*/:
                    int scrollIndicators = (a.getInt(attr, VISIBLE) << STATUS_BAR_TRANSPARENT) & SCROLL_INDICATORS_PFLAG3_MASK;
                    if (scrollIndicators == 0) {
                        break;
                    }
                    this.mPrivateFlags3 |= scrollIndicators;
                    initializeScrollIndicators = true;
                    break;
                case RILConstants.RIL_REQUEST_CDMA_BURST_DTMF /*85*/:
                    if (!a.getBoolean(attr, DBG)) {
                        break;
                    }
                    viewFlagValues |= STATUS_BAR_DISABLE_CLOCK;
                    viewFlagMasks |= STATUS_BAR_DISABLE_CLOCK;
                    break;
                case RILConstants.RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY /*86*/:
                    int resourceId = a.getResourceId(attr, VISIBLE);
                    if (resourceId == 0) {
                        int pointerType = a.getInt(attr, VIEW_IS_NOT_ADVIEW_OR_DISABLE);
                        if (pointerType == VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                            break;
                        }
                        setPointerIcon(PointerIcon.getSystemIcon(context, pointerType));
                        break;
                    }
                    setPointerIcon(PointerIcon.load(context.getResources(), resourceId));
                    break;
                case RILConstants.RIL_REQUEST_CDMA_SEND_SMS /*87*/:
                    if (a.peekValue(attr) == null) {
                        break;
                    }
                    forceHasOverlappingRendering(a.getBoolean(attr, true));
                    break;
                case RILConstants.RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE /*88*/:
                    if (targetSdkVersion < PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT && !(this instanceof FrameLayout)) {
                        break;
                    }
                    if (this.mForegroundInfo == null) {
                        this.mForegroundInfo = new ForegroundInfo();
                    }
                    this.mForegroundInfo.mInsidePadding = a.getBoolean(attr, this.mForegroundInfo.mInsidePadding);
                    break;
                    break;
                default:
                    break;
            }
        }
        TypedArray ahwext = context.obtainStyledAttributes(attrs, androidhwext.R.styleable.View, defStyleAttr, defStyleRes);
        int Nhwext = ahwext.getIndexCount();
        for (i = VISIBLE; i < Nhwext; i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
            attr = ahwext.getIndex(i);
            switch (attr) {
                case VISIBLE /*0*/:
                    setHwStateListAnimator(AnimatorInflater.loadHwStateListAnimator(context, ahwext.getResourceId(attr, VISIBLE)));
                    break;
                default:
                    break;
            }
        }
        setOverScrollMode(overScrollMode);
        this.mUserPaddingStart = startPadding;
        this.mUserPaddingEnd = endPadding;
        if (background != null) {
            setBackground(background);
        }
        this.mLeftPaddingDefined = leftPaddingDefined;
        this.mRightPaddingDefined = rightPaddingDefined;
        if (padding >= 0) {
            leftPadding = padding;
            topPadding = padding;
            rightPadding = padding;
            bottomPadding = padding;
            this.mUserPaddingLeftInitial = padding;
            this.mUserPaddingRightInitial = padding;
        }
        if (isRtlCompatibilityMode()) {
            if (!this.mLeftPaddingDefined && startPaddingDefined) {
                leftPadding = startPadding;
            }
            this.mUserPaddingLeftInitial = leftPadding >= 0 ? leftPadding : this.mUserPaddingLeftInitial;
            if (!this.mRightPaddingDefined && endPaddingDefined) {
                rightPadding = endPadding;
            }
            if (rightPadding >= 0) {
                i2 = rightPadding;
            } else {
                i2 = this.mUserPaddingRightInitial;
            }
            this.mUserPaddingRightInitial = i2;
        } else {
            boolean z = !startPaddingDefined ? endPaddingDefined : true;
            if (this.mLeftPaddingDefined && !z) {
                this.mUserPaddingLeftInitial = leftPadding;
            }
            if (this.mRightPaddingDefined && !z) {
                this.mUserPaddingRightInitial = rightPadding;
            }
        }
        i2 = this.mUserPaddingLeftInitial;
        if (topPadding < 0) {
            topPadding = this.mPaddingTop;
        }
        int i3 = this.mUserPaddingRightInitial;
        if (bottomPadding < 0) {
            bottomPadding = this.mPaddingBottom;
        }
        internalSetPadding(i2, topPadding, i3, bottomPadding);
        if (viewFlagMasks != 0) {
            setFlags(viewFlagValues, viewFlagMasks);
        }
        if (initializeScrollbars) {
            initializeScrollbarsInternal(a);
        }
        if (initializeScrollIndicators) {
            initializeScrollIndicatorsInternal();
        }
        a.recycle();
        if (scrollbarStyle != 0) {
            recomputePadding();
        }
        if (!(x == 0 && y == 0)) {
            scrollTo(x, y);
        }
        if (transformSet) {
            setTranslationX(tx);
            setTranslationY(ty);
            setTranslationZ(tz);
            setElevation(elevation);
            setRotation(rotation);
            setRotationX(rotationX);
            setRotationY(rotationY);
            setScaleX(sx);
            setScaleY(sy);
        }
        if (!(setScrollContainer || (viewFlagValues & SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) == 0)) {
            setScrollContainer(true);
        }
        computeOpaqueFlags();
    }

    View() {
        this.mCurrentAnimation = null;
        this.mRecreateDisplayList = DBG;
        this.mID = VIEW_IS_NOT_CHECKED;
        this.mAccessibilityViewId = VIEW_IS_NOT_CHECKED;
        this.mAccessibilityCursorPosition = VIEW_IS_NOT_CHECKED;
        this.mTag = null;
        this.mForceRTL = DBG;
        this.mTransientStateCount = VISIBLE;
        this.mClipBounds = null;
        this.mPaddingLeft = VISIBLE;
        this.mPaddingRight = VISIBLE;
        this.mLabelForId = VIEW_IS_NOT_CHECKED;
        this.mAccessibilityTraversalBeforeId = VIEW_IS_NOT_CHECKED;
        this.mAccessibilityTraversalAfterId = VIEW_IS_NOT_CHECKED;
        this.mLeftPaddingDefined = DBG;
        this.mRightPaddingDefined = DBG;
        this.mOldWidthMeasureSpec = UNDEFINED_PADDING;
        this.mOldHeightMeasureSpec = UNDEFINED_PADDING;
        this.mLongClickX = Float.NaN;
        this.mLongClickY = Float.NaN;
        this.mDrawableState = null;
        this.mOutlineProvider = ViewOutlineProvider.BACKGROUND;
        this.mNextFocusLeftId = VIEW_IS_NOT_CHECKED;
        this.mNextFocusRightId = VIEW_IS_NOT_CHECKED;
        this.mNextFocusUpId = VIEW_IS_NOT_CHECKED;
        this.mNextFocusDownId = VIEW_IS_NOT_CHECKED;
        this.mNextFocusForwardId = VIEW_IS_NOT_CHECKED;
        this.mPendingCheckForTap = null;
        this.mTouchDelegate = null;
        this.mDrawingCacheBackgroundColor = VISIBLE;
        this.mAnimator = null;
        this.mLayerType = VISIBLE;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, VISIBLE) : null;
        this.mViewCheckedResult = VIEW_IS_NOT_CHECKED;
        this.mResources = null;
        this.mRenderNode = RenderNode.create(getClass().getName(), this);
    }

    private static SparseArray<String> getAttributeMap() {
        if (mAttributeMap == null) {
            mAttributeMap = new SparseArray();
        }
        return mAttributeMap;
    }

    private void saveAttributeData(AttributeSet attrs, TypedArray t) {
        int j;
        int attrsCount = attrs == null ? VISIBLE : attrs.getAttributeCount();
        int indexCount = t.getIndexCount();
        String[] attributes = new String[((attrsCount + indexCount) * TEXT_DIRECTION_ANY_RTL)];
        int i = VISIBLE;
        for (j = VISIBLE; j < attrsCount; j += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
            attributes[i] = attrs.getAttributeName(j);
            attributes[i + VIEW_IS_NOT_ADVIEW_OR_DISABLE] = attrs.getAttributeValue(j);
            i += TEXT_DIRECTION_ANY_RTL;
        }
        Resources res = t.getResources();
        SparseArray<String> attributeMap = getAttributeMap();
        for (j = VISIBLE; j < indexCount; j += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
            int index = t.getIndex(j);
            if (t.hasValueOrEmpty(index)) {
                int resourceId = t.getResourceId(index, VISIBLE);
                if (resourceId != 0) {
                    String resourceName = (String) attributeMap.get(resourceId);
                    if (resourceName == null) {
                        try {
                            resourceName = res.getResourceName(resourceId);
                        } catch (NotFoundException e) {
                            resourceName = "0x" + Integer.toHexString(resourceId);
                        }
                        attributeMap.put(resourceId, resourceName);
                    }
                    attributes[i] = resourceName;
                    attributes[i + VIEW_IS_NOT_ADVIEW_OR_DISABLE] = t.getString(index);
                    i += TEXT_DIRECTION_ANY_RTL;
                }
            }
        }
        String[] trimmed = new String[i];
        System.arraycopy(attributes, VISIBLE, trimmed, VISIBLE, i);
        this.mAttributes = trimmed;
    }

    public String toString() {
        char c;
        char c2 = 'D';
        StringBuilder out = new StringBuilder(WILL_NOT_DRAW);
        out.append(getClass().getName());
        out.append('{');
        out.append(Integer.toHexString(System.identityHashCode(this)));
        out.append(' ');
        switch (this.mViewFlags & VISIBILITY_MASK) {
            case VISIBLE /*0*/:
                out.append('V');
                break;
            case TEXT_DIRECTION_RTL /*4*/:
                out.append('I');
                break;
            case STATUS_BAR_TRANSPARENT /*8*/:
                out.append('G');
                break;
            default:
                out.append('.');
                break;
        }
        if ((this.mViewFlags & VIEW_IS_NOT_ADVIEW_OR_DISABLE) == VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
            c = 'F';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & SCROLL_INDICATOR_END) == 0) {
            c = DateFormat.DAY;
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & WILL_NOT_DRAW) == WILL_NOT_DRAW) {
            c = '.';
        } else {
            c = 'D';
        }
        out.append(c);
        if ((this.mViewFlags & SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0) {
            c = 'H';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0) {
            c = 'V';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & RECENT_APPS_VISIBLE) != 0) {
            c = 'C';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & STATUS_BAR_DISABLE_HOME) != 0) {
            c = DateFormat.STANDALONE_MONTH;
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & STATUS_BAR_DISABLE_CLOCK) != 0) {
            c = 'X';
        } else {
            c = '.';
        }
        out.append(c);
        out.append(' ');
        if ((this.mPrivateFlags & STATUS_BAR_TRANSPARENT) != 0) {
            c = 'R';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & TEXT_DIRECTION_ANY_RTL) != 0) {
            c = 'F';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & TEXT_DIRECTION_RTL) != 0) {
            c = 'S';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & STATUS_BAR_DISABLE_SEARCH) != 0) {
            out.append('p');
        } else {
            out.append((this.mPrivateFlags & RECENT_APPS_VISIBLE) != 0 ? 'P' : '.');
        }
        if ((this.mPrivateFlags & STATUS_BAR_UNHIDE) != 0) {
            c = 'H';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & STATUS_BAR_TRANSLUCENT) != 0) {
            c = DateFormat.CAPITAL_AM_PM;
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & UNDEFINED_PADDING) != 0) {
            c = 'I';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & PFLAG_DIRTY_MASK) == 0) {
            c2 = '.';
        }
        out.append(c2);
        out.append(' ');
        out.append(this.mLeft);
        out.append(PhoneNumberUtils.PAUSE);
        out.append(this.mTop);
        out.append('-');
        out.append(this.mRight);
        out.append(PhoneNumberUtils.PAUSE);
        out.append(this.mBottom);
        int id = getId();
        if (id != VIEW_IS_NOT_CHECKED) {
            out.append(" #");
            out.append(Integer.toHexString(id));
            Resources r = this.mResources;
            if (id > 0 && Resources.resourceHasPackage(id) && r != null) {
                String pkgname;
                switch (MEASURED_STATE_MASK & id) {
                    case STATUS_BAR_DISABLE_RECENT /*16777216*/:
                        pkgname = "android";
                        break;
                    case 2130706432:
                        pkgname = "app";
                        break;
                    default:
                        try {
                            pkgname = r.getResourcePackageName(id);
                            break;
                        } catch (NotFoundException e) {
                            break;
                        }
                }
                String typename = r.getResourceTypeName(id);
                String entryname = r.getResourceEntryName(id);
                out.append(" ");
                out.append(pkgname);
                out.append(":");
                out.append(typename);
                out.append("/");
                out.append(entryname);
            }
        }
        out.append("}");
        return out.toString();
    }

    protected void initializeFadingEdge(TypedArray a) {
        TypedArray arr = this.mContext.obtainStyledAttributes(R.styleable.View);
        initializeFadingEdgeInternal(arr);
        arr.recycle();
    }

    protected void initializeFadingEdgeInternal(TypedArray a) {
        initScrollCache();
        this.mScrollCache.fadingEdgeLength = a.getDimensionPixelSize(25, ViewConfiguration.get(this.mContext).getScaledFadingEdgeLength());
    }

    public int getVerticalFadingEdgeLength() {
        if (isVerticalFadingEdgeEnabled()) {
            ScrollabilityCache cache = this.mScrollCache;
            if (cache != null) {
                return cache.fadingEdgeLength;
            }
        }
        return VISIBLE;
    }

    public void setFadingEdgeLength(int length) {
        initScrollCache();
        this.mScrollCache.fadingEdgeLength = length;
    }

    public int getHorizontalFadingEdgeLength() {
        if (isHorizontalFadingEdgeEnabled()) {
            ScrollabilityCache cache = this.mScrollCache;
            if (cache != null) {
                return cache.fadingEdgeLength;
            }
        }
        return VISIBLE;
    }

    public int getVerticalScrollbarWidth() {
        ScrollabilityCache cache = this.mScrollCache;
        if (cache == null) {
            return VISIBLE;
        }
        ScrollBarDrawable scrollBar = cache.scrollBar;
        if (scrollBar == null) {
            return VISIBLE;
        }
        int size = scrollBar.getSize(true);
        if (size <= 0) {
            size = cache.scrollBarSize;
        }
        return size;
    }

    protected int getHorizontalScrollbarHeight() {
        ScrollabilityCache cache = this.mScrollCache;
        if (cache == null) {
            return VISIBLE;
        }
        ScrollBarDrawable scrollBar = cache.scrollBar;
        if (scrollBar == null) {
            return VISIBLE;
        }
        int size = scrollBar.getSize(DBG);
        if (size <= 0) {
            size = cache.scrollBarSize;
        }
        return size;
    }

    protected void initializeScrollbars(TypedArray a) {
        TypedArray arr = this.mContext.obtainStyledAttributes(R.styleable.View);
        initializeScrollbarsInternal(arr);
        arr.recycle();
    }

    protected void initializeScrollbarsInternal(TypedArray a) {
        initScrollCache();
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (scrollabilityCache.scrollBar == null) {
            scrollabilityCache.scrollBar = new ScrollBarDrawable();
            scrollabilityCache.scrollBar.setState(getDrawableState());
            scrollabilityCache.scrollBar.setCallback(this);
        }
        boolean fadeScrollbars = a.getBoolean(47, true);
        if (!fadeScrollbars) {
            scrollabilityCache.state = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
        scrollabilityCache.fadeScrollBars = fadeScrollbars;
        scrollabilityCache.scrollBarFadeDuration = a.getInt(45, ViewConfiguration.getScrollBarFadeDuration());
        scrollabilityCache.scrollBarDefaultDelayBeforeFade = a.getInt(46, ViewConfiguration.getScrollDefaultDelay());
        scrollabilityCache.scrollBarSize = a.getDimensionPixelSize(VIEW_IS_NOT_ADVIEW_OR_DISABLE, ViewConfiguration.get(this.mContext).getScaledScrollBarSize());
        scrollabilityCache.scrollBar.setHorizontalTrackDrawable(a.getDrawable(TEXT_DIRECTION_RTL));
        Drawable thumb = a.getDrawable(TEXT_DIRECTION_ANY_RTL);
        if (thumb != null) {
            scrollabilityCache.scrollBar.setHorizontalThumbDrawable(thumb);
        }
        if (a.getBoolean(TEXT_DIRECTION_FIRST_STRONG_LTR, DBG)) {
            scrollabilityCache.scrollBar.setAlwaysDrawHorizontalTrack(true);
        }
        Drawable track = a.getDrawable(TEXT_DIRECTION_LOCALE);
        scrollabilityCache.scrollBar.setVerticalTrackDrawable(track);
        thumb = a.getDrawable(TEXT_DIRECTION_LTR);
        if (thumb != null) {
            scrollabilityCache.scrollBar.setVerticalThumbDrawable(thumb);
        }
        if (a.getBoolean(TEXT_DIRECTION_FIRST_STRONG_RTL, DBG)) {
            scrollabilityCache.scrollBar.setAlwaysDrawVerticalTrack(true);
        }
        int layoutDirection = getLayoutDirection();
        if (track != null) {
            track.setLayoutDirection(layoutDirection);
        }
        if (thumb != null) {
            thumb.setLayoutDirection(layoutDirection);
        }
        resolvePadding();
    }

    private void initializeScrollIndicatorsInternal() {
        if (this.mScrollIndicatorDrawable == null) {
            this.mScrollIndicatorDrawable = initializeVariousScrollIndicators(this.mContext);
        }
    }

    private void initScrollCache() {
        if (this.mScrollCache == null) {
            this.mScrollCache = new ScrollabilityCache(ViewConfiguration.get(this.mContext), this);
        }
    }

    private ScrollabilityCache getScrollCache() {
        initScrollCache();
        return this.mScrollCache;
    }

    public void setVerticalScrollbarPosition(int position) {
        if (this.mVerticalScrollbarPosition != position) {
            this.mVerticalScrollbarPosition = position;
            computeOpaqueFlags();
            resolvePadding();
        }
    }

    public int getVerticalScrollbarPosition() {
        return this.mVerticalScrollbarPosition;
    }

    boolean isOnScrollbar(float x, float y) {
        if (this.mScrollCache == null) {
            return DBG;
        }
        x += (float) getScrollX();
        y += (float) getScrollY();
        if (isVerticalScrollBarEnabled() && !isVerticalScrollBarHidden()) {
            Rect bounds;
            bounds = this.mScrollCache.mScrollBarBounds;
            getVerticalScrollBarBounds(bounds);
            if (bounds.contains((int) x, (int) y)) {
                return true;
            }
        }
        if (isHorizontalScrollBarEnabled()) {
            bounds = this.mScrollCache.mScrollBarBounds;
            getHorizontalScrollBarBounds(bounds);
            if (bounds.contains((int) x, (int) y)) {
                return true;
            }
        }
        return DBG;
    }

    boolean isOnScrollbarThumb(float x, float y) {
        return !isOnVerticalScrollbarThumb(x, y) ? isOnHorizontalScrollbarThumb(x, y) : true;
    }

    private boolean isOnVerticalScrollbarThumb(float x, float y) {
        if (!(this.mScrollCache == null || !isVerticalScrollBarEnabled() || isVerticalScrollBarHidden())) {
            x += (float) getScrollX();
            y += (float) getScrollY();
            Rect bounds = this.mScrollCache.mScrollBarBounds;
            getVerticalScrollBarBounds(bounds);
            int range = computeVerticalScrollRange();
            int offset = computeVerticalScrollOffset();
            int extent = computeVerticalScrollExtent();
            int thumbLength = ScrollBarUtils.getThumbLength(bounds.height(), bounds.width(), extent, range);
            int thumbTop = bounds.top + ScrollBarUtils.getThumbOffset(bounds.height(), thumbLength, extent, range, offset);
            if (x >= ((float) bounds.left) && x <= ((float) bounds.right) && y >= ((float) thumbTop) && y <= ((float) (thumbTop + thumbLength))) {
                return true;
            }
        }
        return DBG;
    }

    private boolean isOnHorizontalScrollbarThumb(float x, float y) {
        if (this.mScrollCache != null && isHorizontalScrollBarEnabled()) {
            x += (float) getScrollX();
            y += (float) getScrollY();
            Rect bounds = this.mScrollCache.mScrollBarBounds;
            getHorizontalScrollBarBounds(bounds);
            int range = computeHorizontalScrollRange();
            int offset = computeHorizontalScrollOffset();
            int extent = computeHorizontalScrollExtent();
            int thumbLength = ScrollBarUtils.getThumbLength(bounds.width(), bounds.height(), extent, range);
            int thumbLeft = bounds.left + ScrollBarUtils.getThumbOffset(bounds.width(), thumbLength, extent, range, offset);
            if (x >= ((float) thumbLeft) && x <= ((float) (thumbLeft + thumbLength)) && y >= ((float) bounds.top) && y <= ((float) bounds.bottom)) {
                return true;
            }
        }
        return DBG;
    }

    boolean isDraggingScrollBar() {
        if (this.mScrollCache == null || this.mScrollCache.mScrollBarDraggingState == 0) {
            return DBG;
        }
        return true;
    }

    public void setScrollIndicators(int indicators) {
        setScrollIndicators(indicators, 63);
    }

    public int getScrollIndicators() {
        return (this.mPrivateFlags3 & SCROLL_INDICATORS_PFLAG3_MASK) >>> STATUS_BAR_TRANSPARENT;
    }

    ListenerInfo getListenerInfo() {
        if (this.mListenerInfo != null) {
            return this.mListenerInfo;
        }
        this.mListenerInfo = new ListenerInfo();
        return this.mListenerInfo;
    }

    public void setOnScrollChangeListener(OnScrollChangeListener l) {
        getListenerInfo().mOnScrollChangeListener = l;
    }

    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        getListenerInfo().mOnFocusChangeListener = l;
    }

    public void addOnLayoutChangeListener(OnLayoutChangeListener listener) {
        ListenerInfo li = getListenerInfo();
        if (li.mOnLayoutChangeListeners == null) {
            li.mOnLayoutChangeListeners = new ArrayList();
        }
        if (!li.mOnLayoutChangeListeners.contains(listener)) {
            li.mOnLayoutChangeListeners.add(listener);
        }
    }

    public void removeOnLayoutChangeListener(OnLayoutChangeListener listener) {
        ListenerInfo li = this.mListenerInfo;
        if (li != null && li.mOnLayoutChangeListeners != null) {
            li.mOnLayoutChangeListeners.remove(listener);
        }
    }

    public void addOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
        ListenerInfo li = getListenerInfo();
        if (li.mOnAttachStateChangeListeners == null) {
            li.mOnAttachStateChangeListeners = new CopyOnWriteArrayList();
        }
        li.mOnAttachStateChangeListeners.add(listener);
    }

    public void removeOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
        ListenerInfo li = this.mListenerInfo;
        if (li != null && li.mOnAttachStateChangeListeners != null) {
            li.mOnAttachStateChangeListeners.remove(listener);
        }
    }

    public OnFocusChangeListener getOnFocusChangeListener() {
        ListenerInfo li = this.mListenerInfo;
        if (li != null) {
            return li.mOnFocusChangeListener;
        }
        return null;
    }

    public void setOnClickListener(OnClickListener l) {
        if (!isClickable()) {
            setClickable(true);
        }
        getListenerInfo().mOnClickListener = l;
    }

    public boolean hasOnClickListeners() {
        ListenerInfo li = this.mListenerInfo;
        if (li == null || li.mOnClickListener == null) {
            return DBG;
        }
        return true;
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        getListenerInfo().mOnLongClickListener = l;
    }

    public void setOnContextClickListener(OnContextClickListener l) {
        if (!isContextClickable()) {
            setContextClickable(true);
        }
        getListenerInfo().mOnContextClickListener = l;
    }

    public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        getListenerInfo().mOnCreateContextMenuListener = l;
    }

    public void addFrameMetricsListener(Window window, OnFrameMetricsAvailableListener listener, Handler handler) {
        if (this.mAttachInfo == null) {
            if (this.mFrameMetricsObservers == null) {
                this.mFrameMetricsObservers = new ArrayList();
            }
            this.mFrameMetricsObservers.add(new FrameMetricsObserver(window, handler.getLooper(), listener));
        } else if (this.mAttachInfo.mHardwareRenderer != null) {
            if (this.mFrameMetricsObservers == null) {
                this.mFrameMetricsObservers = new ArrayList();
            }
            FrameMetricsObserver fmo = new FrameMetricsObserver(window, handler.getLooper(), listener);
            this.mFrameMetricsObservers.add(fmo);
            this.mAttachInfo.mHardwareRenderer.addFrameMetricsObserver(fmo);
        } else {
            Log.w(VIEW_LOG_TAG, "View not hardware-accelerated. Unable to observe frame stats");
        }
    }

    public void removeFrameMetricsListener(OnFrameMetricsAvailableListener listener) {
        ThreadedRenderer renderer = getHardwareRenderer();
        FrameMetricsObserver fmo = findFrameMetricsObserver(listener);
        if (fmo == null) {
            throw new IllegalArgumentException("attempt to remove OnFrameMetricsAvailableListener that was never added");
        } else if (this.mFrameMetricsObservers != null) {
            this.mFrameMetricsObservers.remove(fmo);
            if (renderer != null) {
                renderer.removeFrameMetricsObserver(fmo);
            }
        }
    }

    private void registerPendingFrameMetricsObservers() {
        if (this.mFrameMetricsObservers != null) {
            ThreadedRenderer renderer = getHardwareRenderer();
            if (renderer != null) {
                for (FrameMetricsObserver fmo : this.mFrameMetricsObservers) {
                    renderer.addFrameMetricsObserver(fmo);
                }
                return;
            }
            Log.w(VIEW_LOG_TAG, "View not hardware-accelerated. Unable to observe frame stats");
        }
    }

    private FrameMetricsObserver findFrameMetricsObserver(OnFrameMetricsAvailableListener listener) {
        for (int i = VISIBLE; i < this.mFrameMetricsObservers.size(); i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
            FrameMetricsObserver observer = (FrameMetricsObserver) this.mFrameMetricsObservers.get(i);
            if (observer.mListener == listener) {
                return observer;
            }
        }
        return null;
    }

    public boolean performClick() {
        boolean result;
        ListenerInfo li = this.mListenerInfo;
        if (li == null || li.mOnClickListener == null) {
            result = DBG;
        } else {
            playSoundEffect(VISIBLE);
            li.mOnClickListener.onClick(this);
            HwFrameworkFactory.getHwView().onClick(this, this.mContext);
            result = true;
        }
        sendAccessibilityEvent(VIEW_IS_NOT_ADVIEW_OR_DISABLE);
        return result;
    }

    public boolean callOnClick() {
        ListenerInfo li = this.mListenerInfo;
        if (li == null || li.mOnClickListener == null) {
            return DBG;
        }
        li.mOnClickListener.onClick(this);
        return true;
    }

    public boolean performLongClick() {
        return performLongClickInternal(this.mLongClickX, this.mLongClickY);
    }

    public boolean performLongClick(float x, float y) {
        this.mLongClickX = x;
        this.mLongClickY = y;
        boolean handled = performLongClick();
        this.mLongClickX = Float.NaN;
        this.mLongClickY = Float.NaN;
        return handled;
    }

    private boolean performLongClickInternal(float x, float y) {
        sendAccessibilityEvent(TEXT_DIRECTION_ANY_RTL);
        boolean handled = DBG;
        ListenerInfo li = this.mListenerInfo;
        if (!(li == null || li.mOnLongClickListener == null)) {
            handled = li.mOnLongClickListener.onLongClick(this);
        }
        if (!handled) {
            boolean isAnchored = (Float.isNaN(x) || Float.isNaN(y)) ? DBG : true;
            handled = isAnchored ? showContextMenu(x, y) : showContextMenu();
        }
        if (handled) {
            performHapticFeedback(VISIBLE);
        }
        return handled;
    }

    public boolean performContextClick(float x, float y) {
        return performContextClick();
    }

    public boolean performContextClick() {
        sendAccessibilityEvent(STATUS_BAR_DISABLE_CLOCK);
        boolean handled = DBG;
        ListenerInfo li = this.mListenerInfo;
        if (!(li == null || li.mOnContextClickListener == null)) {
            handled = li.mOnContextClickListener.onContextClick(this);
        }
        if (handled) {
            performHapticFeedback(TEXT_DIRECTION_FIRST_STRONG_LTR);
        }
        return handled;
    }

    protected boolean performButtonActionOnTouchDown(MotionEvent event) {
        if (!event.isFromSource(InputDevice.SOURCE_MOUSE) || (event.getButtonState() & TEXT_DIRECTION_ANY_RTL) == 0) {
            return DBG;
        }
        showContextMenu(event.getX(), event.getY());
        this.mPrivateFlags |= STATUS_BAR_TRANSIENT;
        return true;
    }

    public boolean showContextMenu() {
        return getParent().showContextMenuForChild(this);
    }

    public boolean showContextMenu(float x, float y) {
        return getParent().showContextMenuForChild(this, x, y);
    }

    public ActionMode startActionMode(ActionMode.Callback callback) {
        return startActionMode(callback, VISIBLE);
    }

    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        ViewParent parent = getParent();
        if (parent == null) {
            return null;
        }
        try {
            return parent.startActionModeForChild(this, callback, type);
        } catch (AbstractMethodError e) {
            return parent.startActionModeForChild(this, callback);
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        this.mStartActivityRequestWho = "@android:view:" + System.identityHashCode(this);
        getContext().startActivityForResult(this.mStartActivityRequestWho, intent, requestCode, null);
    }

    public boolean dispatchActivityResult(String who, int requestCode, int resultCode, Intent data) {
        if (this.mStartActivityRequestWho == null || !this.mStartActivityRequestWho.equals(who)) {
            return DBG;
        }
        onActivityResult(requestCode, resultCode, data);
        this.mStartActivityRequestWho = null;
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public void setOnKeyListener(OnKeyListener l) {
        getListenerInfo().mOnKeyListener = l;
    }

    public void setOnTouchListener(OnTouchListener l) {
        getListenerInfo().mOnTouchListener = l;
    }

    public void setOnGenericMotionListener(OnGenericMotionListener l) {
        getListenerInfo().mOnGenericMotionListener = l;
    }

    public void setOnHoverListener(OnHoverListener l) {
        getListenerInfo().mOnHoverListener = l;
    }

    public void setOnDragListener(OnDragListener l) {
        getListenerInfo().mOnDragListener = l;
    }

    void handleFocusGainInternal(int direction, Rect previouslyFocusedRect) {
        if ((this.mPrivateFlags & TEXT_DIRECTION_ANY_RTL) == 0) {
            this.mPrivateFlags |= TEXT_DIRECTION_ANY_RTL;
            View findFocus = this.mAttachInfo != null ? getRootView().findFocus() : null;
            if (this.mParent != null) {
                this.mParent.requestChildFocus(this, this);
            }
            if (this.mAttachInfo != null) {
                this.mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(findFocus, this);
            }
            onFocusChanged(true, direction, previouslyFocusedRect);
            refreshDrawableState();
        }
    }

    public void getHotspotBounds(Rect outRect) {
        Drawable background = getBackground();
        if (background != null) {
            background.getHotspotBounds(outRect);
        } else {
            getBoundsOnScreen(outRect);
        }
    }

    public boolean requestRectangleOnScreen(Rect rectangle) {
        return requestRectangleOnScreen(rectangle, DBG);
    }

    public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
        if (this.mParent == null) {
            return DBG;
        }
        RectF position;
        View child = this;
        if (this.mAttachInfo != null) {
            position = this.mAttachInfo.mTmpTransformRect;
        } else {
            position = new RectF();
        }
        position.set(rectangle);
        ViewParent parent = this.mParent;
        boolean scrolled = DBG;
        while (parent != null) {
            rectangle.set((int) position.left, (int) position.top, (int) position.right, (int) position.bottom);
            scrolled |= parent.requestChildRectangleOnScreen(child, rectangle, immediate);
            if (!(parent instanceof View)) {
                break;
            }
            position.offset((float) (child.mLeft - child.getScrollX()), (float) (child.mTop - child.getScrollY()));
            child = (View) parent;
            parent = child.getParent();
        }
        return scrolled;
    }

    public void clearFocus() {
        clearFocusInternal(null, true, true);
    }

    void clearFocusInternal(View focused, boolean propagate, boolean refocus) {
        if ((this.mPrivateFlags & TEXT_DIRECTION_ANY_RTL) != 0) {
            this.mPrivateFlags &= -3;
            if (propagate && this.mParent != null) {
                this.mParent.clearChildFocus(this);
            }
            onFocusChanged(DBG, VISIBLE, null);
            refreshDrawableState();
            if (!propagate) {
                return;
            }
            if (!refocus || !rootViewRequestFocus()) {
                notifyGlobalFocusCleared(this);
            }
        }
    }

    void notifyGlobalFocusCleared(View oldFocus) {
        if (oldFocus != null && this.mAttachInfo != null) {
            this.mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(oldFocus, null);
        }
    }

    boolean rootViewRequestFocus() {
        View root = getRootView();
        return root != null ? root.requestFocus() : DBG;
    }

    void unFocus(View focused) {
        clearFocusInternal(focused, DBG, DBG);
    }

    @ExportedProperty(category = "focus")
    public boolean hasFocus() {
        return (this.mPrivateFlags & TEXT_DIRECTION_ANY_RTL) != 0 ? true : DBG;
    }

    public boolean hasFocusable() {
        boolean z = DBG;
        if (!isFocusableInTouchMode()) {
            for (ViewParent p = this.mParent; p instanceof ViewGroup; p = p.getParent()) {
                if (((ViewGroup) p).shouldBlockFocusForTouchscreen()) {
                    return DBG;
                }
            }
        }
        if ((this.mViewFlags & VISIBILITY_MASK) == 0) {
            z = isFocusable();
        }
        return z;
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (gainFocus) {
            sendAccessibilityEvent(STATUS_BAR_TRANSPARENT);
        } else {
            notifyViewAccessibilityStateChangedIfNeeded(VISIBLE);
        }
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (!gainFocus) {
            if (isPressed()) {
                setPressed(DBG);
            }
            if (!(imm == null || this.mAttachInfo == null || !this.mAttachInfo.mHasWindowFocus)) {
                imm.focusOut(this);
            }
            onFocusLost();
        } else if (!(imm == null || this.mAttachInfo == null || !this.mAttachInfo.mHasWindowFocus)) {
            imm.focusIn(this);
        }
        invalidate(true);
        ListenerInfo li = this.mListenerInfo;
        if (!(li == null || li.mOnFocusChangeListener == null)) {
            li.mOnFocusChangeListener.onFocusChange(this, gainFocus);
        }
        if (this.mAttachInfo != null) {
            this.mAttachInfo.mKeyDispatchState.reset(this);
        }
    }

    public void sendAccessibilityEvent(int eventType) {
        if (this.mAccessibilityDelegate != null) {
            this.mAccessibilityDelegate.sendAccessibilityEvent(this, eventType);
        } else {
            sendAccessibilityEventInternal(eventType);
        }
    }

    public void announceForAccessibility(CharSequence text) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && this.mParent != null) {
            AccessibilityEvent event = AccessibilityEvent.obtain((int) RECENT_APPS_VISIBLE);
            onInitializeAccessibilityEvent(event);
            event.getText().add(text);
            event.setContentDescription(null);
            this.mParent.requestSendAccessibilityEvent(this, event);
        }
    }

    public void sendAccessibilityEventInternal(int eventType) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            sendAccessibilityEventUnchecked(AccessibilityEvent.obtain(eventType));
        }
    }

    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        if (this.mAccessibilityDelegate != null) {
            this.mAccessibilityDelegate.sendAccessibilityEventUnchecked(this, event);
        } else {
            sendAccessibilityEventUncheckedInternal(event);
        }
    }

    public void sendAccessibilityEventUncheckedInternal(AccessibilityEvent event) {
        if (isShown()) {
            onInitializeAccessibilityEvent(event);
            if ((event.getEventType() & POPULATING_ACCESSIBILITY_EVENT_TYPES) != 0) {
                dispatchPopulateAccessibilityEvent(event);
            }
            if (getParent() != null) {
                getParent().requestSendAccessibilityEvent(this, event);
            }
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (this.mAccessibilityDelegate != null) {
            return this.mAccessibilityDelegate.dispatchPopulateAccessibilityEvent(this, event);
        }
        return dispatchPopulateAccessibilityEventInternal(event);
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return DBG;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (this.mAccessibilityDelegate != null) {
            this.mAccessibilityDelegate.onPopulateAccessibilityEvent(this, event);
        } else {
            onPopulateAccessibilityEventInternal(event);
        }
    }

    public void onPopulateAccessibilityEventInternal(AccessibilityEvent event) {
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        if (this.mAccessibilityDelegate != null) {
            this.mAccessibilityDelegate.onInitializeAccessibilityEvent(this, event);
        } else {
            onInitializeAccessibilityEventInternal(event);
        }
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        event.setSource(this);
        event.setClassName(getAccessibilityClassName());
        event.setPackageName(getContext().getPackageName());
        event.setEnabled(isEnabled());
        event.setContentDescription(this.mContentDescription);
        switch (event.getEventType()) {
            case STATUS_BAR_TRANSPARENT /*8*/:
                ArrayList<View> focusablesTempList = this.mAttachInfo != null ? this.mAttachInfo.mTempArrayList : new ArrayList();
                getRootView().addFocusables(focusablesTempList, TEXT_DIRECTION_ANY_RTL, VISIBLE);
                event.setItemCount(focusablesTempList.size());
                event.setCurrentItemIndex(focusablesTempList.indexOf(this));
                if (this.mAttachInfo != null) {
                    focusablesTempList.clear();
                }
            case SYSTEM_UI_FLAG_LIGHT_STATUS_BAR /*8192*/:
                CharSequence text = getIterableTextForAccessibility();
                if (text != null && text.length() > 0) {
                    event.setFromIndex(getAccessibilitySelectionStart());
                    event.setToIndex(getAccessibilitySelectionEnd());
                    event.setItemCount(text.length());
                }
            default:
        }
    }

    public AccessibilityNodeInfo createAccessibilityNodeInfo() {
        if (this.mAccessibilityDelegate != null) {
            return this.mAccessibilityDelegate.createAccessibilityNodeInfo(this);
        }
        return createAccessibilityNodeInfoInternal();
    }

    public AccessibilityNodeInfo createAccessibilityNodeInfoInternal() {
        AccessibilityNodeProvider provider = getAccessibilityNodeProvider();
        if (provider != null) {
            return provider.createAccessibilityNodeInfo(VIEW_IS_NOT_CHECKED);
        }
        AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain(this);
        onInitializeAccessibilityNodeInfo(info);
        return info;
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        if (this.mAccessibilityDelegate != null) {
            this.mAccessibilityDelegate.onInitializeAccessibilityNodeInfo(this, info);
        } else {
            onInitializeAccessibilityNodeInfoInternal(info);
        }
    }

    public void getBoundsOnScreen(Rect outRect) {
        getBoundsOnScreen(outRect, DBG);
    }

    public void getBoundsOnScreen(Rect outRect, boolean clipToParent) {
        if (this.mAttachInfo != null) {
            RectF position = this.mAttachInfo.mTmpTransformRect;
            position.set(0.0f, 0.0f, (float) (this.mRight - this.mLeft), (float) (this.mBottom - this.mTop));
            if (!hasIdentityMatrix()) {
                getMatrix().mapRect(position);
            }
            position.offset((float) this.mLeft, (float) this.mTop);
            ViewParent parent = this.mParent;
            while (parent instanceof View) {
                View parentView = (View) parent;
                position.offset((float) (-parentView.mScrollX), (float) (-parentView.mScrollY));
                if (clipToParent) {
                    position.left = Math.max(position.left, 0.0f);
                    position.top = Math.max(position.top, 0.0f);
                    position.right = Math.min(position.right, (float) parentView.getWidth());
                    position.bottom = Math.min(position.bottom, (float) parentView.getHeight());
                }
                if (!parentView.hasIdentityMatrix()) {
                    parentView.getMatrix().mapRect(position);
                }
                position.offset((float) parentView.mLeft, (float) parentView.mTop);
                parent = parentView.mParent;
            }
            if (parent instanceof ViewRootImpl) {
                position.offset(0.0f, (float) (-((ViewRootImpl) parent).mCurScrollY));
            }
            position.offset((float) this.mAttachInfo.mWindowLeft, (float) this.mAttachInfo.mWindowTop);
            outRect.set(Math.round(position.left), Math.round(position.top), Math.round(position.right), Math.round(position.bottom));
        }
    }

    public CharSequence getAccessibilityClassName() {
        return View.class.getName();
    }

    public void onProvideStructure(ViewStructure structure) {
        int id = this.mID;
        if (id <= 0 || (MEASURED_STATE_MASK & id) == 0 || (Spanned.SPAN_PRIORITY & id) == 0 || (Protocol.MAX_MESSAGE & id) == 0) {
            structure.setId(id, null, null, null);
        } else {
            String entry;
            String type;
            String resourcePackageName;
            try {
                Resources res = getResources();
                entry = res.getResourceEntryName(id);
                type = res.getResourceTypeName(id);
                resourcePackageName = res.getResourcePackageName(id);
            } catch (NotFoundException e) {
                resourcePackageName = null;
                type = null;
                entry = null;
            }
            structure.setId(id, resourcePackageName, type, entry);
        }
        structure.setDimens(this.mLeft, this.mTop, this.mScrollX, this.mScrollY, this.mRight - this.mLeft, this.mBottom - this.mTop);
        if (!hasIdentityMatrix()) {
            structure.setTransformation(getMatrix());
        }
        structure.setElevation(getZ());
        structure.setVisibility(getVisibility());
        structure.setEnabled(isEnabled());
        if (isClickable()) {
            structure.setClickable(true);
        }
        if (isFocusable()) {
            structure.setFocusable(true);
        }
        if (isFocused()) {
            structure.setFocused(true);
        }
        if (isAccessibilityFocused()) {
            structure.setAccessibilityFocused(true);
        }
        if (isSelected()) {
            structure.setSelected(true);
        }
        if (isActivated()) {
            structure.setActivated(true);
        }
        if (isLongClickable()) {
            structure.setLongClickable(true);
        }
        if (this instanceof Checkable) {
            structure.setCheckable(true);
            if (((Checkable) this).isChecked()) {
                structure.setChecked(true);
            }
        }
        if (isContextClickable()) {
            structure.setContextClickable(true);
        }
        structure.setClassName(getAccessibilityClassName().toString());
        structure.setContentDescription(getContentDescription());
    }

    public void onProvideVirtualStructure(ViewStructure structure) {
        AccessibilityNodeProvider provider = getAccessibilityNodeProvider();
        if (provider != null) {
            AccessibilityNodeInfo info = createAccessibilityNodeInfo();
            structure.setChildCount(VIEW_IS_NOT_ADVIEW_OR_DISABLE);
            populateVirtualStructure(structure.newChild(VISIBLE), provider, info);
            info.recycle();
        }
    }

    private void populateVirtualStructure(ViewStructure structure, AccessibilityNodeProvider provider, AccessibilityNodeInfo info) {
        structure.setId(AccessibilityNodeInfo.getVirtualDescendantId(info.getSourceNodeId()), null, null, null);
        Rect rect = structure.getTempRect();
        info.getBoundsInParent(rect);
        structure.setDimens(rect.left, rect.top, VISIBLE, VISIBLE, rect.width(), rect.height());
        structure.setVisibility(VISIBLE);
        structure.setEnabled(info.isEnabled());
        if (info.isClickable()) {
            structure.setClickable(true);
        }
        if (info.isFocusable()) {
            structure.setFocusable(true);
        }
        if (info.isFocused()) {
            structure.setFocused(true);
        }
        if (info.isAccessibilityFocused()) {
            structure.setAccessibilityFocused(true);
        }
        if (info.isSelected()) {
            structure.setSelected(true);
        }
        if (info.isLongClickable()) {
            structure.setLongClickable(true);
        }
        if (info.isCheckable()) {
            structure.setCheckable(true);
            if (info.isChecked()) {
                structure.setChecked(true);
            }
        }
        if (info.isContextClickable()) {
            structure.setContextClickable(true);
        }
        CharSequence cname = info.getClassName();
        structure.setClassName(cname != null ? cname.toString() : null);
        structure.setContentDescription(info.getContentDescription());
        if (!(info.getText() == null && info.getError() == null)) {
            structure.setText(info.getText(), info.getTextSelectionStart(), info.getTextSelectionEnd());
        }
        int NCHILDREN = info.getChildCount();
        if (NCHILDREN > 0) {
            structure.setChildCount(NCHILDREN);
            for (int i = VISIBLE; i < NCHILDREN; i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                AccessibilityNodeInfo cinfo = provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(info.getChildId(i)));
                populateVirtualStructure(structure.newChild(i), provider, cinfo);
                cinfo.recycle();
            }
        }
    }

    public void dispatchProvideStructure(ViewStructure structure) {
        if (isAssistBlocked()) {
            structure.setClassName(getAccessibilityClassName().toString());
            structure.setAssistBlocked(true);
            return;
        }
        onProvideStructure(structure);
        onProvideVirtualStructure(structure);
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        if (this.mAttachInfo != null) {
            View rootView;
            View next;
            Rect bounds = this.mAttachInfo.mTmpInvalRect;
            getDrawingRect(bounds);
            info.setBoundsInParent(bounds);
            getBoundsOnScreen(bounds, true);
            info.setBoundsInScreen(bounds);
            ViewParent parent = getParentForAccessibility();
            if (parent instanceof View) {
                info.setParent((View) parent);
            }
            if (this.mID != VIEW_IS_NOT_CHECKED) {
                rootView = getRootView();
                if (rootView == null) {
                    rootView = this;
                }
                View label = rootView.findLabelForView(this, this.mID);
                if (label != null) {
                    info.setLabeledBy(label);
                }
                if (!(this.mAttachInfo == null || (this.mAttachInfo.mAccessibilityFetchFlags & SCROLL_INDICATOR_START) == 0 || !Resources.resourceHasPackage(this.mID))) {
                    try {
                        info.setViewIdResourceName(getResources().getResourceName(this.mID));
                    } catch (NotFoundException e) {
                    }
                }
            }
            if (this.mLabelForId != VIEW_IS_NOT_CHECKED) {
                rootView = getRootView();
                if (rootView == null) {
                    rootView = this;
                }
                View labeled = rootView.findViewInsideOutShouldExist(this, this.mLabelForId);
                if (labeled != null) {
                    info.setLabelFor(labeled);
                }
            }
            if (this.mAccessibilityTraversalBeforeId != VIEW_IS_NOT_CHECKED) {
                rootView = getRootView();
                if (rootView == null) {
                    rootView = this;
                }
                next = rootView.findViewInsideOutShouldExist(this, this.mAccessibilityTraversalBeforeId);
                if (next != null && next.includeForAccessibility()) {
                    info.setTraversalBefore(next);
                }
            }
            if (this.mAccessibilityTraversalAfterId != VIEW_IS_NOT_CHECKED) {
                rootView = getRootView();
                if (rootView == null) {
                    rootView = this;
                }
                next = rootView.findViewInsideOutShouldExist(this, this.mAccessibilityTraversalAfterId);
                if (next != null && next.includeForAccessibility()) {
                    info.setTraversalAfter(next);
                }
            }
            info.setVisibleToUser(isVisibleToUser());
            if (this.mAttachInfo == null || (this.mAttachInfo.mAccessibilityFetchFlags & STATUS_BAR_TRANSPARENT) == 0) {
                info.setImportantForAccessibility(true);
            } else {
                info.setImportantForAccessibility(isImportantForAccessibility());
            }
            info.setPackageName(this.mContext.getPackageName());
            info.setClassName(getAccessibilityClassName());
            info.setContentDescription(getContentDescription());
            info.setEnabled(isEnabled());
            info.setClickable(isClickable());
            info.setFocusable(isFocusable());
            info.setFocused(isFocused());
            info.setAccessibilityFocused(isAccessibilityFocused());
            info.setSelected(isSelected());
            info.setLongClickable(isLongClickable());
            info.setContextClickable(isContextClickable());
            info.setLiveRegion(getAccessibilityLiveRegion());
            info.addAction((int) TEXT_DIRECTION_RTL);
            info.addAction((int) STATUS_BAR_TRANSPARENT);
            if (isFocusable()) {
                if (isFocused()) {
                    info.addAction((int) TEXT_DIRECTION_ANY_RTL);
                } else {
                    info.addAction((int) VIEW_IS_NOT_ADVIEW_OR_DISABLE);
                }
            }
            if (isAccessibilityFocused()) {
                info.addAction((int) WILL_NOT_DRAW);
            } else {
                info.addAction((int) PFLAG_DRAW_ANIMATION);
            }
            if (isClickable() && isEnabled()) {
                info.addAction((int) SCROLL_INDICATOR_START);
            }
            if (isLongClickable() && isEnabled()) {
                info.addAction((int) SCROLL_INDICATOR_END);
            }
            if (isContextClickable() && isEnabled()) {
                info.addAction(AccessibilityAction.ACTION_CONTEXT_CLICK);
            }
            CharSequence text = getIterableTextForAccessibility();
            if (text != null && text.length() > 0) {
                info.setTextSelection(getAccessibilitySelectionStart(), getAccessibilitySelectionEnd());
                info.addAction((int) WILL_NOT_CACHE_DRAWING);
                info.addAction((int) SYSTEM_UI_FLAG_LAYOUT_STABLE);
                info.addAction((int) SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                info.setMovementGranularities(11);
            }
            info.addAction(AccessibilityAction.ACTION_SHOW_ON_SCREEN);
            populateAccessibilityNodeInfoDrawingOrderInParent(info);
        }
    }

    private void populateAccessibilityNodeInfoDrawingOrderInParent(AccessibilityNodeInfo info) {
        if ((this.mPrivateFlags & SCROLL_INDICATOR_START) == 0) {
            info.setDrawingOrder(VISIBLE);
            return;
        }
        int drawingOrderInParent = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        View viewAtDrawingLevel = this;
        View parent = getParentForAccessibility();
        while (viewAtDrawingLevel != parent) {
            ViewParent currentParent = viewAtDrawingLevel.getParent();
            if (!(currentParent instanceof ViewGroup)) {
                drawingOrderInParent = VISIBLE;
                break;
            }
            ViewGroup parentGroup = (ViewGroup) currentParent;
            int childCount = parentGroup.getChildCount();
            if (childCount > VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                List<View> preorderedList = parentGroup.buildOrderedChildList();
                int childDrawIndex;
                int i;
                if (preorderedList != null) {
                    childDrawIndex = preorderedList.indexOf(viewAtDrawingLevel);
                    for (i = VISIBLE; i < childDrawIndex; i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                        drawingOrderInParent += numViewsForAccessibility((View) preorderedList.get(i));
                    }
                } else {
                    int childIndex = parentGroup.indexOfChild(viewAtDrawingLevel);
                    boolean customOrder = parentGroup.isChildrenDrawingOrderEnabled();
                    if (childIndex < 0 || !customOrder) {
                        childDrawIndex = childIndex;
                    } else {
                        childDrawIndex = parentGroup.getChildDrawingOrder(childCount, childIndex);
                    }
                    int numChildrenToIterate = customOrder ? childCount : childDrawIndex;
                    if (childDrawIndex != 0) {
                        for (i = VISIBLE; i < numChildrenToIterate; i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                            int otherDrawIndex;
                            if (customOrder) {
                                otherDrawIndex = parentGroup.getChildDrawingOrder(childCount, i);
                            } else {
                                otherDrawIndex = i;
                            }
                            if (otherDrawIndex < childDrawIndex) {
                                drawingOrderInParent += numViewsForAccessibility(parentGroup.getChildAt(i));
                            }
                        }
                    }
                }
            }
            viewAtDrawingLevel = (View) currentParent;
        }
        info.setDrawingOrder(drawingOrderInParent);
    }

    private static int numViewsForAccessibility(View view) {
        if (view != null) {
            if (view.includeForAccessibility()) {
                return VIEW_IS_NOT_ADVIEW_OR_DISABLE;
            }
            if (view instanceof ViewGroup) {
                return ((ViewGroup) view).getNumChildrenForAccessibility();
            }
        }
        return VISIBLE;
    }

    private View findLabelForView(View view, int labeledId) {
        if (this.mMatchLabelForPredicate == null) {
            this.mMatchLabelForPredicate = new MatchLabelForPredicate();
        }
        this.mMatchLabelForPredicate.mLabeledId = labeledId;
        return findViewByPredicateInsideOut(view, this.mMatchLabelForPredicate);
    }

    protected boolean isVisibleToUser() {
        return isVisibleToUser(null);
    }

    protected boolean isVisibleToUser(Rect boundInView) {
        if (this.mAttachInfo == null || this.mAttachInfo.mWindowVisibility != 0) {
            return DBG;
        }
        View current = this;
        while (current instanceof View) {
            View view = current;
            if (view.getAlpha() <= 0.0f || view.getTransitionAlpha() <= 0.0f || view.getVisibility() != 0) {
                return DBG;
            }
            current = view.mParent;
        }
        Rect visibleRect = this.mAttachInfo.mTmpInvalRect;
        Point offset = this.mAttachInfo.mPoint;
        if (!getGlobalVisibleRect(visibleRect, offset)) {
            return DBG;
        }
        if (boundInView == null) {
            return true;
        }
        visibleRect.offset(-offset.x, -offset.y);
        return boundInView.intersect(visibleRect);
    }

    public AccessibilityDelegate getAccessibilityDelegate() {
        return this.mAccessibilityDelegate;
    }

    public void setAccessibilityDelegate(AccessibilityDelegate delegate) {
        this.mAccessibilityDelegate = delegate;
    }

    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        if (this.mAccessibilityDelegate != null) {
            return this.mAccessibilityDelegate.getAccessibilityNodeProvider(this);
        }
        return null;
    }

    public int getAccessibilityViewId() {
        if (this.mAccessibilityViewId == VIEW_IS_NOT_CHECKED) {
            int i = sNextAccessibilityViewId;
            sNextAccessibilityViewId = i + VIEW_IS_NOT_ADVIEW_OR_DISABLE;
            this.mAccessibilityViewId = i;
        }
        return this.mAccessibilityViewId;
    }

    public int getAccessibilityWindowId() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mAccessibilityWindowId;
        }
        return HwBootFail.STAGE_BOOT_SUCCESS;
    }

    @ExportedProperty(category = "accessibility")
    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    @RemotableViewMethod
    public void setContentDescription(CharSequence contentDescription) {
        boolean nonEmptyDesc = DBG;
        if (this.mContentDescription == null) {
            if (contentDescription == null) {
                return;
            }
        } else if (this.mContentDescription.equals(contentDescription)) {
            return;
        }
        this.mContentDescription = contentDescription;
        if (contentDescription != null && contentDescription.length() > 0) {
            nonEmptyDesc = true;
        }
        if (nonEmptyDesc && getImportantForAccessibility() == 0) {
            setImportantForAccessibility(VIEW_IS_NOT_ADVIEW_OR_DISABLE);
            notifySubtreeAccessibilityStateChangedIfNeeded();
        } else {
            notifyViewAccessibilityStateChangedIfNeeded(TEXT_DIRECTION_RTL);
        }
    }

    @RemotableViewMethod
    public void setAccessibilityTraversalBefore(int beforeId) {
        if (this.mAccessibilityTraversalBeforeId != beforeId) {
            this.mAccessibilityTraversalBeforeId = beforeId;
            notifyViewAccessibilityStateChangedIfNeeded(VISIBLE);
        }
    }

    public int getAccessibilityTraversalBefore() {
        return this.mAccessibilityTraversalBeforeId;
    }

    @RemotableViewMethod
    public void setAccessibilityTraversalAfter(int afterId) {
        if (this.mAccessibilityTraversalAfterId != afterId) {
            this.mAccessibilityTraversalAfterId = afterId;
            notifyViewAccessibilityStateChangedIfNeeded(VISIBLE);
        }
    }

    public int getAccessibilityTraversalAfter() {
        return this.mAccessibilityTraversalAfterId;
    }

    @ExportedProperty(category = "accessibility")
    public int getLabelFor() {
        return this.mLabelForId;
    }

    @RemotableViewMethod
    public void setLabelFor(int id) {
        if (this.mLabelForId != id) {
            this.mLabelForId = id;
            if (this.mLabelForId != VIEW_IS_NOT_CHECKED && this.mID == VIEW_IS_NOT_CHECKED) {
                this.mID = generateViewId();
            }
            notifyViewAccessibilityStateChangedIfNeeded(VISIBLE);
        }
    }

    protected void onFocusLost() {
        resetPressedState();
    }

    private void resetPressedState() {
        if ((this.mViewFlags & SCROLL_INDICATOR_END) != SCROLL_INDICATOR_END && isPressed()) {
            setPressed(DBG);
            if (!this.mHasPerformedLongPress) {
                removeLongPressCallback();
            }
        }
    }

    @ExportedProperty(category = "focus")
    public boolean isFocused() {
        return (this.mPrivateFlags & TEXT_DIRECTION_ANY_RTL) != 0 ? true : DBG;
    }

    public View findFocus() {
        return (this.mPrivateFlags & TEXT_DIRECTION_ANY_RTL) != 0 ? this : null;
    }

    public boolean isScrollContainer() {
        return (this.mPrivateFlags & STATUS_BAR_DISABLE_SYSTEM_INFO) != 0 ? true : DBG;
    }

    public void setScrollContainer(boolean isScrollContainer) {
        if (isScrollContainer) {
            if (this.mAttachInfo != null && (this.mPrivateFlags & STATUS_BAR_DISABLE_SYSTEM_INFO) == 0) {
                this.mAttachInfo.mScrollContainers.add(this);
                this.mPrivateFlags |= STATUS_BAR_DISABLE_SYSTEM_INFO;
            }
            this.mPrivateFlags |= STATUS_BAR_DISABLE_NOTIFICATION_TICKER;
            return;
        }
        if ((this.mPrivateFlags & STATUS_BAR_DISABLE_SYSTEM_INFO) != 0) {
            this.mAttachInfo.mScrollContainers.remove(this);
        }
        this.mPrivateFlags &= -1572865;
    }

    public int getDrawingCacheQuality() {
        return this.mViewFlags & DRAWING_CACHE_QUALITY_MASK;
    }

    public void setDrawingCacheQuality(int quality) {
        setFlags(quality, DRAWING_CACHE_QUALITY_MASK);
    }

    public boolean getKeepScreenOn() {
        return (this.mViewFlags & STATUS_BAR_TRANSIENT) != 0 ? true : DBG;
    }

    public void setKeepScreenOn(boolean keepScreenOn) {
        setFlags(keepScreenOn ? STATUS_BAR_TRANSIENT : VISIBLE, STATUS_BAR_TRANSIENT);
    }

    public int getNextFocusLeftId() {
        return this.mNextFocusLeftId;
    }

    public void setNextFocusLeftId(int nextFocusLeftId) {
        this.mNextFocusLeftId = nextFocusLeftId;
    }

    public int getNextFocusRightId() {
        return this.mNextFocusRightId;
    }

    public void setNextFocusRightId(int nextFocusRightId) {
        this.mNextFocusRightId = nextFocusRightId;
    }

    public int getNextFocusUpId() {
        return this.mNextFocusUpId;
    }

    public void setNextFocusUpId(int nextFocusUpId) {
        this.mNextFocusUpId = nextFocusUpId;
    }

    public int getNextFocusDownId() {
        return this.mNextFocusDownId;
    }

    public void setNextFocusDownId(int nextFocusDownId) {
        this.mNextFocusDownId = nextFocusDownId;
    }

    public int getNextFocusForwardId() {
        return this.mNextFocusForwardId;
    }

    public void setNextFocusForwardId(int nextFocusForwardId) {
        this.mNextFocusForwardId = nextFocusForwardId;
    }

    public boolean isShown() {
        View current = this;
        while ((current.mViewFlags & VISIBILITY_MASK) == 0) {
            ViewParent parent = current.mParent;
            if (parent == null) {
                return DBG;
            }
            if (!(parent instanceof View)) {
                return true;
            }
            current = (View) parent;
            if (current == null) {
                return DBG;
            }
        }
        return DBG;
    }

    protected boolean fitSystemWindows(Rect insets) {
        if ((this.mPrivateFlags3 & SCROLL_INDICATOR_END) != 0) {
            return fitSystemWindowsInt(insets);
        }
        if (insets == null) {
            return DBG;
        }
        try {
            this.mPrivateFlags3 |= PFLAG_DRAW_ANIMATION;
            boolean isConsumed = dispatchApplyWindowInsets(new WindowInsets(insets)).isConsumed();
            return isConsumed;
        } finally {
            this.mPrivateFlags3 &= -65;
        }
    }

    private boolean fitSystemWindowsInt(Rect insets) {
        if ((this.mViewFlags & TEXT_DIRECTION_ANY_RTL) != TEXT_DIRECTION_ANY_RTL) {
            return DBG;
        }
        this.mUserPaddingStart = UNDEFINED_PADDING;
        this.mUserPaddingEnd = UNDEFINED_PADDING;
        Rect localInsets = (Rect) sThreadLocal.get();
        if (localInsets == null) {
            localInsets = new Rect();
            sThreadLocal.set(localInsets);
        }
        boolean res = computeFitSystemWindows(insets, localInsets);
        this.mUserPaddingLeftInitial = localInsets.left;
        this.mUserPaddingRightInitial = localInsets.right;
        internalSetPadding(localInsets.left, localInsets.top, localInsets.right, localInsets.bottom);
        return res;
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if ((this.mPrivateFlags3 & PFLAG_DRAW_ANIMATION) == 0) {
            if (fitSystemWindows(insets.getSystemWindowInsets())) {
                return insets.consumeSystemWindowInsets();
            }
        } else if (fitSystemWindowsInt(insets.getSystemWindowInsets())) {
            return insets.consumeSystemWindowInsets();
        }
        return insets;
    }

    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {
        getListenerInfo().mOnApplyWindowInsetsListener = listener;
    }

    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        try {
            this.mPrivateFlags3 |= SCROLL_INDICATOR_END;
            WindowInsets onApplyWindowInsets;
            if (this.mListenerInfo == null || this.mListenerInfo.mOnApplyWindowInsetsListener == null) {
                onApplyWindowInsets = onApplyWindowInsets(insets);
                this.mPrivateFlags3 &= -33;
                return onApplyWindowInsets;
            }
            onApplyWindowInsets = this.mListenerInfo.mOnApplyWindowInsetsListener.onApplyWindowInsets(this, insets);
            return onApplyWindowInsets;
        } finally {
            this.mPrivateFlags3 &= -33;
        }
    }

    public void getLocationInSurface(int[] location) {
        getLocationInWindow(location);
        if (this.mAttachInfo != null && this.mAttachInfo.mViewRootImpl != null) {
            location[VISIBLE] = location[VISIBLE] + this.mAttachInfo.mViewRootImpl.mWindowAttributes.surfaceInsets.left;
            location[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = location[VIEW_IS_NOT_ADVIEW_OR_DISABLE] + this.mAttachInfo.mViewRootImpl.mWindowAttributes.surfaceInsets.top;
        }
    }

    public WindowInsets getRootWindowInsets() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mViewRootImpl.getWindowInsets(DBG);
        }
        return null;
    }

    protected boolean computeFitSystemWindows(Rect inoutInsets, Rect outLocalInsets) {
        if ((this.mViewFlags & SYSTEM_UI_FLAG_IMMERSIVE) == 0 || this.mAttachInfo == null || ((this.mAttachInfo.mSystemUiVisibility & SYSTEM_UI_LAYOUT_FLAGS) == 0 && !this.mAttachInfo.mOverscanRequested)) {
            outLocalInsets.set(inoutInsets);
            inoutInsets.set(VISIBLE, VISIBLE, VISIBLE, VISIBLE);
            return true;
        }
        Rect overscan = this.mAttachInfo.mOverscanInsets;
        outLocalInsets.set(overscan);
        inoutInsets.left -= overscan.left;
        inoutInsets.top -= overscan.top;
        inoutInsets.right -= overscan.right;
        inoutInsets.bottom -= overscan.bottom;
        return DBG;
    }

    public WindowInsets computeSystemWindowInsets(WindowInsets in, Rect outLocalInsets) {
        if ((this.mViewFlags & SYSTEM_UI_FLAG_IMMERSIVE) == 0 || this.mAttachInfo == null || (this.mAttachInfo.mSystemUiVisibility & SYSTEM_UI_LAYOUT_FLAGS) == 0) {
            outLocalInsets.set(in.getSystemWindowInsets());
            return in.consumeSystemWindowInsets();
        }
        outLocalInsets.set(VISIBLE, VISIBLE, VISIBLE, VISIBLE);
        return in;
    }

    public void setFitsSystemWindows(boolean fitSystemWindows) {
        setFlags(fitSystemWindows ? TEXT_DIRECTION_ANY_RTL : VISIBLE, TEXT_DIRECTION_ANY_RTL);
    }

    @ExportedProperty
    public boolean getFitsSystemWindows() {
        return (this.mViewFlags & TEXT_DIRECTION_ANY_RTL) == TEXT_DIRECTION_ANY_RTL ? true : DBG;
    }

    public boolean fitsSystemWindows() {
        return getFitsSystemWindows();
    }

    public void requestFitSystemWindows() {
        if (this.mParent != null) {
            this.mParent.requestFitSystemWindows();
        }
    }

    public void requestApplyInsets() {
        requestFitSystemWindows();
    }

    public void makeOptionalFitsSystemWindows() {
        setFlags(SYSTEM_UI_FLAG_IMMERSIVE, SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public void getOutsets(Rect outOutsetRect) {
        if (this.mAttachInfo != null) {
            outOutsetRect.set(this.mAttachInfo.mOutsets);
        } else {
            outOutsetRect.setEmpty();
        }
    }

    @ExportedProperty(mapping = {@IntToString(from = 0, to = "VISIBLE"), @IntToString(from = 4, to = "INVISIBLE"), @IntToString(from = 8, to = "GONE")})
    public int getVisibility() {
        return this.mViewFlags & VISIBILITY_MASK;
    }

    @RemotableViewMethod
    public void setVisibility(int visibility) {
        setFlags(visibility, VISIBILITY_MASK);
    }

    @ExportedProperty
    public boolean isEnabled() {
        return (this.mViewFlags & SCROLL_INDICATOR_END) == 0 ? true : DBG;
    }

    @RemotableViewMethod
    public void setEnabled(boolean enabled) {
        if (enabled != isEnabled()) {
            int i;
            if (enabled) {
                i = VISIBLE;
            } else {
                i = SCROLL_INDICATOR_END;
            }
            setFlags(i, SCROLL_INDICATOR_END);
            refreshDrawableState();
            invalidate(true);
            if (!enabled) {
                cancelPendingInputEvents();
            }
        }
    }

    public void setFocusable(boolean focusable) {
        int i = VISIBLE;
        if (!focusable) {
            setFlags(VISIBLE, STATUS_BAR_DISABLE_NOTIFICATION_ALERTS);
        }
        if (focusable) {
            i = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
        setFlags(i, VIEW_IS_NOT_ADVIEW_OR_DISABLE);
    }

    public void setFocusableInTouchMode(boolean focusableInTouchMode) {
        setFlags(focusableInTouchMode ? STATUS_BAR_DISABLE_NOTIFICATION_ALERTS : VISIBLE, STATUS_BAR_DISABLE_NOTIFICATION_ALERTS);
        if (focusableInTouchMode) {
            setFlags(VIEW_IS_NOT_ADVIEW_OR_DISABLE, VIEW_IS_NOT_ADVIEW_OR_DISABLE);
        }
    }

    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        setFlags(soundEffectsEnabled ? SOUND_EFFECTS_ENABLED : VISIBLE, SOUND_EFFECTS_ENABLED);
    }

    @ExportedProperty
    public boolean isSoundEffectsEnabled() {
        return SOUND_EFFECTS_ENABLED == (this.mViewFlags & SOUND_EFFECTS_ENABLED) ? true : DBG;
    }

    public void setHapticFeedbackEnabled(boolean hapticFeedbackEnabled) {
        setFlags(hapticFeedbackEnabled ? STATUS_BAR_UNHIDE : VISIBLE, STATUS_BAR_UNHIDE);
    }

    @ExportedProperty
    public boolean isHapticFeedbackEnabled() {
        return STATUS_BAR_UNHIDE == (this.mViewFlags & STATUS_BAR_UNHIDE) ? true : DBG;
    }

    @ExportedProperty(category = "layout", mapping = {@IntToString(from = 0, to = "LTR"), @IntToString(from = 1, to = "RTL"), @IntToString(from = 2, to = "INHERIT"), @IntToString(from = 3, to = "LOCALE")})
    public int getRawLayoutDirection() {
        return (this.mPrivateFlags2 & VISIBILITY_MASK) >> TEXT_DIRECTION_ANY_RTL;
    }

    @RemotableViewMethod
    public void setLayoutDirection(int layoutDirection) {
        if (getRawLayoutDirection() != layoutDirection) {
            this.mPrivateFlags2 &= -13;
            resetRtlProperties();
            this.mPrivateFlags2 |= (layoutDirection << TEXT_DIRECTION_ANY_RTL) & VISIBILITY_MASK;
            resolveRtlPropertiesIfNeeded();
            requestLayout();
            invalidate(true);
        }
    }

    @ExportedProperty(category = "layout", mapping = {@IntToString(from = 0, to = "RESOLVED_DIRECTION_LTR"), @IntToString(from = 1, to = "RESOLVED_DIRECTION_RTL")})
    public int getLayoutDirection() {
        int i = VISIBLE;
        if (getContext().getApplicationInfo().targetSdkVersion >= PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK_SHIFT || this.mForceRTL) {
            if ((this.mPrivateFlags2 & SCROLL_INDICATOR_START) == SCROLL_INDICATOR_START) {
                i = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
            }
            return i;
        }
        this.mPrivateFlags2 |= SCROLL_INDICATOR_END;
        return VISIBLE;
    }

    @ExportedProperty(category = "layout")
    public boolean isLayoutRtl() {
        return getLayoutDirection() == VIEW_IS_NOT_ADVIEW_OR_DISABLE ? true : DBG;
    }

    public boolean isRtlLocale() {
        String currentLang = Locale.getDefault().getLanguage();
        return (currentLang.contains("ar") || currentLang.contains("fa") || currentLang.contains("iw") || currentLang.contains("ug") || currentLang.contains("ur")) ? true : isLayoutRtl();
    }

    @ExportedProperty(category = "layout")
    public boolean hasTransientState() {
        return (this.mPrivateFlags2 & UNDEFINED_PADDING) == UNDEFINED_PADDING ? true : DBG;
    }

    public void setHasTransientState(boolean hasTransientState) {
        int i;
        if (hasTransientState) {
            i = this.mTransientStateCount + VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        } else {
            i = this.mTransientStateCount + VIEW_IS_NOT_CHECKED;
        }
        this.mTransientStateCount = i;
        if (this.mTransientStateCount < 0) {
            this.mTransientStateCount = VISIBLE;
            Log.e(VIEW_LOG_TAG, "hasTransientState decremented below 0: unmatched pair of setHasTransientState calls");
            return;
        }
        if (!(hasTransientState && this.mTransientStateCount == VIEW_IS_NOT_ADVIEW_OR_DISABLE)) {
            if (hasTransientState || this.mTransientStateCount != 0) {
                return;
            }
        }
        int i2 = HwBootFail.STAGE_BOOT_SUCCESS & this.mPrivateFlags2;
        if (hasTransientState) {
            i = UNDEFINED_PADDING;
        } else {
            i = VISIBLE;
        }
        this.mPrivateFlags2 = i | i2;
        if (this.mParent != null) {
            try {
                this.mParent.childHasTransientStateChanged(this, hasTransientState);
            } catch (AbstractMethodError e) {
                Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
            }
        }
    }

    public boolean isAttachedToWindow() {
        return this.mAttachInfo != null ? true : DBG;
    }

    public boolean isLaidOut() {
        return (this.mPrivateFlags3 & TEXT_DIRECTION_RTL) == TEXT_DIRECTION_RTL ? true : DBG;
    }

    public void setWillNotDraw(boolean willNotDraw) {
        setFlags(willNotDraw ? WILL_NOT_DRAW : VISIBLE, WILL_NOT_DRAW);
    }

    @ExportedProperty(category = "drawing")
    public boolean willNotDraw() {
        return (this.mViewFlags & WILL_NOT_DRAW) == WILL_NOT_DRAW ? true : DBG;
    }

    public void setWillNotCacheDrawing(boolean willNotCacheDrawing) {
        setFlags(willNotCacheDrawing ? WILL_NOT_CACHE_DRAWING : VISIBLE, WILL_NOT_CACHE_DRAWING);
    }

    @ExportedProperty(category = "drawing")
    public boolean willNotCacheDrawing() {
        return (this.mViewFlags & WILL_NOT_CACHE_DRAWING) == WILL_NOT_CACHE_DRAWING ? true : DBG;
    }

    @ExportedProperty
    public boolean isClickable() {
        return (this.mViewFlags & RECENT_APPS_VISIBLE) == RECENT_APPS_VISIBLE ? true : DBG;
    }

    public void setClickable(boolean clickable) {
        setFlags(clickable ? RECENT_APPS_VISIBLE : VISIBLE, RECENT_APPS_VISIBLE);
    }

    public boolean isLongClickable() {
        return (this.mViewFlags & STATUS_BAR_DISABLE_HOME) == STATUS_BAR_DISABLE_HOME ? true : DBG;
    }

    public void setLongClickable(boolean longClickable) {
        setFlags(longClickable ? STATUS_BAR_DISABLE_HOME : VISIBLE, STATUS_BAR_DISABLE_HOME);
    }

    public boolean isContextClickable() {
        return (this.mViewFlags & STATUS_BAR_DISABLE_CLOCK) == STATUS_BAR_DISABLE_CLOCK ? true : DBG;
    }

    public void setContextClickable(boolean contextClickable) {
        setFlags(contextClickable ? STATUS_BAR_DISABLE_CLOCK : VISIBLE, STATUS_BAR_DISABLE_CLOCK);
    }

    private void setPressed(boolean pressed, float x, float y) {
        if (pressed) {
            drawableHotspotChanged(x, y);
        }
        setPressed(pressed);
    }

    public void setPressed(boolean pressed) {
        boolean needsRefresh = pressed != ((this.mPrivateFlags & RECENT_APPS_VISIBLE) == RECENT_APPS_VISIBLE ? true : DBG) ? true : DBG;
        if (pressed) {
            this.mPrivateFlags |= RECENT_APPS_VISIBLE;
        } else {
            this.mPrivateFlags &= -16385;
        }
        if (needsRefresh) {
            refreshDrawableState();
        }
        dispatchSetPressed(pressed);
    }

    protected void dispatchSetPressed(boolean pressed) {
    }

    @ExportedProperty
    public boolean isPressed() {
        return (this.mPrivateFlags & RECENT_APPS_VISIBLE) == RECENT_APPS_VISIBLE ? true : DBG;
    }

    public boolean isAssistBlocked() {
        return (this.mPrivateFlags3 & RECENT_APPS_VISIBLE) != 0 ? true : DBG;
    }

    public void setAssistBlocked(boolean enabled) {
        if (enabled) {
            this.mPrivateFlags3 |= RECENT_APPS_VISIBLE;
        } else {
            this.mPrivateFlags3 &= -16385;
        }
    }

    public boolean isSaveEnabled() {
        return (this.mViewFlags & TV_PICTURE_IN_PICTURE_VISIBLE) != TV_PICTURE_IN_PICTURE_VISIBLE ? true : DBG;
    }

    public void setSaveEnabled(boolean enabled) {
        int i;
        if (enabled) {
            i = VISIBLE;
        } else {
            i = TV_PICTURE_IN_PICTURE_VISIBLE;
        }
        setFlags(i, TV_PICTURE_IN_PICTURE_VISIBLE);
    }

    @ExportedProperty
    public boolean getFilterTouchesWhenObscured() {
        return (this.mViewFlags & SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) != 0 ? true : DBG;
    }

    public void setFilterTouchesWhenObscured(boolean enabled) {
        setFlags(enabled ? SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN : VISIBLE, SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public boolean isSaveFromParentEnabled() {
        return (this.mViewFlags & PFLAG_DOES_NOTHING_REUSE_PLEASE) != PFLAG_DOES_NOTHING_REUSE_PLEASE ? true : DBG;
    }

    public void setSaveFromParentEnabled(boolean enabled) {
        int i;
        if (enabled) {
            i = VISIBLE;
        } else {
            i = PFLAG_DOES_NOTHING_REUSE_PLEASE;
        }
        setFlags(i, PFLAG_DOES_NOTHING_REUSE_PLEASE);
    }

    @ExportedProperty(category = "focus")
    public final boolean isFocusable() {
        return VIEW_IS_NOT_ADVIEW_OR_DISABLE == (this.mViewFlags & VIEW_IS_NOT_ADVIEW_OR_DISABLE) ? true : DBG;
    }

    @ExportedProperty
    public final boolean isFocusableInTouchMode() {
        return STATUS_BAR_DISABLE_NOTIFICATION_ALERTS == (this.mViewFlags & STATUS_BAR_DISABLE_NOTIFICATION_ALERTS) ? true : DBG;
    }

    public View focusSearch(int direction) {
        if (this.mParent != null) {
            return this.mParent.focusSearch(this, direction);
        }
        return null;
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return DBG;
    }

    View findUserSetNextFocus(View root, int direction) {
        switch (direction) {
            case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                return this.mID == VIEW_IS_NOT_CHECKED ? null : root.findViewByPredicateInsideOut(this, new AnonymousClass13(this.mID));
            case TEXT_DIRECTION_ANY_RTL /*2*/:
                if (this.mNextFocusForwardId == VIEW_IS_NOT_CHECKED) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextFocusForwardId);
            case PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK_SHIFT /*17*/:
                if (this.mNextFocusLeftId == VIEW_IS_NOT_CHECKED) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextFocusLeftId);
            case FOCUS_UP /*33*/:
                if (this.mNextFocusUpId == VIEW_IS_NOT_CHECKED) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextFocusUpId);
            case FOCUS_RIGHT /*66*/:
                if (this.mNextFocusRightId == VIEW_IS_NOT_CHECKED) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextFocusRightId);
            case FOCUS_DOWN /*130*/:
                if (this.mNextFocusDownId == VIEW_IS_NOT_CHECKED) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextFocusDownId);
            default:
                return null;
        }
    }

    private View findViewInsideOutShouldExist(View root, int id) {
        if (this.mMatchIdPredicate == null) {
            this.mMatchIdPredicate = new MatchIdPredicate();
        }
        this.mMatchIdPredicate.mId = id;
        View result = root.findViewByPredicateInsideOut(this, this.mMatchIdPredicate);
        if (result == null) {
            Log.w(VIEW_LOG_TAG, "couldn't find view with id " + id);
        }
        return result;
    }

    public ArrayList<View> getFocusables(int direction) {
        ArrayList<View> result = new ArrayList(24);
        addFocusables(result, direction);
        return result;
    }

    public void addFocusables(ArrayList<View> views, int direction) {
        addFocusables(views, direction, isInTouchMode() ? VIEW_IS_NOT_ADVIEW_OR_DISABLE : VISIBLE);
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (views == null || !isFocusable()) {
            return;
        }
        if (((focusableMode & VIEW_IS_NOT_ADVIEW_OR_DISABLE) != VIEW_IS_NOT_ADVIEW_OR_DISABLE || isFocusableInTouchMode()) && !hasAncestorThatBlocksDescendantFocus()) {
            views.add(this);
        }
    }

    public void findViewsWithText(ArrayList<View> outViews, CharSequence searched, int flags) {
        if (getAccessibilityNodeProvider() != null) {
            if ((flags & TEXT_DIRECTION_RTL) != 0) {
                outViews.add(this);
            }
        } else if ((flags & TEXT_DIRECTION_ANY_RTL) != 0 && searched != null && searched.length() > 0 && this.mContentDescription != null && this.mContentDescription.length() > 0) {
            if (this.mContentDescription.toString().toLowerCase().contains(searched.toString().toLowerCase())) {
                outViews.add(this);
            }
        }
    }

    public ArrayList<View> getTouchables() {
        ArrayList<View> result = new ArrayList();
        addTouchables(result);
        return result;
    }

    public void addTouchables(ArrayList<View> views) {
        int viewFlags = this.mViewFlags;
        if (!((viewFlags & RECENT_APPS_VISIBLE) == RECENT_APPS_VISIBLE || (viewFlags & STATUS_BAR_DISABLE_HOME) == STATUS_BAR_DISABLE_HOME)) {
            if ((viewFlags & STATUS_BAR_DISABLE_CLOCK) != STATUS_BAR_DISABLE_CLOCK) {
                return;
            }
        }
        if ((viewFlags & SCROLL_INDICATOR_END) == 0) {
            views.add(this);
        }
    }

    public boolean isAccessibilityFocused() {
        return (this.mPrivateFlags2 & STATUS_BAR_TRANSIENT) != 0 ? true : DBG;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean requestAccessibilityFocus() {
        AccessibilityManager manager = AccessibilityManager.getInstance(this.mContext);
        if (!manager.isEnabled() || !manager.isTouchExplorationEnabled() || (this.mViewFlags & VISIBILITY_MASK) != 0 || (this.mPrivateFlags2 & STATUS_BAR_TRANSIENT) != 0) {
            return DBG;
        }
        this.mPrivateFlags2 |= STATUS_BAR_TRANSIENT;
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null) {
            viewRootImpl.setAccessibilityFocus(this, null);
        }
        invalidate();
        sendAccessibilityEvent(PFLAG_DRAWING_CACHE_VALID);
        return true;
    }

    public void clearAccessibilityFocus() {
        clearAccessibilityFocusNoCallbacks(VISIBLE);
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null) {
            View focusHost = viewRootImpl.getAccessibilityFocusedHost();
            if (focusHost != null && ViewRootImpl.isViewDescendantOf(focusHost, this)) {
                viewRootImpl.setAccessibilityFocus(null, null);
            }
        }
    }

    private void sendAccessibilityHoverEvent(int eventType) {
        View source = this;
        while (!source.includeForAccessibility()) {
            ViewParent parent = source.getParent();
            if (parent instanceof View) {
                source = (View) parent;
            } else {
                return;
            }
        }
        source.sendAccessibilityEvent(eventType);
    }

    void clearAccessibilityFocusNoCallbacks(int action) {
        if ((this.mPrivateFlags2 & STATUS_BAR_TRANSIENT) != 0) {
            this.mPrivateFlags2 &= -67108865;
            invalidate();
            if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain((int) TV_PICTURE_IN_PICTURE_VISIBLE);
                event.setAction(action);
                if (this.mAccessibilityDelegate != null) {
                    this.mAccessibilityDelegate.sendAccessibilityEventUnchecked(this, event);
                } else {
                    sendAccessibilityEventUnchecked(event);
                }
            }
        }
    }

    public final boolean requestFocus() {
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm == null || !imm.isViewInTransition(this) || this.mAttachInfo == null || !this.mAttachInfo.mHasWindowFocus) {
            return requestFocus(FOCUS_DOWN);
        }
        imm.resetInTransitionState();
        return DBG;
    }

    public final boolean requestFocus(int direction) {
        return requestFocus(direction, null);
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return requestFocusNoSearch(direction, previouslyFocusedRect);
    }

    private boolean requestFocusNoSearch(int direction, Rect previouslyFocusedRect) {
        if ((this.mViewFlags & VIEW_IS_NOT_ADVIEW_OR_DISABLE) != VIEW_IS_NOT_ADVIEW_OR_DISABLE || (this.mViewFlags & VISIBILITY_MASK) != 0) {
            return DBG;
        }
        if ((isInTouchMode() && STATUS_BAR_DISABLE_NOTIFICATION_ALERTS != (this.mViewFlags & STATUS_BAR_DISABLE_NOTIFICATION_ALERTS)) || hasAncestorThatBlocksDescendantFocus()) {
            return DBG;
        }
        handleFocusGainInternal(direction, previouslyFocusedRect);
        return true;
    }

    public final boolean requestFocusFromTouch() {
        if (isInTouchMode()) {
            ViewRootImpl viewRoot = getViewRootImpl();
            if (viewRoot != null) {
                viewRoot.ensureTouchMode(DBG);
            }
        }
        return requestFocus(FOCUS_DOWN);
    }

    private boolean hasAncestorThatBlocksDescendantFocus() {
        boolean focusableInTouchMode = isFocusableInTouchMode();
        ViewParent ancestor = this.mParent;
        while (ancestor instanceof ViewGroup) {
            ViewGroup vgAncestor = (ViewGroup) ancestor;
            if (vgAncestor.getDescendantFocusability() == Protocol.BASE_NSD_MANAGER || (!focusableInTouchMode && vgAncestor.shouldBlockFocusForTouchscreen())) {
                return true;
            }
            ancestor = vgAncestor.getParent();
        }
        return DBG;
    }

    @ExportedProperty(category = "accessibility", mapping = {@IntToString(from = 0, to = "auto"), @IntToString(from = 1, to = "yes"), @IntToString(from = 2, to = "no"), @IntToString(from = 4, to = "noHideDescendants")})
    public int getImportantForAccessibility() {
        return (this.mPrivateFlags2 & PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_MASK) >> PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_SHIFT;
    }

    public void setAccessibilityLiveRegion(int mode) {
        if (mode != getAccessibilityLiveRegion()) {
            this.mPrivateFlags2 &= -25165825;
            this.mPrivateFlags2 |= (mode << PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT) & PFLAG_OPAQUE_MASK;
            notifyViewAccessibilityStateChangedIfNeeded(VISIBLE);
        }
    }

    public int getAccessibilityLiveRegion() {
        return (this.mPrivateFlags2 & PFLAG_OPAQUE_MASK) >> PFLAG2_ACCESSIBILITY_LIVE_REGION_SHIFT;
    }

    public void setImportantForAccessibility(int mode) {
        boolean maySkipNotify = true;
        int oldMode = getImportantForAccessibility();
        if (mode != oldMode) {
            boolean hideDescendants = mode == TEXT_DIRECTION_RTL ? true : DBG;
            if (mode == TEXT_DIRECTION_ANY_RTL || hideDescendants) {
                View focusHost = findAccessibilityFocusHost(hideDescendants);
                if (focusHost != null) {
                    focusHost.clearAccessibilityFocus();
                }
            }
            if (!(oldMode == 0 || mode == 0)) {
                maySkipNotify = DBG;
            }
            boolean includeForAccessibility = maySkipNotify ? includeForAccessibility() : DBG;
            this.mPrivateFlags2 &= -7340033;
            this.mPrivateFlags2 |= (mode << PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_SHIFT) & PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_MASK;
            if (maySkipNotify && includeForAccessibility == includeForAccessibility()) {
                notifyViewAccessibilityStateChangedIfNeeded(VISIBLE);
            } else {
                notifySubtreeAccessibilityStateChangedIfNeeded();
            }
        }
    }

    private View findAccessibilityFocusHost(boolean searchDescendants) {
        if (isAccessibilityFocusedViewOrHost()) {
            return this;
        }
        if (searchDescendants) {
            ViewRootImpl viewRoot = getViewRootImpl();
            if (viewRoot != null) {
                View focusHost = viewRoot.getAccessibilityFocusedHost();
                if (focusHost == null || !ViewRootImpl.isViewDescendantOf(focusHost, this)) {
                    return null;
                }
                return focusHost;
            }
        }
        return null;
    }

    public boolean isImportantForAccessibility() {
        int mode = (this.mPrivateFlags2 & PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_MASK) >> PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_SHIFT;
        if (mode == TEXT_DIRECTION_ANY_RTL || mode == TEXT_DIRECTION_RTL) {
            return DBG;
        }
        for (ViewParent parent = this.mParent; parent instanceof View; parent = parent.getParent()) {
            if (((View) parent).getImportantForAccessibility() == TEXT_DIRECTION_RTL) {
                return DBG;
            }
        }
        boolean z = (mode == VIEW_IS_NOT_ADVIEW_OR_DISABLE || isActionableForAccessibility() || hasListenersForAccessibility() || getAccessibilityNodeProvider() != null) ? true : getAccessibilityLiveRegion() != 0 ? true : DBG;
        return z;
    }

    public ViewParent getParentForAccessibility() {
        if (!(this.mParent instanceof View)) {
            return null;
        }
        if (this.mParent.includeForAccessibility()) {
            return this.mParent;
        }
        return this.mParent.getParentForAccessibility();
    }

    public void addChildrenForAccessibility(ArrayList<View> arrayList) {
    }

    public boolean includeForAccessibility() {
        if (this.mAttachInfo == null) {
            return DBG;
        }
        boolean isImportantForAccessibility;
        if ((this.mAttachInfo.mAccessibilityFetchFlags & STATUS_BAR_TRANSPARENT) == 0) {
            isImportantForAccessibility = isImportantForAccessibility();
        } else {
            isImportantForAccessibility = true;
        }
        return isImportantForAccessibility;
    }

    public boolean isActionableForAccessibility() {
        return (isClickable() || isLongClickable()) ? true : isFocusable();
    }

    private boolean hasListenersForAccessibility() {
        ListenerInfo info = getListenerInfo();
        if (this.mTouchDelegate == null && info.mOnKeyListener == null && info.mOnTouchListener == null && info.mOnGenericMotionListener == null && info.mOnHoverListener == null && info.mOnDragListener == null) {
            return DBG;
        }
        return true;
    }

    public void notifyViewAccessibilityStateChangedIfNeeded(int changeType) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && this.mAttachInfo != null) {
            if (this.mSendViewStateChangedAccessibilityEvent == null) {
                this.mSendViewStateChangedAccessibilityEvent = new SendViewStateChangedAccessibilityEvent();
            }
            this.mSendViewStateChangedAccessibilityEvent.runOrPost(changeType);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifySubtreeAccessibilityStateChangedIfNeeded() {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && this.mAttachInfo != null && (this.mPrivateFlags2 & SOUND_EFFECTS_ENABLED) == 0) {
            this.mPrivateFlags2 |= SOUND_EFFECTS_ENABLED;
            if (this.mParent != null) {
                try {
                    this.mParent.notifySubtreeAccessibilityStateChanged(this, this, VIEW_IS_NOT_ADVIEW_OR_DISABLE);
                } catch (AbstractMethodError e) {
                    Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                }
            }
        }
    }

    public void setTransitionVisibility(int visibility) {
        this.mViewFlags = (this.mViewFlags & -13) | visibility;
    }

    void resetSubtreeAccessibilityStateChanged() {
        this.mPrivateFlags2 &= -134217729;
    }

    public boolean dispatchNestedPrePerformAccessibilityAction(int action, Bundle arguments) {
        for (ViewParent p = getParent(); p != null; p = p.getParent()) {
            if (p.onNestedPrePerformAccessibilityAction(this, action, arguments)) {
                return true;
            }
        }
        return DBG;
    }

    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (this.mAccessibilityDelegate != null) {
            return this.mAccessibilityDelegate.performAccessibilityAction(this, action, arguments);
        }
        return performAccessibilityActionInternal(action, arguments);
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        boolean z = DBG;
        if (isNestedScrollingEnabled() && ((action == SYSTEM_UI_FLAG_LIGHT_STATUS_BAR || action == SYSTEM_UI_FLAG_IMMERSIVE_STICKY || action == R.id.accessibilityActionScrollUp || action == R.id.accessibilityActionScrollLeft || action == R.id.accessibilityActionScrollDown || action == R.id.accessibilityActionScrollRight) && dispatchNestedPrePerformAccessibilityAction(action, arguments))) {
            return true;
        }
        switch (action) {
            case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                if (!hasFocus()) {
                    getViewRootImpl().ensureTouchMode(DBG);
                    return requestFocus();
                }
                break;
            case TEXT_DIRECTION_ANY_RTL /*2*/:
                if (hasFocus()) {
                    clearFocus();
                    if (!isFocused()) {
                        z = true;
                    }
                    return z;
                }
                break;
            case TEXT_DIRECTION_RTL /*4*/:
                if (!isSelected()) {
                    setSelected(true);
                    return isSelected();
                }
                break;
            case STATUS_BAR_TRANSPARENT /*8*/:
                if (isSelected()) {
                    setSelected(DBG);
                    if (!isSelected()) {
                        z = true;
                    }
                    return z;
                }
                break;
            case SCROLL_INDICATOR_START /*16*/:
                if (isClickable()) {
                    performClick();
                    return true;
                }
                break;
            case SCROLL_INDICATOR_END /*32*/:
                if (isLongClickable()) {
                    performLongClick();
                    return true;
                }
                break;
            case PFLAG_DRAW_ANIMATION /*64*/:
                if (!isAccessibilityFocused()) {
                    return requestAccessibilityFocus();
                }
                break;
            case WILL_NOT_DRAW /*128*/:
                if (isAccessibilityFocused()) {
                    clearAccessibilityFocus();
                    return true;
                }
                break;
            case SYSTEM_UI_FLAG_LAYOUT_STABLE /*256*/:
                if (arguments != null) {
                    return traverseAtGranularity(arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT), true, arguments.getBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN));
                }
                break;
            case SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION /*512*/:
                if (arguments != null) {
                    return traverseAtGranularity(arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT), DBG, arguments.getBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN));
                }
                break;
            case WILL_NOT_CACHE_DRAWING /*131072*/:
                if (getIterableTextForAccessibility() == null) {
                    return DBG;
                }
                int start;
                int end;
                if (arguments != null) {
                    start = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, VIEW_IS_NOT_CHECKED);
                } else {
                    start = VIEW_IS_NOT_CHECKED;
                }
                if (arguments != null) {
                    end = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, VIEW_IS_NOT_CHECKED);
                } else {
                    end = VIEW_IS_NOT_CHECKED;
                }
                if (!(getAccessibilitySelectionStart() == start && getAccessibilitySelectionEnd() == end) && start == end) {
                    setAccessibilitySelection(start, end);
                    notifyViewAccessibilityStateChangedIfNeeded(VISIBLE);
                    return true;
                }
            case R.id.accessibilityActionShowOnScreen /*16908342*/:
                if (this.mAttachInfo != null) {
                    Rect r = this.mAttachInfo.mTmpInvalRect;
                    getDrawingRect(r);
                    return requestRectangleOnScreen(r, true);
                }
                break;
            case R.id.accessibilityActionContextClick /*16908348*/:
                if (isContextClickable()) {
                    performContextClick();
                    return true;
                }
                break;
        }
        return DBG;
    }

    private boolean traverseAtGranularity(int granularity, boolean forward, boolean extendSelection) {
        CharSequence text = getIterableTextForAccessibility();
        if (text == null || text.length() == 0) {
            return DBG;
        }
        TextSegmentIterator iterator = getIteratorForGranularity(granularity);
        if (iterator == null) {
            return DBG;
        }
        int current = getAccessibilitySelectionEnd();
        if (current == VIEW_IS_NOT_CHECKED) {
            current = forward ? VISIBLE : text.length();
        }
        int[] range = forward ? iterator.following(current) : iterator.preceding(current);
        if (range == null) {
            return DBG;
        }
        int selectionStart;
        int selectionEnd;
        int action;
        int segmentStart = range[VISIBLE];
        int segmentEnd = range[VIEW_IS_NOT_ADVIEW_OR_DISABLE];
        if (extendSelection && isAccessibilitySelectionExtendable()) {
            selectionStart = getAccessibilitySelectionStart();
            if (selectionStart == VIEW_IS_NOT_CHECKED) {
                selectionStart = forward ? segmentStart : segmentEnd;
            }
            selectionEnd = forward ? segmentEnd : segmentStart;
        } else {
            selectionEnd = forward ? segmentEnd : segmentStart;
            selectionStart = selectionEnd;
        }
        setAccessibilitySelection(selectionStart, selectionEnd);
        if (forward) {
            action = SYSTEM_UI_FLAG_LAYOUT_STABLE;
        } else {
            action = SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        sendViewTextTraversedAtGranularityEvent(action, granularity, segmentStart, segmentEnd);
        return true;
    }

    public CharSequence getIterableTextForAccessibility() {
        return getContentDescription();
    }

    public boolean isAccessibilitySelectionExtendable() {
        return DBG;
    }

    public int getAccessibilitySelectionStart() {
        return this.mAccessibilityCursorPosition;
    }

    public int getAccessibilitySelectionEnd() {
        return getAccessibilitySelectionStart();
    }

    public void setAccessibilitySelection(int start, int end) {
        if (start != end || end != this.mAccessibilityCursorPosition) {
            if (start < 0 || start != end || end > getIterableTextForAccessibility().length()) {
                this.mAccessibilityCursorPosition = VIEW_IS_NOT_CHECKED;
            } else {
                this.mAccessibilityCursorPosition = start;
            }
            sendAccessibilityEvent(SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void sendViewTextTraversedAtGranularityEvent(int action, int granularity, int fromIndex, int toIndex) {
        if (this.mParent != null) {
            AccessibilityEvent event = AccessibilityEvent.obtain((int) WILL_NOT_CACHE_DRAWING);
            onInitializeAccessibilityEvent(event);
            onPopulateAccessibilityEvent(event);
            event.setFromIndex(fromIndex);
            event.setToIndex(toIndex);
            event.setAction(action);
            event.setMovementGranularity(granularity);
            this.mParent.requestSendAccessibilityEvent(this, event);
        }
    }

    public TextSegmentIterator getIteratorForGranularity(int granularity) {
        CharSequence text;
        switch (granularity) {
            case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                text = getIterableTextForAccessibility();
                if (text != null && text.length() > 0) {
                    CharacterTextSegmentIterator iterator = CharacterTextSegmentIterator.getInstance(this.mContext.getResources().getConfiguration().locale);
                    iterator.initialize(text.toString());
                    return iterator;
                }
            case TEXT_DIRECTION_ANY_RTL /*2*/:
                text = getIterableTextForAccessibility();
                if (text != null && text.length() > 0) {
                    WordTextSegmentIterator iterator2 = WordTextSegmentIterator.getInstance(this.mContext.getResources().getConfiguration().locale);
                    iterator2.initialize(text.toString());
                    return iterator2;
                }
            case STATUS_BAR_TRANSPARENT /*8*/:
                text = getIterableTextForAccessibility();
                if (text != null && text.length() > 0) {
                    ParagraphTextSegmentIterator iterator3 = ParagraphTextSegmentIterator.getInstance();
                    iterator3.initialize(text.toString());
                    return iterator3;
                }
        }
        return null;
    }

    public final boolean isTemporarilyDetached() {
        return (this.mPrivateFlags3 & STATUS_BAR_DISABLE_SEARCH) != 0 ? true : DBG;
    }

    public void dispatchStartTemporaryDetach() {
        this.mPrivateFlags3 |= STATUS_BAR_DISABLE_SEARCH;
        onStartTemporaryDetach();
    }

    public void onStartTemporaryDetach() {
        removeUnsetPressCallback();
        this.mPrivateFlags |= STATUS_BAR_TRANSIENT;
    }

    public void dispatchFinishTemporaryDetach() {
        onFinishTemporaryDetach();
        this.mPrivateFlags3 &= -33554433;
        if (hasWindowFocus() && hasFocus()) {
            InputMethodManager.getInstance().focusIn(this);
        }
    }

    public void onFinishTemporaryDetach() {
    }

    public DispatcherState getKeyDispatcherState() {
        return this.mAttachInfo != null ? this.mAttachInfo.mKeyDispatchState : null;
    }

    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        return onKeyPreIme(event.getKeyCode(), event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        DispatcherState dispatcherState = null;
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onKeyEvent(event, VISIBLE);
        }
        ListenerInfo li = this.mListenerInfo;
        if (li != null && li.mOnKeyListener != null && (this.mViewFlags & SCROLL_INDICATOR_END) == 0 && li.mOnKeyListener.onKey(this, event.getKeyCode(), event)) {
            return true;
        }
        if (this.mAttachInfo != null) {
            dispatcherState = this.mAttachInfo.mKeyDispatchState;
        }
        if (event.dispatch(this, dispatcherState, this)) {
            return true;
        }
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, VISIBLE);
        }
        return DBG;
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return onKeyShortcut(event.getKeyCode(), event);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.isTargetAccessibilityFocus()) {
            if (!isAccessibilityFocusedViewOrHost()) {
                return DBG;
            }
            event.setTargetAccessibilityFocus(DBG);
        }
        boolean result = DBG;
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTouchEvent(event, VISIBLE);
        }
        int actionMasked = event.getActionMasked();
        if (actionMasked == 0) {
            stopNestedScroll();
        }
        if (onFilterTouchEventForSecurity(event)) {
            if ((this.mViewFlags & SCROLL_INDICATOR_END) == 0 && handleScrollBarDragging(event)) {
                result = true;
            }
            ListenerInfo li = this.mListenerInfo;
            if (li != null && li.mOnTouchListener != null && (this.mViewFlags & SCROLL_INDICATOR_END) == 0 && li.mOnTouchListener.onTouch(this, event)) {
                result = true;
            }
            if (!result && onTouchEvent(event)) {
                result = true;
            }
        }
        if (!(result || this.mInputEventConsistencyVerifier == null)) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, VISIBLE);
        }
        if (!(actionMasked == VIEW_IS_NOT_ADVIEW_OR_DISABLE || actionMasked == TEXT_DIRECTION_LTR)) {
            if (actionMasked == 0) {
                if (result) {
                }
            }
            return result;
        }
        stopNestedScroll();
        return result;
    }

    boolean isAccessibilityFocusedViewOrHost() {
        if (isAccessibilityFocused()) {
            return true;
        }
        return (getViewRootImpl() == null || getViewRootImpl().getAccessibilityFocusedHost() != this) ? DBG : true;
    }

    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        if ((this.mViewFlags & SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) == 0 || (event.getFlags() & VIEW_IS_NOT_ADVIEW_OR_DISABLE) == 0) {
            return true;
        }
        return DBG;
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTrackballEvent(event, VISIBLE);
        }
        return onTrackballEvent(event);
    }

    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onGenericMotionEvent(event, VISIBLE);
        }
        if ((event.getSource() & TEXT_DIRECTION_ANY_RTL) != 0) {
            int action = event.getAction();
            if (action == 9 || action == TEXT_DIRECTION_FIRST_STRONG_RTL || action == PFLAG2_TEXT_DIRECTION_RESOLVED_MASK_SHIFT) {
                if (dispatchHoverEvent(event)) {
                    return true;
                }
            } else if (dispatchGenericPointerEvent(event)) {
                return true;
            }
        } else if (dispatchGenericFocusedEvent(event)) {
            return true;
        }
        if (dispatchGenericMotionEventInternal(event)) {
            return true;
        }
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, VISIBLE);
        }
        return DBG;
    }

    private boolean dispatchGenericMotionEventInternal(MotionEvent event) {
        ListenerInfo li = this.mListenerInfo;
        if ((li != null && li.mOnGenericMotionListener != null && (this.mViewFlags & SCROLL_INDICATOR_END) == 0 && li.mOnGenericMotionListener.onGenericMotion(this, event)) || onGenericMotionEvent(event)) {
            return true;
        }
        int actionButton = event.getActionButton();
        switch (event.getActionMasked()) {
            case PGSdk.TYPE_IM /*11*/:
                if (isContextClickable() && !this.mInContextButtonPress && !this.mHasPerformedLongPress && ((actionButton == SCROLL_INDICATOR_END || actionButton == TEXT_DIRECTION_ANY_RTL) && performContextClick(event.getX(), event.getY()))) {
                    this.mInContextButtonPress = true;
                    setPressed(true, event.getX(), event.getY());
                    removeTapCallback();
                    removeLongPressCallback();
                    return true;
                }
            case VISIBILITY_MASK /*12*/:
                if (this.mInContextButtonPress && (actionButton == SCROLL_INDICATOR_END || actionButton == TEXT_DIRECTION_ANY_RTL)) {
                    this.mInContextButtonPress = DBG;
                    this.mIgnoreNextUpEvent = true;
                    break;
                }
        }
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, VISIBLE);
        }
        return DBG;
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        ListenerInfo li = this.mListenerInfo;
        if (li == null || li.mOnHoverListener == null || (this.mViewFlags & SCROLL_INDICATOR_END) != 0 || !li.mOnHoverListener.onHover(this, event)) {
            return onHoverEvent(event);
        }
        return true;
    }

    protected boolean hasHoveredChild() {
        return DBG;
    }

    protected boolean dispatchGenericPointerEvent(MotionEvent event) {
        return DBG;
    }

    protected boolean dispatchGenericFocusedEvent(MotionEvent event) {
        return DBG;
    }

    public final boolean dispatchPointerEvent(MotionEvent event) {
        if (event.isTouchEvent()) {
            return dispatchTouchEvent(event);
        }
        return dispatchGenericMotionEvent(event);
    }

    public void dispatchWindowFocusChanged(boolean hasFocus) {
        onWindowFocusChanged(hasFocus);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (!hasWindowFocus) {
            if (isPressed()) {
                setPressed(DBG);
            }
            if (!(imm == null || (this.mPrivateFlags & TEXT_DIRECTION_ANY_RTL) == 0)) {
                imm.focusOut(this);
            }
            removeLongPressCallback();
            removeTapCallback();
            onFocusLost();
        } else if (!(imm == null || (this.mPrivateFlags & TEXT_DIRECTION_ANY_RTL) == 0)) {
            imm.focusIn(this);
        }
        refreshDrawableState();
    }

    public boolean hasWindowFocus() {
        return this.mAttachInfo != null ? this.mAttachInfo.mHasWindowFocus : DBG;
    }

    protected void dispatchVisibilityChanged(View changedView, int visibility) {
        onVisibilityChanged(changedView, visibility);
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
    }

    public void dispatchDisplayHint(int hint) {
        onDisplayHint(hint);
    }

    protected void onDisplayHint(int hint) {
    }

    public void dispatchWindowVisibilityChanged(int visibility) {
        onWindowVisibilityChanged(visibility);
    }

    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == 0) {
            initialAwakenScrollBars();
        }
    }

    boolean dispatchVisibilityAggregated(boolean isVisible) {
        boolean thisVisible;
        if (getVisibility() == 0) {
            thisVisible = true;
        } else {
            thisVisible = DBG;
        }
        if (thisVisible || !isVisible) {
            onVisibilityAggregated(isVisible);
        }
        if (thisVisible) {
            return isVisible;
        }
        return DBG;
    }

    public void onVisibilityAggregated(boolean isVisible) {
        Drawable fg = null;
        if (isVisible && this.mAttachInfo != null) {
            initialAwakenScrollBars();
        }
        Drawable dr = this.mBackground;
        if (!(dr == null || isVisible == dr.isVisible())) {
            dr.setVisible(isVisible, DBG);
        }
        if (this.mForegroundInfo != null) {
            fg = this.mForegroundInfo.mDrawable;
        }
        if (fg != null && isVisible != fg.isVisible()) {
            fg.setVisible(isVisible, DBG);
        }
    }

    public int getWindowVisibility() {
        return this.mAttachInfo != null ? this.mAttachInfo.mWindowVisibility : STATUS_BAR_TRANSPARENT;
    }

    private void adjustRectSizeWhenInCompat(Rect inoutRect) {
        if (this.mContext != null && !this.mContext.getResources().getCompatibilityInfo().supportsScreen()) {
            DisplayMetrics metrics = this.mContext.getResources().getDisplayMetrics();
            inoutRect.scale(metrics.density / metrics.noncompatDensity);
            Log.i("SDR", "APS: SDR: View.adjustRectSizeWhenInCompat.  Application name = " + this.mContext.getPackageName() + " is running in compat mode" + ", scale outRect.");
        }
    }

    public void getWindowVisibleDisplayFrame(Rect outRect) {
        if (this.mAttachInfo != null) {
            try {
                this.mAttachInfo.mSession.getDisplayFrame(this.mAttachInfo.mWindow, outRect);
                Rect insets = this.mAttachInfo.mVisibleInsets;
                outRect.left += insets.left;
                outRect.top += insets.top;
                outRect.right -= insets.right;
                outRect.bottom -= insets.bottom;
                adjustRectSizeWhenInCompat(outRect);
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        DisplayManagerGlobal.getInstance().getRealDisplay(VISIBLE).getRectSize(outRect);
        adjustRectSizeWhenInCompat(outRect);
    }

    public void getWindowDisplayFrame(Rect outRect) {
        if (this.mAttachInfo != null) {
            try {
                this.mAttachInfo.mSession.getDisplayFrame(this.mAttachInfo.mWindow, outRect);
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        DisplayManagerGlobal.getInstance().getRealDisplay(VISIBLE).getRectSize(outRect);
    }

    public void dispatchConfigurationChanged(Configuration newConfig) {
        onConfigurationChanged(newConfig);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
    }

    void dispatchCollectViewAttributes(AttachInfo attachInfo, int visibility) {
        performCollectViewAttributes(attachInfo, visibility);
    }

    void performCollectViewAttributes(AttachInfo attachInfo, int visibility) {
        if ((visibility & VISIBILITY_MASK) == 0) {
            if ((this.mViewFlags & STATUS_BAR_TRANSIENT) == STATUS_BAR_TRANSIENT) {
                attachInfo.mKeepScreenOn = true;
            }
            attachInfo.mSystemUiVisibility |= this.mSystemUiVisibility;
            ListenerInfo li = this.mListenerInfo;
            if (li != null && li.mOnSystemUiVisibilityChangeListener != null) {
                attachInfo.mHasSystemUiListeners = true;
            }
        }
    }

    void needGlobalAttributesUpdate(boolean force) {
        AttachInfo ai = this.mAttachInfo;
        if (ai != null && !ai.mRecomputeGlobalAttributes) {
            if (!(force || ai.mKeepScreenOn || ai.mSystemUiVisibility != 0)) {
                if (!ai.mHasSystemUiListeners) {
                    return;
                }
            }
            ai.mRecomputeGlobalAttributes = true;
        }
    }

    @ExportedProperty
    public boolean isInTouchMode() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mInTouchMode;
        }
        return ViewRootImpl.isInTouchMode();
    }

    @CapturedViewProperty
    public final Context getContext() {
        return this.mContext;
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        return DBG;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.isConfirmKey(keyCode)) {
            if ((this.mViewFlags & SCROLL_INDICATOR_END) == SCROLL_INDICATOR_END) {
                return true;
            }
            if (((this.mViewFlags & RECENT_APPS_VISIBLE) == RECENT_APPS_VISIBLE || (this.mViewFlags & STATUS_BAR_DISABLE_HOME) == STATUS_BAR_DISABLE_HOME) && event.getRepeatCount() == 0) {
                float x = ((float) getWidth()) / 2.0f;
                float y = ((float) getHeight()) / 2.0f;
                setPressed(true, x, y);
                checkForLongClick(VISIBLE, x, y);
                return true;
            }
        }
        return DBG;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return DBG;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.isConfirmKey(keyCode)) {
            if ((this.mViewFlags & SCROLL_INDICATOR_END) == SCROLL_INDICATOR_END) {
                return true;
            }
            if ((this.mViewFlags & RECENT_APPS_VISIBLE) == RECENT_APPS_VISIBLE && isPressed()) {
                setPressed(DBG);
                if (!this.mHasPerformedLongPress) {
                    removeLongPressCallback();
                    return performClick();
                }
            }
        }
        return DBG;
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return DBG;
    }

    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        return DBG;
    }

    public boolean onCheckIsTextEditor() {
        return DBG;
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return null;
    }

    public boolean checkInputConnectionProxy(View view) {
        return DBG;
    }

    public void createContextMenu(ContextMenu menu) {
        ContextMenuInfo menuInfo = getContextMenuInfo();
        ((MenuBuilder) menu).setCurrentMenuInfo(menuInfo);
        onCreateContextMenu(menu);
        ListenerInfo li = this.mListenerInfo;
        if (!(li == null || li.mOnCreateContextMenuListener == null)) {
            li.mOnCreateContextMenuListener.onCreateContextMenu(menu, this, menuInfo);
        }
        ((MenuBuilder) menu).setCurrentMenuInfo(null);
        if (this.mParent != null) {
            this.mParent.createContextMenu(menu);
        }
    }

    protected ContextMenuInfo getContextMenuInfo() {
        return null;
    }

    protected void onCreateContextMenu(ContextMenu menu) {
    }

    public boolean onTrackballEvent(MotionEvent event) {
        return DBG;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return DBG;
    }

    public boolean onHoverEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (this.mSendingHoverAccessibilityEvents) {
            if (action == PFLAG2_TEXT_DIRECTION_RESOLVED_MASK_SHIFT || (action == TEXT_DIRECTION_ANY_RTL && !pointInView(event.getX(), event.getY()))) {
                this.mSendingHoverAccessibilityEvents = DBG;
                sendAccessibilityHoverEvent(SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        } else if ((action == 9 || action == TEXT_DIRECTION_FIRST_STRONG_RTL) && !hasHoveredChild() && pointInView(event.getX(), event.getY())) {
            sendAccessibilityHoverEvent(WILL_NOT_DRAW);
            this.mSendingHoverAccessibilityEvents = true;
        }
        if ((action == 9 || action == TEXT_DIRECTION_FIRST_STRONG_RTL) && event.isFromSource(InputDevice.SOURCE_MOUSE) && isOnScrollbar(event.getX(), event.getY())) {
            awakenScrollBars();
        }
        if (!isHoverable()) {
            return DBG;
        }
        switch (action) {
            case PGSdk.TYPE_SCRLOCK /*9*/:
                setHovered(true);
                break;
            case PFLAG2_TEXT_DIRECTION_RESOLVED_MASK_SHIFT /*10*/:
                setHovered(DBG);
                break;
        }
        dispatchGenericMotionEventInternal(event);
        return true;
    }

    private boolean isHoverable() {
        boolean z = true;
        int viewFlags = this.mViewFlags;
        if ((viewFlags & SCROLL_INDICATOR_END) == SCROLL_INDICATOR_END) {
            return DBG;
        }
        if (!((viewFlags & RECENT_APPS_VISIBLE) == RECENT_APPS_VISIBLE || (viewFlags & STATUS_BAR_DISABLE_HOME) == STATUS_BAR_DISABLE_HOME || (viewFlags & STATUS_BAR_DISABLE_CLOCK) == STATUS_BAR_DISABLE_CLOCK)) {
            z = DBG;
        }
        return z;
    }

    @ExportedProperty
    public boolean isHovered() {
        return (this.mPrivateFlags & STATUS_BAR_UNHIDE) != 0 ? true : DBG;
    }

    public void setHovered(boolean hovered) {
        if (hovered) {
            if ((this.mPrivateFlags & STATUS_BAR_UNHIDE) == 0) {
                this.mPrivateFlags |= STATUS_BAR_UNHIDE;
                refreshDrawableState();
                onHoverChanged(true);
            }
        } else if ((this.mPrivateFlags & STATUS_BAR_UNHIDE) != 0) {
            this.mPrivateFlags &= -268435457;
            refreshDrawableState();
            onHoverChanged(DBG);
        }
    }

    public void onHoverChanged(boolean hovered) {
    }

    protected boolean handleScrollBarDragging(MotionEvent event) {
        if (this.mScrollCache == null) {
            return DBG;
        }
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        if ((this.mScrollCache.mScrollBarDraggingState != 0 || action == 0) && event.isFromSource(InputDevice.SOURCE_MOUSE) && event.isButtonPressed(VIEW_IS_NOT_ADVIEW_OR_DISABLE)) {
            switch (action) {
                case VISIBLE /*0*/:
                    break;
                case TEXT_DIRECTION_ANY_RTL /*2*/:
                    if (this.mScrollCache.mScrollBarDraggingState == 0) {
                        return DBG;
                    }
                    int i = this.mScrollCache.mScrollBarDraggingState;
                    Rect bounds;
                    int range;
                    int offset;
                    int extent;
                    int thumbLength;
                    int thumbOffset;
                    float diff;
                    float maxThumbOffset;
                    float newThumbOffset;
                    if (r0 == VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                        bounds = this.mScrollCache.mScrollBarBounds;
                        getVerticalScrollBarBounds(bounds);
                        range = computeVerticalScrollRange();
                        offset = computeVerticalScrollOffset();
                        extent = computeVerticalScrollExtent();
                        thumbLength = ScrollBarUtils.getThumbLength(bounds.height(), bounds.width(), extent, range);
                        thumbOffset = ScrollBarUtils.getThumbOffset(bounds.height(), thumbLength, extent, range, offset);
                        diff = y - this.mScrollCache.mScrollBarDraggingPos;
                        maxThumbOffset = (float) (bounds.height() - thumbLength);
                        newThumbOffset = Math.min(Math.max(((float) thumbOffset) + diff, 0.0f), maxThumbOffset);
                        int height = getHeight();
                        if (Math.round(newThumbOffset) != thumbOffset && maxThumbOffset > 0.0f && height > 0 && extent > 0) {
                            int newY = Math.round((((float) (range - extent)) / (((float) extent) / ((float) height))) * (newThumbOffset / maxThumbOffset));
                            if (newY != getScrollY()) {
                                this.mScrollCache.mScrollBarDraggingPos = y;
                                setScrollY(newY);
                            }
                        }
                        return true;
                    }
                    i = this.mScrollCache.mScrollBarDraggingState;
                    if (r0 == TEXT_DIRECTION_ANY_RTL) {
                        bounds = this.mScrollCache.mScrollBarBounds;
                        getHorizontalScrollBarBounds(bounds);
                        range = computeHorizontalScrollRange();
                        offset = computeHorizontalScrollOffset();
                        extent = computeHorizontalScrollExtent();
                        thumbLength = ScrollBarUtils.getThumbLength(bounds.width(), bounds.height(), extent, range);
                        thumbOffset = ScrollBarUtils.getThumbOffset(bounds.width(), thumbLength, extent, range, offset);
                        diff = x - this.mScrollCache.mScrollBarDraggingPos;
                        maxThumbOffset = (float) (bounds.width() - thumbLength);
                        newThumbOffset = Math.min(Math.max(((float) thumbOffset) + diff, 0.0f), maxThumbOffset);
                        int width = getWidth();
                        if (Math.round(newThumbOffset) != thumbOffset && maxThumbOffset > 0.0f && width > 0 && extent > 0) {
                            int newX = Math.round((((float) (range - extent)) / (((float) extent) / ((float) width))) * (newThumbOffset / maxThumbOffset));
                            if (newX != getScrollX()) {
                                this.mScrollCache.mScrollBarDraggingPos = x;
                                setScrollX(newX);
                            }
                        }
                        return true;
                    }
                    break;
            }
            if (this.mScrollCache.state == 0) {
                return DBG;
            }
            if (isOnVerticalScrollbarThumb(x, y)) {
                this.mScrollCache.mScrollBarDraggingState = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
                this.mScrollCache.mScrollBarDraggingPos = y;
                return true;
            }
            if (isOnHorizontalScrollbarThumb(x, y)) {
                this.mScrollCache.mScrollBarDraggingState = TEXT_DIRECTION_ANY_RTL;
                this.mScrollCache.mScrollBarDraggingPos = x;
                return true;
            }
            this.mScrollCache.mScrollBarDraggingState = VISIBLE;
            return DBG;
        }
        this.mScrollCache.mScrollBarDraggingState = VISIBLE;
        return DBG;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean z = true;
        float x = event.getX();
        float y = event.getY();
        int viewFlags = this.mViewFlags;
        int action = event.getAction();
        if ((viewFlags & SCROLL_INDICATOR_END) == SCROLL_INDICATOR_END) {
            if (action == VIEW_IS_NOT_ADVIEW_OR_DISABLE && (this.mPrivateFlags & RECENT_APPS_VISIBLE) != 0) {
                setPressed(DBG);
            }
            if (!((viewFlags & RECENT_APPS_VISIBLE) == RECENT_APPS_VISIBLE || (viewFlags & STATUS_BAR_DISABLE_HOME) == STATUS_BAR_DISABLE_HOME || (viewFlags & STATUS_BAR_DISABLE_CLOCK) == STATUS_BAR_DISABLE_CLOCK)) {
                z = DBG;
            }
            return z;
        } else if (this.mTouchDelegate != null && this.mTouchDelegate.onTouchEvent(event)) {
            return true;
        } else {
            if ((viewFlags & RECENT_APPS_VISIBLE) != RECENT_APPS_VISIBLE && (viewFlags & STATUS_BAR_DISABLE_HOME) != STATUS_BAR_DISABLE_HOME && (viewFlags & STATUS_BAR_DISABLE_CLOCK) != STATUS_BAR_DISABLE_CLOCK) {
                return DBG;
            }
            switch (action) {
                case VISIBLE /*0*/:
                    this.mHasPerformedLongPress = DBG;
                    if (!performButtonActionOnTouchDown(event)) {
                        if (!isInScrollingContainer()) {
                            setPressed(true, x, y);
                            checkForLongClick(VISIBLE, x, y);
                            break;
                        }
                        this.mPrivateFlags |= STATUS_BAR_DISABLE_SEARCH;
                        if (this.mPendingCheckForTap == null) {
                            this.mPendingCheckForTap = new CheckForTap();
                        }
                        this.mPendingCheckForTap.x = event.getX();
                        this.mPendingCheckForTap.y = event.getY();
                        postDelayed(this.mPendingCheckForTap, (long) ViewConfiguration.getTapTimeout());
                        break;
                    }
                    break;
                case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                    boolean prepressed = (this.mPrivateFlags & STATUS_BAR_DISABLE_SEARCH) != 0 ? true : DBG;
                    if ((this.mPrivateFlags & RECENT_APPS_VISIBLE) != 0 || prepressed) {
                        boolean focusTaken = DBG;
                        if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
                            focusTaken = requestFocus();
                        }
                        if (prepressed) {
                            setPressed(true, x, y);
                        }
                        if (!(this.mHasPerformedLongPress || this.mIgnoreNextUpEvent)) {
                            removeLongPressCallback();
                            if (!focusTaken) {
                                if (this.mPerformClick == null) {
                                    this.mPerformClick = new PerformClick();
                                }
                                if (!post(this.mPerformClick)) {
                                    performClick();
                                }
                            }
                        }
                        if (this.mUnsetPressedState == null) {
                            this.mUnsetPressedState = new UnsetPressedState();
                        }
                        if (prepressed) {
                            postDelayed(this.mUnsetPressedState, (long) ViewConfiguration.getPressedStateDuration());
                        } else if (!post(this.mUnsetPressedState)) {
                            this.mUnsetPressedState.run();
                        }
                        removeTapCallback();
                    }
                    this.mIgnoreNextUpEvent = DBG;
                    break;
                case TEXT_DIRECTION_ANY_RTL /*2*/:
                    drawableHotspotChanged(x, y);
                    if (!pointInView(x, y, (float) this.mTouchSlop)) {
                        removeTapCallback();
                        if ((this.mPrivateFlags & RECENT_APPS_VISIBLE) != 0) {
                            removeLongPressCallback();
                            setPressed(DBG);
                            break;
                        }
                    }
                    break;
                case TEXT_DIRECTION_LTR /*3*/:
                    setPressed(DBG);
                    removeTapCallback();
                    removeLongPressCallback();
                    this.mInContextButtonPress = DBG;
                    this.mHasPerformedLongPress = DBG;
                    this.mIgnoreNextUpEvent = DBG;
                    break;
            }
            return true;
        }
    }

    public boolean isInScrollingContainer() {
        ViewParent p = getParent();
        while (p != null && (p instanceof ViewGroup)) {
            if (((ViewGroup) p).shouldDelayChildPressedState()) {
                return true;
            }
            p = p.getParent();
        }
        return DBG;
    }

    private void removeLongPressCallback() {
        if (this.mPendingCheckForLongPress != null) {
            removeCallbacks(this.mPendingCheckForLongPress);
        }
    }

    private void removePerformClickCallback() {
        if (this.mPerformClick != null) {
            removeCallbacks(this.mPerformClick);
        }
    }

    private void removeUnsetPressCallback() {
        if ((this.mPrivateFlags & RECENT_APPS_VISIBLE) != 0 && this.mUnsetPressedState != null) {
            setPressed(DBG);
            removeCallbacks(this.mUnsetPressedState);
        }
    }

    private void removeTapCallback() {
        if (this.mPendingCheckForTap != null) {
            this.mPrivateFlags &= -33554433;
            removeCallbacks(this.mPendingCheckForTap);
        }
    }

    public void cancelLongPress() {
        removeLongPressCallback();
        removeTapCallback();
    }

    private void removeSendViewScrolledAccessibilityEventCallback() {
        if (this.mSendViewScrolledAccessibilityEvent != null) {
            removeCallbacks(this.mSendViewScrolledAccessibilityEvent);
            this.mSendViewScrolledAccessibilityEvent.mIsPending = DBG;
        }
    }

    public void setTouchDelegate(TouchDelegate delegate) {
        this.mTouchDelegate = delegate;
    }

    public TouchDelegate getTouchDelegate() {
        return this.mTouchDelegate;
    }

    public final void requestUnbufferedDispatch(MotionEvent event) {
        int action = event.getAction();
        if (this.mAttachInfo != null && ((action == 0 || action == TEXT_DIRECTION_ANY_RTL) && event.isTouchEvent())) {
            this.mAttachInfo.mUnbufferedDispatchRequested = true;
        }
    }

    public void bringToFront() {
        if (this.mParent != null) {
            this.mParent.bringChildToFront(this);
        }
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        notifySubtreeAccessibilityStateChangedIfNeeded();
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            postSendViewScrolledAccessibilityEventCallback();
        }
        this.mBackgroundSizeChanged = true;
        if (this.mForegroundInfo != null) {
            this.mForegroundInfo.mBoundsChanged = true;
        }
        AttachInfo ai = this.mAttachInfo;
        if (ai != null) {
            ai.mViewScrollChanged = true;
        }
        if (this.mListenerInfo != null && this.mListenerInfo.mOnScrollChangeListener != null) {
            this.mListenerInfo.mOnScrollChangeListener.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    }

    protected void dispatchDraw(Canvas canvas) {
    }

    public final ViewParent getParent() {
        return this.mParent;
    }

    public void setScrollX(int value) {
        scrollTo(value, this.mScrollY);
    }

    public void setScrollY(int value) {
        scrollTo(this.mScrollX, value);
    }

    public final int getScrollX() {
        return this.mScrollX;
    }

    public final int getScrollY() {
        return this.mScrollY;
    }

    @ExportedProperty(category = "layout")
    public final int getWidth() {
        return this.mRight - this.mLeft;
    }

    @ExportedProperty(category = "layout")
    public final int getHeight() {
        return this.mBottom - this.mTop;
    }

    public void getDrawingRect(Rect outRect) {
        outRect.left = this.mScrollX;
        outRect.top = this.mScrollY;
        outRect.right = this.mScrollX + (this.mRight - this.mLeft);
        outRect.bottom = this.mScrollY + (this.mBottom - this.mTop);
    }

    public final int getMeasuredWidth() {
        return this.mMeasuredWidth & MEASURED_SIZE_MASK;
    }

    @ExportedProperty(category = "measurement", flagMapping = {@FlagToString(equals = 16777216, mask = -16777216, name = "MEASURED_STATE_TOO_SMALL")})
    public final int getMeasuredWidthAndState() {
        return this.mMeasuredWidth;
    }

    public final int getMeasuredHeight() {
        return this.mMeasuredHeight & MEASURED_SIZE_MASK;
    }

    @ExportedProperty(category = "measurement", flagMapping = {@FlagToString(equals = 16777216, mask = -16777216, name = "MEASURED_STATE_TOO_SMALL")})
    public final int getMeasuredHeightAndState() {
        return this.mMeasuredHeight;
    }

    public final int getMeasuredState() {
        return (this.mMeasuredWidth & MEASURED_STATE_MASK) | ((this.mMeasuredHeight >> SCROLL_INDICATOR_START) & InputDevice.SOURCE_ANY);
    }

    public Matrix getMatrix() {
        ensureTransformationInfo();
        Matrix matrix = this.mTransformationInfo.mMatrix;
        this.mRenderNode.getMatrix(matrix);
        return matrix;
    }

    final boolean hasIdentityMatrix() {
        return this.mRenderNode.hasIdentityMatrix();
    }

    void ensureTransformationInfo() {
        if (this.mTransformationInfo == null) {
            this.mTransformationInfo = new TransformationInfo();
        }
    }

    public final Matrix getInverseMatrix() {
        ensureTransformationInfo();
        if (this.mTransformationInfo.mInverseMatrix == null) {
            this.mTransformationInfo.mInverseMatrix = new Matrix();
        }
        Matrix matrix = this.mTransformationInfo.mInverseMatrix;
        this.mRenderNode.getInverseMatrix(matrix);
        return matrix;
    }

    public float getCameraDistance() {
        return -(this.mRenderNode.getCameraDistance() * ((float) this.mResources.getDisplayMetrics().densityDpi));
    }

    public void setCameraDistance(float distance) {
        float dpi = (float) this.mResources.getDisplayMetrics().densityDpi;
        invalidateViewProperty(true, DBG);
        this.mRenderNode.setCameraDistance((-Math.abs(distance)) / dpi);
        invalidateViewProperty(DBG, DBG);
        invalidateParentIfNeededAndWasQuickRejected();
    }

    @ExportedProperty(category = "drawing")
    public float getRotation() {
        return this.mRenderNode.getRotation();
    }

    public void setRotation(float rotation) {
        if (rotation != getRotation()) {
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setRotation(rotation);
            invalidateViewProperty(DBG, true);
            invalidateParentIfNeededAndWasQuickRejected();
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getRotationY() {
        return this.mRenderNode.getRotationY();
    }

    public void setRotationY(float rotationY) {
        if (rotationY != getRotationY()) {
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setRotationY(rotationY);
            invalidateViewProperty(DBG, true);
            invalidateParentIfNeededAndWasQuickRejected();
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getRotationX() {
        return this.mRenderNode.getRotationX();
    }

    public void setRotationX(float rotationX) {
        if (rotationX != getRotationX()) {
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setRotationX(rotationX);
            invalidateViewProperty(DBG, true);
            invalidateParentIfNeededAndWasQuickRejected();
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getScaleX() {
        return this.mRenderNode.getScaleX();
    }

    public void setScaleX(float scaleX) {
        if (scaleX != getScaleX()) {
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setScaleX(scaleX);
            invalidateViewProperty(DBG, true);
            invalidateParentIfNeededAndWasQuickRejected();
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getScaleY() {
        return this.mRenderNode.getScaleY();
    }

    public void setScaleY(float scaleY) {
        if (scaleY != getScaleY()) {
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setScaleY(scaleY);
            invalidateViewProperty(DBG, true);
            invalidateParentIfNeededAndWasQuickRejected();
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getPivotX() {
        return this.mRenderNode.getPivotX();
    }

    public void setPivotX(float pivotX) {
        if (!this.mRenderNode.isPivotExplicitlySet() || pivotX != getPivotX()) {
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setPivotX(pivotX);
            invalidateViewProperty(DBG, true);
            invalidateParentIfNeededAndWasQuickRejected();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getPivotY() {
        return this.mRenderNode.getPivotY();
    }

    public void setPivotY(float pivotY) {
        if (!this.mRenderNode.isPivotExplicitlySet() || pivotY != getPivotY()) {
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setPivotY(pivotY);
            invalidateViewProperty(DBG, true);
            invalidateParentIfNeededAndWasQuickRejected();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getAlpha() {
        return this.mTransformationInfo != null ? this.mTransformationInfo.mAlpha : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
    }

    public void forceHasOverlappingRendering(boolean hasOverlappingRendering) {
        this.mPrivateFlags3 |= STATUS_BAR_DISABLE_RECENT;
        if (hasOverlappingRendering) {
            this.mPrivateFlags3 |= STATUS_BAR_DISABLE_CLOCK;
        } else {
            this.mPrivateFlags3 &= -8388609;
        }
    }

    public final boolean getHasOverlappingRendering() {
        if ((this.mPrivateFlags3 & STATUS_BAR_DISABLE_RECENT) == 0) {
            return hasOverlappingRendering();
        }
        if ((this.mPrivateFlags3 & STATUS_BAR_DISABLE_CLOCK) != 0) {
            return true;
        }
        return DBG;
    }

    @ExportedProperty(category = "drawing")
    public boolean hasOverlappingRendering() {
        return true;
    }

    public void setAlpha(float alpha) {
        ensureTransformationInfo();
        if (this.mTransformationInfo.mAlpha != alpha) {
            this.mTransformationInfo.mAlpha = alpha;
            if (onSetAlpha((int) (255.0f * alpha))) {
                this.mPrivateFlags |= STATUS_BAR_DISABLE_NOTIFICATION_ALERTS;
                invalidateParentCaches();
                invalidate(true);
                return;
            }
            this.mPrivateFlags &= -262145;
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setAlpha(getFinalAlpha());
            notifyViewAccessibilityStateChangedIfNeeded(VISIBLE);
        }
    }

    boolean setAlphaNoInvalidation(float alpha) {
        ensureTransformationInfo();
        if (this.mTransformationInfo.mAlpha != alpha) {
            this.mTransformationInfo.mAlpha = alpha;
            if (onSetAlpha((int) (255.0f * alpha))) {
                this.mPrivateFlags |= STATUS_BAR_DISABLE_NOTIFICATION_ALERTS;
                return true;
            }
            this.mPrivateFlags &= -262145;
            this.mRenderNode.setAlpha(getFinalAlpha());
        }
        return DBG;
    }

    public void setTransitionAlpha(float alpha) {
        ensureTransformationInfo();
        if (this.mTransformationInfo.mTransitionAlpha != alpha) {
            this.mTransformationInfo.mTransitionAlpha = alpha;
            this.mPrivateFlags &= -262145;
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setAlpha(getFinalAlpha());
        }
    }

    private float getFinalAlpha() {
        if (this.mTransformationInfo != null) {
            return this.mTransformationInfo.mAlpha * this.mTransformationInfo.mTransitionAlpha;
        }
        return WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
    }

    @ExportedProperty(category = "drawing")
    public float getTransitionAlpha() {
        return this.mTransformationInfo != null ? this.mTransformationInfo.mTransitionAlpha : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
    }

    @CapturedViewProperty
    public final int getTop() {
        return this.mTop;
    }

    public final void setTop(int top) {
        if (top != this.mTop) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidate(true);
            } else if (this.mAttachInfo != null) {
                int minTop;
                int yLoc;
                if (top < this.mTop) {
                    minTop = top;
                    yLoc = top - this.mTop;
                } else {
                    minTop = this.mTop;
                    yLoc = VISIBLE;
                }
                invalidate(VISIBLE, yLoc, this.mRight - this.mLeft, this.mBottom - minTop);
            }
            int width = this.mRight - this.mLeft;
            int oldHeight = this.mBottom - this.mTop;
            this.mTop = top;
            this.mRenderNode.setTop(this.mTop);
            sizeChange(width, this.mBottom - this.mTop, width, oldHeight);
            if (!matrixIsIdentity) {
                this.mPrivateFlags |= SCROLL_INDICATOR_END;
                invalidate(true);
            }
            this.mBackgroundSizeChanged = true;
            if (this.mForegroundInfo != null) {
                this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & STATUS_BAR_UNHIDE) == STATUS_BAR_UNHIDE) {
                invalidateParentIfNeeded();
            }
        }
    }

    @CapturedViewProperty
    public final int getBottom() {
        return this.mBottom;
    }

    public boolean isDirty() {
        return (this.mPrivateFlags & PFLAG_DIRTY_MASK) != 0 ? true : DBG;
    }

    public final void setBottom(int bottom) {
        if (bottom != this.mBottom) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidate(true);
            } else if (this.mAttachInfo != null) {
                int maxBottom;
                if (bottom < this.mBottom) {
                    maxBottom = this.mBottom;
                } else {
                    maxBottom = bottom;
                }
                invalidate(VISIBLE, VISIBLE, this.mRight - this.mLeft, maxBottom - this.mTop);
            }
            int width = this.mRight - this.mLeft;
            int oldHeight = this.mBottom - this.mTop;
            this.mBottom = bottom;
            this.mRenderNode.setBottom(this.mBottom);
            sizeChange(width, this.mBottom - this.mTop, width, oldHeight);
            if (!matrixIsIdentity) {
                this.mPrivateFlags |= SCROLL_INDICATOR_END;
                invalidate(true);
            }
            this.mBackgroundSizeChanged = true;
            if (this.mForegroundInfo != null) {
                this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & STATUS_BAR_UNHIDE) == STATUS_BAR_UNHIDE) {
                invalidateParentIfNeeded();
            }
        }
    }

    @CapturedViewProperty
    public final int getLeft() {
        return this.mLeft;
    }

    public final void setLeft(int left) {
        if (left != this.mLeft) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidate(true);
            } else if (this.mAttachInfo != null) {
                int minLeft;
                int xLoc;
                if (left < this.mLeft) {
                    minLeft = left;
                    xLoc = left - this.mLeft;
                } else {
                    minLeft = this.mLeft;
                    xLoc = VISIBLE;
                }
                invalidate(xLoc, VISIBLE, this.mRight - minLeft, this.mBottom - this.mTop);
            }
            int oldWidth = this.mRight - this.mLeft;
            int height = this.mBottom - this.mTop;
            this.mLeft = left;
            this.mRenderNode.setLeft(left);
            sizeChange(this.mRight - this.mLeft, height, oldWidth, height);
            if (!matrixIsIdentity) {
                this.mPrivateFlags |= SCROLL_INDICATOR_END;
                invalidate(true);
            }
            this.mBackgroundSizeChanged = true;
            if (this.mForegroundInfo != null) {
                this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & STATUS_BAR_UNHIDE) == STATUS_BAR_UNHIDE) {
                invalidateParentIfNeeded();
            }
        }
    }

    @CapturedViewProperty
    public final int getRight() {
        return this.mRight;
    }

    public final void setRight(int right) {
        if (right != this.mRight) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidate(true);
            } else if (this.mAttachInfo != null) {
                int maxRight;
                if (right < this.mRight) {
                    maxRight = this.mRight;
                } else {
                    maxRight = right;
                }
                invalidate(VISIBLE, VISIBLE, maxRight - this.mLeft, this.mBottom - this.mTop);
            }
            int oldWidth = this.mRight - this.mLeft;
            int height = this.mBottom - this.mTop;
            this.mRight = right;
            this.mRenderNode.setRight(this.mRight);
            sizeChange(this.mRight - this.mLeft, height, oldWidth, height);
            if (!matrixIsIdentity) {
                this.mPrivateFlags |= SCROLL_INDICATOR_END;
                invalidate(true);
            }
            this.mBackgroundSizeChanged = true;
            if (this.mForegroundInfo != null) {
                this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & STATUS_BAR_UNHIDE) == STATUS_BAR_UNHIDE) {
                invalidateParentIfNeeded();
            }
        }
    }

    @ExportedProperty(category = "drawing")
    public float getX() {
        return ((float) this.mLeft) + getTranslationX();
    }

    public void setX(float x) {
        setTranslationX(x - ((float) this.mLeft));
    }

    @ExportedProperty(category = "drawing")
    public float getY() {
        return ((float) this.mTop) + getTranslationY();
    }

    public void setY(float y) {
        setTranslationY(y - ((float) this.mTop));
    }

    @ExportedProperty(category = "drawing")
    public float getZ() {
        return getElevation() + getTranslationZ();
    }

    public void setZ(float z) {
        setTranslationZ(z - getElevation());
    }

    @ExportedProperty(category = "drawing")
    public float getElevation() {
        return this.mRenderNode.getElevation();
    }

    public void setElevation(float elevation) {
        if (elevation != getElevation()) {
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setElevation(elevation);
            invalidateViewProperty(DBG, true);
            invalidateParentIfNeededAndWasQuickRejected();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getTranslationX() {
        return this.mRenderNode.getTranslationX();
    }

    public void setTranslationX(float translationX) {
        if (translationX != getTranslationX()) {
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setTranslationX(translationX);
            invalidateViewProperty(DBG, true);
            invalidateParentIfNeededAndWasQuickRejected();
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getTranslationY() {
        return this.mRenderNode.getTranslationY();
    }

    public void setTranslationY(float translationY) {
        if (translationY != getTranslationY()) {
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setTranslationY(translationY);
            invalidateViewProperty(DBG, true);
            invalidateParentIfNeededAndWasQuickRejected();
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getTranslationZ() {
        return this.mRenderNode.getTranslationZ();
    }

    public void setTranslationZ(float translationZ) {
        if (translationZ != getTranslationZ()) {
            invalidateViewProperty(true, DBG);
            this.mRenderNode.setTranslationZ(translationZ);
            invalidateViewProperty(DBG, true);
            invalidateParentIfNeededAndWasQuickRejected();
        }
    }

    public void setAnimationMatrix(Matrix matrix) {
        invalidateViewProperty(true, DBG);
        this.mRenderNode.setAnimationMatrix(matrix);
        invalidateViewProperty(DBG, true);
        invalidateParentIfNeededAndWasQuickRejected();
    }

    public StateListAnimator getStateListAnimator() {
        return this.mStateListAnimator;
    }

    public void setStateListAnimator(StateListAnimator stateListAnimator) {
        if (this.mStateListAnimator != stateListAnimator) {
            if (this.mStateListAnimator != null) {
                this.mStateListAnimator.setTarget(null);
            }
            this.mStateListAnimator = stateListAnimator;
            if (stateListAnimator != null) {
                stateListAnimator.setTarget(this);
                if (isAttachedToWindow()) {
                    stateListAnimator.setState(getDrawableState());
                }
            }
        }
    }

    public final boolean getClipToOutline() {
        return this.mRenderNode.getClipToOutline();
    }

    public void setClipToOutline(boolean clipToOutline) {
        damageInParent();
        if (getClipToOutline() != clipToOutline) {
            this.mRenderNode.setClipToOutline(clipToOutline);
        }
    }

    private void setOutlineProviderFromAttribute(int providerInt) {
        switch (providerInt) {
            case VISIBLE /*0*/:
                setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                setOutlineProvider(null);
            case TEXT_DIRECTION_ANY_RTL /*2*/:
                setOutlineProvider(ViewOutlineProvider.BOUNDS);
            case TEXT_DIRECTION_LTR /*3*/:
                setOutlineProvider(ViewOutlineProvider.PADDED_BOUNDS);
            default:
        }
    }

    public void setOutlineProvider(ViewOutlineProvider provider) {
        this.mOutlineProvider = provider;
        invalidateOutline();
    }

    public ViewOutlineProvider getOutlineProvider() {
        return this.mOutlineProvider;
    }

    public void invalidateOutline() {
        rebuildOutline();
        notifySubtreeAccessibilityStateChangedIfNeeded();
        invalidateViewProperty(DBG, DBG);
    }

    private void rebuildOutline() {
        if (this.mAttachInfo != null) {
            if (this.mOutlineProvider == null) {
                this.mRenderNode.setOutline(null);
            } else {
                Outline outline = this.mAttachInfo.mTmpOutline;
                outline.setEmpty();
                outline.setAlpha(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                this.mOutlineProvider.getOutline(this, outline);
                this.mRenderNode.setOutline(outline);
            }
        }
    }

    @ExportedProperty(category = "drawing")
    public boolean hasShadow() {
        return this.mRenderNode.hasShadow();
    }

    public void setRevealClip(boolean shouldClip, float x, float y, float radius) {
        this.mRenderNode.setRevealClip(shouldClip, x, y, radius);
        invalidateViewProperty(DBG, DBG);
    }

    public void getHitRect(Rect outRect) {
        if (hasIdentityMatrix() || this.mAttachInfo == null) {
            outRect.set(this.mLeft, this.mTop, this.mRight, this.mBottom);
            return;
        }
        RectF tmpRect = this.mAttachInfo.mTmpTransformRect;
        tmpRect.set(0.0f, 0.0f, (float) getWidth(), (float) getHeight());
        getMatrix().mapRect(tmpRect);
        outRect.set(((int) tmpRect.left) + this.mLeft, ((int) tmpRect.top) + this.mTop, ((int) tmpRect.right) + this.mLeft, ((int) tmpRect.bottom) + this.mTop);
    }

    final boolean pointInView(float localX, float localY) {
        return pointInView(localX, localY, 0.0f);
    }

    public boolean pointInView(float localX, float localY, float slop) {
        if (localX < (-slop) || localY < (-slop) || localX >= ((float) (this.mRight - this.mLeft)) + slop || localY >= ((float) (this.mBottom - this.mTop)) + slop) {
            return DBG;
        }
        return true;
    }

    public void getFocusedRect(Rect r) {
        getDrawingRect(r);
    }

    public boolean getGlobalVisibleRect(Rect r, Point globalOffset) {
        int width = this.mRight - this.mLeft;
        int height = this.mBottom - this.mTop;
        if (width <= 0 || height <= 0) {
            return DBG;
        }
        r.set(VISIBLE, VISIBLE, width, height);
        if (globalOffset != null) {
            globalOffset.set(-this.mScrollX, -this.mScrollY);
        }
        return this.mParent != null ? this.mParent.getChildVisibleRect(this, r, globalOffset) : true;
    }

    public final boolean getGlobalVisibleRect(Rect r) {
        return getGlobalVisibleRect(r, null);
    }

    public final boolean getLocalVisibleRect(Rect r) {
        Point offset = this.mAttachInfo != null ? this.mAttachInfo.mPoint : new Point();
        if (!getGlobalVisibleRect(r, offset)) {
            return DBG;
        }
        r.offset(-offset.x, -offset.y);
        return true;
    }

    public void offsetTopAndBottom(int offset) {
        if (offset != 0) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidateViewProperty(DBG, DBG);
            } else if (isHardwareAccelerated()) {
                invalidateViewProperty(DBG, DBG);
            } else {
                ViewParent p = this.mParent;
                if (!(p == null || this.mAttachInfo == null)) {
                    int minTop;
                    int maxBottom;
                    int yLoc;
                    Rect r = this.mAttachInfo.mTmpInvalRect;
                    if (offset < 0) {
                        minTop = this.mTop + offset;
                        maxBottom = this.mBottom;
                        yLoc = offset;
                    } else {
                        minTop = this.mTop;
                        maxBottom = this.mBottom + offset;
                        yLoc = VISIBLE;
                    }
                    r.set(VISIBLE, yLoc, this.mRight - this.mLeft, maxBottom - minTop);
                    p.invalidateChild(this, r);
                }
            }
            this.mTop += offset;
            this.mBottom += offset;
            this.mRenderNode.offsetTopAndBottom(offset);
            if (isHardwareAccelerated()) {
                invalidateViewProperty(DBG, DBG);
                invalidateParentIfNeededAndWasQuickRejected();
            } else {
                if (!matrixIsIdentity) {
                    invalidateViewProperty(DBG, true);
                }
                invalidateParentIfNeeded();
            }
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    public void offsetLeftAndRight(int offset) {
        if (offset != 0) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidateViewProperty(DBG, DBG);
            } else if (isHardwareAccelerated()) {
                invalidateViewProperty(DBG, DBG);
            } else {
                ViewParent p = this.mParent;
                if (!(p == null || this.mAttachInfo == null)) {
                    int minLeft;
                    int maxRight;
                    Rect r = this.mAttachInfo.mTmpInvalRect;
                    if (offset < 0) {
                        minLeft = this.mLeft + offset;
                        maxRight = this.mRight;
                    } else {
                        minLeft = this.mLeft;
                        maxRight = this.mRight + offset;
                    }
                    r.set(VISIBLE, VISIBLE, maxRight - minLeft, this.mBottom - this.mTop);
                    p.invalidateChild(this, r);
                }
            }
            this.mLeft += offset;
            this.mRight += offset;
            this.mRenderNode.offsetLeftAndRight(offset);
            if (isHardwareAccelerated()) {
                invalidateViewProperty(DBG, DBG);
                invalidateParentIfNeededAndWasQuickRejected();
            } else {
                if (!matrixIsIdentity) {
                    invalidateViewProperty(DBG, true);
                }
                invalidateParentIfNeeded();
            }
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    @ExportedProperty(deepExport = true, prefix = "layout_")
    public LayoutParams getLayoutParams() {
        return this.mLayoutParams;
    }

    public void setLayoutParams(LayoutParams params) {
        if (params == null) {
            throw new NullPointerException("Layout parameters cannot be null");
        }
        this.mLayoutParams = params;
        resolveLayoutParams();
        if (this.mParent instanceof ViewGroup) {
            ((ViewGroup) this.mParent).onSetLayoutParams(this, params);
        }
        requestLayout();
    }

    public void resolveLayoutParams() {
        if (this.mLayoutParams != null) {
            this.mLayoutParams.resolveLayoutDirection(getLayoutDirection());
        }
    }

    public void scrollTo(int x, int y) {
        if (this.mScrollX != x || this.mScrollY != y) {
            int oldX = this.mScrollX;
            int oldY = this.mScrollY;
            this.mScrollX = x;
            this.mScrollY = y;
            invalidateParentCaches();
            onScrollChanged(this.mScrollX, this.mScrollY, oldX, oldY);
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        }
    }

    public void scrollBy(int x, int y) {
        scrollTo(this.mScrollX + x, this.mScrollY + y);
    }

    protected boolean awakenScrollBars() {
        if (this.mScrollCache != null) {
            return awakenScrollBars(this.mScrollCache.scrollBarDefaultDelayBeforeFade, true);
        }
        return DBG;
    }

    private boolean initialAwakenScrollBars() {
        if (this.mScrollCache != null) {
            return awakenScrollBars(this.mScrollCache.scrollBarDefaultDelayBeforeFade * TEXT_DIRECTION_RTL, true);
        }
        return DBG;
    }

    protected boolean awakenScrollBars(int startDelay) {
        return awakenScrollBars(startDelay, true);
    }

    protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
        ScrollabilityCache scrollCache = this.mScrollCache;
        if (scrollCache == null || !scrollCache.fadeScrollBars) {
            return DBG;
        }
        if (scrollCache.scrollBar == null) {
            scrollCache.scrollBar = new ScrollBarDrawable();
            scrollCache.scrollBar.setState(getDrawableState());
            scrollCache.scrollBar.setCallback(this);
        }
        if (!isHorizontalScrollBarEnabled() && !isVerticalScrollBarEnabled()) {
            return DBG;
        }
        if (invalidate) {
            postInvalidateOnAnimation();
        }
        if (scrollCache.state == 0) {
            startDelay = Math.max(750, startDelay);
        }
        long fadeStartTime = AnimationUtils.currentAnimationTimeMillis() + ((long) startDelay);
        scrollCache.fadeStartTime = fadeStartTime;
        scrollCache.state = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        if (this.mAttachInfo != null) {
            this.mAttachInfo.mHandler.removeCallbacks(scrollCache);
            this.mAttachInfo.mHandler.postAtTime(scrollCache, fadeStartTime);
        }
        return true;
    }

    private boolean skipInvalidate() {
        if (HwFrameworkFactory.getHwNsdImpl().checkAdBlock(this, getContext().getPackageName())) {
            return true;
        }
        boolean z = ((this.mViewFlags & VISIBILITY_MASK) == 0 || this.mCurrentAnimation != null) ? DBG : this.mParent instanceof ViewGroup ? ((ViewGroup) this.mParent).isViewTransitioning(this) ? DBG : true : true;
        return z;
    }

    public void invalidate(Rect dirty) {
        int scrollX = this.mScrollX;
        int scrollY = this.mScrollY;
        invalidateInternal(dirty.left - scrollX, dirty.top - scrollY, dirty.right - scrollX, dirty.bottom - scrollY, true, DBG);
    }

    public void invalidate(int l, int t, int r, int b) {
        int scrollX = this.mScrollX;
        int scrollY = this.mScrollY;
        invalidateInternal(l - scrollX, t - scrollY, r - scrollX, b - scrollY, true, DBG);
    }

    public void invalidate() {
        invalidate(true);
    }

    void invalidate(boolean invalidateCache) {
        invalidateInternal(VISIBLE, VISIBLE, this.mRight - this.mLeft, this.mBottom - this.mTop, invalidateCache, true);
    }

    private boolean checkIsAdView() {
        if (VIEW_IS_NOT_ADVIEW_OR_DISABLE == this.mViewCheckedResult) {
            return DBG;
        }
        if (this.mViewCheckedResult == 0) {
            setVisibility(STATUS_BAR_TRANSPARENT);
            return true;
        }
        this.mViewCheckedResult = HwFrameworkFactory.getHwAdCleaner().checkAdCleaner(this, getContext().getPackageName(), VISIBLE);
        if (this.mViewCheckedResult != 0) {
            return DBG;
        }
        setVisibility(STATUS_BAR_TRANSPARENT);
        return true;
    }

    void invalidateInternal(int l, int t, int r, int b, boolean invalidateCache, boolean fullInvalidate) {
        if (this.mGhostView != null) {
            this.mGhostView.invalidate(true);
        } else if (!checkIsAdView() && !skipInvalidate()) {
            if ((this.mPrivateFlags & PFLAG2_LAYOUT_DIRECTION_RESOLVED_MASK) != PFLAG2_LAYOUT_DIRECTION_RESOLVED_MASK && (!(invalidateCache && (this.mPrivateFlags & PFLAG_DRAWING_CACHE_VALID) == PFLAG_DRAWING_CACHE_VALID) && (this.mPrivateFlags & UNDEFINED_PADDING) == UNDEFINED_PADDING)) {
                if (fullInvalidate && isOpaque() != this.mLastIsOpaque) {
                }
            }
            if (fullInvalidate) {
                this.mLastIsOpaque = isOpaque();
                this.mPrivateFlags &= -33;
            }
            this.mPrivateFlags |= STATUS_BAR_DISABLE_HOME;
            if (invalidateCache) {
                this.mPrivateFlags |= UNDEFINED_PADDING;
                this.mPrivateFlags &= -32769;
            }
            AttachInfo ai = this.mAttachInfo;
            ViewParent p = this.mParent;
            if (p != null && ai != null && l < r && t < b) {
                Rect damage = ai.mTmpInvalRect;
                damage.set(l, t, r, b);
                p.invalidateChild(this, damage);
            }
            if (this.mBackground != null && this.mBackground.isProjected()) {
                View receiver = getProjectionReceiver();
                if (receiver != null) {
                    receiver.damageInParent();
                }
            }
            if (isHardwareAccelerated() && getZ() != 0.0f) {
                damageShadowReceiver();
            }
        }
    }

    private View getProjectionReceiver() {
        ViewParent p = getParent();
        while (p != null && (p instanceof View)) {
            View v = (View) p;
            if (v.isProjectionReceiver()) {
                return v;
            }
            p = p.getParent();
        }
        return null;
    }

    private boolean isProjectionReceiver() {
        return this.mBackground != null ? true : DBG;
    }

    private void damageShadowReceiver() {
        if (this.mAttachInfo != null) {
            ViewParent p = getParent();
            if (p != null && (p instanceof ViewGroup)) {
                ((ViewGroup) p).damageInParent();
            }
        }
    }

    void invalidateViewProperty(boolean invalidateParent, boolean forceRedraw) {
        if (isHardwareAccelerated() && this.mRenderNode.isValid() && (this.mPrivateFlags & PFLAG_DRAW_ANIMATION) == 0) {
            damageInParent();
        } else {
            if (invalidateParent) {
                invalidateParentCaches();
            }
            if (forceRedraw) {
                this.mPrivateFlags |= SCROLL_INDICATOR_END;
            }
            invalidate((boolean) DBG);
        }
        if (isHardwareAccelerated() && invalidateParent && getZ() != 0.0f) {
            damageShadowReceiver();
        }
    }

    protected void damageInParent() {
        AttachInfo ai = this.mAttachInfo;
        if (this.mParent != null && ai != null) {
            Rect r = ai.mTmpInvalRect;
            r.set(VISIBLE, VISIBLE, this.mRight - this.mLeft, this.mBottom - this.mTop);
            if (this.mParent instanceof ViewGroup) {
                ((ViewGroup) this.mParent).damageChild(this, r);
            } else {
                this.mParent.invalidateChild(this, r);
            }
        }
    }

    void transformRect(Rect rect) {
        if (!getMatrix().isIdentity()) {
            RectF boundingRect = this.mAttachInfo.mTmpTransformRect;
            boundingRect.set(rect);
            getMatrix().mapRect(boundingRect);
            rect.set((int) Math.floor((double) boundingRect.left), (int) Math.floor((double) boundingRect.top), (int) Math.ceil((double) boundingRect.right), (int) Math.ceil((double) boundingRect.bottom));
        }
    }

    protected void invalidateParentCaches() {
        if (this.mParent instanceof View) {
            View view = (View) this.mParent;
            view.mPrivateFlags |= UNDEFINED_PADDING;
        }
    }

    protected void invalidateParentIfNeeded() {
        if (isHardwareAccelerated() && (this.mParent instanceof View)) {
            ((View) this.mParent).invalidate(true);
        }
    }

    protected void invalidateParentIfNeededAndWasQuickRejected() {
        if ((this.mPrivateFlags2 & STATUS_BAR_UNHIDE) != 0) {
            invalidateParentIfNeeded();
        }
    }

    @ExportedProperty(category = "drawing")
    public boolean isOpaque() {
        if ((this.mPrivateFlags & PFLAG_OPAQUE_MASK) != PFLAG_OPAQUE_MASK || getFinalAlpha() < WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            return DBG;
        }
        return true;
    }

    protected void computeOpaqueFlags() {
        if (this.mBackground == null || this.mBackground.getOpacity() != VIEW_IS_NOT_CHECKED) {
            this.mPrivateFlags &= -8388609;
        } else {
            this.mPrivateFlags |= STATUS_BAR_DISABLE_CLOCK;
        }
        int flags = this.mViewFlags;
        if (((flags & SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) == 0 && (flags & SYSTEM_UI_FLAG_LAYOUT_STABLE) == 0) || (flags & SCROLLBARS_STYLE_MASK) == 0 || (flags & SCROLLBARS_STYLE_MASK) == STATUS_BAR_DISABLE_SEARCH) {
            this.mPrivateFlags |= STATUS_BAR_DISABLE_RECENT;
        } else {
            this.mPrivateFlags &= -16777217;
        }
    }

    protected boolean hasOpaqueScrollbars() {
        return (this.mPrivateFlags & STATUS_BAR_DISABLE_RECENT) == STATUS_BAR_DISABLE_RECENT ? true : DBG;
    }

    public Handler getHandler() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mHandler;
        }
        return null;
    }

    private HandlerActionQueue getRunQueue() {
        if (this.mRunQueue == null) {
            this.mRunQueue = new HandlerActionQueue();
        }
        return this.mRunQueue;
    }

    public ViewRootImpl getViewRootImpl() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mViewRootImpl;
        }
        return null;
    }

    public ThreadedRenderer getHardwareRenderer() {
        return this.mAttachInfo != null ? this.mAttachInfo.mHardwareRenderer : null;
    }

    public boolean post(Runnable action) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mHandler.post(action);
        }
        getRunQueue().post(action);
        return true;
    }

    public boolean postDelayed(Runnable action, long delayMillis) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mHandler.postDelayed(action, delayMillis);
        }
        getRunQueue().postDelayed(action, delayMillis);
        return true;
    }

    public void postOnAnimation(Runnable action) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            attachInfo.mViewRootImpl.mChoreographer.postCallback(VIEW_IS_NOT_ADVIEW_OR_DISABLE, action, null);
        } else {
            getRunQueue().post(action);
        }
    }

    public void postOnAnimationDelayed(Runnable action, long delayMillis) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            attachInfo.mViewRootImpl.mChoreographer.postCallbackDelayed(VIEW_IS_NOT_ADVIEW_OR_DISABLE, action, null, delayMillis);
        } else {
            getRunQueue().postDelayed(action, delayMillis);
        }
    }

    public boolean removeCallbacks(Runnable action) {
        if (action != null) {
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo != null) {
                attachInfo.mHandler.removeCallbacks(action);
                attachInfo.mViewRootImpl.mChoreographer.removeCallbacks(VIEW_IS_NOT_ADVIEW_OR_DISABLE, action, null);
            }
            getRunQueue().removeCallbacks(action);
        }
        return true;
    }

    public void postInvalidate() {
        postInvalidateDelayed(0);
    }

    public void postInvalidate(int left, int top, int right, int bottom) {
        postInvalidateDelayed(0, left, top, right, bottom);
    }

    public void postInvalidateDelayed(long delayMilliseconds) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            attachInfo.mViewRootImpl.dispatchInvalidateDelayed(this, delayMilliseconds);
        }
    }

    public void postInvalidateDelayed(long delayMilliseconds, int left, int top, int right, int bottom) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            InvalidateInfo info = InvalidateInfo.obtain();
            info.target = this;
            info.left = left;
            info.top = top;
            info.right = right;
            info.bottom = bottom;
            attachInfo.mViewRootImpl.dispatchInvalidateRectDelayed(info, delayMilliseconds);
        }
    }

    public void postInvalidateOnAnimation() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            attachInfo.mViewRootImpl.dispatchInvalidateOnAnimation(this);
        }
    }

    public void postInvalidateOnAnimation(int left, int top, int right, int bottom) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            InvalidateInfo info = InvalidateInfo.obtain();
            info.target = this;
            info.left = left;
            info.top = top;
            info.right = right;
            info.bottom = bottom;
            attachInfo.mViewRootImpl.dispatchInvalidateRectOnAnimation(info);
        }
    }

    private void postSendViewScrolledAccessibilityEventCallback() {
        if (this.mSendViewScrolledAccessibilityEvent == null) {
            this.mSendViewScrolledAccessibilityEvent = new SendViewScrolledAccessibilityEvent();
        }
        if (!this.mSendViewScrolledAccessibilityEvent.mIsPending) {
            this.mSendViewScrolledAccessibilityEvent.mIsPending = true;
            postDelayed(this.mSendViewScrolledAccessibilityEvent, ViewConfiguration.getSendRecurringAccessibilityEventsInterval());
        }
    }

    public void computeScroll() {
    }

    public boolean isHorizontalFadingEdgeEnabled() {
        return (this.mViewFlags & SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == SYSTEM_UI_FLAG_IMMERSIVE_STICKY ? true : DBG;
    }

    public void setHorizontalFadingEdgeEnabled(boolean horizontalFadingEdgeEnabled) {
        if (isHorizontalFadingEdgeEnabled() != horizontalFadingEdgeEnabled) {
            if (horizontalFadingEdgeEnabled) {
                initScrollCache();
            }
            this.mViewFlags ^= SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
    }

    public boolean isVerticalFadingEdgeEnabled() {
        return (this.mViewFlags & SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) == SYSTEM_UI_FLAG_LIGHT_STATUS_BAR ? true : DBG;
    }

    public void setVerticalFadingEdgeEnabled(boolean verticalFadingEdgeEnabled) {
        if (isVerticalFadingEdgeEnabled() != verticalFadingEdgeEnabled) {
            if (verticalFadingEdgeEnabled) {
                initScrollCache();
            }
            this.mViewFlags ^= SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
    }

    protected float getTopFadingEdgeStrength() {
        return computeVerticalScrollOffset() > 0 ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL : 0.0f;
    }

    protected float getBottomFadingEdgeStrength() {
        return computeVerticalScrollOffset() + computeVerticalScrollExtent() < computeVerticalScrollRange() ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL : 0.0f;
    }

    protected float getLeftFadingEdgeStrength() {
        return computeHorizontalScrollOffset() > 0 ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL : 0.0f;
    }

    protected float getRightFadingEdgeStrength() {
        return computeHorizontalScrollOffset() + computeHorizontalScrollExtent() < computeHorizontalScrollRange() ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL : 0.0f;
    }

    public boolean isHorizontalScrollBarEnabled() {
        return (this.mViewFlags & SYSTEM_UI_FLAG_LAYOUT_STABLE) == SYSTEM_UI_FLAG_LAYOUT_STABLE ? true : DBG;
    }

    public void setHorizontalScrollBarEnabled(boolean horizontalScrollBarEnabled) {
        if (isHorizontalScrollBarEnabled() != horizontalScrollBarEnabled) {
            this.mViewFlags ^= SYSTEM_UI_FLAG_LAYOUT_STABLE;
            computeOpaqueFlags();
            resolvePadding();
        }
    }

    public boolean isVerticalScrollBarEnabled() {
        return (this.mViewFlags & SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) == SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION ? true : DBG;
    }

    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
        if (isVerticalScrollBarEnabled() != verticalScrollBarEnabled) {
            this.mViewFlags ^= SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            computeOpaqueFlags();
            resolvePadding();
        }
    }

    protected void recomputePadding() {
        internalSetPadding(this.mUserPaddingLeft, this.mPaddingTop, this.mUserPaddingRight, this.mUserPaddingBottom);
    }

    public void setScrollbarFadingEnabled(boolean fadeScrollbars) {
        initScrollCache();
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        scrollabilityCache.fadeScrollBars = fadeScrollbars;
        if (fadeScrollbars) {
            scrollabilityCache.state = VISIBLE;
        } else {
            scrollabilityCache.state = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
    }

    public boolean isScrollbarFadingEnabled() {
        return this.mScrollCache != null ? this.mScrollCache.fadeScrollBars : DBG;
    }

    public int getScrollBarDefaultDelayBeforeFade() {
        if (this.mScrollCache == null) {
            return ViewConfiguration.getScrollDefaultDelay();
        }
        return this.mScrollCache.scrollBarDefaultDelayBeforeFade;
    }

    public void setScrollBarDefaultDelayBeforeFade(int scrollBarDefaultDelayBeforeFade) {
        getScrollCache().scrollBarDefaultDelayBeforeFade = scrollBarDefaultDelayBeforeFade;
    }

    public int getScrollBarFadeDuration() {
        if (this.mScrollCache == null) {
            return ViewConfiguration.getScrollBarFadeDuration();
        }
        return this.mScrollCache.scrollBarFadeDuration;
    }

    public void setScrollBarFadeDuration(int scrollBarFadeDuration) {
        getScrollCache().scrollBarFadeDuration = scrollBarFadeDuration;
    }

    public int getScrollBarSize() {
        if (this.mScrollCache == null) {
            return ViewConfiguration.get(this.mContext).getScaledScrollBarSize();
        }
        return this.mScrollCache.scrollBarSize;
    }

    public void setScrollBarSize(int scrollBarSize) {
        getScrollCache().scrollBarSize = scrollBarSize;
    }

    public void setScrollBarStyle(int style) {
        if (style != (this.mViewFlags & SCROLLBARS_STYLE_MASK)) {
            this.mViewFlags = (this.mViewFlags & -50331649) | (style & SCROLLBARS_STYLE_MASK);
            computeOpaqueFlags();
            resolvePadding();
        }
    }

    @ExportedProperty(mapping = {@IntToString(from = 0, to = "INSIDE_OVERLAY"), @IntToString(from = 16777216, to = "INSIDE_INSET"), @IntToString(from = 33554432, to = "OUTSIDE_OVERLAY"), @IntToString(from = 50331648, to = "OUTSIDE_INSET")})
    public int getScrollBarStyle() {
        return this.mViewFlags & SCROLLBARS_STYLE_MASK;
    }

    protected int computeHorizontalScrollRange() {
        return getWidth();
    }

    protected int computeHorizontalScrollOffset() {
        return this.mScrollX;
    }

    protected int computeHorizontalScrollExtent() {
        return getWidth();
    }

    protected int computeVerticalScrollRange() {
        return getHeight();
    }

    protected int computeVerticalScrollOffset() {
        return this.mScrollY;
    }

    protected int computeVerticalScrollExtent() {
        return getHeight();
    }

    public boolean canScrollHorizontally(int direction) {
        boolean z = true;
        int offset = computeHorizontalScrollOffset();
        int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) {
            return DBG;
        }
        if (direction < 0) {
            if (offset <= 0) {
                z = DBG;
            }
            return z;
        }
        if (offset >= range + VIEW_IS_NOT_CHECKED) {
            z = DBG;
        }
        return z;
    }

    public boolean canScrollVertically(int direction) {
        boolean z = true;
        int offset = computeVerticalScrollOffset();
        int range = computeVerticalScrollRange() - computeVerticalScrollExtent();
        if (range == 0) {
            return DBG;
        }
        if (direction < 0) {
            if (offset <= 0) {
                z = DBG;
            }
            return z;
        }
        if (offset >= range + VIEW_IS_NOT_CHECKED) {
            z = DBG;
        }
        return z;
    }

    void getScrollIndicatorBounds(Rect out) {
        out.left = this.mScrollX;
        out.right = (this.mScrollX + this.mRight) - this.mLeft;
        out.top = this.mScrollY;
        out.bottom = (this.mScrollY + this.mBottom) - this.mTop;
    }

    private void onDrawScrollIndicators(Canvas c) {
        if ((this.mPrivateFlags3 & SCROLL_INDICATORS_PFLAG3_MASK) != 0) {
            Drawable dr = this.mScrollIndicatorDrawable;
            if (dr != null) {
                int leftRtl;
                int rightRtl;
                int h = dr.getIntrinsicHeight();
                int w = dr.getIntrinsicWidth();
                int[] xy = new int[TEXT_DIRECTION_ANY_RTL];
                if (reSizeScrollIndicators(xy)) {
                    h = xy[VISIBLE];
                    w = xy[VIEW_IS_NOT_ADVIEW_OR_DISABLE];
                }
                Rect rect = this.mAttachInfo.mTmpInvalRect;
                getScrollIndicatorBounds(rect);
                if ((this.mPrivateFlags3 & SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0 && canScrollVertically(VIEW_IS_NOT_CHECKED)) {
                    dr.setBounds(rect.left, rect.top, rect.right, rect.top + h);
                    dr.draw(c);
                }
                if ((this.mPrivateFlags3 & SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0 && canScrollVertically(VIEW_IS_NOT_ADVIEW_OR_DISABLE)) {
                    dr.setBounds(rect.left, rect.bottom - h, rect.right, rect.bottom);
                    dr.draw(c);
                }
                if (getLayoutDirection() == VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                    leftRtl = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    rightRtl = SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                } else {
                    leftRtl = SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                    rightRtl = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                int leftMask = leftRtl | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                if ((this.mPrivateFlags3 & leftMask) != 0 && canScrollHorizontally(VIEW_IS_NOT_CHECKED)) {
                    dr.setBounds(rect.left, rect.top, rect.left + w, rect.bottom);
                    dr.draw(c);
                }
                int rightMask = rightRtl | SYSTEM_UI_FLAG_IMMERSIVE;
                if ((this.mPrivateFlags3 & rightMask) != 0 && canScrollHorizontally(VIEW_IS_NOT_ADVIEW_OR_DISABLE)) {
                    dr.setBounds(rect.right - w, rect.top, rect.right, rect.bottom);
                    dr.draw(c);
                }
            }
        }
    }

    private void getHorizontalScrollBarBounds(Rect bounds) {
        int inside = (this.mViewFlags & STATUS_BAR_DISABLE_SEARCH) == 0 ? VIEW_IS_NOT_CHECKED : VISIBLE;
        boolean drawVerticalScrollBar = isVerticalScrollBarEnabled() ? isVerticalScrollBarHidden() ? DBG : true : DBG;
        int size = getHorizontalScrollbarHeight();
        int verticalScrollBarGap = drawVerticalScrollBar ? getVerticalScrollbarWidth() : VISIBLE;
        int width = this.mRight - this.mLeft;
        bounds.top = ((this.mScrollY + (this.mBottom - this.mTop)) - size) - (this.mUserPaddingBottom & inside);
        bounds.left = this.mScrollX + (this.mPaddingLeft & inside);
        bounds.right = ((this.mScrollX + width) - (this.mUserPaddingRight & inside)) - verticalScrollBarGap;
        bounds.bottom = bounds.top + size;
    }

    private void getVerticalScrollBarBounds(Rect bounds) {
        int inside = (this.mViewFlags & STATUS_BAR_DISABLE_SEARCH) == 0 ? VIEW_IS_NOT_CHECKED : VISIBLE;
        int size = getVerticalScrollbarWidth();
        int verticalScrollbarPosition = this.mVerticalScrollbarPosition;
        if (verticalScrollbarPosition == 0) {
            verticalScrollbarPosition = isRtlLocale() ? VIEW_IS_NOT_ADVIEW_OR_DISABLE : TEXT_DIRECTION_ANY_RTL;
        }
        int width = this.mRight - this.mLeft;
        int height = this.mBottom - this.mTop;
        switch (verticalScrollbarPosition) {
            case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                bounds.left = this.mScrollX + (this.mUserPaddingLeft & inside);
                break;
            default:
                bounds.left = ((this.mScrollX + width) - size) - (this.mUserPaddingRight & inside);
                break;
        }
        bounds.top = this.mScrollY + (this.mPaddingTop & inside);
        bounds.right = bounds.left + size;
        bounds.bottom = (this.mScrollY + height) - (this.mUserPaddingBottom & inside);
    }

    protected final void onDrawScrollBars(Canvas canvas) {
        ScrollabilityCache cache = this.mScrollCache;
        if (cache != null) {
            int state = cache.state;
            if (state != 0) {
                boolean invalidate = DBG;
                if (state == TEXT_DIRECTION_ANY_RTL) {
                    if (cache.interpolatorValues == null) {
                        cache.interpolatorValues = new float[VIEW_IS_NOT_ADVIEW_OR_DISABLE];
                    }
                    float[] values = cache.interpolatorValues;
                    if (cache.scrollBarInterpolator.timeToValues(values) == Result.FREEZE_END) {
                        cache.state = VISIBLE;
                    } else {
                        cache.scrollBar.mutate().setAlpha(Math.round(values[VISIBLE]));
                    }
                    invalidate = true;
                } else {
                    cache.scrollBar.mutate().setAlpha(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
                }
                boolean drawHorizontalScrollBar = isHorizontalScrollBarEnabled();
                boolean drawVerticalScrollBar = isVerticalScrollBarEnabled() ? isVerticalScrollBarHidden() ? DBG : true : DBG;
                if (drawVerticalScrollBar || drawHorizontalScrollBar) {
                    Rect bounds;
                    ScrollBarDrawable scrollBar = cache.scrollBar;
                    if (drawHorizontalScrollBar) {
                        scrollBar.setParameters(computeHorizontalScrollRange(), computeHorizontalScrollOffset(), computeHorizontalScrollExtent(), DBG);
                        bounds = cache.mScrollBarBounds;
                        getHorizontalScrollBarBounds(bounds);
                        onDrawHorizontalScrollBar(canvas, scrollBar, bounds.left, bounds.top, bounds.right, bounds.bottom);
                        if (invalidate) {
                            invalidate(bounds);
                        }
                    }
                    if (drawVerticalScrollBar) {
                        scrollBar.setParameters(computeVerticalScrollRange(), computeVerticalScrollOffset(), computeVerticalScrollExtent(), true);
                        bounds = cache.mScrollBarBounds;
                        getVerticalScrollBarBounds(bounds);
                        onDrawVerticalScrollBar(canvas, scrollBar, bounds.left, bounds.top, bounds.right, bounds.bottom);
                        if (invalidate) {
                            invalidate(bounds);
                        }
                    }
                }
            }
        }
    }

    protected boolean isVerticalScrollBarHidden() {
        return DBG;
    }

    protected void onDrawHorizontalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }

    protected void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }

    protected void onDraw(Canvas canvas) {
    }

    void assignParent(ViewParent parent) {
        if (this.mParent == null) {
            this.mParent = parent;
        } else if (parent == null) {
            this.mParent = null;
        } else {
            throw new RuntimeException("view " + this + " being added, but" + " it already has a parent");
        }
    }

    protected void onAttachedToWindow() {
        if ((this.mPrivateFlags & SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0) {
            this.mParent.requestTransparentRegion(this);
        }
        this.mPrivateFlags3 &= -5;
        jumpDrawablesToCurrentState();
        resetSubtreeAccessibilityStateChanged();
        rebuildOutline();
        if (isFocused()) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null) {
                imm.focusIn(this);
            }
        }
    }

    public boolean resolveRtlPropertiesIfNeeded() {
        if (!needRtlPropertiesResolution()) {
            return DBG;
        }
        if (!isLayoutDirectionResolved()) {
            resolveLayoutDirection();
            resolveLayoutParams();
        }
        if (!isTextDirectionResolved()) {
            resolveTextDirection();
        }
        if (!isTextAlignmentResolved()) {
            resolveTextAlignment();
        }
        if (!areDrawablesResolved()) {
            resolveDrawables();
        }
        if (!isPaddingResolved()) {
            resolvePadding();
        }
        onRtlPropertiesChanged(getLayoutDirection());
        return true;
    }

    public void resetRtlProperties() {
        resetResolvedLayoutDirection();
        resetResolvedTextDirection();
        resetResolvedTextAlignment();
        resetResolvedPadding();
        resetResolvedDrawables();
    }

    void dispatchScreenStateChanged(int screenState) {
        onScreenStateChanged(screenState);
    }

    public void onScreenStateChanged(int screenState) {
    }

    private boolean hasRtlSupport() {
        return this.mContext.getApplicationInfo().hasRtlSupport();
    }

    private boolean isRtlCompatibilityMode() {
        if (getContext().getApplicationInfo().targetSdkVersion < PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK_SHIFT || !hasRtlSupport()) {
            return true;
        }
        return DBG;
    }

    private boolean needRtlPropertiesResolution() {
        return (this.mPrivateFlags2 & ALL_RTL_PROPERTIES_RESOLVED) != ALL_RTL_PROPERTIES_RESOLVED ? true : DBG;
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
    }

    public boolean resolveLayoutDirection() {
        this.mPrivateFlags2 &= -49;
        if (hasRtlSupport()) {
            switch ((this.mPrivateFlags2 & VISIBILITY_MASK) >> TEXT_DIRECTION_ANY_RTL) {
                case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                    this.mPrivateFlags2 |= SCROLL_INDICATOR_START;
                    break;
                case TEXT_DIRECTION_ANY_RTL /*2*/:
                    if (canResolveLayoutDirection()) {
                        try {
                            if (this.mParent.isLayoutDirectionResolved()) {
                                if (this.mParent.getLayoutDirection() == VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                                    this.mPrivateFlags2 |= SCROLL_INDICATOR_START;
                                    break;
                                }
                            }
                            return DBG;
                        } catch (AbstractMethodError e) {
                            Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                            break;
                        }
                    }
                    return DBG;
                    break;
                case TEXT_DIRECTION_LTR /*3*/:
                    if (VIEW_IS_NOT_ADVIEW_OR_DISABLE == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
                        this.mPrivateFlags2 |= SCROLL_INDICATOR_START;
                        break;
                    }
                    break;
            }
        } else if (this.mForceRTL) {
            this.mPrivateFlags2 |= SCROLL_INDICATOR_START;
        }
        this.mPrivateFlags2 |= SCROLL_INDICATOR_END;
        return true;
    }

    public boolean canResolveLayoutDirection() {
        switch (getRawLayoutDirection()) {
            case TEXT_DIRECTION_ANY_RTL /*2*/:
                if (this.mParent != null) {
                    try {
                        return this.mParent.canResolveLayoutDirection();
                    } catch (AbstractMethodError e) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                    }
                }
                return DBG;
            default:
                return true;
        }
    }

    public void resetResolvedLayoutDirection() {
        this.mPrivateFlags2 &= -49;
    }

    public boolean isLayoutDirectionInherited() {
        return getRawLayoutDirection() == TEXT_DIRECTION_ANY_RTL ? true : DBG;
    }

    public boolean isLayoutDirectionResolved() {
        return (this.mPrivateFlags2 & SCROLL_INDICATOR_END) == SCROLL_INDICATOR_END ? true : DBG;
    }

    boolean isPaddingResolved() {
        return (this.mPrivateFlags2 & PFLAG_DOES_NOTHING_REUSE_PLEASE) == PFLAG_DOES_NOTHING_REUSE_PLEASE ? true : DBG;
    }

    public void resolvePadding() {
        int resolvedLayoutDirection = getLayoutDirection();
        if (!isRtlCompatibilityMode()) {
            if (!(this.mBackground == null || (this.mLeftPaddingDefined && this.mRightPaddingDefined))) {
                Rect padding = (Rect) sThreadLocal.get();
                if (padding == null) {
                    padding = new Rect();
                    sThreadLocal.set(padding);
                }
                this.mBackground.getPadding(padding);
                if (!this.mLeftPaddingDefined) {
                    this.mUserPaddingLeftInitial = padding.left;
                }
                if (!this.mRightPaddingDefined) {
                    this.mUserPaddingRightInitial = padding.right;
                }
            }
            switch (resolvedLayoutDirection) {
                case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                    if (this.mUserPaddingStart != UNDEFINED_PADDING) {
                        this.mUserPaddingRight = this.mUserPaddingStart;
                    } else {
                        this.mUserPaddingRight = this.mUserPaddingRightInitial;
                    }
                    if (this.mUserPaddingEnd == UNDEFINED_PADDING) {
                        this.mUserPaddingLeft = this.mUserPaddingLeftInitial;
                        break;
                    } else {
                        this.mUserPaddingLeft = this.mUserPaddingEnd;
                        break;
                    }
                default:
                    if (this.mUserPaddingStart != UNDEFINED_PADDING) {
                        this.mUserPaddingLeft = this.mUserPaddingStart;
                    } else {
                        this.mUserPaddingLeft = this.mUserPaddingLeftInitial;
                    }
                    if (this.mUserPaddingEnd == UNDEFINED_PADDING) {
                        this.mUserPaddingRight = this.mUserPaddingRightInitial;
                        break;
                    } else {
                        this.mUserPaddingRight = this.mUserPaddingEnd;
                        break;
                    }
            }
            this.mUserPaddingBottom = this.mUserPaddingBottom >= 0 ? this.mUserPaddingBottom : this.mPaddingBottom;
        }
        internalSetPadding(this.mUserPaddingLeft, this.mPaddingTop, this.mUserPaddingRight, this.mUserPaddingBottom);
        onRtlPropertiesChanged(resolvedLayoutDirection);
        this.mPrivateFlags2 |= PFLAG_DOES_NOTHING_REUSE_PLEASE;
    }

    public void resetResolvedPadding() {
        resetResolvedPaddingInternal();
    }

    void resetResolvedPaddingInternal() {
        this.mPrivateFlags2 &= -536870913;
    }

    protected void onDetachedFromWindow() {
    }

    protected void onDetachedFromWindowInternal() {
        this.mPrivateFlags &= -67108865;
        this.mPrivateFlags3 &= -5;
        this.mPrivateFlags3 &= -33554433;
        removeUnsetPressCallback();
        removeLongPressCallback();
        removePerformClickCallback();
        removeSendViewScrolledAccessibilityEventCallback();
        stopNestedScroll();
        jumpDrawablesToCurrentState();
        destroyDrawingCache();
        cleanupDraw();
        this.mCurrentAnimation = null;
    }

    private void cleanupDraw() {
        resetDisplayList();
        if (this.mAttachInfo != null) {
            this.mAttachInfo.mViewRootImpl.cancelInvalidate(this);
        }
    }

    void invalidateInheritedLayoutMode(int layoutModeOfRoot) {
    }

    protected int getWindowAttachCount() {
        return this.mWindowAttachCount;
    }

    public IBinder getWindowToken() {
        return this.mAttachInfo != null ? this.mAttachInfo.mWindowToken : null;
    }

    public WindowId getWindowId() {
        if (this.mAttachInfo == null) {
            return null;
        }
        if (this.mAttachInfo.mWindowId == null) {
            try {
                this.mAttachInfo.mIWindowId = this.mAttachInfo.mSession.getWindowId(this.mAttachInfo.mWindowToken);
                this.mAttachInfo.mWindowId = new WindowId(this.mAttachInfo.mIWindowId);
            } catch (RemoteException e) {
            }
        }
        return this.mAttachInfo.mWindowId;
    }

    public IBinder getApplicationWindowToken() {
        AttachInfo ai = this.mAttachInfo;
        if (ai == null) {
            return null;
        }
        IBinder appWindowToken = ai.mPanelParentWindowToken;
        if (appWindowToken == null) {
            appWindowToken = ai.mWindowToken;
        }
        return appWindowToken;
    }

    public Display getDisplay() {
        return this.mAttachInfo != null ? this.mAttachInfo.mDisplay : null;
    }

    IWindowSession getWindowSession() {
        return this.mAttachInfo != null ? this.mAttachInfo.mSession : null;
    }

    int combineVisibility(int vis1, int vis2) {
        return Math.max(vis1, vis2);
    }

    void dispatchAttachedToWindow(AttachInfo info, int visibility) {
        CopyOnWriteArrayList<OnAttachStateChangeListener> listeners = null;
        this.mAttachInfo = info;
        if (this.mOverlay != null) {
            this.mOverlay.getOverlayView().dispatchAttachedToWindow(info, visibility);
        }
        this.mWindowAttachCount += VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        this.mPrivateFlags |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (this.mFloatingTreeObserver != null) {
            info.mTreeObserver.merge(this.mFloatingTreeObserver);
            this.mFloatingTreeObserver = null;
        }
        registerPendingFrameMetricsObservers();
        if ((this.mPrivateFlags & STATUS_BAR_DISABLE_NOTIFICATION_TICKER) != 0) {
            this.mAttachInfo.mScrollContainers.add(this);
            this.mPrivateFlags |= STATUS_BAR_DISABLE_SYSTEM_INFO;
        }
        if (this.mRunQueue != null) {
            this.mRunQueue.executeActions(info.mHandler);
        }
        performCollectViewAttributes(this.mAttachInfo, visibility);
        onAttachedToWindow();
        ListenerInfo li = this.mListenerInfo;
        if (li != null) {
            listeners = li.mOnAttachStateChangeListeners;
        }
        if (listeners != null && listeners.size() > 0) {
            for (OnAttachStateChangeListener listener : listeners) {
                listener.onViewAttachedToWindow(this);
            }
        }
        int vis = info.mWindowVisibility;
        if (vis != STATUS_BAR_TRANSPARENT) {
            onWindowVisibilityChanged(vis);
            if (isShown()) {
                onVisibilityAggregated(vis == 0 ? true : DBG);
            }
        }
        onVisibilityChanged(this, visibility);
        if ((this.mPrivateFlags & SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) != 0) {
            refreshDrawableState();
        }
        needGlobalAttributesUpdate(DBG);
    }

    void dispatchDetachedFromWindow() {
        CopyOnWriteArrayList<OnAttachStateChangeListener> listeners;
        AttachInfo info = this.mAttachInfo;
        if (!(info == null || info.mWindowVisibility == STATUS_BAR_TRANSPARENT)) {
            onWindowVisibilityChanged(STATUS_BAR_TRANSPARENT);
            if (isShown()) {
                onVisibilityAggregated(DBG);
            }
        }
        onDetachedFromWindow();
        onDetachedFromWindowInternal();
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            imm.onViewDetachedFromWindow(this);
        }
        ListenerInfo li = this.mListenerInfo;
        if (li != null) {
            listeners = li.mOnAttachStateChangeListeners;
        } else {
            listeners = null;
        }
        if (listeners != null && listeners.size() > 0) {
            for (OnAttachStateChangeListener listener : listeners) {
                listener.onViewDetachedFromWindow(this);
            }
        }
        if ((this.mPrivateFlags & STATUS_BAR_DISABLE_SYSTEM_INFO) != 0) {
            this.mAttachInfo.mScrollContainers.remove(this);
            this.mPrivateFlags &= -1048577;
        }
        this.mAttachInfo = null;
        if (this.mOverlay != null) {
            this.mOverlay.getOverlayView().dispatchDetachedFromWindow();
        }
    }

    public final void cancelPendingInputEvents() {
        dispatchCancelPendingInputEvents();
    }

    void dispatchCancelPendingInputEvents() {
        this.mPrivateFlags3 &= -17;
        onCancelPendingInputEvents();
        if ((this.mPrivateFlags3 & SCROLL_INDICATOR_START) != SCROLL_INDICATOR_START) {
            throw new SuperNotCalledException("View " + getClass().getSimpleName() + " did not call through to super.onCancelPendingInputEvents()");
        }
    }

    public void onCancelPendingInputEvents() {
        removePerformClickCallback();
        cancelLongPress();
        this.mPrivateFlags3 |= SCROLL_INDICATOR_START;
    }

    public void saveHierarchyState(SparseArray<Parcelable> container) {
        dispatchSaveInstanceState(container);
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        if (this.mID != VIEW_IS_NOT_CHECKED && (this.mViewFlags & TV_PICTURE_IN_PICTURE_VISIBLE) == 0) {
            this.mPrivateFlags &= -131073;
            Parcelable state = onSaveInstanceState();
            if ((this.mPrivateFlags & WILL_NOT_CACHE_DRAWING) == 0) {
                throw new IllegalStateException("Derived class did not call super.onSaveInstanceState()");
            } else if (state != null) {
                container.put(this.mID, state);
            }
        }
    }

    protected Parcelable onSaveInstanceState() {
        this.mPrivateFlags |= WILL_NOT_CACHE_DRAWING;
        if (this.mStartActivityRequestWho == null) {
            return BaseSavedState.EMPTY_STATE;
        }
        BaseSavedState state = new BaseSavedState(AbsSavedState.EMPTY_STATE);
        state.mStartActivityRequestWhoSaved = this.mStartActivityRequestWho;
        return state;
    }

    public void restoreHierarchyState(SparseArray<Parcelable> container) {
        dispatchRestoreInstanceState(container);
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        if (this.mID != VIEW_IS_NOT_CHECKED) {
            Parcelable state = (Parcelable) container.get(this.mID);
            if (state != null) {
                this.mPrivateFlags &= -131073;
                onRestoreInstanceState(state);
                if ((this.mPrivateFlags & WILL_NOT_CACHE_DRAWING) == 0) {
                    throw new IllegalStateException("Derived class did not call super.onRestoreInstanceState()");
                }
            }
        }
    }

    protected void onRestoreInstanceState(Parcelable state) {
        this.mPrivateFlags |= WILL_NOT_CACHE_DRAWING;
        if (state != null && !(state instanceof AbsSavedState)) {
            throw new IllegalArgumentException("Wrong state class, expecting View State but received " + state.getClass().toString() + " instead. This usually happens " + "when two views of different type have the same id in the same hierarchy. " + "This view's id is " + ViewDebug.resolveId(this.mContext, getId()) + ". Make sure " + "other views do not use the same id.");
        } else if (state != null && (state instanceof BaseSavedState)) {
            this.mStartActivityRequestWho = ((BaseSavedState) state).mStartActivityRequestWhoSaved;
        }
    }

    public long getDrawingTime() {
        return this.mAttachInfo != null ? this.mAttachInfo.mDrawingTime : 0;
    }

    public void setDuplicateParentStateEnabled(boolean enabled) {
        setFlags(enabled ? STATUS_BAR_DISABLE_BACK : VISIBLE, STATUS_BAR_DISABLE_BACK);
    }

    public boolean isDuplicateParentStateEnabled() {
        return (this.mViewFlags & STATUS_BAR_DISABLE_BACK) == STATUS_BAR_DISABLE_BACK ? true : DBG;
    }

    public void setLayerType(int layerType, Paint paint) {
        if (layerType < 0 || layerType > TEXT_DIRECTION_ANY_RTL) {
            throw new IllegalArgumentException("Layer type can only be one of: LAYER_TYPE_NONE, LAYER_TYPE_SOFTWARE or LAYER_TYPE_HARDWARE");
        } else if (this.mRenderNode.setLayerType(layerType)) {
            if (layerType != VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                destroyDrawingCache();
            }
            this.mLayerType = layerType;
            if (this.mLayerType == 0) {
                paint = null;
            }
            this.mLayerPaint = paint;
            this.mRenderNode.setLayerPaint(this.mLayerPaint);
            invalidateParentCaches();
            invalidate(true);
        } else {
            setLayerPaint(paint);
        }
    }

    public void setLayerPaint(Paint paint) {
        int layerType = getLayerType();
        if (layerType != 0) {
            this.mLayerPaint = paint;
            if (layerType != TEXT_DIRECTION_ANY_RTL) {
                invalidate();
            } else if (this.mRenderNode.setLayerPaint(paint)) {
                invalidateViewProperty(DBG, DBG);
            }
        }
    }

    public int getLayerType() {
        return this.mLayerType;
    }

    public void buildLayer() {
        if (this.mLayerType != 0) {
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo == null) {
                throw new IllegalStateException("This view must be attached to a window first");
            } else if (getWidth() != 0 && getHeight() != 0) {
                switch (this.mLayerType) {
                    case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                        buildDrawingCache(true);
                        break;
                    case TEXT_DIRECTION_ANY_RTL /*2*/:
                        updateDisplayListIfDirty();
                        if (attachInfo.mHardwareRenderer != null && this.mRenderNode.isValid()) {
                            attachInfo.mHardwareRenderer.buildLayer(this.mRenderNode);
                            break;
                        }
                }
            }
        }
    }

    protected void destroyHardwareResources() {
        resetDisplayList();
    }

    public void setDrawingCacheEnabled(boolean enabled) {
        int i = VISIBLE;
        this.mCachingFailed = DBG;
        if (enabled) {
            i = PFLAG_DRAWING_CACHE_VALID;
        }
        setFlags(i, PFLAG_DRAWING_CACHE_VALID);
    }

    @ExportedProperty(category = "drawing")
    public boolean isDrawingCacheEnabled() {
        return (this.mViewFlags & PFLAG_DRAWING_CACHE_VALID) == PFLAG_DRAWING_CACHE_VALID ? true : DBG;
    }

    public void outputDirtyFlags(String indent, boolean clear, int clearMask) {
        Log.d(VIEW_LOG_TAG, indent + this + "             DIRTY(" + (this.mPrivateFlags & PFLAG_DIRTY_MASK) + ") DRAWN(" + (this.mPrivateFlags & SCROLL_INDICATOR_END) + ")" + " CACHE_VALID(" + (this.mPrivateFlags & PFLAG_DRAWING_CACHE_VALID) + ") INVALIDATED(" + (this.mPrivateFlags & UNDEFINED_PADDING) + ")");
        if (clear) {
            this.mPrivateFlags &= clearMask;
        }
        if (this instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) this;
            int count = parent.getChildCount();
            for (int i = VISIBLE; i < count; i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                parent.getChildAt(i).outputDirtyFlags(indent + "  ", clear, clearMask);
            }
        }
    }

    protected void dispatchGetDisplayList() {
    }

    public boolean canHaveDisplayList() {
        return (this.mAttachInfo == null || this.mAttachInfo.mHardwareRenderer == null) ? DBG : true;
    }

    public RenderNode updateDisplayListIfDirty() {
        RenderNode renderNode = this.mRenderNode;
        if (!canHaveDisplayList()) {
            return renderNode;
        }
        if ((this.mPrivateFlags & PFLAG_DRAWING_CACHE_VALID) != 0 && renderNode.isValid() && !this.mRecreateDisplayList) {
            this.mPrivateFlags |= 32800;
            this.mPrivateFlags &= -6291457;
        } else if (!renderNode.isValid() || this.mRecreateDisplayList) {
            this.mRecreateDisplayList = true;
            int width = this.mRight - this.mLeft;
            int height = this.mBottom - this.mTop;
            int layerType = getLayerType();
            DisplayListCanvas canvas = renderNode.start(width, height);
            canvas.setHighContrastText(this.mAttachInfo.mHighContrastText);
            if (layerType == VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                try {
                    buildDrawingCache(true);
                    Bitmap cache = getDrawingCache(true);
                    if (cache != null) {
                        canvas.drawBitmap(cache, 0.0f, 0.0f, this.mLayerPaint);
                    }
                } catch (Throwable th) {
                    renderNode.end(canvas);
                    setDisplayListProperties(renderNode);
                }
            } else {
                computeScroll();
                canvas.translate((float) (-this.mScrollX), (float) (-this.mScrollY));
                this.mPrivateFlags |= 32800;
                this.mPrivateFlags &= -6291457;
                if ((this.mPrivateFlags & WILL_NOT_DRAW) == WILL_NOT_DRAW) {
                    dispatchDraw(canvas);
                    if (!(this.mOverlay == null || this.mOverlay.isEmpty())) {
                        this.mOverlay.getOverlayView().draw(canvas);
                    }
                } else {
                    draw(canvas);
                }
            }
            renderNode.end(canvas);
            setDisplayListProperties(renderNode);
        } else {
            this.mPrivateFlags |= 32800;
            this.mPrivateFlags &= -6291457;
            dispatchGetDisplayList();
            return renderNode;
        }
        return renderNode;
    }

    private void resetDisplayList() {
        if (this.mRenderNode.isValid()) {
            this.mRenderNode.discardDisplayList();
        }
        if (this.mBackgroundRenderNode != null && this.mBackgroundRenderNode.isValid()) {
            this.mBackgroundRenderNode.discardDisplayList();
        }
    }

    public void onRenderNodeDetached(RenderNode renderNode) {
    }

    public Bitmap getDrawingCache() {
        return getDrawingCache(DBG);
    }

    public Bitmap getDrawingCache(boolean autoScale) {
        if ((this.mViewFlags & WILL_NOT_CACHE_DRAWING) == WILL_NOT_CACHE_DRAWING) {
            return null;
        }
        if ((this.mViewFlags & PFLAG_DRAWING_CACHE_VALID) == PFLAG_DRAWING_CACHE_VALID) {
            buildDrawingCache(autoScale);
        }
        return autoScale ? this.mDrawingCache : this.mUnscaledDrawingCache;
    }

    public void destroyDrawingCache() {
        if (this.mDrawingCache != null) {
            this.mDrawingCache.recycle();
            this.mDrawingCache = null;
        }
        if (this.mUnscaledDrawingCache != null) {
            this.mUnscaledDrawingCache.recycle();
            this.mUnscaledDrawingCache = null;
        }
    }

    public void setDrawingCacheBackgroundColor(int color) {
        if (color != this.mDrawingCacheBackgroundColor) {
            this.mDrawingCacheBackgroundColor = color;
            this.mPrivateFlags &= -32769;
        }
    }

    public int getDrawingCacheBackgroundColor() {
        return this.mDrawingCacheBackgroundColor;
    }

    public void buildDrawingCache() {
        buildDrawingCache(DBG);
    }

    public void buildDrawingCache(boolean autoScale) {
        if ((this.mPrivateFlags & PFLAG_DRAWING_CACHE_VALID) != 0) {
            if (autoScale) {
                if (this.mDrawingCache != null) {
                    return;
                }
            } else if (this.mUnscaledDrawingCache != null) {
                return;
            }
        }
        if (Trace.isTagEnabled(8)) {
            Trace.traceBegin(8, "buildDrawingCache/SW Layer for " + getClass().getSimpleName());
        }
        try {
            buildDrawingCacheImpl(autoScale);
        } finally {
            Trace.traceEnd(8);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void buildDrawingCacheImpl(boolean autoScale) {
        this.mCachingFailed = DBG;
        int width = this.mRight - this.mLeft;
        int height = this.mBottom - this.mTop;
        AttachInfo attachInfo = this.mAttachInfo;
        boolean z = attachInfo != null ? attachInfo.mScalingRequired : DBG;
        if (autoScale && z) {
            width = (int) ((((float) width) * attachInfo.mApplicationScale) + 0.5f);
            height = (int) ((((float) height) * attachInfo.mApplicationScale) + 0.5f);
        }
        ViewRootImpl root = getViewRootImpl();
        int drawingCacheBackgroundColor = (root == null || root.mRogTranslater != null) ? VISIBLE : this.mDrawingCacheBackgroundColor;
        boolean isOpaque = drawingCacheBackgroundColor == 0 ? isOpaque() : true;
        boolean z2 = attachInfo != null ? attachInfo.mUse32BitDrawingCache : DBG;
        int i = width * height;
        int i2 = (!isOpaque || z2) ? TEXT_DIRECTION_RTL : TEXT_DIRECTION_ANY_RTL;
        long projectedBitmapSize = (long) (i2 * i);
        long drawingCacheSize = (long) ViewConfiguration.get(this.mContext).getScaledMaximumDrawingCacheSize();
        if (width <= 0 || height <= 0 || projectedBitmapSize > drawingCacheSize) {
            if (width > 0 && height > 0) {
                Log.w(VIEW_LOG_TAG, getClass().getSimpleName() + " not displayed because it is" + " too large to fit into a software layer (or drawing cache), needs " + projectedBitmapSize + " bytes, only " + drawingCacheSize + " available");
            }
            destroyDrawingCache();
            this.mCachingFailed = true;
            return;
        }
        Canvas canvas;
        boolean clear = true;
        Bitmap bitmap = autoScale ? this.mDrawingCache : this.mUnscaledDrawingCache;
        if (!(bitmap != null && bitmap.getWidth() == width && bitmap.getHeight() == height)) {
            Config quality;
            if (isOpaque) {
                quality = z2 ? Config.ARGB_8888 : Config.RGB_565;
            } else {
                i2 = this.mViewFlags;
                quality = Config.ARGB_8888;
            }
            if (bitmap != null) {
                bitmap.recycle();
            }
            try {
                bitmap = Bitmap.createBitmap(this.mResources.getDisplayMetrics(), width, height, quality);
                if (bitmap == null) {
                    throw new OutOfMemoryError();
                }
                bitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
                if (autoScale) {
                    this.mDrawingCache = bitmap;
                } else {
                    this.mUnscaledDrawingCache = bitmap;
                }
                if (isOpaque && z2) {
                    bitmap.setHasAlpha(DBG);
                }
                if (drawingCacheBackgroundColor != 0) {
                    clear = true;
                } else {
                    clear = DBG;
                }
            } catch (OutOfMemoryError e) {
                if (autoScale) {
                    this.mDrawingCache = null;
                } else {
                    this.mUnscaledDrawingCache = null;
                }
                this.mCachingFailed = true;
                return;
            }
        }
        if (attachInfo != null) {
            canvas = attachInfo.mCanvas;
            if (canvas == null) {
                canvas = new Canvas();
            }
            canvas.setBitmap(bitmap);
            attachInfo.mCanvas = null;
        } else {
            canvas = new Canvas(bitmap);
        }
        if (clear) {
            bitmap.eraseColor(drawingCacheBackgroundColor);
        }
        computeScroll();
        int restoreCount = canvas.save();
        if (autoScale && z) {
            float scale = attachInfo.mApplicationScale;
            canvas.scale(scale, scale);
        }
        canvas.translate((float) (-this.mScrollX), (float) (-this.mScrollY));
        this.mPrivateFlags |= SCROLL_INDICATOR_END;
        if (this.mAttachInfo != null) {
            if (this.mAttachInfo.mHardwareAccelerated) {
            }
        }
        this.mPrivateFlags |= PFLAG_DRAWING_CACHE_VALID;
        i2 = this.mPrivateFlags & WILL_NOT_DRAW;
        if (r0 == WILL_NOT_DRAW) {
            this.mPrivateFlags &= -6291457;
            dispatchDraw(canvas);
            if (this.mOverlay != null) {
                if (!this.mOverlay.isEmpty()) {
                    this.mOverlay.getOverlayView().draw(canvas);
                }
            }
        } else {
            draw(canvas);
        }
        canvas.restoreToCount(restoreCount);
        canvas.setBitmap(null);
        if (attachInfo != null) {
            attachInfo.mCanvas = canvas;
        }
    }

    public Bitmap createSnapshot(Config quality, int backgroundColor, boolean skipChildren) {
        Bitmap bitmap;
        int width = this.mRight - this.mLeft;
        int height = this.mBottom - this.mTop;
        AttachInfo attachInfo = this.mAttachInfo;
        float scale = attachInfo != null ? attachInfo.mApplicationScale : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        width = (int) ((((float) width) * scale) + 0.5f);
        height = (int) ((((float) height) * scale) + 0.5f);
        if (this.mResources != null) {
            DisplayMetrics displayMetrics = this.mResources.getDisplayMetrics();
            if (width <= 0) {
                width = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
            }
            if (height <= 0) {
                height = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
            }
            bitmap = Bitmap.createBitmap(displayMetrics, width, height, quality);
        } else {
            if (width <= 0) {
                width = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
            }
            if (height <= 0) {
                height = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
            }
            bitmap = Bitmap.createBitmap(width, height, quality);
        }
        if (bitmap == null) {
            throw new OutOfMemoryError();
        }
        Canvas canvas;
        Resources resources = getResources();
        if (resources != null) {
            bitmap.setDensity(resources.getDisplayMetrics().densityDpi);
        }
        if (attachInfo != null) {
            canvas = attachInfo.mCanvas;
            if (canvas == null) {
                canvas = new Canvas();
            }
            canvas.setBitmap(bitmap);
            attachInfo.mCanvas = null;
        } else {
            canvas = new Canvas(bitmap);
        }
        if ((MEASURED_STATE_MASK & backgroundColor) != 0) {
            bitmap.eraseColor(backgroundColor);
        }
        computeScroll();
        int restoreCount = canvas.save();
        canvas.scale(scale, scale);
        canvas.translate((float) (-this.mScrollX), (float) (-this.mScrollY));
        int flags = this.mPrivateFlags;
        this.mPrivateFlags &= -6291457;
        if ((this.mPrivateFlags & WILL_NOT_DRAW) == WILL_NOT_DRAW) {
            dispatchDraw(canvas);
            if (!(this.mOverlay == null || this.mOverlay.isEmpty())) {
                this.mOverlay.getOverlayView().draw(canvas);
            }
        } else {
            draw(canvas);
        }
        this.mPrivateFlags = flags;
        canvas.restoreToCount(restoreCount);
        canvas.setBitmap(null);
        if (attachInfo != null) {
            attachInfo.mCanvas = canvas;
        }
        return bitmap;
    }

    public boolean isInEditMode() {
        return DBG;
    }

    protected boolean isPaddingOffsetRequired() {
        return DBG;
    }

    protected int getLeftPaddingOffset() {
        return VISIBLE;
    }

    protected int getRightPaddingOffset() {
        return VISIBLE;
    }

    protected int getTopPaddingOffset() {
        return VISIBLE;
    }

    protected int getBottomPaddingOffset() {
        return VISIBLE;
    }

    protected int getFadeTop(boolean offsetRequired) {
        int top = this.mPaddingTop;
        if (offsetRequired) {
            return top + getTopPaddingOffset();
        }
        return top;
    }

    protected int getFadeHeight(boolean offsetRequired) {
        int padding = this.mPaddingTop;
        if (offsetRequired) {
            padding += getTopPaddingOffset();
        }
        return ((this.mBottom - this.mTop) - this.mPaddingBottom) - padding;
    }

    @ExportedProperty(category = "drawing")
    public boolean isHardwareAccelerated() {
        return this.mAttachInfo != null ? this.mAttachInfo.mHardwareAccelerated : DBG;
    }

    public void setClipBounds(Rect clipBounds) {
        if (clipBounds != this.mClipBounds && (clipBounds == null || !clipBounds.equals(this.mClipBounds))) {
            if (clipBounds == null) {
                this.mClipBounds = null;
            } else if (this.mClipBounds == null) {
                this.mClipBounds = new Rect(clipBounds);
            } else {
                this.mClipBounds.set(clipBounds);
            }
            this.mRenderNode.setClipBounds(this.mClipBounds);
            invalidateViewProperty(DBG, DBG);
        }
    }

    public Rect getClipBounds() {
        return this.mClipBounds != null ? new Rect(this.mClipBounds) : null;
    }

    public boolean getClipBounds(Rect outRect) {
        if (this.mClipBounds == null) {
            return DBG;
        }
        outRect.set(this.mClipBounds);
        return true;
    }

    private boolean applyLegacyAnimation(ViewGroup parent, long drawingTime, Animation a, boolean scalingRequired) {
        Transformation invalidationTransform;
        int flags = parent.mGroupFlags;
        if (!a.isInitialized()) {
            a.initialize(this.mRight - this.mLeft, this.mBottom - this.mTop, parent.getWidth(), parent.getHeight());
            a.initializeInvalidateRegion(VISIBLE, VISIBLE, this.mRight - this.mLeft, this.mBottom - this.mTop);
            if (this.mAttachInfo != null) {
                a.setListenerHandler(this.mAttachInfo.mHandler);
            }
            onAnimationStart();
        }
        Transformation t = parent.getChildTransformation();
        boolean more = a.getTransformation(drawingTime, t, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        if (!scalingRequired || this.mAttachInfo.mApplicationScale == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            invalidationTransform = t;
        } else {
            if (parent.mInvalidationTransformation == null) {
                parent.mInvalidationTransformation = new Transformation();
            }
            invalidationTransform = parent.mInvalidationTransformation;
            a.getTransformation(drawingTime, invalidationTransform, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        }
        if (more) {
            if (a.willChangeBounds()) {
                if (parent.mInvalidateRegion == null) {
                    parent.mInvalidateRegion = new RectF();
                }
                RectF region = parent.mInvalidateRegion;
                a.getInvalidateRegion(VISIBLE, VISIBLE, this.mRight - this.mLeft, this.mBottom - this.mTop, region, invalidationTransform);
                parent.mPrivateFlags |= PFLAG_DRAW_ANIMATION;
                int left = this.mLeft + ((int) region.left);
                int top = this.mTop + ((int) region.top);
                parent.invalidate(left, top, ((int) (region.width() + 0.5f)) + left, ((int) (region.height() + 0.5f)) + top);
            } else if ((flags & LogPower.DISABLE_SENSOR) == WILL_NOT_DRAW) {
                parent.mGroupFlags |= TEXT_DIRECTION_RTL;
            } else if ((flags & TEXT_DIRECTION_RTL) == 0) {
                parent.mPrivateFlags |= PFLAG_DRAW_ANIMATION;
                parent.invalidate(this.mLeft, this.mTop, this.mRight, this.mBottom);
            }
        }
        return more;
    }

    void setDisplayListProperties(RenderNode renderNode) {
        boolean z = DBG;
        if (renderNode != null) {
            renderNode.setHasOverlappingRendering(getHasOverlappingRendering());
            if (this.mParent instanceof ViewGroup) {
                z = ((ViewGroup) this.mParent).getClipChildren();
            }
            renderNode.setClipToBounds(z);
            float alpha = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            if ((this.mParent instanceof ViewGroup) && (((ViewGroup) this.mParent).mGroupFlags & SYSTEM_UI_FLAG_IMMERSIVE) != 0) {
                ViewGroup parentVG = this.mParent;
                Transformation t = parentVG.getChildTransformation();
                if (parentVG.getChildStaticTransformation(this, t)) {
                    int transformType = t.getTransformationType();
                    if (transformType != 0) {
                        if ((transformType & VIEW_IS_NOT_ADVIEW_OR_DISABLE) != 0) {
                            alpha = t.getAlpha();
                        }
                        if ((transformType & TEXT_DIRECTION_ANY_RTL) != 0) {
                            renderNode.setStaticMatrix(t.getMatrix());
                        }
                    }
                }
            }
            if (this.mTransformationInfo != null) {
                alpha *= getFinalAlpha();
                if (alpha < WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL && onSetAlpha((int) (255.0f * alpha))) {
                    alpha = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
                }
                renderNode.setAlpha(alpha);
            } else if (alpha < WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                renderNode.setAlpha(alpha);
            }
        }
    }

    boolean draw(Canvas canvas, ViewGroup parent, long drawingTime) {
        boolean drawingWithRenderNode;
        boolean hardwareAcceleratedCanvas = canvas.isHardwareAccelerated();
        if (this.mAttachInfo == null || !this.mAttachInfo.mHardwareAccelerated) {
            drawingWithRenderNode = DBG;
        } else {
            drawingWithRenderNode = hardwareAcceleratedCanvas;
        }
        boolean z = DBG;
        boolean childHasIdentityMatrix = hasIdentityMatrix();
        int parentFlags = parent.mGroupFlags;
        if ((parentFlags & SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0) {
            parent.getChildTransformation().clear();
            parent.mGroupFlags &= -257;
        }
        Transformation transformToApply = null;
        boolean concatMatrix = DBG;
        boolean z2 = this.mAttachInfo != null ? this.mAttachInfo.mScalingRequired : DBG;
        Animation a = getAnimation();
        if (a != null) {
            z = applyLegacyAnimation(parent, drawingTime, a, z2);
            concatMatrix = a.willChangeTransformationMatrix();
            if (concatMatrix) {
                this.mPrivateFlags3 |= VIEW_IS_NOT_ADVIEW_OR_DISABLE;
            }
            transformToApply = parent.getChildTransformation();
        } else {
            if ((this.mPrivateFlags3 & VIEW_IS_NOT_ADVIEW_OR_DISABLE) != 0) {
                this.mRenderNode.setAnimationMatrix(null);
                this.mPrivateFlags3 &= -2;
            }
            if (!(drawingWithRenderNode || (parentFlags & SYSTEM_UI_FLAG_IMMERSIVE) == 0)) {
                Transformation t = parent.getChildTransformation();
                if (parent.getChildStaticTransformation(this, t)) {
                    int transformType = t.getTransformationType();
                    transformToApply = transformType != 0 ? t : null;
                    concatMatrix = (transformType & TEXT_DIRECTION_ANY_RTL) != 0 ? true : DBG;
                }
            }
        }
        concatMatrix |= childHasIdentityMatrix ? VISIBLE : VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        this.mPrivateFlags |= SCROLL_INDICATOR_END;
        if (!concatMatrix && (parentFlags & GL10.GL_EXP2) == VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
            if (canvas.quickReject((float) this.mLeft, (float) this.mTop, (float) this.mRight, (float) this.mBottom, EdgeType.BW) && (this.mPrivateFlags & PFLAG_DRAW_ANIMATION) == 0) {
                this.mPrivateFlags2 |= STATUS_BAR_UNHIDE;
                return z;
            }
        }
        this.mPrivateFlags2 &= -268435457;
        if (hardwareAcceleratedCanvas) {
            this.mRecreateDisplayList = (this.mPrivateFlags & UNDEFINED_PADDING) != 0 ? true : DBG;
            this.mPrivateFlags &= HwBootFail.STAGE_BOOT_SUCCESS;
        }
        RenderNode renderNode = null;
        Bitmap cache = null;
        int layerType = getLayerType();
        if (layerType == VIEW_IS_NOT_ADVIEW_OR_DISABLE || !drawingWithRenderNode) {
            if (layerType != 0) {
                layerType = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
                buildDrawingCache(true);
            }
            cache = getDrawingCache(true);
        }
        if (drawingWithRenderNode) {
            renderNode = updateDisplayListIfDirty();
            if (!renderNode.isValid()) {
                renderNode = null;
                drawingWithRenderNode = DBG;
            }
        }
        int sx = VISIBLE;
        int sy = VISIBLE;
        if (!drawingWithRenderNode) {
            computeScroll();
            sx = this.mScrollX;
            sy = this.mScrollY;
        }
        boolean drawingWithDrawingCache = (cache == null || drawingWithRenderNode) ? DBG : true;
        boolean offsetForScroll = (cache != null || drawingWithRenderNode) ? DBG : true;
        int restoreTo = VIEW_IS_NOT_CHECKED;
        if (!(drawingWithRenderNode && transformToApply == null)) {
            restoreTo = canvas.save();
        }
        if (offsetForScroll) {
            canvas.translate((float) (this.mLeft - sx), (float) (this.mTop - sy));
        } else {
            if (!drawingWithRenderNode) {
                canvas.translate((float) this.mLeft, (float) this.mTop);
            }
            if (z2) {
                if (drawingWithRenderNode) {
                    restoreTo = canvas.save();
                }
                float scale = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL / this.mAttachInfo.mApplicationScale;
                canvas.scale(scale, scale);
            }
        }
        float alpha = drawingWithRenderNode ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL : getAlpha() * getTransitionAlpha();
        if (transformToApply != null || alpha < WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL || !hasIdentityMatrix() || (this.mPrivateFlags3 & TEXT_DIRECTION_ANY_RTL) != 0) {
            if (!(transformToApply == null && childHasIdentityMatrix)) {
                int transX = VISIBLE;
                int transY = VISIBLE;
                if (offsetForScroll) {
                    transX = -sx;
                    transY = -sy;
                }
                if (transformToApply != null) {
                    if (concatMatrix) {
                        if (drawingWithRenderNode) {
                            renderNode.setAnimationMatrix(transformToApply.getMatrix());
                        } else {
                            canvas.translate((float) (-transX), (float) (-transY));
                            canvas.concat(transformToApply.getMatrix());
                            canvas.translate((float) transX, (float) transY);
                        }
                        parent.mGroupFlags |= SYSTEM_UI_FLAG_LAYOUT_STABLE;
                    }
                    float transformAlpha = transformToApply.getAlpha();
                    if (transformAlpha < WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                        alpha *= transformAlpha;
                        parent.mGroupFlags |= SYSTEM_UI_FLAG_LAYOUT_STABLE;
                    }
                }
                if (!(childHasIdentityMatrix || drawingWithRenderNode)) {
                    canvas.translate((float) (-transX), (float) (-transY));
                    canvas.concat(getMatrix());
                    canvas.translate((float) transX, (float) transY);
                }
            }
            if (alpha < WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL || (this.mPrivateFlags3 & TEXT_DIRECTION_ANY_RTL) != 0) {
                if (alpha < WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                    this.mPrivateFlags3 |= TEXT_DIRECTION_ANY_RTL;
                } else {
                    this.mPrivateFlags3 &= -3;
                }
                parent.mGroupFlags |= SYSTEM_UI_FLAG_LAYOUT_STABLE;
                if (!drawingWithDrawingCache) {
                    int multipliedAlpha = (int) (255.0f * alpha);
                    if (onSetAlpha(multipliedAlpha)) {
                        this.mPrivateFlags |= STATUS_BAR_DISABLE_NOTIFICATION_ALERTS;
                    } else if (drawingWithRenderNode) {
                        renderNode.setAlpha((getAlpha() * alpha) * getTransitionAlpha());
                    } else if (layerType == 0) {
                        canvas.saveLayerAlpha((float) sx, (float) sy, (float) (getWidth() + sx), (float) (getHeight() + sy), multipliedAlpha);
                    }
                }
            }
        } else if ((this.mPrivateFlags & STATUS_BAR_DISABLE_NOTIFICATION_ALERTS) == STATUS_BAR_DISABLE_NOTIFICATION_ALERTS) {
            onSetAlpha(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
            this.mPrivateFlags &= -262145;
        }
        if (!drawingWithRenderNode) {
            if ((parentFlags & VIEW_IS_NOT_ADVIEW_OR_DISABLE) != 0 && cache == null) {
                if (offsetForScroll) {
                    canvas.clipRect(sx, sy, getWidth() + sx, getHeight() + sy);
                } else if (!z2 || cache == null) {
                    canvas.clipRect(VISIBLE, VISIBLE, getWidth(), getHeight());
                } else {
                    canvas.clipRect(VISIBLE, VISIBLE, cache.getWidth(), cache.getHeight());
                }
            }
            if (this.mClipBounds != null) {
                canvas.clipRect(this.mClipBounds);
            }
        }
        if (drawingWithDrawingCache) {
            if (cache != null) {
                this.mPrivateFlags &= -6291457;
                if (layerType == 0 || this.mLayerPaint == null) {
                    Paint cachePaint = parent.mCachePaint;
                    if (cachePaint == null) {
                        cachePaint = new Paint();
                        cachePaint.setDither(DBG);
                        parent.mCachePaint = cachePaint;
                    }
                    cachePaint.setAlpha((int) (255.0f * alpha));
                    canvas.drawBitmap(cache, 0.0f, 0.0f, cachePaint);
                } else {
                    int layerPaintAlpha = this.mLayerPaint.getAlpha();
                    if (alpha < WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                        this.mLayerPaint.setAlpha((int) (((float) layerPaintAlpha) * alpha));
                    }
                    canvas.drawBitmap(cache, 0.0f, 0.0f, this.mLayerPaint);
                    if (alpha < WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
                        this.mLayerPaint.setAlpha(layerPaintAlpha);
                    }
                }
            }
        } else if (drawingWithRenderNode) {
            this.mPrivateFlags &= -6291457;
            ((DisplayListCanvas) canvas).drawRenderNode(renderNode);
        } else if ((this.mPrivateFlags & WILL_NOT_DRAW) == WILL_NOT_DRAW) {
            this.mPrivateFlags &= -6291457;
            dispatchDraw(canvas);
        } else {
            draw(canvas);
        }
        if (restoreTo >= 0) {
            canvas.restoreToCount(restoreTo);
        }
        if (!(a == null || z)) {
            if (!(hardwareAcceleratedCanvas || a.getFillAfter())) {
                onSetAlpha(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
            }
            parent.finishAnimatingView(this, a);
        }
        if (z && hardwareAcceleratedCanvas && a.hasAlpha() && (this.mPrivateFlags & STATUS_BAR_DISABLE_NOTIFICATION_ALERTS) == STATUS_BAR_DISABLE_NOTIFICATION_ALERTS) {
            invalidate(true);
        }
        this.mRecreateDisplayList = DBG;
        return z;
    }

    public void draw(Canvas canvas) {
        int privateFlags = this.mPrivateFlags;
        boolean dirtyOpaque = (PFLAG_DIRTY_MASK & privateFlags) == STATUS_BAR_DISABLE_BACK ? (this.mAttachInfo == null || !this.mAttachInfo.mIgnoreDirtyState) ? true : DBG : DBG;
        this.mPrivateFlags = (-6291457 & privateFlags) | SCROLL_INDICATOR_END;
        if (!dirtyOpaque) {
            drawBackground(canvas);
        }
        int viewFlags = this.mViewFlags;
        boolean horizontalEdges = (viewFlags & SYSTEM_UI_FLAG_IMMERSIVE_STICKY) != 0 ? true : DBG;
        boolean verticalEdges = (viewFlags & SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0 ? true : DBG;
        if (verticalEdges || horizontalEdges) {
            boolean drawTop = DBG;
            boolean drawBottom = DBG;
            boolean drawLeft = DBG;
            boolean drawRight = DBG;
            float topFadeStrength = 0.0f;
            float bottomFadeStrength = 0.0f;
            float leftFadeStrength = 0.0f;
            float rightFadeStrength = 0.0f;
            int paddingLeft = this.mPaddingLeft;
            boolean offsetRequired = isPaddingOffsetRequired();
            if (offsetRequired) {
                paddingLeft += getLeftPaddingOffset();
            }
            int left = this.mScrollX + paddingLeft;
            int right = (((this.mRight + left) - this.mLeft) - this.mPaddingRight) - paddingLeft;
            int top = this.mScrollY + getFadeTop(offsetRequired);
            int bottom = top + getFadeHeight(offsetRequired);
            if (offsetRequired) {
                right += getRightPaddingOffset();
                bottom += getBottomPaddingOffset();
            }
            ScrollabilityCache scrollabilityCache = this.mScrollCache;
            float fadeHeight = (float) scrollabilityCache.fadingEdgeLength;
            int length = (int) fadeHeight;
            if (verticalEdges && top + length > bottom - length) {
                length = (bottom - top) / TEXT_DIRECTION_ANY_RTL;
            }
            if (horizontalEdges && left + length > right - length) {
                length = (right - left) / TEXT_DIRECTION_ANY_RTL;
            }
            if (verticalEdges) {
                topFadeStrength = Math.max(0.0f, Math.min(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, getTopFadingEdgeStrength()));
                drawTop = topFadeStrength * fadeHeight > WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL ? true : DBG;
                bottomFadeStrength = Math.max(0.0f, Math.min(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, getBottomFadingEdgeStrength()));
                drawBottom = bottomFadeStrength * fadeHeight > WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL ? true : DBG;
            }
            if (horizontalEdges) {
                leftFadeStrength = Math.max(0.0f, Math.min(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, getLeftFadingEdgeStrength()));
                drawLeft = leftFadeStrength * fadeHeight > WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL ? true : DBG;
                rightFadeStrength = Math.max(0.0f, Math.min(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, getRightFadingEdgeStrength()));
                drawRight = rightFadeStrength * fadeHeight > WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL ? true : DBG;
            }
            int saveCount = canvas.getSaveCount();
            int solidColor = getSolidColor();
            if (solidColor == 0) {
                if (drawTop) {
                    canvas.saveLayer((float) left, (float) top, (float) right, (float) (top + length), null, TEXT_DIRECTION_RTL);
                }
                if (drawBottom) {
                    canvas.saveLayer((float) left, (float) (bottom - length), (float) right, (float) bottom, null, TEXT_DIRECTION_RTL);
                }
                if (drawLeft) {
                    canvas.saveLayer((float) left, (float) top, (float) (left + length), (float) bottom, null, TEXT_DIRECTION_RTL);
                }
                if (drawRight) {
                    canvas.saveLayer((float) (right - length), (float) top, (float) right, (float) bottom, null, TEXT_DIRECTION_RTL);
                }
            } else {
                scrollabilityCache.setFadeColor(solidColor);
            }
            if (!dirtyOpaque) {
                onDraw(canvas);
            }
            dispatchDraw(canvas);
            Paint p = scrollabilityCache.paint;
            Matrix matrix = scrollabilityCache.matrix;
            Shader fade = scrollabilityCache.shader;
            if (drawTop) {
                matrix.setScale(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, fadeHeight * topFadeStrength);
                matrix.postTranslate((float) left, (float) top);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                canvas.drawRect((float) left, (float) top, (float) right, (float) (top + length), p);
            }
            if (drawBottom) {
                matrix.setScale(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, fadeHeight * bottomFadeStrength);
                matrix.postRotate(180.0f);
                matrix.postTranslate((float) left, (float) bottom);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                canvas.drawRect((float) left, (float) (bottom - length), (float) right, (float) bottom, p);
            }
            if (drawLeft) {
                matrix.setScale(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, fadeHeight * leftFadeStrength);
                matrix.postRotate(-90.0f);
                matrix.postTranslate((float) left, (float) top);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                canvas.drawRect((float) left, (float) top, (float) (left + length), (float) bottom, p);
            }
            if (drawRight) {
                matrix.setScale(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL, fadeHeight * rightFadeStrength);
                matrix.postRotate(90.0f);
                matrix.postTranslate((float) right, (float) top);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                canvas.drawRect((float) (right - length), (float) top, (float) right, (float) bottom, p);
            }
            canvas.restoreToCount(saveCount);
            if (!(this.mOverlay == null || this.mOverlay.isEmpty())) {
                this.mOverlay.getOverlayView().dispatchDraw(canvas);
            }
            onDrawForeground(canvas);
            return;
        }
        if (!dirtyOpaque) {
            onDraw(canvas);
        }
        dispatchDraw(canvas);
        if (!(this.mOverlay == null || this.mOverlay.isEmpty())) {
            this.mOverlay.getOverlayView().dispatchDraw(canvas);
        }
        onDrawForeground(canvas);
    }

    private void drawBackground(Canvas canvas) {
        Drawable background = this.mBackground;
        if (background != null) {
            setBackgroundBounds();
            if (!(!canvas.isHardwareAccelerated() || this.mAttachInfo == null || this.mAttachInfo.mHardwareRenderer == null)) {
                this.mBackgroundRenderNode = getDrawableRenderNode(background, this.mBackgroundRenderNode);
                RenderNode renderNode = this.mBackgroundRenderNode;
                if (renderNode != null && renderNode.isValid()) {
                    setBackgroundRenderNodeProperties(renderNode);
                    ((DisplayListCanvas) canvas).drawRenderNode(renderNode);
                    return;
                }
            }
            int scrollX = this.mScrollX;
            int scrollY = this.mScrollY;
            if ((scrollX | scrollY) == 0) {
                background.draw(canvas);
            } else {
                canvas.translate((float) scrollX, (float) scrollY);
                background.draw(canvas);
                canvas.translate((float) (-scrollX), (float) (-scrollY));
            }
        }
    }

    void setBackgroundBounds() {
        if (this.mBackgroundSizeChanged && this.mBackground != null) {
            this.mBackground.setBounds(VISIBLE, VISIBLE, this.mRight - this.mLeft, this.mBottom - this.mTop);
            this.mBackgroundSizeChanged = DBG;
            rebuildOutline();
        }
    }

    private void setBackgroundRenderNodeProperties(RenderNode renderNode) {
        renderNode.setTranslationX((float) this.mScrollX);
        renderNode.setTranslationY((float) this.mScrollY);
    }

    private RenderNode getDrawableRenderNode(Drawable drawable, RenderNode renderNode) {
        if (renderNode == null) {
            renderNode = RenderNode.create(drawable.getClass().getName(), this);
        }
        Rect bounds = drawable.getBounds();
        DisplayListCanvas canvas = renderNode.start(bounds.width(), bounds.height());
        canvas.translate((float) (-bounds.left), (float) (-bounds.top));
        try {
            drawable.draw(canvas);
            renderNode.setLeftTopRightBottom(bounds.left, bounds.top, bounds.right, bounds.bottom);
            renderNode.setProjectBackwards(drawable.isProjected());
            renderNode.setProjectionReceiver(true);
            renderNode.setClipToBounds(DBG);
            return renderNode;
        } finally {
            renderNode.end(canvas);
        }
    }

    public ViewOverlay getOverlay() {
        if (this.mOverlay == null) {
            this.mOverlay = new ViewOverlay(this.mContext, this);
        }
        return this.mOverlay;
    }

    @ExportedProperty(category = "drawing")
    public int getSolidColor() {
        return VISIBLE;
    }

    private static String printFlags(int flags) {
        String output = "";
        int numFlags = VISIBLE;
        if ((flags & VIEW_IS_NOT_ADVIEW_OR_DISABLE) == VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
            output = output + "TAKES_FOCUS";
            numFlags = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
        switch (flags & VISIBILITY_MASK) {
            case TEXT_DIRECTION_RTL /*4*/:
                if (numFlags > 0) {
                    output = output + " ";
                }
                return output + "INVISIBLE";
            case STATUS_BAR_TRANSPARENT /*8*/:
                if (numFlags > 0) {
                    output = output + " ";
                }
                return output + "GONE";
            default:
                return output;
        }
    }

    private static String printPrivateFlags(int privateFlags) {
        String output = "";
        int numFlags = VISIBLE;
        if ((privateFlags & VIEW_IS_NOT_ADVIEW_OR_DISABLE) == VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
            output = output + "WANTS_FOCUS";
            numFlags = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
        if ((privateFlags & TEXT_DIRECTION_ANY_RTL) == TEXT_DIRECTION_ANY_RTL) {
            if (numFlags > 0) {
                output = output + " ";
            }
            output = output + "FOCUSED";
            numFlags += VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
        if ((privateFlags & TEXT_DIRECTION_RTL) == TEXT_DIRECTION_RTL) {
            if (numFlags > 0) {
                output = output + " ";
            }
            output = output + "SELECTED";
            numFlags += VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
        if ((privateFlags & STATUS_BAR_TRANSPARENT) == STATUS_BAR_TRANSPARENT) {
            if (numFlags > 0) {
                output = output + " ";
            }
            output = output + "IS_ROOT_NAMESPACE";
            numFlags += VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
        if ((privateFlags & SCROLL_INDICATOR_START) == SCROLL_INDICATOR_START) {
            if (numFlags > 0) {
                output = output + " ";
            }
            output = output + "HAS_BOUNDS";
            numFlags += VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
        if ((privateFlags & SCROLL_INDICATOR_END) != SCROLL_INDICATOR_END) {
            return output;
        }
        if (numFlags > 0) {
            output = output + " ";
        }
        return output + "DRAWN";
    }

    public boolean isLayoutRequested() {
        return (this.mPrivateFlags & SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == SYSTEM_UI_FLAG_IMMERSIVE_STICKY ? true : DBG;
    }

    public static boolean isLayoutModeOptical(Object o) {
        return o instanceof ViewGroup ? ((ViewGroup) o).isLayoutModeOptical() : DBG;
    }

    private boolean setOpticalFrame(int left, int top, int right, int bottom) {
        Insets parentInsets = this.mParent instanceof View ? ((View) this.mParent).getOpticalInsets() : Insets.NONE;
        Insets childInsets = getOpticalInsets();
        return setFrame((parentInsets.left + left) - childInsets.left, (parentInsets.top + top) - childInsets.top, (parentInsets.left + right) + childInsets.right, (parentInsets.top + bottom) + childInsets.bottom);
    }

    public void layout(int l, int t, int r, int b) {
        if ((this.mPrivateFlags3 & STATUS_BAR_TRANSPARENT) != 0) {
            onMeasure(this.mOldWidthMeasureSpec, this.mOldHeightMeasureSpec);
            this.mPrivateFlags3 &= -9;
        }
        int oldL = this.mLeft;
        int oldT = this.mTop;
        int oldB = this.mBottom;
        int oldR = this.mRight;
        boolean changed = isLayoutModeOptical(this.mParent) ? setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);
        if (changed || (this.mPrivateFlags & SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) == SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) {
            onLayout(changed, l, t, r, b);
            this.mPrivateFlags &= -8193;
            ListenerInfo li = this.mListenerInfo;
            if (!(li == null || li.mOnLayoutChangeListeners == null)) {
                ArrayList<OnLayoutChangeListener> listenersCopy = (ArrayList) li.mOnLayoutChangeListeners.clone();
                int numListeners = listenersCopy.size();
                for (int i = VISIBLE; i < numListeners; i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                    ((OnLayoutChangeListener) listenersCopy.get(i)).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
                }
            }
        }
        this.mPrivateFlags &= -4097;
        this.mPrivateFlags3 |= TEXT_DIRECTION_RTL;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = DBG;
        if (this.mLeft == left && this.mRight == right && this.mTop == top) {
            if (this.mBottom != bottom) {
            }
            return changed;
        }
        changed = true;
        int drawn = this.mPrivateFlags & SCROLL_INDICATOR_END;
        int oldWidth = this.mRight - this.mLeft;
        int oldHeight = this.mBottom - this.mTop;
        int newWidth = right - left;
        int newHeight = bottom - top;
        boolean sizeChanged = (newWidth == oldWidth && newHeight == oldHeight) ? DBG : true;
        invalidate(sizeChanged);
        this.mLeft = left;
        this.mTop = top;
        this.mRight = right;
        this.mBottom = bottom;
        this.mRenderNode.setLeftTopRightBottom(this.mLeft, this.mTop, this.mRight, this.mBottom);
        this.mPrivateFlags |= SCROLL_INDICATOR_START;
        if (sizeChanged) {
            sizeChange(newWidth, newHeight, oldWidth, oldHeight);
        }
        if ((this.mViewFlags & VISIBILITY_MASK) == 0 || this.mGhostView != null) {
            this.mPrivateFlags |= SCROLL_INDICATOR_END;
            invalidate(sizeChanged);
            invalidateParentCaches();
        }
        this.mPrivateFlags |= drawn;
        this.mBackgroundSizeChanged = true;
        if (this.mForegroundInfo != null) {
            this.mForegroundInfo.mBoundsChanged = true;
        }
        notifySubtreeAccessibilityStateChangedIfNeeded();
        return changed;
    }

    public void setLeftTopRightBottom(int left, int top, int right, int bottom) {
        setFrame(left, top, right, bottom);
    }

    private void sizeChange(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
        if (this.mOverlay != null) {
            this.mOverlay.getOverlayView().setRight(newWidth);
            this.mOverlay.getOverlayView().setBottom(newHeight);
        }
        rebuildOutline();
    }

    protected void onFinishInflate() {
    }

    public Resources getResources() {
        return this.mResources;
    }

    public void invalidateDrawable(Drawable drawable) {
        if (verifyDrawable(drawable)) {
            Rect dirty = drawable.getDirtyBounds();
            int scrollX = this.mScrollX;
            int scrollY = this.mScrollY;
            invalidate(dirty.left + scrollX, dirty.top + scrollY, dirty.right + scrollX, dirty.bottom + scrollY);
            rebuildOutline();
        }
    }

    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (verifyDrawable(who) && what != null) {
            long delay = when - SystemClock.uptimeMillis();
            if (this.mAttachInfo != null) {
                this.mAttachInfo.mViewRootImpl.mChoreographer.postCallbackDelayed(VIEW_IS_NOT_ADVIEW_OR_DISABLE, what, who, Choreographer.subtractFrameDelay(delay));
                return;
            }
            getRunQueue().postDelayed(what, delay);
        }
    }

    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (verifyDrawable(who) && what != null) {
            if (this.mAttachInfo != null) {
                this.mAttachInfo.mViewRootImpl.mChoreographer.removeCallbacks(VIEW_IS_NOT_ADVIEW_OR_DISABLE, what, who);
            }
            getRunQueue().removeCallbacks(what);
        }
    }

    public void unscheduleDrawable(Drawable who) {
        if (this.mAttachInfo != null && who != null) {
            this.mAttachInfo.mViewRootImpl.mChoreographer.removeCallbacks(VIEW_IS_NOT_ADVIEW_OR_DISABLE, null, who);
        }
    }

    protected void resolveDrawables() {
        if (isLayoutDirectionResolved() || getRawLayoutDirection() != TEXT_DIRECTION_ANY_RTL) {
            int layoutDirection = isLayoutDirectionResolved() ? getLayoutDirection() : getRawLayoutDirection();
            if (this.mBackground != null) {
                this.mBackground.setLayoutDirection(layoutDirection);
            }
            if (!(this.mForegroundInfo == null || this.mForegroundInfo.mDrawable == null)) {
                this.mForegroundInfo.mDrawable.setLayoutDirection(layoutDirection);
            }
            this.mPrivateFlags2 |= STATUS_BAR_TRANSLUCENT;
            onResolveDrawables(layoutDirection);
        }
    }

    boolean areDrawablesResolved() {
        return (this.mPrivateFlags2 & STATUS_BAR_TRANSLUCENT) == STATUS_BAR_TRANSLUCENT ? true : DBG;
    }

    public void onResolveDrawables(int layoutDirection) {
    }

    protected void resetResolvedDrawables() {
        resetResolvedDrawablesInternal();
    }

    void resetResolvedDrawablesInternal() {
        this.mPrivateFlags2 &= -1073741825;
    }

    protected boolean verifyDrawable(Drawable who) {
        if (who != this.mBackground) {
            return (this.mForegroundInfo == null || this.mForegroundInfo.mDrawable != who) ? DBG : true;
        } else {
            return true;
        }
    }

    protected void drawableStateChanged() {
        int i = VISIBLE;
        Drawable fg = null;
        int[] state = getDrawableState();
        int changed = VISIBLE;
        Drawable bg = this.mBackground;
        if (bg != null && bg.isStateful()) {
            changed = bg.setState(state);
        }
        if (this.mForegroundInfo != null) {
            fg = this.mForegroundInfo.mDrawable;
        }
        if (fg != null && fg.isStateful()) {
            changed |= fg.setState(state);
        }
        if (this.mScrollCache != null) {
            Drawable scrollBar = this.mScrollCache.scrollBar;
            if (scrollBar != null && scrollBar.isStateful()) {
                if (scrollBar.setState(state) && this.mScrollCache.state != 0) {
                    i = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
                }
                changed |= i;
            }
        }
        if (this.mStateListAnimator != null) {
            this.mStateListAnimator.setState(state);
        }
        if (this.mHwStateListAnimator != null && isEnabled()) {
            this.mHwStateListAnimator.setState(getDrawableState());
        }
        if (changed != 0) {
            invalidate();
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        if (this.mBackground != null) {
            this.mBackground.setHotspot(x, y);
        }
        if (!(this.mForegroundInfo == null || this.mForegroundInfo.mDrawable == null)) {
            this.mForegroundInfo.mDrawable.setHotspot(x, y);
        }
        dispatchDrawableHotspotChanged(x, y);
    }

    public void dispatchDrawableHotspotChanged(float x, float y) {
    }

    public void refreshDrawableState() {
        this.mPrivateFlags |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        drawableStateChanged();
        ViewParent parent = this.mParent;
        if (parent != null) {
            parent.childDrawableStateChanged(this);
        }
    }

    public final int[] getDrawableState() {
        if (this.mDrawableState != null && (this.mPrivateFlags & SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) == 0) {
            return this.mDrawableState;
        }
        this.mDrawableState = onCreateDrawableState(VISIBLE);
        this.mPrivateFlags &= -1025;
        return this.mDrawableState;
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        if ((this.mViewFlags & STATUS_BAR_DISABLE_BACK) == STATUS_BAR_DISABLE_BACK && (this.mParent instanceof View)) {
            return ((View) this.mParent).onCreateDrawableState(extraSpace);
        }
        int privateFlags = this.mPrivateFlags;
        int viewStateIndex = VISIBLE;
        if ((privateFlags & RECENT_APPS_VISIBLE) != 0) {
            viewStateIndex = SCROLL_INDICATOR_START;
        }
        if ((this.mViewFlags & SCROLL_INDICATOR_END) == 0) {
            viewStateIndex |= STATUS_BAR_TRANSPARENT;
        }
        if (isFocused()) {
            viewStateIndex |= TEXT_DIRECTION_RTL;
        }
        if ((privateFlags & TEXT_DIRECTION_RTL) != 0) {
            viewStateIndex |= TEXT_DIRECTION_ANY_RTL;
        }
        if (hasWindowFocus()) {
            viewStateIndex |= VIEW_IS_NOT_ADVIEW_OR_DISABLE;
        }
        if ((STATUS_BAR_TRANSLUCENT & privateFlags) != 0) {
            viewStateIndex |= SCROLL_INDICATOR_END;
        }
        if (this.mAttachInfo != null && this.mAttachInfo.mHardwareAccelerationRequested && ThreadedRenderer.isAvailable()) {
            viewStateIndex |= PFLAG_DRAW_ANIMATION;
        }
        if ((STATUS_BAR_UNHIDE & privateFlags) != 0) {
            viewStateIndex |= WILL_NOT_DRAW;
        }
        int privateFlags2 = this.mPrivateFlags2;
        if ((privateFlags2 & VIEW_IS_NOT_ADVIEW_OR_DISABLE) != 0) {
            viewStateIndex |= SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }
        if ((privateFlags2 & TEXT_DIRECTION_ANY_RTL) != 0) {
            viewStateIndex |= SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        int[] drawableState = StateSet.get(viewStateIndex);
        if (extraSpace == 0) {
            return drawableState;
        }
        int[] fullState;
        if (drawableState != null) {
            fullState = new int[(drawableState.length + extraSpace)];
            System.arraycopy(drawableState, VISIBLE, fullState, VISIBLE, drawableState.length);
        } else {
            fullState = new int[extraSpace];
        }
        return fullState;
    }

    protected static int[] mergeDrawableStates(int[] baseState, int[] additionalState) {
        int i = baseState.length + VIEW_IS_NOT_CHECKED;
        while (i >= 0 && baseState[i] == 0) {
            i += VIEW_IS_NOT_CHECKED;
        }
        System.arraycopy(additionalState, VISIBLE, baseState, i + VIEW_IS_NOT_ADVIEW_OR_DISABLE, additionalState.length);
        return baseState;
    }

    public void jumpDrawablesToCurrentState() {
        if (this.mBackground != null) {
            this.mBackground.jumpToCurrentState();
        }
        if (this.mStateListAnimator != null) {
            this.mStateListAnimator.jumpToCurrentState();
        }
        if (this.mHwStateListAnimator != null && isEnabled()) {
            this.mHwStateListAnimator.jumpToCurrentState();
        }
        if (this.mForegroundInfo != null && this.mForegroundInfo.mDrawable != null) {
            this.mForegroundInfo.mDrawable.jumpToCurrentState();
        }
    }

    @RemotableViewMethod
    public void setBackgroundColor(int color) {
        if (this.mBackground instanceof ColorDrawable) {
            ((ColorDrawable) this.mBackground.mutate()).setColor(color);
            computeOpaqueFlags();
            this.mBackgroundResource = VISIBLE;
            return;
        }
        setBackground(new ColorDrawable(color));
    }

    @RemotableViewMethod
    public void setBackgroundResource(int resid) {
        if (resid == 0 || resid != this.mBackgroundResource) {
            Drawable d = null;
            if (resid != 0) {
                d = this.mContext.getDrawable(resid);
            }
            setBackground(d);
            this.mBackgroundResource = resid;
        }
    }

    public void setBackground(Drawable background) {
        setBackgroundDrawable(background);
    }

    @Deprecated
    public void setBackgroundDrawable(Drawable background) {
        computeOpaqueFlags();
        if (background != this.mBackground) {
            boolean requestLayout = DBG;
            this.mBackgroundResource = VISIBLE;
            if (this.mBackground != null) {
                if (isAttachedToWindow()) {
                    this.mBackground.setVisible(DBG, DBG);
                }
                this.mBackground.setCallback(null);
                unscheduleDrawable(this.mBackground);
            }
            if (background != null) {
                Rect padding = (Rect) sThreadLocal.get();
                if (padding == null) {
                    padding = new Rect();
                    sThreadLocal.set(padding);
                }
                resetResolvedDrawablesInternal();
                background.setLayoutDirection(getLayoutDirection());
                if (background.getPadding(padding)) {
                    resetResolvedPaddingInternal();
                    switch (background.getLayoutDirection()) {
                        case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                            this.mUserPaddingLeftInitial = padding.right;
                            this.mUserPaddingRightInitial = padding.left;
                            internalSetPadding(padding.right, padding.top, padding.left, padding.bottom);
                            break;
                        default:
                            this.mUserPaddingLeftInitial = padding.left;
                            this.mUserPaddingRightInitial = padding.right;
                            internalSetPadding(padding.left, padding.top, padding.right, padding.bottom);
                            break;
                    }
                    this.mLeftPaddingDefined = DBG;
                    this.mRightPaddingDefined = DBG;
                }
                if (this.mBackground != null && this.mBackground.getMinimumHeight() == background.getMinimumHeight()) {
                    if (this.mBackground.getMinimumWidth() != background.getMinimumWidth()) {
                    }
                    this.mBackground = background;
                    if (background.isStateful()) {
                        background.setState(getDrawableState());
                    }
                    if (isAttachedToWindow()) {
                        background.setVisible(getWindowVisibility() != 0 ? isShown() : DBG, DBG);
                    }
                    applyBackgroundTint();
                    background.setCallback(this);
                    if ((this.mPrivateFlags & WILL_NOT_DRAW) != 0) {
                        this.mPrivateFlags &= -129;
                        requestLayout = true;
                    }
                }
                requestLayout = true;
                this.mBackground = background;
                if (background.isStateful()) {
                    background.setState(getDrawableState());
                }
                if (isAttachedToWindow()) {
                    if (getWindowVisibility() != 0) {
                    }
                    background.setVisible(getWindowVisibility() != 0 ? isShown() : DBG, DBG);
                }
                applyBackgroundTint();
                background.setCallback(this);
                if ((this.mPrivateFlags & WILL_NOT_DRAW) != 0) {
                    this.mPrivateFlags &= -129;
                    requestLayout = true;
                }
            } else {
                this.mBackground = null;
                if ((this.mViewFlags & WILL_NOT_DRAW) != 0 && (this.mForegroundInfo == null || this.mForegroundInfo.mDrawable == null)) {
                    this.mPrivateFlags |= WILL_NOT_DRAW;
                }
                requestLayout = true;
            }
            computeOpaqueFlags();
            if (requestLayout) {
                requestLayout();
            }
            this.mBackgroundSizeChanged = true;
            invalidate(true);
            invalidateOutline();
        }
    }

    public Drawable getBackground() {
        return this.mBackground;
    }

    public void setBackgroundTintList(ColorStateList tint) {
        if (this.mBackgroundTint == null) {
            this.mBackgroundTint = new TintInfo();
        }
        this.mBackgroundTint.mTintList = tint;
        this.mBackgroundTint.mHasTintList = true;
        applyBackgroundTint();
    }

    public ColorStateList getBackgroundTintList() {
        return this.mBackgroundTint != null ? this.mBackgroundTint.mTintList : null;
    }

    public void setBackgroundTintMode(Mode tintMode) {
        if (this.mBackgroundTint == null) {
            this.mBackgroundTint = new TintInfo();
        }
        this.mBackgroundTint.mTintMode = tintMode;
        this.mBackgroundTint.mHasTintMode = true;
        applyBackgroundTint();
    }

    public Mode getBackgroundTintMode() {
        return this.mBackgroundTint != null ? this.mBackgroundTint.mTintMode : null;
    }

    private void applyBackgroundTint() {
        if (this.mBackground != null && this.mBackgroundTint != null) {
            TintInfo tintInfo = this.mBackgroundTint;
            if (tintInfo.mHasTintList || tintInfo.mHasTintMode) {
                this.mBackground = this.mBackground.mutate();
                if (tintInfo.mHasTintList) {
                    this.mBackground.setTintList(tintInfo.mTintList);
                }
                if (tintInfo.mHasTintMode) {
                    this.mBackground.setTintMode(tintInfo.mTintMode);
                }
                if (this.mBackground.isStateful()) {
                    this.mBackground.setState(getDrawableState());
                }
            }
        }
    }

    public Drawable getForeground() {
        return this.mForegroundInfo != null ? this.mForegroundInfo.mDrawable : null;
    }

    public void setForeground(Drawable foreground) {
        if (this.mForegroundInfo == null) {
            if (foreground != null) {
                this.mForegroundInfo = new ForegroundInfo();
            } else {
                return;
            }
        }
        if (foreground != this.mForegroundInfo.mDrawable) {
            if (this.mForegroundInfo.mDrawable != null) {
                if (isAttachedToWindow()) {
                    this.mForegroundInfo.mDrawable.setVisible(DBG, DBG);
                }
                this.mForegroundInfo.mDrawable.setCallback(null);
                unscheduleDrawable(this.mForegroundInfo.mDrawable);
            }
            this.mForegroundInfo.mDrawable = foreground;
            this.mForegroundInfo.mBoundsChanged = true;
            if (foreground != null) {
                if ((this.mPrivateFlags & WILL_NOT_DRAW) != 0) {
                    this.mPrivateFlags &= -129;
                }
                foreground.setLayoutDirection(getLayoutDirection());
                if (foreground.isStateful()) {
                    foreground.setState(getDrawableState());
                }
                applyForegroundTint();
                if (isAttachedToWindow()) {
                    foreground.setVisible(getWindowVisibility() == 0 ? isShown() : DBG, DBG);
                }
                foreground.setCallback(this);
            } else if ((this.mViewFlags & WILL_NOT_DRAW) != 0 && this.mBackground == null) {
                this.mPrivateFlags |= WILL_NOT_DRAW;
            }
            requestLayout();
            invalidate();
        }
    }

    public boolean isForegroundInsidePadding() {
        return this.mForegroundInfo != null ? this.mForegroundInfo.mInsidePadding : true;
    }

    public int getForegroundGravity() {
        if (this.mForegroundInfo != null) {
            return this.mForegroundInfo.mGravity;
        }
        return 8388659;
    }

    public void setForegroundGravity(int gravity) {
        if (this.mForegroundInfo == null) {
            this.mForegroundInfo = new ForegroundInfo();
        }
        if (this.mForegroundInfo.mGravity != gravity) {
            if ((Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK & gravity) == 0) {
                gravity |= Gravity.START;
            }
            if ((gravity & LogPower.APP_PROCESS_EXIT) == 0) {
                gravity |= PFLAG2_LAYOUT_DIRECTION_RESOLVED_MASK;
            }
            this.mForegroundInfo.mGravity = gravity;
            requestLayout();
        }
    }

    public void setForegroundTintList(ColorStateList tint) {
        if (this.mForegroundInfo == null) {
            this.mForegroundInfo = new ForegroundInfo();
        }
        if (this.mForegroundInfo.mTintInfo == null) {
            this.mForegroundInfo.mTintInfo = new TintInfo();
        }
        this.mForegroundInfo.mTintInfo.mTintList = tint;
        this.mForegroundInfo.mTintInfo.mHasTintList = true;
        applyForegroundTint();
    }

    public ColorStateList getForegroundTintList() {
        if (this.mForegroundInfo == null || this.mForegroundInfo.mTintInfo == null) {
            return null;
        }
        return this.mForegroundInfo.mTintInfo.mTintList;
    }

    public void setForegroundTintMode(Mode tintMode) {
        if (this.mForegroundInfo == null) {
            this.mForegroundInfo = new ForegroundInfo();
        }
        if (this.mForegroundInfo.mTintInfo == null) {
            this.mForegroundInfo.mTintInfo = new TintInfo();
        }
        this.mForegroundInfo.mTintInfo.mTintMode = tintMode;
        this.mForegroundInfo.mTintInfo.mHasTintMode = true;
        applyForegroundTint();
    }

    public Mode getForegroundTintMode() {
        if (this.mForegroundInfo == null || this.mForegroundInfo.mTintInfo == null) {
            return null;
        }
        return this.mForegroundInfo.mTintInfo.mTintMode;
    }

    private void applyForegroundTint() {
        if (this.mForegroundInfo != null && this.mForegroundInfo.mDrawable != null && this.mForegroundInfo.mTintInfo != null) {
            TintInfo tintInfo = this.mForegroundInfo.mTintInfo;
            if (tintInfo.mHasTintList || tintInfo.mHasTintMode) {
                this.mForegroundInfo.mDrawable = this.mForegroundInfo.mDrawable.mutate();
                if (tintInfo.mHasTintList) {
                    this.mForegroundInfo.mDrawable.setTintList(tintInfo.mTintList);
                }
                if (tintInfo.mHasTintMode) {
                    this.mForegroundInfo.mDrawable.setTintMode(tintInfo.mTintMode);
                }
                if (this.mForegroundInfo.mDrawable.isStateful()) {
                    this.mForegroundInfo.mDrawable.setState(getDrawableState());
                }
            }
        }
    }

    public void onDrawForeground(Canvas canvas) {
        Drawable foreground = null;
        onDrawScrollIndicators(canvas);
        onDrawScrollBars(canvas);
        if (this.mForegroundInfo != null) {
            foreground = this.mForegroundInfo.mDrawable;
        }
        if (foreground != null) {
            if (this.mForegroundInfo.mBoundsChanged) {
                this.mForegroundInfo.mBoundsChanged = DBG;
                Rect selfBounds = this.mForegroundInfo.mSelfBounds;
                Rect overlayBounds = this.mForegroundInfo.mOverlayBounds;
                if (this.mForegroundInfo.mInsidePadding) {
                    selfBounds.set(VISIBLE, VISIBLE, getWidth(), getHeight());
                } else {
                    selfBounds.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
                }
                Gravity.apply(this.mForegroundInfo.mGravity, foreground.getIntrinsicWidth(), foreground.getIntrinsicHeight(), selfBounds, overlayBounds, getLayoutDirection());
                foreground.setBounds(overlayBounds);
            }
            foreground.draw(canvas);
        }
    }

    public void setPadding(int left, int top, int right, int bottom) {
        resetResolvedPaddingInternal();
        this.mUserPaddingStart = UNDEFINED_PADDING;
        this.mUserPaddingEnd = UNDEFINED_PADDING;
        this.mUserPaddingLeftInitial = left;
        this.mUserPaddingRightInitial = right;
        this.mLeftPaddingDefined = true;
        this.mRightPaddingDefined = true;
        internalSetPadding(left, top, right, bottom);
    }

    protected void internalSetPadding(int left, int top, int right, int bottom) {
        int i = VISIBLE;
        this.mUserPaddingLeft = left;
        this.mUserPaddingRight = right;
        this.mUserPaddingBottom = bottom;
        int viewFlags = this.mViewFlags;
        boolean changed = DBG;
        if ((viewFlags & SCROLLBARS_MASK) != 0) {
            if ((viewFlags & SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0) {
                int offset = (viewFlags & STATUS_BAR_DISABLE_RECENT) == 0 ? VISIBLE : getVerticalScrollbarWidth();
                switch (this.mVerticalScrollbarPosition) {
                    case VISIBLE /*0*/:
                        if (!isRtlLocale()) {
                            right += offset;
                            break;
                        } else {
                            left += offset;
                            break;
                        }
                    case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                        left += offset;
                        break;
                    case TEXT_DIRECTION_ANY_RTL /*2*/:
                        right += offset;
                        break;
                }
            }
            if ((viewFlags & SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0) {
                if ((viewFlags & STATUS_BAR_DISABLE_RECENT) != 0) {
                    i = getHorizontalScrollbarHeight();
                }
                bottom += i;
            }
        }
        if (this.mPaddingLeft != left) {
            changed = true;
            this.mPaddingLeft = left;
        }
        if (this.mPaddingTop != top) {
            changed = true;
            this.mPaddingTop = top;
        }
        if (this.mPaddingRight != right) {
            changed = true;
            this.mPaddingRight = right;
        }
        if (this.mPaddingBottom != bottom) {
            changed = true;
            this.mPaddingBottom = bottom;
        }
        if (changed) {
            requestLayout();
            invalidateOutline();
        }
    }

    public void setPaddingRelative(int start, int top, int end, int bottom) {
        resetResolvedPaddingInternal();
        this.mUserPaddingStart = start;
        this.mUserPaddingEnd = end;
        this.mLeftPaddingDefined = true;
        this.mRightPaddingDefined = true;
        switch (getLayoutDirection()) {
            case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                this.mUserPaddingLeftInitial = end;
                this.mUserPaddingRightInitial = start;
                internalSetPadding(end, top, start, bottom);
            default:
                this.mUserPaddingLeftInitial = start;
                this.mUserPaddingRightInitial = end;
                internalSetPadding(start, top, end, bottom);
        }
    }

    public int getPaddingTop() {
        return this.mPaddingTop;
    }

    public int getPaddingBottom() {
        return this.mPaddingBottom;
    }

    public int getPaddingLeft() {
        if (!isPaddingResolved()) {
            resolvePadding();
        }
        return this.mPaddingLeft;
    }

    public int getPaddingStart() {
        if (!isPaddingResolved()) {
            resolvePadding();
        }
        return getLayoutDirection() == VIEW_IS_NOT_ADVIEW_OR_DISABLE ? this.mPaddingRight : this.mPaddingLeft;
    }

    public int getPaddingRight() {
        if (!isPaddingResolved()) {
            resolvePadding();
        }
        return this.mPaddingRight;
    }

    public int getPaddingEnd() {
        if (!isPaddingResolved()) {
            resolvePadding();
        }
        return getLayoutDirection() == VIEW_IS_NOT_ADVIEW_OR_DISABLE ? this.mPaddingLeft : this.mPaddingRight;
    }

    public boolean isPaddingRelative() {
        return (this.mUserPaddingStart == UNDEFINED_PADDING && this.mUserPaddingEnd == UNDEFINED_PADDING) ? DBG : true;
    }

    Insets computeOpticalInsets() {
        return this.mBackground == null ? Insets.NONE : this.mBackground.getOpticalInsets();
    }

    public void resetPaddingToInitialValues() {
        if (isRtlCompatibilityMode()) {
            this.mPaddingLeft = this.mUserPaddingLeftInitial;
            this.mPaddingRight = this.mUserPaddingRightInitial;
            return;
        }
        if (isRtlLocale()) {
            int i;
            this.mPaddingLeft = this.mUserPaddingEnd >= 0 ? this.mUserPaddingEnd : this.mUserPaddingLeftInitial;
            if (this.mUserPaddingStart >= 0) {
                i = this.mUserPaddingStart;
            } else {
                i = this.mUserPaddingRightInitial;
            }
            this.mPaddingRight = i;
        } else {
            this.mPaddingLeft = this.mUserPaddingStart >= 0 ? this.mUserPaddingStart : this.mUserPaddingLeftInitial;
            this.mPaddingRight = this.mUserPaddingEnd >= 0 ? this.mUserPaddingEnd : this.mUserPaddingRightInitial;
        }
    }

    public Insets getOpticalInsets() {
        if (this.mLayoutInsets == null) {
            this.mLayoutInsets = computeOpticalInsets();
        }
        return this.mLayoutInsets;
    }

    public void setOpticalInsets(Insets insets) {
        this.mLayoutInsets = insets;
    }

    public void setSelected(boolean selected) {
        boolean z;
        if ((this.mPrivateFlags & TEXT_DIRECTION_RTL) != 0) {
            z = true;
        } else {
            z = VISIBLE;
        }
        if (z != selected) {
            int i;
            int i2 = this.mPrivateFlags & -5;
            if (selected) {
                i = TEXT_DIRECTION_RTL;
            } else {
                i = VISIBLE;
            }
            this.mPrivateFlags = i | i2;
            if (!selected) {
                resetPressedState();
            }
            invalidate(true);
            refreshDrawableState();
            dispatchSetSelected(selected);
            if (selected) {
                sendAccessibilityEvent(TEXT_DIRECTION_RTL);
            } else {
                notifyViewAccessibilityStateChangedIfNeeded(VISIBLE);
            }
        }
    }

    protected void dispatchSetSelected(boolean selected) {
    }

    @ExportedProperty
    public boolean isSelected() {
        return (this.mPrivateFlags & TEXT_DIRECTION_RTL) != 0 ? true : DBG;
    }

    public void setActivated(boolean activated) {
        boolean z;
        int i = STATUS_BAR_TRANSLUCENT;
        if ((this.mPrivateFlags & STATUS_BAR_TRANSLUCENT) != 0) {
            z = true;
        } else {
            z = VISIBLE;
        }
        if (z != activated) {
            int i2 = this.mPrivateFlags & -1073741825;
            if (!activated) {
                i = VISIBLE;
            }
            this.mPrivateFlags = i | i2;
            invalidate(true);
            refreshDrawableState();
            dispatchSetActivated(activated);
        }
    }

    protected void dispatchSetActivated(boolean activated) {
    }

    @ExportedProperty
    public boolean isActivated() {
        return (this.mPrivateFlags & STATUS_BAR_TRANSLUCENT) != 0 ? true : DBG;
    }

    public ViewTreeObserver getViewTreeObserver() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mTreeObserver;
        }
        if (this.mFloatingTreeObserver == null) {
            this.mFloatingTreeObserver = new ViewTreeObserver();
        }
        return this.mFloatingTreeObserver;
    }

    public View getRootView() {
        if (this.mAttachInfo != null) {
            View v = this.mAttachInfo.mRootView;
            if (v != null) {
                return v;
            }
        }
        View parent = this;
        while (parent.mParent != null && (parent.mParent instanceof View)) {
            parent = parent.mParent;
        }
        return parent;
    }

    public boolean toGlobalMotionEvent(MotionEvent ev) {
        AttachInfo info = this.mAttachInfo;
        if (info == null) {
            return DBG;
        }
        Matrix m = info.mTmpMatrix;
        m.set(Matrix.IDENTITY_MATRIX);
        transformMatrixToGlobal(m);
        ev.transform(m);
        return true;
    }

    public boolean toLocalMotionEvent(MotionEvent ev) {
        AttachInfo info = this.mAttachInfo;
        if (info == null) {
            return DBG;
        }
        Matrix m = info.mTmpMatrix;
        m.set(Matrix.IDENTITY_MATRIX);
        transformMatrixToLocal(m);
        ev.transform(m);
        return true;
    }

    public void transformMatrixToGlobal(Matrix m) {
        ViewParent parent = this.mParent;
        if (parent instanceof View) {
            View vp = (View) parent;
            vp.transformMatrixToGlobal(m);
            m.preTranslate((float) (-vp.mScrollX), (float) (-vp.mScrollY));
        } else if (parent instanceof ViewRootImpl) {
            ViewRootImpl vr = (ViewRootImpl) parent;
            vr.transformMatrixToGlobal(m);
            m.preTranslate(0.0f, (float) (-vr.mCurScrollY));
        }
        m.preTranslate((float) this.mLeft, (float) this.mTop);
        if (!hasIdentityMatrix()) {
            m.preConcat(getMatrix());
        }
    }

    public void transformMatrixToLocal(Matrix m) {
        ViewParent parent = this.mParent;
        if (parent instanceof View) {
            View vp = (View) parent;
            vp.transformMatrixToLocal(m);
            m.postTranslate((float) vp.mScrollX, (float) vp.mScrollY);
        } else if (parent instanceof ViewRootImpl) {
            ViewRootImpl vr = (ViewRootImpl) parent;
            vr.transformMatrixToLocal(m);
            m.postTranslate(0.0f, (float) vr.mCurScrollY);
        }
        m.postTranslate((float) (-this.mLeft), (float) (-this.mTop));
        if (!hasIdentityMatrix()) {
            m.postConcat(getInverseMatrix());
        }
    }

    @ExportedProperty(category = "layout", indexMapping = {@IntToString(from = 0, to = "x"), @IntToString(from = 1, to = "y")})
    public int[] getLocationOnScreen() {
        int[] location = new int[TEXT_DIRECTION_ANY_RTL];
        getLocationOnScreen(location);
        return location;
    }

    public void getLocationOnScreen(int[] outLocation) {
        getLocationInWindow(outLocation);
        AttachInfo info = this.mAttachInfo;
        if (info != null) {
            outLocation[VISIBLE] = outLocation[VISIBLE] + info.mWindowLeft;
            outLocation[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = outLocation[VIEW_IS_NOT_ADVIEW_OR_DISABLE] + info.mWindowTop;
        }
    }

    public void getLocationInWindow(int[] outLocation) {
        if (outLocation == null || outLocation.length < TEXT_DIRECTION_ANY_RTL) {
            throw new IllegalArgumentException("outLocation must be an array of two integers");
        }
        outLocation[VISIBLE] = VISIBLE;
        outLocation[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = VISIBLE;
        transformFromViewToWindowSpace(outLocation);
    }

    public void transformFromViewToWindowSpace(int[] inOutLocation) {
        if (inOutLocation == null || inOutLocation.length < TEXT_DIRECTION_ANY_RTL) {
            throw new IllegalArgumentException("inOutLocation must be an array of two integers");
        } else if (this.mAttachInfo == null) {
            inOutLocation[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = VISIBLE;
            inOutLocation[VISIBLE] = VISIBLE;
        } else {
            float[] position = this.mAttachInfo.mTmpTransformLocation;
            position[VISIBLE] = (float) inOutLocation[VISIBLE];
            position[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = (float) inOutLocation[VIEW_IS_NOT_ADVIEW_OR_DISABLE];
            if (!hasIdentityMatrix()) {
                getMatrix().mapPoints(position);
            }
            position[VISIBLE] = position[VISIBLE] + ((float) this.mLeft);
            position[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = position[VIEW_IS_NOT_ADVIEW_OR_DISABLE] + ((float) this.mTop);
            ViewParent viewParent = this.mParent;
            while (viewParent instanceof View) {
                View view = (View) viewParent;
                position[VISIBLE] = position[VISIBLE] - ((float) view.mScrollX);
                position[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = position[VIEW_IS_NOT_ADVIEW_OR_DISABLE] - ((float) view.mScrollY);
                if (!view.hasIdentityMatrix()) {
                    view.getMatrix().mapPoints(position);
                }
                position[VISIBLE] = position[VISIBLE] + ((float) view.mLeft);
                position[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = position[VIEW_IS_NOT_ADVIEW_OR_DISABLE] + ((float) view.mTop);
                viewParent = view.mParent;
            }
            if (viewParent instanceof ViewRootImpl) {
                position[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = position[VIEW_IS_NOT_ADVIEW_OR_DISABLE] - ((float) ((ViewRootImpl) viewParent).mCurScrollY);
            }
            inOutLocation[VISIBLE] = Math.round(position[VISIBLE]);
            inOutLocation[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = Math.round(position[VIEW_IS_NOT_ADVIEW_OR_DISABLE]);
        }
    }

    protected View findViewTraversal(int id) {
        if (id == this.mID) {
            return this;
        }
        return null;
    }

    protected View findViewWithTagTraversal(Object tag) {
        if (tag == null || !tag.equals(this.mTag)) {
            return null;
        }
        return this;
    }

    protected View findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        if (predicate.apply(this)) {
            return this;
        }
        return null;
    }

    public final View findViewById(int id) {
        if (id < 0) {
            return null;
        }
        return findViewTraversal(id);
    }

    final View findViewByAccessibilityId(int accessibilityId) {
        if (accessibilityId < 0) {
            return null;
        }
        View view = findViewByAccessibilityIdTraversal(accessibilityId);
        if (view == null) {
            return null;
        }
        if (!view.includeForAccessibility()) {
            view = null;
        }
        return view;
    }

    public View findViewByAccessibilityIdTraversal(int accessibilityId) {
        if (getAccessibilityViewId() == accessibilityId) {
            return this;
        }
        return null;
    }

    public final View findViewWithTag(Object tag) {
        if (tag == null) {
            return null;
        }
        return findViewWithTagTraversal(tag);
    }

    public final View findViewByPredicate(Predicate<View> predicate) {
        return findViewByPredicateTraversal(predicate, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final View findViewByPredicateInsideOut(View start, Predicate<View> predicate) {
        View view = null;
        while (true) {
            View view2 = start.findViewByPredicateTraversal(predicate, view);
            if (view2 != null || start == this) {
                return view2;
            }
            ViewParent parent = start.getParent();
            if (parent == null || !(parent instanceof View)) {
                return null;
            }
            view = start;
            start = (View) parent;
        }
        return null;
    }

    public void setId(int id) {
        this.mID = id;
        if (this.mID == VIEW_IS_NOT_CHECKED && this.mLabelForId != VIEW_IS_NOT_CHECKED) {
            this.mID = generateViewId();
        }
    }

    public void setIsRootNamespace(boolean isRoot) {
        if (isRoot) {
            this.mPrivateFlags |= STATUS_BAR_TRANSPARENT;
        } else {
            this.mPrivateFlags &= -9;
        }
    }

    public boolean isRootNamespace() {
        return (this.mPrivateFlags & STATUS_BAR_TRANSPARENT) != 0 ? true : DBG;
    }

    @CapturedViewProperty
    public int getId() {
        return this.mID;
    }

    @ExportedProperty
    public Object getTag() {
        return this.mTag;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public Object getTag(int key) {
        return this.mKeyedTags != null ? this.mKeyedTags.get(key) : null;
    }

    public void setTag(int key, Object tag) {
        if ((key >>> 24) < TEXT_DIRECTION_ANY_RTL) {
            throw new IllegalArgumentException("The key must be an application-specific resource id.");
        }
        setKeyedTag(key, tag);
    }

    public void setTagInternal(int key, Object tag) {
        if ((key >>> 24) != VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
            throw new IllegalArgumentException("The key must be a framework-specific resource id.");
        }
        setKeyedTag(key, tag);
    }

    private void setKeyedTag(int key, Object tag) {
        if (this.mKeyedTags == null) {
            this.mKeyedTags = new SparseArray(TEXT_DIRECTION_ANY_RTL);
        }
        this.mKeyedTags.put(key, tag);
    }

    public void debug() {
        debug(VISIBLE);
    }

    protected void debug(int depth) {
        String output = debugIndent(depth + VIEW_IS_NOT_CHECKED) + "+ " + this;
        int id = getId();
        if (id != VIEW_IS_NOT_CHECKED) {
            output = output + " (id=" + id + ")";
        }
        Object tag = getTag();
        if (tag != null) {
            output = output + " (tag=" + tag + ")";
        }
        Log.d(VIEW_LOG_TAG, output);
        if ((this.mPrivateFlags & TEXT_DIRECTION_ANY_RTL) != 0) {
            Log.d(VIEW_LOG_TAG, debugIndent(depth) + " FOCUSED");
        }
        Log.d(VIEW_LOG_TAG, debugIndent(depth) + "frame={" + this.mLeft + ", " + this.mTop + ", " + this.mRight + ", " + this.mBottom + "} scroll={" + this.mScrollX + ", " + this.mScrollY + "} ");
        if (this.mPaddingLeft == 0 && this.mPaddingTop == 0 && this.mPaddingRight == 0) {
            if (this.mPaddingBottom != 0) {
            }
            Log.d(VIEW_LOG_TAG, debugIndent(depth) + "mMeasureWidth=" + this.mMeasuredWidth + " mMeasureHeight=" + this.mMeasuredHeight);
            output = debugIndent(depth);
            if (this.mLayoutParams != null) {
                output = output + "BAD! no layout params";
            } else {
                output = this.mLayoutParams.debug(output);
            }
            Log.d(VIEW_LOG_TAG, output);
            Log.d(VIEW_LOG_TAG, ((debugIndent(depth) + "flags={") + printFlags(this.mViewFlags)) + "}");
            Log.d(VIEW_LOG_TAG, ((debugIndent(depth) + "privateFlags={") + printPrivateFlags(this.mPrivateFlags)) + "}");
        }
        Log.d(VIEW_LOG_TAG, debugIndent(depth) + "padding={" + this.mPaddingLeft + ", " + this.mPaddingTop + ", " + this.mPaddingRight + ", " + this.mPaddingBottom + "}");
        Log.d(VIEW_LOG_TAG, debugIndent(depth) + "mMeasureWidth=" + this.mMeasuredWidth + " mMeasureHeight=" + this.mMeasuredHeight);
        output = debugIndent(depth);
        if (this.mLayoutParams != null) {
            output = this.mLayoutParams.debug(output);
        } else {
            output = output + "BAD! no layout params";
        }
        Log.d(VIEW_LOG_TAG, output);
        Log.d(VIEW_LOG_TAG, ((debugIndent(depth) + "flags={") + printFlags(this.mViewFlags)) + "}");
        Log.d(VIEW_LOG_TAG, ((debugIndent(depth) + "privateFlags={") + printPrivateFlags(this.mPrivateFlags)) + "}");
    }

    protected static String debugIndent(int depth) {
        StringBuilder spaces = new StringBuilder(((depth * TEXT_DIRECTION_ANY_RTL) + TEXT_DIRECTION_LTR) * TEXT_DIRECTION_ANY_RTL);
        for (int i = VISIBLE; i < (depth * TEXT_DIRECTION_ANY_RTL) + TEXT_DIRECTION_LTR; i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
            spaces.append(' ').append(' ');
        }
        return spaces.toString();
    }

    @ExportedProperty(category = "layout")
    public int getBaseline() {
        return VIEW_IS_NOT_CHECKED;
    }

    public boolean isInLayout() {
        ViewRootImpl viewRoot = getViewRootImpl();
        return viewRoot != null ? viewRoot.isInLayout() : DBG;
    }

    public void requestLayout() {
        if (this.mMeasureCache != null) {
            this.mMeasureCache.clear();
        }
        if (this.mAttachInfo != null && this.mAttachInfo.mViewRequestingLayout == null) {
            ViewRootImpl viewRoot = getViewRootImpl();
            if (viewRoot == null || !viewRoot.isInLayout() || viewRoot.requestLayoutDuringLayout(this)) {
                this.mAttachInfo.mViewRequestingLayout = this;
            } else {
                return;
            }
        }
        this.mPrivateFlags |= SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        this.mPrivateFlags |= UNDEFINED_PADDING;
        if (!(this.mParent == null || this.mParent.isLayoutRequested())) {
            this.mParent.requestLayout();
        }
        if (this.mAttachInfo != null && this.mAttachInfo.mViewRequestingLayout == this) {
            this.mAttachInfo.mViewRequestingLayout = null;
        }
    }

    public void forceLayout() {
        if (this.mMeasureCache != null) {
            this.mMeasureCache.clear();
        }
        this.mPrivateFlags |= SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        this.mPrivateFlags |= UNDEFINED_PADDING;
    }

    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean specChanged;
        boolean matchesSpecSize;
        boolean optical = isLayoutModeOptical(this);
        if (optical != isLayoutModeOptical(this.mParent)) {
            Insets insets = getOpticalInsets();
            int oWidth = insets.left + insets.right;
            int oHeight = insets.top + insets.bottom;
            if (optical) {
                oWidth = -oWidth;
            }
            widthMeasureSpec = MeasureSpec.adjust(widthMeasureSpec, oWidth);
            if (optical) {
                oHeight = -oHeight;
            }
            heightMeasureSpec = MeasureSpec.adjust(heightMeasureSpec, oHeight);
        }
        long key = (((long) widthMeasureSpec) << SCROLL_INDICATOR_END) | (((long) heightMeasureSpec) & ExpandableListView.PACKED_POSITION_VALUE_NULL);
        if (this.mMeasureCache == null) {
            this.mMeasureCache = new LongSparseLongArray(TEXT_DIRECTION_ANY_RTL);
        }
        int i = this.mPrivateFlags & SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        boolean forceLayout = r0 == SYSTEM_UI_FLAG_IMMERSIVE_STICKY ? true : DBG;
        if (widthMeasureSpec == this.mOldWidthMeasureSpec) {
            specChanged = heightMeasureSpec != this.mOldHeightMeasureSpec ? true : DBG;
        } else {
            specChanged = true;
        }
        boolean isSpecExactly = MeasureSpec.getMode(widthMeasureSpec) == STATUS_BAR_TRANSLUCENT ? MeasureSpec.getMode(heightMeasureSpec) == STATUS_BAR_TRANSLUCENT ? true : DBG : DBG;
        if (getMeasuredWidth() == MeasureSpec.getSize(widthMeasureSpec)) {
            matchesSpecSize = getMeasuredHeight() == MeasureSpec.getSize(heightMeasureSpec) ? true : DBG;
        } else {
            matchesSpecSize = DBG;
        }
        boolean needsLayout = specChanged ? (!sAlwaysRemeasureExactly && isSpecExactly && matchesSpecSize) ? DBG : true : DBG;
        if (forceLayout || needsLayout) {
            this.mPrivateFlags &= -2049;
            resolveRtlPropertiesIfNeeded();
            int cacheIndex = forceLayout ? VIEW_IS_NOT_CHECKED : this.mMeasureCache.indexOfKey(key);
            if (cacheIndex < 0 || sIgnoreMeasureCache) {
                onMeasure(widthMeasureSpec, heightMeasureSpec);
                this.mPrivateFlags3 &= -9;
            } else {
                long value = this.mMeasureCache.valueAt(cacheIndex);
                setMeasuredDimensionRaw((int) (value >> SCROLL_INDICATOR_END), (int) value);
                this.mPrivateFlags3 |= STATUS_BAR_TRANSPARENT;
            }
            i = this.mPrivateFlags & SYSTEM_UI_FLAG_IMMERSIVE;
            if (r0 != SYSTEM_UI_FLAG_IMMERSIVE) {
                throw new IllegalStateException("View with id " + getId() + ": " + getClass().getName() + "#onMeasure() did not set the" + " measured dimension by calling" + " setMeasuredDimension()");
            }
            this.mPrivateFlags |= SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        this.mOldWidthMeasureSpec = widthMeasureSpec;
        this.mOldHeightMeasureSpec = heightMeasureSpec;
        this.mMeasureCache.put(key, (((long) this.mMeasuredWidth) << SCROLL_INDICATOR_END) | (((long) this.mMeasuredHeight) & ExpandableListView.PACKED_POSITION_VALUE_NULL));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    protected final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        boolean optical = isLayoutModeOptical(this);
        if (optical != isLayoutModeOptical(this.mParent)) {
            Insets insets = getOpticalInsets();
            int opticalWidth = insets.left + insets.right;
            int opticalHeight = insets.top + insets.bottom;
            if (!optical) {
                opticalWidth = -opticalWidth;
            }
            measuredWidth += opticalWidth;
            if (!optical) {
                opticalHeight = -opticalHeight;
            }
            measuredHeight += opticalHeight;
        }
        setMeasuredDimensionRaw(measuredWidth, measuredHeight);
    }

    private void setMeasuredDimensionRaw(int measuredWidth, int measuredHeight) {
        this.mMeasuredWidth = measuredWidth;
        this.mMeasuredHeight = measuredHeight;
        this.mPrivateFlags |= SYSTEM_UI_FLAG_IMMERSIVE;
    }

    public static int combineMeasuredStates(int curState, int newState) {
        return curState | newState;
    }

    public static int resolveSize(int size, int measureSpec) {
        return resolveSizeAndState(size, measureSpec, VISIBLE) & MEASURED_SIZE_MASK;
    }

    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case UNDEFINED_PADDING /*-2147483648*/:
                if (specSize >= size) {
                    result = size;
                    break;
                }
                result = specSize | STATUS_BAR_DISABLE_RECENT;
                break;
            case STATUS_BAR_TRANSLUCENT /*1073741824*/:
                result = specSize;
                break;
            default:
                result = size;
                break;
        }
        return (MEASURED_STATE_MASK & childMeasuredState) | result;
    }

    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case UNDEFINED_PADDING /*-2147483648*/:
            case STATUS_BAR_TRANSLUCENT /*1073741824*/:
                return specSize;
            case VISIBLE /*0*/:
                return size;
            default:
                return result;
        }
    }

    protected int getSuggestedMinimumHeight() {
        return this.mBackground == null ? this.mMinHeight : Math.max(this.mMinHeight, this.mBackground.getMinimumHeight());
    }

    protected int getSuggestedMinimumWidth() {
        return this.mBackground == null ? this.mMinWidth : Math.max(this.mMinWidth, this.mBackground.getMinimumWidth());
    }

    public int getMinimumHeight() {
        return this.mMinHeight;
    }

    @RemotableViewMethod
    public void setMinimumHeight(int minHeight) {
        this.mMinHeight = minHeight;
        requestLayout();
    }

    public int getMinimumWidth() {
        return this.mMinWidth;
    }

    public void setMinimumWidth(int minWidth) {
        this.mMinWidth = minWidth;
        requestLayout();
    }

    public Animation getAnimation() {
        return this.mCurrentAnimation;
    }

    public void startAnimation(Animation animation) {
        if (!HwFrameworkFactory.getHwView().cancelAnimation(this, this.mContext)) {
            animation.setStartTime(-1);
            setAnimation(animation);
            invalidateParentCaches();
            invalidate(true);
        }
    }

    public void clearAnimation() {
        if (this.mCurrentAnimation != null) {
            this.mCurrentAnimation.detach();
        }
        this.mCurrentAnimation = null;
        invalidateParentIfNeeded();
    }

    public void setAnimation(Animation animation) {
        this.mCurrentAnimation = animation;
        if (animation != null) {
            if (this.mAttachInfo != null && this.mAttachInfo.mDisplayState == VIEW_IS_NOT_ADVIEW_OR_DISABLE && animation.getStartTime() == -1) {
                animation.setStartTime(AnimationUtils.currentAnimationTimeMillis());
            }
            animation.reset();
        }
    }

    protected void onAnimationStart() {
        this.mPrivateFlags |= TV_PICTURE_IN_PICTURE_VISIBLE;
    }

    protected void onAnimationEnd() {
        this.mPrivateFlags &= -65537;
    }

    protected boolean onSetAlpha(int alpha) {
        return DBG;
    }

    public boolean gatherTransparentRegion(Region region) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (!(region == null || attachInfo == null)) {
            if ((this.mPrivateFlags & WILL_NOT_DRAW) == 0) {
                int[] location = attachInfo.mTransparentLocation;
                getLocationInWindow(location);
                region.op(location[VISIBLE], location[VIEW_IS_NOT_ADVIEW_OR_DISABLE], (location[VISIBLE] + this.mRight) - this.mLeft, (location[VIEW_IS_NOT_ADVIEW_OR_DISABLE] + this.mBottom) - this.mTop, Op.DIFFERENCE);
            } else {
                if (!(this.mBackground == null || this.mBackground.getOpacity() == -2)) {
                    applyDrawableToTransparentRegion(this.mBackground, region);
                }
                if (!(this.mForegroundInfo == null || this.mForegroundInfo.mDrawable == null || this.mForegroundInfo.mDrawable.getOpacity() == -2)) {
                    applyDrawableToTransparentRegion(this.mForegroundInfo.mDrawable, region);
                }
            }
        }
        return true;
    }

    public void playSoundEffect(int soundConstant) {
        if (this.mAttachInfo != null && this.mAttachInfo.mRootCallbacks != null && isSoundEffectsEnabled()) {
            this.mAttachInfo.mRootCallbacks.playSoundEffect(soundConstant);
        }
    }

    public boolean performHapticFeedback(int feedbackConstant) {
        return performHapticFeedback(feedbackConstant, VISIBLE);
    }

    public boolean performHapticFeedback(int feedbackConstant, int flags) {
        boolean z = DBG;
        if (this.mAttachInfo == null) {
            return DBG;
        }
        if ((flags & VIEW_IS_NOT_ADVIEW_OR_DISABLE) == 0 && !isHapticFeedbackEnabled()) {
            return DBG;
        }
        Callbacks callbacks = this.mAttachInfo.mRootCallbacks;
        if ((flags & TEXT_DIRECTION_ANY_RTL) != 0) {
            z = true;
        }
        return callbacks.performHapticFeedback(feedbackConstant, z);
    }

    public void setSystemUiVisibility(int visibility) {
        if (visibility != this.mSystemUiVisibility) {
            this.mSystemUiVisibility = visibility;
            if (this.mParent != null && this.mAttachInfo != null && !this.mAttachInfo.mRecomputeGlobalAttributes) {
                this.mParent.recomputeViewAttributes(this);
            }
        }
    }

    public int getSystemUiVisibility() {
        return this.mSystemUiVisibility;
    }

    public int getWindowSystemUiVisibility() {
        return this.mAttachInfo != null ? this.mAttachInfo.mSystemUiVisibility : VISIBLE;
    }

    public void onWindowSystemUiVisibilityChanged(int visible) {
    }

    public void dispatchWindowSystemUiVisiblityChanged(int visible) {
        onWindowSystemUiVisibilityChanged(visible);
    }

    public void setOnSystemUiVisibilityChangeListener(OnSystemUiVisibilityChangeListener l) {
        getListenerInfo().mOnSystemUiVisibilityChangeListener = l;
        if (this.mParent != null && this.mAttachInfo != null && !this.mAttachInfo.mRecomputeGlobalAttributes) {
            this.mParent.recomputeViewAttributes(this);
        }
    }

    public void dispatchSystemUiVisibilityChanged(int visibility) {
        ListenerInfo li = this.mListenerInfo;
        if (li != null && li.mOnSystemUiVisibilityChangeListener != null) {
            li.mOnSystemUiVisibilityChangeListener.onSystemUiVisibilityChange(visibility & PUBLIC_STATUS_BAR_VISIBILITY_MASK);
        }
    }

    public void setDisabledSystemUiVisibility(int flags) {
        if (this.mAttachInfo != null && this.mAttachInfo.mDisabledSystemUiVisibility != flags) {
            this.mAttachInfo.mDisabledSystemUiVisibility = flags;
            if (this.mParent != null) {
                this.mParent.recomputeViewAttributes(this);
            }
        }
    }

    public final boolean startDrag(ClipData data, DragShadowBuilder shadowBuilder, Object myLocalState, int flags) {
        return startDragAndDrop(data, shadowBuilder, myLocalState, flags);
    }

    public final boolean startDragAndDrop(ClipData data, DragShadowBuilder shadowBuilder, Object myLocalState, int flags) {
        if (this.mAttachInfo == null) {
            Log.w(VIEW_LOG_TAG, "startDragAndDrop called on a detached view.");
            return DBG;
        }
        boolean okay = DBG;
        Point shadowSize = new Point();
        Point shadowTouchPoint = new Point();
        shadowBuilder.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
        if (shadowSize.x < 0 || shadowSize.y < 0 || shadowTouchPoint.x < 0 || shadowTouchPoint.y < 0) {
            throw new IllegalStateException("Drag shadow dimensions must not be negative");
        }
        if (this.mAttachInfo.mDragSurface != null) {
            this.mAttachInfo.mDragSurface.release();
        }
        this.mAttachInfo.mDragSurface = new Surface();
        try {
            this.mAttachInfo.mDragToken = this.mAttachInfo.mSession.prepareDrag(this.mAttachInfo.mWindow, flags, shadowSize.x, shadowSize.y, this.mAttachInfo.mDragSurface);
            if (this.mAttachInfo.mDragToken != null) {
                Canvas canvas = this.mAttachInfo.mDragSurface.lockCanvas(null);
                canvas.drawColor(VISIBLE, Mode.CLEAR);
                shadowBuilder.onDrawShadow(canvas);
                this.mAttachInfo.mDragSurface.unlockCanvasAndPost(canvas);
                ViewRootImpl root = getViewRootImpl();
                root.setLocalDragState(myLocalState);
                root.getLastTouchPoint(shadowSize);
                okay = this.mAttachInfo.mSession.performDrag(this.mAttachInfo.mWindow, this.mAttachInfo.mDragToken, root.getLastTouchSource(), (float) shadowSize.x, (float) shadowSize.y, (float) shadowTouchPoint.x, (float) shadowTouchPoint.y, data);
            }
        } catch (Exception e) {
            Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e);
            this.mAttachInfo.mDragSurface.destroy();
            this.mAttachInfo.mDragSurface = null;
        } catch (Throwable th) {
            this.mAttachInfo.mDragSurface.unlockCanvasAndPost(canvas);
        }
        return okay;
    }

    public final void cancelDragAndDrop() {
        if (this.mAttachInfo == null) {
            Log.w(VIEW_LOG_TAG, "cancelDragAndDrop called on a detached view.");
            return;
        }
        if (this.mAttachInfo.mDragToken != null) {
            try {
                this.mAttachInfo.mSession.cancelDragAndDrop(this.mAttachInfo.mDragToken);
            } catch (Exception e) {
                Log.e(VIEW_LOG_TAG, "Unable to cancel drag", e);
            }
            this.mAttachInfo.mDragToken = null;
        } else {
            Log.e(VIEW_LOG_TAG, "No active drag to cancel");
        }
    }

    public final void updateDragShadow(DragShadowBuilder shadowBuilder) {
        if (this.mAttachInfo == null) {
            Log.w(VIEW_LOG_TAG, "updateDragShadow called on a detached view.");
            return;
        }
        if (this.mAttachInfo.mDragToken != null) {
            try {
                Canvas canvas = this.mAttachInfo.mDragSurface.lockCanvas(null);
                canvas.drawColor(VISIBLE, Mode.CLEAR);
                shadowBuilder.onDrawShadow(canvas);
                this.mAttachInfo.mDragSurface.unlockCanvasAndPost(canvas);
            } catch (Exception e) {
                Log.e(VIEW_LOG_TAG, "Unable to update drag shadow", e);
            } catch (Throwable th) {
                this.mAttachInfo.mDragSurface.unlockCanvasAndPost(canvas);
            }
        } else {
            Log.e(VIEW_LOG_TAG, "No active drag");
        }
    }

    public final boolean startMovingTask(float startX, float startY) {
        try {
            return this.mAttachInfo.mSession.startMovingTask(this.mAttachInfo.mWindow, startX, startY);
        } catch (RemoteException e) {
            Log.e(VIEW_LOG_TAG, "Unable to start moving", e);
            return DBG;
        }
    }

    public boolean onDragEvent(DragEvent event) {
        return DBG;
    }

    public boolean dispatchDragEvent(DragEvent event) {
        ListenerInfo li = this.mListenerInfo;
        if (li == null || li.mOnDragListener == null || (this.mViewFlags & SCROLL_INDICATOR_END) != 0 || !li.mOnDragListener.onDrag(this, event)) {
            return onDragEvent(event);
        }
        return true;
    }

    boolean canAcceptDrag() {
        return (this.mPrivateFlags2 & VIEW_IS_NOT_ADVIEW_OR_DISABLE) != 0 ? true : DBG;
    }

    public void onCloseSystemDialogs(String reason) {
    }

    public void applyDrawableToTransparentRegion(Drawable dr, Region region) {
        Region r = dr.getTransparentRegion();
        Rect db = dr.getBounds();
        AttachInfo attachInfo = this.mAttachInfo;
        if (r == null || attachInfo == null) {
            region.op(db, Op.DIFFERENCE);
            return;
        }
        int w = getRight() - getLeft();
        int h = getBottom() - getTop();
        if (db.left > 0) {
            r.op(VISIBLE, VISIBLE, db.left, h, Op.UNION);
        }
        if (db.right < w) {
            r.op(db.right, VISIBLE, w, h, Op.UNION);
        }
        if (db.top > 0) {
            r.op(VISIBLE, VISIBLE, w, db.top, Op.UNION);
        }
        if (db.bottom < h) {
            r.op(VISIBLE, db.bottom, w, h, Op.UNION);
        }
        int[] location = attachInfo.mTransparentLocation;
        getLocationInWindow(location);
        r.translate(location[VISIBLE], location[VIEW_IS_NOT_ADVIEW_OR_DISABLE]);
        region.op(r, Op.INTERSECT);
    }

    private void checkForLongClick(int delayOffset, float x, float y) {
        if ((this.mViewFlags & STATUS_BAR_DISABLE_HOME) == STATUS_BAR_DISABLE_HOME) {
            this.mHasPerformedLongPress = DBG;
            if (this.mPendingCheckForLongPress == null) {
                this.mPendingCheckForLongPress = new CheckForLongPress();
            }
            this.mPendingCheckForLongPress.setAnchor(x, y);
            this.mPendingCheckForLongPress.rememberWindowAttachCount();
            postDelayed(this.mPendingCheckForLongPress, (long) (ViewConfiguration.getLongPressTimeout() - delayOffset));
        }
    }

    public static View inflate(Context context, int resource, ViewGroup root) {
        return LayoutInflater.from(context).inflate(resource, root);
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int overScrollMode = this.mOverScrollMode;
        boolean canScrollHorizontal = computeHorizontalScrollRange() > computeHorizontalScrollExtent() ? true : DBG;
        boolean canScrollVertical = computeVerticalScrollRange() > computeVerticalScrollExtent() ? true : DBG;
        boolean z = overScrollMode != 0 ? overScrollMode == VIEW_IS_NOT_ADVIEW_OR_DISABLE ? canScrollHorizontal : DBG : true;
        boolean z2 = overScrollMode != 0 ? overScrollMode == VIEW_IS_NOT_ADVIEW_OR_DISABLE ? canScrollVertical : DBG : true;
        int newScrollX = scrollX + deltaX;
        if (!z) {
            maxOverScrollX = VISIBLE;
        }
        int newScrollY = scrollY + deltaY;
        if (!z2) {
            maxOverScrollY = VISIBLE;
        }
        int left = -maxOverScrollX;
        int right = maxOverScrollX + scrollRangeX;
        int top = -maxOverScrollY;
        int bottom = maxOverScrollY + scrollRangeY;
        boolean clampedX = DBG;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        }
        boolean clampedY = DBG;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        }
        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);
        return !clampedX ? clampedY : true;
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
    }

    public int getOverScrollMode() {
        return this.mOverScrollMode;
    }

    public void setOverScrollMode(int overScrollMode) {
        if (overScrollMode == 0 || overScrollMode == VIEW_IS_NOT_ADVIEW_OR_DISABLE || overScrollMode == TEXT_DIRECTION_ANY_RTL) {
            this.mOverScrollMode = overScrollMode;
            return;
        }
        throw new IllegalArgumentException("Invalid overscroll mode " + overScrollMode);
    }

    public void setNestedScrollingEnabled(boolean enabled) {
        if (enabled) {
            this.mPrivateFlags3 |= WILL_NOT_DRAW;
            return;
        }
        stopNestedScroll();
        this.mPrivateFlags3 &= -129;
    }

    public boolean isNestedScrollingEnabled() {
        return (this.mPrivateFlags3 & WILL_NOT_DRAW) == WILL_NOT_DRAW ? true : DBG;
    }

    public boolean startNestedScroll(int axes) {
        if (hasNestedScrollingParent()) {
            return true;
        }
        if (isNestedScrollingEnabled()) {
            View child = this;
            for (ViewParent p = getParent(); p != null; p = p.getParent()) {
                try {
                    if (p.onStartNestedScroll(child, this, axes)) {
                        this.mNestedScrollingParent = p;
                        p.onNestedScrollAccepted(child, this, axes);
                        return true;
                    }
                } catch (AbstractMethodError e) {
                    Log.e(VIEW_LOG_TAG, "ViewParent " + p + " does not implement interface " + "method onStartNestedScroll", e);
                }
                if (p instanceof View) {
                    child = (View) p;
                }
            }
        }
        return DBG;
    }

    public void stopNestedScroll() {
        if (this.mNestedScrollingParent != null) {
            this.mNestedScrollingParent.onStopNestedScroll(this);
            this.mNestedScrollingParent = null;
        }
    }

    public boolean hasNestedScrollingParent() {
        return this.mNestedScrollingParent != null ? true : DBG;
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        if (isNestedScrollingEnabled() && this.mNestedScrollingParent != null) {
            if (dxConsumed != 0 || dyConsumed != 0 || dxUnconsumed != 0 || dyUnconsumed != 0) {
                int startX = VISIBLE;
                int startY = VISIBLE;
                if (offsetInWindow != null) {
                    getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[VISIBLE];
                    startY = offsetInWindow[VIEW_IS_NOT_ADVIEW_OR_DISABLE];
                }
                this.mNestedScrollingParent.onNestedScroll(this, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
                if (offsetInWindow != null) {
                    getLocationInWindow(offsetInWindow);
                    offsetInWindow[VISIBLE] = offsetInWindow[VISIBLE] - startX;
                    offsetInWindow[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = offsetInWindow[VIEW_IS_NOT_ADVIEW_OR_DISABLE] - startY;
                }
                return true;
            } else if (offsetInWindow != null) {
                offsetInWindow[VISIBLE] = VISIBLE;
                offsetInWindow[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = VISIBLE;
            }
        }
        return DBG;
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        boolean z = true;
        if (isNestedScrollingEnabled() && this.mNestedScrollingParent != null) {
            if (dx != 0 || dy != 0) {
                int startX = VISIBLE;
                int startY = VISIBLE;
                if (offsetInWindow != null) {
                    getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[VISIBLE];
                    startY = offsetInWindow[VIEW_IS_NOT_ADVIEW_OR_DISABLE];
                }
                if (consumed == null) {
                    if (this.mTempNestedScrollConsumed == null) {
                        this.mTempNestedScrollConsumed = new int[TEXT_DIRECTION_ANY_RTL];
                    }
                    consumed = this.mTempNestedScrollConsumed;
                }
                consumed[VISIBLE] = VISIBLE;
                consumed[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = VISIBLE;
                this.mNestedScrollingParent.onNestedPreScroll(this, dx, dy, consumed);
                if (offsetInWindow != null) {
                    getLocationInWindow(offsetInWindow);
                    offsetInWindow[VISIBLE] = offsetInWindow[VISIBLE] - startX;
                    offsetInWindow[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = offsetInWindow[VIEW_IS_NOT_ADVIEW_OR_DISABLE] - startY;
                }
                if (consumed[VISIBLE] == 0 && consumed[VIEW_IS_NOT_ADVIEW_OR_DISABLE] == 0) {
                    z = DBG;
                }
                return z;
            } else if (offsetInWindow != null) {
                offsetInWindow[VISIBLE] = VISIBLE;
                offsetInWindow[VIEW_IS_NOT_ADVIEW_OR_DISABLE] = VISIBLE;
            }
        }
        return DBG;
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        if (!isNestedScrollingEnabled() || this.mNestedScrollingParent == null) {
            return DBG;
        }
        return this.mNestedScrollingParent.onNestedFling(this, velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        if (!isNestedScrollingEnabled() || this.mNestedScrollingParent == null) {
            return DBG;
        }
        return this.mNestedScrollingParent.onNestedPreFling(this, velocityX, velocityY);
    }

    protected float getVerticalScrollFactor() {
        if (this.mVerticalScrollFactor == 0.0f) {
            TypedValue outValue = new TypedValue();
            if (this.mContext.getTheme().resolveAttribute(R.attr.listPreferredItemHeight, outValue, true)) {
                this.mVerticalScrollFactor = outValue.getDimension(this.mContext.getResources().getDisplayMetrics());
            } else {
                throw new IllegalStateException("Expected theme to define listPreferredItemHeight.");
            }
        }
        return this.mVerticalScrollFactor;
    }

    protected float getHorizontalScrollFactor() {
        return getVerticalScrollFactor();
    }

    @ExportedProperty(category = "text", mapping = {@IntToString(from = 0, to = "INHERIT"), @IntToString(from = 1, to = "FIRST_STRONG"), @IntToString(from = 2, to = "ANY_RTL"), @IntToString(from = 3, to = "LTR"), @IntToString(from = 4, to = "RTL"), @IntToString(from = 5, to = "LOCALE"), @IntToString(from = 6, to = "FIRST_STRONG_LTR"), @IntToString(from = 7, to = "FIRST_STRONG_RTL")})
    public int getRawTextDirection() {
        return (this.mPrivateFlags2 & PFLAG2_TEXT_DIRECTION_MASK) >> TEXT_DIRECTION_FIRST_STRONG_LTR;
    }

    public void setTextDirection(int textDirection) {
        if (getRawTextDirection() != textDirection) {
            this.mPrivateFlags2 &= -449;
            resetResolvedTextDirection();
            this.mPrivateFlags2 |= (textDirection << TEXT_DIRECTION_FIRST_STRONG_LTR) & PFLAG2_TEXT_DIRECTION_MASK;
            resolveTextDirection();
            onRtlPropertiesChanged(getLayoutDirection());
            requestLayout();
            invalidate(true);
        }
    }

    @ExportedProperty(category = "text", mapping = {@IntToString(from = 0, to = "INHERIT"), @IntToString(from = 1, to = "FIRST_STRONG"), @IntToString(from = 2, to = "ANY_RTL"), @IntToString(from = 3, to = "LTR"), @IntToString(from = 4, to = "RTL"), @IntToString(from = 5, to = "LOCALE"), @IntToString(from = 6, to = "FIRST_STRONG_LTR"), @IntToString(from = 7, to = "FIRST_STRONG_RTL")})
    public int getTextDirection() {
        return (this.mPrivateFlags2 & PFLAG2_TEXT_DIRECTION_RESOLVED_MASK) >> PFLAG2_TEXT_DIRECTION_RESOLVED_MASK_SHIFT;
    }

    public boolean resolveTextDirection() {
        this.mPrivateFlags2 &= -7681;
        if (hasRtlSupport()) {
            int textDirection = getRawTextDirection();
            switch (textDirection) {
                case VISIBLE /*0*/:
                    if (canResolveTextDirection()) {
                        try {
                            if (this.mParent == null || this.mParent.isTextDirectionResolved()) {
                                int parentResolvedDirection = VIEW_IS_NOT_CHECKED;
                                try {
                                    if (this.mParent != null) {
                                        parentResolvedDirection = this.mParent.getTextDirection();
                                    }
                                } catch (AbstractMethodError e) {
                                    Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                                    parentResolvedDirection = TEXT_DIRECTION_LTR;
                                }
                                switch (parentResolvedDirection) {
                                    case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                                    case TEXT_DIRECTION_ANY_RTL /*2*/:
                                    case TEXT_DIRECTION_LTR /*3*/:
                                    case TEXT_DIRECTION_RTL /*4*/:
                                    case TEXT_DIRECTION_LOCALE /*5*/:
                                    case TEXT_DIRECTION_FIRST_STRONG_LTR /*6*/:
                                    case TEXT_DIRECTION_FIRST_STRONG_RTL /*7*/:
                                        this.mPrivateFlags2 |= parentResolvedDirection << PFLAG2_TEXT_DIRECTION_RESOLVED_MASK_SHIFT;
                                        break;
                                    default:
                                        this.mPrivateFlags2 |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                                        break;
                                }
                            }
                            this.mPrivateFlags2 |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                            return DBG;
                        } catch (AbstractMethodError e2) {
                            Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e2);
                            this.mPrivateFlags2 |= SYSTEM_UI_LAYOUT_FLAGS;
                            return true;
                        }
                    }
                    this.mPrivateFlags2 |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    return DBG;
                case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                case TEXT_DIRECTION_ANY_RTL /*2*/:
                case TEXT_DIRECTION_LTR /*3*/:
                case TEXT_DIRECTION_RTL /*4*/:
                case TEXT_DIRECTION_LOCALE /*5*/:
                case TEXT_DIRECTION_FIRST_STRONG_LTR /*6*/:
                case TEXT_DIRECTION_FIRST_STRONG_RTL /*7*/:
                    this.mPrivateFlags2 |= textDirection << PFLAG2_TEXT_DIRECTION_RESOLVED_MASK_SHIFT;
                    break;
                default:
                    this.mPrivateFlags2 |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    break;
            }
        }
        this.mPrivateFlags2 |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        this.mPrivateFlags2 |= SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        return true;
    }

    public boolean canResolveTextDirection() {
        switch (getRawTextDirection()) {
            case VISIBLE /*0*/:
                if (this.mParent != null) {
                    try {
                        return this.mParent.canResolveTextDirection();
                    } catch (AbstractMethodError e) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                    }
                }
                return DBG;
            default:
                return true;
        }
    }

    public void resetResolvedTextDirection() {
        this.mPrivateFlags2 &= -7681;
        this.mPrivateFlags2 |= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    }

    public boolean isTextDirectionInherited() {
        return getRawTextDirection() == 0 ? true : DBG;
    }

    public boolean isTextDirectionResolved() {
        return (this.mPrivateFlags2 & SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) == SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION ? true : DBG;
    }

    @ExportedProperty(category = "text", mapping = {@IntToString(from = 0, to = "INHERIT"), @IntToString(from = 1, to = "GRAVITY"), @IntToString(from = 2, to = "TEXT_START"), @IntToString(from = 3, to = "TEXT_END"), @IntToString(from = 4, to = "CENTER"), @IntToString(from = 5, to = "VIEW_START"), @IntToString(from = 6, to = "VIEW_END")})
    public int getRawTextAlignment() {
        return (this.mPrivateFlags2 & PFLAG2_TEXT_ALIGNMENT_MASK) >> PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT;
    }

    public void setTextAlignment(int textAlignment) {
        if (textAlignment != getRawTextAlignment()) {
            this.mPrivateFlags2 &= -57345;
            resetResolvedTextAlignment();
            this.mPrivateFlags2 |= (textAlignment << PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT) & PFLAG2_TEXT_ALIGNMENT_MASK;
            resolveTextAlignment();
            onRtlPropertiesChanged(getLayoutDirection());
            requestLayout();
            invalidate(true);
        }
    }

    @ExportedProperty(category = "text", mapping = {@IntToString(from = 0, to = "INHERIT"), @IntToString(from = 1, to = "GRAVITY"), @IntToString(from = 2, to = "TEXT_START"), @IntToString(from = 3, to = "TEXT_END"), @IntToString(from = 4, to = "CENTER"), @IntToString(from = 5, to = "VIEW_START"), @IntToString(from = 6, to = "VIEW_END")})
    public int getTextAlignment() {
        return (this.mPrivateFlags2 & PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK) >> PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK_SHIFT;
    }

    public boolean resolveTextAlignment() {
        this.mPrivateFlags2 &= -983041;
        if (hasRtlSupport()) {
            int textAlignment = getRawTextAlignment();
            switch (textAlignment) {
                case VISIBLE /*0*/:
                    if (canResolveTextAlignment()) {
                        try {
                            if (this.mParent.isTextAlignmentResolved()) {
                                int parentResolvedTextAlignment;
                                try {
                                    parentResolvedTextAlignment = this.mParent.getTextAlignment();
                                } catch (AbstractMethodError e) {
                                    Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                                    parentResolvedTextAlignment = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
                                }
                                switch (parentResolvedTextAlignment) {
                                    case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                                    case TEXT_DIRECTION_ANY_RTL /*2*/:
                                    case TEXT_DIRECTION_LTR /*3*/:
                                    case TEXT_DIRECTION_RTL /*4*/:
                                    case TEXT_DIRECTION_LOCALE /*5*/:
                                    case TEXT_DIRECTION_FIRST_STRONG_LTR /*6*/:
                                        this.mPrivateFlags2 |= parentResolvedTextAlignment << PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK_SHIFT;
                                        break;
                                    default:
                                        this.mPrivateFlags2 |= WILL_NOT_CACHE_DRAWING;
                                        break;
                                }
                            }
                            this.mPrivateFlags2 |= WILL_NOT_CACHE_DRAWING;
                            return DBG;
                        } catch (AbstractMethodError e2) {
                            Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e2);
                            this.mPrivateFlags2 |= Protocol.BASE_DHCP;
                            return true;
                        }
                    }
                    this.mPrivateFlags2 |= WILL_NOT_CACHE_DRAWING;
                    return DBG;
                case VIEW_IS_NOT_ADVIEW_OR_DISABLE /*1*/:
                case TEXT_DIRECTION_ANY_RTL /*2*/:
                case TEXT_DIRECTION_LTR /*3*/:
                case TEXT_DIRECTION_RTL /*4*/:
                case TEXT_DIRECTION_LOCALE /*5*/:
                case TEXT_DIRECTION_FIRST_STRONG_LTR /*6*/:
                    this.mPrivateFlags2 |= textAlignment << PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK_SHIFT;
                    break;
                default:
                    this.mPrivateFlags2 |= WILL_NOT_CACHE_DRAWING;
                    break;
            }
        }
        this.mPrivateFlags2 |= WILL_NOT_CACHE_DRAWING;
        this.mPrivateFlags2 |= TV_PICTURE_IN_PICTURE_VISIBLE;
        return true;
    }

    public boolean canResolveTextAlignment() {
        switch (getRawTextAlignment()) {
            case VISIBLE /*0*/:
                if (this.mParent != null) {
                    try {
                        return this.mParent.canResolveTextAlignment();
                    } catch (AbstractMethodError e) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                    }
                }
                return DBG;
            default:
                return true;
        }
    }

    public void resetResolvedTextAlignment() {
        this.mPrivateFlags2 &= -983041;
        this.mPrivateFlags2 |= WILL_NOT_CACHE_DRAWING;
    }

    public boolean isTextAlignmentInherited() {
        return getRawTextAlignment() == 0 ? true : DBG;
    }

    public boolean isTextAlignmentResolved() {
        return (this.mPrivateFlags2 & TV_PICTURE_IN_PICTURE_VISIBLE) == TV_PICTURE_IN_PICTURE_VISIBLE ? true : DBG;
    }

    public static int generateViewId() {
        int result;
        int newValue;
        do {
            result = sNextGeneratedId.get();
            newValue = result + VIEW_IS_NOT_ADVIEW_OR_DISABLE;
            if (newValue > MEASURED_SIZE_MASK) {
                newValue = VIEW_IS_NOT_ADVIEW_OR_DISABLE;
            }
        } while (!sNextGeneratedId.compareAndSet(result, newValue));
        return result;
    }

    public void captureTransitioningViews(List<View> transitioningViews) {
        if (getVisibility() == 0) {
            transitioningViews.add(this);
        }
    }

    public void findNamedViews(Map<String, View> namedElements) {
        if (getVisibility() == 0 || this.mGhostView != null) {
            String transitionName = getTransitionName();
            if (transitionName != null) {
                namedElements.put(transitionName, this);
            }
        }
    }

    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        if (isDraggingScrollBar() || isOnScrollbarThumb(x, y)) {
            return PointerIcon.getSystemIcon(this.mContext, RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED);
        }
        return this.mPointerIcon;
    }

    public void setPointerIcon(PointerIcon pointerIcon) {
        this.mPointerIcon = pointerIcon;
        if (this.mAttachInfo != null && !this.mAttachInfo.mHandlingPointerEvent) {
            try {
                this.mAttachInfo.mSession.updatePointerIcon(this.mAttachInfo.mWindow);
            } catch (RemoteException e) {
            }
        }
    }

    public PointerIcon getPointerIcon() {
        return this.mPointerIcon;
    }

    public ViewPropertyAnimator animate() {
        if (this.mAnimator == null) {
            this.mAnimator = new ViewPropertyAnimator(this);
        }
        return this.mAnimator;
    }

    public final void setTransitionName(String transitionName) {
        this.mTransitionName = transitionName;
    }

    @ExportedProperty
    public String getTransitionName() {
        return this.mTransitionName;
    }

    public void requestKeyboardShortcuts(List<KeyboardShortcutGroup> list, int deviceId) {
    }

    private boolean inLiveRegion() {
        if (getAccessibilityLiveRegion() != 0) {
            return true;
        }
        for (ViewParent parent = getParent(); parent instanceof View; parent = parent.getParent()) {
            if (((View) parent).getAccessibilityLiveRegion() != 0) {
                return true;
            }
        }
        return DBG;
    }

    private static void dumpFlags() {
        HashMap<String, String> found = Maps.newHashMap();
        try {
            Field[] declaredFields = View.class.getDeclaredFields();
            int length = declaredFields.length;
            for (int i = VISIBLE; i < length; i += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                Field field = declaredFields[i];
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    if (field.getType().equals(Integer.TYPE)) {
                        dumpFlag(found, field.getName(), field.getInt(null));
                    } else if (field.getType().equals(int[].class)) {
                        int[] values = (int[]) field.get(null);
                        for (int i2 = VISIBLE; i2 < values.length; i2 += VIEW_IS_NOT_ADVIEW_OR_DISABLE) {
                            dumpFlag(found, field.getName() + "[" + i2 + "]", values[i2]);
                        }
                    }
                }
            }
            ArrayList<String> keys = Lists.newArrayList();
            keys.addAll(found.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                Log.d(VIEW_LOG_TAG, (String) found.get(key));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void dumpFlag(HashMap<String, String> found, String name, int value) {
        String substring;
        Object[] objArr = new Object[VIEW_IS_NOT_ADVIEW_OR_DISABLE];
        objArr[VISIBLE] = Integer.toBinaryString(value);
        String bits = String.format("%32s", objArr).replace('0', ' ');
        int prefix = name.indexOf(95);
        StringBuilder stringBuilder = new StringBuilder();
        if (prefix > 0) {
            substring = name.substring(VISIBLE, prefix);
        } else {
            substring = name;
        }
        found.put(stringBuilder.append(substring).append(bits).append(name).toString(), bits + " " + name);
    }

    public void encode(ViewHierarchyEncoder stream) {
        stream.beginObject(this);
        encodeProperties(stream);
        stream.endObject();
    }

    protected void encodeProperties(ViewHierarchyEncoder stream) {
        Object resolveId = ViewDebug.resolveId(getContext(), this.mID);
        if (resolveId instanceof String) {
            stream.addProperty("id", (String) resolveId);
        } else {
            stream.addProperty("id", this.mID);
        }
        stream.addProperty("misc:transformation.alpha", this.mTransformationInfo != null ? this.mTransformationInfo.mAlpha : 0.0f);
        stream.addProperty("misc:transitionName", getTransitionName());
        stream.addProperty("layout:left", this.mLeft);
        stream.addProperty("layout:right", this.mRight);
        stream.addProperty("layout:top", this.mTop);
        stream.addProperty("layout:bottom", this.mBottom);
        stream.addProperty("layout:width", getWidth());
        stream.addProperty("layout:height", getHeight());
        stream.addProperty("layout:layoutDirection", getLayoutDirection());
        stream.addProperty("layout:layoutRtl", isLayoutRtl());
        stream.addProperty("layout:hasTransientState", hasTransientState());
        stream.addProperty("layout:baseline", getBaseline());
        LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            stream.addPropertyKey("layoutParams");
            layoutParams.encode(stream);
        }
        stream.addProperty("scrolling:scrollX", this.mScrollX);
        stream.addProperty("scrolling:scrollY", this.mScrollY);
        stream.addProperty("padding:paddingLeft", this.mPaddingLeft);
        stream.addProperty("padding:paddingRight", this.mPaddingRight);
        stream.addProperty("padding:paddingTop", this.mPaddingTop);
        stream.addProperty("padding:paddingBottom", this.mPaddingBottom);
        stream.addProperty("padding:userPaddingRight", this.mUserPaddingRight);
        stream.addProperty("padding:userPaddingLeft", this.mUserPaddingLeft);
        stream.addProperty("padding:userPaddingBottom", this.mUserPaddingBottom);
        stream.addProperty("padding:userPaddingStart", this.mUserPaddingStart);
        stream.addProperty("padding:userPaddingEnd", this.mUserPaddingEnd);
        stream.addProperty("measurement:minHeight", this.mMinHeight);
        stream.addProperty("measurement:minWidth", this.mMinWidth);
        stream.addProperty("measurement:measuredWidth", this.mMeasuredWidth);
        stream.addProperty("measurement:measuredHeight", this.mMeasuredHeight);
        stream.addProperty("drawing:elevation", getElevation());
        stream.addProperty("drawing:translationX", getTranslationX());
        stream.addProperty("drawing:translationY", getTranslationY());
        stream.addProperty("drawing:translationZ", getTranslationZ());
        stream.addProperty("drawing:rotation", getRotation());
        stream.addProperty("drawing:rotationX", getRotationX());
        stream.addProperty("drawing:rotationY", getRotationY());
        stream.addProperty("drawing:scaleX", getScaleX());
        stream.addProperty("drawing:scaleY", getScaleY());
        stream.addProperty("drawing:pivotX", getPivotX());
        stream.addProperty("drawing:pivotY", getPivotY());
        stream.addProperty("drawing:opaque", isOpaque());
        stream.addProperty("drawing:alpha", getAlpha());
        stream.addProperty("drawing:transitionAlpha", getTransitionAlpha());
        stream.addProperty("drawing:shadow", hasShadow());
        stream.addProperty("drawing:solidColor", getSolidColor());
        stream.addProperty("drawing:layerType", this.mLayerType);
        stream.addProperty("drawing:willNotDraw", willNotDraw());
        stream.addProperty("drawing:hardwareAccelerated", isHardwareAccelerated());
        stream.addProperty("drawing:willNotCacheDrawing", willNotCacheDrawing());
        stream.addProperty("drawing:drawingCacheEnabled", isDrawingCacheEnabled());
        stream.addProperty("drawing:overlappingRendering", hasOverlappingRendering());
        stream.addProperty("focus:hasFocus", hasFocus());
        stream.addProperty("focus:isFocused", isFocused());
        stream.addProperty("focus:isFocusable", isFocusable());
        stream.addProperty("focus:isFocusableInTouchMode", isFocusableInTouchMode());
        stream.addProperty("misc:clickable", isClickable());
        stream.addProperty("misc:pressed", isPressed());
        stream.addProperty("misc:selected", isSelected());
        stream.addProperty("misc:touchMode", isInTouchMode());
        stream.addProperty("misc:hovered", isHovered());
        stream.addProperty("misc:activated", isActivated());
        stream.addProperty("misc:visibility", getVisibility());
        stream.addProperty("misc:fitsSystemWindows", getFitsSystemWindows());
        stream.addProperty("misc:filterTouchesWhenObscured", getFilterTouchesWhenObscured());
        stream.addProperty("misc:enabled", isEnabled());
        stream.addProperty("misc:soundEffectsEnabled", isSoundEffectsEnabled());
        stream.addProperty("misc:hapticFeedbackEnabled", isHapticFeedbackEnabled());
        Theme theme = getContext().getTheme();
        if (theme != null) {
            stream.addPropertyKey("theme");
            theme.encode(stream);
        }
        int n = this.mAttributes != null ? this.mAttributes.length : VISIBLE;
        stream.addProperty("meta:__attrCount__", n / TEXT_DIRECTION_ANY_RTL);
        for (int i = VISIBLE; i < n; i += TEXT_DIRECTION_ANY_RTL) {
            stream.addProperty("meta:__attr__" + this.mAttributes[i], this.mAttributes[i + VIEW_IS_NOT_ADVIEW_OR_DISABLE]);
        }
        stream.addProperty("misc:scrollBarStyle", getScrollBarStyle());
        stream.addProperty("text:textDirection", getTextDirection());
        stream.addProperty("text:textAlignment", getTextAlignment());
        CharSequence contentDescription = getContentDescription();
        stream.addProperty("accessibility:contentDescription", contentDescription == null ? "" : contentDescription.toString());
        stream.addProperty("accessibility:labelFor", getLabelFor());
        stream.addProperty("accessibility:importantForAccessibility", getImportantForAccessibility());
    }

    public boolean hasAncestorThatBlocksDescendantFocusOuter() {
        return hasAncestorThatBlocksDescendantFocus();
    }

    public AttachInfo getAttachInfo() {
        return this.mAttachInfo;
    }

    public void setTouchInOtherThread(boolean touchable) {
        this.mCanTouchInOtherThread = touchable;
    }

    public boolean isTouchableInOtherThread() {
        return this.mCanTouchInOtherThread;
    }

    public HwStateListAnimator getHwStateListAnimator() {
        return this.mHwStateListAnimator;
    }

    public void setHwStateListAnimator(HwStateListAnimator stateListAnimator) {
        if (this.mHwStateListAnimator != stateListAnimator) {
            if (this.mHwStateListAnimator != null) {
                this.mHwStateListAnimator.setTarget(null);
            }
            this.mHwStateListAnimator = stateListAnimator;
            if (stateListAnimator != null) {
                stateListAnimator.setTarget(this);
                if (isAttachedToWindow()) {
                    stateListAnimator.setState(getDrawableState());
                }
            }
        }
    }

    protected Drawable initializeVariousScrollIndicators(Context context) {
        return context.getDrawable(R.drawable.scroll_indicator_material);
    }

    protected boolean reSizeScrollIndicators(int[] xy) {
        return DBG;
    }
}
