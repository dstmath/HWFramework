package android.view;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.MemoryLeakMonitorManager;
import android.os.RemoteException;
import android.provider.SettingsStringUtil;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.ActionMode;
import android.view.InputQueue;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.R;
import java.util.Collections;
import java.util.List;

public abstract class Window {
    public static final int DECOR_CAPTION_SHADE_AUTO = 0;
    public static final int DECOR_CAPTION_SHADE_DARK = 2;
    public static final int DECOR_CAPTION_SHADE_LIGHT = 1;
    @Deprecated
    protected static final int DEFAULT_FEATURES = 65;
    public static final int FEATURE_ACTION_BAR = 8;
    public static final int FEATURE_ACTION_BAR_OVERLAY = 9;
    public static final int FEATURE_ACTION_MODE_OVERLAY = 10;
    public static final int FEATURE_ACTIVITY_TRANSITIONS = 13;
    public static final int FEATURE_CONTENT_TRANSITIONS = 12;
    public static final int FEATURE_CONTEXT_MENU = 6;
    public static final int FEATURE_CUSTOM_TITLE = 7;
    @Deprecated
    public static final int FEATURE_INDETERMINATE_PROGRESS = 5;
    public static final int FEATURE_LEFT_ICON = 3;
    @UnsupportedAppUsage
    public static final int FEATURE_MAX = 13;
    public static final int FEATURE_NO_TITLE = 1;
    public static final int FEATURE_OPTIONS_PANEL = 0;
    @Deprecated
    public static final int FEATURE_PROGRESS = 2;
    public static final int FEATURE_RIGHT_ICON = 4;
    public static final int FEATURE_SWIPE_TO_DISMISS = 11;
    public static final int ID_ANDROID_CONTENT = 16908290;
    public static final String NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME = "android:navigation:background";
    @Deprecated
    public static final int PROGRESS_END = 10000;
    @Deprecated
    public static final int PROGRESS_INDETERMINATE_OFF = -4;
    @Deprecated
    public static final int PROGRESS_INDETERMINATE_ON = -3;
    @Deprecated
    public static final int PROGRESS_SECONDARY_END = 30000;
    @Deprecated
    public static final int PROGRESS_SECONDARY_START = 20000;
    @Deprecated
    public static final int PROGRESS_START = 0;
    @Deprecated
    public static final int PROGRESS_VISIBILITY_OFF = -2;
    @Deprecated
    public static final int PROGRESS_VISIBILITY_ON = -1;
    public static final String STATUS_BAR_BACKGROUND_TRANSITION_NAME = "android:status:background";
    private Window mActiveChild;
    @UnsupportedAppUsage
    private String mAppName;
    @UnsupportedAppUsage
    private IBinder mAppToken;
    @UnsupportedAppUsage
    private Callback mCallback;
    private boolean mCloseOnSwipeEnabled = false;
    private boolean mCloseOnTouchOutside = false;
    private Window mContainer;
    @UnsupportedAppUsage
    private final Context mContext;
    private int mDefaultWindowFormat = -1;
    @UnsupportedAppUsage
    private boolean mDestroyed;
    @UnsupportedAppUsage
    private int mFeatures;
    private int mForcedWindowFlags = 0;
    @UnsupportedAppUsage
    private boolean mHardwareAccelerated;
    private boolean mHasChildren = false;
    private boolean mHasSoftInputMode = false;
    private boolean mHaveDimAmount = false;
    private boolean mHaveWindowFormat = false;
    private boolean mIsActive = false;
    @UnsupportedAppUsage
    private int mLocalFeatures;
    private OnRestrictedCaptionAreaChangedListener mOnRestrictedCaptionAreaChangedListener;
    private OnWindowDismissedCallback mOnWindowDismissedCallback;
    private OnWindowSwipeDismissedCallback mOnWindowSwipeDismissedCallback;
    private boolean mOverlayWithDecorCaptionEnabled = false;
    private Rect mRestrictedCaptionAreaRect;
    private boolean mSetCloseOnTouchOutside = false;
    @UnsupportedAppUsage
    private final WindowManager.LayoutParams mWindowAttributes = new WindowManager.LayoutParams();
    private WindowControllerCallback mWindowControllerCallback;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private WindowManager mWindowManager;
    @UnsupportedAppUsage
    private TypedArray mWindowStyle;

