package android.app;

import android.R;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnShowListener;
import android.content.pm.ApplicationInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.hwcontrol.HwWidgetFactory.HwDialogStub;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.BrowserContract.Searches;
import android.provider.DocumentsContract.Document;
import android.renderscript.Mesh.TriangleMeshBuilder;
import android.rms.AppAssociate;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.Window.Callback;
import android.view.Window.OnWindowDismissedCallback;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.policy.DecorView;
import com.android.internal.policy.HwPolicyFactory;
import java.lang.ref.WeakReference;

public class Dialog implements DialogInterface, Callback, KeyEvent.Callback, OnCreateContextMenuListener, OnWindowDismissedCallback {
    private static final int CANCEL = 68;
    private static final String DIALOG_HIERARCHY_TAG = "android:dialogHierarchy";
    private static final String DIALOG_SHOWING_TAG = "android:dialogShowing";
    private static final int DISMISS = 67;
    private static final int SHOW = 69;
    private static final String TAG = "Dialog";
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
    private final Runnable mDismissAction;
    private Message mDismissMessage;
    private final Handler mHandler;
    private HwDialogStub mHwDialogStub;
    private boolean mIsFirstShown;
    private final Handler mListenersHandler;
    private OnKeyListener mOnKeyListener;
    private final OnLayoutChangeListener mOrientationChangeHandler;
    private Activity mOwnerActivity;
    private SearchEvent mSearchEvent;
    private Message mShowMessage;
    private boolean mShowing;
    final Window mWindow;
    private final WindowManager mWindowManager;

    final /* synthetic */ class -void__init__android_content_Context_context_int_themeResId_boolean_createContextThemeWrapper_LambdaImpl0 implements Runnable {
        private /* synthetic */ Dialog val$this;

        public /* synthetic */ -void__init__android_content_Context_context_int_themeResId_boolean_createContextThemeWrapper_LambdaImpl0(Dialog dialog) {
            this.val$this = dialog;
        }

        public void run() {
            this.val$this.-android_app_Dialog-mthref-0();
        }
    }

    private static final class ListenersHandler extends Handler {
        private final WeakReference<DialogInterface> mDialog;

