package android.view;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.app.slice.Slice;
import android.common.HwFrameworkFactory;
import android.content.AutofillOptions;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
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
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManagerGlobal;
import android.iawareperf.IHwRtgSchedImpl;
import android.media.TtmlUtils;
import android.net.TrafficStats;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.CalendarContract;
import android.provider.Downloads;
import android.provider.SettingsStringUtil;
import android.provider.Telephony;
import android.provider.UserDictionary;
import android.rms.AppAssociate;
import android.sysprop.DisplayProperties;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.HwLogUtils;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.LongSparseLongArray;
import android.util.Pools;
import android.util.Property;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.StateSet;
import android.util.StatsLog;
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
import android.view.WindowInsetsAnimationListener;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityEventSource;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeIdManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.view.contentcapture.ContentCaptureManager;
import android.view.contentcapture.ContentCaptureSession;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inspector.InspectionCompanion;
import android.view.inspector.IntFlagMapping;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ScrollBarDrawable;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.view.TooltipPopup;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.widget.ScrollBarUtils;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.view.HwWindowManager;
import com.huawei.android.view.IHwShadowManager;
import java.lang.annotation.RCWeakRef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class View implements Drawable.Callback, KeyEvent.Callback, AccessibilityEventSource {
    public static final int ACCESSIBILITY_CURSOR_POSITION_UNDEFINED = -1;
    public static final int ACCESSIBILITY_LIVE_REGION_ASSERTIVE = 2;
    static final int ACCESSIBILITY_LIVE_REGION_DEFAULT = 0;
    public static final int ACCESSIBILITY_LIVE_REGION_NONE = 0;
    public static final int ACCESSIBILITY_LIVE_REGION_POLITE = 1;
    static final int ALL_RTL_PROPERTIES_RESOLVED = 1610678816;
    public static final Property<View, Float> ALPHA = new FloatProperty<View>(AppAssociate.ASSOC_WINDOW_ALPHA) {
        /* class android.view.View.AnonymousClass3 */

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
    private static final String AUTOFILL_LOG_TAG = "View.Autofill";
    public static final int AUTOFILL_TYPE_DATE = 4;
    public static final int AUTOFILL_TYPE_LIST = 3;
    public static final int AUTOFILL_TYPE_NONE = 0;
    public static final int AUTOFILL_TYPE_TEXT = 1;
    public static final int AUTOFILL_TYPE_TOGGLE = 2;
    private static final String CHARACTERISTICS = SystemProperties.get("ro.build.characteristics", PhoneConstants.APN_TYPE_DEFAULT);
    static final int CLICKABLE = 16384;
    private static final String CONTENT_CAPTURE_LOG_TAG = "View.ContentCapture";
    static final int CONTEXT_CLICKABLE = 8388608;
    @UnsupportedAppUsage
    private static final boolean DBG = false;
    private static final boolean DEBUG_CONTENT_CAPTURE = false;
    static final int DEBUG_CORNERS_COLOR = Color.rgb(63, 127, 255);
    static final int DEBUG_CORNERS_SIZE_DIP = 8;
    public static boolean DEBUG_DRAW = false;
    static final int DISABLED = 32;
    public static final int DRAG_ACCEPTABLE_CHECKED_NOT_INTERESTED_IN_DRAG = 255;
    public static final int DRAG_ACCEPTABLE_MIME_TYPE_ALL = 1;
    public static final int DRAG_ACCEPTABLE_MIME_TYPE_NONE = 3;
    public static final int DRAG_ACCEPTABLE_MIME_TYPE_PARTIAL = 2;
    public static final int DRAG_ACCEPTABLE_MIME_TYPE_UNKNOWN = 0;
    public static final int DRAG_FLAG_GLOBAL = 256;
    public static final int DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION = 64;
    public static final int DRAG_FLAG_GLOBAL_PREFIX_URI_PERMISSION = 128;
    public static final int DRAG_FLAG_GLOBAL_URI_READ = 1;
    public static final int DRAG_FLAG_GLOBAL_URI_WRITE = 2;
    public static final int DRAG_FLAG_HW_ALPHA = 4194304;
    @UnsupportedAppUsage
    public static final int DRAG_FLAG_HW_FINGER_DOWN = 536870912;
    @UnsupportedAppUsage
    public static final int DRAG_FLAG_HW_FREEFORM_OR_SPLITSCREEN_ANIM = 1073741824;
    public static final int DRAG_FLAG_HW_MULTI_WINDOW_BASE_ANIM = 1024;
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
    private static final String[] GESTURE_NAV_VIEW_TITLES = {"GestureNavLeft", "GestureNavRight", "GestureNavBottom"};
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
    private static final boolean IS_TV = ("tv".equals(CHARACTERISTICS) || "mobiletv".equals(CHARACTERISTICS));
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
    @UnsupportedAppUsage
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
    private static final int[] PFLAG2_TEXT_DIRECTION_FLAGS = {0, 64, 128, 192, 256, 320, 384, 448};
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
    static final int PFLAG_DIRTY_MASK = 2097152;
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
        /* class android.view.View.AnonymousClass10 */

        public void setValue(View object, float value) {
            object.setRotation(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getRotation());
        }
    };
    public static final Property<View, Float> ROTATION_X = new FloatProperty<View>("rotationX") {
        /* class android.view.View.AnonymousClass11 */

        public void setValue(View object, float value) {
            object.setRotationX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getRotationX());
        }
    };
    public static final Property<View, Float> ROTATION_Y = new FloatProperty<View>("rotationY") {
        /* class android.view.View.AnonymousClass12 */

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
        /* class android.view.View.AnonymousClass13 */

        public void setValue(View object, float value) {
            object.setScaleX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getScaleX());
        }
    };
    public static final Property<View, Float> SCALE_Y = new FloatProperty<View>("scaleY") {
        /* class android.view.View.AnonymousClass14 */

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
    private static final long SCROLLBAR_DELAYMILLIS = 50;
    public static final int SCROLLBAR_POSITION_DEFAULT = 0;
    public static final int SCROLLBAR_POSITION_LEFT = 1;
    public static final int SCROLLBAR_POSITION_RIGHT = 2;
    private static final float SCROLLBAR_REFRESHRATE = 20.0f;
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
    @UnsupportedAppUsage
    public static final int STATUS_BAR_DISABLE_BACK = 4194304;
    public static final int STATUS_BAR_DISABLE_CLOCK = 8388608;
    @UnsupportedAppUsage
    public static final int STATUS_BAR_DISABLE_EXPAND = 65536;
    @UnsupportedAppUsage
    public static final int STATUS_BAR_DISABLE_HOME = 2097152;
    public static final int STATUS_BAR_DISABLE_NOTIFICATION_ALERTS = 262144;
    public static final int STATUS_BAR_DISABLE_NOTIFICATION_ICONS = 131072;
    public static final int STATUS_BAR_DISABLE_NOTIFICATION_TICKER = 524288;
    @UnsupportedAppUsage
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
        /* class android.view.View.AnonymousClass4 */

        public void setValue(View object, float value) {
            object.setTranslationX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getTranslationX());
        }
    };
    public static final Property<View, Float> TRANSLATION_Y = new FloatProperty<View>("translationY") {
        /* class android.view.View.AnonymousClass5 */

        public void setValue(View object, float value) {
            object.setTranslationY(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getTranslationY());
        }
    };
    public static final Property<View, Float> TRANSLATION_Z = new FloatProperty<View>("translationZ") {
        /* class android.view.View.AnonymousClass6 */

        public void setValue(View object, float value) {
            object.setTranslationZ(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getTranslationZ());
        }
    };
    private static final int UNDEFINED_PADDING = Integer.MIN_VALUE;
    protected static final String VIEW_LOG_TAG = "View";
    protected static final int VIEW_STRUCTURE_FOR_ASSIST = 0;
    protected static final int VIEW_STRUCTURE_FOR_AUTOFILL = 1;
    protected static final int VIEW_STRUCTURE_FOR_CONTENT_CAPTURE = 2;
    private static final int[] VISIBILITY_FLAGS = {0, 4, 8};
    static final int VISIBILITY_MASK = 12;
    public static final int VISIBLE = 0;
    static final int WILL_NOT_CACHE_DRAWING = 131072;
    static final int WILL_NOT_DRAW = 128;
    protected static final int[] WINDOW_FOCUSED_STATE_SET = StateSet.get(1);
    public static final Property<View, Float> X = new FloatProperty<View>("x") {
        /* class android.view.View.AnonymousClass7 */

        public void setValue(View object, float value) {
            object.setX(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getX());
        }
    };
    public static final Property<View, Float> Y = new FloatProperty<View>("y") {
        /* class android.view.View.AnonymousClass8 */

        public void setValue(View object, float value) {
            object.setY(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getY());
        }
    };
    public static final Property<View, Float> Z = new FloatProperty<View>("z") {
        /* class android.view.View.AnonymousClass9 */

        public void setValue(View object, float value) {
            object.setZ(value);
        }

        public Float get(View object) {
            return Float.valueOf(object.getZ());
        }
    };
    private static SparseArray<String> mAttributeMap;
    private static boolean sAcceptZeroSizeDragShadow;
    private static boolean sAlwaysAssignFocus;
    private static boolean sAlwaysRemeasureExactly = false;
    private static boolean sAutoFocusableOffUIThreadWontNotifyParents;
    static boolean sBrokenInsetsDispatch;
    protected static boolean sBrokenWindowBackground;
    private static boolean sCanFocusZeroSized;
    static boolean sCascadedDragDrop;
    private static boolean sCompatibilityDone = false;
    private static Paint sDebugPaint;
    public static boolean sDebugViewAttributes = false;
    public static String sDebugViewAttributesApplicationPackage;
    static boolean sHasFocusableExcludeAutoFocusable;
    private static boolean sIgnoreMeasureCache = false;
    private static int sNextAccessibilityViewId;
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    protected static boolean sPreserveMarginParamsInLayoutParamConversion;
    static boolean sTextureViewIgnoresDrawableSetters = false;
    static final ThreadLocal<Rect> sThreadLocal = new ThreadLocal<>();
    private static boolean sThrowOnInvalidFloatProperties;
    private static boolean sUseBrokenMakeMeasureSpec = false;
    private static boolean sUseDefaultFocusHighlight;
    static boolean sUseZeroUnspecifiedMeasureSpec = false;
    private boolean isAndroidForceDarkState;
    private boolean isHwForceDarkState;
    private int mAccessibilityCursorPosition;
    @UnsupportedAppUsage
    AccessibilityDelegate mAccessibilityDelegate;
    private CharSequence mAccessibilityPaneTitle;
    private int mAccessibilityTraversalAfterId;
    private int mAccessibilityTraversalBeforeId;
    @UnsupportedAppUsage
    private int mAccessibilityViewId;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private ViewPropertyAnimator mAnimator;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    AttachInfo mAttachInfo;
    private SparseArray<int[]> mAttributeResolutionStacks;
    private SparseIntArray mAttributeSourceResId;
    @ViewDebug.ExportedProperty(category = "attributes", hasAdjacentMapping = true)
    public String[] mAttributes;
    private String[] mAutofillHints;
    private AutofillId mAutofillId;
    private int mAutofillViewId;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "bg_")
    private Drawable mBackground;
    private RenderNode mBackgroundRenderNode;
    @UnsupportedAppUsage
    private int mBackgroundResource;
    private boolean mBackgroundSizeChanged;
    private TintInfo mBackgroundTint;
    private boolean mBlurEnabled;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    protected int mBottom;
    private ContentCaptureSession mCachedContentCaptureSession;
    @UnsupportedAppUsage
    public boolean mCachingFailed;
    private boolean mCanTouchInOtherThread;
    @ViewDebug.ExportedProperty(category = "drawing")
    Rect mClipBounds;
    private ContentCaptureSession mContentCaptureSession;
    private CharSequence mContentDescription;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(deepExport = true)
    protected Context mContext;
    protected Animation mCurrentAnimation;
    private final Rect mCurrentDirtyRect;
    private Drawable mDefaultFocusHighlight;
    private Drawable mDefaultFocusHighlightCache;
    boolean mDefaultFocusHighlightEnabled;
    private boolean mDefaultFocusHighlightSizeChanged;
    private int[] mDrawableState;
    @UnsupportedAppUsage
    private Bitmap mDrawingCache;
    private int mDrawingCacheBackgroundColor;
    private int mExplicitStyle;
    private ViewTreeObserver mFloatingTreeObserver;
    private boolean mForceRTL;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "fg_")
    private ForegroundInfo mForegroundInfo;
    private ArrayList<FrameMetricsObserver> mFrameMetricsObservers;
    GhostView mGhostView;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private boolean mHasPerformedLongPress;
    private boolean mHoveringTouchDelegate;
    private IHwShadowManager mHwShadowManager;
    @ViewDebug.ExportedProperty(resolveId = true)
    int mID;
    private boolean mIgnoreNextUpEvent;
    private boolean mInContextButtonPress;
    protected final InputEventConsistencyVerifier mInputEventConsistencyVerifier;
    private boolean mIsNeedDetect;
    @UnsupportedAppUsage
    private SparseArray<Object> mKeyedTags;
    private int mLabelForId;
    private boolean mLastIsOpaque;
    Paint mLayerPaint;
    @ViewDebug.ExportedProperty(category = "drawing", mapping = {@ViewDebug.IntToString(from = 0, to = "NONE"), @ViewDebug.IntToString(from = 1, to = "SOFTWARE"), @ViewDebug.IntToString(from = 2, to = "HARDWARE")})
    int mLayerType;
    private Insets mLayoutInsets;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    protected ViewGroup.LayoutParams mLayoutParams;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    protected int mLeft;
    private boolean mLeftPaddingDefined;
    @UnsupportedAppUsage
    ListenerInfo mListenerInfo;
    private float mLongClickX;
    private float mLongClickY;
    private MatchIdPredicate mMatchIdPredicate;
    private MatchLabelForPredicate mMatchLabelForPredicate;
    private LongSparseLongArray mMeasureCache;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "measurement")
    int mMeasuredHeight;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "measurement")
    int mMeasuredWidth;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    @ViewDebug.ExportedProperty(category = "measurement")
    private int mMinHeight;
    @UnsupportedAppUsage(maxTargetSdk = 28)
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
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingBottom;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingLeft;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingRight;
    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = "padding")
    protected int mPaddingTop;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    @RCWeakRef
    protected ViewParent mParent;
    private CheckForLongPress mPendingCheckForLongPress;
    @UnsupportedAppUsage
    private CheckForTap mPendingCheckForTap;
    private PerformClick mPerformClick;
    private PointerIcon mPointerIcon;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769414)
    @ViewDebug.ExportedProperty(flagMapping = {@ViewDebug.FlagToString(equals = 4096, mask = 4096, name = "FORCE_LAYOUT"), @ViewDebug.FlagToString(equals = 8192, mask = 8192, name = "LAYOUT_REQUIRED"), @ViewDebug.FlagToString(equals = 32768, mask = 32768, name = "DRAWING_CACHE_INVALID", outputIf = false), @ViewDebug.FlagToString(equals = 32, mask = 32, name = "DRAWN", outputIf = true), @ViewDebug.FlagToString(equals = 32, mask = 32, name = "NOT_DRAWN", outputIf = false), @ViewDebug.FlagToString(equals = 2097152, mask = 2097152, name = "DIRTY")}, formatToHexString = true)
    public int mPrivateFlags;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123768943)
    int mPrivateFlags2;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 129147060)
    int mPrivateFlags3;
    @UnsupportedAppUsage
    boolean mRecreateDisplayList;
    @UnsupportedAppUsage
    final RenderNode mRenderNode;
    @UnsupportedAppUsage
    private final Resources mResources;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    protected int mRight;
    private boolean mRightPaddingDefined;
    private RoundScrollbarRenderer mRoundScrollbarRenderer;
    private int mRoundX;
    private int mRoundY;
    private HandlerActionQueue mRunQueue;
    @UnsupportedAppUsage
    private ScrollabilityCache mScrollCache;
    private Drawable mScrollIndicatorDrawable;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    @ViewDebug.ExportedProperty(category = "scrolling")
    protected int mScrollX;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    @ViewDebug.ExportedProperty(category = "scrolling")
    protected int mScrollY;
    private SendViewScrolledAccessibilityEvent mSendViewScrolledAccessibilityEvent;
    private boolean mSendingHoverAccessibilityEvents;
    private int mSourceLayoutId;
    @UnsupportedAppUsage
    String mStartActivityRequestWho;
    private StateListAnimator mStateListAnimator;
    @ViewDebug.ExportedProperty(flagMapping = {@ViewDebug.FlagToString(equals = 1, mask = 1, name = "LOW_PROFILE"), @ViewDebug.FlagToString(equals = 2, mask = 2, name = "HIDE_NAVIGATION"), @ViewDebug.FlagToString(equals = 4, mask = 4, name = "FULLSCREEN"), @ViewDebug.FlagToString(equals = 256, mask = 256, name = "LAYOUT_STABLE"), @ViewDebug.FlagToString(equals = 512, mask = 512, name = "LAYOUT_HIDE_NAVIGATION"), @ViewDebug.FlagToString(equals = 1024, mask = 1024, name = "LAYOUT_FULLSCREEN"), @ViewDebug.FlagToString(equals = 2048, mask = 2048, name = "IMMERSIVE"), @ViewDebug.FlagToString(equals = 4096, mask = 4096, name = "IMMERSIVE_STICKY"), @ViewDebug.FlagToString(equals = 8192, mask = 8192, name = "LIGHT_STATUS_BAR"), @ViewDebug.FlagToString(equals = 16, mask = 16, name = "LIGHT_NAVIGATION_BAR"), @ViewDebug.FlagToString(equals = 65536, mask = 65536, name = "STATUS_BAR_DISABLE_EXPAND"), @ViewDebug.FlagToString(equals = 131072, mask = 131072, name = "STATUS_BAR_DISABLE_NOTIFICATION_ICONS"), @ViewDebug.FlagToString(equals = 262144, mask = 262144, name = "STATUS_BAR_DISABLE_NOTIFICATION_ALERTS"), @ViewDebug.FlagToString(equals = 524288, mask = 524288, name = "STATUS_BAR_DISABLE_NOTIFICATION_TICKER"), @ViewDebug.FlagToString(equals = 1048576, mask = 1048576, name = "STATUS_BAR_DISABLE_SYSTEM_INFO"), @ViewDebug.FlagToString(equals = 2097152, mask = 2097152, name = "STATUS_BAR_DISABLE_HOME"), @ViewDebug.FlagToString(equals = 4194304, mask = 4194304, name = "STATUS_BAR_DISABLE_BACK"), @ViewDebug.FlagToString(equals = 8388608, mask = 8388608, name = "STATUS_BAR_DISABLE_CLOCK"), @ViewDebug.FlagToString(equals = 16777216, mask = 16777216, name = "STATUS_BAR_DISABLE_RECENT"), @ViewDebug.FlagToString(equals = 33554432, mask = 33554432, name = "STATUS_BAR_DISABLE_SEARCH"), @ViewDebug.FlagToString(equals = 67108864, mask = 67108864, name = "STATUS_BAR_TRANSIENT"), @ViewDebug.FlagToString(equals = 134217728, mask = 134217728, name = "NAVIGATION_BAR_TRANSIENT"), @ViewDebug.FlagToString(equals = 268435456, mask = 268435456, name = "STATUS_BAR_UNHIDE"), @ViewDebug.FlagToString(equals = 536870912, mask = 536870912, name = "NAVIGATION_BAR_UNHIDE"), @ViewDebug.FlagToString(equals = 1073741824, mask = 1073741824, name = "STATUS_BAR_TRANSLUCENT"), @ViewDebug.FlagToString(equals = Integer.MIN_VALUE, mask = Integer.MIN_VALUE, name = "NAVIGATION_BAR_TRANSLUCENT"), @ViewDebug.FlagToString(equals = 32768, mask = 32768, name = "NAVIGATION_BAR_TRANSPARENT"), @ViewDebug.FlagToString(equals = 8, mask = 8, name = "STATUS_BAR_TRANSPARENT")}, formatToHexString = true)
    int mSystemUiVisibility;
    @UnsupportedAppUsage
    protected Object mTag;
    private int[] mTempNestedScrollConsumed;
    TooltipInfo mTooltipInfo;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    protected int mTop;
    private TouchDelegate mTouchDelegate;
    private int mTouchSlop;
    @UnsupportedAppUsage
    public TransformationInfo mTransformationInfo;
    int mTransientStateCount;
    private String mTransitionName;
    @UnsupportedAppUsage
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
    @UnsupportedAppUsage
    private int mVerticalScrollbarPosition;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    @ViewDebug.ExportedProperty(formatToHexString = true)
    int mViewFlags;
    private Handler mVisibilityChangeForAutofillHandler;
    int mWindowAttachCount;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AutofillFlags {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AutofillImportance {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AutofillType {
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface LayerType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface LayoutDir {
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface ResolvedLayoutDir {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScrollBarStyle {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScrollIndicators {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TextAlignment {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewStructureType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {
    }

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<View> {
        private int mAccessibilityFocusedId;
        private int mAccessibilityHeadingId;
        private int mAccessibilityLiveRegionId;
        private int mAccessibilityPaneTitleId;
        private int mAccessibilityTraversalAfterId;
        private int mAccessibilityTraversalBeforeId;
        private int mActivatedId;
        private int mAlphaId;
        private int mAutofillHintsId;
        private int mBackgroundId;
        private int mBackgroundTintId;
        private int mBackgroundTintModeId;
        private int mBaselineId;
        private int mClickableId;
        private int mContentDescriptionId;
        private int mContextClickableId;
        private int mDefaultFocusHighlightEnabledId;
        private int mDrawingCacheQualityId;
        private int mDuplicateParentStateId;
        private int mElevationId;
        private int mEnabledId;
        private int mFadingEdgeLengthId;
        private int mFilterTouchesWhenObscuredId;
        private int mFitsSystemWindowsId;
        private int mFocusableId;
        private int mFocusableInTouchModeId;
        private int mFocusedByDefaultId;
        private int mFocusedId;
        private int mForceDarkAllowedId;
        private int mForegroundGravityId;
        private int mForegroundId;
        private int mForegroundTintId;
        private int mForegroundTintModeId;
        private int mHapticFeedbackEnabledId;
        private int mIdId;
        private int mImportantForAccessibilityId;
        private int mImportantForAutofillId;
        private int mIsScrollContainerId;
        private int mKeepScreenOnId;
        private int mKeyboardNavigationClusterId;
        private int mLabelForId;
        private int mLayerTypeId;
        private int mLayoutDirectionId;
        private int mLongClickableId;
        private int mMinHeightId;
        private int mMinWidthId;
        private int mNestedScrollingEnabledId;
        private int mNextClusterForwardId;
        private int mNextFocusDownId;
        private int mNextFocusForwardId;
        private int mNextFocusLeftId;
        private int mNextFocusRightId;
        private int mNextFocusUpId;
        private int mOutlineAmbientShadowColorId;
        private int mOutlineProviderId;
        private int mOutlineSpotShadowColorId;
        private int mOverScrollModeId;
        private int mPaddingBottomId;
        private int mPaddingLeftId;
        private int mPaddingRightId;
        private int mPaddingTopId;
        private int mPointerIconId;
        private int mPressedId;
        private boolean mPropertiesMapped = false;
        private int mRawLayoutDirectionId;
        private int mRawTextAlignmentId;
        private int mRawTextDirectionId;
        private int mRequiresFadingEdgeId;
        private int mRotationId;
        private int mRotationXId;
        private int mRotationYId;
        private int mSaveEnabledId;
        private int mScaleXId;
        private int mScaleYId;
        private int mScreenReaderFocusableId;
        private int mScrollIndicatorsId;
        private int mScrollXId;
        private int mScrollYId;
        private int mScrollbarDefaultDelayBeforeFadeId;
        private int mScrollbarFadeDurationId;
        private int mScrollbarSizeId;
        private int mScrollbarStyleId;
        private int mSelectedId;
        private int mSolidColorId;
        private int mSoundEffectsEnabledId;
        private int mStateListAnimatorId;
        private int mTagId;
        private int mTextAlignmentId;
        private int mTextDirectionId;
        private int mTooltipTextId;
        private int mTransformPivotXId;
        private int mTransformPivotYId;
        private int mTransitionNameId;
        private int mTranslationXId;
        private int mTranslationYId;
        private int mTranslationZId;
        private int mVisibilityId;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mAccessibilityFocusedId = propertyMapper.mapBoolean("accessibilityFocused", 0);
            this.mAccessibilityHeadingId = propertyMapper.mapBoolean("accessibilityHeading", 16844160);
            SparseArray<String> accessibilityLiveRegionEnumMapping = new SparseArray<>();
            accessibilityLiveRegionEnumMapping.put(0, "none");
            accessibilityLiveRegionEnumMapping.put(1, "polite");
            accessibilityLiveRegionEnumMapping.put(2, "assertive");
            Objects.requireNonNull(accessibilityLiveRegionEnumMapping);
            this.mAccessibilityLiveRegionId = propertyMapper.mapIntEnum("accessibilityLiveRegion", 16843758, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mAccessibilityPaneTitleId = propertyMapper.mapObject("accessibilityPaneTitle", 16844156);
            this.mAccessibilityTraversalAfterId = propertyMapper.mapResourceId("accessibilityTraversalAfter", 16843986);
            this.mAccessibilityTraversalBeforeId = propertyMapper.mapResourceId("accessibilityTraversalBefore", 16843985);
            this.mActivatedId = propertyMapper.mapBoolean("activated", 0);
            this.mAlphaId = propertyMapper.mapFloat(AppAssociate.ASSOC_WINDOW_ALPHA, 16843551);
            this.mAutofillHintsId = propertyMapper.mapObject("autofillHints", 16844118);
            this.mBackgroundId = propertyMapper.mapObject("background", 16842964);
            this.mBackgroundTintId = propertyMapper.mapObject("backgroundTint", 16843883);
            this.mBackgroundTintModeId = propertyMapper.mapObject("backgroundTintMode", 16843884);
            this.mBaselineId = propertyMapper.mapInt("baseline", 16843548);
            this.mClickableId = propertyMapper.mapBoolean("clickable", 16842981);
            this.mContentDescriptionId = propertyMapper.mapObject("contentDescription", 16843379);
            this.mContextClickableId = propertyMapper.mapBoolean("contextClickable", 16844007);
            this.mDefaultFocusHighlightEnabledId = propertyMapper.mapBoolean("defaultFocusHighlightEnabled", 16844130);
            SparseArray<String> drawingCacheQualityEnumMapping = new SparseArray<>();
            drawingCacheQualityEnumMapping.put(0, "auto");
            drawingCacheQualityEnumMapping.put(524288, "low");
            drawingCacheQualityEnumMapping.put(1048576, "high");
            Objects.requireNonNull(drawingCacheQualityEnumMapping);
            this.mDrawingCacheQualityId = propertyMapper.mapIntEnum("drawingCacheQuality", 16842984, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mDuplicateParentStateId = propertyMapper.mapBoolean("duplicateParentState", 16842985);
            this.mElevationId = propertyMapper.mapFloat("elevation", 16843840);
            this.mEnabledId = propertyMapper.mapBoolean("enabled", 16842766);
            this.mFadingEdgeLengthId = propertyMapper.mapInt("fadingEdgeLength", 16842976);
            this.mFilterTouchesWhenObscuredId = propertyMapper.mapBoolean("filterTouchesWhenObscured", 16843460);
            this.mFitsSystemWindowsId = propertyMapper.mapBoolean("fitsSystemWindows", 16842973);
            SparseArray<String> focusableEnumMapping = new SparseArray<>();
            focusableEnumMapping.put(0, "false");
            focusableEnumMapping.put(1, "true");
            focusableEnumMapping.put(16, "auto");
            Objects.requireNonNull(focusableEnumMapping);
            this.mFocusableId = propertyMapper.mapIntEnum("focusable", 16842970, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mFocusableInTouchModeId = propertyMapper.mapBoolean("focusableInTouchMode", 16842971);
            this.mFocusedId = propertyMapper.mapBoolean("focused", 0);
            this.mFocusedByDefaultId = propertyMapper.mapBoolean("focusedByDefault", 16844100);
            this.mForceDarkAllowedId = propertyMapper.mapBoolean("forceDarkAllowed", 16844172);
            this.mForegroundId = propertyMapper.mapObject("foreground", 16843017);
            this.mForegroundGravityId = propertyMapper.mapGravity("foregroundGravity", 16843264);
            this.mForegroundTintId = propertyMapper.mapObject("foregroundTint", 16843885);
            this.mForegroundTintModeId = propertyMapper.mapObject("foregroundTintMode", 16843886);
            this.mHapticFeedbackEnabledId = propertyMapper.mapBoolean("hapticFeedbackEnabled", 16843358);
            this.mIdId = propertyMapper.mapResourceId("id", 16842960);
            SparseArray<String> importantForAccessibilityEnumMapping = new SparseArray<>();
            importantForAccessibilityEnumMapping.put(0, "auto");
            importantForAccessibilityEnumMapping.put(1, "yes");
            importantForAccessibilityEnumMapping.put(2, "no");
            importantForAccessibilityEnumMapping.put(4, "noHideDescendants");
            Objects.requireNonNull(importantForAccessibilityEnumMapping);
            this.mImportantForAccessibilityId = propertyMapper.mapIntEnum("importantForAccessibility", 16843690, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            SparseArray<String> importantForAutofillEnumMapping = new SparseArray<>();
            importantForAutofillEnumMapping.put(0, "auto");
            importantForAutofillEnumMapping.put(1, "yes");
            importantForAutofillEnumMapping.put(2, "no");
            importantForAutofillEnumMapping.put(4, "yesExcludeDescendants");
            importantForAutofillEnumMapping.put(8, "noExcludeDescendants");
            Objects.requireNonNull(importantForAutofillEnumMapping);
            this.mImportantForAutofillId = propertyMapper.mapIntEnum("importantForAutofill", 16844120, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mIsScrollContainerId = propertyMapper.mapBoolean("isScrollContainer", 16843342);
            this.mKeepScreenOnId = propertyMapper.mapBoolean("keepScreenOn", 16843286);
            this.mKeyboardNavigationClusterId = propertyMapper.mapBoolean("keyboardNavigationCluster", 16844096);
            this.mLabelForId = propertyMapper.mapResourceId("labelFor", 16843718);
            SparseArray<String> layerTypeEnumMapping = new SparseArray<>();
            layerTypeEnumMapping.put(0, "none");
            layerTypeEnumMapping.put(1, "software");
            layerTypeEnumMapping.put(2, "hardware");
            Objects.requireNonNull(layerTypeEnumMapping);
            this.mLayerTypeId = propertyMapper.mapIntEnum("layerType", 16843604, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            SparseArray<String> layoutDirectionEnumMapping = new SparseArray<>();
            layoutDirectionEnumMapping.put(0, "ltr");
            layoutDirectionEnumMapping.put(1, "rtl");
            Objects.requireNonNull(layoutDirectionEnumMapping);
            this.mLayoutDirectionId = propertyMapper.mapIntEnum("layoutDirection", 16843698, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mLongClickableId = propertyMapper.mapBoolean("longClickable", 16842982);
            this.mMinHeightId = propertyMapper.mapInt("minHeight", 16843072);
            this.mMinWidthId = propertyMapper.mapInt("minWidth", 16843071);
            this.mNestedScrollingEnabledId = propertyMapper.mapBoolean("nestedScrollingEnabled", 16843830);
            this.mNextClusterForwardId = propertyMapper.mapResourceId("nextClusterForward", 16844098);
            this.mNextFocusDownId = propertyMapper.mapResourceId("nextFocusDown", 16842980);
            this.mNextFocusForwardId = propertyMapper.mapResourceId("nextFocusForward", 16843580);
            this.mNextFocusLeftId = propertyMapper.mapResourceId("nextFocusLeft", 16842977);
            this.mNextFocusRightId = propertyMapper.mapResourceId("nextFocusRight", 16842978);
            this.mNextFocusUpId = propertyMapper.mapResourceId("nextFocusUp", 16842979);
            this.mOutlineAmbientShadowColorId = propertyMapper.mapColor("outlineAmbientShadowColor", 16844162);
            this.mOutlineProviderId = propertyMapper.mapObject("outlineProvider", 16843960);
            this.mOutlineSpotShadowColorId = propertyMapper.mapColor("outlineSpotShadowColor", 16844161);
            SparseArray<String> overScrollModeEnumMapping = new SparseArray<>();
            overScrollModeEnumMapping.put(0, "always");
            overScrollModeEnumMapping.put(1, "ifContentScrolls");
            overScrollModeEnumMapping.put(2, "never");
            Objects.requireNonNull(overScrollModeEnumMapping);
            this.mOverScrollModeId = propertyMapper.mapIntEnum("overScrollMode", 16843457, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mPaddingBottomId = propertyMapper.mapInt("paddingBottom", 16842969);
            this.mPaddingLeftId = propertyMapper.mapInt("paddingLeft", 16842966);
            this.mPaddingRightId = propertyMapper.mapInt("paddingRight", 16842968);
            this.mPaddingTopId = propertyMapper.mapInt("paddingTop", 16842967);
            this.mPointerIconId = propertyMapper.mapObject("pointerIcon", 16844041);
            this.mPressedId = propertyMapper.mapBoolean("pressed", 0);
            SparseArray<String> rawLayoutDirectionEnumMapping = new SparseArray<>();
            rawLayoutDirectionEnumMapping.put(0, "ltr");
            rawLayoutDirectionEnumMapping.put(1, "rtl");
            rawLayoutDirectionEnumMapping.put(2, "inherit");
            rawLayoutDirectionEnumMapping.put(3, UserDictionary.Words.LOCALE);
            Objects.requireNonNull(rawLayoutDirectionEnumMapping);
            this.mRawLayoutDirectionId = propertyMapper.mapIntEnum("rawLayoutDirection", 0, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            SparseArray<String> rawTextAlignmentEnumMapping = new SparseArray<>();
            rawTextAlignmentEnumMapping.put(0, "inherit");
            rawTextAlignmentEnumMapping.put(1, "gravity");
            rawTextAlignmentEnumMapping.put(2, "textStart");
            rawTextAlignmentEnumMapping.put(3, "textEnd");
            rawTextAlignmentEnumMapping.put(4, "center");
            rawTextAlignmentEnumMapping.put(5, "viewStart");
            rawTextAlignmentEnumMapping.put(6, "viewEnd");
            Objects.requireNonNull(rawTextAlignmentEnumMapping);
            this.mRawTextAlignmentId = propertyMapper.mapIntEnum("rawTextAlignment", 0, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            SparseArray<String> rawTextDirectionEnumMapping = new SparseArray<>();
            rawTextDirectionEnumMapping.put(0, "inherit");
            rawTextDirectionEnumMapping.put(1, "firstStrong");
            rawTextDirectionEnumMapping.put(2, "anyRtl");
            rawTextDirectionEnumMapping.put(3, "ltr");
            rawTextDirectionEnumMapping.put(4, "rtl");
            rawTextDirectionEnumMapping.put(5, UserDictionary.Words.LOCALE);
            rawTextDirectionEnumMapping.put(6, "firstStrongLtr");
            rawTextDirectionEnumMapping.put(7, "firstStrongRtl");
            Objects.requireNonNull(rawTextDirectionEnumMapping);
            this.mRawTextDirectionId = propertyMapper.mapIntEnum("rawTextDirection", 0, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            IntFlagMapping requiresFadingEdgeFlagMapping = new IntFlagMapping();
            requiresFadingEdgeFlagMapping.add(4096, 4096, Slice.HINT_HORIZONTAL);
            requiresFadingEdgeFlagMapping.add(12288, 0, "none");
            requiresFadingEdgeFlagMapping.add(8192, 8192, "vertical");
            Objects.requireNonNull(requiresFadingEdgeFlagMapping);
            this.mRequiresFadingEdgeId = propertyMapper.mapIntFlag("requiresFadingEdge", 16843685, new IntFunction() {
                /* class android.view.$$Lambda$gFNlJIKfxqleu304aRWP5R5v1yY */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return IntFlagMapping.this.get(i);
                }
            });
            this.mRotationId = propertyMapper.mapFloat("rotation", 16843558);
            this.mRotationXId = propertyMapper.mapFloat("rotationX", 16843559);
            this.mRotationYId = propertyMapper.mapFloat("rotationY", 16843560);
            this.mSaveEnabledId = propertyMapper.mapBoolean("saveEnabled", 16842983);
            this.mScaleXId = propertyMapper.mapFloat("scaleX", 16843556);
            this.mScaleYId = propertyMapper.mapFloat("scaleY", 16843557);
            this.mScreenReaderFocusableId = propertyMapper.mapBoolean("screenReaderFocusable", 16844148);
            IntFlagMapping scrollIndicatorsFlagMapping = new IntFlagMapping();
            scrollIndicatorsFlagMapping.add(2, 2, "bottom");
            scrollIndicatorsFlagMapping.add(32, 32, "end");
            scrollIndicatorsFlagMapping.add(4, 4, "left");
            scrollIndicatorsFlagMapping.add(-1, 0, "none");
            scrollIndicatorsFlagMapping.add(8, 8, "right");
            scrollIndicatorsFlagMapping.add(16, 16, Telephony.BaseMmsColumns.START);
            scrollIndicatorsFlagMapping.add(1, 1, "top");
            Objects.requireNonNull(scrollIndicatorsFlagMapping);
            this.mScrollIndicatorsId = propertyMapper.mapIntFlag("scrollIndicators", 16844006, new IntFunction() {
                /* class android.view.$$Lambda$gFNlJIKfxqleu304aRWP5R5v1yY */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return IntFlagMapping.this.get(i);
                }
            });
            this.mScrollXId = propertyMapper.mapInt("scrollX", 16842962);
            this.mScrollYId = propertyMapper.mapInt("scrollY", 16842963);
            this.mScrollbarDefaultDelayBeforeFadeId = propertyMapper.mapInt("scrollbarDefaultDelayBeforeFade", 16843433);
            this.mScrollbarFadeDurationId = propertyMapper.mapInt("scrollbarFadeDuration", 16843432);
            this.mScrollbarSizeId = propertyMapper.mapInt("scrollbarSize", 16842851);
            SparseArray<String> scrollbarStyleEnumMapping = new SparseArray<>();
            scrollbarStyleEnumMapping.put(0, "insideOverlay");
            scrollbarStyleEnumMapping.put(16777216, "insideInset");
            scrollbarStyleEnumMapping.put(33554432, "outsideOverlay");
            scrollbarStyleEnumMapping.put(50331648, "outsideInset");
            Objects.requireNonNull(scrollbarStyleEnumMapping);
            this.mScrollbarStyleId = propertyMapper.mapIntEnum("scrollbarStyle", 16842879, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mSelectedId = propertyMapper.mapBoolean(Slice.HINT_SELECTED, 0);
            this.mSolidColorId = propertyMapper.mapColor("solidColor", 16843594);
            this.mSoundEffectsEnabledId = propertyMapper.mapBoolean("soundEffectsEnabled", 16843285);
            this.mStateListAnimatorId = propertyMapper.mapObject("stateListAnimator", 16843848);
            this.mTagId = propertyMapper.mapObject(DropBoxManager.EXTRA_TAG, 16842961);
            SparseArray<String> textAlignmentEnumMapping = new SparseArray<>();
            textAlignmentEnumMapping.put(1, "gravity");
            textAlignmentEnumMapping.put(2, "textStart");
            textAlignmentEnumMapping.put(3, "textEnd");
            textAlignmentEnumMapping.put(4, "center");
            textAlignmentEnumMapping.put(5, "viewStart");
            textAlignmentEnumMapping.put(6, "viewEnd");
            Objects.requireNonNull(textAlignmentEnumMapping);
            this.mTextAlignmentId = propertyMapper.mapIntEnum("textAlignment", 16843697, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            SparseArray<String> textDirectionEnumMapping = new SparseArray<>();
            textDirectionEnumMapping.put(1, "firstStrong");
            textDirectionEnumMapping.put(2, "anyRtl");
            textDirectionEnumMapping.put(3, "ltr");
            textDirectionEnumMapping.put(4, "rtl");
            textDirectionEnumMapping.put(5, UserDictionary.Words.LOCALE);
            textDirectionEnumMapping.put(6, "firstStrongLtr");
            textDirectionEnumMapping.put(7, "firstStrongRtl");
            Objects.requireNonNull(textDirectionEnumMapping);
            this.mTextDirectionId = propertyMapper.mapIntEnum("textDirection", 0, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mTooltipTextId = propertyMapper.mapObject("tooltipText", 16844084);
            this.mTransformPivotXId = propertyMapper.mapFloat("transformPivotX", 16843552);
            this.mTransformPivotYId = propertyMapper.mapFloat("transformPivotY", 16843553);
            this.mTransitionNameId = propertyMapper.mapObject("transitionName", 16843776);
            this.mTranslationXId = propertyMapper.mapFloat("translationX", 16843554);
            this.mTranslationYId = propertyMapper.mapFloat("translationY", 16843555);
            this.mTranslationZId = propertyMapper.mapFloat("translationZ", 16843770);
            SparseArray<String> visibilityEnumMapping = new SparseArray<>();
            visibilityEnumMapping.put(0, CalendarContract.CalendarColumns.VISIBLE);
            visibilityEnumMapping.put(4, "invisible");
            visibilityEnumMapping.put(8, "gone");
            Objects.requireNonNull(visibilityEnumMapping);
            this.mVisibilityId = propertyMapper.mapIntEnum(Downloads.Impl.COLUMN_VISIBILITY, 16842972, new IntFunction() {
                /* class android.view.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mPropertiesMapped = true;
        }

        public void readProperties(View node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readBoolean(this.mAccessibilityFocusedId, node.isAccessibilityFocused());
                propertyReader.readBoolean(this.mAccessibilityHeadingId, node.isAccessibilityHeading());
                propertyReader.readIntEnum(this.mAccessibilityLiveRegionId, node.getAccessibilityLiveRegion());
                propertyReader.readObject(this.mAccessibilityPaneTitleId, node.getAccessibilityPaneTitle());
                propertyReader.readResourceId(this.mAccessibilityTraversalAfterId, node.getAccessibilityTraversalAfter());
                propertyReader.readResourceId(this.mAccessibilityTraversalBeforeId, node.getAccessibilityTraversalBefore());
                propertyReader.readBoolean(this.mActivatedId, node.isActivated());
                propertyReader.readFloat(this.mAlphaId, node.getAlpha());
                propertyReader.readObject(this.mAutofillHintsId, node.getAutofillHints());
                propertyReader.readObject(this.mBackgroundId, node.getBackground());
                propertyReader.readObject(this.mBackgroundTintId, node.getBackgroundTintList());
                propertyReader.readObject(this.mBackgroundTintModeId, node.getBackgroundTintMode());
                propertyReader.readInt(this.mBaselineId, node.getBaseline());
                propertyReader.readBoolean(this.mClickableId, node.isClickable());
                propertyReader.readObject(this.mContentDescriptionId, node.getContentDescription());
                propertyReader.readBoolean(this.mContextClickableId, node.isContextClickable());
                propertyReader.readBoolean(this.mDefaultFocusHighlightEnabledId, node.getDefaultFocusHighlightEnabled());
                propertyReader.readIntEnum(this.mDrawingCacheQualityId, node.getDrawingCacheQuality());
                propertyReader.readBoolean(this.mDuplicateParentStateId, node.isDuplicateParentStateEnabled());
                propertyReader.readFloat(this.mElevationId, node.getElevation());
                propertyReader.readBoolean(this.mEnabledId, node.isEnabled());
                propertyReader.readInt(this.mFadingEdgeLengthId, node.getFadingEdgeLength());
                propertyReader.readBoolean(this.mFilterTouchesWhenObscuredId, node.getFilterTouchesWhenObscured());
                propertyReader.readBoolean(this.mFitsSystemWindowsId, node.getFitsSystemWindows());
                propertyReader.readIntEnum(this.mFocusableId, node.getFocusable());
                propertyReader.readBoolean(this.mFocusableInTouchModeId, node.isFocusableInTouchMode());
                propertyReader.readBoolean(this.mFocusedId, node.isFocused());
                propertyReader.readBoolean(this.mFocusedByDefaultId, node.isFocusedByDefault());
                propertyReader.readBoolean(this.mForceDarkAllowedId, node.isForceDarkAllowed());
                propertyReader.readObject(this.mForegroundId, node.getForeground());
                propertyReader.readGravity(this.mForegroundGravityId, node.getForegroundGravity());
                propertyReader.readObject(this.mForegroundTintId, node.getForegroundTintList());
                propertyReader.readObject(this.mForegroundTintModeId, node.getForegroundTintMode());
                propertyReader.readBoolean(this.mHapticFeedbackEnabledId, node.isHapticFeedbackEnabled());
                propertyReader.readResourceId(this.mIdId, node.getId());
                propertyReader.readIntEnum(this.mImportantForAccessibilityId, node.getImportantForAccessibility());
                propertyReader.readIntEnum(this.mImportantForAutofillId, node.getImportantForAutofill());
                propertyReader.readBoolean(this.mIsScrollContainerId, node.isScrollContainer());
                propertyReader.readBoolean(this.mKeepScreenOnId, node.getKeepScreenOn());
                propertyReader.readBoolean(this.mKeyboardNavigationClusterId, node.isKeyboardNavigationCluster());
                propertyReader.readResourceId(this.mLabelForId, node.getLabelFor());
                propertyReader.readIntEnum(this.mLayerTypeId, node.getLayerType());
                propertyReader.readIntEnum(this.mLayoutDirectionId, node.getLayoutDirection());
                propertyReader.readBoolean(this.mLongClickableId, node.isLongClickable());
                propertyReader.readInt(this.mMinHeightId, node.getMinimumHeight());
                propertyReader.readInt(this.mMinWidthId, node.getMinimumWidth());
                propertyReader.readBoolean(this.mNestedScrollingEnabledId, node.isNestedScrollingEnabled());
                propertyReader.readResourceId(this.mNextClusterForwardId, node.getNextClusterForwardId());
                propertyReader.readResourceId(this.mNextFocusDownId, node.getNextFocusDownId());
                propertyReader.readResourceId(this.mNextFocusForwardId, node.getNextFocusForwardId());
                propertyReader.readResourceId(this.mNextFocusLeftId, node.getNextFocusLeftId());
                propertyReader.readResourceId(this.mNextFocusRightId, node.getNextFocusRightId());
                propertyReader.readResourceId(this.mNextFocusUpId, node.getNextFocusUpId());
                propertyReader.readColor(this.mOutlineAmbientShadowColorId, node.getOutlineAmbientShadowColor());
                propertyReader.readObject(this.mOutlineProviderId, node.getOutlineProvider());
                propertyReader.readColor(this.mOutlineSpotShadowColorId, node.getOutlineSpotShadowColor());
                propertyReader.readIntEnum(this.mOverScrollModeId, node.getOverScrollMode());
                propertyReader.readInt(this.mPaddingBottomId, node.getPaddingBottom());
                propertyReader.readInt(this.mPaddingLeftId, node.getPaddingLeft());
                propertyReader.readInt(this.mPaddingRightId, node.getPaddingRight());
                propertyReader.readInt(this.mPaddingTopId, node.getPaddingTop());
                propertyReader.readObject(this.mPointerIconId, node.getPointerIcon());
                propertyReader.readBoolean(this.mPressedId, node.isPressed());
                propertyReader.readIntEnum(this.mRawLayoutDirectionId, node.getRawLayoutDirection());
                propertyReader.readIntEnum(this.mRawTextAlignmentId, node.getRawTextAlignment());
                propertyReader.readIntEnum(this.mRawTextDirectionId, node.getRawTextDirection());
                propertyReader.readIntFlag(this.mRequiresFadingEdgeId, node.getFadingEdge());
                propertyReader.readFloat(this.mRotationId, node.getRotation());
                propertyReader.readFloat(this.mRotationXId, node.getRotationX());
                propertyReader.readFloat(this.mRotationYId, node.getRotationY());
                propertyReader.readBoolean(this.mSaveEnabledId, node.isSaveEnabled());
                propertyReader.readFloat(this.mScaleXId, node.getScaleX());
                propertyReader.readFloat(this.mScaleYId, node.getScaleY());
                propertyReader.readBoolean(this.mScreenReaderFocusableId, node.isScreenReaderFocusable());
                propertyReader.readIntFlag(this.mScrollIndicatorsId, node.getScrollIndicators());
                propertyReader.readInt(this.mScrollXId, node.getScrollX());
                propertyReader.readInt(this.mScrollYId, node.getScrollY());
                propertyReader.readInt(this.mScrollbarDefaultDelayBeforeFadeId, node.getScrollBarDefaultDelayBeforeFade());
                propertyReader.readInt(this.mScrollbarFadeDurationId, node.getScrollBarFadeDuration());
                propertyReader.readInt(this.mScrollbarSizeId, node.getScrollBarSize());
                propertyReader.readIntEnum(this.mScrollbarStyleId, node.getScrollBarStyle());
                propertyReader.readBoolean(this.mSelectedId, node.isSelected());
                propertyReader.readColor(this.mSolidColorId, node.getSolidColor());
                propertyReader.readBoolean(this.mSoundEffectsEnabledId, node.isSoundEffectsEnabled());
                propertyReader.readObject(this.mStateListAnimatorId, node.getStateListAnimator());
                propertyReader.readObject(this.mTagId, node.getTag());
                propertyReader.readIntEnum(this.mTextAlignmentId, node.getTextAlignment());
                propertyReader.readIntEnum(this.mTextDirectionId, node.getTextDirection());
                propertyReader.readObject(this.mTooltipTextId, node.getTooltipText());
                propertyReader.readFloat(this.mTransformPivotXId, node.getPivotX());
                propertyReader.readFloat(this.mTransformPivotYId, node.getPivotY());
                propertyReader.readObject(this.mTransitionNameId, node.getTransitionName());
                propertyReader.readFloat(this.mTranslationXId, node.getTranslationX());
                propertyReader.readFloat(this.mTranslationYId, node.getTranslationY());
                propertyReader.readFloat(this.mTranslationZId, node.getTranslationZ());
                propertyReader.readIntEnum(this.mVisibilityId, node.getVisibility());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
    }

    public void forceRTL(boolean flag) {
        this.mForceRTL = flag;
    }

    /* access modifiers changed from: package-private */
    public static class TransformationInfo {
        @ViewDebug.ExportedProperty
        private float mAlpha = 1.0f;
        private Matrix mInverseMatrix;
        private final Matrix mMatrix = new Matrix();
        float mTransitionAlpha = 1.0f;

        TransformationInfo() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class TintInfo {
        BlendMode mBlendMode;
        boolean mHasTintList;
        boolean mHasTintMode;
        ColorStateList mTintList;

        TintInfo() {
        }
    }

    /* access modifiers changed from: private */
    public static class ForegroundInfo {
        private boolean mBoundsChanged;
        private Drawable mDrawable;
        private int mGravity;
        private boolean mInsidePadding;
        private final Rect mOverlayBounds;
        private final Rect mSelfBounds;
        private TintInfo mTintInfo;

        private ForegroundInfo() {
            this.mGravity = 119;
            this.mInsidePadding = true;
            this.mBoundsChanged = true;
            this.mSelfBounds = new Rect();
            this.mOverlayBounds = new Rect();
        }
    }

    /* access modifiers changed from: package-private */
    public static class ListenerInfo {
        int mAcceptableItemCnt;
        ArrayList<String> mAcceptableMimeType;
        int mMimeTypeSupportState;
        OnApplyWindowInsetsListener mOnApplyWindowInsetsListener;
        private CopyOnWriteArrayList<OnAttachStateChangeListener> mOnAttachStateChangeListeners;
        OnCapturedPointerListener mOnCapturedPointerListener;
        @UnsupportedAppUsage
        public OnClickListener mOnClickListener;
        protected OnContextClickListener mOnContextClickListener;
        @UnsupportedAppUsage
        protected OnCreateContextMenuListener mOnCreateContextMenuListener;
        @UnsupportedAppUsage
        private OnDragListener mOnDragListener;
        @UnsupportedAppUsage
        protected OnFocusChangeListener mOnFocusChangeListener;
        @UnsupportedAppUsage
        private OnGenericMotionListener mOnGenericMotionListener;
        @UnsupportedAppUsage
        private OnHoverListener mOnHoverListener;
        @UnsupportedAppUsage
        private OnKeyListener mOnKeyListener;
        private ArrayList<OnLayoutChangeListener> mOnLayoutChangeListeners;
        @UnsupportedAppUsage
        protected OnLongClickListener mOnLongClickListener;
        protected OnScrollChangeListener mOnScrollChangeListener;
        private OnSystemUiVisibilityChangeListener mOnSystemUiVisibilityChangeListener;
        @UnsupportedAppUsage
        private OnTouchListener mOnTouchListener;
        public RenderNode.PositionUpdateListener mPositionUpdateListener;
        private List<Rect> mSystemGestureExclusionRects;
        private ArrayList<OnUnhandledKeyEventListener> mUnhandledKeyListeners;
        private WindowInsetsAnimationListener mWindowInsetsAnimationListener;

        ListenerInfo() {
        }
    }

    /* access modifiers changed from: private */
    public static class TooltipInfo {
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
        /* access modifiers changed from: public */
        private boolean updateAnchorPos(MotionEvent event) {
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
        /* access modifiers changed from: public */
        private void clearAnchorPos() {
            this.mAnchorX = Integer.MAX_VALUE;
            this.mAnchorY = Integer.MAX_VALUE;
        }
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
        this.mHoveringTouchDelegate = false;
        this.mDrawingCacheBackgroundColor = 0;
        this.mAnimator = null;
        this.mLayerType = 0;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 0) : null;
        this.mSourceLayoutId = 0;
        this.mCurrentDirtyRect = new Rect();
        this.mIsNeedDetect = HwMwUtils.ENABLED;
        this.isAndroidForceDarkState = true;
        this.isHwForceDarkState = false;
        this.mContext = context;
        this.mResources = context != null ? context.getResources() : resources;
        this.mViewFlags = 402653200;
        this.mPrivateFlags2 = 140296;
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setOverScrollMode(1);
        this.mUserPaddingStart = Integer.MIN_VALUE;
        this.mUserPaddingEnd = Integer.MIN_VALUE;
        this.mRenderNode = RenderNode.create(getClass().getName(), new ViewAnimationHostBridge(this));
        if (!sCompatibilityDone && context != null) {
            int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
            sUseBrokenMakeMeasureSpec = targetSdkVersion <= 17;
            sIgnoreMeasureCache = targetSdkVersion < 19;
            Canvas.sCompatibilityRestore = targetSdkVersion < 23;
            Canvas.sCompatibilitySetBitmap = targetSdkVersion < 26;
            Canvas.setCompatibilityVersion(targetSdkVersion);
            sUseZeroUnspecifiedMeasureSpec = targetSdkVersion < 23;
            sAlwaysRemeasureExactly = targetSdkVersion <= 23;
            sTextureViewIgnoresDrawableSetters = targetSdkVersion <= 23;
            sPreserveMarginParamsInLayoutParamConversion = targetSdkVersion >= 24;
            sCascadedDragDrop = targetSdkVersion < 24;
            sHasFocusableExcludeAutoFocusable = targetSdkVersion < 26;
            sAutoFocusableOffUIThreadWontNotifyParents = targetSdkVersion < 26;
            sUseDefaultFocusHighlight = context.getResources().getBoolean(R.bool.config_useDefaultFocusHighlight);
            sThrowOnInvalidFloatProperties = targetSdkVersion >= 28;
            sCanFocusZeroSized = targetSdkVersion < 28;
            sAlwaysAssignFocus = targetSdkVersion < 28;
            sAcceptZeroSizeDragShadow = targetSdkVersion < 28;
            sBrokenInsetsDispatch = ViewRootImpl.sNewInsetsMode != 2 || targetSdkVersion < 29;
            sBrokenWindowBackground = targetSdkVersion < 29 ? true : z;
            sCompatibilityDone = true;
        }
    }

    public View(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public View(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /* JADX INFO: Multiple debug info for r0v5 int: [D('background' android.graphics.drawable.Drawable), D('overScrollMode' int)] */
    /* JADX INFO: Multiple debug info for r1v2 int: [D('leftPadding' int), D('targetSdkVersion' int)] */
    /* JADX INFO: Multiple debug info for r16v1 int: [D('viewFlagValues' int), D('viewFlagMasks' int)] */
    /* JADX INFO: Multiple debug info for r15v33 int: [D('rightPaddingDefined' boolean), D('numHints' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:199:0x071e, code lost:
        if (r1 >= 14) goto L_0x08ae;
     */
    public View(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context);
        int rightPadding;
        int leftPadding;
        int rightPadding2;
        int x;
        int rightPadding3;
        boolean leftPaddingDefined;
        boolean rightPaddingDefined;
        String rawString;
        CharSequence[] rawHints;
        CharSequence[] rawHints2;
        this.mSourceLayoutId = Resources.getAttributeSetSourceResId(attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.View, defStyleAttr, defStyleRes);
        retrieveExplicitStyle(context.getTheme(), attrs);
        saveAttributeDataForStyleable(context, R.styleable.View, attrs, a, defStyleAttr, defStyleRes);
        if (sDebugViewAttributes) {
            saveAttributeData(attrs, a);
        }
        int startPadding = Integer.MIN_VALUE;
        int endPadding = Integer.MIN_VALUE;
        boolean transformSet = false;
        int scrollbarStyle = 0;
        int overScrollMode = this.mOverScrollMode;
        boolean initializeScrollbars = false;
        boolean initializeScrollIndicators = false;
        boolean startPaddingDefined = false;
        boolean endPaddingDefined = false;
        int leftPadding2 = -1;
        int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        int topPadding = -1;
        int viewFlagValues = 0 | 16;
        int padding = -1;
        int viewFlagMasks = 0 | 16;
        float tx = 0.0f;
        float ty = 0.0f;
        float tz = 0.0f;
        float elevation = 0.0f;
        float rotation = 0.0f;
        float rotationX = 0.0f;
        float rotationY = 0.0f;
        float sx = 1.0f;
        float sy = 1.0f;
        Drawable background = null;
        int rightPadding4 = -1;
        int paddingHorizontal = -1;
        int paddingVertical = -1;
        int x2 = 0;
        int y = 0;
        boolean rightPaddingDefined2 = false;
        boolean rightPaddingDefined3 = false;
        int i = 0;
        boolean setScrollContainer = false;
        int bottomPadding = -1;
        int overScrollMode2 = overScrollMode;
        for (int N = a.getIndexCount(); i < N; N = N) {
            int attr = a.getIndex(i);
            if (attr != 109) {
                switch (attr) {
                    case 8:
                        int scrollbarStyle2 = a.getInt(attr, 0);
                        if (scrollbarStyle2 != 0) {
                            viewFlagValues |= scrollbarStyle2 & 50331648;
                            scrollbarStyle = scrollbarStyle2;
                            viewFlagMasks = 50331648 | viewFlagMasks;
                            padding = padding;
                            rightPaddingDefined3 = rightPaddingDefined3;
                            rightPaddingDefined2 = rightPaddingDefined2;
                            break;
                        } else {
                            scrollbarStyle = scrollbarStyle2;
                            padding = padding;
                            rightPaddingDefined3 = rightPaddingDefined3;
                            rightPaddingDefined2 = rightPaddingDefined2;
                            break;
                        }
                    case 9:
                        leftPaddingDefined = rightPaddingDefined2;
                        rightPaddingDefined = rightPaddingDefined3;
                        this.mID = a.getResourceId(attr, -1);
                        break;
                    case 10:
                        leftPaddingDefined = rightPaddingDefined2;
                        rightPaddingDefined = rightPaddingDefined3;
                        this.mTag = a.getText(attr);
                        break;
                    case 11:
                        x2 = a.getDimensionPixelOffset(attr, 0);
                        padding = padding;
                        break;
                    case 12:
                        y = a.getDimensionPixelOffset(attr, 0);
                        padding = padding;
                        break;
                    case 13:
                        background = a.getDrawable(attr);
                        padding = padding;
                        break;
                    case 14:
                        int padding2 = a.getDimensionPixelSize(attr, -1);
                        this.mUserPaddingLeftInitial = padding2;
                        this.mUserPaddingRightInitial = padding2;
                        rightPaddingDefined3 = true;
                        rightPaddingDefined2 = true;
                        padding = padding2;
                        break;
                    case 15:
                        int leftPadding3 = a.getDimensionPixelSize(attr, -1);
                        this.mUserPaddingLeftInitial = leftPadding3;
                        leftPadding2 = leftPadding3;
                        rightPaddingDefined2 = true;
                        padding = padding;
                        break;
                    case 16:
                        topPadding = a.getDimensionPixelSize(attr, -1);
                        padding = padding;
                        break;
                    case 17:
                        int rightPadding5 = a.getDimensionPixelSize(attr, -1);
                        this.mUserPaddingRightInitial = rightPadding5;
                        rightPadding4 = rightPadding5;
                        rightPaddingDefined3 = true;
                        padding = padding;
                        break;
                    case 18:
                        bottomPadding = a.getDimensionPixelSize(attr, -1);
                        padding = padding;
                        break;
                    case 19:
                        int viewFlagValues2 = (viewFlagValues & -18) | getFocusableAttribute(a);
                        if ((viewFlagValues2 & 16) == 0) {
                            viewFlagValues = viewFlagValues2;
                            viewFlagMasks |= 17;
                            padding = padding;
                            rightPaddingDefined3 = rightPaddingDefined3;
                            rightPaddingDefined2 = rightPaddingDefined2;
                            break;
                        } else {
                            viewFlagValues = viewFlagValues2;
                            padding = padding;
                            rightPaddingDefined3 = rightPaddingDefined3;
                            rightPaddingDefined2 = rightPaddingDefined2;
                            break;
                        }
                    case 20:
                        leftPaddingDefined = rightPaddingDefined2;
                        rightPaddingDefined = rightPaddingDefined3;
                        if (a.getBoolean(attr, false)) {
                            viewFlagValues = (viewFlagValues & -17) | 262145;
                            viewFlagMasks = 262161 | viewFlagMasks;
                            padding = padding;
                            rightPaddingDefined3 = rightPaddingDefined;
                            rightPaddingDefined2 = leftPaddingDefined;
                            break;
                        }
                        break;
                    case 21:
                        leftPaddingDefined = rightPaddingDefined2;
                        rightPaddingDefined = rightPaddingDefined3;
                        int visibility = a.getInt(attr, 0);
                        if (visibility != 0) {
                            viewFlagMasks |= 12;
                            viewFlagValues = VISIBILITY_FLAGS[visibility] | viewFlagValues;
                            padding = padding;
                            rightPaddingDefined3 = rightPaddingDefined;
                            rightPaddingDefined2 = leftPaddingDefined;
                            break;
                        }
                        break;
                    case 22:
                        leftPaddingDefined = rightPaddingDefined2;
                        rightPaddingDefined = rightPaddingDefined3;
                        if (a.getBoolean(attr, false)) {
                            viewFlagValues |= 2;
                            viewFlagMasks |= 2;
                            padding = padding;
                            rightPaddingDefined3 = rightPaddingDefined;
                            rightPaddingDefined2 = leftPaddingDefined;
                            break;
                        }
                        break;
                    case 23:
                        leftPaddingDefined = rightPaddingDefined2;
                        rightPaddingDefined = rightPaddingDefined3;
                        int scrollbars = a.getInt(attr, 0);
                        if (scrollbars != 0) {
                            viewFlagMasks |= 768;
                            initializeScrollbars = true;
                            rightPaddingDefined3 = rightPaddingDefined;
                            rightPaddingDefined2 = leftPaddingDefined;
                            viewFlagValues |= scrollbars;
                            padding = padding;
                            break;
                        }
                        break;
                    case 24:
                        leftPaddingDefined = rightPaddingDefined2;
                        rightPaddingDefined = rightPaddingDefined3;
                        break;
                    default:
                        switch (attr) {
                            case 26:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                this.mNextFocusLeftId = a.getResourceId(attr, -1);
                                break;
                            case 27:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                this.mNextFocusRightId = a.getResourceId(attr, -1);
                                break;
                            case 28:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                this.mNextFocusUpId = a.getResourceId(attr, -1);
                                break;
                            case 29:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                this.mNextFocusDownId = a.getResourceId(attr, -1);
                                break;
                            case 30:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                if (a.getBoolean(attr, false)) {
                                    viewFlagValues |= 16384;
                                    viewFlagMasks |= 16384;
                                    padding = padding;
                                    rightPaddingDefined3 = rightPaddingDefined;
                                    rightPaddingDefined2 = leftPaddingDefined;
                                    break;
                                }
                                break;
                            case 31:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                if (a.getBoolean(attr, false)) {
                                    viewFlagMasks = 2097152 | viewFlagMasks;
                                    viewFlagValues |= 2097152;
                                    padding = padding;
                                    rightPaddingDefined3 = rightPaddingDefined;
                                    rightPaddingDefined2 = leftPaddingDefined;
                                    break;
                                }
                                break;
                            case 32:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                if (!a.getBoolean(attr, true)) {
                                    viewFlagMasks = 65536 | viewFlagMasks;
                                    viewFlagValues |= 65536;
                                    padding = padding;
                                    rightPaddingDefined3 = rightPaddingDefined;
                                    rightPaddingDefined2 = leftPaddingDefined;
                                    break;
                                }
                                break;
                            case 33:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                int cacheQuality = a.getInt(attr, 0);
                                if (cacheQuality != 0) {
                                    viewFlagMasks |= DRAWING_CACHE_QUALITY_MASK;
                                    viewFlagValues = DRAWING_CACHE_QUALITY_FLAGS[cacheQuality] | viewFlagValues;
                                    padding = padding;
                                    rightPaddingDefined3 = rightPaddingDefined;
                                    rightPaddingDefined2 = leftPaddingDefined;
                                    break;
                                }
                                break;
                            case 34:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                if (a.getBoolean(attr, false)) {
                                    viewFlagValues = 4194304 | viewFlagValues;
                                    viewFlagMasks = 4194304 | viewFlagMasks;
                                    padding = padding;
                                    rightPaddingDefined3 = rightPaddingDefined;
                                    rightPaddingDefined2 = leftPaddingDefined;
                                    break;
                                }
                                break;
                            case 35:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                if (targetSdkVersion >= 23 || (this instanceof FrameLayout)) {
                                    setForeground(a.getDrawable(attr));
                                    break;
                                }
                            case 36:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                this.mMinWidth = a.getDimensionPixelSize(attr, 0);
                                break;
                            case 37:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                this.mMinHeight = a.getDimensionPixelSize(attr, 0);
                                break;
                            case 38:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                if (targetSdkVersion >= 23 || (this instanceof FrameLayout)) {
                                    setForegroundGravity(a.getInt(attr, 0));
                                    break;
                                }
                            case 39:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                if (!a.getBoolean(attr, true)) {
                                    viewFlagValues = -134217729 & viewFlagValues;
                                    viewFlagMasks = 134217728 | viewFlagMasks;
                                    padding = padding;
                                    rightPaddingDefined3 = rightPaddingDefined;
                                    rightPaddingDefined2 = leftPaddingDefined;
                                    break;
                                }
                                break;
                            case 40:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                if (a.getBoolean(attr, false)) {
                                    viewFlagValues = 67108864 | viewFlagValues;
                                    viewFlagMasks = 67108864 | viewFlagMasks;
                                    padding = padding;
                                    rightPaddingDefined3 = rightPaddingDefined;
                                    rightPaddingDefined2 = leftPaddingDefined;
                                    break;
                                }
                                break;
                            case 41:
                                if (a.getBoolean(attr, false)) {
                                    setScrollContainer(true);
                                }
                                setScrollContainer = true;
                                padding = padding;
                                rightPaddingDefined3 = rightPaddingDefined3;
                                rightPaddingDefined2 = rightPaddingDefined2;
                                break;
                            case 42:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                if (!a.getBoolean(attr, true)) {
                                    viewFlagValues = -268435457 & viewFlagValues;
                                    viewFlagMasks = 268435456 | viewFlagMasks;
                                    padding = padding;
                                    rightPaddingDefined3 = rightPaddingDefined;
                                    rightPaddingDefined2 = leftPaddingDefined;
                                    break;
                                }
                                break;
                            case 43:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                if (!context.isRestricted()) {
                                    String handlerName = a.getString(attr);
                                    if (handlerName != null) {
                                        setOnClickListener(new DeclaredOnClickListener(this, handlerName));
                                        break;
                                    }
                                } else {
                                    throw new IllegalStateException("The android:onClick attribute cannot be used within a restricted context");
                                }
                                break;
                            case 44:
                                leftPaddingDefined = rightPaddingDefined2;
                                rightPaddingDefined = rightPaddingDefined3;
                                setContentDescription(a.getString(attr));
                                break;
                            default:
                                switch (attr) {
                                    case 48:
                                        overScrollMode2 = a.getInt(attr, 1);
                                        padding = padding;
                                        break;
                                    case 49:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        if (a.getBoolean(attr, false)) {
                                            viewFlagValues |= 1024;
                                            viewFlagMasks |= 1024;
                                            padding = padding;
                                            rightPaddingDefined3 = rightPaddingDefined;
                                            rightPaddingDefined2 = leftPaddingDefined;
                                            break;
                                        }
                                        break;
                                    case 50:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setAlpha(a.getFloat(attr, 1.0f));
                                        break;
                                    case 51:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setPivotX(a.getDimension(attr, 0.0f));
                                        break;
                                    case 52:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setPivotY(a.getDimension(attr, 0.0f));
                                        break;
                                    case 53:
                                        tx = a.getDimension(attr, 0.0f);
                                        transformSet = true;
                                        padding = padding;
                                        break;
                                    case 54:
                                        ty = a.getDimension(attr, 0.0f);
                                        transformSet = true;
                                        padding = padding;
                                        break;
                                    case 55:
                                        sx = a.getFloat(attr, 1.0f);
                                        transformSet = true;
                                        padding = padding;
                                        break;
                                    case 56:
                                        sy = a.getFloat(attr, 1.0f);
                                        transformSet = true;
                                        padding = padding;
                                        break;
                                    case 57:
                                        rotation = a.getFloat(attr, 0.0f);
                                        transformSet = true;
                                        padding = padding;
                                        break;
                                    case 58:
                                        rotationX = a.getFloat(attr, 0.0f);
                                        transformSet = true;
                                        padding = padding;
                                        break;
                                    case 59:
                                        rotationY = a.getFloat(attr, 0.0f);
                                        transformSet = true;
                                        padding = padding;
                                        break;
                                    case 60:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        this.mVerticalScrollbarPosition = a.getInt(attr, 0);
                                        break;
                                    case 61:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        this.mNextFocusForwardId = a.getResourceId(attr, -1);
                                        break;
                                    case 62:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setLayerType(a.getInt(attr, 0), null);
                                        break;
                                    case 63:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        int fadingEdge = a.getInt(attr, 0);
                                        if (fadingEdge != 0) {
                                            viewFlagMasks |= 12288;
                                            initializeFadingEdgeInternal(a);
                                            viewFlagValues |= fadingEdge;
                                            padding = padding;
                                            rightPaddingDefined3 = rightPaddingDefined;
                                            rightPaddingDefined2 = leftPaddingDefined;
                                            break;
                                        }
                                        break;
                                    case 64:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setImportantForAccessibility(a.getInt(attr, 0));
                                        break;
                                    case 65:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        this.mPrivateFlags2 &= -449;
                                        int textDirection = a.getInt(attr, -1);
                                        if (textDirection != -1) {
                                            this.mPrivateFlags2 |= PFLAG2_TEXT_DIRECTION_FLAGS[textDirection];
                                            break;
                                        }
                                        break;
                                    case 66:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        this.mPrivateFlags2 &= -57345;
                                        this.mPrivateFlags2 |= PFLAG2_TEXT_ALIGNMENT_FLAGS[a.getInt(attr, 1)];
                                        break;
                                    case 67:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        this.mPrivateFlags2 &= -61;
                                        int layoutDirection = a.getInt(attr, -1);
                                        this.mPrivateFlags2 |= (layoutDirection != -1 ? LAYOUT_DIRECTION_FLAGS[layoutDirection] : 2) << 2;
                                        break;
                                    case 68:
                                        startPadding = a.getDimensionPixelSize(attr, Integer.MIN_VALUE);
                                        startPaddingDefined = startPadding != Integer.MIN_VALUE;
                                        padding = padding;
                                        rightPaddingDefined3 = rightPaddingDefined3;
                                        rightPaddingDefined2 = rightPaddingDefined2;
                                        break;
                                    case 69:
                                        endPadding = a.getDimensionPixelSize(attr, Integer.MIN_VALUE);
                                        endPaddingDefined = endPadding != Integer.MIN_VALUE;
                                        padding = padding;
                                        rightPaddingDefined3 = rightPaddingDefined3;
                                        rightPaddingDefined2 = rightPaddingDefined2;
                                        break;
                                    case 70:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setLabelFor(a.getResourceId(attr, -1));
                                        break;
                                    case 71:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setAccessibilityLiveRegion(a.getInt(attr, 0));
                                        break;
                                    case 72:
                                        tz = a.getDimension(attr, 0.0f);
                                        transformSet = true;
                                        padding = padding;
                                        break;
                                    case 73:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setTransitionName(a.getString(attr));
                                        break;
                                    case 74:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setNestedScrollingEnabled(a.getBoolean(attr, false));
                                        break;
                                    case 75:
                                        elevation = a.getDimension(attr, 0.0f);
                                        transformSet = true;
                                        padding = padding;
                                        break;
                                    case 76:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setStateListAnimator(AnimatorInflater.loadStateListAnimator(context, a.getResourceId(attr, 0)));
                                        break;
                                    case 77:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        if (this.mBackgroundTint == null) {
                                            this.mBackgroundTint = new TintInfo();
                                        }
                                        this.mBackgroundTint.mTintList = a.getColorStateList(77);
                                        this.mBackgroundTint.mHasTintList = true;
                                        break;
                                    case 78:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        if (this.mBackgroundTint == null) {
                                            this.mBackgroundTint = new TintInfo();
                                        }
                                        this.mBackgroundTint.mBlendMode = Drawable.parseBlendMode(a.getInt(78, -1), null);
                                        this.mBackgroundTint.mHasTintMode = true;
                                        break;
                                    case 79:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        if (targetSdkVersion >= 23 || (this instanceof FrameLayout)) {
                                            setForegroundTintList(a.getColorStateList(attr));
                                            break;
                                        }
                                    case 80:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        if (targetSdkVersion >= 23 || (this instanceof FrameLayout)) {
                                            setForegroundTintBlendMode(Drawable.parseBlendMode(a.getInt(attr, -1), null));
                                            break;
                                        }
                                    case 81:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setOutlineProviderFromAttribute(a.getInt(81, 0));
                                        break;
                                    case 82:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setAccessibilityTraversalBefore(a.getResourceId(attr, -1));
                                        break;
                                    case 83:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setAccessibilityTraversalAfter(a.getResourceId(attr, -1));
                                        break;
                                    case 84:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        int scrollIndicators = (a.getInt(attr, 0) << 8) & SCROLL_INDICATORS_PFLAG3_MASK;
                                        if (scrollIndicators != 0) {
                                            this.mPrivateFlags3 |= scrollIndicators;
                                            initializeScrollIndicators = true;
                                            padding = padding;
                                            rightPaddingDefined3 = rightPaddingDefined;
                                            rightPaddingDefined2 = leftPaddingDefined;
                                            break;
                                        }
                                        break;
                                    case 85:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        if (a.getBoolean(attr, false)) {
                                            viewFlagValues = 8388608 | viewFlagValues;
                                            viewFlagMasks = 8388608 | viewFlagMasks;
                                            padding = padding;
                                            rightPaddingDefined3 = rightPaddingDefined;
                                            rightPaddingDefined2 = leftPaddingDefined;
                                            break;
                                        }
                                        break;
                                    case 86:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        int resourceId = a.getResourceId(attr, 0);
                                        if (resourceId != 0) {
                                            setPointerIcon(PointerIcon.load(context.getResources(), resourceId));
                                            break;
                                        } else {
                                            int pointerType = a.getInt(attr, 1);
                                            if (pointerType != 1) {
                                                setPointerIcon(PointerIcon.getSystemIcon(context, pointerType));
                                                break;
                                            }
                                        }
                                        break;
                                    case 87:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        if (a.peekValue(attr) != null) {
                                            forceHasOverlappingRendering(a.getBoolean(attr, true));
                                            break;
                                        }
                                        break;
                                    case 88:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        setTooltipText(a.getText(attr));
                                        break;
                                    case 89:
                                        int paddingHorizontal2 = a.getDimensionPixelSize(attr, -1);
                                        this.mUserPaddingLeftInitial = paddingHorizontal2;
                                        this.mUserPaddingRightInitial = paddingHorizontal2;
                                        paddingHorizontal = paddingHorizontal2;
                                        rightPaddingDefined3 = true;
                                        rightPaddingDefined2 = true;
                                        padding = padding;
                                        break;
                                    case 90:
                                        paddingVertical = a.getDimensionPixelSize(attr, -1);
                                        padding = padding;
                                        break;
                                    case 91:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        if (a.peekValue(attr) != null) {
                                            setKeyboardNavigationCluster(a.getBoolean(attr, true));
                                            break;
                                        }
                                        break;
                                    case 92:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        this.mNextClusterForwardId = a.getResourceId(attr, -1);
                                        break;
                                    case 93:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        if (a.peekValue(attr) != null) {
                                            setFocusedByDefault(a.getBoolean(attr, true));
                                            break;
                                        }
                                        break;
                                    case 94:
                                        if (a.peekValue(attr) != null) {
                                            String rawString2 = null;
                                            CharSequence[] rawHints3 = null;
                                            if (a.getType(attr) == 1) {
                                                try {
                                                    rawHints2 = a.getTextArray(attr);
                                                } catch (Resources.NotFoundException e) {
                                                    rawString2 = getResources().getString(a.getResourceId(attr, 0));
                                                    rawHints2 = null;
                                                }
                                                rawHints3 = rawHints2;
                                                rawString = rawString2;
                                            } else {
                                                rawString = a.getString(attr);
                                            }
                                            if (rawHints3 != null) {
                                                rawHints = rawHints3;
                                            } else if (rawString != null) {
                                                rawHints = rawString.split(SmsManager.REGEX_PREFIX_DELIMITER);
                                            } else {
                                                throw new IllegalArgumentException("Could not resolve autofillHints");
                                            }
                                            String[] hints = new String[rawHints.length];
                                            rightPaddingDefined = rightPaddingDefined3;
                                            int numHints = rawHints.length;
                                            leftPaddingDefined = rightPaddingDefined2;
                                            for (int rawHintNum = 0; rawHintNum < numHints; rawHintNum++) {
                                                hints[rawHintNum] = rawHints[rawHintNum].toString().trim();
                                            }
                                            setAutofillHints(hints);
                                            break;
                                        } else {
                                            leftPaddingDefined = rightPaddingDefined2;
                                            rightPaddingDefined = rightPaddingDefined3;
                                            break;
                                        }
                                    case 95:
                                        if (a.peekValue(attr) != null) {
                                            setImportantForAutofill(a.getInt(attr, 0));
                                            leftPaddingDefined = rightPaddingDefined2;
                                            rightPaddingDefined = rightPaddingDefined3;
                                            break;
                                        } else {
                                            leftPaddingDefined = rightPaddingDefined2;
                                            rightPaddingDefined = rightPaddingDefined3;
                                            break;
                                        }
                                    case 96:
                                        if (a.peekValue(attr) != null) {
                                            setDefaultFocusHighlightEnabled(a.getBoolean(attr, true));
                                            leftPaddingDefined = rightPaddingDefined2;
                                            rightPaddingDefined = rightPaddingDefined3;
                                            break;
                                        } else {
                                            leftPaddingDefined = rightPaddingDefined2;
                                            rightPaddingDefined = rightPaddingDefined3;
                                            break;
                                        }
                                    case 97:
                                        if (a.peekValue(attr) != null) {
                                            setScreenReaderFocusable(a.getBoolean(attr, false));
                                            leftPaddingDefined = rightPaddingDefined2;
                                            rightPaddingDefined = rightPaddingDefined3;
                                            break;
                                        } else {
                                            leftPaddingDefined = rightPaddingDefined2;
                                            rightPaddingDefined = rightPaddingDefined3;
                                            break;
                                        }
                                    case 98:
                                        if (a.peekValue(attr) != null) {
                                            setAccessibilityPaneTitle(a.getString(attr));
                                            leftPaddingDefined = rightPaddingDefined2;
                                            rightPaddingDefined = rightPaddingDefined3;
                                            break;
                                        } else {
                                            leftPaddingDefined = rightPaddingDefined2;
                                            rightPaddingDefined = rightPaddingDefined3;
                                            break;
                                        }
                                    case 99:
                                        setAccessibilityHeading(a.getBoolean(attr, false));
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        break;
                                    case 100:
                                        setOutlineSpotShadowColor(a.getColor(attr, -16777216));
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        break;
                                    case 101:
                                        setOutlineAmbientShadowColor(a.getColor(attr, -16777216));
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        break;
                                    case 102:
                                        this.isAndroidForceDarkState = a.getBoolean(attr, true);
                                        this.mRenderNode.setForceDarkAllowed(this.isAndroidForceDarkState);
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        break;
                                    default:
                                        leftPaddingDefined = rightPaddingDefined2;
                                        rightPaddingDefined = rightPaddingDefined3;
                                        break;
                                }
                                break;
                        }
                        break;
                }
                i++;
            } else {
                leftPaddingDefined = rightPaddingDefined2;
                rightPaddingDefined = rightPaddingDefined3;
                if (targetSdkVersion >= 23 || (this instanceof FrameLayout)) {
                    if (this.mForegroundInfo == null) {
                        this.mForegroundInfo = new ForegroundInfo();
                    }
                    ForegroundInfo foregroundInfo = this.mForegroundInfo;
                    foregroundInfo.mInsidePadding = a.getBoolean(attr, foregroundInfo.mInsidePadding);
                }
            }
            padding = padding;
            rightPaddingDefined3 = rightPaddingDefined;
            rightPaddingDefined2 = leftPaddingDefined;
            i++;
        }
        setOverScrollMode(overScrollMode2);
        this.mUserPaddingStart = startPadding;
        this.mUserPaddingEnd = endPadding;
        if (background != null) {
            setBackground(background);
        }
        this.mLeftPaddingDefined = rightPaddingDefined2;
        this.mRightPaddingDefined = rightPaddingDefined3;
        if (padding >= 0) {
            topPadding = padding;
            bottomPadding = padding;
            this.mUserPaddingLeftInitial = padding;
            this.mUserPaddingRightInitial = padding;
            rightPadding2 = padding;
            rightPadding = padding;
            leftPadding = paddingHorizontal;
        } else {
            if (paddingHorizontal >= 0) {
                leftPadding2 = paddingHorizontal;
                leftPadding = paddingHorizontal;
                this.mUserPaddingLeftInitial = leftPadding;
                this.mUserPaddingRightInitial = leftPadding;
                rightPadding3 = paddingHorizontal;
            } else {
                leftPadding = paddingHorizontal;
                rightPadding3 = rightPadding4;
            }
            if (paddingVertical >= 0) {
                topPadding = paddingVertical;
                bottomPadding = paddingVertical;
                rightPadding2 = rightPadding3;
                rightPadding = leftPadding2;
            } else {
                rightPadding2 = rightPadding3;
                rightPadding = leftPadding2;
            }
        }
        if (isRtlCompatibilityMode()) {
            if (!this.mLeftPaddingDefined && startPaddingDefined) {
                rightPadding = startPadding;
            }
            this.mUserPaddingLeftInitial = rightPadding >= 0 ? rightPadding : this.mUserPaddingLeftInitial;
            if (!this.mRightPaddingDefined && endPaddingDefined) {
                rightPadding2 = endPadding;
            }
            this.mUserPaddingRightInitial = rightPadding2 >= 0 ? rightPadding2 : this.mUserPaddingRightInitial;
        } else {
            boolean hasRelativePadding = startPaddingDefined || endPaddingDefined;
            if (this.mLeftPaddingDefined && !hasRelativePadding) {
                this.mUserPaddingLeftInitial = rightPadding;
            }
            if (this.mRightPaddingDefined && !hasRelativePadding) {
                this.mUserPaddingRightInitial = rightPadding2;
            }
        }
        internalSetPadding(this.mUserPaddingLeftInitial, topPadding >= 0 ? topPadding : this.mPaddingTop, this.mUserPaddingRightInitial, bottomPadding >= 0 ? bottomPadding : this.mPaddingBottom);
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
        if (x2 == 0 && y == 0) {
            x = x2;
        } else {
            x = x2;
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
        if (!setScrollContainer && (viewFlagValues & 512) != 0) {
            setScrollContainer(true);
        }
        computeOpaqueFlags();
    }

    public int[] getAttributeResolutionStack(int attribute) {
        SparseArray<int[]> sparseArray;
        if (!sDebugViewAttributes || (sparseArray = this.mAttributeResolutionStacks) == null || sparseArray.get(attribute) == null) {
            return new int[0];
        }
        int[] attributeResolutionStack = this.mAttributeResolutionStacks.get(attribute);
        int stackSize = attributeResolutionStack.length;
        if (this.mSourceLayoutId != 0) {
            stackSize++;
        }
        int currentIndex = 0;
        int[] stack = new int[stackSize];
        int i = this.mSourceLayoutId;
        if (i != 0) {
            stack[0] = i;
            currentIndex = 0 + 1;
        }
        for (int i2 : attributeResolutionStack) {
            stack[currentIndex] = i2;
            currentIndex++;
        }
        return stack;
    }

    public Map<Integer, Integer> getAttributeSourceResourceMap() {
        HashMap<Integer, Integer> map = new HashMap<>();
        if (!sDebugViewAttributes || this.mAttributeSourceResId == null) {
            return map;
        }
        for (int i = 0; i < this.mAttributeSourceResId.size(); i++) {
            map.put(Integer.valueOf(this.mAttributeSourceResId.keyAt(i)), Integer.valueOf(this.mAttributeSourceResId.valueAt(i)));
        }
        return map;
    }

    public int getExplicitStyle() {
        if (!sDebugViewAttributes) {
            return 0;
        }
        return this.mExplicitStyle;
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

        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            if (this.mResolvedMethod == null) {
                resolveMethod(this.mHostView.getContext(), this.mMethodName);
            }
            try {
                this.mResolvedMethod.invoke(this.mResolvedContext, v);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not execute non-public method for android:onClick", e);
            } catch (InvocationTargetException e2) {
                throw new IllegalStateException("Could not execute method for android:onClick", e2);
            }
        }

        private void resolveMethod(Context context, String name) {
            String idText;
            Method method;
            while (context != null) {
                try {
                    if (!context.isRestricted() && (method = context.getClass().getMethod(this.mMethodName, View.class)) != null) {
                        this.mResolvedMethod = method;
                        this.mResolvedContext = context;
                        return;
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
            if (id == -1) {
                idText = "";
            } else {
                idText = " with id '" + this.mHostView.getContext().getResources().getResourceEntryName(id) + "'";
            }
            throw new IllegalStateException("Could not find method " + this.mMethodName + "(View) in a parent or ancestor Context for android:onClick attribute defined on view " + this.mHostView.getClass() + idText);
        }
    }

    @UnsupportedAppUsage
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
        this.mHoveringTouchDelegate = false;
        this.mDrawingCacheBackgroundColor = 0;
        this.mAnimator = null;
        this.mLayerType = 0;
        this.mInputEventConsistencyVerifier = InputEventConsistencyVerifier.isInstrumentationEnabled() ? new InputEventConsistencyVerifier(this, 0) : null;
        this.mSourceLayoutId = 0;
        this.mCurrentDirtyRect = new Rect();
        this.mIsNeedDetect = HwMwUtils.ENABLED;
        this.isAndroidForceDarkState = true;
        this.isHwForceDarkState = false;
        this.mResources = null;
        this.mRenderNode = RenderNode.create(getClass().getName(), new ViewAnimationHostBridge(this));
    }

    /* access modifiers changed from: package-private */
    public final boolean debugDraw() {
        AttachInfo attachInfo;
        return DEBUG_DRAW || ((attachInfo = this.mAttachInfo) != null && attachInfo.mDebugLayout);
    }

    private static SparseArray<String> getAttributeMap() {
        if (mAttributeMap == null) {
            mAttributeMap = new SparseArray<>();
        }
        return mAttributeMap;
    }

    private void retrieveExplicitStyle(Resources.Theme theme, AttributeSet attrs) {
        if (sDebugViewAttributes) {
            try {
                this.mExplicitStyle = theme.getExplicitStyle(attrs);
            } catch (NullPointerException e) {
                Log.e(VIEW_LOG_TAG, "getExplicitStyle happened on null");
            }
        }
    }

    public final void saveAttributeDataForStyleable(Context context, int[] styleable, AttributeSet attrs, TypedArray t, int defStyleAttr, int defStyleRes) {
        if (sDebugViewAttributes) {
            int[] attributeResolutionStack = context.getTheme().getAttributeResolutionStack(defStyleAttr, defStyleRes, this.mExplicitStyle);
            if (this.mAttributeResolutionStacks == null) {
                this.mAttributeResolutionStacks = new SparseArray<>();
            }
            if (this.mAttributeSourceResId == null) {
                this.mAttributeSourceResId = new SparseIntArray();
            }
            int indexCount = t.getIndexCount();
            for (int j = 0; j < indexCount; j++) {
                int index = t.getIndex(j);
                this.mAttributeSourceResId.append(styleable[index], t.getSourceResourceId(index, 0));
                this.mAttributeResolutionStacks.append(styleable[index], attributeResolutionStack);
            }
        }
    }

    private void saveAttributeData(AttributeSet attrs, TypedArray t) {
        int resourceId;
        int attrsCount = attrs == null ? 0 : attrs.getAttributeCount();
        int indexCount = t.getIndexCount();
        String[] attributes = new String[((attrsCount + indexCount) * 2)];
        int i = 0;
        for (int j = 0; j < attrsCount; j++) {
            attributes[i] = attrs.getAttributeName(j);
            attributes[i + 1] = attrs.getAttributeValue(j);
            i += 2;
        }
        Resources res = t.getResources();
        SparseArray<String> attributeMap = getAttributeMap();
        int i2 = i;
        for (int j2 = 0; j2 < indexCount; j2++) {
            int index = t.getIndex(j2);
            if (t.hasValueOrEmpty(index) && (resourceId = t.getResourceId(index, 0)) != 0) {
                synchronized (attributeMap) {
                    String resourceName = attributeMap.get(resourceId);
                    if (resourceName == null) {
                        try {
                            resourceName = res.getResourceName(resourceId);
                        } catch (Resources.NotFoundException e) {
                            resourceName = "0x" + Integer.toHexString(resourceId);
                        }
                        attributeMap.put(resourceId, resourceName);
                    }
                    attributes[i2] = resourceName;
                    attributes[i2 + 1] = t.getString(index);
                    i2 += 2;
                }
            }
        }
        String[] trimmed = new String[i2];
        System.arraycopy(attributes, 0, trimmed, 0, i2);
        this.mAttributes = trimmed;
    }

    public String toString() {
        String pkgname;
        StringBuilder out = new StringBuilder(128);
        out.append(getClass().getName());
        out.append('{');
        out.append(Integer.toHexString(System.identityHashCode(this)));
        out.append(' ');
        int i = this.mViewFlags & 12;
        char c = 'I';
        char c2 = 'V';
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
            c2 = '.';
        }
        out.append(c2);
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
        int i2 = this.mPrivateFlags;
        if ((33554432 & i2) != 0) {
            out.append('p');
        } else {
            out.append((i2 & 16384) != 0 ? 'P' : '.');
        }
        if ((this.mPrivateFlags & 268435456) == 0) {
            c5 = '.';
        }
        out.append(c5);
        out.append((this.mPrivateFlags & 1073741824) != 0 ? DateFormat.CAPITAL_AM_PM : '.');
        if ((this.mPrivateFlags & Integer.MIN_VALUE) == 0) {
            c = '.';
        }
        out.append(c);
        if ((this.mPrivateFlags & 2097152) != 0) {
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
                int i3 = -16777216 & id;
                if (i3 == 16777216) {
                    pkgname = "android";
                } else if (i3 != 2130706432) {
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
        if (this.mAutofillId != null) {
            out.append(" aid=");
            out.append(this.mAutofillId);
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
        ScrollabilityCache cache;
        if (!isVerticalFadingEdgeEnabled() || (cache = this.mScrollCache) == null) {
            return 0;
        }
        return cache.fadingEdgeLength;
    }

    public void setFadingEdgeLength(int length) {
        initScrollCache();
        this.mScrollCache.fadingEdgeLength = length;
    }

    public int getHorizontalFadingEdgeLength() {
        ScrollabilityCache cache;
        if (!isHorizontalFadingEdgeEnabled() || (cache = this.mScrollCache) == null) {
            return 0;
        }
        return cache.fadingEdgeLength;
    }

    public int getVerticalScrollbarWidth() {
        ScrollBarDrawable scrollBar;
        ScrollabilityCache cache = this.mScrollCache;
        if (cache == null || (scrollBar = cache.scrollBar) == null) {
            return 0;
        }
        int size = scrollBar.getSize(true);
        if (size <= 0) {
            return cache.scrollBarSize;
        }
        return size;
    }

    /* access modifiers changed from: protected */
    public int getHorizontalScrollbarHeight() {
        ScrollBarDrawable scrollBar;
        ScrollabilityCache cache = this.mScrollCache;
        if (cache == null || (scrollBar = cache.scrollBar) == null) {
            return 0;
        }
        int size = scrollBar.getSize(false);
        if (size <= 0) {
            return cache.scrollBarSize;
        }
        return size;
    }

    /* access modifiers changed from: protected */
    public void initializeScrollbars(TypedArray a) {
        TypedArray arr = this.mContext.obtainStyledAttributes(R.styleable.View);
        initializeScrollbarsInternal(arr);
        arr.recycle();
    }

    private void initializeScrollBarDrawable() {
        initScrollCache();
        if (this.mScrollCache.scrollBar == null) {
            this.mScrollCache.scrollBar = new ScrollBarDrawable();
            this.mScrollCache.scrollBar.setState(getDrawableState());
            this.mScrollCache.scrollBar.setCallback(this);
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
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

    public void setVerticalScrollbarThumbDrawable(Drawable drawable) {
        initializeScrollBarDrawable();
        this.mScrollCache.scrollBar.setVerticalThumbDrawable(drawable);
    }

    public void setVerticalScrollbarTrackDrawable(Drawable drawable) {
        initializeScrollBarDrawable();
        this.mScrollCache.scrollBar.setVerticalTrackDrawable(drawable);
    }

    public void setHorizontalScrollbarThumbDrawable(Drawable drawable) {
        initializeScrollBarDrawable();
        this.mScrollCache.scrollBar.setHorizontalThumbDrawable(drawable);
    }

    public void setHorizontalScrollbarTrackDrawable(Drawable drawable) {
        initializeScrollBarDrawable();
        this.mScrollCache.scrollBar.setHorizontalTrackDrawable(drawable);
    }

    public Drawable getVerticalScrollbarThumbDrawable() {
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (scrollabilityCache != null) {
            return scrollabilityCache.scrollBar.getVerticalThumbDrawable();
        }
        return null;
    }

    public Drawable getVerticalScrollbarTrackDrawable() {
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (scrollabilityCache != null) {
            return scrollabilityCache.scrollBar.getVerticalTrackDrawable();
        }
        return null;
    }

    public Drawable getHorizontalScrollbarThumbDrawable() {
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (scrollabilityCache != null) {
            return scrollabilityCache.scrollBar.getHorizontalThumbDrawable();
        }
        return null;
    }

    public Drawable getHorizontalScrollbarTrackDrawable() {
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (scrollabilityCache != null) {
            return scrollabilityCache.scrollBar.getHorizontalTrackDrawable();
        }
        return null;
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

    @UnsupportedAppUsage
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
        boolean canScrollVertically = computeVerticalScrollRange() > computeVerticalScrollExtent();
        if (isVerticalScrollBarEnabled() && !isVerticalScrollBarHidden() && canScrollVertically) {
            Rect touchBounds = this.mScrollCache.mScrollBarTouchBounds;
            getVerticalScrollBarBounds(null, touchBounds);
            if (touchBounds.contains((int) x2, (int) y2)) {
                return true;
            }
        }
        boolean canScrollHorizontally = computeHorizontalScrollRange() > computeHorizontalScrollExtent();
        if (isHorizontalScrollBarEnabled() && canScrollHorizontally) {
            Rect touchBounds2 = this.mScrollCache.mScrollBarTouchBounds;
            getHorizontalScrollBarBounds(null, touchBounds2);
            if (touchBounds2.contains((int) x2, (int) y2)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean isOnScrollbarThumb(float x, float y) {
        return isOnVerticalScrollbarThumb(x, y) || isOnHorizontalScrollbarThumb(x, y);
    }

    private boolean isOnVerticalScrollbarThumb(float x, float y) {
        int range;
        int extent;
        if (this.mScrollCache != null && isVerticalScrollBarEnabled() && !isVerticalScrollBarHidden() && (range = computeVerticalScrollRange()) > (extent = computeVerticalScrollExtent())) {
            float x2 = x + ((float) getScrollX());
            float y2 = y + ((float) getScrollY());
            Rect bounds = this.mScrollCache.mScrollBarBounds;
            Rect touchBounds = this.mScrollCache.mScrollBarTouchBounds;
            getVerticalScrollBarBounds(bounds, touchBounds);
            int offset = computeVerticalScrollOffset();
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
        int range;
        int extent;
        if (this.mScrollCache != null && isHorizontalScrollBarEnabled() && (range = computeHorizontalScrollRange()) > (extent = computeHorizontalScrollExtent())) {
            float x2 = x + ((float) getScrollX());
            float y2 = y + ((float) getScrollY());
            Rect bounds = this.mScrollCache.mScrollBarBounds;
            Rect touchBounds = this.mScrollCache.mScrollBarTouchBounds;
            getHorizontalScrollBarBounds(bounds, touchBounds);
            int offset = computeHorizontalScrollOffset();
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
    @UnsupportedAppUsage
    public boolean isDraggingScrollBar() {
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        return (scrollabilityCache == null || scrollabilityCache.mScrollBarDraggingState == 0) ? false : true;
    }

    public void setScrollIndicators(int indicators) {
        setScrollIndicators(indicators, 63);
    }

    public void setScrollIndicators(int indicators, int mask) {
        int mask2 = (mask << 8) & SCROLL_INDICATORS_PFLAG3_MASK;
        int indicators2 = (indicators << 8) & mask2;
        int i = this.mPrivateFlags3;
        int updatedFlags = ((~mask2) & i) | indicators2;
        if (i != updatedFlags) {
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
    @UnsupportedAppUsage
    public ListenerInfo getListenerInfo() {
        ListenerInfo listenerInfo = this.mListenerInfo;
        if (listenerInfo != null) {
            return listenerInfo;
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
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo == null) {
            if (this.mFrameMetricsObservers == null) {
                this.mFrameMetricsObservers = new ArrayList<>();
            }
            this.mFrameMetricsObservers.add(new FrameMetricsObserver(window, handler.getLooper(), listener));
        } else if (attachInfo.mThreadedRenderer != null) {
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
        if (fmo != null) {
            ArrayList<FrameMetricsObserver> arrayList = this.mFrameMetricsObservers;
            if (arrayList != null) {
                arrayList.remove(fmo);
                if (renderer != null) {
                    renderer.removeFrameMetricsObserver(fmo);
                    return;
                }
                return;
            }
            return;
        }
        throw new IllegalArgumentException("attempt to remove OnFrameMetricsAvailableListener that was never added");
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
        if (this.mFrameMetricsObservers == null) {
            return null;
        }
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
    /* access modifiers changed from: public */
    private boolean performClickInternal() {
        notifyAutofillManagerOnClick();
        return performClick();
    }

    public boolean performClick() {
        boolean result;
        Jlog.setInClickFuncMark();
        notifyAutofillManagerOnClick();
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
        Jlog.clearTouchState();
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
        if (!event.isFromSource(8194) || (event.getButtonState() & 2) == 0) {
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

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void startActivityForResult(Intent intent, int requestCode) {
        this.mStartActivityRequestWho = "@android:view:" + System.identityHashCode(this);
        getContext().startActivityForResult(this.mStartActivityRequestWho, intent, requestCode, null);
    }

    public boolean dispatchActivityResult(String who, int requestCode, int resultCode, Intent data) {
        String str = this.mStartActivityRequestWho;
        if (str == null || !str.equals(who)) {
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

    public void setDragAcceptableMimeType(String[] mimeTypes) {
        HwViewEx.setDragAcceptableMimeType(mimeTypes, this);
    }

    /* access modifiers changed from: package-private */
    public void handleFocusGainInternal(int direction, Rect previouslyFocusedRect) {
        int i = this.mPrivateFlags;
        if ((i & 2) == 0) {
            this.mPrivateFlags = i | 2;
            View oldFocus = this.mAttachInfo != null ? getRootView().findFocus() : null;
            ViewParent viewParent = this.mParent;
            if (viewParent != null) {
                viewParent.requestChildFocus(this, this);
                updateFocusedInCluster(oldFocus, direction);
            }
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo != null) {
                attachInfo.mTreeObserver.dispatchOnGlobalFocusChange(oldFocus, this);
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
        AttachInfo attachInfo = this.mAttachInfo;
        RectF position = attachInfo != null ? attachInfo.mTmpTransformRect : new RectF();
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
        clearFocusInternal(null, true, sAlwaysAssignFocus || !isInTouchMode());
    }

    /* access modifiers changed from: package-private */
    public void clearFocusInternal(View focused, boolean propagate, boolean refocus) {
        ViewParent viewParent;
        int i = this.mPrivateFlags;
        if ((i & 2) != 0) {
            this.mPrivateFlags = i & -3;
            clearParentsWantFocus();
            if (propagate && (viewParent = this.mParent) != null) {
                viewParent.clearChildFocus(this);
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
        AttachInfo attachInfo;
        if (oldFocus != null && (attachInfo = this.mAttachInfo) != null) {
            attachInfo.mTreeObserver.dispatchOnGlobalFocusChange(oldFocus, null);
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
        int i = this.mViewFlags;
        if (!((i & 12) == 0 && (i & 32) == 0)) {
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
        if (!gainFocus) {
            if (isPressed()) {
                setPressed(false);
            }
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo != null && attachInfo.mHasWindowFocus) {
                notifyFocusChangeToInputMethodManager(false);
            }
            onFocusLost();
        } else {
            AttachInfo attachInfo2 = this.mAttachInfo;
            if (attachInfo2 != null && attachInfo2.mHasWindowFocus) {
                notifyFocusChangeToInputMethodManager(true);
            }
        }
        invalidate(true);
        ListenerInfo li = this.mListenerInfo;
        if (!(li == null || li.mOnFocusChangeListener == null)) {
            li.mOnFocusChangeListener.onFocusChange(this, gainFocus);
        }
        AttachInfo attachInfo3 = this.mAttachInfo;
        if (attachInfo3 != null) {
            attachInfo3.mKeyDispatchState.reset(this);
        }
        notifyEnterOrExitForAutoFillIfNeeded(gainFocus);
    }

    private void notifyFocusChangeToInputMethodManager(boolean hasFocus) {
        if (IS_TV && hasFocus && isFocused()) {
            Rect globalRect = new Rect();
            getGlobalVisibleRect(globalRect);
            if (!(globalRect.bottom - globalRect.top == 0 || globalRect.right - globalRect.left == 0)) {
                List<Rect> focusAreas = new ArrayList<>();
                focusAreas.add(globalRect);
                ComponentName componentName = new ComponentName(this.mContext.getPackageName(), "");
                Context context = this.mContext;
                if (context instanceof Activity) {
                    componentName = ((Activity) context).getComponentName();
                }
                HwWindowManager.notifyFocusViewLocationChange(focusAreas, componentName);
            }
        }
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(InputMethodManager.class);
        if (imm != null) {
            if (hasFocus) {
                imm.focusIn(this);
            } else {
                imm.focusOut(this);
            }
        }
    }

    public void notifyEnterOrExitForAutoFillIfNeeded(boolean enter) {
        AutofillManager afm;
        if (canNotifyAutofillEnterExitEvent() && (afm = getAutofillManager()) != null) {
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

    @Override // android.view.accessibility.AccessibilityEventSource
    public void sendAccessibilityEvent(int eventType) {
        AccessibilityDelegate accessibilityDelegate = this.mAccessibilityDelegate;
        if (accessibilityDelegate != null) {
            accessibilityDelegate.sendAccessibilityEvent(this, eventType);
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

    @Override // android.view.accessibility.AccessibilityEventSource
    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        AccessibilityDelegate accessibilityDelegate = this.mAccessibilityDelegate;
        if (accessibilityDelegate != null) {
            accessibilityDelegate.sendAccessibilityEventUnchecked(this, event);
        } else {
            sendAccessibilityEventUncheckedInternal(event);
        }
    }

    public void sendAccessibilityEventUncheckedInternal(AccessibilityEvent event) {
        boolean isWindowDisappearedEvent = true;
        if (!(event.getEventType() == 32) || (32 & event.getContentChangeTypes()) == 0) {
            isWindowDisappearedEvent = false;
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
        AccessibilityDelegate accessibilityDelegate = this.mAccessibilityDelegate;
        if (accessibilityDelegate != null) {
            return accessibilityDelegate.dispatchPopulateAccessibilityEvent(this, event);
        }
        return dispatchPopulateAccessibilityEventInternal(event);
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return false;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityDelegate accessibilityDelegate = this.mAccessibilityDelegate;
        if (accessibilityDelegate != null) {
            accessibilityDelegate.onPopulateAccessibilityEvent(this, event);
        } else {
            onPopulateAccessibilityEventInternal(event);
        }
    }

    public void onPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        if (event.getEventType() == 32 && isAccessibilityPane()) {
            event.getText().add(getAccessibilityPaneTitle());
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityDelegate accessibilityDelegate = this.mAccessibilityDelegate;
        if (accessibilityDelegate != null) {
            accessibilityDelegate.onInitializeAccessibilityEvent(this, event);
        } else {
            onInitializeAccessibilityEventInternal(event);
        }
    }

    @UnsupportedAppUsage
    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        CharSequence text;
        event.setSource(this);
        event.setClassName(getAccessibilityClassName());
        event.setPackageName(getContext().getPackageName());
        event.setEnabled(isEnabled());
        event.setContentDescription(this.mContentDescription);
        int eventType = event.getEventType();
        if (eventType == 8) {
            AttachInfo attachInfo = this.mAttachInfo;
            ArrayList<View> focusablesTempList = attachInfo != null ? attachInfo.mTempArrayList : new ArrayList<>();
            getRootView().addFocusables(focusablesTempList, 2, 0);
            event.setItemCount(focusablesTempList.size());
            event.setCurrentItemIndex(focusablesTempList.indexOf(this));
            if (this.mAttachInfo != null) {
                focusablesTempList.clear();
            }
        } else if (eventType == 8192 && (text = getIterableTextForAccessibility()) != null && text.length() > 0) {
            event.setFromIndex(getAccessibilitySelectionStart());
            event.setToIndex(getAccessibilitySelectionEnd());
            event.setItemCount(text.length());
        }
    }

    public AccessibilityNodeInfo createAccessibilityNodeInfo() {
        AccessibilityDelegate accessibilityDelegate = this.mAccessibilityDelegate;
        if (accessibilityDelegate != null) {
            return accessibilityDelegate.createAccessibilityNodeInfo(this);
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
        AccessibilityDelegate accessibilityDelegate = this.mAccessibilityDelegate;
        if (accessibilityDelegate != null) {
            accessibilityDelegate.onInitializeAccessibilityNodeInfo(this, info);
        } else {
            onInitializeAccessibilityNodeInfoInternal(info);
        }
    }

    @UnsupportedAppUsage
    public void getBoundsOnScreen(Rect outRect) {
        getBoundsOnScreen(outRect, false);
    }

    @UnsupportedAppUsage
    public void getBoundsOnScreen(Rect outRect, boolean clipToParent) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            RectF position = attachInfo.mTmpTransformRect;
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
        onProvideStructure(structure, 0, 0);
    }

    public void onProvideAutofillStructure(ViewStructure structure, int flags) {
        onProvideStructure(structure, 1, flags);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00d0  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00d9  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00e2  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00eb  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00fd  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0106  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x010d  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0122  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x012b  */
    public void onProvideStructure(ViewStructure structure, int viewFor, int flags) {
        int ignoredParentTop;
        int ignoredParentLeft;
        String type;
        String pkg;
        String pkg2;
        int id = this.mID;
        if (id == -1 || isViewIdGenerated(id)) {
            structure.setId(id, null, null, null);
        } else {
            try {
                Resources res = getResources();
                pkg = res.getResourceEntryName(id);
                type = res.getResourceTypeName(id);
                pkg2 = res.getResourcePackageName(id);
            } catch (Resources.NotFoundException e) {
                type = null;
                pkg = null;
                pkg2 = null;
            }
            structure.setId(id, pkg2, type, pkg);
        }
        if (viewFor == 1 || viewFor == 2) {
            int autofillType = getAutofillType();
            if (autofillType != 0) {
                structure.setAutofillType(autofillType);
                structure.setAutofillHints(getAutofillHints());
                structure.setAutofillValue(getAutofillValue());
            }
            structure.setImportantForAutofill(getImportantForAutofill());
        }
        int ignoredParentLeft2 = 0;
        int ignoredParentTop2 = 0;
        if (viewFor == 1 && (flags & 1) == 0) {
            View parentGroup = null;
            ViewParent viewParent = getParent();
            if (viewParent instanceof View) {
                parentGroup = (View) viewParent;
            }
            while (true) {
                if (parentGroup == null || parentGroup.isImportantForAutofill()) {
                    break;
                }
                ignoredParentLeft2 += parentGroup.mLeft;
                ignoredParentTop2 += parentGroup.mTop;
                ViewParent viewParent2 = parentGroup.getParent();
                if (!(viewParent2 instanceof View)) {
                    ignoredParentLeft = ignoredParentLeft2;
                    ignoredParentTop = ignoredParentTop2;
                    break;
                }
                parentGroup = (View) viewParent2;
            }
            int ignoredParentLeft3 = this.mLeft;
            int i = this.mTop;
            structure.setDimens(ignoredParentLeft + ignoredParentLeft3, ignoredParentTop + i, this.mScrollX, this.mScrollY, this.mRight - ignoredParentLeft3, this.mBottom - i);
            if (viewFor == 0) {
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
        ignoredParentLeft = ignoredParentLeft2;
        ignoredParentTop = ignoredParentTop2;
        int ignoredParentLeft32 = this.mLeft;
        int i2 = this.mTop;
        structure.setDimens(ignoredParentLeft + ignoredParentLeft32, ignoredParentTop + i2, this.mScrollX, this.mScrollY, this.mRight - ignoredParentLeft32, this.mBottom - i2);
        if (viewFor == 0) {
        }
        structure.setVisibility(getVisibility());
        structure.setEnabled(isEnabled());
        if (isClickable()) {
        }
        if (isFocusable()) {
        }
        if (isFocused()) {
        }
        if (isAccessibilityFocused()) {
        }
        if (isSelected()) {
        }
        if (isActivated()) {
        }
        if (isLongClickable()) {
        }
        if (this instanceof Checkable) {
        }
        if (isOpaque()) {
        }
        if (isContextClickable()) {
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
            if (forAutofill && Log.isLoggable(AUTOFILL_LOG_TAG, 2)) {
                Log.v(AUTOFILL_LOG_TAG, "onProvideVirtualStructureCompat() for " + this);
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
        AccessibilityNodeProvider provider;
        if (this.mContext.isAutofillCompatibilityEnabled() && (provider = getAccessibilityNodeProvider()) != null) {
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

    public final AutofillId getAutofillId() {
        if (this.mAutofillId == null) {
            this.mAutofillId = new AutofillId(getAutofillViewId());
        }
        return this.mAutofillId;
    }

    public void setAutofillId(AutofillId id) {
        if (Log.isLoggable(AUTOFILL_LOG_TAG, 2)) {
            Log.v(AUTOFILL_LOG_TAG, "setAutofill(): from " + this.mAutofillId + " to " + id);
        }
        if (isAttachedToWindow()) {
            throw new IllegalStateException("Cannot set autofill id when view is attached");
        } else if (id != null && !id.isNonVirtual()) {
            throw new IllegalStateException("Cannot set autofill id assigned to virtual views");
        } else if (id != null || (this.mPrivateFlags3 & 1073741824) != 0) {
            this.mAutofillId = id;
            if (id != null) {
                this.mAutofillViewId = id.getViewId();
                this.mPrivateFlags3 = 1073741824 | this.mPrivateFlags3;
                return;
            }
            this.mAutofillViewId = -1;
            this.mPrivateFlags3 &= -1073741825;
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
                if (Log.isLoggable(AUTOFILL_LOG_TAG, 2)) {
                    Log.v(AUTOFILL_LOG_TAG, "View (" + this + ") is not important for autofill because parent " + parent + "'s importance is " + parentImportance);
                }
                return false;
            }
        }
        int importance = getImportantForAutofill();
        if (importance == 4 || importance == 1) {
            return true;
        }
        if (importance == 8 || importance == 2) {
            if (Log.isLoggable(AUTOFILL_LOG_TAG, 2)) {
                Log.v(AUTOFILL_LOG_TAG, "View (" + this + ") is not important for autofill because its importance is " + importance);
            }
            return false;
        } else if (importance != 0) {
            Log.w(AUTOFILL_LOG_TAG, "invalid autofill importance (" + importance + " on view " + this);
            return false;
        } else {
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
    }

    public void setContentCaptureSession(ContentCaptureSession contentCaptureSession) {
        this.mContentCaptureSession = contentCaptureSession;
    }

    public final ContentCaptureSession getContentCaptureSession() {
        ContentCaptureSession contentCaptureSession = this.mCachedContentCaptureSession;
        if (contentCaptureSession != null) {
            return contentCaptureSession;
        }
        this.mCachedContentCaptureSession = getAndCacheContentCaptureSession();
        return this.mCachedContentCaptureSession;
    }

    private ContentCaptureSession getAndCacheContentCaptureSession() {
        ContentCaptureSession contentCaptureSession = this.mContentCaptureSession;
        if (contentCaptureSession != null) {
            return contentCaptureSession;
        }
        ContentCaptureSession session = null;
        ViewParent viewParent = this.mParent;
        if (viewParent instanceof View) {
            session = ((View) viewParent).getContentCaptureSession();
        }
        if (session != null) {
            return session;
        }
        ContentCaptureManager ccm = (ContentCaptureManager) this.mContext.getSystemService(ContentCaptureManager.class);
        if (ccm == null) {
            return null;
        }
        return ccm.getMainContentCaptureSession();
    }

    private AutofillManager getAutofillManager() {
        return (AutofillManager) this.mContext.getSystemService(AutofillManager.class);
    }

    private boolean isAutofillable() {
        AutofillManager afm;
        if (getAutofillType() == 0) {
            return false;
        }
        if (!isImportantForAutofill()) {
            AutofillOptions options = this.mContext.getAutofillOptions();
            if (options == null || !options.isAugmentedAutofillEnabled(this.mContext) || (afm = getAutofillManager()) == null) {
                return false;
            }
            afm.notifyViewEnteredForAugmentedAutofill(this);
        }
        if (getAutofillViewId() > 1073741823) {
            return true;
        }
        return false;
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
                    cinfo.recycle();
                }
            }
        }
    }

    public void dispatchProvideStructure(ViewStructure structure) {
        dispatchProvideStructure(structure, 0, 0);
    }

    public void dispatchProvideAutofillStructure(ViewStructure structure, int flags) {
        dispatchProvideStructure(structure, 1, flags);
    }

    private void dispatchProvideStructure(ViewStructure structure, int viewFor, int flags) {
        if (viewFor == 1) {
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
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            Rect bounds = attachInfo.mTmpInvalRect;
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
                AttachInfo attachInfo2 = this.mAttachInfo;
                if (!(attachInfo2 == null || (attachInfo2.mAccessibilityFetchFlags & 16) == 0 || !Resources.resourceHasPackage(this.mID))) {
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
            TooltipInfo tooltipInfo = this.mTooltipInfo;
            if (!(tooltipInfo == null || tooltipInfo.mTooltipText == null)) {
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
            TouchDelegate touchDelegate = this.mTouchDelegate;
            if (touchDelegate != null) {
                info.setTouchDelegateInfo(touchDelegate.getTouchDelegateInfo());
            }
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
                    for (int i = 0; i < childDrawIndex; i++) {
                        drawingOrderInParent += numViewsForAccessibility(preorderedList.get(i));
                    }
                } else {
                    int childIndex = parentGroup.indexOfChild(viewAtDrawingLevel);
                    boolean customOrder = parentGroup.isChildrenDrawingOrderEnabled();
                    int childDrawIndex2 = (childIndex < 0 || !customOrder) ? childIndex : parentGroup.getChildDrawingOrder(childCount, childIndex);
                    int numChildrenToIterate = customOrder ? childCount : childDrawIndex2;
                    if (childDrawIndex2 != 0) {
                        for (int i2 = 0; i2 < numChildrenToIterate; i2++) {
                            if ((customOrder ? parentGroup.getChildDrawingOrder(childCount, i2) : i2) < childDrawIndex2) {
                                drawingOrderInParent += numViewsForAccessibility(parentGroup.getChildAt(i2));
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
        if (view == null) {
            return 0;
        }
        if (view.includeForAccessibility()) {
            return 1;
        }
        if (view instanceof ViewGroup) {
            return ((ViewGroup) view).getNumChildrenForAccessibility();
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
            return false;
        }
        Log.w(VIEW_LOG_TAG, "isVisibleToUserForAutofill(" + virtualId + "): no provider");
        return false;
    }

    @UnsupportedAppUsage
    public boolean isVisibleToUser() {
        return isVisibleToUser(null);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isVisibleToUser(Rect boundInView) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo == null || attachInfo.mWindowVisibility != 0) {
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
        AccessibilityDelegate accessibilityDelegate = this.mAccessibilityDelegate;
        if (accessibilityDelegate != null) {
            return accessibilityDelegate.getAccessibilityNodeProvider(this);
        }
        return null;
    }

    @UnsupportedAppUsage
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
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mAccessibilityWindowId;
        }
        return -1;
    }

    @ViewDebug.ExportedProperty(category = Context.ACCESSIBILITY_SERVICE)
    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    @RemotableViewMethod
    public void setContentDescription(CharSequence contentDescription) {
        CharSequence charSequence = this.mContentDescription;
        if (charSequence == null) {
            if (contentDescription == null) {
                return;
            }
        } else if (charSequence.equals(contentDescription)) {
            return;
        }
        this.mContentDescription = contentDescription;
        if (!(contentDescription != null && contentDescription.length() > 0) || getImportantForAccessibility() != 0) {
            notifyViewAccessibilityStateChangedIfNeeded(4);
            return;
        }
        setImportantForAccessibility(1);
        notifySubtreeAccessibilityStateChangedIfNeeded();
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

    @ViewDebug.ExportedProperty(category = Context.ACCESSIBILITY_SERVICE)
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
    @UnsupportedAppUsage
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
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo != null && (this.mPrivateFlags & 1048576) == 0) {
                attachInfo.mScrollContainers.add(this);
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
        ViewParent parent;
        View current = this;
        while ((current.mViewFlags & 12) == 0 && (parent = current.mParent) != null) {
            if (!(parent instanceof View)) {
                return true;
            }
            current = (View) parent;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean fitSystemWindows(Rect insets) {
        int i = this.mPrivateFlags3;
        if ((i & 32) != 0) {
            return fitSystemWindowsInt(insets);
        }
        if (insets == null) {
            return false;
        }
        try {
            this.mPrivateFlags3 = i | 64;
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
            if (fitSystemWindows(insets.getSystemWindowInsetsAsRect())) {
                return insets.consumeSystemWindowInsets();
            }
        } else if (fitSystemWindowsInt(insets.getSystemWindowInsetsAsRect())) {
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

    public void setWindowInsetsAnimationListener(WindowInsetsAnimationListener listener) {
        getListenerInfo().mWindowInsetsAnimationListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void dispatchWindowInsetsAnimationStarted(WindowInsetsAnimationListener.InsetsAnimation animation) {
        ListenerInfo listenerInfo = this.mListenerInfo;
        if (listenerInfo != null && listenerInfo.mWindowInsetsAnimationListener != null) {
            this.mListenerInfo.mWindowInsetsAnimationListener.onStarted(animation);
        }
    }

    /* access modifiers changed from: package-private */
    public WindowInsets dispatchWindowInsetsAnimationProgress(WindowInsets insets) {
        ListenerInfo listenerInfo = this.mListenerInfo;
        if (listenerInfo == null || listenerInfo.mWindowInsetsAnimationListener == null) {
            return insets;
        }
        return this.mListenerInfo.mWindowInsetsAnimationListener.onProgress(insets);
    }

    /* access modifiers changed from: package-private */
    public void dispatchWindowInsetsAnimationFinished(WindowInsetsAnimationListener.InsetsAnimation animation) {
        ListenerInfo listenerInfo = this.mListenerInfo;
        if (listenerInfo != null && listenerInfo.mWindowInsetsAnimationListener != null) {
            this.mListenerInfo.mWindowInsetsAnimationListener.onFinished(animation);
        }
    }

    public void setSystemGestureExclusionRects(List<Rect> rects) {
        if (!rects.isEmpty() || this.mListenerInfo != null) {
            ListenerInfo info = getListenerInfo();
            if (rects.isEmpty()) {
                info.mSystemGestureExclusionRects = null;
                if (info.mPositionUpdateListener != null) {
                    this.mRenderNode.removePositionUpdateListener(info.mPositionUpdateListener);
                }
            } else {
                info.mSystemGestureExclusionRects = rects;
                if (info.mPositionUpdateListener == null) {
                    info.mPositionUpdateListener = new RenderNode.PositionUpdateListener() {
                        /* class android.view.View.AnonymousClass1 */

                        @Override // android.graphics.RenderNode.PositionUpdateListener
                        public void positionChanged(long n, int l, int t, int r, int b) {
                            View.this.postUpdateSystemGestureExclusionRects();
                        }

                        @Override // android.graphics.RenderNode.PositionUpdateListener
                        public void positionLost(long frameNumber) {
                            View.this.postUpdateSystemGestureExclusionRects();
                        }
                    };
                    this.mRenderNode.addPositionUpdateListener(info.mPositionUpdateListener);
                }
            }
            postUpdateSystemGestureExclusionRects();
        }
    }

    /* access modifiers changed from: package-private */
    public void postUpdateSystemGestureExclusionRects() {
        Handler h = getHandler();
        if (h != null) {
            h.postAtFrontOfQueue(new Runnable() {
                /* class android.view.$$Lambda$WlJa6OPA72p3gYtA3nVKC7Z1tGY */

                @Override // java.lang.Runnable
                public final void run() {
                    View.this.updateSystemGestureExclusionRects();
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void updateSystemGestureExclusionRects() {
        AttachInfo ai = this.mAttachInfo;
        if (ai != null) {
            ai.mViewRootImpl.updateSystemGestureExclusionRectsForView(this);
        }
    }

    public List<Rect> getSystemGestureExclusionRects() {
        List<Rect> list;
        ListenerInfo info = this.mListenerInfo;
        if (info == null || (list = info.mSystemGestureExclusionRects) == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public void getLocationInSurface(int[] location) {
        getLocationInWindow(location);
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null && attachInfo.mViewRootImpl != null) {
            location[0] = location[0] + this.mAttachInfo.mViewRootImpl.mWindowAttributes.surfaceInsets.left;
            location[1] = location[1] + this.mAttachInfo.mViewRootImpl.mWindowAttributes.surfaceInsets.top;
        }
    }

    public WindowInsets getRootWindowInsets() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mViewRootImpl.getWindowInsets(false);
        }
        return null;
    }

    public WindowInsetsController getWindowInsetsController() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mViewRootImpl.getInsetsController();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    @Deprecated
    public boolean computeFitSystemWindows(Rect inoutInsets, Rect outLocalInsets) {
        WindowInsets innerInsets = computeSystemWindowInsets(new WindowInsets(inoutInsets), outLocalInsets);
        inoutInsets.set(innerInsets.getSystemWindowInsetsAsRect());
        return innerInsets.isSystemWindowInsetsConsumed();
    }

    public WindowInsets computeSystemWindowInsets(WindowInsets in, Rect outLocalInsets) {
        AttachInfo attachInfo;
        if ((this.mViewFlags & 2048) == 0 || (attachInfo = this.mAttachInfo) == null || ((attachInfo.mSystemUiVisibility & 1536) == 0 && !this.mAttachInfo.mOverscanRequested)) {
            outLocalInsets.set(in.getSystemWindowInsetsAsRect());
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

    @UnsupportedAppUsage
    public boolean fitsSystemWindows() {
        return getFitsSystemWindows();
    }

    @Deprecated
    public void requestFitSystemWindows() {
        ViewParent viewParent = this.mParent;
        if (viewParent != null) {
            viewParent.requestFitSystemWindows();
        }
    }

    public void requestApplyInsets() {
        requestFitSystemWindows();
    }

    @UnsupportedAppUsage
    public void makeOptionalFitsSystemWindows() {
        setFlags(2048, 2048);
    }

    public void getOutsets(Rect outOutsetRect) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            outOutsetRect.set(attachInfo.mOutsets);
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
        setFlags(visibility, 12);
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

    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT, mapping = {@ViewDebug.IntToString(from = 0, to = "LTR"), @ViewDebug.IntToString(from = 1, to = "RTL"), @ViewDebug.IntToString(from = 2, to = "INHERIT"), @ViewDebug.IntToString(from = 3, to = "LOCALE")})
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

    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT, mapping = {@ViewDebug.IntToString(from = 0, to = "RESOLVED_DIRECTION_LTR"), @ViewDebug.IntToString(from = 1, to = "RESOLVED_DIRECTION_RTL")})
    public int getLayoutDirection() {
        if (getContext().getApplicationInfo().targetSdkVersion < 17 && !this.mForceRTL) {
            this.mPrivateFlags2 |= 32;
            return 0;
        } else if ((this.mPrivateFlags2 & 16) == 16) {
            return 1;
        } else {
            return 0;
        }
    }

    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    public boolean isLayoutRtl() {
        return getLayoutDirection() == 1;
    }

    public boolean isRtlLocale() {
        String currentLang = Locale.getDefault().getLanguage();
        return currentLang.contains("ar") || currentLang.contains("fa") || currentLang.contains("iw") || currentLang.contains("ug") || currentLang.contains("ur") || isLayoutRtl();
    }

    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
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
        int i2 = this.mTransientStateCount;
        int i3 = 0;
        if (i2 < 0) {
            this.mTransientStateCount = 0;
            Log.e(VIEW_LOG_TAG, "hasTransientState decremented below 0: unmatched pair of setHasTransientState calls");
        } else if ((hasTransientState && i2 == 1) || (!hasTransientState && this.mTransientStateCount == 0)) {
            int i4 = this.mPrivateFlags2 & Integer.MAX_VALUE;
            if (hasTransientState) {
                i3 = Integer.MIN_VALUE;
            }
            this.mPrivateFlags2 = i4 | i3;
            boolean newHasTransientState = hasTransientState();
            ViewParent viewParent = this.mParent;
            if (viewParent != null && newHasTransientState != oldHasTransientState) {
                try {
                    viewParent.childHasTransientStateChanged(this, newHasTransientState);
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
    /* access modifiers changed from: public */
    private void setPressed(boolean pressed, float x, float y) {
        if (pressed) {
            drawableHotspotChanged(x, y);
        }
        setPressed(pressed);
    }

    public void setPressed(boolean pressed) {
        boolean needsRefresh = true;
        if (pressed == ((this.mPrivateFlags & 16384) == 16384)) {
            needsRefresh = false;
        }
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

    @UnsupportedAppUsage
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
        int i = this.mViewFlags;
        if ((i & 16) > 0) {
            return 16;
        }
        return i & 1;
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
        ViewParent viewParent = this.mParent;
        if (viewParent != null) {
            return viewParent.focusSearch(this, direction);
        }
        return null;
    }

    @ViewDebug.ExportedProperty(category = "focus")
    public final boolean isKeyboardNavigationCluster() {
        return (this.mPrivateFlags3 & 32768) != 0;
    }

    /* access modifiers changed from: package-private */
    public View findKeyboardNavigationCluster() {
        ViewParent viewParent = this.mParent;
        if (!(viewParent instanceof View)) {
            return null;
        }
        View cluster = ((View) viewParent).findKeyboardNavigationCluster();
        if (cluster != null) {
            return cluster;
        }
        if (isKeyboardNavigationCluster()) {
            return this;
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
                if (parent != cluster) {
                    child = (View) parent;
                } else {
                    return;
                }
            }
        }
    }

    private void updateFocusedInCluster(View oldFocus, int direction) {
        View oldCluster;
        if (oldFocus != null && (oldCluster = oldFocus.findKeyboardNavigationCluster()) != findKeyboardNavigationCluster()) {
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
            ViewParent viewParent = this.mParent;
            if (!(viewParent instanceof ViewGroup)) {
                return;
            }
            if (isFocusedByDefault) {
                ((ViewGroup) viewParent).setDefaultFocus(this);
            } else {
                ((ViewGroup) viewParent).clearDefaultFocus(this);
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
        ViewParent viewParent = this.mParent;
        if (viewParent != null) {
            return viewParent.keyboardNavigationClusterSearch(currentCluster, direction);
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
        int i;
        if (direction != 1) {
            if (direction == 2) {
                int i2 = this.mNextFocusForwardId;
                if (i2 == -1) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, i2);
            } else if (direction == 17) {
                int i3 = this.mNextFocusLeftId;
                if (i3 == -1) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, i3);
            } else if (direction == 33) {
                int i4 = this.mNextFocusUpId;
                if (i4 == -1) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, i4);
            } else if (direction == 66) {
                int i5 = this.mNextFocusRightId;
                if (i5 == -1) {
                    return null;
                }
                return findViewInsideOutShouldExist(root, i5);
            } else if (direction == 130 && (i = this.mNextFocusDownId) != -1) {
                return findViewInsideOutShouldExist(root, i);
            } else {
                return null;
            }
        } else if (this.mID == -1) {
            return null;
        } else {
            final int id = this.mID;
            return root.findViewByPredicateInsideOut(this, new Predicate<View>() {
                /* class android.view.View.AnonymousClass2 */

                public boolean test(View t) {
                    return t.mNextFocusForwardId == id;
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public View findUserSetNextKeyboardNavigationCluster(View root, int direction) {
        int i;
        if (direction != 1) {
            if (direction == 2 && (i = this.mNextClusterForwardId) != -1) {
                return findViewInsideOutShouldExist(root, i);
            }
            return null;
        } else if (this.mID == -1) {
            return null;
        } else {
            return root.findViewByPredicateInsideOut(this, new Predicate(this.mID) {
                /* class android.view.$$Lambda$View$7kZ4TXHKswReUMQB8098MEBcx_U */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return View.lambda$findUserSetNextKeyboardNavigationCluster$0(this.f$0, (View) obj);
                }
            });
        }
    }

    static /* synthetic */ boolean lambda$findUserSetNextKeyboardNavigationCluster$0(int id, View t) {
        return t.mNextClusterForwardId == id;
    }

    private View findViewInsideOutShouldExist(View root, int id) {
        if (this.mMatchIdPredicate == null) {
            this.mMatchIdPredicate = new MatchIdPredicate();
        }
        MatchIdPredicate matchIdPredicate = this.mMatchIdPredicate;
        matchIdPredicate.mId = id;
        View result = root.findViewByPredicateInsideOut(this, matchIdPredicate);
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
        if ((focusableMode & 1) != 1 || isFocusableInTouchMode()) {
            views.add(this);
        }
    }

    public void addKeyboardNavigationClusters(Collection<View> views, int direction) {
        if (isKeyboardNavigationCluster() && hasFocusable()) {
            views.add(this);
        }
    }

    public void findViewsWithText(ArrayList<View> outViews, CharSequence searched, int flags) {
        CharSequence charSequence;
        if (getAccessibilityNodeProvider() != null) {
            if ((flags & 4) != 0) {
                outViews.add(this);
            }
        } else if ((flags & 2) != 0 && searched != null && searched.length() > 0 && (charSequence = this.mContentDescription) != null && charSequence.length() > 0) {
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

    @UnsupportedAppUsage
    public boolean requestAccessibilityFocus() {
        AccessibilityManager manager = AccessibilityManager.getInstance(this.mContext);
        if (!manager.isEnabled() || !manager.isTouchExplorationEnabled() || (this.mViewFlags & 12) != 0) {
            return false;
        }
        int i = this.mPrivateFlags2;
        if ((i & 67108864) != 0) {
            return false;
        }
        this.mPrivateFlags2 = i | 67108864;
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null) {
            viewRootImpl.setAccessibilityFocus(this, null);
        }
        invalidate();
        sendAccessibilityEvent(32768);
        return true;
    }

    @UnsupportedAppUsage
    public void clearAccessibilityFocus() {
        View focusHost;
        clearAccessibilityFocusNoCallbacks(0);
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null && (focusHost = viewRootImpl.getAccessibilityFocusedHost()) != null && ViewRootImpl.isViewDescendantOf(focusHost, this)) {
            viewRootImpl.setAccessibilityFocus(null, null);
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

    /* access modifiers changed from: package-private */
    public void clearAccessibilityFocusNoCallbacks(int action) {
        int i = this.mPrivateFlags2;
        if ((67108864 & i) != 0) {
            this.mPrivateFlags2 = i & -67108865;
            invalidate();
            if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(65536);
                event.setAction(action);
                AccessibilityDelegate accessibilityDelegate = this.mAccessibilityDelegate;
                if (accessibilityDelegate != null) {
                    accessibilityDelegate.sendAccessibilityEventUnchecked(this, event);
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
        ViewParent viewParent = this.mParent;
        if (viewParent instanceof View) {
            ((View) viewParent).mPrivateFlags &= -2;
            ((View) viewParent).clearParentsWantFocus();
        }
    }

    public final boolean requestFocusFromTouch() {
        ViewRootImpl viewRoot;
        if (isInTouchMode() && (viewRoot = getViewRootImpl()) != null) {
            viewRoot.ensureTouchMode(false);
        }
        return requestFocus(130);
    }

    private boolean hasAncestorThatBlocksDescendantFocus() {
        boolean focusableInTouchMode = isFocusableInTouchMode();
        ViewParent ancestor = this.mParent;
        while (ancestor instanceof ViewGroup) {
            ViewGroup vgAncestor = (ViewGroup) ancestor;
            if (vgAncestor.getDescendantFocusability() == 393216) {
                return true;
            }
            if (!focusableInTouchMode && vgAncestor.shouldBlockFocusForTouchscreen()) {
                return true;
            }
            ancestor = vgAncestor.getParent();
        }
        return false;
    }

    @ViewDebug.ExportedProperty(category = Context.ACCESSIBILITY_SERVICE, mapping = {@ViewDebug.IntToString(from = 0, to = "auto"), @ViewDebug.IntToString(from = 1, to = "yes"), @ViewDebug.IntToString(from = 2, to = "no"), @ViewDebug.IntToString(from = 4, to = "noHideDescendants")})
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
        View focusHost;
        int oldMode = getImportantForAccessibility();
        if (mode != oldMode) {
            boolean oldIncludeForAccessibility = true;
            boolean hideDescendants = mode == 4;
            if ((mode == 2 || hideDescendants) && (focusHost = findAccessibilityFocusHost(hideDescendants)) != null) {
                focusHost.clearAccessibilityFocus();
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
        ViewRootImpl viewRoot;
        View focusHost;
        if (isAccessibilityFocusedViewOrHost()) {
            return this;
        }
        if (!searchDescendants || (viewRoot = getViewRootImpl()) == null || (focusHost = viewRoot.getAccessibilityFocusedHost()) == null || !ViewRootImpl.isViewDescendantOf(focusHost, this)) {
            return null;
        }
        return focusHost;
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
        if (mode == 1 || isActionableForAccessibility() || hasListenersForAccessibility() || getAccessibilityNodeProvider() != null || getAccessibilityLiveRegion() != 0 || isAccessibilityPane()) {
            return true;
        }
        return false;
    }

    public ViewParent getParentForAccessibility() {
        ViewParent viewParent = this.mParent;
        if (!(viewParent instanceof View)) {
            return null;
        }
        if (((View) viewParent).includeForAccessibility()) {
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

    @UnsupportedAppUsage
    public boolean includeForAccessibility() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo == null) {
            return false;
        }
        if ((attachInfo.mAccessibilityFetchFlags & 8) != 0 || isImportantForAccessibility()) {
            return true;
        }
        return false;
    }

    public boolean isActionableForAccessibility() {
        return isClickable() || isLongClickable() || isFocusable();
    }

    private boolean hasListenersForAccessibility() {
        ListenerInfo info = getListenerInfo();
        return (this.mTouchDelegate == null && info.mOnKeyListener == null && info.mOnTouchListener == null && info.mOnGenericMotionListener == null && info.mOnHoverListener == null && info.mOnDragListener == null) ? false : true;
    }

    @UnsupportedAppUsage
    public void notifyViewAccessibilityStateChangedIfNeeded(int changeType) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && this.mAttachInfo != null) {
            if (changeType != 1 && isAccessibilityPane() && (getVisibility() == 0 || changeType == 32)) {
                AccessibilityEvent event = AccessibilityEvent.obtain();
                onInitializeAccessibilityEvent(event);
                event.setEventType(32);
                event.setContentChangeTypes(changeType);
                event.setSource(this);
                onPopulateAccessibilityEvent(event);
                ViewParent viewParent = this.mParent;
                if (viewParent != null) {
                    try {
                        viewParent.requestSendAccessibilityEvent(this, event);
                    } catch (AbstractMethodError e) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                    }
                }
            } else if (getAccessibilityLiveRegion() != 0) {
                AccessibilityEvent event2 = AccessibilityEvent.obtain();
                event2.setEventType(2048);
                event2.setContentChangeTypes(changeType);
                sendAccessibilityEventUnchecked(event2);
            } else {
                ViewParent viewParent2 = this.mParent;
                if (viewParent2 != null) {
                    try {
                        viewParent2.notifySubtreeAccessibilityStateChanged(this, this, changeType);
                    } catch (AbstractMethodError e2) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e2);
                    }
                }
            }
        }
    }

    @UnsupportedAppUsage
    public void notifySubtreeAccessibilityStateChangedIfNeeded() {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled() && this.mAttachInfo != null) {
            int i = this.mPrivateFlags2;
            if ((i & 134217728) == 0) {
                this.mPrivateFlags2 = i | 134217728;
                ViewParent viewParent = this.mParent;
                if (viewParent != null) {
                    try {
                        viewParent.notifySubtreeAccessibilityStateChanged(this, this, 1);
                    } catch (AbstractMethodError e) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                    }
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
        AccessibilityDelegate accessibilityDelegate = this.mAccessibilityDelegate;
        if (accessibilityDelegate != null) {
            return accessibilityDelegate.performAccessibilityAction(this, action, arguments);
        }
        return performAccessibilityActionInternal(action, arguments);
    }

    @UnsupportedAppUsage
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
                AttachInfo attachInfo = this.mAttachInfo;
                if (attachInfo != null) {
                    Rect r = attachInfo.mTmpInvalRect;
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
                TooltipInfo tooltipInfo = this.mTooltipInfo;
                if (tooltipInfo == null || tooltipInfo.mTooltipPopup == null) {
                    return showLongClickTooltip(0, 0);
                }
                return false;
            case 16908357:
                TooltipInfo tooltipInfo2 = this.mTooltipInfo;
                if (tooltipInfo2 == null || tooltipInfo2.mTooltipPopup == null) {
                    return false;
                }
                hideTooltip();
                return true;
        }
        return false;
    }

    private boolean traverseAtGranularity(int granularity, boolean forward, boolean extendSelection) {
        AccessibilityIterators.TextSegmentIterator iterator;
        int selectionStart;
        int selectionStart2;
        int action;
        CharSequence text = getIterableTextForAccessibility();
        if (text == null || text.length() == 0 || (iterator = getIteratorForGranularity(granularity)) == null) {
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
            selectionStart2 = forward ? segmentEnd : segmentStart;
            selectionStart = selectionStart2;
        } else {
            int selectionStart3 = getAccessibilitySelectionStart();
            if (selectionStart3 == -1) {
                selectionStart3 = forward ? segmentStart : segmentEnd;
            }
            selectionStart = forward ? segmentEnd : segmentStart;
            selectionStart2 = selectionStart3;
        }
        setAccessibilitySelection(selectionStart2, selectionStart);
        if (forward) {
            action = 256;
        } else {
            action = 512;
        }
        sendViewTextTraversedAtGranularityEvent(action, granularity, segmentStart, segmentEnd);
        return true;
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public AccessibilityIterators.TextSegmentIterator getIteratorForGranularity(int granularity) {
        CharSequence text;
        if (granularity == 1) {
            CharSequence text2 = getIterableTextForAccessibility();
            if (text2 == null || text2.length() <= 0) {
                return null;
            }
            AccessibilityIterators.CharacterTextSegmentIterator iterator = AccessibilityIterators.CharacterTextSegmentIterator.getInstance(this.mContext.getResources().getConfiguration().locale);
            iterator.initialize(text2.toString());
            return iterator;
        } else if (granularity == 2) {
            CharSequence text3 = getIterableTextForAccessibility();
            if (text3 == null || text3.length() <= 0) {
                return null;
            }
            AccessibilityIterators.WordTextSegmentIterator iterator2 = AccessibilityIterators.WordTextSegmentIterator.getInstance(this.mContext.getResources().getConfiguration().locale);
            iterator2.initialize(text3.toString());
            return iterator2;
        } else if (granularity != 8 || (text = getIterableTextForAccessibility()) == null || text.length() <= 0) {
            return null;
        } else {
            AccessibilityIterators.ParagraphTextSegmentIterator iterator3 = AccessibilityIterators.ParagraphTextSegmentIterator.getInstance();
            iterator3.initialize(text.toString());
            return iterator3;
        }
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
            notifyFocusChangeToInputMethodManager(true);
        }
        notifyEnterOrExitForAutoFillIfNeeded(true);
    }

    public void onFinishTemporaryDetach() {
    }

    public KeyEvent.DispatcherState getKeyDispatcherState() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mKeyDispatchState;
        }
        return null;
    }

    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        return onKeyPreIme(event.getKeyCode(), event);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        InputEventConsistencyVerifier inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier != null) {
            inputEventConsistencyVerifier.onKeyEvent(event, 0);
        }
        ListenerInfo li = this.mListenerInfo;
        if (li != null && li.mOnKeyListener != null && (this.mViewFlags & 32) == 0 && li.mOnKeyListener.onKey(this, event.getKeyCode(), event)) {
            return true;
        }
        AttachInfo attachInfo = this.mAttachInfo;
        if (event.dispatch(this, attachInfo != null ? attachInfo.mKeyDispatchState : null, this)) {
            return true;
        }
        InputEventConsistencyVerifier inputEventConsistencyVerifier2 = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier2 != null) {
            inputEventConsistencyVerifier2.onUnhandledEvent(event, 0);
        }
        return false;
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return onKeyShortcut(event.getKeyCode(), event);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        InputEventConsistencyVerifier inputEventConsistencyVerifier;
        if (event.isTargetAccessibilityFocus()) {
            if (!isAccessibilityFocusedViewOrHost() && !isGestureNavView()) {
                return false;
            }
            event.setTargetAccessibilityFocus(false);
        }
        boolean result = false;
        InputEventConsistencyVerifier inputEventConsistencyVerifier2 = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier2 != null) {
            inputEventConsistencyVerifier2.onTouchEvent(event, 0);
        }
        int actionMasked = event.getActionMasked();
        if (actionMasked == 0) {
            stopNestedScroll();
        }
        if (onFilterTouchEventForSecurity(event)) {
            if ((this.mViewFlags & 32) == 0 && handleScrollBarDragging(event)) {
                result = true;
            }
            long startTime = SystemClock.uptimeMillis();
            ListenerInfo li = this.mListenerInfo;
            if (li != null && li.mOnTouchListener != null && (this.mViewFlags & 32) == 0 && li.mOnTouchListener.onTouch(this, event)) {
                result = true;
            }
            if (!result && onTouchEvent(event)) {
                result = true;
            }
            if (HwLogUtils.isDebugVersion()) {
                HwLogUtils.checkTime(startTime, "dispatchTouchEvent", this);
            }
        }
        if (!result && (inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier) != null) {
            inputEventConsistencyVerifier.onUnhandledEvent(event, 0);
        }
        if (actionMasked == 1 || actionMasked == 3 || (actionMasked == 0 && !result)) {
            stopNestedScroll();
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean isAccessibilityFocusedViewOrHost() {
        return isAccessibilityFocused() || (getViewRootImpl() != null && getViewRootImpl().getAccessibilityFocusedHost() == this);
    }

    /* access modifiers changed from: protected */
    public boolean canReceivePointerEvents() {
        return (this.mViewFlags & 12) == 0 || getAnimation() != null;
    }

    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        if ((this.mViewFlags & 1024) == 0 || (event.getFlags() & 1) == 0) {
            return true;
        }
        return false;
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        InputEventConsistencyVerifier inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier != null) {
            inputEventConsistencyVerifier.onTrackballEvent(event, 0);
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
        InputEventConsistencyVerifier inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier != null) {
            inputEventConsistencyVerifier.onGenericMotionEvent(event, 0);
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
        InputEventConsistencyVerifier inputEventConsistencyVerifier2 = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier2 != null) {
            inputEventConsistencyVerifier2.onUnhandledEvent(event, 0);
        }
        return false;
    }

    private boolean dispatchGenericMotionEventInternal(MotionEvent event) {
        ListenerInfo li = this.mListenerInfo;
        if ((li != null && li.mOnGenericMotionListener != null && (this.mViewFlags & 32) == 0 && li.mOnGenericMotionListener.onGenericMotion(this, event)) || onGenericMotionEvent(event)) {
            return true;
        }
        int actionButton = event.getActionButton();
        int actionMasked = event.getActionMasked();
        if (actionMasked != 11) {
            if (actionMasked == 12 && this.mInContextButtonPress && (actionButton == 32 || actionButton == 2)) {
                this.mInContextButtonPress = false;
                this.mIgnoreNextUpEvent = true;
            }
        } else if (isContextClickable() && !this.mInContextButtonPress && !this.mHasPerformedLongPress && ((actionButton == 32 || actionButton == 2) && performContextClick(event.getX(), event.getY()))) {
            this.mInContextButtonPress = true;
            setPressed(true, event.getX(), event.getY());
            removeTapCallback();
            removeLongPressCallback();
            return true;
        }
        InputEventConsistencyVerifier inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier != null) {
            inputEventConsistencyVerifier.onUnhandledEvent(event, 0);
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
    public boolean pointInHoveredChild(MotionEvent event) {
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

    @UnsupportedAppUsage
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
        if (!hasWindowFocus) {
            if (isPressed()) {
                setPressed(false);
            }
            this.mPrivateFlags3 &= -131073;
            if ((this.mPrivateFlags & 2) != 0) {
                notifyFocusChangeToInputMethodManager(false);
            }
            removeLongPressCallback();
            removeTapCallback();
            onFocusLost();
        } else if ((this.mPrivateFlags & 2) != 0) {
            notifyFocusChangeToInputMethodManager(true);
        }
        refreshDrawableState();
    }

    public boolean hasWindowFocus() {
        AttachInfo attachInfo = this.mAttachInfo;
        return attachInfo != null && attachInfo.mHasWindowFocus;
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
        return thisVisible && isVisible;
    }

    public void onVisibilityAggregated(boolean isVisible) {
        int i;
        int i2;
        AutofillManager afm;
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
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        Drawable fg = foregroundInfo != null ? foregroundInfo.mDrawable : null;
        if (!(fg == null || isVisible == fg.isVisible())) {
            fg.setVisible(isVisible, false);
        }
        if (isAutofillable() && (afm = getAutofillManager()) != null && getAutofillViewId() > 1073741823) {
            Handler handler = this.mVisibilityChangeForAutofillHandler;
            if (handler != null) {
                handler.removeMessages(0);
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
        if (isAccessibilityPane() && isVisible != oldVisible) {
            if (isVisible) {
                i2 = 16;
            } else {
                i2 = 32;
            }
            notifyViewAccessibilityStateChangedIfNeeded(i2);
        }
    }

    public int getWindowVisibility() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mWindowVisibility;
        }
        return 8;
    }

    public void getWindowVisibleDisplayFrame(Rect outRect) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            try {
                attachInfo.mSession.getDisplayFrame(this.mAttachInfo.mWindow, outRect);
                Rect insets = this.mAttachInfo.mVisibleInsets;
                outRect.left += insets.left;
                outRect.top += insets.top;
                outRect.right -= insets.right;
                outRect.bottom -= insets.bottom;
            } catch (RemoteException e) {
            }
        } else {
            DisplayManagerGlobal.getInstance().getRealDisplay(0).getRectSize(outRect);
            Resources resources = this.mResources;
            if (resources != null) {
                CompatibilityInfo info = resources.getCompatibilityInfo();
                if (!info.supportsScreen()) {
                    outRect.scale(info.getSdrLowResolutionRatio());
                }
            }
        }
    }

    @UnsupportedAppUsage
    public void getWindowDisplayFrame(Rect outRect) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            try {
                attachInfo.mSession.getDisplayFrame(this.mAttachInfo.mWindow, outRect);
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
            if (this.isHwForceDarkState) {
                attachInfo.mSystemUiVisibility |= HwFrameworkFactory.getHwForceDarkManager().updateHwForceDarkSystemUIVisibility(this.mSystemUiVisibility);
            } else {
                attachInfo.mSystemUiVisibility |= this.mSystemUiVisibility;
            }
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
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mInTouchMode;
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

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.isConfirmKey(keyCode)) {
            if ((this.mViewFlags & 32) == 32) {
                return true;
            }
            if (event.getRepeatCount() == 0) {
                int i = this.mViewFlags;
                boolean clickable = (i & 16384) == 16384 || (i & 2097152) == 2097152;
                if (clickable || (this.mViewFlags & 1073741824) == 1073741824) {
                    float x = ((float) getWidth()) / 2.0f;
                    float y = ((float) getHeight()) / 2.0f;
                    if (clickable) {
                        setPressed(true, x, y);
                    }
                    checkForLongClick((long) ViewConfiguration.getLongPressTimeout(), x, y, 0);
                    return true;
                }
            }
        }
        return false;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.isConfirmKey(keyCode)) {
            int i = this.mViewFlags;
            if ((i & 32) == 32) {
                return true;
            }
            if ((i & 16384) == 16384 && isPressed()) {
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

    @Override // android.view.KeyEvent.Callback
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
        ViewParent viewParent = this.mParent;
        if (viewParent != null) {
            viewParent.createContextMenu(menu);
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

    private boolean dispatchTouchExplorationHoverEvent(MotionEvent event) {
        AccessibilityManager manager = AccessibilityManager.getInstance(this.mContext);
        if (!manager.isEnabled() || !manager.isTouchExplorationEnabled()) {
            return false;
        }
        boolean oldHoveringTouchDelegate = this.mHoveringTouchDelegate;
        int action = event.getActionMasked();
        boolean pointInDelegateRegion = false;
        AccessibilityNodeInfo.TouchDelegateInfo info = this.mTouchDelegate.getTouchDelegateInfo();
        for (int i = 0; i < info.getRegionCount(); i++) {
            if (info.getRegionAt(i).contains((int) event.getX(), (int) event.getY())) {
                pointInDelegateRegion = true;
            }
        }
        if (!oldHoveringTouchDelegate) {
            if ((action == 9 || action == 7) && !pointInHoveredChild(event) && pointInDelegateRegion) {
                this.mHoveringTouchDelegate = true;
            }
        } else if (action == 10 || (action == 7 && (pointInHoveredChild(event) || !pointInDelegateRegion))) {
            this.mHoveringTouchDelegate = false;
        }
        if (action != 7) {
            if (action != 9) {
                if (!(action == 10 && oldHoveringTouchDelegate)) {
                    return false;
                }
                this.mTouchDelegate.onTouchExplorationHoverEvent(event);
                return false;
            } else if (oldHoveringTouchDelegate || !this.mHoveringTouchDelegate) {
                return false;
            } else {
                return this.mTouchDelegate.onTouchExplorationHoverEvent(event);
            }
        } else if (oldHoveringTouchDelegate && this.mHoveringTouchDelegate) {
            return this.mTouchDelegate.onTouchExplorationHoverEvent(event);
        } else {
            if (!oldHoveringTouchDelegate && this.mHoveringTouchDelegate) {
                MotionEvent eventNoHistory = event.getHistorySize() == 0 ? event : MotionEvent.obtainNoHistory(event);
                eventNoHistory.setAction(9);
                boolean handled = this.mTouchDelegate.onTouchExplorationHoverEvent(eventNoHistory);
                eventNoHistory.setAction(action);
                return handled | this.mTouchDelegate.onTouchExplorationHoverEvent(eventNoHistory);
            } else if (!oldHoveringTouchDelegate || this.mHoveringTouchDelegate) {
                return false;
            } else {
                boolean hoverExitPending = event.isHoverExitPending();
                event.setHoverExitPending(true);
                this.mTouchDelegate.onTouchExplorationHoverEvent(event);
                MotionEvent eventNoHistory2 = event.getHistorySize() == 0 ? event : MotionEvent.obtainNoHistory(event);
                eventNoHistory2.setHoverExitPending(hoverExitPending);
                eventNoHistory2.setAction(10);
                this.mTouchDelegate.onTouchExplorationHoverEvent(eventNoHistory2);
                return false;
            }
        }
    }

    public boolean onHoverEvent(MotionEvent event) {
        if (this.mTouchDelegate != null && dispatchTouchExplorationHoverEvent(event)) {
            return true;
        }
        int action = event.getActionMasked();
        if (!this.mSendingHoverAccessibilityEvents) {
            if ((action == 9 || action == 7) && !hasHoveredChild() && pointInView(event.getX(), event.getY())) {
                sendAccessibilityHoverEvent(128);
                this.mSendingHoverAccessibilityEvents = true;
            }
        } else if (action == 10 || (action == 7 && !pointInView(event.getX(), event.getY()))) {
            this.mSendingHoverAccessibilityEvents = false;
            sendAccessibilityHoverEvent(256);
        }
        if ((action == 9 || action == 7) && event.isFromSource(8194) && isOnScrollbar(event.getX(), event.getY())) {
            awakenScrollBars();
        }
        if (!isHoverable() && !isHovered()) {
            return false;
        }
        if (action == 9) {
            setHovered(true);
        } else if (action == 10) {
            setHovered(false);
        }
        dispatchGenericMotionEventInternal(event);
        return true;
    }

    private boolean isHoverable() {
        int viewFlags = this.mViewFlags;
        if ((viewFlags & 32) == 32) {
            return false;
        }
        if ((viewFlags & 16384) == 16384 || (viewFlags & 2097152) == 2097152 || (viewFlags & 8388608) == 8388608) {
            return true;
        }
        return false;
    }

    @ViewDebug.ExportedProperty
    public boolean isHovered() {
        return (this.mPrivateFlags & 268435456) != 0;
    }

    public void setHovered(boolean hovered) {
        if (hovered) {
            int i = this.mPrivateFlags;
            if ((i & 268435456) == 0) {
                this.mPrivateFlags = 268435456 | i;
                refreshDrawableState();
                onHoverChanged(true);
                return;
            }
            return;
        }
        int i2 = this.mPrivateFlags;
        if ((268435456 & i2) != 0) {
            this.mPrivateFlags = -268435457 & i2;
            refreshDrawableState();
            onHoverChanged(false);
        }
    }

    public void onHoverChanged(boolean hovered) {
    }

    /* access modifiers changed from: protected */
    public boolean handleScrollBarDragging(MotionEvent event) {
        if (this.mScrollCache == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        if ((this.mScrollCache.mScrollBarDraggingState != 0 || action == 0) && event.isFromSource(8194) && event.isButtonPressed(1)) {
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
                            return true;
                        }
                        int newY = Math.round((((float) (range - extent)) / (((float) extent) / ((float) height))) * (newThumbOffset / maxThumbOffset));
                        if (newY == getScrollY()) {
                            return true;
                        }
                        this.mScrollCache.mScrollBarDraggingPos = y;
                        setScrollY(newY);
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
                            return true;
                        }
                        int newX = Math.round((((float) (range2 - extent2)) / (((float) extent2) / ((float) width))) * (newThumbOffset2 / maxThumbOffset2));
                        if (newX == getScrollX()) {
                            return true;
                        }
                        this.mScrollCache.mScrollBarDraggingPos = x;
                        setScrollX(newX);
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
                ScrollabilityCache scrollabilityCache = this.mScrollCache;
                scrollabilityCache.mScrollBarDraggingState = 1;
                scrollabilityCache.mScrollBarDraggingPos = y;
                return true;
            }
            if (isOnHorizontalScrollbarThumb(x, y)) {
                ScrollabilityCache scrollabilityCache2 = this.mScrollCache;
                scrollabilityCache2.mScrollBarDraggingState = 2;
                scrollabilityCache2.mScrollBarDraggingPos = x;
                return true;
            }
            this.mScrollCache.mScrollBarDraggingState = 0;
            return false;
        }
        this.mScrollCache.mScrollBarDraggingState = 0;
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int touchSlop;
        int motionClassification;
        int touchSlop2;
        float x = event.getX();
        float y = event.getY();
        int viewFlags = this.mViewFlags;
        int action = event.getAction();
        boolean deepPress = false;
        boolean clickable = (viewFlags & 16384) == 16384 || (viewFlags & 2097152) == 2097152 || (viewFlags & 8388608) == 8388608;
        if ((viewFlags & 32) == 32) {
            if (action == 1 && (this.mPrivateFlags & 16384) != 0) {
                setPressed(false);
            }
            this.mPrivateFlags3 &= -131073;
            return clickable;
        }
        TouchDelegate touchDelegate = this.mTouchDelegate;
        if (touchDelegate != null) {
            if (touchDelegate.onTouchEvent(event)) {
                return true;
            }
        }
        if (!clickable && (viewFlags & 1073741824) != 1073741824) {
            return false;
        }
        if (action == 0) {
            if (event.getSource() == 4098) {
                this.mPrivateFlags3 |= 131072;
            }
            this.mHasPerformedLongPress = false;
            if (!clickable) {
                checkForLongClick((long) ViewConfiguration.getLongPressTimeout(), x, y, 3);
                return true;
            } else if (performButtonActionOnTouchDown(event)) {
                return true;
            } else {
                if (isInScrollingContainer()) {
                    this.mPrivateFlags |= 33554432;
                    if (this.mPendingCheckForTap == null) {
                        this.mPendingCheckForTap = new CheckForTap();
                    }
                    this.mPendingCheckForTap.x = event.getX();
                    this.mPendingCheckForTap.y = event.getY();
                    postDelayed(this.mPendingCheckForTap, (long) ViewConfiguration.getTapTimeout());
                    return true;
                }
                setPressed(true, x, y);
                checkForLongClick((long) ViewConfiguration.getLongPressTimeout(), x, y, 3);
                return true;
            }
        } else if (action == 1) {
            this.mPrivateFlags3 &= -131073;
            if ((viewFlags & 1073741824) == 1073741824) {
                handleTooltipUp();
            }
            if (!clickable) {
                removeTapCallback();
                removeLongPressCallback();
                this.mInContextButtonPress = false;
                this.mHasPerformedLongPress = false;
                this.mIgnoreNextUpEvent = false;
                return true;
            }
            boolean prepressed = (this.mPrivateFlags & 33554432) != 0;
            if ((this.mPrivateFlags & 16384) != 0 || prepressed) {
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
                        if (HwFrameworkFactory.getHwView() != null) {
                            HwFrameworkFactory.getHwView().scheduleFrameNow(prepressed, this);
                        }
                        if (this.mPerformClick == null) {
                            this.mPerformClick = new PerformClick();
                        }
                        if (!post(this.mPerformClick)) {
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
            return true;
        } else if (action == 2) {
            if (clickable) {
                drawableHotspotChanged(x, y);
            }
            int motionClassification2 = event.getClassification();
            boolean ambiguousGesture = motionClassification2 == 1;
            int touchSlop3 = this.mTouchSlop;
            if (!ambiguousGesture || !hasPendingLongPressCallback()) {
                touchSlop = touchSlop3;
                motionClassification = motionClassification2;
            } else {
                float ambiguousMultiplier = ViewConfiguration.getAmbiguousGestureMultiplier();
                if (!pointInView(x, y, (float) touchSlop3)) {
                    removeLongPressCallback();
                    motionClassification = motionClassification2;
                    touchSlop2 = touchSlop3;
                    checkForLongClick(((long) (((float) ViewConfiguration.getLongPressTimeout()) * ambiguousMultiplier)) - (event.getEventTime() - event.getDownTime()), x, y, 3);
                } else {
                    touchSlop2 = touchSlop3;
                    motionClassification = motionClassification2;
                }
                touchSlop = (int) (((float) touchSlop2) * ambiguousMultiplier);
            }
            if (!pointInView(x, y, (float) touchSlop)) {
                removeTapCallback();
                removeLongPressCallback();
                if ((this.mPrivateFlags & 16384) != 0) {
                    setPressed(false);
                }
                this.mPrivateFlags3 &= -131073;
            }
            if (motionClassification == 2) {
                deepPress = true;
            }
            if (!deepPress || !hasPendingLongPressCallback()) {
                return true;
            }
            removeLongPressCallback();
            checkForLongClick(0, x, y, 4);
            return true;
        } else if (action != 3) {
            return true;
        } else {
            if (clickable) {
                setPressed(false);
            }
            removeTapCallback();
            removeLongPressCallback();
            this.mInContextButtonPress = false;
            this.mHasPerformedLongPress = false;
            this.mIgnoreNextUpEvent = false;
            this.mPrivateFlags3 &= -131073;
            return true;
        }
    }

    @UnsupportedAppUsage
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
        CheckForLongPress checkForLongPress = this.mPendingCheckForLongPress;
        if (checkForLongPress != null) {
            removeCallbacks(checkForLongPress);
        }
    }

    private boolean hasPendingLongPressCallback() {
        AttachInfo attachInfo;
        if (this.mPendingCheckForLongPress == null || (attachInfo = this.mAttachInfo) == null) {
            return false;
        }
        return attachInfo.mHandler.hasCallbacks(this.mPendingCheckForLongPress);
    }

    @UnsupportedAppUsage
    private void removePerformClickCallback() {
        PerformClick performClick = this.mPerformClick;
        if (performClick != null) {
            removeCallbacks(performClick);
        }
    }

    private void removeUnsetPressCallback() {
        if ((this.mPrivateFlags & 16384) != 0 && this.mUnsetPressedState != null) {
            setPressed(false);
            removeCallbacks(this.mUnsetPressedState);
        }
    }

    private void removeTapCallback() {
        CheckForTap checkForTap = this.mPendingCheckForTap;
        if (checkForTap != null) {
            this.mPrivateFlags &= -33554433;
            removeCallbacks(checkForTap);
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
        if (SystemProperties.getBoolean("hw_sc.view.disable_unbuffered_dispatch", false)) {
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo == null || attachInfo.mViewRootImpl == null || this.mAttachInfo.mViewRootImpl.mWindowAttributes == null) {
                Log.i(VIEW_LOG_TAG, "requestUnbufferedDispatch window unknown");
                return;
            }
            Log.i(VIEW_LOG_TAG, "requestUnbufferedDispatch window " + ((Object) this.mAttachInfo.mViewRootImpl.mWindowAttributes.getTitle()));
            return;
        }
        int action = event.getAction();
        if (this.mAttachInfo == null) {
            return;
        }
        if ((action == 0 || action == 2) && event.isTouchEvent()) {
            this.mAttachInfo.mUnbufferedDispatchRequested = true;
        }
    }

    private boolean hasSize() {
        return this.mBottom > this.mTop && this.mRight > this.mLeft;
    }

    private boolean canTakeFocus() {
        int i = this.mViewFlags;
        if ((i & 12) == 0 && (i & 1) == 1 && (i & 32) == 0 && (sCanFocusZeroSized || !isLayoutValid() || hasSize())) {
            return true;
        }
        return false;
    }

    /* JADX INFO: Multiple debug info for r5v5 int: [D('newVisibility' int), D('viewRootImpl' android.view.ViewRootImpl)] */
    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void setFlags(int flags, int mask) {
        AttachInfo attachInfo;
        ForegroundInfo foregroundInfo;
        ViewParent viewParent;
        int newFocus;
        boolean accessibilityEnabled = AccessibilityManager.getInstance(this.mContext).isEnabled();
        boolean oldIncludeForAccessibility = accessibilityEnabled && includeForAccessibility();
        int old = this.mViewFlags;
        this.mViewFlags = (this.mViewFlags & (~mask)) | (flags & mask);
        int i = this.mViewFlags;
        int changed = i ^ old;
        if (changed != 0) {
            int privateFlags = this.mPrivateFlags;
            boolean shouldNotifyFocusableAvailable = false;
            int focusableChangedByAuto = 0;
            if (!((i & 16) == 0 || (changed & BatteryStats.HistoryItem.EVENT_TEMP_WHITELIST_FINISH) == 0)) {
                if ((i & 16384) != 0) {
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
                    ViewParent viewParent2 = this.mParent;
                    if (viewParent2 instanceof ViewGroup) {
                        ((ViewGroup) viewParent2).clearFocusedInCluster();
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
            if (shouldNotifyFocusableAvailable && (viewParent = this.mParent) != null) {
                viewParent.focusableViewAvailable(this);
            }
            if ((changed & 8) != 0) {
                needGlobalAttributesUpdate(false);
                requestLayout();
                if ((this.mViewFlags & 12) == 8) {
                    if (hasFocus()) {
                        clearFocus();
                        ViewParent viewParent3 = this.mParent;
                        if (viewParent3 instanceof ViewGroup) {
                            ((ViewGroup) viewParent3).clearFocusedInCluster();
                        }
                    }
                    clearAccessibilityFocus();
                    destroyDrawingCache();
                    ViewParent viewParent4 = this.mParent;
                    if (viewParent4 instanceof View) {
                        ((View) viewParent4).invalidate(true);
                    }
                    this.mPrivateFlags |= 32;
                }
                AttachInfo attachInfo2 = this.mAttachInfo;
                if (attachInfo2 != null) {
                    attachInfo2.mViewVisibilityChanged = true;
                }
            }
            if ((changed & 4) != 0) {
                needGlobalAttributesUpdate(false);
                this.mPrivateFlags |= 32;
                if ((this.mViewFlags & 12) == 4 && getRootView() != this) {
                    if (hasFocus()) {
                        clearFocus();
                        ViewParent viewParent5 = this.mParent;
                        if (viewParent5 instanceof ViewGroup) {
                            ((ViewGroup) viewParent5).clearFocusedInCluster();
                        }
                    }
                    clearAccessibilityFocus();
                }
                AttachInfo attachInfo3 = this.mAttachInfo;
                if (attachInfo3 != null) {
                    attachInfo3.mViewVisibilityChanged = true;
                }
            }
            if ((changed & 12) != 0) {
                if (!(newVisibility == 0 || this.mAttachInfo == null)) {
                    cleanupDraw();
                }
                ViewParent viewParent6 = this.mParent;
                if (viewParent6 instanceof ViewGroup) {
                    ViewGroup parent = (ViewGroup) viewParent6;
                    parent.onChildVisibilityChanged(this, changed & 12, newVisibility);
                    parent.invalidate(true);
                } else if (viewParent6 != null) {
                    viewParent6.invalidateChild(this, null);
                }
                if (this.mAttachInfo != null) {
                    dispatchVisibilityChanged(this, newVisibility);
                    if (this.mParent != null && getWindowVisibility() == 0) {
                        ViewParent viewParent7 = this.mParent;
                        if (!(viewParent7 instanceof ViewGroup) || ((ViewGroup) viewParent7).isShown()) {
                            dispatchVisibilityAggregated(newVisibility == 0);
                        }
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
                } else if (this.mBackground == null && this.mDefaultFocusHighlight == null && ((foregroundInfo = this.mForegroundInfo) == null || foregroundInfo.mDrawable == null)) {
                    this.mPrivateFlags |= 128;
                } else {
                    this.mPrivateFlags &= -129;
                }
                requestLayout();
                invalidate(true);
            }
            if (!((67108864 & changed) == 0 || this.mParent == null || (attachInfo = this.mAttachInfo) == null || attachInfo.mRecomputeGlobalAttributes)) {
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
        ViewParent viewParent = this.mParent;
        if (viewParent != null) {
            viewParent.bringChildToFront(this);
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
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        if (foregroundInfo != null) {
            foregroundInfo.mBoundsChanged = true;
        }
        AttachInfo ai = this.mAttachInfo;
        if (ai != null) {
            ai.mViewScrollChanged = true;
        }
        ListenerInfo listenerInfo = this.mListenerInfo;
        if (listenerInfo != null && listenerInfo.mOnScrollChangeListener != null) {
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

    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    public final int getWidth() {
        return this.mRight - this.mLeft;
    }

    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    public final int getHeight() {
        return this.mBottom - this.mTop;
    }

    public void getDrawingRect(Rect outRect) {
        int i = this.mScrollX;
        outRect.left = i;
        int i2 = this.mScrollY;
        outRect.top = i2;
        outRect.right = i + (this.mRight - this.mLeft);
        outRect.bottom = i2 + (this.mBottom - this.mTop);
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
        return (this.mMeasuredWidth & -16777216) | ((this.mMeasuredHeight >> 16) & -256);
    }

    public Matrix getMatrix() {
        ensureTransformationInfo();
        Matrix matrix = this.mTransformationInfo.mMatrix;
        this.mRenderNode.getMatrix(matrix);
        return matrix;
    }

    @UnsupportedAppUsage
    public final boolean hasIdentityMatrix() {
        return this.mRenderNode.hasIdentityMatrix();
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void ensureTransformationInfo() {
        if (this.mTransformationInfo == null) {
            this.mTransformationInfo = new TransformationInfo();
        }
    }

    @UnsupportedAppUsage
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
        return this.mRenderNode.getCameraDistance() * ((float) this.mResources.getDisplayMetrics().densityDpi);
    }

    public void setCameraDistance(float distance) {
        invalidateViewProperty(true, false);
        this.mRenderNode.setCameraDistance(Math.abs(distance) / ((float) this.mResources.getDisplayMetrics().densityDpi));
        invalidateViewProperty(false, false);
        invalidateParentIfNeededAndWasQuickRejected();
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getRotation() {
        return this.mRenderNode.getRotationZ();
    }

    public void setRotation(float rotation) {
        if (rotation != getRotation()) {
            invalidateViewProperty(true, false);
            this.mRenderNode.setRotationZ(rotation);
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
        TransformationInfo transformationInfo = this.mTransformationInfo;
        if (transformationInfo != null) {
            return transformationInfo.mAlpha;
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
        int i = this.mPrivateFlags3;
        if ((16777216 & i) != 0) {
            return (i & 8388608) != 0;
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

    public void setShadowClip(boolean isShadowClip) {
        this.mRenderNode.setShadowClip(isShadowClip);
    }

    public boolean getShadowClip() {
        return this.mRenderNode.getShadowClip();
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123768435)
    public boolean setAlphaNoInvalidation(float alpha) {
        ensureTransformationInfo();
        if (this.mTransformationInfo.mAlpha == alpha) {
            return false;
        }
        setAlphaInternal(alpha);
        if (onSetAlpha((int) (255.0f * alpha))) {
            this.mPrivateFlags |= 262144;
            return true;
        }
        this.mPrivateFlags &= -262145;
        this.mRenderNode.setAlpha(getFinalAlpha());
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setAlphaInternal(float alpha) {
        float oldAlpha = this.mTransformationInfo.mAlpha;
        this.mTransformationInfo.mAlpha = alpha;
        boolean z = true;
        boolean z2 = alpha == 0.0f;
        if (oldAlpha != 0.0f) {
            z = false;
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
        TransformationInfo transformationInfo = this.mTransformationInfo;
        if (transformationInfo != null) {
            return transformationInfo.mAlpha * this.mTransformationInfo.mTransitionAlpha;
        }
        return 1.0f;
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public float getTransitionAlpha() {
        TransformationInfo transformationInfo = this.mTransformationInfo;
        if (transformationInfo != null) {
            return transformationInfo.mTransitionAlpha;
        }
        return 1.0f;
    }

    public void setForceDarkAllowed(boolean allow) {
        if (this.mRenderNode.setForceDarkAllowed(allow)) {
            invalidate();
        }
        this.isAndroidForceDarkState = allow;
    }

    @ViewDebug.ExportedProperty(category = "drawing")
    public boolean isForceDarkAllowed() {
        return this.mRenderNode.isForceDarkAllowed();
    }

    @ViewDebug.CapturedViewProperty
    public final int getTop() {
        return this.mTop;
    }

    public final void setTop(int top) {
        int minTop;
        int yLoc;
        if (top != this.mTop) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidate(true);
            } else if (this.mAttachInfo != null) {
                int i = this.mTop;
                if (top < i) {
                    minTop = top;
                    yLoc = top - i;
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
            ForegroundInfo foregroundInfo = this.mForegroundInfo;
            if (foregroundInfo != null) {
                foregroundInfo.mBoundsChanged = true;
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
        return (this.mPrivateFlags & 2097152) != 0;
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
            ForegroundInfo foregroundInfo = this.mForegroundInfo;
            if (foregroundInfo != null) {
                foregroundInfo.mBoundsChanged = true;
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
        int minLeft;
        int xLoc;
        if (left != this.mLeft) {
            boolean matrixIsIdentity = hasIdentityMatrix();
            if (!matrixIsIdentity) {
                invalidate(true);
            } else if (this.mAttachInfo != null) {
                int i = this.mLeft;
                if (left < i) {
                    minLeft = left;
                    xLoc = left - i;
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
            ForegroundInfo foregroundInfo = this.mForegroundInfo;
            if (foregroundInfo != null) {
                foregroundInfo.mBoundsChanged = true;
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
            ForegroundInfo foregroundInfo = this.mForegroundInfo;
            if (foregroundInfo != null) {
                foregroundInfo.mBoundsChanged = true;
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

    public void setShadowStyle(int size, int mode, int device) {
        if (this.mHwShadowManager == null) {
            this.mHwShadowManager = HwFrameworkFactory.getHwShadowManager(getContext());
        }
        IHwShadowManager iHwShadowManager = this.mHwShadowManager;
        if (iHwShadowManager != null && iHwShadowManager.setShadowStyle(this.mRenderNode, size, mode, device)) {
            invalidateViewProperty(false, false);
        }
    }

    @ViewDebug.ExportedProperty(category = "drawing")
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

    public Matrix getAnimationMatrix() {
        return this.mRenderNode.getAnimationMatrix();
    }

    public StateListAnimator getStateListAnimator() {
        return this.mStateListAnimator;
    }

    public void setStateListAnimator(StateListAnimator stateListAnimator) {
        StateListAnimator stateListAnimator2 = this.mStateListAnimator;
        if (stateListAnimator2 != stateListAnimator) {
            if (stateListAnimator2 != null) {
                stateListAnimator2.setTarget(null);
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
        if (providerInt == 0) {
            setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        } else if (providerInt == 1) {
            setOutlineProvider(null);
        } else if (providerInt == 2) {
            setOutlineProvider(ViewOutlineProvider.BOUNDS);
        } else if (providerInt == 3) {
            setOutlineProvider(ViewOutlineProvider.PADDED_BOUNDS);
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
            ViewOutlineProvider viewOutlineProvider = this.mOutlineProvider;
            if (viewOutlineProvider == null) {
                this.mRenderNode.setOutline(null);
                return;
            }
            if (viewOutlineProvider == ViewOutlineProvider.HW_MULTIWINDOW_FREEFORM_OUTLINE_PROVIDER || this.mOutlineProvider == ViewOutlineProvider.HW_MULTIWINDOW_SPLITSCREEN_OUTLINE_PROVIDER || this.mOutlineProvider == ViewOutlineProvider.HW_MULTIWINDOW_SPLITSCREEN_BLACK_OUTLINE_PROVIDER) {
                setClipToOutline(true);
            }
            Outline outline = this.mAttachInfo.mTmpOutline;
            outline.setEmpty();
            outline.setAlpha(1.0f);
            this.mOutlineProvider.getOutline(this, outline);
            this.mRenderNode.setOutline(outline);
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
        AttachInfo attachInfo;
        if (hasIdentityMatrix() || (attachInfo = this.mAttachInfo) == null) {
            outRect.set(this.mLeft, this.mTop, this.mRight, this.mBottom);
            return;
        }
        RectF tmpRect = attachInfo.mTmpTransformRect;
        tmpRect.set(0.0f, 0.0f, (float) getWidth(), (float) getHeight());
        getMatrix().mapRect(tmpRect);
        outRect.set(((int) tmpRect.left) + this.mLeft, ((int) tmpRect.top) + this.mTop, ((int) tmpRect.right) + this.mLeft, ((int) tmpRect.bottom) + this.mTop);
    }

    /* access modifiers changed from: package-private */
    public final boolean pointInView(float localX, float localY) {
        return pointInView(localX, localY, 0.0f);
    }

    @UnsupportedAppUsage
    public boolean pointInView(float localX, float localY, float slop) {
        return localX >= (-slop) && localY >= (-slop) && localX < ((float) (this.mRight - this.mLeft)) + slop && localY < ((float) (this.mBottom - this.mTop)) + slop;
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
        ViewParent viewParent = this.mParent;
        if (viewParent == null || viewParent.getChildVisibleRect(this, r, globalOffset)) {
            return true;
        }
        return false;
    }

    public final boolean getGlobalVisibleRect(Rect r) {
        return getGlobalVisibleRect(r, null);
    }

    public final boolean getLocalVisibleRect(Rect r) {
        AttachInfo attachInfo = this.mAttachInfo;
        Point offset = attachInfo != null ? attachInfo.mPoint : new Point();
        if (!getGlobalVisibleRect(r, offset)) {
            return false;
        }
        r.offset(-offset.x, -offset.y);
        return true;
    }

    public void offsetTopAndBottom(int offset) {
        AttachInfo attachInfo;
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
                if (!(p == null || (attachInfo = this.mAttachInfo) == null)) {
                    Rect r = attachInfo.mTmpInvalRect;
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
        AttachInfo attachInfo;
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
                if (!(p == null || (attachInfo = this.mAttachInfo) == null)) {
                    Rect r = attachInfo.mTmpInvalRect;
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
            ViewParent viewParent = this.mParent;
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).onSetLayoutParams(this, params);
            }
            requestLayout();
            return;
        }
        throw new NullPointerException("Layout parameters cannot be null");
    }

    public void resolveLayoutParams() {
        ViewGroup.LayoutParams layoutParams = this.mLayoutParams;
        if (layoutParams != null) {
            layoutParams.resolveLayoutDirection(getLayoutDirection());
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
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (scrollabilityCache == null || !awakenScrollBars(scrollabilityCache.scrollBarDefaultDelayBeforeFade, true)) {
            return false;
        }
        return true;
    }

    private boolean initialAwakenScrollBars() {
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (scrollabilityCache == null || !awakenScrollBars(scrollabilityCache.scrollBarDefaultDelayBeforeFade * 4, true)) {
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
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            attachInfo.mHandler.removeCallbacks(scrollCache);
            this.mAttachInfo.mHandler.postAtTime(scrollCache, fadeStartTime);
        }
        return true;
    }

    private boolean skipInvalidate() {
        if ((this.mViewFlags & 12) != 0 && this.mCurrentAnimation == null) {
            ViewParent viewParent = this.mParent;
            if (!(viewParent instanceof ViewGroup) || !((ViewGroup) viewParent).isViewTransitioning(this)) {
                return true;
            }
        }
        return false;
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

    @UnsupportedAppUsage
    public void invalidate(boolean invalidateCache) {
        invalidateInternal(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop, invalidateCache, true);
    }

    /* access modifiers changed from: package-private */
    public void invalidateInternal(int l, int t, int r, int b, boolean invalidateCache, boolean fullInvalidate) {
        View receiver;
        GhostView ghostView = this.mGhostView;
        if (ghostView != null) {
            ghostView.invalidate(true);
        } else if (!skipInvalidate()) {
            this.mCachedContentCaptureSession = null;
            int i = this.mPrivateFlags;
            if ((i & 48) == 48 || ((invalidateCache && (i & 32768) == 32768) || (this.mPrivateFlags & Integer.MIN_VALUE) != Integer.MIN_VALUE || (fullInvalidate && isOpaque() != this.mLastIsOpaque))) {
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
                    HwFrameworkFactory.getHwApsImpl().savePartialUpdateDirty(this.mCurrentDirtyRect, l, t, r, b, getContext(), getClass().getSimpleName());
                    p.invalidateChild(this, damage);
                }
                Drawable drawable = this.mBackground;
                if (drawable != null && drawable.isProjected() && (receiver = getProjectionReceiver()) != null) {
                    receiver.damageInParent();
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
    @UnsupportedAppUsage
    public void invalidateViewProperty(boolean invalidateParent, boolean forceRedraw) {
        if (!isHardwareAccelerated() || !this.mRenderNode.hasDisplayList() || (this.mPrivateFlags & 64) != 0) {
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
        ViewParent viewParent = this.mParent;
        if (viewParent != null && this.mAttachInfo != null) {
            viewParent.onDescendantInvalidated(this, this);
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void invalidateParentCaches() {
        ViewParent viewParent = this.mParent;
        if (viewParent instanceof View) {
            ((View) viewParent).mPrivateFlags |= Integer.MIN_VALUE;
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void invalidateParentIfNeeded() {
        if (isHardwareAccelerated()) {
            ViewParent viewParent = this.mParent;
            if (viewParent instanceof View) {
                ((View) viewParent).invalidate(true);
            }
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
    @UnsupportedAppUsage
    public void computeOpaqueFlags() {
        Drawable drawable = this.mBackground;
        if (drawable == null || drawable.getOpacity() != -1) {
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

    @UnsupportedAppUsage
    public ViewRootImpl getViewRootImpl() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mViewRootImpl;
        }
        return null;
    }

    @UnsupportedAppUsage
    public ThreadedRenderer getThreadedRenderer() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mThreadedRenderer;
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

    public int getFadingEdge() {
        return this.mViewFlags & 12288;
    }

    public int getFadingEdgeLength() {
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (scrollabilityCache == null || (this.mViewFlags & 12288) == 0) {
            return 0;
        }
        return scrollabilityCache.fadingEdgeLength;
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
    @UnsupportedAppUsage
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
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        return scrollabilityCache != null && scrollabilityCache.fadeScrollBars;
    }

    public int getScrollBarDefaultDelayBeforeFade() {
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (scrollabilityCache == null) {
            return ViewConfiguration.getScrollDefaultDelay();
        }
        return scrollabilityCache.scrollBarDefaultDelayBeforeFade;
    }

    public void setScrollBarDefaultDelayBeforeFade(int scrollBarDefaultDelayBeforeFade) {
        getScrollCache().scrollBarDefaultDelayBeforeFade = scrollBarDefaultDelayBeforeFade;
    }

    public int getScrollBarFadeDuration() {
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (scrollabilityCache == null) {
            return ViewConfiguration.getScrollBarFadeDuration();
        }
        return scrollabilityCache.scrollBarFadeDuration;
    }

    public void setScrollBarFadeDuration(int scrollBarFadeDuration) {
        getScrollCache().scrollBarFadeDuration = scrollBarFadeDuration;
    }

    public int getScrollBarSize() {
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (scrollabilityCache == null) {
            return ViewConfiguration.get(this.mContext).getScaledScrollBarSize();
        }
        return scrollabilityCache.scrollBarSize;
    }

    public void setScrollBarSize(int scrollBarSize) {
        getScrollCache().scrollBarSize = scrollBarSize;
    }

    public void setScrollBarStyle(int style) {
        int i = this.mViewFlags;
        if (style != (i & 50331648)) {
            this.mViewFlags = (i & -50331649) | (50331648 & style);
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
        int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) {
            return false;
        }
        if (direction < 0) {
            if (offset > 0) {
                return true;
            }
            return false;
        } else if (offset < range - 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean canScrollVertically(int direction) {
        int offset = computeVerticalScrollOffset();
        int range = computeVerticalScrollRange() - computeVerticalScrollExtent();
        if (range == 0) {
            return false;
        }
        if (direction < 0) {
            if (offset > 0) {
                return true;
            }
            return false;
        } else if (offset < range - 1) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void getScrollIndicatorBounds(Rect out) {
        int i = this.mScrollX;
        out.left = i;
        out.right = (i + this.mRight) - this.mLeft;
        int i2 = this.mScrollY;
        out.top = i2;
        out.bottom = (i2 + this.mBottom) - this.mTop;
    }

    /* JADX INFO: Multiple debug info for r7v1 int: [D('canScrollLeft' boolean), D('rightMask' int)] */
    private void onDrawScrollIndicators(Canvas c) {
        Drawable dr;
        int rightRtl;
        int leftRtl;
        if ((this.mPrivateFlags3 & SCROLL_INDICATORS_PFLAG3_MASK) != 0 && (dr = this.mScrollIndicatorDrawable) != null) {
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
            int i = this.mScrollX;
            bounds.left = (this.mPaddingLeft & inside) + i;
            bounds.right = ((i + width) - (this.mUserPaddingRight & inside)) - verticalScrollBarGap;
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
    public void adjustVerticalScrollBarBounds(Rect bounds) {
    }

    /* access modifiers changed from: protected */
    public final void onDrawScrollBars(Canvas canvas) {
        boolean invalidate;
        ScrollBarDrawable scrollBar;
        ScrollabilityCache cache = this.mScrollCache;
        if (cache != null) {
            int state = cache.state;
            if (state != 0) {
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
                    invalidate = false;
                }
                boolean drawHorizontalScrollBar = isHorizontalScrollBarEnabled();
                boolean drawVerticalScrollBar = isVerticalScrollBarEnabled() && !isVerticalScrollBarHidden();
                if (this.mRoundScrollbarRenderer != null) {
                    if (drawVerticalScrollBar) {
                        Rect bounds = cache.mScrollBarBounds;
                        getVerticalScrollBarBounds(bounds, null);
                        this.mRoundScrollbarRenderer.drawRoundScrollbars(canvas, ((float) cache.scrollBar.getAlpha()) / 255.0f, bounds);
                        if (invalidate) {
                            invalidate();
                        }
                    }
                } else if (drawVerticalScrollBar || drawHorizontalScrollBar) {
                    ScrollBarDrawable scrollBar2 = cache.scrollBar;
                    if (drawHorizontalScrollBar) {
                        scrollBar2.setParameters(computeHorizontalScrollRange(), computeHorizontalScrollOffset(), computeHorizontalScrollExtent(), false);
                        Rect bounds2 = cache.mScrollBarBounds;
                        getHorizontalScrollBarBounds(bounds2, null);
                        scrollBar = scrollBar2;
                        onDrawHorizontalScrollBar(canvas, scrollBar2, bounds2.left, bounds2.top, bounds2.right, bounds2.bottom);
                        if (invalidate) {
                            invalidate(bounds2);
                        }
                    } else {
                        scrollBar = scrollBar2;
                    }
                    if (drawVerticalScrollBar) {
                        scrollBar.setParameters(computeVerticalScrollRange(), computeVerticalScrollOffset(), computeVerticalScrollExtent(), true);
                        Rect bounds3 = cache.mScrollBarBounds;
                        getVerticalScrollBarBounds(bounds3, null);
                        adjustVerticalScrollBarBounds(bounds3);
                        onDrawVerticalScrollBar(canvas, scrollBar, bounds3.left, bounds3.top, bounds3.right, bounds3.bottom);
                        if (invalidate) {
                            postInvalidateDelayed(SCROLLBAR_DELAYMILLIS, bounds3.left, bounds3.top, bounds3.right, bounds3.bottom);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isVerticalScrollBarHidden() {
        return false;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void onDrawHorizontalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t, int r, int b) {
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
        AccessibilityNodeIdManager.getInstance().registerViewWithId(this, getAccessibilityViewId());
        resetSubtreeAccessibilityStateChanged();
        rebuildOutline();
        if (isFocused()) {
            notifyFocusChangeToInputMethodManager(true);
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
        AttachInfo attachInfo = this.mAttachInfo;
        attachInfo.mDisplay = display;
        attachInfo.mDisplayState = display.getState();
        onMovedToDisplay(display.getDisplayId(), config);
    }

    public void onMovedToDisplay(int displayId, Configuration config) {
    }

    @UnsupportedAppUsage
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
            int i = this.mPrivateFlags2;
            int i2 = (i & 12) >> 2;
            if (i2 == 1) {
                this.mPrivateFlags2 = i | 16;
            } else if (i2 != 2) {
                if (i2 == 3 && 1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
                    this.mPrivateFlags2 |= 16;
                }
            } else if (!canResolveLayoutDirection()) {
                return false;
            } else {
                try {
                    if (!this.mParent.isLayoutDirectionResolved()) {
                        return false;
                    }
                    if (this.mParent.getLayoutDirection() == 1) {
                        this.mPrivateFlags2 |= 16;
                    }
                } catch (AbstractMethodError e) {
                    Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
                }
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
        ViewParent viewParent = this.mParent;
        if (viewParent == null) {
            return false;
        }
        try {
            return viewParent.canResolveLayoutDirection();
        } catch (AbstractMethodError e) {
            Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
            return false;
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

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean isPaddingResolved() {
        return (this.mPrivateFlags2 & 536870912) == 536870912;
    }

    @UnsupportedAppUsage
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
                int i = this.mUserPaddingStart;
                if (i != Integer.MIN_VALUE) {
                    this.mUserPaddingLeft = i;
                } else {
                    this.mUserPaddingLeft = this.mUserPaddingLeftInitial;
                }
                int i2 = this.mUserPaddingEnd;
                if (i2 != Integer.MIN_VALUE) {
                    this.mUserPaddingRight = i2;
                } else {
                    this.mUserPaddingRight = this.mUserPaddingRightInitial;
                }
            } else {
                int i3 = this.mUserPaddingStart;
                if (i3 != Integer.MIN_VALUE) {
                    this.mUserPaddingRight = i3;
                } else {
                    this.mUserPaddingRight = this.mUserPaddingRightInitial;
                }
                int i4 = this.mUserPaddingEnd;
                if (i4 != Integer.MIN_VALUE) {
                    this.mUserPaddingLeft = i4;
                } else {
                    this.mUserPaddingLeft = this.mUserPaddingLeftInitial;
                }
            }
            int i5 = this.mUserPaddingBottom;
            if (i5 < 0) {
                i5 = this.mPaddingBottom;
            }
            this.mUserPaddingBottom = i5;
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
    @UnsupportedAppUsage
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
        AccessibilityNodeIdManager.getInstance().unregisterViewWithId(getAccessibilityViewId());
    }

    private void cleanupDraw() {
        resetDisplayList();
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            attachInfo.mViewRootImpl.cancelInvalidate(this);
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
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mWindowToken;
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
            return ai.mWindowToken;
        }
        return appWindowToken;
    }

    public Display getDisplay() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mDisplay;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public IWindowSession getWindowSession() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mSession;
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public IWindow getWindow() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mWindow;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int combineVisibility(int vis1, int vis2) {
        return Math.max(vis1, vis2);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage(maxTargetSdk = 28)
    public void dispatchAttachedToWindow(AttachInfo info, int visibility) {
        this.mAttachInfo = info;
        ViewOverlay viewOverlay = this.mOverlay;
        if (viewOverlay != null) {
            viewOverlay.getOverlayView().dispatchAttachedToWindow(info, visibility);
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
        HandlerActionQueue handlerActionQueue = this.mRunQueue;
        if (handlerActionQueue != null) {
            handlerActionQueue.executeActions(info.mHandler);
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
        if (this.mIsNeedDetect && getViewRootImpl() != null) {
            HwMwUtils.performPolicy(11, this);
            this.mIsNeedDetect = false;
        }
        notifyEnterOrExitForAutoFillIfNeeded(true);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage(maxTargetSdk = 28)
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
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(InputMethodManager.class);
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
        ViewOverlay viewOverlay = this.mOverlay;
        if (viewOverlay != null) {
            viewOverlay.getOverlayView().dispatchDetachedFromWindow();
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
        BaseSavedState state = new BaseSavedState(AbsSavedState.EMPTY_STATE);
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
        Parcelable state;
        int i = this.mID;
        if (i != -1 && (state = container.get(i)) != null) {
            this.mPrivateFlags &= -131073;
            onRestoreInstanceState(state);
            if ((this.mPrivateFlags & 131072) == 0) {
                throw new IllegalStateException("Derived class did not call super.onRestoreInstanceState()");
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
                } else if (Log.isLoggable(AUTOFILL_LOG_TAG, 3)) {
                    Log.d(AUTOFILL_LOG_TAG, "onRestoreInstanceState(): not setting autofillId to " + baseState.mAutofillViewId + " because view explicitly set it to " + this.mAutofillId);
                }
            }
        }
    }

    public long getDrawingTime() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mDrawingTime;
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
                int i = this.mLayerType;
                if (i == 1) {
                    buildDrawingCache(true);
                } else if (i == 2) {
                    updateDisplayListIfDirty();
                    if (attachInfo.mThreadedRenderer != null && this.mRenderNode.hasDisplayList()) {
                        attachInfo.mThreadedRenderer.buildLayer(this.mRenderNode);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void destroyHardwareResources() {
        ViewOverlay viewOverlay = this.mOverlay;
        if (viewOverlay != null) {
            viewOverlay.getOverlayView().destroyHardwareResources();
        }
        GhostView ghostView = this.mGhostView;
        if (ghostView != null) {
            ghostView.destroyHardwareResources();
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
        Log.d(VIEW_LOG_TAG, indent + this + "             DIRTY(" + (this.mPrivateFlags & 2097152) + ") DRAWN(" + (this.mPrivateFlags & 32) + ") CACHE_VALID(" + (this.mPrivateFlags & 32768) + ") INVALIDATED(" + (this.mPrivateFlags & Integer.MIN_VALUE) + ")");
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
        AttachInfo attachInfo = this.mAttachInfo;
        return (attachInfo == null || attachInfo.mThreadedRenderer == null) ? false : true;
    }

    @UnsupportedAppUsage
    public RenderNode updateDisplayListIfDirty() {
        RenderNode renderNode = this.mRenderNode;
        if (!canHaveDisplayList()) {
            return renderNode;
        }
        if ((this.mPrivateFlags & 32768) != 0 && renderNode.hasDisplayList() && !this.mRecreateDisplayList) {
            this.mPrivateFlags |= 32800;
            this.mPrivateFlags &= -2097153;
        } else if (!renderNode.hasDisplayList() || this.mRecreateDisplayList) {
            this.mRecreateDisplayList = true;
            int width = this.mRight - this.mLeft;
            int height = this.mBottom - this.mTop;
            int layerType = getLayerType();
            RecordingCanvas canvas = renderNode.beginRecording(width, height);
            handForceDarkConfigrationChange(canvas);
            if (layerType == 1) {
                try {
                    buildDrawingCache(true);
                    Bitmap cache = getDrawingCache(true);
                    if (cache != null) {
                        canvas.drawBitmap(cache, 0.0f, 0.0f, this.mLayerPaint);
                    }
                } catch (Throwable th) {
                    renderNode.endRecording();
                    setDisplayListProperties(renderNode);
                    throw th;
                }
            } else {
                computeScroll();
                canvas.translate((float) (-this.mScrollX), (float) (-this.mScrollY));
                this.mPrivateFlags |= 32800;
                this.mPrivateFlags &= -2097153;
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
            renderNode.endRecording();
            setDisplayListProperties(renderNode);
        } else {
            this.mPrivateFlags |= 32800;
            this.mPrivateFlags &= -2097153;
            dispatchGetDisplayList();
            return renderNode;
        }
        return renderNode;
    }

    @UnsupportedAppUsage
    private void resetDisplayList() {
        this.mRenderNode.discardDisplayList();
        RenderNode renderNode = this.mBackgroundRenderNode;
        if (renderNode != null) {
            renderNode.discardDisplayList();
        }
    }

    @Deprecated
    public Bitmap getDrawingCache() {
        return getDrawingCache(false);
    }

    @Deprecated
    public Bitmap getDrawingCache(boolean autoScale) {
        int i = this.mViewFlags;
        if ((i & 131072) == 131072) {
            return null;
        }
        if ((i & 32768) == 32768) {
            buildDrawingCache(autoScale);
        }
        return autoScale ? this.mDrawingCache : this.mUnscaledDrawingCache;
    }

    @Deprecated
    public void destroyDrawingCache() {
        Bitmap bitmap = this.mDrawingCache;
        if (bitmap != null) {
            bitmap.recycle();
            this.mDrawingCache = null;
        }
        Bitmap bitmap2 = this.mUnscaledDrawingCache;
        if (bitmap2 != null) {
            bitmap2.recycle();
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

    /* JADX INFO: Multiple debug info for r6v18 float: [D('scale' float), D('scalingRequired' boolean)] */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x0174  */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x0178  */
    private void buildDrawingCacheImpl(boolean autoScale) {
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
        if (width > 0 && height > 0) {
            if (projectedBitmapSize <= drawingCacheSize) {
                boolean clear = true;
                Bitmap bitmap = autoScale ? this.mDrawingCache : this.mUnscaledDrawingCache;
                if (!(bitmap != null && bitmap.getWidth() == width && bitmap.getHeight() == height)) {
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
                            if (drawingCacheBackgroundColor != 0) {
                                z = true;
                            }
                            clear = z;
                        } else {
                            try {
                                throw new OutOfMemoryError();
                            } catch (OutOfMemoryError e2) {
                                if (!autoScale) {
                                    this.mDrawingCache = null;
                                } else {
                                    this.mUnscaledDrawingCache = null;
                                }
                                this.mCachingFailed = true;
                                return;
                            }
                        }
                    } catch (OutOfMemoryError e3) {
                        if (!autoScale) {
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
                AttachInfo attachInfo2 = this.mAttachInfo;
                if (attachInfo2 == null || !attachInfo2.mHardwareAccelerated || this.mLayerType != 0) {
                    this.mPrivateFlags |= 32768;
                }
                int i2 = this.mPrivateFlags;
                if ((i2 & 128) == 128) {
                    this.mPrivateFlags = i2 & -2097153;
                    dispatchDraw(canvas);
                    drawAutofilledHighlight(canvas);
                    ViewOverlay viewOverlay = this.mOverlay;
                    if (viewOverlay != null && !viewOverlay.isEmpty()) {
                        this.mOverlay.getOverlayView().draw(canvas);
                    }
                } else {
                    draw(canvas);
                }
                canvas.restoreToCount(restoreCount);
                canvas.setBitmap(null);
                if (attachInfo != null) {
                    attachInfo.mCanvas = canvas;
                    return;
                }
                return;
            }
        }
        if (width > 0 && height > 0) {
            Log.w(VIEW_LOG_TAG, getClass().getSimpleName() + " not displayed because it is too large to fit into a software layer (or drawing cache), needs " + projectedBitmapSize + " bytes, only " + drawingCacheSize + " available");
        }
        destroyDrawingCache();
        this.mCachingFailed = true;
    }

    @UnsupportedAppUsage
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
            this.mPrivateFlags &= -2097153;
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
        AttachInfo attachInfo = this.mAttachInfo;
        return attachInfo != null && attachInfo.mHardwareAccelerated;
    }

    public void setClipBounds(Rect clipBounds) {
        Rect rect = this.mClipBounds;
        if (clipBounds == rect) {
            return;
        }
        if (clipBounds == null || !clipBounds.equals(rect)) {
            if (clipBounds != null) {
                Rect rect2 = this.mClipBounds;
                if (rect2 == null) {
                    this.mClipBounds = new Rect(clipBounds);
                } else {
                    rect2.set(clipBounds);
                }
            } else {
                this.mClipBounds = null;
            }
            this.mRenderNode.setClipRect(this.mClipBounds);
            invalidateViewProperty(false, false);
        }
    }

    public Rect getClipBounds() {
        Rect rect = this.mClipBounds;
        if (rect != null) {
            return new Rect(rect);
        }
        return null;
    }

    public boolean getClipBounds(Rect outRect) {
        Rect rect = this.mClipBounds;
        if (rect == null) {
            return false;
        }
        outRect.set(rect);
        return true;
    }

    private boolean applyLegacyAnimation(ViewGroup parent, long drawingTime, Animation a, boolean scalingRequired) {
        Transformation invalidationTransform;
        int flags = parent.mGroupFlags;
        if (!a.isInitialized()) {
            a.initialize(this.mRight - this.mLeft, this.mBottom - this.mTop, parent.getWidth(), parent.getHeight());
            a.initializeInvalidateRegion(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop);
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo != null) {
                a.setListenerHandler(attachInfo.mHandler);
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
            Transformation invalidationTransform2 = parent.mInvalidationTransformation;
            a.getTransformation(drawingTime, invalidationTransform2, 1.0f);
            invalidationTransform = invalidationTransform2;
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

    /* access modifiers changed from: package-private */
    public void setDisplayListProperties(RenderNode renderNode) {
        int transformType;
        if (renderNode != null) {
            renderNode.setHasOverlappingRendering(getHasOverlappingRendering());
            ViewParent viewParent = this.mParent;
            renderNode.setClipToBounds((viewParent instanceof ViewGroup) && ((ViewGroup) viewParent).getClipChildren());
            float alpha = 1.0f;
            ViewParent viewParent2 = this.mParent;
            if ((viewParent2 instanceof ViewGroup) && (((ViewGroup) viewParent2).mGroupFlags & 2048) != 0) {
                ViewGroup parentVG = (ViewGroup) this.mParent;
                Transformation t = parentVG.getChildTransformation();
                if (parentVG.getChildStaticTransformation(this, t) && (transformType = t.getTransformationType()) != 0) {
                    if ((transformType & 1) != 0) {
                        alpha = t.getAlpha();
                    }
                    if ((transformType & 2) != 0) {
                        renderNode.setStaticMatrix(t.getMatrix());
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
            HwFrameworkFactory.getHwApsImpl().setPartialDirtyToNative(this.mCurrentDirtyRect, renderNode, getWidth(), getHeight(), getContext(), getClass().getSimpleName());
        }
    }

    /* JADX INFO: Multiple debug info for r2v4 android.graphics.Paint: [D('cachePaint' android.graphics.Paint), D('layerPaintAlpha' int)] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x01ce  */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x01d1  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x01d5  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0217  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x0251  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x0258  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0266  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x02e3  */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x02f4  */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x0337  */
    /* JADX WARNING: Removed duplicated region for block: B:176:0x0358  */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x03a1  */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x03a7  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x0179  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x017c  */
    public boolean draw(Canvas canvas, ViewGroup parent, long drawingTime) {
        Animation a;
        int layerType;
        Bitmap cache;
        RenderNode renderNode;
        int sx;
        int sy;
        int restoreTo;
        float alpha;
        boolean more;
        boolean hardwareAcceleratedCanvas;
        int restoreTo2;
        RenderNode renderNode2;
        Bitmap cache2;
        int restoreTo3;
        int sy2;
        Paint paint;
        int i;
        Bitmap cache3;
        float alpha2;
        float alpha3;
        int transX;
        int transY;
        boolean hardwareAcceleratedCanvas2 = canvas.isHardwareAccelerated();
        AttachInfo attachInfo = this.mAttachInfo;
        boolean drawingWithRenderNode = attachInfo != null && attachInfo.mHardwareAccelerated && hardwareAcceleratedCanvas2;
        boolean more2 = false;
        boolean childHasIdentityMatrix = hasIdentityMatrix();
        int parentFlags = parent.mGroupFlags;
        if ((parentFlags & 256) != 0) {
            parent.getChildTransformation().clear();
            parent.mGroupFlags &= TrafficStats.TAG_NETWORK_STACK_RANGE_END;
        }
        Transformation transformToApply = null;
        boolean concatMatrix = false;
        AttachInfo attachInfo2 = this.mAttachInfo;
        boolean scalingRequired = attachInfo2 != null && attachInfo2.mScalingRequired;
        Animation a2 = getAnimation();
        if (a2 != null) {
            a = a2;
            more2 = applyLegacyAnimation(parent, drawingTime, a2, scalingRequired);
            concatMatrix = a.willChangeTransformationMatrix();
            if (concatMatrix) {
                this.mPrivateFlags3 |= 1;
            }
            transformToApply = parent.getChildTransformation();
        } else {
            a = a2;
            Transformation transformation = null;
            if ((this.mPrivateFlags3 & 1) != 0) {
                this.mRenderNode.setAnimationMatrix(null);
                this.mPrivateFlags3 &= -2;
            }
            if (!drawingWithRenderNode && (parentFlags & 2048) != 0) {
                Transformation t = parent.getChildTransformation();
                if (parent.getChildStaticTransformation(this, t)) {
                    int transformType = t.getTransformationType();
                    if (transformType != 0) {
                        transformation = t;
                    }
                    transformToApply = transformation;
                    concatMatrix = (transformType & 2) != 0;
                }
            }
        }
        boolean concatMatrix2 = concatMatrix | (!childHasIdentityMatrix);
        this.mPrivateFlags |= 32;
        if (concatMatrix2 || (parentFlags & 2049) != 1 || !canvas.quickReject((float) this.mLeft, (float) this.mTop, (float) this.mRight, (float) this.mBottom, Canvas.EdgeType.BW) || (this.mPrivateFlags & 64) != 0) {
            this.mPrivateFlags2 &= -268435457;
            if (hardwareAcceleratedCanvas2) {
                this.mRecreateDisplayList = (this.mPrivateFlags & Integer.MIN_VALUE) != 0;
                this.mPrivateFlags &= Integer.MAX_VALUE;
            }
            int layerType2 = getLayerType();
            if (layerType2 == 1 || !drawingWithRenderNode) {
                if (layerType2 != 0) {
                    layerType2 = 1;
                    buildDrawingCache(true);
                }
                cache = getDrawingCache(true);
                layerType = layerType2;
            } else {
                cache = null;
                layerType = layerType2;
            }
            if (drawingWithRenderNode) {
                RenderNode renderNode3 = updateDisplayListIfDirty();
                if (!renderNode3.hasDisplayList()) {
                    drawingWithRenderNode = false;
                    renderNode = null;
                } else {
                    renderNode = renderNode3;
                }
            } else {
                renderNode = null;
            }
            if (!drawingWithRenderNode) {
                computeScroll();
                sx = this.mScrollX;
                sy = this.mScrollY;
            } else {
                sx = 0;
                sy = 0;
            }
            boolean drawingWithDrawingCache = cache != null && !drawingWithRenderNode;
            boolean offsetForScroll = cache == null && !drawingWithRenderNode;
            int restoreTo4 = -1;
            if (!drawingWithRenderNode || transformToApply != null) {
                restoreTo4 = canvas.save();
            }
            if (offsetForScroll) {
                canvas.translate((float) (this.mLeft - sx), (float) (this.mTop - sy));
            } else {
                if (!drawingWithRenderNode) {
                    canvas.translate((float) this.mLeft, (float) this.mTop);
                }
                if (scalingRequired) {
                    if (drawingWithRenderNode) {
                        restoreTo4 = canvas.save();
                    }
                    float scale = 1.0f / this.mAttachInfo.mApplicationScale;
                    canvas.scale(scale, scale);
                    restoreTo = restoreTo4;
                    alpha = !drawingWithRenderNode ? 1.0f : getAlpha() * getTransitionAlpha();
                    if (transformToApply == null || alpha < 1.0f || !hasIdentityMatrix() || (this.mPrivateFlags3 & 2) != 0) {
                        if (transformToApply == null || !childHasIdentityMatrix) {
                            transX = 0;
                            if (!offsetForScroll) {
                                transX = -sx;
                                transY = -sy;
                            } else {
                                transY = 0;
                            }
                            if (transformToApply == null) {
                                if (concatMatrix2) {
                                    if (drawingWithRenderNode) {
                                        cache3 = cache;
                                        renderNode.setAnimationMatrix(transformToApply.getMatrix());
                                        hardwareAcceleratedCanvas = hardwareAcceleratedCanvas2;
                                    } else {
                                        cache3 = cache;
                                        hardwareAcceleratedCanvas = hardwareAcceleratedCanvas2;
                                        canvas.translate((float) (-transX), (float) (-transY));
                                        canvas.concat(transformToApply.getMatrix());
                                        canvas.translate((float) transX, (float) transY);
                                    }
                                    parent.mGroupFlags |= 256;
                                } else {
                                    cache3 = cache;
                                    hardwareAcceleratedCanvas = hardwareAcceleratedCanvas2;
                                }
                                float transformAlpha = transformToApply.getAlpha();
                                if (transformAlpha < 1.0f) {
                                    alpha *= transformAlpha;
                                    parent.mGroupFlags |= 256;
                                }
                            } else {
                                cache3 = cache;
                                hardwareAcceleratedCanvas = hardwareAcceleratedCanvas2;
                            }
                            if (!childHasIdentityMatrix && !drawingWithRenderNode) {
                                canvas.translate((float) (-transX), (float) (-transY));
                                canvas.concat(getMatrix());
                                canvas.translate((float) transX, (float) transY);
                            }
                            alpha2 = alpha;
                        } else {
                            cache3 = cache;
                            hardwareAcceleratedCanvas = hardwareAcceleratedCanvas2;
                            alpha2 = alpha;
                        }
                        if (alpha2 >= 1.0f || (this.mPrivateFlags3 & 2) != 0) {
                            if (alpha2 >= 1.0f) {
                                this.mPrivateFlags3 |= 2;
                            } else {
                                this.mPrivateFlags3 &= -3;
                            }
                            parent.mGroupFlags |= 256;
                            if (drawingWithDrawingCache) {
                                int multipliedAlpha = (int) (alpha2 * 255.0f);
                                if (onSetAlpha(multipliedAlpha)) {
                                    alpha3 = alpha2;
                                    more = more2;
                                    cache2 = cache3;
                                    sy2 = sy;
                                    renderNode2 = renderNode;
                                    restoreTo2 = restoreTo;
                                    restoreTo3 = sx;
                                    this.mPrivateFlags |= 262144;
                                } else if (drawingWithRenderNode) {
                                    renderNode.setAlpha(getAlpha() * alpha2 * getTransitionAlpha());
                                    alpha3 = alpha2;
                                    more = more2;
                                    cache2 = cache3;
                                    sy2 = sy;
                                    renderNode2 = renderNode;
                                    restoreTo2 = restoreTo;
                                    restoreTo3 = sx;
                                } else if (layerType == 0) {
                                    alpha3 = alpha2;
                                    sy2 = sy;
                                    renderNode2 = renderNode;
                                    more = more2;
                                    cache2 = cache3;
                                    restoreTo2 = restoreTo;
                                    restoreTo3 = sx;
                                    canvas.saveLayerAlpha((float) sx, (float) sy, (float) (getWidth() + sx), (float) (sy + getHeight()), multipliedAlpha);
                                } else {
                                    alpha3 = alpha2;
                                    more = more2;
                                    cache2 = cache3;
                                    sy2 = sy;
                                    renderNode2 = renderNode;
                                    restoreTo2 = restoreTo;
                                    restoreTo3 = sx;
                                }
                            } else {
                                alpha3 = alpha2;
                                more = more2;
                                cache2 = cache3;
                                sy2 = sy;
                                renderNode2 = renderNode;
                                restoreTo2 = restoreTo;
                                restoreTo3 = sx;
                            }
                        } else {
                            alpha3 = alpha2;
                            more = more2;
                            cache2 = cache3;
                            sy2 = sy;
                            renderNode2 = renderNode;
                            restoreTo2 = restoreTo;
                            restoreTo3 = sx;
                        }
                        alpha = alpha3;
                    } else {
                        if ((this.mPrivateFlags & 262144) == 262144) {
                            onSetAlpha(255);
                            this.mPrivateFlags &= -262145;
                        }
                        hardwareAcceleratedCanvas = hardwareAcceleratedCanvas2;
                        restoreTo2 = restoreTo;
                        more = more2;
                        sy2 = sy;
                        restoreTo3 = sx;
                        renderNode2 = renderNode;
                        cache2 = cache;
                    }
                    if (!drawingWithRenderNode) {
                        if ((parentFlags & 1) != 0 && cache2 == null) {
                            if (offsetForScroll) {
                                canvas.clipRect(restoreTo3, sy2, restoreTo3 + getWidth(), sy2 + getHeight());
                            } else {
                                if (!scalingRequired) {
                                    i = 0;
                                } else if (cache2 == null) {
                                    i = 0;
                                } else {
                                    canvas.clipRect(0, 0, cache2.getWidth(), cache2.getHeight());
                                }
                                canvas.clipRect(i, i, getWidth(), getHeight());
                            }
                        }
                        Rect rect = this.mClipBounds;
                        if (rect != null) {
                            canvas.clipRect(rect);
                        }
                    }
                    if (drawingWithDrawingCache) {
                        if (drawingWithRenderNode) {
                            this.mPrivateFlags = -2097153 & this.mPrivateFlags;
                            ((RecordingCanvas) canvas).drawRenderNode(renderNode2);
                        } else {
                            int i2 = this.mPrivateFlags;
                            if ((i2 & 128) == 128) {
                                this.mPrivateFlags = -2097153 & i2;
                                dispatchDraw(canvas);
                            } else {
                                draw(canvas);
                            }
                        }
                    } else if (cache2 != null) {
                        this.mPrivateFlags = -2097153 & this.mPrivateFlags;
                        if (layerType == 0 || (paint = this.mLayerPaint) == null) {
                            Paint cachePaint = parent.mCachePaint;
                            if (cachePaint == null) {
                                cachePaint = new Paint();
                                cachePaint.setDither(false);
                                parent.mCachePaint = cachePaint;
                            }
                            cachePaint.setAlpha((int) (alpha * 255.0f));
                            canvas.drawBitmap(cache2, 0.0f, 0.0f, cachePaint);
                        } else {
                            int layerPaintAlpha = paint.getAlpha();
                            if (alpha < 1.0f) {
                                this.mLayerPaint.setAlpha((int) (((float) layerPaintAlpha) * alpha));
                            }
                            canvas.drawBitmap(cache2, 0.0f, 0.0f, this.mLayerPaint);
                            if (alpha < 1.0f) {
                                this.mLayerPaint.setAlpha(layerPaintAlpha);
                            }
                        }
                    }
                    if (restoreTo2 < 0) {
                        canvas.restoreToCount(restoreTo2);
                    }
                    if (a != null && !more) {
                        if (!hardwareAcceleratedCanvas && !a.getFillAfter()) {
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
            }
            restoreTo = restoreTo4;
            if (!drawingWithRenderNode) {
            }
            if (transformToApply == null) {
            }
            if (transformToApply == null) {
            }
            transX = 0;
            if (!offsetForScroll) {
            }
            if (transformToApply == null) {
            }
            canvas.translate((float) (-transX), (float) (-transY));
            canvas.concat(getMatrix());
            canvas.translate((float) transX, (float) transY);
            alpha2 = alpha;
            if (alpha2 >= 1.0f) {
            }
            if (alpha2 >= 1.0f) {
            }
            parent.mGroupFlags |= 256;
            if (drawingWithDrawingCache) {
            }
            alpha = alpha3;
            if (!drawingWithRenderNode) {
            }
            if (drawingWithDrawingCache) {
            }
            if (restoreTo2 < 0) {
            }
            onSetAlpha(255);
            parent.finishAnimatingView(this, a);
            invalidate(true);
            this.mRecreateDisplayList = false;
            return more;
        }
        this.mPrivateFlags2 |= 268435456;
        return more2;
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
            canvas.drawRect((float) l, (float) t, (float) (l + cornerSquareSize), (float) (t + cornerSquareSize), paint);
            canvas.drawRect((float) (r - cornerSquareSize), (float) t, (float) r, (float) (t + cornerSquareSize), paint);
            canvas.drawRect((float) l, (float) (b - cornerSquareSize), (float) (l + cornerSquareSize), (float) b, paint);
            canvas.drawRect((float) (r - cornerSquareSize), (float) (b - cornerSquareSize), (float) r, (float) b, paint);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine((float) l, (float) t, (float) r, (float) b, paint);
            canvas.drawLine((float) l, (float) b, (float) r, (float) t, paint);
        }
    }

    /* JADX INFO: Multiple debug info for r9v1 android.view.View$ScrollabilityCache: [D('scrollabilityCache' android.view.View$ScrollabilityCache), D('viewFlags' int)] */
    /* JADX INFO: Multiple debug info for r5v1 int: [D('drawLeft' boolean), D('length' int)] */
    /* JADX INFO: Multiple debug info for r10v3 android.graphics.Paint: [D('topSaveCount' int), D('p' android.graphics.Paint)] */
    /* JADX INFO: Multiple debug info for r0v2 android.graphics.Matrix: [D('bottomSaveCount' int), D('matrix' android.graphics.Matrix)] */
    /* JADX INFO: Multiple debug info for r13v2 android.graphics.Shader: [D('bottomFadeStrength' float), D('fade' android.graphics.Shader)] */
    public void draw(Canvas canvas) {
        int paddingLeft;
        int right;
        int right2;
        float topFadeStrength;
        boolean drawRight;
        int length;
        int leftSaveCount;
        int bottomSaveCount;
        int saveCount;
        int leftSaveCount2;
        int bottomSaveCount2;
        int length2;
        int bottom;
        int left;
        int solidColor;
        int left2;
        int length3;
        int rightSaveCount;
        float fadeHeight;
        int right3;
        int bottom2;
        int right4;
        int leftSaveCount3;
        int topSaveCount;
        if (!canvas.isHardwareAccelerated()) {
            handForceDarkConfigrationChange(canvas);
        }
        this.mPrivateFlags = (-2097153 & this.mPrivateFlags) | 32;
        drawBackground(canvas);
        int viewFlags = this.mViewFlags;
        boolean horizontalEdges = (viewFlags & 4096) != 0;
        boolean verticalEdges = (viewFlags & 8192) != 0;
        if (verticalEdges || horizontalEdges) {
            float bottomFadeStrength = 0.0f;
            float leftFadeStrength = 0.0f;
            float rightFadeStrength = 0.0f;
            int paddingLeft2 = this.mPaddingLeft;
            boolean offsetRequired = isPaddingOffsetRequired();
            if (offsetRequired) {
                paddingLeft = paddingLeft2 + getLeftPaddingOffset();
            } else {
                paddingLeft = paddingLeft2;
            }
            int left3 = this.mScrollX + paddingLeft;
            boolean drawTop = false;
            int right5 = (((this.mRight + left3) - this.mLeft) - this.mPaddingRight) - paddingLeft;
            int top = this.mScrollY + getFadeTop(offsetRequired);
            int bottom3 = top + getFadeHeight(offsetRequired);
            if (offsetRequired) {
                right = right5 + getRightPaddingOffset();
                right2 = bottom3 + getBottomPaddingOffset();
            } else {
                right = right5;
                right2 = bottom3;
            }
            ScrollabilityCache scrollabilityCache = this.mScrollCache;
            boolean drawBottom = false;
            float fadeHeight2 = (float) scrollabilityCache.fadingEdgeLength;
            boolean drawLeft = false;
            int length4 = (int) fadeHeight2;
            if (verticalEdges) {
                drawRight = false;
                topFadeStrength = 0.0f;
                if (top + length4 > right2 - length4) {
                    length4 = (right2 - top) / 2;
                }
            } else {
                drawRight = false;
                topFadeStrength = 0.0f;
            }
            if (!horizontalEdges || left3 + length4 <= right - length4) {
                length = length4;
            } else {
                length = (right - left3) / 2;
            }
            if (verticalEdges) {
                float topFadeStrength2 = Math.max(0.0f, Math.min(1.0f, getTopFadingEdgeStrength()));
                drawTop = topFadeStrength2 * fadeHeight2 > 1.0f;
                topFadeStrength = topFadeStrength2;
                bottomFadeStrength = Math.max(0.0f, Math.min(1.0f, getBottomFadingEdgeStrength()));
                drawBottom = bottomFadeStrength * fadeHeight2 > 1.0f;
            }
            if (horizontalEdges) {
                leftFadeStrength = Math.max(0.0f, Math.min(1.0f, getLeftFadingEdgeStrength()));
                boolean drawLeft2 = leftFadeStrength * fadeHeight2 > 1.0f;
                rightFadeStrength = Math.max(0.0f, Math.min(1.0f, getRightFadingEdgeStrength()));
                drawRight = rightFadeStrength * fadeHeight2 > 1.0f;
                drawLeft = drawLeft2;
            }
            int saveCount2 = canvas.getSaveCount();
            int topSaveCount2 = -1;
            int bottomSaveCount3 = -1;
            int leftSaveCount4 = -1;
            int solidColor2 = getSolidColor();
            if (solidColor2 == 0) {
                if (drawTop) {
                    topSaveCount2 = canvas.saveUnclippedLayer(left3, top, right, top + length);
                }
                if (drawBottom) {
                    topSaveCount = topSaveCount2;
                    bottomSaveCount3 = canvas.saveUnclippedLayer(left3, right2 - length, right, right2);
                } else {
                    topSaveCount = topSaveCount2;
                }
                if (drawLeft) {
                    leftSaveCount4 = canvas.saveUnclippedLayer(left3, top, left3 + length, right2);
                }
                if (drawRight) {
                    bottomSaveCount2 = bottomSaveCount3;
                    leftSaveCount2 = leftSaveCount4;
                    bottomSaveCount = canvas.saveUnclippedLayer(right - length, top, right, right2);
                    leftSaveCount = saveCount2;
                    saveCount = topSaveCount;
                } else {
                    bottomSaveCount2 = bottomSaveCount3;
                    leftSaveCount2 = leftSaveCount4;
                    bottomSaveCount = -1;
                    leftSaveCount = saveCount2;
                    saveCount = topSaveCount;
                }
            } else {
                scrollabilityCache.setFadeColor(solidColor2);
                bottomSaveCount2 = -1;
                leftSaveCount2 = -1;
                bottomSaveCount = -1;
                leftSaveCount = saveCount2;
                saveCount = -1;
            }
            onDraw(canvas);
            dispatchDraw(canvas);
            Paint p = scrollabilityCache.paint;
            Matrix matrix = scrollabilityCache.matrix;
            Shader fade = scrollabilityCache.shader;
            if (drawRight) {
                matrix.setScale(1.0f, fadeHeight2 * rightFadeStrength);
                matrix.postRotate(90.0f);
                matrix.postTranslate((float) right, (float) top);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                if (solidColor2 == 0) {
                    canvas.restoreUnclippedLayer(bottomSaveCount, p);
                    bottom = right2;
                    fadeHeight = fadeHeight2;
                    solidColor = solidColor2;
                    length2 = length;
                    left2 = left3;
                    length3 = 1065353216;
                    rightSaveCount = top;
                    left = right;
                    right3 = leftSaveCount2;
                } else {
                    rightSaveCount = top;
                    left2 = left3;
                    left = right;
                    fadeHeight = fadeHeight2;
                    bottom = right2;
                    right3 = leftSaveCount2;
                    solidColor = solidColor2;
                    length2 = length;
                    length3 = 1065353216;
                    canvas.drawRect((float) (right - length), (float) top, (float) right, (float) right2, p);
                }
            } else {
                bottom = right2;
                solidColor = solidColor2;
                left = right;
                length2 = length;
                length3 = 1065353216;
                rightSaveCount = top;
                left2 = left3;
                fadeHeight = fadeHeight2;
                right3 = leftSaveCount2;
            }
            if (drawLeft) {
                matrix.setScale(length3, fadeHeight * leftFadeStrength);
                matrix.postRotate(-90.0f);
                matrix.postTranslate((float) left2, (float) rightSaveCount);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                if (solidColor == 0) {
                    canvas.restoreUnclippedLayer(right3, p);
                    bottom2 = bottom;
                } else {
                    bottom2 = bottom;
                    canvas.drawRect((float) left2, (float) rightSaveCount, (float) (left2 + length2), (float) bottom, p);
                }
            } else {
                bottom2 = bottom;
            }
            if (drawBottom) {
                matrix.setScale(length3, fadeHeight * bottomFadeStrength);
                matrix.postRotate(180.0f);
                matrix.postTranslate((float) left2, (float) bottom2);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                if (solidColor == 0) {
                    canvas.restoreUnclippedLayer(bottomSaveCount2, p);
                    leftSaveCount3 = left;
                    right4 = bottomSaveCount2;
                } else {
                    leftSaveCount3 = left;
                    right4 = bottomSaveCount2;
                    canvas.drawRect((float) left2, (float) (bottom2 - length2), (float) left, (float) bottom2, p);
                }
            } else {
                leftSaveCount3 = left;
                right4 = bottomSaveCount2;
            }
            if (drawTop) {
                matrix.setScale(1.0f, fadeHeight * topFadeStrength);
                matrix.postTranslate((float) left2, (float) rightSaveCount);
                fade.setLocalMatrix(matrix);
                p.setShader(fade);
                if (solidColor == 0) {
                    canvas.restoreUnclippedLayer(saveCount, p);
                } else {
                    canvas.drawRect((float) left2, (float) rightSaveCount, (float) leftSaveCount3, (float) (rightSaveCount + length2), p);
                }
            }
            canvas.restoreToCount(leftSaveCount);
            drawAutofilledHighlight(canvas);
            ViewOverlay viewOverlay = this.mOverlay;
            if (viewOverlay != null && !viewOverlay.isEmpty()) {
                this.mOverlay.getOverlayView().dispatchDraw(canvas);
            }
            onDrawForeground(canvas);
            if (debugDraw()) {
                debugDrawFocus(canvas);
                return;
            }
            return;
        }
        onDraw(canvas);
        dispatchDraw(canvas);
        drawAutofilledHighlight(canvas);
        ViewOverlay viewOverlay2 = this.mOverlay;
        if (viewOverlay2 != null && !viewOverlay2.isEmpty()) {
            this.mOverlay.getOverlayView().dispatchDraw(canvas);
        }
        onDrawForeground(canvas);
        drawDefaultFocusHighlight(canvas);
        if (debugDraw()) {
            debugDrawFocus(canvas);
        }
    }

    /* JADX INFO: Multiple debug info for r1v1 int: [D('renderNode' android.graphics.RenderNode), D('scrollX' int)] */
    @UnsupportedAppUsage
    private void drawBackground(Canvas canvas) {
        AttachInfo attachInfo;
        Drawable background = this.mBackground;
        if (background != null) {
            setBackgroundBounds();
            if (!(!canvas.isHardwareAccelerated() || (attachInfo = this.mAttachInfo) == null || attachInfo.mThreadedRenderer == null)) {
                this.mBackgroundRenderNode = getDrawableRenderNode(background, this.mBackgroundRenderNode);
                RenderNode renderNode = this.mBackgroundRenderNode;
                if (renderNode != null && renderNode.hasDisplayList()) {
                    setBackgroundRenderNodeProperties(renderNode);
                    ((RecordingCanvas) canvas).drawRenderNode(renderNode);
                    return;
                }
            }
            int scrollX = this.mScrollX;
            int scrollY = this.mScrollY;
            if ((scrollX | scrollY) == 0) {
                background.draw(canvas);
                return;
            }
            canvas.translate((float) scrollX, (float) scrollY);
            background.draw(canvas);
            canvas.translate((float) (-scrollX), (float) (-scrollY));
        }
    }

    /* access modifiers changed from: package-private */
    public void setBackgroundBounds() {
        Drawable drawable;
        if (this.mBackgroundSizeChanged && (drawable = this.mBackground) != null) {
            drawable.setBounds(0, 0, this.mRight - this.mLeft, this.mBottom - this.mTop);
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
            renderNode = RenderNode.create(drawable.getClass().getName(), new ViewAnimationHostBridge(this));
            renderNode.setUsageHint(1);
        }
        Rect bounds = drawable.getBounds();
        RecordingCanvas canvas = renderNode.beginRecording(bounds.width(), bounds.height());
        canvas.translate((float) (-bounds.left), (float) (-bounds.top));
        try {
            drawable.draw(canvas);
            renderNode.endRecording();
            renderNode.setLeftTopRightBottom(bounds.left, bounds.top, bounds.right, bounds.bottom);
            renderNode.setProjectBackwards(drawable.isProjected());
            renderNode.setProjectionReceiver(true);
            renderNode.setClipToBounds(false);
            return renderNode;
        } catch (Throwable th) {
            renderNode.endRecording();
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
        ViewParent viewParent = this.mParent;
        Insets parentInsets = viewParent instanceof View ? ((View) viewParent).getOpticalInsets() : Insets.NONE;
        Insets childInsets = getOpticalInsets();
        return setFrame((parentInsets.left + left) - childInsets.left, (parentInsets.top + top) - childInsets.top, parentInsets.left + right + childInsets.right, parentInsets.top + bottom + childInsets.bottom);
    }

    public void layout(int l, int t, int r, int b) {
        View view;
        if (ViewRootImpl.DEBUG_VIEW_TRACE) {
            Trace.traceBegin(8, "layout " + getClass().getName());
        }
        if ((this.mPrivateFlags3 & 8) != 0) {
            onMeasure(this.mOldWidthMeasureSpec, this.mOldHeightMeasureSpec);
            this.mPrivateFlags3 &= -9;
        }
        int oldL = this.mLeft;
        int oldT = this.mTop;
        int oldB = this.mBottom;
        int oldR = this.mRight;
        boolean changed = isLayoutModeOptical(this.mParent) ? setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);
        View view2 = null;
        if (changed || (this.mPrivateFlags & 8192) == 8192) {
            onLayout(changed, l, t, r, b);
            if (!shouldDrawRoundScrollbar()) {
                this.mRoundScrollbarRenderer = null;
            } else if (this.mRoundScrollbarRenderer == null) {
                this.mRoundScrollbarRenderer = new RoundScrollbarRenderer(this);
            }
            this.mPrivateFlags &= -8193;
            ListenerInfo li = this.mListenerInfo;
            if (li == null || li.mOnLayoutChangeListeners == null) {
                view = null;
            } else {
                ArrayList<OnLayoutChangeListener> listenersCopy = (ArrayList) li.mOnLayoutChangeListeners.clone();
                int numListeners = listenersCopy.size();
                int i = 0;
                while (i < numListeners) {
                    listenersCopy.get(i).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
                    i++;
                    view2 = view2;
                    oldR = oldR;
                    numListeners = numListeners;
                    listenersCopy = listenersCopy;
                    li = li;
                }
                view = view2;
            }
        } else {
            view = null;
        }
        boolean wasLayoutValid = isLayoutValid();
        this.mPrivateFlags &= -4097;
        this.mPrivateFlags3 |= 4;
        if (wasLayoutValid || !isFocused()) {
            int i2 = this.mPrivateFlags;
            if ((i2 & 1) != 0) {
                this.mPrivateFlags = i2 & -2;
                View focused = findFocus();
                if (focused != null && !restoreDefaultFocus() && !hasParentWantsFocus()) {
                    focused.clearFocusInternal(view, true, false);
                }
            }
        } else {
            this.mPrivateFlags &= -2;
            if (canTakeFocus()) {
                clearParentsWantFocus();
            } else if (getViewRootImpl() == null || !getViewRootImpl().isInLayout()) {
                clearFocusInternal(view, true, false);
                clearParentsWantFocus();
            } else if (!hasParentWantsFocus()) {
                clearFocusInternal(view, true, false);
            }
        }
        int i3 = this.mPrivateFlags3;
        if ((134217728 & i3) != 0) {
            this.mPrivateFlags3 = i3 & -134217729;
            notifyEnterOrExitForAutoFillIfNeeded(true);
        }
        if (ViewRootImpl.DEBUG_VIEW_TRACE) {
            Trace.traceEnd(8);
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
    @UnsupportedAppUsage(maxTargetSdk = 28)
    public boolean setFrame(int left, int top, int right, int bottom) {
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
            ForegroundInfo foregroundInfo = this.mForegroundInfo;
            if (foregroundInfo != null) {
                foregroundInfo.mBoundsChanged = true;
            }
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
        return changed;
    }

    public final void setLeftTopRightBottom(int left, int top, int right, int bottom) {
        setFrame(left, top, right, bottom);
    }

    private void sizeChange(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
        ViewOverlay viewOverlay = this.mOverlay;
        if (viewOverlay != null) {
            viewOverlay.getOverlayView().setRight(newWidth);
            this.mOverlay.getOverlayView().setBottom(newHeight);
        }
        if (!sCanFocusZeroSized && isLayoutValid()) {
            ViewParent viewParent = this.mParent;
            if (!(viewParent instanceof ViewGroup) || !((ViewGroup) viewParent).isLayoutSuppressed()) {
                if (newWidth <= 0 || newHeight <= 0) {
                    if (hasFocus()) {
                        clearFocus();
                        ViewParent viewParent2 = this.mParent;
                        if (viewParent2 instanceof ViewGroup) {
                            ((ViewGroup) viewParent2).clearFocusedInCluster();
                        }
                    }
                    clearAccessibilityFocus();
                } else if ((oldWidth <= 0 || oldHeight <= 0) && this.mParent != null && canTakeFocus()) {
                    this.mParent.focusableViewAvailable(this);
                }
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

    @Override // android.graphics.drawable.Drawable.Callback
    public void invalidateDrawable(Drawable drawable) {
        if (verifyDrawable(drawable)) {
            Rect dirty = drawable.getDirtyBounds();
            int scrollX = this.mScrollX;
            int scrollY = this.mScrollY;
            invalidate(dirty.left + scrollX, dirty.top + scrollY, dirty.right + scrollX, dirty.bottom + scrollY);
            rebuildOutline();
        }
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (verifyDrawable(who) && what != null) {
            long delay = when - SystemClock.uptimeMillis();
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo != null) {
                attachInfo.mViewRootImpl.mChoreographer.postCallbackDelayed(1, what, who, Choreographer.subtractFrameDelay(delay));
            } else {
                getRunQueue().postDelayed(what, delay);
            }
        }
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (verifyDrawable(who) && what != null) {
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo != null) {
                attachInfo.mViewRootImpl.mChoreographer.removeCallbacks(1, what, who);
            }
            getRunQueue().removeCallbacks(what);
        }
    }

    public void unscheduleDrawable(Drawable who) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null && who != null) {
            attachInfo.mViewRootImpl.mChoreographer.removeCallbacks(1, null, who);
        }
    }

    /* access modifiers changed from: protected */
    public void resolveDrawables() {
        if (isLayoutDirectionResolved() || getRawLayoutDirection() != 2) {
            int layoutDirection = isLayoutDirectionResolved() ? getLayoutDirection() : getRawLayoutDirection();
            Drawable drawable = this.mBackground;
            if (drawable != null) {
                drawable.setLayoutDirection(layoutDirection);
            }
            ForegroundInfo foregroundInfo = this.mForegroundInfo;
            if (!(foregroundInfo == null || foregroundInfo.mDrawable == null)) {
                this.mForegroundInfo.mDrawable.setLayoutDirection(layoutDirection);
            }
            Drawable drawable2 = this.mDefaultFocusHighlight;
            if (drawable2 != null) {
                drawable2.setLayoutDirection(layoutDirection);
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
        ForegroundInfo foregroundInfo;
        return who == this.mBackground || ((foregroundInfo = this.mForegroundInfo) != null && foregroundInfo.mDrawable == who) || this.mDefaultFocusHighlight == who;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        Drawable scrollBar;
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
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        Drawable fg = foregroundInfo != null ? foregroundInfo.mDrawable : null;
        if (fg != null && fg.isStateful()) {
            changed |= fg.setState(state);
        }
        ScrollabilityCache scrollabilityCache = this.mScrollCache;
        if (!(scrollabilityCache == null || (scrollBar = scrollabilityCache.scrollBar) == null || !scrollBar.isStateful())) {
            changed |= scrollBar.setState(state) && this.mScrollCache.state != 0;
        }
        StateListAnimator stateListAnimator = this.mStateListAnimator;
        if (stateListAnimator != null) {
            stateListAnimator.setState(state);
        }
        if (changed) {
            invalidate();
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.setHotspot(x, y);
        }
        Drawable drawable2 = this.mDefaultFocusHighlight;
        if (drawable2 != null) {
            drawable2.setHotspot(x, y);
        }
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        if (!(foregroundInfo == null || foregroundInfo.mDrawable == null)) {
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
        Context context;
        if (this.mDefaultFocusHighlightCache == null && (context = this.mContext) != null) {
            TypedArray ta = context.obtainStyledAttributes(new int[]{16843534});
            this.mDefaultFocusHighlightCache = ta.getDrawable(0);
            ta.recycle();
        }
        return this.mDefaultFocusHighlightCache;
    }

    private void setDefaultFocusHighlight(Drawable highlight) {
        ForegroundInfo foregroundInfo;
        this.mDefaultFocusHighlight = highlight;
        boolean z = true;
        this.mDefaultFocusHighlightSizeChanged = true;
        if (highlight != null) {
            int i = this.mPrivateFlags;
            if ((i & 128) != 0) {
                this.mPrivateFlags = i & -129;
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
        } else if ((this.mViewFlags & 128) != 0 && this.mBackground == null && ((foregroundInfo = this.mForegroundInfo) == null || foregroundInfo.mDrawable == null)) {
            this.mPrivateFlags |= 128;
        }
        invalidate();
    }

    public boolean isDefaultFocusHighlightNeeded(Drawable background, Drawable foreground) {
        return !isInTouchMode() && getDefaultFocusHighlightEnabled() && ((background == null || !background.isStateful() || !background.hasFocusStateSpecified()) && (foreground == null || !foreground.isStateful() || !foreground.hasFocusStateSpecified())) && isAttachedToWindow() && sUseDefaultFocusHighlight;
    }

    private void switchDefaultFocusHighlight() {
        if (isFocused()) {
            Drawable drawable = this.mBackground;
            ForegroundInfo foregroundInfo = this.mForegroundInfo;
            boolean needed = isDefaultFocusHighlightNeeded(drawable, foregroundInfo == null ? null : foregroundInfo.mDrawable);
            boolean active = this.mDefaultFocusHighlight != null;
            if (needed && !active) {
                setDefaultFocusHighlight(getDefaultFocusHighlightDrawable());
            } else if (!needed && active) {
                setDefaultFocusHighlight(null);
            }
        }
    }

    private void drawDefaultFocusHighlight(Canvas canvas) {
        Drawable drawable = this.mDefaultFocusHighlight;
        if (drawable != null) {
            if (this.mDefaultFocusHighlightSizeChanged) {
                this.mDefaultFocusHighlightSizeChanged = false;
                int l = this.mScrollX;
                int r = (this.mRight + l) - this.mLeft;
                int t = this.mScrollY;
                drawable.setBounds(l, t, r, (this.mBottom + t) - this.mTop);
            }
            this.mDefaultFocusHighlight.draw(canvas);
        }
    }

    public final int[] getDrawableState() {
        int[] iArr = this.mDrawableState;
        if (iArr != null && (this.mPrivateFlags & 1024) == 0) {
            return iArr;
        }
        this.mDrawableState = onCreateDrawableState(0);
        this.mPrivateFlags &= -1025;
        return this.mDrawableState;
    }

    /* access modifiers changed from: protected */
    public int[] onCreateDrawableState(int extraSpace) {
        if ((this.mViewFlags & 4194304) == 4194304) {
            ViewParent viewParent = this.mParent;
            if (viewParent instanceof View) {
                return ((View) viewParent).onCreateDrawableState(extraSpace);
            }
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
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null && attachInfo.mHardwareAccelerationRequested && ThreadedRenderer.isAvailable()) {
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
        if (drawableState == null) {
            return new int[extraSpace];
        }
        int[] fullState = new int[(drawableState.length + extraSpace)];
        System.arraycopy(drawableState, 0, fullState, 0, drawableState.length);
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
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.jumpToCurrentState();
        }
        StateListAnimator stateListAnimator = this.mStateListAnimator;
        if (stateListAnimator != null) {
            stateListAnimator.jumpToCurrentState();
        }
        Drawable drawable2 = this.mDefaultFocusHighlight;
        if (drawable2 != null) {
            drawable2.jumpToCurrentState();
        }
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        if (foregroundInfo != null && foregroundInfo.mDrawable != null) {
            this.mForegroundInfo.mDrawable.jumpToCurrentState();
        }
    }

    @RemotableViewMethod
    public void setBackgroundColor(int color) {
        Drawable drawable = this.mBackground;
        if (drawable instanceof ColorDrawable) {
            ((ColorDrawable) drawable.mutate()).setColor(color);
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
        ForegroundInfo foregroundInfo;
        computeOpaqueFlags();
        Drawable drawable = this.mBackground;
        if (background != drawable) {
            boolean requestLayout = false;
            this.mBackgroundResource = 0;
            if (drawable != null) {
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
                Drawable drawable2 = this.mBackground;
                if (!(drawable2 != null && drawable2.getMinimumHeight() == background.getMinimumHeight() && this.mBackground.getMinimumWidth() == background.getMinimumWidth())) {
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
                int i = this.mPrivateFlags;
                if ((i & 128) != 0) {
                    this.mPrivateFlags = i & -129;
                    requestLayout = true;
                }
            } else {
                this.mBackground = null;
                if ((this.mViewFlags & 128) != 0 && this.mDefaultFocusHighlight == null && ((foregroundInfo = this.mForegroundInfo) == null || foregroundInfo.mDrawable == null)) {
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
        TintInfo tintInfo = this.mBackgroundTint;
        tintInfo.mTintList = tint;
        tintInfo.mHasTintList = true;
        applyBackgroundTint();
    }

    public ColorStateList getBackgroundTintList() {
        TintInfo tintInfo = this.mBackgroundTint;
        if (tintInfo != null) {
            return tintInfo.mTintList;
        }
        return null;
    }

    public void setBackgroundTintMode(PorterDuff.Mode tintMode) {
        BlendMode mode = null;
        if (tintMode != null) {
            mode = BlendMode.fromValue(tintMode.nativeInt);
        }
        setBackgroundTintBlendMode(mode);
    }

    public void setBackgroundTintBlendMode(BlendMode blendMode) {
        if (this.mBackgroundTint == null) {
            this.mBackgroundTint = new TintInfo();
        }
        TintInfo tintInfo = this.mBackgroundTint;
        tintInfo.mBlendMode = blendMode;
        tintInfo.mHasTintMode = true;
        applyBackgroundTint();
    }

    public PorterDuff.Mode getBackgroundTintMode() {
        TintInfo tintInfo = this.mBackgroundTint;
        if (tintInfo == null || tintInfo.mBlendMode == null) {
            return null;
        }
        return BlendMode.blendModeToPorterDuffMode(this.mBackgroundTint.mBlendMode);
    }

    public BlendMode getBackgroundTintBlendMode() {
        TintInfo tintInfo = this.mBackgroundTint;
        if (tintInfo != null) {
            return tintInfo.mBlendMode;
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
                    this.mBackground.setTintBlendMode(tintInfo.mBlendMode);
                }
                if (this.mBackground.isStateful()) {
                    this.mBackground.setState(getDrawableState());
                }
            }
        }
    }

    public Drawable getForeground() {
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        if (foregroundInfo != null) {
            return foregroundInfo.mDrawable;
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
            this.mForegroundInfo.mDrawable = foreground;
            boolean z = true;
            this.mForegroundInfo.mBoundsChanged = true;
            if (foreground != null) {
                int i = this.mPrivateFlags;
                if ((i & 128) != 0) {
                    this.mPrivateFlags = i & -129;
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
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        if (foregroundInfo != null) {
            return foregroundInfo.mInsidePadding;
        }
        return true;
    }

    public int getForegroundGravity() {
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        if (foregroundInfo != null) {
            return foregroundInfo.mGravity;
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
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        if (foregroundInfo == null || foregroundInfo.mTintInfo == null) {
            return null;
        }
        return this.mForegroundInfo.mTintInfo.mTintList;
    }

    public void setForegroundTintMode(PorterDuff.Mode tintMode) {
        BlendMode mode = null;
        if (tintMode != null) {
            mode = BlendMode.fromValue(tintMode.nativeInt);
        }
        setForegroundTintBlendMode(mode);
    }

    public void setForegroundTintBlendMode(BlendMode blendMode) {
        if (this.mForegroundInfo == null) {
            this.mForegroundInfo = new ForegroundInfo();
        }
        if (this.mForegroundInfo.mTintInfo == null) {
            this.mForegroundInfo.mTintInfo = new TintInfo();
        }
        this.mForegroundInfo.mTintInfo.mBlendMode = blendMode;
        this.mForegroundInfo.mTintInfo.mHasTintMode = true;
        applyForegroundTint();
    }

    public PorterDuff.Mode getForegroundTintMode() {
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        BlendMode blendMode = (foregroundInfo == null || foregroundInfo.mTintInfo == null) ? null : this.mForegroundInfo.mTintInfo.mBlendMode;
        if (blendMode != null) {
            return BlendMode.blendModeToPorterDuffMode(blendMode);
        }
        return null;
    }

    public BlendMode getForegroundTintBlendMode() {
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        if (foregroundInfo == null || foregroundInfo.mTintInfo == null) {
            return null;
        }
        return this.mForegroundInfo.mTintInfo.mBlendMode;
    }

    private void applyForegroundTint() {
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        if (foregroundInfo != null && foregroundInfo.mDrawable != null && this.mForegroundInfo.mTintInfo != null) {
            TintInfo tintInfo = this.mForegroundInfo.mTintInfo;
            if (tintInfo.mHasTintList || tintInfo.mHasTintMode) {
                ForegroundInfo foregroundInfo2 = this.mForegroundInfo;
                foregroundInfo2.mDrawable = foregroundInfo2.mDrawable.mutate();
                if (tintInfo.mHasTintList) {
                    this.mForegroundInfo.mDrawable.setTintList(tintInfo.mTintList);
                }
                if (tintInfo.mHasTintMode) {
                    this.mForegroundInfo.mDrawable.setTintBlendMode(tintInfo.mBlendMode);
                }
                if (this.mForegroundInfo.mDrawable.isStateful()) {
                    this.mForegroundInfo.mDrawable.setState(getDrawableState());
                }
            }
        }
    }

    private Drawable getAutofilledDrawable() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo == null) {
            return null;
        }
        if (attachInfo.mAutofilledDrawable == null) {
            Context rootContext = getRootView().getContext();
            TypedArray a = rootContext.getTheme().obtainStyledAttributes(AUTOFILL_HIGHLIGHT_ATTR);
            int attributeResourceId = a.getResourceId(0, 0);
            this.mAttachInfo.mAutofilledDrawable = rootContext.getDrawable(attributeResourceId);
            a.recycle();
        }
        return this.mAttachInfo.mAutofilledDrawable;
    }

    private void drawAutofilledHighlight(Canvas canvas) {
        Drawable autofilledHighlight;
        if (isAutofilled() && (autofilledHighlight = getAutofilledDrawable()) != null) {
            autofilledHighlight.setBounds(0, 0, getWidth(), getHeight());
            autofilledHighlight.draw(canvas);
        }
    }

    public void onDrawForeground(Canvas canvas) {
        onDrawScrollIndicators(canvas);
        onDrawScrollBars(canvas);
        ForegroundInfo foregroundInfo = this.mForegroundInfo;
        Drawable foreground = foregroundInfo != null ? foregroundInfo.mDrawable : null;
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

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123768420)
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
                int i2 = this.mVerticalScrollbarPosition;
                if (i2 != 0) {
                    if (i2 == 1) {
                        left += offset;
                    } else if (i2 == 2) {
                        right += offset;
                    }
                } else if (isLayoutRtl()) {
                    left += offset;
                } else {
                    right += offset;
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

    public int getSourceLayoutResId() {
        return this.mSourceLayoutId;
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
        Drawable drawable = this.mBackground;
        return drawable == null ? Insets.NONE : drawable.getOpticalInsets();
    }

    @UnsupportedAppUsage
    public void resetPaddingToInitialValues() {
        if (isRtlCompatibilityMode()) {
            this.mPaddingLeft = this.mUserPaddingLeftInitial;
            this.mPaddingRight = this.mUserPaddingRightInitial;
        } else if (isLayoutRtl()) {
            int i = this.mUserPaddingEnd;
            if (i < 0) {
                i = this.mUserPaddingLeftInitial;
            }
            this.mPaddingLeft = i;
            int i2 = this.mUserPaddingStart;
            if (i2 < 0) {
                i2 = this.mUserPaddingRightInitial;
            }
            this.mPaddingRight = i2;
        } else {
            int i3 = this.mUserPaddingStart;
            if (i3 < 0) {
                i3 = this.mUserPaddingLeftInitial;
            }
            this.mPaddingLeft = i3;
            int i4 = this.mUserPaddingEnd;
            if (i4 < 0) {
                i4 = this.mUserPaddingRightInitial;
            }
            this.mPaddingRight = i4;
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
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mTreeObserver;
        }
        if (this.mFloatingTreeObserver == null) {
            this.mFloatingTreeObserver = new ViewTreeObserver(this.mContext);
        }
        return this.mFloatingTreeObserver;
    }

    public View getRootView() {
        View v;
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null && (v = attachInfo.mRootView) != null) {
            return v;
        }
        View parent = this;
        while (true) {
            ViewParent viewParent = parent.mParent;
            if (viewParent == null || !(viewParent instanceof View)) {
                break;
            }
            parent = (View) viewParent;
        }
        return parent;
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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

    public void transformMatrixToGlobal(Matrix matrix) {
        ViewParent parent = this.mParent;
        if (parent instanceof View) {
            View vp = (View) parent;
            vp.transformMatrixToGlobal(matrix);
            matrix.preTranslate((float) (-vp.mScrollX), (float) (-vp.mScrollY));
        } else if (parent instanceof ViewRootImpl) {
            ViewRootImpl vr = (ViewRootImpl) parent;
            vr.transformMatrixToGlobal(matrix);
            matrix.preTranslate(0.0f, (float) (-vr.mCurScrollY));
        }
        matrix.preTranslate((float) this.mLeft, (float) this.mTop);
        if (!hasIdentityMatrix()) {
            matrix.preConcat(getMatrix());
        }
    }

    public void transformMatrixToLocal(Matrix matrix) {
        ViewParent parent = this.mParent;
        if (parent instanceof View) {
            View vp = (View) parent;
            vp.transformMatrixToLocal(matrix);
            matrix.postTranslate((float) vp.mScrollX, (float) vp.mScrollY);
        } else if (parent instanceof ViewRootImpl) {
            ViewRootImpl vr = (ViewRootImpl) parent;
            vr.transformMatrixToLocal(matrix);
            matrix.postTranslate(0.0f, (float) vr.mCurScrollY);
        }
        matrix.postTranslate((float) (-this.mLeft), (float) (-this.mTop));
        if (!hasIdentityMatrix()) {
            matrix.postConcat(getInverseMatrix());
        }
    }

    @UnsupportedAppUsage
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT, indexMapping = {@ViewDebug.IntToString(from = 0, to = "x"), @ViewDebug.IntToString(from = 1, to = "y")})
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

    public void getAbsLocationOnScreen(int[] outLocation) {
        AttachInfo info = this.mAttachInfo;
        if (info == null || info.mViewRootImpl == null || info.mViewRootImpl.getWindowModeType() != 102) {
            getLocationOnScreen(outLocation);
            return;
        }
        getLocationInWindow(outLocation);
        float scale = HwActivityTaskManager.getStackScale(-100);
        outLocation[0] = info.mWindowLeft + ((int) (((float) outLocation[0]) * scale));
        outLocation[1] = info.mWindowTop + ((int) (((float) outLocation[1]) * scale));
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
        }
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo == null) {
            inOutLocation[1] = 0;
            inOutLocation[0] = 0;
            return;
        }
        float[] position = attachInfo.mTmpTransformLocation;
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

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: android.view.View */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public <T extends View> T findViewTraversal(int id) {
        if (id == this.mID) {
            return this;
        }
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: android.view.View */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public <T extends View> T findViewWithTagTraversal(Object tag) {
        if (tag == null || !tag.equals(this.mTag)) {
            return null;
        }
        return this;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: android.view.View */
    /* JADX WARN: Multi-variable type inference failed */
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
        return (T) findViewTraversal(id);
    }

    public final <T extends View> T requireViewById(int id) {
        T view = (T) findViewById(id);
        if (view != null) {
            return view;
        }
        throw new IllegalArgumentException("ID does not reference a View inside this View");
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: android.view.View */
    /* JADX WARN: Multi-variable type inference failed */
    public <T extends View> T findViewByAccessibilityIdTraversal(int accessibilityId) {
        if (getAccessibilityViewId() == accessibilityId) {
            return this;
        }
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: android.view.View */
    /* JADX WARN: Multi-variable type inference failed */
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
        return (T) findViewWithTagTraversal(tag);
    }

    public final <T extends View> T findViewByPredicate(Predicate<View> predicate) {
        return (T) findViewByPredicateTraversal(predicate, null);
    }

    public final <T extends View> T findViewByPredicateInsideOut(View start, Predicate<View> predicate) {
        T view;
        View childToSkip = null;
        while (true) {
            view = (T) start.findViewByPredicateTraversal(predicate, childToSkip);
            if (view != null || start == this) {
                break;
            }
            ViewParent parent = start.getParent();
            if (parent == null || !(parent instanceof View)) {
                return null;
            }
            childToSkip = start;
            start = (View) parent;
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

    @UnsupportedAppUsage
    public boolean isRootNamespace() {
        return (this.mPrivateFlags & 8) != 0;
    }

    @ViewDebug.CapturedViewProperty
    public int getId() {
        return this.mID;
    }

    public long getUniqueDrawingId() {
        return this.mRenderNode.getUniqueId();
    }

    @ViewDebug.ExportedProperty
    public Object getTag() {
        return this.mTag;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public Object getTag(int key) {
        SparseArray<Object> sparseArray = this.mKeyedTags;
        if (sparseArray != null) {
            return sparseArray.get(key);
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public void debug() {
        debug(0);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void debug(int depth) {
        String output;
        String output2 = debugIndent(depth - 1) + "+ " + this;
        int id = getId();
        if (id != -1) {
            output2 = output2 + " (id=" + id + ")";
        }
        Object tag = getTag();
        if (tag != null) {
            output2 = output2 + " (tag=" + tag + ")";
        }
        Log.d(VIEW_LOG_TAG, output2);
        if ((this.mPrivateFlags & 2) != 0) {
            Log.d(VIEW_LOG_TAG, debugIndent(depth) + " FOCUSED");
        }
        Log.d(VIEW_LOG_TAG, debugIndent(depth) + "frame={" + this.mLeft + ", " + this.mTop + ", " + this.mRight + ", " + this.mBottom + "} scroll={" + this.mScrollX + ", " + this.mScrollY + "} ");
        if (!(this.mPaddingLeft == 0 && this.mPaddingTop == 0 && this.mPaddingRight == 0 && this.mPaddingBottom == 0)) {
            Log.d(VIEW_LOG_TAG, debugIndent(depth) + "padding={" + this.mPaddingLeft + ", " + this.mPaddingTop + ", " + this.mPaddingRight + ", " + this.mPaddingBottom + "}");
        }
        Log.d(VIEW_LOG_TAG, debugIndent(depth) + "mMeasureWidth=" + this.mMeasuredWidth + " mMeasureHeight=" + this.mMeasuredHeight);
        String output3 = debugIndent(depth);
        ViewGroup.LayoutParams layoutParams = this.mLayoutParams;
        if (layoutParams == null) {
            output = output3 + "BAD! no layout params";
        } else {
            output = layoutParams.debug(output3);
        }
        Log.d(VIEW_LOG_TAG, output);
        Log.d(VIEW_LOG_TAG, ((debugIndent(depth) + "flags={") + printFlags(this.mViewFlags)) + "}");
        Log.d(VIEW_LOG_TAG, ((debugIndent(depth) + "privateFlags={") + printPrivateFlags(this.mPrivateFlags)) + "}");
    }

    protected static String debugIndent(int depth) {
        StringBuilder spaces = new StringBuilder(((depth * 2) + 3) * 2);
        for (int i = 0; i < (depth * 2) + 3; i++) {
            spaces.append(' ');
            spaces.append(' ');
        }
        return spaces.toString();
    }

    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    public int getBaseline() {
        return -1;
    }

    public boolean isInLayout() {
        ViewRootImpl viewRoot = getViewRootImpl();
        return viewRoot != null && viewRoot.isInLayout();
    }

    @Override // android.view.ViewParent
    public void requestLayout() {
        LongSparseLongArray longSparseLongArray = this.mMeasureCache;
        if (longSparseLongArray != null) {
            longSparseLongArray.clear();
        }
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null && attachInfo.mViewRequestingLayout == null) {
            ViewRootImpl viewRoot = getViewRootImpl();
            if (viewRoot == null || !viewRoot.isInLayout() || viewRoot.requestLayoutDuringLayout(this)) {
                this.mAttachInfo.mViewRequestingLayout = this;
            } else {
                return;
            }
        }
        this.mPrivateFlags |= 4096;
        this.mPrivateFlags |= Integer.MIN_VALUE;
        ViewParent viewParent = this.mParent;
        if (viewParent != null && !viewParent.isLayoutRequested()) {
            this.mParent.requestLayout();
        }
        AttachInfo attachInfo2 = this.mAttachInfo;
        if (attachInfo2 != null && attachInfo2.mViewRequestingLayout == this) {
            this.mAttachInfo.mViewRequestingLayout = null;
        }
    }

    public void forceLayout() {
        LongSparseLongArray longSparseLongArray = this.mMeasureCache;
        if (longSparseLongArray != null) {
            longSparseLongArray.clear();
        }
        this.mPrivateFlags |= 4096;
        this.mPrivateFlags |= Integer.MIN_VALUE;
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x0119  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0137  */
    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec2;
        int widthMeasureSpec2;
        int i;
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
        boolean needsLayout = true;
        boolean forceLayout = (this.mPrivateFlags & 4096) == 4096;
        boolean specChanged = (widthMeasureSpec2 == this.mOldWidthMeasureSpec && heightMeasureSpec2 == this.mOldHeightMeasureSpec) ? false : true;
        boolean isSpecExactly = MeasureSpec.getMode(widthMeasureSpec2) == 1073741824 && MeasureSpec.getMode(heightMeasureSpec2) == 1073741824;
        boolean matchesSpecSize = getMeasuredWidth() == MeasureSpec.getSize(widthMeasureSpec2) && getMeasuredHeight() == MeasureSpec.getSize(heightMeasureSpec2);
        if (!specChanged || (!sAlwaysRemeasureExactly && isSpecExactly && matchesSpecSize)) {
            needsLayout = false;
        }
        if (forceLayout || needsLayout) {
            this.mPrivateFlags &= -2049;
            resolveRtlPropertiesIfNeeded();
            int cacheIndex = forceLayout ? -1 : this.mMeasureCache.indexOfKey(key);
            if (cacheIndex >= 0) {
                if (!sIgnoreMeasureCache) {
                    long value = this.mMeasureCache.valueAt(cacheIndex);
                    setMeasuredDimensionRaw((int) (value >> 32), (int) value);
                    this.mPrivateFlags3 |= 8;
                    i = this.mPrivateFlags;
                    if ((i & 2048) != 2048) {
                        this.mPrivateFlags = i | 8192;
                    } else {
                        throw new IllegalStateException("View with id " + getId() + ": " + getClass().getName() + "#onMeasure() did not set the measured dimension by calling setMeasuredDimension()");
                    }
                }
            }
            if (ViewRootImpl.DEBUG_VIEW_TRACE) {
                Trace.traceBegin(8, "onMeasure " + getClass().getName());
            }
            onMeasure(widthMeasureSpec2, heightMeasureSpec2);
            if (ViewRootImpl.DEBUG_VIEW_TRACE) {
                Trace.traceEnd(8);
            }
            this.mPrivateFlags3 &= -9;
            i = this.mPrivateFlags;
            if ((i & 2048) != 2048) {
            }
        }
        this.mOldWidthMeasureSpec = widthMeasureSpec2;
        this.mOldHeightMeasureSpec = heightMeasureSpec2;
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
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode != Integer.MIN_VALUE) {
            if (specMode == 0) {
                return size;
            }
            if (specMode != 1073741824) {
                return size;
            }
        }
        return specSize;
    }

    /* access modifiers changed from: protected */
    public int getSuggestedMinimumHeight() {
        Drawable drawable = this.mBackground;
        return drawable == null ? this.mMinHeight : Math.max(this.mMinHeight, drawable.getMinimumHeight());
    }

    /* access modifiers changed from: protected */
    public int getSuggestedMinimumWidth() {
        Drawable drawable = this.mBackground;
        return drawable == null ? this.mMinWidth : Math.max(this.mMinWidth, drawable.getMinimumWidth());
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
        animation.setStartTime(-1);
        setAnimation(animation);
        invalidateParentCaches();
        invalidate(true);
    }

    public void clearAnimation() {
        Animation animation = this.mCurrentAnimation;
        if (animation != null) {
            animation.detach();
        }
        this.mCurrentAnimation = null;
        invalidateParentIfNeeded();
    }

    public void setAnimation(Animation animation) {
        this.mCurrentAnimation = animation;
        if (animation != null) {
            AttachInfo attachInfo = this.mAttachInfo;
            if (attachInfo != null && attachInfo.mDisplayState == 1 && animation.getStartTime() == -1) {
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

    @UnsupportedAppUsage
    public boolean gatherTransparentRegion(Region region) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (!(region == null || attachInfo == null)) {
            if ((this.mPrivateFlags & 128) == 0) {
                int[] location = attachInfo.mTransparentLocation;
                getLocationInWindow(location);
                int shadowOffset = getZ() > 0.0f ? (int) getZ() : 0;
                region.op(location[0] - shadowOffset, location[1] - shadowOffset, ((location[0] + this.mRight) - this.mLeft) + shadowOffset, ((location[1] + this.mBottom) - this.mTop) + (shadowOffset * 3), Region.Op.DIFFERENCE);
            } else {
                Drawable drawable = this.mBackground;
                if (!(drawable == null || drawable.getOpacity() == -2)) {
                    applyDrawableToTransparentRegion(this.mBackground, region);
                }
                ForegroundInfo foregroundInfo = this.mForegroundInfo;
                if (!(foregroundInfo == null || foregroundInfo.mDrawable == null || this.mForegroundInfo.mDrawable.getOpacity() == -2)) {
                    applyDrawableToTransparentRegion(this.mForegroundInfo.mDrawable, region);
                }
                Drawable drawable2 = this.mDefaultFocusHighlight;
                if (!(drawable2 == null || drawable2.getOpacity() == -2)) {
                    applyDrawableToTransparentRegion(this.mDefaultFocusHighlight, region);
                }
            }
        }
        return true;
    }

    public void playSoundEffect(int soundConstant) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null && attachInfo.mRootCallbacks != null && isSoundEffectsEnabled()) {
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
        AttachInfo attachInfo;
        if (visibility != this.mSystemUiVisibility) {
            this.mSystemUiVisibility = visibility;
            if (this.mParent != null && (attachInfo = this.mAttachInfo) != null && !attachInfo.mRecomputeGlobalAttributes) {
                this.mParent.recomputeViewAttributes(this);
            }
        }
    }

    public int getSystemUiVisibility() {
        return this.mSystemUiVisibility;
    }

    public int getWindowSystemUiVisibility() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null) {
            return attachInfo.mSystemUiVisibility;
        }
        return 0;
    }

    public void onWindowSystemUiVisibilityChanged(int visible) {
    }

    public void dispatchWindowSystemUiVisiblityChanged(int visible) {
        onWindowSystemUiVisibilityChanged(visible);
    }

    public void setOnSystemUiVisibilityChangeListener(OnSystemUiVisibilityChangeListener l) {
        AttachInfo attachInfo;
        getListenerInfo().mOnSystemUiVisibilityChangeListener = l;
        if (this.mParent != null && (attachInfo = this.mAttachInfo) != null && !attachInfo.mRecomputeGlobalAttributes) {
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
        int i = this.mSystemUiVisibility;
        int val = ((~localChanges) & i) | (localValue & localChanges);
        if (val == i) {
            return false;
        }
        setSystemUiVisibility(val);
        return true;
    }

    @UnsupportedAppUsage
    public void setDisabledSystemUiVisibility(int flags) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null && attachInfo.mDisabledSystemUiVisibility != flags) {
            this.mAttachInfo.mDisabledSystemUiVisibility = flags;
            ViewParent viewParent = this.mParent;
            if (viewParent != null) {
                viewParent.recomputeViewAttributes(this);
            }
        }
    }

    public static class DragShadowBuilder {
        int mClipTotalItemCnt;
        int mEnteredZoneMimeTypeSupportState;
        int mEnteredZoneSupportItemCnt;
        boolean mIsEnteredDropZone;
        @UnsupportedAppUsage
        private final WeakReference<View> mView;

        public DragShadowBuilder(View view) {
            this.mIsEnteredDropZone = false;
            this.mEnteredZoneMimeTypeSupportState = 0;
            this.mView = new WeakReference<>(view);
        }

        public DragShadowBuilder() {
            this.mIsEnteredDropZone = false;
            this.mEnteredZoneMimeTypeSupportState = 0;
            this.mView = new WeakReference<>(null);
        }

        public final View getView() {
            return this.mView.get();
        }

        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
            View view = this.mView.get();
            if (view != null) {
                outShadowSize.set(view.getWidth(), view.getHeight());
                outShadowTouchPoint.set(outShadowSize.x / 2, outShadowSize.y / 2);
                return;
            }
            Log.e(View.VIEW_LOG_TAG, "Asked for drag thumb metrics but no view");
        }

        public void onDrawShadow(Canvas canvas) {
            View view = this.mView.get();
            if (view != null) {
                view.draw(canvas);
            } else {
                Log.e(View.VIEW_LOG_TAG, "Asked to draw drag shadow but no view");
            }
        }

        public void setDragEnterExitState(boolean isEnterDropZone, int mimeTypeSupportState, int acceptableItemCnt) {
            this.mIsEnteredDropZone = isEnterDropZone;
            this.mEnteredZoneMimeTypeSupportState = mimeTypeSupportState;
            this.mEnteredZoneSupportItemCnt = acceptableItemCnt;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setClipItemTotalCnt(int totalItemCnt) {
            this.mClipTotalItemCnt = totalItemCnt;
        }
    }

    @Deprecated
    public final boolean startDrag(ClipData data, DragShadowBuilder shadowBuilder, Object myLocalState, int flags) {
        return startDragAndDrop(data, shadowBuilder, myLocalState, flags);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:144:0x02a8, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x02a9, code lost:
        r12 = r3;
        r20 = r3;
        r21 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x02b7, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x02b8, code lost:
        r12 = r3;
        r20 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x0311, code lost:
        r12.destroy();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x0328, code lost:
        r12.destroy();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0226 A[SYNTHETIC, Splitter:B:108:0x0226] */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x0253 A[SYNTHETIC, Splitter:B:119:0x0253] */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0263  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0266  */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x026a  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x02b7 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:79:0x018a] */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x0311  */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x0328  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0176  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0184  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0187  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0191 A[SYNTHETIC, Splitter:B:83:0x0191] */
    public final boolean startDragAndDrop(ClipData data, DragShadowBuilder shadowBuilder, Object myLocalState, int flags) {
        SurfaceSession session;
        Surface surface;
        int flags2;
        Exception e;
        boolean z;
        Exception e2;
        IBinder token;
        int flags3 = flags;
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo == null) {
            Log.w(VIEW_LOG_TAG, "startDragAndDrop called on a detached view.");
            return false;
        } else if (!attachInfo.mViewRootImpl.mSurface.isValid()) {
            Log.w(VIEW_LOG_TAG, "startDragAndDrop called with an invalid surface.");
            return false;
        } else {
            HwViewEx.mShadow = shadowBuilder;
            HwViewEx.attachInfoForDrag = this.mAttachInfo;
            if (data != null) {
                data.prepareToLeaveProcess((flags3 & 256) != 0);
                HwViewEx.mShadow.setClipItemTotalCnt(data.getItemCount());
            }
            HwViewEx.mIsAddReceptableFlag = false;
            Point shadowSize = new Point();
            Point shadowTouchPoint = new Point();
            shadowBuilder.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
            if (shadowSize.x < 0 || shadowSize.y < 0 || shadowTouchPoint.x < 0 || shadowTouchPoint.y < 0) {
                throw new IllegalStateException("Drag shadow dimensions must not be negative");
            }
            if (shadowSize.x == 0 || shadowSize.y == 0) {
                if (sAcceptZeroSizeDragShadow) {
                    shadowSize.x = 1;
                    shadowSize.y = 1;
                } else {
                    throw new IllegalStateException("Drag shadow dimensions must be positive");
                }
            }
            boolean isInBasicMode = HwPCUtils.isInBasicMode();
            if (isInBasicMode || HwPCUtils.isInSinkWindowsCastMode()) {
                Point forbiddenAdd = HwViewEx.getAddAndForbiddenSize(this.mContext);
                shadowSize.x += forbiddenAdd.x / 2;
                shadowSize.y += forbiddenAdd.y / 2;
                HwWindowManager.registerHwMultiDisplayDragStateListener(HwViewEx.getListener());
                if (isInBasicMode) {
                    HwViewEx.sDragState = 0;
                } else {
                    HwWindowManager.updateDragState(0);
                }
                HwViewEx.mIsAddReceptableFlag = false;
            }
            if (HwViewEx.mIsAddReceptableFlag) {
                HwViewEx.hwGetSurfaceSize(this.mContext, shadowSize);
            }
            ViewRootImpl root = this.mAttachInfo.mViewRootImpl;
            SurfaceSession session2 = new SurfaceSession();
            SurfaceControl surfaceControl = new SurfaceControl.Builder(session2).setName("drag surface").setParent(root.getSurfaceControl()).setBufferSize(shadowSize.x, shadowSize.y).setFormat(-3).build();
            Surface surface2 = new Surface();
            surface2.copyFrom(surfaceControl);
            IBinder token2 = null;
            if (!isInBasicMode) {
                try {
                    if (!HwPCUtils.isInSinkWindowsCastMode()) {
                        if (HwViewEx.mIsAddReceptableFlag) {
                            flags3 |= 4194304;
                            try {
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("PointShadowSize", shadowSize);
                                bundle.putParcelable("PointShadowTouch", shadowTouchPoint);
                                HwViewEx.hwOnDrawShadow(this.mContext, data, shadowBuilder, bundle, surface2);
                            } catch (Exception e3) {
                                e2 = e3;
                                flags2 = flags3;
                                surface = surface2;
                                session = session2;
                                z = false;
                            } catch (Throwable th) {
                                e = th;
                                flags2 = flags3;
                                surface = surface2;
                                session = session2;
                                if (token2 == null) {
                                }
                                session.kill();
                                throw e;
                            }
                        } else {
                            Canvas canvas = surface2.lockCanvas(null);
                            try {
                                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                                shadowBuilder.onDrawShadow(canvas);
                            } finally {
                                surface2.unlockCanvasAndPost(canvas);
                            }
                        }
                        HwWindowManager.setDragStartBitmap(shadowBuilder, shadowSize);
                        if (isInBasicMode) {
                            HwWindowManager.dragStartForBasicMode(data, shadowBuilder, shadowSize);
                        }
                        root.getLastTouchPoint(shadowSize);
                        flags2 = flags3 | (root.getLastTouchSource() != 4098 ? 536870912 : 0);
                        try {
                            root.setLocalDragShadow(this);
                            if ((flags2 & 1073741824) != 0) {
                                try {
                                    if (myLocalState instanceof Point) {
                                        shadowSize.x = ((Point) myLocalState).x;
                                        shadowSize.y = ((Point) myLocalState).y;
                                    }
                                } catch (Exception e4) {
                                    e2 = e4;
                                    surface = surface2;
                                    session = session2;
                                    z = false;
                                    try {
                                        Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e2);
                                        if (token2 == null) {
                                        }
                                        session.kill();
                                        return z;
                                    } catch (Throwable th2) {
                                        e = th2;
                                    }
                                } catch (Throwable th3) {
                                    e = th3;
                                    surface = surface2;
                                    session = session2;
                                    if (token2 == null) {
                                    }
                                    session.kill();
                                    throw e;
                                }
                            }
                            if (isInBasicMode && (myLocalState instanceof Point) && HwPCUtils.ASSOCIATEASS_PACKAGE_NAME.equals(this.mContext.getBasePackageName())) {
                                shadowSize.x = ((Point) myLocalState).x;
                                shadowSize.y = ((Point) myLocalState).y;
                            }
                        } catch (Exception e5) {
                            e2 = e5;
                            surface = surface2;
                            session = session2;
                            z = false;
                            Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e2);
                            if (token2 == null) {
                            }
                            session.kill();
                            return z;
                        } catch (Throwable th4) {
                        }
                        try {
                            surface = surface2;
                            session = session2;
                            z = false;
                            try {
                                token = this.mAttachInfo.mSession.performDrag(this.mAttachInfo.mWindow, flags2, surfaceControl, root.getLastTouchSource(), (float) shadowSize.x, (float) shadowSize.y, (float) shadowTouchPoint.x, (float) shadowTouchPoint.y, data);
                                if (token != null) {
                                    try {
                                        if (this.mAttachInfo.mDragSurface != null) {
                                            this.mAttachInfo.mDragSurface.release();
                                        }
                                        this.mAttachInfo.mDragSurface = surface;
                                        this.mAttachInfo.mDragToken = token;
                                        root.setLocalDragState(myLocalState);
                                    } catch (Exception e6) {
                                        e2 = e6;
                                        token2 = token;
                                        flags2 = flags2;
                                        Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e2);
                                        if (token2 == null) {
                                        }
                                        session.kill();
                                        return z;
                                    } catch (Throwable th5) {
                                        e = th5;
                                        token2 = token;
                                        flags2 = flags2;
                                        if (token2 == null) {
                                        }
                                        session.kill();
                                        throw e;
                                    }
                                }
                                flags2 = flags2;
                                if ((flags2 & 1024) != 0) {
                                    try {
                                        setOriginalDragViewCenter();
                                    } catch (Exception e7) {
                                        e2 = e7;
                                        token2 = token;
                                    } catch (Throwable th6) {
                                        e = th6;
                                        token2 = token;
                                        if (token2 == null) {
                                        }
                                        session.kill();
                                        throw e;
                                    }
                                }
                                boolean z2 = token == null;
                                if (token == null) {
                                    surface.destroy();
                                }
                                if ((flags2 & 1073741824) == 0 || Process.myUid() != 1000) {
                                    session.kill();
                                }
                                return z2;
                            } catch (Exception e8) {
                                e2 = e8;
                                flags2 = flags2;
                                Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e2);
                                if (token2 == null) {
                                }
                                session.kill();
                                return z;
                            } catch (Throwable th7) {
                                e = th7;
                                flags2 = flags2;
                                if (token2 == null) {
                                }
                                session.kill();
                                throw e;
                            }
                        } catch (Exception e9) {
                            e2 = e9;
                            flags2 = flags2;
                            surface = surface2;
                            session = session2;
                            z = false;
                            Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e2);
                            if (token2 == null) {
                            }
                            session.kill();
                            return z;
                        } catch (Throwable th8) {
                            e = th8;
                            flags2 = flags2;
                            surface = surface2;
                            session = session2;
                            if (token2 == null) {
                            }
                            session.kill();
                            throw e;
                        }
                    }
                } catch (Exception e10) {
                    e2 = e10;
                    flags2 = flags3;
                    surface = surface2;
                    session = session2;
                    z = false;
                    Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e2);
                    if (token2 == null) {
                    }
                    session.kill();
                    return z;
                } catch (Throwable th9) {
                    e = th9;
                    flags2 = flags3;
                    surface = surface2;
                    session = session2;
                    if (token2 == null) {
                    }
                    session.kill();
                    throw e;
                }
            }
            try {
                HwViewEx.setClipData(data);
                HwViewEx.initBitmapAddAndForbidden(this, this.mContext, surfaceControl, shadowSize, shadowBuilder);
            } catch (Exception e11) {
                e2 = e11;
                surface = surface2;
                session = session2;
                z = false;
                flags2 = flags3;
                Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e2);
                if (token2 == null) {
                }
                session.kill();
                return z;
            } catch (Throwable th10) {
                e = th10;
                surface = surface2;
                session = session2;
                flags2 = flags3;
                if (token2 == null) {
                }
                session.kill();
                throw e;
            }
            try {
                HwWindowManager.setDragStartBitmap(shadowBuilder, shadowSize);
                if (isInBasicMode) {
                }
                root.getLastTouchPoint(shadowSize);
                flags2 = flags3 | (root.getLastTouchSource() != 4098 ? 536870912 : 0);
                root.setLocalDragShadow(this);
                if ((flags2 & 1073741824) != 0) {
                }
                shadowSize.x = ((Point) myLocalState).x;
                shadowSize.y = ((Point) myLocalState).y;
                surface = surface2;
                session = session2;
                z = false;
                token = this.mAttachInfo.mSession.performDrag(this.mAttachInfo.mWindow, flags2, surfaceControl, root.getLastTouchSource(), (float) shadowSize.x, (float) shadowSize.y, (float) shadowTouchPoint.x, (float) shadowTouchPoint.y, data);
                if (token != null) {
                }
                flags2 = flags2;
                if ((flags2 & 1024) != 0) {
                }
                if (token == null) {
                }
                if (token == null) {
                }
                session.kill();
                return z2;
            } catch (Exception e12) {
                e2 = e12;
                surface = surface2;
                session = session2;
                z = false;
                flags2 = flags3;
                Log.e(VIEW_LOG_TAG, "Unable to initiate drag", e2);
                if (token2 == null) {
                }
                session.kill();
                return z;
            } catch (Throwable th11) {
                e = th11;
                surface = surface2;
                session = session2;
                flags2 = flags3;
                if (token2 == null) {
                }
                session.kill();
                throw e;
            }
        }
    }

    private void setOriginalDragViewCenter() throws RemoteException {
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        this.mAttachInfo.mSession.setOriginalDragViewCenter(this.mAttachInfo.mWindow, loc[0] + (getWidth() / 2), loc[1] + (getHeight() / 2));
    }

    public final void cancelDragAndDrop() {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo == null) {
            Log.w(VIEW_LOG_TAG, "cancelDragAndDrop called on a detached view.");
        } else if (attachInfo.mDragToken != null) {
            try {
                this.mAttachInfo.mSession.cancelDragAndDrop(this.mAttachInfo.mDragToken, false);
            } catch (Exception e) {
                Log.e(VIEW_LOG_TAG, "Unable to cancel drag", e);
            }
            this.mAttachInfo.mDragToken = null;
        } else {
            Log.e(VIEW_LOG_TAG, "No active drag to cancel");
        }
    }

    public final void updateDragShadow(DragShadowBuilder shadowBuilder) {
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo == null) {
            Log.w(VIEW_LOG_TAG, "updateDragShadow called on a detached view.");
        } else if (attachInfo.mDragToken != null) {
            try {
                Canvas canvas = this.mAttachInfo.mDragSurface.lockCanvas(null);
                try {
                    canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                    shadowBuilder.onDrawShadow(canvas);
                } finally {
                    this.mAttachInfo.mDragSurface.unlockCanvasAndPost(canvas);
                }
            } catch (Exception e) {
                Log.e(VIEW_LOG_TAG, "Unable to update drag shadow", e);
            }
            if (HwPCUtils.isInSinkWindowsCastMode() && HwPCUtils.ASSOCIATEASS_PACKAGE_NAME.equals(this.mContext.getBasePackageName())) {
                HwViewEx.setClipData(null);
                HwViewEx.initBitmapAddAndForbidden(this, this.mContext, null, null, shadowBuilder);
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

    public void finishMovingTask() {
        try {
            this.mAttachInfo.mSession.finishMovingTask(this.mAttachInfo.mWindow);
        } catch (RemoteException e) {
            Log.e(VIEW_LOG_TAG, "Unable to finish moving", e);
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
        AttachInfo attachInfo;
        AttachInfo attachInfo2;
        ListenerInfo li = this.mListenerInfo;
        if (li == null || li.mOnDragListener == null || (this.mViewFlags & 32) != 0 || !li.mOnDragListener.onDrag(this, event)) {
            result = onDragEvent(event);
        } else {
            result = true;
        }
        if (HwViewEx.mIsAddReceptableFlag) {
            HwViewEx.verifyDragDataMimeType(event, result, this);
        }
        int i = event.mAction;
        if (i == 4) {
            this.mPrivateFlags2 &= -4;
            refreshDrawableState();
            HwViewEx.attachInfoForDrag = null;
        } else if (i == 5) {
            this.mPrivateFlags2 |= 2;
            refreshDrawableState();
            if (!(!HwViewEx.mIsAddReceptableFlag || (attachInfo = this.mAttachInfo) == null || attachInfo.mSession == null)) {
                try {
                    this.mAttachInfo.mSession.dragRecipientFeedback(this.mAttachInfo.mWindow, true, getListenerInfo().mMimeTypeSupportState, getListenerInfo().mAcceptableItemCnt);
                    Log.i(VIEW_LOG_TAG, "view = " + this + ", supportState = " + getListenerInfo().mMimeTypeSupportState + ", mAcceptableMimeType = " + getListenerInfo().mAcceptableMimeType);
                } catch (RemoteException e) {
                    Log.i(VIEW_LOG_TAG, "View RemoteException " + e);
                }
            }
        } else if (i == 6) {
            this.mPrivateFlags2 &= -3;
            refreshDrawableState();
            if (!(!HwViewEx.mIsAddReceptableFlag || (attachInfo2 = this.mAttachInfo) == null || attachInfo2.mSession == null)) {
                try {
                    this.mAttachInfo.mSession.dragRecipientFeedback(this.mAttachInfo.mWindow, false, 0, 0);
                } catch (RemoteException e2) {
                    Log.i(VIEW_LOG_TAG, "View RemoteException " + e2);
                }
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public boolean canAcceptDrag() {
        return (this.mPrivateFlags2 & 1) != 0;
    }

    public void updateDragEnterExitState(boolean isEntered, int mimeTypeSupportState, int acceptableItemCnt) {
        HwViewEx.updateDragEnterExitState(isEntered, mimeTypeSupportState, acceptableItemCnt, this);
    }

    @UnsupportedAppUsage
    public void onCloseSystemDialogs(String reason) {
    }

    @UnsupportedAppUsage
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
    /* access modifiers changed from: public */
    private void checkForLongClick(long delay, float x, float y, int classification) {
        int i = this.mViewFlags;
        if ((i & 2097152) == 2097152 || (i & 1073741824) == 1073741824) {
            this.mHasPerformedLongPress = false;
            if (this.mPendingCheckForLongPress == null) {
                this.mPendingCheckForLongPress = new CheckForLongPress();
            }
            this.mPendingCheckForLongPress.setAnchor(x, y);
            this.mPendingCheckForLongPress.rememberWindowAttachCount();
            this.mPendingCheckForLongPress.rememberPressedState();
            this.mPendingCheckForLongPress.setClassification(classification);
            postDelayed(this.mPendingCheckForLongPress, delay);
        }
    }

    public static View inflate(Context context, int resource, ViewGroup root) {
        return LayoutInflater.from(context).inflate(resource, root);
    }

    /* access modifiers changed from: protected */
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int maxOverScrollX2;
        int maxOverScrollY2;
        boolean clampedX;
        boolean clampedY;
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
        int top = -maxOverScrollY2;
        int bottom = maxOverScrollY2 + scrollRangeY;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        } else {
            clampedX = false;
        }
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        } else {
            clampedY = false;
        }
        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);
        return clampedX || clampedY;
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
        if (!isNestedScrollingEnabled()) {
            return false;
        }
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
        return false;
    }

    public void stopNestedScroll() {
        ViewParent viewParent = this.mNestedScrollingParent;
        if (viewParent != null) {
            viewParent.onStopNestedScroll(this);
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
                    return false;
                }
                return true;
            } else if (offsetInWindow != null) {
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        ViewParent viewParent;
        if (!isNestedScrollingEnabled() || (viewParent = this.mNestedScrollingParent) == null) {
            return false;
        }
        return viewParent.onNestedFling(this, velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        ViewParent viewParent;
        if (!isNestedScrollingEnabled() || (viewParent = this.mNestedScrollingParent) == null) {
            return false;
        }
        return viewParent.onNestedPreFling(this, velocityX, velocityY);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
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
    @UnsupportedAppUsage
    public float getHorizontalScrollFactor() {
        return getVerticalScrollFactor();
    }

    @UnsupportedAppUsage
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
                        this.mPrivateFlags2 = this.mPrivateFlags2 | 1536;
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
        ViewParent viewParent = this.mParent;
        if (viewParent == null) {
            return false;
        }
        try {
            return viewParent.canResolveTextDirection();
        } catch (AbstractMethodError e) {
            Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
            return false;
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

    @UnsupportedAppUsage
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
                                    this.mPrivateFlags2 |= 131072;
                                    break;
                            }
                        } else {
                            this.mPrivateFlags2 = 131072 | this.mPrivateFlags2;
                            return false;
                        }
                    } catch (AbstractMethodError e2) {
                        Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e2);
                        this.mPrivateFlags2 = this.mPrivateFlags2 | 196608;
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
                    this.mPrivateFlags2 |= 131072;
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
        ViewParent viewParent = this.mParent;
        if (viewParent == null) {
            return false;
        }
        try {
            return viewParent.canResolveTextAlignment();
        } catch (AbstractMethodError e) {
            Log.e(VIEW_LOG_TAG, this.mParent.getClass().getSimpleName() + " does not fully implement ViewParent", e);
            return false;
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
        String transitionName;
        if ((getVisibility() == 0 || this.mGhostView != null) && (transitionName = getTransitionName()) != null) {
            namedElements.put(transitionName, this);
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
        AttachInfo attachInfo = this.mAttachInfo;
        if (attachInfo != null && !attachInfo.mHandlingPointerEvent) {
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
            return (1073741823 & size) | (-1073741824 & mode);
        }

        @UnsupportedAppUsage
        public static int makeSafeMeasureSpec(int size, int mode) {
            if (!View.sUseZeroUnspecifiedMeasureSpec || mode != 0) {
                return makeMeasureSpec(size, mode);
            }
            return 0;
        }

        public static int getMode(int measureSpec) {
            return -1073741824 & measureSpec;
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

    /* access modifiers changed from: private */
    public final class CheckForLongPress implements Runnable {
        private int mClassification;
        private boolean mOriginalPressedState;
        private int mOriginalWindowAttachCount;
        private float mX;
        private float mY;

        private CheckForLongPress() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.mOriginalPressedState == View.this.isPressed() && View.this.mParent != null && this.mOriginalWindowAttachCount == View.this.mWindowAttachCount) {
                View.this.recordGestureClassification(this.mClassification);
                if (!View.this.getRootView().isLongPressSwipe() && View.this.performLongClick(this.mX, this.mY)) {
                    View.this.mHasPerformedLongPress = true;
                }
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

        public void setClassification(int classification) {
            this.mClassification = classification;
        }
    }

    /* access modifiers changed from: private */
    public final class CheckForTap implements Runnable {
        public float x;
        public float y;

        private CheckForTap() {
        }

        @Override // java.lang.Runnable
        public void run() {
            View.this.mPrivateFlags &= -33554433;
            View.this.setPressed(true, this.x, this.y);
            View.this.checkForLongClick((long) (ViewConfiguration.getLongPressTimeout() - ViewConfiguration.getTapTimeout()), this.x, this.y, 3);
        }
    }

    /* access modifiers changed from: private */
    public final class PerformClick implements Runnable {
        private PerformClick() {
        }

        @Override // java.lang.Runnable
        public void run() {
            ListenerInfo li = View.this.mListenerInfo;
            IHwRtgSchedImpl hwRtgSchedImpl = null;
            if (!(li == null || li.mOnClickListener == null || (hwRtgSchedImpl = HwFrameworkFactory.getHwRtgSchedImpl()) == null)) {
                hwRtgSchedImpl.beginClickFreq();
            }
            View.this.recordGestureClassification(1);
            View.this.performClickInternal();
            if (hwRtgSchedImpl != null) {
                hwRtgSchedImpl.endClickFreq();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recordGestureClassification(int classification) {
        if (classification != 0) {
            StatsLog.write(177, getClass().getName(), classification);
        }
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

    /* access modifiers changed from: private */
    public final class UnsetPressedState implements Runnable {
        private UnsetPressedState() {
        }

        @Override // java.lang.Runnable
        public void run() {
            View.this.setPressed(false);
        }
    }

    /* access modifiers changed from: private */
    public static class VisibilityChangeForAutofillHandler extends Handler {
        private final AutofillManager mAfm;
        private final View mView;

        private VisibilityChangeForAutofillHandler(AutofillManager afm, View view) {
            this.mAfm = afm;
            this.mView = view;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            AutofillManager autofillManager = this.mAfm;
            View view = this.mView;
            autofillManager.notifyViewVisibilityChanged(view, view.isShown());
        }
    }

    public static class BaseSavedState extends AbsSavedState {
        static final int AUTOFILL_ID = 4;
        public static final Parcelable.Creator<BaseSavedState> CREATOR = new Parcelable.ClassLoaderCreator<BaseSavedState>() {
            /* class android.view.View.BaseSavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }

            @Override // android.os.Parcelable.ClassLoaderCreator
            public BaseSavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new BaseSavedState(in, loader);
            }

            @Override // android.os.Parcelable.Creator
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

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.mSavedData);
            out.writeString(this.mStartActivityRequestWhoSaved);
            out.writeBoolean(this.mIsAutofilled);
            out.writeInt(this.mAutofillViewId);
        }
    }

    /* access modifiers changed from: package-private */
    public static final class AttachInfo {
        int mAccessibilityFetchFlags;
        Drawable mAccessibilityFocusDrawable;
        int mAccessibilityWindowId = -1;
        boolean mAlwaysConsumeSystemBars;
        @UnsupportedAppUsage
        float mApplicationScale;
        Drawable mAutofilledDrawable;
        Canvas mCanvas;
        @UnsupportedAppUsage
        final Rect mContentInsets = new Rect();
        boolean mDebugLayout = DisplayProperties.debug_layout().orElse(false).booleanValue();
        int mDisabledSystemUiVisibility;
        Display mDisplay;
        final DisplayCutout.ParcelableWrapper mDisplayCutout = new DisplayCutout.ParcelableWrapper(DisplayCutout.NO_CUTOUT);
        @UnsupportedAppUsage
        int mDisplayState = 0;
        public Surface mDragSurface;
        IBinder mDragToken;
        @UnsupportedAppUsage
        long mDrawingTime;
        boolean mForceReportNewAttributes;
        @UnsupportedAppUsage
        final ViewTreeObserver.InternalInsetsInfo mGivenInternalInsets = new ViewTreeObserver.InternalInsetsInfo();
        int mGlobalSystemUiVisibility = -1;
        @UnsupportedAppUsage
        final Handler mHandler;
        boolean mHandlingPointerEvent;
        boolean mHardwareAccelerated;
        boolean mHardwareAccelerationRequested;
        boolean mHasNonEmptyGivenInternalInsets;
        boolean mHasSystemUiListeners;
        @UnsupportedAppUsage
        boolean mHasWindowFocus;
        IWindowId mIWindowId;
        @UnsupportedAppUsage
        boolean mInTouchMode;
        final int[] mInvalidateChildLocation = new int[2];
        @UnsupportedAppUsage
        boolean mKeepScreenOn;
        @UnsupportedAppUsage
        final KeyEvent.DispatcherState mKeyDispatchState = new KeyEvent.DispatcherState();
        boolean mNeedsUpdateLightCenter;
        final Rect mOutsets = new Rect();
        final Rect mOverscanInsets = new Rect();
        boolean mOverscanRequested;
        IBinder mPanelParentWindowToken;
        List<RenderNode> mPendingAnimatingRenderNodes;
        final Point mPoint = new Point();
        @UnsupportedAppUsage
        boolean mRecomputeGlobalAttributes;
        final Callbacks mRootCallbacks;
        View mRootView;
        @UnsupportedAppUsage
        boolean mScalingRequired;
        @UnsupportedAppUsage
        final ArrayList<View> mScrollContainers = new ArrayList<>();
        @UnsupportedAppUsage
        final IWindowSession mSession;
        @UnsupportedAppUsage
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
        @UnsupportedAppUsage
        final ViewTreeObserver mTreeObserver;
        boolean mUnbufferedDispatchRequested;
        boolean mUse32BitDrawingCache;
        View mViewRequestingLayout;
        final ViewRootImpl mViewRootImpl;
        @UnsupportedAppUsage
        boolean mViewScrollChanged;
        @UnsupportedAppUsage
        boolean mViewVisibilityChanged;
        @UnsupportedAppUsage
        final Rect mVisibleInsets = new Rect();
        @UnsupportedAppUsage
        final IWindow mWindow;
        WindowId mWindowId;
        int mWindowLeft;
        final IBinder mWindowToken;
        int mWindowTop;
        int mWindowVisibility;

        /* access modifiers changed from: package-private */
        public interface Callbacks {
            boolean performHapticFeedback(int i, boolean z);

            void playSoundEffect(int i);
        }

        /* access modifiers changed from: package-private */
        public static class InvalidateInfo {
            private static final int POOL_LIMIT = 10;
            private static final Pools.SynchronizedPool<InvalidateInfo> sPool = new Pools.SynchronizedPool<>(10);
            @UnsupportedAppUsage
            int bottom;
            @UnsupportedAppUsage
            int left;
            @UnsupportedAppUsage
            int right;
            @UnsupportedAppUsage
            View target;
            @UnsupportedAppUsage
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

    /* access modifiers changed from: private */
    public static class ScrollabilityCache implements Runnable {
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
        @UnsupportedAppUsage
        public View host;
        public float[] interpolatorValues;
        private int mLastColor;
        public final Rect mScrollBarBounds = new Rect();
        public float mScrollBarDraggingPos = 0.0f;
        public int mScrollBarDraggingState = 0;
        public final Rect mScrollBarTouchBounds = new Rect();
        public final Matrix matrix;
        public final Paint paint;
        @UnsupportedAppUsage
        public ScrollBarDrawable scrollBar;
        public int scrollBarDefaultDelayBeforeFade;
        public int scrollBarFadeDuration;
        public final Interpolator scrollBarInterpolator = new Interpolator(1, 2);
        public int scrollBarMinTouchTarget;
        public int scrollBarSize;
        public Shader shader;
        @UnsupportedAppUsage
        public int state = 0;

        public ScrollabilityCache(ViewConfiguration configuration, View host2) {
            this.fadingEdgeLength = configuration.getScaledFadingEdgeLength();
            this.scrollBarSize = configuration.getScaledScrollBarSize();
            this.scrollBarMinTouchTarget = configuration.getScaledMinScrollbarTouchTarget();
            this.scrollBarDefaultDelayBeforeFade = ViewConfiguration.getScrollDefaultDelay();
            this.scrollBarFadeDuration = ViewConfiguration.getScrollBarFadeDuration();
            this.paint = new Paint();
            this.matrix = new Matrix();
            this.shader = new LinearGradient(0.0f, 0.0f, 0.0f, 1.0f, -16777216, 0, Shader.TileMode.CLAMP);
            this.paint.setShader(this.shader);
            this.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            this.host = host2;
        }

        public void setFadeColor(int color) {
            if (color != this.mLastColor) {
                this.mLastColor = color;
                if (color != 0) {
                    this.shader = new LinearGradient(0.0f, 0.0f, 0.0f, 1.0f, color | -16777216, color & 16777215, Shader.TileMode.CLAMP);
                    this.paint.setShader(this.shader);
                    this.paint.setXfermode(null);
                    return;
                }
                this.shader = new LinearGradient(0.0f, 0.0f, 0.0f, 1.0f, -16777216, 0, Shader.TileMode.CLAMP);
                this.paint.setShader(this.shader);
                this.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            }
        }

        @Override // java.lang.Runnable
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

    /* access modifiers changed from: private */
    public class SendViewScrolledAccessibilityEvent implements Runnable {
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

        @Override // java.lang.Runnable
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
        /* access modifiers changed from: public */
        private void reset() {
            this.mIsPending = false;
            this.mDeltaX = 0;
            this.mDeltaY = 0;
        }
    }

    @UnsupportedAppUsage
    private void cancel(SendViewScrolledAccessibilityEvent callback) {
        if (callback != null && callback.mIsPending) {
            removeCallbacks(callback);
            callback.reset();
        }
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

        @UnsupportedAppUsage
        public AccessibilityNodeInfo createAccessibilityNodeInfo(View host) {
            return host.createAccessibilityNodeInfoInternal();
        }
    }

    /* access modifiers changed from: private */
    public static class MatchIdPredicate implements Predicate<View> {
        public int mId;

        private MatchIdPredicate() {
        }

        public boolean test(View view) {
            return view.mID == this.mId;
        }
    }

    /* access modifiers changed from: private */
    public static class MatchLabelForPredicate implements Predicate<View> {
        private int mLabeledId;

        private MatchLabelForPredicate() {
        }

        public boolean test(View view) {
            return view.mLabelForId == this.mLabeledId;
        }
    }

    private static void dumpFlags() {
        HashMap<String, String> found = Maps.newHashMap();
        try {
            Field[] declaredFields = View.class.getDeclaredFields();
            for (Field field : declaredFields) {
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
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                Log.d(VIEW_LOG_TAG, found.get(it.next()));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void dumpFlag(HashMap<String, String> found, String name, int value) {
        String bits = String.format("%32s", Integer.toBinaryString(value)).replace('0', ' ');
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
        TransformationInfo transformationInfo = this.mTransformationInfo;
        stream.addProperty("misc:transformation.alpha", transformationInfo != null ? transformationInfo.mAlpha : 0.0f);
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
        Rect rect = this.mClipBounds;
        stream.addProperty("drawing:clipBounds", rect == null ? null : rect.toString());
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
        String[] strArr = this.mAttributes;
        int n = strArr != null ? strArr.length : 0;
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
            return true;
        }
        return false;
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
            TooltipInfo tooltipInfo = this.mTooltipInfo;
            tooltipInfo.mShowTooltipRunnable = new Runnable() {
                /* class android.view.$$Lambda$View$llq76MkPXP4bNcb9oJt_msw0fnQ */

                @Override // java.lang.Runnable
                public final void run() {
                    boolean unused = View.this.showHoverTooltip();
                }
            };
            tooltipInfo.mHideTooltipRunnable = new Runnable() {
                /* class android.view.$$Lambda$QI1s392qW8l6mC24bcy9050SkuY */

                @Override // java.lang.Runnable
                public final void run() {
                    View.this.hideTooltip();
                }
            };
            tooltipInfo.mHoverSlop = ViewConfiguration.get(this.mContext).getScaledHoverSlop();
            this.mTooltipInfo.clearAnchorPos();
        }
        this.mTooltipInfo.mTooltipText = tooltipText;
    }

    @UnsupportedAppUsage
    public void setTooltip(CharSequence tooltipText) {
        setTooltipText(tooltipText);
    }

    public CharSequence getTooltipText() {
        TooltipInfo tooltipInfo = this.mTooltipInfo;
        if (tooltipInfo != null) {
            return tooltipInfo.mTooltipText;
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
        TooltipInfo tooltipInfo = this.mTooltipInfo;
        tooltipInfo.mTooltipFromLongClick = fromLongClick;
        tooltipInfo.mTooltipPopup = new TooltipPopup(getContext());
        this.mTooltipInfo.mTooltipPopup.show(this, x, y, (this.mPrivateFlags3 & 131072) == 131072, this.mTooltipInfo.mTooltipText);
        this.mAttachInfo.mTooltipHost = this;
        notifyViewAccessibilityStateChangedIfNeeded(0);
        return true;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void hideTooltip() {
        TooltipInfo tooltipInfo = this.mTooltipInfo;
        if (tooltipInfo != null) {
            removeCallbacks(tooltipInfo.mShowTooltipRunnable);
            if (this.mTooltipInfo.mTooltipPopup != null) {
                this.mTooltipInfo.mTooltipPopup.hide();
                TooltipInfo tooltipInfo2 = this.mTooltipInfo;
                tooltipInfo2.mTooltipPopup = null;
                tooltipInfo2.mTooltipFromLongClick = false;
                tooltipInfo2.clearAnchorPos();
                AttachInfo attachInfo = this.mAttachInfo;
                if (attachInfo != null) {
                    attachInfo.mTooltipHost = null;
                }
                notifyViewAccessibilityStateChangedIfNeeded(0);
            }
        }
    }

    private boolean showLongClickTooltip(int x, int y) {
        TooltipInfo tooltipInfo = this.mTooltipInfo;
        if (tooltipInfo != null) {
            removeCallbacks(tooltipInfo.mShowTooltipRunnable);
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
        int action = event.getAction();
        if (action != 0) {
            if (action == 1) {
                handleTooltipUp();
            }
        } else if (event.getRepeatCount() == 0) {
            hideTooltip();
        }
    }

    private void handleTooltipUp() {
        TooltipInfo tooltipInfo = this.mTooltipInfo;
        if (tooltipInfo != null && tooltipInfo.mTooltipPopup != null) {
            removeCallbacks(this.mTooltipInfo.mHideTooltipRunnable);
            postDelayed(this.mTooltipInfo.mHideTooltipRunnable, (long) ViewConfiguration.getLongPressTooltipHideTimeout());
        }
    }

    private int getFocusableAttribute(TypedArray attributes) {
        TypedValue val = new TypedValue();
        if (!attributes.getValue(19, val)) {
            return 16;
        }
        if (val.type == 18) {
            return val.data == 0 ? 0 : 1;
        }
        return val.data;
    }

    public View getTooltipView() {
        TooltipInfo tooltipInfo = this.mTooltipInfo;
        if (tooltipInfo == null || tooltipInfo.mTooltipPopup == null) {
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
        ListenerInfo listenerInfo = this.mListenerInfo;
        if (listenerInfo == null || listenerInfo.mUnhandledKeyListeners == null) {
            return false;
        }
        for (int i = this.mListenerInfo.mUnhandledKeyListeners.size() - 1; i >= 0; i--) {
            if (((OnUnhandledKeyEventListener) this.mListenerInfo.mUnhandledKeyListeners.get(i)).onUnhandledKeyEvent(this, event)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasUnhandledKeyListener() {
        ListenerInfo listenerInfo = this.mListenerInfo;
        return (listenerInfo == null || listenerInfo.mUnhandledKeyListeners == null || this.mListenerInfo.mUnhandledKeyListeners.isEmpty()) ? false : true;
    }

    public void addOnUnhandledKeyEventListener(OnUnhandledKeyEventListener listener) {
        ArrayList<OnUnhandledKeyEventListener> listeners = getListenerInfo().mUnhandledKeyListeners;
        if (listeners == null) {
            listeners = new ArrayList<>();
            getListenerInfo().mUnhandledKeyListeners = listeners;
        }
        listeners.add(listener);
        if (listeners.size() == 1) {
            ViewParent viewParent = this.mParent;
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).incrementChildUnhandledKeyListeners();
            }
        }
    }

    public void removeOnUnhandledKeyEventListener(OnUnhandledKeyEventListener listener) {
        ListenerInfo listenerInfo = this.mListenerInfo;
        if (listenerInfo != null && listenerInfo.mUnhandledKeyListeners != null && !this.mListenerInfo.mUnhandledKeyListeners.isEmpty()) {
            this.mListenerInfo.mUnhandledKeyListeners.remove(listener);
            if (this.mListenerInfo.mUnhandledKeyListeners.isEmpty()) {
                this.mListenerInfo.mUnhandledKeyListeners = null;
                ViewParent viewParent = this.mParent;
                if (viewParent instanceof ViewGroup) {
                    ((ViewGroup) viewParent).decrementChildUnhandledKeyListeners();
                }
            }
        }
    }

    public void setTouchInOtherThread(boolean touchable) {
        this.mCanTouchInOtherThread = touchable;
    }

    public boolean isTouchableInOtherThread() {
        return this.mCanTouchInOtherThread;
    }

    @UnsupportedAppUsage
    public void setBlurEnabled(boolean enabled) {
        this.mBlurEnabled = enabled;
    }

    @UnsupportedAppUsage
    public boolean isBlurEnabled() {
        return this.mBlurEnabled;
    }

    @UnsupportedAppUsage
    public void setBlurCornerRadius(int roundXdp, int roundYdp) {
        this.mRoundX = dipsToPixels(roundXdp);
        this.mRoundY = dipsToPixels(roundYdp);
    }

    @UnsupportedAppUsage
    public int getBlurCornerRoundX() {
        int i = this.mRoundX;
        if (i > 0) {
            return i;
        }
        if (this.mBackground != null) {
            AttachInfo attachInfo = this.mAttachInfo;
            Outline outline = attachInfo != null ? attachInfo.mTmpOutline : new Outline();
            this.mBackground.getOutline(outline);
            int radius = (int) outline.getRadius();
            if (radius > 0) {
                return radius;
            }
        }
        return this.mRoundX;
    }

    @UnsupportedAppUsage
    public int getBlurCornerRoundY() {
        int i = this.mRoundY;
        if (i > 0) {
            return i;
        }
        if (this.mBackground != null) {
            AttachInfo attachInfo = this.mAttachInfo;
            Outline outline = attachInfo != null ? attachInfo.mTmpOutline : new Outline();
            this.mBackground.getOutline(outline);
            int radius = (int) outline.getRadius();
            if (radius > 0) {
                return radius;
            }
        }
        return this.mRoundY;
    }

    /* access modifiers changed from: protected */
    public Drawable initializeVariousScrollIndicators(Context context) {
        return context.getDrawable(R.drawable.scroll_indicator_material);
    }

    /* access modifiers changed from: protected */
    public boolean reSizeScrollIndicators(int[] xy) {
        return false;
    }

    public boolean isLongPressSwipe() {
        return false;
    }

    private void handForceDarkConfigrationChange(Canvas canvas) {
        boolean newState;
        AttachInfo attachInfo;
        ViewRootImpl rootImpl = getViewRootImpl();
        if (rootImpl != null && this.isHwForceDarkState != (newState = HwFrameworkFactory.getHwForceDarkManager().setAllowedHwForceDark(getContext(), canvas, rootImpl.hwForceDarkState, this.isAndroidForceDarkState, rootImpl.mWindowAttributes))) {
            this.isHwForceDarkState = newState;
            if (!(this.mParent == null || (attachInfo = this.mAttachInfo) == null || attachInfo.mRecomputeGlobalAttributes)) {
                this.mParent.recomputeViewAttributes(this);
            }
            invalidate();
        }
    }

    private boolean isGestureNavView() {
        ViewGroup.LayoutParams layoutParams = this.mLayoutParams;
        if (!(layoutParams instanceof WindowManager.LayoutParams)) {
            return false;
        }
        CharSequence viewTitle = ((WindowManager.LayoutParams) layoutParams).getTitle();
        for (String gestureNavViewTitle : GESTURE_NAV_VIEW_TITLES) {
            if (gestureNavViewTitle.equals(viewTitle)) {
                return true;
            }
        }
        return false;
    }

    public boolean dispatchStatusBarTop() {
        return false;
    }
}
