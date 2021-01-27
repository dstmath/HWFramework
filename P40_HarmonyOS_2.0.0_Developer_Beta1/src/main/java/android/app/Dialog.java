package android.app;

import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.res.AbsResources;
import android.content.res.AbsResourcesImpl;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.hwtheme.HwThemeManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.MemoryLeakMonitorManager;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.HwWidgetColumn;
import com.android.hwext.internal.R;
import com.android.internal.policy.HwPolicyFactory;
import com.huawei.android.view.HwWindowManager;
import java.lang.annotation.RCUnownedThisRef;
import java.lang.ref.WeakReference;

public class Dialog implements DialogInterface, Window.Callback, KeyEvent.Callback, View.OnCreateContextMenuListener, Window.OnWindowDismissedCallback {
    private static final int ALPHA_MASK = 255;
    private static final int ALPHA_POS = 24;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private static final int CANCEL = 68;
    private static final int COLOR_MASK = -1;
    private static final int DEVICE_TYPE_PHONE = 1;
    private static final String DIALOG_HIERARCHY_TAG = "android:dialogHierarchy";
    private static final String DIALOG_SHOWING_TAG = "android:dialogShowing";
    private static final int DISMISS = 67;
    private static final float FLOAT_COMPARE_VALUE = 1.0E-6f;
    private static final int NON_TRANSPARENT = 255;
    private static final int SHOW = 69;
    private static final String TAG = "Dialog";
    private ActionBar mActionBar;
    private ActionMode mActionMode;
    private int mActionModeTypeStarting;
    private String mCancelAndDismissTaken;
    @UnsupportedAppUsage
    private Message mCancelMessage;
    protected boolean mCancelable;
    private boolean mCanceled;
    @UnsupportedAppUsage
    final Context mContext;
    private boolean mCreated;
    View mDecor;
    private int mDeviceType;
    @UnsupportedAppUsage
    private Message mDismissMessage;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final Handler mHandler;
    private HwWidgetColumn mHwWidgetColumn;
    private float mLastDensity;
    private int mLastHeightPixel;
    private int mLastWidthPixel;
    @UnsupportedAppUsage
    private final Handler mListenersHandler;
    @UnsupportedAppUsage
    private DialogInterface.OnKeyListener mOnKeyListener;
    private final View.OnLayoutChangeListener mOrientationChangeHandler;
    @UnsupportedAppUsage
    private Activity mOwnerActivity;
    private SearchEvent mSearchEvent;
    @UnsupportedAppUsage
    private Message mShowMessage;
    @UnsupportedAppUsage
    private boolean mShowing;
    @UnsupportedAppUsage
    final Window mWindow;
    private final WindowManager mWindowManager;
    private int mYOffset;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void layoutWhenGravityChanged() {
        WindowManager.LayoutParams params = this.mWindow.getAttributes();
        ViewGroup.LayoutParams vl = this.mDecor.getLayoutParams();
        if (vl instanceof WindowManager.LayoutParams) {
            params = (WindowManager.LayoutParams) vl;
        } else {
            Log.e(TAG, "Params must be WindowManager.LayoutParams.");
        }
        if (2011 != params.type) {
            boolean isFormalSet = isFormalSet(params);
            if (!isPhoneDevice()) {
                setCenterGravityParams(params, isFormalSet);
            } else if (!isInMultiWindowMode(params)) {
                updateShowPosition(params, isFormalSet);
            } else {
                setCenterGravityParams(params, isFormalSet);
            }
            this.mWindow.setAttributes(params);
        }
    }

    public Dialog(Context context) {
        this(context, 0, true);
    }

    public Dialog(Context context, int themeResId) {
        this(context, themeResId, true);
    }

