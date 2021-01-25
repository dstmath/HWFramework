package com.android.server.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.hardware.fingerprint.IFingerprintService;
import android.hdm.HwDeviceManager;
import android.media.AudioManagerInternal;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.provider.Settings;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.IntArray;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.IWindow;
import android.view.KeyEvent;
import android.view.MagnificationSpec;
import android.view.WindowInfo;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.accessibility.IAccessibilityInteractionConnection;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import android.view.accessibility.IAccessibilityManager;
import android.view.accessibility.IAccessibilityManagerClient;
import com.android.internal.accessibility.AccessibilityShortcutController;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.IntPair;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.accessibility.AbstractAccessibilityServiceConnection;
import com.android.server.accessibility.AccessibilityManagerService;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.pm.DumpState;
import com.android.server.power.IHwShutdownThread;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.android.server.wm.WindowManagerInternal;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParserException;

public class AccessibilityManagerService extends IAccessibilityManager.Stub implements AbstractAccessibilityServiceConnection.SystemSupport {
    private static final String ACCESSIBILITY_SCREENREADER_ENABLED = "accessibility_screenreader_enabled";
    private static final char COMPONENT_NAME_SEPARATOR = ':';
    private static final boolean DEBUG = false;
    private static final String FUNCTION_DUMP = "dump";
    private static final String FUNCTION_REGISTER_UI_TEST_AUTOMATION_SERVICE = "registerUiTestAutomationService";
    private static final String GET_WINDOW_TOKEN = "getWindowToken";
    private static final String LOG_TAG = "AccessibilityManagerService";
    public static final int MAGNIFICATION_GESTURE_HANDLER_ID = 0;
    private static final int OWN_PROCESS_ID = Process.myPid();
    private static final String SCREENREADER_SERVICE_CLSNAME = "com.bjbyhd.screenreader_huawei.ScreenReaderService";
    private static final String SCREENREADER_SERVICE_PKGNAME = "com.bjbyhd.screenreader_huawei";
    private static final String SETTING_CLSNAME = "com.android.settings.navigation.NavigationModeSettingService";
    private static final String SETTING_PKGNAME = "com.android.settings";
    private static final String SETTING_TALKBACK_STATE = "com.android.settings.talkback_state";
    private static final String SET_PIP_ACTION_REPLACEMENT = "setPictureInPictureActionReplacingConnection";
    private static final String TALKBACK_SERVICE_CLSNAME = "com.google.android.marvin.talkback.TalkBackService";
    private static final int TALKBACK_SERVICE_DISABLE = 0;
    private static final int TALKBACK_SERVICE_ENABLE = 1;
    private static final String TALKBACK_SERVICE_PKTNAME = "com.google.android.marvin.talkback";
    private static final String TALKBACK_SETTINGS_ACTION = "huawei.settings.intent.action.TALKBACK_SETTINGS";
    private static final String TEMPORARY_ENABLE_ACCESSIBILITY_UNTIL_KEYGUARD_REMOVED = "temporaryEnableAccessibilityStateUntilKeyguardRemoved";
    private static final int WAIT_FOR_USER_STATE_FULLY_INITIALIZED_MILLIS = 3000;
    private static final int WAIT_MOTION_INJECTOR_TIMEOUT_MILLIS = 1000;
    private static final int WAIT_WINDOWS_TIMEOUT_MILLIS = 5000;
    private static int sIdCounter = 1;
    private static int sNextWindowId;
    private final AccessibilityDisplayListener mA11yDisplayListener;
    private final AppOpsManager mAppOpsManager;
    private AppWidgetManagerInternal mAppWidgetService;
    private final Context mContext;
    private int mCurrentUserId = 0;
    private AlertDialog mEnableTouchExplorationDialog;
    private FingerprintGestureDispatcher mFingerprintGestureDispatcher;
    private final GlobalActionPerformer mGlobalActionPerformer;
    private final RemoteCallbackList<IAccessibilityManagerClient> mGlobalClients = new RemoteCallbackList<>();
    private final SparseArray<RemoteAccessibilityConnection> mGlobalInteractionConnections = new SparseArray<>();
    private final SparseArray<IBinder> mGlobalWindowTokens = new SparseArray<>();
    private boolean mHasInputFilter;
    private boolean mInitialized;
    private AccessibilityInputFilter mInputFilter;
    private InteractionBridge mInteractionBridge;
    private boolean mIsAccessibilityButtonShown;
    private boolean mIsScreenReaderEnabled;
    private boolean mIsTalkBackEnabled;
    private KeyEventDispatcher mKeyEventDispatcher;
    private final Object mLock = new Object();
    private MagnificationController mMagnificationController;
    private final MainHandler mMainHandler;
    private MotionEventInjector mMotionEventInjector;
    private final PackageManager mPackageManager;
    private RemoteAccessibilityConnection mPictureInPictureActionReplacingConnection;
    private final PowerManager mPowerManager;
    private final SecurityPolicy mSecurityPolicy;
    private final TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(COMPONENT_NAME_SEPARATOR);
    private final List<AccessibilityServiceInfo> mTempAccessibilityServiceInfoList = new ArrayList();
    private final Set<ComponentName> mTempComponentNameSet = new HashSet();
    private final IntArray mTempIntArray = new IntArray(0);
    private final Point mTempPoint = new Point();
    private final Rect mTempRect = new Rect();
    private final Rect mTempRect1 = new Rect();
    private final UiAutomationManager mUiAutomationManager = new UiAutomationManager(this.mLock);
    private final UserManager mUserManager;
    private final SparseArray<UserState> mUserStates = new SparseArray<>();
    private final WindowManagerInternal mWindowManagerService;
    private WindowsForAccessibilityCallback mWindowsForAccessibilityCallback;

