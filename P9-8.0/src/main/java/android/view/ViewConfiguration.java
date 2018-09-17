package android.view;

import android.app.AppGlobals;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.rms.AppAssociate;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import com.android.internal.R;

public class ViewConfiguration {
    private static final int A11Y_SHORTCUT_KEY_TIMEOUT = 3000;
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
    private static final int SCROLL_BAR_SIZE = 10;
    private static final float SCROLL_FRICTION = 0.015f;
    private static final long SEND_RECURRING_ACCESSIBILITY_EVENTS_INTERVAL_MILLIS = 100;
    private static final int TAP_TIMEOUT = 100;
    private static final int TOUCH_SLOP = 8;
    private static final float VERTICAL_SCROLL_FACTOR = 64.0f;
    private static final int WINDOW_TOUCH_SLOP = 16;
    private static final int ZOOM_CONTROLS_TIMEOUT = 3000;
    static final SparseArray<ViewConfiguration> sConfigurations = new SparseArray(2);
    private final int mDoubleTapSlop;
    private final int mDoubleTapTouchSlop;
    private final int mEdgeSlop;
    private final int mFadingEdgeLength;
    private final boolean mFadingMarqueeEnabled;
    private final long mGlobalActionsKeyTimeout;
    private final float mHorizontalScrollFactor;
    private final int mMaximumDrawingCacheSize;
    private final int mMaximumFlingVelocity;
    private final int mMinScrollbarTouchTarget;
    private final int mMinimumFlingVelocity;
    private final int mOverflingDistance;
    private final int mOverscrollDistance;
    private final int mPagingTouchSlop;
    private final int mScrollbarSize;
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
        this.mScrollbarSize = 10;
        this.mTouchSlop = 8;
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
        Display display = ((WindowManager) context.getSystemService(AppAssociate.ASSOC_WINDOW)).getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        this.mMaximumDrawingCacheSize = (size.x * 4) * size.y;
        this.mOverscrollDistance = (int) ((0.0f * sizeAndDensity) + 0.5f);
        this.mOverflingDistance = (int) ((6.0f * sizeAndDensity) + 0.5f);
        if (!this.sHasPermanentMenuKeySet) {
            switch (res.getInteger(R.integer.config_overrideHasPermanentMenuKey)) {
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
                        this.sHasPermanentMenuKey = WindowManagerGlobal.getWindowManagerService().hasNavigationBar() ^ 1;
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
        this.mMinScrollbarTouchTarget = res.getDimensionPixelSize(R.dimen.config_minScrollbarTouchTarget);
        this.mPagingTouchSlop = this.mTouchSlop * 2;
        this.mDoubleTapTouchSlop = this.mTouchSlop;
        this.mMinimumFlingVelocity = res.getDimensionPixelSize(R.dimen.config_viewMinFlingVelocity);
        this.mMaximumFlingVelocity = res.getDimensionPixelSize(R.dimen.config_viewMaxFlingVelocity);
        this.mGlobalActionsKeyTimeout = (long) res.getInteger(R.integer.config_globalActionsKeyTimeout);
        this.mHorizontalScrollFactor = (float) res.getDimensionPixelSize(R.dimen.config_horizontalScrollFactor);
        this.mVerticalScrollFactor = (float) res.getDimensionPixelSize(R.dimen.config_verticalScrollFactor);
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
        return 10;
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
        return AppGlobals.getIntCoreSetting(Secure.LONG_PRESS_TIMEOUT, 500);
    }

    public static int getMultiPressTimeout() {
        return AppGlobals.getIntCoreSetting(Secure.MULTI_PRESS_TIMEOUT, 300);
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
        int maxFlingVelocity = Integer.parseInt(SystemProperties.get("sys.aps.maxFlingVelocity", "-1"));
        return -1 == maxFlingVelocity ? this.mMaximumFlingVelocity : maxFlingVelocity;
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