    Dialog(Context context, int themeResId, boolean createContextThemeWrapper) {
        this.mCancelable = true;
        this.mCreated = false;
        this.mShowing = false;
        this.mCanceled = false;
        this.mHandler = new Handler();
        this.mActionModeTypeStarting = 0;
        this.mLastWidthPixel = 0;
        this.mLastHeightPixel = 0;
        this.mLastDensity = 0.0f;
        this.mOrientationChangeHandler = new View.OnLayoutChangeListener() {
            /* class android.app.Dialog.AnonymousClass1 */
            private final Rect mNewRect = new Rect();
            private final Rect mOldRect = new Rect();

            @Override // android.view.View.OnLayoutChangeListener
            @RCUnownedThisRef
            public void onLayoutChange(View view, int newLeft, int newRight, int newTop, int newBottom, int oldLeft, int oldRight, int oldTop, int oldBottom) {
                this.mNewRect.set(newLeft, newRight, newTop, newBottom);
                this.mOldRect.set(oldLeft, oldRight, oldTop, oldBottom);
                if (Dialog.this.isShowing() && !this.mNewRect.equals(this.mOldRect)) {
                    Dialog.this.layoutWhenGravityChanged();
                }
                if (HwWidgetFactory.isHwTheme(Dialog.this.mContext)) {
                    Dialog dialog = Dialog.this;
                    if (dialog instanceof AlertDialog) {
                        dialog.setDialogColumnLayout();
                    }
                }
            }
        };
        if (createContextThemeWrapper) {
            if (themeResId == 0) {
                TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(16843528, outValue, true);
                themeResId = outValue.resourceId;
            }
            this.mContext = new ContextThemeWrapper(context, themeResId);
        } else {
            this.mContext = context;
        }
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        Window w = HwPolicyFactory.getHwPhoneWindow(this.mContext);
        this.mWindow = w;
        w.setCallback(this);
        w.setOnWindowDismissedCallback(this);
        w.setOnWindowSwipeDismissedCallback(new Window.OnWindowSwipeDismissedCallback() {
            /* class android.app.Dialog.AnonymousClass2 */

            @Override // android.view.Window.OnWindowSwipeDismissedCallback
            @RCUnownedThisRef
            public void onWindowSwipeDismissed() {
                if (Dialog.this.mCancelable) {
                    Dialog.this.cancel();
                }
            }
        });
        w.setWindowManager(this.mWindowManager, null, null);
        w.setGravity(17);
        this.mListenersHandler = new ListenersHandler(this);
        this.mDeviceType = this.mContext.getResources().getInteger(R.integer.emui_device_type);
    }

    private int getYOffset() {
        TypedValue outValue = new TypedValue();
        this.mContext.getTheme().resolveAttribute(R.attr.dialogMarginBottom, outValue, true);
        return TypedValue.complexToDimensionPixelSize(outValue.data, this.mContext.getResources().getDisplayMetrics());
    }

    @Deprecated
    protected Dialog(Context context, boolean cancelable, Message cancelCallback) {
        this(context);
        this.mCancelable = cancelable;
        updateWindowForCancelable();
        this.mCancelMessage = cancelCallback;
    }

    protected Dialog(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        this(context);
        this.mCancelable = cancelable;
        updateWindowForCancelable();
        setOnCancelListener(cancelListener);
    }

    public final Context getContext() {
        return this.mContext;
    }

    public ActionBar getActionBar() {
        return this.mActionBar;
    }

    public final void setOwnerActivity(Activity activity) {
        this.mOwnerActivity = activity;
        getWindow().setVolumeControlStream(this.mOwnerActivity.getVolumeControlStream());
    }

    public final Activity getOwnerActivity() {
        return this.mOwnerActivity;
    }

    public boolean isShowing() {
        View view = this.mDecor;
        return view != null && view.getVisibility() == 0;
    }

    public void create() {
        if (!this.mCreated) {
            dispatchOnCreate(null);
        }
    }

