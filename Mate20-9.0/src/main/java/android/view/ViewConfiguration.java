package android.view;

import android.app.AppGlobals;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.rms.AppAssociate;
import android.util.DisplayMetrics;
import android.util.SparseArray;

public class ViewConfiguration {
    private static final int A11Y_SHORTCUT_KEY_TIMEOUT = 3000;
    private static final int A11Y_SHORTCUT_KEY_TIMEOUT_AFTER_CONFIRMATION = 1000;
    private static final long ACTION_MODE_HIDE_DURATION_DEFAULT = 2000;
    private static final int DEFAULT_LONG_PRESS_TIMEOUT = 500;
    private static final int DEFAULT_MULTI_PRESS_TIMEOUT = 300;
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
    private static final float HORIZONTAL_SCROLL_FACTOR = 64.0f;
    private static final int HOVER_TAP_SLOP = 20;
    private static final int HOVER_TAP_TIMEOUT = 150;
    private static final int HOVER_TOOLTIP_HIDE_SHORT_TIMEOUT = 3000;
    private static final int HOVER_TOOLTIP_HIDE_TIMEOUT = 15000;
    private static final int HOVER_TOOLTIP_SHOW_TIMEOUT = 500;
    private static final int JUMP_TAP_TIMEOUT = 500;
    private static final int KEY_REPEAT_DELAY = 50;
    private static final int LONG_PRESS_TOOLTIP_HIDE_TIMEOUT = 1500;
    @Deprecated
    private static final int MAXIMUM_DRAWING_CACHE_SIZE = 1536000;
    private static final int MAXIMUM_FLING_VELOCITY = 8000;
    private static final int MINIMUM_FLING_VELOCITY = 50;
    private static final int MIN_SCROLLBAR_TOUCH_TARGET = 48;
    private static final int OVERFLING_DISTANCE = 6;
    private static final int OVERSCROLL_DISTANCE = 0;
    private static final int PAGING_TOUCH_SLOP = 16;
    private static final int PRESSED_STATE_DURATION = 64;
    private static final int SCROLL_BAR_DEFAULT_DELAY = 300;
    private static final int SCROLL_BAR_FADE_DURATION = 250;
    private static final int SCROLL_BAR_SIZE = 4;
    private static final float SCROLL_FRICTION = 0.015f;
    private static final long SEND_RECURRING_ACCESSIBILITY_EVENTS_INTERVAL_MILLIS = 100;
    private static final int TAP_TIMEOUT = 100;
    private static final int TOUCH_SLOP = 8;
    private static final float VERTICAL_SCROLL_FACTOR = 64.0f;
    private static final int WINDOW_TOUCH_SLOP = 16;
    private static final int ZOOM_CONTROLS_TIMEOUT = 3000;
    static final SparseArray<ViewConfiguration> sConfigurations = new SparseArray<>(2);
    private final int mDoubleTapSlop;
    private final int mDoubleTapTouchSlop;
    private final int mEdgeSlop;
    private final int mFadingEdgeLength;
    private final boolean mFadingMarqueeEnabled;
    private final long mGlobalActionsKeyTimeout;
    private final float mHorizontalScrollFactor;
    private final int mHoverSlop;
    private final int mMaximumDrawingCacheSize;
    private final int mMaximumFlingVelocity;
    private final int mMinScrollbarTouchTarget;
    private final int mMinimumFlingVelocity;
    private final int mOverflingDistance;
    private final int mOverscrollDistance;
    private final int mPagingTouchSlop;
    private final int mScrollbarSize;
    private final boolean mShowMenuShortcutsWhenKeyboardPresent;
    private final int mTouchSlop;
    private final float mVerticalScrollFactor;
    private final int mWindowTouchSlop;
    private boolean sHasPermanentMenuKey;
    private boolean sHasPermanentMenuKeySet;