    static /* synthetic */ int access$2508() {
        int i = sIdCounter;
        sIdCounter = i + 1;
        return i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UserState getCurrentUserStateLocked() {
        return getUserStateLocked(this.mCurrentUserId);
    }

    public static final class Lifecycle extends SystemService {
        private final AccessibilityManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            this.mService = new AccessibilityManagerService(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.accessibility.AccessibilityManagerService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.accessibility.AccessibilityManagerService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            publishBinderService("accessibility", this.mService);
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            this.mService.onBootPhase(phase);
        }
    }

    public AccessibilityManagerService(Context context) {
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWindowManagerService = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mSecurityPolicy = new SecurityPolicy();
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mMainHandler = new MainHandler(this.mContext.getMainLooper());
        this.mGlobalActionPerformer = new GlobalActionPerformer(this.mContext, this.mWindowManagerService);
        this.mA11yDisplayListener = new AccessibilityDisplayListener(this.mContext, this.mMainHandler);
        registerBroadcastReceivers();
        new AccessibilityContentObserver(this.mMainHandler).register(context.getContentResolver());
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public int getCurrentUserIdLocked() {
        return this.mCurrentUserId;
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public boolean isAccessibilityButtonShown() {
        return this.mIsAccessibilityButtonShown;
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public FingerprintGestureDispatcher getFingerprintGestureDispatcher() {
        return this.mFingerprintGestureDispatcher;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onBootPhase(int phase) {
        if (phase == 500 && this.mPackageManager.hasSystemFeature("android.software.app_widgets")) {
            this.mAppWidgetService = (AppWidgetManagerInternal) LocalServices.getService(AppWidgetManagerInternal.class);
        }
    }

    private UserState getUserState(int userId) {
        UserState userStateLocked;
        synchronized (this.mLock) {
            userStateLocked = getUserStateLocked(userId);
        }
        return userStateLocked;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UserState getUserStateLocked(int userId) {
        UserState state = this.mUserStates.get(userId);
        if (state != null) {
            return state;
        }
        UserState state2 = new UserState(userId);
        this.mUserStates.put(userId, state2);
        return state2;
    }

    /* access modifiers changed from: package-private */
    public boolean getBindInstantServiceAllowed(int userId) {
        UserState userState = getUserState(userId);
        if (userState == null) {
            return false;
        }
        return userState.getBindInstantServiceAllowed();
    }

    /* access modifiers changed from: package-private */
    public void setBindInstantServiceAllowed(int userId, boolean allowed) {
        synchronized (this.mLock) {
            UserState userState = getUserState(userId);
            if (userState == null) {
                if (allowed) {
                    userState = new UserState(userId);
                    this.mUserStates.put(userId, userState);
                } else {
                    return;
                }
            }
            userState.setBindInstantServiceAllowed(allowed);
        }
    }

    private void registerBroadcastReceivers() {
        new PackageMonitor() {
            /* class com.android.server.accessibility.AccessibilityManagerService.AnonymousClass1 */

            public void onSomePackagesChanged() {
                synchronized (AccessibilityManagerService.this.mLock) {
                    if (getChangingUserId() == AccessibilityManagerService.this.mCurrentUserId) {
                        UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                        userState.mInstalledServices.clear();
                        if (AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState)) {
                            AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                        }
                    }
                }
            }

            public void onPackageUpdateFinished(String packageName, int uid) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    int userId = getChangingUserId();
                    if (userId == AccessibilityManagerService.this.mCurrentUserId) {
                        UserState userState = AccessibilityManagerService.this.getUserStateLocked(userId);
                        if (userState.mBindingServices.removeIf(new Predicate(packageName) {
                            /* class com.android.server.accessibility.$$Lambda$AccessibilityManagerService$1$49HMbWlhAK8DBFFzhu5wH_EQaM */
                            private final /* synthetic */ String f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Predicate
                            public final boolean test(Object obj) {
                                return AccessibilityManagerService.AnonymousClass1.lambda$onPackageUpdateFinished$0(this.f$0, (ComponentName) obj);
                            }
                        })) {
                            AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                        }
                    }
                }
            }

            static /* synthetic */ boolean lambda$onPackageUpdateFinished$0(String packageName, ComponentName component) {
                return component != null && component.getPackageName().equals(packageName);
            }

            public void onPackageRemoved(String packageName, int uid) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    int userId = getChangingUserId();
                    if (userId == AccessibilityManagerService.this.mCurrentUserId) {
                        UserState userState = AccessibilityManagerService.this.getUserStateLocked(userId);
                        Iterator<ComponentName> it = userState.mEnabledServices.iterator();
                        while (it.hasNext()) {
                            ComponentName comp = it.next();
                            if (comp.getPackageName().equals(packageName)) {
                                it.remove();
                                userState.mBindingServices.remove(comp);
                                AccessibilityManagerService.this.persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, userId);
                                userState.mTouchExplorationGrantedServices.remove(comp);
                                AccessibilityManagerService.this.persistComponentNamesToSettingLocked("touch_exploration_granted_accessibility_services", userState.mTouchExplorationGrantedServices, userId);
                                AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                return;
                            }
                        }
                    }
                }
            }

            public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    int userId = getChangingUserId();
                    if (userId != AccessibilityManagerService.this.mCurrentUserId) {
                        return false;
                    }
                    UserState userState = AccessibilityManagerService.this.getUserStateLocked(userId);
                    Iterator<ComponentName> it = userState.mEnabledServices.iterator();
                    while (it.hasNext()) {
                        ComponentName comp = it.next();
                        String compPkg = comp.getPackageName();
                        for (String pkg : packages) {
                            if (compPkg.equals(pkg)) {
                                if (!doit) {
                                    return true;
                                }
                                if (HwDeviceManager.disallowOp(53, pkg)) {
                                    Slog.w(AccessibilityManagerService.LOG_TAG, " onHandleForceStop. this forcestop pkg =" + compPkg + " in MDM accessibility whitelist ,block!");
                                } else {
                                    it.remove();
                                    userState.mBindingServices.remove(comp);
                                    AccessibilityManagerService.this.persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, userId);
                                    AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                }
                            }
                        }
                    }
                    return false;
                }
            }
        }.register(this.mContext, (Looper) null, UserHandle.ALL, true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.os.action.SETTING_RESTORED");
        intentFilter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.accessibility.AccessibilityManagerService.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    AccessibilityManagerService.this.switchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
                    UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                    synchronized (AccessibilityManagerService.this.mLock) {
                        AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState);
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    AccessibilityManagerService.this.unlockUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    AccessibilityManagerService.this.removeUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                    synchronized (AccessibilityManagerService.this.mLock) {
                        UserState userState2 = AccessibilityManagerService.this.getCurrentUserStateLocked();
                        if (AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState2)) {
                            AccessibilityManagerService.this.onUserStateChangedLocked(userState2);
                        }
                    }
                } else if ("android.os.action.SETTING_RESTORED".equals(action) && "enabled_accessibility_services".equals(intent.getStringExtra("setting_name"))) {
                    synchronized (AccessibilityManagerService.this.mLock) {
                        AccessibilityManagerService.this.restoreEnabledAccessibilityServicesLocked(intent.getStringExtra("previous_value"), intent.getStringExtra("new_value"));
                    }
                }
            }
        }, UserHandle.ALL, intentFilter, null, null);
    }

    public long addClient(IAccessibilityManagerClient callback, int userId) {
        if (callback == null) {
            Slog.e(LOG_TAG, "addClient param error");
            return 0;
        }
        synchronized (this.mLock) {
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            UserState userState = getUserStateLocked(resolvedUserId);
            Client client = new Client(callback, Binder.getCallingUid(), userState);
            if (this.mSecurityPolicy.isCallerInteractingAcrossUsers(userId)) {
                this.mGlobalClients.register(callback, client);
                return IntPair.of(userState.getClientState(), client.mLastSentRelevantEventTypes);
            }
            userState.mUserClients.register(callback, client);
            return IntPair.of(resolvedUserId == this.mCurrentUserId ? userState.getClientState() : 0, client.mLastSentRelevantEventTypes);
        }
    }

    public void sendAccessibilityEvent(AccessibilityEvent event, int userId) {
        AccessibilityWindowInfo pip;
        if (event == null) {
            Slog.e(LOG_TAG, "sendAccessibilityEvent param error");
            return;
        }
        boolean dispatchEvent = false;
        synchronized (this.mLock) {
            if (event.getWindowId() == -3 && (pip = this.mSecurityPolicy.getPictureInPictureWindow()) != null) {
                event.setWindowId(pip.getId());
            }
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            event.setPackageName(this.mSecurityPolicy.resolveValidReportedPackageLocked(event.getPackageName(), UserHandle.getCallingAppId(), resolvedUserId));
            if (resolvedUserId == this.mCurrentUserId) {
                if (this.mSecurityPolicy.canDispatchAccessibilityEventLocked(event)) {
                    this.mSecurityPolicy.updateActiveAndAccessibilityFocusedWindowLocked(event.getWindowId(), event.getSourceNodeId(), event.getEventType(), event.getAction());
                    this.mSecurityPolicy.updateEventSourceLocked(event);
                    dispatchEvent = true;
                }
                if (this.mHasInputFilter && this.mInputFilter != null) {
                    this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$BX2CMQr5jU9WhPYx7Aaae4zgxf4.INSTANCE, this, AccessibilityEvent.obtain(event)));
                }
            }
        }
        if (dispatchEvent) {
            if (event.getEventType() == 32 && this.mWindowsForAccessibilityCallback != null) {
                ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)).computeWindowsForAccessibility();
            }
            synchronized (this.mLock) {
                notifyAccessibilityServicesDelayedLocked(event, false);
                notifyAccessibilityServicesDelayedLocked(event, true);
                this.mUiAutomationManager.sendAccessibilityEventLocked(event);
            }
        }
        if (OWN_PROCESS_ID != Binder.getCallingPid()) {
            event.recycle();
        }
    }

    /* access modifiers changed from: private */
    public void sendAccessibilityEventToInputFilter(AccessibilityEvent event) {
        synchronized (this.mLock) {
            if (this.mHasInputFilter && this.mInputFilter != null) {
                this.mInputFilter.notifyAccessibilityEvent(event);
            }
        }
        event.recycle();
    }

    public List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList(int userId) {
        List<AccessibilityServiceInfo> list;
        synchronized (this.mLock) {
            list = getUserStateLocked(this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId)).mInstalledServices;
        }
        return list;
    }

    public List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackType, int userId) {
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId));
            if (this.mUiAutomationManager.suppressingAccessibilityServicesLocked()) {
                return Collections.emptyList();
            }
            List<AccessibilityServiceConnection> services = userState.mBoundServices;
            int serviceCount = services.size();
            List<AccessibilityServiceInfo> result = new ArrayList<>(serviceCount);
            for (int i = 0; i < serviceCount; i++) {
                AccessibilityServiceConnection service = services.get(i);
                if ((service.mFeedbackType & feedbackType) != 0) {
                    result.add(service.getServiceInfo());
                }
            }
            return result;
        }
    }

    public void interrupt(int userId) {
        List<IAccessibilityServiceClient> interfacesToInterrupt;
        synchronized (this.mLock) {
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            if (resolvedUserId == this.mCurrentUserId) {
                List<AccessibilityServiceConnection> services = getUserStateLocked(resolvedUserId).mBoundServices;
                int numServices = services.size();
                interfacesToInterrupt = new ArrayList<>(numServices);
                for (int i = 0; i < numServices; i++) {
                    AccessibilityServiceConnection service = services.get(i);
                    IBinder a11yServiceBinder = service.mService;
                    IAccessibilityServiceClient a11yServiceInterface = service.mServiceInterface;
                    if (!(a11yServiceBinder == null || a11yServiceInterface == null)) {
                        interfacesToInterrupt.add(a11yServiceInterface);
                    }
                }
            } else {
                return;
            }
        }
        int count = interfacesToInterrupt.size();
        for (int i2 = 0; i2 < count; i2++) {
            try {
                interfacesToInterrupt.get(i2).onInterrupt();
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error sending interrupt request to " + interfacesToInterrupt.get(i2), re);
            }
        }
    }

    public int addAccessibilityInteractionConnection(IWindow windowToken, IAccessibilityInteractionConnection connection, String packageName, int userId) throws RemoteException {
        Object obj;
        WindowManagerInternal wm;
        String str;
        String packageName2;
        int windowId;
        if (windowToken != null) {
            if (connection != null) {
                Object obj2 = this.mLock;
                synchronized (obj2) {
                    try {
                        int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
                        int resolvedUid = UserHandle.getUid(resolvedUserId, UserHandle.getCallingAppId());
                        str = packageName;
                        try {
                            packageName2 = this.mSecurityPolicy.resolveValidReportedPackageLocked(str, UserHandle.getCallingAppId(), resolvedUserId);
                        } catch (Throwable th) {
                            wm = th;
                            obj = obj2;
                            throw wm;
                        }
                        try {
                            int windowId2 = sNextWindowId;
                            sNextWindowId = windowId2 + 1;
                            if (this.mSecurityPolicy.isCallerInteractingAcrossUsers(userId)) {
                                RemoteAccessibilityConnection wrapper = new RemoteAccessibilityConnection(windowId2, connection, packageName2, resolvedUid, -1);
                                wrapper.linkToDeath();
                                this.mGlobalInteractionConnections.put(windowId2, wrapper);
                                this.mGlobalWindowTokens.put(windowId2, windowToken.asBinder());
                                windowId = windowId2;
                                obj = obj2;
                            } else {
                                windowId = windowId2;
                                obj = obj2;
                                try {
                                    RemoteAccessibilityConnection wrapper2 = new RemoteAccessibilityConnection(windowId2, connection, packageName2, resolvedUid, resolvedUserId);
                                    wrapper2.linkToDeath();
                                    UserState userState = getUserStateLocked(resolvedUserId);
                                    userState.mInteractionConnections.put(windowId, wrapper2);
                                    userState.mWindowTokens.put(windowId, windowToken.asBinder());
                                } catch (Throwable th2) {
                                    wm = th2;
                                    throw wm;
                                }
                            }
                            ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class)).computeWindowsForAccessibility();
                            return windowId;
                        } catch (Throwable th3) {
                            wm = th3;
                            obj = obj2;
                            throw wm;
                        }
                    } catch (Throwable th4) {
                        wm = th4;
                        str = packageName;
                        obj = obj2;
                        throw wm;
                    }
                }
            }
        }
        Slog.e(LOG_TAG, "addAccessibilityInteractionConnection param error");
        return 0;
    }

    public void removeAccessibilityInteractionConnection(IWindow window) {
        if (window == null) {
            Slog.e(LOG_TAG, "removeAccessibilityInteractionConnection param error");
            return;
        }
        synchronized (this.mLock) {
            this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(UserHandle.getCallingUserId());
            IBinder token = window.asBinder();
            int removedWindowId = removeAccessibilityInteractionConnectionInternalLocked(token, this.mGlobalWindowTokens, this.mGlobalInteractionConnections);
            if (removedWindowId >= 0) {
                this.mSecurityPolicy.onAccessibilityClientRemovedLocked(removedWindowId);
                return;
            }
            int userCount = this.mUserStates.size();
            for (int i = 0; i < userCount; i++) {
                UserState userState = this.mUserStates.valueAt(i);
                int removedWindowIdForUser = removeAccessibilityInteractionConnectionInternalLocked(token, userState.mWindowTokens, userState.mInteractionConnections);
                if (removedWindowIdForUser >= 0) {
                    this.mSecurityPolicy.onAccessibilityClientRemovedLocked(removedWindowIdForUser);
                    return;
                }
            }
        }
    }

    private int removeAccessibilityInteractionConnectionInternalLocked(IBinder windowToken, SparseArray<IBinder> windowTokens, SparseArray<RemoteAccessibilityConnection> interactionConnections) {
        int count = windowTokens.size();
        for (int i = 0; i < count; i++) {
            if (windowTokens.valueAt(i) == windowToken) {
                int windowId = windowTokens.keyAt(i);
                windowTokens.removeAt(i);
                interactionConnections.get(windowId).unlinkToDeath();
                interactionConnections.remove(windowId);
                return windowId;
            }
        }
        return -1;
    }

    public void setPictureInPictureActionReplacingConnection(IAccessibilityInteractionConnection connection) throws RemoteException {
        this.mSecurityPolicy.enforceCallingPermission("android.permission.MODIFY_ACCESSIBILITY_DATA", SET_PIP_ACTION_REPLACEMENT);
        synchronized (this.mLock) {
            if (this.mPictureInPictureActionReplacingConnection != null) {
                this.mPictureInPictureActionReplacingConnection.unlinkToDeath();
                this.mPictureInPictureActionReplacingConnection = null;
            }
            if (connection != null) {
                RemoteAccessibilityConnection wrapper = new RemoteAccessibilityConnection(-3, connection, "foo.bar.baz", 1000, -1);
                this.mPictureInPictureActionReplacingConnection = wrapper;
                wrapper.linkToDeath();
            }
        }
    }

    public void registerUiTestAutomationService(IBinder owner, IAccessibilityServiceClient serviceClient, AccessibilityServiceInfo accessibilityServiceInfo, int flags) {
        this.mSecurityPolicy.enforceCallingPermission("android.permission.RETRIEVE_WINDOW_CONTENT", FUNCTION_REGISTER_UI_TEST_AUTOMATION_SERVICE);
        synchronized (this.mLock) {
            UiAutomationManager uiAutomationManager = this.mUiAutomationManager;
            Context context = this.mContext;
            int i = sIdCounter;
            sIdCounter = i + 1;
            uiAutomationManager.registerUiTestAutomationServiceLocked(owner, serviceClient, context, accessibilityServiceInfo, i, this.mMainHandler, this.mSecurityPolicy, this, this.mWindowManagerService, this.mGlobalActionPerformer, flags);
            onUserStateChangedLocked(getCurrentUserStateLocked());
        }
    }

    public void unregisterUiTestAutomationService(IAccessibilityServiceClient serviceClient) {
        synchronized (this.mLock) {
            this.mUiAutomationManager.unregisterUiTestAutomationServiceLocked(serviceClient);
        }
    }

    public void temporaryEnableAccessibilityStateUntilKeyguardRemoved(ComponentName service, boolean touchExplorationEnabled) {
        this.mSecurityPolicy.enforceCallingPermission("android.permission.TEMPORARY_ENABLE_ACCESSIBILITY", TEMPORARY_ENABLE_ACCESSIBILITY_UNTIL_KEYGUARD_REMOVED);
        if (this.mWindowManagerService.isKeyguardLocked()) {
            synchronized (this.mLock) {
                UserState userState = getCurrentUserStateLocked();
                userState.mIsTouchExplorationEnabled = touchExplorationEnabled;
                userState.mIsDisplayMagnificationEnabled = false;
                userState.mIsNavBarMagnificationEnabled = false;
                userState.mIsAutoclickEnabled = false;
                userState.mEnabledServices.clear();
                userState.mEnabledServices.add(service);
                userState.mBindingServices.clear();
                userState.mTouchExplorationGrantedServices.clear();
                userState.mTouchExplorationGrantedServices.add(service);
                onUserStateChangedLocked(userState);
            }
        }
    }

    public IBinder getWindowToken(int windowId, int userId) {
        this.mSecurityPolicy.enforceCallingPermission("android.permission.RETRIEVE_WINDOW_TOKEN", GET_WINDOW_TOKEN);
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId) != this.mCurrentUserId) {
                return null;
            }
            if (this.mSecurityPolicy.findA11yWindowInfoById(windowId) == null) {
                return null;
            }
            return findWindowTokenLocked(windowId);
        }
    }

    public void notifyAccessibilityButtonClicked(int displayId) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE") == 0) {
            synchronized (this.mLock) {
                notifyAccessibilityButtonClickedLocked(displayId);
            }
            return;
        }
        throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR_SERVICE");
    }

    public void notifyAccessibilityButtonVisibilityChanged(boolean shown) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE") == 0) {
            synchronized (this.mLock) {
                notifyAccessibilityButtonVisibilityChangedLocked(shown);
            }
            return;
        }
        throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR_SERVICE");
    }

    /* access modifiers changed from: package-private */
    public boolean onGesture(int gestureId) {
        boolean handled;
        synchronized (this.mLock) {
            handled = notifyGestureLocked(gestureId, false);
            if (!handled) {
                handled = notifyGestureLocked(gestureId, true);
            }
        }
        return handled;
    }

    @VisibleForTesting
    public boolean notifyKeyEvent(KeyEvent event, int policyFlags) {
        synchronized (this.mLock) {
            List<AccessibilityServiceConnection> boundServices = getCurrentUserStateLocked().mBoundServices;
            if (boundServices.isEmpty()) {
                Slog.w(LOG_TAG, "volume_debug boundServices is empty");
                this.mInputFilter.noFilterEvent(event, policyFlags);
                return false;
            }
            return getKeyEventDispatcher().notifyKeyEventLocked(event, policyFlags, boundServices);
        }
    }

    public void notifyMagnificationChanged(int displayId, Region region, float scale, float centerX, float centerY) {
        synchronized (this.mLock) {
            notifyClearAccessibilityCacheLocked();
            notifyMagnificationChangedLocked(displayId, region, scale, centerX, centerY);
        }
    }

    /* access modifiers changed from: package-private */
    public void setMotionEventInjector(MotionEventInjector motionEventInjector) {
        synchronized (this.mLock) {
            this.mMotionEventInjector = motionEventInjector;
            this.mLock.notifyAll();
        }
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public MotionEventInjector getMotionEventInjectorLocked() {
        long endMillis = SystemClock.uptimeMillis() + 1000;
        while (this.mMotionEventInjector == null && SystemClock.uptimeMillis() < endMillis) {
            try {
                this.mLock.wait(endMillis - SystemClock.uptimeMillis());
            } catch (InterruptedException e) {
            }
        }
        if (this.mMotionEventInjector == null) {
            Slog.e(LOG_TAG, "MotionEventInjector installation timed out");
        }
        return this.mMotionEventInjector;
    }

    /* access modifiers changed from: package-private */
    public boolean getAccessibilityFocusClickPointInScreen(Point outPoint) {
        return getInteractionBridge().getAccessibilityFocusClickPointInScreenNotLocked(outPoint);
    }

    public boolean performActionOnAccessibilityFocusedItem(AccessibilityNodeInfo.AccessibilityAction action) {
        return getInteractionBridge().performActionOnAccessibilityFocusedItemNotLocked(action);
    }

    /* access modifiers changed from: package-private */
    public boolean getWindowBounds(int windowId, Rect outBounds) {
        IBinder token;
        synchronized (this.mLock) {
            token = this.mGlobalWindowTokens.get(windowId);
            if (token == null) {
                token = getCurrentUserStateLocked().mWindowTokens.get(windowId);
            }
        }
        this.mWindowManagerService.getWindowFrame(token, outBounds);
        if (!outBounds.isEmpty()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean accessibilityFocusOnlyInActiveWindow() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mWindowsForAccessibilityCallback == null;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public int getActiveWindowId() {
        return this.mSecurityPolicy.getActiveWindowId();
    }

    /* access modifiers changed from: package-private */
    public void onTouchInteractionStart() {
        this.mSecurityPolicy.onTouchInteractionStart();
    }

    /* access modifiers changed from: package-private */
    public void onTouchInteractionEnd() {
        this.mSecurityPolicy.onTouchInteractionEnd();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void switchUser(int userId) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId != userId || !this.mInitialized) {
                UserState oldUserState = getCurrentUserStateLocked();
                oldUserState.onSwitchToAnotherUserLocked();
                boolean announceNewUser = false;
                if (oldUserState.mUserClients.getRegisteredCallbackCount() > 0) {
                    this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$zXJtauhUptSkQJSFM55grAVbo.INSTANCE, this, 0, Integer.valueOf(oldUserState.mUserId)));
                }
                if (((UserManager) this.mContext.getSystemService("user")).getUsers().size() > 1) {
                    announceNewUser = true;
                }
                this.mCurrentUserId = userId;
                UserState userState = getCurrentUserStateLocked();
                readConfigurationForUserStateLocked(userState);
                onUserStateChangedLocked(userState);
                if (announceNewUser) {
                    this.mMainHandler.sendMessageDelayed(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$GuW_dQ2mWyy8l4tm19TzFxGbeM.INSTANCE, this), BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void announceNewUserIfNeeded() {
        synchronized (this.mLock) {
            if (getCurrentUserStateLocked().isHandlingAccessibilityEvents()) {
                String message = this.mContext.getString(17041446, ((UserManager) this.mContext.getSystemService("user")).getUserInfo(this.mCurrentUserId).name);
                AccessibilityEvent event = AccessibilityEvent.obtain((int) DumpState.DUMP_KEYSETS);
                event.getText().add(message);
                sendAccessibilityEventLocked(event, this.mCurrentUserId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unlockUser(int userId) {
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.resolveProfileParentLocked(userId) == this.mCurrentUserId) {
                onUserStateChangedLocked(getUserStateLocked(this.mCurrentUserId));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeUser(int userId) {
        synchronized (this.mLock) {
            this.mUserStates.remove(userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void restoreEnabledAccessibilityServicesLocked(String oldSetting, String newSetting) {
        readComponentNamesFromStringLocked(oldSetting, this.mTempComponentNameSet, false);
        readComponentNamesFromStringLocked(newSetting, this.mTempComponentNameSet, true);
        UserState userState = getUserStateLocked(0);
        userState.mEnabledServices.clear();
        userState.mEnabledServices.addAll(this.mTempComponentNameSet);
        persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, 0);
        onUserStateChangedLocked(userState);
    }

    private InteractionBridge getInteractionBridge() {
        InteractionBridge interactionBridge;
        synchronized (this.mLock) {
            if (this.mInteractionBridge == null) {
                this.mInteractionBridge = new InteractionBridge();
            }
            interactionBridge = this.mInteractionBridge;
        }
        return interactionBridge;
    }

    private boolean notifyGestureLocked(int gestureId, boolean isDefault) {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            AccessibilityServiceConnection service = state.mBoundServices.get(i);
            if (service.mRequestTouchExplorationMode && service.mIsDefault == isDefault) {
                service.notifyGesture(gestureId);
                return true;
            }
        }
        return false;
    }

    private void notifyClearAccessibilityCacheLocked() {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            state.mBoundServices.get(i).notifyClearAccessibilityNodeInfoCache();
        }
    }

    private void notifyMagnificationChangedLocked(int displayId, Region region, float scale, float centerX, float centerY) {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            state.mBoundServices.get(i).notifyMagnificationChangedLocked(displayId, region, scale, centerX, centerY);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifySoftKeyboardShowModeChangedLocked(int showMode) {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            state.mBoundServices.get(i).notifySoftKeyboardShowModeChangedLocked(showMode);
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:43:0x001e */
    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r1v5, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r1v6, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    private void notifyAccessibilityButtonClickedLocked(int displayId) {
        UserState state = getCurrentUserStateLocked();
        boolean z = state.mIsNavBarMagnificationEnabled;
        int i = state.mBoundServices.size() - 1;
        int potentialTargets = z;
        while (i >= 0) {
            if (state.mBoundServices.get(i).mRequestAccessibilityButton) {
                potentialTargets = (potentialTargets == true ? 1 : 0) + 1;
            }
            i--;
            potentialTargets = potentialTargets;
        }
        if (potentialTargets != 0) {
            if (potentialTargets != 1) {
                if (state.mServiceAssignedToAccessibilityButton == null && !state.mIsNavBarMagnificationAssignedToAccessibilityButton) {
                    this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$2LOhxU7QkqHWHlN_uVPLmAzrNWk.INSTANCE, this, Integer.valueOf(displayId)));
                } else if (!state.mIsNavBarMagnificationEnabled || !state.mIsNavBarMagnificationAssignedToAccessibilityButton) {
                    for (int i2 = state.mBoundServices.size() - 1; i2 >= 0; i2--) {
                        AccessibilityServiceConnection service = state.mBoundServices.get(i2);
                        if (service.mRequestAccessibilityButton && service.mComponentName.equals(state.mServiceAssignedToAccessibilityButton)) {
                            service.notifyAccessibilityButtonClickedLocked();
                            return;
                        }
                    }
                } else {
                    this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$fHb6jcCpfXvxrnfdXJngiIFuoo.INSTANCE, this, Integer.valueOf(displayId)));
                    return;
                }
                this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$2LOhxU7QkqHWHlN_uVPLmAzrNWk.INSTANCE, this, Integer.valueOf(displayId)));
            } else if (state.mIsNavBarMagnificationEnabled) {
                this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$fHb6jcCpfXvxrnfdXJngiIFuoo.INSTANCE, this, Integer.valueOf(displayId)));
            } else {
                for (int i3 = state.mBoundServices.size() - 1; i3 >= 0; i3--) {
                    AccessibilityServiceConnection service2 = state.mBoundServices.get(i3);
                    if (service2.mRequestAccessibilityButton) {
                        service2.notifyAccessibilityButtonClickedLocked();
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendAccessibilityButtonToInputFilter(int displayId) {
        synchronized (this.mLock) {
            if (this.mHasInputFilter && this.mInputFilter != null) {
                this.mInputFilter.notifyAccessibilityButtonClicked(displayId);
            }
        }
    }

    /* access modifiers changed from: private */
    public void showAccessibilityButtonTargetSelection(int displayId) {
        Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
        intent.addFlags(268468224);
        this.mContext.startActivityAsUser(intent, ActivityOptions.makeBasic().setLaunchDisplayId(displayId).toBundle(), UserHandle.of(this.mCurrentUserId));
    }

    private void notifyAccessibilityButtonVisibilityChangedLocked(boolean available) {
        UserState state = getCurrentUserStateLocked();
        this.mIsAccessibilityButtonShown = available;
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            AccessibilityServiceConnection clientConnection = state.mBoundServices.get(i);
            if (clientConnection.mRequestAccessibilityButton) {
                clientConnection.notifyAccessibilityButtonAvailabilityChangedLocked(clientConnection.isAccessibilityButtonAvailableLocked(state));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeAccessibilityInteractionConnectionLocked(int windowId, int userId) {
        if (userId == -1) {
            this.mGlobalWindowTokens.remove(windowId);
            this.mGlobalInteractionConnections.remove(windowId);
        } else {
            UserState userState = getCurrentUserStateLocked();
            userState.mWindowTokens.remove(windowId);
            userState.mInteractionConnections.remove(windowId);
        }
        this.mSecurityPolicy.onAccessibilityClientRemovedLocked(windowId);
    }

    private boolean readInstalledAccessibilityServiceLocked(UserState userState) {
        this.mTempAccessibilityServiceInfoList.clear();
        int flags = 819332;
        if (userState.getBindInstantServiceAllowed()) {
            flags = 819332 | DumpState.DUMP_VOLUMES;
        }
        List<ResolveInfo> installedServices = this.mPackageManager.queryIntentServicesAsUser(new Intent("android.accessibilityservice.AccessibilityService"), flags, this.mCurrentUserId);
        int count = installedServices.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = installedServices.get(i);
            if (canRegisterService(resolveInfo.serviceInfo)) {
                try {
                    this.mTempAccessibilityServiceInfoList.add(new AccessibilityServiceInfo(resolveInfo, this.mContext));
                } catch (IOException | XmlPullParserException xppe) {
                    Slog.e(LOG_TAG, "Error while initializing AccessibilityServiceInfo", xppe);
                }
            }
        }
        if (!this.mTempAccessibilityServiceInfoList.equals(userState.mInstalledServices)) {
            userState.mInstalledServices.clear();
            userState.mInstalledServices.addAll(this.mTempAccessibilityServiceInfoList);
            this.mTempAccessibilityServiceInfoList.clear();
            return true;
        }
        this.mTempAccessibilityServiceInfoList.clear();
        return false;
    }

    private boolean canRegisterService(ServiceInfo serviceInfo) {
        if (!"android.permission.BIND_ACCESSIBILITY_SERVICE".equals(serviceInfo.permission)) {
            Slog.w(LOG_TAG, "Skipping accessibility service " + new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToShortString() + ": it does not require the permission android.permission.BIND_ACCESSIBILITY_SERVICE");
            return false;
        }
        if (this.mAppOpsManager.noteOpNoThrow("android:bind_accessibility_service", serviceInfo.applicationInfo.uid, serviceInfo.packageName) == 0) {
            return true;
        }
        Slog.w(LOG_TAG, "Skipping accessibility service " + new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToShortString() + ": disallowed by AppOps");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean readEnabledAccessibilityServicesLocked(UserState userState) {
        this.mTempComponentNameSet.clear();
        readComponentNamesFromSettingLocked("enabled_accessibility_services", userState.mUserId, this.mTempComponentNameSet);
        if (!this.mTempComponentNameSet.equals(userState.mEnabledServices)) {
            userState.mEnabledServices.clear();
            userState.mEnabledServices.addAll(this.mTempComponentNameSet);
            this.mTempComponentNameSet.clear();
            return true;
        }
        this.mTempComponentNameSet.clear();
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean readTouchExplorationGrantedAccessibilityServicesLocked(UserState userState) {
        this.mTempComponentNameSet.clear();
        readComponentNamesFromSettingLocked("touch_exploration_granted_accessibility_services", userState.mUserId, this.mTempComponentNameSet);
        if (!this.mTempComponentNameSet.equals(userState.mTouchExplorationGrantedServices)) {
            userState.mTouchExplorationGrantedServices.clear();
            userState.mTouchExplorationGrantedServices.addAll(this.mTempComponentNameSet);
            this.mTempComponentNameSet.clear();
            return true;
        }
        this.mTempComponentNameSet.clear();
        return false;
    }

    private void notifyAccessibilityServicesDelayedLocked(AccessibilityEvent event, boolean isDefault) {
        try {
            UserState state = getCurrentUserStateLocked();
            int count = state.mBoundServices.size();
            for (int i = 0; i < count; i++) {
                AccessibilityServiceConnection service = state.mBoundServices.get(i);
                if (service.mIsDefault == isDefault) {
                    service.notifyAccessibilityEvent(event);
                }
            }
        } catch (IndexOutOfBoundsException e) {
        }
    }

    private void updateRelevantEventsLocked(UserState userState) {
        this.mMainHandler.post(new Runnable(userState) {
            /* class com.android.server.accessibility.$$Lambda$AccessibilityManagerService$RFkfb_W9wnTTs_gy8Dg3k2uQOYQ */
            private final /* synthetic */ AccessibilityManagerService.UserState f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AccessibilityManagerService.this.lambda$updateRelevantEventsLocked$1$AccessibilityManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$updateRelevantEventsLocked$1$AccessibilityManagerService(UserState userState) {
        broadcastToClients(userState, FunctionalUtils.ignoreRemoteException(new FunctionalUtils.RemoteExceptionIgnoringConsumer(userState) {
            /* class com.android.server.accessibility.$$Lambda$AccessibilityManagerService$CNt8wbTQCYcsUnUkUCQHtKqrtY */
            private final /* synthetic */ AccessibilityManagerService.UserState f$1;

            {
                this.f$1 = r2;
            }

            public final void acceptOrThrow(Object obj) {
                AccessibilityManagerService.this.lambda$updateRelevantEventsLocked$0$AccessibilityManagerService(this.f$1, (AccessibilityManagerService.Client) obj);
            }
        }));
    }

    public /* synthetic */ void lambda$updateRelevantEventsLocked$0$AccessibilityManagerService(UserState userState, Client client) throws RemoteException {
        int relevantEventTypes;
        boolean changed = false;
        synchronized (this.mLock) {
            relevantEventTypes = computeRelevantEventTypesLocked(userState, client);
            if (client.mLastSentRelevantEventTypes != relevantEventTypes) {
                client.mLastSentRelevantEventTypes = relevantEventTypes;
                changed = true;
            }
        }
        if (changed) {
            client.mCallback.setRelevantEventTypes(relevantEventTypes);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int computeRelevantEventTypesLocked(UserState userState, Client client) {
        int i;
        int relevantEventTypes = 0;
        int serviceCount = userState.mBoundServices.size();
        int i2 = 0;
        while (true) {
            i = 0;
            if (i2 >= serviceCount) {
                break;
            }
            AccessibilityServiceConnection service = userState.mBoundServices.get(i2);
            if (isClientInPackageWhitelist(service.getServiceInfo(), client)) {
                i = service.getRelevantEventTypes();
            }
            relevantEventTypes |= i;
            i2++;
        }
        if (isClientInPackageWhitelist(this.mUiAutomationManager.getServiceInfo(), client)) {
            i = this.mUiAutomationManager.getRelevantEventTypes();
        }
        return relevantEventTypes | i;
    }

    private static boolean isClientInPackageWhitelist(AccessibilityServiceInfo serviceInfo, Client client) {
        if (serviceInfo == null) {
            return false;
        }
        String[] clientPackages = client.mPackageNames;
        boolean result = ArrayUtils.isEmpty(serviceInfo.packageNames);
        if (result || clientPackages == null) {
            return result;
        }
        for (String packageName : clientPackages) {
            if (ArrayUtils.contains(serviceInfo.packageNames, packageName)) {
                return true;
            }
        }
        return result;
    }

    private void broadcastToClients(UserState userState, Consumer<Client> clientAction) {
        this.mGlobalClients.broadcastForEachCookie(clientAction);
        userState.mUserClients.broadcastForEachCookie(clientAction);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unbindAllServicesLocked(UserState userState) {
        List<AccessibilityServiceConnection> services = userState.mBoundServices;
        for (int count = services.size(); count > 0; count--) {
            services.get(0).unbindLocked();
        }
    }

    private void readComponentNamesFromSettingLocked(String settingName, int userId, Set<ComponentName> outComponentNames) {
        readComponentNamesFromStringLocked(Settings.Secure.getStringForUser(this.mContext.getContentResolver(), settingName, userId), outComponentNames, false);
    }

    private void readComponentNamesFromStringLocked(String names, Set<ComponentName> outComponentNames, boolean doMerge) {
        ComponentName enabledService;
        if (!doMerge) {
            outComponentNames.clear();
        }
        if (names != null) {
            TextUtils.SimpleStringSplitter splitter = this.mStringColonSplitter;
            splitter.setString(names);
            while (splitter.hasNext()) {
                String str = splitter.next();
                if (!(str == null || str.length() <= 0 || (enabledService = ComponentName.unflattenFromString(str)) == null)) {
                    outComponentNames.add(enabledService);
                }
            }
        }
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public void persistComponentNamesToSettingLocked(String settingName, Set<ComponentName> componentNames, int userId) {
        StringBuilder builder = new StringBuilder();
        for (ComponentName componentName : componentNames) {
            if (builder.length() > 0) {
                builder.append(COMPONENT_NAME_SEPARATOR);
            }
            builder.append(componentName.flattenToShortString());
        }
        long identity = Binder.clearCallingIdentity();
        try {
            String settingValue = builder.toString();
            Settings.Secure.putStringForUser(this.mContext.getContentResolver(), settingName, TextUtils.isEmpty(settingValue) ? null : settingValue, userId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* JADX INFO: Multiple debug info for r14v7 'service'  com.android.server.accessibility.AccessibilityServiceConnection: [D('service' com.android.server.accessibility.AccessibilityServiceConnection), D('componentNameToServiceMap' java.util.Map<android.content.ComponentName, com.android.server.accessibility.AccessibilityServiceConnection>)] */
    private void updateServicesLocked(UserState userState) {
        int i;
        Map<ComponentName, AccessibilityServiceConnection> componentNameToServiceMap;
        int count;
        AccessibilityServiceConnection service;
        AccessibilityServiceConnection service2;
        Map<ComponentName, AccessibilityServiceConnection> componentNameToServiceMap2 = userState.mComponentNameToServiceMap;
        boolean isUnlockingOrUnlocked = ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class)).isUserUnlockingOrUnlocked(userState.mUserId);
        int count2 = userState.mInstalledServices.size();
        int i2 = 0;
        while (true) {
            if (i2 >= count2) {
                break;
            } else if (i2 >= userState.mInstalledServices.size()) {
                break;
            } else {
                AccessibilityServiceInfo installedService = userState.mInstalledServices.get(i2);
                ComponentName componentName = ComponentName.unflattenFromString(installedService.getId());
                AccessibilityServiceConnection service3 = componentNameToServiceMap2.get(componentName);
                if (!isUnlockingOrUnlocked && !installedService.isDirectBootAware()) {
                    Slog.d(LOG_TAG, "Ignoring non-encryption-aware service " + componentName);
                    i = i2;
                    count = count2;
                    componentNameToServiceMap = componentNameToServiceMap2;
                } else if (userState.mBindingServices.contains(componentName)) {
                    i = i2;
                    count = count2;
                    componentNameToServiceMap = componentNameToServiceMap2;
                } else {
                    if (!userState.mEnabledServices.contains(componentName)) {
                        i = i2;
                        count = count2;
                        componentNameToServiceMap = componentNameToServiceMap2;
                        service = service3;
                    } else if (!this.mUiAutomationManager.suppressingAccessibilityServicesLocked()) {
                        if (service3 == null) {
                            Context context = this.mContext;
                            int i3 = sIdCounter;
                            sIdCounter = i3 + 1;
                            componentNameToServiceMap = componentNameToServiceMap2;
                            i = i2;
                            count = count2;
                            service2 = new AccessibilityServiceConnection(userState, context, componentName, installedService, i3, this.mMainHandler, this.mLock, this.mSecurityPolicy, this, this.mWindowManagerService, this.mGlobalActionPerformer);
                        } else {
                            i = i2;
                            count = count2;
                            componentNameToServiceMap = componentNameToServiceMap2;
                            service2 = service3;
                            if (userState.mBoundServices.contains(service2)) {
                            }
                        }
                        service2.bindLocked();
                    } else {
                        i = i2;
                        count = count2;
                        componentNameToServiceMap = componentNameToServiceMap2;
                        service = service3;
                    }
                    if (service != null) {
                        service.unbindLocked();
                    }
                }
                i2 = i + 1;
                count2 = count;
                componentNameToServiceMap2 = componentNameToServiceMap;
            }
        }
        int count3 = userState.mBoundServices.size();
        this.mTempIntArray.clear();
        for (int i4 = 0; i4 < count3; i4++) {
            ResolveInfo resolveInfo = userState.mBoundServices.get(i4).mAccessibilityServiceInfo.getResolveInfo();
            if (resolveInfo != null) {
                this.mTempIntArray.add(resolveInfo.serviceInfo.applicationInfo.uid);
            }
        }
        AudioManagerInternal audioManager = (AudioManagerInternal) LocalServices.getService(AudioManagerInternal.class);
        if (audioManager != null) {
            audioManager.setAccessibilityServiceUids(this.mTempIntArray);
        }
        updateAccessibilityEnabledSetting(userState);
    }

    private void scheduleUpdateClientsIfNeededLocked(UserState userState) {
        int clientState = userState.getClientState();
        if (userState.mLastSentClientState == clientState) {
            return;
        }
        if (this.mGlobalClients.getRegisteredCallbackCount() > 0 || userState.mUserClients.getRegisteredCallbackCount() > 0) {
            userState.mLastSentClientState = clientState;
            this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$5vwr6qVeqdCr73CeDmVnsJlZHM.INSTANCE, this, Integer.valueOf(clientState), Integer.valueOf(userState.mUserId)));
        }
    }

    /* access modifiers changed from: private */
    public void sendStateToAllClients(int clientState, int userId) {
        sendStateToClients(clientState, this.mGlobalClients);
        sendStateToClients(clientState, userId);
    }

    /* access modifiers changed from: private */
    public void sendStateToClients(int clientState, int userId) {
        sendStateToClients(clientState, getUserState(userId).mUserClients);
    }

    private void sendStateToClients(int clientState, RemoteCallbackList<IAccessibilityManagerClient> clients) {
        clients.broadcast(FunctionalUtils.ignoreRemoteException(new FunctionalUtils.RemoteExceptionIgnoringConsumer(clientState) {
            /* class com.android.server.accessibility.$$Lambda$AccessibilityManagerService$K4sS36agT2_B03tVUTy8mldugxY */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final void acceptOrThrow(Object obj) {
                ((IAccessibilityManagerClient) obj).setState(this.f$0);
            }
        }));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleNotifyClientsOfServicesStateChangeLocked(UserState userState) {
        updateRecommendedUiTimeoutLocked(userState);
        this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$heq1MRdQjg8BGWFbpV3PEpnDVcg.INSTANCE, this, userState.mUserClients, Long.valueOf(getRecommendedTimeoutMillisLocked(userState))));
    }

    /* access modifiers changed from: private */
    public void sendServicesStateChanged(RemoteCallbackList<IAccessibilityManagerClient> userClients, long uiTimeout) {
        notifyClientsOfServicesStateChange(this.mGlobalClients, uiTimeout);
        notifyClientsOfServicesStateChange(userClients, uiTimeout);
    }

    private void notifyClientsOfServicesStateChange(RemoteCallbackList<IAccessibilityManagerClient> clients, long uiTimeout) {
        clients.broadcast(FunctionalUtils.ignoreRemoteException(new FunctionalUtils.RemoteExceptionIgnoringConsumer(uiTimeout) {
            /* class com.android.server.accessibility.$$Lambda$AccessibilityManagerService$_rvRsbhZRBJitXrpMqI0NptLUa8 */
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            public final void acceptOrThrow(Object obj) {
                ((IAccessibilityManagerClient) obj).notifyServicesStateChanged(this.f$0);
            }
        }));
    }

    private void scheduleUpdateInputFilter(UserState userState) {
        this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$w0ifSldCn8nADYgU7v1foSdmfe0.INSTANCE, this, userState));
    }

    private void scheduleUpdateFingerprintGestureHandling(UserState userState) {
        this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$mAPLBShddfLlktd9Q8jVo04VVXo.INSTANCE, this, userState));
    }

    /* access modifiers changed from: private */
    public void updateInputFilter(UserState userState) {
        if (!this.mUiAutomationManager.suppressingAccessibilityServicesLocked()) {
            boolean setInputFilter = false;
            AccessibilityInputFilter inputFilter = null;
            synchronized (this.mLock) {
                int flags = 0;
                if (userState.mIsDisplayMagnificationEnabled) {
                    flags = 0 | 1;
                }
                if (userState.mIsNavBarMagnificationEnabled) {
                    flags |= 64;
                }
                if (userHasMagnificationServicesLocked(userState)) {
                    flags |= 32;
                }
                if (userState.isHandlingAccessibilityEvents() && userState.mIsTouchExplorationEnabled) {
                    flags |= 2;
                }
                if (userState.mIsFilterKeyEventsEnabled) {
                    flags |= 4;
                }
                if (userState.mIsAutoclickEnabled) {
                    flags |= 8;
                }
                if (userState.mIsPerformGesturesEnabled) {
                    flags |= 16;
                }
                if (flags != 0) {
                    if (!this.mHasInputFilter) {
                        this.mHasInputFilter = true;
                        if (this.mInputFilter == null) {
                            this.mInputFilter = new AccessibilityInputFilter(this.mContext, this);
                        }
                        inputFilter = this.mInputFilter;
                        setInputFilter = true;
                    }
                    this.mInputFilter.setUserAndEnabledFeatures(userState.mUserId, flags);
                } else if (this.mHasInputFilter) {
                    this.mHasInputFilter = false;
                    this.mInputFilter.setUserAndEnabledFeatures(userState.mUserId, 0);
                    inputFilter = null;
                    setInputFilter = true;
                }
            }
            if (setInputFilter) {
                Slog.i(LOG_TAG, "volume_debug  setInputFilter");
                this.mWindowManagerService.setInputFilter(inputFilter);
            }
        }
    }

    /* access modifiers changed from: private */
    public void showEnableTouchExplorationDialog(final AccessibilityServiceConnection service) {
        synchronized (this.mLock) {
            String label = service.getServiceInfo().getResolveInfo().loadLabel(this.mContext.getPackageManager()).toString();
            final UserState userState = getCurrentUserStateLocked();
            if (!userState.mIsTouchExplorationEnabled) {
                if (this.mEnableTouchExplorationDialog == null || !this.mEnableTouchExplorationDialog.isShowing()) {
                    this.mEnableTouchExplorationDialog = new AlertDialog.Builder(this.mContext).setIconAttribute(16843605).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                        /* class com.android.server.accessibility.AccessibilityManagerService.AnonymousClass4 */

                        /* JADX INFO: finally extract failed */
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialog, int which) {
                            userState.mTouchExplorationGrantedServices.add(service.mComponentName);
                            AccessibilityManagerService.this.persistComponentNamesToSettingLocked("touch_exploration_granted_accessibility_services", userState.mTouchExplorationGrantedServices, userState.mUserId);
                            userState.mIsTouchExplorationEnabled = true;
                            long identity = Binder.clearCallingIdentity();
                            try {
                                Settings.Secure.putIntForUser(AccessibilityManagerService.this.mContext.getContentResolver(), "touch_exploration_enabled", 1, userState.mUserId);
                                Binder.restoreCallingIdentity(identity);
                                AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                            } catch (Throwable th) {
                                Binder.restoreCallingIdentity(identity);
                                throw th;
                            }
                        }
                    }).setNegativeButton(17039360, new DialogInterface.OnClickListener() {
                        /* class com.android.server.accessibility.AccessibilityManagerService.AnonymousClass3 */

                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setTitle(17040035).setMessage(this.mContext.getString(17040034, label)).create();
                    this.mEnableTouchExplorationDialog.getWindow().setType(2003);
                    this.mEnableTouchExplorationDialog.getWindow().getAttributes().privateFlags |= 16;
                    this.mEnableTouchExplorationDialog.setCanceledOnTouchOutside(true);
                    this.mEnableTouchExplorationDialog.show();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserStateChangedLocked(UserState userState) {
        this.mInitialized = true;
        updateLegacyCapabilitiesLocked(userState);
        updateServicesLocked(userState);
        updateAccessibilityShortcutLocked(userState);
        updateWindowsForAccessibilityCallbackLocked(userState);
        updateAccessibilityFocusBehaviorLocked(userState);
        updateFilterKeyEventsLocked(userState);
        updateTouchExplorationLocked(userState);
        updatePerformGesturesLocked(userState);
        updateMagnificationLocked(userState);
        scheduleUpdateFingerprintGestureHandling(userState);
        scheduleUpdateInputFilter(userState);
        updateRelevantEventsLocked(userState);
        scheduleUpdateClientsIfNeededLocked(userState);
        updateAccessibilityButtonTargetsLocked(userState);
    }

    private void updateAccessibilityFocusBehaviorLocked(UserState userState) {
        List<AccessibilityServiceConnection> boundServices = userState.mBoundServices;
        int boundServiceCount = boundServices.size();
        for (int i = 0; i < boundServiceCount; i++) {
            if (boundServices.get(i).canRetrieveInteractiveWindowsLocked()) {
                userState.mAccessibilityFocusOnlyInActiveWindow = false;
                return;
            }
        }
        userState.mAccessibilityFocusOnlyInActiveWindow = true;
    }

    private void updateWindowsForAccessibilityCallbackLocked(UserState userState) {
        boolean observingWindows = this.mUiAutomationManager.canRetrieveInteractiveWindowsLocked();
        List<AccessibilityServiceConnection> boundServices = userState.mBoundServices;
        int boundServiceCount = boundServices.size();
        int i = 0;
        while (!observingWindows && i < boundServiceCount) {
            if (boundServices.get(i).canRetrieveInteractiveWindowsLocked()) {
                observingWindows = true;
            }
            i++;
        }
        if (observingWindows) {
            if (this.mWindowsForAccessibilityCallback == null) {
                this.mWindowsForAccessibilityCallback = new WindowsForAccessibilityCallback();
                this.mWindowManagerService.setWindowsForAccessibilityCallback(this.mWindowsForAccessibilityCallback);
            }
        } else if (this.mWindowsForAccessibilityCallback != null) {
            this.mWindowsForAccessibilityCallback = null;
            this.mWindowManagerService.setWindowsForAccessibilityCallback((WindowManagerInternal.WindowsForAccessibilityCallback) null);
            this.mSecurityPolicy.clearWindowsLocked();
        }
    }

    private void updateLegacyCapabilitiesLocked(UserState userState) {
        int installedServiceCount = userState.mInstalledServices.size();
        int i = 0;
        while (i < installedServiceCount && i < userState.mInstalledServices.size()) {
            AccessibilityServiceInfo serviceInfo = userState.mInstalledServices.get(i);
            if (serviceInfo != null) {
                ResolveInfo resolveInfo = serviceInfo.getResolveInfo();
                if ((serviceInfo.getCapabilities() & 2) == 0 && resolveInfo.serviceInfo.applicationInfo.targetSdkVersion <= 17) {
                    if (userState.mTouchExplorationGrantedServices.contains(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name))) {
                        serviceInfo.setCapabilities(serviceInfo.getCapabilities() | 2);
                    }
                }
            }
            i++;
        }
    }

    private void updatePerformGesturesLocked(UserState userState) {
        int serviceCount = userState.mBoundServices.size();
        for (int i = 0; i < serviceCount; i++) {
            if ((userState.mBoundServices.get(i).getCapabilities() & 32) != 0) {
                userState.mIsPerformGesturesEnabled = true;
                return;
            }
        }
        userState.mIsPerformGesturesEnabled = false;
    }

    private void updateFilterKeyEventsLocked(UserState userState) {
        int serviceCount = userState.mBoundServices.size();
        for (int i = 0; i < serviceCount; i++) {
            AccessibilityServiceConnection service = userState.mBoundServices.get(i);
            if (service.mRequestFilterKeyEvents && (service.getCapabilities() & 8) != 0) {
                Slog.i(LOG_TAG, "volume_debug the the filter package:" + service.mComponentName.getPackageName());
                userState.mIsFilterKeyEventsEnabled = true;
                return;
            }
        }
        userState.mIsFilterKeyEventsEnabled = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean readConfigurationForUserStateLocked(UserState userState) {
        return readInstalledAccessibilityServiceLocked(userState) | readEnabledAccessibilityServicesLocked(userState) | readTouchExplorationGrantedAccessibilityServicesLocked(userState) | readTouchExplorationEnabledSettingLocked(userState) | readHighTextContrastEnabledSettingLocked(userState) | readMagnificationEnabledSettingsLocked(userState) | readAutoclickEnabledSettingLocked(userState) | readAccessibilityShortcutSettingLocked(userState) | readAccessibilityButtonSettingsLocked(userState) | readUserRecommendedUiTimeoutSettingsLocked(userState);
    }

    private void updateAccessibilityEnabledSetting(UserState userState) {
        long identity = Binder.clearCallingIdentity();
        int i = 0;
        boolean isA11yEnabled = this.mUiAutomationManager.isUiAutomationRunningLocked() || userState.isHandlingAccessibilityEvents();
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (isA11yEnabled) {
                i = 1;
            }
            Settings.Secure.putIntForUser(contentResolver, "accessibility_enabled", i, userState.mUserId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean readTouchExplorationEnabledSettingLocked(UserState userState) {
        boolean touchExplorationEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "touch_exploration_enabled", 0, userState.mUserId) == 1;
        if (touchExplorationEnabled == userState.mIsTouchExplorationEnabled) {
            return false;
        }
        userState.mIsTouchExplorationEnabled = touchExplorationEnabled;
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean readMagnificationEnabledSettingsLocked(UserState userState) {
        boolean displayMagnificationEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0, userState.mUserId) == 1;
        boolean navBarMagnificationEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_navbar_enabled", 0, userState.mUserId) == 1;
        if (displayMagnificationEnabled == userState.mIsDisplayMagnificationEnabled && navBarMagnificationEnabled == userState.mIsNavBarMagnificationEnabled) {
            return false;
        }
        userState.mIsDisplayMagnificationEnabled = displayMagnificationEnabled;
        userState.mIsNavBarMagnificationEnabled = navBarMagnificationEnabled;
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean readAutoclickEnabledSettingLocked(UserState userState) {
        boolean autoclickEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_autoclick_enabled", 0, userState.mUserId) == 1;
        if (autoclickEnabled == userState.mIsAutoclickEnabled) {
            return false;
        }
        userState.mIsAutoclickEnabled = autoclickEnabled;
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean readHighTextContrastEnabledSettingLocked(UserState userState) {
        boolean highTextContrastEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "high_text_contrast_enabled", 0, userState.mUserId) == 1;
        if (highTextContrastEnabled == userState.mIsTextHighContrastEnabled) {
            return false;
        }
        userState.mIsTextHighContrastEnabled = highTextContrastEnabled;
        return true;
    }

    private void updateTouchExplorationLocked(UserState userState) {
        boolean enabled = this.mUiAutomationManager.isTouchExplorationEnabledLocked();
        int serviceCount = userState.mBoundServices.size();
        int i = 0;
        while (true) {
            if (i >= serviceCount) {
                break;
            } else if (canRequestAndRequestsTouchExplorationLocked(userState.mBoundServices.get(i), userState)) {
                enabled = true;
                break;
            } else {
                i++;
            }
        }
        if (enabled != userState.mIsTouchExplorationEnabled) {
            userState.mIsTouchExplorationEnabled = enabled;
            long identity = Binder.clearCallingIdentity();
            try {
                Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "touch_exploration_enabled", enabled ? 1 : 0, userState.mUserId);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean readAccessibilityShortcutSettingLocked(UserState userState) {
        String componentNameToEnableString = AccessibilityShortcutController.getTargetServiceComponentNameString(this.mContext, userState.mUserId);
        if (componentNameToEnableString != null && !componentNameToEnableString.isEmpty()) {
            ComponentName componentNameToEnable = ComponentName.unflattenFromString(componentNameToEnableString);
            if (componentNameToEnable != null && componentNameToEnable.equals(userState.mServiceToEnableWithShortcut)) {
                return false;
            }
            userState.mServiceToEnableWithShortcut = componentNameToEnable;
            scheduleNotifyClientsOfServicesStateChangeLocked(userState);
            return true;
        } else if (userState.mServiceToEnableWithShortcut == null) {
            return false;
        } else {
            userState.mServiceToEnableWithShortcut = null;
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean readAccessibilityButtonSettingsLocked(UserState userState) {
        String componentId = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "accessibility_button_target_component", userState.mUserId);
        if (TextUtils.isEmpty(componentId)) {
            if (userState.mServiceAssignedToAccessibilityButton == null && !userState.mIsNavBarMagnificationAssignedToAccessibilityButton) {
                return false;
            }
            userState.mServiceAssignedToAccessibilityButton = null;
            userState.mIsNavBarMagnificationAssignedToAccessibilityButton = false;
            return true;
        } else if (!componentId.equals(MagnificationController.class.getName())) {
            ComponentName componentName = ComponentName.unflattenFromString(componentId);
            if (Objects.equals(componentName, userState.mServiceAssignedToAccessibilityButton)) {
                return false;
            }
            userState.mServiceAssignedToAccessibilityButton = componentName;
            userState.mIsNavBarMagnificationAssignedToAccessibilityButton = false;
            return true;
        } else if (userState.mIsNavBarMagnificationAssignedToAccessibilityButton) {
            return false;
        } else {
            userState.mServiceAssignedToAccessibilityButton = null;
            userState.mIsNavBarMagnificationAssignedToAccessibilityButton = true;
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean readUserRecommendedUiTimeoutSettingsLocked(UserState userState) {
        int nonInteractiveUiTimeout = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_non_interactive_ui_timeout_ms", 0, userState.mUserId);
        int interactiveUiTimeout = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_interactive_ui_timeout_ms", 0, userState.mUserId);
        if (nonInteractiveUiTimeout == userState.mUserNonInteractiveUiTimeout && interactiveUiTimeout == userState.mUserInteractiveUiTimeout) {
            return false;
        }
        userState.mUserNonInteractiveUiTimeout = nonInteractiveUiTimeout;
        userState.mUserInteractiveUiTimeout = interactiveUiTimeout;
        scheduleNotifyClientsOfServicesStateChangeLocked(userState);
        return true;
    }

    private void updateAccessibilityShortcutLocked(UserState userState) {
        if (userState.mServiceToEnableWithShortcut != null) {
            boolean shortcutServiceIsInstalled = AccessibilityShortcutController.getFrameworkShortcutFeaturesMap().containsKey(userState.mServiceToEnableWithShortcut);
            int i = 0;
            while (!shortcutServiceIsInstalled && i < userState.mInstalledServices.size()) {
                if (userState.mInstalledServices.get(i).getComponentName().equals(userState.mServiceToEnableWithShortcut)) {
                    shortcutServiceIsInstalled = true;
                }
                i++;
            }
            if (!shortcutServiceIsInstalled) {
                userState.mServiceToEnableWithShortcut = null;
                long identity = Binder.clearCallingIdentity();
                try {
                    Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "accessibility_shortcut_target_service", null, userState.mUserId);
                    Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "accessibility_shortcut_enabled", 0, userState.mUserId);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    private boolean canRequestAndRequestsTouchExplorationLocked(AccessibilityServiceConnection service, UserState userState) {
        if (!service.canReceiveEventsLocked() || !service.mRequestTouchExplorationMode) {
            return false;
        }
        if (service.getServiceInfo().getResolveInfo().serviceInfo.applicationInfo.targetSdkVersion <= 17) {
            if (userState.mTouchExplorationGrantedServices.contains(service.mComponentName)) {
                return true;
            }
            AlertDialog alertDialog = this.mEnableTouchExplorationDialog;
            if (alertDialog == null || !alertDialog.isShowing()) {
                this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$bNCuysjTCG2afhYMHuqu25CfY5g.INSTANCE, this, service));
            }
        } else if ((service.getCapabilities() & 2) != 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMagnificationLocked(UserState userState) {
        MagnificationController magnificationController;
        if (userState.mUserId == this.mCurrentUserId) {
            if (!this.mUiAutomationManager.suppressingAccessibilityServicesLocked() || (magnificationController = this.mMagnificationController) == null) {
                ArrayList<Display> displays = getValidDisplayList();
                if (userState.mIsDisplayMagnificationEnabled || userState.mIsNavBarMagnificationEnabled) {
                    for (int i = 0; i < displays.size(); i++) {
                        getMagnificationController().register(displays.get(i).getDisplayId());
                    }
                    return;
                }
                for (int i2 = 0; i2 < displays.size(); i2++) {
                    int displayId = displays.get(i2).getDisplayId();
                    if (userHasListeningMagnificationServicesLocked(userState, displayId)) {
                        getMagnificationController().register(displayId);
                    } else {
                        MagnificationController magnificationController2 = this.mMagnificationController;
                        if (magnificationController2 != null) {
                            magnificationController2.unregister(displayId);
                        }
                    }
                }
                return;
            }
            magnificationController.unregisterAll();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean userHasMagnificationServicesLocked(UserState userState) {
        List<AccessibilityServiceConnection> services = userState.mBoundServices;
        int count = services.size();
        for (int i = 0; i < count; i++) {
            if (this.mSecurityPolicy.canControlMagnification(services.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean userHasListeningMagnificationServicesLocked(UserState userState, int displayId) {
        List<AccessibilityServiceConnection> services = userState.mBoundServices;
        int count = services.size();
        for (int i = 0; i < count; i++) {
            AccessibilityServiceConnection service = services.get(i);
            if (this.mSecurityPolicy.canControlMagnification(service) && service.isMagnificationCallbackEnabled(displayId)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void updateFingerprintGestureHandling(UserState userState) {
        List<AccessibilityServiceConnection> services;
        synchronized (this.mLock) {
            services = userState.mBoundServices;
            if (this.mFingerprintGestureDispatcher == null && this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                int numServices = services.size();
                int i = 0;
                while (true) {
                    if (i >= numServices) {
                        break;
                    }
                    if (services.get(i).isCapturingFingerprintGestures()) {
                        long identity = Binder.clearCallingIdentity();
                        try {
                            IFingerprintService service = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));
                            if (service != null) {
                                this.mFingerprintGestureDispatcher = new FingerprintGestureDispatcher(service, this.mContext.getResources(), this.mLock);
                                break;
                            }
                        } finally {
                            Binder.restoreCallingIdentity(identity);
                        }
                    }
                    i++;
                }
            }
        }
        FingerprintGestureDispatcher fingerprintGestureDispatcher = this.mFingerprintGestureDispatcher;
        if (fingerprintGestureDispatcher != null) {
            fingerprintGestureDispatcher.updateClientList(services);
        }
    }

    private void updateAccessibilityButtonTargetsLocked(UserState userState) {
        for (int i = userState.mBoundServices.size() - 1; i >= 0; i--) {
            AccessibilityServiceConnection service = userState.mBoundServices.get(i);
            if (service.mRequestAccessibilityButton) {
                service.notifyAccessibilityButtonAvailabilityChangedLocked(service.isAccessibilityButtonAvailableLocked(userState));
            }
        }
    }

    private void updateRecommendedUiTimeoutLocked(UserState userState) {
        int newNonInteractiveUiTimeout = userState.mUserNonInteractiveUiTimeout;
        int newInteractiveUiTimeout = userState.mUserInteractiveUiTimeout;
        if (newNonInteractiveUiTimeout == 0 || newInteractiveUiTimeout == 0) {
            int serviceNonInteractiveUiTimeout = 0;
            int serviceInteractiveUiTimeout = 0;
            List<AccessibilityServiceConnection> services = userState.mBoundServices;
            for (int i = 0; i < services.size(); i++) {
                int timeout = services.get(i).getServiceInfo().getInteractiveUiTimeoutMillis();
                if (serviceInteractiveUiTimeout < timeout) {
                    serviceInteractiveUiTimeout = timeout;
                }
                int timeout2 = services.get(i).getServiceInfo().getNonInteractiveUiTimeoutMillis();
                if (serviceNonInteractiveUiTimeout < timeout2) {
                    serviceNonInteractiveUiTimeout = timeout2;
                }
            }
            if (newNonInteractiveUiTimeout == 0) {
                newNonInteractiveUiTimeout = serviceNonInteractiveUiTimeout;
            }
            if (newInteractiveUiTimeout == 0) {
                newInteractiveUiTimeout = serviceInteractiveUiTimeout;
            }
        }
        userState.mNonInteractiveUiTimeout = newNonInteractiveUiTimeout;
        userState.mInteractiveUiTimeout = newInteractiveUiTimeout;
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    @GuardedBy({"mLock"})
    public MagnificationSpec getCompatibleMagnificationSpecLocked(int windowId) {
        IBinder windowToken = this.mGlobalWindowTokens.get(windowId);
        if (windowToken == null) {
            windowToken = getCurrentUserStateLocked().mWindowTokens.get(windowId);
        }
        if (windowToken != null) {
            return this.mWindowManagerService.getCompatibleMagnificationSpecForWindow(windowToken);
        }
        return null;
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public KeyEventDispatcher getKeyEventDispatcher() {
        if (this.mKeyEventDispatcher == null) {
            this.mKeyEventDispatcher = new KeyEventDispatcher(this.mMainHandler, 8, this.mLock, this.mPowerManager);
        }
        return this.mKeyEventDispatcher;
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public PendingIntent getPendingIntentActivity(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getActivity(context, requestCode, intent, flags);
    }

    public void performAccessibilityShortcut() {
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000 || this.mContext.checkCallingPermission("android.permission.MANAGE_ACCESSIBILITY") == 0) {
            Map<ComponentName, AccessibilityShortcutController.ToggleableFrameworkFeatureInfo> frameworkFeatureMap = AccessibilityShortcutController.getFrameworkShortcutFeaturesMap();
            synchronized (this.mLock) {
                UserState userState = getUserStateLocked(this.mCurrentUserId);
                ComponentName serviceName = userState.mServiceToEnableWithShortcut;
                if (serviceName != null) {
                    if (frameworkFeatureMap.containsKey(serviceName)) {
                        AccessibilityShortcutController.ToggleableFrameworkFeatureInfo featureInfo = frameworkFeatureMap.get(serviceName);
                        SettingsStringUtil.SettingStringHelper setting = new SettingsStringUtil.SettingStringHelper(this.mContext.getContentResolver(), featureInfo.getSettingKey(), this.mCurrentUserId);
                        if (!TextUtils.equals(featureInfo.getSettingOnValue(), setting.read())) {
                            setting.write(featureInfo.getSettingOnValue());
                        } else {
                            setting.write(featureInfo.getSettingOffValue());
                        }
                    }
                    long identity = Binder.clearCallingIdentity();
                    try {
                        if (userState.mComponentNameToServiceMap.get(serviceName) == null) {
                            enableAccessibilityServiceLocked(serviceName, this.mCurrentUserId);
                        } else {
                            disableAccessibilityServiceLocked(serviceName, this.mCurrentUserId);
                        }
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        } else {
            throw new SecurityException("performAccessibilityShortcut requires the MANAGE_ACCESSIBILITY permission");
        }
    }

    public String getAccessibilityShortcutService() {
        String flattenToString;
        if (this.mContext.checkCallingPermission("android.permission.MANAGE_ACCESSIBILITY") == 0) {
            synchronized (this.mLock) {
                flattenToString = getUserStateLocked(this.mCurrentUserId).mServiceToEnableWithShortcut.flattenToString();
            }
            return flattenToString;
        }
        throw new SecurityException("getAccessibilityShortcutService requires the MANAGE_ACCESSIBILITY permission");
    }

    private void enableAccessibilityServiceLocked(ComponentName componentName, int userId) {
        SettingsStringUtil.SettingStringHelper setting = new SettingsStringUtil.SettingStringHelper(this.mContext.getContentResolver(), "enabled_accessibility_services", userId);
        setting.write(SettingsStringUtil.ComponentNameSet.add(setting.read(), componentName));
        UserState userState = getUserStateLocked(userId);
        if (userState.mEnabledServices.add(componentName)) {
            onUserStateChangedLocked(userState);
        }
    }

    private void disableAccessibilityServiceLocked(ComponentName componentName, int userId) {
        SettingsStringUtil.SettingStringHelper setting = new SettingsStringUtil.SettingStringHelper(this.mContext.getContentResolver(), "enabled_accessibility_services", userId);
        setting.write(SettingsStringUtil.ComponentNameSet.remove(setting.read(), componentName));
        UserState userState = getUserStateLocked(userId);
        if (userState.mEnabledServices.remove(componentName)) {
            onUserStateChangedLocked(userState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAccessibilityEventLocked(AccessibilityEvent event, int userId) {
        event.setEventTime(SystemClock.uptimeMillis());
        this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$X8i00nfnUx_qUoIgZixkfu6ddSY.INSTANCE, this, event, Integer.valueOf(userId)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTalkBackService(ComponentName serviceName) {
        if (serviceName != null && TALKBACK_SERVICE_PKTNAME.equals(serviceName.getPackageName()) && TALKBACK_SERVICE_CLSNAME.equals(serviceName.getClassName())) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isScreenReaderService(ComponentName serviceName) {
        if (serviceName != null && SCREENREADER_SERVICE_PKGNAME.equals(serviceName.getPackageName()) && SCREENREADER_SERVICE_CLSNAME.equals(serviceName.getClassName())) {
            return true;
        }
        return false;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setScreenReaderState(int value) {
        long identity = Binder.clearCallingIdentity();
        try {
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, value, getCurrentUserIdLocked());
            Binder.restoreCallingIdentity(identity);
            Slog.i(LOG_TAG, "setScreenReaderState value : " + value);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getScreenReaderState() {
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), ACCESSIBILITY_SCREENREADER_ENABLED, 0, getCurrentUserIdLocked()) == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAccessibilityEnableChangedEvent(int enable) {
        Intent intent = new Intent(TALKBACK_SETTINGS_ACTION);
        intent.setComponent(new ComponentName(SETTING_PKGNAME, SETTING_CLSNAME));
        intent.putExtra(SETTING_TALKBACK_STATE, enable);
        long identity = Binder.clearCallingIdentity();
        try {
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        } catch (Exception e) {
            Slog.e(LOG_TAG, "start setting server err");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        Slog.i(LOG_TAG, "send com.android.settings.talkback_state enable " + enable);
    }

    public boolean sendFingerprintGesture(int gestureKeyCode) {
        synchronized (this.mLock) {
            if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
                throw new SecurityException("Only SYSTEM can call sendFingerprintGesture");
            }
        }
        FingerprintGestureDispatcher fingerprintGestureDispatcher = this.mFingerprintGestureDispatcher;
        if (fingerprintGestureDispatcher == null) {
            return false;
        }
        return fingerprintGestureDispatcher.onFingerprintGesture(gestureKeyCode);
    }

    public int getAccessibilityWindowId(IBinder windowToken) {
        int findWindowIdLocked;
        synchronized (this.mLock) {
            if (UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
                findWindowIdLocked = findWindowIdLocked(windowToken);
            } else {
                throw new SecurityException("Only SYSTEM can call getAccessibilityWindowId");
            }
        }
        return findWindowIdLocked;
    }

    public long getRecommendedTimeoutMillis() {
        long recommendedTimeoutMillisLocked;
        synchronized (this.mLock) {
            recommendedTimeoutMillisLocked = getRecommendedTimeoutMillisLocked(getCurrentUserStateLocked());
        }
        return recommendedTimeoutMillisLocked;
    }

    private long getRecommendedTimeoutMillisLocked(UserState userState) {
        return IntPair.of(userState.mInteractiveUiTimeout, userState.mNonInteractiveUiTimeout);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, LOG_TAG, pw)) {
            synchronized (this.mLock) {
                pw.println("ACCESSIBILITY MANAGER (dumpsys accessibility)");
                pw.println();
                int userCount = this.mUserStates.size();
                for (int i = 0; i < userCount; i++) {
                    UserState userState = this.mUserStates.valueAt(i);
                    pw.append((CharSequence) ("User state[attributes:{id=" + userState.mUserId));
                    StringBuilder sb = new StringBuilder();
                    sb.append(", currentUser=");
                    sb.append(userState.mUserId == this.mCurrentUserId);
                    pw.append((CharSequence) sb.toString());
                    pw.append((CharSequence) (", touchExplorationEnabled=" + userState.mIsTouchExplorationEnabled));
                    pw.append((CharSequence) (", displayMagnificationEnabled=" + userState.mIsDisplayMagnificationEnabled));
                    pw.append((CharSequence) (", navBarMagnificationEnabled=" + userState.mIsNavBarMagnificationEnabled));
                    pw.append((CharSequence) (", autoclickEnabled=" + userState.mIsAutoclickEnabled));
                    pw.append((CharSequence) (", nonInteractiveUiTimeout=" + userState.mNonInteractiveUiTimeout));
                    pw.append((CharSequence) (", interactiveUiTimeout=" + userState.mInteractiveUiTimeout));
                    pw.append((CharSequence) (", installedServiceCount=" + userState.mInstalledServices.size()));
                    if (this.mUiAutomationManager.isUiAutomationRunningLocked()) {
                        pw.append(", ");
                        this.mUiAutomationManager.dumpUiAutomationService(fd, pw, args);
                        pw.println();
                    }
                    pw.append("}");
                    pw.println();
                    pw.append("     Bound services:{");
                    int serviceCount = userState.mBoundServices.size();
                    for (int j = 0; j < serviceCount; j++) {
                        if (j > 0) {
                            pw.append(", ");
                            pw.println();
                            pw.append("                     ");
                        }
                        userState.mBoundServices.get(j).dump(fd, pw, args);
                    }
                    pw.println("}");
                    pw.append("     Enabled services:{");
                    Iterator<ComponentName> it = userState.mEnabledServices.iterator();
                    if (it.hasNext()) {
                        pw.append((CharSequence) it.next().toShortString());
                        while (it.hasNext()) {
                            pw.append(", ");
                            pw.append((CharSequence) it.next().toShortString());
                        }
                    }
                    pw.println("}");
                    pw.append("     Binding services:{");
                    Iterator<ComponentName> it2 = userState.mBindingServices.iterator();
                    if (it2.hasNext()) {
                        pw.append((CharSequence) it2.next().toShortString());
                        while (it2.hasNext()) {
                            pw.append(", ");
                            pw.append((CharSequence) it2.next().toShortString());
                        }
                    }
                    pw.println("}]");
                    pw.println();
                }
                if (this.mSecurityPolicy.mWindows != null) {
                    int windowCount = this.mSecurityPolicy.mWindows.size();
                    for (int j2 = 0; j2 < windowCount; j2++) {
                        if (j2 > 0) {
                            pw.append(',');
                            pw.println();
                        }
                        pw.append("Window[");
                        pw.append((CharSequence) this.mSecurityPolicy.mWindows.get(j2).toString());
                        pw.append(']');
                    }
                    pw.println();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void putSecureIntForUser(String key, int value, int userid) {
        long identity = Binder.clearCallingIdentity();
        try {
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), key, value, userid);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: package-private */
    public class RemoteAccessibilityConnection implements IBinder.DeathRecipient {
        private final IAccessibilityInteractionConnection mConnection;
        private final String mPackageName;
        private final int mUid;
        private final int mUserId;
        private final int mWindowId;

        RemoteAccessibilityConnection(int windowId, IAccessibilityInteractionConnection connection, String packageName, int uid, int userId) {
            this.mWindowId = windowId;
            this.mPackageName = packageName;
            this.mUid = uid;
            this.mUserId = userId;
            this.mConnection = connection;
        }

        public int getUid() {
            return this.mUid;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public IAccessibilityInteractionConnection getRemote() {
            return this.mConnection;
        }

        public void linkToDeath() throws RemoteException {
            this.mConnection.asBinder().linkToDeath(this, 0);
        }

        public void unlinkToDeath() {
            this.mConnection.asBinder().unlinkToDeath(this, 0);
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            unlinkToDeath();
            synchronized (AccessibilityManagerService.this.mLock) {
                AccessibilityManagerService.this.removeAccessibilityInteractionConnectionLocked(this.mWindowId, this.mUserId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class MainHandler extends Handler {
        public static final int MSG_SEND_KEY_EVENT_TO_INPUT_FILTER = 8;

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 8) {
                KeyEvent event = (KeyEvent) msg.obj;
                int policyFlags = msg.arg1;
                synchronized (AccessibilityManagerService.this.mLock) {
                    if (AccessibilityManagerService.this.mHasInputFilter && AccessibilityManagerService.this.mInputFilter != null) {
                        AccessibilityManagerService.this.mInputFilter.sendInputEvent(event, policyFlags);
                    }
                }
                event.recycle();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearAccessibilityFocus(IntSupplier windowId) {
        clearAccessibilityFocus(windowId.getAsInt());
    }

    /* access modifiers changed from: package-private */
    public void clearAccessibilityFocus(int windowId) {
        getInteractionBridge().clearAccessibilityFocusNotLocked(windowId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IBinder findWindowTokenLocked(int windowId) {
        IBinder token = this.mGlobalWindowTokens.get(windowId);
        if (token != null) {
            return token;
        }
        return getCurrentUserStateLocked().mWindowTokens.get(windowId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int findWindowIdLocked(IBinder token) {
        int globalIndex = this.mGlobalWindowTokens.indexOfValue(token);
        if (globalIndex >= 0) {
            return this.mGlobalWindowTokens.keyAt(globalIndex);
        }
        UserState userState = getCurrentUserStateLocked();
        int userIndex = userState.mWindowTokens.indexOfValue(token);
        if (userIndex >= 0) {
            return userState.mWindowTokens.keyAt(userIndex);
        }
        return -1;
    }

    private void notifyOutsideTouchIfNeeded(int targetWindowId, int action) {
        if (action == 16 || action == 32) {
            List<RemoteAccessibilityConnection> connectionList = new ArrayList<>();
            synchronized (this.mLock) {
                List<Integer> outsideWindowsIds = this.mSecurityPolicy.getWatchOutsideTouchWindowIdLocked(targetWindowId);
                for (int i = 0; i < outsideWindowsIds.size(); i++) {
                    connectionList.add(getConnectionLocked(outsideWindowsIds.get(i).intValue()));
                }
            }
            for (int i2 = 0; i2 < connectionList.size(); i2++) {
                RemoteAccessibilityConnection connection = connectionList.get(i2);
                if (connection != null) {
                    try {
                        connection.getRemote().notifyOutsideTouch();
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public void ensureWindowsAvailableTimed() {
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.mWindows == null) {
                if (this.mWindowsForAccessibilityCallback == null) {
                    onUserStateChangedLocked(getCurrentUserStateLocked());
                }
                if (this.mWindowsForAccessibilityCallback != null) {
                    long startMillis = SystemClock.uptimeMillis();
                    while (this.mSecurityPolicy.mWindows == null) {
                        long remainMillis = 5000 - (SystemClock.uptimeMillis() - startMillis);
                        if (remainMillis > 0) {
                            try {
                                this.mLock.wait(remainMillis);
                            } catch (InterruptedException e) {
                            }
                        } else {
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public MagnificationController getMagnificationController() {
        MagnificationController magnificationController;
        synchronized (this.mLock) {
            if (this.mMagnificationController == null) {
                this.mMagnificationController = new MagnificationController(this.mContext, this, this.mLock);
                this.mMagnificationController.setUserId(this.mCurrentUserId);
            }
            magnificationController = this.mMagnificationController;
        }
        return magnificationController;
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x003a A[SYNTHETIC, Splitter:B:22:0x003a] */
    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public boolean performAccessibilityAction(int resolvedWindowId, long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int fetchFlags, long interrogatingTid) {
        RemoteException re;
        boolean isA11yFocusAction;
        AccessibilityWindowInfo a11yWindowInfo;
        IBinder activityToken;
        RemoteAccessibilityConnection connection;
        Throwable th;
        WindowInfo windowInfo;
        synchronized (this.mLock) {
            try {
                RemoteAccessibilityConnection connection2 = getConnectionLocked(resolvedWindowId);
                if (connection2 == null) {
                    return false;
                }
                if (action != 64) {
                    if (action != 128) {
                        isA11yFocusAction = false;
                        a11yWindowInfo = this.mSecurityPolicy.findA11yWindowInfoById(resolvedWindowId);
                        if (!isA11yFocusAction || (windowInfo = this.mSecurityPolicy.findWindowInfoById(resolvedWindowId)) == null) {
                            activityToken = null;
                        } else {
                            activityToken = windowInfo.activityToken;
                        }
                        if (a11yWindowInfo != null) {
                            try {
                                if (a11yWindowInfo.isInPictureInPictureMode() && this.mPictureInPictureActionReplacingConnection != null && !isA11yFocusAction) {
                                    connection = this.mPictureInPictureActionReplacingConnection;
                                }
                            } catch (Throwable th2) {
                                re = th2;
                                throw re;
                            }
                        }
                        connection = connection2;
                    }
                }
                isA11yFocusAction = true;
                a11yWindowInfo = this.mSecurityPolicy.findA11yWindowInfoById(resolvedWindowId);
                if (!isA11yFocusAction) {
                }
                activityToken = null;
                if (a11yWindowInfo != null) {
                }
                connection = connection2;
                try {
                } catch (Throwable th3) {
                    re = th3;
                    throw re;
                }
            } catch (Throwable th4) {
                re = th4;
                throw re;
            }
        }
        int interrogatingPid = Binder.getCallingPid();
        long identityToken = Binder.clearCallingIdentity();
        try {
            this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 3, 0);
            notifyOutsideTouchIfNeeded(resolvedWindowId, action);
            if (activityToken != null) {
                try {
                    ((ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class)).setFocusedActivity(activityToken);
                } catch (RemoteException e) {
                } catch (Throwable th5) {
                    th = th5;
                    Binder.restoreCallingIdentity(identityToken);
                    throw th;
                }
            }
            try {
                connection.mConnection.performAccessibilityAction(accessibilityNodeId, action, arguments, interactionId, callback, fetchFlags, interrogatingPid, interrogatingTid);
                Binder.restoreCallingIdentity(identityToken);
                return true;
            } catch (RemoteException e2) {
                Binder.restoreCallingIdentity(identityToken);
                return false;
            } catch (Throwable th6) {
                th = th6;
                Binder.restoreCallingIdentity(identityToken);
                throw th;
            }
        } catch (RemoteException e3) {
            Binder.restoreCallingIdentity(identityToken);
            return false;
        } catch (Throwable th7) {
            th = th7;
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public RemoteAccessibilityConnection getConnectionLocked(int windowId) {
        RemoteAccessibilityConnection connection = this.mGlobalInteractionConnections.get(windowId);
        if (connection == null) {
            connection = getCurrentUserStateLocked().mInteractionConnections.get(windowId);
        }
        if (connection == null || connection.mConnection == null) {
            return null;
        }
        return connection;
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public IAccessibilityInteractionConnectionCallback replaceCallbackIfNeeded(IAccessibilityInteractionConnectionCallback originalCallback, int resolvedWindowId, int interactionId, int interrogatingPid, long interrogatingTid) {
        RemoteAccessibilityConnection remoteAccessibilityConnection;
        AccessibilityWindowInfo windowInfo = this.mSecurityPolicy.findA11yWindowInfoById(resolvedWindowId);
        if (windowInfo == null || !windowInfo.isInPictureInPictureMode() || (remoteAccessibilityConnection = this.mPictureInPictureActionReplacingConnection) == null) {
            return originalCallback;
        }
        return new ActionReplacingCallback(originalCallback, remoteAccessibilityConnection.mConnection, interactionId, interrogatingPid, interrogatingTid);
    }

    @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection.SystemSupport
    public void onClientChangeLocked(boolean serviceInfoChanged) {
        UserState userState = getUserStateLocked(this.mCurrentUserId);
        onUserStateChangedLocked(userState);
        if (serviceInfoChanged) {
            scheduleNotifyClientsOfServicesStateChangeLocked(userState);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.accessibility.AccessibilityManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new AccessibilityShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    /* access modifiers changed from: package-private */
    public final class WindowsForAccessibilityCallback implements WindowManagerInternal.WindowsForAccessibilityCallback {
        WindowsForAccessibilityCallback() {
        }

        public void onWindowsForAccessibilityChanged(List<WindowInfo> windows) {
            synchronized (AccessibilityManagerService.this.mLock) {
                AccessibilityManagerService.this.mSecurityPolicy.updateWindowsLocked(windows);
                AccessibilityManagerService.this.mLock.notifyAll();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private AccessibilityWindowInfo populateReportedWindowLocked(WindowInfo window) {
            int windowId = AccessibilityManagerService.this.findWindowIdLocked(window.token);
            if (windowId < 0) {
                return null;
            }
            AccessibilityWindowInfo reportedWindow = AccessibilityWindowInfo.obtain();
            reportedWindow.setId(windowId);
            reportedWindow.setType(getTypeForWindowManagerWindowType(window.type));
            reportedWindow.setLayer(window.layer);
            reportedWindow.setFocused(window.focused);
            reportedWindow.setBoundsInScreen(window.boundsInScreen);
            reportedWindow.setTitle(window.title);
            reportedWindow.setAnchorId(window.accessibilityIdOfAnchor);
            reportedWindow.setPictureInPicture(window.inPictureInPicture);
            int parentId = AccessibilityManagerService.this.findWindowIdLocked(window.parentToken);
            if (parentId >= 0) {
                reportedWindow.setParentId(parentId);
            }
            if (window.childTokens != null) {
                int childCount = window.childTokens.size();
                for (int i = 0; i < childCount; i++) {
                    int childId = AccessibilityManagerService.this.findWindowIdLocked((IBinder) window.childTokens.get(i));
                    if (childId >= 0) {
                        reportedWindow.addChild(childId);
                    }
                }
            }
            return reportedWindow;
        }

        private int getTypeForWindowManagerWindowType(int windowType) {
            if (!(windowType == 1 || windowType == 2 || windowType == 3 || windowType == 4 || windowType == 1005)) {
                if (!(windowType == 2014 || windowType == 2017 || windowType == 2024)) {
                    if (windowType == 2032) {
                        return 4;
                    }
                    if (windowType == 2034) {
                        return 5;
                    }
                    if (!(windowType == 2036 || windowType == 2038 || windowType == 2019 || windowType == 2020)) {
                        switch (windowType) {
                            case 1000:
                            case NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE /* 1001 */:
                            case 1002:
                            case 1003:
                                break;
                            default:
                                switch (windowType) {
                                    case IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME /* 2000 */:
                                    case 2001:
                                    case 2003:
                                        break;
                                    case 2002:
                                        break;
                                    default:
                                        switch (windowType) {
                                            case 2005:
                                            case 2007:
                                                break;
                                            case 2006:
                                            case 2008:
                                            case 2009:
                                            case 2010:
                                                break;
                                            case 2011:
                                            case 2012:
                                                return 2;
                                            default:
                                                return -1;
                                        }
                                }
                        }
                    }
                }
                return 3;
            }
            return 1;
        }
    }

    /* access modifiers changed from: private */
    public final class InteractionBridge {
        private final ComponentName COMPONENT_NAME = new ComponentName("com.android.server.accessibility", "InteractionBridge");
        private final AccessibilityInteractionClient mClient;
        private final int mConnectionId;
        private final Display mDefaultDisplay;

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x008d, code lost:
            r0 = th;
         */
        public InteractionBridge() {
            UserState userState;
            AccessibilityServiceInfo info = new AccessibilityServiceInfo();
            info.setCapabilities(1);
            info.flags |= 64;
            info.flags |= 2;
            synchronized (AccessibilityManagerService.this.mLock) {
                userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
            }
            AccessibilityServiceConnection service = new AccessibilityServiceConnection(userState, AccessibilityManagerService.this.mContext, this.COMPONENT_NAME, info, AccessibilityManagerService.access$2508(), AccessibilityManagerService.this.mMainHandler, AccessibilityManagerService.this.mLock, AccessibilityManagerService.this.mSecurityPolicy, AccessibilityManagerService.this, AccessibilityManagerService.this.mWindowManagerService, AccessibilityManagerService.this.mGlobalActionPerformer, AccessibilityManagerService.this) {
                /* class com.android.server.accessibility.AccessibilityManagerService.InteractionBridge.AnonymousClass1 */

                @Override // com.android.server.accessibility.AbstractAccessibilityServiceConnection
                public boolean supportsFlagForNotImportantViews(AccessibilityServiceInfo info) {
                    return true;
                }
            };
            this.mConnectionId = service.mId;
            this.mClient = AccessibilityInteractionClient.getInstance();
            AccessibilityInteractionClient accessibilityInteractionClient = this.mClient;
            AccessibilityInteractionClient.addConnection(this.mConnectionId, service);
            this.mDefaultDisplay = ((DisplayManager) AccessibilityManagerService.this.mContext.getSystemService("display")).getDisplay(0);
            return;
            while (true) {
            }
        }

        public void clearAccessibilityFocusNotLocked(int windowId) {
            synchronized (AccessibilityManagerService.this.mLock) {
                RemoteAccessibilityConnection connection = AccessibilityManagerService.this.getConnectionLocked(windowId);
                if (connection != null) {
                    try {
                        connection.getRemote().clearAccessibilityFocus();
                    } catch (RemoteException e) {
                    }
                }
            }
        }

        public boolean performActionOnAccessibilityFocusedItemNotLocked(AccessibilityNodeInfo.AccessibilityAction action) {
            AccessibilityNodeInfo focus = getAccessibilityFocusNotLocked();
            if (focus == null || !focus.getActionList().contains(action)) {
                return false;
            }
            return focus.performAction(action.getId());
        }

        public boolean getAccessibilityFocusClickPointInScreenNotLocked(Point outPoint) {
            AccessibilityNodeInfo focus = getAccessibilityFocusNotLocked();
            if (focus == null) {
                return false;
            }
            synchronized (AccessibilityManagerService.this.mLock) {
                Rect boundsInScreen = AccessibilityManagerService.this.mTempRect;
                focus.getBoundsInScreen(boundsInScreen);
                MagnificationSpec spec = AccessibilityManagerService.this.getCompatibleMagnificationSpecLocked(focus.getWindowId());
                if (spec != null && !spec.isNop()) {
                    boundsInScreen.offset((int) (-spec.offsetX), (int) (-spec.offsetY));
                    boundsInScreen.scale(1.0f / spec.scale);
                }
                Rect windowBounds = AccessibilityManagerService.this.mTempRect1;
                AccessibilityManagerService.this.getWindowBounds(focus.getWindowId(), windowBounds);
                if (!boundsInScreen.intersect(windowBounds)) {
                    return false;
                }
                Point screenSize = AccessibilityManagerService.this.mTempPoint;
                this.mDefaultDisplay.getRealSize(screenSize);
                if (!boundsInScreen.intersect(0, 0, screenSize.x, screenSize.y)) {
                    return false;
                }
                outPoint.set(boundsInScreen.centerX(), boundsInScreen.centerY());
                return true;
            }
        }

        private AccessibilityNodeInfo getAccessibilityFocusNotLocked() {
            synchronized (AccessibilityManagerService.this.mLock) {
                int focusedWindowId = AccessibilityManagerService.this.mSecurityPolicy.mAccessibilityFocusedWindowId;
                if (focusedWindowId == -1) {
                    return null;
                }
                return getAccessibilityFocusNotLocked(focusedWindowId);
            }
        }

        private AccessibilityNodeInfo getAccessibilityFocusNotLocked(int windowId) {
            return this.mClient.findFocus(this.mConnectionId, windowId, AccessibilityNodeInfo.ROOT_NODE_ID, 2);
        }
    }

    public class SecurityPolicy {
        public static final int INVALID_WINDOW_ID = -1;
        private static final int KEEP_SOURCE_EVENT_TYPES = 4438463;
        public SparseArray<AccessibilityWindowInfo> mA11yWindowInfoById = new SparseArray<>();
        public long mAccessibilityFocusNodeId = 2147483647L;
        public int mAccessibilityFocusedWindowId = -1;
        public int mActiveWindowId = -1;
        public int mFocusedWindowId = -1;
        private boolean mHasWatchOutsideTouchWindow;
        private boolean mTouchInteractionInProgress;
        public SparseArray<WindowInfo> mWindowInfoById = new SparseArray<>();
        public List<AccessibilityWindowInfo> mWindows;

        public SecurityPolicy() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean canDispatchAccessibilityEventLocked(AccessibilityEvent event) {
            switch (event.getEventType()) {
                case 32:
                case 64:
                case 128:
                case 256:
                case 512:
                case 1024:
                case DumpState.DUMP_KEYSETS /* 16384 */:
                case DumpState.DUMP_DOMAIN_PREFERRED /* 262144 */:
                case DumpState.DUMP_FROZEN /* 524288 */:
                case DumpState.DUMP_DEXOPT /* 1048576 */:
                case DumpState.DUMP_COMPILER_STATS /* 2097152 */:
                case DumpState.DUMP_CHANGES /* 4194304 */:
                case DumpState.DUMP_SERVICE_PERMISSIONS /* 16777216 */:
                    return true;
                default:
                    return isRetrievalAllowingWindowLocked(event.getWindowId());
            }
        }

        private boolean isValidPackageForUid(String packageName, int uid) {
            long token = Binder.clearCallingIdentity();
            boolean z = false;
            try {
                if (uid == AccessibilityManagerService.this.mPackageManager.getPackageUidAsUser(packageName, UserHandle.getUserId(uid))) {
                    z = true;
                }
                return z;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        /* access modifiers changed from: package-private */
        public String resolveValidReportedPackageLocked(CharSequence packageName, int appId, int userId) {
            if (packageName == null) {
                return null;
            }
            if (appId == 1000) {
                return packageName.toString();
            }
            String packageNameStr = packageName.toString();
            int resolvedUid = UserHandle.getUid(userId, appId);
            if (isValidPackageForUid(packageNameStr, resolvedUid)) {
                return packageName.toString();
            }
            if (AccessibilityManagerService.this.mAppWidgetService != null && ArrayUtils.contains(AccessibilityManagerService.this.mAppWidgetService.getHostedWidgetPackages(resolvedUid), packageNameStr)) {
                return packageName.toString();
            }
            String[] packageNames = AccessibilityManagerService.this.mPackageManager.getPackagesForUid(resolvedUid);
            if (ArrayUtils.isEmpty(packageNames)) {
                return null;
            }
            return packageNames[0];
        }

        /* access modifiers changed from: package-private */
        public String[] computeValidReportedPackages(String targetPackage, int targetUid) {
            ArraySet<String> widgetPackages;
            if (UserHandle.getAppId(targetUid) == 1000) {
                return EmptyArray.STRING;
            }
            String[] uidPackages = {targetPackage};
            if (AccessibilityManagerService.this.mAppWidgetService == null || (widgetPackages = AccessibilityManagerService.this.mAppWidgetService.getHostedWidgetPackages(targetUid)) == null || widgetPackages.isEmpty()) {
                return uidPackages;
            }
            String[] validPackages = new String[(uidPackages.length + widgetPackages.size())];
            System.arraycopy(uidPackages, 0, validPackages, 0, uidPackages.length);
            int widgetPackageCount = widgetPackages.size();
            for (int i = 0; i < widgetPackageCount; i++) {
                validPackages[uidPackages.length + i] = widgetPackages.valueAt(i);
            }
            return validPackages;
        }

        public void clearWindowsLocked() {
            List<WindowInfo> windows = Collections.emptyList();
            int activeWindowId = this.mActiveWindowId;
            updateWindowsLocked(windows);
            this.mActiveWindowId = activeWindowId;
            this.mWindows = null;
        }

        public void onAccessibilityClientRemovedLocked(int windowId) {
            if (AccessibilityManagerService.this.mWindowsForAccessibilityCallback == null && windowId >= 0 && this.mActiveWindowId == windowId) {
                this.mActiveWindowId = -1;
            }
        }

        public void updateWindowsLocked(List<WindowInfo> windows) {
            AccessibilityWindowInfo window;
            if (this.mWindows == null) {
                this.mWindows = new ArrayList();
            }
            List<AccessibilityWindowInfo> oldWindowList = new ArrayList<>(this.mWindows);
            SparseArray<AccessibilityWindowInfo> oldWindowsById = this.mA11yWindowInfoById.clone();
            this.mWindows.clear();
            this.mA11yWindowInfoById.clear();
            for (int i = 0; i < this.mWindowInfoById.size(); i++) {
                this.mWindowInfoById.valueAt(i).recycle();
            }
            this.mWindowInfoById.clear();
            this.mHasWatchOutsideTouchWindow = false;
            this.mFocusedWindowId = -1;
            if (!this.mTouchInteractionInProgress) {
                this.mActiveWindowId = -1;
            }
            boolean activeWindowGone = true;
            int windowCount = windows.size();
            if (windowCount > 0) {
                for (int i2 = 0; i2 < windowCount; i2++) {
                    WindowInfo windowInfo = windows.get(i2);
                    if (AccessibilityManagerService.this.mWindowsForAccessibilityCallback != null) {
                        window = AccessibilityManagerService.this.mWindowsForAccessibilityCallback.populateReportedWindowLocked(windowInfo);
                    } else {
                        window = null;
                    }
                    if (window != null) {
                        window.setLayer((windowCount - 1) - window.getLayer());
                        int windowId = window.getId();
                        if (window.isFocused()) {
                            this.mFocusedWindowId = windowId;
                            if (!this.mTouchInteractionInProgress) {
                                this.mActiveWindowId = windowId;
                                window.setActive(true);
                            } else if (windowId == this.mActiveWindowId) {
                                activeWindowGone = false;
                            }
                        }
                        if (!this.mHasWatchOutsideTouchWindow && windowInfo.hasFlagWatchOutsideTouch) {
                            this.mHasWatchOutsideTouchWindow = true;
                        }
                        this.mWindows.add(window);
                        this.mA11yWindowInfoById.put(windowId, window);
                        this.mWindowInfoById.put(windowId, WindowInfo.obtain(windowInfo));
                    }
                }
                if (this.mTouchInteractionInProgress && activeWindowGone) {
                    this.mActiveWindowId = this.mFocusedWindowId;
                }
                int accessibilityWindowCount = this.mWindows.size();
                for (int i3 = 0; i3 < accessibilityWindowCount; i3++) {
                    AccessibilityWindowInfo window2 = this.mWindows.get(i3);
                    if (window2.getId() == this.mActiveWindowId) {
                        window2.setActive(true);
                    }
                    if (window2.getId() == this.mAccessibilityFocusedWindowId) {
                        window2.setAccessibilityFocused(true);
                    }
                }
            }
            sendEventsForChangedWindowsLocked(oldWindowList, oldWindowsById);
            for (int i4 = oldWindowList.size() - 1; i4 >= 0; i4--) {
                oldWindowList.remove(i4).recycle();
            }
        }

        private void sendEventsForChangedWindowsLocked(List<AccessibilityWindowInfo> oldWindows, SparseArray<AccessibilityWindowInfo> oldWindowsById) {
            List<AccessibilityEvent> events = new ArrayList<>();
            int oldWindowsCount = oldWindows.size();
            for (int i = 0; i < oldWindowsCount; i++) {
                AccessibilityWindowInfo window = oldWindows.get(i);
                if (this.mA11yWindowInfoById.get(window.getId()) == null) {
                    events.add(AccessibilityEvent.obtainWindowsChangedEvent(window.getId(), 2));
                }
            }
            int newWindowCount = this.mWindows.size();
            for (int i2 = 0; i2 < newWindowCount; i2++) {
                AccessibilityWindowInfo newWindow = this.mWindows.get(i2);
                AccessibilityWindowInfo oldWindow = oldWindowsById.get(newWindow.getId());
                if (oldWindow == null) {
                    events.add(AccessibilityEvent.obtainWindowsChangedEvent(newWindow.getId(), 1));
                } else {
                    int changes = newWindow.differenceFrom(oldWindow);
                    if (changes != 0) {
                        events.add(AccessibilityEvent.obtainWindowsChangedEvent(newWindow.getId(), changes));
                    }
                }
            }
            int numEvents = events.size();
            for (int i3 = 0; i3 < numEvents; i3++) {
                AccessibilityManagerService.this.sendAccessibilityEventLocked(events.get(i3), AccessibilityManagerService.this.mCurrentUserId);
            }
        }

        public boolean computePartialInteractiveRegionForWindowLocked(int windowId, Region outRegion) {
            List<AccessibilityWindowInfo> list = this.mWindows;
            if (list == null) {
                return false;
            }
            Region windowInteractiveRegion = null;
            boolean windowInteractiveRegionChanged = false;
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityWindowInfo currentWindow = this.mWindows.get(i);
                if (windowInteractiveRegion == null) {
                    if (currentWindow.getId() == windowId) {
                        Rect currentWindowBounds = AccessibilityManagerService.this.mTempRect;
                        currentWindow.getBoundsInScreen(currentWindowBounds);
                        outRegion.set(currentWindowBounds);
                        windowInteractiveRegion = outRegion;
                    }
                } else if (currentWindow.getType() != 4) {
                    Rect currentWindowBounds2 = AccessibilityManagerService.this.mTempRect;
                    currentWindow.getBoundsInScreen(currentWindowBounds2);
                    if (windowInteractiveRegion.op(currentWindowBounds2, Region.Op.DIFFERENCE)) {
                        windowInteractiveRegionChanged = true;
                    }
                }
            }
            return windowInteractiveRegionChanged;
        }

        public void updateEventSourceLocked(AccessibilityEvent event) {
            if ((event.getEventType() & KEEP_SOURCE_EVENT_TYPES) == 0) {
                event.setSource(null);
            }
        }

        public void updateActiveAndAccessibilityFocusedWindowLocked(int windowId, long nodeId, int eventType, int eventAction) {
            if (eventType == 32) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    if (AccessibilityManagerService.this.mWindowsForAccessibilityCallback == null) {
                        this.mFocusedWindowId = getFocusedWindowId();
                        if (windowId == this.mFocusedWindowId) {
                            this.mActiveWindowId = windowId;
                        }
                    }
                }
            } else if (eventType == 128) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    if (this.mTouchInteractionInProgress && this.mActiveWindowId != windowId) {
                        setActiveWindowLocked(windowId);
                    }
                }
            } else if (eventType == 32768) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    if (this.mAccessibilityFocusedWindowId != windowId) {
                        AccessibilityManagerService.this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Xd4PICw0vnPU2BuBjOCbMMfcgU.INSTANCE, AccessibilityManagerService.this, box(this.mAccessibilityFocusedWindowId)));
                        AccessibilityManagerService.this.mSecurityPolicy.setAccessibilityFocusedWindowLocked(windowId);
                        this.mAccessibilityFocusNodeId = nodeId;
                    }
                }
            } else if (eventType == 65536) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    if (this.mAccessibilityFocusNodeId == nodeId) {
                        this.mAccessibilityFocusNodeId = 2147483647L;
                    }
                    if (this.mAccessibilityFocusNodeId == 2147483647L && this.mAccessibilityFocusedWindowId == windowId && eventAction != 64) {
                        this.mAccessibilityFocusedWindowId = -1;
                    }
                }
            }
        }

        public void onTouchInteractionStart() {
            synchronized (AccessibilityManagerService.this.mLock) {
                this.mTouchInteractionInProgress = true;
            }
        }

        public void onTouchInteractionEnd() {
            synchronized (AccessibilityManagerService.this.mLock) {
                this.mTouchInteractionInProgress = false;
                int oldActiveWindow = AccessibilityManagerService.this.mSecurityPolicy.mActiveWindowId;
                setActiveWindowLocked(this.mFocusedWindowId);
                if (oldActiveWindow != AccessibilityManagerService.this.mSecurityPolicy.mActiveWindowId && this.mAccessibilityFocusedWindowId == oldActiveWindow && AccessibilityManagerService.this.getCurrentUserStateLocked().mAccessibilityFocusOnlyInActiveWindow) {
                    AccessibilityManagerService.this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$Xd4PICw0vnPU2BuBjOCbMMfcgU.INSTANCE, AccessibilityManagerService.this, box(oldActiveWindow)));
                }
            }
        }

        private IntSupplier box(int value) {
            return PooledLambda.obtainSupplier(value).recycleOnUse();
        }

        public int getActiveWindowId() {
            if (this.mActiveWindowId == -1 && !this.mTouchInteractionInProgress) {
                this.mActiveWindowId = getFocusedWindowId();
            }
            return this.mActiveWindowId;
        }

        private void setActiveWindowLocked(int windowId) {
            int i = this.mActiveWindowId;
            if (i != windowId) {
                AccessibilityManagerService.this.sendAccessibilityEventLocked(AccessibilityEvent.obtainWindowsChangedEvent(i, 32), AccessibilityManagerService.this.mCurrentUserId);
                this.mActiveWindowId = windowId;
                List<AccessibilityWindowInfo> list = this.mWindows;
                if (list != null) {
                    int windowCount = list.size();
                    for (int i2 = 0; i2 < windowCount; i2++) {
                        AccessibilityWindowInfo window = this.mWindows.get(i2);
                        if (window.getId() == windowId) {
                            window.setActive(true);
                            AccessibilityManagerService.this.sendAccessibilityEventLocked(AccessibilityEvent.obtainWindowsChangedEvent(windowId, 32), AccessibilityManagerService.this.mCurrentUserId);
                        } else {
                            window.setActive(false);
                        }
                    }
                }
            }
        }

        private void setAccessibilityFocusedWindowLocked(int windowId) {
            int i = this.mAccessibilityFocusedWindowId;
            if (i != windowId) {
                AccessibilityManagerService.this.sendAccessibilityEventLocked(AccessibilityEvent.obtainWindowsChangedEvent(i, 128), AccessibilityManagerService.this.mCurrentUserId);
                this.mAccessibilityFocusedWindowId = windowId;
                List<AccessibilityWindowInfo> list = this.mWindows;
                if (list != null) {
                    int windowCount = list.size();
                    for (int i2 = 0; i2 < windowCount; i2++) {
                        AccessibilityWindowInfo window = this.mWindows.get(i2);
                        if (window.getId() == windowId) {
                            window.setAccessibilityFocused(true);
                            AccessibilityManagerService.this.sendAccessibilityEventLocked(AccessibilityEvent.obtainWindowsChangedEvent(windowId, 128), AccessibilityManagerService.this.mCurrentUserId);
                        } else {
                            window.setAccessibilityFocused(false);
                        }
                    }
                }
            }
        }

        public boolean canGetAccessibilityNodeInfoLocked(AbstractAccessibilityServiceConnection service, int windowId) {
            return canRetrieveWindowContentLocked(service) && isRetrievalAllowingWindowLocked(windowId);
        }

        public boolean canRetrieveWindowsLocked(AbstractAccessibilityServiceConnection service) {
            return canRetrieveWindowContentLocked(service) && service.mRetrieveInteractiveWindows;
        }

        public boolean canRetrieveWindowContentLocked(AbstractAccessibilityServiceConnection service) {
            return (service.getCapabilities() & 1) != 0;
        }

        public boolean canControlMagnification(AbstractAccessibilityServiceConnection service) {
            return (service.getCapabilities() & 16) != 0;
        }

        public boolean canPerformGestures(AccessibilityServiceConnection service) {
            return (service.getCapabilities() & 32) != 0;
        }

        public boolean canCaptureFingerprintGestures(AccessibilityServiceConnection service) {
            return (service.getCapabilities() & 64) != 0;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int resolveProfileParentLocked(int userId) {
            if (userId != AccessibilityManagerService.this.mCurrentUserId) {
                long identity = Binder.clearCallingIdentity();
                try {
                    UserInfo parent = AccessibilityManagerService.this.mUserManager.getProfileParent(userId);
                    if (parent != null) {
                        return parent.getUserHandle().getIdentifier();
                    }
                    Binder.restoreCallingIdentity(identity);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
            return userId;
        }

        public int resolveCallingUserIdEnforcingPermissionsLocked(int userId) {
            int callingUid = Binder.getCallingUid();
            if (callingUid != 0 && callingUid != 1000 && callingUid != 2000) {
                int callingUserId = UserHandle.getUserId(callingUid);
                if (callingUserId == userId) {
                    return resolveProfileParentLocked(userId);
                }
                if (resolveProfileParentLocked(callingUserId) == AccessibilityManagerService.this.mCurrentUserId && (userId == -2 || userId == -3)) {
                    return AccessibilityManagerService.this.mCurrentUserId;
                }
                if (!hasPermission("android.permission.INTERACT_ACROSS_USERS") && !hasPermission("android.permission.INTERACT_ACROSS_USERS_FULL")) {
                    throw new SecurityException("Call from user " + callingUserId + " as user " + userId + " without permission INTERACT_ACROSS_USERS or INTERACT_ACROSS_USERS_FULL not allowed.");
                } else if (userId == -2 || userId == -3) {
                    return AccessibilityManagerService.this.mCurrentUserId;
                } else {
                    throw new IllegalArgumentException("Calling user can be changed to only UserHandle.USER_CURRENT or UserHandle.USER_CURRENT_OR_SELF.");
                }
            } else if (userId == -2 || userId == -3) {
                return AccessibilityManagerService.this.mCurrentUserId;
            } else {
                return resolveProfileParentLocked(userId);
            }
        }

        public boolean isCallerInteractingAcrossUsers(int userId) {
            return Binder.getCallingPid() == Process.myPid() || Binder.getCallingUid() == 2000 || userId == -2 || userId == -3;
        }

        private boolean isRetrievalAllowingWindowLocked(int windowId) {
            if (Binder.getCallingUid() == 1000) {
                return true;
            }
            if (Binder.getCallingUid() == 2000 && !isShellAllowedToRetrieveWindowLocked(windowId)) {
                return false;
            }
            if (windowId != this.mActiveWindowId && findA11yWindowInfoById(windowId) == null) {
                return false;
            }
            return true;
        }

        private boolean isShellAllowedToRetrieveWindowLocked(int windowId) {
            long token = Binder.clearCallingIdentity();
            try {
                IBinder windowToken = AccessibilityManagerService.this.findWindowTokenLocked(windowId);
                if (windowToken == null) {
                    return false;
                }
                int userId = AccessibilityManagerService.this.mWindowManagerService.getWindowOwnerUserId(windowToken);
                if (userId == -10000) {
                    Binder.restoreCallingIdentity(token);
                    return false;
                }
                boolean z = !AccessibilityManagerService.this.mUserManager.hasUserRestriction("no_debugging_features", UserHandle.of(userId));
                Binder.restoreCallingIdentity(token);
                return z;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public AccessibilityWindowInfo findA11yWindowInfoById(int windowId) {
            return this.mA11yWindowInfoById.get(windowId);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private WindowInfo findWindowInfoById(int windowId) {
            return this.mWindowInfoById.get(windowId);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private List<Integer> getWatchOutsideTouchWindowIdLocked(int targetWindowId) {
            WindowInfo targetWindow = this.mWindowInfoById.get(targetWindowId);
            if (targetWindow == null || !this.mHasWatchOutsideTouchWindow) {
                return Collections.emptyList();
            }
            List<Integer> outsideWindowsId = new ArrayList<>();
            for (int i = 0; i < this.mWindowInfoById.size(); i++) {
                WindowInfo window = this.mWindowInfoById.valueAt(i);
                if (window != null && window.layer < targetWindow.layer && window.hasFlagWatchOutsideTouch) {
                    outsideWindowsId.add(Integer.valueOf(this.mWindowInfoById.keyAt(i)));
                }
            }
            return outsideWindowsId;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private AccessibilityWindowInfo getPictureInPictureWindow() {
            List<AccessibilityWindowInfo> list = this.mWindows;
            if (list == null) {
                return null;
            }
            int windowCount = list.size();
            for (int i = 0; i < windowCount; i++) {
                AccessibilityWindowInfo window = this.mWindows.get(i);
                if (window.isInPictureInPictureMode()) {
                    return window;
                }
            }
            return null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void enforceCallingPermission(String permission, String function) {
            if (AccessibilityManagerService.OWN_PROCESS_ID != Binder.getCallingPid() && !hasPermission(permission)) {
                throw new SecurityException("You do not have " + permission + " required to call " + function + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            }
        }

        private boolean hasPermission(String permission) {
            return AccessibilityManagerService.this.mContext.checkCallingPermission(permission) == 0;
        }

        private int getFocusedWindowId() {
            int findWindowIdLocked;
            IBinder token = AccessibilityManagerService.this.mWindowManagerService.getFocusedWindowToken();
            synchronized (AccessibilityManagerService.this.mLock) {
                findWindowIdLocked = AccessibilityManagerService.this.findWindowIdLocked(token);
            }
            return findWindowIdLocked;
        }

        public boolean checkAccessibilityAccess(AbstractAccessibilityServiceConnection service) {
            String packageName = service.getComponentName().getPackageName();
            ResolveInfo resolveInfo = service.getServiceInfo().getResolveInfo();
            boolean z = true;
            if (resolveInfo == null) {
                return true;
            }
            int uid = resolveInfo.serviceInfo.applicationInfo.uid;
            long identityToken = Binder.clearCallingIdentity();
            try {
                if (AccessibilityManagerService.OWN_PROCESS_ID == Binder.getCallingPid()) {
                    if (AccessibilityManagerService.this.mAppOpsManager.noteOpNoThrow("android:access_accessibility", uid, packageName) != 0) {
                        z = false;
                    }
                    return z;
                }
                if (AccessibilityManagerService.this.mAppOpsManager.noteOp("android:access_accessibility", uid, packageName) != 0) {
                    z = false;
                }
                Binder.restoreCallingIdentity(identityToken);
                return z;
            } finally {
                Binder.restoreCallingIdentity(identityToken);
            }
        }
    }

    public ArrayList<Display> getValidDisplayList() {
        return this.mA11yDisplayListener.getValidDisplayList();
    }

    public class AccessibilityDisplayListener implements DisplayManager.DisplayListener {
        private final DisplayManager mDisplayManager;
        private final ArrayList<Display> mDisplaysList = new ArrayList<>();

        AccessibilityDisplayListener(Context context, MainHandler handler) {
            this.mDisplayManager = (DisplayManager) context.getSystemService("display");
            this.mDisplayManager.registerDisplayListener(this, handler);
            initializeDisplayList();
        }

        /* access modifiers changed from: package-private */
        public ArrayList<Display> getValidDisplayList() {
            ArrayList<Display> arrayList;
            synchronized (AccessibilityManagerService.this.mLock) {
                arrayList = this.mDisplaysList;
            }
            return arrayList;
        }

        private void initializeDisplayList() {
            Display[] displays = this.mDisplayManager.getDisplays();
            synchronized (AccessibilityManagerService.this.mLock) {
                this.mDisplaysList.clear();
                for (Display display : displays) {
                    if (display.getType() != 4) {
                        this.mDisplaysList.add(display);
                    }
                }
            }
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int displayId) {
            Display display = this.mDisplayManager.getDisplay(displayId);
            if (display != null && display.getType() != 4) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    this.mDisplaysList.add(display);
                    if (AccessibilityManagerService.this.mInputFilter != null) {
                        AccessibilityManagerService.this.mInputFilter.onDisplayChanged();
                    }
                    AccessibilityManagerService.this.updateMagnificationLocked(AccessibilityManagerService.this.getCurrentUserStateLocked());
                }
            }
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int displayId) {
            synchronized (AccessibilityManagerService.this.mLock) {
                int i = 0;
                while (true) {
                    if (i >= this.mDisplaysList.size()) {
                        break;
                    } else if (this.mDisplaysList.get(i).getDisplayId() == displayId) {
                        this.mDisplaysList.remove(i);
                        break;
                    } else {
                        i++;
                    }
                }
                if (AccessibilityManagerService.this.mInputFilter != null) {
                    AccessibilityManagerService.this.mInputFilter.onDisplayChanged();
                }
            }
            if (AccessibilityManagerService.this.mMagnificationController != null) {
                AccessibilityManagerService.this.mMagnificationController.onDisplayRemoved(displayId);
            }
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(int displayId) {
        }
    }

    /* access modifiers changed from: package-private */
    public class Client {
        final IAccessibilityManagerClient mCallback;
        int mLastSentRelevantEventTypes;
        final String[] mPackageNames;

        private Client(IAccessibilityManagerClient callback, int clientUid, UserState userState) {
            this.mCallback = callback;
            this.mPackageNames = AccessibilityManagerService.this.mPackageManager.getPackagesForUid(clientUid);
            synchronized (AccessibilityManagerService.this.mLock) {
                this.mLastSentRelevantEventTypes = AccessibilityManagerService.this.computeRelevantEventTypesLocked(userState, this);
            }
        }
    }

    public class UserState {
        public boolean mAccessibilityFocusOnlyInActiveWindow;
        private boolean mBindInstantServiceAllowed;
        private final Set<ComponentName> mBindingServices = new HashSet();
        public final ArrayList<AccessibilityServiceConnection> mBoundServices = new ArrayList<>();
        public final Map<ComponentName, AccessibilityServiceConnection> mComponentNameToServiceMap = new HashMap();
        public final Set<ComponentName> mEnabledServices = new HashSet();
        public final List<AccessibilityServiceInfo> mInstalledServices = new ArrayList();
        public final SparseArray<RemoteAccessibilityConnection> mInteractionConnections = new SparseArray<>();
        public int mInteractiveUiTimeout = 0;
        public boolean mIsAutoclickEnabled;
        public boolean mIsDisplayMagnificationEnabled;
        public boolean mIsFilterKeyEventsEnabled;
        public boolean mIsNavBarMagnificationAssignedToAccessibilityButton;
        public boolean mIsNavBarMagnificationEnabled;
        public boolean mIsPerformGesturesEnabled;
        public boolean mIsTextHighContrastEnabled;
        public boolean mIsTouchExplorationEnabled;
        public int mLastSentClientState = -1;
        public int mNonInteractiveUiTimeout = 0;
        public ComponentName mServiceAssignedToAccessibilityButton;
        public ComponentName mServiceChangingSoftKeyboardMode;
        public ComponentName mServiceToEnableWithShortcut;
        private int mSoftKeyboardShowMode = 0;
        public final Set<ComponentName> mTouchExplorationGrantedServices = new HashSet();
        public final RemoteCallbackList<IAccessibilityManagerClient> mUserClients = new RemoteCallbackList<>();
        public final int mUserId;
        public int mUserInteractiveUiTimeout;
        public int mUserNonInteractiveUiTimeout;
        public final SparseArray<IBinder> mWindowTokens = new SparseArray<>();

        public UserState(int userId) {
            this.mUserId = userId;
        }

        public int getClientState() {
            int clientState = 0;
            boolean a11yEnabled = AccessibilityManagerService.this.mUiAutomationManager.isUiAutomationRunningLocked() || isHandlingAccessibilityEvents();
            if (a11yEnabled) {
                clientState = 0 | 1;
            }
            if (a11yEnabled && this.mIsTouchExplorationEnabled) {
                clientState |= 2;
            }
            if (this.mIsTextHighContrastEnabled) {
                return clientState | 4;
            }
            return clientState;
        }

        public boolean isHandlingAccessibilityEvents() {
            return !this.mBoundServices.isEmpty() || !this.mBindingServices.isEmpty();
        }

        public void onSwitchToAnotherUserLocked() {
            AccessibilityManagerService.this.unbindAllServicesLocked(this);
            this.mBoundServices.clear();
            this.mBindingServices.clear();
            this.mLastSentClientState = -1;
            this.mNonInteractiveUiTimeout = 0;
            this.mInteractiveUiTimeout = 0;
            this.mEnabledServices.clear();
            this.mTouchExplorationGrantedServices.clear();
            this.mIsTouchExplorationEnabled = false;
            this.mIsDisplayMagnificationEnabled = false;
            this.mIsNavBarMagnificationEnabled = false;
            this.mServiceAssignedToAccessibilityButton = null;
            this.mIsNavBarMagnificationAssignedToAccessibilityButton = false;
            this.mIsAutoclickEnabled = false;
            this.mUserNonInteractiveUiTimeout = 0;
            this.mUserInteractiveUiTimeout = 0;
        }

        public void addServiceLocked(AccessibilityServiceConnection serviceConnection) {
            if (!this.mBoundServices.contains(serviceConnection)) {
                serviceConnection.onAdded();
                this.mBoundServices.add(serviceConnection);
                Slog.i(AccessibilityManagerService.LOG_TAG, "volume_debug addServiceLocked:" + serviceConnection.mComponentName.getPackageName());
                this.mComponentNameToServiceMap.put(serviceConnection.mComponentName, serviceConnection);
                AccessibilityManagerService.this.scheduleNotifyClientsOfServicesStateChangeLocked(this);
                if (AccessibilityManagerService.this.isTalkBackService(serviceConnection.mComponentName)) {
                    AccessibilityManagerService.this.mIsTalkBackEnabled = true;
                } else if (AccessibilityManagerService.this.isScreenReaderService(serviceConnection.mComponentName)) {
                    AccessibilityManagerService.this.mIsScreenReaderEnabled = true;
                } else {
                    Slog.d(AccessibilityManagerService.LOG_TAG, "addServiceLocked not handle else");
                }
                if (AccessibilityManagerService.this.getScreenReaderState()) {
                    return;
                }
                if (AccessibilityManagerService.this.mIsTalkBackEnabled || AccessibilityManagerService.this.mIsScreenReaderEnabled) {
                    AccessibilityManagerService.this.notifyAccessibilityEnableChangedEvent(1);
                    AccessibilityManagerService.this.setScreenReaderState(1);
                }
            }
        }

        public void removeServiceLocked(AccessibilityServiceConnection serviceConnection) {
            Slog.i(AccessibilityManagerService.LOG_TAG, "volume_debug removeServiceLocked:" + serviceConnection.mComponentName.getPackageName());
            this.mBoundServices.remove(serviceConnection);
            serviceConnection.onRemoved();
            ComponentName componentName = this.mServiceChangingSoftKeyboardMode;
            if (componentName != null && componentName.equals(serviceConnection.getServiceInfo().getComponentName())) {
                setSoftKeyboardModeLocked(0, null);
            }
            this.mComponentNameToServiceMap.clear();
            for (int i = 0; i < this.mBoundServices.size(); i++) {
                AccessibilityServiceConnection boundClient = this.mBoundServices.get(i);
                this.mComponentNameToServiceMap.put(boundClient.mComponentName, boundClient);
            }
            AccessibilityManagerService.this.scheduleNotifyClientsOfServicesStateChangeLocked(this);
            if (AccessibilityManagerService.this.isTalkBackService(serviceConnection.mComponentName)) {
                AccessibilityManagerService.this.mIsTalkBackEnabled = false;
            } else if (AccessibilityManagerService.this.isScreenReaderService(serviceConnection.mComponentName)) {
                AccessibilityManagerService.this.mIsScreenReaderEnabled = false;
            } else {
                Slog.d(AccessibilityManagerService.LOG_TAG, "removeServiceLocked not handle else");
            }
            if (!(!AccessibilityManagerService.this.getScreenReaderState() || AccessibilityManagerService.this.mIsTalkBackEnabled || AccessibilityManagerService.this.mIsScreenReaderEnabled)) {
                AccessibilityManagerService.this.notifyAccessibilityEnableChangedEvent(0);
                AccessibilityManagerService.this.setScreenReaderState(0);
            }
        }

        public void serviceDisconnectedLocked(AccessibilityServiceConnection serviceConnection) {
            removeServiceLocked(serviceConnection);
            this.mBindingServices.add(serviceConnection.getComponentName());
        }

        public Set<ComponentName> getBindingServicesLocked() {
            return this.mBindingServices;
        }

        public Set<ComponentName> getEnabledServicesLocked() {
            return this.mEnabledServices;
        }

        public int getSoftKeyboardShowMode() {
            return this.mSoftKeyboardShowMode;
        }

        public boolean setSoftKeyboardModeLocked(int newMode, ComponentName requester) {
            boolean z = false;
            if (newMode == 0 || newMode == 1 || newMode == 2) {
                int i = this.mSoftKeyboardShowMode;
                if (i == newMode) {
                    return true;
                }
                if (newMode == 2) {
                    if (hasUserOverriddenHardKeyboardSettingLocked()) {
                        return false;
                    }
                    if (getSoftKeyboardValueFromSettings() != 2) {
                        if (Settings.Secure.getInt(AccessibilityManagerService.this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", 0) != 0) {
                            z = true;
                        }
                        setOriginalHardKeyboardValue(z);
                    }
                    AccessibilityManagerService.this.putSecureIntForUser("show_ime_with_hard_keyboard", 1, this.mUserId);
                } else if (i == 2) {
                    AccessibilityManagerService.this.putSecureIntForUser("show_ime_with_hard_keyboard", getOriginalHardKeyboardValue() ? 1 : 0, this.mUserId);
                }
                saveSoftKeyboardValueToSettings(newMode);
                this.mSoftKeyboardShowMode = newMode;
                this.mServiceChangingSoftKeyboardMode = requester;
                AccessibilityManagerService.this.notifySoftKeyboardShowModeChangedLocked(this.mSoftKeyboardShowMode);
                return true;
            }
            Slog.w(AccessibilityManagerService.LOG_TAG, "Invalid soft keyboard mode");
            return false;
        }

        public void reconcileSoftKeyboardModeWithSettingsLocked() {
            boolean showWithHardKeyboardSettings = Settings.Secure.getInt(AccessibilityManagerService.this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", 0) != 0;
            if (this.mSoftKeyboardShowMode == 2 && !showWithHardKeyboardSettings) {
                setSoftKeyboardModeLocked(0, null);
                setUserOverridesHardKeyboardSettingLocked();
            }
            if (getSoftKeyboardValueFromSettings() != this.mSoftKeyboardShowMode) {
                Slog.e(AccessibilityManagerService.LOG_TAG, "Show IME setting inconsistent with internal state. Overwriting");
                setSoftKeyboardModeLocked(0, null);
                AccessibilityManagerService.this.putSecureIntForUser("accessibility_soft_keyboard_mode", 0, this.mUserId);
            }
        }

        private void setUserOverridesHardKeyboardSettingLocked() {
            AccessibilityManagerService.this.putSecureIntForUser("accessibility_soft_keyboard_mode", 1073741824 | Settings.Secure.getInt(AccessibilityManagerService.this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0), this.mUserId);
        }

        private boolean hasUserOverriddenHardKeyboardSettingLocked() {
            if ((1073741824 & Settings.Secure.getInt(AccessibilityManagerService.this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0)) != 0) {
                return true;
            }
            return false;
        }

        private void setOriginalHardKeyboardValue(boolean originalHardKeyboardValue) {
            int i = 0;
            int i2 = -536870913 & Settings.Secure.getInt(AccessibilityManagerService.this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0);
            if (originalHardKeyboardValue) {
                i = 536870912;
            }
            AccessibilityManagerService.this.putSecureIntForUser("accessibility_soft_keyboard_mode", i | i2, this.mUserId);
        }

        private void saveSoftKeyboardValueToSettings(int softKeyboardShowMode) {
            AccessibilityManagerService.this.putSecureIntForUser("accessibility_soft_keyboard_mode", (Settings.Secure.getInt(AccessibilityManagerService.this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0) & -4) | softKeyboardShowMode, this.mUserId);
        }

        private int getSoftKeyboardValueFromSettings() {
            return Settings.Secure.getInt(AccessibilityManagerService.this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0) & 3;
        }

        private boolean getOriginalHardKeyboardValue() {
            return (Settings.Secure.getInt(AccessibilityManagerService.this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0) & 536870912) != 0;
        }

        public boolean getBindInstantServiceAllowed() {
            boolean z;
            synchronized (AccessibilityManagerService.this.mLock) {
                z = this.mBindInstantServiceAllowed;
            }
            return z;
        }

        public void setBindInstantServiceAllowed(boolean allowed) {
            synchronized (AccessibilityManagerService.this.mLock) {
                AccessibilityManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_BIND_INSTANT_SERVICE", "setBindInstantServiceAllowed");
                if (allowed) {
                    this.mBindInstantServiceAllowed = allowed;
                    AccessibilityManagerService.this.onUserStateChangedLocked(this);
                }
            }
        }

        public int getInputFilterFlag() {
            int flag = 0;
            if (this.mIsDisplayMagnificationEnabled) {
                flag = 0 | 1;
            }
            if (this.mIsNavBarMagnificationEnabled) {
                flag |= 64;
            }
            if (AccessibilityManagerService.this.userHasMagnificationServicesLocked(this)) {
                flag |= 32;
            }
            if (isHandlingAccessibilityEvents() && this.mIsTouchExplorationEnabled) {
                flag |= 2;
            }
            if (this.mIsFilterKeyEventsEnabled) {
                flag |= 4;
            }
            if (this.mIsAutoclickEnabled) {
                flag |= 8;
            }
            if (this.mIsPerformGesturesEnabled) {
                flag |= 16;
            }
            Slog.i(AccessibilityManagerService.LOG_TAG, "inputfilter flag:" + flag);
            return flag;
        }
    }

    private final class AccessibilityContentObserver extends ContentObserver {
        private final Uri mAccessibilityButtonComponentIdUri = Settings.Secure.getUriFor("accessibility_button_target_component");
        private final Uri mAccessibilityShortcutServiceIdUri = Settings.Secure.getUriFor("accessibility_shortcut_target_service");
        private final Uri mAccessibilitySoftKeyboardModeUri = Settings.Secure.getUriFor("accessibility_soft_keyboard_mode");
        private final Uri mAutoclickEnabledUri = Settings.Secure.getUriFor("accessibility_autoclick_enabled");
        private final Uri mDisplayMagnificationEnabledUri = Settings.Secure.getUriFor("accessibility_display_magnification_enabled");
        private final Uri mEnabledAccessibilityServicesUri = Settings.Secure.getUriFor("enabled_accessibility_services");
        private final Uri mHighTextContrastUri = Settings.Secure.getUriFor("high_text_contrast_enabled");
        private final Uri mNavBarMagnificationEnabledUri = Settings.Secure.getUriFor("accessibility_display_magnification_navbar_enabled");
        private final Uri mShowImeWithHardKeyboardUri = Settings.Secure.getUriFor("show_ime_with_hard_keyboard");
        private final Uri mTouchExplorationEnabledUri = Settings.Secure.getUriFor("touch_exploration_enabled");
        private final Uri mTouchExplorationGrantedAccessibilityServicesUri = Settings.Secure.getUriFor("touch_exploration_granted_accessibility_services");
        private final Uri mUserInteractiveUiTimeoutUri = Settings.Secure.getUriFor("accessibility_interactive_ui_timeout_ms");
        private final Uri mUserNonInteractiveUiTimeoutUri = Settings.Secure.getUriFor("accessibility_non_interactive_ui_timeout_ms");

        public AccessibilityContentObserver(Handler handler) {
            super(handler);
        }

        public void register(ContentResolver contentResolver) {
            contentResolver.registerContentObserver(this.mTouchExplorationEnabledUri, false, this, -1);
            contentResolver.registerContentObserver(this.mDisplayMagnificationEnabledUri, false, this, -1);
            contentResolver.registerContentObserver(this.mNavBarMagnificationEnabledUri, false, this, -1);
            contentResolver.registerContentObserver(this.mAutoclickEnabledUri, false, this, -1);
            contentResolver.registerContentObserver(this.mEnabledAccessibilityServicesUri, false, this, -1);
            contentResolver.registerContentObserver(this.mTouchExplorationGrantedAccessibilityServicesUri, false, this, -1);
            contentResolver.registerContentObserver(this.mHighTextContrastUri, false, this, -1);
            contentResolver.registerContentObserver(this.mAccessibilitySoftKeyboardModeUri, false, this, -1);
            contentResolver.registerContentObserver(this.mShowImeWithHardKeyboardUri, false, this, -1);
            contentResolver.registerContentObserver(this.mAccessibilityShortcutServiceIdUri, false, this, -1);
            contentResolver.registerContentObserver(this.mAccessibilityButtonComponentIdUri, false, this, -1);
            contentResolver.registerContentObserver(this.mUserNonInteractiveUiTimeoutUri, false, this, -1);
            contentResolver.registerContentObserver(this.mUserInteractiveUiTimeoutUri, false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            synchronized (AccessibilityManagerService.this.mLock) {
                UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                if (!this.mTouchExplorationEnabledUri.equals(uri)) {
                    if (!this.mDisplayMagnificationEnabledUri.equals(uri)) {
                        if (!this.mNavBarMagnificationEnabledUri.equals(uri)) {
                            if (this.mAutoclickEnabledUri.equals(uri)) {
                                if (AccessibilityManagerService.this.readAutoclickEnabledSettingLocked(userState)) {
                                    AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                }
                            } else if (this.mEnabledAccessibilityServicesUri.equals(uri)) {
                                if (AccessibilityManagerService.this.readEnabledAccessibilityServicesLocked(userState)) {
                                    AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                }
                            } else if (this.mTouchExplorationGrantedAccessibilityServicesUri.equals(uri)) {
                                if (AccessibilityManagerService.this.readTouchExplorationGrantedAccessibilityServicesLocked(userState)) {
                                    AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                }
                            } else if (!this.mHighTextContrastUri.equals(uri)) {
                                if (!this.mAccessibilitySoftKeyboardModeUri.equals(uri)) {
                                    if (!this.mShowImeWithHardKeyboardUri.equals(uri)) {
                                        if (this.mAccessibilityShortcutServiceIdUri.equals(uri)) {
                                            if (AccessibilityManagerService.this.readAccessibilityShortcutSettingLocked(userState)) {
                                                AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                            }
                                        } else if (this.mAccessibilityButtonComponentIdUri.equals(uri)) {
                                            if (AccessibilityManagerService.this.readAccessibilityButtonSettingsLocked(userState)) {
                                                AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                            }
                                        } else if (this.mUserNonInteractiveUiTimeoutUri.equals(uri) || this.mUserInteractiveUiTimeoutUri.equals(uri)) {
                                            AccessibilityManagerService.this.readUserRecommendedUiTimeoutSettingsLocked(userState);
                                        }
                                    }
                                }
                                userState.reconcileSoftKeyboardModeWithSettingsLocked();
                            } else if (AccessibilityManagerService.this.readHighTextContrastEnabledSettingLocked(userState)) {
                                AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                            }
                        }
                    }
                    if (AccessibilityManagerService.this.readMagnificationEnabledSettingsLocked(userState)) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if (AccessibilityManagerService.this.readTouchExplorationEnabledSettingLocked(userState)) {
                    AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                }
            }
        }
    }
}