    public void show() {
        if (HwWindowManager.isAppControlPolicyExists() && HwWindowManager.isNeedForbidDialogAct(this.mContext.getPackageName(), getForbidDialogComponentName(this.mContext))) {
            Log.i(TAG, "show dialog isNeedForbidDialogAct forbid");
        } else if (!this.mShowing) {
            this.mCanceled = false;
            if (!this.mCreated) {
                dispatchOnCreate(null);
            } else {
                this.mWindow.getDecorView().dispatchConfigurationChanged(this.mContext.getResources().getConfiguration());
            }
            onStart();
            this.mDecor = this.mWindow.getDecorView();
            if (this.mActionBar == null && this.mWindow.hasFeature(8)) {
                ApplicationInfo info = this.mContext.getApplicationInfo();
                this.mWindow.setDefaultIcon(info.icon);
                this.mWindow.setDefaultLogo(info.logo);
                this.mActionBar = HwWidgetFactory.getHuaweiActionBarImpl(this);
            }
            WindowManager.LayoutParams l = this.mWindow.getAttributes();
            l.layoutInDisplaySideMode = 1;
            boolean isFormalSet = isFormalSet(l);
            if (HwWidgetFactory.isHwTheme(this.mContext) && (this instanceof AlertDialog) && 2011 != l.type) {
                if (isPhoneDevice()) {
                    updateShowPosition(l, isFormalSet);
                } else {
                    setCenterGravityParams(l, isFormalSet);
                }
            }
            boolean restoreSoftInputMode = false;
            if ((l.softInputMode & 256) == 0) {
                l.softInputMode |= 256;
                restoreSoftInputMode = true;
            }
            enableBlurEffect(l);
            this.mWindowManager.addView(this.mDecor, l);
            if (isPhoneDevice()) {
                updateMultiWindowModePositionAfterAddView(l, isFormalSet);
            }
            if (restoreSoftInputMode) {
                l.softInputMode &= TrafficStats.TAG_NETWORK_STACK_RANGE_END;
            }
            if (HwWidgetFactory.isHwTheme(this.mContext) && (this instanceof AlertDialog)) {
                setDialogColumnLayout();
            }
            this.mShowing = true;
            sendShowMessage();
        } else if (this.mDecor != null) {
            if (this.mWindow.hasFeature(8)) {
                this.mWindow.invalidatePanelMenu(8);
            }
            this.mDecor.setVisibility(0);
        }
    }

    private void enableBlurEffect(WindowManager.LayoutParams layoutParams) {
        View view;
        if (HwWidgetFactory.isHwTheme(this.mContext) && (this instanceof AlertDialog)) {
            if ((layoutParams.hwFlags & 33554432) == 0 || (view = this.mDecor) == null || !view.isBlurEnabled()) {
                clearBlurBehindFlag(layoutParams);
                return;
            }
            Drawable drawable = this.mDecor.getBackground();
            if (drawable == null) {
                clearBlurBehindFlag(layoutParams);
                return;
            }
            TypedValue colorValue = new TypedValue();
            TypedValue blurColorValue = new TypedValue();
            Resources.Theme currentTheme = this.mContext.getTheme();
            boolean isColorValueValid = currentTheme.resolveAttribute(33620271, colorValue, true);
            boolean isBlurColorValueValid = currentTheme.resolveAttribute(R.attr.colorDialogBgBlur, blurColorValue, true);
            if (!isColorValueValid || !isBlurColorValueValid) {
                clearBlurBehindFlag(layoutParams);
                return;
            }
            int blurColorResId = blurColorValue.resourceId;
            Resources resources = this.mContext.getResources();
            if (isThemeColor(resources, blurColorResId)) {
                int themeBlurColor = resources.getColor(blurColorResId, currentTheme);
                int themeBlurColorAlpha = getColorAlpha(themeBlurColor);
                if (themeBlurColorAlpha != 255) {
                    drawable.setColorFilter(themeBlurColor & -1, PorterDuff.Mode.SRC);
                    drawable.setAlpha(themeBlurColorAlpha);
                    return;
                }
                clearBlurBehindFlag(layoutParams);
            } else if (!isThemeColor(resources, colorValue.resourceId)) {
                int systemBlurColor = blurColorValue.data;
                drawable.setColorFilter(systemBlurColor & -1, PorterDuff.Mode.SRC);
                drawable.setAlpha(getColorAlpha(systemBlurColor));
            } else {
                clearBlurBehindFlag(layoutParams);
            }
        }
    }

