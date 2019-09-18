package android.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.CoordinationModeUtils;
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
import com.android.internal.policy.HwPolicyFactory;
import java.lang.annotation.RCUnownedThisRef;
import java.lang.ref.WeakReference;

public class Dialog implements DialogInterface, Window.Callback, KeyEvent.Callback, View.OnCreateContextMenuListener, Window.OnWindowDismissedCallback {
    private static final int CANCEL = 68;
    private static final String DIALOG_HIERARCHY_TAG = "android:dialogHierarchy";
    private static final String DIALOG_SHOWING_TAG = "android:dialogShowing";
    private static final int DISMISS = 67;
    private static final int SHOW = 69;
    private static final String TAG = "Dialog";
    private static final int Y_OFFSET = 20;
    private ActionBar mActionBar;
    private ActionMode mActionMode;
    private int mActionModeTypeStarting;
    private String mCancelAndDismissTaken;
    private Message mCancelMessage;
    protected boolean mCancelable;
    private boolean mCanceled;
    final Context mContext;
    private boolean mCreated;
    View mDecor;
    private Message mDismissMessage;
    private int mFinalHeight;
    private int mFinalWidth;
    private final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mIsFirstShown;
    private final Handler mListenersHandler;
    private DialogInterface.OnKeyListener mOnKeyListener;
    private final View.OnLayoutChangeListener mOrientationChangeHandler;
    private Activity mOwnerActivity;
    private SearchEvent mSearchEvent;
    private Message mShowMessage;
    private boolean mShowing;
    final Window mWindow;
    private final WindowManager mWindowManager;
    private int mYOffset;

    private static final class ListenersHandler extends Handler {
        private final WeakReference<DialogInterface> mDialog;

