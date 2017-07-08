package com.android.server;

import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import android.os.Handler.Callback;
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
import android.provider.Settings.Secure;
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
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.content.PackageMonitor;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.inputmethod.InputMethodUtils.InputMethodSettings;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;
import com.android.internal.view.IInputSessionCallback.Stub;
import com.android.internal.view.InputBindResult;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class HwSecureInputMethodManagerService extends AbsInputMethodManagerService implements ServiceConnection, Callback {
    static final boolean DEBUG = false;
    static final boolean DEBUG_FLOW;
    static final boolean DEBUG_RESTORE = false;
    static final int MSG_ATTACH_TOKEN = 1040;
    static final int MSG_BIND_CLIENT = 3010;
    static final int MSG_BIND_INPUT = 1010;
    static final int MSG_CREATE_SESSION = 1050;
    static final int MSG_HIDE_SOFT_INPUT = 1030;
    static final int MSG_RESTART_INPUT = 2010;
    static final int MSG_SET_ACTIVE = 3020;
    static final int MSG_SET_INTERACTIVE = 3030;
    static final int MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER = 3040;
    static final int MSG_SHOW_SOFT_INPUT = 1020;
    static final int MSG_START_INPUT = 2000;
    static final int MSG_UNBIND_CLIENT = 3000;
    static final int MSG_UNBIND_INPUT = 1000;
    private static final int NOT_A_SUBTYPE_ID = -1;
    static final int SECURE_SUGGESTION_SPANS_MAX_SIZE = 20;
    public static final String SETTINGS_SECURE_KEYBOARD_CONTROL = "secure_keyboard";
    static final String TAG = "SecInputMethodManagerService";
    static final long TIME_TO_RECONNECT = 3000;
    static final int UNBIND_SECIME_IF_SHOULD = 10000;
    private static final String secImeId = "com.huawei.secime/.SoftKeyboard";
    private final AppOpsManager mAppOpsManager;
    int mBackDisposition;
    boolean mBoundToMethod;
    final HandlerCaller mCaller;
    final HashMap<IBinder, ClientState> mClients;
    final Context mContext;
    EditorInfo mCurAttribute;
    ClientState mCurClient;
    private boolean mCurClientInKeyguard;
    IBinder mCurFocusedWindow;
    ClientState mCurFocusedWindowClient;
    String mCurId;
    IInputContext mCurInputContext;
    int mCurInputContextMissingMethods;
    Intent mCurIntent;
    IInputMethod mCurMethod;
    String mCurMethodId;
    int mCurSeq;
    IBinder mCurToken;
    int mCurUserActionNotificationSequenceNumber;
    SessionState mEnabledSession;
    boolean mHaveConnection;
    private final IPackageManager mIPackageManager;
    final IWindowManager mIWindowManager;
    int mImeWindowVis;
    boolean mInputShown;
    boolean mIsInteractive;
    private KeyguardManager mKeyguardManager;
    long mLastBindTime;
    private LocaleList mLastSystemLocales;
    final ArrayList<InputMethodInfo> mMethodList;
    final HashMap<String, InputMethodInfo> mMethodMap;
    private final MyPackageMonitor mMyPackageMonitor;
    final InputBindResult mNoBinding;
    final Resources mRes;
    private SecureSettingsObserver mSecureSettingsObserver;
    private final LruCache<SuggestionSpan, InputMethodInfo> mSecureSuggestionSpans;
    final InputMethodSettings mSettings;
    private final HashMap<InputMethodInfo, ArrayList<InputMethodSubtype>> mShortcutInputMethodsAndSubtypes;
    private boolean mShouldSetActive;
    boolean mShowExplicitlyRequested;
    boolean mShowForced;
    boolean mShowRequested;
    private final String mSlotIme;
    private StatusBarManagerService mStatusBar;
    boolean mSystemReady;
    private int mUnbindCounter;
    private final UserManager mUserManager;
    boolean mVisibleBound;
    final ServiceConnection mVisibleConnection;
    final WindowManagerInternal mWindowManagerInternal;

    static final class ClientState {
        final InputBinding binding;
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
            this.binding = new InputBinding(null, this.inputContext.asBinder(), this.uid, this.pid);
        }
    }

    class ImmsBroadcastReceiver extends BroadcastReceiver {
        ImmsBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_REMOVED".equals(action)) {
                HwSecureInputMethodManagerService.this.updateCurrentProfileIds();
            } else {
                Slog.w(HwSecureInputMethodManagerService.TAG, "Unexpected intent " + intent);
            }
        }
    }

    private final class LocalSecureServiceImpl implements HwSecureInputMethodManagerInternal {
        private LocalSecureServiceImpl() {
        }

        public void setClientActiveFlag() {
            HwSecureInputMethodManagerService.this.mShouldSetActive = true;
        }
    }

    private static final class MethodCallback extends Stub {
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

        public void onStart() {
            publishBinderService(HwInputMethodManagerService.SECURITY_INPUT_SERVICE_NAME, this.mService);
        }

        public void onSwitchUser(int userHandle) {
            this.mService.onSwitchUser(userHandle);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemRunning((StatusBarManagerService) ServiceManager.getService("statusbar"));
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
            return getChangingUserId() == HwSecureInputMethodManagerService.this.mSettings.getCurrentUserId() ? true : HwSecureInputMethodManagerService.DEBUG_RESTORE;
        }

        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            return HwSecureInputMethodManagerService.DEBUG_RESTORE;
        }

        public void onSomePackagesChanged() {
            if (isChangingPackagesOfCurrentUser()) {
                synchronized (HwSecureInputMethodManagerService.this.mMethodMap) {
                    HwSecureInputMethodManagerService.this.buildInputMethodListLocked(HwSecureInputMethodManagerService.DEBUG_RESTORE);
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
            return HwSecureInputMethodManagerService.DEBUG_RESTORE;
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
        boolean mRegistered;
        int mUserId;

        public SecureSettingsObserver() {
            super(new Handler());
            this.mRegistered = HwSecureInputMethodManagerService.DEBUG_RESTORE;
        }

        public void registerContentObserverInner(int userId) {
            Slog.d(HwSecureInputMethodManagerService.TAG, "SecureSettingsObserver mRegistered=" + this.mRegistered + " new user=" + userId + " current user=" + this.mUserId);
            if (!this.mRegistered || this.mUserId != userId) {
                ContentResolver resolver = HwSecureInputMethodManagerService.this.mContext.getContentResolver();
                if (this.mRegistered) {
                    resolver.unregisterContentObserver(this);
                    this.mRegistered = HwSecureInputMethodManagerService.DEBUG_RESTORE;
                }
                this.mUserId = userId;
                resolver.registerContentObserver(Secure.getUriFor(HwSecureInputMethodManagerService.SETTINGS_SECURE_KEYBOARD_CONTROL), HwSecureInputMethodManagerService.DEBUG_RESTORE, this, userId);
                this.mRegistered = true;
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                Slog.i(HwSecureInputMethodManagerService.TAG, "SecureSettingsObserver onChange, uri = " + uri.toString());
                if (Secure.getUriFor(HwSecureInputMethodManagerService.SETTINGS_SECURE_KEYBOARD_CONTROL).equals(uri) && !HwSecureInputMethodManagerService.this.isSecureIMEEnable()) {
                    synchronized (HwSecureInputMethodManagerService.this.mMethodMap) {
                        HwSecureInputMethodManagerService.this.hideCurrentInputLocked(0, null);
                        HwSecureInputMethodManagerService.this.mCurMethodId = null;
                        HwSecureInputMethodManagerService.this.unbindCurrentMethodLocked(HwSecureInputMethodManagerService.DEBUG_RESTORE);
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

    static {
        DEBUG_FLOW = Log.HWINFO;
    }

    protected boolean isSecureIME(String packageName) {
        if (HwInputMethodManagerService.SECURE_IME_PACKAGENAME.equals(packageName)) {
            return true;
        }
        return DEBUG_RESTORE;
    }

    protected boolean shouldBuildInputMethodList(String packageName) {
        if (HwInputMethodManagerService.SECURE_IME_PACKAGENAME.equals(packageName)) {
            return true;
        }
        return DEBUG_RESTORE;
    }

    private boolean isSecureIMEEnable() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), SETTINGS_SECURE_KEYBOARD_CONTROL, 1, this.mSettings.getCurrentUserId()) == 1 ? true : DEBUG_RESTORE;
    }

    private boolean setClientActiveIfShould() {
        if (!this.mShouldSetActive || !this.mIsInteractive) {
            return DEBUG_RESTORE;
        }
        synchronized (this.mMethodMap) {
            if (this.mCurClient == null || this.mCurClient.client == null) {
                return DEBUG_RESTORE;
            }
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, 1, this.mCurClient));
            this.mShouldSetActive = DEBUG_RESTORE;
            return true;
        }
    }

    protected void switchUserExtra(int userId) {
        if (this.mSecureSettingsObserver != null) {
            this.mSecureSettingsObserver.registerContentObserverInner(userId);
        }
        if (!isSecureIMEEnable()) {
            hideCurrentInputLocked(0, null);
            this.mCurMethodId = null;
            unbindCurrentMethodLocked(DEBUG_RESTORE);
        }
    }

    void onUnlockUser(int userId) {
        boolean z = DEBUG_RESTORE;
        synchronized (this.mMethodMap) {
            int currentUserId = this.mSettings.getCurrentUserId();
            if (userId != currentUserId) {
                return;
            }
            InputMethodSettings inputMethodSettings = this.mSettings;
            if (!this.mSystemReady) {
                z = true;
            }
            inputMethodSettings.switchCurrentUser(currentUserId, z);
            buildInputMethodListLocked(DEBUG_RESTORE);
        }
    }

    void onSwitchUser(int userId) {
        synchronized (this.mMethodMap) {
            switchUserLocked(userId);
        }
    }

    public HwSecureInputMethodManagerService(Context context) {
        boolean z = DEBUG_RESTORE;
        this.mNoBinding = new InputBindResult(null, null, null, NOT_A_SUBTYPE_ID, NOT_A_SUBTYPE_ID);
        this.mMethodList = new ArrayList();
        this.mMethodMap = new HashMap();
        this.mSecureSuggestionSpans = new LruCache(SECURE_SUGGESTION_SPANS_MAX_SIZE);
        this.mVisibleConnection = new MySerServiceConnection();
        this.mVisibleBound = DEBUG_RESTORE;
        this.mClients = new HashMap();
        this.mSystemReady = DEBUG_RESTORE;
        this.mShortcutInputMethodsAndSubtypes = new HashMap();
        this.mIsInteractive = true;
        this.mCurUserActionNotificationSequenceNumber = 0;
        this.mBackDisposition = 0;
        this.mMyPackageMonitor = new MyPackageMonitor();
        this.mUnbindCounter = 0;
        this.mIPackageManager = AppGlobals.getPackageManager();
        this.mContext = context;
        this.mRes = context.getResources();
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mCaller = new HandlerCaller(context, null, new HandlerCaller.Callback() {
            public void executeMessage(Message msg) {
                HwSecureInputMethodManagerService.this.handleMessage(msg);
            }
        }, true);
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mSlotIme = this.mContext.getString(17039389);
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
        Resources resources = this.mRes;
        ContentResolver contentResolver = context.getContentResolver();
        HashMap hashMap = this.mMethodMap;
        ArrayList arrayList = this.mMethodList;
        if (!this.mSystemReady) {
            z = true;
        }
        this.mSettings = new InputMethodSettings(resources, contentResolver, hashMap, arrayList, userId, z);
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
            if (!(updateOnlyWhenLocaleChanged && (newLocales == null || newLocales.equals(this.mLastSystemLocales)))) {
                if (!updateOnlyWhenLocaleChanged) {
                    hideCurrentInputLocked(0, null);
                    resetCurrentMethodAndClient(6);
                }
                buildInputMethodListLocked(resetDefaultEnabledIme);
                this.mLastSystemLocales = newLocales;
            }
        }
    }

    private void resetStateIfCurrentLocaleChangedLocked() {
        resetAllInternalStateLocked(true, true);
    }

    private void switchUserLocked(int newUserId) {
        boolean useCopyOnWriteSettings = (this.mSystemReady && this.mUserManager.isUserUnlockingOrUnlocked(newUserId)) ? DEBUG_RESTORE : true;
        this.mSettings.switchCurrentUser(newUserId, useCopyOnWriteSettings);
        updateCurrentProfileIds();
        resetAllInternalStateLocked(DEBUG_RESTORE, DEBUG_RESTORE);
        switchUserExtra(newUserId);
    }

    void updateCurrentProfileIds() {
        this.mSettings.setCurrentProfileIds(this.mUserManager.getProfileIdsWithDisabled(this.mSettings.getCurrentUserId()));
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Input Method Manager Crash", e);
            }
            throw e;
        }
    }

    public void systemRunning(StatusBarManagerService statusBar) {
        boolean z = DEBUG_RESTORE;
        this.mSecureSettingsObserver = new SecureSettingsObserver();
        this.mSecureSettingsObserver.registerContentObserverInner(this.mSettings.getCurrentUserId());
        if (LocalServices.getService(HwSecureInputMethodManagerInternal.class) == null) {
            LocalServices.addService(HwSecureInputMethodManagerInternal.class, new LocalSecureServiceImpl());
        }
        synchronized (this.mMethodMap) {
            if (!this.mSystemReady) {
                this.mSystemReady = true;
                int currentUserId = this.mSettings.getCurrentUserId();
                InputMethodSettings inputMethodSettings = this.mSettings;
                if (!this.mUserManager.isUserUnlockingOrUnlocked(currentUserId)) {
                    z = true;
                }
                inputMethodSettings.switchCurrentUser(currentUserId, z);
                this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
                this.mStatusBar = statusBar;
                if (this.mStatusBar != null) {
                    this.mStatusBar.setIconVisibility(this.mSlotIme, DEBUG_RESTORE);
                }
                updateSystemUiLocked(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                buildInputMethodListLocked(true);
                this.mLastSystemLocales = this.mRes.getConfiguration().getLocales();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean calledFromValidUser() {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (uid == MSG_UNBIND_INPUT || this.mSettings.isCurrentProfile(userId) || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            return true;
        }
        Slog.w(TAG, "--- IPC called from background users. Ignore. callers=" + Debug.getCallers(10));
        return DEBUG_RESTORE;
    }

    private boolean calledWithValidToken(IBinder token) {
        if (token == null || this.mCurToken != token) {
            return DEBUG_RESTORE;
        }
        return true;
    }

    private boolean bindCurrentInputMethodService(Intent service, ServiceConnection conn, int flags) {
        if (service != null && conn != null) {
            return this.mContext.bindServiceAsUser(service, conn, flags, new UserHandle(this.mSettings.getCurrentUserId()));
        }
        Slog.e(TAG, "--- bind failed: service = " + service + ", conn = " + conn);
        return DEBUG_RESTORE;
    }

    public List<InputMethodInfo> getInputMethodList() {
        if (!calledFromValidUser()) {
            return Collections.emptyList();
        }
        List arrayList;
        synchronized (this.mMethodMap) {
            arrayList = new ArrayList(this.mMethodList);
        }
        return arrayList;
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
                ClientState cs = (ClientState) this.mClients.remove(client.asBinder());
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

    void executeOrSendMessage(IInterface target, Message msg) {
        if (target != null) {
            if (target.asBinder() instanceof Binder) {
                this.mCaller.sendMessage(msg);
            } else {
                handleMessage(msg);
                msg.recycle();
            }
        }
    }

    void unbindCurrentClientLocked(int unbindClientReason) {
        if (this.mCurClient != null) {
            if (this.mBoundToMethod) {
                this.mBoundToMethod = DEBUG_RESTORE;
                if (this.mCurMethod != null) {
                    executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageO(MSG_UNBIND_INPUT, this.mCurMethod));
                }
            }
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, 0, this.mCurClient));
            executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(MSG_UNBIND_CLIENT, this.mCurSeq, unbindClientReason, this.mCurClient.client));
            this.mCurClient.sessionRequested = DEBUG_RESTORE;
            this.mCurClient = null;
        }
    }

    private int getImeShowFlags() {
        if (this.mShowForced) {
            return 3;
        }
        if (this.mShowExplicitlyRequested) {
            return 1;
        }
        return 0;
    }

    private int getAppShowFlags() {
        if (this.mShowForced) {
            return 2;
        }
        if (this.mShowExplicitlyRequested) {
            return 0;
        }
        return 1;
    }

    InputBindResult attachNewInputLocked(boolean initial) {
        InputChannel inputChannel = null;
        if (!this.mBoundToMethod) {
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_BIND_INPUT, this.mCurMethod, this.mCurClient.binding));
            this.mBoundToMethod = true;
        }
        SessionState session = this.mCurClient.curSession;
        if (session != null) {
            if (initial) {
                executeOrSendMessage(session.method, this.mCaller.obtainMessageIOOO(MSG_START_INPUT, this.mCurInputContextMissingMethods, session, this.mCurInputContext, this.mCurAttribute));
            } else {
                executeOrSendMessage(session.method, this.mCaller.obtainMessageIOOO(MSG_RESTART_INPUT, this.mCurInputContextMissingMethods, session, this.mCurInputContext, this.mCurAttribute));
            }
        }
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
        return new InputBindResult(iInputMethodSession, inputChannel, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
    }

    InputBindResult startInputLocked(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        ClientState cs = (ClientState) this.mClients.get(client.asBinder());
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
            return startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
        }
    }

    InputBindResult startInputUncheckedLocked(ClientState cs, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (this.mCurMethodId == null) {
            return this.mNoBinding;
        }
        if (InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, cs.uid, attribute.packageName)) {
            if (this.mCurClient != cs) {
                this.mCurClientInKeyguard = isKeyguardLocked();
                unbindCurrentClientLocked(1);
                if (this.mIsInteractive) {
                    executeOrSendMessage(cs.client, this.mCaller.obtainMessageIO(MSG_SET_ACTIVE, this.mIsInteractive ? 1 : 0, cs));
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
            if ((HwGlobalActionsData.FLAG_REBOOT & controlFlags) != 0) {
                this.mShowRequested = true;
            }
            if (this.mCurId != null && this.mCurId.equals(this.mCurMethodId)) {
                if (cs.curSession != null) {
                    return attachNewInputLocked((controlFlags & HwGlobalActionsData.FLAG_SILENTMODE_SILENT) != 0 ? true : DEBUG_RESTORE);
                } else if (this.mHaveConnection) {
                    if (this.mCurMethod != null) {
                        requestClientSessionLocked(cs);
                        return new InputBindResult(null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    } else if (SystemClock.uptimeMillis() < this.mLastBindTime + TIME_TO_RECONNECT) {
                        return new InputBindResult(null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
                    } else {
                        EventLog.writeEvent(32000, new Object[]{this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), Integer.valueOf(0)});
                    }
                }
            }
            try {
                return startInputInnerLocked();
            } catch (RuntimeException e) {
                Slog.w(TAG, "Unexpected exception", e);
                return null;
            }
        }
        Slog.e(TAG, "Rejecting this client as it reported an invalid package name. uid=" + cs.uid + " package=" + attribute.packageName);
        return this.mNoBinding;
    }

    InputBindResult startInputInnerLocked() {
        if (this.mCurMethodId == null || !this.mMethodMap.containsKey(this.mCurMethodId)) {
            return this.mNoBinding;
        }
        if (this.mSystemReady) {
            InputMethodInfo info = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
            if (info == null) {
                Slog.w(TAG, "info == null id: " + this.mCurMethodId);
                return this.mNoBinding;
            }
            unbindCurrentMethodLocked(true);
            this.mCurIntent = new Intent("android.view.InputMethod");
            this.mCurIntent.setComponent(info.getComponent());
            this.mCurIntent.putExtra("android.intent.extra.client_label", 17040471);
            this.mCurIntent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.INPUT_METHOD_SETTINGS"), 0));
            if (bindCurrentInputMethodService(this.mCurIntent, this, 1610612741)) {
                this.mLastBindTime = SystemClock.uptimeMillis();
                this.mHaveConnection = true;
                this.mCurId = info.getId();
                this.mCurToken = new Binder();
                try {
                    Slog.v(TAG, "Adding window token: " + this.mCurToken);
                    this.mIWindowManager.addWindowToken(this.mCurToken, 2011);
                } catch (RemoteException e) {
                }
                return new InputBindResult(null, null, this.mCurId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
            }
            this.mCurIntent = null;
            Slog.w(TAG, "Failure connecting to input method service: " + this.mCurIntent);
            return null;
        }
        return new InputBindResult(null, null, this.mCurMethodId, this.mCurSeq, this.mCurUserActionNotificationSequenceNumber);
    }

    private InputBindResult startInput(int startInputReason, IInputMethodClient client, IInputContext inputContext, int missingMethods, EditorInfo attribute, int controlFlags) {
        if (!calledFromValidUser()) {
            return null;
        }
        InputBindResult startInputLocked;
        synchronized (this.mMethodMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                startInputLocked = startInputLocked(startInputReason, client, inputContext, missingMethods, attribute, controlFlags);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return startInputLocked;
    }

    public void finishInput(IInputMethodClient client) {
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mMethodMap) {
            if (this.mCurIntent != null && name.equals(this.mCurIntent.getComponent())) {
                this.mCurMethod = IInputMethod.Stub.asInterface(service);
                if (this.mCurToken == null) {
                    Slog.w(TAG, "Service connected without a token!");
                    unbindCurrentMethodLocked(DEBUG_RESTORE);
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

    void onSessionCreated(IInputMethod method, IInputMethodSession session, InputChannel channel) {
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
            InputBindResult res = attachNewInputLocked(true);
            if (res == null) {
                return;
            }
            if (res.method != null) {
                executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageOO(MSG_BIND_CLIENT, this.mCurClient.client, res));
            }
        }
    }

    void unbindCurrentMethodLocked(boolean savePosition) {
        if (this.mVisibleBound) {
            this.mContext.unbindService(this.mVisibleConnection);
            this.mVisibleBound = DEBUG_RESTORE;
        }
        if (this.mHaveConnection) {
            this.mContext.unbindService(this);
            this.mHaveConnection = DEBUG_RESTORE;
        }
        if (this.mCurToken != null) {
            try {
                if ((this.mImeWindowVis & 1) != 0 && savePosition) {
                    this.mWindowManagerInternal.saveLastInputMethodWindowForTransition();
                }
                this.mIWindowManager.removeWindowToken(this.mCurToken);
            } catch (RemoteException e) {
            }
            this.mCurToken = null;
        }
        this.mCurId = null;
        clearCurMethodLocked();
    }

    void resetCurrentMethodAndClient(int unbindClientReason) {
        this.mCurMethodId = null;
        unbindCurrentMethodLocked(DEBUG_RESTORE);
        unbindCurrentClientLocked(unbindClientReason);
    }

    void requestClientSessionLocked(ClientState cs) {
        if (!cs.sessionRequested) {
            InputChannel[] channels = InputChannel.openInputChannelPair(cs.toString());
            cs.sessionRequested = true;
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOOO(MSG_CREATE_SESSION, this.mCurMethod, channels[1], new MethodCallback(this, this.mCurMethod, channels[0])));
        }
    }

    void clearClientSessionLocked(ClientState cs) {
        finishSessionLocked(cs.curSession);
        cs.curSession = null;
        cs.sessionRequested = DEBUG_RESTORE;
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

    void clearCurMethodLocked() {
        if (this.mCurMethod != null) {
            for (ClientState cs : this.mClients.values()) {
                clearClientSessionLocked(cs);
            }
            finishSessionLocked(this.mEnabledSession);
            this.mEnabledSession = null;
            this.mCurMethod = null;
        }
        if (this.mStatusBar != null) {
            this.mStatusBar.setIconVisibility(this.mSlotIme, DEBUG_RESTORE);
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        synchronized (this.mMethodMap) {
            if (!(this.mCurMethod == null || this.mCurIntent == null || !name.equals(this.mCurIntent.getComponent()))) {
                clearCurMethodLocked();
                this.mLastBindTime = SystemClock.uptimeMillis();
                this.mShowRequested = this.mInputShown;
                this.mInputShown = DEBUG_RESTORE;
                if (this.mCurClient != null) {
                    executeOrSendMessage(this.mCurClient.client, this.mCaller.obtainMessageIIO(MSG_UNBIND_CLIENT, 3, this.mCurSeq, this.mCurClient.client));
                }
            }
        }
    }

    public void updateStatusIcon(IBinder token, String packageName, int iconId) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    if (iconId == 0) {
                        if (this.mStatusBar != null) {
                            this.mStatusBar.setIconVisibility(this.mSlotIme, DEBUG_RESTORE);
                        }
                    } else if (packageName != null) {
                        CharSequence contentDescription = null;
                        try {
                            contentDescription = this.mContext.getPackageManager().getApplicationLabel(this.mIPackageManager.getApplicationInfo(packageName, 0, this.mSettings.getCurrentUserId()));
                        } catch (RemoteException e) {
                        }
                        if (this.mStatusBar != null) {
                            this.mStatusBar.setIcon(this.mSlotIme, packageName, iconId, 0, contentDescription != null ? contentDescription.toString() : null);
                            this.mStatusBar.setIconVisibility(this.mSlotIme, true);
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                Slog.e(TAG, "Ignoring updateStatusIcon due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean shouldShowImeSwitcherLocked(int visibility) {
        return DEBUG_RESTORE;
    }

    private boolean isKeyguardLocked() {
        return this.mKeyguardManager != null ? this.mKeyguardManager.isKeyguardLocked() : DEBUG_RESTORE;
    }

    private boolean isScreenLocked() {
        if (this.mKeyguardManager == null || !this.mKeyguardManager.isKeyguardLocked()) {
            return DEBUG_RESTORE;
        }
        return this.mKeyguardManager.isKeyguardSecure();
    }

    public void setImeWindowStatus(IBinder token, int vis, int backDisposition) {
        if (calledWithValidToken(token)) {
            synchronized (this.mMethodMap) {
                this.mImeWindowVis = vis;
                this.mBackDisposition = backDisposition;
                updateSystemUiLocked(token, vis, backDisposition);
            }
            return;
        }
        Slog.e(TAG, "Ignoring setImeWindowStatus due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
    }

    private void updateSystemUi(IBinder token, int vis, int backDisposition) {
        synchronized (this.mMethodMap) {
            updateSystemUiLocked(token, vis, backDisposition);
        }
    }

    private void updateSystemUiLocked(IBinder token, int vis, int backDisposition) {
        if (calledWithValidToken(token)) {
            long ident = Binder.clearCallingIdentity();
            if (vis != 0) {
                try {
                    if (isKeyguardLocked() && !this.mCurClientInKeyguard) {
                        vis = 0;
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
            boolean needsToShowImeSwitcher = shouldShowImeSwitcherLocked(vis);
            if (this.mStatusBar != null) {
                this.mStatusBar.setImeWindowStatus(token, vis, backDisposition, needsToShowImeSwitcher);
            }
            Binder.restoreCallingIdentity(ident);
            return;
        }
        Slog.e(TAG, "Ignoring updateSystemUiLocked due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
    }

    public void registerSuggestionSpansForNotification(SuggestionSpan[] spans) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                InputMethodInfo currentImi = (InputMethodInfo) this.mMethodMap.get(this.mCurMethodId);
                for (SuggestionSpan ss : spans) {
                    if (!TextUtils.isEmpty(ss.getNotificationTargetClassName())) {
                        this.mSecureSuggestionSpans.put(ss, currentImi);
                    }
                }
            }
        }
    }

    public boolean notifySuggestionPicked(SuggestionSpan span, String originalString, int index) {
        return DEBUG_RESTORE;
    }

    public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return DEBUG_RESTORE;
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                boolean showCurrentInputLocked;
                if (!(this.mCurClient == null || client == null)) {
                    if (this.mCurClient.client.asBinder() != client.asBinder()) {
                    }
                    if (DEBUG_FLOW) {
                        Slog.v(TAG, "Client requesting input be shown, requestedUid=" + uid);
                    }
                    showCurrentInputLocked = showCurrentInputLocked(flags, resultReceiver);
                    return showCurrentInputLocked;
                }
                if (client != null) {
                    try {
                        if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                            Slog.w(TAG, "Ignoring showSoftInput of uid " + uid + ": " + client);
                            Binder.restoreCallingIdentity(ident);
                            return DEBUG_RESTORE;
                        }
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(ident);
                        return DEBUG_RESTORE;
                    }
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Client requesting input be shown, requestedUid=" + uid);
                }
                showCurrentInputLocked = showCurrentInputLocked(flags, resultReceiver);
                return showCurrentInputLocked;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        this.mShowRequested = true;
        if ((flags & 2) != 0) {
            this.mShowExplicitlyRequested = true;
            this.mShowForced = true;
        } else if ((flags & 1) == 0) {
            this.mShowExplicitlyRequested = true;
        }
        if (!this.mSystemReady) {
            return DEBUG_RESTORE;
        }
        boolean res = DEBUG_RESTORE;
        if (this.mCurMethod != null) {
            if (DEBUG_FLOW) {
                Slog.d(TAG, "showCurrentInputLocked: mCurToken=" + this.mCurToken);
            }
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageIOO(MSG_SHOW_SOFT_INPUT, getImeShowFlags(), this.mCurMethod, resultReceiver));
            this.mInputShown = true;
            if (this.mHaveConnection && !this.mVisibleBound) {
                bindCurrentInputMethodService(this.mCurIntent, this.mVisibleConnection, 201326593);
                this.mVisibleBound = true;
            }
            res = true;
        } else if (this.mHaveConnection && SystemClock.uptimeMillis() >= this.mLastBindTime + TIME_TO_RECONNECT) {
            EventLog.writeEvent(32000, new Object[]{this.mCurMethodId, Long.valueOf(SystemClock.uptimeMillis() - this.mLastBindTime), Integer.valueOf(1)});
            Slog.w(TAG, "Force disconnect/connect to the IME in showCurrentInputLocked()");
            this.mContext.unbindService(this);
            bindCurrentInputMethodService(this.mCurIntent, this, 1073741825);
        }
        return res;
    }

    public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
        if (!calledFromValidUser()) {
            return DEBUG_RESTORE;
        }
        int pid = Binder.getCallingPid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                boolean hideCurrentInputLocked;
                if (!(this.mCurClient == null || client == null)) {
                    if (this.mCurClient.client.asBinder() != client.asBinder()) {
                    }
                    if (DEBUG_FLOW) {
                        Slog.v(TAG, "Client requesting input be hidden, pid=" + pid);
                    }
                    hideCurrentInputLocked = hideCurrentInputLocked(flags, resultReceiver);
                    return hideCurrentInputLocked;
                }
                if (client != null) {
                    try {
                        if (!this.mIWindowManager.inputMethodClientHasFocus(client)) {
                            Binder.restoreCallingIdentity(ident);
                            return DEBUG_RESTORE;
                        }
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(ident);
                        return DEBUG_RESTORE;
                    }
                }
                if (DEBUG_FLOW) {
                    Slog.v(TAG, "Client requesting input be hidden, pid=" + pid);
                }
                hideCurrentInputLocked = hideCurrentInputLocked(flags, resultReceiver);
                return hideCurrentInputLocked;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        boolean shouldHideSoftInput = true;
        if ((flags & 1) != 0 && (this.mShowExplicitlyRequested || this.mShowForced)) {
            return DEBUG_RESTORE;
        }
        if (this.mShowForced && (flags & 2) != 0) {
            return DEBUG_RESTORE;
        }
        boolean res;
        if (this.mCurMethod == null) {
            shouldHideSoftInput = DEBUG_RESTORE;
        } else if (!this.mInputShown && (this.mImeWindowVis & 1) == 0) {
            shouldHideSoftInput = DEBUG_RESTORE;
        }
        if (shouldHideSoftInput) {
            executeOrSendMessage(this.mCurMethod, this.mCaller.obtainMessageOO(MSG_HIDE_SOFT_INPUT, this.mCurMethod, resultReceiver));
            res = true;
        } else {
            res = DEBUG_RESTORE;
        }
        if (!(this.mInputShown || this.mShowRequested)) {
            res = DEBUG_RESTORE;
        }
        if (this.mHaveConnection && this.mVisibleBound) {
            this.mContext.unbindService(this.mVisibleConnection);
            this.mVisibleBound = DEBUG_RESTORE;
        }
        this.mInputShown = DEBUG_RESTORE;
        this.mShowRequested = DEBUG_RESTORE;
        this.mShowExplicitlyRequested = DEBUG_RESTORE;
        this.mShowForced = DEBUG_RESTORE;
        return res;
    }

    public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mMethodMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mCurMethodId == null) {
                    if (isSecureIMEEnable()) {
                        this.mCurMethodId = secImeId;
                        Slog.d(TAG, "startInputOrWindowGainedFocus, mCurMethodId is null, reset");
                        this.mShouldSetActive = true;
                    } else {
                        Slog.d(TAG, "startInputOrWindowGainedFocus, secure ime is disable");
                        Binder.restoreCallingIdentity(ident);
                        return null;
                    }
                }
                if (windowToken == null || startInputReason != UNBIND_SECIME_IF_SHOULD) {
                    this.mUnbindCounter = 0;
                    Binder.restoreCallingIdentity(ident);
                    if (startInputReason == 8) {
                        Slog.i(TAG, "startInputOrWindowGainedFocus, client deactive by imms, set active again to enable secure inputmethod");
                        this.mShouldSetActive = true;
                    }
                    InputBindResult ret = secureImeStartInputOrWindowGainedFocus(startInputReason, client, windowToken, controlFlags, softInputMode, windowFlags, attribute, inputContext, missingMethods);
                    if (!(ret == null || ret == this.mNoBinding)) {
                        setClientActiveIfShould();
                    }
                    return ret;
                }
                if (this.mCurFocusedWindow != windowToken && this.mHaveConnection && this.mUnbindCounter > 0 && !isScreenLocked()) {
                    Slog.d(TAG, "unbind secime");
                    unbindCurrentMethodLocked(true);
                    unbindCurrentClientLocked(UNBIND_SECIME_IF_SHOULD);
                }
                this.mUnbindCounter++;
                this.mCurFocusedWindow = windowToken;
                Binder.restoreCallingIdentity(ident);
                return null;
            } catch (Throwable th) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected InputBindResult windowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int controlFlags, int softInputMode, int windowFlags, EditorInfo attribute, IInputContext inputContext, int missingMethods) {
        boolean calledFromValidUser = calledFromValidUser();
        InputBindResult inputBindResult = null;
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mMethodMap) {
                ClientState cs = (ClientState) this.mClients.get(client.asBinder());
                if (cs != null) {
                    try {
                        if (!this.mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                            Slog.w(TAG, "Focus gain on non-focused client " + cs.client + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
                            return null;
                        }
                    } catch (RemoteException e) {
                    }
                    if (calledFromValidUser) {
                        if (this.mCurFocusedWindow != windowToken) {
                            this.mCurFocusedWindow = windowToken;
                            this.mCurFocusedWindowClient = cs;
                            boolean isLayoutSizeAtLeast;
                            if ((softInputMode & 240) != 16) {
                                isLayoutSizeAtLeast = this.mRes.getConfiguration().isLayoutSizeAtLeast(3);
                            } else {
                                isLayoutSizeAtLeast = true;
                            }
                            boolean isTextEditor = (controlFlags & 2) != 0 ? true : DEBUG_RESTORE;
                            boolean didStart = DEBUG_RESTORE;
                            switch (softInputMode & 15) {
                                case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                                    if (!isTextEditor || !r10) {
                                        if (LayoutParams.mayUseInputMethod(windowFlags)) {
                                            if (DEBUG_FLOW) {
                                                Slog.v(TAG, "Unspecified window will hide input");
                                            }
                                            hideCurrentInputLocked(2, null);
                                            break;
                                        }
                                    } else if ((softInputMode & HwGlobalActionsData.FLAG_SILENTMODE_SILENT) != 0) {
                                        if (DEBUG_FLOW) {
                                            Slog.v(TAG, "Unspecified window will show input");
                                        }
                                        if (attribute != null) {
                                            inputBindResult = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                                            didStart = true;
                                        }
                                        showCurrentInputLocked(1, null);
                                        break;
                                    }
                                    break;
                                case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                                    if ((softInputMode & HwGlobalActionsData.FLAG_SILENTMODE_SILENT) != 0) {
                                        if (DEBUG_FLOW) {
                                            Slog.v(TAG, "Window asks to hide input going forward");
                                        }
                                        hideCurrentInputLocked(0, null);
                                        break;
                                    }
                                    break;
                                case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                                    if (DEBUG_FLOW) {
                                        Slog.v(TAG, "Window asks to hide input");
                                    }
                                    hideCurrentInputLocked(0, null);
                                    break;
                                case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                                    if ((softInputMode & HwGlobalActionsData.FLAG_SILENTMODE_SILENT) != 0) {
                                        if (DEBUG_FLOW) {
                                            Slog.v(TAG, "Window asks to show input going forward");
                                        }
                                        if (attribute != null) {
                                            inputBindResult = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                                            didStart = true;
                                        }
                                        showCurrentInputLocked(1, null);
                                        break;
                                    }
                                    break;
                                case LifeCycleStateMachine.LOGOUT /*5*/:
                                    if (DEBUG_FLOW) {
                                        Slog.v(TAG, "Window asks to always show input");
                                    }
                                    if (attribute != null) {
                                        inputBindResult = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                                        didStart = true;
                                    }
                                    showCurrentInputLocked(1, null);
                                    break;
                            }
                        }
                        Slog.w(TAG, "Window already focused, ignoring focus gain of: " + client + " attribute=" + attribute + ", token = " + windowToken);
                        if (attribute != null) {
                            InputBindResult startInputUncheckedLocked = startInputUncheckedLocked(cs, inputContext, missingMethods, attribute, controlFlags);
                            Binder.restoreCallingIdentity(ident);
                            return startInputUncheckedLocked;
                        }
                        if (this.mInputShown) {
                            Slog.i(TAG, "Window already focused, force refreshime kgon:" + isKeyguardLocked());
                            updateSystemUi(this.mCurToken, this.mImeWindowVis, this.mBackDisposition);
                        }
                        Binder.restoreCallingIdentity(ident);
                        return null;
                    }
                    Slog.w(TAG, "A background user is requesting window. Hiding IME.");
                    Slog.w(TAG, "If you want to interect with IME, you need android.permission.INTERACT_ACROSS_USERS_FULL");
                    hideCurrentInputLocked(0, null);
                    Binder.restoreCallingIdentity(ident);
                    return null;
                }
                throw new IllegalArgumentException("unknown client " + client.asBinder());
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
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

    public boolean switchToLastInputMethod(IBinder token) {
        return DEBUG_RESTORE;
    }

    public boolean switchToNextInputMethod(IBinder token, boolean onlyCurrentIme) {
        return DEBUG_RESTORE;
    }

    public boolean shouldOfferSwitchingToNextInputMethod(IBinder token) {
        return DEBUG_RESTORE;
    }

    public InputMethodSubtype getLastInputMethodSubtype() {
        if (!calledFromValidUser()) {
            return null;
        }
        synchronized (this.mMethodMap) {
            Pair<String, String> lastIme = this.mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme == null || TextUtils.isEmpty((CharSequence) lastIme.first) || TextUtils.isEmpty((CharSequence) lastIme.second)) {
                return null;
            }
            InputMethodInfo lastImi = (InputMethodInfo) this.mMethodMap.get(lastIme.first);
            if (lastImi == null) {
                return null;
            }
            try {
                int lastSubtypeId = InputMethodUtils.getSubtypeIdFromHashCode(lastImi, Integer.parseInt((String) lastIme.second));
                if (lastSubtypeId < 0 || lastSubtypeId >= lastImi.getSubtypeCount()) {
                    return null;
                }
                InputMethodSubtype subtypeAt = lastImi.getSubtypeAt(lastSubtypeId);
                return subtypeAt;
            } catch (NumberFormatException e) {
                return null;
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
                    if (calledWithValidToken(token)) {
                        this.mWindowManagerInternal.clearLastInputMethodWindowForTransition();
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    Slog.e(TAG, "Ignoring clearLastInputMethodWindowForTransition due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void notifyUserAction(int sequenceNumber) {
    }

    public void hideMySoftInput(IBinder token, int flags) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    if (DEBUG_FLOW) {
                        Slog.v(TAG, "hideMySoftInput, pid=" + Binder.getCallingPid() + ", token=" + token);
                    }
                    long ident = Binder.clearCallingIdentity();
                    try {
                        hideCurrentInputLocked(flags, null);
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    Slog.e(TAG, "Ignoring hideInputMethod due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
                }
            }
        }
    }

    public void showMySoftInput(IBinder token, int flags) {
        if (calledFromValidUser()) {
            synchronized (this.mMethodMap) {
                if (calledWithValidToken(token)) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        showCurrentInputLocked(flags, null);
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    Slog.e(TAG, "Ignoring showMySoftInput due to an invalid token. uid:" + Binder.getCallingUid() + " token:" + token);
                }
            }
        }
    }

    void setEnabledSessionInMainThread(SessionState session) {
        if (this.mEnabledSession != session) {
            if (!(this.mEnabledSession == null || this.mEnabledSession.session == null)) {
                try {
                    this.mEnabledSession.method.setSessionEnabled(this.mEnabledSession.session, DEBUG_RESTORE);
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

    public boolean handleMessage(Message msg) {
        boolean z = DEBUG_RESTORE;
        SomeArgs args;
        int missingMethods;
        SessionState session;
        switch (msg.what) {
            case MSG_UNBIND_INPUT /*1000*/:
                try {
                    ((IInputMethod) msg.obj).unbindInput();
                } catch (RemoteException e) {
                }
                return true;
            case MSG_BIND_INPUT /*1010*/:
                args = msg.obj;
                try {
                    ((IInputMethod) args.arg1).bindInput((InputBinding) args.arg2);
                } catch (RemoteException e2) {
                }
                args.recycle();
                return true;
            case MSG_SHOW_SOFT_INPUT /*1020*/:
                args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).showSoftInput(msg.arg1, (ResultReceiver) args.arg2);
                } catch (RemoteException e3) {
                }
                args.recycle();
                return true;
            case MSG_HIDE_SOFT_INPUT /*1030*/:
                args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).hideSoftInput(0, (ResultReceiver) args.arg2);
                } catch (RemoteException e4) {
                }
                args.recycle();
                return true;
            case MSG_ATTACH_TOKEN /*1040*/:
                args = (SomeArgs) msg.obj;
                try {
                    ((IInputMethod) args.arg1).attachToken((IBinder) args.arg2);
                } catch (RemoteException e5) {
                }
                args.recycle();
                return true;
            case MSG_CREATE_SESSION /*1050*/:
                args = (SomeArgs) msg.obj;
                IInputMethod method = args.arg1;
                InputChannel channel = args.arg2;
                try {
                    method.createSession(channel, (IInputSessionCallback) args.arg3);
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                } catch (RemoteException e6) {
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                } catch (Throwable th) {
                    if (channel != null && Binder.isProxy(method)) {
                        channel.dispose();
                    }
                }
                args.recycle();
                return true;
            case MSG_START_INPUT /*2000*/:
                missingMethods = msg.arg1;
                args = (SomeArgs) msg.obj;
                try {
                    session = args.arg1;
                    setEnabledSessionInMainThread(session);
                    session.method.startInput((IInputContext) args.arg2, missingMethods, (EditorInfo) args.arg3);
                } catch (RemoteException e7) {
                }
                args.recycle();
                return true;
            case MSG_RESTART_INPUT /*2010*/:
                missingMethods = msg.arg1;
                args = (SomeArgs) msg.obj;
                try {
                    session = (SessionState) args.arg1;
                    setEnabledSessionInMainThread(session);
                    session.method.restartInput((IInputContext) args.arg2, missingMethods, (EditorInfo) args.arg3);
                } catch (RemoteException e8) {
                }
                args.recycle();
                return true;
            case MSG_UNBIND_CLIENT /*3000*/:
                try {
                    ((IInputMethodClient) msg.obj).onUnbindMethod(msg.arg1, msg.arg2);
                } catch (RemoteException e9) {
                }
                return true;
            case MSG_BIND_CLIENT /*3010*/:
                args = (SomeArgs) msg.obj;
                IInputMethodClient client = args.arg1;
                InputBindResult res = args.arg2;
                try {
                    client.onBindMethod(res);
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                } catch (RemoteException e10) {
                    Slog.w(TAG, "Client died receiving input method " + args.arg2);
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                } catch (Throwable th2) {
                    if (res.channel != null && Binder.isProxy(client)) {
                        res.channel.dispose();
                    }
                }
                args.recycle();
                return true;
            case MSG_SET_ACTIVE /*3020*/:
                try {
                    if (msg.arg1 == 1) {
                        ((ClientState) msg.obj).client.setActive(true);
                    }
                } catch (RemoteException e11) {
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
                int sequenceNumber = msg.arg1;
                ClientState clientState = msg.obj;
                try {
                    clientState.client.setUserActionNotificationSequenceNumber(sequenceNumber);
                } catch (RemoteException e12) {
                    Slog.w(TAG, "Got RemoteException sending setUserActionNotificationSequenceNumber(" + sequenceNumber + ") notification to pid " + clientState.pid + " uid " + clientState.uid);
                }
                return true;
            default:
                return DEBUG_RESTORE;
        }
    }

    private void handleSetInteractive(boolean interactive) {
        int i = 0;
        synchronized (this.mMethodMap) {
            int i2;
            this.mIsInteractive = interactive;
            IBinder iBinder = this.mCurToken;
            if (interactive) {
                i2 = this.mImeWindowVis;
            } else {
                i2 = 0;
            }
            updateSystemUiLocked(iBinder, i2, this.mBackDisposition);
            if (!(this.mCurClient == null || this.mCurClient.client == null)) {
                IInterface iInterface = this.mCurClient.client;
                HandlerCaller handlerCaller = this.mCaller;
                if (this.mIsInteractive) {
                    i = 1;
                }
                executeOrSendMessage(iInterface, handlerCaller.obtainMessageIO(MSG_SET_ACTIVE, i, this.mCurClient));
            }
        }
    }

    void buildInputMethodListLocked(boolean resetDefaultEnabledIme) {
        this.mMethodList.clear();
        this.mMethodMap.clear();
        List<ResolveInfo> services = this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent("android.view.InputMethod"), 32896, this.mSettings.getCurrentUserId());
        for (int i = 0; i < services.size(); i++) {
            ResolveInfo ri = (ResolveInfo) services.get(i);
            ServiceInfo si = ri.serviceInfo;
            ComponentName compName = new ComponentName(si.packageName, si.name);
            if (!"android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                Slog.w(TAG, "Skipping input method " + compName + ": it does not require the permission " + "android.permission.BIND_INPUT_METHOD");
            } else if (shouldBuildInputMethodList(si.packageName)) {
                try {
                    InputMethodInfo p = new InputMethodInfo(this.mContext, ri);
                    this.mMethodList.add(p);
                    this.mMethodMap.put(p.getId(), p);
                } catch (Exception e) {
                    Slog.w(TAG, "Unable to load input method " + compName, e);
                }
            }
        }
        updateSecureIMEStatus();
    }

    public boolean setInputMethodEnabled(String id, boolean enabled) {
        return DEBUG_RESTORE;
    }

    private Pair<InputMethodInfo, InputMethodSubtype> findLastResortApplicableShortcutInputMethodAndSubtypeLocked(String mode) {
        for (InputMethodInfo imi : this.mSettings.getEnabledInputMethodListLocked()) {
            ArrayList<InputMethodSubtype> overridingImplicitlyEnabledSubtypes = InputMethodUtils.getOverridingImplicitlyEnabledSubtypes(imi, mode);
        }
        return null;
    }

    public InputMethodSubtype getCurrentInputMethodSubtype() {
        return null;
    }

    public List getShortcutInputMethodsAndSubtypes() {
        synchronized (this.mMethodMap) {
            ArrayList<Object> ret = new ArrayList();
            if (this.mShortcutInputMethodsAndSubtypes.size() == 0) {
                Pair<InputMethodInfo, InputMethodSubtype> info = findLastResortApplicableShortcutInputMethodAndSubtypeLocked("voice");
                if (info != null) {
                    ret.add(info.first);
                    ret.add(info.second);
                }
                return ret;
            }
            return ret;
        }
    }

    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        return DEBUG_RESTORE;
    }

    private static String imeWindowStatusToString(int imeWindowVis) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if ((imeWindowVis & 1) != 0) {
            sb.append("Active");
            first = DEBUG_RESTORE;
        }
        if ((imeWindowVis & 2) != 0) {
            if (!first) {
                sb.append("|");
            }
            sb.append("Visible");
        }
        return sb.toString();
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump InputMethodManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        Printer p = new PrintWriterPrinter(pw);
        synchronized (this.mMethodMap) {
            p.println("  Clients:");
            for (ClientState ci : this.mClients.values()) {
                p.println("  Client " + ci + ":");
                p.println("    client=" + ci.client);
                p.println("    inputContext=" + ci.inputContext);
                p.println("    sessionRequested=" + ci.sessionRequested);
                p.println("    curSession=" + ci.curSession);
            }
            p.println("  mCurMethodId=" + this.mCurMethodId);
            ClientState client = this.mCurClient;
            p.println("  mCurClient=" + client + " mCurSeq=" + this.mCurSeq);
            p.println("  mCurFocusedWindow=" + this.mCurFocusedWindow);
            ClientState focusedWindowClient = this.mCurFocusedWindowClient;
            p.println("  mCurFocusedWindowClient=" + focusedWindowClient);
            p.println("  mCurId=" + this.mCurId + " mHaveConnect=" + this.mHaveConnection + " mBoundToMethod=" + this.mBoundToMethod);
            p.println("  mCurToken=" + this.mCurToken);
            p.println("  mCurIntent=" + this.mCurIntent);
            IInputMethod method = this.mCurMethod;
            p.println("  mCurMethod=" + this.mCurMethod);
            p.println("  mEnabledSession=" + this.mEnabledSession);
            p.println("  mImeWindowVis=" + imeWindowStatusToString(this.mImeWindowVis));
            p.println("  mShowRequested=" + this.mShowRequested + " mShowExplicitlyRequested=" + this.mShowExplicitlyRequested + " mShowForced=" + this.mShowForced + " mInputShown=" + this.mInputShown);
            p.println("  mCurUserActionNotificationSequenceNumber=" + this.mCurUserActionNotificationSequenceNumber);
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
            } catch (RemoteException e22) {
                p.println("Input method service dead: " + e22);
            }
        } else {
            p.println("No input method service.");
        }
    }
}