    private boolean isThemeColor(Resources resources, int resourceId) {
        AbsResourcesImpl.ThemeColor themeColor;
        AbsResources hwResources = HwThemeManager.getHwResources();
        if (hwResources == null || (themeColor = hwResources.getColor(resources, new TypedValue(), resourceId)) == null) {
            return false;
        }
        return themeColor.mIsThemed;
    }

    private void clearBlurBehindFlag(WindowManager.LayoutParams layoutParams) {
        layoutParams.hwFlags &= -33554433;
    }

    private int getColorAlpha(int color) {
        return (color >> 24) & 255;
    }

    private boolean isEquals(float value, float targetValue) {
        return Math.abs(value - targetValue) < FLOAT_COMPARE_VALUE;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDialogColumnLayout() {
        Context context = this.mContext;
        if (context != null && this.mDeviceType == 1) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int widthPixel = displayMetrics.widthPixels;
            int heightPixel = displayMetrics.heightPixels;
            float density = displayMetrics.density;
            if (widthPixel != this.mLastWidthPixel || heightPixel != this.mLastHeightPixel || !isEquals(density, this.mLastDensity)) {
                this.mLastWidthPixel = widthPixel;
                this.mLastHeightPixel = heightPixel;
                this.mLastDensity = density;
                if (this.mHwWidgetColumn == null) {
                    this.mHwWidgetColumn = HwWidgetFactory.getHwWidgetColumn(this.mContext);
                }
                int columnWidth = this.mHwWidgetColumn.getMinColumnWidth(2);
                if (columnWidth <= widthPixel && columnWidth > 0) {
                    Rect rect = new Rect(0, 0, 0, 0);
                    WindowManager.LayoutParams layoutParams = this.mWindow.getAttributes();
                    Drawable drawable = this.mDecor.getBackground();
                    if (drawable instanceof InsetDrawable) {
                        drawable.getPadding(rect);
                    }
                    int windowWidth = rect.left + columnWidth + rect.right;
                    layoutParams.width = windowWidth <= widthPixel ? windowWidth : widthPixel;
                    this.mWindow.setAttributes(layoutParams);
                }
            }
        }
    }

    private void setCenterGravityParams(WindowManager.LayoutParams params, boolean isFormalSet) {
        params.gravity = 17;
        params.y = isFormalSet ? params.y : 0;
    }

    private void setBottomGravityParams(WindowManager.LayoutParams params, boolean isFormalSet) {
        params.gravity = 80;
        params.y = isFormalSet ? params.y : this.mYOffset;
    }

    private boolean isFormalSet(WindowManager.LayoutParams params) {
        int formalYOffset = params.y;
        if (this.mYOffset == 0) {
            this.mYOffset = getYOffset();
        }
        return (formalYOffset == this.mYOffset || formalYOffset == 0) ? false : true;
    }

    private boolean isTablet() {
        return this.mContext.getResources().getBoolean(34537472);
    }

    private boolean isPhoneDevice() {
        return this.mDeviceType == 1 && !isTablet();
    }

    private void updateMultiWindowModePositionAfterAddView(WindowManager.LayoutParams params, boolean isFormalSet) {
        if (HwWidgetFactory.isHwTheme(this.mContext) && (this instanceof AlertDialog) && isInMultiWindowMode(params)) {
            setCenterGravityParams(params, isFormalSet);
            this.mWindow.setAttributes(params);
        }
    }

    private void updateShowPosition(WindowManager.LayoutParams params, boolean isFormalSet) {
        if (this.mContext.getResources().getConfiguration().orientation == 1) {
            setBottomGravityParams(params, isFormalSet);
        } else {
            setCenterGravityParams(params, isFormalSet);
        }
    }

