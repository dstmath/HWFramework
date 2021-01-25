package android.app;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.IRequestFinishCallback;
import android.app.Instrumentation;
import android.app.PictureInPictureParams;
import android.app.VoiceInteractor;
import android.app.assist.AssistContent;
import android.common.HwFrameworkFactory;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hsm.HwSystemManager;
import android.hwcontrol.HwWidgetFactory;
import android.media.HwMediaMonitorUtils;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.GraphicsEnvironment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.MemoryLeakMonitorManager;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.CalendarContract;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.EventLog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.PrintWriterPrinter;
import android.util.SparseArray;
import android.util.SuperNotCalledException;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.KeyboardShortcutInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.RemoteAnimationDefinition;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityEvent;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillPopupWindow;
import android.view.autofill.Helper;
import android.view.autofill.IAutofillWindowPresenter;
import android.view.contentcapture.ContentCaptureManager;
import android.webkit.WebView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.app.ToolbarActionBar;
import com.android.internal.app.WindowDecorActionBar;
import com.android.internal.policy.DecorView;
import com.android.internal.policy.HwPolicyFactory;
import com.android.internal.policy.PhoneWindow;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.pgmng.log.LogPower;
import dalvik.system.VMRuntime;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class Activity extends ContextThemeWrapper implements LayoutInflater.Factory2, Window.Callback, KeyEvent.Callback, View.OnCreateContextMenuListener, ComponentCallbacks2, Window.OnWindowDismissedCallback, Window.WindowControllerCallback, AutofillManager.AutofillClient, ContentCaptureManager.ContentCaptureClient {
    private static final String AUTOFILL_RESET_NEEDED = "@android:autofillResetNeeded";
    private static final String AUTO_FILL_AUTH_WHO_PREFIX = "@android:autoFillAuth:";
    private static final int CONTENT_CAPTURE_PAUSE = 3;
    private static final int CONTENT_CAPTURE_RESUME = 2;
    private static final int CONTENT_CAPTURE_START = 1;
    private static final int CONTENT_CAPTURE_STOP = 4;
    private static final boolean DEBUG_LIFECYCLE = false;
    public static final int DEFAULT_KEYS_DIALER = 1;
    public static final int DEFAULT_KEYS_DISABLE = 0;
    public static final int DEFAULT_KEYS_SEARCH_GLOBAL = 4;
    public static final int DEFAULT_KEYS_SEARCH_LOCAL = 3;
    public static final int DEFAULT_KEYS_SHORTCUT = 2;
    public static final int DONT_FINISH_TASK_WITH_ACTIVITY = 0;
    public static final int FINISH_TASK_WITH_ACTIVITY = 2;
    public static final int FINISH_TASK_WITH_ROOT_ACTIVITY = 1;
    protected static final int[] FOCUSED_STATE_SET = {16842908};
    @UnsupportedAppUsage
    static final String FRAGMENTS_TAG = "android:fragments";
    private static final String HAS_CURENT_PERMISSIONS_REQUEST_KEY = "android:hasCurrentPermissionsRequest";
    private static final String KEYBOARD_SHORTCUTS_RECEIVER_PKG_NAME = "com.android.systemui";
    private static final String LAST_AUTOFILL_ID = "android:lastAutofillId";
    private static final int LOG_AM_ON_ACTIVITY_RESULT_CALLED = 30062;
    private static final int LOG_AM_ON_CREATE_CALLED = 30057;
    private static final int LOG_AM_ON_DESTROY_CALLED = 30060;
    private static final int LOG_AM_ON_PAUSE_CALLED = 30021;
    private static final int LOG_AM_ON_RESTART_CALLED = 30058;
    private static final int LOG_AM_ON_RESUME_CALLED = 30022;
    private static final int LOG_AM_ON_START_CALLED = 30059;
    private static final int LOG_AM_ON_STOP_CALLED = 30049;
    private static final int LOG_AM_ON_TOP_RESUMED_GAINED_CALLED = 30064;
    private static final int LOG_AM_ON_TOP_RESUMED_LOST_CALLED = 30065;
    private static final String REQUEST_PERMISSIONS_WHO_PREFIX = "@android:requestPermissions:";
    public static final int RESULT_CANCELED = 0;
    public static final int RESULT_FIRST_USER = 1;
    public static final int RESULT_OK = -1;
    private static final String SAVED_DIALOGS_TAG = "android:savedDialogs";
    private static final String SAVED_DIALOG_ARGS_KEY_PREFIX = "android:dialog_args_";
    private static final String SAVED_DIALOG_IDS_KEY = "android:savedDialogIds";
    private static final String SAVED_DIALOG_KEY_PREFIX = "android:dialog_";
    private static final String TAG = "Activity";
    private static final String WINDOW_HIERARCHY_TAG = "android:viewHierarchyState";
    ActionBar mActionBar = null;
    private int mActionModeTypeStarting = 0;
    @UnsupportedAppUsage
    ActivityInfo mActivityInfo;
    private final ArrayList<Application.ActivityLifecycleCallbacks> mActivityLifecycleCallbacks = new ArrayList<>();
    @UnsupportedAppUsage
    ActivityTransitionState mActivityTransitionState = new ActivityTransitionState();
    @UnsupportedAppUsage
    private Application mApplication;
    private IBinder mAssistToken;
    private boolean mAutoFillIgnoreFirstResumePause;
    private boolean mAutoFillResetNeeded;
    private AutofillManager mAutofillManager;
    private AutofillPopupWindow mAutofillPopupWindow;
    @UnsupportedAppUsage
    boolean mCalled;
    private boolean mCanEnterPictureInPicture = false;
    private boolean mChangeCanvasToTranslucent;
    boolean mChangingConfigurations = false;
    @UnsupportedAppUsage
    private ComponentName mComponent;
    @UnsupportedAppUsage
    int mConfigChangeFlags;
    private ContentCaptureManager mContentCaptureManager;
    private ArrayList<View> mCoverViewList = new ArrayList<>();
    @UnsupportedAppUsage
    Configuration mCurrentConfig;
    private HwCustSplitActivity mCustActivity = null;
    View mDecor = null;
    private int mDefaultKeyMode = 0;
    private SpannableStringBuilder mDefaultKeySsb = null;
    @UnsupportedAppUsage
    private boolean mDestroyed;
    private boolean mDoReportFullyDrawn = true;
    @UnsupportedAppUsage
    String mEmbeddedID;
    private boolean mEnableDefaultActionBarUp;
    boolean mEnterAnimationComplete;
    SharedElementCallback mEnterTransitionListener = SharedElementCallback.NULL_CALLBACK;
    SharedElementCallback mExitTransitionListener = SharedElementCallback.NULL_CALLBACK;
    @UnsupportedAppUsage
    boolean mFinished;
    @UnsupportedAppUsage
    final FragmentController mFragments = FragmentController.createController(new HostCallbacks());
    @UnsupportedAppUsage
    final Handler mHandler = new Handler();
    private boolean mHasCurrentPermissionsRequest;
    @UnsupportedAppUsage
    private int mIdent;
    private final Object mInstanceTracker = StrictMode.trackActivity(this);
    @UnsupportedAppUsage
    private Instrumentation mInstrumentation;
    @UnsupportedAppUsage
    Intent mIntent;
    private boolean mIsFullFlag = false;
    private int mLastAutofillId = View.LAST_APP_AUTOFILL_ID;
    @UnsupportedAppUsage
    NonConfigurationInstances mLastNonConfigurationInstances;
    @UnsupportedAppUsage
    ActivityThread mMainThread;
    @GuardedBy({"mManagedCursors"})
    private final ArrayList<ManagedCursor> mManagedCursors = new ArrayList<>();
    private SparseArray<ManagedDialog> mManagedDialogs;
    private MenuInflater mMenuInflater;
    public int mNavigationBarColor = 0;
    @UnsupportedAppUsage
    Activity mParent;
    @UnsupportedAppUsage
    String mReferrer;
    private boolean mRestoredFromBundle;
    @UnsupportedAppUsage
    @GuardedBy({"this"})
    int mResultCode = 0;
    @UnsupportedAppUsage
    @GuardedBy({"this"})
    Intent mResultData = null;
    @UnsupportedAppUsage
    boolean mResumed;
    private SearchEvent mSearchEvent;
    private SearchManager mSearchManager;
    boolean mStartedActivity;
    @UnsupportedAppUsage
    boolean mStopped;
    private ActivityManager.TaskDescription mTaskDescription = new ActivityManager.TaskDescription();
    @UnsupportedAppUsage
    private CharSequence mTitle;
    private int mTitleColor = 0;
    private boolean mTitleReady = false;
    @UnsupportedAppUsage
    private IBinder mToken;
    private TranslucentConversionListener mTranslucentCallback;
    private Thread mUiThread;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    boolean mVisibleFromClient = true;
    boolean mVisibleFromServer = false;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    VoiceInteractor mVoiceInteractor;
    @UnsupportedAppUsage
    private Window mWindow;
    @UnsupportedAppUsage
    boolean mWindowAdded = false;
    @UnsupportedAppUsage
    private WindowManager mWindowManager;
    private IZrHung mZrHungAppEyeUiProbe = HwFrameworkFactory.getZrHung(IZrHung.APPEYE_UIP_NAME);

    @Retention(RetentionPolicy.SOURCE)
    @interface ContentCaptureNotificationType {
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface DefaultKeyMode {
    }

    @SystemApi
    public interface TranslucentConversionListener {
        void onTranslucentConversionComplete(boolean z);
    }

    private static native String getDlWarning();

    /* access modifiers changed from: private */
    public static class ManagedDialog {
        Bundle mArgs;
        Dialog mDialog;

        private ManagedDialog() {
        }
    }

    /* access modifiers changed from: package-private */
    public static final class NonConfigurationInstances {
        Object activity;
        HashMap<String, Object> children;
        FragmentManagerNonConfig fragments;
        ArrayMap<String, LoaderManager> loaders;
        VoiceInteractor voiceInteractor;

        NonConfigurationInstances() {
        }
    }

    /* access modifiers changed from: private */
    public static final class ManagedCursor {
        private final Cursor mCursor;
        private boolean mReleased = false;
        private boolean mUpdated = false;

        ManagedCursor(Cursor cursor) {
            this.mCursor = cursor;
        }
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public void setIntent(Intent newIntent) {
        this.mIntent = newIntent;
    }

    public final Application getApplication() {
        return this.mApplication;
    }

    public final boolean isChild() {
        return this.mParent != null;
    }

    public final Activity getParent() {
        return this.mParent;
    }

    public WindowManager getWindowManager() {
        return this.mWindowManager;
    }

    public Window getWindow() {
        return this.mWindow;
    }

    @Deprecated
    public LoaderManager getLoaderManager() {
        return this.mFragments.getLoaderManager();
    }

    public View getCurrentFocus() {
        Window window = this.mWindow;
        if (window != null) {
            return window.getCurrentFocus();
        }
        return null;
    }

    private AutofillManager getAutofillManager() {
        if (this.mAutofillManager == null) {
            this.mAutofillManager = (AutofillManager) getSystemService(AutofillManager.class);
        }
        return this.mAutofillManager;
    }

    private ContentCaptureManager getContentCaptureManager() {
        if (!UserHandle.isApp(Process.myUid())) {
            return null;
        }
        if (this.mContentCaptureManager == null) {
            this.mContentCaptureManager = (ContentCaptureManager) getSystemService(ContentCaptureManager.class);
        }
        return this.mContentCaptureManager;
    }

    private String getContentCaptureTypeAsString(int type) {
        if (type == 1) {
            return HwMediaMonitorUtils.TYPE_ROUTE_START;
        }
        if (type == 2) {
            return "RESUME";
        }
        if (type == 3) {
            return HwMediaMonitorUtils.TYPE_ROUTE_PAUSE;
        }
        if (type == 4) {
            return HwMediaMonitorUtils.TYPE_ROUTE_STOP;
        }
        return "UNKNOW-" + type;
    }

    private void notifyContentCaptureManagerIfNeeded(int type) {
        if (Trace.isTagEnabled(64)) {
            Trace.traceBegin(64, "notifyContentCapture(" + getContentCaptureTypeAsString(type) + ") for " + this.mComponent.toShortString());
        }
        try {
            ContentCaptureManager cm = getContentCaptureManager();
            if (cm != null) {
                if (type == 1) {
                    Window window = getWindow();
                    if (window != null) {
                        cm.updateWindowAttributes(window.getAttributes());
                    }
                    cm.onActivityCreated(this.mToken, getComponentName());
                } else if (type == 2) {
                    cm.onActivityResumed();
                } else if (type == 3) {
                    cm.onActivityPaused();
                } else if (type != 4) {
                    Log.wtf(TAG, "Invalid @ContentCaptureNotificationType: " + type);
                } else {
                    cm.onActivityDestroyed();
                }
                Trace.traceEnd(64);
            }
        } finally {
            Trace.traceEnd(64);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ContextThemeWrapper, android.content.ContextWrapper
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        if (newBase != null) {
            newBase.setAutofillClient(this);
            newBase.setContentCaptureOptions(getContentCaptureOptions());
        }
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public final AutofillManager.AutofillClient getAutofillClient() {
        return this;
    }

    @Override // android.content.Context
    public final ContentCaptureManager.ContentCaptureClient getContentCaptureClient() {
        return this;
    }

    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        synchronized (this.mActivityLifecycleCallbacks) {
            this.mActivityLifecycleCallbacks.add(callback);
        }
    }

    public void unregisterActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        synchronized (this.mActivityLifecycleCallbacks) {
            this.mActivityLifecycleCallbacks.remove(callback);
        }
    }

    private void dispatchActivityPreCreated(Bundle savedInstanceState) {
        getApplication().dispatchActivityPreCreated(this, savedInstanceState);
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((Application.ActivityLifecycleCallbacks) obj).onActivityPreCreated(this, savedInstanceState);
            }
        }
    }

    private void dispatchActivityCreated(Bundle savedInstanceState) {
        getApplication().dispatchActivityCreated(this, savedInstanceState);
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((Application.ActivityLifecycleCallbacks) obj).onActivityCreated(this, savedInstanceState);
            }
        }
    }

    private void dispatchActivityPostCreated(Bundle savedInstanceState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((Application.ActivityLifecycleCallbacks) obj).onActivityPostCreated(this, savedInstanceState);
            }
        }
        getApplication().dispatchActivityPostCreated(this, savedInstanceState);
    }

    private void dispatchActivityPreStarted() {
        getApplication().dispatchActivityPreStarted(this);
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((Application.ActivityLifecycleCallbacks) obj).onActivityPreStarted(this);
            }
        }
    }

    private void dispatchActivityStarted() {
        getApplication().dispatchActivityStarted(this);
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((Application.ActivityLifecycleCallbacks) obj).onActivityStarted(this);
            }
        }
    }

    private void dispatchActivityPostStarted() {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((Application.ActivityLifecycleCallbacks) obj).onActivityPostStarted(this);
            }
        }
        getApplication().dispatchActivityPostStarted(this);
    }

    private void dispatchActivityPreResumed() {
        getApplication().dispatchActivityPreResumed(this);
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((Application.ActivityLifecycleCallbacks) obj).onActivityPreResumed(this);
            }
        }
    }

    private void dispatchActivityResumed() {
        getApplication().dispatchActivityResumed(this);
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((Application.ActivityLifecycleCallbacks) obj).onActivityResumed(this);
            }
        }
    }

    private void dispatchActivityPostResumed() {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((Application.ActivityLifecycleCallbacks) obj).onActivityPostResumed(this);
            }
        }
        getApplication().dispatchActivityPostResumed(this);
    }

    private void dispatchActivityPrePaused() {
        getApplication().dispatchActivityPrePaused(this);
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivityPrePaused(this);
            }
        }
    }

    private void dispatchActivityPaused() {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivityPaused(this);
            }
        }
        getApplication().dispatchActivityPaused(this);
    }

    private void dispatchActivityPostPaused() {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivityPostPaused(this);
            }
        }
        getApplication().dispatchActivityPostPaused(this);
    }

    private void dispatchActivityPreStopped() {
        getApplication().dispatchActivityPreStopped(this);
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivityPreStopped(this);
            }
        }
    }

    private void dispatchActivityStopped() {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivityStopped(this);
            }
        }
        getApplication().dispatchActivityStopped(this);
    }

    private void dispatchActivityPostStopped() {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivityPostStopped(this);
            }
        }
        getApplication().dispatchActivityPostStopped(this);
    }

    private void dispatchActivityPreSaveInstanceState(Bundle outState) {
        getApplication().dispatchActivityPreSaveInstanceState(this, outState);
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivityPreSaveInstanceState(this, outState);
            }
        }
    }

    private void dispatchActivitySaveInstanceState(Bundle outState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivitySaveInstanceState(this, outState);
            }
        }
        getApplication().dispatchActivitySaveInstanceState(this, outState);
    }

    private void dispatchActivityPostSaveInstanceState(Bundle outState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivityPostSaveInstanceState(this, outState);
            }
        }
        getApplication().dispatchActivityPostSaveInstanceState(this, outState);
    }

    private void dispatchActivityPreDestroyed() {
        getApplication().dispatchActivityPreDestroyed(this);
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivityPreDestroyed(this);
            }
        }
    }

    private void dispatchActivityDestroyed() {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivityDestroyed(this);
            }
        }
        getApplication().dispatchActivityDestroyed(this);
    }

    private void dispatchActivityPostDestroyed() {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = callbacks.length - 1; i >= 0; i--) {
                ((Application.ActivityLifecycleCallbacks) callbacks[i]).onActivityPostDestroyed(this);
            }
        }
        getApplication().dispatchActivityPostDestroyed(this);
    }

    private Object[] collectActivityLifecycleCallbacks() {
        Object[] callbacks = null;
        synchronized (this.mActivityLifecycleCallbacks) {
            if (this.mActivityLifecycleCallbacks.size() > 0) {
                callbacks = this.mActivityLifecycleCallbacks.toArray();
            }
        }
        return callbacks;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        boolean z = false;
        if (this.mCustActivity == null && (this.mIntent.getHwFlags() & 4) != 0) {
            this.mCustActivity = (HwCustSplitActivity) HwCustUtils.createObj(HwCustSplitActivity.class, new Object[0]);
        }
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
            this.mCustActivity.initSplitMode(this.mToken);
        }
        NonConfigurationInstances nonConfigurationInstances = this.mLastNonConfigurationInstances;
        if (nonConfigurationInstances != null) {
            this.mFragments.restoreLoaderNonConfig(nonConfigurationInstances.loaders);
        }
        if (this.mActivityInfo.parentActivityName != null) {
            ActionBar actionBar = this.mActionBar;
            if (actionBar == null) {
                this.mEnableDefaultActionBarUp = true;
            } else {
                actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            }
        }
        if (savedInstanceState != null) {
            this.mAutoFillResetNeeded = savedInstanceState.getBoolean(AUTOFILL_RESET_NEEDED, false);
            this.mLastAutofillId = savedInstanceState.getInt(LAST_AUTOFILL_ID, View.LAST_APP_AUTOFILL_ID);
            if (this.mAutoFillResetNeeded) {
                getAutofillManager().onCreate(savedInstanceState);
            }
            Parcelable p = savedInstanceState.getParcelable(FRAGMENTS_TAG);
            FragmentController fragmentController = this.mFragments;
            NonConfigurationInstances nonConfigurationInstances2 = this.mLastNonConfigurationInstances;
            fragmentController.restoreAllState(p, nonConfigurationInstances2 != null ? nonConfigurationInstances2.fragments : null);
        }
        this.mFragments.dispatchCreate();
        dispatchActivityCreated(savedInstanceState);
        VoiceInteractor voiceInteractor = this.mVoiceInteractor;
        if (voiceInteractor != null) {
            voiceInteractor.attachActivity(this);
        }
        if (savedInstanceState != null) {
            z = true;
        }
        this.mRestoredFromBundle = z;
        this.mCalled = true;
    }

    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        onCreate(savedInstanceState);
    }

    /* access modifiers changed from: package-private */
    public final void performRestoreInstanceState(Bundle savedInstanceState) {
        if (Log.HWINFO) {
            Trace.traceBegin(64, "onRestoreInstanceState");
        }
        onRestoreInstanceState(savedInstanceState);
        if (Log.HWINFO) {
            Trace.traceEnd(64);
        }
        restoreManagedDialogs(savedInstanceState);
    }

    /* access modifiers changed from: package-private */
    public final void performRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        onRestoreInstanceState(savedInstanceState, persistentState);
        if (savedInstanceState != null) {
            restoreManagedDialogs(savedInstanceState);
        }
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Bundle windowState;
        if (this.mWindow != null && (windowState = savedInstanceState.getBundle(WINDOW_HIERARCHY_TAG)) != null) {
            this.mWindow.restoreHierarchyState(windowState);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
    }

    private void restoreManagedDialogs(Bundle savedInstanceState) {
        Bundle b = savedInstanceState.getBundle(SAVED_DIALOGS_TAG);
        if (b != null) {
            int[] ids = b.getIntArray(SAVED_DIALOG_IDS_KEY);
            int numDialogs = ids.length;
            this.mManagedDialogs = new SparseArray<>(numDialogs);
            for (int i : ids) {
                Integer dialogId = Integer.valueOf(i);
                Bundle dialogState = b.getBundle(savedDialogKeyFor(dialogId.intValue()));
                if (dialogState != null) {
                    ManagedDialog md = new ManagedDialog();
                    md.mArgs = b.getBundle(savedDialogArgsKeyFor(dialogId.intValue()));
                    md.mDialog = createDialog(dialogId, dialogState, md.mArgs);
                    if (md.mDialog != null) {
                        this.mManagedDialogs.put(dialogId.intValue(), md);
                        onPrepareDialog(dialogId.intValue(), md.mDialog, md.mArgs);
                        md.mDialog.onRestoreInstanceState(dialogState);
                    }
                }
            }
        }
    }

    private Dialog createDialog(Integer dialogId, Bundle state, Bundle args) {
        Dialog dialog = onCreateDialog(dialogId.intValue(), args);
        if (dialog == null) {
            return null;
        }
        dialog.dispatchOnCreate(state);
        return dialog;
    }

    private static String savedDialogKeyFor(int key) {
        return SAVED_DIALOG_KEY_PREFIX + key;
    }

    private static String savedDialogArgsKeyFor(int key) {
        return SAVED_DIALOG_ARGS_KEY_PREFIX + key;
    }

    /* access modifiers changed from: protected */
    public void onPostCreate(Bundle savedInstanceState) {
        if (!isChild()) {
            this.mTitleReady = true;
            onTitleChanged(getTitle(), getTitleColor());
        }
        this.mCalled = true;
        notifyContentCaptureManagerIfNeeded(1);
    }

    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        onPostCreate(savedInstanceState);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        this.mCalled = true;
        this.mFragments.doLoaderStart();
        dispatchActivityStarted();
        if (this.mAutoFillResetNeeded) {
            getAutofillManager().onVisibleForAutofill();
        }
    }

    /* access modifiers changed from: protected */
    public void onRestart() {
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityRestart();
        }
        this.mCalled = true;
    }

    @Deprecated
    public void onStateNotSaved() {
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        View focus;
        dispatchActivityResumed();
        this.mActivityTransitionState.onResume(this);
        enableAutofillCompatibilityIfNeeded();
        if (this.mAutoFillResetNeeded && !this.mAutoFillIgnoreFirstResumePause && (focus = getCurrentFocus()) != null && focus.canNotifyAutofillEnterExitEvent()) {
            getAutofillManager().notifyViewEntered(focus);
        }
        notifyContentCaptureManagerIfNeeded(2);
        this.mCalled = true;
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityResume();
        }
        if (this.mZrHungAppEyeUiProbe != null) {
            ZrHungData data = new ZrHungData();
            data.putString(CalendarContract.RemindersColumns.METHOD, "setCurrActivity");
            data.put(Context.ACTIVITY_SERVICE, this);
            this.mZrHungAppEyeUiProbe.check(data);
        }
    }

    /* access modifiers changed from: protected */
    public void onPostResume() {
        Window win = getWindow();
        if (win != null) {
            win.makeActive();
        }
        ActionBar actionBar = this.mActionBar;
        if (actionBar != null) {
            actionBar.setShowHideAnimationEnabled(true);
        }
        this.mCalled = true;
        if (HwMwUtils.ENABLED) {
            HwMwUtils.performPolicy(1001, this, this.mCoverViewList);
        }
    }

    public void onTopResumedActivityChanged(boolean isTopResumedActivity) {
    }

    /* access modifiers changed from: package-private */
    public final void performTopResumedActivityChanged(boolean isTopResumedActivity, String reason) {
        onTopResumedActivityChanged(isTopResumedActivity);
        writeEventLog(isTopResumedActivity ? LOG_AM_ON_TOP_RESUMED_GAINED_CALLED : LOG_AM_ON_TOP_RESUMED_LOST_CALLED, reason);
    }

    /* access modifiers changed from: package-private */
    public void setVoiceInteractor(IVoiceInteractor voiceInteractor) {
        VoiceInteractor voiceInteractor2 = this.mVoiceInteractor;
        if (!(voiceInteractor2 == null || voiceInteractor2.getActiveRequests() == null)) {
            VoiceInteractor.Request[] activeRequests = this.mVoiceInteractor.getActiveRequests();
            for (VoiceInteractor.Request activeRequest : activeRequests) {
                activeRequest.cancel();
                activeRequest.clear();
            }
        }
        if (voiceInteractor == null) {
            this.mVoiceInteractor = null;
        } else {
            this.mVoiceInteractor = new VoiceInteractor(voiceInteractor, this, this, Looper.myLooper());
        }
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public int getNextAutofillId() {
        if (this.mLastAutofillId == 2147483646) {
            this.mLastAutofillId = View.LAST_APP_AUTOFILL_ID;
        }
        this.mLastAutofillId++;
        return this.mLastAutofillId;
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public AutofillId autofillClientGetNextAutofillId() {
        return new AutofillId(getNextAutofillId());
    }

    public boolean isVoiceInteraction() {
        return this.mVoiceInteractor != null;
    }

    public boolean isVoiceInteractionRoot() {
        try {
            if (this.mVoiceInteractor == null || !ActivityTaskManager.getService().isRootVoiceInteraction(this.mToken)) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public VoiceInteractor getVoiceInteractor() {
        return this.mVoiceInteractor;
    }

    public boolean isLocalVoiceInteractionSupported() {
        try {
            return ActivityTaskManager.getService().supportsLocalVoiceInteraction();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void startLocalVoiceInteraction(Bundle privateOptions) {
        try {
            ActivityTaskManager.getService().startLocalVoiceInteraction(this.mToken, privateOptions);
        } catch (RemoteException e) {
        }
    }

    public void onLocalVoiceInteractionStarted() {
    }

    public void onLocalVoiceInteractionStopped() {
    }

    public void stopLocalVoiceInteraction() {
        try {
            ActivityTaskManager.getService().stopLocalVoiceInteraction(this.mToken);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityNewIntent(intent);
        }
    }

    /* access modifiers changed from: package-private */
    public final void performSaveInstanceState(Bundle outState) {
        dispatchActivityPreSaveInstanceState(outState);
        if (Log.HWINFO) {
            Trace.traceBegin(64, "onSaveInstatnceState");
        }
        onSaveInstanceState(outState);
        if (Log.HWINFO) {
            Trace.traceEnd(64);
        }
        saveManagedDialogs(outState);
        this.mActivityTransitionState.saveState(outState);
        storeHasCurrentPermissionRequest(outState);
        dispatchActivityPostSaveInstanceState(outState);
    }

    /* access modifiers changed from: package-private */
    public final void performSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        dispatchActivityPreSaveInstanceState(outState);
        onSaveInstanceState(outState, outPersistentState);
        saveManagedDialogs(outState);
        storeHasCurrentPermissionRequest(outState);
        dispatchActivityPostSaveInstanceState(outState);
    }

    /* access modifiers changed from: protected */
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle(WINDOW_HIERARCHY_TAG, this.mWindow.saveHierarchyState());
        outState.putInt(LAST_AUTOFILL_ID, this.mLastAutofillId);
        Parcelable p = this.mFragments.saveAllState();
        if (p != null) {
            outState.putParcelable(FRAGMENTS_TAG, p);
        }
        if (this.mAutoFillResetNeeded) {
            outState.putBoolean(AUTOFILL_RESET_NEEDED, true);
            getAutofillManager().onSaveInstanceState(outState);
        }
        dispatchActivitySaveInstanceState(outState);
    }

    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        onSaveInstanceState(outState);
    }

    @UnsupportedAppUsage
    private void saveManagedDialogs(Bundle outState) {
        int numDialogs;
        SparseArray<ManagedDialog> sparseArray = this.mManagedDialogs;
        if (!(sparseArray == null || (numDialogs = sparseArray.size()) == 0)) {
            Bundle dialogState = new Bundle();
            int[] ids = new int[this.mManagedDialogs.size()];
            for (int i = 0; i < numDialogs; i++) {
                int key = this.mManagedDialogs.keyAt(i);
                ids[i] = key;
                ManagedDialog md = this.mManagedDialogs.valueAt(i);
                dialogState.putBundle(savedDialogKeyFor(key), md.mDialog.onSaveInstanceState());
                if (md.mArgs != null) {
                    dialogState.putBundle(savedDialogArgsKeyFor(key), md.mArgs);
                }
            }
            dialogState.putIntArray(SAVED_DIALOG_IDS_KEY, ids);
            outState.putBundle(SAVED_DIALOGS_TAG, dialogState);
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        dispatchActivityPaused();
        if (this.mAutoFillResetNeeded) {
            if (!this.mAutoFillIgnoreFirstResumePause) {
                View focus = getCurrentFocus();
                if (focus != null && focus.canNotifyAutofillEnterExitEvent()) {
                    getAutofillManager().notifyViewExited(focus);
                }
            } else {
                this.mAutoFillIgnoreFirstResumePause = false;
            }
        }
        HwFrameworkFactory.getHwApsImpl().setApsOnPause();
        notifyContentCaptureManagerIfNeeded(3);
        this.mCalled = true;
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityPaused();
        }
    }

    /* access modifiers changed from: protected */
    public void onUserLeaveHint() {
    }

    @Deprecated
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        return false;
    }

    public CharSequence onCreateDescription() {
        return null;
    }

    public void onProvideAssistData(Bundle data) {
    }

    public void onProvideAssistContent(AssistContent outContent) {
    }

    public void onGetDirectActions(CancellationSignal cancellationSignal, Consumer<List<DirectAction>> callback) {
        callback.accept(Collections.emptyList());
    }

    public void onPerformDirectAction(String actionId, Bundle arguments, CancellationSignal cancellationSignal, Consumer<Bundle> consumer) {
    }

    public final void requestShowKeyboardShortcuts() {
        Intent intent = new Intent(Intent.ACTION_SHOW_KEYBOARD_SHORTCUTS);
        intent.setPackage("com.android.systemui");
        sendBroadcastAsUser(intent, Process.myUserHandle());
    }

    public final void dismissKeyboardShortcutsHelper() {
        Intent intent = new Intent(Intent.ACTION_DISMISS_KEYBOARD_SHORTCUTS);
        intent.setPackage("com.android.systemui");
        sendBroadcastAsUser(intent, Process.myUserHandle());
    }

    @Override // android.view.Window.Callback
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {
        if (menu != null) {
            KeyboardShortcutGroup group = null;
            int menuSize = menu.size();
            for (int i = 0; i < menuSize; i++) {
                MenuItem item = menu.getItem(i);
                CharSequence title = item.getTitle();
                char alphaShortcut = item.getAlphabeticShortcut();
                int alphaModifiers = item.getAlphabeticModifiers();
                if (!(title == null || alphaShortcut == 0)) {
                    if (group == null) {
                        int resource = this.mApplication.getApplicationInfo().labelRes;
                        group = new KeyboardShortcutGroup(resource != 0 ? getString(resource) : null);
                    }
                    group.addItem(new KeyboardShortcutInfo(title, alphaShortcut, alphaModifiers));
                }
            }
            if (group != null) {
                data.add(group);
            }
        }
    }

    public boolean showAssist(Bundle args) {
        try {
            return ActivityTaskManager.getService().showAssistFromActivity(this.mToken, args);
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        ActionBar actionBar = this.mActionBar;
        if (actionBar != null) {
            actionBar.setShowHideAnimationEnabled(false);
        }
        this.mActivityTransitionState.onStop();
        dispatchActivityStopped();
        this.mTranslucentCallback = null;
        this.mCalled = true;
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityStop();
        }
        if (this.mAutoFillResetNeeded) {
            getAutofillManager().onInvisibleForAutofill();
        }
        if (isFinishing()) {
            if (this.mAutoFillResetNeeded) {
                getAutofillManager().onActivityFinishing();
            } else {
                Intent intent = this.mIntent;
                if (intent != null && intent.hasExtra(AutofillManager.EXTRA_RESTORE_SESSION_TOKEN)) {
                    getAutofillManager().onPendingSaveUi(1, this.mIntent.getIBinderExtra(AutofillManager.EXTRA_RESTORE_SESSION_TOKEN));
                }
            }
        }
        this.mEnterAnimationComplete = false;
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        this.mCalled = true;
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityDestroy();
        }
        SparseArray<ManagedDialog> sparseArray = this.mManagedDialogs;
        if (sparseArray != null) {
            int numDialogs = sparseArray.size();
            for (int i = 0; i < numDialogs; i++) {
                ManagedDialog md = this.mManagedDialogs.valueAt(i);
                if (md.mDialog.isShowing()) {
                    md.mDialog.dismiss();
                }
            }
            this.mManagedDialogs = null;
        }
        synchronized (this.mManagedCursors) {
            int numCursors = this.mManagedCursors.size();
            for (int i2 = 0; i2 < numCursors; i2++) {
                ManagedCursor c = this.mManagedCursors.get(i2);
                if (c != null) {
                    c.mCursor.close();
                }
            }
            this.mManagedCursors.clear();
        }
        SearchManager searchManager = this.mSearchManager;
        if (searchManager != null) {
            searchManager.stopSearch();
        }
        ActionBar actionBar = this.mActionBar;
        if (actionBar != null) {
            actionBar.onDestroy();
        }
        dispatchActivityDestroyed();
        notifyContentCaptureManagerIfNeeded(4);
        MemoryLeakMonitorManager.watchMemoryLeak(this);
        this.mCoverViewList = null;
    }

    public void reportFullyDrawn() {
        if (this.mDoReportFullyDrawn) {
            this.mDoReportFullyDrawn = false;
            try {
                ActivityTaskManager.getService().reportActivityFullyDrawn(this.mToken, this.mRestoredFromBundle);
                VMRuntime.getRuntime().notifyStartupCompleted();
            } catch (RemoteException e) {
            }
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        onMultiWindowModeChanged(isInMultiWindowMode);
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
            this.mCustActivity.setLastReportedMultiWindowMode(isInMultiWindowMode);
        }
    }

    @Deprecated
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
    }

    public boolean isInMultiWindowMode() {
        try {
            return ActivityTaskManager.getService().isInMultiWindowMode(this.mToken);
        } catch (RemoteException e) {
            return false;
        }
    }

    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        onPictureInPictureModeChanged(isInPictureInPictureMode);
    }

    @Deprecated
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
    }

    public boolean isInPictureInPictureMode() {
        try {
            return ActivityTaskManager.getService().isInPictureInPictureMode(this.mToken);
        } catch (RemoteException e) {
            return false;
        }
    }

    @Deprecated
    public void enterPictureInPictureMode() {
        enterPictureInPictureMode(new PictureInPictureParams.Builder().build());
    }

    @Deprecated
    public boolean enterPictureInPictureMode(PictureInPictureArgs args) {
        return enterPictureInPictureMode(PictureInPictureArgs.convert(args));
    }

    public boolean enterPictureInPictureMode(PictureInPictureParams params) {
        try {
            if (!deviceSupportsPictureInPictureMode()) {
                return false;
            }
            if (params == null) {
                throw new IllegalArgumentException("Expected non-null picture-in-picture params");
            } else if (this.mCanEnterPictureInPicture) {
                return ActivityTaskManager.getService().enterPictureInPictureMode(this.mToken, params);
            } else {
                throw new IllegalStateException("Activity must be resumed to enter picture-in-picture");
            }
        } catch (RemoteException e) {
            return false;
        }
    }

    @Deprecated
    public void setPictureInPictureArgs(PictureInPictureArgs args) {
        setPictureInPictureParams(PictureInPictureArgs.convert(args));
    }

    public void setPictureInPictureParams(PictureInPictureParams params) {
        try {
            if (deviceSupportsPictureInPictureMode()) {
                if (params != null) {
                    ActivityTaskManager.getService().setPictureInPictureParams(this.mToken, params);
                    return;
                }
                throw new IllegalArgumentException("Expected non-null picture-in-picture params");
            }
        } catch (RemoteException e) {
        }
    }

    public int getMaxNumPictureInPictureActions() {
        try {
            return ActivityTaskManager.getService().getMaxNumPictureInPictureActions(this.mToken);
        } catch (RemoteException e) {
            return 0;
        }
    }

    private boolean deviceSupportsPictureInPictureMode() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
    }

    /* access modifiers changed from: package-private */
    public void dispatchMovedToDisplay(int displayId, Configuration config) {
        updateDisplay(displayId);
        onMovedToDisplay(displayId, config);
    }

    public void onMovedToDisplay(int displayId, Configuration config) {
    }

    @Override // android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        this.mCalled = true;
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityConfigurationChanged(newConfig);
        }
        this.mFragments.dispatchConfigurationChanged(newConfig);
        Window window = this.mWindow;
        if (window != null) {
            window.onConfigurationChanged(newConfig);
        }
        ActionBar actionBar = this.mActionBar;
        if (actionBar != null) {
            actionBar.onConfigurationChanged(newConfig);
        }
        if (HwMwUtils.ENABLED) {
            HwMwUtils.performPolicy(1001, this, this.mCoverViewList);
        }
    }

    public int getChangingConfigurations() {
        return this.mConfigChangeFlags;
    }

    public Object getLastNonConfigurationInstance() {
        NonConfigurationInstances nonConfigurationInstances = this.mLastNonConfigurationInstances;
        if (nonConfigurationInstances != null) {
            return nonConfigurationInstances.activity;
        }
        return null;
    }

    public Object onRetainNonConfigurationInstance() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public HashMap<String, Object> getLastNonConfigurationChildInstances() {
        NonConfigurationInstances nonConfigurationInstances = this.mLastNonConfigurationInstances;
        if (nonConfigurationInstances != null) {
            return nonConfigurationInstances.children;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public HashMap<String, Object> onRetainNonConfigurationChildInstances() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public NonConfigurationInstances retainNonConfigurationInstances() {
        Object activity = onRetainNonConfigurationInstance();
        HashMap<String, Object> children = onRetainNonConfigurationChildInstances();
        FragmentManagerNonConfig fragments = this.mFragments.retainNestedNonConfig();
        this.mFragments.doLoaderStart();
        this.mFragments.doLoaderStop(true);
        ArrayMap<String, LoaderManager> loaders = this.mFragments.retainLoaderNonConfig();
        if (activity == null && children == null && fragments == null && loaders == null && this.mVoiceInteractor == null) {
            return null;
        }
        NonConfigurationInstances nci = new NonConfigurationInstances();
        nci.activity = activity;
        nci.children = children;
        nci.fragments = fragments;
        nci.loaders = loaders;
        VoiceInteractor voiceInteractor = this.mVoiceInteractor;
        if (voiceInteractor != null) {
            voiceInteractor.retainInstance();
            nci.voiceInteractor = this.mVoiceInteractor;
        }
        return nci;
    }

    @Override // android.content.ComponentCallbacks
    public void onLowMemory() {
        this.mCalled = true;
        this.mFragments.dispatchLowMemory();
    }

    @Override // android.content.ComponentCallbacks2
    public void onTrimMemory(int level) {
        this.mCalled = true;
        this.mFragments.dispatchTrimMemory(level);
    }

    @Deprecated
    public FragmentManager getFragmentManager() {
        return this.mFragments.getFragmentManager();
    }

    @Deprecated
    public void onAttachFragment(Fragment fragment) {
    }

    @UnsupportedAppUsage
    @Deprecated
    public final Cursor managedQuery(Uri uri, String[] projection, String selection, String sortOrder) {
        Cursor c = getContentResolver().query(uri, projection, selection, null, sortOrder);
        if (c != null) {
            startManagingCursor(c);
        }
        return c;
    }

    @Deprecated
    public final Cursor managedQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c = getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        if (c != null) {
            startManagingCursor(c);
        }
        return c;
    }

    @Deprecated
    public void startManagingCursor(Cursor c) {
        synchronized (this.mManagedCursors) {
            this.mManagedCursors.add(new ManagedCursor(c));
        }
    }

    @Deprecated
    public void stopManagingCursor(Cursor c) {
        synchronized (this.mManagedCursors) {
            int N = this.mManagedCursors.size();
            int i = 0;
            while (true) {
                if (i >= N) {
                    break;
                } else if (this.mManagedCursors.get(i).mCursor == c) {
                    this.mManagedCursors.remove(i);
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public void setPersistent(boolean isPersistent) {
    }

    public <T extends View> T findViewById(int id) {
        return (T) getWindow().findViewById(id);
    }

    public final <T extends View> T requireViewById(int id) {
        T view = (T) findViewById(id);
        if (view != null) {
            return view;
        }
        throw new IllegalArgumentException("ID does not reference a View inside this Activity");
    }

    public ActionBar getActionBar() {
        initWindowDecorActionBar();
        return this.mActionBar;
    }

    public void setActionBar(Toolbar toolbar) {
        ActionBar ab = getActionBar();
        if (!(ab instanceof WindowDecorActionBar)) {
            this.mMenuInflater = null;
            if (ab != null) {
                ab.onDestroy();
            }
            if (toolbar != null) {
                ToolbarActionBar tbab = new ToolbarActionBar(toolbar, getTitle(), this);
                this.mActionBar = tbab;
                this.mWindow.setCallback(tbab.getWrappedWindowCallback());
            } else {
                this.mActionBar = null;
                this.mWindow.setCallback(this);
            }
            invalidateOptionsMenu();
            return;
        }
        throw new IllegalStateException("This Activity already has an action bar supplied by the window decor. Do not request Window.FEATURE_ACTION_BAR and set android:windowActionBar to false in your theme to use a Toolbar instead.");
    }

    private void initWindowDecorActionBar() {
        Window window = getWindow();
        window.getDecorView();
        if (!isChild() && window.hasFeature(8) && this.mActionBar == null) {
            this.mActionBar = HwWidgetFactory.getHuaweiActionBarImpl(this);
            this.mActionBar.setDefaultDisplayHomeAsUpEnabled(this.mEnableDefaultActionBarUp);
            this.mWindow.setDefaultIcon(this.mActivityInfo.getIconResource());
            this.mWindow.setDefaultLogo(this.mActivityInfo.getLogoResource());
        }
    }

    public void setContentView(int layoutResID) {
        getWindow().setContentView(layoutResID);
        initWindowDecorActionBar();
    }

    public void setContentView(View view) {
        getWindow().setContentView(view);
        initWindowDecorActionBar();
    }

    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getWindow().setContentView(view, params);
        initWindowDecorActionBar();
    }

    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getWindow().addContentView(view, params);
        initWindowDecorActionBar();
    }

    public TransitionManager getContentTransitionManager() {
        return getWindow().getTransitionManager();
    }

    public void setContentTransitionManager(TransitionManager tm) {
        getWindow().setTransitionManager(tm);
    }

    public Scene getContentScene() {
        return getWindow().getContentScene();
    }

    public void setFinishOnTouchOutside(boolean finish) {
        this.mWindow.setCloseOnTouchOutside(finish);
    }

    public final void setDefaultKeyMode(int mode) {
        this.mDefaultKeyMode = mode;
        if (mode != 0) {
            if (mode != 1) {
                if (mode != 2) {
                    if (!(mode == 3 || mode == 4)) {
                        throw new IllegalArgumentException();
                    }
                }
            }
            this.mDefaultKeySsb = new SpannableStringBuilder();
            Selection.setSelection(this.mDefaultKeySsb, 0);
            return;
        }
        this.mDefaultKeySsb = null;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled;
        if (keyCode == 4) {
            if (getApplicationInfo().targetSdkVersion >= 5) {
                event.startTracking();
            } else {
                onBackPressed();
            }
            return true;
        }
        int i = this.mDefaultKeyMode;
        if (i == 0) {
            return false;
        }
        if (i == 2) {
            Window w = getWindow();
            if (!w.hasFeature(0) || !w.performPanelShortcut(0, keyCode, event, 2)) {
                return false;
            }
            return true;
        } else if (keyCode == 61) {
            return false;
        } else {
            boolean clearSpannable = false;
            if (event.getRepeatCount() != 0 || event.isSystem()) {
                clearSpannable = true;
                handled = false;
            } else {
                handled = TextKeyListener.getInstance().onKeyDown(null, this.mDefaultKeySsb, keyCode, event);
                if (handled && this.mDefaultKeySsb.length() > 0) {
                    String str = this.mDefaultKeySsb.toString();
                    clearSpannable = true;
                    int i2 = this.mDefaultKeyMode;
                    if (i2 == 1) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(WebView.SCHEME_TEL + str));
                        intent.addFlags(268435456);
                        startActivity(intent);
                    } else if (i2 == 3) {
                        startSearch(str, false, null, false);
                    } else if (i2 == 4) {
                        startSearch(str, false, null, true);
                    }
                }
            }
            if (clearSpannable) {
                this.mDefaultKeySsb.clear();
                this.mDefaultKeySsb.clearSpans();
                Selection.setSelection(this.mDefaultKeySsb, 0);
            }
            return handled;
        }
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    @Override // android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (getApplicationInfo().targetSdkVersion < 5 || keyCode != 4 || !event.isTracking() || event.isCanceled()) {
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
        ActionBar actionBar = this.mActionBar;
        if (actionBar == null || !actionBar.collapseActionView()) {
            HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
            if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
                this.mCustActivity.handleBackPressed();
            }
            FragmentManager fragmentManager = this.mFragments.getFragmentManager();
            if (!fragmentManager.isStateSaved() && fragmentManager.popBackStackImmediate()) {
                return;
            }
            if (!isTaskRoot()) {
                finishAfterTransition();
                return;
            }
            try {
                ActivityTaskManager.getService().onBackPressedOnTaskRoot(this.mToken, new IRequestFinishCallback.Stub() {
                    /* class android.app.Activity.AnonymousClass1 */

                    public /* synthetic */ void lambda$requestFinish$0$Activity$1() {
                        Activity.this.finishAfterTransition();
                    }

                    @Override // android.app.IRequestFinishCallback
                    public void requestFinish() {
                        Activity.this.mHandler.post(new Runnable() {
                            /* class android.app.$$Lambda$Activity$1$pR5b3qDyhldlD2RtkXoHHxgyGPU */

                            @Override // java.lang.Runnable
                            public final void run() {
                                Activity.AnonymousClass1.this.lambda$requestFinish$0$Activity$1();
                            }
                        });
                    }
                });
            } catch (RemoteException e) {
                finishAfterTransition();
            }
        }
    }

    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        ActionBar actionBar = getActionBar();
        return actionBar != null && actionBar.onKeyShortcut(keyCode, event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mWindow.shouldCloseOnTouch(this, event)) {
            return false;
        }
        finish();
        return true;
    }

    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return false;
    }

    public void onUserInteraction() {
    }

    @Override // android.view.Window.Callback
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        View decor;
        if (!(this.mParent != null || (decor = this.mDecor) == null || decor.getParent() == null)) {
            getWindowManager().updateViewLayout(decor, params);
            ContentCaptureManager contentCaptureManager = this.mContentCaptureManager;
            if (contentCaptureManager != null) {
                contentCaptureManager.updateWindowAttributes(params);
            }
        }
        if ((params.flags & 1024) != 0) {
            if (!this.mIsFullFlag && this.mResumed) {
                LogPower.push(120, getPackageName());
                this.mIsFullFlag = true;
            }
        } else if (this.mIsFullFlag) {
            LogPower.push(135, getPackageName());
            this.mIsFullFlag = false;
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

    public boolean hasWindowFocus() {
        View d;
        Window w = getWindow();
        if (w == null || (d = w.getDecorView()) == null) {
            return false;
        }
        return d.hasWindowFocus();
    }

    @Override // android.view.Window.OnWindowDismissedCallback
    public void onWindowDismissed(boolean finishTask, boolean suppressWindowTransition) {
        finish(finishTask ? 2 : 0);
        if (suppressWindowTransition) {
            overridePendingTransition(0, 0);
        }
    }

    @Override // android.view.Window.WindowControllerCallback
    public void toggleFreeformWindowingMode() throws RemoteException {
        ActivityTaskManager.getService().toggleFreeformWindowingMode(this.mToken);
    }

    @Override // android.view.Window.WindowControllerCallback
    public void enterPictureInPictureModeIfPossible() {
        if (this.mActivityInfo.supportsPictureInPicture()) {
            enterPictureInPictureMode();
        }
    }

    @Override // android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent event) {
        ActionBar actionBar;
        onUserInteraction();
        if (event.getKeyCode() == 82 && (actionBar = this.mActionBar) != null && actionBar.onMenuKeyEvent(event)) {
            return true;
        }
        Window win = getWindow();
        if (win.superDispatchKeyEvent(event)) {
            return true;
        }
        View decor = this.mDecor;
        if (decor == null) {
            decor = win.getDecorView();
        }
        return event.dispatch(this, decor != null ? decor.getKeyDispatcherState() : null, this);
    }

    @Override // android.view.Window.Callback
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        onUserInteraction();
        if (getWindow().superDispatchKeyShortcutEvent(event)) {
            return true;
        }
        return onKeyShortcut(event.getKeyCode(), event);
    }

    @Override // android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            onUserInteraction();
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    @Override // android.view.Window.Callback
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        onUserInteraction();
        if (getWindow().superDispatchTrackballEvent(ev)) {
            return true;
        }
        return onTrackballEvent(ev);
    }

    @Override // android.view.Window.Callback
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        onUserInteraction();
        if (getWindow().superDispatchGenericMotionEvent(ev)) {
            return true;
        }
        return onGenericMotionEvent(ev);
    }

    @Override // android.view.Window.Callback
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(getClass().getName());
        event.setPackageName(getPackageName());
        ViewGroup.LayoutParams params = getWindow().getAttributes();
        event.setFullScreen(params.width == -1 && params.height == -1);
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            event.getText().add(title);
        }
        return true;
    }

    @Override // android.view.Window.Callback
    public View onCreatePanelView(int featureId) {
        return null;
    }

    @Override // android.view.Window.Callback
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == 0) {
            return onCreateOptionsMenu(menu) | this.mFragments.dispatchCreateOptionsMenu(menu, getMenuInflater());
        }
        return false;
    }

    @Override // android.view.Window.Callback
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId == 0) {
            return onPrepareOptionsMenu(menu) | this.mFragments.dispatchPrepareOptionsMenu(menu);
        }
        return true;
    }

    @Override // android.view.Window.Callback
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == 8) {
            initWindowDecorActionBar();
            ActionBar actionBar = this.mActionBar;
            if (actionBar != null) {
                actionBar.dispatchMenuVisibilityChanged(true);
            } else {
                Log.e(TAG, "Tried to open action bar menu with no action bar");
            }
        }
        return true;
    }

    @Override // android.view.Window.Callback
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        ActionBar actionBar;
        CharSequence titleCondensed = item.getTitleCondensed();
        if (featureId == 0) {
            if (titleCondensed != null) {
                EventLog.writeEvent(50000, 0, titleCondensed.toString());
            }
            if (onOptionsItemSelected(item) || this.mFragments.dispatchOptionsItemSelected(item)) {
                return true;
            }
            if (item.getItemId() != 16908332 || (actionBar = this.mActionBar) == null || (actionBar.getDisplayOptions() & 4) == 0) {
                return false;
            }
            Activity activity = this.mParent;
            if (activity == null) {
                return onNavigateUp();
            }
            return activity.onNavigateUpFromChild(this);
        } else if (featureId != 6) {
            return false;
        } else {
            if (titleCondensed != null) {
                EventLog.writeEvent(50000, 1, titleCondensed.toString());
            }
            if (onContextItemSelected(item)) {
                return true;
            }
            return this.mFragments.dispatchContextItemSelected(item);
        }
    }

    @Override // android.view.Window.Callback
    public void onPanelClosed(int featureId, Menu menu) {
        if (featureId == 0) {
            this.mFragments.dispatchOptionsMenuClosed(menu);
            onOptionsMenuClosed(menu);
        } else if (featureId == 6) {
            onContextMenuClosed(menu);
        } else if (featureId == 8) {
            initWindowDecorActionBar();
            this.mActionBar.dispatchMenuVisibilityChanged(false);
        }
    }

    public void invalidateOptionsMenu() {
        if (this.mWindow.hasFeature(0)) {
            ActionBar actionBar = this.mActionBar;
            if (actionBar == null || !actionBar.invalidateOptionsMenu()) {
                this.mWindow.invalidatePanelMenu(0);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        Activity activity = this.mParent;
        if (activity != null) {
            return activity.onCreateOptionsMenu(menu);
        }
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        Activity activity = this.mParent;
        if (activity != null) {
            return activity.onPrepareOptionsMenu(menu);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Activity activity = this.mParent;
        if (activity != null) {
            return activity.onOptionsItemSelected(item);
        }
        return false;
    }

    public boolean onNavigateUp() {
        Intent upIntent = getParentActivityIntent();
        if (upIntent == null) {
            return false;
        }
        if (this.mActivityInfo.taskAffinity == null) {
            finish();
            return true;
        } else if (shouldUpRecreateTask(upIntent)) {
            TaskStackBuilder b = TaskStackBuilder.create(this);
            onCreateNavigateUpTaskStack(b);
            onPrepareNavigateUpTaskStack(b);
            b.startActivities();
            if (this.mResultCode == 0 && this.mResultData == null) {
                finishAffinity();
                return true;
            }
            Log.i(TAG, "onNavigateUp only finishing topmost activity to return a result");
            finish();
            return true;
        } else {
            navigateUpTo(upIntent);
            return true;
        }
    }

    public boolean onNavigateUpFromChild(Activity child) {
        return onNavigateUp();
    }

    public void onCreateNavigateUpTaskStack(TaskStackBuilder builder) {
        builder.addParentStack(this);
    }

    public void onPrepareNavigateUpTaskStack(TaskStackBuilder builder) {
    }

    public void onOptionsMenuClosed(Menu menu) {
        Activity activity = this.mParent;
        if (activity != null) {
            activity.onOptionsMenuClosed(menu);
        }
    }

    public void openOptionsMenu() {
        if (this.mWindow.hasFeature(0)) {
            ActionBar actionBar = this.mActionBar;
            if (actionBar == null || !actionBar.openOptionsMenu()) {
                this.mWindow.openPanel(0, null);
            }
        }
    }

    public void closeOptionsMenu() {
        if (this.mWindow.hasFeature(0)) {
            ActionBar actionBar = this.mActionBar;
            if (actionBar == null || !actionBar.closeOptionsMenu()) {
                this.mWindow.closePanel(0);
            }
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

    public void closeContextMenu() {
        if (this.mWindow.hasFeature(6)) {
            this.mWindow.closePanel(6);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        Activity activity = this.mParent;
        if (activity != null) {
            return activity.onContextItemSelected(item);
        }
        return false;
    }

    public void onContextMenuClosed(Menu menu) {
        Activity activity = this.mParent;
        if (activity != null) {
            activity.onContextMenuClosed(menu);
        }
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public Dialog onCreateDialog(int id) {
        return null;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public Dialog onCreateDialog(int id, Bundle args) {
        return onCreateDialog(id);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void onPrepareDialog(int id, Dialog dialog) {
        dialog.setOwnerActivity(this);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        onPrepareDialog(id, dialog);
    }

    @Deprecated
    public final void showDialog(int id) {
        showDialog(id, null);
    }

    @Deprecated
    public final boolean showDialog(int id, Bundle args) {
        if (this.mManagedDialogs == null) {
            this.mManagedDialogs = new SparseArray<>();
        }
        ManagedDialog md = this.mManagedDialogs.get(id);
        if (md == null) {
            md = new ManagedDialog();
            md.mDialog = createDialog(Integer.valueOf(id), null, args);
            if (md.mDialog == null) {
                return false;
            }
            this.mManagedDialogs.put(id, md);
        }
        md.mArgs = args;
        onPrepareDialog(id, md.mDialog, args);
        md.mDialog.show();
        return true;
    }

    @Deprecated
    public final void dismissDialog(int id) {
        SparseArray<ManagedDialog> sparseArray = this.mManagedDialogs;
        if (sparseArray != null) {
            ManagedDialog md = sparseArray.get(id);
            if (md != null) {
                md.mDialog.dismiss();
                return;
            }
            throw missingDialog(id);
        }
        throw missingDialog(id);
    }

    private IllegalArgumentException missingDialog(int id) {
        return new IllegalArgumentException("no dialog with id " + id + " was ever shown via Activity#showDialog");
    }

    @Deprecated
    public final void removeDialog(int id) {
        ManagedDialog md;
        SparseArray<ManagedDialog> sparseArray = this.mManagedDialogs;
        if (sparseArray != null && (md = sparseArray.get(id)) != null) {
            md.mDialog.dismiss();
            this.mManagedDialogs.remove(id);
        }
    }

    @Override // android.view.Window.Callback
    public boolean onSearchRequested(SearchEvent searchEvent) {
        this.mSearchEvent = searchEvent;
        boolean result = onSearchRequested();
        this.mSearchEvent = null;
        return result;
    }

    @Override // android.view.Window.Callback
    public boolean onSearchRequested() {
        int uiMode = getResources().getConfiguration().uiMode & 15;
        if (uiMode == 4 || uiMode == 6) {
            return false;
        }
        startSearch(null, false, null, false);
        return true;
    }

    public final SearchEvent getSearchEvent() {
        return this.mSearchEvent;
    }

    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData, boolean globalSearch) {
        ensureSearchManager();
        this.mSearchManager.startSearch(initialQuery, selectInitialQuery, getComponentName(), appSearchData, globalSearch);
    }

    public void triggerSearch(String query, Bundle appSearchData) {
        ensureSearchManager();
        this.mSearchManager.triggerSearch(query, getComponentName(), appSearchData);
    }

    public void takeKeyEvents(boolean get) {
        getWindow().takeKeyEvents(get);
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

    public MenuInflater getMenuInflater() {
        if (this.mMenuInflater == null) {
            initWindowDecorActionBar();
            ActionBar actionBar = this.mActionBar;
            if (actionBar != null) {
                this.mMenuInflater = new MenuInflater(actionBar.getThemedContext(), this);
            } else {
                this.mMenuInflater = new MenuInflater(this);
            }
        }
        return this.mMenuInflater;
    }

    @Override // android.view.ContextThemeWrapper, android.content.ContextWrapper, android.content.Context
    public void setTheme(int resid) {
        super.setTheme(resid);
        this.mWindow.setTheme(resid);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ContextThemeWrapper
    public void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        int colorPrimary;
        Activity activity = this.mParent;
        if (activity == null) {
            super.onApplyThemeResource(theme, resid, first);
        } else {
            try {
                theme.setTo(activity.getTheme());
            } catch (Exception e) {
            }
            theme.applyStyle(resid, false);
        }
        TypedArray a = theme.obtainStyledAttributes(R.styleable.ActivityTaskDescription);
        if (this.mTaskDescription.getPrimaryColor() == 0 && (colorPrimary = a.getColor(1, 0)) != 0 && Color.alpha(colorPrimary) == 255) {
            this.mTaskDescription.setPrimaryColor(colorPrimary);
        }
        int colorBackground = a.getColor(0, 0);
        if (colorBackground != 0 && Color.alpha(colorBackground) == 255) {
            this.mTaskDescription.setBackgroundColor(colorBackground);
        }
        int statusBarColor = a.getColor(2, 0);
        if (statusBarColor != 0) {
            this.mTaskDescription.setStatusBarColor(statusBarColor);
        }
        int navigationBarColor = a.getColor(3, 0);
        if (navigationBarColor != 0) {
            this.mTaskDescription.setNavigationBarColor(navigationBarColor);
        }
        if (!(getApplicationInfo().targetSdkVersion < 29)) {
            this.mTaskDescription.setEnsureStatusBarContrastWhenTransparent(a.getBoolean(4, false));
            this.mTaskDescription.setEnsureNavigationBarContrastWhenTransparent(a.getBoolean(5, true));
        }
        a.recycle();
        setTaskDescription(this.mTaskDescription);
    }

    public final void requestPermissions(String[] permissions, int requestCode) {
        if (requestCode < 0) {
            throw new IllegalArgumentException("requestCode should be >= 0");
        } else if (this.mHasCurrentPermissionsRequest) {
            Log.w(TAG, "Can request only one set of permissions at a time");
            onRequestPermissionsResult(requestCode, new String[0], new int[0]);
        } else {
            startActivityForResult(REQUEST_PERMISSIONS_WHO_PREFIX, getPackageManager().buildRequestPermissionsIntent(permissions), requestCode, null);
            this.mHasCurrentPermissionsRequest = true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    }

    public boolean shouldShowRequestPermissionRationale(String permission) {
        return getPackageManager().shouldShowRequestPermissionRationale(permission);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, null);
    }

    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity == null || !hwCustSplitActivity.isRequestSplit(this) || !this.mCustActivity.handleStartSplitActivity(intent)) {
            Activity activity = this.mParent;
            if (activity == null) {
                Bundle options2 = HwActivityTaskManager.hookStartActivityOptions(this, HwPCUtils.hookStartActivityOptions(this, transferSpringboardActivityOptions(options)));
                if (!HwSystemManager.canStartActivity(getApplicationContext(), intent)) {
                    Log.i(TAG, "this app not allowed to StartActivity:" + intent);
                    return;
                }
                Instrumentation.ActivityResult ar = this.mInstrumentation.execStartActivity(this, this.mMainThread.getApplicationThread(), this.mToken, this, intent, requestCode, options2);
                if (ar != null) {
                    this.mMainThread.sendActivityResult(this.mToken, this.mEmbeddedID, requestCode, ar.getResultCode(), ar.getResultData());
                }
                if (requestCode >= 0) {
                    this.mStartedActivity = true;
                }
                if (HwPCUtils.isValidExtDisplayId(this)) {
                    try {
                        cancelInputsAndStartExitTransition(options2);
                    } catch (IllegalArgumentException e) {
                        HwPCUtils.log(TAG, "fail to cancelInputsAndStartExitTransition");
                    }
                } else {
                    cancelInputsAndStartExitTransition(options2);
                }
            } else if (options != null) {
                activity.startActivityFromChild(this, intent, requestCode, options);
            } else {
                activity.startActivityFromChild(this, intent, requestCode);
            }
        }
    }

    private void cancelInputsAndStartExitTransition(Bundle options) {
        Window window = this.mWindow;
        View decor = window != null ? window.peekDecorView() : null;
        if (decor != null) {
            decor.cancelPendingInputEvents();
        }
        if (options != null) {
            this.mActivityTransitionState.startExitOutTransition(this, options);
        }
    }

    public boolean isActivityTransitionRunning() {
        return this.mActivityTransitionState.isTransitionRunning();
    }

    private Bundle transferSpringboardActivityOptions(Bundle options) {
        Window window;
        ActivityOptions activityOptions;
        if (options != null || (window = this.mWindow) == null || window.isActive() || (activityOptions = getActivityOptions()) == null || activityOptions.getAnimationType() != 5) {
            return options;
        }
        return activityOptions.toBundle();
    }

    @UnsupportedAppUsage
    public void startActivityForResultAsUser(Intent intent, int requestCode, UserHandle user) {
        startActivityForResultAsUser(intent, requestCode, null, user);
    }

    public void startActivityForResultAsUser(Intent intent, int requestCode, Bundle options, UserHandle user) {
        startActivityForResultAsUser(intent, this.mEmbeddedID, requestCode, options, user);
    }

    public void startActivityForResultAsUser(Intent intent, String resultWho, int requestCode, Bundle options, UserHandle user) {
        if (this.mParent == null) {
            Bundle options2 = HwActivityTaskManager.hookStartActivityOptions(this, HwPCUtils.hookStartActivityOptions(this, transferSpringboardActivityOptions(options)));
            HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
            if (hwCustSplitActivity == null || !hwCustSplitActivity.isRequestSplit(this) || !this.mCustActivity.handleStartSplitActivity(intent)) {
                Instrumentation.ActivityResult ar = this.mInstrumentation.execStartActivity(this, this.mMainThread.getApplicationThread(), this.mToken, resultWho, intent, requestCode, options2, user);
                if (ar != null) {
                    this.mMainThread.sendActivityResult(this.mToken, this.mEmbeddedID, requestCode, ar.getResultCode(), ar.getResultData());
                }
                if (requestCode >= 0) {
                    this.mStartedActivity = true;
                }
                cancelInputsAndStartExitTransition(options2);
                return;
            }
            return;
        }
        throw new RuntimeException("Can't be called from a child");
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void startActivityAsUser(Intent intent, UserHandle user) {
        startActivityAsUser(intent, null, user);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void startActivityAsUser(Intent intent, Bundle options, UserHandle user) {
        if (this.mParent == null) {
            Bundle options2 = HwActivityTaskManager.hookStartActivityOptions(this, HwPCUtils.hookStartActivityOptions(this, transferSpringboardActivityOptions(options)));
            HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
            if (hwCustSplitActivity == null || !hwCustSplitActivity.isRequestSplit(this) || !this.mCustActivity.handleStartSplitActivity(intent)) {
                Instrumentation.ActivityResult ar = this.mInstrumentation.execStartActivity(this, this.mMainThread.getApplicationThread(), this.mToken, this.mEmbeddedID, intent, -1, options2, user);
                if (ar != null) {
                    this.mMainThread.sendActivityResult(this.mToken, this.mEmbeddedID, -1, ar.getResultCode(), ar.getResultData());
                }
                cancelInputsAndStartExitTransition(options2);
                return;
            }
            return;
        }
        throw new RuntimeException("Can't be called from a child");
    }

    public void startActivityAsCaller(Intent intent, Bundle options, IBinder permissionToken, boolean ignoreTargetSecurity, int userId) {
        if (this.mParent == null) {
            Bundle options2 = HwActivityTaskManager.hookStartActivityOptions(this, HwPCUtils.hookStartActivityOptions(this, transferSpringboardActivityOptions(options)));
            HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
            if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
                if (this.mCustActivity.handleStartSplitActivity(intent)) {
                    return;
                }
            }
            Instrumentation.ActivityResult ar = this.mInstrumentation.execStartActivityAsCaller(this, this.mMainThread.getApplicationThread(), this.mToken, this, intent, -1, options2, permissionToken, ignoreTargetSecurity, userId);
            if (ar != null) {
                this.mMainThread.sendActivityResult(this.mToken, this.mEmbeddedID, -1, ar.getResultCode(), ar.getResultData());
            }
            cancelInputsAndStartExitTransition(options2);
            return;
        }
        throw new RuntimeException("Can't be called from a child");
    }

    public void startIntentSenderForResult(IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, null);
    }

    public void startIntentSenderForResult(IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
        Activity activity = this.mParent;
        if (activity == null) {
            startIntentSenderForResultInner(intent, this.mEmbeddedID, requestCode, fillInIntent, flagsMask, flagsValues, options);
        } else if (options != null) {
            activity.startIntentSenderFromChild(this, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
        } else {
            activity.startIntentSenderFromChild(this, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startIntentSenderForResultInner(IntentSender intent, String who, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, Bundle options) throws IntentSender.SendIntentException {
        try {
            Bundle options2 = transferSpringboardActivityOptions(options);
            String resolvedType = null;
            if (fillInIntent != null) {
                try {
                    fillInIntent.migrateExtraStreamToClipData();
                    fillInIntent.prepareToLeaveProcess(this);
                    resolvedType = fillInIntent.resolveTypeIfNeeded(getContentResolver());
                } catch (RemoteException e) {
                }
            }
            int result = ActivityTaskManager.getService().startActivityIntentSender(this.mMainThread.getApplicationThread(), intent != null ? intent.getTarget() : null, intent != null ? intent.getWhitelistToken() : null, fillInIntent, resolvedType, this.mToken, who, requestCode, flagsMask, flagsValues, options2);
            if (result != -96) {
                Instrumentation.checkStartActivityResult(result, null);
                if (options2 != null) {
                    cancelInputsAndStartExitTransition(options2);
                }
                if (requestCode >= 0) {
                    this.mStartedActivity = true;
                    return;
                }
                return;
            }
            throw new IntentSender.SendIntentException();
        } catch (RemoteException e2) {
        }
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void startActivity(Intent intent) {
        startActivity(intent, null);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void startActivity(Intent intent, Bundle options) {
        if (options != null) {
            startActivityForResult(intent, -1, options);
        } else {
            startActivityForResult(intent, -1);
        }
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void startActivities(Intent[] intents) {
        startActivities(intents, null);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void startActivities(Intent[] intents, Bundle options) {
        this.mInstrumentation.execStartActivities(this, this.mMainThread.getApplicationThread(), this.mToken, this, intents, HwActivityTaskManager.hookStartActivityOptions(this, HwPCUtils.hookStartActivityOptions(this, options)));
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, null);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
        if (options != null) {
            startIntentSenderForResult(intent, -1, fillInIntent, flagsMask, flagsValues, extraFlags, options);
        } else {
            startIntentSenderForResult(intent, -1, fillInIntent, flagsMask, flagsValues, extraFlags);
        }
    }

    public boolean startActivityIfNeeded(Intent intent, int requestCode) {
        return startActivityIfNeeded(intent, requestCode, null);
    }

    public boolean startActivityIfNeeded(Intent intent, int requestCode, Bundle options) {
        if (this.mParent == null) {
            int result = 1;
            try {
                Uri referrer = onProvideReferrer();
                if (referrer != null) {
                    intent.putExtra(Intent.EXTRA_REFERRER, referrer);
                }
                intent.migrateExtraStreamToClipData();
                intent.prepareToLeaveProcess(this);
                long start = SystemClock.uptimeMillis();
                result = ActivityTaskManager.getService().startActivity(this.mMainThread.getApplicationThread(), getBasePackageName(), intent, intent.resolveTypeIfNeeded(getContentResolver()), this.mToken, this.mEmbeddedID, requestCode, 1, null, options);
                Jlog.printStartActivityInfo(intent, start, result);
            } catch (RemoteException e) {
            }
            Instrumentation.checkStartActivityResult(result, intent);
            if (requestCode >= 0) {
                this.mStartedActivity = true;
            }
            if (result != 1) {
                return true;
            }
            return false;
        }
        throw new UnsupportedOperationException("startActivityIfNeeded can only be called from a top-level activity");
    }

    public boolean startNextMatchingActivity(Intent intent) {
        return startNextMatchingActivity(intent, null);
    }

    public boolean startNextMatchingActivity(Intent intent, Bundle options) {
        if (this.mParent == null) {
            try {
                intent.migrateExtraStreamToClipData();
                intent.prepareToLeaveProcess(this);
                return ActivityTaskManager.getService().startNextMatchingActivity(this.mToken, intent, options);
            } catch (RemoteException e) {
                return false;
            }
        } else {
            throw new UnsupportedOperationException("startNextMatchingActivity can only be called from a top-level activity");
        }
    }

    public void startActivityFromChild(Activity child, Intent intent, int requestCode) {
        startActivityFromChild(child, intent, requestCode, null);
    }

    public void startActivityFromChild(Activity child, Intent intent, int requestCode, Bundle options) {
        Bundle options2 = HwActivityTaskManager.hookStartActivityOptions(this, HwPCUtils.hookStartActivityOptions(this, transferSpringboardActivityOptions(options)));
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this) && this.mCustActivity.handleStartSplitActivity(intent)) {
            return;
        }
        if (!HwSystemManager.canStartActivity(getApplicationContext(), intent)) {
            Log.i(TAG, "this app not allowed to start activity:" + intent);
            return;
        }
        Instrumentation.ActivityResult ar = this.mInstrumentation.execStartActivity(this, this.mMainThread.getApplicationThread(), this.mToken, child, intent, requestCode, options2);
        if (ar != null) {
            this.mMainThread.sendActivityResult(this.mToken, child.mEmbeddedID, requestCode, ar.getResultCode(), ar.getResultData());
        }
        cancelInputsAndStartExitTransition(options2);
    }

    @Deprecated
    public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
        startActivityFromFragment(fragment, intent, requestCode, null);
    }

    @Deprecated
    public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options) {
        if (!HwSystemManager.canStartActivity(getApplicationContext(), intent)) {
            Log.i(TAG, "this app not allowed to start activity:" + intent);
            return;
        }
        startActivityForResult(fragment.mWho, intent, requestCode, options);
    }

    public void startActivityAsUserFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options, UserHandle user) {
        startActivityForResultAsUser(intent, fragment.mWho, requestCode, options, user);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    @UnsupportedAppUsage
    public void startActivityForResult(String who, Intent intent, int requestCode, Bundle options) {
        Uri referrer = onProvideReferrer();
        if (referrer != null) {
            intent.putExtra(Intent.EXTRA_REFERRER, referrer);
        }
        Bundle options2 = HwPCUtils.hookStartActivityOptions(this, transferSpringboardActivityOptions(options));
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity == null || !hwCustSplitActivity.isRequestSplit(this) || !this.mCustActivity.handleStartSplitActivity(intent)) {
            Instrumentation.ActivityResult ar = this.mInstrumentation.execStartActivity(this, this.mMainThread.getApplicationThread(), this.mToken, who, intent, requestCode, options2);
            if (ar != null) {
                this.mMainThread.sendActivityResult(this.mToken, who, requestCode, ar.getResultCode(), ar.getResultData());
            }
            cancelInputsAndStartExitTransition(options2);
        }
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public boolean canStartActivityForResult() {
        return true;
    }

    public void startIntentSenderFromChild(Activity child, IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        startIntentSenderFromChild(child, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, null);
    }

    public void startIntentSenderFromChild(Activity child, IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
        startIntentSenderForResultInner(intent, child.mEmbeddedID, requestCode, fillInIntent, flagsMask, flagsValues, options);
    }

    public void startIntentSenderFromChildFragment(Fragment child, IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
        startIntentSenderForResultInner(intent, child.mWho, requestCode, fillInIntent, flagsMask, flagsValues, options);
    }

    public void overridePendingTransition(int enterAnim, int exitAnim) {
        try {
            ActivityTaskManager.getService().overridePendingTransition(this.mToken, getPackageName(), enterAnim, exitAnim);
        } catch (RemoteException e) {
        }
    }

    public final void setResult(int resultCode) {
        synchronized (this) {
            this.mResultCode = resultCode;
            this.mResultData = null;
        }
    }

    public final void setResult(int resultCode, Intent data) {
        synchronized (this) {
            this.mResultCode = resultCode;
            this.mResultData = data;
        }
    }

    public Uri getReferrer() {
        Intent intent = getIntent();
        try {
            Uri referrer = (Uri) intent.getParcelableExtra(Intent.EXTRA_REFERRER);
            if (referrer != null) {
                return referrer;
            }
            String referrerName = intent.getStringExtra(Intent.EXTRA_REFERRER_NAME);
            if (referrerName != null) {
                return Uri.parse(referrerName);
            }
            if (this.mReferrer != null) {
                return new Uri.Builder().scheme("android-app").authority(this.mReferrer).build();
            }
            return null;
        } catch (BadParcelableException e) {
            Log.w(TAG, "Cannot read referrer from intent; intent extras contain unknown custom Parcelable objects");
        }
    }

    public Uri onProvideReferrer() {
        return null;
    }

    public String getCallingPackage() {
        try {
            return ActivityTaskManager.getService().getCallingPackage(this.mToken);
        } catch (RemoteException e) {
            return null;
        }
    }

    public ComponentName getCallingActivity() {
        try {
            return ActivityTaskManager.getService().getCallingActivity(this.mToken);
        } catch (RemoteException e) {
            return null;
        }
    }

    public void setVisible(boolean visible) {
        if (this.mVisibleFromClient != visible) {
            this.mVisibleFromClient = visible;
            if (!this.mVisibleFromServer) {
                return;
            }
            if (visible) {
                makeVisible();
            } else {
                this.mDecor.setVisibility(4);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void makeVisible() {
        if (!this.mWindowAdded) {
            getWindowManager().addView(this.mDecor, getWindow().getAttributes());
            this.mWindowAdded = true;
        }
        this.mDecor.setVisibility(0);
    }

    public boolean isFinishing() {
        return this.mFinished;
    }

    public boolean isDestroyed() {
        return this.mDestroyed;
    }

    public boolean isChangingConfigurations() {
        return this.mChangingConfigurations;
    }

    public void recreate() {
        if (this.mParent != null) {
            throw new IllegalStateException("Can only be called on top-level activity");
        } else if (Looper.myLooper() == this.mMainThread.getLooper()) {
            this.mMainThread.scheduleRelaunchActivity(this.mToken);
        } else {
            throw new IllegalStateException("Must be called from main thread");
        }
    }

    @UnsupportedAppUsage
    private void finish(int finishTask) {
        int resultCode;
        Intent resultData;
        Activity activity = this.mParent;
        if (activity == null) {
            synchronized (this) {
                resultCode = this.mResultCode;
                resultData = this.mResultData;
            }
            if (resultData != null) {
                try {
                    resultData.prepareToLeaveProcess(this);
                } catch (RemoteException e) {
                }
            }
            if (ActivityTaskManager.getService().finishActivity(this.mToken, resultCode, resultData, finishTask)) {
                this.mFinished = true;
            }
        } else {
            activity.finishFromChild(this);
        }
        try {
            if (this.mIntent != null && this.mIntent.hasExtra(AutofillManager.EXTRA_RESTORE_SESSION_TOKEN)) {
                getAutofillManager().onPendingSaveUi(2, this.mIntent.getIBinderExtra(AutofillManager.EXTRA_RESTORE_SESSION_TOKEN));
            }
        } catch (RuntimeException e2) {
            Log.e(TAG, " finish parcel err!");
        }
    }

    public void finish() {
        finish(0);
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
            this.mCustActivity.splitActivityFinish();
        }
    }

    public void finishAffinity() {
        if (this.mParent != null) {
            throw new IllegalStateException("Can not be called from an embedded activity");
        } else if (this.mResultCode == 0 && this.mResultData == null) {
            try {
                if (ActivityTaskManager.getService().finishActivityAffinity(this.mToken)) {
                    this.mFinished = true;
                }
            } catch (RemoteException e) {
            }
        } else {
            throw new IllegalStateException("Can not be called to deliver a result");
        }
    }

    public void finishFromChild(Activity child) {
        finish();
    }

    public void finishAfterTransition() {
        if (!this.mActivityTransitionState.startExitBackTransition(this)) {
            finish();
        }
    }

    public void finishActivity(int requestCode) {
        Activity activity = this.mParent;
        if (activity == null) {
            try {
                ActivityTaskManager.getService().finishSubActivity(this.mToken, this.mEmbeddedID, requestCode);
            } catch (RemoteException e) {
            }
        } else {
            activity.finishActivityFromChild(this, requestCode);
        }
    }

    public void finishActivityFromChild(Activity child, int requestCode) {
        try {
            ActivityTaskManager.getService().finishSubActivity(this.mToken, child.mEmbeddedID, requestCode);
        } catch (RemoteException e) {
        }
    }

    public void finishAndRemoveTask() {
        finish(1);
    }

    public boolean releaseInstance() {
        try {
            return ActivityTaskManager.getService().releaseActivityInstance(this.mToken);
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public void onActivityReenter(int resultCode, Intent data) {
    }

    public PendingIntent createPendingResult(int requestCode, Intent data, int flags) {
        String packageName = getPackageName();
        try {
            data.prepareToLeaveProcess(this);
            IIntentSender target = ActivityManager.getService().getIntentSender(3, packageName, this.mParent == null ? this.mToken : this.mParent.mToken, this.mEmbeddedID, requestCode, new Intent[]{data}, null, flags, null, getUserId());
            if (target != null) {
                return new PendingIntent(target);
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public void setRequestedOrientation(int requestedOrientation) {
        Activity activity = this.mParent;
        if (activity == null) {
            try {
                ActivityTaskManager.getService().setRequestedOrientation(this.mToken, requestedOrientation);
            } catch (RemoteException e) {
            }
        } else {
            activity.setRequestedOrientation(requestedOrientation);
        }
        HwCustSplitActivity hwCustSplitActivity = this.mCustActivity;
        if (hwCustSplitActivity != null && hwCustSplitActivity.isRequestSplit(this)) {
            this.mCustActivity.setSplitActivityOrientation(requestedOrientation);
        }
    }

    public int getRequestedOrientation() {
        Activity activity = this.mParent;
        if (activity != null) {
            return activity.getRequestedOrientation();
        }
        try {
            return ActivityTaskManager.getService().getRequestedOrientation(this.mToken);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public int getTaskId() {
        try {
            return ActivityTaskManager.getService().getTaskForActivity(this.mToken, false);
        } catch (RemoteException e) {
            return -1;
        }
    }

    @Override // android.view.Window.WindowControllerCallback
    public boolean isTaskRoot() {
        try {
            return ActivityTaskManager.getService().getTaskForActivity(this.mToken, true) >= 0;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean moveTaskToBack(boolean nonRoot) {
        try {
            return ActivityTaskManager.getService().moveActivityTaskToBack(this.mToken, nonRoot);
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getLocalClassName() {
        String pkg = getPackageName();
        String cls = this.mComponent.getClassName();
        int packageLen = pkg.length();
        if (!cls.startsWith(pkg) || cls.length() <= packageLen || cls.charAt(packageLen) != '.') {
            return cls;
        }
        return cls.substring(packageLen + 1);
    }

    public ComponentName getComponentName() {
        return this.mComponent;
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final ComponentName autofillClientGetComponentName() {
        return getComponentName();
    }

    @Override // android.view.contentcapture.ContentCaptureManager.ContentCaptureClient
    public final ComponentName contentCaptureClientGetComponentName() {
        return getComponentName();
    }

    public SharedPreferences getPreferences(int mode) {
        return getSharedPreferences(getLocalClassName(), mode);
    }

    private void ensureSearchManager() {
        if (this.mSearchManager == null) {
            try {
                this.mSearchManager = new SearchManager(this, null);
            } catch (ServiceManager.ServiceNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override // android.view.ContextThemeWrapper, android.content.ContextWrapper, android.content.Context
    public Object getSystemService(String name) {
        if (getBaseContext() == null) {
            throw new IllegalStateException("System services not available to Activities before onCreate()");
        } else if ("window".equals(name)) {
            return this.mWindowManager;
        } else {
            if (!"search".equals(name)) {
                return super.getSystemService(name);
            }
            ensureSearchManager();
            return this.mSearchManager;
        }
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        onTitleChanged(title, this.mTitleColor);
        Activity activity = this.mParent;
        if (activity != null) {
            activity.onChildTitleChanged(this, title);
        }
    }

    public void setTitle(int titleId) {
        setTitle(getText(titleId));
    }

    @Deprecated
    public void setTitleColor(int textColor) {
        this.mTitleColor = textColor;
        onTitleChanged(this.mTitle, textColor);
    }

    public final CharSequence getTitle() {
        return this.mTitle;
    }

    public final int getTitleColor() {
        return this.mTitleColor;
    }

    /* access modifiers changed from: protected */
    public void onTitleChanged(CharSequence title, int color) {
        if (this.mTitleReady) {
            Window win = getWindow();
            if (win != null) {
                win.setTitle(title);
                if (color != 0) {
                    win.setTitleColor(color);
                }
            }
            ActionBar actionBar = this.mActionBar;
            if (actionBar != null) {
                actionBar.setWindowTitle(title);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onChildTitleChanged(Activity childActivity, CharSequence title) {
    }

    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
        ActivityManager.TaskDescription taskDescription2 = this.mTaskDescription;
        if (taskDescription2 != taskDescription) {
            taskDescription2.copyFromPreserveHiddenFields(taskDescription);
            if (taskDescription.getIconFilename() == null && taskDescription.getIcon() != null) {
                int size = ActivityManager.getLauncherLargeIconSizeInner(this);
                this.mTaskDescription.setIcon(Bitmap.createScaledBitmap(taskDescription.getIcon(), size, size, true));
            }
        }
        try {
            ActivityTaskManager.getService().setTaskDescription(this.mToken, this.mTaskDescription);
        } catch (RemoteException e) {
        }
    }

    @Deprecated
    public final void setProgressBarVisibility(boolean visible) {
        int i;
        Window window = getWindow();
        if (visible) {
            i = -1;
        } else {
            i = -2;
        }
        window.setFeatureInt(2, i);
    }

    @Deprecated
    public final void setProgressBarIndeterminateVisibility(boolean visible) {
        getWindow().setFeatureInt(5, visible ? -1 : -2);
    }

    @Deprecated
    public final void setProgressBarIndeterminate(boolean indeterminate) {
        int i;
        Window window = getWindow();
        if (indeterminate) {
            i = -3;
        } else {
            i = -4;
        }
        window.setFeatureInt(2, i);
    }

    @Deprecated
    public final void setProgress(int progress) {
        getWindow().setFeatureInt(2, progress + 0);
    }

    @Deprecated
    public final void setSecondaryProgress(int secondaryProgress) {
        getWindow().setFeatureInt(2, secondaryProgress + 20000);
    }

    public final void setVolumeControlStream(int streamType) {
        getWindow().setVolumeControlStream(streamType);
    }

    public final int getVolumeControlStream() {
        return getWindow().getVolumeControlStream();
    }

    public final void setMediaController(MediaController controller) {
        getWindow().setMediaController(controller);
    }

    public final MediaController getMediaController() {
        return getWindow().getMediaController();
    }

    public final void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != this.mUiThread) {
            this.mHandler.post(action);
        } else {
            action.run();
        }
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final void autofillClientRunOnUiThread(Runnable action) {
        runOnUiThread(action);
    }

    @Override // android.view.LayoutInflater.Factory
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }

    @Override // android.view.LayoutInflater.Factory2
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (!"fragment".equals(name)) {
            return onCreateView(name, context, attrs);
        }
        return this.mFragments.onCreateView(parent, name, context, attrs);
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        dumpInner(prefix, fd, writer, args);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001f, code lost:
        if (r1.equals("--autofill") == false) goto L_0x002c;
     */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x002f  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0036  */
    public void dumpInner(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        if (args != null && args.length > 0) {
            boolean z = false;
            String str = args[0];
            int hashCode = str.hashCode();
            if (hashCode != 1159329357) {
                if (hashCode == 1455016274) {
                }
            } else if (str.equals("--contentcapture")) {
                z = true;
                if (z) {
                    dumpAutofillManager(prefix, writer);
                    return;
                } else if (z) {
                    dumpContentCaptureManager(prefix, writer);
                    return;
                }
            }
            z = true;
            if (z) {
            }
        }
        writer.print(prefix);
        writer.print("Local Activity ");
        writer.print(Integer.toHexString(System.identityHashCode(this)));
        writer.println(" State:");
        String innerPrefix = prefix + "  ";
        writer.print(innerPrefix);
        writer.print("mResumed=");
        writer.print(this.mResumed);
        writer.print(" mStopped=");
        writer.print(this.mStopped);
        writer.print(" mFinished=");
        writer.println(this.mFinished);
        writer.print(innerPrefix);
        writer.print("mChangingConfigurations=");
        writer.println(this.mChangingConfigurations);
        writer.print(innerPrefix);
        writer.print("mCurrentConfig=");
        writer.println(this.mCurrentConfig);
        this.mFragments.dumpLoaders(innerPrefix, fd, writer, args);
        this.mFragments.getFragmentManager().dump(innerPrefix, fd, writer, args);
        VoiceInteractor voiceInteractor = this.mVoiceInteractor;
        if (voiceInteractor != null) {
            voiceInteractor.dump(innerPrefix, fd, writer, args);
        }
        if (!(getWindow() == null || getWindow().peekDecorView() == null || getWindow().peekDecorView().getViewRootImpl() == null)) {
            getWindow().peekDecorView().getViewRootImpl().dump(prefix, fd, writer, args);
        }
        this.mHandler.getLooper().dump(new PrintWriterPrinter(writer), prefix);
        dumpAutofillManager(prefix, writer);
        dumpContentCaptureManager(prefix, writer);
        ResourcesManager.getInstance().dump(prefix, writer);
    }

    /* access modifiers changed from: package-private */
    public void dumpAutofillManager(String prefix, PrintWriter writer) {
        AutofillManager afm = getAutofillManager();
        if (afm != null) {
            afm.dump(prefix, writer);
            writer.print(prefix);
            writer.print("Autofill Compat Mode: ");
            writer.println(isAutofillCompatibilityEnabled());
            return;
        }
        writer.print(prefix);
        writer.println("No AutofillManager");
    }

    /* access modifiers changed from: package-private */
    public void dumpContentCaptureManager(String prefix, PrintWriter writer) {
        ContentCaptureManager cm = getContentCaptureManager();
        if (cm != null) {
            cm.dump(prefix, writer);
            return;
        }
        writer.print(prefix);
        writer.println("No ContentCaptureManager");
    }

    public boolean isImmersive() {
        try {
            return ActivityTaskManager.getService().isImmersive(this.mToken);
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean isTopOfTask() {
        if (this.mToken == null || this.mWindow == null) {
            return false;
        }
        try {
            return ActivityTaskManager.getService().isTopOfTask(getActivityToken());
        } catch (RemoteException e) {
            return false;
        }
    }

    @SystemApi
    public void convertFromTranslucent() {
        try {
            this.mTranslucentCallback = null;
            if (ActivityTaskManager.getService().convertFromTranslucent(this.mToken)) {
                WindowManagerGlobal.getInstance().changeCanvasOpacity(this.mToken, true);
            }
        } catch (RemoteException e) {
        }
    }

    @SystemApi
    public boolean convertToTranslucent(TranslucentConversionListener callback, ActivityOptions options) {
        boolean drawComplete;
        TranslucentConversionListener translucentConversionListener;
        try {
            this.mTranslucentCallback = callback;
            this.mChangeCanvasToTranslucent = ActivityTaskManager.getService().convertToTranslucent(this.mToken, options == null ? null : options.toBundle());
            WindowManagerGlobal.getInstance().changeCanvasOpacity(this.mToken, false);
            drawComplete = true;
        } catch (RemoteException e) {
            this.mChangeCanvasToTranslucent = false;
            drawComplete = false;
        }
        if (!this.mChangeCanvasToTranslucent && (translucentConversionListener = this.mTranslucentCallback) != null) {
            translucentConversionListener.onTranslucentConversionComplete(drawComplete);
        }
        return this.mChangeCanvasToTranslucent;
    }

    /* access modifiers changed from: package-private */
    public void onTranslucentConversionComplete(boolean drawComplete) {
        TranslucentConversionListener translucentConversionListener = this.mTranslucentCallback;
        if (translucentConversionListener != null) {
            translucentConversionListener.onTranslucentConversionComplete(drawComplete);
            this.mTranslucentCallback = null;
        }
        if (this.mChangeCanvasToTranslucent) {
            WindowManagerGlobal.getInstance().changeCanvasOpacity(this.mToken, false);
        }
    }

    public void onNewActivityOptions(ActivityOptions options) {
        this.mActivityTransitionState.setEnterActivityOptions(this, options);
        if (!this.mStopped) {
            this.mActivityTransitionState.enterReady(this);
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public ActivityOptions getActivityOptions() {
        try {
            return ActivityOptions.fromBundle(ActivityTaskManager.getService().getActivityOptions(this.mToken));
        } catch (RemoteException e) {
            return null;
        }
    }

    @Deprecated
    public boolean requestVisibleBehind(boolean visible) {
        return false;
    }

    @Deprecated
    public void onVisibleBehindCanceled() {
        this.mCalled = true;
    }

    @SystemApi
    @Deprecated
    public boolean isBackgroundVisibleBehind() {
        return false;
    }

    @SystemApi
    @Deprecated
    public void onBackgroundVisibleBehindChanged(boolean visible) {
    }

    public void onEnterAnimationComplete() {
    }

    public void dispatchEnterAnimationComplete() {
        this.mEnterAnimationComplete = true;
        this.mInstrumentation.onEnterAnimationComplete();
        onEnterAnimationComplete();
        if (getWindow() != null && getWindow().getDecorView() != null) {
            View decorView = getWindow().getDecorView();
            if ((decorView instanceof DecorView) && WindowConfiguration.isHwMultiStackWindowingMode(((DecorView) decorView).getWindowMode())) {
                decorView.getContext().setAutofillClient(this);
            }
            decorView.getViewTreeObserver().dispatchOnEnterAnimationComplete();
        }
    }

    public void setImmersive(boolean i) {
        try {
            ActivityTaskManager.getService().setImmersive(this.mToken, i);
        } catch (RemoteException e) {
        }
    }

    public void setVrModeEnabled(boolean enabled, ComponentName requestedComponent) throws PackageManager.NameNotFoundException {
        try {
            if (ActivityTaskManager.getService().setVrMode(this.mToken, enabled, requestedComponent) != 0) {
                throw new PackageManager.NameNotFoundException(requestedComponent.flattenToString());
            }
        } catch (RemoteException e) {
        }
    }

    public ActionMode startActionMode(ActionMode.Callback callback) {
        return this.mWindow.getDecorView().startActionMode(callback);
    }

    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return this.mWindow.getDecorView().startActionMode(callback, type);
    }

    @Override // android.view.Window.Callback
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        if (this.mActionModeTypeStarting != 0) {
            return null;
        }
        initWindowDecorActionBar();
        ActionBar actionBar = this.mActionBar;
        if (actionBar != null) {
            return actionBar.startActionMode(callback);
        }
        return null;
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
    }

    @Override // android.view.Window.Callback
    public void onActionModeFinished(ActionMode mode) {
    }

    public boolean shouldUpRecreateTask(Intent targetIntent) {
        try {
            PackageManager pm = getPackageManager();
            ComponentName cn = targetIntent.getComponent();
            if (cn == null) {
                cn = targetIntent.resolveActivity(pm);
            }
            ActivityInfo info = pm.getActivityInfo(cn, 0);
            if (info.taskAffinity == null) {
                return false;
            }
            return ActivityTaskManager.getService().shouldUpRecreateTask(this.mToken, info.taskAffinity);
        } catch (RemoteException e) {
            return false;
        } catch (PackageManager.NameNotFoundException e2) {
            return false;
        }
    }

    public boolean navigateUpTo(Intent upIntent) {
        Intent upIntent2;
        int resultCode;
        Intent resultData;
        Activity activity = this.mParent;
        if (activity != null) {
            return activity.navigateUpToFromChild(this, upIntent);
        }
        if (upIntent.getComponent() == null) {
            ComponentName destInfo = upIntent.resolveActivity(getPackageManager());
            if (destInfo == null) {
                return false;
            }
            upIntent2 = new Intent(upIntent);
            upIntent2.setComponent(destInfo);
        } else {
            upIntent2 = upIntent;
        }
        synchronized (this) {
            resultCode = this.mResultCode;
            resultData = this.mResultData;
        }
        if (resultData != null) {
            resultData.prepareToLeaveProcess(this);
        }
        try {
            upIntent2.prepareToLeaveProcess(this);
            return ActivityTaskManager.getService().navigateUpTo(this.mToken, upIntent2, resultCode, resultData);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean navigateUpToFromChild(Activity child, Intent upIntent) {
        return navigateUpTo(upIntent);
    }

    public Intent getParentActivityIntent() {
        String parentName = this.mActivityInfo.parentActivityName;
        if (TextUtils.isEmpty(parentName)) {
            return null;
        }
        ComponentName target = new ComponentName(this, parentName);
        try {
            if (getPackageManager().getActivityInfo(target, 0).parentActivityName == null) {
                return Intent.makeMainActivity(target);
            }
            return new Intent().setComponent(target);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getParentActivityIntent: bad parentActivityName '" + parentName + "' in manifest");
            return null;
        }
    }

    public void setEnterSharedElementCallback(SharedElementCallback callback) {
        if (callback == null) {
            callback = SharedElementCallback.NULL_CALLBACK;
        }
        this.mEnterTransitionListener = callback;
    }

    public void setExitSharedElementCallback(SharedElementCallback callback) {
        if (callback == null) {
            callback = SharedElementCallback.NULL_CALLBACK;
        }
        this.mExitTransitionListener = callback;
    }

    public void postponeEnterTransition() {
        this.mActivityTransitionState.postponeEnterTransition();
    }

    public void startPostponedEnterTransition() {
        this.mActivityTransitionState.startPostponedEnterTransition();
    }

    public DragAndDropPermissions requestDragAndDropPermissions(DragEvent event) {
        DragAndDropPermissions dragAndDropPermissions = DragAndDropPermissions.obtain(event);
        if (dragAndDropPermissions == null || !dragAndDropPermissions.take(getActivityToken())) {
            return null;
        }
        return dragAndDropPermissions;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public final void setParent(Activity parent) {
        this.mParent = parent;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final void attach(Context context, ActivityThread aThread, Instrumentation instr, IBinder token, int ident, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, NonConfigurationInstances lastNonConfigurationInstances, Configuration config, String referrer, IVoiceInteractor voiceInteractor, Window window, ViewRootImpl.ActivityConfigCallback activityConfigCallback, IBinder assistToken) {
        attachBaseContext(context);
        this.mFragments.attachHost(null);
        this.mWindow = HwPolicyFactory.getHwPhoneWindow(this, window, activityConfigCallback);
        this.mWindow.setWindowControllerCallback(this);
        this.mWindow.setCallback(this);
        this.mWindow.setOnWindowDismissedCallback(this);
        this.mWindow.getLayoutInflater().setPrivateFactory(this);
        if (info.softInputMode != 0) {
            this.mWindow.setSoftInputMode(info.softInputMode);
        }
        if (info.uiOptions != 0) {
            this.mWindow.setUiOptions(info.uiOptions);
        }
        this.mUiThread = Thread.currentThread();
        this.mMainThread = aThread;
        this.mInstrumentation = instr;
        this.mToken = token;
        this.mAssistToken = assistToken;
        this.mIdent = ident;
        this.mApplication = application;
        this.mIntent = intent;
        this.mReferrer = referrer;
        this.mComponent = intent.getComponent();
        this.mActivityInfo = info;
        this.mTitle = title;
        this.mParent = parent;
        this.mEmbeddedID = id;
        this.mLastNonConfigurationInstances = lastNonConfigurationInstances;
        if (voiceInteractor != null) {
            if (lastNonConfigurationInstances != null) {
                this.mVoiceInteractor = lastNonConfigurationInstances.voiceInteractor;
            } else {
                this.mVoiceInteractor = new VoiceInteractor(voiceInteractor, this, this, Looper.myLooper());
            }
        }
        this.mWindow.setWindowManager((WindowManager) context.getSystemService("window"), this.mToken, this.mComponent.flattenToString(), (info.flags & 512) != 0);
        Activity activity = this.mParent;
        if (activity != null) {
            this.mWindow.setContainer(activity.getWindow());
        }
        this.mWindowManager = this.mWindow.getWindowManager();
        this.mCurrentConfig = config;
        this.mWindow.setColorMode(info.colorMode);
        setAutofillOptions(application.getAutofillOptions());
        setContentCaptureOptions(application.getContentCaptureOptions());
        Window window2 = this.mWindow;
        if (window2 instanceof PhoneWindow) {
            ((PhoneWindow) window2).setAppToken(this.mToken);
        }
    }

    private void enableAutofillCompatibilityIfNeeded() {
        AutofillManager afm;
        if (isAutofillCompatibilityEnabled() && (afm = (AutofillManager) getSystemService(AutofillManager.class)) != null) {
            afm.enableCompatibilityMode();
        }
    }

    @Override // android.content.ContextWrapper, android.content.Context
    @UnsupportedAppUsage
    public final IBinder getActivityToken() {
        Activity activity = this.mParent;
        return activity != null ? activity.getActivityToken() : this.mToken;
    }

    public final IBinder getAssistToken() {
        Activity activity = this.mParent;
        return activity != null ? activity.getAssistToken() : this.mAssistToken;
    }

    @VisibleForTesting
    public final ActivityThread getActivityThread() {
        return this.mMainThread;
    }

    /* access modifiers changed from: package-private */
    public final void performCreate(Bundle icicle) {
        performCreate(icicle, null);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public final void performCreate(Bundle icicle, PersistableBundle persistentState) {
        dispatchActivityPreCreated(icicle);
        this.mCanEnterPictureInPicture = true;
        restoreHasCurrentPermissionRequest(icicle);
        if (Log.HWINFO) {
            Trace.traceBegin(64, "onCreate");
        }
        if (persistentState != null) {
            onCreate(icicle, persistentState);
        } else {
            onCreate(icicle);
        }
        writeEventLog(LOG_AM_ON_CREATE_CALLED, "performCreate");
        this.mActivityTransitionState.readState(icicle);
        if (Log.HWINFO) {
            Trace.traceEnd(64);
        }
        this.mVisibleFromClient = true ^ this.mWindow.getWindowStyle().getBoolean(10, false);
        this.mFragments.dispatchActivityCreated();
        this.mActivityTransitionState.setEnterActivityOptions(this, getActivityOptions());
        dispatchActivityPostCreated(icicle);
    }

    /* access modifiers changed from: package-private */
    public final void performNewIntent(Intent intent) {
        this.mCanEnterPictureInPicture = true;
        onNewIntent(intent);
    }

    /* access modifiers changed from: package-private */
    public final void performStart(String reason) {
        String dlwarning;
        dispatchActivityPreStarted();
        this.mActivityTransitionState.setEnterActivityOptions(this, getActivityOptions());
        this.mFragments.noteStateNotSaved();
        this.mCalled = false;
        this.mFragments.execPendingActions();
        this.mInstrumentation.callActivityOnStart(this);
        writeEventLog(LOG_AM_ON_START_CALLED, reason);
        if (this.mCalled) {
            this.mFragments.dispatchStart();
            this.mFragments.reportLoaderStart();
            boolean isAppDebuggable = (this.mApplication.getApplicationInfo().flags & 2) != 0;
            boolean isDlwarningEnabled = SystemProperties.getInt("ro.bionic.ld.warning", 0) == 1;
            if ((isAppDebuggable || isDlwarningEnabled) && (dlwarning = getDlWarning()) != null) {
                String appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
                String warning = "Detected problems with app native libraries\n(please consult log for detail):\n" + dlwarning;
                if (isAppDebuggable) {
                    new AlertDialog.Builder(this).setTitle(appName).setMessage(warning).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).setCancelable(false).show();
                } else {
                    Toast.makeText(this, appName + "\n" + warning, 1).show();
                }
            }
            GraphicsEnvironment.getInstance().showAngleInUseDialogBox(this);
            this.mActivityTransitionState.enterReady(this);
            dispatchActivityPostStarted();
            return;
        }
        throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onStart()");
    }

    /* access modifiers changed from: package-private */
    public final void performRestart(boolean start, String reason) {
        this.mCanEnterPictureInPicture = true;
        this.mFragments.noteStateNotSaved();
        if (this.mToken != null && this.mParent == null) {
            WindowManagerGlobal.getInstance().setStoppedState(this.mToken, false);
        }
        if (this.mStopped) {
            this.mStopped = false;
            synchronized (this.mManagedCursors) {
                int N = this.mManagedCursors.size();
                for (int i = 0; i < N; i++) {
                    ManagedCursor mc = this.mManagedCursors.get(i);
                    if (mc.mReleased || mc.mUpdated) {
                        if (!mc.mCursor.requery()) {
                            if (getApplicationInfo().targetSdkVersion >= 14) {
                                throw new IllegalStateException("trying to requery an already closed cursor  " + mc.mCursor);
                            }
                        }
                        mc.mReleased = false;
                        mc.mUpdated = false;
                    }
                }
            }
            this.mCalled = false;
            this.mInstrumentation.callActivityOnRestart(this);
            writeEventLog(LOG_AM_ON_RESTART_CALLED, reason);
            if (!this.mCalled) {
                throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onRestart()");
            } else if (start) {
                performStart(reason);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void performResume(boolean followedByPause, String reason) {
        Window window;
        dispatchActivityPreResumed();
        performRestart(true, reason);
        this.mFragments.execPendingActions();
        this.mLastNonConfigurationInstances = null;
        if (this.mAutoFillResetNeeded) {
            this.mAutoFillIgnoreFirstResumePause = followedByPause;
            boolean z = this.mAutoFillIgnoreFirstResumePause;
        }
        this.mCalled = false;
        this.mInstrumentation.callActivityOnResume(this);
        writeEventLog(LOG_AM_ON_RESUME_CALLED, reason);
        if (this.mCalled) {
            if (!this.mVisibleFromClient && !this.mFinished) {
                Log.w(TAG, "An activity without a UI must call finish() before onResume() completes");
                if (getApplicationInfo().targetSdkVersion > 22) {
                    throw new IllegalStateException("Activity " + this.mComponent.toShortString() + " did not call finish() prior to onResume() completing");
                }
            }
            this.mCalled = false;
            this.mFragments.dispatchResume();
            this.mFragments.execPendingActions();
            onPostResume();
            if (this.mCalled) {
                dispatchActivityPostResumed();
                if (!this.mIsFullFlag && (window = this.mWindow) != null && (window.getAttributes().flags & 1024) != 0) {
                    this.mIsFullFlag = true;
                    LogPower.push(120, getPackageName());
                    return;
                }
                return;
            }
            throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onPostResume()");
        }
        throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onResume()");
    }

    /* access modifiers changed from: package-private */
    public final void performPause() {
        dispatchActivityPrePaused();
        this.mDoReportFullyDrawn = false;
        this.mFragments.dispatchPause();
        this.mCalled = false;
        if (Log.HWINFO) {
            Trace.traceBegin(64, "onPause");
        }
        onPause();
        if (Log.HWINFO) {
            Trace.traceEnd(64);
        }
        writeEventLog(LOG_AM_ON_PAUSE_CALLED, "performPause");
        this.mResumed = false;
        if (this.mCalled || getApplicationInfo().targetSdkVersion < 9) {
            dispatchActivityPostPaused();
            if (this.mIsFullFlag) {
                this.mIsFullFlag = false;
                LogPower.push(135, getPackageName());
                return;
            }
            return;
        }
        throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onPause()");
    }

    /* access modifiers changed from: package-private */
    public final void performUserLeaving() {
        onUserInteraction();
        onUserLeaveHint();
    }

    /* access modifiers changed from: package-private */
    public final void performStop(boolean preserveWindow, String reason) {
        this.mDoReportFullyDrawn = false;
        this.mFragments.doLoaderStop(this.mChangingConfigurations);
        this.mCanEnterPictureInPicture = false;
        if (!this.mStopped) {
            dispatchActivityPreStopped();
            Window window = this.mWindow;
            if (window != null) {
                window.closeAllPanels();
            }
            if (!preserveWindow && this.mToken != null && this.mParent == null) {
                WindowManagerGlobal.getInstance().setStoppedState(this.mToken, true);
            }
            this.mFragments.dispatchStop();
            this.mCalled = false;
            this.mInstrumentation.callActivityOnStop(this);
            writeEventLog(LOG_AM_ON_STOP_CALLED, reason);
            if (this.mCalled) {
                synchronized (this.mManagedCursors) {
                    int N = this.mManagedCursors.size();
                    for (int i = 0; i < N; i++) {
                        ManagedCursor mc = this.mManagedCursors.get(i);
                        if (!mc.mReleased) {
                            mc.mCursor.deactivate();
                            mc.mReleased = true;
                        }
                    }
                }
                this.mStopped = true;
                dispatchActivityPostStopped();
            } else {
                throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onStop()");
            }
        }
        this.mResumed = false;
    }

    /* access modifiers changed from: package-private */
    public final void performDestroy() {
        dispatchActivityPreDestroyed();
        this.mDestroyed = true;
        this.mWindow.destroy();
        this.mFragments.dispatchDestroy();
        if (Log.HWINFO) {
            Trace.traceBegin(64, "onDestroy");
        }
        onDestroy();
        if (Log.HWINFO) {
            Trace.traceEnd(64);
        }
        writeEventLog(LOG_AM_ON_DESTROY_CALLED, "performDestroy");
        this.mFragments.doLoaderDestroy();
        VoiceInteractor voiceInteractor = this.mVoiceInteractor;
        if (voiceInteractor != null) {
            voiceInteractor.detachActivity();
        }
        dispatchActivityPostDestroyed();
    }

    /* access modifiers changed from: package-private */
    public final void dispatchMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        this.mFragments.dispatchMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        Window window = this.mWindow;
        if (window != null) {
            window.onMultiWindowModeChanged();
        }
        onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
    }

    /* access modifiers changed from: package-private */
    public final void dispatchPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        this.mFragments.dispatchPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        Window window = this.mWindow;
        if (window != null) {
            window.onPictureInPictureModeChanged(isInPictureInPictureMode);
        }
        onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    @UnsupportedAppUsage
    public final boolean isResumed() {
        return this.mResumed;
    }

    private void storeHasCurrentPermissionRequest(Bundle bundle) {
        if (bundle != null && this.mHasCurrentPermissionsRequest) {
            bundle.putBoolean(HAS_CURENT_PERMISSIONS_REQUEST_KEY, true);
        }
    }

    private void restoreHasCurrentPermissionRequest(Bundle bundle) {
        if (bundle != null) {
            this.mHasCurrentPermissionsRequest = bundle.getBoolean(HAS_CURENT_PERMISSIONS_REQUEST_KEY, false);
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityResult(String who, int requestCode, int resultCode, Intent data, String reason) {
        this.mFragments.noteStateNotSaved();
        if (who == null) {
            onActivityResult(requestCode, resultCode, data);
        } else if (who.startsWith(REQUEST_PERMISSIONS_WHO_PREFIX)) {
            String who2 = who.substring(REQUEST_PERMISSIONS_WHO_PREFIX.length());
            if (TextUtils.isEmpty(who2)) {
                dispatchRequestPermissionsResult(requestCode, data);
            } else {
                Fragment frag = this.mFragments.findFragmentByWho(who2);
                if (frag != null) {
                    dispatchRequestPermissionsResultToFragment(requestCode, data, frag);
                }
            }
        } else if (who.startsWith("@android:view:")) {
            Iterator<ViewRootImpl> it = WindowManagerGlobal.getInstance().getRootViews(getActivityToken()).iterator();
            while (it.hasNext()) {
                ViewRootImpl viewRoot = it.next();
                if (viewRoot.getView() != null && viewRoot.getView().dispatchActivityResult(who, requestCode, resultCode, data)) {
                    return;
                }
            }
        } else if (who.startsWith(AUTO_FILL_AUTH_WHO_PREFIX)) {
            getAutofillManager().onAuthenticationResult(requestCode, resultCode == -1 ? data : null, getCurrentFocus());
        } else {
            Fragment frag2 = this.mFragments.findFragmentByWho(who);
            if (frag2 != null) {
                frag2.onActivityResult(requestCode, resultCode, data);
            }
        }
        writeEventLog(LOG_AM_ON_ACTIVITY_RESULT_CALLED, reason);
    }

    public void startLockTask() {
        try {
            ActivityTaskManager.getService().startLockTaskModeByToken(this.mToken);
        } catch (RemoteException e) {
        }
    }

    public void stopLockTask() {
        try {
            ActivityTaskManager.getService().stopLockTaskModeByToken(this.mToken);
        } catch (RemoteException e) {
        }
    }

    public void showLockTaskEscapeMessage() {
        try {
            ActivityTaskManager.getService().showLockTaskEscapeMessage(this.mToken);
        } catch (RemoteException e) {
        }
    }

    public boolean isOverlayWithDecorCaptionEnabled() {
        return this.mWindow.isOverlayWithDecorCaptionEnabled();
    }

    public void setOverlayWithDecorCaptionEnabled(boolean enabled) {
        this.mWindow.setOverlayWithDecorCaptionEnabled(enabled);
    }

    private void dispatchRequestPermissionsResult(int requestCode, Intent data) {
        String[] permissions;
        int[] grantResults;
        this.mHasCurrentPermissionsRequest = false;
        if (data != null) {
            permissions = data.getStringArrayExtra(PackageManager.EXTRA_REQUEST_PERMISSIONS_NAMES);
        } else {
            permissions = new String[0];
        }
        if (data != null) {
            grantResults = data.getIntArrayExtra(PackageManager.EXTRA_REQUEST_PERMISSIONS_RESULTS);
        } else {
            grantResults = new int[0];
        }
        onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void dispatchRequestPermissionsResultToFragment(int requestCode, Intent data, Fragment fragment) {
        String[] permissions;
        int[] grantResults;
        if (data != null) {
            permissions = data.getStringArrayExtra(PackageManager.EXTRA_REQUEST_PERMISSIONS_NAMES);
        } else {
            permissions = new String[0];
        }
        if (data != null) {
            grantResults = data.getIntArrayExtra(PackageManager.EXTRA_REQUEST_PERMISSIONS_RESULTS);
        } else {
            grantResults = new int[0];
        }
        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final void autofillClientAuthenticate(int authenticationId, IntentSender intent, Intent fillInIntent) {
        try {
            startIntentSenderForResultInner(intent, AUTO_FILL_AUTH_WHO_PREFIX, authenticationId, fillInIntent, 0, 0, null);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "authenticate() failed for intent:" + intent, e);
        }
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final void autofillClientResetableStateAvailable() {
        this.mAutoFillResetNeeded = true;
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final boolean autofillClientRequestShowFillUi(View anchor, int width, int height, Rect anchorBounds, IAutofillWindowPresenter presenter) {
        boolean wasShowing;
        AutofillPopupWindow autofillPopupWindow = this.mAutofillPopupWindow;
        if (autofillPopupWindow == null) {
            wasShowing = false;
            this.mAutofillPopupWindow = new AutofillPopupWindow(presenter);
        } else {
            wasShowing = autofillPopupWindow.isShowing();
        }
        this.mAutofillPopupWindow.update(anchor, 0, 0, width, height, anchorBounds);
        return !wasShowing && this.mAutofillPopupWindow.isShowing();
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final void autofillClientDispatchUnhandledKey(View anchor, KeyEvent keyEvent) {
        ViewRootImpl rootImpl = anchor.getViewRootImpl();
        if (rootImpl != null) {
            rootImpl.dispatchKeyFromAutofill(keyEvent);
        }
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final boolean autofillClientRequestHideFillUi() {
        AutofillPopupWindow autofillPopupWindow = this.mAutofillPopupWindow;
        if (autofillPopupWindow == null) {
            return false;
        }
        autofillPopupWindow.dismiss();
        this.mAutofillPopupWindow = null;
        return true;
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final boolean autofillClientIsFillUiShowing() {
        AutofillPopupWindow autofillPopupWindow = this.mAutofillPopupWindow;
        return autofillPopupWindow != null && autofillPopupWindow.isShowing();
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final View[] autofillClientFindViewsByAutofillIdTraversal(AutofillId[] autofillId) {
        View[] views = new View[autofillId.length];
        ArrayList<ViewRootImpl> roots = WindowManagerGlobal.getInstance().getRootViews(getActivityToken());
        for (int rootNum = 0; rootNum < roots.size(); rootNum++) {
            View rootView = roots.get(rootNum).getView();
            if (rootView != null) {
                int viewCount = autofillId.length;
                for (int viewNum = 0; viewNum < viewCount; viewNum++) {
                    if (views[viewNum] == null) {
                        views[viewNum] = rootView.findViewByAutofillIdTraversal(autofillId[viewNum].getViewId());
                    }
                }
            }
        }
        return views;
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final View autofillClientFindViewByAutofillIdTraversal(AutofillId autofillId) {
        View view;
        ArrayList<ViewRootImpl> roots = WindowManagerGlobal.getInstance().getRootViews(getActivityToken());
        for (int rootNum = 0; rootNum < roots.size(); rootNum++) {
            View rootView = roots.get(rootNum).getView();
            if (!(rootView == null || (view = rootView.findViewByAutofillIdTraversal(autofillId.getViewId())) == null)) {
                return view;
            }
        }
        return null;
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final boolean[] autofillClientGetViewVisibility(AutofillId[] autofillIds) {
        int autofillIdCount = autofillIds.length;
        boolean[] visible = new boolean[autofillIdCount];
        for (int i = 0; i < autofillIdCount; i++) {
            AutofillId autofillId = autofillIds[i];
            View view = autofillClientFindViewByAutofillIdTraversal(autofillId);
            if (view != null) {
                if (!autofillId.isVirtualInt()) {
                    visible[i] = view.isVisibleToUser();
                } else {
                    visible[i] = view.isVisibleToUserForAutofill(autofillId.getVirtualChildIntId());
                }
            }
        }
        if (Helper.sVerbose) {
            Log.v(TAG, "autofillClientGetViewVisibility(): " + Arrays.toString(visible));
        }
        return visible;
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final View autofillClientFindViewByAccessibilityIdTraversal(int viewId, int windowId) {
        View view;
        ArrayList<ViewRootImpl> roots = WindowManagerGlobal.getInstance().getRootViews(getActivityToken());
        for (int rootNum = 0; rootNum < roots.size(); rootNum++) {
            View rootView = roots.get(rootNum).getView();
            if (!(rootView == null || rootView.getAccessibilityWindowId() != windowId || (view = rootView.findViewByAccessibilityIdTraversal(viewId)) == null)) {
                return view;
            }
        }
        return null;
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final IBinder autofillClientGetActivityToken() {
        return getActivityToken();
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final boolean autofillClientIsVisibleForAutofill() {
        return !this.mStopped;
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final boolean autofillClientIsCompatibilityModeEnabled() {
        return isAutofillCompatibilityEnabled();
    }

    @Override // android.view.autofill.AutofillManager.AutofillClient
    public final boolean isDisablingEnterExitEventForAutofill() {
        return this.mAutoFillIgnoreFirstResumePause || !this.mResumed;
    }

    @UnsupportedAppUsage
    public void setDisablePreviewScreenshots(boolean disable) {
        try {
            ActivityTaskManager.getService().setDisablePreviewScreenshots(this.mToken, disable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setShowWhenLocked(boolean showWhenLocked) {
        try {
            ActivityTaskManager.getService().setShowWhenLocked(this.mToken, showWhenLocked);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setInheritShowWhenLocked(boolean inheritShowWhenLocked) {
        try {
            ActivityTaskManager.getService().setInheritShowWhenLocked(this.mToken, inheritShowWhenLocked);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setTurnScreenOn(boolean turnScreenOn) {
        try {
            ActivityTaskManager.getService().setTurnScreenOn(this.mToken, turnScreenOn);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void registerRemoteAnimations(RemoteAnimationDefinition definition) {
        try {
            ActivityTaskManager.getService().registerRemoteAnimations(this.mToken, definition);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void writeEventLog(int event, String reason) {
        EventLog.writeEvent(event, Integer.valueOf(UserHandle.myUserId()), getComponentName().getClassName(), reason);
    }

    class HostCallbacks extends FragmentHostCallback<Activity> {
        public HostCallbacks() {
            super(Activity.this);
        }

        @Override // android.app.FragmentHostCallback
        public void onDump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            Activity.this.dump(prefix, fd, writer, args);
        }

        @Override // android.app.FragmentHostCallback
        public boolean onShouldSaveFragmentState(Fragment fragment) {
            return !Activity.this.isFinishing();
        }

        @Override // android.app.FragmentHostCallback
        public LayoutInflater onGetLayoutInflater() {
            LayoutInflater result = Activity.this.getLayoutInflater();
            if (onUseFragmentManagerInflaterFactory()) {
                return result.cloneInContext(Activity.this);
            }
            return result;
        }

        @Override // android.app.FragmentHostCallback
        public boolean onUseFragmentManagerInflaterFactory() {
            return Activity.this.getApplicationInfo().targetSdkVersion >= 21;
        }

        @Override // android.app.FragmentHostCallback
        public Activity onGetHost() {
            return Activity.this;
        }

        @Override // android.app.FragmentHostCallback
        public void onInvalidateOptionsMenu() {
            Activity.this.invalidateOptionsMenu();
        }

        @Override // android.app.FragmentHostCallback
        public void onStartActivityFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options) {
            Activity.this.startActivityFromFragment(fragment, intent, requestCode, options);
        }

        @Override // android.app.FragmentHostCallback
        public void onStartActivityAsUserFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options, UserHandle user) {
            Activity.this.startActivityAsUserFromFragment(fragment, intent, requestCode, options, user);
        }

        @Override // android.app.FragmentHostCallback
        public void onStartIntentSenderFromFragment(Fragment fragment, IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
            if (Activity.this.mParent == null) {
                Activity.this.startIntentSenderForResultInner(intent, fragment.mWho, requestCode, fillInIntent, flagsMask, flagsValues, options);
            } else if (options != null) {
                Activity.this.mParent.startIntentSenderFromChildFragment(fragment, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
            }
        }

        @Override // android.app.FragmentHostCallback
        public void onRequestPermissionsFromFragment(Fragment fragment, String[] permissions, int requestCode) {
            Activity.this.startActivityForResult(Activity.REQUEST_PERMISSIONS_WHO_PREFIX + fragment.mWho, Activity.this.getPackageManager().buildRequestPermissionsIntent(permissions), requestCode, null);
        }

        @Override // android.app.FragmentHostCallback
        public boolean onHasWindowAnimations() {
            return Activity.this.getWindow() != null;
        }

        @Override // android.app.FragmentHostCallback
        public int onGetWindowAnimations() {
            Window w = Activity.this.getWindow();
            if (w == null) {
                return 0;
            }
            return w.getAttributes().windowAnimations;
        }

        @Override // android.app.FragmentHostCallback
        public void onAttachFragment(Fragment fragment) {
            Activity.this.onAttachFragment(fragment);
        }

        @Override // android.app.FragmentHostCallback, android.app.FragmentContainer
        public <T extends View> T onFindViewById(int id) {
            return (T) Activity.this.findViewById(id);
        }

        @Override // android.app.FragmentHostCallback, android.app.FragmentContainer
        public boolean onHasView() {
            Window w = Activity.this.getWindow();
            return (w == null || w.peekDecorView() == null) ? false : true;
        }
    }
}