    public interface OnFrameMetricsAvailableListener {
        void onFrameMetricsAvailable(Window window, FrameMetrics frameMetrics, int i);
    }

    public interface OnRestrictedCaptionAreaChangedListener {
        void onRestrictedCaptionAreaChanged(Rect rect);
    }

    public interface OnWindowDismissedCallback {
        void onWindowDismissed(boolean z, boolean z2);
    }

    public interface OnWindowSwipeDismissedCallback {
        void onWindowSwipeDismissed();
    }

    public interface WindowControllerCallback {
        void enterPictureInPictureModeIfPossible();

        boolean isTaskRoot();

        void toggleFreeformWindowingMode() throws RemoteException;
    }

    public abstract void addContentView(View view, ViewGroup.LayoutParams layoutParams);

    @UnsupportedAppUsage
    public abstract void alwaysReadCloseOnTouchAttr();

    public abstract void clearContentView();

    public abstract void closeAllPanels();

    public abstract void closePanel(int i);

    public abstract View getCurrentFocus();

    public abstract View getDecorView();

    public abstract WindowInsetsController getInsetsController();

    public abstract LayoutInflater getLayoutInflater();

    public abstract int getNavigationBarColor();

    public abstract int getStatusBarColor();

    public abstract int getVolumeControlStream();

    public abstract void invalidatePanelMenu(int i);

    public abstract boolean isFloating();

    public abstract boolean isShortcutKey(int i, KeyEvent keyEvent);

    /* access modifiers changed from: protected */
    public abstract void onActive();

    public abstract void onConfigurationChanged(Configuration configuration);

    public abstract void onMultiWindowModeChanged();

    public abstract void onPictureInPictureModeChanged(boolean z);

    public abstract void openPanel(int i, KeyEvent keyEvent);

    public abstract View peekDecorView();

    public abstract boolean performContextMenuIdentifierAction(int i, int i2);

    public abstract boolean performPanelIdentifierAction(int i, int i2, int i3);

    public abstract boolean performPanelShortcut(int i, int i2, KeyEvent keyEvent, int i3);

    public abstract void reportActivityRelaunched();

    public abstract void restoreHierarchyState(Bundle bundle);

    public abstract Bundle saveHierarchyState();

    public abstract void setBackgroundDrawable(Drawable drawable);

    public abstract void setChildDrawable(int i, Drawable drawable);

    public abstract void setChildInt(int i, int i2);

    public abstract void setContentView(int i);

    public abstract void setContentView(View view);

    public abstract void setContentView(View view, ViewGroup.LayoutParams layoutParams);

    public abstract void setDecorCaptionShade(int i);

    public abstract void setFeatureDrawable(int i, Drawable drawable);

    public abstract void setFeatureDrawableAlpha(int i, int i2);

    public abstract void setFeatureDrawableResource(int i, int i2);

    public abstract void setFeatureDrawableUri(int i, Uri uri);

    public abstract void setFeatureInt(int i, int i2);

    public abstract void setNavigationBarColor(int i);

    public abstract void setResizingCaptionDrawable(Drawable drawable);

    public abstract void setStatusBarColor(int i);

    public abstract void setTitle(CharSequence charSequence);

    @Deprecated
    public abstract void setTitleColor(int i);

    public abstract void setVolumeControlStream(int i);

    public abstract boolean superDispatchGenericMotionEvent(MotionEvent motionEvent);

    public abstract boolean superDispatchKeyEvent(KeyEvent keyEvent);

    public abstract boolean superDispatchKeyShortcutEvent(KeyEvent keyEvent);

    public abstract boolean superDispatchTouchEvent(MotionEvent motionEvent);

    public abstract boolean superDispatchTrackballEvent(MotionEvent motionEvent);

    public abstract void takeInputQueue(InputQueue.Callback callback);

    public abstract void takeKeyEvents(boolean z);