    private boolean isInMultiWindowMode(WindowManager.LayoutParams params) {
        try {
            if (this.mContext == null || this.mContext.getResources() == null || !this.mContext.getResources().getConfiguration().windowConfiguration.inHwPCFreeFormWindowingMode()) {
                return ActivityTaskManager.getService().isInMultiWindowMode(params.token);
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Dialog:RemoteException");
            return false;
        }
    }

    public void hide() {
        View view = this.mDecor;
        if (view != null) {
            view.setVisibility(8);
        }
    }

    @Override // android.content.DialogInterface
    public void dismiss() {
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            dismissDialog();
        } else {
            this.mHandler.post(new Runnable() {
                /* class android.app.$$Lambda$oslF4K8Uk6v6nTRoaEpCmfAptE */

                @Override // java.lang.Runnable
                public final void run() {
                    Dialog.this.dismissDialog();
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dismissDialog() {
        if (this.mDecor != null && this.mShowing) {
            if (this.mWindow.isDestroyed()) {
                Log.e(TAG, "Tried to dismissDialog() but the Dialog's window was already destroyed!");
                return;
            }
            try {
                this.mWindowManager.removeViewImmediate(this.mDecor);
            } finally {
                ActionMode actionMode = this.mActionMode;
                if (actionMode != null) {
                    actionMode.finish();
                }
                this.mDecor = null;
                this.mWindow.closeAllPanels();
                onStop();
                this.mShowing = false;
                sendDismissMessage();
                MemoryLeakMonitorManager.watchMemoryLeak(this);
            }
        }
    }

    private void sendDismissMessage() {
        Message message = this.mDismissMessage;
        if (message != null) {
            Message.obtain(message).sendToTarget();
        }
    }

    private void sendShowMessage() {
        Message message = this.mShowMessage;
        if (message != null) {
            Message.obtain(message).sendToTarget();
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnCreate(Bundle savedInstanceState) {
        if (!this.mCreated) {
            onCreate(savedInstanceState);
            this.mCreated = true;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        ActionBar actionBar = this.mActionBar;
        if (actionBar != null) {
            actionBar.setShowHideAnimationEnabled(true);
        }
        registerOrientationHandler();
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        ActionBar actionBar = this.mActionBar;
        if (actionBar != null) {
            actionBar.setShowHideAnimationEnabled(false);
        }
        unregisterOrientationHandler();
    }

    public Bundle onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(DIALOG_SHOWING_TAG, this.mShowing);
        if (this.mCreated) {
            bundle.putBundle(DIALOG_HIERARCHY_TAG, this.mWindow.saveHierarchyState());
        }
        return bundle;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Bundle dialogHierarchyState = savedInstanceState.getBundle(DIALOG_HIERARCHY_TAG);
        if (dialogHierarchyState != null) {
            dispatchOnCreate(savedInstanceState);
            this.mWindow.restoreHierarchyState(dialogHierarchyState);
            if (savedInstanceState.getBoolean(DIALOG_SHOWING_TAG)) {
                show();
            }
        }
    }

    public Window getWindow() {
        return this.mWindow;
    }

    public View getCurrentFocus() {
        Window window = this.mWindow;
        if (window != null) {
            return window.getCurrentFocus();
        }
        return null;
    }

    public <T extends View> T findViewById(int id) {
        return (T) this.mWindow.findViewById(id);
    }

    public final <T extends View> T requireViewById(int id) {
        T view = (T) findViewById(id);
        if (view != null) {
            return view;
        }
        throw new IllegalArgumentException("ID does not reference a View inside this Dialog");
    }

    public void setContentView(int layoutResID) {
        this.mWindow.setContentView(layoutResID);
    }

    public void setContentView(View view) {
        this.mWindow.setContentView(view);
    }

    public void setContentView(View view, ViewGroup.LayoutParams params) {
        this.mWindow.setContentView(view, params);
    }

    public void addContentView(View view, ViewGroup.LayoutParams params) {
        this.mWindow.addContentView(view, params);
    }

    public void setTitle(CharSequence title) {
        this.mWindow.setTitle(title);
        this.mWindow.getAttributes().setTitle(title);
    }

    public void setTitle(int titleId) {
        setTitle(this.mContext.getText(titleId));
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4 && keyCode != 111) {
            return false;
        }
        event.startTracking();
        return true;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode != 4 && keyCode != 111) || !event.isTracking() || event.isCanceled()) {
            return false;
        }
        onBackPressed();
        return true;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return false;
    }

    public void onBackPressed() {
        if (this.mCancelable) {
            cancel();
        }
    }

    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mCancelable || !this.mShowing || !this.mWindow.shouldCloseOnTouch(this.mContext, event) || hasButtons()) {
            return false;
        }
        cancel();
        return true;
    }

    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return false;
    }

    @Override // android.view.Window.Callback
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        View view = this.mDecor;
        if (view != null) {
            this.mWindowManager.updateViewLayout(view, params);
        }
    }