    @Deprecated
    public ViewConfiguration() {
        this.mEdgeSlop = 12;
        this.mFadingEdgeLength = 12;
        this.mMinimumFlingVelocity = 50;
        this.mMaximumFlingVelocity = MAXIMUM_FLING_VELOCITY;
        this.mScrollbarSize = 4;
        this.mTouchSlop = 8;
        this.mHoverSlop = 4;
        this.mMinScrollbarTouchTarget = 48;
        this.mDoubleTapTouchSlop = 8;
        this.mPagingTouchSlop = 16;
        this.mDoubleTapSlop = 100;
        this.mWindowTouchSlop = 16;
        this.mMaximumDrawingCacheSize = MAXIMUM_DRAWING_CACHE_SIZE;
        this.mOverscrollDistance = 0;
        this.mOverflingDistance = 6;
        this.mFadingMarqueeEnabled = true;
        this.mGlobalActionsKeyTimeout = 500;
        this.mHorizontalScrollFactor = 64.0f;
        this.mVerticalScrollFactor = 64.0f;
        this.mShowMenuShortcutsWhenKeyboardPresent = false;
    }

    private ViewConfiguration(Context context) {
        float sizeAndDensity;
        Resources res = context.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();
        float density = metrics.density;
        if (config.isLayoutSizeAtLeast(4)) {
            sizeAndDensity = 1.5f * density;
        } else {
            sizeAndDensity = density;
        }
        this.mEdgeSlop = (int) ((sizeAndDensity * 12.0f) + 0.5f);
        this.mFadingEdgeLength = (int) ((12.0f * sizeAndDensity) + 0.5f);
        this.mScrollbarSize = res.getDimensionPixelSize(17104976);
        this.mDoubleTapSlop = (int) ((100.0f * sizeAndDensity) + 0.5f);
        this.mWindowTouchSlop = (int) ((16.0f * sizeAndDensity) + 0.5f);
        Display display = ((WindowManager) context.getSystemService(AppAssociate.ASSOC_WINDOW)).getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        this.mMaximumDrawingCacheSize = 4 * size.x * size.y;
        this.mOverscrollDistance = (int) ((0.0f * sizeAndDensity) + 0.5f);
        this.mOverflingDistance = (int) ((6.0f * sizeAndDensity) + 0.5f);
        if (!this.sHasPermanentMenuKeySet) {
            switch (res.getInteger(17694848)) {
                case 1:
                    this.sHasPermanentMenuKey = true;
                    this.sHasPermanentMenuKeySet = true;
                    break;
                case 2:
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
        this.mFadingMarqueeEnabled = res.getBoolean(17957054);
        this.mTouchSlop = res.getDimensionPixelSize(17104979);
        this.mHoverSlop = res.getDimensionPixelSize(17104978);
        this.mMinScrollbarTouchTarget = res.getDimensionPixelSize(17104966);
        this.mPagingTouchSlop = this.mTouchSlop * 2;
        this.mDoubleTapTouchSlop = this.mTouchSlop;
        this.mMinimumFlingVelocity = res.getDimensionPixelSize(17104981);
        this.mMaximumFlingVelocity = res.getDimensionPixelSize(17104980);
        this.mGlobalActionsKeyTimeout = (long) res.getInteger(17694790);
        this.mHorizontalScrollFactor = (float) res.getDimensionPixelSize(17104961);
        this.mVerticalScrollFactor = (float) res.getDimensionPixelSize(17104977);
        this.mShowMenuShortcutsWhenKeyboardPresent = res.getBoolean(17957018);
    }

    public static ViewConfiguration get(Context context) {
        int density = (int) (100.0f * context.getResources().getDisplayMetrics().density);
        ViewConfiguration configuration = sConfigurations.get(density);
        if (configuration != null) {
            return configuration;
        }
        ViewConfiguration configuration2 = new ViewConfiguration(context);
        sConfigurations.put(density, configuration2);
        return configuration2;
    }

    @Deprecated
    public static int getScrollBarSize() {
        return 4;
    }

    public int getScaledScrollBarSize() {
        return this.mScrollbarSize;
    }

    public int getScaledMinScrollbarTouchTarget() {
        return this.mMinScrollbarTouchTarget;
    }

    public static int getScrollBarFadeDuration() {
        return 250;
    }

    public static int getScrollDefaultDelay() {
        return 300;
    }

    @Deprecated
    public static int getFadingEdgeLength() {
        return 12;
    }

    public int getScaledFadingEdgeLength() {
        return this.mFadingEdgeLength;
    }

    public static int getPressedStateDuration() {
        return 64;
    }

    public static int getLongPressTimeout() {
        return AppGlobals.getIntCoreSetting(Settings.Secure.LONG_PRESS_TIMEOUT, 500);
    }

    public static int getMultiPressTimeout() {
        return AppGlobals.getIntCoreSetting(Settings.Secure.MULTI_PRESS_TIMEOUT, 300);
    }

    public static int getKeyRepeatTimeout() {
        return getLongPressTimeout();
    }

    public static int getKeyRepeatDelay() {
        return 50;
    }

    public static int getTapTimeout() {
        return 100;
    }

    public static int getJumpTapTimeout() {
        return 500;
    }

    public static int getDoubleTapTimeout() {
        return 300;
    }

    public static int getDoubleTapMinTime() {
        return 40;
    }

    public static int getHoverTapTimeout() {
        return 150;
    }

    public static int getHoverTapSlop() {
        return 20;
    }

    @Deprecated
    public static int getEdgeSlop() {
        return 12;
    }

    public int getScaledEdgeSlop() {
        return this.mEdgeSlop;
    }

    @Deprecated
    public static int getTouchSlop() {
        return 8;
    }

    public int getScaledTouchSlop() {
        return this.mTouchSlop;
    }

    public int getScaledHoverSlop() {
        return this.mHoverSlop;
    }

    public int getScaledDoubleTapTouchSlop() {
        return this.mDoubleTapTouchSlop;
    }

    public int getScaledPagingTouchSlop() {
        return this.mPagingTouchSlop;
    }

    @Deprecated
    public static int getDoubleTapSlop() {
        return 100;
    }

    public int getScaledDoubleTapSlop() {
        return this.mDoubleTapSlop;
    }

    public static long getSendRecurringAccessibilityEventsInterval() {
        return SEND_RECURRING_ACCESSIBILITY_EVENTS_INTERVAL_MILLIS;
    }

    @Deprecated
    public static int getWindowTouchSlop() {
        return 16;
    }

    public int getScaledWindowTouchSlop() {
        return this.mWindowTouchSlop;
    }

    @Deprecated
    public static int getMinimumFlingVelocity() {
        return 50;
    }

    public int getScaledMinimumFlingVelocity() {
        return this.mMinimumFlingVelocity;
    }

    @Deprecated
    public static int getMaximumFlingVelocity() {
        return MAXIMUM_FLING_VELOCITY;
    }

    public int getScaledMaximumFlingVelocity() {
        int resultFlingVelocity = this.mMaximumFlingVelocity;
        int maxFlingVelocity = Integer.parseInt(SystemProperties.get("sys.aps.maxFlingVelocity", "-1"));
        if (maxFlingVelocity <= 0 || maxFlingVelocity > 100) {
            return resultFlingVelocity;
        }
        return (this.mMaximumFlingVelocity * maxFlingVelocity) / 100;
    }

    public int getScaledScrollFactor() {
        return (int) this.mVerticalScrollFactor;
    }

    public float getScaledHorizontalScrollFactor() {
        return this.mHorizontalScrollFactor;
    }

    public float getScaledVerticalScrollFactor() {
        return this.mVerticalScrollFactor;
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

    public long getAccessibilityShortcutKeyTimeout() {
        return 3000;
    }

    public long getAccessibilityShortcutKeyTimeoutAfterConfirmation() {
        return 1000;
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

    public boolean shouldShowMenuShortcutsWhenKeyboardPresent() {
        return this.mShowMenuShortcutsWhenKeyboardPresent;
    }

    public boolean isFadingMarqueeEnabled() {
        return this.mFadingMarqueeEnabled;
    }

    public static int getLongPressTooltipHideTimeout() {
        return 1500;
    }

    public static int getHoverTooltipShowTimeout() {
        return 500;
    }

    public static int getHoverTooltipHideTimeout() {
        return HOVER_TOOLTIP_HIDE_TIMEOUT;
    }

    public static int getHoverTooltipHideShortTimeout() {
        return 3000;
    }
}
