package com.android.server.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.IAccessibilityServiceClient;
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
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.accessibility.AbstractAccessibilityServiceConnection;
import com.android.server.accessibility.AccessibilityManagerService;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.pm.DumpState;
import com.android.server.power.IHwShutdownThread;
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
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParserException;

public class AccessibilityManagerService extends IAccessibilityManager.Stub implements AbstractAccessibilityServiceConnection.SystemSupport {
    private static final char COMPONENT_NAME_SEPARATOR = ':';
    private static final int DALTONIAN_SIM_ALL = 0;
    private static final int DALTONIAN_SIM_DEU = 2;
    private static final int DALTONIAN_SIM_PRO = 1;
    private static final int DALTONIAN_SIM_TRI = 3;
    private static final int DALTONIZER_CORRECT_DEUTERANOMALY = 12;
    private static final int DALTONIZER_CORRECT_PROTANOMALY = 11;
    private static final int DALTONIZER_CORRECT_TRITANOMALY = 13;
    private static final boolean DEBUG = false;
    private static final String DE_ACTION_DALTONIAN_DEU = "ACTION_DALTONIAN_DEU";
    private static final String DE_ACTION_DALTONIAN_PRO = "ACTION_DALTONIAN_PRO";
    private static final String DE_ACTION_DALTONIAN_SIM_ALL = "ACTION_DALTONIAN_SIM_ALL";
    private static final String DE_ACTION_DALTONIAN_SIM_DEU = "ACTION_DALTONIAN_SIM_DEU";
    private static final String DE_ACTION_DALTONIAN_SIM_PRO = "ACTION_DALTONIAN_SIM_PRO";
    private static final String DE_ACTION_DALTONIAN_SIM_TRI = "ACTION_DALTONIAN_SIM_TRI";
    private static final String DE_ACTION_DALTONIAN_TRI = "ACTION_DALTONIAN_TRI";
    private static final String DE_ACTION_MODE_OFF = "ACTION_MODE_OFF";
    private static final String DE_ACTION_MODE_ON = "ACTION_MODE_ON";
    private static final String DE_FEATURE_COLOR_INVERSE = "FEATURE_COLOR_INVERSE";
    private static final String DE_FEATURE_DALTONIAN = "FEATURE_DALTONIAN";
    private static final String DE_SCENE_COLOR_INVERSE = "SCENE_COLOR_INVERSE";
    private static final String DE_SCENE_DALTONIAN = "SCENE_DALTONIAN";
    private static final String FUNCTION_DUMP = "dump";
    private static final String FUNCTION_REGISTER_UI_TEST_AUTOMATION_SERVICE = "registerUiTestAutomationService";
    private static final String GET_WINDOW_TOKEN = "getWindowToken";
    private static final String LOG_TAG = "AccessibilityManagerService";
    public static final int MAGNIFICATION_GESTURE_HANDLER_ID = 0;
    /* access modifiers changed from: private */
    public static final int OWN_PROCESS_ID = Process.myPid();
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
    private final AppOpsManager mAppOpsManager;
    private AppWidgetManagerInternal mAppWidgetService;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentUserId = 0;
    private int mDaltonizerMode = -1;
    private HwServiceFactory.IDisplayEngineInterface mDisplayEngineInterface = null;
    private AlertDialog mEnableTouchExplorationDialog;
    private FingerprintGestureDispatcher mFingerprintGestureDispatcher;
    private boolean mFirstBoot = true;
    /* access modifiers changed from: private */
    public final GlobalActionPerformer mGlobalActionPerformer;
    private final RemoteCallbackList<IAccessibilityManagerClient> mGlobalClients = new RemoteCallbackList<>();
    private final SparseArray<RemoteAccessibilityConnection> mGlobalInteractionConnections = new SparseArray<>();
    private final SparseArray<IBinder> mGlobalWindowTokens = new SparseArray<>();
    /* access modifiers changed from: private */
    public boolean mHasInputFilter;
    private boolean mInitialized;
    /* access modifiers changed from: private */
    public AccessibilityInputFilter mInputFilter;
    private InteractionBridge mInteractionBridge;
    private boolean mInvertColors = false;
    private boolean mIsAccessibilityButtonShown;
    private KeyEventDispatcher mKeyEventDispatcher;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private MagnificationController mMagnificationController;
    /* access modifiers changed from: private */
    public final MainHandler mMainHandler;
    private MotionEventInjector mMotionEventInjector;
    /* access modifiers changed from: private */
    public final PackageManager mPackageManager;
    private RemoteAccessibilityConnection mPictureInPictureActionReplacingConnection;
    private final PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public final SecurityPolicy mSecurityPolicy;
    private final TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(COMPONENT_NAME_SEPARATOR);
    private boolean mSupportColorInverse = false;
    private boolean mSupportDaltonian = false;
    private final List<AccessibilityServiceInfo> mTempAccessibilityServiceInfoList = new ArrayList();
    private final Set<ComponentName> mTempComponentNameSet = new HashSet();
    private final IntArray mTempIntArray = new IntArray(0);
    /* access modifiers changed from: private */
    public final Point mTempPoint = new Point();
    /* access modifiers changed from: private */
    public final Rect mTempRect = new Rect();
    /* access modifiers changed from: private */
    public final Rect mTempRect1 = new Rect();
    /* access modifiers changed from: private */
    public final UiAutomationManager mUiAutomationManager = new UiAutomationManager();
    /* access modifiers changed from: private */
    public final UserManager mUserManager;
    /* access modifiers changed from: private */
    public final SparseArray<UserState> mUserStates = new SparseArray<>();
    /* access modifiers changed from: private */
    public final WindowManagerInternal mWindowManagerService;
    /* access modifiers changed from: private */
    public WindowsForAccessibilityCallback mWindowsForAccessibilityCallback;

