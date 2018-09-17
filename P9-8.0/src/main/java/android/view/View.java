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
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Interpolator;
import android.graphics.Interpolator.Result;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Style;
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
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.SettingsStringUtil;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatProperty;
import android.util.Log;
import android.util.LogException;
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
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ScrollBarDrawable;
import com.android.internal.R;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.view.TooltipPopup;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.widget.ScrollBarUtils;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import huawei.android.animation.HwStateListAnimator;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import javax.microedition.khronos.opengles.GL10;

public class View implements Callback, KeyEvent.Callback, AccessibilityEventSource {
    public static final int ACCESSIBILITY_CURSOR_POSITION_UNDEFINED = -1;
    public static final int ACCESSIBILITY_LIVE_REGION_ASSERTIVE = 2;
    static final int ACCESSIBILITY_LIVE_REGION_DEFAULT = 0;
    public static final int ACCESSIBILITY_LIVE_REGION_NONE = 0;
    public static final int ACCESSIBILITY_LIVE_REGION_POLITE = 1;
    static final int ALL_RTL_PROPERTIES_RESOLVED = 1610678816;
    public static final Property<View, Float> ALPHA = new FloatProperty<View>("alpha") {
        public void setValue(View object, float value) {
            object.setAlpha(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getAlpha());
        }
    };
    public static final int AUTOFILL_FLAG_INCLUDE_NOT_IMPORTANT_VIEWS = 1;
    private static final int[] AUTOFILL_HIGHLIGHT_ATTR = new int[]{R.attr.autofilledHighlight};
    public static final String AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE = "creditCardExpirationDate";
    public static final String AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY = "creditCardExpirationDay";
    public static final String AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH = "creditCardExpirationMonth";
    public static final String AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR = "creditCardExpirationYear";
    public static final String AUTOFILL_HINT_CREDIT_CARD_NUMBER = "creditCardNumber";
    public static final String AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE = "creditCardSecurityCode";
    public static final String AUTOFILL_HINT_EMAIL_ADDRESS = "emailAddress";
    public static final String AUTOFILL_HINT_NAME = "name";
    public static final String AUTOFILL_HINT_PASSWORD = "password";
    public static final String AUTOFILL_HINT_PHONE = "phone";
    public static final String AUTOFILL_HINT_POSTAL_ADDRESS = "postalAddress";
    public static final String AUTOFILL_HINT_POSTAL_CODE = "postalCode";
    public static final String AUTOFILL_HINT_USERNAME = "username";
    public static final int AUTOFILL_TYPE_DATE = 4;
    public static final int AUTOFILL_TYPE_LIST = 3;
    public static final int AUTOFILL_TYPE_NONE = 0;
    public static final int AUTOFILL_TYPE_TEXT = 1;
    public static final int AUTOFILL_TYPE_TOGGLE = 2;
    static final int CLICKABLE = 16384;
    static final int CONTEXT_CLICKABLE = 8388608;
    private static final boolean DBG = false;
    static final int DEBUG_CORNERS_COLOR = Color.rgb(63, 127, 255);
    static final int DEBUG_CORNERS_SIZE_DIP = 8;
    public static boolean DEBUG_DRAW = false;
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
    private static final int[] DRAWING_CACHE_QUALITY_FLAGS = new int[]{0, 524288, 1048576};
    public static final int DRAWING_CACHE_QUALITY_HIGH = 1048576;
    public static final int DRAWING_CACHE_QUALITY_LOW = 524288;
    static final int DRAWING_CACHE_QUALITY_MASK = 1572864;
    static final int DRAW_MASK = 128;
    static final int DUPLICATE_PARENT_STATE = 4194304;
    protected static final int[] EMPTY_STATE_SET = StateSet.get(0);
    static final int ENABLED = 0;
    protected static final int[] ENABLED_FOCUSED_SELECTED_STATE_SET = StateSet.get(14);
    protected static final int[] ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = StateSet.get(15);
    protected static final int[] ENABLED_FOCUSED_STATE_SET = StateSet.get(12);
    protected static final int[] ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET = StateSet.get(13);
    static final int ENABLED_MASK = 32;
    protected static final int[] ENABLED_SELECTED_STATE_SET = StateSet.get(10);
    protected static final int[] ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET = StateSet.get(11);
    protected static final int[] ENABLED_STATE_SET = StateSet.get(8);
    protected static final int[] ENABLED_WINDOW_FOCUSED_STATE_SET = StateSet.get(9);
    static final int FADING_EDGE_HORIZONTAL = 4096;
    static final int FADING_EDGE_MASK = 12288;
    static final int FADING_EDGE_NONE = 0;
    static final int FADING_EDGE_VERTICAL = 8192;
    static final int FILTER_TOUCHES_WHEN_OBSCURED = 1024;
    public static final int FIND_VIEWS_WITH_ACCESSIBILITY_NODE_PROVIDERS = 4;
    public static final int FIND_VIEWS_WITH_CONTENT_DESCRIPTION = 2;
    public static final int FIND_VIEWS_WITH_TEXT = 1;
    private static final int FITS_SYSTEM_WINDOWS = 2;
    public static final int FOCUSABLE = 1;
    public static final int FOCUSABLES_ALL = 0;
    public static final int FOCUSABLES_TOUCH_MODE = 1;
    public static final int FOCUSABLE_AUTO = 16;
    static final int FOCUSABLE_IN_TOUCH_MODE = 262144;
    private static final int FOCUSABLE_MASK = 17;
    protected static final int[] FOCUSED_SELECTED_STATE_SET = StateSet.get(6);
    protected static final int[] FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = StateSet.get(7);
    protected static final int[] FOCUSED_STATE_SET = StateSet.get(4);
    protected static final int[] FOCUSED_WINDOW_FOCUSED_STATE_SET = StateSet.get(5);
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
    public static final int IMPORTANT_FOR_AUTOFILL_AUTO = 0;
    public static final int IMPORTANT_FOR_AUTOFILL_NO = 2;
    public static final int IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS = 8;
    public static final int IMPORTANT_FOR_AUTOFILL_YES = 1;
    public static final int IMPORTANT_FOR_AUTOFILL_YES_EXCLUDE_DESCENDANTS = 4;
    public static final int INVISIBLE = 4;
    public static final int KEEP_SCREEN_ON = 67108864;
    public static final int LAST_APP_AUTOFILL_ID = 1073741823;
    public static final int LAYER_TYPE_HARDWARE = 2;
    public static final int LAYER_TYPE_NONE = 0;
    public static final int LAYER_TYPE_SOFTWARE = 1;
    private static final int LAYOUT_DIRECTION_DEFAULT = 2;
    private static final int[] LAYOUT_DIRECTION_FLAGS = new int[]{0, 1, 2, 3};
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
    public static final int NOT_FOCUSABLE = 0;
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
    private static final int[] PFLAG2_TEXT_ALIGNMENT_FLAGS = new int[]{0, 8192, 16384, 24576, 32768, 40960, 49152};
    static final int PFLAG2_TEXT_ALIGNMENT_MASK = 57344;
    static final int PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT = 13;
    static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED = 65536;
    private static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED_DEFAULT = 131072;
    static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK = 917504;
    static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK_SHIFT = 17;
    private static final int[] PFLAG2_TEXT_DIRECTION_FLAGS = new int[]{0, 64, 128, 192, 256, 320, MetricsEvent.ACTION_SHOW_SETTINGS_SUGGESTION, PFLAG2_TEXT_DIRECTION_MASK};
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
    private static final int PFLAG3_CLUSTER = 32768;
    private static final int PFLAG3_FINGER_DOWN = 131072;
    static final int PFLAG3_FITTING_SYSTEM_WINDOWS = 64;
    private static final int PFLAG3_FOCUSED_BY_DEFAULT = 262144;
    private static final int PFLAG3_HAS_OVERLAPPING_RENDERING_FORCED = 16777216;
    static final int PFLAG3_IMPORTANT_FOR_AUTOFILL_MASK = 7864320;
    static final int PFLAG3_IMPORTANT_FOR_AUTOFILL_SHIFT = 19;
    private static final int PFLAG3_IS_AUTOFILLED = 65536;
    static final int PFLAG3_IS_LAID_OUT = 4;
    static final int PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT = 8;
    static final int PFLAG3_NESTED_SCROLLING_ENABLED = 128;
    static final int PFLAG3_NOTIFY_AUTOFILL_ENTER_ON_LAYOUT = 134217728;
    private static final int PFLAG3_NO_REVEAL_ON_FOCUS = 67108864;
    private static final int PFLAG3_OVERLAPPING_RENDERING_FORCED_VALUE = 8388608;
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
    protected static final int[] PRESSED_ENABLED_FOCUSED_SELECTED_STATE_SET = StateSet.get(30);
    protected static final int[] PRESSED_ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = StateSet.get(31);
    protected static final int[] PRESSED_ENABLED_FOCUSED_STATE_SET = StateSet.get(28);
    protected static final int[] PRESSED_ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET = StateSet.get(29);
    protected static final int[] PRESSED_ENABLED_SELECTED_STATE_SET = StateSet.get(26);
    protected static final int[] PRESSED_ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET = StateSet.get(27);
    protected static final int[] PRESSED_ENABLED_STATE_SET = StateSet.get(24);
    protected static final int[] PRESSED_ENABLED_WINDOW_FOCUSED_STATE_SET = StateSet.get(25);
    protected static final int[] PRESSED_FOCUSED_SELECTED_STATE_SET = StateSet.get(22);
    protected static final int[] PRESSED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET = StateSet.get(23);
    protected static final int[] PRESSED_FOCUSED_STATE_SET = StateSet.get(20);
    protected static final int[] PRESSED_FOCUSED_WINDOW_FOCUSED_STATE_SET = StateSet.get(21);
    protected static final int[] PRESSED_SELECTED_STATE_SET = StateSet.get(18);
    protected static final int[] PRESSED_SELECTED_WINDOW_FOCUSED_STATE_SET = StateSet.get(19);
    protected static final int[] PRESSED_STATE_SET = StateSet.get(16);
    protected static final int[] PRESSED_WINDOW_FOCUSED_STATE_SET = StateSet.get(17);
    private static final int PROVIDER_BACKGROUND = 0;
    private static final int PROVIDER_BOUNDS = 2;
    private static final int PROVIDER_NONE = 1;
    private static final int PROVIDER_PADDED_BOUNDS = 3;
    public static final int PUBLIC_STATUS_BAR_VISIBILITY_MASK = 16375;
    public static final Property<View, Float> ROTATION = new FloatProperty<View>("rotation") {
        public void setValue(View object, float value) {
            object.setRotation(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getRotation());
        }
    };
    public static final Property<View, Float> ROTATION_X = new FloatProperty<View>("rotationX") {
        public void setValue(View object, float value) {
            object.setRotationX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getRotationX());
        }
    };
    public static final Property<View, Float> ROTATION_Y = new FloatProperty<View>("rotationY") {
        public void setValue(View object, float value) {
            object.setRotationY(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getRotationY());
        }
    };
    static final int SAVE_DISABLED = 65536;
    static final int SAVE_DISABLED_MASK = 65536;
    public static final Property<View, Float> SCALE_X = new FloatProperty<View>("scaleX") {
        public void setValue(View object, float value) {
            object.setScaleX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getScaleX());
        }
    };
    public static final Property<View, Float> SCALE_Y = new FloatProperty<View>("scaleY") {
        public void setValue(View object, float value) {
            object.setScaleY(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getScaleY());
        }
    };
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
    protected static final int[] SELECTED_STATE_SET = StateSet.get(2);
    protected static final int[] SELECTED_WINDOW_FOCUSED_STATE_SET = StateSet.get(3);
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
    @Deprecated
    public static final int STATUS_BAR_HIDDEN = 1;
    public static final int STATUS_BAR_TRANSIENT = 67108864;
    public static final int STATUS_BAR_TRANSLUCENT = 1073741824;
    public static final int STATUS_BAR_TRANSPARENT = 8;
    public static final int STATUS_BAR_UNHIDE = 268435456;
    @Deprecated
    public static final int STATUS_BAR_VISIBLE = 0;
    public static final int SYSTEM_UI_CLEARABLE_FLAGS = 7;
    public static final int SYSTEM_UI_FLAG_FULLSCREEN = 4;
    public static final int SYSTEM_UI_FLAG_HIDE_NAVIGATION = 2;
    public static final int SYSTEM_UI_FLAG_IMMERSIVE = 2048;
    public static final int SYSTEM_UI_FLAG_IMMERSIVE_STICKY = 4096;
    public static final int SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = 1024;
    public static final int SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = 512;
    public static final int SYSTEM_UI_FLAG_LAYOUT_STABLE = 256;
    public static final int SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR = 16;
    public static final int SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = 8192;
    public static final int SYSTEM_UI_FLAG_LOW_PROFILE = 1;
    public static final int SYSTEM_UI_FLAG_VISIBLE = 0;
    public static final int SYSTEM_UI_LAYOUT_FLAGS = 1536;
    private static final int SYSTEM_UI_RESERVED_LEGACY1 = 16384;
    private static final int SYSTEM_UI_RESERVED_LEGACY2 = 65536;
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
    static final int TOOLTIP = 1073741824;
    public static final Property<View, Float> TRANSLATION_X = new FloatProperty<View>("translationX") {
        public void setValue(View object, float value) {
            object.setTranslationX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getTranslationX());
        }
    };
    public static final Property<View, Float> TRANSLATION_Y = new FloatProperty<View>("translationY") {
        public void setValue(View object, float value) {
            object.setTranslationY(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getTranslationY());
        }
    };
    public static final Property<View, Float> TRANSLATION_Z = new FloatProperty<View>("translationZ") {
        public void setValue(View object, float value) {
            object.setTranslationZ(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getTranslationZ());
        }
    };
    private static final int UNDEFINED_PADDING = Integer.MIN_VALUE;
    private static final int VIEW_IS_ADVIEW = 0;
    private static final int VIEW_IS_NOT_ADVIEW_OR_DISABLE = 1;
    private static final int VIEW_IS_NOT_CHECKED = -1;
    protected static final String VIEW_LOG_TAG = "View";
    private static final int[] VISIBILITY_FLAGS = new int[]{0, 4, 8};
    static final int VISIBILITY_MASK = 12;
    public static final int VISIBLE = 0;
    static final int WILL_NOT_CACHE_DRAWING = 131072;
    static final int WILL_NOT_DRAW = 128;
    protected static final int[] WINDOW_FOCUSED_STATE_SET = StateSet.get(1);
    public static final Property<View, Float> X = new FloatProperty<View>("x") {
        public void setValue(View object, float value) {
            object.setX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getX());
        }
    };
    public static final Property<View, Float> Y = new FloatProperty<View>("y") {
        public void setValue(View object, float value) {
            object.setY(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getY());
        }
    };
    public static final Property<View, Float> Z = new FloatProperty<View>("z") {
        public void setValue(View object, float value) {
            object.setZ(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getZ());
        }
    };
    private static SparseArray<String> mAttributeMap;
    public static boolean mDebugViewAttributes = false;
    private static boolean sAlwaysRemeasureExactly = false;
    private static boolean sAutoFocusableOffUIThreadWontNotifyParents;
    static boolean sCascadedDragDrop;
    private static boolean sCompatibilityDone = false;
    private static Paint sDebugPaint;
    static boolean sHasFocusableExcludeAutoFocusable;
    private static boolean sIgnoreMeasureCache = false;
    private static boolean sLayoutParamsAlwaysChanged = false;
    private static int sNextAccessibilityViewId;
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    protected static boolean sPreserveMarginParamsInLayoutParamConversion;
    static boolean sTextureViewIgnoresDrawableSetters = false;
    static final ThreadLocal<Rect> sThreadLocal = new ThreadLocal();
    private static boolean sUseBrokenMakeMeasureSpec = false;
    private static boolean sUseDefaultFocusHighlight;
    static boolean sUseZeroUnspecifiedMeasureSpec = false;
    private int mAccessibilityCursorPosition;
    AccessibilityDelegate mAccessibilityDelegate;
    private int mAccessibilityTraversalAfterId;
    private int mAccessibilityTraversalBeforeId;
    private int mAccessibilityViewId;
    private ViewPropertyAnimator mAnimator;
    AttachInfo mAttachInfo;
    @ExportedProperty(category = "attributes", hasAdjacentMapping = true)
    public String[] mAttributes;
    private String[] mAutofillHints;
    private AutofillId mAutofillId;
    private int mAutofillViewId;
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
    private Drawable mDefaultFocusHighlight;
    private Drawable mDefaultFocusHighlightCache;
    boolean mDefaultFocusHighlightEnabled;
    private boolean mDefaultFocusHighlightSizeChanged;
    private int[] mDrawableState;
    private Bitmap mDrawingCache;
    private int mDrawingCacheBackgroundColor;
    private ViewTreeObserver mFloatingTreeObserver;
    private boolean mForceRTL;
    @ExportedProperty(deepExport = true, prefix = "fg_")
    private ForegroundInfo mForegroundInfo;
    private ArrayList<FrameMetricsObserver> mFrameMetricsObservers;
    GhostView mGhostView = this;
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
    int mNextClusterForwardId;
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
    public int mPrivateFlags;
    int mPrivateFlags2;
    int mPrivateFlags3;
    boolean mRecreateDisplayList;
    final RenderNode mRenderNode;
    private final Resources mResources;
    @ExportedProperty(category = "layout")
    protected int mRight;
    private boolean mRightPaddingDefined;
    private RoundScrollbarRenderer mRoundScrollbarRenderer;
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
    TooltipInfo mTooltipInfo;
    @ExportedProperty(category = "layout")
    protected int mTop;
    private TouchDelegate mTouchDelegate;
    private int mTouchSlop;
    public TransformationInfo mTransformationInfo;
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
    private Handler mVisibilityChangeForAutofillHandler;
    int mWindowAttachCount;

    public interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View view);

        void onViewDetachedFromWindow(View view);
    }

    public interface OnClickListener {
        void onClick(View view);
    }

    public interface OnTouchListener {
        boolean onTouch(View view, MotionEvent motionEvent);
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

        public void addExtraDataToAccessibilityNodeInfo(View host, AccessibilityNodeInfo info, String extraDataKey, Bundle arguments) {
            host.addExtraDataToAccessibilityNodeInfo(info, extraDataKey, arguments);
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

    static final class AttachInfo {
        int mAccessibilityFetchFlags;
        Drawable mAccessibilityFocusDrawable;
        int mAccessibilityWindowId = -1;
        boolean mAlwaysConsumeNavBar;
        float mApplicationScale;
        Drawable mAutofilledDrawable;
        Canvas mCanvas;
        final Rect mContentInsets = new Rect();
        boolean mDebugLayout = SystemProperties.getBoolean(View.DEBUG_LAYOUT_PROPERTY, false);
        int mDisabledSystemUiVisibility;
        Display mDisplay;
        int mDisplayState = 0;
        public Surface mDragSurface;
        IBinder mDragToken;
        long mDrawingTime;
        List<View> mEmptyPartialLayoutViews;
        boolean mForceReportNewAttributes;
        final InternalInsetsInfo mGivenInternalInsets = new InternalInsetsInfo();
        int mGlobalSystemUiVisibility = -1;
        final Handler mHandler;
        boolean mHandlingPointerEvent;
        boolean mHardwareAccelerated;
        boolean mHardwareAccelerationRequested;
        boolean mHasNonEmptyGivenInternalInsets;
        boolean mHasSystemUiListeners;
        boolean mHasWindowFocus;
        boolean mHighContrastText;
        IWindowId mIWindowId;
        boolean mIgnoreDirtyState;
        boolean mInTouchMode;
        final int[] mInvalidateChildLocation = new int[2];
        boolean mKeepScreenOn;
        final DispatcherState mKeyDispatchState = new DispatcherState();
        boolean mNeedsUpdateLightCenter;
        final Rect mOutsets = new Rect();
        final Rect mOverscanInsets = new Rect();
        boolean mOverscanRequested;
        IBinder mPanelParentWindowToken;
        List<View> mPartialLayoutViews = new ArrayList();
        List<RenderNode> mPendingAnimatingRenderNodes;
        final Point mPoint = new Point();
        boolean mRecomputeGlobalAttributes;
        final Callbacks mRootCallbacks;
        View mRootView;
        boolean mScalingRequired;
        final ArrayList<View> mScrollContainers = new ArrayList();
        final IWindowSession mSession;
        boolean mSetIgnoreDirtyState = false;
        final Rect mStableInsets = new Rect();
        int mSystemUiVisibility;
        final ArrayList<View> mTempArrayList = new ArrayList(24);
        ThreadedRenderer mThreadedRenderer;
        final Rect mTmpInvalRect = new Rect();
        final int[] mTmpLocation = new int[2];
        final Matrix mTmpMatrix = new Matrix();
        final Outline mTmpOutline = new Outline();
        final List<RectF> mTmpRectList = new ArrayList();
        final float[] mTmpTransformLocation = new float[2];
        final RectF mTmpTransformRect = new RectF();
        final RectF mTmpTransformRect1 = new RectF();
        final Transformation mTmpTransformation = new Transformation();
        View mTooltipHost;
        final int[] mTransparentLocation = new int[2];
        final ViewTreeObserver mTreeObserver;
        boolean mUnbufferedDispatchRequested;
        boolean mUse32BitDrawingCache;
        View mViewRequestingLayout;
        final ViewRootImpl mViewRootImpl;
        boolean mViewScrollChanged;
        boolean mViewVisibilityChanged;
        final Rect mVisibleInsets = new Rect();
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
            private static final SynchronizedPool<InvalidateInfo> sPool = new SynchronizedPool(10);
            int bottom;
            int left;
            int right;
            View target;
            int top;

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

        AttachInfo(IWindowSession session, IWindow window, Display display, ViewRootImpl viewRootImpl, Handler handler, Callbacks effectPlayer, Context context) {
            this.mSession = session;
            this.mWindow = window;
            this.mWindowToken = window.asBinder();
            this.mDisplay = display;
            this.mViewRootImpl = viewRootImpl;
            this.mHandler = handler;
            this.mRootCallbacks = effectPlayer;
            this.mTreeObserver = new ViewTreeObserver(context);
        }
    }

    public static class BaseSavedState extends AbsSavedState {
        static final int AUTOFILL_ID = 4;
        public static final Creator<BaseSavedState> CREATOR = new ClassLoaderCreator<BaseSavedState>() {
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }

            public BaseSavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new BaseSavedState(in, loader);
            }

            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        };
        static final int IS_AUTOFILLED = 2;
        static final int START_ACTIVITY_REQUESTED_WHO_SAVED = 1;
        int mAutofillViewId;
        boolean mIsAutofilled;
        int mSavedData;
        String mStartActivityRequestWhoSaved;

        public BaseSavedState(Parcel source) {
            this(source, null);
        }

        public BaseSavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            this.mSavedData = source.readInt();
            this.mStartActivityRequestWhoSaved = source.readString();
            this.mIsAutofilled = source.readBoolean();
            this.mAutofillViewId = source.readInt();
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.mSavedData);
            out.writeString(this.mStartActivityRequestWhoSaved);
            out.writeBoolean(this.mIsAutofilled);
            out.writeInt(this.mAutofillViewId);
        }
    }

    private final class CheckForLongPress implements Runnable {
        private boolean mOriginalPressedState;
        private int mOriginalWindowAttachCount;
        private float mX;
        private float mY;

        /* synthetic */ CheckForLongPress(View this$0, CheckForLongPress -this1) {
            this();
        }

        private CheckForLongPress() {
        }

        public void run() {
            if (this.mOriginalPressedState == View.this.isPressed() && View.this.mParent != null && this.mOriginalWindowAttachCount == View.this.mWindowAttachCount && !View.this.getRootView().isLongPressSwipe() && View.this.performLongClick(this.mX, this.mY)) {
                View.this.mHasPerformedLongPress = true;
            }
        }

        public void setAnchor(float x, float y) {
            this.mX = x;
            this.mY = y;
        }

        public void rememberWindowAttachCount() {
            this.mOriginalWindowAttachCount = View.this.mWindowAttachCount;
        }

        public void rememberPressedState() {
            this.mOriginalPressedState = View.this.isPressed();
        }
    }

    private final class CheckForTap implements Runnable {
        public float x;
        public float y;

        /* synthetic */ CheckForTap(View this$0, CheckForTap -this1) {
            this();
        }

        private CheckForTap() {
        }

        public void run() {
            View view = View.this;
            view.mPrivateFlags &= -33554433;
            View.this.setPressed(true, this.x, this.y);
            View.this.checkForLongClick(ViewConfiguration.getTapTimeout(), this.x, this.y);
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
                this.mResolvedMethod.invoke(this.mResolvedContext, new Object[]{v});
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
                        Method method = context.getClass().getMethod(this.mMethodName, new Class[]{View.class});
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
            throw new IllegalStateException("Could not find method " + this.mMethodName + "(View) in a parent or ancestor Context for android:onClick " + "attribute defined on view " + this.mHostView.getClass() + (id == -1 ? LogException.NO_VALUE : " with id '" + this.mHostView.getContext().getResources().getResourceEntryName(id) + "'"));
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
                outShadowTouchPoint.set(outShadowSize.x / 2, outShadowSize.y / 2);
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

        /* synthetic */ ForegroundInfo(ForegroundInfo -this0) {
            this();
        }

        private ForegroundInfo() {
            this.mGravity = 119;
            this.mInsidePadding = true;
            this.mBoundsChanged = true;
            this.mSelfBounds = new Rect();
            this.mOverlayBounds = new Rect();
        }
    }

    static class ListenerInfo {
        OnApplyWindowInsetsListener mOnApplyWindowInsetsListener;
        private CopyOnWriteArrayList<OnAttachStateChangeListener> mOnAttachStateChangeListeners;
        OnCapturedPointerListener mOnCapturedPointerListener;
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

    private static class MatchIdPredicate implements Predicate<View> {
        public int mId;

        /* synthetic */ MatchIdPredicate(MatchIdPredicate -this0) {
            this();
        }

        private MatchIdPredicate() {
        }

        public boolean test(View view) {
            return view.mID == this.mId;
        }
    }

    private static class MatchLabelForPredicate implements Predicate<View> {
        private int mLabeledId;

        /* synthetic */ MatchLabelForPredicate(MatchLabelForPredicate -this0) {
            this();
        }

        private MatchLabelForPredicate() {
        }

        public boolean test(View view) {
            return view.mLabelForId == this.mLabeledId;
        }
    }

    public static class MeasureSpec {
        public static final int AT_MOST = Integer.MIN_VALUE;
        public static final int EXACTLY = 1073741824;
        private static final int MODE_MASK = -1073741824;
        private static final int MODE_SHIFT = 30;
        public static final int UNSPECIFIED = 0;

        public static int makeMeasureSpec(int size, int mode) {
            if (View.sUseBrokenMakeMeasureSpec) {
                return size + mode;
            }
            return (View.LAST_APP_AUTOFILL_ID & size) | (MODE_MASK & mode);
        }

        public static int makeSafeMeasureSpec(int size, int mode) {
            if (View.sUseZeroUnspecifiedMeasureSpec && mode == 0) {
                return 0;
            }
            return makeMeasureSpec(size, mode);
        }

        public static int getMode(int measureSpec) {
            return MODE_MASK & measureSpec;
        }

        public static int getSize(int measureSpec) {
            return View.LAST_APP_AUTOFILL_ID & measureSpec;
        }

        static int adjust(int measureSpec, int delta) {
            int mode = getMode(measureSpec);
            int size = getSize(measureSpec);
            if (mode == 0) {
                return makeMeasureSpec(size, 0);
            }
            size += delta;
            if (size < 0) {
                Log.e(View.VIEW_LOG_TAG, "MeasureSpec.adjust: new size would be negative! (" + size + ") spec: " + toString(measureSpec) + " delta: " + delta);
                size = 0;
            }
            return makeMeasureSpec(size, mode);
        }

        public static String toString(int measureSpec) {
            int mode = getMode(measureSpec);
            int size = getSize(measureSpec);
            StringBuilder sb = new StringBuilder("MeasureSpec: ");
            if (mode == 0) {
                sb.append("UNSPECIFIED ");
            } else if (mode == 1073741824) {
                sb.append("EXACTLY ");
            } else if (mode == Integer.MIN_VALUE) {
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

    public interface OnCapturedPointerListener {
        boolean onCapturedPointer(View view, MotionEvent motionEvent);
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
        /* synthetic */ PerformClick(View this$0, PerformClick -this1) {
            this();
        }

        private PerformClick() {
        }

        public void run() {
            View.this.performClick();
        }
    }

    private static class ScrollabilityCache implements Runnable {
        public static final int DRAGGING_HORIZONTAL_SCROLL_BAR = 2;
        public static final int DRAGGING_VERTICAL_SCROLL_BAR = 1;
        public static final int FADING = 2;
        public static final int NOT_DRAGGING = 0;
        public static final int OFF = 0;
        public static final int ON = 1;
        private static final float[] OPAQUE = new float[]{255.0f};
        private static final float[] TRANSPARENT = new float[]{0.0f};
        public boolean fadeScrollBars;
        public long fadeStartTime;
        public int fadingEdgeLength;
        public View host;
        public float[] interpolatorValues;
        private int mLastColor;
        public final Rect mScrollBarBounds = new Rect();
        public float mScrollBarDraggingPos = 0.0f;
        public int mScrollBarDraggingState = 0;
        public final Rect mScrollBarTouchBounds = new Rect();
        public final Matrix matrix;
        public final Paint paint;
        public ScrollBarDrawable scrollBar;
        public int scrollBarDefaultDelayBeforeFade;
        public int scrollBarFadeDuration;
        public final Interpolator scrollBarInterpolator = new Interpolator(1, 2);
        public int scrollBarMinTouchTarget;
        public int scrollBarSize;
        public Shader shader;
        public int state = 0;

        public ScrollabilityCache(ViewConfiguration configuration, View host) {
            this.fadingEdgeLength = configuration.getScaledFadingEdgeLength();
            this.scrollBarSize = configuration.getScaledScrollBarSize();
            this.scrollBarMinTouchTarget = configuration.getScaledMinScrollbarTouchTarget();
            this.scrollBarDefaultDelayBeforeFade = ViewConfiguration.getScrollDefaultDelay();
            this.scrollBarFadeDuration = ViewConfiguration.getScrollBarFadeDuration();
            this.paint = new Paint();
            this.matrix = new Matrix();
            this.shader = new LinearGradient(0.0f, 0.0f, 0.0f, 1.0f, -16777216, 0, TileMode.CLAMP);
            this.paint.setShader(this.shader);
            this.paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
            this.host = host;
        }

        public void setFadeColor(int color) {
            if (color != this.mLastColor) {
                this.mLastColor = color;
                if (color != 0) {
                    this.shader = new LinearGradient(0.0f, 0.0f, 0.0f, 1.0f, -16777216 | color, color & 16777215, TileMode.CLAMP);
                    this.paint.setShader(this.shader);
                    this.paint.setXfermode(null);
                    return;
                }
                this.shader = new LinearGradient(0.0f, 0.0f, 0.0f, 1.0f, -16777216, 0, TileMode.CLAMP);
                this.paint.setShader(this.shader);
                this.paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
            }
        }

        public void run() {
            long now = AnimationUtils.currentAnimationTimeMillis();
            if (now >= this.fadeStartTime) {
                int nextFrame = (int) now;
                Interpolator interpolator = this.scrollBarInterpolator;
                interpolator.setKeyFrame(0, nextFrame, OPAQUE);
                interpolator.setKeyFrame(1, nextFrame + this.scrollBarFadeDuration, TRANSPARENT);
                this.state = 2;
                this.host.invalidate(true);
            }
        }
    }

    private class SendViewScrolledAccessibilityEvent implements Runnable {
        public volatile boolean mIsPending;

        /* synthetic */ SendViewScrolledAccessibilityEvent(View this$0, SendViewScrolledAccessibilityEvent -this1) {
            this();
        }

        private SendViewScrolledAccessibilityEvent() {
        }

        public void run() {
            View.this.sendAccessibilityEvent(4096);
            this.mIsPending = false;
        }
    }

    private class SendViewStateChangedAccessibilityEvent implements Runnable {
        private int mChangeTypes;
        private long mLastEventTimeMillis;
        private boolean mPosted;
        private boolean mPostedWithDelay;

        /* synthetic */ SendViewStateChangedAccessibilityEvent(View this$0, SendViewStateChangedAccessibilityEvent -this1) {
            this();
        }

        private SendViewStateChangedAccessibilityEvent() {
            this.mChangeTypes = 0;
        }

        public void run() {
            this.mPosted = false;
            this.mPostedWithDelay = false;
            this.mLastEventTimeMillis = SystemClock.uptimeMillis();
            if (AccessibilityManager.getInstance(View.this.mContext).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain();
                event.setEventType(2048);
                event.setContentChangeTypes(this.mChangeTypes);
                View.this.sendAccessibilityEventUnchecked(event);
            }
            this.mChangeTypes = 0;
        }

        public void runOrPost(int changeType) {
            this.mChangeTypes |= changeType;
            if (View.this.inLiveRegion()) {
                if (this.mPostedWithDelay) {
                    View.this.removeCallbacks(this);
                    this.mPostedWithDelay = false;
                }
                if (!this.mPosted) {
                    View.this.post(this);
                    this.mPosted = true;
                }
            } else if (!this.mPosted) {
                long timeSinceLastMillis = SystemClock.uptimeMillis() - this.mLastEventTimeMillis;
                long minEventIntevalMillis = ViewConfiguration.getSendRecurringAccessibilityEventsInterval();
                if (timeSinceLastMillis >= minEventIntevalMillis) {
                    View.this.removeCallbacks(this);
                    run();
                } else {
                    View.this.postDelayed(this, minEventIntevalMillis - timeSinceLastMillis);
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

    private static class TooltipInfo {
        int mAnchorX;
        int mAnchorY;
        Runnable mHideTooltipRunnable;
        Runnable mShowTooltipRunnable;
        boolean mTooltipFromLongClick;
        TooltipPopup mTooltipPopup;
        CharSequence mTooltipText;

        /* synthetic */ TooltipInfo(TooltipInfo -this0) {
            this();
        }

        private TooltipInfo() {
        }
    }

    static class TransformationInfo {
        @ExportedProperty
        float mAlpha = 1.0f;
        private Matrix mInverseMatrix;
        private final Matrix mMatrix = new Matrix();
        float mTransitionAlpha = 1.0f;

        TransformationInfo() {
        }
    }

    private final class UnsetPressedState implements Runnable {
        /* synthetic */ UnsetPressedState(View this$0, UnsetPressedState -this1) {
            this();
        }

        private UnsetPressedState() {
        }

        public void run() {
            View.this.setPressed(false);
        }
    }

    private static class VisibilityChangeForAutofillHandler extends Handler {
        private final AutofillManager mAfm;
        private final View mView;

        /* synthetic */ VisibilityChangeForAutofillHandler(AutofillManager afm, View view, VisibilityChangeForAutofillHandler -this2) {
            this(afm, view);
        }

        private VisibilityChangeForAutofillHandler(AutofillManager afm, View view) {
            this.mAfm = afm;
            this.mView = view;
        }

        public void handleMessage(Message msg) {
            this.mAfm.notifyViewVisibilityChange(this.mView, this.mView.isShown());
        }
    }

    public void forceRTL(boolean flag) {
        this.mForceRTL = flag;
    }

    public View(Context context) {
        Resources resources = null;
        boolean z = false;
        this.mCurrentAnimation = null;
        this.mRecreateDisplayList = false;
        this.mID = -1;
        this.mAutofillViewId = -1;
        this.mAccessibilityViewId = -1;
        this.mAccessibilityCursorPosition = -1;
        this.mTag = null;
        this.mForceRTL = false;
        this.mTransientStateCount = 0;
        this.mClipBounds = null;
        this.mPaddingLeft = 0;
        this.mPaddingRight = 0;
        this.mLabelForId = -1;
        this.mAccessibilityTraversalBeforeId = -1;
        this.mAccessibilityTraversalAfterId = -1;
        this.mLeftPaddingDefined = false;
        this.mRightPaddingDefined = false;
        this.mOldWidthMeasureSpec = Integer.MIN_VALUE;
        this.mOldHeightMeasureSpec = Integer.MIN_VALUE;
        this.mLongClickX = Float.NaN;
        this.mLongClickY = Float.NaN;
        this.mDrawableState = null;
        this.mOutlineProvider = ViewOutlineProvider.BACKGROUND;
        this.mNextFocusLeftId = -1;
        this.mNextFocusRightId = -1;
        this.mNextFocusUpId = -1;
        this.mNextFocusDownId = -1;
        this.mNextFocusForwardId = -1;
        this.mNextClusterForwardId = -1;
        this.mDefaultFocusHighlightEnabled = true;
        this.mPendingCheckForTap = null;
        this.mTouchDelegate = null;
        this.mDrawingCacheBackgroundColor = 0;
        this.mAnimator = null;
        this.mLayerType = 0;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 0) : null;
        this.mViewCheckedResult = -1;
        this.mContext = context;
        if (context != null) {
            resources = context.getResources();
        }
        this.mResources = resources;
        this.mViewFlags = 402653200;
        this.mPrivateFlags2 = 140296;
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setOverScrollMode(1);
        this.mUserPaddingStart = Integer.MIN_VALUE;
        this.mUserPaddingEnd = Integer.MIN_VALUE;
        this.mRenderNode = RenderNode.create(getClass().getName(), this);
        if (!sCompatibilityDone && context != null) {
            boolean z2;
            int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
            if (targetSdkVersion <= 17) {
                z2 = true;
            } else {
                z2 = false;
            }
            sUseBrokenMakeMeasureSpec = z2;
            if (targetSdkVersion < 19) {
                z2 = true;
            } else {
                z2 = false;
            }
            sIgnoreMeasureCache = z2;
            if (targetSdkVersion < 23) {
                z2 = true;
            } else {
                z2 = false;
            }
            Canvas.sCompatibilityRestore = z2;
            if (targetSdkVersion < 26) {
                z2 = true;
            } else {
                z2 = false;
            }
            Canvas.sCompatibilitySetBitmap = z2;
            if (targetSdkVersion < 23) {
                z2 = true;
            } else {
                z2 = false;
            }
            sUseZeroUnspecifiedMeasureSpec = z2;
            if (targetSdkVersion <= 23) {
                z2 = true;
            } else {
                z2 = false;
            }
            sAlwaysRemeasureExactly = z2;
            if (targetSdkVersion <= 23) {
                z2 = true;
            } else {
                z2 = false;
            }
            sLayoutParamsAlwaysChanged = z2;
            if (targetSdkVersion <= 23) {
                z2 = true;
            } else {
                z2 = false;
            }
            sTextureViewIgnoresDrawableSetters = z2;
            if (targetSdkVersion >= 24) {
                z2 = true;
            } else {
                z2 = false;
            }
            sPreserveMarginParamsInLayoutParamConversion = z2;
            if (targetSdkVersion < 24) {
                z2 = true;
            } else {
                z2 = false;
            }
            sCascadedDragDrop = z2;
            if (targetSdkVersion < 26) {
                z2 = true;
            } else {
                z2 = false;
            }
            sHasFocusableExcludeAutoFocusable = z2;
            if (targetSdkVersion < 26) {
                z = true;
            }
            sAutoFocusableOffUIThreadWontNotifyParents = z;
            sUseDefaultFocusHighlight = context.getResources().getBoolean(R.bool.config_useDefaultFocusHighlight);
            sCompatibilityDone = true;
        }
    }

    public View(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public View(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public View(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        int i;
        int attr;
        int i2;
        this(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.View, defStyleAttr, defStyleRes);
        if (mDebugViewAttributes) {
            saveAttributeData(attrs, a);
        }
        Drawable background = null;
        int leftPadding = -1;
        int topPadding = -1;
        int rightPadding = -1;
        int bottomPadding = -1;
        int startPadding = Integer.MIN_VALUE;
        int endPadding = Integer.MIN_VALUE;
        int padding = -1;
        int paddingHorizontal = -1;
        int paddingVertical = -1;
        boolean setScrollContainer = false;
        int x = 0;
        int y = 0;
        float tx = 0.0f;
        float ty = 0.0f;
        float tz = 0.0f;
        float elevation = 0.0f;
        float rotation = 0.0f;
        float rotationX = 0.0f;
        float rotationY = 0.0f;
        float sx = 1.0f;
        float sy = 1.0f;
        boolean transformSet = false;
        int scrollbarStyle = 0;
        int overScrollMode = this.mOverScrollMode;
        boolean initializeScrollbars = false;
        boolean initializeScrollIndicators = false;
        boolean startPaddingDefined = false;
        boolean endPaddingDefined = false;
        boolean leftPaddingDefined = false;
        boolean rightPaddingDefined = false;
        int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        int viewFlagValues = 16;
        int viewFlagMasks = 16;
        int N = a.getIndexCount();
        for (i = 0; i < N; i++) {
            attr = a.getIndex(i);
            switch (attr) {
                case 8:
                    scrollbarStyle = a.getInt(attr, 0);
                    if (scrollbarStyle == 0) {
                        break;
                    }
                    viewFlagValues |= 50331648 & scrollbarStyle;
                    viewFlagMasks |= 50331648;
                    break;
                case 9:
                    this.mID = a.getResourceId(attr, -1);
                    break;
                case 10:
                    this.mTag = a.getText(attr);
                    break;
                case 11:
                    x = a.getDimensionPixelOffset(attr, 0);
                    break;
                case 12:
                    y = a.getDimensionPixelOffset(attr, 0);
                    break;
                case 13:
                    background = a.getDrawable(attr);
                    break;
                case 14:
                    padding = a.getDimensionPixelSize(attr, -1);
                    this.mUserPaddingLeftInitial = padding;
                    this.mUserPaddingRightInitial = padding;
                    leftPaddingDefined = true;
                    rightPaddingDefined = true;
                    break;
                case 15:
                    leftPadding = a.getDimensionPixelSize(attr, -1);
                    this.mUserPaddingLeftInitial = leftPadding;
                    leftPaddingDefined = true;
                    break;
                case 16:
                    topPadding = a.getDimensionPixelSize(attr, -1);
                    break;
                case 17:
                    rightPadding = a.getDimensionPixelSize(attr, -1);
                    this.mUserPaddingRightInitial = rightPadding;
                    rightPaddingDefined = true;
                    break;
                case 18:
                    bottomPadding = a.getDimensionPixelSize(attr, -1);
                    break;
                case 19:
                    viewFlagValues = (viewFlagValues & -18) | getFocusableAttribute(a);
                    if ((viewFlagValues & 16) != 0) {
                        break;
                    }
                    viewFlagMasks |= 17;
                    break;
                case 20:
                    if (!a.getBoolean(attr, false)) {
                        break;
                    }
                    viewFlagValues = (viewFlagValues & -17) | 262145;
                    viewFlagMasks |= 262161;
                    break;
                case 21:
                    int visibility = a.getInt(attr, 0);
                    if (visibility == 0) {
                        break;
                    }
                    viewFlagValues |= VISIBILITY_FLAGS[visibility];
                    viewFlagMasks |= 12;
                    break;
                case 22:
                    if (!a.getBoolean(attr, false)) {
                        break;
                    }
                    viewFlagValues |= 2;
                    viewFlagMasks |= 2;
                    break;
                case 23:
                    int scrollbars = a.getInt(attr, 0);
                    if (scrollbars == 0) {
                        break;
                    }
                    viewFlagValues |= scrollbars;
                    viewFlagMasks |= 768;
                    initializeScrollbars = true;
                    break;
                case 24:
                    if (targetSdkVersion >= 14) {
                        break;
                    }
                case 26:
                    this.mNextFocusLeftId = a.getResourceId(attr, -1);
                    break;
                case 27:
                    this.mNextFocusRightId = a.getResourceId(attr, -1);
                    break;
                case 28:
                    this.mNextFocusUpId = a.getResourceId(attr, -1);
                    break;
                case 29:
                    this.mNextFocusDownId = a.getResourceId(attr, -1);
                    break;
                case 30:
                    if (!a.getBoolean(attr, false)) {
                        break;
                    }
                    viewFlagValues |= 16384;
                    viewFlagMasks |= 16384;
                    break;
                case 31:
                    if (!a.getBoolean(attr, false)) {
                        break;
                    }
                    viewFlagValues |= 2097152;
                    viewFlagMasks |= 2097152;
                    break;
                case 32:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues |= 65536;
                        viewFlagMasks |= 65536;
                        break;
                    }
                    break;
                case 33:
                    int cacheQuality = a.getInt(attr, 0);
                    if (cacheQuality == 0) {
                        break;
                    }
                    viewFlagValues |= DRAWING_CACHE_QUALITY_FLAGS[cacheQuality];
                    viewFlagMasks |= DRAWING_CACHE_QUALITY_MASK;
                    break;
                case 34:
                    if (!a.getBoolean(attr, false)) {
                        break;
                    }
                    viewFlagValues |= 4194304;
                    viewFlagMasks |= 4194304;
                    break;
                case 35:
                    if (targetSdkVersion < 23 && !(this instanceof FrameLayout)) {
                        break;
                    }
                    setForeground(a.getDrawable(attr));
                    break;
                case 36:
                    this.mMinWidth = a.getDimensionPixelSize(attr, 0);
                    break;
                case 37:
                    this.mMinHeight = a.getDimensionPixelSize(attr, 0);
                    break;
                case 38:
                    if (targetSdkVersion < 23 && !(this instanceof FrameLayout)) {
                        break;
                    }
                    setForegroundGravity(a.getInt(attr, 0));
                    break;
                case 39:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues &= -134217729;
                        viewFlagMasks |= 134217728;
                        break;
                    }
                    break;
                case 40:
                    if (!a.getBoolean(attr, false)) {
                        break;
                    }
                    viewFlagValues |= 67108864;
                    viewFlagMasks |= 67108864;
                    break;
                case 41:
                    setScrollContainer = true;
                    if (!a.getBoolean(attr, false)) {
                        break;
                    }
                    setScrollContainer(true);
                    break;
                case 42:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues &= -268435457;
                        viewFlagMasks |= 268435456;
                        break;
                    }
                    break;
                case 43:
                    if (!context.isRestricted()) {
                        String handlerName = a.getString(attr);
                        if (handlerName == null) {
                            break;
                        }
                        setOnClickListener(new DeclaredOnClickListener(this, handlerName));
                        break;
                    }
                    throw new IllegalStateException("The android:onClick attribute cannot be used within a restricted context");
                case 44:
                    setContentDescription(a.getString(attr));
                    break;
                case 48:
                    overScrollMode = a.getInt(attr, 1);
                    break;
                case 49:
                    if (!a.getBoolean(attr, false)) {
                        break;
                    }
                    viewFlagValues |= 1024;
                    viewFlagMasks |= 1024;
                    break;
                case 50:
                    setAlpha(a.getFloat(attr, 1.0f));
                    break;
                case 51:
                    setPivotX(a.getDimension(attr, 0.0f));
                    break;
                case 52:
                    setPivotY(a.getDimension(attr, 0.0f));
                    break;
                case 53:
                    tx = a.getDimension(attr, 0.0f);
                    transformSet = true;
                    break;
                case 54:
                    ty = a.getDimension(attr, 0.0f);
                    transformSet = true;
                    break;
                case 55:
                    sx = a.getFloat(attr, 1.0f);
                    transformSet = true;
                    break;
                case 56:
                    sy = a.getFloat(attr, 1.0f);
                    transformSet = true;
                    break;
                case 57:
                    rotation = a.getFloat(attr, 0.0f);
                    transformSet = true;
                    break;
                case 58:
                    rotationX = a.getFloat(attr, 0.0f);
                    transformSet = true;
                    break;
                case 59:
                    rotationY = a.getFloat(attr, 0.0f);
                    transformSet = true;
                    break;
                case 60:
                    this.mVerticalScrollbarPosition = a.getInt(attr, 0);
                    break;
                case 61:
                    this.mNextFocusForwardId = a.getResourceId(attr, -1);
                    break;
                case 62:
                    setLayerType(a.getInt(attr, 0), null);
                    break;
                case 63:
                    int fadingEdge = a.getInt(attr, 0);
                    if (fadingEdge == 0) {
                        break;
                    }
                    viewFlagValues |= fadingEdge;
                    viewFlagMasks |= 12288;
                    initializeFadingEdgeInternal(a);
                    break;
                case 64:
                    setImportantForAccessibility(a.getInt(attr, 0));
                    break;
                case 65:
                    this.mPrivateFlags2 &= -449;
                    int textDirection = a.getInt(attr, -1);
                    if (textDirection == -1) {
                        break;
                    }
                    this.mPrivateFlags2 |= PFLAG2_TEXT_DIRECTION_FLAGS[textDirection];
                    break;
                case 66:
                    this.mPrivateFlags2 &= -57345;
                    this.mPrivateFlags2 |= PFLAG2_TEXT_ALIGNMENT_FLAGS[a.getInt(attr, 1)];
                    break;
                case 67:
                    this.mPrivateFlags2 &= -61;
                    int layoutDirection = a.getInt(attr, -1);
                    this.mPrivateFlags2 |= (layoutDirection != -1 ? LAYOUT_DIRECTION_FLAGS[layoutDirection] : 2) << 2;
                    break;
                case 68:
                    startPadding = a.getDimensionPixelSize(attr, Integer.MIN_VALUE);
                    if (startPadding == Integer.MIN_VALUE) {
                        startPaddingDefined = false;
                        break;
                    } else {
                        startPaddingDefined = true;
                        break;
                    }
                case 69:
                    endPadding = a.getDimensionPixelSize(attr, Integer.MIN_VALUE);
                    if (endPadding == Integer.MIN_VALUE) {
                        endPaddingDefined = false;
                        break;
                    } else {
                        endPaddingDefined = true;
                        break;
                    }
                case 70:
                    setLabelFor(a.getResourceId(attr, -1));
                    break;
                case 71:
                    setAccessibilityLiveRegion(a.getInt(attr, 0));
                    break;
                case 72:
                    tz = a.getDimension(attr, 0.0f);
                    transformSet = true;
                    break;
                case 73:
                    setTransitionName(a.getString(attr));
                    break;
                case 74:
                    setNestedScrollingEnabled(a.getBoolean(attr, false));
                    break;
                case 75:
                    elevation = a.getDimension(attr, 0.0f);
                    transformSet = true;
                    break;
                case 76:
                    setStateListAnimator(AnimatorInflater.loadStateListAnimator(context, a.getResourceId(attr, 0)));
                    break;
                case 77:
                    if (this.mBackgroundTint == null) {
                        this.mBackgroundTint = new TintInfo();
                    }
                    this.mBackgroundTint.mTintList = a.getColorStateList(77);
                    this.mBackgroundTint.mHasTintList = true;
                    break;
                case 78:
                    if (this.mBackgroundTint == null) {
                        this.mBackgroundTint = new TintInfo();
                    }
                    this.mBackgroundTint.mTintMode = Drawable.parseTintMode(a.getInt(78, -1), null);
                    this.mBackgroundTint.mHasTintMode = true;
                    break;
                case 79:
                    if (targetSdkVersion < 23 && !(this instanceof FrameLayout)) {
                        break;
                    }
                    setForegroundTintList(a.getColorStateList(attr));
                    break;
                case 80:
                    if (targetSdkVersion < 23 && !(this instanceof FrameLayout)) {
                        break;
                    }
                    setForegroundTintMode(Drawable.parseTintMode(a.getInt(attr, -1), null));
                    break;
                case 81:
                    setOutlineProviderFromAttribute(a.getInt(81, 0));
                    break;
                case 82:
                    setAccessibilityTraversalBefore(a.getResourceId(attr, -1));
                    break;
                case 83:
                    setAccessibilityTraversalAfter(a.getResourceId(attr, -1));
                    break;
                case 84:
                    int scrollIndicators = (a.getInt(attr, 0) << 8) & SCROLL_INDICATORS_PFLAG3_MASK;
                    if (scrollIndicators == 0) {
                        break;
                    }
                    this.mPrivateFlags3 |= scrollIndicators;
                    initializeScrollIndicators = true;
                    break;
                case 85:
                    if (!a.getBoolean(attr, false)) {
                        break;
                    }
                    viewFlagValues |= 8388608;
                    viewFlagMasks |= 8388608;
                    break;
                case 86:
                    int resourceId = a.getResourceId(attr, 0);
                    if (resourceId == 0) {
                        int pointerType = a.getInt(attr, 1);
                        if (pointerType == 1) {
                            break;
                        }
                        setPointerIcon(PointerIcon.getSystemIcon(context, pointerType));
                        break;
                    }
                    setPointerIcon(PointerIcon.load(context.getResources(), resourceId));
                    break;
                case 87:
                    if (a.peekValue(attr) == null) {
                        break;
                    }
                    forceHasOverlappingRendering(a.getBoolean(attr, true));
                    break;
                case 88:
                    setTooltipText(a.getText(attr));
                    break;
                case 89:
                    paddingHorizontal = a.getDimensionPixelSize(attr, -1);
                    this.mUserPaddingLeftInitial = paddingHorizontal;
                    this.mUserPaddingRightInitial = paddingHorizontal;
                    leftPaddingDefined = true;
                    rightPaddingDefined = true;
                    break;
                case 90:
                    paddingVertical = a.getDimensionPixelSize(attr, -1);
                    break;
                case 91:
                    if (a.peekValue(attr) == null) {
                        break;
                    }
                    setKeyboardNavigationCluster(a.getBoolean(attr, true));
                    break;
                case 92:
                    this.mNextClusterForwardId = a.getResourceId(attr, -1);
                    break;
                case 93:
                    if (a.peekValue(attr) == null) {
                        break;
                    }
                    setFocusedByDefault(a.getBoolean(attr, true));
                    break;
                case 94:
                    if (a.peekValue(attr) != null) {
                        CharSequence[] rawHints = null;
                        String rawString = null;
                        if (a.getType(attr) == 1) {
                            int resId = a.getResourceId(attr, 0);
                            try {
                                rawHints = a.getTextArray(attr);
                            } catch (NotFoundException e) {
                                rawString = getResources().getString(resId);
                            }
                        } else {
                            rawString = a.getString(attr);
                        }
                        if (rawHints == null) {
                            if (rawString == null) {
                                throw new IllegalArgumentException("Could not resolve autofillHints");
                            }
                            rawHints = rawString.split(",");
                        }
                        String[] hints = new String[rawHints.length];
                        int numHints = rawHints.length;
                        for (int rawHintNum = 0; rawHintNum < numHints; rawHintNum++) {
                            hints[rawHintNum] = rawHints[rawHintNum].toString().trim();
                        }
                        setAutofillHints(hints);
                        break;
                    }
                    continue;
                case 95:
                    if (a.peekValue(attr) == null) {
                        break;
                    }
                    setImportantForAutofill(a.getInt(attr, 0));
                    break;
                case 96:
                    if (a.peekValue(attr) == null) {
                        break;
                    }
                    setDefaultFocusHighlightEnabled(a.getBoolean(attr, true));
                    break;
                case 100:
                    if (targetSdkVersion < 23 && !(this instanceof FrameLayout)) {
                        break;
                    }
                    if (this.mForegroundInfo == null) {
                        this.mForegroundInfo = new ForegroundInfo();
                    }
                    this.mForegroundInfo.mInsidePadding = a.getBoolean(attr, this.mForegroundInfo.mInsidePadding);
                    break;
                default:
                    break;
            }
        }
        TypedArray ahwext = context.obtainStyledAttributes(attrs, androidhwext.R.styleable.View, defStyleAttr, defStyleRes);
        int Nhwext = ahwext.getIndexCount();
        for (i = 0; i < Nhwext; i++) {
            attr = ahwext.getIndex(i);
            switch (attr) {
                case 0:
                    setHwStateListAnimator(AnimatorInflater.loadHwStateListAnimator(context, ahwext.getResourceId(attr, 0)));
                    break;
                default:
                    break;
            }
        }
        ahwext.recycle();
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
        } else {
            if (paddingHorizontal >= 0) {
                leftPadding = paddingHorizontal;
                rightPadding = paddingHorizontal;
                this.mUserPaddingLeftInitial = paddingHorizontal;
                this.mUserPaddingRightInitial = paddingHorizontal;
            }
            if (paddingVertical >= 0) {
                topPadding = paddingVertical;
                bottomPadding = paddingVertical;
            }
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
            int hasRelativePadding = !startPaddingDefined ? endPaddingDefined : 1;
            if (this.mLeftPaddingDefined && (hasRelativePadding ^ 1) != 0) {
                this.mUserPaddingLeftInitial = leftPadding;
            }
            if (this.mRightPaddingDefined && (hasRelativePadding ^ 1) != 0) {
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
        if (!(setScrollContainer || (viewFlagValues & 512) == 0)) {
            setScrollContainer(true);
        }
        computeOpaqueFlags();
    }

    View() {
        this.mCurrentAnimation = null;
        this.mRecreateDisplayList = false;
        this.mID = -1;
        this.mAutofillViewId = -1;
        this.mAccessibilityViewId = -1;
        this.mAccessibilityCursorPosition = -1;
        this.mTag = null;
        this.mForceRTL = false;
        this.mTransientStateCount = 0;
        this.mClipBounds = null;
        this.mPaddingLeft = 0;
        this.mPaddingRight = 0;
        this.mLabelForId = -1;
        this.mAccessibilityTraversalBeforeId = -1;
        this.mAccessibilityTraversalAfterId = -1;
        this.mLeftPaddingDefined = false;
        this.mRightPaddingDefined = false;
        this.mOldWidthMeasureSpec = Integer.MIN_VALUE;
        this.mOldHeightMeasureSpec = Integer.MIN_VALUE;
        this.mLongClickX = Float.NaN;
        this.mLongClickY = Float.NaN;
        this.mDrawableState = null;
        this.mOutlineProvider = ViewOutlineProvider.BACKGROUND;
        this.mNextFocusLeftId = -1;
        this.mNextFocusRightId = -1;
        this.mNextFocusUpId = -1;
        this.mNextFocusDownId = -1;
        this.mNextFocusForwardId = -1;
        this.mNextClusterForwardId = -1;
        this.mDefaultFocusHighlightEnabled = true;
        this.mPendingCheckForTap = null;
        this.mTouchDelegate = null;
        this.mDrawingCacheBackgroundColor = 0;
        this.mAnimator = null;
        this.mLayerType = 0;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 0) : null;
        this.mViewCheckedResult = -1;
        this.mResources = null;
        this.mRenderNode = RenderNode.create(getClass().getName(), this);
    }

    final boolean debugDraw() {
        if (DEBUG_DRAW) {
            return true;
        }
        return this.mAttachInfo != null ? this.mAttachInfo.mDebugLayout : false;
    }

    private static SparseArray<String> getAttributeMap() {
        if (mAttributeMap == null) {
            mAttributeMap = new SparseArray();
        }
        return mAttributeMap;
    }

    private void saveAttributeData(AttributeSet attrs, TypedArray t) {
        int j;
        int attrsCount = attrs == null ? 0 : attrs.getAttributeCount();
        int indexCount = t.getIndexCount();
        String[] attributes = new String[((attrsCount + indexCount) * 2)];
        int i = 0;
        for (j = 0; j < attrsCount; j++) {
            attributes[i] = attrs.getAttributeName(j);
            attributes[i + 1] = attrs.getAttributeValue(j);
            i += 2;
        }
        Resources res = t.getResources();
        SparseArray<String> attributeMap = getAttributeMap();
        for (j = 0; j < indexCount; j++) {
            int index = t.getIndex(j);
            if (t.hasValueOrEmpty(index)) {
                int resourceId = t.getResourceId(index, 0);
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
                    attributes[i + 1] = t.getString(index);
                    i += 2;
                }
            }
        }
        String[] trimmed = new String[i];
        System.arraycopy(attributes, 0, trimmed, 0, i);
        this.mAttributes = trimmed;
    }

    public String toString() {
        char c;
        char c2 = 'D';
        StringBuilder out = new StringBuilder(128);
        out.append(getClass().getName());
        out.append('{');
        out.append(Integer.toHexString(System.identityHashCode(this)));
        out.append(' ');
        switch (this.mViewFlags & 12) {
            case 0:
                out.append('V');
                break;
            case 4:
                out.append('I');
                break;
            case 8:
                out.append('G');
                break;
            default:
                out.append('.');
                break;
        }
        if ((this.mViewFlags & 1) == 1) {
            c = 'F';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & 32) == 0) {
            c = DateFormat.DAY;
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & 128) == 128) {
            c = '.';
        } else {
            c = 'D';
        }
        out.append(c);
        if ((this.mViewFlags & 256) != 0) {
            c = 'H';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & 512) != 0) {
            c = 'V';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & 16384) != 0) {
            c = 'C';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & 2097152) != 0) {
            c = DateFormat.STANDALONE_MONTH;
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mViewFlags & 8388608) != 0) {
            c = 'X';
        } else {
            c = '.';
        }
        out.append(c);
        out.append(' ');
        if ((this.mPrivateFlags & 8) != 0) {
            c = 'R';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & 2) != 0) {
            c = 'F';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & 4) != 0) {
            c = 'S';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & 33554432) != 0) {
            out.append('p');
        } else {
            out.append((this.mPrivateFlags & 16384) != 0 ? 'P' : '.');
        }
        if ((this.mPrivateFlags & 268435456) != 0) {
            c = 'H';
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & 1073741824) != 0) {
            c = DateFormat.CAPITAL_AM_PM;
        } else {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & Integer.MIN_VALUE) != 0) {
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
        out.append(',');
        out.append(this.mTop);
        out.append('-');
        out.append(this.mRight);
        out.append(',');
        out.append(this.mBottom);
        int id = getId();
        if (id != -1) {
            out.append(" #");
            out.append(Integer.toHexString(id));
            Resources r = this.mResources;
            if (id > 0 && Resources.resourceHasPackage(id) && r != null) {
                String pkgname;
                switch (-16777216 & id) {
                    case 16777216:
                        pkgname = ZenModeConfig.SYSTEM_AUTHORITY;
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
                out.append(SettingsStringUtil.DELIMITER);
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
        return 0;
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
        return 0;
    }

    public int getVerticalScrollbarWidth() {
        ScrollabilityCache cache = this.mScrollCache;
        if (cache == null) {
            return 0;
        }
        ScrollBarDrawable scrollBar = cache.scrollBar;
        if (scrollBar == null) {
            return 0;
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
            return 0;
        }
        ScrollBarDrawable scrollBar = cache.scrollBar;
        if (scrollBar == null) {
            return 0;
        }
        int size = scrollBar.getSize(false);
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
            scrollabilityCache.state = 1;
        }
        scrollabilityCache.fadeScrollBars = fadeScrollbars;
        scrollabilityCache.scrollBarFadeDuration = a.getInt(45, ViewConfiguration.getScrollBarFadeDuration());
        scrollabilityCache.scrollBarDefaultDelayBeforeFade = a.getInt(46, ViewConfiguration.getScrollDefaultDelay());
        scrollabilityCache.scrollBarSize = a.getDimensionPixelSize(1, ViewConfiguration.get(this.mContext).getScaledScrollBarSize());
        scrollabilityCache.scrollBar.setHorizontalTrackDrawable(a.getDrawable(4));
        Drawable thumb = a.getDrawable(2);
        if (thumb != null) {
            scrollabilityCache.scrollBar.setHorizontalThumbDrawable(thumb);
        }
        if (a.getBoolean(6, false)) {
            scrollabilityCache.scrollBar.setAlwaysDrawHorizontalTrack(true);
        }
        Drawable track = a.getDrawable(5);
        scrollabilityCache.scrollBar.setVerticalTrackDrawable(track);
        thumb = a.getDrawable(3);
        if (thumb != null) {
            scrollabilityCache.scrollBar.setVerticalThumbDrawable(thumb);
        }
        if (a.getBoolean(7, false)) {
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
            return false;
        }
        Rect touchBounds;
        x += (float) getScrollX();
        y += (float) getScrollY();
        if (isVerticalScrollBarEnabled() && (isVerticalScrollBarHidden() ^ 1) != 0) {
            touchBounds = this.mScrollCache.mScrollBarTouchBounds;
            getVerticalScrollBarBounds(null, touchBounds);
            if (touchBounds.contains((int) x, (int) y)) {
                return true;
            }
        }
        if (isHorizontalScrollBarEnabled()) {
            touchBounds = this.mScrollCache.mScrollBarTouchBounds;
            getHorizontalScrollBarBounds(null, touchBounds);
            if (touchBounds.contains((int) x, (int) y)) {
                return true;
            }
        }
        return false;
    }

    boolean isOnScrollbarThumb(float x, float y) {
        return !isOnVerticalScrollbarThumb(x, y) ? isOnHorizontalScrollbarThumb(x, y) : true;
    }

    private boolean isOnVerticalScrollbarThumb(float x, float y) {
        if (!(this.mScrollCache == null || !isVerticalScrollBarEnabled() || (isVerticalScrollBarHidden() ^ 1) == 0)) {
            x += (float) getScrollX();
            y += (float) getScrollY();
            Rect bounds = this.mScrollCache.mScrollBarBounds;
            Rect touchBounds = this.mScrollCache.mScrollBarTouchBounds;
            getVerticalScrollBarBounds(bounds, touchBounds);
            int range = computeVerticalScrollRange();
            int offset = computeVerticalScrollOffset();
            int extent = computeVerticalScrollExtent();
            int thumbLength = ScrollBarUtils.getThumbLength(bounds.height(), bounds.width(), extent, range);
            int thumbTop = bounds.top + ScrollBarUtils.getThumbOffset(bounds.height(), thumbLength, extent, range, offset);
            int adjust = Math.max(this.mScrollCache.scrollBarMinTouchTarget - thumbLength, 0) / 2;
            if (x >= ((float) touchBounds.left) && x <= ((float) touchBounds.right) && y >= ((float) (thumbTop - adjust)) && y <= ((float) ((thumbTop + thumbLength) + adjust))) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnHorizontalScrollbarThumb(float x, float y) {
        if (this.mScrollCache != null && isHorizontalScrollBarEnabled()) {
            x += (float) getScrollX();
            y += (float) getScrollY();
            Rect bounds = this.mScrollCache.mScrollBarBounds;
            Rect touchBounds = this.mScrollCache.mScrollBarTouchBounds;
            getHorizontalScrollBarBounds(bounds, touchBounds);
            int range = computeHorizontalScrollRange();
            int offset = computeHorizontalScrollOffset();
            int extent = computeHorizontalScrollExtent();
            int thumbLength = ScrollBarUtils.getThumbLength(bounds.width(), bounds.height(), extent, range);
            int thumbLeft = bounds.left + ScrollBarUtils.getThumbOffset(bounds.width(), thumbLength, extent, range, offset);
            int adjust = Math.max(this.mScrollCache.scrollBarMinTouchTarget - thumbLength, 0) / 2;
            if (x >= ((float) (thumbLeft - adjust)) && x <= ((float) ((thumbLeft + thumbLength) + adjust)) && y >= ((float) touchBounds.top) && y <= ((float) touchBounds.bottom)) {
                return true;
            }
        }
        return false;
    }

    boolean isDraggingScrollBar() {
        if (this.mScrollCache == null || this.mScrollCache.mScrollBarDraggingState == 0) {
            return false;
        }
        return true;
    }

    public void setScrollIndicators(int indicators) {
        setScrollIndicators(indicators, 63);
    }

    public void setScrollIndicators(int indicators, int mask) {
        mask = (mask << 8) & SCROLL_INDICATORS_PFLAG3_MASK;
        indicators = (indicators << 8) & mask;
        int updatedFlags = indicators | (this.mPrivateFlags3 & (~mask));
        if (this.mPrivateFlags3 != updatedFlags) {
            this.mPrivateFlags3 = updatedFlags;
            if (indicators != 0) {
                initializeScrollIndicatorsInternal();
            }
            invalidate();
        }
    }

    public int getScrollIndicators() {
        return (this.mPrivateFlags3 & SCROLL_INDICATORS_PFLAG3_MASK) >>> 8;
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
            return false;
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
        } else if (this.mAttachInfo.mThreadedRenderer != null) {
            if (this.mFrameMetricsObservers == null) {
                this.mFrameMetricsObservers = new ArrayList();
            }
            FrameMetricsObserver fmo = new FrameMetricsObserver(window, handler.getLooper(), listener);
            this.mFrameMetricsObservers.add(fmo);
            this.mAttachInfo.mThreadedRenderer.addFrameMetricsObserver(fmo);
        } else {
            Log.w(VIEW_LOG_TAG, "View not hardware-accelerated. Unable to observe frame stats");
        }
    }

    public void removeFrameMetricsListener(OnFrameMetricsAvailableListener listener) {
        ThreadedRenderer renderer = getThreadedRenderer();
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
            ThreadedRenderer renderer = getThreadedRenderer();
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
        for (int i = 0; i < this.mFrameMetricsObservers.size(); i++) {
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
            result = false;
        } else {
            playSoundEffect(0);
            li.mOnClickListener.onClick(this);
            HwFrameworkFactory.getHwView().onClick(this, this.mContext);
            result = true;
        }
        sendAccessibilityEvent(1);
        notifyEnterOrExitForAutoFillIfNeeded(true);
        return result;
    }

    public boolean callOnClick() {
        ListenerInfo li = this.mListenerInfo;
        if (li == null || li.mOnClickListener == null) {
            return false;
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
        sendAccessibilityEvent(2);
        boolean handled = false;
        ListenerInfo li = this.mListenerInfo;
        if (!(li == null || li.mOnLongClickListener == null)) {
            handled = li.mOnLongClickListener.onLongClick(this);
        }
        if (!handled) {
            handled = (!Float.isNaN(x) ? Float.isNaN(y) ^ 1 : 0) != 0 ? showContextMenu(x, y) : showContextMenu();
        }
        if ((this.mViewFlags & 1073741824) == 1073741824 && !handled) {
            handled = showLongClickTooltip((int) x, (int) y);
        }
        if (handled) {
            performHapticFeedback(0);
        }
        return handled;
    }

    public boolean performContextClick(float x, float y) {
        return performContextClick();
    }

    public boolean performContextClick() {
        sendAccessibilityEvent(8388608);
        boolean handled = false;
        ListenerInfo li = this.mListenerInfo;
        if (!(li == null || li.mOnContextClickListener == null)) {
            handled = li.mOnContextClickListener.onContextClick(this);
        }
        if (handled) {
            performHapticFeedback(6);
        }
        return handled;
    }

    protected boolean performButtonActionOnTouchDown(MotionEvent event) {
        if (!event.isFromSource(InputDevice.SOURCE_MOUSE) || (event.getButtonState() & 2) == 0) {
            return false;
        }
        showContextMenu(event.getX(), event.getY());
        this.mPrivateFlags |= 67108864;
        return true;
    }

    public boolean showContextMenu() {
        return getParent().showContextMenuForChild(this);
    }

    public boolean showContextMenu(float x, float y) {
        return getParent().showContextMenuForChild(this, x, y);
    }

    public ActionMode startActionMode(ActionMode.Callback callback) {
        return startActionMode(callback, 0);
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
            return false;
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
        if ((this.mPrivateFlags & 2) == 0) {
            this.mPrivateFlags |= 2;
            View oldFocus = this.mAttachInfo != null ? getRootView().findFocus() : null;
            if (this.mParent != null) {
                this.mParent.requestChildFocus(this, this);
                updateFocusedInCluster(oldFocus, direction);
            }
            if (this.mAttachInfo != null) {
                this.mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(oldFocus, this);
            }
            onFocusChanged(true, direction, previouslyFocusedRect);
            refreshDrawableState();
        }
    }

    public final void setRevealOnFocusHint(boolean revealOnFocus) {
        if (revealOnFocus) {
            this.mPrivateFlags3 &= -67108865;
        } else {
            this.mPrivateFlags3 |= 67108864;
        }
    }

    public final boolean getRevealOnFocusHint() {
        return (this.mPrivateFlags3 & 67108864) == 0;
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
        return requestRectangleOnScreen(rectangle, false);
    }

    public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
        if (this.mParent == null) {
            return false;
        }
        View child = this;
        RectF position = this.mAttachInfo != null ? this.mAttachInfo.mTmpTransformRect : new RectF();
        position.set(rectangle);
        ViewParent parent = this.mParent;
        boolean scrolled = false;
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
        if ((this.mPrivateFlags & 2) != 0) {
            this.mPrivateFlags &= -3;
            if (propagate && this.mParent != null) {
                this.mParent.clearChildFocus(this);
            }
            onFocusChanged(false, 0, null);
            refreshDrawableState();
            if (!propagate) {
                return;
            }
            if (!refocus || (rootViewRequestFocus() ^ 1) != 0) {
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
        return root != null ? root.requestFocus() : false;
    }

    void unFocus(View focused) {
        clearFocusInternal(focused, false, false);
    }

    @ExportedProperty(category = "focus")
    public boolean hasFocus() {
        return (this.mPrivateFlags & 2) != 0;
    }

    public boolean hasFocusable() {
        return hasFocusable(sHasFocusableExcludeAutoFocusable ^ 1, false);
    }

    public boolean hasExplicitFocusable() {
        return hasFocusable(false, true);
    }

    boolean hasFocusable(boolean allowAutoFocus, boolean dispatchExplicit) {
        if (!isFocusableInTouchMode()) {
            for (ViewParent p = this.mParent; p instanceof ViewGroup; p = p.getParent()) {
                if (((ViewGroup) p).shouldBlockFocusForTouchscreen()) {
                    return false;
                }
            }
        }
        if ((this.mViewFlags & 12) != 0) {
            return false;
        }
        if ((allowAutoFocus || getFocusable() != 16) && isFocusable()) {
            return true;
        }
        return false;
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (gainFocus) {
            sendAccessibilityEvent(8);
        } else {
            notifyViewAccessibilityStateChangedIfNeeded(0);
        }
        switchDefaultFocusHighlight();
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (!gainFocus) {
            if (isPressed()) {
                setPressed(false);
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
        notifyEnterOrExitForAutoFillIfNeeded(gainFocus);
    }

    private void notifyEnterOrExitForAutoFillIfNeeded(boolean enter) {
        if (isAutofillable() && isAttachedToWindow()) {
            AutofillManager afm = getAutofillManager();
            if (afm == null) {
                return;
            }
            if (enter && hasWindowFocus() && isFocused()) {
                if (!isLaidOut()) {
                    this.mPrivateFlags3 |= 134217728;
                } else if (isVisibleToUser()) {
                    afm.notifyViewEntered(this);
                }
            } else if (!hasWindowFocus() || (isFocused() ^ 1) != 0) {
                afm.notifyViewExited(this);
            }
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
            AccessibilityEvent event = AccessibilityEvent.obtain(16384);
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
        return false;
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
            case 8:
                ArrayList<View> focusablesTempList = this.mAttachInfo != null ? this.mAttachInfo.mTempArrayList : new ArrayList();
                getRootView().addFocusables(focusablesTempList, 2, 0);
                event.setItemCount(focusablesTempList.size());
                event.setCurrentItemIndex(focusablesTempList.indexOf(this));
                if (this.mAttachInfo != null) {
                    focusablesTempList.clear();
                    return;
                }
                return;
            case 8192:
                CharSequence text = getIterableTextForAccessibility();
                if (text != null && text.length() > 0) {
                    event.setFromIndex(getAccessibilitySelectionStart());
                    event.setToIndex(getAccessibilitySelectionEnd());
                    event.setItemCount(text.length());
                    return;
                }
                return;
            default:
                return;
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
            return provider.createAccessibilityNodeInfo(-1);
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
        getBoundsOnScreen(outRect, false);
    }

    public void getBoundsOnScreen(Rect outRect, boolean clipToParent) {
        if (this.mAttachInfo != null) {
            RectF position = this.mAttachInfo.mTmpTransformRect;
            position.set(0.0f, 0.0f, (float) (this.mRight - this.mLeft), (float) (this.mBottom - this.mTop));
            mapRectFromViewToScreenCoords(position, clipToParent);
            outRect.set(Math.round(position.left), Math.round(position.top), Math.round(position.right), Math.round(position.bottom));
        }
    }

    public void mapRectFromViewToScreenCoords(RectF rect, boolean clipToParent) {
        if (!hasIdentityMatrix()) {
            getMatrix().mapRect(rect);
        }
        rect.offset((float) this.mLeft, (float) this.mTop);
        ViewParent parent = this.mParent;
        while (parent instanceof View) {
            View parentView = (View) parent;
            rect.offset((float) (-parentView.mScrollX), (float) (-parentView.mScrollY));
            if (clipToParent) {
                rect.left = Math.max(rect.left, 0.0f);
                rect.top = Math.max(rect.top, 0.0f);
                rect.right = Math.min(rect.right, (float) parentView.getWidth());
                rect.bottom = Math.min(rect.bottom, (float) parentView.getHeight());
            }
            if (!parentView.hasIdentityMatrix()) {
                parentView.getMatrix().mapRect(rect);
            }
            rect.offset((float) parentView.mLeft, (float) parentView.mTop);
            parent = parentView.mParent;
        }
        if (parent instanceof ViewRootImpl) {
            rect.offset(0.0f, (float) (-((ViewRootImpl) parent).mCurScrollY));
        }
        rect.offset((float) this.mAttachInfo.mWindowLeft, (float) this.mAttachInfo.mWindowTop);
    }

    public CharSequence getAccessibilityClassName() {
        return View.class.getName();
    }

    public void onProvideStructure(ViewStructure structure) {
        onProvideStructureForAssistOrAutofill(structure, false, 0);
    }

    public void onProvideAutofillStructure(ViewStructure structure, int flags) {
        onProvideStructureForAssistOrAutofill(structure, true, flags);
    }

    private void onProvideStructureForAssistOrAutofill(ViewStructure structure, boolean forAutofill, int flags) {
        int id = this.mID;
        if (id == -1 || (isViewIdGenerated(id) ^ 1) == 0) {
            structure.setId(id, null, null, null);
        } else {
            String entry;
            String type;
            String pkg;
            try {
                Resources res = getResources();
                entry = res.getResourceEntryName(id);
                type = res.getResourceTypeName(id);
                pkg = res.getResourcePackageName(id);
            } catch (NotFoundException e) {
                pkg = null;
                type = null;
                entry = null;
            }
            structure.setId(id, pkg, type, entry);
        }
        if (forAutofill) {
            int autofillType = getAutofillType();
            if (autofillType != 0) {
                structure.setAutofillType(autofillType);
                structure.setAutofillHints(getAutofillHints());
                structure.setAutofillValue(getAutofillValue());
            }
        }
        int ignoredParentLeft = 0;
        int ignoredParentTop = 0;
        if (forAutofill && (flags & 1) == 0) {
            View parentGroup = null;
            ViewParent viewParent = getParent();
            if (viewParent instanceof View) {
                parentGroup = (View) viewParent;
            }
            while (parentGroup != null && (parentGroup.isImportantForAutofill() ^ 1) != 0) {
                ignoredParentLeft += parentGroup.mLeft;
                ignoredParentTop += parentGroup.mTop;
                viewParent = parentGroup.getParent();
                if (!(viewParent instanceof View)) {
                    break;
                }
                parentGroup = (View) viewParent;
            }
        }
        structure.setDimens(ignoredParentLeft + this.mLeft, ignoredParentTop + this.mTop, this.mScrollX, this.mScrollY, this.mRight - this.mLeft, this.mBottom - this.mTop);
        if (!forAutofill) {
            if (!hasIdentityMatrix()) {
                structure.setTransformation(getMatrix());
            }
            structure.setElevation(getZ());
        }
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
        if (isOpaque()) {
            structure.setOpaque(true);
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
            structure.setChildCount(1);
            populateVirtualStructure(structure.newChild(0), provider, info);
            info.recycle();
        }
    }

    public void onProvideAutofillVirtualStructure(ViewStructure structure, int flags) {
    }

    public void autofill(AutofillValue value) {
    }

    public void autofill(SparseArray<AutofillValue> sparseArray) {
    }

    public final AutofillId getAutofillId() {
        if (this.mAutofillId == null) {
            this.mAutofillId = new AutofillId(getAutofillViewId());
        }
        return this.mAutofillId;
    }

    public int getAutofillType() {
        return 0;
    }

    @ExportedProperty
    public String[] getAutofillHints() {
        return this.mAutofillHints;
    }

    public boolean isAutofilled() {
        return (this.mPrivateFlags3 & 65536) != 0;
    }

    public AutofillValue getAutofillValue() {
        return null;
    }

    @ExportedProperty(mapping = {@IntToString(from = 0, to = "auto"), @IntToString(from = 1, to = "yes"), @IntToString(from = 2, to = "no"), @IntToString(from = 4, to = "yesExcludeDescendants"), @IntToString(from = 8, to = "noExcludeDescendants")})
    public int getImportantForAutofill() {
        return (this.mPrivateFlags3 & PFLAG3_IMPORTANT_FOR_AUTOFILL_MASK) >> 19;
    }

    public void setImportantForAutofill(int mode) {
        this.mPrivateFlags3 &= -7864321;
        this.mPrivateFlags3 |= (mode << 19) & PFLAG3_IMPORTANT_FOR_AUTOFILL_MASK;
    }

    public final boolean isImportantForAutofill() {
        for (ViewParent parent = this.mParent; parent instanceof View; parent = parent.getParent()) {
            int parentImportance = ((View) parent).getImportantForAutofill();
            if (parentImportance == 8 || parentImportance == 4) {
                return false;
            }
        }
        int importance = getImportantForAutofill();
        if (importance == 4 || importance == 1) {
            return true;
        }
        if (importance == 8 || importance == 2) {
            return false;
        }
        int id = this.mID;
        if (!(id == -1 || (isViewIdGenerated(id) ^ 1) == 0)) {
            Resources res = getResources();
            String entry = null;
            String pkg = null;
            try {
                entry = res.getResourceEntryName(id);
                pkg = res.getResourcePackageName(id);
            } catch (NotFoundException e) {
            }
            return (entry == null || pkg == null || !pkg.equals(this.mContext.getPackageName())) ? false : true;
        }
    }

    private AutofillManager getAutofillManager() {
        return (AutofillManager) this.mContext.getSystemService(AutofillManager.class);
    }

    private boolean isAutofillable() {
        if (getAutofillType() == 0 || !isImportantForAutofill() || getAutofillViewId() <= LAST_APP_AUTOFILL_ID) {
            return false;
        }
        return true;
    }

    private void populateVirtualStructure(ViewStructure structure, AccessibilityNodeProvider provider, AccessibilityNodeInfo info) {
        structure.setId(AccessibilityNodeInfo.getVirtualDescendantId(info.getSourceNodeId()), null, null, null);
        Rect rect = structure.getTempRect();
        info.getBoundsInParent(rect);
        structure.setDimens(rect.left, rect.top, 0, 0, rect.width(), rect.height());
        structure.setVisibility(0);
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
            for (int i = 0; i < NCHILDREN; i++) {
                AccessibilityNodeInfo cinfo = provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(info.getChildId(i)));
                populateVirtualStructure(structure.newChild(i), provider, cinfo);
                cinfo.recycle();
            }
        }
    }

    public void dispatchProvideStructure(ViewStructure structure) {
        dispatchProvideStructureForAssistOrAutofill(structure, false, 0);
    }

    public void dispatchProvideAutofillStructure(ViewStructure structure, int flags) {
        dispatchProvideStructureForAssistOrAutofill(structure, true, flags);
    }

    private void dispatchProvideStructureForAssistOrAutofill(ViewStructure structure, boolean forAutofill, int flags) {
        if (forAutofill) {
            structure.setAutofillId(getAutofillId());
            if (isLaidOut()) {
                onProvideAutofillStructure(structure, flags);
                onProvideAutofillVirtualStructure(structure, flags);
            } else {
                Log.w(VIEW_LOG_TAG, "dispatchProvideAutofillStructure(): not laid out, ignoring");
            }
        } else if (isAssistBlocked()) {
            structure.setClassName(getAccessibilityClassName().toString());
            structure.setAssistBlocked(true);
        } else {
            onProvideStructure(structure);
            onProvideVirtualStructure(structure);
        }
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
            if (this.mID != -1) {
                rootView = getRootView();
                if (rootView == null) {
                    rootView = this;
                }
                View label = rootView.findLabelForView(this, this.mID);
                if (label != null) {
                    info.setLabeledBy(label);
                }
                if (!(this.mAttachInfo == null || (this.mAttachInfo.mAccessibilityFetchFlags & 16) == 0 || !Resources.resourceHasPackage(this.mID))) {
                    try {
                        info.setViewIdResourceName(getResources().getResourceName(this.mID));
                    } catch (NotFoundException e) {
                    }
                }
            }
            if (this.mLabelForId != -1) {
                rootView = getRootView();
                if (rootView == null) {
                    rootView = this;
                }
                View labeled = rootView.findViewInsideOutShouldExist(this, this.mLabelForId);
                if (labeled != null) {
                    info.setLabelFor(labeled);
                }
            }
            if (this.mAccessibilityTraversalBeforeId != -1) {
                rootView = getRootView();
                if (rootView == null) {
                    rootView = this;
                }
                next = rootView.findViewInsideOutShouldExist(this, this.mAccessibilityTraversalBeforeId);
                if (next != null && next.includeForAccessibility()) {
                    info.setTraversalBefore(next);
                }
            }
            if (this.mAccessibilityTraversalAfterId != -1) {
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
            info.setImportantForAccessibility(isImportantForAccessibility());
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
            info.addAction(4);
            info.addAction(8);
            if (isFocusable()) {
                if (isFocused()) {
                    info.addAction(2);
                } else {
                    info.addAction(1);
                }
            }
            if (isAccessibilityFocused()) {
                info.addAction(128);
            } else {
                info.addAction(64);
            }
            if (isClickable() && isEnabled()) {
                info.addAction(16);
            }
            if (isLongClickable() && isEnabled()) {
                info.addAction(32);
            }
            if (isContextClickable() && isEnabled()) {
                info.addAction(AccessibilityAction.ACTION_CONTEXT_CLICK);
            }
            CharSequence text = getIterableTextForAccessibility();
            if (text != null && text.length() > 0) {
                info.setTextSelection(getAccessibilitySelectionStart(), getAccessibilitySelectionEnd());
                info.addAction(131072);
                info.addAction(256);
                info.addAction(512);
                info.setMovementGranularities(11);
            }
            info.addAction(AccessibilityAction.ACTION_SHOW_ON_SCREEN);
            populateAccessibilityNodeInfoDrawingOrderInParent(info);
        }
    }

    public void addExtraDataToAccessibilityNodeInfo(AccessibilityNodeInfo info, String extraDataKey, Bundle arguments) {
    }

    private void populateAccessibilityNodeInfoDrawingOrderInParent(AccessibilityNodeInfo info) {
        if ((this.mPrivateFlags & 16) == 0) {
            info.setDrawingOrder(0);
            return;
        }
        int drawingOrderInParent = 1;
        View viewAtDrawingLevel = this;
        View parent = getParentForAccessibility();
        while (viewAtDrawingLevel != parent) {
            ViewParent currentParent = viewAtDrawingLevel.getParent();
            if (!(currentParent instanceof ViewGroup)) {
                drawingOrderInParent = 0;
                break;
            }
            ViewGroup parentGroup = (ViewGroup) currentParent;
            int childCount = parentGroup.getChildCount();
            if (childCount > 1) {
                List<View> preorderedList = parentGroup.buildOrderedChildList();
                int i;
                if (preorderedList != null) {
                    for (i = 0; i < preorderedList.indexOf(viewAtDrawingLevel); i++) {
                        drawingOrderInParent += numViewsForAccessibility((View) preorderedList.get(i));
                    }
                } else {
                    int childDrawIndex;
                    int childIndex = parentGroup.indexOfChild(viewAtDrawingLevel);
                    boolean customOrder = parentGroup.isChildrenDrawingOrderEnabled();
                    if (childIndex < 0 || !customOrder) {
                        childDrawIndex = childIndex;
                    } else {
                        childDrawIndex = parentGroup.getChildDrawingOrder(childCount, childIndex);
                    }
                    int numChildrenToIterate = customOrder ? childCount : childDrawIndex;
                    if (childDrawIndex != 0) {
                        i = 0;
                        while (i < numChildrenToIterate) {
                            if ((customOrder ? parentGroup.getChildDrawingOrder(childCount, i) : i) < childDrawIndex) {
                                drawingOrderInParent += numViewsForAccessibility(parentGroup.getChildAt(i));
                            }
                            i++;
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
                return 1;
            }
            if (view instanceof ViewGroup) {
                return ((ViewGroup) view).getNumChildrenForAccessibility();
            }
        }
        return 0;
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
            return false;
        }
        View current = this;
        while (current instanceof View) {
            View view = current;
            if (view.getAlpha() <= 0.0f || view.getTransitionAlpha() <= 0.0f || view.getVisibility() != 0) {
                return false;
            }
            current = view.mParent;
        }
        Rect visibleRect = this.mAttachInfo.mTmpInvalRect;
        Point offset = this.mAttachInfo.mPoint;
        if (!getGlobalVisibleRect(visibleRect, offset)) {
            return false;
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
        if (this.mAccessibilityViewId == -1) {
            int i = sNextAccessibilityViewId;
            sNextAccessibilityViewId = i + 1;
            this.mAccessibilityViewId = i;
        }
        return this.mAccessibilityViewId;
    }

    public int getAutofillViewId() {
        if (this.mAutofillViewId == -1) {
            this.mAutofillViewId = this.mContext.getNextAutofillId();
        }
        return this.mAutofillViewId;
    }

    public int getAccessibilityWindowId() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mAccessibilityWindowId;
        }
        return -1;
    }

    @ExportedProperty(category = "accessibility")
    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    @RemotableViewMethod
    public void setContentDescription(CharSequence contentDescription) {
        if (this.mContentDescription == null) {
            if (contentDescription == null) {
                return;
            }
        } else if (this.mContentDescription.equals(contentDescription)) {
            return;
        }
        this.mContentDescription = contentDescription;
        boolean nonEmptyDesc = contentDescription != null && contentDescription.length() > 0;
        if (nonEmptyDesc && getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
            notifySubtreeAccessibilityStateChangedIfNeeded();
        } else {
            notifyViewAccessibilityStateChangedIfNeeded(4);
        }
    }

    @RemotableViewMethod
    public void setAccessibilityTraversalBefore(int beforeId) {
        if (this.mAccessibilityTraversalBeforeId != beforeId) {
            this.mAccessibilityTraversalBeforeId = beforeId;
            notifyViewAccessibilityStateChangedIfNeeded(0);
        }
    }

    public int getAccessibilityTraversalBefore() {
        return this.mAccessibilityTraversalBeforeId;
    }

    @RemotableViewMethod
    public void setAccessibilityTraversalAfter(int afterId) {
        if (this.mAccessibilityTraversalAfterId != afterId) {
            this.mAccessibilityTraversalAfterId = afterId;
            notifyViewAccessibilityStateChangedIfNeeded(0);
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
            if (this.mLabelForId != -1 && this.mID == -1) {
                this.mID = generateViewId();
            }
            notifyViewAccessibilityStateChangedIfNeeded(0);
        }
    }

    protected void onFocusLost() {
        resetPressedState();
    }

    private void resetPressedState() {
        if ((this.mViewFlags & 32) != 32 && isPressed()) {
            setPressed(false);
            if (!this.mHasPerformedLongPress) {
                removeLongPressCallback();
            }
        }
    }

    @ExportedProperty(category = "focus")
    public boolean isFocused() {
        return (this.mPrivateFlags & 2) != 0;
    }

    public View findFocus() {
        return (this.mPrivateFlags & 2) != 0 ? this : null;
    }

    public boolean isScrollContainer() {
        return (this.mPrivateFlags & 1048576) != 0;
    }

    public void setScrollContainer(boolean isScrollContainer) {
        if (isScrollContainer) {
            if (this.mAttachInfo != null && (this.mPrivateFlags & 1048576) == 0) {
                this.mAttachInfo.mScrollContainers.add(this);
                this.mPrivateFlags |= 1048576;
            }
            this.mPrivateFlags |= 524288;
            return;
        }
        if ((this.mPrivateFlags & 1048576) != 0) {
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
        return (this.mViewFlags & 67108864) != 0;
    }

    public void setKeepScreenOn(boolean keepScreenOn) {
        setFlags(keepScreenOn ? 67108864 : 0, 67108864);
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

    public int getNextClusterForwardId() {
        return this.mNextClusterForwardId;
    }

    public void setNextClusterForwardId(int nextClusterForwardId) {
        this.mNextClusterForwardId = nextClusterForwardId;
    }

    public boolean isShown() {
        View current = this;
        while ((current.mViewFlags & 12) == 0) {
            ViewParent parent = current.mParent;
            if (parent == null) {
                return false;
            }
            if (!(parent instanceof View)) {
                return true;
            }
            current = (View) parent;
            if (current == null) {
                return false;
            }
        }
        return false;
    }

    @Deprecated
    protected boolean fitSystemWindows(Rect insets) {
        if ((this.mPrivateFlags3 & 32) != 0) {
            return fitSystemWindowsInt(insets);
        }
        if (insets == null) {
            return false;
        }
        try {
            this.mPrivateFlags3 |= 64;
            boolean isConsumed = dispatchApplyWindowInsets(new WindowInsets(insets)).isConsumed();
            return isConsumed;
        } finally {
            this.mPrivateFlags3 &= -65;
        }
    }

    private boolean fitSystemWindowsInt(Rect insets) {
        if ((this.mViewFlags & 2) != 2) {
            return false;
        }
        this.mUserPaddingStart = Integer.MIN_VALUE;
        this.mUserPaddingEnd = Integer.MIN_VALUE;
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
        if ((this.mPrivateFlags3 & 64) == 0) {
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
            this.mPrivateFlags3 |= 32;
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
            location[0] = location[0] + this.mAttachInfo.mViewRootImpl.mWindowAttributes.surfaceInsets.left;
            location[1] = location[1] + this.mAttachInfo.mViewRootImpl.mWindowAttributes.surfaceInsets.top;
        }
    }

    public WindowInsets getRootWindowInsets() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mViewRootImpl.getWindowInsets(false);
        }
        return null;
    }

    protected boolean computeFitSystemWindows(Rect inoutInsets, Rect outLocalInsets) {
        if ((this.mViewFlags & 2048) == 0 || this.mAttachInfo == null || ((this.mAttachInfo.mSystemUiVisibility & SYSTEM_UI_LAYOUT_FLAGS) == 0 && (this.mAttachInfo.mOverscanRequested ^ 1) != 0)) {
            outLocalInsets.set(inoutInsets);
            inoutInsets.set(0, 0, 0, 0);
            return true;
        }
        Rect overscan = this.mAttachInfo.mOverscanInsets;
        outLocalInsets.set(overscan);
        inoutInsets.left -= overscan.left;
        inoutInsets.top -= overscan.top;
        inoutInsets.right -= overscan.right;
        inoutInsets.bottom -= overscan.bottom;
        return false;
    }

    public WindowInsets computeSystemWindowInsets(WindowInsets in, Rect outLocalInsets) {
        if ((this.mViewFlags & 2048) == 0 || this.mAttachInfo == null || (this.mAttachInfo.mSystemUiVisibility & SYSTEM_UI_LAYOUT_FLAGS) == 0) {
            outLocalInsets.set(in.getSystemWindowInsets());
            return in.consumeSystemWindowInsets();
        }
        outLocalInsets.set(0, 0, 0, 0);
        return in;
    }

    public void setFitsSystemWindows(boolean fitSystemWindows) {
        setFlags(fitSystemWindows ? 2 : 0, 2);
    }

    @ExportedProperty
    public boolean getFitsSystemWindows() {
        return (this.mViewFlags & 2) == 2;
    }

    public boolean fitsSystemWindows() {
        return getFitsSystemWindows();
    }

    @Deprecated
    public void requestFitSystemWindows() {
        if (this.mParent != null) {
            this.mParent.requestFitSystemWindows();
        }
    }

    public void requestApplyInsets() {
        requestFitSystemWindows();
    }

    public void makeOptionalFitsSystemWindows() {
        setFlags(2048, 2048);
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
        return this.mViewFlags & 12;
    }

    @RemotableViewMethod
    public void setVisibility(int visibility) {
        setFlags(visibility, 12);
    }

    @ExportedProperty
    public boolean isEnabled() {
        return (this.mViewFlags & 32) == 0;
    }

    @RemotableViewMethod
    public void setEnabled(boolean enabled) {
        if (enabled != isEnabled()) {
            setFlags(enabled ? 0 : 32, 32);
            refreshDrawableState();
            invalidate(true);
            if (!enabled) {
                cancelPendingInputEvents();
            }
        }
    }

    public void setFocusable(boolean focusable) {
        setFocusable(focusable ? 1 : 0);
    }

    public void setFocusable(int focusable) {
        if ((focusable & 17) == 0) {
            setFlags(0, 262144);
        }
        setFlags(focusable, 17);
    }

    public void setFocusableInTouchMode(boolean focusableInTouchMode) {
        setFlags(focusableInTouchMode ? 262144 : 0, 262144);
        if (focusableInTouchMode) {
            setFlags(1, 17);
        }
    }

    public void setAutofillHints(String... autofillHints) {
        if (autofillHints == null || autofillHints.length == 0) {
            this.mAutofillHints = null;
        } else {
            this.mAutofillHints = autofillHints;
        }
    }

    public void setAutofilled(boolean isAutofilled) {
        if (isAutofilled != isAutofilled()) {
            if (isAutofilled) {
                this.mPrivateFlags3 |= 65536;
            } else {
                this.mPrivateFlags3 &= -65537;
            }
            invalidate();
        }
    }

    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        setFlags(soundEffectsEnabled ? 134217728 : 0, 134217728);
    }

    @ExportedProperty
    public boolean isSoundEffectsEnabled() {
        return 134217728 == (this.mViewFlags & 134217728);
    }

    public void setHapticFeedbackEnabled(boolean hapticFeedbackEnabled) {
        setFlags(hapticFeedbackEnabled ? 268435456 : 0, 268435456);
    }

    @ExportedProperty
    public boolean isHapticFeedbackEnabled() {
        return 268435456 == (this.mViewFlags & 268435456);
    }

    @ExportedProperty(category = "layout", mapping = {@IntToString(from = 0, to = "LTR"), @IntToString(from = 1, to = "RTL"), @IntToString(from = 2, to = "INHERIT"), @IntToString(from = 3, to = "LOCALE")})
    public int getRawLayoutDirection() {
        return (this.mPrivateFlags2 & 12) >> 2;
    }

    @RemotableViewMethod
    public void setLayoutDirection(int layoutDirection) {
        if (getRawLayoutDirection() != layoutDirection) {
            this.mPrivateFlags2 &= -13;
            resetRtlProperties();
            this.mPrivateFlags2 |= (layoutDirection << 2) & 12;
            resolveRtlPropertiesIfNeeded();
            requestLayout();
            invalidate(true);
        }
    }

    @ExportedProperty(category = "layout", mapping = {@IntToString(from = 0, to = "RESOLVED_DIRECTION_LTR"), @IntToString(from = 1, to = "RESOLVED_DIRECTION_RTL")})
    public int getLayoutDirection() {
        int i = 0;
        if (getContext().getApplicationInfo().targetSdkVersion >= 17 || (this.mForceRTL ^ 1) == 0) {
            if ((this.mPrivateFlags2 & 16) == 16) {
                i = 1;
            }
            return i;
        }
        this.mPrivateFlags2 |= 32;
        return 0;
    }

    @ExportedProperty(category = "layout")
    public boolean isLayoutRtl() {
        return getLayoutDirection() == 1;
    }

    public boolean isRtlLocale() {
        String currentLang = Locale.getDefault().getLanguage();
        return (currentLang.contains("ar") || currentLang.contains("fa") || currentLang.contains("iw") || currentLang.contains("ug") || currentLang.contains("ur")) ? true : isLayoutRtl();
    }

    @ExportedProperty(category = "layout")
    public boolean hasTransientState() {
        return (this.mPrivateFlags2 & Integer.MIN_VALUE) == Integer.MIN_VALUE;
    }

    public void setHasTransientState(boolean hasTransientState) {
        int i;
        if (hasTransientState) {
            i = this.mTransientStateCount + 1;
        } else {
            i = this.mTransientStateCount - 1;
        }
        this.mTransientStateCount = i;
        if (this.mTransientStateCount < 0) {
            this.mTransientStateCount = 0;
            Log.e(VIEW_LOG_TAG, "hasTransientState decremented below 0: unmatched pair of setHasTransientState calls");
        } else if ((hasTransientState && this.mTransientStateCount == 1) || (!hasTransientState && this.mTransientStateCount == 0)) {
            int i2 = Integer.MAX_VALUE & this.mPrivateFlags2;
            if (hasTransientState) {
                i = Integer.MIN_VALUE;
            } else {
                i = 0;
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
    }

    public boolean isAttachedToWindow() {
        return this.mAttachInfo != null;
    }

    public boolean isLaidOut() {
        return (this.mPrivateFlags3 & 4) == 4;
    }

    public void setWillNotDraw(boolean willNotDraw) {
        setFlags(willNotDraw ? 128 : 0, 128);
    }

    @ExportedProperty(category = "drawing")
    public boolean willNotDraw() {
        return (this.mViewFlags & 128) == 128;
    }

    public void setWillNotCacheDrawing(boolean willNotCacheDrawing) {
        setFlags(willNotCacheDrawing ? 131072 : 0, 131072);
    }

    @ExportedProperty(category = "drawing")
    public boolean willNotCacheDrawing() {
        return (this.mViewFlags & 131072) == 131072;
    }

    @ExportedProperty
    public boolean isClickable() {
        return (this.mViewFlags & 16384) == 16384;
    }

    public void setClickable(boolean clickable) {
        setFlags(clickable ? 16384 : 0, 16384);
    }

    public boolean isLongClickable() {
        return (this.mViewFlags & 2097152) == 2097152;
    }

    public void setLongClickable(boolean longClickable) {
        setFlags(longClickable ? 2097152 : 0, 2097152);
    }

    public boolean isContextClickable() {
        return (this.mViewFlags & 8388608) == 8388608;
    }

    public void setContextClickable(boolean contextClickable) {
        setFlags(contextClickable ? 8388608 : 0, 8388608);
    }

    private void setPressed(boolean pressed, float x, float y) {
        if (pressed) {
            drawableHotspotChanged(x, y);
        }
        setPressed(pressed);
    }

    public void setPressed(boolean pressed) {
        boolean needsRefresh = pressed != ((this.mPrivateFlags & 16384) == 16384);
        if (pressed) {
            this.mPrivateFlags |= 16384;
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
        return (this.mPrivateFlags & 16384) == 16384;
    }

    public boolean isAssistBlocked() {
        return (this.mPrivateFlags3 & 16384) != 0;
    }

    public void setAssistBlocked(boolean enabled) {
        if (enabled) {
            this.mPrivateFlags3 |= 16384;
        } else {
            this.mPrivateFlags3 &= -16385;
        }
    }

    public boolean isSaveEnabled() {
        return (this.mViewFlags & 65536) != 65536;
    }

    public void setSaveEnabled(boolean enabled) {
        int i;
        if (enabled) {
            i = 0;
        } else {
            i = 65536;
        }
        setFlags(i, 65536);
    }

    @ExportedProperty
    public boolean getFilterTouchesWhenObscured() {
        return (this.mViewFlags & 1024) != 0;
    }

    public void setFilterTouchesWhenObscured(boolean enabled) {
        setFlags(enabled ? 1024 : 0, 1024);
    }

    public boolean isSaveFromParentEnabled() {
        return (this.mViewFlags & 536870912) != 536870912;
    }

    public void setSaveFromParentEnabled(boolean enabled) {
        int i;
        if (enabled) {
            i = 0;
        } else {
            i = 536870912;
        }
        setFlags(i, 536870912);
    }

    @ExportedProperty(category = "focus")
    public final boolean isFocusable() {
        return 1 == (this.mViewFlags & 1);
    }

    @ExportedProperty(category = "focus", mapping = {@IntToString(from = 0, to = "NOT_FOCUSABLE"), @IntToString(from = 1, to = "FOCUSABLE"), @IntToString(from = 16, to = "FOCUSABLE_AUTO")})
    public int getFocusable() {
        return (this.mViewFlags & 16) > 0 ? 16 : this.mViewFlags & 1;
    }

    @ExportedProperty(category = "focus")
    public final boolean isFocusableInTouchMode() {
        return 262144 == (this.mViewFlags & 262144);
    }

    public View focusSearch(int direction) {
        if (this.mParent != null) {
            return this.mParent.focusSearch(this, direction);
        }
        return null;
    }

    @ExportedProperty(category = "focus")
    public final boolean isKeyboardNavigationCluster() {
        return (this.mPrivateFlags3 & 32768) != 0;
    }

    View findKeyboardNavigationCluster() {
        if (this.mParent instanceof View) {
            View cluster = ((View) this.mParent).findKeyboardNavigationCluster();
            if (cluster != null) {
                return cluster;
            }
            if (isKeyboardNavigationCluster()) {
                return this;
            }
        }
        return null;
    }

    public void setKeyboardNavigationCluster(boolean isCluster) {
        if (isCluster) {
            this.mPrivateFlags3 |= 32768;
        } else {
            this.mPrivateFlags3 &= -32769;
        }
    }

    public final void setFocusedInCluster() {
        setFocusedInCluster(findKeyboardNavigationCluster());
    }

    private void setFocusedInCluster(View cluster) {
        if (this instanceof ViewGroup) {
            ((ViewGroup) this).mFocusedInCluster = null;
        }
        if (cluster != this) {
            View child = this;
            for (View parent = this.mParent; parent instanceof ViewGroup; parent = parent.getParent()) {
                ((ViewGroup) parent).mFocusedInCluster = child;
                if (parent == cluster) {
                    break;
                }
                child = parent;
            }
        }
    }

    private void updateFocusedInCluster(View oldFocus, int direction) {
        if (oldFocus != null) {
            View oldCluster = oldFocus.findKeyboardNavigationCluster();
            if (oldCluster != findKeyboardNavigationCluster()) {
                oldFocus.setFocusedInCluster(oldCluster);
                if (!(oldFocus.mParent instanceof ViewGroup)) {
                    return;
                }
                if (direction == 2 || direction == 1) {
                    ((ViewGroup) oldFocus.mParent).clearFocusedInCluster(oldFocus);
                } else if ((oldFocus instanceof ViewGroup) && ((ViewGroup) oldFocus).getDescendantFocusability() == 262144 && ViewRootImpl.isViewDescendantOf(this, oldFocus)) {
                    ((ViewGroup) oldFocus.mParent).clearFocusedInCluster(oldFocus);
                }
            }
        }
    }

    @ExportedProperty(category = "focus")
    public final boolean isFocusedByDefault() {
        return (this.mPrivateFlags3 & 262144) != 0;
    }

    public void setFocusedByDefault(boolean isFocusedByDefault) {
        boolean z = false;
        if ((this.mPrivateFlags3 & 262144) != 0) {
            z = true;
        }
        if (isFocusedByDefault != z) {
            if (isFocusedByDefault) {
                this.mPrivateFlags3 |= 262144;
            } else {
                this.mPrivateFlags3 &= -262145;
            }
            if (this.mParent instanceof ViewGroup) {
                if (isFocusedByDefault) {
                    ((ViewGroup) this.mParent).setDefaultFocus(this);
                } else {
                    ((ViewGroup) this.mParent).clearDefaultFocus(this);
                }
            }
        }
    }

    boolean hasDefaultFocus() {
        return isFocusedByDefault();
    }

    public View keyboardNavigationClusterSearch(View currentCluster, int direction) {
        if (isKeyboardNavigationCluster()) {
            currentCluster = this;
        }
        if (isRootNamespace()) {
            return FocusFinder.getInstance().findNextKeyboardNavigationCluster(this, currentCluster, direction);
        }
        if (this.mParent != null) {
            return this.mParent.keyboardNavigationClusterSearch(currentCluster, direction);
        }
        return null;
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return false;
    }

    public void setDefaultFocusHighlightEnabled(boolean defaultFocusHighlightEnabled) {
        this.mDefaultFocusHighlightEnabled = defaultFocusHighlightEnabled;
    }

    @ExportedProperty(category = "focus")
    public final boolean getDefaultFocusHighlightEnabled() {
        return this.mDefaultFocusHighlightEnabled;
    }

    View findUserSetNextFocus(View root, int direction) {
        switch (direction) {
            case 1:
                if (this.mID == -1) {
                    return null;
                }
                final int id = this.mID;
                return root.findViewByPredicateInsideOut(this, new Predicate<View>() {
                    public boolean test(View t) {
                        return t.mNextFocusForwardId == id;
                    }
                });
            case 2:
                if (this.mNextFocusForwardId == -1) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextFocusForwardId);
            case 17:
                if (this.mNextFocusLeftId == -1) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextFocusLeftId);
            case 33:
                if (this.mNextFocusUpId == -1) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextFocusUpId);
            case 66:
                if (this.mNextFocusRightId == -1) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextFocusRightId);
            case 130:
                if (this.mNextFocusDownId == -1) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextFocusDownId);
            default:
                return null;
        }
    }

    View findUserSetNextKeyboardNavigationCluster(View root, int direction) {
        switch (direction) {
            case 1:
                if (this.mID == -1) {
                    return null;
                }
                return root.findViewByPredicateInsideOut(this, new android.view.-$Lambda$iU_USrtPm1XIm5H9QYQvXfBGDE4.AnonymousClass2(this.mID));
            case 2:
                if (this.mNextClusterForwardId == -1) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextClusterForwardId);
            default:
                return null;
        }
    }

    static /* synthetic */ boolean lambda$-android_view_View_420373(int id, View t) {
        return t.mNextClusterForwardId == id;
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
        addFocusables(views, direction, isInTouchMode() ? 1 : 0);
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (views == null || !isFocusable()) {
            return;
        }
        if (((focusableMode & 1) != 1 || (isFocusableInTouchMode() ^ 1) == 0) && !hasAncestorThatBlocksDescendantFocus()) {
            views.add(this);
        }
    }

    public void addKeyboardNavigationClusters(Collection<View> views, int direction) {
        if (isKeyboardNavigationCluster() && hasFocusable()) {
            views.add(this);
        }
    }

    public void findViewsWithText(ArrayList<View> outViews, CharSequence searched, int flags) {
        if (getAccessibilityNodeProvider() != null) {
            if ((flags & 4) != 0) {
                outViews.add(this);
            }
        } else if ((flags & 2) != 0 && searched != null && searched.length() > 0 && this.mContentDescription != null && this.mContentDescription.length() > 0) {
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
        if (((viewFlags & 16384) == 16384 || (viewFlags & 2097152) == 2097152 || (viewFlags & 8388608) == 8388608) && (viewFlags & 32) == 0) {
            views.add(this);
        }
    }

    public boolean isAccessibilityFocused() {
        return (this.mPrivateFlags2 & 67108864) != 0;
    }

    /* JADX WARNING: Missing block: B:4:0x0018, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean requestAccessibilityFocus() {
        AccessibilityManager manager = AccessibilityManager.getInstance(this.mContext);
        if (!manager.isEnabled() || (manager.isTouchExplorationEnabled() ^ 1) != 0 || (this.mViewFlags & 12) != 0 || (this.mPrivateFlags2 & 67108864) != 0) {
            return false;
        }
        this.mPrivateFlags2 |= 67108864;
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null) {
            viewRootImpl.setAccessibilityFocus(this, null);
        }
        invalidate();
        sendAccessibilityEvent(32768);
        return true;
    }

    public void clearAccessibilityFocus() {
        clearAccessibilityFocusNoCallbacks(0);
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
        if ((this.mPrivateFlags2 & 67108864) != 0) {
            this.mPrivateFlags2 &= -67108865;
            invalidate();
            if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(65536);
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
        return requestFocus(130);
    }

    public boolean restoreFocusInCluster(int direction) {
        if (restoreDefaultFocus()) {
            return true;
        }
        return requestFocus(direction);
    }

    public boolean restoreFocusNotInCluster() {
        return requestFocus(130);
    }

    public boolean restoreDefaultFocus() {
        return requestFocus(130);
    }

    public final boolean requestFocus(int direction) {
        return requestFocus(direction, null);
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return requestFocusNoSearch(direction, previouslyFocusedRect);
    }

    private boolean requestFocusNoSearch(int direction, Rect previouslyFocusedRect) {
        if ((this.mViewFlags & 1) != 1 || (this.mViewFlags & 12) != 0) {
            return false;
        }
        if ((isInTouchMode() && 262144 != (this.mViewFlags & 262144)) || hasAncestorThatBlocksDescendantFocus()) {
            return false;
        }
        handleFocusGainInternal(direction, previouslyFocusedRect);
        return true;
    }

    public final boolean requestFocusFromTouch() {
        if (isInTouchMode()) {
            ViewRootImpl viewRoot = getViewRootImpl();
            if (viewRoot != null) {
                viewRoot.ensureTouchMode(false);
            }
        }
        return requestFocus(130);
    }

    private boolean hasAncestorThatBlocksDescendantFocus() {
        boolean focusableInTouchMode = isFocusableInTouchMode();
        ViewParent ancestor = this.mParent;
        while (ancestor instanceof ViewGroup) {
            ViewGroup vgAncestor = (ViewGroup) ancestor;
            if (vgAncestor.getDescendantFocusability() == 393216 || (!focusableInTouchMode && vgAncestor.shouldBlockFocusForTouchscreen())) {
                return true;
            }
            ancestor = vgAncestor.getParent();
        }
        return false;
    }

    @ExportedProperty(category = "accessibility", mapping = {@IntToString(from = 0, to = "auto"), @IntToString(from = 1, to = "yes"), @IntToString(from = 2, to = "no"), @IntToString(from = 4, to = "noHideDescendants")})
    public int getImportantForAccessibility() {
        return (this.mPrivateFlags2 & PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_MASK) >> 20;
    }

    public void setAccessibilityLiveRegion(int mode) {
        if (mode != getAccessibilityLiveRegion()) {
            this.mPrivateFlags2 &= -25165825;
            this.mPrivateFlags2 |= (mode << 23) & 25165824;
            notifyViewAccessibilityStateChangedIfNeeded(0);
        }
    }

    public int getAccessibilityLiveRegion() {
        return (this.mPrivateFlags2 & 25165824) >> 23;
    }

    public void setImportantForAccessibility(int mode) {
        int oldMode = getImportantForAccessibility();
        if (mode != oldMode) {
            boolean hideDescendants = mode == 4;
            if (mode == 2 || hideDescendants) {
                View focusHost = findAccessibilityFocusHost(hideDescendants);
                if (focusHost != null) {
                    focusHost.clearAccessibilityFocus();
                }
            }
            boolean maySkipNotify = oldMode != 0 ? mode == 0 : true;
            boolean oldIncludeForAccessibility = maySkipNotify ? includeForAccessibility() : false;
            this.mPrivateFlags2 &= -7340033;
            this.mPrivateFlags2 |= (mode << 20) & PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_MASK;
            if (maySkipNotify && oldIncludeForAccessibility == includeForAccessibility()) {
                notifyViewAccessibilityStateChangedIfNeeded(0);
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
        int mode = (this.mPrivateFlags2 & PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_MASK) >> 20;
        if (mode == 2 || mode == 4) {
            return false;
        }
        for (ViewParent parent = this.mParent; parent instanceof View; parent = parent.getParent()) {
            if (((View) parent).getImportantForAccessibility() == 4) {
                return false;
            }
        }
        boolean z = (mode == 1 || isActionableForAccessibility() || hasListenersForAccessibility() || getAccessibilityNodeProvider() != null) ? true : getAccessibilityLiveRegion() != 0;
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
            return false;
        }
        boolean isImportantForAccessibility;
        if ((this.mAttachInfo.mAccessibilityFetchFlags & 8) == 0) {
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
            return false;
        }
        return true;
    }

    public void notifyViewAccessibilityStateChangedIfNeeded(int changeType) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && this.mAttachInfo != null) {
            if (this.mSendViewStateChangedAccessibilityEvent == null) {
                this.mSendViewStateChangedAccessibilityEvent = new SendViewStateChangedAccessibilityEvent(this, null);
            }
            this.mSendViewStateChangedAccessibilityEvent.runOrPost(changeType);
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifySubtreeAccessibilityStateChangedIfNeeded() {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && this.mAttachInfo != null && (this.mPrivateFlags2 & 134217728) == 0) {
            this.mPrivateFlags2 |= 134217728;
            if (this.mParent != null) {
                try {
                    this.mParent.notifySubtreeAccessibilityStateChanged(this, this, 1);
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
        return false;
    }

    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (this.mAccessibilityDelegate != null) {
            return this.mAccessibilityDelegate.performAccessibilityAction(this, action, arguments);
        }
        return performAccessibilityActionInternal(action, arguments);
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (isNestedScrollingEnabled() && ((action == 8192 || action == 4096 || action == R.id.accessibilityActionScrollUp || action == R.id.accessibilityActionScrollLeft || action == R.id.accessibilityActionScrollDown || action == R.id.accessibilityActionScrollRight) && dispatchNestedPrePerformAccessibilityAction(action, arguments))) {
            return true;
        }
        switch (action) {
            case 1:
                if (!hasFocus()) {
                    getViewRootImpl().ensureTouchMode(false);
                    return requestFocus();
                }
                break;
            case 2:
                if (hasFocus()) {
                    clearFocus();
                    return isFocused() ^ 1;
                }
                break;
            case 4:
                if (!isSelected()) {
                    setSelected(true);
                    return isSelected();
                }
                break;
            case 8:
                if (isSelected()) {
                    setSelected(false);
                    return isSelected() ^ 1;
                }
                break;
            case 16:
                if (isClickable()) {
                    performClick();
                    return true;
                }
                break;
            case 32:
                if (isLongClickable()) {
                    performLongClick();
                    return true;
                }
                break;
            case 64:
                if (!isAccessibilityFocused()) {
                    return requestAccessibilityFocus();
                }
                break;
            case 128:
                if (isAccessibilityFocused()) {
                    clearAccessibilityFocus();
                    return true;
                }
                break;
            case 256:
                if (arguments != null) {
                    return traverseAtGranularity(arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT), true, arguments.getBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN));
                }
                break;
            case 512:
                if (arguments != null) {
                    return traverseAtGranularity(arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT), false, arguments.getBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN));
                }
                break;
            case 131072:
                if (getIterableTextForAccessibility() == null) {
                    return false;
                }
                int start;
                int end;
                if (arguments != null) {
                    start = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, -1);
                } else {
                    start = -1;
                }
                if (arguments != null) {
                    end = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, -1);
                } else {
                    end = -1;
                }
                if (!(getAccessibilitySelectionStart() == start && getAccessibilitySelectionEnd() == end) && start == end) {
                    setAccessibilitySelection(start, end);
                    notifyViewAccessibilityStateChangedIfNeeded(0);
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
        return false;
    }

    private boolean traverseAtGranularity(int granularity, boolean forward, boolean extendSelection) {
        CharSequence text = getIterableTextForAccessibility();
        if (text == null || text.length() == 0) {
            return false;
        }
        TextSegmentIterator iterator = getIteratorForGranularity(granularity);
        if (iterator == null) {
            return false;
        }
        int current = getAccessibilitySelectionEnd();
        if (current == -1) {
            current = forward ? 0 : text.length();
        }
        int[] range = forward ? iterator.following(current) : iterator.preceding(current);
        if (range == null) {
            return false;
        }
        int selectionStart;
        int selectionEnd;
        int action;
        int segmentStart = range[0];
        int segmentEnd = range[1];
        if (extendSelection && isAccessibilitySelectionExtendable()) {
            selectionStart = getAccessibilitySelectionStart();
            if (selectionStart == -1) {
                selectionStart = forward ? segmentStart : segmentEnd;
            }
            selectionEnd = forward ? segmentEnd : segmentStart;
        } else {
            selectionEnd = forward ? segmentEnd : segmentStart;
            selectionStart = selectionEnd;
        }
        setAccessibilitySelection(selectionStart, selectionEnd);
        if (forward) {
            action = 256;
        } else {
            action = 512;
        }
        sendViewTextTraversedAtGranularityEvent(action, granularity, segmentStart, segmentEnd);
        return true;
    }

    public CharSequence getIterableTextForAccessibility() {
        return getContentDescription();
    }

    public boolean isAccessibilitySelectionExtendable() {
        return false;
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
                this.mAccessibilityCursorPosition = -1;
            } else {
                this.mAccessibilityCursorPosition = start;
            }
            sendAccessibilityEvent(8192);
        }
    }

    private void sendViewTextTraversedAtGranularityEvent(int action, int granularity, int fromIndex, int toIndex) {
        if (this.mParent != null) {
            AccessibilityEvent event = AccessibilityEvent.obtain(131072);
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
            case 1:
                text = getIterableTextForAccessibility();
                if (text != null && text.length() > 0) {
                    CharacterTextSegmentIterator iterator = CharacterTextSegmentIterator.getInstance(this.mContext.getResources().getConfiguration().locale);
                    iterator.initialize(text.toString());
                    return iterator;
                }
            case 2:
                text = getIterableTextForAccessibility();
                if (text != null && text.length() > 0) {
                    WordTextSegmentIterator iterator2 = WordTextSegmentIterator.getInstance(this.mContext.getResources().getConfiguration().locale);
                    iterator2.initialize(text.toString());
                    return iterator2;
                }
            case 8:
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
        return (this.mPrivateFlags3 & 33554432) != 0;
    }

    public void dispatchStartTemporaryDetach() {
        this.mPrivateFlags3 |= 33554432;
        notifyEnterOrExitForAutoFillIfNeeded(false);
        onStartTemporaryDetach();
    }

    public void onStartTemporaryDetach() {
        removeUnsetPressCallback();
        this.mPrivateFlags |= 67108864;
    }

    public void dispatchFinishTemporaryDetach() {
        this.mPrivateFlags3 &= -33554433;
        onFinishTemporaryDetach();
        if (hasWindowFocus() && hasFocus()) {
            InputMethodManager.getInstance().focusIn(this);
        }
        notifyEnterOrExitForAutoFillIfNeeded(true);
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
            this.mInputEventConsistencyVerifier.onKeyEvent(event, 0);
        }
        ListenerInfo li = this.mListenerInfo;
        if (li != null && li.mOnKeyListener != null && (this.mViewFlags & 32) == 0 && li.mOnKeyListener.onKey(this, event.getKeyCode(), event)) {
            return true;
        }
        if (this.mAttachInfo != null) {
            dispatcherState = this.mAttachInfo.mKeyDispatchState;
        }
        if (event.dispatch(this, dispatcherState, this)) {
            return true;
        }
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, 0);
        }
        return false;
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return onKeyShortcut(event.getKeyCode(), event);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.isTargetAccessibilityFocus()) {
            if (!isAccessibilityFocusedViewOrHost()) {
                return false;
            }
            event.setTargetAccessibilityFocus(false);
        }
        boolean result = false;
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTouchEvent(event, 0);
        }
        int actionMasked = event.getActionMasked();
        if (actionMasked == 0) {
            stopNestedScroll();
        }
        if (onFilterTouchEventForSecurity(event)) {
            if ((this.mViewFlags & 32) == 0 && handleScrollBarDragging(event)) {
                result = true;
            }
            ListenerInfo li = this.mListenerInfo;
            if (li != null && li.mOnTouchListener != null && (this.mViewFlags & 32) == 0 && li.mOnTouchListener.onTouch(this, event)) {
                result = true;
            }
            if (!result && onTouchEvent(event)) {
                result = true;
            }
        }
        if (!(result || this.mInputEventConsistencyVerifier == null)) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, 0);
        }
        if (actionMasked == 1 || actionMasked == 3 || (actionMasked == 0 && (result ^ 1) != 0)) {
            stopNestedScroll();
        }
        return result;
    }

    boolean isAccessibilityFocusedViewOrHost() {
        if (isAccessibilityFocused()) {
            return true;
        }
        return getViewRootImpl() != null && getViewRootImpl().getAccessibilityFocusedHost() == this;
    }

    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        if ((this.mViewFlags & 1024) == 0 || (event.getFlags() & 1) == 0) {
            return true;
        }
        return false;
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onTrackballEvent(event, 0);
        }
        return onTrackballEvent(event);
    }

    public boolean dispatchCapturedPointerEvent(MotionEvent event) {
        if (!hasPointerCapture()) {
            return false;
        }
        ListenerInfo li = this.mListenerInfo;
        if (li == null || li.mOnCapturedPointerListener == null || !li.mOnCapturedPointerListener.onCapturedPointer(this, event)) {
            return onCapturedPointerEvent(event);
        }
        return true;
    }

    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onGenericMotionEvent(event, 0);
        }
        if ((event.getSource() & 2) != 0) {
            int action = event.getAction();
            if (action == 9 || action == 7 || action == 10) {
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
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, 0);
        }
        return false;
    }

    private boolean dispatchGenericMotionEventInternal(MotionEvent event) {
        ListenerInfo li = this.mListenerInfo;
        if ((li != null && li.mOnGenericMotionListener != null && (this.mViewFlags & 32) == 0 && li.mOnGenericMotionListener.onGenericMotion(this, event)) || onGenericMotionEvent(event)) {
            return true;
        }
        int actionButton = event.getActionButton();
        switch (event.getActionMasked()) {
            case 11:
                if (isContextClickable() && (this.mInContextButtonPress ^ 1) != 0 && (this.mHasPerformedLongPress ^ 1) != 0 && ((actionButton == 32 || actionButton == 2) && performContextClick(event.getX(), event.getY()))) {
                    this.mInContextButtonPress = true;
                    setPressed(true, event.getX(), event.getY());
                    removeTapCallback();
                    removeLongPressCallback();
                    return true;
                }
            case 12:
                if (this.mInContextButtonPress && (actionButton == 32 || actionButton == 2)) {
                    this.mInContextButtonPress = false;
                    this.mIgnoreNextUpEvent = true;
                    break;
                }
        }
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, 0);
        }
        return false;
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        ListenerInfo li = this.mListenerInfo;
        if (li == null || li.mOnHoverListener == null || (this.mViewFlags & 32) != 0 || !li.mOnHoverListener.onHover(this, event)) {
            return onHoverEvent(event);
        }
        return true;
    }

    protected boolean hasHoveredChild() {
        return false;
    }

    protected boolean dispatchGenericPointerEvent(MotionEvent event) {
        return false;
    }

    protected boolean dispatchGenericFocusedEvent(MotionEvent event) {
        return false;
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
                setPressed(false);
            }
            this.mPrivateFlags3 &= -131073;
            if (!(imm == null || (this.mPrivateFlags & 2) == 0)) {
                imm.focusOut(this);
            }
            removeLongPressCallback();
            removeTapCallback();
            onFocusLost();
        } else if (!(imm == null || (this.mPrivateFlags & 2) == 0)) {
            imm.focusIn(this);
        }
        notifyEnterOrExitForAutoFillIfNeeded(hasWindowFocus);
        refreshDrawableState();
    }

    public boolean hasWindowFocus() {
        return this.mAttachInfo != null ? this.mAttachInfo.mHasWindowFocus : false;
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
        boolean thisVisible = getVisibility() == 0;
        if (thisVisible || (isVisible ^ 1) != 0) {
            onVisibilityAggregated(isVisible);
        }
        if (thisVisible) {
            return isVisible;
        }
        return false;
    }

    public void onVisibilityAggregated(boolean isVisible) {
        if (isVisible && this.mAttachInfo != null) {
            initialAwakenScrollBars();
        }
        Drawable dr = this.mBackground;
        if (!(dr == null || isVisible == dr.isVisible())) {
            dr.setVisible(isVisible, false);
        }
        Drawable hl = this.mDefaultFocusHighlight;
        if (!(hl == null || isVisible == hl.isVisible())) {
            hl.setVisible(isVisible, false);
        }
        Drawable fg = this.mForegroundInfo != null ? this.mForegroundInfo.mDrawable : null;
        if (!(fg == null || isVisible == fg.isVisible())) {
            fg.setVisible(isVisible, false);
        }
        if (isAutofillable()) {
            AutofillManager afm = getAutofillManager();
            if (afm != null && getAutofillViewId() > LAST_APP_AUTOFILL_ID) {
                if (this.mVisibilityChangeForAutofillHandler != null) {
                    this.mVisibilityChangeForAutofillHandler.removeMessages(0);
                }
                if (isVisible) {
                    afm.notifyViewVisibilityChange(this, true);
                    return;
                }
                if (this.mVisibilityChangeForAutofillHandler == null) {
                    this.mVisibilityChangeForAutofillHandler = new VisibilityChangeForAutofillHandler(afm, this, null);
                }
                this.mVisibilityChangeForAutofillHandler.obtainMessage(0, this).sendToTarget();
            }
        }
    }

    public int getWindowVisibility() {
        return this.mAttachInfo != null ? this.mAttachInfo.mWindowVisibility : 8;
    }

    private void adjustRectSizeWhenInCompat(Rect inoutRect) {
        Resources resources = getResources();
        if (!(resources == null || resources.getCompatibilityInfo().supportsScreen())) {
            DisplayMetrics metrics = resources.getDisplayMetrics();
            if (!(metrics == null || metrics.noncompatWidthPixels == 0)) {
                inoutRect.scale((((float) metrics.widthPixels) * 1.0f) / ((float) metrics.noncompatWidthPixels));
            }
        }
    }

    public void getWindowVisibleDisplayFrame(Rect outRect) {
        if (this.mAttachInfo != null) {
            try {
                this.mAttachInfo.mSession.getDisplayFrame(this.mAttachInfo.mWindow, outRect);
                if (outRect.isEmpty()) {
                    Log.w(VIEW_LOG_TAG, "WindowVisibleDisplayFrame is Empty");
                    if ("com.android.contacts".equals(getContext().getPackageName())) {
                        DisplayManagerGlobal.getInstance().getRealDisplay(0).getRectSize(outRect);
                    }
                }
                adjustRectSizeWhenInCompat(outRect);
                Rect insets = this.mAttachInfo.mVisibleInsets;
                outRect.left += insets.left;
                outRect.top += insets.top;
                outRect.right -= insets.right;
                outRect.bottom -= insets.bottom;
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        DisplayManagerGlobal.getInstance().getRealDisplay(0).getRectSize(outRect);
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
        DisplayManagerGlobal.getInstance().getRealDisplay(0).getRectSize(outRect);
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
        if ((visibility & 12) == 0) {
            if ((this.mViewFlags & 67108864) == 67108864) {
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
        if (ai != null && (ai.mRecomputeGlobalAttributes ^ 1) != 0) {
            if (force || ai.mKeepScreenOn || ai.mSystemUiVisibility != 0 || ai.mHasSystemUiListeners) {
                ai.mRecomputeGlobalAttributes = true;
            }
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
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.isConfirmKey(keyCode)) {
            if ((this.mViewFlags & 32) == 32) {
                return true;
            }
            if (event.getRepeatCount() == 0) {
                boolean clickable = (this.mViewFlags & 16384) != 16384 ? (this.mViewFlags & 2097152) == 2097152 : true;
                if (clickable || (this.mViewFlags & 1073741824) == 1073741824) {
                    float x = ((float) getWidth()) / 2.0f;
                    float y = ((float) getHeight()) / 2.0f;
                    if (clickable) {
                        setPressed(true, x, y);
                    }
                    checkForLongClick(0, x, y);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.isConfirmKey(keyCode)) {
            if ((this.mViewFlags & 32) == 32) {
                return true;
            }
            if ((this.mViewFlags & 16384) == 16384 && isPressed()) {
                setPressed(false);
                if (!this.mHasPerformedLongPress) {
                    removeLongPressCallback();
                    if (!event.isCanceled()) {
                        return performClick();
                    }
                }
            }
        }
        return false;
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return false;
    }

    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onCheckIsTextEditor() {
        return false;
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return null;
    }

    public boolean checkInputConnectionProxy(View view) {
        return false;
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
        return false;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return false;
    }

    public boolean onHoverEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (this.mSendingHoverAccessibilityEvents) {
            if (action == 10 || (action == 2 && (pointInView(event.getX(), event.getY()) ^ 1) != 0)) {
                this.mSendingHoverAccessibilityEvents = false;
                sendAccessibilityHoverEvent(256);
            }
        } else if ((action == 9 || action == 7) && (hasHoveredChild() ^ 1) != 0 && pointInView(event.getX(), event.getY())) {
            sendAccessibilityHoverEvent(128);
            this.mSendingHoverAccessibilityEvents = true;
        }
        if ((action == 9 || action == 7) && event.isFromSource(InputDevice.SOURCE_MOUSE) && isOnScrollbar(event.getX(), event.getY())) {
            awakenScrollBars();
        }
        if (!isHoverable() && !isHovered()) {
            return false;
        }
        switch (action) {
            case 9:
                setHovered(true);
                break;
            case 10:
                setHovered(false);
                break;
        }
        dispatchGenericMotionEventInternal(event);
        return true;
    }

    private boolean isHoverable() {
        boolean z = true;
        int viewFlags = this.mViewFlags;
        if ((viewFlags & 32) == 32) {
            return false;
        }
        if (!((viewFlags & 16384) == 16384 || (viewFlags & 2097152) == 2097152 || (viewFlags & 8388608) == 8388608)) {
            z = false;
        }
        return z;
    }

    @ExportedProperty
    public boolean isHovered() {
        return (this.mPrivateFlags & 268435456) != 0;
    }

    public void setHovered(boolean hovered) {
        if (hovered) {
            if ((this.mPrivateFlags & 268435456) == 0) {
                this.mPrivateFlags |= 268435456;
                refreshDrawableState();
                onHoverChanged(true);
            }
        } else if ((this.mPrivateFlags & 268435456) != 0) {
            this.mPrivateFlags &= -268435457;
            refreshDrawableState();
            onHoverChanged(false);
        }
    }

    public void onHoverChanged(boolean hovered) {
    }

    protected boolean handleScrollBarDragging(MotionEvent event) {
        if (this.mScrollCache == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        if ((this.mScrollCache.mScrollBarDraggingState != 0 || action == 0) && (event.isFromSource(InputDevice.SOURCE_MOUSE) ^ 1) == 0 && (event.isButtonPressed(1) ^ 1) == 0) {
            switch (action) {
                case 0:
                    break;
                case 2:
                    if (this.mScrollCache.mScrollBarDraggingState == 0) {
                        return false;
                    }
                    Rect bounds;
                    int range;
                    int offset;
                    int extent;
                    int thumbLength;
                    int thumbOffset;
                    float maxThumbOffset;
                    float newThumbOffset;
                    if (this.mScrollCache.mScrollBarDraggingState == 1) {
                        bounds = this.mScrollCache.mScrollBarBounds;
                        getVerticalScrollBarBounds(bounds, null);
                        range = computeVerticalScrollRange();
                        offset = computeVerticalScrollOffset();
                        extent = computeVerticalScrollExtent();
                        thumbLength = ScrollBarUtils.getThumbLength(bounds.height(), bounds.width(), extent, range);
                        thumbOffset = ScrollBarUtils.getThumbOffset(bounds.height(), thumbLength, extent, range, offset);
                        maxThumbOffset = (float) (bounds.height() - thumbLength);
                        newThumbOffset = Math.min(Math.max(((float) thumbOffset) + (y - this.mScrollCache.mScrollBarDraggingPos), 0.0f), maxThumbOffset);
                        int height = getHeight();
                        if (Math.round(newThumbOffset) != thumbOffset && maxThumbOffset > 0.0f && height > 0 && extent > 0) {
                            int newY = Math.round((((float) (range - extent)) / (((float) extent) / ((float) height))) * (newThumbOffset / maxThumbOffset));
                            if (newY != getScrollY()) {
                                this.mScrollCache.mScrollBarDraggingPos = y;
                                setScrollY(newY);
                            }
                        }
                        return true;
                    } else if (this.mScrollCache.mScrollBarDraggingState == 2) {
                        bounds = this.mScrollCache.mScrollBarBounds;
                        getHorizontalScrollBarBounds(bounds, null);
                        range = computeHorizontalScrollRange();
                        offset = computeHorizontalScrollOffset();
                        extent = computeHorizontalScrollExtent();
                        thumbLength = ScrollBarUtils.getThumbLength(bounds.width(), bounds.height(), extent, range);
                        thumbOffset = ScrollBarUtils.getThumbOffset(bounds.width(), thumbLength, extent, range, offset);
                        maxThumbOffset = (float) (bounds.width() - thumbLength);
                        newThumbOffset = Math.min(Math.max(((float) thumbOffset) + (x - this.mScrollCache.mScrollBarDraggingPos), 0.0f), maxThumbOffset);
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
                return false;
            }
            if (isOnVerticalScrollbarThumb(x, y)) {
                this.mScrollCache.mScrollBarDraggingState = 1;
                this.mScrollCache.mScrollBarDraggingPos = y;
                return true;
            }
            if (isOnHorizontalScrollbarThumb(x, y)) {
                this.mScrollCache.mScrollBarDraggingState = 2;
                this.mScrollCache.mScrollBarDraggingPos = x;
                return true;
            }
            this.mScrollCache.mScrollBarDraggingState = 0;
            return false;
        }
        this.mScrollCache.mScrollBarDraggingState = 0;
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int viewFlags = this.mViewFlags;
        int action = event.getAction();
        boolean clickable = ((viewFlags & 16384) == 16384 || (2097152 & viewFlags) == 2097152) ? true : (8388608 & viewFlags) == 8388608;
        if ((viewFlags & 32) == 32) {
            if (action == 1 && (this.mPrivateFlags & 16384) != 0) {
                setPressed(false);
            }
            this.mPrivateFlags3 &= -131073;
            return clickable;
        } else if (this.mTouchDelegate != null && this.mTouchDelegate.onTouchEvent(event)) {
            return true;
        } else {
            if (!clickable && (1073741824 & viewFlags) != 1073741824) {
                return false;
            }
            switch (action) {
                case 0:
                    if (event.getSource() == 4098) {
                        this.mPrivateFlags3 |= 131072;
                    }
                    this.mHasPerformedLongPress = false;
                    if (clickable) {
                        if (!performButtonActionOnTouchDown(event)) {
                            if (!isInScrollingContainer()) {
                                setPressed(true, x, y);
                                checkForLongClick(0, x, y);
                                break;
                            }
                            this.mPrivateFlags |= 33554432;
                            if (this.mPendingCheckForTap == null) {
                                this.mPendingCheckForTap = new CheckForTap(this, null);
                            }
                            this.mPendingCheckForTap.x = event.getX();
                            this.mPendingCheckForTap.y = event.getY();
                            postDelayed(this.mPendingCheckForTap, (long) ViewConfiguration.getTapTimeout());
                            break;
                        }
                    }
                    checkForLongClick(0, x, y);
                    break;
                    break;
                case 1:
                    this.mPrivateFlags3 &= -131073;
                    if ((1073741824 & viewFlags) == 1073741824) {
                        handleTooltipUp();
                    }
                    if (!clickable) {
                        removeTapCallback();
                        removeLongPressCallback();
                        this.mInContextButtonPress = false;
                        this.mHasPerformedLongPress = false;
                        this.mIgnoreNextUpEvent = false;
                        break;
                    }
                    boolean prepressed = (this.mPrivateFlags & 33554432) != 0;
                    if ((this.mPrivateFlags & 16384) != 0 || prepressed) {
                        boolean focusTaken = false;
                        if (isFocusable() && isFocusableInTouchMode() && (isFocused() ^ 1) != 0) {
                            focusTaken = requestFocus();
                        }
                        if (prepressed) {
                            setPressed(true, x, y);
                        }
                        if (!(this.mHasPerformedLongPress || (this.mIgnoreNextUpEvent ^ 1) == 0)) {
                            removeLongPressCallback();
                            if (!focusTaken) {
                                if (this.mPerformClick == null) {
                                    this.mPerformClick = new PerformClick(this, null);
                                }
                                if (!post(this.mPerformClick)) {
                                    performClick();
                                }
                            }
                        }
                        if (this.mUnsetPressedState == null) {
                            this.mUnsetPressedState = new UnsetPressedState(this, null);
                        }
                        if (prepressed) {
                            postDelayed(this.mUnsetPressedState, (long) ViewConfiguration.getPressedStateDuration());
                        } else if (!post(this.mUnsetPressedState)) {
                            this.mUnsetPressedState.run();
                        }
                        removeTapCallback();
                    }
                    this.mIgnoreNextUpEvent = false;
                    break;
                case 2:
                    if (clickable) {
                        drawableHotspotChanged(x, y);
                    }
                    if (!pointInView(x, y, (float) this.mTouchSlop)) {
                        removeTapCallback();
                        removeLongPressCallback();
                        if ((this.mPrivateFlags & 16384) != 0) {
                            setPressed(false);
                        }
                        this.mPrivateFlags3 &= -131073;
                        break;
                    }
                    break;
                case 3:
                    if (clickable) {
                        setPressed(false);
                    }
                    removeTapCallback();
                    removeLongPressCallback();
                    this.mInContextButtonPress = false;
                    this.mHasPerformedLongPress = false;
                    this.mIgnoreNextUpEvent = false;
                    this.mPrivateFlags3 &= -131073;
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
        return false;
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
        if ((this.mPrivateFlags & 16384) != 0 && this.mUnsetPressedState != null) {
            setPressed(false);
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
            this.mSendViewScrolledAccessibilityEvent.mIsPending = false;
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
        if (this.mAttachInfo != null && ((action == 0 || action == 2) && (event.isTouchEvent() ^ 1) == 0)) {
            this.mAttachInfo.mUnbufferedDispatchRequested = true;
        }
    }

    void setFlags(int flags, int mask) {
        boolean accessibilityEnabled = AccessibilityManager.getInstance(this.mContext).isEnabled();
        boolean oldIncludeForAccessibility = accessibilityEnabled ? includeForAccessibility() : false;
        int old = this.mViewFlags;
        this.mViewFlags = (this.mViewFlags & (~mask)) | (flags & mask);
        int changed = this.mViewFlags ^ old;
        if (changed != 0) {
            int privateFlags = this.mPrivateFlags;
            int focusableChangedByAuto = 0;
            if (!((this.mViewFlags & 16) == 0 || (changed & 16401) == 0)) {
                int newFocus;
                if ((this.mViewFlags & 16384) != 0) {
                    newFocus = 1;
                } else {
                    newFocus = 0;
                }
                this.mViewFlags = (this.mViewFlags & -2) | newFocus;
                focusableChangedByAuto = (old & 1) ^ (newFocus & 1);
                changed = (changed & -2) | focusableChangedByAuto;
            }
            if (!((changed & 1) == 0 || (privateFlags & 16) == 0)) {
                if ((old & 1) == 1 && (privateFlags & 2) != 0) {
                    clearFocus();
                } else if ((old & 1) == 0 && (privateFlags & 2) == 0 && this.mParent != null) {
                    ViewRootImpl viewRootImpl = getViewRootImpl();
                    if (!sAutoFocusableOffUIThreadWontNotifyParents || focusableChangedByAuto == 0 || viewRootImpl == null || viewRootImpl.mThread == Thread.currentThread()) {
                        this.mParent.focusableViewAvailable(this);
                    }
                }
            }
            int newVisibility = flags & 12;
            if (newVisibility == 0 && (changed & 12) != 0) {
                this.mPrivateFlags |= 32;
                invalidate(true);
                needGlobalAttributesUpdate(true);
                if (this.mParent != null) {
                    this.mParent.focusableViewAvailable(this);
                }
            }
            if ((changed & 8) != 0) {
                needGlobalAttributesUpdate(false);
                requestLayout();
                if ((this.mViewFlags & 12) == 8) {
                    if (hasFocus()) {
                        clearFocus();
                    }
                    clearAccessibilityFocus();
                    destroyDrawingCache();
                    if (this.mParent instanceof View) {
                        ((View) this.mParent).invalidate(true);
                    }
                    this.mPrivateFlags |= 32;
                }
                if (this.mAttachInfo != null) {
                    this.mAttachInfo.mViewVisibilityChanged = true;
                }
            }
            if ((changed & 4) != 0) {
                needGlobalAttributesUpdate(false);
                this.mPrivateFlags |= 32;
                if ((this.mViewFlags & 12) == 4 && getRootView() != this) {
                    if (hasFocus()) {
                        clearFocus();
                    }
                    clearAccessibilityFocus();
                }
                if (this.mAttachInfo != null) {
                    this.mAttachInfo.mViewVisibilityChanged = true;
                }
            }
            if ((changed & 12) != 0) {
                if (!(newVisibility == 0 || this.mAttachInfo == null)) {
                    cleanupDraw();
                }
                if (this.mParent instanceof ViewGroup) {
                    ((ViewGroup) this.mParent).onChildVisibilityChanged(this, changed & 12, newVisibility);
                    ((View) this.mParent).invalidate(true);
                } else if (this.mParent != null) {
                    this.mParent.invalidateChild(this, null);
                }
                if (this.mAttachInfo != null) {
                    dispatchVisibilityChanged(this, newVisibility);
                    if (this.mParent != null && getWindowVisibility() == 0 && (!(this.mParent instanceof ViewGroup) || ((ViewGroup) this.mParent).isShown())) {
                        dispatchVisibilityAggregated(newVisibility == 0);
                    }
                    notifySubtreeAccessibilityStateChangedIfNeeded();
                }
            }
            if ((131072 & changed) != 0) {
                destroyDrawingCache();
            }
            if ((32768 & changed) != 0) {
                destroyDrawingCache();
                this.mPrivateFlags &= -32769;
                invalidateParentCaches();
            }
            if ((DRAWING_CACHE_QUALITY_MASK & changed) != 0) {
                destroyDrawingCache();
                this.mPrivateFlags &= -32769;
            }
            if ((changed & 128) != 0) {
                if ((this.mViewFlags & 128) == 0) {
                    this.mPrivateFlags &= -129;
                } else if (this.mBackground == null && this.mDefaultFocusHighlight == null && (this.mForegroundInfo == null || this.mForegroundInfo.mDrawable == null)) {
                    this.mPrivateFlags |= 128;
                } else {
                    this.mPrivateFlags &= -129;
                }
                requestLayout();
                invalidate(true);
            }
            if (!((67108864 & changed) == 0 || this.mParent == null || this.mAttachInfo == null || (this.mAttachInfo.mRecomputeGlobalAttributes ^ 1) == 0)) {
                this.mParent.recomputeViewAttributes(this);
            }
            if (accessibilityEnabled) {
                if ((changed & 1) == 0 && (changed & 12) == 0 && (changed & 16384) == 0 && (2097152 & changed) == 0 && (8388608 & changed) == 0) {
                    if ((changed & 32) != 0) {
                        notifyViewAccessibilityStateChangedIfNeeded(0);
                    }
                } else if (oldIncludeForAccessibility != includeForAccessibility()) {
                    notifySubtreeAccessibilityStateChangedIfNeeded();
                } else {
                    notifyViewAccessibilityStateChangedIfNeeded(0);
                }
            }
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
        this.mDefaultFocusHighlightSizeChanged = true;
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
        return this.mMeasuredWidth & 16777215;
    }

    @ExportedProperty(category = "measurement", flagMapping = {@FlagToString(equals = 16777216, mask = -16777216, name = "MEASURED_STATE_TOO_SMALL")})
    public final int getMeasuredWidthAndState() {
        return this.mMeasuredWidth;
    }

    public final int getMeasuredHeight() {
        return this.mMeasuredHeight & 16777215;
    }

    @ExportedProperty(category = "measurement", flagMapping = {@FlagToString(equals = 16777216, mask = -16777216, name = "MEASURED_STATE_TOO_SMALL")})
    public final int getMeasuredHeightAndState() {
        return this.mMeasuredHeight;
    }

    public final int getMeasuredState() {
        return (this.mMeasuredWidth & -16777216) | ((this.mMeasuredHeight >> 16) & InputDevice.SOURCE_ANY);
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
        invalidateViewProperty(true, false);
        this.mRenderNode.setCameraDistance((-Math.abs(distance)) / dpi);
        invalidateViewProperty(false, false);
        invalidateParentIfNeededAndWasQuickRejected();
    }

    @ExportedProperty(category = "drawing")
    public float getRotation() {
        return this.mRenderNode.getRotation();
    }

    public void setRotation(float rotation) {
        if (rotation != getRotation()) {
            invalidateViewProperty(true, false);
            this.mRenderNode.setRotation(rotation);
            invalidateViewProperty(false, true);
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
            invalidateViewProperty(true, false);
            this.mRenderNode.setRotationY(rotationY);
            invalidateViewProperty(false, true);
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
            invalidateViewProperty(true, false);
            this.mRenderNode.setRotationX(rotationX);
            invalidateViewProperty(false, true);
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
            invalidateViewProperty(true, false);
            this.mRenderNode.setScaleX(scaleX);
            invalidateViewProperty(false, true);
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
            invalidateViewProperty(true, false);
            this.mRenderNode.setScaleY(scaleY);
            invalidateViewProperty(false, true);
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
            invalidateViewProperty(true, false);
            this.mRenderNode.setPivotX(pivotX);
            invalidateViewProperty(false, true);
            invalidateParentIfNeededAndWasQuickRejected();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getPivotY() {
        return this.mRenderNode.getPivotY();
    }

    public void setPivotY(float pivotY) {
        if (!this.mRenderNode.isPivotExplicitlySet() || pivotY != getPivotY()) {
            invalidateViewProperty(true, false);
            this.mRenderNode.setPivotY(pivotY);
            invalidateViewProperty(false, true);
            invalidateParentIfNeededAndWasQuickRejected();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getAlpha() {
        return this.mTransformationInfo != null ? this.mTransformationInfo.mAlpha : 1.0f;
    }

    public void forceHasOverlappingRendering(boolean hasOverlappingRendering) {
        this.mPrivateFlags3 |= 16777216;
        if (hasOverlappingRendering) {
            this.mPrivateFlags3 |= 8388608;
        } else {
            this.mPrivateFlags3 &= -8388609;
        }
    }

    public final boolean getHasOverlappingRendering() {
        if ((this.mPrivateFlags3 & 16777216) == 0) {
            return hasOverlappingRendering();
        }
        if ((this.mPrivateFlags3 & 8388608) != 0) {
            return true;
        }
        return false;
    }

    @ExportedProperty(category = "drawing")
    public boolean hasOverlappingRendering() {
        return true;
    }

    public void setAlpha(float alpha) {
        ensureTransformationInfo();
        if (this.mTransformationInfo.mAlpha != alpha) {
            if (((alpha == 0.0f ? 1 : 0) ^ (this.mTransformationInfo.mAlpha == 0.0f ? 1 : 0)) != 0) {
                notifySubtreeAccessibilityStateChangedIfNeeded();
            }
            this.mTransformationInfo.mAlpha = alpha;
            if (onSetAlpha((int) (255.0f * alpha))) {
                this.mPrivateFlags |= 262144;
                invalidateParentCaches();
                invalidate(true);
                return;
            }
            this.mPrivateFlags &= -262145;
            invalidateViewProperty(true, false);
            this.mRenderNode.setAlpha(getFinalAlpha());
        }
    }

    boolean setAlphaNoInvalidation(float alpha) {
        ensureTransformationInfo();
        if (this.mTransformationInfo.mAlpha != alpha) {
            this.mTransformationInfo.mAlpha = alpha;
            if (onSetAlpha((int) (255.0f * alpha))) {
                this.mPrivateFlags |= 262144;
                return true;
            }
            this.mPrivateFlags &= -262145;
            this.mRenderNode.setAlpha(getFinalAlpha());
        }
        return false;
    }

    public void setTransitionAlpha(float alpha) {
        ensureTransformationInfo();
        if (this.mTransformationInfo.mTransitionAlpha != alpha) {
            this.mTransformationInfo.mTransitionAlpha = alpha;
            this.mPrivateFlags &= -262145;
            invalidateViewProperty(true, false);
            this.mRenderNode.setAlpha(getFinalAlpha());
        }
    }

    private float getFinalAlpha() {
        if (this.mTransformationInfo != null) {
            return this.mTransformationInfo.mAlpha * this.mTransformationInfo.mTransitionAlpha;
        }
        return 1.0f;
    }

    @ExportedProperty(category = "drawing")
    public float getTransitionAlpha() {
        return this.mTransformationInfo != null ? this.mTransformationInfo.mTransitionAlpha : 1.0f;
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
                    yLoc = 0;
                }
                invalidate(0, yLoc, this.mRight - this.mLeft, this.mBottom - minTop);
            }
            int width = this.mRight - this.mLeft;
            int oldHeight = this.mBottom - this.mTop;
            this.mTop = top;
            this.mRenderNode.setTop(this.mTop);
            sizeChange(width, this.mBottom - this.mTop, width, oldHeight);
            if (!matrixIsIdentity) {
                this.mPrivateFlags |= 32;
                invalidate(true);
            }
            this.mBackgroundSizeChanged = true;
            this.mDefaultFocusHighlightSizeChanged = true;
            if (this.mForegroundInfo != null) {
                this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & 268435456) == 268435456) {
                invalidateParentIfNeeded();
            }
        }
    }

    @CapturedViewProperty
    public final int getBottom() {
        return this.mBottom;
    }

    public boolean isDirty() {
        return (this.mPrivateFlags & PFLAG_DIRTY_MASK) != 0;
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
                invalidate(0, 0, this.mRight - this.mLeft, maxBottom - this.mTop);
            }
            int width = this.mRight - this.mLeft;
            int oldHeight = this.mBottom - this.mTop;
            this.mBottom = bottom;
            this.mRenderNode.setBottom(this.mBottom);
            sizeChange(width, this.mBottom - this.mTop, width, oldHeight);
            if (!matrixIsIdentity) {
                this.mPrivateFlags |= 32;
                invalidate(true);
            }
            this.mBackgroundSizeChanged = true;
            this.mDefaultFocusHighlightSizeChanged = true;
            if (this.mForegroundInfo != null) {
                this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & 268435456) == 268435456) {
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
                    xLoc = 0;
                }
                invalidate(xLoc, 0, this.mRight - minLeft, this.mBottom - this.mTop);
            }
            int oldWidth = this.mRight - this.mLeft;
            int height = this.mBottom - this.mTop;
            this.mLeft = left;
            this.mRenderNode.setLeft(left);
            sizeChange(this.mRight - this.mLeft, height, oldWidth, height);
            if (!matrixIsIdentity) {
                this.mPrivateFlags |= 32;
                invalidate(true);
            }
            this.mBackgroundSizeChanged = true;
            this.mDefaultFocusHighlightSizeChanged = true;
            if (this.mForegroundInfo != null) {
                this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & 268435456) == 268435456) {
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
                invalidate(0, 0, maxRight - this.mLeft, this.mBottom - this.mTop);
            }
            int oldWidth = this.mRight - this.mLeft;
            int height = this.mBottom - this.mTop;
            this.mRight = right;
            this.mRenderNode.setRight(this.mRight);
            sizeChange(this.mRight - this.mLeft, height, oldWidth, height);
            if (!matrixIsIdentity) {
                this.mPrivateFlags |= 32;
                invalidate(true);
            }
            this.mBackgroundSizeChanged = true;
            this.mDefaultFocusHighlightSizeChanged = true;
            if (this.mForegroundInfo != null) {
                this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & 268435456) == 268435456) {
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
            invalidateViewProperty(true, false);
            this.mRenderNode.setElevation(elevation);
            invalidateViewProperty(false, true);
            invalidateParentIfNeededAndWasQuickRejected();
        }
    }

    @ExportedProperty(category = "drawing")
    public float getTranslationX() {
        return this.mRenderNode.getTranslationX();
    }

    public void setTranslationX(float translationX) {
        if (translationX != getTranslationX()) {
            invalidateViewProperty(true, false);
            this.mRenderNode.setTranslationX(translationX);
            invalidateViewProperty(false, true);
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
            invalidateViewProperty(true, false);
            this.mRenderNode.setTranslationY(translationY);
            invalidateViewProperty(false, true);
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
            invalidateViewProperty(true, false);
            this.mRenderNode.setTranslationZ(translationZ);
            invalidateViewProperty(false, true);
            invalidateParentIfNeededAndWasQuickRejected();
        }
    }

    public void setAnimationMatrix(Matrix matrix) {
        invalidateViewProperty(true, false);
        this.mRenderNode.setAnimationMatrix(matrix);
        invalidateViewProperty(false, true);
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
            case 0:
                setOutlineProvider(ViewOutlineProvider.BACKGROUND);
                return;
            case 1:
                setOutlineProvider(null);
                return;
            case 2:
                setOutlineProvider(ViewOutlineProvider.BOUNDS);
                return;
            case 3:
                setOutlineProvider(ViewOutlineProvider.PADDED_BOUNDS);
                return;
            default:
                return;
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
        invalidateViewProperty(false, false);
    }

    private void rebuildOutline() {
        if (this.mAttachInfo != null) {
            if (this.mOutlineProvider == null) {
                this.mRenderNode.setOutline(null);
            } else {
                Outline outline = this.mAttachInfo.mTmpOutline;
                outline.setEmpty();
                outline.setAlpha(1.0f);
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
        invalidateViewProperty(false, false);
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
            return false;
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
            return false;
        }
        r.set(0, 0, width, height);
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
            return false;
        }
        r.offset(-offset.x, -offset.y);
        return true;
    }

    public void offsetTopAndBottom(int offset) {
        if (offset != 0) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidateViewProperty(false, false);
            } else if (isHardwareAccelerated()) {
                invalidateViewProperty(false, false);
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
                        yLoc = 0;
                    }
                    r.set(0, yLoc, this.mRight - this.mLeft, maxBottom - minTop);
                    p.invalidateChild(this, r);
                }
            }
            this.mTop += offset;
            this.mBottom += offset;
            this.mRenderNode.offsetTopAndBottom(offset);
            if (isHardwareAccelerated()) {
                invalidateViewProperty(false, false);
                invalidateParentIfNeededAndWasQuickRejected();
            } else {
                if (!matrixIsIdentity) {
                    invalidateViewProperty(false, true);
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
                invalidateViewProperty(false, false);
            } else if (isHardwareAccelerated()) {
                invalidateViewProperty(false, false);
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
                    r.set(0, 0, maxRight - minLeft, this.mBottom - this.mTop);
                    p.invalidateChild(this, r);
                }
            }
            this.mLeft += offset;
            this.mRight += offset;
            this.mRenderNode.offsetLeftAndRight(offset);
            if (isHardwareAccelerated()) {
                invalidateViewProperty(false, false);
                invalidateParentIfNeededAndWasQuickRejected();
            } else {
                if (!matrixIsIdentity) {
                    invalidateViewProperty(false, true);
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
        return false;
    }

    private boolean initialAwakenScrollBars() {
        if (this.mScrollCache != null) {
            return awakenScrollBars(this.mScrollCache.scrollBarDefaultDelayBeforeFade * 4, true);
        }
        return false;
    }

    protected boolean awakenScrollBars(int startDelay) {
        return awakenScrollBars(startDelay, true);
    }

    protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
        ScrollabilityCache scrollCache = this.mScrollCache;
        if (scrollCache == null || (scrollCache.fadeScrollBars ^ 1) != 0) {
            return false;
        }
        if (scrollCache.scrollBar == null) {
            scrollCache.scrollBar = new ScrollBarDrawable();
            scrollCache.scrollBar.setState(getDrawableState());
            scrollCache.scrollBar.setCallback(this);
        }
        if (!isHorizontalScrollBarEnabled() && !isVerticalScrollBarEnabled()) {
            return false;
        }
        if (invalidate) {
            postInvalidateOnAnimation();
        }
        if (scrollCache.state == 0) {
            startDelay = Math.max(MetricsEvent.SETTINGS_LANGUAGE_CATEGORY, startDelay);
        }
        long fadeStartTime = AnimationUtils.currentAnimationTimeMillis() + ((long) startDelay);
        scrollCache.fadeStartTime = fadeStartTime;
        scrollCache.state = 1;
        if (this.mAttachInfo != null) {
            this.mAttachInfo.mHandler.removeCallbacks(scrollCache);
            this.mAttachInfo.mHandler.postAtTime(scrollCache, fadeStartTime);
        }
        return true;
    }

    private boolean skipInvalidate() {
        boolean z = true;
        if (HwFrameworkFactory.getHwNsdImpl().checkAdBlock(this, getContext().getPackageName())) {
            return true;
        }
        if ((this.mViewFlags & 12) == 0 || this.mCurrentAnimation != null) {
            z = false;
        } else if (this.mParent instanceof ViewGroup) {
            z = ((ViewGroup) this.mParent).isViewTransitioning(this) ^ 1;
        }
        return z;
    }

    public void invalidate(Rect dirty) {
        int scrollX = this.mScrollX;
        int scrollY = this.mScrollY;
        invalidateInternal(dirty.left - scrollX, dirty.top - scrollY, dirty.right - scrollX, dirty.bottom - scrollY, true, false);
    }

    public void invalidate(int l, int t, int r, int b) {
        int scrollX = this.mScrollX;
        int scrollY = this.mScrollY;
        invalidateInternal(l - scrollX, t - scrollY, r - scrollX, b - scrollY, true, false);
    }

    public void invalidate() {
        if (this.mAttachInfo == null || !ViewRootImpl.sSLBSwitch || !HwFrameworkFactory.getHwNsdImpl().doProcessDrawSLB(this.mAttachInfo.mDrawingTime, this.mAttachInfo.mViewScrollChanged, this.mAttachInfo.mHandlingPointerEvent)) {
            invalidate(true);
        }
    }

    public void invalidate(boolean invalidateCache) {
        invalidateInternal(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop, invalidateCache, true);
    }

    private boolean checkIsAdView() {
        if (1 == this.mViewCheckedResult) {
            return false;
        }
        if (this.mViewCheckedResult == 0) {
            setVisibility(8);
            return true;
        }
        this.mViewCheckedResult = HwFrameworkFactory.getHwAdCleaner().checkAdCleaner(this, getContext().getPackageName(), 0);
        if (this.mViewCheckedResult != 0) {
            return false;
        }
        setVisibility(8);
        return true;
    }

    void invalidateInternal(int l, int t, int r, int b, boolean invalidateCache, boolean fullInvalidate) {
        if (this.mGhostView != null) {
            this.mGhostView.invalidate(true);
        } else if (!checkIsAdView() && !skipInvalidate()) {
            if ((this.mPrivateFlags & 48) == 48 || ((invalidateCache && (this.mPrivateFlags & 32768) == 32768) || (this.mPrivateFlags & Integer.MIN_VALUE) != Integer.MIN_VALUE || (fullInvalidate && isOpaque() != this.mLastIsOpaque))) {
                if (fullInvalidate) {
                    this.mLastIsOpaque = isOpaque();
                    this.mPrivateFlags &= -33;
                }
                this.mPrivateFlags |= 2097152;
                if (invalidateCache) {
                    this.mPrivateFlags |= Integer.MIN_VALUE;
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
        return this.mBackground != null;
    }

    void invalidateViewProperty(boolean invalidateParent, boolean forceRedraw) {
        if (isHardwareAccelerated() && (this.mRenderNode.isValid() ^ 1) == 0 && (this.mPrivateFlags & 64) == 0) {
            damageInParent();
            return;
        }
        if (invalidateParent) {
            invalidateParentCaches();
        }
        if (forceRedraw) {
            this.mPrivateFlags |= 32;
        }
        invalidate(false);
    }

    protected void damageInParent() {
        if (this.mParent != null && this.mAttachInfo != null) {
            this.mParent.onDescendantInvalidated(this, this);
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
            view.mPrivateFlags |= Integer.MIN_VALUE;
        }
    }

    protected void invalidateParentIfNeeded() {
        if (isHardwareAccelerated() && (this.mParent instanceof View)) {
            ((View) this.mParent).invalidate(true);
        }
    }

    protected void invalidateParentIfNeededAndWasQuickRejected() {
        if ((this.mPrivateFlags2 & 268435456) != 0) {
            invalidateParentIfNeeded();
        }
    }

    @ExportedProperty(category = "drawing")
    public boolean isOpaque() {
        if ((this.mPrivateFlags & 25165824) != 25165824 || getFinalAlpha() < 1.0f) {
            return false;
        }
        return true;
    }

    protected void computeOpaqueFlags() {
        if (this.mBackground == null || this.mBackground.getOpacity() != -1) {
            this.mPrivateFlags &= -8388609;
        } else {
            this.mPrivateFlags |= 8388608;
        }
        int flags = this.mViewFlags;
        if (((flags & 512) == 0 && (flags & 256) == 0) || (flags & 50331648) == 0 || (flags & 50331648) == 33554432) {
            this.mPrivateFlags |= 16777216;
        } else {
            this.mPrivateFlags &= -16777217;
        }
    }

    protected boolean hasOpaqueScrollbars() {
        return (this.mPrivateFlags & 16777216) == 16777216;
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

    public ThreadedRenderer getThreadedRenderer() {
        return this.mAttachInfo != null ? this.mAttachInfo.mThreadedRenderer : null;
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
            attachInfo.mViewRootImpl.mChoreographer.postCallback(1, action, null);
        } else {
            getRunQueue().post(action);
        }
    }

    public void postOnAnimationDelayed(Runnable action, long delayMillis) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            attachInfo.mViewRootImpl.mChoreographer.postCallbackDelayed(1, action, null, delayMillis);
        } else {
            getRunQueue().postDelayed(action, delayMillis);
        }
    }

    public boolean removeCallbacks(Runnable action) {
        if (action != null) {
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo != null) {
                attachInfo.mHandler.removeCallbacks(action);
                attachInfo.mViewRootImpl.mChoreographer.removeCallbacks(1, action, null);
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
        if (attachInfo != null && (!ViewRootImpl.sSLBSwitch || !HwFrameworkFactory.getHwNsdImpl().doProcessDrawSLB(this.mAttachInfo.mDrawingTime, this.mAttachInfo.mViewScrollChanged, this.mAttachInfo.mHandlingPointerEvent))) {
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
            this.mSendViewScrolledAccessibilityEvent = new SendViewScrolledAccessibilityEvent(this, null);
        }
        if (!this.mSendViewScrolledAccessibilityEvent.mIsPending) {
            this.mSendViewScrolledAccessibilityEvent.mIsPending = true;
            postDelayed(this.mSendViewScrolledAccessibilityEvent, ViewConfiguration.getSendRecurringAccessibilityEventsInterval());
        }
    }

    public void computeScroll() {
    }

    public boolean isHorizontalFadingEdgeEnabled() {
        return (this.mViewFlags & 4096) == 4096;
    }

    public void setHorizontalFadingEdgeEnabled(boolean horizontalFadingEdgeEnabled) {
        if (isHorizontalFadingEdgeEnabled() != horizontalFadingEdgeEnabled) {
            if (horizontalFadingEdgeEnabled) {
                initScrollCache();
            }
            this.mViewFlags ^= 4096;
        }
    }

    public boolean isVerticalFadingEdgeEnabled() {
        return (this.mViewFlags & 8192) == 8192;
    }

    public void setVerticalFadingEdgeEnabled(boolean verticalFadingEdgeEnabled) {
        if (isVerticalFadingEdgeEnabled() != verticalFadingEdgeEnabled) {
            if (verticalFadingEdgeEnabled) {
                initScrollCache();
            }
            this.mViewFlags ^= 8192;
        }
    }

    protected float getTopFadingEdgeStrength() {
        return computeVerticalScrollOffset() > 0 ? 1.0f : 0.0f;
    }

    protected float getBottomFadingEdgeStrength() {
        return computeVerticalScrollOffset() + computeVerticalScrollExtent() < computeVerticalScrollRange() ? 1.0f : 0.0f;
    }

    protected float getLeftFadingEdgeStrength() {
        return computeHorizontalScrollOffset() > 0 ? 1.0f : 0.0f;
    }

    protected float getRightFadingEdgeStrength() {
        return computeHorizontalScrollOffset() + computeHorizontalScrollExtent() < computeHorizontalScrollRange() ? 1.0f : 0.0f;
    }

    public boolean isHorizontalScrollBarEnabled() {
        return (this.mViewFlags & 256) == 256;
    }

    public void setHorizontalScrollBarEnabled(boolean horizontalScrollBarEnabled) {
        if (isHorizontalScrollBarEnabled() != horizontalScrollBarEnabled) {
            this.mViewFlags ^= 256;
            computeOpaqueFlags();
            resolvePadding();
        }
    }

    public boolean isVerticalScrollBarEnabled() {
        return (this.mViewFlags & 512) == 512;
    }

    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
        if (isVerticalScrollBarEnabled() != verticalScrollBarEnabled) {
            this.mViewFlags ^= 512;
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
            scrollabilityCache.state = 0;
        } else {
            scrollabilityCache.state = 1;
        }
    }

    public boolean isScrollbarFadingEnabled() {
        return this.mScrollCache != null ? this.mScrollCache.fadeScrollBars : false;
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
        if (style != (this.mViewFlags & 50331648)) {
            this.mViewFlags = (this.mViewFlags & -50331649) | (style & 50331648);
            computeOpaqueFlags();
            resolvePadding();
        }
    }

    @ExportedProperty(mapping = {@IntToString(from = 0, to = "INSIDE_OVERLAY"), @IntToString(from = 16777216, to = "INSIDE_INSET"), @IntToString(from = 33554432, to = "OUTSIDE_OVERLAY"), @IntToString(from = 50331648, to = "OUTSIDE_INSET")})
    public int getScrollBarStyle() {
        return this.mViewFlags & 50331648;
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
            return false;
        }
        if (direction < 0) {
            if (offset <= 0) {
                z = false;
            }
            return z;
        }
        if (offset >= range - 1) {
            z = false;
        }
        return z;
    }

    public boolean canScrollVertically(int direction) {
        boolean z = true;
        int offset = computeVerticalScrollOffset();
        int range = computeVerticalScrollRange() - computeVerticalScrollExtent();
        if (range == 0) {
            return false;
        }
        if (direction < 0) {
            if (offset <= 0) {
                z = false;
            }
            return z;
        }
        if (offset >= range - 1) {
            z = false;
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
                int[] xy = new int[2];
                if (reSizeScrollIndicators(xy)) {
                    h = xy[0];
                    w = xy[1];
                }
                Rect rect = this.mAttachInfo.mTmpInvalRect;
                getScrollIndicatorBounds(rect);
                if ((this.mPrivateFlags3 & 256) != 0 && canScrollVertically(-1)) {
                    dr.setBounds(rect.left, rect.top, rect.right, rect.top + h);
                    dr.draw(c);
                }
                if ((this.mPrivateFlags3 & 512) != 0 && canScrollVertically(1)) {
                    dr.setBounds(rect.left, rect.bottom - h, rect.right, rect.bottom);
                    dr.draw(c);
                }
                if (getLayoutDirection() == 1) {
                    leftRtl = 8192;
                    rightRtl = 4096;
                } else {
                    leftRtl = 4096;
                    rightRtl = 8192;
                }
                if ((this.mPrivateFlags3 & (leftRtl | 1024)) != 0 && canScrollHorizontally(-1)) {
                    dr.setBounds(rect.left, rect.top, rect.left + w, rect.bottom);
                    dr.draw(c);
                }
                if ((this.mPrivateFlags3 & (rightRtl | 2048)) != 0 && canScrollHorizontally(1)) {
                    dr.setBounds(rect.right - w, rect.top, rect.right, rect.bottom);
                    dr.draw(c);
                }
            }
        }
    }

    private void getHorizontalScrollBarBounds(Rect drawBounds, Rect touchBounds) {
        Rect bounds = drawBounds != null ? drawBounds : touchBounds;
        if (bounds != null) {
            int drawVerticalScrollBar;
            int inside = (this.mViewFlags & 33554432) == 0 ? -1 : 0;
            if (isVerticalScrollBarEnabled()) {
                drawVerticalScrollBar = isVerticalScrollBarHidden() ^ 1;
            } else {
                drawVerticalScrollBar = 0;
            }
            int size = getHorizontalScrollbarHeight();
            int verticalScrollBarGap = drawVerticalScrollBar != 0 ? getVerticalScrollbarWidth() : 0;
            int width = this.mRight - this.mLeft;
            int height = this.mBottom - this.mTop;
            bounds.top = ((this.mScrollY + height) - size) - (this.mUserPaddingBottom & inside);
            bounds.left = this.mScrollX + (this.mPaddingLeft & inside);
            bounds.right = ((this.mScrollX + width) - (this.mUserPaddingRight & inside)) - verticalScrollBarGap;
            bounds.bottom = bounds.top + size;
            if (touchBounds != null) {
                if (touchBounds != bounds) {
                    touchBounds.set(bounds);
                }
                int minTouchTarget = this.mScrollCache.scrollBarMinTouchTarget;
                if (touchBounds.height() < minTouchTarget) {
                    touchBounds.bottom = Math.min(touchBounds.bottom + ((minTouchTarget - touchBounds.height()) / 2), this.mScrollY + height);
                    touchBounds.top = touchBounds.bottom - minTouchTarget;
                }
                if (touchBounds.width() < minTouchTarget) {
                    touchBounds.left -= (minTouchTarget - touchBounds.width()) / 2;
                    touchBounds.right = touchBounds.left + minTouchTarget;
                }
            }
        }
    }

    private void getVerticalScrollBarBounds(Rect bounds, Rect touchBounds) {
        if (this.mRoundScrollbarRenderer == null) {
            getStraightVerticalScrollBarBounds(bounds, touchBounds);
            return;
        }
        if (bounds == null) {
            bounds = touchBounds;
        }
        getRoundVerticalScrollBarBounds(bounds);
    }

    private void getRoundVerticalScrollBarBounds(Rect bounds) {
        int width = this.mRight - this.mLeft;
        int height = this.mBottom - this.mTop;
        bounds.left = this.mScrollX;
        bounds.top = this.mScrollY;
        bounds.right = bounds.left + width;
        bounds.bottom = this.mScrollY + height;
    }

    private void getStraightVerticalScrollBarBounds(Rect drawBounds, Rect touchBounds) {
        Rect bounds = drawBounds != null ? drawBounds : touchBounds;
        if (bounds != null) {
            int inside = (this.mViewFlags & 33554432) == 0 ? -1 : 0;
            int size = getVerticalScrollbarWidth();
            int verticalScrollbarPosition = this.mVerticalScrollbarPosition;
            if (verticalScrollbarPosition == 0) {
                verticalScrollbarPosition = isRtlLocale() ? 1 : 2;
            }
            int width = this.mRight - this.mLeft;
            int height = this.mBottom - this.mTop;
            switch (verticalScrollbarPosition) {
                case 1:
                    bounds.left = this.mScrollX + (this.mUserPaddingLeft & inside);
                    break;
                default:
                    bounds.left = ((this.mScrollX + width) - size) - (this.mUserPaddingRight & inside);
                    break;
            }
            bounds.top = this.mScrollY + (this.mPaddingTop & inside);
            bounds.right = bounds.left + size;
            bounds.bottom = (this.mScrollY + height) - (this.mUserPaddingBottom & inside);
            if (touchBounds != null) {
                if (touchBounds != bounds) {
                    touchBounds.set(bounds);
                }
                int minTouchTarget = this.mScrollCache.scrollBarMinTouchTarget;
                if (touchBounds.width() < minTouchTarget) {
                    int adjust = (minTouchTarget - touchBounds.width()) / 2;
                    if (verticalScrollbarPosition == 2) {
                        touchBounds.right = Math.min(touchBounds.right + adjust, this.mScrollX + width);
                        touchBounds.left = touchBounds.right - minTouchTarget;
                    } else {
                        touchBounds.left = Math.max(touchBounds.left + adjust, this.mScrollX);
                        touchBounds.right = touchBounds.left + minTouchTarget;
                    }
                }
                if (touchBounds.height() < minTouchTarget) {
                    touchBounds.top -= (minTouchTarget - touchBounds.height()) / 2;
                    touchBounds.bottom = touchBounds.top + minTouchTarget;
                }
            }
        }
    }

    protected final void onDrawScrollBars(Canvas canvas) {
        ScrollabilityCache cache = this.mScrollCache;
        if (cache != null) {
            int state = cache.state;
            if (state != 0) {
                int drawVerticalScrollBar;
                boolean invalidate = false;
                if (state == 2) {
                    if (cache.interpolatorValues == null) {
                        cache.interpolatorValues = new float[1];
                    }
                    float[] values = cache.interpolatorValues;
                    if (cache.scrollBarInterpolator.timeToValues(values) == Result.FREEZE_END) {
                        cache.state = 0;
                    } else {
                        cache.scrollBar.mutate().setAlpha(Math.round(values[0]));
                    }
                    invalidate = true;
                } else {
                    cache.scrollBar.mutate().setAlpha(255);
                }
                boolean drawHorizontalScrollBar = isHorizontalScrollBarEnabled();
                if (isVerticalScrollBarEnabled()) {
                    drawVerticalScrollBar = isVerticalScrollBarHidden() ^ 1;
                } else {
                    drawVerticalScrollBar = 0;
                }
                Rect bounds;
                if (this.mRoundScrollbarRenderer != null) {
                    if (drawVerticalScrollBar != 0) {
                        bounds = cache.mScrollBarBounds;
                        getVerticalScrollBarBounds(bounds, null);
                        this.mRoundScrollbarRenderer.drawRoundScrollbars(canvas, ((float) cache.scrollBar.getAlpha()) / 255.0f, bounds);
                        if (invalidate) {
                            invalidate();
                        }
                    }
                } else if (drawVerticalScrollBar != 0 || drawHorizontalScrollBar) {
                    ScrollBarDrawable scrollBar = cache.scrollBar;
                    if (drawHorizontalScrollBar) {
                        scrollBar.setParameters(computeHorizontalScrollRange(), computeHorizontalScrollOffset(), computeHorizontalScrollExtent(), false);
                        bounds = cache.mScrollBarBounds;
                        getHorizontalScrollBarBounds(bounds, null);
                        onDrawHorizontalScrollBar(canvas, scrollBar, bounds.left, bounds.top, bounds.right, bounds.bottom);
                        if (invalidate) {
                            invalidate(bounds);
                        }
                    }
                    if (drawVerticalScrollBar != 0) {
                        scrollBar.setParameters(computeVerticalScrollRange(), computeVerticalScrollOffset(), computeVerticalScrollExtent(), true);
                        bounds = cache.mScrollBarBounds;
                        getVerticalScrollBarBounds(bounds, null);
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
        return false;
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
        if ((this.mPrivateFlags & 512) != 0) {
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
            return false;
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

    void dispatchMovedToDisplay(Display display, Configuration config) {
        this.mAttachInfo.mDisplay = display;
        this.mAttachInfo.mDisplayState = display.getState();
        onMovedToDisplay(display.getDisplayId(), config);
    }

    public void onMovedToDisplay(int displayId, Configuration config) {
    }

    private boolean hasRtlSupport() {
        return this.mContext.getApplicationInfo().hasRtlSupport();
    }

    private boolean isRtlCompatibilityMode() {
        return getContext().getApplicationInfo().targetSdkVersion >= 17 ? hasRtlSupport() ^ 1 : true;
    }

    private boolean needRtlPropertiesResolution() {
        return (this.mPrivateFlags2 & ALL_RTL_PROPERTIES_RESOLVED) != ALL_RTL_PROPERTIES_RESOLVED;
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
    }

    public boolean resolveLayoutDirection() {
        this.mPrivateFlags2 &= -49;
        if (hasRtlSupport()) {
            switch ((this.mPrivateFlags2 & 12) >> 2) {
                case 1:
                    this.mPrivateFlags2 |= 16;
                    break;
                case 2:
                    if (canResolveLayoutDirection()) {
                        try {
                            if (this.mParent.isLayoutDirectionResolved()) {
                                if (this.mParent.getLayoutDirection() == 1) {
                                    this.mPrivateFlags2 |= 16;
                                    break;
                                }
                            }
                            return false;
                        } catch (AbstractMethodError e) {
                            Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                            break;
                        }
                    }
                    return false;
                    break;
                case 3:
                    if (1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
                        this.mPrivateFlags2 |= 16;
                        break;
                    }
                    break;
            }
        } else if (this.mForceRTL) {
            this.mPrivateFlags2 |= 16;
        }
        this.mPrivateFlags2 |= 32;
        return true;
    }

    public boolean canResolveLayoutDirection() {
        switch (getRawLayoutDirection()) {
            case 2:
                if (this.mParent != null) {
                    try {
                        return this.mParent.canResolveLayoutDirection();
                    } catch (AbstractMethodError e) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                    }
                }
                return false;
            default:
                return true;
        }
    }

    public void resetResolvedLayoutDirection() {
        this.mPrivateFlags2 &= -49;
    }

    public boolean isLayoutDirectionInherited() {
        return getRawLayoutDirection() == 2;
    }

    public boolean isLayoutDirectionResolved() {
        return (this.mPrivateFlags2 & 32) == 32;
    }

    boolean isPaddingResolved() {
        return (this.mPrivateFlags2 & 536870912) == 536870912;
    }

    public void resolvePadding() {
        int resolvedLayoutDirection = getLayoutDirection();
        if (!isRtlCompatibilityMode()) {
            if (!(this.mBackground == null || (this.mLeftPaddingDefined && (this.mRightPaddingDefined ^ 1) == 0))) {
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
                case 1:
                    if (this.mUserPaddingStart != Integer.MIN_VALUE) {
                        this.mUserPaddingRight = this.mUserPaddingStart;
                    } else {
                        this.mUserPaddingRight = this.mUserPaddingRightInitial;
                    }
                    if (this.mUserPaddingEnd == Integer.MIN_VALUE) {
                        this.mUserPaddingLeft = this.mUserPaddingLeftInitial;
                        break;
                    } else {
                        this.mUserPaddingLeft = this.mUserPaddingEnd;
                        break;
                    }
                default:
                    if (this.mUserPaddingStart != Integer.MIN_VALUE) {
                        this.mUserPaddingLeft = this.mUserPaddingStart;
                    } else {
                        this.mUserPaddingLeft = this.mUserPaddingLeftInitial;
                    }
                    if (this.mUserPaddingEnd == Integer.MIN_VALUE) {
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
        this.mPrivateFlags2 |= 536870912;
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
        if ((this.mViewFlags & 1073741824) == 1073741824) {
            -android_view_View-mthref-1();
        }
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
        this.mAttachInfo = info;
        if (this.mOverlay != null) {
            this.mOverlay.getOverlayView().dispatchAttachedToWindow(info, visibility);
        }
        this.mWindowAttachCount++;
        this.mPrivateFlags |= 1024;
        if (this.mFloatingTreeObserver != null) {
            info.mTreeObserver.merge(this.mFloatingTreeObserver);
            this.mFloatingTreeObserver = null;
        }
        registerPendingFrameMetricsObservers();
        if ((this.mPrivateFlags & 524288) != 0) {
            this.mAttachInfo.mScrollContainers.add(this);
            this.mPrivateFlags |= 1048576;
        }
        if (this.mRunQueue != null) {
            this.mRunQueue.executeActions(info.mHandler);
            this.mRunQueue = null;
        }
        performCollectViewAttributes(this.mAttachInfo, visibility);
        onAttachedToWindow();
        ListenerInfo li = this.mListenerInfo;
        Iterable<OnAttachStateChangeListener> listeners = li != null ? li.mOnAttachStateChangeListeners : null;
        if (listeners != null && listeners.size() > 0) {
            for (OnAttachStateChangeListener listener : listeners) {
                listener.onViewAttachedToWindow(this);
            }
        }
        int vis = info.mWindowVisibility;
        if (vis != 8) {
            onWindowVisibilityChanged(vis);
            if (isShown()) {
                onVisibilityAggregated(vis == 0);
            }
        }
        onVisibilityChanged(this, visibility);
        if ((this.mPrivateFlags & 1024) != 0) {
            refreshDrawableState();
        }
        needGlobalAttributesUpdate(false);
        notifyEnterOrExitForAutoFillIfNeeded(true);
    }

    void dispatchDetachedFromWindow() {
        AttachInfo info = this.mAttachInfo;
        if (!(info == null || info.mWindowVisibility == 8)) {
            onWindowVisibilityChanged(8);
            if (isShown()) {
                onVisibilityAggregated(false);
            }
        }
        onDetachedFromWindow();
        onDetachedFromWindowInternal();
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            imm.onViewDetachedFromWindow(this);
        }
        ListenerInfo li = this.mListenerInfo;
        Iterable<OnAttachStateChangeListener> listeners = li != null ? li.mOnAttachStateChangeListeners : null;
        if (listeners != null && listeners.size() > 0) {
            for (OnAttachStateChangeListener listener : listeners) {
                listener.onViewDetachedFromWindow(this);
            }
        }
        if ((this.mPrivateFlags & 1048576) != 0) {
            this.mAttachInfo.mScrollContainers.remove(this);
            this.mPrivateFlags &= -1048577;
        }
        this.mAttachInfo = null;
        if (this.mOverlay != null) {
            this.mOverlay.getOverlayView().dispatchDetachedFromWindow();
        }
        notifyEnterOrExitForAutoFillIfNeeded(false);
    }

    public final void cancelPendingInputEvents() {
        dispatchCancelPendingInputEvents();
    }

    void dispatchCancelPendingInputEvents() {
        this.mPrivateFlags3 &= -17;
        onCancelPendingInputEvents();
        if ((this.mPrivateFlags3 & 16) != 16) {
            throw new SuperNotCalledException("View " + getClass().getSimpleName() + " did not call through to super.onCancelPendingInputEvents()");
        }
    }

    public void onCancelPendingInputEvents() {
        removePerformClickCallback();
        cancelLongPress();
        this.mPrivateFlags3 |= 16;
    }

    public void saveHierarchyState(SparseArray<Parcelable> container) {
        dispatchSaveInstanceState(container);
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        if (this.mID != -1 && (this.mViewFlags & 65536) == 0) {
            this.mPrivateFlags &= -131073;
            Parcelable state = onSaveInstanceState();
            if ((this.mPrivateFlags & 131072) == 0) {
                throw new IllegalStateException("Derived class did not call super.onSaveInstanceState()");
            } else if (state != null) {
                container.put(this.mID, state);
            }
        }
    }

    protected Parcelable onSaveInstanceState() {
        this.mPrivateFlags |= 131072;
        if (this.mStartActivityRequestWho == null && !isAutofilled() && this.mAutofillViewId <= LAST_APP_AUTOFILL_ID) {
            return BaseSavedState.EMPTY_STATE;
        }
        BaseSavedState state = new BaseSavedState(AbsSavedState.EMPTY_STATE);
        if (this.mStartActivityRequestWho != null) {
            state.mSavedData |= 1;
        }
        if (isAutofilled()) {
            state.mSavedData |= 2;
        }
        if (this.mAutofillViewId > LAST_APP_AUTOFILL_ID) {
            state.mSavedData |= 4;
        }
        state.mStartActivityRequestWhoSaved = this.mStartActivityRequestWho;
        state.mIsAutofilled = isAutofilled();
        state.mAutofillViewId = this.mAutofillViewId;
        return state;
    }

    public void restoreHierarchyState(SparseArray<Parcelable> container) {
        dispatchRestoreInstanceState(container);
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        if (this.mID != -1) {
            Parcelable state = (Parcelable) container.get(this.mID);
            if (state != null) {
                this.mPrivateFlags &= -131073;
                onRestoreInstanceState(state);
                if ((this.mPrivateFlags & 131072) == 0) {
                    throw new IllegalStateException("Derived class did not call super.onRestoreInstanceState()");
                }
            }
        }
    }

    protected void onRestoreInstanceState(Parcelable state) {
        this.mPrivateFlags |= 131072;
        if (state != null && ((state instanceof AbsSavedState) ^ 1) != 0) {
            throw new IllegalArgumentException("Wrong state class, expecting View State but received " + state.getClass().toString() + " instead. This usually happens " + "when two views of different type have the same id in the same hierarchy. " + "This view's id is " + ViewDebug.resolveId(this.mContext, getId()) + ". Make sure " + "other views do not use the same id.");
        } else if (state != null && (state instanceof BaseSavedState)) {
            BaseSavedState baseState = (BaseSavedState) state;
            if ((baseState.mSavedData & 1) != 0) {
                this.mStartActivityRequestWho = baseState.mStartActivityRequestWhoSaved;
            }
            if ((baseState.mSavedData & 2) != 0) {
                setAutofilled(baseState.mIsAutofilled);
            }
            if ((baseState.mSavedData & 4) != 0) {
                this.mAutofillViewId = baseState.mAutofillViewId;
            }
        }
    }

    public long getDrawingTime() {
        return this.mAttachInfo != null ? this.mAttachInfo.mDrawingTime : 0;
    }

    public void setDuplicateParentStateEnabled(boolean enabled) {
        setFlags(enabled ? 4194304 : 0, 4194304);
    }

    public boolean isDuplicateParentStateEnabled() {
        return (this.mViewFlags & 4194304) == 4194304;
    }

    public void setLayerType(int layerType, Paint paint) {
        if (layerType < 0 || layerType > 2) {
            throw new IllegalArgumentException("Layer type can only be one of: LAYER_TYPE_NONE, LAYER_TYPE_SOFTWARE or LAYER_TYPE_HARDWARE");
        } else if (this.mRenderNode.setLayerType(layerType)) {
            if (layerType != 1) {
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
            if (layerType != 2) {
                invalidate();
            } else if (this.mRenderNode.setLayerPaint(paint)) {
                invalidateViewProperty(false, false);
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
                    case 1:
                        buildDrawingCache(true);
                        break;
                    case 2:
                        updateDisplayListIfDirty();
                        if (attachInfo.mThreadedRenderer != null && this.mRenderNode.isValid()) {
                            attachInfo.mThreadedRenderer.buildLayer(this.mRenderNode);
                            break;
                        }
                }
            }
        }
    }

    protected void destroyHardwareResources() {
        if (this.mOverlay != null) {
            this.mOverlay.getOverlayView().destroyHardwareResources();
        }
        if (this.mGhostView != null) {
            this.mGhostView.destroyHardwareResources();
        }
    }

    public void setDrawingCacheEnabled(boolean enabled) {
        int i = 0;
        this.mCachingFailed = false;
        if (enabled) {
            i = 32768;
        }
        setFlags(i, 32768);
    }

    @ExportedProperty(category = "drawing")
    public boolean isDrawingCacheEnabled() {
        return (this.mViewFlags & 32768) == 32768;
    }

    public void outputDirtyFlags(String indent, boolean clear, int clearMask) {
        Log.d(VIEW_LOG_TAG, indent + this + "             DIRTY(" + (this.mPrivateFlags & PFLAG_DIRTY_MASK) + ") DRAWN(" + (this.mPrivateFlags & 32) + ")" + " CACHE_VALID(" + (this.mPrivateFlags & 32768) + ") INVALIDATED(" + (this.mPrivateFlags & Integer.MIN_VALUE) + ")");
        if (clear) {
            this.mPrivateFlags &= clearMask;
        }
        if (this instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) this;
            int count = parent.getChildCount();
            for (int i = 0; i < count; i++) {
                parent.getChildAt(i).outputDirtyFlags(indent + "  ", clear, clearMask);
            }
        }
    }

    protected void dispatchGetDisplayList() {
    }

    public boolean canHaveDisplayList() {
        return (this.mAttachInfo == null || this.mAttachInfo.mThreadedRenderer == null) ? false : true;
    }

    public RenderNode updateDisplayListIfDirty() {
        RenderNode renderNode = this.mRenderNode;
        if (!canHaveDisplayList()) {
            return renderNode;
        }
        if ((this.mPrivateFlags & 32768) != 0 && (renderNode.isValid() ^ 1) == 0 && !this.mRecreateDisplayList) {
            this.mPrivateFlags |= 32800;
            this.mPrivateFlags &= -6291457;
        } else if (!renderNode.isValid() || (this.mRecreateDisplayList ^ 1) == 0) {
            this.mRecreateDisplayList = true;
            int width = this.mRight - this.mLeft;
            int height = this.mBottom - this.mTop;
            int layerType = getLayerType();
            DisplayListCanvas canvas = renderNode.start(width, height);
            canvas.setHighContrastText(this.mAttachInfo.mHighContrastText);
            if (layerType == 1) {
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
                if ((this.mPrivateFlags & 128) == 128) {
                    dispatchDraw(canvas);
                    drawAutofilledHighlight(canvas);
                    if (!(this.mOverlay == null || (this.mOverlay.isEmpty() ^ 1) == 0)) {
                        this.mOverlay.getOverlayView().draw(canvas);
                    }
                    if (debugDraw()) {
                        debugDrawFocus(canvas);
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
        this.mRenderNode.discardDisplayList();
        if (this.mBackgroundRenderNode != null) {
            this.mBackgroundRenderNode.discardDisplayList();
        }
    }

    public Bitmap getDrawingCache() {
        return getDrawingCache(false);
    }

    public Bitmap getDrawingCache(boolean autoScale) {
        if ((this.mViewFlags & 131072) == 131072) {
            return null;
        }
        if ((this.mViewFlags & 32768) == 32768) {
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
        buildDrawingCache(false);
    }

    public void buildDrawingCache(boolean autoScale) {
        if ((this.mPrivateFlags & 32768) != 0) {
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

    private void buildDrawingCacheImpl(boolean autoScale) {
        this.mCachingFailed = false;
        int width = this.mRight - this.mLeft;
        int height = this.mBottom - this.mTop;
        AttachInfo attachInfo = this.mAttachInfo;
        boolean scalingRequired = attachInfo != null ? attachInfo.mScalingRequired : false;
        if (autoScale && scalingRequired) {
            width = (int) ((((float) width) * attachInfo.mApplicationScale) + 0.5f);
            height = (int) ((((float) height) * attachInfo.mApplicationScale) + 0.5f);
        }
        int drawingCacheBackgroundColor = this.mDrawingCacheBackgroundColor;
        boolean opaque = drawingCacheBackgroundColor == 0 ? isOpaque() : true;
        int use32BitCache = attachInfo != null ? attachInfo.mUse32BitDrawingCache : 0;
        int i = width * height;
        int i2 = (!opaque || (use32BitCache ^ 1) == 0) ? 4 : 2;
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
            if (opaque) {
                quality = use32BitCache != 0 ? Config.ARGB_8888 : Config.RGB_565;
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
                if (opaque && use32BitCache != 0) {
                    bitmap.setHasAlpha(false);
                }
                if (drawingCacheBackgroundColor != 0) {
                    clear = true;
                } else {
                    clear = false;
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
        if (autoScale && scalingRequired) {
            float scale = attachInfo.mApplicationScale;
            canvas.scale(scale, scale);
        }
        canvas.translate((float) (-this.mScrollX), (float) (-this.mScrollY));
        this.mPrivateFlags |= 32;
        if (!(this.mAttachInfo != null && (this.mAttachInfo.mHardwareAccelerated ^ 1) == 0 && this.mLayerType == 0)) {
            this.mPrivateFlags |= 32768;
        }
        if ((this.mPrivateFlags & 128) == 128) {
            this.mPrivateFlags &= -6291457;
            dispatchDraw(canvas);
            drawAutofilledHighlight(canvas);
            if (!(this.mOverlay == null || (this.mOverlay.isEmpty() ^ 1) == 0)) {
                this.mOverlay.getOverlayView().draw(canvas);
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
        float scale = attachInfo != null ? attachInfo.mApplicationScale : 1.0f;
        width = (int) ((((float) width) * scale) + 0.5f);
        height = (int) ((((float) height) * scale) + 0.5f);
        if (this.mResources != null) {
            DisplayMetrics displayMetrics = this.mResources.getDisplayMetrics();
            if (width <= 0) {
                width = 1;
            }
            if (height <= 0) {
                height = 1;
            }
            bitmap = Bitmap.createBitmap(displayMetrics, width, height, quality);
        } else {
            if (width <= 0) {
                width = 1;
            }
            if (height <= 0) {
                height = 1;
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
        boolean enabledHwBitmapsInSwMode = canvas.isHwBitmapsInSwModeEnabled();
        canvas.setHwBitmapsInSwModeEnabled(true);
        if ((-16777216 & backgroundColor) != 0) {
            bitmap.eraseColor(backgroundColor);
        }
        computeScroll();
        int restoreCount = canvas.save();
        canvas.scale(scale, scale);
        canvas.translate((float) (-this.mScrollX), (float) (-this.mScrollY));
        int flags = this.mPrivateFlags;
        this.mPrivateFlags &= -6291457;
        if ((this.mPrivateFlags & 128) == 128) {
            dispatchDraw(canvas);
            drawAutofilledHighlight(canvas);
            if (!(this.mOverlay == null || (this.mOverlay.isEmpty() ^ 1) == 0)) {
                this.mOverlay.getOverlayView().draw(canvas);
            }
        } else {
            draw(canvas);
        }
        this.mPrivateFlags = flags;
        canvas.restoreToCount(restoreCount);
        canvas.setBitmap(null);
        canvas.setHwBitmapsInSwModeEnabled(enabledHwBitmapsInSwMode);
        if (attachInfo != null) {
            attachInfo.mCanvas = canvas;
        }
        return bitmap;
    }

    public boolean isInEditMode() {
        return false;
    }

    protected boolean isPaddingOffsetRequired() {
        return false;
    }

    protected int getLeftPaddingOffset() {
        return 0;
    }

    protected int getRightPaddingOffset() {
        return 0;
    }

    protected int getTopPaddingOffset() {
        return 0;
    }

    protected int getBottomPaddingOffset() {
        return 0;
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
        return this.mAttachInfo != null ? this.mAttachInfo.mHardwareAccelerated : false;
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
            invalidateViewProperty(false, false);
        }
    }

    public Rect getClipBounds() {
        return this.mClipBounds != null ? new Rect(this.mClipBounds) : null;
    }

    public boolean getClipBounds(Rect outRect) {
        if (this.mClipBounds == null) {
            return false;
        }
        outRect.set(this.mClipBounds);
        return true;
    }

    private boolean applyLegacyAnimation(ViewGroup parent, long drawingTime, Animation a, boolean scalingRequired) {
        Transformation invalidationTransform;
        int flags = parent.mGroupFlags;
        if (!a.isInitialized()) {
            a.initialize(this.mRight - this.mLeft, this.mBottom - this.mTop, parent.getWidth(), parent.getHeight());
            a.initializeInvalidateRegion(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop);
            if (this.mAttachInfo != null) {
                a.setListenerHandler(this.mAttachInfo.mHandler);
            }
            onAnimationStart();
        }
        Transformation t = parent.getChildTransformation();
        boolean more = a.getTransformation(drawingTime, t, 1.0f);
        if (!scalingRequired || this.mAttachInfo.mApplicationScale == 1.0f) {
            invalidationTransform = t;
        } else {
            if (parent.mInvalidationTransformation == null) {
                parent.mInvalidationTransformation = new Transformation();
            }
            invalidationTransform = parent.mInvalidationTransformation;
            a.getTransformation(drawingTime, invalidationTransform, 1.0f);
        }
        if (more) {
            if (a.willChangeBounds()) {
                if (parent.mInvalidateRegion == null) {
                    parent.mInvalidateRegion = new RectF();
                }
                RectF region = parent.mInvalidateRegion;
                a.getInvalidateRegion(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop, region, invalidationTransform);
                parent.mPrivateFlags |= 64;
                int left = this.mLeft + ((int) region.left);
                int top = this.mTop + ((int) region.top);
                parent.invalidate(left, top, ((int) (region.width() + 0.5f)) + left, ((int) (region.height() + 0.5f)) + top);
            } else if ((flags & 144) == 128) {
                parent.mGroupFlags |= 4;
            } else if ((flags & 4) == 0) {
                parent.mPrivateFlags |= 64;
                parent.invalidate(this.mLeft, this.mTop, this.mRight, this.mBottom);
            }
        }
        return more;
    }

    void setDisplayListProperties(RenderNode renderNode) {
        boolean z = false;
        if (renderNode != null) {
            renderNode.setHasOverlappingRendering(getHasOverlappingRendering());
            if (this.mParent instanceof ViewGroup) {
                z = ((ViewGroup) this.mParent).getClipChildren();
            }
            renderNode.setClipToBounds(z);
            float alpha = 1.0f;
            if ((this.mParent instanceof ViewGroup) && (((ViewGroup) this.mParent).mGroupFlags & 2048) != 0) {
                ViewGroup parentVG = this.mParent;
                Transformation t = parentVG.getChildTransformation();
                if (parentVG.getChildStaticTransformation(this, t)) {
                    int transformType = t.getTransformationType();
                    if (transformType != 0) {
                        if ((transformType & 1) != 0) {
                            alpha = t.getAlpha();
                        }
                        if ((transformType & 2) != 0) {
                            renderNode.setStaticMatrix(t.getMatrix());
                        }
                    }
                }
            }
            if (this.mTransformationInfo != null) {
                alpha *= getFinalAlpha();
                if (alpha < 1.0f && onSetAlpha((int) (255.0f * alpha))) {
                    alpha = 1.0f;
                }
                renderNode.setAlpha(alpha);
            } else if (alpha < 1.0f) {
                renderNode.setAlpha(alpha);
            }
        }
    }

    boolean draw(Canvas canvas, ViewGroup parent, long drawingTime) {
        int drawingWithRenderNode;
        boolean hardwareAcceleratedCanvas = canvas.isHardwareAccelerated();
        if (this.mAttachInfo == null || !this.mAttachInfo.mHardwareAccelerated) {
            drawingWithRenderNode = 0;
        } else {
            drawingWithRenderNode = hardwareAcceleratedCanvas;
        }
        boolean more = false;
        boolean childHasIdentityMatrix = hasIdentityMatrix();
        int parentFlags = parent.mGroupFlags;
        if ((parentFlags & 256) != 0) {
            parent.getChildTransformation().clear();
            parent.mGroupFlags &= -257;
        }
        Transformation transformToApply = null;
        boolean concatMatrix = false;
        boolean scalingRequired = this.mAttachInfo != null ? this.mAttachInfo.mScalingRequired : false;
        Animation a = getAnimation();
        if (a != null) {
            more = applyLegacyAnimation(parent, drawingTime, a, scalingRequired);
            concatMatrix = a.willChangeTransformationMatrix();
            if (concatMatrix) {
                this.mPrivateFlags3 |= 1;
            }
            transformToApply = parent.getChildTransformation();
        } else {
            if ((this.mPrivateFlags3 & 1) != 0) {
                this.mRenderNode.setAnimationMatrix(null);
                this.mPrivateFlags3 &= -2;
            }
            if (drawingWithRenderNode == 0 && (parentFlags & 2048) != 0) {
                Transformation t = parent.getChildTransformation();
                if (parent.getChildStaticTransformation(this, t)) {
                    int transformType = t.getTransformationType();
                    transformToApply = transformType != 0 ? t : null;
                    concatMatrix = (transformType & 2) != 0;
                }
            }
        }
        concatMatrix |= childHasIdentityMatrix ^ 1;
        this.mPrivateFlags |= 32;
        if (!concatMatrix && (parentFlags & GL10.GL_EXP2) == 1) {
            if (canvas.quickReject((float) this.mLeft, (float) this.mTop, (float) this.mRight, (float) this.mBottom, EdgeType.BW) && (this.mPrivateFlags & 64) == 0) {
                this.mPrivateFlags2 |= 268435456;
                return more;
            }
        }
        this.mPrivateFlags2 &= -268435457;
        if (hardwareAcceleratedCanvas) {
            this.mRecreateDisplayList = (this.mPrivateFlags & Integer.MIN_VALUE) != 0;
            this.mPrivateFlags &= Integer.MAX_VALUE;
        }
        RenderNode renderNode = null;
        Bitmap cache = null;
        int layerType = getLayerType();
        if (layerType == 1 || (drawingWithRenderNode ^ 1) != 0) {
            if (layerType != 0) {
                layerType = 1;
                buildDrawingCache(true);
            }
            cache = getDrawingCache(true);
        }
        if (drawingWithRenderNode != 0) {
            renderNode = updateDisplayListIfDirty();
            if (!renderNode.isValid()) {
                renderNode = null;
                drawingWithRenderNode = 0;
            }
        }
        int sx = 0;
        int sy = 0;
        if (drawingWithRenderNode == 0) {
            computeScroll();
            sx = this.mScrollX;
            sy = this.mScrollY;
        }
        int drawingWithDrawingCache = cache != null ? drawingWithRenderNode ^ 1 : 0;
        int offsetForScroll = cache == null ? drawingWithRenderNode ^ 1 : 0;
        int restoreTo = -1;
        if (drawingWithRenderNode == 0 || transformToApply != null) {
            restoreTo = canvas.save();
        }
        if (offsetForScroll != 0) {
            canvas.translate((float) (this.mLeft - sx), (float) (this.mTop - sy));
        } else {
            if (drawingWithRenderNode == 0) {
                canvas.translate((float) this.mLeft, (float) this.mTop);
            }
            if (scalingRequired) {
                if (drawingWithRenderNode != 0) {
                    restoreTo = canvas.save();
                }
                float scale = 1.0f / this.mAttachInfo.mApplicationScale;
                canvas.scale(scale, scale);
            }
        }
        float alpha = drawingWithRenderNode != 0 ? 1.0f : getAlpha() * getTransitionAlpha();
        if (transformToApply != null || alpha < 1.0f || (hasIdentityMatrix() ^ 1) != 0 || (this.mPrivateFlags3 & 2) != 0) {
            if (!(transformToApply == null && (childHasIdentityMatrix ^ 1) == 0)) {
                int transX = 0;
                int transY = 0;
                if (offsetForScroll != 0) {
                    transX = -sx;
                    transY = -sy;
                }
                if (transformToApply != null) {
                    if (concatMatrix) {
                        if (drawingWithRenderNode != 0) {
                            renderNode.setAnimationMatrix(transformToApply.getMatrix());
                        } else {
                            canvas.translate((float) (-transX), (float) (-transY));
                            canvas.concat(transformToApply.getMatrix());
                            canvas.translate((float) transX, (float) transY);
                        }
                        parent.mGroupFlags |= 256;
                    }
                    float transformAlpha = transformToApply.getAlpha();
                    if (transformAlpha < 1.0f) {
                        alpha *= transformAlpha;
                        parent.mGroupFlags |= 256;
                    }
                }
                if (!(childHasIdentityMatrix || (drawingWithRenderNode ^ 1) == 0)) {
                    canvas.translate((float) (-transX), (float) (-transY));
                    canvas.concat(getMatrix());
                    canvas.translate((float) transX, (float) transY);
                }
            }
            if (alpha < 1.0f || (this.mPrivateFlags3 & 2) != 0) {
                if (alpha < 1.0f) {
                    this.mPrivateFlags3 |= 2;
                } else {
                    this.mPrivateFlags3 &= -3;
                }
                parent.mGroupFlags |= 256;
                if (drawingWithDrawingCache == 0) {
                    int multipliedAlpha = (int) (255.0f * alpha);
                    if (onSetAlpha(multipliedAlpha)) {
                        this.mPrivateFlags |= 262144;
                    } else if (drawingWithRenderNode != 0) {
                        renderNode.setAlpha((getAlpha() * alpha) * getTransitionAlpha());
                    } else if (layerType == 0) {
                        canvas.saveLayerAlpha((float) sx, (float) sy, (float) (getWidth() + sx), (float) (getHeight() + sy), multipliedAlpha);
                    }
                }
            }
        } else if ((this.mPrivateFlags & 262144) == 262144) {
            onSetAlpha(255);
            this.mPrivateFlags &= -262145;
        }
        if (drawingWithRenderNode == 0) {
            if ((parentFlags & 1) != 0 && cache == null) {
                if (offsetForScroll != 0) {
                    canvas.clipRect(sx, sy, getWidth() + sx, getHeight() + sy);
                } else if (!scalingRequired || cache == null) {
                    canvas.clipRect(0, 0, getWidth(), getHeight());
                } else {
                    canvas.clipRect(0, 0, cache.getWidth(), cache.getHeight());
                }
            }
            if (this.mClipBounds != null) {
                canvas.clipRect(this.mClipBounds);
            }
        }
        if (drawingWithDrawingCache == 0) {
            if (drawingWithRenderNode != 0) {
                this.mPrivateFlags &= -6291457;
                ((DisplayListCanvas) canvas).drawRenderNode(renderNode);
            } else if ((this.mPrivateFlags & 128) == 128) {
                this.mPrivateFlags &= -6291457;
                dispatchDraw(canvas);
            } else {
                draw(canvas);
            }
        } else if (cache != null) {
            this.mPrivateFlags &= -6291457;
            if (layerType == 0 || this.mLayerPaint == null) {
                Paint cachePaint = parent.mCachePaint;
                if (cachePaint == null) {
                    cachePaint = new Paint();
                    cachePaint.setDither(false);
                    parent.mCachePaint = cachePaint;
                }
                cachePaint.setAlpha((int) (255.0f * alpha));
                canvas.drawBitmap(cache, 0.0f, 0.0f, cachePaint);
            } else {
                int layerPaintAlpha = this.mLayerPaint.getAlpha();
                if (alpha < 1.0f) {
                    this.mLayerPaint.setAlpha((int) (((float) layerPaintAlpha) * alpha));
                }
                canvas.drawBitmap(cache, 0.0f, 0.0f, this.mLayerPaint);
                if (alpha < 1.0f) {
                    this.mLayerPaint.setAlpha(layerPaintAlpha);
                }
            }
        }
        if (restoreTo >= 0) {
            canvas.restoreToCount(restoreTo);
        }
        if (!(a == null || (more ^ 1) == 0)) {
            if (!(hardwareAcceleratedCanvas || (a.getFillAfter() ^ 1) == 0)) {
                onSetAlpha(255);
            }
            parent.finishAnimatingView(this, a);
        }
        if (more && hardwareAcceleratedCanvas && a.hasAlpha() && (this.mPrivateFlags & 262144) == 262144) {
            invalidate(true);
        }
        this.mRecreateDisplayList = false;
        return more;
    }

    static Paint getDebugPaint() {
        if (sDebugPaint == null) {
            sDebugPaint = new Paint();
            sDebugPaint.setAntiAlias(false);
        }
        return sDebugPaint;
    }

    final int dipsToPixels(int dips) {
        return (int) ((((float) dips) * getContext().getResources().getDisplayMetrics().density) + 0.5f);
    }

    private final void debugDrawFocus(Canvas canvas) {
        if (isFocused()) {
            int cornerSquareSize = dipsToPixels(8);
            int l = this.mScrollX;
            int r = (this.mRight + l) - this.mLeft;
            int t = this.mScrollY;
            int b = (this.mBottom + t) - this.mTop;
            Paint paint = getDebugPaint();
            paint.setColor(DEBUG_CORNERS_COLOR);
            paint.setStyle(Style.FILL);
            Canvas canvas2 = canvas;
            canvas2.drawRect((float) l, (float) t, (float) (l + cornerSquareSize), (float) (t + cornerSquareSize), paint);
            canvas2 = canvas;
            canvas2.drawRect((float) (r - cornerSquareSize), (float) t, (float) r, (float) (t + cornerSquareSize), paint);
            canvas.drawRect((float) l, (float) (b - cornerSquareSize), (float) (l + cornerSquareSize), (float) b, paint);
            canvas.drawRect((float) (r - cornerSquareSize), (float) (b - cornerSquareSize), (float) r, (float) b, paint);
            paint.setStyle(Style.STROKE);
            canvas.drawLine((float) l, (float) t, (float) r, (float) b, paint);
            canvas.drawLine((float) l, (float) b, (float) r, (float) t, paint);
        }
    }

    public void draw(Canvas canvas) {
        int privateFlags = this.mPrivateFlags;
        int dirtyOpaque = (PFLAG_DIRTY_MASK & privateFlags) == 4194304 ? this.mAttachInfo != null ? this.mAttachInfo.mIgnoreDirtyState ^ 1 : 1 : 0;
        this.mPrivateFlags = (-6291457 & privateFlags) | 32;
        if (dirtyOpaque == 0) {
            drawBackground(canvas);
        }
        int viewFlags = this.mViewFlags;
        boolean horizontalEdges = (viewFlags & 4096) != 0;
        boolean verticalEdges = (viewFlags & 8192) != 0;
        if (verticalEdges || (horizontalEdges ^ 1) == 0) {
            boolean drawTop = false;
            boolean drawBottom = false;
            boolean drawLeft = false;
            boolean drawRight = false;
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
                length = (bottom - top) / 2;
            }
            if (horizontalEdges && left + length > right - length) {
                length = (right - left) / 2;
            }
            if (verticalEdges) {
                topFadeStrength = Math.max(0.0f, Math.min(1.0f, getTopFadingEdgeStrength()));
                drawTop = topFadeStrength * fadeHeight > 1.0f;
                bottomFadeStrength = Math.max(0.0f, Math.min(1.0f, getBottomFadingEdgeStrength()));
                drawBottom = bottomFadeStrength * fadeHeight > 1.0f;
            }
            if (horizontalEdges) {
                leftFadeStrength = Math.max(0.0f, Math.min(1.0f, getLeftFadingEdgeStrength()));
                drawLeft = leftFadeStrength * fadeHeight > 1.0f;
                rightFadeStrength = Math.max(0.0f, Math.min(1.0f, getRightFadingEdgeStrength()));
                drawRight = rightFadeStrength * fadeHeight > 1.0f;
            }
            int saveCount = canvas.getSaveCount();
            int solidColor = getSolidColor();
            if (solidColor == 0) {
                if (drawTop) {
                    canvas.saveLayer((float) left, (float) top, (float) right, (float) (top + length), null, 4);
                }
                if (drawBottom) {
                    canvas.saveLayer((float) left, (float) (bottom - length), (float) right, (float) bottom, null, 4);
                }
                if (drawLeft) {
                    canvas.saveLayer((float) left, (float) top, (float) (left + length), (float) bottom, null, 4);
                }
                if (drawRight) {
                    canvas.saveLayer((float) (right - length), (float) top, (float) right, (float) bottom, null, 4);
                }
            } else {
                scrollabilityCache.setFadeColor(solidColor);
            }
            if (dirtyOpaque == 0) {
                onDraw(canvas);
            }
            dispatchDraw(canvas);
            Paint p = scrollabilityCache.paint;
            Matrix matrix = scrollabilityCache.matrix;
            Shader fade = scrollabilityCache.shader;
            if (drawTop) {
                matrix.setScale(1.0f, fadeHeight * topFadeStrength);
                matrix.postTranslate((float) left, (float) top);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                canvas.drawRect((float) left, (float) top, (float) right, (float) (top + length), p);
            }
            if (drawBottom) {
                matrix.setScale(1.0f, fadeHeight * bottomFadeStrength);
                matrix.postRotate(180.0f);
                matrix.postTranslate((float) left, (float) bottom);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                canvas.drawRect((float) left, (float) (bottom - length), (float) right, (float) bottom, p);
            }
            if (drawLeft) {
                matrix.setScale(1.0f, fadeHeight * leftFadeStrength);
                matrix.postRotate(-90.0f);
                matrix.postTranslate((float) left, (float) top);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                canvas.drawRect((float) left, (float) top, (float) (left + length), (float) bottom, p);
            }
            if (drawRight) {
                matrix.setScale(1.0f, fadeHeight * rightFadeStrength);
                matrix.postRotate(90.0f);
                matrix.postTranslate((float) right, (float) top);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                canvas.drawRect((float) (right - length), (float) top, (float) right, (float) bottom, p);
            }
            canvas.restoreToCount(saveCount);
            drawAutofilledHighlight(canvas);
            if (!(this.mOverlay == null || (this.mOverlay.isEmpty() ^ 1) == 0)) {
                this.mOverlay.getOverlayView().dispatchDraw(canvas);
            }
            onDrawForeground(canvas);
            if (debugDraw()) {
                debugDrawFocus(canvas);
            }
            return;
        }
        if (dirtyOpaque == 0) {
            onDraw(canvas);
        }
        dispatchDraw(canvas);
        drawAutofilledHighlight(canvas);
        if (!(this.mOverlay == null || (this.mOverlay.isEmpty() ^ 1) == 0)) {
            this.mOverlay.getOverlayView().dispatchDraw(canvas);
        }
        onDrawForeground(canvas);
        drawDefaultFocusHighlight(canvas);
        if (debugDraw()) {
            debugDrawFocus(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        Drawable background = this.mBackground;
        if (background != null) {
            setBackgroundBounds();
            if (!(!canvas.isHardwareAccelerated() || this.mAttachInfo == null || this.mAttachInfo.mThreadedRenderer == null)) {
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
            this.mBackground.setBounds(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop);
            this.mBackgroundSizeChanged = false;
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
            renderNode.setClipToBounds(false);
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
        return 0;
    }

    private static String printFlags(int flags) {
        String output = LogException.NO_VALUE;
        int numFlags = 0;
        if ((flags & 1) == 1) {
            output = output + "TAKES_FOCUS";
            numFlags = 1;
        }
        switch (flags & 12) {
            case 4:
                if (numFlags > 0) {
                    output = output + " ";
                }
                return output + "INVISIBLE";
            case 8:
                if (numFlags > 0) {
                    output = output + " ";
                }
                return output + "GONE";
            default:
                return output;
        }
    }

    private static String printPrivateFlags(int privateFlags) {
        String output = LogException.NO_VALUE;
        int numFlags = 0;
        if ((privateFlags & 1) == 1) {
            output = output + "WANTS_FOCUS";
            numFlags = 1;
        }
        if ((privateFlags & 2) == 2) {
            if (numFlags > 0) {
                output = output + " ";
            }
            output = output + "FOCUSED";
            numFlags++;
        }
        if ((privateFlags & 4) == 4) {
            if (numFlags > 0) {
                output = output + " ";
            }
            output = output + "SELECTED";
            numFlags++;
        }
        if ((privateFlags & 8) == 8) {
            if (numFlags > 0) {
                output = output + " ";
            }
            output = output + "IS_ROOT_NAMESPACE";
            numFlags++;
        }
        if ((privateFlags & 16) == 16) {
            if (numFlags > 0) {
                output = output + " ";
            }
            output = output + "HAS_BOUNDS";
            numFlags++;
        }
        if ((privateFlags & 32) != 32) {
            return output;
        }
        if (numFlags > 0) {
            output = output + " ";
        }
        return output + "DRAWN";
    }

    public boolean isLayoutRequested() {
        return (this.mPrivateFlags & 4096) == 4096;
    }

    public static boolean isLayoutModeOptical(Object o) {
        return o instanceof ViewGroup ? ((ViewGroup) o).isLayoutModeOptical() : false;
    }

    private boolean setOpticalFrame(int left, int top, int right, int bottom) {
        Insets parentInsets = this.mParent instanceof View ? ((View) this.mParent).getOpticalInsets() : Insets.NONE;
        Insets childInsets = getOpticalInsets();
        return setFrame((parentInsets.left + left) - childInsets.left, (parentInsets.top + top) - childInsets.top, (parentInsets.left + right) + childInsets.right, (parentInsets.top + bottom) + childInsets.bottom);
    }

    public void layout(int l, int t, int r, int b) {
        if ((this.mPrivateFlags3 & 8) != 0) {
            onMeasure(this.mOldWidthMeasureSpec, this.mOldHeightMeasureSpec);
            this.mPrivateFlags3 &= -9;
        }
        int oldL = this.mLeft;
        int oldT = this.mTop;
        int oldB = this.mBottom;
        int oldR = this.mRight;
        boolean changed = isLayoutModeOptical(this.mParent) ? setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);
        if (changed || (this.mPrivateFlags & 8192) == 8192) {
            onLayout(changed, l, t, r, b);
            if (!shouldDrawRoundScrollbar()) {
                this.mRoundScrollbarRenderer = null;
            } else if (this.mRoundScrollbarRenderer == null) {
                this.mRoundScrollbarRenderer = new RoundScrollbarRenderer(this);
            }
            this.mPrivateFlags &= -8193;
            ListenerInfo li = this.mListenerInfo;
            if (!(li == null || li.mOnLayoutChangeListeners == null)) {
                ArrayList<OnLayoutChangeListener> listenersCopy = (ArrayList) li.mOnLayoutChangeListeners.clone();
                int numListeners = listenersCopy.size();
                for (int i = 0; i < numListeners; i++) {
                    ((OnLayoutChangeListener) listenersCopy.get(i)).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
                }
            }
        }
        this.mPrivateFlags &= -4097;
        this.mPrivateFlags3 |= 4;
        if ((this.mPrivateFlags3 & 134217728) != 0) {
            this.mPrivateFlags3 &= -134217729;
            notifyEnterOrExitForAutoFillIfNeeded(true);
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = false;
        if (!(this.mLeft == left && this.mRight == right && this.mTop == top && this.mBottom == bottom)) {
            changed = true;
            int drawn = this.mPrivateFlags & 32;
            int oldWidth = this.mRight - this.mLeft;
            int oldHeight = this.mBottom - this.mTop;
            int newWidth = right - left;
            int newHeight = bottom - top;
            boolean sizeChanged = (newWidth == oldWidth && newHeight == oldHeight) ? false : true;
            invalidate(sizeChanged);
            this.mLeft = left;
            this.mTop = top;
            this.mRight = right;
            this.mBottom = bottom;
            this.mRenderNode.setLeftTopRightBottom(this.mLeft, this.mTop, this.mRight, this.mBottom);
            this.mPrivateFlags |= 16;
            if (sizeChanged) {
                sizeChange(newWidth, newHeight, oldWidth, oldHeight);
            }
            if ((this.mViewFlags & 12) == 0 || this.mGhostView != null) {
                this.mPrivateFlags |= 32;
                invalidate(sizeChanged);
                invalidateParentCaches();
            }
            this.mPrivateFlags |= drawn;
            this.mBackgroundSizeChanged = true;
            this.mDefaultFocusHighlightSizeChanged = true;
            if (this.mForegroundInfo != null) {
                this.mForegroundInfo.mBoundsChanged = true;
            }
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
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
                this.mAttachInfo.mViewRootImpl.mChoreographer.postCallbackDelayed(1, what, who, Choreographer.subtractFrameDelay(delay));
                return;
            }
            getRunQueue().postDelayed(what, delay);
        }
    }

    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (verifyDrawable(who) && what != null) {
            if (this.mAttachInfo != null) {
                this.mAttachInfo.mViewRootImpl.mChoreographer.removeCallbacks(1, what, who);
            }
            getRunQueue().removeCallbacks(what);
        }
    }

    public void unscheduleDrawable(Drawable who) {
        if (this.mAttachInfo != null && who != null) {
            this.mAttachInfo.mViewRootImpl.mChoreographer.removeCallbacks(1, null, who);
        }
    }

    protected void resolveDrawables() {
        if (isLayoutDirectionResolved() || getRawLayoutDirection() != 2) {
            int layoutDirection = isLayoutDirectionResolved() ? getLayoutDirection() : getRawLayoutDirection();
            if (this.mBackground != null) {
                this.mBackground.setLayoutDirection(layoutDirection);
            }
            if (!(this.mForegroundInfo == null || this.mForegroundInfo.mDrawable == null)) {
                this.mForegroundInfo.mDrawable.setLayoutDirection(layoutDirection);
            }
            if (this.mDefaultFocusHighlight != null) {
                this.mDefaultFocusHighlight.setLayoutDirection(layoutDirection);
            }
            this.mPrivateFlags2 |= 1073741824;
            onResolveDrawables(layoutDirection);
        }
    }

    boolean areDrawablesResolved() {
        return (this.mPrivateFlags2 & 1073741824) == 1073741824;
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
        if (who == this.mBackground) {
            return true;
        }
        if ((this.mForegroundInfo == null || this.mForegroundInfo.mDrawable != who) && this.mDefaultFocusHighlight != who) {
            return false;
        }
        return true;
    }

    protected void drawableStateChanged() {
        int i = 0;
        int[] state = getDrawableState();
        int changed = 0;
        Drawable bg = this.mBackground;
        if (bg != null && bg.isStateful()) {
            changed = bg.setState(state);
        }
        Drawable hl = this.mDefaultFocusHighlight;
        if (hl != null && hl.isStateful()) {
            changed |= hl.setState(state);
        }
        Drawable fg = this.mForegroundInfo != null ? this.mForegroundInfo.mDrawable : null;
        if (fg != null && fg.isStateful()) {
            changed |= fg.setState(state);
        }
        if (this.mScrollCache != null) {
            Drawable scrollBar = this.mScrollCache.scrollBar;
            if (scrollBar != null && scrollBar.isStateful()) {
                if (scrollBar.setState(state) && this.mScrollCache.state != 0) {
                    i = 1;
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
        if (this.mDefaultFocusHighlight != null) {
            this.mDefaultFocusHighlight.setHotspot(x, y);
        }
        if (!(this.mForegroundInfo == null || this.mForegroundInfo.mDrawable == null)) {
            this.mForegroundInfo.mDrawable.setHotspot(x, y);
        }
        dispatchDrawableHotspotChanged(x, y);
    }

    public void dispatchDrawableHotspotChanged(float x, float y) {
    }

    public void refreshDrawableState() {
        this.mPrivateFlags |= 1024;
        drawableStateChanged();
        ViewParent parent = this.mParent;
        if (parent != null) {
            parent.childDrawableStateChanged(this);
        }
    }

    private Drawable getDefaultFocusHighlightDrawable() {
        if (this.mDefaultFocusHighlightCache == null && this.mContext != null) {
            TypedArray ta = this.mContext.obtainStyledAttributes(new int[]{R.attr.selectableItemBackground});
            this.mDefaultFocusHighlightCache = ta.getDrawable(0);
            ta.recycle();
        }
        return this.mDefaultFocusHighlightCache;
    }

    private void setDefaultFocusHighlight(Drawable highlight) {
        this.mDefaultFocusHighlight = highlight;
        this.mDefaultFocusHighlightSizeChanged = true;
        if (highlight != null) {
            if ((this.mPrivateFlags & 128) != 0) {
                this.mPrivateFlags &= -129;
            }
            highlight.setLayoutDirection(getLayoutDirection());
            if (highlight.isStateful()) {
                highlight.setState(getDrawableState());
            }
            if (isAttachedToWindow()) {
                highlight.setVisible(getWindowVisibility() == 0 ? isShown() : false, false);
            }
            highlight.setCallback(this);
        } else if ((this.mViewFlags & 128) != 0 && this.mBackground == null && (this.mForegroundInfo == null || this.mForegroundInfo.mDrawable == null)) {
            this.mPrivateFlags |= 128;
        }
        invalidate();
    }

    public boolean isDefaultFocusHighlightNeeded(Drawable background, Drawable foreground) {
        boolean lackFocusState;
        if (background != null && (background.isStateful() ^ 1) == 0 && (background.hasFocusStateSpecified() ^ 1) == 0) {
            lackFocusState = false;
        } else if (foreground == null || (foreground.isStateful() ^ 1) != 0) {
            lackFocusState = true;
        } else {
            lackFocusState = foreground.hasFocusStateSpecified() ^ 1;
        }
        if (!isInTouchMode() && getDefaultFocusHighlightEnabled() && lackFocusState && isAttachedToWindow()) {
            return sUseDefaultFocusHighlight;
        }
        return false;
    }

    private void switchDefaultFocusHighlight() {
        if (isFocused()) {
            boolean needed = isDefaultFocusHighlightNeeded(this.mBackground, this.mForegroundInfo == null ? null : this.mForegroundInfo.mDrawable);
            boolean active = this.mDefaultFocusHighlight != null;
            if (needed && (active ^ 1) != 0) {
                setDefaultFocusHighlight(getDefaultFocusHighlightDrawable());
            } else if (!needed && active) {
                setDefaultFocusHighlight(null);
            }
        }
    }

    private void drawDefaultFocusHighlight(Canvas canvas) {
        if (this.mDefaultFocusHighlight != null) {
            if (this.mDefaultFocusHighlightSizeChanged) {
                this.mDefaultFocusHighlightSizeChanged = false;
                int l = this.mScrollX;
                int r = (this.mRight + l) - this.mLeft;
                int t = this.mScrollY;
                this.mDefaultFocusHighlight.setBounds(l, t, r, (this.mBottom + t) - this.mTop);
            }
            this.mDefaultFocusHighlight.draw(canvas);
        }
    }

    public final int[] getDrawableState() {
        if (this.mDrawableState != null && (this.mPrivateFlags & 1024) == 0) {
            return this.mDrawableState;
        }
        this.mDrawableState = onCreateDrawableState(0);
        this.mPrivateFlags &= -1025;
        return this.mDrawableState;
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        if ((this.mViewFlags & 4194304) == 4194304 && (this.mParent instanceof View)) {
            return ((View) this.mParent).onCreateDrawableState(extraSpace);
        }
        int privateFlags = this.mPrivateFlags;
        int viewStateIndex = 0;
        if ((privateFlags & 16384) != 0) {
            viewStateIndex = 16;
        }
        if ((this.mViewFlags & 32) == 0) {
            viewStateIndex |= 8;
        }
        if (isFocused()) {
            viewStateIndex |= 4;
        }
        if ((privateFlags & 4) != 0) {
            viewStateIndex |= 2;
        }
        if (hasWindowFocus()) {
            viewStateIndex |= 1;
        }
        if ((1073741824 & privateFlags) != 0) {
            viewStateIndex |= 32;
        }
        if (this.mAttachInfo != null && this.mAttachInfo.mHardwareAccelerationRequested && ThreadedRenderer.isAvailable()) {
            viewStateIndex |= 64;
        }
        if ((268435456 & privateFlags) != 0) {
            viewStateIndex |= 128;
        }
        int privateFlags2 = this.mPrivateFlags2;
        if ((privateFlags2 & 1) != 0) {
            viewStateIndex |= 256;
        }
        if ((privateFlags2 & 2) != 0) {
            viewStateIndex |= 512;
        }
        int[] drawableState = StateSet.get(viewStateIndex);
        if (extraSpace == 0) {
            return drawableState;
        }
        int[] fullState;
        if (drawableState != null) {
            fullState = new int[(drawableState.length + extraSpace)];
            System.arraycopy(drawableState, 0, fullState, 0, drawableState.length);
        } else {
            fullState = new int[extraSpace];
        }
        return fullState;
    }

    protected static int[] mergeDrawableStates(int[] baseState, int[] additionalState) {
        int i = baseState.length - 1;
        while (i >= 0 && baseState[i] == 0) {
            i--;
        }
        System.arraycopy(additionalState, 0, baseState, i + 1, additionalState.length);
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
        if (this.mDefaultFocusHighlight != null) {
            this.mDefaultFocusHighlight.jumpToCurrentState();
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
            this.mBackgroundResource = 0;
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
            boolean requestLayout = false;
            this.mBackgroundResource = 0;
            if (this.mBackground != null) {
                if (isAttachedToWindow()) {
                    this.mBackground.setVisible(false, false);
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
                        case 1:
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
                    this.mLeftPaddingDefined = false;
                    this.mRightPaddingDefined = false;
                }
                if (!(this.mBackground != null && this.mBackground.getMinimumHeight() == background.getMinimumHeight() && this.mBackground.getMinimumWidth() == background.getMinimumWidth())) {
                    requestLayout = true;
                }
                this.mBackground = background;
                if (background.isStateful()) {
                    background.setState(getDrawableState());
                }
                if (isAttachedToWindow()) {
                    background.setVisible(getWindowVisibility() == 0 ? isShown() : false, false);
                }
                applyBackgroundTint();
                background.setCallback(this);
                if ((this.mPrivateFlags & 128) != 0) {
                    this.mPrivateFlags &= -129;
                    requestLayout = true;
                }
            } else {
                this.mBackground = null;
                if ((this.mViewFlags & 128) != 0 && this.mDefaultFocusHighlight == null && (this.mForegroundInfo == null || this.mForegroundInfo.mDrawable == null)) {
                    this.mPrivateFlags |= 128;
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
                    this.mForegroundInfo.mDrawable.setVisible(false, false);
                }
                this.mForegroundInfo.mDrawable.setCallback(null);
                unscheduleDrawable(this.mForegroundInfo.mDrawable);
            }
            this.mForegroundInfo.mDrawable = foreground;
            this.mForegroundInfo.mBoundsChanged = true;
            if (foreground != null) {
                if ((this.mPrivateFlags & 128) != 0) {
                    this.mPrivateFlags &= -129;
                }
                foreground.setLayoutDirection(getLayoutDirection());
                if (foreground.isStateful()) {
                    foreground.setState(getDrawableState());
                }
                applyForegroundTint();
                if (isAttachedToWindow()) {
                    foreground.setVisible(getWindowVisibility() == 0 ? isShown() : false, false);
                }
                foreground.setCallback(this);
            } else if ((this.mViewFlags & 128) != 0 && this.mBackground == null && this.mDefaultFocusHighlight == null) {
                this.mPrivateFlags |= 128;
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
            if ((gravity & 112) == 0) {
                gravity |= 48;
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

    private Drawable getAutofilledDrawable() {
        if (this.mAttachInfo == null) {
            return null;
        }
        if (this.mAttachInfo.mAutofilledDrawable == null) {
            Context rootContext = getRootView().getContext();
            TypedArray a = rootContext.getTheme().obtainStyledAttributes(AUTOFILL_HIGHLIGHT_ATTR);
            int attributeResourceId = a.getResourceId(0, 0);
            this.mAttachInfo.mAutofilledDrawable = rootContext.getDrawable(attributeResourceId);
            a.recycle();
        }
        return this.mAttachInfo.mAutofilledDrawable;
    }

    private void drawAutofilledHighlight(Canvas canvas) {
        if (isAutofilled()) {
            Drawable autofilledHighlight = getAutofilledDrawable();
            if (autofilledHighlight != null) {
                autofilledHighlight.setBounds(0, 0, getWidth(), getHeight());
                autofilledHighlight.draw(canvas);
            }
        }
    }

    public void onDrawForeground(Canvas canvas) {
        onDrawScrollIndicators(canvas);
        onDrawScrollBars(canvas);
        Drawable foreground = this.mForegroundInfo != null ? this.mForegroundInfo.mDrawable : null;
        if (foreground != null) {
            if (this.mForegroundInfo.mBoundsChanged) {
                this.mForegroundInfo.mBoundsChanged = false;
                Rect selfBounds = this.mForegroundInfo.mSelfBounds;
                Rect overlayBounds = this.mForegroundInfo.mOverlayBounds;
                if (this.mForegroundInfo.mInsidePadding) {
                    selfBounds.set(0, 0, getWidth(), getHeight());
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
        this.mUserPaddingStart = Integer.MIN_VALUE;
        this.mUserPaddingEnd = Integer.MIN_VALUE;
        this.mUserPaddingLeftInitial = left;
        this.mUserPaddingRightInitial = right;
        this.mLeftPaddingDefined = true;
        this.mRightPaddingDefined = true;
        internalSetPadding(left, top, right, bottom);
    }

    protected void internalSetPadding(int left, int top, int right, int bottom) {
        int i = 0;
        this.mUserPaddingLeft = left;
        this.mUserPaddingRight = right;
        this.mUserPaddingBottom = bottom;
        int viewFlags = this.mViewFlags;
        boolean changed = false;
        if ((viewFlags & 768) != 0) {
            if ((viewFlags & 512) != 0) {
                int offset = (viewFlags & 16777216) == 0 ? 0 : getVerticalScrollbarWidth();
                switch (this.mVerticalScrollbarPosition) {
                    case 0:
                        if (!isRtlLocale()) {
                            right += offset;
                            break;
                        } else {
                            left += offset;
                            break;
                        }
                    case 1:
                        left += offset;
                        break;
                    case 2:
                        right += offset;
                        break;
                }
            }
            if ((viewFlags & 256) != 0) {
                if ((viewFlags & 16777216) != 0) {
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
            case 1:
                this.mUserPaddingLeftInitial = end;
                this.mUserPaddingRightInitial = start;
                internalSetPadding(end, top, start, bottom);
                return;
            default:
                this.mUserPaddingLeftInitial = start;
                this.mUserPaddingRightInitial = end;
                internalSetPadding(start, top, end, bottom);
                return;
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
        return getLayoutDirection() == 1 ? this.mPaddingRight : this.mPaddingLeft;
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
        return getLayoutDirection() == 1 ? this.mPaddingLeft : this.mPaddingRight;
    }

    public boolean isPaddingRelative() {
        return (this.mUserPaddingStart == Integer.MIN_VALUE && this.mUserPaddingEnd == Integer.MIN_VALUE) ? false : true;
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
            this.mPaddingLeft = this.mUserPaddingEnd >= 0 ? this.mUserPaddingEnd : this.mUserPaddingLeftInitial;
            this.mPaddingRight = this.mUserPaddingStart >= 0 ? this.mUserPaddingStart : this.mUserPaddingRightInitial;
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
        if (((this.mPrivateFlags & 4) != 0) != selected) {
            int i;
            int i2 = this.mPrivateFlags & -5;
            if (selected) {
                i = 4;
            } else {
                i = 0;
            }
            this.mPrivateFlags = i | i2;
            if (!selected) {
                resetPressedState();
            }
            invalidate(true);
            refreshDrawableState();
            dispatchSetSelected(selected);
            if (selected) {
                sendAccessibilityEvent(4);
            } else {
                notifyViewAccessibilityStateChangedIfNeeded(0);
            }
        }
    }

    protected void dispatchSetSelected(boolean selected) {
    }

    @ExportedProperty
    public boolean isSelected() {
        return (this.mPrivateFlags & 4) != 0;
    }

    public void setActivated(boolean activated) {
        int i = 1073741824;
        if (((this.mPrivateFlags & 1073741824) != 0) != activated) {
            int i2 = this.mPrivateFlags & -1073741825;
            if (!activated) {
                i = 0;
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
        return (this.mPrivateFlags & 1073741824) != 0;
    }

    public ViewTreeObserver getViewTreeObserver() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mTreeObserver;
        }
        if (this.mFloatingTreeObserver == null) {
            this.mFloatingTreeObserver = new ViewTreeObserver(this.mContext);
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
            return false;
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
            return false;
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
        int[] location = new int[2];
        getLocationOnScreen(location);
        return location;
    }

    public void getLocationOnScreen(int[] outLocation) {
        getLocationInWindow(outLocation);
        AttachInfo info = this.mAttachInfo;
        if (info != null) {
            outLocation[0] = outLocation[0] + info.mWindowLeft;
            outLocation[1] = outLocation[1] + info.mWindowTop;
        }
    }

    public void getLocationInWindow(int[] outLocation) {
        if (outLocation == null || outLocation.length < 2) {
            throw new IllegalArgumentException("outLocation must be an array of two integers");
        }
        outLocation[0] = 0;
        outLocation[1] = 0;
        transformFromViewToWindowSpace(outLocation);
    }

    public void transformFromViewToWindowSpace(int[] inOutLocation) {
        if (inOutLocation == null || inOutLocation.length < 2) {
            throw new IllegalArgumentException("inOutLocation must be an array of two integers");
        } else if (this.mAttachInfo == null) {
            inOutLocation[1] = 0;
            inOutLocation[0] = 0;
        } else {
            float[] position = this.mAttachInfo.mTmpTransformLocation;
            position[0] = (float) inOutLocation[0];
            position[1] = (float) inOutLocation[1];
            if (!hasIdentityMatrix()) {
                getMatrix().mapPoints(position);
            }
            position[0] = position[0] + ((float) this.mLeft);
            position[1] = position[1] + ((float) this.mTop);
            ViewParent viewParent = this.mParent;
            while (viewParent instanceof View) {
                View view = (View) viewParent;
                position[0] = position[0] - ((float) view.mScrollX);
                position[1] = position[1] - ((float) view.mScrollY);
                if (!view.hasIdentityMatrix()) {
                    view.getMatrix().mapPoints(position);
                }
                position[0] = position[0] + ((float) view.mLeft);
                position[1] = position[1] + ((float) view.mTop);
                viewParent = view.mParent;
            }
            if (viewParent instanceof ViewRootImpl) {
                position[1] = position[1] - ((float) ((ViewRootImpl) viewParent).mCurScrollY);
            }
            inOutLocation[0] = Math.round(position[0]);
            inOutLocation[1] = Math.round(position[1]);
        }
    }

    protected <T extends View> T findViewTraversal(int id) {
        if (id == this.mID) {
            return this;
        }
        return null;
    }

    protected <T extends View> T findViewWithTagTraversal(Object tag) {
        if (tag == null || !tag.equals(this.mTag)) {
            return null;
        }
        return this;
    }

    protected <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        if (predicate.test(this)) {
            return this;
        }
        return null;
    }

    public final <T extends View> T findViewById(int id) {
        if (id == -1) {
            return null;
        }
        return findViewTraversal(id);
    }

    final <T extends View> T findViewByAccessibilityId(int accessibilityId) {
        if (accessibilityId < 0) {
            return null;
        }
        T view = findViewByAccessibilityIdTraversal(accessibilityId);
        if (view == null) {
            return null;
        }
        if (!view.includeForAccessibility()) {
            view = null;
        }
        return view;
    }

    public <T extends View> T findViewByAccessibilityIdTraversal(int accessibilityId) {
        if (getAccessibilityViewId() == accessibilityId) {
            return this;
        }
        return null;
    }

    public <T extends View> T findViewByAutofillIdTraversal(int autofillId) {
        if (getAutofillViewId() == autofillId) {
            return this;
        }
        return null;
    }

    public final <T extends View> T findViewWithTag(Object tag) {
        if (tag == null) {
            return null;
        }
        return findViewWithTagTraversal(tag);
    }

    public final <T extends View> T findViewByPredicate(Predicate<View> predicate) {
        return findViewByPredicateTraversal(predicate, null);
    }

    public final <T extends View> T findViewByPredicateInsideOut(View start, Predicate<View> predicate) {
        View childToSkip = null;
        while (true) {
            T view = start.findViewByPredicateTraversal(predicate, childToSkip);
            if (view != null || start == this) {
                return view;
            }
            ViewParent parent = start.getParent();
            if (parent == null || ((parent instanceof View) ^ 1) != 0) {
                return null;
            }
            childToSkip = start;
            start = (View) parent;
        }
        return null;
    }

    public void setId(int id) {
        this.mID = id;
        if (this.mID == -1 && this.mLabelForId != -1) {
            this.mID = generateViewId();
        }
    }

    public void setIsRootNamespace(boolean isRoot) {
        if (isRoot) {
            this.mPrivateFlags |= 8;
        } else {
            this.mPrivateFlags &= -9;
        }
    }

    public boolean isRootNamespace() {
        return (this.mPrivateFlags & 8) != 0;
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
        if ((key >>> 24) < 2) {
            throw new IllegalArgumentException("The key must be an application-specific resource id.");
        }
        setKeyedTag(key, tag);
    }

    public void setTagInternal(int key, Object tag) {
        if ((key >>> 24) != 1) {
            throw new IllegalArgumentException("The key must be a framework-specific resource id.");
        }
        setKeyedTag(key, tag);
    }

    private void setKeyedTag(int key, Object tag) {
        if (this.mKeyedTags == null) {
            this.mKeyedTags = new SparseArray(2);
        }
        this.mKeyedTags.put(key, tag);
    }

    public void debug() {
        debug(0);
    }

    protected void debug(int depth) {
        String output = debugIndent(depth - 1) + "+ " + this;
        int id = getId();
        if (id != -1) {
            output = output + " (id=" + id + ")";
        }
        Object tag = getTag();
        if (tag != null) {
            output = output + " (tag=" + tag + ")";
        }
        Log.d(VIEW_LOG_TAG, output);
        if ((this.mPrivateFlags & 2) != 0) {
            Log.d(VIEW_LOG_TAG, debugIndent(depth) + " FOCUSED");
        }
        Log.d(VIEW_LOG_TAG, debugIndent(depth) + "frame={" + this.mLeft + ", " + this.mTop + ", " + this.mRight + ", " + this.mBottom + "} scroll={" + this.mScrollX + ", " + this.mScrollY + "} ");
        if (!(this.mPaddingLeft == 0 && this.mPaddingTop == 0 && this.mPaddingRight == 0 && this.mPaddingBottom == 0)) {
            Log.d(VIEW_LOG_TAG, debugIndent(depth) + "padding={" + this.mPaddingLeft + ", " + this.mPaddingTop + ", " + this.mPaddingRight + ", " + this.mPaddingBottom + "}");
        }
        Log.d(VIEW_LOG_TAG, debugIndent(depth) + "mMeasureWidth=" + this.mMeasuredWidth + " mMeasureHeight=" + this.mMeasuredHeight);
        output = debugIndent(depth);
        if (this.mLayoutParams == null) {
            output = output + "BAD! no layout params";
        } else {
            output = this.mLayoutParams.debug(output);
        }
        Log.d(VIEW_LOG_TAG, output);
        Log.d(VIEW_LOG_TAG, ((debugIndent(depth) + "flags={") + printFlags(this.mViewFlags)) + "}");
        Log.d(VIEW_LOG_TAG, ((debugIndent(depth) + "privateFlags={") + printPrivateFlags(this.mPrivateFlags)) + "}");
    }

    protected static String debugIndent(int depth) {
        StringBuilder spaces = new StringBuilder(((depth * 2) + 3) * 2);
        for (int i = 0; i < (depth * 2) + 3; i++) {
            spaces.append(' ').append(' ');
        }
        return spaces.toString();
    }

    @ExportedProperty(category = "layout")
    public int getBaseline() {
        return -1;
    }

    public boolean isInLayout() {
        ViewRootImpl viewRoot = getViewRootImpl();
        return viewRoot != null ? viewRoot.isInLayout() : false;
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
        this.mPrivateFlags |= 4096;
        this.mPrivateFlags |= Integer.MIN_VALUE;
        if (!(this.mParent == null || (this.mParent.isLayoutRequested() ^ 1) == 0 || this.mParent == null)) {
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
        this.mPrivateFlags |= 4096;
        this.mPrivateFlags |= Integer.MIN_VALUE;
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
        long key = (((long) widthMeasureSpec) << 32) | (((long) heightMeasureSpec) & 4294967295L);
        if (this.mMeasureCache == null) {
            this.mMeasureCache = new LongSparseLongArray(2);
        }
        boolean forceLayout = (this.mPrivateFlags & 4096) == 4096;
        if (widthMeasureSpec == this.mOldWidthMeasureSpec) {
            specChanged = heightMeasureSpec != this.mOldHeightMeasureSpec;
        } else {
            specChanged = true;
        }
        boolean isSpecExactly = MeasureSpec.getMode(widthMeasureSpec) == 1073741824 ? MeasureSpec.getMode(heightMeasureSpec) == 1073741824 : false;
        if (getMeasuredWidth() == MeasureSpec.getSize(widthMeasureSpec)) {
            matchesSpecSize = getMeasuredHeight() == MeasureSpec.getSize(heightMeasureSpec);
        } else {
            matchesSpecSize = false;
        }
        int needsLayout = specChanged ? (sAlwaysRemeasureExactly || (isSpecExactly ^ 1) != 0) ? 1 : matchesSpecSize ^ 1 : 0;
        if (forceLayout || needsLayout != 0) {
            this.mPrivateFlags &= -2049;
            resolveRtlPropertiesIfNeeded();
            int cacheIndex = forceLayout ? -1 : this.mMeasureCache.indexOfKey(key);
            if (cacheIndex < 0 || sIgnoreMeasureCache) {
                onMeasure(widthMeasureSpec, heightMeasureSpec);
                this.mPrivateFlags3 &= -9;
            } else {
                long value = this.mMeasureCache.valueAt(cacheIndex);
                setMeasuredDimensionRaw((int) (value >> 32), (int) value);
                this.mPrivateFlags3 |= 8;
            }
            if ((this.mPrivateFlags & 2048) != 2048) {
                throw new IllegalStateException("View with id " + getId() + ": " + getClass().getName() + "#onMeasure() did not set the" + " measured dimension by calling" + " setMeasuredDimension()");
            }
            this.mPrivateFlags |= 8192;
        }
        this.mOldWidthMeasureSpec = widthMeasureSpec;
        this.mOldHeightMeasureSpec = heightMeasureSpec;
        this.mMeasureCache.put(key, (((long) this.mMeasuredWidth) << 32) | (((long) this.mMeasuredHeight) & 4294967295L));
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
        this.mPrivateFlags |= 2048;
    }

    public static int combineMeasuredStates(int curState, int newState) {
        return curState | newState;
    }

    public static int resolveSize(int size, int measureSpec) {
        return resolveSizeAndState(size, measureSpec, 0) & 16777215;
    }

    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case Integer.MIN_VALUE:
                if (specSize >= size) {
                    result = size;
                    break;
                }
                result = specSize | 16777216;
                break;
            case 1073741824:
                result = specSize;
                break;
            default:
                result = size;
                break;
        }
        return (-16777216 & childMeasuredState) | result;
    }

    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case Integer.MIN_VALUE:
            case 1073741824:
                return specSize;
            case 0:
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
            if (this.mAttachInfo != null && this.mAttachInfo.mDisplayState == 1 && animation.getStartTime() == -1) {
                animation.setStartTime(AnimationUtils.currentAnimationTimeMillis());
            }
            animation.reset();
        }
    }

    protected void onAnimationStart() {
        this.mPrivateFlags |= 65536;
    }

    protected void onAnimationEnd() {
        this.mPrivateFlags &= -65537;
    }

    protected boolean onSetAlpha(int alpha) {
        return false;
    }

    public boolean gatherTransparentRegion(Region region) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (!(region == null || attachInfo == null)) {
            if ((this.mPrivateFlags & 128) == 0) {
                int[] location = attachInfo.mTransparentLocation;
                getLocationInWindow(location);
                int shadowOffset = getZ() > 0.0f ? (int) getZ() : 0;
                region.op(location[0] - shadowOffset, location[1] - shadowOffset, ((location[0] + this.mRight) - this.mLeft) + shadowOffset, (shadowOffset * 3) + ((location[1] + this.mBottom) - this.mTop), Op.DIFFERENCE);
            } else {
                if (!(this.mBackground == null || this.mBackground.getOpacity() == -2)) {
                    applyDrawableToTransparentRegion(this.mBackground, region);
                }
                if (!(this.mForegroundInfo == null || this.mForegroundInfo.mDrawable == null || this.mForegroundInfo.mDrawable.getOpacity() == -2)) {
                    applyDrawableToTransparentRegion(this.mForegroundInfo.mDrawable, region);
                }
                if (!(this.mDefaultFocusHighlight == null || this.mDefaultFocusHighlight.getOpacity() == -2)) {
                    applyDrawableToTransparentRegion(this.mDefaultFocusHighlight, region);
                }
            }
        }
        return true;
    }

    public void playSoundEffect(int soundConstant) {
        if (this.mAttachInfo != null && this.mAttachInfo.mRootCallbacks != null && (isSoundEffectsEnabled() ^ 1) == 0) {
            this.mAttachInfo.mRootCallbacks.playSoundEffect(soundConstant);
        }
    }

    public boolean performHapticFeedback(int feedbackConstant) {
        return performHapticFeedback(feedbackConstant, 0);
    }

    public boolean performHapticFeedback(int feedbackConstant, int flags) {
        boolean z = false;
        if (this.mAttachInfo == null) {
            return false;
        }
        if ((flags & 1) == 0 && (isHapticFeedbackEnabled() ^ 1) != 0) {
            return false;
        }
        Callbacks callbacks = this.mAttachInfo.mRootCallbacks;
        if ((flags & 2) != 0) {
            z = true;
        }
        return callbacks.performHapticFeedback(feedbackConstant, z);
    }

    public void setSystemUiVisibility(int visibility) {
        if (visibility != this.mSystemUiVisibility) {
            this.mSystemUiVisibility = visibility;
            if (this.mParent != null && this.mAttachInfo != null && (this.mAttachInfo.mRecomputeGlobalAttributes ^ 1) != 0) {
                this.mParent.recomputeViewAttributes(this);
            }
        }
    }

    public int getSystemUiVisibility() {
        return this.mSystemUiVisibility;
    }

    public int getWindowSystemUiVisibility() {
        return this.mAttachInfo != null ? this.mAttachInfo.mSystemUiVisibility : 0;
    }

    public void onWindowSystemUiVisibilityChanged(int visible) {
    }

    public void dispatchWindowSystemUiVisiblityChanged(int visible) {
        onWindowSystemUiVisibilityChanged(visible);
    }

    public void setOnSystemUiVisibilityChangeListener(OnSystemUiVisibilityChangeListener l) {
        getListenerInfo().mOnSystemUiVisibilityChangeListener = l;
        if (this.mParent != null && this.mAttachInfo != null && (this.mAttachInfo.mRecomputeGlobalAttributes ^ 1) != 0) {
            this.mParent.recomputeViewAttributes(this);
        }
    }

    public void dispatchSystemUiVisibilityChanged(int visibility) {
        ListenerInfo li = this.mListenerInfo;
        if (li != null && li.mOnSystemUiVisibilityChangeListener != null) {
            li.mOnSystemUiVisibilityChangeListener.onSystemUiVisibilityChange(visibility & PUBLIC_STATUS_BAR_VISIBILITY_MASK);
        }
    }

    boolean updateLocalSystemUiVisibility(int localValue, int localChanges) {
        int val = (this.mSystemUiVisibility & (~localChanges)) | (localValue & localChanges);
        if (val == this.mSystemUiVisibility) {
            return false;
        }
        setSystemUiVisibility(val);
        return true;
    }

    public void setDisabledSystemUiVisibility(int flags) {
        if (this.mAttachInfo != null && this.mAttachInfo.mDisabledSystemUiVisibility != flags) {
            this.mAttachInfo.mDisabledSystemUiVisibility = flags;
            if (this.mParent != null) {
                this.mParent.recomputeViewAttributes(this);
            }
        }
    }

    @Deprecated
    public final boolean startDrag(ClipData data, DragShadowBuilder shadowBuilder, Object myLocalState, int flags) {
        return startDragAndDrop(data, shadowBuilder, myLocalState, flags);
    }

    public final boolean startDragAndDrop(ClipData data, DragShadowBuilder shadowBuilder, Object myLocalState, int flags) {
        if (this.mAttachInfo == null) {
            Log.w(VIEW_LOG_TAG, "startDragAndDrop called on a detached view.");
            return false;
        }
        if (data != null) {
            data.prepareToLeaveProcess((flags & 256) != 0);
        }
        boolean okay = false;
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
        Canvas canvas;
        try {
            this.mAttachInfo.mDragToken = this.mAttachInfo.mSession.prepareDrag(this.mAttachInfo.mWindow, flags, shadowSize.x, shadowSize.y, this.mAttachInfo.mDragSurface);
            if (this.mAttachInfo.mDragToken != null) {
                canvas = this.mAttachInfo.mDragSurface.lockCanvas(null);
                canvas.drawColor(0, Mode.CLEAR);
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
            Canvas canvas;
            try {
                canvas = this.mAttachInfo.mDragSurface.lockCanvas(null);
                canvas.drawColor(0, Mode.CLEAR);
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
            return false;
        }
    }

    public boolean onDragEvent(DragEvent event) {
        return false;
    }

    boolean dispatchDragEnterExitInPreN(DragEvent event) {
        return callDragEventHandler(event);
    }

    public boolean dispatchDragEvent(DragEvent event) {
        event.mEventHandlerWasCalled = true;
        if (event.mAction == 2 || event.mAction == 3) {
            getViewRootImpl().setDragFocus(this, event);
        }
        return callDragEventHandler(event);
    }

    final boolean callDragEventHandler(DragEvent event) {
        boolean result;
        ListenerInfo li = this.mListenerInfo;
        if (li == null || li.mOnDragListener == null || (this.mViewFlags & 32) != 0 || !li.mOnDragListener.onDrag(this, event)) {
            result = onDragEvent(event);
        } else {
            result = true;
        }
        switch (event.mAction) {
            case 4:
                this.mPrivateFlags2 &= -4;
                refreshDrawableState();
                break;
            case 5:
                this.mPrivateFlags2 |= 2;
                refreshDrawableState();
                break;
            case 6:
                this.mPrivateFlags2 &= -3;
                refreshDrawableState();
                break;
        }
        return result;
    }

    boolean canAcceptDrag() {
        return (this.mPrivateFlags2 & 1) != 0;
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
            r.op(0, 0, db.left, h, Op.UNION);
        }
        if (db.right < w) {
            r.op(db.right, 0, w, h, Op.UNION);
        }
        if (db.top > 0) {
            r.op(0, 0, w, db.top, Op.UNION);
        }
        if (db.bottom < h) {
            r.op(0, db.bottom, w, h, Op.UNION);
        }
        int[] location = attachInfo.mTransparentLocation;
        getLocationInWindow(location);
        r.translate(location[0], location[1]);
        region.op(r, Op.INTERSECT);
    }

    private void checkForLongClick(int delayOffset, float x, float y) {
        if ((this.mViewFlags & 2097152) == 2097152 || (this.mViewFlags & 1073741824) == 1073741824) {
            this.mHasPerformedLongPress = false;
            if (this.mPendingCheckForLongPress == null) {
                this.mPendingCheckForLongPress = new CheckForLongPress(this, null);
            }
            this.mPendingCheckForLongPress.setAnchor(x, y);
            this.mPendingCheckForLongPress.rememberWindowAttachCount();
            this.mPendingCheckForLongPress.rememberPressedState();
            postDelayed(this.mPendingCheckForLongPress, (long) (ViewConfiguration.getLongPressTimeout() - delayOffset));
        }
    }

    public static View inflate(Context context, int resource, ViewGroup root) {
        return LayoutInflater.from(context).inflate(resource, root);
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int overScrollMode = this.mOverScrollMode;
        boolean canScrollHorizontal = computeHorizontalScrollRange() > computeHorizontalScrollExtent();
        boolean canScrollVertical = computeVerticalScrollRange() > computeVerticalScrollExtent();
        boolean overScrollHorizontal = overScrollMode != 0 ? overScrollMode == 1 ? canScrollHorizontal : false : true;
        boolean overScrollVertical = overScrollMode != 0 ? overScrollMode == 1 ? canScrollVertical : false : true;
        int newScrollX = scrollX + deltaX;
        if (!overScrollHorizontal) {
            maxOverScrollX = 0;
        }
        int newScrollY = scrollY + deltaY;
        if (!overScrollVertical) {
            maxOverScrollY = 0;
        }
        int left = -maxOverScrollX;
        int right = maxOverScrollX + scrollRangeX;
        int top = -maxOverScrollY;
        int bottom = maxOverScrollY + scrollRangeY;
        boolean clampedX = false;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        }
        boolean clampedY = false;
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
        if (overScrollMode == 0 || overScrollMode == 1 || overScrollMode == 2) {
            this.mOverScrollMode = overScrollMode;
            return;
        }
        throw new IllegalArgumentException("Invalid overscroll mode " + overScrollMode);
    }

    public void setNestedScrollingEnabled(boolean enabled) {
        if (enabled) {
            this.mPrivateFlags3 |= 128;
            return;
        }
        stopNestedScroll();
        this.mPrivateFlags3 &= -129;
    }

    public boolean isNestedScrollingEnabled() {
        return (this.mPrivateFlags3 & 128) == 128;
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
        return false;
    }

    public void stopNestedScroll() {
        if (this.mNestedScrollingParent != null) {
            this.mNestedScrollingParent.onStopNestedScroll(this);
            this.mNestedScrollingParent = null;
        }
    }

    public boolean hasNestedScrollingParent() {
        return this.mNestedScrollingParent != null;
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        if (isNestedScrollingEnabled() && this.mNestedScrollingParent != null) {
            if (dxConsumed != 0 || dyConsumed != 0 || dxUnconsumed != 0 || dyUnconsumed != 0) {
                int startX = 0;
                int startY = 0;
                if (offsetInWindow != null) {
                    getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[0];
                    startY = offsetInWindow[1];
                }
                this.mNestedScrollingParent.onNestedScroll(this, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
                if (offsetInWindow != null) {
                    getLocationInWindow(offsetInWindow);
                    offsetInWindow[0] = offsetInWindow[0] - startX;
                    offsetInWindow[1] = offsetInWindow[1] - startY;
                }
                return true;
            } else if (offsetInWindow != null) {
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        boolean z = true;
        if (isNestedScrollingEnabled() && this.mNestedScrollingParent != null) {
            if (dx != 0 || dy != 0) {
                int startX = 0;
                int startY = 0;
                if (offsetInWindow != null) {
                    getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[0];
                    startY = offsetInWindow[1];
                }
                if (consumed == null) {
                    if (this.mTempNestedScrollConsumed == null) {
                        this.mTempNestedScrollConsumed = new int[2];
                    }
                    consumed = this.mTempNestedScrollConsumed;
                }
                consumed[0] = 0;
                consumed[1] = 0;
                this.mNestedScrollingParent.onNestedPreScroll(this, dx, dy, consumed);
                if (offsetInWindow != null) {
                    getLocationInWindow(offsetInWindow);
                    offsetInWindow[0] = offsetInWindow[0] - startX;
                    offsetInWindow[1] = offsetInWindow[1] - startY;
                }
                if (consumed[0] == 0 && consumed[1] == 0) {
                    z = false;
                }
                return z;
            } else if (offsetInWindow != null) {
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        if (!isNestedScrollingEnabled() || this.mNestedScrollingParent == null) {
            return false;
        }
        return this.mNestedScrollingParent.onNestedFling(this, velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        if (!isNestedScrollingEnabled() || this.mNestedScrollingParent == null) {
            return false;
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
        return (this.mPrivateFlags2 & PFLAG2_TEXT_DIRECTION_MASK) >> 6;
    }

    public void setTextDirection(int textDirection) {
        if (getRawTextDirection() != textDirection) {
            this.mPrivateFlags2 &= -449;
            resetResolvedTextDirection();
            this.mPrivateFlags2 |= (textDirection << 6) & PFLAG2_TEXT_DIRECTION_MASK;
            resolveTextDirection();
            onRtlPropertiesChanged(getLayoutDirection());
            requestLayout();
            invalidate(true);
        }
    }

    @ExportedProperty(category = "text", mapping = {@IntToString(from = 0, to = "INHERIT"), @IntToString(from = 1, to = "FIRST_STRONG"), @IntToString(from = 2, to = "ANY_RTL"), @IntToString(from = 3, to = "LTR"), @IntToString(from = 4, to = "RTL"), @IntToString(from = 5, to = "LOCALE"), @IntToString(from = 6, to = "FIRST_STRONG_LTR"), @IntToString(from = 7, to = "FIRST_STRONG_RTL")})
    public int getTextDirection() {
        return (this.mPrivateFlags2 & PFLAG2_TEXT_DIRECTION_RESOLVED_MASK) >> 10;
    }

    public boolean resolveTextDirection() {
        this.mPrivateFlags2 &= -7681;
        if (hasRtlSupport()) {
            int textDirection = getRawTextDirection();
            switch (textDirection) {
                case 0:
                    if (canResolveTextDirection()) {
                        try {
                            if (this.mParent == null || (this.mParent.isTextDirectionResolved() ^ 1) == 0) {
                                int parentResolvedDirection = -1;
                                try {
                                    if (this.mParent != null) {
                                        parentResolvedDirection = this.mParent.getTextDirection();
                                    }
                                } catch (AbstractMethodError e) {
                                    Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                                    parentResolvedDirection = 3;
                                }
                                switch (parentResolvedDirection) {
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 4:
                                    case 5:
                                    case 6:
                                    case 7:
                                        this.mPrivateFlags2 |= parentResolvedDirection << 10;
                                        break;
                                    default:
                                        this.mPrivateFlags2 |= 1024;
                                        break;
                                }
                            }
                            this.mPrivateFlags2 |= 1024;
                            return false;
                        } catch (AbstractMethodError e2) {
                            Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e2);
                            this.mPrivateFlags2 |= SYSTEM_UI_LAYOUT_FLAGS;
                            return true;
                        }
                    }
                    this.mPrivateFlags2 |= 1024;
                    return false;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    this.mPrivateFlags2 |= textDirection << 10;
                    break;
                default:
                    this.mPrivateFlags2 |= 1024;
                    break;
            }
        }
        this.mPrivateFlags2 |= 1024;
        this.mPrivateFlags2 |= 512;
        return true;
    }

    public boolean canResolveTextDirection() {
        switch (getRawTextDirection()) {
            case 0:
                if (this.mParent != null) {
                    try {
                        return this.mParent.canResolveTextDirection();
                    } catch (AbstractMethodError e) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                    }
                }
                return false;
            default:
                return true;
        }
    }

    public void resetResolvedTextDirection() {
        this.mPrivateFlags2 &= -7681;
        this.mPrivateFlags2 |= 1024;
    }

    public boolean isTextDirectionInherited() {
        return getRawTextDirection() == 0;
    }

    public boolean isTextDirectionResolved() {
        return (this.mPrivateFlags2 & 512) == 512;
    }

    @ExportedProperty(category = "text", mapping = {@IntToString(from = 0, to = "INHERIT"), @IntToString(from = 1, to = "GRAVITY"), @IntToString(from = 2, to = "TEXT_START"), @IntToString(from = 3, to = "TEXT_END"), @IntToString(from = 4, to = "CENTER"), @IntToString(from = 5, to = "VIEW_START"), @IntToString(from = 6, to = "VIEW_END")})
    public int getRawTextAlignment() {
        return (this.mPrivateFlags2 & PFLAG2_TEXT_ALIGNMENT_MASK) >> 13;
    }

    public void setTextAlignment(int textAlignment) {
        if (textAlignment != getRawTextAlignment()) {
            this.mPrivateFlags2 &= -57345;
            resetResolvedTextAlignment();
            this.mPrivateFlags2 |= (textAlignment << 13) & PFLAG2_TEXT_ALIGNMENT_MASK;
            resolveTextAlignment();
            onRtlPropertiesChanged(getLayoutDirection());
            requestLayout();
            invalidate(true);
        }
    }

    @ExportedProperty(category = "text", mapping = {@IntToString(from = 0, to = "INHERIT"), @IntToString(from = 1, to = "GRAVITY"), @IntToString(from = 2, to = "TEXT_START"), @IntToString(from = 3, to = "TEXT_END"), @IntToString(from = 4, to = "CENTER"), @IntToString(from = 5, to = "VIEW_START"), @IntToString(from = 6, to = "VIEW_END")})
    public int getTextAlignment() {
        return (this.mPrivateFlags2 & PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK) >> 17;
    }

    public boolean resolveTextAlignment() {
        this.mPrivateFlags2 &= -983041;
        if (hasRtlSupport()) {
            int textAlignment = getRawTextAlignment();
            switch (textAlignment) {
                case 0:
                    if (canResolveTextAlignment()) {
                        try {
                            if (this.mParent.isTextAlignmentResolved()) {
                                int parentResolvedTextAlignment;
                                try {
                                    parentResolvedTextAlignment = this.mParent.getTextAlignment();
                                } catch (AbstractMethodError e) {
                                    Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                                    parentResolvedTextAlignment = 1;
                                }
                                switch (parentResolvedTextAlignment) {
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 4:
                                    case 5:
                                    case 6:
                                        this.mPrivateFlags2 |= parentResolvedTextAlignment << 17;
                                        break;
                                    default:
                                        this.mPrivateFlags2 |= 131072;
                                        break;
                                }
                            }
                            this.mPrivateFlags2 |= 131072;
                            return false;
                        } catch (AbstractMethodError e2) {
                            Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e2);
                            this.mPrivateFlags2 |= 196608;
                            return true;
                        }
                    }
                    this.mPrivateFlags2 |= 131072;
                    return false;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    this.mPrivateFlags2 |= textAlignment << 17;
                    break;
                default:
                    this.mPrivateFlags2 |= 131072;
                    break;
            }
        }
        this.mPrivateFlags2 |= 131072;
        this.mPrivateFlags2 |= 65536;
        return true;
    }

    public boolean canResolveTextAlignment() {
        switch (getRawTextAlignment()) {
            case 0:
                if (this.mParent != null) {
                    try {
                        return this.mParent.canResolveTextAlignment();
                    } catch (AbstractMethodError e) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                    }
                }
                return false;
            default:
                return true;
        }
    }

    public void resetResolvedTextAlignment() {
        this.mPrivateFlags2 &= -983041;
        this.mPrivateFlags2 |= 131072;
    }

    public boolean isTextAlignmentInherited() {
        return getRawTextAlignment() == 0;
    }

    public boolean isTextAlignmentResolved() {
        return (this.mPrivateFlags2 & 65536) == 65536;
    }

    public static int generateViewId() {
        int result;
        int newValue;
        do {
            result = sNextGeneratedId.get();
            newValue = result + 1;
            if (newValue > 16777215) {
                newValue = 1;
            }
        } while (!sNextGeneratedId.compareAndSet(result, newValue));
        return result;
    }

    private static boolean isViewIdGenerated(int id) {
        return (-16777216 & id) == 0 && (16777215 & id) != 0;
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
            return PointerIcon.getSystemIcon(this.mContext, 1000);
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

    public boolean hasPointerCapture() {
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl == null) {
            return false;
        }
        return viewRootImpl.hasPointerCapture();
    }

    public void requestPointerCapture() {
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null) {
            viewRootImpl.requestPointerCapture(true);
        }
    }

    public void releasePointerCapture() {
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null) {
            viewRootImpl.requestPointerCapture(false);
        }
    }

    public void onPointerCaptureChange(boolean hasCapture) {
    }

    public void dispatchPointerCaptureChanged(boolean hasCapture) {
        onPointerCaptureChange(hasCapture);
    }

    public boolean onCapturedPointerEvent(MotionEvent event) {
        return false;
    }

    public void setOnCapturedPointerListener(OnCapturedPointerListener l) {
        getListenerInfo().mOnCapturedPointerListener = l;
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
        return false;
    }

    private static void dumpFlags() {
        HashMap<String, String> found = Maps.newHashMap();
        try {
            for (Field field : View.class.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    if (field.getType().equals(Integer.TYPE)) {
                        dumpFlag(found, field.getName(), field.getInt(null));
                    } else if (field.getType().equals(int[].class)) {
                        int[] values = (int[]) field.get(null);
                        for (int i = 0; i < values.length; i++) {
                            dumpFlag(found, field.getName() + "[" + i + "]", values[i]);
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
        String bits = String.format("%32s", new Object[]{Integer.toBinaryString(value)}).replace('0', ' ');
        int prefix = name.indexOf(95);
        StringBuilder stringBuilder = new StringBuilder();
        if (prefix > 0) {
            substring = name.substring(0, prefix);
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
        stream.addProperty("focus:focusable", getFocusable());
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
        int n = this.mAttributes != null ? this.mAttributes.length : 0;
        stream.addProperty("meta:__attrCount__", n / 2);
        for (int i = 0; i < n; i += 2) {
            stream.addProperty("meta:__attr__" + this.mAttributes[i], this.mAttributes[i + 1]);
        }
        stream.addProperty("misc:scrollBarStyle", getScrollBarStyle());
        stream.addProperty("text:textDirection", getTextDirection());
        stream.addProperty("text:textAlignment", getTextAlignment());
        CharSequence contentDescription = getContentDescription();
        stream.addProperty("accessibility:contentDescription", contentDescription == null ? LogException.NO_VALUE : contentDescription.toString());
        stream.addProperty("accessibility:labelFor", getLabelFor());
        stream.addProperty("accessibility:importantForAccessibility", getImportantForAccessibility());
    }

    boolean shouldDrawRoundScrollbar() {
        boolean z = true;
        if (!this.mResources.getConfiguration().isScreenRound() || this.mAttachInfo == null) {
            return false;
        }
        View rootView = getRootView();
        WindowInsets insets = getRootWindowInsets();
        int height = getHeight();
        int width = getWidth();
        int displayHeight = rootView.getHeight();
        int displayWidth = rootView.getWidth();
        if (height != displayHeight || width != displayWidth) {
            return false;
        }
        getLocationInWindow(this.mAttachInfo.mTmpLocation);
        if (this.mAttachInfo.mTmpLocation[0] != insets.getStableInsetLeft()) {
            z = false;
        } else if (this.mAttachInfo.mTmpLocation[1] != insets.getStableInsetTop()) {
            z = false;
        }
        return z;
    }

    public void setTooltipText(CharSequence tooltipText) {
        if (TextUtils.isEmpty(tooltipText)) {
            setFlags(0, 1073741824);
            -android_view_View-mthref-1();
            this.mTooltipInfo = null;
            return;
        }
        setFlags(1073741824, 1073741824);
        if (this.mTooltipInfo == null) {
            this.mTooltipInfo = new TooltipInfo();
            this.mTooltipInfo.mShowTooltipRunnable = new -$Lambda$iU_USrtPm1XIm5H9QYQvXfBGDE4(this);
            this.mTooltipInfo.mHideTooltipRunnable = new android.view.-$Lambda$iU_USrtPm1XIm5H9QYQvXfBGDE4.AnonymousClass1(this);
        }
        this.mTooltipInfo.mTooltipText = tooltipText;
    }

    public void setTooltip(CharSequence tooltipText) {
        setTooltipText(tooltipText);
    }

    public CharSequence getTooltipText() {
        return this.mTooltipInfo != null ? this.mTooltipInfo.mTooltipText : null;
    }

    public CharSequence getTooltip() {
        return getTooltipText();
    }

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean showTooltip(int x, int y, boolean fromLongClick) {
        if (this.mAttachInfo == null || this.mTooltipInfo == null || (this.mViewFlags & 32) != 0 || TextUtils.isEmpty(this.mTooltipInfo.mTooltipText)) {
            return false;
        }
        -android_view_View-mthref-1();
        this.mTooltipInfo.mTooltipFromLongClick = fromLongClick;
        this.mTooltipInfo.mTooltipPopup = new TooltipPopup(getContext());
        this.mTooltipInfo.mTooltipPopup.show(this, x, y, (this.mPrivateFlags3 & 131072) == 131072, this.mTooltipInfo.mTooltipText);
        this.mAttachInfo.mTooltipHost = this;
        return true;
    }

    /* renamed from: hideTooltip */
    void -android_view_View-mthref-1() {
        if (this.mTooltipInfo != null) {
            removeCallbacks(this.mTooltipInfo.mShowTooltipRunnable);
            if (this.mTooltipInfo.mTooltipPopup != null) {
                this.mTooltipInfo.mTooltipPopup.hide();
                this.mTooltipInfo.mTooltipPopup = null;
                this.mTooltipInfo.mTooltipFromLongClick = false;
                if (this.mAttachInfo != null) {
                    this.mAttachInfo.mTooltipHost = null;
                }
            }
        }
    }

    private boolean showLongClickTooltip(int x, int y) {
        removeCallbacks(this.mTooltipInfo.mShowTooltipRunnable);
        removeCallbacks(this.mTooltipInfo.mHideTooltipRunnable);
        return showTooltip(x, y, true);
    }

    private void showHoverTooltip() {
        showTooltip(this.mTooltipInfo.mAnchorX, this.mTooltipInfo.mAnchorY, false);
    }

    boolean dispatchTooltipHoverEvent(MotionEvent event) {
        if (this.mTooltipInfo == null) {
            return false;
        }
        switch (event.getAction()) {
            case 7:
                if ((this.mViewFlags & 1073741824) == 1073741824 && (this.mViewFlags & 32) == 0) {
                    if (!this.mTooltipInfo.mTooltipFromLongClick) {
                        int timeout;
                        if (this.mTooltipInfo.mTooltipPopup == null) {
                            this.mTooltipInfo.mAnchorX = (int) event.getX();
                            this.mTooltipInfo.mAnchorY = (int) event.getY();
                            removeCallbacks(this.mTooltipInfo.mShowTooltipRunnable);
                            postDelayed(this.mTooltipInfo.mShowTooltipRunnable, (long) ViewConfiguration.getHoverTooltipShowTimeout());
                        }
                        if ((getWindowSystemUiVisibility() & 1) == 1) {
                            timeout = ViewConfiguration.getHoverTooltipHideShortTimeout();
                        } else {
                            timeout = ViewConfiguration.getHoverTooltipHideTimeout();
                        }
                        removeCallbacks(this.mTooltipInfo.mHideTooltipRunnable);
                        postDelayed(this.mTooltipInfo.mHideTooltipRunnable, (long) timeout);
                    }
                    return true;
                }
            case 10:
                if (!this.mTooltipInfo.mTooltipFromLongClick) {
                    -android_view_View-mthref-1();
                    break;
                }
                break;
        }
        return false;
    }

    void handleTooltipKey(KeyEvent event) {
        switch (event.getAction()) {
            case 0:
                if (event.getRepeatCount() == 0) {
                    -android_view_View-mthref-1();
                    return;
                }
                return;
            case 1:
                handleTooltipUp();
                return;
            default:
                return;
        }
    }

    private void handleTooltipUp() {
        if (this.mTooltipInfo != null && this.mTooltipInfo.mTooltipPopup != null) {
            removeCallbacks(this.mTooltipInfo.mHideTooltipRunnable);
            postDelayed(this.mTooltipInfo.mHideTooltipRunnable, (long) ViewConfiguration.getLongPressTooltipHideTimeout());
        }
    }

    private int getFocusableAttribute(TypedArray attributes) {
        int i = 0;
        TypedValue val = new TypedValue();
        if (!attributes.getValue(19, val)) {
            return 16;
        }
        if (val.type != 18) {
            return val.data;
        }
        if (val.data != 0) {
            i = 1;
        }
        return i;
    }

    public View getTooltipView() {
        if (this.mTooltipInfo == null || this.mTooltipInfo.mTooltipPopup == null) {
            return null;
        }
        return this.mTooltipInfo.mTooltipPopup.getContentView();
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
        return false;
    }

    public boolean isLongPressSwipe() {
        return false;
    }
}