    public abstract void takeSurface(SurfaceHolder.Callback2 callback2);

    public abstract void togglePanel(int i, KeyEvent keyEvent);

    public interface Callback {
        boolean dispatchGenericMotionEvent(MotionEvent motionEvent);

        boolean dispatchKeyEvent(KeyEvent keyEvent);

        boolean dispatchKeyShortcutEvent(KeyEvent keyEvent);

        boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        boolean dispatchTouchEvent(MotionEvent motionEvent);

        boolean dispatchTrackballEvent(MotionEvent motionEvent);

        void onActionModeFinished(ActionMode actionMode);

        void onActionModeStarted(ActionMode actionMode);

        void onAttachedToWindow();

        void onContentChanged();

        boolean onCreatePanelMenu(int i, Menu menu);

        View onCreatePanelView(int i);

        void onDetachedFromWindow();

        boolean onMenuItemSelected(int i, MenuItem menuItem);

        boolean onMenuOpened(int i, Menu menu);

        void onPanelClosed(int i, Menu menu);

        boolean onPreparePanel(int i, View view, Menu menu);

        boolean onSearchRequested();

        boolean onSearchRequested(SearchEvent searchEvent);

        void onWindowAttributesChanged(WindowManager.LayoutParams layoutParams);

        void onWindowFocusChanged(boolean z);

        ActionMode onWindowStartingActionMode(ActionMode.Callback callback);

        ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int i);