    @Override // android.view.Window.Callback
    public void onContentChanged() {
    }

    @Override // android.view.Window.Callback
    public void onWindowFocusChanged(boolean hasFocus) {
    }

    @Override // android.view.Window.Callback
    public void onAttachedToWindow() {
    }

    @Override // android.view.Window.Callback
    public void onDetachedFromWindow() {
    }

    @Override // android.view.Window.OnWindowDismissedCallback
    public void onWindowDismissed(boolean finishTask, boolean suppressWindowTransition) {
        dismiss();
    }

    @Override // android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent event) {
        DialogInterface.OnKeyListener onKeyListener = this.mOnKeyListener;
        if ((onKeyListener != null && onKeyListener.onKey(this, event.getKeyCode(), event)) || this.mWindow.superDispatchKeyEvent(event)) {
            return true;
        }
        View view = this.mDecor;
        return event.dispatch(this, view != null ? view.getKeyDispatcherState() : null, this);
    }

    @Override // android.view.Window.Callback
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        if (this.mWindow.superDispatchKeyShortcutEvent(event)) {
            return true;
        }
        return onKeyShortcut(event.getKeyCode(), event);
    }

    @Override // android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mWindow.superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    @Override // android.view.Window.Callback
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        if (this.mWindow.superDispatchTrackballEvent(ev)) {
            return true;
        }
        return onTrackballEvent(ev);
    }

    @Override // android.view.Window.Callback
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        if (this.mWindow.superDispatchGenericMotionEvent(ev)) {
            return true;
        }
        return onGenericMotionEvent(ev);
    }

    @Override // android.view.Window.Callback
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(getClass().getName());
        event.setPackageName(this.mContext.getPackageName());
        ViewGroup.LayoutParams params = getWindow().getAttributes();
        event.setFullScreen(params.width == -1 && params.height == -1);
        return false;
    }

    @Override // android.view.Window.Callback
    public View onCreatePanelView(int featureId) {
        return null;
    }

    @Override // android.view.Window.Callback
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == 0) {
            return onCreateOptionsMenu(menu);
        }
        return false;
    }

    @Override // android.view.Window.Callback
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId != 0) {
            return true;
        }
        if (!onPrepareOptionsMenu(menu) || !menu.hasVisibleItems()) {
            return false;
        }
        return true;
    }

    @Override // android.view.Window.Callback
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == 8) {
            this.mActionBar.dispatchMenuVisibilityChanged(true);
        }
        return true;
    }

    @Override // android.view.Window.Callback
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return false;
    }

    @Override // android.view.Window.Callback
    public void onPanelClosed(int featureId, Menu menu) {
        if (featureId == 8) {
            this.mActionBar.dispatchMenuVisibilityChanged(false);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public void onOptionsMenuClosed(Menu menu) {
    }

    public void openOptionsMenu() {
        if (this.mWindow.hasFeature(0)) {
            this.mWindow.openPanel(0, null);
        }
    }

    public void closeOptionsMenu() {
        if (this.mWindow.hasFeature(0)) {
            this.mWindow.closePanel(0);
        }
    }

    public void invalidateOptionsMenu() {
        if (this.mWindow.hasFeature(0)) {
            this.mWindow.invalidatePanelMenu(0);
        }
    }

    @Override // android.view.View.OnCreateContextMenuListener
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    }

    public void registerForContextMenu(View view) {
        view.setOnCreateContextMenuListener(this);
    }

    public void unregisterForContextMenu(View view) {
        view.setOnCreateContextMenuListener(null);
    }

    public void openContextMenu(View view) {
        view.showContextMenu();
    }

    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }

    public void onContextMenuClosed(Menu menu) {
    }

    @Override // android.view.Window.Callback
    public boolean onSearchRequested(SearchEvent searchEvent) {
        this.mSearchEvent = searchEvent;
        return onSearchRequested();
    }

    @Override // android.view.Window.Callback
    public boolean onSearchRequested() {
        SearchManager searchManager = (SearchManager) this.mContext.getSystemService("search");
        ComponentName appName = getAssociatedActivity();
        if (appName == null || searchManager.getSearchableInfo(appName) == null) {
            return false;
        }
        searchManager.startSearch(null, false, appName, null, false);
        dismiss();
        return true;
    }

    public final SearchEvent getSearchEvent() {
        return this.mSearchEvent;
    }

    @Override // android.view.Window.Callback
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        ActionBar actionBar = this.mActionBar;
        if (actionBar == null || this.mActionModeTypeStarting != 0) {
            return null;
        }
        return actionBar.startActionMode(callback);
    }

    /* JADX INFO: finally extract failed */
    @Override // android.view.Window.Callback
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        try {
            this.mActionModeTypeStarting = type;
            ActionMode onWindowStartingActionMode = onWindowStartingActionMode(callback);
            this.mActionModeTypeStarting = 0;
            return onWindowStartingActionMode;
        } catch (Throwable th) {
            this.mActionModeTypeStarting = 0;
            throw th;
        }
    }

    @Override // android.view.Window.Callback
    public void onActionModeStarted(ActionMode mode) {
        this.mActionMode = mode;
    }

    @Override // android.view.Window.Callback
    public void onActionModeFinished(ActionMode mode) {
        if (mode == this.mActionMode) {
            this.mActionMode = null;
        }
    }

    private ComponentName getAssociatedActivity() {
        Activity activity = this.mOwnerActivity;
        Context context = getContext();
        while (true) {
            Context context2 = null;
            if (activity != null || context == null) {
                break;
            } else if (context instanceof Activity) {
                activity = (Activity) context;
            } else {
                if (context instanceof ContextWrapper) {
                    context2 = ((ContextWrapper) context).getBaseContext();
                }
                context = context2;
            }
        }
        if (activity == null) {
            return null;
        }
        return activity.getComponentName();
    }

    public void takeKeyEvents(boolean get) {
        this.mWindow.takeKeyEvents(get);
    }

    public final boolean requestWindowFeature(int featureId) {
        return getWindow().requestFeature(featureId);
    }

    public final void setFeatureDrawableResource(int featureId, int resId) {
        getWindow().setFeatureDrawableResource(featureId, resId);
    }

    public final void setFeatureDrawableUri(int featureId, Uri uri) {
        getWindow().setFeatureDrawableUri(featureId, uri);
    }

    public final void setFeatureDrawable(int featureId, Drawable drawable) {
        getWindow().setFeatureDrawable(featureId, drawable);
    }

    public final void setFeatureDrawableAlpha(int featureId, int alpha) {
        getWindow().setFeatureDrawableAlpha(featureId, alpha);
    }

    public LayoutInflater getLayoutInflater() {
        return getWindow().getLayoutInflater();
    }

    public void setCancelable(boolean flag) {
        this.mCancelable = flag;
        updateWindowForCancelable();
    }

    public void setCanceledOnTouchOutside(boolean cancel) {
        if (cancel && !this.mCancelable) {
            this.mCancelable = true;
            updateWindowForCancelable();
        }
        this.mWindow.setCloseOnTouchOutside(cancel);
    }

    @Override // android.content.DialogInterface
    public void cancel() {
        Message message;
        if (!this.mCanceled && (message = this.mCancelMessage) != null) {
            this.mCanceled = true;
            Message.obtain(message).sendToTarget();
        }
        dismiss();
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        if (this.mCancelAndDismissTaken != null) {
            throw new IllegalStateException("OnCancelListener is already taken by " + this.mCancelAndDismissTaken + " and can not be replaced.");
        } else if (listener != null) {
            this.mCancelMessage = this.mListenersHandler.obtainMessage(68, listener);
        } else {
            this.mCancelMessage = null;
        }
    }

    public void setCancelMessage(Message msg) {
        this.mCancelMessage = msg;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        if (this.mCancelAndDismissTaken != null) {
            throw new IllegalStateException("OnDismissListener is already taken by " + this.mCancelAndDismissTaken + " and can not be replaced.");
        } else if (listener != null) {
            this.mDismissMessage = this.mListenersHandler.obtainMessage(67, listener);
        } else {
            this.mDismissMessage = null;
        }
    }

    public void setOnShowListener(DialogInterface.OnShowListener listener) {
        if (listener != null) {
            this.mShowMessage = this.mListenersHandler.obtainMessage(69, listener);
        } else {
            this.mShowMessage = null;
        }
    }

    public void setDismissMessage(Message msg) {
        this.mDismissMessage = msg;
    }

    public boolean takeCancelAndDismissListeners(String msg, DialogInterface.OnCancelListener cancel, DialogInterface.OnDismissListener dismiss) {
        if (this.mCancelAndDismissTaken != null) {
            this.mCancelAndDismissTaken = null;
        } else if (!(this.mCancelMessage == null && this.mDismissMessage == null)) {
            return false;
        }
        setOnCancelListener(cancel);
        setOnDismissListener(dismiss);
        this.mCancelAndDismissTaken = msg;
        return true;
    }

    public final void setVolumeControlStream(int streamType) {
        getWindow().setVolumeControlStream(streamType);
    }

    public final int getVolumeControlStream() {
        return getWindow().getVolumeControlStream();
    }

    public void setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
        this.mOnKeyListener = onKeyListener;
    }

    private static final class ListenersHandler extends Handler {
        private final WeakReference<DialogInterface> mDialog;

        public ListenersHandler(Dialog dialog) {
            this.mDialog = new WeakReference<>(dialog);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 67:
                    ((DialogInterface.OnDismissListener) msg.obj).onDismiss(this.mDialog.get());
                    return;
                case 68:
                    ((DialogInterface.OnCancelListener) msg.obj).onCancel(this.mDialog.get());
                    return;
                case 69:
                    ((DialogInterface.OnShowListener) msg.obj).onShow(this.mDialog.get());
                    return;
                default:
                    return;
            }
        }
    }

    private void updateWindowForCancelable() {
        this.mWindow.setCloseOnSwipeEnabled(this.mCancelable);
    }

    private boolean hasButtons() {
        return HwWidgetFactory.isHwTheme(this.mContext) && containsButtons();
    }

    private void registerOrientationHandler() {
        if (HwWidgetFactory.isHwTheme(this.mContext) && (this instanceof AlertDialog)) {
            unregisterOrientationHandler();
            this.mWindow.getDecorView().addOnLayoutChangeListener(this.mOrientationChangeHandler);
        }
    }

    private void unregisterOrientationHandler() {
        if (HwWidgetFactory.isHwTheme(this.mContext) && (this instanceof AlertDialog)) {
            this.mWindow.getDecorView().removeOnLayoutChangeListener(this.mOrientationChangeHandler);
        }
    }

    /* access modifiers changed from: protected */
    public boolean containsButtons() {
        return hasButton(-1) || hasButton(-2) || hasButton(-3);
    }

    private boolean hasButton(int whichButton) {
        Button button;
        if (!(this instanceof AlertDialog) || (button = ((AlertDialog) this).getButton(whichButton)) == null || button.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    private ComponentName getForbidDialogComponentName(Context context) {
        Activity activity = this.mOwnerActivity;
        int curCount = 0;
        while (true) {
            Context context2 = null;
            if (activity != null || context == null || curCount >= 16) {
                break;
            }
            curCount++;
            if (context instanceof Activity) {
                activity = (Activity) context;
            } else {
                if (context instanceof ContextWrapper) {
                    context2 = ((ContextWrapper) context).getBaseContext();
                }
                context = context2;
            }
        }
        if (activity == null) {
            return null;
        }
        return activity.getComponentName();
    }
}
