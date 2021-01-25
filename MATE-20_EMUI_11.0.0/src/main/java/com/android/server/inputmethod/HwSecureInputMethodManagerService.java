package com.android.server.inputmethod;

import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Matrix;
import android.hardware.display.DisplayManagerInternal;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.style.SuggestionSpan;
import android.util.ArrayMap;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.IInputContentUriToken;
import com.android.internal.inputmethod.IInputMethodPrivilegedOperations;
import com.android.internal.inputmethod.InputMethodDebug;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.InputBindResult;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.inputmethod.InputMethodUtils;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.android.inputmethod.IHwSecureInputMethodManager;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.xmlpull.v1.XmlPullParserException;

public class HwSecureInputMethodManagerService extends AbsInputMethodManagerService implements ServiceConnection, Handler.Callback {
    static final boolean DEBUG = false;
    static final boolean DEBUG_FLOW = Log.HWINFO;
    static final boolean DEBUG_RESTORE = false;
    static final int MSG_BIND_CLIENT = 3010;
    static final int MSG_BIND_INPUT = 1010;
    static final int MSG_CREATE_SESSION = 1050;
    static final int MSG_HIDE_SOFT_INPUT = 1030;
    static final int MSG_INITIALIZE_IME = 1040;
    static final int MSG_REPORT_FULLSCREEN_MODE = 3045;
    static final int MSG_SET_ACTIVE = 3020;
    static final int MSG_SET_INTERACTIVE = 3030;
    static final int MSG_SHOULD_SET_ACTIVE = 3060;
    static final int MSG_SHOW_SOFT_INPUT = 1020;
    static final int MSG_START_INPUT = 2000;
    static final int MSG_SYSTEM_UNLOCK_USER = 5000;
    static final int MSG_UNBIND_CLIENT = 3000;
    static final int MSG_UNBIND_INPUT = 1000;
    private static final int NOT_A_SUBTYPE_ID = -1;
    static final int SECURE_SUGGESTION_SPANS_MAX_SIZE = 20;
    private static final String SEC_IME_ID = "com.huawei.secime/.SoftKeyboard";
    static final String TAG = "SecInputMethodManagerService";
    static final long TIME_TO_RECONNECT = 3000;
    static final int UNBIND_SECIME_IF_SHOULD = 10000;
    private final AppOpsManager mAppOpsManager;
    int mBackDisposition = 0;
    boolean mBoundToMethod;
    final HandlerCaller mCaller;
    final ArrayMap<IBinder, ClientState> mClients = new ArrayMap<>();
    final Context mContext;
    EditorInfo mCurAttribute;
    ClientState mCurClient;
    private boolean mCurClientInKeyguard;
    IBinder mCurFocusedWindow;
    ClientState mCurFocusedWindowClient;
    int mCurFocusedWindowSoftInputMode;
    String mCurId;
    IInputContext mCurInputContext;
    int mCurInputContextMissingMethods;
    Intent mCurIntent;
    IInputMethod mCurMethod;
    String mCurMethodId;
    int mCurSeq;
    IBinder mCurToken;
    int mCurTokenDisplayId = -1;
    SessionState mEnabledSession;
    final Handler mHandler;
    boolean mHaveConnection;
    HwInnerSecureInputMethodManagerService mHwInnerService = new HwInnerSecureInputMethodManagerService(this);
    private final IPackageManager mIPackageManager = AppGlobals.getPackageManager();
    final IWindowManager mIWindowManager;
    final ImeDisplayValidator mImeDisplayValidator;
    @GuardedBy({"mMethodMap"})
    private final WeakHashMap<IBinder, IBinder> mImeTargetWindowMap = new WeakHashMap<>();
    int mImeWindowVis;
    boolean mInFullscreenMode;
    boolean mInputShown;
    boolean mIsInteractive = true;
    boolean mIsVrImeStarted = false;
    private KeyguardManager mKeyguardManager;
    long mLastBindTime;
    private LocaleList mLastSystemLocales;
    final ArrayList<InputMethodInfo> mMethodList = new ArrayList<>();
    final ArrayMap<String, InputMethodInfo> mMethodMap = new ArrayMap<>();
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor();
    final InputBindResult mNoBinding = new InputBindResult(-1, (IInputMethodSession) null, (InputChannel) null, (String) null, -1, (Matrix) null);
    final Resources mRes;
    private SecureSettingsObserver mSecureSettingsObserver;
    private final LruCache<SuggestionSpan, InputMethodInfo> mSecureSuggestionSpans = new LruCache<>(20);
    final InputMethodUtils.InputMethodSettings mSettings;
    private boolean mShouldSetActive;
    boolean mShowExplicitlyRequested;
    boolean mShowForced;
    boolean mShowRequested;
    private final String mSlotIme;
    private StatusBarManagerService mStatusBar;
    boolean mSystemReady = false;
    private int mUnbindCounter = 0;
    private final UserManager mUserManager;
    boolean mVisibleBound = false;
    final ServiceConnection mVisibleConnection = new ServiceConnection() {
        /* class com.android.server.inputmethod.HwSecureInputMethodManagerService.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName name) {
            HwSecureInputMethodManagerService.this.unbindVisibleConnection();
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    final Object mVisibleServiceLock = new Object();
    final WindowManagerInternal mWindowManagerInternal;

    /* access modifiers changed from: package-private */
    @FunctionalInterface
    public interface ImeDisplayValidator {
        boolean displayCanShowIme(int i);
    }

    /* access modifiers changed from: protected */
    public boolean isSecureIME(String packageName) {
        return HwInputMethodManagerService.SECURE_IME_PACKAGENAME.equals(packageName);
    }

    /* access modifiers changed from: protected */
    public boolean shouldBuildInputMethodList(String packageName) {
        return HwInputMethodManagerService.SECURE_IME_PACKAGENAME.equals(packageName);
    }

    private boolean setClientActiveIfShould() {
        if (!this.mShouldSetActive || !this.mIsInteractive) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (this.mCurClient == null || this.mCurClient.client == null) {
                return false;
            }
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO((int) MSG_SET_ACTIVE, 1, this.mCurClient));
            this.mShouldSetActive = false;
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void switchUserExtra(int userId) {
        SecureSettingsObserver secureSettingsObserver = this.mSecureSettingsObserver;
        if (secureSettingsObserver != null) {
            secureSettingsObserver.registerContentObserverInner(userId);
        }
        if (!HwInputMethodUtils.isSecureIMEEnable(this.mContext, this.mSettings.getCurrentUserId()) && !HwInputMethodUtils.isNeedSecIMEInSpecialScenes(this.mContext)) {
            hideCurrentInputLocked(0, null);
            this.mCurMethodId = null;
            unbindCurrentMethodLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public static class SessionState {
        InputChannel channel;
        final ClientState client;
        final IInputMethod method;
        IInputMethodSession session;

        public String toString() {
            return "SessionState{uid " + this.client.uid + " pid " + this.client.pid + " method " + Integer.toHexString(System.identityHashCode(this.method)) + " session " + Integer.toHexString(System.identityHashCode(this.session)) + " channel " + this.channel + "}";
        }

        SessionState(ClientState clientState, IInputMethod inputMethod, IInputMethodSession methodSession, InputChannel inputChannel) {
            this.client = clientState;
            this.method = inputMethod;
            this.session = methodSession;
            this.channel = inputChannel;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ClientState {
        final InputBinding binding = new InputBinding(null, this.inputContext.asBinder(), this.uid, this.pid);
        final IInputMethodClient client;
        final ClientDeathRecipient clientDeathRecipient;
        SessionState curSession;
        final IInputContext inputContext;
        final int pid;
        final int selfReportedDisplayId;
        boolean sessionRequested;
        boolean shouldPreRenderIme;
        final int uid;

        public String toString() {
            return "ClientState{" + Integer.toHexString(System.identityHashCode(this)) + " uid=" + this.uid + " pid=" + this.pid + " displayId=" + this.selfReportedDisplayId + "}";
        }

        ClientState(IInputMethodClient iInputMethodClient, IInputContext iInputContext, int iUid, int iPid, int iSelfReportedDisplayId, ClientDeathRecipient cClientDeathRecipient) {
            this.client = iInputMethodClient;
            this.inputContext = iInputContext;
            this.uid = iUid;
            this.pid = iPid;
            this.selfReportedDisplayId = iSelfReportedDisplayId;
            this.clientDeathRecipient = cClientDeathRecipient;
        }
    }

    /* access modifiers changed from: private */
    public static class StartInputInfo {
        private static final AtomicInteger S_SEQUENCE_NUMBER = new AtomicInteger(0);
        final int mClientBindSequenceNumber;
        final EditorInfo mEditorInfo;
        final String mImeId;
        final IBinder mImeToken;
        final boolean mRestarting;
        final int mSequenceNumber = S_SEQUENCE_NUMBER.getAndIncrement();
        final int mStartInputReason;
        final IBinder mTargetWindow;
        final int mTargetWindowSoftInputMode;
        final long mTimestamp = SystemClock.uptimeMillis();
        final long mWallTime = System.currentTimeMillis();

        StartInputInfo(IBinder imeToken, String imeId, int startInputReason, boolean restarting, IBinder targetWindow, EditorInfo editorInfo, int targetWindowSoftInputMode, int clientBindSequenceNumber) {
            this.mImeToken = imeToken;
            this.mImeId = imeId;
            this.mStartInputReason = startInputReason;
            this.mRestarting = restarting;
            this.mTargetWindow = targetWindow;
            this.mEditorInfo = editorInfo;
            this.mTargetWindowSoftInputMode = targetWindowSoftInputMode;
            this.mClientBindSequenceNumber = clientBindSequenceNumber;
        }
    }

    /* access modifiers changed from: private */
    public class SecureSettingsObserver extends ContentObserver {
        boolean mRegistered = false;
        int mUserId;

        public SecureSettingsObserver() {
            super(new Handler());
        }

        public void registerContentObserverInner(int userId) {
            Slog.d(HwSecureInputMethodManagerService.TAG, "SecureSettingsObserver mRegistered=" + this.mRegistered + " new user=" + userId + " current user=" + this.mUserId);
            if (!this.mRegistered || this.mUserId != userId) {
                ContentResolver resolver = HwSecureInputMethodManagerService.this.mContext.getContentResolver();
                if (this.mRegistered) {
                    resolver.unregisterContentObserver(this);
                    this.mRegistered = false;
                }
                this.mUserId = userId;
                resolver.registerContentObserver(Settings.Secure.getUriFor(HwInputMethodUtils.SETTINGS_SECURE_KEYBOARD_CONTROL), false, this, userId);
                this.mRegistered = true;
            }
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                Slog.i(HwSecureInputMethodManagerService.TAG, "SecureSettingsObserver onChange, uri = " + uri.toString());
                if (Settings.Secure.getUriFor(HwInputMethodUtils.SETTINGS_SECURE_KEYBOARD_CONTROL).equals(uri) && !HwInputMethodUtils.isSecureIMEEnable(HwSecureInputMethodManagerService.this.mContext, HwSecureInputMethodManagerService.this.mSettings.getCurrentUserId()) && !HwInputMethodUtils.isNeedSecIMEInSpecialScenes(HwSecureInputMethodManagerService.this.mContext)) {
                    synchronized (HwSecureInputMethodManagerService.this.mMethodMap) {
                        HwSecureInputMethodManagerService.this.hideCurrentInputLocked(0, null);
                        HwSecureInputMethodManagerService.this.mCurMethodId = null;
                        HwSecureInputMethodManagerService.this.unbindCurrentMethodLocked();
                    }
                }
            }
        }
    }

    class ImmsBroadcastReceiver extends BroadcastReceiver {
        ImmsBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_REMOVED".equals(action)) {
                HwSecureInputMethodManagerService.this.updateCurrentProfileIds();
                return;
            }
            Slog.w(HwSecureInputMethodManagerService.TAG, "Unexpected intent " + intent);
        }
    }

    class MyPackageMonitor extends PackageMonitor {
        MyPackageMonitor() {
        }

        private boolean isChangingPackagesOfCurrentUser() {
            return getChangingUserId() == HwSecureInputMethodManagerService.this.mSettings.getCurrentUserId();
        }

        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            return false;
        }

        public void onSomePackagesChanged() {
            if (isChangingPackagesOfCurrentUser()) {
                synchronized (HwSecureInputMethodManagerService.this.mMethodMap) {
                    HwSecureInputMethodManagerService.this.buildInputMethodListLocked(false);
                }
            }
        }

        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            onSomePackagesChanged();
            if (components != null) {
                for (String name : components) {
                    if (packageName.equals(name)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static final class MethodCallback extends IInputSessionCallback.Stub {
        private final InputChannel mChannel;
        private final IInputMethod mMethod;
        private final HwSecureInputMethodManagerService mParentIMMS;

        MethodCallback(HwSecureInputMethodManagerService imms, IInputMethod method, InputChannel channel) {
            this.mParentIMMS = imms;
            this.mMethod = method;
            this.mChannel = channel;
        }

        public void sessionCreated(IInputMethodSession session) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mParentIMMS.onSessionCreated(this.mMethod, session, this.mChannel);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public static final class MyLifecycle extends SystemService {
        private HwSecureInputMethodManagerService mService;

        public MyLifecycle(Context context) {
            super(context);
            this.mService = new HwSecureInputMethodManagerService(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.inputmethod.HwSecureInputMethodManagerService$MyLifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.inputmethod.HwSecureInputMethodManagerService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        public void onStart() {
            publishBinderService(HwInputMethodManagerService.SECURITY_INPUT_SERVICE_NAME, this.mService);
        }

        public void onSwitchUser(int userHandle) {
            this.mService.onSwitchUser(userHandle);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                if (LocalServices.getService(HwSecureInputMethodManagerInternal.class) == null) {
                    LocalServices.addService(HwSecureInputMethodManagerInternal.class, new LocalSecureServiceImpl(this.mService));
                }
                this.mService.systemRunning(ServiceManager.getService("statusbar"));
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.mHandler.sendMessage(this.mService.mHandler.obtainMessage(5000, userHandle, 0));
        }
    }

    /* access modifiers changed from: package-private */
    public void onUnlockUser(int userId) {
        synchronized (this.mMethodMap) {
            int currentUserId = this.mSettings.getCurrentUserId();
            if (userId == currentUserId) {
                this.mSettings.switchCurrentUser(currentUserId, !this.mSystemReady);
                buildInputMethodListLocked(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onSwitchUser(int userId) {
        synchronized (this.mMethodMap) {
            switchUserLocked(userId);
        }
    }

    public HwSecureInputMethodManagerService(Context context) {
        this.mContext = context;
        this.mRes = context.getResources();
        this.mHandler = new Handler(this);
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mImeDisplayValidator = new ImeDisplayValidator((DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class)) {
            /* class com.android.server.inputmethod.$$Lambda$HwSecureInputMethodManagerService$Qh38sYFZ2W0IP2FzH4WejCv3iU */
            private final /* synthetic */ DisplayManagerInternal f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.server.inputmethod.HwSecureInputMethodManagerService.ImeDisplayValidator
            public final boolean displayCanShowIme(int i) {
                return HwSecureInputMethodManagerService.lambda$new$0(this.f$0, i);
            }
        };
        this.mCaller = new HandlerCaller(context, (Looper) null, new HandlerCaller.Callback() {
            /* class com.android.server.inputmethod.HwSecureInputMethodManagerService.AnonymousClass2 */

            public void executeMessage(Message msg) {
                HwSecureInputMethodManagerService.this.handleMessage(msg);
            }
        }, true);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mSlotIme = this.mContext.getString(17041310);
        new Bundle().putBoolean("android.allowDuringSetup", true);
        IntentFilter broadcastFilter = new IntentFilter();
        broadcastFilter.addAction("android.intent.action.USER_ADDED");
        broadcastFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(new ImmsBroadcastReceiver(), broadcastFilter);
        int userId = 0;
        try {
            userId = ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            Slog.w(TAG, "Couldn't get current user ID; guessing it's 0", e);
        }
        this.mMyPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
        this.mSettings = new InputMethodUtils.InputMethodSettings(this.mRes, context.getContentResolver(), this.mMethodMap, userId, !this.mSystemReady);
        updateCurrentProfileIds();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.inputmethod.HwSecureInputMethodManagerService.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                synchronized (HwSecureInputMethodManagerService.this.mMethodMap) {
                    HwSecureInputMethodManagerService.this.resetStateIfCurrentLocaleChangedLocked();
                }
            }
        }, filter);
    }

    static /* synthetic */ boolean lambda$new$0(DisplayManagerInternal displayManagerInternal, int displayId) {
        DisplayInfo displayInfo = displayManagerInternal.getDisplayInfo(displayId);
        return (displayInfo == null || (displayInfo.flags & 64) == 0) ? false : true;
    }

    private void resetAllInternalStateLocked(boolean updateOnlyWhenLocaleChanged, boolean resetDefaultEnabledIme) {
        if (this.mSystemReady) {
            LocaleList newLocales = this.mRes.getConfiguration().getLocales();
            if (!updateOnlyWhenLocaleChanged || (newLocales != null && !newLocales.equals(this.mLastSystemLocales))) {
                if (!updateOnlyWhenLocaleChanged) {
                    hideCurrentInputLocked(0, null);
                    resetCurrentMethodAndClient(6);
                }
                buildInputMethodListLocked(resetDefaultEnabledIme);
                this.mLastSystemLocales = newLocales;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetStateIfCurrentLocaleChangedLocked() {
        resetAllInternalStateLocked(true, true);
    }

    private void switchUserLocked(int newUserId) {
        this.mSettings.switchCurrentUser(newUserId, !this.mSystemReady || !this.mUserManager.isUserUnlockingOrUnlocked(newUserId));
        updateCurrentProfileIds();
        resetAllInternalStateLocked(false, false);
        switchUserExtra(newUserId);
    }

    /* access modifiers changed from: package-private */
    public void updateCurrentProfileIds() {
        InputMethodUtils.InputMethodSettings inputMethodSettings = this.mSettings;
        inputMethodSettings.setCurrentProfileIds(this.mUserManager.getProfileIdsWithDisabled(inputMethodSettings.getCurrentUserId()));
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return HwSecureInputMethodManagerService.super.onTransact(code, data, reply, flags);
        } catch (RuntimeException exception) {
            if (!(exception instanceof SecurityException)) {
                Slog.wtf(TAG, "Input Method Manager Crash", exception);
            }
            throw exception;
        }
    }

    public void systemRunning(StatusBarManagerService statusBar) {
        this.mSecureSettingsObserver = new SecureSettingsObserver();
        this.mSecureSettingsObserver.registerContentObserverInner(this.mSettings.getCurrentUserId());
        synchronized (this.mMethodMap) {
            if (!this.mSystemReady) {
                this.mSystemReady = true;
                int currentUserId = this.mSettings.getCurrentUserId();
                this.mSettings.switchCurrentUser(currentUserId, !this.mUserManager.isUserUnlockingOrUnlocked(currentUserId));
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
                this.mStatusBar = statusBar;
                if (this.mStatusBar != null) {
                    this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                }
                updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                buildInputMethodListLocked(true);
                this.mLastSystemLocales = this.mRes.getConfiguration().getLocales();
            }
        }
    }

    private boolean calledFromValidUserLocked() {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (uid == 1000 || this.mSettings.isCurrentProfile(userId) || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            return true;
        }
        Slog.w(TAG, "--- IPC called from background users. Ignore. callers=" + Debug.getCallers(10));
        return false;
    }

    private boolean calledWithValidTokenLocked(IBinder token) {
        if (token == null || this.mCurToken != token) {
            return false;
        }
        return true;
    }

    private boolean bindCurrentInputMethodService(Intent service, ServiceConnection conn, int flags) {
        if (service != null && conn != null) {
            return this.mContext.bindServiceAsUser(service, conn, flags, new UserHandle(this.mSettings.getCurrentUserId()));
        }
        Slog.e(TAG, "--- bind failed: service = " + service + ", conn = " + conn);
        return false;
    }

    public List<InputMethodInfo> getInputMethodList(int userId) {
        synchronized (this.mMethodMap) {
            if (!calledFromValidUserLocked()) {
                return Collections.emptyList();
            }
            return new ArrayList(this.mMethodList);
        }
    }

    public List<InputMethodInfo> getVrInputMethodList() {
        return getInputMethodList(true);
    }

    private List<InputMethodInfo> getInputMethodList(boolean isVrOnly) {
        ArrayList<InputMethodInfo> methodList;
        if (!calledFromValidUserLocked()) {
            return Collections.emptyList();
        }
        synchronized (this.mMethodMap) {
            methodList = new ArrayList<>();
            Iterator<InputMethodInfo> it = this.mMethodList.iterator();
            while (it.hasNext()) {
                InputMethodInfo info = it.next();
                if (info.isVrOnly() == isVrOnly) {
                    methodList.add(info);
                }
            }
        }
        return methodList;
    }

    public List<InputMethodInfo> getEnabledInputMethodList(int userid) {
        return Collections.emptyList();
    }

    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId, boolean allowsImplicitlySelectedSubtypes) {
        return Collections.emptyList();
    }

    /* access modifiers changed from: private */
    public static final class ClientDeathRecipient implements IBinder.DeathRecipient {
        private final IInputMethodClient mClient;
        private final HwSecureInputMethodManagerService mImms;

        ClientDeathRecipient(HwSecureInputMethodManagerService imms, IInputMethodClient client) {
            this.mImms = imms;
            this.mClient = client;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            this.mImms.removeClient(this.mClient);
        }
    }

    public void addClient(IInputMethodClient client, IInputContext inputContext, int selfReportedDisplayId) {
        RemoteException e;
        int callerUid = Binder.getCallingUid();
        int callerPid = Binder.getCallingPid();
        synchronized (this.mMethodMap) {
            try {
                for (ClientState state : this.mClients.values()) {
                    if (state.uid == callerUid && state.pid == callerPid) {
                        if (state.selfReportedDisplayId == selfReportedDisplayId) {
                            throw new SecurityException("uid=" + callerUid + "/pid=" + callerPid + "/displayId=" + selfReportedDisplayId + " is already registered.");
                        }
                    }
                }
                try {
                    ClientDeathRecipient deathRecipient = new ClientDeathRecipient(this, client);
                    try {
                        client.asBinder().linkToDeath(deathRecipient, 0);
                        this.mClients.put(client.asBinder(), new ClientState(client, inputContext, callerUid, callerPid, selfReportedDisplayId, deathRecipient));
                    } catch (RemoteException e2) {
                        throw new IllegalStateException(e2);
                    }
                } catch (Throwable th) {
                    e = th;
                    throw e;
                }
            } catch (Throwable th2) {
                e = th2;
                throw e;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeClient(IInputMethodClient client) {
        synchronized (this.mMethodMap) {
            ClientState cs = this.mClients.remove(client.asBinder());
            if (cs != null) {
                client.asBinder().unlinkToDeath(cs.clientDeathRecipient, 0);
                clearClientSessionLocked(cs);
                if (this.mCurClient == cs) {
                    this.mCurClient = null;
                }
                if (this.mCurFocusedWindowClient == cs) {
                    this.mCurFocusedWindowClient = null;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void executeOrSendMessage(IInterface target, Message msg) {
        if (target != null) {
            if (target.asBinder() instanceof Binder) {
                this.mCaller.sendMessage(msg);
                return;
            }
            handleMessage(msg);
            msg.recycle();
        }
    }

    /* access modifiers changed from: package-private */
    public void unbindCurrentClientLocked(int unbindClientReason) {
        if (this.mCurClient != null) {
            if (this.mBoundToMethod) {
                this.mBoundToMethod = false;
                IInputMethod iInputMethod = this.mCurMethod;
                if (iInputMethod != null) {
                    executeOrSendMessage(iInputMethod, this.mCaller.obtainMessageO(1000, iInputMethod));
                }
            }
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO((int) MSG_SET_ACTIVE, 0, this.mCurClient));
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(3000, this.mCurSeq, unbindClientReason, this.mCurClient.client));
            this.mCurClient.sessionRequested = false;
            this.mCurClient = null;
        }
    }

    private int getImeShowFlags() {
        if (this.mShowForced) {
            return 0 | 3;
        }
        if (this.mShowExplicitlyRequested) {
            return 0 | 1;
        }
        Slog.i(TAG, "getImeShowFlags: other things.");
        return 0;
    }

    private int getAppShowFlags() {
        if (this.mShowForced) {
            return 0 | 2;
        }
        if (!this.mShowExplicitlyRequested) {
            return 0 | 1;
        }
        Slog.i(TAG, "getAppShowFlags: other things.");
        return 0;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r22v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: package-private */
    public InputBindResult attachNewInputLocked(int startInputReason, boolean initial) {
        if (!this.mBoundToMethod) {
            IInputMethod iInputMethod = this.mCurMethod;
            executeOrSendMessage(iInputMethod, this.mCaller.obtainMessageOO(1010, iInputMethod, this.mCurClient.binding));
            this.mBoundToMethod = true;
        }
        SessionState session = this.mCurClient.curSession;
        Binder startInputToken = new Binder();
        new StartInputInfo(this.mCurToken, this.mCurId, startInputReason, !initial ? 1 : 0, this.mCurFocusedWindow, this.mCurAttribute, this.mCurFocusedWindowSoftInputMode, this.mCurSeq);
        this.mImeTargetWindowMap.put(startInputToken, this.mCurFocusedWindow);
        if (session != null) {
            executeOrSendMessage(session.method, this.mCaller.obtainMessageIIOOOO(2000, this.mCurInputContextMissingMethods, !initial, startInputToken, session, this.mCurInputContext, this.mCurAttribute));
        }
        InputChannel inputChannel = null;
        if (this.mShowRequested) {
            if (DEBUG_FLOW) {
                Slog.v(TAG, "Attach new input asks to show input");
            }
            showCurrentInputLocked(getAppShowFlags(), null);
        }
        if (session == null) {
            return null;
        }
        IInputMethodSession iInputMethodSession = session.session;
        if (session.channel != null) {
            inputChannel = session.channel.dup();
        }
        return new InputBindResult(0, iInputMethodSession, inputChannel, this.mCurId, this.mCurSeq, (Matrix) null);
    }

    /* access modifiers changed from: package-private */
    public InputBindResult startInputLocked(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        ClientState cs = this.mClients.get(client.asBinder());
        if (cs == null) {
            throw new IllegalArgumentException("unknown client " + client.asBinder());
        } else if (attribute == null) {
            Slog.w(TAG, "Ignoring startInput with null EditorInfo. uid=" + cs.uid + " pid=" + cs.pid);
            return null;
        } else if (this.mWindowManagerInternal.isInputMethodClientFocus(cs.uid, cs.pid, cs.selfReportedDisplayId)) {
            return startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags, startInputReason);
        } else {
            Slog.w(TAG, "Ignoring showSoftInput of " + client);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public InputBindResult startInputUncheckedLocked(ClientState cs, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags, int startInputReason) {
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        if (!InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, cs.uid, attribute.packageName)) {
            Slog.e(TAG, "Rejecting this client as it reported an invalid package name. uid=" + cs.uid + " package=" + attribute.packageName);
            return this.mNoBinding;
        }
        if (this.mCurClient != cs) {
            this.mCurClientInKeyguard = isKeyguardLocked();
            unbindCurrentClientLocked(1);
            if (this.mIsInteractive) {
                executeOrSendMessage(cs.client, this.mCaller.obtainMessageIO((int) MSG_SET_ACTIVE, this.mIsInteractive ? 1 : 0, cs));
            }
        }
        this.mCurSeq++;
        if (this.mCurSeq <= 0) {
            this.mCurSeq = 1;
        }
        this.mCurClient = cs;
        this.mCurInputContext = inputContext;
        this.mCurInputContextMissingMethods = missingMethods;
        this.mCurAttribute = attribute;
        if ((controlFlags & 65536) != 0) {
            this.mShowRequested = true;
        }
        int displayIdToShowIme = computeImeDisplayIdForTarget(cs.selfReportedDisplayId, this.mIsVrImeStarted, this.mImeDisplayValidator);
        String str = this.mCurId;
        if (str != null && str.equals(this.mCurMethodId) && displayIdToShowIme == this.mCurTokenDisplayId) {
            boolean z = false;
            if (cs.curSession != null) {
                if ((controlFlags & 8) != 0) {
                    z = true;
                }
                return attachNewInputLocked(startInputReason, z);
            } else if (this.mHaveConnection) {
                if (this.mCurMethod != null) {
                    requestClientSessionLocked(cs);
                    return new InputBindResult(1, (IInputMethodSession) null, (InputChannel) null, this.mCurId, this.mCurSeq, (Matrix) null);
                } else if (SystemClock.uptimeMillis() < this.mLastBindTime + 3000) {
                    return new InputBindResult(2, (IInputMethodSession) null, (InputChannel) null, this.mCurId, this.mCurSeq, (Matrix) null);
                } else {
                    EventLog.writeEvent(32000, this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), 0);
                }
            }
        }
        try {
            return startInputInnerLocked(displayIdToShowIme);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Unexpected exception", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public InputBindResult startInputInnerLocked(int displayIdToShowIme) {
        String str = this.mCurMethodId;
        if (str == null || !this.mMethodMap.containsKey(str)) {
            return this.mNoBinding;
        }
        if (!this.mSystemReady) {
            return new InputBindResult(7, (IInputMethodSession) null, (InputChannel) null, this.mCurMethodId, this.mCurSeq, (Matrix) null);
        }
        InputMethodInfo info = this.mMethodMap.get(this.mCurMethodId);
        if (info == null) {
            Slog.w(TAG, "info == null id: " + this.mCurMethodId);
            return this.mNoBinding;
        }
        unbindCurrentMethodLocked();
        this.mCurIntent = new Intent("android.view.InputMethod");
        this.mCurIntent.setComponent(info.getComponent());
        this.mCurIntent.putExtra("android.intent.extra.client_label", 17040297);
        this.mCurIntent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.INPUT_METHOD_SETTINGS"), 0));
        if (bindCurrentInputMethodService(this.mCurIntent, this, 1610612741)) {
            this.mLastBindTime = SystemClock.uptimeMillis();
            this.mHaveConnection = true;
            this.mCurId = info.getId();
            this.mCurToken = new Binder();
            this.mCurTokenDisplayId = displayIdToShowIme;
            try {
                Slog.v(TAG, "Adding window token: " + this.mCurToken);
                this.mIWindowManager.addWindowToken(this.mCurToken, (int) HwArbitrationDEFS.MSG_ARBITRATION_REQUEST_MPLINK, this.mCurTokenDisplayId);
            } catch (RemoteException e) {
                Slog.i(TAG, "add window token error.");
            }
            return new InputBindResult(2, (IInputMethodSession) null, (InputChannel) null, this.mCurId, this.mCurSeq, (Matrix) null);
        }
        this.mCurIntent = null;
        Slog.w(TAG, "Failure connecting to input method service: " + this.mCurIntent);
        return null;
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mMethodMap) {
            if (this.mCurIntent != null && name.equals(this.mCurIntent.getComponent())) {
                this.mCurMethod = IInputMethod.Stub.asInterface(service);
                if (this.mCurToken == null) {
                    Slog.w(TAG, "Service connected without a token!");
                    unbindCurrentMethodLocked();
                    return;
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Initiating attach with token: " + this.mCurToken);
                }
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageIOO((int) MSG_INITIALIZE_IME, this.mCurTokenDisplayId, this.mCurMethod, this.mCurToken));
                if (this.mCurClient != null) {
                    clearClientSessionLocked(this.mCurClient);
                    requestClientSessionLocked(this.mCurClient);
                }
            }
        }
    }

    static int computeImeDisplayIdForTarget(int displayId, boolean isVrImeStarted, ImeDisplayValidator checker) {
        if (isVrImeStarted || displayId == 0 || displayId == -1) {
            return 0;
        }
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId)) {
            if (checker.displayCanShowIme(displayId)) {
                return displayId;
            }
            return 0;
        } else if (((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)).isHardKeyboardAvailable() || HwPCUtils.mTouchDeviceID != -1 || HwPCUtils.enabledInPad()) {
            return displayId;
        } else {
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void onSessionCreated(IInputMethod method, IInputMethodSession session, InputChannel channel) {
        synchronized (this.mMethodMap) {
            if (this.mCurMethod == null || method == null || this.mCurMethod.asBinder() != method.asBinder() || this.mCurClient == null) {
                channel.dispose();
                return;
            }
            if (DEBUG_FLOW) {
                Slog.v(TAG, "IME session created");
            }
            clearClientSessionLocked(this.mCurClient);
            this.mCurClient.curSession = new SessionState(this.mCurClient, method, session, channel);
            InputBindResult res = attachNewInputLocked(9, true);
            if (res != null) {
                if (res.method != null) {
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageOO((int) MSG_BIND_CLIENT, this.mCurClient.client, res));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unbindCurrentMethodLocked() {
        unbindVisibleConnection();
        if (this.mHaveConnection) {
            this.mContext.unbindService(this);
            this.mHaveConnection = false;
        }
        IBinder iBinder = this.mCurToken;
        if (iBinder != null) {
            try {
                this.mIWindowManager.removeWindowToken(iBinder, this.mCurTokenDisplayId);
            } catch (RemoteException e) {
                Slog.i(TAG, "remove window token error.");
            }
            this.mCurToken = null;
            this.mCurTokenDisplayId = -1;
        }
        this.mCurId = null;
        clearCurMethodLocked();
    }

    /* access modifiers changed from: package-private */
    public void resetCurrentMethodAndClient(int unbindClientReason) {
        this.mCurMethodId = null;
        unbindCurrentMethodLocked();
        unbindCurrentClientLocked(unbindClientReason);
    }

    /* access modifiers changed from: package-private */
    public void requestClientSessionLocked(ClientState cs) {
        if (!cs.sessionRequested) {
            InputChannel[] channels = InputChannel.openInputChannelPair(cs.toString());
            cs.sessionRequested = true;
            IInputMethod iInputMethod = this.mCurMethod;
            executeOrSendMessage(iInputMethod, this.mCaller.obtainMessageOOO((int) MSG_CREATE_SESSION, iInputMethod, channels[1], new MethodCallback(this, iInputMethod, channels[0])));
        }
    }

    /* access modifiers changed from: package-private */
    public void clearClientSessionLocked(ClientState cs) {
        finishSessionLocked(cs.curSession);
        cs.curSession = null;
        cs.sessionRequested = false;
    }

    private void finishSessionLocked(SessionState sessionState) {
        if (sessionState != null) {
            if (sessionState.session != null) {
                try {
                    sessionState.session.finishSession();
                } catch (RemoteException e) {
                    Slog.w(TAG, "Session failed to close due to remote exception", e);
                    updateSystemUiLocked(this.mCurToken, 0, this.mBackDisposition);
                }
                sessionState.session = null;
            }
            if (sessionState.channel != null) {
                sessionState.channel.dispose();
                sessionState.channel = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearCurMethodLocked() {
        if (this.mCurMethod != null) {
            int numClients = this.mClients.size();
            for (int i = 0; i < numClients; i++) {
                clearClientSessionLocked(this.mClients.valueAt(i));
            }
            finishSessionLocked(this.mEnabledSession);
            this.mEnabledSession = null;
            this.mCurMethod = null;
        }
        StatusBarManagerService statusBarManagerService = this.mStatusBar;
        if (statusBarManagerService != null) {
            statusBarManagerService.setIconVisibility(this.mSlotIme, false);
        }
        this.mInFullscreenMode = false;
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName name) {
        synchronized (this.mMethodMap) {
            if (!(this.mCurMethod == null || this.mCurIntent == null || !name.equals(this.mCurIntent.getComponent()))) {
                clearCurMethodLocked();
                this.mLastBindTime = SystemClock.uptimeMillis();
                this.mShowRequested = this.mInputShown;
                this.mInputShown = false;
                if (this.mCurClient != null) {
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(3000, 3, this.mCurSeq, this.mCurClient.client));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateStatusIcon(IBinder token, String packageName, int iconId) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (!calledWithValidTokenLocked(token)) {
                    int uid = Binder.getCallingUid();
                    Slog.e(TAG, "Ignoring updateStatusIcon due to an invalid token. uid:" + uid + " token:" + token);
                    return;
                }
                if (iconId == 0) {
                    if (this.mStatusBar != null) {
                        this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                    }
                } else if (packageName != null) {
                    CharSequence contentDescription = null;
                    try {
                        contentDescription = this.mContext.getPackageManager().getApplicationLabel(this.mIPackageManager.getApplicationInfo(packageName, 0, this.mSettings.getCurrentUserId()));
                    } catch (RemoteException e) {
                        Slog.i(TAG, "get application label error.");
                    }
                    if (this.mStatusBar != null) {
                        this.mStatusBar.setIcon(this.mSlotIme, packageName, iconId, 0, contentDescription != null ? contentDescription.toString() : null);
                        this.mStatusBar.setIconVisibility(this.mSlotIme, true);
                    }
                } else {
                    Slog.i(TAG, "packageName is null.");
                }
                Binder.restoreCallingIdentity(ident);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean shouldShowImeSwitcherLocked(int visibility) {
        return false;
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguardManager = this.mKeyguardManager;
        return keyguardManager != null && keyguardManager.isKeyguardLocked();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setImeWindowStatus(IBinder token, int vis, int backDisposition) {
        boolean dismissImeOnBackKeyPressed;
        synchronized (this.mMethodMap) {
            if (!calledWithValidTokenLocked(token)) {
                Slog.e(TAG, "Ignoring setImeWindowStatus due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
                return;
            }
            this.mImeWindowVis = vis;
            this.mBackDisposition = backDisposition;
            updateSystemUiLocked(token, vis, backDisposition);
        }
        boolean z = false;
        if (backDisposition == 1) {
            dismissImeOnBackKeyPressed = false;
        } else if (backDisposition != 2) {
            dismissImeOnBackKeyPressed = (vis & 2) != 0;
        } else {
            dismissImeOnBackKeyPressed = true;
        }
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if ((vis & 2) != 0) {
            z = true;
        }
        windowManagerInternal.updateInputMethodWindowStatus(token, z, dismissImeOnBackKeyPressed);
    }

    private void updateSystemUi(IBinder token, int vis, int backDisposition) {
        synchronized (this.mMethodMap) {
            updateSystemUiLocked(token, vis, backDisposition);
        }
    }

    private void updateSystemUiLocked(IBinder token, int vis, int backDisposition) {
        if (!calledWithValidTokenLocked(token)) {
            int uid = Binder.getCallingUid();
            Slog.e(TAG, "Ignoring updateSystemUiLocked due to an invalid token. uid:" + uid + " token:" + token);
            return;
        }
        long ident = Binder.clearCallingIdentity();
        if (vis != 0) {
            try {
                if (isKeyguardLocked() && !this.mCurClientInKeyguard) {
                    vis = 0;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }
        boolean needsToShowImeSwitcher = shouldShowImeSwitcherLocked(vis);
        if (this.mStatusBar != null) {
            this.mStatusBar.setImeWindowStatus(this.mCurTokenDisplayId, token, vis, backDisposition, needsToShowImeSwitcher);
        }
        Binder.restoreCallingIdentity(ident);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportFullscreenMode(IBinder token, boolean fullscreen) {
        synchronized (this.mMethodMap) {
            if (calledWithValidTokenLocked(token)) {
                if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                    this.mInFullscreenMode = fullscreen;
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO((int) MSG_REPORT_FULLSCREEN_MODE, fullscreen ? 1 : 0, this.mCurClient));
                }
            }
        }
    }

    public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        int uid = Binder.getCallingUid();
        synchronized (this.mMethodMap) {
            if (!calledFromValidUserLocked()) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mCurClient == null || client == null || this.mCurClient.client.asBinder() != client.asBinder()) {
                    ClientState cs = this.mClients.get(client.asBinder());
                    if (cs == null) {
                        throw new IllegalArgumentException("unknown client " + client.asBinder());
                    } else if (!this.mWindowManagerInternal.isInputMethodClientFocus(cs.uid, cs.pid, cs.selfReportedDisplayId)) {
                        Slog.w(TAG, "Ignoring showSoftInput of uid " + uid + ": " + client);
                        return false;
                    }
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Client requesting input be shown, requestedUid=" + uid);
                }
                boolean showCurrentInputLocked = showCurrentInputLocked(flags, resultReceiver);
                Binder.restoreCallingIdentity(ident);
                return showCurrentInputLocked;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        this.mShowRequested = true;
        if ((flags & 2) != 0) {
            this.mShowExplicitlyRequested = true;
            this.mShowForced = true;
        } else if ((flags & 1) == 0) {
            this.mShowExplicitlyRequested = true;
        } else {
            Slog.i(TAG, "others");
        }
        if (!this.mSystemReady) {
            return false;
        }
        if (this.mWindowManagerInternal.isPcFreeFormWinClient(this.mCurFocusedWindow)) {
            Slog.v(TAG, "needn't show IME when pc multi-window mode");
            return false;
        } else if (this.mCurMethod != null) {
            if (DEBUG_FLOW) {
                Slog.d(TAG, "showCurrentInputLocked: mCurToken=" + this.mCurToken);
            }
            this.mWindowManagerInternal.setSecureImeHolder(this.mCurFocusedWindow);
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageIOO(1020, getImeShowFlags(), this.mCurMethod, resultReceiver));
            this.mInputShown = true;
            if (this.mHaveConnection && !this.mVisibleBound) {
                this.mVisibleBound = bindCurrentInputMethodService(this.mCurIntent, this.mVisibleConnection, 201326593);
            }
            return true;
        } else if (!this.mHaveConnection || SystemClock.uptimeMillis() < this.mLastBindTime + 3000) {
            return false;
        } else {
            EventLog.writeEvent(32000, this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), 1);
            Slog.w(TAG, "Force disconnect/connect to the IME in showCurrentInputLocked()");
            this.mContext.unbindService(this);
            bindCurrentInputMethodService(this.mCurIntent, this, 1073741825);
            return false;
        }
    }

    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        int uid = Binder.getCallingUid();
        synchronized (this.mMethodMap) {
            if (!calledFromValidUserLocked()) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mCurClient == null || client == null || this.mCurClient.client.asBinder() != client.asBinder()) {
                    ClientState cs = this.mClients.get(client.asBinder());
                    if (cs == null) {
                        throw new IllegalArgumentException("unknown client " + client.asBinder());
                    } else if (!this.mWindowManagerInternal.isInputMethodClientFocus(cs.uid, cs.pid, cs.selfReportedDisplayId)) {
                        return false;
                    }
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Client requesting input be hidden, pid=" + uid);
                }
                boolean hideCurrentInputLocked = hideCurrentInputLocked(flags, resultReceiver);
                Binder.restoreCallingIdentity(ident);
                return hideCurrentInputLocked;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        boolean res;
        if ((flags & 1) != 0 && (this.mShowExplicitlyRequested || this.mShowForced)) {
            return false;
        }
        if (this.mShowForced && (flags & 2) != 0) {
            return false;
        }
        boolean shouldHideSoftInput = true;
        if (this.mCurMethod == null || (!this.mInputShown && (this.mImeWindowVis & 1) == 0)) {
            shouldHideSoftInput = false;
        }
        if (shouldHideSoftInput) {
            IInputMethod iInputMethod = this.mCurMethod;
            executeOrSendMessage(iInputMethod, this.mCaller.obtainMessageOO(1030, iInputMethod, resultReceiver));
            res = true;
        } else {
            res = false;
        }
        if (!this.mInputShown && !this.mShowRequested) {
            res = false;
        }
        unbindVisibleConnection();
        this.mInputShown = false;
        this.mShowRequested = false;
        this.mShowExplicitlyRequested = false;
        this.mShowForced = false;
        return res;
    }

    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int startInputFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion) {
        if (!calledFromValidUserLocked()) {
            return null;
        }
        synchronized (this.mMethodMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mCurMethodId == null) {
                    if (!HwInputMethodUtils.isSecureIMEEnable(this.mContext, this.mSettings.getCurrentUserId())) {
                        if (!HwInputMethodUtils.isNeedSecIMEInSpecialScenes(this.mContext)) {
                            Slog.d(TAG, "startInputOrWindowGainedFocus, secure ime is disable");
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        }
                    }
                    this.mCurMethodId = "com.huawei.secime/.SoftKeyboard";
                    Slog.d(TAG, "startInputOrWindowGainedFocus, mCurMethodId is null, reset");
                    this.mShouldSetActive = true;
                }
                if (windowToken == null || startInputReason != 10000) {
                    this.mUnbindCounter = 0;
                    Binder.restoreCallingIdentity(ident);
                } else {
                    if (this.mCurFocusedWindow != windowToken && this.mHaveConnection && this.mUnbindCounter > 1 && !HwInputMethodUtils.isNeedSecIMEInSpecialScenes(this.mContext)) {
                        Slog.d(TAG, "unbind secime");
                        unbindCurrentMethodLocked();
                        unbindCurrentClientLocked(10000);
                    }
                    this.mUnbindCounter++;
                    this.mCurFocusedWindow = windowToken;
                    return null;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        if (startInputReason == 8) {
            Slog.i(TAG, "startInputOrWindowGainedFocus, client deactive by imms, set active again to enable secure inputmethod");
            this.mShouldSetActive = true;
        }
        InputBindResult ret = secureImeStartInputOrWindowGainedFocus(startInputReason, client, windowToken, startInputFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods, unverifiedTargetSdkVersion);
        if (!(ret == null || ret == this.mNoBinding)) {
            setClientActiveIfShould();
        }
        return ret;
    }

    private InputBindResult secureImeStartInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int startInputFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion) {
        Log.i(TAG, "secureImeStartInputOrWindowGainedFocus");
        if (windowToken == null) {
            Slog.e(TAG, "windowToken cannot be null.");
            return InputBindResult.NULL;
        }
        InputBindResult result = startInputOrWindowGainedFocusInternal(startInputReason, client, windowToken, startInputFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods, unverifiedTargetSdkVersion);
        if (result != null) {
            return result;
        }
        Slog.wtf(TAG, "InputBindResult is @NonNull. startInputReason=" + InputMethodDebug.startInputReasonToString(startInputReason) + " windowFlags=#" + Integer.toHexString(windowFlags) + " editorInfo=" + attribute);
        return InputBindResult.NULL;
    }

    private InputBindResult startInputOrWindowGainedFocusInternal(int startInputReason, IInputMethodClient client, IBinder windowToken, int startInputFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion) {
        ResultReceiver resultReceiver;
        boolean calledFromValidUserLocked = calledFromValidUserLocked();
        InputBindResult res = null;
        synchronized (this.mMethodMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                ClientState cs = this.mClients.get(client.asBinder());
                if (cs == null) {
                    throw new IllegalArgumentException("unknown client " + client.asBinder());
                } else if (!this.mWindowManagerInternal.isInputMethodClientFocus(cs.uid, cs.pid, cs.selfReportedDisplayId)) {
                    return InputBindResult.NOT_IME_TARGET_WINDOW;
                } else if (!calledFromValidUserLocked) {
                    Slog.w(TAG, "A background user is requesting window. Hiding IME.");
                    Slog.w(TAG, "If you want to interect with IME, you need android.permission.INTERACT_ACROSS_USERS_FULL");
                    hideCurrentInputLocked(0, null);
                    Binder.restoreCallingIdentity(ident);
                    return null;
                } else if (this.mCurFocusedWindow == windowToken) {
                    Slog.w(TAG, "Window already focused, ignoring focus gain of: " + client + " attribute=" + attribute + ", token = " + windowToken);
                    if (attribute != null) {
                        InputBindResult startInputUncheckedLocked = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, startInputFlags, startInputReason);
                        Binder.restoreCallingIdentity(ident);
                        return startInputUncheckedLocked;
                    }
                    if (this.mInputShown) {
                        Slog.i(TAG, "Window already focused, force refreshime kgon:" + isKeyguardLocked());
                        updateSystemUi(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                    }
                    Binder.restoreCallingIdentity(ident);
                    return null;
                } else {
                    this.mCurFocusedWindow = windowToken;
                    this.mCurFocusedWindowSoftInputMode = softInputMode;
                    this.mCurFocusedWindowClient = cs;
                    boolean doAutoShow = (softInputMode & 240) == 16 || this.mRes.getConfiguration().isLayoutSizeAtLeast(3);
                    boolean isTextEditor = (startInputFlags & 2) != 0;
                    boolean didStart = false;
                    int i = softInputMode & 15;
                    if (i != 0) {
                        if (i != 1) {
                            if (i != 2) {
                                if (i == 3) {
                                    if (DEBUG_FLOW) {
                                        Slog.v(TAG, "Window asks to hide input");
                                    }
                                    hideCurrentInputLocked(0, null);
                                } else if (i != 4) {
                                    if (i == 5) {
                                        if (DEBUG_FLOW) {
                                            Slog.v(TAG, "Window asks to always show input");
                                        }
                                        if (attribute != null) {
                                            resultReceiver = null;
                                            res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, startInputFlags, startInputReason);
                                            didStart = true;
                                        } else {
                                            resultReceiver = null;
                                        }
                                        showCurrentInputLocked(1, resultReceiver);
                                    }
                                } else if ((softInputMode & 256) != 0) {
                                    if (DEBUG_FLOW) {
                                        Slog.v(TAG, "Window asks to show input going forward");
                                    }
                                    if (attribute != null) {
                                        res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, startInputFlags, startInputReason);
                                        didStart = true;
                                    }
                                    showCurrentInputLocked(1, null);
                                }
                            } else if ((softInputMode & 256) != 0) {
                                if (DEBUG_FLOW) {
                                    Slog.v(TAG, "Window asks to hide input going forward");
                                }
                                hideCurrentInputLocked(0, null);
                            }
                        }
                    } else if (!isTextEditor || !doAutoShow) {
                        if (WindowManager.LayoutParams.mayUseInputMethod(windowFlags)) {
                            if (DEBUG_FLOW) {
                                Slog.v(TAG, "Unspecified window will hide input");
                            }
                            hideCurrentInputLocked(2, null);
                        }
                    } else if ((softInputMode & 256) != 0) {
                        if (DEBUG_FLOW) {
                            Slog.v(TAG, "Unspecified window will show input");
                        }
                        if (attribute != null) {
                            res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, startInputFlags, startInputReason);
                            didStart = true;
                        }
                        showCurrentInputLocked(1, null);
                    } else {
                        Slog.v(TAG, "start input fail");
                    }
                    if (!didStart && attribute != null) {
                        res = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, startInputFlags, startInputReason);
                    }
                    Binder.restoreCallingIdentity(ident);
                    return res;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) {
    }

    public void showInputMethodPickerFromSystem(IInputMethodClient client, int a, int b) {
    }

    public void showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient client, String inputMethodId) {
    }

    public boolean isInputMethodPickerShownForTest() {
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean switchToNextInputMethod(IBinder token, boolean onlyCurrentIme) {
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldOfferSwitchingToNextInputMethod(IBinder token) {
        return false;
    }

    public InputMethodSubtype getLastInputMethodSubtype() {
        synchronized (this.mMethodMap) {
            if (!calledFromValidUserLocked()) {
                return null;
            }
            Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme != null && !TextUtils.isEmpty((CharSequence) lastIme.first)) {
                if (!TextUtils.isEmpty((CharSequence) lastIme.second)) {
                    InputMethodInfo lastImi = this.mMethodMap.get(lastIme.first);
                    if (lastImi == null) {
                        return null;
                    }
                    try {
                        int lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, Integer.parseInt((String) lastIme.second));
                        if (lastSubtypeId >= 0) {
                            if (lastSubtypeId < lastImi.getSubtypeCount()) {
                                return lastImi.getSubtypeAt(lastSubtypeId);
                            }
                        }
                        return null;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
            return null;
        }
    }

    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
    }

    public int getInputMethodWindowVisibleHeight() {
        return this.mWindowManagerInternal.getInputMethodWindowVisibleHeight(this.mCurTokenDisplayId);
    }

    public void reportActivityView(IInputMethodClient parentClient, int childDisplayId, float[] matrixValues) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyUserAction(IBinder token) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applyImeVisibility(IBinder token, boolean setVisible) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportPreRendered(IBinder token, EditorInfo info) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportStartInput(IBinder token, IBinder startInputToken) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideMySoftInput(IBinder token, int flags) {
        synchronized (this.mMethodMap) {
            if (!calledWithValidTokenLocked(token)) {
                int uid = Binder.getCallingUid();
                Slog.e(TAG, "Ignoring hideInputMethod due to an invalid token. uid:" + uid + " token:" + token);
                return;
            }
            if (DEBUG_FLOW) {
                Slog.v(TAG, "hideMySoftInput, pid=" + Binder.getCallingPid() + ", token=" + token);
            }
            long ident = Binder.clearCallingIdentity();
            try {
                hideCurrentInputLocked(flags, null);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showMySoftInput(IBinder token, int flags) {
        synchronized (this.mMethodMap) {
            if (!calledWithValidTokenLocked(token)) {
                int uid = Binder.getCallingUid();
                Slog.e(TAG, "Ignoring showMySoftInput due to an invalid token. uid:" + uid + " token:" + token);
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                showCurrentInputLocked(flags, null);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setEnabledSessionInMainThread(SessionState session) {
        SessionState sessionState = this.mEnabledSession;
        if (sessionState != session) {
            if (!(sessionState == null || sessionState.session == null)) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, false);
                } catch (RemoteException e) {
                    Slog.i(TAG, "mEnableSession setSessionEnabled error.");
                }
            }
            this.mEnabledSession = session;
            SessionState sessionState2 = this.mEnabledSession;
            if (sessionState2 != null && sessionState2.session != null) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, true);
                } catch (RemoteException e2) {
                    Slog.i(TAG, "session setSessionEnabled error.");
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0077, code lost:
        if (android.os.Binder.isProxy(r6) != false) goto L_0x0079;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0079, code lost:
        r7.channel.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a3, code lost:
        if (android.os.Binder.isProxy(r6) != false) goto L_0x0079;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x012b, code lost:
        if (android.os.Binder.isProxy(r6) != false) goto L_0x012d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x012d, code lost:
        r7.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0140, code lost:
        if (android.os.Binder.isProxy(r6) != false) goto L_0x012d;
     */
    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        boolean z = false;
        switch (msg.what) {
            case 1000:
                try {
                    ((IInputMethod) msg.obj).unbindInput();
                } catch (RemoteException e) {
                    Slog.i(TAG, "unbindInput error.");
                }
                return true;
            case 1010:
                SomeArgs args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).bindInput((InputBinding) args.arg2);
                } catch (RemoteException e2) {
                    Slog.i(TAG, "bindInput error.");
                }
                args.recycle();
                return true;
            case 1020:
                SomeArgs args2 = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args2.arg1).showSoftInput(msg.arg1, (ResultReceiver) args2.arg2);
                } catch (RemoteException e3) {
                    Slog.i(TAG, "showSoftInput error.");
                }
                args2.recycle();
                return true;
            case 1030:
                SomeArgs args3 = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args3.arg1).hideSoftInput(0, (ResultReceiver) args3.arg2);
                } catch (RemoteException e4) {
                    Slog.i(TAG, "hideSoftInput error.");
                }
                args3.recycle();
                return true;
            case MSG_INITIALIZE_IME /* 1040 */:
                SomeArgs args4 = (SomeArgs) msg.obj;
                try {
                    if (args4.arg2 instanceof IBinder) {
                        IBinder token = (IBinder) args4.arg2;
                        ((IInputMethod) args4.arg1).initializeInternal(token, msg.arg1, new InputMethodPrivilegedOperationsImpl(this, token));
                    }
                } catch (RemoteException e5) {
                    Slog.i(TAG, "initailizeInternal error.");
                }
                args4.recycle();
                return true;
            case MSG_CREATE_SESSION /* 1050 */:
                SomeArgs args5 = (SomeArgs) msg.obj;
                IInputMethod method = (IInputMethod) args5.arg1;
                InputChannel channel = (InputChannel) args5.arg2;
                try {
                    method.createSession(channel, (IInputSessionCallback) args5.arg3);
                    if (channel != null) {
                        break;
                    }
                } catch (RemoteException e6) {
                    Slog.i(TAG, "create session error.");
                    if (channel != null) {
                        break;
                    }
                } catch (Throwable th) {
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                    throw th;
                }
                args5.recycle();
                return true;
            case 2000:
                int missingMethods = msg.arg1;
                boolean restarting = msg.arg2 != 0;
                SomeArgs args6 = (SomeArgs) msg.obj;
                IBinder startInputToken = (IBinder) args6.arg1;
                SessionState session = (SessionState) args6.arg2;
                IInputContext inputContext = (IInputContext) args6.arg3;
                EditorInfo editorInfo = (EditorInfo) args6.arg4;
                try {
                    setEnabledSessionInMainThread(session);
                    session.method.startInput(startInputToken, inputContext, missingMethods, editorInfo, restarting, session.client.shouldPreRenderIme);
                } catch (RemoteException e7) {
                    Slog.i(TAG, "start input error.");
                }
                args6.recycle();
                return true;
            case 3000:
                try {
                    ((IInputMethodClient) msg.obj).onUnbindMethod(msg.arg1, msg.arg2);
                } catch (RemoteException e8) {
                    Slog.i(TAG, "There is nothing interesting about the last client dying.");
                }
                return true;
            case MSG_BIND_CLIENT /* 3010 */:
                SomeArgs args7 = (SomeArgs) msg.obj;
                IInputMethodClient client = (IInputMethodClient) args7.arg1;
                InputBindResult res = (InputBindResult) args7.arg2;
                try {
                    client.onBindMethod(res);
                    if (res.channel != null) {
                        break;
                    }
                } catch (RemoteException e9) {
                    Slog.w(TAG, "Client died receiving input method " + args7.arg2);
                    if (res.channel != null) {
                        break;
                    }
                } catch (Throwable th2) {
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                    throw th2;
                }
                args7.recycle();
                return true;
            case MSG_SET_ACTIVE /* 3020 */:
                try {
                    if (msg.arg1 == 1) {
                        IInputMethodClient iInputMethodClient = ((ClientState) msg.obj).client;
                        if (msg.arg2 != 0) {
                            z = true;
                        }
                        iInputMethodClient.setActive(true, z);
                    }
                } catch (RemoteException e10) {
                    Slog.w(TAG, "Got RemoteException sending setActive(false) notification to pid " + ((ClientState) msg.obj).pid + " uid " + ((ClientState) msg.obj).uid);
                }
                return true;
            case MSG_SET_INTERACTIVE /* 3030 */:
                if (msg.arg1 != 0) {
                    z = true;
                }
                handleSetInteractive(z);
                return true;
            case MSG_SHOULD_SET_ACTIVE /* 3060 */:
                this.mShouldSetActive = true;
                return true;
            case 5000:
                onUnlockUser(msg.arg1);
                return true;
            default:
                return false;
        }
    }

    private void handleSetInteractive(boolean interactive) {
        synchronized (this.mMethodMap) {
            this.mIsInteractive = interactive;
            int i = 0;
            updateSystemUiLocked(this.mCurToken, interactive ? this.mImeWindowVis : 0, this.mBackDisposition);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                IInputMethodClient iInputMethodClient = this.mCurClient.client;
                HandlerCaller handlerCaller = this.mCaller;
                int i2 = this.mIsInteractive ? 1 : 0;
                if (this.mInFullscreenMode) {
                    i = 1;
                }
                executeOrSendMessage(iInputMethodClient, handlerCaller.obtainMessageIIO((int) MSG_SET_ACTIVE, i2, i, this.mCurClient));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void buildInputMethodListLocked(boolean resetDefaultEnabledIme) {
        this.mMethodList.clear();
        this.mMethodMap.clear();
        List<ResolveInfo> services = this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.view.InputMethod"), 32896, this.mSettings.getCurrentUserId());
        for (int i = 0; i < services.size(); i++) {
            ResolveInfo ri = services.get(i);
            ServiceInfo si = ri.serviceInfo;
            ComponentName compName = new ComponentName(si.packageName, si.name);
            if (!"android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                Slog.w(TAG, "Skipping input method " + compName + ": it does not require the permission android.permission.BIND_INPUT_METHOD");
            } else if (shouldBuildInputMethodList(si.packageName)) {
                try {
                    InputMethodInfo methodInfo = new InputMethodInfo(this.mContext, ri);
                    this.mMethodList.add(methodInfo);
                    this.mMethodMap.put(methodInfo.getId(), methodInfo);
                } catch (IOException | XmlPullParserException e) {
                    Slog.w(TAG, "Unable to load input method " + compName, e);
                }
            }
        }
        updateSecureIMEStatus();
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        return null;
    }

    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        return false;
    }

    private static final class LocalSecureServiceImpl implements HwSecureInputMethodManagerInternal {
        private final HwSecureInputMethodManagerService mService;

        LocalSecureServiceImpl(HwSecureInputMethodManagerService service) {
            this.mService = service;
        }

        @Override // com.android.server.inputmethod.HwSecureInputMethodManagerInternal
        public void setClientActiveFlag() {
            this.mService.mHandler.sendEmptyMessage(HwSecureInputMethodManagerService.MSG_SHOULD_SET_ACTIVE);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IInputContentUriToken createInputContentUriToken(IBinder token, Uri contentUri, String packageName) {
        if (token == null) {
            throw new NullPointerException("token");
        } else if (packageName == null) {
            throw new NullPointerException(AppActConstant.ATTR_PACKAGE_NAME);
        } else if (contentUri == null) {
            throw new NullPointerException("contentUri");
        } else if ("content".equals(contentUri.getScheme())) {
            synchronized (this.mMethodMap) {
                int uid = Binder.getCallingUid();
                if (this.mCurMethodId == null) {
                    return null;
                }
                if (this.mCurToken != token) {
                    Slog.e(TAG, "Ignoring createInputContentUriToken mCurToken=" + this.mCurToken + " token=" + token);
                    return null;
                } else if (!TextUtils.equals(this.mCurAttribute.packageName, packageName)) {
                    Slog.e(TAG, "Ignoring createInputContentUriToken mCurAttribute.packageName=" + this.mCurAttribute.packageName + " packageName=" + packageName);
                    return null;
                } else {
                    int imeUserId = UserHandle.getUserId(uid);
                    int appUserId = UserHandle.getUserId(this.mCurClient.uid);
                    return new InputContentUriTokenHandler(ContentProvider.getUriWithoutUserId(contentUri), uid, packageName, ContentProvider.getUserIdFromUri(contentUri, imeUserId), appUserId);
                }
            }
        } else {
            throw new InvalidParameterException("contentUri must have content scheme");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean switchToPreviousInputMethod(IBinder token) {
        return false;
    }

    /* access modifiers changed from: private */
    public static final class InputMethodPrivilegedOperationsImpl extends IInputMethodPrivilegedOperations.Stub {
        private final HwSecureInputMethodManagerService mImms;
        private final IBinder mToken;

        InputMethodPrivilegedOperationsImpl(HwSecureInputMethodManagerService imms, IBinder token) {
            this.mImms = imms;
            this.mToken = token;
        }

        public void setImeWindowStatus(int vis, int backDisposition) {
            this.mImms.setImeWindowStatus(this.mToken, vis, backDisposition);
        }

        public void reportStartInput(IBinder startInputToken) {
            this.mImms.reportStartInput(this.mToken, startInputToken);
        }

        public void applyImeVisibility(boolean setVisible) {
            this.mImms.applyImeVisibility(this.mToken, setVisible);
        }

        public IInputContentUriToken createInputContentUriToken(Uri contentUri, String packageName) {
            return this.mImms.createInputContentUriToken(this.mToken, contentUri, packageName);
        }

        public void reportFullscreenMode(boolean fullscreen) {
            this.mImms.reportFullscreenMode(this.mToken, fullscreen);
        }

        public void setInputMethod(String id) {
        }

        public void setInputMethodAndSubtype(String id, InputMethodSubtype subtype) {
        }

        public void hideMySoftInput(int flags) {
            this.mImms.hideMySoftInput(this.mToken, flags);
        }

        public void showMySoftInput(int flags) {
            this.mImms.showMySoftInput(this.mToken, flags);
        }

        public void updateStatusIcon(String packageName, int iconId) {
            this.mImms.updateStatusIcon(this.mToken, packageName, iconId);
        }

        public boolean switchToPreviousInputMethod() {
            return this.mImms.switchToPreviousInputMethod(this.mToken);
        }

        public boolean switchToNextInputMethod(boolean onlyCurrentIme) {
            return this.mImms.switchToNextInputMethod(this.mToken, onlyCurrentIme);
        }

        public boolean shouldOfferSwitchingToNextInputMethod() {
            return this.mImms.shouldOfferSwitchingToNextInputMethod(this.mToken);
        }

        public void notifyUserAction() {
            this.mImms.notifyUserAction(this.mToken);
        }

        public void reportPreRendered(EditorInfo info) {
            this.mImms.reportPreRendered(this.mToken, info);
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        ClientState client;
        ClientState focusedWindowClient;
        IInputMethod method;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump InputMethodManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        Printer writerPrinter = new PrintWriterPrinter(pw);
        synchronized (this.mMethodMap) {
            writerPrinter.println("  Clients:");
            int numClients = this.mClients.size();
            for (int i = 0; i < numClients; i++) {
                ClientState ci = this.mClients.valueAt(i);
                writerPrinter.println("  Client " + ci + AwarenessInnerConstants.COLON_KEY);
                StringBuilder sb = new StringBuilder();
                sb.append("    client=");
                sb.append(ci.client);
                writerPrinter.println(sb.toString());
                writerPrinter.println("    inputContext=" + ci.inputContext);
                writerPrinter.println("    sessionRequested=" + ci.sessionRequested);
                writerPrinter.println("    curSession=" + ci.curSession);
            }
            writerPrinter.println("  mCurMethodId=" + this.mCurMethodId);
            client = this.mCurClient;
            writerPrinter.println("  mCurClient=" + client + " mCurSeq=" + this.mCurSeq);
            writerPrinter.println("  mCurFocusedWindow=" + this.mCurFocusedWindow + " softInputMode=" + InputMethodDebug.softInputModeToString(this.mCurFocusedWindowSoftInputMode) + " client=" + this.mCurFocusedWindowClient);
            focusedWindowClient = this.mCurFocusedWindowClient;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("  mCurFocusedWindowClient=");
            sb2.append(focusedWindowClient);
            writerPrinter.println(sb2.toString());
            writerPrinter.println("  mCurId=" + this.mCurId + " mHaveConnect=" + this.mHaveConnection + " mBoundToMethod=" + this.mBoundToMethod);
            StringBuilder sb3 = new StringBuilder();
            sb3.append("  mCurToken=");
            sb3.append(this.mCurToken);
            writerPrinter.println(sb3.toString());
            writerPrinter.println("  mCurTokenDisplayId=" + this.mCurTokenDisplayId);
            writerPrinter.println("  mCurIntent=" + this.mCurIntent);
            method = this.mCurMethod;
            writerPrinter.println("  mCurMethod=" + this.mCurMethod);
            writerPrinter.println("  mEnabledSession=" + this.mEnabledSession);
            writerPrinter.println("  mShowRequested=" + this.mShowRequested + " mShowExplicitlyRequested=" + this.mShowExplicitlyRequested + " mShowForced=" + this.mShowForced + " mInputShown=" + this.mInputShown);
            StringBuilder sb4 = new StringBuilder();
            sb4.append("  mInFullscreenMode=");
            sb4.append(this.mInFullscreenMode);
            writerPrinter.println(sb4.toString());
            writerPrinter.println("  mSystemReady=" + this.mSystemReady + " mInteractive=" + this.mIsInteractive);
            writerPrinter.println("  mSettings:");
        }
        writerPrinter.println(" ");
        if (client != null) {
            pw.flush();
            try {
                client.client.asBinder().dump(fd, args);
            } catch (RemoteException e) {
                writerPrinter.println("Input method client dead: " + e);
            }
        } else {
            writerPrinter.println("No input method client.");
        }
        if (!(focusedWindowClient == null || client == focusedWindowClient)) {
            writerPrinter.println(" ");
            writerPrinter.println("Warning: Current input method client doesn't match the last focused. window.");
            writerPrinter.println("Dumping input method client in the last focused window just in case.");
            writerPrinter.println(" ");
            pw.flush();
            try {
                focusedWindowClient.client.asBinder().dump(fd, args);
            } catch (RemoteException e2) {
                writerPrinter.println("Input method client in focused window dead: " + e2);
            }
        }
        writerPrinter.println(" ");
        if (method != null) {
            pw.flush();
            try {
                method.asBinder().dump(fd, args);
            } catch (RemoteException e3) {
                writerPrinter.println("Input method service dead: " + e3);
            }
        } else {
            writerPrinter.println("No input method service.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unbindVisibleConnection() {
        synchronized (this.mVisibleServiceLock) {
            if (this.mVisibleBound) {
                this.mContext.unbindService(this.mVisibleConnection);
                this.mVisibleBound = false;
            }
        }
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.inputmethod.HwSecureInputMethodManagerService$HwInnerSecureInputMethodManagerService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    public void hideInputMethod() {
        hideCurrentInputLocked(0, null);
    }

    public class HwInnerSecureInputMethodManagerService extends IHwSecureInputMethodManager.Stub {
        HwSecureInputMethodManagerService mHwSIMMS;

        HwInnerSecureInputMethodManagerService(HwSecureInputMethodManagerService imms) {
            this.mHwSIMMS = imms;
        }

        public void hideInputMethod() {
            this.mHwSIMMS.hideInputMethod();
        }
    }
}