        default void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> list, Menu menu, int deviceId) {
        }

        default void onPointerCaptureChanged(boolean hasCapture) {
        }
    }

    public Window(Context context) {
        this.mContext = context;
        int defaultFeatures = getDefaultFeatures(context);
        this.mLocalFeatures = defaultFeatures;
        this.mFeatures = defaultFeatures;
    }

    public final Context getContext() {
        return this.mContext;
    }

    public final TypedArray getWindowStyle() {
        TypedArray typedArray;
        synchronized (this) {
            if (this.mWindowStyle == null) {
                this.mWindowStyle = this.mContext.obtainStyledAttributes(R.styleable.Window);
            }
            typedArray = this.mWindowStyle;
        }
        return typedArray;
    }

    public void setContainer(Window container) {
        this.mContainer = container;
        if (container != null) {
            this.mFeatures |= 2;
            this.mLocalFeatures |= 2;
            container.mHasChildren = true;
        }
    }

    public final Window getContainer() {
        return this.mContainer;
    }

    public final boolean hasChildren() {
        return this.mHasChildren;
    }

    public final void destroy() {
        this.mDestroyed = true;
        MemoryLeakMonitorManager.watchMemoryLeak(this);
    }

    @UnsupportedAppUsage
    public final boolean isDestroyed() {
        return this.mDestroyed;
    }

    public void setWindowManager(WindowManager wm, IBinder appToken, String appName) {
        setWindowManager(wm, appToken, appName, false);
    }

    public void setWindowManager(WindowManager wm, IBinder appToken, String appName, boolean hardwareAccelerated) {
        this.mAppToken = appToken;
        this.mAppName = appName;
        this.mHardwareAccelerated = hardwareAccelerated;
        if (wm == null) {
            wm = (WindowManager) this.mContext.getSystemService("window");
        }
        this.mWindowManager = ((WindowManagerImpl) wm).createLocalWindowManager(this);
    }

    /* access modifiers changed from: package-private */
    public void adjustLayoutParamsForSubWindow(WindowManager.LayoutParams wp) {
        String str;
        View decor;
        CharSequence curTitle = wp.getTitle();
        if (wp.type >= 1000 && wp.type <= 1999) {
            if (wp.token == null && (decor = peekDecorView()) != null) {
                wp.token = decor.getWindowToken();
            }
            if (curTitle == null || curTitle.length() == 0) {
                StringBuilder title = new StringBuilder(32);
                if (wp.type == 1001) {
                    title.append("Media");
                } else if (wp.type == 1004) {
                    title.append("MediaOvr");
                } else if (wp.type == 1000) {
                    title.append("Panel");
                } else if (wp.type == 1002) {
                    title.append("SubPanel");
                } else if (wp.type == 1005) {
                    title.append("AboveSubPanel");
                } else if (wp.type == 1003) {
                    title.append("AtchDlg");
                } else {
                    title.append(wp.type);
                }
                if (this.mAppName != null) {
                    title.append(SettingsStringUtil.DELIMITER);
                    title.append(this.mAppName);
                }
                wp.setTitle(title);
            }
        } else if (wp.type < 2000 || wp.type > 2999) {
            if (wp.token == null) {
                Window window = this.mContainer;
                wp.token = window == null ? this.mAppToken : window.mAppToken;
            }
            if ((curTitle == null || curTitle.length() == 0) && (str = this.mAppName) != null) {
                wp.setTitle(str);
            }
        } else if (curTitle == null || curTitle.length() == 0) {
            StringBuilder title2 = new StringBuilder(32);
            title2.append("Sys");
            title2.append(wp.type);
            if (this.mAppName != null) {
                title2.append(SettingsStringUtil.DELIMITER);
                title2.append(this.mAppName);
            }
            wp.setTitle(title2);
        }
        if (wp.packageName == null) {
            wp.packageName = this.mContext.getPackageName();
        }
        if (this.mHardwareAccelerated || (this.mWindowAttributes.flags & 16777216) != 0) {
            wp.flags |= 16777216;
        }
    }

    public WindowManager getWindowManager() {
        return this.mWindowManager;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public final Callback getCallback() {
        return this.mCallback;
    }

    public final void addOnFrameMetricsAvailableListener(OnFrameMetricsAvailableListener listener, Handler handler) {
        View decorView = getDecorView();
        if (decorView == null) {
            throw new IllegalStateException("can't observe a Window without an attached view");
        } else if (listener != null) {
            decorView.addFrameMetricsListener(this, listener, handler);
        } else {
            throw new NullPointerException("listener cannot be null");
        }
    }

    public final void removeOnFrameMetricsAvailableListener(OnFrameMetricsAvailableListener listener) {
        if (getDecorView() != null) {
            getDecorView().removeFrameMetricsListener(listener);
        }
    }

    public final void setOnWindowDismissedCallback(OnWindowDismissedCallback dcb) {
        this.mOnWindowDismissedCallback = dcb;
    }

    public final void dispatchOnWindowDismissed(boolean finishTask, boolean suppressWindowTransition) {
        OnWindowDismissedCallback onWindowDismissedCallback = this.mOnWindowDismissedCallback;
        if (onWindowDismissedCallback != null) {
            onWindowDismissedCallback.onWindowDismissed(finishTask, suppressWindowTransition);
        }
    }

    public final void setOnWindowSwipeDismissedCallback(OnWindowSwipeDismissedCallback sdcb) {
        this.mOnWindowSwipeDismissedCallback = sdcb;
    }

    public final void dispatchOnWindowSwipeDismissed() {
        OnWindowSwipeDismissedCallback onWindowSwipeDismissedCallback = this.mOnWindowSwipeDismissedCallback;
        if (onWindowSwipeDismissedCallback != null) {
            onWindowSwipeDismissedCallback.onWindowSwipeDismissed();
        }
    }

    public final void setWindowControllerCallback(WindowControllerCallback wccb) {
        this.mWindowControllerCallback = wccb;
    }

    public final WindowControllerCallback getWindowControllerCallback() {
        return this.mWindowControllerCallback;
    }

    public final void setRestrictedCaptionAreaListener(OnRestrictedCaptionAreaChangedListener listener) {
        this.mOnRestrictedCaptionAreaChangedListener = listener;
        this.mRestrictedCaptionAreaRect = listener != null ? new Rect() : null;
    }

    public void setLayout(int width, int height) {
        WindowManager.LayoutParams attrs = getAttributes();
        attrs.width = width;
        attrs.height = height;
        dispatchWindowAttributesChanged(attrs);
    }

    public void setGravity(int gravity) {
        WindowManager.LayoutParams attrs = getAttributes();
        attrs.gravity = gravity;
        dispatchWindowAttributesChanged(attrs);
    }

    public void setType(int type) {
        WindowManager.LayoutParams attrs = getAttributes();
        attrs.type = type;
        dispatchWindowAttributesChanged(attrs);
    }

    public void setFormat(int format) {
        WindowManager.LayoutParams attrs = getAttributes();
        if (format != 0) {
            attrs.format = format;
            this.mHaveWindowFormat = true;
        } else {
            attrs.format = this.mDefaultWindowFormat;
            this.mHaveWindowFormat = false;
        }
        dispatchWindowAttributesChanged(attrs);
    }

    public void setWindowAnimations(int resId) {
        WindowManager.LayoutParams attrs = getAttributes();
        attrs.windowAnimations = resId;
        dispatchWindowAttributesChanged(attrs);
    }

    public void setSoftInputMode(int mode) {
        WindowManager.LayoutParams attrs = getAttributes();
        if (mode != 0) {
            attrs.softInputMode = mode;
            this.mHasSoftInputMode = true;
        } else {
            this.mHasSoftInputMode = false;
        }
        dispatchWindowAttributesChanged(attrs);
    }

    public void addFlags(int flags) {
        setFlags(flags, flags);
    }

    @UnsupportedAppUsage
    public void addPrivateFlags(int flags) {
        setPrivateFlags(flags, flags);
    }

    @SystemApi
    public void addSystemFlags(int flags) {
        addPrivateFlags(flags);
    }

    public void clearFlags(int flags) {
        setFlags(0, flags);
    }

    public void setFlags(int flags, int mask) {
        WindowManager.LayoutParams attrs = getAttributes();
        attrs.flags = (attrs.flags & (~mask)) | (flags & mask);
        this.mForcedWindowFlags |= mask;
        dispatchWindowAttributesChanged(attrs);
    }

    private void setPrivateFlags(int flags, int mask) {
        WindowManager.LayoutParams attrs = getAttributes();
        attrs.privateFlags = (attrs.privateFlags & (~mask)) | (flags & mask);
        dispatchWindowAttributesChanged(attrs);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void setNeedsMenuKey(int value) {
        WindowManager.LayoutParams attrs = getAttributes();
        attrs.needsMenuKey = value;
        dispatchWindowAttributesChanged(attrs);
    }

    /* access modifiers changed from: protected */
    public void dispatchWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onWindowAttributesChanged(attrs);
        }
    }

    public void setHwFlags(int privateFlags, int mask) {
        WindowManager.LayoutParams attrs = getAttributes();
        attrs.privateFlags = (attrs.privateFlags & (~mask)) | (privateFlags & mask);
    }

    public void setColorMode(int colorMode) {
        WindowManager.LayoutParams attrs = getAttributes();
        attrs.setColorMode(colorMode);
        dispatchWindowAttributesChanged(attrs);
    }

    public int getColorMode() {
        return getAttributes().getColorMode();
    }

    public boolean isWideColorGamut() {
        if (getColorMode() != 1 || !getContext().getResources().getConfiguration().isScreenWideColorGamut()) {
            return false;
        }
        return true;
    }

    public void setDimAmount(float amount) {
        WindowManager.LayoutParams attrs = getAttributes();
        attrs.dimAmount = amount;
        this.mHaveDimAmount = true;
        dispatchWindowAttributesChanged(attrs);
    }

    public void setAttributes(WindowManager.LayoutParams a) {
        this.mWindowAttributes.copyFrom(a);
        dispatchWindowAttributesChanged(this.mWindowAttributes);
    }

    public final WindowManager.LayoutParams getAttributes() {
        return this.mWindowAttributes;
    }

    /* access modifiers changed from: protected */
    public final int getForcedWindowFlags() {
        return this.mForcedWindowFlags;
    }

    /* access modifiers changed from: protected */
    public final boolean hasSoftInputMode() {
        return this.mHasSoftInputMode;
    }

    @UnsupportedAppUsage
    public void setCloseOnTouchOutside(boolean close) {
        this.mCloseOnTouchOutside = close;
        this.mSetCloseOnTouchOutside = true;
    }

    @UnsupportedAppUsage
    public void setCloseOnTouchOutsideIfNotSet(boolean close) {
        if (!this.mSetCloseOnTouchOutside) {
            this.mCloseOnTouchOutside = close;
            this.mSetCloseOnTouchOutside = true;
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public boolean shouldCloseOnTouch(Context context, MotionEvent event) {
        return this.mCloseOnTouchOutside && peekDecorView() != null && ((event.getAction() == 1 && isOutOfBounds(context, event)) || event.getAction() == 4);
    }

    public void setSustainedPerformanceMode(boolean enable) {
        setPrivateFlags(enable ? 262144 : 0, 262144);
    }

    private boolean isOutOfBounds(Context context, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
        View decorView = getDecorView();
        return x < (-slop) || y < (-slop) || x > decorView.getWidth() + slop || y > decorView.getHeight() + slop;
    }

    public boolean requestFeature(int featureId) {
        int flag = 1 << featureId;
        this.mFeatures |= flag;
        int i = this.mLocalFeatures;
        Window window = this.mContainer;
        this.mLocalFeatures = i | (window != null ? (~window.mFeatures) & flag : flag);
        if ((this.mFeatures & flag) != 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void removeFeature(int featureId) {
        int flag = 1 << featureId;
        this.mFeatures &= ~flag;
        int i = this.mLocalFeatures;
        Window window = this.mContainer;
        this.mLocalFeatures = i & (~(window != null ? (~window.mFeatures) & flag : flag));
    }

    public final void makeActive() {
        Window window = this.mContainer;
        if (window != null) {
            Window window2 = window.mActiveChild;
            if (window2 != null) {
                window2.mIsActive = false;
            }
            this.mContainer.mActiveChild = this;
        }
        this.mIsActive = true;
        onActive();
    }

    public final boolean isActive() {
        return this.mIsActive;
    }

    public <T extends View> T findViewById(int id) {
        return (T) getDecorView().findViewById(id);
    }

    public final <T extends View> T requireViewById(int id) {
        T view = (T) findViewById(id);
        if (view != null) {
            return view;
        }
        throw new IllegalArgumentException("ID does not reference a View inside this Window");
    }

    public void setElevation(float elevation) {
    }

    public float getElevation() {
        return 0.0f;
    }

    public void setClipToOutline(boolean clipToOutline) {
    }

    public void setBackgroundDrawableResource(int resId) {
        setBackgroundDrawable(this.mContext.getDrawable(resId));
    }

    /* access modifiers changed from: protected */
    public final int getFeatures() {
        return this.mFeatures;
    }

    public static int getDefaultFeatures(Context context) {
        int features = 0;
        Resources res = context.getResources();
        if (res.getBoolean(R.bool.config_defaultWindowFeatureOptionsPanel)) {
            features = 0 | 1;
        }
        if (res.getBoolean(R.bool.config_defaultWindowFeatureContextMenu)) {
            return features | 64;
        }
        return features;
    }

    public boolean hasFeature(int feature) {
        return (getFeatures() & (1 << feature)) != 0;
    }

    /* access modifiers changed from: protected */
    public final int getLocalFeatures() {
        return this.mLocalFeatures;
    }

    /* access modifiers changed from: protected */
    public void setDefaultWindowFormat(int format) {
        this.mDefaultWindowFormat = format;
        if (!this.mHaveWindowFormat) {
            WindowManager.LayoutParams attrs = getAttributes();
            attrs.format = format;
            dispatchWindowAttributesChanged(attrs);
        }
    }

    /* access modifiers changed from: protected */
    public boolean haveDimAmount() {
        return this.mHaveDimAmount;
    }

    public void setMediaController(MediaController controller) {
    }

    public MediaController getMediaController() {
        return null;
    }

    public void setUiOptions(int uiOptions) {
    }

    public void setSplitActionBarAlways(boolean bAlwaysSplit) {
    }

    public void setUiOptions(int uiOptions, int mask) {
    }

    public void setIcon(int resId) {
    }

    public void setDefaultIcon(int resId) {
    }

    public void setLogo(int resId) {
    }

    public void setDefaultLogo(int resId) {
    }

    public void setLocalFocus(boolean hasFocus, boolean inTouchMode) {
    }

    public void injectInputEvent(InputEvent event) {
    }

    public TransitionManager getTransitionManager() {
        return null;
    }

    public void setTransitionManager(TransitionManager tm) {
        throw new UnsupportedOperationException();
    }

    public Scene getContentScene() {
        return null;
    }

    public void setEnterTransition(Transition transition) {
    }

    public void setReturnTransition(Transition transition) {
    }

    public void setExitTransition(Transition transition) {
    }

    public void setReenterTransition(Transition transition) {
    }

    public Transition getEnterTransition() {
        return null;
    }

    public Transition getReturnTransition() {
        return null;
    }

    public Transition getExitTransition() {
        return null;
    }

    public Transition getReenterTransition() {
        return null;
    }

    public void setSharedElementEnterTransition(Transition transition) {
    }

    public void setSharedElementReturnTransition(Transition transition) {
    }

    public Transition getSharedElementEnterTransition() {
        return null;
    }

    public Transition getSharedElementReturnTransition() {
        return null;
    }

    public void setSharedElementExitTransition(Transition transition) {
    }

    public void setSharedElementReenterTransition(Transition transition) {
    }

    public Transition getSharedElementExitTransition() {
        return null;
    }

    public Transition getSharedElementReenterTransition() {
        return null;
    }

    public void setAllowEnterTransitionOverlap(boolean allow) {
    }

    public boolean getAllowEnterTransitionOverlap() {
        return true;
    }

    public void setAllowReturnTransitionOverlap(boolean allow) {
    }

    public boolean getAllowReturnTransitionOverlap() {
        return true;
    }

    public long getTransitionBackgroundFadeDuration() {
        return 0;
    }

    public void setTransitionBackgroundFadeDuration(long fadeDurationMillis) {
    }

    public boolean getSharedElementsUseOverlay() {
        return true;
    }

    public void setSharedElementsUseOverlay(boolean sharedElementsUseOverlay) {
    }

    public void setNavigationBarDividerColor(int dividerColor) {
    }

    public int getNavigationBarDividerColor() {
        return 0;
    }

    public void setStatusBarContrastEnforced(boolean ensureContrast) {
    }

    public boolean isStatusBarContrastEnforced() {
        return false;
    }

    public void setNavigationBarContrastEnforced(boolean enforceContrast) {
    }

    public boolean isNavigationBarContrastEnforced() {
        return false;
    }

    public void setSystemGestureExclusionRects(List<Rect> list) {
        throw new UnsupportedOperationException("window does not support gesture exclusion rects");
    }

    public List<Rect> getSystemGestureExclusionRects() {
        return Collections.emptyList();
    }

    public void setTheme(int resId) {
    }

    public void setOverlayWithDecorCaptionEnabled(boolean enabled) {
        this.mOverlayWithDecorCaptionEnabled = enabled;
    }

    public boolean isOverlayWithDecorCaptionEnabled() {
        return this.mOverlayWithDecorCaptionEnabled;
    }

    public void notifyRestrictedCaptionAreaCallback(int left, int top, int right, int bottom) {
        if (this.mOnRestrictedCaptionAreaChangedListener != null) {
            this.mRestrictedCaptionAreaRect.set(left, top, right, bottom);
            this.mOnRestrictedCaptionAreaChangedListener.onRestrictedCaptionAreaChanged(this.mRestrictedCaptionAreaRect);
        }
    }

    public void setCloseOnSwipeEnabled(boolean closeOnSwipeEnabled) {
        this.mCloseOnSwipeEnabled = closeOnSwipeEnabled;
    }

    public boolean isCloseOnSwipeEnabled() {
        return this.mCloseOnSwipeEnabled;
    }

    public void setHwFloating(boolean isFloating) {
    }

    public boolean getHwFloating() {
        return false;
    }

    public void onFreeFormOutLineChanged(int touchingState) {
    }

    public void restoreFreeFormConfig() {
    }
}
