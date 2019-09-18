package com.android.server;

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
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.LocaleList;
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
import android.util.EventLog;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.Slog;
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
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.InputBindResult;
import com.android.internal.view.InputMethodClient;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.inputmethod.HwInputMethodUtils;
import com.android.server.security.trustcircle.tlv.command.query.DATA_TCIS_ERROR_STEP;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wm.WindowManagerInternal;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.xmlpull.v1.XmlPullParserException;

public class HwSecureInputMethodManagerService extends AbsInputMethodManagerService implements ServiceConnection, Handler.Callback {
    static final boolean DEBUG = false;
    static final boolean DEBUG_FLOW = Log.HWINFO;
    static final boolean DEBUG_RESTORE = false;
    static final int MSG_ATTACH_TOKEN = 1040;
    static final int MSG_BIND_CLIENT = 3010;
    static final int MSG_BIND_INPUT = 1010;
    static final int MSG_CREATE_SESSION = 1050;
    static final int MSG_HIDE_SOFT_INPUT = 1030;
    static final int MSG_REPORT_FULLSCREEN_MODE = 3045;
    static final int MSG_SET_ACTIVE = 3020;
    static final int MSG_SET_INTERACTIVE = 3030;
    static final int MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER = 3040;
    static final int MSG_SHOW_SOFT_INPUT = 1020;
    static final int MSG_START_INPUT = 2000;
    static final int MSG_UNBIND_CLIENT = 3000;
    static final int MSG_UNBIND_INPUT = 1000;
    private static final int NOT_A_SUBTYPE_ID = -1;
    static final int SECURE_SUGGESTION_SPANS_MAX_SIZE = 20;
    static final String TAG = "SecInputMethodManagerService";
    static final long TIME_TO_RECONNECT = 3000;
    static final int UNBIND_SECIME_IF_SHOULD = 10000;
    private static final String secImeId = "com.huawei.secime/.SoftKeyboard";
    private final AppOpsManager mAppOpsManager;
    int mBackDisposition = 0;
    boolean mBoundToMethod;
    final HandlerCaller mCaller;
    final HashMap<IBinder, ClientState> mClients = new HashMap<>();
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
    int mCurUserActionNotificationSequenceNumber = 0;
    SessionState mEnabledSession;
    boolean mHaveConnection;
    private final IPackageManager mIPackageManager = AppGlobals.getPackageManager();
    final IWindowManager mIWindowManager;
    int mImeWindowVis;
    boolean mInFullscreenMode;
    boolean mInputShown;
    boolean mIsInteractive = true;
    private KeyguardManager mKeyguardManager;
    long mLastBindTime;
    private LocaleList mLastSystemLocales;
    final ArrayList<InputMethodInfo> mMethodList = new ArrayList<>();
    final HashMap<String, InputMethodInfo> mMethodMap = new HashMap<>();
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor();
    final InputBindResult mNoBinding;
    final Resources mRes;
    private SecureSettingsObserver mSecureSettingsObserver;
    private final LruCache<SuggestionSpan, InputMethodInfo> mSecureSuggestionSpans = new LruCache<>(20);
    final InputMethodUtils.InputMethodSettings mSettings;
    private final HashMap<InputMethodInfo, ArrayList<InputMethodSubtype>> mShortcutInputMethodsAndSubtypes = new HashMap<>();
    /* access modifiers changed from: private */
    public boolean mShouldSetActive;
    boolean mShowExplicitlyRequested;
    boolean mShowForced;
    boolean mShowRequested;
    private final String mSlotIme;
    @GuardedBy("mMethodMap")
    private final WeakHashMap<IBinder, StartInputInfo> mStartInputMap = new WeakHashMap<>();
    private StatusBarManagerService mStatusBar;
    boolean mSystemReady = false;
    private int mUnbindCounter = 0;
    private final UserManager mUserManager;
    boolean mVisibleBound = false;
    final ServiceConnection mVisibleConnection = new MySerServiceConnection();
    final WindowManagerInternal mWindowManagerInternal;

    static final class ClientState {
        final InputBinding binding = new InputBinding(null, this.inputContext.asBinder(), this.uid, this.pid);
        final IInputMethodClient client;
        SessionState curSession;
        final IInputContext inputContext;
        final int pid;
        boolean sessionRequested;
        final int uid;

        public String toString() {
            return "ClientState{" + Integer.toHexString(System.identityHashCode(this)) + " uid " + this.uid + " pid " + this.pid + "}";
        }

        ClientState(IInputMethodClient _client, IInputContext _inputContext, int _uid, int _pid) {
            this.client = _client;
            this.inputContext = _inputContext;
            this.uid = _uid;
            this.pid = _pid;
        }
    }