    private final class AccessibilityContentObserver extends ContentObserver {
        private final Uri mAccessibilityButtonComponentIdUri = Settings.Secure.getUriFor("accessibility_button_target_component");
        private final Uri mAccessibilityShortcutServiceIdUri = Settings.Secure.getUriFor("accessibility_shortcut_target_service");
        private final Uri mAccessibilitySoftKeyboardModeUri = Settings.Secure.getUriFor("accessibility_soft_keyboard_mode");
        private final Uri mAutoclickEnabledUri = Settings.Secure.getUriFor("accessibility_autoclick_enabled");
        private final Uri mDisplayDaltonizerEnabledUri = Settings.Secure.getUriFor("accessibility_display_daltonizer_enabled");
        private final Uri mDisplayDaltonizerUri = Settings.Secure.getUriFor("accessibility_display_daltonizer");
        private final Uri mDisplayInversionEnabledUri = Settings.Secure.getUriFor("accessibility_display_inversion_enabled");
        private final Uri mDisplayMagnificationEnabledUri = Settings.Secure.getUriFor("accessibility_display_magnification_enabled");
        private final Uri mEnabledAccessibilityServicesUri = Settings.Secure.getUriFor("enabled_accessibility_services");
        private final Uri mHighTextContrastUri = Settings.Secure.getUriFor("high_text_contrast_enabled");
        private final Uri mNavBarMagnificationEnabledUri = Settings.Secure.getUriFor("accessibility_display_magnification_navbar_enabled");
        private final Uri mTouchExplorationEnabledUri = Settings.Secure.getUriFor("touch_exploration_enabled");
        private final Uri mTouchExplorationGrantedAccessibilityServicesUri = Settings.Secure.getUriFor("touch_exploration_granted_accessibility_services");

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
            contentResolver.registerContentObserver(this.mDisplayInversionEnabledUri, false, this, -1);
            contentResolver.registerContentObserver(this.mDisplayDaltonizerEnabledUri, false, this, -1);
            contentResolver.registerContentObserver(this.mDisplayDaltonizerUri, false, this, -1);
            contentResolver.registerContentObserver(this.mHighTextContrastUri, false, this, -1);
            contentResolver.registerContentObserver(this.mAccessibilitySoftKeyboardModeUri, false, this, -1);
            contentResolver.registerContentObserver(this.mAccessibilityShortcutServiceIdUri, false, this, -1);
            contentResolver.registerContentObserver(this.mAccessibilityButtonComponentIdUri, false, this, -1);
        }

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
                            } else if (!this.mTouchExplorationGrantedAccessibilityServicesUri.equals(uri)) {
                                if (!this.mDisplayDaltonizerEnabledUri.equals(uri)) {
                                    if (!this.mDisplayDaltonizerUri.equals(uri)) {
                                        if (this.mDisplayInversionEnabledUri.equals(uri)) {
                                            AccessibilityManagerService.this.updateDisplayInversionLocked(userState);
                                        } else if (this.mHighTextContrastUri.equals(uri)) {
                                            if (AccessibilityManagerService.this.readHighTextContrastEnabledSettingLocked(userState)) {
                                                AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                            }
                                        } else if (this.mAccessibilitySoftKeyboardModeUri.equals(uri)) {
                                            if (AccessibilityManagerService.this.readSoftKeyboardShowModeChangedLocked(userState)) {
                                                AccessibilityManagerService.this.notifySoftKeyboardShowModeChangedLocked(userState.mSoftKeyboardShowMode);
                                                AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                            }
                                        } else if (this.mAccessibilityShortcutServiceIdUri.equals(uri)) {
                                            if (AccessibilityManagerService.this.readAccessibilityShortcutSettingLocked(userState)) {
                                                AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                            }
                                        } else if (this.mAccessibilityButtonComponentIdUri.equals(uri) && AccessibilityManagerService.this.readAccessibilityButtonSettingsLocked(userState)) {
                                            AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                        }
                                    }
                                }
                                AccessibilityManagerService.this.updateDisplayDaltonizerLocked(userState);
                            } else if (AccessibilityManagerService.this.readTouchExplorationGrantedAccessibilityServicesLocked(userState)) {
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

    class Client {
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

    private final class InteractionBridge {
        private final ComponentName COMPONENT_NAME = new ComponentName("com.android.server.accessibility", "InteractionBridge");
        private final AccessibilityInteractionClient mClient;
        private final int mConnectionId;
        private final Display mDefaultDisplay;
        final /* synthetic */ AccessibilityManagerService this$0;

        /* JADX INFO: finally extract failed */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x008a, code lost:
            r0 = th;
         */
        public InteractionBridge(AccessibilityManagerService accessibilityManagerService) {
            UserState userState;
            AccessibilityManagerService accessibilityManagerService2 = accessibilityManagerService;
            this.this$0 = accessibilityManagerService2;
            AccessibilityServiceInfo info = new AccessibilityServiceInfo();
            info.setCapabilities(1);
            info.flags |= 64;
            info.flags |= 2;
            synchronized (accessibilityManagerService.mLock) {
                try {
                    userState = accessibilityManagerService.getCurrentUserStateLocked();
                } catch (Throwable th) {
                    th = th;
                    AccessibilityServiceInfo accessibilityServiceInfo = info;
                    while (true) {
                        throw th;
                    }
                }
            }
            AccessibilityServiceInfo accessibilityServiceInfo2 = info;
            final AccessibilityManagerService accessibilityManagerService3 = accessibilityManagerService2;
            AnonymousClass1 r1 = new AccessibilityServiceConnection(this, userState, accessibilityManagerService.mContext, this.COMPONENT_NAME, info, AccessibilityManagerService.access$2508(), accessibilityManagerService.mMainHandler, accessibilityManagerService.mLock, accessibilityManagerService.mSecurityPolicy, accessibilityManagerService2, accessibilityManagerService.mWindowManagerService, accessibilityManagerService.mGlobalActionPerformer) {
                final /* synthetic */ InteractionBridge this$1;

                {
                    this.this$1 = this$1;
                }

                public boolean supportsFlagForNotImportantViews(AccessibilityServiceInfo info) {
                    return true;
                }
            };
            this.mConnectionId = r1.mId;
            this.mClient = AccessibilityInteractionClient.getInstance();
            AccessibilityInteractionClient accessibilityInteractionClient = this.mClient;
            AccessibilityInteractionClient.addConnection(this.mConnectionId, r1);
            this.mDefaultDisplay = ((DisplayManager) accessibilityManagerService.mContext.getSystemService("display")).getDisplay(0);
        }

        public void clearAccessibilityFocusNotLocked(int windowId) {
            AccessibilityNodeInfo focus = getAccessibilityFocusNotLocked(windowId);
            if (focus != null) {
                focus.performAction(128);
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
            synchronized (this.this$0.mLock) {
                Rect boundsInScreen = this.this$0.mTempRect;
                focus.getBoundsInScreen(boundsInScreen);
                MagnificationSpec spec = this.this$0.getCompatibleMagnificationSpecLocked(focus.getWindowId());
                if (spec != null && !spec.isNop()) {
                    boundsInScreen.offset((int) (-spec.offsetX), (int) (-spec.offsetY));
                    boundsInScreen.scale(1.0f / spec.scale);
                }
                Rect windowBounds = this.this$0.mTempRect1;
                this.this$0.getWindowBounds(focus.getWindowId(), windowBounds);
                if (!boundsInScreen.intersect(windowBounds)) {
                    return false;
                }
                Point screenSize = this.this$0.mTempPoint;
                this.mDefaultDisplay.getRealSize(screenSize);
                if (!boundsInScreen.intersect(0, 0, screenSize.x, screenSize.y)) {
                    return false;
                }
                outPoint.set(boundsInScreen.centerX(), boundsInScreen.centerY());
                return true;
            }
        }

        private AccessibilityNodeInfo getAccessibilityFocusNotLocked() {
            synchronized (this.this$0.mLock) {
                int focusedWindowId = this.this$0.mSecurityPolicy.mAccessibilityFocusedWindowId;
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

    final class MainHandler extends Handler {
        public static final int MSG_SEND_KEY_EVENT_TO_INPUT_FILTER = 8;

        public MainHandler(Looper looper) {
            super(looper);
        }

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

    class RemoteAccessibilityConnection implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final IAccessibilityInteractionConnection mConnection;
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

        public void binderDied() {
            unlinkToDeath();
            synchronized (AccessibilityManagerService.this.mLock) {
                AccessibilityManagerService.this.removeAccessibilityInteractionConnectionLocked(this.mWindowId, this.mUserId);
            }
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
        private boolean mTouchInteractionInProgress;
        public SparseArray<WindowInfo> mWindowInfoById = new SparseArray<>();
        public List<AccessibilityWindowInfo> mWindows;

        public SecurityPolicy() {
        }

        /* access modifiers changed from: private */
        public boolean canDispatchAccessibilityEventLocked(AccessibilityEvent event) {
            switch (event.getEventType()) {
                case 32:
                case 64:
                case 128:
                case 256:
                case 512:
                case 1024:
                case 16384:
                case 262144:
                case DumpState.DUMP_FROZEN /*524288*/:
                case DumpState.DUMP_DEXOPT /*1048576*/:
                case DumpState.DUMP_COMPILER_STATS /*2097152*/:
                case DumpState.DUMP_CHANGES /*4194304*/:
                case DumpState.DUMP_SERVICE_PERMISSIONS /*16777216*/:
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
            AppWidgetManagerInternal appWidgetManager = AccessibilityManagerService.this.getAppWidgetManager();
            if (appWidgetManager != null && ArrayUtils.contains(appWidgetManager.getHostedWidgetPackages(resolvedUid), packageNameStr)) {
                return packageName.toString();
            }
            String[] packageNames = AccessibilityManagerService.this.mPackageManager.getPackagesForUid(resolvedUid);
            if (ArrayUtils.isEmpty(packageNames)) {
                return null;
            }
            return packageNames[0];
        }

        /* access modifiers changed from: package-private */
        public String[] computeValidReportedPackages(int callingUid, String targetPackage, int targetUid) {
            if (UserHandle.getAppId(callingUid) == 1000) {
                return EmptyArray.STRING;
            }
            String[] uidPackages = {targetPackage};
            AppWidgetManagerInternal appWidgetManager = AccessibilityManagerService.this.getAppWidgetManager();
            if (appWidgetManager != null) {
                ArraySet<String> widgetPackages = appWidgetManager.getHostedWidgetPackages(targetUid);
                if (widgetPackages != null && !widgetPackages.isEmpty()) {
                    String[] validPackages = new String[(uidPackages.length + widgetPackages.size())];
                    System.arraycopy(uidPackages, 0, validPackages, 0, uidPackages.length);
                    int widgetPackageCount = widgetPackages.size();
                    for (int i = 0; i < widgetPackageCount; i++) {
                        validPackages[uidPackages.length + i] = widgetPackages.valueAt(i);
                    }
                    return validPackages;
                }
            }
            return uidPackages;
        }

        /* access modifiers changed from: private */
        public boolean getBindInstantServiceAllowed(int userId) {
            AccessibilityManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_BIND_INSTANT_SERVICE", "getBindInstantServiceAllowed");
            UserState state = (UserState) AccessibilityManagerService.this.mUserStates.get(userId);
            return state != null && state.mBindInstantServiceAllowed;
        }

        /* access modifiers changed from: private */
        public void setBindInstantServiceAllowed(int userId, boolean allowed) {
            AccessibilityManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_BIND_INSTANT_SERVICE", "setBindInstantServiceAllowed");
            UserState state = (UserState) AccessibilityManagerService.this.mUserStates.get(userId);
            if (state == null) {
                if (allowed) {
                    state = new UserState(userId);
                    AccessibilityManagerService.this.mUserStates.put(userId, state);
                } else {
                    return;
                }
            }
            if (state.mBindInstantServiceAllowed != allowed) {
                state.mBindInstantServiceAllowed = allowed;
                AccessibilityManagerService.this.onUserStateChangedLocked(state);
            }
        }

        public void clearWindowsLocked() {
            List<WindowInfo> windows = Collections.emptyList();
            int activeWindowId = this.mActiveWindowId;
            updateWindowsLocked(windows);
            this.mActiveWindowId = activeWindowId;
            this.mWindows = null;
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
            this.mFocusedWindowId = -1;
            if (!this.mTouchInteractionInProgress) {
                this.mActiveWindowId = -1;
            }
            int windowCount = windows.size();
            if (windowCount > 0) {
                int activeWindowGone = true;
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
                        this.mWindows.add(window);
                        this.mA11yWindowInfoById.put(windowId, window);
                        this.mWindowInfoById.put(windowId, WindowInfo.obtain(windowInfo));
                    }
                }
                if (this.mTouchInteractionInProgress != 0 && activeWindowGone) {
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
                int accessibilityWindowCount2 = activeWindowGone;
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
            if (this.mWindows == null) {
                return false;
            }
            Region windowInteractiveRegion = null;
            boolean windowInteractiveRegionChanged = false;
            for (int i = this.mWindows.size() - 1; i >= 0; i--) {
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
            if (!this.mTouchInteractionInProgress) {
                this.mActiveWindowId = getFocusedWindowId();
            }
            return this.mActiveWindowId;
        }

        private void setActiveWindowLocked(int windowId) {
            if (this.mActiveWindowId != windowId) {
                AccessibilityManagerService.this.sendAccessibilityEventLocked(AccessibilityEvent.obtainWindowsChangedEvent(this.mActiveWindowId, 32), AccessibilityManagerService.this.mCurrentUserId);
                this.mActiveWindowId = windowId;
                if (this.mWindows != null) {
                    int windowCount = this.mWindows.size();
                    for (int i = 0; i < windowCount; i++) {
                        AccessibilityWindowInfo window = this.mWindows.get(i);
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
            if (this.mAccessibilityFocusedWindowId != windowId) {
                AccessibilityManagerService.this.sendAccessibilityEventLocked(AccessibilityEvent.obtainWindowsChangedEvent(this.mAccessibilityFocusedWindowId, 128), AccessibilityManagerService.this.mCurrentUserId);
                this.mAccessibilityFocusedWindowId = windowId;
                if (this.mWindows != null) {
                    int windowCount = this.mWindows.size();
                    for (int i = 0; i < windowCount; i++) {
                        AccessibilityWindowInfo window = this.mWindows.get(i);
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
        public int resolveProfileParentLocked(int userId) {
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
            boolean z = true;
            if (Binder.getCallingUid() == 1000) {
                return true;
            }
            if (Binder.getCallingUid() == 2000 && !isShellAllowedToRetrieveWindowLocked(windowId)) {
                return false;
            }
            if (windowId == this.mActiveWindowId) {
                return true;
            }
            if (findA11yWindowInfoById(windowId) == null) {
                z = false;
            }
            return z;
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
        public WindowInfo findWindowInfoById(int windowId) {
            return this.mWindowInfoById.get(windowId);
        }

        /* access modifiers changed from: private */
        public AccessibilityWindowInfo getPictureInPictureWindow() {
            if (this.mWindows != null) {
                int windowCount = this.mWindows.size();
                for (int i = 0; i < windowCount; i++) {
                    AccessibilityWindowInfo window = this.mWindows.get(i);
                    if (window.isInPictureInPictureMode()) {
                        return window;
                    }
                }
            }
            return null;
        }

        /* access modifiers changed from: private */
        public void enforceCallingPermission(String permission, String function) {
            if (AccessibilityManagerService.OWN_PROCESS_ID != Binder.getCallingPid() && !hasPermission(permission)) {
                throw new SecurityException("You do not have " + permission + " required to call " + function + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            }
        }

        private boolean hasPermission(String permission) {
            return AccessibilityManagerService.this.mContext.checkCallingPermission(permission) == 0;
        }

        private int getFocusedWindowId() {
            int access$2400;
            IBinder token = AccessibilityManagerService.this.mWindowManagerService.getFocusedWindowToken();
            synchronized (AccessibilityManagerService.this.mLock) {
                access$2400 = AccessibilityManagerService.this.findWindowIdLocked(token);
            }
            return access$2400;
        }
    }

    public class UserState {
        public boolean mAccessibilityFocusOnlyInActiveWindow;
        public boolean mBindInstantServiceAllowed;
        /* access modifiers changed from: private */
        public final Set<ComponentName> mBindingServices = new HashSet();
        public final ArrayList<AccessibilityServiceConnection> mBoundServices = new ArrayList<>();
        public final Map<ComponentName, AccessibilityServiceConnection> mComponentNameToServiceMap = new HashMap();
        public final Set<ComponentName> mEnabledServices = new HashSet();
        public final List<AccessibilityServiceInfo> mInstalledServices = new ArrayList();
        public final SparseArray<RemoteAccessibilityConnection> mInteractionConnections = new SparseArray<>();
        public boolean mIsAutoclickEnabled;
        public boolean mIsDisplayMagnificationEnabled;
        public boolean mIsFilterKeyEventsEnabled;
        public boolean mIsNavBarMagnificationAssignedToAccessibilityButton;
        public boolean mIsNavBarMagnificationEnabled;
        public boolean mIsPerformGesturesEnabled;
        public boolean mIsTextHighContrastEnabled;
        public boolean mIsTouchExplorationEnabled;
        public int mLastSentClientState = -1;
        public ComponentName mServiceAssignedToAccessibilityButton;
        public ComponentName mServiceChangingSoftKeyboardMode;
        public ComponentName mServiceToEnableWithShortcut;
        public int mSoftKeyboardShowMode = 0;
        public final Set<ComponentName> mTouchExplorationGrantedServices = new HashSet();
        public final RemoteCallbackList<IAccessibilityManagerClient> mUserClients = new RemoteCallbackList<>();
        public final int mUserId;
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
            this.mEnabledServices.clear();
            this.mTouchExplorationGrantedServices.clear();
            this.mIsTouchExplorationEnabled = false;
            this.mIsDisplayMagnificationEnabled = false;
            this.mIsNavBarMagnificationEnabled = false;
            this.mServiceAssignedToAccessibilityButton = null;
            this.mIsNavBarMagnificationAssignedToAccessibilityButton = false;
            this.mIsAutoclickEnabled = false;
            this.mSoftKeyboardShowMode = 0;
        }

        public void addServiceLocked(AccessibilityServiceConnection serviceConnection) {
            if (!this.mBoundServices.contains(serviceConnection)) {
                serviceConnection.onAdded();
                this.mBoundServices.add(serviceConnection);
                Slog.i(AccessibilityManagerService.LOG_TAG, "volume_debug addServiceLocked:" + serviceConnection.mComponentName.getPackageName());
                this.mComponentNameToServiceMap.put(serviceConnection.mComponentName, serviceConnection);
                AccessibilityManagerService.this.scheduleNotifyClientsOfServicesStateChange(this);
                if (AccessibilityManagerService.this.isTalkBackService(serviceConnection.mComponentName)) {
                    AccessibilityManagerService.this.notifyAccessibilityEnableChangedEvent(1);
                }
            }
        }

        public void removeServiceLocked(AccessibilityServiceConnection serviceConnection) {
            Slog.i(AccessibilityManagerService.LOG_TAG, "volume_debug removeServiceLocked:" + serviceConnection.mComponentName.getPackageName());
            this.mBoundServices.remove(serviceConnection);
            serviceConnection.onRemoved();
            this.mComponentNameToServiceMap.clear();
            for (int i = 0; i < this.mBoundServices.size(); i++) {
                AccessibilityServiceConnection boundClient = this.mBoundServices.get(i);
                this.mComponentNameToServiceMap.put(boundClient.mComponentName, boundClient);
            }
            AccessibilityManagerService.this.scheduleNotifyClientsOfServicesStateChange(this);
            if (AccessibilityManagerService.this.isTalkBackService(serviceConnection.mComponentName)) {
                AccessibilityManagerService.this.notifyAccessibilityEnableChangedEvent(0);
            }
        }

        public Set<ComponentName> getBindingServicesLocked() {
            return this.mBindingServices;
        }
    }

    final class WindowsForAccessibilityCallback implements WindowManagerInternal.WindowsForAccessibilityCallback {
        WindowsForAccessibilityCallback() {
        }

        public void onWindowsForAccessibilityChanged(List<WindowInfo> windows) {
            synchronized (AccessibilityManagerService.this.mLock) {
                AccessibilityManagerService.this.mSecurityPolicy.updateWindowsLocked(windows);
                AccessibilityManagerService.this.mLock.notifyAll();
            }
        }

        /* access modifiers changed from: private */
        public AccessibilityWindowInfo populateReportedWindowLocked(WindowInfo window) {
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

        /* JADX WARNING: Code restructure failed: missing block: B:15:0x001b, code lost:
            return 3;
         */
        private int getTypeForWindowManagerWindowType(int windowType) {
            switch (windowType) {
                case 1:
                case 2:
                case 3:
                case 4:
                    break;
                default:
                    switch (windowType) {
                        case 1000:
                        case NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE /*1001*/:
                        case 1002:
                        case 1003:
                            break;
                        default:
                            switch (windowType) {
                                case IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME /*2000*/:
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
                                            switch (windowType) {
                                                case 2019:
                                                case 2020:
                                                    break;
                                                default:
                                                    switch (windowType) {
                                                        case 1005:
                                                            break;
                                                        case 2014:
                                                        case 2017:
                                                        case 2024:
                                                        case 2036:
                                                        case 2038:
                                                            break;
                                                        case 2032:
                                                            return 4;
                                                        case 2034:
                                                            return 5;
                                                        default:
                                                            return -1;
                                                    }
                                            }
                                    }
                            }
                    }
            }
            return 1;
        }
    }

    static /* synthetic */ int access$2508() {
        int i = sIdCounter;
        sIdCounter = i + 1;
        return i;
    }

    /* access modifiers changed from: private */
    public UserState getCurrentUserStateLocked() {
        return getUserStateLocked(this.mCurrentUserId);
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
        registerBroadcastReceivers();
        new AccessibilityContentObserver(this.mMainHandler).register(context.getContentResolver());
    }

    public int getCurrentUserIdLocked() {
        return this.mCurrentUserId;
    }

    public boolean isAccessibilityButtonShown() {
        return this.mIsAccessibilityButtonShown;
    }

    public FingerprintGestureDispatcher getFingerprintGestureDispatcher() {
        return this.mFingerprintGestureDispatcher;
    }

    private UserState getUserState(int userId) {
        UserState userStateLocked;
        synchronized (this.mLock) {
            userStateLocked = getUserStateLocked(userId);
        }
        return userStateLocked;
    }

    /* access modifiers changed from: private */
    public UserState getUserStateLocked(int userId) {
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
        return this.mSecurityPolicy.getBindInstantServiceAllowed(userId);
    }

    /* access modifiers changed from: package-private */
    public void setBindInstantServiceAllowed(int userId, boolean allowed) {
        this.mSecurityPolicy.setBindInstantServiceAllowed(userId, allowed);
    }

    private void registerBroadcastReceivers() {
        new PackageMonitor() {
            /* JADX WARNING: Code restructure failed: missing block: B:11:0x002e, code lost:
                return;
             */
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

            /* JADX WARNING: Code restructure failed: missing block: B:16:0x0049, code lost:
                return;
             */
            public void onPackageUpdateFinished(String packageName, int uid) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    int userId = getChangingUserId();
                    if (userId == AccessibilityManagerService.this.mCurrentUserId) {
                        UserState userState = AccessibilityManagerService.this.getUserStateLocked(userId);
                        boolean unboundAService = false;
                        for (int i = userState.mBoundServices.size() - 1; i >= 0; i--) {
                            AccessibilityServiceConnection boundService = userState.mBoundServices.get(i);
                            if (boundService.mComponentName.getPackageName().equals(packageName)) {
                                boundService.unbindLocked();
                                unboundAService = true;
                            }
                        }
                        if (unboundAService) {
                            AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                        }
                    }
                }
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
                String[] strArr = packages;
                synchronized (AccessibilityManagerService.this.mLock) {
                    int userId = getChangingUserId();
                    if (userId != AccessibilityManagerService.this.mCurrentUserId) {
                        return false;
                    }
                    UserState userState = AccessibilityManagerService.this.getUserStateLocked(userId);
                    Iterator<ComponentName> it = userState.mEnabledServices.iterator();
                    while (it.hasNext()) {
                        String compPkg = it.next().getPackageName();
                        for (String pkg : strArr) {
                            if (compPkg.equals(pkg)) {
                                if (!doit) {
                                    return true;
                                }
                                if (HwDeviceManager.disallowOp(53, pkg)) {
                                    Slog.w(AccessibilityManagerService.LOG_TAG, " onHandleForceStop. this forcestop pkg =" + compPkg + " in MDM accessibility whitelist ,block!");
                                } else {
                                    it.remove();
                                    AccessibilityManagerService.this.persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, userId);
                                    AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                }
                            }
                        }
                    }
                    return false;
                }
            }
        }.register(this.mContext, null, UserHandle.ALL, true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.os.action.SETTING_RESTORED");
        intentFilter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    AccessibilityManagerService.this.switchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
                    UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                    synchronized (AccessibilityManagerService.this.mLock) {
                        boolean unused = AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState);
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
        synchronized (this.mLock) {
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            UserState userState = getUserStateLocked(resolvedUserId);
            Client client = new Client(callback, Binder.getCallingUid(), userState);
            Client client2 = client;
            if (this.mSecurityPolicy.isCallerInteractingAcrossUsers(userId)) {
                this.mGlobalClients.register(callback, client2);
                long of = IntPair.of(userState.getClientState(), client2.mLastSentRelevantEventTypes);
                return of;
            }
            userState.mUserClients.register(callback, client2);
            long of2 = IntPair.of(resolvedUserId == this.mCurrentUserId ? userState.getClientState() : 0, client2.mLastSentRelevantEventTypes);
            return of2;
        }
    }

    public void sendAccessibilityEvent(AccessibilityEvent event, int userId) {
        boolean dispatchEvent = false;
        synchronized (this.mLock) {
            if (event.getWindowId() == -3) {
                AccessibilityWindowInfo pip = this.mSecurityPolicy.getPictureInPictureWindow();
                if (pip != null) {
                    event.setWindowId(pip.getId());
                }
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
                List<AccessibilityServiceInfo> emptyList = Collections.emptyList();
                return emptyList;
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

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0037, code lost:
        r0 = r4;
        r1 = 0;
        r2 = r0.size();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003d, code lost:
        if (r1 >= r2) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r0.get(r1).onInterrupt();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0049, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004a, code lost:
        android.util.Slog.e(LOG_TAG, "Error sending interrupt request to " + r0.get(r1), r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0067, code lost:
        return;
     */
    public void interrupt(int userId) {
        synchronized (this.mLock) {
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            if (resolvedUserId == this.mCurrentUserId) {
                List<AccessibilityServiceConnection> services = getUserStateLocked(resolvedUserId).mBoundServices;
                int numServices = services.size();
                List<IAccessibilityServiceClient> arrayList = new ArrayList<>(numServices);
                for (int i = 0; i < numServices; i++) {
                    AccessibilityServiceConnection service = services.get(i);
                    IBinder a11yServiceBinder = service.mService;
                    IAccessibilityServiceClient a11yServiceInterface = service.mServiceInterface;
                    if (!(a11yServiceBinder == null || a11yServiceInterface == null)) {
                        arrayList.add(a11yServiceInterface);
                    }
                }
            } else {
                return;
            }
        }
        int i2 = i2 + 1;
    }

    public int addAccessibilityInteractionConnection(IWindow windowToken, IAccessibilityInteractionConnection connection, String packageName, int userId) throws RemoteException {
        Object obj;
        String str;
        int windowId;
        int i = userId;
        Object obj2 = this.mLock;
        synchronized (obj2) {
            try {
                int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(i);
                int resolvedUid = UserHandle.getUid(resolvedUserId, UserHandle.getCallingAppId());
                str = packageName;
                try {
                    String packageName2 = this.mSecurityPolicy.resolveValidReportedPackageLocked(str, UserHandle.getCallingAppId(), resolvedUserId);
                    try {
                        int i2 = sNextWindowId;
                        sNextWindowId = i2 + 1;
                        int windowId2 = i2;
                        if (this.mSecurityPolicy.isCallerInteractingAcrossUsers(i)) {
                            RemoteAccessibilityConnection remoteAccessibilityConnection = new RemoteAccessibilityConnection(windowId2, connection, packageName2, resolvedUid, -1);
                            RemoteAccessibilityConnection wrapper = remoteAccessibilityConnection;
                            wrapper.linkToDeath();
                            this.mGlobalInteractionConnections.put(windowId2, wrapper);
                            this.mGlobalWindowTokens.put(windowId2, windowToken.asBinder());
                            windowId = windowId2;
                            obj = obj2;
                        } else {
                            r7 = r7;
                            windowId = windowId2;
                            obj = obj2;
                            RemoteAccessibilityConnection remoteAccessibilityConnection2 = new RemoteAccessibilityConnection(windowId2, connection, packageName2, resolvedUid, resolvedUserId);
                            remoteAccessibilityConnection2.linkToDeath();
                            UserState userState = getUserStateLocked(resolvedUserId);
                            userState.mInteractionConnections.put(windowId, remoteAccessibilityConnection2);
                            userState.mWindowTokens.put(windowId, windowToken.asBinder());
                        }
                        return windowId;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    obj = obj2;
                    String packageName3 = str;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                str = packageName;
                obj = obj2;
                String packageName32 = str;
                throw th;
            }
        }
    }

    public void removeAccessibilityInteractionConnection(IWindow window) {
        synchronized (this.mLock) {
            this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(UserHandle.getCallingUserId());
            IBinder token = window.asBinder();
            if (removeAccessibilityInteractionConnectionInternalLocked(token, this.mGlobalWindowTokens, this.mGlobalInteractionConnections) < 0) {
                int userCount = this.mUserStates.size();
                int i = 0;
                while (i < userCount) {
                    UserState userState = this.mUserStates.valueAt(i);
                    if (removeAccessibilityInteractionConnectionInternalLocked(token, userState.mWindowTokens, userState.mInteractionConnections) < 0) {
                        i++;
                    } else {
                        return;
                    }
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
                RemoteAccessibilityConnection remoteAccessibilityConnection = new RemoteAccessibilityConnection(-3, connection, "foo.bar.baz", 1000, -1);
                this.mPictureInPictureActionReplacingConnection = remoteAccessibilityConnection;
                remoteAccessibilityConnection.linkToDeath();
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
            uiAutomationManager.registerUiTestAutomationServiceLocked(owner, serviceClient, context, accessibilityServiceInfo, i, this.mMainHandler, this.mLock, this.mSecurityPolicy, this, this.mWindowManagerService, this.mGlobalActionPerformer, flags);
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
            IBinder findWindowTokenLocked = findWindowTokenLocked(windowId);
            return findWindowTokenLocked;
        }
    }

    public void notifyAccessibilityButtonClicked() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE") == 0) {
            synchronized (this.mLock) {
                notifyAccessibilityButtonClickedLocked();
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
            boolean notifyKeyEventLocked = getKeyEventDispatcher().notifyKeyEventLocked(event, policyFlags, boundServices);
            return notifyKeyEventLocked;
        }
    }

    public void notifyMagnificationChanged(Region region, float scale, float centerX, float centerY) {
        synchronized (this.mLock) {
            notifyClearAccessibilityCacheLocked();
            notifyMagnificationChangedLocked(region, scale, centerX, centerY);
        }
    }

    /* access modifiers changed from: package-private */
    public void setMotionEventInjector(MotionEventInjector motionEventInjector) {
        synchronized (this.mLock) {
            this.mMotionEventInjector = motionEventInjector;
            this.mLock.notifyAll();
        }
    }

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
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0066, code lost:
        return;
     */
    public void switchUser(int userId) {
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
                    this.mMainHandler.sendMessageDelayed(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$GuW_dQ2mWyy8l4tm19TzFxGbeM.INSTANCE, this), 3000);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void announceNewUserIfNeeded() {
        synchronized (this.mLock) {
            if (getCurrentUserStateLocked().isHandlingAccessibilityEvents()) {
                String message = this.mContext.getString(17041314, new Object[]{((UserManager) this.mContext.getSystemService("user")).getUserInfo(this.mCurrentUserId).name});
                AccessibilityEvent event = AccessibilityEvent.obtain(16384);
                event.getText().add(message);
                sendAccessibilityEventLocked(event, this.mCurrentUserId);
            }
        }
    }

    /* access modifiers changed from: private */
    public void unlockUser(int userId) {
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.resolveProfileParentLocked(userId) == this.mCurrentUserId) {
                onUserStateChangedLocked(getUserStateLocked(this.mCurrentUserId));
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeUser(int userId) {
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
                this.mInteractionBridge = new InteractionBridge(this);
            }
            interactionBridge = this.mInteractionBridge;
        }
        return interactionBridge;
    }

    private boolean notifyGestureLocked(int gestureId, boolean isDefault) {
        UserState state = getCurrentUserStateLocked();
        int i = state.mBoundServices.size() - 1;
        while (i >= 0) {
            AccessibilityServiceConnection service = state.mBoundServices.get(i);
            if (!service.mRequestTouchExplorationMode || service.mIsDefault != isDefault) {
                i--;
            } else {
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

    private void notifyMagnificationChangedLocked(Region region, float scale, float centerX, float centerY) {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            state.mBoundServices.get(i).notifyMagnificationChangedLocked(region, scale, centerX, centerY);
        }
    }

    /* access modifiers changed from: private */
    public void notifySoftKeyboardShowModeChangedLocked(int showMode) {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            state.mBoundServices.get(i).notifySoftKeyboardShowModeChangedLocked(showMode);
        }
    }

    private void notifyAccessibilityButtonClickedLocked() {
        UserState state = getCurrentUserStateLocked();
        int potentialTargets = state.mIsNavBarMagnificationEnabled;
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            if (state.mBoundServices.get(i).mRequestAccessibilityButton) {
                potentialTargets++;
            }
        }
        if (potentialTargets != 0) {
            if (potentialTargets != 1) {
                if (state.mServiceAssignedToAccessibilityButton == null && !state.mIsNavBarMagnificationAssignedToAccessibilityButton) {
                    this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$jMhXm1Zlw_GKL4YQWGQVpZTTP4.INSTANCE, this));
                } else if (!state.mIsNavBarMagnificationEnabled || !state.mIsNavBarMagnificationAssignedToAccessibilityButton) {
                    int i2 = state.mBoundServices.size() - 1;
                    while (i2 >= 0) {
                        AccessibilityServiceConnection service = state.mBoundServices.get(i2);
                        if (!service.mRequestAccessibilityButton || !service.mComponentName.equals(state.mServiceAssignedToAccessibilityButton)) {
                            i2--;
                        } else {
                            service.notifyAccessibilityButtonClickedLocked();
                            return;
                        }
                    }
                } else {
                    this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$UqJpHUxGOFXcIrjlVaMMAhCDDjA.INSTANCE, this));
                    return;
                }
                this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$jMhXm1Zlw_GKL4YQWGQVpZTTP4.INSTANCE, this));
            } else if (state.mIsNavBarMagnificationEnabled) {
                this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$UqJpHUxGOFXcIrjlVaMMAhCDDjA.INSTANCE, this));
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
    public void sendAccessibilityButtonToInputFilter() {
        synchronized (this.mLock) {
            if (this.mHasInputFilter && this.mInputFilter != null) {
                this.mInputFilter.notifyAccessibilityButtonClicked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void showAccessibilityButtonTargetSelection() {
        Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
        intent.addFlags(268468224);
        this.mContext.startActivityAsUser(intent, UserHandle.of(this.mCurrentUserId));
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
    public void removeAccessibilityInteractionConnectionLocked(int windowId, int userId) {
        if (userId == -1) {
            this.mGlobalWindowTokens.remove(windowId);
            this.mGlobalInteractionConnections.remove(windowId);
            return;
        }
        UserState userState = getCurrentUserStateLocked();
        userState.mWindowTokens.remove(windowId);
        userState.mInteractionConnections.remove(windowId);
    }

    private boolean readInstalledAccessibilityServiceLocked(UserState userState) {
        this.mTempAccessibilityServiceInfoList.clear();
        int flags = 819332;
        if (userState.mBindInstantServiceAllowed) {
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
            Slog.w(LOG_TAG, "Skipping accessibility service " + new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToShortString() + ": it does not require the permission " + "android.permission.BIND_ACCESSIBILITY_SERVICE");
            return false;
        }
        if (this.mAppOpsManager.noteOpNoThrow("android:bind_accessibility_service", serviceInfo.applicationInfo.uid, serviceInfo.packageName) == 0) {
            return true;
        }
        Slog.w(LOG_TAG, "Skipping accessibility service " + new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToShortString() + ": disallowed by AppOps");
        return false;
    }

    /* access modifiers changed from: private */
    public boolean readEnabledAccessibilityServicesLocked(UserState userState) {
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
    public boolean readTouchExplorationGrantedAccessibilityServicesLocked(UserState userState) {
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
            private final /* synthetic */ AccessibilityManagerService.UserState f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                AccessibilityManagerService.this.broadcastToClients(this.f$1, FunctionalUtils.ignoreRemoteException(new FunctionalUtils.RemoteExceptionIgnoringConsumer(this.f$1) {
                    private final /* synthetic */ AccessibilityManagerService.UserState f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void acceptOrThrow(Object obj) {
                        AccessibilityManagerService.lambda$updateRelevantEventsLocked$0(AccessibilityManagerService.this, this.f$1, (AccessibilityManagerService.Client) obj);
                    }
                }));
            }
        });
    }

    public static /* synthetic */ void lambda$updateRelevantEventsLocked$0(AccessibilityManagerService accessibilityManagerService, UserState userState, Client client) throws RemoteException {
        int relevantEventTypes;
        boolean changed = false;
        synchronized (accessibilityManagerService.mLock) {
            relevantEventTypes = accessibilityManagerService.computeRelevantEventTypesLocked(userState, client);
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
    public int computeRelevantEventTypesLocked(UserState userState, Client client) {
        int i;
        int serviceCount = userState.mBoundServices.size();
        int i2 = 0;
        int relevantEventTypes = 0;
        for (int i3 = 0; i3 < serviceCount; i3++) {
            AccessibilityServiceConnection service = userState.mBoundServices.get(i3);
            if (isClientInPackageWhitelist(service.getServiceInfo(), client)) {
                i = service.getRelevantEventTypes();
            } else {
                i = 0;
            }
            relevantEventTypes |= i;
        }
        if (isClientInPackageWhitelist(this.mUiAutomationManager.getServiceInfo(), client)) {
            i2 = this.mUiAutomationManager.getRelevantEventTypes();
        }
        return relevantEventTypes | i2;
    }

    private static boolean isClientInPackageWhitelist(AccessibilityServiceInfo serviceInfo, Client client) {
        int i = 0;
        if (serviceInfo == null) {
            return false;
        }
        String[] clientPackages = client.mPackageNames;
        boolean result = ArrayUtils.isEmpty(serviceInfo.packageNames);
        if (!result && clientPackages != null) {
            int length = clientPackages.length;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (ArrayUtils.contains(serviceInfo.packageNames, clientPackages[i])) {
                    result = true;
                    break;
                }
                i++;
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    public void broadcastToClients(UserState userState, Consumer<Client> clientAction) {
        this.mGlobalClients.broadcastForEachCookie(clientAction);
        userState.mUserClients.broadcastForEachCookie(clientAction);
    }

    /* access modifiers changed from: private */
    public void unbindAllServicesLocked(UserState userState) {
        List<AccessibilityServiceConnection> services = userState.mBoundServices;
        for (int count = services.size(); count > 0; count--) {
            services.get(0).unbindLocked();
        }
    }

    private void readComponentNamesFromSettingLocked(String settingName, int userId, Set<ComponentName> outComponentNames) {
        readComponentNamesFromStringLocked(Settings.Secure.getStringForUser(this.mContext.getContentResolver(), settingName, userId), outComponentNames, false);
    }

    private void readComponentNamesFromStringLocked(String names, Set<ComponentName> outComponentNames, boolean doMerge) {
        if (!doMerge) {
            outComponentNames.clear();
        }
        if (names != null) {
            TextUtils.SimpleStringSplitter splitter = this.mStringColonSplitter;
            splitter.setString(names);
            while (splitter.hasNext()) {
                String str = splitter.next();
                if (str != null && str.length() > 0) {
                    ComponentName enabledService = ComponentName.unflattenFromString(str);
                    if (enabledService != null) {
                        outComponentNames.add(enabledService);
                    }
                }
            }
        }
    }

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
            Settings.Secure.putStringForUser(this.mContext.getContentResolver(), settingName, builder.toString(), userId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void updateServicesLocked(UserState userState) {
        int count;
        Map<ComponentName, AccessibilityServiceConnection> componentNameToServiceMap;
        int i;
        AccessibilityServiceConnection service;
        UserState userState2 = userState;
        Map<ComponentName, AccessibilityServiceConnection> componentNameToServiceMap2 = userState2.mComponentNameToServiceMap;
        boolean isUnlockingOrUnlocked = ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class)).isUserUnlockingOrUnlocked(userState2.mUserId);
        int count2 = userState2.mInstalledServices.size();
        int i2 = 0;
        while (true) {
            int count3 = count2;
            if (i2 >= count3) {
                break;
            } else if (i2 >= userState2.mInstalledServices.size()) {
                Map<ComponentName, AccessibilityServiceConnection> map = componentNameToServiceMap2;
                break;
            } else {
                AccessibilityServiceInfo installedService = userState2.mInstalledServices.get(i2);
                ComponentName componentName = ComponentName.unflattenFromString(installedService.getId());
                AccessibilityServiceConnection service2 = componentNameToServiceMap2.get(componentName);
                if (!isUnlockingOrUnlocked && !installedService.isDirectBootAware()) {
                    Slog.d(LOG_TAG, "Ignoring non-encryption-aware service " + componentName);
                } else if (!userState.mBindingServices.contains(componentName)) {
                    if (!userState2.mEnabledServices.contains(componentName) || this.mUiAutomationManager.suppressingAccessibilityServicesLocked()) {
                        AccessibilityServiceInfo accessibilityServiceInfo = installedService;
                        count = count3;
                        i = i2;
                        componentNameToServiceMap = componentNameToServiceMap2;
                        AccessibilityServiceConnection service3 = service2;
                        if (service3 != null) {
                            service3.unbindLocked();
                        }
                        i2 = i + 1;
                        componentNameToServiceMap2 = componentNameToServiceMap;
                        count2 = count;
                    } else {
                        if (service2 == null) {
                            Context context = this.mContext;
                            int i3 = sIdCounter;
                            sIdCounter = i3 + 1;
                            MainHandler mainHandler = this.mMainHandler;
                            Object obj = this.mLock;
                            componentNameToServiceMap = componentNameToServiceMap2;
                            AccessibilityServiceConnection accessibilityServiceConnection = service2;
                            ComponentName componentName2 = componentName;
                            AccessibilityServiceInfo accessibilityServiceInfo2 = installedService;
                            count = count3;
                            i = i2;
                            AccessibilityServiceConnection service4 = new AccessibilityServiceConnection(userState2, context, componentName, installedService, i3, mainHandler, obj, this.mSecurityPolicy, this, this.mWindowManagerService, this.mGlobalActionPerformer);
                            service = service4;
                        } else {
                            AccessibilityServiceInfo accessibilityServiceInfo3 = installedService;
                            count = count3;
                            i = i2;
                            componentNameToServiceMap = componentNameToServiceMap2;
                            service = service2;
                            if (userState2.mBoundServices.contains(service)) {
                                i2 = i + 1;
                                componentNameToServiceMap2 = componentNameToServiceMap;
                                count2 = count;
                            }
                        }
                        service.bindLocked();
                        i2 = i + 1;
                        componentNameToServiceMap2 = componentNameToServiceMap;
                        count2 = count;
                    }
                }
                count = count3;
                i = i2;
                componentNameToServiceMap = componentNameToServiceMap2;
                i2 = i + 1;
                componentNameToServiceMap2 = componentNameToServiceMap;
                count2 = count;
            }
        }
        int count4 = userState2.mBoundServices.size();
        this.mTempIntArray.clear();
        for (int i4 = 0; i4 < count4; i4++) {
            ResolveInfo resolveInfo = userState2.mBoundServices.get(i4).mAccessibilityServiceInfo.getResolveInfo();
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
    public void scheduleNotifyClientsOfServicesStateChange(UserState userState) {
        this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$687mZTUrupnt857GOjw3XcKgrWE.INSTANCE, this, userState.mUserClients));
    }

    /* access modifiers changed from: private */
    public void sendServicesStateChanged(RemoteCallbackList<IAccessibilityManagerClient> userClients) {
        notifyClientsOfServicesStateChange(this.mGlobalClients);
        notifyClientsOfServicesStateChange(userClients);
    }

    private void notifyClientsOfServicesStateChange(RemoteCallbackList<IAccessibilityManagerClient> clients) {
        clients.broadcast(FunctionalUtils.ignoreRemoteException($$Lambda$AccessibilityManagerService$ffR9e75U5oQEFdGZAdynWg701xE.INSTANCE));
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
                        /* JADX INFO: finally extract failed */
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
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setTitle(17039991).setMessage(this.mContext.getString(17039990, new Object[]{label})).create();
                    this.mEnableTouchExplorationDialog.getWindow().setType(2003);
                    this.mEnableTouchExplorationDialog.getWindow().getAttributes().privateFlags |= 16;
                    this.mEnableTouchExplorationDialog.setCanceledOnTouchOutside(true);
                    this.mEnableTouchExplorationDialog.show();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserStateChangedLocked(UserState userState) {
        this.mInitialized = true;
        updateLegacyCapabilitiesLocked(userState);
        updateServicesLocked(userState);
        updateAccessibilityShortcutLocked(userState);
        updateWindowsForAccessibilityCallbackLocked(userState);
        updateAccessibilityFocusBehaviorLocked(userState);
        updateFilterKeyEventsLocked(userState);
        updateTouchExplorationLocked(userState);
        updatePerformGesturesLocked(userState);
        updateDisplayDaltonizerLocked(userState);
        updateDisplayInversionLocked(userState);
        updateMagnificationLocked(userState);
        updateSoftKeyboardShowModeLocked(userState);
        scheduleUpdateFingerprintGestureHandling(userState);
        scheduleUpdateInputFilter(userState);
        scheduleUpdateClientsIfNeededLocked(userState);
        updateRelevantEventsLocked(userState);
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
            return;
        }
        if (this.mWindowsForAccessibilityCallback != null) {
            this.mWindowsForAccessibilityCallback = null;
            this.mWindowManagerService.setWindowsForAccessibilityCallback(null);
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
        int i = 0;
        while (i < serviceCount) {
            AccessibilityServiceConnection service = userState.mBoundServices.get(i);
            if (!service.mRequestFilterKeyEvents || (service.getCapabilities() & 8) == 0) {
                i++;
            } else {
                Slog.i(LOG_TAG, "volume_debug the the filter package:" + service.mComponentName.getPackageName());
                userState.mIsFilterKeyEventsEnabled = true;
                return;
            }
        }
        userState.mIsFilterKeyEventsEnabled = false;
    }

    /* access modifiers changed from: private */
    public boolean readConfigurationForUserStateLocked(UserState userState) {
        return readInstalledAccessibilityServiceLocked(userState) | readEnabledAccessibilityServicesLocked(userState) | readTouchExplorationGrantedAccessibilityServicesLocked(userState) | readTouchExplorationEnabledSettingLocked(userState) | readHighTextContrastEnabledSettingLocked(userState) | readMagnificationEnabledSettingsLocked(userState) | readAutoclickEnabledSettingLocked(userState) | readAccessibilityShortcutSettingLocked(userState) | readAccessibilityButtonSettingsLocked(userState);
    }

    private void updateAccessibilityEnabledSetting(UserState userState) {
        long identity = Binder.clearCallingIdentity();
        int i = 1;
        boolean isA11yEnabled = this.mUiAutomationManager.isUiAutomationRunningLocked() || userState.isHandlingAccessibilityEvents();
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (!isA11yEnabled) {
                i = 0;
            }
            Settings.Secure.putIntForUser(contentResolver, "accessibility_enabled", i, userState.mUserId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: private */
    public boolean readTouchExplorationEnabledSettingLocked(UserState userState) {
        boolean touchExplorationEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "touch_exploration_enabled", 0, userState.mUserId) == 1;
        if (touchExplorationEnabled == userState.mIsTouchExplorationEnabled) {
            return false;
        }
        userState.mIsTouchExplorationEnabled = touchExplorationEnabled;
        return true;
    }

    /* access modifiers changed from: private */
    public boolean readMagnificationEnabledSettingsLocked(UserState userState) {
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
    public boolean readAutoclickEnabledSettingLocked(UserState userState) {
        boolean autoclickEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_autoclick_enabled", 0, userState.mUserId) == 1;
        if (autoclickEnabled == userState.mIsAutoclickEnabled) {
            return false;
        }
        userState.mIsAutoclickEnabled = autoclickEnabled;
        return true;
    }

    /* access modifiers changed from: private */
    public boolean readHighTextContrastEnabledSettingLocked(UserState userState) {
        boolean highTextContrastEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "high_text_contrast_enabled", 0, userState.mUserId) == 1;
        if (highTextContrastEnabled == userState.mIsTextHighContrastEnabled) {
            return false;
        }
        userState.mIsTextHighContrastEnabled = highTextContrastEnabled;
        return true;
    }

    /* access modifiers changed from: private */
    public boolean readSoftKeyboardShowModeChangedLocked(UserState userState) {
        int softKeyboardShowMode = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0, userState.mUserId);
        if (softKeyboardShowMode == userState.mSoftKeyboardShowMode) {
            return false;
        }
        userState.mSoftKeyboardShowMode = softKeyboardShowMode;
        return true;
    }

    private void updateTouchExplorationLocked(UserState userState) {
        boolean enabled = this.mUiAutomationManager.isTouchExplorationEnabledLocked();
        int serviceCount = userState.mBoundServices.size();
        int i = 0;
        int i2 = 0;
        while (true) {
            if (i2 >= serviceCount) {
                break;
            } else if (canRequestAndRequestsTouchExplorationLocked(userState.mBoundServices.get(i2), userState)) {
                enabled = true;
                break;
            } else {
                i2++;
            }
        }
        if (enabled != userState.mIsTouchExplorationEnabled) {
            userState.mIsTouchExplorationEnabled = enabled;
            long identity = Binder.clearCallingIdentity();
            try {
                ContentResolver contentResolver = this.mContext.getContentResolver();
                if (enabled) {
                    i = 1;
                }
                Settings.Secure.putIntForUser(contentResolver, "touch_exploration_enabled", i, userState.mUserId);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean readAccessibilityShortcutSettingLocked(UserState userState) {
        String componentNameToEnableString = AccessibilityShortcutController.getTargetServiceComponentNameString(this.mContext, userState.mUserId);
        if (componentNameToEnableString != null && !componentNameToEnableString.isEmpty()) {
            ComponentName componentNameToEnable = ComponentName.unflattenFromString(componentNameToEnableString);
            if (componentNameToEnable != null && componentNameToEnable.equals(userState.mServiceToEnableWithShortcut)) {
                return false;
            }
            userState.mServiceToEnableWithShortcut = componentNameToEnable;
            return true;
        } else if (userState.mServiceToEnableWithShortcut == null) {
            return false;
        } else {
            userState.mServiceToEnableWithShortcut = null;
            return true;
        }
    }

    /* access modifiers changed from: private */
    public boolean readAccessibilityButtonSettingsLocked(UserState userState) {
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
            if (this.mEnableTouchExplorationDialog == null || !this.mEnableTouchExplorationDialog.isShowing()) {
                this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AccessibilityManagerService$bNCuysjTCG2afhYMHuqu25CfY5g.INSTANCE, this, service));
            }
        } else if ((service.getCapabilities() & 2) != 0) {
            return true;
        }
        return false;
    }

    private void initDisplayEngineSupported() {
        if (this.mDisplayEngineInterface == null) {
            this.mDisplayEngineInterface = HwServiceFactory.getDisplayEngineInterface();
            if (this.mDisplayEngineInterface != null) {
                this.mDisplayEngineInterface.initialize();
                this.mSupportColorInverse = this.mDisplayEngineInterface.getSupported(DE_FEATURE_COLOR_INVERSE);
                this.mSupportDaltonian = this.mDisplayEngineInterface.getSupported(DE_FEATURE_DALTONIAN);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    public void updateDisplayDaltonizerLocked(UserState userState) {
        initDisplayEngineSupported();
        Slog.i(LOG_TAG, "mSupportDaltonian:" + this.mSupportDaltonian);
        if (!this.mSupportDaltonian) {
            DisplayAdjustmentUtils.applyDaltonizerSetting(this.mContext, userState.mUserId);
        } else {
            ContentResolver cr = this.mContext.getContentResolver();
            int daltonizerMode = -1;
            long identity = Binder.clearCallingIdentity();
            try {
                if (Settings.Secure.getIntForUser(cr, "accessibility_display_daltonizer_enabled", 0, userState.mUserId) != 0) {
                    daltonizerMode = Settings.Secure.getIntForUser(cr, "accessibility_display_daltonizer", 12, userState.mUserId);
                }
                Binder.restoreCallingIdentity(identity);
                Slog.i(LOG_TAG, "daltonizerMode:" + daltonizerMode);
                if (this.mDaltonizerMode == daltonizerMode) {
                    Slog.i(LOG_TAG, "daltonizerMode not change, return");
                    return;
                }
                this.mDaltonizerMode = daltonizerMode;
                String action = DE_ACTION_MODE_OFF;
                switch (daltonizerMode) {
                    case 0:
                        action = DE_ACTION_DALTONIAN_SIM_ALL;
                        break;
                    case 1:
                        action = DE_ACTION_DALTONIAN_SIM_PRO;
                        break;
                    case 2:
                        action = DE_ACTION_DALTONIAN_SIM_DEU;
                        break;
                    case 3:
                        action = DE_ACTION_DALTONIAN_SIM_TRI;
                        break;
                    default:
                        switch (daltonizerMode) {
                            case 11:
                                action = DE_ACTION_DALTONIAN_PRO;
                                break;
                            case 12:
                                action = DE_ACTION_DALTONIAN_DEU;
                                break;
                            case 13:
                                action = DE_ACTION_DALTONIAN_TRI;
                                break;
                        }
                }
                this.mDisplayEngineInterface.setScene(DE_SCENE_DALTONIAN, action);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    public void updateDisplayInversionLocked(UserState userState) {
        initDisplayEngineSupported();
        Slog.i(LOG_TAG, "mSupportColorInverse:" + this.mSupportColorInverse);
        if (!this.mSupportColorInverse) {
            DisplayAdjustmentUtils.applyInversionSetting(this.mContext, userState.mUserId);
        } else {
            ContentResolver cr = this.mContext.getContentResolver();
            long identity = Binder.clearCallingIdentity();
            boolean invertColors = false;
            try {
                if (Settings.Secure.getIntForUser(cr, "accessibility_display_inversion_enabled", 0, userState.mUserId) != 0) {
                    invertColors = true;
                }
                Binder.restoreCallingIdentity(identity);
                if (this.mInvertColors == invertColors) {
                    Slog.i(LOG_TAG, "invertColors not change,return");
                    return;
                }
                this.mInvertColors = invertColors;
                Slog.i(LOG_TAG, "invertColors:" + invertColors);
                this.mDisplayEngineInterface.setScene(DE_SCENE_COLOR_INVERSE, invertColors ? DE_ACTION_MODE_ON : DE_ACTION_MODE_OFF);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    private void updateMagnificationLocked(UserState userState) {
        if (userState.mUserId == this.mCurrentUserId) {
            if (!this.mUiAutomationManager.suppressingAccessibilityServicesLocked() && (userState.mIsDisplayMagnificationEnabled || userState.mIsNavBarMagnificationEnabled || userHasListeningMagnificationServicesLocked(userState))) {
                getMagnificationController();
                this.mMagnificationController.register();
            } else if (this.mMagnificationController != null) {
                this.mMagnificationController.unregister();
            }
        }
    }

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

    private boolean userHasListeningMagnificationServicesLocked(UserState userState) {
        List<AccessibilityServiceConnection> services = userState.mBoundServices;
        int count = services.size();
        for (int i = 0; i < count; i++) {
            AccessibilityServiceConnection service = services.get(i);
            if (this.mSecurityPolicy.canControlMagnification(service) && service.isMagnificationCallbackEnabled()) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: finally extract failed */
    private void updateSoftKeyboardShowModeLocked(UserState userState) {
        if (userState.mUserId == this.mCurrentUserId && userState.mSoftKeyboardShowMode != 0 && !userState.mEnabledServices.contains(userState.mServiceChangingSoftKeyboardMode)) {
            long identity = Binder.clearCallingIdentity();
            try {
                Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0, userState.mUserId);
                Binder.restoreCallingIdentity(identity);
                userState.mSoftKeyboardShowMode = 0;
                userState.mServiceChangingSoftKeyboardMode = null;
                notifySoftKeyboardShowModeChangedLocked(userState.mSoftKeyboardShowMode);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateFingerprintGestureHandling(UserState userState) {
        List<AccessibilityServiceConnection> list;
        synchronized (this.mLock) {
            list = userState.mBoundServices;
            if (this.mFingerprintGestureDispatcher == null && this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                int numServices = list.size();
                int i = 0;
                while (true) {
                    if (i >= numServices) {
                        break;
                    }
                    if (list.get(i).isCapturingFingerprintGestures()) {
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
        List<AccessibilityServiceConnection> services = list;
        if (this.mFingerprintGestureDispatcher != null) {
            this.mFingerprintGestureDispatcher.updateClientList(services);
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

    @GuardedBy("mLock")
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

    public KeyEventDispatcher getKeyEventDispatcher() {
        if (this.mKeyEventDispatcher == null) {
            this.mKeyEventDispatcher = new KeyEventDispatcher(this.mMainHandler, 8, this.mLock, this.mPowerManager);
        }
        return this.mKeyEventDispatcher;
    }

    public PendingIntent getPendingIntentActivity(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getActivity(context, requestCode, intent, flags);
    }

    public void performAccessibilityShortcut() {
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000 || this.mContext.checkCallingPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
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
            throw new SecurityException("performAccessibilityShortcut requires the WRITE_SECURE_SETTINGS permission");
        }
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
    public void sendAccessibilityEventLocked(AccessibilityEvent event, int userId) {
        event.setEventTime(SystemClock.uptimeMillis());
        this.mMainHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$X8i00nfnUx_qUoIgZixkfu6ddSY.INSTANCE, this, event, Integer.valueOf(userId)));
    }

    /* access modifiers changed from: private */
    public boolean isTalkBackService(ComponentName serviceName) {
        if (serviceName != null && TALKBACK_SERVICE_PKTNAME.equals(serviceName.getPackageName()) && TALKBACK_SERVICE_CLSNAME.equals(serviceName.getClassName())) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void notifyAccessibilityEnableChangedEvent(int enable) {
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
        if (this.mFingerprintGestureDispatcher == null) {
            return false;
        }
        return this.mFingerprintGestureDispatcher.onFingerprintGesture(gestureKeyCode);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, LOG_TAG, pw)) {
            synchronized (this.mLock) {
                pw.println("ACCESSIBILITY MANAGER (dumpsys accessibility)");
                pw.println();
                int userCount = this.mUserStates.size();
                for (int i = 0; i < userCount; i++) {
                    UserState userState = this.mUserStates.valueAt(i);
                    pw.append("User state[attributes:{id=" + userState.mUserId);
                    StringBuilder sb = new StringBuilder();
                    sb.append(", currentUser=");
                    sb.append(userState.mUserId == this.mCurrentUserId);
                    pw.append(sb.toString());
                    pw.append(", touchExplorationEnabled=" + userState.mIsTouchExplorationEnabled);
                    pw.append(", displayMagnificationEnabled=" + userState.mIsDisplayMagnificationEnabled);
                    pw.append(", navBarMagnificationEnabled=" + userState.mIsNavBarMagnificationEnabled);
                    pw.append(", autoclickEnabled=" + userState.mIsAutoclickEnabled);
                    if (this.mUiAutomationManager.isUiAutomationRunningLocked()) {
                        pw.append(", ");
                        this.mUiAutomationManager.dumpUiAutomationService(fd, pw, args);
                        pw.println();
                    }
                    pw.append("}");
                    pw.println();
                    pw.append("           services:{");
                    int serviceCount = userState.mBoundServices.size();
                    for (int j = 0; j < serviceCount; j++) {
                        if (j > 0) {
                            pw.append(", ");
                            pw.println();
                            pw.append("                     ");
                        }
                        userState.mBoundServices.get(j).dump(fd, pw, args);
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
                        pw.append(this.mSecurityPolicy.mWindows.get(j2).toString());
                        pw.append(']');
                    }
                }
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
    public IBinder findWindowTokenLocked(int windowId) {
        IBinder token = this.mGlobalWindowTokens.get(windowId);
        if (token != null) {
            return token;
        }
        return getCurrentUserStateLocked().mWindowTokens.get(windowId);
    }

    /* access modifiers changed from: private */
    public int findWindowIdLocked(IBinder token) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x004d, code lost:
        r13 = r0;
        r17 = android.os.Binder.getCallingPid();
        r10 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r1.mPowerManager.userActivity(android.os.SystemClock.uptimeMillis(), 3, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0061, code lost:
        if (r12 == null) goto L_0x007d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        ((android.app.ActivityManagerInternal) com.android.server.LocalServices.getService(android.app.ActivityManagerInternal.class)).setFocusedActivity(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x006f, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0070, code lost:
        r1 = r10;
        r19 = r12;
        r18 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0077, code lost:
        r1 = r10;
        r19 = r12;
        r18 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0081, code lost:
        r1 = r10;
        r19 = r12;
        r18 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        com.android.server.accessibility.AccessibilityManagerService.RemoteAccessibilityConnection.access$2200(r13).performAccessibilityAction(r22, r14, r25, r26, r27, r28, r17, r29);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0098, code lost:
        android.os.Binder.restoreCallingIdentity(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x009c, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x009d, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00a1, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00a2, code lost:
        r1 = r10;
        r19 = r12;
        r18 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00a7, code lost:
        android.os.Binder.restoreCallingIdentity(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00aa, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ac, code lost:
        r1 = r10;
        r19 = r12;
        r18 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00b1, code lost:
        android.os.Binder.restoreCallingIdentity(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00b5, code lost:
        return false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x002a A[Catch:{ all -> 0x00bc }] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0038 A[SYNTHETIC, Splitter:B:22:0x0038] */
    public boolean performAccessibilityAction(int resolvedWindowId, long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, int fetchFlags, long interrogatingTid) {
        boolean isA11yFocusAction;
        AccessibilityWindowInfo a11yWindowInfo;
        IBinder activityToken;
        int i = resolvedWindowId;
        int i2 = action;
        IBinder activityToken2 = null;
        synchronized (this.mLock) {
            try {
                RemoteAccessibilityConnection connection = getConnectionLocked(resolvedWindowId);
                if (connection == null) {
                    return false;
                }
                if (i2 != 64) {
                    if (i2 != 128) {
                        isA11yFocusAction = false;
                        a11yWindowInfo = this.mSecurityPolicy.findA11yWindowInfoById(i);
                        if (!isA11yFocusAction) {
                            WindowInfo windowInfo = this.mSecurityPolicy.findWindowInfoById(i);
                            if (windowInfo != null) {
                                activityToken2 = windowInfo.activityToken;
                            }
                        }
                        activityToken = activityToken2;
                        if (a11yWindowInfo != null) {
                            try {
                                if (a11yWindowInfo.isInPictureInPictureMode() && this.mPictureInPictureActionReplacingConnection != null && !isA11yFocusAction) {
                                    connection = this.mPictureInPictureActionReplacingConnection;
                                }
                            } catch (Throwable th) {
                                re = th;
                                IBinder iBinder = activityToken;
                                throw re;
                            }
                        }
                    }
                }
                isA11yFocusAction = true;
                a11yWindowInfo = this.mSecurityPolicy.findA11yWindowInfoById(i);
                if (!isA11yFocusAction) {
                }
                activityToken = activityToken2;
                if (a11yWindowInfo != null) {
                }
                try {
                } catch (Throwable th2) {
                    re = th2;
                    IBinder iBinder2 = activityToken;
                    throw re;
                }
            } catch (Throwable th3) {
                re = th3;
                throw re;
            }
        }
    }

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

    public IAccessibilityInteractionConnectionCallback replaceCallbackIfNeeded(IAccessibilityInteractionConnectionCallback originalCallback, int resolvedWindowId, int interactionId, int interrogatingPid, long interrogatingTid) {
        AccessibilityWindowInfo windowInfo = this.mSecurityPolicy.findA11yWindowInfoById(resolvedWindowId);
        if (windowInfo == null || !windowInfo.isInPictureInPictureMode() || this.mPictureInPictureActionReplacingConnection == null) {
            return originalCallback;
        }
        ActionReplacingCallback actionReplacingCallback = new ActionReplacingCallback(originalCallback, this.mPictureInPictureActionReplacingConnection.mConnection, interactionId, interrogatingPid, interrogatingTid);
        return actionReplacingCallback;
    }

    public void onClientChange(boolean serviceInfoChanged) {
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(this.mCurrentUserId);
            onUserStateChangedLocked(userState);
            if (serviceInfoChanged) {
                scheduleNotifyClientsOfServicesStateChange(userState);
            }
        }
    }

    /* access modifiers changed from: private */
    public AppWidgetManagerInternal getAppWidgetManager() {
        AppWidgetManagerInternal appWidgetManagerInternal;
        synchronized (this.mLock) {
            if (this.mAppWidgetService == null && this.mPackageManager.hasSystemFeature("android.software.app_widgets")) {
                this.mAppWidgetService = (AppWidgetManagerInternal) LocalServices.getService(AppWidgetManagerInternal.class);
            }
            appWidgetManagerInternal = this.mAppWidgetService;
        }
        return appWidgetManagerInternal;
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new AccessibilityShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
    }
}
