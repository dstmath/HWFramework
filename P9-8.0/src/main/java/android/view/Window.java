package android.view;

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
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.rms.AppAssociate;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.SurfaceHolder.Callback2;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.R;
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
    public static final String PROPERTY_HARDWARE_UI = "persist.sys.ui.hw";
    public static final String STATUS_BAR_BACKGROUND_TRANSITION_NAME = "android:status:background";
    private Window mActiveChild;
    private String mAppName;
    private IBinder mAppToken;
    private Callback mCallback;
    private boolean mCloseOnSwipeEnabled = false;
    private boolean mCloseOnTouchOutside = false;
    private Window mContainer;
    private final Context mContext;
    private int mDefaultWindowFormat = -1;
    private boolean mDestroyed;
    private int mFeatures;
    private int mForcedWindowFlags = 0;
    private boolean mHardwareAccelerated;
    private boolean mHasChildren = false;
    private boolean mHasSoftInputMode = false;
    private boolean mHaveDimAmount = false;
    private boolean mHaveWindowFormat = false;
    private boolean mIsActive = false;
    private int mLocalFeatures;
    private OnRestrictedCaptionAreaChangedListener mOnRestrictedCaptionAreaChangedListener;
    private OnWindowDismissedCallback mOnWindowDismissedCallback;
    private OnWindowSwipeDismissedCallback mOnWindowSwipeDismissedCallback;
    private boolean mOverlayWithDecorCaptionEnabled = false;
    private Rect mRestrictedCaptionAreaRect;
    private boolean mSetCloseOnTouchOutside = false;
    private final LayoutParams mWindowAttributes = new LayoutParams();
    private WindowControllerCallback mWindowControllerCallback;
    private WindowManager mWindowManager;
    private TypedArray mWindowStyle;

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

        void onWindowAttributesChanged(LayoutParams layoutParams);

        void onWindowFocusChanged(boolean z);

        ActionMode onWindowStartingActionMode(android.view.ActionMode.Callback callback);

        ActionMode onWindowStartingActionMode(android.view.ActionMode.Callback callback, int i);

        void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> list, Menu menu, int deviceId) {
        }

        void onPointerCaptureChanged(boolean hasCapture) {
        }
    }

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

        void exitFreeformMode() throws RemoteException;

        int getWindowStackId() throws RemoteException;

        boolean isTaskRoot();
    }

    public abstract void addContentView(View view, ViewGroup.LayoutParams layoutParams);

    public abstract void alwaysReadCloseOnTouchAttr();

    public abstract void clearContentView();

    public abstract void closeAllPanels();

    public abstract void closePanel(int i);

    public abstract View getCurrentFocus();

    public abstract View getDecorView();

    public abstract LayoutInflater getLayoutInflater();

    public abstract int getNavigationBarColor();

    public abstract int getStatusBarColor();

    public abstract int getVolumeControlStream();

    public abstract void invalidatePanelMenu(int i);

    public abstract boolean isFloating();

    public abstract boolean isShortcutKey(int i, KeyEvent keyEvent);

    protected abstract void onActive();

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

    public abstract void takeInputQueue(android.view.InputQueue.Callback callback);

    public abstract void takeKeyEvents(boolean z);

    public abstract void takeSurface(Callback2 callback2);

    public abstract void togglePanel(int i, KeyEvent keyEvent);

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
    }

    public final boolean isDestroyed() {
        return this.mDestroyed;
    }

    public void setWindowManager(WindowManager wm, IBinder appToken, String appName) {
        setWindowManager(wm, appToken, appName, false);
    }

    public void setWindowManager(WindowManager wm, IBinder appToken, String appName, boolean hardwareAccelerated) {
        boolean z;
        this.mAppToken = appToken;
        this.mAppName = appName;
        if (hardwareAccelerated) {
            z = true;
        } else {
            z = SystemProperties.getBoolean(PROPERTY_HARDWARE_UI, false);
        }
        this.mHardwareAccelerated = z;
        if (wm == null) {
            wm = (WindowManager) this.mContext.getSystemService(AppAssociate.ASSOC_WINDOW);
        }
        this.mWindowManager = ((WindowManagerImpl) wm).createLocalWindowManager(this);
    }

    void adjustLayoutParamsForSubWindow(LayoutParams wp) {
        CharSequence curTitle = wp.getTitle();
        StringBuilder title;
        if (wp.type >= 1000 && wp.type <= LayoutParams.LAST_SUB_WINDOW) {
            if (wp.token == null) {
                View decor = peekDecorView();
                if (decor != null) {
                    wp.token = decor.getWindowToken();
                }
            }
            if (curTitle == null || curTitle.length() == 0) {
                title = new StringBuilder(32);
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
                    title.append(SettingsStringUtil.DELIMITER).append(this.mAppName);
                }
                wp.setTitle(title);
            }
        } else if (wp.type < 2000 || wp.type > 2999) {
            if (wp.token == null) {
                wp.token = this.mContainer == null ? this.mAppToken : this.mContainer.mAppToken;
            }
            if ((curTitle == null || curTitle.length() == 0) && this.mAppName != null) {
                wp.setTitle(this.mAppName);
            }
        } else if (curTitle == null || curTitle.length() == 0) {
            title = new StringBuilder(32);
            title.append("Sys").append(wp.type);
            if (this.mAppName != null) {
                title.append(SettingsStringUtil.DELIMITER).append(this.mAppName);
            }
            wp.setTitle(title);
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
        } else if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        } else {
            decorView.addFrameMetricsListener(this, listener, handler);
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
        if (this.mOnWindowDismissedCallback != null) {
            this.mOnWindowDismissedCallback.onWindowDismissed(finishTask, suppressWindowTransition);
        }
    }

    public final void setOnWindowSwipeDismissedCallback(OnWindowSwipeDismissedCallback sdcb) {
        this.mOnWindowSwipeDismissedCallback = sdcb;
    }

    public final void dispatchOnWindowSwipeDismissed() {
        if (this.mOnWindowSwipeDismissedCallback != null) {
            this.mOnWindowSwipeDismissedCallback.onWindowSwipeDismissed();
        }
    }

    public final void setWindowControllerCallback(WindowControllerCallback wccb) {
        this.mWindowControllerCallback = wccb;
    }

    public final WindowControllerCallback getWindowControllerCallback() {
        return this.mWindowControllerCallback;
    }

    public final void setRestrictedCaptionAreaListener(OnRestrictedCaptionAreaChangedListener listener) {
        Rect rect = null;
        this.mOnRestrictedCaptionAreaChangedListener = listener;
        if (listener != null) {
            rect = new Rect();
        }
        this.mRestrictedCaptionAreaRect = rect;
    }

    public void setLayout(int width, int height) {
        LayoutParams attrs = getAttributes();
        attrs.width = width;
        attrs.height = height;
        dispatchWindowAttributesChanged(attrs);
    }

    public void setGravity(int gravity) {
        LayoutParams attrs = getAttributes();
        attrs.gravity = gravity;
        dispatchWindowAttributesChanged(attrs);
    }

    public void setType(int type) {
        LayoutParams attrs = getAttributes();
        attrs.type = type;
        dispatchWindowAttributesChanged(attrs);
    }

    public void setFormat(int format) {
        LayoutParams attrs = getAttributes();
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
        LayoutParams attrs = getAttributes();
        attrs.windowAnimations = resId;
        dispatchWindowAttributesChanged(attrs);
    }

    public void setSoftInputMode(int mode) {
        LayoutParams attrs = getAttributes();
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

    public void addPrivateFlags(int flags) {
        setPrivateFlags(flags, flags);
    }

    public void clearFlags(int flags) {
        setFlags(0, flags);
    }

    public void setFlags(int flags, int mask) {
        LayoutParams attrs = getAttributes();
        attrs.flags = (attrs.flags & (~mask)) | (flags & mask);
        this.mForcedWindowFlags |= mask;
        dispatchWindowAttributesChanged(attrs);
    }

    private void setPrivateFlags(int flags, int mask) {
        LayoutParams attrs = getAttributes();
        attrs.privateFlags = (attrs.privateFlags & (~mask)) | (flags & mask);
        dispatchWindowAttributesChanged(attrs);
    }

    protected void setNeedsMenuKey(int value) {
        LayoutParams attrs = getAttributes();
        attrs.needsMenuKey = value;
        dispatchWindowAttributesChanged(attrs);
    }

    protected void dispatchWindowAttributesChanged(LayoutParams attrs) {
        if (this.mCallback != null) {
            this.mCallback.onWindowAttributesChanged(attrs);
        }
    }

    public void setHwFlags(int privateFlags, int mask) {
        LayoutParams attrs = getAttributes();
        attrs.privateFlags = (attrs.privateFlags & (~mask)) | (privateFlags & mask);
    }

    public void setColorMode(int colorMode) {
        LayoutParams attrs = getAttributes();
        attrs.setColorMode(colorMode);
        dispatchWindowAttributesChanged(attrs);
    }

    public int getColorMode() {
        return getAttributes().getColorMode();
    }

    public void setDimAmount(float amount) {
        LayoutParams attrs = getAttributes();
        attrs.dimAmount = amount;
        this.mHaveDimAmount = true;
        dispatchWindowAttributesChanged(attrs);
    }

    public void setAttributes(LayoutParams a) {
        this.mWindowAttributes.copyFrom(a);
        dispatchWindowAttributesChanged(this.mWindowAttributes);
    }

    public final LayoutParams getAttributes() {
        return this.mWindowAttributes;
    }

    protected final int getForcedWindowFlags() {
        return this.mForcedWindowFlags;
    }

    protected final boolean hasSoftInputMode() {
        return this.mHasSoftInputMode;
    }

    public void setCloseOnTouchOutside(boolean close) {
        this.mCloseOnTouchOutside = close;
        this.mSetCloseOnTouchOutside = true;
    }

    public void setCloseOnTouchOutsideIfNotSet(boolean close) {
        if (!this.mSetCloseOnTouchOutside) {
            this.mCloseOnTouchOutside = close;
            this.mSetCloseOnTouchOutside = true;
        }
    }

    public void setDisableWallpaperTouchEvents(boolean disable) {
        int i;
        if (disable) {
            i = 2048;
        } else {
            i = 0;
        }
        setPrivateFlags(i, 2048);
    }

    public boolean shouldCloseOnTouch(Context context, MotionEvent event) {
        if (this.mCloseOnTouchOutside && event.getAction() == 0 && isOutOfBounds(context, event) && peekDecorView() != null) {
            return true;
        }
        return false;
    }

    public void setSustainedPerformanceMode(boolean enable) {
        int i;
        if (enable) {
            i = 262144;
        } else {
            i = 0;
        }
        setPrivateFlags(i, 262144);
    }

    private boolean isOutOfBounds(Context context, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
        View decorView = getDecorView();
        if (x < (-slop) || y < (-slop) || x > decorView.getWidth() + slop || y > decorView.getHeight() + slop) {
            return true;
        }
        return false;
    }

    public boolean requestFeature(int featureId) {
        int i;
        int flag = 1 << featureId;
        this.mFeatures |= flag;
        int i2 = this.mLocalFeatures;
        if (this.mContainer != null) {
            i = (~this.mContainer.mFeatures) & flag;
        } else {
            i = flag;
        }
        this.mLocalFeatures = i | i2;
        if ((this.mFeatures & flag) != 0) {
            return true;
        }
        return false;
    }

    protected void removeFeature(int featureId) {
        int flag = 1 << featureId;
        this.mFeatures &= ~flag;
        int i = this.mLocalFeatures;
        if (this.mContainer != null) {
            flag &= ~this.mContainer.mFeatures;
        }
        this.mLocalFeatures = i & (~flag);
    }

    public final void makeActive() {
        if (this.mContainer != null) {
            if (this.mContainer.mActiveChild != null) {
                this.mContainer.mActiveChild.mIsActive = false;
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
        return getDecorView().findViewById(id);
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

    protected final int getFeatures() {
        return this.mFeatures;
    }

    public static int getDefaultFeatures(Context context) {
        int features = 0;
        Resources res = context.getResources();
        if (res.getBoolean(R.bool.config_defaultWindowFeatureOptionsPanel)) {
            features = 1;
        }
        if (res.getBoolean(R.bool.config_defaultWindowFeatureContextMenu)) {
            return features | 64;
        }
        return features;
    }

    public boolean hasFeature(int feature) {
        return (getFeatures() & (1 << feature)) != 0;
    }

    protected final int getLocalFeatures() {
        return this.mLocalFeatures;
    }

    protected void setDefaultWindowFormat(int format) {
        this.mDefaultWindowFormat = format;
        if (!this.mHaveWindowFormat) {
            LayoutParams attrs = getAttributes();
            attrs.format = format;
            dispatchWindowAttributesChanged(attrs);
        }
    }

    protected boolean haveDimAmount() {
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

    public void setHwDrawerFeature(boolean using, int overlayActionBar) {
    }

    public void setDrawerOpend(boolean open) {
    }
}