        public ListenersHandler(Dialog dialog) {
            this.mDialog = new WeakReference(dialog);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Dialog.DISMISS /*67*/:
                    ((OnDismissListener) msg.obj).onDismiss((DialogInterface) this.mDialog.get());
                case Dialog.CANCEL /*68*/:
                    ((OnCancelListener) msg.obj).onCancel((DialogInterface) this.mDialog.get());
                case Dialog.SHOW /*69*/:
                    ((OnShowListener) msg.obj).onShow((DialogInterface) this.mDialog.get());
                default:
            }
        }
    }

    private void layoutWhenGravityChanged() {
        int i = 17;
        try {
            LayoutParams l = this.mWindow.getAttributes();
            ViewGroup.LayoutParams vl = this.mDecor.getLayoutParams();
            if (vl instanceof LayoutParams) {
                l = (LayoutParams) vl;
            } else {
                Log.e(TAG, "Params must be WindowManager.LayoutParams.");
            }
            if (2011 != l.type) {
                if (ActivityManagerNative.getDefault().isInMultiWindowMode(l.token)) {
                    l.gravity = 17;
                } else if (this.mContext.getResources().getBoolean(34406403)) {
                    l.gravity = 17;
                } else {
                    if (1 == this.mContext.getResources().getConfiguration().orientation) {
                        i = 80;
                    }
                    l.gravity = i;
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

    /* synthetic */ void -android_app_Dialog-mthref-0() {
        dismissDialog();
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
        this.mOrientationChangeHandler = new OnLayoutChangeListener() {
            private final Rect mNewRect;
            private final Rect mOldRect;

            {
                this.mNewRect = new Rect();
                this.mOldRect = new Rect();
            }

            public void onLayoutChange(View view, int newLeft, int newRight, int newTop, int newBottom, int oldLeft, int oldRight, int oldTop, int oldBottom) {
                this.mNewRect.set(newLeft, newRight, newTop, newBottom);
                this.mOldRect.set(oldLeft, oldRight, oldTop, oldBottom);
                if (!Dialog.this.isShowing()) {
                    return;
                }
                if (!this.mNewRect.equals(this.mOldRect) || Dialog.this.mIsFirstShown) {
                    Dialog.this.layoutWhenGravityChanged();
                    Dialog.this.mIsFirstShown = false;
                }
            }
        };
        this.mDismissAction = new -void__init__android_content_Context_context_int_themeResId_boolean_createContextThemeWrapper_LambdaImpl0();
        if (createContextThemeWrapper) {
            if (themeResId == 0) {
                TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(R.attr.dialogTheme, outValue, true);
                themeResId = outValue.resourceId;
            }
            this.mContext = new ContextThemeWrapper(context, themeResId);
        } else {
            this.mContext = context;
        }
        this.mWindowManager = (WindowManager) context.getSystemService(AppAssociate.ASSOC_WINDOW);
        Window w = HwPolicyFactory.getHwPhoneWindow(this.mContext);
        this.mWindow = w;
        w.setCallback(this);
        w.setOnWindowDismissedCallback(this);
        w.setWindowManager(this.mWindowManager, null, null);
        w.setGravity(17);
        this.mListenersHandler = new ListenersHandler(this);
        this.mHwDialogStub = HwWidgetFactory.getHwDialogStub(this.mContext, this.mWindow, this, null);
    }

    @Deprecated
    protected Dialog(Context context, boolean cancelable, Message cancelCallback) {
        this(context);
        this.mCancelable = cancelable;
        this.mCancelMessage = cancelCallback;
    }

    protected Dialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        this(context);
        this.mCancelable = cancelable;
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
        int i = 17;
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
        }
        onStart();
        this.mDecor = this.mWindow.getDecorView();
        if (this.mActionBar == null && this.mWindow.hasFeature(8)) {
            ApplicationInfo info = this.mContext.getApplicationInfo();
            this.mWindow.setDefaultIcon(info.icon);
            this.mWindow.setDefaultLogo(info.logo);
            this.mActionBar = HwWidgetFactory.getHuaweiActionBarImpl(this);
        }
        LayoutParams l = this.mWindow.getAttributes();
        if (HwWidgetFactory.isHwTheme(this.mContext) && (this instanceof AlertDialog) && 2011 != l.type) {
            if (this.mContext.getResources().getBoolean(34406403)) {
                l.gravity = 17;
            } else {
                if (1 == this.mContext.getResources().getConfiguration().orientation) {
                    i = 80;
                }
                l.gravity = i;
            }
        }
        if (this.mDecor instanceof DecorView) {
            ((DecorView) this.mDecor).updateAvailableWidth();
        }
        if ((l.softInputMode & TriangleMeshBuilder.TEXTURE_0) == 0) {
            LayoutParams nl = new LayoutParams();
            nl.copyFrom(l);
            nl.softInputMode |= TriangleMeshBuilder.TEXTURE_0;
            l = nl;
        }
        l.privateFlags |= Document.FLAG_SUPPORTS_REMOVE;
        this.mWindowManager.addView(this.mDecor, l);
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
            this.mHandler.post(this.mDismissAction);
        }
    }

    void dismissDialog() {
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

    void dispatchOnCreate(Bundle savedInstanceState) {
        if (!this.mCreated) {
            onCreate(savedInstanceState);
            this.mCreated = true;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
    }

    protected void onStart() {
        if (this.mActionBar != null) {
            this.mActionBar.setShowHideAnimationEnabled(true);
        }
        registerOrientationHandler();
    }

    protected void onStop() {
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
        return this.mWindow != null ? this.mWindow.getCurrentFocus() : null;
    }

    public View findViewById(int id) {
        return this.mWindow.findViewById(id);
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
        if (keyCode != 4) {
            return false;
        }
        event.startTracking();
        return true;
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != 4 || !event.isTracking() || event.isCanceled()) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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

    public void onWindowAttributesChanged(LayoutParams params) {
        if (this.mDecor != null) {
            this.mWindowManager.updateViewLayout(this.mDecor, params);
        }
    }

    public void onContentChanged() {
    }

    public void onWindowFocusChanged(boolean hasFocus) {
    }

    public void onAttachedToWindow() {
        if (hasAnimatorMask()) {
            this.mHwDialogStub.showDialogFactory();
        }
    }

    public void onDetachedFromWindow() {
        if (hasAnimatorMask()) {
            this.mHwDialogStub.dismissDialogFactory();
        }
    }

    public void onWindowDismissed(boolean finishTask) {
        dismiss();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        DispatcherState dispatcherState = null;
        if ((this.mOnKeyListener != null && this.mOnKeyListener.onKey(this, event.getKeyCode(), event)) || this.mWindow.superDispatchKeyEvent(event)) {
            return true;
        }
        if (this.mDecor != null) {
            dispatcherState = this.mDecor.getKeyDispatcherState();
        }
        return event.dispatch(this, dispatcherState, this);
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
        boolean isFullScreen = params.width == -1 ? params.height == -1 : false;
        event.setFullScreen(isFullScreen);
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
        boolean z = false;
        if (featureId != 0 || menu == null) {
            return true;
        }
        if (onPrepareOptionsMenu(menu)) {
            z = menu.hasVisibleItems();
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

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
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
        SearchManager searchManager = (SearchManager) this.mContext.getSystemService(Searches.SEARCH);
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

    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        try {
            this.mActionModeTypeStarting = type;
            ActionMode onWindowStartingActionMode = onWindowStartingActionMode(callback);
            return onWindowStartingActionMode;
        } finally {
            this.mActionModeTypeStarting = 0;
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

    private ComponentName getAssociatedActivity() {
        Activity activity = this.mOwnerActivity;
        Context context = getContext();
        while (activity == null && context != null) {
            if (context instanceof Activity) {
                activity = (Activity) context;
            } else if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            } else {
                context = null;
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
    }

    public void setCanceledOnTouchOutside(boolean cancel) {
        if (cancel && !this.mCancelable) {
            this.mCancelable = true;
        }
        this.mWindow.setCloseOnTouchOutside(cancel);
    }

    public void cancel() {
        if (!(this.mCanceled || this.mCancelMessage == null)) {
            this.mCanceled = true;
            Message.obtain(this.mCancelMessage).sendToTarget();
        }
        dismiss();
    }

    public void setOnCancelListener(OnCancelListener listener) {
        if (this.mCancelAndDismissTaken != null) {
            throw new IllegalStateException("OnCancelListener is already taken by " + this.mCancelAndDismissTaken + " and can not be replaced.");
        } else if (listener != null) {
            this.mCancelMessage = this.mListenersHandler.obtainMessage(CANCEL, listener);
        } else {
            this.mCancelMessage = null;
        }
    }

    public void setCancelMessage(Message msg) {
        this.mCancelMessage = msg;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        if (this.mCancelAndDismissTaken != null) {
            throw new IllegalStateException("OnDismissListener is already taken by " + this.mCancelAndDismissTaken + " and can not be replaced.");
        } else if (listener != null) {
            this.mDismissMessage = this.mListenersHandler.obtainMessage(DISMISS, listener);
        } else {
            this.mDismissMessage = null;
        }
    }

    public void setOnShowListener(OnShowListener listener) {
        if (listener != null) {
            this.mShowMessage = this.mListenersHandler.obtainMessage(SHOW, listener);
        } else {
            this.mShowMessage = null;
        }
    }

    public void setDismissMessage(Message msg) {
        this.mDismissMessage = msg;
    }

    public boolean takeCancelAndDismissListeners(String msg, OnCancelListener cancel, OnDismissListener dismiss) {
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

    public void setOnKeyListener(OnKeyListener onKeyListener) {
        this.mOnKeyListener = onKeyListener;
    }

    private boolean hasAnimatorMask() {
        if (this.mHwDialogStub == null || (this.mHwDialogStub.getMask() & 1) == 0) {
            return false;
        }
        return true;
    }

    private boolean hasDismissMask() {
        if (this.mHwDialogStub == null || (this.mHwDialogStub.getMask() & 2) == 0) {
            return false;
        }
        return true;
    }

    private boolean hasButtons() {
        return hasDismissMask() ? this.mHwDialogStub.hasButtons() : false;
    }

    protected boolean containsButtons() {
        return this.mHwDialogStub.hasButtons();
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
}
