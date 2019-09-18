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
import android.content.res.TypedArray;
import android.freeform.HwFreeFormUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Interpolator;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManagerGlobal;
import android.net.wifi.WifiEnterpriseConfig;
import android.opengl.GLES10;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.SettingsStringUtil;
import android.rms.AppAssociate;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatProperty;
import android.util.Jlog;
import android.util.JlogConstants;
import android.util.Log;
import android.util.LongSparseLongArray;
import android.util.Pools;
import android.util.Property;
import android.util.SparseArray;
import android.util.StateSet;
import android.util.SuperNotCalledException;
import android.util.TypedValue;
import android.view.AccessibilityIterators;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.DisplayCutout;
import android.view.KeyEvent;
import android.view.SurfaceControl;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityEventSource;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.view.autofill.Helper;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ScrollBarDrawable;
import android.widget.SeekBar;
import com.android.internal.R;
import com.android.internal.view.TooltipPopup;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.widget.DecorCaptionView;
import com.android.internal.widget.ScrollBarUtils;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import java.lang.annotation.RCWeakRef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class View implements Drawable.Callback, KeyEvent.Callback, AccessibilityEventSource {
    public static final int ACCESSIBILITY_CURSOR_POSITION_UNDEFINED = -1;
    public static final int ACCESSIBILITY_LIVE_REGION_ASSERTIVE = 2;
    static final int ACCESSIBILITY_LIVE_REGION_DEFAULT = 0;
    public static final int ACCESSIBILITY_LIVE_REGION_NONE = 0;
    public static final int ACCESSIBILITY_LIVE_REGION_POLITE = 1;
    static final int ALL_RTL_PROPERTIES_RESOLVED = 1610678816;
    public static final Property<View, Float> ALPHA = new FloatProperty<View>(AppAssociate.ASSOC_WINDOW_ALPHA) {
        public void setValue(View object, float value) {
            object.setAlpha(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getAlpha());
        }
    };
    public static final int AUTOFILL_FLAG_INCLUDE_NOT_IMPORTANT_VIEWS = 1;
    private static final int[] AUTOFILL_HIGHLIGHT_ATTR = {16844136};
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
    @Deprecated
    public static final int DRAWING_CACHE_QUALITY_AUTO = 0;
    private static final int[] DRAWING_CACHE_QUALITY_FLAGS = {0, 524288, 1048576};
    @Deprecated
    public static final int DRAWING_CACHE_QUALITY_HIGH = 1048576;
    @Deprecated
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
    private static final int[] LAYOUT_DIRECTION_FLAGS = {0, 1, 2, 3};
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
    private static final int[] PFLAG2_TEXT_ALIGNMENT_FLAGS = {0, 8192, 16384, 24576, 32768, 40960, 49152};
    static final int PFLAG2_TEXT_ALIGNMENT_MASK = 57344;
    static final int PFLAG2_TEXT_ALIGNMENT_MASK_SHIFT = 13;
    static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED = 65536;
    private static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED_DEFAULT = 131072;
    static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK = 917504;
    static final int PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK_SHIFT = 17;
    private static final int[] PFLAG2_TEXT_DIRECTION_FLAGS = {0, 64, 128, 192, 256, 320, JlogConstants.JLID_ACTIVITY_START_RECORD_TIME, 448};
    static final int PFLAG2_TEXT_DIRECTION_MASK = 448;
    static final int PFLAG2_TEXT_DIRECTION_MASK_SHIFT = 6;
    static final int PFLAG2_TEXT_DIRECTION_RESOLVED = 512;
    static final int PFLAG2_TEXT_DIRECTION_RESOLVED_DEFAULT = 1024;
    static final int PFLAG2_TEXT_DIRECTION_RESOLVED_MASK = 7168;
    static final int PFLAG2_TEXT_DIRECTION_RESOLVED_MASK_SHIFT = 10;
    static final int PFLAG2_VIEW_QUICK_REJECTED = 268435456;
    private static final int PFLAG3_ACCESSIBILITY_HEADING = Integer.MIN_VALUE;
    private static final int PFLAG3_AGGREGATED_VISIBLE = 536870912;
    static final int PFLAG3_APPLYING_INSETS = 32;
    static final int PFLAG3_ASSIST_BLOCKED = 16384;
    private static final int PFLAG3_AUTOFILLID_EXPLICITLY_SET = 1073741824;
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
    private static final int PFLAG3_SCREEN_READER_FOCUSABLE = 268435456;
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
    private static final int PFLAG_NOTIFY_AUTOFILL_MANAGER_ON_CLICK = 536870912;
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
    protected static final String VIEW_LOG_TAG = "View";
    private static final int[] VISIBILITY_FLAGS = {0, 4, 8};
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
    private static final List<String> mLauncherList = Arrays.asList(new String[]{"com.android.launcher3", "com.huawei.android.launcher"});
    static long mPartialUpdateTimes = 0;
    static long mTotalAreaDecreasedDraw = 0;
    static long mTotalAreaDecreasedRefreshHeight = 0;
    static long mTotalFullViewAreaUpdate = 0;
    static long mTotalRefreshTimes = 0;
    private static final boolean mtouchEventOptEnable = SystemProperties.getBoolean("persist.huawei.touchevent_opt", false);
    private static boolean sAcceptZeroSizeDragShadow;
    private static boolean sAlwaysAssignFocus;
    private static boolean sAlwaysRemeasureExactly = false;
    private static boolean sAutoFocusableOffUIThreadWontNotifyParents;
    private static boolean sCanFocusZeroSized;
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
    static final ThreadLocal<Rect> sThreadLocal = new ThreadLocal<>();
    private static boolean sThrowOnInvalidFloatProperties;
    public static volatile boolean sTriggerFlag = false;
    /* access modifiers changed from: private */
    public static boolean sUseBrokenMakeMeasureSpec = false;
    private static boolean sUseDefaultFocusHighlight;
    static boolean sUseZeroUnspecifiedMeasureSpec = false;
    private int mAccessibilityCursorPosition;
    AccessibilityDelegate mAccessibilityDelegate;
    private CharSequence mAccessibilityPaneTitle;
    private int mAccessibilityTraversalAfterId;
    private int mAccessibilityTraversalBeforeId;
    private int mAccessibilityViewId;
    private ViewPropertyAnimator mAnimator;
    AttachInfo mAttachInfo;
    @ViewDebug.ExportedProperty(category = "attributes", hasAdjacentMapping = true)
    public String[] mAttributes;
    private String[] mAutofillHints;
    private AutofillId mAutofillId;
    private int mAutofillViewId;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "bg_")
    private Drawable mBackground;
    private RenderNode mBackgroundRenderNode;
    private int mBackgroundResource;
    private boolean mBackgroundSizeChanged;
    private TintInfo mBackgroundTint;
    @ViewDebug.ExportedProperty(category = "layout")
    protected int mBottom;
    public boolean mCachingFailed;
    private boolean mCanTouchInOtherThread;
    @ViewDebug.ExportedProperty(category = "drawing")
    Rect mClipBounds;
    private CharSequence mContentDescription;
    /* access modifiers changed from: protected */
    @ViewDebug.ExportedProperty(deepExport = true)
    public Context mContext;
    protected Animation mCurrentAnimation;
    final Rect mCurrentInvalRect;
    private Drawable mDefaultFocusHighlight;
    private Drawable mDefaultFocusHighlightCache;
    boolean mDefaultFocusHighlightEnabled;
    private boolean mDefaultFocusHighlightSizeChanged;
    private int[] mDrawableState;
    private Bitmap mDrawingCache;
    private int mDrawingCacheBackgroundColor;
    private ViewTreeObserver mFloatingTreeObserver;
    private boolean mForceRTL;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "fg_")
    private ForegroundInfo mForegroundInfo;
    private ArrayList<FrameMetricsObserver> mFrameMetricsObservers;
    GhostView mGhostView;
    /* access modifiers changed from: private */
    public boolean mHasPerformedLongPress;
    IHwApsImpl mHwApsImpl;
    @ViewDebug.ExportedProperty(resolveId = true)
    int mID;
    private boolean mIgnoreNextUpEvent;
    private boolean mInContextButtonPress;
    protected final InputEventConsistencyVerifier mInputEventConsistencyVerifier;
    boolean mIsPartialUpdateDebugOn;
    boolean mIsSupportPartialUpdate;
    private SparseArray<Object> mKeyedTags;
    /* access modifiers changed from: private */
    public int mLabelForId;
    private boolean mLastIsOpaque;
    Paint mLayerPaint;
    @ViewDebug.ExportedProperty(category = "drawing", mapping = {@ViewDebug.IntToString(from = 0, to = "NONE"), @ViewDebug.IntToString(from = 1, to = "SOFTWARE"), @ViewDebug.IntToString(from = 2, to = "HARDWARE")})
    int mLayerType;
    private Insets mLayoutInsets;
    protected ViewGroup.LayoutParams mLayoutParams;
    @ViewDebug.ExportedProperty(category = "layout")
    protected int mLeft;
    private boolean mLeftPaddingDefined;
    ListenerInfo mListenerInfo;
    private float mLongClickX;
    private float mLongClickY;
    private MatchIdPredicate mMatchIdPredicate;
    private MatchLabelForPredicate mMatchLabelForPredicate;
    private LongSparseLongArray mMeasureCache;
    @ViewDebug.ExportedProperty(category = "measurement")
    int mMeasuredHeight;
    @ViewDebug.ExportedProperty(category = "measurement")
    int mMeasuredWidth;
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mMinHeight;
    @ViewDebug.ExportedProperty(category = "measurement")
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
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingBottom;
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingLeft;
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingRight;
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingTop;
    @RCWeakRef
    protected ViewParent mParent;
    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap;
    private PerformClick mPerformClick;
    private PointerIcon mPointerIcon;
    @ViewDebug.ExportedProperty(flagMapping = {@ViewDebug.FlagToString(equals = 4096, mask = 4096, name = "FORCE_LAYOUT"), @ViewDebug.FlagToString(equals = 8192, mask = 8192, name = "LAYOUT_REQUIRED"), @ViewDebug.FlagToString(equals = 32768, mask = 32768, name = "DRAWING_CACHE_INVALID", outputIf = false), @ViewDebug.FlagToString(equals = 32, mask = 32, name = "DRAWN", outputIf = true), @ViewDebug.FlagToString(equals = 32, mask = 32, name = "NOT_DRAWN", outputIf = false), @ViewDebug.FlagToString(equals = 4194304, mask = 6291456, name = "DIRTY_OPAQUE"), @ViewDebug.FlagToString(equals = 2097152, mask = 6291456, name = "DIRTY")}, formatToHexString = true)
    public int mPrivateFlags;
    int mPrivateFlags2;
    int mPrivateFlags3;
    boolean mRecreateDisplayList;
    final RenderNode mRenderNode;
    private final Resources mResources;
    @ViewDebug.ExportedProperty(category = "layout")
    protected int mRight;
    private boolean mRightPaddingDefined;
    private RoundScrollbarRenderer mRoundScrollbarRenderer;
    private HandlerActionQueue mRunQueue;
    private ScrollabilityCache mScrollCache;
    private Drawable mScrollIndicatorDrawable;
    /* access modifiers changed from: protected */
    @ViewDebug.ExportedProperty(category = "scrolling")
    public int mScrollX;
    /* access modifiers changed from: protected */
    @ViewDebug.ExportedProperty(category = "scrolling")
    public int mScrollY;
    private SendViewScrolledAccessibilityEvent mSendViewScrolledAccessibilityEvent;
    private boolean mSendingHoverAccessibilityEvents;
    String mStartActivityRequestWho;
    private StateListAnimator mStateListAnimator;
    @ViewDebug.ExportedProperty(flagMapping = {@ViewDebug.FlagToString(equals = 1, mask = 1, name = "LOW_PROFILE"), @ViewDebug.FlagToString(equals = 2, mask = 2, name = "HIDE_NAVIGATION"), @ViewDebug.FlagToString(equals = 4, mask = 4, name = "FULLSCREEN"), @ViewDebug.FlagToString(equals = 256, mask = 256, name = "LAYOUT_STABLE"), @ViewDebug.FlagToString(equals = 512, mask = 512, name = "LAYOUT_HIDE_NAVIGATION"), @ViewDebug.FlagToString(equals = 1024, mask = 1024, name = "LAYOUT_FULLSCREEN"), @ViewDebug.FlagToString(equals = 2048, mask = 2048, name = "IMMERSIVE"), @ViewDebug.FlagToString(equals = 4096, mask = 4096, name = "IMMERSIVE_STICKY"), @ViewDebug.FlagToString(equals = 8192, mask = 8192, name = "LIGHT_STATUS_BAR"), @ViewDebug.FlagToString(equals = 16, mask = 16, name = "LIGHT_NAVIGATION_BAR"), @ViewDebug.FlagToString(equals = 65536, mask = 65536, name = "STATUS_BAR_DISABLE_EXPAND"), @ViewDebug.FlagToString(equals = 131072, mask = 131072, name = "STATUS_BAR_DISABLE_NOTIFICATION_ICONS"), @ViewDebug.FlagToString(equals = 262144, mask = 262144, name = "STATUS_BAR_DISABLE_NOTIFICATION_ALERTS"), @ViewDebug.FlagToString(equals = 524288, mask = 524288, name = "STATUS_BAR_DISABLE_NOTIFICATION_TICKER"), @ViewDebug.FlagToString(equals = 1048576, mask = 1048576, name = "STATUS_BAR_DISABLE_SYSTEM_INFO"), @ViewDebug.FlagToString(equals = 2097152, mask = 2097152, name = "STATUS_BAR_DISABLE_HOME"), @ViewDebug.FlagToString(equals = 4194304, mask = 4194304, name = "STATUS_BAR_DISABLE_BACK"), @ViewDebug.FlagToString(equals = 8388608, mask = 8388608, name = "STATUS_BAR_DISABLE_CLOCK"), @ViewDebug.FlagToString(equals = 16777216, mask = 16777216, name = "STATUS_BAR_DISABLE_RECENT"), @ViewDebug.FlagToString(equals = 33554432, mask = 33554432, name = "STATUS_BAR_DISABLE_SEARCH"), @ViewDebug.FlagToString(equals = 67108864, mask = 67108864, name = "STATUS_BAR_TRANSIENT"), @ViewDebug.FlagToString(equals = 134217728, mask = 134217728, name = "NAVIGATION_BAR_TRANSIENT"), @ViewDebug.FlagToString(equals = 268435456, mask = 268435456, name = "STATUS_BAR_UNHIDE"), @ViewDebug.FlagToString(equals = 536870912, mask = 536870912, name = "NAVIGATION_BAR_UNHIDE"), @ViewDebug.FlagToString(equals = 1073741824, mask = 1073741824, name = "STATUS_BAR_TRANSLUCENT"), @ViewDebug.FlagToString(equals = Integer.MIN_VALUE, mask = Integer.MIN_VALUE, name = "NAVIGATION_BAR_TRANSLUCENT"), @ViewDebug.FlagToString(equals = 32768, mask = 32768, name = "NAVIGATION_BAR_TRANSPARENT"), @ViewDebug.FlagToString(equals = 8, mask = 8, name = "STATUS_BAR_TRANSPARENT")}, formatToHexString = true)
    int mSystemUiVisibility;
    protected Object mTag;
    private int[] mTempNestedScrollConsumed;
    TooltipInfo mTooltipInfo;
    @ViewDebug.ExportedProperty(category = "layout")
    protected int mTop;
    private TouchDelegate mTouchDelegate;
    private int mTouchSlop;
    public TransformationInfo mTransformationInfo;
    int mTransientStateCount;
    private String mTransitionName;
    private Bitmap mUnscaledDrawingCache;
    private UnsetPressedState mUnsetPressedState;
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mUserPaddingBottom;
    @ViewDebug.ExportedProperty(category = "padding")
    int mUserPaddingEnd;
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mUserPaddingLeft;
    int mUserPaddingLeftInitial;
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mUserPaddingRight;
    int mUserPaddingRightInitial;
    @ViewDebug.ExportedProperty(category = "padding")
    int mUserPaddingStart;
    private float mVerticalScrollFactor;
    private int mVerticalScrollbarPosition;
    @ViewDebug.ExportedProperty(formatToHexString = true)
    int mViewFlags;
    private Handler mVisibilityChangeForAutofillHandler;
    int mWindowAttachCount;

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
        final DisplayCutout.ParcelableWrapper mDisplayCutout = new DisplayCutout.ParcelableWrapper(DisplayCutout.NO_CUTOUT);
        int mDisplayState = 0;
        public Surface mDragSurface;
        IBinder mDragToken;
        long mDrawingTime;
        List<View> mEmptyPartialLayoutViews;
        boolean mForceReportNewAttributes;
        final ViewTreeObserver.InternalInsetsInfo mGivenInternalInsets = new ViewTreeObserver.InternalInsetsInfo();
        int mGlobalSystemUiVisibility = -1;
        final Handler mHandler;
        boolean mHandlingPointerEvent;
        boolean mHardwareAccelerated;
        boolean mHardwareAccelerationRequested;
        boolean mHasNonEmptyGivenInternalInsets;
        boolean mHasSystemUiListeners;
        boolean mHasWindowFocus;
        IWindowId mIWindowId;
        boolean mIgnoreDirtyState;
        boolean mInTouchMode;
        final int[] mInvalidateChildLocation = new int[2];
        boolean mKeepScreenOn;
        final KeyEvent.DispatcherState mKeyDispatchState = new KeyEvent.DispatcherState();
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
        final ArrayList<View> mScrollContainers = new ArrayList<>();
        final IWindowSession mSession;
        boolean mSetIgnoreDirtyState = false;
        final Rect mStableInsets = new Rect();
        int mSystemUiVisibility;
        final ArrayList<View> mTempArrayList = new ArrayList<>(24);
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
            private static final Pools.SynchronizedPool<InvalidateInfo> sPool = new Pools.SynchronizedPool<>(10);
            int bottom;
            int left;
            int right;
            View target;
            int top;

            InvalidateInfo() {
            }

            public static InvalidateInfo obtain() {
                InvalidateInfo instance = sPool.acquire();
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface AutofillFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AutofillImportance {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AutofillType {
    }

    public static class BaseSavedState extends AbsSavedState {
        static final int AUTOFILL_ID = 4;
        public static final Parcelable.Creator<BaseSavedState> CREATOR = new Parcelable.ClassLoaderCreator<BaseSavedState>() {
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

        private CheckForLongPress() {
        }

        public void run() {
            if (this.mOriginalPressedState == View.this.isPressed() && View.this.mParent != null && this.mOriginalWindowAttachCount == View.this.mWindowAttachCount && !View.this.getRootView().isLongPressSwipe() && View.this.performLongClick(this.mX, this.mY)) {
                boolean unused = View.this.mHasPerformedLongPress = true;
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

        private CheckForTap() {
        }

        public void run() {
            View.this.mPrivateFlags &= -33554433;
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
            String idText;
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
            if (this.mHostView.getId() == -1) {
                idText = "";
            } else {
                idText = " with id '" + this.mHostView.getContext().getResources().getResourceEntryName(id) + "'";
            }
            throw new IllegalStateException("Could not find method " + this.mMethodName + "(View) in a parent or ancestor Context for android:onClick attribute defined on view " + this.mHostView.getClass() + idText);
        }
    }

    public static class DragShadowBuilder {
        private final WeakReference<View> mView;

        public DragShadowBuilder(View view) {
            this.mView = new WeakReference<>(view);
        }

        public DragShadowBuilder() {
            this.mView = new WeakReference<>(null);
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface DrawingCacheQuality {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface FindViewFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface FocusDirection {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface FocusRealDirection {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Focusable {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface FocusableMode {
    }

    private static class ForegroundInfo {
        /* access modifiers changed from: private */
        public boolean mBoundsChanged;
        /* access modifiers changed from: private */
        public Drawable mDrawable;
        /* access modifiers changed from: private */
        public int mGravity;
        /* access modifiers changed from: private */
        public boolean mInsidePadding;
        /* access modifiers changed from: private */
        public final Rect mOverlayBounds;
        /* access modifiers changed from: private */
        public final Rect mSelfBounds;
        /* access modifiers changed from: private */
        public TintInfo mTintInfo;

        private ForegroundInfo() {
            this.mGravity = 119;
            this.mInsidePadding = true;
            this.mBoundsChanged = true;
            this.mSelfBounds = new Rect();
            this.mOverlayBounds = new Rect();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface LayoutDir {
    }

    static class ListenerInfo {
        OnApplyWindowInsetsListener mOnApplyWindowInsetsListener;
        /* access modifiers changed from: private */
        public CopyOnWriteArrayList<OnAttachStateChangeListener> mOnAttachStateChangeListeners;
        OnCapturedPointerListener mOnCapturedPointerListener;
        public OnClickListener mOnClickListener;
        protected OnContextClickListener mOnContextClickListener;
        protected OnCreateContextMenuListener mOnCreateContextMenuListener;
        /* access modifiers changed from: private */
        public OnDragListener mOnDragListener;
        protected OnFocusChangeListener mOnFocusChangeListener;
        /* access modifiers changed from: private */
        public OnGenericMotionListener mOnGenericMotionListener;
        /* access modifiers changed from: private */
        public OnHoverListener mOnHoverListener;
        /* access modifiers changed from: private */
        public OnKeyListener mOnKeyListener;
        /* access modifiers changed from: private */
        public ArrayList<OnLayoutChangeListener> mOnLayoutChangeListeners;
        protected OnLongClickListener mOnLongClickListener;
        protected OnScrollChangeListener mOnScrollChangeListener;
        /* access modifiers changed from: private */
        public OnSystemUiVisibilityChangeListener mOnSystemUiVisibilityChangeListener;
        /* access modifiers changed from: private */
        public OnTouchListener mOnTouchListener;
        /* access modifiers changed from: private */
        public ArrayList<OnUnhandledKeyEventListener> mUnhandledKeyListeners;

        ListenerInfo() {
        }
    }

    private static class MatchIdPredicate implements Predicate<View> {
        public int mId;

        private MatchIdPredicate() {
        }

        public boolean test(View view) {
            return view.mID == this.mId;
        }
    }

    private static class MatchLabelForPredicate implements Predicate<View> {
        /* access modifiers changed from: private */
        public int mLabeledId;

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

        @Retention(RetentionPolicy.SOURCE)
        public @interface MeasureSpecMode {
        }

        public static int makeMeasureSpec(int size, int mode) {
            if (View.sUseBrokenMakeMeasureSpec) {
                return size + mode;
            }
            return (1073741823 & size) | (MODE_MASK & mode);
        }

        public static int makeSafeMeasureSpec(int size, int mode) {
            if (!View.sUseZeroUnspecifiedMeasureSpec || mode != 0) {
                return makeMeasureSpec(size, mode);
            }
            return 0;
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
                return makeMeasureSpec(size, 0);
            }
            int size2 = size + delta;
            if (size2 < 0) {
                Log.e(View.VIEW_LOG_TAG, "MeasureSpec.adjust: new size would be negative! (" + size2 + ") spec: " + toString(measureSpec) + " delta: " + delta);
                size2 = 0;
            }
            return makeMeasureSpec(size2, mode);
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
                sb.append(mode);
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            }
            sb.append(size);
            return sb.toString();
        }
    }

    public interface OnApplyWindowInsetsListener {
        WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets);
    }

    public interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View view);

        void onViewDetachedFromWindow(View view);
    }

    public interface OnCapturedPointerListener {
        boolean onCapturedPointer(View view, MotionEvent motionEvent);
    }

    public interface OnClickListener {
        void onClick(View view);
    }

    public interface OnContextClickListener {
        boolean onContextClick(View view);
    }

    public interface OnCreateContextMenuListener {
        void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo);
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

    public interface OnTouchListener {
        boolean onTouch(View view, MotionEvent motionEvent);
    }

    public interface OnUnhandledKeyEventListener {
        boolean onUnhandledKeyEvent(View view, KeyEvent keyEvent);
    }

    private final class PerformClick implements Runnable {
        private PerformClick() {
        }

        public void run() {
            Trace.traceBegin(8, "PerformClickInternal");
            boolean unused = View.this.performClickInternal();
            Trace.traceEnd(8);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ResolvedLayoutDir {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScrollBarStyle {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScrollIndicators {
    }

    private static class ScrollabilityCache implements Runnable {
        public static final int DRAGGING_HORIZONTAL_SCROLL_BAR = 2;
        public static final int DRAGGING_VERTICAL_SCROLL_BAR = 1;
        public static final int FADING = 2;
        public static final int NOT_DRAGGING = 0;
        public static final int OFF = 0;
        public static final int ON = 1;
        private static final float[] OPAQUE = {255.0f};
        private static final float[] TRANSPARENT = {0.0f};
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

        public ScrollabilityCache(ViewConfiguration configuration, View host2) {
            this.fadingEdgeLength = configuration.getScaledFadingEdgeLength();
            this.scrollBarSize = configuration.getScaledScrollBarSize();
            this.scrollBarMinTouchTarget = configuration.getScaledMinScrollbarTouchTarget();
            this.scrollBarDefaultDelayBeforeFade = ViewConfiguration.getScrollDefaultDelay();
            this.scrollBarFadeDuration = ViewConfiguration.getScrollBarFadeDuration();
            this.paint = new Paint();
            this.matrix = new Matrix();
            LinearGradient linearGradient = new LinearGradient(0.0f, 0.0f, 0.0f, 1.0f, -16777216, 0, Shader.TileMode.CLAMP);
            this.shader = linearGradient;
            this.paint.setShader(this.shader);
            this.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            this.host = host2;
        }

        public void setFadeColor(int color) {
            if (color != this.mLastColor) {
                this.mLastColor = color;
                if (color != 0) {
                    LinearGradient linearGradient = new LinearGradient(0.0f, 0.0f, 0.0f, 1.0f, color | -16777216, color & 16777215, Shader.TileMode.CLAMP);
                    this.shader = linearGradient;
                    this.paint.setShader(this.shader);
                    this.paint.setXfermode(null);
                    return;
                }
                LinearGradient linearGradient2 = new LinearGradient(0.0f, 0.0f, 0.0f, 1.0f, -16777216, 0, Shader.TileMode.CLAMP);
                this.shader = linearGradient2;
                this.paint.setShader(this.shader);
                this.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            }
        }

        public void run() {
            long now = AnimationUtils.currentAnimationTimeMillis();
            if (now >= this.fadeStartTime) {
                int nextFrame = (int) now;
                Interpolator interpolator = this.scrollBarInterpolator;
                interpolator.setKeyFrame(0, nextFrame, OPAQUE);
                interpolator.setKeyFrame(0 + 1, nextFrame + this.scrollBarFadeDuration, TRANSPARENT);
                this.state = 2;
                this.host.invalidate(true);
            }
        }
    }

    private class SendViewScrolledAccessibilityEvent implements Runnable {
        public int mDeltaX;
        public int mDeltaY;
        public volatile boolean mIsPending;

        private SendViewScrolledAccessibilityEvent() {
        }

        public void post(int dx, int dy) {
            this.mDeltaX += dx;
            this.mDeltaY += dy;
            if (!this.mIsPending) {
                this.mIsPending = true;
                View.this.postDelayed(this, ViewConfiguration.getSendRecurringAccessibilityEventsInterval());
            }
        }

        public void run() {
            if (AccessibilityManager.getInstance(View.this.mContext).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(4096);
                event.setScrollDeltaX(this.mDeltaX);
                event.setScrollDeltaY(this.mDeltaY);
                View.this.sendAccessibilityEventUnchecked(event);
            }
            reset();
        }

        /* access modifiers changed from: private */
        public void reset() {
            this.mIsPending = false;
            this.mDeltaX = 0;
            this.mDeltaY = 0;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TextAlignment {
    }

    static class TintInfo {
        boolean mHasTintList;
        boolean mHasTintMode;
        ColorStateList mTintList;
        PorterDuff.Mode mTintMode;

        TintInfo() {
        }
    }

    private static class TooltipInfo {
        int mAnchorX;
        int mAnchorY;
        Runnable mHideTooltipRunnable;
        int mHoverSlop;
        Runnable mShowTooltipRunnable;
        boolean mTooltipFromLongClick;
        TooltipPopup mTooltipPopup;
        CharSequence mTooltipText;

        private TooltipInfo() {
        }

        /* access modifiers changed from: private */
        public boolean updateAnchorPos(MotionEvent event) {
            int newAnchorX = (int) event.getX();
            int newAnchorY = (int) event.getY();
            if (Math.abs(newAnchorX - this.mAnchorX) <= this.mHoverSlop && Math.abs(newAnchorY - this.mAnchorY) <= this.mHoverSlop) {
                return false;
            }
            this.mAnchorX = newAnchorX;
            this.mAnchorY = newAnchorY;
            return true;
        }

        /* access modifiers changed from: private */
        public void clearAnchorPos() {
            this.mAnchorX = Integer.MAX_VALUE;
            this.mAnchorY = Integer.MAX_VALUE;
        }
    }

    static class TransformationInfo {
        @ViewDebug.ExportedProperty
        float mAlpha = 1.0f;
        /* access modifiers changed from: private */
        public Matrix mInverseMatrix;
        /* access modifiers changed from: private */
        public final Matrix mMatrix = new Matrix();
        float mTransitionAlpha = 1.0f;

        TransformationInfo() {
        }
    }

    private final class UnsetPressedState implements Runnable {
        private UnsetPressedState() {
        }

        public void run() {
            View.this.setPressed(false);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {
    }

    private static class VisibilityChangeForAutofillHandler extends Handler {
        private final AutofillManager mAfm;
        private final View mView;

        private VisibilityChangeForAutofillHandler(AutofillManager afm, View view) {
            this.mAfm = afm;
            this.mView = view;
        }

        public void handleMessage(Message msg) {
            this.mAfm.notifyViewVisibilityChanged(this.mView, this.mView.isShown());
        }
    }

    public void forceRTL(boolean flag) {
        this.mForceRTL = flag;
    }

    public View(Context context) {
        Resources resources = null;
        this.mCurrentAnimation = null;
        boolean z = false;
        this.mRecreateDisplayList = false;
        this.mID = -1;
        this.mAutofillViewId = -1;
        this.mAccessibilityViewId = -1;
        this.mAccessibilityCursorPosition = -1;
        this.mTag = null;
        this.mCurrentInvalRect = new Rect();
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
        this.mHwApsImpl = null;
        this.mIsSupportPartialUpdate = false;
        this.mIsPartialUpdateDebugOn = false;
        this.mContext = context;
        this.mResources = context != null ? context.getResources() : resources;
        this.mViewFlags = 402653200;
        this.mPrivateFlags2 = 140296;
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setOverScrollMode(1);
        this.mUserPaddingStart = Integer.MIN_VALUE;
        this.mUserPaddingEnd = Integer.MIN_VALUE;
        this.mRenderNode = RenderNode.create(getClass().getName(), this);
        if (!sCompatibilityDone && context != null) {
            int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
            sUseBrokenMakeMeasureSpec = targetSdkVersion <= 17;
            sIgnoreMeasureCache = targetSdkVersion < 19;
            Canvas.sCompatibilityRestore = targetSdkVersion < 23;
            Canvas.sCompatibilitySetBitmap = targetSdkVersion < 26;
            Canvas.setCompatibilityVersion(targetSdkVersion);
            sUseZeroUnspecifiedMeasureSpec = targetSdkVersion < 23;
            sAlwaysRemeasureExactly = targetSdkVersion <= 23;
            sLayoutParamsAlwaysChanged = targetSdkVersion <= 23;
            sTextureViewIgnoresDrawableSetters = targetSdkVersion <= 23;
            sPreserveMarginParamsInLayoutParamConversion = targetSdkVersion >= 24;
            sCascadedDragDrop = targetSdkVersion < 24;
            sHasFocusableExcludeAutoFocusable = targetSdkVersion < 26;
            sAutoFocusableOffUIThreadWontNotifyParents = targetSdkVersion < 26;
            sUseDefaultFocusHighlight = context.getResources().getBoolean(17957058);
            sThrowOnInvalidFloatProperties = targetSdkVersion >= 28;
            sCanFocusZeroSized = targetSdkVersion < 28;
            sAlwaysAssignFocus = targetSdkVersion < 28;
            sAcceptZeroSizeDragShadow = targetSdkVersion < 28 ? true : z;
            sCompatibilityDone = true;
        }
    }

    public View(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public View(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v3, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v17, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v5, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r18v3, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v5, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r19v4, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v4, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v5, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r19v5, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v6, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r18v4, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r50v3, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r21v4, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r19v8, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r18v6, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v8, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v8, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v12, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r19v9, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r18v7, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v9, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r16v9, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v6, resolved type: int} */
    /* JADX WARNING: type inference failed for: r0v187, types: [java.lang.CharSequence[]] */
    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x048c, code lost:
        r40 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0601, code lost:
        r30 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:209:0x0873, code lost:
        if (r3 >= 14) goto L_0x0a0d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:219:0x08bd, code lost:
        r40 = r0;
        r51 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:223:0x08dd, code lost:
        r40 = r9;
        r51 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x0919, code lost:
        r40 = r0;
        r51 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x0920, code lost:
        r51 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:233:0x0922, code lost:
        r10 = r55;
        r13 = r62;
        r15 = r64;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:242:0x09e1, code lost:
        r10 = r55;
        r13 = r62;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:252:0x0a2f, code lost:
        r10 = r55;
        r13 = r62;
        r15 = r64;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x014d, code lost:
        r60 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x014f, code lost:
        r62 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0269, code lost:
        r64 = r15;
        r10 = r40;
        r13 = r51;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x031d, code lost:
        r64 = r15;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    public View(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context);
        int rightPadding;
        int rightPadding2;
        int leftPadding;
        int paddingHorizontal;
        int x;
        float sx;
        int N;
        int x2;
        boolean rightPaddingDefined;
        int padding;
        int y;
        int padding2;
        int y2;
        int viewFlagValues;
        int viewFlagMasks;
        int padding3;
        int y3;
        int x3;
        int padding4;
        int viewFlagMasks2;
        int viewFlagValues2;
        int y4;
        int viewFlagValues3;
        int viewFlagMasks3;
        int viewFlagValues4;
        boolean transformSet;
        int y5;
        int x4;
        int y6;
        int padding5;
        String[] strArr;
        String rawString;
        Context context2 = context;
        AttributeSet attributeSet = attrs;
        TypedArray a = context2.obtainStyledAttributes(attributeSet, R.styleable.View, defStyleAttr, defStyleRes);
        if (mDebugViewAttributes) {
            saveAttributeData(attributeSet, a);
        }
        int startPadding = Integer.MIN_VALUE;
        int endPadding = Integer.MIN_VALUE;
        int rawHintNum = -1;
        boolean transformSet2 = false;
        int scrollbarStyle = 0;
        boolean initializeScrollbars = false;
        boolean initializeScrollIndicators = false;
        boolean startPaddingDefined = false;
        boolean endPaddingDefined = false;
        int overScrollMode = this.mOverScrollMode;
        int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        int N2 = a.getIndexCount();
        int viewFlagValues5 = 0 | 16;
        int topPadding = -1;
        int paddingHorizontal2 = -1;
        int viewFlagMasks4 = 0 | 16;
        float tx = 0.0f;
        float ty = 0.0f;
        float tz = 0.0f;
        float elevation = 0.0f;
        float rotation = 0.0f;
        float rotationX = 0.0f;
        float rotationY = 0.0f;
        float sx2 = 1.0f;
        float sy = 1.0f;
        Drawable background = null;
        boolean leftPaddingDefined = false;
        int viewFlagValues6 = 0;
        int leftPadding2 = -1;
        int paddingVertical = -1;
        boolean rightPaddingDefined2 = false;
        int overScrollMode2 = overScrollMode;
        int rightPadding3 = -1;
        int rightPadding4 = 0;
        int bottomPadding = -1;
        int viewFlagValues7 = 0;
        boolean setScrollContainer = false;
        while (true) {
            int i = viewFlagValues6;
            if (i < N2) {
                N = N2;
                int attr = a.getIndex(i);
                switch (attr) {
                    case 8:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        int y7 = viewFlagValues5;
                        int padding6 = viewFlagMasks4;
                        int scrollbarStyle2 = a.getInt(attr, 0);
                        if (scrollbarStyle2 != 0) {
                            y7 |= 50331648 & scrollbarStyle2;
                            padding6 |= 50331648;
                        }
                        scrollbarStyle = scrollbarStyle2;
                    case 9:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mID = a.getResourceId(attr, -1);
                    case 10:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mTag = a.getText(attr);
                    case 11:
                        int x5 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z = rightPaddingDefined2;
                        int y8 = viewFlagValues5;
                        int padding7 = viewFlagMasks4;
                        x3 = a.getDimensionPixelOffset(attr, 0);
                        break;
                    case 12:
                        x2 = rightPadding4;
                        int i2 = viewFlagValues7;
                        int padding8 = rawHintNum;
                        boolean z2 = rightPaddingDefined2;
                        int y9 = viewFlagValues5;
                        int padding9 = viewFlagMasks4;
                        rawHintNum = padding8;
                        viewFlagValues7 = a.getDimensionPixelOffset(attr, 0);
                        break;
                    case 13:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z3 = rightPaddingDefined2;
                        int y10 = viewFlagValues5;
                        int padding10 = viewFlagMasks4;
                        background = a.getDrawable(attr);
                        break;
                    case 14:
                        x2 = rightPadding4;
                        int y11 = viewFlagValues7;
                        int i3 = rawHintNum;
                        boolean z4 = rightPaddingDefined2;
                        int y12 = viewFlagValues5;
                        int padding11 = viewFlagMasks4;
                        int padding12 = a.getDimensionPixelSize(attr, -1);
                        this.mUserPaddingLeftInitial = padding12;
                        this.mUserPaddingRightInitial = padding12;
                        rightPaddingDefined2 = true;
                        viewFlagValues7 = y11;
                        rawHintNum = padding12;
                        leftPaddingDefined = true;
                        break;
                    case 15:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z5 = rightPaddingDefined2;
                        int y13 = viewFlagValues5;
                        int padding13 = viewFlagMasks4;
                        int leftPadding3 = a.getDimensionPixelSize(attr, -1);
                        this.mUserPaddingLeftInitial = leftPadding3;
                        leftPadding2 = leftPadding3;
                        leftPaddingDefined = true;
                        break;
                    case 16:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z6 = rightPaddingDefined2;
                        int y14 = viewFlagValues5;
                        int padding14 = viewFlagMasks4;
                        topPadding = a.getDimensionPixelSize(attr, -1);
                        break;
                    case 17:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z7 = rightPaddingDefined2;
                        int y15 = viewFlagValues5;
                        int padding15 = viewFlagMasks4;
                        int rightPadding5 = a.getDimensionPixelSize(attr, -1);
                        this.mUserPaddingRightInitial = rightPadding5;
                        rightPadding3 = rightPadding5;
                        rightPaddingDefined2 = true;
                        break;
                    case 18:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z8 = rightPaddingDefined2;
                        int y16 = viewFlagValues5;
                        int padding16 = viewFlagMasks4;
                        bottomPadding = a.getDimensionPixelSize(attr, -1);
                        break;
                    case 19:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        padding4 = viewFlagMasks4;
                        viewFlagValues2 = (viewFlagValues5 & -18) | getFocusableAttribute(a);
                        if ((viewFlagValues2 & 16) != 0) {
                            viewFlagValues5 = viewFlagValues2;
                            break;
                        } else {
                            viewFlagMasks2 = padding4 | 17;
                            break;
                        }
                    case 20:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (a.getBoolean(attr, false)) {
                            viewFlagValues2 = (y2 & -17) | 262145;
                            viewFlagMasks2 = 262161 | padding2;
                            break;
                        }
                    case 21:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        int visibility = a.getInt(attr, 0);
                        if (visibility != 0) {
                            viewFlagValues3 = VISIBILITY_FLAGS[visibility] | y2;
                            y4 = padding2 | 12;
                            break;
                        }
                    case 22:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (a.getBoolean(attr, false)) {
                            viewFlagValues4 = y2 | 2;
                            viewFlagMasks3 = padding2 | 2;
                            break;
                        }
                    case 23:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        int scrollbars = a.getInt(attr, 0);
                        if (scrollbars != 0) {
                            viewFlagValues5 = y2 | scrollbars;
                            viewFlagMasks4 = padding2 | 768;
                            initializeScrollbars = true;
                            break;
                        }
                    case 24:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        break;
                    case 26:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mNextFocusLeftId = a.getResourceId(attr, -1);
                    case 27:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mNextFocusRightId = a.getResourceId(attr, -1);
                    case 28:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mNextFocusUpId = a.getResourceId(attr, -1);
                    case 29:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mNextFocusDownId = a.getResourceId(attr, -1);
                    case 30:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (a.getBoolean(attr, false)) {
                            viewFlagValues2 = y2 | 16384;
                            viewFlagMasks2 = padding2 | 16384;
                            break;
                        }
                    case 31:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (a.getBoolean(attr, false)) {
                            viewFlagValues4 = 2097152 | y2;
                            viewFlagMasks3 = 2097152 | padding2;
                            break;
                        }
                    case 32:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (!a.getBoolean(attr, true)) {
                            viewFlagValues2 = 65536 | y2;
                            viewFlagMasks2 = 65536 | padding2;
                            break;
                        }
                    case 33:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        int cacheQuality = a.getInt(attr, 0);
                        if (cacheQuality != 0) {
                            viewFlagValues3 = DRAWING_CACHE_QUALITY_FLAGS[cacheQuality] | y2;
                            y4 = DRAWING_CACHE_QUALITY_MASK | padding2;
                            break;
                        }
                    case 34:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (a.getBoolean(attr, false)) {
                            viewFlagMasks = 4194304 | padding2;
                            viewFlagValues = y2 | 4194304;
                            break;
                        }
                    case 35:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (targetSdkVersion >= 23 || (this instanceof FrameLayout)) {
                            setForeground(a.getDrawable(attr));
                        }
                    case 36:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mMinWidth = a.getDimensionPixelSize(attr, 0);
                    case 37:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mMinHeight = a.getDimensionPixelSize(attr, 0);
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 38:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (targetSdkVersion >= 23 || (this instanceof FrameLayout)) {
                            setForegroundGravity(a.getInt(attr, 0));
                            viewFlagValues = y2;
                            viewFlagMasks = padding2;
                            break;
                        }
                    case 39:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (!a.getBoolean(attr, true)) {
                            viewFlagValues2 = -134217729 & y2;
                            viewFlagMasks2 = 134217728 | padding2;
                            break;
                        }
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 40:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (a.getBoolean(attr, false)) {
                            viewFlagMasks4 = 67108864 | padding2;
                            viewFlagValues5 = y2 | 67108864;
                            break;
                        }
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 41:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (a.getBoolean(attr, false)) {
                            setScrollContainer(true);
                        }
                        setScrollContainer = true;
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 42:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (!a.getBoolean(attr, true)) {
                            viewFlagValues2 = -268435457 & y2;
                            viewFlagMasks2 = 268435456 | padding2;
                            break;
                        }
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 43:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (!context.isRestricted()) {
                            String handlerName = a.getString(attr);
                            if (handlerName != null) {
                                setOnClickListener(new DeclaredOnClickListener(this, handlerName));
                            }
                            viewFlagValues = y2;
                            viewFlagMasks = padding2;
                            break;
                        } else {
                            throw new IllegalStateException("The android:onClick attribute cannot be used within a restricted context");
                        }
                    case 44:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setContentDescription(a.getString(attr));
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 48:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z9 = rightPaddingDefined2;
                        int y17 = viewFlagValues5;
                        int padding17 = viewFlagMasks4;
                        overScrollMode2 = a.getInt(attr, 1);
                        break;
                    case 49:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (a.getBoolean(attr, false)) {
                            viewFlagValues2 = y2 | 1024;
                            viewFlagMasks2 = padding2 | 1024;
                            break;
                        }
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 50:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setAlpha(a.getFloat(attr, 1.0f));
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 51:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setPivotX(a.getDimension(attr, 0.0f));
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 52:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setPivotY(a.getDimension(attr, 0.0f));
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 53:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z10 = rightPaddingDefined2;
                        int y18 = viewFlagValues5;
                        int padding18 = viewFlagMasks4;
                        transformSet = true;
                        tx = a.getDimension(attr, 0.0f);
                        break;
                    case 54:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z11 = rightPaddingDefined2;
                        int y19 = viewFlagValues5;
                        int padding19 = viewFlagMasks4;
                        transformSet = true;
                        ty = a.getDimension(attr, 0.0f);
                        break;
                    case 55:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z12 = rightPaddingDefined2;
                        int y20 = viewFlagValues5;
                        int padding20 = viewFlagMasks4;
                        transformSet = true;
                        sx2 = a.getFloat(attr, 1.0f);
                        break;
                    case 56:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z13 = rightPaddingDefined2;
                        int y21 = viewFlagValues5;
                        int padding21 = viewFlagMasks4;
                        transformSet = true;
                        sy = a.getFloat(attr, 1.0f);
                        break;
                    case 57:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z14 = rightPaddingDefined2;
                        int y22 = viewFlagValues5;
                        int padding22 = viewFlagMasks4;
                        transformSet = true;
                        rotation = a.getFloat(attr, 0.0f);
                        break;
                    case 58:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z15 = rightPaddingDefined2;
                        int y23 = viewFlagValues5;
                        int padding23 = viewFlagMasks4;
                        transformSet = true;
                        rotationX = a.getFloat(attr, 0.0f);
                        break;
                    case 59:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z16 = rightPaddingDefined2;
                        int y24 = viewFlagValues5;
                        int padding24 = viewFlagMasks4;
                        transformSet = true;
                        rotationY = a.getFloat(attr, 0.0f);
                        break;
                    case 60:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mVerticalScrollbarPosition = a.getInt(attr, 0);
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 61:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mNextFocusForwardId = a.getResourceId(attr, -1);
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 62:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setLayerType(a.getInt(attr, 0), null);
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 63:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        int fadingEdge = a.getInt(attr, 0);
                        if (fadingEdge != 0) {
                            viewFlagValues3 = y2 | fadingEdge;
                            y4 = padding2 | 12288;
                            initializeFadingEdgeInternal(a);
                            break;
                        }
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 64:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setImportantForAccessibility(a.getInt(attr, 0));
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 65:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mPrivateFlags2 &= -449;
                        int textDirection = a.getInt(attr, -1);
                        if (textDirection != -1) {
                            this.mPrivateFlags2 |= PFLAG2_TEXT_DIRECTION_FLAGS[textDirection];
                        }
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 66:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mPrivateFlags2 &= -57345;
                        this.mPrivateFlags2 |= PFLAG2_TEXT_ALIGNMENT_FLAGS[a.getInt(attr, 1)];
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 67:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        this.mPrivateFlags2 &= -61;
                        int layoutDirection = a.getInt(attr, -1);
                        this.mPrivateFlags2 |= (layoutDirection != -1 ? LAYOUT_DIRECTION_FLAGS[layoutDirection] : 2) << 2;
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 68:
                        x4 = rightPadding4;
                        y6 = viewFlagValues7;
                        padding5 = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y5 = viewFlagValues5;
                        padding4 = viewFlagMasks4;
                        int x6 = a.getDimensionPixelSize(attr, Integer.MIN_VALUE);
                        startPaddingDefined = x6 != Integer.MIN_VALUE;
                        startPadding = x6;
                        break;
                    case 69:
                        x4 = rightPadding4;
                        y6 = viewFlagValues7;
                        padding5 = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y5 = viewFlagValues5;
                        padding4 = viewFlagMasks4;
                        int endPadding2 = a.getDimensionPixelSize(attr, Integer.MIN_VALUE);
                        endPaddingDefined = endPadding2 != Integer.MIN_VALUE;
                        endPadding = endPadding2;
                        break;
                    case 70:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setLabelFor(a.getResourceId(attr, -1));
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 71:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setAccessibilityLiveRegion(a.getInt(attr, 0));
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 72:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z17 = rightPaddingDefined2;
                        int y25 = viewFlagValues5;
                        int padding25 = viewFlagMasks4;
                        transformSet = true;
                        tz = a.getDimension(attr, 0.0f);
                        break;
                    case 73:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setTransitionName(a.getString(attr));
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 74:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setNestedScrollingEnabled(a.getBoolean(attr, false));
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 75:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        boolean z18 = rightPaddingDefined2;
                        int y26 = viewFlagValues5;
                        int padding26 = viewFlagMasks4;
                        transformSet = true;
                        elevation = a.getDimension(attr, 0.0f);
                        break;
                    case 76:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setStateListAnimator(AnimatorInflater.loadStateListAnimator(context2, a.getResourceId(attr, 0)));
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 77:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (this.mBackgroundTint == null) {
                            this.mBackgroundTint = new TintInfo();
                        }
                        this.mBackgroundTint.mTintList = a.getColorStateList(77);
                        this.mBackgroundTint.mHasTintList = true;
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 78:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (this.mBackgroundTint == null) {
                            this.mBackgroundTint = new TintInfo();
                        }
                        rightPaddingDefined = rightPaddingDefined2;
                        this.mBackgroundTint.mTintMode = Drawable.parseTintMode(a.getInt(78, -1), null);
                        this.mBackgroundTint.mHasTintMode = true;
                        viewFlagValues = y2;
                        viewFlagMasks = padding2;
                        break;
                    case 79:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (targetSdkVersion >= 23 || (this instanceof FrameLayout)) {
                            setForegroundTintList(a.getColorStateList(attr));
                            break;
                        }
                    case 80:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        if (targetSdkVersion >= 23 || (this instanceof FrameLayout)) {
                            setForegroundTintMode(Drawable.parseTintMode(a.getInt(attr, -1), null));
                            break;
                        }
                    case 81:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setOutlineProviderFromAttribute(a.getInt(81, 0));
                        break;
                    case 82:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setAccessibilityTraversalBefore(a.getResourceId(attr, -1));
                        break;
                    case 83:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        setAccessibilityTraversalAfter(a.getResourceId(attr, -1));
                        break;
                    case 84:
                        x3 = rightPadding4;
                        y3 = viewFlagValues7;
                        padding3 = rawHintNum;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                        int scrollIndicators = (a.getInt(attr, 0) << 8) & SCROLL_INDICATORS_PFLAG3_MASK;
                        if (scrollIndicators != 0) {
                            this.mPrivateFlags3 |= scrollIndicators;
                            initializeScrollIndicators = true;
                            viewFlagValues5 = y2;
                            viewFlagMasks4 = padding2;
                            break;
                        }
                        break;
                    case 85:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        if (!a.getBoolean(attr, false)) {
                            y2 = viewFlagValues5;
                            padding2 = viewFlagMasks4;
                            rightPaddingDefined = rightPaddingDefined2;
                            viewFlagValues = y2;
                            viewFlagMasks = padding2;
                            break;
                        } else {
                            viewFlagMasks4 = 8388608 | viewFlagMasks4;
                            viewFlagValues5 |= 8388608;
                            viewFlagValues7 = y;
                            rawHintNum = padding;
                            break;
                        }
                    case 86:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        int resourceId = a.getResourceId(attr, 0);
                        if (resourceId == 0) {
                            int pointerType = a.getInt(attr, 1);
                            if (pointerType != 1) {
                                setPointerIcon(PointerIcon.getSystemIcon(context2, pointerType));
                                break;
                            }
                        } else {
                            setPointerIcon(PointerIcon.load(context.getResources(), resourceId));
                            break;
                        }
                        break;
                    case 87:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        if (a.peekValue(attr) != null) {
                            forceHasOverlappingRendering(a.getBoolean(attr, true));
                            break;
                        }
                        break;
                    case 88:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        setTooltipText(a.getText(attr));
                        break;
                    case 89:
                        x2 = rightPadding4;
                        int i4 = rawHintNum;
                        int paddingHorizontal3 = a.getDimensionPixelSize(attr, -1);
                        this.mUserPaddingLeftInitial = paddingHorizontal3;
                        this.mUserPaddingRightInitial = paddingHorizontal3;
                        paddingHorizontal2 = paddingHorizontal3;
                        leftPaddingDefined = true;
                        rightPaddingDefined2 = true;
                        viewFlagValues7 = viewFlagValues7;
                        break;
                    case 90:
                        x2 = rightPadding4;
                        int i5 = viewFlagValues7;
                        int i6 = rawHintNum;
                        paddingVertical = a.getDimensionPixelSize(attr, -1);
                        break;
                    case 91:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        if (a.peekValue(attr) != null) {
                            setKeyboardNavigationCluster(a.getBoolean(attr, true));
                            break;
                        }
                        break;
                    case 92:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        this.mNextClusterForwardId = a.getResourceId(attr, -1);
                        break;
                    case 93:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        if (a.peekValue(attr) != null) {
                            setFocusedByDefault(a.getBoolean(attr, true));
                            break;
                        }
                        break;
                    case 94:
                        y = viewFlagValues7;
                        if (a.peekValue(attr) == null) {
                            x2 = rightPadding4;
                            padding = rawHintNum;
                            rightPaddingDefined = rightPaddingDefined2;
                            y2 = viewFlagValues5;
                            padding2 = viewFlagMasks4;
                            viewFlagValues = y2;
                            viewFlagMasks = padding2;
                            break;
                        } else {
                            String rawString2 = null;
                            if (a.getType(attr) == 1) {
                                int resId = a.getResourceId(attr, 0);
                                try {
                                    strArr = a.getTextArray(attr);
                                } catch (Resources.NotFoundException e) {
                                    Resources.NotFoundException notFoundException = e;
                                    Resources.NotFoundException notFoundException2 = e;
                                    rawString2 = getResources().getString(resId);
                                    strArr = null;
                                }
                                rawString = rawString2;
                            } else {
                                rawString = a.getString(attr);
                                strArr = null;
                            }
                            if (strArr != null) {
                                String[] strArr2 = strArr;
                            } else if (rawString != null) {
                                String[] strArr3 = strArr;
                                strArr = rawString.split(",");
                                String str = rawString;
                            } else {
                                String[] strArr4 = strArr;
                                String str2 = rawString;
                                throw new IllegalArgumentException("Could not resolve autofillHints");
                            }
                            String[] hints = new String[strArr.length];
                            x2 = rightPadding4;
                            int numHints = strArr.length;
                            int rawHintNum2 = 0;
                            while (true) {
                                padding = rawHintNum;
                                int rawHintNum3 = rawHintNum2;
                                if (rawHintNum3 >= numHints) {
                                    setAutofillHints(hints);
                                    break;
                                } else {
                                    hints[rawHintNum3] = strArr[rawHintNum3].toString().trim();
                                    rawHintNum2 = rawHintNum3 + 1;
                                    rawHintNum = padding;
                                    numHints = numHints;
                                }
                            }
                        }
                    case 95:
                        y = viewFlagValues7;
                        if (a.peekValue(attr) != null) {
                            setImportantForAutofill(a.getInt(attr, 0));
                        }
                    case 96:
                        y = viewFlagValues7;
                        if (a.peekValue(attr) != null) {
                            setDefaultFocusHighlightEnabled(a.getBoolean(attr, true));
                        }
                    case 97:
                        y = viewFlagValues7;
                        if (a.peekValue(attr) != null) {
                            setScreenReaderFocusable(a.getBoolean(attr, false));
                        }
                    case 98:
                        y = viewFlagValues7;
                        if (a.peekValue(attr) != null) {
                            setAccessibilityPaneTitle(a.getString(attr));
                        }
                    case 99:
                        y = viewFlagValues7;
                        setAccessibilityHeading(a.getBoolean(attr, false));
                    case 100:
                        y = viewFlagValues7;
                        setOutlineSpotShadowColor(a.getColor(attr, -16777216));
                    case 101:
                        y = viewFlagValues7;
                        setOutlineAmbientShadowColor(a.getColor(attr, -16777216));
                    case 105:
                        if (targetSdkVersion < 23 && !(this instanceof FrameLayout)) {
                            x2 = rightPadding4;
                            y = viewFlagValues7;
                            break;
                        } else {
                            if (this.mForegroundInfo == null) {
                                y = viewFlagValues7;
                                this.mForegroundInfo = new ForegroundInfo();
                            } else {
                                y = viewFlagValues7;
                            }
                            boolean unused = this.mForegroundInfo.mInsidePadding = a.getBoolean(attr, this.mForegroundInfo.mInsidePadding);
                        }
                        break;
                    default:
                        x2 = rightPadding4;
                        y = viewFlagValues7;
                        padding = rawHintNum;
                        rightPaddingDefined = rightPaddingDefined2;
                        y2 = viewFlagValues5;
                        padding2 = viewFlagMasks4;
                }
            } else {
                int x7 = rightPadding4;
                int y27 = viewFlagValues7;
                int padding27 = rawHintNum;
                boolean rightPaddingDefined3 = rightPaddingDefined2;
                int y28 = viewFlagValues5;
                int padding28 = viewFlagMasks4;
                setOverScrollMode(overScrollMode2);
                this.mUserPaddingStart = startPadding;
                this.mUserPaddingEnd = endPadding;
                if (background != null) {
                    setBackground(background);
                }
                this.mLeftPaddingDefined = leftPaddingDefined;
                this.mRightPaddingDefined = rightPaddingDefined3;
                if (padding27 >= 0) {
                    leftPadding = padding27;
                    topPadding = padding27;
                    bottomPadding = padding27;
                    int padding29 = padding27;
                    this.mUserPaddingLeftInitial = padding29;
                    this.mUserPaddingRightInitial = padding29;
                    rightPadding = padding27;
                    rightPadding2 = paddingHorizontal2;
                } else {
                    rightPadding2 = paddingHorizontal2;
                    if (rightPadding2 >= 0) {
                        leftPadding2 = rightPadding2;
                        rightPadding3 = rightPadding2;
                        this.mUserPaddingLeftInitial = rightPadding2;
                        this.mUserPaddingRightInitial = rightPadding2;
                    }
                    leftPadding = leftPadding2;
                    if (paddingVertical >= 0) {
                        topPadding = paddingVertical;
                        bottomPadding = paddingVertical;
                    }
                    rightPadding = rightPadding3;
                }
                if (isRtlCompatibilityMode()) {
                    if (!this.mLeftPaddingDefined && startPaddingDefined) {
                        leftPadding = startPadding;
                    }
                    this.mUserPaddingLeftInitial = leftPadding >= 0 ? leftPadding : this.mUserPaddingLeftInitial;
                    if (!this.mRightPaddingDefined && endPaddingDefined) {
                        rightPadding = endPadding;
                    }
                    this.mUserPaddingRightInitial = rightPadding >= 0 ? rightPadding : this.mUserPaddingRightInitial;
                    int i7 = targetSdkVersion;
                } else {
                    boolean hasRelativePadding = startPaddingDefined || endPaddingDefined;
                    int i8 = targetSdkVersion;
                    if (this.mLeftPaddingDefined != 0 && !hasRelativePadding) {
                        this.mUserPaddingLeftInitial = leftPadding;
                    }
                    if (this.mRightPaddingDefined && !hasRelativePadding) {
                        this.mUserPaddingRightInitial = rightPadding;
                    }
                }
                int i9 = this.mUserPaddingLeftInitial;
                int i10 = topPadding >= 0 ? topPadding : this.mPaddingTop;
                int i11 = leftPadding;
                int leftPadding4 = this.mUserPaddingRightInitial;
                if (bottomPadding >= 0) {
                    int i12 = rightPadding2;
                    paddingHorizontal = bottomPadding;
                } else {
                    int i13 = rightPadding2;
                    paddingHorizontal = this.mPaddingBottom;
                }
                internalSetPadding(i9, i10, leftPadding4, paddingHorizontal);
                if (padding28 != 0) {
                    setFlags(y28, padding28);
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
                if (x7 == 0 && y27 == 0) {
                    int i14 = y27;
                    x = x7;
                } else {
                    x = x7;
                    scrollTo(x, y27);
                }
                if (transformSet2) {
                    setTranslationX(tx);
                    int i15 = x;
                    float ty2 = ty;
                    setTranslationY(ty2);
                    float f = ty2;
                    float ty3 = tz;
                    setTranslationZ(ty3);
                    float f2 = ty3;
                    float tz2 = elevation;
                    setElevation(tz2);
                    float f3 = tz2;
                    float rotation2 = rotation;
                    setRotation(rotation2);
                    float f4 = rotation2;
                    float rotationX2 = rotationX;
                    setRotationX(rotationX2);
                    float f5 = rotationX2;
                    float rotationY2 = rotationY;
                    setRotationY(rotationY2);
                    float f6 = rotationY2;
                    float sx3 = sx2;
                    setScaleX(sx3);
                    float f7 = sx3;
                    sx = sy;
                    setScaleY(sx);
                } else {
                    float f8 = tx;
                    float f9 = ty;
                    float f10 = sx2;
                    sx = sy;
                    float f11 = rotation;
                    float f12 = rotationX;
                    float f13 = rotationY;
                    float f14 = tz;
                    float f15 = elevation;
                }
                if (!setScrollContainer && (y28 & 512) != 0) {
                    setScrollContainer(true);
                }
                computeOpaqueFlags();
                this.mHwApsImpl = HwFrameworkFactory.getHwApsImpl();
                if (this.mHwApsImpl != null) {
                    float f16 = sx;
                    this.mIsSupportPartialUpdate = this.mHwApsImpl.isSupportApsPartialUpdate() && !(this instanceof SeekBar);
                    this.mIsPartialUpdateDebugOn = this.mHwApsImpl.isDebugPartialUpdateOn();
                    return;
                }
                float sy2 = sx;
                return;
            }
            viewFlagValues6 = i + 1;
            N2 = N;
            rightPadding4 = x2;
        }
    }

    View() {
        this.mCurrentAnimation = null;
        this.mRecreateDisplayList = false;
        this.mID = -1;
        this.mAutofillViewId = -1;
        this.mAccessibilityViewId = -1;
        this.mAccessibilityCursorPosition = -1;
        this.mTag = null;
        this.mCurrentInvalRect = new Rect();
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
        this.mHwApsImpl = null;
        this.mIsSupportPartialUpdate = false;
        this.mIsPartialUpdateDebugOn = false;
        this.mResources = null;
        this.mRenderNode = RenderNode.create(getClass().getName(), this);
    }

    /* access modifiers changed from: package-private */
    public final boolean debugDraw() {
        return DEBUG_DRAW || (this.mAttachInfo != null && this.mAttachInfo.mDebugLayout);
    }

    private static SparseArray<String> getAttributeMap() {
        if (mAttributeMap == null) {
            mAttributeMap = new SparseArray<>();
        }
        return mAttributeMap;
    }

    private void saveAttributeData(AttributeSet attrs, TypedArray t) {
        String resourceName;
        AttributeSet attributeSet = attrs;
        TypedArray typedArray = t;
        int attrsCount = attributeSet == null ? 0 : attrs.getAttributeCount();
        int indexCount = t.getIndexCount();
        String[] attributes = new String[((attrsCount + indexCount) * 2)];
        int i = 0;
        for (int j = 0; j < attrsCount; j++) {
            attributes[i] = attributeSet.getAttributeName(j);
            attributes[i + 1] = attributeSet.getAttributeValue(j);
            i += 2;
        }
        Resources res = t.getResources();
        SparseArray<String> attributeMap = getAttributeMap();
        int j2 = 0;
        while (true) {
            int j3 = j2;
            if (j3 < indexCount) {
                int index = typedArray.getIndex(j3);
                if (typedArray.hasValueOrEmpty(index)) {
                    int resourceId = typedArray.getResourceId(index, 0);
                    if (resourceId != 0) {
                        String resourceName2 = attributeMap.get(resourceId);
                        if (resourceName2 == null) {
                            try {
                                resourceName = res.getResourceName(resourceId);
                            } catch (Resources.NotFoundException e) {
                                Resources.NotFoundException notFoundException = e;
                                resourceName = "0x" + Integer.toHexString(resourceId);
                            }
                            resourceName2 = resourceName;
                            attributeMap.put(resourceId, resourceName2);
                        }
                        attributes[i] = resourceName2;
                        attributes[i + 1] = typedArray.getString(index);
                        i += 2;
                    }
                }
                j2 = j3 + 1;
            } else {
                String[] trimmed = new String[i];
                System.arraycopy(attributes, 0, trimmed, 0, i);
                this.mAttributes = trimmed;
                return;
            }
        }
    }

    public String toString() {
        String pkgname;
        StringBuilder out = new StringBuilder(128);
        out.append(getClass().getName());
        out.append('{');
        out.append(Integer.toHexString(System.identityHashCode(this)));
        out.append(' ');
        int i = this.mViewFlags & 12;
        char c = 'V';
        char c2 = 'I';
        char c3 = '.';
        if (i == 0) {
            out.append('V');
        } else if (i == 4) {
            out.append('I');
        } else if (i != 8) {
            out.append('.');
        } else {
            out.append('G');
        }
        char c4 = 'F';
        out.append((this.mViewFlags & 1) == 1 ? 'F' : '.');
        out.append((this.mViewFlags & 32) == 0 ? DateFormat.DAY : '.');
        out.append((this.mViewFlags & 128) == 128 ? '.' : 'D');
        char c5 = 'H';
        out.append((this.mViewFlags & 256) != 0 ? 'H' : '.');
        if ((this.mViewFlags & 512) == 0) {
            c = '.';
        }
        out.append(c);
        out.append((this.mViewFlags & 16384) != 0 ? 'C' : '.');
        out.append((this.mViewFlags & 2097152) != 0 ? DateFormat.STANDALONE_MONTH : '.');
        out.append((this.mViewFlags & 8388608) != 0 ? 'X' : '.');
        out.append(' ');
        out.append((this.mPrivateFlags & 8) != 0 ? 'R' : '.');
        if ((this.mPrivateFlags & 2) == 0) {
            c4 = '.';
        }
        out.append(c4);
        out.append((this.mPrivateFlags & 4) != 0 ? 'S' : '.');
        if ((this.mPrivateFlags & 33554432) != 0) {
            out.append('p');
        } else {
            out.append((this.mPrivateFlags & 16384) != 0 ? 'P' : '.');
        }
        if ((this.mPrivateFlags & 268435456) == 0) {
            c5 = '.';
        }
        out.append(c5);
        out.append((this.mPrivateFlags & 1073741824) != 0 ? DateFormat.CAPITAL_AM_PM : '.');
        if ((this.mPrivateFlags & Integer.MIN_VALUE) == 0) {
            c2 = '.';
        }
        out.append(c2);
        if ((this.mPrivateFlags & PFLAG_DIRTY_MASK) != 0) {
            c3 = 'D';
        }
        out.append(c3);
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
                int i2 = -16777216 & id;
                if (i2 == 16777216) {
                    pkgname = "android";
                } else if (i2 != 2130706432) {
                    try {
                        pkgname = r.getResourcePackageName(id);
                    } catch (Resources.NotFoundException e) {
                    }
                } else {
                    pkgname = "app";
                }
                String typename = r.getResourceTypeName(id);
                String entryname = r.getResourceEntryName(id);
                out.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
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

    /* access modifiers changed from: protected */
    public void initializeFadingEdge(TypedArray a) {
        TypedArray arr = this.mContext.obtainStyledAttributes(R.styleable.View);
        initializeFadingEdgeInternal(arr);
        arr.recycle();
    }

    /* access modifiers changed from: protected */
    public void initializeFadingEdgeInternal(TypedArray a) {
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

    /* access modifiers changed from: protected */
    public int getHorizontalScrollbarHeight() {
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

    /* access modifiers changed from: protected */
    public void initializeScrollbars(TypedArray a) {
        TypedArray arr = this.mContext.obtainStyledAttributes(R.styleable.View);
        initializeScrollbarsInternal(arr);
        arr.recycle();
    }

    /* access modifiers changed from: protected */
    public void initializeScrollbarsInternal(TypedArray a) {
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
        Drawable thumb2 = a.getDrawable(3);
        if (thumb2 != null) {
            scrollabilityCache.scrollBar.setVerticalThumbDrawable(thumb2);
        }
        if (a.getBoolean(7, false)) {
            scrollabilityCache.scrollBar.setAlwaysDrawVerticalTrack(true);
        }
        int layoutDirection = getLayoutDirection();
        if (track != null) {
            track.setLayoutDirection(layoutDirection);
        }
        if (thumb2 != null) {
            thumb2.setLayoutDirection(layoutDirection);
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

    /* access modifiers changed from: package-private */
    public boolean isOnScrollbar(float x, float y) {
        if (this.mScrollCache == null) {
            return false;
        }
        float x2 = x + ((float) getScrollX());
        float y2 = y + ((float) getScrollY());
        if (isVerticalScrollBarEnabled() && !isVerticalScrollBarHidden()) {
            Rect touchBounds = this.mScrollCache.mScrollBarTouchBounds;
            getVerticalScrollBarBounds(null, touchBounds);
            if (touchBounds.contains((int) x2, (int) y2)) {
                return true;
            }
        }
        if (isHorizontalScrollBarEnabled()) {
            Rect touchBounds2 = this.mScrollCache.mScrollBarTouchBounds;
            getHorizontalScrollBarBounds(null, touchBounds2);
            if (touchBounds2.contains((int) x2, (int) y2)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isOnScrollbarThumb(float x, float y) {
        return isOnVerticalScrollbarThumb(x, y) || isOnHorizontalScrollbarThumb(x, y);
    }

    private boolean isOnVerticalScrollbarThumb(float x, float y) {
        if (this.mScrollCache != null && isVerticalScrollBarEnabled() && !isVerticalScrollBarHidden()) {
            float x2 = x + ((float) getScrollX());
            float y2 = y + ((float) getScrollY());
            Rect bounds = this.mScrollCache.mScrollBarBounds;
            Rect touchBounds = this.mScrollCache.mScrollBarTouchBounds;
            getVerticalScrollBarBounds(bounds, touchBounds);
            int range = computeVerticalScrollRange();
            int offset = computeVerticalScrollOffset();
            int extent = computeVerticalScrollExtent();
            int thumbLength = ScrollBarUtils.getThumbLength(bounds.height(), bounds.width(), extent, range);
            int thumbTop = bounds.top + ScrollBarUtils.getThumbOffset(bounds.height(), thumbLength, extent, range, offset);
            int adjust = Math.max(this.mScrollCache.scrollBarMinTouchTarget - thumbLength, 0) / 2;
            if (x2 >= ((float) touchBounds.left) && x2 <= ((float) touchBounds.right) && y2 >= ((float) (thumbTop - adjust)) && y2 <= ((float) (thumbTop + thumbLength + adjust))) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnHorizontalScrollbarThumb(float x, float y) {
        if (this.mScrollCache != null && isHorizontalScrollBarEnabled()) {
            float x2 = x + ((float) getScrollX());
            float y2 = y + ((float) getScrollY());
            Rect bounds = this.mScrollCache.mScrollBarBounds;
            Rect touchBounds = this.mScrollCache.mScrollBarTouchBounds;
            getHorizontalScrollBarBounds(bounds, touchBounds);
            int range = computeHorizontalScrollRange();
            int offset = computeHorizontalScrollOffset();
            int extent = computeHorizontalScrollExtent();
            int thumbLength = ScrollBarUtils.getThumbLength(bounds.width(), bounds.height(), extent, range);
            int thumbLeft = bounds.left + ScrollBarUtils.getThumbOffset(bounds.width(), thumbLength, extent, range, offset);
            int adjust = Math.max(this.mScrollCache.scrollBarMinTouchTarget - thumbLength, 0) / 2;
            if (x2 >= ((float) (thumbLeft - adjust)) && x2 <= ((float) (thumbLeft + thumbLength + adjust)) && y2 >= ((float) touchBounds.top) && y2 <= ((float) touchBounds.bottom)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isDraggingScrollBar() {
        return (this.mScrollCache == null || this.mScrollCache.mScrollBarDraggingState == 0) ? false : true;
    }

    public void setScrollIndicators(int indicators) {
        setScrollIndicators(indicators, 63);
    }

    public void setScrollIndicators(int indicators, int mask) {
        int mask2 = (mask << 8) & SCROLL_INDICATORS_PFLAG3_MASK;
        int indicators2 = (indicators << 8) & mask2;
        int updatedFlags = (this.mPrivateFlags3 & (~mask2)) | indicators2;
        if (this.mPrivateFlags3 != updatedFlags) {
            this.mPrivateFlags3 = updatedFlags;
            if (indicators2 != 0) {
                initializeScrollIndicatorsInternal();
            }
            invalidate();
        }
    }

    public int getScrollIndicators() {
        return (this.mPrivateFlags3 & SCROLL_INDICATORS_PFLAG3_MASK) >>> 8;
    }

    /* access modifiers changed from: package-private */
    public ListenerInfo getListenerInfo() {
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
            ArrayList unused = li.mOnLayoutChangeListeners = new ArrayList();
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
            CopyOnWriteArrayList unused = li.mOnAttachStateChangeListeners = new CopyOnWriteArrayList();
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
        return (li == null || li.mOnClickListener == null) ? false : true;
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

    public void addFrameMetricsListener(Window window, Window.OnFrameMetricsAvailableListener listener, Handler handler) {
        if (this.mAttachInfo == null) {
            if (this.mFrameMetricsObservers == null) {
                this.mFrameMetricsObservers = new ArrayList<>();
            }
            this.mFrameMetricsObservers.add(new FrameMetricsObserver(window, handler.getLooper(), listener));
        } else if (this.mAttachInfo.mThreadedRenderer != null) {
            if (this.mFrameMetricsObservers == null) {
                this.mFrameMetricsObservers = new ArrayList<>();
            }
            FrameMetricsObserver fmo = new FrameMetricsObserver(window, handler.getLooper(), listener);
            this.mFrameMetricsObservers.add(fmo);
            this.mAttachInfo.mThreadedRenderer.addFrameMetricsObserver(fmo);
        } else {
            Log.w(VIEW_LOG_TAG, "View not hardware-accelerated. Unable to observe frame stats");
        }
    }

    public void removeFrameMetricsListener(Window.OnFrameMetricsAvailableListener listener) {
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
                Iterator<FrameMetricsObserver> it = this.mFrameMetricsObservers.iterator();
                while (it.hasNext()) {
                    renderer.addFrameMetricsObserver(it.next());
                }
                return;
            }
            Log.w(VIEW_LOG_TAG, "View not hardware-accelerated. Unable to observe frame stats");
        }
    }

    private FrameMetricsObserver findFrameMetricsObserver(Window.OnFrameMetricsAvailableListener listener) {
        for (int i = 0; i < this.mFrameMetricsObservers.size(); i++) {
            FrameMetricsObserver observer = this.mFrameMetricsObservers.get(i);
            if (observer.mListener == listener) {
                return observer;
            }
        }
        return null;
    }

    public void setNotifyAutofillManagerOnClick(boolean notify) {
        if (notify) {
            this.mPrivateFlags |= 536870912;
        } else {
            this.mPrivateFlags &= -536870913;
        }
    }

    private void notifyAutofillManagerOnClick() {
        if ((this.mPrivateFlags & 536870912) != 0) {
            try {
                getAutofillManager().notifyViewClicked(this);
            } finally {
                this.mPrivateFlags = -536870913 & this.mPrivateFlags;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean performClickInternal() {
        notifyAutofillManagerOnClick();
        return performClick();
    }

    public boolean performClick() {
        if (Jlog.isMicroTest()) {
            Jlog.i(JlogConstants.JL_VIEW_CLICK_BEGIN, Jlog.getMessage(VIEW_LOG_TAG, "performClick", ""));
        }
        notifyAutofillManagerOnClick();
        ListenerInfo li = this.mListenerInfo;
        boolean result = false;
        if (!(li == null || li.mOnClickListener == null)) {
            playSoundEffect(0);
            li.mOnClickListener.onClick(this);
            HwFrameworkFactory.getHwView().onClick(this, this.mContext);
            result = true;
        }
        sendAccessibilityEvent(1);
        notifyEnterOrExitForAutoFillIfNeeded(true);
        if (Jlog.isMicroTest()) {
            Jlog.i(JlogConstants.JL_VIEW_CLICK_END, Jlog.getMessage(VIEW_LOG_TAG, "performClick", ""));
        }
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
            handled = !Float.isNaN(x) && !Float.isNaN(y) ? showContextMenu(x, y) : showContextMenu();
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

    /* access modifiers changed from: protected */
    public boolean performButtonActionOnTouchDown(MotionEvent event) {
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
        OnKeyListener unused = getListenerInfo().mOnKeyListener = l;
    }

    public void setOnTouchListener(OnTouchListener l) {
        OnTouchListener unused = getListenerInfo().mOnTouchListener = l;
    }

    public void setOnGenericMotionListener(OnGenericMotionListener l) {
        OnGenericMotionListener unused = getListenerInfo().mOnGenericMotionListener = l;
    }

    public void setOnHoverListener(OnHoverListener l) {
        OnHoverListener unused = getListenerInfo().mOnHoverListener = l;
    }

    public void setOnDragListener(OnDragListener l) {
        OnDragListener unused = getListenerInfo().mOnDragListener = l;
    }

    /* access modifiers changed from: package-private */
    public void handleFocusGainInternal(int direction, Rect previouslyFocusedRect) {
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
        boolean scrolled = false;
        if (this.mParent == null) {
            return false;
        }
        View child = this;
        RectF position = this.mAttachInfo != null ? this.mAttachInfo.mTmpTransformRect : new RectF();
        position.set(rectangle);
        ViewParent parent = this.mParent;
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
        clearFocusInternal(null, true, sAlwaysAssignFocus || !isInTouchMode());
    }

    /* access modifiers changed from: package-private */
    public void clearFocusInternal(View focused, boolean propagate, boolean refocus) {
        if ((this.mPrivateFlags & 2) != 0) {
            this.mPrivateFlags &= -3;
            clearParentsWantFocus();
            if (propagate && this.mParent != null) {
                this.mParent.clearChildFocus(this);
            }
            onFocusChanged(false, 0, null);
            refreshDrawableState();
            if (!propagate) {
                return;
            }
            if (!refocus || !rootViewRequestFocus()) {
                notifyGlobalFocusCleared(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyGlobalFocusCleared(View oldFocus) {
        if (oldFocus != null && this.mAttachInfo != null) {
            this.mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(oldFocus, null);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean rootViewRequestFocus() {
        View root = getRootView();
        return root != null && root.requestFocus();
    }

    /* access modifiers changed from: package-private */
    public void unFocus(View focused) {
        clearFocusInternal(focused, false, false);
    }

    @ViewDebug.ExportedProperty(category = "focus")
    public boolean hasFocus() {
        return (this.mPrivateFlags & 2) != 0;
    }

    public boolean hasFocusable() {
        return hasFocusable(!sHasFocusableExcludeAutoFocusable, false);
    }

    public boolean hasExplicitFocusable() {
        return hasFocusable(false, true);
    }

    /* access modifiers changed from: package-private */
    public boolean hasFocusable(boolean allowAutoFocus, boolean dispatchExplicit) {
        if (!isFocusableInTouchMode()) {
            for (ViewParent p = this.mParent; p instanceof ViewGroup; p = p.getParent()) {
                if (((ViewGroup) p).shouldBlockFocusForTouchscreen()) {
                    return false;
                }
            }
        }
        if ((this.mViewFlags & 12) != 0 || (this.mViewFlags & 32) != 0) {
            return false;
        }
        if ((allowAutoFocus || getFocusable() != 16) && isFocusable()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
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

    public void notifyEnterOrExitForAutoFillIfNeeded(boolean enter) {
        if (canNotifyAutofillEnterExitEvent()) {
            AutofillManager afm = getAutofillManager();
            if (afm == null) {
                return;
            }
            if (!enter || !isFocused()) {
                if (!enter && !isFocused()) {
                    afm.notifyViewExited(this);
                }
            } else if (!isLaidOut()) {
                this.mPrivateFlags3 |= 134217728;
            } else if (isVisibleToUser()) {
                afm.notifyViewEntered(this);
            }
        }
    }

    public void setAccessibilityPaneTitle(CharSequence accessibilityPaneTitle) {
        if (!TextUtils.equals(accessibilityPaneTitle, this.mAccessibilityPaneTitle)) {
            this.mAccessibilityPaneTitle = accessibilityPaneTitle;
            notifyViewAccessibilityStateChangedIfNeeded(8);
        }
    }

    public CharSequence getAccessibilityPaneTitle() {
        return this.mAccessibilityPaneTitle;
    }

    private boolean isAccessibilityPane() {
        return this.mAccessibilityPaneTitle != null;
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
        boolean isWindowDisappearedEvent = false;
        if ((event.getEventType() == 32) && (32 & event.getContentChangeTypes()) != 0) {
            isWindowDisappearedEvent = true;
        }
        if (isShown() || isWindowDisappearedEvent) {
            onInitializeAccessibilityEvent(event);
            if ((event.getEventType() & POPULATING_ACCESSIBILITY_EVENT_TYPES) != 0) {
                dispatchPopulateAccessibilityEvent(event);
            }
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestSendAccessibilityEvent(this, event);
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
        if (event.getEventType() == 32 && !TextUtils.isEmpty(getAccessibilityPaneTitle())) {
            event.getText().add(getAccessibilityPaneTitle());
        }
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
        int eventType = event.getEventType();
        if (eventType == 8) {
            ArrayList<View> focusablesTempList = this.mAttachInfo != null ? this.mAttachInfo.mTempArrayList : new ArrayList<>();
            getRootView().addFocusables(focusablesTempList, 2, 0);
            event.setItemCount(focusablesTempList.size());
            event.setCurrentItemIndex(focusablesTempList.indexOf(this));
            if (this.mAttachInfo != null) {
                focusablesTempList.clear();
            }
        } else if (eventType == 8192) {
            CharSequence text = getIterableTextForAccessibility();
            if (text != null && text.length() > 0) {
                event.setFromIndex(getAccessibilitySelectionStart());
                event.setToIndex(getAccessibilitySelectionEnd());
                event.setItemCount(text.length());
            }
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

    /* JADX WARNING: type inference failed for: r4v4, types: [android.view.ViewParent] */
    /* JADX WARNING: type inference failed for: r4v5, types: [android.view.ViewParent] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 2 */
    private void onProvideStructureForAssistOrAutofill(ViewStructure structure, boolean forAutofill, int flags) {
        String type;
        String entry;
        int id = this.mID;
        String pkg = null;
        if (id == -1 || isViewIdGenerated(id)) {
            structure.setId(id, null, null, null);
        } else {
            try {
                Resources res = getResources();
                String entry2 = res.getResourceEntryName(id);
                type = res.getResourceTypeName(id);
                String str = entry2;
                entry = res.getResourcePackageName(id);
                pkg = str;
            } catch (Resources.NotFoundException e) {
                entry = null;
                type = null;
            }
            structure.setId(id, entry, type, pkg);
        }
        if (forAutofill) {
            int autofillType = getAutofillType();
            if (autofillType != 0) {
                structure.setAutofillType(autofillType);
                structure.setAutofillHints(getAutofillHints());
                structure.setAutofillValue(getAutofillValue());
            }
            structure.setImportantForAutofill(getImportantForAutofill());
        }
        int ignoredParentLeft = 0;
        int ignoredParentTop = 0;
        if (forAutofill && (flags & 1) == 0) {
            View parentGroup = null;
            ? parentGroup2 = getParent();
            if (parentGroup2 instanceof View) {
                parentGroup = parentGroup2;
            }
            while (parentGroup != null && !parentGroup.isImportantForAutofill()) {
                ignoredParentLeft += parentGroup.mLeft;
                ignoredParentTop += parentGroup.mTop;
                ? parentGroup3 = parentGroup.getParent();
                if (!(parentGroup3 instanceof View)) {
                    break;
                }
                parentGroup = parentGroup3;
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
        onProvideVirtualStructureCompat(structure, false);
    }

    private void onProvideVirtualStructureCompat(ViewStructure structure, boolean forAutofill) {
        AccessibilityNodeProvider provider = getAccessibilityNodeProvider();
        if (provider != null) {
            if (Helper.sVerbose && forAutofill) {
                Log.v(VIEW_LOG_TAG, "onProvideVirtualStructureCompat() for " + this);
            }
            AccessibilityNodeInfo info = createAccessibilityNodeInfo();
            structure.setChildCount(1);
            populateVirtualStructure(structure.newChild(0), provider, info, forAutofill);
            info.recycle();
        }
    }

    public void onProvideAutofillVirtualStructure(ViewStructure structure, int flags) {
        if (this.mContext.isAutofillCompatibilityEnabled()) {
            onProvideVirtualStructureCompat(structure, true);
        }
    }

    public void autofill(AutofillValue value) {
    }

    public void autofill(SparseArray<AutofillValue> values) {
        if (this.mContext.isAutofillCompatibilityEnabled()) {
            AccessibilityNodeProvider provider = getAccessibilityNodeProvider();
            if (provider != null) {
                int valueCount = values.size();
                for (int i = 0; i < valueCount; i++) {
                    AutofillValue value = values.valueAt(i);
                    if (value.isText()) {
                        int virtualId = values.keyAt(i);
                        CharSequence text = value.getTextValue();
                        Bundle arguments = new Bundle();
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
                        provider.performAction(virtualId, 2097152, arguments);
                    }
                }
            }
        }
    }

    public final AutofillId getAutofillId() {
        if (this.mAutofillId == null) {
            this.mAutofillId = new AutofillId(getAutofillViewId());
        }
        return this.mAutofillId;
    }

    public void setAutofillId(AutofillId id) {
        if (Helper.sVerbose) {
            Log.v(VIEW_LOG_TAG, "setAutofill(): from " + this.mAutofillId + " to " + id);
        }
        if (isAttachedToWindow()) {
            throw new IllegalStateException("Cannot set autofill id when view is attached");
        } else if (id != null && id.isVirtual()) {
            throw new IllegalStateException("Cannot set autofill id assigned to virtual views");
        } else if (id != null || (this.mPrivateFlags3 & 1073741824) != 0) {
            this.mAutofillId = id;
            if (id != null) {
                this.mAutofillViewId = id.getViewId();
                this.mPrivateFlags3 = 1073741824 | this.mPrivateFlags3;
            } else {
                this.mAutofillViewId = -1;
                this.mPrivateFlags3 &= -1073741825;
            }
        }
    }

    public int getAutofillType() {
        return 0;
    }

    @ViewDebug.ExportedProperty
    public String[] getAutofillHints() {
        return this.mAutofillHints;
    }

    public boolean isAutofilled() {
        return (this.mPrivateFlags3 & 65536) != 0;
    }

    public AutofillValue getAutofillValue() {
        return null;
    }

    @ViewDebug.ExportedProperty(mapping = {@ViewDebug.IntToString(from = 0, to = "auto"), @ViewDebug.IntToString(from = 1, to = "yes"), @ViewDebug.IntToString(from = 2, to = "no"), @ViewDebug.IntToString(from = 4, to = "yesExcludeDescendants"), @ViewDebug.IntToString(from = 8, to = "noExcludeDescendants")})
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
        if (id != -1 && !isViewIdGenerated(id)) {
            Resources res = getResources();
            String entry = null;
            String pkg = null;
            try {
                entry = res.getResourceEntryName(id);
                pkg = res.getResourcePackageName(id);
            } catch (Resources.NotFoundException e) {
            }
            if (!(entry == null || pkg == null || !pkg.equals(this.mContext.getPackageName()))) {
                return true;
            }
        }
        if (getAutofillHints() != null) {
            return true;
        }
        return false;
    }

    private AutofillManager getAutofillManager() {
        return (AutofillManager) this.mContext.getSystemService(AutofillManager.class);
    }

    private boolean isAutofillable() {
        return getAutofillType() != 0 && isImportantForAutofill() && getAutofillViewId() > 1073741823;
    }

    public boolean canNotifyAutofillEnterExitEvent() {
        return isAutofillable() && isAttachedToWindow();
    }

    private void populateVirtualStructure(ViewStructure structure, AccessibilityNodeProvider provider, AccessibilityNodeInfo info, boolean forAutofill) {
        String str = null;
        structure.setId(AccessibilityNodeInfo.getVirtualDescendantId(info.getSourceNodeId()), null, null, info.getViewIdResourceName());
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
        if (forAutofill) {
            structure.setAutofillId(new AutofillId(getAutofillId(), AccessibilityNodeInfo.getVirtualDescendantId(info.getSourceNodeId())));
        }
        CharSequence cname = info.getClassName();
        if (cname != null) {
            str = cname.toString();
        }
        structure.setClassName(str);
        structure.setContentDescription(info.getContentDescription());
        if (forAutofill) {
            int maxTextLength = info.getMaxTextLength();
            if (maxTextLength != -1) {
                structure.setMaxTextLength(maxTextLength);
            }
            structure.setHint(info.getHintText());
        }
        CharSequence text = info.getText();
        boolean hasText = (text == null && info.getError() == null) ? false : true;
        if (hasText) {
            structure.setText(text, info.getTextSelectionStart(), info.getTextSelectionEnd());
        }
        if (forAutofill) {
            if (info.isEditable()) {
                structure.setDataIsSensitive(true);
                if (hasText) {
                    structure.setAutofillType(1);
                    structure.setAutofillValue(AutofillValue.forText(text));
                }
                int inputType = info.getInputType();
                if (inputType == 0 && info.isPassword()) {
                    inputType = 129;
                }
                structure.setInputType(inputType);
            } else {
                structure.setDataIsSensitive(false);
            }
        }
        int NCHILDREN = info.getChildCount();
        if (NCHILDREN > 0) {
            structure.setChildCount(NCHILDREN);
            for (int i = 0; i < NCHILDREN; i++) {
                if (AccessibilityNodeInfo.getVirtualDescendantId(info.getChildNodeIds().get(i)) == -1) {
                    Log.e(VIEW_LOG_TAG, "Virtual view pointing to its host. Ignoring");
                } else {
                    AccessibilityNodeInfo cinfo = provider.createAccessibilityNodeInfo(AccessibilityNodeInfo.getVirtualDescendantId(info.getChildId(i)));
                    populateVirtualStructure(structure.newChild(i), provider, cinfo, forAutofill);
                    if (cinfo != null) {
                        cinfo.recycle();
                    }
                }
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
            onProvideAutofillStructure(structure, flags);
            onProvideAutofillVirtualStructure(structure, flags);
        } else if (!isAssistBlocked()) {
            onProvideStructure(structure);
            onProvideVirtualStructure(structure);
        } else {
            structure.setClassName(getAccessibilityClassName().toString());
            structure.setAssistBlocked(true);
        }
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        AccessibilityNodeInfo.AccessibilityAction accessibilityAction;
        if (this.mAttachInfo != null) {
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
                View rootView = getRootView();
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
                    } catch (Resources.NotFoundException e) {
                    }
                }
            }
            if (this.mLabelForId != -1) {
                View rootView2 = getRootView();
                if (rootView2 == null) {
                    rootView2 = this;
                }
                View labeled = rootView2.findViewInsideOutShouldExist(this, this.mLabelForId);
                if (labeled != null) {
                    info.setLabelFor(labeled);
                }
            }
            if (this.mAccessibilityTraversalBeforeId != -1) {
                View rootView3 = getRootView();
                if (rootView3 == null) {
                    rootView3 = this;
                }
                View next = rootView3.findViewInsideOutShouldExist(this, this.mAccessibilityTraversalBeforeId);
                if (next != null && next.includeForAccessibility()) {
                    info.setTraversalBefore(next);
                }
            }
            if (this.mAccessibilityTraversalAfterId != -1) {
                View rootView4 = getRootView();
                if (rootView4 == null) {
                    rootView4 = this;
                }
                View next2 = rootView4.findViewInsideOutShouldExist(this, this.mAccessibilityTraversalAfterId);
                if (next2 != null && next2.includeForAccessibility()) {
                    info.setTraversalAfter(next2);
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
            info.setScreenReaderFocusable(isScreenReaderFocusable());
            info.setFocused(isFocused());
            info.setAccessibilityFocused(isAccessibilityFocused());
            info.setSelected(isSelected());
            info.setLongClickable(isLongClickable());
            info.setContextClickable(isContextClickable());
            info.setLiveRegion(getAccessibilityLiveRegion());
            if (!(this.mTooltipInfo == null || this.mTooltipInfo.mTooltipText == null)) {
                info.setTooltipText(this.mTooltipInfo.mTooltipText);
                if (this.mTooltipInfo.mTooltipPopup == null) {
                    accessibilityAction = AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_TOOLTIP;
                } else {
                    accessibilityAction = AccessibilityNodeInfo.AccessibilityAction.ACTION_HIDE_TOOLTIP;
                }
                info.addAction(accessibilityAction);
            }
            info.addAction(4);
            info.addAction(8);
            if (isFocusable()) {
                if (isFocused()) {
                    info.addAction(2);
                } else {
                    info.addAction(1);
                }
            }
            if (!isAccessibilityFocused()) {
                info.addAction(64);
            } else {
                info.addAction(128);
            }
            if (isClickable() && isEnabled()) {
                info.addAction(16);
            }
            if (isLongClickable() && isEnabled()) {
                info.addAction(32);
            }
            if (isContextClickable() && isEnabled()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CONTEXT_CLICK);
            }
            CharSequence text = getIterableTextForAccessibility();
            if (text != null && text.length() > 0) {
                info.setTextSelection(getAccessibilitySelectionStart(), getAccessibilitySelectionEnd());
                info.addAction(131072);
                info.addAction(256);
                info.addAction(512);
                info.setMovementGranularities(11);
            }
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN);
            populateAccessibilityNodeInfoDrawingOrderInParent(info);
            info.setPaneTitle(this.mAccessibilityPaneTitle);
            info.setHeading(isAccessibilityHeading());
        }
    }

    public void addExtraDataToAccessibilityNodeInfo(AccessibilityNodeInfo info, String extraDataKey, Bundle arguments) {
    }

    private void populateAccessibilityNodeInfoDrawingOrderInParent(AccessibilityNodeInfo info) {
        AccessibilityNodeInfo accessibilityNodeInfo = info;
        int i = 0;
        if ((this.mPrivateFlags & 16) == 0) {
            accessibilityNodeInfo.setDrawingOrder(0);
            return;
        }
        int drawingOrderInParent = 1;
        View viewAtDrawingLevel = this;
        ViewParent parent = getParentForAccessibility();
        while (true) {
            if (viewAtDrawingLevel == parent) {
                break;
            }
            ViewParent currentParent = viewAtDrawingLevel.getParent();
            if (!(currentParent instanceof ViewGroup)) {
                drawingOrderInParent = 0;
                break;
            }
            ViewGroup parentGroup = (ViewGroup) currentParent;
            int childCount = parentGroup.getChildCount();
            if (childCount > 1) {
                List<View> preorderedList = parentGroup.buildOrderedChildList();
                if (preorderedList != null) {
                    int childDrawIndex = preorderedList.indexOf(viewAtDrawingLevel);
                    int drawingOrderInParent2 = drawingOrderInParent;
                    for (int i2 = i; i2 < childDrawIndex; i2++) {
                        drawingOrderInParent2 += numViewsForAccessibility(preorderedList.get(i2));
                    }
                    drawingOrderInParent = drawingOrderInParent2;
                } else {
                    int childIndex = parentGroup.indexOfChild(viewAtDrawingLevel);
                    boolean customOrder = parentGroup.isChildrenDrawingOrderEnabled();
                    int childDrawIndex2 = (childIndex < 0 || !customOrder) ? childIndex : parentGroup.getChildDrawingOrder(childCount, childIndex);
                    int numChildrenToIterate = customOrder ? childCount : childDrawIndex2;
                    if (childDrawIndex2 != 0) {
                        int drawingOrderInParent3 = drawingOrderInParent;
                        for (int i3 = i; i3 < numChildrenToIterate; i3++) {
                            if ((customOrder ? parentGroup.getChildDrawingOrder(childCount, i3) : i3) < childDrawIndex2) {
                                drawingOrderInParent3 += numViewsForAccessibility(parentGroup.getChildAt(i3));
                            }
                        }
                        drawingOrderInParent = drawingOrderInParent3;
                    }
                }
            }
            viewAtDrawingLevel = (View) currentParent;
            i = 0;
        }
        accessibilityNodeInfo.setDrawingOrder(drawingOrderInParent);
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
        int unused = this.mMatchLabelForPredicate.mLabeledId = labeledId;
        return findViewByPredicateInsideOut(view, this.mMatchLabelForPredicate);
    }

    public boolean isVisibleToUserForAutofill(int virtualId) {
        if (!this.mContext.isAutofillCompatibilityEnabled()) {
            return true;
        }
        AccessibilityNodeProvider provider = getAccessibilityNodeProvider();
        if (provider != null) {
            AccessibilityNodeInfo node = provider.createAccessibilityNodeInfo(virtualId);
            if (node != null) {
                return node.isVisibleToUser();
            }
        } else {
            Log.w(VIEW_LOG_TAG, "isVisibleToUserForAutofill(" + virtualId + "): no provider");
        }
        return false;
    }

    public boolean isVisibleToUser() {
        return isVisibleToUser(null);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public boolean isVisibleToUser(Rect boundInView) {
        if (this.mAttachInfo == null || this.mAttachInfo.mWindowVisibility != 0) {
            return false;
        }
        Object current = this;
        while (current instanceof View) {
            View view = (View) current;
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

    @ViewDebug.ExportedProperty(category = "accessibility")
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
        if (!(contentDescription != null && contentDescription.length() > 0) || getImportantForAccessibility() != 0) {
            notifyViewAccessibilityStateChangedIfNeeded(4);
        } else {
            setImportantForAccessibility(1);
            notifySubtreeAccessibilityStateChangedIfNeeded();
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

    @ViewDebug.ExportedProperty(category = "accessibility")
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

    /* access modifiers changed from: protected */
    public void onFocusLost() {
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

    @ViewDebug.ExportedProperty(category = "focus")
    public boolean isFocused() {
        return (this.mPrivateFlags & 2) != 0;
    }

    public View findFocus() {
        if ((this.mPrivateFlags & 2) != 0) {
            return this;
        }
        return null;
    }

    public boolean isScrollContainer() {
        return (this.mPrivateFlags & 1048576) != 0;
    }

    public void setScrollContainer(boolean isScrollContainer) {
        if (isScrollContainer) {
            if (this.mAttachInfo != null && (this.mPrivateFlags & 1048576) == 0) {
                this.mAttachInfo.mScrollContainers.add(this);
                this.mPrivateFlags = 1048576 | this.mPrivateFlags;
            }
            this.mPrivateFlags |= 524288;
            return;
        }
        if ((1048576 & this.mPrivateFlags) != 0) {
            this.mAttachInfo.mScrollContainers.remove(this);
        }
        this.mPrivateFlags &= -1572865;
    }

    @Deprecated
    public int getDrawingCacheQuality() {
        return this.mViewFlags & DRAWING_CACHE_QUALITY_MASK;
    }

    @Deprecated
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

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean fitSystemWindows(Rect insets) {
        if ((this.mPrivateFlags3 & 32) != 0) {
            return fitSystemWindowsInt(insets);
        }
        if (insets == null) {
            return false;
        }
        try {
            this.mPrivateFlags3 |= 64;
            return dispatchApplyWindowInsets(new WindowInsets(insets)).isConsumed();
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
        Rect localInsets = sThreadLocal.get();
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
            if (this.mListenerInfo != null && this.mListenerInfo.mOnApplyWindowInsetsListener != null) {
                return this.mListenerInfo.mOnApplyWindowInsetsListener.onApplyWindowInsets(this, insets);
            }
            WindowInsets onApplyWindowInsets = onApplyWindowInsets(insets);
            this.mPrivateFlags3 &= -33;
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

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean computeFitSystemWindows(Rect inoutInsets, Rect outLocalInsets) {
        WindowInsets innerInsets = computeSystemWindowInsets(new WindowInsets(inoutInsets), outLocalInsets);
        inoutInsets.set(innerInsets.getSystemWindowInsets());
        return innerInsets.isSystemWindowInsetsConsumed();
    }

    public WindowInsets computeSystemWindowInsets(WindowInsets in, Rect outLocalInsets) {
        if ((this.mViewFlags & 2048) == 0 || this.mAttachInfo == null || ((this.mAttachInfo.mSystemUiVisibility & SYSTEM_UI_LAYOUT_FLAGS) == 0 && !this.mAttachInfo.mOverscanRequested)) {
            outLocalInsets.set(in.getSystemWindowInsets());
            return in.consumeSystemWindowInsets().inset(outLocalInsets);
        }
        outLocalInsets.set(this.mAttachInfo.mOverscanInsets);
        return in.inset(outLocalInsets);
    }

    public void setFitsSystemWindows(boolean fitSystemWindows) {
        setFlags(fitSystemWindows ? 2 : 0, 2);
    }

    @ViewDebug.ExportedProperty
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

    @ViewDebug.ExportedProperty(mapping = {@ViewDebug.IntToString(from = 0, to = "VISIBLE"), @ViewDebug.IntToString(from = 4, to = "INVISIBLE"), @ViewDebug.IntToString(from = 8, to = "GONE")})
    public int getVisibility() {
        return this.mViewFlags & 12;
    }

    @RemotableViewMethod
    public void setVisibility(int visibility) {
        int oldViewFlags = this.mViewFlags;
        setFlags(visibility, 12);
        if (oldViewFlags != this.mViewFlags) {
            sTriggerFlag = true;
        }
    }

    @ViewDebug.ExportedProperty
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
        setFocusable((int) focusable);
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

    @ViewDebug.ExportedProperty
    public boolean isSoundEffectsEnabled() {
        return 134217728 == (this.mViewFlags & 134217728);
    }

    public void setHapticFeedbackEnabled(boolean hapticFeedbackEnabled) {
        setFlags(hapticFeedbackEnabled ? 268435456 : 0, 268435456);
    }

    @ViewDebug.ExportedProperty
    public boolean isHapticFeedbackEnabled() {
        return 268435456 == (this.mViewFlags & 268435456);
    }

    @ViewDebug.ExportedProperty(category = "layout", mapping = {@ViewDebug.IntToString(from = 0, to = "LTR"), @ViewDebug.IntToString(from = 1, to = "RTL"), @ViewDebug.IntToString(from = 2, to = "INHERIT"), @ViewDebug.IntToString(from = 3, to = "LOCALE")})
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

    @ViewDebug.ExportedProperty(category = "layout", mapping = {@ViewDebug.IntToString(from = 0, to = "RESOLVED_DIRECTION_LTR"), @ViewDebug.IntToString(from = 1, to = "RESOLVED_DIRECTION_RTL")})
    public int getLayoutDirection() {
        int i = 0;
        if (getContext().getApplicationInfo().targetSdkVersion >= 17 || this.mForceRTL) {
            if ((this.mPrivateFlags2 & 16) == 16) {
                i = 1;
            }
            return i;
        }
        this.mPrivateFlags2 |= 32;
        return 0;
    }

    @ViewDebug.ExportedProperty(category = "layout")
    public boolean isLayoutRtl() {
        return getLayoutDirection() == 1;
    }

    public boolean isRtlLocale() {
        String currentLang = Locale.getDefault().getLanguage();
        return currentLang.contains("ar") || currentLang.contains("fa") || currentLang.contains("iw") || currentLang.contains("ug") || currentLang.contains("ur") || isLayoutRtl();
    }

    @ViewDebug.ExportedProperty(category = "layout")
    public boolean hasTransientState() {
        return (this.mPrivateFlags2 & Integer.MIN_VALUE) == Integer.MIN_VALUE;
    }

    public void setHasTransientState(boolean hasTransientState) {
        int i;
        boolean oldHasTransientState = hasTransientState();
        if (hasTransientState) {
            i = this.mTransientStateCount + 1;
        } else {
            i = this.mTransientStateCount - 1;
        }
        this.mTransientStateCount = i;
        int i2 = 0;
        if (this.mTransientStateCount < 0) {
            this.mTransientStateCount = 0;
            Log.e(VIEW_LOG_TAG, "hasTransientState decremented below 0: unmatched pair of setHasTransientState calls");
        } else if ((hasTransientState && this.mTransientStateCount == 1) || (!hasTransientState && this.mTransientStateCount == 0)) {
            int i3 = this.mPrivateFlags2 & Integer.MAX_VALUE;
            if (hasTransientState) {
                i2 = Integer.MIN_VALUE;
            }
            this.mPrivateFlags2 = i3 | i2;
            boolean newHasTransientState = hasTransientState();
            if (this.mParent != null && newHasTransientState != oldHasTransientState) {
                try {
                    this.mParent.childHasTransientStateChanged(this, newHasTransientState);
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

    /* access modifiers changed from: package-private */
    public boolean isLayoutValid() {
        return isLaidOut() && (this.mPrivateFlags & 4096) == 0;
    }

    public void setWillNotDraw(boolean willNotDraw) {
        setFlags(willNotDraw ? 128 : 0, 128);
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean willNotDraw() {
        return (this.mViewFlags & 128) == 128;
    }

    @Deprecated
    public void setWillNotCacheDrawing(boolean willNotCacheDrawing) {
        setFlags(willNotCacheDrawing ? 131072 : 0, 131072);
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    @Deprecated
    public boolean willNotCacheDrawing() {
        return (this.mViewFlags & 131072) == 131072;
    }

    @ViewDebug.ExportedProperty
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

    /* access modifiers changed from: private */
    public void setPressed(boolean pressed, float x, float y) {
        if (pressed) {
            drawableHotspotChanged(x, y);
        }
        setPressed(pressed);
    }

    public void setPressed(boolean pressed) {
        boolean z = false;
        if (pressed != ((this.mPrivateFlags & 16384) == 16384)) {
            z = true;
        }
        boolean needsRefresh = z;
        if (pressed) {
            this.mPrivateFlags = 16384 | this.mPrivateFlags;
        } else {
            this.mPrivateFlags &= -16385;
        }
        if (needsRefresh) {
            refreshDrawableState();
        }
        dispatchSetPressed(pressed);
    }

    /* access modifiers changed from: protected */
    public void dispatchSetPressed(boolean pressed) {
    }

    @ViewDebug.ExportedProperty
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
        setFlags(enabled ? 0 : 65536, 65536);
    }

    @ViewDebug.ExportedProperty
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
        setFlags(enabled ? 0 : 536870912, 536870912);
    }

    @ViewDebug.ExportedProperty(category = "focus")
    public final boolean isFocusable() {
        return 1 == (this.mViewFlags & 1);
    }

    @ViewDebug.ExportedProperty(category = "focus", mapping = {@ViewDebug.IntToString(from = 0, to = "NOT_FOCUSABLE"), @ViewDebug.IntToString(from = 1, to = "FOCUSABLE"), @ViewDebug.IntToString(from = 16, to = "FOCUSABLE_AUTO")})
    public int getFocusable() {
        if ((this.mViewFlags & 16) > 0) {
            return 16;
        }
        return this.mViewFlags & 1;
    }

    @ViewDebug.ExportedProperty(category = "focus")
    public final boolean isFocusableInTouchMode() {
        return 262144 == (this.mViewFlags & 262144);
    }

    public boolean isScreenReaderFocusable() {
        return (this.mPrivateFlags3 & 268435456) != 0;
    }

    public void setScreenReaderFocusable(boolean screenReaderFocusable) {
        updatePflags3AndNotifyA11yIfChanged(268435456, screenReaderFocusable);
    }

    public boolean isAccessibilityHeading() {
        return (this.mPrivateFlags3 & Integer.MIN_VALUE) != 0;
    }

    public void setAccessibilityHeading(boolean isHeading) {
        updatePflags3AndNotifyA11yIfChanged(Integer.MIN_VALUE, isHeading);
    }

    private void updatePflags3AndNotifyA11yIfChanged(int mask, boolean newValue) {
        int pflags3;
        int pflags32 = this.mPrivateFlags3;
        if (newValue) {
            pflags3 = pflags32 | mask;
        } else {
            pflags3 = pflags32 & (~mask);
        }
        if (pflags3 != this.mPrivateFlags3) {
            this.mPrivateFlags3 = pflags3;
            notifyViewAccessibilityStateChangedIfNeeded(0);
        }
    }

    public View focusSearch(int direction) {
        if (this.mParent != null) {
            return this.mParent.focusSearch(this, direction);
        }
        return null;
    }

    @ViewDebug.ExportedProperty(category = "focus")
    public final boolean isKeyboardNavigationCluster() {
        return (this.mPrivateFlags3 & 32768) != 0;
    }

    /* access modifiers changed from: package-private */
    public View findKeyboardNavigationCluster() {
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
            for (ViewParent parent = this.mParent; parent instanceof ViewGroup; parent = parent.getParent()) {
                ((ViewGroup) parent).mFocusedInCluster = child;
                if (parent == cluster) {
                    break;
                }
                child = (View) parent;
            }
        }
    }

    private void updateFocusedInCluster(View oldFocus, int direction) {
        if (oldFocus != null) {
            View oldCluster = oldFocus.findKeyboardNavigationCluster();
            if (oldCluster != findKeyboardNavigationCluster()) {
                oldFocus.setFocusedInCluster(oldCluster);
                if (oldFocus.mParent instanceof ViewGroup) {
                    if (direction == 2 || direction == 1) {
                        ((ViewGroup) oldFocus.mParent).clearFocusedInCluster(oldFocus);
                    } else if ((oldFocus instanceof ViewGroup) && ((ViewGroup) oldFocus).getDescendantFocusability() == 262144 && ViewRootImpl.isViewDescendantOf(this, oldFocus)) {
                        ((ViewGroup) oldFocus.mParent).clearFocusedInCluster(oldFocus);
                    }
                }
            }
        }
    }

    @ViewDebug.ExportedProperty(category = "focus")
    public final boolean isFocusedByDefault() {
        return (this.mPrivateFlags3 & 262144) != 0;
    }

    public void setFocusedByDefault(boolean isFocusedByDefault) {
        if (isFocusedByDefault != ((this.mPrivateFlags3 & 262144) != 0)) {
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

    /* access modifiers changed from: package-private */
    public boolean hasDefaultFocus() {
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

    @ViewDebug.ExportedProperty(category = "focus")
    public final boolean getDefaultFocusHighlightEnabled() {
        return this.mDefaultFocusHighlightEnabled;
    }

    /* access modifiers changed from: package-private */
    public View findUserSetNextFocus(View root, int direction) {
        if (direction != 17) {
            if (direction != 33) {
                if (direction != 66) {
                    if (direction != 130) {
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
                            default:
                                return null;
                        }
                    } else if (this.mNextFocusDownId == -1) {
                        return null;
                    } else {
                        return findViewInsideOutShouldExist(root, this.mNextFocusDownId);
                    }
                } else if (this.mNextFocusRightId == -1) {
                    return null;
                } else {
                    return findViewInsideOutShouldExist(root, this.mNextFocusRightId);
                }
            } else if (this.mNextFocusUpId == -1) {
                return null;
            } else {
                return findViewInsideOutShouldExist(root, this.mNextFocusUpId);
            }
        } else if (this.mNextFocusLeftId == -1) {
            return null;
        } else {
            return findViewInsideOutShouldExist(root, this.mNextFocusLeftId);
        }
    }

    /* access modifiers changed from: package-private */
    public View findUserSetNextKeyboardNavigationCluster(View root, int direction) {
        switch (direction) {
            case 1:
                if (this.mID == -1) {
                    return null;
                }
                return root.findViewByPredicateInsideOut(this, new Predicate(this.mID) {
                    private final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final boolean test(Object obj) {
                        return View.lambda$findUserSetNextKeyboardNavigationCluster$0(this.f$0, (View) obj);
                    }
                });
            case 2:
                if (this.mNextClusterForwardId == -1) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, this.mNextClusterForwardId);
            default:
                return null;
        }
    }

    static /* synthetic */ boolean lambda$findUserSetNextKeyboardNavigationCluster$0(int id, View t) {
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
        ArrayList<View> result = new ArrayList<>(24);
        addFocusables(result, direction);
        return result;
    }

    public void addFocusables(ArrayList<View> views, int direction) {
        addFocusables(views, direction, isInTouchMode() ? 1 : 0);
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (views == null || !canTakeFocus()) {
            return;
        }
        if (((focusableMode & 1) != 1 || isFocusableInTouchMode()) && !hasAncestorThatBlocksDescendantFocus()) {
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
        ArrayList<View> result = new ArrayList<>();
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

    public boolean requestAccessibilityFocus() {
        AccessibilityManager manager = AccessibilityManager.getInstance(this.mContext);
        if (!manager.isEnabled() || !manager.isTouchExplorationEnabled() || (this.mViewFlags & 12) != 0 || (this.mPrivateFlags2 & 67108864) != 0) {
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

    /* JADX WARNING: type inference failed for: r1v1, types: [android.view.ViewParent] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void sendAccessibilityHoverEvent(int eventType) {
        View source = this;
        while (!source.includeForAccessibility()) {
            ? source2 = source.getParent();
            if (source2 instanceof View) {
                source = source2;
            } else {
                return;
            }
        }
        source.sendAccessibilityEvent(eventType);
    }

    /* access modifiers changed from: package-private */
    public void clearAccessibilityFocusNoCallbacks(int action) {
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
        if (!canTakeFocus()) {
            return false;
        }
        if ((isInTouchMode() && 262144 != (this.mViewFlags & 262144)) || hasAncestorThatBlocksDescendantFocus()) {
            return false;
        }
        if (!isLayoutValid()) {
            this.mPrivateFlags |= 1;
        } else {
            clearParentsWantFocus();
        }
        handleFocusGainInternal(direction, previouslyFocusedRect);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void clearParentsWantFocus() {
        if (this.mParent instanceof View) {
            ((View) this.mParent).mPrivateFlags &= -2;
            ((View) this.mParent).clearParentsWantFocus();
        }
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

    @ViewDebug.ExportedProperty(category = "accessibility", mapping = {@ViewDebug.IntToString(from = 0, to = "auto"), @ViewDebug.IntToString(from = 1, to = "yes"), @ViewDebug.IntToString(from = 2, to = "no"), @ViewDebug.IntToString(from = 4, to = "noHideDescendants")})
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
            boolean oldIncludeForAccessibility = true;
            boolean hideDescendants = mode == 4;
            if (mode == 2 || hideDescendants) {
                View focusHost = findAccessibilityFocusHost(hideDescendants);
                if (focusHost != null) {
                    focusHost.clearAccessibilityFocus();
                }
            }
            boolean maySkipNotify = oldMode == 0 || mode == 0;
            if (!maySkipNotify || !includeForAccessibility()) {
                oldIncludeForAccessibility = false;
            }
            this.mPrivateFlags2 &= -7340033;
            this.mPrivateFlags2 |= (mode << 20) & PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_MASK;
            if (!maySkipNotify || oldIncludeForAccessibility != includeForAccessibility()) {
                notifySubtreeAccessibilityStateChangedIfNeeded();
            } else {
                notifyViewAccessibilityStateChangedIfNeeded(0);
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
                if (focusHost != null && ViewRootImpl.isViewDescendantOf(focusHost, this)) {
                    return focusHost;
                }
            }
        }
        return null;
    }

    public boolean isImportantForAccessibility() {
        int mode = (this.mPrivateFlags2 & PFLAG2_IMPORTANT_FOR_ACCESSIBILITY_MASK) >> 20;
        boolean z = false;
        if (mode == 2 || mode == 4) {
            return false;
        }
        for (ViewParent parent = this.mParent; parent instanceof View; parent = parent.getParent()) {
            if (((View) parent).getImportantForAccessibility() == 4) {
                return false;
            }
        }
        if (mode == 1 || isActionableForAccessibility() || hasListenersForAccessibility() || getAccessibilityNodeProvider() != null || getAccessibilityLiveRegion() != 0 || isAccessibilityPane()) {
            z = true;
        }
        return z;
    }

    public ViewParent getParentForAccessibility() {
        if (!(this.mParent instanceof View)) {
            return null;
        }
        if (((View) this.mParent).includeForAccessibility()) {
            return this.mParent;
        }
        return this.mParent.getParentForAccessibility();
    }

    /* access modifiers changed from: package-private */
    public View getSelfOrParentImportantForA11y() {
        if (isImportantForAccessibility()) {
            return this;
        }
        ViewParent parent = getParentForAccessibility();
        if (parent instanceof View) {
            return (View) parent;
        }
        return null;
    }

    public void addChildrenForAccessibility(ArrayList<View> arrayList) {
    }

    public boolean includeForAccessibility() {
        boolean z = false;
        if (this.mAttachInfo == null) {
            return false;
        }
        if ((this.mAttachInfo.mAccessibilityFetchFlags & 8) != 0 || isImportantForAccessibility()) {
            z = true;
        }
        return z;
    }

    public boolean isActionableForAccessibility() {
        return isClickable() || isLongClickable() || isFocusable();
    }

    private boolean hasListenersForAccessibility() {
        ListenerInfo info = getListenerInfo();
        return (this.mTouchDelegate == null && info.mOnKeyListener == null && info.mOnTouchListener == null && info.mOnGenericMotionListener == null && info.mOnHoverListener == null && info.mOnDragListener == null) ? false : true;
    }

    public void notifyViewAccessibilityStateChangedIfNeeded(int changeType) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && this.mAttachInfo != null) {
            if (changeType == 1 || !isAccessibilityPane() || !(getVisibility() == 0 || changeType == 32)) {
                if (getAccessibilityLiveRegion() != 0) {
                    AccessibilityEvent event = AccessibilityEvent.obtain();
                    event.setEventType(2048);
                    event.setContentChangeTypes(changeType);
                    sendAccessibilityEventUnchecked(event);
                } else if (this.mParent != null) {
                    try {
                        this.mParent.notifySubtreeAccessibilityStateChanged(this, this, changeType);
                    } catch (AbstractMethodError e) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                    }
                }
                return;
            }
            AccessibilityEvent event2 = AccessibilityEvent.obtain();
            event2.setEventType(32);
            event2.setContentChangeTypes(changeType);
            event2.setSource(this);
            onPopulateAccessibilityEvent(event2);
            if (this.mParent != null) {
                try {
                    this.mParent.requestSendAccessibilityEvent(this, event2);
                } catch (AbstractMethodError e2) {
                    Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e2);
                }
            }
        }
    }

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

    /* access modifiers changed from: package-private */
    public void resetSubtreeAccessibilityStateChanged() {
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
        int start;
        if (isNestedScrollingEnabled() && ((action == 8192 || action == 4096 || action == 16908344 || action == 16908345 || action == 16908346 || action == 16908347) && dispatchNestedPrePerformAccessibilityAction(action, arguments))) {
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
                    return !isFocused();
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
                    return !isSelected();
                }
                break;
            case 16:
                if (isClickable()) {
                    performClickInternal();
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
                int end = -1;
                if (arguments != null) {
                    start = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, -1);
                } else {
                    start = -1;
                }
                if (arguments != null) {
                    end = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, -1);
                }
                if (!(getAccessibilitySelectionStart() == start && getAccessibilitySelectionEnd() == end) && start == end) {
                    setAccessibilitySelection(start, end);
                    notifyViewAccessibilityStateChangedIfNeeded(0);
                    return true;
                }
            case 16908342:
                if (this.mAttachInfo != null) {
                    Rect r = this.mAttachInfo.mTmpInvalRect;
                    getDrawingRect(r);
                    return requestRectangleOnScreen(r, true);
                }
                break;
            case 16908348:
                if (isContextClickable()) {
                    performContextClick();
                    return true;
                }
                break;
            case 16908356:
                if (this.mTooltipInfo == null || this.mTooltipInfo.mTooltipPopup == null) {
                    return showLongClickTooltip(0, 0);
                }
                return false;
            case 16908357:
                if (this.mTooltipInfo == null || this.mTooltipInfo.mTooltipPopup == null) {
                    return false;
                }
                hideTooltip();
                return true;
        }
        return false;
    }

    private boolean traverseAtGranularity(int granularity, boolean forward, boolean extendSelection) {
        int selectionStart;
        int selectionEnd;
        int action;
        CharSequence text = getIterableTextForAccessibility();
        if (text == null || text.length() == 0) {
            return false;
        }
        AccessibilityIterators.TextSegmentIterator iterator = getIteratorForGranularity(granularity);
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
        int segmentStart = range[0];
        int segmentEnd = range[1];
        if (!extendSelection || !isAccessibilitySelectionExtendable()) {
            selectionStart = forward ? segmentEnd : segmentStart;
            selectionEnd = selectionStart;
        } else {
            selectionStart = getAccessibilitySelectionStart();
            if (selectionStart == -1) {
                selectionStart = forward ? segmentStart : segmentEnd;
            }
            selectionEnd = forward ? segmentEnd : segmentStart;
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

    public AccessibilityIterators.TextSegmentIterator getIteratorForGranularity(int granularity) {
        if (granularity != 8) {
            switch (granularity) {
                case 1:
                    CharSequence text = getIterableTextForAccessibility();
                    if (text != null && text.length() > 0) {
                        AccessibilityIterators.CharacterTextSegmentIterator iterator = AccessibilityIterators.CharacterTextSegmentIterator.getInstance(this.mContext.getResources().getConfiguration().locale);
                        iterator.initialize(text.toString());
                        return iterator;
                    }
                case 2:
                    CharSequence text2 = getIterableTextForAccessibility();
                    if (text2 != null && text2.length() > 0) {
                        AccessibilityIterators.WordTextSegmentIterator iterator2 = AccessibilityIterators.WordTextSegmentIterator.getInstance(this.mContext.getResources().getConfiguration().locale);
                        iterator2.initialize(text2.toString());
                        return iterator2;
                    }
            }
        } else {
            CharSequence text3 = getIterableTextForAccessibility();
            if (text3 != null && text3.length() > 0) {
                AccessibilityIterators.ParagraphTextSegmentIterator iterator3 = AccessibilityIterators.ParagraphTextSegmentIterator.getInstance();
                iterator3.initialize(text3.toString());
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

    public KeyEvent.DispatcherState getKeyDispatcherState() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mKeyDispatchState;
        }
        return null;
    }

    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        return onKeyPreIme(event.getKeyCode(), event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onKeyEvent(event, 0);
        }
        ListenerInfo li = this.mListenerInfo;
        if (li != null && li.mOnKeyListener != null && (this.mViewFlags & 32) == 0 && li.mOnKeyListener.onKey(this, event.getKeyCode(), event)) {
            return true;
        }
        if (event.dispatch(this, this.mAttachInfo != null ? this.mAttachInfo.mKeyDispatchState : null, this)) {
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
        if (Jlog.isMicroTest()) {
            Jlog.i(JlogConstants.JL_VIEW_TOUCH_BEGIN, Jlog.getMessage(VIEW_LOG_TAG, "dispatchTouchEvent", String.valueOf(event.getSequenceNumber()) + "&oper=" + event.getActionMasked()));
        }
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
        if (!result && this.mInputEventConsistencyVerifier != null) {
            this.mInputEventConsistencyVerifier.onUnhandledEvent(event, 0);
        }
        if (actionMasked == 1 || actionMasked == 3 || (actionMasked == 0 && !result)) {
            stopNestedScroll();
        }
        if (Jlog.isMicroTest()) {
            Jlog.i(JlogConstants.JL_VIEW_TOUCH_END, Jlog.getMessage(VIEW_LOG_TAG, "dispatchTouchEvent", String.valueOf(event.getSequenceNumber()) + "&oper=" + event.getActionMasked()));
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean isAccessibilityFocusedViewOrHost() {
        return isAccessibilityFocused() || (getViewRootImpl() != null && getViewRootImpl().getAccessibilityFocusedHost() == this);
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
                if (isContextClickable() && !this.mInContextButtonPress && !this.mHasPerformedLongPress && ((actionButton == 32 || actionButton == 2) && performContextClick(event.getX(), event.getY()))) {
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

    /* access modifiers changed from: protected */
    public boolean dispatchHoverEvent(MotionEvent event) {
        ListenerInfo li = this.mListenerInfo;
        if (li == null || li.mOnHoverListener == null || (this.mViewFlags & 32) != 0 || !li.mOnHoverListener.onHover(this, event)) {
            return onHoverEvent(event);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean hasHoveredChild() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean dispatchGenericPointerEvent(MotionEvent event) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean dispatchGenericFocusedEvent(MotionEvent event) {
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
        refreshDrawableState();
    }

    public boolean hasWindowFocus() {
        return this.mAttachInfo != null && this.mAttachInfo.mHasWindowFocus;
    }

    /* access modifiers changed from: protected */
    public void dispatchVisibilityChanged(View changedView, int visibility) {
        onVisibilityChanged(changedView, visibility);
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View changedView, int visibility) {
    }

    public void dispatchDisplayHint(int hint) {
        onDisplayHint(hint);
    }

    /* access modifiers changed from: protected */
    public void onDisplayHint(int hint) {
    }

    public void dispatchWindowVisibilityChanged(int visibility) {
        onWindowVisibilityChanged(visibility);
    }

    /* access modifiers changed from: protected */
    public void onWindowVisibilityChanged(int visibility) {
        if (visibility == 0) {
            initialAwakenScrollBars();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dispatchVisibilityAggregated(boolean isVisible) {
        boolean thisVisible = getVisibility() == 0;
        if (thisVisible || !isVisible) {
            onVisibilityAggregated(isVisible);
        }
        if (!thisVisible || !isVisible) {
            return false;
        }
        return true;
    }

    public void onVisibilityAggregated(boolean isVisible) {
        int i;
        int i2;
        boolean oldVisible = (this.mPrivateFlags3 & 536870912) != 0;
        if (isVisible) {
            i = 536870912 | this.mPrivateFlags3;
        } else {
            i = this.mPrivateFlags3 & -536870913;
        }
        this.mPrivateFlags3 = i;
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
            if (afm != null && getAutofillViewId() > 1073741823) {
                if (this.mVisibilityChangeForAutofillHandler != null) {
                    this.mVisibilityChangeForAutofillHandler.removeMessages(0);
                }
                if (isVisible) {
                    afm.notifyViewVisibilityChanged(this, true);
                } else {
                    if (this.mVisibilityChangeForAutofillHandler == null) {
                        this.mVisibilityChangeForAutofillHandler = new VisibilityChangeForAutofillHandler(afm, this);
                    }
                    this.mVisibilityChangeForAutofillHandler.obtainMessage(0, this).sendToTarget();
                }
            }
        }
        if (!TextUtils.isEmpty(getAccessibilityPaneTitle()) && isVisible != oldVisible) {
            if (isVisible) {
                i2 = 16;
            } else {
                i2 = 32;
            }
            notifyViewAccessibilityStateChangedIfNeeded(i2);
        }
    }

    public int getWindowVisibility() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mWindowVisibility;
        }
        return 8;
    }

    private void adjustRectSizeWhenInCompat(Rect inoutRect) {
        Resources resources = getResources();
        if (resources != null && !resources.getCompatibilityInfo().supportsScreen()) {
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
                adjustRectSizeWhenInCompat(outRect);
                Rect insets = this.mAttachInfo.mVisibleInsets;
                outRect.left += insets.left;
                outRect.top += insets.top;
                outRect.right -= insets.right;
                outRect.bottom -= insets.bottom;
            } catch (RemoteException e) {
            }
        } else {
            DisplayManagerGlobal.getInstance().getRealDisplay(0).getRectSize(outRect);
            adjustRectSizeWhenInCompat(outRect);
        }
    }

    public void getWindowDisplayFrame(Rect outRect) {
        if (this.mAttachInfo != null) {
            try {
                this.mAttachInfo.mSession.getDisplayFrame(this.mAttachInfo.mWindow, outRect);
            } catch (RemoteException e) {
            }
        } else {
            DisplayManagerGlobal.getInstance().getRealDisplay(0).getRectSize(outRect);
        }
    }

    public void dispatchConfigurationChanged(Configuration newConfig) {
        onConfigurationChanged(newConfig);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
    }

    /* access modifiers changed from: package-private */
    public void dispatchCollectViewAttributes(AttachInfo attachInfo, int visibility) {
        performCollectViewAttributes(attachInfo, visibility);
    }

    /* access modifiers changed from: package-private */
    public void performCollectViewAttributes(AttachInfo attachInfo, int visibility) {
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

    /* access modifiers changed from: package-private */
    public void needGlobalAttributesUpdate(boolean force) {
        AttachInfo ai = this.mAttachInfo;
        if (ai != null && !ai.mRecomputeGlobalAttributes) {
            if (force || ai.mKeepScreenOn || ai.mSystemUiVisibility != 0 || ai.mHasSystemUiListeners) {
                ai.mRecomputeGlobalAttributes = true;
            }
        }
    }

    @ViewDebug.ExportedProperty
    public boolean isInTouchMode() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mInTouchMode;
        }
        return ViewRootImpl.isInTouchMode();
    }

    @ViewDebug.CapturedViewProperty
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
                boolean clickable = (this.mViewFlags & 16384) == 16384 || (this.mViewFlags & 2097152) == 2097152;
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
                        return performClickInternal();
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
        ContextMenu.ContextMenuInfo menuInfo = getContextMenuInfo();
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

    /* access modifiers changed from: protected */
    public ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void onCreateContextMenu(ContextMenu menu) {
    }

    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return false;
    }

    public boolean onHoverEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (!this.mSendingHoverAccessibilityEvents) {
            if ((action == 9 || action == 7) && !hasHoveredChild() && pointInView(event.getX(), event.getY())) {
                sendAccessibilityHoverEvent(128);
                this.mSendingHoverAccessibilityEvents = true;
            }
        } else if (action == 10 || (action == 2 && !pointInView(event.getX(), event.getY()))) {
            this.mSendingHoverAccessibilityEvents = false;
            sendAccessibilityHoverEvent(256);
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
        int viewFlags = this.mViewFlags;
        boolean z = false;
        if ((viewFlags & 32) == 32) {
            return false;
        }
        if ((viewFlags & 16384) == 16384 || (viewFlags & 2097152) == 2097152 || (viewFlags & 8388608) == 8388608) {
            z = true;
        }
        return z;
    }

    @ViewDebug.ExportedProperty
    public boolean isHovered() {
        return (this.mPrivateFlags & 268435456) != 0;
    }

    public void setHovered(boolean hovered) {
        if (hovered) {
            if ((this.mPrivateFlags & 268435456) == 0) {
                this.mPrivateFlags = 268435456 | this.mPrivateFlags;
                refreshDrawableState();
                onHoverChanged(true);
            }
        } else if ((268435456 & this.mPrivateFlags) != 0) {
            this.mPrivateFlags &= -268435457;
            refreshDrawableState();
            onHoverChanged(false);
        }
    }

    public void onHoverChanged(boolean hovered) {
    }

    /* access modifiers changed from: protected */
    public boolean handleScrollBarDragging(MotionEvent event) {
        MotionEvent motionEvent = event;
        if (this.mScrollCache == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        if ((this.mScrollCache.mScrollBarDraggingState != 0 || action == 0) && motionEvent.isFromSource(InputDevice.SOURCE_MOUSE) && motionEvent.isButtonPressed(1)) {
            if (action != 0) {
                if (action == 2) {
                    if (this.mScrollCache.mScrollBarDraggingState == 0) {
                        return false;
                    }
                    if (this.mScrollCache.mScrollBarDraggingState == 1) {
                        Rect bounds = this.mScrollCache.mScrollBarBounds;
                        getVerticalScrollBarBounds(bounds, null);
                        int range = computeVerticalScrollRange();
                        int offset = computeVerticalScrollOffset();
                        int extent = computeVerticalScrollExtent();
                        int thumbLength = ScrollBarUtils.getThumbLength(bounds.height(), bounds.width(), extent, range);
                        int thumbOffset = ScrollBarUtils.getThumbOffset(bounds.height(), thumbLength, extent, range, offset);
                        float maxThumbOffset = (float) (bounds.height() - thumbLength);
                        float newThumbOffset = Math.min(Math.max(((float) thumbOffset) + (y - this.mScrollCache.mScrollBarDraggingPos), 0.0f), maxThumbOffset);
                        int height = getHeight();
                        if (Math.round(newThumbOffset) == thumbOffset || maxThumbOffset <= 0.0f || height <= 0 || extent <= 0) {
                        } else {
                            Rect rect = bounds;
                            int newY = Math.round((((float) (range - extent)) / (((float) extent) / ((float) height))) * (newThumbOffset / maxThumbOffset));
                            if (newY != getScrollY()) {
                                this.mScrollCache.mScrollBarDraggingPos = y;
                                setScrollY(newY);
                            }
                        }
                        return true;
                    } else if (this.mScrollCache.mScrollBarDraggingState == 2) {
                        Rect bounds2 = this.mScrollCache.mScrollBarBounds;
                        getHorizontalScrollBarBounds(bounds2, null);
                        int range2 = computeHorizontalScrollRange();
                        int offset2 = computeHorizontalScrollOffset();
                        int extent2 = computeHorizontalScrollExtent();
                        int thumbLength2 = ScrollBarUtils.getThumbLength(bounds2.width(), bounds2.height(), extent2, range2);
                        int thumbOffset2 = ScrollBarUtils.getThumbOffset(bounds2.width(), thumbLength2, extent2, range2, offset2);
                        float maxThumbOffset2 = (float) (bounds2.width() - thumbLength2);
                        float newThumbOffset2 = Math.min(Math.max(((float) thumbOffset2) + (x - this.mScrollCache.mScrollBarDraggingPos), 0.0f), maxThumbOffset2);
                        int width = getWidth();
                        if (Math.round(newThumbOffset2) == thumbOffset2 || maxThumbOffset2 <= 0.0f || width <= 0 || extent2 <= 0) {
                        } else {
                            Rect rect2 = bounds2;
                            int newX = Math.round((((float) (range2 - extent2)) / (((float) extent2) / ((float) width))) * (newThumbOffset2 / maxThumbOffset2));
                            if (newX != getScrollX()) {
                                this.mScrollCache.mScrollBarDraggingPos = x;
                                setScrollX(newX);
                            }
                        }
                        return true;
                    }
                }
                this.mScrollCache.mScrollBarDraggingState = 0;
                return false;
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
        boolean clickable = (viewFlags & 16384) == 16384 || (viewFlags & 2097152) == 2097152 || (viewFlags & 8388608) == 8388608;
        if ((viewFlags & 32) == 32) {
            if (action == 1 && (16384 & this.mPrivateFlags) != 0) {
                setPressed(false);
            }
            this.mPrivateFlags3 &= -131073;
            return clickable;
        } else if (this.mTouchDelegate != null && this.mTouchDelegate.onTouchEvent(event)) {
            return true;
        } else {
            if (!clickable && (viewFlags & 1073741824) != 1073741824) {
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
                            } else {
                                this.mPrivateFlags |= 33554432;
                                if (this.mPendingCheckForTap == null) {
                                    this.mPendingCheckForTap = new CheckForTap();
                                }
                                this.mPendingCheckForTap.x = event.getX();
                                this.mPendingCheckForTap.y = event.getY();
                                postDelayed(this.mPendingCheckForTap, (long) ViewConfiguration.getTapTimeout());
                                break;
                            }
                        }
                    } else {
                        checkForLongClick(0, x, y);
                        break;
                    }
                    break;
                case 1:
                    this.mPrivateFlags3 = -131073 & this.mPrivateFlags3;
                    if ((viewFlags & 1073741824) == 1073741824) {
                        handleTooltipUp();
                    }
                    if (clickable) {
                        boolean prepressed = (this.mPrivateFlags & 33554432) != 0;
                        if ((16384 & this.mPrivateFlags) != 0 || prepressed) {
                            boolean focusTaken = false;
                            if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
                                focusTaken = requestFocus();
                            }
                            if (prepressed) {
                                setPressed(true, x, y);
                            }
                            if (!this.mHasPerformedLongPress && !this.mIgnoreNextUpEvent) {
                                removeLongPressCallback();
                                if (!focusTaken) {
                                    if (this.mPerformClick == null) {
                                        this.mPerformClick = new PerformClick();
                                    }
                                    if ((mtouchEventOptEnable && !postAtFrontOfQueue(this.mPerformClick)) || (!mtouchEventOptEnable && !post(this.mPerformClick))) {
                                        performClickInternal();
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
                        this.mIgnoreNextUpEvent = false;
                        break;
                    } else {
                        removeTapCallback();
                        removeLongPressCallback();
                        this.mInContextButtonPress = false;
                        this.mHasPerformedLongPress = false;
                        this.mIgnoreNextUpEvent = false;
                        break;
                    }
                case 2:
                    if (clickable) {
                        drawableHotspotChanged(x, y);
                    }
                    if (!pointInView(x, y, (float) this.mTouchSlop)) {
                        removeTapCallback();
                        removeLongPressCallback();
                        if ((16384 & this.mPrivateFlags) != 0) {
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

    public void setTouchDelegate(TouchDelegate delegate) {
        this.mTouchDelegate = delegate;
    }

    public TouchDelegate getTouchDelegate() {
        return this.mTouchDelegate;
    }

    public final void requestUnbufferedDispatch(MotionEvent event) {
        int action = event.getAction();
        if (this.mAttachInfo != null && ((action == 0 || action == 2) && event.isTouchEvent())) {
            this.mAttachInfo.mUnbufferedDispatchRequested = true;
        }
    }

    private boolean hasSize() {
        return this.mBottom > this.mTop && this.mRight > this.mLeft;
    }

    private boolean canTakeFocus() {
        if ((this.mViewFlags & 12) == 0 && (this.mViewFlags & 1) == 1 && (this.mViewFlags & 32) == 0 && (sCanFocusZeroSized || !isLayoutValid() || hasSize())) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setFlags(int flags, int mask) {
        int newFocus;
        boolean accessibilityEnabled = AccessibilityManager.getInstance(this.mContext).isEnabled();
        boolean oldIncludeForAccessibility = accessibilityEnabled && includeForAccessibility();
        int old = this.mViewFlags;
        this.mViewFlags = (this.mViewFlags & (~mask)) | (flags & mask);
        int changed = this.mViewFlags ^ old;
        if (changed != 0) {
            int privateFlags = this.mPrivateFlags;
            boolean shouldNotifyFocusableAvailable = false;
            int focusableChangedByAuto = 0;
            if (!((this.mViewFlags & 16) == 0 || (changed & BatteryStats.HistoryItem.EVENT_TEMP_WHITELIST_FINISH) == 0)) {
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
                    if (this.mParent instanceof ViewGroup) {
                        ((ViewGroup) this.mParent).clearFocusedInCluster();
                    }
                } else if ((old & 1) == 0 && (privateFlags & 2) == 0 && this.mParent != null) {
                    ViewRootImpl viewRootImpl = getViewRootImpl();
                    if (!sAutoFocusableOffUIThreadWontNotifyParents || focusableChangedByAuto == 0 || viewRootImpl == null || viewRootImpl.mThread == Thread.currentThread()) {
                        shouldNotifyFocusableAvailable = canTakeFocus();
                    }
                }
            }
            int newVisibility = flags & 12;
            if (newVisibility == 0 && (changed & 12) != 0) {
                this.mPrivateFlags |= 32;
                invalidate(true);
                needGlobalAttributesUpdate(true);
                shouldNotifyFocusableAvailable = hasSize();
            }
            if ((changed & 32) != 0) {
                if ((this.mViewFlags & 32) == 0) {
                    shouldNotifyFocusableAvailable = canTakeFocus();
                } else if (isFocused()) {
                    clearFocus();
                }
            }
            if (shouldNotifyFocusableAvailable && this.mParent != null) {
                this.mParent.focusableViewAvailable(this);
            }
            if ((changed & 8) != 0) {
                needGlobalAttributesUpdate(false);
                requestLayout();
                if ((this.mViewFlags & 12) == 8) {
                    if (hasFocus()) {
                        clearFocus();
                        if (this.mParent instanceof ViewGroup) {
                            ((ViewGroup) this.mParent).clearFocusedInCluster();
                        }
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
                        if (this.mParent instanceof ViewGroup) {
                            ((ViewGroup) this.mParent).clearFocusedInCluster();
                        }
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
            if (!((67108864 & changed) == 0 || this.mParent == null || this.mAttachInfo == null || this.mAttachInfo.mRecomputeGlobalAttributes)) {
                this.mParent.recomputeViewAttributes(this);
            }
            if (accessibilityEnabled) {
                if (isAccessibilityPane()) {
                    changed &= -13;
                }
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

    /* access modifiers changed from: protected */
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        notifySubtreeAccessibilityStateChangedIfNeeded();
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            postSendViewScrolledAccessibilityEventCallback(l - oldl, t - oldt);
        }
        this.mBackgroundSizeChanged = true;
        this.mDefaultFocusHighlightSizeChanged = true;
        if (this.mForegroundInfo != null) {
            boolean unused = this.mForegroundInfo.mBoundsChanged = true;
        }
        AttachInfo ai = this.mAttachInfo;
        if (ai != null) {
            ai.mViewScrollChanged = true;
        }
        if (this.mListenerInfo != null && this.mListenerInfo.mOnScrollChangeListener != null) {
            this.mListenerInfo.mOnScrollChangeListener.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
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

    @ViewDebug.ExportedProperty(category = "layout")
    public final int getWidth() {
        return this.mRight - this.mLeft;
    }

    @ViewDebug.ExportedProperty(category = "layout")
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

    @ViewDebug.ExportedProperty(category = "measurement", flagMapping = {@ViewDebug.FlagToString(equals = 16777216, mask = -16777216, name = "MEASURED_STATE_TOO_SMALL")})
    public final int getMeasuredWidthAndState() {
        return this.mMeasuredWidth;
    }

    public final int getMeasuredHeight() {
        return this.mMeasuredHeight & 16777215;
    }

    @ViewDebug.ExportedProperty(category = "measurement", flagMapping = {@ViewDebug.FlagToString(equals = 16777216, mask = -16777216, name = "MEASURED_STATE_TOO_SMALL")})
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

    /* access modifiers changed from: package-private */
    public final boolean hasIdentityMatrix() {
        return this.mRenderNode.hasIdentityMatrix();
    }

    /* access modifiers changed from: package-private */
    public void ensureTransformationInfo() {
        if (this.mTransformationInfo == null) {
            this.mTransformationInfo = new TransformationInfo();
        }
    }

    public final Matrix getInverseMatrix() {
        ensureTransformationInfo();
        if (this.mTransformationInfo.mInverseMatrix == null) {
            Matrix unused = this.mTransformationInfo.mInverseMatrix = new Matrix();
        }
        Matrix matrix = this.mTransformationInfo.mInverseMatrix;
        this.mRenderNode.getInverseMatrix(matrix);
        return matrix;
    }

    public float getCameraDistance() {
        return -(this.mRenderNode.getCameraDistance() * ((float) this.mResources.getDisplayMetrics().densityDpi));
    }

    public void setCameraDistance(float distance) {
        invalidateViewProperty(true, false);
        this.mRenderNode.setCameraDistance((-Math.abs(distance)) / ((float) this.mResources.getDisplayMetrics().densityDpi));
        invalidateViewProperty(false, false);
        invalidateParentIfNeededAndWasQuickRejected();
    }

    @ViewDebug.ExportedProperty(category = "drawing")
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

    @ViewDebug.ExportedProperty(category = "drawing")
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

    @ViewDebug.ExportedProperty(category = "drawing")
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

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getScaleX() {
        return this.mRenderNode.getScaleX();
    }

    public void setScaleX(float scaleX) {
        if (scaleX != getScaleX()) {
            float scaleX2 = sanitizeFloatPropertyValue(scaleX, "scaleX");
            invalidateViewProperty(true, false);
            this.mRenderNode.setScaleX(scaleX2);
            invalidateViewProperty(false, true);
            invalidateParentIfNeededAndWasQuickRejected();
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getScaleY() {
        return this.mRenderNode.getScaleY();
    }

    public void setScaleY(float scaleY) {
        if (scaleY != getScaleY()) {
            float scaleY2 = sanitizeFloatPropertyValue(scaleY, "scaleY");
            invalidateViewProperty(true, false);
            this.mRenderNode.setScaleY(scaleY2);
            invalidateViewProperty(false, true);
            invalidateParentIfNeededAndWasQuickRejected();
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing")
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

    @ViewDebug.ExportedProperty(category = "drawing")
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

    public boolean isPivotSet() {
        return this.mRenderNode.isPivotExplicitlySet();
    }

    public void resetPivot() {
        if (this.mRenderNode.resetPivot()) {
            invalidateViewProperty(false, false);
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getAlpha() {
        if (this.mTransformationInfo != null) {
            return this.mTransformationInfo.mAlpha;
        }
        return 1.0f;
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
        if ((this.mPrivateFlags3 & 16777216) != 0) {
            return (this.mPrivateFlags3 & 8388608) != 0;
        }
        return hasOverlappingRendering();
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean hasOverlappingRendering() {
        return true;
    }

    public void setAlpha(float alpha) {
        ensureTransformationInfo();
        if (this.mTransformationInfo.mAlpha != alpha) {
            setAlphaInternal(alpha);
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

    /* access modifiers changed from: package-private */
    public boolean setAlphaNoInvalidation(float alpha) {
        ensureTransformationInfo();
        if (this.mTransformationInfo.mAlpha != alpha) {
            setAlphaInternal(alpha);
            if (onSetAlpha((int) (255.0f * alpha))) {
                this.mPrivateFlags |= 262144;
                return true;
            }
            this.mPrivateFlags &= -262145;
            this.mRenderNode.setAlpha(getFinalAlpha());
        }
        return false;
    }

    private void setAlphaInternal(float alpha) {
        float oldAlpha = this.mTransformationInfo.mAlpha;
        this.mTransformationInfo.mAlpha = alpha;
        boolean z = false;
        boolean z2 = alpha == 0.0f;
        if (oldAlpha == 0.0f) {
            z = true;
        }
        if (z2 ^ z) {
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
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

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getTransitionAlpha() {
        if (this.mTransformationInfo != null) {
            return this.mTransformationInfo.mTransitionAlpha;
        }
        return 1.0f;
    }

    @ViewDebug.CapturedViewProperty
    public final int getTop() {
        return this.mTop;
    }

    public final void setTop(int top) {
        int yLoc;
        int minTop;
        if (top != this.mTop) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidate(true);
            } else if (this.mAttachInfo != null) {
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
                boolean unused = this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & 268435456) == 268435456) {
                invalidateParentIfNeeded();
            }
        }
    }

    @ViewDebug.CapturedViewProperty
    public final int getBottom() {
        return this.mBottom;
    }

    public boolean isDirty() {
        return (this.mPrivateFlags & PFLAG_DIRTY_MASK) != 0;
    }

    public final void setBottom(int bottom) {
        int maxBottom;
        if (bottom != this.mBottom) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidate(true);
            } else if (this.mAttachInfo != null) {
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
                boolean unused = this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & 268435456) == 268435456) {
                invalidateParentIfNeeded();
            }
        }
    }

    @ViewDebug.CapturedViewProperty
    public final int getLeft() {
        return this.mLeft;
    }

    public final void setLeft(int left) {
        int xLoc;
        int minLeft;
        if (left != this.mLeft) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidate(true);
            } else if (this.mAttachInfo != null) {
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
                boolean unused = this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & 268435456) == 268435456) {
                invalidateParentIfNeeded();
            }
        }
    }

    @ViewDebug.CapturedViewProperty
    public final int getRight() {
        return this.mRight;
    }

    public final void setRight(int right) {
        int maxRight;
        if (right != this.mRight) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidate(true);
            } else if (this.mAttachInfo != null) {
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
                boolean unused = this.mForegroundInfo.mBoundsChanged = true;
            }
            invalidateParentIfNeeded();
            if ((this.mPrivateFlags2 & 268435456) == 268435456) {
                invalidateParentIfNeeded();
            }
        }
    }

    private static float sanitizeFloatPropertyValue(float value, String propertyName) {
        return sanitizeFloatPropertyValue(value, propertyName, -3.4028235E38f, Float.MAX_VALUE);
    }

    private static float sanitizeFloatPropertyValue(float value, String propertyName, float min, float max) {
        if (value >= min && value <= max) {
            return value;
        }
        if (value < min || value == Float.NEGATIVE_INFINITY) {
            if (!sThrowOnInvalidFloatProperties) {
                return min;
            }
            throw new IllegalArgumentException("Cannot set '" + propertyName + "' to " + value + ", the value must be >= " + min);
        } else if (value > max || value == Float.POSITIVE_INFINITY) {
            if (!sThrowOnInvalidFloatProperties) {
                return max;
            }
            throw new IllegalArgumentException("Cannot set '" + propertyName + "' to " + value + ", the value must be <= " + max);
        } else if (!Float.isNaN(value)) {
            throw new IllegalStateException("How do you get here?? " + value);
        } else if (!sThrowOnInvalidFloatProperties) {
            return 0.0f;
        } else {
            throw new IllegalArgumentException("Cannot set '" + propertyName + "' to Float.NaN");
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getX() {
        return ((float) this.mLeft) + getTranslationX();
    }

    public void setX(float x) {
        setTranslationX(x - ((float) this.mLeft));
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getY() {
        return ((float) this.mTop) + getTranslationY();
    }

    public void setY(float y) {
        setTranslationY(y - ((float) this.mTop));
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getZ() {
        return getElevation() + getTranslationZ();
    }

    public void setZ(float z) {
        setTranslationZ(z - getElevation());
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getElevation() {
        return this.mRenderNode.getElevation();
    }

    public void setElevation(float elevation) {
        if (elevation != getElevation()) {
            float elevation2 = sanitizeFloatPropertyValue(elevation, "elevation");
            invalidateViewProperty(true, false);
            this.mRenderNode.setElevation(elevation2);
            invalidateViewProperty(false, true);
            invalidateParentIfNeededAndWasQuickRejected();
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getTranslationX() {
        return this.mRenderNode.getTranslationX();
    }

    public void setTranslationX(float translationX) {
        if (translationX != getTranslationX() && (!HwFreeFormUtils.isFreeFormEnable() || !HwFreeFormUtils.getHideAnimator() || this.mContext.getPackageName() == null || !this.mContext.getPackageName().equals("com.tencent.mm") || !(this instanceof DecorCaptionView))) {
            invalidateViewProperty(true, false);
            this.mRenderNode.setTranslationX(translationX);
            invalidateViewProperty(false, true);
            invalidateParentIfNeededAndWasQuickRejected();
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing")
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

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getTranslationZ() {
        return this.mRenderNode.getTranslationZ();
    }

    public void setTranslationZ(float translationZ) {
        if (translationZ != getTranslationZ()) {
            float translationZ2 = sanitizeFloatPropertyValue(translationZ, "translationZ");
            invalidateViewProperty(true, false);
            this.mRenderNode.setTranslationZ(translationZ2);
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

    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean hasShadow() {
        return this.mRenderNode.hasShadow();
    }

    public void setOutlineSpotShadowColor(int color) {
        if (this.mRenderNode.setSpotShadowColor(color)) {
            invalidateViewProperty(true, true);
        }
    }

    public int getOutlineSpotShadowColor() {
        return this.mRenderNode.getSpotShadowColor();
    }

    public void setOutlineAmbientShadowColor(int color) {
        if (this.mRenderNode.setAmbientShadowColor(color)) {
            invalidateViewProperty(true, true);
        }
    }

    public int getOutlineAmbientShadowColor() {
        return this.mRenderNode.getAmbientShadowColor();
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

    /* access modifiers changed from: package-private */
    public final boolean pointInView(float localX, float localY) {
        return pointInView(localX, localY, 0.0f);
    }

    public boolean pointInView(float localX, float localY, float slop) {
        return localX >= (-slop) && localY >= (-slop) && localX < ((float) (this.mRight - this.mLeft)) + slop && localY < ((float) (this.mBottom - this.mTop)) + slop;
    }

    public void getFocusedRect(Rect r) {
        getDrawingRect(r);
    }

    public boolean getGlobalVisibleRect(Rect r, Point globalOffset) {
        int width = this.mRight - this.mLeft;
        int height = this.mBottom - this.mTop;
        boolean z = false;
        if (width <= 0 || height <= 0) {
            return false;
        }
        r.set(0, 0, width, height);
        if (globalOffset != null) {
            globalOffset.set(-this.mScrollX, -this.mScrollY);
        }
        if (this.mParent == null || this.mParent.getChildVisibleRect(this, r, globalOffset)) {
            z = true;
        }
        return z;
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
        int yLoc;
        int maxBottom;
        int minTop;
        if (offset != 0) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidateViewProperty(false, false);
            } else if (isHardwareAccelerated()) {
                invalidateViewProperty(false, false);
            } else {
                ViewParent p = this.mParent;
                if (!(p == null || this.mAttachInfo == null)) {
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
        int maxRight;
        int minLeft;
        if (offset != 0) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidateViewProperty(false, false);
            } else if (isHardwareAccelerated()) {
                invalidateViewProperty(false, false);
            } else {
                ViewParent p = this.mParent;
                if (!(p == null || this.mAttachInfo == null)) {
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

    @ViewDebug.ExportedProperty(deepExport = true, prefix = "layout_")
    public ViewGroup.LayoutParams getLayoutParams() {
        return this.mLayoutParams;
    }

    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params != null) {
            this.mLayoutParams = params;
            resolveLayoutParams();
            if (this.mParent instanceof ViewGroup) {
                ((ViewGroup) this.mParent).onSetLayoutParams(this, params);
            }
            requestLayout();
            return;
        }
        throw new NullPointerException("Layout parameters cannot be null");
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

    /* access modifiers changed from: protected */
    public boolean awakenScrollBars() {
        if (this.mScrollCache == null || !awakenScrollBars(this.mScrollCache.scrollBarDefaultDelayBeforeFade, true)) {
            return false;
        }
        return true;
    }

    private boolean initialAwakenScrollBars() {
        if (this.mScrollCache == null || !awakenScrollBars(this.mScrollCache.scrollBarDefaultDelayBeforeFade * 4, true)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean awakenScrollBars(int startDelay) {
        return awakenScrollBars(startDelay, true);
    }

    /* access modifiers changed from: protected */
    public boolean awakenScrollBars(int startDelay, boolean invalidate) {
        ScrollabilityCache scrollCache = this.mScrollCache;
        if (scrollCache == null || !scrollCache.fadeScrollBars) {
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
            startDelay = Math.max(750, startDelay);
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
        return (this.mViewFlags & 12) != 0 && this.mCurrentAnimation == null && (!(this.mParent instanceof ViewGroup) || !((ViewGroup) this.mParent).isViewTransitioning(this));
    }

    @Deprecated
    public void invalidate(Rect dirty) {
        int scrollX = this.mScrollX;
        int scrollY = this.mScrollY;
        invalidateInternal(dirty.left - scrollX, dirty.top - scrollY, dirty.right - scrollX, dirty.bottom - scrollY, true, false);
    }

    @Deprecated
    public void invalidate(int l, int t, int r, int b) {
        int scrollX = this.mScrollX;
        int scrollY = this.mScrollY;
        invalidateInternal(l - scrollX, t - scrollY, r - scrollX, b - scrollY, true, false);
    }

    public void invalidate() {
        invalidate(true);
    }

    public void invalidate(boolean invalidateCache) {
        invalidateInternal(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop, invalidateCache, true);
    }

    private void savePartialUpdateDirty(int l, int t, int r, int b) {
        if (this.mIsSupportPartialUpdate) {
            this.mCurrentInvalRect.union(l, t, r, b);
            if (this.mIsPartialUpdateDebugOn && !this.mHwApsImpl.isInPowerTest()) {
                Log.i("partial", "APS: PU, invalidate, pkgname=" + getContext().getPackageName() + ", union dirty = (" + l + "," + t + "," + r + "," + b + "), now mCurrentInvalRect = " + this.mCurrentInvalRect + ", classname = " + getClass().getSimpleName());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateInternal(int l, int t, int r, int b, boolean invalidateCache, boolean fullInvalidate) {
        if (this.mGhostView != null) {
            this.mGhostView.invalidate(true);
        } else if (!skipInvalidate()) {
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
                    if (!fullInvalidate) {
                        savePartialUpdateDirty(l, t, r, b);
                    } else {
                        savePartialUpdateDirty(0, 0, getWidth(), getHeight());
                    }
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

    /* access modifiers changed from: package-private */
    public void invalidateViewProperty(boolean invalidateParent, boolean forceRedraw) {
        if (!isHardwareAccelerated() || !this.mRenderNode.isValid() || (this.mPrivateFlags & 64) != 0) {
            if (invalidateParent) {
                invalidateParentCaches();
            }
            if (forceRedraw) {
                this.mPrivateFlags |= 32;
            }
            invalidate(false);
            return;
        }
        damageInParent();
    }

    /* access modifiers changed from: protected */
    public void damageInParent() {
        if (this.mParent != null && this.mAttachInfo != null) {
            this.mParent.onDescendantInvalidated(this, this);
        }
    }

    /* access modifiers changed from: package-private */
    public void transformRect(Rect rect) {
        if (!getMatrix().isIdentity()) {
            RectF boundingRect = this.mAttachInfo.mTmpTransformRect;
            boundingRect.set(rect);
            getMatrix().mapRect(boundingRect);
            rect.set((int) Math.floor((double) boundingRect.left), (int) Math.floor((double) boundingRect.top), (int) Math.ceil((double) boundingRect.right), (int) Math.ceil((double) boundingRect.bottom));
        }
    }

    /* access modifiers changed from: protected */
    public void invalidateParentCaches() {
        if (this.mParent instanceof View) {
            ((View) this.mParent).mPrivateFlags |= Integer.MIN_VALUE;
        }
    }

    /* access modifiers changed from: protected */
    public void invalidateParentIfNeeded() {
        if (isHardwareAccelerated() && (this.mParent instanceof View)) {
            ((View) this.mParent).invalidate(true);
        }
    }

    /* access modifiers changed from: protected */
    public void invalidateParentIfNeededAndWasQuickRejected() {
        if ((this.mPrivateFlags2 & 268435456) != 0) {
            invalidateParentIfNeeded();
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean isOpaque() {
        return (this.mPrivateFlags & 25165824) == 25165824 && getFinalAlpha() >= 1.0f;
    }

    /* access modifiers changed from: protected */
    public void computeOpaqueFlags() {
        if (this.mBackground == null || this.mBackground.getOpacity() != -1) {
            this.mPrivateFlags &= -8388609;
        } else {
            this.mPrivateFlags |= 8388608;
        }
        int flags = this.mViewFlags;
        if (((flags & 512) == 0 && (flags & 256) == 0) || (flags & 50331648) == 0 || (50331648 & flags) == 33554432) {
            this.mPrivateFlags |= 16777216;
        } else {
            this.mPrivateFlags &= -16777217;
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasOpaqueScrollbars() {
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
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mThreadedRenderer;
        }
        return null;
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
            AttachInfo.InvalidateInfo info = AttachInfo.InvalidateInfo.obtain();
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
            sTriggerFlag = true;
        }
    }

    public void postInvalidateOnAnimation(int left, int top, int right, int bottom) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            AttachInfo.InvalidateInfo info = AttachInfo.InvalidateInfo.obtain();
            info.target = this;
            info.left = left;
            info.top = top;
            info.right = right;
            info.bottom = bottom;
            attachInfo.mViewRootImpl.dispatchInvalidateRectOnAnimation(info);
        }
    }

    private void postSendViewScrolledAccessibilityEventCallback(int dx, int dy) {
        if (this.mSendViewScrolledAccessibilityEvent == null) {
            this.mSendViewScrolledAccessibilityEvent = new SendViewScrolledAccessibilityEvent();
        }
        this.mSendViewScrolledAccessibilityEvent.post(dx, dy);
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

    /* access modifiers changed from: protected */
    public float getTopFadingEdgeStrength() {
        return computeVerticalScrollOffset() > 0 ? 1.0f : 0.0f;
    }

    /* access modifiers changed from: protected */
    public float getBottomFadingEdgeStrength() {
        return computeVerticalScrollOffset() + computeVerticalScrollExtent() < computeVerticalScrollRange() ? 1.0f : 0.0f;
    }

    /* access modifiers changed from: protected */
    public float getLeftFadingEdgeStrength() {
        return computeHorizontalScrollOffset() > 0 ? 1.0f : 0.0f;
    }

    /* access modifiers changed from: protected */
    public float getRightFadingEdgeStrength() {
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

    /* access modifiers changed from: protected */
    public void recomputePadding() {
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
        return this.mScrollCache != null && this.mScrollCache.fadeScrollBars;
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
            this.mViewFlags = (this.mViewFlags & -50331649) | (50331648 & style);
            computeOpaqueFlags();
            resolvePadding();
        }
    }

    @ViewDebug.ExportedProperty(mapping = {@ViewDebug.IntToString(from = 0, to = "INSIDE_OVERLAY"), @ViewDebug.IntToString(from = 16777216, to = "INSIDE_INSET"), @ViewDebug.IntToString(from = 33554432, to = "OUTSIDE_OVERLAY"), @ViewDebug.IntToString(from = 50331648, to = "OUTSIDE_INSET")})
    public int getScrollBarStyle() {
        return this.mViewFlags & 50331648;
    }

    /* access modifiers changed from: protected */
    public int computeHorizontalScrollRange() {
        return getWidth();
    }

    /* access modifiers changed from: protected */
    public int computeHorizontalScrollOffset() {
        return this.mScrollX;
    }

    /* access modifiers changed from: protected */
    public int computeHorizontalScrollExtent() {
        return getWidth();
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollRange() {
        return getHeight();
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollOffset() {
        return this.mScrollY;
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollExtent() {
        return getHeight();
    }

    public boolean canScrollHorizontally(int direction) {
        int offset = computeHorizontalScrollOffset();
        boolean z = false;
        if (computeHorizontalScrollRange() - computeHorizontalScrollExtent() == 0) {
            return false;
        }
        if (direction < 0) {
            if (offset > 0) {
                z = true;
            }
            return z;
        }
        if (offset < range - 1) {
            z = true;
        }
        return z;
    }

    public boolean canScrollVertically(int direction) {
        int offset = computeVerticalScrollOffset();
        boolean z = false;
        if (computeVerticalScrollRange() - computeVerticalScrollExtent() == 0) {
            return false;
        }
        if (direction < 0) {
            if (offset > 0) {
                z = true;
            }
            return z;
        }
        if (offset < range - 1) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void getScrollIndicatorBounds(Rect out) {
        out.left = this.mScrollX;
        out.right = (this.mScrollX + this.mRight) - this.mLeft;
        out.top = this.mScrollY;
        out.bottom = (this.mScrollY + this.mBottom) - this.mTop;
    }

    private void onDrawScrollIndicators(Canvas c) {
        int rightRtl;
        int leftRtl;
        if ((this.mPrivateFlags3 & SCROLL_INDICATORS_PFLAG3_MASK) != 0) {
            Drawable dr = this.mScrollIndicatorDrawable;
            if (dr != null) {
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
                if ((this.mPrivateFlags3 & (1024 | leftRtl)) != 0 && canScrollHorizontally(-1)) {
                    dr.setBounds(rect.left, rect.top, rect.left + w, rect.bottom);
                    dr.draw(c);
                }
                if ((this.mPrivateFlags3 & (2048 | rightRtl)) != 0 && canScrollHorizontally(1)) {
                    dr.setBounds(rect.right - w, rect.top, rect.right, rect.bottom);
                    dr.draw(c);
                }
            }
        }
    }

    private void getHorizontalScrollBarBounds(Rect drawBounds, Rect touchBounds) {
        Rect bounds = drawBounds != null ? drawBounds : touchBounds;
        if (bounds != null) {
            int verticalScrollBarGap = 0;
            int inside = (this.mViewFlags & 33554432) == 0 ? -1 : 0;
            boolean drawVerticalScrollBar = isVerticalScrollBarEnabled() && !isVerticalScrollBarHidden();
            int size = getHorizontalScrollbarHeight();
            if (drawVerticalScrollBar) {
                verticalScrollBarGap = getVerticalScrollbarWidth();
            }
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
        } else {
            getRoundVerticalScrollBarBounds(bounds != null ? bounds : touchBounds);
        }
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
                verticalScrollbarPosition = isLayoutRtl() ? 1 : 2;
            }
            int width = this.mRight - this.mLeft;
            int height = this.mBottom - this.mTop;
            if (verticalScrollbarPosition != 1) {
                bounds.left = ((this.mScrollX + width) - size) - (this.mUserPaddingRight & inside);
            } else {
                bounds.left = this.mScrollX + (this.mUserPaddingLeft & inside);
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

    /* access modifiers changed from: protected */
    public final void onDrawScrollBars(Canvas canvas) {
        ScrollBarDrawable scrollBar;
        ScrollabilityCache cache = this.mScrollCache;
        if (cache != null) {
            int state = cache.state;
            if (state != 0) {
                boolean invalidate = false;
                if (state == 2) {
                    if (cache.interpolatorValues == null) {
                        cache.interpolatorValues = new float[1];
                    }
                    float[] values = cache.interpolatorValues;
                    if (cache.scrollBarInterpolator.timeToValues(values) == Interpolator.Result.FREEZE_END) {
                        cache.state = 0;
                    } else {
                        cache.scrollBar.mutate().setAlpha(Math.round(values[0]));
                    }
                    invalidate = true;
                } else {
                    cache.scrollBar.mutate().setAlpha(255);
                }
                boolean invalidate2 = invalidate;
                boolean drawHorizontalScrollBar = isHorizontalScrollBarEnabled();
                boolean drawVerticalScrollBar = isVerticalScrollBarEnabled() && !isVerticalScrollBarHidden();
                if (this.mRoundScrollbarRenderer == null) {
                    Canvas canvas2 = canvas;
                    if (drawVerticalScrollBar || drawHorizontalScrollBar) {
                        ScrollBarDrawable scrollBar2 = cache.scrollBar;
                        if (drawHorizontalScrollBar) {
                            scrollBar2.setParameters(computeHorizontalScrollRange(), computeHorizontalScrollOffset(), computeHorizontalScrollExtent(), false);
                            Rect bounds = cache.mScrollBarBounds;
                            getHorizontalScrollBarBounds(bounds, null);
                            Rect bounds2 = bounds;
                            scrollBar = scrollBar2;
                            onDrawHorizontalScrollBar(canvas2, scrollBar2, bounds.left, bounds.top, bounds.right, bounds.bottom);
                            if (invalidate2) {
                                invalidate(bounds2);
                            }
                        } else {
                            scrollBar = scrollBar2;
                        }
                        if (drawVerticalScrollBar) {
                            scrollBar.setParameters(computeVerticalScrollRange(), computeVerticalScrollOffset(), computeVerticalScrollExtent(), true);
                            Rect bounds3 = cache.mScrollBarBounds;
                            getVerticalScrollBarBounds(bounds3, null);
                            onDrawVerticalScrollBar(canvas2, scrollBar, bounds3.left, bounds3.top, bounds3.right, bounds3.bottom);
                            if (invalidate2) {
                                invalidate(bounds3);
                            }
                        }
                    }
                } else if (drawVerticalScrollBar) {
                    Rect bounds4 = cache.mScrollBarBounds;
                    getVerticalScrollBarBounds(bounds4, null);
                    this.mRoundScrollbarRenderer.drawRoundScrollbars(canvas, ((float) cache.scrollBar.getAlpha()) / 255.0f, bounds4);
                    if (invalidate2) {
                        invalidate();
                    }
                }
            }
            return;
        }
        Canvas canvas3 = canvas;
    }

    /* access modifiers changed from: protected */
    public boolean isVerticalScrollBarHidden() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onDrawHorizontalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
    }

    /* access modifiers changed from: package-private */
    public void assignParent(ViewParent parent) {
        if (this.mParent == null) {
            this.mParent = parent;
        } else if (parent == null) {
            this.mParent = null;
        } else {
            throw new RuntimeException("view " + this + " being added, but it already has a parent");
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
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

    /* access modifiers changed from: package-private */
    public void dispatchScreenStateChanged(int screenState) {
        onScreenStateChanged(screenState);
    }

    public void onScreenStateChanged(int screenState) {
    }

    /* access modifiers changed from: package-private */
    public void dispatchMovedToDisplay(Display display, Configuration config) {
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
        return getContext().getApplicationInfo().targetSdkVersion < 17 || !hasRtlSupport();
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
                            } else {
                                return false;
                            }
                        } catch (AbstractMethodError e) {
                            Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                            break;
                        }
                    } else {
                        return false;
                    }
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
        if (getRawLayoutDirection() != 2) {
            return true;
        }
        if (this.mParent != null) {
            try {
                return this.mParent.canResolveLayoutDirection();
            } catch (AbstractMethodError e) {
                Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
            }
        }
        return false;
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

    /* access modifiers changed from: package-private */
    public boolean isPaddingResolved() {
        return (this.mPrivateFlags2 & 536870912) == 536870912;
    }

    public void resolvePadding() {
        int resolvedLayoutDirection = getLayoutDirection();
        if (!isRtlCompatibilityMode()) {
            if (this.mBackground != null && (!this.mLeftPaddingDefined || !this.mRightPaddingDefined)) {
                Rect padding = sThreadLocal.get();
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
            if (resolvedLayoutDirection != 1) {
                if (this.mUserPaddingStart != Integer.MIN_VALUE) {
                    this.mUserPaddingLeft = this.mUserPaddingStart;
                } else {
                    this.mUserPaddingLeft = this.mUserPaddingLeftInitial;
                }
                if (this.mUserPaddingEnd != Integer.MIN_VALUE) {
                    this.mUserPaddingRight = this.mUserPaddingEnd;
                } else {
                    this.mUserPaddingRight = this.mUserPaddingRightInitial;
                }
            } else {
                if (this.mUserPaddingStart != Integer.MIN_VALUE) {
                    this.mUserPaddingRight = this.mUserPaddingStart;
                } else {
                    this.mUserPaddingRight = this.mUserPaddingRightInitial;
                }
                if (this.mUserPaddingEnd != Integer.MIN_VALUE) {
                    this.mUserPaddingLeft = this.mUserPaddingEnd;
                } else {
                    this.mUserPaddingLeft = this.mUserPaddingLeftInitial;
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

    /* access modifiers changed from: package-private */
    public void resetResolvedPaddingInternal() {
        this.mPrivateFlags2 &= -536870913;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindowInternal() {
        this.mPrivateFlags &= -67108865;
        this.mPrivateFlags3 &= -5;
        this.mPrivateFlags3 &= -33554433;
        removeUnsetPressCallback();
        removeLongPressCallback();
        removePerformClickCallback();
        cancel(this.mSendViewScrolledAccessibilityEvent);
        stopNestedScroll();
        jumpDrawablesToCurrentState();
        destroyDrawingCache();
        cleanupDraw();
        this.mCurrentAnimation = null;
        if ((this.mViewFlags & 1073741824) == 1073741824) {
            hideTooltip();
        }
    }

    private void cleanupDraw() {
        resetDisplayList();
        if (this.mAttachInfo != null) {
            this.mAttachInfo.mViewRootImpl.cancelInvalidate(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void invalidateInheritedLayoutMode(int layoutModeOfRoot) {
    }

    /* access modifiers changed from: protected */
    public int getWindowAttachCount() {
        return this.mWindowAttachCount;
    }

    public IBinder getWindowToken() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mWindowToken;
        }
        return null;
    }

    public WindowId getWindowId() {
        AttachInfo ai = this.mAttachInfo;
        if (ai == null) {
            return null;
        }
        if (ai.mWindowId == null) {
            try {
                ai.mIWindowId = ai.mSession.getWindowId(ai.mWindowToken);
                if (ai.mIWindowId != null) {
                    ai.mWindowId = new WindowId(ai.mIWindowId);
                }
            } catch (RemoteException e) {
            }
        }
        return ai.mWindowId;
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
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mDisplay;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public IWindowSession getWindowSession() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mSession;
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public IWindow getWindow() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mWindow;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int combineVisibility(int vis1, int vis2) {
        return Math.max(vis1, vis2);
    }

    /* access modifiers changed from: package-private */
    public void dispatchAttachedToWindow(AttachInfo info, int visibility) {
        this.mAttachInfo = info;
        if (this.mOverlay != null) {
            this.mOverlay.getOverlayView().dispatchAttachedToWindow(info, visibility);
        }
        this.mWindowAttachCount++;
        this.mPrivateFlags |= 1024;
        CopyOnWriteArrayList<OnAttachStateChangeListener> listeners = null;
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
        if (li != null) {
            listeners = li.mOnAttachStateChangeListeners;
        }
        if (listeners != null && listeners.size() > 0) {
            Iterator<OnAttachStateChangeListener> it = listeners.iterator();
            while (it.hasNext()) {
                it.next().onViewAttachedToWindow(this);
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

    /* access modifiers changed from: package-private */
    public void dispatchDetachedFromWindow() {
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
        CopyOnWriteArrayList<OnAttachStateChangeListener> listeners = li != null ? li.mOnAttachStateChangeListeners : null;
        if (listeners != null && listeners.size() > 0) {
            Iterator<OnAttachStateChangeListener> it = listeners.iterator();
            while (it.hasNext()) {
                it.next().onViewDetachedFromWindow(this);
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

    /* access modifiers changed from: package-private */
    public void dispatchCancelPendingInputEvents() {
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

    /* access modifiers changed from: protected */
    public void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
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

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        this.mPrivateFlags |= 131072;
        if (this.mStartActivityRequestWho == null && !isAutofilled() && this.mAutofillViewId <= 1073741823) {
            return BaseSavedState.EMPTY_STATE;
        }
        BaseSavedState state = new BaseSavedState((Parcelable) AbsSavedState.EMPTY_STATE);
        if (this.mStartActivityRequestWho != null) {
            state.mSavedData |= 1;
        }
        if (isAutofilled()) {
            state.mSavedData |= 2;
        }
        if (this.mAutofillViewId > 1073741823) {
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

    /* access modifiers changed from: protected */
    public void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        if (this.mID != -1) {
            Parcelable state = container.get(this.mID);
            if (state != null) {
                this.mPrivateFlags &= -131073;
                onRestoreInstanceState(state);
                if ((this.mPrivateFlags & 131072) == 0) {
                    throw new IllegalStateException("Derived class did not call super.onRestoreInstanceState()");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        this.mPrivateFlags |= 131072;
        if (state != null && !(state instanceof AbsSavedState)) {
            throw new IllegalArgumentException("Wrong state class, expecting View State but received " + state.getClass().toString() + " instead. This usually happens when two views of different type have the same id in the same hierarchy. This view's id is " + ViewDebug.resolveId(this.mContext, getId()) + ". Make sure other views do not use the same id.");
        } else if (state != null && (state instanceof BaseSavedState)) {
            BaseSavedState baseState = (BaseSavedState) state;
            if ((baseState.mSavedData & 1) != 0) {
                this.mStartActivityRequestWho = baseState.mStartActivityRequestWhoSaved;
            }
            if ((baseState.mSavedData & 2) != 0) {
                setAutofilled(baseState.mIsAutofilled);
            }
            if ((baseState.mSavedData & 4) != 0) {
                ((BaseSavedState) state).mSavedData &= -5;
                if ((this.mPrivateFlags3 & 1073741824) == 0) {
                    this.mAutofillViewId = baseState.mAutofillViewId;
                    this.mAutofillId = null;
                } else if (Helper.sDebug) {
                    Log.d(VIEW_LOG_TAG, "onRestoreInstanceState(): not setting autofillId to " + baseState.mAutofillViewId + " because view explicitly set it to " + this.mAutofillId);
                }
            }
        }
    }

    public long getDrawingTime() {
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mDrawingTime;
        }
        return 0;
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
        } else if (!this.mRenderNode.setLayerType(layerType)) {
            setLayerPaint(paint);
        } else {
            if (layerType != 1) {
                destroyDrawingCache();
            }
            this.mLayerType = layerType;
            this.mLayerPaint = this.mLayerType == 0 ? null : paint;
            this.mRenderNode.setLayerPaint(this.mLayerPaint);
            invalidateParentCaches();
            invalidate(true);
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

    /* access modifiers changed from: protected */
    public void destroyHardwareResources() {
        if (this.mOverlay != null) {
            this.mOverlay.getOverlayView().destroyHardwareResources();
        }
        if (this.mGhostView != null) {
            this.mGhostView.destroyHardwareResources();
        }
    }

    @Deprecated
    public void setDrawingCacheEnabled(boolean enabled) {
        int i = 0;
        this.mCachingFailed = false;
        if (enabled) {
            i = 32768;
        }
        setFlags(i, 32768);
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    @Deprecated
    public boolean isDrawingCacheEnabled() {
        return (this.mViewFlags & 32768) == 32768;
    }

    public void outputDirtyFlags(String indent, boolean clear, int clearMask) {
        Log.d(VIEW_LOG_TAG, indent + this + "             DIRTY(" + (this.mPrivateFlags & PFLAG_DIRTY_MASK) + ") DRAWN(" + (this.mPrivateFlags & 32) + ") CACHE_VALID(" + (this.mPrivateFlags & 32768) + ") INVALIDATED(" + (this.mPrivateFlags & Integer.MIN_VALUE) + ")");
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

    /* access modifiers changed from: protected */
    public void dispatchGetDisplayList() {
    }

    public boolean canHaveDisplayList() {
        return (this.mAttachInfo == null || this.mAttachInfo.mThreadedRenderer == null) ? false : true;
    }

    public RenderNode updateDisplayListIfDirty() {
        RenderNode renderNode = this.mRenderNode;
        if (!canHaveDisplayList()) {
            return renderNode;
        }
        if ((this.mPrivateFlags & 32768) != 0 && renderNode.isValid() && !this.mRecreateDisplayList) {
            this.mPrivateFlags |= 32800;
            this.mPrivateFlags &= -6291457;
        } else if (!renderNode.isValid() || this.mRecreateDisplayList) {
            this.mRecreateDisplayList = true;
            int width = this.mRight - this.mLeft;
            int height = this.mBottom - this.mTop;
            int layerType = getLayerType();
            DisplayListCanvas canvas = renderNode.start(width, height);
            if (layerType == 1) {
                try {
                    buildDrawingCache(true);
                    Bitmap cache = getDrawingCache(true);
                    if (cache != null) {
                        canvas.drawBitmap(cache, 0.0f, 0.0f, this.mLayerPaint);
                    }
                } finally {
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
                    if (this.mOverlay != null && !this.mOverlay.isEmpty()) {
                        this.mOverlay.getOverlayView().draw(canvas);
                    }
                    if (debugDraw()) {
                        debugDrawFocus(canvas);
                    }
                } else {
                    draw(canvas);
                }
            }
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

    @Deprecated
    public Bitmap getDrawingCache() {
        return getDrawingCache(false);
    }

    @Deprecated
    public Bitmap getDrawingCache(boolean autoScale) {
        if ((this.mViewFlags & 131072) == 131072) {
            return null;
        }
        if ((this.mViewFlags & 32768) == 32768) {
            buildDrawingCache(autoScale);
        }
        return autoScale ? this.mDrawingCache : this.mUnscaledDrawingCache;
    }

    @Deprecated
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

    @Deprecated
    public void setDrawingCacheBackgroundColor(int color) {
        if (color != this.mDrawingCacheBackgroundColor) {
            this.mDrawingCacheBackgroundColor = color;
            this.mPrivateFlags &= -32769;
        }
    }

    @Deprecated
    public int getDrawingCacheBackgroundColor() {
        return this.mDrawingCacheBackgroundColor;
    }

    @Deprecated
    public void buildDrawingCache() {
        buildDrawingCache(false);
    }

    @Deprecated
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

    /* JADX WARNING: Removed duplicated region for block: B:111:0x017e  */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0182  */
    private void buildDrawingCacheImpl(boolean autoScale) {
        boolean clear;
        Canvas canvas;
        Bitmap.Config quality;
        boolean z;
        this.mCachingFailed = false;
        int width = this.mRight - this.mLeft;
        int height = this.mBottom - this.mTop;
        AttachInfo attachInfo = this.mAttachInfo;
        boolean scalingRequired = attachInfo != null && attachInfo.mScalingRequired;
        if (autoScale && scalingRequired) {
            width = (int) ((((float) width) * attachInfo.mApplicationScale) + 0.5f);
            height = (int) ((((float) height) * attachInfo.mApplicationScale) + 0.5f);
        }
        int drawingCacheBackgroundColor = this.mDrawingCacheBackgroundColor;
        boolean opaque = drawingCacheBackgroundColor != 0 || isOpaque();
        boolean use32BitCache = attachInfo != null && attachInfo.mUse32BitDrawingCache;
        long projectedBitmapSize = (long) (width * height * ((!opaque || use32BitCache) ? 4 : 2));
        long drawingCacheSize = (long) ViewConfiguration.get(this.mContext).getScaledMaximumDrawingCacheSize();
        if (width <= 0 || height <= 0) {
            boolean z2 = opaque;
        } else if (projectedBitmapSize > drawingCacheSize) {
            boolean z3 = scalingRequired;
            boolean z4 = opaque;
        } else {
            Bitmap bitmap = autoScale ? this.mDrawingCache : this.mUnscaledDrawingCache;
            if (bitmap != null && bitmap.getWidth() == width && bitmap.getHeight() == height) {
                clear = true;
            } else {
                if (!opaque) {
                    int i = this.mViewFlags;
                    quality = Bitmap.Config.ARGB_8888;
                } else {
                    quality = use32BitCache ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                }
                if (bitmap != null) {
                    bitmap.recycle();
                }
                try {
                    bitmap = Bitmap.createBitmap(this.mResources.getDisplayMetrics(), width, height, quality);
                    if (bitmap != null) {
                        bitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
                        if (autoScale) {
                            try {
                                this.mDrawingCache = bitmap;
                            } catch (OutOfMemoryError e) {
                                boolean z5 = scalingRequired;
                                boolean z6 = opaque;
                            }
                        } else {
                            this.mUnscaledDrawingCache = bitmap;
                        }
                        if (!opaque || !use32BitCache) {
                            z = false;
                        } else {
                            z = false;
                            bitmap.setHasAlpha(false);
                        }
                        clear = drawingCacheBackgroundColor != 0 ? true : z;
                    } else {
                        boolean z7 = opaque;
                        try {
                            throw new OutOfMemoryError();
                        } catch (OutOfMemoryError e2) {
                            if (!autoScale) {
                            }
                            this.mCachingFailed = true;
                            return;
                        }
                    }
                } catch (OutOfMemoryError e3) {
                    boolean z8 = scalingRequired;
                    boolean z9 = opaque;
                    if (!autoScale) {
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
                    Canvas canvas2 = canvas;
                    canvas = new Canvas();
                } else {
                    Canvas canvas3 = canvas;
                }
                canvas.setBitmap(bitmap);
                boolean z10 = opaque;
                attachInfo.mCanvas = null;
            } else {
                canvas = new Canvas(bitmap);
            }
            if (clear) {
                bitmap.eraseColor(drawingCacheBackgroundColor);
            }
            computeScroll();
            int restoreCount = canvas.save();
            if (!autoScale || !scalingRequired) {
            } else {
                Bitmap bitmap2 = bitmap;
                float scale = attachInfo.mApplicationScale;
                canvas.scale(scale, scale);
            }
            boolean z11 = scalingRequired;
            canvas.translate((float) (-this.mScrollX), (float) (-this.mScrollY));
            this.mPrivateFlags |= 32;
            if (this.mAttachInfo == null || !this.mAttachInfo.mHardwareAccelerated || this.mLayerType != 0) {
                this.mPrivateFlags |= 32768;
            }
            if ((this.mPrivateFlags & 128) == 128) {
                this.mPrivateFlags &= -6291457;
                dispatchDraw(canvas);
                drawAutofilledHighlight(canvas);
                if (this.mOverlay != null && !this.mOverlay.isEmpty()) {
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
            return;
        }
        if (width > 0 && height > 0) {
            Log.w(VIEW_LOG_TAG, getClass().getSimpleName() + " not displayed because it is too large to fit into a software layer (or drawing cache), needs " + projectedBitmapSize + " bytes, only " + drawingCacheSize + " available");
        }
        destroyDrawingCache();
        this.mCachingFailed = true;
    }

    public Bitmap createSnapshot(ViewDebug.CanvasProvider canvasProvider, boolean skipChildren) {
        int width = this.mRight - this.mLeft;
        int height = this.mBottom - this.mTop;
        AttachInfo attachInfo = this.mAttachInfo;
        float scale = attachInfo != null ? attachInfo.mApplicationScale : 1.0f;
        int width2 = (int) ((((float) width) * scale) + 0.5f);
        int height2 = (int) ((((float) height) * scale) + 0.5f);
        Canvas oldCanvas = null;
        int i = 1;
        int i2 = width2 > 0 ? width2 : 1;
        if (height2 > 0) {
            i = height2;
        }
        try {
            Canvas canvas = canvasProvider.getCanvas(this, i2, i);
            if (attachInfo != null) {
                oldCanvas = attachInfo.mCanvas;
                attachInfo.mCanvas = null;
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
                if (this.mOverlay != null && !this.mOverlay.isEmpty()) {
                    this.mOverlay.getOverlayView().draw(canvas);
                }
            } else {
                draw(canvas);
            }
            this.mPrivateFlags = flags;
            canvas.restoreToCount(restoreCount);
            return canvasProvider.createBitmap();
        } finally {
            if (oldCanvas != null) {
                attachInfo.mCanvas = oldCanvas;
            }
        }
    }

    public boolean isInEditMode() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isPaddingOffsetRequired() {
        return false;
    }

    /* access modifiers changed from: protected */
    public int getLeftPaddingOffset() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getRightPaddingOffset() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getTopPaddingOffset() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getBottomPaddingOffset() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getFadeTop(boolean offsetRequired) {
        int top = this.mPaddingTop;
        if (offsetRequired) {
            return top + getTopPaddingOffset();
        }
        return top;
    }

    /* access modifiers changed from: protected */
    public int getFadeHeight(boolean offsetRequired) {
        int padding = this.mPaddingTop;
        if (offsetRequired) {
            padding += getTopPaddingOffset();
        }
        return ((this.mBottom - this.mTop) - this.mPaddingBottom) - padding;
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean isHardwareAccelerated() {
        return this.mAttachInfo != null && this.mAttachInfo.mHardwareAccelerated;
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
        if (this.mClipBounds != null) {
            return new Rect(this.mClipBounds);
        }
        return null;
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
        ViewGroup viewGroup = parent;
        long j = drawingTime;
        Animation animation = a;
        int flags = viewGroup.mGroupFlags;
        if (!a.isInitialized()) {
            animation.initialize(this.mRight - this.mLeft, this.mBottom - this.mTop, parent.getWidth(), parent.getHeight());
            animation.initializeInvalidateRegion(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop);
            if (this.mAttachInfo != null) {
                animation.setListenerHandler(this.mAttachInfo.mHandler);
            }
            onAnimationStart();
        }
        Transformation t = parent.getChildTransformation();
        boolean more = animation.getTransformation(j, t, 1.0f);
        if (!scalingRequired || this.mAttachInfo.mApplicationScale == 1.0f) {
            invalidationTransform = t;
        } else {
            if (viewGroup.mInvalidationTransformation == null) {
                viewGroup.mInvalidationTransformation = new Transformation();
            }
            Transformation invalidationTransform2 = viewGroup.mInvalidationTransformation;
            animation.getTransformation(j, invalidationTransform2, 1.0f);
            invalidationTransform = invalidationTransform2;
        }
        if (more) {
            if (a.willChangeBounds()) {
                if (viewGroup.mInvalidateRegion == null) {
                    viewGroup.mInvalidateRegion = new RectF();
                }
                RectF region = viewGroup.mInvalidateRegion;
                animation.getInvalidateRegion(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop, region, invalidationTransform);
                viewGroup.mPrivateFlags |= 64;
                RectF region2 = region;
                int left = this.mLeft + ((int) region2.left);
                int top = this.mTop + ((int) region2.top);
                viewGroup.invalidate(left, top, ((int) (region2.width() + 0.5f)) + left, ((int) (region2.height() + 0.5f)) + top);
            } else if ((flags & 144) == 128) {
                viewGroup.mGroupFlags |= 4;
            } else if ((flags & 4) == 0) {
                viewGroup.mPrivateFlags |= 64;
                viewGroup.invalidate(this.mLeft, this.mTop, this.mRight, this.mBottom);
            }
        }
        return more;
    }

    private void setPartialDirtyToNative(RenderNode renderNode) {
        if (this.mIsSupportPartialUpdate) {
            renderNode.setDirtyLeftTopRightBottom(this.mCurrentInvalRect.left, this.mCurrentInvalRect.top, this.mCurrentInvalRect.right, this.mCurrentInvalRect.bottom);
            if (this.mIsPartialUpdateDebugOn && !this.mHwApsImpl.isInPowerTest()) {
                mTotalRefreshTimes++;
                mTotalFullViewAreaUpdate += (long) (getWidth() * getHeight());
                float thisRefreshRatio = 1.0f;
                int width = this.mCurrentInvalRect.width();
                int height = this.mCurrentInvalRect.height();
                if (width > 0 && height > 0 && getWidth() > 0 && getHeight() > 0) {
                    thisRefreshRatio = (((float) (width * height)) * 1.0f) / ((float) (getWidth() * getHeight()));
                    if (thisRefreshRatio < 1.0f) {
                        mPartialUpdateTimes++;
                        mTotalAreaDecreasedDraw += (long) ((getWidth() * getHeight()) - (width * height));
                        mTotalAreaDecreasedRefreshHeight += (long) (getHeight() - height);
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("APS: PU, setDirty, pkgname=");
                sb.append(getContext().getPackageName());
                sb.append(", partial update refresh times = ");
                sb.append(mPartialUpdateTimes);
                sb.append(", partial update refresh ratio = ");
                sb.append((((float) mPartialUpdateTimes) * 1.0f) / ((float) mTotalRefreshTimes));
                sb.append(", currentDirty = ");
                sb.append(this.mCurrentInvalRect.isEmpty() ? new Rect(0, 0, getWidth(), getHeight()) : this.mCurrentInvalRect);
                sb.append(", (w,h)=(");
                sb.append(getWidth());
                sb.append(", ");
                sb.append(getHeight());
                sb.append("), this refresh area ratio = ");
                sb.append(thisRefreshRatio);
                sb.append(", totalAreaDecreasedDraw = ");
                sb.append(mTotalAreaDecreasedDraw);
                sb.append(", totalAreaDecreasedRatio = ");
                sb.append((((float) mTotalAreaDecreasedDraw) * 1.0f) / ((float) mTotalFullViewAreaUpdate));
                sb.append(", totalAreaDecreasedRefreshHeight = ");
                sb.append(mTotalAreaDecreasedRefreshHeight);
                sb.append(", classname = ");
                sb.append(getClass().getSimpleName());
                Log.i("partial", sb.toString());
            }
            if (!this.mCurrentInvalRect.isEmpty()) {
                this.mCurrentInvalRect.setEmpty();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDisplayListProperties(RenderNode renderNode) {
        boolean z;
        if (renderNode != null) {
            renderNode.setHasOverlappingRendering(getHasOverlappingRendering());
            if (!(this.mParent instanceof ViewGroup) || !((ViewGroup) this.mParent).getClipChildren()) {
                z = false;
            } else {
                z = true;
            }
            renderNode.setClipToBounds(z);
            float alpha = 1.0f;
            if ((this.mParent instanceof ViewGroup) && (((ViewGroup) this.mParent).mGroupFlags & 2048) != 0) {
                ViewGroup parentVG = (ViewGroup) this.mParent;
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
                float alpha2 = alpha * getFinalAlpha();
                if (alpha2 < 1.0f && onSetAlpha((int) (255.0f * alpha2))) {
                    alpha2 = 1.0f;
                }
                renderNode.setAlpha(alpha2);
            } else if (alpha < 1.0f) {
                renderNode.setAlpha(alpha);
            }
            setPartialDirtyToNative(renderNode);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0144, code lost:
        if (r1 != null) goto L_0x0149;
     */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x0186  */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0189  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x01e5  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x01e9  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x022b  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x0265  */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x026c  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x027a  */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x02f5  */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x0306  */
    /* JADX WARNING: Removed duplicated region for block: B:173:0x034a  */
    /* JADX WARNING: Removed duplicated region for block: B:179:0x036c  */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x03b8  */
    /* JADX WARNING: Removed duplicated region for block: B:196:0x03be  */
    /* JADX WARNING: Removed duplicated region for block: B:198:0x03c2 A[ADDED_TO_REGION] */
    public boolean draw(Canvas canvas, ViewGroup parent, long drawingTime) {
        Animation a;
        Transformation transformToApply;
        Transformation transformToApply2;
        int restoreTo;
        float alpha;
        int restoreTo2;
        Animation a2;
        boolean more;
        Bitmap cache;
        int sx;
        int restoreTo3;
        RenderNode renderNode;
        Animation a3;
        Bitmap cache2;
        float alpha2;
        float f;
        int transX;
        int transY;
        int restoreTo4;
        int restoreTo5;
        Canvas canvas2 = canvas;
        ViewGroup viewGroup = parent;
        boolean hardwareAcceleratedCanvas = canvas.isHardwareAccelerated();
        boolean drawingWithRenderNode = this.mAttachInfo != null && this.mAttachInfo.mHardwareAccelerated && hardwareAcceleratedCanvas;
        boolean more2 = false;
        boolean childHasIdentityMatrix = hasIdentityMatrix();
        int parentFlags = viewGroup.mGroupFlags;
        if ((parentFlags & 256) != 0) {
            parent.getChildTransformation().clear();
            viewGroup.mGroupFlags &= -257;
        }
        Transformation transformToApply3 = null;
        boolean concatMatrix = false;
        boolean scalingRequired = this.mAttachInfo != null && this.mAttachInfo.mScalingRequired;
        Animation a4 = getAnimation();
        if (a4 != null) {
            a = a4;
            more2 = applyLegacyAnimation(viewGroup, drawingTime, a4, scalingRequired);
            concatMatrix = a.willChangeTransformationMatrix();
            if (concatMatrix) {
                this.mPrivateFlags3 |= 1;
            }
            transformToApply3 = parent.getChildTransformation();
        } else {
            a = a4;
            Transformation transformation = null;
            if ((this.mPrivateFlags3 & 1) != 0) {
                this.mRenderNode.setAnimationMatrix(null);
                this.mPrivateFlags3 &= -2;
            }
            if (!drawingWithRenderNode && (parentFlags & 2048) != 0) {
                Transformation t = parent.getChildTransformation();
                if (viewGroup.getChildStaticTransformation(this, t)) {
                    int transformType = t.getTransformationType();
                    if (transformType != 0) {
                        transformation = t;
                    }
                    transformToApply3 = transformation;
                    concatMatrix = (transformType & 2) != 0;
                }
            }
        }
        Transformation transformToApply4 = transformToApply3;
        boolean concatMatrix2 = concatMatrix | (!childHasIdentityMatrix);
        this.mPrivateFlags |= 32;
        if (concatMatrix2 || (parentFlags & GLES10.GL_EXP2) != 1) {
            transformToApply = transformToApply4;
        } else {
            transformToApply = transformToApply4;
            if (canvas2.quickReject((float) this.mLeft, (float) this.mTop, (float) this.mRight, (float) this.mBottom, Canvas.EdgeType.BW) && (this.mPrivateFlags & 64) == 0) {
                this.mPrivateFlags2 |= 268435456;
                return more2;
            }
        }
        this.mPrivateFlags2 &= -268435457;
        if (hardwareAcceleratedCanvas) {
            this.mRecreateDisplayList = (this.mPrivateFlags & Integer.MIN_VALUE) != 0;
            this.mPrivateFlags &= Integer.MAX_VALUE;
        }
        RenderNode renderNode2 = null;
        Bitmap cache3 = null;
        int layerType = getLayerType();
        if (layerType == 1 || !drawingWithRenderNode) {
            if (layerType != 0) {
                layerType = 1;
                buildDrawingCache(true);
            }
            cache3 = getDrawingCache(true);
        }
        Bitmap cache4 = cache3;
        int layerType2 = layerType;
        if (drawingWithRenderNode) {
            renderNode2 = updateDisplayListIfDirty();
            if (!renderNode2.isValid()) {
                renderNode2 = null;
                drawingWithRenderNode = false;
            }
        }
        RenderNode renderNode3 = renderNode2;
        int sx2 = 0;
        int sy = 0;
        if (!drawingWithRenderNode) {
            computeScroll();
            sx2 = this.mScrollX;
            sy = this.mScrollY;
        }
        int sx3 = sx2;
        int sy2 = sy;
        boolean drawingWithDrawingCache = cache4 != null && !drawingWithRenderNode;
        boolean offsetForScroll = cache4 == null && !drawingWithRenderNode;
        int restoreTo6 = -1;
        if (drawingWithRenderNode) {
            transformToApply2 = transformToApply;
        } else {
            transformToApply2 = transformToApply;
        }
        restoreTo6 = canvas.save();
        if (offsetForScroll) {
            restoreTo4 = restoreTo6;
            canvas2.translate((float) (this.mLeft - sx3), (float) (this.mTop - sy2));
        } else {
            restoreTo4 = restoreTo6;
            if (!drawingWithRenderNode) {
                canvas2.translate((float) this.mLeft, (float) this.mTop);
            }
            if (scalingRequired) {
                if (drawingWithRenderNode) {
                    restoreTo5 = canvas.save();
                } else {
                    restoreTo5 = restoreTo4;
                }
                float scale = 1.0f / this.mAttachInfo.mApplicationScale;
                canvas2.scale(scale, scale);
                restoreTo = restoreTo5;
                alpha = !drawingWithRenderNode ? 1.0f : getAlpha() * getTransitionAlpha();
                boolean hardwareAcceleratedCanvas2 = hardwareAcceleratedCanvas;
                if (transformToApply2 == null || alpha < 1.0f || !hasIdentityMatrix()) {
                    cache2 = cache4;
                } else if ((this.mPrivateFlags3 & 2) != 0) {
                    cache2 = cache4;
                } else {
                    Bitmap cache5 = cache4;
                    if ((this.mPrivateFlags & 262144) == 262144) {
                        onSetAlpha(255);
                        this.mPrivateFlags &= -262145;
                    }
                    Transformation transformation2 = transformToApply2;
                    renderNode = renderNode3;
                    a2 = a;
                    restoreTo2 = restoreTo;
                    more = more2;
                    boolean z = childHasIdentityMatrix;
                    cache = cache5;
                    restoreTo3 = sy2;
                    sx = sx3;
                    if (!drawingWithRenderNode) {
                        if ((parentFlags & 1) != 0 && cache == null) {
                            if (offsetForScroll) {
                                canvas2.clipRect(sx, restoreTo3, sx + getWidth(), restoreTo3 + getHeight());
                            } else if (!scalingRequired || cache == null) {
                                canvas2.clipRect(0, 0, getWidth(), getHeight());
                            } else {
                                canvas2.clipRect(0, 0, cache.getWidth(), cache.getHeight());
                            }
                        }
                        if (this.mClipBounds != null) {
                            canvas2.clipRect(this.mClipBounds);
                        }
                    }
                    if (!drawingWithDrawingCache) {
                        if (drawingWithRenderNode) {
                            this.mPrivateFlags = -6291457 & this.mPrivateFlags;
                            ((DisplayListCanvas) canvas2).drawRenderNode(renderNode);
                        } else if ((this.mPrivateFlags & 128) == 128) {
                            this.mPrivateFlags = -6291457 & this.mPrivateFlags;
                            dispatchDraw(canvas);
                        } else {
                            draw(canvas);
                        }
                    } else if (cache != null) {
                        this.mPrivateFlags = -6291457 & this.mPrivateFlags;
                        if (layerType2 == 0 || this.mLayerPaint == null) {
                            Paint cachePaint = viewGroup.mCachePaint;
                            if (cachePaint == null) {
                                cachePaint = new Paint();
                                cachePaint.setDither(false);
                                viewGroup.mCachePaint = cachePaint;
                            }
                            cachePaint.setAlpha((int) (alpha * 255.0f));
                            canvas2.drawBitmap(cache, 0.0f, 0.0f, cachePaint);
                        } else {
                            int layerPaintAlpha = this.mLayerPaint.getAlpha();
                            if (alpha < 1.0f) {
                                this.mLayerPaint.setAlpha((int) (((float) layerPaintAlpha) * alpha));
                            }
                            canvas2.drawBitmap(cache, 0.0f, 0.0f, this.mLayerPaint);
                            if (alpha < 1.0f) {
                                this.mLayerPaint.setAlpha(layerPaintAlpha);
                            }
                        }
                    }
                    if (restoreTo2 >= 0) {
                        canvas2.restoreToCount(restoreTo2);
                    } else {
                        int i = restoreTo2;
                    }
                    if (a2 != null || more) {
                        a3 = a2;
                    } else {
                        if (!hardwareAcceleratedCanvas2) {
                            a3 = a2;
                            if (!a3.getFillAfter()) {
                                onSetAlpha(255);
                            }
                        } else {
                            a3 = a2;
                        }
                        viewGroup.finishAnimatingView(this, a3);
                    }
                    if (more && hardwareAcceleratedCanvas2 && a3.hasAlpha() && (this.mPrivateFlags & 262144) == 262144) {
                        invalidate(true);
                    }
                    this.mRecreateDisplayList = false;
                    return more;
                }
                if (transformToApply2 == null || !childHasIdentityMatrix) {
                    transX = 0;
                    transY = 0;
                    if (offsetForScroll) {
                        transX = -sx3;
                        transY = -sy2;
                    }
                    if (transformToApply2 == null) {
                        if (concatMatrix2) {
                            if (drawingWithRenderNode) {
                                more = more2;
                                renderNode3.setAnimationMatrix(transformToApply2.getMatrix());
                                a2 = a;
                            } else {
                                more = more2;
                                a2 = a;
                                canvas2.translate((float) (-transX), (float) (-transY));
                                canvas2.concat(transformToApply2.getMatrix());
                                canvas2.translate((float) transX, (float) transY);
                            }
                            viewGroup.mGroupFlags |= 256;
                        } else {
                            a2 = a;
                            more = more2;
                        }
                        float transformAlpha = transformToApply2.getAlpha();
                        if (transformAlpha < 1.0f) {
                            alpha *= transformAlpha;
                            viewGroup.mGroupFlags |= 256;
                        }
                    } else {
                        a2 = a;
                        more = more2;
                    }
                    if (!childHasIdentityMatrix && !drawingWithRenderNode) {
                        canvas2.translate((float) (-transX), (float) (-transY));
                        canvas2.concat(getMatrix());
                        canvas2.translate((float) transX, (float) transY);
                    }
                    alpha2 = alpha;
                } else {
                    alpha2 = alpha;
                    a2 = a;
                    more = more2;
                }
                if (alpha2 >= 1.0f || (this.mPrivateFlags3 & 2) != 0) {
                    if (alpha2 >= 1.0f) {
                        this.mPrivateFlags3 |= 2;
                    } else {
                        this.mPrivateFlags3 &= -3;
                    }
                    viewGroup.mGroupFlags |= 256;
                    if (drawingWithDrawingCache) {
                        int multipliedAlpha = (int) (255.0f * alpha2);
                        if (onSetAlpha(multipliedAlpha)) {
                            sx = sx3;
                            f = alpha2;
                            restoreTo2 = restoreTo;
                            boolean z2 = childHasIdentityMatrix;
                            cache = cache2;
                            restoreTo3 = sy2;
                            renderNode = renderNode3;
                            this.mPrivateFlags |= 262144;
                        } else if (drawingWithRenderNode) {
                            renderNode3.setAlpha(getAlpha() * alpha2 * getTransitionAlpha());
                            Transformation transformation3 = transformToApply2;
                            sx = sx3;
                            f = alpha2;
                            restoreTo2 = restoreTo;
                            boolean z3 = childHasIdentityMatrix;
                            cache = cache2;
                            restoreTo3 = sy2;
                            renderNode = renderNode3;
                        } else if (layerType2 == 0) {
                            Transformation transformation4 = transformToApply2;
                            sx = sx3;
                            f = alpha2;
                            renderNode = renderNode3;
                            restoreTo2 = restoreTo;
                            boolean z4 = childHasIdentityMatrix;
                            cache = cache2;
                            restoreTo3 = sy2;
                            canvas2.saveLayerAlpha((float) sx3, (float) sy2, (float) (getWidth() + sx3), (float) (sy2 + getHeight()), multipliedAlpha);
                        } else {
                            sx = sx3;
                            f = alpha2;
                            restoreTo2 = restoreTo;
                            boolean z5 = childHasIdentityMatrix;
                            cache = cache2;
                            restoreTo3 = sy2;
                            renderNode = renderNode3;
                        }
                    } else {
                        sx = sx3;
                        f = alpha2;
                        restoreTo2 = restoreTo;
                        boolean z6 = childHasIdentityMatrix;
                        cache = cache2;
                        restoreTo3 = sy2;
                        renderNode = renderNode3;
                    }
                } else {
                    Transformation transformation5 = transformToApply2;
                    sx = sx3;
                    f = alpha2;
                    restoreTo2 = restoreTo;
                    boolean z7 = childHasIdentityMatrix;
                    cache = cache2;
                    restoreTo3 = sy2;
                    renderNode = renderNode3;
                }
                alpha = f;
                if (!drawingWithRenderNode) {
                }
                if (!drawingWithDrawingCache) {
                }
                if (restoreTo2 >= 0) {
                }
                if (a2 != null) {
                }
                a3 = a2;
                invalidate(true);
                this.mRecreateDisplayList = false;
                return more;
            }
        }
        restoreTo = restoreTo4;
        if (!drawingWithRenderNode) {
        }
        boolean hardwareAcceleratedCanvas22 = hardwareAcceleratedCanvas;
        if (transformToApply2 == null) {
        }
        cache2 = cache4;
        if (transformToApply2 == null) {
        }
        transX = 0;
        transY = 0;
        if (offsetForScroll) {
        }
        if (transformToApply2 == null) {
        }
        canvas2.translate((float) (-transX), (float) (-transY));
        canvas2.concat(getMatrix());
        canvas2.translate((float) transX, (float) transY);
        alpha2 = alpha;
        if (alpha2 >= 1.0f) {
        }
        if (alpha2 >= 1.0f) {
        }
        viewGroup.mGroupFlags |= 256;
        if (drawingWithDrawingCache) {
        }
        alpha = f;
        if (!drawingWithRenderNode) {
        }
        if (!drawingWithDrawingCache) {
        }
        if (restoreTo2 >= 0) {
        }
        if (a2 != null) {
        }
        a3 = a2;
        invalidate(true);
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

    /* access modifiers changed from: package-private */
    public final int dipsToPixels(int dips) {
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
            paint.setStyle(Paint.Style.FILL);
            Canvas canvas2 = canvas;
            Paint paint2 = paint;
            canvas2.drawRect((float) l, (float) t, (float) (l + cornerSquareSize), (float) (t + cornerSquareSize), paint2);
            Canvas canvas3 = canvas;
            canvas3.drawRect((float) (r - cornerSquareSize), (float) t, (float) r, (float) (t + cornerSquareSize), paint2);
            canvas.drawRect((float) l, (float) (b - cornerSquareSize), (float) (l + cornerSquareSize), (float) b, paint2);
            canvas.drawRect((float) (r - cornerSquareSize), (float) (b - cornerSquareSize), (float) r, (float) b, paint2);
            paint.setStyle(Paint.Style.STROKE);
            Canvas canvas4 = canvas;
            canvas4.drawLine((float) l, (float) t, (float) r, (float) b, paint2);
            canvas4.drawLine((float) l, (float) b, (float) r, (float) t, paint2);
        }
    }

    public void draw(Canvas canvas) {
        float topFadeStrength;
        boolean drawRight;
        boolean drawBottom;
        boolean drawLeft;
        Paint p;
        float leftFadeStrength;
        int top;
        int saveCount;
        int bottom;
        int top2;
        float fadeHeight;
        float f;
        Paint p2;
        float f2;
        Canvas canvas2;
        int top3;
        Paint p3;
        Canvas canvas3 = canvas;
        int privateFlags = this.mPrivateFlags;
        boolean dirtyOpaque = (PFLAG_DIRTY_MASK & privateFlags) == 4194304 && (this.mAttachInfo == null || !this.mAttachInfo.mIgnoreDirtyState);
        this.mPrivateFlags = (-6291457 & privateFlags) | 32;
        if (!dirtyOpaque) {
            drawBackground(canvas);
        }
        int viewFlags = this.mViewFlags;
        boolean horizontalEdges = (viewFlags & 4096) != 0;
        boolean verticalEdges = (viewFlags & 8192) != 0;
        if (verticalEdges || horizontalEdges) {
            float bottomFadeStrength = 0.0f;
            float leftFadeStrength2 = 0.0f;
            float rightFadeStrength = 0.0f;
            int paddingLeft = this.mPaddingLeft;
            int i = privateFlags;
            boolean offsetRequired = isPaddingOffsetRequired();
            if (offsetRequired) {
                paddingLeft += getLeftPaddingOffset();
            }
            int paddingLeft2 = paddingLeft;
            int left = this.mScrollX + paddingLeft2;
            boolean drawTop = false;
            int right = (((this.mRight + left) - this.mLeft) - this.mPaddingRight) - paddingLeft2;
            int top4 = this.mScrollY + getFadeTop(offsetRequired);
            int bottom2 = top4 + getFadeHeight(offsetRequired);
            if (offsetRequired) {
                right += getRightPaddingOffset();
                bottom2 += getBottomPaddingOffset();
            }
            int right2 = right;
            int bottom3 = bottom2;
            int i2 = viewFlags;
            ScrollabilityCache scrollabilityCache = this.mScrollCache;
            float fadeHeight2 = (float) scrollabilityCache.fadingEdgeLength;
            int length = (int) fadeHeight2;
            if (verticalEdges) {
                drawRight = false;
                topFadeStrength = 0.0f;
                if (top4 + length > bottom3 - length) {
                    length = (bottom3 - top4) / 2;
                }
            } else {
                drawRight = false;
                topFadeStrength = 0.0f;
            }
            if (horizontalEdges && left + length > right2 - length) {
                length = (right2 - left) / 2;
            }
            int length2 = length;
            if (verticalEdges) {
                boolean z = verticalEdges;
                float topFadeStrength2 = Math.max(0.0f, Math.min(1.0f, getTopFadingEdgeStrength()));
                drawTop = topFadeStrength2 * fadeHeight2 > 1.0f;
                float topFadeStrength3 = topFadeStrength2;
                bottomFadeStrength = Math.max(0.0f, Math.min(1.0f, getBottomFadingEdgeStrength()));
                drawBottom = bottomFadeStrength * fadeHeight2 > 1.0f;
                topFadeStrength = topFadeStrength3;
            } else {
                drawBottom = false;
            }
            if (horizontalEdges) {
                boolean z2 = horizontalEdges;
                leftFadeStrength2 = Math.max(0.0f, Math.min(1.0f, getLeftFadingEdgeStrength()));
                drawLeft = leftFadeStrength2 * fadeHeight2 > 1.0f;
                rightFadeStrength = Math.max(0.0f, Math.min(1.0f, getRightFadingEdgeStrength()));
                drawRight = rightFadeStrength * fadeHeight2 > 1.0f;
            } else {
                drawLeft = false;
            }
            int saveCount2 = canvas.getSaveCount();
            int solidColor = getSolidColor();
            if (solidColor == 0) {
                if (drawTop) {
                    canvas3.saveUnclippedLayer(left, top4, right2, top4 + length2);
                }
                if (drawBottom) {
                    canvas3.saveUnclippedLayer(left, bottom3 - length2, right2, bottom3);
                }
                if (drawLeft) {
                    canvas3.saveUnclippedLayer(left, top4, left + length2, bottom3);
                }
                if (drawRight) {
                    canvas3.saveUnclippedLayer(right2 - length2, top4, right2, bottom3);
                }
            } else {
                scrollabilityCache.setFadeColor(solidColor);
            }
            if (!dirtyOpaque) {
                onDraw(canvas);
            }
            dispatchDraw(canvas);
            Paint p4 = scrollabilityCache.paint;
            boolean z3 = dirtyOpaque;
            Matrix matrix = scrollabilityCache.matrix;
            Shader fade = scrollabilityCache.shader;
            if (drawTop) {
                ScrollabilityCache scrollabilityCache2 = scrollabilityCache;
                matrix.setScale(1.0f, fadeHeight2 * topFadeStrength);
                matrix.postTranslate((float) left, (float) top4);
                fade.setLocalMatrix(matrix);
                p4.setShader(fade);
                saveCount = saveCount2;
                top = top4;
                top2 = left;
                leftFadeStrength = leftFadeStrength2;
                bottom = bottom3;
                fadeHeight = fadeHeight2;
                int i3 = solidColor;
                p = p4;
                f = 1.0f;
                canvas3.drawRect((float) left, (float) top4, (float) right2, (float) (top4 + length2), p4);
            } else {
                top = top4;
                int i4 = solidColor;
                p = p4;
                ScrollabilityCache scrollabilityCache3 = scrollabilityCache;
                saveCount = saveCount2;
                leftFadeStrength = leftFadeStrength2;
                f = 1.0f;
                top2 = left;
                bottom = bottom3;
                fadeHeight = fadeHeight2;
            }
            if (drawBottom) {
                matrix.setScale(f, fadeHeight * bottomFadeStrength);
                matrix.postRotate(180.0f);
                matrix.postTranslate((float) top2, (float) bottom);
                fade.setLocalMatrix(matrix);
                Paint p5 = p;
                p5.setShader(fade);
                boolean z4 = drawBottom;
                f2 = f;
                canvas2 = canvas;
                p2 = p5;
                canvas2.drawRect((float) top2, (float) (bottom - length2), (float) right2, (float) bottom, p5);
            } else {
                p2 = p;
                f2 = f;
                canvas2 = canvas;
            }
            if (drawLeft) {
                matrix.setScale(f2, fadeHeight * leftFadeStrength);
                matrix.postRotate(-90.0f);
                int top5 = top;
                matrix.postTranslate((float) top2, (float) top5);
                fade.setLocalMatrix(matrix);
                Paint p6 = p2;
                p6.setShader(fade);
                p3 = p6;
                top3 = top5;
                canvas2.drawRect((float) top2, (float) top5, (float) (top2 + length2), (float) bottom, p3);
            } else {
                top3 = top;
                p3 = p2;
            }
            if (drawRight) {
                matrix.setScale(f2, fadeHeight * rightFadeStrength);
                matrix.postRotate(90.0f);
                int top6 = top3;
                matrix.postTranslate((float) right2, (float) top6);
                fade.setLocalMatrix(matrix);
                Paint p7 = p3;
                p7.setShader(fade);
                Paint paint = p7;
                canvas2.drawRect((float) (right2 - length2), (float) top6, (float) right2, (float) bottom, p7);
            } else {
                int i5 = top3;
            }
            canvas2.restoreToCount(saveCount);
            Shader shader = fade;
            drawAutofilledHighlight(canvas);
            if (this.mOverlay != null && !this.mOverlay.isEmpty()) {
                this.mOverlay.getOverlayView().dispatchDraw(canvas2);
            }
            onDrawForeground(canvas);
            if (debugDraw()) {
                debugDrawFocus(canvas);
            }
            return;
        }
        if (!dirtyOpaque) {
            onDraw(canvas);
        }
        dispatchDraw(canvas);
        drawAutofilledHighlight(canvas);
        if (this.mOverlay != null && !this.mOverlay.isEmpty()) {
            this.mOverlay.getOverlayView().dispatchDraw(canvas3);
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

    /* access modifiers changed from: package-private */
    public void setBackgroundBounds() {
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

    /* JADX INFO: finally extract failed */
    private RenderNode getDrawableRenderNode(Drawable drawable, RenderNode renderNode) {
        if (renderNode == null) {
            renderNode = RenderNode.create(drawable.getClass().getName(), this);
        }
        Rect bounds = drawable.getBounds();
        DisplayListCanvas canvas = renderNode.start(bounds.width(), bounds.height());
        canvas.translate((float) (-bounds.left), (float) (-bounds.top));
        try {
            drawable.draw(canvas);
            renderNode.end(canvas);
            renderNode.setLeftTopRightBottom(bounds.left, bounds.top, bounds.right, bounds.bottom);
            renderNode.setProjectBackwards(drawable.isProjected());
            renderNode.setProjectionReceiver(true);
            renderNode.setClipToBounds(false);
            return renderNode;
        } catch (Throwable th) {
            renderNode.end(canvas);
            throw th;
        }
    }

    public ViewOverlay getOverlay() {
        if (this.mOverlay == null) {
            this.mOverlay = new ViewOverlay(this.mContext, this);
        }
        return this.mOverlay;
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public int getSolidColor() {
        return 0;
    }

    private static String printFlags(int flags) {
        String output = "";
        int numFlags = 0;
        if ((flags & 1) == 1) {
            output = output + "TAKES_FOCUS";
            numFlags = 0 + 1;
        }
        int i = flags & 12;
        if (i == 4) {
            if (numFlags > 0) {
                output = output + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
            }
            return output + "INVISIBLE";
        } else if (i != 8) {
            return output;
        } else {
            if (numFlags > 0) {
                output = output + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
            }
            return output + "GONE";
        }
    }

    private static String printPrivateFlags(int privateFlags) {
        String output = "";
        int numFlags = 0;
        if ((privateFlags & 1) == 1) {
            output = output + "WANTS_FOCUS";
            numFlags = 0 + 1;
        }
        if ((privateFlags & 2) == 2) {
            if (numFlags > 0) {
                output = output + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
            }
            output = output + "FOCUSED";
            numFlags++;
        }
        if ((privateFlags & 4) == 4) {
            if (numFlags > 0) {
                output = output + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
            }
            output = output + "SELECTED";
            numFlags++;
        }
        if ((privateFlags & 8) == 8) {
            if (numFlags > 0) {
                output = output + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
            }
            output = output + "IS_ROOT_NAMESPACE";
            numFlags++;
        }
        if ((privateFlags & 16) == 16) {
            if (numFlags > 0) {
                output = output + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
            }
            output = output + "HAS_BOUNDS";
            numFlags++;
        }
        if ((privateFlags & 32) != 32) {
            return output;
        }
        if (numFlags > 0) {
            output = output + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        }
        return output + "DRAWN";
    }

    public boolean isLayoutRequested() {
        return (this.mPrivateFlags & 4096) == 4096;
    }

    public static boolean isLayoutModeOptical(Object o) {
        return (o instanceof ViewGroup) && ((ViewGroup) o).isLayoutModeOptical();
    }

    private boolean setOpticalFrame(int left, int top, int right, int bottom) {
        Insets parentInsets = this.mParent instanceof View ? ((View) this.mParent).getOpticalInsets() : Insets.NONE;
        Insets childInsets = getOpticalInsets();
        return setFrame((parentInsets.left + left) - childInsets.left, (parentInsets.top + top) - childInsets.top, parentInsets.left + right + childInsets.right, parentInsets.top + bottom + childInsets.bottom);
    }

    public void layout(int l, int t, int r, int b) {
        boolean z;
        if ((this.mPrivateFlags3 & 8) != 0) {
            onMeasure(this.mOldWidthMeasureSpec, this.mOldHeightMeasureSpec);
            this.mPrivateFlags3 &= -9;
        }
        int oldL = this.mLeft;
        int oldT = this.mTop;
        int oldB = this.mBottom;
        int oldR = this.mRight;
        boolean changed = isLayoutModeOptical(this.mParent) ? setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);
        boolean z2 = false;
        Object obj = null;
        if (changed || (this.mPrivateFlags & 8192) == 8192) {
            onLayout(changed, l, t, r, b);
            if (!shouldDrawRoundScrollbar()) {
                this.mRoundScrollbarRenderer = null;
            } else if (this.mRoundScrollbarRenderer == null) {
                this.mRoundScrollbarRenderer = new RoundScrollbarRenderer(this);
            }
            this.mPrivateFlags &= -8193;
            ListenerInfo li = this.mListenerInfo;
            if (li != null && li.mOnLayoutChangeListeners != null) {
                ArrayList<OnLayoutChangeListener> listenersCopy = (ArrayList) li.mOnLayoutChangeListeners.clone();
                int numListeners = listenersCopy.size();
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 >= numListeners) {
                        break;
                    }
                    int oldL2 = oldL;
                    Object obj2 = obj;
                    listenersCopy.get(i2).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
                    i = i2 + 1;
                    z2 = z2;
                    numListeners = numListeners;
                    listenersCopy = listenersCopy;
                    li = li;
                    oldL = oldL2;
                    obj = null;
                }
            }
            z = z2;
        } else {
            int i3 = oldL;
            z = false;
        }
        boolean wasLayoutValid = isLayoutValid();
        this.mPrivateFlags &= -4097;
        this.mPrivateFlags3 |= 4;
        if (!wasLayoutValid && isFocused()) {
            this.mPrivateFlags &= -2;
            if (canTakeFocus()) {
                clearParentsWantFocus();
            } else if (getViewRootImpl() == null || !getViewRootImpl().isInLayout()) {
                clearFocusInternal(null, true, z);
                clearParentsWantFocus();
            } else if (!hasParentWantsFocus()) {
                clearFocusInternal(null, true, z);
            }
        } else if ((this.mPrivateFlags & 1) != 0) {
            this.mPrivateFlags &= -2;
            View focused = findFocus();
            if (focused != null && !restoreDefaultFocus() && !hasParentWantsFocus()) {
                focused.clearFocusInternal(null, true, z);
            }
        }
        if ((this.mPrivateFlags3 & 134217728) != 0) {
            this.mPrivateFlags3 &= -134217729;
            notifyEnterOrExitForAutoFillIfNeeded(true);
        }
    }

    private boolean hasParentWantsFocus() {
        ViewParent parent = this.mParent;
        while (parent instanceof ViewGroup) {
            ViewGroup pv = (ViewGroup) parent;
            if ((pv.mPrivateFlags & 1) != 0) {
                return true;
            }
            parent = pv.mParent;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    /* access modifiers changed from: protected */
    public boolean setFrame(int left, int top, int right, int bottom) {
        int i = left;
        int i2 = top;
        int i3 = right;
        int i4 = bottom;
        boolean changed = false;
        if (!(this.mLeft == i && this.mRight == i3 && this.mTop == i2 && this.mBottom == i4)) {
            changed = true;
            int drawn = this.mPrivateFlags & 32;
            int oldWidth = this.mRight - this.mLeft;
            int oldHeight = this.mBottom - this.mTop;
            int newWidth = i3 - i;
            int newHeight = i4 - i2;
            boolean sizeChanged = (newWidth == oldWidth && newHeight == oldHeight) ? false : true;
            invalidate(sizeChanged);
            this.mLeft = i;
            this.mTop = i2;
            this.mRight = i3;
            this.mBottom = i4;
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
                boolean unused = this.mForegroundInfo.mBoundsChanged = true;
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
        if (!sCanFocusZeroSized && isLayoutValid() && (!(this.mParent instanceof ViewGroup) || !((ViewGroup) this.mParent).isLayoutSuppressed())) {
            if (newWidth <= 0 || newHeight <= 0) {
                if (hasFocus()) {
                    clearFocus();
                    if (this.mParent instanceof ViewGroup) {
                        ((ViewGroup) this.mParent).clearFocusedInCluster();
                    }
                }
                clearAccessibilityFocus();
            } else if ((oldWidth <= 0 || oldHeight <= 0) && this.mParent != null && canTakeFocus()) {
                this.mParent.focusableViewAvailable(this);
            }
        }
        rebuildOutline();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
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

    /* access modifiers changed from: protected */
    public void resolveDrawables() {
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

    /* access modifiers changed from: package-private */
    public boolean areDrawablesResolved() {
        return (this.mPrivateFlags2 & 1073741824) == 1073741824;
    }

    public void onResolveDrawables(int layoutDirection) {
    }

    /* access modifiers changed from: protected */
    public void resetResolvedDrawables() {
        resetResolvedDrawablesInternal();
    }

    /* access modifiers changed from: package-private */
    public void resetResolvedDrawablesInternal() {
        this.mPrivateFlags2 &= -1073741825;
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable who) {
        return who == this.mBackground || (this.mForegroundInfo != null && this.mForegroundInfo.mDrawable == who) || this.mDefaultFocusHighlight == who;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        int[] state = getDrawableState();
        boolean changed = false;
        Drawable bg = this.mBackground;
        if (bg != null && bg.isStateful()) {
            changed = false | bg.setState(state);
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
                changed |= scrollBar.setState(state) && this.mScrollCache.state != 0;
            }
        }
        if (this.mStateListAnimator != null) {
            this.mStateListAnimator.setState(state);
        }
        if (changed) {
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
            TypedArray ta = this.mContext.obtainStyledAttributes(new int[]{16843534});
            this.mDefaultFocusHighlightCache = ta.getDrawable(0);
            ta.recycle();
        }
        return this.mDefaultFocusHighlightCache;
    }

    private void setDefaultFocusHighlight(Drawable highlight) {
        this.mDefaultFocusHighlight = highlight;
        boolean z = true;
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
                if (getWindowVisibility() != 0 || !isShown()) {
                    z = false;
                }
                highlight.setVisible(z, false);
            }
            highlight.setCallback(this);
        } else if ((this.mViewFlags & 128) != 0 && this.mBackground == null && (this.mForegroundInfo == null || this.mForegroundInfo.mDrawable == null)) {
            this.mPrivateFlags |= 128;
        }
        invalidate();
    }

    public boolean isDefaultFocusHighlightNeeded(Drawable background, Drawable foreground) {
        boolean lackFocusState = (background == null || !background.isStateful() || !background.hasFocusStateSpecified()) && (foreground == null || !foreground.isStateful() || !foreground.hasFocusStateSpecified());
        if (isInTouchMode() || !getDefaultFocusHighlightEnabled() || !lackFocusState || !isAttachedToWindow() || !sUseDefaultFocusHighlight) {
            return false;
        }
        return true;
    }

    private void switchDefaultFocusHighlight() {
        Drawable drawable;
        if (isFocused()) {
            Drawable drawable2 = this.mBackground;
            if (this.mForegroundInfo == null) {
                drawable = null;
            } else {
                drawable = this.mForegroundInfo.mDrawable;
            }
            boolean needed = isDefaultFocusHighlightNeeded(drawable2, drawable);
            boolean active = this.mDefaultFocusHighlight != null;
            if (needed && !active) {
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

    /* access modifiers changed from: protected */
    public int[] onCreateDrawableState(int extraSpace) {
        int[] fullState;
        if ((this.mViewFlags & 4194304) == 4194304 && (this.mParent instanceof View)) {
            return ((View) this.mParent).onCreateDrawableState(extraSpace);
        }
        int privateFlags = this.mPrivateFlags;
        int viewStateIndex = 0;
        if ((privateFlags & 16384) != 0) {
            viewStateIndex = 0 | 16;
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
                Rect padding = sThreadLocal.get();
                if (padding == null) {
                    padding = new Rect();
                    sThreadLocal.set(padding);
                }
                resetResolvedDrawablesInternal();
                background.setLayoutDirection(getLayoutDirection());
                if (background.getPadding(padding)) {
                    resetResolvedPaddingInternal();
                    if (background.getLayoutDirection() != 1) {
                        this.mUserPaddingLeftInitial = padding.left;
                        this.mUserPaddingRightInitial = padding.right;
                        internalSetPadding(padding.left, padding.top, padding.right, padding.bottom);
                    } else {
                        this.mUserPaddingLeftInitial = padding.right;
                        this.mUserPaddingRightInitial = padding.left;
                        internalSetPadding(padding.right, padding.top, padding.left, padding.bottom);
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
                    background.setVisible(getWindowVisibility() == 0 && isShown(), false);
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
        if (this.mBackgroundTint != null) {
            return this.mBackgroundTint.mTintList;
        }
        return null;
    }

    public void setBackgroundTintMode(PorterDuff.Mode tintMode) {
        if (this.mBackgroundTint == null) {
            this.mBackgroundTint = new TintInfo();
        }
        this.mBackgroundTint.mTintMode = tintMode;
        this.mBackgroundTint.mHasTintMode = true;
        applyBackgroundTint();
    }

    public PorterDuff.Mode getBackgroundTintMode() {
        if (this.mBackgroundTint != null) {
            return this.mBackgroundTint.mTintMode;
        }
        return null;
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
        if (this.mForegroundInfo != null) {
            return this.mForegroundInfo.mDrawable;
        }
        return null;
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
            Drawable unused = this.mForegroundInfo.mDrawable = foreground;
            boolean z = true;
            boolean unused2 = this.mForegroundInfo.mBoundsChanged = true;
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
                    if (getWindowVisibility() != 0 || !isShown()) {
                        z = false;
                    }
                    foreground.setVisible(z, false);
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
        if (this.mForegroundInfo != null) {
            return this.mForegroundInfo.mInsidePadding;
        }
        return true;
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
            if ((8388615 & gravity) == 0) {
                gravity |= Gravity.START;
            }
            if ((gravity & 112) == 0) {
                gravity |= 48;
            }
            int unused = this.mForegroundInfo.mGravity = gravity;
            requestLayout();
        }
    }

    public void setForegroundTintList(ColorStateList tint) {
        if (this.mForegroundInfo == null) {
            this.mForegroundInfo = new ForegroundInfo();
        }
        if (this.mForegroundInfo.mTintInfo == null) {
            TintInfo unused = this.mForegroundInfo.mTintInfo = new TintInfo();
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

    public void setForegroundTintMode(PorterDuff.Mode tintMode) {
        if (this.mForegroundInfo == null) {
            this.mForegroundInfo = new ForegroundInfo();
        }
        if (this.mForegroundInfo.mTintInfo == null) {
            TintInfo unused = this.mForegroundInfo.mTintInfo = new TintInfo();
        }
        this.mForegroundInfo.mTintInfo.mTintMode = tintMode;
        this.mForegroundInfo.mTintInfo.mHasTintMode = true;
        applyForegroundTint();
    }

    public PorterDuff.Mode getForegroundTintMode() {
        if (this.mForegroundInfo == null || this.mForegroundInfo.mTintInfo == null) {
            return null;
        }
        return this.mForegroundInfo.mTintInfo.mTintMode;
    }

    private void applyForegroundTint() {
        if (this.mForegroundInfo != null && this.mForegroundInfo.mDrawable != null && this.mForegroundInfo.mTintInfo != null) {
            TintInfo tintInfo = this.mForegroundInfo.mTintInfo;
            if (tintInfo.mHasTintList || tintInfo.mHasTintMode) {
                Drawable unused = this.mForegroundInfo.mDrawable = this.mForegroundInfo.mDrawable.mutate();
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
                boolean unused = this.mForegroundInfo.mBoundsChanged = false;
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

    /* access modifiers changed from: protected */
    public void internalSetPadding(int left, int top, int right, int bottom) {
        this.mUserPaddingLeft = left;
        this.mUserPaddingRight = right;
        this.mUserPaddingBottom = bottom;
        int viewFlags = this.mViewFlags;
        boolean changed = false;
        if ((viewFlags & 768) != 0) {
            int i = 0;
            if ((viewFlags & 512) != 0) {
                int offset = (viewFlags & 16777216) == 0 ? 0 : getVerticalScrollbarWidth();
                switch (this.mVerticalScrollbarPosition) {
                    case 0:
                        if (!isLayoutRtl()) {
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
        if (getLayoutDirection() != 1) {
            this.mUserPaddingLeftInitial = start;
            this.mUserPaddingRightInitial = end;
            internalSetPadding(start, top, end, bottom);
            return;
        }
        this.mUserPaddingLeftInitial = end;
        this.mUserPaddingRightInitial = start;
        internalSetPadding(end, top, start, bottom);
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

    /* access modifiers changed from: package-private */
    public Insets computeOpticalInsets() {
        return this.mBackground == null ? Insets.NONE : this.mBackground.getOpticalInsets();
    }

    public void resetPaddingToInitialValues() {
        if (isRtlCompatibilityMode()) {
            this.mPaddingLeft = this.mUserPaddingLeftInitial;
            this.mPaddingRight = this.mUserPaddingRightInitial;
            return;
        }
        if (isLayoutRtl()) {
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
            this.mPrivateFlags = (this.mPrivateFlags & -5) | (selected ? 4 : 0);
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

    /* access modifiers changed from: protected */
    public void dispatchSetSelected(boolean selected) {
    }

    @ViewDebug.ExportedProperty
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
            this.mPrivateFlags = i2 | i;
            invalidate(true);
            refreshDrawableState();
            dispatchSetActivated(activated);
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchSetActivated(boolean activated) {
    }

    @ViewDebug.ExportedProperty
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

    /* JADX WARNING: type inference failed for: r1v3, types: [android.view.ViewParent] */
    /* JADX WARNING: Multi-variable type inference failed */
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

    @ViewDebug.ExportedProperty(category = "layout", indexMapping = {@ViewDebug.IntToString(from = 0, to = "x"), @ViewDebug.IntToString(from = 1, to = "y")})
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

    /* access modifiers changed from: protected */
    public <T extends View> T findViewTraversal(int id) {
        if (id == this.mID) {
            return this;
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public <T extends View> T findViewWithTagTraversal(Object tag) {
        if (tag == null || !tag.equals(this.mTag)) {
            return null;
        }
        return this;
    }

    /* access modifiers changed from: protected */
    public <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
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

    public final <T extends View> T requireViewById(int id) {
        T view = findViewById(id);
        if (view != null) {
            return view;
        }
        throw new IllegalArgumentException("ID does not reference a View inside this View");
    }

    /* access modifiers changed from: package-private */
    public final <T extends View> T findViewByAccessibilityId(int accessibilityId) {
        T t = null;
        if (accessibilityId < 0) {
            return null;
        }
        T view = findViewByAccessibilityIdTraversal(accessibilityId);
        if (view == null) {
            return null;
        }
        if (view.includeForAccessibility()) {
            t = view;
        }
        return t;
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
        T view;
        View start2 = start;
        View childToSkip = null;
        while (true) {
            view = start2.findViewByPredicateTraversal(predicate, childToSkip);
            if (view != null || start2 == this) {
                return view;
            }
            ViewParent parent = start2.getParent();
            if (parent == null || !(parent instanceof View)) {
                return null;
            }
            childToSkip = start2;
            start2 = (View) parent;
        }
        return view;
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

    @ViewDebug.CapturedViewProperty
    public int getId() {
        return this.mID;
    }

    @ViewDebug.ExportedProperty
    public Object getTag() {
        return this.mTag;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public Object getTag(int key) {
        if (this.mKeyedTags != null) {
            return this.mKeyedTags.get(key);
        }
        return null;
    }

    public void setTag(int key, Object tag) {
        if ((key >>> 24) >= 2) {
            setKeyedTag(key, tag);
            return;
        }
        throw new IllegalArgumentException("The key must be an application-specific resource id.");
    }

    public void setTagInternal(int key, Object tag) {
        if ((key >>> 24) == 1) {
            setKeyedTag(key, tag);
            return;
        }
        throw new IllegalArgumentException("The key must be a framework-specific resource id.");
    }

    private void setKeyedTag(int key, Object tag) {
        if (this.mKeyedTags == null) {
            this.mKeyedTags = new SparseArray<>(2);
        }
        this.mKeyedTags.put(key, tag);
    }

    public void debug() {
        debug(0);
    }

    /* access modifiers changed from: protected */
    public void debug(int depth) {
        String output;
        String output2 = debugIndent(depth - 1);
        String output3 = output2 + "+ " + this;
        if (getId() != -1) {
            output3 = output3 + " (id=" + id + ")";
        }
        if (getTag() != null) {
            output3 = output3 + " (tag=" + tag + ")";
        }
        Log.d(VIEW_LOG_TAG, output3);
        if ((this.mPrivateFlags & 2) != 0) {
            Log.d(VIEW_LOG_TAG, debugIndent(depth) + " FOCUSED");
        }
        String output4 = debugIndent(depth);
        Log.d(VIEW_LOG_TAG, output4 + "frame={" + this.mLeft + ", " + this.mTop + ", " + this.mRight + ", " + this.mBottom + "} scroll={" + this.mScrollX + ", " + this.mScrollY + "} ");
        if (!(this.mPaddingLeft == 0 && this.mPaddingTop == 0 && this.mPaddingRight == 0 && this.mPaddingBottom == 0)) {
            String output5 = debugIndent(depth);
            Log.d(VIEW_LOG_TAG, output5 + "padding={" + this.mPaddingLeft + ", " + this.mPaddingTop + ", " + this.mPaddingRight + ", " + this.mPaddingBottom + "}");
        }
        String output6 = debugIndent(depth);
        Log.d(VIEW_LOG_TAG, output6 + "mMeasureWidth=" + this.mMeasuredWidth + " mMeasureHeight=" + this.mMeasuredHeight);
        String output7 = debugIndent(depth);
        if (this.mLayoutParams == null) {
            output = output7 + "BAD! no layout params";
        } else {
            output = this.mLayoutParams.debug(output7);
        }
        Log.d(VIEW_LOG_TAG, output);
        String output8 = debugIndent(depth);
        String output9 = output8 + "flags={";
        String output10 = output9 + printFlags(this.mViewFlags);
        Log.d(VIEW_LOG_TAG, output10 + "}");
        String output11 = debugIndent(depth);
        String output12 = output11 + "privateFlags={";
        String output13 = output12 + printPrivateFlags(this.mPrivateFlags);
        Log.d(VIEW_LOG_TAG, output13 + "}");
    }

    protected static String debugIndent(int depth) {
        StringBuilder spaces = new StringBuilder(((depth * 2) + 3) * 2);
        for (int i = 0; i < (depth * 2) + 3; i++) {
            spaces.append(' ');
            spaces.append(' ');
        }
        return spaces.toString();
    }

    @ViewDebug.ExportedProperty(category = "layout")
    public int getBaseline() {
        return -1;
    }

    public boolean isInLayout() {
        ViewRootImpl viewRoot = getViewRootImpl();
        return viewRoot != null && viewRoot.isInLayout();
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
        if (!(this.mParent == null || this.mParent.isLayoutRequested() || this.mParent == null)) {
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

    /* JADX WARNING: Removed duplicated region for block: B:59:0x00f1  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0113  */
    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMeasureSpec2;
        int heightMeasureSpec2;
        boolean optical = isLayoutModeOptical(this);
        if (optical != isLayoutModeOptical(this.mParent)) {
            Insets insets = getOpticalInsets();
            int oWidth = insets.left + insets.right;
            int oHeight = insets.top + insets.bottom;
            widthMeasureSpec2 = MeasureSpec.adjust(widthMeasureSpec, optical ? -oWidth : oWidth);
            heightMeasureSpec2 = MeasureSpec.adjust(heightMeasureSpec, optical ? -oHeight : oHeight);
        } else {
            widthMeasureSpec2 = widthMeasureSpec;
            heightMeasureSpec2 = heightMeasureSpec;
        }
        long key = (((long) widthMeasureSpec2) << 32) | (((long) heightMeasureSpec2) & 4294967295L);
        if (this.mMeasureCache == null) {
            this.mMeasureCache = new LongSparseLongArray(2);
        }
        boolean needsLayout = false;
        boolean forceLayout = (this.mPrivateFlags & 4096) == 4096;
        boolean specChanged = (widthMeasureSpec2 == this.mOldWidthMeasureSpec && heightMeasureSpec2 == this.mOldHeightMeasureSpec) ? false : true;
        boolean isSpecExactly = MeasureSpec.getMode(widthMeasureSpec2) == 1073741824 && MeasureSpec.getMode(heightMeasureSpec2) == 1073741824;
        boolean matchesSpecSize = getMeasuredWidth() == MeasureSpec.getSize(widthMeasureSpec2) && getMeasuredHeight() == MeasureSpec.getSize(heightMeasureSpec2);
        if (specChanged && (sAlwaysRemeasureExactly || !isSpecExactly || !matchesSpecSize)) {
            needsLayout = true;
        }
        if (forceLayout || needsLayout) {
            this.mPrivateFlags &= -2049;
            resolveRtlPropertiesIfNeeded();
            int cacheIndex = forceLayout ? -1 : this.mMeasureCache.indexOfKey(key);
            if (cacheIndex < 0) {
                boolean z = specChanged;
            } else if (sIgnoreMeasureCache) {
                boolean z2 = forceLayout;
                boolean z3 = specChanged;
            } else {
                long value = this.mMeasureCache.valueAt(cacheIndex);
                boolean z4 = forceLayout;
                boolean z5 = specChanged;
                setMeasuredDimensionRaw((int) (value >> 32), (int) value);
                this.mPrivateFlags3 |= 8;
                if ((this.mPrivateFlags & 2048) != 2048) {
                    this.mPrivateFlags |= 8192;
                } else {
                    int i = heightMeasureSpec2;
                    throw new IllegalStateException("View with id " + getId() + ": " + getClass().getName() + "#onMeasure() did not set the measured dimension by calling setMeasuredDimension()");
                }
            }
            onMeasure(widthMeasureSpec2, heightMeasureSpec2);
            this.mPrivateFlags3 &= -9;
            if ((this.mPrivateFlags & 2048) != 2048) {
            }
        } else {
            boolean z6 = forceLayout;
            boolean z7 = specChanged;
        }
        this.mOldWidthMeasureSpec = widthMeasureSpec2;
        this.mOldHeightMeasureSpec = heightMeasureSpec2;
        boolean z8 = optical;
        int i2 = heightMeasureSpec2;
        this.mMeasureCache.put(key, (((long) this.mMeasuredHeight) & 4294967295L) | (((long) this.mMeasuredWidth) << 32));
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    /* access modifiers changed from: protected */
    public final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        boolean optical = isLayoutModeOptical(this);
        if (optical != isLayoutModeOptical(this.mParent)) {
            Insets insets = getOpticalInsets();
            int opticalWidth = insets.left + insets.right;
            int opticalHeight = insets.top + insets.bottom;
            measuredWidth += optical ? opticalWidth : -opticalWidth;
            measuredHeight += optical ? opticalHeight : -opticalHeight;
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
        if (specMode != Integer.MIN_VALUE) {
            if (specMode != 1073741824) {
                result = size;
            } else {
                result = specSize;
            }
        } else if (specSize < size) {
            result = 16777216 | specSize;
        } else {
            result = size;
        }
        return (-16777216 & childMeasuredState) | result;
    }

    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode != Integer.MIN_VALUE) {
            if (specMode == 0) {
                return size;
            }
            if (specMode != 1073741824) {
                return result;
            }
        }
        return specSize;
    }

    /* access modifiers changed from: protected */
    public int getSuggestedMinimumHeight() {
        return this.mBackground == null ? this.mMinHeight : Math.max(this.mMinHeight, this.mBackground.getMinimumHeight());
    }

    /* access modifiers changed from: protected */
    public int getSuggestedMinimumWidth() {
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

    /* access modifiers changed from: protected */
    public void onAnimationStart() {
        this.mPrivateFlags |= 65536;
    }

    /* access modifiers changed from: protected */
    public void onAnimationEnd() {
        this.mPrivateFlags &= -65537;
    }

    /* access modifiers changed from: protected */
    public boolean onSetAlpha(int alpha) {
        return false;
    }

    public boolean gatherTransparentRegion(Region region) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (!(region == null || attachInfo == null)) {
            if ((this.mPrivateFlags & 128) == 0) {
                int[] location = attachInfo.mTransparentLocation;
                getLocationInWindow(location);
                int shadowOffset = getZ() > 0.0f ? (int) getZ() : 0;
                region.op(location[0] - shadowOffset, location[1] - shadowOffset, ((location[0] + this.mRight) - this.mLeft) + shadowOffset, ((location[1] + this.mBottom) - this.mTop) + (shadowOffset * 3), Region.Op.DIFFERENCE);
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
        if (this.mAttachInfo != null && this.mAttachInfo.mRootCallbacks != null && isSoundEffectsEnabled()) {
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
        if ((flags & 1) == 0 && !isHapticFeedbackEnabled()) {
            return false;
        }
        AttachInfo.Callbacks callbacks = this.mAttachInfo.mRootCallbacks;
        if ((flags & 2) != 0) {
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
        if (this.mAttachInfo != null) {
            return this.mAttachInfo.mSystemUiVisibility;
        }
        return 0;
    }

    public void onWindowSystemUiVisibilityChanged(int visible) {
    }

    public void dispatchWindowSystemUiVisiblityChanged(int visible) {
        onWindowSystemUiVisibilityChanged(visible);
    }

    public void setOnSystemUiVisibilityChangeListener(OnSystemUiVisibilityChangeListener l) {
        OnSystemUiVisibilityChangeListener unused = getListenerInfo().mOnSystemUiVisibilityChangeListener = l;
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

    /* access modifiers changed from: package-private */
    public boolean updateLocalSystemUiVisibility(int localValue, int localChanges) {
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

    /* JADX WARNING: Removed duplicated region for block: B:74:0x0186  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01a0  */
    public final boolean startDragAndDrop(ClipData data, DragShadowBuilder shadowBuilder, Object myLocalState, int flags) {
        int i;
        Surface surface;
        ViewRootImpl root;
        SurfaceSession session;
        int i2;
        SurfaceSession session2;
        ClipData clipData = data;
        DragShadowBuilder dragShadowBuilder = shadowBuilder;
        if (this.mAttachInfo == null) {
            Log.w(VIEW_LOG_TAG, "startDragAndDrop called on a detached view.");
            return false;
        }
        if (clipData != null) {
            i = flags;
            clipData.prepareToLeaveProcess((i & 256) != 0);
        } else {
            i = flags;
        }
        Point shadowSize = new Point();
        Point shadowTouchPoint = new Point();
        dragShadowBuilder.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
        if (shadowSize.x < 0 || shadowSize.y < 0 || shadowTouchPoint.x < 0 || shadowTouchPoint.y < 0) {
            Point point = shadowSize;
            throw new IllegalStateException("Drag shadow dimensions must not be negative");
        }
        if (shadowSize.x == 0 || shadowSize.y == 0) {
            if (sAcceptZeroSizeDragShadow) {
                shadowSize.x = 1;
                shadowSize.y = 1;
            } else {
                Point point2 = shadowSize;
                throw new IllegalStateException("Drag shadow dimensions must be positive");
            }
        }
        if (this.mAttachInfo.mDragSurface != null) {
            this.mAttachInfo.mDragSurface.release();
        }
        this.mAttachInfo.mDragSurface = new Surface();
        this.mAttachInfo.mDragToken = null;
        ViewRootImpl root2 = this.mAttachInfo.mViewRootImpl;
        SurfaceSession session3 = new SurfaceSession(root2.mSurface);
        SurfaceControl surface2 = new SurfaceControl.Builder(session3).setName("drag surface").setSize(shadowSize.x, shadowSize.y).setFormat(-3).build();
        try {
            this.mAttachInfo.mDragSurface.copyFrom(surface2);
            Canvas canvas = this.mAttachInfo.mDragSurface.lockCanvas(null);
            try {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                dragShadowBuilder.onDrawShadow(canvas);
                this.mAttachInfo.mDragSurface.unlockCanvasAndPost(canvas);
                root2.setLocalDragState(myLocalState);
                root2.getLastTouchPoint(shadowSize);
                try {
                    i2 = i;
                    Canvas canvas2 = canvas;
                    SurfaceControl surfaceControl = surface2;
                    Canvas canvas3 = canvas2;
                    root = root2;
                    session2 = session3;
                    surface = null;
                    Point point3 = shadowTouchPoint;
                    Point point4 = shadowSize;
                } catch (Exception e) {
                    e = e;
                    SurfaceControl surfaceControl2 = surface2;
                    session = session3;
                    root = root2;
                    Point point5 = shadowTouchPoint;
                    Point point6 = shadowSize;
                    surface = null;
                    try {
                        Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e);
                        if (this.mAttachInfo.mDragToken == null) {
                        }
                        session.kill();
                        return false;
                    } catch (Throwable th) {
                        e = th;
                        if (this.mAttachInfo.mDragToken == null) {
                        }
                        session.kill();
                        throw e;
                    }
                } catch (Throwable th2) {
                    e = th2;
                    SurfaceControl surfaceControl3 = surface2;
                    session = session3;
                    root = root2;
                    Point point7 = shadowTouchPoint;
                    Point point8 = shadowSize;
                    surface = null;
                    if (this.mAttachInfo.mDragToken == null) {
                    }
                    session.kill();
                    throw e;
                }
            } catch (Exception e2) {
                e = e2;
                Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e);
                if (this.mAttachInfo.mDragToken == null) {
                }
                session.kill();
                return false;
            } catch (Throwable th3) {
                SurfaceControl surfaceControl4 = surface2;
                session = session3;
                root = root2;
                surface = null;
                Point point9 = shadowTouchPoint;
                Point point10 = shadowSize;
                this.mAttachInfo.mDragSurface.unlockCanvasAndPost(canvas);
                throw th3;
            }
            try {
                this.mAttachInfo.mDragToken = this.mAttachInfo.mSession.performDrag(this.mAttachInfo.mWindow, i2, surface2, root2.getLastTouchSource(), (float) shadowSize.x, (float) shadowSize.y, (float) shadowTouchPoint.x, (float) shadowTouchPoint.y, clipData);
                boolean z = this.mAttachInfo.mDragToken != null;
                if (this.mAttachInfo.mDragToken == null) {
                    this.mAttachInfo.mDragSurface.destroy();
                    this.mAttachInfo.mDragSurface = null;
                    root.setLocalDragState(null);
                }
                session2.kill();
                return z;
            } catch (Exception e3) {
                e = e3;
                session = session2;
                Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e);
                if (this.mAttachInfo.mDragToken == null) {
                }
                session.kill();
                return false;
            } catch (Throwable th4) {
                e = th4;
                session = session2;
                if (this.mAttachInfo.mDragToken == null) {
                }
                session.kill();
                throw e;
            }
        } catch (Exception e4) {
            e = e4;
            SurfaceControl surfaceControl5 = surface2;
            session = session3;
            root = root2;
            surface = null;
            Point point11 = shadowTouchPoint;
            Point point12 = shadowSize;
            Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e);
            if (this.mAttachInfo.mDragToken == null) {
                this.mAttachInfo.mDragSurface.destroy();
                this.mAttachInfo.mDragSurface = surface;
                root.setLocalDragState(surface);
            }
            session.kill();
            return false;
        } catch (Throwable th5) {
            e = th5;
            SurfaceControl surfaceControl6 = surface2;
            session = session3;
            root = root2;
            surface = null;
            Point point13 = shadowTouchPoint;
            Point point14 = shadowSize;
            if (this.mAttachInfo.mDragToken == null) {
                this.mAttachInfo.mDragSurface.destroy();
                this.mAttachInfo.mDragSurface = surface;
                root.setLocalDragState(surface);
            }
            session.kill();
            throw e;
        }
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
        Canvas canvas;
        if (this.mAttachInfo == null) {
            Log.w(VIEW_LOG_TAG, "updateDragShadow called on a detached view.");
            return;
        }
        if (this.mAttachInfo.mDragToken != null) {
            try {
                canvas = this.mAttachInfo.mDragSurface.lockCanvas(null);
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                shadowBuilder.onDrawShadow(canvas);
                this.mAttachInfo.mDragSurface.unlockCanvasAndPost(canvas);
            } catch (Exception e) {
                Log.e(VIEW_LOG_TAG, "Unable to update drag shadow", e);
            } catch (Throwable th) {
                this.mAttachInfo.mDragSurface.unlockCanvasAndPost(canvas);
                throw th;
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

    /* access modifiers changed from: package-private */
    public boolean dispatchDragEnterExitInPreN(DragEvent event) {
        return callDragEventHandler(event);
    }

    public boolean dispatchDragEvent(DragEvent event) {
        event.mEventHandlerWasCalled = true;
        if (event.mAction == 2 || event.mAction == 3) {
            getViewRootImpl().setDragFocus(this, event);
        }
        return callDragEventHandler(event);
    }

    /* access modifiers changed from: package-private */
    public final boolean callDragEventHandler(DragEvent event) {
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

    /* access modifiers changed from: package-private */
    public boolean canAcceptDrag() {
        return (this.mPrivateFlags2 & 1) != 0;
    }

    public void onCloseSystemDialogs(String reason) {
    }

    public void applyDrawableToTransparentRegion(Drawable dr, Region region) {
        Region r = dr.getTransparentRegion();
        Rect db = dr.getBounds();
        AttachInfo attachInfo = this.mAttachInfo;
        if (r == null || attachInfo == null) {
            region.op(db, Region.Op.DIFFERENCE);
            return;
        }
        int w = getRight() - getLeft();
        int h = getBottom() - getTop();
        if (db.left > 0) {
            r.op(0, 0, db.left, h, Region.Op.UNION);
        }
        if (db.right < w) {
            r.op(db.right, 0, w, h, Region.Op.UNION);
        }
        if (db.top > 0) {
            r.op(0, 0, w, db.top, Region.Op.UNION);
        }
        if (db.bottom < h) {
            r.op(0, db.bottom, w, h, Region.Op.UNION);
        }
        int[] location = attachInfo.mTransparentLocation;
        getLocationInWindow(location);
        r.translate(location[0], location[1]);
        region.op(r, Region.Op.INTERSECT);
    }

    /* access modifiers changed from: private */
    public void checkForLongClick(int delayOffset, float x, float y) {
        if ((this.mViewFlags & 2097152) == 2097152 || (this.mViewFlags & 1073741824) == 1073741824) {
            this.mHasPerformedLongPress = false;
            if (this.mPendingCheckForLongPress == null) {
                this.mPendingCheckForLongPress = new CheckForLongPress();
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

    /* access modifiers changed from: protected */
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int maxOverScrollX2;
        int maxOverScrollY2;
        int overScrollMode = this.mOverScrollMode;
        boolean canScrollHorizontal = computeHorizontalScrollRange() > computeHorizontalScrollExtent();
        boolean canScrollVertical = computeVerticalScrollRange() > computeVerticalScrollExtent();
        boolean overScrollHorizontal = overScrollMode == 0 || (overScrollMode == 1 && canScrollHorizontal);
        boolean overScrollVertical = overScrollMode == 0 || (overScrollMode == 1 && canScrollVertical);
        int newScrollX = scrollX + deltaX;
        if (!overScrollHorizontal) {
            maxOverScrollX2 = 0;
        } else {
            maxOverScrollX2 = maxOverScrollX;
        }
        int newScrollY = scrollY + deltaY;
        if (!overScrollVertical) {
            maxOverScrollY2 = 0;
        } else {
            maxOverScrollY2 = maxOverScrollY;
        }
        int left = -maxOverScrollX2;
        int right = maxOverScrollX2 + scrollRangeX;
        int i = overScrollMode;
        int overScrollMode2 = -maxOverScrollY2;
        boolean z = canScrollHorizontal;
        int bottom = maxOverScrollY2 + scrollRangeY;
        boolean clampedX = false;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        }
        boolean clampedX2 = clampedX;
        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < overScrollMode2) {
            newScrollY = overScrollMode2;
            clampedY = true;
        }
        int top = overScrollMode2;
        boolean clampedY2 = clampedY;
        onOverScrolled(newScrollX, newScrollY, clampedX2, clampedY2);
        return clampedX2 || clampedY2;
    }

    /* access modifiers changed from: protected */
    public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
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
                    Log.e(VIEW_LOG_TAG, "ViewParent " + p + " does not implement interface method onStartNestedScroll", e);
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
        if (isNestedScrollingEnabled() && this.mNestedScrollingParent != null) {
            boolean z = true;
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

    /* access modifiers changed from: protected */
    public float getVerticalScrollFactor() {
        if (this.mVerticalScrollFactor == 0.0f) {
            TypedValue outValue = new TypedValue();
            if (this.mContext.getTheme().resolveAttribute(16842829, outValue, true)) {
                this.mVerticalScrollFactor = outValue.getDimension(this.mContext.getResources().getDisplayMetrics());
            } else {
                throw new IllegalStateException("Expected theme to define listPreferredItemHeight.");
            }
        }
        return this.mVerticalScrollFactor;
    }

    /* access modifiers changed from: protected */
    public float getHorizontalScrollFactor() {
        return getVerticalScrollFactor();
    }

    @ViewDebug.ExportedProperty(category = "text", mapping = {@ViewDebug.IntToString(from = 0, to = "INHERIT"), @ViewDebug.IntToString(from = 1, to = "FIRST_STRONG"), @ViewDebug.IntToString(from = 2, to = "ANY_RTL"), @ViewDebug.IntToString(from = 3, to = "LTR"), @ViewDebug.IntToString(from = 4, to = "RTL"), @ViewDebug.IntToString(from = 5, to = "LOCALE"), @ViewDebug.IntToString(from = 6, to = "FIRST_STRONG_LTR"), @ViewDebug.IntToString(from = 7, to = "FIRST_STRONG_RTL")})
    public int getRawTextDirection() {
        return (this.mPrivateFlags2 & 448) >> 6;
    }

    public void setTextDirection(int textDirection) {
        if (getRawTextDirection() != textDirection) {
            this.mPrivateFlags2 &= -449;
            resetResolvedTextDirection();
            this.mPrivateFlags2 |= (textDirection << 6) & 448;
            resolveTextDirection();
            onRtlPropertiesChanged(getLayoutDirection());
            requestLayout();
            invalidate(true);
        }
    }

    @ViewDebug.ExportedProperty(category = "text", mapping = {@ViewDebug.IntToString(from = 0, to = "INHERIT"), @ViewDebug.IntToString(from = 1, to = "FIRST_STRONG"), @ViewDebug.IntToString(from = 2, to = "ANY_RTL"), @ViewDebug.IntToString(from = 3, to = "LTR"), @ViewDebug.IntToString(from = 4, to = "RTL"), @ViewDebug.IntToString(from = 5, to = "LOCALE"), @ViewDebug.IntToString(from = 6, to = "FIRST_STRONG_LTR"), @ViewDebug.IntToString(from = 7, to = "FIRST_STRONG_RTL")})
    public int getTextDirection() {
        return (this.mPrivateFlags2 & PFLAG2_TEXT_DIRECTION_RESOLVED_MASK) >> 10;
    }

    public boolean resolveTextDirection() {
        this.mPrivateFlags2 &= -7681;
        if (hasRtlSupport()) {
            int textDirection = getRawTextDirection();
            switch (textDirection) {
                case 0:
                    if (!canResolveTextDirection()) {
                        this.mPrivateFlags2 |= 1024;
                        return false;
                    }
                    try {
                        if (this.mParent == null || this.mParent.isTextDirectionResolved()) {
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
                        } else {
                            this.mPrivateFlags2 |= 1024;
                            return false;
                        }
                    } catch (AbstractMethodError e2) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e2);
                        this.mPrivateFlags2 = this.mPrivateFlags2 | SYSTEM_UI_LAYOUT_FLAGS;
                        return true;
                    }
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
        } else {
            this.mPrivateFlags2 |= 1024;
        }
        this.mPrivateFlags2 |= 512;
        return true;
    }

    public boolean canResolveTextDirection() {
        if (getRawTextDirection() != 0) {
            return true;
        }
        if (this.mParent != null) {
            try {
                return this.mParent.canResolveTextDirection();
            } catch (AbstractMethodError e) {
                Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
            }
        }
        return false;
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

    @ViewDebug.ExportedProperty(category = "text", mapping = {@ViewDebug.IntToString(from = 0, to = "INHERIT"), @ViewDebug.IntToString(from = 1, to = "GRAVITY"), @ViewDebug.IntToString(from = 2, to = "TEXT_START"), @ViewDebug.IntToString(from = 3, to = "TEXT_END"), @ViewDebug.IntToString(from = 4, to = "CENTER"), @ViewDebug.IntToString(from = 5, to = "VIEW_START"), @ViewDebug.IntToString(from = 6, to = "VIEW_END")})
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

    @ViewDebug.ExportedProperty(category = "text", mapping = {@ViewDebug.IntToString(from = 0, to = "INHERIT"), @ViewDebug.IntToString(from = 1, to = "GRAVITY"), @ViewDebug.IntToString(from = 2, to = "TEXT_START"), @ViewDebug.IntToString(from = 3, to = "TEXT_END"), @ViewDebug.IntToString(from = 4, to = "CENTER"), @ViewDebug.IntToString(from = 5, to = "VIEW_START"), @ViewDebug.IntToString(from = 6, to = "VIEW_END")})
    public int getTextAlignment() {
        return (this.mPrivateFlags2 & PFLAG2_TEXT_ALIGNMENT_RESOLVED_MASK) >> 17;
    }

    public boolean resolveTextAlignment() {
        int parentResolvedTextAlignment;
        this.mPrivateFlags2 &= -983041;
        if (hasRtlSupport()) {
            int textAlignment = getRawTextAlignment();
            switch (textAlignment) {
                case 0:
                    if (!canResolveTextAlignment()) {
                        this.mPrivateFlags2 |= 131072;
                        return false;
                    }
                    try {
                        if (this.mParent.isTextAlignmentResolved()) {
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
                                    this.mPrivateFlags2 = 131072 | this.mPrivateFlags2;
                                    break;
                            }
                        } else {
                            this.mPrivateFlags2 = 131072 | this.mPrivateFlags2;
                            return false;
                        }
                    } catch (AbstractMethodError e2) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e2);
                        this.mPrivateFlags2 = this.mPrivateFlags2 | Menu.CATEGORY_SECONDARY;
                        return true;
                    }
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    this.mPrivateFlags2 |= textAlignment << 17;
                    break;
                default:
                    this.mPrivateFlags2 = 131072 | this.mPrivateFlags2;
                    break;
            }
        } else {
            this.mPrivateFlags2 |= 131072;
        }
        this.mPrivateFlags2 |= 65536;
        return true;
    }

    public boolean canResolveTextAlignment() {
        if (getRawTextAlignment() != 0) {
            return true;
        }
        if (this.mParent != null) {
            try {
                return this.mParent.canResolveTextAlignment();
            } catch (AbstractMethodError e) {
                Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
            }
        }
        return false;
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

    @ViewDebug.ExportedProperty
    public String getTransitionName() {
        return this.mTransitionName;
    }

    public void requestKeyboardShortcuts(List<KeyboardShortcutGroup> list, int deviceId) {
    }

    private void cancel(SendViewScrolledAccessibilityEvent callback) {
        if (callback != null && callback.mIsPending) {
            removeCallbacks(callback);
            callback.reset();
        }
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
                        for (int dumpFlag : values) {
                            dumpFlag(found, field.getName() + "[" + i + "]", dumpFlag);
                        }
                    }
                }
            }
            ArrayList<String> keys = Lists.newArrayList();
            keys.addAll(found.keySet());
            Collections.sort(keys);
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                Log.d(VIEW_LOG_TAG, found.get(it.next()));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void dumpFlag(HashMap<String, String> found, String name, int value) {
        String bits = String.format("%32s", new Object[]{Integer.toBinaryString(value)}).replace('0', ' ');
        int prefix = name.indexOf(95);
        StringBuilder sb = new StringBuilder();
        sb.append(prefix > 0 ? name.substring(0, prefix) : name);
        sb.append(bits);
        sb.append(name);
        String key = sb.toString();
        found.put(key, bits + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + name);
    }

    public void encode(ViewHierarchyEncoder stream) {
        stream.beginObject(this);
        encodeProperties(stream);
        stream.endObject();
    }

    /* access modifiers changed from: protected */
    public void encodeProperties(ViewHierarchyEncoder stream) {
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
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
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
        stream.addProperty("drawing:clipBounds", this.mClipBounds == null ? null : this.mClipBounds.toString());
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
        stream.addProperty("drawing:outlineAmbientShadowColor", getOutlineAmbientShadowColor());
        stream.addProperty("drawing:outlineSpotShadowColor", getOutlineSpotShadowColor());
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
        Resources.Theme theme = getContext().getTheme();
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
        stream.addProperty("accessibility:contentDescription", contentDescription == null ? "" : contentDescription.toString());
        stream.addProperty("accessibility:labelFor", getLabelFor());
        stream.addProperty("accessibility:importantForAccessibility", getImportantForAccessibility());
    }

    /* access modifiers changed from: package-private */
    public boolean shouldDrawRoundScrollbar() {
        boolean z = false;
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
        if (this.mAttachInfo.mTmpLocation[0] == insets.getStableInsetLeft() && this.mAttachInfo.mTmpLocation[1] == insets.getStableInsetTop()) {
            z = true;
        }
        return z;
    }

    public void setTooltipText(CharSequence tooltipText) {
        if (TextUtils.isEmpty(tooltipText)) {
            setFlags(0, 1073741824);
            hideTooltip();
            this.mTooltipInfo = null;
            return;
        }
        setFlags(1073741824, 1073741824);
        if (this.mTooltipInfo == null) {
            this.mTooltipInfo = new TooltipInfo();
            this.mTooltipInfo.mShowTooltipRunnable = new Runnable() {
                public final void run() {
                    boolean unused = View.this.showHoverTooltip();
                }
            };
            this.mTooltipInfo.mHideTooltipRunnable = new Runnable() {
                public final void run() {
                    View.this.hideTooltip();
                }
            };
            this.mTooltipInfo.mHoverSlop = ViewConfiguration.get(this.mContext).getScaledHoverSlop();
            this.mTooltipInfo.clearAnchorPos();
        }
        this.mTooltipInfo.mTooltipText = tooltipText;
    }

    public void setTooltip(CharSequence tooltipText) {
        setTooltipText(tooltipText);
    }

    public CharSequence getTooltipText() {
        if (this.mTooltipInfo != null) {
            return this.mTooltipInfo.mTooltipText;
        }
        return null;
    }

    public CharSequence getTooltip() {
        return getTooltipText();
    }

    private boolean showTooltip(int x, int y, boolean fromLongClick) {
        if (this.mAttachInfo == null || this.mTooltipInfo == null) {
            return false;
        }
        if ((fromLongClick && (this.mViewFlags & 32) != 0) || TextUtils.isEmpty(this.mTooltipInfo.mTooltipText)) {
            return false;
        }
        hideTooltip();
        this.mTooltipInfo.mTooltipFromLongClick = fromLongClick;
        this.mTooltipInfo.mTooltipPopup = new TooltipPopup(getContext());
        this.mTooltipInfo.mTooltipPopup.show(this, x, y, (this.mPrivateFlags3 & 131072) == 131072, this.mTooltipInfo.mTooltipText);
        this.mAttachInfo.mTooltipHost = this;
        notifyViewAccessibilityStateChangedIfNeeded(0);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void hideTooltip() {
        if (this.mTooltipInfo != null) {
            removeCallbacks(this.mTooltipInfo.mShowTooltipRunnable);
            if (this.mTooltipInfo.mTooltipPopup != null) {
                this.mTooltipInfo.mTooltipPopup.hide();
                this.mTooltipInfo.mTooltipPopup = null;
                this.mTooltipInfo.mTooltipFromLongClick = false;
                this.mTooltipInfo.clearAnchorPos();
                if (this.mAttachInfo != null) {
                    this.mAttachInfo.mTooltipHost = null;
                }
                notifyViewAccessibilityStateChangedIfNeeded(0);
            }
        }
    }

    private boolean showLongClickTooltip(int x, int y) {
        if (this.mTooltipInfo != null) {
            removeCallbacks(this.mTooltipInfo.mShowTooltipRunnable);
            removeCallbacks(this.mTooltipInfo.mHideTooltipRunnable);
        }
        return showTooltip(x, y, true);
    }

    /* access modifiers changed from: private */
    public boolean showHoverTooltip() {
        return showTooltip(this.mTooltipInfo.mAnchorX, this.mTooltipInfo.mAnchorY, false);
    }

    /* access modifiers changed from: package-private */
    public boolean dispatchTooltipHoverEvent(MotionEvent event) {
        int timeout;
        if (this.mTooltipInfo == null) {
            return false;
        }
        int action = event.getAction();
        if (action != 7) {
            if (action == 10) {
                this.mTooltipInfo.clearAnchorPos();
                if (!this.mTooltipInfo.mTooltipFromLongClick) {
                    hideTooltip();
                }
            }
        } else if ((this.mViewFlags & 1073741824) == 1073741824) {
            if (!this.mTooltipInfo.mTooltipFromLongClick && this.mTooltipInfo.updateAnchorPos(event)) {
                if (this.mTooltipInfo.mTooltipPopup == null) {
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
        return false;
    }

    /* access modifiers changed from: package-private */
    public void handleTooltipKey(KeyEvent event) {
        switch (event.getAction()) {
            case 0:
                if (event.getRepeatCount() == 0) {
                    hideTooltip();
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
        TypedValue val = new TypedValue();
        if (!attributes.getValue(19, val)) {
            return 16;
        }
        if (val.type != 18) {
            return val.data;
        }
        return val.data == 0 ? 0 : 1;
    }

    public View getTooltipView() {
        if (this.mTooltipInfo == null || this.mTooltipInfo.mTooltipPopup == null) {
            return null;
        }
        return this.mTooltipInfo.mTooltipPopup.getContentView();
    }

    public static boolean isDefaultFocusHighlightEnabled() {
        return sUseDefaultFocusHighlight;
    }

    /* access modifiers changed from: package-private */
    public View dispatchUnhandledKeyEvent(KeyEvent evt) {
        if (onUnhandledKeyEvent(evt)) {
            return this;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean onUnhandledKeyEvent(KeyEvent event) {
        if (!(this.mListenerInfo == null || this.mListenerInfo.mUnhandledKeyListeners == null)) {
            for (int i = this.mListenerInfo.mUnhandledKeyListeners.size() - 1; i >= 0; i--) {
                if (((OnUnhandledKeyEventListener) this.mListenerInfo.mUnhandledKeyListeners.get(i)).onUnhandledKeyEvent(this, event)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasUnhandledKeyListener() {
        return (this.mListenerInfo == null || this.mListenerInfo.mUnhandledKeyListeners == null || this.mListenerInfo.mUnhandledKeyListeners.isEmpty()) ? false : true;
    }

    public void addOnUnhandledKeyEventListener(OnUnhandledKeyEventListener listener) {
        ArrayList<OnUnhandledKeyEventListener> listeners = getListenerInfo().mUnhandledKeyListeners;
        if (listeners == null) {
            listeners = new ArrayList<>();
            ArrayList unused = getListenerInfo().mUnhandledKeyListeners = listeners;
        }
        listeners.add(listener);
        if (listeners.size() == 1 && (this.mParent instanceof ViewGroup)) {
            ((ViewGroup) this.mParent).incrementChildUnhandledKeyListeners();
        }
    }

    public void removeOnUnhandledKeyEventListener(OnUnhandledKeyEventListener listener) {
        if (this.mListenerInfo != null && this.mListenerInfo.mUnhandledKeyListeners != null && !this.mListenerInfo.mUnhandledKeyListeners.isEmpty()) {
            this.mListenerInfo.mUnhandledKeyListeners.remove(listener);
            if (this.mListenerInfo.mUnhandledKeyListeners.isEmpty()) {
                ArrayList unused = this.mListenerInfo.mUnhandledKeyListeners = null;
                if (this.mParent instanceof ViewGroup) {
                    ((ViewGroup) this.mParent).decrementChildUnhandledKeyListeners();
                }
            }
        }
    }

    public boolean hasAncestorThatBlocksDescendantFocusOuter() {
        return hasAncestorThatBlocksDescendantFocus();
    }

    public void setTouchInOtherThread(boolean touchable) {
        this.mCanTouchInOtherThread = touchable;
    }

    public boolean isTouchableInOtherThread() {
        return this.mCanTouchInOtherThread;
    }

    /* access modifiers changed from: protected */
    public Drawable initializeVariousScrollIndicators(Context context) {
        return context.getDrawable(17303351);
    }

    /* access modifiers changed from: protected */
    public boolean reSizeScrollIndicators(int[] xy) {
        return false;
    }

    public boolean isLongPressSwipe() {
        return false;
    }

    private boolean postAtFrontOfQueue(Runnable action) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo == null) {
            getRunQueue().post(action);
            return true;
        } else if (mLauncherList.contains(this.mContext.getPackageName())) {
            return attachInfo.mHandler.postAtFrontOfQueue(action);
        } else {
            return attachInfo.mHandler.post(action);
        }
    }
}
