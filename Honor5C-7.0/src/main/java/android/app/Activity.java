package android.app;

import android.R;
import android.app.ActivityManager.TaskDescription;
import android.app.Instrumentation.ActivityResult;
import android.app.VoiceInteractor.Request;
import android.app.assist.AssistContent;
import android.bluetooth.BluetoothAssignedNumbers;
import android.common.HwFrameworkFactory;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hsm.HwSystemManager;
import android.hwcontrol.HwWidgetFactory;
import android.media.session.MediaController;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.health.HealthKeys;
import android.provider.BrowserContract.Searches;
import android.provider.DocumentsContract.Document;
import android.rms.AppAssociate;
import android.rms.iaware.Events;
import android.security.keymaster.KeymasterDefs;
import android.speech.SpeechRecognizer;
import android.telecom.AudioState;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.BoostFramework;
import android.util.EventLog;
import android.util.Log;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SuperNotCalledException;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.KeyboardShortcutGroup;
import android.view.KeyboardShortcutInfo;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory2;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewRootImpl;
import android.view.Window;
import android.view.Window.Callback;
import android.view.Window.OnWindowDismissedCallback;
import android.view.Window.WindowControllerCallback;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import android.widget.Toolbar;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.app.ToolbarActionBar;
import com.android.internal.app.WindowDecorActionBar;
import com.android.internal.policy.HwPolicyFactory;
import com.huawei.pgmng.common.Utils;
import com.huawei.pgmng.log.LogPower;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Activity extends ContextThemeWrapper implements Factory2, Callback, KeyEvent.Callback, OnCreateContextMenuListener, ComponentCallbacks2, OnWindowDismissedCallback, WindowControllerCallback {
    private static final boolean DEBUG_LIFECYCLE = false;
    public static final int DEFAULT_KEYS_DIALER = 1;
    public static final int DEFAULT_KEYS_DISABLE = 0;
    public static final int DEFAULT_KEYS_SEARCH_GLOBAL = 4;
    public static final int DEFAULT_KEYS_SEARCH_LOCAL = 3;
    public static final int DEFAULT_KEYS_SHORTCUT = 2;
    public static final int DONT_FINISH_TASK_WITH_ACTIVITY = 0;
    public static final int FINISH_TASK_WITH_ACTIVITY = 2;
    public static final int FINISH_TASK_WITH_ROOT_ACTIVITY = 1;
    protected static final int[] FOCUSED_STATE_SET = null;
    static final String FRAGMENTS_TAG = "android:fragments";
    private static final String HAS_CURENT_PERMISSIONS_REQUEST_KEY = "android:hasCurrentPermissionsRequest";
    private static final String KEYBOARD_SHORTCUTS_RECEIVER_CLASS_NAME = "com.android.systemui.statusbar.KeyboardShortcutsReceiver";
    private static final String KEYBOARD_SHORTCUTS_RECEIVER_PKG_NAME = "com.android.systemui";
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
    private static int[] mAsParamVal;
    private static int mDragBoostPossible;
    private static BoostFramework mPerf;
    private static int mPerfLockDuration;
    ActionBar mActionBar;
    private int mActionModeTypeStarting;
    ActivityInfo mActivityInfo;
    ActivityTransitionState mActivityTransitionState;
    private Application mApplication;
    boolean mCalled;
    private boolean mChangeCanvasToTranslucent;
    boolean mChangingConfigurations;
    private ComponentName mComponent;
    int mConfigChangeFlags;
    Configuration mCurrentConfig;
    private HwCustActivity mCustActivity;
    View mDecor;
    private int mDefaultKeyMode;
    private SpannableStringBuilder mDefaultKeySsb;
    private boolean mDestroyed;
    private boolean mDoReportFullyDrawn;
    private boolean mEatKeyUpEvent;
    String mEmbeddedID;
    private boolean mEnableDefaultActionBarUp;
    SharedElementCallback mEnterTransitionListener;
    SharedElementCallback mExitTransitionListener;
    boolean mFinished;
    final FragmentController mFragments;
    final Handler mHandler;
    private boolean mHasCurrentPermissionsRequest;
    private int mIdent;
    private final Object mInstanceTracker;
    private Instrumentation mInstrumentation;
    Intent mIntent;
    private boolean mIsFullFlag;
    NonConfigurationInstances mLastNonConfigurationInstances;
    ActivityThread mMainThread;
    private final ArrayList<ManagedCursor> mManagedCursors;
    private SparseArray<ManagedDialog> mManagedDialogs;
    private MenuInflater mMenuInflater;
    Activity mParent;
    String mReferrer;
    int mResultCode;
    Intent mResultData;
    boolean mResumed;
    private SearchEvent mSearchEvent;
    private SearchManager mSearchManager;
    boolean mStartedActivity;
    boolean mStopped;
    private TaskDescription mTaskDescription;
    boolean mTemporaryPause;
    private CharSequence mTitle;
    private int mTitleColor;
    private boolean mTitleReady;
    private IBinder mToken;
    private TranslucentConversionListener mTranslucentCallback;
    private Thread mUiThread;
    boolean mVisibleBehind;
    boolean mVisibleFromClient;
    boolean mVisibleFromServer;
    private VoiceInteractor mVoiceInteractor;
    private Window mWindow;
    boolean mWindowAdded;
    private WindowManager mWindowManager;

    class HostCallbacks extends FragmentHostCallback<Activity> {
        public HostCallbacks() {
            super(Activity.this);
        }

        public void onDump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            Activity.this.dump(prefix, fd, writer, args);
        }

        public boolean onShouldSaveFragmentState(Fragment fragment) {
            return Activity.this.isFinishing() ? Activity.DEBUG_LIFECYCLE : true;
        }

        public LayoutInflater onGetLayoutInflater() {
            LayoutInflater result = Activity.this.getLayoutInflater();
            if (onUseFragmentManagerInflaterFactory()) {
                return result.cloneInContext(Activity.this);
            }
            return result;
        }

        public boolean onUseFragmentManagerInflaterFactory() {
            return Activity.this.getApplicationInfo().targetSdkVersion >= 21 ? true : Activity.DEBUG_LIFECYCLE;
        }

        public Activity onGetHost() {
            return Activity.this;
        }

        public void onInvalidateOptionsMenu() {
            Activity.this.invalidateOptionsMenu();
        }

        public void onStartActivityFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options) {
            Activity.this.startActivityFromFragment(fragment, intent, requestCode, options);
        }

        public void onStartIntentSenderFromFragment(Fragment fragment, IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
            if (Activity.this.mParent == null) {
                Activity.this.startIntentSenderForResultInner(intent, fragment.mWho, requestCode, fillInIntent, flagsMask, flagsValues, options);
            } else if (options != null) {
                Activity.this.mParent.startIntentSenderFromChildFragment(fragment, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
            }
        }

        public void onRequestPermissionsFromFragment(Fragment fragment, String[] permissions, int requestCode) {
            Activity.this.startActivityForResult(Activity.REQUEST_PERMISSIONS_WHO_PREFIX + fragment.mWho, Activity.this.getPackageManager().buildRequestPermissionsIntent(permissions), requestCode, null);
        }

        public boolean onHasWindowAnimations() {
            return Activity.this.getWindow() != null ? true : Activity.DEBUG_LIFECYCLE;
        }

        public int onGetWindowAnimations() {
            Window w = Activity.this.getWindow();
            return w == null ? Activity.RESULT_CANCELED : w.getAttributes().windowAnimations;
        }

        public void onAttachFragment(Fragment fragment) {
            Activity.this.onAttachFragment(fragment);
        }

        public View onFindViewById(int id) {
            return Activity.this.findViewById(id);
        }

        public boolean onHasView() {
            Window w = Activity.this.getWindow();
            if (w == null || w.peekDecorView() == null) {
                return Activity.DEBUG_LIFECYCLE;
            }
            return true;
        }
    }

    private static final class ManagedCursor {
        private final Cursor mCursor;
        private boolean mReleased;
        private boolean mUpdated;

        ManagedCursor(Cursor cursor) {
            this.mCursor = cursor;
            this.mReleased = Activity.DEBUG_LIFECYCLE;
            this.mUpdated = Activity.DEBUG_LIFECYCLE;
        }
    }

    private static class ManagedDialog {
        Bundle mArgs;
        Dialog mDialog;

        private ManagedDialog() {
        }
    }

    static final class NonConfigurationInstances {
        Object activity;
        HashMap<String, Object> children;
        FragmentManagerNonConfig fragments;
        ArrayMap<String, LoaderManager> loaders;
        VoiceInteractor voiceInteractor;

        NonConfigurationInstances() {
        }
    }

    public interface TranslucentConversionListener {
        void onTranslucentConversionComplete(boolean z);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.Activity.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.Activity.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.Activity.<clinit>():void");
    }

    private static native String getDlWarning();

    public Activity() {
        this.mDoReportFullyDrawn = true;
        this.mTemporaryPause = DEBUG_LIFECYCLE;
        this.mChangingConfigurations = DEBUG_LIFECYCLE;
        this.mDecor = null;
        this.mWindowAdded = DEBUG_LIFECYCLE;
        this.mVisibleFromServer = DEBUG_LIFECYCLE;
        this.mVisibleFromClient = true;
        this.mActionBar = null;
        this.mIsFullFlag = DEBUG_LIFECYCLE;
        this.mTitleColor = RESULT_CANCELED;
        this.mHandler = new Handler();
        this.mFragments = FragmentController.createController(new HostCallbacks());
        this.mManagedCursors = new ArrayList();
        this.mResultCode = RESULT_CANCELED;
        this.mResultData = null;
        this.mTitleReady = DEBUG_LIFECYCLE;
        this.mActionModeTypeStarting = RESULT_CANCELED;
        this.mDefaultKeyMode = RESULT_CANCELED;
        this.mDefaultKeySsb = null;
        this.mTaskDescription = new TaskDescription();
        this.mInstanceTracker = StrictMode.trackActivity(this);
        this.mActivityTransitionState = new ActivityTransitionState();
        this.mEnterTransitionListener = SharedElementCallback.NULL_CALLBACK;
        this.mExitTransitionListener = SharedElementCallback.NULL_CALLBACK;
        this.mCustActivity = (HwCustActivity) HwCustUtils.createObj(HwCustActivity.class, new Object[RESULT_CANCELED]);
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
        return this.mParent != null ? true : DEBUG_LIFECYCLE;
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

    public LoaderManager getLoaderManager() {
        return this.mFragments.getLoaderManager();
    }

    public View getCurrentFocus() {
        return this.mWindow != null ? this.mWindow.getCurrentFocus() : null;
    }

    protected void onCreate(Bundle savedInstanceState) {
        FragmentManagerNonConfig fragmentManagerNonConfig = null;
        if (this.mCustActivity != null && this.mCustActivity.isRequestSplit(this)) {
            this.mCustActivity.initSplitMode(this.mToken);
        }
        if (this.mLastNonConfigurationInstances != null) {
            this.mFragments.restoreLoaderNonConfig(this.mLastNonConfigurationInstances.loaders);
        }
        if (this.mActivityInfo.parentActivityName != null) {
            if (this.mActionBar == null) {
                this.mEnableDefaultActionBarUp = true;
            } else {
                this.mActionBar.setDefaultDisplayHomeAsUpEnabled(true);
            }
        }
        if (savedInstanceState != null) {
            Parcelable p = savedInstanceState.getParcelable(FRAGMENTS_TAG);
            FragmentController fragmentController = this.mFragments;
            if (this.mLastNonConfigurationInstances != null) {
                fragmentManagerNonConfig = this.mLastNonConfigurationInstances.fragments;
            }
            fragmentController.restoreAllState(p, fragmentManagerNonConfig);
        }
        this.mFragments.dispatchCreate();
        getApplication().dispatchActivityCreated(this, savedInstanceState);
        if (this.mVoiceInteractor != null) {
            this.mVoiceInteractor.attachActivity(this);
        }
        this.mCalled = true;
    }

    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        onCreate(savedInstanceState);
    }

    final void performRestoreInstanceState(Bundle savedInstanceState) {
        Trace.traceBegin(64, "onRestoreInstanceState");
        onRestoreInstanceState(savedInstanceState);
        Trace.traceEnd(64);
        restoreManagedDialogs(savedInstanceState);
    }

    final void performRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        onRestoreInstanceState(savedInstanceState, persistentState);
        if (savedInstanceState != null) {
            restoreManagedDialogs(savedInstanceState);
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (this.mWindow != null) {
            Bundle windowState = savedInstanceState.getBundle(WINDOW_HIERARCHY_TAG);
            if (windowState != null) {
                this.mWindow.restoreHierarchyState(windowState);
            }
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
            this.mManagedDialogs = new SparseArray(numDialogs);
            for (int i = RESULT_CANCELED; i < numDialogs; i += RESULT_FIRST_USER) {
                Integer dialogId = Integer.valueOf(ids[i]);
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

    protected void onPostCreate(Bundle savedInstanceState) {
        if (!isChild()) {
            this.mTitleReady = true;
            onTitleChanged(getTitle(), getTitleColor());
        }
        this.mCalled = true;
    }

    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        onPostCreate(savedInstanceState);
    }

    protected void onStart() {
        this.mCalled = true;
        this.mFragments.doLoaderStart();
        getApplication().dispatchActivityStarted(this);
    }

    protected void onRestart() {
        if (this.mCustActivity != null && this.mCustActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityRestart();
        }
        this.mCalled = true;
    }

    public void onStateNotSaved() {
    }

    protected void onResume() {
        getApplication().dispatchActivityResumed(this);
        this.mActivityTransitionState.onResume(this, isTopOfTask());
        this.mCalled = true;
        if (this.mCustActivity != null && this.mCustActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityResume();
        }
    }

    protected void onPostResume() {
        Window win = getWindow();
        if (win != null) {
            win.makeActive();
        }
        if (this.mActionBar != null) {
            this.mActionBar.setShowHideAnimationEnabled(true);
        }
        this.mCalled = true;
    }

    void setVoiceInteractor(IVoiceInteractor voiceInteractor) {
        if (this.mVoiceInteractor != null) {
            Request[] activeRequests = this.mVoiceInteractor.getActiveRequests();
            int length = activeRequests.length;
            for (int i = RESULT_CANCELED; i < length; i += RESULT_FIRST_USER) {
                Request activeRequest = activeRequests[i];
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

    public boolean isVoiceInteraction() {
        return this.mVoiceInteractor != null ? true : DEBUG_LIFECYCLE;
    }

    public boolean isVoiceInteractionRoot() {
        boolean z = DEBUG_LIFECYCLE;
        try {
            if (this.mVoiceInteractor != null) {
                z = ActivityManagerNative.getDefault().isRootVoiceInteraction(this.mToken);
            }
            return z;
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        }
    }

    public VoiceInteractor getVoiceInteractor() {
        return this.mVoiceInteractor;
    }

    public boolean isLocalVoiceInteractionSupported() {
        try {
            return ActivityManagerNative.getDefault().supportsLocalVoiceInteraction();
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        }
    }

    public void startLocalVoiceInteraction(Bundle privateOptions) {
        try {
            ActivityManagerNative.getDefault().startLocalVoiceInteraction(this.mToken, privateOptions);
        } catch (RemoteException e) {
        }
    }

    public void onLocalVoiceInteractionStarted() {
    }

    public void onLocalVoiceInteractionStopped() {
    }

    public void stopLocalVoiceInteraction() {
        try {
            ActivityManagerNative.getDefault().stopLocalVoiceInteraction(this.mToken);
        } catch (RemoteException e) {
        }
    }

    protected void onNewIntent(Intent intent) {
        if (this.mCustActivity != null && this.mCustActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityNewIntent(intent);
        }
    }

    final void performSaveInstanceState(Bundle outState) {
        Trace.traceBegin(64, "onSaveInstatnceState");
        onSaveInstanceState(outState);
        Trace.traceEnd(64);
        saveManagedDialogs(outState);
        this.mActivityTransitionState.saveState(outState);
        storeHasCurrentPermissionRequest(outState);
    }

    final void performSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        onSaveInstanceState(outState, outPersistentState);
        saveManagedDialogs(outState);
        storeHasCurrentPermissionRequest(outState);
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle(WINDOW_HIERARCHY_TAG, this.mWindow.saveHierarchyState());
        Parcelable p = this.mFragments.saveAllState();
        if (p != null) {
            outState.putParcelable(FRAGMENTS_TAG, p);
        }
        getApplication().dispatchActivitySaveInstanceState(this, outState);
    }

    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        onSaveInstanceState(outState);
    }

    private void saveManagedDialogs(Bundle outState) {
        if (this.mManagedDialogs != null) {
            int numDialogs = this.mManagedDialogs.size();
            if (numDialogs != 0) {
                Bundle dialogState = new Bundle();
                int[] ids = new int[this.mManagedDialogs.size()];
                for (int i = RESULT_CANCELED; i < numDialogs; i += RESULT_FIRST_USER) {
                    int key = this.mManagedDialogs.keyAt(i);
                    ids[i] = key;
                    ManagedDialog md = (ManagedDialog) this.mManagedDialogs.valueAt(i);
                    dialogState.putBundle(savedDialogKeyFor(key), md.mDialog.onSaveInstanceState());
                    if (md.mArgs != null) {
                        dialogState.putBundle(savedDialogArgsKeyFor(key), md.mArgs);
                    }
                }
                dialogState.putIntArray(SAVED_DIALOG_IDS_KEY, ids);
                outState.putBundle(SAVED_DIALOGS_TAG, dialogState);
            }
        }
    }

    protected void onPause() {
        getApplication().dispatchActivityPaused(this);
        this.mCalled = true;
        if (HwFrameworkFactory.getHwNsdImpl().isSupportAps() && HwFrameworkFactory.getHwNsdImpl().isAPSReady()) {
            HwFrameworkFactory.getHwNsdImpl().setAPSOnPause();
        }
        if (this.mCustActivity != null && this.mCustActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityPaused();
        }
    }

    protected void onUserLeaveHint() {
    }

    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        return DEBUG_LIFECYCLE;
    }

    public CharSequence onCreateDescription() {
        return null;
    }

    public void onProvideAssistData(Bundle data) {
    }

    public void onProvideAssistContent(AssistContent outContent) {
    }

    public final void requestShowKeyboardShortcuts() {
        Intent intent = new Intent(Intent.ACTION_SHOW_KEYBOARD_SHORTCUTS);
        intent.setComponent(new ComponentName(KEYBOARD_SHORTCUTS_RECEIVER_PKG_NAME, KEYBOARD_SHORTCUTS_RECEIVER_CLASS_NAME));
        sendBroadcast(intent);
    }

    public final void dismissKeyboardShortcutsHelper() {
        Intent intent = new Intent(Intent.ACTION_DISMISS_KEYBOARD_SHORTCUTS);
        intent.setComponent(new ComponentName(KEYBOARD_SHORTCUTS_RECEIVER_PKG_NAME, KEYBOARD_SHORTCUTS_RECEIVER_CLASS_NAME));
        sendBroadcast(intent);
    }

    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {
        if (menu != null) {
            Object group = null;
            int menuSize = menu.size();
            for (int i = RESULT_CANCELED; i < menuSize; i += RESULT_FIRST_USER) {
                MenuItem item = menu.getItem(i);
                CharSequence title = item.getTitle();
                char alphaShortcut = item.getAlphabeticShortcut();
                if (!(title == null || alphaShortcut == '\u0000')) {
                    if (group == null) {
                        CharSequence string;
                        int resource = this.mApplication.getApplicationInfo().labelRes;
                        if (resource != 0) {
                            string = getString(resource);
                        } else {
                            string = null;
                        }
                        group = new KeyboardShortcutGroup(string);
                    }
                    group.addItem(new KeyboardShortcutInfo(title, alphaShortcut, StrictMode.DETECT_VM_REGISTRATION_LEAKS));
                }
            }
            if (group != null) {
                data.add(group);
            }
        }
    }

    public boolean showAssist(Bundle args) {
        try {
            return ActivityManagerNative.getDefault().showAssistFromActivity(this.mToken, args);
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        }
    }

    protected void onStop() {
        if (this.mActionBar != null) {
            this.mActionBar.setShowHideAnimationEnabled(DEBUG_LIFECYCLE);
        }
        this.mActivityTransitionState.onStop();
        getApplication().dispatchActivityStopped(this);
        this.mTranslucentCallback = null;
        this.mCalled = true;
        if (this.mCustActivity != null && this.mCustActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityStop();
        }
    }

    protected void onDestroy() {
        int i;
        this.mCalled = true;
        if (this.mCustActivity != null && this.mCustActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityDestroy();
        }
        if (this.mManagedDialogs != null) {
            int numDialogs = this.mManagedDialogs.size();
            for (i = RESULT_CANCELED; i < numDialogs; i += RESULT_FIRST_USER) {
                ManagedDialog md = (ManagedDialog) this.mManagedDialogs.valueAt(i);
                if (md.mDialog.isShowing()) {
                    md.mDialog.dismiss();
                }
            }
            this.mManagedDialogs = null;
        }
        synchronized (this.mManagedCursors) {
            int numCursors = this.mManagedCursors.size();
            for (i = RESULT_CANCELED; i < numCursors; i += RESULT_FIRST_USER) {
                ManagedCursor c = (ManagedCursor) this.mManagedCursors.get(i);
                if (c != null) {
                    c.mCursor.close();
                }
            }
            this.mManagedCursors.clear();
        }
        if (this.mSearchManager != null) {
            this.mSearchManager.stopSearch();
        }
        if (this.mActionBar != null) {
            this.mActionBar.onDestroy();
        }
        getApplication().dispatchActivityDestroyed(this);
    }

    public void reportFullyDrawn() {
        if (this.mDoReportFullyDrawn) {
            this.mDoReportFullyDrawn = DEBUG_LIFECYCLE;
            try {
                ActivityManagerNative.getDefault().reportActivityFullyDrawn(this.mToken);
            } catch (RemoteException e) {
            }
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
    }

    public boolean isInMultiWindowMode() {
        try {
            return ActivityManagerNative.getDefault().isInMultiWindowMode(this.mToken);
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        }
    }

    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
    }

    public boolean isInPictureInPictureMode() {
        try {
            return ActivityManagerNative.getDefault().isInPictureInPictureMode(this.mToken);
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        }
    }

    public void enterPictureInPictureMode() {
        try {
            ActivityManagerNative.getDefault().enterPictureInPictureMode(this.mToken);
        } catch (RemoteException e) {
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        this.mCalled = true;
        if (this.mCustActivity != null && this.mCustActivity.isRequestSplit(this)) {
            this.mCustActivity.onSplitActivityConfigurationChanged(newConfig);
        }
        this.mFragments.dispatchConfigurationChanged(newConfig);
        if (this.mWindow != null) {
            this.mWindow.onConfigurationChanged(newConfig);
        }
        if (this.mActionBar != null) {
            this.mActionBar.onConfigurationChanged(newConfig);
        }
    }

    public int getChangingConfigurations() {
        return this.mConfigChangeFlags;
    }

    public Object getLastNonConfigurationInstance() {
        if (this.mLastNonConfigurationInstances != null) {
            return this.mLastNonConfigurationInstances.activity;
        }
        return null;
    }

    public Object onRetainNonConfigurationInstance() {
        return null;
    }

    HashMap<String, Object> getLastNonConfigurationChildInstances() {
        if (this.mLastNonConfigurationInstances != null) {
            return this.mLastNonConfigurationInstances.children;
        }
        return null;
    }

    HashMap<String, Object> onRetainNonConfigurationChildInstances() {
        return null;
    }

    NonConfigurationInstances retainNonConfigurationInstances() {
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
        if (this.mVoiceInteractor != null) {
            this.mVoiceInteractor.retainInstance();
            nci.voiceInteractor = this.mVoiceInteractor;
        }
        return nci;
    }

    public void onLowMemory() {
        this.mCalled = true;
        this.mFragments.dispatchLowMemory();
    }

    public void onTrimMemory(int level) {
        this.mCalled = true;
        this.mFragments.dispatchTrimMemory(level);
    }

    public FragmentManager getFragmentManager() {
        return this.mFragments.getFragmentManager();
    }

    public void onAttachFragment(Fragment fragment) {
    }

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
            for (int i = RESULT_CANCELED; i < N; i += RESULT_FIRST_USER) {
                if (((ManagedCursor) this.mManagedCursors.get(i)).mCursor == c) {
                    this.mManagedCursors.remove(i);
                    break;
                }
            }
        }
    }

    @Deprecated
    public void setPersistent(boolean isPersistent) {
    }

    public View findViewById(int id) {
        return getWindow().findViewById(id);
    }

    public ActionBar getActionBar() {
        initWindowDecorActionBar();
        return this.mActionBar;
    }

    public void setActionBar(Toolbar toolbar) {
        ActionBar ab = getActionBar();
        if (ab instanceof WindowDecorActionBar) {
            throw new IllegalStateException("This Activity already has an action bar supplied by the window decor. Do not request Window.FEATURE_ACTION_BAR and set android:windowActionBar to false in your theme to use a Toolbar instead.");
        }
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

    public void setContentView(View view, LayoutParams params) {
        getWindow().setContentView(view, params);
        initWindowDecorActionBar();
    }

    public void addContentView(View view, LayoutParams params) {
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
        switch (mode) {
            case RESULT_CANCELED /*0*/:
            case FINISH_TASK_WITH_ACTIVITY /*2*/:
                this.mDefaultKeySsb = null;
            case RESULT_FIRST_USER /*1*/:
            case DEFAULT_KEYS_SEARCH_LOCAL /*3*/:
            case DEFAULT_KEYS_SEARCH_GLOBAL /*4*/:
                this.mDefaultKeySsb = new SpannableStringBuilder();
                Selection.setSelection(this.mDefaultKeySsb, RESULT_CANCELED);
            default:
                throw new IllegalArgumentException();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == DEFAULT_KEYS_SEARCH_GLOBAL) {
            if (getApplicationInfo().targetSdkVersion >= 5) {
                event.startTracking();
            } else {
                onBackPressed();
            }
            return true;
        } else if (this.mDefaultKeyMode == 0) {
            return DEBUG_LIFECYCLE;
        } else {
            if (this.mDefaultKeyMode == FINISH_TASK_WITH_ACTIVITY) {
                Window w = getWindow();
                return (w.hasFeature(RESULT_CANCELED) && w.performPanelShortcut(RESULT_CANCELED, keyCode, event, FINISH_TASK_WITH_ACTIVITY)) ? true : DEBUG_LIFECYCLE;
            } else {
                boolean handled;
                boolean clearSpannable = DEBUG_LIFECYCLE;
                if (event.getRepeatCount() == 0 && !event.isSystem()) {
                    handled = TextKeyListener.getInstance().onKeyDown(null, this.mDefaultKeySsb, keyCode, event);
                    if (handled && this.mDefaultKeySsb.length() > 0) {
                        String str = this.mDefaultKeySsb.toString();
                        clearSpannable = true;
                        switch (this.mDefaultKeyMode) {
                            case RESULT_FIRST_USER /*1*/:
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + str));
                                intent.addFlags(KeymasterDefs.KM_ENUM);
                                startActivity(intent);
                                break;
                            case DEFAULT_KEYS_SEARCH_LOCAL /*3*/:
                                startSearch(str, DEBUG_LIFECYCLE, null, DEBUG_LIFECYCLE);
                                break;
                            case DEFAULT_KEYS_SEARCH_GLOBAL /*4*/:
                                startSearch(str, DEBUG_LIFECYCLE, null, true);
                                break;
                            default:
                                break;
                        }
                    }
                }
                clearSpannable = true;
                handled = DEBUG_LIFECYCLE;
                if (clearSpannable) {
                    this.mDefaultKeySsb.clear();
                    this.mDefaultKeySsb.clearSpans();
                    Selection.setSelection(this.mDefaultKeySsb, RESULT_CANCELED);
                }
                return handled;
            }
        }
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return DEBUG_LIFECYCLE;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (getApplicationInfo().targetSdkVersion < 5 || keyCode != DEFAULT_KEYS_SEARCH_GLOBAL || !event.isTracking() || event.isCanceled()) {
            return DEBUG_LIFECYCLE;
        }
        onBackPressed();
        return true;
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return DEBUG_LIFECYCLE;
    }

    public void onBackPressed() {
        if (this.mActionBar == null || !this.mActionBar.collapseActionView()) {
            if (this.mCustActivity != null && this.mCustActivity.isRequestSplit(this)) {
                this.mCustActivity.handleBackPressed();
            }
            if (!this.mFragments.getFragmentManager().popBackStackImmediate()) {
                finishAfterTransition();
            }
        }
    }

    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        ActionBar actionBar = getActionBar();
        return actionBar != null ? actionBar.onKeyShortcut(keyCode, event) : DEBUG_LIFECYCLE;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mWindow.shouldCloseOnTouch(this, event)) {
            return DEBUG_LIFECYCLE;
        }
        finish();
        return true;
    }

    public boolean onTrackballEvent(MotionEvent event) {
        return DEBUG_LIFECYCLE;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return DEBUG_LIFECYCLE;
    }

    public void onUserInteraction() {
    }

    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        if (this.mParent == null) {
            View decor = this.mDecor;
            if (!(decor == null || decor.getParent() == null)) {
                getWindowManager().updateViewLayout(decor, params);
            }
        }
        if ((params.flags & Document.FLAG_SUPPORTS_REMOVE) != 0) {
            if (!this.mIsFullFlag && this.mResumed) {
                LogPower.push(BluetoothAssignedNumbers.NIKE, getPackageName());
                this.mIsFullFlag = true;
            }
        } else if (this.mIsFullFlag) {
            LogPower.push(Const.CODE_C3_SKIP4_RANGE_END, getPackageName());
            this.mIsFullFlag = DEBUG_LIFECYCLE;
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

    public boolean hasWindowFocus() {
        Window w = getWindow();
        if (w != null) {
            View d = w.getDecorView();
            if (d != null) {
                return d.hasWindowFocus();
            }
        }
        return DEBUG_LIFECYCLE;
    }

    public void onWindowDismissed(boolean finishTask) {
        finish(finishTask ? FINISH_TASK_WITH_ACTIVITY : RESULT_CANCELED);
    }

    public void exitFreeformMode() throws RemoteException {
        ActivityManagerNative.getDefault().exitFreeformMode(this.mToken);
    }

    public int getWindowStackId() throws RemoteException {
        return ActivityManagerNative.getDefault().getActivityStackId(this.mToken);
    }

    public void enterPictureInPictureModeIfPossible() {
        if (this.mActivityInfo.resizeMode == DEFAULT_KEYS_SEARCH_LOCAL) {
            enterPictureInPictureMode();
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        DispatcherState dispatcherState = null;
        onUserInteraction();
        if (event.getKeyCode() == 82 && this.mActionBar != null && this.mActionBar.onMenuKeyEvent(event)) {
            return true;
        }
        if (event.isCtrlPressed() && event.getUnicodeChar(event.getMetaState() & -28673) == 60) {
            int action = event.getAction();
            if (action == 0) {
                ActionBar actionBar = getActionBar();
                if (actionBar != null && actionBar.isShowing() && actionBar.requestFocus()) {
                    this.mEatKeyUpEvent = true;
                    return true;
                }
            } else if (action == RESULT_FIRST_USER && this.mEatKeyUpEvent) {
                this.mEatKeyUpEvent = DEBUG_LIFECYCLE;
                return true;
            }
        }
        Window win = getWindow();
        if (win.superDispatchKeyEvent(event)) {
            return true;
        }
        View decor = this.mDecor;
        if (decor == null) {
            decor = win.getDecorView();
        }
        if (decor != null) {
            dispatcherState = decor.getKeyDispatcherState();
        }
        return event.dispatch(this, dispatcherState, this);
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        onUserInteraction();
        if (getWindow().superDispatchKeyShortcutEvent(event)) {
            return true;
        }
        return onKeyShortcut(event.getKeyCode(), event);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        int i = RESULT_CANCELED;
        if (mDragBoostPossible == RESULT_OK) {
            mDragBoostPossible = RESULT_CANCELED;
            String currentActivity = getPackageName();
            String[] activityList = getResources().getStringArray(17236063);
            if (activityList != null) {
                int length = activityList.length;
                while (i < length) {
                    if (currentActivity.indexOf(activityList[i]) != RESULT_OK) {
                        mDragBoostPossible = RESULT_FIRST_USER;
                        break;
                    }
                    i += RESULT_FIRST_USER;
                }
            }
        }
        if (mDragBoostPossible == RESULT_FIRST_USER) {
            if (mPerf == null) {
                mPerf = new BoostFramework();
            }
            if (mPerfLockDuration == RESULT_OK) {
                mPerfLockDuration = getResources().getInteger(17694930);
                mAsParamVal = getResources().getIntArray(17236064);
            }
            mPerf.perfLockAcquireTouch(ev, getResources().getDisplayMetrics(), mPerfLockDuration, mAsParamVal);
        }
        if (ev.getAction() == 0) {
            onUserInteraction();
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean dispatchTrackballEvent(MotionEvent ev) {
        onUserInteraction();
        if (getWindow().superDispatchTrackballEvent(ev)) {
            return true;
        }
        return onTrackballEvent(ev);
    }

    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        onUserInteraction();
        if (getWindow().superDispatchGenericMotionEvent(ev)) {
            return true;
        }
        return onGenericMotionEvent(ev);
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        boolean isFullScreen = DEBUG_LIFECYCLE;
        event.setClassName(getClass().getName());
        event.setPackageName(getPackageName());
        LayoutParams params = getWindow().getAttributes();
        if (params.width == RESULT_OK && params.height == RESULT_OK) {
            isFullScreen = true;
        }
        event.setFullScreen(isFullScreen);
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            event.getText().add(title);
        }
        return true;
    }

    public View onCreatePanelView(int featureId) {
        return null;
    }

    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == 0) {
            return onCreateOptionsMenu(menu) | this.mFragments.dispatchCreateOptionsMenu(menu, getMenuInflater());
        }
        return DEBUG_LIFECYCLE;
    }

    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId != 0 || menu == null) {
            return true;
        }
        return onPrepareOptionsMenu(menu) | this.mFragments.dispatchPrepareOptionsMenu(menu);
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == 8) {
            initWindowDecorActionBar();
            if (this.mActionBar != null) {
                this.mActionBar.dispatchMenuVisibilityChanged(true);
            } else {
                Log.e(TAG, "Tried to open action bar menu with no action bar");
            }
        }
        return true;
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        CharSequence titleCondensed = item.getTitleCondensed();
        Object[] objArr;
        switch (featureId) {
            case RESULT_CANCELED /*0*/:
                if (titleCondensed != null) {
                    objArr = new Object[FINISH_TASK_WITH_ACTIVITY];
                    objArr[RESULT_CANCELED] = Integer.valueOf(RESULT_CANCELED);
                    objArr[RESULT_FIRST_USER] = titleCondensed.toString();
                    EventLog.writeEvent(HealthKeys.BASE_SERVICE, objArr);
                }
                if (onOptionsItemSelected(item) || this.mFragments.dispatchOptionsItemSelected(item)) {
                    return true;
                }
                if (item.getItemId() != R.id.home || this.mActionBar == null || (this.mActionBar.getDisplayOptions() & DEFAULT_KEYS_SEARCH_GLOBAL) == 0) {
                    return DEBUG_LIFECYCLE;
                }
                if (this.mParent == null) {
                    return onNavigateUp();
                }
                return this.mParent.onNavigateUpFromChild(this);
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                if (titleCondensed != null) {
                    objArr = new Object[FINISH_TASK_WITH_ACTIVITY];
                    objArr[RESULT_CANCELED] = Integer.valueOf(RESULT_FIRST_USER);
                    objArr[RESULT_FIRST_USER] = titleCondensed.toString();
                    EventLog.writeEvent(HealthKeys.BASE_SERVICE, objArr);
                }
                if (onContextItemSelected(item)) {
                    return true;
                }
                return this.mFragments.dispatchContextItemSelected(item);
            default:
                return DEBUG_LIFECYCLE;
        }
    }

    public void onPanelClosed(int featureId, Menu menu) {
        switch (featureId) {
            case RESULT_CANCELED /*0*/:
                this.mFragments.dispatchOptionsMenuClosed(menu);
                onOptionsMenuClosed(menu);
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                onContextMenuClosed(menu);
            case AudioState.ROUTE_SPEAKER /*8*/:
                initWindowDecorActionBar();
                this.mActionBar.dispatchMenuVisibilityChanged(DEBUG_LIFECYCLE);
            default:
        }
    }

    public void invalidateOptionsMenu() {
        if (!this.mWindow.hasFeature(RESULT_CANCELED)) {
            return;
        }
        if (this.mActionBar == null || !this.mActionBar.invalidateOptionsMenu()) {
            this.mWindow.invalidatePanelMenu(RESULT_CANCELED);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.mParent != null) {
            return this.mParent.onCreateOptionsMenu(menu);
        }
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.mParent != null) {
            return this.mParent.onPrepareOptionsMenu(menu);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.mParent != null) {
            return this.mParent.onOptionsItemSelected(item);
        }
        return DEBUG_LIFECYCLE;
    }

    public boolean onNavigateUp() {
        Intent upIntent = getParentActivityIntent();
        if (upIntent == null) {
            return DEBUG_LIFECYCLE;
        }
        if (this.mActivityInfo.taskAffinity == null) {
            finish();
        } else if (shouldUpRecreateTask(upIntent)) {
            TaskStackBuilder b = TaskStackBuilder.create(this);
            onCreateNavigateUpTaskStack(b);
            onPrepareNavigateUpTaskStack(b);
            b.startActivities();
            if (this.mResultCode == 0 && this.mResultData == null) {
                finishAffinity();
            } else {
                Log.i(TAG, "onNavigateUp only finishing topmost activity to return a result");
                finish();
            }
        } else {
            navigateUpTo(upIntent);
        }
        return true;
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
        if (this.mParent != null) {
            this.mParent.onOptionsMenuClosed(menu);
        }
    }

    public void openOptionsMenu() {
        if (!this.mWindow.hasFeature(RESULT_CANCELED)) {
            return;
        }
        if (this.mActionBar == null || !this.mActionBar.openOptionsMenu()) {
            this.mWindow.openPanel(RESULT_CANCELED, null);
        }
    }

    public void closeOptionsMenu() {
        if (this.mWindow.hasFeature(RESULT_CANCELED)) {
            this.mWindow.closePanel(RESULT_CANCELED);
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

    public void closeContextMenu() {
        if (this.mWindow.hasFeature(6)) {
            this.mWindow.closePanel(6);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (this.mParent != null) {
            return this.mParent.onContextItemSelected(item);
        }
        return DEBUG_LIFECYCLE;
    }

    public void onContextMenuClosed(Menu menu) {
        if (this.mParent != null) {
            this.mParent.onContextMenuClosed(menu);
        }
    }

    @Deprecated
    protected Dialog onCreateDialog(int id) {
        return null;
    }

    @Deprecated
    protected Dialog onCreateDialog(int id, Bundle args) {
        return onCreateDialog(id);
    }

    @Deprecated
    protected void onPrepareDialog(int id, Dialog dialog) {
        dialog.setOwnerActivity(this);
    }

    @Deprecated
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        onPrepareDialog(id, dialog);
    }

    @Deprecated
    public final void showDialog(int id) {
        showDialog(id, null);
    }

    @Deprecated
    public final boolean showDialog(int id, Bundle args) {
        if (this.mManagedDialogs == null) {
            this.mManagedDialogs = new SparseArray();
        }
        ManagedDialog md = (ManagedDialog) this.mManagedDialogs.get(id);
        if (md == null) {
            md = new ManagedDialog();
            md.mDialog = createDialog(Integer.valueOf(id), null, args);
            if (md.mDialog == null) {
                return DEBUG_LIFECYCLE;
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
        if (this.mManagedDialogs == null) {
            throw missingDialog(id);
        }
        ManagedDialog md = (ManagedDialog) this.mManagedDialogs.get(id);
        if (md == null) {
            throw missingDialog(id);
        }
        md.mDialog.dismiss();
    }

    private IllegalArgumentException missingDialog(int id) {
        return new IllegalArgumentException("no dialog with id " + id + " was ever " + "shown via Activity#showDialog");
    }

    @Deprecated
    public final void removeDialog(int id) {
        if (this.mManagedDialogs != null) {
            ManagedDialog md = (ManagedDialog) this.mManagedDialogs.get(id);
            if (md != null) {
                md.mDialog.dismiss();
                this.mManagedDialogs.remove(id);
            }
        }
    }

    public boolean onSearchRequested(SearchEvent searchEvent) {
        this.mSearchEvent = searchEvent;
        boolean result = onSearchRequested();
        this.mSearchEvent = null;
        return result;
    }

    public boolean onSearchRequested() {
        if ((getResources().getConfiguration().uiMode & 15) == DEFAULT_KEYS_SEARCH_GLOBAL) {
            return DEBUG_LIFECYCLE;
        }
        startSearch(null, DEBUG_LIFECYCLE, null, DEBUG_LIFECYCLE);
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
            if (this.mActionBar != null) {
                this.mMenuInflater = new MenuInflater(this.mActionBar.getThemedContext(), this);
            } else {
                this.mMenuInflater = new MenuInflater(this);
            }
        }
        return this.mMenuInflater;
    }

    public void setTheme(int resid) {
        super.setTheme(resid);
        this.mWindow.setTheme(resid);
    }

    protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
        if (this.mParent == null) {
            super.onApplyThemeResource(theme, resid, first);
        } else {
            try {
                theme.setTo(this.mParent.getTheme());
            } catch (Exception e) {
            }
            theme.applyStyle(resid, DEBUG_LIFECYCLE);
        }
        TypedArray a = theme.obtainStyledAttributes(com.android.internal.R.styleable.ActivityTaskDescription);
        if (this.mTaskDescription.getPrimaryColor() == 0) {
            int colorPrimary = a.getColor(RESULT_FIRST_USER, RESULT_CANCELED);
            if (colorPrimary != 0 && Color.alpha(colorPrimary) == Process.PROC_TERM_MASK) {
                this.mTaskDescription.setPrimaryColor(colorPrimary);
            }
        }
        if (this.mTaskDescription.getBackgroundColor() == 0) {
            int colorBackground = a.getColor(RESULT_CANCELED, RESULT_CANCELED);
            if (colorBackground != 0 && Color.alpha(colorBackground) == Process.PROC_TERM_MASK) {
                this.mTaskDescription.setBackgroundColor(colorBackground);
            }
        }
        a.recycle();
        setTaskDescription(this.mTaskDescription);
    }

    public final void requestPermissions(String[] permissions, int requestCode) {
        if (this.mHasCurrentPermissionsRequest) {
            Log.w(TAG, "Can reqeust only one set of permissions at a time");
            onRequestPermissionsResult(requestCode, new String[RESULT_CANCELED], new int[RESULT_CANCELED]);
            return;
        }
        startActivityForResult(REQUEST_PERMISSIONS_WHO_PREFIX, getPackageManager().buildRequestPermissionsIntent(permissions), requestCode, null);
        this.mHasCurrentPermissionsRequest = true;
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
        if (this.mCustActivity == null || !this.mCustActivity.isRequestSplit(this) || !this.mCustActivity.handleStartSplitActivity(intent)) {
            if (this.mParent == null) {
                if (HwSystemManager.canStartActivity(getApplicationContext(), intent)) {
                    ActivityResult ar = this.mInstrumentation.execStartActivity((Context) this, this.mMainThread.getApplicationThread(), this.mToken, this, intent, requestCode, options);
                    if (ar != null) {
                        this.mMainThread.sendActivityResult(this.mToken, this.mEmbeddedID, requestCode, ar.getResultCode(), ar.getResultData());
                    }
                    if (requestCode >= 0) {
                        this.mStartedActivity = true;
                    }
                    cancelInputsAndStartExitTransition(options);
                } else {
                    Log.i(TAG, "this app not allowed to StartActivity:" + intent);
                }
            } else if (options != null) {
                this.mParent.startActivityFromChild(this, intent, requestCode, options);
            } else {
                this.mParent.startActivityFromChild(this, intent, requestCode);
            }
        }
    }

    private void cancelInputsAndStartExitTransition(Bundle options) {
        View decor = null;
        if (this.mWindow != null) {
            decor = this.mWindow.peekDecorView();
        }
        if (decor != null) {
            decor.cancelPendingInputEvents();
        }
        if (options != null && !isTopOfTask()) {
            this.mActivityTransitionState.startExitOutTransition(this, options);
        }
    }

    public void startActivityForResultAsUser(Intent intent, int requestCode, UserHandle user) {
        startActivityForResultAsUser(intent, requestCode, null, user);
    }

    public void startActivityForResultAsUser(Intent intent, int requestCode, Bundle options, UserHandle user) {
        if (this.mParent != null) {
            throw new RuntimeException("Can't be called from a child");
        } else if (this.mCustActivity == null || !this.mCustActivity.isRequestSplit(this) || !this.mCustActivity.handleStartSplitActivity(intent)) {
            ActivityResult ar = this.mInstrumentation.execStartActivity(this, this.mMainThread.getApplicationThread(), this.mToken, this, intent, requestCode, options, user);
            if (ar != null) {
                this.mMainThread.sendActivityResult(this.mToken, this.mEmbeddedID, requestCode, ar.getResultCode(), ar.getResultData());
            }
            if (requestCode >= 0) {
                this.mStartedActivity = true;
            }
            cancelInputsAndStartExitTransition(options);
        }
    }

    public void startActivityAsUser(Intent intent, UserHandle user) {
        startActivityAsUser(intent, null, user);
    }

    public void startActivityAsUser(Intent intent, Bundle options, UserHandle user) {
        if (this.mParent != null) {
            throw new RuntimeException("Can't be called from a child");
        } else if (this.mCustActivity == null || !this.mCustActivity.isRequestSplit(this) || !this.mCustActivity.handleStartSplitActivity(intent)) {
            ActivityResult ar = this.mInstrumentation.execStartActivity(this, this.mMainThread.getApplicationThread(), this.mToken, this, intent, RESULT_OK, options, user);
            if (ar != null) {
                this.mMainThread.sendActivityResult(this.mToken, this.mEmbeddedID, RESULT_OK, ar.getResultCode(), ar.getResultData());
            }
            cancelInputsAndStartExitTransition(options);
        }
    }

    public void startActivityAsCaller(Intent intent, Bundle options, boolean ignoreTargetSecurity, int userId) {
        if (this.mParent != null) {
            throw new RuntimeException("Can't be called from a child");
        } else if (this.mCustActivity == null || !this.mCustActivity.isRequestSplit(this) || !this.mCustActivity.handleStartSplitActivity(intent)) {
            ActivityResult ar = this.mInstrumentation.execStartActivityAsCaller(this, this.mMainThread.getApplicationThread(), this.mToken, this, intent, RESULT_OK, options, ignoreTargetSecurity, userId);
            if (ar != null) {
                this.mMainThread.sendActivityResult(this.mToken, this.mEmbeddedID, RESULT_OK, ar.getResultCode(), ar.getResultData());
            }
            cancelInputsAndStartExitTransition(options);
        }
    }

    public void startIntentSenderForResult(IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws SendIntentException {
        startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, null);
    }

    public void startIntentSenderForResult(IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
        if (this.mParent == null) {
            startIntentSenderForResultInner(intent, this.mEmbeddedID, requestCode, fillInIntent, flagsMask, flagsValues, options);
        } else if (options != null) {
            this.mParent.startIntentSenderFromChild(this, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
        } else {
            this.mParent.startIntentSenderFromChild(this, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags);
        }
    }

    private void startIntentSenderForResultInner(IntentSender intent, String who, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, Bundle options) throws SendIntentException {
        String resolvedType = null;
        if (fillInIntent != null) {
            try {
                fillInIntent.migrateExtraStreamToClipData();
                fillInIntent.prepareToLeaveProcess((Context) this);
                resolvedType = fillInIntent.resolveTypeIfNeeded(getContentResolver());
            } catch (RemoteException e) {
            }
        }
        int result = ActivityManagerNative.getDefault().startActivityIntentSender(this.mMainThread.getApplicationThread(), intent, fillInIntent, resolvedType, this.mToken, who, requestCode, flagsMask, flagsValues, options);
        if (result == -6) {
            throw new SendIntentException();
        }
        Instrumentation.checkStartActivityResult(result, null);
        if (requestCode >= 0) {
            this.mStartedActivity = true;
        }
    }

    public void startActivity(Intent intent) {
        startActivity(intent, null);
    }

    public void startActivity(Intent intent, Bundle options) {
        if (options != null) {
            startActivityForResult(intent, RESULT_OK, options);
        } else {
            startActivityForResult(intent, RESULT_OK);
        }
    }

    public void startActivities(Intent[] intents) {
        startActivities(intents, null);
    }

    public void startActivities(Intent[] intents, Bundle options) {
        this.mInstrumentation.execStartActivities(this, this.mMainThread.getApplicationThread(), this.mToken, this, intents, options);
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws SendIntentException {
        startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, null);
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
        if (options != null) {
            startIntentSenderForResult(intent, RESULT_OK, fillInIntent, flagsMask, flagsValues, extraFlags, options);
        } else {
            startIntentSenderForResult(intent, RESULT_OK, fillInIntent, flagsMask, flagsValues, extraFlags);
        }
    }

    public boolean startActivityIfNeeded(Intent intent, int requestCode) {
        return startActivityIfNeeded(intent, requestCode, null);
    }

    public boolean startActivityIfNeeded(Intent intent, int requestCode, Bundle options) {
        if (this.mParent == null) {
            int result = RESULT_FIRST_USER;
            try {
                Parcelable referrer = onProvideReferrer();
                if (referrer != null) {
                    intent.putExtra(Intent.EXTRA_REFERRER, referrer);
                }
                intent.migrateExtraStreamToClipData();
                intent.prepareToLeaveProcess((Context) this);
                result = ActivityManagerNative.getDefault().startActivity(this.mMainThread.getApplicationThread(), getBasePackageName(), intent, intent.resolveTypeIfNeeded(getContentResolver()), this.mToken, this.mEmbeddedID, requestCode, RESULT_FIRST_USER, null, options);
            } catch (RemoteException e) {
            }
            Instrumentation.checkStartActivityResult(result, intent);
            if (requestCode >= 0) {
                this.mStartedActivity = true;
            }
            return result != RESULT_FIRST_USER ? true : DEBUG_LIFECYCLE;
        } else {
            throw new UnsupportedOperationException("startActivityIfNeeded can only be called from a top-level activity");
        }
    }

    public boolean startNextMatchingActivity(Intent intent) {
        return startNextMatchingActivity(intent, null);
    }

    public boolean startNextMatchingActivity(Intent intent, Bundle options) {
        if (this.mParent == null) {
            try {
                intent.migrateExtraStreamToClipData();
                intent.prepareToLeaveProcess((Context) this);
                return ActivityManagerNative.getDefault().startNextMatchingActivity(this.mToken, intent, options);
            } catch (RemoteException e) {
                return DEBUG_LIFECYCLE;
            }
        }
        throw new UnsupportedOperationException("startNextMatchingActivity can only be called from a top-level activity");
    }

    public void startActivityFromChild(Activity child, Intent intent, int requestCode) {
        startActivityFromChild(child, intent, requestCode, null);
    }

    public void startActivityFromChild(Activity child, Intent intent, int requestCode, Bundle options) {
        if (this.mCustActivity == null || !this.mCustActivity.isRequestSplit(this) || !this.mCustActivity.handleStartSplitActivity(intent)) {
            if (HwSystemManager.canStartActivity(getApplicationContext(), intent)) {
                ActivityResult ar = this.mInstrumentation.execStartActivity((Context) this, this.mMainThread.getApplicationThread(), this.mToken, child, intent, requestCode, options);
                if (ar != null) {
                    this.mMainThread.sendActivityResult(this.mToken, child.mEmbeddedID, requestCode, ar.getResultCode(), ar.getResultData());
                }
                cancelInputsAndStartExitTransition(options);
                return;
            }
            Log.i(TAG, "this app not allowed to start activity:" + intent);
        }
    }

    public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
        startActivityFromFragment(fragment, intent, requestCode, null);
    }

    public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options) {
        if (HwSystemManager.canStartActivity(getApplicationContext(), intent)) {
            startActivityForResult(fragment.mWho, intent, requestCode, options);
        } else {
            Log.i(TAG, "this app not allowed to start activity:" + intent);
        }
    }

    public void startActivityForResult(String who, Intent intent, int requestCode, Bundle options) {
        Parcelable referrer = onProvideReferrer();
        if (referrer != null) {
            intent.putExtra(Intent.EXTRA_REFERRER, referrer);
        }
        if (this.mCustActivity == null || !this.mCustActivity.isRequestSplit(this) || !this.mCustActivity.handleStartSplitActivity(intent)) {
            ActivityResult ar = this.mInstrumentation.execStartActivity((Context) this, this.mMainThread.getApplicationThread(), this.mToken, who, intent, requestCode, options);
            if (ar != null) {
                this.mMainThread.sendActivityResult(this.mToken, who, requestCode, ar.getResultCode(), ar.getResultData());
            }
            cancelInputsAndStartExitTransition(options);
        }
    }

    public boolean canStartActivityForResult() {
        return true;
    }

    public void startIntentSenderFromChild(Activity child, IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws SendIntentException {
        startIntentSenderFromChild(child, intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, null);
    }

    public void startIntentSenderFromChild(Activity child, IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
        startIntentSenderForResultInner(intent, child.mEmbeddedID, requestCode, fillInIntent, flagsMask, flagsValues, options);
    }

    public void startIntentSenderFromChildFragment(Fragment child, IntentSender intent, int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
        startIntentSenderForResultInner(intent, child.mWho, requestCode, fillInIntent, flagsMask, flagsValues, options);
    }

    public void overridePendingTransition(int enterAnim, int exitAnim) {
        try {
            ActivityManagerNative.getDefault().overridePendingTransition(this.mToken, getPackageName(), enterAnim, exitAnim);
        } catch (RemoteException e) {
        }
    }

    public void setExitPosition(int startX, int startY, int width, int height) {
        try {
            ActivityManagerNative.getDefault().setExitPosition(startX, startY, width, height);
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
                return new Builder().scheme("android-app").authority(this.mReferrer).build();
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
            return ActivityManagerNative.getDefault().getCallingPackage(this.mToken);
        } catch (RemoteException e) {
            return null;
        }
    }

    public ComponentName getCallingActivity() {
        try {
            return ActivityManagerNative.getDefault().getCallingActivity(this.mToken);
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
                this.mDecor.setVisibility(DEFAULT_KEYS_SEARCH_GLOBAL);
            }
        }
    }

    void makeVisible() {
        if (!this.mWindowAdded) {
            getWindowManager().addView(this.mDecor, getWindow().getAttributes());
            this.mWindowAdded = true;
        }
        this.mDecor.setVisibility(RESULT_CANCELED);
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
        } else if (Looper.myLooper() != this.mMainThread.getLooper()) {
            throw new IllegalStateException("Must be called from main thread");
        } else {
            this.mMainThread.requestRelaunchActivity(this.mToken, null, null, RESULT_CANCELED, DEBUG_LIFECYCLE, null, null, DEBUG_LIFECYCLE, DEBUG_LIFECYCLE);
        }
    }

    private void finish(int finishTask) {
        if (this.mParent == null) {
            int resultCode;
            Intent resultData;
            synchronized (this) {
                resultCode = this.mResultCode;
                resultData = this.mResultData;
            }
            if (resultData != null) {
                try {
                    resultData.prepareToLeaveProcess((Context) this);
                } catch (RemoteException e) {
                    return;
                }
            }
            if (ActivityManagerNative.getDefault().finishActivity(this.mToken, resultCode, resultData, finishTask)) {
                this.mFinished = true;
                return;
            }
            return;
        }
        this.mParent.finishFromChild(this);
    }

    public void finish() {
        finish(RESULT_CANCELED);
        if (this.mCustActivity != null && this.mCustActivity.isRequestSplit(this)) {
            this.mCustActivity.splitActivityFinish();
        }
    }

    public void finishAffinity() {
        if (this.mParent != null) {
            throw new IllegalStateException("Can not be called from an embedded activity");
        } else if (this.mResultCode == 0 && this.mResultData == null) {
            try {
                if (ActivityManagerNative.getDefault().finishActivityAffinity(this.mToken)) {
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
        if (this.mParent == null) {
            try {
                ActivityManagerNative.getDefault().finishSubActivity(this.mToken, this.mEmbeddedID, requestCode);
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        this.mParent.finishActivityFromChild(this, requestCode);
    }

    public void finishActivityFromChild(Activity child, int requestCode) {
        try {
            ActivityManagerNative.getDefault().finishSubActivity(this.mToken, child.mEmbeddedID, requestCode);
        } catch (RemoteException e) {
        }
    }

    public void finishAndRemoveTask() {
        finish(RESULT_FIRST_USER);
    }

    public boolean releaseInstance() {
        try {
            return ActivityManagerNative.getDefault().releaseActivityInstance(this.mToken);
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public void onActivityReenter(int resultCode, Intent data) {
    }

    public PendingIntent createPendingResult(int requestCode, Intent data, int flags) {
        String packageName = getPackageName();
        try {
            data.prepareToLeaveProcess((Context) this);
            IActivityManager iActivityManager = ActivityManagerNative.getDefault();
            IBinder iBinder = this.mParent == null ? this.mToken : this.mParent.mToken;
            String str = this.mEmbeddedID;
            Intent[] intentArr = new Intent[RESULT_FIRST_USER];
            intentArr[RESULT_CANCELED] = data;
            IIntentSender target = iActivityManager.getIntentSender(DEFAULT_KEYS_SEARCH_LOCAL, packageName, iBinder, str, requestCode, intentArr, null, flags, null, UserHandle.myUserId());
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
            Slog.d(TAG, "createPendingResult catch RemoteException: " + e.toString() + " callers=" + Debug.getCallers(5));
            return null;
        }
    }

    public void setRequestedOrientation(int requestedOrientation) {
        if (this.mParent == null) {
            try {
                ActivityManagerNative.getDefault().setRequestedOrientation(this.mToken, requestedOrientation);
            } catch (RemoteException e) {
            }
        } else {
            this.mParent.setRequestedOrientation(requestedOrientation);
        }
        if (this.mCustActivity != null && this.mCustActivity.isRequestSplit(this)) {
            this.mCustActivity.setSplitActivityOrientation(requestedOrientation);
        }
    }

    public int getRequestedOrientation() {
        if (this.mParent != null) {
            return this.mParent.getRequestedOrientation();
        }
        try {
            return ActivityManagerNative.getDefault().getRequestedOrientation(this.mToken);
        } catch (RemoteException e) {
            return RESULT_OK;
        }
    }

    public int getTaskId() {
        try {
            return ActivityManagerNative.getDefault().getTaskForActivity(this.mToken, DEBUG_LIFECYCLE);
        } catch (RemoteException e) {
            return RESULT_OK;
        }
    }

    public boolean isTaskRoot() {
        boolean z = true;
        try {
            if (ActivityManagerNative.getDefault().getTaskForActivity(this.mToken, true) < 0) {
                z = DEBUG_LIFECYCLE;
            }
            return z;
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        }
    }

    public boolean moveTaskToBack(boolean nonRoot) {
        try {
            return ActivityManagerNative.getDefault().moveActivityTaskToBack(this.mToken, nonRoot);
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        }
    }

    public String getLocalClassName() {
        String pkg = getPackageName();
        String cls = this.mComponent.getClassName();
        int packageLen = pkg.length();
        if (cls.startsWith(pkg) && cls.length() > packageLen && cls.charAt(packageLen) == '.') {
            return cls.substring(packageLen + RESULT_FIRST_USER);
        }
        return cls;
    }

    public ComponentName getComponentName() {
        return this.mComponent;
    }

    public SharedPreferences getPreferences(int mode) {
        return getSharedPreferences(getLocalClassName(), mode);
    }

    private void ensureSearchManager() {
        if (this.mSearchManager == null) {
            this.mSearchManager = new SearchManager(this, null);
        }
    }

    public Object getSystemService(String name) {
        if (getBaseContext() == null) {
            throw new IllegalStateException("System services not available to Activities before onCreate()");
        } else if (AppAssociate.ASSOC_WINDOW.equals(name)) {
            return this.mWindowManager;
        } else {
            if (!Searches.SEARCH.equals(name)) {
                return super.getSystemService(name);
            }
            ensureSearchManager();
            return this.mSearchManager;
        }
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        onTitleChanged(title, this.mTitleColor);
        if (this.mParent != null) {
            this.mParent.onChildTitleChanged(this, title);
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

    protected void onTitleChanged(CharSequence title, int color) {
        if (this.mTitleReady) {
            Window win = getWindow();
            if (win != null) {
                win.setTitle(title);
                if (color != 0) {
                    win.setTitleColor(color);
                }
            }
            if (this.mActionBar != null) {
                this.mActionBar.setWindowTitle(title);
            }
        }
    }

    protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
    }

    public void setTaskDescription(TaskDescription taskDescription) {
        if (this.mTaskDescription != taskDescription) {
            this.mTaskDescription.copyFrom(taskDescription);
            if (taskDescription.getIconFilename() == null && taskDescription.getIcon() != null) {
                int size = ActivityManager.getLauncherLargeIconSizeInner(this);
                this.mTaskDescription.setIcon(Bitmap.createScaledBitmap(taskDescription.getIcon(), size, size, true));
            }
        }
        try {
            ActivityManagerNative.getDefault().setTaskDescription(this.mToken, this.mTaskDescription);
        } catch (RemoteException e) {
        }
    }

    @Deprecated
    public final void setProgressBarVisibility(boolean visible) {
        int i;
        Window window = getWindow();
        if (visible) {
            i = RESULT_OK;
        } else {
            i = -2;
        }
        window.setFeatureInt(FINISH_TASK_WITH_ACTIVITY, i);
    }

    @Deprecated
    public final void setProgressBarIndeterminateVisibility(boolean visible) {
        getWindow().setFeatureInt(5, visible ? RESULT_OK : -2);
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
        window.setFeatureInt(FINISH_TASK_WITH_ACTIVITY, i);
    }

    @Deprecated
    public final void setProgress(int progress) {
        getWindow().setFeatureInt(FINISH_TASK_WITH_ACTIVITY, progress + RESULT_CANCELED);
    }

    @Deprecated
    public final void setSecondaryProgress(int secondaryProgress) {
        getWindow().setFeatureInt(FINISH_TASK_WITH_ACTIVITY, secondaryProgress + Events.EVENT_BASE_MEM);
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

    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }

    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if ("fragment".equals(name)) {
            return this.mFragments.onCreateView(parent, name, context, attrs);
        }
        return onCreateView(name, context, attrs);
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        dumpInner(prefix, fd, writer, args);
    }

    void dumpInner(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
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
        if (this.mVoiceInteractor != null) {
            this.mVoiceInteractor.dump(innerPrefix, fd, writer, args);
        }
        if (!(getWindow() == null || getWindow().peekDecorView() == null || getWindow().peekDecorView().getViewRootImpl() == null)) {
            getWindow().peekDecorView().getViewRootImpl().dump(prefix, fd, writer, args);
        }
        this.mHandler.getLooper().dump(new PrintWriterPrinter(writer), prefix);
    }

    public boolean isImmersive() {
        try {
            return ActivityManagerNative.getDefault().isImmersive(this.mToken);
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        }
    }

    private boolean isTopOfTask() {
        if (this.mToken == null || this.mWindow == null) {
            return DEBUG_LIFECYCLE;
        }
        try {
            return ActivityManagerNative.getDefault().isTopOfTask(getActivityToken());
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        }
    }

    public void convertFromTranslucent() {
        try {
            this.mTranslucentCallback = null;
            if (ActivityManagerNative.getDefault().convertFromTranslucent(this.mToken)) {
                WindowManagerGlobal.getInstance().changeCanvasOpacity(this.mToken, true);
            }
        } catch (RemoteException e) {
        }
    }

    public boolean convertToTranslucent(TranslucentConversionListener callback, ActivityOptions options) {
        boolean drawComplete;
        try {
            this.mTranslucentCallback = callback;
            this.mChangeCanvasToTranslucent = ActivityManagerNative.getDefault().convertToTranslucent(this.mToken, options);
            WindowManagerGlobal.getInstance().changeCanvasOpacity(this.mToken, DEBUG_LIFECYCLE);
            drawComplete = true;
        } catch (RemoteException e) {
            this.mChangeCanvasToTranslucent = DEBUG_LIFECYCLE;
            drawComplete = DEBUG_LIFECYCLE;
        }
        if (!(this.mChangeCanvasToTranslucent || this.mTranslucentCallback == null)) {
            this.mTranslucentCallback.onTranslucentConversionComplete(drawComplete);
        }
        return this.mChangeCanvasToTranslucent;
    }

    void onTranslucentConversionComplete(boolean drawComplete) {
        if (this.mTranslucentCallback != null) {
            this.mTranslucentCallback.onTranslucentConversionComplete(drawComplete);
            this.mTranslucentCallback = null;
        }
        if (this.mChangeCanvasToTranslucent) {
            WindowManagerGlobal.getInstance().changeCanvasOpacity(this.mToken, DEBUG_LIFECYCLE);
        }
    }

    public void onNewActivityOptions(ActivityOptions options) {
        this.mActivityTransitionState.setEnterActivityOptions(this, options);
        if (!this.mStopped) {
            this.mActivityTransitionState.enterReady(this);
        }
    }

    ActivityOptions getActivityOptions() {
        try {
            return ActivityManagerNative.getDefault().getActivityOptions(this.mToken);
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean requestVisibleBehind(boolean visible) {
        if (!this.mResumed) {
            visible = DEBUG_LIFECYCLE;
        }
        try {
            if (!ActivityManagerNative.getDefault().requestVisibleBehind(this.mToken, visible)) {
                visible = DEBUG_LIFECYCLE;
            }
            this.mVisibleBehind = visible;
        } catch (RemoteException e) {
            this.mVisibleBehind = DEBUG_LIFECYCLE;
        }
        return this.mVisibleBehind;
    }

    public void onVisibleBehindCanceled() {
        this.mCalled = true;
    }

    public boolean isBackgroundVisibleBehind() {
        try {
            return ActivityManagerNative.getDefault().isBackgroundVisibleBehind(this.mToken);
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        }
    }

    public void onBackgroundVisibleBehindChanged(boolean visible) {
    }

    public void onEnterAnimationComplete() {
    }

    public void dispatchEnterAnimationComplete() {
        onEnterAnimationComplete();
        if (getWindow() != null && getWindow().getDecorView() != null) {
            getWindow().getDecorView().getViewTreeObserver().dispatchOnEnterAnimationComplete();
        }
    }

    public void setImmersive(boolean i) {
        try {
            ActivityManagerNative.getDefault().setImmersive(this.mToken, i);
        } catch (RemoteException e) {
        }
    }

    public void setVrModeEnabled(boolean enabled, ComponentName requestedComponent) throws NameNotFoundException {
        try {
            if (ActivityManagerNative.getDefault().setVrMode(this.mToken, enabled, requestedComponent) != 0) {
                throw new NameNotFoundException(requestedComponent.flattenToString());
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

    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        if (this.mActionModeTypeStarting == 0) {
            initWindowDecorActionBar();
            if (this.mActionBar != null) {
                return this.mActionBar.startActionMode(callback);
            }
        }
        return null;
    }

    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        try {
            this.mActionModeTypeStarting = type;
            ActionMode onWindowStartingActionMode = onWindowStartingActionMode(callback);
            return onWindowStartingActionMode;
        } finally {
            this.mActionModeTypeStarting = RESULT_CANCELED;
        }
    }

    public void onActionModeStarted(ActionMode mode) {
    }

    public void onActionModeFinished(ActionMode mode) {
    }

    public boolean shouldUpRecreateTask(Intent targetIntent) {
        try {
            PackageManager pm = getPackageManager();
            ComponentName cn = targetIntent.getComponent();
            if (cn == null) {
                cn = targetIntent.resolveActivity(pm);
            }
            ActivityInfo info = pm.getActivityInfo(cn, RESULT_CANCELED);
            if (info.taskAffinity == null) {
                return DEBUG_LIFECYCLE;
            }
            return ActivityManagerNative.getDefault().shouldUpRecreateTask(this.mToken, info.taskAffinity);
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
        } catch (NameNotFoundException e2) {
            return DEBUG_LIFECYCLE;
        }
    }

    public boolean navigateUpTo(Intent upIntent) {
        if (this.mParent != null) {
            return this.mParent.navigateUpToFromChild(this, upIntent);
        }
        if (upIntent.getComponent() == null) {
            ComponentName destInfo = upIntent.resolveActivity(getPackageManager());
            if (destInfo == null) {
                return DEBUG_LIFECYCLE;
            }
            Intent upIntent2 = new Intent(upIntent);
            upIntent2.setComponent(destInfo);
            upIntent = upIntent2;
        }
        synchronized (this) {
            int resultCode = this.mResultCode;
            Intent resultData = this.mResultData;
        }
        if (resultData != null) {
            resultData.prepareToLeaveProcess((Context) this);
        }
        try {
            upIntent.prepareToLeaveProcess((Context) this);
            return ActivityManagerNative.getDefault().navigateUpTo(this.mToken, upIntent, resultCode, resultData);
        } catch (RemoteException e) {
            return DEBUG_LIFECYCLE;
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
        ComponentName target = new ComponentName((Context) this, parentName);
        try {
            Intent parentIntent;
            if (getPackageManager().getActivityInfo(target, RESULT_CANCELED).parentActivityName == null) {
                parentIntent = Intent.makeMainActivity(target);
            } else {
                parentIntent = new Intent().setComponent(target);
            }
            return parentIntent;
        } catch (NameNotFoundException e) {
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

    final void setParent(Activity parent) {
        this.mParent = parent;
    }

    final void attach(Context context, ActivityThread aThread, Instrumentation instr, IBinder token, int ident, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, NonConfigurationInstances lastNonConfigurationInstances, Configuration config, String referrer, IVoiceInteractor voiceInteractor, Window window) {
        attachBaseContext(context);
        this.mFragments.attachHost(null);
        this.mWindow = HwPolicyFactory.getHwPhoneWindow(this, window);
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
        this.mWindow.setWindowManager((WindowManager) context.getSystemService(AppAssociate.ASSOC_WINDOW), this.mToken, this.mComponent.flattenToString(), (info.flags & Document.FLAG_VIRTUAL_DOCUMENT) != 0 ? true : DEBUG_LIFECYCLE);
        if (this.mParent != null) {
            this.mWindow.setContainer(this.mParent.getWindow());
        }
        this.mWindowManager = this.mWindow.getWindowManager();
        this.mCurrentConfig = config;
    }

    public final IBinder getActivityToken() {
        return this.mParent != null ? this.mParent.getActivityToken() : this.mToken;
    }

    final void performCreateCommon() {
        boolean z = DEBUG_LIFECYCLE;
        if (!this.mWindow.getWindowStyle().getBoolean(10, DEBUG_LIFECYCLE)) {
            z = true;
        }
        this.mVisibleFromClient = z;
        this.mFragments.dispatchActivityCreated();
        this.mActivityTransitionState.setEnterActivityOptions(this, getActivityOptions());
    }

    final void performCreate(Bundle icicle) {
        restoreHasCurrentPermissionRequest(icicle);
        Trace.traceBegin(64, "onCreate");
        onCreate(icicle);
        Trace.traceEnd(64);
        this.mActivityTransitionState.readState(icicle);
        performCreateCommon();
    }

    final void performCreate(Bundle icicle, PersistableBundle persistentState) {
        restoreHasCurrentPermissionRequest(icicle);
        Trace.traceBegin(64, "onCreate_persist");
        onCreate(icicle, persistentState);
        Trace.traceEnd(64);
        this.mActivityTransitionState.readState(icicle);
        performCreateCommon();
    }

    final void performStart() {
        this.mActivityTransitionState.setEnterActivityOptions(this, getActivityOptions());
        this.mFragments.noteStateNotSaved();
        this.mCalled = DEBUG_LIFECYCLE;
        this.mFragments.execPendingActions();
        this.mInstrumentation.callActivityOnStart(this);
        if (this.mCalled) {
            boolean isAppDebuggable;
            this.mFragments.dispatchStart();
            this.mFragments.reportLoaderStart();
            boolean isDlwarningEnabled = SystemProperties.getInt("ro.bionic.ld.warning", RESULT_CANCELED) == RESULT_FIRST_USER ? true : DEBUG_LIFECYCLE;
            if ((this.mApplication.getApplicationInfo().flags & FINISH_TASK_WITH_ACTIVITY) != 0) {
                isAppDebuggable = true;
            } else {
                isAppDebuggable = DEBUG_LIFECYCLE;
            }
            if (isAppDebuggable || isDlwarningEnabled) {
                String dlwarning = getDlWarning();
                if (dlwarning != null) {
                    CharSequence appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
                    CharSequence warning = "Detected problems with app native libraries\n(please consult log for detail):\n" + dlwarning;
                    if (isAppDebuggable) {
                        new AlertDialog.Builder(this).setTitle(appName).setMessage(warning).setPositiveButton((int) R.string.ok, null).setCancelable(DEBUG_LIFECYCLE).show();
                    } else {
                        Toast.makeText(this, appName + "\n" + warning, RESULT_FIRST_USER).show();
                    }
                }
            }
            this.mActivityTransitionState.enterReady(this);
            return;
        }
        throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onStart()");
    }

    final void performRestart() {
        this.mFragments.noteStateNotSaved();
        if (this.mToken != null && this.mParent == null) {
            WindowManagerGlobal.getInstance().setStoppedState(this.mToken, DEBUG_LIFECYCLE);
        }
        if (this.mStopped) {
            this.mStopped = DEBUG_LIFECYCLE;
            synchronized (this.mManagedCursors) {
                int N = this.mManagedCursors.size();
                for (int i = RESULT_CANCELED; i < N; i += RESULT_FIRST_USER) {
                    ManagedCursor mc = (ManagedCursor) this.mManagedCursors.get(i);
                    if (mc.mReleased || mc.mUpdated) {
                        if (mc.mCursor.requery() || getApplicationInfo().targetSdkVersion < 14) {
                            mc.mReleased = DEBUG_LIFECYCLE;
                            mc.mUpdated = DEBUG_LIFECYCLE;
                        } else {
                            throw new IllegalStateException("trying to requery an already closed cursor  " + mc.mCursor);
                        }
                    }
                }
            }
            this.mCalled = DEBUG_LIFECYCLE;
            this.mInstrumentation.callActivityOnRestart(this);
            if (this.mCalled) {
                performStart();
                return;
            }
            throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onRestart()");
        }
    }

    final void performResume() {
        performRestart();
        this.mFragments.execPendingActions();
        this.mLastNonConfigurationInstances = null;
        this.mCalled = DEBUG_LIFECYCLE;
        this.mInstrumentation.callActivityOnResume(this);
        if (this.mCalled) {
            if (!(this.mVisibleFromClient || this.mFinished)) {
                Log.w(TAG, "An activity without a UI must call finish() before onResume() completes");
                if (getApplicationInfo().targetSdkVersion > 22 && !"com.android.server.telecom".equals(getPackageName())) {
                    throw new IllegalStateException("Activity " + this.mComponent.toShortString() + " did not call finish() prior to onResume() completing");
                }
            }
            this.mCalled = DEBUG_LIFECYCLE;
            this.mFragments.dispatchResume();
            this.mFragments.execPendingActions();
            onPostResume();
            if (this.mCalled) {
                String str;
                if (!(this.mIsFullFlag || this.mWindow == null || (this.mWindow.getAttributes().flags & Document.FLAG_SUPPORTS_REMOVE) == 0)) {
                    this.mIsFullFlag = true;
                    LogPower.push(BluetoothAssignedNumbers.NIKE, getPackageName());
                }
                if (Utils.isActivityHardwareAccelerated(this.mActivityInfo, this.mWindow)) {
                    str = WifiEnterpriseConfig.ENGINE_ENABLE;
                } else {
                    str = WifiEnterpriseConfig.ENGINE_DISABLE;
                }
                LogPower.push(Const.CODE_C1_DF1, str);
                return;
            }
            throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onPostResume()");
        }
        throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onResume()");
    }

    final void performPause() {
        this.mDoReportFullyDrawn = DEBUG_LIFECYCLE;
        this.mFragments.dispatchPause();
        this.mCalled = DEBUG_LIFECYCLE;
        Trace.traceBegin(64, "onPause");
        onPause();
        Trace.traceEnd(64);
        this.mResumed = DEBUG_LIFECYCLE;
        if (this.mCalled || getApplicationInfo().targetSdkVersion < 9) {
            this.mResumed = DEBUG_LIFECYCLE;
            if (this.mIsFullFlag) {
                this.mIsFullFlag = DEBUG_LIFECYCLE;
                LogPower.push(Const.CODE_C3_SKIP4_RANGE_END, getPackageName());
                return;
            }
            return;
        }
        throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onPause()");
    }

    final void performUserLeaving() {
        onUserInteraction();
        onUserLeaveHint();
    }

    final void performStop(boolean preserveWindow) {
        this.mDoReportFullyDrawn = DEBUG_LIFECYCLE;
        this.mFragments.doLoaderStop(this.mChangingConfigurations);
        if (!this.mStopped) {
            if (this.mWindow != null) {
                this.mWindow.closeAllPanels();
            }
            if (!(preserveWindow || this.mToken == null || this.mParent != null)) {
                WindowManagerGlobal.getInstance().setStoppedState(this.mToken, true);
            }
            this.mFragments.dispatchStop();
            this.mCalled = DEBUG_LIFECYCLE;
            this.mInstrumentation.callActivityOnStop(this);
            if (this.mCalled) {
                synchronized (this.mManagedCursors) {
                    int N = this.mManagedCursors.size();
                    for (int i = RESULT_CANCELED; i < N; i += RESULT_FIRST_USER) {
                        ManagedCursor mc = (ManagedCursor) this.mManagedCursors.get(i);
                        if (!mc.mReleased) {
                            mc.mCursor.deactivate();
                            mc.mReleased = true;
                        }
                    }
                }
                this.mStopped = true;
            } else {
                throw new SuperNotCalledException("Activity " + this.mComponent.toShortString() + " did not call through to super.onStop()");
            }
        }
        this.mResumed = DEBUG_LIFECYCLE;
    }

    final void performDestroy() {
        this.mDestroyed = true;
        this.mWindow.destroy();
        this.mFragments.dispatchDestroy();
        Trace.traceBegin(64, "onDestroy");
        onDestroy();
        Trace.traceEnd(64);
        this.mFragments.doLoaderDestroy();
        if (this.mVoiceInteractor != null) {
            this.mVoiceInteractor.detachActivity();
        }
    }

    final void dispatchMultiWindowModeChanged(boolean isInMultiWindowMode) {
        this.mFragments.dispatchMultiWindowModeChanged(isInMultiWindowMode);
        if (this.mWindow != null) {
            this.mWindow.onMultiWindowModeChanged();
        }
        onMultiWindowModeChanged(isInMultiWindowMode);
    }

    final void dispatchPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        this.mFragments.dispatchPictureInPictureModeChanged(isInPictureInPictureMode);
        onPictureInPictureModeChanged(isInPictureInPictureMode);
    }

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
            this.mHasCurrentPermissionsRequest = bundle.getBoolean(HAS_CURENT_PERMISSIONS_REQUEST_KEY, DEBUG_LIFECYCLE);
        }
    }

    void dispatchActivityResult(String who, int requestCode, int resultCode, Intent data) {
        this.mFragments.noteStateNotSaved();
        if (who == null) {
            onActivityResult(requestCode, resultCode, data);
        } else if (who.startsWith(REQUEST_PERMISSIONS_WHO_PREFIX)) {
            who = who.substring(REQUEST_PERMISSIONS_WHO_PREFIX.length());
            if (TextUtils.isEmpty(who)) {
                dispatchRequestPermissionsResult(requestCode, data);
            } else {
                frag = this.mFragments.findFragmentByWho(who);
                if (frag != null) {
                    dispatchRequestPermissionsResultToFragment(requestCode, data, frag);
                }
            }
        } else if (who.startsWith("@android:view:")) {
            for (ViewRootImpl viewRoot : WindowManagerGlobal.getInstance().getRootViews(getActivityToken())) {
                if (viewRoot.getView() != null && viewRoot.getView().dispatchActivityResult(who, requestCode, resultCode, data)) {
                    return;
                }
            }
        } else {
            frag = this.mFragments.findFragmentByWho(who);
            if (frag != null) {
                frag.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public void startLockTask() {
        try {
            ActivityManagerNative.getDefault().startLockTaskMode(this.mToken);
        } catch (RemoteException e) {
        }
    }

    public void stopLockTask() {
        try {
            ActivityManagerNative.getDefault().stopLockTaskMode();
        } catch (RemoteException e) {
        }
    }

    public void showLockTaskEscapeMessage() {
        try {
            ActivityManagerNative.getDefault().showLockTaskEscapeMessage(this.mToken);
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
        this.mHasCurrentPermissionsRequest = DEBUG_LIFECYCLE;
        if (data != null) {
            permissions = data.getStringArrayExtra(PackageManager.EXTRA_REQUEST_PERMISSIONS_NAMES);
        } else {
            permissions = new String[RESULT_CANCELED];
        }
        if (data != null) {
            grantResults = data.getIntArrayExtra(PackageManager.EXTRA_REQUEST_PERMISSIONS_RESULTS);
        } else {
            grantResults = new int[RESULT_CANCELED];
        }
        onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void dispatchRequestPermissionsResultToFragment(int requestCode, Intent data, Fragment fragment) {
        String[] permissions;
        int[] grantResults;
        if (data != null) {
            permissions = data.getStringArrayExtra(PackageManager.EXTRA_REQUEST_PERMISSIONS_NAMES);
        } else {
            permissions = new String[RESULT_CANCELED];
        }
        if (data != null) {
            grantResults = data.getIntArrayExtra(PackageManager.EXTRA_REQUEST_PERMISSIONS_RESULTS);
        } else {
            grantResults = new int[RESULT_CANCELED];
        }
        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