        public ListenersHandler(Dialog dialog) {
            this.mDialog = new WeakReference<>(dialog);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 67:
                    ((DialogInterface.OnDismissListener) msg.obj).onDismiss((DialogInterface) this.mDialog.get());
                    return;
                case 68:
                    ((DialogInterface.OnCancelListener) msg.obj).onCancel((DialogInterface) this.mDialog.get());
                    return;
                case 69:
                    ((DialogInterface.OnShowListener) msg.obj).onShow((DialogInterface) this.mDialog.get());
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX WARNING: type inference failed for: r1v3, types: [android.view.ViewGroup$LayoutParams] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void layoutWhenGravityChanged() {
        try {
            WindowManager.LayoutParams l = this.mWindow.getAttributes();
            ? layoutParams = this.mDecor.getLayoutParams();
            if (layoutParams instanceof WindowManager.LayoutParams) {
                l = layoutParams;
            } else {
                Log.e(TAG, "Params must be WindowManager.LayoutParams.");
            }
            if (2011 != l.type) {
                if (ActivityManagerNative.getDefault().isInMultiWindowMode(l.token)) {
                    l.gravity = 17;
                } else if (!this.mContext.getResources().getBoolean(34537472)) {
                    if (this.mYOffset == 0) {
                        this.mYOffset = getYOffset();
                    }
                    int formalYOffset = l.y;
                    int i = 0;
                    boolean isFormalSet = (formalYOffset == this.mYOffset || formalYOffset == 0) ? false : true;
                    if (1 == this.mContext.getResources().getConfiguration().orientation) {
                        l.gravity = 80;
                        l.y = isFormalSet ? formalYOffset : this.mYOffset;
                    } else {
                        l.gravity = 17;
                        if (isFormalSet) {
                            i = formalYOffset;
                        }
                        l.y = i;
                    }
                    updateLayoutInCoordinationMode();
                    if (this.mFinalWidth != 0) {
                        l.width = this.mFinalWidth;
                        l.height = -2;
                    } else if (this.mFinalHeight != 0) {
                        l.gravity = 80;
                        l.height = this.mFinalHeight;
                        l.width = -2;
                    }
                } else {
                    l.gravity = 17;
                }
                this.mWindow.setAttributes(l);
            }
            onWindowAttributesChanged(l);
        } catch (RemoteException e) {
            Log.e(TAG, "Dialog: RemoteException = " + e);
        } catch (Exception e2) {
            Log.e(TAG, "Dialog: layoutWhenGravityChanged cause Exception = " + e2);
        }
    }

    private void updateLayoutInCoordinationMode() {
        if (CoordinationModeUtils.isFoldable()) {
            this.mFinalWidth = 0;
            this.mFinalHeight = 0;
            int createmode = Settings.Global.getInt(this.mContext.getContentResolver(), "coordination_create_mode", 0);
            if (1 == this.mContext.getResources().getConfiguration().orientation) {
                if (createmode == 3) {
                    this.mFinalWidth = CoordinationModeUtils.getFoldScreenSubWidth();
                } else if (createmode == 4) {
                    this.mFinalWidth = CoordinationModeUtils.getFoldScreenMainWidth();
                }
            } else if (2 == this.mContext.getResources().getConfiguration().orientation) {
                if (createmode == 3) {
                    this.mFinalHeight = CoordinationModeUtils.getFoldScreenSubWidth();
                } else if (createmode == 4) {
                    this.mFinalHeight = CoordinationModeUtils.getFoldScreenMainWidth();
                }
            }
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
        this.mIsFirstShown = true;
        this.mActionModeTypeStarting = 0;
        this.mFinalWidth = 0;
        this.mFinalHeight = 0;
        this.mOrientationChangeHandler = new View.OnLayoutChangeListener() {
            private final Rect mNewRect = new Rect();
            private final Rect mOldRect = new Rect();

            @RCUnownedThisRef
            public void onLayoutChange(View view, int newLeft, int newRight, int newTop, int newBottom, int oldLeft, int oldRight, int oldTop, int oldBottom) {
                this.mNewRect.set(newLeft, newRight, newTop, newBottom);
                this.mOldRect.set(oldLeft, oldRight, oldTop, oldBottom);
                if (!Dialog.this.isShowing()) {
                    return;
                }
                if (!this.mNewRect.equals(this.mOldRect) || Dialog.this.mIsFirstShown) {
                    Dialog.this.layoutWhenGravityChanged();
                    boolean unused = Dialog.this.mIsFirstShown = false;
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
        this.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Window w = HwPolicyFactory.getHwPhoneWindow(this.mContext);
        this.mWindow = w;
        w.setCallback(this);
        w.setOnWindowDismissedCallback(this);
        w.setOnWindowSwipeDismissedCallback(new Window.OnWindowSwipeDismissedCallback() {
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
    }

    private int getYOffset() {
        return (int) (20.0f * this.mContext.getResources().getDisplayMetrics().scaledDensity);
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
        return this.mShowing;
    }

    public void create() {
        if (!this.mCreated) {
            dispatchOnCreate(null);
        }
    }

    public void show() {
        int i = 0;
        if (this.mShowing) {
            if (this.mDecor != null) {
                if (this.mWindow.hasFeature(8)) {
                    this.mWindow.invalidatePanelMenu(8);
                }
                this.mDecor.setVisibility(0);
            }
            return;
        }
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
        if (HwWidgetFactory.isHwTheme(this.mContext) && (this instanceof AlertDialog) && 2011 != l.type) {
            if (!this.mContext.getResources().getBoolean(34537472)) {
                int formalYOffset = l.y;
                if (this.mYOffset == 0) {
                    this.mYOffset = getYOffset();
                }
                boolean isFormalSet = (formalYOffset == this.mYOffset || formalYOffset == 0) ? false : true;
                if (1 == this.mContext.getResources().getConfiguration().orientation) {
                    l.gravity = 80;
                    l.y = isFormalSet ? formalYOffset : this.mYOffset;
                } else {
                    l.gravity = 17;
                    if (isFormalSet) {
                        i = formalYOffset;
                    }
                    l.y = i;
                }
            } else {
                l.gravity = 17;
            }
        }
        updateLayoutInCoordinationMode();
        if (this.mFinalWidth != 0) {
            l.width = this.mFinalWidth;
        } else if (this.mFinalHeight != 0) {
            l.gravity = 80;
            l.height = this.mFinalHeight;
        }
        boolean restoreSoftInputMode = false;
        if ((l.softInputMode & 256) == 0) {
            l.softInputMode |= 256;
            restoreSoftInputMode = true;
        }
        l.privateFlags |= 1024;
        this.mWindowManager.addView(this.mDecor, l);
        if (restoreSoftInputMode) {
            l.softInputMode &= -257;
        }
        this.mShowing = true;
        sendShowMessage();
    }

    public void hide() {
        if (this.mDecor != null) {
            this.mDecor.setVisibility(8);
        }
    }

    public void dismiss() {
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            dismissDialog();
        } else {
            this.mHandler.post(new Runnable() {
                public final void run() {
                    Dialog.this.dismissDialog();
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void dismissDialog() {
        if (this.mDecor != null && this.mShowing) {
            if (this.mWindow.isDestroyed()) {
                Log.e(TAG, "Tried to dismissDialog() but the Dialog's window was already destroyed!");
                return;
            }
            try {
                this.mWindowManager.removeViewImmediate(this.mDecor);
            } finally {
                if (this.mActionMode != null) {
                    this.mActionMode.finish();
                }
                this.mDecor = null;
                this.mIsFirstShown = true;
                this.mWindow.closeAllPanels();
                onStop();
                this.mShowing = false;
                sendDismissMessage();
            }
        }
    }

    private void sendDismissMessage() {
        if (this.mDismissMessage != null) {
            Message.obtain(this.mDismissMessage).sendToTarget();
        }
    }

    private void sendShowMessage() {
        if (this.mShowMessage != null) {
            Message.obtain(this.mShowMessage).sendToTarget();
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
        if (this.mActionBar != null) {
            this.mActionBar.setShowHideAnimationEnabled(true);
        }
        registerOrientationHandler();
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        if (this.mActionBar != null) {
            this.mActionBar.setShowHideAnimationEnabled(false);
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
        if (this.mWindow != null) {
            return this.mWindow.getCurrentFocus();
        }
        return null;
    }

    public <T extends View> T findViewById(int id) {
        return this.mWindow.findViewById(id);
    }

    public final <T extends View> T requireViewById(int id) {
        T view = findViewById(id);
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4 && keyCode != 111) {
            return false;
        }
        event.startTracking();
        return true;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode != 4 && keyCode != 111) || !event.isTracking() || event.isCanceled()) {
            return false;
        }
        onBackPressed();
        return true;
    }

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

    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        if (this.mDecor != null) {
            this.mWindowManager.updateViewLayout(this.mDecor, params);
        }
    }

    public void onContentChanged() {
    }

    public void onWindowFocusChanged(boolean hasFocus) {
    }

    public void onAttachedToWindow() {
    }

    public void onDetachedFromWindow() {
    }

    public void onWindowDismissed(boolean finishTask, boolean suppressWindowTransition) {
        dismiss();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((this.mOnKeyListener != null && this.mOnKeyListener.onKey(this, event.getKeyCode(), event)) || this.mWindow.superDispatchKeyEvent(event)) {
            return true;
        }
        return event.dispatch(this, this.mDecor != null ? this.mDecor.getKeyDispatcherState() : null, this);
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        if (this.mWindow.superDispatchKeyShortcutEvent(event)) {
            return true;
        }
        return onKeyShortcut(event.getKeyCode(), event);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mWindow.superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean dispatchTrackballEvent(MotionEvent ev) {
        if (this.mWindow.superDispatchTrackballEvent(ev)) {
            return true;
        }
        return onTrackballEvent(ev);
    }

    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        if (this.mWindow.superDispatchGenericMotionEvent(ev)) {
            return true;
        }
        return onGenericMotionEvent(ev);
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(getClass().getName());
        event.setPackageName(this.mContext.getPackageName());
        ViewGroup.LayoutParams params = getWindow().getAttributes();
        event.setFullScreen(params.width == -1 && params.height == -1);
        return false;
    }

    public View onCreatePanelView(int featureId) {
        return null;
    }

    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == 0) {
            return onCreateOptionsMenu(menu);
        }
        return false;
    }

    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        boolean z = true;
        if (featureId != 0 || menu == null) {
            return true;
        }
        if (!onPrepareOptionsMenu(menu) || !menu.hasVisibleItems()) {
            z = false;
        }
        return z;
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == 8) {
            this.mActionBar.dispatchMenuVisibilityChanged(true);
        }
        return true;
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return false;
    }

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

    public boolean onSearchRequested(SearchEvent searchEvent) {
        this.mSearchEvent = searchEvent;
        return onSearchRequested();
    }

    public boolean onSearchRequested() {
        SearchManager searchManager = (SearchManager) this.mContext.getSystemService(Context.SEARCH_SERVICE);
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

    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        if (this.mActionBar == null || this.mActionModeTypeStarting != 0) {
            return null;
        }
        return this.mActionBar.startActionMode(callback);
    }

    /* JADX INFO: finally extract failed */
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

    public void onActionModeStarted(ActionMode mode) {
        this.mActionMode = mode;
    }

    public void onActionModeFinished(ActionMode mode) {
        if (mode == this.mActionMode) {
            this.mActionMode = null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:19:? A[RETURN, SYNTHETIC] */
    private ComponentName getAssociatedActivity() {
        Activity activity = this.mOwnerActivity;
        Context context = getContext();
        while (true) {
            Context context2 = null;
            if (activity != null || context == null) {
                if (activity != null) {
                    return null;
                }
                return activity.getComponentName();
            } else if (context instanceof Activity) {
                activity = context;
            } else {
                if (context instanceof ContextWrapper) {
                    context2 = ((ContextWrapper) context).getBaseContext();
                }
                context = context2;
            }
        }
        if (activity != null) {
        }
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

    public void cancel() {
        if (!this.mCanceled && this.mCancelMessage != null) {
            this.mCanceled = true;
            Message.obtain(this.mCancelMessage).sendToTarget();
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
        boolean z = false;
        if (!(this instanceof AlertDialog)) {
            return false;
        }
        Button button = ((AlertDialog) this).getButton(whichButton);
        if (button != null && button.getVisibility() == 0) {
            z = true;
        }
        return z;
    }
}
