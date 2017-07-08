package android.view;

import android.app.AppGlobals;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import com.android.internal.R;

public class ViewConfiguration {
    private static final long ACTION_MODE_HIDE_DURATION_DEFAULT = 2000;
    private static final int DEFAULT_LONG_PRESS_TIMEOUT = 500;
    private static final int DOUBLE_TAP_MIN_TIME = 40;
    private static final int DOUBLE_TAP_SLOP = 100;
    private static final int DOUBLE_TAP_TIMEOUT = 300;
    private static final int DOUBLE_TAP_TOUCH_SLOP = 8;
    private static final int EDGE_SLOP = 12;
    private static final int FADING_EDGE_LENGTH = 12;
    private static final int GLOBAL_ACTIONS_KEY_TIMEOUT = 500;
    private static final int HAS_PERMANENT_MENU_KEY_AUTODETECT = 0;
    private static final int HAS_PERMANENT_MENU_KEY_FALSE = 2;
    private static final int HAS_PERMANENT_MENU_KEY_TRUE = 1;
    private static final int HOVER_TAP_SLOP = 20;
    private static final int HOVER_TAP_TIMEOUT = 150;
    private static final int JUMP_TAP_TIMEOUT = 500;
    private static final int KEY_REPEAT_DELAY = 50;
    @Deprecated
    private static final int MAXIMUM_DRAWING_CACHE_SIZE = 1536000;
    private static final int MAXIMUM_FLING_VELOCITY = 8000;
    private static final int MINIMUM_FLING_VELOCITY = 50;
    private static final int OVERFLING_DISTANCE = 6;
    private static final int OVERSCROLL_DISTANCE = 0;
    private static final int PAGING_TOUCH_SLOP = 16;
    private static final int PRESSED_STATE_DURATION = 64;
    private static final int SCROLL_BAR_DEFAULT_DELAY = 300;
    private static final int SCROLL_BAR_FADE_DURATION = 250;
    private static final int SCROLL_BAR_SIZE = 10;
    private static final float SCROLL_FRICTION = 0.015f;
    private static final long SEND_RECURRING_ACCESSIBILITY_EVENTS_INTERVAL_MILLIS = 100;
    private static final int TAP_TIMEOUT = 100;
    private static final int TOUCH_SLOP = 8;
    private static final int WINDOW_TOUCH_SLOP = 16;
    private static final int ZOOM_CONTROLS_TIMEOUT = 3000;
    static final SparseArray<ViewConfiguration> sConfigurations = null;
    private final int mDoubleTapSlop;
    private final int mDoubleTapTouchSlop;
    private final int mEdgeSlop;
    private final int mFadingEdgeLength;
    private final boolean mFadingMarqueeEnabled;
    private final long mGlobalActionsKeyTimeout;
    private final int mMaximumDrawingCacheSize;
    private final int mMaximumFlingVelocity;
    private final int mMinimumFlingVelocity;
    private final int mOverflingDistance;
    private final int mOverscrollDistance;
    private final int mPagingTouchSlop;
    private final int mScrollbarSize;
    private final int mTouchSlop;
    private final int mWindowTouchSlop;
    private boolean sHasPermanentMenuKey;
    private boolean sHasPermanentMenuKeySet;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.ViewConfiguration.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.ViewConfiguration.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.ViewConfiguration.<clinit>():void");
    }

    @Deprecated
    public ViewConfiguration() {
        this.mEdgeSlop = FADING_EDGE_LENGTH;
        this.mFadingEdgeLength = FADING_EDGE_LENGTH;
        this.mMinimumFlingVelocity = MINIMUM_FLING_VELOCITY;
        this.mMaximumFlingVelocity = MAXIMUM_FLING_VELOCITY;
        this.mScrollbarSize = SCROLL_BAR_SIZE;
        this.mTouchSlop = TOUCH_SLOP;
        this.mDoubleTapTouchSlop = TOUCH_SLOP;
        this.mPagingTouchSlop = WINDOW_TOUCH_SLOP;
        this.mDoubleTapSlop = TAP_TIMEOUT;
        this.mWindowTouchSlop = WINDOW_TOUCH_SLOP;
        this.mMaximumDrawingCacheSize = MAXIMUM_DRAWING_CACHE_SIZE;
        this.mOverscrollDistance = OVERSCROLL_DISTANCE;
        this.mOverflingDistance = OVERFLING_DISTANCE;
        this.mFadingMarqueeEnabled = true;
        this.mGlobalActionsKeyTimeout = 500;
    }

    private ViewConfiguration(Context context) {
        float sizeAndDensity;
        Resources res = context.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();
        float density = metrics.density;
        if (config.isLayoutSizeAtLeast(4)) {
            sizeAndDensity = density * 1.5f;
        } else {
            sizeAndDensity = density;
        }
        this.mEdgeSlop = (int) ((12.0f * sizeAndDensity) + 0.5f);
        this.mFadingEdgeLength = (int) ((12.0f * sizeAndDensity) + 0.5f);
        this.mScrollbarSize = (int) ((10.0f * density) + 0.5f);
        this.mDoubleTapSlop = (int) ((100.0f * sizeAndDensity) + 0.5f);
        this.mWindowTouchSlop = (int) ((16.0f * sizeAndDensity) + 0.5f);
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        this.mMaximumDrawingCacheSize = (size.x * 4) * size.y;
        this.mOverscrollDistance = (int) ((0.0f * sizeAndDensity) + 0.5f);
        this.mOverflingDistance = (int) ((6.0f * sizeAndDensity) + 0.5f);
        if (!this.sHasPermanentMenuKeySet) {
            switch (res.getInteger(R.integer.config_overrideHasPermanentMenuKey)) {
                case HAS_PERMANENT_MENU_KEY_TRUE /*1*/:
                    this.sHasPermanentMenuKey = true;
                    this.sHasPermanentMenuKeySet = true;
                    break;
                case HAS_PERMANENT_MENU_KEY_FALSE /*2*/:
                    this.sHasPermanentMenuKey = false;
                    this.sHasPermanentMenuKeySet = true;
                    break;
                default:
                    try {
                        this.sHasPermanentMenuKey = !WindowManagerGlobal.getWindowManagerService().hasNavigationBar();
                        this.sHasPermanentMenuKeySet = true;
                        break;
                    } catch (RemoteException e) {
                        this.sHasPermanentMenuKey = false;
                        break;
                    }
            }
        }
        this.mFadingMarqueeEnabled = res.getBoolean(R.bool.config_ui_enableFadingMarquee);
        this.mTouchSlop = res.getDimensionPixelSize(R.dimen.config_viewConfigurationTouchSlop);
        this.mPagingTouchSlop = this.mTouchSlop * HAS_PERMANENT_MENU_KEY_FALSE;
        this.mDoubleTapTouchSlop = this.mTouchSlop;
        this.mMinimumFlingVelocity = res.getDimensionPixelSize(R.dimen.config_viewMinFlingVelocity);
        this.mMaximumFlingVelocity = res.getDimensionPixelSize(R.dimen.config_viewMaxFlingVelocity);
        this.mGlobalActionsKeyTimeout = (long) res.getInteger(R.integer.config_globalActionsKeyTimeout);
    }

    public static ViewConfiguration get(Context context) {
        int density = (int) (context.getResources().getDisplayMetrics().density * 100.0f);
        ViewConfiguration configuration = (ViewConfiguration) sConfigurations.get(density);
        if (configuration != null) {
            return configuration;
        }
        configuration = new ViewConfiguration(context);
        sConfigurations.put(density, configuration);
        return configuration;
    }

    @Deprecated
    public static int getScrollBarSize() {
        return SCROLL_BAR_SIZE;
    }

    public int getScaledScrollBarSize() {
        return this.mScrollbarSize;
    }

    public static int getScrollBarFadeDuration() {
        return SCROLL_BAR_FADE_DURATION;
    }

    public static int getScrollDefaultDelay() {
        return SCROLL_BAR_DEFAULT_DELAY;
    }

    @Deprecated
    public static int getFadingEdgeLength() {
        return FADING_EDGE_LENGTH;
    }

    public int getScaledFadingEdgeLength() {
        return this.mFadingEdgeLength;
    }

    public static int getPressedStateDuration() {
        return PRESSED_STATE_DURATION;
    }

    public static int getLongPressTimeout() {
        return AppGlobals.getIntCoreSetting("long_press_timeout", JUMP_TAP_TIMEOUT);
    }

    public static int getKeyRepeatTimeout() {
        return getLongPressTimeout();
    }

    public static int getKeyRepeatDelay() {
        return MINIMUM_FLING_VELOCITY;
    }

    public static int getTapTimeout() {
        return TAP_TIMEOUT;
    }

    public static int getJumpTapTimeout() {
        return JUMP_TAP_TIMEOUT;
    }

    public static int getDoubleTapTimeout() {
        return SCROLL_BAR_DEFAULT_DELAY;
    }

    public static int getDoubleTapMinTime() {
        return DOUBLE_TAP_MIN_TIME;
    }

    public static int getHoverTapTimeout() {
        return HOVER_TAP_TIMEOUT;
    }

    public static int getHoverTapSlop() {
        return HOVER_TAP_SLOP;
    }

    @Deprecated
    public static int getEdgeSlop() {
        return FADING_EDGE_LENGTH;
    }

    public int getScaledEdgeSlop() {
        return this.mEdgeSlop;
    }

    @Deprecated
    public static int getTouchSlop() {
        return TOUCH_SLOP;
    }

    public int getScaledTouchSlop() {
        return this.mTouchSlop;
    }

    public int getScaledDoubleTapTouchSlop() {
        return this.mDoubleTapTouchSlop;
    }

    public int getScaledPagingTouchSlop() {
        return this.mPagingTouchSlop;
    }

    @Deprecated
    public static int getDoubleTapSlop() {
        return TAP_TIMEOUT;
    }

    public int getScaledDoubleTapSlop() {
        return this.mDoubleTapSlop;
    }

    public static long getSendRecurringAccessibilityEventsInterval() {
        return SEND_RECURRING_ACCESSIBILITY_EVENTS_INTERVAL_MILLIS;
    }

    @Deprecated
    public static int getWindowTouchSlop() {
        return WINDOW_TOUCH_SLOP;
    }

    public int getScaledWindowTouchSlop() {
        return this.mWindowTouchSlop;
    }

    @Deprecated
    public static int getMinimumFlingVelocity() {
        return MINIMUM_FLING_VELOCITY;
    }

    public int getScaledMinimumFlingVelocity() {
        return this.mMinimumFlingVelocity;
    }

    @Deprecated
    public static int getMaximumFlingVelocity() {
        return MAXIMUM_FLING_VELOCITY;
    }

    public int getScaledMaximumFlingVelocity() {
        int maxFlingVelocity = Integer.parseInt(SystemProperties.get("sys.aps.maxFlingVelocity", "-1"));
        if (-1 == maxFlingVelocity) {
            return this.mMaximumFlingVelocity;
        }
        return maxFlingVelocity;
    }

    @Deprecated
    public static int getMaximumDrawingCacheSize() {
        return MAXIMUM_DRAWING_CACHE_SIZE;
    }

    public int getScaledMaximumDrawingCacheSize() {
        return this.mMaximumDrawingCacheSize;
    }

    public int getScaledOverscrollDistance() {
        return this.mOverscrollDistance;
    }

    public int getScaledOverflingDistance() {
        return this.mOverflingDistance;
    }

    public static long getZoomControlsTimeout() {
        return 3000;
    }

    @Deprecated
    public static long getGlobalActionKeyTimeout() {
        return 500;
    }

    public long getDeviceGlobalActionKeyTimeout() {
        return this.mGlobalActionsKeyTimeout;
    }

    public static float getScrollFriction() {
        return SCROLL_FRICTION;
    }

    public static long getDefaultActionModeHideDuration() {
        return ACTION_MODE_HIDE_DURATION_DEFAULT;
    }

    public boolean hasPermanentMenuKey() {
        return this.sHasPermanentMenuKey;
    }

    public boolean isFadingMarqueeEnabled() {
        return this.mFadingMarqueeEnabled;
    }
}