    class ImmsBroadcastReceiver extends BroadcastReceiver {
        ImmsBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_REMOVED".equals(action)) {
                HwSecureInputMethodManagerService.this.updateCurrentProfileIds();
                return;
            }
            Slog.w(HwSecureInputMethodManagerService.TAG, "Unexpected intent " + intent);
        }
    }

    private final class LocalSecureServiceImpl implements HwSecureInputMethodManagerInternal {
        private LocalSecureServiceImpl() {
        }

        public void setClientActiveFlag() {
            boolean unused = HwSecureInputMethodManagerService.this.mShouldSetActive = true;
        }
    }

    private static final class MethodCallback extends IInputSessionCallback.Stub {
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

        /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.HwSecureInputMethodManagerService, android.os.IBinder] */
        public void onStart() {
            publishBinderService(HwInputMethodManagerService.SECURITY_INPUT_SERVICE_NAME, this.mService);
        }

        public void onSwitchUser(int userHandle) {
            this.mService.onSwitchUser(userHandle);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemRunning(ServiceManager.getService("statusbar"));
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
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

    static class MySerServiceConnection implements ServiceConnection {
        MySerServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private class SecureSettingsObserver extends ContentObserver {
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

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                Slog.i(HwSecureInputMethodManagerService.TAG, "SecureSettingsObserver onChange, uri = " + uri.toString());
                if (Settings.Secure.getUriFor(HwInputMethodUtils.SETTINGS_SECURE_KEYBOARD_CONTROL).equals(uri) && !HwInputMethodUtils.isSecureIMEEnable(HwSecureInputMethodManagerService.this.mContext, HwSecureInputMethodManagerService.this.mSettings.getCurrentUserId()) && !HwInputMethodUtils.isNeedSecIMEInSpecialScenes(HwSecureInputMethodManagerService.this.mContext)) {
                    synchronized (HwSecureInputMethodManagerService.this.mMethodMap) {
                        HwSecureInputMethodManagerService.this.hideCurrentInputLocked(0, null);
                        HwSecureInputMethodManagerService.this.mCurMethodId = null;
                        HwSecureInputMethodManagerService.this.unbindCurrentMethodLocked(false);
                    }
                }
            }
        }
    }

    static class SessionState {
        InputChannel channel;
        final ClientState client;
        final IInputMethod method;
        IInputMethodSession session;

        public String toString() {
            return "SessionState{uid " + this.client.uid + " pid " + this.client.pid + " method " + Integer.toHexString(System.identityHashCode(this.method)) + " session " + Integer.toHexString(System.identityHashCode(this.session)) + " channel " + this.channel + "}";
        }

        SessionState(ClientState _client, IInputMethod _method, IInputMethodSession _session, InputChannel _channel) {
            this.client = _client;
            this.method = _method;
            this.session = _session;
            this.channel = _channel;
        }
    }

    private static class StartInputInfo {
        private static final AtomicInteger sSequenceNumber = new AtomicInteger(0);
        final int mClientBindSequenceNumber;
        final EditorInfo mEditorInfo;
        final String mImeId;
        final IBinder mImeToken;
        final boolean mRestarting;
        final int mSequenceNumber = sSequenceNumber.getAndIncrement();
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

    /* access modifiers changed from: protected */
    public boolean isSecureIME(String packageName) {
        return HwInputMethodManagerService.SECURE_IME_PACKAGENAME.equals(packageName);
    }

    /* access modifiers changed from: protected */
    public boolean shouldBuildInputMethodList(String packageName) {
        return HwInputMethodManagerService.SECURE_IME_PACKAGENAME.equals(packageName);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002e, code lost:
        return false;
     */
    private boolean setClientActiveIfShould() {
        if (!this.mShouldSetActive || !this.mIsInteractive) {
            return false;
        }
        synchronized (this.mMethodMap) {
            if (this.mCurClient != null && this.mCurClient.client != null) {
                executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, 1, this.mCurClient));
                this.mShouldSetActive = false;
                return true;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void switchUserExtra(int userId) {
        if (this.mSecureSettingsObserver != null) {
            this.mSecureSettingsObserver.registerContentObserverInner(userId);
        }
        if (!HwInputMethodUtils.isSecureIMEEnable(this.mContext, this.mSettings.getCurrentUserId()) && !HwInputMethodUtils.isNeedSecIMEInSpecialScenes(this.mContext)) {
            hideCurrentInputLocked(0, null);
            this.mCurMethodId = null;
            unbindCurrentMethodLocked(false);
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
        Context context2 = context;
        InputBindResult inputBindResult = new InputBindResult(-1, null, null, null, -1, -1);
        this.mNoBinding = inputBindResult;
        this.mContext = context2;
        this.mRes = context.getResources();
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mCaller = new HandlerCaller(context2, null, new HandlerCaller.Callback() {
            public void executeMessage(Message msg) {
                HwSecureInputMethodManagerService.this.handleMessage(msg);
            }
        }, true);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mSlotIme = this.mContext.getString(17041189);
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
        InputMethodUtils.InputMethodSettings inputMethodSettings = new InputMethodUtils.InputMethodSettings(this.mRes, context.getContentResolver(), this.mMethodMap, this.mMethodList, userId, !this.mSystemReady);
        this.mSettings = inputMethodSettings;
        updateCurrentProfileIds();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                synchronized (HwSecureInputMethodManagerService.this.mMethodMap) {
                    HwSecureInputMethodManagerService.this.resetStateIfCurrentLocaleChangedLocked();
                }
            }
        }, filter);
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
    public void resetStateIfCurrentLocaleChangedLocked() {
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
        this.mSettings.setCurrentProfileIds(this.mUserManager.getProfileIdsWithDisabled(this.mSettings.getCurrentUserId()));
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return HwSecureInputMethodManagerService.super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Input Method Manager Crash", e);
            }
            throw e;
        }
    }

    public void systemRunning(StatusBarManagerService statusBar) {
        this.mSecureSettingsObserver = new SecureSettingsObserver();
        this.mSecureSettingsObserver.registerContentObserverInner(this.mSettings.getCurrentUserId());
        if (LocalServices.getService(HwSecureInputMethodManagerInternal.class) == null) {
            LocalServices.addService(HwSecureInputMethodManagerInternal.class, new LocalSecureServiceImpl());
        }
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

    private boolean calledFromValidUser() {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (uid == 1000 || this.mSettings.isCurrentProfile(userId) || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            return true;
        }
        Slog.w(TAG, "--- IPC called from background users. Ignore. callers=" + Debug.getCallers(10));
        return false;
    }

    private boolean calledWithValidToken(IBinder token) {
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

    public List<InputMethodInfo> getInputMethodList() {
        ArrayList arrayList;
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        synchronized (this.mMethodMap) {
            arrayList = new ArrayList(this.mMethodList);
        }
        return arrayList;
    }

    public List<InputMethodInfo> getVrInputMethodList() {
        return getInputMethodList(true);
    }

    private List<InputMethodInfo> getInputMethodList(boolean isVrOnly) {
        ArrayList<InputMethodInfo> methodList;
        if (!calledFromValidUser()) {
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

    public List<InputMethodInfo> getEnabledInputMethodList() {
        return Collections.emptyList();
    }

    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId, boolean allowsImplicitlySelectedSubtypes) {
        return Collections.emptyList();
    }

    public void addClient(IInputMethodClient client, IInputContext inputContext, int uid, int pid) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                this.mClients.put(client.asBinder(), new ClientState(client, inputContext, uid, pid));
            }
        }
    }

    public void removeClient(IInputMethodClient client) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                ClientState cs = this.mClients.remove(client.asBinder());
                if (cs != null) {
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
    }

    /* access modifiers changed from: package-private */
    public void executeOrSendMessage(IInterface target, Message msg) {
        if (target != null) {
            if (target.asBinder() instanceof Binder) {
                this.mCaller.sendMessage(msg);
            } else {
                handleMessage(msg);
                msg.recycle();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unbindCurrentClientLocked(int unbindClientReason) {
        if (this.mCurClient != null) {
            if (this.mBoundToMethod) {
                this.mBoundToMethod = false;
                if (this.mCurMethod != null) {
                    executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(1000, this.mCurMethod));
                }
            }
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, 0, this.mCurClient));
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
        return 0;
    }

    private int getAppShowFlags() {
        if (this.mShowForced) {
            return 0 | 2;
        }
        if (!this.mShowExplicitlyRequested) {
            return 0 | 1;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public InputBindResult attachNewInputLocked(int startInputReason, boolean initial) {
        if (!this.mBoundToMethod) {
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(1010, this.mCurMethod, this.mCurClient.binding));
            this.mBoundToMethod = true;
        }
        SessionState session = this.mCurClient.curSession;
        Binder startInputToken = new Binder();
        int i = startInputReason;
        StartInputInfo info = new StartInputInfo(this.mCurToken, this.mCurId, i, !initial, this.mCurFocusedWindow, this.mCurAttribute, this.mCurFocusedWindowSoftInputMode, this.mCurSeq);
        this.mStartInputMap.put(startInputToken, info);
        if (session != null) {
            IInputMethod iInputMethod = session.method;
            HandlerCaller handlerCaller = this.mCaller;
            int i2 = this.mCurInputContextMissingMethods;
            IInputContext iInputContext = this.mCurInputContext;
            EditorInfo editorInfo = this.mCurAttribute;
            executeOrSendMessage(iInputMethod, handlerCaller.obtainMessageIIOOOO(2000, i2, initial ^ true ? 1 : 0, startInputToken, session, iInputContext, editorInfo));
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
        InputChannel inputChannel2 = inputChannel;
        InputBindResult inputBindResult = new InputBindResult(0, iInputMethodSession, inputChannel2, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
        return inputBindResult;
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
        } else {
            try {
                if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                    Slog.w(TAG, "Starting input on non-focused client " + cs.client + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
                    return null;
                }
            } catch (RemoteException e) {
            }
            return startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags, startInputReason);
        }
    }

    /* access modifiers changed from: package-private */
    public InputBindResult startInputUncheckedLocked(ClientState cs, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags, int startInputReason) {
        ClientState clientState = cs;
        EditorInfo editorInfo = attribute;
        int i = controlFlags;
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        if (!InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, clientState.uid, editorInfo.packageName)) {
            Slog.e(TAG, "Rejecting this client as it reported an invalid package name. uid=" + clientState.uid + " package=" + editorInfo.packageName);
            return this.mNoBinding;
        }
        if (this.mCurClient != clientState) {
            this.mCurClientInKeyguard = isKeyguardLocked();
            unbindCurrentClientLocked(1);
            if (this.mIsInteractive) {
                executeOrSendMessage(clientState.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, this.mIsInteractive ? 1 : 0, clientState));
            }
        }
        this.mCurSeq++;
        if (this.mCurSeq <= 0) {
            this.mCurSeq = 1;
        }
        this.mCurClient = clientState;
        this.mCurInputContext = inputContext;
        this.mCurInputContextMissingMethods = missingMethods;
        this.mCurAttribute = editorInfo;
        if ((65536 & i) != 0) {
            this.mShowRequested = true;
        }
        if (this.mCurId == null || !this.mCurId.equals(this.mCurMethodId)) {
            int i2 = startInputReason;
        } else {
            boolean z = false;
            if (clientState.curSession != null) {
                if ((i & 256) != 0) {
                    z = true;
                }
                return attachNewInputLocked(startInputReason, z);
            }
            int i3 = startInputReason;
            if (this.mHaveConnection) {
                if (this.mCurMethod != null) {
                    requestClientSessionLocked(cs);
                    InputBindResult inputBindResult = new InputBindResult(1, null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    return inputBindResult;
                } else if (SystemClock.uptimeMillis() < this.mLastBindTime + 3000) {
                    InputBindResult inputBindResult2 = new InputBindResult(2, null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    return inputBindResult2;
                } else {
                    EventLog.writeEvent(32000, new Object[]{this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), 0});
                }
            }
        }
        try {
            return startInputInnerLocked();
        } catch (RuntimeException e) {
            RuntimeException runtimeException = e;
            Slog.w(TAG, "Unexpected exception", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public InputBindResult startInputInnerLocked() {
        if (this.mCurMethodId == null || !this.mMethodMap.containsKey(this.mCurMethodId)) {
            return this.mNoBinding;
        }
        if (!this.mSystemReady) {
            InputBindResult inputBindResult = new InputBindResult(7, null, null, this.mCurMethodId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
            return inputBindResult;
        }
        InputMethodInfo info = this.mMethodMap.get(this.mCurMethodId);
        if (info == null) {
            Slog.w(TAG, "info == null id: " + this.mCurMethodId);
            return this.mNoBinding;
        }
        unbindCurrentMethodLocked(true);
        this.mCurIntent = new Intent("android.view.InputMethod");
        this.mCurIntent.setComponent(info.getComponent());
        this.mCurIntent.putExtra("android.intent.extra.client_label", 17040222);
        this.mCurIntent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.INPUT_METHOD_SETTINGS"), 0));
        if (bindCurrentInputMethodService(this.mCurIntent, this, 1610612741)) {
            this.mLastBindTime = SystemClock.uptimeMillis();
            this.mHaveConnection = true;
            this.mCurId = info.getId();
            this.mCurToken = new Binder();
            try {
                Slog.v(TAG, "Adding window token: " + this.mCurToken);
                this.mIWindowManager.addWindowToken(this.mCurToken, HwArbitrationDEFS.MSG_ARBITRATION_REQUEST_MPLINK, 0);
            } catch (RemoteException e) {
            }
            InputBindResult inputBindResult2 = new InputBindResult(2, null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
            return inputBindResult2;
        }
        this.mCurIntent = null;
        Slog.w(TAG, "Failure connecting to input method service: " + this.mCurIntent);
        return null;
    }

    private InputBindResult startInput(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        InputBindResult startInputLocked;
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mMethodMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                startInputLocked = startInputLocked(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return startInputLocked;
    }

    public void finishInput(IInputMethodClient client) {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0066, code lost:
        return;
     */
    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mMethodMap) {
            if (this.mCurIntent != null && name.equals(this.mCurIntent.getComponent())) {
                this.mCurMethod = IInputMethod.Stub.asInterface(service);
                if (this.mCurToken == null) {
                    Slog.w(TAG, "Service connected without a token!");
                    unbindCurrentMethodLocked(false);
                    return;
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Initiating attach with token: " + this.mCurToken);
                }
                executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_ATTACH_TOKEN, this.mCurMethod, this.mCurToken));
                if (this.mCurClient != null) {
                    clearClientSessionLocked(this.mCurClient);
                    requestClientSessionLocked(this.mCurClient);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0057, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0059, code lost:
        r9.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005c, code lost:
        return;
     */
    public void onSessionCreated(IInputMethod method, IInputMethodSession session, InputChannel channel) {
        synchronized (this.mMethodMap) {
            if (this.mCurMethod != null && method != null && this.mCurMethod.asBinder() == method.asBinder() && this.mCurClient != null) {
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "IME session created");
                }
                clearClientSessionLocked(this.mCurClient);
                this.mCurClient.curSession = new SessionState(this.mCurClient, method, session, channel);
                InputBindResult res = attachNewInputLocked(9, true);
                if (res != null) {
                    if (res.method != null) {
                        executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageOO(MSG_BIND_CLIENT, this.mCurClient.client, res));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unbindCurrentMethodLocked(boolean savePosition) {
        if (this.mVisibleBound) {
            this.mContext.unbindService(this.mVisibleConnection);
            this.mVisibleBound = false;
        }
        if (this.mHaveConnection) {
            this.mContext.unbindService(this);
            this.mHaveConnection = false;
        }
        if (this.mCurToken != null) {
            try {
                if ((this.mImeWindowVis & 1) != 0 && savePosition) {
                    this.mWindowManagerInternal.saveLastInputMethodWindowForTransition();
                }
                this.mIWindowManager.removeWindowToken(this.mCurToken, 0);
            } catch (RemoteException e) {
            }
            this.mCurToken = null;
        }
        this.mCurId = null;
        clearCurMethodLocked();
    }

    /* access modifiers changed from: package-private */
    public void resetCurrentMethodAndClient(int unbindClientReason) {
        this.mCurMethodId = null;
        unbindCurrentMethodLocked(false);
        unbindCurrentClientLocked(unbindClientReason);
    }

    /* access modifiers changed from: package-private */
    public void requestClientSessionLocked(ClientState cs) {
        if (!cs.sessionRequested) {
            InputChannel[] channels = InputChannel.openInputChannelPair(cs.toString());
            cs.sessionRequested = true;
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOOO(MSG_CREATE_SESSION, this.mCurMethod, channels[1], new MethodCallback(this, this.mCurMethod, channels[0])));
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
            for (ClientState cs : this.mClients.values()) {
                clearClientSessionLocked(cs);
            }
            finishSessionLocked(this.mEnabledSession);
            this.mEnabledSession = null;
            this.mCurMethod = null;
        }
        if (this.mStatusBar != null) {
            this.mStatusBar.setIconVisibility(this.mSlotIme, false);
        }
        this.mInFullscreenMode = false;
    }

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

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007f, code lost:
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0083, code lost:
        return;
     */
    public void updateStatusIcon(IBinder token, String packageName, int iconId) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (!calledWithValidToken(token)) {
                    int uid = Binder.getCallingUid();
                    Slog.e(TAG, "Ignoring updateStatusIcon due to an invalid token. uid:" + uid + " token:" + token);
                    Binder.restoreCallingIdentity(ident);
                } else if (iconId == 0) {
                    if (this.mStatusBar != null) {
                        this.mStatusBar.setIconVisibility(this.mSlotIme, false);
                    }
                } else if (packageName != null) {
                    String str = null;
                    CharSequence contentDescription = null;
                    try {
                        contentDescription = this.mContext.getPackageManager().getApplicationLabel(this.mIPackageManager.getApplicationInfo(packageName, 0, this.mSettings.getCurrentUserId()));
                    } catch (RemoteException e) {
                    }
                    if (this.mStatusBar != null) {
                        StatusBarManagerService statusBarManagerService = this.mStatusBar;
                        String str2 = this.mSlotIme;
                        if (contentDescription != null) {
                            str = contentDescription.toString();
                        }
                        statusBarManagerService.setIcon(str2, packageName, iconId, 0, str);
                        this.mStatusBar.setIconVisibility(this.mSlotIme, true);
                    }
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    private boolean shouldShowImeSwitcherLocked(int visibility) {
        return false;
    }

    private boolean isKeyguardLocked() {
        return this.mKeyguardManager != null && this.mKeyguardManager.isKeyguardLocked();
    }

    private boolean isScreenLocked() {
        return this.mKeyguardManager != null && this.mKeyguardManager.isKeyguardLocked() && this.mKeyguardManager.isKeyguardSecure();
    }

    public void setImeWindowStatus(IBinder token, IBinder startInputToken, int vis, int backDisposition) {
        StartInputInfo info;
        boolean dismissImeOnBackKeyPressed;
        if (!calledWithValidToken(token)) {
            int uid = Binder.getCallingUid();
            Slog.e(TAG, "Ignoring setImeWindowStatus due to an invalid token. uid:" + uid + " token:" + token);
            return;
        }
        synchronized (this.mMethodMap) {
            info = this.mStartInputMap.get(startInputToken);
            this.mImeWindowVis = vis;
            this.mBackDisposition = backDisposition;
            updateSystemUiLocked(token, vis, backDisposition);
        }
        boolean z = false;
        switch (backDisposition) {
            case 1:
                dismissImeOnBackKeyPressed = false;
                break;
            case 2:
                dismissImeOnBackKeyPressed = true;
                break;
            default:
                if ((vis & 2) == 0) {
                    dismissImeOnBackKeyPressed = false;
                    break;
                } else {
                    dismissImeOnBackKeyPressed = true;
                    break;
                }
        }
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if ((vis & 2) != 0) {
            z = true;
        }
        windowManagerInternal.updateInputMethodWindowStatus(token, z, dismissImeOnBackKeyPressed, info != null ? info.mTargetWindow : null);
    }

    private void updateSystemUi(IBinder token, int vis, int backDisposition) {
        synchronized (this.mMethodMap) {
            updateSystemUiLocked(token, vis, backDisposition);
        }
    }

    private void updateSystemUiLocked(IBinder token, int vis, int backDisposition) {
        if (!calledWithValidToken(token)) {
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
            this.mStatusBar.setImeWindowStatus(token, vis, backDisposition, needsToShowImeSwitcher);
        }
        Binder.restoreCallingIdentity(ident);
    }

    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                InputMethodInfo currentImi = this.mMethodMap.get(this.mCurMethodId);
                for (SuggestionSpan ss : spans) {
                    if (!TextUtils.isEmpty(ss.getNotificationTargetClassName())) {
                        this.mSecureSuggestionSpans.put(ss, currentImi);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0030, code lost:
        return;
     */
    public void reportFullscreenMode(IBinder token, boolean fullscreen) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                        this.mInFullscreenMode = fullscreen;
                        executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_REPORT_FULLSCREEN_MODE, fullscreen, this.mCurClient));
                    }
                }
            }
        }
    }

    public boolean notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        return false;
    }

    public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return false;
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if ((this.mCurClient == null || client == null || this.mCurClient.client.asBinder() != client.asBinder()) && client != null) {
                    try {
                        if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                            Slog.w(TAG, "Ignoring showSoftInput of uid " + uid + ": " + client);
                            Binder.restoreCallingIdentity(ident);
                            return false;
                        }
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    }
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Client requesting input be shown, requestedUid=" + uid);
                }
                boolean showCurrentInputLocked = showCurrentInputLocked(flags, resultReceiver);
                Binder.restoreCallingIdentity(ident);
                return showCurrentInputLocked;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
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
        }
        if (!this.mSystemReady) {
            return false;
        }
        boolean res = false;
        if (this.mCurMethod != null) {
            if (DEBUG_FLOW) {
                Slog.d(TAG, "showCurrentInputLocked: mCurToken=" + this.mCurToken);
            }
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageIOO(1020, getImeShowFlags(), this.mCurMethod, resultReceiver));
            this.mInputShown = true;
            if (this.mHaveConnection && !this.mVisibleBound) {
                bindCurrentInputMethodService(this.mCurIntent, this.mVisibleConnection, 201326593);
                this.mVisibleBound = true;
            }
            res = true;
        } else if (this.mHaveConnection && SystemClock.uptimeMillis() >= this.mLastBindTime + 3000) {
            EventLog.writeEvent(32000, new Object[]{this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), 1});
            Slog.w(TAG, "Force disconnect/connect to the IME in showCurrentInputLocked()");
            this.mContext.unbindService(this);
            bindCurrentInputMethodService(this.mCurIntent, this, DATA_TCIS_ERROR_STEP.ID);
        }
        return res;
    }

    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return false;
        }
        int pid = Binder.getCallingPid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if ((this.mCurClient == null || client == null || this.mCurClient.client.asBinder() != client.asBinder()) && client != null) {
                    try {
                        if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                            Binder.restoreCallingIdentity(ident);
                            return false;
                        }
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    }
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Client requesting input be hidden, pid=" + pid);
                }
                boolean hideCurrentInputLocked = hideCurrentInputLocked(flags, resultReceiver);
                Binder.restoreCallingIdentity(ident);
                return hideCurrentInputLocked;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
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
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(1030, this.mCurMethod, resultReceiver));
            res = true;
        } else {
            res = false;
        }
        if (!this.mInputShown && !this.mShowRequested) {
            res = false;
        }
        if (this.mHaveConnection && this.mVisibleBound) {
            this.mContext.unbindService(this.mVisibleConnection);
            this.mVisibleBound = false;
        }
        this.mInputShown = false;
        this.mShowRequested = false;
        this.mShowExplicitlyRequested = false;
        this.mShowForced = false;
        return res;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0087, code lost:
        if (r2 != 8) goto L_0x0093;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0089, code lost:
        android.util.Slog.i(TAG, "startInputOrWindowGainedFocus, client deactive by imms, set active again to enable secure inputmethod");
        r1.mShouldSetActive = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0093, code lost:
        r0 = secureImeStartInputOrWindowGainedFocus(r12, r13, r14, r15, r16, r17, r18, r19, r20);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0097, code lost:
        if (r0 == null) goto L_0x00a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x009b, code lost:
        if (r0 == r1.mNoBinding) goto L_0x00a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x009d, code lost:
        setClientActiveIfShould();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00a0, code lost:
        return r0;
     */
    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion) {
        int i = startInputReason;
        IBinder iBinder = windowToken;
        if (!calledFromValidUser()) {
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
                if (iBinder == null || i != 10000) {
                    this.mUnbindCounter = 0;
                    Binder.restoreCallingIdentity(ident);
                } else {
                    if (this.mCurFocusedWindow != iBinder && this.mHaveConnection && this.mUnbindCounter > 1 && !isScreenLocked()) {
                        Slog.d(TAG, "unbind secime");
                        unbindCurrentMethodLocked(true);
                        unbindCurrentClientLocked(10000);
                    }
                    this.mUnbindCounter++;
                    this.mCurFocusedWindow = iBinder;
                    return null;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private InputBindResult secureImeStartInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        if (windowToken != null) {
            return windowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods);
        }
        return startInput(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0128 A[Catch:{ all -> 0x022d, all -> 0x0236 }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x012a A[Catch:{ all -> 0x022d, all -> 0x0236 }] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0134 A[Catch:{ all -> 0x022d, all -> 0x0236 }] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0136 A[Catch:{ all -> 0x022d, all -> 0x0236 }] */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0160 A[Catch:{ all -> 0x022d, all -> 0x0236 }] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x018a A[Catch:{ all -> 0x022d, all -> 0x0236 }] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x019a A[Catch:{ all -> 0x022d, all -> 0x0236 }] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01ae A[Catch:{ all -> 0x022d, all -> 0x0236 }] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01af A[Catch:{ all -> 0x022d, all -> 0x0236 }] */
    public InputBindResult windowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        HashMap<String, InputMethodInfo> hashMap;
        boolean z;
        ResultReceiver resultReceiver;
        int i;
        IBinder iBinder = windowToken;
        int i2 = softInputMode;
        EditorInfo editorInfo = attribute;
        boolean calledFromValidUser = calledFromValidUser();
        InputBindResult res = null;
        long ident = Binder.clearCallingIdentity();
        try {
            HashMap<String, InputMethodInfo> hashMap2 = this.mMethodMap;
            synchronized (hashMap2) {
                try {
                    ClientState cs = this.mClients.get(client.asBinder());
                    if (cs != null) {
                        try {
                            if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                                Slog.w(TAG, "Focus gain on non-focused client " + cs.client + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
                                try {
                                    Binder.restoreCallingIdentity(ident);
                                    return null;
                                } catch (Throwable th) {
                                    th = th;
                                    hashMap = hashMap2;
                                    boolean z2 = calledFromValidUser;
                                    try {
                                        throw th;
                                    } catch (Throwable th2) {
                                        th = th2;
                                    }
                                }
                            }
                        } catch (RemoteException e) {
                        }
                        if (!calledFromValidUser) {
                            Slog.w(TAG, "A background user is requesting window. Hiding IME.");
                            Slog.w(TAG, "If you want to interect with IME, you need android.permission.INTERACT_ACROSS_USERS_FULL");
                            hideCurrentInputLocked(0, null);
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        } else if (this.mCurFocusedWindow == iBinder) {
                            Slog.w(TAG, "Window already focused, ignoring focus gain of: " + client + " attribute=" + editorInfo + ", token = " + iBinder);
                            if (editorInfo != null) {
                                boolean z3 = calledFromValidUser;
                                ClientState clientState = cs;
                                hashMap = hashMap2;
                                InputBindResult startInputUncheckedLocked = startInputUncheckedLocked(cs, inputContext, missingMethods, editorInfo, controlFlags, startInputReason);
                                Binder.restoreCallingIdentity(ident);
                                return startInputUncheckedLocked;
                            }
                            hashMap = hashMap2;
                            boolean z4 = calledFromValidUser;
                            ClientState clientState2 = cs;
                            if (this.mInputShown) {
                                Slog.i(TAG, "Window already focused, force refreshime kgon:" + isKeyguardLocked());
                                updateSystemUi(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                            }
                            Binder.restoreCallingIdentity(ident);
                            return null;
                        } else {
                            hashMap = hashMap2;
                            boolean z5 = calledFromValidUser;
                            ClientState cs2 = cs;
                            this.mCurFocusedWindow = iBinder;
                            this.mCurFocusedWindowSoftInputMode = i2;
                            this.mCurFocusedWindowClient = cs2;
                            if ((i2 & 240) != 16) {
                                if (!this.mRes.getConfiguration().isLayoutSizeAtLeast(3)) {
                                    z = false;
                                    boolean doAutoShow = z;
                                    boolean isTextEditor = (controlFlags & 2) == 0;
                                    boolean didStart = false;
                                    switch (i2 & 15) {
                                        case 0:
                                            if (isTextEditor) {
                                                if (doAutoShow) {
                                                    if ((i2 & 256) != 0) {
                                                        if (DEBUG_FLOW) {
                                                            Slog.v(TAG, "Unspecified window will show input");
                                                        }
                                                        if (editorInfo != null) {
                                                            didStart = true;
                                                            res = startInputUncheckedLocked(cs2, inputContext, missingMethods, editorInfo, controlFlags, startInputReason);
                                                        }
                                                        showCurrentInputLocked(1, null);
                                                        break;
                                                    }
                                                }
                                            }
                                            if (WindowManager.LayoutParams.mayUseInputMethod(windowFlags)) {
                                                if (DEBUG_FLOW) {
                                                    Slog.v(TAG, "Unspecified window will hide input");
                                                }
                                                hideCurrentInputLocked(2, null);
                                                break;
                                            }
                                            break;
                                        case 1:
                                            break;
                                        case 2:
                                            if ((i2 & 256) != 0) {
                                                if (DEBUG_FLOW) {
                                                    Slog.v(TAG, "Window asks to hide input going forward");
                                                }
                                                hideCurrentInputLocked(0, null);
                                                break;
                                            }
                                            break;
                                        case 3:
                                            if (DEBUG_FLOW) {
                                                Slog.v(TAG, "Window asks to hide input");
                                            }
                                            hideCurrentInputLocked(0, null);
                                            break;
                                        case 4:
                                            if ((i2 & 256) != 0) {
                                                if (DEBUG_FLOW) {
                                                    Slog.v(TAG, "Window asks to show input going forward");
                                                }
                                                if (editorInfo != null) {
                                                    didStart = true;
                                                    res = startInputUncheckedLocked(cs2, inputContext, missingMethods, editorInfo, controlFlags, startInputReason);
                                                }
                                                showCurrentInputLocked(1, null);
                                                break;
                                            }
                                            break;
                                        case 5:
                                            if (DEBUG_FLOW) {
                                                Slog.v(TAG, "Window asks to always show input");
                                            }
                                            if (editorInfo != null) {
                                                resultReceiver = null;
                                                i = 1;
                                                didStart = true;
                                                res = startInputUncheckedLocked(cs2, inputContext, missingMethods, editorInfo, controlFlags, startInputReason);
                                            } else {
                                                resultReceiver = null;
                                                i = 1;
                                            }
                                            showCurrentInputLocked(i, resultReceiver);
                                            break;
                                    }
                                    if (!didStart && editorInfo != null) {
                                        res = startInputUncheckedLocked(cs2, inputContext, missingMethods, editorInfo, controlFlags, startInputReason);
                                    }
                                    Binder.restoreCallingIdentity(ident);
                                    return res;
                                }
                            }
                            z = true;
                            boolean doAutoShow2 = z;
                            boolean isTextEditor2 = (controlFlags & 2) == 0;
                            boolean didStart2 = false;
                            switch (i2 & 15) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    break;
                                case 5:
                                    break;
                            }
                            res = startInputUncheckedLocked(cs2, inputContext, missingMethods, editorInfo, controlFlags, startInputReason);
                            Binder.restoreCallingIdentity(ident);
                            return res;
                        }
                    } else {
                        hashMap = hashMap2;
                        boolean z6 = calledFromValidUser;
                        ClientState clientState3 = cs;
                        throw new IllegalArgumentException("unknown client " + client.asBinder());
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        } catch (Throwable th4) {
            th = th4;
            boolean z7 = calledFromValidUser;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) {
    }

    public void setInputMethod(IBinder token, String id) {
    }

    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
    }

    public void showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient client, String inputMethodId) {
    }

    public boolean isInputMethodPickerShownForTest() {
        return false;
    }

    public boolean switchToNextInputMethod(IBinder token, boolean onlyCurrentIme) {
        return false;
    }

    public boolean shouldOfferSwitchingToNextInputMethod(IBinder token) {
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0053, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0058, code lost:
        return null;
     */
    public InputMethodSubtype getLastInputMethodSubtype() {
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mMethodMap) {
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
                                InputMethodSubtype subtypeAt = lastImi.getSubtypeAt(lastSubtypeId);
                                return subtypeAt;
                            }
                        }
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
    }

    public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
    }

    public int getInputMethodWindowVisibleHeight() {
        return this.mWindowManagerInternal.getInputMethodWindowVisibleHeight();
    }

    public void clearLastInputMethodWindowForTransition(IBinder token) {
        if (calledFromValidUser()) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mMethodMap) {
                    if (!calledWithValidToken(token)) {
                        int uid = Binder.getCallingUid();
                        Slog.e(TAG, "Ignoring clearLastInputMethodWindowForTransition due to an invalid token. uid:" + uid + " token:" + token);
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    this.mWindowManagerInternal.clearLastInputMethodWindowForTransition();
                    Binder.restoreCallingIdentity(ident);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }
    }

    public void notifyUserAction(int sequenceNumber) {
    }

    public void hideMySoftInput(IBinder token, int flags) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (!calledWithValidToken(token)) {
                    int uid = Binder.getCallingUid();
                    Slog.e(TAG, "Ignoring hideInputMethod due to an invalid token. uid:" + uid + " token:" + token);
                    return;
                }
                if (DEBUG_FLOW != 0) {
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
    }

    public void showMySoftInput(IBinder token, int flags) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (!calledWithValidToken(token)) {
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
    }

    /* access modifiers changed from: package-private */
    public void setEnabledSessionInMainThread(SessionState session) {
        if (this.mEnabledSession != session) {
            if (!(this.mEnabledSession == null || this.mEnabledSession.session == null)) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, false);
                } catch (RemoteException e) {
                }
            }
            this.mEnabledSession = session;
            if (this.mEnabledSession != null && this.mEnabledSession.session != null) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, true);
                } catch (RemoteException e2) {
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00a1, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x00a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a3, code lost:
        r3.channel.dispose();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00cc, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x00a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x013c, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0153, code lost:
        if (android.os.Binder.isProxy(r1) != false) goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0155, code lost:
        r3.dispose();
     */
    public boolean handleMessage(Message msg) {
        boolean z = false;
        switch (msg.what) {
            case 1000:
                try {
                    ((IInputMethod) msg.obj).unbindInput();
                } catch (RemoteException e) {
                }
                return true;
            case 1010:
                SomeArgs args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).bindInput((InputBinding) args.arg2);
                } catch (RemoteException e2) {
                }
                args.recycle();
                return true;
            case 1020:
                SomeArgs args2 = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args2.arg1).showSoftInput(msg.arg1, (ResultReceiver) args2.arg2);
                } catch (RemoteException e3) {
                }
                args2.recycle();
                return true;
            case 1030:
                SomeArgs args3 = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args3.arg1).hideSoftInput(0, (ResultReceiver) args3.arg2);
                } catch (RemoteException e4) {
                }
                args3.recycle();
                return true;
            case MSG_ATTACH_TOKEN /*1040*/:
                SomeArgs args4 = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args4.arg1).attachToken((IBinder) args4.arg2);
                } catch (RemoteException e5) {
                }
                args4.recycle();
                return true;
            case MSG_CREATE_SESSION /*1050*/:
                SomeArgs args5 = (SomeArgs) msg.obj;
                IInputMethod method = (IInputMethod) args5.arg1;
                InputChannel channel = (InputChannel) args5.arg2;
                try {
                    method.createSession(channel, (IInputSessionCallback) args5.arg3);
                    if (channel != null) {
                        break;
                    }
                } catch (RemoteException e6) {
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
                    session.method.startInput(startInputToken, inputContext, missingMethods, editorInfo, restarting);
                } catch (RemoteException e7) {
                }
                args6.recycle();
                return true;
            case 3000:
                try {
                    ((IInputMethodClient) msg.obj).onUnbindMethod(msg.arg1, msg.arg2);
                } catch (RemoteException e8) {
                }
                return true;
            case MSG_BIND_CLIENT /*3010*/:
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
            case MSG_SET_ACTIVE /*3020*/:
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
            case MSG_SET_INTERACTIVE /*3030*/:
                if (msg.arg1 != 0) {
                    z = true;
                }
                handleSetInteractive(z);
                return true;
            case MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER /*3040*/:
                try {
                    ((ClientState) msg.obj).client.setUserActionNotificationSequenceNumber(msg.arg1);
                } catch (RemoteException e11) {
                    Slog.w(TAG, "Got RemoteException sending setUserActionNotificationSequenceNumber(" + sequenceNumber + ") notification to pid " + clientState.pid + " uid " + clientState.uid);
                }
                return true;
            default:
                return false;
        }
    }

    private void handleSetInteractive(boolean interactive) {
        synchronized (this.mMethodMap) {
            this.mIsInteractive = interactive;
            updateSystemUiLocked(this.mCurToken, interactive ? this.mImeWindowVis : 0, this.mBackDisposition);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(MSG_SET_ACTIVE, this.mIsInteractive ? 1 : 0, this.mInFullscreenMode ? 1 : 0, this.mCurClient));
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
            new ComponentName(si.packageName, si.name);
            if (!"android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                Slog.w(TAG, "Skipping input method " + compName + ": it does not require the permission " + "android.permission.BIND_INPUT_METHOD");
            } else if (shouldBuildInputMethodList(si.packageName)) {
                try {
                    InputMethodInfo p = new InputMethodInfo(this.mContext, ri);
                    this.mMethodList.add(p);
                    this.mMethodMap.put(p.getId(), p);
                } catch (IOException | XmlPullParserException e) {
                    Slog.w(TAG, "Unable to load input method " + compName, e);
                }
            }
        }
        updateSecureIMEStatus();
    }

    private Pair<InputMethodInfo, InputMethodSubtype> findLastResortApplicableShortcutInputMethodAndSubtypeLocked(String mode) {
        for (InputMethodInfo imi : this.mSettings.getEnabledInputMethodListLocked()) {
            InputMethodUtils.getOverridingImplicitlyEnabledSubtypes(imi, mode);
        }
        return null;
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0024, code lost:
        return r1;
     */
    public List getShortcutInputMethodsAndSubtypes() {
        synchronized (this.mMethodMap) {
            ArrayList<Object> ret = new ArrayList<>();
            if (this.mShortcutInputMethodsAndSubtypes.size() != 0) {
                return ret;
            }
            Pair<InputMethodInfo, InputMethodSubtype> info = findLastResortApplicableShortcutInputMethodAndSubtypeLocked("voice");
            if (info != null) {
                ret.add(info.first);
                ret.add(info.second);
            }
        }
    }

    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        return false;
    }

    private static String imeWindowStatusToString(int imeWindowVis) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if ((imeWindowVis & 1) != 0) {
            sb.append("Active");
            first = false;
        }
        if ((imeWindowVis & 2) != 0) {
            if (!first) {
                sb.append("|");
            }
            sb.append("Visible");
        }
        return sb.toString();
    }

    public IInputContentUriToken createInputContentUriToken(IBinder token, Uri contentUri, String packageName) {
        if (!calledFromValidUser()) {
            return null;
        }
        if (token == null) {
            throw new NullPointerException("token");
        } else if (packageName == null) {
            throw new NullPointerException("packageName");
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
                    InputContentUriTokenHandler inputContentUriTokenHandler = new InputContentUriTokenHandler(ContentProvider.getUriWithoutUserId(contentUri), uid, packageName, ContentProvider.getUserIdFromUri(contentUri, imeUserId), appUserId);
                    return inputContentUriTokenHandler;
                }
            }
        } else {
            throw new InvalidParameterException("contentUri must have content scheme");
        }
    }

    public boolean switchToPreviousInputMethod(IBinder token) {
        return false;
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
        Printer p = new PrintWriterPrinter(pw);
        synchronized (this.mMethodMap) {
            p.println("  Clients:");
            for (ClientState ci : this.mClients.values()) {
                p.println("  Client " + ci + ":");
                StringBuilder sb = new StringBuilder();
                sb.append("    client=");
                sb.append(ci.client);
                p.println(sb.toString());
                p.println("    inputContext=" + ci.inputContext);
                p.println("    sessionRequested=" + ci.sessionRequested);
                p.println("    curSession=" + ci.curSession);
            }
            p.println("  mCurMethodId=" + this.mCurMethodId);
            client = this.mCurClient;
            p.println("  mCurClient=" + client + " mCurSeq=" + this.mCurSeq);
            p.println("  mCurFocusedWindow=" + this.mCurFocusedWindow + " softInputMode=" + InputMethodClient.softInputModeToString(this.mCurFocusedWindowSoftInputMode) + " client=" + this.mCurFocusedWindowClient);
            focusedWindowClient = this.mCurFocusedWindowClient;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("  mCurFocusedWindowClient=");
            sb2.append(focusedWindowClient);
            p.println(sb2.toString());
            p.println("  mCurId=" + this.mCurId + " mHaveConnect=" + this.mHaveConnection + " mBoundToMethod=" + this.mBoundToMethod);
            StringBuilder sb3 = new StringBuilder();
            sb3.append("  mCurToken=");
            sb3.append(this.mCurToken);
            p.println(sb3.toString());
            p.println("  mCurIntent=" + this.mCurIntent);
            method = this.mCurMethod;
            p.println("  mCurMethod=" + this.mCurMethod);
            p.println("  mEnabledSession=" + this.mEnabledSession);
            p.println("  mImeWindowVis=" + imeWindowStatusToString(this.mImeWindowVis));
            p.println("  mShowRequested=" + this.mShowRequested + " mShowExplicitlyRequested=" + this.mShowExplicitlyRequested + " mShowForced=" + this.mShowForced + " mInputShown=" + this.mInputShown);
            StringBuilder sb4 = new StringBuilder();
            sb4.append("  mInFullscreenMode=");
            sb4.append(this.mInFullscreenMode);
            p.println(sb4.toString());
            StringBuilder sb5 = new StringBuilder();
            sb5.append("  mCurUserActionNotificationSequenceNumber=");
            sb5.append(this.mCurUserActionNotificationSequenceNumber);
            p.println(sb5.toString());
            p.println("  mSystemReady=" + this.mSystemReady + " mInteractive=" + this.mIsInteractive);
            p.println("  mSettings:");
        }
        p.println(" ");
        if (client != null) {
            pw.flush();
            try {
                client.client.asBinder().dump(fd, args);
            } catch (RemoteException e) {
                p.println("Input method client dead: " + e);
            }
        } else {
            p.println("No input method client.");
        }
        if (!(focusedWindowClient == null || client == focusedWindowClient)) {
            p.println(" ");
            p.println("Warning: Current input method client doesn't match the last focused. window.");
            p.println("Dumping input method client in the last focused window just in case.");
            p.println(" ");
            pw.flush();
            try {
                focusedWindowClient.client.asBinder().dump(fd, args);
            } catch (RemoteException e2) {
                p.println("Input method client in focused window dead: " + e2);
            }
        }
        p.println(" ");
        if (method != null) {
            pw.flush();
            try {
                method.asBinder().dump(fd, args);
            } catch (RemoteException e3) {
                p.println("Input method service dead: " + e3);
            }
        } else {
            p.println("No input method service.");
        }
    }

    public IBinder getHwInnerService() {
        return null;
    }
}
