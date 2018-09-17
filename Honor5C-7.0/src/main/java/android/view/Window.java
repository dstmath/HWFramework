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
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.SurfaceHolder.Callback2;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.R;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.AsyncService;
import com.android.internal.util.Protocol;
import com.huawei.pgmng.log.LogPower;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

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
    private boolean mCloseOnTouchOutside;
    private Window mContainer;
    private final Context mContext;
    private int mDefaultWindowFormat;
    private boolean mDestroyed;
    private int mFeatures;
    private int mForcedWindowFlags;
    private boolean mHardwareAccelerated;
    private boolean mHasChildren;
    private boolean mHasSoftInputMode;
    private boolean mHaveDimAmount;
    private boolean mHaveWindowFormat;
    private boolean mIsActive;
    private int mLocalFeatures;
    private OnRestrictedCaptionAreaChangedListener mOnRestrictedCaptionAreaChangedListener;
    private OnWindowDismissedCallback mOnWindowDismissedCallback;
    private boolean mOverlayWithDecorCaptionEnabled;
    private Rect mRestrictedCaptionAreaRect;
    private boolean mSetCloseOnTouchOutside;
    private final LayoutParams mWindowAttributes;
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
    }

    public interface OnFrameMetricsAvailableListener {
        void onFrameMetricsAvailable(Window window, FrameMetrics frameMetrics, int i);
    }

    public interface OnRestrictedCaptionAreaChangedListener {
        void onRestrictedCaptionAreaChanged(Rect rect);
    }

    public interface OnWindowDismissedCallback {
        void onWindowDismissed(boolean z);
    }

    public interface WindowControllerCallback {
        void enterPictureInPictureModeIfPossible();

        void exitFreeformMode() throws RemoteException;

        int getWindowStackId() throws RemoteException;
    }

    private void setPrivateFlags(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.Window.setPrivateFlags(int, int):void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.Window.setPrivateFlags(int, int):void");
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

    public abstract void openPanel(int i, KeyEvent keyEvent);

    public abstract View peekDecorView();

    public abstract boolean performContextMenuIdentifierAction(int i, int i2);

    public abstract boolean performPanelIdentifierAction(int i, int i2, int i3);

    public abstract boolean performPanelShortcut(int i, int i2, KeyEvent keyEvent, int i3);

    protected void removeFeature(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.Window.removeFeature(int):void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.Window.removeFeature(int):void");
    }

    public abstract void reportActivityRelaunched();

    public boolean requestFeature(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.Window.requestFeature(int):boolean
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.Window.requestFeature(int):boolean");
    }

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

    public void setFlags(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.Window.setFlags(int, int):void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.Window.setFlags(int, int):void");
    }

    public void setHwFlags(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.Window.setHwFlags(int, int):void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.Window.setHwFlags(int, int):void");
    }

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
        this.mIsActive = false;
        this.mHasChildren = false;
        this.mCloseOnTouchOutside = false;
        this.mSetCloseOnTouchOutside = false;
        this.mForcedWindowFlags = PROGRESS_START;
        this.mHaveWindowFormat = false;
        this.mHaveDimAmount = false;
        this.mDefaultWindowFormat = PROGRESS_VISIBILITY_ON;
        this.mHasSoftInputMode = false;
        this.mOverlayWithDecorCaptionEnabled = false;
        this.mWindowAttributes = new LayoutParams();
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
            this.mFeatures |= FEATURE_PROGRESS;
            this.mLocalFeatures |= FEATURE_PROGRESS;
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
            wm = (WindowManager) this.mContext.getSystemService("window");
        }
        this.mWindowManager = ((WindowManagerImpl) wm).createLocalWindowManager(this);
    }

    void adjustLayoutParamsForSubWindow(LayoutParams wp) {
        CharSequence curTitle = wp.getTitle();
        StringBuilder title;
        if (wp.type >= RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED && wp.type <= LayoutParams.LAST_SUB_WINDOW) {
            if (wp.token == null) {
                View decor = peekDecorView();
                if (decor != null) {
                    wp.token = decor.getWindowToken();
                }
            }
            if (curTitle == null || curTitle.length() == 0) {
                title = new StringBuilder(32);
                if (wp.type == RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED) {
                    title.append("Media");
                } else if (wp.type == RILConstants.RIL_UNSOL_RESPONSE_NEW_SMS_STATUS_REPORT) {
                    title.append("MediaOvr");
                } else if (wp.type == RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED) {
                    title.append("Panel");
                } else if (wp.type == RILConstants.RIL_UNSOL_RESPONSE_VOICE_NETWORK_STATE_CHANGED) {
                    title.append("SubPanel");
                } else if (wp.type == RILConstants.RIL_UNSOL_RESPONSE_NEW_SMS_ON_SIM) {
                    title.append("AboveSubPanel");
                } else if (wp.type == RILConstants.RIL_UNSOL_RESPONSE_NEW_SMS) {
                    title.append("AtchDlg");
                } else {
                    title.append(wp.type);
                }
                if (this.mAppName != null) {
                    title.append(":").append(this.mAppName);
                }
                wp.setTitle(title);
            }
        } else if (wp.type < LogPower.FIRST_IAWARE_TAG || wp.type > LogPower.LAST_IAWARE_TAG) {
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
                title.append(":").append(this.mAppName);
            }
            wp.setTitle(title);
        }
        if (wp.packageName == null) {
            wp.packageName = this.mContext.getPackageName();
        }
        if (this.mHardwareAccelerated || (this.mWindowAttributes.flags & AsyncService.CMD_ASYNC_SERVICE_DESTROY) != 0) {
            wp.flags |= AsyncService.CMD_ASYNC_SERVICE_DESTROY;
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

    public final void dispatchOnWindowDismissed(boolean finishTask) {
        if (this.mOnWindowDismissedCallback != null) {
            this.mOnWindowDismissedCallback.onWindowDismissed(finishTask);
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
        setFlags(PROGRESS_START, flags);
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
            i = GL10.GL_EXP;
        } else {
            i = PROGRESS_START;
        }
        setPrivateFlags(i, GL10.GL_EXP);
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
            i = Protocol.BASE_DATA_CONNECTION;
        } else {
            i = PROGRESS_START;
        }
        setPrivateFlags(i, Protocol.BASE_DATA_CONNECTION);
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

    public View findViewById(int id) {
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
        int features = PROGRESS_START;
        Resources res = context.getResources();
        if (res.getBoolean(R.bool.config_defaultWindowFeatureOptionsPanel)) {
            features = FEATURE_NO_TITLE;
        }
        if (res.getBoolean(R.bool.config_defaultWindowFeatureContextMenu)) {
            return features | 64;
        }
        return features;
    }

    public boolean hasFeature(int feature) {
        return (getFeatures() & (FEATURE_NO_TITLE << feature)) != 0;
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
